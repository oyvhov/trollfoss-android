package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class StarterLayoutTest {
    private fun oldWorld() = World().also { w ->
        WorldFactory.addFixtures(w)
        w.fixtures.entries.removeAll { it.value.place != PlaceId.HOME || it.value.type != FixtureType.CHEST }
    }
    private fun block(w: World, x: Float = 0.75f) = w.addThing(ThingType.UP_BLOCK, 2,
        PlaceId.HOME, x, PlaceId.HOME.floor).also { WorldFactory.remember(w, it) }

    @Test fun upgradingCollectsTheSameObjectInAnExistingBoxAndItCanBeTakenOutAgain() {
        val w = oldWorld(); val t = block(w); val count = w.bodies.size
        StarterLayout.upgrade(w)
        assertEquals(count, w.bodies.size); assertSame(t, w.bodies[t.id])
        assertEquals(2, t.variant); assertTrue(t.inside >= 0)
        assertEquals(FixtureType.CHEST, w.fixtures[t.inside]!!.type)
        assertEquals(t.inside, t.homeOwner); assertTrue(t.homeInside)
        val s = Sim(w); s.settle(PlaceId.HOME)
        assertEquals(t.homeOwner, t.inside)
        val box = w.fixtures[t.inside]!!; s.tap(PlaceId.HOME, box, 0f, 0f)
        assertTrue(box.open)
        assertTrue(s.surfaces(PlaceId.HOME).any { it.owner == box.id && it.interior })
        assertSame(t, w.bodies[t.id])
    }

    @Test fun migrationPreservesTheChildsMovedHeldPackedAndCarriedItemsAndOwnRooms() {
        val w = oldWorld()
        val moved = block(w).apply { x = 0.5f }
        val held = block(w).apply { held = true }
        val packed = block(w).apply { mode = Mode.BAG; place = null }
        val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.9f, 0.9f)
        val worn = block(w).apply { mode = Mode.WORN; holder = p.id }
        val created = w.addThing(ThingType.UP_BLOCK, 1, PlaceId.HOME, 0.75f, 0.9f)
        val own = w.addThing(ThingType.UP_BLOCK, 0, PlaceId.MINE_GROUND, 0.75f, 0.9f).also { WorldFactory.remember(w, it) }
        StarterLayout.upgrade(w)
        assertEquals(0.5f, moved.x, 0f); assertTrue(held.held); assertNull(packed.place)
        assertEquals(p.id, worn.holder); assertEquals(Mode.WORN, worn.mode)
        assertEquals(0.75f, created.x, 0f); assertEquals(-1, own.homeOwner)
        assertTrue(listOf(moved, held, packed, worn, created, own).all { it.inside == -1 })
    }

    @Test fun thereIsNoPileOrRemovalWhenASmallBoxIsFull() {
        val w = oldWorld(); val things = List(10) { block(w) }; val count = w.bodies.size
        StarterLayout.upgrade(w)
        val stored = things.filter { it.inside >= 0 }
        assertTrue(stored.isNotEmpty()); assertTrue(stored.size < things.size)
        stored.zipWithNext().forEach { (a,b) -> assertTrue(kotlin.math.abs(a.x-b.x) >= (a.w+b.w)/2f) }
        assertEquals(count, w.bodies.size)
        things.filter { it.inside < 0 }.forEach { assertEquals(0.75f, it.x, 0f) }
    }

    @Test fun anUpdateRunsOnceAndTheTidyingButtonReturnsThingsToTheirNewHome() {
        val w = oldWorld(); val t = block(w)
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val moved = loaded.bodies[t.id] as Thing
        assertTrue(StarterLayout.FLAG in loaded.flags); assertTrue(moved.homeInside)
        val homeX = moved.x; val homeY = moved.y; val homeBox = moved.inside
        moved.x = 0.75f; moved.y = 0.9f; moved.inside = -1
        val restarted = WorldStore.decode(WorldStore.encode(loaded, Settings())).world
        val same = restarted.bodies[t.id] as Thing
        assertEquals(0.75f, same.x, 0f); assertEquals(-1, same.inside)
        val s = Sim(restarted); assertTrue(s.designer.tidy(PlaceId.HOME) > 0)
        repeat(180) { s.step(PlaceId.HOME, 0.016f) }
        assertEquals(homeX, same.x, 0.005f); assertEquals(homeY, same.y, 0.005f)
        assertEquals(homeBox, same.inside)
    }

    @Test fun freshRoomsKeepEveryStarterPropAndGiveSmallThingsFurnitureHomes() {
        val w = WorldFactory.create()
        assertTrue(StarterLayout.FLAG in w.flags)
        val expected = PlaceId.entries.sumOf { Places.spec(it).things.size + Places.spec(it).people.sumOf { p ->
            (if(p.hat != null) 1 else 0) + (if(p.glasses != null) 1 else 0) + (if(p.hand != null) 1 else 0) } }
        // The portrait update adds Berit's removable glasses after the blueprints, preserving old ids.
        val berit = w.people().single { it.name == "Berit" }
        val portraitGlasses = w.worn(berit, Slot.FACE)!!
        assertEquals(ThingType.ROUND_GLASSES, portraitGlasses.type)
        assertEquals(1, portraitGlasses.variant)
        assertEquals(expected + 1, w.bodies.values.filterIsInstance<Thing>().size)
        val blocks = w.bodiesIn(PlaceId.MANOR_UPPER).filterIsInstance<Thing>().filter { it.type == ThingType.UP_BLOCK }
        assertEquals(3, blocks.size); assertTrue(blocks.count { it.homeOwner >= 0 } >= 2)
        val laundry = w.bodiesIn(PlaceId.MANOR_CELLAR).filterIsInstance<Thing>().first { it.type == ThingType.CE_SOCK }
        assertEquals(FixtureType.CE_BASKET, w.fixtures[laundry.homeOwner]!!.type)
    }
}
