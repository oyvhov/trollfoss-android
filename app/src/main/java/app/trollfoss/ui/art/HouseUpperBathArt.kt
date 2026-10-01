package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The bathroom and the bedroom: a shower cabin with a rainbow, a mirror that fogs and takes a smiley, a rack of towels with
 * a duck hiding in them; the canopy bed, the wardrobe that dresses you up, the dressing table, the jewellery box with its
 * ballerina and the rocking chair.
 */

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

// ---------------------------------------------------------------------------------------------- the shower

private const val SHOWER_D = 0.14f

/** The back of the cabin: tiles, a pipe, the shower head, the stream of water and, after a moment, a rainbow. */
internal fun DrawScope.upShower(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val on = f.on
    val d = SHOWER_D
    upShadow(u, 0.3f, d, 0.8f)
    // The tray on the floor and the tiled back wall.
    fxBox(u, -0.15f, -0.016f, 0.15f, 0f, d + 0.01f, UpC.paper, pen, rad = 0.008f, top = Color(0xFFDDE8EE))
    val back = fxFront(u, -0.14f, -0.57f, 0.14f, -0.016f, d)
    drawRect(Color(0xFFE6F6F7), back.topLeft, back.size)
    clipRect(back.left, back.top, back.right, back.bottom) {
        var yy = back.top
        while (yy < back.bottom) { drawLine(Color(0xFF9FD3D3), Offset(back.left, yy), Offset(back.right, yy), strokeWidth = pen.lw * 0.5f); yy += 0.04f * u }
        var xx = back.left
        var row = 0
        while (xx < back.right) { drawLine(Color(0xFF9FD3D3), Offset(xx, back.top), Offset(xx, back.bottom), strokeWidth = pen.lw * 0.5f); xx += 0.04f * u; row++ }
        // Blue tiles with a fish on them.
        for (k in 0 until 3) {
            val c = Offset(back.left + back.width * (0.2f + 0.3f * k), back.top + back.height * (0.62f + 0.05f * (k % 2)))
            drawOval(Color(0xFF5AA9E6), Offset(c.x - 0.012f * u, c.y - 0.007f * u), Size(0.024f * u, 0.014f * u))
            drawPath(Path().apply { poly(c.x - 0.01f * u, c.y, c.x - 0.02f * u, c.y - 0.007f * u, c.x - 0.02f * u, c.y + 0.007f * u) }, Color(0xFF5AA9E6))
        }
        // The rainbow: it stands in the cabin once the steam is thick.
        if (on) {
            val a = ((f.timer - 1.2f) / 0.8f).coerceIn(0f, 1f)
            if (a > 0f) {
                val c = Offset(back.center.x, back.bottom - 0.04f * u)
                drawRainbow(c, 0.12f * u, 0.011f * u, 0.85f * a)
                twinkle(Offset(c.x + 0.09f * u, c.y - 0.09f * u), 0.016f * u * (0.6f + 0.4f * sin(t * 4f)), Color.White, a)
            }
        }
    }
    drawRect(Ink.line, back.topLeft, back.size, style = pen.stroke)
    // The pipe and the head: chrome.
    val head = q(0f, -0.5f, d - 0.05f)
    capsule(q(0.06f, -0.52f, d - 0.004f), q(0.06f, -0.34f, d - 0.004f), 0.008f * u, UpC.steel, pen)
    capsule(q(0.06f, -0.52f, d - 0.004f), Offset(head.x, head.y - 0.012f * u), 0.008f * u, UpC.steel, pen)
    drawOval(UpC.steel, Offset(head.x - 0.032f * u, head.y - 0.008f * u), Size(0.064f * u, 0.02f * u))
    drawOval(Ink.line, Offset(head.x - 0.032f * u, head.y - 0.008f * u), Size(0.064f * u, 0.02f * u), style = pen.thin)
    for (k in -2..2) drawCircle(Ink.line.copy(alpha = 0.5f), 0.0018f * u, Offset(head.x + k * 0.012f * u, head.y + 0.003f * u))
    // Taps and a soap dish.
    for (s in listOf(-1f, 1f)) {
        val tp = q(0.06f + s * 0.02f, -0.3f, d - 0.004f)
        inkedCircle(tp, 0.007f * u, if (s < 0f) UpC.sky else UpC.red, pen, shade = false)
    }
    inkedRound(Rect(-0.1f * u + back.center.x - back.center.x, -0.28f * u, -0.04f * u, -0.268f * u), 0.004f * u, UpC.paper, pen, shade = false)
    inkedOval(Rect(-0.095f * u, -0.296f * u, -0.055f * u, -0.278f * u), UpC.pink, pen, shade = false)
    // The water.
    if (on) {
        val n = 16
        for (k in 0 until n) {
            val spread = (k / (n - 1f) - 0.5f) * 2f
            val ph = (t * 1.5f + k * 0.173f) % 1f
            val x0 = head.x + spread * 0.012f * u
            val y0 = head.y + 0.01f * u
            val x1 = head.x + spread * 0.07f * u
            val y1 = -0.02f * u
            val a = ph
            val b = min(1f, ph + 0.22f)
            drawLine(Color(0xFFBFE6FF).copy(alpha = 0.85f), Offset(lerpF(x0, x1, a), lerpF(y0, y1, a * a)), Offset(lerpF(x0, x1, b), lerpF(y0, y1, b * b)), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
        }
        // Splashes and steam over the tray.
        for (k in 0 until 5) {
            val ph = (t * 1.2f + k * 0.2f) % 1f
            drawCircle(Color.White.copy(alpha = 0.6f * (1f - ph)), 0.003f * u, Offset(head.x + (k - 2) * 0.035f * u, -0.02f * u - ph * 0.025f * u))
        }
        fxPuffs(back.center.x, back.bottom - 0.01f * u, t, 0.03f * u, 0.3f * u, Color.White, 0.55f, 5, 0.25f, 0.02f * u)
    }
}

/** The glass door of the cabin in front of whoever stands in it: pale glass, a frame and a few drops. */
internal fun DrawScope.upShowerFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val glass = Rect(-0.145f * u, -0.57f * u, 0.145f * u, -0.012f * u)
    val a = if (f.on) 0.34f else 0.16f
    drawRect(Color(0xFFCFEFFF).copy(alpha = a), glass.topLeft, glass.size)
    // The frame and a handle.
    for (x in listOf(-0.145f, 0.145f)) fxBox(u, x - 0.008f, -0.58f, x + 0.008f, 0f, 0.012f, UpC.steel, pen, rad = 0.003f)
    fxBox(u, -0.15f, -0.585f, 0.15f, -0.565f, 0.016f, UpC.steel, pen, rad = 0.003f)
    capsule(p(0.1f, -0.36f), p(0.1f, -0.27f), 0.007f * u, UpC.steelDark, pen)
    // A diagonal shine across the glass.
    drawLine(Color.White.copy(alpha = 0.5f), Offset(glass.left + 0.03f * u, glass.top + 0.12f * u), Offset(glass.left + 0.09f * u, glass.top + 0.03f * u), strokeWidth = 0.01f * u, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.35f), Offset(glass.left + 0.035f * u, glass.top + 0.15f * u), Offset(glass.left + 0.12f * u, glass.top + 0.04f * u), strokeWidth = 0.005f * u, cap = StrokeCap.Round)
    // Drops running down when it is wet.
    if (f.on) {
        for (k in 0 until 6) {
            val x = glass.left + glass.width * (0.15f + 0.14f * k)
            val ph = (t * 0.35f + k * 0.29f) % 1f
            val y = glass.top + glass.height * (0.1f + 0.7f * ph)
            drawCircle(Color.White.copy(alpha = 0.7f), 0.004f * u, Offset(x, y))
            drawLine(Color.White.copy(alpha = 0.35f), Offset(x, y - 0.03f * u), Offset(x, y), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
        }
    }
}

