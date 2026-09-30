package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.Palette
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.thScissors(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val steel = ThingInk.silver
    val handle = Color(0xFFFF9F43)
    for (s in SIDES) {
        val c = o(-0.3f, -0.5f + s * 0.24f)
        val ring = Rect(c.x - 0.16f * w, c.y - 0.2f * h, c.x + 0.16f * w, c.y + 0.2f * h)
        drawOval(Ink.line, ring.topLeft, ring.size, style = Stroke(h * 0.12f + pen.lw * 2f))
        drawOval(handle, ring.topLeft, ring.size, style = Stroke(h * 0.12f))
        drawArc(Color.White.copy(alpha = 0.5f), 200f, 60f, false, ring.topLeft, ring.size, style = Stroke(h * 0.04f, cap = StrokeCap.Round))
        capsule(o(-0.15f, -0.5f + s * 0.17f), o(-0.02f, -0.5f + s * 0.05f), h * 0.1f, handle, pen)
    }
    val upper = thSketch(w, h) { m(-0.04f, -0.58f); q(0.28f, -0.8f, 0.49f, -0.78f); q(0.3f, -0.62f, 0.02f, -0.44f); z() }
    val lower = thSketch(w, h) { m(-0.04f, -0.42f); q(0.28f, -0.2f, 0.49f, -0.22f); q(0.3f, -0.38f, 0.02f, -0.56f); z() }
    inked(lower, steel.darken(0.08f), pen)
    inked(upper, steel, pen)
    drawLine(Color.White.copy(alpha = 0.8f), o(0.1f, -0.62f), o(0.36f, -0.74f), pen.lw * 0.6f, StrokeCap.Round)
    val pivot = o(0f, -0.5f)
    thDot(pivot, h * 0.07f, ThingInk.steel, pen)
    drawLine(Ink.line, Offset(pivot.x - h * 0.04f, pivot.y), Offset(pivot.x + h * 0.04f, pivot.y), pen.lw * 0.4f)
}

internal fun DrawScope.thHairDryer(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val pink = Color(0xFFFF6FA8)
    val cord = thSketch(w, h) { m(-0.12f, -0.04f); c(-0.2f, 0f, -0.3f, -0.1f, -0.4f, -0.04f); c(-0.44f, -0.02f, -0.48f, -0.06f, -0.49f, -0.1f) }
    drawPath(cord, Ink.line, style = Stroke(pen.lw * 1.8f, cap = StrokeCap.Round))
    drawPath(cord, Color(0xFF8E93A6), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    val handle = thSketch(w, h) { m(-0.26f, -0.5f); l(-0.04f, -0.5f); l(-0.02f, -0.1f); q(-0.03f, -0.02f, -0.1f, -0.02f); l(-0.16f, -0.02f); q(-0.22f, -0.03f, -0.22f, -0.1f); z() }
    inked(handle, pink.darken(0.08f), pen)
    inkedRound(Rect(-0.18f * w, -0.4f * h, -0.08f * w, -0.3f * h), w * 0.02f, Color.White, pen, shade = false)
    val nozzle = thSketch(w, h) { m(0.2f, -0.86f); l(0.48f, -0.8f); q(0.5f, -0.68f, 0.48f, -0.56f); l(0.2f, -0.5f); z() }
    inked(nozzle, Color(0xFF3A3F5C), pen)
    inkedRound(Rect(-0.42f * w, -0.94f * h, 0.26f * w, -0.42f * h), h * 0.26f, pink, pen)
    val back = Rect(-0.47f * w, -0.92f * h, -0.33f * w, -0.44f * h)
    inkedOval(back, Color(0xFFE0E4EE), pen, shade = false)
    for (k in 0 until 3) drawLine(Color(0xFF8E93A6), Offset(back.left + back.width * 0.3f, back.top + back.height * (0.3f + k * 0.2f)), Offset(back.right - back.width * 0.25f, back.top + back.height * (0.3f + k * 0.2f)), pen.lw * 0.5f, StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.7f), o(-0.24f, -0.84f), o(0.14f, -0.84f), pen.lw * 1.1f, StrokeCap.Round)
}

