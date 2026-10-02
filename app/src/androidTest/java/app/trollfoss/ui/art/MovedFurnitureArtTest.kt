package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovedFurnitureArtTest {
    @Test fun levelToysHaveTheirOwnArtInEveryPlace() {
        val image=Bitmap.createBitmap(220,220,Bitmap.Config.ARGB_8888)
        val scope=CanvasDrawScope()
        for(reward in app.trollfoss.domain.ToyReward.entries) for(place in PlaceId.entries) {
            image.eraseColor(android.graphics.Color.TRANSPARENT)
            scope.draw(Density(1f),LayoutDirection.Ltr,Canvas(image.asImageBitmap()),Size(220f,220f)) {
                drawContext.transform.translate(110f,200f)
                val f=Fixture(-1,place,reward.type,0f,0f)
                assertTrue(drawToyBack(f,260f,Pen(2f)));assertTrue(drawToyFront(f,260f,Pen(2f)))
            }
            val pixels=IntArray(220*220);image.getPixels(pixels,0,220,0,0,220,220)
            assertTrue("${reward.name} in $place has empty art",pixels.count { it ushr 24!=0 }>100)
        }
        image.recycle()
    }
    @Test fun handmadeToysHaveRealArtInEveryPlace() {
        val image = Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888)
        val scope = CanvasDrawScope()
        for (type in listOf(FixtureType.PLAY_FORT, FixtureType.PLAY_CART)) for (place in PlaceId.entries) {
            scope.draw(Density(1f),LayoutDirection.Ltr,Canvas(image.asImageBitmap()),Size(200f,200f)) {
                drawContext.transform.translate(100f,170f)
                val f = Fixture(-1,place,type,0f,0f)
                assertTrue(drawPlayBack(f,200f,Pen(2f))); assertTrue(drawPlayFront(f,200f,Pen(2f)))
            }
        }
        image.recycle()
    }
    @Test fun everyHouseFurnitureTypeUsesItsActualDrawingInEveryDestination() {
        val families = setOf("GR", "UP", "AT", "CE", "GA", "MI")
        val image = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val scope = CanvasDrawScope()
        for (type in FixtureType.entries.filter { it.name.substringBefore('_') in families }) {
            for (place in PlaceId.entries) {
                var drawn = false
                scope.draw(Density(1f), LayoutDirection.Ltr, Canvas(image.asImageBitmap()), Size(160f, 160f)) {
                    drawContext.transform.translate(75f, 145f)
                    val f = Fixture(-1, place, type, 0f, 0f)
                    drawn = drawManorFixtureBack(f, 100f, Pen(1.4f), emptyList()) || drawMineFixtureBack(f, 100f, Pen(1.4f), emptyList())
                }
                assertTrue("$type in $place fell back to a box", drawn)
            }
        }
        image.recycle()
    }
}
