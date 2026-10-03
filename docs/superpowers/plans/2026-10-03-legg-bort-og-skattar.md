# Legg bort og skattar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a six-year-old put things, furniture and figures away with one gesture, collect treasure in a safe place, get between floors by tapping what looks like the way, and see equal stored furniture as one card with a count.

**Architecture:** Every rule lives in a small pure object that a JVM test can drive (`StoreStacks`, `Treasure`, `TreasureBox`, `TreasureStart`, `TapHint`, `AwayCorner`, `PanelIdle`, `StairDoorways`). `Sim` and `Engine` only call them. The engine wiring is covered by Android tests that drive the real pointer engine, added to the existing `SharedPlayTest`.

**Tech Stack:** Kotlin, Jetpack Compose (everything drawn on `Canvas`), JUnit 4, Gradle. Android min SDK 26.

**Spec:** `docs/superpowers/specs/2026-10-03-legg-bort-og-skattar-design.md`. Read it first. Also read `AGENTS.md` and `docs/AI_INSTRUCTIONS.md`.

## Global Constraints

- Branch `claude/legg-bort`, worktree `C:\topa\.claude\worktrees\legg-bort`, based on published 1.7.1 (`2197745`). Never work in `C:\topa` itself: that checkout is stale and holds old uncommitted work that must stay untouched.
- Source root: `app/src/main/java/app/trollfoss/`. JVM tests: `app/src/test/java/app/trollfoss/`. Android tests: `app/src/androidTest/java/app/trollfoss/`.
- Every text a child sees exists in nynorsk and bokmål as `Txt(nn, nb)`. This plan adds exactly one: `Txt("Skattekiste","Skattekiste")`. No other new child-facing text.
- Never: ads, purchases, accounts, tracking, countdowns, punishment, scoreboards, text the child must read.
- Humour is kind: nobody gets hurt, nothing is lost.
- No change to the save format beyond one new flag in `world.flags` (`layout:treasure-box:1`) and one new `FixtureType` (`TREASURE_BOX`, saved by name). Old saves must load unchanged.
- No new signing key, no version bump, no release. A release happens only when the user asks.
- Code comments are in English and match the density of the surrounding code.
- Commit messages are in English and end with the line `Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>`.
- All Gradle runs go through the machine-wide build lock, from the worktree folder:
  - One JVM test class: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.StoreStacksTest" -Plain`
  - All JVM tests: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest" -Plain`
  - Android tests need a running emulator (see Task 9) and the package suffix `.leggbort`, so no private world is touched.
- `local.properties` (git-ignored, holds `sdk.dir`) must exist in the worktree. It was copied from `C:\topa\local.properties` when the worktree was made. If it is missing, copy it again.
- Only one emulator at a time. Stop the phone emulator before starting the tablet.

## Review Focus

1. **A tap on something in the open bag tray near the bag.** The bigger touch area must not swallow it. Expect: the thing comes out of the bag. Pinned in Task 6 (`reach` test).
2. **Carrying a figure low along the floor to the right edge.** Expect: outside the corner the camera still follows; inside the corner it stays put and the figure goes in the bag only when let go there. Pinned in Task 6 (camera test).
3. **A treasure box with finds in it dropped on the corner or the panel.** Expect: it stays in the room, opens, and no find falls out or disappears. Pinned in Task 3 (store test) and Task 6 (furniture test).
4. **An old 1.7.1 save, and a save where the child already put the box in the store.** Expect: exactly one box appears once; never a second one. Pinned in Task 4.
5. **A place with more than 70 things, most of them treasure.** Expect: a machine may still make a new thing and no gem, coin or pearl goes in a puff. Pinned in Task 2.

## File Structure

| File | Responsibility |
| --- | --- |
| Create `domain/StoreStacks.kt` | Groups equal stored pieces into one stack with a count. |
| Create `domain/Treasure.kt` | `Treasure` (what counts as treasure, what a full place may drop), `TreasureBox` (the box rules), `TreasureStart` (the one-time gift). |
| Create `domain/TapHint.kt` | Which furniture answers a tap, and the nearest one to a missed tap. |
| Create `ui/play/AwayCorner.kt` | Geometry and choices of the put-away corner. |
| Create `ui/play/PanelIdle.kt` | Counts play while the furniture panel is open. |
| Create `ui/art/StairDoorways.kt` | The doorways painted on staircases, shared by art and touch. |
| Create `ui/art/TreasureBoxArt.kt` | Drawing of the treasure box and its glass. |
| Modify `domain/Fixtures.kt`, `domain/Decor.kt`, `domain/Sim.kt`, `domain/Designer.kt`, `domain/HouseAtticRules.kt`, `domain/HouseGroundRules.kt`, `domain/HouseGardenRules.kt`, `domain/HouseCellarRules.kt`, `domain/WorldFactory.kt`, `data/WorldStore.kt` | Hooks into the rules. |
| Modify `ui/play/Engine.kt`, `ui/screens/DesignerPanel.kt`, `ui/screens/PlayScreen.kt`, `ui/FurnitureLabels.kt`, `ui/art/FixtureArt.kt`, `ui/art/HouseGroundHallArt.kt`, `ui/art/HouseCellarTunnelArt.kt`, `ui/art/HouseUpperPassageArt.kt` | Wiring and drawing. |
| Modify `CHANGELOG.md`, `docs/DESIGN.md`, `docs/OVERLEVERING.md` | Documentation. |

All paths under `domain/`, `data/` and `ui/` are relative to `app/src/main/java/app/trollfoss/`.

---

### Task 1: Equal furniture in the store becomes one card with a count

**Files:**
- Create: `app/src/main/java/app/trollfoss/domain/StoreStacks.kt`
- Modify: `app/src/main/java/app/trollfoss/ui/screens/DesignerPanel.kt` (the `DesignTab.STORE` and `DesignTab.TRASH` branches, about lines 219–257)
- Test: `app/src/test/java/app/trollfoss/domain/StoreStacksTest.kt`

**Interfaces:**
- Consumes: `Stored(type, variant, mode, door)` (a data class in `domain/Decor.kt`), `world.storage: ArrayList<Stored>`, `world.discardedStorage: ArrayList<Pair<Int, Stored>>`, `Engine.addFromStore(index)`, `Engine.discardFromStore(index)`, `Engine.restoreStorage(index)`.
- Produces: `data class StoreStack(val item: Stored, val count: Int, val index: Int)`, `StoreStacks.of(storage: List<Stored>): List<StoreStack>`, `StoreStacks.ofDiscarded(discarded: List<Pair<Int, Stored>>): List<StoreStack>`.

- [ ] **Step 1: Write the failing test**

