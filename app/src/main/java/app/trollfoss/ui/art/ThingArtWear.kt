package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.Garment
import app.trollfoss.domain.Palette
import kotlin.math.cos
import kotlin.math.sin

// Hats sit on a grown-up head: their bottom edge (y = 0) is where they rest. Glasses are centred in
// their box, the space helmet is a bubble centred on the head.

/** Rows of little knitted V stitches, as one path, for knitted hats. */
private fun knitPath(w: Float, h: Float, top: Float, bottom: Float, step: Float): Path = Path().apply {
    var y = top
    while (y < bottom) {
        var x = -0.46f
        while (x < 0.46f) {
            moveTo((x - step * 0.3f) * w, (y - step * 0.35f) * h)
            lineTo(x * w, y * h)
            lineTo((x + step * 0.3f) * w, (y - step * 0.35f) * h)
            x += step * 0.62f
        }
        y += step
    }
}

private val CAP_COL = longArrayOf(0xFFFF5A4E, 0xFF3D6BFF, 0xFF3BC46B, 0xFFFFC83D)

internal fun DrawScope.thCap(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(CAP_COL[v.mod(4)])
    val dome = thSketch(w, h) {
        m(-0.42f, -0.26f); c(-0.44f, -0.8f, -0.22f, -0.98f, 0f, -0.98f); c(0.22f, -0.98f, 0.44f, -0.8f, 0.42f, -0.26f)
        q(0f, -0.2f, -0.42f, -0.26f); z()
    }
    inked(dome, col, pen)
    val seam = col.darken(0.3f)
    drawPath(thSketch(w, h) { m(0f, -0.96f); q(-0.2f, -0.8f, -0.22f, -0.26f); m(0f, -0.96f); q(0.2f, -0.8f, 0.22f, -0.26f) }, seam, style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
    drawCircle(seam, w * 0.014f, o(-0.3f, -0.66f))
    drawCircle(seam, w * 0.014f, o(0.3f, -0.66f))
    val badge = o(0f, -0.58f)
    thDot(badge, w * 0.085f, Color.White, pen)
    drawPath(starPath(badge, w * 0.06f, w * 0.027f), col)
    inkedCircle(o(0f, -0.965f), w * 0.035f, col.darken(0.12f), pen, shade = false)
    shine(o(-0.2f, -0.8f), w * 0.1f, h * 0.1f, 0.5f)
    val brim = thSketch(w, h) {
        m(-0.47f, -0.3f); q(0f, -0.42f, 0.47f, -0.3f); q(0.5f, -0.12f, 0.3f, -0.04f); q(0f, 0.01f, -0.3f, -0.04f)
        q(-0.5f, -0.12f, -0.47f, -0.3f); z()
    }
    inked(brim, col.darken(0.1f), pen)
    val stitch = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(pen.lw * 1.2f, pen.lw * 1.2f)))
    drawPath(thSketch(w, h) { m(-0.38f, -0.16f); q(0f, -0.04f, 0.38f, -0.16f) }, col.lighten(0.45f), style = stitch)
}

private val BEANIE_COL = longArrayOf(0xFFE8554E, 0xFF3D6BFF, 0xFF1FB5A8, 0xFFF2B233)

