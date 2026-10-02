package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class VehiclesTest {
    private fun advance(s: Sim, place: PlaceId, seconds: Float) {
        repeat((seconds * 60).toInt()) { s.step(place, 1f / 60f) }
    }

    @Test fun vehiclesTravelWithPassengersReverseStopAndKeepTheirPositionAfterReload() {
        for (type in listOf(FixtureType.TRACTOR, FixtureType.BUMPER_CAR, FixtureType.BOAT, FixtureType.SUBMARINE)) {
            val w = World(); val s = Sim(w); val place = PlaceId.CAFE
            val f = s.designer.add(place, type, 0, 0.6f, place.floor)!!
            val rider = w.addPerson(Species.FOLK, Look(), 1f, place, f.x, f.y)
            assertTrue(s.seat(rider, f, 0))
            val before = f.x
            s.tap(place, f, 0f, 0f)
            advance(s, place, 2f)
            assertTrue("$type must travel", f.x > before + 0.6f)
            assertEquals(s.seatPoint(f, 0)[0], rider.x, 0.001f)
            val right = f.x
            s.vehicles.drive(f, -1); advance(s, place, 0.5f)
            assertTrue(f.x < right)
            s.vehicles.drive(f, 0)
            val stopped = f.x
            advance(s, place, 1f)
            assertEquals(stopped, f.x, 0.001f)
            s.vehicles.drive(f, 1)
            val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
            val saved = loaded.fixtures[f.id]!!
            val restarted = Sim(loaded); restarted.settle(place)
            assertEquals(stopped, saved.x, 0.001f)
            assertFalse(saved.on)
            assertEquals(Mode.SEATED, loaded.bodies[rider.id]!!.mode)
            restarted.vehicles.drive(saved, -1); advance(restarted, place, 0.5f)
            assertTrue(saved.x < stopped)
        }
    }

    @Test fun boatStaysOnTheWaterAndCanBeStoredAndSailedAgain() {
        val w = WorldFactory.create(); val s = Sim(w); val place = PlaceId.BEACH
        val boat = w.fixturesIn(place).first { it.type == FixtureType.BOAT }
        val water = Places.spec(place).water!!
        s.vehicles.drive(boat, -1); advance(s, place, 6f)
        assertTrue(boat.x - boat.spec.w / 2f >= water.x1 - 0.001f)
        assertFalse(boat.on)
        val left = boat.x
        s.vehicles.drive(boat, 1); advance(s, place, 6f)
        assertTrue(boat.x > left)
        assertTrue(boat.x + boat.spec.w / 2f <= water.x2 + 0.001f)
        assertFalse(boat.on)
        assertTrue(s.designer.store(place, boat))
        val moved = s.designer.unstore(PlaceId.FOREST, w.storage.indexOfFirst { it.type == FixtureType.BOAT }, 1f, 0.9f)!!
        val start = moved.x
        s.vehicles.drive(moved, 1); advance(s, PlaceId.FOREST, 0.5f)
        assertTrue(moved.x > start)
        assertEquals(Places.spec(PlaceId.FOREST).water!!.line + 0.02f, moved.y, 0.001f)
    }

    @Test fun submarineRisesDivesAndStopsAtBothHeightLimitsWithItsTwoPassengers() {
        val w = World(); val s = Sim(w); val place = PlaceId.UNDERWATER
        val sub = s.designer.add(place, FixtureType.SUBMARINE, 0, 1f, place.floor)!!
        val riders = (0..1).map { i ->
            w.addPerson(Species.FOLK, Look(), 1f, place, sub.x, sub.y).also { assertTrue(s.seat(it, sub, i)) }
        }
        s.vehicles.dive(sub, -1); advance(s, place, 3f)
        assertEquals(place.floor - 0.3f, sub.y, 0.001f)
        assertFalse(sub.on)
        riders.forEachIndexed { i, p -> assertEquals(s.seatPoint(sub, i)[1], p.y, 0.001f) }
        val saved = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val loadedSub = saved.fixtures[sub.id]!!
        val restarted = Sim(saved); restarted.settle(place)
        assertEquals(sub.y, loadedSub.y, 0.001f)
        restarted.vehicles.dive(loadedSub, 1); advance(restarted, place, 4f)
        assertEquals(PlaceId.FRONT, loadedSub.y, 0.001f)
        assertFalse(loadedSub.on)
    }

    @Test fun bumperCarBouncesOffAnotherCarWithoutLosingPeopleOrThings() {
        val w = World(); val events = mutableListOf<Fx>()
        val s = Sim(w, object : SimListener {
            override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) { events += fx }
        })
        val place = PlaceId.TIVOLI
        val car = s.designer.add(place, FixtureType.BUMPER_CAR, 0, 0.8f, place.floor)!!
        val parked = s.designer.add(place, FixtureType.BUMPER_CAR, 1, 1.3f, place.floor)!!
        val rider = w.addPerson(Species.FOLK, Look(), 1f, place, car.x, car.y)
        s.seat(rider, car, 0)
        s.vehicles.drive(car, 1); advance(s, place, 0.8f)
        assertEquals(-1, car.mode)
        assertTrue(Fx.BUMP in events)
        assertEquals(Mode.SEATED, rider.mode)
        assertSame(parked, w.fixtures[parked.id])
        assertEquals(1, w.bodies.size)
        assertEquals(2, w.fixtures.size)
    }

    @Test fun bumperCarCanReverseOutOfFurnitureItWasPlacedInside() {
        val w = World(); val s = Sim(w); val place = PlaceId.BEACH
        val car = s.designer.add(place, FixtureType.BUMPER_CAR, 0, 0.8f, place.floor)!!
        s.designer.add(place, FixtureType.SANDCASTLE, 0, 0.84f, place.floor)
        s.vehicles.drive(car, 1); advance(s, place, 0.5f)
        assertEquals(-1, car.mode)
        assertTrue(car.x < 0.7f)
    }
}
