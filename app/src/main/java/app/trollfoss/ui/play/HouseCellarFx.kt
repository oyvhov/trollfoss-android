package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.CellarCode
import app.trollfoss.domain.CellarTunes
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Mode
import app.trollfoss.domain.Person
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.pow

/**
 * Storhuset, cellar floor: effects. [code] is one of [CellarCode] (the cellar's block of [app.trollfoss.domain.HouseFx]),
 * [arg] is the small number the rule sent along. Sound and sparkle only: what happens to things and figures is
 * decided by the rules in `domain/HouseCellar*.kt`.
 */
object CellarFx {
    private val steam = Color(0xFFF4F8FF)
    private val sawdust = Color(0xFFE8C98A)
    private val gold = Color(0xFFFFD447)
    private val fur = Color(0xFFB9A2F0)
    private val water = Color(0xFF9ADAFF)
    private val neonColors = listOf(Color(0xFFFF4DA6), Color(0xFF2FD6C8), Color(0xFFFFC83D), Color(0xFF8B5CF6), Color(0xFF4D96FF))

    private fun rate(semitones: Int): Float = 2f.pow(semitones / 12f)

    fun play(s: FxStage, code: Int, arg: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        when (code) {
            CellarCode.SWITCH -> {
                s.sfx(Sfx.CLICK, 0.6f, if (arg == 1) 1.25f else 0.8f)
                if (arg == 1) s.burst(PKind.SPARK, x, y, 4, 0.18f, 0.008f, Color(0xFFFFE08A))
            }
            // ---- workshop
            CellarCode.CHARGE -> {
                s.sfx(Sfx.CE_ZAP, if (arg == 2) 0.45f else 0.7f)
                s.burst(PKind.SPARK, x, y, if (arg == 2) 5 else 12, 0.35f, 0.01f, Color(0xFF7CFFB2))
                if (arg == 1) s.after(0.5f) { s.sfx(Sfx.BEEP, 0.5f, 1.3f) }
            }
            CellarCode.CHARGED -> {
                s.sfx(Sfx.DING, 0.8f)
                s.after(0.15f) { s.sfx(Sfx.SPARKLE, 0.6f) }
                s.burst(PKind.STAR, x, y, 10, 0.5f, 0.012f, Color(0xFF7CFFB2))
                s.laughAround(x, null, 0.5f, 0.8f)
            }
            CellarCode.MOUSE_PEEK -> {
                s.sfx(Sfx.SQUEAK, 0.55f, 1.7f)
                s.burst(PKind.DUST, x, y, 3, 0.12f, 0.008f, up = 0.05f)
            }
            CellarCode.MOUSE_CHEESE -> {
                s.sfx(Sfx.CHOMP, 0.6f, 1.6f)
                for (i in 0 until 3) s.after(0.2f + i * 0.14f) { s.sfx(Sfx.SQUEAK, 0.45f, 1.5f + i * 0.2f) }
                s.burst(PKind.HEART, x, y - 0.03f, 4, 0.15f, 0.012f, up = 0.25f)
                if (arg == 1) {
                    s.after(0.5f) {
                        s.sfx(Sfx.COIN, 0.8f)
                        s.burst(PKind.STAR, x + 0.05f, y, 6, 0.3f, 0.01f, gold)
                    }
                }
            }
            CellarCode.SAW_START -> {
                s.sfx(Sfx.CE_RASP, 0.7f)
                for (i in 0 until 4) s.after(0.1f + i * 0.2f) { s.burst(PKind.CRUMB, x, y + 0.02f, 5, 0.25f, 0.006f, sawdust, up = 0.1f) }
            }
            CellarCode.SAW_DONE -> {
                s.sfx(Sfx.THUD, 0.6f, 1.2f)
                s.sfx(Sfx.POP, 0.5f, 0.9f)
                s.burst(PKind.CRUMB, x, y, 12, 0.4f, 0.007f, sawdust, up = 0.15f)
                s.laughAround(x, null, 0.6f, 0.7f)
            }
            // ---- laundry
            CellarCode.WASH -> wash(s, arg, x, y)
            CellarCode.WASH_DONE -> {
                s.sfx(Sfx.DING, 0.7f, 1.1f)
                s.sfx(Sfx.OPEN, 0.5f)
                s.burst(PKind.BUBBLE, x, y - 0.05f, 12, 0.3f, 0.014f, Color.White, up = 0.25f, life = 1.4f)
                s.after(0.2f) { s.sfx(Sfx.POP, 0.5f, 1.2f) }
                if (arg > 0) s.laughAround(x, null, 0.6f, 0.7f)
            }
            CellarCode.DRYER_LOST -> {
                s.sfx(Sfx.GULP, 0.6f, 1.3f)
                s.after(0.3f) { s.sfx(Sfx.HMM, 0.5f, 1.3f) }
                s.burst(PKind.DUST, x, y, 5, 0.2f, 0.01f, Color.White, up = 0.1f)
            }
            CellarCode.SOCK_EAT -> {
                s.sfx(Sfx.CHOMP, 0.9f, 0.8f)
                s.after(0.25f) { s.sfx(Sfx.GULP, 0.7f, 0.7f) }
                s.burst(PKind.CRUMB, x, y, 8, 0.25f, 0.007f, fur, up = 0.1f)
            }
            CellarCode.SOCK_BURP -> {
                s.sfx(Sfx.BURP, 0.85f, 0.85f + s.random.nextFloat() * 0.3f)
                s.burst(PKind.BUBBLE, x, y - 0.02f, 5, 0.2f, 0.014f, fur, up = 0.2f, life = 1.2f)
                s.laughAround(x, null, 0.55f, 1.0f)
            }
            CellarCode.SOCK_KEY -> {
                s.sfx(Sfx.BURP, 0.9f, 0.7f)
                s.after(0.3f) { s.sfx(Sfx.COIN, 0.8f) }
                s.burst(PKind.STAR, x, y, 18, 0.55f, 0.013f, gold, up = 0.35f)
                s.burst(PKind.BUBBLE, x, y, 6, 0.25f, 0.016f, fur, up = 0.25f, life = 1.3f)
                s.laughAround(x, null, 0.6f, 1.4f)
                s.haptic()
            }
            CellarCode.SOCK_BLEH -> {
                s.sfx(Sfx.OOF, 0.7f, 0.6f)
                s.after(0.15f) { s.sfx(Sfx.BLOOP, 0.5f, 1.2f) }
                s.burst(PKind.DROP, x, y, 5, 0.3f, 0.01f, Color(0xFF8EDB6B), up = 0.3f)
            }
            CellarCode.SOCK_PLAY -> {
                if (arg >= 3) {
                    s.sfx(Sfx.TICKLE, 0.8f, 0.8f)
                    s.burst(PKind.HEART, x, y, 3, 0.15f, 0.012f, up = 0.25f)
                } else if (arg == 2) {
                    s.sfx(Sfx.BURP, 0.45f, 1.4f)
                } else {
                    s.sfx(Sfx.HMM, 0.6f, 0.7f)
                }
            }
            CellarCode.BASKET_POP -> {
                s.sfx(Sfx.POP, 0.6f, 1.1f)
                s.sfx(Sfx.SWISH, 0.3f, 1.4f)
                s.burst(PKind.LEAF, x, y, 4, 0.2f, 0.012f, Color.White, up = 0.25f, life = 1.2f)
            }
            CellarCode.LINE_SWING -> {
                s.sfx(Sfx.SWISH, 0.5f, 1.2f)
                if (arg == 1) s.after(0.2f) { s.sfx(Sfx.POP, 0.5f, 1.3f) }
            }
            CellarCode.IRON -> {
                s.sfx(Sfx.SIZZLE, 0.6f, 1.2f)
                s.sfx(Sfx.CE_HISS, 0.3f)
                s.burst(PKind.STEAM, x, y, 8, 0.12f, 0.02f, steam, up = 0.15f, life = 1.3f)
            }
            CellarCode.CHUTE -> {
                s.sfx(Sfx.CE_CLANG, 0.6f, 0.7f)
                if (arg == 0) {
                    s.sfx(Sfx.WHOOSH, 0.5f, 1.3f)
                    s.after(0.35f) { s.sfx(Sfx.THUD, 0.5f, 0.8f) }
                }
                s.burst(PKind.DUST, x, y, 6, 0.25f, 0.012f, up = 0.05f)
            }
            // ---- boiler room
            CellarCode.VALVE -> valve(s, arg, x, y)
            CellarCode.VALVES_ALL -> {
                s.haptic()
                for ((i, semi) in intArrayOf(0, 4, 7).withIndex()) s.after(0.2f + i * 0.4f) { s.sfx(Sfx.CE_WHISTLE, 0.8f, rate(semi)) }
                s.burst(PKind.STEAM, x, y - 0.1f, 14, 0.2f, 0.03f, steam, up = 0.25f, life = 1.6f)
                s.burst(PKind.STAR, x, y - 0.2f, 10, 0.4f, 0.012f, gold)
            }
            CellarCode.RELEASE -> {
                s.sfx(Sfx.CE_HISS, 1f)
                s.sfx(Sfx.CE_WHISTLE, 0.7f, 0.8f)
                s.burst(PKind.STEAM, x, y, 22, 0.35f, 0.035f, steam, up = 0.3f, life = 1.8f)
                s.laughAround(x, null, 0.5f, 1.6f)
            }
            CellarCode.BOILER_TAP -> {
                s.sfx(Sfx.CE_CLANG, 0.7f, 0.6f)
                s.after(0.1f) { s.sfx(Sfx.CE_HISS, 0.4f) }
                s.burst(PKind.STEAM, x, y - 0.22f, 5, 0.1f, 0.022f, steam, up = 0.2f, life = 1.3f)
            }
            // ---- pool and sauna
            CellarCode.DIVE -> {
                s.sfx(Sfx.BOING, 0.8f, 1.5f)
                s.sfx(Sfx.CE_CREAK, 0.3f, 1.4f)
                if (arg > 0) {
                    s.after(0.25f) { s.sfx(Sfx.WHOOSH, 0.5f, 1.4f) }
                    s.laughAround(x, null, 0.7f, 1.2f)
                }
            }
            CellarCode.SLIDE_GO -> {
                s.sfx(Sfx.WHOOSH, 0.8f, 1.1f)
                s.after(0.3f) { s.sfx(Sfx.SWISH, 0.5f, 1.4f) }
                s.burst(PKind.DROP, x, y, 6, 0.3f, 0.009f, water, up = 0.1f)
            }
            CellarCode.SPLASH_TAP -> {
                s.sfx(Sfx.SPLASH, 0.5f, 1.4f)
                s.sfx(Sfx.BUBBLE, 0.5f, 1.2f)
                repeat(8) { s.particle(Particle(PKind.DROP, x + (s.random.nextFloat() - 0.5f) * 0.1f, y, (s.random.nextFloat() - 0.5f) * 0.4f, -0.45f - s.random.nextFloat() * 0.3f, 0.8f, 0.01f, water)) }
            }
            CellarCode.FLOAT_PUSH -> {
                s.sfx(Sfx.SQUEAK, 0.7f, 1.1f)
                s.burst(PKind.BUBBLE, x, y + 0.04f, 4, 0.12f, 0.01f, Color.White, up = 0.1f)
            }
            CellarCode.SHOWER -> shower(s, arg, x, y)
            CellarCode.LADLE -> {
                s.sfx(Sfx.SPLASH, 0.4f, 0.7f)
                s.after(0.12f) { s.sfx(Sfx.CE_HISS, 0.7f) }
                s.burst(PKind.STEAM, x, y, 14, 0.18f, 0.03f, steam, up = 0.25f, life = 1.8f)
                // Whoever sits in the sauna goes «aaah».
                fixture?.let { sauna ->
                    for (i in 0 until sauna.spec.spots.size) s.world.seatedAt(sauna, i)?.let { p -> s.faces(p, Face.OOH, 0.5f, Face.YUM, 2f) }
                }
            }
            CellarCode.LIFEBUOY -> {
                s.sfx(Sfx.BOING, 0.5f, 1.6f)
                s.sfx(Sfx.TICK, 0.4f, 0.8f)
            }
            // ---- party room
            CellarCode.JUKE_START -> {
                s.sfx(Sfx.CHIME, 0.7f, 0.9f + arg * 0.06f)
                s.burst(PKind.NOTE, x, y, 6, 0.3f, 0.013f, null, up = 0.3f, life = 1.4f)
            }
            CellarCode.JUKE_STOP -> {
                s.sfx(Sfx.CLICK, 0.6f, 0.7f)
                s.sfx(Sfx.WHIRR, 0.3f, 1.6f)
            }
            CellarCode.JUKE_NOTE -> note(s, arg, x, y)
            CellarCode.FLOOR_TAP -> {
                for (i in 0 until 6) s.after(i * 0.07f) { s.sfx(Sfx.NOTE, 0.5f, rate(CellarTunes.SCALE[3 + i])) }
                s.burst(PKind.STAR, x, y, 8, 0.4f, 0.011f, null, up = 0.25f)
            }
            CellarCode.KARAOKE -> {
                if (arg == 1) {
                    s.sfx(Sfx.BEEP, 0.6f, 1.2f)
                    s.after(0.12f) { s.sfx(Sfx.NOTE, 0.5f, 1.25f) }
                } else {
                    s.sfx(Sfx.CLICK, 0.5f, 0.8f)
                }
            }
            CellarCode.BAR_POP -> {
                when (arg) {
                    0 -> {
                        s.sfx(Sfx.POP, 0.7f, 1.3f)
                        s.burst(PKind.CRUMB, x, y, 8, 0.3f, 0.01f, Color(0xFFFFF4C2), up = 0.3f)
                    }
                    1 -> {
                        s.sfx(Sfx.POP, 0.7f, 0.8f)
                        s.sfx(Sfx.SPRAY, 0.4f, 1.3f)
                        s.burst(PKind.BUBBLE, x, y, 7, 0.25f, 0.01f, Color.White, up = 0.3f, life = 1.1f)
                    }
                    else -> {
                        s.sfx(Sfx.POP, 0.7f, 1.0f)
                        s.burst(PKind.SPARK, x, y, 6, 0.3f, 0.01f, null, up = 0.2f)
                    }
                }
            }
            CellarCode.CONFETTI -> {
                s.sfx(Sfx.CE_BANG, 1f)
                s.after(0.1f) { s.sfx(Sfx.SPARKLE, 0.6f) }
                s.burst(PKind.CONFETTI, x, y, 44, 1f, 0.013f, null, up = 1.0f, life = 2.4f)
                s.burst(PKind.STAR, x, y, 10, 0.5f, 0.012f, gold, up = 0.6f)
                s.haptic()
                s.laughAround(x, null, 0.5f, 1.4f)
            }
            // ---- the tunnel
            CellarCode.RIDE_BELL -> {
                s.sfx(Sfx.CE_BELL, 0.8f)
                if (arg == 0) s.after(0.3f) { s.sfx(Sfx.BONK, 0.4f, 0.7f) }
            }
            CellarCode.RIDE_START -> {
                s.sfx(Sfx.CE_BELL, 0.9f)
                s.sfx(Sfx.RUMBLE, 0.5f, 1.4f)
                s.after(0.3f) { s.sfx(Sfx.OPEN, 0.6f, 0.6f) }
                s.burst(PKind.DUST, x, y + 0.05f, 10, 0.3f, 0.014f, up = 0.1f)
                riders(s, fixture) { p -> s.faces(p, Face.OOH, 0.8f, Face.LAUGH, 3f) }
            }
            CellarCode.RIDE_CLACK -> {
                s.sfx(Sfx.CE_CLACK, 0.55f, 0.85f + arg * 0.12f)
                s.burst(PKind.SPARK, x, y, 2, 0.2f, 0.008f, Color(0xFFFFC96B), up = 0.1f)
                if (arg >= 2) riders(s, fixture) { p -> if (s.random.nextFloat() < 0.35f) s.voice(p, Sfx.GIGGLE, 0.5f) }
            }
            CellarCode.RIDE_ENTER -> {
                s.sfx(Sfx.WHOOSH, 0.9f, 0.8f)
                s.sfx(Sfx.RUMBLE, 0.6f, 1.6f)
                s.haptic()
                riders(s, fixture) { p ->
                    s.faces(p, Face.WOW, 0.6f, Face.LAUGH, 2f)
                    s.voice(p, Sfx.GIGGLE, 0.7f)
                }
            }
            CellarCode.KNOCK -> {
                s.sfx(Sfx.BONK, 0.6f, 1.5f - 0.15f * arg.coerceIn(1, 3))
                s.burst(PKind.DUST, x, y, 4, 0.15f, 0.01f, up = 0.05f)
                if (arg >= 3) s.after(0.3f) { s.sfx(Sfx.CE_CLANG, 0.5f, 1.4f) }
            }
            CellarCode.RIDE_BACK -> {
                for (i in 0 until 3) s.after(i * 0.16f) { s.sfx(Sfx.CE_CLACK, 0.5f, 1.1f - i * 0.05f) }
                s.sfx(Sfx.CE_BELL, 0.6f, 1.1f)
                s.sfx(Sfx.WHOOSH, 0.5f, 1.2f)
            }
            else -> Unit
        }
    }

