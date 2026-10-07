package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Level 9 toys with real fingers on the engine: the reaction pads take taps directly, the others open their dialog. */
@RunWith(AndroidJUnit4::class)
class InventorPlayUiTest {
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
    private var clock = 10_000L
    private fun tap(e: Engine, x: Float, y: Float) {
        val p = at(e, x, y)
        clock += 400; e.down(1, p, clock); repeat(2) { e.update(0.016f) }; e.up(1, p, clock + 60); repeat(3) { e.update(0.016f) }
    }

    @Test fun aTapOnALitPadScoresAndNeverOpensTheDialog() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val board = s.designer.add(place, FixtureType.PLAY_REACTION_COURSE, 0, 1.4f, place.floor)!!
        val e = engine(w, s, place)
        repeat(20) { e.update(0.016f) }
        tap(e, board.x, board.y - 0.03f)
        assertEquals("the board is tapped, not opened", -1, e.toyFixtureId)
        var guard = 0
        while (board.angle < 1f && guard++ < 600) e.update(0.016f)
        val pad = board.angle.toInt() - 1
        assertTrue("a pad lit up", pad in 0..3)
        tap(e, board.x + (pad - 1.5f) * InventorPlay.PAD, board.y - 0.03f)
        assertEquals("one right", 1f, board.angleV, 0f)
        assertEquals(-1, e.toyFixtureId)
    }

    @Test fun theOtherLevel9ToysOpenTheirDialogOnATap() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val types = listOf(FixtureType.PLAY_ROBOT_WORKSHOP, FixtureType.PLAY_HELPER_ROBOT, FixtureType.PLAY_ROCKET_KIT)
        val toys = types.mapIndexed { i, t -> s.designer.add(place, t, 0, 0.8f + i * 0.9f, place.floor)!! }
        val e = engine(w, s, place)
        repeat(20) { e.update(0.016f) }
        for (toy in toys) {
            tap(e, toy.x, toy.y - toy.spec.h / 2f)
            assertEquals("${toy.type} opens its dialog", toy.id, e.toyFixtureId)
            e.cancel()
        }
    }
}
