package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.GroundRules
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The library of Storstova in oblique 3D: the tall bookcases, the bookcase with the red book, the rolling
// ladder, the writing desk, the talking book on its lectern and the marble bust.

private val BOOKS = intArrayOf(
    0xFF8E2F3E.toInt(), 0xFF2E5E4A.toInt(), 0xFF2B3F6B.toInt(), 0xFFD9A93E.toInt(), 0xFF2D6C73.toInt(),
    0xFF8A5A3C.toInt(), 0xFFF2E6CC.toInt(), 0xFFC8553D.toInt(), 0xFF5A3A78.toInt(), 0xFF3A3A44.toInt(),
)

/**
 * The inside of a bookcase of [w] units across, from the floor up to [top]: five boards, and on each a row
 * of books of different heights, leaning ones, flat stacks and now and then a trinket. Drawn in front of
 * the back panel at pixel origin (0, 0) = bottom centre.
 */
private fun DrawScope.grBooksInCase(u: Float, w: Float, pen: Pen, salt: Int, redBook: Boolean) {
    val boards = floatArrayOf(-0.04f, -0.18f, -0.32f, -0.46f, -0.60f)
    val inner = w - 0.04f
    for (row in 0 until 5) {
        val base = boards[row]
        val gap = 0.14f
        var x = -inner / 2f
        var i = 0
        while (x < inner / 2f - 0.012f) {
            val h = hash01(row * 31 + i, salt)
            val bw = 0.012f + 0.014f * hash01(row * 31 + i, salt + 1)
            if (x + bw > inner / 2f) break
            // A trinket or a flat stack now and then.
            val r = hash01(row * 31 + i, salt + 2)
            if (r > 0.93f && x + 0.05f < inner / 2f) {
                grTrinket(Offset((x + 0.025f) * u, base * u), u, pen, (row + i + salt) % 4)
                x += 0.055f
                i++
                continue
            }
            if (r < 0.1f && x + 0.06f < inner / 2f) {
                // A flat stack of three books.
                for (k in 0 until 3) {
                    val c = Color(BOOKS[((row + i + k) * 3 + salt).mod(BOOKS.size)])
                    val rr = Rect((x + 0.002f * k) * u, (base - 0.014f * (k + 1)) * u, (x + 0.052f - 0.003f * k) * u, (base - 0.014f * k) * u)
                    inkedRound(rr, 0.002f * u, c, pen, shade = false)
                    drawLine(Color.White.copy(alpha = 0.4f), Offset(rr.left + 0.005f * u, rr.center.y), Offset(rr.left + 0.012f * u, rr.center.y), pen.lw * 0.6f)
                }
                x += 0.058f
                i++
                continue
            }
            val bh = 0.082f + 0.04f * h
            val c = Color(BOOKS[((row * 5 + i * 7 + salt).mod(BOOKS.size))])
            val lean = if (hash01(row * 31 + i, salt + 3) > 0.9f) 0.35f else 0f
            val rect = Rect(x * u, (base - bh) * u, (x + bw) * u, base * u)
            if (lean > 0f) {
                rotate(lean * 12f, Offset(rect.right, rect.bottom)) { inkedRound(rect, 0.002f * u, c, pen, shade = false) }
                x += bw + 0.02f
            } else {
                inkedRound(rect, 0.002f * u, c, pen, shade = false)
                val band = c.darken(0.35f)
                drawLine(band, Offset(rect.left, rect.top + 0.012f * u), Offset(rect.right, rect.top + 0.012f * u), pen.lw * 0.7f)
                drawLine(GrC.gold, Offset(rect.left + 0.001f * u, rect.top + 0.024f * u), Offset(rect.right - 0.001f * u, rect.top + 0.024f * u), pen.lw * 0.6f)
                if (hash01(row * 31 + i, salt + 4) > 0.5f) drawLine(band, Offset(rect.left, rect.bottom - 0.014f * u), Offset(rect.right, rect.bottom - 0.014f * u), pen.lw * 0.7f)
                x += bw
            }
            i++
        }
    }
}