```kotlin
package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Equal pieces in the store are shown as one card with a count. */
class StoreStacksTest {
    private val pot0 = Stored(FixtureType.FLOWER_POT, 0)
    private val pot1 = Stored(FixtureType.FLOWER_POT, 1)
    private val sofa = Stored(FixtureType.SOFA, 0)

    @Test fun `equal pieces become one stack that counts them and points at the newest`() {
        val stacks = StoreStacks.of(listOf(pot0, sofa, pot0, pot1, pot0))
        assertEquals(listOf(StoreStack(pot0, 3, 4), StoreStack(sofa, 1, 1), StoreStack(pot1, 1, 3)), stacks)
    }

    @Test fun `pieces that differ in mode or door stay apart`() {
        val art1 = Stored(FixtureType.PLAY_ART, 0, mode = 3)
        val art2 = Stored(FixtureType.PLAY_ART, 0, mode = 4)
        val door1 = Stored(FixtureType.PLAY_DOOR, 0, 0, RoomLink(PlaceId.HOME, 1))
        val door2 = Stored(FixtureType.PLAY_DOOR, 0, 0, RoomLink(PlaceId.HOME, 2))
        val stacks = StoreStacks.of(listOf(art1, art2, door1, door2))
        assertEquals(4, stacks.size)
        assertTrue(stacks.all { it.count == 1 })
    }

    @Test fun `an empty store has no stacks`() {
        assertTrue(StoreStacks.of(emptyList()).isEmpty())
    }

    @Test fun `taking one from a stack leaves a smaller stack in the same place`() {
        val w = WorldFactory.create(); val s = Sim(w)
        w.storage += listOf(pot0, sofa, pot0, pot0)
        val stack = StoreStacks.of(w.storage).first()
        assertEquals(3, stack.count)
        assertNotNull(s.designer.unstore(PlaceId.BEACH, stack.index, 1f, 0.9f))
        assertEquals(listOf(StoreStack(pot0, 2, 2), StoreStack(sofa, 1, 1)), StoreStacks.of(w.storage))
    }

    @Test fun `the recycling box is grouped by the piece and points into the box`() {
        val stacks = StoreStacks.ofDiscarded(listOf(0 to pot0, 5 to sofa, 2 to pot0))
        assertEquals(listOf(StoreStack(pot0, 2, 2), StoreStack(sofa, 1, 1)), stacks)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.StoreStacksTest" -Plain`
Expected: compilation fails with `Unresolved reference: StoreStacks`.

- [ ] **Step 3: Write the implementation**

`app/src/main/java/app/trollfoss/domain/StoreStacks.kt`:

```kotlin
package app.trollfoss.domain

/** Equal pieces in the store, shown as one card: [count] of [item], and the [index] a tap takes (the newest). */
data class StoreStack(val item: Stored, val count: Int, val index: Int)

object StoreStacks {
    /** Groups equal pieces. A stack stands where its oldest piece lies in [storage], so the list keeps its order. */
    fun of(storage: List<Stored>): List<StoreStack> {
        val newest = LinkedHashMap<Stored, Int>()
        val count = HashMap<Stored, Int>()
        storage.forEachIndexed { i, item ->
            newest[item] = i
            count[item] = (count[item] ?: 0) + 1
        }
        return newest.map { (item, index) -> StoreStack(item, count.getValue(item), index) }
    }

    /** The recycling box grouped the same way; [StoreStack.index] points into [discarded]. */
    fun ofDiscarded(discarded: List<Pair<Int, Stored>>): List<StoreStack> = of(discarded.map { it.second })
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`, 5 tests passed.

- [ ] **Step 5: Show stacks in the panel**

In `ui/screens/DesignerPanel.kt`, replace the grid of the `DesignTab.STORE` branch (the `LazyVerticalGrid` that starts with `itemsIndexed(stored) { index, item ->`) with:

```kotlin
                        val stacks = remember(stored) { app.trollfoss.domain.StoreStacks.of(stored) }
                        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(stacks) { _, stack ->
                                val item = stack.item
                                // In the store the furniture just stands there, on a pale pad with no frame round it.
                                Box {
                                Tile(modifier = furnitureDrag(engine, item.type, item.variant, storeIndex = stack.index), look = TileLook.SOFT, feet = if (item.type.spec.wall) null else thumbSide * fixtureThumbFeet(item.type), onClick = { engine.addFromStore(stack.index) }) {
                                    FurnitureThumb(item.type, item.variant, place, false)
                                }
                                // Several of the same: one card, and how many.
                                if (stack.count > 1) CountBadge(stack.count, Modifier.align(Alignment.TopEnd).padding(2.dp))
                                RoundButton(SM.deleteStored.str(), onClick = { engine.discardFromStore(stack.index) },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp), size = 40.dp,
                                    tone = Tones.Cream, icon = DesignIcons.Bin)
                                }
                            }
                        }
```

Replace the grid of the `DesignTab.TRASH` branch (the `LazyVerticalGrid` with `itemsIndexed(discarded) { index, removed ->`) with:

```kotlin
                    else {
                        val stacks = remember(discarded) { app.trollfoss.domain.StoreStacks.ofDiscarded(discarded) }
                        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(stacks) { _, stack ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box {
                                        Tile(look = TileLook.SOFT, onClick = { engine.restoreStorage(stack.index) }) {
                                            FurnitureThumb(stack.item.type, stack.item.variant, place, false)
                                        }
                                        if (stack.count > 1) CountBadge(stack.count, Modifier.align(Alignment.TopEnd).padding(2.dp))
                                    }
                                    RoundButton(SM.restoreStored.str(), onClick = { engine.restoreStorage(stack.index) }, size = 40.dp, tone = Tones.Mint, icon = DesignIcons.Undo)
                                }
                            }
                        }
                    }
```

Keep the line `if (discarded.isEmpty()) GameText(SM.emptyTrash.str(), fontSize = 16.sp, color = T.Ink)` in front of that `else`.

Add this composable at the bottom of the file:

```kotlin
/** How many equal pieces one card stands for. */
@Composable
private fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier.size(26.dp).background(T.Berry, androidx.compose.foundation.shape.CircleShape).border(2.dp, T.Ink, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center,
    ) { GameText("$count", fontSize = 13.sp, style = MaterialTheme.typography.titleMedium, color = Color.White) }
}
```

- [ ] **Step 6: Build and run the JVM tests of the designer**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.StoreStacksTest --tests app.trollfoss.domain.DesignerTest :app:assembleDebug" -Plain`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/app/trollfoss/domain/StoreStacks.kt app/src/main/java/app/trollfoss/ui/screens/DesignerPanel.kt app/src/test/java/app/trollfoss/domain/StoreStacksTest.kt
git commit -m "Show equal stored furniture as one card with a count

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 2: No treasure disappears on its own

**Files:**
- Create: `app/src/main/java/app/trollfoss/domain/Treasure.kt`
- Modify: `app/src/main/java/app/trollfoss/domain/Sim.kt` (`enum class Fx` near line 22, `capPlace` near line 1444)
- Modify: `app/src/main/java/app/trollfoss/domain/HouseGroundRules.kt` (`cap`, near line 55)
- Modify: `app/src/main/java/app/trollfoss/domain/HouseGardenRules.kt` (`limit`, near line 132)
- Modify: `app/src/main/java/app/trollfoss/domain/HouseCellarRules.kt` (`limit`, near line 166)
- Modify: `app/src/main/java/app/trollfoss/domain/HouseAtticRules.kt` (`mapTable` near line 505, `chest` near line 523)
- Modify: `app/src/main/java/app/trollfoss/ui/play/Engine.kt` (`onFx`, near line 1699)
- Test: `app/src/test/java/app/trollfoss/domain/TreasureTest.kt`, `app/src/test/java/app/trollfoss/domain/HouseAtticTest.kt`

