package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.data.WorldHistory
import org.junit.Assert.*
import org.junit.Test

class CreativePlayTest {
    @Test fun petGoesHomeAfterTravelAndReloadWithoutLosingItsIdentity() {
        val s=sim();val owner=person(s)
        val pet=s.world.addPerson(Species.CAT,Look(),1f,PlaceId.FARM,2.1f,0.94f)
        Players.toggle(s.world,owner)
        assertTrue(s.community.choosePet(owner,pet))
        Players.arrive(s.world,PlaceId.BEACH,1f);s.community.follow(PlaceId.BEACH)
        val saved=reload(s.world);val again=Sim(saved)
        val samePet=saved.bodies[pet.id] as Person
        samePet.held=true;assertFalse(again.community.choosePet(saved.bodies[owner.id] as Person,null))
        samePet.held=false;assertTrue(again.community.choosePet(saved.bodies[owner.id] as Person,null))
        assertEquals(PlaceId.FARM,samePet.place);assertEquals(2.1f,samePet.x,0.001f)
        assertTrue(saved.community.pets.isEmpty());assertTrue(saved.community.petHomes.isEmpty())
    }
    @Test fun waterRunsAcrossThreeChannelsAndStopsAtAGap() {
        val s=sim()
        fun add(type:FixtureType,x:Float)=Fixture(s.world.nextId++,PlaceId.HOME,type,x,0.94f).also { s.world.fixtures[it.id]=it }
        val channels=(0..2).map { add(FixtureType.PLAY_CHANNEL,0.5f+it*0.49f) }
        val wheel=add(FixtureType.PLAY_WATER_WHEEL,1.94f)
        channels.first().count=8;channels.first().on=true
        repeat(250) { channels.forEach { s.creative.step(it,0.02f) };s.creative.step(wheel,0.02f) }
        assertTrue(wheel.angle>0f)
        wheel.angle=0f;wheel.count=0;wheel.on=false
        channels.forEach { it.count=0;it.on=false }
        channels[1].x=3f;channels.first().count=8;channels.first().on=true
        repeat(250) { channels.forEach { s.creative.step(it,0.02f) };s.creative.step(wheel,0.02f) }
        assertEquals(0f,wheel.angle,0.001f)
        val empty=s.world.addThing(ThingType.BUCKET,0,PlaceId.HOME,1f,0.9f)
        assertFalse(ToyPlay.accepts(channels.first(),empty))
        empty.used=1;assertTrue(ToyPlay.accepts(channels.first(),empty))
    }
    private fun sim()=Sim(World().apply { stickers.addAll(0..20) })
    private fun toy(s:Sim,r:ToyReward,x:Float=1f)=requireNotNull(s.toys.claim(r,PlaceId.HOME,x))
    private fun person(s:Sim,x:Float=1f)=s.world.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,x,0.95f)
    private fun reload(w:World)=WorldStore.decode(WorldStore.encode(w,Settings())).world

    @Test fun grownTreeSurvivesStorageRecyclingRestartAndReturn() {
        val s=sim();val tree=toy(s,ToyReward.TREE)
        repeat(3) { s.toys.supply(tree);val bucket=s.world.bodies[s.world.toyInputs["${tree.id}:BUCKET"]] as Thing;assertTrue(s.creative.drop(tree,bucket)) }
        assertEquals(3,tree.mode)
        assertTrue(s.designer.store(tree.place,tree));assertTrue(s.designer.discard(0))
        val saved=reload(s.world);val again=Sim(saved)
        assertTrue(again.designer.undoDiscard())
        val returned=again.designer.unstore(PlaceId.BEACH,0,1f,0.94f)!!
        assertEquals(3,returned.mode)
        again.creative.tap(returned)
        assertTrue(saved.bodiesIn(PlaceId.BEACH).any { it is Thing && it.type==ThingType.APPLE })
    }
    @Test fun pairedPortalsCarryTheActualPersonAndHatAndRespectHeldFigures() {
        val s=sim();val first=toy(s,ToyReward.PORTAL,0.8f);val second=toy(s,ToyReward.PORTAL,2.4f)
        val p=person(s,first.x);val hat=s.world.addThing(ThingType.CAP,2,p.place,p.x,p.y);s.give(p,hat,Part.HAT)
        p.held=true;assertFalse(s.creative.transport(first,p));p.held=false
        assertTrue(s.creative.transport(first,p));assertEquals(second.x+0.24f,p.x,0.001f)
        assertSame(hat,s.world.worn(p,Slot.HEAD));assertSame(p,s.world.bodies[p.id])
    }
    @Test fun buildBenchMakesARealCartAndReturnsAllOriginalBlocks() {
        val s=sim();val bench=toy(s,ToyReward.BUILD);s.toys.supply(bench)
        val parts=s.world.bodies.values.filterIsInstance<Thing>().toList()
        assertEquals(3,parts.size);parts.forEach { assertTrue(s.creative.drop(bench,it)) }
        s.creative.tap(bench)
        val cart=s.world.fixtures.values.single { it.type==FixtureType.PLAY_CART }
        assertEquals(parts.map { it.id }.toSet(),s.world.playAssemblies[cart.id]!!.parts.toSet())
        assertTrue(s.magic.unmake(cart))
        parts.forEach { assertSame(it,s.world.bodies[it.id]);assertNotEquals(Mode.INSIDE,it.mode) }
    }
    @Test fun craneBeltAndHoverKeepCargoRecoverableAfterReload() {
        for(reward in listOf(ToyReward.CRANE,ToyReward.CONVEYOR,ToyReward.HOVER)) {
            val s=sim();val f=toy(s,reward);val cargo=s.world.addThing(ThingType.TEDDY,0,f.place,f.x,f.y)
            assertTrue(s.creative.drop(f,cargo));s.creative.tap(f)
            repeat(40) { s.creative.step(f,0.02f) }
            val saved=reload(s.world);val restored=saved.bodies[cargo.id] as Thing
            Sim(saved).toys.releaseAll(saved.fixtures[f.id]!!)
            assertEquals(Mode.FREE,restored.mode);assertEquals(f.place,restored.place)
        }
    }
    @Test fun seesawRespondsToTwoRidersAndTandemCarriesBoth() {
        val s=sim();val seesaw=toy(s,ToyReward.SEESAW)
        val a=person(s);val b=person(s,1.2f)
        assertTrue(s.seat(a,seesaw,0));s.creative.step(seesaw,0.5f);assertTrue(seesaw.angle<0)
        assertTrue(s.seat(b,seesaw,1));s.creative.tap(seesaw)
        val bike=toy(s,ToyReward.TANDEM,2.4f);s.seat(a,bike,0);s.seat(b,bike,1)
        val start=bike.x;s.vehicles.drive(bike,1);repeat(40) { s.step(bike.place,0.02f) }
        assertTrue(bike.x>start);assertEquals(bike.id,a.holder);assertEquals(bike.id,b.holder)
    }
    @Test fun companionArtTraitsAndDoorSurviveRestart() {
        val s=sim();s.world.mine.started=true
        val p=person(s);val pet=s.world.addPerson(Species.CAT,Look(),1f,PlaceId.HOME,1.2f,0.95f)
        s.world.playerIds+=p.id;s.community.setTrait(p,Temperament.CURIOUS);assertTrue(s.community.choosePet(p,pet))
        val teddy=s.world.addThing(ThingType.TEDDY,0,p.place,p.x,p.y);assertTrue(s.community.awaken(teddy,p))
        val art=s.community.newArt();assertTrue(s.community.addMark(art,2,4,0.5f,0.5f));val frame=s.community.hangArt(art,p.place!!,2f)!!
        val door=toy(s,ToyReward.DOOR,3f);assertTrue(s.community.linkDoor(door,PlaceId.MINE_GROUND,0))
        assertTrue(s.designer.store(door.place,door))
        val saved=reload(s.world);val again=Sim(saved)
        assertEquals(Temperament.CURIOUS,saved.community.traits[p.id]);assertEquals(pet.id,saved.community.pets[p.id])
        assertEquals(p.id,saved.community.teddies[teddy.id]);assertEquals(1,saved.community.art[art]!!.size)
        assertEquals(Community.ART_BASE+art,saved.fixtures[frame.id]!!.variant)
        val returned=again.designer.unstore(PlaceId.BEACH,0,1f,0.94f)!!
        assertEquals(RoomLink(PlaceId.MINE_GROUND,0),again.community.door(returned))
    }
    @Test fun partyReturnsVisitorsButKeepsTheChosenPlayerAndTheirBelongings() {
        val s=sim();val p=person(s);val guest=person(s,2f);s.world.playerIds+=p.id
        assertTrue(s.community.startParty(PlaceId.BEACH,1f,listOf(p.id,guest.id),2,1))
        val saved=reload(s.world);assertTrue(Sim(saved).community.endParty())
        assertEquals(PlaceId.BEACH,saved.bodies[p.id]!!.place);assertEquals(PlaceId.HOME,saved.bodies[guest.id]!!.place)
        assertTrue(saved.community.guests.isEmpty())
    }
    @Test fun undoPackingResumesTheSamePlayer() {
        var w=World();val s=Sim(w);val p=person(s);w.playerIds+=p.id
        val history=WorldHistory({ w });s.journal=history
        s.edit { Players.pack(w,p) };assertTrue(Players.paused(w,p))
        w=history.undo()!!;val restored=w.bodies[p.id] as Person
        assertFalse(Players.paused(w,restored));assertEquals(Mode.FREE,restored.mode)
    }
    @Test fun fullBackWallStillLeavesTheForegroundAvailableForGift() {
        val s=Sim(World())
        for(i in 0..20) s.world.fixtures[i]=Fixture(i,PlaceId.HOME,FixtureType.SOFA,i*0.2f,PlaceId.HOME.floor)
        val gift=s.toys.claim(ToyReward.CAMERA,PlaceId.HOME,1f)
        assertNotNull(gift);assertTrue(gift!!.y>PlaceId.HOME.floor)
    }
}
