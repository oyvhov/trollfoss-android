package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class CaveVisitTest {
    @Test fun findTheMouseAndWakeEachCapWithoutLosingThePicnicOrTheSong() {
        val world=WorldFactory.create()
        val state=world.highlands.apply {
            cave=true;x=0f;z=10f
            bag.add(BergItem.CAKE);held=BergItem.CAKE;song.add(EchoNote(2,5))
        }
        val walk=HighlandsWalk(state)
        assertEquals(BergEvent.NONE,walk.touchSpring(1))
        assertEquals(BergEvent.NONE,walk.callMouse())
        walk.walkTo(CaveMouse.calling)
        repeat(550){walk.step(.05f)}
        assertEquals(BergAction.MOUSE,walk.nearby()?.action)
        assertTrue(Highlands.cameraDistance(-10f,2f,0f,.18f,8f)<4.9f)
        assertEquals(BergEvent.MOUSE,walk.use())
        assertEquals(BergEvent.NONE,walk.callMouse())
        repeat(40){walk.step(.05f)}
        assertTrue(walk.roomFocus>.95f)
        walk.zoomBy(1.2f);repeat(38){walk.step(.05f)}
        assertTrue(walk.roomFocus<.02f)
        repeat(156){walk.step(.05f)}
        assertEquals(-1f,walk.caveMouseAge,0f)
        val bank=BergSpring.bank
        walk.walkTo(BergPoint(bank.x,bank.z))
        repeat(650){walk.step(.05f)}
        assertEquals(bank,walk.nearby())
        assertFalse(Highlands.walkable(BergSpring.X,BergSpring.Z,true))
        assertEquals(BergEvent.SPRING,walk.use())
        for(key in listOf(2,0,1)){
            assertEquals(BergEvent.SPRING,walk.touchSpring(key))
            assertEquals(key,walk.springKey)
        }
        val next=WorldStore.decode(WorldStore.encode(world,Settings())).world.highlands
        assertTrue(next.springAwake);assertEquals(BergItem.CAKE,next.held)
        assertEquals(listOf(EchoNote(2,5)),next.song)
        next.cave=false
        assertEquals(BergEvent.NONE,HighlandsWalk(next).touchSpring())
    }
    @Test fun roomDropsHaveOneImpactPerVisibleLandingAndPauseWithTheRoom() {
        val walk=HighlandsWalk(HighlandsState().apply {cave=true})
        val impacts=mutableListOf<Float>()
        repeat(538){walk.step(.04f);if(walk.springHit) impacts+=walk.time}
        assertEquals(4,impacts.size)
        assertEquals(4.15f,impacts[0],.05f)
        assertEquals(10.6f,impacts[1],.05f)
        walk.state.cave=false
        repeat(320){walk.step(.04f);assertFalse(walk.springHit)}
    }
}
