package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private fun circlePath(c: Offset, r: Float): Path = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }

internal fun DrawScope.thBall(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h * 0.5f)
    val r = w * 0.48f
    val red = Color(0xFFFF5A4E)
    inkedCircle(c, r, red, pen)
    clipPath(circlePath(c, r)) {
        val band = Path().apply {
            moveTo(c.x - r * 1.2f, c.y + r * 0.1f)
            quadraticTo(c.x, c.y - r * 0.55f, c.x + r * 1.2f, c.y + r * 0.1f)
        }
        drawPath(band, Ink.line, style = Stroke(r * 0.46f + pen.lw * 1.2f))
        drawPath(band, Color.White, style = Stroke(r * 0.46f))
    }
    val star = starPath(Offset(c.x + r * 0.02f, c.y - r * 0.2f), r * 0.36f, r * 0.16f, 8f)
    drawPath(star, ThingInk.sun)
    drawPath(star, Ink.line, style = Stroke(pen.lw * 0.6f, join = StrokeJoin.Round))
    thCrescent(c, r, 0.18f)
    thSheen(c, r)
    drawCircle(Ink.line, r, c, style = pen.stroke)
    shine(Offset(c.x - r * 0.42f, c.y - r * 0.5f), r * 0.3f, r * 0.2f)
}

private val BEACH_K = floatArrayOf(-2.0f, -1.2f, -0.45f, 0.3f, 1.05f, 1.8f)
private val BEACH_COL = longArrayOf(0xFFFF4D4D, 0xFFFFD23F, 0xFF3D8BFF)

internal fun DrawScope.thBeachBall(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h * 0.5f)
    val r = w * 0.48f
    val ball = circlePath(c, r)
    drawPath(ball, Color.White)
    val p = Offset(c.x - r * 0.2f, c.y - r * 0.5f)
    val q = Offset(c.x + r * 0.35f, c.y + r * 1.6f)
    val mx = (p.x + q.x) / 2
    val my = (p.y + q.y) / 2
    val dl = sqrt((q.x - p.x) * (q.x - p.x) + (q.y - p.y) * (q.y - p.y))
    val px = -(q.y - p.y) / dl
    val py = (q.x - p.x) / dl
    clipPath(ball) {
        for (g in 0 until 3) {
            val k0 = BEACH_K[g * 2] * r
            val k1 = BEACH_K[g * 2 + 1] * r
            val gore = Path().apply {
                moveTo(p.x, p.y)
                quadraticTo(mx + px * k0, my + py * k0, q.x, q.y)
                quadraticTo(mx + px * k1, my + py * k1, p.x, p.y)
                close()
            }
            drawPath(gore, Color(BEACH_COL[g]))
        }
        for (k in BEACH_K) {
            val seam = Path().apply { moveTo(p.x, p.y); quadraticTo(mx + px * k * r, my + py * k * r, q.x, q.y) }
            drawPath(seam, Ink.line.copy(alpha = 0.35f), style = Stroke(pen.lw * 0.45f))
        }
    }
    thCrescent(c, r, 0.16f)
    thSheen(c, r, 0.5f)
    drawCircle(Ink.line, r, c, style = pen.stroke)
    thDot(p, r * 0.14f, Color.White, pen)
    shine(Offset(c.x - r * 0.5f, c.y - r * 0.2f), r * 0.16f, r * 0.26f)
}

internal fun DrawScope.thTeddy(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val fur = Color(0xFFB9793F)
    val light = Color(0xFFEBC690)
    for (s in SIDES) {
        inkedOval(rect(s * 0.25f * w, -0.14f * h, 0.36f * w, 0.24f * h), fur, pen)
        drawOval(light, o(s * 0.25f - 0.09f, -0.2f), Size(0.18f * w, 0.14f * h))
    }
    inkedOval(rect(0f, -0.38f * h, 0.66f * w, 0.52f * h), fur, pen)
    drawOval(light, o(-0.17f, -0.52f), Size(0.34f * w, 0.36f * h))
    drawArc(fur.darken(0.3f), 200f, 140f, false, o(-0.1f, -0.44f), Size(0.2f * w, 0.14f * h), style = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round))
    for (s in SIDES) {
        rotate(s * 28f, pivot = o(s * 0.3f, -0.5f)) { inkedOval(rect(s * 0.36f * w, -0.4f * h, 0.2f * w, 0.3f * h), fur, pen) }
    }
    for (s in SIDES) {
        inkedCircle(o(s * 0.26f, -0.87f), w * 0.11f, fur, pen)
        drawCircle(light, w * 0.055f, o(s * 0.26f, -0.87f))
    }
    val head = o(0f, -0.7f)
    inkedCircle(head, w * 0.3f, fur, pen)
    thSheen(head, w * 0.3f, 0.25f)
    inkedOval(rect(0f, -0.62f * h, 0.28f * w, 0.15f * h), light, pen, shade = false)
    drawOval(Ink.line, o(-0.055f, -0.68f), Size(0.11f * w, 0.06f * h))
    drawCircle(Color.White, w * 0.015f, o(-0.02f, -0.67f))
    drawPath(thSketch(w, h) { m(-0.06f, -0.6f); q(-0.03f, -0.57f, 0f, -0.6f); q(0.03f, -0.57f, 0.06f, -0.6f) }, Ink.line, style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
    for (s in SIDES) {
        drawCircle(Ink.line, w * 0.03f, o(s * 0.11f, -0.75f))
        drawCircle(Color.White, w * 0.011f, o(s * 0.11f - 0.01f, -0.76f))
        drawOval(Ink.blush, o(s * 0.2f - 0.06f, -0.66f), Size(0.12f * w, 0.05f * h))
    }
    val bow = ThingInk.sky
    for (s in SIDES) thFill(thSketch(w, h) { m(0f, -0.46f); l(s * 0.16f, -0.52f); l(s * 0.16f, -0.4f); z() }, bow, pen)
    thDot(o(0f, -0.46f), w * 0.04f, bow, pen)
}

