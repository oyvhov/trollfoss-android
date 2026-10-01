package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/** The tivoli, the shop, the doctor, the stage and the sea floor. */
class AttractionsTest {
    private val events = ArrayList<Fx>()

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            events += fx
        }
    }, Random(3))

    private fun step(sim: Sim, place: PlaceId, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun fixture(world: World, place: PlaceId, type: FixtureType) = world.fixturesIn(place).first { it.type == type }

    @Test
    fun `the ferris wheel takes its riders over the top`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val wheel = fixture(world, PlaceId.TIVOLI, FixtureType.FERRIS_WHEEL)
        val rider = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.TIVOLI, wheel.x, 0.5f)
        assertTrue(sim.seat(rider, wheel, 0))
        val startY = rider.y
        sim.tap(PlaceId.TIVOLI, wheel, 0f, -0.4f)
        step(sim, PlaceId.TIVOLI, 20f)
        assertTrue(rider.y != startY)
        assertTrue(Fx.WHEE in events)
        assertTrue("tivoli_top" in world.unlocked)
    }

    @Test
    fun `a ball thrown at the cans knocks them over and wins a prize`() {
        val world = WorldFactory.create(Random(2))
        val sim = sim(world)
        val stall = fixture(world, PlaceId.TIVOLI, FixtureType.CAN_TOSS)
        val before = world.bodiesIn(PlaceId.TIVOLI).size
        val ball = world.addThing(ThingType.BALL, 0, PlaceId.TIVOLI, stall.x - 0.3f, stall.y - 0.26f)
        ball.vx = 3f
        ball.vy = -0.4f
        step(sim, PlaceId.TIVOLI, 0.4f)
        assertEquals(1, stall.mode)
        assertTrue(Fx.KNOCK in events)
        assertTrue(world.bodiesIn(PlaceId.TIVOLI).size >= before + 2)
        assertTrue("tivoli_cans" in world.unlocked)
    }

    @Test
    fun `the checkout belt carries things past the scanner`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val checkout = fixture(world, PlaceId.SHOP, FixtureType.CHECKOUT)
        step(sim, PlaceId.SHOP, 0.5f)
        val melon = world.bodiesIn(PlaceId.SHOP).first { it is Thing && it.type == ThingType.WATERMELON }
        assertEquals(checkout.id, melon.restOwner)
        val start = melon.x
        step(sim, PlaceId.SHOP, 3f)
        assertTrue(melon.x > start + 0.15f)
        assertTrue(Fx.SCAN in events)
    }

    @Test
    fun `a pushed trolley rolls with what lies in its basket`() {
        val world = WorldFactory.create(Random(4))
        val sim = sim(world)
        val cart = fixture(world, PlaceId.SHOP, FixtureType.CART)
        step(sim, PlaceId.SHOP, 0.5f)
        val apple = world.bodiesIn(PlaceId.SHOP).first { it.restOwner == cart.id }
        val offset = apple.x - (cart.x + cart.shiftX)
        sim.tap(PlaceId.SHOP, cart, 0.1f, -0.1f)
        step(sim, PlaceId.SHOP, 1.5f)
        assertTrue(abs(cart.shiftX) > 0.2f)
        assertEquals(offset, apple.x - (cart.x + cart.shiftX), 0.01f)
        assertTrue(apple.resting)
    }

    @Test
    fun `the x-ray shows whoever stands in it`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val xray = fixture(world, PlaceId.DOCTOR, FixtureType.XRAY)
        val patient = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.DOCTOR, xray.x, 0.9f)
        assertTrue(sim.seat(patient, xray, 0))
        assertEquals(Pose.STAND, patient.anim.pose)
        sim.tap(PlaceId.DOCTOR, xray, 0f, -0.2f)
        step(sim, PlaceId.DOCTOR, 0.2f)
        assertTrue("doctor_xray" in world.unlocked)
    }

    @Test
    fun `drums, xylophone and a song make a band`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world)
        sim.tap(PlaceId.STAGE, fixture(world, PlaceId.STAGE, FixtureType.DRUM_KIT), 0f, -0.1f)
        sim.tap(PlaceId.STAGE, fixture(world, PlaceId.STAGE, FixtureType.XYLOPHONE), 0.05f, -0.1f)
        sim.tap(PlaceId.STAGE, fixture(world, PlaceId.STAGE, FixtureType.MIC_STAND), 0f, -0.2f)
        assertTrue(Fx.SING in events)
        assertTrue("stage_band" in world.unlocked)
    }

    @Test
    fun `the speaker makes loose things jump`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        step(sim, PlaceId.STAGE, 0.5f)
        val speaker = fixture(world, PlaceId.STAGE, FixtureType.SPEAKER)
        val drum = world.bodiesIn(PlaceId.STAGE).first { it is Thing && it.type == ThingType.DRUM }
        assertTrue(drum.resting)
        sim.tap(PlaceId.STAGE, speaker, 0f, -0.2f)
        assertFalse(drum.resting)
        assertTrue(Fx.BOOM in events)
    }

    @Test
    fun `under water figures sink slowly and swim, light things rise`() {
        val world = WorldFactory.create(Random(8))
        val sim = sim(world)
        val iver = world.people().first { it.name == "Iver" }
        assertEquals(PlaceId.UNDERWATER, iver.place)
        val ring = world.bodiesIn(PlaceId.UNDERWATER).first { it is Thing && it.type == ThingType.SWIM_RING }
        val start = iver.y
        step(sim, PlaceId.UNDERWATER, 1f)
        assertEquals(Pose.SWIM, iver.anim.pose)
        assertTrue(iver.y > start && iver.y - start < 0.1f)
        step(sim, PlaceId.UNDERWATER, 4f)
        assertEquals(PlaceId.UNDERWATER.ceiling + ring.h, ring.y, 0.01f)
        val boot = world.bodiesIn(PlaceId.UNDERWATER).first { it is Thing && it.type == ThingType.BOOT }
        assertTrue(boot.resting)
    }

    @Test
    fun `the octopus squirts ink at the nearest figure`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val octopus = fixture(world, PlaceId.UNDERWATER, FixtureType.OCTOPUS)
        val diver = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.UNDERWATER, octopus.x + 0.2f, 0.9f)
        repeat(3) { sim.tap(PlaceId.UNDERWATER, octopus, 0f, -0.2f) }
        assertTrue(diver.anim.ink > 0f)
        assertTrue("sea_octopus" in world.unlocked)
    }

    @Test
    fun `a save from before the new places gets them filled, without doubling names`() {
        val world = WorldFactory.create(Random(11))
        // Pretend the save is old: nothing lives in the new places.
        val newPlaces = setOf(PlaceId.TIVOLI, PlaceId.SHOP, PlaceId.DOCTOR, PlaceId.STAGE, PlaceId.UNDERWATER)
        world.bodies.values.filter { it.place in newPlaces }.map { it.id }.forEach { world.bodies.remove(it) }
        val known = PlaceId.entries.toSet() - newPlaces
        assertTrue(WorldFactory.addMissingPlaces(world, known))
        assertTrue(world.bodiesIn(PlaceId.UNDERWATER).any { it is Person })
        val names = world.people().map { it.name }.filter { it.isNotBlank() }
        assertEquals(names.size, names.toSet().size)
        // Places the save already knew are left alone, even when empty.
        world.bodies.values.filter { it.place == PlaceId.SHOP }.map { it.id }.forEach { world.bodies.remove(it) }
        assertFalse(WorldFactory.addMissingPlaces(world, PlaceId.entries.toSet()))
        assertTrue(world.bodiesIn(PlaceId.SHOP).isEmpty())
    }

    @Test
    fun `every new place has named folk and glimt`() {
        val world = WorldFactory.create(Random(10))
        for (place in listOf(PlaceId.TIVOLI, PlaceId.SHOP, PlaceId.DOCTOR, PlaceId.STAGE, PlaceId.UNDERWATER)) {
            assertTrue(world.bodiesIn(place).any { it is Person && it.species == Species.FOLK && it.name.isNotBlank() })
            assertEquals(3, Secrets.inPlace(place).size)
        }
    }

    // ------------------------------------------------------------------ Heileberget

    @Test
    fun `the cable car takes riders up to the ledge and lets them out`() {
        val world = WorldFactory.create(Random(51))
        val sim = sim(world)
        val cabin = fixture(world, PlaceId.HEILEBERGET, FixtureType.CABLE_CAR)
        val rider = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HEILEBERGET, cabin.x, 0.8f)
        assertTrue(sim.seat(rider, cabin, 0))
        sim.tap(PlaceId.HEILEBERGET, cabin, 0f, -0.1f)
        assertTrue(cabin.on)
        step(sim, PlaceId.HEILEBERGET, 8.5f)
        assertEquals(1, cabin.mode)
        assertEquals(Mode.FREE, rider.mode)
        assertEquals(Attractions.CABLE_TRAVEL, cabin.shiftX, 0.01f)
        step(sim, PlaceId.HEILEBERGET, 2f)
        val ledge = fixture(world, PlaceId.HEILEBERGET, FixtureType.ROCK_LEDGE)
        assertEquals(ledge.id, rider.restOwner)
        assertTrue(Fx.ARRIVE in events)
        assertTrue("berg_cable" in world.unlocked)
        // And back down again from the top station's bell.
        val top = world.fixturesIn(PlaceId.HEILEBERGET).filter { it.type == FixtureType.CABLE_STATION }.maxByOrNull { it.x }!!
        sim.tap(PlaceId.HEILEBERGET, top, 0f, -0.1f)
        step(sim, PlaceId.HEILEBERGET, 8.5f)
        assertEquals(0, cabin.mode)
        assertEquals(0f, cabin.shiftX, 0.01f)
    }

    @Test
    fun `the echo rock answers, and three shouts find the glimt`() {
        val world = WorldFactory.create(Random(52))
        val sim = sim(world)
        val rock = fixture(world, PlaceId.HEILEBERGET, FixtureType.ECHO_ROCK)
        repeat(3) { sim.tap(PlaceId.HEILEBERGET, rock, 0f, -0.2f) }
        assertEquals(3, events.count { it == Fx.ECHO })
        assertTrue("berg_echo" in world.unlocked)
    }

    @Test
    fun `a figure on the summit raises the flag`() {
        val world = WorldFactory.create(Random(53))
        val sim = sim(world)
        val flag = fixture(world, PlaceId.HEILEBERGET, FixtureType.SUMMIT_FLAG)
        val summit = fixture(world, PlaceId.HEILEBERGET, FixtureType.SUMMIT_ROCK)
        assertEquals(summit.id, flag.host)
        step(sim, PlaceId.HEILEBERGET, 1f)
        // The goat on the summit does not count; a figure does.
        val hiker = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HEILEBERGET, summit.x - 0.2f, summit.y - summit.spec.h - 0.1f)
        step(sim, PlaceId.HEILEBERGET, 2f)
        assertEquals(summit.id, hiker.restOwner)
        assertEquals(1, flag.mode)
        assertTrue(Fx.SUMMIT in events)
        assertTrue("berg_top" in world.unlocked)
    }

    @Test
    fun `Heileberget is the longest place, with goats and its hikers`() {
        val world = WorldFactory.create(Random(54))
        assertTrue(PlaceId.entries.all { it == PlaceId.HEILEBERGET || it.big || it.width < PlaceId.HEILEBERGET.width })
        val people = world.bodiesIn(PlaceId.HEILEBERGET).filterIsInstance<Person>()
        assertEquals(3, people.count { it.species == Species.GOAT })
        assertEquals(setOf("BesteSonja", "Tuva"), people.filter { it.species == Species.FOLK }.map { it.name }.toSet())
    }
}
