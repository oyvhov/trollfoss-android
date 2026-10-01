package app.trollfoss.audio

import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** Every sound effect in Trollfoss. Voices are played at each figure's own pitch. */
enum class Sfx {
    TAP, PICK, DROP, THUD, BOING, CHOMP, GULP, YUM, GIGGLE, OOH, BABBLE, HMM, OOF, SPARKLE, MAGIC, POOF,
    SPLASH, SIZZLE, DING, WHIRR, SNIP, SPRAY, FLUSH, SQUEAK, ZIP, SHUTTER, FANFARE, CLICK, NOTE, DRUM, RING,
    HORN, OWL, CUCKOO, REGISTER, POP, WHOOSH, CHIME, BUBBLE, BLOOP, TICK, FIREWORK, COIN, PAGE, SWISH, OPEN,
    SHUT, VROOM, MEOW, WOOF, CHIRP, ROAR, SNORE, MOO, BAA, CLUCK, NEIGH, BEEP, RUMBLE,

    // Silly ones: humour is half the game.
    BURP, HICCUP, PRRT, SNEEZE, SLIP, BONK, TICKLE, SPLAT,

    // Rolf the robot butler and Sture the ghost (see ui/play/FigurarFx.kt). Kept here, ahead of the floors'
    // blocks, so that merging the floors never touches them.
    FG_ROLF_TALK, FG_ROLF_LAUGH, FG_ROLF_OOH, FG_ROLF_OOF, FG_ROLF_HAPPY, FG_ROLF_FUEL, FG_ROLF_BOW,
    FG_STURE_GIGGLE, FG_STURE_HUM, FG_STURE_OOH, FG_STURE_SNIFF, FG_STURE_SNEEZE,
    // Mitt hus (MineFx.kt): building sounds.
    MI_HAMMER, MI_SAW, MI_DRILL, MI_PLING, MI_CRANE, MI_PLANK, MI_DINGDONG, MI_CRASH,

    // Storhuset. Each floor adds its sounds in its own block, and their recipes in [Synth.voices] below.
    // ---- ground floor ----
    GR_BONG, GR_CLANK, GR_CREAK, GR_TINKLE, GR_CRACKLE, GR_FLAP, GR_BELL, GR_CLINK, GR_POPCORN, GR_FILM,
    GR_WHOOMP, GR_BLEND, GR_RATTLE, GR_TALK, GR_TADA, GR_SHUSH, GR_SLURP,

    // ---- upper floor ----
    UP_WHISTLE, UP_CHUFF, UP_TUMBLE, UP_QUACK, UP_SHOWER, UP_RUSTLE, UP_TWEET, UP_MUSICBOX, UP_DINGDONG, UP_WIPE, UP_SPROING,

    // ---- attic ----
    AT_CREAK, AT_WIND, AT_SCRATCH, AT_OOO, AT_SPRING, AT_KNIT, AT_DONG, AT_COINS,

    // ---- cellar ----
    /** Pipes, steam and machines of the cellar: a metal clang, a hiss, the washer's drum, a saw, rail clacks, a zap, party sounds. */
    CE_CLANG, CE_HISS, CE_THRUM, CE_RASP, CE_CLACK, CE_ZAP, CE_BASS, CE_HAT, CE_WHISTLE, CE_CREAK, CE_BANG, CE_BELL,

    // ---- garden ----
    GA_CROAK, GA_CREAK, GA_WINK, GA_GROW, GA_SPRAY, GA_DIG,
}

/**
 * Synthesises every effect: soft bell and pluck tones, little vowel-shaped voices, and filtered noise
 * for water, air and fizz. Nothing is recorded, so there is nothing to license, and every sound is
 * rounded off so it stays friendly at full volume.
 */
object Synth {
    const val SAMPLE_RATE = 44_100

    private val bell = listOf(1.0 to 1.0, 2.0 to 0.32, 3.0 to 0.1, 4.16 to 0.05)
    private val soft = listOf(1.0 to 1.0, 2.0 to 0.12)
    private val hum = listOf(1.0 to 1.0, 2.0 to 0.25, 3.0 to 0.1)
    private val vowelE = listOf(1.0 to 0.55, 2.0 to 1.0, 3.0 to 0.5, 4.0 to 0.25, 5.0 to 0.12)
    private val vowelO = listOf(1.0 to 1.0, 2.0 to 0.6, 3.0 to 0.2, 4.0 to 0.08)
    private val buzzy = listOf(1.0 to 1.0, 2.0 to 0.5, 3.0 to 0.35, 4.0 to 0.25, 5.0 to 0.18, 6.0 to 0.12)
    private val pluck = listOf(1.0 to 1.0, 2.0 to 0.42, 3.0 to 0.16, 4.0 to 0.08, 6.0 to 0.03)
    private val brass = listOf(1.0 to 1.0, 2.0 to 0.6, 3.0 to 0.45, 4.0 to 0.3, 5.0 to 0.2)

    private sealed interface Voice

    private class Tone(
        val frequency: Double,
        val start: Double,
        val duration: Double,
        val gain: Double = 0.5,
        val partials: List<Pair<Double, Double>> = bell,
        val decay: Double = 5.0,
        val slideTo: Double? = null,
        val vibrato: Double = 0.0,
        val vibratoHz: Double = 6.0,
        val attack: Double = 0.004,
    ) : Voice

    private class Noise(
        val start: Double,
        val duration: Double,
        val gain: Double,
        val from: Double,
        val to: Double = from,
        val decay: Double = 6.0,
        val attack: Double = 0.003,
        /** Rise and fall like a gust instead of decaying from the start. */
        val swell: Boolean = false,
        val seed: Int = 1,
    ) : Voice

    private class Crackle(val start: Double, val duration: Double, val density: Double, val gain: Double, val seed: Int = 3) : Voice

    private fun syllables(count: Int, base: Double, step: Double, length: Double, gap: Double, partials: List<Pair<Double, Double>>, gain: Double, seed: Int = 0): List<Voice> {
        val random = java.util.Random(seed.toLong())
        return (0 until count).map { i ->
            val f = base + step * i + if (seed != 0) (random.nextDouble() - 0.5) * base * 0.5 else 0.0
            Tone(f, i * (length + gap), length, gain, partials, decay = 5.0, slideTo = f * 0.86, vibrato = 0.25, vibratoHz = 9.0, attack = 0.01)
        }
    }

