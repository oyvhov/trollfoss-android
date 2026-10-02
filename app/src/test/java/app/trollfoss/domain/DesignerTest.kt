package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** The home designer and tidying up. */
class DesignerTest {
    @Test fun `deleting one stored item preserves duplicates and saving and can be undone after travel`() {
        val w = WorldFactory.create(); val s = Sim(w)
        val first = Stored(FixtureType.FLOWER_POT, 0)
        val second = Stored(FixtureType.FLOWER_POT, 1)
        w.storage += listOf(first, second, first)
        val fixturesBefore = w.fixtures.size
        assertTrue(s.designer.discard(1))
        assertEquals(listOf(first, first), w.storage)
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        assertEquals(w.storage, loaded.storage)
        assertEquals(fixturesBefore, loaded.fixtures.size)
        assertFalse(s.designer.discard(99))
        assertTrue(s.designer.discard(0))
        // A remaining piece can be placed elsewhere before both deletions are undone.
        assertNotNull(s.designer.unstore(PlaceId.BEACH, 0, 1f, 0.9f))
        assertTrue(s.designer.undoDiscard())
        assertTrue(s.designer.undoDiscard())
        assertEquals(listOf(first, second), w.storage)
        assertFalse(s.designer.canUndoDiscard)
        assertFalse(s.designer.undoDiscard())
    }

    @Test fun `storage can place furniture at every destination including the beach and saves it`() {
        for (place in PlaceId.entries) {
            val w = WorldFactory.create(Random(1)); val s = Sim(w)
            MineDemo.fill(s, 0, 0)
            w.storage += Stored(FixtureType.CAMPFIRE, 2)
            val f = s.designer.unstore(place, 0, place.width - 0.4f, place.floor)!!
            assertTrue("$place", w.storage.isEmpty())
            if (place == PlaceId.BEACH) assertTrue(f.x + f.spec.w / 2f <= 2.3f)
            val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
            assertEquals("$place", FixtureType.CAMPFIRE, loaded.fixtures[f.id]?.type)
            assertEquals(f.x, loaded.fixtures[f.id]!!.x, 0.001f)
        }
    }

    @Test fun `more than twenty additions survive reload and full destinations keep the stored item`() {
        val w = WorldFactory.create(); val s = Sim(w); val place = PlaceId.BEACH
        repeat(30) { assertNotNull(s.designer.add(place, FixtureType.FLOWER_POT, it % 3, 1f, place.floor)) }
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        assertEquals(30, loaded.fixturesIn(place).count { place.indexOf(it.id) >= place.addedFrom })
        for (i in place.addedFrom..place.addedMax) {
            w.fixtures[place.idBase + i] = Fixture(place.idBase + i, place, FixtureType.STOOL, 1f, place.floor)
        }
        w.storage += Stored(FixtureType.BENCH, 0)
        assertNull(s.designer.unstore(place, 0, 1f, place.floor))
        assertEquals(listOf(Stored(FixtureType.BENCH, 0)), w.storage)
        w.fixtures.remove(place.idBase + place.addedMax)
        assertNotNull(s.designer.unstore(place, 0, 1f, place.floor))
        assertTrue(w.storage.isEmpty())
    }

    @Test fun `an unbuilt house keeps furniture in storage until a floor exists`() {
        val w = WorldFactory.create(); val s = Sim(w)
        w.storage += Stored(FixtureType.SOFA, 0)
        assertNull(s.designer.unstore(PlaceId.MINE_GROUND, 0, 5f, 0.9f))
        assertEquals(1, w.storage.size)
        MineDemo.fill(s, 0, 0)
        assertNotNull(s.designer.unstore(PlaceId.MINE_GROUND, 0, 5f, 0.9f))
        assertTrue(w.storage.isEmpty())
    }