**Interfaces:**
- Consumes: `Thing.mode`, `Thing.inside` (the id of the furniture it lies in, or -1), `Thing.held`, `Thing.z` (a `Long`, higher is newer), `Sim.MAX_THINGS` (70), `HouseAtticRules.MAX_COINS` (18), `HouseAtticRules.MAX_GEMS` (6).
- Produces: `Treasure.TYPES: Set<ThingType>`, `Treasure.isTreasure(t: Thing): Boolean`, `Treasure.loose(world: World, place: PlaceId, type: ThingType): Int`, `Treasure.oldestToDrop(things: List<Thing>, keep: Thing?, spare: (Thing) -> Boolean = { false }): Thing?`, and three new `Fx` values used by later tasks: `Fx.TREASURE_IN`, `Fx.TREASURE_PARTY`, `Fx.TREASURE_EMPTY`.

- [ ] **Step 1: Write the failing tests**

`app/src/test/java/app/trollfoss/domain/TreasureTest.kt`:

```kotlin
package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Gems, coins and pearls are what a child collects: nothing takes them away on its own. */
class TreasureTest {
    @Test fun `a full place lets go of its oldest loose thing, but never of a treasure`() {
        val w = World()
        val gem = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1f, 0.9f).apply { z = 1L }
        val coin = w.addThing(ThingType.COIN, 0, PlaceId.HOME, 1.1f, 0.9f).apply { z = 2L }
        val pearl = w.addThing(ThingType.PEARL, 0, PlaceId.HOME, 1.15f, 0.9f).apply { z = 3L }
        val sock = w.addThing(ThingType.UP_SOCK, 0, PlaceId.HOME, 1.2f, 0.9f).apply { z = 4L }
        val ball = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1.3f, 0.9f).apply { z = 5L }
        assertSame(sock, Treasure.oldestToDrop(listOf(gem, coin, pearl, sock, ball), keep = ball))
        assertNull(Treasure.oldestToDrop(listOf(gem, coin, pearl), keep = null))
        // The newest thing is spared, and so is whatever the caller asks to spare.
        assertNull(Treasure.oldestToDrop(listOf(gem, sock), keep = sock))
        assertSame(ball, Treasure.oldestToDrop(listOf(sock, ball), keep = null) { it === sock })
    }

    @Test fun `things held or lying inside furniture are not loose`() {
        val w = World()
        val held = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1f, 0.9f).apply { held = true }
        val inside = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1.1f, 0.9f).apply { inside = 5 }
        val loose = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1.2f, 0.9f)
        assertEquals(1, Treasure.loose(w, PlaceId.HOME, ThingType.GEM))
        assertEquals(0, Treasure.loose(w, PlaceId.HOME, ThingType.COIN))
        assertTrue(listOf(held, inside, loose).all(Treasure::isTreasure))
    }

    @Test fun `a place full of treasure keeps every piece when a machine makes one more thing`() {
        val w = WorldFactory.create(Random(1)); val s = Sim(w); val place = PlaceId.TIVOLI
        val cart = w.fixturesIn(place).first { it.type == FixtureType.POPCORN_CART }
        val gems = (0 until Sim.MAX_THINGS + 5).map { w.addThing(ThingType.GEM, it % 5, place, 0.5f + it * 0.02f, place.floor) }
        s.tap(place, cart, 0f, -0.1f)
        assertTrue(gems.all { w.bodies[it.id] === it })
    }
}
```

Add these three tests to the end of `app/src/test/java/app/trollfoss/domain/HouseAtticTest.kt` (inside the class; it already has `newWorld()`, `sim(world)`, `step(sim, seconds)`, `fixture(world, index)`, `spawned` and `fxs`):

```kotlin
    // ------------------------------------------------------------------ treasure stays

    @Test
    fun `the chest never takes back a coin or a gem`() {
        val world = newWorld()
        val sim = sim(world)
        val chest = fixture(world, AtticIds.CHEST)
        repeat(12) {                       // open, shut, open …: six showers
            sim.tap(attic, chest, 0f, -0.1f)
            step(sim, 0.2f)
        }
        val made = spawned.filter { it.type == ThingType.COIN || it.type == ThingType.GEM }
        assertTrue("every coin and gem the chest threw out is still there", made.all { world.bodies[it.id] === it })
        assertTrue(Treasure.loose(world, attic, ThingType.COIN) <= HouseAtticRules.MAX_COINS)
        assertTrue(Treasure.loose(world, attic, ThingType.GEM) <= HouseAtticRules.MAX_GEMS)
    }

    @Test
    fun `with six loose gems about the chest keeps its next gem`() {
        val world = newWorld()
        val sim = sim(world)
        val mine = (Treasure.loose(world, attic, ThingType.GEM) until HouseAtticRules.MAX_GEMS)
            .map { world.addThing(ThingType.GEM, 0, attic, 7.6f + it * 0.05f, attic.floor) }
        spawned.clear()
        sim.tap(attic, fixture(world, AtticIds.CHEST), 0f, -0.1f)   // the first opening would give a gem
        assertTrue(spawned.none { it.type == ThingType.GEM })
        assertTrue(mine.all { world.bodies[it.id] === it })
    }

    @Test
    fun `a chest with nothing left to give coughs dust instead`() {
        val world = newWorld()
        val sim = sim(world)
        repeat(HouseAtticRules.MAX_COINS) { world.addThing(ThingType.COIN, 0, attic, 7.2f + it * 0.03f, attic.floor) }
        repeat(HouseAtticRules.MAX_GEMS) { world.addThing(ThingType.GEM, 0, attic, 7.9f + it * 0.05f, attic.floor) }
        spawned.clear(); fxs.clear()
        sim.tap(attic, fixture(world, AtticIds.CHEST), 0f, -0.1f)
        assertTrue(spawned.isEmpty())
        assertTrue(Fx.TREASURE_EMPTY in fxs)
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.TreasureTest --tests app.trollfoss.domain.HouseAtticTest" -Plain`
Expected: compilation fails with `Unresolved reference: Treasure`.

- [ ] **Step 3: Add `Treasure` and the new effects**

`app/src/main/java/app/trollfoss/domain/Treasure.kt`:

```kotlin
package app.trollfoss.domain

/** Gems, coins and pearls: what a child collects. Nothing in the game takes them away on its own. */
object Treasure {
    val TYPES = setOf(ThingType.GEM, ThingType.COIN, ThingType.PEARL)

    fun isTreasure(t: Thing): Boolean = t.type in TYPES

    /** How many [type] lie about in [place]: free, not held and not inside any furniture. */
    fun loose(world: World, place: PlaceId, type: ThingType): Int =
        world.bodiesIn(place).count { it is Thing && it.type == type && it.mode == Mode.FREE && !it.held && it.inside < 0 }

    /**
     * The oldest loose thing a full place may let go of: never a treasure, never [keep] (the thing just
     * made) and never one the caller wants to [spare]. Null when there is nothing it may take.
     */
    fun oldestToDrop(things: List<Thing>, keep: Thing?, spare: (Thing) -> Boolean = { false }): Thing? =
        things.filter { it !== keep && it.mode == Mode.FREE && it.inside < 0 && !it.held && !isTreasure(it) && !spare(it) }
            .minByOrNull { it.z }
}
```

In `domain/Sim.kt`, extend `enum class Fx`: after the lines

```kotlin
    // Rolf and Sture: a bow, a dusty sneeze; the code and the figure are in the param (see [FigurarEvent]).
    FIGURAR,
```

add

```kotlin

    // Collecting: a find lands in the treasure box, every fifth one is a party, and a chest with nothing left to give.
    TREASURE_IN, TREASURE_PARTY, TREASURE_EMPTY,
```

- [ ] **Step 4: Use it in the four place caps**

`domain/Sim.kt`, `capPlace`: replace the line that starts with `val oldest = things.filter {` with

```kotlin
        val oldest = Treasure.oldestToDrop(things, keep) ?: return
```

`domain/HouseGardenRules.kt`, `limit(keep: Thing)`: replace the line that starts with `val oldest = things.filter {` with

```kotlin
        val oldest = Treasure.oldestToDrop(things, keep) ?: return
```

`domain/HouseGroundRules.kt`, `cap(keep: Thing)`: replace the two lines of `val oldest = things.filter { … }.minByOrNull { it.z } ?: return` with

```kotlin
        val oldest = Treasure.oldestToDrop(things, keep) { it.type == ThingType.GOLDEN_KEY } ?: return
```

`domain/HouseCellarRules.kt`, `limit(type: ThingType, max: Int)`: add as the first line of the function

```kotlin
        if (type in Treasure.TYPES) return
```

- [ ] **Step 5: Change the attic rules**

`domain/HouseAtticRules.kt`, in `mapTable`, replace

```kotlin
            val t = toss(ThingType.GEM, random.nextInt(ThingType.GEM.variants), f, 0f, -0.2f, 0.4f, -1.9f)
            t.vrot = 200f
            trim(MAX_GEMS, setOf(ThingType.GEM))
```

with

```kotlin
            // The child keeps what it has found: with enough gems about, the map keeps this one.
            if (Treasure.loose(world, place, ThingType.GEM) < MAX_GEMS) {
                val t = toss(ThingType.GEM, random.nextInt(ThingType.GEM.variants), f, 0f, -0.2f, 0.4f, -1.9f)
                t.vrot = 200f
            }
```

In `chest`, replace

```kotlin
        for (i in 0 until 6) {
            val t = toss(ThingType.COIN, 0, f, (i - 2.5f) * 0.02f, -0.14f, (i - 2.5f) * 0.22f + (random.nextFloat() - 0.5f) * 0.1f, -2.2f - random.nextFloat() * 0.9f)
            t.vrot = (random.nextFloat() - 0.5f) * 500f
        }
        if (f.count % 3 == 1) toss(ThingType.GEM, random.nextInt(ThingType.GEM.variants), f, 0f, -0.14f, 0.2f, -2.8f)
        trim(MAX_COINS, setOf(ThingType.COIN))
        trim(MAX_GEMS, setOf(ThingType.GEM))
        fx(AtticCode.CHEST, f.x, f.y - 0.2f, f, arg = 0)
```

with

```kotlin
        // Nothing the chest gave is ever taken back: it gives only what there is room for on the floor.
        val coins = (MAX_COINS - Treasure.loose(world, place, ThingType.COIN)).coerceIn(0, 6)
        for (i in 0 until coins) {
            val t = toss(ThingType.COIN, 0, f, (i - 2.5f) * 0.02f, -0.14f, (i - 2.5f) * 0.22f + (random.nextFloat() - 0.5f) * 0.1f, -2.2f - random.nextFloat() * 0.9f)
            t.vrot = (random.nextFloat() - 0.5f) * 500f
        }
        val gem = f.count % 3 == 1 && Treasure.loose(world, place, ThingType.GEM) < MAX_GEMS
        if (gem) toss(ThingType.GEM, random.nextInt(ThingType.GEM.variants), f, 0f, -0.14f, 0.2f, -2.8f)
        if (coins == 0 && !gem) sim.listener.onFx(Fx.TREASURE_EMPTY, f.x, f.y - 0.2f, f)
        else fx(AtticCode.CHEST, f.x, f.y - 0.2f, f, arg = 0)
```

Leave `trim` itself in place: toys, costumes and books still use it.

- [ ] **Step 6: Give the three new effects a sound and a picture**

`ui/play/Engine.kt`, in `onFx`, add these branches to the `when (fx)` (next to `Fx.STORE ->`):

```kotlin
            Fx.TREASURE_IN -> {
                s(Sfx.CHOMP, 0.55f, 1.25f)
                particles.burst(PKind.SPARK, x, y, 5, 0.3f, 0.009f, T.SunTop)
            }
            Fx.TREASURE_PARTY -> {
                s(Sfx.BURP, 0.8f)
                s(Sfx.SPARKLE, 0.6f)
                particles.burst(PKind.STAR, x, y, 14, 0.5f, 0.012f, T.Sun)
                laughAround(x, null)
            }
            Fx.TREASURE_EMPTY -> {
                // Nothing left to give: a cough of dust, and a moth that lived in there.
                s(Sfx.POOF, 0.6f)
                particles.burst(PKind.DUST, x, y, 10, 0.35f, 0.016f, up = 0.08f, life = 0.8f)
                particles.add(Particle(PKind.BUTTERFLY, x, y, 0.12f, -0.2f, 2.4f, 0.016f, Color(0xFFB9A58C)))
            }
```

- [ ] **Step 7: Run the tests to verify they pass**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`. The existing test `the treasure map leads to a gem, and the chest showers coins` must still pass.

- [ ] **Step 8: Run all JVM tests**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest" -Plain`
Expected: `BUILD SUCCESSFUL`. If a test of the garden, ground floor or cellar counted on a gem or coin going in a puff, change that test to expect the treasure to stay and say so in the commit message.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/app/trollfoss/domain/Treasure.kt app/src/main/java/app/trollfoss/domain/Sim.kt app/src/main/java/app/trollfoss/domain/HouseGroundRules.kt app/src/main/java/app/trollfoss/domain/HouseGardenRules.kt app/src/main/java/app/trollfoss/domain/HouseCellarRules.kt app/src/main/java/app/trollfoss/domain/HouseAtticRules.kt app/src/main/java/app/trollfoss/ui/play/Engine.kt app/src/test/java/app/trollfoss/domain/TreasureTest.kt app/src/test/java/app/trollfoss/domain/HouseAtticTest.kt
git commit -m "Keep gems, coins and pearls from disappearing on their own

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 3: The treasure box

