package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Extra silhouettes share the original head and attachment points in every pose. */
internal fun DrawScope.newHair(style: Int, back: Boolean, color: Color, accent: Color, c: Offset, r: Float, pen: Pen, length: Float) {
    fun p(x: Float, y: Float) = Offset(c.x + x * r, c.y + y * r)
    fun curl(x: Float, y: Float, radius: Float) {
        inkedCircle(p(x, y), radius * r, color, pen)
        drawArc(color.darken(0.25f), 30f, 230f, false, p(x - radius * 0.6f, y - radius * 0.6f), Size(radius * 1.2f * r, radius * 1.2f * r), style = pen.thin)
    }
    if (back) {
        when (style) {
            9 -> { // High ponytail, with a curling end.
                val tail = Path().apply {
                    moveTo(p(0.45f, -0.95f).x, p(0.45f, -0.95f).y)
                    cubicTo(p(1.7f, -1.5f).x, p(1.7f, -1.5f).y, p(1.7f, length).x, p(1.7f, length).y, p(0.9f, length).x, p(0.9f, length).y)
                    quadraticTo(p(1.25f, 0f).x, p(1.25f, 0f).y, p(0.45f, -0.95f).x, p(0.45f, -0.95f).y); close()
                }
                inked(tail, color.darken(0.1f), pen)
                inkedCircle(p(0.8f, -0.85f), r * 0.12f, accent, pen)
            }
            10 -> for (side in floatArrayOf(-1f, 1f)) {
                inkedOval(Rect(p(side * 1.1f - 0.4f, -0.8f), p(side * 1.1f + 0.4f, 0.7f * length)), color, pen)
                inkedCircle(p(side * 0.95f, -0.6f), r * 0.12f, accent, pen)
            }
            11 -> for (i in 0..12) {
                val angle = (i * 24f + 130f) * Math.PI.toFloat() / 180f
                curl(cos(angle) * 1.12f, sin(angle) * 1.12f, 0.35f)
            }
            12 -> for (side in floatArrayOf(-1f, 1f)) curl(side * 0.8f, -1.0f, 0.46f)
            14 -> for (i in -4..4) {
                val x = i * 0.25f
                val path = Path().apply {
                    moveTo(p(x, -0.8f).x, p(x, -0.8f).y)
                    cubicTo(p(x + 0.35f, 0.1f).x, p(x + 0.35f, 0.1f).y, p(x - 0.2f, length).x, p(x - 0.2f, length).y, p(x, length * 1.25f).x, p(x, length * 1.25f).y)
                }
                drawPath(path, Ink.line, style = Stroke(r * 0.21f + pen.lw * 2f, cap = StrokeCap.Round))
                drawPath(path, color, style = Stroke(r * 0.21f, cap = StrokeCap.Round))
                drawCircle(accent, r * 0.11f, p(x, length * 1.05f))
            }
            15 -> for (side in floatArrayOf(-1f, 1f)) {
                val path = Path().apply {
                    moveTo(p(side * 0.8f, -0.8f).x, p(side * 0.8f, -0.8f).y)
                    for (k in 0..5) {
                        val y = k * 0.25f * length
                        quadraticTo(p(side * (1.35f + k % 2 * 0.2f), y).x, p(side * (1.35f + k % 2 * 0.2f), y).y, p(side, y + 0.13f).x, p(side, y + 0.13f).y)
                    }
                }
                drawPath(path, Ink.line, style = Stroke(r * 0.45f + pen.lw * 2f, cap = StrokeCap.Round))
                drawPath(path, color, style = Stroke(r * 0.45f, cap = StrokeCap.Round))
            }
            17 -> for (side in floatArrayOf(-1f, 1f)) {
                for (k in 0..3) curl(side * (1f + k * 0.18f), -0.45f + k * 0.3f * length, 0.25f)
                inkedCircle(p(side * 0.92f, -0.4f), r * 0.11f, accent, pen)
            }
        }
        return
    }
    val cap = Path().apply {
        arcTo(Rect(p(-1.08f, -1.08f), p(1.08f, 1.08f)), 180f, 180f, true)
        quadraticTo(p(0.6f, -0.7f).x, p(0.6f, -0.7f).y, p(0.1f, -0.77f).x, p(0.1f, -0.77f).y)
        quadraticTo(p(-0.55f, -0.72f).x, p(-0.55f, -0.72f).y, p(-1.08f, -0.12f).x, p(-1.08f, -0.12f).y)
        close()
    }
    inked(cap, color, pen)
    when (style) {
        11, 12, 17 -> for (i in -3..3) curl(i * 0.27f, -0.9f + abs(i) * 0.055f, 0.23f)
        13 -> { // Swept quiff.
            val quiff = Path().apply {
                moveTo(p(-0.85f, -0.4f).x, p(-0.85f, -0.4f).y)
                cubicTo(p(-1.1f, -1.8f).x, p(-1.1f, -1.8f).y, p(0.95f, -1.7f).x, p(0.95f, -1.7f).y, p(1.05f, -0.85f).x, p(1.05f, -0.85f).y)
                quadraticTo(p(0.6f, -1.15f).x, p(0.6f, -1.15f).y, p(-0.85f, -0.4f).x, p(-0.85f, -0.4f).y); close()
            }
            inked(quiff, color, pen)
        }
        16 -> { // A bright, soft mohawk.
            val crest = Path().apply {
                moveTo(p(-0.3f, -0.82f).x, p(-0.3f, -0.82f).y)
                for (i in 0..5) lineTo(p(-0.3f + i * 0.12f, if (i % 2 == 0) -1.1f else -1.75f).x, p(-0.3f + i * 0.12f, if (i % 2 == 0) -1.1f else -1.75f).y)
                lineTo(p(0.3f, -0.82f).x, p(0.3f, -0.82f).y); close()
            }
            inked(crest, accent, pen)
        }
        else -> for (i in -2..2) drawLine(color.darken(0.2f), p(i * 0.25f, -1.01f), p(i * 0.32f + 0.15f, -0.8f), strokeWidth = pen.lw * 0.65f, cap = StrokeCap.Round)
    }
    drawArc(color.lighten(0.45f), 220f, 35f, false, p(-0.95f, -1.03f), Size(r * 1.4f, r), style = pen.thin)
}