/** A small thing standing on a shelf: an hourglass, a candle, a brass telescope or a little plant. */
private fun DrawScope.grTrinket(at: Offset, u: Float, pen: Pen, kind: Int) {
    when (kind) {
        0 -> {
            // An hourglass.
            val top = Offset(at.x, at.y - 0.06f * u)
            val g = Path().apply {
                moveTo(at.x - 0.014f * u, top.y)
                lineTo(at.x + 0.014f * u, top.y)
                lineTo(at.x + 0.003f * u, at.y - 0.03f * u)
                lineTo(at.x + 0.014f * u, at.y)
                lineTo(at.x - 0.014f * u, at.y)
                lineTo(at.x - 0.003f * u, at.y - 0.03f * u)
                close()
            }
            drawPath(g, Color(0xFFE6F4FB))
            drawPath(Path().apply { moveTo(at.x - 0.01f * u, at.y); lineTo(at.x + 0.01f * u, at.y); lineTo(at.x, at.y - 0.016f * u); close() }, Color(0xFFE8B84A))
            drawPath(g, Ink.line, style = pen.thin)
            drawRect(GrC.walnutLight, Offset(at.x - 0.017f * u, top.y - 0.005f * u), Size(0.034f * u, 0.006f * u))
            drawRect(GrC.walnutLight, Offset(at.x - 0.017f * u, at.y), Size(0.034f * u, 0.006f * u))
        }
        1 -> {
            // A brass telescope on a tiny stand.
            capsule(Offset(at.x - 0.02f * u, at.y - 0.02f * u), Offset(at.x + 0.022f * u, at.y - 0.05f * u), 0.012f * u, GrC.brass, pen)
            capsule(Offset(at.x + 0.014f * u, at.y - 0.044f * u), Offset(at.x + 0.034f * u, at.y - 0.058f * u), 0.017f * u, GrC.brassDark, pen)
            drawLine(Ink.line, Offset(at.x - 0.008f * u, at.y - 0.012f * u), Offset(at.x - 0.016f * u, at.y), pen.lw * 1.6f)
            drawLine(Ink.line, Offset(at.x + 0.008f * u, at.y - 0.012f * u), Offset(at.x + 0.016f * u, at.y), pen.lw * 1.6f)
        }
        2 -> {
            // A candle in a brass stick.
            inkedRound(Rect(at.x - 0.008f * u, at.y - 0.012f * u, at.x + 0.008f * u, at.y), 0.002f * u, GrC.brass, pen, shade = false)
            inkedRound(Rect(at.x - 0.005f * u, at.y - 0.05f * u, at.x + 0.005f * u, at.y - 0.012f * u), 0.002f * u, GrC.ivory, pen, shade = false)
            grFlame(Offset(at.x, at.y - 0.05f * u), 0.018f * u, pen, at.x)
        }
        else -> {
            // A little plant in a pot.
            inkedRound(Rect(at.x - 0.013f * u, at.y - 0.022f * u, at.x + 0.013f * u, at.y), 0.003f * u, GrC.terracotta, pen, shade = false)
            for (k in -1..1) {
                val leaf = Path().apply {
                    moveTo(at.x, at.y - 0.022f * u)
                    quadraticTo(at.x + k * 0.02f * u, at.y - 0.05f * u, at.x + k * 0.025f * u, at.y - 0.055f * u)
                    quadraticTo(at.x + k * 0.008f * u, at.y - 0.04f * u, at.x, at.y - 0.022f * u)
                }
                inked(leaf, GrC.leaf, pen, shade = false)
            }
        }
    }
}

/** The carcass of a tall bookcase: dark back panel, two side walls, a cornice, a plinth and the brass rail. */
private fun DrawScope.grCaseFrame(u: Float, w: Float, h: Float, d: Float, pen: Pen, wood: Color) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    grShadow(u, w, d)
    // The back and the top, then the open front.
    fxBox(u, -w / 2f, -h, w / 2f, 0f, d, wood.darken(0.35f), pen, rad = 0.004f)
    val back = Rect(-w / 2f * u + 0.012f * u, -h * u + 0.016f * u, w / 2f * u - 0.012f * u, -0.03f * u)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2B1C16), Color(0xFF3F2A20)), back.top, back.bottom), back.topLeft, back.size)
    // The boards seen from the front, with a lit top edge.
    for (b in floatArrayOf(-0.04f, -0.18f, -0.32f, -0.46f, -0.60f, -0.74f)) {
        inkedRound(Rect((-w / 2f + 0.008f) * u, b * u, (w / 2f - 0.008f) * u, (b + 0.012f) * u), 0.0015f * u, wood.lighten(0.05f), pen, shade = false)
    }
}

