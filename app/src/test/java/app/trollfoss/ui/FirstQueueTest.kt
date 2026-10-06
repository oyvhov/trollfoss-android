package app.trollfoss.ui

import app.trollfoss.domain.First
import org.junit.Assert.*
import org.junit.Test

class FirstQueueTest {
    @Test fun popsAreSpacedApart() {
        val q = FirstQueue(gap = 1.2f, max = 5)
        q.push(First.TRACTOR, 10f, 20f); q.push(First.BOAT, Float.NaN, Float.NaN)
        val first = q.due(0f)!!
        assertEquals(First.TRACTOR, first.first); assertEquals(10f, first.x, 0f)
        assertNull(q.due(0.5f))
        assertEquals(First.BOAT, q.due(1.3f)!!.first)
        assertNull(q.due(5f))
    }

    @Test fun aLongQueueFoldsIntoOneWithACount() {
        val q = FirstQueue(gap = 1.2f, max = 5)
        repeat(8) { q.push(First.entries[it], Float.NaN, Float.NaN) }
        val shown = ArrayList<FirstPop>(); var t = 0f
        while (true) { val p = q.due(t) ?: break; shown += p; t += 1.3f }
        assertEquals(5, shown.size)
        assertEquals(3, shown.last().extra)
        assertEquals(8, shown.size + shown.sumOf { it.extra })
    }
}
