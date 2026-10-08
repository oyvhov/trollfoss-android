package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Places
import app.trollfoss.domain.Weather
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * The outdoor places in oblique 3D: a walkable ground band from the place's back edge (0.78) to the
 * front edge (0.97), the cut front of the diorama below it, and scenery rising behind with parallax.
 * Far layers are flat and pale with no outlines; the ground and near things have ink outlines.
 */

/** Small grass tufts scattered over a ground band, in scene pixels. */
private fun scatterTufts(u: Float, x0: Float, x1: Float, y0: Float, y1: Float, count: Int, salt: Int, h: Float): Path {
    val p = Path()
    for (i in 0 until count) {
        val x = mix(x0, x1, hash01(i, salt))
        val y = mix(y0, y1, hash01(i, salt + 1))
        p.addPath(tuftPath(x * u, y * u, h * u * (0.7f + 0.5f * hash01(i, salt + 2)), 0f))
    }
    return p
}

// ============================================================================================ BEACH

private const val BEACH_HZ = 0.5f

private fun sandBackY(x: Float) = 0.781f + 0.003f * sin(x * 6.1f)

private class Beach(val sand: Path, val edge: Path, val shelf: Path, val ripples: Path, val steps: Path, val towel: Path, val towelStripes: Path)

/** The beach: a band of sand with the sea behind it and to the right, in scene pixels. */
private val beachMemo = Memo { u ->
    val sand = Path()
    val edge = Path()
    for (i in 0..28) {
        val x = -0.4f + i * 0.1f
        if (i == 0) {
            sand.moveTo(x * u, sandBackY(x) * u)
            edge.moveTo(x * u, sandBackY(x) * u)
        } else {
            sand.lineTo(x * u, sandBackY(x) * u)
            edge.lineTo(x * u, sandBackY(x) * u)
        }
    }
    for (p in listOf(sand, edge)) p.quadraticTo(2.36f * u, 0.87f * u, 2.3f * u, FRONT_Y * u)
    sand.lineTo(-0.4f * u, FRONT_Y * u)
    sand.close()
    val shelf = Path().apply {
        moveTo(2.38f * u, 0.781f * u)
        lineTo(2.56f * u, 0.786f * u)
        quadraticTo(2.6f * u, 0.9f * u, 2.74f * u, FRONT_Y * u)
        lineTo(2.3f * u, FRONT_Y * u)
        quadraticTo(2.36f * u, 0.87f * u, 2.38f * u, 0.781f * u)
        close()
    }
    val ripples = Path()
    for (i in 0 until 44) {
        val x = hash01(i, 301) * 2.2f
        val y = 0.8f + hash01(i, 302) * 0.155f
        ripples.moveTo((x - 0.03f) * u, y * u)
        ripples.quadraticTo((x - 0.015f) * u, (y - 0.005f) * u, x * u, y * u)
        ripples.quadraticTo((x + 0.015f) * u, (y + 0.005f) * u, (x + 0.03f) * u, y * u)
    }
    val steps = Path()
    for (k in 0 until 9) {
        val f = k / 8f
        val side = if (k % 2 == 0) -0.013f else 0.013f
        steps.floorDisc(u, 0f, mix(0.62f, 1.02f, f) + side, mix(0.962f, 0.8f, f), 0.009f, 0.02f, 12)
    }
    val towel = Path().apply { floorQuad(u, 0f, 1.24f, 1.56f, 0.952f, 0.892f) }
    val towelStripes = Path()
    for (k in 0 until 3) towelStripes.floorQuad(u, 0f, 1.28f + k * 0.1f, 1.32f + k * 0.1f, 0.952f, 0.892f)
    Beach(sand, edge, shelf, ripples, steps, towel, towelStripes)
}

internal fun DrawScope.beachBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val hz = BEACH_HZ
    val back = PlaceId.BEACH.back
    drawSky(st, pen, Mood.SUMMER, hz)
    drawStars(st, pen, 0.42f)
    drawAurora(st, pen, 0.7f, 0.02f, 0.22f)
    val sunX = st.fx(0.16f, 0.04f)
    drawSun(Offset(sunX, 0.14f * u), 0.05f * u, pen, (1f - n) * (1f - overcast(pen)))
    val moonX = st.fx(0.78f, 0.04f)
    val moonVis = ramp((n - 0.3f) / 0.5f)
    drawMoon(Offset(moonX, 0.12f * u), 0.038f * u, pen, moonVis)
    drawRainbow(Offset(st.fx(0.55f, 0.08f), (hz + 0.02f) * u), 0.42f * u, 0.015f * u, pen.rainbow)
    drawClouds(st, pen, 0.05f, 0.24f, 3, 0.06f)
    drawPeaks(
        st, 0.08f, hz, 0.5f, 0.1f, 0.2f,
        Color(0xFFAFC7E6).atNight(n, 0.75f), Color(0xFF96B1D8).atNight(n, 0.75f),
        Color(0xFFF1F6FF).atNight(n, 0.7f), Color(0xFFD2DEF2).atNight(n, 0.7f), 0.3f, 3,
    )
    drawPeaks(st, 0.14f, hz + 0.004f, 0.38f, 0.04f, 0.09f, Color(0xFF86ACD8).atNight(n, 0.7f), Color(0xFF7299C8).atNight(n, 0.7f), null, null, 0f, 7)
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFFA2D8F4).atNight(n, 0.75f),
            0.35f to Color(0xFF5FACE6).atNight(n, 0.68f),
            0.6f to Color(0xFF3A8BD6).atNight(n, 0.62f),
            1f to Color(0xFF2B66AE).atNight(n, 0.6f),
            startY = hz * u,
            endY = u,
        ),
        Offset(0f, hz * u),
        Size(st.w, st.h - hz * u + 2f),
    )
    beachFarShore(st, pen)
    drawWaterPlane(st, pen, -9f, 99f, hz + 0.01f, back - 0.006f, Color.White, 9, 0.15f, 0.95f, 0.5f * (1f - 0.5f * n))
    beachGlints(st, pen, sunX, moonX, moonVis)
    drawGulls(st, pen, 3, 0.1f, 0.32f)

    val b = beachMemo.of(u)
    inScene(st) {
        drawPath(b.shelf, Brush.horizontalGradient(listOf(pen.sandy(Color(0xFFEBD49E)).atNight(n, 0.5f), Color(0xFF8FD6D0).atNight(n, 0.5f), Color(0xFF4FA3DD).atNight(n, 0.5f)), startX = 2.3f * u, endX = 2.74f * u))
        drawPath(
            b.sand,
            Brush.verticalGradient(
                0f to pen.sandy(Color(0xFFE6C48A)).atNight(n, 0.45f),
                0.08f to pen.sandy(Color(0xFFF7E4B8)).atNight(n, 0.45f),
                1f to pen.sandy(Color(0xFFF0CD8E)).atNight(n, 0.45f),
                startY = back * u,
                endY = FRONT_Y * u,
            ),
        )
        drawPath(b.ripples, pen.sandy(Color(0xFFD9B27A)).atNight(n, 0.45f), alpha = 0.6f, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
        drawPath(b.steps, pen.sandy(Color(0xFFD2A86E)).atNight(n, 0.45f), alpha = 0.55f)
        translate(0.006f * u, 0.004f * u) { drawPath(b.towel, Ink.shadow) }
        drawPath(b.towel, Color(0xFF4FB3E8).atNight(n, 0.45f))
        drawPath(b.towelStripes, Color(0xFFFFF4D6).atNight(n, 0.45f))
        drawPath(b.towel, Ink.line, style = pen.thin)
        drawPath(b.edge, Ink.line, style = pen.stroke)
        drawPath(
            b.edge, Color.White, alpha = 0.85f * (1f - 0.4f * n),
            style = Stroke(
                width = pen.lw * 1.8f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.05f * u, 0.03f * u), wrap(t * 0.02f * u, 0.08f * u)),
            ),
        )
    }
    beachDetails(st, pen)
    drawBase(st, pen, pen.sandy(Color(0xFFF3D9A6)).atNight(n, 0.45f), Color(0xFFCE9F66).atNight(n, 0.45f))
    drawLine(Color(0xFFE2B77E).atNight(n, 0.45f), Offset(0f, 0.988f * u), Offset(st.w, 0.988f * u), strokeWidth = 0.004f * u)
    drawBaseStones(st, pen, Color(0xFFB9B2C2).atNight(n, 0.45f), 17)
}

