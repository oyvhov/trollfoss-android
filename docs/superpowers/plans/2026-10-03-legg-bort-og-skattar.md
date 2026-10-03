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
- Produces: `FixtureType.TREASURE_BOX`; `Sim.treasure: TreasureBox` with `holds(f: Fixture): List<Thing>`, `tap(place, f)`, `step(place, dt)`, `put(place, f, t): Boolean`, `landed(t: Thing, owner: Int)`, `arrange(f): List<Thing>`, `refuse(place, f)`; constants `TreasureBox.REACH = 0.25f`, `TreasureBox.LINGER = 1.2f`, `TreasureBox.PARTY = 5`.

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

### Task 5: The doorway on a staircase can be tapped

**Files:**
- Create: `app/src/main/java/app/trollfoss/ui/art/StairDoorways.kt`
- Modify: `app/src/main/java/app/trollfoss/ui/art/HouseGroundHallArt.kt` (`grStairs`, the `run { … }` block near line 46)
- Modify: `app/src/main/java/app/trollfoss/ui/art/HouseCellarTunnelArt.kt` (`ceStairs`, near line 40)
- Modify: `app/src/main/java/app/trollfoss/ui/art/HouseUpperPassageArt.kt` (`upStairsUp`, near line 104)
- Modify: `app/src/main/java/app/trollfoss/ui/play/Engine.kt` (`fixtureAt`, near line 1102)
- Test: `app/src/test/java/app/trollfoss/ui/art/StairDoorwaysTest.kt`, `app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt`

**Background:** the touch area of a `STAIRCASE` is its spec box, 0.84 wide and 0.58 high. Three staircases paint a doorway above that box: the arch behind the grand stairs in the hall, the lit door at the top of the cellar stairs, and the little door to the attic on the first floor. A child taps the doorway and nothing happens. The other staircases (the two stairwells in the floor, both stairs of Mitt hus) and both ends of the secret tunnel were checked: what they paint lies inside their boxes, so they need no change.

**Interfaces:**
- Consumes: `FX_DX`, `FX_DY` (`internal const` in `ui/art/FixtureArt.kt`, the oblique projection), `FixtureDoors.hit` (the pattern this follows).
- Produces: `internal data class Doorway(left, top, right, bottom)` with `contains(x, y, margin)`; `StairDoorways.hall`, `StairDoorways.cellar`, `StairDoorways.attic`, `StairDoorways.of(f: Fixture): Doorway?`, `StairDoorways.hit(f: Fixture, x: Float, y: Float, margin: Float): Boolean`; constants `HALL_Z`, `ATTIC_Z`, `ATTIC_LEFT`, `ATTIC_RIGHT`, `ATTIC_SILL`, `ATTIC_TOP`. Coordinates are scene units from the bottom centre of the fixture, y negative upwards.

- [ ] **Step 1: Write the failing JVM test**

`app/src/test/java/app/trollfoss/ui/art/StairDoorwaysTest.kt`:

```kotlin
package app.trollfoss.ui.art

import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The doorway painted on a staircase belongs to the staircase: a tap on it is a tap on the stairs. */
class StairDoorwaysTest {
    private fun stairs(place: PlaceId, variant: Int = 0) = Fixture(1, place, FixtureType.STAIRCASE, 0f, 0f, variant)

    @Test fun `the glowing arch behind the grand stairs lies above the box of the stairs and is part of them`() {
        val f = stairs(PlaceId.MANOR_GROUND)
        assertTrue("above the box", -0.75f < -f.spec.h)
        assertTrue(StairDoorways.hit(f, -0.17f, -0.75f, 0f))
        assertFalse(StairDoorways.hit(f, 0.30f, -0.75f, 0f))
    }

    @Test fun `the lit door at the top of the cellar stairs is part of them`() {
        val f = stairs(PlaceId.MANOR_CELLAR)
        assertTrue(StairDoorways.hit(f, -0.34f, -0.70f, 0f))
        assertFalse(StairDoorways.hit(f, 0.20f, -0.70f, 0f))
    }

    @Test fun `the little door to the attic is part of the stairs up, not of the stairwell down`() {
        assertTrue(StairDoorways.hit(stairs(PlaceId.MANOR_UPPER, variant = 1), 0.25f, -0.70f, 0f))
        assertNull(StairDoorways.of(stairs(PlaceId.MANOR_UPPER, variant = 0)))
    }

    @Test fun `stairs without a painted doorway and other furniture have none`() {
        assertNull(StairDoorways.of(stairs(PlaceId.MANOR_ATTIC)))
        assertNull(StairDoorways.of(stairs(PlaceId.MINE_GROUND, variant = 2)))
        assertNull(StairDoorways.of(Fixture(1, PlaceId.MANOR_GROUND, FixtureType.LIFT, 0f, 0f)))
    }

    @Test fun `the margin widens the doorway a little`() {
        val f = stairs(PlaceId.MANOR_CELLAR)
        assertFalse(StairDoorways.hit(f, -0.44f, -0.70f, 0f))
        assertTrue(StairDoorways.hit(f, -0.44f, -0.70f, 0.012f))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.ui.art.StairDoorwaysTest" -Plain`
Expected: compilation fails with `Unresolved reference: StairDoorways`.

- [ ] **Step 3: Write the shared geometry**

`app/src/main/java/app/trollfoss/ui/art/StairDoorways.kt`:

```kotlin
package app.trollfoss.ui.art

import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId

/** A doorway painted on a staircase, in scene units from the bottom centre of the fixture (y is negative upwards). */
internal data class Doorway(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float, margin: Float): Boolean =
        x in (left - margin)..(right + margin) && y in (top - margin)..(bottom + margin)
}

/**
 * The same numbers describe both the painted doorway of a staircase and its touch area, the way
 * [FixtureDoors] does for cupboard doors: a child taps the door it wants to go through, not the steps.
 */
internal object StairDoorways {
    /** Storstova: the arch in the wall behind the landing of the grand stairs, [HALL_Z] deep. */
    const val HALL_Z = 0.26f
    val hall = Doorway(-0.4f + FX_DX * HALL_Z, -0.85f + FX_DY * HALL_Z, -0.2f + FX_DX * HALL_Z, -0.52f + FX_DY * HALL_Z)

    /** The cellar: the lit door at the top of the wooden flight. */
    val cellar = Doorway(-0.43f, -0.80f, -0.26f, -0.50f)

    /** The first floor: the little door up to the attic, in the wall [ATTIC_Z] deep. */
    const val ATTIC_Z = 0.07f
    const val ATTIC_LEFT = 0.1f
    const val ATTIC_RIGHT = 0.33f
    const val ATTIC_SILL = -0.53f
    const val ATTIC_TOP = -0.80f
    val attic = Doorway(ATTIC_LEFT + FX_DX * ATTIC_Z, ATTIC_TOP - 0.02f + FX_DY * ATTIC_Z, ATTIC_RIGHT + FX_DX * ATTIC_Z, ATTIC_SILL + FX_DY * ATTIC_Z)

    fun of(f: Fixture): Doorway? = when {
        f.type != FixtureType.STAIRCASE -> null
        f.place == PlaceId.MANOR_GROUND -> hall
        f.place == PlaceId.MANOR_CELLAR -> cellar
        f.place == PlaceId.MANOR_UPPER && f.variant == 1 -> attic
        else -> null
    }

    fun hit(f: Fixture, x: Float, y: Float, margin: Float): Boolean = of(f)?.contains(x, y, margin) == true
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`, 5 tests passed.