**Files:**
- Modify: `app/src/main/java/app/trollfoss/domain/Fixtures.kt` (the end of `enum class FixtureType`, and the `when` in `build`)
- Modify: `app/src/main/java/app/trollfoss/domain/Treasure.kt` (add `class TreasureBox`)
- Modify: `app/src/main/java/app/trollfoss/domain/Sim.kt` (member, `step`, `land`, `tap`, `dropInto`)
- Modify: `app/src/main/java/app/trollfoss/domain/Designer.kt` (`canStore`, `storeNow`)
- Modify: `app/src/main/java/app/trollfoss/domain/Decor.kt` (`catalogue`)
- Modify: `app/src/main/java/app/trollfoss/ui/FurnitureLabels.kt`
- Create: `app/src/main/java/app/trollfoss/ui/art/TreasureBoxArt.kt`
- Modify: `app/src/main/java/app/trollfoss/ui/art/FixtureArt.kt` (the two `when (f.type)` blocks, near lines 40 and 149)
- Modify: `app/src/main/java/app/trollfoss/ui/play/Engine.kt` (`accepts`, near line 394)
- Test: `app/src/test/java/app/trollfoss/domain/TreasureBoxTest.kt`

**Interfaces:**
- Consumes: `Fx.TREASURE_IN`, `Fx.TREASURE_PARTY` (Task 2); `FixtureSpec(w, h, container, glass, surfaces, dropZone)`; `SurfaceSpec(x1, x2, dy, interior, closedOnly)`; `Sim.invalidate(place)`; `Fixture.timer` (not saved, free for this type); `Sim.settle(place)` gives every thing lying on an interior shelf its `inside` again after loading.
- Produces: `FixtureType.TREASURE_BOX`; `Sim.treasure: TreasureBox` with `holds(f: Fixture): List<Thing>`, `tap(place, f)`, `step(place, dt)`, `put(place, f, t): Boolean`, `landed(place, t, owner: Int)`, `arrange(f): List<Thing>`, `refuse(place, f)`; constants `TreasureBox.REACH = 0.25f`, `TreasureBox.LINGER = 1.2f`, `TreasureBox.PARTY = 5`.

- [ ] **Step 1: Write the failing test**

`app/src/test/java/app/trollfoss/domain/TreasureBoxTest.kt`:

```kotlin
package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/** The glass-fronted treasure box: easy to hit, shows what is in it, and never loses a find. */
class TreasureBoxTest {
    private val place = PlaceId.HOME
    private val fxs = ArrayList<Fx>()

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) { fxs += fx }
    }, Random(1))

    private fun box(sim: Sim) = sim.designer.add(place, FixtureType.TREASURE_BOX, 0, 1.6f, 0.9f)!!

    private fun step(sim: Sim, seconds: Float) {
        var t = 0f
        while (t < seconds) { sim.step(place, 1f / 60f); t += 1f / 60f }
    }

    private fun gem(world: World, f: Fixture) = world.addThing(ThingType.GEM, 0, place, f.x, f.y - 0.1f)

    @Test fun `the box has a glass front and room inside`() {
        val spec = FixtureType.TREASURE_BOX.spec
        assertTrue(spec.glass)
        assertTrue(spec.container != null && spec.dropZone != null)
        assertEquals(3, spec.surfaces.count { it.interior })
    }

    @Test fun `a thing let go over the box lies on a shelf inside it`() {
        val w = World(); val s = sim(w); val f = box(s)
        val t = gem(w, f)
        assertTrue(s.dropInto(place, f, t))
        assertEquals(f.id, t.inside)
        assertTrue(t.resting)
        assertEquals(listOf(t), s.treasure.holds(f))
        assertTrue(Fx.TREASURE_IN in fxs)
    }

    @Test fun `twenty finds each get a place of their own inside the box`() {
        val w = World(); val s = sim(w); val f = box(s)
        repeat(20) { assertTrue(s.dropInto(place, f, gem(w, f))) }
        val inside = s.treasure.holds(f)
        assertEquals(20, inside.size)
        assertEquals(20, inside.map { it.x to it.y }.toSet().size)
        val room = f.spec.container!!
        for (t in inside) {
            assertTrue(abs(t.x - f.x) <= room.right)
            assertTrue(t.y <= f.y + room.bottom + 0.0001f && t.y - t.h >= f.y + room.top)
        }
    }

    @Test fun `every fifth find sets off the party`() {
        val w = World(); val s = sim(w); val f = box(s)
        repeat(4) { s.dropInto(place, f, gem(w, f)) }
        assertEquals(0, fxs.count { it == Fx.TREASURE_PARTY })
        s.dropInto(place, f, gem(w, f))
        assertEquals(1, fxs.count { it == Fx.TREASURE_PARTY })
        repeat(5) { s.dropInto(place, f, gem(w, f)) }
        assertEquals(2, fxs.count { it == Fx.TREASURE_PARTY })
    }

    @Test fun `the lid lifts for a held thing nearby and falls shut a moment after it is gone`() {
        val w = World(); val s = sim(w); val f = box(s)
        val t = w.addThing(ThingType.GEM, 0, place, f.x + 0.1f, f.y - 0.1f).apply { held = true }
        step(s, 0.1f)
        assertTrue(f.open)
        t.held = false; t.x = f.x + 1.2f
        step(s, 1.0f)
        assertTrue("still open after one second", f.open)
        step(s, 0.4f)
        assertFalse(f.open)
    }

    @Test fun `a thing held far away leaves the lid shut`() {
        val w = World(); val s = sim(w); val f = box(s)
        w.addThing(ThingType.GEM, 0, place, f.x + 0.6f, f.y - 0.1f).apply { held = true }
        step(s, 0.5f)
        assertFalse(f.open)
    }

    @Test fun `a tap opens the box by hand and it stays the way the child left it`() {
        val w = World(); val s = sim(w); val f = box(s)
        s.tap(place, f, 0f, -0.1f)
        assertTrue(f.open)
        step(s, 3f)
        assertTrue(f.open)
        s.tap(place, f, 0f, -0.1f)
        assertFalse(f.open)
    }

    @Test fun `a box with finds in it stays out of the store and an empty one goes in`() {
        val w = World(); val s = sim(w); val f = box(s)
        val t = gem(w, f)
        s.dropInto(place, f, t)
        assertFalse(s.designer.store(place, f))
        assertTrue(w.fixtures[f.id] === f)
        assertTrue("it opens to show why", f.open)
        assertEquals(listOf(t), s.treasure.holds(f))
        // The child takes the gem out.
        t.inside = -1; t.resting = false; t.restOwner = -2; t.x = f.x + 1f; t.y = place.floor
        assertTrue(s.designer.store(place, f))
        assertEquals(FixtureType.TREASURE_BOX, w.storage.last().type)
    }

    @Test fun `finds are still in the box after saving and loading`() {
        val w = World(); val s = sim(w); val f = box(s)
        repeat(3) { s.dropInto(place, f, gem(w, f)) }
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val s2 = Sim(loaded)
        s2.settle(place)
        val f2 = loaded.fixtures.getValue(f.id)
        assertEquals(FixtureType.TREASURE_BOX, f2.type)
        assertEquals(3, s2.treasure.holds(f2).size)
    }

    @Test fun `the box is free in the catalogue of every place`() {
        val w = World()
        for (p in PlaceId.entries) {
            val item = Decor.catalogue(p).firstOrNull { it.type == FixtureType.TREASURE_BOX }
            assertTrue("$p", item != null && item.stickers == 0 && Decor.available(w, item))
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.TreasureBoxTest" -Plain`
Expected: compilation fails with `Unresolved reference: TREASURE_BOX`.

