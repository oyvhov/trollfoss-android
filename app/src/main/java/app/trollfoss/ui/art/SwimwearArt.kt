package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.Garment

/** Swim outfits hide the saved trousers and shoes; putting the old top back restores the whole look. */
internal fun DrawScope.swimwearDetails(color: Color, h: Float, pen: Pen, shirt: Boolean) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    for (side in floatArrayOf(-1f, 1f)) {
        drawLine(color.lighten(.6f), p(side * .13f, -.48f), p(side * .145f, -.23f), .023f * h)
    }
    drawArc(color.darken(.3f), 0f, 180f, false, p(-.075f, -.525f), Size(.15f * h, .085f * h), style = pen.thin)
    val wave = Path().apply {
        moveTo(-.11f * h, -.33f * h)
        cubicTo(-.04f * h, -.4f * h, .02f * h, -.28f * h, .11f * h, -.35f * h)
    }
    drawPath(wave, Color(0xFFFFF1C4), style = pen.stroke)
    if (shirt) drawLine(color.darken(.35f), p(-.16f, -.247f), p(.16f, -.247f), pen.lw)
}

internal fun DrawScope.drawSwimGarment(style: Int, color: Color, w: Float, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * w, y * h)
    val sleeves = style == Garment.SWIM_SET
    val suit = Path().apply {
        moveTo(-.23f * w, -.94f * h)
        quadraticTo(0f, -.74f * h, .23f * w, -.94f * h)
        if (sleeves) {
            lineTo(.49f * w, -.73f * h); lineTo(.38f * w, -.50f * h); lineTo(.27f * w, -.59f * h)
        }
        lineTo(.31f * w, -.04f * h); lineTo(.055f * w, -.04f * h)
        lineTo(0f, -.24f * h); lineTo(-.055f * w, -.04f * h); lineTo(-.31f * w, -.04f * h)
        if (sleeves) {
            lineTo(-.27f * w, -.59f * h); lineTo(-.38f * w, -.50f * h); lineTo(-.49f * w, -.73f * h)
        }
        close()
    }
    inked(suit, color, pen)
    clipPath(suit) {
        for (side in floatArrayOf(-1f, 1f)) drawLine(color.lighten(.6f), p(side * .23f, -.94f), p(side * .25f, 0f), .04f * w)
        val wave = Path().apply {
            moveTo(-.21f * w, -.51f * h)
            cubicTo(-.08f * w, -.64f * h, .06f * w, -.41f * h, .21f * w, -.54f * h)
        }
        drawPath(wave, Color(0xFFFFF1C4), style = pen.stroke)
        if (sleeves) drawLine(color.darken(.35f), p(-.4f, -.32f), p(.4f, -.32f), pen.lw)
    }
}
