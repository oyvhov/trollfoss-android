package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.data.WorldHistory
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ChristmasPlayTest {
    private val place=PlaceId.CHRISTMAS_WORKSHOP
    private fun sim()=Sim(World())
    private fun fixture(s:Sim,type:FixtureType)=s.designer.add(place,type,0,2f,place.floor)!!
    private fun thing(s:Sim,type:ThingType)=s.world.addThing(type,0,place,2f,0.5f)
    private fun reload(s:Sim)=Sim(WorldStore.decode(WorldStore.encode(s.world,Settings())).world)

    @Test fun all180DesignChoicesAreFreeAndUsable() {
        val w=World()
        assertEquals(36,Christmas.types.size)
        assertEquals(108,Christmas.catalogue.size)
        assertEquals(36,Christmas.things.size)
        assertEquals(16,Christmas.clothes.size)
        assertEquals(180,Christmas.catalogue.size+Christmas.things.size+Christmas.clothes.size+12+8)
        Christmas.catalogue.forEach { assertTrue(Decor.available(w,it));assertTrue(it.type.spec.w>0f) }
        assertEquals(12,Decor.WALLS-12);assertEquals(8,Decor.FLOORS-9)
    }

    @Test fun calendarBoundariesAndSummerChristmasResolveSeparatelyFromSeason() {
        fun day(m:Int,d:Int)=LocalDate.of(2026,m,d).toEpochDay()
        assertEquals(Festival.NONE,Seasons.festival(day(11,19)))
        assertEquals(Festival.CHRISTMAS,Seasons.festival(day(11,20)))
        assertEquals(Festival.CHRISTMAS,Seasons.festival(day(1,6)))
        assertEquals(Festival.NONE,Seasons.festival(day(1,7)))
        assertEquals(Festival.CHRISTMAS,Seasons.resolveFestival(FestivalChoice.CHRISTMAS,day(7,1)))
        assertEquals(Season.SUMMER,Seasons.resolve(SeasonChoice.AUTO,day(7,1)))
    }

    @Test fun oldFestivalSwitchMigratesWithoutChangingRoomStyles() {
        val json=WorldStore.encode(WorldFactory.create(),Settings(festive=false))
        json.getJSONObject("settings").remove("festival")
        val saved=WorldStore.decode(json)
        assertEquals(FestivalChoice.NONE,saved.settings.festival)
        assertEquals(FestivalChoice.CHRISTMAS,WorldStore.decode(WorldStore.encode(saved.world,Settings(festival=FestivalChoice.CHRISTMAS))).settings.festival)
    }

    @Test fun allNissegrendaDoorsHaveValidDestinationsAndWaysBack() {
        val places=PlaceId.entries.filter {it.christmas}
        assertEquals(4,places.size);assertEquals(listOf(PlaceId.CHRISTMAS_SQUARE),places.filter {it.onMap})
        places.forEach { p ->
            assertEquals(p,Places.spec(p).id)
            House.floor(p)!!.passages.forEach { door ->
                assertNotNull(House.arrival(door.to,door.arrive))
                assertTrue(House.floor(door.to)!!.passages.any {it.to==p})
            }
        }
        val ids=PlaceId.entries.flatMap { p->(0 until if(p.big) 300 else 100).map {p.idBase+it} }
        assertEquals(ids.size,ids.distinct().size)
    }

    @Test fun oldWorldGetsNewPlacesOnceAndKeepsItsPeople() {
        val w=WorldFactory.create();val ids=w.bodies.values.filterNot {it.place?.christmas==true}.map {it.id}.toSet()
        val json=WorldStore.encode(w,Settings())
        json.put("places",JSONArray(PlaceId.entries.filterNot {it.christmas}.map {it.name}))
        // Model an actual pre-Christmas save, where the new residents did not exist yet.
        val bodies=json.getJSONArray("bodies")
        json.put("bodies",JSONArray((0 until bodies.length()).map {bodies.getJSONObject(it)}.filterNot {it.optString("place").startsWith("CHRISTMAS_")}))
        val loaded=WorldStore.decode(json).world
        assertEquals(1,loaded.people().count {it.name=="Julenissen"})
        val again=WorldStore.decode(WorldStore.encode(loaded,Settings())).world
        assertEquals(loaded.bodies.keys,again.bodies.keys)
        assertTrue(again.bodies.keys.containsAll(ids))
    }

    @Test fun giftReturnsTheOriginalObjectAcrossBagAndRestart() {
        val s=sim();val bench=fixture(s,FixtureType.XM_WRAP);val toy=thing(s,ThingType.XM_ROBOT)
        toy.used=0
        assertTrue(s.christmas.drop(bench,toy))
        val parcel=s.world.bodies.values.filterIsInstance<Thing>().single {it.type==ThingType.XM_PARCEL}
        parcel.mode=Mode.BAG;parcel.place=null
        val next=reload(s);val back=next.world.bodies[parcel.id] as Thing
        assertEquals(Mode.INSIDE,next.world.bodies[toy.id]!!.mode)
        back.mode=Mode.FREE;back.place=PlaceId.HOME;back.x=1f
        assertTrue(next.christmas.use(back))
        assertNull(next.world.bodies[parcel.id]);assertEquals(ThingType.XM_ROBOT,(next.world.bodies[toy.id] as Thing).type)
        assertEquals(PlaceId.HOME,next.world.bodies[toy.id]!!.place)
        assertEquals(Mode.FREE,next.world.bodies[toy.id]!!.mode)
    }

    @Test fun packagesCannotContainPackagesOrStockings() {
        val s=sim();val b=fixture(s,FixtureType.XM_WRAP)
        assertFalse(s.christmas.drop(b,thing(s,ThingType.XM_PARCEL)))
        assertFalse(s.christmas.drop(b,thing(s,ThingType.XM_STOCKING)))
        assertTrue(s.world.christmas.parcels.isEmpty())
    }

    @Test fun deletingAnUnopenedPackageRecoversItsOriginal() {
        val s=sim();val b=fixture(s,FixtureType.XM_WRAP);val original=thing(s,ThingType.XM_TRAIN)
        assertTrue(s.christmas.drop(b,original))
        val id=s.world.christmas.parcels.keys.single()
        s.world.remove(s.world.bodies[id]!!)
        assertEquals(Mode.FREE,original.mode);assertTrue(original.id in s.world.bodies)
    }

    @Test fun decoratedTreeKeepsIdentitySlotsAndLightsAcrossStorageTrashAndRestart() {
        val s=sim();val tree=fixture(s,FixtureType.XM_TREE)
        val ornaments=listOf(thing(s,ThingType.XM_BAUBLE),thing(s,ThingType.XM_STAR))
        ornaments.forEach {assertTrue(s.christmas.drop(tree,it))}
        tree.mode=2;tree.on=true
        assertTrue(s.designer.store(place,tree))
        val decoy=fixture(s,FixtureType.XM_SMALL_TREE)
        assertTrue(s.world.inMachine(decoy).isEmpty())
        assertTrue(s.designer.discard(0))
        val next=reload(s)
        ornaments.forEach {assertEquals(Mode.INSIDE,next.world.bodies[it.id]!!.mode);assertNull(next.world.bodies[it.id]!!.place)}
        assertTrue(next.designer.restoreDiscarded(0))
        val f=next.designer.unstore(PlaceId.HOME,0,1f,PlaceId.HOME.floor)!!
        assertTrue(f.on);assertEquals(2,f.mode)
        assertEquals(ornaments.map {it.id}.toSet(),next.world.inMachine(f).map {it.id}.toSet())
        assertEquals(listOf(0,11),next.world.inMachine(f).map {it.slot})
    }

    @Test fun treeCapacityRejectsThirteenthWithoutLosingIt() {
        val s=sim();val tree=fixture(s,FixtureType.XM_TREE)
        repeat(12) {assertTrue(s.christmas.drop(tree,thing(s,ThingType.XM_BAUBLE)))}
        val extra=thing(s,ThingType.XM_STAR)
        assertFalse(s.christmas.drop(tree,extra));assertEquals(Mode.FREE,extra.mode)
    }

    @Test fun treeDecorationCanBeTakenOffAgain() {
        val s=sim();val tree=fixture(s,FixtureType.XM_TREE);val star=thing(s,ThingType.XM_STAR)
        assertTrue(s.christmas.drop(tree,star));val point=Christmas.ornamentPoint(star.slot)
        s.christmas.tap(tree,point.first,point.second)
        assertEquals(Mode.FREE,star.mode)
    }

    @Test fun calendarHas24FreeDoorsAndNeverDuplicatesAnOpenedReward() {
        val s=sim()
        for(i in 23 downTo 0) assertTrue(s.christmas.openDoor(i,place,1f))
        val next=reload(s)
        val count=next.world.bodies.size
        repeat(24) {assertFalse(next.christmas.openDoor(it,place,1f))}
        assertEquals(count,next.world.bodies.size)
        assertEquals(24,next.world.christmas.opened.size)
    }

    @Test fun bakingRequiresRollingAndCuttingAndSurvivesRestartInOven() {
        val s=sim();val b=fixture(s,FixtureType.XM_BAKE);val dough=thing(s,ThingType.XM_DOUGH)
        val pin=thing(s,ThingType.XM_ROLLING_PIN);val cutter=thing(s,ThingType.XM_CUTTER)
        assertFalse(s.christmas.drop(b,cutter));assertTrue(s.christmas.drop(b,dough))
        assertTrue(s.christmas.drop(b,pin));assertTrue(s.christmas.drop(b,cutter))
        b.mode=3;s.christmas.tap(b,0f,0f)
        val next=reload(s);repeat(150){next.step(place,0.02f)}
        assertNull(next.world.bodies[dough.id]);assertNotNull(next.world.bodies[pin.id]);assertNotNull(next.world.bodies[cutter.id])
        assertEquals(1,next.world.bodies.values.filterIsInstance<Thing>().count {it.type==ThingType.XM_LUSSEKATT})
    }

    @Test fun gingerbreadHouseNeedsThreePiecesAndIcing() {
        val s=sim();val f=fixture(s,FixtureType.XM_HOUSE_BENCH);val icing=thing(s,ThingType.XM_ICING)
        assertFalse(s.christmas.drop(f,icing))
        repeat(3){assertTrue(s.christmas.drop(f,thing(s,ThingType.XM_GINGER_HEART)))}
        assertTrue(s.christmas.drop(f,icing));assertTrue(s.world.inMachine(f).isEmpty())
        assertTrue(icing.id in s.world.bodies);assertTrue(s.world.bodies.values.any {it is Thing && it.type==ThingType.XM_GINGER_HOUSE})
    }

    @Test fun filledStockingKeepsItsContentAcrossRestart() {
        val s=sim();val sock=thing(s,ThingType.XM_STOCKING);val bear=thing(s,ThingType.XM_BEAR)
        assertTrue(s.christmas.fillStocking(sock,bear));val next=reload(s)
        assertTrue(next.christmas.use(next.world.bodies[sock.id] as Thing));assertEquals(Mode.FREE,next.world.bodies[bear.id]!!.mode)
    }

    @Test fun sleighCarriesExactlyItsSeatedRidersAndActualGift() {
        val s=sim();val f=fixture(s,FixtureType.XM_SLEIGH);val wrap=fixture(s,FixtureType.XM_WRAP)
        val original=thing(s,ThingType.XM_BEAR);s.christmas.drop(wrap,original)
        val parcel=s.world.bodies[s.world.christmas.parcels.keys.single()] as Thing
        assertTrue(s.christmas.drop(f,parcel))
        val riders=List(2){s.world.addPerson(Species.FOLK,Look(),1f,place,2f,place.floor)}
        riders.forEachIndexed {i,p->assertTrue(s.seat(p,f,i))}
        assertTrue(s.christmas.fly(f,PlaceId.FARM))
        riders.forEach {assertEquals(PlaceId.FARM,it.place);assertEquals(Mode.FREE,it.mode)}
        assertEquals(PlaceId.FARM,parcel.place);assertTrue(s.christmas.use(parcel));assertEquals(PlaceId.FARM,original.place)
    }

    @Test fun fullSceneRefusesRecipeWithoutConsumingMaterial() {
        val s=sim();val f=fixture(s,FixtureType.XM_CRAFT);val paper=thing(s,ThingType.XM_PAPER)
        repeat(100){thing(s,ThingType.COIN)}
        assertFalse(s.christmas.drop(f,paper));assertTrue(paper.id in s.world.bodies);assertEquals(Mode.FREE,paper.mode)
    }

    @Test fun toyWorkshopNeedsTwoPartsAndKeepsTheHammerAfterAssembly() {
        val s=sim();val f=fixture(s,FixtureType.XM_TOY_BENCH)
        val hammer=thing(s,ThingType.HAMMER)
        assertFalse(s.christmas.drop(f,hammer))
        val a=thing(s,ThingType.PLANK);val b=thing(s,ThingType.STICK)
        assertTrue(s.christmas.drop(f,a));assertTrue(s.christmas.drop(f,b))
        val next=reload(s);val bench=next.world.fixtures[f.id]!!
        assertTrue(next.christmas.drop(bench,next.world.bodies[hammer.id] as Thing))
        assertNull(next.world.bodies[a.id]);assertNull(next.world.bodies[b.id])
        assertTrue(hammer.id in next.world.bodies)
        assertTrue(next.world.bodies.values.any {it is Thing && it.type==ThingType.XM_TRAIN})
    }

    @Test fun parcelWithSameNumberAsAFixtureCannotBecomeItsMachineInput() {
        val s=sim();val f=fixture(s,FixtureType.XM_WRAP)
        s.world.nextId=f.id-1
        val toy=thing(s,ThingType.XM_BEAR)
        assertTrue(s.christmas.drop(f,toy))
        assertEquals(f.id,s.world.christmas.parcels.keys.single())
        assertTrue(s.world.inMachine(f).isEmpty())
        val next=reload(s)
        assertTrue(next.world.inMachine(next.world.fixtures[f.id]!!).isEmpty())
        assertTrue(next.christmas.use(next.world.bodies[f.id] as Thing))
        assertEquals(Mode.FREE,next.world.bodies[toy.id]!!.mode)
    }

    @Test fun blueprintLightsStayOffAndChosenWrappingColourSurvivesRestart() {
        val s=Sim(WorldFactory.create())
        val tree=s.world.fixturesIn(PlaceId.CHRISTMAS_HOME).first {it.type==FixtureType.XM_TREE}
        tree.mode=0;tree.on=false
        val wrap=s.world.fixturesIn(place).first {it.type==FixtureType.XM_WRAP}
        wrap.variant=2
        val next=reload(s)
        assertFalse(next.world.fixtures[tree.id]!!.on)
        assertEquals(0,next.world.fixtures[tree.id]!!.mode)
        assertEquals(2,next.world.fixtures[wrap.id]!!.variant)
    }

    @Test fun elfHuntKeepsOneIdentityThroughAllThreeHidingPlaces() {
        val s=Sim(WorldFactory.create());val elf=s.world.people().single {it.name=="Rampenissen"}
        assertTrue(s.christmas.found(elf));val next=reload(s)
        assertTrue(next.christmas.found(next.world.bodies[elf.id] as Person))
        assertTrue(next.christmas.found(next.world.bodies[elf.id] as Person))
        assertFalse(next.christmas.found(next.world.bodies[elf.id] as Person))
        assertEquals(3,next.world.christmas.rampStage)
        assertEquals(1,next.world.people().count {it.name=="Rampenissen"})
    }

    @Test fun endingChristmasPartyReturnsInvitedResidentsHome() {
        val s=Sim(WorldFactory.create())
        val people=s.world.people().filter {it.species==Species.FOLK && it.mode==Mode.FREE}.take(4)
        val homes=people.associate {it.id to it.place}
        s.christmas.party(PlaceId.CHRISTMAS_HOME,4f)
        assertEquals(PlaceId.CHRISTMAS_HOME,s.world.community.partyPlace)
        val next=reload(s)
        next.christmas.party(PlaceId.CHRISTMAS_HOME,4f)
        assertNull(next.world.community.partyPlace)
        homes.forEach {(id,place)->assertEquals(place,next.world.bodies[id]!!.place)}
    }

    @Test fun bellStoryRequiresAnActualHandoverToSanta() {
        val s=Sim(WorldFactory.create());val santa=s.world.people().single {it.name=="Julenissen"}
        val bell=s.world.addThing(ThingType.XM_BELL,0,santa.place,santa.x,santa.y)
        assertEquals(Give.NONE,s.give(santa,bell,Part.HAT))
        assertFalse("bell:returned" in s.world.christmas.deeds)
        assertEquals(Give.HELD,s.give(santa,bell,Part.HAND))
        assertTrue("bell:returned" in s.world.christmas.deeds)
    }

    @Test fun rolfServesTheActualCupAndLeavesOccupiedHandsAlone() {
        val s=sim();val rolf=s.world.addPerson(Species.ROBOT,Look(),1f,place,1f,place.floor,"Rolf")
        val friend=s.world.addPerson(Species.FOLK,Look(),1f,place,1.4f,place.floor)
        val cup=thing(s,ThingType.XM_COCOA);s.give(rolf,cup,Part.HAND)
        val bear=thing(s,ThingType.XM_BEAR);s.give(friend,bear,Part.HAND)
        repeat(130){s.christmas.tick(place,0.02f)}
        assertEquals(rolf.id,cup.holder)
        bear.mode=Mode.FREE;bear.holder=-1
        repeat(130){s.christmas.tick(place,0.02f)}
        assertEquals(friend.id,cup.holder);assertSame(cup,s.world.worn(friend,Slot.HAND))
    }

    @Test fun undoWrappingAndFillingStockingsRestoresTheSameToyWithoutErasingDiscovery() {
        for(wrapping in listOf(true,false)) {
            val s=sim();val toy=thing(s,ThingType.XM_TRAIN)
            val bench=fixture(s,FixtureType.XM_WRAP)
            val stocking=thing(s,ThingType.XM_STOCKING)
            val history=WorldHistory({s.world})
            history.begin(toy.id)
            if(wrapping) assertTrue(s.christmas.drop(bench,toy))
            else assertTrue(s.christmas.fillStocking(stocking,toy))
            history.end()
            val restored=history.undo()!!
            assertEquals(Mode.FREE,restored.bodies[toy.id]!!.mode)
            assertEquals(-1,restored.bodies[toy.id]!!.holder)
            assertTrue(restored.christmas.parcels.isEmpty());assertTrue(restored.christmas.stocking.isEmpty())
            assertTrue((if(wrapping) "wrap" else "stocking") in restored.christmas.deeds)
        }
    }

    @Test fun brushingTheReindeerKeepsTheBrushAndItsOccupiedHand() {
        val s=sim();val deer=s.world.addPerson(Species.REINDEER,Look(),1f,place,1f,place.floor)
        val carrot=thing(s,ThingType.CARROT);s.give(deer,carrot,Part.HAND)
        val brush=thing(s,ThingType.XM_BRUSH)
        assertEquals(Give.PLAY,s.give(deer,brush,Part.BODY))
        assertSame(brush,s.world.bodies[brush.id]);assertEquals(Mode.FREE,brush.mode)
        assertSame(carrot,s.world.worn(deer,Slot.HAND))
        assertTrue(deer.anim.faceTime>0f);assertTrue("reindeer" in s.world.christmas.deeds)
    }

    @Test fun wishPicturesCanBeChosenBeforeMailingAndSurviveRestart() {
        val s=sim();val box=fixture(s,FixtureType.XM_POST);val letter=thing(s,ThingType.XM_WISH)
        repeat(3){assertTrue(s.christmas.use(letter))}
        assertEquals(3,letter.variant);assertFalse("wish" in s.world.christmas.deeds)
        assertTrue(s.christmas.drop(box,letter))
        val next=reload(s);val saved=next.world.bodies[letter.id] as Thing
        assertEquals(3,saved.variant);assertEquals(1,saved.used)
        assertTrue("wish" in next.world.christmas.deeds)
    }
}