internal fun DrawScope.thComb(w: Float, h: Float, pen: Pen) {
    val col = Color(0xFF4AB3FF)
    // Fine teeth in a deeper tone of the comb (an ink rim on each would swallow them), a solid handle.
    val tooth = col.darken(0.3f)
    val n = 12
    for (k in 0 until n) {
        val x = (-0.43f + k * (0.62f / (n - 1))) * w
        drawLine(tooth, Offset(x, -0.6f * h), Offset(x, -0.1f * h), w * 0.026f, StrokeCap.Round)
    }
    val handle = thSketch(w, h) { m(0.24f, -0.62f); l(0.44f, -0.62f); q(0.49f, -0.62f, 0.49f, -0.4f); q(0.49f, -0.1f, 0.4f, -0.08f); l(0.28f, -0.08f); q(0.24f, -0.08f, 0.24f, -0.2f); z() }
    inked(handle, col, pen, shade = false)
    val spine = Rect(-0.48f * w, -0.98f * h, 0.49f * w, -0.56f * h)
    inkedRound(spine, h * 0.2f, col, pen)
    drawLine(Color.White.copy(alpha = 0.7f), Offset(-0.4f * w, -0.84f * h), Offset(0.3f * w, -0.84f * h), pen.lw * 0.8f, StrokeCap.Round)
}

/** A hair-colour spray: the can is the colour it gives. */
internal fun DrawScope.thSpray(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = argb(Palette.hairs[v.mod(Palette.hairs.size)])
    val cap = thSketch(w, h) { m(-0.18f, -0.84f); l(-0.18f, -0.92f); q(-0.18f, -0.97f, -0.1f, -0.97f); l(0.1f, -0.97f); q(0.18f, -0.97f, 0.18f, -0.92f); l(0.18f, -0.84f); z() }
    inked(cap, col.lighten(0.25f), pen)
    thDot(o(0.22f, -0.92f), w * 0.05f, ThingInk.steel, pen)
    val mist = col.lighten(0.3f)
    for (k in 0 until 3) {
        val ph = (pen.t * 0.8f + k * 0.33f) % 1f
        drawCircle(mist.copy(alpha = 0.8f * (1f - ph)), w * 0.035f, o(0.3f + ph * 0.15f, -0.93f - ph * 0.05f + k * 0.01f))
    }
    val shoulder = thSketch(w, h) { m(-0.4f, -0.72f); q(-0.38f, -0.86f, 0f, -0.86f); q(0.38f, -0.86f, 0.4f, -0.72f); z() }
    inked(shoulder, ThingInk.silver, pen)
    val can = Rect(-0.4f * w, -0.74f * h, 0.4f * w, -0.02f * h)
    inkedRound(can, w * 0.1f, col, pen, shade = false)
    clipPath(roundPath(can, w * 0.1f)) {
        drawRect(col.darken(0.2f), o(0.18f, -0.74f), Size(0.22f * w, 0.72f * h))
        val label = Rect(-0.4f * w, -0.54f * h, 0.4f * w, -0.26f * h)
        drawRect(Color.White, label.topLeft, label.size)
        val swoosh = thSketch(w, h) { m(-0.3f, -0.3f); c(-0.2f, -0.56f, 0.1f, -0.2f, 0.3f, -0.48f) }
        drawPath(swoosh, col, style = Stroke(w * 0.1f, cap = StrokeCap.Round))
        drawLine(Ink.line, label.topLeft, Offset(label.right, label.top), pen.lw * 0.5f)
        drawLine(Ink.line, Offset(label.left, label.bottom), Offset(label.right, label.bottom), pen.lw * 0.5f)
        drawRect(ThingInk.silver, o(-0.5f, -0.1f), Size(w, 0.08f * h))
        drawLine(Color.White.copy(alpha = 0.55f), o(-0.24f, -0.68f), o(-0.24f, -0.12f), w * 0.08f, StrokeCap.Round)
    }
    drawRoundRect(Ink.line, can.topLeft, can.size, CornerRadius(w * 0.1f), style = pen.stroke)
}

