package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.PlaceId
import kotlin.math.cos
import kotlin.math.sin

/*
 * Trollhola: the troll cave behind the waterfall, as a cut-away in oblique 3D. Rough violet rock with
 * lit ledges, stalactites, hanging roots, carved spirals, glowing crystals and mushrooms, a rocky floor
 * band, and the falling water shimmering at the far right.
 */

private const val CURTAIN_X = 2.64f

/** An angular rock outline around ([cx], [cy]), clockwise on screen. */
private fun Path.addRock(cx: Float, cy: Float, rx: Float, ry: Float, salt: Int) {
    val n = 7
    for (i in 0 until n) {
        val a = i * (6.2831855f / n) + hash01(salt, 7) * 0.5f
        val k = 0.78f + 0.3f * hash01(i, salt)
        val x = cx + cos(a) * rx * k
        val y = cy + sin(a) * ry * k
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private class CaveStatic(
    val plates: Path, val ledges: Path, val holes: Path, val ceiling: Path, val ceilingEdge: Path,
    val stalactites: Path, val stalLight: Path, val stones: Path, val cracks: List<Offset>, val spirals: Path,
    val curtain: Path, val curtainEdge: Path,
)

private val caveStatic = Memo { u ->
    val back = PlaceId.LAB.back
    val plates = Path()
    val ledges = Path()
    for (i in -2 until 13) {
        val cx = i * 0.28f + hash01(i, 501) * 0.1f
        val upper = i % 2 == 0
        val cy = if (upper) 0.26f + hash01(i, 502) * 0.14f else 0.56f + hash01(i, 503) * 0.12f
        val rx = 0.12f + 0.06f * hash01(i, 504)
        val ry = 0.08f + 0.05f * hash01(i, 505)
        plates.addRock(cx * u, cy * u, rx * u, ry * u, 510 + i)
        ledges.floorQuad(u, 0f, cx - rx * 0.55f, cx + rx * 0.45f, cy - ry * 0.62f, cy - ry * 0.62f - 0.016f)
    }
    val holes = Path()
    holes.addRock(1.25f * u, 0.6f * u, 0.075f * u, 0.06f * u, 530)
    holes.addRock(0.06f * u, 0.22f * u, 0.06f * u, 0.05f * u, 531)
    holes.addRock(2.02f * u, 0.14f * u, 0.05f * u, 0.04f * u, 532)
    fun ceilY(x: Float) = 0.035f + 0.018f * sin(x * 7.3f) + 0.01f * sin(x * 17.1f)
    val ceiling = Path()
    val ceilingEdge = Path()
    ceiling.moveTo(-0.4f * u, (SKY_TOP - 0.02f) * u)
    ceiling.lineTo(3.7f * u, (SKY_TOP - 0.02f) * u)
    for (i in 41 downTo 0) {
        val x = -0.4f + i * 0.1f
        ceiling.lineTo(x * u, ceilY(x) * u)
        if (i == 41) ceilingEdge.moveTo(x * u, ceilY(x) * u) else ceilingEdge.lineTo(x * u, ceilY(x) * u)
    }
    ceiling.close()
    val stalactites = Path()
    val stalLight = Path()
    for (i in 0 until 32) {
        if (hash01(i, 541) < 0.25f) continue
        val x = -0.3f + i * 0.12f + hash01(i, 542) * 0.05f
        val len = 0.05f + 0.12f * hash01(i, 543)
        val w = 0.03f + 0.03f * hash01(i, 544)
        val top = ceilY(x) - 0.01f
        val tip = x + (hash01(i, 545) - 0.5f) * 0.01f
        stalactites.moveTo((x - w / 2f) * u, top * u)
        stalactites.lineTo((x + w / 2f) * u, top * u)
        stalactites.quadraticTo((x + w * 0.25f) * u, (top + len * 0.5f) * u, tip * u, (top + len) * u)
        stalactites.quadraticTo((x - w * 0.3f) * u, (top + len * 0.45f) * u, (x - w / 2f) * u, top * u)
        stalactites.close()
        stalLight.moveTo((x - w * 0.42f) * u, (top + 0.004f) * u)
        stalLight.lineTo((x - w * 0.08f) * u, (top + 0.004f) * u)
        stalLight.lineTo(tip * u, (top + len * 0.9f) * u)
        stalLight.close()
    }
    val stones = Path()
    for (i in 0 until 12) {
        stones.floorDisc(u, 0f, -0.1f + i * 0.25f + hash01(i, 551) * 0.12f, mix(0.83f, 0.955f, hash01(i, 552)), 0.025f + 0.03f * hash01(i, 553), 0.04f, 9)
    }
    val cracks = ArrayList<Offset>(24)
    for (i in 0 until 10) {
        val x = -0.1f + i * 0.3f + hash01(i, 561) * 0.1f
        val y0 = mix(FRONT_Y, back, hash01(i, 562) * 0.6f)
        val y1 = y0 - 0.04f
        cracks.add(Offset((x + recede(y0)) * u, y0 * u))
        cracks.add(Offset((x + recede(y1) + 0.01f) * u, y1 * u))
    }
    val spirals = Path()
    for ((cx, cy, r) in listOf(Triple(0.7f, 0.55f, 0.035f), Triple(1.0f, 0.26f, 0.04f), Triple(1.46f, 0.42f, 0.034f), Triple(2.12f, 0.44f, 0.03f))) {
        for (k in 0..34) {
            val a = k * 0.5f
            val rr = r * k / 34f
            val x = (cx + cos(a) * rr) * u
            val y = (cy + sin(a) * rr) * u
            if (k == 0) spirals.moveTo(x, y) else spirals.lineTo(x, y)
        }
    }
    val curtain = Path()
    val curtainEdge = Path()
    val ex = floatArrayOf(CURTAIN_X, 2.62f, 2.66f, 2.61f, 2.67f, 2.72f)
    val ey = floatArrayOf(FRONT_Y, 0.8f, 0.6f, 0.42f, 0.24f, 0.1f)
    curtain.moveTo(ex[5] * u, ey[5] * u)
    curtain.lineTo(3.8f * u, 0.05f * u)
    curtain.lineTo(3.8f * u, FRONT_Y * u)
    for (i in 0 until 5) curtain.lineTo(ex[i] * u, ey[i] * u)
    curtain.close()
    for (i in 0 until 6) if (i == 0) curtainEdge.moveTo(ex[i] * u, ey[i] * u) else curtainEdge.lineTo(ex[i] * u, ey[i] * u)
    CaveStatic(plates, ledges, holes, ceiling, ceilingEdge, stalactites, stalLight, stones, cracks, spirals, curtain, curtainEdge)
}

internal fun DrawScope.labBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val back = PlaceId.LAB.back
    val cs = caveStatic.of(u)
    drawRect(
        Brush.verticalGradient(0f to Color(0xFF221A3C), 0.5f to Color(0xFF392A62), 1f to Color(0xFF4A3677), startY = 0f, endY = back * u),
        Offset(0f, SKY_TOP * u),
        Size(st.w, (back - SKY_TOP) * u),
    )
    val glow = 0.6f + 0.35f * sin(t * 1.2f)
    inScene(st) {
        drawPath(cs.holes, Color(0xFF130D24))
        drawPath(cs.holes, Ink.line, style = pen.thin)
        drawPath(cs.plates, Color(0xFF4E3A7C))
        drawPath(cs.plates, Ink.line, alpha = 0.55f, style = pen.thin)
        drawPath(cs.ledges, Color(0xFF7A62B0))
        drawPath(cs.ledges, Ink.line, alpha = 0.5f, style = pen.thin)
        drawPath(cs.spirals, Color(0xFF1B1430), style = Stroke(pen.lw * 2.4f, cap = StrokeCap.Round))
        drawPath(cs.spirals, Color(0xFF6FF2FF), alpha = 0.25f * glow, style = Stroke(pen.lw * 4f, cap = StrokeCap.Round))
        drawPath(cs.spirals, Color(0xFFB9F6FF), alpha = glow, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
    }
    crystals(st, pen, 0.07f, 0.42f, 0.1f, 22f, Color(0xFF6FF2FF), 0f)
    crystals(st, pen, 0.7f, 0.33f, 0.09f, -12f, Color(0xFFFF7BD8), 1.3f)
    crystals(st, pen, 1.3f, 0.3f, 0.08f, 14f, Color(0xFFB58CFF), 2.1f)
    crystals(st, pen, 2.33f, 0.34f, 0.09f, -20f, Color(0xFF7CFFB2), 3.4f)
    hangingRoots(st, pen)
    inScene(st) {
        drawPath(cs.ceiling, Color(0xFF1C1532))
        drawPath(cs.stalactites, Color(0xFF4A3A75))
        drawPath(cs.stalLight, Color(0xFF7A67AE))
        drawPath(cs.stalactites, Ink.line, style = pen.stroke)
        drawPath(cs.ceilingEdge, Ink.line, style = pen.stroke)
    }
    drips(st, pen)

    // The rocky floor band.
    drawRect(
        Brush.verticalGradient(0f to Color(0xFF2E2350), 0.3f to Color(0xFF45376B), 1f to Color(0xFF5A4984), startY = back * u, endY = FRONT_Y * u),
        Offset(0f, back * u),
        Size(st.w, (FRONT_Y - back) * u),
    )
    drawLine(Ink.line, Offset(0f, back * u), Offset(st.w, back * u), strokeWidth = pen.lw)
    inScene(st) {
        drawPath(cs.stones, Color(0xFF6D5C98))
        drawPath(cs.stones, Ink.line, alpha = 0.7f, style = pen.thin)
        drawPoints(cs.cracks, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round, alpha = 0.5f)
    }
    wallShadow(st, back)
    if (st.sees(1.85f, 2.05f)) {
        val c = st.o(1.94f, 0.94f)
        val pool = Path().apply { floorDisc(u, st.cam, 1.94f, 0.94f, 0.07f, 0.06f, 18) }
        drawPath(pool, Brush.radialGradient(listOf(Color(0xFF9DF6FF).copy(alpha = 0.9f), Color(0xFF3A6FB0)), center = c, radius = 0.07f * u))
        drawPath(pool, Ink.line, style = pen.thin)
        drawCircle(Color.White, 0.006f * u, Offset(c.x - 0.02f * u, c.y - 0.004f * u), alpha = glow)
    }
    crystals(st, pen, 1.56f, 0.815f, 0.12f, 0f, Color(0xFF6FF2FF), 0.7f)
    crystals(st, pen, 0.08f, 0.83f, 0.1f, 10f, Color(0xFFFF7BD8), 2.6f)
    glowShrooms(st, pen, 0.64f, 0.83f, Color(0xFF5FF0D0), 0.4f)
    glowShrooms(st, pen, 1.19f, 0.84f, Color(0xFFFFA8F0), 1.9f)
    glowShrooms(st, pen, 2.33f, 0.825f, Color(0xFF5FF0D0), 3.1f)
    if (st.sees(CURTAIN_X - 0.4f, 3.8f)) waterCurtain(st, pen, cs)
    drawBase(st, pen, Color(0xFF6A56A0), Color(0xFF2C2148))
    drawBaseStones(st, pen, Color(0xFF55437F), 37)
}

/** A cluster of glowing crystals growing from ([x], [y]) at [angle] degrees from upright. */
private fun DrawScope.crystals(st: Stage, pen: Pen, x: Float, y: Float, s: Float, angle: Float, color: Color, phase: Float) {
    if (!st.sees(x - 0.2f, x + 0.2f)) return
    val u = st.u
    val base = st.o(x, y)
    val size = s * u
    val glow = 0.65f + 0.35f * sin(pen.t * 1.6f + phase)
    val a0 = Math.toRadians(angle.toDouble()).toFloat()
    val gc = Offset(base.x + sin(a0) * size * 0.5f, base.y - cos(a0) * size * 0.5f)
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.5f * glow), color.copy(alpha = 0f)), center = gc, radius = size * 1.8f), size * 1.8f, gc)
    val body = Path()
    val facet = Path()
    val specs = floatArrayOf(-0.32f, 0.55f, 0.16f, -20f, 0f, 1f, 0.2f, 0f, 0.3f, 0.72f, 0.17f, 17f, -0.12f, 0.38f, 0.13f, -40f, 0.16f, 0.42f, 0.12f, 36f)
    var tip = base
    for (k in 0 until 5) {
        val dx = specs[k * 4] * size
        val len = specs[k * 4 + 1] * size
        val wd = specs[k * 4 + 2] * size
        val ta = Math.toRadians((specs[k * 4 + 3] + angle).toDouble()).toFloat()
        val ux = sin(ta)
        val uy = -cos(ta)
        val qx = cos(ta)
        val qy = sin(ta)
        val bx = base.x + dx * cos(a0)
        val by = base.y + dx * sin(a0)
        fun pt(across: Float, along: Float) = Offset(bx + qx * across + ux * along, by + qy * across + uy * along)
        val p1 = pt(-wd / 2f, 0f)
        val p2 = pt(-wd / 2f, len * 0.72f)
        val p3 = pt(0f, len)
        val p4 = pt(wd / 2f, len * 0.72f)
        val p5 = pt(wd / 2f, 0f)
        val m = pt(0f, 0f)
        body.moveTo(p1.x, p1.y)
        body.lineTo(p2.x, p2.y)
        body.lineTo(p3.x, p3.y)
        body.lineTo(p4.x, p4.y)
        body.lineTo(p5.x, p5.y)
        body.close()
        facet.moveTo(p1.x, p1.y)
        facet.lineTo(p2.x, p2.y)
        facet.lineTo(p3.x, p3.y)
        facet.lineTo(m.x, m.y)
        facet.close()
        if (k == 1) tip = p3
    }
    drawPath(body, color.darken(0.12f))
    drawPath(facet, color.lighten(0.45f))
    drawPath(body, Ink.line, style = pen.stroke)
    twinkle(tip, size * 0.2f, Color.White, glow)
}

