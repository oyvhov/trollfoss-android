package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Gems, coins and pearls are what a child collects: nothing takes them away on its own. */
class TreasureTest {
    @Test fun `a full place lets go of its oldest loose thing, but never of a treasure`() {
        val w = World()
        val gem = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1f, 0.9f).apply { z = 1L }
        val coin = w.addThing(ThingType.COIN, 0, PlaceId.HOME, 1.1f, 0.9f).apply { z = 2L }
        val pearl = w.addThing(ThingType.PEARL, 0, PlaceId.HOME, 1.15f, 0.9f).apply { z = 3L }
        val sock = w.addThing(ThingType.UP_SOCK, 0, PlaceId.HOME, 1.2f, 0.9f).apply { z = 4L }
        val ball = w.addThing(ThingType.BALL, 0, PlaceId.HOME, 1.3f, 0.9f).apply { z = 5L }
        assertSame(sock, Treasure.oldestToDrop(listOf(gem, coin, pearl, sock, ball), keep = ball))
        assertNull(Treasure.oldestToDrop(listOf(gem, coin, pearl), keep = null))
        // The newest thing is spared, and so is whatever the caller asks to spare.
        assertNull(Treasure.oldestToDrop(listOf(gem, sock), keep = sock))
        assertSame(ball, Treasure.oldestToDrop(listOf(sock, ball), keep = null) { it === sock })
    }

    @Test fun `things held or lying inside furniture are not loose`() {
        val w = World()
        val held = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1f, 0.9f).apply { held = true }
        val inside = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1.1f, 0.9f).apply { inside = 5 }
        val loose = w.addThing(ThingType.GEM, 0, PlaceId.HOME, 1.2f, 0.9f)
        assertEquals(1, Treasure.loose(w, PlaceId.HOME, ThingType.GEM))
        assertEquals(0, Treasure.loose(w, PlaceId.HOME, ThingType.COIN))
        assertTrue(listOf(held, inside, loose).all(Treasure::isTreasure))
    }

    @Test fun `a place full of treasure keeps every piece when a machine makes one more thing`() {
        val w = WorldFactory.create(Random(1)); val s = Sim(w); val place = PlaceId.TIVOLI
        val cart = w.fixturesIn(place).first { it.type == FixtureType.POPCORN_CART }
        val gems = (0 until Sim.MAX_THINGS + 5).map { w.addThing(ThingType.GEM, it % 5, place, 0.5f + it * 0.02f, place.floor) }
        s.tap(place, cart, 0f, -0.1f)
        assertTrue(gems.all { w.bodies[it.id] === it })
    }
}
