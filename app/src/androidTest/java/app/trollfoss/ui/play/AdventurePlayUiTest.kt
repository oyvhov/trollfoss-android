package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Level 7 toys and the peeking troll, played with real fingers on the engine. */
@RunWith(AndroidJUnit4::class)
class AdventurePlayUiTest {
    private val firsts = ArrayList<Triple<First, Float, Float>>()
    private val host = object : EngineHost {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id: String) {}
        override fun discovered(key: String) {}
        override fun telescope() {}
        override fun radio(on: Boolean) {}
        override fun egg(id: String) {}
        override fun passage(passage: Passage, arrivalX: Float) {}
        override fun first(first: First, x: Float, y: Float) { firsts += Triple(first, x, y) }
    }

    private fun engine(w: World, s: Sim, place: PlaceId) = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
    private fun at(e: Engine, x: Float, y: Float) = Offset((x - e.cam) * e.u, 1200f - e.u + y * e.u)
    private fun finger(e: Engine, p: Person) = at(e, p.x, p.y - p.h / 2f)
    private fun tap(e: Engine, where: Offset) { e.down(1, where, 1000); e.up(1, where, 1100) }
    private fun drag(e: Engine, from: Offset, to: Offset) {
        e.down(1, from, 1000)
        e.move(1, Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f), 1050)
        repeat(3) { e.update(0.016f) }
        e.move(1, to, 1100)
        repeat(3) { e.update(0.016f) }
        e.up(1, to, 1200)
    }

    @Test fun aFriendDraggedIntoTheCableCarRidesAcrossAndTheStickerStartsOnScreen() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val car = s.designer.add(place, FixtureType.PLAY_CABLE_CAR, 0, 1.6f, place.floor)!!
        // Close by, so the drag is a calm placing and not a throw.
        val p = w.addPerson(Species.FOLK, Look(), 1f, place, car.x - 0.6f, place.floor)
        val e = engine(w, s, place)
        repeat(20) { e.update(0.016f) }
        val seat = s.seatPoint(car, 0)
        drag(e, finger(e, p), at(e, seat[0], seat[1] + p.h * Anatomy.HIPS - p.h / 2f))
        assertEquals("a real drag seats the friend in the gondola", Mode.SEATED, p.mode)
        assertEquals(car.id, p.holder)
        tap(e, at(e, car.x + 0.4f, car.y - 0.3f))
        assertEquals("a tap on the toy opens its controls", car.id, e.toyFixtureId)
        e.toyFixtureId = -1
        val startX = p.x
        s.edit { s.tap(place, car, 0f, -0.2f) }
        repeat(240) { e.update(1f / 60f) }
        assertTrue(car.angle > 0.95f)
        assertTrue("the rider rides along", p.x > startX + 0.5f)
        val sticker = firsts.single { it.first == First.CABLE_CAR }
        assertFalse("the sticker starts where the cable car is", sticker.second.isNaN() || sticker.third.isNaN())
    }

    @Test fun theDiggerDrivesAndDigsTreasureOnTheBeach() {
        val w = World(); val s = Sim(w); val place = PlaceId.BEACH
        val digger = s.designer.add(place, FixtureType.PLAY_DIGGER, 0, 1.2f, place.floor)!!
        val e = engine(w, s, place)
        repeat(20) { e.update(0.016f) }
        tap(e, at(e, digger.x - 0.1f, digger.y - 0.2f))
        assertSame("vehicles skip the toy dialog and take the arrows", digger, e.vehicle)
        val before = w.bodies.size
        e.dig()
        assertEquals(before + 1, w.bodies.size)
        assertTrue(First.DIGGER.name in w.firsts)
        e.closeDriving()
    }

    @Test fun aPeekingTrollCanBeCaughtWithATap() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val wardrobe = s.designer.add(place, FixtureType.WARDROBE, 0, 1.4f, place.floor)!!
        val e = engine(w, s, place)
        repeat(5) { e.update(0.016f) }
        s.mischief.force(MischiefKind.TROLL, place)
        assertNotNull(s.mischief.troll)
        tap(e, at(e, wardrobe.x + wardrobe.spec.w * 0.42f, wardrobe.y - wardrobe.spec.h * 0.62f))
        assertNull("the troll pops away", s.mischief.troll)
        assertTrue(First.PEEK_TROLL.name in w.firsts)
    }
}
