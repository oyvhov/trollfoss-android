package app.trollfoss.ui.play

import org.junit.Assert.*
import org.junit.Test

class PlayViewportTest {
    @Test fun `opening a panel preserves toy scale and centres work in uncovered space`() {
        for ((w, h, panel) in listOf(Triple(1920f, 1200f, 456f), Triple(1600f, 1200f, 456f), Triple(2400f, 1080f, 651f))) {
            val full = PlayViewport(w, h)
            val open = PlayViewport(w, h, panel)
            assertEquals(full.unit, open.unit, 0.001f)
            val camera = open.cameraFor(5f)
            val screenX = (5f - camera) * open.unit
            assertEquals(open.right / 2f, screenX, 0.001f)
            assertTrue(screenX < w - panel)
            assertEquals(5f, full.focus(full.cameraFor(open.focus(camera))), 0.001f)
        }
    }
    @Test fun `tablet retains the requested wider view`() {
        assertEquals(2.35f, PlayViewport(1920f, 1200f).visibleUnits, 0.001f)
        assertEquals(1080f, PlayViewport(2400f, 1080f).unit, 0.001f)
    }
}
