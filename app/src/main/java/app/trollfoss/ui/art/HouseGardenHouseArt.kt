package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.GardenLayout
import app.trollfoss.domain.Weather
import kotlin.math.sin

/*
 * Hagen, the garden of Storhuset: the BACK OF THE BIG HOUSE as the background draws it. An ochre wooden
 * house with white corner boards, sash windows that glow at night, a round bay tower, the balcony with its
 * red slide curving down to the pond, a drainpipe over the rain barrel and string lights. The big shapes are
 * built once per scale (see [gaHouseGeo]) in scene pixels. Helpers start with `gh` so they cannot clash.
 */

/** Colours of the house and the things that belong to it. */
internal object GhC {
    val siding = Color(0xFFEBC26E)
    val sidingShade = Color(0xFFD7A74F)
    val trim = Color(0xFFFBF7EE)
    val trimShade = Color(0xFFDAD3C3)
    val roof = Color(0xFF59627F)
    val roofLight = Color(0xFF727C9C)
    val roofDark = Color(0xFF444C68)
    val stone = Color(0xFFA7A5B3)
    val stoneDark = Color(0xFF7F7D92)
    val wood = Color(0xFFD3A06B)
    val woodDark = Color(0xFFA9774A)
    val glass = Color(0xFF9FD3EE)
    val glassDeep = Color(0xFF5F9ECB)
    val warm = Color(0xFFFFD66B)
    val red = Color(0xFFD2443A)
    val redDark = Color(0xFF9F2E2A)
    val yellow = Color(0xFFFFC83D)
    val curtain = Color(0xFFF4E6D0)
    val geranium = Color(0xFFE8473F)
    val metal = Color(0xFF8C93A8)
}

/** Mist, rain and night take their share of a colour; [k] is how much of the night, [fog] how much of the grey. */
internal fun Color.ga(pen: Pen, k: Float = 0.55f, fog: Float = 0.3f): Color {
    val oc = overcast(pen)
    val c = if (oc > 0f) lerp(this, Color(0xFFB3BCCB), oc * fog) else this
    return c.atNight(pen.night, k)
}

/** A colour of the ground or of roofs in snow: it fades toward snow. */
internal fun Color.gaSnow(pen: Pen, amount: Float = 0.6f): Color =
    if (pen.weather == Weather.SNOW) lerp(this, Pal.snow, amount) else this

private const val EAVE = -0.22f
private const val FOUND_TOP = 0.735f
private const val WALL_BASE = 0.78f
private const val BELT = 0.268f

/** Geometry of the house in scene pixels. */
private class GhGeo(
    val wall: Path, val boards: Path, val belt: Path, val corners: Path, val foundation: Path, val joints: Path,
    val roof: Path, val roofLines: Path, val fascia: Path, val dormer: Path, val dormerRoof: Path, val chimney: Path,
    val tower: Path, val towerLines: Path, val towerRoof: Path, val towerRoofLines: Path, val corbel: Path,
    val deck: Path, val balusters: Path, val rail: Path, val posts: Path, val brackets: Path,
    val drain: Path, val drainClips: Path, val chute: Path, val chuteSupports: Path, val chuteBraces: Path,
    val bulbs: List<Offset>, val bulbWire: Path,
)

private val ghGeo = Memo { u -> ghBuild(u) }

/** The slide's path: where the chute is at [t] from the balcony (0) to the pond (1), in scene units. */
internal fun ghChute(t: Float): Offset {
    val x0 = GardenLayout.SLIDE_X0
    val y0 = GardenLayout.SLIDE_Y0
    val x3 = GardenLayout.SLIDE_X1
    val y3 = GardenLayout.SLIDE_Y1
    val x1 = x0 + 0.28f
    val y1 = y0 + 0.02f
    val x2 = x3 - 0.55f
    val y2 = y3 - 0.02f
    val s = 1f - t
    val x = s * s * s * x0 + 3f * s * s * t * x1 + 3f * s * t * t * x2 + t * t * t * x3
    val y = s * s * s * y0 + 3f * s * s * t * y1 + 3f * s * t * t * y2 + t * t * t * y3
    return Offset(x, y)
}