/** A magic wand: a slim stick with a gold star that sparkles. */
internal fun DrawScope.thWand(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val stick = Color(0xFF6A4BD8)
    capsule(o(0f, -0.03f), o(0f, -0.78f), w * 0.3f, stick, pen)
    for (k in 0 until 3) drawLine(ThingInk.gold, o(-0.14f, -0.2f - k * 0.18f), o(0.14f, -0.26f - k * 0.18f), pen.lw * 0.6f, StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.5f), o(-0.05f, -0.1f), o(-0.05f, -0.72f), w * 0.07f, StrokeCap.Round)
    val sc = o(0f, -0.86f)
    val pulse = 0.5f + 0.5f * sin(pen.t * 2.6f)
    thGlow(sc, h * 0.13f, Color(0xFFFFE066), 0.4f + 0.3f * pulse)
    inked(starPath(sc, w * 0.62f, w * 0.28f, sin(pen.t * 1.5f) * 8f), Color(0xFFFFD23F), pen)
    shine(Offset(sc.x - w * 0.12f, sc.y - w * 0.14f), w * 0.16f, w * 0.1f)
    for (k in 0 until 3) {
        val a = pen.t * 1.8f + k * 2.094f
        thGlint(Offset(sc.x + cos(a) * w * 0.7f, sc.y + sin(a) * h * 0.1f), w * 0.3f, pen.t, 3.2f, k * 1.7f)
    }
}

private val BUCKET_COL = longArrayOf(0xFFFF5A4E, 0xFF4AB3FF, 0xFFFFD23F)

