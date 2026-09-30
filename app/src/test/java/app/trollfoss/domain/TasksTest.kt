package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** The task board. */
class TasksTest {
    @Test
    fun `the board deals three different tasks, mostly in different places`() {
        val world = WorldFactory.create(Random(1))
        val sim = Sim(world)
        val board = sim.tasks.board()
        assertEquals(3, board.size)
        assertEquals(3, board.map { it.id }.toSet().size)
    }

    @Test
    fun `doing a task earns a sticker and a new set comes when all three are done`() {
        val world = WorldFactory.create(Random(2))
        val sim = Sim(world)
        var finished = 0
        sim.tasks.onDone = { finished++ }
        world.taskSet.clear()
        world.taskSet += listOf("sneeze", "tidy", "photo")
        sim.tasks.record(Deed.SNEEZE, PlaceId.CAFE)
        assertEquals(1, finished)
        assertEquals(1, world.stickers.size)
        assertFalse(sim.tasks.allDone())
        sim.tasks.record(Deed.TIDY, PlaceId.HOME)
        sim.tasks.record(Deed.PHOTO, PlaceId.FARM)
        assertTrue(sim.tasks.allDone())
        sim.tasks.deal()
        assertFalse(sim.tasks.allDone())
        assertEquals(3, sim.tasks.board().size)
    }

    @Test
    fun `a sneeze in the game counts for the sneeze task`() {
        val world = WorldFactory.create(Random(3))
        val sim = Sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("sneeze", "tidy", "photo")
        val sondre = world.people().first { it.name == "Sondre" }
        val pepper = world.bodiesIn(PlaceId.CAFE).first { it is Thing && it.type == ThingType.PEPPER } as Thing
        sim.give(sondre, pepper, Part.MOUTH)
        var t = 0f
        while (t < 1f) { sim.step(PlaceId.CAFE, 1f / 60f); t += 1f / 60f }
        assertTrue(sim.tasks.done(sim.tasks.board().first { it.id == "sneeze" }))
    }

    @Test
    fun `tasks in a place only count there, except bringing things`() {
        val world = WorldFactory.create(Random(4))
        val sim = Sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("xray", "egg_to_cafe", "scan")
        sim.tasks.record(Deed.XRAY, PlaceId.HOME)
        assertEquals(0, sim.tasks.progress(sim.tasks.board()[0]))
        sim.tasks.record(Deed.XRAY, PlaceId.DOCTOR)
        assertEquals(1, sim.tasks.progress(sim.tasks.board()[0]))
        sim.tasks.record(Deed.BROUGHT, PlaceId.HOME, ThingType.EGG)
        assertEquals(0, sim.tasks.progress(sim.tasks.board()[1]))
        sim.tasks.record(Deed.BROUGHT, PlaceId.CAFE, ThingType.EGG)
        assertTrue(sim.tasks.done(sim.tasks.board()[1]))
    }

    @Test
    fun `task progress and stickers survive saving`() {
        val world = WorldFactory.create(Random(5))
        val sim = Sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("scan", "wishes", "glimt")
        sim.tasks.record(Deed.SCAN, PlaceId.SHOP)
        sim.tasks.record(Deed.SCAN, PlaceId.SHOP)
        val json = WorldStore.encode(world, Settings()).toString()
        val w2 = WorldStore.decode(org.json.JSONObject(json)).world
        assertEquals(listOf("scan", "wishes", "glimt"), w2.taskSet)
        assertEquals(2, w2.taskProgress["scan"])
    }
}
