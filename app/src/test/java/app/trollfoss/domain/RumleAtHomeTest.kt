package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class RumleAtHomeTest {
    @Test fun heLooksThenGraspsTheBirdAndPutsItDownBeforeOpeningHisFingers() {
        val home = RumleAtHome()
        fun at(time: Float) = home.sample(time, true, null, false)
        assertEquals(RumleQuietPose.resting, at(1f))
        assertTrue(at(2.8f).watchBird > 0f)
        assertEquals(0f, at(2.8f).touch, 0f)
        assertTrue(at(3.6f).touch > .5f)
        assertEquals(0f, at(4.11f).lift, 0f)
        val holding = at(5.4f)
        assertEquals(1f, holding.touch, .001f)
        assertEquals(1f, holding.lift, .001f)
        assertNotEquals(holding.turn, at(7.4f).turn, .5f)
        val returning = at(9.20f)
        assertEquals(0f, returning.lift, 0f)
        assertEquals(1f, returning.touch, .001f)
        assertEquals(RumleQuietPose.resting, at(11f))
        assertTrue(at(21f).cupEar > .99f)
        assertEquals(RumleQuietPose.resting, at(25f))
    }

    @Test fun aChildCanInterruptMidReachAndTheNextQuietMomentStartsAfterTheVisit() {
        val home = RumleAtHome()
        val holding = home.sample(6f, true, null, false)
        assertEquals(holding, home.sample(6f, true, 0f, true))
        val release = home.sample(6.2f, true, .2f, true)
        assertTrue(release.touch in 0.45f..0.55f)
        assertEquals(release, home.sample(6.2f, true, .2f, true))
        assertEquals(RumleQuietPose.resting, home.sample(6.5f, true, .5f, true))
        assertEquals(RumleQuietPose.resting, home.sample(9.8f, true, null, false))
        assertEquals(RumleQuietPose.resting, home.sample(11f, true, null, false))
        assertTrue(home.sample(15.4f, true, null, false).lift > .99f)
        assertEquals(RumleQuietPose.resting, home.sample(18f, false, null, true))
    }

    @Test fun hearingTheMouseOrTheStonesTurnsHisHeadAndCupsHisEarThenTheHandRests() {
        val home = RumleAtHome()
        home.sample(0f, true, null, true)
        assertTrue(home.sample(.6f, true, null, true).cupEar > .75f)
        assertEquals(1f, home.sample(1.5f, true, null, true).listen, .001f)
        val waiting = home.sample(7f, true, null, true)
        assertEquals(0f, waiting.cupEar, .001f)
        assertEquals(1f, waiting.listen, .001f)
        assertEquals(RumleQuietPose.resting, home.sample(8f, true, 1f, true))
    }

    @Test fun eyesLeadAnArrivingOrDepartingGuestAndTheHeadTurnsAtTheSameSpeedAcrossFrameRates() {
        fun track(intervals: Int): RumleGaze = RumleGaze().apply {
            follow(0f, true, 0f, 0f)
            repeat(intervals) { i -> follow((i+1)*.5f/intervals, true, .5f, .24f) }
        }
        val gaze = RumleGaze()
        gaze.follow(0f,true,0f,0f)
        gaze.follow(.02f, true, .5f,.24f)
        assertTrue(gaze.yaw < .1f)
        assertTrue(gaze.eyeX > .06f && gaze.eyeY < -.025f)
        val slow = track(10)
        val fast = track(60)
        assertEquals(slow.yaw, fast.yaw, .0001f)
        assertTrue(slow.yaw > .40f)
        slow.follow(.54f, true, -.5f, -.1f)
        assertTrue(slow.eyeX < -.05f)
        slow.follow(.55f, false, -.4f,.18f)
        assertEquals(-.4f, slow.yaw, 0f)
        assertEquals(0f, slow.eyeX, 0f)
        assertEquals(0f, slow.eyeY, 0f)
    }
}