// ---------------------------------------------------------------------------------------------- the bathroom mirror

/** A round mirror in a brass frame. Clear it shows the room and sparkles; in the steam it fogs over; a finger can draw a smiley in the fog. */
internal fun DrawScope.upBathMirror(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val c = p(0f, -0.15f)
    val rx = 0.088f * u
    val ry = 0.11f * u
    // A little shelf with a tooth mug.
    fxBox(u, -0.1f, -0.014f, 0.1f, 0f, 0.04f, UpC.paper, pen, rad = 0.004f)
    inkedRound(Rect(-0.07f * u, -0.044f * u, -0.038f * u, -0.014f * u), 0.006f * u, Color(0xFFBFE3FA), pen, shade = false)
    for (k in 0..1) capsule(p(-0.062f + k * 0.012f, -0.04f), p(-0.058f + k * 0.012f, -0.075f), 0.006f * u, if (k == 0) UpC.red else UpC.sky, pen)
    // The frame and the glass.
    val frame = Path().apply { addOval(Rect(c.x - rx - 0.012f * u, c.y - ry - 0.012f * u, c.x + rx + 0.012f * u, c.y + ry + 0.012f * u)) }
    val glass = Path().apply { addOval(Rect(c.x - rx, c.y - ry, c.x + rx, c.y + ry)) }
    drawPath(frame, UpC.brass)
    drawPath(frame, Ink.line, style = pen.stroke)
    drawPath(glass, Brush.verticalGradient(listOf(Color(0xFFD9F0FF), Color(0xFFA6D4EE)), startY = c.y - ry, endY = c.y + ry))
    clipPath(glass) {
        when (f.mode) {
            0 -> {
                // Clear: two shines that glide across now and then.
                val g = 5f
                if (g < 1f) {
                    val x = c.x - rx + 2f * rx * g
                    drawLine(Color.White.copy(alpha = 0.8f), Offset(x - 0.02f * u, c.y + ry * 0.4f), Offset(x + 0.02f * u, c.y - ry * 0.4f), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(x + 0.03f * u, c.y + ry * 0.4f), Offset(x + 0.06f * u, c.y - ry * 0.4f), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
                }
                drawLine(Color.White.copy(alpha = 0.8f), Offset(c.x - rx * 0.6f, c.y - ry * 0.4f), Offset(c.x - rx * 0.3f, c.y - ry * 0.72f), strokeWidth = 0.01f * u, cap = StrokeCap.Round)
            }
            else -> {
                // Fog: a milky veil with running drops.
                drawRect(Color(0xFFF3F8FA).copy(alpha = 0.88f), Offset(c.x - rx, c.y - ry), Size(rx * 2f, ry * 2f))
                for (k in 0 until 9) {
                    val x = c.x - rx * 0.8f + rx * 1.6f * hash01(k, 181)
                    val y = c.y - ry * 0.8f + ry * 1.0f * hash01(k, 182)
                    drawLine(Color(0xFFD7E8EE), Offset(x, y), Offset(x, y + ry * (0.3f + 0.4f * hash01(k, 183))), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
                    drawCircle(Color(0xFFD7E8EE), 0.004f * u, Offset(x, y))
                }
                if (f.mode == 2) {
                    // The wiped smiley: eyes and a smile, in clear glass.
                    val clear = Color(0xFFBFE3F5)
                    drawCircle(clear, ry * 0.62f, c, style = Stroke(0.012f * u, cap = StrokeCap.Round))
                    drawCircle(clear, 0.012f * u, Offset(c.x - 0.03f * u, c.y - 0.025f * u))
                    drawCircle(clear, 0.012f * u, Offset(c.x + 0.03f * u, c.y - 0.025f * u))
                    drawArc(clear, 20f, 140f, false, Offset(c.x - 0.04f * u, c.y - 0.012f * u), Size(0.08f * u, 0.07f * u), style = Stroke(0.012f * u, cap = StrokeCap.Round))
                }
            }
        }
    }
    drawPath(glass, Ink.line, style = pen.thin)
    // A little sparkle on the frame now and then.
    val sp = 9f
    if (f.mode == 0 && sp < 0.6f) twinkle(Offset(c.x + rx * 0.7f, c.y - ry * 0.75f), 0.018f * u * sin(PI.toFloat() * sp / 0.6f), Color.White, 1f)
}

// ---------------------------------------------------------------------------------------------- the towels

/** A brass rail with three fluffy towels; the middle one has a little yellow beak peeking out of its fold. */
internal fun DrawScope.upTowels(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val deg = f.angle * 57.3f
    // The rail and its two brackets.
    val rail = -0.168f
    for (x in listOf(-0.115f, 0.115f)) {
        fxBox(u, x - 0.008f, rail - 0.01f, x + 0.008f, rail + 0.02f, 0.03f, UpC.brassDark, pen, rad = 0.003f)
    }
    capsule(p(-0.125f, rail), p(0.125f, rail), 0.009f * u, UpC.brass, pen)
    val cols = listOf(Color(0xFF7FC3E8), Color(0xFFF6E27A), Color(0xFFF6A9C4))
    for ((k, cx) in listOf(-0.075f, 0.0f, 0.075f).withIndex()) {
        val sway = deg * (1f + 0.35f * k)
        rotate(sway, p(cx, rail)) {
            val t0 = rail
            val h = 0.14f
            val tw = Path().apply {
                moveTo((cx - 0.034f) * u, t0 * u)
                lineTo((cx + 0.034f) * u, t0 * u)
                lineTo((cx + 0.036f) * u, (t0 + h) * u)
                for (i in 6 downTo 0) lineTo((cx - 0.036f + 0.072f * i / 6f) * u, (t0 + h + (if (i % 2 == 0) 0.008f else 0f)) * u)
                lineTo((cx - 0.036f) * u, (t0 + h) * u)
                close()
            }
            inked(tw, cols[k], pen)
            // Stripes near the bottom, and a fold at the top.
            drawLine(Color.White.copy(alpha = 0.85f), p(cx - 0.034f, t0 + h * 0.72f), p(cx + 0.034f, t0 + h * 0.72f), strokeWidth = 0.008f * u)
            drawLine(cols[k].darken(0.25f), p(cx - 0.034f, t0 + h * 0.82f), p(cx + 0.034f, t0 + h * 0.82f), strokeWidth = 0.004f * u)
            drawLine(cols[k].darken(0.2f), p(cx - 0.034f, t0 + 0.014f), p(cx + 0.034f, t0 + 0.014f), strokeWidth = pen.lw * 0.7f)
            if (k == 1) {
                // A duck's beak and one eye, in the fold of the middle towel.
                drawOval(Color(0xFFFFB02E), Offset((cx - 0.012f) * u, (t0 + 0.05f) * u), Size(0.026f * u, 0.012f * u))
                drawOval(Ink.line, Offset((cx - 0.012f) * u, (t0 + 0.05f) * u), Size(0.026f * u, 0.012f * u), style = pen.thin)
                drawCircle(Ink.line, 0.002f * u, p(cx - 0.006f, t0 + 0.043f))
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------- the canopy bed

private const val BED_D = 0.18f

/** The back of the four-poster: the back posts, the canopy over it, and the bed itself. */
internal fun DrawScope.upCanopyBed(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = BED_D
    val sway = upBeat(f) * sin(upBeat(f) * 25f) * 0.012f * u
    val post = Color(0xFFF7F3EC)
    val top = -0.5f
    upShadow(u, 0.42f, d)
    // The back posts and the sheer curtain between them.
    fxPost(u, -0.2f, d - 0.01f, 0f, top, 0.011f, post, pen)
    fxPost(u, 0.2f, d - 0.01f, 0f, top, 0.011f, post, pen)
    val a = q(-0.2f, top + 0.02f, d - 0.01f)
    val b = q(0.2f, top + 0.02f, d - 0.01f)
    val c = q(0.2f, -0.12f, d - 0.01f)
    val e = q(-0.2f, -0.12f, d - 0.01f)
    val sheer = Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x + sway, c.y); lineTo(e.x + sway, e.y); close() }
    drawPath(sheer, Color(0xFFFFE3EE).copy(alpha = 0.7f))
    for (k in 1..9) {
        val fx = lerpF(a.x, b.x, k / 10f)
        drawLine(Color(0xFFF7B6CF).copy(alpha = 0.7f), Offset(fx, a.y), Offset(fx + sway, e.y), strokeWidth = pen.lw * 0.6f)
    }
    drawPath(sheer, Ink.line, alpha = 0.5f, style = pen.thin)
    // The bed itself, as in the rest of the house.
    fxBed(f, u, pen)
    // The canopy: a frame, a soft pink cloth over it, a scalloped valance along the front and the right side, and stars.
    val tl = q(-0.215f, top, 0f)
    fxBox(u, -0.215f, top - 0.012f, 0.215f, top + 0.012f, d + 0.02f, Color(0xFFF7B6CF), pen, rad = 0.006f, z = -0.01f, top = Color(0xFFFFD6E6), side = Color(0xFFD98BAA))
    val cloth = fxFlat(u, -0.215f, 0.215f, top - 0.012f, -0.01f, d + 0.01f, 0.006f)
    clipPath(cloth) {
        for (j in 0 until 3) for (i in 0 until 9) {
            val cc = q(-0.19f + i * 0.047f + (if (j % 2 == 0) 0f else 0.023f), top - 0.012f, 0.02f + j * 0.06f)
            drawPath(starPath(cc, 0.007f * u, 0.003f * u), Color.White.copy(alpha = 0.85f))
        }
    }
    drawPath(cloth, Ink.line, style = pen.thin)
    if (tl.x > 1e9f) Unit
}

/** The front of the four-poster: the duvet, the front posts, the tied-back curtains, a scalloped valance and fairy lights. */
internal fun DrawScope.upCanopyBedFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val n = pen.night
    val sway = upBeat(f) * sin(upBeat(f) * 25f + 1f) * 0.012f * u
    fxBedFront(f, u, pen)
    val post = Color(0xFFF7F3EC)
    val top = -0.5f
    for (x in listOf(-0.2f, 0.2f)) {
        fxPost(u, x, 0.01f, 0f, top, 0.012f, post, pen)
        inkedCircle(q(x, top - 0.016f, 0.01f), 0.014f * u, UpC.brass, pen)
    }
    // The curtains hang from the top, tied back to the posts with a ribbon.
    for (s in listOf(-1f, 1f)) {
        val px = s * 0.2f
        val inward = -s
        val cur = Path().apply {
            val a = q(px, top + 0.012f, 0.012f)
            moveTo(a.x, a.y)
            lineTo(a.x + inward * 0.05f * u, a.y)
            quadraticTo(a.x + inward * 0.025f * u, a.y + 0.2f * u, a.x + inward * 0.05f * u + sway, a.y + 0.33f * u)
            lineTo(a.x, a.y + 0.33f * u)
            close()
        }
        inked(cur, Color(0xFFF7B6CF), pen)
        val tie = q(px, top + 0.2f, 0.012f)
        drawLine(UpC.brass, tie, Offset(tie.x + inward * 0.04f * u, tie.y + 0.004f * u), strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
    }
    // A scalloped valance along the front of the canopy, with fairy lights along its edge.
    val y0 = top + 0.012f
    val v = Path().apply {
        val l = q(-0.215f, y0 - 0.025f, 0f)
        val r = q(0.215f, y0 - 0.025f, 0f)
        moveTo(l.x, l.y); lineTo(r.x, r.y); lineTo(r.x, (y0 + 0.002f) * u + 0f)
        val n8 = 8
        for (k in n8 - 1 downTo 0) {
            val xa = -0.215f + 0.43f * k / n8
            val xb = -0.215f + 0.43f * (k + 1) / n8
            val pa = q(xa, y0, 0f)
            val pc = q((xa + xb) / 2f, y0 + 0.034f, 0f)
            quadraticTo(pc.x, pc.y, pa.x, pa.y)
        }
        close()
    }
    inked(v, Color(0xFFFFD6E6), pen)
    for (k in 0..8) {
        val xx = -0.2f + 0.4f * k / 8f
        val bc = q(xx, y0 + 0.014f + 0.01f * sin(k * 1.3f), 0f)
        val lit = ramp((n - 0.2f) / 0.5f)
        val col = listOf(Color(0xFFFFE066), Color(0xFFFF8FB1), Color(0xFF8FD9C0))[k % 3]
        if (lit > 0f) fxGlow(bc, 0.03f * u, col, 0.8f * lit * (0.6f + 0.4f * sin(t * 3f + k)))
        drawCircle(col.lighten(0.3f), 0.0045f * u, bc)
    }
}

// ---------------------------------------------------------------------------------------------- the wardrobe

private val garmentColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFFB983FF), Color(0xFFFF9F43), Color(0xFFF08CB8))

/** A sage-green wardrobe with a mirror in one door; open it shows clothes on hangers, hats on the shelf and a pair of shoes. */
internal fun DrawScope.upWardrobe(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.16f
    val body = Color(0xFF9CB8A0)
    val trim = Color(0xFFF7F3EC)
    upShadow(u, 0.36f, d)
    for (x in listOf(-0.14f, 0.14f)) fxBox(u, x - 0.012f, -0.026f, x + 0.012f, 0f, 0.02f, UpC.wood, pen, rad = 0.003f, z = d - 0.03f)
    fxBox(u, -0.17f, -0.46f, 0.17f, -0.02f, d, body, pen, rad = 0.006f)
    // A cornice with a little arch and stars, and a plinth.
    fxBox(u, -0.18f, -0.485f, 0.18f, -0.455f, d + 0.016f, trim, pen, rad = 0.004f)
    for (k in 0 until 3) drawPath(starPath(p(-0.09f + k * 0.09f, -0.47f), 0.008f * u, 0.0035f * u), UpC.yellow)
    inkedRound(Rect(-0.175f * u, -0.034f * u, 0.175f * u, -0.016f * u), 0.003f * u, trim, pen)
    if (!f.open) {
        for (s in 0..1) {
            val l = if (s == 0) -0.162f else 0.004f
            val door = Rect(l * u, -0.445f * u, (l + 0.158f) * u, -0.04f * u)
            inkedRound(door, 0.006f * u, body, pen, shade = false)
            drawRoundRect(body.darken(0.14f), Offset(door.left + 0.014f * u, door.top + 0.014f * u), Size(door.width - 0.028f * u, door.height - 0.028f * u), CornerRadius(0.01f * u), style = pen.thin)
        }
        // A long mirror in the left door, sparkling now and then, and a rosette on the right one.
        val m = Rect(-0.135f * u, -0.41f * u, -0.025f * u, -0.075f * u)
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFE2F4FF), Color(0xFFA9D6EE)), startY = m.top, endY = m.bottom), m.topLeft, m.size, CornerRadius(0.008f * u))
        drawRoundRect(trim, m.topLeft, m.size, CornerRadius(0.008f * u), style = Stroke(0.007f * u))
        drawRoundRect(Ink.line, m.topLeft, m.size, CornerRadius(0.008f * u), style = pen.thin)
        drawLine(Color.White.copy(alpha = 0.7f), Offset(m.left + 0.02f * u, m.top + 0.08f * u), Offset(m.left + 0.06f * u, m.top + 0.02f * u), strokeWidth = 0.01f * u, cap = StrokeCap.Round)
        val g = 5f
        if (g < 0.6f) twinkle(Offset(m.right - 0.025f * u, m.top + 0.07f * u), 0.02f * u * sin(PI.toFloat() * g / 0.6f), Color.White, 1f)
        val rc = p(0.083f, -0.24f)
        drawPath(starPath(rc, 0.034f * u, 0.016f * u), UpC.yellow)
        drawPath(starPath(rc, 0.034f * u, 0.016f * u), Ink.line, style = pen.thin)
        drawCircle(UpC.pink, 0.011f * u, rc)
        inkedCircle(p(-0.012f, -0.2f), 0.008f * u, UpC.brass, pen, shade = false)
        inkedCircle(p(0.012f, -0.2f), 0.008f * u, UpC.brass, pen, shade = false)
        return
    }
    // Open: a warm inside with a rail of clothes, a shelf with hats and a pair of shoes below.
    val id = d - 0.012f
    fxHollow(u, -0.152f, -0.445f, 0.152f, -0.04f, id, Color(0xFFE9C58C), pen)
    clipRect(-0.152f * u, -0.445f * u, 0.152f * u, -0.04f * u) {
        val rz = id * 0.5f
        fxGlow(q(0f, -0.25f, rz), 0.2f * u, Color(0xFFFFE9A8), 0.5f)
        fxLine(q(-0.152f, -0.4f, rz), q(0.152f, -0.4f, rz), UpC.steel, 0.006f * u)
        // Clothes on hangers, swaying a little: a dress, a shirt, trousers, a jumper.
        for (k in 0 until 6) {
            val hx = -0.115f + k * 0.046f
            val sw = sin(t * 1.4f + k * 0.9f) * 0.004f
            val hp = q(hx + sw, -0.4f, rz)
            val col = garmentColors[(k * 2 + 1) % garmentColors.size]
            val len = 0.07f + 0.04f * (k % 3)
            drawLine(Ink.line, Offset(hp.x, hp.y - 0.008f * u), hp, strokeWidth = pen.lw * 0.7f)
            val g = Path().apply {
                moveTo(hp.x - 0.022f * u, hp.y + 0.006f * u); lineTo(hp.x + 0.022f * u, hp.y + 0.006f * u)
                lineTo(hp.x + (0.026f + 0.01f * (k % 2)) * u, hp.y + len * u); lineTo(hp.x - (0.026f + 0.01f * (k % 2)) * u, hp.y + len * u); close()
            }
            inked(g, col, pen, shade = false)
            if (k % 3 == 1) drawLine(Color.White.copy(alpha = 0.7f), Offset(hp.x - 0.02f * u, hp.y + 0.03f * u), Offset(hp.x + 0.02f * u, hp.y + 0.03f * u), strokeWidth = 0.006f * u)
        }
        fxInShelf(u, -0.152f, 0.152f, -0.24f, id, UpC.oak, pen, 0.009f)
        // Hats on the shelf: a party hat, a crown and a flat cap.
        val sh = q(-0.09f, -0.24f, id * 0.5f)
        drawPath(Path().apply { poly(sh.x - 0.018f * u, sh.y, sh.x + 0.018f * u, sh.y, sh.x, sh.y - 0.06f * u) }, UpC.pink)
        drawCircle(UpC.yellow, 0.005f * u, Offset(sh.x, sh.y - 0.06f * u))
        val cr = q(0.0f, -0.24f, id * 0.5f)
        drawPath(Path().apply { poly(cr.x - 0.022f * u, cr.y, cr.x - 0.022f * u, cr.y - 0.03f * u, cr.x - 0.011f * u, cr.y - 0.016f * u, cr.x, cr.y - 0.036f * u, cr.x + 0.011f * u, cr.y - 0.016f * u, cr.x + 0.022f * u, cr.y - 0.03f * u, cr.x + 0.022f * u, cr.y) }, UpC.yellow)
        drawPath(Path().apply { poly(cr.x - 0.022f * u, cr.y, cr.x - 0.022f * u, cr.y - 0.03f * u, cr.x - 0.011f * u, cr.y - 0.016f * u, cr.x, cr.y - 0.036f * u, cr.x + 0.011f * u, cr.y - 0.016f * u, cr.x + 0.022f * u, cr.y - 0.03f * u, cr.x + 0.022f * u, cr.y) }, Ink.line, style = pen.thin)
        val cp = q(0.09f, -0.24f, id * 0.5f)
        inkedOval(Rect(cp.x - 0.026f * u, cp.y - 0.02f * u, cp.x + 0.026f * u, cp.y), Color(0xFF55637A), pen, shade = false)
        // A pair of shoes on the floor of the wardrobe.
        for (k in 0..1) {
            val sc = q(-0.04f + k * 0.07f, -0.04f, id * 0.5f)
            inkedOval(Rect(sc.x - 0.026f * u, sc.y - 0.022f * u, sc.x + 0.03f * u, sc.y), if (k == 0) UpC.red else UpC.red.darken(0.1f), pen, shade = false)
        }
    }
    fxOpenDoor(u, -0.162f, -0.447f, -0.042f, 0.152f, -1f, body, body.darken(0.05f), pen, 128f)
    val lv = fxDoorVec(u, 0.152f, -1f, 128f)
    inkedCircle(Offset(-0.162f * u + lv.x * 0.85f, -0.24f * u + lv.y * 0.85f), 0.007f * u, UpC.brass, pen, shade = false)
    fxOpenDoor(u, 0.162f, -0.447f, -0.042f, 0.152f, 1f, body, body.darken(0.05f), pen, 128f)
    val rv = fxDoorVec(u, 0.152f, 1f, 128f)
    inkedCircle(Offset(0.162f * u + rv.x * 0.85f, -0.24f * u + rv.y * 0.85f), 0.007f * u, UpC.brass, pen, shade = false)
}

