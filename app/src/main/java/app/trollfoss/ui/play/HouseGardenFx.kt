package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.GardenFxCode
import app.trollfoss.domain.Person
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.pow

/**
 * Storhuset, garden floor: effects. Each event of [GardenFxCode] becomes sound and sparkle: the frogs croak
 * in their own notes, plants shoot up in a rain of leaves, the gnomes wink, the compost burps. [code] is in
 * the garden's block of [app.trollfoss.domain.HouseFx], [arg] is the small number the rule sent along.
 */
object GardenFx {
    /** The frogs' notes in semitones: a major pentatonic, deepest frog first. */
    private val FROG_SEMI = intArrayOf(-9, -7, -5, -2, 0)

    /** The notes of the bird house tune (semitones above the chirp). */
    private val BIRD_SEMI = intArrayOf(0, 2, 4, 7, 9, 12, 9, 7)

    private val FROG_COLORS = intArrayOf(0xFF3BC46B.toInt(), 0xFF2F9BFF.toInt(), 0xFFFFC83D.toInt(), 0xFFFF4D6D.toInt(), 0xFFB38BFF.toInt())
    private val LEAF = Color(0xFF6FD25A)
    private val SAND = Color(0xFFE8C98A)
    private val SOIL = Color(0xFF7A5438)
    private val WATER = Color(0xFF9ADAFF)
    private val GOLD = Color(0xFFFFD447)

    private var lastWhirr = -9f

    private fun rate(semi: Int): Float = 2f.pow(semi / 12f)

    /** Louder close to the middle of the screen, never silent: distant frogs and birds are part of the garden. */
    private fun near(s: FxStage, x: Float, base: Float): Float = base * (1f - (abs(x - s.centerX) / 7f).coerceAtMost(0.75f))

    private fun rider(s: FxStage, fixture: Fixture?, spot: Int = 0): Person? = fixture?.let { s.world.seatedAt(it, spot) }

