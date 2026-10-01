package app.trollfoss.ui.play

import kotlin.math.min

/** Panels reserve space without changing the size of the toys. All scene tools use the visible centre. */
data class PlayViewport(val width: Float, val height: Float, val panel: Float = 0f) {
    val unit: Float = min(height, width / if (width / height < 2.05f) 2.35f else 2.05f)
    val right: Float = (width - panel).coerceIn(width * 0.35f, width)
    val visibleUnits: Float = right / unit
    fun focus(camera: Float): Float = camera + visibleUnits / 2f
    fun cameraFor(focus: Float): Float = focus - visibleUnits / 2f
}
