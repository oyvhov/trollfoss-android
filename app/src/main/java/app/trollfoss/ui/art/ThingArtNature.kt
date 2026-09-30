package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

// ------------------------------------------------------------------ nature

internal fun DrawScope.thShell(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val peach = Color(0xFFFFB59A)
    val c = o(0f, -0.14f)
    val rx = 0.48f * w
    val ry = 0.84f * h
    val n = 7
    val fan = Path()
    fan.moveTo(c.x - 0.1f * w, c.y + 0.02f * h)
    for (i in 0..n) {
        val a = Math.PI + Math.PI * i / n
        val x = c.x + rx * cos(a).toFloat()
        val y = c.y + ry * sin(a).toFloat()
        if (i == 0) {
            fan.lineTo(x, y)
        } else {
            val m = Math.PI + Math.PI * (i - 0.5) / n
            fan.quadraticTo(c.x + rx * 1.12f * cos(m).toFloat(), c.y + ry * 1.1f * sin(m).toFloat(), x, y)
        }
    }
    fan.lineTo(c.x + 0.1f * w, c.y + 0.02f * h)
    fan.close()
    inked(fan, peach, pen)
    clipPath(fan) {
        for (i in 1 until n) {
            val a = Math.PI + Math.PI * i / n
            drawLine(peach.darken(0.3f), c, Offset(c.x + rx * cos(a).toFloat(), c.y + ry * sin(a).toFloat()), pen.lw * 0.5f)
        }
        drawOval(Color.White.copy(alpha = 0.35f), Offset(c.x - rx * 0.7f, c.y - ry * 0.7f), Size(rx * 1.4f, ry * 1.4f), style = Stroke(h * 0.06f))
        drawOval(Color.White.copy(alpha = 0.25f), Offset(c.x - rx * 0.4f, c.y - ry * 0.4f), Size(rx * 0.8f, ry * 0.8f), style = Stroke(h * 0.05f))
    }
    drawPath(fan, Ink.line, style = pen.stroke)
    val ears = thSketch(w, h) { m(-0.22f, -0.24f); l(0.22f, -0.24f); l(0.14f, -0.02f); l(-0.14f, -0.02f); z() }
    inked(ears, peach.darken(0.1f), pen, shade = false)
    shine(o(-0.2f, -0.62f), w * 0.1f, h * 0.1f, 0.6f)
}

private val STAR5_COS = FloatArray(10) { cos(Math.toRadians(-90.0 + it * 36.0)).toFloat() }
private val STAR5_SIN = FloatArray(10) { sin(Math.toRadians(-90.0 + it * 36.0)).toFloat() }

internal fun DrawScope.thStarfish(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -0.47f * h)
    val pts = FloatArray(20)
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) 0.6f * w else 0.24f * w
        pts[i * 2] = c.x + r * STAR5_COS[i]
        pts[i * 2 + 1] = c.y + r * STAR5_SIN[i]
    }
    val orange = Color(0xFFFF8A5B)
    val star = blobPath(*pts)
    inked(star, orange, pen)
    val bump = Color(0xFFFFC7A8)
    for (i in 0 until 5) {
        val cs = STAR5_COS[i * 2]
        val sn = STAR5_SIN[i * 2]
        for (k in 1..2) drawCircle(bump, w * 0.028f, Offset(c.x + cs * w * 0.14f * k, c.y + sn * w * 0.14f * k))
    }
    drawCircle(orange.darken(0.2f), w * 0.05f, c)
    drawCircle(bump, w * 0.02f, c)
    shine(Offset(c.x - w * 0.12f, c.y - w * 0.18f), w * 0.07f, h * 0.05f, 0.6f)
}

