package app.trollfoss.ui.art

import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The doorway painted on a staircase belongs to the staircase: a tap on it is a tap on the stairs. */
class StairDoorwaysTest {
    private fun stairs(place: PlaceId, variant: Int = 0) = Fixture(1, place, FixtureType.STAIRCASE, 0f, 0f, variant)

    @Test fun `the glowing arch behind the grand stairs lies above the box of the stairs and is part of them`() {
        val f = stairs(PlaceId.MANOR_GROUND)
        assertTrue("above the box", -0.75f < -f.spec.h)
        assertTrue(StairDoorways.hit(f, -0.17f, -0.75f, 0f))
        assertFalse(StairDoorways.hit(f, 0.30f, -0.75f, 0f))
    }

    @Test fun `the lit door at the top of the cellar stairs is part of them`() {
        val f = stairs(PlaceId.MANOR_CELLAR)
        assertTrue(StairDoorways.hit(f, -0.34f, -0.70f, 0f))
        assertFalse(StairDoorways.hit(f, 0.20f, -0.70f, 0f))
    }

    @Test fun `the little door to the attic is part of the stairs up, not of the stairwell down`() {
        assertTrue(StairDoorways.hit(stairs(PlaceId.MANOR_UPPER, variant = 1), 0.25f, -0.70f, 0f))
        assertNull(StairDoorways.of(stairs(PlaceId.MANOR_UPPER, variant = 0)))
    }

    @Test fun `stairs without a painted doorway and other furniture have none`() {
        assertNull(StairDoorways.of(stairs(PlaceId.MANOR_ATTIC)))
        assertNull(StairDoorways.of(stairs(PlaceId.MINE_GROUND, variant = 2)))
        assertNull(StairDoorways.of(Fixture(1, PlaceId.MANOR_GROUND, FixtureType.LIFT, 0f, 0f)))
    }

    @Test fun `the margin widens the doorway a little`() {
        val f = stairs(PlaceId.MANOR_CELLAR)
        assertFalse(StairDoorways.hit(f, -0.44f, -0.70f, 0f))
        assertTrue(StairDoorways.hit(f, -0.44f, -0.70f, 0.012f))
    }
}
