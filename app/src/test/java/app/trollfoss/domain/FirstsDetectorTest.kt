package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class FirstsDetectorTest {
    private fun fixture(type: FixtureType, place: PlaceId = PlaceId.FARM) = Sim(World()).designer.add(place, type, 0, 1.5f, place.floor)!!

    @Test fun vehiclesMapByType() {
        assertEquals(First.TRACTOR, FirstsDetector.of(Fx.VROOM, fixture(FixtureType.TRACTOR), null, 2))
        assertEquals(First.BUS, FirstsDetector.of(Fx.VROOM, fixture(FixtureType.PLAY_BUS), null, 2))
        assertEquals(First.BOAT, FirstsDetector.of(Fx.TOOT, fixture(FixtureType.BOAT, PlaceId.BEACH), null, 0))
        assertNull(FirstsDetector.of(Fx.VROOM, null, null, 0))
    }

    @Test fun pumpAndLiftAreToldApart() {
        assertEquals(First.PUMP, FirstsDetector.of(Fx.PUMP, fixture(FixtureType.PLAY_PUMP), null, 0))
        assertEquals(First.MINI_LIFT, FirstsDetector.of(Fx.PUMP, fixture(FixtureType.PLAY_LIFT), null, 0))
    }

    @Test fun relayAwardsOnRealPlay() {
        val s = Sim(World())
        val f = s.designer.add(PlaceId.FARM, FixtureType.TRACTOR, 0, 1.5f, PlaceId.FARM.floor)!!
        s.listener.onFx(Fx.VROOM, f.x, f.top, f, null, 2)
        assertTrue("TRACTOR" in s.world.firsts)
    }

    @Test fun eventsThatAlreadyGiveStickersAreNotFirsts() {
        assertNull(FirstsDetector.of(Fx.GIFT, null, null, 1))
        assertNull(FirstsDetector.of(Fx.HOUSE, null, null, 0))
    }

    @Test fun aChildPuttingAFriendToBedCountsAsSleep() {
        val s = Sim(World())
        val bed = s.designer.add(PlaceId.HOME, FixtureType.BED, 0, 1.5f, PlaceId.HOME.floor)!!
        val p = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.2f, PlaceId.HOME.floor)
        assertTrue(s.seatByChild(p, bed, 0))
        assertTrue("SLEEP" in s.world.firsts)
    }

    @Test fun villagersGoingToBedByThemselvesEarnNothing() {
        val s = Sim(World())
        val bed = s.designer.add(PlaceId.HOME, FixtureType.BED, 0, 1.5f, PlaceId.HOME.floor)!!
        val p = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.2f, PlaceId.HOME.floor)
        assertTrue(s.seat(p, bed, 0))
        assertTrue(s.world.firsts.isEmpty())
    }

    @Test fun friendsActionsCount() {
        val s = Sim(World())
        val a = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.0f, PlaceId.HOME.floor)
        val b = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.3f, PlaceId.HOME.floor)
        assertTrue(s.community.friend(a, b, FriendAction.HIGH_FIVE))
        assertTrue("HIGH_FIVE" in s.world.firsts)
    }

    @Test fun reachingANewPlaceByTravelCounts() {
        val s = Sim(World()); s.visit(PlaceId.HOME); s.visit(PlaceId.FARM)
        assertTrue("FIRST_TRIP" in s.world.firsts)
    }
}