private fun ghBuild(u: Float): GhGeo {
    val x0 = GardenLayout.HOUSE_X0
    val x1 = GardenLayout.HOUSE_X1
    val wall = Path().apply { addRect(Rect(x0 * u, EAVE * u, x1 * u, FOUND_TOP * u)) }
    val boards = Path()
    var y = EAVE + 0.036f
    while (y < FOUND_TOP) {
        boards.moveTo(x0 * u, y * u)
        boards.lineTo(x1 * u, y * u)
        y += 0.036f
    }
    val belt = Path().apply {
        addRect(Rect(x0 * u, (BELT - 0.01f) * u, x1 * u, (BELT + 0.026f) * u))
        addRect(Rect(x0 * u, (EAVE - 0.003f) * u, x1 * u, (EAVE + 0.018f) * u))
    }
    val corners = Path().apply {
        addRect(Rect(x0 * u, EAVE * u, (x0 + 0.05f) * u, FOUND_TOP * u))
        addRect(Rect((x1 - 0.05f) * u, EAVE * u, x1 * u, FOUND_TOP * u))
    }
    val foundation = Path().apply { addRect(Rect(x0 * u, FOUND_TOP * u, x1 * u, WALL_BASE * u)) }
    val joints = Path()
    var jx = x0
    var jr = 0
    while (jx < x1) {
        val w = 0.11f + 0.07f * hash01(jr, 701)
        joints.moveTo((jx + w) * u, FOUND_TOP * u)
        joints.lineTo((jx + w) * u, (FOUND_TOP + 0.022f * (1 + jr % 2)) * u)
        joints.moveTo((jx + w * 0.4f) * u, (FOUND_TOP + 0.022f) * u)
        joints.lineTo((jx + w * 0.4f) * u, WALL_BASE * u)
        jx += w
        jr++
    }

    // The roof above the eave: slate, with the dormer and the chimney (high up, so tablets see them).
    val roof = Path().apply { addRect(Rect((x0 - 0.1f) * u, (EAVE - 0.34f) * u, (x1 + 0.1f) * u, (EAVE + 0.012f) * u)) }
    val roofLines = Path()
    var ry = EAVE - 0.3f
    var row = 0
    while (ry < EAVE) {
        roofLines.moveTo((x0 - 0.1f) * u, ry * u)
        roofLines.lineTo((x1 + 0.1f) * u, ry * u)
        var rx = x0 - 0.1f + (row % 2) * 0.06f
        while (rx < x1 + 0.1f) {
            roofLines.moveTo(rx * u, ry * u)
            roofLines.lineTo(rx * u, (ry + 0.04f) * u)
            rx += 0.12f
        }
        ry += 0.04f
        row++
    }
    val fascia = Path().apply { addRect(Rect((x0 - 0.1f) * u, (EAVE - 0.006f) * u, (x1 + 0.1f) * u, (EAVE + 0.016f) * u)) }
    val dormer = Path().apply {
        moveTo(1.3f * u, (EAVE - 0.01f) * u)
        lineTo(1.3f * u, (EAVE - 0.14f) * u)
        lineTo(1.6f * u, (EAVE - 0.14f) * u)
        lineTo(1.6f * u, (EAVE - 0.01f) * u)
        close()
    }
    val dormerRoof = Path().apply {
        moveTo(1.26f * u, (EAVE - 0.13f) * u)
        lineTo(1.45f * u, (EAVE - 0.25f) * u)
        lineTo(1.64f * u, (EAVE - 0.13f) * u)
        close()
    }
    val chimney = Path().apply { addRect(Rect(2.55f * u, (EAVE - 0.5f) * u, 2.7f * u, (EAVE - 0.18f) * u)) }

    // The round bay tower, from a corbel under the balcony floor up past the eave.
    val tcx = 0.55f
    val tr = 0.27f
    val tower = Path().apply { addRect(Rect((tcx - tr) * u, (EAVE - 0.08f) * u, (tcx + tr) * u, 0.285f * u)) }
    val towerLines = Path()
    for (k in 1..5) {
        val fx = tcx - tr + 2f * tr * (k / 6f)
        towerLines.moveTo(fx * u, (EAVE - 0.08f) * u)
        towerLines.lineTo(fx * u, 0.285f * u)
    }
    val towerRoof = Path().apply {
        moveTo((tcx - tr - 0.05f) * u, (EAVE - 0.07f) * u)
        lineTo(tcx * u, (EAVE - 0.5f) * u)
        lineTo((tcx + tr + 0.05f) * u, (EAVE - 0.07f) * u)
        close()
    }
    val towerRoofLines = Path()
    for (k in 1..5) {
        val f = k / 6f
        towerRoofLines.moveTo((tcx - (tr + 0.05f) * (1f - f)) * u, (EAVE - 0.07f - 0.43f * f) * u)
        towerRoofLines.lineTo((tcx + (tr + 0.05f) * (1f - f)) * u, (EAVE - 0.07f - 0.43f * f) * u)
    }
    val corbel = Path().apply {
        moveTo((tcx - tr) * u, 0.285f * u)
        lineTo((tcx + tr) * u, 0.285f * u)
        lineTo((tcx + tr * 0.55f) * u, 0.36f * u)
        lineTo((tcx - tr * 0.55f) * u, 0.36f * u)
        close()
    }

    // The balcony: a deck, a rail with balusters (open at the right end, where the slide starts) and brackets.
    val bx0 = 2.1f
    val bx1 = GardenLayout.SLIDE_X0 + 0.04f
    val deck = Path().apply { addRect(Rect(bx0 * u, 0.268f * u, bx1 * u, 0.3f * u)) }
    val balusters = Path()
    var bxx = bx0 + 0.05f
    while (bxx < bx1 - 0.2f) {
        balusters.moveTo(bxx * u, 0.178f * u)
        balusters.lineTo(bxx * u, 0.268f * u)
        bxx += 0.045f
    }
    val rail = Path().apply { addRect(Rect(bx0 * u, 0.16f * u, (bx1 - 0.14f) * u, 0.18f * u)) }
    val posts = Path().apply {
        addRect(Rect((bx0 - 0.005f) * u, 0.145f * u, (bx0 + 0.03f) * u, 0.268f * u))
        addRect(Rect((bx1 - 0.19f) * u, 0.145f * u, (bx1 - 0.155f) * u, 0.268f * u))
    }
    val brackets = Path()
    for (bk in floatArrayOf(2.2f, 2.65f, 3.1f)) {
        brackets.moveTo(bk * u, 0.3f * u)
        brackets.lineTo((bk + 0.1f) * u, 0.3f * u)
        brackets.lineTo(bk * u, 0.4f * u)
        brackets.close()
    }

    // The drainpipe over the rain barrel.
    val dpx = 1.62f
    val drain = Path().apply {
        addRect(Rect((dpx - 0.012f) * u, (EAVE + 0.012f) * u, (dpx + 0.012f) * u, (FOUND_TOP - 0.02f) * u))
        moveTo((dpx - 0.012f) * u, (FOUND_TOP - 0.02f) * u)
        lineTo((dpx - 0.04f) * u, FOUND_TOP * u)
        lineTo((dpx - 0.012f) * u, (FOUND_TOP + 0.03f) * u)
    }
    val clips = Path()
    for (k in 0 until 5) {
        val cy = EAVE + 0.1f + k * 0.17f
        clips.addRect(Rect((dpx - 0.02f) * u, cy * u, (dpx + 0.02f) * u, (cy + 0.012f) * u))
    }

    // The slide, as a ribbon along its curve, and the posts that hold it up.
    val chute = Path()
    val steps = 28
    for (k in 0..steps) {
        val p = ghChute(k / steps.toFloat())
        if (k == 0) chute.moveTo(p.x * u, p.y * u) else chute.lineTo(p.x * u, p.y * u)
    }
    val supports = Path()
    val braces = Path()
    for ((i, t) in floatArrayOf(0.28f, 0.5f, 0.72f, 0.92f).withIndex()) {
        val p = ghChute(t)
        val groundY = 0.84f - 0.02f * (i % 2)
        supports.moveTo(p.x * u, (p.y + 0.03f) * u)
        supports.lineTo((p.x + 0.0f) * u, groundY * u)
        if (i > 0) {
            val q = ghChute(floatArrayOf(0.28f, 0.5f, 0.72f, 0.92f)[i - 1])
            braces.moveTo(q.x * u, (q.y + 0.2f).coerceAtMost(0.82f) * u)
            braces.lineTo(p.x * u, (p.y + 0.12f).coerceAtMost(0.82f) * u)
        }
    }

    // String lights from the balcony rail to the drainpipe and on to the door, little bulbs on a sagging wire.
    val bulbs = ArrayList<Offset>(16)
    val wire = Path()
    val segs = listOf(floatArrayOf(2.1f, 0.2f, 1.62f, 0.12f), floatArrayOf(1.62f, 0.12f, 1.3f, 0.3f), floatArrayOf(2.1f, 0.2f, 2.55f, 0.07f))
    for (sg in segs) {
        val n = 6
        for (k in 0..n) {
            val f = k / n.toFloat()
            val x = sg[0] + (sg[2] - sg[0]) * f
            val yy = sg[1] + (sg[3] - sg[1]) * f + sin(f * 3.1416f) * 0.035f
            if (k == 0) wire.moveTo(x * u, yy * u) else wire.lineTo(x * u, yy * u)
            if (k in 1 until n) bulbs.add(Offset(x * u, (yy + 0.012f) * u))
        }
    }
    return GhGeo(
        wall, boards, belt, corners, foundation, joints, roof, roofLines, fascia, dormer, dormerRoof, chimney,
        tower, towerLines, towerRoof, towerRoofLines, corbel, deck, balusters, rail, posts, brackets,
        drain, clips, chute, supports, braces, bulbs, wire,
    )
}