    private fun voices(sfx: Sfx): List<Voice> = when (sfx) {
        Sfx.TAP -> listOf(
            Tone(650.0, 0.0, 0.085, 0.3, soft, decay = 32.0, slideTo = 520.0),
            Tone(1300.0, 0.0, 0.045, 0.12, soft, decay = 55.0),
        )
        Sfx.PICK -> listOf(
            Tone(420.0, 0.0, 0.13, 0.45, soft, decay = 12.0, slideTo = 840.0),
            Tone(840.0, 0.0, 0.09, 0.15, soft, decay = 18.0, slideTo = 1680.0),
        )
        Sfx.DROP -> listOf(
            Tone(190.0, 0.0, 0.11, 0.6, soft, decay = 30.0, slideTo = 110.0),
            Noise(0.0, 0.05, 0.22, 1100.0, 300.0, decay = 60.0),
        )
        Sfx.THUD -> listOf(
            Tone(130.0, 0.0, 0.2, 0.75, soft, decay = 17.0, slideTo = 65.0),
            Noise(0.0, 0.09, 0.35, 700.0, 200.0, decay = 35.0),
        )
        Sfx.BOING -> listOf(
            Tone(240.0, 0.0, 0.17, 0.45, soft, decay = 5.0, slideTo = 560.0, vibrato = 0.4, vibratoHz = 14.0),
            Tone(560.0, 0.15, 0.26, 0.4, soft, decay = 7.0, slideTo = 380.0, vibrato = 0.5, vibratoHz = 12.0),
        )
        Sfx.CHOMP -> listOf(
            Noise(0.0, 0.06, 0.55, 2600.0, 1200.0, decay = 45.0, seed = 5),
            Tone(230.0, 0.0, 0.05, 0.3, soft, decay = 40.0, slideTo = 170.0),
            Noise(0.12, 0.06, 0.5, 2300.0, 1100.0, decay = 45.0, seed = 9),
            Tone(210.0, 0.12, 0.05, 0.28, soft, decay = 40.0, slideTo = 160.0),
        )
        Sfx.GULP -> listOf(
            Tone(300.0, 0.0, 0.09, 0.5, soft, decay = 14.0, slideTo = 520.0),
            Tone(520.0, 0.1, 0.13, 0.4, soft, decay = 12.0, slideTo = 250.0),
        )
        Sfx.YUM -> listOf(
            Tone(330.0, 0.0, 0.2, 0.35, hum, decay = 3.0, vibrato = 0.2, attack = 0.03),
            Tone(392.0, 0.22, 0.32, 0.35, hum, decay = 3.0, slideTo = 440.0, vibrato = 0.3, attack = 0.03),
        )
        Sfx.GIGGLE -> syllables(4, 760.0, -45.0, 0.07, 0.02, vowelE, 0.38)
        Sfx.OOH -> listOf(Tone(380.0, 0.0, 0.36, 0.42, vowelO, decay = 3.0, slideTo = 700.0, vibrato = 0.3, attack = 0.02))
        Sfx.BABBLE -> syllables(4, 560.0, 0.0, 0.075, 0.035, vowelE, 0.34, seed = 11)
        Sfx.HMM -> listOf(Tone(260.0, 0.0, 0.3, 0.32, hum, decay = 3.0, slideTo = 300.0, vibrato = 0.2, attack = 0.03))
        Sfx.OOF -> listOf(Tone(420.0, 0.0, 0.13, 0.42, vowelO, decay = 8.0, slideTo = 270.0, attack = 0.008))
        Sfx.SPARKLE -> listOf(2093.0, 2637.0, 3136.0, 3520.0, 4186.0).mapIndexed { i, f -> Tone(f, i * 0.05, 0.4, 0.22, decay = 7.0) }
        Sfx.MAGIC -> listOf(1046.5, 1174.7, 1318.5, 1568.0, 1760.0, 2093.0, 2349.3, 2637.0).mapIndexed { i, f ->
            Tone(f, i * 0.04, 0.5, 0.2, decay = 5.0)
        } + Noise(0.0, 0.6, 0.08, 7000.0, 9000.0, swell = true)
        Sfx.POOF -> listOf(
            Noise(0.0, 0.38, 0.55, 1900.0, 300.0, decay = 8.0),
            Tone(210.0, 0.0, 0.22, 0.3, soft, decay = 12.0, slideTo = 90.0),
        )
        Sfx.SPLASH -> listOf(
            Noise(0.0, 0.5, 0.6, 3200.0, 700.0, decay = 7.0, seed = 2),
            Noise(0.04, 0.35, 0.3, 5500.0, 2200.0, decay = 9.0, seed = 4),
            Tone(260.0, 0.0, 0.12, 0.25, soft, decay = 18.0, slideTo = 140.0),
        )
        Sfx.SIZZLE -> listOf(
            Noise(0.0, 0.7, 0.3, 6500.0, 5200.0, decay = 2.5, attack = 0.03, seed = 6),
            Crackle(0.0, 0.7, 0.05, 0.35),
        )
        Sfx.DING -> listOf(
            Tone(1568.0, 0.0, 1.3, 0.5, bell, decay = 3.0),
            Tone(2093.0, 0.0, 1.1, 0.24, bell, decay = 3.5),
        )
        Sfx.WHIRR -> listOf(
            Tone(135.0, 0.0, 1.1, 0.35, buzzy, decay = 0.4, vibrato = 0.3, vibratoHz = 30.0, attack = 0.08),
            Noise(0.0, 1.1, 0.22, 1300.0, 1700.0, decay = 0.5, attack = 0.08),
        )
        Sfx.SNIP -> listOf(
            Noise(0.0, 0.04, 0.5, 5200.0, 4000.0, decay = 80.0),
            Tone(2600.0, 0.0, 0.03, 0.2, soft, decay = 90.0),
            Noise(0.1, 0.04, 0.5, 5200.0, 4000.0, decay = 80.0, seed = 7),
            Tone(2900.0, 0.1, 0.03, 0.2, soft, decay = 90.0),
        )
        Sfx.SPRAY -> listOf(Noise(0.0, 0.4, 0.4, 7200.0, 6000.0, decay = 4.0, attack = 0.02))
        Sfx.FLUSH -> listOf(
            Noise(0.0, 1.3, 0.55, 700.0, 2400.0, swell = true, seed = 8),
            Tone(210.0, 0.35, 0.4, 0.22, soft, decay = 6.0, slideTo = 110.0, vibrato = 1.0, vibratoHz = 9.0),
            Tone(180.0, 0.8, 0.35, 0.2, soft, decay = 6.0, slideTo = 95.0, vibrato = 1.0, vibratoHz = 11.0),
        )
        Sfx.SQUEAK -> listOf(
            Tone(1400.0, 0.0, 0.09, 0.4, soft, decay = 10.0, slideTo = 1900.0),
            Tone(1900.0, 0.1, 0.1, 0.35, soft, decay = 10.0, slideTo = 1500.0),
        )
        Sfx.ZIP -> listOf(
            Noise(0.0, 0.22, 0.4, 1800.0, 6200.0, decay = 5.0),
            Tone(900.0, 0.0, 0.2, 0.12, soft, decay = 8.0, slideTo = 1800.0, vibrato = 1.0, vibratoHz = 40.0),
        )
        Sfx.SHUTTER -> listOf(
            Noise(0.0, 0.03, 0.6, 5200.0, 4000.0, decay = 90.0),
            Tone(1200.0, 0.0, 0.02, 0.22, soft, decay = 120.0),
            Noise(0.07, 0.04, 0.5, 4200.0, 3000.0, decay = 80.0, seed = 5),
        )
        Sfx.FANFARE -> listOf(
            Tone(523.25, 0.0, 0.18, 0.4, brass, decay = 4.0, attack = 0.02),
            Tone(659.25, 0.12, 0.18, 0.4, brass, decay = 4.0, attack = 0.02),
            Tone(783.99, 0.24, 0.18, 0.4, brass, decay = 4.0, attack = 0.02),
            Tone(1046.5, 0.36, 0.8, 0.45, brass, decay = 2.5, attack = 0.02),
            Tone(783.99, 0.36, 0.8, 0.2, bell, decay = 2.5),
            Tone(659.25, 0.36, 0.8, 0.2, bell, decay = 2.5),
        )
        Sfx.CLICK -> listOf(
            Tone(1800.0, 0.0, 0.025, 0.4, soft, decay = 120.0),
            Noise(0.0, 0.015, 0.3, 4000.0, decay = 150.0),
        )
        // Middle C an octave up; the piano and guitar pitch it with the playback rate.
        Sfx.NOTE -> listOf(Tone(523.25, 0.0, 0.9, 0.5, pluck, decay = 4.0))
        Sfx.DRUM -> listOf(
            Tone(115.0, 0.0, 0.35, 0.8, soft, decay = 9.0, slideTo = 60.0),
            Noise(0.0, 0.08, 0.3, 1500.0, 400.0, decay = 30.0),
        )
        Sfx.RING -> (0 until 6).map { i -> Tone(if (i % 2 == 0) 1320.0 else 1650.0, i * 0.07, 0.065, 0.3, soft, decay = 6.0) } +
            (0 until 6).map { i -> Tone(if (i % 2 == 0) 1320.0 else 1650.0, 0.55 + i * 0.07, 0.065, 0.3, soft, decay = 6.0) }
        Sfx.HORN -> listOf(
            Tone(220.0, 0.0, 0.55, 0.42, brass, decay = 1.0, attack = 0.04, vibrato = 0.1),
            Tone(277.2, 0.0, 0.55, 0.3, brass, decay = 1.0, attack = 0.04, vibrato = 0.1),
        )
        Sfx.OWL -> listOf(
            Tone(430.0, 0.0, 0.26, 0.4, vowelO, decay = 2.0, slideTo = 390.0, attack = 0.04),
            Tone(430.0, 0.36, 0.42, 0.4, vowelO, decay = 2.0, slideTo = 370.0, attack = 0.04),
        )
        Sfx.CUCKOO -> listOf(
            Tone(784.0, 0.0, 0.2, 0.45, vowelO, decay = 4.0, attack = 0.01),
            Tone(622.3, 0.24, 0.3, 0.45, vowelO, decay = 4.0, attack = 0.01),
            Tone(784.0, 0.62, 0.2, 0.45, vowelO, decay = 4.0, attack = 0.01),
            Tone(622.3, 0.86, 0.3, 0.45, vowelO, decay = 4.0, attack = 0.01),
        )
        Sfx.REGISTER -> listOf(
            Noise(0.0, 0.12, 0.35, 3000.0, 2000.0, decay = 30.0),
            Tone(2093.0, 0.06, 0.6, 0.4, bell, decay = 5.0),
            Tone(2637.0, 0.12, 0.6, 0.32, bell, decay = 5.0),
        )
        Sfx.POP -> listOf(
            Tone(480.0, 0.0, 0.08, 0.5, soft, decay = 24.0, slideTo = 1050.0),
            Noise(0.0, 0.02, 0.25, 2200.0, decay = 90.0),
        )
        Sfx.WHOOSH -> listOf(Noise(0.0, 0.95, 0.7, 250.0, 2600.0, swell = true, seed = 7))
        Sfx.CHIME -> listOf(783.99, 987.77, 1174.66, 1567.98, 1975.53).mapIndexed { i, f -> Tone(f, i * 0.07, 0.8, 0.3, bell, decay = 3.5) } +
            listOf(3136.0, 3951.0, 4699.0).mapIndexed { i, f -> Tone(f, 0.35 + i * 0.06, 0.4, 0.12, decay = 7.0) }
        Sfx.BUBBLE -> listOf(Tone(620.0, 0.0, 0.07, 0.35, soft, decay = 22.0, slideTo = 1150.0))
        Sfx.BLOOP -> listOf(
            Tone(320.0, 0.0, 0.16, 0.5, soft, decay = 12.0, slideTo = 140.0),
            Noise(0.0, 0.2, 0.28, 1300.0, 400.0, decay = 12.0),
        )
        Sfx.TICK -> listOf(Tone(2400.0, 0.0, 0.025, 0.3, soft, decay = 150.0), Tone(1800.0, 0.16, 0.025, 0.25, soft, decay = 150.0))
        Sfx.FIREWORK -> listOf(
            Noise(0.0, 0.1, 0.8, 2200.0, 300.0, decay = 25.0),
            Tone(90.0, 0.0, 0.25, 0.5, soft, decay = 14.0, slideTo = 50.0),
            Crackle(0.12, 1.1, 0.12, 0.4),
        )
        Sfx.COIN -> listOf(
            Tone(1975.5, 0.0, 0.09, 0.4, decay = 18.0),
            Tone(2637.0, 0.07, 0.42, 0.42, decay = 6.0),
        )
        Sfx.PAGE -> listOf(Noise(0.0, 0.2, 0.35, 3200.0, 1500.0, decay = 9.0, attack = 0.03))
        Sfx.SWISH -> listOf(Noise(0.0, 0.4, 0.4, 400.0, 1700.0, swell = true, seed = 3))
        Sfx.OPEN -> listOf(
            Tone(300.0, 0.0, 0.18, 0.3, soft, decay = 9.0, slideTo = 540.0),
            Noise(0.0, 0.08, 0.18, 900.0, decay = 30.0),
        )
        Sfx.SHUT -> listOf(
            Tone(210.0, 0.0, 0.12, 0.5, soft, decay = 25.0, slideTo = 120.0),
            Noise(0.0, 0.05, 0.38, 1600.0, 500.0, decay = 50.0),
        )
        Sfx.VROOM -> listOf(Tone(95.0, 0.0, 0.65, 0.42, buzzy, decay = 1.5, slideTo = 170.0, vibrato = 1.2, vibratoHz = 22.0, attack = 0.03))
        Sfx.MEOW -> listOf(
            Tone(560.0, 0.0, 0.18, 0.4, vowelE, decay = 2.0, slideTo = 860.0, vibrato = 0.2, attack = 0.02),
            Tone(860.0, 0.17, 0.26, 0.4, vowelE, decay = 3.0, slideTo = 520.0, vibrato = 0.3, attack = 0.005),
        )
        Sfx.WOOF -> listOf(
            Tone(330.0, 0.0, 0.13, 0.6, buzzy, decay = 10.0, slideTo = 210.0, attack = 0.005),
            Noise(0.0, 0.07, 0.25, 900.0, 400.0, decay = 30.0),
            Tone(350.0, 0.2, 0.13, 0.55, buzzy, decay = 10.0, slideTo = 220.0, attack = 0.005),
        )
        Sfx.CHIRP -> listOf(
            Tone(1800.0, 0.0, 0.07, 0.35, soft, decay = 10.0, slideTo = 2600.0),
            Tone(2600.0, 0.09, 0.07, 0.3, soft, decay = 10.0, slideTo = 2000.0),
        )
        Sfx.ROAR -> listOf(
            Tone(270.0, 0.0, 0.42, 0.45, buzzy, decay = 2.5, slideTo = 170.0, vibrato = 0.8, vibratoHz = 28.0, attack = 0.02),
            Noise(0.0, 0.42, 0.22, 950.0, 450.0, decay = 3.0),
        )
        Sfx.SNORE -> listOf(
            Noise(0.0, 0.8, 0.3, 300.0, 700.0, swell = true, seed = 4),
            Tone(110.0, 0.0, 0.8, 0.12, buzzy, decay = 0.5, vibrato = 1.5, vibratoHz = 25.0, attack = 0.3),
        )
            Sfx.MOO -> listOf(
            Tone(150.0, 0.0, 0.95, 0.5, vowelO, decay = 1.2, slideTo = 118.0, vibrato = 0.3, vibratoHz = 5.0, attack = 0.09),
            Tone(300.0, 0.0, 0.95, 0.18, buzzy, decay = 1.4, slideTo = 236.0, attack = 0.12),
        )
        Sfx.BAA -> listOf(Tone(430.0, 0.0, 0.55, 0.42, vowelE, decay = 2.0, slideTo = 380.0, vibrato = 1.2, vibratoHz = 9.0, attack = 0.02))
        Sfx.CLUCK -> (0 until 3).flatMap { i ->
            listOf(
                Tone(720.0 - i * 40.0, i * 0.13, 0.07, 0.38, vowelO, decay = 18.0, slideTo = 480.0),
                Noise(i * 0.13, 0.02, 0.2, 2200.0, decay = 90.0, seed = 3 + i),
            )
        }
        Sfx.NEIGH -> listOf(
            Tone(700.0, 0.0, 0.26, 0.34, vowelE, decay = 2.0, slideTo = 1250.0, vibrato = 1.4, vibratoHz = 18.0, attack = 0.02),
            Tone(1250.0, 0.22, 0.55, 0.34, vowelE, decay = 3.0, slideTo = 620.0, vibrato = 2.0, vibratoHz = 16.0),
        )
        Sfx.BEEP -> listOf(
            Tone(1200.0, 0.0, 0.09, 0.35, soft, decay = 8.0),
            Tone(1600.0, 0.1, 0.09, 0.3, soft, decay = 8.0),
        )
        // A deep, rolling «rrraap».
        Sfx.BURP -> listOf(
            Tone(118.0, 0.0, 0.55, 0.55, vowelO, decay = 1.6, slideTo = 86.0, vibrato = 1.6, vibratoHz = 24.0, attack = 0.03),
            Tone(236.0, 0.0, 0.55, 0.25, buzzy, decay = 2.0, slideTo = 170.0, vibrato = 1.6, vibratoHz = 24.0, attack = 0.03),
            Noise(0.0, 0.5, 0.18, 500.0, 300.0, decay = 2.5, seed = 12),
        )
        // «Hikk!»
        Sfx.HICCUP -> listOf(
            Tone(760.0, 0.0, 0.08, 0.5, vowelE, decay = 18.0, slideTo = 1250.0, attack = 0.004),
            Noise(0.0, 0.03, 0.3, 2600.0, decay = 90.0, seed = 13),
        )
        // The whoopee cushion: a long flapping raspberry that droops at the end.
        Sfx.PRRT -> listOf(
            Tone(150.0, 0.0, 0.85, 0.55, buzzy, decay = 1.1, slideTo = 92.0, vibrato = 3.5, vibratoHz = 34.0, attack = 0.02),
            Tone(300.0, 0.0, 0.85, 0.2, buzzy, decay = 1.3, slideTo = 180.0, vibrato = 3.5, vibratoHz = 34.0, attack = 0.02),
            Noise(0.0, 0.8, 0.2, 400.0, 250.0, decay = 1.5, seed = 14),
        )
        // «Ah … ah … ATSJO!»
        Sfx.SNEEZE -> listOf(
            Tone(420.0, 0.0, 0.26, 0.35, vowelE, decay = 2.0, slideTo = 520.0, attack = 0.04),
            Tone(520.0, 0.34, 0.3, 0.4, vowelE, decay = 2.0, slideTo = 680.0, attack = 0.04),
            Noise(0.72, 0.32, 0.9, 5200.0, 1600.0, decay = 8.0, attack = 0.002, seed = 15),
            Tone(620.0, 0.72, 0.28, 0.5, vowelO, decay = 8.0, slideTo = 300.0, attack = 0.002),
        )
        // A slide whistle up, then a woody bonk.
        Sfx.SLIP -> listOf(
            Tone(420.0, 0.0, 0.42, 0.42, soft, decay = 1.0, slideTo = 1700.0, vibrato = 0.2, vibratoHz = 8.0, attack = 0.02),
            Tone(620.0, 0.5, 0.12, 0.5, pluck, decay = 22.0, slideTo = 440.0),
            Noise(0.5, 0.05, 0.3, 1200.0, decay = 60.0, seed = 16),
        )
        Sfx.BONK -> listOf(
            Tone(560.0, 0.0, 0.16, 0.6, pluck, decay = 22.0, slideTo = 420.0),
            Tone(1120.0, 0.0, 0.08, 0.2, soft, decay = 40.0),
            Noise(0.0, 0.03, 0.35, 1800.0, decay = 90.0, seed = 17),
        )
        // A helpless giggle fit.
        Sfx.TICKLE -> syllables(9, 820.0, -22.0, 0.065, 0.025, vowelE, 0.38, seed = 21)
        Sfx.SPLAT -> listOf(
            Noise(0.0, 0.22, 0.8, 900.0, 250.0, decay = 12.0, seed = 18),
            Tone(160.0, 0.0, 0.14, 0.4, soft, decay = 20.0, slideTo = 70.0),
        )
        Sfx.RUMBLE -> listOf(
            Noise(0.0, 1.2, 0.6, 120.0, 320.0, swell = true, seed = 9),
            Tone(55.0, 0.0, 1.2, 0.5, buzzy, decay = 0.6, slideTo = 72.0, vibrato = 2.0, vibratoHz = 12.0, attack = 0.2),
        )

        // ---- Rolf the robot butler: beeps, bops and polite dings ----
        // «Bee-bop-boo-bap»: robot chatter.
        Sfx.FG_ROLF_TALK -> listOf(
            Tone(880.0, 0.0, 0.07, 0.38, hum, decay = 10.0, slideTo = 1100.0),
            Tone(1320.0, 0.09, 0.06, 0.34, hum, decay = 10.0, slideTo = 990.0),
            Tone(660.0, 0.18, 0.09, 0.36, buzzy, decay = 9.0, slideTo = 880.0),
            Tone(1100.0, 0.3, 0.08, 0.34, hum, decay = 10.0, slideTo = 1500.0),
            Tone(760.0, 0.42, 0.1, 0.3, soft, decay = 9.0, slideTo = 620.0),
        )
        // «Ba-ba-ba-ba!»: a staccato robot laugh that climbs.
        Sfx.FG_ROLF_LAUGH -> (0 until 4).flatMap { i ->
            listOf(
                Tone(620.0 + i * 70.0, i * 0.11, 0.07, 0.4, buzzy, decay = 14.0, slideTo = 520.0 + i * 70.0),
                Tone(1240.0 + i * 140.0, i * 0.11, 0.05, 0.12, soft, decay = 18.0),
            )
        }
        // «Bwooop!»: a surprised glide up and a little bip.
        Sfx.FG_ROLF_OOH -> listOf(
            Tone(420.0, 0.0, 0.32, 0.42, hum, decay = 3.0, slideTo = 1500.0, attack = 0.02),
            Tone(840.0, 0.0, 0.3, 0.14, soft, decay = 3.0, slideTo = 3000.0),
            Tone(1500.0, 0.34, 0.06, 0.3, soft, decay = 10.0, slideTo = 1100.0),
        )
        // «Bwomp»: a low, sagging bonk of a robot.
        Sfx.FG_ROLF_OOF -> listOf(
            Tone(520.0, 0.0, 0.25, 0.5, buzzy, decay = 6.0, slideTo = 190.0),
            Tone(260.0, 0.0, 0.25, 0.2, hum, decay = 6.0, slideTo = 95.0),
            Noise(0.0, 0.03, 0.25, 1500.0, decay = 90.0, seed = 33),
        )
        // «Tri-lee-leeee»: content chirps.
        Sfx.FG_ROLF_HAPPY -> listOf(
            Tone(1000.0, 0.0, 0.07, 0.36, soft, decay = 9.0, slideTo = 1300.0),
            Tone(1300.0, 0.08, 0.07, 0.36, soft, decay = 9.0, slideTo = 1700.0),
            Tone(1700.0, 0.16, 0.16, 0.36, soft, decay = 6.0, slideTo = 2100.0, vibrato = 0.4, vibratoHz = 14.0),
        )
        // «Glug-glug … whirrr … pshhh … bzzt … DING!»: the fuel joke.
        Sfx.FG_ROLF_FUEL -> listOf(
            Tone(180.0, 0.0, 0.12, 0.5, vowelO, decay = 12.0, slideTo = 300.0),
            Tone(220.0, 0.16, 0.12, 0.5, vowelO, decay = 12.0, slideTo = 340.0),
            Noise(0.45, 0.7, 0.3, 300.0, 1800.0, swell = true, seed = 34),
            Tone(200.0, 0.45, 0.8, 0.3, buzzy, decay = 1.5, slideTo = 700.0, vibrato = 0.5, vibratoHz = 9.0),
            Noise(1.0, 0.45, 0.4, 5000.0, 2500.0, decay = 4.0, seed = 35),
            Tone(900.0, 1.0, 0.35, 0.22, buzzy, decay = 3.0, slideTo = 400.0, vibrato = 3.0, vibratoHz = 30.0),
            Tone(1760.0, 1.5, 0.55, 0.5, bell, decay = 6.0),
            Tone(2200.0, 1.55, 0.45, 0.22, bell, decay = 7.0),
        )
        // A servo whirr as he folds forward, and a polite ding.
        Sfx.FG_ROLF_BOW -> listOf(
            Noise(0.0, 0.2, 0.22, 700.0, 1500.0, swell = true, seed = 36),
            Tone(300.0, 0.0, 0.2, 0.18, buzzy, decay = 3.0, slideTo = 500.0),
            Tone(1568.0, 0.18, 0.35, 0.45, bell, decay = 7.0),
            Tone(2093.0, 0.22, 0.3, 0.2, bell, decay = 8.0),
        )

        // ---- Sture the ghost: shy, breathy and wobbly ----
        // «Hi-hi-hi-hi»: a shy little giggle.
        Sfx.FG_STURE_GIGGLE -> syllables(6, 1250.0, -45.0, 0.055, 0.035, vowelE, 0.3, seed = 31) +
            Noise(0.0, 0.4, 0.07, 3000.0, 2000.0, swell = true, seed = 37)
        // A soft, wandering «ooo-oo-ooo» hum.
        Sfx.FG_STURE_HUM -> listOf(
            Tone(360.0, 0.0, 0.45, 0.35, vowelO, decay = 2.0, slideTo = 420.0, vibrato = 0.5, vibratoHz = 6.0, attack = 0.05),
            Tone(420.0, 0.5, 0.35, 0.3, vowelO, decay = 2.5, slideTo = 340.0, vibrato = 0.6, vibratoHz = 6.0, attack = 0.05),
        )
        // «Woo-oo-ooh»: the classic, but friendly and a bit wobbly.
        Sfx.FG_STURE_OOH -> listOf(
            Tone(330.0, 0.0, 0.8, 0.45, vowelO, decay = 1.4, slideTo = 520.0, vibrato = 0.7, vibratoHz = 7.0, attack = 0.08),
            Tone(660.0, 0.05, 0.7, 0.14, soft, decay = 1.8, slideTo = 1040.0, vibrato = 0.7, vibratoHz = 7.0),
            Noise(0.0, 0.8, 0.08, 1500.0, 900.0, swell = true, seed = 38),
        )
        // Two quick sniffs of air.
        Sfx.FG_STURE_SNIFF -> listOf(
            Noise(0.0, 0.07, 0.5, 3200.0, 5200.0, decay = 14.0, seed = 41),
            Noise(0.1, 0.08, 0.45, 3400.0, 5400.0, decay = 14.0, seed = 42),
        )
        // «Ah … ah … tsjuuu»: soft, with a puff of dust at the end.
        Sfx.FG_STURE_SNEEZE -> listOf(
            Tone(500.0, 0.0, 0.26, 0.28, vowelO, decay = 2.0, slideTo = 620.0, attack = 0.04),
            Tone(620.0, 0.34, 0.3, 0.32, vowelO, decay = 2.0, slideTo = 760.0, attack = 0.04),
            Noise(0.72, 0.5, 0.7, 3600.0, 900.0, decay = 5.0, attack = 0.002, seed = 45),
            Tone(800.0, 0.72, 0.22, 0.35, vowelO, decay = 8.0, slideTo = 380.0, attack = 0.002),
        )
        // ---- Mitt hus: hammer, saw, drill, crane, plank, bell, crash ----
        Sfx.MI_HAMMER -> listOf(
            Tone(190.0, 0.0, 0.1, 0.7, soft, decay = 26.0, slideTo = 120.0),
            Tone(880.0, 0.0, 0.035, 0.28, soft, decay = 80.0),
            Noise(0.0, 0.03, 0.4, 1900.0, 700.0, decay = 90.0, seed = 31),
        )
        Sfx.MI_SAW -> listOf(
            Noise(0.0, 0.2, 0.5, 900.0, 2600.0, swell = true, seed = 32),
            Noise(0.22, 0.2, 0.5, 2600.0, 900.0, swell = true, seed = 33),
            Tone(330.0, 0.0, 0.42, 0.1, buzzy, decay = 2.0, vibrato = 0.5, vibratoHz = 32.0, attack = 0.03),
        )
        Sfx.MI_DRILL -> listOf(
            Tone(240.0, 0.0, 0.55, 0.38, buzzy, decay = 1.2, slideTo = 360.0, vibrato = 0.5, vibratoHz = 42.0, attack = 0.04),
            Noise(0.0, 0.55, 0.2, 2200.0, 3200.0, decay = 1.2, seed = 34),
        )
        Sfx.MI_PLING -> listOf(
            Tone(1318.5, 0.0, 0.7, 0.45, bell, decay = 4.0),
            Tone(1975.5, 0.06, 0.8, 0.34, bell, decay = 4.0),
            Tone(2637.0, 0.12, 0.9, 0.24, bell, decay = 4.5),
        )
        Sfx.MI_CRANE -> listOf(
            Tone(110.0, 0.0, 0.12, 0.5, soft, decay = 18.0),
            Tone(2200.0, 0.0, 0.02, 0.3, soft, decay = 120.0), Tone(2000.0, 0.09, 0.02, 0.28, soft, decay = 120.0), Tone(2200.0, 0.18, 0.02, 0.28, soft, decay = 120.0),
            Tone(95.0, 0.05, 0.38, 0.2, buzzy, decay = 2.0, slideTo = 130.0, vibrato = 0.4, vibratoHz = 22.0, attack = 0.05),
        )
        Sfx.MI_PLANK -> listOf(
            Tone(150.0, 0.0, 0.16, 0.7, soft, decay = 20.0, slideTo = 90.0),
            Tone(420.0, 0.02, 0.06, 0.25, pluck, decay = 30.0),
            Noise(0.0, 0.1, 0.4, 1300.0, 300.0, decay = 30.0, seed = 35),
        )
        Sfx.MI_DINGDONG -> listOf(
            Tone(784.0, 0.0, 0.9, 0.5, bell, decay = 3.0),
            Tone(1568.0, 0.0, 0.6, 0.15, bell, decay = 4.0),
            Tone(622.3, 0.45, 1.1, 0.5, bell, decay = 2.8),
            Tone(1244.6, 0.45, 0.7, 0.15, bell, decay = 4.0),
        )
        Sfx.MI_CRASH -> listOf(
            Noise(0.0, 0.08, 0.8, 2400.0, 600.0, decay = 40.0, seed = 36),
            Tone(300.0, 0.0, 0.07, 0.4, pluck, decay = 30.0),
            Noise(0.1, 0.07, 0.7, 2000.0, 500.0, decay = 40.0, seed = 37),
            Tone(380.0, 0.1, 0.06, 0.35, pluck, decay = 30.0),
            Noise(0.2, 0.06, 0.6, 2600.0, 700.0, decay = 40.0, seed = 38),
            Noise(0.3, 0.05, 0.5, 3000.0, 900.0, decay = 40.0, seed = 39),
            Tone(260.0, 0.3, 0.06, 0.3, pluck, decay = 30.0),
        )

        // ---- Storhuset ground floor ----
        // The grandfather clock: a deep bell with a woody strike.
        Sfx.GR_BONG -> listOf(
            Tone(196.0, 0.0, 1.8, 0.62, bell, decay = 2.0),
            Tone(392.0, 0.0, 1.3, 0.24, bell, decay = 2.8),
            Tone(98.0, 0.0, 1.2, 0.3, soft, decay = 3.0),
            Noise(0.0, 0.03, 0.3, 1800.0, 600.0, decay = 80.0, seed = 41),
        )
        // The armour: two metallic clanks.
        Sfx.GR_CLANK -> listOf(
            Noise(0.0, 0.08, 0.6, 3400.0, 1500.0, decay = 35.0, seed = 42),
            Tone(880.0, 0.0, 0.22, 0.32, listOf(1.0 to 1.0, 2.76 to 0.55, 5.4 to 0.3, 8.9 to 0.12), decay = 12.0),
            Noise(0.1, 0.07, 0.5, 4000.0, 1800.0, decay = 40.0, seed = 43),
            Tone(1175.0, 0.1, 0.2, 0.28, listOf(1.0 to 1.0, 2.76 to 0.55, 5.4 to 0.3, 8.9 to 0.12), decay = 12.0),
        )
        // An old door that complains.
        Sfx.GR_CREAK -> listOf(
            Tone(180.0, 0.0, 0.6, 0.34, buzzy, decay = 1.2, slideTo = 270.0, vibrato = 1.2, vibratoHz = 22.0, attack = 0.05),
            Tone(250.0, 0.5, 0.45, 0.3, buzzy, decay = 1.4, slideTo = 165.0, vibrato = 1.6, vibratoHz = 27.0, attack = 0.03),
            Noise(0.0, 0.95, 0.1, 600.0, 950.0, swell = true, seed = 44),
        )
        // Crystal prisms knocking together.
        Sfx.GR_TINKLE -> listOf(2093.0, 3136.0, 2637.0, 3951.0, 4699.0, 3520.0, 2794.0, 4186.0, 3322.0).mapIndexed { i, f ->
            Tone(f, i * 0.05, 0.6, 0.15, decay = 5.5)
        }
        // Logs crackling and a low roar.
        Sfx.GR_CRACKLE -> listOf(
            Crackle(0.0, 0.9, 0.22, 0.55, seed = 5),
            Noise(0.0, 0.9, 0.09, 200.0, 360.0, swell = true, seed = 45),
        )
        // Books flapping out of the shelf like startled birds.
        Sfx.GR_FLAP -> (0 until 6).map { i -> Noise(i * 0.065, 0.05, 0.45, 950.0, 320.0, decay = 50.0, seed = 50 + i) }
        // The little brass bell on the table.
        Sfx.GR_BELL -> listOf(
            Tone(2637.0, 0.0, 1.0, 0.45, bell, decay = 4.5),
            Tone(3951.0, 0.0, 0.7, 0.24, bell, decay = 6.5),
            Noise(0.0, 0.015, 0.25, 4000.0, decay = 150.0, seed = 46),
        )
        // Plates and glasses landing.
        Sfx.GR_CLINK -> listOf(
            Tone(3136.0, 0.0, 0.3, 0.3, soft, decay = 18.0),
            Tone(4186.0, 0.03, 0.26, 0.25, soft, decay = 20.0),
            Tone(2349.0, 0.07, 0.34, 0.28, soft, decay = 16.0),
            Tone(3520.0, 0.12, 0.26, 0.2, soft, decay = 22.0),
        )
        // Corn popping in the pan.
        Sfx.GR_POPCORN -> listOf<Voice>(Crackle(0.0, 1.5, 0.36, 0.7, seed = 7)) +
            (0 until 9).map { i -> Tone(480.0 + i * 41.0, 0.02 + i * 0.16, 0.05, 0.3, soft, decay = 40.0, slideTo = 1000.0 + i * 20.0) }
        // The film starts: da-da-daaa.
        Sfx.GR_FILM -> listOf(
            Tone(261.6, 0.0, 0.24, 0.4, brass, decay = 4.0, attack = 0.02),
            Tone(329.6, 0.26, 0.24, 0.4, brass, decay = 4.0, attack = 0.02),
            Tone(392.0, 0.52, 0.24, 0.4, brass, decay = 4.0, attack = 0.02),
            Tone(523.25, 0.8, 1.2, 0.46, brass, decay = 1.8, attack = 0.02),
            Tone(392.0, 0.8, 1.2, 0.2, bell, decay = 2.0),
            Tone(261.6, 0.8, 1.2, 0.2, bell, decay = 2.0),
        )
        // A wood oven taking a breath of fire.
        Sfx.GR_WHOOMP -> listOf(
            Noise(0.0, 0.55, 0.6, 150.0, 720.0, swell = true, seed = 47),
            Tone(80.0, 0.0, 0.45, 0.5, soft, decay = 6.0, slideTo = 45.0),
        )
        // The mixer winding up.
        Sfx.GR_BLEND -> listOf(
            Tone(110.0, 0.0, 1.5, 0.34, buzzy, decay = 0.3, slideTo = 320.0, vibrato = 0.5, vibratoHz = 34.0, attack = 0.1),
            Noise(0.0, 1.5, 0.2, 800.0, 2500.0, decay = 0.3, attack = 0.1, seed = 48),
        )
        // The ladder rolling along its brass rail.
        Sfx.GR_RATTLE -> listOf<Voice>(Noise(0.0, 0.7, 0.24, 300.0, 700.0, swell = true, seed = 13)) +
            (0 until 9).map { i -> Tone(160.0 + (i % 3) * 32.0, i * 0.07, 0.05, 0.3, soft, decay = 40.0) }
        // The talking book: a gravelly, deep mumble.
        Sfx.GR_TALK -> syllables(7, 300.0, 11.0, 0.09, 0.035, vowelO, 0.42, seed = 31)
        // Rolf's «ta-da».
        Sfx.GR_TADA -> listOf(880.0, 1175.0, 1480.0).mapIndexed { i, f -> Tone(f, i * 0.1, 0.09, 0.32, buzzy, decay = 10.0) } +
            Tone(1760.0, 0.3, 0.45, 0.4, bell, decay = 4.0)
        // «Shhhh!»
        Sfx.GR_SHUSH -> listOf(Noise(0.0, 0.7, 0.55, 5200.0, 4300.0, attack = 0.1, swell = true, seed = 33))
        // A hungry plant slurping something down.
        Sfx.GR_SLURP -> listOf(
            Noise(0.0, 0.4, 0.45, 800.0, 2800.0, swell = true, seed = 35),
            Tone(300.0, 0.0, 0.35, 0.3, soft, decay = 6.0, slideTo = 720.0, vibrato = 0.6, vibratoHz = 15.0),
        )

        // ---- Storhuset upper floor ----
        // The toy train: a steam whistle (two blasts), and the chuff of the engine.
        Sfx.UP_WHISTLE -> listOf(
            Tone(988.0, 0.0, 0.34, 0.42, brass, decay = 1.6, slideTo = 940.0, vibrato = 0.12, vibratoHz = 18.0, attack = 0.02),
            Tone(1244.5, 0.0, 0.34, 0.32, brass, decay = 1.6, slideTo = 1190.0, vibrato = 0.12, vibratoHz = 18.0, attack = 0.02),
            Noise(0.0, 0.3, 0.1, 5200.0, 4200.0, decay = 3.0, seed = 31),
            Tone(988.0, 0.42, 0.5, 0.42, brass, decay = 1.2, slideTo = 900.0, vibrato = 0.12, vibratoHz = 18.0, attack = 0.02),
            Tone(1244.5, 0.42, 0.5, 0.32, brass, decay = 1.2, slideTo = 1120.0, vibrato = 0.12, vibratoHz = 18.0, attack = 0.02),
            Noise(0.42, 0.45, 0.1, 5200.0, 3800.0, decay = 3.0, seed = 32),
        )
        Sfx.UP_CHUFF -> listOf(
            Noise(0.0, 0.1, 0.55, 2000.0, 500.0, decay = 28.0, seed = 33),
            Tone(110.0, 0.0, 0.08, 0.3, soft, decay = 30.0, slideTo = 70.0),
        )
        // A tower of wooden blocks coming down: knocks of different pitch, one after the other.
        Sfx.UP_TUMBLE -> listOf(
            Tone(520.0, 0.0, 0.1, 0.55, pluck, decay = 22.0, slideTo = 400.0),
            Noise(0.0, 0.04, 0.4, 1800.0, decay = 80.0, seed = 34),
            Tone(660.0, 0.09, 0.1, 0.5, pluck, decay = 22.0, slideTo = 500.0),
            Tone(440.0, 0.17, 0.1, 0.5, pluck, decay = 22.0, slideTo = 340.0),
            Noise(0.17, 0.04, 0.35, 1500.0, decay = 80.0, seed = 35),
            Tone(590.0, 0.27, 0.1, 0.45, pluck, decay = 22.0, slideTo = 450.0),
            Tone(380.0, 0.36, 0.12, 0.45, pluck, decay = 20.0, slideTo = 300.0),
            Noise(0.36, 0.05, 0.35, 1300.0, decay = 70.0, seed = 36),
            Tone(500.0, 0.47, 0.1, 0.35, pluck, decay = 24.0, slideTo = 400.0),
            Tone(330.0, 0.58, 0.14, 0.3, pluck, decay = 22.0, slideTo = 260.0),
        )
        // A rubber duck's nasal «kvakk-kvakk».
        Sfx.UP_QUACK -> listOf(
            Tone(640.0, 0.0, 0.13, 0.5, buzzy, decay = 12.0, slideTo = 400.0, attack = 0.005),
            Noise(0.0, 0.08, 0.12, 1800.0, 900.0, decay = 25.0, seed = 37),
            Tone(610.0, 0.17, 0.15, 0.5, buzzy, decay = 10.0, slideTo = 380.0, attack = 0.005),
        )
        Sfx.UP_SHOWER -> listOf(
            Noise(0.0, 1.6, 0.34, 6200.0, 7600.0, swell = true, seed = 38),
            Noise(0.0, 1.6, 0.22, 2400.0, 3400.0, swell = true, seed = 39),
        )
        // Plastic balls or a heap of clothes rustling.
        Sfx.UP_RUSTLE -> listOf(
            Noise(0.0, 0.32, 0.45, 3600.0, 1900.0, decay = 9.0, seed = 40),
            Noise(0.07, 0.28, 0.35, 4600.0, 2400.0, decay = 10.0, seed = 41),
            Crackle(0.0, 0.34, 0.22, 0.25),
        )
        Sfx.UP_TWEET -> listOf(
            Tone(2600.0, 0.0, 0.06, 0.32, soft, decay = 14.0, slideTo = 3400.0),
            Tone(3300.0, 0.08, 0.06, 0.3, soft, decay = 14.0, slideTo = 2800.0),
            Tone(2900.0, 0.17, 0.05, 0.28, soft, decay = 14.0, slideTo = 3600.0),
            Tone(3600.0, 0.24, 0.12, 0.3, soft, decay = 9.0, slideTo = 3000.0, vibrato = 0.6, vibratoHz = 30.0),
        )
        // A music box: a high, glassy pluck that the ballerina's tune plays at different pitches.
        Sfx.UP_MUSICBOX -> listOf(
            Tone(1318.5, 0.0, 0.9, 0.38, bell, decay = 4.2),
            Tone(2637.0, 0.0, 0.5, 0.12, bell, decay = 8.0),
            Tone(3951.0, 0.0, 0.3, 0.05, soft, decay = 12.0),
        )
        Sfx.UP_DINGDONG -> listOf(
            Tone(783.99, 0.0, 0.9, 0.45, bell, decay = 3.2),
            Tone(1567.98, 0.0, 0.5, 0.12, bell, decay = 6.0),
            Tone(622.25, 0.5, 1.2, 0.45, bell, decay = 2.6),
            Tone(1244.5, 0.5, 0.6, 0.12, bell, decay = 6.0),
        )
        // A finger squeaking over wet glass.
        Sfx.UP_WIPE -> listOf(
            Noise(0.0, 0.3, 0.26, 3600.0, 5200.0, swell = true, seed = 42),
            Tone(2300.0, 0.0, 0.28, 0.14, soft, decay = 3.0, slideTo = 3100.0, vibrato = 0.5, vibratoHz = 40.0, attack = 0.04),
        )
        Sfx.UP_SPROING -> listOf(
            Tone(180.0, 0.0, 0.5, 0.5, soft, decay = 3.0, slideTo = 820.0, vibrato = 1.2, vibratoHz = 22.0),
            Tone(360.0, 0.0, 0.4, 0.22, soft, decay = 4.0, slideTo = 1500.0, vibrato = 1.2, vibratoHz = 24.0),
        )

        // ---- Storhuset attic ----
        // Old wood that creaks up and down: the rocking horse and the trunk lid.
        Sfx.AT_CREAK -> listOf(
            Tone(150.0, 0.0, 0.3, 0.3, buzzy, decay = 4.0, slideTo = 215.0, vibrato = 1.8, vibratoHz = 26.0, attack = 0.03),
            Tone(215.0, 0.27, 0.32, 0.26, buzzy, decay = 4.0, slideTo = 138.0, vibrato = 1.8, vibratoHz = 24.0, attack = 0.03),
            Noise(0.0, 0.55, 0.07, 800.0, 380.0, decay = 4.0, seed = 31),
        )
        // A gust through the old roof, soft and a little silly.
        Sfx.AT_WIND -> listOf(
            Noise(0.0, 1.4, 0.4, 380.0, 950.0, swell = true, seed = 32),
            Noise(0.1, 1.2, 0.22, 950.0, 520.0, swell = true, seed = 33),
            Tone(300.0, 0.0, 1.4, 0.05, hum, decay = 0.5, slideTo = 390.0, vibrato = 0.6, vibratoHz = 4.0, attack = 0.4),
        )
        // The needle skips across the record.
        Sfx.AT_SCRATCH -> listOf(
            Noise(0.0, 0.14, 0.5, 3200.0, 600.0, decay = 18.0, seed = 34),
            Tone(900.0, 0.0, 0.16, 0.3, buzzy, decay = 14.0, slideTo = 200.0),
            Crackle(0.0, 0.3, 0.45, 0.3, seed = 35),
        )
        // A friendly ghost: «ooOOoo», never scary.
        Sfx.AT_OOO -> listOf(
            Tone(330.0, 0.0, 0.7, 0.4, vowelO, decay = 1.6, slideTo = 500.0, vibrato = 0.9, vibratoHz = 6.0, attack = 0.12),
            Tone(500.0, 0.45, 0.6, 0.35, vowelO, decay = 2.5, slideTo = 300.0, vibrato = 0.9, vibratoHz = 6.0, attack = 0.05),
        )
        // The toy in the box springs out: «doi-oi-oing».
        Sfx.AT_SPRING -> listOf(
            Tone(220.0, 0.0, 0.1, 0.4, pluck, decay = 12.0, slideTo = 700.0),
            Tone(700.0, 0.09, 0.4, 0.4, soft, decay = 5.0, slideTo = 300.0, vibrato = 1.2, vibratoHz = 22.0),
            Tone(420.0, 0.3, 0.3, 0.25, soft, decay = 7.0, slideTo = 560.0, vibrato = 1.0, vibratoHz = 24.0),
        )
        // Knitting needles: tick, tack, tick, tack.
        Sfx.AT_KNIT -> listOf(0.0, 0.09, 0.19, 0.27, 0.39, 0.47).mapIndexed { i, start ->
            Tone(if (i % 2 == 0) 1500.0 else 1150.0, start, 0.04, 0.3, pluck, decay = 60.0)
        }
        // The deep stroke of the grandfather clock.
        Sfx.AT_DONG -> listOf(
            Tone(196.0, 0.0, 1.5, 0.55, bell, decay = 2.2),
            Tone(293.7, 0.0, 1.1, 0.2, bell, decay = 3.0),
            Tone(98.0, 0.0, 1.2, 0.25, soft, decay = 2.8),
        )
        // A shower of coins on coins.
        Sfx.AT_COINS -> (0 until 9).map { i ->
            Tone(2200.0 + (i * 137 % 1300), i * 0.045, 0.2, 0.2, bell, decay = 12.0)
        }

        // ---- Storhuset cellar ----

        // A pipe struck with a spanner: a ringing, slightly out-of-tune clang. Pitched with the playback rate.
        Sfx.CE_CLANG -> listOf(
            Tone(330.0, 0.0, 0.95, 0.5, listOf(1.0 to 1.0, 2.76 to 0.55, 5.4 to 0.3, 8.9 to 0.12), decay = 4.5),
            Tone(247.0, 0.0, 0.6, 0.25, soft, decay = 8.0),
            Noise(0.0, 0.03, 0.45, 3500.0, 1800.0, decay = 90.0, seed = 31),
        )
        // Steam escaping from a pipe.
        Sfx.CE_HISS -> listOf(
            Noise(0.0, 0.95, 0.55, 5200.0, 3600.0, swell = true, seed = 32),
            Noise(0.0, 0.95, 0.22, 2400.0, 1700.0, swell = true, seed = 33),
        )
        // The washing machine's drum: a low wobbling hum and some sloshing.
        Sfx.CE_THRUM -> listOf(
            Tone(72.0, 0.0, 1.4, 0.42, buzzy, decay = 0.25, vibrato = 0.7, vibratoHz = 9.0, attack = 0.1),
            Tone(144.0, 0.0, 1.4, 0.12, soft, decay = 0.3, vibrato = 0.5, vibratoHz = 9.0, attack = 0.1),
            Noise(0.0, 1.4, 0.2, 600.0, 950.0, swell = true, seed = 34),
        )
        // A hand saw: four rasping strokes.
        Sfx.CE_RASP -> (0 until 4).flatMap { i ->
            listOf(
                Noise(i * 0.2, 0.17, 0.5, 2300.0, 1500.0, decay = 5.0, attack = 0.01, seed = 35 + i),
                Tone(230.0, i * 0.2, 0.17, 0.16, buzzy, decay = 6.0, slideTo = 150.0),
            )
        }
        // Rail joints: clack-clack.
        Sfx.CE_CLACK -> listOf(
            Noise(0.0, 0.03, 0.7, 3200.0, 1500.0, decay = 120.0, seed = 39),
            Tone(880.0, 0.0, 0.05, 0.25, soft, decay = 60.0, slideTo = 600.0),
            Noise(0.13, 0.03, 0.6, 3000.0, 1400.0, decay = 120.0, seed = 40),
            Tone(780.0, 0.13, 0.05, 0.22, soft, decay = 60.0, slideTo = 540.0),
        )
        // Charging: a rising electric buzz with crackles.
        Sfx.CE_ZAP -> listOf(
            Tone(120.0, 0.0, 0.55, 0.35, buzzy, decay = 0.8, slideTo = 520.0, vibrato = 2.0, vibratoHz = 40.0, attack = 0.02),
            Crackle(0.0, 0.55, 0.3, 0.4, seed = 41),
        )
        // The jukebox's bass: a round pluck on C3, pitched down with the playback rate.
        Sfx.CE_BASS -> listOf(
            Tone(130.8, 0.0, 0.34, 0.75, listOf(1.0 to 1.0, 2.0 to 0.35, 3.0 to 0.12), decay = 7.0, attack = 0.006),
        )
        // A hi-hat.
        Sfx.CE_HAT -> listOf(Noise(0.0, 0.06, 0.5, 7500.0, 6500.0, decay = 70.0, seed = 42))
        // The boiler's steam whistle.
        Sfx.CE_WHISTLE -> listOf(
            Tone(660.0, 0.0, 0.65, 0.35, listOf(1.0 to 1.0, 2.0 to 0.25, 3.0 to 0.1), decay = 0.8, attack = 0.04, vibrato = 0.15, vibratoHz = 8.0),
            Noise(0.0, 0.65, 0.14, 4500.0, 4500.0, swell = true, seed = 43),
        )
        // A stiff valve wheel turning.
        Sfx.CE_CREAK -> listOf(
            Tone(170.0, 0.0, 0.42, 0.28, buzzy, decay = 1.5, slideTo = 270.0, vibrato = 1.5, vibratoHz = 22.0, attack = 0.05),
            Noise(0.0, 0.4, 0.1, 900.0, 1400.0, swell = true, seed = 44),
        )
        // The confetti cannon: pop and a puff of fizz.
        Sfx.CE_BANG -> listOf(
            Noise(0.0, 0.14, 0.9, 1800.0, 500.0, decay = 22.0, attack = 0.001, seed = 45),
            Tone(110.0, 0.0, 0.22, 0.6, soft, decay = 14.0, slideTo = 60.0),
            Crackle(0.05, 0.3, 0.3, 0.3, seed = 46),
        )
        // The mine cart's bell: ding-ding.
        Sfx.CE_BELL -> listOf(
            Tone(1760.0, 0.0, 0.45, 0.4, bell, decay = 6.0),
            Tone(2349.0, 0.0, 0.4, 0.18, bell, decay = 7.0),
            Tone(1760.0, 0.19, 0.5, 0.4, bell, decay = 6.0),
            Tone(2349.0, 0.19, 0.4, 0.18, bell, decay = 7.0),
        )

        // ---- Storhuset garden ----
        // A frog's «ribbit»: a low buzz that bends up, then a shorter one that sags. Played at a pentatonic rate per frog.
        Sfx.GA_CROAK -> listOf(
            Tone(300.0, 0.0, 0.11, 0.55, buzzy, decay = 9.0, slideTo = 420.0, vibrato = 1.5, vibratoHz = 40.0, attack = 0.008),
            Tone(360.0, 0.13, 0.17, 0.55, buzzy, decay = 6.0, slideTo = 250.0, vibrato = 1.2, vibratoHz = 38.0, attack = 0.006),
            Noise(0.0, 0.05, 0.12, 900.0, 500.0, decay = 40.0, seed = 31),
        )
        // Rope, planks and hinges that complain a little: the swing, the ladder and the gate.
        Sfx.GA_CREAK -> listOf(
            Tone(180.0, 0.0, 0.45, 0.3, buzzy, decay = 2.5, slideTo = 260.0, vibrato = 2.5, vibratoHz = 26.0, attack = 0.03),
            Noise(0.0, 0.45, 0.14, 500.0, 900.0, swell = true, seed = 33),
        )
        // A gnome's wink: two bright plucks and a twinkle.
        Sfx.GA_WINK -> listOf(
            Tone(1760.0, 0.0, 0.1, 0.4, pluck, decay = 14.0),
            Tone(2349.0, 0.09, 0.28, 0.4, bell, decay = 7.0),
            Tone(3520.0, 0.12, 0.2, 0.15, soft, decay = 12.0),
        )
        // A plant shooting up: a rising whistle that ends in two bell notes.
        Sfx.GA_GROW -> listOf(
            Tone(300.0, 0.0, 0.25, 0.4, soft, decay = 4.0, slideTo = 900.0),
            Tone(900.0, 0.2, 0.2, 0.35, bell, decay = 8.0),
            Tone(1350.0, 0.28, 0.35, 0.3, bell, decay = 6.0),
            Noise(0.0, 0.3, 0.1, 1200.0, 3000.0, swell = true, seed = 35),
        )
        // The sprinkler: tsch-tsch-tsch.
        Sfx.GA_SPRAY -> (0 until 5).map { i -> Noise(i * 0.13, 0.12, 0.35, 5200.0, 3400.0, decay = 14.0, seed = 40 + i) } +
            Noise(0.0, 0.7, 0.12, 6500.0, 5800.0, swell = true, seed = 47)
        // A spade in the sand.
        Sfx.GA_DIG -> listOf(
            Noise(0.0, 0.12, 0.5, 1500.0, 500.0, decay = 22.0, seed = 51),
            Noise(0.1, 0.1, 0.4, 1200.0, 450.0, decay = 24.0, seed = 52),
            Tone(140.0, 0.0, 0.08, 0.25, soft, decay = 30.0, slideTo = 90.0),
        )
    }

