package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Progression
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaterfallMapTest {
    @Test fun theMapKeepsTheWaterfallAndTheDiscoveredTunnelWithoutACaveLandmark() {
        val out=java.io.File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir,"waterfall-scenes").apply { mkdirs() }
        for(phone in listOf(false,true)) {
            val w=if(phone) 3840 else 3072; val h=if(phone) 1080 else 1200
            val g=mapGeo(w.toFloat(),h.toFloat())
            assertFalse(g.bases.containsKey(PlaceId.LAB))
            assertFalse(g.plotOrder.contains(PlaceId.LAB))
            assertTrue(g.tunnel.dots.isNotEmpty())
            val bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
            try {
                CanvasDrawScope().draw(Density(if(phone) 2.625f else 1.5f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(w.toFloat(),h.toFloat())) {
                    val pen=Pen(h*0.0034f)
                    drawIslandMap(pen,PlaceId.FOREST,0f,tunnel=true)
                    drawFallProgress(g,pen,0,Progression.thresholds,0f,false)
                }
                java.io.File(out,"${if(phone) "phone" else "tablet"}-MAP.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            } finally { bitmap.recycle() }
        }
    }
}
