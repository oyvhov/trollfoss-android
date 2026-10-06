package app.trollfoss.ui

import app.trollfoss.domain.First

/** A sticker on its way to the book: from screen point ([x], [y]), NaN when it happened elsewhere. */
data class FirstPop(val first: First, val x: Float, val y: Float, val extra: Int = 0)

/** At most one flying sticker per [gap] seconds; when more than [max] wait, the last one carries a count. */
class FirstQueue(private val gap: Float = 1.2f, private val max: Int = 5) {
    private val waiting = ArrayDeque<FirstPop>()
    private var last = -99f

    fun push(first: First, x: Float, y: Float) {
        if (waiting.size >= max) waiting.addLast(waiting.removeLast().let { it.copy(extra = it.extra + 1) })
        else waiting.addLast(FirstPop(first, x, y))
    }

    fun due(now: Float): FirstPop? {
        if (now - last < gap || waiting.isEmpty()) return null
        last = now
        return waiting.removeFirst()
    }
}
