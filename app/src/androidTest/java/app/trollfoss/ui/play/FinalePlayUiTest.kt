package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinalePlayUiTest {
    private val place = PlaceId.HOME
    private var radio = false
    private val host = object : EngineHost {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id: String) {}
        override fun discovered(key: String) {}
        override fun telescope() {}
        override fun radio(on: Boolean) { radio = on }
        override fun egg(id: String) {}
        override fun passage(passage: Passage, arrivalX: Float) {}
    }
    private var height = 1200f
    private var clock = 10000L
    private fun at(e: Engine,x: Float,y: Float) = Offset((x-e.cam)*e.u,height-e.u+y*e.u)
    private fun tap(e: Engine,x: Float,y: Float) {
        val p = at(e,x,y); clock+=500
        e.down(1,p,clock); e.up(1,p,clock+90); repeat(2) { e.update(0.016f) }
    }
    private fun drag(e: Engine,from: Offset,to: Offset) {
        clock+=1000; e.down(1,from,clock)
        e.move(1,(from+to)*0.5f,clock+80); repeat(3) { e.update(0.016f) }
        e.move(1,to,clock+180); repeat(3) { e.update(0.016f) }
        // Rest the finger over the seat before releasing, rather than throwing a friend at it.
        e.move(1,to,clock+350); repeat(3) { e.update(0.016f) }
        e.up(1,to,clock+450); repeat(2) { e.update(0.016f) }
    }
    private fun bothSizes(test: (World,Sim,Engine)->Unit) {
        for(phone in listOf(false,true)) {
            height = if(phone) 1080f else 1200f
            val w = World(); val s = Sim(w)
            val e = Engine(w,place,s,host,false,0f).apply { compact=phone; setSize(if(phone) 2400f else 1920f,height,if(phone) 2.625f else 1.5f) }
            try { test(w,s,e) } finally { e.cancel() }
        }
    }

    @Test fun twoFriendsCanBeDraggedIntoTheDragonAndTheArrowControlsDriveThem() = bothSizes { w,s,e ->
        val f = s.designer.add(place,FixtureType.PLAY_DRAGON_CART,0,1.2f,place.floor)!!
        val a = w.addPerson(Species.FOLK,Look(),0.9f,place,0.55f,place.floor)
        val b = w.addPerson(Species.FOLK,Look(),0.9f,place,1.95f,place.floor)
        repeat(12) { e.update(0.016f) }
        for((i,p) in listOf(a,b).withIndex()) {
            val seat = s.seatPoint(f,i)
            drag(e,at(e,p.x,p.y-p.h/2),at(e,seat[0],seat[1]+p.h*Anatomy.HIPS-p.h/2))
            assertEquals("friend ${i+1} sits in the dragon",Mode.SEATED,p.mode)
            assertEquals(f.id,p.holder)
        }
        tap(e,f.x+0.28f,f.y-0.19f)
        assertSame(f,e.vehicle); assertEquals(-1,e.toyFixtureId)
        val start = f.x; e.drive(1); repeat(24) { e.update(0.016f) }; e.drive(0)
        assertTrue(f.x>start+0.1f)
        assertEquals(Mode.SEATED,a.mode); assertEquals(Mode.SEATED,b.mode)
        assertTrue(First.DRAGON_CART.name in w.firsts)
    }

    @Test fun aThingDraggedIntoTheAirshipsBasketFollowsTheUpAndDownControls() = bothSizes { w,s,e ->
        val f = s.designer.add(place,FixtureType.PLAY_AIRSHIP,0,1.2f,place.floor)!!
        val t = w.addThing(ThingType.BALL,0,place,1.95f,place.floor)
        repeat(12) { e.update(0.016f) }
        drag(e,at(e,t.x,t.y-t.h/2),at(e,f.x+FinalePlay.CARGO_X,f.y-0.16f))
        assertEquals(Mode.INSIDE,t.mode); assertEquals(f.id,t.holder)
        tap(e,f.x-0.1f,f.y-0.50f)
        assertSame(f,e.vehicle)
        val y = f.y; e.dive(-1); repeat(30) { e.update(0.016f) }; e.drive(0)
        assertTrue(f.y<y-0.05f); assertEquals(f.y+FinalePlay.CARGO_Y,t.y,0.001f)
        assertTrue(First.AIRSHIP.name in w.firsts)
        e.dive(1); repeat(35) { e.update(0.016f) }; e.closeDriving()
        assertEquals(y,f.y,0.035f)
    }

    @Test fun tappingTheVisibleTrollThreeTimesCompletesTheGameWithoutADialog() = bothSizes { w,s,e ->
        val f = s.designer.add(place,FixtureType.PLAY_HIDE_TROLL,0,1.2f,place.floor)!!
        repeat(10) { e.update(0.016f) }
        tap(e,f.x,f.y-0.07f)
        repeat(3) {
            assertTrue(f.angle.toInt() in 1..3)
            tap(e,f.x+(f.angle.toInt()-2)*FinalePlay.HOLE_STEP,f.y-0.145f)
            assertEquals(-1,e.toyFixtureId)
        }
        assertTrue(First.HIDE_TROLL.name in w.firsts)
    }

    @Test fun thePartyButtonStartsAndStopsWithOneTapEach() = bothSizes { w,s,e ->
        val f = s.designer.add(place,FixtureType.PLAY_TROLL_PARTY,0,1.2f,place.floor)!!
        val p = w.addPerson(Species.FOLK,Look(),0.9f,place,1.65f,place.floor)
        repeat(10) { e.update(0.016f) }
        tap(e,f.x,f.y-0.08f)
        assertTrue(f.on); assertTrue(s.musicOn(place)); assertEquals(-1,e.toyFixtureId)
        assertEquals("reduced motion keeps the party dance still",0f,p.anim.dance,0f)
        tap(e,f.x,f.y-0.08f)
        assertFalse(f.on); assertFalse(s.musicOn(place))
        assertTrue(First.TROLL_PARTY.name in w.firsts)
        tap(e,f.x,f.y-0.08f)
        assertTrue(radio)
        assertTrue(s.designer.store(place,f))
        assertFalse("putting the party away stops its music",radio)
    }
}
