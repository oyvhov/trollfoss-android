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
        world.stickers += listOf(1, 2, 3)
        val json = WorldStore.encode(world, Settings()).toString()
        val w2 = WorldStore.decode(org.json.JSONObject(json)).world
        assertEquals(RoomStyle(4, 3), Decor.style(w2, PlaceId.HOME, 1))
        assertEquals(FixtureType.FLOWER_POT, w2.fixtures[lamp.id]?.type)
        assertTrue(w2.fixturesIn(PlaceId.HOME).none { it.type == FixtureType.BED })
        assertEquals(listOf(Stored(FixtureType.BED, 0)), w2.storage)
        assertEquals(3, w2.stickers.size)
        val milk = w2.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.MILK } as Thing
        assertEquals(PlaceId.HOME, milk.homePlace)
    }
}
