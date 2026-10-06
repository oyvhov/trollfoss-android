package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class ToyPlayTest {
    @Test fun consecutiveGiftTrialsLeaveBothMachinesAccessible() {
        val s=Sim(World())
        val pump=s.toys.claim(ToyReward.PUMP,PlaceId.BEACH,1.1f)!!
        val camera=s.toys.claim(ToyReward.CAMERA,PlaceId.BEACH,1.1f)!!
        assertTrue(kotlin.math.abs(pump.x-camera.x)>(pump.spec.w+camera.spec.w)/2)
        assertEquals(2,s.world.fixtures.size)
    }
    @Test fun catalogueShowsEveryRewardAndUsesPermanentRightsInEveryPlace() {
        val w=World()
        for(place in PlaceId.entries) {
            val toys=Decor.catalogue(place).filter { Decor.reward(it)!=null }
            assertEquals(ToyReward.entries.size,toys.size)
            assertEquals(ToyReward.entries.filter { it.level==1 }.toSet(),toys.filter { Decor.available(w,it) }.map { Decor.reward(it) }.toSet())
        }
        w.stickers.addAll((0..Progression.thresholds.last()).toList());Progression.remember(w)
        val toys=Decor.catalogue(PlaceId.BEACH).filter { Decor.reward(it)!=null }
        assertFalse(Decor.available(w,toys.single { Decor.reward(it)==ToyReward.TRAIN }))
        w.stickers.clear()
        assertTrue(Decor.available(w,toys.single { Decor.reward(it)==ToyReward.BUS }))
        w.flags+="toy:TRAIN"
        assertTrue(toys.all { Decor.available(w,it) });assertTrue(w.stickers.isEmpty())
    }
    @Test fun legacyBrokenMarkerWorksEvenWithoutASeparateFixtureStateRow() {
        val s=Sim(World());s.toys.startTrain();val f=s.world.fixtures.values.single()
        f.variant=0
        val restored=WorldStore.decode(WorldStore.encode(s.world,Settings())).world
        assertTrue(Sim(restored).toys.broken(restored.fixtures[f.id]!!))
    }
    private fun fixture(s:Sim,reward:ToyReward,place:PlaceId=PlaceId.HOME):Fixture {
        s.world.stickers.addAll((0..Progression.thresholds.last()).toList())
        return requireNotNull(s.toys.claim(reward,place,1.1f))
    }
    @Test fun levelsRespectThresholdsAndNeverSpendOldStickers() {
        val w=World()
        assertEquals(1,Progression.level(w));assertEquals(2,Progression.missing(w))
        assertTrue(Progression.unlocked(w,ToyReward.CAMERA));assertFalse(Progression.unlocked(w,ToyReward.BUS))
        w.stickers.addAll(listOf(0,1));Progression.remember(w)
        assertEquals(2,Progression.level(w));assertEquals(2,Progression.missing(w))
        w.stickers.addAll((2..50).toList());Progression.remember(w)
        assertEquals(Progression.thresholds.size,Progression.level(w));assertEquals(0,Progression.missing(w));assertEquals(51,w.stickers.size)
        assertFalse(Progression.unlocked(w,ToyReward.TRAIN))
        w.stickers.clear();assertTrue(Progression.unlocked(w,ToyReward.BUS))
    }
    @Test fun lockedClaimDoesNothingAndEmptyHouseHasNoInvisibleReward() {
        val s=Sim(World());assertNull(s.toys.claim(ToyReward.BUS,PlaceId.BEACH,1f));assertTrue(s.world.fixtures.isEmpty())
        assertNull(s.toys.claim(ToyReward.PUMP,PlaceId.MINE_UPPER,1f))
    }
    @Test fun everyRewardCanMoveToEveryBuiltPlace() {
        for(place in PlaceId.entries) for(reward in ToyReward.entries) {
            val w=World();w.mine.started=true;w.mine.upperBuilt=true;w.flags+="toy:TRAIN"
            val s=Sim(w);val f=fixture(s,reward,place)
            assertEquals(place,f.place);assertTrue(s.designer.canStore(place,f));assertTrue(s.designer.store(place,f))
            val back=requireNotNull(s.designer.unstore(place,0,1f,place.floor));assertEquals(reward.type,back.type)
        }
    }
    @Test fun bubblesAreRealTargetsAndStartingAgainDoesNotDuplicateMachine() {
        val s=Sim(World());val f=fixture(s,ToyReward.BUBBLES)
        s.tap(f.place,f,0f,0f);val b=s.toys.bubbles.single();assertTrue(s.toys.pop(f.place,b.x,b.y));assertTrue(s.toys.bubbles.isEmpty())
        repeat(60) { s.step(f.place,0.02f) };assertTrue(s.toys.bubbles.isNotEmpty())
        s.tap(f.place,f,0f,0f);assertFalse(f.on);assertEquals(1,s.world.fixtures.size)
    }
    @Test fun windmillUsesTheOriginalDryerAndStops() {
        val s=Sim(World());val f=fixture(s,ToyReward.WINDMILL);s.toys.supply(f)
        val t=s.world.bodies.values.filterIsInstance<Thing>().single()
        assertTrue(s.dropInto(f.place,f,t));repeat(100) { s.step(f.place,0.02f) }
        assertTrue(f.angle>0f);assertSame(t,s.world.bodies[t.id]);assertEquals(Mode.FREE,t.mode)
        repeat(450) { s.step(f.place,0.02f) };assertFalse(f.on)
    }
    @Test fun pumpTransformsSameBallAndFillsReusableBucket() {
        val s=Sim(World());val f=fixture(s,ToyReward.PUMP);s.toys.supply(f)
        val ball=s.world.bodies.values.filterIsInstance<Thing>().first { it.type==ThingType.BALL }
        val bucket=s.world.bodies.values.filterIsInstance<Thing>().first { it.type==ThingType.BUCKET }
        assertTrue(s.dropInto(f.place,f,ball));assertEquals(ThingType.BEACH_BALL,ball.type);assertSame(ball,s.world.bodies[ball.id])
        assertTrue(s.dropInto(f.place,f,bucket));assertEquals(1,bucket.used)
    }
    @Test fun colorAndWashingKeepTheOriginalObjectAndColor() {
        val s=Sim(World());val f=fixture(s,ToyReward.COLORS)
        val t=s.world.addThing(ThingType.BALL,2,f.place,f.x,f.y);f.mode=4
        s.dropInto(f.place,f,t);assertEquals(4,t.variant);f.mode=1;s.dropInto(f.place,f,t)
        val sink=s.designer.add(f.place,FixtureType.SINK,0,2f,f.y)!!
        assertTrue(PlayInteractions.accepts(sink,t))
        assertFalse(s.dropInto(f.place,sink,t)) // Washing acts on a loose object; it does not take it inside.
        assertEquals(2,t.variant);assertSame(t,s.world.bodies[t.id])
    }
    @Test fun liftRaisesPassengerAndOriginalCargoAndStorageReleasesBoth() {
        val s=Sim(World());val f=fixture(s,ToyReward.LIFT)
        val p=s.world.addPerson(Species.FOLK,Look(),1f,f.place,f.x,f.y)
        val hat=s.world.addThing(ThingType.CAP,1,f.place,f.x,f.y);s.give(p,hat,Part.HAT);s.seat(p,f,0)
        val teddy=s.world.addThing(ThingType.TEDDY,0,f.place,f.x,f.y);s.dropInto(f.place,f,teddy)
        s.step(f.place,0.02f);val y=p.y;s.tap(f.place,f,0f,0f);repeat(100) { s.step(f.place,0.02f) }
        assertTrue(p.y<y-0.2f);assertEquals(f.y-0.045f-0.28f,teddy.y,0.002f)
        val saved=WorldStore.decode(WorldStore.encode(s.world,Settings())).world
        assertEquals(1f,saved.fixtures.getValue(f.id).angle,0.002f);assertEquals(Mode.INSIDE,saved.bodies[teddy.id]?.mode)
        assertTrue(s.designer.store(f.place,f));assertEquals(Mode.FREE,p.mode);assertEquals(Mode.FREE,teddy.mode);assertEquals(p.id,hat.holder)
    }
    @Test fun popcornIsMadeFromTheSameCornAndCanBeFed() {
        val s=Sim(World());val f=fixture(s,ToyReward.POPCORN);s.toys.supply(f)
        val corn=s.world.bodies.values.filterIsInstance<Thing>().single();val id=corn.id
        s.dropInto(f.place,f,corn);repeat(110) { s.step(f.place,0.02f) }
        assertEquals(ThingType.POPCORN,corn.type);assertEquals(Mode.FREE,corn.mode);assertSame(corn,s.world.bodies[id])
        val p=s.world.addPerson(Species.FOLK,Look(),1f,f.place,f.x,f.y)
        assertEquals(Give.ATE,s.give(p,corn,Part.MOUTH))
        s.toys.supply(f);assertTrue(s.world.bodies.values.filterIsInstance<Thing>().any { it.type==ThingType.PLAY_CORN })
    }
    @Test fun railsChangeTheOriginalBallsRouteAndReleaseIt() {
        val s=Sim(World());val f=fixture(s,ToyReward.MARBLES)
        val t=s.world.addThing(ThingType.BALL,0,f.place,f.x,f.y)
        f.mode=7;s.dropInto(f.place,f,t);repeat(30) { s.step(f.place,0.02f) };val x=t.x
        repeat(30) { s.step(f.place,0.02f) };assertNotEquals(x,t.x)
        repeat(190) { s.step(f.place,0.02f) };assertEquals(Mode.FREE,t.mode);assertSame(t,s.world.bodies[t.id])
        f.mode=0;s.dropInto(f.place,f,t);s.step(f.place,0.02f);assertEquals(Mode.FREE,t.mode)
    }
    @Test fun pillowLaunchesTheSameTeddyAndCanBeRepeated() {
        val s=Sim(World());val f=fixture(s,ToyReward.PILLOW)
        val t=s.world.addThing(ThingType.TEDDY,0,f.place,f.x,f.y)
        s.dropInto(f.place,f,t);s.tap(f.place,f,0f,0f)
        assertEquals(Mode.FREE,t.mode);assertTrue(t.vy<0f);assertTrue(t.vx>0f);assertSame(t,s.world.bodies[t.id])
    }
    @Test fun busAndTrainKeepBothPassengersAndClothesWhileDriving() {
        for(reward in listOf(ToyReward.BUS,ToyReward.TRAIN)) {
            val s=Sim(World());s.world.flags+="toy:TRAIN";val f=fixture(s,reward)
            val people=(0..1).map { i -> s.world.addPerson(Species.FOLK,Look(),1f,f.place,f.x+i*0.2f,f.y) }
            people.forEachIndexed { i,p -> s.seat(p,f,i) };val start=f.x
            s.vehicles.drive(f,1);repeat(30) { s.step(f.place,0.02f) }
            assertTrue(f.x>start);assertTrue(people.all { it.mode==Mode.SEATED && it.holder==f.id })
            val loaded=WorldStore.decode(WorldStore.encode(s.world,Settings())).world
            assertEquals(2,loaded.people().count { it.holder==f.id })
        }
    }
    @Test fun photographKeepsTheSittersLookThroughEditingStorageAndRestart() {
        val s=Sim(World());val f=fixture(s,ToyReward.CAMERA)
        val p=s.world.addPerson(Species.FOLK,Look(hair=6,eyeColor=4,hairLength=1.5f),1f,f.place,f.x,f.y)
        s.tap(f.place,f,0f,0f);val frame=s.world.fixtures.values.single { it.type==FixtureType.PICTURE }
        val photo=s.world.toyPhotos.values.single();p.look=p.look.copy(hair=0,eyeColor=0)
        assertEquals(6,photo.look.hair);assertEquals(4,photo.look.eyeColor)
        s.designer.store(f.place,frame)
        val loaded=WorldStore.decode(WorldStore.encode(s.world,Settings())).world
        val back=requireNotNull(Sim(loaded).designer.unstore(PlaceId.BEACH,0,1f,0.3f))
        assertEquals(frame.variant,back.variant);assertEquals(6,loaded.toyPhotos[back.variant-ToyPlay.PHOTO_BASE]?.look?.hair)
    }
    @Test fun suppliesRecallOnlyTheirOwnLooseThingsAndDoNotTakeHeldToys() {
        val s=Sim(World());val f=fixture(s,ToyReward.LIFT)
        val own=s.world.addThing(ThingType.TEDDY,0,PlaceId.BEACH,3f,0.9f)
        s.toys.supply(f);val kit=s.world.bodies.values.filterIsInstance<Thing>().first { it.id!=own.id };kit.held=true
        s.toys.supply(f);assertTrue(kit.held);assertEquals(2,s.world.bodies.size);assertEquals(PlaceId.BEACH,own.place)
        kit.held=false;s.dropInto(f.place,f,kit);s.toys.supply(f);assertEquals(Mode.INSIDE,kit.mode);assertEquals(2,s.world.bodies.size)
    }
    @Test fun trainRepairGrantsPermanentEntitlementOnlyOnceAndRecoversItsPart() {
        val s=Sim(World());s.toys.startTrain();val f=s.world.fixtures.values.single();val gear=s.world.bodies.values.filterIsInstance<Thing>().single()
        val x=f.x;s.vehicles.drive(f,1);s.step(f.place,0.02f);assertEquals(x,f.x,0f)
        s.toys.lifted(gear);assertEquals(1,s.toys.trainStage)
        assertTrue(s.dropInto(f.place,f,gear));assertEquals(2,s.toys.trainStage);assertEquals(Mode.BAG,gear.mode)
        val p=s.world.addPerson(Species.FOLK,Look(),1f,f.place,f.x,f.y);s.seat(p,f,0)
        assertEquals(3,s.toys.trainStage);assertTrue(Progression.unlocked(s.world,ToyReward.TRAIN));assertEquals(1,s.world.stickers.size)
        s.toys.seated(f);assertEquals(1,s.world.stickers.size)
        s.designer.store(f.place,f)
        val w=WorldStore.decode(WorldStore.encode(s.world,Settings())).world
        assertTrue(Progression.unlocked(w,ToyReward.TRAIN));assertNotNull(Sim(w).toys.claim(ToyReward.TRAIN,PlaceId.BEACH,1f))
    }
    @Test fun changingOneTaskPreservesOtherProgressAndIntroRewardsCannotDoubleCount() {
        val w=World();val s=Sim(w);val board=s.tasks.board();assertEquals(listOf("feed_horse","bedtime","crown"),board.map { it.id })
        s.tasks.record(Deed.FED,PlaceId.FARM,species=Species.HORSE);s.tasks.record(Deed.FED,PlaceId.FARM,species=Species.HORSE)
        assertEquals(1,w.stickers.size);s.tasks.swap(board[1]);assertEquals(1,w.taskProgress["feed_horse"])
        assertTrue("crown" in w.taskSet);assertFalse("bedtime" in w.taskSet)
    }
    @Test fun newWorldDoesNotEarnStickersFromPresetClothesOrBeds() {
        val w=WorldFactory.create();assertTrue(w.stickers.isEmpty())
        val s=Sim(w);assertTrue(s.tasks.board().all { !s.tasks.done(it) })
    }
    @Test fun brokenTrainKeepsRepairStateThroughStorageAndNeverPoisonsReusedSlots() {
        val s=Sim(World());s.toys.startTrain();val f=s.world.fixtures.values.single()
        assertTrue(s.toys.broken(f));s.designer.store(f.place,f)
        val bus=s.designer.add(f.place,FixtureType.PLAY_BUS,0,1.15f,0.9f)!!
        assertEquals(f.id,bus.id);assertFalse(s.toys.broken(bus))
        s.toys.startTrain();assertSame(bus,s.world.fixtures[bus.id]);assertNotEquals(bus.id,s.world.toyInputs["train:fixture"])
        val restored=s.designer.unstore(PlaceId.BEACH,0,2f,0.9f)!!
        assertTrue(s.toys.broken(restored));s.vehicles.drive(restored,1);assertFalse(restored.on)
    }
}
