package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import kotlin.math.cos
import kotlin.math.sin

/** Small, readable motifs rather than the tiny text/logos in the photo references. */
internal fun DrawScope.portraitPattern(look: Look, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    val accent = argb(Palette.cloth[look.accent])
    fun sun(x: Float, y: Float, radius: Float, color: Color) {
        for (k in 0..9) {
            val a = k * Math.PI.toFloat() / 5f
            drawLine(color, p(x + cos(a) * radius * 1.25f, y + sin(a) * radius * 1.25f),
                p(x + cos(a) * radius * 1.65f, y + sin(a) * radius * 1.65f),
                strokeWidth = pen.lw * .85f, cap = StrokeCap.Round)
        }
        drawCircle(color, radius * h, p(x, y))
    }
    when (look.pattern) {
        7 -> {
            drawRect(accent.copy(alpha = .8f), p(-.2f, -.50f), Size(.09f * h, .28f * h))
            drawRect(accent.copy(alpha = .7f), p(.08f, -.31f), Size(.12f * h, .10f * h))
            sun(.045f, -.399f, .039f, argb(Palette.cloth[2]))
            for ((x, y) in listOf(-.07f to -.33f, .085f to -.26f)) {
                for (k in 0..4) {
                    val a = k * Math.PI.toFloat() * .4f
                    drawCircle(Color(0xFFFFE5EC), .018f * h, p(x + cos(a) * .021f, y + sin(a) * .021f))
                }
                drawCircle(accent, .011f * h, p(x, y))
            }
        }
        8 -> for (k in -6..6) {
            drawLine(accent, p(k * .034f, -.5f), p(k * .034f, -.2f), strokeWidth = h * .008f)
            drawLine(accent.copy(alpha = .6f), p(k * .034f + .013f, -.5f), p(k * .034f + .013f, -.2f), strokeWidth = h * .003f)
        }
        9 -> sun(0f, -.356f, .032f, accent.lighten(.72f))
    }
}

/** Button shirts and an open jacket; patterns sit underneath the seams and collar. */
internal fun DrawScope.portraitShirt(look: Look, color: Color, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    val jacket = look.top == 12
    val seam = color.darken(.3f)
    if (jacket) {
        for (s in floatArrayOf(-1f, 1f)) {
            val panel = Path().apply {
                moveTo(s * .08f * h, -.49f * h)
                lineTo(s * .19f * h, -.49f * h)
                lineTo(s * .2f * h, -.22f * h)
                lineTo(s * .066f * h, -.22f * h)
                quadraticTo(s * .083f * h, -.36f * h, s * .08f * h, -.49f * h)
                close()
            }
            inked(panel, color, pen)
            drawLine(color.lighten(.4f), p(s * .093f, -.42f), p(s * .084f, -.246f), strokeWidth = pen.lw * .6f)
            inkedRound(Rect(p(s * .126f - .027f, -.395f), p(s * .126f + .027f, -.34f)), h * .005f, color.darken(.05f), pen, shade = false)
            drawLine(seam, p(s * .151f, -.38f), p(s * .101f, -.38f), strokeWidth = pen.lw * .6f)
            for (k in 0..2) drawCircle(Color(0xFFF0DDB7), .005f * h, p(s * .078f, -.33f + k * .037f))
        }
    } else {
        // A glimpse of the tee through the open neck, then a short button placket.
        val neck = Path().apply {
            moveTo(-.06f * h, -.5f * h); lineTo(.06f * h, -.5f * h)
            lineTo(0f, -.398f * h); close()
        }
        inked(neck, Color(0xFFFFFCF4), pen, shade = false)
        drawLine(seam, p(0f, -.398f), p(0f, -.232f), strokeWidth = pen.lw * .65f)
        for (k in 0..3) inkedCircle(p(.013f, -.381f + k * .043f), h * .006f, color.lighten(.55f), pen, shade = false)
    }
    for (s in floatArrayOf(-1f, 1f)) {
        val collar = Path().apply {
            moveTo(s * .065f * h, -.491f * h)
            lineTo(s * .123f * h, -.463f * h)
            lineTo(s * .077f * h, -.41f * h)
            lineTo(s * .021f * h, -.461f * h); close()
        }
        inked(collar, color.lighten(.1f), pen, shade = false)
    }
    drawLine(seam, p(-.13f, -.238f), p(.13f, -.238f), strokeWidth = pen.lw * .5f)
}

internal fun DrawScope.trimmedBeard(look: Look, hair: Color, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    val beard = Path().apply {
        moveTo(-.235f * h, -.666f * h)
        cubicTo(-.18f * h, -.622f * h, -.167f * h, -.6f * h, -.123f * h, -.582f * h)
        cubicTo(-.092f * h, -.532f * h, .092f * h, -.532f * h, .123f * h, -.582f * h)
        cubicTo(.167f * h, -.6f * h, .18f * h, -.622f * h, .235f * h, -.666f * h)
        lineTo(.25f * h, -.45f * h); lineTo(-.25f * h, -.45f * h); close()
    }
    clipPath(folkFace(look.face, h)) {
        inked(beard, hair.darken(.2f), pen)
        for (s in floatArrayOf(-1f, 1f)) for (k in 0..3) {
            drawLine(hair.lighten(.42f), p(s * (.132f + k * .021f), -.512f - k * .02f),
                p(s * (.121f + k * .02f), -.494f - k * .022f), strokeWidth = pen.lw * .65f, cap = StrokeCap.Round)
        }
    }
    for (s in floatArrayOf(-1f, 1f)) {
        val moustache = Path().apply {
            moveTo(0f, -.626f * h)
            quadraticTo(s * .05f * h, -.642f * h, s * .085f * h, -.609f * h)
            quadraticTo(s * .047f * h, -.605f * h, 0f, -.617f * h); close()
        }
        drawPath(moustache, hair.darken(.28f))
    }
}