private fun DrawScope.grCaseTrim(u: Float, w: Float, h: Float, pen: Pen, wood: Color) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    // Side posts, a cornice with a gilded line, a plinth.
    inkedRound(Rect(-w / 2f * u, -h * u, (-w / 2f + 0.012f) * u, 0f), 0.002f * u, wood, pen, shade = false)
    inkedRound(Rect((w / 2f - 0.012f) * u, -h * u, w / 2f * u, 0f), 0.002f * u, wood, pen, shade = false)
    inkedRound(Rect((-w / 2f - 0.006f) * u, (-h - 0.018f) * u, (w / 2f + 0.006f) * u, -h * u + 0.008f * u), 0.003f * u, wood.lighten(0.08f), pen)
    drawLine(GrC.gold, Offset((-w / 2f) * u, (-h - 0.008f) * u), Offset((w / 2f) * u, (-h - 0.008f) * u), pen.lw * 0.8f)
    inkedRound(Rect((-w / 2f - 0.004f) * u, -0.026f * u, (w / 2f + 0.004f) * u, 0f), 0.002f * u, wood.darken(0.1f), pen)
    // The brass rail the ladder rolls on, with its brackets.
    val ry = -0.66f
    drawLine(Ink.line, Offset((-w / 2f - 0.01f) * u, ry * u), Offset((w / 2f + 0.01f) * u, ry * u), 0.007f * u + pen.lw * 2f, StrokeCap.Round)
    drawLine(GrC.brass, Offset((-w / 2f - 0.01f) * u, ry * u), Offset((w / 2f + 0.01f) * u, ry * u), 0.007f * u, StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.5f), Offset((-w / 2f) * u, (ry - 0.0015f) * u), Offset((w / 2f) * u, (ry - 0.0015f) * u), pen.lw * 0.5f)
    for (x in floatArrayOf(-w / 2f + 0.03f, w / 2f - 0.03f)) {
        drawLine(GrC.brassDark, Offset(x * u, ry * u), Offset(x * u, (ry + 0.012f) * u), pen.lw * 2f)
    }
}

internal fun DrawScope.grBookshelf(f: Fixture, u: Float, pen: Pen) {
    val w = 0.32f
    val h = 0.74f
    val wood = GrC.walnut
    grCaseFrame(u, w, h, 0.12f, pen, wood)
    grBooksInCase(u, w, pen, 100 + f.variant * 17, false)
    grCaseTrim(u, w, h, pen, wood)
    if (f.anim > 0.05f) twinkle(Offset(0.1f * u, -0.4f * u), 0.02f * u * f.anim, Color.White, f.anim)
}

// ------------------------------------------------------------------------------------------------ the secret bookcase

