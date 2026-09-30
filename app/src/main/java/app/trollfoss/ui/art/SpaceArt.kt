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
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.PlaceId
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Romstasjonen: a cut-away station module. Curved white panels with ring frames, cables, a handrail and
 * blinking lights; a metal grating floor receding into the picture; and a long window onto space where
 * the Sun and the eight planets drift by in their true order. Space is always dark: night is ignored.
 */

/** Ring frames, where they meet the back wall. */
private val SPACE_RIBS = floatArrayOf(-0.25f, 0.55f, 1.35f, 2.15f, 2.95f, 3.75f, 4.55f)
private const val WIN_X0 = 1.24f
private const val WIN_X1 = 2.9f
private const val WIN_Y0 = 0.13f
private const val WIN_Y1 = 0.4f
private const val RIB_W = 0.05f

private class SpaceStatic(
    val seams: List<Offset>, val rivets: List<Offset>, val ribs: Path, val ribLight: Path, val ceilingRibs: Path,
    val grid: List<Offset>, val floorRibs: Path, val cables: List<Path>, val clips: Path, val baseRivets: List<Offset>,
    val vent: List<Offset>,
)

private val cableColors = listOf(Color(0xFFF28C38), Color(0xFF3E7BD6), Color(0xFF6B7785))

private val spaceStatic = Memo { u ->
    val back = PlaceId.SPACE.back
    val seams = ArrayList<Offset>()
    val rivets = ArrayList<Offset>()
    for (y in floatArrayOf(0.115f, 0.46f, 0.64f)) {
        seams.add(Offset(-0.5f * u, y * u))
        seams.add(Offset(4.8f * u, y * u))
        var x = -0.5f
        while (x < 4.8f) {
            rivets.add(Offset(x * u, (y - 0.01f) * u))
            rivets.add(Offset(x * u, (y + 0.01f) * u))
            x += 0.07f
        }
    }
    for (k in 0 until SPACE_RIBS.size - 1) {
        val x = (SPACE_RIBS[k] + SPACE_RIBS[k + 1]) / 2f
        seams.add(Offset(x * u, 0.115f * u))
        seams.add(Offset(x * u, (back - 0.02f) * u))
    }
    val ribs = Path()
    val ribLight = Path()
    val ceilingRibs = Path()
    val floorRibs = Path()
    val shift = recede(back)
    for (xb in SPACE_RIBS) {
        ribs.addRect(Rect((xb - RIB_W / 2f) * u, 0.1f * u, (xb + RIB_W / 2f) * u, back * u))
        ribLight.addRect(Rect((xb - RIB_W / 2f) * u, 0.1f * u, (xb - RIB_W * 0.1f) * u, back * u))
        ceilingRibs.moveTo(xb * u, 0.101f * u)
        ceilingRibs.quadraticTo((xb - 0.02f) * u, -0.02f * u, (xb - 0.26f) * u, (SKY_TOP - 0.01f) * u)
        floorRibs.floorQuad(u, 0f, xb - shift - RIB_W / 2f, xb - shift + RIB_W / 2f, FRONT_Y, back)
    }
    val grid = ArrayList<Offset>()
    var y = back + 0.034f
    while (y < FRONT_Y) {
        grid.add(Offset(-0.5f * u, y * u))
        grid.add(Offset(4.8f * u, y * u))
        y += 0.034f
    }
    var gx = -0.8f
    while (gx < 4.6f) {
        grid.add(Offset(gx * u, FRONT_Y * u))
        grid.add(Offset((gx + shift) * u, back * u))
        gx += 0.07f
    }
    val cables = List(cableColors.size) { Path() }
    val clips = Path()
    for (k in 0 until SPACE_RIBS.size - 1) {
        val a = SPACE_RIBS[k]
        val b = SPACE_RIBS[k + 1]
        for (c in cables.indices) {
            val cy = 0.078f + c * 0.012f
            cables[c].moveTo(a * u, cy * u)
            cables[c].quadraticTo((a + b) / 2f * u, (cy + 0.05f + c * 0.012f) * u, b * u, cy * u)
        }
        clips.addRect(Rect((a - 0.02f) * u, 0.07f * u, (a + 0.02f) * u, 0.112f * u))
    }
    val baseRivets = ArrayList<Offset>()
    var bx = -0.5f
    while (bx < 4.8f) {
        baseRivets.add(Offset(bx * u, 0.986f * u))
        bx += 0.06f
    }
    val vent = ArrayList<Offset>()
    for (k in 0 until 5) {
        val vy = 0.668f + k * 0.012f
        vent.add(Offset(1.47f * u, vy * u))
        vent.add(Offset(1.61f * u, vy * u))
    }
    SpaceStatic(seams, rivets, ribs, ribLight, ceilingRibs, grid, floorRibs, cables, clips, baseRivets, vent)
}

