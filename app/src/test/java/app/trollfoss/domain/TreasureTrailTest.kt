package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldHistory
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class TreasureTrailTest {
    private fun begin() = Sim(World()).also { it.magic.start(Adventure.RUMLE) }
    private fun reload(s: Sim) = Sim(WorldStore.decode(WorldStore.encode(s.world,Settings())).world)
    private fun finish(s: Sim) { for(i in 0..2) s.trail.find(s.trail.note(i)!!) }

    @Test fun optionalTrailStartsWithoutLevelsAndSuppliesExactlyThreeNotes() {
        val s=Sim(World())
        s.trail.prepare()
        assertTrue(s.world.bodies.isEmpty()); assertFalse(s.trail.started)
        s.magic.start(Adventure.RUMLE)
        val ids=s.world.rumleNotes.toList()
        repeat(5) { s.magic.start(Adventure.RUMLE) }
        assertEquals(ids,s.world.rumleNotes.toList())
        assertEquals(3,s.world.bodies.size)
        assertEquals(0,s.world.stickers.size)
        assertEquals(TreasureTrail.stops.map { it.place },(0..2).map { s.trail.note(it)!!.place })
    }

    @Test fun anyOrderEarnsOneLanternAndTheOriginalPapersRemain() {
        val s=begin(); val ids=s.world.rumleNotes.toList()
        assertTrue(s.trail.find(s.trail.note(2)!!)); assertEquals(0,s.trail.nextIndex)
        assertTrue(s.trail.find(s.trail.note(0)!!)); assertEquals(1,s.trail.nextIndex)
        assertNull(s.trail.lamp())
        assertTrue(s.trail.find(s.trail.note(1)!!))
        assertTrue(s.trail.complete); assertEquals(3,s.magic.stage(Adventure.RUMLE))
        val lamp=s.trail.lamp()!!
        assertEquals(Mode.BAG,lamp.mode)
        assertTrue(First.RUMLE_TREASURE.name in s.world.firsts)
        repeat(12) { i -> s.trail.find(s.trail.note(i%3)!!); s.magic.start(Adventure.RUMLE) }
        assertSame(lamp,s.trail.lamp()); assertEquals(4,s.world.bodies.size)
        assertEquals(ids,s.world.rumleNotes.toList()); assertEquals(1,s.world.stickers.size)
    }

    @Test fun pauseAndReloadKeepCluesProgressPlayersAndPackedNotes() {
        val s=begin(); s.trail.find(s.trail.note(2)!!)
        val note=s.trail.note(0)!!; note.mode=Mode.BAG; note.place=null
        val p=s.world.addPerson(Species.FOLK,Look(),0.9f,PlaceId.HOME,1f,PlaceId.HOME.floor)
        s.world.playerIds+=p.id
        val crown=s.world.addThing(ThingType.CROWN,0,p.place,p.x,p.y); s.give(p,crown,Part.HAT)
        s.magic.dismissAdventure()
        val back=reload(s); back.world.place=PlaceId.HOME
        assertTrue(back.trail.found(2)); assertNull(back.magic.active)
        back.magic.start(Adventure.RUMLE)
        assertEquals(Mode.BAG,back.trail.note(0)!!.mode)
        assertEquals(PlaceId.HOME,back.trail.nextPlace())
        assertEquals(listOf(p.id),back.world.playerIds.toList())
        assertEquals(p.id,back.world.bodies[crown.id]!!.holder)
        assertEquals(3,back.world.bodies.values.count { it is Thing && it.type==ThingType.RUMLE_NOTE })
    }

    @Test fun preparingNeverRecallsAMovedHeldOrWornPaper() {
        val s=begin(); val note=s.trail.note(0)!!
        note.place=PlaceId.FARM; note.x=2.3f; note.held=true
        s.magic.start(Adventure.RUMLE)
        assertEquals(PlaceId.FARM,s.trail.nextPlace()); assertEquals(2.3f,note.x,0f); assertTrue(note.held)
        note.held=false
        val p=s.world.addPerson(Species.FOLK,Look(),1f,PlaceId.FARM,2.3f,PlaceId.FARM.floor)
        s.give(p,note,Part.HAND)
        assertEquals(Mode.WORN,note.mode); assertEquals(p.id,note.holder); assertTrue(s.trail.found(0))
        s.magic.start(Adventure.RUMLE)
        assertEquals(p.id,note.holder); assertEquals(PlaceId.FARM,note.place)
    }

    @Test fun missingCluesAreRecoverableButUnrelatedBodiesAndOldWorldsAreUntouched() {
        val s=begin(); val missing=s.trail.note(1)!!; val keep=s.trail.note(0)!!
        s.world.bodies.remove(missing.id)
        val ball=s.world.addThing(ThingType.BALL,0,PlaceId.HOME,2f,0.8f)
        s.world.rumleNotes[1]=ball.id // A stale/corrupt reference must not claim somebody else's thing.
        s.trail.prepare()
        assertSame(ball,s.world.bodies[ball.id]); assertEquals(ThingType.BALL,ball.type)
        assertNotEquals(ball.id,s.trail.note(1)!!.id); assertSame(keep,s.trail.note(0))
        val old=WorldStore.encode(World(),Settings()).apply { remove("rumle") }
        val loaded=Sim(WorldStore.decode(old).world)
        assertFalse(loaded.trail.started); assertNull(loaded.trail.lamp()); assertTrue(loaded.world.rumleNotes.all { it == -1 })
    }

    @Test fun undoOfTheLastPickupKeepsThePrizeAndDoesNotGiveADuplicate() {
        val s=begin(); s.trail.find(s.trail.note(0)!!); s.trail.find(s.trail.note(1)!!)
        val note=s.trail.note(2)!!
        val history=WorldHistory({ s.world })
        history.begin(note.id); note.x+=0.2f; s.magic.lifted(note); history.end()
        val lampId=s.trail.lamp()!!.id
        val back=Sim(history.undo()!!)
        assertTrue(back.trail.complete); assertEquals(lampId,back.trail.lamp()!!.id)
        assertEquals("the paper keeps its found stamp after undo",1,back.trail.note(2)!!.used)
        back.magic.start(Adventure.RUMLE)
        assertEquals(1,back.world.bodies.values.count { it is Thing && it.type==ThingType.TROLL_LANTERN })
        assertTrue(First.RUMLE_TREASURE.name in back.world.firsts)
    }

    @Test fun lanternCyclesThreePicturesAndOffAndKeepsItsPictureInAHandAndAfterReload() {
        val s=begin(); finish(s); val lamp=s.trail.lamp()!!
        val p=s.world.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,1.4f,PlaceId.HOME.floor)
        lamp.place=PlaceId.HOME; lamp.mode=Mode.FREE; lamp.x=1.1f; lamp.y=PlaceId.HOME.floor
        s.use(PlaceId.HOME,lamp); assertEquals(1,lamp.used); assertTrue(TreasureTrail.lit(lamp))
        s.give(p,lamp,Part.HAND); assertEquals(1,lamp.used)
        assertTrue(s.personPlay.use(p,lamp)); assertEquals(2,lamp.used)
        val back=reload(s); val saved=back.trail.lamp()!!
        assertEquals(2,saved.used); assertEquals(p.id,saved.holder)
        back.use(PlaceId.HOME,saved); assertEquals(3,saved.used)
        back.use(PlaceId.HOME,saved); assertEquals(0,saved.used); assertFalse(TreasureTrail.lit(saved))
        assertEquals(2,back.world.stickers.size)
    }

    @Test fun hiddenBagItemsAndOtherChildsHeldLanternDoNotReact() {
        val s=begin(); finish(s); val lamp=s.trail.lamp()!!
        assertFalse(s.trail.use(lamp)); assertEquals(0,lamp.used)
        val p=s.world.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,1.3f,PlaceId.HOME.floor)
        lamp.place=p.place; lamp.mode=Mode.FREE; s.give(p,lamp,Part.HAND); p.held=true
        assertFalse(s.trail.use(lamp)); assertEquals(0,lamp.used)
        p.held=false; lamp.held=true
        assertFalse(s.trail.use(lamp)); assertEquals(0,lamp.used)
    }

    @Test fun aMovedLanternIsFoundAtItsActualPlaceAndMissingOneIsRecoveredOnlyOnce() {
        val s=begin(); finish(s); val lamp=s.trail.lamp()!!
        lamp.mode=Mode.FREE; lamp.place=PlaceId.MOUNTAIN; lamp.x=2.4f
        s.magic.start(Adventure.RUMLE)
        assertSame(lamp,s.trail.target()); assertEquals(PlaceId.MOUNTAIN,s.trail.nextPlace())
        assertEquals(2.4f,lamp.x,0f)
        s.world.bodies.remove(lamp.id)
        repeat(4) { s.trail.prepare() }
        assertEquals(1,s.world.bodies.values.count { it is Thing && it.type==ThingType.TROLL_LANTERN })
        assertEquals(Mode.BAG,s.trail.lamp()!!.mode)
    }
}