- [ ] **Step 3: Add the type and its spec**

`domain/Fixtures.kt`: in `enum class FixtureType`, after the line `PLAY_TREE, PLAY_REPAIR, PLAY_DOOR, PLAY_TUNNEL, PLAY_JUMP, PLAY_WATER_WHEEL, PLAY_ART, PLAY_RESCUE,` and before the `;`, add

```kotlin
    // Collecting: a glass-fronted box for gems, coins and other finds (see [TreasureBox]).
    TREASURE_BOX,
```

In the `when` of `build` in the same file, next to `TOY_BOX -> FixtureSpec(`, add

```kotlin
            TREASURE_BOX -> FixtureSpec(
                0.26f, 0.20f,
                container = RRect(-0.12f, -0.19f, 0.12f, -0.015f),
                glass = true,
                // Generous: a find let go anywhere over the box, or just above it, goes in.
                dropZone = RRect(-0.17f, -0.36f, 0.17f, 0.02f),
                surfaces = listOf(
                    SurfaceSpec(-0.11f, 0.11f, -0.015f, interior = true),
                    SurfaceSpec(-0.11f, 0.11f, -0.07f, interior = true),
                    SurfaceSpec(-0.11f, 0.11f, -0.125f, interior = true),
                    SurfaceSpec(-0.13f, 0.13f, -0.20f, closedOnly = true),
                ),
            )
```

- [ ] **Step 4: Add the rules**

Append to `domain/Treasure.kt` (and add `import kotlin.math.hypot` under the `package` line):

```kotlin
/**
 * The glass-fronted treasure box: the lid lifts for a hand that comes near with a find, the finds line up on
 * three shelves so each can be seen, and a box with finds in it never goes into the store.
 */
class TreasureBox(private val sim: Sim) {
    private val world get() = sim.world
    private val listener get() = sim.listener

    /** What lies in [f], oldest first. */
    fun holds(f: Fixture): List<Thing> =
        world.bodiesIn(f.place).filterIsInstance<Thing>()
            .filter { it.inside == f.id && it.mode == Mode.FREE && !it.held }.sortedBy { it.z }

    /** A tap opens or shuts the box by hand; then it stays the way the child left it ([Fixture.timer] 0). */
    fun tap(place: PlaceId, f: Fixture) {
        f.open = !f.open
        f.timer = 0f
        sim.invalidate(place)
        listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2f, f)
    }

    /** The lid lifts for a held thing that comes near, and falls shut [LINGER] seconds after it is gone. */
    fun step(place: PlaceId, dt: Float) {
        for (f in world.fixturesIn(place)) {
            if (f.type != FixtureType.TREASURE_BOX) continue
            val cy = f.y - f.spec.h / 2f
            val near = world.bodiesIn(place).any { it is Thing && it.held && hypot(it.x - f.x, it.y - it.h / 2f - cy) < REACH }
            when {
                near && !f.open -> {
                    f.open = true
                    f.timer = LINGER
                    sim.invalidate(place)
                    listener.onFx(Fx.OPEN, f.x, cy, f)
                }
                near && f.timer > 0f -> f.timer = LINGER
                !near && f.open && f.timer > 0f -> {
                    f.timer -= dt
                    if (f.timer <= 0f) {
                        f.timer = 0f
                        f.open = false
                        sim.invalidate(place)
                        listener.onFx(Fx.CLOSE, f.x, cy, f)
                    }
                }
            }
        }
    }

    /** A thing let go over the box goes straight onto a shelf. */
    fun put(place: PlaceId, f: Fixture, t: Thing): Boolean {
        t.place = place
        t.mode = Mode.FREE
        t.holder = -1
        t.held = false
        t.inside = f.id
        t.z = world.nextZ()
        received(f)
        return true
    }

    /** A thing that came to rest on a shelf by itself (thrown in, or dropped just above). */
    fun landed(t: Thing, owner: Int) {
        val f = world.fixtures[owner] ?: return
        if (f.type == FixtureType.TREASURE_BOX) received(f)
    }

    private fun received(f: Fixture) {
        val things = arrange(f)
        listener.onFx(Fx.TREASURE_IN, f.x, f.y - f.spec.h / 2f, f)
        if (things.isNotEmpty() && things.size % PARTY == 0) {
            f.anim = 1f
            listener.onFx(Fx.TREASURE_PARTY, f.x, f.top, f)
        }
    }

    /** Lines everything in the box up on its shelves, seven to a row, the oldest at the bottom left. */
    fun arrange(f: Fixture): List<Thing> {
        val things = holds(f)
        things.forEachIndexed { i, t ->
            val slot = i % SLOTS
            t.x = f.x + FIRST_X + (slot % PER_ROW) * STEP_X
            t.y = f.y + ROWS[slot / PER_ROW]
            t.vx = 0f
            t.vy = 0f
            t.vrot = 0f
            t.rot = 0f
            t.resting = true
            t.restOwner = f.id
            t.inside = f.id
            t.ground = f.depth + 0.012f
        }
        return things
    }

    /** A box with finds in it stays out of the store: it opens and wobbles, so the child sees why. */
    fun refuse(place: PlaceId, f: Fixture) {
        if (!f.open) {
            f.open = true
            sim.invalidate(place)
        }
        f.timer = LINGER
        f.anim = 1f
        listener.onFx(Fx.OPEN, f.x, f.y - f.spec.h / 2f, f)
    }

    companion object {
        /** How near a held thing must come for the lid to lift, in scene units. */
        const val REACH = 0.25f

        /** Seconds the lid stays up after the thing is gone. */
        const val LINGER = 1.2f

        /** Every this many finds, the box has a little party. */
        const val PARTY = 5

        const val PER_ROW = 7
        const val SLOTS = 21
        const val FIRST_X = -0.09f
        const val STEP_X = 0.03f

        /** The three shelves, as heights above the bottom of the box (the interior surfaces of its spec). */
        val ROWS = floatArrayOf(-0.015f, -0.07f, -0.125f)
    }
}
```

- [ ] **Step 5: Hook the rules into `Sim`**

`domain/Sim.kt`:

After the line `val playerFollow = PlayerFollow(this)` add

```kotlin
    val treasure = TreasureBox(this)
```

