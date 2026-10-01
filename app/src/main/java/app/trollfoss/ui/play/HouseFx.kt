package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.HouseFx
import app.trollfoss.domain.PassageKind
import app.trollfoss.domain.Thing

/**
 * Turns the house's [app.trollfoss.domain.Fx.HOUSE] events into sound and sparkle. The code of an event
 * (see [HouseFx]) says which floor it came from; each floor has its own player in its own file.
 */
object HouseFxPlayer {
    fun play(s: FxStage, param: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        val code = HouseFx.code(param)
        val arg = HouseFx.arg(param)
        when (code) {
            HouseFx.PASSAGE -> passage(s, PassageKind.entries[arg.coerceIn(0, PassageKind.entries.size - 1)], x, y)
            HouseFx.LOCKED -> locked(s, x, y)

            in HouseFx.GROUND until HouseFx.UPPER -> GroundFx.play(s, code, arg, x, y, fixture, thing)

            in HouseFx.UPPER until HouseFx.ATTIC -> UpperFx.play(s, code, arg, x, y, fixture, thing)

            in HouseFx.ATTIC until HouseFx.CELLAR -> AtticFx.play(s, code, arg, x, y, fixture, thing)

            in HouseFx.CELLAR until HouseFx.GARDEN -> CellarFx.play(s, code, arg, x, y, fixture, thing)

            in HouseFx.GARDEN until HouseFx.STORY -> GardenFx.play(s, code, arg, x, y, fixture, thing)

            else -> Unit
        }
    }

    /** Taking stairs, a lift, a slide, a pole, a hatch or a secret door. */
    private fun passage(s: FxStage, kind: PassageKind, x: Float, y: Float) {
        when (kind) {
            PassageKind.STAIRS, PassageKind.LADDER -> {
                for (i in 0 until 4) s.after(i * 0.11f) { s.sfx(Sfx.TICK, 0.5f, 0.8f + i * 0.08f) }
            }
            PassageKind.LIFT -> {
                s.sfx(Sfx.DING, 0.7f, 1.2f)
                s.after(0.25f) { s.sfx(Sfx.WHIRR, 0.5f, 0.6f) }
            }
            PassageKind.SLIDE -> {
                s.sfx(Sfx.WHOOSH, 0.8f, 1.1f)
                s.after(0.3f) { s.sfx(Sfx.SWISH, 0.6f, 1.4f) }
            }
            PassageKind.POLE -> {
                s.sfx(Sfx.WHOOSH, 0.7f, 1.5f)
                s.after(0.2f) { s.sfx(Sfx.SWISH, 0.5f, 1.8f) }
            }
            PassageKind.HATCH -> {
                s.sfx(Sfx.OPEN, 0.6f, 0.8f)
                s.after(0.2f) { s.sfx(Sfx.THUD, 0.5f, 0.9f) }
            }
            PassageKind.SECRET -> {
                s.sfx(Sfx.OPEN, 0.7f, 0.55f)
                s.after(0.15f) { s.sfx(Sfx.MAGIC, 0.5f, 0.9f) }
                s.burst(PKind.SPARK, x, y, 8, 0.3f, 0.01f)
            }
            PassageKind.DOOR -> {
                s.sfx(Sfx.OPEN, 0.7f, 0.9f)
                s.after(0.25f) { s.sfx(Sfx.SHUT, 0.6f, 0.9f) }
            }
            PassageKind.DUMBWAITER -> {
                s.sfx(Sfx.CLICK, 0.6f, 0.8f)
                s.after(0.2f) { s.sfx(Sfx.WHIRR, 0.5f, 0.8f) }
            }
        }
        s.burst(PKind.DUST, x, y + 0.08f, 5, 0.25f, 0.012f, up = 0.1f)
    }

    /** A locked way: a rattle, and whoever is near shrugs. */
    private fun locked(s: FxStage, x: Float, y: Float) {
        s.sfx(Sfx.BONK, 0.5f, 1.4f)
        s.after(0.12f) { s.sfx(Sfx.CLICK, 0.6f, 0.7f) }
        for (b in s.world.bodiesIn(s.place)) {
            if (b is app.trollfoss.domain.Person && !b.held && kotlin.math.abs(b.x - x) < 0.7f) s.faces(b, Face.OOH, 0.4f, Face.GRIN, 0.8f)
        }
        s.burst(PKind.SPARK, x, y, 4, 0.2f, 0.008f, Color(0xFFFFC83D))
    }
}
