package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class ValleyRiverLifeTest {
    private val place = PlaceId.VAGSTADDALEN
    private fun advance(sim: Sim, seconds: Float = 3f) = repeat((seconds * 60).toInt()) { sim.step(place, 1f / 60f) }
    private fun fixture(w: World, index: Int) = w.fixtures.getValue(WorldFactory.fixtureId(place, index))

    @Test fun bothSwimOutfitsCanBeWornSavedAndExchangedBackWithoutLosingAnyAppearance() {
        for (style in listOf(Garment.SWIM_SUIT, Garment.SWIM_SET)) {
            val w = WorldFactory.create(); val s = Sim(w)
            val p = w.people().first { it.name == "Hedda" }
            val original = p.look.copy(bottom = 2, shoes = 14, pattern = 10, accent = 6)
            p.look = original
            val item = w.addThing(ThingType.GARMENT, Garment.pack(style, 4), p.place, p.x, p.y)
            assertEquals(Give.DRESSED, s.give(p, item, Part.BODY))
            assertEquals(style, p.look.top)
            assertEquals(original.bottom, p.look.bottom)
            val saved = WorldStore.decode(WorldStore.encode(w, Settings())).world
            val same = saved.bodies.getValue(p.id) as Person
            assertEquals(p.look, same.look)
            assertEquals("Hedda", same.name)
            assertEquals(Give.DRESSED, Sim(saved).give(same, saved.bodies.getValue(item.id) as Thing, Part.BODY))
            assertEquals(original, same.look)
        }
    }

    @Test fun aChildFloatsAndSwimsInTheRiverThenStandsOnTheBeach() {
        val w = WorldFactory.create(); val s = Sim(w)
        val p = w.addPerson(Species.FOLK, Look(height = .78f, top = Garment.SWIM_SUIT), 1f, place, 3.05f, .85f)
        advance(s)
        assertEquals(Pose.SWIM, p.anim.pose)
        assertTrue(p.y in Vagstaddalen.WATERLINE..Vagstaddalen.BED)
        assertTrue("head stays above the water", p.y - p.h < Vagstaddalen.WATERLINE)
        p.x = 2.64f; p.y = .89f; p.ground = .94f; p.vx = 0f; p.vy = 0f; p.resting = false
        advance(s)
        assertEquals(Pose.STAND, p.anim.pose)
        assertTrue(p.resting)
    }

    @Test fun dinghyCarriesTwoChildrenStopsAtTheBanksAndKeepsItsVariantInStorage() {
        val w = WorldFactory.create(); val s = Sim(w); val boat = fixture(w, Vagstaddalen.DINGHY)
        val children = (0..1).map { i ->
            w.addPerson(Species.FOLK, Look(height=.78f, top=Garment.SWIM_SET), 1f, place, boat.x, boat.y)
                .also { assertTrue(s.seat(it, boat, i)) }
        }
        s.vehicles.drive(boat,-1); advance(s,5f)
        assertEquals(Vagstaddalen.RIVER_LEFT + boat.spec.w/2f,boat.x,.001f)
        assertFalse(boat.on)
        s.vehicles.drive(boat,1); advance(s,5f)
        assertEquals(Vagstaddalen.RIVER_RIGHT - boat.spec.w/2f,boat.x,.001f)
        assertFalse(boat.on)
        children.forEachIndexed { i,p -> assertEquals(s.seatPoint(boat,i)[0],p.x,.001f) }
        val loaded=WorldStore.decode(WorldStore.encode(w,Settings())).world
        assertEquals(1,fixture(loaded,Vagstaddalen.DINGHY).variant)
        children.forEach { assertEquals(boat.id,loaded.bodies.getValue(it.id).holder) }
        assertTrue(s.designer.store(place,boat))
        val again=s.designer.unstore(place,w.storage.indexOfFirst { it.type==FixtureType.BOAT },3.4f,.9f)!!
        assertEquals(1,again.variant)
        assertEquals(Vagstaddalen.WATERLINE+.02f,again.y,.001f)
    }

    @Test fun rightBankFishingAndFireProduceUsableFood() {
        val w=WorldFactory.create(); val s=Sim(w)
        val fishing=fixture(w,Vagstaddalen.RIVER_FISHING)
        s.tap(place,fishing,0f,-.04f); advance(s,1.6f)
        assertEquals(1,w.catches)
        assertFalse(fishing.on)
        val fire=fixture(w,Vagstaddalen.RIVER_FIRE)
        assertTrue(fire.on)
        val fish=w.addThing(ThingType.FISH,0,place,fire.x,fire.y-.15f)
        assertTrue(s.dropInto(place,fire,fish))
        advance(s,8f)
        assertEquals(ThingType.GRILLED_FISH,fish.type)
    }

    @Test fun oldSaveGetsTheAdditionsOnceAndKeepsTheMovedCabinOpenNookAndPackedTree() {
        val w=WorldFactory.create(); val s=Sim(w)
        val newIds=w.bodiesIn(place).filterIsInstance<Thing>().filter { t ->
            Vagstaddalen.riversideThings.any { it.type==t.type && it.variant==t.variant && kotlin.math.abs(t.homeDx-it.x)<.05f }
        }.map { it.id }
        newIds.forEach { w.bodies.remove(it) }
        w.flags.remove(Vagstaddalen.RIVER_LIFE)
        val cabin=fixture(w,0); s.moveFixture(place,cabin,1.10f,.87f)
        val nook=fixture(w,8); nook.open=true
        assertTrue(s.designer.store(place,fixture(w,7)))
        val json=WorldStore.encode(w,Settings())
        val encodedFixtures=json.getJSONArray("fixtures")
        for(i in encodedFixtures.length()-1 downTo 0) {
            if(encodedFixtures.getJSONObject(i).getInt("id") in 3909..3912) encodedFixtures.remove(i)
        }
        val before=w.bodies.mapValues { (_,b) -> Triple(b.place,b.x,b.y) }
        val loaded=WorldStore.decode(json).world
        assertEquals(FixtureType.SECRET_NOOK,fixture(loaded,8).type)
        assertTrue(fixture(loaded,8).open)
        assertEquals(1.10f,fixture(loaded,0).x,.001f)
        assertFalse(loaded.fixtures.containsKey(3907))
        assertTrue(loaded.storage.any { it.type==FixtureType.PINE_TREE })
        before.forEach { (id,position) ->
            val b=loaded.bodies.getValue(id)
            assertEquals(position,Triple(b.place,b.x,b.y))
        }
        assertTrue(Vagstaddalen.RIVER_LIFE in loaded.flags)
        assertEquals(before.size+Vagstaddalen.riversideThings.size,loaded.bodies.size)
        val outfit=loaded.bodiesIn(place).filterIsInstance<Thing>().first { it.type==ThingType.GARMENT }
        outfit.mode=Mode.BAG; outfit.place=null
        assertTrue(Sim(loaded).designer.store(place,fixture(loaded,Vagstaddalen.DINGHY)))
        val again=WorldStore.decode(WorldStore.encode(loaded,Settings())).world
        assertEquals(loaded.bodies.keys,again.bodies.keys)
        assertEquals(Mode.BAG,again.bodies.getValue(outfit.id).mode)
        assertFalse(again.fixtures.containsKey(3911))
        assertEquals(1,again.storage.count { it.type==FixtureType.BOAT && it.variant==1 })
    }
}
