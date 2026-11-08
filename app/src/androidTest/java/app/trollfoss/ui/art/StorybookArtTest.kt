package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.*
import app.trollfoss.ui.play.SpriteCache
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class StorybookArtTest {
    @Test fun coordinatedHousesAndFullSizeRoomPreview() {
        val bitmap = Bitmap.createBitmap(1800,1200,Bitmap.Config.ARGB_8888)
        val world=WorldFactory.create(); val sim=Sim(world)
        sim.mine.layFoundation(1); sim.mine.finishJob()
        sim.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.LIVING);sim.mine.finishJob()
        val ids=world.fixtures.keys.toSet()
        CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(1800f,1200f)) {
            drawRect(Color(0xFFF5EDDE))
            HousePalette.entries.forEachIndexed { i,palette ->
                sim.mine.applyPalette(palette)
                translate(i*600f,0f) { drawMineHouse(world.mine,35f,390f,200f,Pen(2f)) }
            }
            sim.mine.applyPalette(HousePalette.MOON)
            clipRect(top=500f) { translate(120f,610f) {
                drawMineSuggestion(RoomSuggestion(PlaceId.MINE_GROUND,2,RoomKind.LIBRARY),world.mine,
                    Stage(4f,560f,1560f,560f,10f),Pen(2.2f))
            } }
        }
        assertEquals(ids,world.fixtures.keys)
        assertNull(world.mine.kind(PlaceId.MINE_GROUND,2))
        val out=File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),"storybook").apply { mkdirs() }
        File(out,"houses-and-preview.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }

    @Test fun animatedAndStaticCachesStayWithinTheirMemoryBudgets() {
        val budget=64L*1024
        val cache=SpriteCache(budget)
        val bitmap=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888)
        val scope=CanvasDrawScope()
        repeat(30) { frame ->
            scope.draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(128f,128f)) {
                cache.frame()
                with(cache) {
                    repeat(5) { index ->
                        stampSlow(frame*5+index,Rect(0f,0f,100f,100f),frame.toFloat(),10f) { drawCircle(Color.Red,20f,Offset(50f,50f)) }
                        stamp("static:$frame:$index",Rect(0f,0f,100f,100f),0f) { drawCircle(Color.Blue,20f,Offset(50f,50f)) }
                    }
                    finish()
                }
            }
            assertTrue(cache.retainedBytes <= budget*2)
        }
        assertTrue(cache.retainedBytes>0)
        cache.clear(); assertEquals(0L,cache.retainedBytes)
        bitmap.recycle()
    }
}
