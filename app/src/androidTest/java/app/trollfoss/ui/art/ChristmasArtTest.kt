package app.trollfoss.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Deterministic contact sheets are recorded separately from pass/fail assertions. */
class ChristmasArtTest {
    private fun record(name:String,w:Int,h:Int,draw:DrawScope.()->Unit) {
        val bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(w.toFloat(),h.toFloat())) {
            drawRect(Color(0xFFFFF1DF));draw()
        }
        val out=File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),"christmas").apply {mkdirs()}
        File(out,"$name.png").outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
        bitmap.recycle()
    }
    @Test fun recordsAllFurnitureColoursSmallThingsClothesAndRoomStyles() {
        Christmas.catalogue.chunked(54).forEachIndexed { page,items ->
            record("furniture-$page",1200,1440) {
                items.forEachIndexed { i,item ->
                    val f=Fixture(-1,PlaceId.CHRISTMAS_HOME,item.type,0f,0f,item.variant).apply {on=true;mode=1}
                    val u=minOf(170f/f.spec.w,130f/f.spec.h)
                    translate((i%6)*200f+100f,(i/6)*160f+145f) {drawFixtureBack(f,u,Pen(1.8f));drawFixtureFront(f,u,Pen(1.8f))}
                }
            }
        }
        val choices=Christmas.things.map {it to 0}+Christmas.clothes
        record("small-things-and-clothes",1200,1120) {
            choices.forEachIndexed { i,(type,v) -> val u=90f/maxOf(type.w,type.h)
                translate((i%8)*150f+75f,(i/8)*160f+125f) {drawThing(type,v,0,type.w*u,type.h*u,Pen(2f))}
            }
        }
        record("room-styles",1200,640) {
            repeat(20){ i->val rect=Rect((i%5)*240f+8,(i/5)*160f+8,(i%5+1)*240f-8,(i/5+1)*160f-8)
                if(i<12) drawWallSwatch(12+i,rect,Pen(2f)) else drawFloorSwatch(9+i-12,rect,Pen(2f))
            }
        }
    }
    @Test fun recordsAllNissegrendaAreasWithTheirActualFurnitureAndFigures() {
        val world=WorldFactory.create()
        for(place in PlaceId.entries.filter {it.christmas}) record(place.name.lowercase(),1600,900) {
            val u=700f;val cam=if(place==PlaceId.CHRISTMAS_SQUARE) 1.2f else if(place==PlaceId.CHRISTMAS_STABLE) 1.2f else 1f
            translate(0f,200f) {
                drawPlaceBack(place,cam,u,Pen(2.2f,night=0.1f,season=Season.WINTER,festival=Festival.CHRISTMAS))
                world.fixturesIn(place).sortedBy {it.depth}.filter {it.x in cam-0.5f..cam+3f}.forEach {f -> translate((f.x-cam)*u,f.y*u) {drawFixtureBack(f,u,Pen(2.2f));drawFixtureFront(f,u,Pen(2.2f))}}
                world.people().filter {it.place==place && it.x in cam-0.5f..cam+3f}.forEach {p -> translate((p.x-cam)*u,p.y*u) {drawPerson(p.species,p.look,Pose.STAND,p.anim,p.h*u,Pen(2.2f))}}
            }
        }
    }
}