/** The bookcase with the red book. Its spec is the lower part (0.30 x 0.46); it is drawn as tall as the others. */
internal fun DrawScope.grSecretShelf(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val w = 0.3f
    val h = 0.74f
    val wood = GrC.walnut
    val open = when {
        f.mode == 1 && f.timer > 0f -> 1f - (f.timer / 1.8f).coerceIn(0f, 1f)
        f.mode == 1 -> 1f
        else -> 0f
    }
    val o = open * open * (3f - 2f * open)
    grCaseFrame(u, w, h, 0.12f, pen, wood)
    // The way behind: a dark stair going up, with a warm lantern glow.
    if (o > 0.01f) {
        clipRect(-w / 2f * u + 0.012f * u, -h * u + 0.016f * u, w / 2f * u - 0.012f * u, -0.03f * u) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF120D24), Color(0xFF3A2A4A)), -h * u, 0f), p(-w / 2f, -h), Size(w * u, h * u))
            for (k in 0..7) {
                val y = -0.05f - k * 0.075f
                val x0 = -0.12f + k * 0.012f
                drawRect(Color(0xFF6A5A7A), p(x0, y), Size((0.24f - k * 0.024f) * u, 0.016f * u))
                drawRect(Color(0xFF2A2040), p(x0, y + 0.016f), Size((0.24f - k * 0.024f) * u, 0.02f * u))
            }
            grGlow(p(0.04f, -0.22f), 0.2f * u, pen, 0.7f * o, Color(0xFFFFD98A))
            drawCircle(Color(0xFFFFE9A8), 0.012f * u, p(0.07f, -0.5f))
        }
    }
    // The swinging front: the books squeeze together as the case turns away on its left edge.
    val sx = 1f - 0.86f * o
    run {
        scale(sx, 1f, pivot = p(-w / 2f, 0f)) {
            drawRect(wood.darken(0.35f), p(-w / 2f + 0.012f, -h + 0.016f), Size((w - 0.024f) * u, (h - 0.046f) * u))
            grBooksInCase(u, w, pen, 100 + 17 * 5, true)
            // The red book: it sits a little proud of the others, with a gold clasp and a twinkle now and then.
            if (f.mode != 1 || o < 0.5f) {
                val rb = Rect(0.03f * u, -0.395f * u, 0.052f * u, -0.3f * u)
                val tilt = if (f.timer > 0f && f.mode == 1) 14f * (1f - o) else 0f
                rotate(tilt, Offset(rb.left, rb.bottom)) {
                    inkedRound(Rect(rb.left, rb.top - 0.004f * u, rb.right + 0.003f * u, rb.bottom), 0.002f * u, Color(0xFFD2443A), pen)
                    drawLine(GrC.gold, Offset(rb.left, rb.top + 0.016f * u), Offset(rb.right + 0.003f * u, rb.top + 0.016f * u), pen.lw * 0.9f)
                    drawLine(GrC.gold, Offset(rb.left, rb.top + 0.03f * u), Offset(rb.right + 0.003f * u, rb.top + 0.03f * u), pen.lw * 0.9f)
                    val hh = fxHeart(rb.center.x + 0.0015f * u, rb.bottom - 0.022f * u, 0.0065f * u)
                    drawPath(hh, GrC.gold)
                }
                val gl = max(0f, sin(pen.t * 2.2f))
                if (gl > 0.7f && f.mode != 1) twinkle(Offset(rb.right + 0.006f * u, rb.top + 0.01f * u), 0.018f * u, Color.White, (gl - 0.7f) * 3.3f)
            }
        }
    }
    grCaseTrim(u, w, h, pen, wood)
    // A wisp of dust and a little glow from the opening.
    if (o in 0.05f..0.95f) {
        for (k in 0 until 4) {
            val ph = fxFrac(pen.t * 1.2f + k * 0.25f)
            drawCircle(Color.White.copy(alpha = 0.5f * (1f - ph)), 0.012f * u * (1f + ph), p(-0.1f + k * 0.07f, -0.05f - ph * 0.12f))
        }
    }
}

// ------------------------------------------------------------------------------------------------ the ladder