// ---------------------------------------------------------------------------------------------- the dressing table

/** A dressing table with a round mirror ringed with bulbs, perfume bottles, a comb and a hand mirror. */
internal fun DrawScope.upVanity(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val on = f.on
    val d = 0.13f
    upShadow(u, 0.34f, d)
    // The table: a top, two drawers and four thin legs.
    for ((x, z) in listOf(-0.15f to 0.01f, 0.15f to 0.01f, -0.15f to d - 0.02f, 0.15f to d - 0.02f)) fxPost(u, x, z, 0f, -0.18f, 0.008f, UpC.oak, pen)
    fxBox(u, -0.17f, -0.2f, 0.17f, -0.17f, d, Color(0xFFFFE9F2), pen, rad = 0.004f, top = Color(0xFFFFFFFF))
    for (s in listOf(-1f, 1f)) {
        val c = p(s * 0.08f, -0.135f)
        inkedRound(Rect(c.x - 0.062f * u, c.y - 0.026f * u, c.x + 0.062f * u, c.y + 0.026f * u), 0.005f * u, Color(0xFFF7B6CF), pen, shade = false)
        inkedCircle(c, 0.006f * u, UpC.brass, pen, shade = false)
    }
    fxBox(u, -0.17f, -0.17f, 0.17f, -0.1f, d - 0.02f, Color(0xFFFFE9F2), pen, rad = 0.003f, front = false)
    // The mirror on the wall behind, with bulbs round it.
    val mc = q(0f, -0.35f, d - 0.015f)
    val rx = 0.115f * u
    val ry = 0.13f * u
    drawOval(Color(0xFFF7B6CF), Offset(mc.x - rx - 0.012f * u, mc.y - ry - 0.012f * u), Size((rx + 0.012f * u) * 2f, (ry + 0.012f * u) * 2f))
    drawOval(Ink.line, Offset(mc.x - rx - 0.012f * u, mc.y - ry - 0.012f * u), Size((rx + 0.012f * u) * 2f, (ry + 0.012f * u) * 2f), style = pen.stroke)
    val glass = Path().apply { addOval(Rect(mc.x - rx, mc.y - ry, mc.x + rx, mc.y + ry)) }
    drawPath(glass, Brush.verticalGradient(listOf(Color(0xFFE7F5FF), Color(0xFFB4DAF0)), startY = mc.y - ry, endY = mc.y + ry))
    clipPath(glass) {
        drawLine(Color.White.copy(alpha = 0.8f), Offset(mc.x - rx * 0.55f, mc.y - ry * 0.2f), Offset(mc.x - rx * 0.25f, mc.y - ry * 0.65f), strokeWidth = 0.012f * u, cap = StrokeCap.Round)
        val g = 5f
        if (g < 1f) drawLine(Color.White.copy(alpha = 0.5f), Offset(mc.x - rx + 2f * rx * g - 0.03f * u, mc.y + ry * 0.5f), Offset(mc.x - rx + 2f * rx * g + 0.03f * u, mc.y - ry * 0.5f), strokeWidth = 0.012f * u, cap = StrokeCap.Round)
    }
    drawPath(glass, Ink.line, style = pen.thin)
    for (k in 0 until 12) {
        val a = k * 2f * PI.toFloat() / 12f
        val b = Offset(mc.x + cos(a) * (rx + 0.006f * u), mc.y + sin(a) * (ry + 0.006f * u))
        if (on) fxGlow(b, 0.035f * u, Color(0xFFFFE066), 0.75f * (0.7f + 0.3f * sin(t * 6f + k)))
        drawCircle(if (on) Color(0xFFFFF3A8) else Color(0xFFFFF7DA), 0.0065f * u, b)
        drawCircle(Ink.line, 0.0065f * u, b, style = pen.thin)
    }
    // On the table: two perfume bottles, a comb, a hand mirror and a flower in a vase.
    val top = -0.2f
    for ((i, c) in listOf(Color(0xFFB9A2F0) to -0.12f, Color(0xFFFF9EC4) to -0.095f).withIndex()) {
        val b = q(c.second, top, 0.03f)
        inkedRound(Rect(b.x - 0.011f * u, b.y - (0.034f + 0.01f * i) * u, b.x + 0.011f * u, b.y), 0.006f * u, c.first, pen)
        inkedCircle(Offset(b.x, b.y - (0.04f + 0.01f * i) * u), 0.006f * u, UpC.brass, pen, shade = false)
    }
    val comb = q(-0.01f, top, 0.05f)
    inkedRound(Rect(comb.x - 0.03f * u, comb.y - 0.008f * u, comb.x + 0.03f * u, comb.y), 0.003f * u, UpC.skin.lighten(0.2f), pen, shade = false)
    // The hand mirror leaning.
    val hm = q(0.085f, top, 0.04f)
    rotate(-18f, hm) {
        capsule(hm, Offset(hm.x, hm.y - 0.026f * u), 0.006f * u, UpC.pink, pen)
        inkedCircle(Offset(hm.x, hm.y - 0.046f * u), 0.018f * u, UpC.pink, pen)
        drawCircle(Color(0xFFCFEAFB), 0.013f * u, Offset(hm.x, hm.y - 0.046f * u))
    }
    val vs = q(0.14f, top, 0.06f)
    inkedRound(Rect(vs.x - 0.01f * u, vs.y - 0.026f * u, vs.x + 0.01f * u, vs.y), 0.005f * u, Color(0xFFBFE3FA), pen, shade = false)
    capsule(Offset(vs.x, vs.y - 0.026f * u), Offset(vs.x, vs.y - 0.06f * u), 0.003f * u, UpC.leaf, pen)
    inkedCircle(Offset(vs.x, vs.y - 0.066f * u), 0.01f * u, UpC.yellow, pen, shade = false)
    drawCircle(UpC.orange, 0.004f * u, Offset(vs.x, vs.y - 0.066f * u))
}

