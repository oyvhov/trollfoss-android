package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.hypot

class RumleVisitTest {
    private fun follow(walk: HighlandsWalk, expected: BergAction) {
        walk.followRecipe()
        repeat(1600) {
            if(walk.destination!=null) walk.step(.04f)
        }
        assertNull("Must actually arrive, not teleport",walk.destination)
        assertEquals(expected,walk.nearby()?.action)
    }

    @Test fun walkTheWholeRecipeSaveTheSteepingPotAndComeBackForTea() {
        val world=WorldFactory.create()
        val state=world.highlands
        state.bag.add(BergItem.DIAMOND);state.held=BergItem.DIAMOND
        val walk=HighlandsWalk(state)
        follow(walk,BergAction.BERRIES)
        assertEquals(BergEvent.BERRIES,walk.use())
        assertEquals(BergEvent.BASKET_FULL,walk.use())
        follow(walk,BergAction.ENTER);assertEquals(BergEvent.TRAVEL,walk.use())
        follow(walk,BergAction.KETTLE);assertEquals(BergEvent.FILL,walk.use())
        assertEquals(BergEvent.STIR,walk.use())
        val restored=WorldStore.decode(WorldStore.encode(world,Settings())).world
        assertEquals(BerryPot.STEEPED,restored.highlands.pot)
        val next=HighlandsWalk(restored.highlands)
        assertEquals(BergEvent.POUR,next.use())
        follow(next,BergAction.TROLL);assertEquals(BergEvent.OFFER,next.use())
        assertEquals(BergEvent.NONE,next.use())
        val sounds=List(100){next.step(.045f)}.filter { it!=BergEvent.NONE }
        assertEquals(listOf(BergEvent.TOAST,BergEvent.SIP),sounds)
        assertEquals(RumleGesture.REST,next.rumle.gesture)
        val saved=WorldStore.decode(WorldStore.encode(restored,Settings())).world.highlands
        assertTrue(saved.sharedJuice);assertEquals(BerryPot.EMPTY,saved.pot)
        assertEquals(setOf(BergItem.DIAMOND),saved.bag)
        follow(next,BergAction.EXIT);assertEquals(BergEvent.TRAVEL,next.use())
        follow(next,BergAction.BERRIES);assertEquals(BergEvent.BERRIES,next.use())
    }

    @Test fun takingTheServingFromAcrossTheRugRequiresWalkingAndCanBeInterrupted() {
        val s=HighlandsState().apply {cave=true;x=-3f;z=1.6f;held=BergItem.PEPSI;bag.add(held)}
        val walk=HighlandsWalk(s)
        assertEquals(BergEvent.NONE,walk.use());assertNotNull(walk.destination)
        assertEquals(BergItem.PEPSI,s.held)
        walk.forward=-1f;walk.step(.05f);walk.forward=0f
        assertNull(walk.destination);assertEquals(BergItem.PEPSI,s.held)
        walk.use();val events=List(170){walk.step(.05f)}.filter {it!=BergEvent.NONE}
        assertEquals(listOf(BergEvent.OFFER,BergEvent.GULP,BergEvent.BURP,BergEvent.FART),events)
        assertTrue(hypot(s.x-BergKitchen.serving.x,s.z-BergKitchen.serving.z)<.25f)
        assertFalse(BergItem.PEPSI in s.bag);assertEquals(BergItem.PEPSI,s.lastBottle)
    }

    @Test fun changingLeaderOrVisitingTheBagCancelsAnUnfinishedDelivery() {
        val s=HighlandsState().apply {cave=true;x=-3f;z=.2f;held=BergItem.BERRY_CUP;bag.add(held)}
        val walk=HighlandsWalk(s)
        walk.use();s.leader=23;repeat(100){walk.step(.05f)}
        assertEquals(BergItem.BERRY_CUP,s.held);assertFalse(s.sharedJuice)
        s.x=-3f;s.z=.2f;walk.use();walk.stop();repeat(100){walk.step(.05f)}
        assertEquals(BergItem.BERRY_CUP,s.held);assertFalse(s.sharedJuice)
    }

    @Test fun filledCupCannotBeOverwrittenAndTheSecondPotCanWait() {
        val s=HighlandsState().apply {cave=true;x=BergKitchen.hearth.x;z=BergKitchen.hearth.z}
        val walk=HighlandsWalk(s)
        assertEquals(BergEvent.NEED_BERRIES,walk.use())
        s.bag.add(BergItem.BERRIES);s.bag.add(BergItem.BERRY_CUP);s.bag.add(BergItem.GOLD);s.held=BergItem.GOLD
        assertEquals(BergEvent.FILL,walk.use());assertEquals(BergItem.GOLD,s.held)
        assertEquals(BergEvent.STIR,walk.use());assertEquals(BergEvent.CUP_FULL,walk.use())
        assertEquals(BerryPot.STEEPED,s.pot)
        follow(walk,BergAction.TROLL);assertEquals(BergEvent.OFFER,walk.use())
        follow(walk,BergAction.KETTLE);assertEquals(BergEvent.POUR,walk.use())
        assertEquals(setOf(BergItem.GOLD,BergItem.BERRY_CUP),s.bag)
    }

    @Test fun doorwayWaveLeavesTheCaveEntranceAndHeldDrinkAlone() {
        val s=HighlandsState().apply {x=13f;z=-11f;held=BergItem.URGE;bag.add(held);yaw=.8f}
        val walk=HighlandsWalk(s)
        assertEquals(BergEvent.WAVE,walk.waveOutside())
        repeat(20){walk.step(.04f)}
        assertTrue(walk.visitFocus>.5f);assertEquals(.8f,s.yaw,.001f)
        walk.zoomBy(1.1f);repeat(60){walk.step(.04f)}
        assertEquals(0f,walk.visitFocus,.001f);assertEquals(BergItem.URGE,s.held)
        assertEquals(BergEvent.TRAVEL,walk.use());assertEquals(RumleGesture.REST,walk.rumle.gesture)
    }

    @Test fun olderAndUnrecognisedKitchenDataLeaveExistingTreasuresIntact() {
        val world=WorldFactory.create()
        world.highlands.apply {bag.add(BergItem.GOLD);held=BergItem.GOLD;light=true;taken=3}
        val json=WorldStore.encode(world,Settings())
        val data=json.getJSONObject("highlands")
        for(key in listOf("berryPot","sharedJuice","lastBottle")) data.remove(key)
        for(i in 0..1) {
            if(i==1) {data.put("berryPot","OTHER");data.put("lastBottle","GOLD")}
            val restored=WorldStore.decode(json).world.highlands
            assertEquals(BerryPot.EMPTY,restored.pot);assertEquals(BergItem.NONE,restored.lastBottle)
            assertFalse(restored.sharedJuice);assertTrue(restored.light);assertEquals(BergItem.GOLD,restored.held)
        }
    }
}
