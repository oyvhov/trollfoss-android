package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test

/** Real pointer integration: decorations and gift wrapping use the same paths as ordinary play. */
class ChristmasEngineTest {
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
    private fun point(e:Engine,x:Float,y:Float,h:Float)=Offset((x-e.cam)*e.u,h-e.u+y*e.u)
    private fun drag(e:Engine,from:Offset,to:Offset,id:Long=1L) {
        e.down(id,from,1000);e.move(id,(from+to)/2f,1050)
        repeat(3){e.update(0.016f)}
        e.move(id,to,1100);repeat(3){e.update(0.016f)};e.up(id,to,1200)
    }
    @Test fun actualOrnamentDragAndRemovalWorksOnPhoneAndTablet() {
        for((width,height) in listOf(2400f to 1080f,1920f to 1200f)) {
            val w=World();val s=Sim(w);val p=PlaceId.CHRISTMAS_HOME
            val tree=s.designer.add(p,FixtureType.XM_TREE,0,1.4f,0.9f)!!
            val t=w.addThing(ThingType.XM_BAUBLE,0,p,0.4f,0.65f)
            val e=Engine(w,p,s,host,false,0f).apply {setSize(width,height,1.5f)}
            drag(e,point(e,t.x,t.y-t.h/2,height),point(e,tree.x,tree.y-0.35f,height))
            assertEquals(Mode.INSIDE,t.mode);assertEquals(tree.id,t.holder)
            val q=Christmas.ornamentPoint(t.slot)
            val at=point(e,tree.x+q.first,tree.y+q.second,height)
            e.down(2,at,2000);e.up(2,at,2100)
            assertEquals(Mode.FREE,t.mode);assertSame(t,w.bodies[t.id]);e.cancel()
        }
    }
    @Test fun twoFingersCanWrapDifferentToysWithoutDuplicatingThem() {
        val w=World();val s=Sim(w);val p=PlaceId.CHRISTMAS_WORKSHOP
        val a=s.designer.add(p,FixtureType.XM_WRAP,0,0.8f,0.9f)!!
        val b=s.designer.add(p,FixtureType.XM_WRAP,1,1.7f,0.9f)!!
        val first=w.addThing(ThingType.XM_TRAIN,0,p,0.35f,0.5f)
        val second=w.addThing(ThingType.XM_ROBOT,0,p,2.1f,0.5f)
        val e=Engine(w,p,s,host,false,0f).apply {setSize(2400f,1080f,1.5f)}
        e.down(1,point(e,first.x,first.y-first.h/2,1080f),1000)
        e.down(2,point(e,second.x,second.y-second.h/2,1080f),1000)
        e.move(1,point(e,a.x,a.top,1080f),1100);e.move(2,point(e,b.x,b.top,1080f),1100)
        repeat(4){e.update(0.016f)}
        e.up(1,point(e,a.x,a.top,1080f),1200);e.up(2,point(e,b.x,b.top,1080f),1200)
        assertEquals(setOf(first.id,second.id),w.christmas.parcels.values.toSet())
        assertEquals(2,w.christmas.parcels.size);e.cancel()
    }
    @Test fun accessibleCalendarAndSleighActionsOpenTheirPictureControls() {
        val w=World();val s=Sim(w);val p=PlaceId.CHRISTMAS_SQUARE
        val f=s.designer.add(p,FixtureType.XM_CALENDAR,0,1f,p.floor)!!
        val e=Engine(w,p,s,host,false,0f).apply {setSize(1920f,1200f,1.5f)}
        assertTrue(e.accessibleFixture(f.id));assertEquals(f.id,e.christmasFixtureId);e.cancel()
    }

    @Test fun musicBoxDanceSurvivesRealFramesAndStopsWhenSwitchedOff() {
        val w=World();val s=Sim(w);val place=PlaceId.CHRISTMAS_HOME
        val box=s.designer.add(place,FixtureType.XM_MUSICBOX,0,1f,place.floor)!!
        val person=w.addPerson(Species.FOLK,Look(),1f,place,1.3f,0.95f)
        val e=Engine(w,place,s,host,false,0f).apply {setSize(1920f,1200f,1.5f)}
        repeat(30){e.update(0.016f)}
        s.christmas.tap(box,0f,0f)
        repeat(30){e.update(0.016f)}
        assertTrue(s.musicOn(place));assertTrue(person.anim.dance>0f)
        s.christmas.tap(box,0f,0f);e.update(0.016f)
        assertFalse(s.musicOn(place));assertEquals(0f,person.anim.dance,0f);e.cancel()
    }
}