/** An ordinary knitted beanie with one stripe and a pompom. */
internal fun DrawScope.thBeanie(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(BEANIE_COL[v.mod(4)])
    val dome = thSketch(w, h) {
        m(-0.44f, -0.26f); c(-0.46f, -0.74f, -0.22f, -0.8f, 0f, -0.8f); c(0.22f, -0.8f, 0.46f, -0.74f, 0.44f, -0.26f); z()
    }
    inked(dome, col, pen, outline = false)
    clipPath(dome) {
        drawRect(Color.White, o(-0.5f, -0.58f), Size(w, 0.1f * h))
        drawPath(knitPath(w, h, -0.76f, -0.3f, 0.09f), col.darken(0.2f), style = Stroke(pen.lw * 0.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    drawPath(dome, Ink.line, style = pen.stroke)
    val cuff = Rect(-0.48f * w, -0.36f * h, 0.48f * w, -0.02f * h)
    inkedRound(cuff, h * 0.1f, col.darken(0.06f), pen)
    for (k in -7..7) drawLine(col.darken(0.3f), o(k * 0.06f, -0.31f), o(k * 0.06f, -0.07f), pen.lw * 0.5f, StrokeCap.Round)
    val pc = o(0f, -0.83f)
    val pr = w * 0.1f
    thUnion(thFluff(pc, pr, 9), Color(0xFFFFFBF4), pen)
    val fleck = Color(0xFFD9D2C8)
    drawLine(fleck, Offset(pc.x - pr * 0.4f, pc.y), Offset(pc.x - pr * 0.1f, pc.y + pr * 0.3f), pen.lw * 0.4f, StrokeCap.Round)
    drawLine(fleck, Offset(pc.x + pr * 0.2f, pc.y - pr * 0.3f), Offset(pc.x + pr * 0.45f, pc.y), pen.lw * 0.4f, StrokeCap.Round)
}

/** 0: a gold crown with jewels. 1: the grand star crown, glowing, with a star on top. */
internal fun DrawScope.thCrown(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val gold = Color(0xFFFFC83D)
    if (v.mod(2) == 0) {
        val crown = thSketch(w, h) {
            m(-0.42f, -0.04f); l(-0.46f, -0.64f); l(-0.31f, -0.42f); l(-0.22f, -0.78f); l(-0.1f, -0.46f); l(0f, -0.88f)
            l(0.1f, -0.46f); l(0.22f, -0.78f); l(0.31f, -0.42f); l(0.46f, -0.64f); l(0.42f, -0.04f); q(0f, 0.02f, -0.42f, -0.04f); z()
        }
        inked(crown, gold, pen)
        drawPath(thSketch(w, h) { m(-0.44f, -0.3f); q(0f, -0.24f, 0.44f, -0.3f) }, gold.darken(0.35f), style = pen.thin)
        drawPath(thSketch(w, h) { m(-0.4f, -0.36f); q(0f, -0.3f, 0.4f, -0.36f) }, Color.White.copy(alpha = 0.5f), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
        crownGem(o(0f, -0.15f), w * 0.07f, h * 0.2f, Color(0xFFFF3B5C), pen)
        crownGem(o(-0.26f, -0.16f), w * 0.05f, w * 0.05f * 2f, Color(0xFF3D7BFF), pen)
        crownGem(o(0.26f, -0.16f), w * 0.05f, w * 0.05f * 2f, Color(0xFF2FD18B), pen)
        for (k in 0 until 5) inkedCircle(o(CROWN_TIPS[k * 2], CROWN_TIPS[k * 2 + 1]), w * 0.035f, gold.lighten(0.2f), pen, shade = false)
        for (k in 0 until 5) thGlint(o(CROWN_TIPS[k * 2] + 0.03f, CROWN_TIPS[k * 2 + 1] - 0.02f), w * 0.09f, pen.t, 1.7f, k * 1.3f)
    } else {
        val velvet = Color(0xFFC8283C)
        inked(thSketch(w, h) { m(-0.38f, -0.3f); c(-0.4f, -0.74f, 0.4f, -0.74f, 0.38f, -0.3f); z() }, velvet, pen)
        val crown = thSketch(w, h) {
            m(-0.44f, -0.04f); l(-0.47f, -0.56f); l(-0.33f, -0.38f); l(-0.22f, -0.64f); l(-0.1f, -0.4f); l(-0.04f, -0.62f)
            q(0f, -0.7f, 0.04f, -0.62f); l(0.1f, -0.4f); l(0.22f, -0.64f); l(0.33f, -0.38f); l(0.47f, -0.56f); l(0.44f, -0.04f)
            q(0f, 0.02f, -0.44f, -0.04f); z()
        }
        inked(crown, gold, pen)
        drawPath(thSketch(w, h) { m(-0.45f, -0.3f); q(0f, -0.24f, 0.45f, -0.3f) }, gold.darken(0.35f), style = pen.thin)
        for (k in 0 until 9) {
            val x = -0.4f + k * 0.1f
            val t = (x + 0.45f) / 0.9f
            drawCircle(Color.White, w * 0.018f, o(x, -0.3f + 0.12f * t * (1f - t) + 0.035f))
        }
        crownGem(o(0f, -0.14f), w * 0.07f, h * 0.18f, Color(0xFF9B6BFF), pen)
        crownGem(o(-0.26f, -0.14f), w * 0.05f, w * 0.1f, Color(0xFFFF3B5C), pen)
        crownGem(o(0.26f, -0.14f), w * 0.05f, w * 0.1f, Color(0xFF3D7BFF), pen)
        for (k in 0 until 4) inkedCircle(o(CROWN2_TIPS[k * 2], CROWN2_TIPS[k * 2 + 1]), w * 0.032f, Color.White, pen, shade = false)
        val sc = o(0f, -0.8f)
        val pulse = 0.5f + 0.5f * sin(pen.t * 2.4f)
        thGlow(sc, w * 0.24f, Color(0xFFFFE066), 0.5f + 0.25f * pulse)
        inked(starPath(sc, h * 0.2f, h * 0.09f, sin(pen.t * 1.2f) * 6f), Color(0xFFFFE066), pen)
        shine(Offset(sc.x - h * 0.04f, sc.y - h * 0.05f), h * 0.06f, h * 0.04f)
        for (k in 0 until 3) {
            val a = pen.t * 1.4f + k * 2.094f
            thGlint(Offset(sc.x + cos(a) * w * 0.2f, sc.y + sin(a) * h * 0.14f), w * 0.07f, pen.t, 3f, k * 2f)
        }
    }
}

private val CROWN_TIPS = floatArrayOf(-0.46f, -0.64f, -0.22f, -0.78f, 0f, -0.88f, 0.22f, -0.78f, 0.46f, -0.64f)
private val CROWN2_TIPS = floatArrayOf(-0.47f, -0.56f, -0.22f, -0.64f, 0.22f, -0.64f, 0.47f, -0.56f)

private fun DrawScope.crownGem(c: Offset, rx: Float, ry: Float, color: Color, pen: Pen) {
    val r = Rect(c.x - rx, c.y - ry / 2, c.x + rx, c.y + ry / 2)
    drawOval(color, r.topLeft, r.size)
    drawOval(Ink.line, r.topLeft, r.size, style = pen.thin)
    drawOval(Color.White.copy(alpha = 0.75f), Offset(c.x - rx * 0.55f, c.y - ry * 0.32f), Size(rx * 0.6f, ry * 0.3f))
}

private val PARTY_COL = longArrayOf(0xFFFF6FA8, 0xFF4AB3FF, 0xFFFFD23F, 0xFF3BC46B)
private val PARTY_DOT = longArrayOf(0xFFFFE066, 0xFFFFFFFF, 0xFFFF5A4E, 0xFFFFFFFF)
private val PARTY_DOTS = floatArrayOf(-0.2f, -0.3f, 0.12f, -0.22f, -0.04f, -0.5f, 0.2f, -0.44f, -0.26f, -0.16f, 0.04f, -0.7f, 0.3f, -0.2f)

internal fun DrawScope.thPartyHat(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val i = v.mod(4)
    val col = Color(PARTY_COL[i])
    val dot = Color(PARTY_DOT[i])
    val tip = o(0f, -0.84f)
    for (k in 0 until 7) {
        val a = Math.toRadians(-90.0 + (k - 3) * 24.0)
        val len = h * (0.13f + 0.02f * (k % 2))
        val end = Offset(tip.x + (len * cos(a)).toFloat(), tip.y + (len * sin(a)).toFloat())
        capsule(tip, end, w * 0.035f, if (k % 2 == 0) dot else col.lighten(0.3f), pen)
    }
    val cone = thSketch(w, h) { m(-0.44f, -0.1f); l(0f, -0.84f); l(0.44f, -0.1f); q(0f, 0.06f, -0.44f, -0.1f); z() }
    inked(cone, col, pen, outline = false)
    clipPath(cone) {
        if (i % 2 == 0) {
            for (k in 0 until 7) drawCircle(dot, w * 0.055f, o(PARTY_DOTS[k * 2], PARTY_DOTS[k * 2 + 1]))
        } else {
            for (k in 0 until 5) drawLine(dot, o(-0.6f, -0.2f - k * 0.17f), o(0.6f, 0.04f - k * 0.17f), h * 0.05f)
        }
        drawPath(thSketch(w, h) { m(-0.5f, -0.1f); q(0f, 0.06f, 0.5f, -0.1f) }, Color.White, style = Stroke(h * 0.16f))
    }
    drawPath(thSketch(w, h) { m(-0.4f, -0.16f); q(0f, -0.02f, 0.4f, -0.16f) }, col.darken(0.2f), style = pen.thin)
    drawPath(cone, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    inkedCircle(tip, w * 0.07f, dot, pen)
    shine(o(-0.14f, -0.46f), w * 0.05f, h * 0.12f, 0.5f)
}

internal fun DrawScope.thVikingHelmet(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val horn = Color(0xFFF3E7C9)
    for (s in SIDES) {
        val path = thSketch(w, h) { m(s * 0.3f, -0.36f); q(s * 0.5f, -0.46f, s * 0.47f, -0.98f); q(s * 0.4f, -0.6f, s * 0.28f, -0.6f); z() }
        inked(path, horn, pen)
        drawLine(horn.darken(0.3f), o(s * 0.34f, -0.4f), o(s * 0.33f, -0.58f), pen.lw * 0.6f, StrokeCap.Round)
        drawLine(horn.darken(0.3f), o(s * 0.42f, -0.56f), o(s * 0.38f, -0.7f), pen.lw * 0.6f, StrokeCap.Round)
    }
    val steel = Color(0xFFB8C2D4)
    val dome = thSketch(w, h) { m(-0.34f, -0.22f); c(-0.36f, -0.74f, -0.18f, -0.9f, 0f, -0.9f); c(0.18f, -0.9f, 0.36f, -0.74f, 0.34f, -0.22f); z() }
    inked(dome, steel, pen)
    val bronze = Color(0xFFD9A441)
    val ridge = thSketch(w, h) { m(-0.04f, -0.9f); q(0f, -0.91f, 0.04f, -0.9f); l(0.05f, -0.26f); l(-0.05f, -0.26f); z() }
    inked(ridge, bronze, pen, shade = false)
    inkedRound(Rect(-0.38f * w, -0.3f * h, 0.38f * w, -0.03f * h), h * 0.08f, bronze, pen)
    for (k in -3..3) drawCircle(bronze.darken(0.45f), w * 0.012f, o(k * 0.1f, -0.165f))
    drawCircle(bronze.darken(0.45f), w * 0.011f, o(0f, -0.72f))
    drawCircle(bronze.darken(0.45f), w * 0.011f, o(0f, -0.5f))
    shine(o(-0.18f, -0.7f), w * 0.07f, h * 0.14f)
}

private val WREATH = longArrayOf(0xFFFFFFFF, 0xFF7B8CF0, 0xFFFFD23F, 0xFFFF9EC7, 0xFFFFFFFF, 0xFF7B8CF0, 0xFFFFD23F)

internal fun DrawScope.thFlowerCrown(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    fun arcY(x: Float) = -0.52f + 1.2f * x * x
    val vine = thSketch(w, h) { m(-0.48f, -0.24f); q(0f, -0.86f, 0.48f, -0.24f) }
    drawPath(vine, Ink.line, style = Stroke(h * 0.12f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(vine, Color(0xFF3F9E47), style = Stroke(h * 0.12f, cap = StrokeCap.Round))
    val leaf = Color(0xFF5DBB4A)
    for (k in 0 until 6) {
        val x = -0.36f + k * 0.145f
        val y = arcY(x)
        val up = if (k % 2 == 0) -1f else 1f
        inked(thLens(o(x, y), o(x + 0.07f, y + up * 0.36f), h * 0.07f), leaf, pen, shade = false)
    }
    for (k in 0 until 7) {
        val x = -0.42f + k * 0.14f
        val c = o(x, arcY(x) - 0.12f)
        val r = h * (0.28f - 0.03f * kotlin.math.abs(k - 3))
        val petal = Color(WREATH[k])
        thBlossom(c, r, if (k % 3 == 1) 5 else 6, petal, if (petal == Color.White) Color(0xFFFFD23F) else Color(0xFFFFF4C2), pen)
    }
}

private val PETAL_COS = Array(2) { n -> FloatArray(if (n == 0) 5 else 6) { cos(Math.toRadians(-90.0 + it * 360.0 / (if (n == 0) 5 else 6))).toFloat() } }
private val PETAL_SIN = Array(2) { n -> FloatArray(if (n == 0) 5 else 6) { sin(Math.toRadians(-90.0 + it * 360.0 / (if (n == 0) 5 else 6))).toFloat() } }

/** A flower seen from the front: round petals and a centre. */
internal fun DrawScope.thBlossom(c: Offset, r: Float, petals: Int, petal: Color, heart: Color, pen: Pen) {
    val n = if (petals == 5) 0 else 1
    val cs = PETAL_COS[n]
    val sn = PETAL_SIN[n]
    val path = Path()
    val pr = r * 0.46f
    for (i in cs.indices) {
        val px = c.x + r * 0.54f * cs[i]
        val py = c.y + r * 0.54f * sn[i]
        path.addOval(Rect(px - pr, py - pr, px + pr, py + pr))
    }
    thUnion(path, petal, pen)
    drawCircle(heart, r * 0.3f, c)
    drawCircle(Ink.line, r * 0.3f, c, style = pen.thin)
    drawCircle(heart.darken(0.3f), r * 0.08f, Offset(c.x + r * 0.08f, c.y + r * 0.06f))
}

internal fun DrawScope.thChefHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val white = Color(0xFFFFFFFF)
    val puff = Path().apply {
        addOval(rect(-0.25f * w, -0.62f * h, 0.5f * w, 0.36f * h))
        addOval(rect(0.25f * w, -0.62f * h, 0.5f * w, 0.36f * h))
        addOval(rect(0f, -0.74f * h, 0.62f * w, 0.48f * h))
        addRect(Rect(-0.36f * w, -0.62f * h, 0.36f * w, -0.3f * h))
    }
    thUnion(puff, white, pen)
    val crease = Color(0xFFC9D6EE)
    drawArc(crease, 200f, 80f, false, o(-0.2f, -0.76f), Size(0.24f * w, 0.3f * h), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
    drawArc(crease, 260f, 80f, false, o(-0.02f, -0.74f), Size(0.24f * w, 0.28f * h), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
    drawLine(crease, o(-0.12f, -0.58f), o(-0.1f, -0.4f), pen.lw * 0.6f, StrokeCap.Round)
    drawLine(crease, o(0.14f, -0.6f), o(0.12f, -0.42f), pen.lw * 0.6f, StrokeCap.Round)
    val band = Rect(-0.32f * w, -0.36f * h, 0.32f * w, -0.02f * h)
    inkedRound(band, w * 0.05f, white, pen)
    for (k in -2..2) drawLine(crease, o(k * 0.11f, -0.32f), o(k * 0.11f, -0.06f), pen.lw * 0.6f, StrokeCap.Round)
}

internal fun DrawScope.thSunHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val straw = Color(0xFFF2D08A)
    val weave = straw.darken(0.25f)
    val brim = Rect(-0.5f * w, -0.44f * h, 0.5f * w, -0.01f * h)
    inkedOval(brim, straw, pen)
    drawOval(weave, Offset(brim.left + w * 0.06f, brim.top + h * 0.05f), Size(brim.width - w * 0.12f, brim.height - h * 0.1f), style = Stroke(pen.lw * 0.45f))
    drawOval(weave, Offset(brim.left + w * 0.12f, brim.top + h * 0.1f), Size(brim.width - w * 0.24f, brim.height - h * 0.2f), style = Stroke(pen.lw * 0.45f))
    val crown = thSketch(w, h) { m(-0.25f, -0.24f); c(-0.28f, -0.8f, -0.14f, -0.96f, 0f, -0.96f); c(0.14f, -0.96f, 0.28f, -0.8f, 0.25f, -0.24f); q(0f, -0.16f, -0.25f, -0.24f); z() }
    inked(crown, straw, pen, outline = false)
    val ribbon = Color(0xFF3D7BD6)
    clipPath(crown) {
        for (k in -2..2) drawPath(thSketch(w, h) { m(k * 0.09f, -0.96f); q(k * 0.14f, -0.6f, k * 0.12f, -0.2f) }, weave, style = Stroke(pen.lw * 0.45f))
        drawPath(thSketch(w, h) { m(-0.3f, -0.34f); q(0f, -0.26f, 0.3f, -0.34f) }, ribbon, style = Stroke(h * 0.16f))
    }
    drawPath(crown, Ink.line, style = pen.stroke)
    for (s in SIDES) thFill(thSketch(w, h) { m(0.2f, -0.34f); l(0.2f + s * 0.08f, -0.44f); l(0.2f + s * 0.08f, -0.22f); z() }, ribbon, pen)
    thFill(thSketch(w, h) { m(0.2f, -0.3f); l(0.24f, -0.1f); l(0.28f, -0.14f); z() }, ribbon, pen)
    thDot(o(0.2f, -0.33f), w * 0.02f, ribbon.darken(0.1f), pen)
    thBlossom(o(-0.17f, -0.36f), h * 0.18f, 5, Color(0xFFFF6F8E), Color(0xFFFFE066), pen)
}

internal fun DrawScope.thWizardHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val purple = Color(0xFF6A4BD8)
    inkedOval(Rect(-0.5f * w, -0.2f * h, 0.5f * w, -0.01f * h), purple.darken(0.2f), pen)
    val cone = thSketch(w, h) {
        m(-0.34f, -0.12f); q(-0.14f, -0.5f, 0f, -0.84f); q(0.14f, -1f, 0.38f, -0.92f); q(0.16f, -0.86f, 0.1f, -0.74f)
        q(0.16f, -0.4f, 0.34f, -0.12f); q(0f, -0.04f, -0.34f, -0.12f); z()
    }
    inked(cone, purple, pen, outline = false)
    val gold = Color(0xFFFFD23F)
    clipPath(cone) { drawPath(thSketch(w, h) { m(-0.4f, -0.16f); q(0f, -0.08f, 0.4f, -0.16f) }, gold, style = Stroke(h * 0.08f)) }
    drawPath(cone, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val pulse = 1f + 0.12f * sin(pen.t * 3f)
    inked(starPath(o(-0.06f, -0.44f), w * 0.11f * pulse, w * 0.05f * pulse), gold, pen, shade = false)
    inked(starPath(o(0.14f, -0.28f), w * 0.055f, w * 0.025f, 15f), gold, pen, shade = false)
    drawPath(starPath(o(-0.16f, -0.24f), w * 0.04f, w * 0.018f, -10f), gold)
    val moon = o(0.07f, -0.63f)
    drawCircle(gold, w * 0.07f, moon)
    drawCircle(purple, w * 0.06f, Offset(moon.x + w * 0.035f, moon.y - w * 0.02f))
    inkedCircle(o(0.38f, -0.92f), w * 0.05f, gold, pen)
    thGlint(o(-0.2f, -0.6f), w * 0.07f, pen.t, 2.2f, 0f, gold.lighten(0.5f))
    thGlint(o(0.22f, -0.5f), w * 0.06f, pen.t, 2.2f, 2.5f, gold.lighten(0.5f))
    drawPath(thSketch(w, h) { m(-0.2f, -0.2f); q(-0.08f, -0.5f, 0f, -0.78f) }, Color.White.copy(alpha = 0.25f), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
}

private val BOW_COL = longArrayOf(0xFFFF5A4E, 0xFFFF6FA8, 0xFF4AB3FF, 0xFFFFD23F)

internal fun DrawScope.thBow(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(BOW_COL[v.mod(4)])
    for (s in SIDES) {
        val tail = thSketch(w, h) { m(s * 0.04f, -0.46f); l(s * 0.24f, -0.02f); l(s * 0.16f, -0.1f); l(s * 0.1f, -0.02f); l(-s * 0.04f, -0.46f); z() }
        inked(tail, col.darken(0.1f), pen)
    }
    for (s in SIDES) {
        val loop = thSketch(w, h) { m(s * 0.06f, -0.5f); c(s * 0.2f, -0.92f, s * 0.5f, -0.98f, s * 0.48f, -0.62f); c(s * 0.48f, -0.3f, s * 0.3f, -0.12f, s * 0.06f, -0.4f); z() }
        inked(loop, col, pen)
        drawPath(thSketch(w, h) { m(s * 0.1f, -0.5f); q(s * 0.3f, -0.62f, s * 0.38f, -0.74f) }, col.darken(0.3f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
        drawPath(thSketch(w, h) { m(s * 0.1f, -0.46f); q(s * 0.3f, -0.4f, s * 0.38f, -0.32f) }, col.darken(0.3f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
        drawCircle(Color.White.copy(alpha = 0.85f), w * 0.03f, o(s * 0.3f, -0.56f))
        drawCircle(Color.White.copy(alpha = 0.85f), w * 0.022f, o(s * 0.4f, -0.72f))
        drawCircle(Color.White.copy(alpha = 0.85f), w * 0.022f, o(s * 0.36f, -0.4f))
    }
    inkedRound(Rect(-0.1f * w, -0.66f * h, 0.1f * w, -0.3f * h), w * 0.05f, col.darken(0.08f), pen)
    shine(o(-0.03f, -0.56f), w * 0.04f, h * 0.1f, 0.6f)
}

internal fun DrawScope.thNisseHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val red = Color(0xFFD2443A)
    // Up to a point, then the long tip flops over to the right and hangs down.
    val cone = thSketch(w, h) {
        m(-0.42f, -0.26f); c(-0.4f, -0.6f, -0.22f, -0.9f, 0.04f, -0.98f); q(0.34f, -1.02f, 0.4f, -0.58f)
        l(0.3f, -0.6f); q(0.28f, -0.82f, 0.1f, -0.82f); c(0.2f, -0.64f, 0.38f, -0.46f, 0.42f, -0.26f); z()
    }
    inked(cone, red, pen, outline = false)
    clipPath(cone) { drawPath(knitPath(w, h, -0.9f, -0.3f, 0.1f), red.darken(0.2f), style = Stroke(pen.lw * 0.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
    drawPath(cone, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val cuff = Rect(-0.46f * w, -0.32f * h, 0.46f * w, -0.02f * h)
    inkedRound(cuff, h * 0.1f, red.darken(0.08f), pen)
    for (k in -7..7) drawLine(red.darken(0.32f), o(k * 0.058f, -0.28f), o(k * 0.058f, -0.06f), pen.lw * 0.5f, StrokeCap.Round)
    val pc = o(0.36f, -0.55f)
    val pr = w * 0.1f
    thUnion(thFluff(pc, pr, 9), Color(0xFFFFFBF4), pen)
    shine(o(-0.2f, -0.62f), w * 0.07f, h * 0.12f, 0.4f)
}

/** The astronaut's bubble helmet; the engine centres it on the head, so the bubble fills the box. */
internal fun DrawScope.thSpaceHelmet(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val c = o(0f, -0.52f)
    val r = h * 0.48f
    val bubble = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    drawPath(bubble, Color(0x1FBFE6FF))
    clipPath(bubble) {
        // A gold-tinted visor reflection that fades out towards the face.
        drawRect(
            Brush.verticalGradient(
                0f to Color(0x80FFD25A),
                0.35f to Color(0x40FFC83D),
                0.62f to Color(0x00FFC83D),
                startY = c.y - r,
                endY = c.y + r,
            ),
            Offset(c.x - r, c.y - r),
            Size(r * 2f, r * 2f),
        )
        drawCircle(Color.White.copy(alpha = 0.3f), r - pen.lw * 1.4f, c, style = Stroke(pen.lw * 0.8f))
    }
    drawArc(Color.White.copy(alpha = 0.75f), 200f, 55f, false, Offset(c.x - r * 0.82f, c.y - r * 0.82f), Size(r * 1.64f, r * 1.64f), style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = 0.8f), r * 0.06f, Offset(c.x - r * 0.28f, c.y - r * 0.8f))
    drawRoundRect(Color.White.copy(alpha = 0.45f), Offset(c.x + r * 0.34f, c.y - r * 0.66f), Size(r * 0.2f, r * 0.14f), androidx.compose.ui.geometry.CornerRadius(r * 0.04f))
    drawCircle(Ink.line, r, c, style = pen.stroke)
    val ring = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(-0.44f * w, -0.26f * h, 0.44f * w, 0f))
        addOval(Rect(-0.3f * w, -0.2f * h, 0.3f * w, -0.1f * h))
    }
    inked(ring, Color(0xFFF1F3F8), pen)
    drawOval(Color(0xFFC9D0DC), Offset(-0.3f * w, -0.2f * h), Size(0.6f * w, 0.1f * h), style = Stroke(pen.lw * 0.6f))
    thDot(o(-0.2f, -0.05f), w * 0.022f, Color(0xFFFF5A4E), pen)
    thDot(o(0f, -0.035f), w * 0.022f, Color(0xFF3BC46B), pen)
    thDot(o(0.2f, -0.05f), w * 0.022f, Color(0xFF4AB3FF), pen)
}

internal fun DrawScope.thSunglasses(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    for (s in SIDES) drawLine(Ink.line, o(s * 0.45f, -0.78f), o(s * 0.5f, -0.72f), pen.lw * 1.4f, StrokeCap.Round)
    drawPath(thSketch(w, h) { m(-0.07f, -0.74f); q(0f, -0.86f, 0.07f, -0.74f) }, Ink.line, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
    for (s in SIDES) {
        val lens = thSketch(w, h) {
            m(s * 0.06f, -0.86f); l(s * 0.44f, -0.86f); q(s * 0.47f, -0.86f, s * 0.46f, -0.74f); l(s * 0.42f, -0.34f)
            q(s * 0.38f, -0.1f, s * 0.26f, -0.1f); l(s * 0.2f, -0.1f); q(s * 0.08f, -0.1f, s * 0.07f, -0.3f); l(s * 0.05f, -0.76f)
            q(s * 0.05f, -0.86f, s * 0.06f, -0.86f); z()
        }
        drawPath(lens, Ink.line, style = Stroke(pen.lw * 2f, join = StrokeJoin.Round))
        drawPath(lens, Brush.verticalGradient(listOf(Color(0xFF2A2F5A), Color(0xFF5B45B8), Color(0xFFB068C8)), -0.86f * h, -0.1f * h))
        clipPath(lens) {
            drawLine(Color.White.copy(alpha = 0.55f), o(s * 0.12f - 0.1f, -0.3f), o(s * 0.12f + 0.1f, -0.9f), w * 0.035f)
            drawLine(Color.White.copy(alpha = 0.35f), o(s * 0.26f - 0.1f, -0.3f), o(s * 0.26f + 0.1f, -0.9f), w * 0.015f)
        }
    }
}

internal fun DrawScope.thRectangleGlasses(w: Float, h: Float, pen: Pen) {
    fun p(x: Float,y: Float)=Offset(x*w,y*h)
    val frame=Color(0xFF60423D)
    for(s in SIDES) {
        val lens=Rect(p(s*.235f-.2f,-.93f),p(s*.235f+.2f,-.07f))
        drawRoundRect(Color(0x20DAE8E6),lens.topLeft,lens.size,androidx.compose.ui.geometry.CornerRadius(h*.23f))
        drawRoundRect(Ink.line,lens.topLeft,lens.size,androidx.compose.ui.geometry.CornerRadius(h*.23f),style=Stroke(pen.lw*2.4f))
        drawRoundRect(frame,lens.topLeft,lens.size,androidx.compose.ui.geometry.CornerRadius(h*.23f),style=Stroke(pen.lw*1.2f))
        drawLine(frame,p(s*.43f,-.73f),p(s*.5f,-.77f),pen.lw*1.8f,StrokeCap.Round)
        drawLine(Color.White.copy(alpha=.6f),p(s*.235f-.11f,-.72f),p(s*.235f-.055f,-.82f),pen.lw*.7f,StrokeCap.Round)
    }
    drawPath(thSketch(w,h) { m(-.045f,-.63f);q(0f,-.79f,.045f,-.63f) },frame,style=Stroke(pen.lw*1.7f,cap=StrokeCap.Round))
}

internal fun DrawScope.thRoundGlasses(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val frame = Color(0xFFB07A3A)
    val r = h * 0.43f
    for (s in SIDES) {
        drawLine(Ink.line, o(s * 0.42f, -0.62f), o(s * 0.5f, -0.66f), pen.lw * 1.8f, StrokeCap.Round)
        drawLine(frame, o(s * 0.42f, -0.62f), o(s * 0.5f, -0.66f), pen.lw * 0.8f, StrokeCap.Round)
    }
    val bridge = thSketch(w, h) { m(-0.06f, -0.56f); q(0f, -0.7f, 0.06f, -0.56f) }
    drawPath(bridge, Ink.line, style = Stroke(pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(bridge, frame, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    for (s in SIDES) {
        val c = o(s * 0.235f, -0.5f)
        drawCircle(Color(0x44CFEFFF), r, c)
        drawArc(Color.White.copy(alpha = 0.8f), 200f, 60f, false, Offset(c.x - r * 0.7f, c.y - r * 0.7f), Size(r * 1.4f, r * 1.4f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
        drawLine(Color.White.copy(alpha = 0.5f), Offset(c.x + r * 0.1f, c.y + r * 0.5f), Offset(c.x + r * 0.5f, c.y + r * 0.1f), pen.lw * 0.7f, StrokeCap.Round)
        drawCircle(Ink.line, r, c, style = Stroke(pen.lw * 2.2f))
        drawCircle(frame, r, c, style = Stroke(pen.lw * 1f))
    }
}

internal fun DrawScope.thStarGlasses(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val pink = Color(0xFFFF6FA8)
    val bridge = thSketch(w, h) { m(-0.08f, -0.58f); q(0f, -0.7f, 0.08f, -0.58f) }
    drawPath(bridge, Ink.line, style = Stroke(pen.lw * 2.2f, cap = StrokeCap.Round))
    drawPath(bridge, pink, style = Stroke(pen.lw * 1f, cap = StrokeCap.Round))
    for (s in SIDES) {
        val c = o(s * 0.25f, -0.5f)
        val star = starPath(c, h * 0.47f, h * 0.24f, s * 8f)
        drawPath(star, Brush.verticalGradient(listOf(Color(0x66FFE066), Color(0x66FF9EC7)), c.y - h * 0.47f, c.y + h * 0.4f))
        drawPath(star, Ink.line, style = Stroke(pen.lw * 2.6f, join = StrokeJoin.Round))
        drawPath(star, pink, style = Stroke(pen.lw * 1.3f, join = StrokeJoin.Round))
        clipPath(star) { drawLine(Color.White.copy(alpha = 0.6f), Offset(c.x - h * 0.3f, c.y + h * 0.1f), Offset(c.x - h * 0.05f, c.y - h * 0.3f), pen.lw * 0.9f, StrokeCap.Round) }
        drawCircle(Color.White, pen.lw * 0.4f, Offset(c.x + s * h * 0.28f, c.y - h * 0.3f))
    }
    thGlint(o(0.44f, -0.9f), h * 0.18f, pen.t, 2.6f, 0f)
    thGlint(o(-0.46f, -0.2f), h * 0.14f, pen.t, 2.6f, 2.2f)
}

// ------------------------------------------------------------------ garments on a hanger

private val SHIRT = Color(0xFFF7F4EE)

private fun teePath(w: Float, h: Float) = thSketch(w, h) {
    m(-0.13f, -0.8f); q(0f, -0.68f, 0.13f, -0.8f); l(0.3f, -0.76f); l(0.49f, -0.56f); l(0.39f, -0.44f); l(0.31f, -0.52f)
    l(0.31f, -0.06f); q(0.31f, -0.02f, 0.27f, -0.02f); l(-0.27f, -0.02f); q(-0.31f, -0.02f, -0.31f, -0.06f)
    l(-0.31f, -0.52f); l(-0.39f, -0.44f); l(-0.49f, -0.56f); l(-0.3f, -0.76f); z()
}

private fun longPath(w: Float, h: Float) = thSketch(w, h) {
    m(-0.13f, -0.8f); q(0f, -0.68f, 0.13f, -0.8f); l(0.3f, -0.76f); q(0.42f, -0.72f, 0.45f, -0.56f); l(0.49f, -0.08f)
    l(0.37f, -0.06f); l(0.33f, -0.48f); l(0.31f, -0.06f); q(0.31f, -0.02f, 0.27f, -0.02f); l(-0.27f, -0.02f)
    q(-0.31f, -0.02f, -0.31f, -0.06f); l(-0.33f, -0.48f); l(-0.37f, -0.06f); l(-0.49f, -0.08f); l(-0.45f, -0.56f)
    q(-0.42f, -0.72f, -0.3f, -0.76f); z()
}

private fun dressPath(w: Float, h: Float) = thSketch(w, h) {
    m(-0.13f, -0.8f); q(0f, -0.7f, 0.13f, -0.8f); l(0.27f, -0.77f); l(0.38f, -0.62f); l(0.27f, -0.55f); l(0.23f, -0.44f)
    l(0.47f, -0.06f); q(0.36f, 0f, 0.24f, -0.04f); q(0.12f, 0f, 0f, -0.04f); q(-0.12f, 0f, -0.24f, -0.04f); q(-0.36f, 0f, -0.47f, -0.06f)
    l(-0.23f, -0.44f); l(-0.27f, -0.55f); l(-0.38f, -0.62f); l(-0.27f, -0.77f); z()
}

/** A top on a wooden hanger. Style and colour come from [Garment]. */
internal fun DrawScope.thGarment(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val style = Garment.style(v)
    val col = argb(Palette.cloth[Garment.color(v)])
    val hook = thSketch(w, h) { m(0f, -0.8f); l(0f, -0.88f); q(0f, -0.99f, 0.08f, -0.99f); q(0.15f, -0.99f, 0.15f, -0.92f) }
    drawPath(hook, Ink.line, style = Stroke(w * 0.035f + pen.lw * 1.6f, cap = StrokeCap.Round))
    drawPath(hook, ThingInk.silver, style = Stroke(w * 0.035f, cap = StrokeCap.Round))
    val bar = thSketch(w, h) { m(-0.44f, -0.7f); q(0f, -0.92f, 0.44f, -0.7f) }
    drawPath(bar, Ink.line, style = Stroke(h * 0.07f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(bar, ThingInk.wood, style = Stroke(h * 0.07f, cap = StrokeCap.Round))
    if (style == 1) inkedOval(Rect(-0.22f * w, -0.9f * h, 0.22f * w, -0.7f * h), col.darken(0.15f), pen)
    val shape = when (style) {
        1, 5, 6, 7, 8, 9, 12, 13, 14, 15 -> longPath(w, h)
        2 -> dressPath(w, h)
        else -> teePath(w, h)
    }
    val base = when (style) {
        4, 6 -> SHIRT
        else -> col
    }
    inked(shape, base, pen, outline = false)
    val dark = col.darken(0.28f)
    clipPath(shape) {
        when (style) {
            14, 15 -> {
                drawLine(dark,o(0f,-.76f),o(0f,if(style==15) -.05f else -.45f),pen.lw*1.6f)
                drawLine(col.lighten(.5f),o(0f,-.76f),o(0f,if(style==15) -.05f else -.45f),pen.lw*.55f)
                for(s in SIDES) inked(thSketch(w,h) { m(s*.03f,-.8f);l(s*.14f,-.85f);l(s*.23f,-.72f);l(s*.045f,-.59f);z() },col.lighten(.07f),pen,shade=false)
                inkedRound(Rect(o(-.023f,-.64f),o(.023f,-.56f)),w*.008f,ThingInk.silver,pen,shade=false)
            }
            7, 8, 12 -> {
                if (style == 12) drawRect(SHIRT, o(-.11f, -.79f), Size(.22f * w, .74f * h))
                else {
                    drawLine(dark, o(0f, -.76f), o(0f, -.04f), pen.lw * .7f)
                    for (k in 0..3) drawCircle(SHIRT, w * .018f, o(.06f, -.64f + k * .16f))
                }
                for (s in SIDES) {
                    val collar = thSketch(w, h) { m(s * .11f, -.80f); l(s * .27f, -.73f); l(s * .18f, -.58f); l(s * .04f, -.73f); z() }
                    inked(collar, col.lighten(.14f), pen, shade = false)
                }
            }
            0 -> {
                drawPath(starPath(o(0f, -0.46f), w * 0.1f, w * 0.045f), Color.White.copy(alpha = 0.85f))
                drawLine(dark, o(0.34f, -0.5f), o(0.45f, -0.6f), pen.lw * 0.5f)
                drawLine(dark, o(-0.34f, -0.5f), o(-0.45f, -0.6f), pen.lw * 0.5f)
            }
            1 -> {
                inkedRound(Rect(-0.2f * w, -0.34f * h, 0.2f * w, -0.12f * h), w * 0.05f, col.darken(0.08f), pen, shade = false)
                drawRect(dark, o(-0.5f, -0.1f), Size(w, 0.1f * h))
                drawRect(dark, o(0.34f, -0.14f), Size(0.2f * w, 0.1f * h))
                drawRect(dark, o(-0.54f, -0.14f), Size(0.2f * w, 0.1f * h))
                for (s in SIDES) {
                    drawLine(Color.White, o(s * 0.06f, -0.76f), o(s * 0.07f, -0.56f), pen.lw * 0.6f, StrokeCap.Round)
                    drawCircle(Color.White, pen.lw * 0.6f, o(s * 0.07f, -0.55f))
                }
            }
            2 -> {
                drawRect(dark, o(-0.5f, -0.48f), Size(w, 0.07f * h))
                for (k in -2..2) drawLine(dark, o(k * 0.06f, -0.4f), o(k * 0.16f, -0.06f), pen.lw * 0.45f)
                drawCircle(Color.White.copy(alpha = 0.8f), w * 0.02f, o(-0.1f, -0.64f))
                drawCircle(Color.White.copy(alpha = 0.8f), w * 0.02f, o(0.12f, -0.6f))
                drawCircle(Color.White.copy(alpha = 0.8f), w * 0.02f, o(0f, -0.54f))
            }
            3 -> for (y in STRIPE_Y) drawRect(Color.White.copy(alpha = 0.85f), o(-0.5f, y), Size(w, 0.07f * h))
            4 -> {
                drawRect(col, o(-0.5f, -0.3f), Size(w, 0.3f * h))
                inkedRound(Rect(-0.19f * w, -0.62f * h, 0.19f * w, -0.26f * h), w * 0.04f, col, pen, shade = false)
                drawRect(col, o(-0.17f, -0.32f), Size(0.34f * w, 0.1f * h))
                for (s in SIDES) {
                    drawLine(col, o(s * 0.15f, -0.6f), o(s * 0.2f, -0.8f), w * 0.07f, StrokeCap.Round)
                    thDot(o(s * 0.13f, -0.56f), w * 0.028f, ThingInk.sun, pen)
                }
                drawRoundRect(Ink.line, o(-0.09f, -0.5f), Size(0.18f * w, 0.12f * h), androidx.compose.ui.geometry.CornerRadius(w * 0.02f), style = pen.thin)
            }
            5 -> {
                // A cosy winter sweater: a pale yoke with snowflake stars, dots, and a patterned hem.
                drawOval(SHIRT, o(-0.62f, -1.1f), Size(1.24f * w, 0.56f * h))
                for (k in -2..2) drawPath(thSelbu(o(k * 0.18f, -0.66f), w * 0.07f), col)
                for (k in -3..3) drawCircle(col, w * 0.016f, o(k * 0.12f + 0.06f, -0.58f))
                var y = -0.46f
                var row = 0
                while (y > -0.2f) {
                    var x = -0.26f + (row % 2) * 0.09f
                    while (x < 0.28f) {
                        drawCircle(SHIRT, w * 0.014f, o(x, y))
                        x += 0.18f
                    }
                    y += 0.09f
                    row++
                }
                drawRect(SHIRT, o(-0.5f, -0.13f), Size(w, 0.11f * h))
                for (k in -4..4) drawPath(thSketch(w, h) { m(k * 0.1f - 0.04f, -0.07f); l(k * 0.1f, -0.11f); l(k * 0.1f + 0.04f, -0.07f) }, col, style = Stroke(pen.lw * 0.5f))
                drawRect(SHIRT, o(0.3f, -0.16f), Size(0.2f * w, 0.1f * h))
                drawRect(SHIRT, o(-0.5f, -0.16f), Size(0.2f * w, 0.1f * h))
            }
            6 -> {
                // Bunad: a dark embroidered vest over the white shirt, and a silver brooch.
                for (s in SIDES) {
                    val vest = thSketch(w, h) { m(s * 0.06f, -0.78f); l(s * 0.28f, -0.76f); l(s * 0.32f, -0.02f); l(s * 0.08f, -0.02f); q(s * 0.12f, -0.4f, s * 0.06f, -0.78f); z() }
                    drawPath(vest, col)
                    drawPath(vest, Ink.line, style = pen.thin)
                    for (k in 0 until 3) {
                        val p = o(s * (0.19f + k * 0.01f), -0.62f + k * 0.2f)
                        drawCircle(Color(0xFFFF5A4E), w * 0.035f, p)
                        drawCircle(Color(0xFFFFD23F), w * 0.014f, p)
                        drawCircle(Color(0xFF3BC46B), w * 0.018f, Offset(p.x + s * w * 0.05f, p.y + h * 0.05f))
                    }
                }
            }
        }
    }
    drawPath(shape, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    if (style == 1 || style == 5 || style == 6) {
        for (s in SIDES) drawLine(Ink.line, o(s * 0.33f, -0.48f), o(s * 0.35f, -0.2f), pen.lw * 0.5f, StrokeCap.Round)
    }
    val collar = thSketch(w, h) { m(-0.13f, -0.8f); q(0f, -0.68f, 0.13f, -0.8f) }
    drawPath(collar, if (style == 4 || style == 6) SHIRT.darken(0.2f) else dark, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    if (style == 6) {
        val broochC = o(0f, -0.7f)
        drawLine(Color(0xFFD9DDE8), broochC, o(0f, -0.58f), pen.lw * 0.7f)
        thDot(broochC, w * 0.055f, Color(0xFFE8ECF5), pen)
        drawCircle(Color(0xFFB8C0D0), w * 0.025f, broochC)
        for (k in -1..1) thDot(o(k * 0.05f, -0.56f), w * 0.018f, Color(0xFFD9DDE8), pen)
    }
}

private val STRIPE_Y = floatArrayOf(-0.66f, -0.5f, -0.34f, -0.18f)
