package app.trollfoss.ui.play

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The corner by the bag where things, figures and furniture are put away. */
class AwayCornerTest {
    // Tablet numbers: a bag of radius 57 px with its centre at (1833, 1113).
    private val cx = 1833f
    private val cy = 1113f
    private val r = 57f

    @Test fun `the corner reaches further for what comes from the scene than for what comes out of the bag`() {
        assertEquals(AwayCorner.HIT, AwayCorner.reach(fromScene = true), 0f)
        assertEquals(AwayCorner.BAG_ONLY, AwayCorner.reach(fromScene = false), 0f)
        assertTrue(AwayCorner.BAG_ONLY < AwayCorner.DRAW && AwayCorner.DRAW < AwayCorner.HIT)
    }

    @Test fun `a finger up and to the left of the bag is in the corner only for what comes from the scene`() {
        // 106 px from the centre: outside the bag's own 80 px, inside the grown corner's 137 px.
        assertTrue(AwayCorner.contains(cx - 75f, cy - 75f, cx, cy, r, fromScene = true))
        assertFalse(AwayCorner.contains(cx - 75f, cy - 75f, cx, cy, r, fromScene = false))
        assertFalse(AwayCorner.contains(cx - 233f, cy, cx, cy, r, fromScene = true))
        assertTrue(AwayCorner.contains(cx, cy, cx, cy, r, fromScene = false))
    }

    @Test fun `things and figures get the bag, furniture alone gets the crate, nothing held gets nothing`() {
        assertEquals(AwayPicture.BAG, AwayCorner.picture(bodies = 1, furniture = 0))
        assertEquals(AwayPicture.BAG, AwayCorner.picture(bodies = 1, furniture = 1))
        assertEquals(AwayPicture.CRATE, AwayCorner.picture(bodies = 0, furniture = 2))
        assertNull(AwayCorner.picture(bodies = 0, furniture = 0))
    }

    @Test fun `a near miss is the lower right quarter of the screen`() {
        assertTrue(AwayCorner.nearMiss(1500f, 900f, 1920f, 1200f))
        assertFalse(AwayCorner.nearMiss(900f, 900f, 1920f, 1200f))
        assertFalse(AwayCorner.nearMiss(1500f, 500f, 1920f, 1200f))
    }
}
