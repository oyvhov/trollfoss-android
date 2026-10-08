package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.domain.Progression
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RainbowFinaleTest {
    @Test fun theBookShowsRainbowWaterOnlyAtLevelTen() {
        val bitmap = Bitmap.createBitmap(180,300,Bitmap.Config.ARGB_8888)
        fun render(stickers: Int): IntArray {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(180f,300f)) {
                drawRect(Color.White); drawMiniFall(stickers,Progression.thresholds)
            }
            return IntArray(180*300).also { bitmap.getPixels(it,0,180,0,0,180,300) }
        }
        try {
            val before = render(39); val complete = render(40)
            var colors = 0
            for(y in 50..250) for(x in 70..140) if(before[y*180+x]!=complete[y*180+x]) colors++
            assertTrue("the water itself changes",colors>5000)
        } finally { bitmap.recycle() }
    }

    @Test fun theMapFinaleHasColourAndFreezesWhenMotionIsReduced() {
        val bitmap = Bitmap.createBitmap(260,320,Bitmap.Config.ARGB_8888)
        fun render(t: Float,motion: Boolean): IntArray {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(260f,320f)) {
                drawRect(Color.White); drawRainbowFinale(130f,30f,120f,245f,t,motion,Pen(1.8f))
            }
            return IntArray(260*320).also { bitmap.getPixels(it,0,260,0,0,260,320) }
        }
        try {
            val still = render(0f,false)
            assertArrayEquals(still,render(8.4f,false))
            assertFalse(render(0f,true).contentEquals(render(1.3f,true)))
            assertTrue(still.count { it!=android.graphics.Color.WHITE }>20000)
        } finally { bitmap.recycle() }
    }
}
