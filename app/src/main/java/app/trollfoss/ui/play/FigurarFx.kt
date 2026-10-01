package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Anatomy
import app.trollfoss.domain.Face
import app.trollfoss.domain.FigurarEvent
import app.trollfoss.domain.FigurarPose
import app.trollfoss.domain.Give
import app.trollfoss.domain.Part
import app.trollfoss.domain.Person
import app.trollfoss.domain.Species
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.sin

/**
 * The sound and sparkle of Rolf the robot butler and Sture the ghost: their voices, Rolf's bows, Sture's
 * dusty sneezes, and what they make of food. The rules are in `domain/Figurar.kt`, the pictures in
 * `ui/art/PersonArtHouse.kt`.
 */
object FigurarFx {
    /** Old attic dust. */
    val DUST_COLOR = Color(0xFFE6DCC6)

    /** Sture's gentle bob in the air, in scene units (negative is up); [h] is his height. */
    fun hover(time: Float, id: Int, h: Float): Float = sin(time * 1.7f + id) * 0.04f * h

    /** What Rolf says when the game asks a figure for [sfx]: his beeps and bops. */
    fun robotVoice(sfx: Sfx): Sfx = when (sfx) {
        Sfx.GIGGLE, Sfx.TICKLE -> Sfx.FG_ROLF_LAUGH
        Sfx.BABBLE, Sfx.HMM -> Sfx.FG_ROLF_TALK
        Sfx.OOH -> Sfx.FG_ROLF_OOH
        Sfx.OOF -> Sfx.FG_ROLF_OOF
        Sfx.YUM -> Sfx.FG_ROLF_HAPPY
        else -> Sfx.BEEP
    }

    /** What Sture says: a shy giggle, a soft hum and a wobbly «ooh». */
    fun ghostVoice(sfx: Sfx): Sfx = when (sfx) {
        Sfx.GIGGLE, Sfx.TICKLE, Sfx.YUM -> Sfx.FG_STURE_GIGGLE
        Sfx.BABBLE, Sfx.HMM -> Sfx.FG_STURE_HUM
        else -> Sfx.FG_STURE_OOH
    }

    /** Plays [FigurarEvent]s: sounds only when the figure is near the middle of the view. */
    fun play(s: FxStage, param: Int, x: Float, y: Float) {
        val p = s.person(FigurarEvent.id(param)) ?: return
        val near = abs(x - s.centerX) < 1.5f
        when (FigurarEvent.code(param)) {
            FigurarEvent.BOW -> {
                if (near) s.sfx(Sfx.FG_ROLF_BOW, 0.55f)
                // Whoever is near smiles back.
                for (o in s.world.bodiesIn(s.place)) {
                    if (o is Person && o.species == Species.FOLK && !o.held && o.anim.face != Face.SLEEP && abs(o.x - p.x) < 0.7f) {
                        s.faces(o, Face.GRIN, 0.9f, Face.HAPPY, 0.1f)
                    }
                }
            }
            FigurarEvent.ACHOO -> if (near) s.voice(p, Sfx.FG_STURE_SNEEZE, 0.8f, own = true)
            FigurarEvent.DUST -> {
                val nose = Anatomy.at(p, Part.MOUTH)
                s.burst(PKind.DUST, nose[0], nose[1], 18, 0.55f, 0.011f, DUST_COLOR, up = 0.1f, life = 1.1f)
                s.burst(PKind.DUST, nose[0], nose[1] + 0.02f, 8, 0.25f, 0.016f, DUST_COLOR, up = 0.05f, life = 1.5f)
                p.squashV -= 7f
                p.anim.hopV = 1.2f
                s.faces(p, Face.OOH, 0.3f, Face.LAUGH, 1.1f)
                s.after(0.5f) { if (!p.held && near) s.voice(p, Sfx.GIGGLE, 0.6f) }
                s.laughAround(p.x, p, 0.6f, 0.7f)
            }
        }
    }

    /** Rolf and Sture have their own reactions to food. Returns true when this handled [result]. */
    fun gift(s: FxStage, p: Person, t: Thing, result: Give): Boolean = when {
        p.species == Species.ROBOT && (result == Give.ATE || result == Give.DRANK || result == Give.FINISHED) -> {
            fuel(s, p)
            true
        }
        p.species == Species.GHOST && result == Give.SNIFF -> {
            sniff(s, p)
            true
        }
        else -> false
    }

    /**
     * «Fuel!» Rolf swallows it whole, glugs, whirrs, puffs steam out of his antenna, sees stars for a moment,
     * and then bows politely, delighted, with a ding and a heart.
     */
    private fun fuel(s: FxStage, p: Person) {
        val top = Anatomy.at(p, Part.HAT)
        s.sfx(Sfx.FG_ROLF_FUEL, 0.85f)
        s.faces(p, Face.CHOMP, 0.5f, Face.DIZZY, 1.0f)
        p.squashV += 4f
        s.after(0.5f) {
            s.burst(PKind.STEAM, p.x, top[1] - p.h * 0.08f, 6, 0.1f, 0.02f, Color.White, up = 0.35f, life = 1.2f)
            s.burst(PKind.SPARK, p.x, p.y - p.h * 0.3f, 8, 0.45f, 0.01f, Color(0xFFFFC83D))
            p.anim.hopV = 1.4f
        }
        s.after(1.5f) {
            if (!p.held) {
                s.faces(p, Face.GRIN, 1.3f, Face.HAPPY, 0.1f)
                p.anim.wave = FigurarPose.BOW_SECONDS
                val head = Anatomy.at(p, Part.HEAD)
                s.burst(PKind.HEART, head[0], head[1] - p.h * 0.3f, 3, 0.2f, 0.016f, up = 0.3f, life = 1.4f)
                s.laughAround(p.x, p, 0.3f, 0.9f)
            }
        }
        s.haptic()
    }

    /** «Hm?» Sture sniffs the food twice, puzzled, tilts his head, and giggles at himself. */
    private fun sniff(s: FxStage, p: Person) {
        s.sfx(Sfx.FG_STURE_SNIFF, 0.8f)
        s.after(0.3f) { s.sfx(Sfx.FG_STURE_SNIFF, 0.7f, 1.12f) }
        s.faces(p, Face.OOH, 0.8f, Face.LAUGH, 0.9f)
        p.anim.tilt = 12f * p.anim.facing
        s.after(0.85f) {
            if (!p.held) {
                s.voice(p, Sfx.GIGGLE, 0.7f)
                val head = Anatomy.at(p, Part.HEAD)
                s.burst(PKind.HEART, head[0], head[1] - p.h * 0.3f, 2, 0.15f, 0.014f, Color(0xFFFFA3BE), up = 0.3f, life = 1.2f)
            }
        }
    }
}
