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
class TreasureTrailUiTest {
    private val place=PlaceId.HOME
    private var height=1200f
    private var clock=10000L
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
    private fun at(e:Engine,x:Float,y:Float)=Offset((x-e.cam)*e.u,height-e.u+y*e.u)
    private fun tap(e:Engine,x:Float,y:Float) {
        clock+=500; val point=at(e,x,y)
        e.down(1,point,clock); e.up(1,point,clock+90)
        repeat(3) { e.update(0.016f) }
    }
    private fun bothSizes(test:(World,Sim,Engine)->Unit) {
        for(phone in listOf(false,true)) {
            height=if(phone) 1080f else 1200f
            val w=World(); val s=Sim(w)
            val e=Engine(w,place,s,host,false,0f).apply { compact=phone; setSize(if(phone) 2400f else 1920f,height,if(phone) 2.625f else 1.5f) }
            try { test(w,s,e) } finally { e.cancel() }
        }
    }

    @Test fun tappingThreeRealPapersInAnyOrderFindsOneTreasure() = bothSizes { w,s,e ->
        s.magic.start(Adventure.RUMLE)
        for(i in listOf(2,0,1)) {
            val note=s.trail.note(i)!!
            note.place=place; note.x=1.1f; note.y=place.floor; note.ground=place.floor
            repeat(10) { e.update(0.016f) }
            val revision=e.playVersion
            tap(e,note.x,note.y-note.h/2)
            assertTrue("clue $i answers to a tap",s.trail.found(i))
            assertTrue("the clue publishes new progress to the adventure cards",e.playVersion>revision)
            note.x=0.15f+i*0.15f
        }
        assertTrue(s.trail.complete)
        assertEquals(1,w.bag().count { it is Thing && it.type==ThingType.TROLL_LANTERN })
    }

    @Test fun pickingUpTheLastPaperAlsoFindsTheTreasure() = bothSizes { w,s,e ->
        s.magic.start(Adventure.RUMLE)
        s.trail.find(s.trail.note(0)!!); s.trail.find(s.trail.note(1)!!)
        val paper=s.trail.note(2)!!
        paper.place=place; paper.x=1.1f; paper.y=place.floor; paper.ground=place.floor
        repeat(10) { e.update(0.016f) }
        val from=at(e,paper.x,paper.y-paper.h/2)
        val to=from+Offset(0.35f*e.u,-0.2f*e.u)
        clock+=1000; e.down(4,from,clock); e.move(4,to,clock+200)
        repeat(3) { e.update(0.016f) }
        e.move(4,to,clock+400); e.up(4,to,clock+500)
        assertTrue(s.trail.complete)
        assertSame(paper,w.bodies[paper.id]); assertNotNull(s.trail.lamp())
    }

    @Test fun theLanternAnswersOnTheFloorAndInAFriendsHand() = bothSizes { w,s,e ->
        val lamp=w.addThing(ThingType.TROLL_LANTERN,0,place,0.7f,place.floor)
        val person=w.addPerson(Species.FOLK,Look(),1f,place,1.4f,place.floor)
        repeat(15) { e.update(0.016f) }
        tap(e,lamp.x,lamp.y-lamp.h/2); assertEquals(1,lamp.used)
        s.give(person,lamp,Part.HAND)
        repeat(15) { e.update(0.016f) }
        val hand=Anatomy.at(person,Part.HAND)
        tap(e,hand[0],hand[1]-lamp.h*0.25f)
        assertEquals("a tap changes the held lantern picture",2,lamp.used)
        assertEquals(Mode.WORN,lamp.mode); assertEquals(person.id,lamp.holder)
    }

    @Test fun findingAPackedLanternOpensItsPageEvenWhenAFriendCarriesIt() = bothSizes { w,s,e ->
        repeat(TravelBag.PAGE_SIZE) { w.addThing(ThingType.BALL,0,null,0f,0f).apply { mode=Mode.BAG } }
        val lamp=w.addThing(ThingType.TROLL_LANTERN,0,null,0f,0f).apply { mode=Mode.BAG }
        assertTrue(e.openBagAt(lamp)); assertTrue(e.bagOpen); assertEquals(1,e.bagPage)
        val person=w.addPerson(Species.FOLK,Look(),1f,place,1.4f,place.floor)
        lamp.mode=Mode.FREE; lamp.place=place; s.give(person,lamp,Part.HAND)
        person.mode=Mode.BAG; person.place=null; lamp.place=null
        e.bagOpen=false
        assertTrue(e.openBagAt(lamp)); assertTrue(e.bagOpen); assertEquals(1,e.bagPage)
    }

