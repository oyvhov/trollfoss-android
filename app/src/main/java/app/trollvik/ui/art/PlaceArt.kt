package app.trollvik.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import app.trollvik.domain.PlaceId

/**
 * Draws a place's background in screen pixels: sky or walls, far scenery with parallax, and the floor
 * or ground. [cam] is the left edge of the view in scene units and [u] pixels per unit; scene point
 * (x, y) is at ((x - cam) * u, y * u). Far layers move slower than the camera.
 */
fun DrawScope.drawPlaceBack(place: PlaceId, cam: Float, u: Float, pen: Pen) {
    val top = if (place.outdoor) lerp(Color(0xFF6CC6FF), Color(0xFF171447), pen.night) else Color(0xFFFFE3C9)
    val low = if (place.outdoor) lerp(Color(0xFFDDF3FF), Color(0xFF3B2F7A), pen.night) else Color(0xFFFFD2AE)
    drawRect(Brush.verticalGradient(listOf(top, low), 0f, size.height * place.floor), Offset.Zero, size)
    val floorY = place.floor * u
    drawRect(if (place.outdoor) Color(0xFF7ED957) else Color(0xFFC98A55), Offset(0f, floorY), Size(size.width, size.height - floorY))
    drawLine(Ink.line, Offset(0f, floorY), Offset(size.width, floorY), strokeWidth = pen.lw)
}

/** Draws what lies in front of everything: the sea's surface, snowdrifts, grass tufts. */
fun DrawScope.drawPlaceFront(place: PlaceId, cam: Float, u: Float, pen: Pen) {
    val water = app.trollvik.domain.Places.spec(place).water ?: return
    val x1 = (water.x1 - cam) * u
    val x2 = (water.x2 - cam) * u
    drawRect(Color(0x662F9BFF), Offset(x1, water.line * u), Size(x2 - x1, (water.bottom - water.line) * u + size.height))
}
