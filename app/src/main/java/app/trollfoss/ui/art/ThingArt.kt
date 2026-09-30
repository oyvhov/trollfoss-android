package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Cat
import app.trollfoss.domain.ThingType

/**
 * Draws a thing with its origin at the bottom centre, inside the box (-w/2, -h) to (w/2, 0) in pixels.
 * [variant] is the colour or flavour, [used] how many bites have been taken (drawn as bite marks), and
 * [cook] a hint of browning for food on the stove. Hats are drawn as they sit on a head.
 */
fun DrawScope.drawThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float = 0f) {
    val box = Rect(-w / 2, -h, w / 2, 0f)
    val color = when (type.cat) {
        Cat.FOOD -> Color(0xFFF2B45A)
        Cat.DRINK -> Color(0xFF4AB3FF)
        Cat.POTION -> Color(0xFF8B5CF6)
        Cat.HAT -> Color(0xFFFF5A4E)
        Cat.GLASSES -> Color(0xFF2B2140)
        Cat.GARMENT -> Color(0xFF3BC46B)
        Cat.TOY -> Color(0xFFFFC83D)
        Cat.TOOL -> Color(0xFF8E93A6)
        Cat.NATURE -> Color(0xFF7ED957)
        Cat.HOME -> Color(0xFFFF9EC7)
        Cat.MAGIC -> Color(0xFFC4A6FF)
    }
    inkedRound(box, minOf(w, h) * 0.3f, color, pen)
    val p = Path().apply {
        moveTo(0f, -h * 0.8f)
        lineTo(w * 0.2f, -h * 0.5f)
        lineTo(0f, -h * 0.2f)
        lineTo(-w * 0.2f, -h * 0.5f)
        close()
    }
    drawPath(p, Color.White.copy(alpha = 0.5f))
    shine(Offset(-w * 0.2f, -h * 0.75f), w * 0.15f, h * 0.1f)
}
