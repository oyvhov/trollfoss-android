package app.trollfoss.domain

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

/** Weights for the hand, pendant and independent lines of sight in the chair. */
internal data class RumleQuietPose(
    val touch: Float = 0f,
    val lift: Float = 0f,
    val turn: Float = 0f,
    val watchBird: Float = 0f,
    val cupEar: Float = 0f,
    val listen: Float = 0f,
) {
    fun blend(towards: RumleQuietPose, amount: Float): RumleQuietPose {
        if (amount <= 0f) return this
        if (amount >= 1f) return towards
        fun mix(from: Float, to: Float) = from + (to - from) * amount
        return RumleQuietPose(
            mix(touch, towards.touch), mix(lift, towards.lift), mix(turn, towards.turn),
            mix(watchBird, towards.watchBird), mix(cupEar, towards.cupEar), mix(listen, towards.listen),
        )
    }

    companion object { val resting = RumleQuietPose() }
}

private fun ease(from: Float, to: Float, time: Float): Float {
    val t = ((time - from) / (to - from)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun interval(time: Float, start: Float, inPlace: Float, leave: Float, end: Float): Float =
    ease(start, inPlace, time) * (1f - ease(leave, end, time))

/** Uses time spent in the active game. Duplicate renders leave the pose unchanged. */
internal class RumleAtHome {
    private enum class Mode { QUIET, LISTENING, BUSY }
    private var mode = Mode.QUIET
    private var began = 0f
    private var previousTime = Float.NaN
    private var previous = RumleQuietPose.resting
    private var from = RumleQuietPose.resting

    fun sample(time: Float, enabled: Boolean, busyAge: Float?, listening: Boolean): RumleQuietPose {
        if (!enabled || time < previousTime) {
            mode = Mode.QUIET
            began = 0f
            from = RumleQuietPose.resting
            previous = RumleQuietPose.resting
            previousTime = Float.NaN
            if (!enabled) return previous
        }
        val next = when {
            busyAge != null -> Mode.BUSY
            listening -> Mode.LISTENING
            else -> Mode.QUIET
        }
        if (next != mode) {
            from = previous
            mode = next
            // A late first render may already be well into the child's chosen action.
            began = time - (busyAge ?: 0f).coerceAtLeast(0f)
        }
        val age = (time - began).coerceAtLeast(0f)
        val target = when (mode) {
            Mode.BUSY -> RumleQuietPose.resting
            Mode.LISTENING -> {
                val firstListen = 1f - ease(3.4f, 4.5f, age)
                val nextListen = interval(age % 20.5f, 15.2f, 16.2f, 18f, 19f)
                RumleQuietPose(cupEar = max(firstListen, nextListen), listen = 1f)
            }
            Mode.QUIET -> routine(age)
        }
        val transition = if (mode == Mode.BUSY) .40f else .80f
        previous = from.blend(target, ease(0f, transition, age))
        previousTime = time
        return previous
    }

    private fun routine(age: Float): RumleQuietPose {
        val phase = age % 34f
        // First he looks, then reaches, closes his fingers and only then lifts the little bird.
        val touch = interval(phase, 2.9f, 4.1f, 9.25f, 10.4f)
        val lift = interval(phase, 4.18f, 5.35f, 8.05f, 9.18f)
        val turn = if (lift > 0f) lift * sin((phase - 4.18f) * 1.3f) else 0f
        val watchBird = interval(phase, 2.20f, 3.15f, 8.95f, 10.25f)
        val cupEar = interval(phase, 19.8f, 20.85f, 22.55f, 23.8f)
        val listen = interval(phase, 19.10f, 20f, 22.85f, 24.1f)
        return RumleQuietPose(touch, lift, turn, watchBird, cupEar, listen)
    }
}

/** The pupils find a moving visitor first. The head follows without turning the body in the chair. */
internal class RumleGaze {
    private var time = Float.NaN
    var yaw = 0f
        private set
    var pitch = 0f
        private set
    var eyeX = 0f
        private set
    var eyeY = 0f
        private set

    fun follow(now: Float, motion: Boolean, targetYaw: Float, targetPitch: Float) {
        val dt = now - time
        time = now
        if (!motion || !dt.isFinite() || dt < 0f || dt > 1f) {
            yaw = targetYaw
            pitch = targetPitch
        } else {
            val follow = 1f - exp(-dt * 4.6f)
            yaw += (targetYaw - yaw) * follow
            pitch += (targetPitch - pitch) * follow
        }
        eyeX = ((targetYaw - yaw) * .20f).coerceIn(-.075f, .075f)
        eyeY = ((pitch - targetPitch) * .16f).coerceIn(-.055f, .055f)
    }
}
