package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Every world gets one treasure box in the bedroom of Familiehuset, once. */
class TreasureStartTest {
    private fun boxes(w: World) = w.fixtures.values.filter { it.type == FixtureType.TREASURE_BOX }
    private fun reload(w: World) = WorldStore.decode(WorldStore.encode(w, Settings())).world

    /** What a save from 1.7.1 looks like: no box, no flag. */
    private fun oldWorld(): World = WorldFactory.create(Random(1)).also { w ->
        boxes(w).forEach { w.fixtures.remove(it.id) }
        w.flags.remove(TreasureStart.FLAG)
    }

    @Test fun `a new world has one treasure box in the bedroom of Familiehuset`() {
        val w = WorldFactory.create(Random(1))
        assertEquals(1, boxes(w).size)
        val box = boxes(w).single()
        assertEquals(PlaceId.HOME, box.place)
        assertEquals(0, Decor.roomAt(PlaceId.HOME, box.x))
        assertTrue(TreasureStart.FLAG in w.flags)
    }

    @Test fun `an old save gets the box once and keeps everything else`() {
        val old = oldWorld()
        val bodies = old.bodies.size
        val fixtures = old.fixtures.size
        val loaded = reload(old)
        assertEquals(1, boxes(loaded).size)
        assertEquals(fixtures + 1, loaded.fixtures.size)
        assertEquals(bodies, loaded.bodies.size)
        assertTrue(TreasureStart.FLAG in loaded.flags)
        // Loading again adds nothing.
        assertEquals(fixtures + 1, reload(loaded).fixtures.size)
    }

    @Test fun `a child who put the box in the store does not get a second one`() {
        val w = WorldFactory.create(Random(1)); val s = Sim(w)
        assertTrue(s.designer.store(PlaceId.HOME, boxes(w).single()))
        w.flags.remove(TreasureStart.FLAG)
        val loaded = reload(w)
        assertTrue(boxes(loaded).isEmpty())
        assertEquals(1, loaded.storage.count { it.type == FixtureType.TREASURE_BOX })
    }

    @Test fun `a child who threw the box away after the gift does not get it back by itself`() {
        val w = WorldFactory.create(Random(1)); val s = Sim(w)
        assertTrue(s.designer.store(PlaceId.HOME, boxes(w).single()))
        assertTrue(s.designer.discard(w.storage.lastIndex))
        val loaded = reload(w)
        assertTrue(boxes(loaded).isEmpty())
        assertTrue(loaded.storage.none { it.type == FixtureType.TREASURE_BOX })
    }
}
