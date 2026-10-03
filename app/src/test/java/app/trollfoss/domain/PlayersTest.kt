package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class PlayersTest {
    @Test fun packingKeepsTheFigureAndClothesAndStopsFollowingAcrossRestart() {
        val world = WorldFactory.create()
        val p = world.people().first { it.species == Species.FOLK }
        Players.toggle(world, p)
        val hat = world.addThing(ThingType.PARTY_HAT, 2, p.place, p.x, p.y).apply {
            mode = Mode.WORN; holder = p.id; slot = Slot.HEAD.ordinal
        }
        val look = p.look
        val count = world.bodies.size
        p.mode = Mode.SEATED
        p.held = true
        world.mine.guests.add(Guest(p.id, PlaceId.HOME, 1f))
        assertTrue(Players.pack(world, p))
        Players.arrive(world, PlaceId.BEACH, 1f)
        assertEquals(count, world.bodies.size)
        assertEquals(Mode.BAG, p.mode)
        assertNull(p.place)
        assertFalse(p.held)
        assertTrue(p.id in world.playerIds)
        assertTrue(Players.paused(world,p))
        assertTrue(Players.activeTeam(world).isEmpty())
        assertSame(p,Players.team(world).single())
        assertTrue(world.mine.guests.none { it.id == p.id })
        val saved = WorldStore.decode(WorldStore.encode(world, Settings())).world
        val recovered = saved.bodies[p.id] as Person
        assertEquals(look, recovered.look)
        assertEquals(Mode.BAG, recovered.mode)
        assertEquals(hat.id, saved.worn(recovered, Slot.HEAD)?.id)
        assertNull(saved.worn(recovered, Slot.HEAD)?.place)
        assertTrue(PlayConnections.invite(Sim(saved), recovered, PlaceId.BEACH, 1f))
        assertEquals(Mode.FREE, recovered.mode)
        assertEquals(PlaceId.BEACH, recovered.place)
        assertEquals(PlaceId.BEACH, saved.worn(recovered, Slot.HEAD)?.place)
        assertTrue(recovered.id in saved.playerIds)
        assertFalse(Players.paused(saved,recovered))
        assertSame(recovered,Players.activeTeam(saved).single())
        Players.arrive(saved, PlaceId.HOME, 1f)
        assertSame(recovered, Players.team(saved).single())
        assertEquals(PlaceId.HOME, recovered.place)
    }
    @Test fun twoPlayersKeepTheirLooksClothesAndBelongingsThroughEveryPlaceAndRestart() {
        val world = WorldFactory.create()
        val players = world.people().filter { it.species == Species.FOLK }.take(2)
        players.forEach { Players.toggle(world, it) }
        val looks = players.map { it.look }
        val belongings = players.map { person ->
            world.addThing(ThingType.PARTY_HAT, 2, person.place, person.x, person.y).apply {
                mode = Mode.WORN; holder = person.id; slot = Slot.HEAD.ordinal
            }
        }
        val bodyCount = world.bodies.size
        for (place in PlaceId.entries) {
            players[0].mode = Mode.SEATED
            players[1].mode = Mode.BAG
            Players.arrive(world, place, 1f)
            assertEquals(bodyCount, world.bodies.size)
            players.forEachIndexed { i, person ->
                assertSame(person, world.bodies[person.id])
                assertEquals(place, person.place)
                assertEquals(looks[i], person.look)
                assertEquals(Mode.FREE, person.mode)
                assertEquals(Pose.STAND, person.anim.pose)
                assertEquals(place, belongings[i].place)
                assertEquals(person.id, belongings[i].holder)
                assertEquals(Mode.WORN, belongings[i].mode)
            }
        }
        world.flags.add(Players.CHOSEN)
        val saved = WorldStore.decode(WorldStore.encode(world, Settings())).world
        assertEquals(players.map { it.id }, Players.team(saved).map { it.id })
        Players.team(saved).forEachIndexed { i, p ->
            assertEquals(looks[i], p.look)
            assertEquals(belongings[i].id, saved.worn(p, Slot.HEAD)?.id)
        }
        assertTrue(Players.CHOSEN in saved.flags)
    }

    @Test fun selectionIsOrderedSupportsMoreThanTwoAndNeverDuplicates() {
        val world = WorldFactory.create()
        val people = world.people().filter { it.species == Species.FOLK }.take(5)
        people.reversed().forEach { Players.toggle(world, it) }
        assertEquals(people.reversed(), Players.team(world))
        Players.toggle(world, people[0])
        assertFalse(people[0].id in world.playerIds)
        Players.toggle(world, people[0])
        assertEquals(people[0], Players.team(world).last())
        val animal = world.people().first { it.species != Species.FOLK }
        Players.toggle(world, animal)
        assertEquals(5, Players.team(world).size)
    }

    @Test fun oldSavesOfferAChoiceWithoutChangingOrReplacingAnyFigure() {
        val world = WorldFactory.create()
        val json = WorldStore.encode(world, Settings()).apply { remove("players") }
        val loaded = WorldStore.decode(json).world
        assertTrue(loaded.playerIds.isEmpty())
        assertEquals(world.people().map { it.name }, loaded.people().map { it.name })
        json.put("players", JSONArray(listOf(-1, 999999, world.people().first { it.species != Species.FOLK }.id)))
        assertTrue(WorldStore.decode(json).world.playerIds.isEmpty())
    }

    @Test fun selectedPlayersNeverWanderOffToOtherFloorsOrReturnHomeFromAParty() {
        val world = WorldFactory.create()
        val p = world.people().first { it.species == Species.FOLK }
        Players.toggle(world, p)
        Players.arrive(world, PlaceId.MANOR_UPPER, 1f)
        repeat(100) { House.shuffle(world, PlaceId.MANOR_GROUND, Random(it)) }
        assertEquals(PlaceId.MANOR_UPPER, p.place)
        world.mine.guests.add(Guest(p.id, PlaceId.HOME, 1f))
        House.moveTo(world, p, PlaceId.MINE_YARD, 1f)
        Mine.returnGuests(world)
        assertEquals(PlaceId.MINE_YARD, p.place)
    }

    @Test fun takingAStairBringsBothPlayersEvenIfOneIsFarAwayOrSeated() {
        val world = WorldFactory.create()
        val people = world.people().filter { it.species == Species.FOLK }.take(2)
        people.forEach { Players.toggle(world, it) }
        House.moveTo(world, people[0], PlaceId.MANOR_GROUND, 10f)
        people[1].mode = Mode.SEATED
        val passage = House.passages.first { it.place == PlaceId.MANOR_GROUND && it.to == PlaceId.MANOR_UPPER && it.locked == null }
        val fixture = world.fixtures[WorldFactory.fixtureId(passage.place, passage.fixture)]!!
        HouseRules(Sim(world), Random(1)).usePassage(passage, fixture)
        people.forEach { assertEquals(PlaceId.MANOR_UPPER, it.place); assertEquals(Mode.FREE, it.mode) }
        assertTrue(people[0].x != people[1].x)
    }
}
