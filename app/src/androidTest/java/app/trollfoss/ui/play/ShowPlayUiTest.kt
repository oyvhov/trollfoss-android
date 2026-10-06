package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Level 8 toys with real fingers on the engine: dancing on the dance floor and the light rig's disco. */
@RunWith(AndroidJUnit4::class)
class ShowPlayUiTest {
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
    }

    private fun engine(w: World, s: Sim, place: PlaceId) = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
    private fun at(e: Engine, x: Float, y: Float) = Offset((x - e.cam) * e.u, 1200f - e.u + y * e.u)
    private fun drag(e: Engine, from: Offset, to: Offset) {
        e.down(1, from, 1000)
        e.move(1, Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f), 1050)
        repeat(3) { e.update(0.016f) }
        e.move(1, to, 1100)
        repeat(3) { e.update(0.016f) }
        e.up(1, to, 1200)
    }

    @Test fun aFriendDraggedOntoTheDanceFloorDancesWhenTheMusicIsOn() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val floor = s.designer.add(place, FixtureType.PLAY_DANCE_FLOOR, 0, 1.4f, place.floor)!!
        val p = w.addPerson(Species.FOLK, Look(), 1f, place, floor.x - 0.6f, place.floor)
        val e = engine(w, s, place)
        repeat(20) { e.update(0.016f) }
        s.edit { s.tap(place, floor, 0f, -0.02f) }
        drag(e, at(e, p.x, p.y - p.h / 2f), at(e, floor.x, floor.y - p.h / 2f))
        repeat(90) { e.update(1f / 60f) }
        assertEquals(Mode.FREE, p.mode)
        assertTrue("the friend is on the floor", s.show.onDanceFloor(p))
        assertNotEquals("and dances", 0f, p.anim.dance)
        s.edit { s.tap(place, floor, 0f, -0.02f) }
        repeat(10) { e.update(1f / 60f) }
        assertEquals("music off, dancing stops", 0f, p.anim.dance)
    }

    @Test fun theLightRigsDiscoMakesEveryoneStandingDance() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val rig = s.designer.add(place, FixtureType.PLAY_LIGHT_RIG, 0, 1.2f, place.floor)!!
        val p = w.addPerson(Species.FOLK, Look(), 1f, place, 2.2f, place.floor)
        val e = engine(w, s, place)
        repeat(20) { e.update(0.016f) }
        s.edit { s.tap(place, rig, 0f, -0.3f); s.tap(place, rig, 0f, -0.3f) }
        assertEquals(2, rig.mode)
        repeat(30) { e.update(1f / 60f) }
        assertNotEquals("disco from the rig", 0f, p.anim.dance)
    }
}
