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
