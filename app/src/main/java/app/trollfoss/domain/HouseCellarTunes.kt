package app.trollfoss.domain

/**
 * The jukebox's five tunes, written as steps on the pentatonic scale every instrument of Trollfoss uses (the
 * piano, the xylophone, the carousel): 0 to 9 are C D E G A in two octaves, -1 is a rest. The rules send one
 * event per step ([CellarCode.JUKE_NOTE]); the effect player plays the melody, a bass and the drums.
 *
 *  1. Polka: bouncy, in two
 *  2. Disco funk: syncopated, with a hi-hat on the off-beat
 *  3. Waltz: in three
 *  4. Music box: slow and sparse, no drums
 *  5. March: a fanfare with a snare
 */
object CellarTunes {
    const val COUNT = 5

    /** The pentatonic scale of the instruments, in semitones from middle C an octave up (index 0 to 9). */
    val SCALE = intArrayOf(-12, -10, -8, -5, -3, 0, 2, 4, 7, 9)

    /** Bass notes in semitones from C3 (never above it, so a bass can be pitched down with the playback rate). */
    const val NO_BASS = 99

    /** Drum bits: a kick, a hi-hat and a snare. */
    const val KICK = 1
    const val HAT = 2
    const val SNARE = 4

    private const val R = -1
    private const val N = NO_BASS

    private val steps = floatArrayOf(0.19f, 0.17f, 0.24f, 0.3f, 0.2f)

    private val melodies = arrayOf(
        // Polka.
        intArrayOf(
            5, R, 7, R, 8, R, 7, R, 5, R, 3, R, 5, R, R, R,
            8, R, 9, R, 8, R, 7, R, 5, 7, 5, 3, 5, R, R, R,
        ),
        // Disco funk.
        intArrayOf(
            5, R, 5, 8, R, 8, 7, R, 5, R, 5, 7, R, 5, 3, R,
            6, R, 6, 9, R, 9, 8, R, 7, 8, 7, 5, R, 3, 5, R,
        ),
        // Waltz, eight bars of three.
        intArrayOf(
            5, R, 7, 8, R, R, 7, R, 5, 3, R, R,
            5, R, 7, 9, R, 8, 7, 5, 3, 5, R, R,
        ),
        // Music box.
        intArrayOf(
            5, 7, 8, 7, 5, 7, 9, R, 8, 9, 8, 7, 5, 3, 5, R,
            3, 5, 7, 5, 3, 2, 3, R, 5, 7, 8, 9, 8, 7, 5, R,
        ),
        // March.
        intArrayOf(
            5, R, 5, 5, 7, R, 8, R, 8, R, 7, 5, 8, R, R, R,
            9, R, 9, 8, 7, R, 5, R, 7, 5, 3, 5, R, R, R, R,
        ),
    )

    private val basses = arrayOf(
        intArrayOf(
            0, N, N, N, -5, N, N, N, 0, N, N, N, -5, N, N, N,
            -3, N, N, N, -5, N, N, N, 0, N, N, N, -5, N, 0, N,
        ),
        intArrayOf(
            0, N, 0, N, -3, N, -3, N, 0, N, 0, N, -5, N, -3, N,
            -5, N, -5, N, -5, N, -3, N, 0, N, -3, N, -5, N, -3, N,
        ),
        intArrayOf(0, N, N, -3, N, N, 0, N, N, -5, N, N, 0, N, N, -3, N, N, -5, N, N, 0, N, N),
        intArrayOf(
            0, N, N, N, N, N, N, N, -5, N, N, N, N, N, N, N,
            -3, N, N, N, N, N, N, N, -5, N, N, N, N, N, N, N,
        ),
        intArrayOf(
            0, N, N, N, -5, N, N, N, 0, N, N, N, -5, N, N, N,
            0, N, N, N, -5, N, N, N, -3, N, N, N, -5, N, 0, N,
        ),
    )

    /** Drums: the kick every four steps (every three for the waltz), a hi-hat where the tune is lively. */
    private fun drumAt(tune: Int, step: Int): Int = when (tune) {
        0 -> (if (step % 8 == 0) KICK else 0) or (if (step % 8 == 4) SNARE else 0) or (if (step % 2 == 1) HAT else 0)
        1 -> (if (step % 4 == 0) KICK else 0) or (if (step % 4 == 2) HAT else 0) or (if (step % 8 == 4) SNARE else 0)
        2 -> (if (step % 3 == 0) KICK else 0) or (if (step % 3 != 0) HAT else 0)
        3 -> 0
        else -> (if (step % 4 == 0) KICK else 0) or (if (step % 4 == 2) SNARE else 0)
    }

    fun length(tune: Int): Int = melodies[tune.coerceIn(0, COUNT - 1)].size

    /** Seconds between two steps. */
    fun stepSeconds(tune: Int): Float = steps[tune.coerceIn(0, COUNT - 1)]

    /** The melody note of [step] (0 to 9), or -1. */
    fun melody(tune: Int, step: Int): Int {
        val m = melodies[tune.coerceIn(0, COUNT - 1)]
        return m[step.mod(m.size)]
    }

    /** The bass note of [step] in semitones from C3, or [NO_BASS]. */
    fun bass(tune: Int, step: Int): Int {
        val b = basses[tune.coerceIn(0, COUNT - 1)]
        return b[step.mod(b.size)]
    }

    fun drum(tune: Int, step: Int): Int = drumAt(tune.coerceIn(0, COUNT - 1), step.mod(length(tune)))
}