In `fun step(place: PlaceId, dt: Float)`, after the line `toys.tick(place, dt)` add

```kotlin
        treasure.step(place, dt)
```

In `private fun land(place: PlaceId, b: Body, s: Surface)`, in the `else ->` branch, after the line `listener.onLand(b, impact)` add

```kotlin
                if (b is Thing && s.interior) treasure.landed(b, s.owner)
```

In `fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float)`, in `when (f.type)`, before the line `FixtureType.TOY_BOX -> {` add

```kotlin
            FixtureType.TREASURE_BOX -> treasure.tap(place, f)
```

In `fun dropInto(place: PlaceId, f: Fixture, t: Thing): Boolean`, after the line `if (toys.drop(f, t)) return true` add

```kotlin
        if (f.type == FixtureType.TREASURE_BOX) return treasure.put(place, f, t)
```

- [ ] **Step 6: Keep a full box out of the store**

`domain/Designer.kt`, in `canStore`, after the line `if (!sim.movable(f)) return false` add

```kotlin
        // A treasure box with finds in it stays where it is: nothing the child collected may fall out or get lost.
        if (f.type == FixtureType.TREASURE_BOX && sim.treasure.holds(f).isNotEmpty()) return false
```

In `storeNow`, replace the line `if (!canStore(place, f)) return false` with

```kotlin
        if (f.type == FixtureType.TREASURE_BOX && sim.treasure.holds(f).isNotEmpty()) sim.treasure.refuse(place, f)
        if (!canStore(place, f)) return false
```

- [ ] **Step 7: Catalogue and label**

`domain/Decor.kt`, in `catalogue(place)`: change `return toys + when {` to

```kotlin
        // The treasure box is free everywhere: a child collects wherever it plays.
        return toys + CatalogueItem(FixtureType.TREASURE_BOX) + when {
```

`ui/FurnitureLabels.kt`, in the `names` map, after the line `FixtureType.TOY_BOX to Txt("Leikekasse","Lekekasse"),` add

```kotlin
        FixtureType.TREASURE_BOX to Txt("Skattekiste","Skattekiste"),
```

- [ ] **Step 8: Draw the box**

`app/src/main/java/app/trollfoss/ui/art/TreasureBoxArt.kt`:

```kotlin
package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture

private val TreasureWood = Color(0xFF8A5A3A)
private val TreasureBrass = Color(0xFFD9A441)

/** The treasure box, back layer: a dark inside with three shelves, and the lid standing up when it is open. */
internal fun DrawScope.fxTreasureBox(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val d = 0.1f
    // Little brass feet.
    for (x in floatArrayOf(-0.11f, 0.11f)) fxBox(u, x - 0.012f, -0.012f, x + 0.012f, 0f, 0.014f, TreasureBrass, pen, z = d - 0.02f)
    if (f.open) {
        // The lid, tipped up behind the box, with a star on it.
        fxBox(u, -0.134f, -0.33f, 0.134f, -0.2f, 0.016f, TreasureWood.darken(0.08f), pen, rad = 0.008f, z = d - 0.004f)
        val star = starPath(p(0f, -0.265f), 0.02f * u, 0.009f * u)
        drawPath(star, TreasureBrass)
        drawPath(star, Ink.line, style = pen.thin)
    }
    // The box itself, then its dark inside and the shelves the finds lie on.
    fxBox(u, -0.13f, -0.198f, 0.13f, -0.008f, d, TreasureWood, pen, rad = 0.006f)
    val inside = Rect(p(-0.12f, -0.19f), p(0.12f, -0.015f))
    drawRect(Color(0xFF2B2140), inside.topLeft, inside.size)
    for (y in floatArrayOf(-0.07f, -0.125f)) {
        drawLine(TreasureWood.lighten(0.1f), p(-0.12f, y), p(0.12f, y), strokeWidth = 0.006f * u)
        drawLine(Ink.line, p(-0.12f, y + 0.004f), p(0.12f, y + 0.004f), strokeWidth = pen.lw * 0.6f)
    }
    drawRect(Ink.line, inside.topLeft, inside.size, style = pen.thin)
    if (!f.open) {
        // The shut lid: a low band with a brass star for a lock.
        fxBox(u, -0.134f, -0.21f, 0.134f, -0.194f, d + 0.004f, TreasureWood.darken(0.08f), pen, rad = 0.005f)
        val star = starPath(p(0f, -0.202f), 0.012f * u, 0.005f * u)
        drawPath(star, TreasureBrass)
        drawPath(star, Ink.line, style = pen.thin)
    }
}

/** The treasure box, front layer: the glass over the finds, a brass frame and one streak of light. */
internal fun DrawScope.fxTreasureGlass(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val pane = Rect(p(-0.12f, -0.19f), p(0.12f, -0.015f))
    drawRect(Color(0x30BFE8FF), pane.topLeft, pane.size)
    val streak = Path().apply {
        moveTo(pane.left + 0.03f * u, pane.bottom)
        lineTo(pane.left + 0.06f * u, pane.bottom)
        lineTo(pane.left + 0.11f * u, pane.top)
        lineTo(pane.left + 0.08f * u, pane.top)
        close()
    }
    drawPath(streak, Color.White.copy(alpha = 0.28f))
    drawRect(TreasureBrass, pane.topLeft, pane.size, style = androidx.compose.ui.graphics.drawscope.Stroke(0.008f * u))
    drawRect(Ink.line, pane.topLeft, pane.size, style = pen.thin)
}
```

`ui/art/FixtureArt.kt`: in the first `when (f.type)` (the back layer), after the line `FixtureType.CHEST -> fxChest(f, u, pen)` add

```kotlin
            FixtureType.TREASURE_BOX -> fxTreasureBox(f, u, pen)
```

In the second `when (f.type)` (the front layer), after the line `FixtureType.DISPLAY_CASE -> fxDisplayGlass(f, u, pen)` add

```kotlin
            FixtureType.TREASURE_BOX -> fxTreasureGlass(f, u, pen)
```

If `darken`, `lighten`, `starPath` or `fxBox` do not resolve, they live in `ui/art/Ink.kt` and `ui/art/FixtureArt.kt` in the same package; `fxChest` in `ui/art/FixtureArtHome.kt` uses all four the same way.

- [ ] **Step 9: Show the landing ring over the box**

`ui/play/Engine.kt`, in `private fun accepts(f: Fixture, t: Thing): Boolean`, change the first line from

```kotlin
    private fun accepts(f: Fixture, t: Thing): Boolean = PlayInteractions.accepts(f, t) ||
```

to

```kotlin
    private fun accepts(f: Fixture, t: Thing): Boolean = PlayInteractions.accepts(f, t) || f.type == FixtureType.TREASURE_BOX ||
```

- [ ] **Step 10: Run the tests to verify they pass**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.TreasureBoxTest :app:assembleDebug" -Plain`
Expected: `BUILD SUCCESSFUL`, 10 tests passed.

