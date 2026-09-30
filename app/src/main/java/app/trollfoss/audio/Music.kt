package app.trollfoss.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** One loop per place, one for the map, a lullaby for the night and dance music for the radio. */
enum class MusicTheme { MAP, HOME, CAFE, SALON, BEACH, FOREST, LAB, PARK, FARM, SPACE, RADIO, NIGHT }

/**
 * Composes calm, looping background music in code: chords on a soft pad, a plucked arpeggio or bell
 * melody, a bass line and, for the livelier places, soft drums. The loop is seamless because every note
 * and echo that runs past the end wraps round to the start. Same engine as Komet, new songs.
 */
object MusicComposer {
    const val SAMPLE_RATE = 22_050

    private class Recipe(
        val bpm: Double,
        /** Chord tones per bar, as MIDI notes (60 = middle C). */
        val chords: List<IntArray>,
        /** One bass note per bar. */
        val bass: IntArray,
        /** Chord-tone index per eighth note, -1 for a rest; null for no arpeggio. */
        val arpeggio: IntArray? = null,
        val arpeggioShift: Int = 12,
        val pluckDecay: Double = 6.0,
        val arpeggioLevel: Double = 0.2,
        /** Notes the bell melody may use; empty for none. */
        val bells: IntArray = IntArray(0),
        val bellChance: Double = 0.5,
        val drums: Boolean = false,
        /** A kick on every beat, for the radio's dance music. */
        val fourOnFloor: Boolean = false,
        val padLevel: Double = 0.16,
        val seed: Int = 1,
    )

