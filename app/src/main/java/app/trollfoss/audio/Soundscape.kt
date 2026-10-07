package app.trollfoss.audio

import app.trollfoss.domain.PlaceId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** The soft sound underneath the music: what a place sounds like when nobody is playing anything. */
enum class SoundBed { WIND, WAVES, FALL, BIRDS, CRICKETS, ROOM, MURMUR, DRIPS, DRONE, SEA }

/**
 * Looping soundscapes made in code, like the music: filtered noise, a few chirps, a ticking clock. Every bed is
 * [SECONDS] long and loops without a click (the end is faded into the start). [bedFor] picks the bed for a place and
 * [mapGain] makes the waterfall on the map louder with each level.
 */
object Soundscape {
    const val RATE = MusicComposer.SAMPLE_RATE
    const val SECONDS = 8
    private const val LENGTH = RATE * SECONDS
    private const val OVERLAP = RATE / 2

    /** The bed for [place]; on the map it is always the waterfall. Night turns birdsong into crickets. */
    fun bedFor(place: PlaceId, night: Boolean, onMap: Boolean): SoundBed {
        if (onMap) return SoundBed.FALL
        return when (place) {
            PlaceId.HOME, PlaceId.SALON, PlaceId.DOCTOR, PlaceId.LAB, PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER,
            PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> SoundBed.ROOM
            PlaceId.CAFE, PlaceId.SHOP, PlaceId.STAGE -> SoundBed.MURMUR
            PlaceId.BEACH -> SoundBed.WAVES
            PlaceId.MOUNTAIN, PlaceId.HEILEBERGET, PlaceId.CLOUD_ISLAND, PlaceId.MANOR_ATTIC -> SoundBed.WIND
            PlaceId.SPACE -> SoundBed.DRONE
            PlaceId.UNDERWATER -> SoundBed.SEA
            PlaceId.MANOR_CELLAR -> SoundBed.DRIPS
            PlaceId.FOREST, PlaceId.FARM, PlaceId.TIVOLI, PlaceId.MANOR_GARDEN, PlaceId.MINE_YARD, PlaceId.VAGSTADDALEN ->
                if (night) SoundBed.CRICKETS else SoundBed.BIRDS
        }
    }

    /** How loud the waterfall on the map is at [level] (1 and up): a soft rush at first, a roar at the top. */
    fun mapGain(level: Int): Float = (0.3f + 0.08f * (level.coerceIn(1, 10) - 1)).coerceAtMost(1f)

    /** How loud a bed plays, as a share of the full level. */
    fun gain(bed: SoundBed, level: Int): Float = when (bed) {
        SoundBed.FALL -> mapGain(level)
        SoundBed.ROOM, SoundBed.DRONE -> 0.6f
        else -> 0.8f
    }

    /** One loop of [bed] as 16-bit mono PCM at [RATE]. The same bed always sounds the same. */
    fun render(bed: SoundBed): ShortArray {
        val random = Random(bed.ordinal * 7919 + 13)
        val raw = FloatArray(LENGTH + OVERLAP)
        when (bed) {
            SoundBed.WIND -> wind(raw, random, 1f)
            SoundBed.WAVES -> waves(raw, random)
            SoundBed.FALL -> fall(raw, random)
            SoundBed.BIRDS -> { wind(raw, random, 0.3f); chirps(raw, random) }
            SoundBed.CRICKETS -> { wind(raw, random, 0.22f); crickets(raw, random) }
            SoundBed.ROOM -> room(raw, random)
            SoundBed.MURMUR -> murmur(raw, random)
            SoundBed.DRIPS -> drips(raw, random)
            SoundBed.DRONE -> drone(raw, random)
            SoundBed.SEA -> sea(raw, random)
        }
        val loop = loopable(raw)
        val peak = loop.maxOf { abs(it) }.takeIf { it > 0f } ?: 1f
        val scale = 0.6f * Short.MAX_VALUE / peak
        return ShortArray(LENGTH) { (loop[it] * scale).roundToInt().coerceIn(-32767, 32767).toShort() }
    }

    // ------------------------------------------------------------------ beds

