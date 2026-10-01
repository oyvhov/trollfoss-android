package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Palette
import app.trollfoss.domain.Person
import app.trollfoss.domain.Thing
import app.trollfoss.domain.UpperCodes
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

/**
 * Storhuset, upper floor: effects. [code] is in [app.trollfoss.domain.HouseFx].UPPER up to the next block (100 codes),
 * [arg] is the small number the rule sent along (see [UpperCodes]). Sound and sparkle only: the picture moves by itself.
 */
object UpperFx {
    /** The pentatonic scale of the house in semitones from the note a sound is made in, like the piano's. */
    private val SCALE = intArrayOf(-12, -10, -8, -5, -3, 0, 2, 4, 7, 9)

    private fun rate(note: Int): Float = 2f.pow(SCALE[note.coerceIn(0, SCALE.size - 1)] / 12f)

    private val paint = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF4D96FF),
        Color(0xFFB983FF), Color(0xFFFF9F43), Color(0xFFF08CB8), Color(0xFF3FC7C0),
    )

    /** A waltz for the ballerina's music box, as indices into [SCALE]. */
    private val WALTZ = intArrayOf(5, 7, 8, 7, 5, 3, 5, 7, 5, 3, 2, 0, 2, 3, 5, 3)

    fun play(s: FxStage, code: Int, arg: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        val c = code - app.trollfoss.domain.HouseFx.UPPER
        when (c) {
            UpperCodes.TRAIN_GO -> {
                s.sfx(Sfx.UP_WHISTLE, 0.8f, if (arg == 1) 1.15f else 1f)
                for (i in 0 until 3) s.after(0.55f + i * 0.28f) { s.sfx(Sfx.UP_CHUFF, 0.4f, 0.9f + i * 0.08f) }
                s.burst(PKind.STEAM, x, y - 0.1f, 6, 0.12f, 0.02f, Color.White, up = 0.25f, life = 1.3f)
                s.haptic()
            }
            UpperCodes.TRAIN_STOP -> if (arg == 1) {
                s.sfx(Sfx.DING, 0.7f, 1.5f)
                s.after(0.2f) { s.sfx(Sfx.UP_WHISTLE, 0.45f, 1.3f) }
                s.burst(PKind.STEAM, x, y - 0.05f, 8, 0.15f, 0.022f, Color.White, up = 0.15f, life = 1.4f)
                s.laughAround(x, null, 0.5f, 0.9f)
            } else {
                s.sfx(Sfx.UP_WHISTLE, 0.5f, 0.85f)
            }
            UpperCodes.TRAIN_CHUFF -> {
                s.sfx(Sfx.UP_CHUFF, 0.3f, 0.95f + (if (arg == 1) -0.15f else 0.1f))
                s.burst(PKind.STEAM, x, y, 2, 0.06f, 0.016f, Color.White, up = 0.2f, life = 1.1f)
            }
            UpperCodes.TRAIN_LAP -> {
                s.sfx(Sfx.DING, 0.45f, 1.7f)
                s.burst(PKind.SPARK, x, y - 0.2f, 4, 0.3f, 0.01f, Color(0xFFFFD447))
            }
            UpperCodes.BLOCKS_FALL -> {
                s.sfx(Sfx.UP_TUMBLE, 0.9f)
                s.burst(PKind.DUST, x, y + 0.1f, 8, 0.3f, 0.016f, up = 0.1f)
                val blocks = listOf(Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF5AA9F5), Color(0xFFB983FF), Color(0xFFFF9F43))
                for ((i, col) in blocks.withIndex()) s.burst(PKind.CONFETTI, x, y - 0.02f - i * 0.025f, 2, 0.5f, 0.012f, col, up = 0.5f, life = 1.3f)
                s.laughAround(x, null, 0.6f, 1.1f)
                s.haptic()
            }
            UpperCodes.BLOCKS_BUILD -> {
                for (k in 0 until 6) s.after(0.1f + k * 0.18f) { s.sfx(Sfx.POP, 0.5f, 0.8f + k * 0.12f) }
                s.after(1.25f) {
                    s.sfx(Sfx.CHIME, 0.5f, 1.4f)
                    s.burst(PKind.SPARK, x, y - 0.22f, 8, 0.3f, 0.011f, Color(0xFFFFD447))
                }
            }
            UpperCodes.PORTRAIT -> portrait(s, arg and 0xFF, arg and 0x100 != 0, x, y)
            UpperCodes.DOLL_BELL -> {
                s.sfx(Sfx.UP_DINGDONG, 0.75f)
                s.burst(PKind.SPARK, x, y - 0.1f, 6, 0.25f, 0.01f, Color(0xFFFFD447))
                s.burst(PKind.HEART, x, y - 0.2f, 2, 0.15f, 0.012f, up = 0.3f)
            }
            UpperCodes.PUPPET -> when (arg) {
                0 -> s.sfx(Sfx.SHUT, 0.5f, 1.2f)
                1 -> {
                    // The crocodile bows to a round of applause.
                    s.sfx(Sfx.OPEN, 0.5f, 1.2f)
                    s.after(0.25f) { s.sfx(Sfx.CHOMP, 0.7f, 0.8f) }
                    s.after(0.5f) { s.sfx(Sfx.CHOMP, 0.7f, 0.9f) }
                    applause(s, 0.6f)
                    s.laughAround(x, null, 0.7f, 1.0f)
                }
                else -> {
                    s.sfx(Sfx.OPEN, 0.5f, 1.4f)
                    s.after(0.25f) { s.sfx(Sfx.BOING, 0.5f, 1.6f) }
                    s.after(0.5f) { s.sfx(Sfx.SQUEAK, 0.5f, 1.4f) }
                    applause(s, 0.6f)
                    s.laughAround(x, null, 0.7f, 1.0f)
                }
            }
            UpperCodes.LAMP -> {
                s.sfx(Sfx.CLICK, 0.6f, if (arg == 1) 1.1f else 0.8f)
                if (arg == 1) {
                    s.sfx(Sfx.CHIME, 0.25f, 1.6f)
                    s.burst(PKind.SPARK, x, y - 0.1f, 5, 0.2f, 0.01f, Color(0xFFFFE066))
                }
            }
            UpperCodes.POSTER -> poster(s, arg, x, y)
            UpperCodes.MOBILE -> {
                s.sfx(Sfx.SWISH, 0.3f, 1.3f)
                for (i in 0 until 3) s.after(0.1f + i * 0.17f) { s.sfx(Sfx.NOTE, 0.35f, rate(5 + i * 2) * 2f) }
                s.burst(PKind.STAR, x, y - 0.1f, 4, 0.25f, 0.011f, Color(0xFFFFE066), up = 0.2f)
            }
            UpperCodes.PIT_BURST -> {
                s.sfx(Sfx.UP_RUSTLE, 0.8f, 0.95f + (arg % 3) * 0.08f)
                s.burst(PKind.CRUMB, x, y, 14, 0.6f, 0.015f, null, up = 0.8f, life = 1.3f)
                s.laughAround(x, null, 0.4f, 0.8f)
            }
            UpperCodes.PIT_PLUNGE -> {
                s.sfx(Sfx.UP_RUSTLE, 0.95f, 0.85f)
                s.sfx(Sfx.POP, 0.4f, 0.8f)
                s.burst(PKind.CRUMB, x, y, 16, 0.7f, 0.015f, null, up = 0.9f, life = 1.4f)
                s.person(arg)?.let { p ->
                    s.faces(p, Face.WOW, 0.35f, Face.LAUGH, 1.6f)
                    s.after(0.3f) { s.voice(p, Sfx.GIGGLE, 0.6f) }
                    s.laughAround(p.x, p, 0.6f, 0.8f)
                }
            }
            UpperCodes.PIT_BALL -> {
                s.sfx(Sfx.BOING, 0.5f, 1.3f)
                s.burst(PKind.SPARK, x, y, 5, 0.3f, 0.01f)
            }
            UpperCodes.CLIMB_STEP -> {
                s.sfx(Sfx.CLICK, 0.5f, 0.7f)
                s.burst(PKind.DUST, x, y, 3, 0.15f, 0.01f, up = 0.05f, life = 0.5f)
                s.person(arg)?.let { s.voice(it, Sfx.OOF, 0.35f) }
            }
            UpperCodes.CLIMB_TOP -> {
                // The bell: ding-ding, and a cheer for whoever rang it.
                s.sfx(Sfx.DING, 0.9f, 1.15f)
                s.after(0.18f) { s.sfx(Sfx.DING, 0.7f, 1.55f) }
                s.burst(PKind.STAR, x, y, 10, 0.5f, 0.013f, Color(0xFFFFD447), up = 0.4f)
                s.person(arg)?.let { p ->
                    s.faces(p, Face.WOW, 0.4f, Face.LAUGH, 1.6f)
                    s.after(0.4f) { s.voice(p, Sfx.GIGGLE, 0.7f) }
                    s.burst(PKind.CONFETTI, p.x, p.y - p.h, 14, 0.7f, 0.012f, up = 0.7f, life = 1.6f)
                }
                s.laughAround(x, null, 0.6f, 1.0f)
            }
            UpperCodes.CLIMB_DROP -> {
                s.sfx(Sfx.WHOOSH, 0.35f, 1.6f)
                s.person(arg)?.let { p ->
                    s.faces(p, Face.OOH, 0.4f, Face.LAUGH, 1.4f)
                    s.voice(p, Sfx.OOH, 0.6f)
                }
            }
            UpperCodes.TRAMP -> {
                s.sfx(Sfx.UP_SPROING, 0.7f, 0.95f)
                s.burst(PKind.SPARK, x, y, 5, 0.3f, 0.01f)
            }
            UpperCodes.PAINT -> {
                val col = paint[(arg and 0xFF) % paint.size]
                s.sfx(Sfx.SPLAT, 0.65f, 1.3f)
                s.sfx(Sfx.SPRAY, 0.4f, 0.9f)
                s.burst(PKind.DROP, x, y, 16, 0.7f, 0.014f, col, up = 0.6f, life = 1.0f)
                s.burst(PKind.CRUMB, x, y, 8, 0.5f, 0.012f, col, up = 0.5f, life = 0.9f)
                if (arg and 0x100 != 0) {
                    // A new canvas: a page turns and a little chime rings.
                    s.sfx(Sfx.PAGE, 0.6f)
                    s.after(0.2f) { s.sfx(Sfx.CHIME, 0.5f, 1.3f) }
                    s.burst(PKind.STAR, x, y, 8, 0.5f, 0.012f, Color(0xFFFFE066))
                }
                s.laughAround(x, null, 0.5f, 0.8f)
            }
            UpperCodes.KARAOKE -> karaoke(s, arg, x, y)
            UpperCodes.KARAOKE_SQUEAL -> {
                s.sfx(Sfx.SQUEAK, 0.7f, 1.8f)
                s.sfx(Sfx.SQUEAK, 0.5f, 2f)
            }
            UpperCodes.FORT -> if (arg == 1) {
                s.sfx(Sfx.OPEN, 0.5f, 1.3f)
                s.after(0.1f) { s.sfx(Sfx.SPARKLE, 0.35f, 1.4f) }
                s.burst(PKind.STAR, x, y, 8, 0.35f, 0.012f, Color(0xFFFFE066), up = 0.2f)
            } else {
                s.sfx(Sfx.SHUT, 0.5f, 1.4f)
                s.burst(PKind.DUST, x, y, 4, 0.2f, 0.012f, up = 0.1f, life = 0.6f)
            }
            UpperCodes.SHOWER -> if (arg == 1) {
                s.sfx(Sfx.CLICK, 0.6f, 1.1f)
                s.sfx(Sfx.UP_SHOWER, 0.6f)
                s.burst(PKind.STEAM, x, y - 0.1f, 5, 0.1f, 0.03f, Color.White, up = 0.15f, life = 1.6f)
            } else {
                s.sfx(Sfx.CLICK, 0.5f, 0.8f)
                s.after(0.3f) { s.sfx(Sfx.BUBBLE, 0.4f, 0.8f) }
            }
            UpperCodes.SHOWER_SING -> shower(s, arg, x, y)
            UpperCodes.RAINBOW -> {
                s.sfx(Sfx.MAGIC, 0.7f)
                s.after(0.2f) { s.sfx(Sfx.SPARKLE, 0.6f) }
                s.burst(PKind.STAR, x, y - 0.15f, 14, 0.6f, 0.013f, null, up = 0.4f)
                s.haptic()
            }
            UpperCodes.MIRROR -> when (arg) {
                0 -> {
                    s.sfx(Sfx.SPARKLE, 0.5f, 1.3f)
                    s.burst(PKind.SPARK, x, y - 0.12f, 5, 0.2f, 0.01f)
                }
                1 -> s.sfx(Sfx.SPRAY, 0.2f, 0.7f)
                2 -> {
                    s.sfx(Sfx.UP_WIPE, 0.7f)
                    s.after(0.3f) { s.sfx(Sfx.UP_WIPE, 0.6f, 1.15f) }
                    s.after(0.55f) { s.sfx(Sfx.GIGGLE, 0.35f, 1.5f) }
                }
                else -> {
                    s.sfx(Sfx.UP_WIPE, 0.8f, 0.9f)
                    s.after(0.35f) { s.sfx(Sfx.SPARKLE, 0.5f, 1.2f) }
                    s.burst(PKind.SPARK, x, y - 0.12f, 6, 0.2f, 0.01f)
                }
            }
            UpperCodes.TOWEL -> {
                s.sfx(Sfx.SWISH, 0.4f, 1.1f)
                if (arg == 1) {
                    s.after(0.25f) { s.sfx(Sfx.UP_QUACK, 0.8f) }
                    s.after(0.7f) { s.sfx(Sfx.SQUEAK, 0.5f, 1.2f) }
                    s.burst(PKind.SPARK, x, y, 6, 0.25f, 0.01f, Color(0xFFFFD447))
                    s.laughAround(x, null, 0.6f, 0.8f)
                }
            }
            UpperCodes.WARDROBE -> if (arg == 1) {
                s.sfx(Sfx.OPEN, 0.7f, 0.85f)
                s.sfx(Sfx.UP_RUSTLE, 0.4f, 1.2f)
                s.after(0.1f) { s.sfx(Sfx.SPARKLE, 0.3f, 1.1f) }
            } else {
                s.sfx(Sfx.SHUT, 0.7f, 0.85f)
            }
            UpperCodes.DRESS -> dress(s, arg, x, y)
            UpperCodes.WARDROBE_TOSS -> {
                s.sfx(Sfx.POP, 0.7f, 1.0f)
                s.after(0.1f) { s.sfx(Sfx.UP_RUSTLE, 0.5f, 1.3f) }
                s.burst(PKind.CONFETTI, x, y - 0.25f, 12, 0.7f, 0.012f, null, up = 0.7f, life = 1.5f)
            }
            UpperCodes.VANITY -> {
                s.sfx(Sfx.CLICK, 0.6f, 1.2f)
                s.sfx(Sfx.SPARKLE, 0.5f, 1.4f)
                s.burst(PKind.SPARK, x, y - 0.25f, 8, 0.3f, 0.011f, Color(0xFFFFE066))
                if (arg == 1) s.burst(PKind.STEAM, x + 0.1f, y, 3, 0.05f, 0.016f, Color(0xFFFFD6E6), up = 0.1f, life = 1.0f)
            }
            UpperCodes.JEWEL -> if (arg == 1) {
                s.sfx(Sfx.OPEN, 0.4f, 1.6f)
                for (i in 0 until 26) {
                    val n = WALTZ[i % WALTZ.size]
                    s.after(0.4f + i * 0.33f) {
                        s.sfx(Sfx.UP_MUSICBOX, 0.45f, rate(n))
                        if (i % 4 == 0) s.burst(PKind.NOTE, x + 0.04f, y - 0.1f, 1, 0.06f, 0.012f, Color(0xFFB983FF), up = 0.2f, life = 1.5f)
                    }
                }
                s.burst(PKind.STAR, x, y - 0.1f, 6, 0.3f, 0.011f, Color(0xFFFFE9F2))
            } else {
                s.sfx(Sfx.SHUT, 0.4f, 1.6f)
            }
            UpperCodes.ROCK -> {
                s.sfx(Sfx.OPEN, 0.22f, 0.5f)
                s.after(0.5f) { s.sfx(Sfx.OPEN, 0.18f, 0.55f) }
            }
            UpperCodes.WINDOW -> window(s, arg, x, y)
            UpperCodes.FEEDER_FILL -> {
                s.sfx(Sfx.UP_RUSTLE, 0.5f, 1.6f)
                s.burst(PKind.CRUMB, x, y - 0.1f, 10, 0.2f, 0.007f, Color(0xFFE8C85A), up = 0.0f, life = 1.0f)
            }
            UpperCodes.BIRD -> {
                s.sfx(Sfx.UP_TWEET, 0.6f, 0.9f + (arg and 3) * 0.12f)
                s.after(0.9f) { s.sfx(Sfx.UP_TWEET, 0.4f, 1.1f + (arg and 3) * 0.1f) }
                s.burst(PKind.LEAF, x, y, 3, 0.1f, 0.008f, Color.White, up = 0.1f, life = 1.5f)
            }
            UpperCodes.SWING -> {
                s.sfx(Sfx.SWISH, 0.3f, 0.8f)
                s.sfx(Sfx.OPEN, 0.15f, 0.6f)
            }
            UpperCodes.RAIL -> {
                s.sfx(Sfx.SWISH, 0.4f, 1.2f)
                val petals = listOf(Color(0xFFFF8FB1), Color(0xFFFFD447), Color(0xFFF7F3EC))
                for ((i, col) in petals.withIndex()) s.burst(PKind.LEAF, x + (i - 1) * 0.04f, y - 0.05f, 3, 0.15f, 0.01f, col, up = 0.1f, life = 2f)
                // A faint «hello!» answers from far away.
                s.after(0.7f) { s.sfx(Sfx.OOH, 0.15f, 1.3f) }
            }
            UpperCodes.DUCK_KEY -> duckKey(s, arg, x, y)
            UpperCodes.BATH -> if (arg == 1) {
                s.sfx(Sfx.CLICK, 0.6f)
                s.sfx(Sfx.SPLASH, 0.5f, 1.3f)
                s.burst(PKind.BUBBLE, x, y - 0.04f, 14, 0.25f, 0.014f, Color.White, up = 0.5f, life = 2f)
                s.after(0.4f) { s.sfx(Sfx.BUBBLE, 0.5f, 1.2f) }
            } else {
                s.sfx(Sfx.CLICK, 0.5f, 0.8f)
                s.sfx(Sfx.BLOOP, 0.5f, 0.8f)
            }
            UpperCodes.PURR -> {
                s.sfx(Sfx.SNORE, 0.3f, 1.8f)
                s.burst(PKind.HEART, x, y - 0.03f, 3, 0.12f, 0.014f, up = 0.3f, life = 1.5f)
            }
            UpperCodes.SEAT_BOING -> {
                s.sfx(Sfx.BOING, 0.35f, 1.2f)
                s.burst(PKind.DUST, x, y - 0.1f, 3, 0.15f, 0.012f, up = 0.1f, life = 0.5f)
            }
            else -> Unit
        }
    }

    /** A little round of applause: fast soft taps, close together. */
    private fun applause(s: FxStage, volume: Float) {
        for (i in 0 until 9) s.after(0.5f + i * 0.09f + s.random.nextFloat() * 0.05f) { s.sfx(Sfx.TICK, volume * 0.5f, 1.3f + s.random.nextFloat() * 0.5f) }
    }

    private fun portrait(s: FxStage, variant: Int, quiet: Boolean, x: Float, y: Float) {
        val v = if (quiet) 0.25f else 0.7f
        when (variant) {
            0 -> {
                // Grandpa tips his cap: a polite «hm-hm».
                s.sfx(Sfx.HMM, v, 0.75f)
                s.after(0.3f) { s.sfx(Sfx.HMM, v, 0.85f) }
            }
            1 -> s.sfx(Sfx.GIGGLE, v, 0.85f)
            2 -> {
                s.sfx(Sfx.POP, v, 1.4f)
                if (!quiet) s.burst(PKind.HEART, x + 0.04f, y - 0.05f, 3, 0.15f, 0.016f, up = 0.4f, life = 1.5f)
            }
            3 -> {
                s.sfx(Sfx.OOH, v, 0.9f)
                s.after(0.25f) { s.sfx(Sfx.YUM, v * 0.7f, 1.1f) }
            }
            4 -> s.sfx(Sfx.GIGGLE, v, 1.7f)
            else -> {
                s.sfx(Sfx.WOOF, v * 0.7f, 1.1f)
                s.after(0.25f) { s.sfx(Sfx.WOOF, v * 0.5f, 1.2f) }
            }
        }
        if (!quiet) s.burst(PKind.SPARK, x, y, 4, 0.2f, 0.01f)
    }

    private fun poster(s: FxStage, variant: Int, x: Float, y: Float) {
        when (variant) {
            0 -> {
                s.sfx(Sfx.WHOOSH, 0.5f, 1.4f)
                s.burst(PKind.SPARK, x, y, 8, 0.4f, 0.011f, Color(0xFFFF9F43), up = 0.5f)
            }
            1 -> {
                s.sfx(Sfx.ROAR, 0.55f, 1.5f)
                for (i in 0 until 3) s.after(0.1f + i * 0.2f) { s.burst(PKind.NOTE, x + 0.05f, y - 0.04f, 1, 0.1f, 0.013f, Color(0xFF8B5CF6), up = 0.2f, life = 1.4f) }
            }
            2 -> {
                s.sfx(Sfx.SPARKLE, 0.6f, 1.2f)
                s.burst(PKind.STAR, x, y, 8, 0.35f, 0.012f, null, up = 0.3f)
            }
            else -> {
                for (i in 0 until 3) s.after(i * 0.15f) { s.sfx(Sfx.BUBBLE, 0.5f, 0.9f + i * 0.2f) }
                s.burst(PKind.BUBBLE, x, y, 6, 0.1f, 0.012f, Color.White, up = 0.35f, life = 1.6f)
            }
        }
    }

    /** A song with the singer's own voice, drums behind it and colours in the air; the audience dances. Nobody on stage: just the band. */
    private fun karaoke(s: FxStage, id: Int, x: Float, y: Float) {
        val singer = s.person(id)
        val tune = intArrayOf(5, 7, 8, 7, 5, 9, 8, 7, 5, 3, 5, 7, 8, 9, 8, 5)
        val base = singer?.voice ?: 1.1f
        s.sfx(Sfx.DRUM, 0.6f, 0.8f)
        for ((i, n) in tune.withIndex()) {
            val t = 0.35f + i * 0.24f
            s.after(t) {
                if (singer != null) {
                    s.sfx(Sfx.OOH, 0.55f, (base * rate(n)).coerceIn(0.5f, 2f))
                    singer.anim.talk = 0.3f
                } else {
                    s.sfx(Sfx.NOTE, 0.5f, rate(n) * 2f)
                }
                if (i % 2 == 0) s.sfx(Sfx.DRUM, 0.45f, if (i % 4 == 0) 0.8f else 1.6f)
                s.burst(PKind.NOTE, x + (s.random.nextFloat() - 0.5f) * 0.4f, y - 0.2f, 1, 0.1f, 0.013f, listOf(Color(0xFFFF6B9E), Color(0xFF6BC6FF), Color(0xFFFFD447))[i % 3], up = 0.3f, life = 1.6f)
            }
        }
        s.after(0.35f + tune.size * 0.24f) {
            s.sfx(Sfx.FANFARE, 0.5f)
            s.burst(PKind.CONFETTI, x, y - 0.3f, 24, 0.9f, 0.012f, null, up = 0.8f, life = 2f)
            applause(s, 0.9f)
        }
        singer?.let { s.faces(it, Face.GRIN, 1.0f, Face.LAUGH, 2f) }
        // Everybody close by dances.
        for (b in s.world.bodiesIn(s.place)) {
            if (b is Person && !b.held && abs(b.x - x) < 1.3f && b.anim.face != Face.SLEEP) b.anim.cheer = 4.5f
        }
    }

    /** Singing in the shower: la-la-la, a very high note, and a squeak. */
    private fun shower(s: FxStage, id: Int, x: Float, y: Float) {
        val p = s.person(id) ?: return
        val tune = intArrayOf(5, 6, 7, 6, 8, 9)
        for ((i, n) in tune.withIndex()) {
            s.after(i * 0.2f) {
                s.sfx(Sfx.OOH, 0.5f, (p.voice * rate(n)).coerceIn(0.5f, 2f))
                p.anim.talk = 0.25f
                s.burst(PKind.NOTE, x + 0.03f, y - 0.05f, 1, 0.08f, 0.012f, Color(0xFF4D96FF), up = 0.25f, life = 1.4f)
            }
        }
        s.after(1.3f) { s.sfx(Sfx.SQUEAK, 0.5f, 1.9f) }
        s.faces(p, Face.GRIN, 1.2f, Face.LAUGH, 1.2f)
        s.laughAround(p.x, p, 1.5f, 0.8f)
    }

    /** The wardrobe's reveal: a whirl of clothes, a pop, stars, a chime, and the figure laughing in a new drakt. */
    private fun dress(s: FxStage, id: Int, x: Float, y: Float) {
        val p = s.person(id)
        s.sfx(Sfx.WHOOSH, 0.7f, 1.2f)
        s.sfx(Sfx.UP_RUSTLE, 0.6f, 1.0f)
        val cloth = Palette.cloth
        for (k in 0 until 7) {
            val col = Color(cloth[(k * 3 + 1) % cloth.size])
            s.burst(PKind.CONFETTI, x, y, 2, 0.8f, 0.016f, col, up = 0.5f, life = 1.2f)
        }
        s.after(0.35f) {
            s.sfx(Sfx.POOF, 0.7f)
            s.burst(PKind.STAR, x, y - 0.05f, 16, 0.7f, 0.014f, null, up = 0.4f)
            s.burst(PKind.SMOKE, x, y, 5, 0.1f, 0.03f, Color(0xFFD9CCF2), up = 0.1f, life = 1.2f)
        }
        s.after(0.6f) {
            s.sfx(Sfx.MAGIC, 0.7f)
            s.sfx(Sfx.CHIME, 0.5f, 1.2f)
        }
        p?.let {
            s.faces(it, Face.WOW, 0.7f, Face.LAUGH, 2.0f)
            s.after(0.8f) { s.voice(it, Sfx.GIGGLE, 0.8f) }
            s.laughAround(it.x, it, 1.0f, 1.0f)
        }
        s.haptic()
    }

    private fun window(s: FxStage, arg: Int, x: Float, y: Float) {
        val variant = arg and 3
        val closed = arg and 4 != 0
        val night = arg and 16 != 0
        s.sfx(Sfx.SWISH, 0.5f, 1.3f)
        if (variant == 1 && night && !closed) {
            // A shooting star, and a wish.
            s.after(0.25f) {
                s.sfx(Sfx.SPARKLE, 0.8f, 1.2f)
                s.sfx(Sfx.MAGIC, 0.5f, 1.3f)
            }
            s.burst(PKind.STAR, x, y - 0.1f, 10, 0.6f, 0.013f, Color(0xFFFFE066), up = 0.3f)
        } else if (!closed) {
            s.after(0.3f) { s.sfx(Sfx.CHIRP, 0.4f, 1.1f) }
        }
    }

    /** The duck that swallowed the golden key: quack, quack, a mighty «hikk!», and out it pops. */
    private fun duckKey(s: FxStage, id: Int, x: Float, y: Float) {
        s.sfx(Sfx.UP_QUACK, 0.8f)
        s.after(0.35f) { s.sfx(Sfx.UP_QUACK, 0.7f, 1.2f) }
        s.after(0.8f) {
            s.sfx(Sfx.HICCUP, 0.9f, 0.9f)
            s.burst(PKind.BUBBLE, x, y, 8, 0.3f, 0.014f, Color.White, up = 0.5f, life = 1.5f)
        }
        s.after(1.0f) {
            s.sfx(Sfx.POP, 0.9f, 1.2f)
            s.sfx(Sfx.MAGIC, 0.7f)
            s.burst(PKind.STAR, x, y - 0.05f, 22, 0.8f, 0.015f, Color(0xFFFFD447), up = 0.6f, life = 1.4f)
            s.burst(PKind.HEART, x, y - 0.08f, 4, 0.25f, 0.016f, up = 0.4f, life = 1.6f)
        }
        s.after(1.3f) { s.sfx(Sfx.UP_QUACK, 0.5f, 1.5f) }
        s.laughAround(x, null, 1.1f, 1.4f)
        s.haptic()
        s.changed()
    }

    @Suppress("unused")
    private fun wiggle(v: Float) = sin(v)
}
