package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class RobotVacuumTest {
    private fun advance(s: Sim, seconds: Float) = repeat((seconds * 60).toInt()) { s.step(PlaceId.HOME, 1f / 60f) }

    @Test fun aCatOrPersonRidesTurnsAndStopsWithTheirClothesAndCarriedThing() {
        for (species in listOf(Species.CAT, Species.FOLK)) {
            val w = World(); val s = Sim(w); val place = PlaceId.HOME
            val vacuum = s.designer.add(place, FixtureType.ROBOT_VACUUM, 0, 0.7f, place.floor)!!
            val look = Look(top = 3, topColor = 5)
            val rider = w.addPerson(species, look, 1f, place, vacuum.x, vacuum.y)
            val originalLook = rider.look
            val ball = w.addThing(ThingType.BALL, 2, place, rider.x, rider.y)
            assertEquals(Give.HELD, s.give(rider, ball, Part.HAND))
            assertTrue(s.seat(rider, vacuum, 0))
            val other = w.addPerson(Species.CAT, Look(), 1f, place, vacuum.x, vacuum.y)
            assertFalse("only one rider", s.seat(other, vacuum, 0))
            assertTrue("sitting starts the ride", vacuum.on)
            advance(s, 1f)
            assertTrue(vacuum.shiftX > 0.1f)
            assertEquals(s.seatPoint(vacuum, 0)[0], rider.x, 0.001f)
            assertEquals(s.seatPoint(vacuum, 0)[1], rider.y, 0.001f)
            var turned = false
            repeat(360) {
                s.step(place, 1f / 60f)
                turned = turned || vacuum.angleV < 0f
                assertEquals(s.seatPoint(vacuum, 0)[0], rider.x, 0.001f)
            }
            assertTrue("turns at the room edge", turned)
            advance(s, 8f)
            assertFalse(vacuum.on)
            val stopped = rider.x
            advance(s, 1f)
            assertEquals(stopped, rider.x, 0.001f)
            assertEquals(Mode.SEATED, rider.mode)
            assertSame(originalLook, rider.look)
            assertEquals(Mode.WORN, ball.mode)
            assertEquals(rider.id, ball.holder)
        }
    }

    @Test fun aRideTurnsAtASofaIgnoresRugsAndPausesWhenLifted() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val vacuum = s.designer.add(place, FixtureType.ROBOT_VACUUM, 0, 0.7f, place.floor)!!
        s.designer.add(place, FixtureType.RUG, 0, 0.9f, place.floor)
        val sofa = s.designer.add(place, FixtureType.SOFA, 0, 1.3f, place.floor)!!
        val cat = w.addPerson(Species.CAT, Look(), 1f, place, vacuum.x, vacuum.y)
        assertTrue(s.seat(cat, vacuum, 0)); assertTrue(vacuum.on)
        advance(s, 2f)
        assertTrue(vacuum.angleV < 0f)
        assertTrue(vacuum.x + vacuum.shiftX + vacuum.spec.w / 2f <= sofa.x - sofa.spec.w / 2f)
        vacuum.lift = 0.1f
        val x = cat.x; val time = vacuum.timer
        advance(s, 1f)
        assertEquals(x, cat.x, 0.001f); assertEquals(time, vacuum.timer, 0.001f)
        vacuum.lift = 0f; advance(s, 0.2f)
        assertTrue(cat.x < x)
    }

    @Test fun savingAndPuttingAwayARideKeepsTheSameRiderAndTheirBall() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val vacuum = s.designer.add(place, FixtureType.ROBOT_VACUUM, 0, 0.7f, place.floor)!!
        val rider = w.addPerson(Species.FOLK, Look(top = 4), 1f, place, vacuum.x, vacuum.y)
        val ball = w.addThing(ThingType.BALL, 0, place, rider.x, rider.y)
        s.give(rider, ball, Part.HAND); s.seat(rider, vacuum, 0)
        advance(s, 1f)
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val restarted = Sim(loaded); restarted.settle(place)
        val saved = loaded.fixtures.getValue(vacuum.id)
        val passenger = loaded.bodies.getValue(rider.id) as Person
        assertEquals(vacuum.shiftX, saved.shiftX, 0.001f)
        assertEquals(rider.x, passenger.x, 0.001f)
        assertEquals(Mode.SEATED, passenger.mode); assertFalse(saved.on)
        assertEquals(4, passenger.look.top)
        assertEquals(passenger.id, loaded.bodies.getValue(ball.id).holder)
        assertTrue(restarted.designer.store(place, saved))
        advance(restarted, 0.5f)
        assertSame(passenger, loaded.bodies[rider.id]); assertEquals(Mode.FREE, passenger.mode)
        assertEquals(Mode.WORN, loaded.bodies.getValue(ball.id).mode)
        val unpacked = restarted.designer.unstore(place, 0, 0.8f, place.floor)!!
        assertTrue(restarted.seat(passenger, unpacked, 0))
        assertTrue(unpacked.on); advance(restarted, 0.5f)
        assertEquals(restarted.seatPoint(unpacked, 0)[0], passenger.x, 0.001f)
    }
}