private val BALLOON_COL = longArrayOf(0xFFFF5A4E, 0xFFFFD23F, 0xFF4AB3FF, 0xFF3BC46B, 0xFFFF6FA8, 0xFF8B5CF6)

/** The string's end is the origin; the balloon floats at the top of the box. */
internal fun DrawScope.thBalloon(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(BALLOON_COL[v.mod(6)])
    val sway = sin(pen.t * 1.7f)
    val string = thSketch(w, h) { m(0f, -0.56f); c(0.2f * sway, -0.42f, -0.2f * sway, -0.2f, 0f, 0f) }
    drawPath(string, Ink.line, style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
    val knot = thSketch(w, h) { m(0f, -0.59f); l(-0.08f, -0.555f); l(0.08f, -0.555f); z() }
    thFill(knot, col.darken(0.15f), pen)
    val body = thSketch(w, h) {
        m(0f, -0.585f); c(-0.28f, -0.6f, -0.47f, -0.7f, -0.47f, -0.82f); c(-0.47f, -0.93f, -0.26f, -0.995f, 0f, -0.995f)
        c(0.26f, -0.995f, 0.47f, -0.93f, 0.47f, -0.82f); c(0.47f, -0.7f, 0.28f, -0.6f, 0f, -0.585f); z()
    }
    inked(body, col, pen, outline = false)
    clipPath(body) { thSheen(o(0f, -0.8f), w * 0.5f, 0.45f) }
    drawPath(body, Ink.line, style = pen.stroke)
    shine(o(-0.22f, -0.87f), w * 0.14f, h * 0.05f, 0.9f)
    drawCircle(Color.White.copy(alpha = 0.8f), w * 0.035f, o(-0.3f, -0.79f))
}

internal fun DrawScope.thDuck(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val yellow = Color(0xFFFFD23F)
    val body = thSketch(w, h) {
        m(-0.36f, -0.44f); c(-0.46f, -0.1f, -0.2f, -0.02f, 0.1f, -0.02f); c(0.34f, -0.02f, 0.48f, -0.14f, 0.47f, -0.36f)
        l(0.49f, -0.62f); c(0.36f, -0.5f, 0.2f, -0.48f, 0f, -0.48f); z()
    }
    inked(body, yellow, pen)
    thFill(thLens(o(0.02f, -0.34f), o(0.34f, -0.28f), h * 0.1f), yellow.darken(0.12f), pen)
    val beak = thSketch(w, h) { m(-0.3f, -0.7f); q(-0.52f, -0.76f, -0.52f, -0.62f); q(-0.44f, -0.54f, -0.3f, -0.62f); z() }
    val head = o(-0.14f, -0.7f)
    inkedCircle(head, w * 0.24f, yellow, pen)
    clipPath(circlePath(head, w * 0.24f)) { thSheen(head, w * 0.24f, 0.35f) }
    inked(beak, Color(0xFFFF8A3D), pen, shade = false)
    drawLine(Color(0xFFD9642A), o(-0.5f, -0.66f), o(-0.34f, -0.66f), pen.lw * 0.5f, StrokeCap.Round)
    drawCircle(Ink.line, w * 0.035f, o(-0.2f, -0.77f))
    drawCircle(Color.White, w * 0.013f, o(-0.21f, -0.79f))
    drawOval(Ink.blush, o(-0.12f, -0.68f), Size(0.12f * w, 0.07f * h))
    shine(o(-0.08f, -0.86f), w * 0.1f, h * 0.06f)
}

internal fun DrawScope.thGuitar(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val neckWood = Color(0xFF7A4A2A)
    inkedRound(Rect(-0.08f * w, -0.9f * h, 0.08f * w, -0.58f * h), w * 0.03f, neckWood, pen, shade = false)
    for (k in 1..4) drawLine(Color(0xFFD9DDE8), o(-0.08f, -0.6f - k * 0.06f), o(0.08f, -0.6f - k * 0.06f), pen.lw * 0.45f)
    inkedRound(Rect(-0.14f * w, -0.995f * h, 0.14f * w, -0.87f * h), w * 0.05f, neckWood, pen)
    for (s in SIDES) for (k in 0 until 2) thDot(o(s * 0.2f, -0.965f + k * 0.06f), w * 0.04f, ThingInk.silver, pen)
    val wood = Color(0xFFE3A058)
    val body = thSketch(w, h) {
        m(0f, -0.64f); c(0.22f, -0.64f, 0.36f, -0.58f, 0.33f, -0.47f); c(0.31f, -0.41f, 0.26f, -0.4f, 0.3f, -0.35f)
        c(0.52f, -0.3f, 0.52f, -0.04f, 0f, -0.04f); c(-0.52f, -0.04f, -0.52f, -0.3f, -0.3f, -0.35f)
        c(-0.26f, -0.4f, -0.31f, -0.41f, -0.33f, -0.47f); c(-0.36f, -0.58f, -0.22f, -0.64f, 0f, -0.64f); z()
    }
    inked(body, wood, pen)
    clipPath(body) {
        drawPath(body, wood.darken(0.35f), style = Stroke(pen.lw * 1.6f))
    }
    drawPath(body, Ink.line, style = pen.stroke)
    val guard = thSketch(w, h) { m(0.1f, -0.44f); q(0.3f, -0.4f, 0.24f, -0.26f); q(0.14f, -0.24f, 0.1f, -0.32f); z() }
    thFill(guard, Color(0xFF8A5234), pen)
    val hole = o(0f, -0.37f)
    drawCircle(Color(0xFF3A2418), w * 0.13f, hole)
    drawCircle(Ink.line, w * 0.13f, hole, style = pen.thin)
    drawCircle(Color(0xFFFFD27A), w * 0.17f, hole, style = Stroke(pen.lw * 0.6f))
    inkedRound(Rect(-0.15f * w, -0.18f * h, 0.15f * w, -0.14f * h), w * 0.02f, Color(0xFF5A3520), pen, shade = false)
    for (k in 0 until 4) {
        val x = -0.045f + k * 0.03f
        drawLine(Color(0xFFF4EEDF), o(x, -0.16f), o(x * 0.6f, -0.88f), pen.lw * 0.35f)
    }
    shine(o(-0.24f, -0.26f), w * 0.1f, h * 0.05f, 0.5f)
}

internal fun DrawScope.thDrum(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val red = Color(0xFFFF5A4E)
    val gold = Color(0xFFFFC83D)
    val body = thSketch(w, h) { m(-0.44f, -0.7f); l(-0.44f, -0.16f); q(0f, 0.04f, 0.44f, -0.16f); l(0.44f, -0.7f); z() }
    inked(body, red, pen, outline = false)
    clipPath(body) {
        val zig = thSketch(w, h) { m(-0.44f, -0.62f); l(-0.3f, -0.18f); l(-0.15f, -0.6f); l(0f, -0.1f); l(0.15f, -0.6f); l(0.3f, -0.18f); l(0.44f, -0.62f) }
        drawPath(zig, Ink.line, style = Stroke(pen.lw * 1.8f, join = StrokeJoin.Round))
        drawPath(zig, Color.White, style = Stroke(pen.lw * 0.8f, join = StrokeJoin.Round))
        drawPath(thSketch(w, h) { m(-0.5f, -0.2f); q(0f, 0f, 0.5f, -0.2f) }, gold, style = Stroke(h * 0.12f))
        drawLine(Color.White.copy(alpha = 0.35f), o(-0.36f, -0.6f), o(-0.36f, -0.22f), w * 0.05f, StrokeCap.Round)
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val rim = Rect(-0.46f * w, -0.84f * h, 0.46f * w, -0.58f * h)
    inkedOval(rim, gold, pen, shade = false)
    drawOval(Color(0xFFFFF4DC), Offset(rim.left + w * 0.05f, rim.top + h * 0.04f), Size(rim.width - w * 0.1f, rim.height - h * 0.08f))
    drawOval(Color.White.copy(alpha = 0.6f), Offset(rim.left + w * 0.14f, rim.top + h * 0.06f), Size(rim.width * 0.4f, rim.height * 0.3f))
    capsule(o(-0.34f, -0.98f), o(0.16f, -0.7f), w * 0.05f, ThingInk.woodLight, pen)
    capsule(o(0.34f, -0.98f), o(-0.16f, -0.7f), w * 0.05f, ThingInk.woodLight, pen)
    inkedCircle(o(-0.34f, -0.98f), w * 0.045f, ThingInk.woodLight, pen, shade = false)
    inkedCircle(o(0.34f, -0.98f), w * 0.045f, ThingInk.woodLight, pen, shade = false)
}

private val BOOK_COL = longArrayOf(0xFFE8554E, 0xFF3D6BFF, 0xFF2E8B57, 0xFF8B5CF6)

/** A closed book standing up, with oblique depth: the pages show on top and at the side. */
internal fun DrawScope.thOpenBook(v: Int, page: Int, w: Float, h: Float, pen: Pen) {
    val cover = app.trollfoss.ui.theme.T.Grape
    inkedRound(Rect(-w * 0.62f, -h * 0.76f, w * 0.62f, -h * 0.05f), w * 0.08f, cover, pen)
    for (side in floatArrayOf(-1f, 1f)) {
        val left = if (side < 0) -w * 0.55f else 0f
        inkedRound(Rect(left, -h * 0.7f, left + w * 0.55f, -h * 0.09f), w * 0.04f, Color(0xFFFFF9E9), pen, shade = false)
        val c = Offset(side * w * 0.27f, -h * 0.43f)
        if ((page + v) % 2 == 0) twinkle(c, w * 0.13f, Color(0xFFF8C54C), 1f)
        else { drawCircle(Color(0xFFEE889F), w * 0.13f, c); drawCircle(Color.White, w * 0.05f, c) }
        drawLine(Ink.line.copy(alpha = 0.45f), Offset(left + w * 0.09f, -h * 0.2f), Offset(left + w * 0.46f, -h * 0.2f), strokeWidth = pen.lw * 0.5f)
    }
}

internal fun DrawScope.thBook(v: Int, w: Float, h: Float, pen: Pen) {
    val i = v.mod(4)
    val col = Color(BOOK_COL[i])
    val depth = w * 0.3f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val f = Rect(-0.46f * w, -h - dy * 0.98f + h * 0.02f, 0.46f * w - dx, -0.02f * h)
    val pages = ThingInk.cream
    val top = Path().apply { moveTo(f.left, f.top); lineTo(f.left + dx, f.top + dy); lineTo(f.right + dx, f.top + dy); lineTo(f.right, f.top); close() }
    val side = Path().apply { moveTo(f.right, f.top); lineTo(f.right + dx, f.top + dy); lineTo(f.right + dx, f.bottom + dy); lineTo(f.right, f.bottom); close() }
    drawPath(side, pages.darken(0.08f))
    clipPath(side) {
        for (k in 1..5) {
            val t = k / 6f
            drawLine(pages.darken(0.3f), Offset(f.right + dx * t, f.top + dy * t + h * 0.03f), Offset(f.right + dx * t, f.bottom + dy * t - h * 0.02f), pen.lw * 0.35f)
        }
        drawLine(col.darken(0.1f), Offset(f.right + dx, f.top + dy), Offset(f.right + dx, f.bottom + dy), pen.lw * 1.6f)
    }
    drawPath(top, pages)
    clipPath(top) { drawLine(col, Offset(f.left + dx, f.top + dy), Offset(f.right + dx, f.top + dy), pen.lw * 1.6f) }
    drawPath(side, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawPath(top, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    inkedRound(f, w * 0.05f, col, pen)
    val spine = Rect(f.left, f.top, f.left + f.width * 0.16f, f.bottom)
    drawRoundRect(col.darken(0.2f), spine.topLeft, spine.size, CornerRadius(w * 0.05f))
    drawLine(Ink.line, Offset(spine.right, f.top + pen.lw), Offset(spine.right, f.bottom - pen.lw), pen.lw * 0.5f)
    val gold = Color(0xFFFFD23F)
    val inset = Rect(spine.right + f.width * 0.1f, f.top + f.height * 0.1f, f.right - f.width * 0.1f, f.bottom - f.height * 0.1f)
    drawRoundRect(gold, inset.topLeft, inset.size, CornerRadius(w * 0.03f), style = Stroke(pen.lw * 0.5f))
    val c = inset.center
    val s = inset.width * 0.42f
    when (i) {
        0 -> thFill(thHeart(c.x, c.y, s * 1.1f), gold, pen)
        1 -> thFill(starPath(c, s * 0.6f, s * 0.28f), gold, pen)
        2 -> {
            val tree = Path().apply {
                moveTo(c.x, c.y - s * 0.62f); lineTo(c.x + s * 0.3f, c.y - s * 0.12f); lineTo(c.x + s * 0.14f, c.y - s * 0.12f)
                lineTo(c.x + s * 0.44f, c.y + s * 0.36f); lineTo(c.x - s * 0.44f, c.y + s * 0.36f); lineTo(c.x - s * 0.14f, c.y - s * 0.12f)
                lineTo(c.x - s * 0.3f, c.y - s * 0.12f); close()
            }
            thFill(tree, gold, pen)
            drawRect(gold, Offset(c.x - s * 0.07f, c.y + s * 0.36f), Size(s * 0.14f, s * 0.16f))
        }
        else -> {
            val moon = Path().apply {
                fillType = PathFillType.EvenOdd
                addOval(Rect(c.x - s * 0.45f, c.y - s * 0.45f, c.x + s * 0.45f, c.y + s * 0.45f))
                addOval(Rect(c.x - s * 0.2f, c.y - s * 0.62f, c.x + s * 0.62f, c.y + s * 0.3f))
            }
            clipPath(Path().apply { addOval(Rect(c.x - s * 0.45f, c.y - s * 0.45f, c.x + s * 0.45f, c.y + s * 0.45f)) }) { drawPath(moon, gold) }
            drawPath(starPath(Offset(c.x + s * 0.3f, c.y - s * 0.3f), s * 0.14f, s * 0.06f), gold)
        }
    }
    for (corner in 0 until 2) {
        val y = if (corner == 0) f.top else f.bottom
        val sgn = if (corner == 0) 1f else -1f
        val tri = Path().apply { moveTo(f.right, y + sgn * f.width * 0.2f); lineTo(f.right, y); lineTo(f.right - f.width * 0.2f, y); close() }
        clipPath(roundPath(f, w * 0.05f)) { drawPath(tri, gold) }
    }
    drawRoundRect(Ink.line, f.topLeft, f.size, CornerRadius(w * 0.05f), style = pen.stroke)
    shine(Offset(spine.right + f.width * 0.12f, f.top + f.height * 0.2f), w * 0.05f, h * 0.1f, 0.4f)
}

/** A phone: a thin slab with a little depth, a bright screen and app tiles. */
internal fun DrawScope.thPhone(w: Float, h: Float, pen: Pen) {
    val depth = w * 0.26f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val f = Rect(-0.46f * w, -h - dy + h * 0.02f, 0.46f * w - dx, -0.02f * h)
    val case = Color(0xFF3A3F5C)
    val front = roundPath(f, w * 0.18f)
    thExtrude(front, dx, dy, case.darken(0.2f), pen)
    inked(front, case, pen)
    val screen = Rect(f.left + f.width * 0.1f, f.top + f.height * 0.1f, f.right - f.width * 0.1f, f.bottom - f.height * 0.08f)
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF7FD8FF), Color(0xFFB58CFF)), screen.top, screen.bottom), screen.topLeft, screen.size, CornerRadius(w * 0.1f))
    val tile = screen.width * 0.3f
    for (k in 0 until 6) {
        val cx = screen.left + screen.width * (if (k % 2 == 0) 0.3f else 0.7f)
        val cy = screen.top + screen.height * (0.22f + (k / 2) * 0.24f)
        drawRoundRect(Color(PHONE_APPS[k]), Offset(cx - tile / 2, cy - tile / 2), Size(tile, tile), CornerRadius(tile * 0.3f))
    }
    drawLine(Color.White, Offset(screen.center.x - screen.width * 0.2f, screen.bottom - screen.height * 0.05f), Offset(screen.center.x + screen.width * 0.2f, screen.bottom - screen.height * 0.05f), pen.lw * 0.5f, StrokeCap.Round)
    drawCircle(Color(0xFF1E2238), w * 0.04f, Offset(f.center.x, f.top + f.height * 0.05f))
    clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(screen, CornerRadius(w * 0.1f))) }) {
        drawLine(Color.White.copy(alpha = 0.35f), Offset(screen.left, screen.top + screen.height * 0.5f), Offset(screen.right, screen.top + screen.height * 0.2f), w * 0.12f)
    }
}