    fun play(s: FxStage, code: Int, arg: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        when (code) {
            GardenFxCode.CROAK -> croak(s, arg, x, y, 0.8f)
            GardenFxCode.FROG_AMBIENT -> {
                croak(s, arg, x, y, near(s, x, 0.42f))
                // Sometimes a second one answers.
                if (arg % 2 == 0) s.after(0.45f) { croak(s, (arg + 2) % 5, x, y, near(s, x, 0.32f)) }
            }
            GardenFxCode.CHOIR -> {
                s.burst(PKind.NOTE, x, y, 16, 0.55f, 0.017f, null, up = 0.5f, life = 2.2f)
                s.after(1.4f) { s.sfx(Sfx.SPARKLE, 0.7f, 1f) }
                s.after(2.9f) {
                    s.sfx(Sfx.CHIME, 0.7f, 1f)
                    s.burst(PKind.HEART, x, y, 8, 0.4f, 0.014f, null, up = 0.4f, life = 1.5f)
                }
                s.laughAround(x, null, 1.0f, 2.2f)
                s.haptic()
            }
            GardenFxCode.SOW -> {
                s.sfx(Sfx.POP, 0.6f, 0.9f)
                s.burst(PKind.DUST, x, y + 0.04f, 6, 0.25f, 0.01f, SOIL, up = 0.2f, life = 0.6f)
            }
            GardenFxCode.WATER -> {
                s.sfx(Sfx.GA_SPRAY, 0.5f, 1.2f)
                s.burst(PKind.DROP, x, y - 0.26f, 7, 0.1f, 0.009f, WATER, up = -0.5f, life = 0.7f)
            }
            GardenFxCode.GROW -> {
                s.sfx(Sfx.GA_GROW, 0.75f, 0.85f + 0.12f * arg)
                s.burst(PKind.LEAF, x, y - 0.05f * arg, 4 + arg * 3, 0.3f, 0.014f, LEAF, up = 0.35f, life = 1.0f)
                s.burst(PKind.SPARK, x, y - 0.06f, 4, 0.25f, 0.009f, Color(0xFFD9FF9A), up = 0.2f)
            }
            GardenFxCode.GIANT -> {
                s.sfx(Sfx.CHIME, 0.85f, 0.9f)
                s.after(0.25f) { s.sfx(Sfx.DING, 0.6f, 0.8f) }
                s.burst(PKind.STAR, x, y - 0.05f, 14, 0.55f, 0.014f, GOLD, up = 0.4f, life = 1.3f)
                s.burst(PKind.LEAF, x, y - 0.1f, 8, 0.35f, 0.015f, LEAF, up = 0.3f, life = 1.2f)
                s.laughAround(x, null, 0.7f, 1.4f)
                s.haptic()
            }
            GardenFxCode.HARVEST -> {
                for (i in 0 until 3) s.after(i * 0.12f) { s.sfx(Sfx.POP, 0.7f, 0.9f + i * 0.2f) }
                s.burst(PKind.STAR, x, y - 0.1f, 8, 0.4f, 0.012f, GOLD, up = 0.3f)
                s.burst(PKind.LEAF, x, y - 0.1f, 6, 0.3f, 0.013f, LEAF, up = 0.2f)
            }
            GardenFxCode.MIST -> {
                s.sfx(Sfx.GA_SPRAY, 0.8f, 1f)
                s.sfx(Sfx.WHIRR, 0.25f, 1.6f)
                repeat(10) { i ->
                    val px = x + (i - 4.5f) * 0.085f
                    s.burst(PKind.STEAM, px, y - 0.2f, 1, 0.05f, 0.03f, Color.White, up = 0.05f, life = 1.4f)
                    s.burst(PKind.DROP, px, y - 0.32f, 1, 0.05f, 0.009f, WATER, up = -0.5f, life = 0.8f)
                }
            }
            GardenFxCode.WOBBLE -> {
                s.sfx(Sfx.BOING, 0.6f, 1.1f)
                rider(s, fixture)?.let { p ->
                    s.faces(p, Face.LAUGH, 1.2f, Face.HAPPY, 0.1f)
                    s.voice(p, Sfx.GIGGLE, 0.8f)
                }
            }
            GardenFxCode.WINK -> {
                s.sfx(Sfx.GA_WINK, 0.7f, 0.9f + 0.07f * arg)
                s.burst(PKind.STAR, x, y - 0.02f, 4, 0.22f, 0.01f, Color(0xFFFFE680), up = 0.15f, life = 0.7f)
            }
            GardenFxCode.POOF -> {
                s.sfx(Sfx.POOF, 0.55f, 1.5f)
                s.burst(PKind.SMOKE, x, y, 7, 0.25f, 0.025f, Color.White, up = 0.1f, life = 0.9f)
                s.burst(PKind.STAR, x, y, 5, 0.3f, 0.01f, Color(0xFFFFE680), up = 0.15f)
            }
            GardenFxCode.DIG -> {
                s.sfx(Sfx.GA_DIG, 0.7f, 0.9f + 0.1f * (arg % 3))
                s.burst(PKind.DUST, x, y - 0.04f, 8, 0.3f, 0.011f, SAND, up = 0.3f, life = 0.7f)
                if (arg == 1) {
                    s.after(0.45f) {
                        s.sfx(Sfx.COIN, 0.7f, 1f)
                        s.burst(PKind.STAR, x, y - 0.1f, 8, 0.4f, 0.012f, GOLD, up = 0.3f)
                    }
                }
                if (arg == 2) s.after(0.4f) { s.sfx(Sfx.POP, 0.6f, 0.9f) }
            }
            GardenFxCode.SWING -> {
                if (arg == 0) {
                    s.sfx(Sfx.GA_CREAK, 0.45f, 1.2f)
                    s.sfx(Sfx.SWISH, 0.4f, 1.2f)
                } else {
                    rider(s, fixture)?.let { p ->
                        s.voice(p, Sfx.GIGGLE, 0.8f)
                        s.faces(p, Face.LAUGH, 1.4f, Face.HAPPY, 0.1f)
                    }
                    s.burst(PKind.LEAF, x, y, 3, 0.2f, 0.012f, LEAF, up = 0.1f)
                }
            }
            GardenFxCode.ZIP_GO -> {
                s.sfx(Sfx.ZIP, 0.8f, 0.9f)
                s.after(0.25f) { s.sfx(Sfx.WHOOSH, 0.6f, 1.2f) }
                rider(s, fixture)?.let { p ->
                    s.faces(p, Face.OOH, 0.8f, Face.LAUGH, 1.8f)
                    s.voice(p, Sfx.OOH, 0.8f)
                    s.after(0.7f) { s.voice(p, Sfx.GIGGLE, 0.8f) }
                }
                s.haptic()
            }
            GardenFxCode.ZIP_END -> {
                s.sfx(Sfx.WHOOSH, 0.55f, 1.4f)
                if (arg == 1) {
                    s.after(0.35f) { s.sfx(Sfx.GIGGLE, 0.7f, 1.1f) }
                    s.laughAround(x - 0.5f, null, 0.9f, 1.6f)
                } else {
                    s.sfx(Sfx.CLICK, 0.5f, 0.8f)
                }
            }
            GardenFxCode.ZIP_BACK -> s.sfx(Sfx.BEEP, 0.4f, 0.8f)
            GardenFxCode.CLIMB -> {
                when (arg) {
                    0, 1 -> {
                        for (i in 0 until 6) s.after(i * 0.23f) { s.sfx(Sfx.TICK, 0.45f, 0.8f + i * 0.06f) }
                        s.sfx(Sfx.GA_CREAK, 0.3f, 1.4f)
                    }
                    2, 3 -> {
                        s.sfx(Sfx.POP, 0.5f, 1.2f)
                        s.burst(PKind.DUST, x, y + 0.1f, 4, 0.2f, 0.01f, up = 0.1f, life = 0.5f)
                    }
                    else -> s.sfx(Sfx.TICK, 0.3f, 0.6f)
                }
            }
            GardenFxCode.GRILL -> if (arg == 1) {
                s.sfx(Sfx.POOF, 0.5f, 1.3f)
                s.sfx(Sfx.SIZZLE, 0.4f, 1.2f)
                s.burst(PKind.SPARK, x, y - 0.05f, 6, 0.3f, 0.01f, Color(0xFFFFA23A), up = 0.4f)
                s.burst(PKind.SMOKE, x, y - 0.1f, 4, 0.1f, 0.025f, Color(0xFFB0B4C0), up = 0.15f, life = 1.2f)
            } else {
                s.sfx(Sfx.CLICK, 0.5f, 0.8f)
            }
            GardenFxCode.PARASOL -> s.sfx(if (arg == 1) Sfx.POP else Sfx.SWISH, 0.6f, 0.9f)
            GardenFxCode.SPRINKLER -> when (arg) {
                1 -> s.sfx(Sfx.GA_SPRAY, 0.8f, 1f)
                0 -> s.sfx(Sfx.CLICK, 0.5f, 0.8f)
                else -> {
                    s.sfx(Sfx.GIGGLE, 0.5f, 1.25f)
                    s.burst(PKind.DROP, x, y, 6, 0.3f, 0.009f, WATER, up = 0.4f, life = 0.7f)
                }
            }
            GardenFxCode.MOWER -> when (arg) {
                1 -> {
                    s.sfx(Sfx.VROOM, 0.45f, 1.7f)
                    s.after(0.3f) { s.sfx(Sfx.BEEP, 0.5f, 1.3f) }
                }
                0 -> {
                    s.sfx(Sfx.BEEP, 0.5f, 0.7f)
                    s.burst(PKind.NOTE, x, y - 0.1f, 1, 0.05f, 0.012f, Color(0xFFB9A2F0), up = 0.3f, life = 1.2f)
                }
                2 -> {
                    s.burst(PKind.LEAF, x - 0.05f, y + 0.02f, 3, 0.25f, 0.009f, LEAF, up = 0.25f, life = 0.6f)
                    if (s.time - lastWhirr > 1.0f) {
                        lastWhirr = s.time
                        s.sfx(Sfx.WHIRR, near(s, x, 0.22f), 1.7f)
                    }
                }
                else -> {
                    s.sfx(Sfx.BEEP, 0.55f, 1.6f)
                    s.sfx(Sfx.BOING, 0.35f, 1.3f)
                }
            }
            GardenFxCode.BIRD -> bird(s, arg, x, y)
            GardenFxCode.GATE -> if (arg == 1) {
                s.sfx(Sfx.GA_CREAK, 0.7f, 1.1f)
                s.after(0.45f) { s.sfx(Sfx.THUD, 0.4f, 1.5f) }
            } else {
                s.sfx(Sfx.GA_CREAK, 0.6f, 0.9f)
                s.after(0.3f) { s.sfx(Sfx.SHUT, 0.5f, 1f) }
            }
            GardenFxCode.COMPOST -> compost(s, arg, x, y)
            GardenFxCode.BARREL -> {
                s.sfx(Sfx.BLOOP, 0.45f, 0.9f + 0.15f * arg)
                s.burst(PKind.DROP, x, y - 0.05f, 5, 0.15f, 0.008f, WATER, up = 0.3f, life = 0.6f)
            }
            GardenFxCode.PINWHEEL -> s.sfx(Sfx.WHIRR, 0.35f, 1.8f)
            GardenFxCode.FLOWERS -> if (arg == 0) {
                s.sfx(Sfx.SPARKLE, 0.35f, 1.3f)
                s.burst(PKind.BUTTERFLY, x, y - 0.06f, 3, 0.18f, 0.014f, null, up = 0.3f, life = 3.0f)
            } else {
                s.sfx(Sfx.POP, 0.6f, 1.3f)
                s.burst(PKind.HEART, x, y - 0.1f, 3, 0.2f, 0.012f, null, up = 0.3f, life = 1.0f)
            }
            GardenFxCode.SHAKE_TREE -> {
                s.sfx(Sfx.SWISH, 0.6f, 0.9f)
                s.burst(PKind.LEAF, x, y - 0.55f, 10, 0.3f, 0.015f, LEAF, up = -0.1f, life = 1.6f)
                if (arg == 1) s.after(0.55f) { s.sfx(Sfx.THUD, 0.4f, 1.5f) }
            }
            GardenFxCode.HAMMOCK -> {
                s.sfx(Sfx.GA_CREAK, 0.4f, 0.8f)
                if (rider(s, fixture) != null) {
                    s.sfx(Sfx.SNORE, 0.3f, 1.2f)
                    s.burst(PKind.ZZZ, x, y - 0.15f, 2, 0.1f, 0.016f, null, up = 0.3f, life = 1.6f)
                }
            }
            GardenFxCode.BRIDGE -> {
                s.sfx(Sfx.GA_CREAK, 0.55f, 1.3f)
                s.sfx(Sfx.TICK, 0.4f, 0.7f)
            }
            GardenFxCode.SNOWMAN -> when (arg) {
                0 -> s.sfx(Sfx.GA_WINK, 0.6f, 0.8f)
                1 -> s.sfx(Sfx.POP, 0.6f, 1.2f)
                else -> {
                    s.sfx(Sfx.MAGIC, 0.5f, 1f)
                    s.burst(PKind.SNOW, x, y - 0.1f, 14, 0.3f, 0.012f, Color.White, up = 0.2f, life = 1.4f)
                }
            }
            GardenFxCode.CABIN -> s.sfx(if (arg == 1) Sfx.OPEN else Sfx.SHUT, 0.6f, 1.2f)
        }
    }