    private fun t(i: Int) = i.toDouble() / RATE

    private fun wind(out: FloatArray, random: Random, level: Float) {
        val a = lowPass(white(out.size, random), 0.05f); val b = lowPass(white(out.size, random), 0.22f)
        for (i in out.indices) {
            val swell = (0.55 + 0.45 * sin(2 * PI * 0.125 * t(i)) * sin(2 * PI * 0.0625 * t(i) + 1.3)).toFloat()
            val gust = (0.5 + 0.5 * sin(2 * PI * 0.25 * t(i) + 0.4)).toFloat()
            out[i] += level * (a[i] * 2.2f * swell + b[i] * 0.45f * swell * gust)
        }
    }

    private fun waves(out: FloatArray, random: Random) {
        val body = lowPass(white(out.size, random), 0.1f); val foam = highPass(lowPass(white(out.size, random), 0.5f), 0.9f)
        for (i in out.indices) {
            val w = (0.5 + 0.5 * sin(2 * PI * t(i) / SECONDS * 1.0 - 1.2)).pow(2.0).toFloat() * 0.75f + 0.25f
            val w2 = (0.5 + 0.5 * sin(2 * PI * t(i) / SECONDS * 2.0 + 0.7)).toFloat() * 0.2f
            out[i] += body[i] * 2.6f * (w + w2) + foam[i] * 1.4f * w * w
        }
    }

    private fun fall(out: FloatArray, random: Random) {
        val roar = lowPass(white(out.size, random), 0.33f); val hiss = highPass(lowPass(white(out.size, random), 0.8f), 0.8f)
        for (i in out.indices) {
            val m = (1.0 + 0.12 * sin(2 * PI * t(i) / SECONDS * 3.0) + 0.06 * sin(2 * PI * t(i) / SECONDS * 7.0 + 1.0)).toFloat()
            out[i] += (roar[i] * 1.7f + hiss[i] * 0.9f) * m
        }
    }

    private fun chirps(out: FloatArray, random: Random) {
        var at = 0.4
        while (at < SECONDS - 0.8) {
            val notes = 2 + random.nextInt(3); val base = 2600.0 + random.nextInt(1800)
            for (n in 0 until notes) tone(out, at + n * 0.13, 0.09, base + n * 220.0, base + n * 220.0 + 600.0, 0.5f)
            at += 0.9 + random.nextDouble() * 1.8
        }
    }

    private fun crickets(out: FloatArray, random: Random) {
        for ((c, carrier) in doubleArrayOf(4300.0, 4850.0).withIndex()) {
            var at = random.nextDouble() * 0.4
            while (at < SECONDS - 0.6) {
                for (p in 0 until 4) tone(out, at + p * 0.055, 0.03, carrier, carrier, 0.22f - c * 0.05f)
                at += 0.42 + c * 0.1 + random.nextDouble() * 0.12
            }
        }
    }

    private fun room(out: FloatArray, random: Random) {
        val hum = lowPass(white(out.size, random), 0.018f)
        for (i in out.indices) out[i] += hum[i] * 1.3f
        // A clock on the wall: tick, tock, once a second.
        for (s in 0 until SECONDS + 1) {
            val at = s + 0.0; val high = s % 2 == 0
            for (i in 0 until (RATE * 0.04).toInt()) {
                val j = ((at * RATE).toInt() + i).takeIf { it < out.size } ?: break
                val env = exp(-i / (RATE * 0.006)).toFloat()
                out[j] += env * 0.45f * sin(2 * PI * (if (high) 2400.0 else 1800.0) * i / RATE).toFloat()
            }
        }
    }

    private fun murmur(out: FloatArray, random: Random) {
        val voices = highPass(lowPass(white(out.size, random), 0.25f), 0.93f)
        // Slow, random «syllables» so it sounds like talk in the next room.
        val syl = FloatArray(out.size); var level = 0.5f; var target = 0.5f
        for (i in out.indices) {
            if (i % (RATE / 5) == 0) target = 0.15f + random.nextFloat() * 0.85f
            level += (target - level) * 0.0007f; syl[i] = level
        }
        for (i in out.indices) out[i] += voices[i] * syl[i] * 3.2f
        for (k in 0 until 2) tone(out, 1.5 + k * 3.6 + random.nextDouble(), 0.35, 3200.0, 3150.0, 0.14f, decay = 12.0)
    }

