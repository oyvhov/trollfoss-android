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

    // Storhuset. Each floor adds its sounds in its own block, and their recipes in [Synth.voices] below.
    // ---- ground floor ----

    // ---- upper floor ----

    // ---- attic ----

    // ---- cellar ----

    // ---- garden ----
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

        // ---- Storhuset ground floor ----

        // ---- Storhuset upper floor ----

        // ---- Storhuset attic ----

        // ---- Storhuset cellar ----

        // ---- Storhuset garden ----
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
