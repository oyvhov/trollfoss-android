package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** A tap that hit nothing: the nearest furniture that would have answered shows itself. */
class TapHintTest {
    private val place = PlaceId.HOME

    @Test fun `distance is zero inside the box of a piece and grows outside it`() {
        val f = Fixture(1, place, FixtureType.CHEST, 1f, 0.9f)      // 0.18 wide, 0.10 high
        assertEquals(0f, TapHint.distance(f, 1f, 0.85f), 0.0001f)
        assertEquals(0.1f, TapHint.distance(f, 1.19f, 0.85f), 0.0001f)
        assertEquals(0.2f, TapHint.distance(f, 1f, 0.6f), 0.0001f)
    }

    @Test fun `the nearest furniture that answers a tap is found within reach, and nothing beyond it`() {
        val w = World(); val s = Sim(w)
        val chest = s.designer.add(place, FixtureType.CHEST, 0, 1.0f, 0.9f)!!
        assertSame(chest, TapHint.nearest(w, place, chest.x + 0.3f, chest.y - 0.3f))
        assertNull(TapHint.nearest(w, place, chest.x + 0.6f, chest.y - 0.3f))
    }

    @Test fun `furniture that does nothing when tapped never hints`() {
        val w = World(); val s = Sim(w)
        val rug = s.designer.add(place, FixtureType.RUG, 0, 1.0f, 0.9f)!!
        assertFalse(TapHint.answers(w, rug))
        assertNull(TapHint.nearest(w, place, rug.x, rug.y - 0.2f))
    }

    @Test fun `of two pieces the nearer one answers`() {
        val w = World(); val s = Sim(w)
        val near = s.designer.add(place, FixtureType.CHEST, 0, 1.0f, 0.9f)!!
        val far = s.designer.add(place, FixtureType.TOY_BOX, 0, 1.6f, 0.9f)!!
        assertSame(near, TapHint.nearest(w, place, near.x + 0.15f, near.y - 0.2f))
        assertSame(far, TapHint.nearest(w, place, far.x - 0.15f, far.y - 0.2f))
    }

    @Test fun `stairs, cupboards, machines and vehicles answer`() {
        val w = WorldFactory.create(Random(1))
        val stairs = w.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MANOR_GROUND, GroundIx.STAIRS))
        assertTrue(TapHint.answers(w, stairs))
        assertTrue(TapHint.answers(w, w.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.WARDROBE }))
        assertTrue(TapHint.answers(w, w.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.STOVE }))
        assertTrue(TapHint.answers(w, w.fixturesIn(PlaceId.FARM).first { it.type == FixtureType.TRACTOR }))
    }
}