    private val recipes: Map<MusicTheme, Recipe> = mapOf(
        // Adventurous and bright: flying over the island.
        MusicTheme.MAP to Recipe(
            bpm = 92.0,
            chords = listOf(intArrayOf(62, 66, 69), intArrayOf(57, 61, 64, 69), intArrayOf(59, 62, 66), intArrayOf(55, 59, 62, 67)),
            bass = intArrayOf(38, 33, 35, 31),
            arpeggio = intArrayOf(0, 1, 2, 1, 0, 2, 1, -1),
            pluckDecay = 5.0,
            bells = intArrayOf(74, 76, 78, 81, 83, 86),
            bellChance = 0.35,
            padLevel = 0.12,
            seed = 5,
        ),
        // Cosy and warm: home.
        MusicTheme.HOME to Recipe(
            bpm = 80.0,
            chords = listOf(intArrayOf(60, 64, 67, 71), intArrayOf(57, 60, 64, 67), intArrayOf(53, 57, 60, 64), intArrayOf(55, 59, 62, 65)),
            bass = intArrayOf(36, 33, 29, 31),
            arpeggio = intArrayOf(0, 2, 1, 3, 0, 2, 1, -1),
            pluckDecay = 4.0,
            arpeggioLevel = 0.16,
            bells = intArrayOf(76, 79, 81, 84),
            bellChance = 0.2,
            padLevel = 0.14,
            seed = 9,
        ),
        // A little jazzy: the café.
        MusicTheme.CAFE to Recipe(
            bpm = 96.0,
            chords = listOf(intArrayOf(62, 65, 69, 72), intArrayOf(55, 59, 62, 65), intArrayOf(60, 64, 67, 71), intArrayOf(57, 61, 64, 67)),
            bass = intArrayOf(38, 31, 36, 33),
            arpeggio = intArrayOf(0, -1, 2, 1, -1, 3, 2, -1),
            pluckDecay = 7.0,
            drums = true,
            padLevel = 0.1,
            seed = 13,
        ),
        // Bouncy pop: the salon.
        MusicTheme.SALON to Recipe(
            bpm = 112.0,
            chords = listOf(intArrayOf(65, 69, 72), intArrayOf(60, 64, 67), intArrayOf(62, 65, 69), intArrayOf(58, 62, 65)),
            bass = intArrayOf(41, 36, 38, 34),
            arpeggio = intArrayOf(0, 1, 2, 1, 0, 1, 2, 1),
            arpeggioShift = 12,
            pluckDecay = 9.0,
            drums = true,
            padLevel = 0.08,
            seed = 17,
        ),
        // Marimba in the sun: the beach.
        MusicTheme.BEACH to Recipe(
            bpm = 100.0,
            chords = listOf(intArrayOf(60, 64, 67), intArrayOf(65, 69, 72), intArrayOf(67, 71, 74), intArrayOf(60, 64, 67)),
            bass = intArrayOf(36, 41, 43, 36),
            arpeggio = intArrayOf(0, 2, 1, 2, 0, 2, 1, 2),
            arpeggioShift = 12,
            pluckDecay = 11.0,
            arpeggioLevel = 0.22,
            drums = true,
            padLevel = 0.07,
            seed = 21,
        ),
        // Calm pentatonic bells: the forest.
        MusicTheme.FOREST to Recipe(
            bpm = 66.0,
            chords = listOf(intArrayOf(57, 60, 64), intArrayOf(53, 57, 60), intArrayOf(48, 52, 55, 60), intArrayOf(55, 59, 62)),
            bass = intArrayOf(45, 41, 36, 43),
            bells = intArrayOf(69, 72, 74, 76, 79, 81),
            bellChance = 0.4,
            padLevel = 0.2,
            seed = 29,
        ),
        // Mysterious minor: the troll lab.
        MusicTheme.LAB to Recipe(
            bpm = 72.0,
            chords = listOf(intArrayOf(64, 67, 71), intArrayOf(60, 64, 67), intArrayOf(57, 60, 64), intArrayOf(59, 63, 66, 69)),
            bass = intArrayOf(40, 36, 33, 35),
            arpeggio = intArrayOf(0, -1, 1, -1, 2, -1, 1, -1),
            pluckDecay = 3.0,
            arpeggioLevel = 0.15,
            bells = intArrayOf(76, 79, 83, 84, 88),
            bellChance = 0.3,
            padLevel = 0.18,
            seed = 37,
        ),
        // Sunny and quick: the park.
        MusicTheme.PARK to Recipe(
            bpm = 116.0,
            chords = listOf(intArrayOf(67, 71, 74), intArrayOf(62, 66, 69), intArrayOf(64, 67, 71), intArrayOf(60, 64, 67)),
            bass = intArrayOf(43, 38, 40, 36),
            arpeggio = intArrayOf(0, 1, 2, 1, 0, 2, 1, 2),
            pluckDecay = 8.0,
            drums = true,
            padLevel = 0.08,
            seed = 41,
        ),
        // Bouncy and folksy: the farm.
        MusicTheme.FARM to Recipe(
            bpm = 104.0,
            chords = listOf(intArrayOf(62, 66, 69), intArrayOf(67, 71, 74), intArrayOf(62, 66, 69), intArrayOf(69, 73, 76)),
            bass = intArrayOf(38, 43, 38, 45),
            arpeggio = intArrayOf(0, -1, 2, 1, 0, -1, 2, 1),
            arpeggioShift = 12,
            pluckDecay = 7.0,
            arpeggioLevel = 0.2,
            drums = true,
            padLevel = 0.08,
            seed = 59,
        ),
        // Floating and wide: the space station.
        MusicTheme.SPACE to Recipe(
            bpm = 58.0,
            chords = listOf(intArrayOf(57, 64, 69, 71), intArrayOf(53, 60, 65, 67), intArrayOf(55, 62, 67, 69), intArrayOf(52, 59, 64, 66)),
            bass = intArrayOf(33, 29, 31, 28),
            bells = intArrayOf(76, 79, 81, 83, 86, 88, 91),
            bellChance = 0.45,
            padLevel = 0.22,
            seed = 61,
        ),
        // Dance music for the radio.
        MusicTheme.RADIO to Recipe(
            bpm = 124.0,
            chords = listOf(intArrayOf(57, 60, 64), intArrayOf(53, 57, 60), intArrayOf(60, 64, 67), intArrayOf(55, 59, 62)),
            bass = intArrayOf(45, 41, 48, 43),
            arpeggio = intArrayOf(0, 2, 1, 2, 0, 2, 1, 2),
            arpeggioShift = 12,
            pluckDecay = 10.0,
            arpeggioLevel = 0.22,
            drums = true,
            fourOnFloor = true,
            padLevel = 0.07,
            seed = 47,
        ),
        // A lullaby for the night.
        MusicTheme.NIGHT to Recipe(
            bpm = 60.0,
            chords = listOf(intArrayOf(53, 57, 60, 64), intArrayOf(52, 55, 59, 62), intArrayOf(50, 53, 57, 60), intArrayOf(48, 52, 55, 59)),
            bass = intArrayOf(41, 40, 38, 36),
            bells = intArrayOf(72, 74, 76, 79, 81),
            bellChance = 0.3,
            padLevel = 0.2,
            seed = 53,
        ),
    )

