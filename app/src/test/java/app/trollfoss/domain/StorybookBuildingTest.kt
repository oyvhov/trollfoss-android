package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.ui.play.RenderQuality
import org.junit.Assert.*
import org.junit.Test

class StorybookBuildingTest {
    private fun started(): Pair<World, Sim> {
        val w = WorldFactory.create(); val s = Sim(w)
        assertTrue(s.mine.layFoundation(1)); s.mine.finishJob()
        s.mine.select(PlaceId.MINE_GROUND,1)
        return w to s
    }

    @Test fun previewNeverWritesRoomsFixturesOrSaveAndStopsWhenBuildingStarts() {
        val (w,s) = started()
        val before = WorldStore.encode(w,Settings()).toString()
        for (kind in RoomKind.entries) {
            val preview = RoomSuggestion(PlaceId.MINE_GROUND,1,kind)
            assertTrue(preview.visible(w.mine))
            assertTrue(preview.pieces.isNotEmpty())
            assertTrue(preview.pieces.none { it.id in w.fixtures })
        }
        assertEquals(before,WorldStore.encode(w,Settings()).toString())
        val preview = RoomSuggestion(PlaceId.MINE_GROUND,1,RoomKind.LIVING)
        s.mine.select(PlaceId.MINE_GROUND,2); assertFalse(preview.visible(w.mine))
        s.mine.select(PlaceId.MINE_GROUND,1)
        assertTrue(s.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.LIVING))
        assertFalse(preview.visible(w.mine))
        s.mine.finishJob(); assertFalse(preview.visible(w.mine))
    }

    @Test fun exteriorSetsSurviveSaveAndLeaveRoomAndPeopleAlone() {
        val (w,s) = started()
        s.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.LIVING); s.mine.finishJob()
        val furniture = w.fixtures.keys.toSet(); val people = w.bodies.keys.toSet()
        for (palette in HousePalette.entries) {
            s.mine.applyPalette(palette)
            val back = WorldStore.decode(WorldStore.encode(w,Settings())).world
            assertTrue(palette.matches(back.mine))
            assertEquals(RoomKind.LIVING,back.mine.kind(PlaceId.MINE_GROUND,1))
            assertEquals(furniture,w.fixtures.keys); assertEquals(people,w.bodies.keys)
        }
        s.mine.restyle(door=0)
        assertEquals(HousePalette.SUN.wall,w.mine.wall)
        assertEquals(0,w.mine.door)
    }

    @Test fun completedRoomGetsAWaveWithoutMovingPlayersOrWakingSleepers() {
        val (w,s) = started()
        val people = w.people().filter { it.species == Species.FOLK }.take(4)
        people.forEach { it.place=PlaceId.MINE_GROUND; it.x=2.6f; it.y=0.95f; it.mode=Mode.FREE; it.anim.pose=Pose.STAND }
        w.playerIds.add(people[0].id)
        people[1].held=true; people[2].anim.face=Face.SLEEP; people[3].x=8f
        s.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.LIVING); s.mine.finishJob()
        assertTrue(people[0].anim.wave>0f)
        assertEquals(2.6f,people[0].x,0f)
        assertEquals(0f,people[1].anim.wave,0f)
        assertEquals(Face.SLEEP,people[2].anim.face)
        assertEquals(0f,people[3].anim.wave,0f)
        assertTrue(people[0].id in w.playerIds)
    }

    @Test fun onlyTheNearestGuestGetsSawdustUpTheNoseAndNothingIsLost() {
        val w = WorldFactory.create()
        val people = w.people().filter { it.species == Species.FOLK }.take(3)
        people.forEachIndexed { i,p -> p.place=PlaceId.MINE_GROUND; p.x=3f+i*0.5f; p.y=0.95f; p.mode=Mode.FREE; p.anim.pose=Pose.STAND }
        val ids = people.map { it.id }
        fun worn() = w.bodies.values.filterIsInstance<Thing>().filter { it.holder in ids }.map { it.id to it.holder }
        val before = worn()
        assertSame(people[0], RoomWelcome.greet(w,PlaceId.MINE_GROUND,2.8f))
        assertEquals(Face.OOH,people[0].anim.face)
        assertEquals(Face.GRIN,people[1].anim.face)
        assertEquals(0f,people[0].anim.sneeze,0f)
        assertEquals(before,worn())
        assertNull(RoomWelcome.greet(w,PlaceId.MINE_UPPER,2.8f))
    }

    @Test fun pictureTurnSurvivesStorageSaveAndDemolition() {
        val (w,s)=started()
        s.mine.buildRoom(PlaceId.MINE_GROUND,1,RoomKind.LIVING);s.mine.finishJob()
        val picture=w.fixturesIn(PlaceId.MINE_GROUND).first { it.type==FixtureType.PICTURE && it.x in Mine.slotRange(1) }
        assertTrue(s.designer.turnPicture(picture));assertEquals(1,picture.mode)
        val saved=WorldStore.decode(WorldStore.encode(w,Settings())).world
        assertEquals(1,saved.fixtures[picture.id]!!.mode)
        assertTrue(s.designer.store(picture.place,picture))
        val index=w.storage.lastIndex
        val restored=s.designer.unstore(PlaceId.MINE_GROUND,index,picture.x,picture.y)!!
        assertEquals(1,restored.mode)
        s.mine.demolish(PlaceId.MINE_GROUND,1)
        assertTrue(w.storage.any { it.type==FixtureType.PICTURE && it.mode==1 })
        val loaded=WorldStore.decode(WorldStore.encode(w,Settings())).world
        assertTrue(loaded.storage.any { it.type==FixtureType.PICTURE && it.mode==1 })
    }

    @Test fun decorationPolicyIgnoresWarmupAndSpikesButRespondsToSustainedSlowDrawing() {
        val q=RenderQuality()
        repeat(120) { q.recordDraw(50_000_000) }
        assertFalse(q.reduced)
        repeat(120) { q.recordDraw(if(it==0) 90_000_000 else 10_000_000) }
        assertFalse(q.reduced)
        repeat(120) { q.recordDraw(30_000_000) }
        assertTrue(q.reduced); assertEquals(120,q.particleLimit)
        repeat(240) { q.recordDraw(1_000_000) }; assertTrue(q.reduced)
        assertTrue(RenderQuality(true,64).cacheBytes<=12*1024*1024)
        assertTrue(RenderQuality(true,64).reduced)
        assertEquals(40*1024*1024L,RenderQuality(false,4096).cacheBytes)
    }
}
