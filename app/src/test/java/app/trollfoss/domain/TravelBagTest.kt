package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class TravelBagTest {
    @Test fun aReturningLegacyItemKeepsItsPlaceWithoutAdmittingNewItems() {
        val w = World()
        repeat(36) { w.addThing(ThingType.COIN, 0, null, 0f, 0f).mode = Mode.BAG }
        val original = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1f, .9f)
        assertFalse(TravelBag.canPack(w, original))
        assertTrue(TravelBag.canPack(w, original, returning = true))
    }
    @Test fun anItemBeingDraggedFromTheBagReservesItsPlace() {
        val w = World()
        repeat(11) { w.addThing(ThingType.COIN, 0, null, 0f, 0f).mode = Mode.BAG }
        val ball = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1f, .9f)
        assertTrue(TravelBag.canPack(w, ball))
        assertFalse(TravelBag.canPack(w, ball, reserved = 1))
    }

    @Test fun fullBagRefusesNewThingsAndFiguresWithoutChangingThem() {
        val w = World()
        repeat(12) { w.addThing(ThingType.GEM, it % 5, null, 0f, 0f).mode = Mode.BAG }
        val ball = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1f, .9f)
        assertFalse(TravelBag.canPack(w, ball))
        val person = WorldFactory.create().people().first()
        assertFalse(TravelBag.canPack(w, person))
        assertEquals(PlaceId.HOME, ball.place)
        assertEquals(Mode.FREE, ball.mode)
        w.bag().first().mode = Mode.FREE
        assertTrue(TravelBag.canPack(w, ball))
    }
    @Test fun allLegacyContentsRemainReachableOnLargePages() {
        val w = World()
        repeat(37) { w.addThing(ThingType.COIN, 0, null, 0f, 0f).apply { mode = Mode.BAG; z = it.toLong() } }
        val original = w.bag()
        val paged = (0 until TravelBag.pages(original.size)).flatMap { TravelBag.page(original, it) }
        assertEquals(original.map { it.id }, paged.map { it.id })
        assertTrue((0 until TravelBag.pages(original.size)).all { TravelBag.page(original, it).size <= 6 })
        assertEquals(37, w.bag().size)
    }
}
