package app.trollfoss.domain

import kotlin.math.abs
import org.junit.Assert.*
import org.junit.Test

class ChristmasArrivalTest {
    private val home = PlaceId.CHRISTMAS_HOME
    private val stable = PlaceId.CHRISTMAS_STABLE

    private fun guests(world: World, count: Int): List<Person> = List(count) {
        world.addPerson(Species.FOLK, Look(), 1f, PlaceId.FARM, 1f, PlaceId.FARM.floor).also { p ->
            Players.toggle(world, p)
        }
    }

    @Test fun visitingASelectedSeatedSantaKeepsHisSeatAndTheGuestsBelongings() {
        val world = WorldFactory.create()
        world.playerIds.clear()
        val santa = world.people().single { it.name == "Julenissen" }
        val chair = world.fixturesIn(home).first { it.type == FixtureType.XM_CHAIR }
        assertTrue(Sim(world).seat(santa, chair, 0))
        val seatX = santa.x
        val seatY = santa.y
        Players.toggle(world, santa)
        val child = guests(world, 1).single().apply { scale = 2.6f }
        val hat = world.addThing(ThingType.XM_HAT, 0, child.place, child.x, child.y).apply {
            mode = Mode.WORN
            holder = child.id
            slot = Slot.HEAD.ordinal
        }
        val target = ChristmasPlaces.visitTarget(world, home)!!
        assertSame(santa, target.resident)
        assertEquals(seatX, target.x, 0f)

        Players.arrive(world, home, target.x, visit = target)

        assertEquals(home, child.place)
        assertEquals(seatX, santa.x, 0f)
        assertEquals(seatY, santa.y, 0f)
        assertEquals(Mode.SEATED, santa.mode)
        assertEquals(chair.id, santa.holder)
        assertTrue(abs(child.x - santa.x) > .4f * (child.h + santa.h))
        assertEquals(home, hat.place)
        assertEquals(child.id, hat.holder)
    }

    @Test fun twentyFiveActiveGuestsKeepEvenAMovedReindeerAtEitherWallFullyInTheClear() {
        val world = World()
        val deer = world.addPerson(Species.REINDEER, Look(), 1f, stable, .1f, stable.floor)
        val visitors = guests(world, 25)
        visitors.first().scale = 2.6f
        val paused = guests(world, 1).single()
        Players.pack(world, paused)

        for (x in listOf(.03f, .56f, 4f, stable.width - .56f, stable.width - .03f)) {
            deer.x = x
            val target = ChristmasPlaces.visitTarget(world, stable)!!
            assertEquals(x, target.x, 0f)
            Players.arrive(world, stable, target.x, visit = target)
            for (guest in visitors) {
                assertEquals(stable, guest.place)
                assertTrue(guest.x - guest.w / 2f >= 0f && guest.x + guest.w / 2f <= stable.width)
                assertTrue(abs(guest.x - deer.x) > .4f * (guest.h + deer.h))
            }
            if (x == 4f) {
                assertTrue(visitors.any { it.x < x })
                assertTrue(visitors.any { it.x > x })
            }
            assertEquals(x, deer.x, 0f)
            assertEquals(Mode.BAG, paused.mode)
            assertNull(paused.place)
        }
    }

    @Test fun movedWorkbenchStaysExposedWithVisitorsOnBothSides() {
        val world = WorldFactory.create()
        world.playerIds.clear()
        val room = PlaceId.CHRISTMAS_WORKSHOP
        val bench = world.fixturesIn(room).first { it.type == FixtureType.XM_TOY_BENCH }
        bench.x = 6.15f
        val visitors = guests(world, 3)
        val target = ChristmasPlaces.visitTarget(world, room)!!
        assertEquals(bench.x, target.x, 0f)
        Players.arrive(world, room, target.x, visit = target)
        for (guest in visitors) {
            assertEquals(room, guest.place)
            assertTrue(abs(guest.x - bench.x) > bench.spec.w / 2f + guest.w / 2f)
        }
        assertTrue(visitors.any { it.x < bench.x } && visitors.any { it.x > bench.x })
    }

    @Test fun santaOnlyReturnsFromAnotherPlaceWhenHeTravelsWithTheTeam() {
        val world = WorldFactory.create()
        world.playerIds.clear()
        val santa = world.people().single { it.name == "Julenissen" }
        House.moveTo(world, santa, PlaceId.FARM, 5f)
        val guest = guests(world, 1).single()
        val fireplace = world.fixturesIn(home).first { it.type == FixtureType.XM_FIREPLACE }
        val emptyHome = ChristmasPlaces.visitTarget(world, home)!!
        assertEquals(fireplace.x, emptyHome.x, 0f)
        Players.arrive(world, home, emptyHome.x, visit = emptyHome)
        assertEquals(PlaceId.FARM, santa.place)
        assertEquals(5f, santa.x, 0f)

        Players.toggle(world, santa)
        val returning = ChristmasPlaces.visitTarget(world, home)!!
        Players.arrive(world, home, returning.x, visit = returning)
        assertEquals(home, santa.place)
        assertEquals(returning.x, santa.x, .001f)
        assertTrue(abs(guest.x - santa.x) > .4f * (guest.h + santa.h))
    }
}
