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
import java.io.File

@RunWith(AndroidJUnit4::class)
class FigureRigUiTest {
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
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun stoppedAnimationClockStillRefreshesClothesAndHeadwear() {
        val w=World();val s=Sim(w)
        val p=w.addPerson(Species.FOLK,Residents.eilevLook(),1f,PlaceId.HOME,.75f,.9f,"Eilev").apply {ground=.9f;resting=true;age=2f}
        val e=Engine(w,PlaceId.HOME,s,host,false,0f).apply {setSize(1920f,1200f,1.5f);photoMode=true}
        val bitmap=Bitmap.createBitmap(1920,1200,Bitmap.Config.ARGB_8888)
        val density=Density(1.5f)
        val text=TextMeasurer(createFontFamilyResolver(context),density,LayoutDirection.Ltr)
        fun frame() { e.update(.1f);CanvasDrawScope().draw(density,LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(1920f,1200f)) {e.draw(this,text)} }
        fun redPixels():Int {
            val x=(p.x-e.cam)*e.u;val y=1200f-e.u+p.y*e.u
            var red=0
            for(px in (x-p.h*e.u*.21f).toInt()..(x+p.h*e.u*.21f).toInt()) for(py in (y-p.h*e.u*.47f).toInt()..(y-p.h*e.u*.24f).toInt()) {
                val c=bitmap.getPixel(px,py)
                if(android.graphics.Color.red(c)>210 && android.graphics.Color.green(c)<130 && android.graphics.Color.blue(c)<150) red++
            }
            return red
        }
        try {
            repeat(3) {frame()};val before=redPixels()
            p.look=p.look.copy(topColor=0,pattern=0)
            val cap=w.addThing(ThingType.CAP,0,PlaceId.HOME,p.x,p.y)
            assertEquals(Give.WORE,s.give(p,cap,Part.HAT))
            repeat(3) {frame()}
            assertTrue("the cached figure changes colour with motion off",redPixels()>before+200)
            assertEquals(0f,p.anim.figureTime,0f)
            val out=File(context.getExternalFilesDir(null),"figure-style").apply {mkdirs()}
            File(out,"reduced-motion-clothes.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        } finally {e.cancel();bitmap.recycle()}
    }

    @Test fun aCapOnATiltedHeadCanBePickedWithoutGrabbingItsOwner() {
        for(phone in listOf(false,true)) for(facing in listOf(-1f,1f)) {
            val width=if(phone)2400f else 1920f;val height=if(phone)1080f else 1200f
            val w=World();val s=Sim(w)
            val p=w.addPerson(Species.FOLK,Residents.eilevLook(),1f,PlaceId.HOME,.75f,.9f,"Eilev").apply {ground=.9f;resting=true;age=2f}
            val cap=w.addThing(ThingType.CAP,0,PlaceId.HOME,p.x,p.y)
            s.give(p,cap,Part.HAT)
            val e=Engine(w,PlaceId.HOME,s,host,true,0f).apply {compact=phone;setSize(width,height,if(phone)2.625f else 1.5f)}
            try {
                e.update(.016f);p.anim.lookX=1f;p.anim.wave=0f;p.anim.facing=facing
                val a=Anatomy.at(p,Part.HAT)
                val angle=FigurePose.attachmentAngle(p.species,p.anim,Slot.HEAD)*kotlin.math.PI.toFloat()/180f
                val offset=cap.h*(p.h*Anatomy.headRadius(p.species)*2f/cap.type.fitsHead)*.45f
                val point=Offset((a[0]+kotlin.math.sin(angle)*offset*facing-e.cam)*e.u,height-e.u+(a[1]-kotlin.math.cos(angle)*offset)*e.u)
                e.down(1,point,1000);e.move(1,point+Offset(100f,-70f),1200)
                assertTrue("the cap is picked at its drawn position",cap.held)
                assertFalse("Eilev stays put",p.held)
            } finally {e.cancel()}
        }
    }
}