private val PHONE_APPS = longArrayOf(0xFFFF5A4E, 0xFFFFD23F, 0xFF3BC46B, 0xFF4A8BFF, 0xFFFF9F43, 0xFFFF6FA8)
private val CAR_COL = longArrayOf(0xFFFF5A4E, 0xFF4AB3FF, 0xFFFFD23F)

/** A toy car from the side, with a little oblique depth so the roof shows. */
internal fun DrawScope.thToyCar(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(CAR_COL[v.mod(3)])
    val depth = h * 0.34f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val tyre = Color(0xFF2B2140)
    for (x in CAR_WHEELS) {
        val c = Offset(o(x, -0.2f).x + dx, o(x, -0.2f).y + dy)
        drawCircle(tyre, h * 0.22f, c)
    }
    val bodyRect = Rect(-0.48f * w, -0.62f * h, 0.4f * w, -0.2f * h)
    val cabin = thSketch(w, h) { m(-0.3f, -0.6f); c(-0.28f, -0.92f, 0.14f, -0.94f, 0.2f, -0.6f); z() }
    val silhouette = Path().apply {
        addRoundRect(androidx.compose.ui.geometry.RoundRect(bodyRect, CornerRadius(h * 0.18f)))
        addPath(cabin)
    }
    thExtrude(silhouette, dx, dy, col.lighten(0.12f), pen)
    inked(cabin, col, pen)
    clipPath(cabin) {
        val glass = Color(0xFFBFE8FF)
        drawRoundRect(glass, o(-0.22f, -0.84f), Size(0.18f * w, 0.24f * h), CornerRadius(w * 0.03f))
        drawRoundRect(glass, o(0.0f, -0.84f), Size(0.16f * w, 0.24f * h), CornerRadius(w * 0.03f))
        drawLine(Color.White.copy(alpha = 0.8f), o(-0.18f, -0.64f), o(-0.1f, -0.82f), pen.lw * 0.7f, StrokeCap.Round)
    }
    drawPath(cabin, Ink.line, style = pen.stroke)
    inkedRound(bodyRect, h * 0.18f, col, pen)
    drawLine(col.darken(0.3f), o(-0.02f, -0.58f), o(-0.02f, -0.26f), pen.lw * 0.5f)
    drawLine(col.darken(0.3f), o(0.06f, -0.5f), o(0.12f, -0.5f), pen.lw * 0.6f, StrokeCap.Round)
    thDot(o(-0.22f, -0.42f), h * 0.1f, Color.White, pen)
    drawPath(starPath(o(-0.22f, -0.42f), h * 0.075f, h * 0.034f), col)
    thDot(o(0.37f, -0.48f), h * 0.07f, ThingInk.sun, pen)
    thDot(o(-0.46f, -0.48f), h * 0.05f, ThingInk.tomato, pen)
    drawLine(Color.White.copy(alpha = 0.6f), o(-0.4f, -0.56f), o(0.3f, -0.56f), pen.lw * 0.8f, StrokeCap.Round)
    capsule(o(0.3f, -0.24f), o(0.42f, -0.24f), h * 0.06f, ThingInk.silver, pen)
    capsule(o(-0.48f, -0.24f), o(-0.4f, -0.24f), h * 0.06f, ThingInk.silver, pen)
    for (x in CAR_WHEELS) {
        val c = o(x, -0.2f)
        inkedCircle(c, h * 0.22f, tyre, pen, shade = false)
        drawCircle(ThingInk.silver, h * 0.1f, c)
        drawCircle(Ink.line, h * 0.1f, c, style = pen.thin)
        drawCircle(Color.White, h * 0.03f, Offset(c.x - h * 0.03f, c.y - h * 0.03f))
    }
}

