package app.trollfoss.ui.play

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValleyRiverUiTest {
    private val place=PlaceId.VAGSTADDALEN
    private val host=object:EngineHost {
        override fun sfx(effect:Sfx,volume:Float,rate:Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id:String) {}
        override fun discovered(key:String) {}
        override fun telescope() {}
        override fun radio(on:Boolean) {}
        override fun egg(id:String) {}
        override fun passage(passage:Passage,arrivalX:Float) {}
    }
    private fun at(e:Engine,x:Float,y:Float)=Offset((x-e.cam)*e.u,e.heightPx-e.u+y*e.u)
    private fun drag(e:Engine,b:Body,x:Float,y:Float) {
        val from=at(e,b.x,b.y-b.h*.5f)
        val target=at(e,x,y)
        e.down(1,from,1000)
        repeat(12) { i ->
            e.move(1,from+(target-from)*((i+1)/12f),1100L+i*20)
            e.update(1f/60f)
        }
        repeat(20) { e.update(1f/60f) } // Allow the held object's deliberately soft follow to catch up.
        e.move(1,target,1900)
        assertTrue("the visible object can be picked up by dragging",b.held)
        e.up(1,target,2000)
    }

    @Test fun fingersCanDressSwimGetIntoTheDinghyAndReturnToTheBeach() {
        for(phone in listOf(false,true)) {
            val w=WorldFactory.create();val s=Sim(w)
            val e=Engine(w,place,s,host,false,0f).apply {
                compact=phone;setSize(if(phone) 2400f else 1920f,if(phone) 1080f else 1200f,if(phone) 2.625f else 1.5f)
                // Keep the outfit and both river banks away from the edge-pan zones in both formats.
                focusOn(3.05f)
            }
            val p=w.addPerson(Species.FOLK,Look(height=.78f),1f,place,2.63f,.93f)
            w.playerIds+=p.id
            try {
                repeat(20) { e.update(1f/60f) }
                val suit=w.bodiesIn(place).filterIsInstance<Thing>().first { it.type==ThingType.GARMENT && Garment.style(it.variant)==Garment.SWIM_SUIT }
                val torso=Anatomy.at(p,Part.BODY)
                drag(e,suit,torso[0],torso[1])
                assertEquals("phone=$phone, outfit ${suit.x},${suit.y} at child ${p.x},${p.y}",Garment.SWIM_SUIT,p.look.top)
                drag(e,p,3.04f,.80f)
                repeat(180) { e.update(1f/60f) }
                assertEquals(Pose.SWIM,p.anim.pose)
                val boat=w.fixtures.getValue(WorldFactory.fixtureId(place,Vagstaddalen.DINGHY))
                drag(e,p,boat.x-.08f,boat.y-.10f)
                assertEquals(Mode.SEATED,p.mode)
                assertEquals(boat.id,p.holder)
                s.vehicles.drive(boat,1)
                repeat(100) { e.update(1f/60f) }
                assertTrue(boat.x>3.46f)
                // Land on the open sand, beyond the nearby stump's magnetic seat.
                drag(e,p,2.71f,.84f)
                repeat(180) { e.update(1f/60f) }
                assertEquals("child at ${p.x},${p.y}, holder=${p.holder}",Mode.FREE,p.mode)
                assertEquals(Pose.STAND,p.anim.pose)
            } finally { e.cancel() }
        }
    }

    @Test fun captureValleyAndBathingInBothFormatsDayNightAndWinter() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val out=java.io.File(context.filesDir,"valley-scenes").apply { mkdirs() }
        for(phone in listOf(false,true)) for(scene in listOf("cabin","beach","river","night","winter")) {
            val w=WorldFactory.create().apply { place=this@ValleyRiverUiTest.place;night=scene=="night" }
            val s=Sim(w)
            val boat=w.fixtures.getValue(WorldFactory.fixtureId(place,Vagstaddalen.DINGHY))
            for(i in 0..1) {
                val p=w.addPerson(Species.FOLK,Look(height=.78f,skin=2+i*3,top=Garment.SWIM_SUIT+i,topColor=4+i*4,hair=if(i==0) 18 else 10),1f,place,3.03f,.88f)
                w.playerIds+=p.id
                if(i==1) s.seat(p,boat,1)
            }
            val width=if(phone) 2400 else 1920;val height=if(phone) 1080 else 1200
            val density=Density(if(phone) 2.625f else 1.5f)
            val e=Engine(w,place,s,host,false,0f).apply {
                compact=phone;setSize(width.toFloat(),height.toFloat(),density.density)
                season=if(scene=="winter") Season.WINTER else Season.SUMMER
                focusOn(when(scene) { "cabin" -> 1.25f; "beach" -> 2.9f; else -> 3.7f })
                photoMode=true
            }
            val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            try {
                repeat(180) { e.update(1f/60f) }
                val text=TextMeasurer(createFontFamilyResolver(context),density,LayoutDirection.Ltr)
                CanvasDrawScope().draw(density,LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(width.toFloat(),height.toFloat())) { e.draw(this,text) }
                val file=java.io.File(out,"${if(phone) "phone" else "tablet"}-$scene.png")
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                assertTrue(file.length()>20000)
            } finally { bitmap.recycle();e.cancel() }
        }
    }
}
