package app.trollfoss.ui.play

import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test

class BodyVisibilityTest {
    @Test fun distantBodiesAreSkippedButReappearWhenTheCameraArrives() {
        val ball = Thing(1, ThingType.BALL).apply { x = 8f }
        assertFalse(bodyMayReachViewport(ball, 0f, 2.35f))
        assertTrue(bodyMayReachViewport(ball, 7f, 2.35f))
        assertFalse(bodyMayReachViewport(ball, 10f, 2.35f))
    }

    @Test fun bothEdgesIncludeArtWhoseOriginIsOutsideTheScreen() {
        val person = Person(1, Species.FOLK, Look(), 1f)
        for (pose in listOf(Pose.STAND, Pose.LIE, Pose.FLOAT)) {
            person.anim.pose = pose
            for (x in listOf(-0.4f, 2.75f)) {
                person.x = x
                assertTrue(bodyMayReachViewport(person, 0f, 2.35f))
            }
        }
        person.scale = 3f
        person.x = -person.h
        assertTrue(bodyMayReachViewport(person, 0f, 2.35f))
    }

    @Test fun draggingNameTagsAndFastThrowTrailsAreNotClipped() {
        val person = Person(1, Species.FOLK, Look(), 1f).apply { x = 8f; held = true }
        assertTrue(bodyMayReachViewport(person, 0f, 2.35f))
        person.held = false
        person.anim.nameTag = 1f
        assertTrue(bodyMayReachViewport(person, 0f, 2.35f))
        val ball = Thing(2, ThingType.BALL).apply { x = 3.7f; vx = 30f }
        assertTrue(bodyMayReachViewport(ball, 0f, 2.35f))
    }
}