    private fun croak(s: FxStage, frog: Int, x: Float, y: Float, volume: Float) {
        val k = frog.coerceIn(0, FROG_SEMI.size - 1)
        s.sfx(Sfx.GA_CROAK, volume, rate(FROG_SEMI[k]))
        if (volume > 0.3f) {
            s.burst(PKind.NOTE, x, y - 0.05f, 1, 0.05f, 0.015f, Color(FROG_COLORS[k]), up = 0.3f, life = 1.3f)
            s.burst(PKind.BUBBLE, x, y, 2, 0.08f, 0.007f, Color.White, up = 0.1f, life = 0.8f)
        }
    }

    /** The bird house: a short tune that climbs, or a far-off chirp. */
    private fun bird(s: FxStage, arg: Int, x: Float, y: Float) {
        when (arg) {
            9 -> {
                s.sfx(Sfx.CHIRP, near(s, x, 0.3f), 1.15f)
                s.after(0.22f) { s.sfx(Sfx.CHIRP, near(s, x, 0.25f), 1.4f) }
            }
            7 -> for (i in 0 until BIRD_SEMI.size) s.after(i * 0.16f) {
                s.sfx(Sfx.CHIRP, 0.6f, rate(BIRD_SEMI[i]).coerceAtMost(2f))
                if (i % 2 == 0) s.burst(PKind.NOTE, x, y - 0.1f, 1, 0.05f, 0.013f, null, up = 0.3f, life = 1.2f)
            }
            else -> {
                val a = BIRD_SEMI[arg % BIRD_SEMI.size]
                s.sfx(Sfx.CHIRP, 0.7f, rate(a).coerceAtMost(2f))
                s.after(0.15f) { s.sfx(Sfx.CHIRP, 0.55f, rate(BIRD_SEMI[(arg + 2) % BIRD_SEMI.size]).coerceAtMost(2f)) }
                s.burst(PKind.NOTE, x, y - 0.15f, 2, 0.08f, 0.013f, null, up = 0.35f, life = 1.2f)
            }
        }
    }

