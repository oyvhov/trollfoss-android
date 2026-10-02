package app.trollfoss.domain

import app.trollfoss.data.WorldHistory
import org.junit.Assert.*
import org.junit.Test

class WorldHistoryTest {
    @Test fun houseStyleUndoReturnsLaterGuestsToTheirOwnHome() {
        var w=World();val s=Sim(w);w.mine.started=true
        val p=w.addPerson(Species.FOLK,Look(),1f,PlaceId.MINE_GROUND,3f,0.9f)
        val h=WorldHistory({ w });s.journal=h;s.mine.restyle(wall=1)
        w.mine.guests+=Guest(p.id,PlaceId.HOME,1f)
        w=h.undo()!!;assertEquals(0,w.mine.wall);assertTrue(w.mine.guests.isEmpty())
        assertEquals(PlaceId.HOME,w.bodies[p.id]!!.place);assertEquals(1f,w.bodies[p.id]!!.x,0f)
    }
    @Test fun movementUndoRetainsEarnedProgressAndUnrelatedPhysics() {
        var w=World();val s=Sim(w);val h=WorldHistory({ w });s.journal=h
        val a=w.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,0.8f,0.9f)
        val b=w.addThing(ThingType.BALL,0,PlaceId.HOME,2f,0.9f)
        h.begin(a.id);a.x=1.5f;b.x=2.3f;w.stickers+=0;w.flags+="toy:BUS";w.taskProgress["bedtime"]=1;h.end()
        b.x=2.8f;w=h.undo()!!
        assertEquals(0.8f,w.bodies[a.id]!!.x,0f);assertEquals(2.8f,w.bodies[b.id]!!.x,0f)
        assertEquals(listOf(0),w.stickers);assertTrue("toy:BUS" in w.flags);assertEquals(1,w.taskProgress["bedtime"])
    }
    @Test fun nestedAssemblyUndoRestoresOriginalPartsAndKeepsBagReward() {
        var w=World();val s=Sim(w);val h=WorldHistory({ w });s.journal=h
        s.magic.kit(PlayRecipe.FORT,PlaceId.HOME,1.2f)
        val ids=w.playKits.getValue(PlayRecipe.FORT);ids.forEach { w.bodies[it]!!.x=1.2f }
        h.begin(ids.last());val f=s.magic.combine(w.bodies[ids.last()] as Thing)!!
        val reward=w.addThing(ThingType.GIFT,0,PlaceId.HOME,1f,0.9f);reward.mode=Mode.BAG;reward.place=null
        w.flags+="adventure:CAMP:done";h.end();w=h.undo()!!
        assertFalse(f.id in w.fixtures);assertTrue(ids.all { w.bodies[it]?.mode==Mode.FREE });assertTrue(reward.id in w.bodies)
        assertTrue("adventure:CAMP:done" in w.flags)
    }
    @Test fun packingUndoRestoresPassengersClothesAndTeamMembership() {
        var w=World();val s=Sim(w);val f=s.designer.add(PlaceId.HOME,FixtureType.PLAY_BUS,0,1.2f,0.9f)!!
        val p=w.addPerson(Species.FOLK,Look(),1f,f.place,f.x,f.y);val hat=w.addThing(ThingType.CAP,2,f.place,f.x,f.y)
        s.give(p,hat,Part.HAT);s.seat(p,f,0);w.playerIds+=p.id
        val h=WorldHistory({ w });s.journal=h;s.designer.store(f.place,f);w=h.undo()!!
        assertTrue(f.id in w.fixtures);assertTrue(w.storage.isEmpty());assertEquals(Mode.SEATED,w.bodies[p.id]?.mode)
        assertEquals(p.id,w.bodies[hat.id]?.holder);assertTrue(p.id in w.playerIds)
    }
    @Test fun foundationRoomAndDemolitionUndoRestoreTheirStructuralState() {
        var w=World();var s=Sim(w);val h=WorldHistory({ w });s.journal=h
        assertTrue(s.mine.layFoundation(0));assertFalse(h.available);s.mine.finishJob();assertTrue(h.available)
        w=h.undo()!!;assertFalse(w.mine.started);assertTrue(w.fixturesIn(PlaceId.MINE_GROUND).isEmpty())
        s=Sim(w);s.journal=h;s.mine.layFoundation(0);s.mine.finishJob();h.clear()
        assertTrue(s.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.BEDROOM));s.mine.finishJob()
        w=h.undo()!!;assertNull(w.mine.kind(PlaceId.MINE_GROUND,1))
        s=Sim(w);s.journal=h;s.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.BEDROOM);s.mine.finishJob();h.clear()
        val ids=w.fixturesIn(PlaceId.MINE_GROUND).filter { it.x in Mine.slotRange(1) }.map { it.id }.toSet()
        assertTrue(s.mine.demolish(PlaceId.MINE_GROUND,1));w=h.undo()!!
        assertEquals(RoomKind.BEDROOM,w.mine.kind(PlaceId.MINE_GROUND,1));assertTrue(w.fixtures.keys.containsAll(ids));assertTrue(w.storage.isEmpty())
    }
    @Test fun undoOwnPhotoRemovesItsFrameAndSnapshotTogether() {
        var w=World();val s=Sim(w);val f=s.toys.claim(ToyReward.CAMERA,PlaceId.HOME,1f)!!
        w.addPerson(Species.FOLK,Look(),1f,f.place,f.x,f.y)
        val h=WorldHistory({ w });s.journal=h;s.edit { s.tap(f.place,f,0f,0f) };assertEquals(1,w.toyPhotos.size)
        w=h.undo()!!;assertTrue(w.toyPhotos.isEmpty());assertTrue(w.fixtures.values.none { it.type==FixtureType.PICTURE })
    }
    @Test fun boundedHistoryAndNoOpGesturesArePredictable() {
        var w=World();val h=WorldHistory({ w });val t=w.addThing(ThingType.BALL,0,PlaceId.HOME,1f,0.9f)
        h.begin(t.id);h.end();assertFalse(h.available)
        repeat(10) { i -> h.begin(t.id);t.x=2f+i;h.end() }
        repeat(8) { w=h.undo()!! };assertFalse(h.available);assertEquals(3f,w.bodies[t.id]!!.x,0f)
    }
}