private val CAR_WHEELS = floatArrayOf(-0.27f, 0.22f)

internal fun DrawScope.thSwimRing(w: Float, h: Float, pen: Pen) {
    val outer = Rect(-0.49f * w, -0.98f * h, 0.49f * w, -0.02f * h)
    val inner = Rect(-0.22f * w, -0.68f * h, 0.22f * w, -0.4f * h)
    val ring = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(outer)
        addOval(inner)
    }
    inked(ring, Color.White, pen, outline = false)
    val red = Color(0xFFFF4D4D)
    val c = Offset(0f, -0.5f * h)
    clipPath(ring) {
        for (k in 0 until 4) {
            val a0 = Math.toRadians(k * 90.0 + 20.0)
            val a1 = Math.toRadians(k * 90.0 + 65.0)
            val big = w
            val wedge = Path().apply {
                moveTo(c.x, c.y)
                lineTo(c.x + big * cos(a0).toFloat(), c.y + big * sin(a0).toFloat())
                lineTo(c.x + big * cos(a1).toFloat(), c.y + big * sin(a1).toFloat())
                close()
            }
            drawPath(wedge, red)
        }
        drawOval(Ink.line.copy(alpha = 0.18f), Offset(inner.left - w * 0.02f, inner.top - h * 0.06f), Size(inner.width + w * 0.04f, inner.height * 0.9f), style = Stroke(h * 0.08f))
        drawOval(Ink.line.copy(alpha = 0.16f), Offset(outer.left + w * 0.03f, outer.top + h * 0.1f), outer.size, style = Stroke(h * 0.12f))
    }
    drawPath(ring, Ink.line, style = pen.stroke)
    drawArc(Color.White.copy(alpha = 0.85f), 195f, 60f, false, Offset(outer.left + w * 0.06f, outer.top + h * 0.1f), Size(outer.width * 0.8f, outer.height * 0.7f), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    inkedRound(rect(0.34f * w, -0.2f * h, 0.07f * w, 0.1f * h), w * 0.02f, Color.White, pen, shade = false)
}