- [ ] **Step 5: Let the art read the same numbers**

`ui/art/HouseGroundHallArt.kt`, in `grStairs`, replace the whole `run { … }` block under the comment `// The doorway up to the first floor behind the landing, warm with light from above.` with

```kotlin
    // The doorway up to the first floor behind the landing, warm with light from above. Its place is shared
    // with the touch area (see [StairDoorways]): a tap on the doorway is a tap on the stairs.
    run {
        val door = StairDoorways.hall
        val arch = archPath(door.left * u, door.right * u, door.bottom * u, (door.bottom - 0.16f) * u, door.top * u)
        drawPath(arch, Brush.verticalGradient(listOf(Color(0xFF3A2A4A), Color(0xFFFFD98A)), startY = door.top * u, endY = door.bottom * u))
        drawPath(arch, Ink.line, style = pen.stroke)
        drawPath(arch, GrC.ivory, style = Stroke(0.012f * u))
        drawPath(arch, Ink.line, style = pen.thin)
        grGlow(Offset((door.left + door.right) / 2f * u, (door.bottom - 0.08f) * u), 0.16f * u, pen, 0.18f)
    }
```

`ui/art/HouseCellarTunnelArt.kt`, in `ceStairs`, replace the line

```kotlin
    fxBox(u, -0.43f, -0.8f, -0.26f, -0.5f, 0.04f, Color(0xFFE9E1D3), pen, rad = 0.005f, z = d - 0.02f)
```

with

```kotlin
    // The frame of the door is also where a tap on it counts (see [StairDoorways]).
    val way = StairDoorways.cellar
    fxBox(u, way.left, way.top, way.right, way.bottom, 0.04f, Color(0xFFE9E1D3), pen, rad = 0.005f, z = d - 0.02f)
```

`ui/art/HouseUpperPassageArt.kt`, in `upStairsUp`, replace the four lines

```kotlin
    val wx = 0.1f
    val wr = 0.33f
    val sill = -0.03f - rise * n
    val doorTop = sill - 0.27f
```

with

```kotlin
    // Shared with the touch area (see [StairDoorways]).
    val wx = StairDoorways.ATTIC_LEFT
    val wr = StairDoorways.ATTIC_RIGHT
    val sill = StairDoorways.ATTIC_SILL
    val doorTop = StairDoorways.ATTIC_TOP
```

The pictures must not change: the new numbers equal the old ones (`-0.03 - 0.0625 * 8 = -0.53`, `-0.53 - 0.27 = -0.80`).

- [ ] **Step 6: Count the doorway as part of the staircase**

`ui/play/Engine.kt`: add the import `import app.trollfoss.ui.art.StairDoorways` next to the import of `FixtureDoors`. In `fixtureAt`, replace

```kotlin
            if (FixtureDoors.hit(f, dx, dy, pad) || f.type == FixtureType.TRACTOR && TractorCab.contains(dx, dy - f.bob, pad)) return f
```

with

```kotlin
            if (FixtureDoors.hit(f, dx, dy, pad) || StairDoorways.hit(f, dx, dy, pad) ||
                f.type == FixtureType.TRACTOR && TractorCab.contains(dx, dy - f.bob, pad)) return f
```

- [ ] **Step 7: Add the Android test**

In `app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt`, add inside the class:

```kotlin
    private var lastPassage: Passage? = null

    /** Like [host], but remembers the way between floors that was taken. */
    private val passageHost = object : EngineHost {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id: String) {}
        override fun discovered(key: String) {}
        override fun telescope() {}
        override fun radio(on: Boolean) {}
        override fun egg(id: String) {}
        override fun passage(passage: Passage, arrivalX: Float) { lastPassage = passage }
    }

    @Test fun aTapOnTheGlowingDoorwayBehindTheGrandStairsGoesUpstairs() {
        val w = WorldFactory.create(); val s = Sim(w); val place = PlaceId.MANOR_GROUND
        val stairs = w.fixtures.getValue(WorldFactory.fixtureId(place, GroundIx.STAIRS))
        val e = Engine(w, place, s, passageHost, false, 0f).apply { setSize(1920f, 1200f, 1.5f); focusOn(stairs.x) }
        // The middle of the arch, above the staircase's own box: where a child taps to go up.
        val at = Offset((stairs.x - 0.17f - e.cam) * e.u, 1200f - e.u + (stairs.y - 0.75f) * e.u)
        e.down(1, at, 1000); e.up(1, at, 1100)
        assertEquals("ground-stairs-up", lastPassage?.id); e.cancel()
    }
```

The Android tests run in Task 9, when the emulator is up. Here, only compile them:

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.ui.art.StairDoorwaysTest :app:assembleDebug :app:assembleDebugAndroidTest" -Suffix .leggbort -Plain`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/app/trollfoss/ui/art/StairDoorways.kt app/src/main/java/app/trollfoss/ui/art/HouseGroundHallArt.kt app/src/main/java/app/trollfoss/ui/art/HouseCellarTunnelArt.kt app/src/main/java/app/trollfoss/ui/art/HouseUpperPassageArt.kt app/src/main/java/app/trollfoss/ui/play/Engine.kt app/src/test/java/app/trollfoss/ui/art/StairDoorwaysTest.kt app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt
git commit -m "Let a tap on the doorway of a staircase take the stairs

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 6: The put-away corner

**Files:**
- Create: `app/src/main/java/app/trollfoss/ui/play/AwayCorner.kt`
- Modify: `app/src/main/java/app/trollfoss/ui/play/Engine.kt` (state near line 188, `update` near lines 341–369, `updatePreviews` near line 418, `upNow` near line 859, `cancel` near line 1115, `drop` near line 1273, `intoBag` near line 1327, `drawBag` near line 3310)
- Modify: `app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt` (the furnish button, near line 351)
- Test: `app/src/test/java/app/trollfoss/ui/play/AwayCornerTest.kt`, `app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt`

**Background:** the outer 8 % of the screen on each side already moves the camera while something is held, so the edge cannot mean «put away». The bag sits in the bottom right corner, drawn by the engine (`drawBag`), with a touch circle of `bagRadius * 1.4`. On a tablet at 1920 × 1200 and density 1.5 the bag has radius 57 px and its centre is at (1833, 1113). Furniture can today only be stored over the open side panel (`storeZone`).

**Interfaces:**
- Consumes: `Designer.store(place, f): Boolean` (false when the piece may not be stored; a treasure box with finds then opens by itself, Task 3), `intoBag(body)`, `letGo(f)`, `Icons.Bag`, `DesignIcons.Box` (both `DrawScope.() -> Unit`).
- Produces: `enum class AwayPicture { BAG, CRATE }`; `AwayCorner.DRAW = 1.6f`, `AwayCorner.HIT = 2.4f`, `AwayCorner.BAG_ONLY = 1.4f`, `AwayCorner.GROW = 0.18f`, `AwayCorner.reach(fromScene: Boolean): Float`, `AwayCorner.contains(fingerX, fingerY, centerX, centerY, bagRadius, fromScene): Boolean`, `AwayCorner.picture(bodies: Int, furniture: Int): AwayPicture?`, `AwayCorner.nearMiss(fingerX, fingerY, width, height): Boolean`; on `Engine`: `val away: AwayPicture?`, `val overAway: Boolean`, `val bagAt: Offset`.

