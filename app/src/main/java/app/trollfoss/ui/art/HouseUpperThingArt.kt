package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.cos
import kotlin.math.sin

/*
 * Things of the upper floor, drawn into the box (-w/2, -h) .. (w/2, 0): a toy block, an odd sock, a paintbrush, a fluffy
 * animal slipper that makes a fine hat, and a paper plane.
 */

private val blockPalette = listOf(Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF5AA9F5), Color(0xFFB983FF), Color(0xFFFF9F43))

/** A cube with a picture on its front: a star, a circle, a triangle, a heart, a moon or a sun. */
internal fun DrawScope.thUpBlock(variant: Int, w: Float, h: Float, pen: Pen) {
    val k = variant.mod(6)
    val col = blockPalette[k]
    val s = min2(w, h)
    val depth = s * 0.28f
    val front = Rect(-s * 0.5f, -s * 0.82f, s * 0.28f, -s * 0.04f)
    box3d(front, depth, col, pen, radius = s * 0.07f)
    val c = front.center
    val r = s * 0.28f
    val white = Color.White.copy(alpha = 0.92f)
    when (k) {
        0 -> drawPath(starPath(c, r, r * 0.45f), white)
        1 -> drawCircle(white, r * 0.8f, c)
        2 -> drawPath(Path().apply { poly(c.x - r * 0.9f, c.y + r * 0.7f, c.x + r * 0.9f, c.y + r * 0.7f, c.x, c.y - r * 0.95f) }, white)
        3 -> drawPath(fxHeart(c.x, c.y, r * 0.75f), white)
        4 -> { drawCircle(white, r * 0.8f, c); drawCircle(col, r * 0.7f, Offset(c.x + r * 0.4f, c.y - r * 0.15f)) }
        else -> { drawCircle(white, r * 0.55f, c); for (i in 0 until 8) { val a = i * 0.7854f; drawLine(white, Offset(c.x + cos(a) * r * 0.75f, c.y + sin(a) * r * 0.75f), Offset(c.x + cos(a) * r * 1.05f, c.y + sin(a) * r * 1.05f), strokeWidth = r * 0.22f, cap = StrokeCap.Round) } }
    }
}

private fun min2(a: Float, b: Float) = if (a < b) a else b

/** An odd sock: a stripy tube with a heel and a toe in another colour. */
internal fun DrawScope.thUpSock(variant: Int, w: Float, h: Float, pen: Pen) {
    val k = variant.mod(6)
    val main = listOf(Color(0xFFF08CB8), Color(0xFF5AA9F5), Color(0xFF6BCB77), Color(0xFFFFD447), Color(0xFFB983FF), Color(0xFFFF9F43))[k]
    val accent = listOf(Color.White, Color(0xFFFFE066), Color.White, Color(0xFFFF6B6B), Color(0xFFFFE066), Color(0xFF5AA9F5))[k]
    val sock = Path().apply {
        moveTo(-w * 0.34f, -h * 0.98f)
        lineTo(w * 0.04f, -h * 0.98f)
        lineTo(w * 0.07f, -h * 0.42f)
        quadraticTo(w * 0.5f, -h * 0.4f, w * 0.5f, -h * 0.15f)
        quadraticTo(w * 0.46f, 0f, w * 0.24f, 0f)
        lineTo(-w * 0.28f, 0f)
        quadraticTo(-w * 0.4f, 0f, -w * 0.38f, -h * 0.2f)
        close()
    }
    inked(sock, main, pen)
    clipPath(sock) {
        drawRect(accent, Offset(-w, -h), androidx.compose.ui.geometry.Size(w * 2f, h * 0.2f))
        drawLine(accent, Offset(-w * 0.4f, -h * 0.62f), Offset(w * 0.1f, -h * 0.62f), strokeWidth = h * 0.1f)
        drawLine(accent, Offset(-w * 0.4f, -h * 0.4f), Offset(w * 0.1f, -h * 0.4f), strokeWidth = h * 0.1f)
        drawCircle(accent, h * 0.2f, Offset(w * 0.34f, -h * 0.12f))
        drawCircle(accent, h * 0.2f, Offset(-w * 0.26f, -h * 0.1f))
    }
    drawPath(sock, Ink.line, style = pen.stroke)
}

/** A paintbrush with a wooden handle, a silver ferrule and bristles dipped in the colour of the variant. */
internal fun DrawScope.thUpBrush(variant: Int, w: Float, h: Float, pen: Pen) {
    val col = listOf(Color(0xFFFF6B6B), Color(0xFF5AA9F5), Color(0xFFFFD447), Color(0xFF6BCB77))[variant.mod(4)]
    val cx = 0f
    capsule(Offset(cx, -h * 0.02f), Offset(cx, -h * 0.62f), w * 0.55f, UpC.wood, pen)
    capsule(Offset(cx, -h * 0.58f), Offset(cx, -h * 0.74f), w * 0.7f, UpC.steel, pen)
    val tip = Path().apply {
        moveTo(cx - w * 0.5f, -h * 0.74f)
        quadraticTo(cx - w * 0.7f, -h * 0.9f, cx, -h)
        quadraticTo(cx + w * 0.7f, -h * 0.9f, cx + w * 0.5f, -h * 0.74f)
        close()
    }
    inked(tip, col, pen, shade = false)
}

