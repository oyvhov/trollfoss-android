package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class GiftParcelTest {
    private fun sim() = Sim(World()).also { s -> repeat(40) { s.world.stickers += it }; Progression.remember(s.world) }

    @Test fun theParcelSetsOutTheFirstToyOfTheLevel() {
        val s = sim()
        val f = s.toys.openGift(6, PlaceId.HOME, 1.2f)!!
        assertEquals(ToyReward.entries.first { it.level == 6 }.type, f.type)
        assertEquals(PlaceId.HOME, f.place)
    }

    @Test fun noParcelToyForFreeOrLockedLevels() {
        assertNull(sim().toys.openGift(1, PlaceId.HOME, 1.2f))
        val fresh = Sim(World())
        assertNull(fresh.toys.openGift(6, PlaceId.HOME, 1.2f))
        assertTrue(fresh.world.fixtures.isEmpty())
    }

    @Test fun aFullRoomPlacesNothingAndLosesNothing() {
        val s = sim(); val big = ToyReward.PICNIC
        while (s.toys.claim(big, PlaceId.HOME, 1.2f) != null) Unit
        val before = s.world.fixtures.size
        assertNull(s.toys.openGift(6, PlaceId.HOME, 1.2f))
        assertEquals(before, s.world.fixtures.size)
        ToyReward.entries.filter { it.level == 6 }.forEach { assertTrue(Progression.unlocked(s.world, it)) }
    }
}
