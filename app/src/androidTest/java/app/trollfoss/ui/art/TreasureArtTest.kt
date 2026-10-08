package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.domain.ThingType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TreasureArtTest {
    @Test fun eachClueAndEachLanternSettingHasItsOwnReadablePicture() {
        val bitmap=Bitmap.createBitmap(160,180,Bitmap.Config.ARGB_8888)
        fun render(type:ThingType,variant:Int,used:Int):IntArray {
            bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(160f,180f)) {
                translate(80f,165f) { drawThing(type,variant,used,110f,140f,Pen(2f)) }
            }
            return IntArray(160*180).also { bitmap.getPixels(it,0,160,0,0,160,180) }
        }
        try {
            val notes=(0..2).map { render(ThingType.RUMLE_NOTE,it,0) }
            assertEquals(3,notes.map { it.contentHashCode() }.toSet().size)
            notes.forEach { assertTrue(it.count { c -> c ushr 24!=0 }>8000) }
            assertFalse(notes[0].contentEquals(render(ThingType.RUMLE_NOTE,0,1)))
            val lamps=(0..3).map { render(ThingType.TROLL_LANTERN,0,it) }
            assertEquals(4,lamps.map { it.contentHashCode() }.toSet().size)
        } finally { bitmap.recycle() }
    }
}
