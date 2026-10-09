package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class FigurePoseTest {
    @Test fun attachmentPointsFollowTheFigureWhenItTurnsAround() {
        val w=World();val p=w.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,1f,.9f,"")
        p.anim.holding=true;p.anim.lookX=1f
        val right=Part.entries.associateWith { Anatomy.at(p,it) }
        p.anim.facing=-1f
        for(part in Part.entries) {
            val left=Anatomy.at(p,part)
            assertEquals(p.x*2-right.getValue(part)[0],left[0],.00001f)
            assertEquals(right.getValue(part)[1],left[1],.00001f)
            assertArrayEquals(right.getValue(part),Anatomy.at(p,part,mirrored=false),.00001f)
        }
    }

    @Test fun occupiedHandStaysPutWhileTheFreeHandWavesOrDances() {
        val a = PersonAnim().apply { wave = 1f; holding = true }
        val occupied = FigurePose.hand(a, true, true)
        for (pose in Pose.entries) for (i in 0..12) {
            a.pose = pose; a.figureTime = i * .2f; a.dance = i * .4f
            assertEquals(occupied, FigurePose.hand(a, true, true))
        }
        a.pose = Pose.STAND; a.dance = 0f
        assertTrue(FigurePose.hand(a, false, true).y < -.5f)
    }

    @Test fun readingOverridesOrdinaryHoldingAndLyingRotatesItsAnchor() {
        val w = WorldFactory.create()
        val p = w.people().first { it.species == Species.FOLK }
        p.anim.pose = Pose.STAND; p.anim.holding = true; p.anim.activity = PersonPlay.READ; p.anim.wave = 1f
        val stand = Anatomy.at(p, Part.HAND)
        assertEquals(p.y - p.h * .38f, stand[1], .00001f)
        p.anim.pose = Pose.LIE
        val lie = Anatomy.at(p, Part.HAND)
        assertEquals(p.x + p.h * .12f, lie[0], .00001f)
        assertEquals(p.y - p.h * .35f, lie[1], .00001f)
    }

    @Test fun mouthsAndHeadAttachmentsMoveAsOneRigidShape() {
        val w = WorldFactory.create()
        for (species in Species.entries) {
            val p = w.addPerson(species, Look(), 1f, PlaceId.HOME, 1f, .9f, "")
            val first = Anatomy.at(p, Part.HAT); val second = Anatomy.at(p, Part.MOUTH)
            fun distance(a: FloatArray, b: FloatArray) = kotlin.math.hypot(a[0]-b[0], a[1]-b[1])
            val before = distance(first,second)
            p.anim.lookX = 1f; p.anim.figureTime = 2f
            assertEquals(species.name, before, distance(Anatomy.at(p,Part.HAT),Anatomy.at(p,Part.MOUTH)), .00001f)
            assertTrue(species.name, Anatomy.at(p, Part.HAT)[0] > first[0])
        }
    }

    @Test fun reducedMotionStopsTheRigButKeepsAnExplicitWaveAndBookPose() {
        val a = PersonAnim().apply { motion = false; wave = 1f; lookX = 1f }
        for (species in Species.entries) {
            val before = FigurePose.head(species,a)
            a.figureTime = 55f; a.dance = 7f
            assertEquals(before, FigurePose.head(species,a))
            assertEquals(0f, before.angle, 0f)
        }
        assertTrue(FigurePose.hand(a,true,false).y < -.5f)
        a.activity = PersonPlay.READ
        assertEquals(-.38f, FigurePose.hand(a,true,true).y, 0f)
    }

    @Test fun previewPoseDoesNotInheritTheSavedBedPose() {
        val a = PersonAnim().apply { pose = Pose.LIE; lookX = 1f; wave = 1f }
        assertTrue(FigurePose.head(Species.FOLK,a,Pose.STAND).angle != 0f)
        assertTrue(FigurePose.hand(a,true,false,Pose.STAND).y < -.5f)
        assertEquals(Pose.LIE,a.pose)
    }

    @Test fun trayStaysLevelAndHeadwearTurnsWhenLyingDown() {
        val a = PersonAnim().apply { lookX = 1f; wave = .6f }
        assertEquals(0f, FigurePose.attachmentAngle(Species.ROBOT,a,Slot.HAND),0f)
        assertTrue(FigurePose.attachmentAngle(Species.ROBOT,a,Slot.HEAD) > 0f)
        a.pose = Pose.LIE
        assertEquals(-90f,FigurePose.attachmentAngle(Species.ROBOT,a,Slot.HEAD),0f)
    }
}