    private fun drips(out: FloatArray, random: Random) {
        val hum = lowPass(white(out.size, random), 0.04f)
        for (i in out.indices) out[i] += hum[i] * 0.9f
        var at = 0.6
        while (at < SECONDS - 0.9) {
            val f0 = 900.0 + random.nextInt(600)
            tone(out, at, 0.3, f0, f0 * 0.8, 0.6f, decay = 11.0)
            tone(out, at + 0.22, 0.3, f0, f0 * 0.8, 0.22f, decay = 11.0)
            at += 1.6 + random.nextDouble() * 1.6
        }
    }

    private fun drone(out: FloatArray, random: Random) {
        val air = lowPass(white(out.size, random), 0.02f)
        for (i in out.indices) {
            val x = t(i)
            val beat = (0.6 + 0.4 * sin(2 * PI * x / SECONDS * 2.0)).toFloat()
            out[i] += (0.5f * sin(2 * PI * 55.0 * x).toFloat() + 0.45f * sin(2 * PI * 55.5 * x).toFloat() * beat + 0.2f * sin(2 * PI * 82.5 * x).toFloat()) * 0.7f + air[i] * 0.9f
        }
        for (k in 0 until 3) tone(out, 1.0 + k * 2.4 + random.nextDouble(), 0.7, 1500.0 + random.nextInt(900), 1500.0 + random.nextInt(900), 0.09f, decay = 5.0)
    }

    private fun sea(out: FloatArray, random: Random) {
        val deep = lowPass(white(out.size, random), 0.014f)
        for (i in out.indices) out[i] += deep[i] * 2.4f * (0.75f + 0.25f * sin(2 * PI * t(i) / SECONDS * 2.0).toFloat())
        for (k in 0 until 6) { val at = 0.5 + k * 1.2 + random.nextDouble() * 0.5; val f0 = 320.0 + random.nextInt(260); tone(out, at, 0.12, f0, f0 * 2.6, 0.3f) }
    }

    // ------------------------------------------------------------------ pieces

    /** A short sine that slides from [from] to [to] Hz; [decay] shapes how fast it dies away. */
    private fun tone(out: FloatArray, start: Double, length: Double, from: Double, to: Double, gain: Float, decay: Double = 0.0) {
        val first = (start * RATE).toInt(); val n = (length * RATE).toInt(); var phase = 0.0
        for (i in 0 until n) {
            val j = first + i
            if (j >= out.size) return
            val k = i.toDouble() / n
            phase += 2 * PI * (from + (to - from) * k) / RATE
            val env = if (decay > 0.0) exp(-k * decay) * (1.0 - exp(-i / (RATE * 0.002))) else sin(PI * k)
            out[j] += (sin(phase) * env * gain).toFloat()
        }
    }

    private fun white(n: Int, random: Random) = FloatArray(n) { random.nextFloat() * 2f - 1f }

    private fun lowPass(input: FloatArray, alpha: Float): FloatArray {
        val out = FloatArray(input.size); var y = 0f
        for (i in input.indices) { y += alpha * (input[i] - y); out[i] = y }
        return out
    }

    private fun highPass(input: FloatArray, alpha: Float): FloatArray {
        val out = FloatArray(input.size); var low = 0f
        for (i in input.indices) { low += (1f - alpha) * (input[i] - low); out[i] = input[i] - low }
        return out
    }

    /** Fades the extra tail into the start so the last sample runs straight on into the first. */
    private fun loopable(raw: FloatArray): FloatArray {
        val out = raw.copyOf(LENGTH)
        for (i in 0 until OVERLAP) {
            val w = (i + 1f) / (OVERLAP + 1f)
            // Equal-power blend: the start fades in while the tail fades out.
            out[i] = raw[i] * kotlin.math.sqrt(w) + raw[LENGTH + i] * kotlin.math.sqrt(1f - w)
        }
        return out
    }
}
