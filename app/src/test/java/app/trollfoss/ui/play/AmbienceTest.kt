package app.trollfoss.ui.play

import org.junit.Assert.*
import org.junit.Test

class AmbienceTest {
    @Test fun ripplesAreCappedAndFadeAway() {
        val r = Ripples(max = 12, life = 1.2f)
        repeat(20) { r.add(it * 0.1f, 0.8f) }
        assertEquals(12, r.count)
        r.step(0.6f)
        assertEquals(12, r.count)
        assertTrue(r.age(0) in 0.59f..0.61f)
        r.step(0.7f)
        assertEquals(0, r.count)
    }

    @Test fun theNewestRippleReplacesTheOldestWhenFull() {
        val r = Ripples(max = 2, life = 1f)
        r.add(1f, 0f); r.step(0.5f); r.add(2f, 0f); r.add(3f, 0f)
        assertEquals(2, r.count)
        assertEquals(setOf(2f, 3f), (0 until r.count).map { r.x(it) }.toSet())
    }
}