    /** The figures sitting in the mine cart. */
    private fun riders(s: FxStage, cart: Fixture?, block: (Person) -> Unit) {
        if (cart == null) return
        for (i in cart.spec.spots.indices) s.world.seatedAt(cart, i)?.let(block)
    }

    private fun wash(s: FxStage, arg: Int, x: Float, y: Float) {
        val dryer = arg == 1 || arg == 3
        val loop = arg >= 2
        s.sfx(Sfx.CE_THRUM, if (loop) 0.5f else 0.75f, if (dryer) 1.25f else 1f)
        if (dryer) {
            s.burst(PKind.STEAM, x, y - 0.12f, 3, 0.1f, 0.016f, Color(0xFFFFE6C2), up = 0.1f, life = 1.2f)
        } else {
            s.burst(PKind.BUBBLE, x, y - 0.1f, if (loop) 2 else 4, 0.15f, 0.012f, Color.White, up = 0.15f, life = 1.2f)
            if (!loop) s.sfx(Sfx.BUBBLE, 0.4f, 0.9f)
        }
    }

    private fun valve(s: FxStage, arg: Int, x: Float, y: Float) {
        val which = arg / 2
        val open = arg % 2 == 1
        val pitch = floatArrayOf(0.8f, 1f, 1.25f)[which.coerceIn(0, 2)]
        s.sfx(Sfx.CE_CREAK, 0.5f, pitch)
        s.after(0.18f) { s.sfx(Sfx.CE_CLANG, 0.8f, pitch) }
        if (open) {
            s.after(0.25f) { s.sfx(Sfx.CE_HISS, 0.6f, 1f + which * 0.1f) }
            s.burst(PKind.STEAM, x, y - 0.1f, 8, 0.15f, 0.022f, steam, up = 0.2f, life = 1.5f)
        } else {
            s.burst(PKind.STEAM, x, y - 0.1f, 3, 0.1f, 0.016f, steam, up = 0.1f, life = 1f)
        }
    }

