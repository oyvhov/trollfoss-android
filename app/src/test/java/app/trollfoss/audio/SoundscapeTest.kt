package app.trollfoss.audio

import app.trollfoss.domain.PlaceId
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class SoundscapeTest {
    @Test fun everyPlaceHasABedAndTheMapAlwaysHasTheWaterfall() {
        for (place in PlaceId.entries) {
            for (night in listOf(false, true)) {
                assertNotNull("$place", Soundscape.bedFor(place, night, onMap = false))
                assertEquals(SoundBed.FALL, Soundscape.bedFor(place, night, onMap = true))
            }
        }
    }

    @Test fun placesSoundLikeThemselves() {
        assertEquals(SoundBed.WAVES, Soundscape.bedFor(PlaceId.BEACH, false, false))
        assertEquals(SoundBed.WAVES, Soundscape.bedFor(PlaceId.BEACH, true, false))
        assertEquals(SoundBed.ROOM, Soundscape.bedFor(PlaceId.HOME, false, false))
        assertEquals(SoundBed.SEA, Soundscape.bedFor(PlaceId.UNDERWATER, false, false))
        assertEquals(SoundBed.DRONE, Soundscape.bedFor(PlaceId.SPACE, true, false))
        assertEquals("birds by day", SoundBed.BIRDS, Soundscape.bedFor(PlaceId.FOREST, false, false))
        assertEquals("crickets at night", SoundBed.CRICKETS, Soundscape.bedFor(PlaceId.FOREST, true, false))
        assertEquals(SoundBed.DRIPS, Soundscape.bedFor(PlaceId.MANOR_CELLAR, false, false))
    }

    @Test fun theWaterfallOnTheMapGrowsWithEveryLevel() {
        val gains = (1..10).map { Soundscape.mapGain(it) }
        for (i in 1 until gains.size) assertTrue("level ${i + 1} is louder", gains[i] > gains[i - 1])
        assertTrue(gains.first() in 0.2f..0.4f)
        assertEquals(1f, gains.last(), 0.001f)
        assertEquals(Soundscape.mapGain(9), Soundscape.gain(SoundBed.FALL, 9), 0f)
        assertEquals("other beds ignore the level", Soundscape.gain(SoundBed.WIND, 1), Soundscape.gain(SoundBed.WIND, 9), 0f)
    }

    @Test fun everyBedIsAudibleUnclippedAndLoopsWithoutAClick() {
        for (bed in SoundBed.entries) {
            val pcm = Soundscape.render(bed)
            assertEquals("$bed length", Soundscape.RATE * Soundscape.SECONDS, pcm.size)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("$bed peaks near 60% of full scale, never clipped ($peak)", peak in 18000..21000)
            val rms = sqrt(pcm.sumOf { it.toDouble() * it } / pcm.size)
            assertTrue("$bed is not silent (rms $rms)", rms > 200.0)
            // The step from the last sample to the first must be no bigger than the steps inside the loop.
            val typical = (1 until pcm.size).sumOf { abs(pcm[it] - pcm[it - 1]).toDouble() } / pcm.size
            val seam = abs(pcm[0] - pcm[pcm.size - 1]).toDouble()
            assertTrue("$bed seam $seam vs typical step $typical", seam <= typical * 6 + 50)
        }
    }

    @Test fun theSameBedAlwaysSoundsTheSame() {
        assertArrayEquals(Soundscape.render(SoundBed.BIRDS), Soundscape.render(SoundBed.BIRDS))
        assertFalse(Soundscape.render(SoundBed.WIND).contentEquals(Soundscape.render(SoundBed.WAVES)))
    }
}
