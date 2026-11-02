package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StairRedesignTest {
    private fun sheet(time: Float): Bitmap {
        val image = Bitmap.createBitmap(1920, 900, Bitmap.Config.ARGB_8888)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image.asImageBitmap()), Size(1920f, 900f)) {
            drawRect(Color(0xFFF3EADB))
            val places = listOf(PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER, PlaceId.MANOR_CELLAR, PlaceId.MINE_GROUND,
                PlaceId.MANOR_UPPER, PlaceId.MANOR_ATTIC, PlaceId.MINE_UPPER, PlaceId.MINE_GROUND)
            places.forEachIndexed { index, place ->
                val f = Fixture(-1, place, FixtureType.STAIRCASE, 0f, 0f).apply {
                    variant = if (index == 1) 1 else 0
                    mode = if (index == 7) 0 else 1
                }
                translate(240f + index % 4 * 480f, 420f + index / 4 * 450f) {
                    if (place.mine) drawMineFixtureBack(f, 420f, Pen(2f, time), emptyList())
                    else drawManorFixtureBack(f, 420f, Pen(2f, time), emptyList())
                }
            }
        }
        return image
    }

    @Test fun stairsStayStillForSpriteCachingAndKeepTheDoorwayTargets() {
        val first = sheet(0f)
        val later = sheet(8f)
        try {
            assertTrue("Stair artwork should not invalidate its sprite each frame", first.sameAs(later))
            for ((place, variant) in listOf(PlaceId.MANOR_GROUND to 0, PlaceId.MANOR_CELLAR to 0, PlaceId.MANOR_UPPER to 1)) {
                val fixture = Fixture(-1, place, FixtureType.STAIRCASE, 0f, 0f).apply { this.variant = variant }
                val door = requireNotNull(StairDoorways.of(fixture))
                assertTrue(StairDoorways.hit(fixture, (door.left + door.right) / 2, (door.top + door.bottom) / 2, 0f))
            }
            val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "redesign").apply { mkdirs() }
            File(directory, "stairs.png").outputStream().use { first.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally { first.recycle(); later.recycle() }
    }
}
