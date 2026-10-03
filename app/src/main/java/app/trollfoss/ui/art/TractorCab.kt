package app.trollfoss.ui.art

/** Roof dimensions shared by the drawing and touch geometry, in scene units. */
internal object TractorCab {
    const val LEFT = -0.225f
    const val RIGHT = 0.07f
    const val TOP = -0.49f
    const val DEPTH = 0.2f

    fun contains(x: Float, y: Float, margin: Float): Boolean {
        // Include the cab posts and their opening; the driver remains separately draggable.
        return x in (LEFT - margin)..(RIGHT + Oblique.DX * DEPTH + margin) &&
            y in (TOP + Oblique.DY * DEPTH - margin)..(-0.14f + margin)
    }
}