internal fun DrawScope.thBucket(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(BUCKET_COL[v.mod(3)])
    val handle = thSketch(w, h) { m(-0.42f, -0.66f); c(-0.4f, -1.04f, 0.4f, -1.04f, 0.42f, -0.66f) }
    drawPath(handle, Ink.line, style = Stroke(pen.lw * 2.2f, cap = StrokeCap.Round))
    drawPath(handle, Color.White, style = Stroke(pen.lw * 1f, cap = StrokeCap.Round))
    val body = thSketch(w, h) { m(-0.44f, -0.72f); l(0.44f, -0.72f); l(0.34f, -0.05f); q(0.33f, -0.02f, 0.29f, -0.02f); l(-0.29f, -0.02f); q(-0.33f, -0.02f, -0.34f, -0.05f); z() }
    inked(body, col, pen, outline = false)
    clipPath(body) {
        drawRect(col.lighten(0.3f), o(-0.5f, -0.66f), Size(w, 0.06f * h))
        drawLine(Color.White.copy(alpha = 0.55f), o(-0.3f, -0.56f), o(-0.24f, -0.12f), w * 0.07f, StrokeCap.Round)
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val star = starPath(o(0.04f, -0.34f), w * 0.14f, w * 0.06f, 10f)
    drawPath(star, Color.White)
    drawPath(star, Ink.line, style = Stroke(pen.lw * 0.5f, join = StrokeJoin.Round))
    val rim = Rect(-0.47f * w, -0.8f * h, 0.47f * w, -0.64f * h)
    inkedOval(rim, col.lighten(0.15f), pen, shade = false)
    val inside = Rect(rim.left + w * 0.05f, rim.top + h * 0.025f, rim.right - w * 0.05f, rim.bottom - h * 0.025f)
    drawOval(col.darken(0.4f), inside.topLeft, inside.size)
    clipPath(ovalPath(inside)) { drawOval(Color(0xFFF2D08A), Offset(inside.left, inside.top + inside.height * 0.35f), inside.size) }
    drawOval(Ink.line, inside.topLeft, inside.size, style = pen.thin)
    for (s in SIDES) thDot(o(s * 0.43f, -0.66f), w * 0.03f, Color.White, pen)
}

internal fun DrawScope.thSpade(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val blue = Color(0xFF3D6BFF)
    inkedRound(Rect(-0.1f * w, -0.9f * h, 0.1f * w, -0.36f * h), w * 0.08f, blue, pen, shade = false)
    drawLine(Color.White.copy(alpha = 0.5f), o(-0.03f, -0.86f), o(-0.03f, -0.42f), w * 0.05f, StrokeCap.Round)
    val grip = Rect(-0.42f * w, -0.995f * h, 0.42f * w, -0.87f * h)
    inkedRound(grip, h * 0.05f, blue, pen)
    val blade = thSketch(w, h) { m(-0.44f, -0.42f); l(0.44f, -0.42f); l(0.44f, -0.2f); q(0.4f, -0.04f, 0f, -0.01f); q(-0.4f, -0.04f, -0.44f, -0.2f); z() }
    inked(blade, Color(0xFFFFC83D), pen)
    drawLine(Color(0xFFFFC83D).darken(0.3f), o(0f, -0.36f), o(0f, -0.08f), pen.lw * 0.5f, StrokeCap.Round)
    inkedRound(Rect(-0.16f * w, -0.46f * h, 0.16f * w, -0.38f * h), w * 0.04f, blue.darken(0.1f), pen, shade = false)
    shine(o(-0.22f, -0.3f), w * 0.08f, h * 0.06f)
}

internal fun DrawScope.thToothbrush(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val mint = Color(0xFF3DDC97)
    inkedRound(Rect(-0.26f * w, -0.8f * h, 0.14f * w, -0.62f * h), w * 0.15f, mint, pen, shade = false)
    val handle = Rect(-0.4f * w, -0.66f * h, 0.36f * w, -0.02f * h)
    inkedRound(handle, w * 0.36f, mint, pen)
    for (k in 0 until 3) drawLine(Color.White, o(-0.16f, -0.44f + k * 0.08f), o(0.12f, -0.44f + k * 0.08f), pen.lw * 0.6f, StrokeCap.Round)
    val bristles = Rect(0.06f * w, -0.97f * h, 0.48f * w, -0.77f * h)
    drawRect(Color.White, bristles.topLeft, bristles.size)
    for (k in 0 until 4) {
        val y = bristles.top + bristles.height * (0.15f + k * 0.23f)
        drawLine(if (k % 2 == 0) Color(0xFF4AB3FF) else Color.White, Offset(bristles.left, y), Offset(bristles.right, y), bristles.height * 0.18f)
    }
    drawRect(Ink.line, bristles.topLeft, bristles.size, style = pen.thin)
    inkedRound(Rect(-0.3f * w, -0.99f * h, 0.12f * w, -0.75f * h), w * 0.14f, mint, pen)
}

internal fun DrawScope.thHammer(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    capsule(o(0f, -0.04f), o(0f, -0.8f), w * 0.2f, ThingInk.woodLight, pen)
    drawLine(ThingInk.wood, o(0.03f, -0.7f), o(0.03f, -0.4f), pen.lw * 0.45f, StrokeCap.Round)
    val grip = Rect(-0.13f * w, -0.36f * h, 0.13f * w, -0.03f * h)
    inkedRound(grip, w * 0.1f, Color(0xFFFF5A4E), pen)
    for (k in 1..3) drawLine(Color(0xFFFF5A4E).darken(0.3f), o(-0.13f, -0.36f + k * 0.08f), o(0.13f, -0.36f + k * 0.08f), pen.lw * 0.45f)
    val steel = Color(0xFFB8C0D0)
    val claw = thSketch(w, h) { m(0.06f, -0.96f); q(0.36f, -0.97f, 0.48f, -0.78f); l(0.42f, -0.75f); q(0.3f, -0.86f, 0.06f, -0.8f); z() }
    inked(claw, steel.darken(0.05f), pen)
    drawLine(steel.darken(0.4f), o(0.2f, -0.88f), o(0.44f, -0.78f), pen.lw * 0.45f, StrokeCap.Round)
    inkedRound(Rect(-0.28f * w, -0.97f * h, 0.12f * w, -0.78f * h), w * 0.04f, steel, pen)
    inkedRound(Rect(-0.48f * w, -0.99f * h, -0.26f * w, -0.75f * h), w * 0.05f, steel.lighten(0.15f), pen)
    shine(o(-0.12f, -0.92f), w * 0.1f, h * 0.025f)
}

internal fun DrawScope.thSaw(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val blade = Path().apply {
        moveTo(-0.2f * w, -0.9f * h)
        lineTo(0.46f * w, -0.6f * h)
        quadraticTo(0.49f * w, -0.58f * h, 0.48f * w, -0.54f * h)
        lineTo(0.48f * w, -0.38f * h)
        val n = 12
        for (k in 0 until n) {
            val t0 = k / n.toFloat()
            val t1 = (k + 0.5f) / n
            val x0 = 0.48f + (-0.2f - 0.48f) * t0
            val y0 = -0.38f + (-0.2f + 0.38f) * t0
            val x1 = 0.48f + (-0.2f - 0.48f) * t1
            val y1 = -0.38f + (-0.2f + 0.38f) * t1
            lineTo(x0 * w, y0 * h)
            lineTo(x1 * w, (y1 + 0.09f) * h)
        }
        lineTo(-0.2f * w, -0.2f * h)
        close()
    }
    inked(blade, ThingInk.silver, pen)
    drawLine(Color.White.copy(alpha = 0.8f), o(-0.12f, -0.8f), o(0.4f, -0.57f), pen.lw * 0.7f, StrokeCap.Round)
    thDot(o(0.38f, -0.5f), w * 0.02f, Color.White, pen)
    val handle = Path().apply {
        fillType = PathFillType.EvenOdd
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(-0.49f * w, -0.97f * h, -0.14f * w, -0.1f * h), CornerRadius(w * 0.08f)))
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(-0.4f * w, -0.78f * h, -0.25f * w, -0.34f * h), CornerRadius(w * 0.06f)))
    }
    inked(handle, ThingInk.wood, pen)
    for (k in 0 until 2) thDot(o(-0.19f, -0.82f + k * 0.52f), w * 0.02f, ThingInk.gold, pen)
}