- [ ] **Step 11: Run all JVM tests**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest" -Plain`
Expected: `BUILD SUCCESSFUL`. A test that lists every furniture type in a catalogue, or checks that every `FixtureType` has a label or art, may need `TREASURE_BOX` added to its expectations.

- [ ] **Step 12: Commit**

```bash
git add app/src/main/java/app/trollfoss/domain/Fixtures.kt app/src/main/java/app/trollfoss/domain/Treasure.kt app/src/main/java/app/trollfoss/domain/Sim.kt app/src/main/java/app/trollfoss/domain/Designer.kt app/src/main/java/app/trollfoss/domain/Decor.kt app/src/main/java/app/trollfoss/ui/FurnitureLabels.kt app/src/main/java/app/trollfoss/ui/art/TreasureBoxArt.kt app/src/main/java/app/trollfoss/ui/art/FixtureArt.kt app/src/main/java/app/trollfoss/ui/play/Engine.kt app/src/test/java/app/trollfoss/domain/TreasureBoxTest.kt
git commit -m "Add a glass-fronted treasure box for the child's finds

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 4: One box in Familiehuset, once

**Files:**
- Modify: `app/src/main/java/app/trollfoss/domain/Treasure.kt` (add `object TreasureStart`)
- Modify: `app/src/main/java/app/trollfoss/domain/WorldFactory.kt` (`create`, near line 33)
- Modify: `app/src/main/java/app/trollfoss/data/WorldStore.kt` (after `StarterLayout.upgrade(world)`, near line 432)
- Test: `app/src/test/java/app/trollfoss/domain/TreasureStartTest.kt`

**Interfaces:**
- Consumes: `FixtureType.TREASURE_BOX` (Task 3), `Designer.add(place, type, variant, x, y): Fixture?` (null when the place is full), `Decor.rooms(PlaceId.HOME)` (the first range is the bedroom), `PlaceId.FRONT`, `world.flags`, `world.storage`.
- Produces: `TreasureStart.FLAG = "layout:treasure-box:1"`, `TreasureStart.upgrade(world: World, sim: Sim = …)`.

- [ ] **Step 1: Write the failing test**

`app/src/test/java/app/trollfoss/domain/TreasureStartTest.kt`:

```kotlin
package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Every world gets one treasure box in the bedroom of Familiehuset, once. */
class TreasureStartTest {
    private fun boxes(w: World) = w.fixtures.values.filter { it.type == FixtureType.TREASURE_BOX }
    private fun reload(w: World) = WorldStore.decode(WorldStore.encode(w, Settings())).world

    /** What a save from 1.7.1 looks like: no box, no flag. */
    private fun oldWorld(): World = WorldFactory.create(Random(1)).also { w ->
        boxes(w).forEach { w.fixtures.remove(it.id) }
        w.flags.remove(TreasureStart.FLAG)
    }

    @Test fun `a new world has one treasure box in the bedroom of Familiehuset`() {
        val w = WorldFactory.create(Random(1))
        assertEquals(1, boxes(w).size)
        val box = boxes(w).single()
        assertEquals(PlaceId.HOME, box.place)
        assertEquals(0, Decor.roomAt(PlaceId.HOME, box.x))
        assertTrue(TreasureStart.FLAG in w.flags)
    }

    @Test fun `an old save gets the box once and keeps everything else`() {
        val old = oldWorld()
        val bodies = old.bodies.size
        val fixtures = old.fixtures.size
        val loaded = reload(old)
        assertEquals(1, boxes(loaded).size)
        assertEquals(fixtures + 1, loaded.fixtures.size)
        assertEquals(bodies, loaded.bodies.size)
        assertTrue(TreasureStart.FLAG in loaded.flags)
        // Loading again adds nothing.
        assertEquals(fixtures + 1, reload(loaded).fixtures.size)
    }

    @Test fun `a child who put the box in the store does not get a second one`() {
        val w = WorldFactory.create(Random(1)); val s = Sim(w)
        assertTrue(s.designer.store(PlaceId.HOME, boxes(w).single()))
        w.flags.remove(TreasureStart.FLAG)
        val loaded = reload(w)
        assertTrue(boxes(loaded).isEmpty())
        assertEquals(1, loaded.storage.count { it.type == FixtureType.TREASURE_BOX })
    }

    @Test fun `a child who threw the box away after the gift does not get it back by itself`() {
        val w = WorldFactory.create(Random(1)); val s = Sim(w)
        assertTrue(s.designer.store(PlaceId.HOME, boxes(w).single()))
        assertTrue(s.designer.discard(w.storage.lastIndex))
        val loaded = reload(w)
        assertTrue(boxes(loaded).isEmpty())
        assertTrue(loaded.storage.none { it.type == FixtureType.TREASURE_BOX })
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.TreasureStartTest" -Plain`
Expected: compilation fails with `Unresolved reference: TreasureStart`.

- [ ] **Step 3: Write the implementation**

Append to `domain/Treasure.kt`:

```kotlin
/** The one-time gift: a treasure box in the bedroom of Familiehuset, for new worlds and for old saves alike. */
object TreasureStart {
    const val FLAG = "layout:treasure-box:1"

    /**
     * Adds the box unless the world has had its gift, or already holds a box (standing somewhere or in the
     * store). A full bedroom leaves the flag unset, so the next load tries again; the box is in the catalogue too.
     */
    fun upgrade(world: World, sim: Sim = Sim(world).apply { tasks.recording = false }) {
        if (FLAG in world.flags) return
        val has = world.fixtures.values.any { it.type == FixtureType.TREASURE_BOX } ||
            world.storage.any { it.type == FixtureType.TREASURE_BOX }
        if (!has) {
            val bedroom = Decor.rooms(PlaceId.HOME).first()
            val x = bedroom.start + (bedroom.endInclusive - bedroom.start) * 0.62f
            sim.designer.add(PlaceId.HOME, FixtureType.TREASURE_BOX, 0, x, PlaceId.FRONT - 0.01f) ?: return
        }
        world.flags += FLAG
    }
}
```

`domain/WorldFactory.kt`, in `create`, after the line `world.flags += StarterLayout.FLAG` add

```kotlin
        TreasureStart.upgrade(world, sim)
```

`data/WorldStore.kt`, after the line `app.trollfoss.domain.StarterLayout.upgrade(world)` add

```kotlin
            app.trollfoss.domain.TreasureStart.upgrade(world)
```

- [ ] **Step 4: Run the test to verify it passes**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 5: Run all JVM tests**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest" -Plain`
Expected: `BUILD SUCCESSFUL`. A test that counts the furniture of Familiehuset in a new world, or the free furniture slots there, now sees one more piece: raise its expected number by one and say so in the commit message. Do not weaken any other assertion.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/app/trollfoss/domain/Treasure.kt app/src/main/java/app/trollfoss/domain/WorldFactory.kt app/src/main/java/app/trollfoss/data/WorldStore.kt app/src/test/java/app/trollfoss/domain/TreasureStartTest.kt
git commit -m "Give every world one treasure box in Familiehuset, once

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

PLAN-PART-2