/** The other side of the fjord: a green headland with holiday houses, boathouses, a sailboat and a lighthouse. */
private fun DrawScope.beachFarShore(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val t = pen.t
    val hz = BEACH_HZ
    val p = 0.2f
    val span = st.span(p)
    fun lx(f: Float) = st.px(f * span, p)
    val land = Path().apply {
        moveTo(lx(-0.1f), (hz - 0.022f) * u)
        quadraticTo(lx(0.1f), (hz - 0.05f) * u, lx(0.22f), (hz - 0.03f) * u)
        quadraticTo(lx(0.34f), (hz - 0.014f) * u, lx(0.46f), (hz + 0.008f) * u)
        lineTo(lx(-0.1f), (hz + 0.008f) * u)
        close()
    }
    drawPath(land, pen.farGround(Color(0xFF86B585)).atNight(n, 0.7f))
    val s = 0.042f * u
    val roof = Color(0xFF5B5566)
    val walls = listOf(Color(0xFFF7F4EE), Color(0xFFFFE08A), Color(0xFFA9D3F0))
    val bases = floatArrayOf(0.033f, 0.035f, 0.028f)
    for ((k, f) in floatArrayOf(0.05f, 0.12f, 0.2f).withIndex()) {
        drawHouse3d(lx(f), (hz - bases[k]) * u, s, 0.5f * s, 0.36f * s, 0.9f * s, walls[k].atNight(n, 0.3f), roof, pen, n, outline = false, sideWindows = 1)
    }
    for ((k, f) in floatArrayOf(0.3f, 0.37f).withIndex()) {
        val bx = lx(f)
        val wall = if (k == 0) Color(0xFFC0463A) else Color(0xFFF2EEE6)
        drawRect(wall.atNight(n, 0.6f), Offset(bx - 0.5f * s, (hz + 0.006f) * u), Size(s, 0.2f * s), alpha = 0.25f)
        drawHouse3d(bx, (hz + 0.006f) * u, s, 0.42f * s, 0.34f * s, 1.3f * s, wall.atNight(n, 0.3f), Color(0xFF4B4756), pen, n, outline = false, door = Color(0xFF3B3346), sideWindows = 0)
    }
    val bp = 0.3f
    sailboat(st.px((0.58f + 0.12f * sin(t * 0.03f)) * st.span(bp), bp), (hz + 0.05f) * u, 0.05f * u, pen, n, t)
    lighthouse(st.fx(0.86f, 0.4f), (hz + 0.1f) * u, 0.085f * u, pen, n, t)
}

private fun DrawScope.sailboat(bx: Float, wy: Float, s: Float, pen: Pen, n: Float, t: Float) {
    val y = wy + sin(t * 1.2f) * s * 0.03f
    val hull = Path().apply { poly(bx - 0.5f * s, y - 0.12f * s, bx + 0.5f * s, y - 0.12f * s, bx + 0.36f * s, y, bx - 0.36f * s, y) }
    val main = Path().apply { poly(bx + 0.03f * s, y - 0.9f * s, bx + 0.42f * s, y - 0.2f * s, bx + 0.03f * s, y - 0.2f * s) }
    val jib = Path().apply { poly(bx - 0.03f * s, y - 0.78f * s, bx - 0.03f * s, y - 0.2f * s, bx - 0.34f * s, y - 0.2f * s) }
    drawLine(Ink.line, Offset(bx, y - 0.12f * s), Offset(bx, y - 0.92f * s), strokeWidth = pen.lw * 0.8f)
    drawPath(main, Color.White.atNight(n, 0.5f))
    drawPath(jib, Color(0xFFFFE08A).atNight(n, 0.5f))
    drawPath(hull, Color(0xFFE2504A).atNight(n, 0.6f))
    drawPath(hull, Ink.line, alpha = 0.7f, style = pen.thin)
}

private fun DrawScope.lighthouse(bx: Float, wy: Float, s: Float, pen: Pen, n: Float, t: Float) {
    val red = Color(0xFFD9443A).atNight(n, 0.5f)
    val top = wy - 1.25f * s
    if (n > 0.3f) {
        val dir = sin(t * 0.7f)
        val len = 7f * s
        val beam = Path().apply {
            moveTo(bx, top)
            lineTo(bx + dir * len, top - 0.5f * s)
            lineTo(bx + dir * len, top + 0.6f * s)
            close()
        }
        drawPath(beam, Brush.horizontalGradient(listOf(Color(0xFFFFF3B0).copy(alpha = 0.3f * n * abs(dir)), Color(0x00FFF3B0)), startX = bx, endX = bx + dir * len))
        drawCircle(Color(0xFFFFE066), 0.5f * s, Offset(bx, top), alpha = 0.3f * n)
    }
    val rock = Path().apply {
        moveTo(bx - 0.9f * s, wy + 0.02f * s)
        quadraticTo(bx - 0.75f * s, wy - 0.25f * s, bx - 0.3f * s, wy - 0.3f * s)
        quadraticTo(bx + 0.2f * s, wy - 0.38f * s, bx + 0.55f * s, wy - 0.2f * s)
        quadraticTo(bx + 0.85f * s, wy - 0.1f * s, bx + 0.95f * s, wy + 0.02f * s)
        close()
    }
    drawPath(rock, Color(0xFF8C8FA3).atNight(n, 0.6f))
    drawPath(rock, Ink.line, alpha = 0.6f, style = pen.thin)
    val tower = Path().apply { poly(bx - 0.14f * s, top + 0.12f * s, bx + 0.14f * s, top + 0.12f * s, bx + 0.22f * s, wy - 0.28f * s, bx - 0.22f * s, wy - 0.28f * s) }
    drawPath(tower, Color.White.atNight(n, 0.45f))
    clipPath(tower) {
        drawRect(red, Offset(bx - 0.3f * s, wy - 0.62f * s), Size(0.6f * s, 0.16f * s))
        drawRect(red, Offset(bx - 0.3f * s, top + 0.3f * s), Size(0.6f * s, 0.16f * s))
        drawRect(Ink.line, Offset(bx + 0.06f * s, top), Size(0.3f * s, 1.2f * s), alpha = 0.12f)
    }
    drawPath(tower, Ink.line, alpha = 0.8f, style = pen.thin)
    drawRect(Color(0xFF4B4756).atNight(n, 0.4f), Offset(bx - 0.2f * s, top + 0.07f * s), Size(0.4f * s, 0.06f * s))
    drawRect(lerp(Color(0xFFFFF3B0), Color(0xFFFFE066), n), Offset(bx - 0.1f * s, top - 0.08f * s), Size(0.2f * s, 0.16f * s))
    val cap = Path().apply { poly(bx - 0.15f * s, top - 0.08f * s, bx, top - 0.24f * s, bx + 0.15f * s, top - 0.08f * s) }
    drawPath(cap, red)
    drawPath(cap, Ink.line, alpha = 0.8f, style = pen.thin)
}

/** Sparkles on the fjord and the sun's (or moon's) road of light. */
private fun DrawScope.beachGlints(st: Stage, pen: Pen, sunX: Float, moonX: Float, moonVis: Float) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val hz = BEACH_HZ
    val span = st.w + 0.2f * u
    val pts = ArrayList<Offset>(24)
    for (i in 0 until 12) {
        val f = hash01(i, 72)
        val gx = wrap(hash01(i, 71) * span - st.cam * u * mix(0.2f, 0.9f, f) + t * 0.004f * u, span) - 0.1f * u
        val gy = (hz + 0.02f + f * 0.25f) * u
        val len = (0.008f + 0.02f * f) * u * max(0.1f, 0.5f + 0.5f * sin(t * 1.4f + i * 2.3f))
        pts.add(Offset(gx - len, gy))
        pts.add(Offset(gx + len, gy))
    }
    drawPoints(pts, PointMode.Lines, Color.White, strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round, alpha = 0.55f * (1f - 0.5f * n))
    val dayVis = (1f - n) * (1f - overcast(pen))
    val moon = moonVis > dayVis
    val a = max(dayVis, moonVis)
    if (a > 0.05f) {
        val lightX = if (moon) moonX else sunX
        val road = ArrayList<Offset>(16)
        for (k in 0 until 8) {
            val y = (hz + 0.01f + k * 0.034f) * u
            val half = (0.006f + k * 0.006f) * u * (0.7f + 0.3f * sin(t * 2f + k * 1.3f))
            val cx = lightX + sin(t * 1.1f + k) * 0.004f * u
            road.add(Offset(cx - half, y))
            road.add(Offset(cx + half, y))
        }
        drawPoints(road, PointMode.Lines, if (moon) Color(0xFFFFF0BF) else Color(0xFFFFE9A0), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round, alpha = 0.7f * a)
    }
}