    private val events = ArrayList<Fx>()

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            events += fx
        }
    }, Random(5))

    private fun step(sim: Sim, place: PlaceId, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    @Test
    fun `furniture from the catalogue can be added, stored and brought back`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val beanbag = sim.designer.add(PlaceId.HOME, FixtureType.BEANBAG, 1, 1.5f, 0.9f)
        assertNotNull(beanbag)
        assertTrue(beanbag!!.place.indexOf(beanbag.id) >= beanbag.place.addedFrom)
        assertTrue(Fx.PLACE in events)
        assertTrue(sim.designer.store(PlaceId.HOME, beanbag))
        assertNull(world.fixtures[beanbag.id])
        assertEquals(Stored(FixtureType.BEANBAG, 1), world.storage.single())
        val back = sim.designer.unstore(PlaceId.HOME, 0, 2.0f, 0.9f)
        assertNotNull(back)
        assertTrue(world.storage.isEmpty())
    }

    @Test
    fun `furniture with an unfound glimt cannot be stored`() {
        val world = WorldFactory.create(Random(2))
        val sim = sim(world)
        val bed = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.BED }
        assertFalse(sim.designer.store(PlaceId.HOME, bed))
        world.found += "home_pillow"
        assertTrue(sim.designer.store(PlaceId.HOME, bed))
    }

    @Test
    fun `storing a bed makes its sleeper stand up and the pillow fall`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        world.found += "home_pillow"
        val bed = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.BED }
        val hedda = world.people().first { it.name == "Hedda" }
        assertEquals(Mode.SEATED, hedda.mode)
        sim.designer.store(PlaceId.HOME, bed)
        assertEquals(Mode.FREE, hedda.mode)
        step(sim, PlaceId.HOME, 2f)
        val pillow = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.PILLOW }
        assertTrue(pillow.resting)
        assertEquals(-1, pillow.restOwner)
    }

    @Test
    fun `tidying sends things home and clears away leftovers`() {
        val world = WorldFactory.create(Random(4))
        val sim = sim(world)
        val milk = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.MILK } as Thing
        val home = milk.x to milk.y
        val fridge = world.fixtures[milk.homeOwner]!!
        assertEquals(FixtureType.FRIDGE, fridge.type)
        // Take the milk out of the fridge and leave it on the floor.
        milk.inside = -1
        milk.restOwner = -2
        milk.x = 1.5f
        milk.y = 0.5f
        milk.resting = false
        val peel = world.addThing(ThingType.BANANA_PEEL, 0, PlaceId.HOME, 1.7f, 0.5f)
        val hat = world.addThing(ThingType.PARTY_HAT, 1, PlaceId.HOME, 1.9f, 0.5f)
        step(sim, PlaceId.HOME, 1f)
        assertTrue(sim.designer.tidy(PlaceId.HOME) >= 3)
        assertFalse(world.bodies.containsKey(peel.id))
        step(sim, PlaceId.HOME, 1.5f)
        assertEquals(home.first, milk.x, 0.005f)
        assertEquals(home.second, milk.y, 0.005f)
        assertEquals(fridge.id, milk.inside)
        val chest = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.CHEST }
        assertEquals(chest.id, hat.inside)
        assertTrue(Fx.TIDY in events)
    }

    @Test
    fun `the bin eats leftovers but sends things with a home back`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val bin = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.TRASH_BIN }
        val peel = world.addThing(ThingType.BANANA_PEEL, 0, PlaceId.HOME, bin.x, 0.5f)
        assertTrue(sim.dropInto(PlaceId.HOME, bin, peel))
        assertFalse(world.bodies.containsKey(peel.id))
        val teddy = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.TEDDY } as Thing
        assertTrue(sim.dropInto(PlaceId.HOME, bin, teddy))
        assertTrue(world.bodies.containsKey(teddy.id))
        assertTrue(teddy.flyT >= 0f)
    }

    @Test
    fun `the robot vacuum slurps up what lies on the floor`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world)
        val robot = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.ROBOT_VACUUM }
        val crumbs = world.addThing(ThingType.COOKIE, 0, PlaceId.HOME, robot.x + 0.3f, robot.depth)
        crumbs.ground = robot.depth
        step(sim, PlaceId.HOME, 1f)
        sim.tap(PlaceId.HOME, robot, 0f, -0.01f)
        step(sim, PlaceId.HOME, 5f)
        assertFalse(world.bodies.containsKey(crumbs.id))
        assertTrue(Fx.SUCK in events)
    }

    @Test
    fun `wallpaper, floors, added and stored furniture survive saving`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        // A board that nothing here counts for, so the number of stickers does not depend on how the deck is shuffled.
        world.taskSet.clear()
        world.taskSet += listOf("sneeze", "slip", "burp")
        sim.designer.restyle(PlaceId.HOME, 1, wall = 4, floor = 3)
        val lamp = sim.designer.add(PlaceId.CAFE, FixtureType.FLOWER_POT, 2, 1.0f, 0.9f)!!
        world.found += "home_pillow"
        sim.designer.store(PlaceId.HOME, world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.BED })
        // Painting may have finished a dealt task already (it depends on how the deck was shuffled); count only these.
        world.stickers.clear()
        world.stickers += listOf(1, 2, 3)
        val json = WorldStore.encode(world, Settings()).toString()
        val w2 = WorldStore.decode(org.json.JSONObject(json)).world
        assertEquals(RoomStyle(4, 3), Decor.style(w2, PlaceId.HOME, 1))
        assertEquals(FixtureType.FLOWER_POT, w2.fixtures[lamp.id]?.type)
        assertTrue(w2.fixturesIn(PlaceId.HOME).none { it.type == FixtureType.BED })
        assertEquals(listOf(Stored(FixtureType.BED, 0)), w2.storage)
        assertEquals(world.stickers.size, w2.stickers.size)
        val milk = w2.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.MILK } as Thing
        assertEquals(PlaceId.HOME, milk.homePlace)
    }
    /** An empty hairdresser's: one room, so only the rules for the back wall and for neighbours are at work. */
    private fun emptyRoom(): Pair<World, Sim> {
        val world = WorldFactory.create(Random(1))
        world.fixtures.values.removeAll { it.place == PlaceId.SALON }
        val sim = sim(world)
        sim.invalidate(PlaceId.SALON)
        return world to sim
    }

    @Test
    fun `furniture let go near the back wall stands flush against it`() {
        val (_, sim) = emptyRoom()
        val sofa = sim.designer.add(PlaceId.SALON, FixtureType.SOFA, 0, 1.4f, PlaceId.SALON.back + 0.02f)!!
        assertEquals(PlaceId.SALON.back + 0.004f, sofa.y, 0.0005f)
        // Further out on the floor it stays where it was put.
        val spot = sim.designer.settle(PlaceId.SALON, sofa, 1.4f, 0.9f)
        assertEquals(0.9f, spot[1], 0.0005f)
    }

    @Test
    fun `furniture half inside a neighbour settles edge to edge with it`() {
        val (_, sim) = emptyRoom()
        val a = sim.designer.add(PlaceId.SALON, FixtureType.STOOL, 0, 1.0f, 0.9f)!!
        val w = a.spec.w
        val spot = sim.designer.settle(PlaceId.SALON, a, 1.0f, 0.9f)
        assertEquals("alone, it stays", 1.0f, spot[0], 0.0005f)
        val b = sim.designer.add(PlaceId.SALON, FixtureType.STOOL, 0, 1.0f + w * 0.6f, 0.905f)!!
        assertTrue("beside, not inside: ${b.x - a.x} against $w", b.x - a.x >= w)
        assertTrue("and close", b.x - a.x < w + 0.02f)
        assertEquals("in line in depth", a.y, b.y, 0.0005f)
        // A big piece right on top of another is the child's own idea: it is not flung half a room away.
        val sofa = sim.designer.add(PlaceId.SALON, FixtureType.SOFA, 0, 2.0f, 0.95f)!!
        val other = sim.designer.add(PlaceId.SALON, FixtureType.SOFA, 0, 2.0f, 0.95f)!!
        assertEquals(sofa.x, other.x, 0.0005f)
    }

    @Test
    fun `a picture settles centred over the furniture below, and a rug lies where it is put`() {
        val (_, sim) = emptyRoom()
        val sofa = sim.designer.add(PlaceId.SALON, FixtureType.SOFA, 0, 1.4f, PlaceId.SALON.back + 0.004f)!!
        val picture = sim.designer.add(PlaceId.SALON, FixtureType.PICTURE, 0, sofa.x + 0.04f, sofa.top - 0.1f)!!
        assertEquals(sofa.x, picture.x, 0.0005f)
        val second = sim.designer.add(PlaceId.SALON, FixtureType.PICTURE, 1, sofa.x + 0.5f, picture.y + 0.02f)!!
        assertEquals("the two pictures hang at the same height", picture.y - picture.spec.h / 2f, second.y - second.spec.h / 2f, 0.0005f)
        val rug = sim.designer.add(PlaceId.SALON, FixtureType.RUG, 0, sofa.x + 0.03f, PlaceId.SALON.back + 0.02f)!!
        assertEquals(sofa.x + 0.03f, rug.x, 0.0005f)
        assertEquals(PlaceId.SALON.back + 0.02f, rug.y, 0.0005f)
    }
    @Test
    fun `a thing let go just beside a table is nudged onto it`() {
        val (world, sim) = emptyRoom()
        val table = sim.designer.add(PlaceId.SALON, FixtureType.TABLE, 0, 1.4f, 0.9f)!!
        val edge = table.x + 0.15f
        val apple = world.addThing(ThingType.APPLE, 0, PlaceId.SALON, edge + 0.02f, table.top - 0.1f)
        val x = sim.nudgeOnto(PlaceId.SALON, apple)
        assertNotNull(x)
        assertTrue("on the table: $x", x!! < edge && x > table.x)
        // Over the table already, far beside it, or put down on the floor: left alone.
        apple.x = table.x
        assertNull(sim.nudgeOnto(PlaceId.SALON, apple))
        apple.x = edge + 0.3f
        assertNull(sim.nudgeOnto(PlaceId.SALON, apple))
        apple.x = edge + 0.02f
        apple.y = 0.9f
        assertNull(sim.nudgeOnto(PlaceId.SALON, apple))
    }
}
