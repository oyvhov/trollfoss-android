package app.trollfoss.domain

import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaveMouseTest {
    @Test fun theMouseRunsBetweenItsDenAndCrumbsAndPausesOnTheOpenFloor() {
        val cycle = (0..400).map { CaveMouse.pose(it * .04f) }
        assertTrue(cycle.all { Highlands.walkable(it.x, it.z, true) })
        assertTrue(cycle.count { it.running == 0f && it.x == CaveMouse.den.x } >= 40)
        assertTrue(cycle.count { it.running == 0f && it.x == CaveMouse.crumbs.x } >= 40)
        assertTrue(cycle.any { it.running > .9f })
        assertTrue(CaveMouse.canCall(CaveMouse.calling.x, CaveMouse.calling.z))
        assertTrue(CaveMouse.canCall(BergKitchen.hearth.x, BergKitchen.hearth.z))
        assertFalse(CaveMouse.canCall(0f, 12f))
    }

    @Test fun callingAwayFromTheDenMeetsTheChildThenRejoinsTheSameRoute() {
        for (at in listOf(1.3f, 3.2f, 7.9f, 11.7f, 15.1f)) {
            val before = CaveMouse.pose(at)
            var previous = CaveMouse.pose(at, 0f)
            assertEquals(before.x, previous.x, .0001f)
            assertEquals(before.z, previous.z, .0001f)
            for (step in 1..170) {
                val age = step * .04f
                val now = CaveMouse.pose(at + age, age)
                assertTrue(Highlands.walkable(now.x, now.z, true))
                assertTrue(hypot(now.x - previous.x, now.z - previous.z) < .19f)
                previous = now
            }
            val greet = CaveMouse.pose(at + 3f, 3f)
            assertEquals(CaveMouse.meeting.x, greet.x, .0001f)
            assertEquals(CaveMouse.meeting.z, greet.z, .0001f)
            assertEquals(1f, greet.offering, .0001f)
            val after = CaveMouse.pose(at + CaveMouse.RESPONSE_SECONDS)
            assertEquals(after.x, previous.x, .0001f)
            assertEquals(after.z, previous.z, .0001f)
        }
    }

    @Test fun reducedMotionHasOneRestingPoseAndOneCompleteGreeting() {
        assertEquals(CaveMouse.pose(0f, motion = false), CaveMouse.pose(129f, motion = false))
        val still = CaveMouse.pose(0f, 0f, false)
        assertEquals(1f, still.offering, .0001f)
        assertEquals(0f, still.running, .0001f)
        assertEquals(still, CaveMouse.pose(37f, 5.1f, false))
        assertEquals(CaveMouse.pose(0f, motion = false), CaveMouse.pose(0f, 7f, false))
    }
}