internal fun DrawScope.grLadder(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val wood = GrC.oak
    val moving = abs(f.shiftX - GroundRules.LADDER_STOPS[f.mode.coerceIn(0, GroundRules.LADDER_STOPS.size - 1)]) > 0.004f
    grShadow(u, 0.12f, 0.06f)
    // Two rails that lean in a little at the top, and rungs.
    val bottomHalf = 0.052f
    val topHalf = 0.04f
    val topY = -0.66f
    for (s in -1..1 step 2) {
        val a = p(s * bottomHalf, -0.012f)
        val b = p(s * topHalf, topY)
        drawLine(Ink.line, a, b, 0.012f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(wood, a, b, 0.012f * u, StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.4f), Offset(a.x - 0.003f * u, a.y), Offset(b.x - 0.003f * u, b.y), pen.lw * 0.5f)
        // A brass caster at the foot.
        inkedCircle(p(s * (bottomHalf + 0.002f), 0f).let { Offset(it.x, it.y - 0.007f * u) }, 0.008f * u, GrC.brass, pen, shade = false)
    }
    for (k in 0 until 9) {
        val y = -0.06f - k * 0.0655f
        val half = mix(bottomHalf, topHalf, (-y - 0.012f) / (-topY - 0.012f))
        val a = p(-half, y)
        val b = p(half, y)
        drawLine(Ink.line, a, b, 0.008f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(wood.lighten(0.12f), a, b, 0.008f * u, StrokeCap.Round)
    }
    // A little reading seat across the rails, where somebody can sit with a book.
    val seat = Rect(-0.055f * u, -0.452f * u, 0.055f * u, -0.436f * u)
    inkedRound(seat, 0.003f * u, GrC.walnutLight, pen)
    drawLine(GrC.brass, Offset(seat.left + 0.004f * u, seat.center.y), Offset(seat.right - 0.004f * u, seat.center.y), pen.lw * 0.7f)
    // Hooks over the rail, with little wheels.
    for (s in -1..1 step 2) {
        val hook = Path().apply {
            moveTo(p(s * topHalf, topY + 0.02f).x, p(0f, topY + 0.02f).y)
            lineTo(p(s * topHalf, topY - 0.004f).x, p(0f, topY - 0.004f).y)
            quadraticTo(p(s * topHalf, topY - 0.022f).x, p(0f, topY - 0.022f).y, p(s * (topHalf + 0.016f), topY - 0.012f).x, p(0f, topY - 0.012f).y)
        }
        drawPath(hook, Ink.line, style = Stroke(0.007f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(hook, GrC.brass, style = Stroke(0.007f * u, cap = StrokeCap.Round))
        val wheel = p(s * (topHalf + 0.016f), topY - 0.012f)
        inkedCircle(wheel, 0.008f * u, GrC.brassDark, pen, shade = false)
        if (moving) drawCircle(Color.White.copy(alpha = 0.5f), 0.004f * u, Offset(wheel.x + cos(pen.t * 30f) * 0.003f * u, wheel.y + sin(pen.t * 30f) * 0.003f * u))
    }
}

// ------------------------------------------------------------------------------------------------ the writing desk

internal fun DrawScope.grDesk(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.16f
    val on = f.on
    val wood = GrC.walnut
    grShadow(u, 0.36f, d)
    // The light of the green lamp.
    if (on) grGlow(p(0.12f, -0.2f), 0.24f * u, pen, 0.3f)
    // Two drawer pedestals and a knee space between them, a leather top.
    fxBox(u, -0.17f, -0.145f, -0.07f, 0f, d, wood, pen, rad = 0.004f)
    fxBox(u, 0.07f, -0.145f, 0.17f, 0f, d, wood, pen, rad = 0.004f)
    fxBox(u, -0.07f, -0.145f, 0.07f, -0.115f, d - 0.03f, wood.darken(0.05f), pen, rad = 0.003f, z = 0.02f)
    for (s in 0..1) {
        val x0 = if (s == 0) -0.16f else 0.08f
        for (k in 0..2) {
            val r = Rect(x0 * u, (-0.138f + k * 0.045f) * u, (x0 + 0.08f) * u, (-0.098f + k * 0.045f) * u)
            drawRoundRect(wood.darken(0.2f), r.topLeft, r.size, androidx.compose.ui.geometry.CornerRadius(0.003f * u), style = pen.thin)
            grKnob(Offset(r.center.x, r.center.y), 0.0045f * u, pen)
        }
    }
    fxBox(u, -0.18f, -0.16f, 0.18f, -0.145f, d + 0.012f, wood.lighten(0.04f), pen, rad = 0.003f, z = -0.006f)
    // The green leather top with a gold border.
    val top = fxFlat(u, -0.165f, 0.165f, -0.1605f, 0.012f, d - 0.012f, 0.002f)
    drawPath(top, Color(0xFF3F7A5A))
    drawPath(top, GrC.gold, style = Stroke(pen.lw * 0.7f))
    // Papers, the inkwell and the quill.
    val paper = Path().apply {
        val a = q(-0.1f, -0.1608f, 0.05f)
        val b = q(-0.03f, -0.1608f, 0.05f)
        val c = q(-0.025f, -0.1608f, 0.12f)
        val e = q(-0.095f, -0.1608f, 0.12f)
        moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(e.x, e.y); close()
    }
    drawPath(paper, Color(0xFFF7F3EC))
    drawPath(paper, Ink.line, style = pen.thin)
    // The scribble: it grows while the quill writes.
    val grow = if (f.timer > 0f) 1f - (f.timer / 1.4f).coerceIn(0f, 1f) else 1f
    val scrib = Path().apply {
        val s0 = q(-0.09f, -0.1612f, 0.075f)
        moveTo(s0.x, s0.y)
        val n = (6 * grow).toInt()
        for (k in 1..n) {
            val c = q(-0.09f + k * 0.011f, -0.1612f, 0.075f + (if (k % 2 == 0) 0.012f else 0.02f))
            lineTo(c.x, c.y)
        }
    }
    drawPath(scrib, Color(0xFF2B3F6B), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    val well = q(0.0f, -0.1605f, 0.1f)
    inkedCircle(Offset(well.x, well.y - 0.007f * u), 0.0085f * u, Color(0xFF2B3F6B), pen, shade = false)
    // The quill, in the well or racing across the page.
    val qx = if (f.timer > 0f) -0.08f + 0.05f * grow + sin(pen.t * 14f) * 0.004f else 0.012f
    val tip = q(qx, -0.1605f, if (f.timer > 0f) 0.09f else 0.085f)
    val feather = Offset(tip.x + 0.03f * u, tip.y - 0.075f * u)
    drawLine(Ink.line, tip, feather, 0.004f * u + pen.lw * 2f, StrokeCap.Round)
    drawLine(Color(0xFFF2EEE6), tip, feather, 0.004f * u, StrokeCap.Round)
    val vane = Path().apply {
        moveTo(feather.x, feather.y)
        quadraticTo(feather.x + 0.016f * u, feather.y + 0.02f * u, feather.x + 0.004f * u, feather.y + 0.05f * u)
        quadraticTo(feather.x - 0.004f * u, feather.y + 0.02f * u, feather.x, feather.y)
    }
    drawPath(vane, Color(0xFFF2EEE6))
    drawPath(vane, Ink.line, style = pen.thin)
    // The banker's lamp: brass stem and a green dome shade.
    val lamp = q(0.12f, -0.1605f, 0.09f)
    capsule(lamp, Offset(lamp.x, lamp.y - 0.04f * u), 0.006f * u, GrC.brass, pen)
    inkedRound(Rect(lamp.x - 0.014f * u, lamp.y - 0.008f * u, lamp.x + 0.014f * u, lamp.y), 0.003f * u, GrC.brassDark, pen, shade = false)
    val shade = Path().apply {
        moveTo(lamp.x - 0.026f * u, lamp.y - 0.04f * u)
        quadraticTo(lamp.x - 0.026f * u, lamp.y - 0.07f * u, lamp.x, lamp.y - 0.07f * u)
        quadraticTo(lamp.x + 0.026f * u, lamp.y - 0.07f * u, lamp.x + 0.026f * u, lamp.y - 0.04f * u)
        close()
    }
    inked(shade, if (on) Color(0xFF4FA878) else Color(0xFF2E6A4A), pen)
    if (on) drawOval(Color.White.copy(alpha = 0.85f), Offset(lamp.x - 0.011f * u, lamp.y - 0.043f * u), Size(0.022f * u, 0.006f * u))
}

// ------------------------------------------------------------------------------------------------ the talking book

internal fun DrawScope.grLectern(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val talking = f.timer > 0f
    grShadow(u, 0.12f, 0.08f)
    val wood = GrC.walnutLight
    // Foot, column and a sloping desk.
    fxBox(u, -0.05f, -0.018f, 0.05f, 0f, 0.08f, GrC.walnut, pen, rad = 0.004f)
    val b = q(0f, -0.018f, 0.04f)
    val t = q(0f, -0.15f, 0.04f)
    fxCyl(b.x, b.y, t.y, 0.016f * u, 0.011f * u, wood, pen, cap = false)
    grKnob(Offset(t.x, t.y - 0.003f * u), 0.014f * u, pen)
    val desk = Path().apply {
        val a = q(-0.058f, -0.158f, 0.0f)
        val bb = q(0.058f, -0.158f, 0.0f)
        val c = q(0.058f, -0.205f, 0.075f)
        val e = q(-0.058f, -0.205f, 0.075f)
        moveTo(a.x, a.y); lineTo(bb.x, bb.y); lineTo(c.x, c.y); lineTo(e.x, e.y); close()
    }
    inked(desk, wood, pen)
    inkedRound(Rect(-0.058f * u, -0.168f * u, 0.058f * u, -0.152f * u), 0.003f * u, GrC.walnut, pen, shade = false)
    // The book: green leather cover, cream pages, and a face on the pages.
    val bookC = q(0f, -0.19f, 0.03f)
    translate(bookC.x, bookC.y) {
        val cover = Path().apply {
            moveTo(-0.058f * u, 0.012f * u); lineTo(0.058f * u, 0.012f * u); lineTo(0.062f * u, -0.082f * u); lineTo(-0.062f * u, -0.082f * u); close()
        }
        inked(cover, Color(0xFF2E6A4A), pen)
        val pagesL = Path().apply { moveTo(-0.054f * u, 0.006f * u); lineTo(-0.0f, 0.012f * u); lineTo(0f, -0.076f * u); lineTo(-0.056f * u, -0.074f * u); close() }
        val pagesR = Path().apply { moveTo(0.054f * u, 0.006f * u); lineTo(0f, 0.012f * u); lineTo(0f, -0.076f * u); lineTo(0.056f * u, -0.074f * u); close() }
        drawPath(pagesL, Color(0xFFF7EEDC))
        drawPath(pagesR, Color(0xFFFFF8E8))
        drawPath(pagesL, Ink.line, style = pen.thin)
        drawPath(pagesR, Ink.line, style = pen.thin)
        for (k in 0..2) {
            drawLine(Color(0xFFD8C9A8), Offset(-0.052f * u, (0.002f - k * 0.002f) * u), Offset(-0.003f * u, (0.008f - k * 0.002f) * u), pen.lw * 0.4f)
        }
        // The eyes: two big round ones that blink and look about.
        val blink = fxFrac(pen.t * 0.3f) > 0.95f
        val look = sin(pen.t * 0.9f) * 0.004f * u
        for (s in -1..1 step 2) {
            val c = Offset(s * 0.028f * u, -0.044f * u)
            drawCircle(Color.White, 0.016f * u, c)
            drawCircle(Ink.line, 0.016f * u, c, style = pen.thin)
            if (blink) {
                drawLine(Ink.line, Offset(c.x - 0.012f * u, c.y), Offset(c.x + 0.012f * u, c.y), pen.lw * 1.3f, StrokeCap.Round)
            } else {
                drawCircle(Ink.line, 0.007f * u, Offset(c.x + look + (if (talking) sin(pen.t * 9f) * 0.003f * u else 0f), c.y + 0.002f * u))
                drawCircle(Color.White, 0.0022f * u, Offset(c.x + look - 0.002f * u, c.y - 0.002f * u))
            }
            // A bushy eyebrow.
            drawLine(Ink.line, Offset(c.x - 0.012f * u, c.y - 0.022f * u - (if (talking) 0.004f * u else 0f)), Offset(c.x + 0.012f * u, c.y - 0.02f * u + (if (talking) 0.004f * u else 0f)), pen.lw * 1.8f, StrokeCap.Round)
        }
        // The mouth runs down the spine, and opens and closes when it talks.
        val open = if (talking) 0.008f + 0.012f * abs(sin(pen.t * 15f)) else 0.003f
        val mouth = Path().apply {
            moveTo(-0.018f * u, -0.016f * u)
            quadraticTo(0f, (-0.008f + open * 6f) * u, 0.018f * u, -0.016f * u)
            quadraticTo(0f, (-0.026f - open * 0f) * u, -0.018f * u, -0.016f * u)
            close()
        }
        drawPath(mouth, Color(0xFF7A2440))
        drawPath(mouth, Ink.line, style = pen.thin)
        if (talking) drawOval(Color(0xFFFF7F9E), Offset(-0.008f * u, -0.014f * u), Size(0.016f * u, 0.006f * u))
        // A bookmark ribbon for a tongue, and a golden clasp.
        drawLine(GrC.burgundy, Offset(0.02f * u, 0.012f * u), Offset(0.024f * u, 0.03f * u + (if (talking) sin(pen.t * 12f) * 0.003f * u else 0f)), pen.lw * 2.2f, StrokeCap.Round)
    }
    if (talking) {
        for (k in 0 until 3) {
            val ph = fxFrac(pen.t * 0.9f + k * 0.33f)
            fxNote(p(0.08f + ph * 0.04f, -0.28f - ph * 0.08f), 0.01f * u, listOf(Color(0xFF8B5CF6), Color(0xFFD2443A), Color(0xFF2F6FB8))[k], (1f - ph))
        }
    }
}

// ------------------------------------------------------------------------------------------------ the marble bust

internal fun DrawScope.grBust(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val marble = GrC.marble
    val cough = f.timer > 0f
    val a = (f.timer / 1.4f).coerceIn(0f, 1f)
    grShadow(u, 0.1f, 0.07f)
    // A fluted plinth.
    inkedRound(Rect(-0.045f * u, -0.012f * u, 0.045f * u, 0f), 0.002f * u, GrC.stoneDark, pen)
    val col = Path().apply {
        moveTo(-0.036f * u, -0.012f * u); lineTo(0.036f * u, -0.012f * u); lineTo(0.032f * u, -0.085f * u); lineTo(-0.032f * u, -0.085f * u); close()
    }
    inked(col, marble, pen)
    for (k in -2..2) drawLine(GrC.marbleDark, p(k * 0.012f, -0.016f), p(k * 0.01f, -0.08f), pen.lw * 0.6f)
    inkedRound(Rect(-0.042f * u, -0.095f * u, 0.042f * u, -0.083f * u), 0.002f * u, marble.darken(0.05f), pen)
    // Shoulders in a draped robe, the head, a curly beard, a cheerful nose, little round glasses.
    val body = Path().apply {
        moveTo(-0.045f * u, -0.095f * u)
        quadraticTo(-0.044f * u, -0.125f * u, -0.02f * u, -0.132f * u)
        lineTo(0.02f * u, -0.132f * u)
        quadraticTo(0.044f * u, -0.125f * u, 0.045f * u, -0.095f * u)
        close()
    }
    inked(body, marble, pen)
    for (k in 0..2) drawLine(GrC.marbleDark, p(-0.03f + k * 0.03f, -0.1f), p(-0.02f + k * 0.03f, -0.128f), pen.lw * 0.6f)
    val puff = if (cough) 0.004f * a else 0f
    val head = p(0f, -0.16f)
    inkedCircle(head, 0.03f * u + puff * u, marble, pen)
    // Curls of hair at the sides and a beard.
    for (s in -1..1 step 2) for (k in 0..2) inkedCircle(Offset(head.x + s * 0.03f * u, head.y - 0.012f * u + k * 0.012f * u), 0.009f * u, marble, pen, shade = false)
    val beard = Path().apply {
        moveTo(head.x - 0.026f * u, head.y + 0.006f * u)
        quadraticTo(head.x - 0.02f * u, head.y + 0.045f * u, head.x, head.y + 0.05f * u)
        quadraticTo(head.x + 0.02f * u, head.y + 0.045f * u, head.x + 0.026f * u, head.y + 0.006f * u)
        quadraticTo(head.x, head.y + 0.02f * u, head.x - 0.026f * u, head.y + 0.006f * u)
    }
    inked(beard, marble, pen)
    drawLine(GrC.marbleDark, Offset(head.x - 0.008f * u, head.y + 0.03f * u), Offset(head.x - 0.012f * u, head.y + 0.042f * u), pen.lw * 0.6f)
    drawLine(GrC.marbleDark, Offset(head.x + 0.008f * u, head.y + 0.03f * u), Offset(head.x + 0.012f * u, head.y + 0.042f * u), pen.lw * 0.6f)
    val nose = Path().apply { moveTo(head.x, head.y - 0.006f * u); lineTo(head.x - 0.006f * u, head.y + 0.012f * u); lineTo(head.x + 0.006f * u, head.y + 0.012f * u); close() }
    drawPath(nose, marble.darken(0.06f))
    drawPath(nose, Ink.line, style = pen.thin)
    for (s in -1..1 step 2) {
        val e = Offset(head.x + s * 0.0125f * u, head.y - 0.008f * u)
        drawCircle(GrC.brass, 0.0085f * u, e, style = Stroke(pen.lw * 0.9f))
        drawCircle(Ink.line, 0.0028f * u, Offset(e.x, e.y + (if (cough) -0.001f * u else 0f)))
    }
    drawLine(GrC.brass, Offset(head.x - 0.004f * u, head.y - 0.008f * u), Offset(head.x + 0.004f * u, head.y - 0.008f * u), pen.lw * 0.7f)
    if (cough) {
        for (k in 0 until 4) {
            val ph = fxFrac(pen.t * 1.5f + k * 0.25f)
            drawCircle(Color.White.copy(alpha = 0.8f * a * (1f - ph)), 0.01f * u * (1f + ph * 1.2f), Offset(head.x + 0.03f * u + ph * 0.05f * u, head.y + 0.02f * u - ph * 0.03f * u))
        }
        twinkle(p(-0.03f, -0.2f), 0.012f * u, Color.White, a)
    }
}