internal fun DrawScope.thWrench(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val steel = Color(0xFFB8C0D0)
    inkedRound(Rect(-0.17f * w, -0.8f * h, 0.17f * w, -0.2f * h), w * 0.12f, steel, pen)
    drawLine(steel.darken(0.3f), o(0f, -0.72f), o(0f, -0.28f), pen.lw * 0.5f, StrokeCap.Round)
    val c = o(0f, -0.84f)
    val r = w * 0.46f
    val slot = 0.15f * w
    val a = Math.toDegrees(asin((slot / r).toDouble())).toFloat()
    val head = Path().apply {
        val topY = c.y - (r * cos(Math.toRadians(a.toDouble()))).toFloat()
        moveTo(slot, topY)
        lineTo(slot, c.y + h * 0.01f)
        lineTo(-slot, c.y + h * 0.01f)
        lineTo(-slot, topY)
        arcTo(Rect(c.x - r, c.y - r, c.x + r, c.y + r), -90f - a, -(360f - 2f * a), false)
        close()
    }
    inked(head, steel, pen)
    val rc = o(0f, -0.14f)
    val rr = w * 0.38f
    val ring = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(rc.x - rr, rc.y - rr, rc.x + rr, rc.y + rr))
        val hr = rr * 0.5f
        moveTo(rc.x + hr, rc.y)
        for (k in 1 until 6) {
            val ang = Math.toRadians(k * 60.0)
            lineTo(rc.x + hr * cos(ang).toFloat(), rc.y + hr * sin(ang).toFloat())
        }
        close()
    }
    inked(ring, steel, pen)
    shine(o(-0.18f, -0.9f), w * 0.14f, h * 0.03f)
}

internal fun DrawScope.thScrewdriver(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    inkedRound(Rect(-0.1f * w, -0.92f * h, 0.1f * w, -0.46f * h), w * 0.05f, Color(0xFFB8C0D0), pen, shade = false)
    val tip = thSketch(w, h) { m(-0.1f, -0.9f); l(0.1f, -0.9f); l(0.06f, -0.995f); l(-0.06f, -0.995f); z() }
    inked(tip, Color(0xFF8E93A6), pen, shade = false)
    val handle = Rect(-0.46f * w, -0.5f * h, 0.46f * w, -0.02f * h)
    inkedRound(handle, w * 0.3f, Color(0xFFFFC83D), pen)
    for (k in -1..1) drawLine(Color(0xFFFFC83D).darken(0.3f), o(k * 0.2f, -0.42f), o(k * 0.2f, -0.1f), pen.lw * 0.5f, StrokeCap.Round)
    inkedRound(Rect(-0.3f * w, -0.54f * h, 0.3f * w, -0.46f * h), w * 0.08f, ThingInk.silver, pen, shade = false)
    drawLine(Color.White.copy(alpha = 0.6f), o(-0.22f, -0.4f), o(-0.22f, -0.1f), w * 0.1f, StrokeCap.Round)
}

