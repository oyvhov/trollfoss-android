package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Equal pieces in the store are shown as one card with a count. */
class StoreStacksTest {
    private val pot0 = Stored(FixtureType.FLOWER_POT, 0)
    private val pot1 = Stored(FixtureType.FLOWER_POT, 1)
    private val sofa = Stored(FixtureType.SOFA, 0)

    @Test fun `equal pieces become one stack that counts them and points at the newest`() {
        val stacks = StoreStacks.of(listOf(pot0, sofa, pot0, pot1, pot0))
        assertEquals(listOf(StoreStack(pot0, 3, 4), StoreStack(sofa, 1, 1), StoreStack(pot1, 1, 3)), stacks)
    }

    @Test fun `pieces that differ in mode or door stay apart`() {
        val art1 = Stored(FixtureType.PLAY_ART, 0, mode = 3)
        val art2 = Stored(FixtureType.PLAY_ART, 0, mode = 4)
        val door1 = Stored(FixtureType.PLAY_DOOR, 0, 0, RoomLink(PlaceId.HOME, 1))
        val door2 = Stored(FixtureType.PLAY_DOOR, 0, 0, RoomLink(PlaceId.HOME, 2))
        val stacks = StoreStacks.of(listOf(art1, art2, door1, door2))
        assertEquals(4, stacks.size)
        assertTrue(stacks.all { it.count == 1 })
    }

    @Test fun `an empty store has no stacks`() {
        assertTrue(StoreStacks.of(emptyList()).isEmpty())
    }

    @Test fun `taking one from a stack leaves a smaller stack in the same place`() {
        val w = WorldFactory.create(); val s = Sim(w)
        w.storage += listOf(pot0, sofa, pot0, pot0)
        val stack = StoreStacks.of(w.storage).first()
        assertEquals(3, stack.count)
        assertNotNull(s.designer.unstore(PlaceId.BEACH, stack.index, 1f, 0.9f))
        assertEquals(listOf(StoreStack(pot0, 2, 2), StoreStack(sofa, 1, 1)), StoreStacks.of(w.storage))
    }

    @Test fun `the recycling box is grouped by the piece and points into the box`() {
        val stacks = StoreStacks.ofDiscarded(listOf(0 to pot0, 5 to sofa, 2 to pot0))
        assertEquals(listOf(StoreStack(pot0, 2, 2), StoreStack(sofa, 1, 1)), stacks)
    }
}
