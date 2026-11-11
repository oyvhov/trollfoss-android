package app.trollfoss.ui.berg

import app.trollfoss.audio.Sfx
import app.trollfoss.domain.BergSpring
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

internal data class CaveCue(val effect: Sfx, val volume: Float, val pitch: Float, val pan: Float = 0f)

/** Short, local sounds. The caller advances this clock only while the children can play. */
internal class BergCaveAudio {
    companion object {
        // The timber landing immediately in front of the pantry crates on the upper shelf.
        const val BOARD_LEFT = 7.2f
        const val BOARD_RIGHT = 16.8f
        const val BOARD_BACK = -19.0f
        const val BOARD_FRONT = -16.4f

        private const val STRIDE = 1.24f
        private val footPitches = floatArrayOf(.96f, 1.04f, .98f, 1.01f, .94f, 1.03f)
        private val emberIntervals = floatArrayOf(3.8f, 6.2f, 4.9f, 7.1f, 2.6f)
    }

    private var inCave = false
    private var lastX = 0f
    private var lastZ = 0f
    private var distanceWalked = 0f
    private var footIndex = 0
    private var untilEmber = 1.85f
    private var emberIndex = 0
    private var dropIndex = 0

    fun step(
        seconds: Float, cave: Boolean, x: Float, z: Float, moving: Boolean, music: Boolean,
        springHit: Boolean = false, yaw: Float = 0f,
    ): CaveCue? {
        if (!cave) {
            inCave = false
            return null
        }
        if (!seconds.isFinite() || seconds <= 0f || !x.isFinite() || !z.isFinite()) return null

        if (!inCave) {
            lastX = x
            lastZ = z
            distanceWalked = STRIDE * .40f
            untilEmber = 1.85f
            inCave = true
        }
        val distance = hypot(x - lastX, z - lastZ)
        lastX = x
        lastZ = z
        // A restored position, a party change or an interrupted frame isn't a footfall.
        if (moving && distance < .85f) distanceWalked += distance
        val mix = if (music) .27f else 1f
        var cue: CaveCue? = null
        if (distanceWalked >= STRIDE) {
            distanceWalked -= STRIDE
            val surface = when {
                x in -6.5f..0.5f && z in -1.9f..3.1f -> Sfx.BERG_STEP_WOOL
                x in -17.5f..-8.5f && z in -26.5f..-19.5f -> Sfx.BERG_STEP_WOOL
                x in BOARD_LEFT..BOARD_RIGHT && z in BOARD_BACK..BOARD_FRONT -> Sfx.BERG_STEP_WOOD
                else -> Sfx.BERG_STEP_STONE
            }
            val gain = when (surface) {
                Sfx.BERG_STEP_WOOL -> .15f
                Sfx.BERG_STEP_WOOD -> .25f
                else -> .22f
            }
            val pace = (distance / seconds / 3.6f).coerceIn(0f, 1f)
            cue = CaveCue(surface, gain * (.67f + pace * .33f) * mix, footPitches[footIndex])
            footIndex = (footIndex + 1) % footPitches.size
        }
        untilEmber -= seconds.coerceAtMost(.15f)
        if (untilEmber <= 0f) {
            untilEmber += emberIntervals[emberIndex]
            emberIndex = (emberIndex + 1) % emberIntervals.size
            if (cue == null) {
                cue = localCue(Sfx.BERG_EMBER, -17f - x, -5.7f - z, 14f, .29f * mix, .92f + emberIndex * .037f, yaw)
            }
        }
        if (springHit) {
            // Each audible drop begins exactly when its visible ring spreads in the spring.
            dropIndex = (dropIndex + 1) % 5
            val drop = localCue(
                Sfx.BERG_DRIP, BergSpring.drip.x - x, BergSpring.drip.z - z,
                19f, .39f * mix, .92f + dropIndex * .043f, yaw,
            )
            if (drop != null) return drop
        }
        return cue
    }

    private fun localCue(
        effect: Sfx, dx: Float, dz: Float, radius: Float, gain: Float, pitch: Float, yaw: Float,
    ): CaveCue? {
        val squared = dx * dx + dz * dz
        val remaining = (1f - squared / (radius * radius)).coerceAtLeast(0f)
        val volume = gain * remaining * remaining
        if (volume < .015f) return null
        // Camera right projected onto the sound's bearing. The other ear still hears the vault.
        val toRight = if (yaw.isFinite()) dx * cos(yaw) - dz * sin(yaw) else 0f
        val pan = .82f * toRight / sqrt(squared).coerceAtLeast(1f)
        return CaveCue(effect, volume, pitch, pan)
    }
}
