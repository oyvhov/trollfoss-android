package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class HighlandsTest {
    @Test fun rampCameraStaysInsideTheFrontWallAndKeepsTheDoorwayOpen() {
        for(x in listOf(-8f,8f,16f)) {
            val distance=Highlands.cameraDistance(x,7f,0f,.25f,10f)
            assertTrue("Lens crossed the front shell at $x",7f+cos(.25f)*distance<14.3f)
            assertTrue(distance>5f)
        }
        // The exit opening still gives a longer view than its solid neighbours.
        assertTrue(Highlands.cameraDistance(0f,7f,0f,.25f,10f)>8f)
        assertTrue(Highlands.caveCameraCeiling(19.5f,4f)<Highlands.caveCameraCeiling(0f,4f))
        assertTrue(Highlands.cameraDistance(1f,-10f,PI.toFloat()/2,.25f,10f)<2f)
    }

    @Test fun larderCratesAreSolidAndBothDrinksAreReachableFromInFront() {
        val state=HighlandsState().apply {cave=true}
        val walk=HighlandsWalk(state)
        for((x,item) in listOf(11f to BergItem.PEPSI,15f to BergItem.URGE)) {
            assertFalse(Highlands.walkable(x,-20f,true))
            walk.walkTo(BergPoint(x,-18f))
            repeat(4000){walk.step(.025f)}
            assertTrue(hypot(state.x-x,state.z+18f)<.5f)
            assertEquals(BergEvent.PICK,walk.use());assertEquals(item,state.held)
        }
    }

    @Test fun washingFocusNeverTakesCameraControlAwayFromTheChildOrChangesSavedYaw() {
        val state=HighlandsState().apply {x=-14f;z=4.5f;yaw=.7f}
        val walk=HighlandsWalk(state)
        walk.use();repeat(15){walk.step(.05f)}
        assertTrue(walk.panFocus>.9f);assertEquals(.7f,state.yaw,0f)
        walk.lookSide=.8f;walk.step(.05f);walk.lookSide=0f
        repeat(20){walk.step(.05f)}
        assertTrue(walk.panFocus<.01f);assertTrue(state.yaw<.7f)
        repeat(60){walk.step(.05f)}
        walk.use();repeat(15){walk.step(.05f)};assertTrue(walk.panFocus>.9f)
        walk.walkTo(BergPoint(-13f,5f));repeat(20){walk.step(.05f)}
        assertTrue(walk.panFocus<.01f)
        walk.stop();repeat(60){walk.step(.05f)}
        state.x=-14f;state.z=4.5f;walk.use();repeat(15){walk.step(.05f)}
        walk.cancelPanFocus();repeat(20){walk.step(.05f)};assertTrue(walk.panFocus<.01f)
    }
    @Test fun goldWashingRevealsOneTreasureAndRemainsPlayableWithoutDuplicatingStoredGold() {
        val world=WorldFactory.create();val state=world.highlands
        state.apply {x=-14f;z=4.5f;bag.add(BergItem.CAKE);held=BergItem.CAKE}
        val walk=HighlandsWalk(state)
        assertEquals(BergEvent.WASH,walk.use())
        assertEquals(BergEvent.NONE,walk.use()) // repeated taps do not reset the little animation
        assertEquals(BergItem.CAKE,state.held);assertFalse(BergItem.GOLD in state.bag)
        assertEquals(listOf(BergEvent.PICK),List(70){walk.step(.05f)}.filter {it!=BergEvent.NONE})
        assertEquals(BergItem.GOLD,state.held);assertTrue(state.taken and 2!=0)
        val saved=WorldStore.decode(WorldStore.encode(world,Settings())).world.highlands
        assertTrue(saved.taken and 2!=0);assertTrue(BergItem.GOLD in saved.bag)
        // Put the only nugget into a chest; another wash is play, not a second inventory item.
        state.cave=true;state.x=-9f;state.z=-10f
        assertEquals(BergEvent.LIGHT,walk.use());assertTrue(state.treasure)
        state.cave=false;state.x=-14f;state.z=4.5f;state.held=BergItem.CAKE
        assertEquals(BergEvent.WASH,walk.use())
        assertEquals(listOf(BergEvent.LIGHT),List(70){walk.step(.05f)}.filter {it!=BergEvent.NONE})
        assertEquals(BergItem.CAKE,state.held);assertFalse(BergItem.GOLD in state.bag);assertTrue(state.treasure)
    }

    @Test fun leavingAnUnfinishedWashDoesNotTakeTheGoldAndItCanBeStartedAgain() {
        val state=HighlandsState().apply {x=-14f;z=4.5f}
        val walk=HighlandsWalk(state)
        assertEquals(BergEvent.WASH,walk.use());repeat(10){walk.step(.05f)}
        state.x=-18f;walk.step(.05f)
        assertEquals(0f,walk.panPlay,0f);assertEquals(0,state.taken)
        state.x=-14f;assertEquals(BergEvent.WASH,walk.use())
        state.x=13f;state.z=-11f
        assertEquals(BergEvent.TRAVEL,walk.use());assertEquals(0f,walk.panPlay,0f)
        assertEquals(0,state.taken);assertTrue(state.bag.isEmpty())
    }

    @Test fun aCloseCameraDoesNotDisappearInsideTheLeaderOrATallFollower() {
        val distance=Highlands.cameraDistance(-9f,-7f,0f,.25f,10f)
        val eyeZ=-7f+cos(.25f)*distance
        val eyeY=1.1f+sin(.25f)*distance
        assertEquals(0f,Highlands.cameraPersonOpacity(-9f,eyeY,eyeZ,-9f,0f,-7f,1f),0f)
        assertEquals(0f,Highlands.cameraPersonOpacity(0f,2f,1f,0f,0f,1.1f,1.4f),0f)
        assertEquals(1f,Highlands.cameraPersonOpacity(0f,3f,10f,0f,0f,0f,1f),0f)
        assertEquals(1f,Highlands.cameraPersonOpacity(0f,6f,0f,0f,0f,0f,1f),0f)
        val fade=(0..25).map {Highlands.cameraPersonOpacity(0f,1.6f,it*.1f,0f,0f,0f,1f)}
        assertTrue(fade.zipWithNext().all {(a,b)->b>=a})
        assertTrue(fade.any {it in .1f.. .9f})
    }

    @Test fun aNearbyButOutOfReachHareDoesNotHideTheReachableGoldPan() {
        val state=HighlandsState().apply {x=-16.1f;z=4.15f}
        val walk=HighlandsWalk(state)
        assertTrue(Highlands.walkable(state.x,state.z,false))
        assertEquals(BergAction.GOLD,walk.nearby()?.action)
        assertEquals(BergEvent.WASH,walk.use())
        repeat(50){walk.step(.05f)}
        assertEquals(BergItem.GOLD,state.held)
        assertEquals(BergAction.GOLD,walk.nearby()?.action)
        state.x=-15f;state.z=6f
        assertEquals(BergAction.HARE,walk.nearby()?.action)
    }
    @Test fun caveCameraClearsTheHearthAtTheCrystalsAndCanStillLookOverLowFurniture() {
        // Actual phone/tablet captures placed the lens above the hearth: its
        // huge near surface covered the lower third of the crystal activity.
        assertTrue(Highlands.cameraDistance(-15.7f,-15.8f,0f,.25f,10f) in 7f..9f)
        assertEquals(10f,Highlands.cameraDistance(-15.7f,-15.8f,0f,.8f,10f),.001f)
        // A clear outward view remains long, while orbiting into the rear pantry stops.
        assertEquals(10f,Highlands.cameraDistance(14.5f,-18f,0f,.25f,10f),.001f)
        assertTrue(Highlands.cameraDistance(14.5f,-18f,PI.toFloat(),.25f,10f) in .8f..2f)
        assertTrue(Highlands.cameraDistance(-3f,-1f,PI.toFloat(),.25f,10f) in .8f..2f)
    }
    @Test fun singingCrystalsHaveDistinctNotesPreserveHeldThingsAndClearOnTravel() {
        val state=HighlandsState().apply {cave=true;x=0f;z=10f;bag.add(BergItem.CAKE);held=BergItem.CAKE}
        val walk=HighlandsWalk(state)
        assertEquals(3,Highlands.crystalPitches.distinct().size)
        Highlands.crystals.forEachIndexed {index,p ->
            assertFalse(Highlands.walkable(p.x,p.z,true))
            state.x=p.x;state.z=p.z+2f
            assertEquals(BergAction.CRYSTAL,walk.nearby()?.action)
            assertEquals(BergEvent.NOTE,walk.use());assertEquals(index,walk.crystalIndex)
            assertTrue(walk.crystalPlay>0f)
            repeat(12){walk.step(.05f)}
            assertEquals(BergEvent.NOTE,walk.use()) // freely replayable, no cooldown or cost
            assertEquals(BergItem.CAKE,state.held);assertEquals(setOf(BergItem.CAKE),state.bag)
        }
        repeat(40){walk.step(.05f)}
        assertEquals(0f,walk.crystalPlay,0f)
        assertEquals(BergEvent.NOTE,walk.use())
        state.x=0f;state.z=12f
        assertEquals(BergEvent.TRAVEL,walk.use());assertEquals(0f,walk.crystalPlay,0f);assertEquals(-1,walk.crystalIndex)
        assertEquals(BergEvent.NONE,walk.use())
    }
    @Test fun rumleTakesTheCakeAndChewsWithoutStartingTheSodaJoke() {
        val state=HighlandsState().apply {cave=true;x=BergKitchen.serving.x;z=BergKitchen.serving.z;bag.add(BergItem.CAKE);held=BergItem.CAKE}
        val walk=HighlandsWalk(state)
        assertEquals(BergEvent.OFFER,walk.use())
        assertFalse(BergItem.CAKE in state.bag);assertEquals(BergItem.NONE,state.held)
        assertEquals(RumleGesture.EAT,walk.rumle.gesture)
        assertEquals(listOf(BergEvent.CAKE),List(70){walk.step(.05f)}.filter {it!=BergEvent.NONE})
        assertEquals(RumleGesture.REST,walk.rumle.gesture)
        state.bag.add(BergItem.CAKE);state.held=BergItem.CAKE;walk.use()
        state.x=0f;state.z=12f;walk.use()
        assertEquals(RumleGesture.REST,walk.rumle.gesture)
    }
    @Test fun skippingStonesMakesThreeSplashesWithoutSpendingTreasureAndCanBeRepeated() {
        val state=HighlandsState().apply {x=5f;z=7f;bag.add(BergItem.DIAMOND);held=BergItem.DIAMOND}
        val walk=HighlandsWalk(state)
        assertTrue(Highlands.walkable(state.x,state.z,false))
        assertTrue(Highlands.skipLandings.all {Highlands.height(it.x,it.z)<Highlands.WATER})
        assertEquals(BergEvent.SKIP,walk.use())
        val effects=List(90){walk.step(.05f)}.filter {it!=BergEvent.NONE}
        assertEquals(listOf(BergEvent.SPLASH,BergEvent.SPLASH,BergEvent.SPLASH),effects)
        assertEquals(0f,walk.skipPlay,0f);assertEquals(BergItem.DIAMOND,state.held)
        assertEquals(setOf(BergItem.DIAMOND),state.bag);assertEquals(5f,state.x,0f);assertEquals(7f,state.z,0f)
        assertEquals(BergEvent.SKIP,walk.use())
        state.x=13f;state.z=-11f
        assertEquals(BergEvent.TRAVEL,walk.use());assertEquals(0f,walk.skipPlay,0f)
    }
    @Test fun rearChestKeepsItsGoldAcrossSavingAndCannotBeEmptiedFromTheOtherRoom() {
        val world=WorldFactory.create();val state=world.highlands
        state.apply {cave=true;x=-13f;z=-23f;bag.add(BergItem.GOLD);held=BergItem.GOLD}
        val walk=HighlandsWalk(state)
        assertEquals(BergEvent.LIGHT,walk.use());assertEquals(1,state.treasureSlot)
        val loaded=WorldStore.decode(WorldStore.encode(world,Settings())).world.highlands
        assertTrue(loaded.treasure);assertEquals(1,loaded.treasureSlot)
        val again=HighlandsWalk(loaded)
        loaded.x=-9f;loaded.z=-10f
        assertEquals(BergEvent.GOLD_ELSEWHERE,again.use());assertFalse(BergItem.GOLD in loaded.bag)
        loaded.x=-13f;loaded.z=-23f
        assertEquals(BergEvent.PICK,again.use());assertEquals(BergItem.GOLD,loaded.held)
        assertFalse(loaded.treasure)
    }
    @Test fun greetingAHareIsRepeatableAndDoesNotConsumeOrMoveAnything() {
        val state=HighlandsState().apply {x=17f;z=-5f;bag.add(BergItem.CAKE);held=BergItem.CAKE}
        val walk=HighlandsWalk(state)
        assertEquals(BergEvent.HARE,walk.use());assertEquals(1,walk.hareIndex)
        assertEquals(5f,walk.harePlay,0f)
        repeat(120){walk.step(.05f)}
        assertEquals(0f,walk.harePlay,0f);assertEquals(BergItem.CAKE,state.held)
        assertEquals(17f,state.x,0f);assertEquals(-5f,state.z,0f)
        assertEquals(BergEvent.HARE,walk.use())
    }
    @Test fun lookoutFacesTheFjordAndTheEntranceRockCannotBeEnteredOrOrbitedThrough() {
        val p=Highlands.lookout
        val state=HighlandsState().apply {x=p.x;z=p.z;yaw=.7f}
        val walk=HighlandsWalk(state);walk.forward=.8f
        assertEquals(BergEvent.LOOKOUT,walk.use())
        assertEquals(PI.toFloat(),state.yaw,.001f);assertEquals(16f,walk.distance,0f)
        assertEquals(0f,walk.forward,0f);assertEquals(.25f,walk.pitch,0f)
        assertEquals(16f,Highlands.outsideCameraDistance(p.x,p.z,state.yaw,walk.pitch,16f),.001f)
        assertEquals(BergEvent.LOOKOUT,walk.use());assertEquals(10f,walk.distance,0f)
        assertFalse(Highlands.walkable(17f,-20f,false))
        assertTrue(Highlands.walkable(13f,-11f,false))
        assertTrue(Highlands.outsideCameraDistance(13f,-11f,PI.toFloat(),.25f,10f) in 4f..5f)
        assertEquals(10f,Highlands.outsideCameraDistance(13f,-11f,0f,.25f,10f),.001f)
        state.x=17f;state.z=-20f;state.repair()
        assertTrue(Highlands.walkable(state.x,state.z,false))
    }
    @Test fun caveCameraStopsBeforePillarsAndEntranceWallButKeepsAnOpenViewLong() {
        assertTrue(Highlands.cameraDistance(-8f,-20f,0f,.25f,10f) in 1f..3f)
        assertTrue(Highlands.cameraDistance(-13f,-20f,.4f,.25f,10f) in 3f..6f)
        assertTrue(Highlands.cameraDistance(0f,12f,0f,.25f,10f) in 3f..4f)
        assertEquals(10f,Highlands.cameraDistance(-14f,-5f,0f,.25f,10f),.001f)
        for(yaw in listOf(0f,1f,2f,3f,4f,5f,6f)) {
            val distance=Highlands.cameraDistance(19f,13f,yaw,.25f,10f)
            assertTrue(distance.isFinite() && distance in .8f..10f)
        }
    }
    @Test fun smallStickDeflectionsWalkGentlyAndFurnitureCannotBeWalkedThrough() {
        fun distance(tilt:Float):Float {
            val state=HighlandsState();val walk=HighlandsWalk(state);walk.forward=tilt
            repeat(25){walk.step(.02f)}
            return 18f-state.z
        }
        assertEquals(0f,distance(.1f),0f)
        assertTrue(distance(.4f) in .4f..1f)
        assertTrue(distance(1f)>distance(.4f)*2f)
        for(p in listOf(BergPoint(-8f,-2f),BergPoint(-17f,-6f),BergPoint(8.5f,-25f))) assertFalse(Highlands.walkable(p.x,p.z,true))
        for(x in listOf(-19f,-9f,7f,19f)) {
            assertFalse(Highlands.walkable(x,-15f,true))
            assertTrue(Highlands.walkable(x,-13.5f,true))
        }
        val state=HighlandsState().apply {cave=true;x=-8f;z=2f};val walk=HighlandsWalk(state);walk.forward=1f
        repeat(300){walk.step(.02f)}
        assertTrue(state.z>-.7f)
        assertEquals(BergAction.CAKE,walk.nearby()?.action)
    }
    @Test fun twinSticksMoveAndOrbitTogetherAndStopOnPause() {
        val state=HighlandsState();val walk=HighlandsWalk(state)
        walk.forward=1f;walk.lookSide=.7f;walk.lookUp=1f
        repeat(30) {walk.step(.02f)}
        assertTrue(state.z<17f);assertTrue(state.x>.2f);assertTrue(state.yaw<-.5f)
        assertEquals(.15f,walk.pitch,.001f)
        walk.stop();val position=state.x to state.z;val yaw=state.yaw
        repeat(30){walk.step(.02f)}
        assertEquals(position,state.x to state.z);assertEquals(yaw,state.yaw,0f)
        walk.facing=1.2f;walk.resetCamera()
        assertEquals(1.2f-PI.toFloat(),state.yaw,.001f)
    }
    @Test fun allRoutesAreReachableAndStayOnDryConnectedGround() {
        for(cave in listOf(false,true)) {
            val start=BergPoint(0f,if(cave) 10f else 18f)
            for(spot in Highlands.spots.filter { it.cave==cave }) {
                val route=Highlands.route(start,BergPoint(spot.x,spot.z),cave)
                assertTrue("No route to ${spot.action}",route.isNotEmpty())
                assertTrue(route.all { Highlands.walkable(it.x,it.z,cave) })
                assertTrue(route.zipWithNext().all { (a,b) -> Highlands.canStep(a.x,a.z,b.x,b.z,cave) })
            }
        }
    }
    @Test fun walkingAndOrbitUseCameraRelativeDirectionsAndNeverLeaveTheWorld() {
        val s=HighlandsState();val walk=HighlandsWalk(s)
        walk.forward=1f;repeat(30) { walk.step(.02f) }
        assertTrue(s.z<18f);assertEquals(0f,s.x,.01f)
        val x=s.x;s.yaw=(PI/2).toFloat();repeat(30) {walk.step(.02f)}
        assertTrue(s.x<x)
        repeat(10000) {walk.step(.04f)}
        assertTrue(Highlands.walkable(s.x,s.z,false))
        walk.stop();val stopped=s.x to s.z;repeat(30){walk.step(.05f)}
        assertEquals(stopped,s.x to s.z)
    }
    @Test fun tapWalkActuallyCompletesTheRouteToTheCave() {
        val s=HighlandsState();val walk=HighlandsWalk(s)
        walk.walkTo(BergPoint(13f,-11f))
        repeat(2000){walk.step(.025f)}
        assertTrue("Stopped at ${s.x},${s.z}",hypot(s.x-13f,s.z+11f)<.5f)
        assertEquals(BergAction.ENTER,walk.nearby()?.action)
        assertEquals(BergEvent.TRAVEL,walk.use());assertTrue(s.cave)
        s.z=12f;assertEquals(BergEvent.TRAVEL,walk.use());assertFalse(s.cave)
        assertTrue(Highlands.walkable(s.x,s.z,false))
    }
    @Test fun treasureIsPhysicalAndReversibleAndSodaHasTwoDelayedJokes() {
        val s=HighlandsState();val walk=HighlandsWalk(s)
        s.x=-12f;s.z=10f;assertEquals(BergEvent.PICK,walk.use())
        assertTrue(BergItem.DIAMOND in s.bag)
        s.x=-9f;s.z=8f;assertEquals(BergEvent.LIGHT,walk.use());assertTrue(s.light)
        assertFalse(BergItem.DIAMOND in s.bag)
        assertEquals(BergEvent.PICK,walk.use());assertFalse(s.light)
        s.cave=true;s.x=9f;s.z=-5f;assertEquals(BergEvent.PICK,walk.use());assertEquals(BergItem.URGE,s.held)
        s.x=BergKitchen.serving.x;s.z=BergKitchen.serving.z;assertEquals(BergEvent.OFFER,walk.use());assertEquals(BergItem.NONE,s.held)
        val events=List(100){walk.step(.05f)}.filter {it!=BergEvent.NONE}
        assertEquals(listOf(BergEvent.GULP,BergEvent.BURP,BergEvent.FART),events)
        assertTrue(Highlands.walkable(s.x,s.z,true))
    }
    @Test fun oldSavesAndNewSavesPreservePeopleAndTheirDollhouseCoordinates() {
        val world=WorldFactory.create();val person=world.people().first {it.species==Species.FOLK}
        world.playerIds.add(person.id)
        val identity=person.look;val oldX=person.x;val oldY=person.y
        world.highlands.apply {x=13f;z=-11f;bag.add(BergItem.DIAMOND);held=BergItem.DIAMOND;shake=false;leader=person.id}
        val json=WorldStore.encode(world,Settings())
        val loaded=WorldStore.decode(json).world
        val again=loaded.bodies[person.id] as Person
        assertEquals(identity,again.look);assertEquals(oldX,again.x,0f);assertEquals(oldY,again.y,0f)
        assertEquals(world.playerIds,loaded.playerIds)
        assertEquals(BergItem.DIAMOND,loaded.highlands.held);assertFalse(loaded.highlands.shake)
        assertEquals(13f,loaded.highlands.x,0f)
        json.remove("highlands")
        val old=WorldStore.decode(json).world
        assertEquals(18f,old.highlands.z,0f);assertEquals(identity,(old.bodies[person.id] as Person).look)
    }
    @Test fun malformedPositionsCannotTrapThePlayerUnderWaterOrOutsideTheCave() {
        val s=HighlandsState();s.x=Float.NaN;s.z=Float.POSITIVE_INFINITY;s.yaw=Float.NaN;s.repair()
        assertTrue(Highlands.walkable(s.x,s.z,false));assertEquals(0f,s.yaw,0f)
        s.cave=true;s.x=99f;s.z=-99f;s.repair()
        assertTrue(Highlands.walkable(s.x,s.z,true))
    }

    @Test fun zoomKeepsTheFamilyStillAndRespectsCloseAndWideLimits() {
        val state=HighlandsState();val walk=HighlandsWalk(state)
        walk.zoomBy(2f);assertEquals(5f,walk.distance,0f)
        walk.zoomBy(100f);assertEquals(4f,walk.distance,0f)
        walk.zoomBy(.01f);assertEquals(16f,walk.distance,0f)
        for(scale in listOf(0f,-1f,Float.NaN,Float.POSITIVE_INFINITY)) walk.zoomBy(scale)
        assertEquals(16f,walk.distance,0f)
        assertEquals(0f,state.x,0f);assertEquals(18f,state.z,0f);assertEquals(0f,state.yaw,0f)
        walk.resetCamera();assertEquals(10f,walk.distance,0f)
    }

    @Test fun butterflyVisitIsRepeatableAndLeavesHeldThingsAndTheWorldUntouched() {
        val state=HighlandsState().apply {
            x=-3f;z=19f;leader=42;bag.add(BergItem.CAKE);held=BergItem.CAKE;taken=3;light=true
        }
        val walk=HighlandsWalk(state)
        assertEquals(BergAction.BUTTERFLY,walk.nearby()?.action)
        assertEquals(BergEvent.BUTTERFLY,walk.use())
        assertEquals(BergEvent.NONE,walk.use()) // repeated taps do not restart the visit
        repeat(60){walk.step(.05f)}
        assertTrue(walk.butterflyPlay>0f)
        assertEquals(BergItem.CAKE,state.held);assertEquals(setOf(BergItem.CAKE),state.bag)
        assertEquals(3,state.taken);assertTrue(state.light)
        assertEquals(-3f,state.x,0f);assertEquals(19f,state.z,0f)
        repeat(90){walk.step(.05f)}
        assertEquals(0f,walk.butterflyPlay,0f)
        assertEquals(BergEvent.BUTTERFLY,walk.use())
    }
    @Test fun butterflyNeverLocksWalkingOrFollowsANewLeaderIntoTheCave() {
        val state=HighlandsState().apply {x=-3f;z=19f;leader=42}
        val walk=HighlandsWalk(state)
        walk.use();walk.forward=1f;walk.step(.05f)
        assertTrue(state.z<19f);assertEquals(0f,walk.butterflyPlay,0f)
        walk.use();walk.walkTo(BergPoint(-1f,19f));walk.step(.05f)
        assertEquals(0f,walk.butterflyPlay,0f)
        walk.stop();walk.use();state.leader=43;walk.step(.05f)
        assertEquals(0f,walk.butterflyPlay,0f)
        walk.use();state.x=13f;state.z=-11f
        assertEquals(BergEvent.TRAVEL,walk.use());assertTrue(state.cave)
        assertEquals(0f,walk.butterflyPlay,0f)
    }

    @Test fun deepTreasuryAndUpperLarderCanBeWalkedToAndBack() {
        val state=HighlandsState().apply { cave=true;x=0f;z=10f }
        val walk=HighlandsWalk(state)
        for(target in listOf(BergPoint(-13f,-23f),BergPoint(15f,-18f),BergPoint(0f,12f))) {
            walk.walkTo(target)
            repeat(4000) { walk.step(.025f) }
            assertTrue("Unreachable cavern room: $target from ${state.x}, ${state.z}",
                hypot(state.x-target.x,state.z-target.z)<.5f)
        }
        assertEquals(BergAction.EXIT,walk.nearby()?.action)
    }
}