internal fun DrawScope.spaceBack(st: Stage, pen: Pen) {
    val u = st.u
    val back = PlaceId.SPACE.back
    val ss = spaceStatic.of(u)
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF6E7A8B), 0.28f to Color(0xFFA9B4C1), 0.36f to Color(0xFFD2DAE2), 0.42f to Color(0xFFEEF2F6), 0.72f to Color(0xFFE5EAF0), 1f to Color(0xFFB9C3CF),
            startY = SKY_TOP * u, endY = back * u,
        ),
        Offset(0f, SKY_TOP * u),
        Size(st.w, (back - SKY_TOP) * u),
    )
    inScene(st) {
        drawPoints(ss.seams, PointMode.Lines, Color(0xFFA9B4C1), strokeWidth = pen.lw * 0.8f)
        drawPoints(ss.rivets, PointMode.Points, Color(0xFF98A4B3), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
        drawPath(ss.ceilingRibs, Ink.line, style = Stroke(RIB_W * u + pen.lw * 2f))
        drawPath(ss.ceilingRibs, Color(0xFFA7B2BF), style = Stroke(RIB_W * u))
        drawPath(ss.ribs, Color(0xFFB9C3CE))
        drawPath(ss.ribLight, Color(0xFFDDE4EB))
        drawPath(ss.ribs, Ink.line, style = pen.thin)
        drawRect(Color(0xFF8E99A7), Offset(1.46f * u, 0.66f * u), Size(0.16f * u, 0.07f * u))
        drawPoints(ss.vent, PointMode.Lines, Color(0xFF4E5866), strokeWidth = 0.005f * u)
        drawRect(Ink.line, Offset(1.46f * u, 0.66f * u), Size(0.16f * u, 0.07f * u), style = pen.thin)
    }
    spaceWindow(st, pen)
    inScene(st) {
        for ((i, c) in ss.cables.withIndex()) {
            drawPath(c, Ink.line, style = Stroke(0.009f * u + pen.lw * 1.6f, cap = StrokeCap.Round))
            drawPath(c, cableColors[i], style = Stroke(0.009f * u, cap = StrokeCap.Round))
        }
        drawPath(ss.clips, Color(0xFF59626F))
        drawPath(ss.clips, Ink.line, style = pen.thin)
    }
    if (st.sees(WIN_X0, WIN_X1)) {
        val brackets = ArrayList<Offset>(10)
        for (k in 0 until 4) {
            val bx = mix(WIN_X0 + 0.06f, WIN_X1 - 0.06f, k / 3f)
            brackets.add(st.o(bx, 0.452f))
            brackets.add(st.o(bx, 0.472f))
        }
        drawPoints(brackets, PointMode.Lines, Color(0xFF59626F), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
        capsule(st.o(WIN_X0 + 0.02f, 0.452f), st.o(WIN_X1 - 0.02f, 0.452f), 0.013f * u, Color(0xFFF2B33D), pen)
    }
    leds(st, pen)

    drawRect(
        Brush.verticalGradient(0f to Color(0xFF363F4B), 1f to Color(0xFF63707F), startY = back * u, endY = FRONT_Y * u),
        Offset(0f, back * u),
        Size(st.w, (FRONT_Y - back) * u),
    )
    inScene(st) {
        drawPath(ss.floorRibs, Color(0xFF7C8898))
        drawPoints(ss.grid, PointMode.Lines, Color(0xFF808D9C), strokeWidth = pen.lw * 0.7f)
        drawPath(ss.floorRibs, Ink.line, style = pen.thin)
    }
    drawRect(
        Brush.verticalGradient(listOf(Color(0x667CF6FF), Color(0x007CF6FF)), startY = back * u, endY = (back + 0.05f) * u),
        Offset(0f, back * u),
        Size(st.w, 0.05f * u),
    )
    drawLine(Color(0xFF9CF8FF), Offset(0f, (back + 0.003f) * u), Offset(st.w, (back + 0.003f) * u), strokeWidth = 0.005f * u)
    drawLine(Ink.line, Offset(0f, back * u), Offset(st.w, back * u), strokeWidth = pen.lw)
    drawRect(Color(0xFF5E6878), Offset(0f, (SKY_TOP - 0.01f) * u), Size(st.w, 0.03f * u))
    drawLine(Color(0xFFC3CCD6), Offset(0f, (SKY_TOP + 0.016f) * u), Offset(st.w, (SKY_TOP + 0.016f) * u), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(0f, (SKY_TOP + 0.02f) * u), Offset(st.w, (SKY_TOP + 0.02f) * u), strokeWidth = pen.lw)
    drawBase(st, pen, Color(0xFFC3CCD6), Color(0xFF4E5866))
    inScene(st) { drawPoints(ss.baseRivets, PointMode.Points, Color(0xFF8B96A5), strokeWidth = 0.007f * u, cap = StrokeCap.Round) }
}

private fun DrawScope.leds(st: Stage, pen: Pen) {
    val u = st.u
    val cols = listOf(Color(0xFFFF5A6E), Color(0xFF5CFF9A), Color(0xFF6FD8FF))
    for (g in cols.indices) {
        val pts = ArrayList<Offset>(8)
        for ((k, xb) in SPACE_RIBS.withIndex()) {
            if (!st.sees(xb - 0.05f, xb + 0.05f)) continue
            pts.add(st.o(xb, 0.52f + g * 0.045f + (k % 2) * 0.02f))
        }
        val a = 0.3f + 0.7f * (0.5f + 0.5f * sin(pen.t * (2.1f + g * 1.3f) + g * 2f))
        drawPoints(pts, PointMode.Points, cols[g], strokeWidth = 0.024f * u, cap = StrokeCap.Round, alpha = 0.25f * a)
        drawPoints(pts, PointMode.Points, cols[g], strokeWidth = 0.01f * u, cap = StrokeCap.Round, alpha = a)
    }
}

/** The long window: black-blue space with stars, the Sun and the planets drifting by with parallax. */
private fun DrawScope.spaceWindow(st: Stage, pen: Pen) {
    if (!st.sees(WIN_X0, WIN_X1)) return
    val u = st.u
    val t = pen.t
    val r = st.rect(WIN_X0, WIN_Y0, WIN_X1, WIN_Y1)
    val corner = CornerRadius(0.045f * u)
    val clip = Path().apply { addRoundRect(RoundRect(r, corner)) }
    clipPath(clip) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF04071A), Color(0xFF0F1B45)), startY = r.top, endY = r.bottom), r.topLeft, r.size)
        val band = Path().apply {
            moveTo(st.px(0f, 0.1f), r.bottom)
            lineTo(st.px(0.6f, 0.1f), r.bottom)
            lineTo(st.px(3.6f, 0.1f), r.top)
            lineTo(st.px(3.0f, 0.1f), r.top)
            close()
        }
        drawPath(band, Brush.horizontalGradient(listOf(Color(0x00B79CFF), Color(0x33B79CFF), Color(0x228FD8FF), Color(0x00B79CFF)), startX = st.px(0.5f, 0.1f), endX = st.px(3.4f, 0.1f)))
        val span = (WIN_X1 - WIN_X0) + 1.2f
        for (g in 0 until 2) {
            val pts = ArrayList<Offset>(24)
            for (i in g until 48 step 2) {
                val lx = WIN_X0 - 0.2f + hash01(i, 601) * span
                pts.add(Offset(st.px(lx, 0.12f), mix(r.top, r.bottom, hash01(i, 602))))
            }
            drawPoints(pts, PointMode.Points, Color(0xFFFFF7DA), strokeWidth = (0.004f + g * 0.002f) * u, cap = StrokeCap.Round, alpha = 0.6f + 0.4f * sin(t * (1.3f + g) + g * 2f))
        }
        planets(st, pen)
        // The window sits in the thick curved wall: its left and lower reveals show.
        drawRect(Brush.horizontalGradient(listOf(Color(0xFFAEB9C6), Color(0xFF8391A1)), startX = r.left, endX = r.left + 0.022f * u), r.topLeft, Size(0.022f * u, r.height))
        drawRect(Brush.verticalGradient(listOf(Color(0xFF8391A1), Color(0xFFC7D0DA)), startY = r.bottom - 0.02f * u, endY = r.bottom), Offset(r.left, r.bottom - 0.02f * u), Size(r.width, 0.02f * u))
        for (k in 0 until 2) {
            val x0 = r.left + r.width * (0.18f + k * 0.45f)
            val glare = Path().apply { poly(x0, r.top, x0 + 0.05f * u, r.top, x0 - 0.05f * u, r.bottom, x0 - 0.1f * u, r.bottom) }
            drawPath(glare, Color.White, alpha = 0.06f)
        }
    }
    drawRoundRect(Color(0xFF97A3B1), Offset(r.left - 0.008f * u, r.top - 0.008f * u), Size(r.width + 0.016f * u, r.height + 0.016f * u), CornerRadius(0.05f * u), style = Stroke(0.016f * u))
    drawRoundRect(Ink.line, Offset(r.left - 0.016f * u, r.top - 0.016f * u), Size(r.width + 0.032f * u, r.height + 0.032f * u), CornerRadius(0.058f * u), style = pen.stroke)
    drawRoundRect(Ink.line, r.topLeft, r.size, corner, style = pen.thin)
}

