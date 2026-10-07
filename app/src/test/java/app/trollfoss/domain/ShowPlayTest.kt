package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class ShowPlayTest {
    private val place = PlaceId.HOME
    private fun sim() = Sim(World()).also { s -> repeat(25) { s.world.stickers += it }; Progression.remember(s.world); s.here = place }
    private fun step(s: Sim, sec: Float) = repeat((sec / 0.02f).toInt()) { s.step(place, 0.02f) }
    private fun folk(s: Sim, x: Float) = s.world.addPerson(Species.FOLK, Look(), 1f, place, x, place.floor)
    private val heard = ArrayList<Pair<Fx, Int>>()
    private fun listening(s: Sim) {
        s.listener = object : SimListener { override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) { heard += fx to param } }
    }

    @Test fun level8OpensAt25Stickers() {
        val s = sim()
        assertEquals(8, Progression.level(s.world))
        val eight = ToyReward.entries.filter { it.level == 8 }
        assertEquals(4, eight.size)
        eight.forEach { assertTrue(Progression.unlocked(s.world, it)) }
    }

    @Test fun theEchoBoxRemembersNearbyNotesAndPlaysThemBackInRhythm() {
        val s = sim(); val box = s.toys.claim(ToyReward.ECHO_BOX, place, 1.2f)!!; listening(s)
        for (k in listOf(0, 4, 7)) { s.listener.onFx(Fx.KEY, box.x + 0.3f, 0.6f, null, null, k); step(s, 0.3f) }
        s.listener.onFx(Fx.KEY, box.x + 3f, 0.6f, null, null, 9) // too far away to hear
        assertEquals(3, s.show.recorded(box))
        heard.clear(); s.tap(place, box, 0f, -0.1f); step(s, 1.5f)
        assertEquals(listOf(0, 4, 7), heard.filter { it.first == Fx.ECHO_NOTE }.map { ShowPlay.echoPitch(it.second) })
        assertEquals("its own playback is not recorded again", 3, s.show.recorded(box))
        assertTrue("ECHO_BOX" in s.world.firsts)
    }

    @Test fun theEchoBoxKeepsOnlyTheLastEightNotes() {
        val s = sim(); val box = s.toys.claim(ToyReward.ECHO_BOX, place, 1.2f)!!
        repeat(20) { s.listener.onFx(Fx.DRUM, box.x, 0.6f, null, null, it % 3); step(s, 0.1f) }
        assertEquals(ShowPlay.MAX_NOTES, s.show.recorded(box))
    }

    @Test fun anEmptyEchoBoxJustSqueaks() {
        val s = sim(); val box = s.toys.claim(ToyReward.ECHO_BOX, place, 1.2f)!!; listening(s)
        s.tap(place, box, 0f, -0.1f); step(s, 0.5f)
        assertTrue(heard.any { it.first == Fx.SQUEAK }); assertTrue(heard.none { it.first == Fx.ECHO_NOTE })
    }

    @Test fun figuresStandingOnTheDanceFloorDanceAndTwoHopTogether() {
        val s = sim(); val floor = s.toys.claim(ToyReward.DANCE_FLOOR, place, 1.2f)!!
        val a = folk(s, floor.x - 0.1f); val b = folk(s, floor.x + 0.1f)
        val held = folk(s, floor.x).also { it.held = true }; val away = folk(s, floor.x + 1.5f)
        assertFalse(s.show.onDanceFloor(a))
        s.tap(place, floor, 0f, -0.02f)
        assertTrue(s.show.onDanceFloor(a) && s.show.onDanceFloor(b))
        assertFalse(s.show.onDanceFloor(held)); assertFalse(s.show.onDanceFloor(away))
        var hopped = false
        repeat(60) { s.step(place, 0.02f); if (a.anim.hopV > 0f) hopped = true }
        assertTrue("two dancers hop together on the beat", hopped)
        assertTrue("DANCE_FLOOR" in s.world.firsts)
    }

    @Test fun theConfettiMachineMakesFriendsHopAndRests() {
        val s = sim(); val m = s.toys.claim(ToyReward.CONFETTI, place, 1.2f)!!; listening(s)
        val p = folk(s, m.x + 0.4f)
        s.tap(place, m, 0f, -0.2f)
        assertEquals(1, heard.count { it.first == Fx.CONFETTI }); assertTrue(p.anim.hopV > 0f)
        s.tap(place, m, 0f, -0.2f)
        assertEquals("resting", 1, heard.count { it.first == Fx.CONFETTI })
        step(s, 2.2f); s.tap(place, m, 0f, -0.2f)
        assertEquals(2, heard.count { it.first == Fx.CONFETTI })
        assertTrue("CONFETTI" in s.world.firsts)
    }

    @Test fun aToysTapAnimationWearsOff() {
        val s = sim(); val m = s.toys.claim(ToyReward.CONFETTI, place, 1.2f)!!
        s.tap(place, m, 0f, -0.2f)
        assertTrue(m.anim > 0f)
        step(s, 1f)
        assertEquals(0f, m.anim, 0f)
    }

    @Test fun musicPlaysWhileAnyMusicMakerIsOn() {
        val s = sim()
        val floor = s.toys.claim(ToyReward.DANCE_FLOOR, place, 1.2f)!!
        val rig = s.toys.claim(ToyReward.LIGHT_RIG, place, 2.4f)!!
        assertFalse(s.musicOn(place))
        s.tap(place, floor, 0f, -0.02f); assertTrue(s.musicOn(place))
        s.tap(place, rig, 0f, -0.3f); s.tap(place, rig, 0f, -0.3f); s.tap(place, rig, 0f, -0.3f) // disco and off again
        assertTrue("the dance floor still plays", s.musicOn(place))
        s.tap(place, floor, 0f, -0.02f); assertFalse(s.musicOn(place))
    }

    @Test fun theLightRigIsOnlyHitWhereItIsDrawn() {
        val s = sim(); val rig = s.toys.claim(ToyReward.LIGHT_RIG, place, 1.2f)!!
        val span = ShowPlay.hitSpan(rig)
        assertTrue("the stand base is hit", -0.1f in span && 0f in span)
        assertTrue("so is the spotlight head to the right", 0.14f in span)
        assertFalse("a tap beside the stand goes to what stands behind it", -0.14f in span)
        assertFalse(0.17f in span)
        val box = s.toys.claim(ToyReward.ECHO_BOX, place, 2.4f)!!
        assertEquals("other toys keep their full width", -box.spec.w / 2..box.spec.w / 2, ShowPlay.hitSpan(box))
    }

    @Test fun theThinDanceFloorIsEasyToHit() {
        val s = sim(); val floor = s.toys.claim(ToyReward.DANCE_FLOOR, place, 1.2f)!!
        assertTrue(ShowPlay.hitHeight(floor) >= 0.12f)
        val box = s.toys.claim(ToyReward.ECHO_BOX, place, 2.4f)!!
        assertEquals(box.spec.h, ShowPlay.hitHeight(box), 0f)
    }

    @Test fun theLightRigCyclesAndKeepsItsModeAfterSaving() {
        val s = sim(); val rig = s.toys.claim(ToyReward.LIGHT_RIG, place, 1.2f)!!
        s.tap(place, rig, 0f, -0.3f); assertEquals(1, rig.mode)
        s.tap(place, rig, 0f, -0.3f); assertEquals(2, rig.mode)
        val back = WorldStore.decode(WorldStore.encode(s.world, Settings())).world
        assertEquals(2, back.fixtures[rig.id]!!.mode)
        s.tap(place, rig, 0f, -0.3f); assertEquals(0, rig.mode)
        assertTrue("LIGHT_RIG" in s.world.firsts)
    }
}
