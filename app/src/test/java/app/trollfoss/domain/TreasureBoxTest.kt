package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/** The glass-fronted treasure box: easy to hit, shows what is in it, and never loses a find. */
class TreasureBoxTest {
    private val place = PlaceId.HOME
    private val fxs = ArrayList<Fx>()

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) { fxs += fx }
    }, Random(1))

    private fun box(sim: Sim) = sim.designer.add(place, FixtureType.TREASURE_BOX, 0, 1.6f, 0.9f)!!

    private fun step(sim: Sim, seconds: Float) {
        var t = 0f
        while (t < seconds) { sim.step(place, 1f / 60f); t += 1f / 60f }
    }

    private fun gem(world: World, f: Fixture) = world.addThing(ThingType.GEM, 0, place, f.x, f.y - 0.1f)

    @Test fun `the box has a glass front and room inside`() {
        val spec = FixtureType.TREASURE_BOX.spec
        assertTrue(spec.glass)
        assertTrue(spec.container != null && spec.dropZone != null)
        assertEquals(3, spec.surfaces.count { it.interior })
    }

    @Test fun `a thing let go over the box lies on a shelf inside it`() {
        val w = World(); val s = sim(w); val f = box(s)
        val t = gem(w, f)
        assertTrue(s.dropInto(place, f, t))
        assertEquals(f.id, t.inside)
        assertTrue(t.resting)
        assertEquals(listOf(t), s.treasure.holds(f))
        assertTrue(Fx.TREASURE_IN in fxs)
    }

    @Test fun `twenty finds each get a place of their own inside the box`() {
        val w = World(); val s = sim(w); val f = box(s)
        repeat(20) { assertTrue(s.dropInto(place, f, gem(w, f))) }
        val inside = s.treasure.holds(f)
        assertEquals(20, inside.size)
        assertEquals(20, inside.map { it.x to it.y }.toSet().size)
        val room = f.spec.container!!
        for (t in inside) {
            assertTrue(abs(t.x - f.x) <= room.right)
            assertTrue(t.y <= f.y + room.bottom + 0.0001f && t.y - t.h >= f.y + room.top)
        }
    }

    @Test fun `every fifth find sets off the party`() {
        val w = World(); val s = sim(w); val f = box(s)
        repeat(4) { s.dropInto(place, f, gem(w, f)) }
        assertEquals(0, fxs.count { it == Fx.TREASURE_PARTY })
        s.dropInto(place, f, gem(w, f))
        assertEquals(1, fxs.count { it == Fx.TREASURE_PARTY })
        repeat(5) { s.dropInto(place, f, gem(w, f)) }
        assertEquals(2, fxs.count { it == Fx.TREASURE_PARTY })
    }

    @Test fun `the lid lifts for a held thing nearby and falls shut a moment after it is gone`() {
        val w = World(); val s = sim(w); val f = box(s)
        val t = w.addThing(ThingType.GEM, 0, place, f.x + 0.1f, f.y - 0.1f).apply { held = true }
        step(s, 0.1f)
        assertTrue(f.open)
        t.held = false; t.x = f.x + 1.2f
        step(s, 1.0f)
        assertTrue("still open after one second", f.open)
        step(s, 0.4f)
        assertFalse(f.open)
    }

    @Test fun `a thing held far away leaves the lid shut`() {
        val w = World(); val s = sim(w); val f = box(s)
        w.addThing(ThingType.GEM, 0, place, f.x + 0.6f, f.y - 0.1f).apply { held = true }
        step(s, 0.5f)
        assertFalse(f.open)
    }

    @Test fun `a tap opens the box by hand and it stays the way the child left it`() {
        val w = World(); val s = sim(w); val f = box(s)
        s.tap(place, f, 0f, -0.1f)
        assertTrue(f.open)
        step(s, 3f)
        assertTrue(f.open)
        s.tap(place, f, 0f, -0.1f)
        assertFalse(f.open)
    }

    @Test fun `a box with finds in it stays out of the store and an empty one goes in`() {
        val w = World(); val s = sim(w); val f = box(s)
        val t = gem(w, f)
        s.dropInto(place, f, t)
        assertFalse(s.designer.store(place, f))
        assertTrue(w.fixtures[f.id] === f)
        assertTrue("it opens to show why", f.open)
        assertEquals(listOf(t), s.treasure.holds(f))
        // The child takes the gem out.
        t.inside = -1; t.resting = false; t.restOwner = -2; t.x = f.x + 1f; t.y = place.floor
        assertTrue(s.designer.store(place, f))
        assertEquals(FixtureType.TREASURE_BOX, w.storage.last().type)
    }

    @Test fun `finds are still in the box after saving and loading`() {
        val w = World(); val s = sim(w); val f = box(s)
        repeat(3) { s.dropInto(place, f, gem(w, f)) }
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val s2 = Sim(loaded)
        s2.settle(place)
        val f2 = loaded.fixtures.getValue(f.id)
        assertEquals(FixtureType.TREASURE_BOX, f2.type)
        assertEquals(3, s2.treasure.holds(f2).size)
    }

    @Test fun `the box is free in the catalogue of every place`() {
        val w = World()
        for (p in PlaceId.entries) {
            val item = Decor.catalogue(p).firstOrNull { it.type == FixtureType.TREASURE_BOX }
            assertTrue("$p", item != null && item.stickers == 0 && Decor.available(w, item))
        }
    }
}