    private fun shower(s: FxStage, arg: Int, x: Float, y: Float) {
        if (arg == 0) {
            s.sfx(Sfx.CLICK, 0.5f, 0.8f)
            return
        }
        s.sfx(Sfx.SPLASH, 0.5f, 1.5f)
        s.sfx(Sfx.CE_HISS, 0.4f, 1f)
        for (i in 0 until 5) {
            s.after(i * 0.15f) {
                repeat(5) { s.particle(Particle(PKind.DROP, x + (s.random.nextFloat() - 0.5f) * 0.1f, y - 0.1f, (s.random.nextFloat() - 0.5f) * 0.05f, 0.6f, 0.5f, 0.008f, water)) }
            }
        }
        s.burst(PKind.STEAM, x, y + 0.1f, 6, 0.1f, 0.025f, steam, up = 0.15f, life = 1.6f)
        // Whoever stands under it gets a happy shiver.
        for (b in s.world.bodiesIn(s.place)) {
            if (b is Person && b.mode == Mode.FREE && !b.held && abs(b.x - x) < 0.16f && b.anim.pose == Pose.STAND) s.faces(b, Face.OOH, 0.4f, Face.LAUGH, 1.6f)
        }
    }

    /** One step of the jukebox's tune: melody, bass and drums. [arg] is the tune times 64 plus the step. */
    private fun note(s: FxStage, arg: Int, x: Float, y: Float) {
        val tune = (arg / 64).coerceIn(0, CellarTunes.COUNT - 1)
        val step = arg % 64
        val melody = CellarTunes.melody(tune, step)
        if (melody >= 0) {
            val box = tune == 3
            s.sfx(Sfx.NOTE, if (box) 0.5f else 0.55f, (rate(CellarTunes.SCALE[melody]) * (if (box) 1.5f else 1f)).coerceAtMost(2f))
            if (step % 4 == 0) {
                s.particle(Particle(PKind.NOTE, x + (s.random.nextFloat() - 0.5f) * 0.12f, y, (s.random.nextFloat() - 0.5f) * 0.1f, -0.14f, 1.5f, 0.013f, neonColors[(step / 4 + tune) % neonColors.size]))
            }
        }
        val bass = CellarTunes.bass(tune, step)
        if (bass != CellarTunes.NO_BASS) s.sfx(Sfx.CE_BASS, 0.6f, rate(bass).coerceIn(0.5f, 1f))
        val drum = CellarTunes.drum(tune, step)
        if (drum and CellarTunes.KICK != 0) s.sfx(Sfx.DRUM, 0.5f, 1f)
        if (drum and CellarTunes.SNARE != 0) s.sfx(Sfx.CE_HAT, 0.45f, 0.6f)
        if (drum and CellarTunes.HAT != 0) s.sfx(Sfx.CE_HAT, 0.28f, 1.1f)
    }

}
