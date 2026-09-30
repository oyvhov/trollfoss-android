package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.Palette
import kotlin.math.sin

// Drinks lower their level with every sip ([used] of three); potions glow and bubble.

private fun left(used: Int): Float = 1f - used.coerceIn(0, 3) / 3f

internal fun DrawScope.thMilk(used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val blue = Color(0xFF3D7FD6)
    // Oblique carton: the side recedes up and to the right at the world's angle, the gable on top.
    val fr = 0.14f
    val dx = 0.3f
    val dy = Oblique.DY / Oblique.DX * dx * w / h
    val top = -0.64f
    val ridge = top + dy / 2f - 0.16f
    val front = thSketch(w, h) { m(-0.46f, top); l(fr, top); l(fr, -0.02f); l(-0.46f, -0.02f); z() }
    val side = thSketch(w, h) { m(fr, top); l(fr + dx, top + dy); l(fr + dx, -0.02f + dy); l(fr, -0.02f); z() }
    val roof = thSketch(w, h) { m(-0.46f, top); l(fr, top); l(fr + dx / 2, ridge); l(-0.46f + dx / 2, ridge); z() }
    val gable = thSketch(w, h) { m(fr, top); l(fr + dx / 2, ridge); l(fr + dx, top + dy); z() }
    drawPath(side, Color(0xFFDDE2EC))
    drawPath(gable, Color(0xFFE9ECF3))
    drawPath(roof, Color.White)
    drawPath(front, ThingInk.paper)
    clipPath(front) {
        drawPath(thSketch(w, h) { m(-0.5f, -0.36f); q(-0.32f, -0.48f, -0.17f, -0.36f); q(-0.02f, -0.25f, 0.16f, -0.38f); l(0.16f, 0f); l(-0.5f, 0f); z() }, blue)
        drawPath(thSketch(w, h) { m(-0.42f, -0.38f); q(-0.18f, -0.6f, 0.08f, -0.4f); z() }, Color(0xFF5DBB4A))
        drawCircle(ThingInk.sun, w * 0.07f, o(-0.08f, -0.54f))
        drawCircle(Color.White.copy(alpha = 0.85f), w * 0.03f, o(-0.3f, -0.2f))
        drawCircle(Color.White.copy(alpha = 0.85f), w * 0.02f, o(-0.16f, -0.12f))
    }
    clipPath(side) { drawPath(thSketch(w, h) { m(fr, -0.38f); l(fr + dx, -0.38f + dy); l(fr + dx, 0.1f); l(fr, 0.1f); z() }, blue.darken(0.15f)) }
    // A little window on the side shows how much milk is left.
    val wx0 = fr + dx * 0.3f
    val wx1 = fr + dx * 0.7f
    val win = thSketch(w, h) { m(wx0, -0.56f + dy * 0.3f); l(wx1, -0.56f + dy * 0.7f); l(wx1, -0.12f + dy * 0.7f); l(wx0, -0.12f + dy * 0.3f); z() }
    drawPath(win, Color(0xFFB8C4D8))
    val lv = left(used)
    if (lv > 0f) {
        val rise = 0.44f * lv
        clipPath(win) { drawPath(thSketch(w, h) { m(wx0 - 0.1f, -0.12f - rise + dy * 0.2f); l(wx1 + 0.1f, -0.12f - rise + dy * 0.8f); l(wx1 + 0.1f, 0.2f); l(wx0 - 0.1f, 0.2f); z() }, Color.White) }
    }
    drawPath(win, Ink.line, style = Stroke(pen.lw * 0.6f, join = StrokeJoin.Round))
    val outline = thSketch(w, h) {
        m(-0.46f, -0.02f); l(-0.46f, top); l(-0.46f + dx / 2, ridge); l(fr + dx / 2, ridge); l(fr + dx, top + dy); l(fr + dx, -0.02f + dy); l(fr, -0.02f); z()
    }
    drawPath(outline, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawPath(thSketch(w, h) { m(-0.46f, top); l(fr, top); l(fr + dx / 2, ridge); m(fr, top); l(fr, -0.02f) }, Ink.line, style = pen.thin)
    val fin = thSketch(w, h) { m(-0.46f + dx / 2, ridge); l(fr + dx / 2, ridge); l(fr + dx / 2, ridge - 0.08f); l(-0.46f + dx / 2, ridge - 0.08f); z() }
    inked(fin, Color.White, pen, shade = false)
    if (used > 0) {
        val sx = fr + dx / 2
        drawPath(thSketch(w, h) { m(sx, ridge + 0.02f); l(sx + 0.06f, ridge + 0.06f); l(sx + 0.04f, ridge + 0.12f); l(sx - 0.02f, ridge + 0.08f); z() }, Ink.line)
    }
    drawLine(Color.White.copy(alpha = 0.8f), o(-0.38f, -0.58f), o(-0.38f, -0.44f), w * 0.05f, StrokeCap.Round)
}

internal fun DrawScope.thJuice(used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val glass = thSketch(w, h) {
        m(-0.42f, -0.86f); l(0.42f, -0.86f); l(0.34f, -0.05f); q(0.33f, -0.02f, 0.29f, -0.02f)
        l(-0.29f, -0.02f); q(-0.33f, -0.02f, -0.34f, -0.05f); z()
    }
    drawPath(glass, ThingInk.glass)
    val straw = thSketch(w, h) { m(0.02f, -0.18f); l(0.12f, -0.93f); l(-0.08f, -0.99f) }
    drawPath(straw, Ink.line, style = Stroke(w * 0.09f + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(straw, ThingInk.sky, style = Stroke(w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(straw, Color.White.copy(alpha = 0.6f), style = Stroke(w * 0.025f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    val juice = Color(0xFFFFA53D)
    val lv = left(used)
    if (lv > 0f) {
        val y = -0.12f - 0.62f * lv
        val half = 0.34f + 0.08f * ((-y - 0.05f) / 0.81f)
        clipPath(glass) {
            drawRect(juice, o(-0.5f, y), Size(w, -y * h))
            drawOval(juice.lighten(0.3f), o(-half, y - 0.03f), Size(half * 2f * w, 0.06f * h))
            drawCircle(juice.lighten(0.35f), w * 0.03f, o(-0.12f, y + 0.2f))
            drawCircle(juice.lighten(0.35f), w * 0.025f, o(0.14f, y + 0.34f))
        }
    }
    clipPath(glass) { drawRect(Color.White.copy(alpha = 0.35f), o(-0.5f, -0.1f), Size(w, 0.1f * h)) }
    drawPath(glass, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawLine(Color.White.copy(alpha = 0.7f), o(-0.3f, -0.76f), o(-0.25f, -0.22f), w * 0.07f, StrokeCap.Round)
    val sc = o(0.34f, -0.86f)
    val sr = w * 0.2f
    inkedCircle(sc, sr, Color(0xFFFF9A2E), pen)
    drawCircle(Color(0xFFFFD08A), sr * 0.74f, sc)
    for (k in 0 until 6) {
        val a = Math.toRadians(k * 60.0)
        drawLine(Color(0xFFFF9A2E), sc, Offset(sc.x + sr * 0.74f * kotlin.math.cos(a).toFloat(), sc.y + sr * 0.74f * kotlin.math.sin(a).toFloat()), pen.lw * 0.45f)
    }
}

/** A tall glass of smoothie; the variant is its fruit colour. */
internal fun DrawScope.thSmoothie(v: Int, used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = argb(Palette.juice[v.mod(Palette.juice.size)])
    val glass = thSketch(w, h) {
        m(-0.44f, -0.84f); c(-0.46f, -0.5f, -0.3f, -0.22f, -0.3f, -0.06f); q(-0.3f, -0.02f, -0.26f, -0.02f)
        l(0.26f, -0.02f); q(0.3f, -0.02f, 0.3f, -0.06f); c(0.3f, -0.22f, 0.46f, -0.5f, 0.44f, -0.84f); z()
    }
    drawPath(glass, ThingInk.glass)
    val straw = thSketch(w, h) { m(0.08f, -0.2f); l(0.18f, -0.9f); l(0.38f, -0.99f) }
    drawPath(straw, Ink.line, style = Stroke(w * 0.09f + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(straw, ThingInk.pink, style = Stroke(w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(straw, Color.White.copy(alpha = 0.7f), style = Stroke(w * 0.025f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    val lv = left(used)
    val y = -0.1f - 0.64f * lv
    clipPath(glass) {
        if (lv < 1f) {
            for (k in 0 until 3) {
                val x = -0.22f + k * 0.2f
                drawLine(col.copy(alpha = 0.35f), o(x, -0.76f), o(x + 0.02f, y), w * 0.06f, StrokeCap.Round)
            }
        }
        if (lv > 0f) {
            drawRect(col, o(-0.5f, y), Size(w, -y * h))
            drawOval(col.lighten(0.3f), o(-0.46f, y - 0.03f), Size(0.92f * w, 0.06f * h))
            drawCircle(col.darken(0.25f), w * 0.018f, o(-0.1f, y + 0.12f))
            drawCircle(col.darken(0.25f), w * 0.015f, o(0.12f, y + 0.2f))
            drawCircle(col.darken(0.25f), w * 0.016f, o(-0.02f, y + 0.3f))
        }
    }
    drawPath(glass, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawLine(Color.White.copy(alpha = 0.7f), o(-0.32f, -0.74f), o(-0.24f, -0.3f), w * 0.07f, StrokeCap.Round)
    val sc = o(-0.36f, -0.84f)
    val sr = w * 0.18f
    inkedCircle(sc, sr, col, pen)
    drawCircle(col.lighten(0.45f), sr * 0.7f, sc)
    drawCircle(col.darken(0.2f), sr * 0.14f, sc)
    thFill(thLens(o(-0.24f, -0.86f), o(-0.06f, -0.96f), w * 0.05f), Color(0xFF3FAE49), pen)
}

/** An everyday mug of cocoa with marshmallows, steaming. */
internal fun DrawScope.thCocoa(used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val mug = Color(0xFF4AA3E8)
    val handle = thSketch(w, h) { m(0.2f, -0.6f); c(0.52f, -0.66f, 0.54f, -0.2f, 0.2f, -0.24f) }
    drawPath(handle, Ink.line, style = Stroke(w * 0.11f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(handle, mug.shadow(), style = Stroke(w * 0.11f, cap = StrokeCap.Round))
    val body = thSketch(w, h) {
        m(-0.4f, -0.72f); l(0.26f, -0.72f); l(0.24f, -0.12f); q(0.22f, -0.02f, 0.12f, -0.02f)
        l(-0.26f, -0.02f); q(-0.36f, -0.02f, -0.38f, -0.12f); z()
    }
    inked(body, mug, pen, outline = false)
    clipPath(body) {
        drawRect(Color.White, o(-0.5f, -0.56f), Size(w, 0.08f * h))
        drawRect(Color.White, o(-0.5f, -0.42f), Size(w, 0.03f * h))
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val rim = Rect(-0.4f * w, -0.86f * h, 0.26f * w, -0.6f * h)
    drawOval(mug.lighten(0.25f), rim.topLeft, rim.size)
    val inner = Rect(rim.left + pen.lw, rim.top + pen.lw * 0.8f, rim.right - pen.lw, rim.bottom - pen.lw * 0.6f)
    val lv = left(used)
    val drop = (1f - lv) * inner.height * 0.45f
    clipPath(ovalPath(inner)) {
        drawOval(mug.darken(0.35f), inner.topLeft, inner.size)
        drawOval(Color(0xFF7A4A2C), Offset(inner.left, inner.top + drop + inner.height * 0.12f), inner.size)
        drawOval(Color(0xFF9A6240), Offset(inner.left + inner.width * 0.15f, inner.top + drop + inner.height * 0.2f), Size(inner.width * 0.4f, inner.height * 0.25f))
    }
    drawOval(Ink.line, rim.topLeft, rim.size, style = pen.stroke)
    // Marshmallows bob in the cocoa; one goes with every sip.
    val n = (3 * lv + 0.99f).toInt().coerceIn(0, 3)
    for (k in 0 until n) {
        val cx = inner.left + inner.width * (0.26f + k * 0.24f)
        val cy = inner.top + inner.height * (0.34f + (k % 2) * 0.18f) + drop + sin(pen.t * 1.5f + k) * h * 0.01f
        val s = w * 0.15f
        val cube = Rect(cx - s / 2, cy - s * 0.55f, cx + s / 2, cy + s * 0.25f)
        drawRoundRect(if (k == 1) Color(0xFFFFD6E4) else Color.White, cube.topLeft, cube.size, CornerRadius(s * 0.25f))
        drawRoundRect(Color.White, cube.topLeft, Size(cube.width, cube.height * 0.4f), CornerRadius(s * 0.25f))
        drawRoundRect(Ink.line, cube.topLeft, cube.size, CornerRadius(s * 0.25f), style = pen.thin)
    }
    shine(o(-0.28f, -0.46f), w * 0.07f, h * 0.18f, 0.6f)
    if (lv > 0f) {
        for (k in 0 until 3) {
            val ph = (pen.t * 0.5f + k * 0.33f) % 1f
            val x = -0.26f + k * 0.2f
            val s = sin(pen.t * 2.2f + k * 1.9f) * 0.05f
            val y0 = -0.82f - ph * 0.06f
            val steam = thSketch(w, h) { m(x, y0); q(x + 0.08f + s, y0 - 0.05f, x, y0 - 0.09f); q(x - 0.08f - s, y0 - 0.13f, x + s, y0 - 0.17f) }
            drawPath(steam, Color.White.copy(alpha = 0.8f * (1f - ph)), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        }
    }
}

// ------------------------------------------------------------------ potions

private fun DrawScope.cork(r: Rect, pen: Pen) {
    inkedRound(r, r.width * 0.2f, ThingInk.wood, pen)
    drawLine(ThingInk.woodDark, Offset(r.left + r.width * 0.3f, r.top + r.height * 0.35f), Offset(r.left + r.width * 0.5f, r.top + r.height * 0.35f), r.height * 0.1f, StrokeCap.Round)
    drawLine(ThingInk.woodDark, Offset(r.left + r.width * 0.55f, r.top + r.height * 0.65f), Offset(r.left + r.width * 0.72f, r.top + r.height * 0.65f), r.height * 0.1f, StrokeCap.Round)
}

/** Bubbles that rise from [bottom] to [top] and start over, each on its own beat. */
private fun DrawScope.bubbles(t: Float, cx: Float, bottom: Float, top: Float, spread: Float, n: Int, size: Float, speed: Float, pen: Pen) {
    for (i in 0 until n) {
        val ph = (t * speed + i * 0.618f) % 1f
        val y = bottom + (top - bottom) * ph
        val x = cx + sin(t * 2.3f + i * 1.7f) * spread + (i - n / 2f) * spread * 0.4f
        val r = size * (0.6f + 0.2f * (i % 3))
        val a = if (ph > 0.85f) (1f - ph) / 0.15f else 1f
        drawCircle(Color.White.copy(alpha = 0.3f * a), r, Offset(x, y))
        drawCircle(Color.White.copy(alpha = 0.85f * a), r, Offset(x, y), style = Stroke(pen.lw * 0.45f))
    }
}

/** Glass, liquid up to [level] (y in pixels), and the ink rim. */
private fun DrawScope.bottle(body: Path, liquid: Color, level: Float, pen: Pen, inside: DrawScope.() -> Unit = {}) {
    drawPath(body, ThingInk.glass)
    clipPath(body) {
        drawRect(liquid, Offset(-1e4f, level), Size(2e4f, 2e4f))
        drawRect(liquid.lighten(0.35f), Offset(-1e4f, level), Size(2e4f, pen.lw * 0.9f))
        drawRect(liquid.darken(0.18f), Offset(-1e4f, level + (body.getBounds().bottom - level) * 0.6f), Size(2e4f, 2e4f))
        inside()
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
}

/** The grow potion: a red heart-shaped bottle. */
internal fun DrawScope.thPotionGrow(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val red = Color(0xFFFF4D6D)
    thGlow(o(0f, -0.36f), w * 0.62f, red, 0.28f + 0.1f * sin(pen.t * 2.2f))
    val neck = Rect(-0.12f * w, -0.8f * h, 0.12f * w, -0.54f * h)
    drawRect(ThingInk.glass, neck.topLeft, neck.size)
    drawRect(Ink.line, neck.topLeft, neck.size, style = pen.thin)
    val heart = thSketch(w, h) {
        m(0f, -0.5f)
        c(0.1f, -0.66f, 0.5f, -0.7f, 0.48f, -0.44f)
        c(0.46f, -0.24f, 0.2f, -0.12f, 0f, -0.03f)
        c(-0.2f, -0.12f, -0.46f, -0.24f, -0.48f, -0.44f)
        c(-0.5f, -0.7f, -0.1f, -0.66f, 0f, -0.5f)
        z()
    }
    bottle(heart, red, -0.46f * h, pen) { bubbles(pen.t, 0f, -0.1f * h, -0.44f * h, w * 0.12f, 3, w * 0.05f, 0.35f, pen) }
    shine(o(-0.26f, -0.52f), w * 0.14f, h * 0.06f)
    cork(Rect(-0.17f * w, -0.97f * h, 0.17f * w, -0.8f * h), pen)
    for (s in SIDES) thFill(thSketch(w, h) { m(0f, -0.62f); l(s * 0.26f, -0.68f); l(s * 0.26f, -0.56f); z() }, ThingInk.pink, pen)
    thDot(o(0f, -0.62f), w * 0.05f, ThingInk.pink, pen)
}

/** The shrink potion: a small round blue flask. */
internal fun DrawScope.thPotionShrink(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val blue = Color(0xFF4AB3FF)
    val c = o(0f, -0.27f)
    val r = w * 0.36f
    thGlow(c, w * 0.52f, blue, 0.28f + 0.1f * sin(pen.t * 2.4f))
    val neck = Rect(-0.1f * w, -0.62f * h, 0.1f * w, -0.44f * h)
    drawRect(ThingInk.glass, neck.topLeft, neck.size)
    drawRect(Ink.line, neck.topLeft, neck.size, style = pen.thin)
    val flask = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    bottle(flask, blue, -0.34f * h, pen) { bubbles(pen.t, 0f, -0.08f * h, -0.34f * h, w * 0.08f, 2, w * 0.04f, 0.4f, pen) }
    shine(Offset(c.x - r * 0.4f, c.y - r * 0.4f), r * 0.4f, r * 0.26f)
    cork(Rect(-0.14f * w, -0.73f * h, 0.14f * w, -0.6f * h), pen)
    // Little twinkles drift down toward it.
    for (k in 0 until 2) {
        val ph = (pen.t * 0.4f + k * 0.5f) % 1f
        twinkle(o(-0.22f + k * 0.44f, -0.98f + ph * 0.18f), w * 0.1f * (1f - ph * 0.5f), Color(0xFFBFE6FF), 1f - ph)
    }
}

/** The rainbow potion: a round flask with stripes of every colour. */
internal fun DrawScope.thPotionRainbow(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val c = o(0f, -0.31f)
    val r = w * 0.46f
    thGlow(c, w * 0.62f, Color(0xFFFFE066), 0.25f + 0.1f * sin(pen.t * 2f))
    val neck = Rect(-0.12f * w, -0.84f * h, 0.12f * w, -0.52f * h)
    drawRect(ThingInk.glass, neck.topLeft, neck.size)
    drawRect(Ink.line, neck.topLeft, neck.size, style = pen.thin)
    val flask = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    val top = -0.46f * h
    val band = (c.y + r - top) / 6f
    drawPath(flask, ThingInk.glass)
    clipPath(flask) {
        for (k in 0 until 6) {
            val slosh = sin(pen.t * 2f + k * 0.8f) * band * 0.15f
            drawRect(Color(RAINBOW6[k]), Offset(c.x - r, top + k * band + slosh), Size(r * 2f, band * 1.6f))
        }
        drawRect(Color.White.copy(alpha = 0.5f), Offset(c.x - r, top), Size(r * 2f, pen.lw * 0.8f))
        bubbles(pen.t, 0f, c.y + r * 0.8f, top, w * 0.1f, 3, w * 0.045f, 0.3f, pen)
    }
    thCrescent(c, r, 0.14f)
    drawPath(flask, Ink.line, style = pen.stroke)
    shine(Offset(c.x - r * 0.45f, c.y - r * 0.4f), r * 0.36f, r * 0.2f)
    cork(Rect(-0.16f * w, -0.98f * h, 0.16f * w, -0.82f * h), pen)
    thGlint(o(0.34f, -0.7f), w * 0.16f, pen.t, 2.6f, 0f)
}

/** The float potion: a slim teardrop bottle of cyan with bubbles rising. */
internal fun DrawScope.thPotionFloat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val cyan = Color(0xFF4FE0E8)
    thGlow(o(0f, -0.35f), w * 0.6f, cyan, 0.26f + 0.1f * sin(pen.t * 2.6f))
    val neck = Rect(-0.1f * w, -0.86f * h, 0.1f * w, -0.66f * h)
    drawRect(ThingInk.glass, neck.topLeft, neck.size)
    drawRect(Ink.line, neck.topLeft, neck.size, style = pen.thin)
    val drop = thSketch(w, h) {
        m(-0.1f, -0.7f)
        c(-0.3f, -0.56f, -0.46f, -0.34f, -0.42f, -0.16f)
        q(-0.38f, -0.02f, 0f, -0.02f)
        q(0.38f, -0.02f, 0.42f, -0.16f)
        c(0.46f, -0.34f, 0.3f, -0.56f, 0.1f, -0.7f)
        z()
    }
    bottle(drop, cyan, -0.56f * h, pen) { bubbles(pen.t, 0f, -0.06f * h, -0.56f * h, w * 0.14f, 5, w * 0.05f, 0.5f, pen) }
    shine(o(-0.22f, -0.36f), w * 0.1f, h * 0.12f)
    cork(Rect(-0.14f * w, -0.99f * h, 0.14f * w, -0.84f * h), pen)
}

/** The back-to-normal potion: clear, in a square bottle with a silver stopper. */
internal fun DrawScope.thPotionNormal(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val clear = Color(0xFFE4F1FF)
    thGlow(o(0f, -0.34f), w * 0.58f, Color.White, 0.3f + 0.1f * sin(pen.t * 2f))
    val neck = Rect(-0.12f * w, -0.78f * h, 0.12f * w, -0.6f * h)
    drawRect(ThingInk.glass, neck.topLeft, neck.size)
    drawRect(Ink.line, neck.topLeft, neck.size, style = pen.thin)
    val body = thSketch(w, h) {
        m(-0.3f, -0.64f); l(0.3f, -0.64f); q(0.42f, -0.64f, 0.42f, -0.52f); l(0.42f, -0.12f); q(0.42f, -0.02f, 0.32f, -0.02f)
        l(-0.32f, -0.02f); q(-0.42f, -0.02f, -0.42f, -0.12f); l(-0.42f, -0.52f); q(-0.42f, -0.64f, -0.3f, -0.64f); z()
    }
    bottle(body, clear, -0.5f * h, pen) {
        bubbles(pen.t, 0f, -0.06f * h, -0.5f * h, w * 0.12f, 3, w * 0.04f, 0.3f, pen)
        drawRect(Color(0xFFBFD8F2), o(-0.5f, -0.5f), Size(w, pen.lw * 0.8f))
    }
    val label = Rect(-0.26f * w, -0.4f * h, 0.26f * w, -0.16f * h)
    drawRoundRect(ThingInk.cream, label.topLeft, label.size, CornerRadius(w * 0.04f))
    drawRoundRect(Ink.line, label.topLeft, label.size, CornerRadius(w * 0.04f), style = pen.thin)
    val drop = thSketch(w, h) { m(0f, -0.36f); q(0.1f, -0.24f, 0f, -0.2f); q(-0.1f, -0.24f, 0f, -0.36f); z() }
    drawPath(drop, Color(0xFF8EB8E6))
    shine(o(-0.3f, -0.52f), w * 0.07f, h * 0.1f)
    inkedRound(Rect(-0.17f * w, -0.86f * h, 0.17f * w, -0.76f * h), w * 0.04f, ThingInk.silver, pen)
    inkedCircle(o(0f, -0.91f), w * 0.13f, ThingInk.silver, pen)
    shine(o(-0.05f, -0.93f), w * 0.06f, h * 0.025f)
    thGlint(o(0.3f, -0.6f), w * 0.14f, pen.t, 2.2f, 1f)
}