/** A sash window of the house in scene units: white frame, glass that glows at night, bars, a curtain, a sill. */
private fun DrawScope.ghWindow(cx: Float, top: Float, w: Float, h: Float, u: Float, pen: Pen, lit: Float, flowers: Boolean) {
    val l = (cx - w / 2f) * u
    val t = top * u
    val ww = w * u
    val hh = h * u
    val night = pen.night
    val glass = lerp(GhC.glass.ga(pen, 0.45f, 0.4f), GhC.warm, lit)
    val deep = lerp(GhC.glassDeep.ga(pen, 0.45f, 0.4f), Color(0xFFFFB84A), lit)
    if (lit > 0.02f) drawCircle(safeRadialGradient(listOf(GhC.warm.copy(alpha = 0.55f * lit), GhC.warm.copy(alpha = 0f)), center = Offset(l + ww / 2f, t + hh / 2f), radius = w * u * 1.5f), w * u * 1.5f, Offset(l + ww / 2f, t + hh / 2f))
    // The frame, a touch bigger than the glass.
    val pad = 0.014f * u
    drawRoundRect(GhC.trim.ga(pen), Offset(l - pad, t - pad), Size(ww + 2 * pad, hh + 2 * pad), androidx.compose.ui.geometry.CornerRadius(0.006f * u))
    drawRoundRect(Ink.line, Offset(l - pad, t - pad), Size(ww + 2 * pad, hh + 2 * pad), androidx.compose.ui.geometry.CornerRadius(0.006f * u), style = pen.stroke)
    drawRect(Brush.verticalGradient(0f to glass, 1f to deep, startY = t, endY = t + hh), Offset(l, t), Size(ww, hh))
    // A curtain half, drawn to one side.
    val cw = ww * 0.3f
    val curtain = Path().apply {
        moveTo(l, t)
        lineTo(l + cw, t)
        quadraticTo(l + cw * 0.7f, t + hh * 0.5f, l + cw * 1.1f, t + hh * 0.85f)
        lineTo(l, t + hh * 0.85f)
        close()
    }
    drawPath(curtain, GhC.curtain.ga(pen, 0.4f).copy(alpha = 0.95f))
    drawPath(curtain, Ink.line, alpha = 0.55f, style = pen.thin)
    // The glass shine: a slanted white stripe by day.
    if (night < 0.5f) {
        val s = Path().apply {
            moveTo(l + ww * 0.55f, t)
            lineTo(l + ww * 0.72f, t)
            lineTo(l + ww * 0.42f, t + hh)
            lineTo(l + ww * 0.25f, t + hh)
            close()
        }
        clipPath(Path().apply { addRect(Rect(l, t, l + ww, t + hh)) }) { drawPath(s, Color.White, alpha = 0.22f * (1f - night)) }
    }
    drawLine(GhC.trim.ga(pen), Offset(l + ww / 2f, t), Offset(l + ww / 2f, t + hh), strokeWidth = pen.lw * 1.6f)
    drawLine(GhC.trim.ga(pen), Offset(l, t + hh * 0.5f), Offset(l + ww, t + hh * 0.5f), strokeWidth = pen.lw * 1.6f)
    drawRect(Ink.line, Offset(l, t), Size(ww, hh), style = pen.thin)
    // Sill.
    drawRoundRect(GhC.trim.ga(pen), Offset(l - pad * 1.6f, t + hh + pad), Size(ww + pad * 3.2f, 0.016f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u))
    drawRoundRect(Ink.line, Offset(l - pad * 1.6f, t + hh + pad), Size(ww + pad * 3.2f, 0.016f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
    if (flowers) {
        val by = t + hh + pad + 0.016f * u
        drawRoundRect(GhC.woodDark.ga(pen), Offset(l - pad, by), Size(ww + 2 * pad, 0.03f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u))
        drawRoundRect(Ink.line, Offset(l - pad, by), Size(ww + 2 * pad, 0.03f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
        for (k in 0 until 6) {
            val fx = l + ww * (0.08f + k * 0.17f)
            drawLine(Color(0xFF3F8F4A).ga(pen), Offset(fx, by), Offset(fx, by - 0.018f * u), strokeWidth = pen.lw)
            val col = if (k % 3 == 1) Color(0xFFFF8FB1) else GhC.geranium
            drawCircle(col.ga(pen, 0.4f), 0.0105f * u, Offset(fx, by - 0.024f * u))
            drawCircle(Ink.line, 0.0105f * u, Offset(fx, by - 0.024f * u), alpha = 0.6f, style = pen.thin)
        }
    }
}

/** The back of the house, in front of the far scenery and behind everything the child can touch. */
internal fun DrawScope.gardenHouse(st: Stage, pen: Pen) {
    if (!st.sees(GardenLayout.HOUSE_X0, GardenLayout.HOUSE_X1 + 0.1f)) return
    val u = st.u
    val g = ghGeo.of(u)
    val n = pen.night
    val lit = ramp((n - 0.3f) / 0.4f)
    inScene(st) {
        // Roof first (it sits behind the eave), then the wall, trims, foundation.
        drawPath(g.roof, GhC.roof.ga(pen, 0.6f).gaSnow(pen, 0.2f))
        drawPath(g.roofLines, GhC.roofDark.ga(pen, 0.6f), alpha = 0.55f, style = Stroke(pen.lw * 0.8f))
        if (pen.weather == Weather.SNOW) {
            drawPath(Path().apply { addRect(Rect((GardenLayout.HOUSE_X0 - 0.1f) * u, (EAVE - 0.34f) * u, (GardenLayout.HOUSE_X1 + 0.1f) * u, (EAVE - 0.3f) * u)) }, Pal.snow)
        }
        // Chimney with smoke.
        drawPath(g.chimney, GhC.stoneDark.ga(pen))
        drawPath(g.chimney, Ink.line, style = pen.stroke)
        drawPath(g.fascia, GhC.trim.ga(pen))
        drawPath(g.fascia, Ink.line, style = pen.thin)
        drawPath(g.wall, GhC.siding.ga(pen, 0.5f))
        drawPath(g.boards, GhC.sidingShade.ga(pen, 0.5f), alpha = 0.7f, style = Stroke(pen.lw * 0.9f))
        // Soft light from the left on the wall: a gradient strip at the corner.
        drawPath(g.corners, GhC.trim.ga(pen))
        drawPath(g.corners, Ink.line, style = pen.thin)
        drawPath(g.belt, GhC.trim.ga(pen))
        drawPath(g.belt, Ink.line, style = pen.thin)
        drawPath(g.foundation, GhC.stone.ga(pen, 0.5f))
        drawPath(g.joints, GhC.stoneDark.ga(pen, 0.5f), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
        drawPath(g.foundation, Ink.line, style = pen.thin)
        drawPath(g.wall, Ink.line, alpha = 0.6f, style = pen.stroke)
        // The dormer in the roof.
        drawPath(g.dormer, GhC.siding.ga(pen, 0.5f))
        drawPath(g.dormer, Ink.line, style = pen.stroke)
        drawPath(g.dormerRoof, GhC.roofLight.ga(pen, 0.6f))
        drawPath(g.dormerRoof, Ink.line, style = pen.stroke)
    }
    // Windows, drawn only when they are on screen.
    fun win(cx: Float, top: Float, w: Float, h: Float, flowers: Boolean) {
        if (st.sees(cx - w, cx + w)) inScene(st) { ghWindow(cx, top, w, h, u, pen, lit, flowers) }
    }
    win(1.3f, EAVE - 0.115f, 0.18f, 0.09f, false)
    win(-0.05f, 0.03f, 0.22f, 0.19f, false)
    win(1.25f, 0.03f, 0.22f, 0.19f, false)
    win(1.78f, 0.03f, 0.22f, 0.19f, false)
    win(2.0f, 0.4f, 0.28f, 0.27f, true)
    win(0.62f, 0.4f, 0.28f, 0.27f, true)
    win(3.0f, 0.4f, 0.22f, 0.25f, true)
    // The bay tower.
    if (st.sees(0.2f, 0.9f)) inScene(st) {
        val body = GhC.siding.ga(pen, 0.5f)
        drawPath(g.towerRoof, GhC.roofLight.ga(pen, 0.6f).gaSnow(pen, 0.25f))
        drawPath(g.towerRoofLines, GhC.roofDark.ga(pen, 0.6f), alpha = 0.5f, style = Stroke(pen.lw * 0.8f))
        drawPath(g.towerRoof, Ink.line, style = pen.stroke)
        drawLine(GhC.yellow.ga(pen), Offset(0.55f * u, (EAVE - 0.5f) * u), Offset(0.55f * u, (EAVE - 0.58f) * u), strokeWidth = pen.lw * 1.4f)
        drawPath(g.corbel, GhC.woodDark.ga(pen, 0.5f))
        drawPath(g.corbel, Ink.line, style = pen.stroke)
        drawPath(g.tower, Brush.horizontalGradient(0f to body.lighten(0.14f), 0.55f to body, 1f to body.darken(0.22f), startX = 0.28f * u, endX = 0.82f * u))
        drawPath(g.towerLines, GhC.sidingShade.ga(pen, 0.5f), alpha = 0.55f, style = Stroke(pen.lw * 0.8f))
        drawPath(g.tower, Ink.line, style = pen.stroke)
        ghWindow(0.55f, 0.0f, 0.18f, 0.19f, u, pen, lit, false)
        ghWindow(0.55f, EAVE + 0.03f, 0.14f, 0.1f, u, pen, lit, false)
    }
    // The balcony, its glass doors and the drainpipe.
    if (st.sees(1.5f, 3.4f)) inScene(st) {
        val deckC = GhC.wood.ga(pen, 0.5f)
        // The glass doors: two leaves with bars, curtains and a warm glow at night.
        val dl = 2.43f * u
        val dt = 0.02f * u
        val dw = 0.54f * u
        val dh = 0.248f * u
        val glass = lerp(GhC.glass.ga(pen, 0.45f, 0.4f), GhC.warm, lit)
        if (lit > 0.02f) drawCircle(safeRadialGradient(listOf(GhC.warm.copy(alpha = 0.5f * lit), GhC.warm.copy(alpha = 0f)), center = Offset(dl + dw / 2f, dt + dh / 2f), radius = 0.5f * u), 0.5f * u, Offset(dl + dw / 2f, dt + dh / 2f))
        drawRoundRect(GhC.trim.ga(pen), Offset(dl - 0.012f * u, dt - 0.012f * u), Size(dw + 0.024f * u, dh + 0.012f * u), androidx.compose.ui.geometry.CornerRadius(0.006f * u))
        drawRoundRect(Ink.line, Offset(dl - 0.012f * u, dt - 0.012f * u), Size(dw + 0.024f * u, dh + 0.012f * u), androidx.compose.ui.geometry.CornerRadius(0.006f * u), style = pen.stroke)
        drawRect(Brush.verticalGradient(0f to glass, 1f to lerp(GhC.glassDeep.ga(pen, 0.45f), Color(0xFFFFB84A), lit), startY = dt, endY = dt + dh), Offset(dl, dt), Size(dw, dh))
        for (k in 1..2) drawLine(GhC.trim.ga(pen), Offset(dl, dt + dh * k / 3f), Offset(dl + dw, dt + dh * k / 3f), strokeWidth = pen.lw * 1.3f)
        drawLine(GhC.trim.ga(pen), Offset(dl + dw / 2f, dt), Offset(dl + dw / 2f, dt + dh), strokeWidth = pen.lw * 1.8f)
        for (c in 0..1) {
            val cx0 = if (c == 0) dl else dl + dw
            val m = if (c == 0) 1f else -1f
            val cur = Path().apply {
                moveTo(cx0, dt)
                lineTo(cx0 + m * dw * 0.2f, dt)
                quadraticTo(cx0 + m * dw * 0.12f, dt + dh * 0.55f, cx0 + m * dw * 0.24f, dt + dh * 0.95f)
                lineTo(cx0, dt + dh * 0.95f)
                close()
            }
            drawPath(cur, GhC.curtain.ga(pen, 0.4f).copy(alpha = 0.95f))
            drawPath(cur, Ink.line, alpha = 0.5f, style = pen.thin)
        }
        drawRect(Ink.line, Offset(dl, dt), Size(dw, dh), style = pen.thin)

        drawPath(g.brackets, GhC.woodDark.ga(pen, 0.5f))
        drawPath(g.brackets, Ink.line, style = pen.thin)
        drawPath(g.deck, deckC)
        drawPath(g.deck, Ink.line, style = pen.stroke)
        drawPath(g.balusters, GhC.trim.ga(pen), style = Stroke(pen.lw * 1.5f, cap = StrokeCap.Round))
        drawPath(g.balusters, Ink.line, alpha = 0.35f, style = Stroke(pen.lw * 0.4f))
        drawPath(g.rail, GhC.trim.ga(pen))
        drawPath(g.rail, Ink.line, style = pen.stroke)
        drawPath(g.posts, GhC.trim.ga(pen))
        drawPath(g.posts, Ink.line, style = pen.stroke)
        // Geraniums in a box on the rail.
        for (k in 0 until 7) {
            val fx = 2.2f + k * 0.13f
            drawCircle(GhC.geranium.ga(pen, 0.4f), 0.014f * u, Offset(fx * u, 0.15f * u))
            drawCircle(Ink.line, 0.014f * u, Offset(fx * u, 0.15f * u), alpha = 0.55f, style = pen.thin)
        }
        // The drainpipe.
        drawPath(g.drain, GhC.metal.ga(pen))
        drawPath(g.drain, Ink.line, style = pen.thin)
        drawPath(g.drainClips, GhC.metal.darken(0.2f).ga(pen))
        drawPath(g.drainClips, Ink.line, style = pen.thin)
        // Smoke from the chimney: only drawn when the roof is on screen.
        if (EAVE - 0.5f > -0.45f) {
            for (k in 0 until 4) {
                val ph = wrap(pen.t * 0.18f + k / 4f, 1f)
                drawCircle(Color.White.ga(pen, 0.5f), (0.02f + ph * 0.045f) * u, Offset((2.625f + ph * 0.16f + sin(ph * 5f + k) * 0.02f) * u, (EAVE - 0.52f - ph * 0.25f) * u), alpha = 0.5f * (1f - ph))
            }
        }
    }
}

/** The slide from the balcony to the pond, with its posts and the string lights, and the zip line over the garden. */
internal fun DrawScope.gardenSlide(st: Stage, pen: Pen) {
    val u = st.u
    if (st.sees(GardenLayout.SLIDE_X0 - 0.1f, GardenLayout.SLIDE_X1 + 0.1f)) {
        val g = ghGeo.of(u)
        inScene(st) {
            val t = pen.t
            // String lights.
            drawPath(g.bulbWire, Ink.line, alpha = 0.7f, style = Stroke(pen.lw * 0.6f))
            val glow = 0.15f + 0.85f * pen.night
            for ((i, b) in g.bulbs.withIndex()) {
                val col = listOf(Color(0xFFFFE680), Color(0xFFFF9EC7), Color(0xFF9AE8FF), Color(0xFFB8FF9A))[i % 4]
                if (pen.night > 0.2f) drawCircle(safeRadialGradient(listOf(col.copy(alpha = 0.6f * glow), col.copy(alpha = 0f)), center = b, radius = 0.03f * u), 0.03f * u, b)
                drawCircle(Ink.line, 0.0055f * u, b)
                drawCircle(lerp(col.darken(0.2f), col.lighten(0.4f), glow * (0.7f + 0.3f * sin(t * 2f + i))), 0.0042f * u, b)
            }
            // Posts and braces first, then the chute ribbon on top.
            drawPath(g.chuteBraces, Ink.line, alpha = 0.9f, style = Stroke(pen.lw * 3.4f, cap = StrokeCap.Round))
            drawPath(g.chuteBraces, GhC.metal.ga(pen), style = Stroke(pen.lw * 1.7f, cap = StrokeCap.Round))
            drawPath(g.chuteSupports, Ink.line, style = Stroke(0.02f * u, cap = StrokeCap.Round))
            drawPath(g.chuteSupports, GhC.wood.ga(pen, 0.5f), style = Stroke(0.0125f * u, cap = StrokeCap.Round))
            drawPath(g.chute, Ink.line, style = Stroke(0.062f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(g.chute, GhC.red.ga(pen, 0.5f), style = Stroke(0.054f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(g.chute, GhC.redDark.ga(pen, 0.5f), style = Stroke(0.04f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(g.chute, GhC.yellow.ga(pen, 0.5f), style = Stroke(0.026f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // A glint that slides down the chute, again and again.
            val k = wrap(t * 0.35f, 1f)
            val p = ghChute(k)
            drawCircle(Color.White, 0.009f * u, Offset(p.x * u, (p.y - 0.004f) * u), alpha = 0.85f * (1f - pen.night * 0.4f))
        }
    }
    // The zip line from the mast on the treehouse deck to the pole by the pond.
    if (st.sees(GardenLayout.ZIP_X1 - 0.1f, GardenLayout.ZIP_X0 + 0.1f)) {
        val line = Path()
        val steps = 16
        for (k in 0..steps) {
            val q = k / steps.toFloat()
            val x = GardenLayout.ZIP_X0 + (GardenLayout.ZIP_X1 - GardenLayout.ZIP_X0) * q
            val y = GardenLayout.ZIP_Y0 + (GardenLayout.ZIP_Y1 - GardenLayout.ZIP_Y0) * q + sin(q * 3.1416f) * 0.025f
            if (k == 0) line.moveTo(st.x(x), y * u) else line.lineTo(st.x(x), y * u)
        }
        drawPath(line, Ink.line, style = Stroke(pen.lw * 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(line, GhC.metal.ga(pen, 0.4f).lighten(0.35f), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