// ---------------------------------------------------------------------------------------------- the jewellery box

/** A pink box with a gold clasp. When it is on the lid is open, music plays and a ballerina spins on her spindle. */
internal fun DrawScope.upJewelBox(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val on = f.on
    val d = 0.07f
    fxBox(u, -0.06f, -0.052f, 0.06f, 0f, d, Color(0xFFF08CB8), pen, rad = 0.008f)
    drawLine(UpC.brass, p(-0.055f, -0.034f), p(0.055f, -0.034f), strokeWidth = 0.004f * u)
    inkedRound(Rect(-0.012f * u, -0.044f * u, 0.012f * u, -0.026f * u), 0.003f * u, UpC.brass, pen, shade = false)
    if (!on) {
        // Closed: a lid with a little heart.
        fxBox(u, -0.062f, -0.075f, 0.062f, -0.05f, d + 0.004f, Color(0xFFF7A6C6), pen, rad = 0.008f, z = -0.002f)
        drawPath(fxHeart(0f, -0.063f * u, 0.011f * u), Color.White)
        drawPath(fxHeart(0f, -0.063f * u, 0.011f * u), Ink.line, style = pen.thin)
        return
    }
    // Open: the lid stands up at the back with a mirror in it; red velvet inside.
    val inside = fxFlat(u, -0.055f, 0.055f, -0.052f, 0.006f, d - 0.006f, 0.004f)
    fxFace(inside, Color(0xFFC63A5A), pen)
    val lid = Rect(-0.06f * u, -0.17f * u, 0.06f * u, -0.095f * u)
    val lc = fxQ(u, 0f, 0f, d)
    translateTo(lc.x, lc.y - 0.0f) {
        inkedRound(Rect(lid.left, lid.top + 0.06f * u, lid.right, lid.bottom + 0.0f * u), 0.008f * u, Color(0xFFF7A6C6), pen)
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFE7F5FF), Color(0xFFB4DAF0)), startY = lid.top, endY = lid.bottom), Offset(lid.left + 0.008f * u, lid.top + 0.068f * u), Size(lid.width - 0.016f * u, lid.height - 0.016f * u), CornerRadius(0.005f * u))
    }
    // The ballerina: she turns, so her width goes with the cosine of her angle.
    val a = f.angle
    val sx = cos(a)
    val base = fxQ(u, 0f, -0.052f, d / 2f)
    capsule(base, Offset(base.x, base.y - 0.02f * u), 0.004f * u, UpC.brassDark, pen)
    translateTo(base.x, base.y - 0.02f * u) {
        scale(if (abs(sx) < 0.15f) 0.15f else sx, 1f, pivot = Offset.Zero) {
            val bob = sin(a * 2f) * 0.002f * u
            // Tutu: three layers of white tulle; body, arms above her head, a bun.
            val tutu = Path().apply {
                moveTo(-0.034f * u, -0.012f * u + bob); quadraticTo(0f, -0.03f * u + bob, 0.034f * u, -0.012f * u + bob)
                quadraticTo(0.014f * u, 0.002f * u + bob, -0.034f * u, -0.012f * u + bob); close()
            }
            inked(tutu, Color(0xFFFFF3F8), pen)
            drawArc(Color(0xFFF7B6CF), 200f, 140f, false, Offset(-0.03f * u, -0.025f * u + bob), Size(0.06f * u, 0.03f * u), style = pen.thin)
            inkedRound(Rect(-0.008f * u, -0.05f * u + bob, 0.008f * u, -0.018f * u + bob), 0.005f * u, Color(0xFFF7A6C6), pen, shade = false)
            capsule(Offset(-0.006f * u, -0.044f * u + bob), Offset(-0.014f * u, -0.074f * u + bob), 0.004f * u, UpC.skin, pen)
            capsule(Offset(0.006f * u, -0.044f * u + bob), Offset(0.014f * u, -0.074f * u + bob), 0.004f * u, UpC.skin, pen)
            inkedCircle(Offset(0f, -0.06f * u + bob), 0.009f * u, UpC.skin, pen, shade = false)
            inkedCircle(Offset(0.003f * u, -0.073f * u + bob), 0.005f * u, Color(0xFF5A3A28), pen, shade = false)
        }
    }
    // Notes drifting up from the box.
    for (k in 0 until 3) {
        val ph = (pen.t * 0.6f + k / 3f) % 1f
        fxNote(p(0.05f + sin(ph * 6f + k) * 0.02f, -0.1f - ph * 0.12f), 0.012f * u, Color(0xFFB983FF), 1f - ph)
    }
}

