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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.cos
import kotlin.math.sin

private val GEM_COL = longArrayOf(0xFFFF3B5C, 0xFF3D7BFF, 0xFF2FD18B, 0xFFA66BFF, 0xFF4FE3E8)

/** A cut gem, side on: table, crown facets and a pointed pavilion. */
internal fun DrawScope.thGem(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = Color(GEM_COL[v.mod(5)])
    thGlow(o(0f, -0.5f), w * 0.55f, col, 0.22f + 0.1f * sin(pen.t * 2f))
    val gem = thSketch(w, h) { m(-0.26f, -0.92f); l(0.26f, -0.92f); l(0.48f, -0.66f); l(0f, -0.03f); l(-0.48f, -0.66f); z() }
    drawPath(gem, col)
    fun facet(color: Color, vararg p: Float) {
        val path = Path()
        path.moveTo(p[0] * w, p[1] * h)
        var k = 2
        while (k < p.size) {
            path.lineTo(p[k] * w, p[k + 1] * h)
            k += 2
        }
        path.close()
        drawPath(path, color)
    }
    facet(col.lighten(0.5f), -0.26f, -0.92f, 0.26f, -0.92f, 0.12f, -0.66f, -0.12f, -0.66f)
    facet(col.lighten(0.25f), -0.48f, -0.66f, -0.26f, -0.92f, -0.12f, -0.66f)
    facet(col.darken(0.05f), 0.26f, -0.92f, 0.48f, -0.66f, 0.12f, -0.66f)
    facet(col.lighten(0.12f), -0.48f, -0.66f, -0.12f, -0.66f, 0f, -0.03f)
    facet(col.lighten(0.35f), -0.12f, -0.66f, 0.12f, -0.66f, 0f, -0.03f)
    facet(col.darken(0.25f), 0.12f, -0.66f, 0.48f, -0.66f, 0f, -0.03f)
    val edge = Stroke(pen.lw * 0.45f, join = StrokeJoin.Round)
    val e = col.darken(0.4f)
    drawLine(e, o(-0.48f, -0.66f), o(0.48f, -0.66f), edge.width)
    drawLine(e, o(-0.12f, -0.66f), o(-0.26f, -0.92f), edge.width)
    drawLine(e, o(0.12f, -0.66f), o(0.26f, -0.92f), edge.width)
    drawLine(e, o(-0.12f, -0.66f), o(0f, -0.03f), edge.width)
    drawLine(e, o(0.12f, -0.66f), o(0f, -0.03f), edge.width)
    drawPath(gem, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    shine(o(-0.12f, -0.82f), w * 0.14f, h * 0.06f, 0.9f)
    thGlint(o(-0.3f, -0.88f), w * 0.22f, pen.t, 2.1f, v * 1.3f)
    thGlint(o(0.24f, -0.42f), w * 0.16f, pen.t, 2.1f, v * 1.3f + 2.6f)
}

private val GIFT_PAPER = longArrayOf(0xFFFF5A4E, 0xFF4A8BFF, 0xFF3BC46B, 0xFF8B5CF6, 0xFFFFD23F)
private val GIFT_RIBBON = longArrayOf(0xFFFFD23F, 0xFFFF5A4E, 0xFFFF6FA8, 0xFFFFD23F, 0xFF3D6BFF)

/** A wrapped present in oblique depth: box, lid, ribbon round both ways and a bow. */
internal fun DrawScope.thGift(v: Int, w: Float, h: Float, pen: Pen) {
    val i = v.mod(5)
    val paper = Color(GIFT_PAPER[i])
    val ribbon = Color(GIFT_RIBBON[i])
    val depth = w * 0.34f
    val dx = obliqueX(depth)
    val dy = obliqueY(depth)
    val body = Rect(-0.45f * w, -0.58f * h, 0.45f * w - dx, -0.02f * h)
    val lid = Rect(body.left - w * 0.03f, -0.72f * h, body.right + w * 0.03f, -0.56f * h)
    box3d(body, depth, paper, pen)
    giftPattern(i, body, w, h, pen)
    drawRect(Ink.line, body.topLeft, body.size, style = pen.stroke)
    val rw = w * 0.13f
    // Ribbon down the front, and round the right side.
    drawRect(ribbon, Offset(body.center.x - rw / 2, body.top), Size(rw, body.height))
    val sideBand = Path().apply {
        moveTo(body.right + dx * 0.42f, body.top + dy * 0.42f); lineTo(body.right + dx * 0.58f, body.top + dy * 0.58f)
        lineTo(body.right + dx * 0.58f, body.bottom + dy * 0.58f); lineTo(body.right + dx * 0.42f, body.bottom + dy * 0.42f); close()
    }
    drawPath(sideBand, ribbon.darken(0.2f))
    drawLine(Ink.line, Offset(body.center.x - rw / 2, body.top), Offset(body.center.x - rw / 2, body.bottom), pen.lw * 0.5f)
    drawLine(Ink.line, Offset(body.center.x + rw / 2, body.top), Offset(body.center.x + rw / 2, body.bottom), pen.lw * 0.5f)
    box3d(lid, depth, paper.lighten(0.06f), pen)
    giftPattern(i, lid, w, h, pen)
    drawRect(ribbon, Offset(lid.center.x - rw / 2, lid.top), Size(rw, lid.height))
    drawRect(Ink.line, lid.topLeft, lid.size, style = pen.stroke)
    val lidTop = Path().apply {
        moveTo(lid.center.x - rw / 2, lid.top); lineTo(lid.center.x - rw / 2 + dx, lid.top + dy)
        lineTo(lid.center.x + rw / 2 + dx, lid.top + dy); lineTo(lid.center.x + rw / 2, lid.top); close()
    }
    drawPath(lidTop, ribbon.lighten(0.15f))
    val across = Path().apply {
        moveTo(lid.left + dx * 0.42f, lid.top + dy * 0.42f); lineTo(lid.right + dx * 0.42f, lid.top + dy * 0.42f)
        lineTo(lid.right + dx * 0.58f, lid.top + dy * 0.58f); lineTo(lid.left + dx * 0.58f, lid.top + dy * 0.58f); close()
    }
    drawPath(across, ribbon.lighten(0.15f))
    val lidSide = Path().apply {
        moveTo(lid.right + dx * 0.42f, lid.top + dy * 0.42f); lineTo(lid.right + dx * 0.58f, lid.top + dy * 0.58f)
        lineTo(lid.right + dx * 0.58f, lid.bottom + dy * 0.58f); lineTo(lid.right + dx * 0.42f, lid.bottom + dy * 0.42f); close()
    }
    drawPath(lidSide, ribbon.darken(0.2f))
    // The bow sits in the middle of the lid.
    val bc = Offset(lid.center.x + dx * 0.5f, lid.top + dy * 0.5f)
    for (s in SIDES) {
        val tail = Path().apply {
            moveTo(bc.x, bc.y); lineTo(bc.x + s * w * 0.14f, bc.y + h * 0.12f); lineTo(bc.x + s * w * 0.09f, bc.y + h * 0.13f)
            lineTo(bc.x + s * w * 0.03f, bc.y + h * 0.1f); close()
        }
        inked(tail, ribbon.darken(0.1f), pen, shade = false)
    }
    for (s in SIDES) {
        val loop = Path().apply {
            moveTo(bc.x, bc.y)
            cubicTo(bc.x + s * w * 0.06f, bc.y - h * 0.26f, bc.x + s * w * 0.3f, bc.y - h * 0.2f, bc.x + s * w * 0.24f, bc.y - h * 0.07f)
            cubicTo(bc.x + s * w * 0.2f, bc.y, bc.x + s * w * 0.08f, bc.y + h * 0.02f, bc.x, bc.y)
            close()
        }
        inked(loop, ribbon, pen)
        drawLine(ribbon.darken(0.3f), Offset(bc.x + s * w * 0.03f, bc.y - h * 0.03f), Offset(bc.x + s * w * 0.16f, bc.y - h * 0.12f), pen.lw * 0.5f, StrokeCap.Round)
    }
    inkedCircle(bc, w * 0.05f, ribbon.darken(0.05f), pen)
    shine(Offset(bc.x - w * 0.14f, bc.y - h * 0.12f), w * 0.05f, h * 0.03f)
}

private fun DrawScope.giftPattern(i: Int, face: Rect, w: Float, h: Float, pen: Pen) {
    clipRect(face.left, face.top, face.right, face.bottom) {
        val mark = when (i) {
            0 -> Color.White
            1 -> Color.White
            2 -> Color.White.copy(alpha = 0.8f)
            3 -> Color(0xFFFFD23F)
            else -> Color(0xFFFF5A4E)
        }
        var row = 0
        var y = face.top + h * 0.06f
        while (y < face.bottom + h * 0.1f) {
            var x = face.left + w * (if (row % 2 == 0) 0.06f else 0.16f)
            while (x < face.right + w * 0.1f) {
                when (i) {
                    0, 3 -> drawCircle(mark, w * 0.03f, Offset(x, y))
                    1 -> drawPath(starPath(Offset(x, y), w * 0.045f, w * 0.02f), mark)
                    4 -> drawPath(thHeart(x, y, w * 0.07f), mark)
                    else -> Unit
                }
                x += w * 0.2f
            }
            y += h * 0.15f
            row++
        }
        if (i == 2) {
            var sx = face.left - face.height
            while (sx < face.right) {
                drawLine(mark, Offset(sx, face.bottom), Offset(sx + face.height, face.top), w * 0.04f)
                sx += w * 0.14f
            }
        }
    }
}

/** A gold krone with a hole in the middle. */
internal fun DrawScope.thCoin(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h * 0.5f)
    val r = w * 0.48f
    val gold = Color(0xFFFFC83D)
    val coin = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r))
        addOval(Rect(c.x - r * 0.26f, c.y - r * 0.26f, c.x + r * 0.26f, c.y + r * 0.26f))
    }
    inked(coin, gold, pen)
    val deep = gold.darken(0.35f)
    drawCircle(deep, r * 0.8f, c, style = Stroke(pen.lw * 0.5f))
    drawCircle(deep, r * 0.36f, c, style = Stroke(pen.lw * 0.5f))
    val crown = Path().apply {
        val y = c.y - r * 0.5f
        val s = r * 0.22f
        moveTo(c.x - s, y + s * 0.4f); lineTo(c.x - s, y - s * 0.3f); lineTo(c.x - s * 0.5f, y + s * 0.05f); lineTo(c.x, y - s * 0.45f)
        lineTo(c.x + s * 0.5f, y + s * 0.05f); lineTo(c.x + s, y - s * 0.3f); lineTo(c.x + s, y + s * 0.4f); close()
    }
    drawPath(crown, deep)
    for (k in 0 until 5) {
        val a = Math.toRadians(40.0 + k * 25.0)
        drawCircle(deep, r * 0.045f, Offset(c.x + (r * 0.58f * cos(a)).toFloat(), c.y + (r * 0.58f * sin(a)).toFloat()))
    }
    drawArc(Color.White.copy(alpha = 0.7f), 200f, 60f, false, Offset(c.x - r * 0.68f, c.y - r * 0.68f), Size(r * 1.36f, r * 1.36f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    thGlint(Offset(c.x + r * 0.45f, c.y - r * 0.45f), r * 0.5f, pen.t, 1.6f, 0f)
}

/** A green rubber boot. */
internal fun DrawScope.thBoot(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val green = Color(0xFF2E9B5E)
    val boot = thSketch(w, h) {
        m(-0.1f, -0.9f); l(0.34f, -0.9f); l(0.36f, -0.1f); q(0.36f, -0.02f, 0.28f, -0.02f); l(-0.4f, -0.02f)
        q(-0.49f, -0.02f, -0.48f, -0.12f); q(-0.48f, -0.34f, -0.24f, -0.38f); q(-0.12f, -0.4f, -0.1f, -0.5f); z()
    }
    inked(boot, green, pen, outline = false)
    clipPath(boot) {
        drawRect(Color(0xFF3A3340), o(-0.5f, -0.12f), Size(w, 0.12f * h))
        for (k in 0 until 6) drawLine(Color(0xFF5A5566), o(-0.4f + k * 0.14f, -0.12f), o(-0.44f + k * 0.14f, -0.02f), pen.lw * 0.5f)
        drawOval(green.lighten(0.2f), o(-0.5f, -0.4f), Size(0.36f * w, 0.26f * h))
        drawLine(Color.White.copy(alpha = 0.45f), o(0f, -0.82f), o(0.02f, -0.3f), w * 0.07f, StrokeCap.Round)
    }
    drawPath(boot, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    inkedRound(Rect(-0.13f * w, -0.99f * h, 0.37f * w, -0.86f * h), h * 0.05f, green.lighten(0.12f), pen)
    inkedRound(Rect(0.3f * w, -0.99f * h, 0.42f * w, -0.8f * h), w * 0.04f, Color(0xFFFFC83D), pen, shade = false)
}

private val SLIME_JUICE = intArrayOf(0, 2, 6, 7, 1)

/** A wobbly blob of slime with a little face. */
internal fun DrawScope.thSlime(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = argb(app.trollfoss.domain.Palette.juice[SLIME_JUICE[v.mod(5)]])
    val t = pen.t
    val a = 0.04f * sin(t * 3.1f)
    val b = 0.04f * sin(t * 3.1f + 1.6f)
    val slime = blobPath(
        -0.49f * w, -0.08f * h, (-0.46f - a) * w, (-0.52f + b) * h, -0.24f * w, (-0.88f - a) * h, 0.04f * w, (-0.98f + a) * h,
        0.3f * w, (-0.86f - b) * h, (0.47f + a) * w, (-0.48f + a) * h, 0.49f * w, -0.08f * h, 0.26f * w, 0f,
        0.12f * w, 0.02f * h, -0.02f * w, -0.04f * h, -0.18f * w, 0.02f * h, -0.32f * w, 0f,
    )
    inked(slime, col, pen, outline = false)
    clipPath(slime) {
        drawOval(col.lighten(0.3f).copy(alpha = 0.7f), o(-0.3f, -0.8f), Size(0.5f * w, 0.4f * h))
        drawCircle(Color.White.copy(alpha = 0.35f), w * 0.05f, o(0.24f, -0.3f))
        drawCircle(Color.White.copy(alpha = 0.3f), w * 0.03f, o(-0.3f, -0.26f))
        drawCircle(Color.White.copy(alpha = 0.3f), w * 0.025f, o(0.34f, -0.56f))
    }
    drawPath(slime, Ink.line, style = pen.stroke)
    for (s in SIDES) {
        val e = o(s * 0.12f, -0.52f + b * 0.3f)
        drawOval(Ink.line, Offset(e.x - w * 0.035f, e.y - h * 0.08f), Size(w * 0.07f, h * 0.16f))
        drawCircle(Color.White, w * 0.015f, Offset(e.x - w * 0.01f, e.y - h * 0.04f))
    }
    drawPath(thSketch(w, h) { m(-0.06f, -0.34f); q(0f, -0.26f, 0.06f, -0.34f) }, Ink.line, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
    shine(o(-0.24f, -0.74f), w * 0.14f, h * 0.12f, 0.9f)
    thGlint(o(0.3f, -0.7f), w * 0.08f, t, 2.4f, v.toFloat())
}

/** A jar with a glowing star inside. */
internal fun DrawScope.thStarJar(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val jar = thSketch(w, h) {
        m(-0.3f, -0.76f); l(0.3f, -0.76f); q(0.46f, -0.74f, 0.46f, -0.6f); l(0.46f, -0.12f); q(0.46f, -0.02f, 0.36f, -0.02f)
        l(-0.36f, -0.02f); q(-0.46f, -0.02f, -0.46f, -0.12f); l(-0.46f, -0.6f); q(-0.46f, -0.74f, -0.3f, -0.76f); z()
    }
    val pulse = 0.5f + 0.5f * sin(pen.t * 2.2f)
    thGlow(o(0f, -0.4f), w * 0.62f, Color(0xFFFFE066), 0.3f + 0.2f * pulse)
    drawPath(jar, ThingInk.glass)
    clipPath(jar) {
        val sc = o(0f, -0.4f + 0.03f * sin(pen.t * 1.5f))
        thGlow(sc, w * 0.46f, Color(0xFFFFF3A0), 0.7f + 0.3f * pulse)
        val star = starPath(sc, w * 0.32f, w * 0.15f, sin(pen.t) * 10f)
        drawPath(star, Color(0xFFFFE066))
        drawPath(star, Color(0xFFE0A21E), style = Stroke(pen.lw * 0.7f, join = StrokeJoin.Round))
        drawCircle(Color(0xFFFFFBE0), w * 0.09f, sc)
        for (k in 0 until 3) {
            val a = pen.t * 1.2f + k * 2.1f
            thGlint(Offset(sc.x + cos(a) * w * 0.3f, sc.y + sin(a) * h * 0.22f), w * 0.1f, pen.t, 3f, k * 1.9f, Color(0xFFFFF7C2))
        }
    }
    drawPath(jar, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawLine(Color.White.copy(alpha = 0.7f), o(-0.34f, -0.62f), o(-0.34f, -0.18f), w * 0.07f, StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.5f), o(0.3f, -0.56f), o(0.3f, -0.46f), w * 0.04f, StrokeCap.Round)
    val lid = Rect(-0.36f * w, -0.95f * h, 0.36f * w, -0.76f * h)
    inkedRound(lid, w * 0.06f, Color(0xFF3D8BFF), pen)
    for (k in 1..5) drawLine(Color(0xFF3D8BFF).darken(0.3f), Offset(lid.left + lid.width * k / 6f, lid.top + h * 0.03f), Offset(lid.left + lid.width * k / 6f, lid.bottom - h * 0.03f), pen.lw * 0.45f)
    shine(o(-0.2f, -0.9f), w * 0.1f, h * 0.03f)
}

/** The speckled violet dragon egg; it shimmers, and shakes and cracks when warmed on the fire. */
internal fun DrawScope.thDragonEgg(cook: Float, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val heat = (cook / 3f).coerceIn(0f, 1f)
    val shake = if (cook > 0f) sin(pen.t * 38f) * (2f + 6f * heat) else 0f
    val violet = Color(0xFF9B6BFF)
    thGlow(o(0f, -0.48f), w * 0.62f, if (heat > 0.3f) Color(0xFFFFB36B) else Color(0xFFC9A8FF), 0.25f + 0.1f * sin(pen.t * 1.8f) + heat * 0.3f)
    rotate(shake, pivot = Offset.Zero) {
        val egg = eggPath(w, h)
        inked(egg, violet, pen, outline = false)
        clipPath(egg) {
            val dark = Color(0xFF6A3FC8)
            val light = Color(0xFFD0B3FF)
            for (k in 0 until 8) {
                val p = o(EGG_SPOTS[k * 3], EGG_SPOTS[k * 3 + 1])
                drawCircle(if (k % 3 == 0) light else dark, w * EGG_SPOTS[k * 3 + 2], p)
            }
            for (row in 0 until 3) for (k in 0 until 3) {
                val x = -0.2f + k * 0.2f + (row % 2) * 0.1f
                val y = -0.66f + row * 0.13f
                drawArc(light.copy(alpha = 0.7f), 0f, 180f, false, o(x - 0.07f, y - 0.05f), Size(0.14f * w, 0.1f * h), style = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round))
            }
            val sweep = ((pen.t * 0.35f) % 1.6f - 0.3f)
            rotate(-25f, pivot = o(0f, -0.5f)) {
                drawRect(Color.White.copy(alpha = 0.28f), o(-0.6f + sweep, -1.2f), Size(0.16f * w, 1.6f * h))
            }
        }
        drawPath(egg, Ink.line, style = pen.stroke)
        if (cook > 1.2f) {
            val grow = ((cook - 1.2f) / 1.8f).coerceIn(0f, 1f)
            val crack = thSketch(w, h) { m(-0.3f, -0.62f); l(-0.18f, -0.7f); l(-0.08f, -0.6f); l(0.04f, -0.72f); l(0.16f, -0.62f); l(0.3f, -0.7f) }
            clipRect(-w, -h * 2f, -0.3f * w + 0.6f * w * grow, 0f) {
                drawPath(crack, Ink.line, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        shine(o(-0.2f, -0.74f), w * 0.14f, h * 0.12f)
        thGlint(o(0.22f, -0.84f), w * 0.2f, pen.t, 2f, 0.5f)
    }
}

private val EGG_SPOTS = floatArrayOf(
    -0.2f, -0.5f, 0.05f, 0.14f, -0.36f, 0.04f, 0.26f, -0.62f, 0.035f, -0.04f, -0.24f, 0.06f,
    0.2f, -0.16f, 0.045f, -0.3f, -0.26f, 0.035f, 0.02f, -0.84f, 0.03f, 0.3f, -0.4f, 0.03f,
)

// ------------------------------------------------------------------ planets

/**
 * A point on the planet's surface at longitude [lon] (radians, turning with [spin]) and latitude
 * [lat]; calls [draw] with the screen spot and the sideways squash when it faces us.
 */
private inline fun onSphere(c: Offset, r: Float, lon: Float, lat: Float, spin: Float, draw: (x: Float, y: Float, k: Float) -> Unit) {
    val a = lon + spin
    val cl = cos(lat)
    val ca = cos(a)
    if (ca * cl <= 0.05f) return
    draw(c.x + r * sin(a) * cl, c.y - r * sin(lat), ca)
}

private fun DrawScope.spot(x: Float, y: Float, k: Float, rw: Float, rh: Float, color: Color) {
    drawOval(color, Offset(x - rw * k, y - rh), Size(rw * 2f * k, rh * 2f))
}

private val PLANET_BASE = longArrayOf(
    0xFF9C9A96, 0xFFE8D39A, 0xFF3D7BD9, 0xFFC4C5C8, 0xFFC8553D,
    0xFFE9CFA0, 0xFFE3C27A, 0xFFA8E3E8, 0xFF3F5BE0, 0xFFD9C3A0,
)

/** 0 Mercury, 1 Venus, 2 Earth, 3 Moon, 4 Mars, 5 Jupiter, 6 Saturn, 7 Uranus, 8 Neptune, 9 Pluto. */
internal fun DrawScope.thPlanet(v: Int, w: Float, h: Float, pen: Pen) {
    val i = v.mod(10)
    val c = Offset(0f, -h * 0.5f)
    val r = when (i) {
        6 -> w * 0.27f
        7 -> w * 0.37f
        else -> w * 0.47f
    }
    val spin = pen.t * 0.35f
    val base = Color(PLANET_BASE[i])
    if (i == 6) saturnRings(c, w, pen, back = true)
    if (i == 7) uranusRing(c, r, pen, back = true)
    val sphere = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    drawCircle(base, r, c)
    clipPath(sphere) {
        when (i) {
            0 -> craters(c, r, spin, base, 9)
            1 -> clouds(c, r, spin, base)
            2 -> earth(c, r, spin)
            3 -> {
                for (k in 0 until 5) onSphere(c, r, MARIA[k * 3], MARIA[k * 3 + 1], spin * 0.6f) { x, y, kk -> spot(x, y, kk, r * MARIA[k * 3 + 2], r * MARIA[k * 3 + 2] * 0.8f, Color(0xFF9A9CA2)) }
                craters(c, r, spin * 0.6f, base, 6)
            }
            4 -> {
                for (k in 0 until 5) onSphere(c, r, MARS_DARK[k * 3], MARS_DARK[k * 3 + 1], spin) { x, y, kk -> spot(x, y, kk, r * MARS_DARK[k * 3 + 2], r * MARS_DARK[k * 3 + 2] * 0.6f, Color(0xFF8A3A2A)) }
                drawOval(Color(0xFFF7F4EE), Offset(c.x - r * 0.4f, c.y - r * 1.02f), Size(r * 0.8f, r * 0.26f))
                drawOval(Color(0xFFF7F4EE), Offset(c.x - r * 0.24f, c.y + r * 0.9f), Size(r * 0.48f, r * 0.14f))
            }
            5 -> {
                bands(c, r, JUPITER_BANDS)
                onSphere(c, r, 0.6f, -0.38f, spin) { x, y, k ->
                    spot(x, y, k, r * 0.26f, r * 0.14f, Color(0xFFE8B89A))
                    spot(x, y, k, r * 0.2f, r * 0.1f, Color(0xFFC8553D))
                }
                for (k in 0 until 3) onSphere(c, r, 2f + k * 1.8f, 0.5f, spin) { x, y, kk -> spot(x, y, kk, r * 0.08f, r * 0.04f, Color(0xFFFFF6E6)) }
            }
            6 -> bands(c, r, SATURN_BANDS)
            7 -> {
                drawCircle(Color(0xFFC6F0F2), r * 0.9f, Offset(c.x - r * 0.1f, c.y - r * 0.1f))
                drawOval(Color(0xFF93D6DC), Offset(c.x - r, c.y + r * 0.35f), Size(r * 2f, r * 0.3f))
            }
            8 -> {
                drawRect(Color(0xFF5A78F0), Offset(c.x - r, c.y - r * 0.35f), Size(r * 2f, r * 0.2f))
                drawRect(Color(0xFF3450C8), Offset(c.x - r, c.y + r * 0.45f), Size(r * 2f, r * 0.25f))
                onSphere(c, r, 0.4f, -0.3f, spin) { x, y, k ->
                    spot(x, y, k, r * 0.22f, r * 0.12f, Color(0xFF22308A))
                    spot(x + r * 0.05f * k, y + r * 0.2f, k, r * 0.18f, r * 0.035f, Color(0xFFEFF4FF))
                }
                onSphere(c, r, 2.8f, 0.25f, spin) { x, y, k -> spot(x, y, k, r * 0.24f, r * 0.03f, Color(0xFFEFF4FF)) }
            }
            else -> {
                for (k in 0 until 4) onSphere(c, r, PLUTO_DARK[k * 3], PLUTO_DARK[k * 3 + 1], spin) { x, y, kk -> spot(x, y, kk, r * PLUTO_DARK[k * 3 + 2], r * PLUTO_DARK[k * 3 + 2] * 0.55f, Color(0xFF8C6A48)) }
                onSphere(c, r, 0.2f, 0.05f, spin) { x, y, k ->
                    scale(k, 1f, pivot = Offset(x, y)) { drawPath(thHeart(x, y, r * 0.8f), Color(0xFFF6EDDD)) }
                }
                craters(c, r, spin, base, 4)
            }
        }
        val night = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(c.x - r * 2f, c.y - r * 2f, c.x + r * 2f, c.y + r * 2f))
            addOval(Rect(c.x - r * 1.34f, c.y - r * 1.3f, c.x + r * 0.84f, c.y + r * 0.88f))
        }
        drawPath(night, Ink.line.copy(alpha = 0.32f))
    }
    thSheen(c, r, 0.38f)
    drawCircle(Ink.line, r, c, style = pen.stroke)
    if (i == 6) saturnRings(c, w, pen, back = false)
    if (i == 7) uranusRing(c, r, pen, back = false)
}

private val MARIA = floatArrayOf(0.2f, 0.3f, 0.28f, 0.9f, 0.1f, 0.22f, -0.4f, -0.2f, 0.2f, 2.6f, 0.4f, 0.3f, 4.2f, -0.3f, 0.25f)
private val MARS_DARK = floatArrayOf(0.3f, 0.1f, 0.3f, 1.4f, -0.2f, 0.36f, 3.0f, 0.3f, 0.28f, 4.4f, -0.1f, 0.3f, 5.4f, 0.4f, 0.2f)
private val PLUTO_DARK = floatArrayOf(-1.2f, -0.1f, 0.42f, 2.2f, -0.2f, 0.45f, 3.4f, 0.3f, 0.3f, 4.6f, -0.3f, 0.36f)
private val CRATERS = floatArrayOf(
    0.3f, 0.4f, 0.12f, 1.2f, -0.3f, 0.16f, 2.1f, 0.2f, 0.1f, 2.9f, -0.5f, 0.14f, 3.7f, 0.5f, 0.12f,
    4.5f, 0f, 0.18f, 5.3f, -0.4f, 0.1f, 0.9f, 0.7f, 0.08f, 5.9f, 0.3f, 0.13f,
)

private fun DrawScope.craters(c: Offset, r: Float, spin: Float, base: Color, n: Int) {
    for (k in 0 until n) {
        onSphere(c, r, CRATERS[k * 3], CRATERS[k * 3 + 1], spin) { x, y, kk ->
            val s = r * CRATERS[k * 3 + 2]
            spot(x, y, kk, s, s, base.darken(0.2f))
            spot(x - s * 0.15f * kk, y - s * 0.15f, kk, s * 0.75f, s * 0.75f, base.darken(0.08f))
            drawArc(base.lighten(0.3f), 20f, 140f, false, Offset(x - s * kk, y - s), Size(s * 2f * kk, s * 2f), style = Stroke(r * 0.03f))
        }
    }
}

private fun DrawScope.clouds(c: Offset, r: Float, spin: Float, base: Color) {
    val tones = arrayOf(base.lighten(0.3f), base.darken(0.12f), base.lighten(0.2f), base.darken(0.08f))
    for (k in 0 until 4) {
        val y0 = c.y - r * 0.7f + k * r * 0.45f
        val phase = spin * 1.5f + k * 1.3f
        val p = Path()
        for (j in 0..8) {
            val x = c.x - r * 1.1f + j * r * 0.275f
            val y = y0 + sin(j * 0.9f + phase) * r * 0.08f
            if (j == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        drawPath(p, tones[k], style = Stroke(r * 0.18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// Continents as clusters of blobs: longitude, latitude, size, and whether it is desert.
private val LAND = floatArrayOf(
    0f, 0.6f, 0.3f, 0f, 0.3f, 0.2f, 0.26f, 0f, 0.45f, -0.35f, 0.24f, 0f, 0.55f, -0.7f, 0.12f, 0f,
    2.2f, 0.65f, 0.26f, 0f, 2.35f, 0.15f, 0.3f, 1f, 2.5f, -0.3f, 0.24f, 0f,
    3.3f, 0.6f, 0.34f, 0f, 3.8f, 0.4f, 0.3f, 1f, 3.9f, 0.1f, 0.18f, 0f,
    4.6f, -0.5f, 0.2f, 1f,
)
private val CLOUD_LAT = floatArrayOf(0.35f, -0.15f, 0.55f, -0.5f, 0.05f)

private fun DrawScope.earth(c: Offset, r: Float, spin: Float) {
    val green = Color(0xFF5DBB4A)
    val desert = Color(0xFFC9A66B)
    for (k in 0 until LAND.size / 4) {
        onSphere(c, r, LAND[k * 4], LAND[k * 4 + 1], spin) { x, y, kk ->
            spot(x, y, kk, r * LAND[k * 4 + 2], r * LAND[k * 4 + 2] * 0.8f, if (LAND[k * 4 + 3] > 0.5f) desert else green)
        }
    }
    for (k in 0 until 5) {
        onSphere(c, r, k * 1.3f + 0.6f, CLOUD_LAT[k], spin * 1.4f) { x, y, kk -> spot(x, y, kk, r * 0.3f, r * 0.05f, Color.White.copy(alpha = 0.85f)) }
    }
    drawOval(Color.White, Offset(c.x - r * 0.55f, c.y - r * 1.04f), Size(r * 1.1f, r * 0.26f))
    drawOval(Color.White, Offset(c.x - r * 0.45f, c.y + r * 0.84f), Size(r * 0.9f, r * 0.22f))
}

private val JUPITER_BANDS = longArrayOf(0xFFD9A871, 0xFFF1DDB8, 0xFFB07A4C, 0xFFF1DDB8, 0xFFE0A96E, 0xFFF4E4C4, 0xFFB07A4C, 0xFFF1DDB8, 0xFFD9A871)
private val SATURN_BANDS = longArrayOf(0xFFD9B46A, 0xFFF0D9A0, 0xFFE3C27A, 0xFFF3E0B0, 0xFFE3C27A, 0xFFF0D9A0, 0xFFD9B46A)

private fun DrawScope.bands(c: Offset, r: Float, colors: LongArray) {
    val n = colors.size
    val bh = r * 2f / n
    for (k in 0 until n) {
        drawRect(Color(colors[k]), Offset(c.x - r, c.y - r + k * bh), Size(r * 2f, bh + 1f))
        if (k > 0) drawLine(Color(colors[k]).darken(0.15f), Offset(c.x - r, c.y - r + k * bh), Offset(c.x + r, c.y - r + k * bh), r * 0.02f)
    }
}

/** Saturn's rings, tilted; the back half goes behind the planet and the front half over it. */
private fun DrawScope.saturnRings(c: Offset, w: Float, pen: Pen, back: Boolean) {
    rotate(-16f, pivot = c) {
        val top = if (back) c.y - w else c.y
        val bottom = if (back) c.y else c.y + w
        clipRect(c.x - w, top, c.x + w, bottom) {
            val outer = Rect(c.x - w * 0.49f, c.y - w * 0.15f, c.x + w * 0.49f, c.y + w * 0.15f)
            drawOval(Ink.line, outer.topLeft, outer.size, style = Stroke(w * 0.1f + pen.lw * 2f))
            drawOval(Color(0xFFE8D3A0), outer.topLeft, outer.size, style = Stroke(w * 0.1f))
            val gap = Rect(c.x - w * 0.43f, c.y - w * 0.13f, c.x + w * 0.43f, c.y + w * 0.13f)
            drawOval(Color(0xFF8C7A56), gap.topLeft, gap.size, style = Stroke(pen.lw * 0.6f))
            val inner = Rect(c.x - w * 0.37f, c.y - w * 0.11f, c.x + w * 0.37f, c.y + w * 0.11f)
            drawOval(Color(0xFFD9B97A).copy(alpha = 0.9f), inner.topLeft, inner.size, style = Stroke(w * 0.05f))
        }
    }
}

/** Uranus's faint ring, standing almost upright. */
private fun DrawScope.uranusRing(c: Offset, r: Float, pen: Pen, back: Boolean) {
    rotate(-8f, pivot = c) {
        val left = if (back) c.x - r * 2f else c.x
        val right = if (back) c.x else c.x + r * 2f
        clipRect(left, c.y - r * 2f, right, c.y + r * 2f) {
            val ring = Rect(c.x - r * 0.22f, c.y - r * 1.32f, c.x + r * 0.22f, c.y + r * 1.32f)
            drawOval(Ink.line.copy(alpha = 0.7f), ring.topLeft, ring.size, style = Stroke(pen.lw * 1.4f))
            drawOval(Color(0xFFDDF6F8), ring.topLeft, ring.size, style = Stroke(pen.lw * 0.6f))
        }
    }
}
