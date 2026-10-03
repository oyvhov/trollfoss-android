package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class PlayerFollowTest {
    private val place = PlaceId.MANOR_GROUND
    private fun player(w: World, x: Float) = w.addPerson(Species.FOLK, Look(), 1f, place, x, place.floor).also { w.playerIds += it.id }
    private fun frames(s: Sim, n: Int, camera: Float, dt: Float = 0.016f, check: () -> Unit = {}) {
        repeat(n) { s.playerFollow.view(camera, 2.35f); s.step(place, dt); check() }
    }

    @Test fun bothPlayersWalkWithBoundedStepsAndKeepTheirOwnClothesAndCarriedThings() {
        val w = World(); val a = player(w, 0.7f); val b = player(w, 1.1f)
        val hat = w.addThing(ThingType.PARTY_HAT, 2, place, a.x, a.y).apply { mode = Mode.WORN; holder = a.id; slot = Slot.HEAD.ordinal }
        val toy = w.addThing(ThingType.TEDDY, 0, place, b.x, b.y).apply { mode = Mode.WORN; holder = b.id; slot = Slot.HAND.ordinal }
        val s = Sim(w); s.settle(place); val count = w.bodies.size
        s.playerFollow.navigate(place, 2f, 2.35f)
        assertEquals(0.7f, a.x, 0f) // Starting a journey must never move a figure.
        var ax = a.x; var bx = b.x; var walked = false
        frames(s, 220, 2f) {
            assertTrue(abs(a.x - ax) <= PlayerFollow.MAX_SPEED * 0.016f + 0.0001f)
            assertTrue(abs(b.x - bx) <= PlayerFollow.MAX_SPEED * 0.016f + 0.0001f)
            walked = walked || a.anim.following && a.anim.walkPhase > 0f
            ax = a.x; bx = b.x
        }
        assertTrue(walked); assertTrue(a.x > 2.3f); assertTrue(b.x - a.x >= 0.25f)
        assertSame(a, w.bodies[a.id]); assertSame(b, w.bodies[b.id]); assertEquals(count, w.bodies.size)
        assertSame(hat, w.worn(a, Slot.HEAD)); assertSame(toy, w.worn(b, Slot.HAND))
        assertEquals(place, hat.place); assertEquals(place, toy.place)
        assertFalse(a.anim.following); assertFalse(b.anim.following)
        val saved = WorldStore.decode(WorldStore.encode(w, Settings())).world
        assertEquals(a.x, saved.bodies[a.id]!!.x, 0.0001f)
        assertEquals(hat.id, saved.worn(saved.bodies[a.id] as Person, Slot.HEAD)!!.id)
        assertEquals(toy.id, saved.worn(saved.bodies[b.id] as Person, Slot.HAND)!!.id)
    }

    @Test fun oneChildHoldingTheirFigureDoesNotPreventTheOtherFromFollowing() {
        val w = World(); val a = player(w, 0.7f); val b = player(w, 1.1f); val s = Sim(w)
        a.held = true; s.playerFollow.navigate(place, 2f, 2.35f)
        frames(s, 220, 2f)
        assertEquals(0.7f, a.x, 0f); assertTrue(a.held); assertTrue(b.x > 2.3f)
        a.held = false
        frames(s, 30, 2f)
        assertTrue(a.x < 1f) // Dropping is the child's choice, not a delayed group teleport.
        s.playerFollow.navigate(place, 3f, 2.35f); frames(s, 220, 3f)
        assertTrue(a.x > 3.3f)
    }

    @Test fun aSmallSwipeLeavesComfortablyVisiblePlayersWhereTheChildPlacedThem() {
        val w = World(); val a = player(w, 0.9f); val b = player(w, 1.4f); val s = Sim(w)
        s.playerFollow.navigate(place, 0.06f, 2.35f); frames(s, 45, 0.06f)
        assertEquals(0.9f, a.x, 0f); assertEquals(1.4f, b.x, 0f)
    }

    @Test fun changingDirectionBrakesBeforeTurningAndNeverSnapsAtTheEndOfTheSwipe() {
        val w = World(); val p = player(w, 1.2f); val s = Sim(w)
        s.playerFollow.navigate(place, 3f, 2.35f); frames(s, 60, 3f)
        val before = p.x
        s.playerFollow.navigate(place, 0f, 2.35f)
        s.step(place, 0.016f)
        assertTrue(p.x >= before) // Momentum is braked gradually.
        var last = p.x
        frames(s, 180, 0f) {
            assertTrue(abs(p.x - last) <= PlayerFollow.MAX_SPEED * 0.016f + 0.0001f)
            last = p.x
        }
        assertTrue(p.x < before)
    }

    @Test fun bedsAndVehiclePassengersAreNotPulledOutByTheOtherChildExploring() {
        val w = World(); val sleeping = player(w, 0.5f); val riding = player(w, 1.2f); val free = player(w, 1.8f)
        val s = Sim(w)
        val bed = s.designer.add(place, FixtureType.BED, 0, 0.5f, place.floor)!!
        val bus = s.designer.add(place, FixtureType.PLAY_BUS, 0, 1.3f, place.floor)!!
        assertTrue(s.seat(sleeping, bed, 0)); assertTrue(s.seat(riding, bus, 0))
        s.playerFollow.navigate(place, 4f, 2.35f); frames(s, 220, 4f)
        assertEquals(Mode.SEATED, sleeping.mode); assertEquals(bed.id, sleeping.holder)
        assertEquals(Mode.SEATED, riding.mode); assertEquals(bus.id, riding.holder)
        assertTrue(free.x > 4.3f)
    }

    @Test fun unfinishedRoomsCannotBecomeAnInvisibleTeleportOrWalkingShortcut() {
        val w = World(); w.mine.started = true
        w.mine.ground[0] = 1; w.mine.ground[2] = RoomKind.KITCHEN.ordinal + 1
        val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.MINE_GROUND, 1f, 0.9f); w.playerIds += p.id
        val s = Sim(w); s.playerFollow.navigate(PlaceId.MINE_GROUND, 4f, 2.35f)
        repeat(200) { s.step(PlaceId.MINE_GROUND, 0.016f) }
        assertEquals(1f, p.x, 0f); assertFalse(p.anim.following)
    }

    @Test fun cancellingOrLeavingThePlaceStopsTheJourneyWithoutMovingAnyBody() {
        val w = World(); val p = player(w, 0.7f); val s = Sim(w)
        s.playerFollow.navigate(place, 2f, 2.35f); frames(s, 20, 2f)
        val x = p.x; s.playerFollow.cancel()
        assertFalse(p.anim.following); frames(s, 20, 2f); assertEquals(x, p.x, 0f)
        s.playerFollow.navigate(place, 2f, 2.35f); House.moveTo(w, p, PlaceId.CAFE, 1f)
        frames(s, 20, 2f); assertEquals(PlaceId.CAFE, p.place); assertEquals(1f, p.x, 0f)
    }

    @Test fun selectedFiguresKeepTheirSpacingAfterTheWalkAndWalkInFrontOfOrdinaryFurniture() {
        val w = World(); val a = player(w, 0.7f); val b = player(w, 1.1f); val s = Sim(w)
        s.settle(place); s.playerFollow.navigate(place, 2f, 2.35f)
        var depth = a.y
        frames(s, 220, 2f) {
            assertTrue(a.y-depth >= -0.0001f); assertTrue(a.y-depth <= 0.12f*0.016f+0.0001f)
            depth = a.y
        }
        assertTrue(a.y > 0.95f); assertTrue(b.y > 0.95f)
        val ax=a.x; val bx=b.x
        a.anim.nextWish = 1000f; b.anim.nextWish = 1000f
        frames(s, 1200, 2f)
        assertEquals(ax,a.x,0f); assertEquals(bx,b.x,0f)
        assertTrue(b.x-a.x >= 0.25f)
        // An unselected local friend still has their ordinary little life.
        w.playerIds.remove(a.id); a.anim.nextWalk=0f; a.anim.still=Life.SETTLE_FOLK
        frames(s, 600, 2f)
        assertTrue(a.x != ax || a.y != depth)
    }
}
