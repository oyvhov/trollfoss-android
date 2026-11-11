package app.trollfoss.domain

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class CaveMousePose(
    val x: Float,
    val z: Float,
    val heading: Float,
    val running: Float,
    val offering: Float = 0f,
    val listening: Float = 0f,
)

/** The den, crumbs and meeting place share an open strip of floor in front of the cake table. */
object CaveMouse {
    val den = BergPoint(-13.90f, -1.45f)
    val crumbs = BergPoint(-9.10f, .30f)
    val calling = BergPoint(-11.65f, 2.15f)
    val meeting = BergPoint(calling.x, 1.30f)
    const val RESPONSE_SECONDS = 6.8f
    private const val PERIOD = 16f
    private val pi = PI.toFloat()

    fun canCall(x: Float, z: Float) = hypot(x + 12.35f, z + .18f) <= 6.3f

    private fun progress(t: Float, start: Float, end: Float): Float {
        val u = ((t - start) / (end - start)).coerceIn(0f, 1f)
        return u * u * (3f - 2f * u)
    }

    private fun travel(t: Float, start: Float, end: Float): Float {
        val u = ((t - start) / (end - start)).coerceIn(0f, 1f)
        return 4f * u * (1f - u)
    }

    private fun turn(a: Float, b: Float, fraction: Float): Float {
        val delta = atan2(sin(b - a), cos(b - a))
        return a + fraction * delta
    }

    private fun foraging(time: Float): CaveMousePose {
        val t = ((time % PERIOD) + PERIOD) % PERIOD
        // Two runs in each direction, with a pause to taste or listen in between.
        val u = .47f * progress(t, 2.4f, 4.35f) + .53f * progress(t, 5.30f, 7.5f) -
            .53f * progress(t, 10.5f, 12.7f) - .47f * progress(t, 13.65f, 15.6f)
        val pace = travel(t, 2.4f, 4.35f) + travel(t, 5.30f, 7.5f) +
            travel(t, 10.5f, 12.7f) + travel(t, 13.65f, 15.6f)
        val x = den.x + (crumbs.x - den.x) * u
        val z = den.z + (crumbs.z - den.z) * u + sin(pi * u) * 1.17f
        val tangent = atan2(crumbs.x - den.x, crumbs.z - den.z + cos(pi * u) * 1.17f * pi)
        val turnBack = 1f - progress(t, .9f, 2.3f) + progress(t, 9.1f, 10.5f)
        return CaveMousePose(x, z, tangent + pi * turnBack, pace)
    }

    fun pose(time: Float, age: Float = -1f, motion: Boolean = true): CaveMousePose {
        val called = age >= 0f && age < RESPONSE_SECONDS
        if (!motion) {
            return if (called) CaveMousePose(meeting.x, meeting.z, 0f, 0f, 1f, 1f)
            else CaveMousePose(crumbs.x, crumbs.z, -.68f, 0f)
        }
        if (!called) return foraging(time)

        // Reconstruct where she actually was at the tap and where her ordinary route will resume.
        val startTime = time - age
        val leaving = age > 4.75f
        val end = if (leaving) foraging(startTime + RESPONSE_SECONDS) else foraging(startTime)
        val fromX = if (leaving) meeting.x else end.x
        val fromZ = if (leaving) meeting.z else end.z
        val toX = if (leaving) end.x else meeting.x
        val toZ = if (leaving) end.z else meeting.z
        val start = if (leaving) 4.78f else .18f
        val finish = if (leaving) RESPONSE_SECONDS else 1.98f
        val amount = progress(age, start, finish)
        val offset = .26f * sin(pi * amount)
        val direction = atan2(toX - fromX, toZ - fromZ + .26f * pi * cos(pi * amount))
        val yaw = if (leaving) {
            turn(turn(0f, direction, progress(age, 4.75f, 5.12f)), end.heading, progress(age, 6.42f, RESPONSE_SECONDS))
        } else {
            turn(turn(end.heading, direction, progress(age, 0f, .36f)), 0f, progress(age, 1.57f, 2.15f))
        }
        val raise = progress(age, 1.92f, 2.68f) * (1f - progress(age, 4.14f, 4.79f))
        val listen = progress(age, 1.65f, 2.22f) * (1f - progress(age, 4.33f, 4.78f))
        return CaveMousePose(
            fromX + (toX - fromX) * amount,
            fromZ + (toZ - fromZ) * amount + offset,
            yaw, travel(age, start, finish), raise, listen,
        )
    }
}
