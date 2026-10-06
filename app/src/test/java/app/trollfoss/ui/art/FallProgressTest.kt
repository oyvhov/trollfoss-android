package app.trollfoss.ui.art

import org.junit.Assert.*
import org.junit.Test

class FallProgressTest {
    private val th = listOf(0, 2, 4, 7, 10, 14, 19)

    @Test fun fillGrowsWithStickersAndStopsAtTheLastLevel() {
        assertEquals(FallProgress.stone(0), FallProgress.fill(0, th), 1e-4f)
        assertEquals(FallProgress.stone(1), FallProgress.fill(2, th), 1e-4f)
        val three = FallProgress.fill(3, th)
        assertTrue(three > FallProgress.stone(1) && three < FallProgress.stone(2))
        assertEquals(FallProgress.stone(6), FallProgress.fill(500, th), 1e-4f)
    }

    @Test fun stonesAreEvenFromBottomToTop() {
        assertEquals(0f, FallProgress.stone(0), 0f)
        assertEquals(1f, FallProgress.stone(9), 0f)
        assertEquals(1f / 9f, FallProgress.stone(1), 1e-6f)
    }

    @Test fun theGiftHopsOnlyWhenTheNextLevelIsAlmostThere() {
        assertFalse(FallProgress.ready(2, th))
        assertTrue(FallProgress.ready(3, th))
        assertFalse(FallProgress.ready(500, th))
    }
}