- [ ] **Step 1: Write the failing JVM test**

`app/src/test/java/app/trollfoss/ui/play/AwayCornerTest.kt`:

```kotlin
package app.trollfoss.ui.play

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The corner by the bag where things, figures and furniture are put away. */
class AwayCornerTest {
    // Tablet numbers: a bag of radius 57 px with its centre at (1833, 1113).
    private val cx = 1833f
    private val cy = 1113f
    private val r = 57f

    @Test fun `the corner reaches further for what comes from the scene than for what comes out of the bag`() {
        assertEquals(AwayCorner.HIT, AwayCorner.reach(fromScene = true), 0f)
        assertEquals(AwayCorner.BAG_ONLY, AwayCorner.reach(fromScene = false), 0f)
        assertTrue(AwayCorner.BAG_ONLY < AwayCorner.DRAW && AwayCorner.DRAW < AwayCorner.HIT)
    }

    @Test fun `a finger up and to the left of the bag is in the corner only for what comes from the scene`() {
        // 106 px from the centre: outside the bag's own 80 px, inside the grown corner's 137 px.
        assertTrue(AwayCorner.contains(cx - 75f, cy - 75f, cx, cy, r, fromScene = true))
        assertFalse(AwayCorner.contains(cx - 75f, cy - 75f, cx, cy, r, fromScene = false))
        assertFalse(AwayCorner.contains(cx - 233f, cy, cx, cy, r, fromScene = true))
        assertTrue(AwayCorner.contains(cx, cy, cx, cy, r, fromScene = false))
    }

    @Test fun `things and figures get the bag, furniture alone gets the crate, nothing held gets nothing`() {
        assertEquals(AwayPicture.BAG, AwayCorner.picture(bodies = 1, furniture = 0))
        assertEquals(AwayPicture.BAG, AwayCorner.picture(bodies = 1, furniture = 1))
        assertEquals(AwayPicture.CRATE, AwayCorner.picture(bodies = 0, furniture = 2))
        assertNull(AwayCorner.picture(bodies = 0, furniture = 0))
    }

    @Test fun `a near miss is the lower right quarter of the screen`() {
        assertTrue(AwayCorner.nearMiss(1500f, 900f, 1920f, 1200f))
        assertFalse(AwayCorner.nearMiss(900f, 900f, 1920f, 1200f))
        assertFalse(AwayCorner.nearMiss(1500f, 500f, 1920f, 1200f))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.ui.play.AwayCornerTest" -Plain`
Expected: compilation fails with `Unresolved reference: AwayCorner`.

- [ ] **Step 3: Write `AwayCorner`**

`app/src/main/java/app/trollfoss/ui/play/AwayCorner.kt`:

```kotlin
package app.trollfoss.ui.play

import kotlin.math.hypot

/** What the corner by the bag offers while something is held: the bag for things and figures, a crate for furniture. */
enum class AwayPicture { BAG, CRATE }

/**
 * The put-away corner. The screen edges carry the camera along, so «away» lives where the bag already is:
 * while the child holds something from the scene the bag grows, and whatever is let go inside goes into the
 * bag (things, figures) or the store (furniture). No panel has to be open.
 */
object AwayCorner {
    /** How much bigger the bag is drawn while something is held. */
    const val DRAW = 1.6f

    /** The touch radius in bag radii, for what was lifted from the scene. */
    const val HIT = 2.4f

    /** The bag's own touch radius: what comes out of the bag tray keeps it, so a tap in the tray still takes a thing out. */
    const val BAG_ONLY = 1.4f

    /** Seconds the bag takes to grow and to shrink back. */
    const val GROW = 0.18f

    fun reach(fromScene: Boolean): Float = if (fromScene) HIT else BAG_ONLY

    fun contains(fingerX: Float, fingerY: Float, centerX: Float, centerY: Float, bagRadius: Float, fromScene: Boolean): Boolean =
        hypot(fingerX - centerX, fingerY - centerY) < bagRadius * reach(fromScene)

    /** Null while nothing from the scene is held; the crate only when everything held is furniture. */
    fun picture(bodies: Int, furniture: Int): AwayPicture? = when {
        bodies > 0 -> AwayPicture.BAG
        furniture > 0 -> AwayPicture.CRATE
        else -> null
    }

    /** Furniture let go outside the corner but in the lower right quarter of the screen: the corner wobbles once, as a hint. */
    fun nearMiss(fingerX: Float, fingerY: Float, width: Float, height: Float): Boolean =
        fingerX > width * 0.5f && fingerY > height * 0.5f
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 5: Add the Android tests**

In `app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt`, add inside the class (it already has `host`, `engine(world)` and `finger(e, p)`):

```kotlin
    // ------------------------------------------------------------------ the put-away corner

    /** Up and to the left of the bag: outside its own small circle, inside the grown corner. */
    private fun cornerOf(e: Engine) = Offset(e.bagAt.x - 75f, e.bagAt.y - 75f)

    private fun at(e: Engine, x: Float, y: Float) = Offset((x - e.cam) * e.u, 1200f - e.u + y * e.u)

    private fun drag(e: Engine, from: Offset, to: Offset) {
        e.down(1, from, 1000)
        e.move(1, Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f), 1050)
        repeat(3) { e.update(0.016f) }
        e.move(1, to, 1100)
        repeat(3) { e.update(0.016f) }
        e.up(1, to, 1200)
    }

    @Test fun aThingLetGoInTheGrownCornerGoesInTheBagWithThePanelShut() {
        val w = World()
        val t = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1.0f, 0.9f)
        val e = engine(w); repeat(30) { e.update(0.016f) }
        drag(e, at(e, t.x, t.y - t.h / 2f), cornerOf(e))
        assertEquals(Mode.BAG, t.mode); e.cancel()
    }

    @Test fun aFigureLetGoInTheGrownCornerGoesInTheBag() {
        val w = World()
        val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.0f, 0.9f, "A")
        val e = engine(w); repeat(30) { e.update(0.016f) }
        drag(e, finger(e, p), cornerOf(e))
        assertEquals(Mode.BAG, p.mode); e.cancel()
    }

    @Test fun furnitureHeldAndLetGoInTheCornerGoesToTheStoreWithThePanelShut() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val f = s.designer.add(place, FixtureType.STOOL, 0, 1.0f, 0.9f)!!
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        e.down(1, at(e, f.x, f.y - f.spec.h / 2f), 1000)
        repeat(40) { e.update(0.016f) }                // a long press lifts the stool
        assertEquals(AwayPicture.CRATE, e.away)
        e.move(1, cornerOf(e), 1700); repeat(3) { e.update(0.016f) }
        assertTrue(e.overAway)
        e.up(1, cornerOf(e), 1800)
        assertNull(w.fixtures[f.id])
        assertEquals(FixtureType.STOOL, w.storage.single().type)
        repeat(20) { e.update(0.016f) }
        assertNull("the corner sleeps again", e.away); e.cancel()
    }

    @Test fun aTreasureBoxWithFindsInItStaysInTheRoomWhenLetGoInTheCorner() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val f = s.designer.add(place, FixtureType.TREASURE_BOX, 0, 1.0f, 0.9f)!!
        val gem = w.addThing(ThingType.GEM, 0, place, f.x, f.y - 0.1f)
        assertTrue(s.dropInto(place, f, gem))
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        // Hold the box by its lid, above the glass, so the finger is on the furniture and not on the gem.
        e.down(1, at(e, f.x + 0.12f, f.y - f.spec.h + 0.004f), 1000)
        repeat(40) { e.update(0.016f) }
        e.move(1, cornerOf(e), 1700); repeat(3) { e.update(0.016f) }
        e.up(1, cornerOf(e), 1800)
        assertTrue(w.fixtures[f.id] === f)
        assertTrue(w.storage.isEmpty())
        assertEquals(listOf(gem), s.treasure.holds(f)); e.cancel()
    }

    @Test fun insideTheCornerTheCameraStaysPutAndAboveItTheEdgeStillCarriesTheCameraAlong() {
        val w = World()
        val t = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1.0f, 0.9f)
        val e = engine(w); repeat(30) { e.update(0.016f) }
        e.down(1, at(e, t.x, t.y - t.h / 2f), 1000)
        e.move(1, Offset(1900f, e.bagAt.y), 1100)      // at the right edge, but inside the corner
        val before = e.cam
        repeat(30) { e.update(0.016f) }
        assertEquals(before, e.cam, 0.0001f)
        e.move(1, Offset(1900f, 300f), 1200)           // at the right edge, well above the corner
        repeat(30) { e.update(0.016f) }
        assertTrue(e.cam > before + 0.2f); e.cancel()
    }
