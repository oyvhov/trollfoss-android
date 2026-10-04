package app.trollfoss.ui.play

/**
 * Counts play in the scene while the furniture panel is open. Moving one figure in the middle of decorating
 * is still decorating; [limit] things or figures in a row, with no furniture and no touch on the panel in
 * between, means the child has gone back to playing and the panel is in the way.
 */
class PanelIdle(private val limit: Int = 2) {
    private var lifts = 0

    /** The child touched the panel or moved furniture: still decorating. */
    fun decorating() {
        lifts = 0
    }

    /** A thing or a figure was moved. True when the panel should step aside. */
    fun played(): Boolean {
        lifts++
        return lifts >= limit
    }
}