    fun render(sfx: Sfx): FloatArray {
        val voices = voices(sfx)
        val seconds = voices.maxOf {
            when (it) {
                is Tone -> it.start + it.duration
                is Noise -> it.start + it.duration
                is Crackle -> it.start + it.duration
            }
        } + 0.02
        val out = FloatArray((seconds * SAMPLE_RATE).toInt())
        for (voice in voices) {
            when (voice) {
                is Tone -> addTone(out, voice)
                is Noise -> addNoise(out, voice)
                is Crackle -> addCrackle(out, voice)
            }
        }
        if (sfx == Sfx.TAP || sfx == Sfx.CLICK) {
            // A short, seeded contact transient gives buttons a real edge.
            val random = java.util.Random(17)
            val count = (SAMPLE_RATE * 0.012).toInt()
            for (i in 0 until count) {
                val envelope = exp(-i.toDouble() / (SAMPLE_RATE * 0.0025))
                out[i] += ((random.nextDouble() * 2 - 1) * envelope * 0.12).toFloat()
            }
        }
        val peak = out.maxOfOrNull { abs(it) } ?: 0f
        if (peak > 0f) {
            val scale = 0.8f / peak
            for (i in out.indices) out[i] *= scale
        }
        return out
    }

    private fun addTone(out: FloatArray, tone: Tone) {
        val offset = (tone.start * SAMPLE_RATE).toInt()
        val count = (tone.duration * SAMPLE_RATE).toInt()
        val phases = DoubleArray(tone.partials.size)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            var frequency = tone.slideTo?.let { tone.frequency * (it / tone.frequency).pow(progress) } ?: tone.frequency
            if (tone.vibrato != 0.0) frequency *= 2.0.pow(tone.vibrato / 12.0 * sin(2.0 * PI * tone.vibratoHz * t))
            val attack = min(1.0, t / tone.attack)
            val release = min(1.0, (tone.duration - t) / 0.025).coerceAtLeast(0.0)
            val envelope = attack * release * exp(-tone.decay * t)
            var sample = 0.0
            tone.partials.forEachIndexed { k, (ratio, level) ->
                phases[k] += 2.0 * PI * frequency * ratio / SAMPLE_RATE
                sample += sin(phases[k]) * level
            }
            val index = offset + i
            if (index < out.size) out[index] += (sample * envelope * tone.gain).toFloat()
        }
    }

    /** White noise through a state-variable band-pass whose centre glides from [Noise.from] to [Noise.to]. */
    private fun addNoise(out: FloatArray, noise: Noise) {
        val offset = (noise.start * SAMPLE_RATE).toInt()
        val count = (noise.duration * SAMPLE_RATE).toInt()
        val random = java.util.Random(noise.seed.toLong())
        var low = 0.0
        var band = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / count
            val cutoff = noise.from * (noise.to / noise.from).pow(progress)
            val f = 2.0 * sin(PI * min(cutoff, SAMPLE_RATE / 4.0) / SAMPLE_RATE)
            val input = random.nextDouble() * 2.0 - 1.0
            low += f * band
            val high = input - low - 0.7 * band
            band += f * high
            val envelope = if (noise.swell) {
                val s = sin(PI * progress)
                s * s
            } else {
                min(1.0, t / noise.attack) * exp(-noise.decay * t) * min(1.0, (noise.duration - t) / 0.02).coerceAtLeast(0.0)
            }
            val index = offset + i
            if (index < out.size) out[index] += (band * envelope * noise.gain * 2.2).toFloat()
        }
    }

    /** Sparse little clicks, like embers or fireworks crackling. */
    private fun addCrackle(out: FloatArray, crackle: Crackle) {
        val offset = (crackle.start * SAMPLE_RATE).toInt()
        val count = (crackle.duration * SAMPLE_RATE).toInt()
        val random = java.util.Random(crackle.seed.toLong())
        val step = (SAMPLE_RATE * 0.004).toInt()
        var i = 0
        while (i < count) {
            if (random.nextDouble() < crackle.density) {
                val amplitude = (0.4 + random.nextDouble() * 0.6) * crackle.gain * (1.0 - i.toDouble() / count)
                val length = (SAMPLE_RATE * (0.002 + random.nextDouble() * 0.004)).toInt()
                for (k in 0 until length) {
                    val index = offset + i + k
                    if (index >= out.size) break
                    out[index] += ((random.nextDouble() * 2 - 1) * amplitude * exp(-k.toDouble() / (length * 0.3))).toFloat()
                }
            }
            i += step
        }
    }

    /** 16-bit mono PCM in a RIFF/WAVE container. */
    fun wav(samples: FloatArray): ByteArray {
        val dataSize = samples.size * 2
        val stream = ByteArrayOutputStream(44 + dataSize)
        fun int(value: Int) {
            stream.write(value and 0xFF)
            stream.write((value shr 8) and 0xFF)
            stream.write((value shr 16) and 0xFF)
            stream.write((value shr 24) and 0xFF)
        }
        fun short(value: Int) {
            stream.write(value and 0xFF)
            stream.write((value shr 8) and 0xFF)
        }
        stream.write("RIFF".toByteArray(Charsets.US_ASCII))
        int(36 + dataSize)
        stream.write("WAVE".toByteArray(Charsets.US_ASCII))
        stream.write("fmt ".toByteArray(Charsets.US_ASCII))
        int(16)
        short(1)
        short(1)
        int(SAMPLE_RATE)
        int(SAMPLE_RATE * 2)
        short(2)
        short(16)
        stream.write("data".toByteArray(Charsets.US_ASCII))
        int(dataSize)
        for (sample in samples) {
            short((sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt())
        }
        return stream.toByteArray()
    }
}