    private fun frequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    /** Renders the loop for [theme] as 16-bit mono PCM at [SAMPLE_RATE]; eight bars long. */
    fun render(theme: MusicTheme): ShortArray {
        val recipe = recipes.getValue(theme)
        val beat = SAMPLE_RATE * 60.0 / recipe.bpm
        val bars = recipe.chords.size * 2
        val length = (bars * 4 * beat).roundToInt()
        val mix = FloatArray(length)
        val random = Random(recipe.seed)

        for (bar in 0 until bars) {
            val chord = recipe.chords[bar % recipe.chords.size]
            val barStart = (bar * 4 * beat).roundToInt()
            for (note in chord) {
                addTone(mix, barStart, (4.6 * beat).roundToInt(), frequency(note), recipe.padLevel, Voice.PAD)
            }
            val root = recipe.bass[bar % recipe.bass.size]
            addTone(mix, barStart, (1.8 * beat).roundToInt(), frequency(root), 0.34, Voice.BASS)
            addTone(mix, barStart + (2 * beat).roundToInt(), (1.6 * beat).roundToInt(), frequency(root + if (recipe.fourOnFloor) 12 else 0), 0.24, Voice.BASS)

            recipe.arpeggio?.let { pattern ->
                for (step in 0 until 8) {
                    val index = pattern[step]
                    if (index < 0) continue
                    val note = chord[index % chord.size] + recipe.arpeggioShift
                    val start = barStart + (step * beat / 2).roundToInt()
                    addTone(mix, start, (0.9 * beat).roundToInt(), frequency(note), recipe.arpeggioLevel, Voice.PLUCK, recipe.pluckDecay)
                }
            }
            if (recipe.bells.isNotEmpty()) {
                for (half in 0 until 4) {
                    if (random.nextDouble() > recipe.bellChance) continue
                    val note = recipe.bells[random.nextInt(recipe.bells.size)]
                    val start = barStart + (half * beat).roundToInt()
                    addTone(mix, start, (3 * beat).roundToInt(), frequency(note), 0.12, Voice.BELL)
                }
            }
            if (recipe.drums) {
                for (b in 0 until 4) {
                    val start = barStart + (b * beat).roundToInt()
                    if (recipe.fourOnFloor || b % 2 == 0) addKick(mix, start, 0.32)
                    addHat(mix, start + (beat / 2).roundToInt(), 0.045, random)
                    if (recipe.fourOnFloor && b % 2 == 1) addHat(mix, start, 0.07, random)
                }
            }
        }

        val echoed = echo(mix, delay = (0.75 * beat).roundToInt(), feedback = 0.3f, wet = 0.3f)
        val warm = lowPass(echoed, 0.42f)
        val peak = warm.maxOf { abs(it) }.takeIf { it > 0f } ?: 1f
        val scale = 0.55f * Short.MAX_VALUE / peak
        return ShortArray(length) { (warm[it] * scale).roundToInt().coerceIn(-32767, 32767).toShort() }
    }

