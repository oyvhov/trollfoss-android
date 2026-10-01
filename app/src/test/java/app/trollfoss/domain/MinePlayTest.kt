package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MinePlayTest {
    private val place = PlaceId.MINE_GROUND
    private fun setup(): Pair<World, Sim> {
        val world = WorldFactory.create(Random(9))
        val sim = Sim(world, random = Random(9))
        sim.mine.layFoundation(1); sim.mine.finishJob()
        for ((i, kind) in listOf(RoomKind.GREENHOUSE, RoomKind.KITCHEN, RoomKind.WORKSHOP).withIndex()) {
            assertTrue(sim.mine.buildRoom(place, i + 1, kind)); sim.mine.finishJob()
        }
        return world to sim
    }
    private fun fixture(w: World, type: FixtureType) = w.fixturesIn(place).first { it.type == type }
    private fun thing(w: World, type: ThingType) = w.bodiesIn(place).filterIsInstance<Thing>().first { it.type == type }
    private fun tick(s: Sim, seconds: Float = 2f) { repeat((seconds * 30).toInt()) { s.step(place, 1f / 30f) } }

    @Test fun `greenhouse berries and counter dough can bake into food for a person`() {
        val (w, s) = setup()
        val bed = fixture(w, FixtureType.MI_PLANT_BED)
        val can = thing(w, ThingType.WATERING_CAN)
        val seeds = thing(w, ThingType.SEEDS)
        assertTrue(s.dropInto(place, bed, seeds))
        assertFalse(s.dropInto(place, bed, can)); assertFalse(s.dropInto(place, bed, can))
        assertEquals(3, bed.count)
        assertTrue(w.bodies.containsKey(can.id)); assertFalse(w.bodies.containsKey(seeds.id))
        s.tap(place, bed, 0f, 0f)
        val berry = thing(w, ThingType.STRAWBERRY)
        val counter = fixture(w, FixtureType.MI_COUNTER)
        assertTrue(s.dropInto(place, counter, thing(w, ThingType.EGG)))
        assertTrue(s.dropInto(place, counter, thing(w, ThingType.MILK)))
        tick(s)
        val dough = thing(w, ThingType.DOUGH)
        val oven = fixture(w, FixtureType.OVEN)
        // The same portable objects are placed inside the oven in the next room.
        for (t in listOf(berry, dough)) { t.inside = oven.id; t.x = oven.x; t.y = oven.y - 0.08f; t.resting = true }
        s.tap(place, oven, 0f, -0.25f)
        tick(s, 3f)
        val cake = thing(w, ThingType.CUPCAKE)
        assertEquals(2, cake.variant)
        val person = w.people().first { it.species == Species.FOLK }
        House.moveTo(w, person, place, 5f)
        assertEquals(Give.ATE, s.give(person, cake, Part.MOUTH))
        assertTrue("mine_dough" in w.discoveries)
        assertTrue("oven_with_STRAWBERRY" in w.discoveries)
    }

    @Test fun `saw makes two portable parts which the workbench turns into a guitar`() {
        val (w, s) = setup()
        val saw = fixture(w, FixtureType.MI_SAW_BENCH)
        s.tap(place, saw, 0f, 0f)
        assertTrue(s.dropInto(place, saw, thing(w, ThingType.PLANK)))
        tick(s)
        val sticks = w.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == ThingType.STICK }
        assertEquals(2, sticks.size)
        val bench = fixture(w, FixtureType.WORKBENCH)
        sticks.forEach { assertTrue(s.dropInto(place, bench, it)) }
        assertFalse(s.dropInto(place, bench, thing(w, ThingType.HAMMER)))
        assertNotNull(thing(w, ThingType.GUITAR))
        assertTrue("mine_saw" in w.discoveries)
    }

    @Test fun `unfinished mixture survives saving and rejects duplicates and toys`() {
        val (w, s) = setup()
        val counter = fixture(w, FixtureType.MI_COUNTER)
        assertTrue(s.dropInto(place, counter, thing(w, ThingType.EGG)))
        val duplicate = w.addThing(ThingType.EGG, 0, place, counter.x, counter.y)
        val toy = w.addThing(ThingType.TEDDY, 0, place, counter.x, counter.y)
        assertFalse(s.dropInto(place, counter, duplicate)); assertFalse(s.dropInto(place, counter, toy))
        val restored = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val next = Sim(restored)
        val nextCounter = restored.fixtures.getValue(counter.id)
        assertEquals(1, restored.inMachine(nextCounter).size)
        assertTrue(next.dropInto(place, nextCounter, thing(restored, ThingType.MILK)))
        tick(next)
        assertNotNull(thing(restored, ThingType.DOUGH))
        assertTrue(restored.bodies.containsKey(duplicate.id)); assertTrue(restored.bodies.containsKey(toy.id))
        assertTrue(restored.inMachine(nextCounter).isEmpty())
    }

    @Test fun `old empty rooms can restock tools without flooding the floor`() {
        val (w, s) = setup()
        val bed = fixture(w, FixtureType.MI_PLANT_BED)
        w.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == ThingType.SEEDS || it.type == ThingType.WATERING_CAN }.forEach { s.removeThing(it, quiet = true) }
        repeat(8) { s.tap(place, bed, 0f, 0f) }
        assertEquals(1, w.bodiesIn(place).filterIsInstance<Thing>().count { it.type == ThingType.SEEDS })
        assertEquals(1, w.bodiesIn(place).filterIsInstance<Thing>().count { it.type == ThingType.WATERING_CAN })
        assertEquals(0, bed.count)
    }

    @Test fun `counter keeps working when moved through storage to the family house`() {
        val (w, s) = setup()
        val counter = fixture(w, FixtureType.MI_COUNTER)
        val egg = thing(w, ThingType.EGG)
        assertTrue(s.dropInto(place, counter, egg))
        assertTrue(s.designer.store(place, counter))
        assertEquals(Mode.FREE, egg.mode)
        assertTrue(w.bodies.containsKey(egg.id))
        val moved = s.designer.unstore(PlaceId.HOME, w.storage.lastIndex, 1.3f, 0.9f)!!
        s.tap(PlaceId.HOME, moved, 0f, 0f)
        for (type in listOf(ThingType.EGG, ThingType.MILK)) {
            val ingredient = w.bodiesIn(PlaceId.HOME).filterIsInstance<Thing>().first { it.type == type }
            assertTrue(s.dropInto(PlaceId.HOME, moved, ingredient))
        }
        repeat(60) { s.step(PlaceId.HOME, 1f / 30f) }
        assertTrue(w.bodiesIn(PlaceId.HOME).filterIsInstance<Thing>().any { it.type == ThingType.DOUGH })
        assertTrue(w.inMachine(moved).isEmpty())
    }
}
