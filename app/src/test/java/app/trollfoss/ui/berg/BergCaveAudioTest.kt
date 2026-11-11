package app.trollfoss.ui.berg

import app.trollfoss.audio.Sfx
import app.trollfoss.audio.Synth
import app.trollfoss.domain.BergSpring
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs

class BergCaveAudioTest {
    private fun walk(x: Float, z: Float, metres: Float, frames: Int, music: Boolean = false): List<CaveCue> {
        val room = BergCaveAudio()
        val dt = metres / (3.6f * frames)
        room.step(dt, true, x, z, false, music)
        return (1..frames).mapNotNull { frame ->
            room.step(dt, true, x + metres * frame / frames, z, true, music)
        }
    }

    @Test fun footstepsFollowDistanceAndTheAuthoredFloor() {
        val fastFrames = walk(3f, -11f, 7.2f, 180)
        assertEquals(6, fastFrames.size)
        assertEquals(fastFrames.map { it.pitch }, walk(3f, -11f, 7.2f, 50).map { it.pitch })
        assertTrue(fastFrames.all { it.effect == Sfx.BERG_STEP_STONE })
        assertTrue(walk(-5f, .6f, 3f, 80).all { it.effect == Sfx.BERG_STEP_WOOL })
        assertTrue(walk(-16f, -22f, 3f, 80).all { it.effect == Sfx.BERG_STEP_WOOL })
        assertTrue(walk(8f, -17.5f, 3f, 80).all { it.effect == Sfx.BERG_STEP_WOOD })
        val quiet = walk(3f, -11f, 7.2f, 180, music = true)
        assertEquals(fastFrames.size, quiet.size)
        assertTrue(quiet.zip(fastFrames).all { (duringSong, usual) -> duringSong.volume < usual.volume * .3f })
    }

    @Test fun stillFeetAndRoomChangesNeverProducePhantomSteps() {
        val room = BergCaveAudio()
        repeat(300) { assertNull(room.step(.04f, true, 7f, 4f, true, false)) }
        assertNull(room.step(.04f, true, -13f, -23f, true, false))
        assertNull(room.step(.04f, false, 13f, -11f, true, false, springHit = true))
        assertNull(room.step(.04f, true, 0f, 10f, true, false))
        repeat(300) { assertNull(room.step(0f, true, 0f, 10f, false, false, springHit = true)) }
    }

    @Test fun theSpringAndEmbersCanBeLocatedByEarAndLeaveRoomForAMelody() {
        fun drop(x: Float, z: Float, music: Boolean = false): CaveCue? {
            val room = BergCaveAudio()
            repeat(350) { assertNull(room.step(.04f, true, x, z, false, music)) }
            return room.step(.04f, true, x, z, false, music, springHit = true)
        }
        val near = checkNotNull(drop(-1.8f, -20f))
        val further = checkNotNull(drop(1.5f, -7f))
        assertEquals(Sfx.BERG_DRIP, near.effect)
        assertTrue(near.volume > further.volume * 2.5f)
        assertNull(drop(6f, 12f))
        assertTrue(checkNotNull(drop(-1.8f, -20f, music = true)).volume < near.volume * .3f)

        fun fire(x: Float, z: Float): List<CaveCue> {
            val room = BergCaveAudio()
            return (1..400).mapNotNull { room.step(.05f, true, x, z, false, false) }
        }
        val embers = fire(-17f, -3.65f)
        assertTrue(embers.size in 3..5)
        assertTrue(embers.all { it.effect == Sfx.BERG_EMBER })
        assertTrue(fire(10f, -5f).isEmpty())
        assertTrue(fire(-7f, -4f).first().volume < embers.first().volume * .4f)
    }

    @Test fun theSoleHitsHaveDifferentTimbresAndTheDropHasAQuietEcho() {
        fun treble(samples: FloatArray): Double {
            var edge = 0.0
            var energy = 0.0
            for (i in 1 until samples.size) {
                edge += abs(samples[i] - samples[i - 1])
                energy += abs(samples[i])
            }
            return edge / energy
        }
        val rock = Synth.render(Sfx.BERG_STEP_STONE)
        val wool = Synth.render(Sfx.BERG_STEP_WOOL)
        assertTrue(treble(rock) > treble(wool) * 1.5)
        val wood = Synth.render(Sfx.BERG_STEP_WOOD)
        assertFalse(rock.contentEquals(wood))
        val drop = Synth.render(Sfx.BERG_DRIP)
        val echoStart = (Synth.SAMPLE_RATE * .18f).toInt()
        val echoPeak = (echoStart until drop.size).maxOf { abs(drop[it]) }
        assertTrue(echoPeak in .002f.. .46f)
        assertTrue(drop.size > Synth.SAMPLE_RATE / 2)
        for (effect in listOf(Sfx.BERG_EMBER, Sfx.BERG_MOUSE)) {
            val sound = Synth.render(effect)
            assertTrue(sound.maxOf { abs(it) } in .78f.. .81f)
            assertArrayEquals(sound, Synth.render(effect), 0f)
        }
    }

    @Test fun hearingTheSameDropFromTheOtherSideOfTheCameraSwapsEars() {
        val spring = BergSpring.drip
        fun hear(x: Float, z: Float, yaw: Float) = checkNotNull(
            BergCaveAudio().step(.04f, true, x, z, false, false, springHit = true, yaw = yaw),
        )
        val left = hear(spring.x + 5f, spring.z, 0f)
        val right = hear(spring.x + 5f, spring.z, PI.toFloat())
        assertTrue(left.pan < -.7f && right.pan > .7f)
        assertEquals(left.volume, right.volume, .0001f)
        assertEquals(0f, hear(spring.x + 5f, spring.z, (PI / 2).toFloat()).pan, .0001f)
        assertEquals(0f, hear(spring.x, spring.z, .82f).pan, .0001f)
    }
}