/** A fluffy animal slipper: 0 a white bunny with long ears, 1 a green frog with bulging eyes, 2 a blue dinosaur with orange plates. */
internal fun DrawScope.thUpSlipper(variant: Int, w: Float, h: Float, pen: Pen) {
    val k = variant.mod(3)
    val body = listOf(Color(0xFFF7F0F7), Color(0xFF6BCB77), Color(0xFF5AA9F5))[k]
    val shoe = Path().apply {
        moveTo(-w * 0.46f, -h * 0.05f)
        quadraticTo(-w * 0.5f, -h * 0.55f, -w * 0.1f, -h * 0.6f)
        quadraticTo(w * 0.34f, -h * 0.62f, w * 0.48f, -h * 0.28f)
        quadraticTo(w * 0.52f, -h * 0.02f, w * 0.3f, 0f)
        lineTo(-w * 0.4f, 0f)
        close()
    }
    inked(shoe, body, pen)
    // A fluffy cuff round the opening.
    for (i in 0 until 6) drawCircle(body.lighten(0.25f), h * 0.11f, Offset(-w * 0.34f + i * w * 0.1f, -h * 0.58f + (i % 2) * h * 0.02f))
    drawLine(body.darken(0.25f), Offset(-w * 0.4f, -h * 0.05f), Offset(w * 0.4f, -h * 0.05f), strokeWidth = pen.lw)
    when (k) {
        0 -> {
            for (s in listOf(-0.3f, -0.12f)) {
                val ear = Path().apply {
                    moveTo(w * s, -h * 0.58f); quadraticTo(w * (s - 0.1f), -h * 1.0f, w * (s + 0.03f), -h * 0.98f); quadraticTo(w * (s + 0.12f), -h * 0.9f, w * (s + 0.1f), -h * 0.58f); close()
                }
                inked(ear, body, pen, shade = false)
                drawPath(Path().apply { moveTo(w * (s + 0.02f), -h * 0.62f); quadraticTo(w * (s - 0.02f), -h * 0.88f, w * (s + 0.04f), -h * 0.9f); quadraticTo(w * (s + 0.08f), -h * 0.8f, w * (s + 0.07f), -h * 0.62f); close() }, Color(0xFFFFB6CE))
            }
            drawCircle(Color(0xFFFF8FB1), h * 0.08f, Offset(w * 0.4f, -h * 0.3f))
            drawCircle(Ink.line, h * 0.04f, Offset(w * 0.3f, -h * 0.4f))
        }
        1 -> {
            for (s in listOf(-0.28f, -0.06f)) {
                inkedCircle(Offset(w * s, -h * 0.72f), h * 0.16f, body, pen, shade = false)
                drawCircle(Color.White, h * 0.1f, Offset(w * s, -h * 0.72f))
                drawCircle(Ink.line, h * 0.05f, Offset(w * s + h * 0.02f, -h * 0.72f))
            }
            drawArc(Ink.line, 20f, 140f, false, Offset(w * 0.14f, -h * 0.38f), androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.2f), style = pen.thin)
        }
        else -> {
            for (i in 0 until 3) drawPath(Path().apply { val x = -w * 0.3f + i * w * 0.14f; poly(x, -h * 0.58f, x + w * 0.07f, -h * 0.82f, x + w * 0.14f, -h * 0.6f) }, Color(0xFFFF9F43))
            for (i in 0 until 3) drawPath(Path().apply { val x = -w * 0.3f + i * w * 0.14f; poly(x, -h * 0.58f, x + w * 0.07f, -h * 0.82f, x + w * 0.14f, -h * 0.6f) }, Ink.line, style = pen.thin)
            drawCircle(Color.White, h * 0.07f, Offset(w * 0.3f, -h * 0.4f))
            drawCircle(Ink.line, h * 0.035f, Offset(w * 0.31f, -h * 0.4f))
            drawLine(Ink.line, Offset(w * 0.2f, -h * 0.2f), Offset(w * 0.46f, -h * 0.24f), strokeWidth = pen.lw, cap = StrokeCap.Round)
        }
    }
}

/** A folded paper plane in a pastel colour, seen from the side with its fold lines. */
internal fun DrawScope.thUpPlane(variant: Int, w: Float, h: Float, pen: Pen) {
    val col = listOf(Color(0xFFFFFBF2), Color(0xFFBFE3FA), Color(0xFFFFE08A))[variant.mod(3)]
    val wing = Path().apply { poly(-w * 0.5f, -h * 0.9f, w * 0.5f, -h * 0.35f, -w * 0.12f, -h * 0.28f) }
    inked(wing, col, pen)
    val under = Path().apply { poly(-w * 0.5f, -h * 0.9f, w * 0.5f, -h * 0.35f, -w * 0.18f, -h * 0.02f, -w * 0.12f, -h * 0.28f) }
    inked(under, col.darken(0.08f), pen, shade = false)
    drawLine(Ink.line.copy(alpha = 0.5f), Offset(-w * 0.5f, -h * 0.9f), Offset(-w * 0.12f, -h * 0.28f), strokeWidth = pen.lw * 0.6f)
    drawLine(Ink.line.copy(alpha = 0.4f), Offset(w * 0.1f, -h * 0.5f), Offset(-w * 0.18f, -h * 0.02f), strokeWidth = pen.lw * 0.6f)
}