private fun DrawScope.beachDetails(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    for ((i, x) in floatArrayOf(0.06f, 0.66f, 1.12f, 1.95f).withIndex()) {
        if (st.sees(x - 0.05f, x + 0.05f)) {
            drawTuft(st.x(x), (sandBackY(x) + 0.008f) * u, 0.055f * u, Color(0xFFA8C46B).atNight(n, 0.45f), pen, sin(t * 1.3f + i) * 0.006f * u)
        }
    }
    if (st.sees(1.2f, 1.6f)) {
        val wood = Color(0xFFC4AE92).atNight(n, 0.45f)
        capsule(st.o(1.44f, 0.8f), st.o(1.49f, 0.78f), 0.008f * u, wood.darken(0.1f), pen)
        val log = Path().apply {
            moveTo(st.x(1.25f), 0.815f * u)
            quadraticTo(st.x(1.25f), 0.797f * u, st.x(1.3f), 0.796f * u)
            lineTo(st.x(1.5f), 0.8f * u)
            quadraticTo(st.x(1.545f), 0.808f * u, st.x(1.51f), 0.818f * u)
            lineTo(st.x(1.29f), 0.82f * u)
            close()
        }
        inked(log, wood, pen)
        drawPoints(
            listOf(st.o(1.32f, 0.805f), st.o(1.46f, 0.807f), st.o(1.34f, 0.812f), st.o(1.42f, 0.813f)),
            PointMode.Lines, wood.darken(0.35f), strokeWidth = pen.lw * 0.5f, cap = StrokeCap.Round,
        )
    }
    if (st.sees(0.6f, 0.72f)) shell(st.o(0.66f, 0.935f), 0.028f * u, Color(0xFFFFB9C6), pen, n)
    if (st.sees(1.68f, 1.78f)) shell(st.o(1.73f, 0.95f), 0.024f * u, Color(0xFFFFE3C2), pen, n)
    if (st.sees(2.0f, 2.1f)) shell(st.o(2.04f, 0.9f), 0.022f * u, Color(0xFFF7C8A0), pen, n)
    if (st.sees(2.6f, 3.9f)) {
        val weed = Path()
        for ((i, x) in floatArrayOf(2.9f, 3.2f, 3.6f).withIndex()) {
            val h = 0.08f + 0.02f * i
            val sway = sin(t * 1.2f + i * 1.7f) * 0.012f
            weed.moveTo(st.x(x), FRONT_Y * u)
            weed.quadraticTo(st.x(x - 0.02f + sway * 0.5f), (FRONT_Y - h * 0.5f) * u, st.x(x + sway), (FRONT_Y - h) * u)
            weed.moveTo(st.x(x + 0.015f), FRONT_Y * u)
            weed.quadraticTo(st.x(x + 0.03f + sway), (FRONT_Y - h * 0.4f) * u, st.x(x + 0.02f + sway * 1.3f), (FRONT_Y - h * 0.75f) * u)
        }
        drawPath(weed, Ink.line, style = Stroke(u * 0.011f, cap = StrokeCap.Round))
        drawPath(weed, Color(0xFF3FA06A).atNight(n, 0.5f), style = Stroke(u * 0.006f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.shell(c: Offset, s: Float, color: Color, pen: Pen, n: Float) {
    val p = Path().apply {
        moveTo(c.x, c.y)
        lineTo(c.x - s * 0.5f, c.y - s * 0.5f)
        quadraticTo(c.x, c.y - s * 1.1f, c.x + s * 0.5f, c.y - s * 0.5f)
        close()
    }
    val col = color.atNight(n, 0.4f)
    inked(p, col, pen, shade = false)
    val r = ArrayList<Offset>(6)
    for (k in -1..1) {
        r.add(c)
        r.add(Offset(c.x + k * s * 0.3f, c.y - s * 0.72f))
    }
    drawPoints(r, PointMode.Lines, col.darken(0.35f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
}

internal fun DrawScope.beachFront(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val w = Places.spec(PlaceId.BEACH).water ?: return
    drawWaterFront(st, pen, w.x1, w.x2, w.line, w.bottom, Color(0xFF6CC4F2), Color(0xFF2F6FB8))
    if (st.sees(w.x1 - 0.05f, w.x1 + 0.15f)) {
        for (k in 0 until 4) {
            val r = (0.01f + 0.004f * sin(t * 3f + k * 2f)) * u
            drawCircle(Color.White, r, st.o(w.x1 + 0.012f + k * 0.03f, w.line + 0.003f), alpha = 0.85f)
        }
    }
    for ((i, x) in floatArrayOf(0.2f, 0.95f, 1.7f).withIndex()) {
        if (st.sees(x - 0.05f, x + 0.05f)) {
            drawTuft(st.x(x), (FRONT_Y + 0.004f) * u, 0.032f * u, Color(0xFF9FC060).atNight(n, 0.4f), pen, sin(t * 1.3f + i * 2f) * 0.004f * u)
        }
    }
}

// =========================================================================================== FOREST

private val cliffEdgeX = floatArrayOf(2.47f, 2.57f, 2.52f, 2.61f, 2.56f, 2.66f, 2.63f, 2.7f)
private val cliffEdgeY = floatArrayOf(-0.4f, 0.12f, 0.26f, 0.4f, 0.53f, 0.65f, 0.74f, 0.79f)
private const val FALL_BASE = 0.792f

private class Cliff(
    val body: Path, val edge: Path, val facets: Path, val ledges: Path, val cracks: List<Offset>,
    val moss: Path, val fall: Path, val fallEdges: Path,
)

/** The mossy cliff behind the pool and the shape of the waterfall, in scene pixels. */
private val forestCliff = Memo { u ->
    val body = Path()
    val edge = Path()
    body.moveTo(cliffEdgeX[0] * u, cliffEdgeY[0] * u)
    body.lineTo(4.6f * u, -0.4f * u)
    body.lineTo(4.6f * u, 0.795f * u)
    for (i in 7 downTo 1) body.lineTo(cliffEdgeX[i] * u, cliffEdgeY[i] * u)
    body.close()
    edge.moveTo(cliffEdgeX[0] * u, cliffEdgeY[0] * u)
    for (i in 1 until 8) edge.lineTo(cliffEdgeX[i] * u, cliffEdgeY[i] * u)
    val facets = Path().apply {
        poly(2.62f * u, 0.08f * u, 2.79f * u, 0.05f * u, 2.81f * u, 0.22f * u, 2.66f * u, 0.26f * u)
        poly(2.65f * u, 0.44f * u, 2.8f * u, 0.41f * u, 2.8f * u, 0.6f * u, 2.69f * u, 0.62f * u)
        poly(3.27f * u, 0.12f * u, 3.5f * u, 0.16f * u, 3.48f * u, 0.34f * u, 3.29f * u, 0.3f * u)
        poly(3.29f * u, 0.5f * u, 3.56f * u, 0.46f * u, 3.57f * u, 0.7f * u, 3.31f * u, 0.72f * u)
        poly(3.7f * u, 0.22f * u, 3.98f * u, 0.18f * u, 4.0f * u, 0.4f * u, 3.72f * u, 0.44f * u)
    }
    // Rock ledges: their lit tops recede up and to the right.
    val ledges = Path()
    val l = floatArrayOf(2.58f, 2.8f, 0.27f, 2.62f, 2.8f, 0.61f, 3.26f, 3.52f, 0.33f, 3.3f, 3.6f, 0.73f, 3.7f, 4.0f, 0.46f)
    for (i in l.indices step 3) ledges.floorQuad(u, 0f, l[i], l[i + 1], l[i + 2], l[i + 2] - 0.022f)
    val c = floatArrayOf(
        2.66f, 0.3f, 2.7f, 0.38f, 2.7f, 0.38f, 2.68f, 0.42f, 3.3f, 0.38f, 3.36f, 0.45f,
        3.36f, 0.45f, 3.34f, 0.49f, 2.6f, 0.66f, 2.66f, 0.74f, 3.45f, 0.2f, 3.52f, 0.26f,
        3.8f, 0.55f, 3.86f, 0.63f,
    )
    val cracks = ArrayList<Offset>(c.size / 2)
    for (i in c.indices step 2) cracks.add(Offset(c[i] * u, c[i + 1] * u))
    val moss = Path()
    val m = floatArrayOf(2.7f, 0.25f, 0.07f, 2.72f, 0.595f, 0.06f, 3.39f, 0.31f, 0.09f, 3.45f, 0.71f, 0.1f, 2.74f, 0.78f, 0.07f, 3.85f, 0.44f, 0.1f)
    for (i in m.indices step 3) moss.addPath(crownPath(m[i] * u, m[i + 1] * u, m[i + 2] * u, 0.018f * u, 40 + i))
    val fall = Path().apply {
        moveTo(2.87f * u, -0.4f * u)
        lineTo(3.17f * u, -0.4f * u)
        quadraticTo(3.19f * u, 0.3f * u, 3.25f * u, FALL_BASE * u)
        lineTo(2.79f * u, FALL_BASE * u)
        quadraticTo(2.85f * u, 0.3f * u, 2.87f * u, -0.4f * u)
        close()
    }
    val fallEdges = Path().apply {
        moveTo(3.17f * u, -0.4f * u)
        quadraticTo(3.19f * u, 0.3f * u, 3.25f * u, FALL_BASE * u)
        moveTo(2.79f * u, FALL_BASE * u)
        quadraticTo(2.85f * u, 0.3f * u, 2.87f * u, -0.4f * u)
    }
    Cliff(body, edge, facets, ledges, cracks, moss, fall, fallEdges)
}

private fun mossBackY(x: Float) = 0.782f + 0.003f * sin(x * 5.3f) + 0.002f * sin(x * 13.1f)

private class ForestGround(
    val fill: Path, val edge: Path, val patches: Path, val stones: Path, val tufts: Path, val leaves: Path,
    val berries: List<Offset>, val flowers: List<Offset>, val fronds: Path, val leaflets: List<Offset>,
    val bed: Path, val roots: Path, val trail: Path, val pebbles: Path,
)

private val forestGround = Memo { u ->
    val fill = Path()
    val edge = Path()
    for (i in 0..30) {
        val x = -0.4f + i * 0.1f
        if (i == 0) {
            fill.moveTo(x * u, mossBackY(x) * u)
            edge.moveTo(x * u, mossBackY(x) * u)
        } else {
            fill.lineTo(x * u, mossBackY(x) * u)
            edge.lineTo(x * u, mossBackY(x) * u)
        }
    }
    for (p in listOf(fill, edge)) {
        p.lineTo(2.66f * u, 0.785f * u)
        p.quadraticTo(2.6f * u, 0.87f * u, 2.55f * u, FRONT_Y * u)
    }
    fill.lineTo(-0.4f * u, FRONT_Y * u)
    fill.close()
    val patches = Path()
    for (i in 0 until 9) {
        patches.floorDisc(u, 0f, 0.1f + hash01(i, 201) * 2.3f, 0.82f + hash01(i, 202) * 0.12f, 0.08f + 0.06f * hash01(i, 203), 0.07f, 16)
    }
    val stones = Path()
    for (i in 0 until 6) {
        stones.floorDisc(u, 0f, 0.2f + hash01(i, 211) * 2.2f, 0.84f + hash01(i, 212) * 0.11f, 0.018f + 0.014f * hash01(i, 213), 0.03f, 12)
    }
    val tufts = scatterTufts(u, 0f, 2.45f, 0.8f, 0.965f, 16, 221, 0.028f)
    val leaves = Path()
    val berries = ArrayList<Offset>(12)
    for ((i, cx) in floatArrayOf(0.56f, 1.06f, 2.0f, 2.36f).withIndex()) {
        val cy = if (i % 2 == 0) 0.81f else 0.9f
        for (k in 0 until 4) {
            val lx = (cx + (k - 1.5f) * 0.018f) * u
            val ly = (cy - 0.012f + (k % 2) * 0.006f) * u
            leaves.addOval(Rect(lx - 0.011f * u, ly - 0.006f * u, lx + 0.011f * u, ly + 0.006f * u))
        }
        for (k in 0 until 3) berries.add(Offset((cx + (k - 1) * 0.013f) * u, (cy - 0.004f - (k % 2) * 0.008f) * u))
    }
    val flowers = ArrayList<Offset>(14)
    for (i in 0 until 14) flowers.add(Offset((0.05f + hash01(i, 231) * 2.4f) * u, (0.81f + hash01(i, 232) * 0.15f) * u))
    val fronds = Path()
    val leaflets = ArrayList<Offset>(120)
    for (fx in floatArrayOf(0.03f, 1.12f, 2.1f)) {
        val by = mossBackY(fx) + 0.01f
        for (k in 0 until 5) {
            val dir = (k - 2) / 2f
            val tipX = fx + dir * 0.075f
            val tipY = by - 0.07f + abs(dir) * 0.03f
            val cX = fx + dir * 0.02f
            val cY = by - 0.1f + abs(dir) * 0.02f
            fronds.moveTo(fx * u, by * u)
            fronds.quadraticTo(cX * u, cY * u, tipX * u, tipY * u)
            for (j in 1..6) {
                val s = j / 7f
                val a = (1f - s) * (1f - s)
                val b = 2f * s * (1f - s)
                val c = s * s
                val px = a * fx + b * cX + c * tipX
                val py = a * by + b * cY + c * tipY
                val tx = 2f * (1f - s) * (cX - fx) + 2f * s * (tipX - cX)
                val ty = 2f * (1f - s) * (cY - by) + 2f * s * (tipY - cY)
                val len = sqrt(tx * tx + ty * ty).coerceAtLeast(1e-4f)
                val l = 0.02f * (1f - s * 0.6f)
                for (side in intArrayOf(-1, 1)) {
                    leaflets.add(Offset(px * u, py * u))
                    leaflets.add(Offset((px - side * ty / len * l + tx / len * l * 0.45f) * u, (py + side * tx / len * l + ty / len * l * 0.45f) * u))
                }
            }
        }
    }
    val bed = Path()
    for (i in 0 until 9) {
        val bx = (2.62f + i * 0.2f + hash01(i, 3) * 0.06f) * u
        val w = (0.03f + 0.025f * hash01(i, 4)) * u
        bed.addOval(Rect(bx - w / 2f, 0.952f * u, bx + w / 2f, 0.952f * u + w * 0.4f))
    }
    val roots = Path()
    for (i in 0 until 7) {
        val rx = (0.2f + i * 0.33f + hash01(i, 241) * 0.1f) * u
        roots.moveTo(rx, (FRONT_Y + 0.008f) * u)
        roots.quadraticTo(rx + 0.03f * u, 0.985f * u, rx + 0.01f * u, 1.01f * u)
    }
    val trail = Path().apply {
        moveTo(0.98f * u, FRONT_Y * u)
        quadraticTo(1.1f * u, 0.87f * u, 1.3f * u, 0.786f * u)
        lineTo(1.38f * u, 0.786f * u)
        quadraticTo(1.22f * u, 0.87f * u, 1.18f * u, FRONT_Y * u)
        close()
    }
    val pebbles = Path()
    for (i in 0 until 7) {
        val f = hash01(i, 251)
        val y = mix(FRONT_Y - 0.01f, 0.8f, f)
        pebbles.floorDisc(u, 0f, 1.08f + f * 0.22f + (hash01(i, 252) - 0.5f) * 0.08f, y, 0.008f + 0.006f * hash01(i, 253), 0.012f, 8)
    }
    ForestGround(fill, edge, patches, stones, tufts, leaves, berries, flowers, fronds, leaflets, bed, roots, trail, pebbles)
}

internal fun DrawScope.forestBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    drawSky(st, pen, Mood.FOREST, 0.66f)
    drawStars(st, pen, 0.5f)
    drawMoon(Offset(st.fx(0.26f, 0.04f), 0.11f * u), 0.038f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, pen, 1.3f, -0.02f, 0.38f)
    drawSun(Offset(st.fx(0.22f, 0.04f), 0.13f * u), 0.05f * u, pen, (1f - n) * (1f - overcast(pen)))
    drawRainbow(Offset(st.fx(0.4f, 0.08f), 0.66f * u), 0.5f * u, 0.016f * u, pen.rainbow)
    drawClouds(st, pen, 0.04f, 0.22f, 3, 0.06f, salt = 5)
    drawPeaks(
        st, 0.08f, 0.56f, 0.6f, 0.15f, 0.28f,
        Color(0xFF9DB5D8).atNight(n, 0.75f), Color(0xFF849FC9).atNight(n, 0.75f),
        Color(0xFFEEF3FC).atNight(n, 0.7f), Color(0xFFD3DFF3).atNight(n, 0.7f), 0.35f, 13,
    )
    drawForestRow(st, 0.2f, 0.6f, 0.04f, 5, 0.045f, 0.06f, 0.11f, pen.farTrees(Color(0xFF6F9E95)).atNight(n, 0.75f), null)
    drawForestRow(st, 0.38f, 0.7f, 0.03f, 9, 0.085f, 0.15f, 0.26f, pen.farTrees(Color(0xFF3F7D62)).atNight(n, 0.65f), pen.farTrees(Color(0xFF356B53)).atNight(n, 0.65f))
    val mp = 0.55f
    drawPath(ridgePath(st, mp, 0.755f, 0.012f, 17), pen.farGround(Color(0xFF82B862)).atNight(n, 0.55f))
    val cabinX = 0.3f * st.span(mp)
    val cbx = st.px(cabinX, mp)
    if (cbx > -0.3f * u && cbx < st.w + 0.3f * u) {
        val cs = 0.12f * u
        drawHouse3d(cbx, (ridgeY(cabinX, 0.755f, 0.012f, 17) + 0.004f) * u, cs, 0.5f * cs, 0.36f * cs, 0.9f * cs, Color(0xFFA8583F), Color(0xFF4B4A58), pen, n, door = Color(0xFF6E4A33), sideWindows = 1, chimney = true, t = t)
    }
    forestNearTrees(st, pen)
    if (st.sees(2.45f, 4.6f)) cliffAndFall(st, pen)
    forestFloor(st, pen)
    fireflies(st, pen)
}

private fun DrawScope.forestNearTrees(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val p = 0.8f
    val spacing = 0.62f
    val l0 = st.cam * p - 0.4f
    val l1 = st.cam * p + st.vw + 0.4f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        if (hash01(i, 51) < 0.2f) continue
        val sx = st.px(i * spacing + (hash01(i, 52) - 0.5f) * 0.28f, p)
        val hgt = (0.5f + 0.15f * hash01(i, 53)) * u
        if (i % 2 == 0) {
            drawBirch(sx, 0.788f * u, hgt, pen, Color(0xFF9CCB5A), i, n)
        } else {
            drawPine(sx, 0.788f * u, hgt * 0.5f, hgt, Pal.granLight.atNight(n, 0.5f), pen)
        }
    }
}

/** The great waterfall: mossy cliff, falling water, the pool's receding surface, foam and spray. */
private fun DrawScope.cliffAndFall(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val cl = forestCliff.of(u)
    val rock = Color(0xFF808C9A).atNight(n, 0.55f)
    inScene(st) {
        drawPath(cl.body, Brush.horizontalGradient(listOf(rock.lighten(0.12f), rock, rock.darken(0.15f)), startX = 2.5f * u, endX = 3.8f * u))
        drawPath(cl.facets, rock.darken(0.1f))
        drawPath(cl.facets, Ink.line, alpha = 0.3f, style = pen.thin)
        drawPath(cl.ledges, pen.snowy(rock.lighten(0.22f), 0.92f))
        drawPath(cl.ledges, Ink.line, alpha = 0.55f, style = pen.thin)
        drawPoints(cl.cracks, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round, alpha = 0.45f)
        drawPath(cl.moss, pen.ground(Color(0xFF5E9E4F)).atNight(n, 0.5f))
        drawPath(cl.moss, Ink.line, alpha = 0.5f, style = pen.thin)
        drawPath(cl.edge, Ink.line, style = pen.stroke)

        drawPath(cl.fall, Brush.verticalGradient(listOf(Color(0xFFBDE8FB).atNight(n, 0.3f), Color(0xFFEFFAFF).atNight(n, 0.25f)), startY = SKY_TOP * u, endY = FALL_BASE * u))
        clipPath(cl.fall) {
            val white = ArrayList<Offset>(28)
            val blue = ArrayList<Offset>(28)
            for (i in 0 until 14) {
                val lane = (2.8f + 0.44f * (i + 0.5f) / 14f) * u
                val speed = 0.55f + 0.3f * hash01(i, 61)
                for (k in 0 until 3) {
                    val y0 = wrap(t * speed + hash01(i * 3 + k, 62) * 1.5f, 1.5f) - 0.5f
                    val len = 0.08f + 0.1f * hash01(i * 3 + k, 63)
                    val list = if ((i + k) % 2 == 0) white else blue
                    list.add(Offset(lane, y0 * u))
                    list.add(Offset(lane, (y0 + len) * u))
                }
            }
            drawPoints(white, PointMode.Lines, Color.White, strokeWidth = u * 0.006f, cap = StrokeCap.Round, alpha = 0.9f)
            drawPoints(blue, PointMode.Lines, Color(0xFF8FCDEE).atNight(n, 0.3f), strokeWidth = u * 0.009f, cap = StrokeCap.Round, alpha = 0.7f)
        }
        drawPath(cl.fallEdges, Ink.line, alpha = 0.7f, style = pen.stroke)
        waterfallGlimmer(Offset(3.02f * u, 0.59f * u), 0.025f * u, pen)
        if (n > 0f) {
            drawPath(cl.fall, Color(0xFFD6EEFF), alpha = 0.25f * n)
            val base = Offset(3.02f * u, 0.76f * u)
            drawCircle(safeRadialGradient(listOf(Color(0xFFBFE6FF).copy(alpha = 0.35f * n), Color(0x00BFE6FF)), center = base, radius = 0.35f * u), 0.35f * u, base)
        }

        // The pool: its front face (0.83..0.95) sits under the front water; its surface recedes to the cliff.
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFF3E9DB6).atNight(n, 0.5f), Color(0xFF1B5E78).atNight(n, 0.5f)), startY = 0.83f * u, endY = 0.95f * u),
            Offset(2.5f * u, 0.83f * u),
            Size(2.1f * u, 0.12f * u),
        )
        val surface = Path().apply { poly(2.6f * u, 0.786f * u, 4.6f * u, 0.786f * u, 4.6f * u, 0.83f * u, 2.55f * u, 0.83f * u) }
        drawPath(surface, Brush.verticalGradient(listOf(Color(0xFFA8E2EE).atNight(n, 0.45f), Color(0xFF55B6CC).atNight(n, 0.5f)), startY = 0.786f * u, endY = 0.83f * u))
        val shimmer = ArrayList<Offset>(24)
        for (i in 0 until 12) {
            val x = (2.82f + i * 0.036f) * u
            val len = (0.01f + 0.018f * (0.5f + 0.5f * sin(t * 3f + i * 1.7f))) * u
            val y = (0.8f + 0.012f * hash01(i, 71)) * u
            shimmer.add(Offset(x, y))
            shimmer.add(Offset(x, y + len))
        }
        drawPoints(shimmer, PointMode.Lines, Color.White, strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round, alpha = 0.6f)
        val foam = Path()
        for (i in 0 until 8) {
            val r = (0.022f + 0.008f * sin(t * 3.4f + i * 1.9f)) * u
            foam.addOval(Rect(Offset((2.8f + i * 0.063f) * u, (FALL_BASE + 0.002f * sin(t * 2.6f + i)) * u), r))
        }
        drawPath(foam, Color(0xFFF2FAFF).atNight(n, 0.2f))
        val spray = 0.3f * (1f - n) * (1f - overcast(pen)) + 0.5f * pen.rainbow
        drawRainbow(Offset(3.02f * u, 0.81f * u), 0.18f * u, 0.008f * u, spray.coerceAtMost(0.6f))
    }
    drawWaterPlane(st, pen, 2.62f, 4.6f, 0.795f, 0.826f, Color.White, 3, 1f, 1f, 0.5f)
}