internal fun DrawScope.folkPattern(look: Look, h: Float, pen: Pen) {
    val color = argb(Palette.cloth[look.accent])
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    when (look.pattern) {
        1 -> for (y in 0..4) drawLine(color, p(-0.3f, -0.42f + y * 0.055f), p(0.3f, -0.42f + y * 0.055f), strokeWidth = h * 0.018f)
        2 -> for (row in 0..3) for (col in -2..2) drawCircle(color, h * 0.012f, p(col * 0.07f + row % 2 * 0.035f, -0.43f + row * 0.07f))
        3 -> { twinkle(p(0f, -0.32f), h * 0.09f, color, 1f) }
        4 -> {
            val heart = Path().apply {
                moveTo(0f, -0.23f * h)
                cubicTo(-0.15f * h, -0.33f * h, -0.08f * h, -0.41f * h, 0f, -0.35f * h)
                cubicTo(0.08f * h, -0.41f * h, 0.15f * h, -0.33f * h, 0f, -0.23f * h); close()
            }
            inked(heart, color, pen, shade = false)
        }
        5 -> { // A tiny monster pocket.
            inkedRound(Rect(p(-0.085f, -0.39f), p(0.085f, -0.25f)), h * 0.03f, color, pen)
            for (s in floatArrayOf(-1f, 1f)) { drawCircle(Color.White, h * 0.023f, p(s * 0.035f, -0.345f)); drawCircle(Ink.line, h * 0.01f, p(s * 0.035f, -0.34f)) }
            drawArc(Ink.line, 0f, 180f, false, p(-0.04f, -0.31f), Size(h * 0.08f, h * 0.04f), style = pen.thin)
        }
    }
}
