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

/** Hair grows out from a fixed scalp. Fullness adds thickness; length moves only the free ends. */
internal fun DrawScope.folkHair(look: Look, back: Boolean, color: Color, c: Offset, r: Float, pen: Pen) {
    val style = look.hair
    if (style == 18) { tousledHair(look, back, color, c, r, pen); return }
    val fit = HairFit(look)
    val accent = argb(Palette.cloth[look.accent])
    fun p(x: Float, y: Float) = Offset(c.x + x * r * fit.headWidth, c.y + y * r * fit.headHeight)
    fun curl(x: Float, y: Float, radius: Float) {
        inkedCircle(p(x, y), radius * r, color, pen)
        drawArc(color.darken(0.25f), 30f, 230f, false, p(x - radius * 0.6f, y - radius * 0.6f), Size(radius * 1.2f * r, radius * 1.2f * r), style = pen.thin)
    }
    fun lock(side: Float, end: Float, waves: Boolean = false) {
        val path = Path().apply {
            moveTo(p(side * 0.86f, -0.65f).x, p(side * 0.86f, -0.65f).y)
            if (waves) {
                for (k in 0..5) {
                    val y = -0.65f + (end + 0.65f) * (k + 1) / 6f
                    quadraticTo(p(side * (fit.outerWidth + if (k % 2 == 0) 0.14f else -0.05f), y - 0.1f).x,
                        p(0f, y - 0.1f).y, p(side * fit.outerWidth, y).x, p(0f, y).y)
                }
            } else cubicTo(p(side * fit.outerWidth, -0.4f).x, p(0f, -0.4f).y,
                p(side * (fit.outerWidth + 0.08f), end - 0.25f).x, p(0f, end - 0.25f).y,
                p(side * fit.outerWidth, end).x, p(0f, end).y)
        }
        drawPath(path, Ink.line, style = Stroke(r * fit.lockWidth + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(path, color, style = Stroke(r * fit.lockWidth, cap = StrokeCap.Round))
    }
    if (style == 0) return // A bare head cannot grow a hairstyle by stretching a slider.
    if (back) {
        when (style) {
            1, 7, 13, 16 -> for (side in floatArrayOf(-1f, 1f)) lock(side, fit.shortEnd)
            2, 4, 8 -> {
                val end = if (style == 2) fit.end * 0.65f else fit.end
                val shell = Path().apply {
                    moveTo(p(-0.92f, -0.6f).x, p(-0.92f, -0.6f).y)
                    cubicTo(p(-fit.outerWidth, -1.1f).x, p(0f, -1.1f).y, p(-fit.outerWidth, end).x, p(0f, end).y, p(-1.02f, end).x, p(0f, end).y)
                    lineTo(p(1.02f, end).x, p(0f, end).y)
                    cubicTo(p(fit.outerWidth, end).x, p(0f, end).y, p(fit.outerWidth, -1.1f).x, p(0f, -1.1f).y, p(0.92f, -0.6f).x, p(0.92f, -0.6f).y)
                    quadraticTo(p(0f, -fit.crown * 1.5f).x, p(0f, -fit.crown * 1.5f).y, p(-0.92f, -0.6f).x, p(-0.92f, -0.6f).y); close()
                }
                inked(shell, color.darken(0.08f), pen)
                for (side in floatArrayOf(-1f, 1f)) drawLine(color.darken(0.25f), p(side * (fit.outerWidth - 0.08f), 0f), p(side * 1.02f, end - 0.12f), strokeWidth = pen.lw * 0.6f)
            }
            3, 11 -> {
                val width = if (style == 11) fit.outerWidth + 0.15f else fit.outerWidth
                val drop = if (style == 11) fit.end - 0.7f else fit.shortEnd
                inkedOval(Rect(p(-width, -fit.crown), p(width, fit.crown + drop)), color.darken(0.08f), pen)
                for (i in 0..12) {
                    val angle = (i * 24f + 130f) * Math.PI.toFloat() / 180f
                    val lower = sin(angle).coerceAtLeast(0f)
                    curl(cos(angle) * width, sin(angle) * fit.crown + lower * drop,
                        if (style == 11) fit.lockWidth + 0.08f else fit.lockWidth * 0.8f)
                }
            }
            5 -> for (side in floatArrayOf(-1f, 1f)) {
                lock(side, fit.end)
                for (k in 0..4) curl(side * (1.02f + k * 0.015f), -0.45f + (fit.end + 0.45f) * k / 4f, fit.lockWidth * 0.7f)
                inkedCircle(p(side * 1.08f, fit.end), r * 0.1f, accent, pen)
            }
            6, 12 -> for (side in if (style == 6) floatArrayOf(0f) else floatArrayOf(-1f, 1f)) {
                // The bun grows above its tied root, rather than moving the root away from the skull.
                val root = p(side * 0.72f, -0.92f)
                val width = r * (0.24f + fit.lockWidth)
                inkedOval(Rect(root.x - width, root.y - r * (fit.tuft + fit.crown - 1.04f), root.x + width, root.y + r * 0.08f), color, pen)
                drawArc(color.darken(0.25f), 200f, 210f, false, Offset(root.x - width * 0.65f, root.y - r * fit.tuft * 0.85f), Size(width * 1.3f, r * fit.tuft * 0.7f), style = pen.thin)
            }
            9 -> {
                val root = p(0.68f, -0.92f)
                val tail = Path().apply {
                    moveTo(root.x, root.y)
                    cubicTo(p(1.75f + fit.lockWidth, -1.25f).x, p(0f, -1.25f).y, p(1.7f + fit.lockWidth, fit.end).x, p(0f, fit.end).y, p(1.1f, fit.end).x, p(0f, fit.end).y)
                    quadraticTo(p(1.3f, -0.2f).x, p(1.3f, -0.2f).y, root.x, root.y); close()
                }
                inked(tail, color.darken(0.1f), pen)
                inkedCircle(root, r * 0.12f, accent, pen)
            }
            10, 15, 17 -> for (side in floatArrayOf(-1f, 1f)) {
                lock(side, fit.end, waves = style == 15)
                if (style == 17) for (k in 0..4) curl(side * (fit.outerWidth + k % 2 * 0.1f), -0.45f + (fit.end + 0.45f) * k / 4f, fit.lockWidth * 0.8f)
                if (style != 15) inkedCircle(p(side * 0.9f, -0.5f), r * 0.11f, accent, pen)
            }
            14 -> for (i in -4..4) {
                val x = i * 0.25f
                val path = Path().apply {
                    moveTo(p(x * 0.8f, -0.8f).x, p(0f, -0.8f).y)
                    cubicTo(p(x + 0.2f, 0f).x, p(0f, 0f).y, p(x - 0.15f, fit.end - 0.3f).x, p(0f, fit.end - 0.3f).y, p(x, fit.end).x, p(0f, fit.end).y)
                }
                val width = r * fit.lockWidth * 0.65f
                drawPath(path, Ink.line, style = Stroke(width + pen.lw * 2f, cap = StrokeCap.Round))
                drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round))
                drawCircle(accent, width * 0.55f, p(x, fit.end - 0.12f))
            }
        }
        return
    }
    // Fixed temples and forehead: a smaller hair setting never exposes a floating scalp gap.
    val cap = Path().apply {
        moveTo(p(-0.98f, -0.15f).x, p(-0.98f, -0.15f).y)
        cubicTo(p(-fit.outerWidth, -fit.crown * 0.7f).x, p(0f, -fit.crown * 0.7f).y,
            p(-0.65f, -fit.crown).x, p(0f, -fit.crown).y,
            p(0f, -fit.crown).x, p(0f, -fit.crown).y)
        cubicTo(p(0.65f, -fit.crown).x, p(0f, -fit.crown).y,
            p(fit.outerWidth, -fit.crown * 0.7f).x, p(0f, -fit.crown * 0.7f).y,
            p(0.98f, -0.15f).x, p(0.98f, -0.15f).y)
        lineTo(p(0.83f, fit.fringe + 0.13f).x, p(0f, fit.fringe + 0.13f).y)
        if (style == 2 || style == 8) lineTo(p(-0.83f, fit.fringe + 0.13f).x, p(0f, fit.fringe + 0.13f).y)
        else {
            quadraticTo(p(0.35f, fit.fringe - 0.15f).x, p(0f, fit.fringe - 0.15f).y, p(0f, fit.fringe).x, p(0f, fit.fringe).y)
            quadraticTo(p(-0.55f, fit.fringe - 0.15f).x, p(0f, fit.fringe - 0.15f).y, p(-0.83f, fit.fringe + 0.13f).x, p(0f, fit.fringe + 0.13f).y)
        }
        close()
    }
    inked(cap, color, pen)
    when (style) {
        3, 11, 12, 17 -> for (i in -3..3) curl(i * 0.27f, -0.88f + abs(i) * 0.035f, fit.lockWidth * 0.7f)
        7, 13, 16 -> {
            val crest = Path().apply {
                moveTo(p(-0.78f, -0.65f).x, p(-0.78f, -0.65f).y)
                if (style == 13) {
                    cubicTo(p(-0.9f, -1.0f - fit.tuft).x, p(0f, -1.0f - fit.tuft).y, p(0.9f, -1.1f - fit.tuft).x, p(0f, -1.1f - fit.tuft).y, p(0.9f, -0.8f).x, p(0f, -0.8f).y)
                } else for (i in 0..8) {
                    val x = if (style == 16) -0.25f + i * 0.0625f else -0.8f + i * 0.2f
                    val y = -fit.crown + if (i % 2 == 0) 0.05f else -fit.tuft
                    lineTo(p(x, y).x, p(x, y).y)
                }
                lineTo(p(0.78f, -0.65f).x, p(0.78f, -0.65f).y)
                quadraticTo(p(0f, -0.9f).x, p(0f, -0.9f).y, p(-0.78f, -0.65f).x, p(-0.78f, -0.65f).y); close()
            }
            inked(crest, if (style == 16) accent else color, pen)
        }
        else -> for (i in -2..2) drawLine(color.darken(0.2f), p(i * 0.25f, -fit.crown + 0.03f), p(i * 0.3f + 0.08f, fit.fringe - 0.02f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
    }
    drawArc(color.lighten(0.45f), 220f, 35f, false, p(-0.95f, -fit.crown), Size(r * 1.4f, r), style = pen.thin)
}

internal fun DrawScope.folkPattern(look: Look, h: Float, pen: Pen) {
    val color = argb(Palette.cloth[look.accent])
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    when (look.pattern) {
        6 -> {
            drawRect(color, p(0f, -.50f), Size(.30f * h, .19f * h))
            drawRect(color, p(-.30f, -.31f), Size(.30f * h, .27f * h))
        }
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