private fun DrawScope.forestFloor(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val g = forestGround.of(u)
    val back = PlaceId.FOREST.back
    val fern = pen.plant(Color(0xFF4E9A48)).atNight(n, 0.45f)
    val moss = pen.ground(Color(0xFF7DB85F)).atNight(n, 0.45f)
    inScene(st) {
        drawRect(Color(0xFF5E7468).atNight(n, 0.5f), Offset(2.5f * u, 0.95f * u), Size(2.1f * u, 0.03f * u))
        drawPath(g.bed, Color(0xFF8C9A92).atNight(n, 0.5f))
        drawPath(g.fill, Brush.verticalGradient(0f to moss.darken(0.12f), 0.12f to moss, 1f to pen.ground(Color(0xFF62A04B)).atNight(n, 0.45f), startY = back * u, endY = FRONT_Y * u))
        drawPath(g.patches, pen.ground(Color(0xFF5C9A45)).atNight(n, 0.45f), alpha = 0.55f)
        drawPath(g.trail, pen.sandy(Color(0xFFBF9E6C)).atNight(n, 0.45f), alpha = 0.85f)
        drawPath(g.pebbles, Color(0xFFA8A29A).atNight(n, 0.45f))
        drawPath(g.stones, Color(0xFF9A9CA8).atNight(n, 0.45f))
        drawPath(g.stones, Ink.line, style = pen.thin)
        drawPath(g.tufts, pen.blade(Color(0xFF4F9440)).atNight(n, 0.45f))
        if (!pen.winter) drawPoints(g.flowers, PointMode.Points, Color(0xFFFFF6D8).atNight(n, 0.4f), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
        drawPath(g.edge, Ink.line, style = pen.stroke)
        drawPath(g.fronds, Ink.line, style = Stroke(pen.lw * 2.4f, cap = StrokeCap.Round))
        drawPath(g.fronds, fern, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        drawPoints(g.leaflets, PointMode.Lines, fern, strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
        drawPath(g.leaves, pen.plant(Color(0xFF2E7D46)).atNight(n, 0.45f))
        drawPoints(g.berries, PointMode.Points, Color(0xFFD8243A).atNight(n, 0.35f), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    }
    if (st.sees(2.45f, 2.75f)) {
        val stems = Path()
        val tails = ArrayList<Pair<Offset, Offset>>(3)
        for (k in 0 until 6) {
            val bx = 2.55f + k * 0.022f + hash01(k, 80) * 0.01f
            val by = 0.86f - k * 0.012f
            val tip = by - 0.2f - 0.05f * hash01(k, 81)
            val sway = sin(t * 1.1f + k * 0.9f) * 0.008f
            stems.moveTo(st.x(bx), by * u)
            stems.quadraticTo(st.x(bx + sway * 0.3f), mix(by, tip, 0.5f) * u, st.x(bx + sway), tip * u)
            if (k % 2 == 1) tails.add(st.o(bx + sway * 0.97f, tip + 0.012f) to st.o(bx + sway * 0.9f, tip + 0.055f))
        }
        drawPath(stems, Ink.line, style = Stroke(u * 0.0075f, cap = StrokeCap.Round))
        drawPath(stems, pen.plant(Color(0xFF5E9B45)).atNight(n, 0.45f), style = Stroke(u * 0.004f, cap = StrokeCap.Round))
        for ((a, b) in tails) capsule(a, b, 0.012f * u, Color(0xFF7A4B2E).atNight(n, 0.4f), pen)
    }
    val pad = pen.plant(Color(0xFF4FA35A)).atNight(n, 0.45f)
    for ((i, px) in floatArrayOf(2.72f, 3.36f).withIndex()) {
        if (pen.winter || !st.sees(px - 0.06f, px + 0.06f)) continue
        val cx = px + sin(t * 0.4f + i) * 0.004f
        val cy = 0.812f - i * 0.01f
        val leaf = Path().apply { floorDisc(u, st.cam, cx, cy, 0.04f - i * 0.006f, 0.05f, 14) }
        inked(leaf, pad, pen, shade = false)
        drawLine(pad.darken(0.35f), st.o(cx, cy), st.o(cx + 0.03f, cy + 0.006f), strokeWidth = pen.lw)
        if (i == 0) {
            val f = st.o(cx - 0.008f, cy - 0.01f)
            inked(starPath(f, 0.014f * u, 0.006f * u), Color(0xFFFF9EC4).atNight(n, 0.35f), pen, shade = false)
            drawCircle(Pal.sun, 0.004f * u, f)
        }
    }
    drawBase(st, pen, pen.ground(Color(0xFF6FAE5A)).atNight(n, 0.45f), Color(0xFF6B4A34).atNight(n, 0.45f))
    inScene(st) { drawPath(g.roots, Color(0xFF4A3222).atNight(n, 0.4f), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round)) }
    drawBaseStones(st, pen, Color(0xFF9A9CA8).atNight(n, 0.45f), 23)
}

private fun DrawScope.fireflies(st: Stage, pen: Pen) {
    val k = ramp((pen.night - 0.35f) / 0.4f)
    if (k <= 0f || pen.season == app.trollfoss.domain.Season.AUTUMN || pen.winter) return
    val u = st.u
    val a = ArrayList<Offset>(8)
    val b = ArrayList<Offset>(8)
    for (i in 0 until 14) {
        val fx = 0.1f + hash01(i, 91) * 2.4f + sin(pen.t * 0.4f + i * 1.7f) * 0.06f
        if (!st.sees(fx, fx)) continue
        val fy = 0.5f + hash01(i, 92) * 0.32f + sin(pen.t * 0.63f + i) * 0.03f
        (if (i % 2 == 0) a else b).add(st.o(fx, fy))
    }
    val glow = Color(0xFFE9FF7A)
    for ((pts, phase) in listOf(a to 0f, b to 3.1f)) {
        val p = 0.5f + 0.5f * sin(pen.t * 2.1f + phase)
        drawPoints(pts, PointMode.Points, glow, strokeWidth = u * 0.032f, cap = StrokeCap.Round, alpha = 0.2f * k * p)
        drawPoints(pts, PointMode.Points, glow, strokeWidth = u * 0.009f, cap = StrokeCap.Round, alpha = k * (0.3f + 0.7f * p))
    }
}

internal fun DrawScope.forestFront(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val w = Places.spec(PlaceId.FOREST).water
    if (w != null) drawWaterFront(st, pen, w.x1, w.x2 + 1.2f, w.line, w.bottom, Color(0xFF7BD3E0), Color(0xFF1F6A86))
    if (st.sees(2.7f, 3.4f)) {
        for (i in 0 until 5) {
            val ph = wrap(t * 0.35f + i / 5f, 1f)
            val bx = 2.86f + hash01(i, 97) * 0.34f - ph * 0.05f
            drawCircle(Color.White, (0.006f + 0.006f * (1f - ph)) * u, st.o(bx, mix(FALL_BASE + 0.01f, 0.834f, ph)), alpha = 0.8f * (1f - ph * 0.5f))
        }
        for (i in 0 until 6) {
            val ph = wrap(t * 0.12f + i / 6f, 1f)
            val mx = 3.02f + (hash01(i, 95) - 0.5f) * 0.4f - ph * 0.22f + sin(ph * 4f + i) * 0.03f
            val my = FALL_BASE - ph * 0.2f
            drawCircle(Color.White, (0.05f + ph * 0.07f) * u, st.o(mx, my), alpha = 0.26f * sin(ph * 3.1416f) * (1f - 0.3f * n))
        }
    }
    for ((i, x) in floatArrayOf(0.15f, 0.95f, 1.75f, 2.35f).withIndex()) {
        if (st.sees(x - 0.05f, x + 0.05f)) {
            drawTuft(st.x(x), (FRONT_Y + 0.004f) * u, 0.034f * u, Color(0xFF5FA34C).atNight(n, 0.45f), pen, sin(t * 1.2f + i * 2f) * 0.004f * u)
        }
    }
}

// ========================================================================================= MOUNTAIN

private fun snowBackY(x: Float) = 0.781f + 0.004f * sin(x * 3.3f) + 0.002f * sin(x * 8.1f)

private class SnowGround(
    val fill: Path, val edge: Path, val drifts: Path, val pillows: Path, val tracks: Path, val prints: List<Offset>, val icicles: Path,
    val rocks: Path, val rockSnow: Path,
)

private val snowGround = Memo { u ->
    val fill = Path()
    val edge = Path()
    for (i in 0..44) {
        val x = -0.4f + i * 0.1f
        if (i == 0) {
            fill.moveTo(x * u, snowBackY(x) * u)
            edge.moveTo(x * u, snowBackY(x) * u)
        } else {
            fill.lineTo(x * u, snowBackY(x) * u)
            edge.lineTo(x * u, snowBackY(x) * u)
        }
    }
    fill.lineTo(4f * u, FRONT_Y * u)
    fill.lineTo(-0.4f * u, FRONT_Y * u)
    fill.close()
    val drifts = Path()
    val s = floatArrayOf(0.45f, 0.9f, 0.2f, 1.45f, 0.93f, 0.24f, 2.7f, 0.9f, 0.18f, 3.35f, 0.94f, 0.22f, 0.9f, 0.82f, 0.14f, 3.0f, 0.82f, 0.16f)
    for (i in s.indices step 3) drifts.floorDisc(u, 0f, s[i], s[i + 1], s[i + 2], 0.06f, 18)
    // Ski tracks running back into the picture, and a sled's curve.
    val tracks = Path()
    for ((x0, bend) in listOf(0.48f to 0.06f, 2.86f to -0.05f)) {
        for (off in floatArrayOf(0f, 0.016f)) {
            tracks.moveTo((x0 + off) * u, FRONT_Y * u)
            tracks.quadraticTo((x0 + off + 0.13f + bend) * u, 0.875f * u, (x0 + off + 0.264f) * u, 0.784f * u)
        }
    }
    val prints = ArrayList<Offset>(16)
    for (k in 0 until 14) {
        val f = k / 13f
        prints.add(Offset((mix(1.5f, 1.86f, f) + (if (k % 2 == 0) -0.008f else 0.008f)) * u, mix(0.955f, 0.8f, f) * u))
    }
    val pillows = Path()
    val pl = floatArrayOf(0.62f, 0.93f, 0.07f, 1.62f, 0.83f, 0.06f, 2.2f, 0.945f, 0.08f, 3.5f, 0.86f, 0.07f, 0.15f, 0.87f, 0.05f)
    for (i in pl.indices step 3) pillows.floorDisc(u, 0f, pl[i], pl[i + 1], pl[i + 2], 0.05f, 16)
    val rocks = Path()
    val rockSnow = Path()
    val rk = floatArrayOf(0.95f, 0.796f, 0.05f, 1.92f, 0.79f, 0.065f, 2.97f, 0.797f, 0.045f, 3.76f, 0.79f, 0.06f)
    for (i in rk.indices step 3) {
        val cx = rk[i]
        val by = rk[i + 1]
        val r = rk[i + 2]
        rocks.moveTo((cx - r) * u, by * u)
        rocks.lineTo((cx - r * 0.7f) * u, (by - r * 0.7f) * u)
        rocks.lineTo((cx - r * 0.1f) * u, (by - r * 0.95f) * u)
        rocks.lineTo((cx + r * 0.6f) * u, (by - r * 0.75f) * u)
        rocks.lineTo((cx + r) * u, by * u)
        rocks.close()
        rockSnow.moveTo((cx - r * 0.78f) * u, (by - r * 0.6f) * u)
        rockSnow.lineTo((cx - r * 0.1f) * u, (by - r * 1.0f) * u)
        rockSnow.lineTo((cx + r * 0.66f) * u, (by - r * 0.72f) * u)
        rockSnow.quadraticTo((cx + r * 0.2f) * u, (by - r * 0.5f) * u, (cx - r * 0.1f) * u, (by - r * 0.62f) * u)
        rockSnow.quadraticTo((cx - r * 0.4f) * u, (by - r * 0.5f) * u, (cx - r * 0.78f) * u, (by - r * 0.6f) * u)
        rockSnow.close()
    }
    val icicles = Path()
    for (i in 0 until 26) {
        val x = -0.4f + i * 0.18f + hash01(i, 121) * 0.08f
        val len = 0.012f + 0.02f * hash01(i, 122)
        icicles.moveTo((x - 0.006f) * u, (FRONT_Y + 0.006f) * u)
        icicles.lineTo((x + 0.006f) * u, (FRONT_Y + 0.006f) * u)
        icicles.lineTo(x * u, (FRONT_Y + 0.006f + len) * u)
        icicles.close()
    }
    SnowGround(fill, edge, drifts, pillows, tracks, prints, icicles, rocks, rockSnow)
}

internal fun DrawScope.mountainBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val hz = 0.7f
    // The engine turns sunny weather into snowfall up here; keep the clear winter sky behind the flakes.
    val sky = if (pen.weather == Weather.SNOW) Pen(pen.lw, pen.t, pen.night, Weather.SUN, pen.rainbow) else pen
    drawSky(st, sky, Mood.WINTER, hz)
    drawStars(st, sky, 0.55f)
    drawMoon(Offset(st.fx(0.3f, 0.04f), 0.12f * u), 0.04f * u, sky, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, sky, 0.85f, 0.02f, 0.32f)
    val vis = (1f - n) * (1f - 0.8f * overcast(sky))
    if (vis > 0.01f) {
        val c = Offset(st.fx(0.66f, 0.04f), 0.5f * u)
        val glow = Color(0xFFFFCF70)
        drawCircle(safeRadialGradient(listOf(glow.copy(alpha = 0.6f * vis), glow.copy(alpha = 0f)), center = c, radius = 0.55f * u), 0.55f * u, c)
        drawSun(c, 0.06f * u, pen, vis, rays = false, color = Color(0xFFFFC23D))
    }
    drawRainbow(Offset(st.fx(0.45f, 0.08f), hz * u), 0.5f * u, 0.016f * u, pen.rainbow)
    drawClouds(st, sky, 0.05f, 0.24f, if (sky !== pen) 3 else 2, 0.06f, salt = 9)
    drawPeaks(
        st, 0.08f, 0.7f, 0.7f, 0.28f, 0.48f,
        Color(0xFFBCCAE6).atNight(n, 0.75f), Color(0xFF9FB1D6).atNight(n, 0.75f),
        Color(0xFFF4F8FF).atNight(n, 0.7f), Color(0xFFCFDBF0).atNight(n, 0.7f), 0.5f, 21,
    )
    drawPeaks(
        st, 0.16f, 0.735f, 0.5f, 0.16f, 0.28f,
        Color(0xFFD6E1F4).atNight(n, 0.7f), Color(0xFFAABDDF).atNight(n, 0.7f),
        Color(0xFFF9FBFF).atNight(n, 0.65f), Color(0xFFD2DDF1).atNight(n, 0.65f), 0.72f, 27,
    )
    val hp = 0.28f
    chairLift(st, pen, hp)
    drawForestRow(
        st, hp, 0.73f, 0.045f, 31, 0.05f, 0.03f, 0.055f, Color(0xFF557385).atNight(n, 0.7f), null,
        skip = 0.4f, ground = Color(0xFFEEF3FB).atNight(n, 0.65f),
    )
    val span = st.span(hp)
    for ((i, f) in floatArrayOf(0.14f, 0.2f, 0.74f).withIndex()) {
        val lx = f * span
        val wall = if (i == 1) Color(0xFFC0463A) else Color(0xFF9A6A48)
        val s = 0.04f * u
        drawHouse3d(st.px(lx, hp), (ridgeY(lx, 0.73f, 0.045f, 31) + 0.008f) * u, s, 0.5f * s, 0.4f * s, 0.9f * s, wall, Color(0xFFF7FAFF), pen, n, outline = false, sideWindows = 1)
    }
    mountainSlope(st, pen)
    val g = snowGround.of(u)
    val back = PlaceId.MOUNTAIN.back
    inScene(st) {
        drawPath(
            g.fill,
            Brush.verticalGradient(
                0f to Color(0xFFD8E2F4).atNight(n, 0.5f),
                0.15f to Color(0xFFF2F6FD).atNight(n, 0.5f),
                1f to Color.White.atNight(n, 0.5f),
                startY = back * u,
                endY = FRONT_Y * u,
            ),
        )
        drawRect(
            Brush.verticalGradient(listOf(Color(0x33FFC98A), Color(0x00FFC98A)), startY = back * u, endY = 0.88f * u),
            Offset(-2f * u, back * u), Size(8f * u, 0.1f * u), alpha = 1f - n,
        )
        drawPath(g.drifts, Pal.snowShade.atNight(n, 0.5f), alpha = 0.5f)
        translate(0.012f * u, 0.006f * u) { drawPath(g.pillows, Color(0xFFB9C9E6).atNight(n, 0.5f), alpha = 0.8f) }
        drawPath(g.pillows, Color.White.atNight(n, 0.5f))
        drawPath(g.pillows, Ink.line, alpha = 0.25f, style = pen.thin)
        drawPath(g.tracks, Color(0xFF9DB2DA).atNight(n, 0.5f), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        drawPoints(g.prints, PointMode.Points, Color(0xFF9DB2DA).atNight(n, 0.5f), strokeWidth = 0.009f * u, cap = StrokeCap.Round)
        drawPath(g.edge, Ink.line, alpha = 0.6f, style = pen.stroke)
        drawPath(g.rocks, Color(0xFF8C94AB).atNight(n, 0.5f))
        drawPath(g.rocks, Ink.line, style = pen.thin)
        drawPath(g.rockSnow, Color.White.atNight(n, 0.5f))
        drawPath(g.rockSnow, Ink.line, alpha = 0.5f, style = pen.thin)
    }
    for (i in 0 until 7) {
        val sx = hash01(i, 101) * 3.8f
        if (!st.sees(sx, sx)) continue
        val a = max(0f, sin(t * 1.3f + i * 2.2f))
        if (a > 0.05f) twinkle(st.o(sx, 0.8f + hash01(i, 102) * 0.16f), 0.012f * u, Color.White, a * (1f - 0.4f * n))
    }
    drawBase(st, pen, Color.White.atNight(n, 0.45f), Color(0xFF7A84A3).atNight(n, 0.45f))
    drawBaseStones(st, pen, Color(0xFF9AA3BF).atNight(n, 0.45f), 29)
    inScene(st) {
        drawPath(g.icicles, Color(0xFFE6F3FF).atNight(n, 0.4f))
        drawPath(g.icicles, Color(0xFF9DB2DA), alpha = 0.6f, style = pen.thin)
    }
}

/** A chair lift climbing a groomed run on the far slope; the chairs creep up and down. */
private fun DrawScope.chairLift(st: Stage, pen: Pen, p: Float) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val span = st.span(p)
    val l0 = 0.36f * span
    val l1 = 0.6f * span
    val x0 = st.px(l0, p)
    val x1 = st.px(l1, p)
    if (x1 < -0.1f * u || x0 > st.w + 0.1f * u) return
    val y0 = ridgeY(l0, 0.73f, 0.045f, 31) - 0.035f
    val y1 = 0.4f
    val run = Path().apply {
        poly(x0 - 0.02f * u, (y0 + 0.045f) * u, x1 - 0.015f * u, (y1 + 0.045f) * u, x1 + 0.03f * u, (y1 + 0.045f) * u, x0 + 0.08f * u, (y0 + 0.045f) * u)
    }
    drawPath(run, Color.White.atNight(n, 0.6f), alpha = 0.85f)
    fun cable(s: Float, off: Float) = Offset(mix(x0, x1, s), (mix(y0, y1, s) + off) * u + 2f * s * (1f - s) * 0.02f * u)
    val metal = Color(0xFF5B5F73).atNight(n, 0.55f)
    val lines = ArrayList<Offset>(40)
    for (k in 0..4) {
        val c = cable(k / 4f, 0f)
        lines.add(Offset(c.x, c.y - 0.004f * u))
        lines.add(Offset(c.x, c.y + 0.04f * u))
    }
    val wire = Path()
    for (off in floatArrayOf(0f, 0.007f)) {
        val a = cable(0f, off)
        val b = cable(1f, off)
        val m = cable(0.5f, off)
        wire.moveTo(a.x, a.y)
        wire.quadraticTo(2f * m.x - (a.x + b.x) / 2f, 2f * m.y - (a.y + b.y) / 2f, b.x, b.y)
    }
    val seats = Path()
    for (k in 0 until 7) {
        val s = wrap(t * 0.012f + k / 7f, 1f)
        for ((ss, off) in listOf(s to 0f, (1f - s) to 0.007f)) {
            val c = cable(ss, off)
            lines.add(c)
            lines.add(Offset(c.x, c.y + 0.02f * u))
            seats.addRoundRect(RoundRect(Rect(c.x - 0.008f * u, c.y + 0.017f * u, c.x + 0.008f * u, c.y + 0.024f * u), CornerRadius(0.002f * u)))
        }
    }
    drawPoints(lines, PointMode.Lines, metal, strokeWidth = pen.lw * 0.7f)
    drawPath(wire, metal, style = Stroke(pen.lw * 0.6f))
    drawPath(seats, Color(0xFFE24C4C).atNight(n, 0.55f))
    val hs = 0.045f * u
    drawHouse3d(x0 + 0.01f * u, (y0 + 0.05f) * u, hs, 0.5f * hs, 0.35f * hs, 0.8f * hs, Color(0xFFB9C0CC), Color(0xFFF7FAFF), pen, n, outline = false, sideWindows = 1)
    drawHouse3d(x1 + 0.005f * u, (y1 + 0.05f) * u, hs * 0.8f, 0.4f * hs, 0.3f * hs, 0.6f * hs, Color(0xFFB9C0CC), Color(0xFFF7FAFF), pen, n, outline = false, sideWindows = 0)
}

/** The near snowy slope with ski tracks and snow-covered pines. */
private fun DrawScope.mountainSlope(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val sp = 0.5f
    val base = 0.775f
    val amp = 0.03f
    drawPath(
        ridgePath(st, sp, base, amp, 37),
        Brush.verticalGradient(listOf(Color.White.atNight(n, 0.55f), Color(0xFFDCE6F6).atNight(n, 0.55f)), startY = 0.72f * u, endY = 0.8f * u),
    )
    drawPath(ridgePath(st, sp, base, amp, 37, closed = false), Ink.line, alpha = 0.4f, style = pen.thin)
    val tracks = Path()
    for (off in floatArrayOf(0f, 0.006f)) {
        for (i in 0 until 16) {
            val sx = -0.05f * st.w + i * st.w * 1.1f / 15f
            val lx = sx / u + st.cam * sp
            val y = (ridgeY(lx, base, amp, 37) + 0.014f + off + 0.006f * sin(lx * 3f)) * u
            if (i == 0) tracks.moveTo(sx, y) else tracks.lineTo(sx, y)
        }
    }
    drawPath(tracks, Color(0xFFB8C8E4).atNight(n, 0.55f), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    val spacing = 0.36f
    val l0 = st.cam * sp - 0.3f
    val l1 = st.cam * sp + st.vw + 0.3f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        if (hash01(i, 111) < 0.3f) continue
        val lx = i * spacing + (hash01(i, 112) - 0.5f) * 0.14f
        val h = (0.16f + 0.08f * hash01(i, 113)) * u
        drawPine(st.px(lx, sp), (ridgeY(lx, base, amp, 37) + 0.01f) * u, h * 0.55f, h, Color(0xFF2F6E5A).atNight(n, 0.5f), pen, snow = Color.White.atNight(n, 0.5f))
    }
}

internal fun DrawScope.mountainFront(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val count = 22
    val xs = FloatArray(count)
    val ys = FloatArray(count)
    for (i in 0 until count) {
        val sx = -0.05f * st.w + i * st.w * 1.1f / (count - 1)
        val lx = sx / u + st.cam
        xs[i] = sx
        ys[i] = (0.974f - 0.012f * max(0f, sin(lx * 4.1f + 1f)) - 0.006f * max(0f, sin(lx * 9.3f))) * u
    }
    val drift = Path().apply {
        smoothRun(xs, ys, count, start = true)
        lineTo(xs[count - 1], (FRONT_Y + 0.01f) * u)
        lineTo(xs[0], (FRONT_Y + 0.01f) * u)
        close()
    }
    drawPath(drift, Brush.verticalGradient(listOf(Color.White.atNight(n, 0.45f), Color(0xFFDCE6F6).atNight(n, 0.45f)), startY = 0.955f * u, endY = (FRONT_Y + 0.01f) * u))
    drawPath(Path().apply { smoothRun(xs, ys, count, start = true) }, Ink.line, alpha = 0.5f, style = pen.thin)
    for (i in 0 until 2) {
        val a = max(0f, sin(pen.t * 1.5f + i * 2.7f))
        if (a > 0.05f) twinkle(Offset(st.w * (0.3f + 0.45f * i), ys[6 + i * 7] + 0.006f * u), 0.009f * u, Color.White, a)
    }
}