private inline fun DrawScope.translateTo(x: Float, y: Float, block: DrawScope.() -> Unit) = translate(x, y, block)

// ---------------------------------------------------------------------------------------------- the rocking chair

/** An oak rocking chair with a green cushion and a knitted blanket over the arm; it rocks about the floor. */
internal fun DrawScope.upRockingChair(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val deg = f.angle * 57.3f
    upShadow(u, 0.2f, 0.1f, 0.8f)
    rotate(deg, p(0f, -0.004f)) {
        // The rockers: two curved runners, front and back, and the legs on them.
        for (z in listOf(0.01f, 0.09f)) {
            val a = q(-0.1f, -0.012f, z)
            val b = q(0.1f, -0.012f, z)
            val m = q(0f, 0.004f, z)
            val rk = Path().apply { moveTo(a.x, a.y); quadraticTo(m.x, m.y * 2f - a.y * 0.0f + 0.006f * u, b.x, b.y) }
            drawPath(rk, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(rk, UpC.oakDark, style = Stroke(0.012f * u, cap = StrokeCap.Round))
        }
        for ((x, z) in listOf(-0.07f to 0.01f, 0.07f to 0.01f, -0.07f to 0.09f, 0.07f to 0.09f)) fxPost(u, x, z, -0.008f, -0.07f, 0.0065f, UpC.oak, pen)
        // The seat, the back with slats, and the arms.
        fxPost(u, -0.07f, 0.09f, -0.07f, -0.24f, 0.0065f, UpC.oak, pen)
        fxPost(u, 0.07f, 0.09f, -0.07f, -0.24f, 0.0065f, UpC.oak, pen)
        for (k in 0 until 4) fxBox(u, -0.07f, -0.235f + k * 0.032f, 0.07f, -0.22f + k * 0.032f, 0.012f, UpC.oak, pen, rad = 0.003f, z = 0.085f)
        fxBox(u, -0.085f, -0.078f, 0.085f, -0.064f, 0.1f, UpC.oak, pen, rad = 0.004f)
        fxBox(u, -0.08f, -0.095f, 0.08f, -0.076f, 0.09f, Color(0xFF5DB380), pen, rad = 0.008f, z = 0.005f, top = Color(0xFF7BCB98))
        for ((x, z) in listOf(-0.088f to 0.02f, 0.088f to 0.02f)) {
            fxPost(u, x, z, -0.07f, -0.13f, 0.0055f, UpC.oak, pen)
        }
        capsule(q(-0.088f, -0.13f, 0.02f), q(-0.088f, -0.13f, 0.085f), 0.01f * u, UpC.oak, pen)
        capsule(q(0.088f, -0.13f, 0.02f), q(0.088f, -0.13f, 0.085f), 0.01f * u, UpC.oak, pen)
        // A knitted blanket over the left arm, with a little heart.
        inkedRound(Rect(-0.108f * u, -0.146f * u, -0.074f * u, -0.066f * u), 0.008f * u, Color(0xFFF2A65A), pen, shade = false)
        drawLine(Color(0xFFD9803A), p(-0.108f, -0.126f), p(-0.074f, -0.126f), strokeWidth = pen.lw * 0.8f)
        drawPath(fxHeart(-0.091f * u, -0.1f * u, 0.006f * u), Color(0xFFFFF3C4))
    }
}