private fun DrawScope.glowShrooms(st: Stage, pen: Pen, x: Float, y: Float, color: Color, phase: Float) {
    if (!st.sees(x - 0.1f, x + 0.1f)) return
    val u = st.u
    val glow = 0.6f + 0.4f * sin(pen.t * 1.3f + phase)
    val c = st.o(x, y)
    val gc = Offset(c.x, c.y - 0.04f * u)
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.45f * glow), color.copy(alpha = 0f)), center = gc, radius = 0.1f * u), 0.1f * u, gc)
    val stems = Path()
    val caps = Path()
    val spots = ArrayList<Offset>(6)
    val sp = floatArrayOf(-0.026f, 0.05f, 0.024f, 0f, 0.075f, 0.034f, 0.028f, 0.042f, 0.021f)
    for (k in 0 until 3) {
        val sx = c.x + sp[k * 3] * u
        val h = sp[k * 3 + 1] * u
        val r = sp[k * 3 + 2] * u
        stems.addRoundRect(RoundRect(Rect(sx - r * 0.28f, c.y - h, sx + r * 0.28f, c.y), CornerRadius(r * 0.2f)))
        caps.moveTo(sx - r, c.y - h + r * 0.15f)
        caps.quadraticTo(sx - r, c.y - h - r * 0.9f, sx, c.y - h - r * 0.9f)
        caps.quadraticTo(sx + r, c.y - h - r * 0.9f, sx + r, c.y - h + r * 0.15f)
        caps.close()
        spots.add(Offset(sx - r * 0.35f, c.y - h - r * 0.4f))
        spots.add(Offset(sx + r * 0.32f, c.y - h - r * 0.22f))
    }
    drawPath(stems, Color(0xFFF1E8FF))
    drawPath(stems, Ink.line, style = pen.thin)
    drawPath(caps, color.copy(alpha = 1f).lighten(0.1f * glow))
    drawPath(caps, Ink.line, style = pen.stroke)
    drawPoints(spots, PointMode.Points, Color.White, strokeWidth = 0.008f * u, cap = StrokeCap.Round, alpha = 0.85f)
}