internal fun DrawScope.thWateringCan(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val green = Color(0xFF2E9B5E)
    capsule(o(0.12f, -0.2f), o(0.4f, -0.68f), w * 0.07f, green.darken(0.08f), pen)
    val end = o(0.4f, -0.68f)
    rotate(-52f, pivot = end) {
        val rose = Path().apply {
            moveTo(end.x - w * 0.02f, end.y - w * 0.045f)
            lineTo(end.x + w * 0.09f, end.y - w * 0.1f)
            lineTo(end.x + w * 0.09f, end.y + w * 0.1f)
            lineTo(end.x - w * 0.02f, end.y + w * 0.045f)
            close()
        }
        inked(rose, green.darken(0.1f), pen, shade = false)
        val face = Rect(end.x + w * 0.065f, end.y - w * 0.105f, end.x + w * 0.115f, end.y + w * 0.105f)
        inkedOval(face, ThingInk.silver, pen, shade = false)
        for (k in -1..1) drawCircle(Ink.line, pen.lw * 0.32f, Offset(face.center.x, face.center.y + k * w * 0.05f))
    }
    val handle = thSketch(w, h) { m(-0.36f, -0.62f); c(-0.4f, -0.98f, 0.1f, -0.98f, 0.08f, -0.66f) }
    drawPath(handle, Ink.line, style = Stroke(w * 0.05f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(handle, green, style = Stroke(w * 0.05f, cap = StrokeCap.Round))
    val body = thSketch(w, h) { m(-0.4f, -0.66f); l(0.16f, -0.66f); l(0.19f, -0.08f); q(0.19f, -0.02f, 0.13f, -0.02f); l(-0.37f, -0.02f); q(-0.43f, -0.02f, -0.43f, -0.08f); z() }
    inked(body, green, pen, outline = false)
    clipPath(body) {
        drawRect(green.lighten(0.2f), o(-0.5f, -0.66f), Size(w, 0.07f * h))
        drawRect(green.darken(0.15f), o(-0.5f, -0.12f), Size(w, 0.1f * h))
        for (k in 0 until 5) drawCircle(green.darken(0.35f), w * 0.01f, o(-0.34f + k * 0.12f, -0.62f))
        drawLine(Color.White.copy(alpha = 0.5f), o(-0.3f, -0.54f), o(-0.3f, -0.18f), w * 0.05f, StrokeCap.Round)
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val mouth = Rect(-0.3f * w, -0.72f * h, 0.02f * w, -0.62f * h)
    inkedOval(mouth, green.lighten(0.15f), pen, shade = false)
    drawOval(Color(0xFF1E4A30), Offset(mouth.left + w * 0.03f, mouth.top + h * 0.02f), Size(mouth.width - w * 0.06f, mouth.height - h * 0.035f))
}

/** A seed packet with a carrot on the front. */
internal fun DrawScope.thSeeds(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val paper = ThingInk.cream
    val packet = thSketch(w, h) {
        m(-0.42f, -0.9f); l(-0.35f, -0.97f); l(-0.28f, -0.9f); l(-0.21f, -0.97f); l(-0.14f, -0.9f); l(-0.07f, -0.97f); l(0f, -0.9f)
        l(0.07f, -0.97f); l(0.14f, -0.9f); l(0.21f, -0.97f); l(0.28f, -0.9f); l(0.35f, -0.97f); l(0.42f, -0.9f)
        l(0.42f, -0.05f); q(0.42f, -0.02f, 0.38f, -0.02f); l(-0.38f, -0.02f); q(-0.42f, -0.02f, -0.42f, -0.05f); z()
    }
    inked(packet, paper, pen, outline = false)
    clipPath(packet) {
        drawRect(Color(0xFF3BC46B), o(-0.5f, -1f), Size(w, 0.18f * h))
        val pic = Rect(-0.32f * w, -0.76f * h, 0.32f * w, -0.26f * h)
        drawRoundRect(Color(0xFFBFE6FF), pic.topLeft, pic.size, CornerRadius(w * 0.05f))
        drawRect(Color(0xFF8A5A36), Offset(pic.left, pic.bottom - pic.height * 0.3f), Size(pic.width, pic.height * 0.3f))
        val cx = 0f
        for (k in -1..1) drawPath(thLens(o(cx, -0.6f), o(cx + k * 0.14f, -0.74f), w * 0.03f), Color(0xFF3F9E47))
        val root = thSketch(w, h) { m(-0.1f, -0.6f); q(0f, -0.64f, 0.1f, -0.6f); q(0.04f, -0.36f, 0f, -0.3f); q(-0.04f, -0.36f, -0.1f, -0.6f); z() }
        drawPath(root, Color(0xFFFF8A2B))
        drawPath(root, Ink.line, style = pen.thin)
        drawRoundRect(Ink.line, pic.topLeft, pic.size, CornerRadius(w * 0.05f), style = pen.thin)
        for (k in 0 until 5) drawOval(Color(0xFF8A5A36), o(-0.26f + k * 0.13f, -0.16f), Size(w * 0.05f, h * 0.03f))
    }
    drawPath(packet, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    shine(o(0.26f, -0.4f), w * 0.05f, h * 0.14f, 0.5f)
}