/** A small firework rocket; below zero [cook] means it is flying, with a flame. */
internal fun DrawScope.thRocket(cook: Float, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val flying = cook < 0f
    if (flying) {
        val flick = 1f + 0.18f * sin(pen.t * 40f) + 0.1f * sin(pen.t * 23f)
        val flame = thSketch(w, h) { m(-0.26f, -0.4f); q(-0.2f, -0.2f * flick, 0f, (-0.42f + 0.34f * flick)); q(0.2f, -0.2f * flick, 0.26f, -0.4f); z() }
        drawPath(flame, Color(0xFFFF8A2B))
        drawPath(thSketch(w, h) { m(-0.14f, -0.4f); q(-0.1f, -0.28f, 0f, (-0.4f + 0.2f * flick)); q(0.1f, -0.28f, 0.14f, -0.4f); z() }, Color(0xFFFFE680))
        for (k in 0 until 4) {
            val ph = (pen.t * 3f + k * 0.25f) % 1f
            drawCircle(Color(0xFFFFD23F).copy(alpha = 1f - ph), w * 0.05f, o(sin(k * 2.1f + pen.t * 9f) * 0.3f, -0.3f + ph * 0.3f))
        }
    } else {
        capsule(o(0f, -0.4f), o(0f, -0.01f), w * 0.12f, ThingInk.woodLight, pen)
        drawPath(thSketch(w, h) { m(0.1f, -0.42f); q(0.3f, -0.38f, 0.28f, -0.3f) }, Ink.line, style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
    }
    val blue = Color(0xFF3D6BFF)
    for (s in SIDES) {
        val fin = thSketch(w, h) { m(s * 0.26f, -0.58f); l(s * 0.48f, -0.4f); l(s * 0.46f, -0.33f); l(s * 0.26f, -0.42f); z() }
        inked(fin, blue, pen, shade = false)
    }
    val body = Rect(-0.3f * w, -0.8f * h, 0.3f * w, -0.4f * h)
    inkedRound(body, w * 0.1f, Color(0xFFFF5A4E), pen, shade = false)
    clipPath(roundPath(body, w * 0.1f)) {
        drawRect(Color.White, o(-0.5f, -0.66f), Size(w, 0.05f * h))
        drawRect(ThingInk.sun, o(-0.5f, -0.54f), Size(w, 0.04f * h))
        drawRect(Ink.line.copy(alpha = 0.2f), o(0.12f, -0.8f), Size(0.2f * w, 0.4f * h))
        drawLine(Color.White.copy(alpha = 0.6f), o(-0.18f, -0.76f), o(-0.18f, -0.44f), w * 0.07f, StrokeCap.Round)
    }
    drawRoundRect(Ink.line, body.topLeft, body.size, CornerRadius(w * 0.1f), style = pen.stroke)
    val cone = thSketch(w, h) { m(-0.3f, -0.79f); q(-0.26f, -0.92f, 0f, -0.995f); q(0.26f, -0.92f, 0.3f, -0.79f); z() }
    inked(cone, ThingInk.sun, pen)
    drawPath(starPath(o(0f, -0.6f), w * 0.1f, w * 0.045f), Color.White)
}

internal fun DrawScope.thSnowball(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val path = blobPath(
        -0.46f * w, -0.5f * h, -0.4f * w, -0.86f * h, -0.02f * w, -1.0f * h, 0.36f * w, -0.9f * h, 0.5f * w, -0.54f * h,
        0.42f * w, -0.14f * h, 0.04f * w, 0.0f * h, -0.36f * w, -0.1f * h,
    )
    val b = path.getBounds()
    val s = b.width * 0.12f
    drawPath(path, ThingInk.snowShade)
    clipPath(path) {
        translate(-s * 0.5f, -s) { drawPath(path, ThingInk.snow) }
        thSheen(o(0f, -0.5f), w * 0.5f, 0.6f)
    }
    drawPath(path, Ink.line, style = pen.stroke)
    val clump = ThingInk.snowShade
    drawArc(clump, 20f, 120f, false, o(-0.3f, -0.62f), Size(0.3f * w, 0.24f * h), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    drawArc(clump, 30f, 110f, false, o(0.02f, -0.46f), Size(0.32f * w, 0.26f * h), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    drawArc(clump, 200f, 100f, false, o(-0.16f, -0.3f), Size(0.26f * w, 0.2f * h), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    val sparkle = Color(0xFF9CC4F0)
    drawCircle(sparkle, w * 0.02f, o(0.18f, -0.34f))
    drawCircle(sparkle, w * 0.015f, o(-0.12f, -0.22f))
    drawCircle(sparkle, w * 0.018f, o(0.26f, -0.66f))
    twinkle(o(-0.2f, -0.66f), w * 0.1f, Color.White)
}

/** A wooden sled seen from the side with oblique depth: the far runner and the slatted seat show. */
internal fun DrawScope.thSled(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val depth = w * 0.24f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val runnerCol = Color(0xFFD2443A)
    val runner = thSketch(w, h) { m(-0.48f, -0.08f); l(0.14f, -0.08f); c(0.26f, -0.08f, 0.34f, -0.2f, 0.33f, -0.36f); c(0.32f, -0.5f, 0.25f, -0.54f, 0.2f, -0.48f) }
    val rw = h * 0.1f
    translate(dx, dy) {
        drawPath(runner, Ink.line, style = Stroke(rw + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(runner, runnerCol.darken(0.35f), style = Stroke(rw, cap = StrokeCap.Round))
    }
    val seat = Rect(-0.48f * w, -0.6f * h, 0.22f * w, -0.46f * h)
    val top = Path().apply { moveTo(seat.left, seat.top); lineTo(seat.left + dx, seat.top + dy); lineTo(seat.right + dx, seat.top + dy); lineTo(seat.right, seat.top); close() }
    val side = Path().apply { moveTo(seat.right, seat.top); lineTo(seat.right + dx, seat.top + dy); lineTo(seat.right + dx, seat.bottom + dy); lineTo(seat.right, seat.bottom); close() }
    drawPath(side, ThingInk.wood.darken(0.25f))
    drawPath(side, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawPath(top, ThingInk.woodLight)
    for (k in 1..2) {
        val t = k / 3f
        drawLine(ThingInk.woodDark, Offset(seat.left + dx * t, seat.top + dy * t), Offset(seat.right + dx * t, seat.top + dy * t), pen.lw * 0.6f)
    }
    for (k in 0 until 3) {
        val t = (k + 0.5f) / 3f
        drawCircle(ThingInk.bark, pen.lw * 0.35f, Offset(seat.left + seat.width * 0.08f + dx * t, seat.top + dy * t))
        drawCircle(ThingInk.bark, pen.lw * 0.35f, Offset(seat.right - seat.width * 0.08f + dx * t, seat.top + dy * t))
    }
    drawPath(top, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    for (x in SLED_POSTS) capsule(o(x, -0.1f), o(x, -0.48f), w * 0.035f, ThingInk.woodDark, pen)
    inkedRound(seat, h * 0.03f, ThingInk.wood, pen)
    drawLine(ThingInk.woodDark, o(-0.44f, -0.53f), o(0.18f, -0.53f), pen.lw * 0.45f)
    drawPath(runner, Ink.line, style = Stroke(rw + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(runner, runnerCol, style = Stroke(rw, cap = StrokeCap.Round))
    drawPath(thSketch(w, h) { m(-0.46f, -0.04f); l(0.14f, -0.04f) }, ThingInk.silver, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
    val rope = thSketch(w, h) { m(0.3f, -0.42f); c(0.52f, -0.3f, 0.5f, -0.08f, 0.4f, -0.14f) }
    drawPath(rope, Ink.line, style = Stroke(pen.lw * 1.8f, cap = StrokeCap.Round))
    drawPath(rope, ThingInk.sun, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
}

private val SLED_POSTS = floatArrayOf(-0.38f, -0.1f, 0.12f)

/** A black tyre with tread and a silver rim. */
internal fun DrawScope.thTire(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h * 0.5f)
    val r = w * 0.48f
    val rubber = Color(0xFF3A3340)
    inkedCircle(c, r, rubber, pen)
    for (k in 0 until 16) {
        val a = k * (Math.PI / 8)
        val ca = cos(a).toFloat()
        val sa = sin(a).toFloat()
        drawLine(Color(0xFF1E1A26), Offset(c.x + ca * r * 0.84f, c.y + sa * r * 0.84f), Offset(c.x + ca * r * 0.98f, c.y + sa * r * 0.98f), pen.lw * 0.8f, StrokeCap.Round)
    }
    drawCircle(Color(0xFF5A5566), r * 0.74f, c, style = Stroke(pen.lw * 0.5f))
    inkedCircle(c, r * 0.5f, ThingInk.silver, pen)
    drawCircle(ThingInk.steel, r * 0.36f, c, style = Stroke(pen.lw * 0.5f))
    for (k in 0 until 5) {
        val a = Math.toRadians(k * 72.0 - 90.0)
        drawCircle(ThingInk.steel.darken(0.3f), r * 0.05f, Offset(c.x + (cos(a) * r * 0.27f).toFloat(), c.y + (sin(a) * r * 0.27f).toFloat()))
    }
    drawCircle(Color(0xFF2B2140), r * 0.12f, c)
    thSheen(c, r, 0.22f)
    drawArc(Color.White.copy(alpha = 0.35f), 200f, 50f, false, Offset(c.x - r * 0.9f, c.y - r * 0.9f), Size(r * 1.8f, r * 1.8f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
}