```

They do not compile yet (`bagAt`, `away`, `overAway` are missing). They run in Task 9.

- [ ] **Step 6: Give the engine the corner's state**

`ui/play/Engine.kt`. Add the import `import app.trollfoss.ui.components.DesignIcons` next to the import of `Icons`.

After the lines

```kotlin
    /** A piece of furniture is being dragged over the panel. */
    var overStore by mutableStateOf(false)
```

add

```kotlin

    /** The put-away corner is awake: something from the scene is held, and the bag has grown to take it. */
    var away by mutableStateOf<AwayPicture?>(null)
        private set

    /** What is held hovers over the put-away corner. */
    var overAway by mutableStateOf(false)
        private set
    private var awayGrow = 0f
    private var awayWobble = 0f
```

After the line `private val bagCenter get() = Offset(geometry.right - bagMargin - bagRadius, heightPx - bagMargin - bagRadius)` add

```kotlin

    /** The centre of the bag on screen. */
    val bagAt: Offset get() = bagCenter

    /** Lifted from the scene (not pulled out of the bag tray): the grown corner is for these. */
    private fun fromScene(g: Grab): Boolean = g.target is Target.Hold || g.target is Target.Furniture

    /** The finger of [g] is where a let-go means «put away». */
    private fun onBag(g: Grab): Boolean =
        AwayCorner.contains(g.finger.x, g.finger.y, bagCenter.x, bagCenter.y, bagRadius, fromScene(g))

    private fun updateAway(dt: Float) {
        val carrying = grabs.values.filter { it.moved && fromScene(it) }
        away = AwayCorner.picture(carrying.count { it.target is Target.Hold }, carrying.count { it.target is Target.Furniture })
        overAway = carrying.any(::onBag)
        awayGrow = if (away != null) min(1f, awayGrow + dt / AwayCorner.GROW) else max(0f, awayGrow - dt / AwayCorner.GROW)
        awayWobble = max(0f, awayWobble - dt * 2.5f)
    }
```

In `update`, after the line `moveFurniture(dt)` add

```kotlin
        updateAway(dt)