    private enum class Voice { PAD, BASS, PLUCK, BELL }

    private fun addTone(mix: FloatArray, start: Int, length: Int, frequency: Double, gain: Double, voice: Voice, decay: Double = 6.0) {
        val n = mix.size
        val partials: DoubleArray
        val levels: DoubleArray
        when (voice) {
            Voice.PAD -> { partials = doubleArrayOf(1.0, 1.003, 2.0, 0.997); levels = doubleArrayOf(0.6, 0.6, 0.18, 0.4) }
            Voice.BASS -> { partials = doubleArrayOf(1.0, 2.0); levels = doubleArrayOf(1.0, 0.22) }
            Voice.PLUCK -> { partials = doubleArrayOf(1.0, 4.0, 10.0); levels = doubleArrayOf(1.0, 0.14, 0.03) }
            Voice.BELL -> { partials = doubleArrayOf(1.0, 2.76, 5.4); levels = doubleArrayOf(1.0, 0.28, 0.1) }
        }
        val step = DoubleArray(partials.size) { 2.0 * PI * frequency * partials[it] / SAMPLE_RATE }
        val attack = when (voice) {
            Voice.PAD -> 0.9 * SAMPLE_RATE
            Voice.BASS -> 0.02 * SAMPLE_RATE
            Voice.PLUCK -> 0.003 * SAMPLE_RATE
            Voice.BELL -> 0.006 * SAMPLE_RATE
        }
        val release = if (voice == Voice.PAD) 1.1 * SAMPLE_RATE else 0.08 * SAMPLE_RATE
        for (i in 0 until length) {
            val t = i.toDouble() / SAMPLE_RATE
            var envelope = min(1.0, i / max(1.0, attack)) * min(1.0, (length - i) / release)
            envelope *= when (voice) {
                Voice.PAD -> 1.0
                Voice.BASS -> exp(-0.9 * t)
                Voice.PLUCK -> exp(-decay * t)
                Voice.BELL -> exp(-1.7 * t)
            }
            if (envelope <= 0.0) continue
            var sample = 0.0
            for (k in partials.indices) sample += sin(step[k] * i) * levels[k]
            val index = (start + i) % n
            mix[index] += (sample * envelope * gain).toFloat()
        }
    }

    private fun addKick(mix: FloatArray, start: Int, gain: Double) {
        val length = (0.18 * SAMPLE_RATE).toInt()
        var phase = 0.0
        for (i in 0 until length) {
            val t = i.toDouble() / SAMPLE_RATE
            val frequency = 45.0 + 75.0 * exp(-30.0 * t)
            phase += 2.0 * PI * frequency / SAMPLE_RATE
            mix[(start + i) % mix.size] += (sin(phase) * exp(-22.0 * t) * gain).toFloat()
        }
    }

    private fun addHat(mix: FloatArray, start: Int, gain: Double, random: Random) {
        val length = (0.05 * SAMPLE_RATE).toInt()
        var previous = 0.0
        for (i in 0 until length) {
            val t = i.toDouble() / SAMPLE_RATE
            val noise = random.nextDouble() * 2 - 1
            val bright = noise - previous
            previous = noise
            mix[(start + i) % mix.size] += (bright * exp(-70.0 * t) * gain).toFloat()
        }
    }

    private fun echo(input: FloatArray, delay: Int, feedback: Float, wet: Float): FloatArray {
        val n = input.size
        val echoes = input.copyOf()
        repeat(5) {
            for (i in 0 until n) echoes[i] = input[i] + feedback * echoes[(i - delay).mod(n)]
        }
        return FloatArray(n) { input[it] + wet * (echoes[it] - input[it]) }
    }

    private fun lowPass(input: FloatArray, alpha: Float): FloatArray {
        val out = FloatArray(input.size)
        var state = 0f
        repeat(2) {
            for (i in input.indices) {
                state += alpha * (input[i] - state)
                out[i] = state
            }
        }
        return out
    }
}
