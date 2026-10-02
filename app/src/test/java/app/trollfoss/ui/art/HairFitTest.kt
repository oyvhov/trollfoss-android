package app.trollfoss.ui.art

import app.trollfoss.domain.Look
import org.junit.Assert.*
import org.junit.Test

class HairFitTest {
    @Test fun lengthMovesFreeEndsWithoutWideningScalpOrLocks() {
        val short = HairFit(Look(hairLength = 0.65f))
        val long = HairFit(Look(hairLength = 1.6f))
        assertTrue(long.end - short.end > 2f)
        assertTrue(long.tuft - short.tuft > 0.8f)
        assertEquals(short.outerWidth, long.outerWidth, 0f)
        assertEquals(short.crown, long.crown, 0f)
        assertEquals(short.lockWidth, long.lockWidth, 0f)
    }
    @Test fun fullnessAddsHairOutsideHeadWithoutChangingEndOrHairline() {
        val small = HairFit(Look(hairSize = 0.8f))
        val full = HairFit(Look(hairSize = 1.5f))
        assertTrue(small.outerWidth >= 1f)
        assertTrue(small.crown >= 1f)
        assertTrue(full.outerWidth > small.outerWidth)
        assertTrue(full.lockWidth > small.lockWidth)
        assertEquals(small.end, full.end, 0f)
        assertEquals(small.fringe, full.fringe, 0f)
    }
}
