package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldHistory
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class FirstsTest {
    private fun sim() = Sim(World())

    @Test fun aFirstGivesOneStickerOnlyOnce() {
        val s = sim(); val before = s.world.stickers.size
        assertTrue(s.firstTime(First.TRACTOR))
        assertFalse(s.firstTime(First.TRACTOR))
        assertEquals(before + 1, s.world.stickers.size)
        assertTrue("TRACTOR" in s.world.firsts)
    }

    @Test fun noFirstsWhileTheWorldIsBuilt() {
        val s = sim(); s.tasks.recording = false
        assertFalse(s.firstTime(First.BOAT))
        assertTrue(s.world.firsts.isEmpty())
    }

    @Test fun listenerHearsTheFirstWithItsPlace() {
        var heard: First? = null
        val s = Sim(World(), object : SimListener { override fun onFirst(first: First, place: PlaceId, x: Float, y: Float) { heard = first } })
        s.firstTime(First.BUBBLE_POP, 1f, 0.5f)
        assertEquals(First.BUBBLE_POP, heard)
    }

    @Test fun tripsCountNewPlaces() {
        val s = sim(); s.visit(PlaceId.HOME)
        s.visit(PlaceId.BEACH)
        assertTrue("FIRST_TRIP" in s.world.firsts)
        listOf(PlaceId.FOREST, PlaceId.FARM, PlaceId.CAFE).forEach(s::visit)
        assertTrue("TRIP_5" in s.world.firsts)
        assertFalse("TRIP_12" in s.world.firsts)
    }

    @Test fun firstsAndVisitedSurviveSaving() {
        val s = sim(); s.firstTime(First.HUG); s.visit(PlaceId.BEACH)
        val back = WorldStore.decode(WorldStore.encode(s.world, Settings())).world
        assertTrue("HUG" in back.firsts)
        assertTrue(PlaceId.BEACH in back.visited)
        assertEquals(s.world.stickers.size, back.stickers.size)
    }

    @Test fun unknownSavedFirstsAreKeptOutOfTheEnum() {
        val json = WorldStore.encode(World(), Settings())
        json.put("firsts", org.json.JSONArray(listOf("HUG", "SOMETHING_FROM_THE_FUTURE")))
        val back = WorldStore.decode(json).world
        assertEquals(setOf("HUG"), back.firsts)
    }

    @Test fun everyGroupHasFirsts() {
        assertEquals(68, First.entries.size)
        FirstGroup.entries.forEach { g -> assertTrue(First.entries.any { it.group == g }) }
    }

    @Test fun aNearbyFriendCheersButAHeldOneDoesNot() {
        val s = sim()
        val near = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.2f, PlaceId.HOME.floor)
        val held = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.3f, PlaceId.HOME.floor).also { it.held = true }
        s.here = PlaceId.HOME
        s.firstTime(First.HUG, 1f, PlaceId.HOME.floor - 0.2f)
        assertEquals(Face.GRIN, near.anim.face)
        assertNotEquals(Face.GRIN, held.anim.face)
        assertTrue(near.anim.wave > 0f)
    }

    @Test fun lowerThresholdsNeverLowerALevel() {
        assertEquals(listOf(0, 2, 4, 7, 10, 14, 19), Progression.thresholds)
        val w = World(); repeat(20) { w.stickers += it }
        assertEquals(7, Progression.level(w))
        for ((i, n) in listOf(0, 2, 4, 7).withIndex()) { w.stickers.clear(); repeat(n) { w.stickers += it }; assertEquals(i + 1, Progression.level(w)) }
    }

    @Test fun retroUpgradeCountsStoredStateOnce() {
        val w = World(); w.flags += "place:repaired:99"; w.community.pets[1] = 2; w.mine.ground[0] = 1
        val stickers = w.stickers.size
        FirstsRetro.upgrade(w); FirstsRetro.upgrade(w)
        assertTrue("REPAIR_LIGHT" in w.firsts && "PET" in w.firsts && "BUILD_ROOM" in w.firsts)
        assertEquals(stickers + 3, w.stickers.size)
        assertTrue(FirstsRetro.FLAG in w.flags)
    }

    @Test fun loadingAnOldSaveRunsTheRetroUpgrade() {
        val w = World(); w.flags += TreasureStart.FLAG; w.flags += "place:repaired:99"
        val back = WorldStore.decode(WorldStore.encode(w, Settings())).world
        assertTrue("REPAIR_LIGHT" in back.firsts)
        assertEquals(w.stickers.size + 1, back.stickers.size)
    }

    @Test fun loadingAnOldSaveCountsItsPetArtAndSecretDoor() {
        val s = sim(); val w = s.world; w.flags += TreasureStart.FLAG
        val owner = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1f, PlaceId.HOME.floor)
        val cat = w.addPerson(Species.CAT, Look(), 1f, PlaceId.HOME, 1.3f, PlaceId.HOME.floor)
        w.community.pets[owner.id] = cat.id
        w.community.art[w.nextId++] = mutableListOf(ArtMark(0, 0, 0.5f, 0.5f))
        val json = WorldStore.encode(w, Settings())
        val back = WorldStore.decode(json).world
        assertTrue("PET" in back.firsts)
        assertTrue("STAMP_ART" in back.firsts)
    }

    @Test fun newWorldsSkipTheRetroUpgrade() {
        assertTrue(FirstsRetro.FLAG in WorldFactory.create().flags)
    }

    @Test fun undoKeepsFirstsAndStickers() {
        var w = World(); val s = Sim(w); val h = WorldHistory({ w }); s.journal = h
        w.flags += TreasureStart.FLAG
        val a = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.8f, 0.9f)
        h.begin(a.id); a.x = 1.5f; s.firstTime(First.TIDY); h.end()
        val stickers = w.stickers.size
        w = h.undo()!!
        assertEquals(0.8f, w.bodies[a.id]!!.x, 0f)
        assertTrue("TIDY" in w.firsts)
        assertEquals(stickers, w.stickers.size)
    }
}