/** A planet with its night side turned away from the Sun (on the left). */
private fun DrawScope.planet(c: Offset, r: Float, color: Color, pen: Pen, detail: DrawScope.() -> Unit = {}) {
    drawCircle(color, r, c)
    clipPath(Path().apply { addOval(Rect(c, r)) }) {
        detail()
        drawCircle(Color(0xFF02030C), r * 1.1f, Offset(c.x + r * 0.7f, c.y + r * 0.12f), alpha = 0.5f)
    }
    drawCircle(Ink.line, r, c, style = pen.thin)
}

private fun DrawScope.planets(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val p = 0.35f
    val lmin = WIN_X0 - (1f - p) * min(st.camMax, WIN_X0)
    val lmax = WIN_X1 - (1f - p) * max(0f, min(st.camMax, WIN_X1 - st.vw))
    fun px(f: Float, i: Int) = st.px(mix(lmin, lmax, f) + sin(t * 0.02f + i) * 0.008f, p)
    fun py(v: Float, i: Int) = (v + sin(t * 0.05f + i * 1.3f) * 0.004f) * u

    // The Sun, far left.
    val sun = Offset(px(0f, 0), py(0.22f, 0))
    val sr = 0.075f * u
    drawCircle(safeRadialGradient(listOf(Color(0x88FFE27A), Color(0x00FFB23D)), center = sun, radius = sr * 3f), sr * 3f, sun)
    drawCircle(safeRadialGradient(listOf(Color(0xFFFFFBE0), Color(0xFFFFD24A), Color(0xFFFFA62B)), center = sun, radius = sr), sr, sun)
    for (k in 0 until 8) {
        val a = k * 0.785f + t * 0.1f
        val l = sr * (1.25f + 0.1f * sin(t * 2f + k))
        drawLine(Color(0xFFFFE27A), Offset(sun.x + sin(a) * sr * 1.05f, sun.y + kotlin.math.cos(a) * sr * 1.05f), Offset(sun.x + sin(a) * l, sun.y + kotlin.math.cos(a) * l), strokeWidth = 0.006f * u, cap = StrokeCap.Round, alpha = 0.8f)
    }

    // Mercury: small and grey with craters.
    val me = Offset(px(0.12f, 1), py(0.19f, 1))
    planet(me, 0.014f * u, Color(0xFFA9A49C), pen) {
        drawCircle(Color(0xFF8A857E), 0.004f * u, Offset(me.x - 0.004f * u, me.y - 0.003f * u))
        drawCircle(Color(0xFF8A857E), 0.003f * u, Offset(me.x + 0.003f * u, me.y + 0.005f * u))
    }
    // Venus: pale yellow veils.
    val ve = Offset(px(0.22f, 2), py(0.3f, 2))
    val vr = 0.026f * u
    planet(ve, vr, Color(0xFFEDD9A3), pen) {
        drawRect(Color(0xFFF7E8C0), Offset(ve.x - vr, ve.y - vr * 0.5f), Size(vr * 2f, vr * 0.3f))
        drawRect(Color(0xFFDCC48A), Offset(ve.x - vr, ve.y + vr * 0.2f), Size(vr * 2f, vr * 0.25f))
    }
    // Earth: big and blue, low in the window, with continents and drifting clouds.
    val ea = Offset(px(0.4f, 3), py(0.56f, 3))
    val er = 0.25f * u
    drawCircle(Color(0xFF8FD8FF), er * 1.06f, ea, alpha = 0.25f)
    planet(ea, er, Color(0xFF2F7BD8), pen) {
        drawPath(crownPath(ea.x - er * 0.45f, ea.y - er * 0.78f, er * 0.28f, er * 0.12f, 611), Color(0xFF5DB35B))
        drawPath(crownPath(ea.x + er * 0.25f, ea.y - er * 0.86f, er * 0.2f, er * 0.08f, 612), Color(0xFF6FBF5E))
        drawPath(crownPath(ea.x - er * 0.05f, ea.y - er * 0.68f, er * 0.12f, er * 0.1f, 613), Color(0xFFD9C27A))
        val drift = wrap(t * 0.006f * u, er * 2f)
        for (k in 0 until 5) {
            val cx = ea.x - er + wrap(k * er * 0.45f + drift, er * 2.2f)
            val cy = ea.y - er * (0.72f + 0.12f * (k % 3))
            drawOval(Color.White, Offset(cx - er * 0.14f, cy - er * 0.025f), Size(er * 0.28f, er * 0.05f), alpha = 0.85f)
        }
        drawOval(Color.White, Offset(ea.x - er * 0.3f, ea.y - er * 1.02f), Size(er * 0.6f, er * 0.1f), alpha = 0.9f)
    }
    drawCircle(Color(0xFFBFEFFF), er, ea, alpha = 0.6f, style = Stroke(0.006f * u))
    // Mars: rusty red with a white polar cap.
    val ma = Offset(px(0.53f, 4), py(0.2f, 4))
    val mr = 0.02f * u
    planet(ma, mr, Color(0xFFD0603C), pen) {
        drawOval(Color(0xFFA9442B), Offset(ma.x - mr * 0.6f, ma.y), Size(mr * 0.9f, mr * 0.5f))
        drawOval(Color.White, Offset(ma.x - mr * 0.4f, ma.y - mr * 1.05f), Size(mr * 0.8f, mr * 0.35f))
    }
    // Jupiter: banded, with the great red spot.
    val ju = Offset(px(0.65f, 5), py(0.27f, 5))
    val jr = 0.07f * u
    planet(ju, jr, Color(0xFFE3C79B), pen) {
        val bands = floatArrayOf(-0.72f, -0.4f, -0.08f, 0.26f, 0.58f)
        for ((k, b) in bands.withIndex()) {
            drawRect(if (k % 2 == 0) Color(0xFFB98A5E) else Color(0xFFF3E3C6), Offset(ju.x - jr, ju.y + b * jr), Size(jr * 2f, jr * 0.16f))
        }
        drawOval(Color(0xFFC8553D), Offset(ju.x - jr * 0.05f, ju.y + jr * 0.28f), Size(jr * 0.36f, jr * 0.2f))
    }
    // Saturn: pale gold with its rings.
    val sa = Offset(px(0.79f, 6), py(0.24f, 6))
    val srr = 0.05f * u
    val ring = Color(0xFFD9C58F)
    rotate(-14f, sa) {
        drawArc(ring, 180f, 180f, false, Offset(sa.x - srr * 2.1f, sa.y - srr * 0.45f), Size(srr * 4.2f, srr * 0.9f), style = Stroke(srr * 0.22f))
        drawArc(Ink.line, 180f, 180f, false, Offset(sa.x - srr * 2.25f, sa.y - srr * 0.56f), Size(srr * 4.5f, srr * 1.12f), style = pen.thin)
    }
    planet(sa, srr, Color(0xFFE8D29A), pen) {
        drawRect(Color(0xFFD1B67A), Offset(sa.x - srr, sa.y - srr * 0.3f), Size(srr * 2f, srr * 0.18f))
        drawRect(Color(0xFFF1E2B8), Offset(sa.x - srr, sa.y + srr * 0.15f), Size(srr * 2f, srr * 0.15f))
    }
    rotate(-14f, sa) {
        drawArc(ring, 0f, 180f, false, Offset(sa.x - srr * 2.1f, sa.y - srr * 0.45f), Size(srr * 4.2f, srr * 0.9f), style = Stroke(srr * 0.22f))
        drawArc(Ink.line, 0f, 180f, false, Offset(sa.x - srr * 2.25f, sa.y - srr * 0.56f), Size(srr * 4.5f, srr * 1.12f), style = pen.thin)
    }
    // Uranus: pale cyan, with a faint upright ring.
    val ur = Offset(px(0.89f, 7), py(0.19f, 7))
    val urr = 0.032f * u
    planet(ur, urr, Color(0xFFA6E4EA), pen)
    rotate(78f, ur) { drawOval(Color(0xFFD7F6F8), Offset(ur.x - urr * 1.6f, ur.y - urr * 0.35f), Size(urr * 3.2f, urr * 0.7f), alpha = 0.6f, style = Stroke(0.003f * u)) }
    // Neptune: deep blue with a dark storm.
    val ne = Offset(px(0.98f, 8), py(0.31f, 8))
    val nr = 0.03f * u
    planet(ne, nr, Color(0xFF3F5FD8), pen) {
        drawRect(Color(0xFF5A7BE6), Offset(ne.x - nr, ne.y - nr * 0.45f), Size(nr * 2f, nr * 0.18f))
        drawOval(Color(0xFF223A99), Offset(ne.x - nr * 0.5f, ne.y + nr * 0.05f), Size(nr * 0.5f, nr * 0.3f))
    }
}