private fun DrawScope.hangingRoots(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val roots = Path()
    for ((i, x) in floatArrayOf(0.32f, 0.95f, 1.55f, 2.22f).withIndex()) {
        if (!st.sees(x - 0.1f, x + 0.1f)) continue
        val len = 0.14f + 0.08f * hash01(i, 571)
        val sway = sin(t * 0.8f + i * 1.7f) * 0.012f
        roots.moveTo(st.x(x), 0.03f * u)
        roots.cubicTo(st.x(x - 0.02f), len * 0.35f * u, st.x(x + 0.025f + sway * 0.5f), len * 0.65f * u, st.x(x + sway), len * u)
        roots.moveTo(st.x(x + 0.004f), len * 0.4f * u)
        roots.quadraticTo(st.x(x + 0.04f), len * 0.5f * u, st.x(x + 0.035f + sway), len * 0.75f * u)
        roots.moveTo(st.x(x - 0.006f), len * 0.25f * u)
        roots.quadraticTo(st.x(x - 0.04f), len * 0.32f * u, st.x(x - 0.045f + sway), len * 0.5f * u)
    }
    drawPath(roots, Ink.line, style = Stroke(0.012f * u, cap = StrokeCap.Round))
    drawPath(roots, Color(0xFF8A5E43), style = Stroke(0.0065f * u, cap = StrokeCap.Round))
}

