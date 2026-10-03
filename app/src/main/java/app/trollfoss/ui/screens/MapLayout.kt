package app.trollfoss.ui.screens

import androidx.compose.ui.geometry.Rect
import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.art.mapSpot

/** Map layout distances are in dp, including the measured controls and their outer padding. */
internal data class MapControls(val edge: Float, val button: Float, val gap: Float) {
    val groupWidth get() = button * 3 + gap * 2
}

internal fun mapControls(height: Float) =
    if (height < 500f) MapControls(8f, 56f, 8f) else MapControls(16f, 64f, 12f)

internal fun mapMarkerBounds(place: PlaceId, width: Float, height: Float, controlsHeight: Float): Rect {
    val spot = mapSpot(place)
    // Raised coastal targets must not take taps from the valley labels above them.
    val markerHeight = if (spot.y > 0.75f) 48f else 110f
    val top = (spot.y * height + 58f - markerHeight)
        .coerceAtMost((height - controlsHeight - markerHeight).coerceAtLeast(0f))
    val left = spot.x * width - 70f
    return Rect(left, top, left + 140f, top + markerHeight)
}