    @Test fun lanternPicturesAreVisibleAndReducedMotionKeepsThemStill() {
        val w=World(); val s=Sim(w)
        val lamp=w.addThing(ThingType.TROLL_LANTERN,0,place,1.1f,place.floor).apply { used=3; resting=true }
        val e=Engine(w,place,s,host,false,0f).apply { setSize(1920f,1200f,1.5f); photoMode=true; skip=2 or 4 }
        val density=Density(1.5f)
        val text=TextMeasurer(createFontFamilyResolver(InstrumentationRegistry.getInstrumentation().targetContext),density,LayoutDirection.Ltr)
        val bitmap=Bitmap.createBitmap(1920,1200,Bitmap.Config.ARGB_8888)
        fun frame():IntArray {
            CanvasDrawScope().draw(density,LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(1920f,1200f)) { e.draw(this,text) }
            return IntArray(1920*1200).also { bitmap.getPixels(it,0,1920,0,0,1920,1200) }
        }
        fun crop(pixels:IntArray):IntArray {
            val out=ArrayList<Int>()
            val centerY=maxOf(place.ceiling+0.17f,lamp.y-0.55f)
            for(y in (1200-e.u+(centerY-0.15f)*e.u).toInt()..(1200-e.u+(centerY+0.15f)*e.u).toInt())
                for(x in ((lamp.x-0.24f)*e.u).toInt()..((lamp.x+0.24f)*e.u).toInt()) out+=pixels[y*1920+x]
            return out.toIntArray()
        }
        try {
            repeat(25) { e.update(0.016f) }
            val lit=crop(frame())
            repeat(70) { e.update(0.016f) }
            assertArrayEquals("projection does not move with animations off",lit,crop(frame()))
            lamp.used=0
            val dark=crop(frame())
            assertTrue("the troll is actually projected above the lamp",lit.indices.count { lit[it]!=dark[it] }>1000)
            val stove=s.designer.add(place,FixtureType.WOOD_STOVE,0,lamp.x,place.floor)!!
            e.skip=4
            val pictureX=(lamp.x*e.u).toInt()
            val pictureY=(1200-e.u+maxOf(place.ceiling+0.17f,lamp.y-0.55f)*e.u).toInt()
            val coveredOff=frame()[pictureY*1920+pictureX]
            lamp.used=2
            assertNotEquals("the picture remains visible across the stove pipe",coveredOff,frame()[pictureY*1920+pictureX])
            w.fixtures.remove(stove.id); s.invalidate(place); e.skip=2 or 4
            val person=w.addPerson(Species.FOLK,Look(),1f,place,1.1f,place.floor)
            s.give(person,lamp,Part.HAND)
            repeat(10) { e.update(0.016f) }
            lamp.used=2; val carried=crop(frame())
            lamp.used=0; val carriedOff=crop(frame())
            assertTrue("a friend carrying the lamp still projects its picture",carried.indices.count { carried[it]!=carriedOff[it] }>1000)
            w.night=true; repeat(200) { e.update(0.016f) }
            val px=((lamp.x+0.16f)*e.u).toInt()
            val py=(1200-e.u+(lamp.y-lamp.h/2)*e.u).toInt()
            val unlit=frame()[py*1920+px]
            lamp.used=1; val glowing=frame()[py*1920+px]
            fun brightness(c:Int)=(c shr 16 and 255)+(c shr 8 and 255)+(c and 255)
            assertTrue("a carried lantern brightens the room beside its beam",brightness(glowing)>brightness(unlit)+30)
            person.mode=Mode.BAG; person.place=null
            val packed=crop(frame())
            lamp.used=0
            assertArrayEquals("a packed friend's light stays inside the bag",packed,crop(frame()))
        } finally { e.cancel(); bitmap.recycle() }
    }

    @Test fun theThreeNotesCanBeReachedAmongTheOriginalFurnitureAndFriends() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val out=java.io.File(context.filesDir,"treasure-scenes").apply { mkdirs() }
        for(phone in listOf(false,true)) {
            val label=if(phone) "phone" else "tablet"
            val width=if(phone) 2400 else 1920
            height=if(phone) 1080f else 1200f
            val d=if(phone) 2.625f else 1.5f
            val density=Density(d)
            val text=TextMeasurer(createFontFamilyResolver(context),density,LayoutDirection.Ltr)
            val bitmap=Bitmap.createBitmap(width,height.toInt(),Bitmap.Config.ARGB_8888)
            val w=WorldFactory.create(); val s=Sim(w)
            s.magic.start(Adventure.RUMLE)
            fun engine(place:PlaceId,x:Float):Engine {
                w.place=place
                return Engine(w,place,s,host,false,0f).apply {
                    compact=phone; setSize(width.toFloat(),height,d); focusOn(x); photoMode=true
                    repeat(20) { update(0.016f) }
                }
            }
            fun capture(e:Engine,name:String) {
                CanvasDrawScope().draw(density,LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(width.toFloat(),height)) { e.draw(this,text) }
                java.io.File(out,"$label-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            }
            try {
                for(i in 0..2) {
                    val paper=s.trail.note(i)!!
                    val e=engine(paper.place!!,paper.x)
                    try {
                        capture(e,"clue-$i")
                        tap(e,paper.x,paper.y-paper.h/2)
                        assertTrue("$label clue $i is reachable in the original scene",s.trail.found(i))
                    } finally { e.cancel() }
                }
                assertTrue(s.trail.complete)
                val lamp=s.trail.lamp()!!
                lamp.mode=Mode.FREE; lamp.place=place; lamp.x=1.1f; lamp.y=place.floor; lamp.ground=place.floor
                w.night=true
                val e=engine(place,lamp.x)
                try {
                    for(mode in 1..3) { s.use(place,lamp); assertEquals(mode,lamp.used); capture(e,"lantern-$mode") }
                    val friend=w.people().first { it.name=="Hedda" }
                    House.moveTo(w,friend,place,1.1f,place.floor)
                    s.give(friend,lamp,Part.HAND); repeat(20) { e.update(0.016f) }
                    capture(e,"lantern-carried")
                } finally { e.cancel() }
            } finally { bitmap.recycle() }
        }
    }
}
