package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Storhuset: the floors, the passages between them and the ids of their furniture. */
class HouseTest {
    private val passages = ArrayList<Passage>()

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onPassage(passage: Passage, arrivalX: Float, riders: Int) {
            passages += passage
        }
    }, Random(3))

    private val passageTypes = setOf(
        FixtureType.STAIRCASE, FixtureType.LIFT, FixtureType.SLIDE, FixtureType.FIRE_POLE, FixtureType.HATCH,
        FixtureType.LADDER, FixtureType.SECRET_DOOR, FixtureType.DOOR, FixtureType.DUMBWAITER,
    )

    @Test
    fun `fixture ids never overlap between places and map back to their place`() {
        val ids = HashSet<Int>()
        for (place in PlaceId.entries) {
            val count = Places.spec(place).fixtures.size
            assertTrue("$place has room for its blueprint", count <= place.addedFrom)
            for (i in 0 until count) {
                val id = WorldFactory.fixtureId(place, i)
                assertTrue("id $id of $place is new", ids.add(id))
                assertEquals(place, PlaceId.ofFixture(id))
                assertEquals(i, place.indexOf(id))
            }
            // The slots for added furniture belong to the place too.
            assertEquals(place, PlaceId.ofFixture(place.idBase + place.addedFrom))
            assertEquals(place, PlaceId.ofFixture(place.idBase + place.addedMax))
        }
    }

    @Test
    fun `every floor has a blueprint with rooms, and only the ground floor is on the map`() {
        for (place in PlaceId.entries.filter { it.manor }) {
            assertNotNull(House.floor(place))
            assertTrue(Places.spec(place).fixtures.isNotEmpty())
            val rooms = Decor.rooms(place)
            assertEquals("rooms of $place cover it", 0f, rooms.first().start, 0.001f)
            assertEquals(place.width, rooms.last().endInclusive, 0.001f)
            for (i in 1 until rooms.size) assertEquals(rooms[i - 1].endInclusive, rooms[i].start, 0.001f)
            assertEquals(place == PlaceId.MANOR_GROUND, place.onMap)
        }
        assertTrue(PlaceId.entries.filter { !it.manor }.all { it.onMap })
    }

    @Test
    fun `passages point at real fixtures and real arrivals`() {
        val ids = HashSet<String>()
        for (p in House.passages) {
            assertTrue("passage id ${p.id} is new", ids.add(p.id))
            val spec = Places.spec(p.place)
            val def = spec.fixtures.getOrNull(p.fixture)
            assertNotNull("${p.id} has a fixture", def)
            assertTrue("${p.id} stands on a way between floors", def!!.type in passageTypes)
            val arrival = House.arrival(p.to, p.arrive)
            assertNotNull("${p.id} arrives at ${p.to}/${p.arrive}", arrival)
            assertTrue("${p.id} arrives inside ${p.to}", arrival!!.x in 0f..p.to.width)
            // A way leaves from a floor of the house, or from Trollhola (the mine door at the end of the cellar's tunnel).
            assertTrue("${p.id} leaves from its own floor", House.hasPassages(p.place) && (p.place == PlaceId.LAB || House.floor(p.place)?.place == p.place))
        }
    }

    @Test
    fun `every way that is not one-way has a way back`() {
        for (p in House.passages.filter { !it.oneWay }) {
            val back = House.passages.filter { it.place == p.to && it.to == p.place }
            assertTrue("${p.id} has a way back from ${p.to}", back.isNotEmpty())
        }
    }

    @Test
    fun `every arrival that a passage names is on the arriving floor`() {
        for (floor in House.floors) {
            val names = floor.arrivals.map { it.name }
            assertEquals("arrival names of ${floor.place} are unique", names.size, names.toSet().size)
        }
    }

    @Test
    fun `whoever stands at the stairs goes up with the view`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val stairs = world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MANOR_GROUND, 0))
        val rider = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MANOR_GROUND, stairs.x + 0.1f, PlaceId.MANOR_GROUND.floor, "Rider")
        val bystander = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MANOR_GROUND, 9.0f, PlaceId.MANOR_GROUND.floor, "Bystander")
        sim.tap(PlaceId.MANOR_GROUND, stairs, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_UPPER, rider.place)
        assertEquals(PlaceId.MANOR_GROUND, bystander.place)
        assertEquals(1, passages.size)
        assertEquals(PlaceId.MANOR_UPPER, passages[0].to)
        val arrival = House.arrival(PlaceId.MANOR_UPPER, "landing")!!
        assertEquals(arrival.x, rider.x, 0.2f)
    }

    @Test
    fun `a hat goes along with its wearer`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val stairs = world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MANOR_GROUND, 0))
        val rider = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MANOR_GROUND, stairs.x, PlaceId.MANOR_GROUND.floor, "Rider")
        val hat = world.addThing(ThingType.CAP, 0, PlaceId.MANOR_GROUND, rider.x, rider.y)
        sim.give(rider, hat, Part.HAT)
        sim.tap(PlaceId.MANOR_GROUND, stairs, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_UPPER, hat.place)
    }

    @Test
    fun `a locked way only wiggles until the key is found`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val shelf = world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MANOR_GROUND, 3))
        val rider = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MANOR_GROUND, shelf.x, PlaceId.MANOR_GROUND.floor, "Rider")
        sim.tap(PlaceId.MANOR_GROUND, shelf, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_GROUND, rider.place)
        assertTrue(passages.isEmpty())
        world.unlocked += "manor_bookshelf"
        sim.tap(PlaceId.MANOR_GROUND, shelf, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_ATTIC, rider.place)
        assertEquals(1, passages.size)
    }

    @Test
    fun `the way in and the fixtures of passages cannot be put away`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val stairs = world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MANOR_GROUND, 0))
        assertFalse(sim.designer.canStore(PlaceId.MANOR_GROUND, stairs))
    }

    @Test
    fun `furniture can be added to a floor of the house in its own id range`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val chair = sim.designer.add(PlaceId.MANOR_UPPER, FixtureType.ARMCHAIR, 0, 3.0f, 0.9f)
        assertNotNull(chair)
        assertEquals(PlaceId.MANOR_UPPER, PlaceId.ofFixture(chair!!.id))
        assertTrue(PlaceId.MANOR_UPPER.indexOf(chair.id) in PlaceId.MANOR_UPPER.addedFrom..PlaceId.MANOR_UPPER.addedMax)
    }

    @Test
    fun `people wander between floors but never out of the floor you are on`() {
        val world = WorldFactory.create(Random(1))
        val here = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MANOR_GROUND, 3f, PlaceId.MANOR_GROUND.floor, "Here")
        val away = (0 until 12).map { world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MANOR_ATTIC, 2f, PlaceId.MANOR_ATTIC.floor, "Away$it") }
        val elsewhere = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1f, PlaceId.HOME.floor, "Elsewhere")
        House.shuffle(world, PlaceId.MANOR_GROUND, Random(9), night = false)
        assertEquals(PlaceId.MANOR_GROUND, here.place)
        assertEquals(PlaceId.HOME, elsewhere.place)
        assertTrue("somebody moved from the attic", away.any { it.place != PlaceId.MANOR_ATTIC })
        assertTrue(away.all { it.place!!.manor })
        assertNull(House.floor(PlaceId.HOME))
    }
}
