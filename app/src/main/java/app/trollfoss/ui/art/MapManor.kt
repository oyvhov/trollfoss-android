package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Storhuset on the village map: a big, slightly crooked timber villa on its own hill, with a round
 * observatory tower, a steep gable with a balcony, a glass winter garden, a pond, a greenhouse, a treehouse
 * in a big tree and a gate with a winding path down to the village lane. It is drawn in the same little
 * space as every other building (see Bx): the origin is the middle of the estate on the ground, one unit is
 * the landmark size, y points down and the depth runs up and to the right. Everything still is stamped
 * into the cached map layer by [manor]; what moves (smoke, flag, weathervane, telescope, glowing windows at
 * night, the ghost in the attic window, the robot butler's glint, a butterfly, the ripples) is [manorLive].
 *
 * The secret path from the cellar to Trollhola ([TunnelGeo]) is dotted over the ground, but only once all
 * five golden keys are found.
 */

/** The big house is drawn a little smaller than the unit suggests: the estate around it is wide. */
internal const val MANOR_SCALE = 0.9f

/** The estate is built around x = 0.15 in its own units; this shifts it so the plot's middle is the map spot. */
private const val DX = -0.15f

private const val MX0 = -1.45f
private const val MX1 = 0.55f
private const val WALL_H = 0.72f
private const val ROOF_H = 0.5f
private const val DEP = 0.7f

private const val TOWER_X = -1.45f
private const val TOWER_R = 0.3f
private const val TOWER_H = 1.28f

/** Where the dome sits on its drum (in the tower's own space). */
private const val DOME_Y = -1.62f
private const val LEAN = -2.2f

private const val BAY_X = -0.55f
private const val BAY_DY = 0.108f
private const val BAY_HW = 0.4f
private const val FWD = 0.26f
private const val ATTIC_Y = -1.0f

private const val GX0 = 0.55f
private const val GX1 = 1.5f
private const val GARDEN_EAVE = -0.42f
private const val GARDEN_RIDGE = -0.62f

private const val TREE_X = 0.2f

private const val GATE_LX = -1.3f
private const val GATE_LY = 0.33f
private const val CELLAR_LX = TOWER_X - 0.13f
private const val CELLAR_LY = 0.07f

private val WALL = Color(0xFFF3CF6E)
private val TRIM = Color(0xFFF7F3EC)
private val ROOF = Color(0xFF8E3B46)
private val DOME = Color(0xFF4FB39C)
private val DOOR = Color(0xFF2F6F8F)
private val STONE = Color(0xFFA29BAE)
private val BRASS = Color(0xFFE5B64A)
private val GLASS = Color(0xFFBFE6F5)
private val LEAF = Color(0xFF5DB04F)
private val WOOD = Color(0xFFC98A55)
private val DARK_GLASS = Color(0xFF3B3A63)

private const val DEG = 0.017453292f

// ------------------------------------------------------------------------------------- where things are

/** The unit of the estate in pixels (the landmark size of this place, scaled down a little). */
internal fun manorUnit(w: Float, h: Float): Float {
    val spot = mapSpot(PlaceId.MANOR_GROUND)
    val s = min(h * 0.15f, w * 0.0825f)
    val by = spot.y + 0.045f
    return s * (0.84f + 0.22f * ((by - 0.25f) / 0.6f).coerceIn(0f, 1f)) * MANOR_SCALE
}

/** A point of the estate (in its own units) as pixels on the map. */
internal fun manorPoint(w: Float, h: Float, lx: Float, ly: Float): Offset {
    val spot = mapSpot(PlaceId.MANOR_GROUND)
    val u = manorUnit(w, h)
    return Offset(spot.x * w + (lx + DX) * u, (spot.y + 0.045f) * h + ly * u)
}

/** The gate of the estate: where the winding path ends and the village lane begins. */
internal fun manorGate(w: Float, h: Float): Offset = manorPoint(w, h, GATE_LX, GATE_LY)

/** The cellar door at the foot of the tower, where the secret path starts. */
internal fun manorCellar(w: Float, h: Float): Offset = manorPoint(w, h, CELLAR_LX, CELLAR_LY)

/**
 * The lane from the gate to the cave road (road 6), as fractions of the map; the join is the nearest point
 * of the cave road to a spot a little left of the gate.
 */
internal fun manorLane(w: Float, h: Float, caveRoad: Poly): Poly {
    val g = manorGate(w, h)
    val j = caveRoad.at(caveRoad.nearestF(g.x - 0.06f * w, g.y + 0.004f * h))
    val dx = j.x - g.x
    val dy = j.y - g.y
    return smoothPoly(
        w, h,
        floatArrayOf(
            g.x / w, g.y / h,
            (g.x + dx * 0.2f) / w, (g.y + 0.011f * h) / h,
            (g.x + dx * 0.45f) / w, (g.y + dy * 0.4f - 0.007f * h) / h,
            (g.x + dx * 0.72f) / w, (g.y + dy * 0.75f + 0.008f * h) / h,
            j.x / w, j.y / h,
        ),
        8,
    )
}

// ------------------------------------------------------------------------------------- the secret path

/** The secret path from the cellar to the cave: the line and its dots (spaced evenly along it). */
internal class TunnelGeo(val line: Poly, val dots: List<Offset>)

internal fun buildTunnel(g: MapGeo): TunnelGeo {
    val w = g.w
    val h = g.h
    val u = manorUnit(w, h)
    val s = manorCellar(w, h)
    val cave = g.bases[PlaceId.LAB]!!
    val cu = g.S * (0.84f + 0.22f * ((cave.y / h - 0.25f) / 0.6f).coerceIn(0f, 1f))
    val e = Offset(cave.x + 0.02f * cu, cave.y + 0.17f * cu)
    val dx = e.x - s.x
    val dy = e.y - s.y
    val len = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
    val nx = -dy / len
    val ny = dx / len
    val line = smoothPoly(
        w, h,
        floatArrayOf(
            s.x / w, s.y / h,
            (s.x + dx * 0.33f + nx * 0.2f * u) / w, (s.y + dy * 0.33f + ny * 0.2f * u) / h,
            (s.x + dx * 0.68f - nx * 0.2f * u) / w, (s.y + dy * 0.68f - ny * 0.2f * u) / h,
            e.x / w, e.y / h,
        ),
        8,
    )
    val dots = ArrayList<Offset>()
    val step = 0.0115f * h
    var dd = 0f
    while (dd <= line.len) {
        dots.add(line.at(dd / line.len))
        dd += step
    }
    return TunnelGeo(line, dots)
}

/** The dots of the secret path, over the ground and under the buildings and trees (still layer). */
internal fun MapPen.drawTunnelPath(d: DrawScope, g: MapGeo) {
    val dots = g.tunnel.dots
    if (dots.isEmpty()) return
    d.drawPoints(dots, PointMode.Points, Ink.line, strokeWidth = lw * 3.3f, cap = StrokeCap.Round, alpha = 0.45f)
    d.drawPoints(dots, PointMode.Points, nt(Color(0xFFFFD35C), 0.3f), strokeWidth = lw * 2.1f, cap = StrokeCap.Round, alpha = 0.97f)
}

/** Two tiny glints drifting along the secret path, from the house to the cave and back (live). */
internal fun MapPen.drawTunnelLive(d: DrawScope, g: MapGeo) {
    if (!tunnel) return
    for (k in 0 until 2) {
        val f = wrap(t * 0.045f + k * 0.5f, 1f)
        val p = g.tunnel.line.at(f)
        twinkleLive(d, p, lw * (2.6f + 0.8f * sin(t * 4f + k * 2f)), Color(0xFFFFF3B0), 0.55f + 0.45f * sin(f * 3.1416f))
    }
}

// ------------------------------------------------------------------------------------- small helpers

private fun Bx.pstroke(p: Path, col: Color, wd: Float, a: Float = 1f) =
    d.drawPath(p, c(col), alpha = a, style = Stroke(lw * wd, cap = StrokeCap.Round, join = StrokeJoin.Round))

private fun Bx.smooth(vararg p: Float): Path {
    val pa = Path()
    val n = p.size / 2
    pa.moveTo(p[0], p[1])
    for (i in 1 until n - 1) pa.quadraticTo(p[i * 2], p[i * 2 + 1], (p[i * 2] + p[i * 2 + 2]) / 2f, (p[i * 2 + 1] + p[i * 2 + 3]) / 2f)
    pa.lineTo(p[n * 2 - 2], p[n * 2 - 1])
    return pa
}

private fun Bx.archPath(cx: Float, w: Float, yb: Float, yt: Float): Path {
    val x0 = cx - w / 2f
    val x1 = cx + w / 2f
    val rr = w / 2f
    return Path().apply {
        moveTo(x0, yb)
        lineTo(x0, yt + rr)
        quadraticTo(x0, yt, cx, yt)
        quadraticTo(x1, yt, x1, yt + rr)
        lineTo(x1, yb)
        close()
    }
}

/** An arched window with a white frame; [warm] lets the glass glow at night. */
private fun Bx.archWin(cx: Float, w: Float, yb: Float, yt: Float, warm: Boolean = true) {
    val p = archPath(cx, w, yb, yt)
    if (warm) glow(cx, (yb + yt) / 2f, w * 1.5f)
    d.drawPath(p, lerp(c(Color(0xFF9CCDE6)), Color(0xFFFFD76B), if (warm) lit else 0f))
    d.drawLine(c(TRIM), Offset(cx, yt), Offset(cx, yb), strokeWidth = lw * 1.2f)
    d.drawLine(c(TRIM), Offset(cx - w / 2f, yt + w * 0.7f), Offset(cx + w / 2f, yt + w * 0.7f), strokeWidth = lw * 1.2f)
    d.drawPath(p, c(TRIM), style = Stroke(lw * 2f, join = StrokeJoin.Round))
    ink(p, 0.8f)
}

private fun Bx.potted(x: Float, y: Float, s: Float, bloom: Color? = null) {
    val p = path(x - s * 0.5f, y - s * 0.55f, x + s * 0.5f, y - s * 0.55f, x + s * 0.38f, y, x - s * 0.38f, y)
    fill(p, Color(0xFFC9824A))
    ink(p, 0.8f)
    disc(x - s * 0.28f, y - s * 0.8f, s * 0.3f, Color(0xFF3FA05A), 0.7f)
    disc(x + s * 0.28f, y - s * 0.78f, s * 0.28f, Color(0xFF4DB868), 0.7f)
    disc(x, y - s * 1.0f, s * 0.32f, Color(0xFF58C46E), 0.7f)
    if (bloom != null) {
        disc(x - s * 0.08f, y - s * 1.12f, s * 0.12f, bloom, 0.6f)
        disc(x + s * 0.3f, y - s * 0.92f, s * 0.1f, bloom, 0.6f)
    }
}



// ------------------------------------------------------------------------------------- the still house

/** The whole estate, still: from the back (the tree) to the front (gate, hedges and flowers). */
internal fun Bx.manor() {
    d.withTransform({ translate(DX, 0f) }) {
        manorShadows()
        manorPines()
        manorPathAndPond()
        manorTree()
        manorMain()
        manorTower()
        manorBay()
        manorGarden()
        manorGreenhouse()
        manorFront()
    }
}

private fun Bx.manorShadows() {
    shadowOf(-1.78f, 0f, -0.09f, 0.34f, 1.4f, 0.22f, 1.95f, 0.1f, 1.8f, -0.25f, -1.45f, -0.25f)
    d.drawOval(Ink.line, Offset(0.1f, -0.42f), Size(1.4f, 0.2f), alpha = 0.12f * (1f - 0.45f * n))
}

private fun Bx.manorPines() {
    pineTree(-2.02f, -0.1f, 0.85f)
    pineTree(-1.8f, -0.34f, 0.62f)
    pineTree(2.34f, -0.04f, 0.72f)
}

/** The lawn path from the porch steps to the gate, and the pond with its stones, lilies and reeds. */
private fun Bx.manorPathAndPond() {
    val edge = m.nt(if (snow) Color(0xFFBFC9DA) else Color(0xFFB08A5A), 0.5f)
    val body = m.nt(if (snow) Color(0xFFF7F9FF) else Color(0xFFEBD6A2), 0.5f)
    val lane = smooth(-0.68f, 0.3f, -0.8f, 0.4f, -1.0f, 0.37f, -1.2f, 0.29f, GATE_LX, GATE_LY)
    d.drawPath(lane, edge, style = Stroke(0.104f + lw * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    d.drawPath(lane, body, style = Stroke(0.104f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // The pond.
    val px = 0.95f
    val py = 0.25f
    d.drawOval(c(Color(0xFF8C8FA3)), Offset(px - 0.58f, py - 0.15f), Size(1.16f, 0.32f))
    d.drawOval(Ink.line, Offset(px - 0.58f, py - 0.15f), Size(1.16f, 0.32f), style = Stroke(lw * 1.1f))
    d.drawOval(c(Color(0xFF1FA6C0)), Offset(px - 0.51f, py - 0.12f), Size(1.02f, 0.25f))
    d.drawOval(c(Color(0xFF6CDCDD)), Offset(px - 0.45f, py - 0.1f), Size(0.78f, 0.16f), alpha = 0.9f)
    d.drawOval(c(Color(0xFFB8F2EC)), Offset(px - 0.28f, py - 0.085f), Size(0.36f, 0.08f), alpha = 0.7f)
    d.drawOval(Ink.line, Offset(px - 0.51f, py - 0.12f), Size(1.02f, 0.25f), alpha = 0.7f, style = Stroke(lw * 0.9f))
    for (k in 0 until 10) {
        val a = 0.15f + 2.85f * k / 9f
        oval(px + cos(a) * 0.56f, py + 0.02f + sin(a) * 0.14f, 0.052f, 0.032f, Color(0xFFB9B4C4), 0.8f)
    }
    for ((lx, ly, lr) in listOf(Triple(px - 0.22f, py, 0.07f), Triple(px + 0.1f, py + 0.05f, 0.075f), Triple(px + 0.33f, py - 0.02f, 0.06f))) {
        oval(lx, ly, lr, lr * 0.45f, Color(0xFF4DB868), 0.7f)
    }
    disc(px - 0.22f, py - 0.035f, 0.025f, Color(0xFFFF8FB8), 0.6f)
    // A little frog on a lily pad.
    val fx = px + 0.1f
    val fy = py + 0.015f
    oval(fx, fy, 0.045f, 0.03f, Color(0xFF6BCB77), 0.8f)
    disc(fx - 0.022f, fy - 0.034f, 0.016f, Color(0xFFFFFFFF), 0.6f)
    disc(fx + 0.022f, fy - 0.034f, 0.016f, Color(0xFFFFFFFF), 0.6f)
    d.drawCircle(Ink.line, 0.007f, Offset(fx - 0.02f, fy - 0.033f))
    d.drawCircle(Ink.line, 0.007f, Offset(fx + 0.024f, fy - 0.033f))
    // Reeds with cattails at the left end.
    for (k in 0 until 4) {
        val rx = px - 0.5f + 0.035f * k
        val hh = 0.17f + 0.04f * (k % 2)
        line(rx, py + 0.02f, rx + 0.012f * (k - 1.5f), py + 0.02f - hh, Color(0xFF4F9A48), 1.1f)
        if (k % 2 == 1) disc(rx + 0.012f * (k - 1.5f), py + 0.02f - hh - 0.012f, 0.014f, Color(0xFF8A5A3A), 0.6f)
    }
}

/** The big tree behind the house with a treehouse in its crown. */
private fun Bx.manorTree() {
    val tx = TREE_X
    val bark = Color(0xFF8A5A3A)
    // A swing branch out over the winter garden, with a rope swing.
    val branch = Path().apply {
        moveTo(tx + 0.1f, -1.5f)
        quadraticTo(tx + 0.5f, -1.52f, tx + 0.95f, -1.62f)
        lineTo(tx + 0.95f, -1.57f)
        quadraticTo(tx + 0.5f, -1.45f, tx + 0.1f, -1.38f)
        close()
    }
    fill(branch, bark)
    ink(branch, 1f)
    line(tx + 0.78f, -1.58f, tx + 0.78f, -1.12f, Color(0xFFD9C9A0), 1.1f)
    line(tx + 0.92f, -1.6f, tx + 0.92f, -1.12f, Color(0xFFD9C9A0), 1.1f)
    box(tx + 0.74f, -1.12f, tx + 0.96f, -1.085f, WOOD, 0.9f)
    // The trunk.
    val trunk = Path().apply {
        moveTo(tx - 0.16f, -0.6f)
        quadraticTo(tx - 0.08f, -0.7f, tx - 0.09f, -0.9f)
        quadraticTo(tx - 0.11f, -1.3f, tx - 0.08f, -1.7f)
        lineTo(tx + 0.1f, -1.7f)
        quadraticTo(tx + 0.12f, -1.3f, tx + 0.1f, -0.9f)
        quadraticTo(tx + 0.1f, -0.7f, tx + 0.2f, -0.6f)
        close()
    }
    fill(trunk, bark)
    d.drawRect(c(bark.darken(0.28f)), Offset(tx + 0.02f, -1.7f), Size(0.08f, 0.9f), alpha = 0.55f)
    ink(trunk, 1.1f)
    // The crown, behind the treehouse.
    val blobs = floatArrayOf(
        tx - 0.55f, -1.95f, 0.38f, 0.32f,
        tx + 0.55f, -1.98f, 0.4f, 0.34f,
        tx, -2.2f, 0.62f, 0.46f,
    )
    for (i in 0 until 3) {
        val cx = blobs[i * 4]
        val cy = blobs[i * 4 + 1]
        val rx = blobs[i * 4 + 2]
        val ry = blobs[i * 4 + 3]
        val sh = crownPath(cx, cy, rx, ry, 20 + i)
        val leaf = if (m.season == app.trollfoss.domain.Season.WINTER) Color(0xFFD3DEE8) else m.leaf(LEAF, 2)
        fill(sh, leaf.darken(if (snow) 0.16f else 0.28f))
        fill(crownPath(cx - rx * 0.14f, cy - ry * 0.16f, rx * 0.88f, ry * 0.8f, 30 + i), if (snow) leaf.lighten(0.5f) else leaf)
        ink(sh, 1.1f, 0.85f)
    }
    d.drawOval(Color.White, Offset(tx - 0.42f, -2.5f), Size(0.3f, 0.12f), alpha = 0.22f)
    // The treehouse: a timber cabin on a platform with a green-copper roof, a round window and bunting.
    val fy = -1.55f
    box(tx - 0.3f, fy, tx + 0.34f, fy + 0.05f, WOOD.darken(0.2f), 1f)
    box(tx - 0.2f, fy - 0.3f, tx + 0.24f, fy, WOOD, 1.1f)
    for (k in 1..3) line(tx - 0.2f, fy - k * 0.075f, tx + 0.24f, fy - k * 0.075f, WOOD.darken(0.4f), 0.7f, 0.5f)
    face(DOME.darken(0.1f), tx - 0.27f, fy - 0.28f, tx + 0.02f, fy - 0.54f, tx + 0.31f, fy - 0.28f)
    face(DOME.darken(0.34f), tx + 0.02f, fy - 0.54f, tx + 0.31f, fy - 0.28f, tx + 0.28f, fy - 0.26f, tx + 0.02f, fy - 0.5f)
    win(tx - 0.07f, fy - 0.24f, tx + 0.07f, fy - 0.1f, round = true)
    // Railing and a rope ladder.
    line(tx - 0.3f, fy, tx - 0.3f, fy - 0.1f, Ink.line, 1.6f)
    line(tx + 0.34f, fy, tx + 0.34f, fy - 0.1f, Ink.line, 1.6f)
    line(tx - 0.3f, fy - 0.1f, tx - 0.2f, fy - 0.1f, Ink.line, 1.4f)
    line(tx + 0.24f, fy - 0.1f, tx + 0.34f, fy - 0.1f, Ink.line, 1.4f)
    // Bunting from the roof to a branch.
    val colsB = listOf(Color(0xFFFF6B8A), Color(0xFFFFC83D), Color(0xFF5AA9E6), Color(0xFFFFFFFF))
    for (k in 0 until 4) {
        val bxp = tx + 0.31f + 0.12f * k
        val byp = fy - 0.28f - 0.06f * k + 0.02f * sin(k * 1.4f)
        fill(path(bxp - 0.025f, byp, bxp + 0.025f, byp, bxp, byp + 0.055f), colsB[k])
    }
    line(tx + 0.31f, fy - 0.28f, tx + 0.75f, fy - 0.5f, Ink.line, 0.7f, 0.8f)
    // Leaves in front, so the cabin sits in the foliage.
    for ((lx, ly, lr) in listOf(Triple(tx - 0.3f, fy - 0.05f, 0.17f), Triple(tx + 0.42f, fy + 0.02f, 0.15f), Triple(tx - 0.12f, fy - 0.5f, 0.14f))) {
        val lp = crownPath(lx, ly, lr, lr * 0.85f, 41)
        fill(lp, LEAF.darken(0.16f))
        ink(lp, 0.9f, 0.8f)
    }
}

/** The long main house: timber walls, a burgundy roof, windows, a dormer, chimneys. */
private fun Bx.manorMain() {
    val vx = 0.5f * DEP
    val vy = -0.36f * DEP
    // The shaded end and its gable.
    face(WALL.darken(0.24f), MX1, -WALL_H, MX1 + vx, -WALL_H + vy, MX1 + vx, vy, MX1, 0f)
    face(WALL.darken(0.22f), MX1, -WALL_H, MX1 + vx, -WALL_H + vy, MX1 + vx / 2f, -WALL_H - ROOF_H + vy / 2f)
    // The lit front wall with board lines and a belt course between the floors.
    box(MX0, -WALL_H, MX1, 0f, WALL)
    val boards = Path()
    var y = -0.1f
    while (y > -WALL_H + 0.02f) {
        boards.moveTo(MX0, y)
        boards.lineTo(MX1, y)
        y -= 0.075f
    }
    d.drawPath(boards, c(WALL.darken(0.45f)), alpha = 0.3f, style = Stroke(lw * 0.6f))
    line(MX0, -0.38f, MX1, -0.38f, TRIM, 2.4f)
    line(MX0, -0.38f, MX1, -0.38f, Ink.line, 0.6f, 0.4f)
    line(MX0, 0f, MX0, -WALL_H, TRIM, 2.4f)
    line(MX1, 0f, MX1, -WALL_H, TRIM, 2.4f)
    // A stone foundation with joints.
    d.drawRect(c(Color(0xFF8E8798)), Offset(MX0 - 0.01f, -0.09f), Size(MX1 - MX0 + 0.02f, 0.09f))
    d.drawRect(Ink.line, Offset(MX0 - 0.01f, -0.09f), Size(MX1 - MX0 + 0.02f, 0.09f), style = Stroke(lw))
    val joints = ArrayList<Offset>()
    var jx = MX0 + 0.1f
    var ji = 0
    while (jx < MX1) {
        joints.add(Offset(jx, if (ji % 2 == 0) -0.09f else -0.045f))
        joints.add(Offset(jx, if (ji % 2 == 0) -0.045f else 0f))
        jx += 0.14f
        ji++
    }
    d.drawPoints(joints, PointMode.Lines, c(Color(0xFF5F5970)), strokeWidth = lw * 0.7f)
    // The roof plane up to the ridge, with courses of shingles.
    val ridgeY = -WALL_H - ROOF_H + vy / 2f
    val rp = face(roofRaw(ROOF).lighten(0.05f), MX0 - 0.05f, -WALL_H + 0.04f, MX1 + 0.05f, -WALL_H + 0.04f, MX1 + vx / 2f + 0.03f, ridgeY, MX0 + vx / 2f - 0.03f, ridgeY)
    if (!snow) {
        // Courses of tiles: a line for each course and short staggered joints between them.
        val tiles = Path()
        val lx0 = MX0 - 0.05f
        val lx1 = MX0 + vx / 2f - 0.03f
        val rx0 = MX1 + 0.05f
        val rx1 = MX1 + vx / 2f + 0.03f
        val ey0 = -WALL_H + 0.04f
        val rows = 7
        for (i in 1 until rows) {
            val f = i.toFloat() / rows
            tiles.moveTo(mix(lx0, lx1, f), mix(ey0, ridgeY, f))
            tiles.lineTo(mix(rx0, rx1, f), mix(ey0, ridgeY, f))
        }
        for (i in 0 until rows) {
            val f0 = i.toFloat() / rows
            val f1 = (i + 1f) / rows
            var u = if (i % 2 == 0) 0.03f else 0.06f
            while (u < 0.99f) {
                tiles.moveTo(mix(mix(lx0, lx1, f0), mix(rx0, rx1, f0), u), mix(ey0, ridgeY, f0))
                tiles.lineTo(mix(mix(lx0, lx1, f1), mix(rx0, rx1, f1), u), mix(ey0, ridgeY, f1))
                u += 0.06f
            }
        }
        d.drawPath(tiles, c(ROOF.darken(0.4f)), alpha = 0.4f, style = Stroke(lw * 0.6f, cap = StrokeCap.Round))
    }
    line(MX0 + vx / 2f - 0.03f, ridgeY, MX1 + vx / 2f + 0.03f, ridgeY, ROOF.lighten(0.35f), 1.7f)
    line(MX0 - 0.05f, -WALL_H + 0.04f, MX1 + 0.05f, -WALL_H + 0.04f, TRIM, 2.4f)
    if (snow) fill(rp, Color(0xFFF4F8FF), 0.82f)
    // A dormer with its own little gable.
    d.withTransform({ translate(0.3f, -0.9f) }) {
        gable(0.13f, 0.14f, 0.15f, 0.14f, WALL, ROOF)
        archWin(0f, 0.08f, -0.03f, -0.19f)
    }
    // Windows: ground floor with flower boxes, upper floor with shutters; some are lit by the live layer.
    win(0.06f, -0.32f, 0.24f, -0.14f, box = true)
    win(0.32f, -0.32f, 0.5f, -0.14f, box = true)
    win(0.06f, -0.64f, 0.24f, -0.46f, shutters = true, warm = false)
    win(0.32f, -0.64f, 0.5f, -0.46f, shutters = true, warm = false)
    win(-1.1f, -0.62f, -0.99f, -0.46f, warm = false)
    win(-1.1f, -0.3f, -0.99f, -0.14f)
    // The window in the shaded end wall.
    val ex = MX1 + vx * 0.3f
    val ey = vy * 0.3f
    val ew = path(ex, ey - 0.62f, ex + vx * 0.4f, ey + vy * 0.4f - 0.62f, ex + vx * 0.4f, ey + vy * 0.4f - 0.4f, ex, ey - 0.4f)
    d.drawPath(ew, lerp(c(Color(0xFF8DB9D6)), Color(0xFFFFD76B), lit))
    d.drawPath(ew, c(TRIM), style = Stroke(lw * 1.6f, join = StrokeJoin.Round))
    glow(ex + vx * 0.2f, ey - 0.5f, 0.16f)
    // Chimneys on the roof, a little crooked.
    d.withTransform({ rotate(-4f, Offset(0.1f, -1.0f)) }) { chimney(0.1f, -1.0f, 0.1f, 0.3f) }
    d.withTransform({ rotate(5f, Offset(-1.1f, -0.98f)) }) { chimney(-1.1f, -0.98f, 0.09f, 0.26f) }
}

/** The round observatory tower on the left, leaning a little: windows, a balcony ring, a drum and a dome. */
private fun Bx.manorTower() {
    val cx = TOWER_X
    val r = TOWER_R
    val top = -TOWER_H
    d.withTransform({ rotate(LEAN, Offset(cx, 0f)) }) {
        val wall = WALL.lighten(0.1f)
        val body = Path().apply {
            moveTo(cx - r, top)
            lineTo(cx - r, 0f)
            arcTo(Rect(cx - r, -0.08f, cx + r, 0.08f), 180f, -180f, false)
            lineTo(cx + r, top)
            close()
        }
        fill(body, wall)
        val band = Path().apply {
            moveTo(cx + r * 0.38f, top)
            lineTo(cx + r, top)
            lineTo(cx + r, 0f)
            arcTo(Rect(cx - r, -0.08f, cx + r, 0.08f), 0f, 67.7f, false)
            close()
        }
        fill(band, WALL.darken(0.26f))
        val boards = Path()
        for (a in floatArrayOf(-60f, -20f, 20f, 60f)) {
            val bx0 = cx + r * sin(a * DEG)
            boards.moveTo(bx0, -0.15f)
            boards.lineTo(bx0, top + 0.02f)
        }
        d.drawPath(boards, c(WALL.darken(0.45f)), alpha = 0.3f, style = Stroke(lw * 0.6f))
        // The stone foot and a white band that wraps round the middle.
        val foot = Path().apply {
            moveTo(cx - r, -0.14f)
            lineTo(cx - r, 0f)
            arcTo(Rect(cx - r, -0.08f, cx + r, 0.08f), 180f, -180f, false)
            lineTo(cx + r, -0.14f)
            close()
        }
        fill(foot, Color(0xFF8E8798))
        line(cx - r, -0.14f, cx + r, -0.14f, Ink.line, 0.9f)
        val tb = Path().apply {
            moveTo(cx - r, -0.74f)
            quadraticTo(cx, -0.74f + 0.16f, cx + r, -0.74f)
        }
        d.drawPath(tb, c(TRIM), style = Stroke(lw * 2.6f))
        d.drawPath(tb, Ink.line, alpha = 0.55f, style = Stroke(lw * 0.6f))
        ink(body, 1.3f)
        // The cellar door: a stone arch in the foot of the tower with a step and a keyhole glint (the secret path starts here).
        val cd = archPath(CELLAR_LX, 0.13f, 0.06f, -0.1f)
        fill(cd, Color(0xFF2A2236))
        d.drawPath(cd, c(Color(0xFFB9B4C4)), style = Stroke(lw * 2.4f, join = StrokeJoin.Round))
        ink(cd, 1f)
        box(CELLAR_LX - 0.09f, 0.06f, CELLAR_LX + 0.09f, 0.085f, Color(0xFF9C95A8), 0.8f)
        disc(CELLAR_LX, 0f, 0.014f, BRASS, 0.5f)
        // Windows: a tall arched one and a round one.
        archWin(cx - 0.07f, 0.13f, -0.3f, -0.58f)
        win(cx - 0.12f, -1.09f, cx + 0.04f, -0.93f, round = true)
        // The balcony ring with its rail.
        val ring = Rect(cx - r - 0.1f, top - 0.085f, cx + r + 0.1f, top + 0.085f)
        d.drawOval(c(TRIM), ring.topLeft, ring.size)
        d.drawOval(Ink.line, ring.topLeft, ring.size, style = Stroke(lw * 1.1f))
        for (k in 0..8) {
            val a = (0.1f + 2.94f * k / 8f)
            val rx = cx + cos(a) * (r + 0.08f)
            val ry = top + sin(a) * 0.075f
            line(rx, ry, rx, ry - 0.075f, TRIM, 0.9f)
        }
        d.drawArc(c(TRIM), 0f, 180f, false, Offset(ring.left, ring.top - 0.075f), ring.size, style = Stroke(lw * 1.8f, cap = StrokeCap.Round))
        // The drum under the dome.
        val dr = 0.25f
        d.drawRect(c(wall), Offset(cx - dr, DOME_Y), Size(dr * 2f, -TOWER_H - DOME_Y))
        d.drawRect(c(WALL.darken(0.26f)), Offset(cx + dr * 0.4f, DOME_Y), Size(dr * 0.6f, -TOWER_H - DOME_Y))
        d.drawRect(Ink.line, Offset(cx - dr, DOME_Y), Size(dr * 2f, -TOWER_H - DOME_Y), style = Stroke(lw * 1.2f))
        win(cx - 0.16f, DOME_Y + 0.1f, cx - 0.04f, DOME_Y + 0.22f, round = true)
        win(cx + 0.06f, DOME_Y + 0.1f, cx + 0.16f, DOME_Y + 0.2f, round = true)
        // The dome with a slit for the telescope, a brass band and a finial.
        val dome = Path().apply {
            moveTo(cx - 0.3f, DOME_Y)
            arcTo(Rect(cx - 0.3f, DOME_Y - 0.26f, cx + 0.3f, DOME_Y + 0.26f), 180f, 180f, false)
            close()
        }
        fill(dome, DOME.darken(0.25f))
        val litDome = Path().apply {
            moveTo(cx - 0.27f, DOME_Y)
            arcTo(Rect(cx - 0.28f, DOME_Y - 0.25f, cx + 0.14f, DOME_Y + 0.25f), 180f, 150f, false)
            lineTo(cx + 0.14f, DOME_Y)
            close()
        }
        fill(litDome, DOME)
        ink(dome, 1.3f)
        d.drawRect(c(BRASS), Offset(cx - 0.31f, DOME_Y - 0.02f), Size(0.62f, 0.04f))
        d.drawRect(Ink.line, Offset(cx - 0.31f, DOME_Y - 0.02f), Size(0.62f, 0.04f), style = Stroke(lw * 0.8f))
        val slit = Path().apply {
            moveTo(cx - 0.04f, DOME_Y - 0.02f)
            lineTo(cx - 0.04f, DOME_Y - 0.17f)
            quadraticTo(cx - 0.04f, DOME_Y - 0.23f, cx, DOME_Y - 0.23f)
            quadraticTo(cx + 0.04f, DOME_Y - 0.23f, cx + 0.04f, DOME_Y - 0.17f)
            lineTo(cx + 0.04f, DOME_Y - 0.02f)
            close()
        }
        fill(slit, Color(0xFF1E1730))
        line(cx, DOME_Y - 0.26f, cx, DOME_Y - 0.36f, Ink.line, 1.4f)
        disc(cx, DOME_Y - 0.38f, 0.022f, BRASS, 0.7f)
        // A flag pole on the balcony ring (the pennant waves in the live layer).
        line(cx + 0.37f, top, cx + 0.37f, top - 0.52f, Ink.line, 1.6f)
        disc(cx + 0.37f, top - 0.535f, 0.018f, BRASS, 0.6f)
    }
}

/** The steep central gable with its round attic window, the balcony, the porch, the front door and the butler. */
private fun Bx.manorBay() {
    val fvx = -0.5f * FWD
    val fvy = 0.36f * FWD
    d.withTransform({ translate(BAY_X, BAY_DY) }) {
        gable(BAY_HW, WALL_H, 0.9f, 0.3f, WALL, ROOF)
        // Courses of tiles on the gable's right roof slope, and a bright ridge.
        if (!snow) {
            val gt = Path()
            for (k in 1..6) {
                val f = k / 7f
                gt.moveTo(mix(0f, BAY_HW, f), mix(-WALL_H - 0.9f, -WALL_H, f))
                gt.lineTo(mix(0.15f, BAY_HW + 0.15f, f), mix(-WALL_H - 0.9f - 0.108f, -WALL_H - 0.108f, f))
            }
            d.drawPath(gt, c(ROOF.darken(0.4f)), alpha = 0.45f, style = Stroke(lw * 0.6f, cap = StrokeCap.Round))
        }
        line(0f, -WALL_H - 0.9f, 0.15f, -WALL_H - 0.9f - 0.108f, ROOF.lighten(0.4f), 1.4f)
        // The attic window: dark glass, where the ghost lives.
        d.drawCircle(c(DARK_GLASS), 0.105f, Offset(0f, ATTIC_Y))
        d.drawCircle(c(Color(0xFF6E6BA6)), 0.105f, Offset(0f, ATTIC_Y), alpha = 0.5f, style = Stroke(0.03f))
        d.drawCircle(c(TRIM), 0.105f, Offset(0f, ATTIC_Y), style = Stroke(lw * 2.4f))
        d.drawCircle(Ink.line, 0.105f + lw, Offset(0f, ATTIC_Y), style = Stroke(lw * 0.8f))
        line(0f, ATTIC_Y - 0.1f, 0f, ATTIC_Y + 0.1f, TRIM, 1.1f)
        // A finial on the peak (the weathervane turns in the live layer).
        line(0f, -1.62f, 0f, -1.78f, Ink.line, 1.6f)
        disc(0f, -1.8f, 0.022f, BRASS, 0.7f)
        // The balcony door upstairs.
        val bd = archPath(0f, 0.26f, -0.44f, -0.7f)
        d.drawPath(bd, lerp(c(Color(0xFF9CCDE6)), Color(0xFFFFD76B), lit))
        glow(0f, -0.57f, 0.34f)
        d.drawLine(c(TRIM), Offset(0f, -0.7f), Offset(0f, -0.44f), strokeWidth = lw * 1.3f)
        d.drawLine(c(TRIM), Offset(-0.13f, -0.52f), Offset(0.13f, -0.52f), strokeWidth = lw * 1.3f)
        d.drawPath(bd, c(TRIM), style = Stroke(lw * 2.2f, join = StrokeJoin.Round))
        ink(bd, 0.8f)
        // The front door, with panels and a round fanlight.
        val door = archPath(0f, 0.3f, 0f, -0.34f)
        fill(door, DOOR)
        ink(door, 1.2f)
        d.drawLine(Ink.line, Offset(0f, -0.3f), Offset(0f, 0f), strokeWidth = lw * 0.9f, alpha = 0.7f)
        d.drawRect(c(DOOR.darken(0.25f)), Offset(-0.12f, -0.2f), Size(0.09f, 0.14f), alpha = 0.6f)
        d.drawRect(c(DOOR.darken(0.25f)), Offset(0.03f, -0.2f), Size(0.09f, 0.14f), alpha = 0.6f)
        disc(-0.03f, -0.12f, 0.012f, BRASS, 0.5f)
        disc(0.03f, -0.12f, 0.012f, BRASS, 0.5f)
        d.drawCircle(lerp(c(Color(0xFF9CCDE6)), Color(0xFFFFD76B), lit), 0.045f, Offset(0f, -0.27f))
        d.drawCircle(c(TRIM), 0.045f, Offset(0f, -0.27f), style = Stroke(lw * 1.4f))
        // The porch floor and the steps down to the lawn.
        val floor = path(-0.34f, 0f, 0.34f, 0f, 0.34f + fvx, fvy, -0.34f + fvx, fvy)
        fill(floor, Color(0xFFC9A27A))
        ink(floor, 1f)
        for (k in 0 until 3) {
            val sw = 0.52f + 0.08f * k
            box(-sw / 2f + fvx, fvy + 0.045f * k, sw / 2f + fvx, fvy + 0.045f * (k + 1), if (k % 2 == 0) Color(0xFFB9B4C4) else Color(0xFFA7A1B5), 0.8f)
        }
        // Potted bushes beside the steps.
        potted(-0.4f + fvx, fvy + 0.1f, 0.12f, Color(0xFFFF8FB8))
        potted(0.4f + fvx, fvy + 0.1f, 0.12f, Color(0xFFFFE27A))
        // The robot butler, tiny, on the porch right of the door.
        manorRobot(0.2f, 0.06f)
        // Porch posts and the balcony slab on top, with a railing of balusters and flower boxes.
        for (sx in floatArrayOf(-0.3f, 0.3f)) {
            line(sx + fvx, fvy, sx + fvx, -0.44f + fvy, Ink.line, 4.6f)
            line(sx + fvx, fvy, sx + fvx, -0.44f + fvy, TRIM, 2.6f)
        }
        val slab = path(-0.37f, -0.44f, 0.37f, -0.44f, 0.37f + fvx, -0.44f + fvy, -0.37f + fvx, -0.44f + fvy)
        fill(slab, WOOD.lighten(0.15f))
        ink(slab, 1f)
        box(-0.37f + fvx, -0.44f + fvy, 0.37f + fvx, -0.4f + fvy, TRIM, 1f)
        val bal = ArrayList<Offset>()
        var bxp = -0.37f
        while (bxp <= 0.371f) {
            bal.add(Offset(bxp + fvx, -0.44f + fvy))
            bal.add(Offset(bxp + fvx, -0.56f + fvy))
            bxp += 0.07f
        }
        d.drawPoints(bal, PointMode.Lines, c(TRIM), strokeWidth = lw * 0.9f)
        line(-0.37f + fvx, -0.56f + fvy, 0.37f + fvx, -0.56f + fvy, Ink.line, 3.0f)
        line(-0.37f + fvx, -0.56f + fvy, 0.37f + fvx, -0.56f + fvy, TRIM, 1.6f)
        line(0.37f + fvx, -0.56f + fvy, 0.37f, -0.56f, TRIM, 1.4f)
        val bloom = listOf(Color(0xFFFF6B8A), Color(0xFFFFC83D), Color(0xFFFFFFFF))
        for (k in 0 until 7) disc(-0.33f + fvx + k * 0.11f, -0.585f + fvy, 0.022f, bloom[k % 3], 0.5f)
        // Two lanterns hanging under the slab.
        for (sx in floatArrayOf(-0.3f, 0.3f)) {
            val lx = sx + fvx
            line(lx, -0.4f + fvy, lx, -0.36f + fvy, Ink.line, 0.9f)
            round(lx - 0.028f, -0.36f + fvy, lx + 0.028f, -0.285f + fvy, 0.012f, BRASS, 0.8f)
            d.drawRect(lerp(c(Color(0xFFFFF3B0)), Color(0xFFFFE27A), lit), Offset(lx - 0.015f, -0.345f + fvy), Size(0.03f, 0.05f))
            glow(lx, -0.32f + fvy, 0.14f, Color(0xFFFFD76B), 0.3f)
        }
    }
}

/** A tiny robot butler: a silver box with an antenna, two cyan eyes and a bow tie (his glint is live). */
private fun Bx.manorRobot(x: Float, y: Float) {
    d.drawOval(Ink.line, Offset(x - 0.06f, y - 0.01f), Size(0.12f, 0.025f), alpha = 0.2f)
    round(x - 0.045f, y - 0.1f, x + 0.045f, y - 0.025f, 0.015f, Color(0xFFB9C4D6), 0.8f)
    round(x - 0.04f, y - 0.17f, x + 0.04f, y - 0.105f, 0.015f, Color(0xFFD5DEEC), 0.8f)
    disc(x - 0.016f, y - 0.14f, 0.011f, Color(0xFF6FF2FF), 0.4f)
    disc(x + 0.016f, y - 0.14f, 0.011f, Color(0xFF6FF2FF), 0.4f)
    line(x, y - 0.17f, x, y - 0.205f, Ink.line, 0.9f)
    fill(path(x - 0.022f, y - 0.095f, x + 0.022f, y - 0.095f, x, y - 0.075f), Color(0xFFE94F4F))
    line(x + 0.045f, y - 0.075f, x + 0.085f, y - 0.095f, Ink.line, 1.0f)
    d.drawRect(c(TRIM), Offset(x + 0.07f, y - 0.1f), Size(0.045f, 0.012f))
    disc(x - 0.025f, y - 0.012f, 0.014f, Color(0xFF55505E), 0.5f)
    disc(x + 0.025f, y - 0.012f, 0.014f, Color(0xFF55505E), 0.5f)
}

/** The winter garden: a glass house leaning on the right end of the villa, full of green plants. */
private fun Bx.manorGarden() {
    val vx = 0.3f
    val vy = -0.216f
    val eave = GARDEN_EAVE
    val ridge = GARDEN_RIDGE
    val glassD = Color(0xFF8CC3D8)
    // The shaded glass end and its gable.
    face(glassD, GX1, eave, GX1 + vx, eave + vy, GX1 + vx, vy, GX1, 0f)
    face(glassD.darken(0.08f), GX1, eave, GX1 + vx, eave + vy, GX1 + vx / 2f, ridge + vy / 2f)
    line(GX1 + vx / 2f, vy / 2f, GX1 + vx / 2f, eave + vy / 2f, TRIM, 1.3f)
    line(GX1, -0.26f, GX1 + vx, -0.26f + vy, TRIM, 1.1f)
    // The inside: a dark back wall, a bench, then plants.
    val x0 = GX0 + 0.03f
    val x1 = GX1 - 0.03f
    val top = eave + 0.03f
    d.drawRect(c(Color(0xFF2F6F5A)), Offset(x0, top), Size(x1 - x0, -0.14f - top))
    d.drawRect(c(Color(0xFF8A6A4A)), Offset(x0, -0.2f), Size(x1 - x0, 0.06f))
    // A palm, a lemon tree, ferns in pots and a hanging basket.
    line(0.9f, -0.14f, 0.88f, -0.31f, Color(0xFF8A5A3A), 3f)
    for (a in floatArrayOf(-165f, -125f, -80f, -35f, 5f)) {
        val ex = 0.88f + cos(a * DEG) * 0.15f
        val ey = -0.31f + sin(a * DEG) * 0.09f + 0.04f
        val fr = Path().apply {
            moveTo(0.88f, -0.31f)
            quadraticTo(0.88f + cos(a * DEG) * 0.08f, -0.31f + sin(a * DEG) * 0.13f, ex, ey)
        }
        pstroke(fr, Color(0xFF3FA05A), 2.6f)
    }
    line(1.3f, -0.14f, 1.3f, -0.25f, Color(0xFF8A5A3A), 2.4f)
    disc(1.3f, -0.3f, 0.075f, Color(0xFF3FA05A), 0f)
    disc(1.25f, -0.32f, 0.055f, Color(0xFF58C46E), 0f)
    for (k in 0 until 4) d.drawCircle(c(Color(0xFFFFD84A)), 0.013f, Offset(1.25f + 0.04f * k, -0.27f - 0.025f * (k % 2)))
    potted(0.66f, -0.14f, 0.12f, Color(0xFFFF8FB8))
    potted(1.1f, -0.14f, 0.1f, null)
    line(1.12f, top, 1.12f, -0.3f, Ink.line, 0.8f, 0.7f)
    disc(1.12f, -0.27f, 0.035f, Color(0xFF58C46E), 0.5f)
    // The glass: a pale tint, a glint, white mullions and a white dado.
    val tint = lerp(c(GLASS), Color(0xFFFFE9A0), lit)
    d.drawRect(tint, Offset(x0, top), Size(x1 - x0, -0.14f - top), alpha = 0.4f)
    line(0.7f, top + 0.02f, 0.82f, -0.17f, Color.White, 1.8f, 0.5f)
    line(0.76f, top + 0.02f, 0.8f, top + 0.08f, Color.White, 1.8f, 0.5f)
    for (k in 0..5) {
        val mx = x0 + (x1 - x0) * k / 5f
        line(mx, top, mx, -0.14f, TRIM, 1.5f)
    }
    line(x0, -0.27f, x1, -0.27f, TRIM, 1.3f)
    d.drawRect(Ink.line, Offset(x0, top), Size(x1 - x0, -0.14f - top), style = Stroke(lw * 1.1f))
    box(GX0, -0.14f, GX1, 0f, TRIM, 1.1f)
    for (k in 0 until 5) d.drawRect(c(TRIM.darken(0.12f)), Offset(GX0 + 0.04f + k * 0.18f, -0.115f), Size(0.14f, 0.09f), style = Stroke(lw * 0.7f))
    // The glass roof with white rafters and a ridge.
    val ridgeY = ridge + vy / 2f
    val rp = path(GX0 - 0.04f, top, GX1 + 0.04f, top, GX1 + vx / 2f + 0.03f, ridgeY, GX0 + vx / 2f - 0.03f, ridgeY)
    fill(rp, Color(0xFFCDEBF6))
    if (lit > 0f) fill(rp, Color(0xFFFFE9A0), 0.35f * lit)
    val raf = ArrayList<Offset>()
    for (k in 0..9) {
        val f = k / 9f
        raf.add(Offset(mix(GX0 - 0.04f, GX1 + 0.04f, f), top))
        raf.add(Offset(mix(GX0 + vx / 2f - 0.03f, GX1 + vx / 2f + 0.03f, f), ridgeY))
    }
    d.drawPoints(raf, PointMode.Lines, c(TRIM), strokeWidth = lw * 1.1f)
    line(GX0 + 0.1f, top - 0.02f, GX0 + 0.3f, top - 0.09f, Color.White, 2.2f, 0.55f)
    ink(rp, 1.2f)
    line(GX0 + vx / 2f - 0.03f, ridgeY, GX1 + vx / 2f + 0.03f, ridgeY, TRIM, 2.2f)
    line(GX0 - 0.04f, top, GX1 + 0.04f, top, TRIM, 2.4f)
    if (snow) fill(rp, Color(0xFFF4F8FF), 0.8f)
}

/** The small greenhouse in front of the garden's end, with tomato plants and a rain barrel. */
private fun Bx.manorGreenhouse() {
    d.withTransform({ translate(1.88f, 0.14f) }) {
        val hw = 0.22f
        val dep = 0.28f
        val glassD = Color(0xFF8CC3D8)
        val pts = floatArrayOf(-hw, -0.2f, 0f, -0.38f, hw, -0.2f, hw, 0f, -hw, 0f)
        d.drawOval(Ink.line, Offset(-0.2f, 0.0f), Size(0.7f, 0.07f), alpha = 0.15f)
        prism(pts, dep, Color(0xFF2F6F5A), glassD, Color(0xFFCDEBF6))
        // Plants inside: two tomato vines with red fruit and a tray of seedlings.
        for (px in floatArrayOf(-0.09f, 0.08f)) {
            line(px, 0f, px, -0.21f, Color(0xFF3FA05A), 1.6f)
            for (k in 0 until 3) disc(px + (k - 1) * 0.04f, -0.07f - 0.05f * k, 0.026f, Color(0xFF58C46E), 0f)
            disc(px + 0.035f, -0.06f, 0.014f, Color(0xFFE0463A), 0f)
            disc(px - 0.03f, -0.12f, 0.014f, Color(0xFFE0463A), 0f)
        }
        d.drawRect(c(Color(0xFF8A5A3A)), Offset(-0.17f, -0.035f), Size(0.34f, 0.035f))
        // Glass tint, mullions, door and frame.
        val face = path(*pts)
        fill(face, lerp(c(GLASS), Color(0xFFFFE9A0), lit), 0.4f)
        for (mx in floatArrayOf(-hw / 2f, 0f, hw / 2f)) line(mx, 0f, mx, if (mx == 0f) -0.38f else -0.3f, TRIM, 1.2f)
        line(-hw, -0.1f, hw, -0.1f, TRIM, 1.1f)
        line(-hw, -0.2f, hw, -0.2f, TRIM, 1.2f)
        line(-0.05f, -0.28f, -0.05f, -0.2f, Color.White, 1.4f, 0.55f)
        ink(face, 1.1f)
        line(-hw, 0f, -hw, -0.2f, TRIM, 2f)
        line(hw, 0f, hw, -0.2f, TRIM, 2f)
        // Pots and a barrel beside it.
        potted(-0.34f, 0.04f, 0.1f, Color(0xFFFFC83D))
        barrel(0.4f, 0.0f, 0.04f)
    }
}

/** Hedges along the front edge of the lawn, the gate with its lanterns and a few flowers and a garden gnome. */
private fun Bx.manorFront() {
    fun edgeY(x: Float): Float {
        val e = (x - 0.15f) / 2.0f
        return 0.03f + 0.5f * sqrt(max(0f, 1f - e * e))
    }
    val hedge = if (snow) Color(0xFFCBD8E4) else m.pen.plant(Color(0xFF3F9A4E))
    val blooms = listOf(Color(0xFFFF8FB8), Color(0xFFFFFFFF), Color(0xFFFFC83D))
    var hx = -1.85f
    var hk = 0
    while (hx < 2.1f) {
        val inGap = hx > GATE_LX - 0.2f && hx < GATE_LX + 0.2f
        val onPond = hx > 0.2f && hx < 1.55f
        if (!inGap && !onPond) {
            val hy = edgeY(hx) - 0.075f
            val r = 0.075f
            val sh = crownPath(hx, hy, r * 1.1f, r * 0.85f, 50 + hk)
            fill(sh, hedge.darken(0.28f))
            fill(crownPath(hx - r * 0.12f, hy - r * 0.16f, r * 0.9f, r * 0.7f, 70 + hk), hedge)
            ink(sh, 0.8f, 0.8f)
            if (hk % 3 == 0) disc(hx + 0.02f, hy - 0.04f, 0.017f, blooms[(hk / 3) % 3], 0.5f)
        }
        hx += 0.15f
        hk++
    }
    // The gate: two stone pillars with lanterns on top, an iron arch between them, a hanging lantern.
    for (side in intArrayOf(-1, 1)) {
        val gx = GATE_LX + side * 0.17f
        box(gx - 0.04f, GATE_LY - 0.2f, gx + 0.04f, GATE_LY + 0.02f, Color(0xFFB9B4C4), 1f)
        d.drawRect(c(Color(0xFF8E8798)), Offset(gx + 0.01f, GATE_LY - 0.2f), Size(0.03f, 0.22f))
        box(gx - 0.055f, GATE_LY - 0.225f, gx + 0.055f, GATE_LY - 0.2f, Color(0xFFA7A1B5), 0.8f)
        round(gx - 0.03f, GATE_LY - 0.31f, gx + 0.03f, GATE_LY - 0.225f, 0.014f, BRASS, 0.8f)
        d.drawRect(lerp(c(Color(0xFFFFF3B0)), Color(0xFFFFE27A), lit), Offset(gx - 0.017f, GATE_LY - 0.295f), Size(0.034f, 0.055f))
        glow(gx, GATE_LY - 0.27f, 0.16f, Color(0xFFFFD76B), 0.3f)
    }
    val arch = Path().apply {
        moveTo(GATE_LX - 0.17f, GATE_LY - 0.2f)
        quadraticTo(GATE_LX, GATE_LY - 0.46f, GATE_LX + 0.17f, GATE_LY - 0.2f)
    }
    d.drawPath(arch, Ink.line, style = Stroke(lw * 2.6f, cap = StrokeCap.Round))
    d.drawPath(arch, c(BRASS.darken(0.15f)), style = Stroke(lw * 1.3f, cap = StrokeCap.Round))
    // A garden gnome by the pond, and bright flowers along the lawn.
    val gx = 0.3f
    val gy = 0.2f
    d.drawOval(Ink.line, Offset(gx - 0.05f, gy - 0.008f), Size(0.1f, 0.025f), alpha = 0.2f)
    oval(gx, gy - 0.045f, 0.035f, 0.045f, Color(0xFF4F7FD0), 0.7f)
    disc(gx, gy - 0.1f, 0.03f, Color(0xFFF2C29B), 0.6f)
    d.drawCircle(Color.White, 0.026f, Offset(gx, gy - 0.075f), alpha = 0.95f)
    fill(path(gx - 0.034f, gy - 0.11f, gx + 0.034f, gy - 0.11f, gx + 0.005f, gy - 0.2f), Color(0xFFE0463A))
    ink(path(gx - 0.034f, gy - 0.11f, gx + 0.034f, gy - 0.11f, gx + 0.005f, gy - 0.2f), 0.7f)
    for (k in 0 until 6) {
        val fx = -0.42f + k * 0.1f
        line(fx, 0.22f, fx, 0.16f, Color(0xFF3F8A4A), 1.2f)
        disc(fx, 0.145f, 0.022f, blooms[k % 3], 0.6f)
    }
    tufts(-0.9f, -0.2f, 1.6f, 1.95f, y = 0.4f, s = 0.05f)
}

// ------------------------------------------------------------------------------------- the live house

private val atticClip: Path by lazy { Path().apply { addOval(Rect(-0.098f, ATTIC_Y - 0.098f, 0.098f, ATTIC_Y + 0.098f)) } }

/** The windows the live layer lights at night, one after another: left, top, right, bottom (house space). */
private val liveWins = floatArrayOf(
    0.06f, -0.64f, 0.24f, -0.46f,
    0.32f, -0.64f, 0.5f, -0.46f,
    -1.1f, -0.62f, -0.99f, -0.46f,
)

/**
 * What moves in the estate: smoke, the flag and weathervane, the telescope, the glow of lanterns and of the
 * windows at night, ripples, a butterfly, the butler's glint and, now and then, the friendly ghost.
 */
internal fun Bx.manorLive() {
    d.withTransform({ translate(DX, 0f) }) {
        chimneySmoke(0.1f, -1.0f, 0.1f, 0.3f)
        chimneySmoke(-1.1f, -0.98f, 0.09f, 0.26f)
        // The weathervane on the gable: an arrow that turns with the wind.
        d.withTransform({ translate(BAY_X, BAY_DY) }) {
            val a = t * 0.7f + 1.8f * sin(t * 0.37f)
            val len = cos(a) * 0.15f
            val vx = len
            d.drawLine(Ink.line, Offset(-vx, -1.84f), Offset(vx, -1.84f), strokeWidth = lw * 3.2f, cap = StrokeCap.Round)
            d.drawLine(c(BRASS), Offset(-vx, -1.84f), Offset(vx, -1.84f), strokeWidth = lw * 1.6f, cap = StrokeCap.Round)
            val dir = if (len >= 0f) 1f else -1f
            d.drawLine(c(BRASS), Offset(vx, -1.84f), Offset(vx - dir * 0.04f, -1.87f), strokeWidth = lw * 1.4f, cap = StrokeCap.Round)
            d.drawLine(c(BRASS), Offset(vx, -1.84f), Offset(vx - dir * 0.04f, -1.81f), strokeWidth = lw * 1.4f, cap = StrokeCap.Round)
            ghost()
        }
        // The telescope in the dome slit pans slowly across the sky.
        d.withTransform({ rotate(LEAN, Offset(TOWER_X, 0f)) }) {
            val fx = TOWER_X + 0.37f
            val fy = -TOWER_H - 0.5f
            val wave = sin(t * 4f + 1.3f) * 0.02f
            val fl = m.scratch
            fl.rewind()
            fl.moveTo(fx, fy)
            fl.lineTo(fx + 0.3f, fy + 0.04f + wave)
            fl.lineTo(fx + 0.23f, fy + 0.075f + wave * 0.5f)
            fl.lineTo(fx + 0.3f, fy + 0.11f + wave)
            fl.lineTo(fx, fy + 0.11f)
            fl.close()
            d.drawPath(fl, c(Color(0xFF3FB59B)))
            d.drawPath(fl, Ink.line, style = Stroke(lw * 0.9f, join = StrokeJoin.Round))
            val ang = (-62f + 14f * sin(t * 0.33f)) * DEG
            val p0 = Offset(TOWER_X, DOME_Y - 0.12f)
            val p1 = Offset(TOWER_X + cos(ang) * 0.27f, DOME_Y - 0.12f + sin(ang) * 0.27f)
            d.drawLine(Ink.line, p0, p1, strokeWidth = lw * 4.6f, cap = StrokeCap.Round)
            d.drawLine(c(BRASS), p0, p1, strokeWidth = lw * 2.4f, cap = StrokeCap.Round)
            d.drawCircle(c(Color(0xFF2B2140)), 0.022f, p1)
            if (lit > 0f) {
                d.drawCircle(Color(0xFFFFE9A0), 0.3f, Offset(TOWER_X, DOME_Y + 0.1f), alpha = 0.12f * lit * (0.7f + 0.3f * sin(t * 1.3f)))
                m.twinkleLive(d, Offset(p1.x + 0.05f, p1.y - 0.05f), 0.06f, Color(0xFFFFF7DA), lit * max(0f, sin(t * 2.2f)))
            }
        }
        // At night the windows light up in turn, as if someone walked through the house.
        if (lit > 0.01f) {
            for (i in 0 until 3) {
                val a = ramp(5f * (0.8f - wrap(t * 0.05f + i * 0.33f, 1f)))
                if (a <= 0.01f) continue
                val x0 = liveWins[i * 4]
                val y0 = liveWins[i * 4 + 1]
                val x1 = liveWins[i * 4 + 2]
                val y1 = liveWins[i * 4 + 3]
                glow((x0 + x1) / 2f, (y0 + y1) / 2f, (x1 - x0) * 1.4f, Color(0xFFFFD76B), 0.25f * a)
                d.drawRect(Color(0xFFFFD76B), Offset(x0 + lw, y0 + lw), Size(x1 - x0 - 2f * lw, y1 - y0 - 2f * lw), alpha = a * lit)
                d.drawLine(c(TRIM), Offset((x0 + x1) / 2f, y0), Offset((x0 + x1) / 2f, y1), strokeWidth = lw * 1.3f)
                d.drawLine(c(TRIM), Offset(x0, (y0 + y1) / 2f), Offset(x1, (y0 + y1) / 2f), strokeWidth = lw * 1.3f)
            }
            // Flickering lanterns: the porch and the gate, and string lights along the winter garden.
            for (k in 0 until 4) {
                val fl2 = 0.8f + 0.2f * sin(t * 7.3f + k * 2.1f) + 0.1f * sin(t * 13f + k)
                val p = lanterns[k]
                d.drawCircle(Color(0xFFFFD76B), 0.15f * fl2, p, alpha = 0.3f * lit)
                d.drawCircle(Color(0xFFFFF3B0), 0.05f, p, alpha = 0.7f * lit)
            }
            d.drawPoints(fairyA, PointMode.Points, Color(0xFFFFE27A), strokeWidth = 0.034f, cap = StrokeCap.Round, alpha = lit * (0.5f + 0.5f * sin(t * 2.6f)))
            d.drawPoints(fairyB, PointMode.Points, Color(0xFFFF9EC4), strokeWidth = 0.034f, cap = StrokeCap.Round, alpha = lit * (0.5f + 0.5f * sin(t * 2.6f + 3.1f)))
        }
        // Ripples in the pond.
        val ph = wrap(t * 0.32f, 1f)
        val rr = 0.05f + 0.3f * ph
        d.drawOval(Color.White, Offset(1.05f - rr, 0.265f - rr * 0.22f), Size(rr * 2f, rr * 0.44f), alpha = 0.7f * (1f - ph), style = Stroke(lw))
        // The robot butler's glint, every few seconds, and the blinking light on his antenna.
        d.withTransform({ translate(BAY_X, BAY_DY) }) {
            val gx = 0.2f + 0.0f
            val gy = 0.06f
            val gp = wrap(t, 4.6f)
            if (gp < 0.5f) m.twinkleLive(d, Offset(gx - 0.03f, gy - 0.17f), 0.05f, Color.White, sin(gp / 0.5f * 3.1416f))
            val on = wrap(t, 1.2f) < 0.6f
            d.drawCircle(if (on) Color(0xFF6FF2FF) else Color(0xFFFF5A6E), 0.014f, Offset(gx, gy - 0.215f))
        }
        // A butterfly over the winter garden and the pond on dry, bright days.
        if (n < 0.5f && !m.rain) {
            val bx = 1.1f + 0.45f * sin(t * 0.4f) + 0.1f * sin(t * 1.3f)
            val by = -0.05f + 0.22f * sin(t * 0.55f + 1f) + 0.04f * sin(t * 3f)
            val flap = 0.25f + 0.75f * kotlin.math.abs(sin(t * 12f))
            d.drawOval(Ink.line, Offset(bx - 0.03f * flap - 0.012f, by - 0.02f), Size(0.03f * flap + 0.012f, 0.032f))
            d.drawOval(Color(0xFFFF8FB8), Offset(bx - 0.027f * flap - 0.01f, by - 0.018f), Size(0.025f * flap + 0.008f, 0.026f))
            d.drawOval(Ink.line, Offset(bx - 0.0f, by - 0.02f), Size(0.03f * flap + 0.012f, 0.032f))
            d.drawOval(Color(0xFFFFC83D), Offset(bx + 0.003f, by - 0.018f), Size(0.025f * flap + 0.008f, 0.026f))
        }
    }
}

/** The ghost in the round attic window (in the bay's space): a few seconds out of every half minute. */
private fun Bx.ghost() {
    val cyc = wrap(t + 13f, 29f)
    val u = (cyc - 23f) / 6f
    if (u <= 0f || u >= 1f) return
    val p = ramp(min(u * 6f, (1f - u) * 6f))
    val gx = 0.012f * sin(t * 1.7f)
    val gy = ATTIC_Y + (1f - p) * 0.2f + 0.016f * sin(t * 2.3f)
    run {
        d.clipPath(atticClip) {
            val body = Path().apply {
                moveTo(gx - 0.072f, gy + 0.16f)
                lineTo(gx - 0.072f, gy)
                arcTo(Rect(gx - 0.072f, gy - 0.072f, gx + 0.072f, gy + 0.072f), 180f, 180f, false)
                lineTo(gx + 0.072f, gy + 0.16f)
                close()
            }
            d.drawPath(body, Color(0xFFF7F8FF))
            d.drawPath(body, Ink.line, style = Stroke(lw * 1.1f, join = StrokeJoin.Round))
            val look = sin(t * 1.1f) * 0.008f
            d.drawOval(Ink.line, Offset(gx - 0.04f + look, gy - 0.03f), Size(0.026f, 0.04f))
            d.drawOval(Ink.line, Offset(gx + 0.014f + look, gy - 0.03f), Size(0.026f, 0.04f))
            d.drawCircle(Color.White, 0.006f, Offset(gx - 0.03f + look, gy - 0.022f))
            d.drawCircle(Color.White, 0.006f, Offset(gx + 0.024f + look, gy - 0.022f))
            d.drawCircle(Color(0xFFFF9EB8), 0.014f, Offset(gx - 0.05f, gy + 0.018f), alpha = 0.7f)
            d.drawCircle(Color(0xFFFF9EB8), 0.014f, Offset(gx + 0.05f, gy + 0.018f), alpha = 0.7f)
            d.drawArc(Ink.line, 20f, 140f, false, Offset(gx - 0.022f, gy + 0.004f), Size(0.044f, 0.03f), style = Stroke(lw * 0.9f, cap = StrokeCap.Round))
            // A little wave.
            val wv = sin(t * 9f) * 0.03f
            d.drawCircle(Color(0xFFF7F8FF), 0.026f, Offset(gx + 0.082f, gy + 0.06f + wv))
            d.drawCircle(Ink.line, 0.026f, Offset(gx + 0.082f, gy + 0.06f + wv), style = Stroke(lw * 0.9f))
        }
    }
    d.drawCircle(c(TRIM), 0.105f, Offset(0f, ATTIC_Y), style = Stroke(lw * 2.4f))
}

/** The four lanterns that flicker at night (the porch pair and the gate pair), in house space. */
private val lanterns: List<Offset> by lazy {
    val fvx = -0.5f * FWD
    val fvy = 0.36f * FWD
    listOf(
        Offset(BAY_X - 0.3f + fvx, BAY_DY - 0.32f + fvy), Offset(BAY_X + 0.3f + fvx, BAY_DY - 0.32f + fvy),
        Offset(GATE_LX - 0.17f, GATE_LY - 0.27f), Offset(GATE_LX + 0.17f, GATE_LY - 0.27f),
    )
}

/** The string lights under the winter garden's eaves, in two groups that twinkle in turn. */
private val fairyA: List<Offset> by lazy { fairy(0) }
private val fairyB: List<Offset> by lazy { fairy(1) }

private fun fairy(parity: Int): List<Offset> = (0 until 8).filter { it % 2 == parity }.map { k ->
    Offset(GX0 + 0.1f + (GX1 - GX0 - 0.2f) * k / 7f, GARDEN_EAVE + 0.06f + 0.03f * sin(k * 1.7f))
}
