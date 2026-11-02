package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpriteCacheTest {
    @Test fun identicalPixelsAreCachedAndChangedPixelsRemainAnimated() {
        val cache = SpriteCache()
        val output = ImageBitmap(80, 80)
        val bounds = Rect(0f, 0f, 80f, 80f)
        var stillDraws = 0
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(output), Size(80f, 80f)) {
            with(cache) {
                repeat(2) {
                    frame()
                    assertTrue(stamp("still", bounds, it.toFloat()) {
                        stillDraws++
                        drawRect(Color.Red, Offset(20f, 20f), Size(30f, 30f))
                    })
                }
                assertEquals("only the two animation probes draw static art", 2, stillDraws)
                frame()
                assertFalse(stamp("moving", bounds, 0f) { t ->
                    drawRect(if (t < .5f) Color.Red else Color.Blue, Offset(20f, 20f), Size(30f, 30f))
                })
                assertTrue(animated("moving"))
            }
        }
        val pixels = IntArray(80 * 80)
        output.readPixels(pixels)
        assertEquals(android.graphics.Color.RED, pixels[30 * 80 + 30])
        cache.clear()
    }
}