/** 0 bluebell, 1 wood anemone, 2 buttercup, 3 red rose, 4 lupin. */
internal fun DrawScope.thFlower(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val stemCol = Color(0xFF3F9E47)
    val leafCol = Color(0xFF5DBB4A)
    when (v.mod(5)) {
        0 -> {
            val stem = thSketch(w, h) { m(0f, -0.01f); q(-0.06f, -0.5f, -0.02f, -0.86f); q(0.06f, -0.98f, 0.22f, -0.86f) }
            drawPath(stem, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(stem, stemCol, style = Stroke(w * 0.1f, cap = StrokeCap.Round))
            val side = thSketch(w, h) { m(-0.03f, -0.66f); q(-0.2f, -0.72f, -0.24f, -0.62f) }
            drawPath(side, Ink.line, style = Stroke(w * 0.07f + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(side, stemCol, style = Stroke(w * 0.07f, cap = StrokeCap.Round))
            inked(thLens(o(0f, -0.1f), o(-0.34f, -0.4f), w * 0.07f), leafCol, pen, shade = false)
            bell(o(0.24f, -0.86f), w * 0.5f, h * 0.2f, pen)
            bell(o(-0.24f, -0.62f), w * 0.4f, h * 0.16f, pen)
        }
        1 -> {
            val stem = thSketch(w, h) { m(0f, -0.01f); q(0.06f, -0.4f, 0f, -0.78f) }
            drawPath(stem, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(stem, stemCol, style = Stroke(w * 0.1f, cap = StrokeCap.Round))
            for (s in SIDES) {
                val leaf = thSketch(w, h) { m(0.02f, -0.5f); l(s * 0.2f, -0.6f); l(s * 0.26f, -0.52f); l(s * 0.4f, -0.56f); l(s * 0.34f, -0.46f); l(s * 0.46f, -0.4f); l(s * 0.24f, -0.42f); z() }
                inked(leaf, leafCol, pen, shade = false)
            }
            anemone(o(0f, -0.83f), w * 0.5f, pen)
        }
        2 -> {
            val stem = thSketch(w, h) { m(0f, -0.01f); q(-0.08f, -0.4f, 0f, -0.72f) }
            drawPath(stem, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(stem, stemCol, style = Stroke(w * 0.1f, cap = StrokeCap.Round))
            inked(thLens(o(-0.02f, -0.3f), o(0.36f, -0.46f), w * 0.08f), leafCol, pen, shade = false)
            val c = o(0f, -0.8f)
            val r = w * 0.48f
            val petals = Path()
            for (i in 0 until 5) {
                val a = Math.toRadians(-90.0 + i * 72.0)
                val px = c.x + r * 0.5f * cos(a).toFloat()
                val py = c.y + r * 0.5f * sin(a).toFloat()
                petals.addOval(Rect(px - r * 0.5f, py - r * 0.5f, px + r * 0.5f, py + r * 0.5f))
            }
            thUnion(petals, Color(0xFFFFD23F), pen)
            for (i in 0 until 5) {
                val a = Math.toRadians(-90.0 + i * 72.0)
                drawCircle(Color.White.copy(alpha = 0.7f), r * 0.1f, Offset(c.x + r * 0.62f * cos(a).toFloat() - r * 0.08f, c.y + r * 0.62f * sin(a).toFloat() - r * 0.1f))
            }
            drawCircle(Color(0xFFB8D94A), r * 0.28f, c)
            drawCircle(Ink.line, r * 0.28f, c, style = pen.thin)
            for (k in 0 until 5) {
                val a = Math.toRadians(k * 72.0)
                drawCircle(Color(0xFFE09A1E), r * 0.06f, Offset(c.x + r * 0.16f * cos(a).toFloat(), c.y + r * 0.16f * sin(a).toFloat()))
            }
        }
        3 -> {
            val stem = thSketch(w, h) { m(0f, -0.01f); q(0.08f, -0.4f, 0f, -0.68f) }
            drawPath(stem, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(stem, stemCol, style = Stroke(w * 0.1f, cap = StrokeCap.Round))
            for (k in 0 until 2) {
                val y = -0.2f - k * 0.2f
                val s = if (k == 0) 1f else -1f
                thFill(thSketch(w, h) { m(s * 0.03f, y); l(s * 0.14f, y - 0.03f); l(s * 0.03f, y - 0.06f); z() }, stemCol.darken(0.2f), pen)
            }
            inked(thLens(o(0.02f, -0.46f), o(-0.36f, -0.56f), w * 0.09f), leafCol, pen, shade = false)
            for (s in SIDES) inked(thLens(o(0f, -0.7f), o(s * 0.3f, -0.66f), w * 0.07f), stemCol, pen, shade = false)
            val c = o(0f, -0.83f)
            val r = w * 0.44f
            inkedCircle(c, r, Color(0xFFE8415A), pen)
            val swirl = Color(0xFFB02A40)
            drawArc(swirl, 180f, 250f, false, Offset(c.x - r * 0.62f, c.y - r * 0.62f), Size(r * 1.24f, r * 1.24f), style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
            drawArc(swirl, 20f, 250f, false, Offset(c.x - r * 0.34f, c.y - r * 0.34f), Size(r * 0.68f, r * 0.68f), style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
            drawArc(swirl, 250f, 200f, false, Offset(c.x - r * 0.12f, c.y - r * 0.12f), Size(r * 0.24f, r * 0.24f), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
            shine(Offset(c.x - r * 0.45f, c.y - r * 0.45f), r * 0.3f, r * 0.2f, 0.6f)
        }
        else -> {
            val stem = thSketch(w, h) { m(0f, -0.01f); q(0.04f, -0.3f, 0f, -0.5f) }
            drawPath(stem, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(stem, stemCol, style = Stroke(w * 0.1f, cap = StrokeCap.Round))
            val palm = o(0.02f, -0.3f)
            for (k in 0 until 4) {
                val a = Math.toRadians(-165.0 + k * 50.0)
                inked(thLens(palm, Offset(palm.x + (w * 0.5f * cos(a)).toFloat(), palm.y + (h * 0.14f * sin(a)).toFloat()), w * 0.09f), leafCol, pen, shade = false)
            }
            val purple = Color(0xFF8B5CF6)
            for (k in 0 until 9) {
                val t = k / 8f
                val y = -0.46f - t * 0.5f
                val side = if (k % 2 == 0) -1f else 1f
                val r = w * (0.2f - t * 0.1f)
                val c = o(side * (0.16f - t * 0.12f), y)
                inkedOval(Rect(c.x - r, c.y - r * 0.8f, c.x + r, c.y + r * 0.8f), if (k % 3 == 0) purple.lighten(0.25f) else purple, pen, shade = false)
            }
            thDot(o(0f, -0.97f), w * 0.07f, purple.lighten(0.35f), pen)
        }
    }
}

/** A hanging bluebell flower with five flared points, top at [top]. */
private fun DrawScope.bell(top: Offset, bw: Float, bh: Float, pen: Pen) {
    val blue = Color(0xFF6A7FE8)
    val p = Path().apply {
        moveTo(top.x - bw * 0.16f, top.y)
        cubicTo(top.x - bw * 0.3f, top.y + bh * 0.3f, top.x - bw * 0.4f, top.y + bh * 0.62f, top.x - bw * 0.5f, top.y + bh)
        lineTo(top.x - bw * 0.3f, top.y + bh * 0.88f)
        lineTo(top.x - bw * 0.16f, top.y + bh * 1.02f)
        lineTo(top.x, top.y + bh * 0.88f)
        lineTo(top.x + bw * 0.16f, top.y + bh * 1.02f)
        lineTo(top.x + bw * 0.3f, top.y + bh * 0.88f)
        lineTo(top.x + bw * 0.5f, top.y + bh)
        cubicTo(top.x + bw * 0.4f, top.y + bh * 0.62f, top.x + bw * 0.3f, top.y + bh * 0.3f, top.x + bw * 0.16f, top.y)
        close()
    }
    inked(p, blue, pen)
    drawLine(blue.darken(0.3f), Offset(top.x, top.y + bh * 0.2f), Offset(top.x, top.y + bh * 0.8f), pen.lw * 0.45f, StrokeCap.Round)
    drawCircle(Color(0xFF3F9E47), bw * 0.14f, top)
}

private fun DrawScope.anemone(c: Offset, r: Float, pen: Pen) {
    val petals = Path()
    for (i in 0 until 6) {
        val a = Math.toRadians(-90.0 + i * 60.0)
        val px = c.x + r * 0.55f * cos(a).toFloat()
        val py = c.y + r * 0.55f * sin(a).toFloat()
        rotateOval(petals, px, py, r * 0.3f, r * 0.5f, a)
    }
    thUnion(petals, Color.White, pen)
    clipPath(petals) { drawCircle(Color(0x33FF6FA8), r * 1.2f, Offset(c.x + r * 0.6f, c.y + r * 0.6f)) }
    drawCircle(Color(0xFFD9E86A), r * 0.24f, c)
    for (k in 0 until 8) {
        val a = Math.toRadians(k * 45.0)
        drawCircle(Color(0xFFFFD23F), r * 0.06f, Offset(c.x + r * 0.3f * cos(a).toFloat(), c.y + r * 0.3f * sin(a).toFloat()))
    }
}

/** Adds an ellipse of radii [rx] (across) and [ry] (along [angle]) centred on ([cx], [cy]). */
private fun rotateOval(path: Path, cx: Float, cy: Float, rx: Float, ry: Float, angle: Double) {
    val ca = cos(angle).toFloat()
    val sa = sin(angle).toFloat()
    // Four-segment ellipse through its axis ends, turned by the angle.
    fun px(x: Float, y: Float) = cx + x * ca - y * sa
    fun py(x: Float, y: Float) = cy + x * sa + y * ca
    val k = 0.5523f
    path.moveTo(px(ry, 0f), py(ry, 0f))
    path.cubicTo(px(ry, rx * k), py(ry, rx * k), px(ry * k, rx), py(ry * k, rx), px(0f, rx), py(0f, rx))
    path.cubicTo(px(-ry * k, rx), py(-ry * k, rx), px(-ry, rx * k), py(-ry, rx * k), px(-ry, 0f), py(-ry, 0f))
    path.cubicTo(px(-ry, -rx * k), py(-ry, -rx * k), px(-ry * k, -rx), py(-ry * k, -rx), px(0f, -rx), py(0f, -rx))
    path.cubicTo(px(ry * k, -rx), py(ry * k, -rx), px(ry, -rx * k), py(ry, -rx * k), px(ry, 0f), py(ry, 0f))
    path.close()
}

private val FLY_DOTS = floatArrayOf(-0.24f, -0.74f, 0.04f, -0.86f, 0.26f, -0.72f, -0.06f, -0.66f, 0.34f, -0.58f, -0.36f, -0.58f)

internal fun DrawScope.thMushroom(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val stemCol = Color(0xFFFFF8EC)
    val stem = thSketch(w, h) { m(-0.14f, -0.54f); q(-0.16f, -0.3f, -0.22f, -0.14f); q(-0.24f, -0.02f, 0f, -0.02f); q(0.24f, -0.02f, 0.22f, -0.14f); q(0.16f, -0.3f, 0.14f, -0.54f); z() }
    inked(stem, stemCol, pen)
    val gills = Rect(-0.4f * w, -0.58f * h, 0.4f * w, -0.44f * h)
    drawOval(Color(0xFFF2E2C4), gills.topLeft, gills.size)
    clipPath(ovalPath(gills)) { for (k in -4..4) drawLine(Color(0xFFD9C29A), Offset(k * 0.08f * w, gills.bottom), Offset(k * 0.05f * w, gills.top), pen.lw * 0.4f) }
    drawOval(Ink.line, gills.topLeft, gills.size, style = pen.thin)
    val skirt = thSketch(w, h) { m(-0.17f, -0.42f); q(0f, -0.36f, 0.17f, -0.42f); l(0.2f, -0.34f); q(0f, -0.28f, -0.2f, -0.34f); z() }
    thFill(skirt, stemCol, pen)
    val red = Color(0xFFE53B35)
    val cap = thSketch(w, h) { m(-0.48f, -0.52f); c(-0.48f, -0.86f, -0.26f, -0.99f, 0f, -0.99f); c(0.26f, -0.99f, 0.48f, -0.86f, 0.48f, -0.52f); q(0f, -0.42f, -0.48f, -0.52f); z() }
    inked(cap, red, pen)
    for (k in 0 until 6) {
        val r = w * (0.045f + 0.02f * (k % 3))
        thDot(o(FLY_DOTS[k * 2], FLY_DOTS[k * 2 + 1]), r, Color.White, pen)
    }
    shine(o(-0.2f, -0.86f), w * 0.14f, h * 0.06f)
}

internal fun DrawScope.thPinecone(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    capsule(o(0.02f, -0.92f), o(0.1f, -0.99f), w * 0.1f, ThingInk.bark, pen)
    val base = Color(0xFF8C5A36)
    inkedOval(Rect(-0.44f * w, -0.94f * h, 0.44f * w, -0.02f * h), base, pen)
    val scale = Color(0xFFC98A55)
    val rows = PINE_ROWS
    for (r in rows.indices) {
        val y = -0.82f + r * 0.15f
        val count = rows[r]
        for (k in 0 until count) {
            val x = (k - (count - 1) / 2f) * 0.24f
            val sw = 0.24f
            val sh = 0.17f
            val p = thSketch(w, h) { m(x - sw / 2, y - sh * 0.3f); q(x, y - sh, x + sw / 2, y - sh * 0.3f); l(x, y + sh * 0.55f); z() }
            drawPath(p, scale)
            drawPath(p, Ink.line, style = Stroke(pen.lw * 0.5f, join = StrokeJoin.Round))
            drawCircle(base.darken(0.3f), w * 0.025f, o(x, y + sh * 0.3f))
        }
    }
    drawOval(Ink.line, Offset(-0.44f * w, -0.94f * h), Size(0.88f * w, 0.92f * h), style = pen.stroke)
}

private val PINE_ROWS = intArrayOf(2, 3, 3, 3, 2, 1)

internal fun DrawScope.thStick(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val bark = Color(0xFF8C5A36)
    val twig = thSketch(w, h) { m(0.12f, -0.56f); q(0.18f, -0.8f, 0.26f, -0.97f) }
    drawPath(twig, Ink.line, style = Stroke(h * 0.26f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(twig, bark, style = Stroke(h * 0.26f, cap = StrokeCap.Round))
    val main = thSketch(w, h) { m(-0.45f, -0.42f); q(0f, -0.7f, 0.45f, -0.5f) }
    drawPath(main, Ink.line, style = Stroke(h * 0.55f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(main, bark, style = Stroke(h * 0.55f, cap = StrokeCap.Round))
    drawPath(thSketch(w, h) { m(-0.4f, -0.56f); q(0f, -0.8f, 0.4f, -0.62f) }, bark.lighten(0.25f), style = Stroke(h * 0.12f, cap = StrokeCap.Round))
    val line = bark.darken(0.35f)
    drawLine(line, o(-0.3f, -0.44f), o(-0.2f, -0.48f), pen.lw * 0.45f, StrokeCap.Round)
    drawLine(line, o(0.02f, -0.5f), o(0.14f, -0.52f), pen.lw * 0.45f, StrokeCap.Round)
    drawLine(line, o(0.26f, -0.44f), o(0.34f, -0.44f), pen.lw * 0.45f, StrokeCap.Round)
    drawOval(line, o(-0.12f, -0.62f), Size(0.04f * w, 0.24f * h))
}

internal fun DrawScope.thRock(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val grey = Color(0xFF9DA3AE)
    val rock = blobPath(
        -0.48f * w, -0.2f * h, -0.4f * w, -0.66f * h, -0.12f * w, -0.96f * h, 0.24f * w, -0.92f * h,
        0.47f * w, -0.6f * h, 0.49f * w, -0.2f * h, 0.2f * w, -0.01f * h, -0.3f * w, -0.01f * h,
    )
    inked(rock, grey, pen, outline = false)
    clipPath(rock) {
        drawPath(thSketch(w, h) { m(-0.36f, -0.5f); l(-0.1f, -0.86f); l(0.1f, -0.72f); l(-0.12f, -0.44f); z() }, grey.lighten(0.2f))
        val moss = blobPath(0.02f * w, -0.86f * h, 0.24f * w, -1.02f * h, 0.5f * w, -0.8f * h, 0.36f * w, -0.66f * h, 0.12f * w, -0.72f * h)
        drawPath(moss, Color(0xFF6FAE5A))
        drawPath(moss, Ink.line, style = pen.thin)
        drawCircle(Color(0xFF8FCB6A), w * 0.02f, o(0.2f, -0.84f))
        drawCircle(Color(0xFF8FCB6A), w * 0.015f, o(0.32f, -0.78f))
        for (k in 0 until 5) drawCircle(grey.darken(0.3f), w * 0.012f, o(ROCK_SPECKS[k * 2], ROCK_SPECKS[k * 2 + 1]))
    }
    drawPath(thSketch(w, h) { m(0.1f, -0.5f); l(0.2f, -0.36f); l(0.16f, -0.2f) }, grey.darken(0.4f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(rock, Ink.line, style = pen.stroke)
}

private val ROCK_SPECKS = floatArrayOf(-0.2f, -0.3f, 0.3f, -0.3f, -0.34f, -0.2f, 0.04f, -0.2f, 0.36f, -0.48f)

private val BIRCH_LEAF: FloatArray by lazy {
    // Outline of a birch leaf in unit box: base at the left, tip at the right, toothed edge.
    val n = 11
    val out = FloatArray((n * 2 + 2) * 2)
    var idx = 0
    fun add(x: Float, y: Float) {
        out[idx++] = x
        out[idx++] = y
    }
    add(-0.34f, -0.5f)
    for (i in 1..n) {
        val t = i / (n + 1f)
        val x = (1 - t) * (1 - t) * -0.34f + 2 * (1 - t) * t * -0.06f + t * t * 0.47f
        val y = (1 - t) * (1 - t) * -0.5f + 2 * (1 - t) * t * -1.22f + t * t * -0.52f
        val tooth = if (i % 2 == 0) 0.05f else 0f
        add(x + tooth * 0.3f, y - tooth)
    }
    add(0.47f, -0.52f)
    for (i in n downTo 1) {
        val t = i / (n + 1f)
        val x = (1 - t) * (1 - t) * -0.34f + 2 * (1 - t) * t * -0.06f + t * t * 0.47f
        val y = (1 - t) * (1 - t) * -0.5f + 2 * (1 - t) * t * 0.2f + t * t * -0.52f
        val tooth = if (i % 2 == 0) 0.05f else 0f
        add(x + tooth * 0.3f, y + tooth)
    }
    out
}

/** A birch leaf with a toothed edge. */
internal fun DrawScope.thLeaf(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val green = Color(0xFF6FC24A)
    capsule(o(-0.32f, -0.5f), o(-0.48f, -0.44f), h * 0.07f, green.darken(0.2f), pen)
    val pts = BIRCH_LEAF
    val leaf = Path()
    for (i in 0 until pts.size / 2) {
        val x = pts[i * 2] * w
        val y = pts[i * 2 + 1] * h
        if (i == 0) leaf.moveTo(x, y) else leaf.lineTo(x, y)
    }
    leaf.close()
    drawPath(leaf, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    inked(leaf, green, pen, outline = false)
    val vein = green.darken(0.3f)
    drawLine(vein, o(-0.32f, -0.5f), o(0.44f, -0.52f), pen.lw * 0.55f, StrokeCap.Round)
    for (k in 0 until 4) {
        val x = -0.2f + k * 0.16f
        drawLine(vein, o(x, -0.505f), o(x + 0.14f, -0.8f + k * 0.05f), pen.lw * 0.4f, StrokeCap.Round)
        drawLine(vein, o(x, -0.505f), o(x + 0.14f, -0.22f - k * 0.05f), pen.lw * 0.4f, StrokeCap.Round)
    }
    drawPath(leaf, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    shine(o(-0.1f, -0.7f), w * 0.1f, h * 0.08f, 0.45f)
}

internal fun DrawScope.thFeather(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val vane = thSketch(w, h) {
        m(0f, -0.2f); q(-0.46f, -0.3f, -0.44f, -0.5f); l(-0.26f, -0.54f); l(-0.44f, -0.6f); q(-0.4f, -0.9f, 0.1f, -0.99f)
        q(0.46f, -0.8f, 0.44f, -0.5f); q(0.4f, -0.28f, 0f, -0.2f); z()
    }
    val base = Color(0xFFEFF2F8)
    inked(vane, base, pen, outline = false)
    clipPath(vane) {
        drawRect(Color(0xFF8E93A6), o(-0.5f, -1f), Size(w, 0.2f * h))
        for (k in 0 until 6) {
            val y = -0.3f - k * 0.1f
            drawLine(Color(0xFFC9D0DC), o(0.02f + k * 0.012f, y), o(-0.5f, y + 0.12f), pen.lw * 0.35f)
            drawLine(Color(0xFFC9D0DC), o(0.02f + k * 0.012f, y), o(0.5f, y + 0.12f), pen.lw * 0.35f)
        }
    }
    drawPath(vane, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val shaft = thSketch(w, h) { m(0f, -0.01f); q(0.02f, -0.5f, 0.08f, -0.96f) }
    drawPath(shaft, Ink.line, style = Stroke(w * 0.08f + pen.lw * 1.2f, cap = StrokeCap.Round))
    drawPath(shaft, Color(0xFFF3E7C9), style = Stroke(w * 0.08f, cap = StrokeCap.Round))
}

// ------------------------------------------------------------------ home

private val CUP_COL = longArrayOf(0xFFF7F4EE, 0xFF2F6FB8, 0xFFE8554E, 0xFFFFC83D)

/** A cup on a saucer, rim tilted towards us so the inside shows. */
internal fun DrawScope.thCup(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val i = v.mod(4)
    val col = Color(CUP_COL[i])
    val saucer = Rect(-0.48f * w, -0.2f * h, 0.46f * w, -0.01f * h)
    inkedOval(saucer, Color.White, pen, shade = false)
    drawOval(Color(0xFFDDE3EE), Offset(saucer.left + w * 0.16f, saucer.top + h * 0.04f), Size(saucer.width - w * 0.32f, saucer.height - h * 0.08f))
    val handle = thSketch(w, h) { m(0.22f, -0.66f); c(0.52f, -0.7f, 0.52f, -0.28f, 0.2f, -0.32f) }
    drawPath(handle, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(handle, col.shadow(), style = Stroke(w * 0.1f, cap = StrokeCap.Round))
    val body = thSketch(w, h) { m(-0.4f, -0.78f); l(0.28f, -0.78f); l(0.22f, -0.24f); q(0.18f, -0.12f, 0.08f, -0.12f); l(-0.2f, -0.12f); q(-0.3f, -0.12f, -0.34f, -0.24f); z() }
    inked(body, col, pen, outline = false)
    val deco = if (i == 0) Color(0xFF2F6FB8) else Color.White
    clipPath(body) {
        when (i) {
            0 -> {
                drawRect(deco, o(-0.5f, -0.7f), Size(w, 0.05f * h))
                drawRect(deco, o(-0.5f, -0.3f), Size(w, 0.03f * h))
            }
            1 -> for (k in 0 until 6) drawCircle(deco, w * 0.04f, o(-0.3f + (k % 3) * 0.24f + (k / 3) * 0.12f, -0.62f + (k / 3) * 0.22f))
            2 -> {
                drawPath(thHeart(-0.06f * w, -0.46f * h, w * 0.22f), deco)
                drawPath(thHeart(0.16f * w, -0.62f * h, w * 0.1f), deco)
            }
            else -> {
                drawRect(Color.White, o(-0.5f, -0.52f), Size(w, 0.1f * h))
                drawRect(Color(0xFFFF9F43), o(-0.5f, -0.49f), Size(w, 0.04f * h))
            }
        }
        drawLine(Color.White.copy(alpha = 0.55f), o(-0.28f, -0.66f), o(-0.24f, -0.3f), w * 0.07f, StrokeCap.Round)
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val rim = Rect(-0.4f * w, -0.86f * h, 0.28f * w, -0.7f * h)
    drawOval(col.lighten(0.2f), rim.topLeft, rim.size)
    val inner = Rect(rim.left + pen.lw, rim.top + pen.lw * 0.8f, rim.right - pen.lw, rim.bottom - pen.lw * 0.5f)
    drawOval(Brush.verticalGradient(listOf(col.darken(0.3f), col.darken(0.08f)), inner.top, inner.bottom), inner.topLeft, inner.size)
    drawOval(Ink.line, rim.topLeft, rim.size, style = pen.stroke)
}

private val PILLOW_COL = longArrayOf(0xFFE8554E, 0xFF4A8BFF, 0xFF2E9B5E, 0xFFFF9EC7)

internal fun DrawScope.thPillow(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val i = v.mod(4)
    val col = Color(PILLOW_COL[i])
    val pillow = thSketch(w, h) {
        m(-0.48f, -0.95f); q(0f, -0.84f, 0.48f, -0.95f); q(0.4f, -0.5f, 0.48f, -0.04f); q(0f, -0.16f, -0.48f, -0.04f)
        q(-0.4f, -0.5f, -0.48f, -0.95f); z()
    }
    inked(pillow, col, pen, outline = false)
    clipPath(pillow) {
        val c = o(0f, -0.5f)
        when (i) {
            0 -> drawPath(thHeart(c.x, c.y, h * 0.5f), Color.White)
            1 -> {
                drawPath(starPath(c, h * 0.24f, h * 0.1f), Color.White)
                drawPath(starPath(o(-0.26f, -0.7f), h * 0.1f, h * 0.045f), Color.White)
                drawPath(starPath(o(0.28f, -0.3f), h * 0.1f, h * 0.045f), Color.White)
            }
            2 -> {
                for (k in -3..3) drawLine(Color.White.copy(alpha = 0.8f), o(k * 0.13f, -1f), o(k * 0.13f, 0f), w * 0.02f)
                for (k in 0 until 4) drawLine(Color.White.copy(alpha = 0.8f), o(-0.5f, -0.8f + k * 0.2f), o(0.5f, -0.8f + k * 0.2f), w * 0.02f)
            }
            else -> for (k in 0 until 9) drawCircle(Color.White, h * 0.06f, o(-0.3f + (k % 3) * 0.3f + (k / 3 % 2) * 0.08f, -0.76f + (k / 3) * 0.26f))
        }
        drawPath(pillow, col.lighten(0.35f), style = Stroke(pen.lw * 2.4f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(pen.lw * 1.2f, pen.lw * 1.2f))))
    }
    drawPath(pillow, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val crease = col.darken(0.3f)
    for (s in SIDES) {
        drawLine(crease, o(s * 0.44f, -0.88f), o(s * 0.34f, -0.8f), pen.lw * 0.5f, StrokeCap.Round)
        drawLine(crease, o(s * 0.44f, -0.1f), o(s * 0.34f, -0.18f), pen.lw * 0.5f, StrokeCap.Round)
    }
    shine(o(-0.28f, -0.74f), w * 0.1f, h * 0.1f, 0.4f)
}

/** A clay pot with a flowering pelargonium. */
internal fun DrawScope.thPlantPot(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val stem = Color(0xFF3F9E47)
    for (k in 0 until 2) {
        val top = if (k == 0) o(-0.16f, -0.82f) else o(0.2f, -0.78f)
        drawLine(Ink.line, o(0f, -0.44f), top, w * 0.05f + pen.lw * 2f, StrokeCap.Round)
        drawLine(stem, o(0f, -0.44f), top, w * 0.05f, StrokeCap.Round)
    }
    val leaf = Color(0xFF5DBB4A)
    for (k in 0 until 5) {
        val c = o(PELARGONIUM_LEAVES[k * 2], PELARGONIUM_LEAVES[k * 2 + 1])
        val r = w * 0.15f
        thUnion(thFluff(c, r * 0.85f, 7), leaf, pen)
        drawCircle(leaf.darken(0.25f), r * 0.5f, c, style = Stroke(pen.lw * 0.5f))
    }
    val red = Color(0xFFFF4D6D)
    for (k in 0 until 2) {
        val cc = if (k == 0) o(-0.16f, -0.86f) else o(0.2f, -0.82f)
        val cluster = Path()
        for (j in 0 until 6) {
            val a = Math.toRadians(j * 60.0 + k * 20.0)
            val r = w * 0.07f
            val px = cc.x + w * 0.09f * cos(a).toFloat()
            val py = cc.y + w * 0.08f * sin(a).toFloat()
            cluster.addOval(Rect(px - r, py - r, px + r, py + r))
        }
        cluster.addOval(Rect(cc.x - w * 0.07f, cc.y - w * 0.07f, cc.x + w * 0.07f, cc.y + w * 0.07f))
        thUnion(cluster, red, pen)
        for (j in 0 until 6) {
            val a = Math.toRadians(j * 60.0 + k * 20.0)
            drawCircle(red.lighten(0.5f), w * 0.018f, Offset(cc.x + w * 0.09f * cos(a).toFloat(), cc.y + w * 0.08f * sin(a).toFloat()))
        }
    }
    val clay = Color(0xFFD2743A)
    val pot = thSketch(w, h) { m(-0.34f, -0.36f); l(0.34f, -0.36f); l(0.27f, -0.04f); q(0.26f, -0.02f, 0.22f, -0.02f); l(-0.22f, -0.02f); q(-0.26f, -0.02f, -0.27f, -0.04f); z() }
    inked(pot, clay, pen)
    drawLine(Color.White.copy(alpha = 0.4f), o(-0.24f, -0.3f), o(-0.2f, -0.08f), w * 0.05f, StrokeCap.Round)
    val rim = Rect(-0.4f * w, -0.46f * h, 0.4f * w, -0.34f * h)
    inkedRound(rim, h * 0.03f, clay.lighten(0.1f), pen)
    drawOval(Color(0xFF5A3A24), Offset(rim.left + w * 0.06f, rim.top - h * 0.015f), Size(rim.width - w * 0.12f, h * 0.035f))
}

private val PELARGONIUM_LEAVES = floatArrayOf(-0.26f, -0.54f, 0.27f, -0.56f, -0.1f, -0.64f, 0.12f, -0.68f, 0f, -0.5f)

/** A candle in a little brass dish; the flame flickers and glows. */
internal fun DrawScope.thCandle(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val t = pen.t
    val flicker = 1f + 0.08f * sin(t * 11f) + 0.05f * sin(t * 17f)
    val sway = 0.05f * sin(t * 7f) * w
    val base = o(0f, -0.72f)
    thGlow(Offset(base.x, base.y - h * 0.1f), w * 1.1f, Color(0xFFFFD27A), 0.35f + 0.1f * sin(t * 9f))
    inkedOval(Rect(-0.5f * w, -0.16f * h, 0.5f * w, -0.01f * h), Color(0xFFE0A63A), pen)
    drawOval(Color(0xFFF2C96A), Offset(-0.36f * w, -0.14f * h), Size(0.72f * w, 0.07f * h))
    val body = Rect(-0.3f * w, -0.68f * h, 0.3f * w, -0.08f * h)
    inkedRound(body, w * 0.08f, Color(0xFFF7F1E3), pen)
    val drip = thSketch(w, h) { m(-0.3f, -0.66f); l(-0.1f, -0.66f); q(-0.1f, -0.56f, -0.16f, -0.56f); q(-0.2f, -0.56f, -0.2f, -0.48f); q(-0.22f, -0.42f, -0.26f, -0.46f); l(-0.3f, -0.5f); z() }
    drawPath(drip, Color.White)
    drawOval(Color.White, Offset(body.left + pen.lw * 0.5f, body.top - h * 0.02f), Size(body.width - pen.lw, h * 0.05f))
    drawRoundRect(Ink.line, body.topLeft, body.size, CornerRadius(w * 0.08f), style = pen.stroke)
    drawLine(Ink.line, base, o(0f, -0.66f), pen.lw * 0.8f, StrokeCap.Round)
    val fh = h * 0.24f * flicker
    val fw = w * 0.4f
    fun flame(scale: Float): Path = Path().apply {
        val th = fh * scale
        val tw = fw * scale
        val tp = Offset(base.x + sway * scale, base.y - th)
        moveTo(tp.x, tp.y)
        cubicTo(tp.x + tw * 0.05f, tp.y + th * 0.35f, base.x + tw * 0.5f, base.y - th * 0.45f, base.x + tw * 0.45f, base.y - th * 0.2f)
        quadraticTo(base.x + tw * 0.4f, base.y + th * 0.02f, base.x, base.y + th * 0.02f)
        quadraticTo(base.x - tw * 0.4f, base.y + th * 0.02f, base.x - tw * 0.45f, base.y - th * 0.2f)
        cubicTo(base.x - tw * 0.5f, base.y - th * 0.45f, tp.x - tw * 0.05f, tp.y + th * 0.35f, tp.x, tp.y)
        close()
    }
    val outer = flame(1f)
    drawPath(outer, Color(0xFFFFA82E))
    drawPath(outer, Color(0xFFE07A1E), style = Stroke(pen.lw * 0.5f))
    drawPath(flame(0.62f), Color(0xFFFFEC8A))
    drawOval(Color(0xFF7FB8FF).copy(alpha = 0.7f), Offset(base.x - fw * 0.12f, base.y - fh * 0.12f), Size(fw * 0.24f, fh * 0.14f))
}

/** A wooden plank with grain and knots, with oblique depth so its top face shows. */
internal fun DrawScope.thPlank(w: Float, h: Float, pen: Pen) {
    val depth = h * 0.9f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val front = Rect(-0.49f * w, -h - dy + h * 0.04f, 0.49f * w - dx, -0.03f * h)
    val wood = Color(0xFFD9A066)
    box3d(front, depth, wood, pen, top = wood.lighten(0.2f), side = wood.darken(0.25f), radius = 0f)
    val grain = wood.darken(0.3f)
    clipPath(Path().apply { addRect(front) }) {
        for (k in 0 until 2) {
            val y = front.top + front.height * (0.35f + k * 0.35f)
            val p = Path().apply {
                moveTo(front.left, y)
                cubicTo(front.left + front.width * 0.3f, y - front.height * 0.2f, front.left + front.width * 0.6f, y + front.height * 0.2f, front.right, y - front.height * 0.05f)
            }
            drawPath(p, grain, style = Stroke(pen.lw * 0.45f))
        }
        val knot = Offset(front.left + front.width * 0.3f, front.center.y)
        drawOval(grain, Offset(knot.x - front.height * 0.3f, knot.y - front.height * 0.18f), Size(front.height * 0.6f, front.height * 0.36f), style = Stroke(pen.lw * 0.45f))
        drawOval(grain.darken(0.2f), Offset(knot.x - front.height * 0.12f, knot.y - front.height * 0.08f), Size(front.height * 0.24f, front.height * 0.16f))
    }
    val topY = front.top + dy * 0.5f
    drawLine(wood.darken(0.15f), Offset(front.left + dx * 0.5f + w * 0.05f, topY), Offset(front.right + dx * 0.5f - w * 0.05f, topY), pen.lw * 0.4f)
    val knot2 = Offset(front.left + front.width * 0.72f, front.center.y)
    drawOval(grain, Offset(knot2.x - front.height * 0.2f, knot2.y - front.height * 0.14f), Size(front.height * 0.4f, front.height * 0.28f), style = Stroke(pen.lw * 0.45f))
    drawCircle(ThingInk.steel, pen.lw * 0.6f, Offset(front.left + front.height * 0.4f, front.center.y))
    drawCircle(ThingInk.steel, pen.lw * 0.6f, Offset(front.right - front.height * 0.4f, front.center.y))
}

/** A wooden bird box with a red roof and a round hole, in oblique depth. */
internal fun DrawScope.thBirdhouse(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val depth = w * 0.32f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val wood = Color(0xFFE3B27A)
    val roofCol = Color(0xFFD2443A)
    val l = -0.4f * w
    val r = 0.4f * w - dx
    val cx = (l + r) / 2
    val eave = -0.58f * h
    val peak = -0.86f * h - dy * 0.2f
    val bottom = -0.08f * h
    // The right wall recedes up and to the right.
    val side = Path().apply { moveTo(r, eave); lineTo(r + dx, eave + dy); lineTo(r + dx, bottom + dy); lineTo(r, bottom); close() }
    drawPath(side, wood.darken(0.25f))
    drawPath(side, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val front = Path().apply { moveTo(l, bottom); lineTo(l, eave); lineTo(cx, peak); lineTo(r, eave); lineTo(r, bottom); close() }
    inked(front, wood, pen, outline = false)
    clipPath(front) { for (k in 1..3) drawLine(wood.darken(0.2f), Offset(l + (r - l) * k / 4f, peak), Offset(l + (r - l) * k / 4f, bottom), pen.lw * 0.45f) }
    drawPath(front, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val hole = Offset(cx, -0.5f * h)
    drawCircle(wood.darken(0.15f), w * 0.13f, hole)
    drawCircle(Color(0xFF3A2418), w * 0.1f, hole)
    drawCircle(Ink.line, w * 0.1f, hole, style = pen.thin)
    capsule(Offset(cx, -0.33f * h), Offset(cx + dx * 0.4f, -0.33f * h + dy * 0.4f), w * 0.035f, ThingInk.woodDark, pen)
    thDot(Offset(cx, -0.33f * h), w * 0.03f, ThingInk.woodDark, pen)
    // Roof: two slabs over the gable, the far edge offset in depth.
    val over = w * 0.08f
    val leftSlab = Path().apply {
        moveTo(cx, peak - h * 0.06f); lineTo(l - over, eave + h * 0.05f); lineTo(l - over + dx, eave + h * 0.05f + dy)
        lineTo(cx + dx, peak - h * 0.06f + dy); close()
    }
    inked(leftSlab, roofCol, pen)
    val rightSlab = Path().apply {
        moveTo(cx, peak - h * 0.06f); lineTo(r + over, eave + h * 0.05f); lineTo(r + over + dx, eave + h * 0.05f + dy)
        lineTo(cx + dx, peak - h * 0.06f + dy); close()
    }
    inked(rightSlab, roofCol.darken(0.15f), pen, shade = false)
    val edge = Path().apply { moveTo(l - over, eave + h * 0.05f); lineTo(cx, peak - h * 0.06f); lineTo(r + over, eave + h * 0.05f) }
    drawPath(edge, Ink.line, style = Stroke(h * 0.07f + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(edge, roofCol.lighten(0.1f), style = Stroke(h * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    inkedRound(Rect(l - w * 0.04f, bottom - h * 0.02f, r + w * 0.04f, -0.01f * h), h * 0.02f, ThingInk.woodDark, pen, shade = false)
    drawCircle(Ink.line, w * 0.04f, Offset(cx + dx * 0.5f, peak - h * 0.1f + dy * 0.5f), style = Stroke(pen.lw * 0.8f))
}