    private fun compost(s: FxStage, arg: Int, x: Float, y: Float) {
        when (arg) {
            0 -> {
                s.sfx(Sfx.BLOOP, 0.5f, 0.7f)
                s.burst(PKind.SMOKE, x, y - 0.05f, 3, 0.08f, 0.02f, Color(0xFF8A7458), up = 0.15f, life = 1.0f)
            }
            1 -> {
                s.sfx(Sfx.CHOMP, 0.6f, 0.8f)
                s.after(0.25f) { s.sfx(Sfx.GULP, 0.5f, 0.7f) }
            }
            2 -> {
                s.sfx(Sfx.BURP, 0.95f, 1f)
                s.burst(PKind.SMOKE, x, y - 0.12f, 8, 0.2f, 0.03f, Color(0xFF9A8460), up = 0.2f, life = 1.2f)
                s.laughAround(x, null, 0.6f, 1.2f)
            }
            3 -> {
                s.sfx(Sfx.MAGIC, 0.8f, 1f)
                s.burst(PKind.STAR, x, y - 0.2f, 14, 0.6f, 0.014f, GOLD, up = 0.5f, life = 1.4f)
                s.haptic()
            }
            4 -> {
                s.sfx(Sfx.HMM, 0.6f, 0.8f)
                s.sfx(Sfx.POP, 0.4f, 0.8f)
            }
            else -> s.sfx(Sfx.POP, 0.5f, 1.1f)
        }
    }
}