```

- [ ] **Step 7: Keep the camera still inside the corner**

In `update`, replace

```kotlin
        if (grabs.values.any { it.moved && (heldBody(it) != null || it.target is Target.Furniture) }) {
            val edge = grabs.values.filter { it.moved && (heldBody(it) != null || it.target is Target.Furniture) }.map { it.finger.x }
            // With the designer panel open, the right edge is where the panel starts; over the panel
            // itself the camera stays put, so furniture can be dropped into the store.
            val panel = if (designMode) storeZone else null
            val right = panel?.left?.minus(dp(36f)) ?: (widthPx * 0.92f)
            for (x in edge) {
                if (x < widthPx * 0.08f) cam -= 1.3f * dt
                if (x > right && (panel == null || x < panel.left)) cam += 1.3f * dt
            }
```

with

```kotlin
        val carrying = grabs.values.filter { it.moved && (heldBody(it) != null || it.target is Target.Furniture) }
        if (carrying.isNotEmpty()) {
            // With the designer panel open, the right edge is where the panel starts; over the panel
            // itself the camera stays put, so furniture can be dropped into the store.
            val panel = if (designMode) storeZone else null
            val right = panel?.left?.minus(dp(36f)) ?: (widthPx * 0.92f)
            for (g in carrying) {
                // In the put-away corner the camera stays put too, so what is held can be let go there.
                if (onBag(g)) continue
                val x = g.finger.x
                if (x < widthPx * 0.08f) cam -= 1.3f * dt
                if (x > right && (panel == null || x < panel.left)) cam += 1.3f * dt
            }
```

Everything from `clampCam()` down to the closing `} else if (grabs.values.none { … }) {` stays as it is.

- [ ] **Step 8: Route the let-go**

In `updatePreviews`, replace

```kotlin
            if (hypot(g.finger.x - bagCenter.x, g.finger.y - bagCenter.y) < bagRadius * 1.4f) continue
```

with

```kotlin
            if (onBag(g)) continue
```

In `drop`, replace

```kotlin
        val fingerOnBag = hypot(g.finger.x - bagCenter.x, g.finger.y - bagCenter.y) < bagRadius * 1.4f
```

with

```kotlin
        val fingerOnBag = onBag(g)
```

In `upNow`, replace the furniture branch

```kotlin
            g.target is Target.Furniture -> {
                val f = (g.target as Target.Furniture).fixture
                overStore = false
                if (storeZone?.contains(at) == true && sim.designer.store(place, f)) {
                    designVersion++
                    host.changed()
                } else {
                    letGo(f)
                }
            }
```

with

```kotlin
            g.target is Target.Furniture -> {
                val f = (g.target as Target.Furniture).fixture
                overStore = false
                // Over the open panel, or in the put-away corner with no panel at all: into the store.
                val inCorner = onBag(g)
                if ((storeZone?.contains(at) == true || inCorner) && sim.designer.store(place, f)) {
                    designVersion++
                    if (inCorner) particles.burst(PKind.SPARK, units(bagCenter.x) + cam, units(bagCenter.y - top), 8, 0.4f)
                    host.changed()
                } else {
                    letGo(f)
                    // It could not be stored, or it nearly got there: the corner wobbles once to show where «away» is.
                    if (inCorner || AwayCorner.nearMiss(at.x, at.y, widthPx, heightPx)) awayWobble = 1f
                }
            }
```

In `intoBag`, replace the line `if (body is Person) Players.pack(world, body) else {` with

```kotlin
        // A figure says goodbye on its way into the bag.
        if (body is Person) voice(body, Sfx.GIGGLE, 0.7f)
        if (body is Person) Players.pack(world, body) else {
```

In `cancel`, after the line `grabs.clear()` add

```kotlin
        away = null
        overAway = false
```

- [ ] **Step 9: Draw the grown bag and the crate**

In `drawBag`, replace the first two lines of the function body

```kotlin
        val c = bagCenter
        val r = bagRadius
```

with

```kotlin
        // While something from the scene is held the bag grows up and to the left, clear of the screen edge.
        val grow = if (motion) awayGrow else if (away != null) 1f else 0f
        val r = bagRadius * (1f + (AwayCorner.DRAW - 1f) * grow)
        val c = Offset(bagCenter.x - (r - bagRadius), bagCenter.y - (r - bagRadius))
```

Replace the block

```kotlin
        val wobble = if (bag.isNotEmpty() && motion) sin(time * 3f) * 3f else 0f
        drawCircle(T.SunDeep, r, Offset(c.x, c.y + dp(5f)))
        drawCircle(Brush.verticalGradient(if (bagOpen) listOf(T.Mint, T.MintDeep) else listOf(T.SunTop, T.Sun), c.y - r, c.y + r), r, c)
        drawCircle(Ink.line, r, c, style = Stroke(dp(2.2f)))
        rotate(wobble, c) {
            inset(c.x - r * 0.62f, c.y - r * 0.66f, size.width - (c.x + r * 0.62f), size.height - (c.y + r * 0.58f)) { Icons.Bag(this) }
        }
```

with

```kotlin
        val wobble = (if (bag.isNotEmpty() && motion) sin(time * 3f) * 3f else 0f) +
            (if (motion) sin(awayWobble * 20f) * 12f * awayWobble else 0f)
        // What is held is over the corner: the whole touch area lights up.
        if (overAway) drawCircle(T.SunTop.copy(alpha = 0.35f), bagRadius * AwayCorner.HIT, bagCenter)
        drawCircle(T.SunDeep, r, Offset(c.x, c.y + dp(5f)))
        drawCircle(Brush.verticalGradient(if (bagOpen || overAway) listOf(T.Mint, T.MintDeep) else listOf(T.SunTop, T.Sun), c.y - r, c.y + r), r, c)
        drawCircle(Ink.line, r, c, style = Stroke(dp(2.2f)))
        rotate(wobble, c) {
            inset(c.x - r * 0.62f, c.y - r * 0.66f, size.width - (c.x + r * 0.62f), size.height - (c.y + r * 0.58f)) {
                // Furniture goes to the store: the corner shows a crate instead of the bag.
                if (away == AwayPicture.CRATE) DesignIcons.Box(this) else Icons.Bag(this)
            }
        }
```

- [ ] **Step 10: Hide the furniture button while the corner is awake**

`ui/screens/PlayScreen.kt`: replace

```kotlin
        if (!engine.designMode) {
            RoundButton(app.trollfoss.ui.SM.furnish.str(), onClick = { menuOpen = false; engine.closeDriving(); engine.designMode = true },
```

with

```kotlin
        // The grown put-away corner takes the place of the furniture button while something is held.
        if (!engine.designMode && engine.away == null) {
            RoundButton(app.trollfoss.ui.SM.furnish.str(), onClick = { menuOpen = false; engine.closeDriving(); engine.designMode = true },
```

- [ ] **Step 11: Build**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.ui.play.AwayCornerTest :app:assembleDebug :app:assembleDebugAndroidTest" -Suffix .leggbort -Plain`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 12: Commit**

```bash
git add app/src/main/java/app/trollfoss/ui/play/AwayCorner.kt app/src/main/java/app/trollfoss/ui/play/Engine.kt app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt app/src/test/java/app/trollfoss/ui/play/AwayCornerTest.kt app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt
git commit -m "Put things, figures and furniture away in the corner by the bag

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 7: A missed tap makes the nearest answering furniture wobble and glow

**Files:**
- Create: `app/src/main/java/app/trollfoss/domain/TapHint.kt`
- Modify: `app/src/main/java/app/trollfoss/ui/play/Engine.kt` (`tapScene` near line 1567, a new `drawTapHint` next to `drawHints` near line 3051, its call near line 2540)
- Test: `app/src/test/java/app/trollfoss/domain/TapHintTest.kt`, `app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt`

**Interfaces:**
- Consumes: `House.passageAt(f): Passage?`, `House.usable(world, passage): Boolean`, `FixtureSpec.container`, `FixtureSpec.machine` (`Machine.NONE` when none), `Vehicles.controllable(f)`, `Fixture.top` (`y - spec.h`), `Fixture.shiftX`.
- Produces: `TapHint.REACH = 0.35f`, `TapHint.SECONDS = 0.6f`, `TapHint.PAUSE = 1.5f`, `TapHint.answers(world, f): Boolean`, `TapHint.distance(f, x, y): Float`, `TapHint.nearest(world, place, x, y): Fixture?`; on `Engine`: `val hintedFixture: Int` (the id of the furniture hinting now, or -1).

- [ ] **Step 1: Write the failing JVM test**

`app/src/test/java/app/trollfoss/domain/TapHintTest.kt`:

```kotlin
package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** A tap that hit nothing: the nearest furniture that would have answered shows itself. */
class TapHintTest {
    private val place = PlaceId.HOME

    @Test fun `distance is zero inside the box of a piece and grows outside it`() {
        val f = Fixture(1, place, FixtureType.CHEST, 1f, 0.9f)      // 0.18 wide, 0.10 high
        assertEquals(0f, TapHint.distance(f, 1f, 0.85f), 0.0001f)
        assertEquals(0.1f, TapHint.distance(f, 1.19f, 0.85f), 0.0001f)
        assertEquals(0.2f, TapHint.distance(f, 1f, 0.6f), 0.0001f)
    }

    @Test fun `the nearest furniture that answers a tap is found within reach, and nothing beyond it`() {
        val w = World(); val s = Sim(w)
        val chest = s.designer.add(place, FixtureType.CHEST, 0, 1.0f, 0.9f)!!
        assertSame(chest, TapHint.nearest(w, place, chest.x + 0.3f, chest.y - 0.3f))
        assertNull(TapHint.nearest(w, place, chest.x + 0.6f, chest.y - 0.3f))
    }

    @Test fun `furniture that does nothing when tapped never hints`() {
        val w = World(); val s = Sim(w)
        val rug = s.designer.add(place, FixtureType.RUG, 0, 1.0f, 0.9f)!!
        assertFalse(TapHint.answers(w, rug))
        assertNull(TapHint.nearest(w, place, rug.x, rug.y - 0.2f))
    }

    @Test fun `of two pieces the nearer one answers`() {
        val w = World(); val s = Sim(w)
        val near = s.designer.add(place, FixtureType.CHEST, 0, 1.0f, 0.9f)!!
        val far = s.designer.add(place, FixtureType.TOY_BOX, 0, 1.6f, 0.9f)!!
        assertSame(near, TapHint.nearest(w, place, near.x + 0.15f, near.y - 0.2f))
        assertSame(far, TapHint.nearest(w, place, far.x - 0.15f, far.y - 0.2f))
    }

    @Test fun `stairs, cupboards, machines and vehicles answer`() {
        val w = WorldFactory.create(Random(1))
        val stairs = w.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MANOR_GROUND, GroundIx.STAIRS))
        assertTrue(TapHint.answers(w, stairs))
        assertTrue(TapHint.answers(w, w.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.WARDROBE }))
        assertTrue(TapHint.answers(w, w.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.STOVE }))
        assertTrue(TapHint.answers(w, w.fixturesIn(PlaceId.FARM).first { it.type == FixtureType.TRACTOR }))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.TapHintTest" -Plain`
Expected: compilation fails with `Unresolved reference: TapHint`.

- [ ] **Step 3: Write `TapHint`**

`app/src/main/java/app/trollfoss/domain/TapHint.kt`:

```kotlin
package app.trollfoss.domain

import kotlin.math.hypot
import kotlin.math.max

/**
 * A hint in the moment: when a tap hits nothing, the nearest piece of furniture that would have answered
 * wobbles and glows for a blink. Nothing is shown before the child tries, and nothing stays afterwards.
 */
object TapHint {
    /** How far from a missed tap a piece may be and still answer, in scene units. */
    const val REACH = 0.35f

    /** Seconds the glow lasts. */
    const val SECONDS = 0.6f

    /** Seconds before the next hint may come. */
    const val PAUSE = 1.5f

    /** Furniture that does something when tapped: usable ways between floors, cupboards, machines and vehicles. */
    fun answers(world: World, f: Fixture): Boolean =
        House.passageAt(f)?.let { House.usable(world, it) } == true ||
            f.spec.container != null || f.spec.machine != Machine.NONE || Vehicles.controllable(f)

    /** How far the point lies from the box of [f]; 0 inside it. */
    fun distance(f: Fixture, x: Float, y: Float): Float {
        val fx = f.x + f.shiftX
        val dx = max(0f, max(fx - f.spec.w / 2f - x, x - (fx + f.spec.w / 2f)))
        val dy = max(0f, max(f.top - y, y - f.y))
        return hypot(dx, dy)
    }

    fun nearest(world: World, place: PlaceId, x: Float, y: Float): Fixture? =
        world.fixturesIn(place).filter { answers(world, it) }
            .map { it to distance(it, x, y) }
            .filter { it.second <= REACH }
            .minByOrNull { it.second }?.first
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`, 5 tests passed.

- [ ] **Step 5: Add the Android test**

In `SharedPlayTest.kt`, inside the class:

```kotlin
    @Test fun aTapThatHitsNothingMakesTheNearestChestAnswerForAMoment() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val chest = s.designer.add(place, FixtureType.CHEST, 0, 1.0f, 0.9f)!!
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        fun tap(x: Float, y: Float, time: Long) { val o = at(e, x, y); e.down(1, o, time); e.up(1, o, time + 80) }
        tap(chest.x + 0.3f, chest.y - 0.3f, 1000)
        assertEquals(chest.id, e.hintedFixture)
        repeat(50) { e.update(0.016f) }                 // 0.8 s later the hint is over
        assertEquals(-1, e.hintedFixture)
        repeat(60) { e.update(0.016f) }
        tap(chest.x + 0.9f, chest.y - 0.3f, 4000)       // nothing within reach
        assertEquals(-1, e.hintedFixture); e.cancel()
    }
```

- [ ] **Step 6: Wire the hint into the engine**

`ui/play/Engine.kt`. Add the import `import app.trollfoss.domain.TapHint` with the other domain imports.

After the lines

```kotlin
    private var hint: Wish? = null
    private var hintUntil = 0f
```

add

```kotlin

    /** The furniture that answers a tap which hit nothing (see [TapHint]). */
    private var tapHintId = -1
    private var tapHintUntil = 0f
    private var tapHintNext = 0f

    /** The id of the furniture hinting right now, or -1. */
    val hintedFixture: Int get() = if (time < tapHintUntil) tapHintId else -1
```

In `tapScene`, replace the last line

```kotlin
        particles.burst(PKind.SPARK, p.x, p.y, 4, 0.25f, 0.008f, Color.White)
```

with

```kotlin
        particles.burst(PKind.SPARK, p.x, p.y, 4, 0.25f, 0.008f, Color.White)
        // Nothing was there. If something close by would have answered, it shows itself for a blink.
        if (time >= tapHintNext) TapHint.nearest(world, place, p.x, p.y)?.let { f ->
            f.anim = 1f
            tapHintId = f.id
            tapHintUntil = time + TapHint.SECONDS
            tapHintNext = time + TapHint.PAUSE
            host.sfx(Sfx.CHIME, 0.35f, 1.3f)
        }
```

Add this function right before `private fun DrawScope.drawHints(lw: Float) {`:

```kotlin
    /** The glow round the furniture that answers a missed tap. */
    private fun DrawScope.drawTapHint(lw: Float) {
        if (time >= tapHintUntil) return
        val f = world.fixtures[tapHintId]?.takeIf { it.place == place } ?: return
        val fade = min(1f, (tapHintUntil - time) / 0.25f)
        val pulse = if (motion) 1f + sin(time * 14f) * 0.06f else 1f
        val c = Offset(sx(f.x + f.shiftX), sy(f.y - f.spec.h / 2f))
        val r = max(f.spec.w, f.spec.h) * 0.6f * u * pulse
        glow(listOf(T.SunTop.copy(alpha = 0.5f * fade), Color.Transparent), c, r * 1.4f)
        drawCircle(Color.White.copy(alpha = 0.85f * fade), r, c, style = Stroke(lw * 2f))
    }
```

At the call site (near line 2540), replace

```kotlin
                drawHints(lw)
```

with

```kotlin
                drawHints(lw)
                drawTapHint(lw)
```

- [ ] **Step 7: Build**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.domain.TapHintTest :app:assembleDebug :app:assembleDebugAndroidTest" -Suffix .leggbort -Plain`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/app/trollfoss/domain/TapHint.kt app/src/main/java/app/trollfoss/ui/play/Engine.kt app/src/test/java/app/trollfoss/domain/TapHintTest.kt app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt
git commit -m "Hint at the nearest answering furniture when a tap hits nothing

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 8: The furniture panel steps aside by itself

**Files:**
- Create: `app/src/main/java/app/trollfoss/ui/play/PanelIdle.kt`
- Modify: `app/src/main/java/app/trollfoss/ui/play/Engine.kt` (`designMode` near line 182, `upNow` near line 857)
- Modify: `app/src/main/java/app/trollfoss/ui/screens/DesignerPanel.kt` (the modifier of the root `Column`, near line 121)
- Modify: `app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt` (the `onClose` of `DesignerPanel`, near line 401)
- Test: `app/src/test/java/app/trollfoss/ui/play/PanelIdleTest.kt`, `app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt`

**Background:** since 1.3.0 things and figures can be moved while the furniture panel is open, at the user's request. That stays. The panel now closes when the child has clearly gone back to playing: two things or figures lifted and let go in a row, with no furniture and no touch on the panel in between.

**Interfaces:**
- Consumes: `drag(e, from, to)`, `at(e, x, y)`, `finger(e, p)` in `SharedPlayTest` (Task 6 added the first two).
- Produces: `class PanelIdle(limit: Int = 2)` with `decorating()` and `played(): Boolean`; on `Engine`: `fun panelTouched()`, `fun closeDesigner()`; `Engine.designMode` keeps its name and type (`Boolean`, readable and writable, observed by Compose).

- [ ] **Step 1: Write the failing JVM test**

`app/src/test/java/app/trollfoss/ui/play/PanelIdleTest.kt`:

```kotlin
package app.trollfoss.ui.play

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The furniture panel steps aside when the child has gone back to playing. */
class PanelIdleTest {
    @Test fun `two things or figures in a row close the panel, one does not`() {
        val idle = PanelIdle()
        assertFalse(idle.played())
        assertTrue(idle.played())
    }

    @Test fun `furniture or a touch on the panel in between starts the count again`() {
        val idle = PanelIdle()
        assertFalse(idle.played())
        idle.decorating()
        assertFalse(idle.played())
        assertTrue(idle.played())
    }

    @Test fun `after the panel has closed the count starts from nothing`() {
        val idle = PanelIdle()
        idle.played(); idle.played()
        idle.decorating()
        assertFalse(idle.played())
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.ui.play.PanelIdleTest" -Plain`
Expected: compilation fails with `Unresolved reference: PanelIdle`.

- [ ] **Step 3: Write `PanelIdle`**

`app/src/main/java/app/trollfoss/ui/play/PanelIdle.kt`:

```kotlin
package app.trollfoss.ui.play

/**
 * Counts play in the scene while the furniture panel is open. Moving one figure in the middle of decorating
 * is still decorating; [limit] things or figures in a row, with no furniture and no touch on the panel in
 * between, means the child has gone back to playing and the panel is in the way.
 */
class PanelIdle(private val limit: Int = 2) {
    private var lifts = 0

    /** The child touched the panel or moved furniture: still decorating. */
    fun decorating() {
        lifts = 0
    }

    /** A thing or a figure was moved. True when the panel should step aside. */
    fun played(): Boolean {
        lifts++
        return lifts >= limit
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run the command from Step 2. Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 5: Add the Android tests**

In `SharedPlayTest.kt`, inside the class:

```kotlin
    private fun nudge(e: Engine, from: Offset) = drag(e, from, Offset(from.x + 120f, from.y - 60f))

    @Test fun theFurniturePanelStepsAsideAfterTwoThingsOrFiguresInARowButNotAfterOne() {
        val w = World()
        val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        val t = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1.5f, 0.9f)
        val e = engine(w); repeat(30) { e.update(0.016f) }
        e.designMode = true
        nudge(e, finger(e, p))
        assertTrue("one moved figure is still decorating", e.designMode)
        repeat(30) { e.update(0.016f) }
        nudge(e, at(e, t.x, t.y - t.h / 2f))
        assertFalse(e.designMode); e.cancel()
    }

    @Test fun movingFurnitureInBetweenKeepsTheFurniturePanelOpen() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val p = w.addPerson(Species.FOLK, Look(), 1f, place, 0.7f, 0.9f, "A")
        val stool = s.designer.add(place, FixtureType.STOOL, 0, 1.6f, 0.9f)!!
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        repeat(30) { e.update(0.016f) }
        e.designMode = true
        nudge(e, finger(e, p))
        repeat(30) { e.update(0.016f) }
        nudge(e, at(e, stool.x, stool.y - stool.spec.h / 2f))      // with the panel open a plain drag moves furniture
        repeat(30) { e.update(0.016f) }
        nudge(e, finger(e, p))
        assertTrue(e.designMode); e.cancel()
    }
```

- [ ] **Step 6: Wire the counter into the engine**

`ui/play/Engine.kt`: replace

```kotlin
    /** The home designer is open: furniture moves with a plain drag, and the panel below takes it away. */
    var designMode by mutableStateOf(false)
```

with

```kotlin
    private val panelIdle = PanelIdle()
    private var designModeState by mutableStateOf(false)

    /** The home designer is open: furniture moves with a plain drag, and the panel below takes it away. */
    var designMode: Boolean
        get() = designModeState
        set(value) {
            if (value != designModeState) panelIdle.decorating()
            designModeState = value
        }

    /** The child touched the furniture panel: still decorating. */
    fun panelTouched() = panelIdle.decorating()

    /** Closes the furniture panel, the way its red X does. */
    fun closeDesigner() {
        designMode = false
        storeZone = null
        host.changed()
    }
```

In `upNow`, replace

```kotlin
            body != null && body.held -> drop(g, body, vx, vy)
```

with

```kotlin
            body != null && body.held -> {
                drop(g, body, vx, vy)
                // Playing, not decorating: after two things or figures in a row the furniture panel steps aside.
                if (designMode && g.target is Target.Hold && panelIdle.played()) closeDesigner()
            }
```

In the furniture branch of `upNow` (changed in Task 6), add as its first line after `val f = (g.target as Target.Furniture).fixture`:

```kotlin
                panelIdle.decorating()
```

- [ ] **Step 7: Tell the engine about touches on the panel**

`ui/screens/DesignerPanel.kt`: in the modifier chain of the root `Column`, after the line `.onGloballyPositioned { engine.storeZone = it.boundsInRoot() }` add

```kotlin
            // Any finger on the panel means the child is still decorating (see Engine.panelTouched).
            .pointerInput(engine) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        engine.panelTouched()
                    }
                }
            }
```

`ui/screens/PlayScreen.kt`: replace

```kotlin
            DesignerPanel(engine, vm.world, place, onClose = {
                engine.designMode = false
                engine.storeZone = null
                vm.scheduleSave()
            })
```

with

```kotlin
            DesignerPanel(engine, vm.world, place, onClose = {
                engine.closeDesigner()
                vm.scheduleSave()
            })
```

- [ ] **Step 8: Build**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest --tests app.trollfoss.ui.play.PanelIdleTest :app:assembleDebug :app:assembleDebugAndroidTest" -Suffix .leggbort -Plain`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/app/trollfoss/ui/play/PanelIdle.kt app/src/main/java/app/trollfoss/ui/play/Engine.kt app/src/main/java/app/trollfoss/ui/screens/DesignerPanel.kt app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt app/src/test/java/app/trollfoss/ui/play/PanelIdleTest.kt app/src/androidTest/java/app/trollfoss/ui/play/SharedPlayTest.kt
git commit -m "Let the furniture panel step aside when the child goes back to playing

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 9: Whole-build check, a look on tablet and phone, and the documents

**Files:**
- Modify: `CHANGELOG.md`, `docs/DESIGN.md`, `docs/OVERLEVERING.md`
- Screenshots go to the git-ignored folder `screenshots/legg-bort/`.

**Interfaces:**
- Consumes: everything from Tasks 1–8.
- Produces: a green build, green Android tests, screenshots, and documents that say what is done and what is not.

- [ ] **Step 1: Run every JVM test, lint and both builds**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest" -Suffix .leggbort -Plain`
Expected: `BUILD SUCCESSFUL`, 0 lint errors. Note the number of unit tests and lint warnings for the handover text (1.7.1 had 467 unit tests and 31 lint warnings in debug).

- [ ] **Step 2: Start the tablet emulator and run the Android tests**

Make sure no other emulator runs (`adb devices`); stop the phone emulator first if it does.

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-TrollfossTablet.ps1`
Then: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Build-Locked.ps1 -Tasks ":app:connectedDebugAndroidTest" -Suffix .leggbort -Plain`
Expected: `BUILD SUCCESSFUL`; 1.7.1 had 32 Android tests, this plan adds 9 (one in Task 5, five in Task 6, one in Task 7, two in Task 8). A failing test is a finding: read it, fix the code or the test's geometry, and run again. Do not delete a test to get green.

- [ ] **Step 3: Look at it on the tablet (1920 × 1200 / 240 dpi)**

The package is `app.trollfoss.leggbort`; private worlds in `app.trollfoss` and `app.trollfoss.debug` are never touched.

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n app.trollfoss.leggbort/app.trollfoss.MainActivity --es place home
```

Go through this list with real drags and taps, and save a screenshot of each with `adb exec-out screencap -p > screenshots\legg-bort\NN-name.png`:

1. Familiehuset, bedroom: the treasure box stands on the floor, is not half inside the bed or the wardrobe, and its glass shows through. If it stands badly, change the `0.62f` and `PlaceId.FRONT - 0.01f` in `TreasureStart.upgrade` and run `TreasureStartTest` again.
2. Lift a ball: the bag grows and the furniture button disappears. Let go: both return.
3. Let the ball go in the grown corner with the panel shut: it is in the bag.
4. Lift a figure and let it go in the corner: it is in the bag and can be dragged out again.
5. Long-press a chair, carry it to the corner: the corner shows a crate and lights up; let go and it is in the store. Open the panel: the store shows it.
6. Store three equal flower pots: one card with a «3». Take one out: «2». Delete one: the recycling box shows it.
7. Carry a figure along the floor to the right edge above the corner: the camera follows. Inside the corner it stands still.
8. Hold a gem near the treasure box: the lid lifts. Let go over it: the gem lies on a shelf, visible through the glass after the lid falls. Five gems: the box wobbles and burps.
9. Try to carry the full box to the corner: it stays, opens and wobbles.
10. Storhuset (reached by the map): tap the glowing doorway behind the grand stairs: the view goes upstairs. Tap the little door to the attic on the first floor, and the lit door of the cellar stairs from the cellar: both work.
11. The attic: open the chest until it coughs dust and a moth flies out; no gem or coin goes in a puff.
12. Tap the wall beside a cupboard: the cupboard wobbles and glows for a blink, and not again within a second and a half.
13. Open the furniture panel, move one figure (the panel stays), then a ball (the panel closes). The furniture button opens it again.
14. Switch to bokmål on the parents' page: the catalogue label reads «Skattekiste».
15. Close the app completely and start it again: the box, its gems and the store are as they were.

- [ ] **Step 4: A short look on the phone (2400 × 1080 / 420 dpi)**

Stop the tablet emulator, then: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-TrollfossEmulator.ps1`, install and start as in Step 3. Check items 2, 3, 5, 8 and 10 of the list. The grown bag must not cover the «more» button or the room buttons.

- [ ] **Step 5: Put the emulators back**

Uninstall the test package and stop the emulator that was started:

```powershell
adb uninstall app.trollfoss.leggbort
adb uninstall app.trollfoss.leggbort.test
adb emu kill
```

If a screen size or density override was set by a script, reset it first with `adb shell wm size reset` and `adb shell wm density reset`.

- [ ] **Step 6: Write the documents**

`CHANGELOG.md`: add under the first line `# Endringslogg`, before `## 1.7.1`:

```markdown

## Neste – Legg bort og skattar

- Hjørnet ved sekken veks når barnet løftar noko. Slepp der legg ting og figurar i sekken og møblar på lager, utan at møbelpanelet må vere ope. Kantane ber framleis kameraet vidare til neste rom.
- Ny skattekiste med glasfront, gratis i Møblar på alle stader, og éi står klar i soverommet i Familiehuset. Lokket spretter opp når barnet kjem nær med ein ting, og kvar femte skatt får kista til å rape glitter.
- Diamantar, myntar og perler forsvinn ikkje lenger av seg sjølv. Loftskista gir berre det det er plass til, og hostar støv når ho er tom.
- Døropninga øvst i trappene i Storhuset kan trykkjast på, slik trappa kan.
- Eit trykk som ikkje treffer noko, får det næraste skapet, maskina, køyretøyet eller trappa til å vippe og lyse eit augeblikk.
- Møbelpanelet lukkar seg sjølv når barnet har flytta to ting eller figurar på rad.
- Like møblar i Lager og Papirkorg blir viste som eitt kort med eit tal.
```

`docs/DESIGN.md`: in §4 «Samhandling – verbet er «dra»», add this paragraph after the paragraph that starts with `**Heimedesignaren**`:

```markdown
**Legg bort-hjørnet:** når barnet løftar ein ting, ein figur eller eit møbel, veks sekken nede til høgre.
Slepp der legg ting og figurar i sekken og møblar på lager, utan at møbelpanelet er ope. Skjermkantane
ber framleis kameraet vidare. **Skattekista** har glasfront og tek imot alt barnet samlar; diamantar,
myntar og perler forsvinn aldri av seg sjølv. Hint kjem berre i augeblinken barnet prøver noko: hjørnet
vaknar ved løft, og eit trykk som ikkje treffer noko, får det næraste som svarar på trykk til å vippe og lyse.
```

`docs/OVERLEVERING.md`: add a new block at the very top, under the heading line, in the same style as the blocks below it. Fill in the real numbers and findings from Steps 1–4; do not copy numbers from this plan:

```markdown
> **NYAST – LEGG BORT OG SKATTAR (<dato>, Claude, lokalt og ikkje utgjeve):** Første runde av «Destiller og test»,
> bygd på det brukaren har sett ein seksåring gjere på nettbrett. Grein `claude/legg-bort` i
> `C:\topa\.claude\worktrees\legg-bort`, frå publisert 1.7.1. Spec og plan: `docs/superpowers/`.
> Legg bort-hjørne ved sekken, skattekiste med glasfront, skattar som aldri forsvinn, trykkbare døropningar
> i trappene i Storhuset, hint ved bomtrykk, møbelpanel som lukkar seg sjølv og like møblar som eitt kort med tal.
> **Kontroll:** <tal> einingstestar og <tal> Android-testar grøne; lint <tal> feil / <tal> åtvaringar.
> Sett på nettbrett 1920 × 1200 / 240 dpi og kort på mobil 2400 × 1080 / 420 dpi, i eiga pakke
> `app.trollfoss.leggbort`. Bilete: Git-ignorert `screenshots/legg-bort/`. <kva som vart funne og retta>.
> **Ikkje gjort:** ingen release, ingen versjonsendring, ingen barnetest av denne runden. Runde to
> (knappar, menyar, symbol) ventar til brukaren har sett barnet bruke dette.
> `C:\topa` står framleis på `codex/magic-rest` med gammalt, ukommittert arbeid som er urørt.
```

- [ ] **Step 7: Commit**

```bash
git add CHANGELOG.md docs/DESIGN.md docs/OVERLEVERING.md
git commit -m "Document the put-away corner, the treasure box and the hints [skip ci]

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

- [ ] **Step 8: Hand over**

Tell the user what was built, what was seen on tablet and phone, and what could not be checked. Do not push, open a pull request or make a release unless the user asks.
