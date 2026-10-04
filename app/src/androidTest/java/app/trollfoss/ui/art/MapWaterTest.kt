package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapWaterTest {
    @Test fun tapWaterFollowsThePoolRiverAndFjordInsteadOfAHorizontalRectangle() {
        for ((w, h) in listOf(3072f to 1200f, 3840f to 1080f)) {
            val g = mapGeo(w, h)
            assertTrue(g.inWater(g.pool.center))
            assertFalse(g.inWater(g.pool.topLeft + Offset(1f, 1f)))
            assertTrue(g.inWater(Offset(w * .95f, h * .80f)))
            assertFalse(g.inWater(Offset(w * .2f, h * .95f)))
            assertFalse(g.inWater(Offset(w * .44f, h * .36f)))
            val f = .5f; val center = g.river.at(f); val dir = g.river.dir(f)
            assertTrue(g.inWater(center))
            val normal = Offset(-dir.y, dir.x)
            assertTrue(g.inWater(center + normal * (g.riverWidth(f) * .4f)))
            assertFalse(g.inWater(center + normal * (g.riverWidth(f) * .7f)))
        }
    }
}