/** Now and then a drop falls from a stalactite. */
private fun DrawScope.drips(st: Stage, pen: Pen) {
    val u = st.u
    for ((i, x) in floatArrayOf(0.45f, 1.83f).withIndex()) {
        if (!st.sees(x, x)) continue
        val ph = wrap(pen.t * 0.45f + i * 0.5f, 1f)
        val y = 0.12f + ph * ph * 0.72f
        drawCircle(Color(0xFFBFF3FF), 0.006f * u, st.o(x, y), alpha = 0.9f * (1f - ph * 0.3f))
    }
}

/** The back of the waterfall seen from inside the cave: falling water with light shimmering through. */
private fun DrawScope.waterCurtain(st: Stage, pen: Pen, cs: CaveStatic) {
    val u = st.u
    val t = pen.t
    val pulse = 0.5f + 0.5f * sin(t * 0.9f)
    for (k in 0 until 2) {
        val y0 = 0.25f + k * 0.28f
        val shaft = Path().apply {
            moveTo(st.x(CURTAIN_X + 0.02f), y0 * u)
            lineTo(st.x(CURTAIN_X + 0.02f), (y0 + 0.14f) * u)
            lineTo(st.x(CURTAIN_X - 0.55f), (y0 + 0.42f) * u)
            lineTo(st.x(CURTAIN_X - 0.45f), (y0 + 0.2f) * u)
            close()
        }
        drawPath(shaft, Color(0xFFBFF3FF), alpha = 0.07f + 0.05f * pulse * (1f - k * 0.3f))
    }
    inScene(st) {
        drawPath(cs.curtain, Brush.horizontalGradient(listOf(Color(0xFF9FDDF2), Color(0xFFE6FAFF), Color(0xFFBFEAF8)), startX = CURTAIN_X * u, endX = 3.0f * u))
        clipPath(cs.curtain) {
            val white = ArrayList<Offset>(40)
            val blue = ArrayList<Offset>(40)
            for (i in 0 until 20) {
                val lane = (CURTAIN_X + 0.012f * i + hash01(i, 581) * 0.01f) * u
                val speed = 0.5f + 0.3f * hash01(i, 582)
                for (k in 0 until 2) {
                    val y0 = wrap(t * speed + hash01(i * 2 + k, 583) * 1.2f, 1.2f) - 0.2f
                    val len = 0.1f + 0.1f * hash01(i * 2 + k, 584)
                    val list = if ((i + k) % 2 == 0) white else blue
                    list.add(Offset(lane, y0 * u))
                    list.add(Offset(lane, (y0 + len) * u))
                }
            }
            drawPoints(white, PointMode.Lines, Color.White, strokeWidth = 0.005f * u, cap = StrokeCap.Round, alpha = 0.9f)
            drawPoints(blue, PointMode.Lines, Color(0xFF6FB8DA), strokeWidth = 0.007f * u, cap = StrokeCap.Round, alpha = 0.6f)
            for (k in 0 until 3) {
                val y = wrap(0.2f + k * 0.33f + t * 0.05f, 1f) * u
                drawRect(Color.White, Offset(CURTAIN_X * u, y), Size(1.2f * u, 0.03f * u), alpha = 0.18f * pulse)
            }
        }
        drawPath(cs.curtainEdge, Color(0xFF2B2150), style = Stroke(0.03f * u, cap = StrokeCap.Round))
        drawPath(cs.curtainEdge, Ink.line, style = pen.stroke)
        for (k in 0 until 3) {
            val ph = wrap(t * 0.2f + k / 3f, 1f)
            drawCircle(Color.White, (0.04f + ph * 0.05f) * u, Offset((CURTAIN_X + 0.05f + ph * 0.04f) * u, (0.92f - ph * 0.12f) * u), alpha = 0.3f * (1f - ph))
        }
    }
}
