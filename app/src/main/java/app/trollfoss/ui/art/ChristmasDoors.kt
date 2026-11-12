package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.ThingType

/** The pictures are destinations: a child can recognise a doorway before reading its name. */
internal fun DrawScope.villageDoor(to: PlaceId?, w: Float, h: Float, pen: Pen) {
    val colour = when (to) {
        PlaceId.CHRISTMAS_HOME -> XmasRed
        PlaceId.CHRISTMAS_WORKSHOP -> Color(0xFF417486)
        PlaceId.CHRISTMAS_STABLE -> Color(0xFF895D46)
        else -> XmasGreen
    }
    inkedRound(Rect(-0.54f*w, -h, 0.54f*w, 0f), 0.08f*w, XmasCream, pen)
    inkedRound(Rect(-0.43f*w, -0.92f*h, 0.43f*w, 0f), 0.08f*w, colour, pen)
    val canopy = Path().apply {
        moveTo(-0.64f*w, -0.92f*h)
        lineTo(-0.48f*w, -1.06f*h)
        lineTo(0.48f*w, -1.06f*h)
        lineTo(0.64f*w, -0.92f*h)
        close()
    }
    inked(canopy, if (to == PlaceId.CHRISTMAS_SQUARE) XmasGold else XmasSnow, pen)
    inkedCircle(Offset(0f, -0.64f*h), 0.34f*w, XmasCream, pen)
    translate(0f, -0.64f*h + 0.23f*w) {
        when (to) {
            PlaceId.CHRISTMAS_HOME -> drawChristmasThing(ThingType.XM_HAT, 0, 0, 0.48f*w, 0.46f*w, pen)
            PlaceId.CHRISTMAS_WORKSHOP -> drawChristmasThing(ThingType.XM_TRAIN, 0, 0, 0.53f*w, 0.42f*w, pen)
            PlaceId.CHRISTMAS_STABLE -> {
                drawChristmasThing(ThingType.XM_HAT, 2, 0, 0.54f*w, 0.48f*w, pen)
                inkedOval(Rect(-0.14f*w, -0.23f*w, 0.14f*w, 0.04f*w), Color(0xFFA67553), pen)
                drawCircle(Ink.line, 0.018f*w, Offset(-0.065f*w, -0.13f*w))
                drawCircle(Ink.line, 0.018f*w, Offset(0.065f*w, -0.13f*w))
                inkedCircle(Offset(0f, -0.04f*w), 0.045f*w, XmasRed, pen)
            }
            else -> {
                val arrow = Path().apply {
                    moveTo(-0.24f*w, -0.23f*w)
                    lineTo(-0.02f*w, -0.43f*w)
                    lineTo(-0.02f*w, -0.31f*w)
                    lineTo(0.22f*w, -0.31f*w)
                    lineTo(0.22f*w, -0.15f*w)
                    lineTo(-0.02f*w, -0.15f*w)
                    lineTo(-0.02f*w, -0.03f*w)
                    close()
                }
                inked(arrow, XmasGreen, pen)
            }
        }
    }
    inkedCircle(Offset(0.31f*w, -0.36f*h), 0.05f*w, XmasGold, pen)
    inkedRound(Rect(-0.5f*w, -0.055f*h, 0.5f*w, 0.015f*h), 0.025f*w, XmasCream, pen)
}
