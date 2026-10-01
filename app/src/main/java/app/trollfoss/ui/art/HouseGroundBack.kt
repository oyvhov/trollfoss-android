package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Season
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Storstova's background: six rooms of two units, each with its own look (hall: marble and walnut; living
 * room: warm and soft; library: dark wood and green; dining room: teal and ivory; kitchen: bright tiles;
 * winter garden: glass and green), and five arched openings between them. The geometry that never changes
 * is built once per scale ([Memo]) and drawn as a few paths; only what moves (candle light, the sun's
 * beams, butterflies) is drawn per frame. A room the child has papered or floored gives way to the child's
 * choice (wallpaper and floor from the designer), but keeps its rugs, its frieze and its arches.
 */

private val GB = PlaceId.MANOR_GROUND.back
private val GRC = recede(PlaceId.MANOR_GROUND.floor)
private val GRB = recede(GB)
private const val TH = 0.035f
private const val ROOM_N = 6

/** Where the back wall of room [i] begins (and where the previous room's ends). */
private fun wallL(i: Int): Float = if (i == 0) -9f else 2f * i - GRC + GRB

private fun wallR(i: Int): Float = if (i == ROOM_N - 1) 99f else wallL(i + 1)

/** The front edge of the floor of room [i]. */
private fun floorL(i: Int): Float = if (i == 0) -9f else 2f * i - GRC + TH

private fun floorR(i: Int): Float = if (i == ROOM_N - 1) 99f else 2f * (i + 1) - GRC

private fun hh(i: Int, salt: Int, a: Float = 0f, b: Float = 1f): Float = a + (b - a) * hash01(i, salt)

private fun Path.rectU(u: Float, x0: Float, y0: Float, x1: Float, y1: Float) = addRect(Rect(x0 * u, y0 * u, x1 * u, y1 * u))

private fun Path.chequer(u: Float, x0: Float, x1: Float, tile: Float, rows: Int, parity: Int) {
    val n = ((x1 - x0) / tile).toInt() + 1
    for (j in 0 until rows) {
        val yf = mix(FRONT_Y, GB, j / rows.toFloat())
        val yb = mix(FRONT_Y, GB, (j + 1) / rows.toFloat())
        for (i in 0 until n) {
            if ((i + j + parity) % 2 == 0) floorQuad(u, 0f, x0 + i * tile + recede(yf), x0 + (i + 1) * tile + recede(yf), yf, yb)
        }
    }
}

// ------------------------------------------------------------------------------------------------- geometry

private class HallG(
    val marble: Path, val veins: List<Offset>, val panels: Path, val panelsIn: Path, val frieze: List<Offset>,
    val rugOuter: Path, val rugField: Path, val rugInner: Path, val rugDots: List<Offset>,
)

private val hallG = Memo { u ->
    val marble = Path().apply { chequer(u, -0.66f, 2.5f, 0.22f, 4, 0) }
    val veins = ArrayList<Offset>(40)
    for (k in 0 until 18) {
        val x = hh(k, 801, -0.2f, 2.3f)
        val y = hh(k, 802, GB + 0.02f, 0.95f)
        val l = hh(k, 803, 0.04f, 0.09f)
        veins.add(Offset((x + recede(y)) * u, y * u))
        veins.add(Offset((x + l + recede(y) - 0.02f) * u, (y - 0.012f) * u))
    }
    val panels = Path()
    val panelsIn = Path()
    var x = -0.3f
    while (x < 2.4f) {
        panels.rectU(u, x, 0.545f, x + 0.29f, 0.775f)
        panelsIn.rectU(u, x + 0.025f, 0.57f, x + 0.265f, 0.75f)
        x += 0.34f
    }
    val frieze = ArrayList<Offset>(60)
    var fx = -0.2f
    while (fx < 2.5f) {
        frieze.add(Offset(fx * u, 0.037f * u))
        fx += 0.055f
    }
    val yf = 0.958f
    val yb = 0.868f
    val rugOuter = Path().apply { floorQuad(u, 0f, 0.40f, 2.0f, yf, yb) }
    val rugField = Path().apply { floorQuad(u, 0f, 0.44f, 1.96f, yf - 0.009f, yb + 0.012f) }
    val rugInner = Path().apply { floorQuad(u, 0f, 0.5f, 1.9f, yf - 0.018f, yb + 0.024f) }
    val dots = ArrayList<Offset>(10)
    val ym = (yf + yb) / 2f
    for (k in 0 until 8) dots.add(Offset((0.62f + k * 0.2f + recede(ym)) * u, ym * u))
    HallG(marble, veins, panels, panelsIn, frieze, rugOuter, rugField, rugInner, dots)
}

private class LivingG(
    val planks: Planks, val stripes: List<Offset>, val panels: Path,
    val rugOuter: Path, val rugField: Path, val rugInner: Path, val rugMotif: Path, val rugFringe: List<Offset>,
    val mat: Path, val matLines: Path,
)

private val livingG = Memo { u ->
    val stripes = ArrayList<Offset>(40)
    var x = 2.0f
    while (x < 4.3f) {
        stripes.add(Offset(x * u, 0.135f * u))
        stripes.add(Offset(x * u, 0.55f * u))
        x += 0.115f
    }
    val panels = Path()
    var px = 2.12f
    while (px < 4.2f) {
        panels.rectU(u, px, 0.62f, px + 0.27f, 0.775f)
        px += 0.32f
    }
    val yf = 0.972f
    val yb = 0.856f
    val outer = Path().apply { floorQuad(u, 0f, 2.55f, 3.82f, yf, yb) }
    val field = Path().apply { floorQuad(u, 0f, 2.6f, 3.77f, yf - 0.01f, yb + 0.014f) }
    val inner = Path().apply { floorQuad(u, 0f, 2.68f, 3.69f, yf - 0.022f, yb + 0.03f) }
    val motif = Path()
    val ym = (yf + yb) / 2f
    for (k in 0 until 5) {
        val cx = 2.9f + k * 0.21f + recede(ym)
        motif.moveTo((cx - 0.07f) * u, ym * u)
        motif.lineTo(cx * u, (ym - 0.03f) * u)
        motif.lineTo((cx + 0.07f) * u, ym * u)
        motif.lineTo(cx * u, (ym + 0.03f) * u)
        motif.close()
    }
    val fringe = ArrayList<Offset>(40)
    for (k in 0 until 14) {
        val fy = mix(yb, yf, k / 13f)
        fringe.add(Offset((2.55f + recede(fy)) * u, fy * u))
        fringe.add(Offset((2.535f + recede(fy)) * u, fy * u))
        fringe.add(Offset((3.82f + recede(fy)) * u, fy * u))
        fringe.add(Offset((3.835f + recede(fy)) * u, fy * u))
    }
    val mat = Path().apply { floorQuad(u, 0f, 3.5f, 4.0f, 0.972f, 0.912f) }
    val lines = Path().apply {
        for (k in 1..4) floorQuad(u, 0f, 3.5f + k * 0.1f - 0.008f, 3.5f + k * 0.1f + 0.008f, 0.972f, 0.912f)
    }
    LivingG(buildPlanks(u, 1.9f, 4.4f, GB, 0.09f, 21), stripes, panels, outer, field, inner, motif, fringe, mat, lines)
}

private class LibraryG(
    val planks: Planks, val diamonds: List<Offset>, val panels: Path,
    val rugOuter: Path, val rugField: Path, val rugInner: Path, val rugMedal: Path,
)

private val libraryG = Memo { u ->
    val diamonds = ArrayList<Offset>(200)
    var row = 0
    var y = 0.08f
    while (y < 0.54f) {
        var x = 3.9f + (row % 2) * 0.055f
        while (x < 6.4f) {
            diamonds.add(Offset(x * u, y * u))
            x += 0.11f
        }
        y += 0.055f
        row++
    }
    val panels = Path()
    var x = 4.0f
    while (x < 6.4f) {
        panels.rectU(u, x, 0.58f, x + 0.22f, 0.775f)
        x += 0.26f
    }
    val yf = 0.968f
    val yb = 0.858f
    val outer = Path().apply { floorQuad(u, 0f, 4.52f, 5.86f, yf, yb) }
    val field = Path().apply { floorQuad(u, 0f, 4.56f, 5.82f, yf - 0.01f, yb + 0.013f) }
    val inner = Path().apply { floorQuad(u, 0f, 4.64f, 5.74f, yf - 0.024f, yb + 0.03f) }
    val ym = (yf + yb) / 2f
    val medal = Path().apply { floorDisc(u, 0f, 5.19f + recede(ym), ym, 0.24f, 0.05f, 24) }
    LibraryG(buildPlanks(u, 3.9f, 6.4f, GB, 0.085f, 31), diamonds, panels, outer, field, inner, medal)
}

private class DiningG(
    val planks: Planks, val lattice: List<Offset>, val panels: Path, val panelsIn: Path,
    val rugOuter: Path, val rugField: Path, val rugMedal: Path, val rugFringe: List<Offset>,
)

private val diningG = Memo { u ->
    val lattice = ArrayList<Offset>(200)
    var row = 0
    var y = 0.17f
    while (y < 0.5f) {
        var x = 5.9f + (row % 2) * 0.07f
        while (x < 8.4f) {
            lattice.add(Offset(x * u, y * u))
            x += 0.14f
        }
        y += 0.06f
        row++
    }
    val panels = Path()
    val panelsIn = Path()
    var x = 5.95f
    while (x < 8.4f) {
        panels.rectU(u, x, 0.535f, x + 0.3f, 0.775f)
        panelsIn.rectU(u, x + 0.03f, 0.565f, x + 0.27f, 0.745f)
        x += 0.36f
    }
    val yf = 0.975f
    val yb = 0.846f
    val outer = Path().apply { floorQuad(u, 0f, 6.3f, 7.8f, yf, yb) }
    val field = Path().apply { floorQuad(u, 0f, 6.36f, 7.74f, yf - 0.012f, yb + 0.016f) }
    val ym = (yf + yb) / 2f
    val medal = Path().apply { floorDisc(u, 0f, 7.05f + recede(ym), ym, 0.42f, 0.06f, 28) }
    val fringe = ArrayList<Offset>(40)
    for (k in 0 until 16) {
        val fy = mix(yb, yf, k / 15f)
        fringe.add(Offset((6.3f + recede(fy)) * u, fy * u))
        fringe.add(Offset((6.28f + recede(fy)) * u, fy * u))
        fringe.add(Offset((7.8f + recede(fy)) * u, fy * u))
        fringe.add(Offset((7.82f + recede(fy)) * u, fy * u))
    }
    DiningG(buildPlanks(u, 5.9f, 8.4f, GB, 0.1f, 41), lattice, panels, panelsIn, outer, field, medal, fringe)
}

private class KitchenG(val tiles: Path, val dark: Path, val bricks: List<Offset>)

private val kitchenG = Memo { u ->
    val tiles = Path()
    var row = 0
    var y = 0.2f
    while (y < GB) {
        tiles.moveTo(8.0f * u, y * u)
        tiles.lineTo(10.4f * u, y * u)
        var x = 8.0f + (row % 2) * 0.05f
        while (x < 10.4f) {
            tiles.moveTo(x * u, y * u)
            tiles.lineTo(x * u, min(y + 0.04f, GB) * u)
            x += 0.1f
        }
        y += 0.04f
        row++
    }
    val dark = Path().apply { chequer(u, 7.7f, 10.5f, 0.21f, 4, 0) }
    // A few bricks showing round the pizza oven's chimney.
    val bricks = ArrayList<Offset>(24)
    KitchenG(tiles, dark, bricks)
}

private class GardenG(
    val frame: Path, val roof: Path, val tiles: Path, val grout: List<Offset>, val moss: Path,
    val hills: Path, val trees: Path, val treeCrowns: Path, val lawn: Path, val tufts: Path, val wall: Path,
)

private val GLASS_X0 = 10.14f
private val SILL = 0.6f

private val gardenG = Memo { u ->
    val frame = Path()
    val roof = Path()
    var m = 0
    var px = 10.2f
    while (px < 12.7f) {
        val c = px + 0.24f
        // The big pier and the arch over each pair of panes, a mullion in the middle and a transom.
        frame.moveTo(px * u, SILL * u)
        frame.lineTo(px * u, 0.3f * u)
        frame.moveTo((px) * u, 0.3f * u)
        frame.addArc(Rect((px) * u, 0.06f * u, (px + 0.48f) * u, 0.54f * u), 180f, 180f)
        frame.moveTo(c * u, SILL * u)
        frame.lineTo(c * u, 0.06f * u)
        frame.moveTo(px * u, 0.38f * u)
        frame.lineTo((px + 0.48f) * u, 0.38f * u)
        // Fan bars in the arch.
        for (k in 1..2) {
            val a = (PI * (1.0 - k / 3.0)).toFloat()
            frame.moveTo(c * u, 0.3f * u)
            frame.lineTo((c + cos(a) * 0.24f) * u, (0.3f - sin(a) * 0.24f) * u)
        }
        // The glass roof: a lattice of diagonals above the arches.
        roof.moveTo(px * u, 0.06f * u)
        roof.lineTo((px + 0.24f) * u, SKY_TOP * u)
        roof.moveTo((px + 0.48f) * u, 0.06f * u)
        roof.lineTo((px + 0.24f) * u, SKY_TOP * u)
        m++
        px += 0.48f
    }
    frame.moveTo(10.0f * u, SILL * u)
    frame.lineTo(12.8f * u, SILL * u)
    roof.moveTo(10.0f * u, 0.0f)
    roof.lineTo(12.8f * u, 0.0f)
    val tiles = Path().apply { chequer(u, 9.6f, 12.8f, 0.2f, 4, 1) }
    val grout = ArrayList<Offset>(60)
    for (k in 0..4) {
        val y = mix(FRONT_Y, GB, k / 4f)
        grout.add(Offset((9.6f + recede(y)) * u, y * u))
        grout.add(Offset((12.8f + recede(y)) * u, y * u))
    }
    var gx = 9.6f
    while (gx < 12.8f) {
        grout.add(Offset(gx * u, FRONT_Y * u))
        grout.add(Offset((gx + recede(GB)) * u, GB * u))
        gx += 0.2f
    }
    val moss = Path()
    for (k in 0 until 10) {
        val x = hh(k, 811, 10.2f, 12.3f)
        val y = hh(k, 812, GB + 0.03f, 0.96f)
        moss.addOval(Rect((x + recede(y) - 0.04f) * u, (y - 0.008f) * u, (x + recede(y) + 0.04f) * u, (y + 0.008f) * u))
    }
    // The view outside: hills, trees and a lawn.
    val hills = Path().apply {
        moveTo(9.8f * u, SILL * u)
        var x = 9.8f
        while (x <= 12.8f) {
            lineTo(x * u, (0.47f + 0.035f * sin(x * 2.3f) + 0.02f * sin(x * 5.1f + 1f)) * u)
            x += 0.08f
        }
        lineTo(12.8f * u, SILL * u)
        close()
    }
    val trees = Path()
    val crowns = Path()
    for (k in 0 until 9) {
        val x = 10.2f + k * 0.3f + hh(k, 821, -0.06f, 0.06f)
        val base = 0.56f + hh(k, 822, 0f, 0.03f)
        val h = hh(k, 823, 0.13f, 0.2f)
        trees.moveTo((x - 0.008f) * u, base * u)
        trees.lineTo((x + 0.008f) * u, base * u)
        trees.lineTo((x + 0.006f) * u, (base - h * 0.55f) * u)
        trees.lineTo((x - 0.006f) * u, (base - h * 0.55f) * u)
        trees.close()
        if (k % 3 == 1) {
            crowns.addPine(x * u, (base - h * 0.2f) * u, h * 0.55f * u, h * 1.1f * u)
        } else {
            crowns.addOval(Rect((x - h * 0.42f) * u, (base - h * 1.12f) * u, (x + h * 0.42f) * u, (base - h * 0.35f) * u))
            crowns.addOval(Rect((x - h * 0.6f) * u, (base - h * 0.9f) * u, (x - h * 0.05f) * u, (base - h * 0.3f) * u))
        }
    }
    val lawn = Path().apply {
        moveTo(9.8f * u, SILL * u)
        var x = 9.8f
        while (x <= 12.8f) {
            lineTo(x * u, (0.545f + 0.012f * sin(x * 3f + 2f)) * u)
            x += 0.1f
        }
        lineTo(12.8f * u, SILL * u)
        close()
    }
    val tufts = Path()
    for (k in 0 until 22) {
        val x = 10.1f + k * 0.12f + hh(k, 831, 0f, 0.08f)
        tufts.addPath(tuftPath(x * u, (0.575f + hh(k, 832, 0f, 0.03f)) * u, 0.014f * u, 0f))
    }
    val wall = Path().apply { rectU(u, 9.8f, SILL, 12.8f, GB) }
    GardenG(frame, roof, tiles, grout, moss, hills, trees, crowns, lawn, tufts, wall)
}

// ------------------------------------------------------------------------------------------------- the back layer

/** The place's back layer: six rooms and the openings between them. */
internal fun DrawScope.groundBack(st: Stage, pen: Pen, styles: List<RoomStyle>) {
    val u = st.u
    for (i in 0 until ROOM_N) {
        if (!st.sees(wallL(i), wallR(i) + 0.4f)) continue
        val paper = styles.wallOf(i)
        val floor = styles.floorOf(i)
        when (i) {
            0 -> grHall(st, pen, paper, floor)
            1 -> grLiving(st, pen, paper, floor)
            2 -> grLibrary(st, pen, paper, floor)
            3 -> grDining(st, pen, paper, floor)
            4 -> grKitchen(st, pen, paper, floor)
            else -> grGarden(st, pen, paper, floor)
        }
    }
    for (j in 1 until ROOM_N) grDivider(st, pen, j, styles.wallOf(j))
    drawBase(st, pen, Color(0xFFE9D6B0), Color(0xFF7B5A3E))
    // A warm lip along the front edge: the cut of the floor boards.
}

/** What lies in front of everything: the sun's beams from the glass, and butterflies in the winter garden. */
internal fun DrawScope.groundFront(st: Stage, pen: Pen) {
    val u = st.u
    val day = (1f - pen.night) * (1f - overcast(pen) * 0.6f)
    if (day > 0.05f && st.sees(10.1f, 12.4f)) {
        val c = Color(0xFFFFF1C2)
        for (k in 0 until 4) {
            val x0 = st.x(10.4f + k * 0.55f)
            val sway = sin(pen.t * 0.25f + k * 1.7f) * 0.01f * u
            val p = Path().apply {
                moveTo(x0 + sway, SKY_TOP * u)
                lineTo(x0 + 0.1f * u + sway, SKY_TOP * u)
                lineTo(x0 + 0.62f * u, FRONT_Y * u)
                lineTo(x0 + 0.46f * u, FRONT_Y * u)
                close()
            }
            drawPath(p, c, alpha = 0.06f * day)
        }
        // Butterflies between the plants.
        for (k in 0 until 3) {
            val bx = st.x(10.5f + k * 0.55f + sin(pen.t * 0.4f + k * 2f) * 0.3f)
            val by = (0.52f + 0.07f * sin(pen.t * 0.9f + k * 1.4f) + 0.012f * sin(pen.t * 3f + k)) * u
            if (bx < -0.05f * u || bx > st.w + 0.05f * u) continue
            val flap = kotlin.math.abs(cos(pen.t * 12f + k * 2f))
            val s = 0.011f * u
            val col = intArrayOf(0xFFFFC83D.toInt(), 0xFFFF8FB1.toInt(), 0xFFFFFFFF.toInt())[k]
            val wing = Path().apply {
                moveTo(bx, by)
                cubicTo(bx - s * 1.4f, by - s * (1.4f * flap + 0.1f), bx - s * 1.6f, by + s * 0.2f, bx, by + s * 0.3f)
                cubicTo(bx + s * 1.6f, by + s * 0.2f, bx + s * 1.4f, by - s * (1.4f * flap + 0.1f), bx, by)
                close()
            }
            drawPath(wing, Color(col))
            drawPath(wing, Ink.line, alpha = 0.7f, style = pen.thin)
            drawLine(Ink.line, Offset(bx, by - s * 0.2f), Offset(bx, by + s * 0.35f), strokeWidth = pen.lw, cap = StrokeCap.Round)
        }
    }
}

// ------------------------------------------------------------------------------------------------- helpers

private fun DrawScope.fillWall(st: Stage, xl: Float, xr: Float, y0: Float, y1: Float, color: Color) {
    val l = max(st.x(xl), -2f)
    val r = min(st.x(xr), st.w + 2f)
    val top = if (y0 <= SKY_TOP) st.backgroundTop else y0 * st.u
    if (r > l) drawRect(color, Offset(l, top), Size(r - l, y1 * st.u - top))
}

private fun DrawScope.fillWallBrush(st: Stage, xl: Float, xr: Float, y0: Float, y1: Float, top: Color, bottom: Color) {
    val l = max(st.x(xl), -2f)
    val r = min(st.x(xr), st.w + 2f)
    val upper = if (y0 <= SKY_TOP) st.backgroundTop else y0 * st.u
    if (r > l) drawRect(Brush.verticalGradient(listOf(top, bottom), startY = y0 * st.u, endY = y1 * st.u), Offset(l, upper), Size(r - l, y1 * st.u - upper))
}

/** Crown moulding, skirting and the shadow in the corner, for one room only. */
private fun DrawScope.roomTrim(st: Stage, pen: Pen, i: Int, crownColor: Color, skirtColor: Color) {
    val l = max(st.x(wallL(i)), 0f)
    val r = min(st.x(wallR(i)), st.w)
    if (r <= l) return
    clipRect(l, 0f - 0f, r, st.h) {
        crown(st, pen, crownColor)
        skirting(st, pen, GB, skirtColor)
    }
}

private fun DrawScope.roomFloorLight(st: Stage, pen: Pen, x: Float) {
    lightPatch(st, x, 0.2f, GB, pen.night)
}

private fun DrawScope.userWall(st: Stage, i: Int, paper: Int): Boolean {
    if (paper <= 0) return false
    paperWall(st, paper, GB, wallL(i), wallR(i))
    return true
}

private fun DrawScope.userFloor(st: Stage, i: Int, floor: Int): Boolean {
    if (floor <= 0) return false
    layFloor(st, floor, GB, floorL(i), floorR(i))
    return true
}

private fun DrawScope.inRoomFloor(st: Stage, i: Int, block: DrawScope.() -> Unit) {
    val l = max(st.x(floorL(i)), -2f)
    val r = min(st.x(floorR(i) + recede(GB)), st.w + 2f)
    if (r > l) clipRect(l, GB * st.u - 2f, r, FRONT_Y * st.u + 2f) { block() }
}

// ------------------------------------------------------------------------------------------------- the hall

private fun DrawScope.grHall(st: Stage, pen: Pen, paper: Int, floor: Int) {
    val u = st.u
    val g = hallG.of(u)
    val l = wallL(0)
    val r = wallR(0)
    if (!userWall(st, 0, paper)) {
        fillWallBrush(st, l, r, SKY_TOP, GB, Color(0xFFE6D8BA), Color(0xFFF3E9D3))
        // Gold frieze under the crown, and a chair rail where the walnut panelling begins.
        fillWall(st, l, r, 0.022f, 0.052f, Color(0xFFF2DFA8))
        inScene(st) { drawPoints(g.frieze, PointMode.Points, GrC.brass, strokeWidth = 0.011f * u, cap = StrokeCap.Round) }
        fillWall(st, l, r, 0.52f, GB, GrC.walnut)
        inScene(st) {
            drawPath(g.panels, GrC.walnutLight)
            drawPath(g.panels, Ink.line, alpha = 0.6f, style = pen.thin)
            drawPath(g.panelsIn, GrC.walnut.darken(0.08f))
            drawPath(g.panelsIn, Ink.line, alpha = 0.35f, style = pen.thin)
        }
        fillWall(st, l, r, 0.505f, 0.525f, GrC.walnutLight.lighten(0.12f))
        drawLine(Ink.line, st.o(l, 0.505f), st.o(r, 0.505f), strokeWidth = pen.lw * 0.7f)
        drawLine(Ink.line, st.o(l, 0.525f), st.o(r, 0.525f), strokeWidth = pen.lw * 0.7f)
    }
    roomTrim(st, pen, 0, Color(0xFFF2DFA8), GrC.walnutDark)
    if (!userFloor(st, 0, floor)) {
        inRoomFloor(st, 0) {
            drawRect(
                Brush.verticalGradient(listOf(Color(0xFFD7DAD3), GrC.marble), startY = GB * u, endY = FRONT_Y * u),
                Offset(0f, GB * u), Size(st.w, (FRONT_Y - GB) * u),
            )
            inScene(st) {
                drawPath(g.marble, GrC.marbleDark, alpha = 0.55f)
                drawPoints(g.veins, PointMode.Lines, GrC.marbleDark.darken(0.1f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round, alpha = 0.7f)
            }
        }
    }
    wallShadow(st, GB)
    // The red runner from the lift to the stairs.
    if (st.sees(0.3f, 2.1f)) {
        inScene(st) {
            drawPath(g.rugOuter, GrC.burgundy)
            drawPath(g.rugOuter, Ink.line, style = pen.thin)
            drawPath(g.rugField, GrC.burgundyLight)
            drawPath(g.rugInner, GrC.burgundy.darken(0.1f))
            drawPath(g.rugInner, GrC.brass, style = Stroke(pen.lw * 0.9f))
            drawPoints(g.rugDots, PointMode.Points, GrC.gold, strokeWidth = 0.012f * u, cap = StrokeCap.Round)
        }
    }
    // Coloured light from the stained glass window, lying on the floor.
    if (pen.night < 0.9f && st.sees(1.4f, 2.1f)) {
        val a = 0.2f * (1f - pen.night)
        val cols = intArrayOf(0xFFE8473F.toInt(), 0xFFFFC83D.toInt(), 0xFF2F9BFF.toInt())
        for (k in 0 until 3) {
            val p = Path().apply { floorQuad(u, st.cam, 1.5f + k * 0.12f, 1.58f + k * 0.12f, 0.95f - k * 0.012f, 0.9f - k * 0.012f) }
            drawPath(p, Color(cols[k]), alpha = a)
        }
    }
}

// ------------------------------------------------------------------------------------------------- the living room

private fun DrawScope.grLiving(st: Stage, pen: Pen, paper: Int, floor: Int) {
    val u = st.u
    val g = livingG.of(u)
    val l = wallL(1)
    val r = wallR(1)
    if (!userWall(st, 1, paper)) {
        fillWallBrush(st, l, r, SKY_TOP, 0.13f, Color(0xFFF1DDBB), Color(0xFFF7E6C8))
        fillWallBrush(st, l, r, 0.13f, GB, Color(0xFFEBBF94), Color(0xFFE5AE7F))
        inScene(st) { drawPoints(g.stripes, PointMode.Lines, Color(0xFFD9985F), strokeWidth = pen.lw * 0.7f, alpha = 0.45f) }
        // The picture rail, and a pale dado with soft panels below.
        fillWall(st, l, r, 0.125f, 0.145f, Color(0xFFFFF8EA))
        drawLine(Ink.line, st.o(l, 0.125f), st.o(r, 0.125f), strokeWidth = pen.lw * 0.6f)
        drawLine(Ink.line, st.o(l, 0.145f), st.o(r, 0.145f), strokeWidth = pen.lw * 0.6f)
        fillWall(st, l, r, 0.585f, GB, Color(0xFFF4E6CD))
        fillWall(st, l, r, 0.575f, 0.59f, Color(0xFFFFF8EA))
        drawLine(Ink.line, st.o(l, 0.575f), st.o(r, 0.575f), strokeWidth = pen.lw * 0.6f)
        inScene(st) {
            drawPath(g.panels, Color(0xFFE9D3AE))
            drawPath(g.panels, Ink.line, alpha = 0.4f, style = pen.thin)
        }
    }
    roomTrim(st, pen, 1, Color(0xFFFFF8EA), Color(0xFFFFF8EA))
    if (!userFloor(st, 1, floor)) {
        inRoomFloor(st, 1) { plankFloor(st, pen, g.planks, GB, Color(0xFFE0A56B)) }
    }
    wallShadow(st, GB)
    if (st.sees(2.5f, 3.9f)) {
        inScene(st) {
            drawPath(g.rugOuter, Color(0xFF2F5A8F))
            drawPath(g.rugOuter, Ink.line, style = pen.thin)
            drawPath(g.rugField, Color(0xFFF1E3C6))
            drawPath(g.rugInner, Color(0xFF3B6EA5))
            drawPath(g.rugMotif, Color(0xFFD9774F))
            drawPath(g.rugMotif, Ink.line, alpha = 0.6f, style = pen.thin)
            drawPoints(g.rugFringe, PointMode.Lines, Color(0xFFF1E3C6), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        }
    }
    // The soft mat where the slide from upstairs lands.
    if (st.sees(3.4f, 4.2f)) {
        inScene(st) {
            drawPath(g.mat, Color(0xFF59C4A5))
            drawPath(g.matLines, Color.White, alpha = 0.55f)
            drawPath(g.mat, Ink.line, style = pen.thin)
        }
    }
}

// ------------------------------------------------------------------------------------------------- the library

private fun DrawScope.grLibrary(st: Stage, pen: Pen, paper: Int, floor: Int) {
    val u = st.u
    val g = libraryG.of(u)
    val l = wallL(2)
    val r = wallR(2)
    if (!userWall(st, 2, paper)) {
        fillWallBrush(st, l, r, SKY_TOP, GB, Color(0xFF27403A), Color(0xFF335247))
        inScene(st) { drawPoints(g.diamonds, PointMode.Points, Color(0xFF3F6A57), strokeWidth = 0.013f * u, cap = StrokeCap.Round) }
        // Dark wood panelling to the chair rail, a gilded frieze under the crown.
        fillWall(st, l, r, 0.55f, GB, GrC.walnutDark)
        inScene(st) {
            drawPath(g.panels, GrC.walnut)
            drawPath(g.panels, Ink.line, alpha = 0.6f, style = pen.thin)
        }
        fillWall(st, l, r, 0.535f, 0.555f, GrC.walnutLight)
        drawLine(Ink.line, st.o(l, 0.535f), st.o(r, 0.535f), strokeWidth = pen.lw * 0.7f)
        drawLine(Ink.line, st.o(l, 0.555f), st.o(r, 0.555f), strokeWidth = pen.lw * 0.7f)
        fillWall(st, l, r, 0.0f, 0.07f, GrC.walnutDark)
        fillWall(st, l, r, 0.07f, 0.08f, GrC.brass)
    }
    roomTrim(st, pen, 2, GrC.walnut, GrC.walnutDark)
    if (!userFloor(st, 2, floor)) {
        inRoomFloor(st, 2) { plankFloor(st, pen, g.planks, GB, Color(0xFF8A5A3C)) }
    }
    wallShadow(st, GB)
    if (st.sees(4.4f, 6.0f)) {
        inScene(st) {
            drawPath(g.rugOuter, Color(0xFF2E5E4A))
            drawPath(g.rugOuter, Ink.line, style = pen.thin)
            drawPath(g.rugField, Color(0xFF8E2F3E))
            drawPath(g.rugInner, Color(0xFFB0485A))
            drawPath(g.rugInner, GrC.brass, style = Stroke(pen.lw * 0.8f))
            drawPath(g.rugMedal, Color(0xFFE8C77A))
            drawPath(g.rugMedal, Ink.line, alpha = 0.6f, style = pen.thin)
        }
    }
}

// ------------------------------------------------------------------------------------------------- the dining room

private fun DrawScope.grDining(st: Stage, pen: Pen, paper: Int, floor: Int) {
    val u = st.u
    val g = diningG.of(u)
    val l = wallL(3)
    val r = wallR(3)
    if (!userWall(st, 3, paper)) {
        fillWallBrush(st, l, r, SKY_TOP, GB, Color(0xFF2A6770), Color(0xFF357D86))
        inScene(st) { drawPoints(g.lattice, PointMode.Points, Color(0xFF6FB3B2), strokeWidth = 0.011f * u, cap = StrokeCap.Round, alpha = 0.8f) }
        // Gilded picture rail, ivory wainscot with raised panels.
        fillWall(st, l, r, 0.125f, 0.14f, GrC.brass)
        drawLine(Ink.line, st.o(l, 0.125f), st.o(r, 0.125f), strokeWidth = pen.lw * 0.6f)
        drawLine(Ink.line, st.o(l, 0.14f), st.o(r, 0.14f), strokeWidth = pen.lw * 0.6f)
        fillWall(st, l, r, 0.5f, GB, GrC.ivory)
        fillWall(st, l, r, 0.49f, 0.505f, GrC.brass)
        drawLine(Ink.line, st.o(l, 0.49f), st.o(r, 0.49f), strokeWidth = pen.lw * 0.6f)
        inScene(st) {
            drawPath(g.panels, GrC.cream)
            drawPath(g.panels, Ink.line, alpha = 0.5f, style = pen.thin)
            drawPath(g.panelsIn, GrC.ivory)
            drawPath(g.panelsIn, Color(0xFFD7C9A4), style = pen.thin)
        }
    }
    roomTrim(st, pen, 3, GrC.brass.lighten(0.2f), GrC.ivory)
    // The tall window behind the table, with its curtains.
    if (paper <= 0) grDiningWindow(st, pen)
    if (!userFloor(st, 3, floor)) {
        inRoomFloor(st, 3) { plankFloor(st, pen, g.planks, GB, Color(0xFFE6C28E)) }
    }
    wallShadow(st, GB)
    if (st.sees(6.2f, 8.0f)) {
        inScene(st) {
            drawPath(g.rugOuter, GrC.brass)
            drawPath(g.rugOuter, Ink.line, style = pen.thin)
            drawPath(g.rugField, Color(0xFFF7EBD0))
            drawPath(g.rugMedal, Color(0xFF8DC9C4))
            drawPath(g.rugMedal, Ink.line, alpha = 0.5f, style = pen.thin)
            drawPoints(g.rugFringe, PointMode.Lines, GrC.cream, strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.grDiningWindow(st: Stage, pen: Pen) {
    if (!st.sees(6.6f, 7.5f)) return
    val u = st.u
    val x0 = 6.75f
    val x1 = 7.4f
    val top = 0.17f
    val bottom = 0.47f
    val arch = archPath(st.x(x0), st.x(x1), bottom * u, 0.3f * u, top * u)
    val night = pen.night
    drawPath(arch, lerp(Color(0xFF8FD0F2), Color(0xFF1C2358), night))
    drawPath(arch, Brush.verticalGradient(listOf(Color.Transparent, lerp(Color(0xFFE6F6FF), Color(0xFF3B3A82), night)), startY = top * u, endY = bottom * u))
    // Hills and trees far off.
    clipPath(arch) {
        drawOval(lerp(Color(0xFF7FC27A), Color(0xFF254A4A), night), Offset(st.x(x0 - 0.1f), (bottom - 0.07f) * u), Size((x1 - x0 + 0.2f) * u, 0.2f * u))
    }
    drawPath(arch, Ink.line, style = pen.stroke)
    // Frame bars.
    val cx = st.x((x0 + x1) / 2f)
    drawLine(Color.White, Offset(cx, top * u), Offset(cx, bottom * u), strokeWidth = pen.lw * 1.4f)
    drawLine(Color.White, Offset(st.x(x0), 0.34f * u), Offset(st.x(x1), 0.34f * u), strokeWidth = pen.lw * 1.4f)
    drawPath(arch, Color.White, style = Stroke(pen.lw * 2.4f))
    // Sill and drapes.
    inkedRound(Rect(st.x(x0 - 0.03f), bottom * u, st.x(x1 + 0.03f), (bottom + 0.016f) * u), 0.004f * u, GrC.ivory, pen, shade = false)
    for (s in 0..1) {
        val sx = if (s == 0) x0 - 0.06f else x1 + 0.06f
        val dir = if (s == 0) -1f else 1f
        val sway = sin(pen.t * 0.5f + s) * 0.003f * u
        val drape = Path().apply {
            moveTo(st.x(sx - 0.05f), (top - 0.02f) * u)
            lineTo(st.x(sx + 0.05f), (top - 0.02f) * u)
            quadraticTo(st.x(sx + 0.05f + dir * 0.03f) + sway, 0.3f * u, st.x(sx + 0.045f), 0.5f * u)
            lineTo(st.x(sx - 0.045f), 0.5f * u)
            quadraticTo(st.x(sx - 0.05f + dir * 0.03f) + sway, 0.3f * u, st.x(sx - 0.05f), (top - 0.02f) * u)
            close()
        }
        inked(drape, Color(0xFFE8C77A), pen)
        for (k in 1..3) drawLine(Color(0xFFB8963A), Offset(st.x(sx - 0.05f + k * 0.025f), (top - 0.01f) * u), Offset(st.x(sx - 0.05f + k * 0.025f + dir * 0.012f), 0.49f * u), strokeWidth = pen.lw * 0.6f)
    }
    // The gilded pole.
    drawLine(Ink.line, Offset(st.x(x0 - 0.12f), (top - 0.02f) * u), Offset(st.x(x1 + 0.12f), (top - 0.02f) * u), strokeWidth = pen.lw * 3.4f, cap = StrokeCap.Round)
    drawLine(GrC.brass, Offset(st.x(x0 - 0.12f), (top - 0.02f) * u), Offset(st.x(x1 + 0.12f), (top - 0.02f) * u), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
}

// ------------------------------------------------------------------------------------------------- the kitchen

private fun DrawScope.grKitchen(st: Stage, pen: Pen, paper: Int, floor: Int) {
    val u = st.u
    val g = kitchenG.of(u)
    val l = wallL(4)
    val r = wallR(4)
    if (!userWall(st, 4, paper)) {
        fillWallBrush(st, l, r, SKY_TOP, 0.2f, Color(0xFFD3EEE0), Color(0xFFCBEBDC))
        fillWall(st, l, r, 0.2f, GB, Color(0xFFFBFDFB))
        inScene(st) { drawPath(g.tiles, Color(0xFFB7CCCB), style = Stroke(pen.lw * 0.6f)) }
        // A mint band where the tiles begin, and a darker one at the foot.
        fillWall(st, l, r, 0.19f, 0.21f, Color(0xFF7FD3B0))
        drawLine(Ink.line, st.o(l, 0.19f), st.o(r, 0.19f), strokeWidth = pen.lw * 0.7f)
        drawLine(Ink.line, st.o(l, 0.21f), st.o(r, 0.21f), strokeWidth = pen.lw * 0.7f)
        fillWall(st, l, r, 0.77f, GB, Color(0xFF7FD3B0))
    }
    roomTrim(st, pen, 4, Color(0xFF9DE0C4), Color(0xFF5DB892))
    if (paper <= 0) grPotRack(st, pen)
    if (!userFloor(st, 4, floor)) {
        inRoomFloor(st, 4) {
            drawRect(Color(0xFFFFF8EC), Offset(0f, GB * u), Size(st.w, (FRONT_Y - GB) * u))
            inScene(st) { drawPath(g.dark, Color(0xFF3F4A58), alpha = 0.92f) }
            drawRect(
                Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.16f), Ink.line.copy(alpha = 0f)), startY = GB * u, endY = FRONT_Y * u),
                Offset(0f, GB * u), Size(st.w, (FRONT_Y - GB) * u),
            )
        }
    }
    wallShadow(st, GB)
    lightPatch(st, 9.64f, 0.22f, GB, pen.night)
}

/** Copper pots hanging from a bar under the ceiling. */
private fun DrawScope.grPotRack(st: Stage, pen: Pen) {
    if (!st.sees(8.4f, 9.4f)) return
    val u = st.u
    val x0 = 8.55f
    val x1 = 9.3f
    val y = 0.1f
    for (cx in floatArrayOf(x0 + 0.05f, x1 - 0.05f)) drawLine(Ink.line, st.o(cx, 0.0f), st.o(cx, y), strokeWidth = pen.lw)
    drawLine(Ink.line, st.o(x0, y), st.o(x1, y), strokeWidth = pen.lw * 3.4f, cap = StrokeCap.Round)
    drawLine(GrC.brass, st.o(x0, y), st.o(x1, y), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
    val copper = Color(0xFFD9774F)
    for (k in 0 until 4) {
        val px = x0 + 0.1f + k * 0.17f
        val sway = sin(pen.t * 0.8f + k * 1.3f) * 0.004f * u
        val c = st.o(px, y)
        drawLine(Ink.line, Offset(c.x, c.y), Offset(c.x + sway, c.y + 0.03f * u), strokeWidth = pen.lw * 0.8f)
        val r = (0.034f - (k % 2) * 0.008f) * u
        val pot = Rect(c.x + sway - r, c.y + 0.03f * u, c.x + sway + r, c.y + 0.03f * u + r * 1.15f)
        inkedRound(pot, r * 0.3f, if (k % 2 == 0) copper else copper.lighten(0.15f), pen)
        shine(Offset(pot.left + r * 0.45f, pot.top + r * 0.45f), r * 0.28f, r * 0.5f, 0.6f)
    }
}

// ------------------------------------------------------------------------------------------------- the winter garden

private data class GgView(val sky: Color, val skyLow: Color, val hill: Color, val crown: Color, val lawn: Color, val trunk: Color)

private fun ggView(pen: Pen): GgView {
    val day = when (pen.season) {
        Season.WINTER -> GgView(Color(0xFF9DC7EA), Color(0xFFEAF3FA), Color(0xFFDCE8F4), Color(0xFFEFF5FB), Color(0xFFF2F6FB), Color(0xFF6E5A4E))
        Season.AUTUMN -> GgView(Color(0xFF7FB4E4), Color(0xFFFFE6BD), Color(0xFFC9A455), Color(0xFFE0883A), Color(0xFFB09A45), Color(0xFF6E4630))
        Season.SPRING -> GgView(Color(0xFF68B8F0), Color(0xFFE8F8FF), Color(0xFF8ED086), Color(0xFFF2B6CC), Color(0xFF7CCB66), Color(0xFF6E4630))
        Season.SUMMER -> GgView(Color(0xFF58B0EC), Color(0xFFE2F5EC), Color(0xFF7FC27A), Color(0xFF3F9A55), Color(0xFF6FAE5A), Color(0xFF6E4630))
    }
    val n = pen.night
    if (n <= 0f) return day
    val night = Color(0xFF1C2358)
    return GgView(
        lerp(day.sky, Color(0xFF100D35), n), lerp(day.skyLow, Color(0xFF55409A), n), lerp(day.hill, Color(0xFF254A4A), n * 0.8f),
        lerp(day.crown, Color(0xFF1E5A3E), n * 0.75f), lerp(day.lawn, night, n * 0.7f), day.trunk.atNight(n),
    )
}

private fun DrawScope.grGarden(st: Stage, pen: Pen, paper: Int, floor: Int) {
    val u = st.u
    val g = gardenG.of(u)
    val l = wallL(5)
    val pal = ggView(pen)
    if (!st.sees(l, l + 3.2f)) return
    val glassTop = SKY_TOP
    // The view outside the glass.
    val skyL = max(st.x(l), -2f)
    val skyR = min(st.x(12.9f), st.w + 2f)
    if (skyR > skyL) {
        drawRect(
            Brush.verticalGradient(0f to pal.sky, 1f to pal.skyLow, startY = glassTop * u, endY = SILL * u),
            Offset(skyL, glassTop * u), Size(skyR - skyL, (SILL - glassTop) * u),
        )
        clipRect(skyL, glassTop * u, skyR, SILL * u) {
            // Sun and moon.
            if (pen.night < 0.8f) drawSun(st.o(11.3f, 0.2f), 0.04f * u, pen, vis = 1f - pen.night, rays = false)
            if (pen.night > 0.2f) drawMoon(st.o(11.8f, 0.2f), 0.035f * u, pen, ramp((pen.night - 0.2f) / 0.6f))
            drawStars(st, pen, 0.55f, 26)
            drawClouds(st, pen, 0.0f, 0.3f, 3, 0.05f, 91)
            inScene(st) {
                drawPath(g.hills, pal.hill)
                drawPath(g.trees, pal.trunk)
                drawPath(g.treeCrowns, pal.crown)
                if (pen.season == Season.SPRING || pen.season == Season.AUTUMN) drawPath(g.treeCrowns, Ink.line, alpha = 0.2f, style = pen.thin)
                drawPath(g.lawn, pal.lawn)
                drawPath(g.tufts, pal.lawn.darken(0.12f))
            }
        }
    }
    // The low stone wall under the glass, panelled in soft green.
    fillWallBrush(st, l - 0.2f, 12.9f, SILL, GB, Color(0xFFD6DFCF), Color(0xFFBFCDB8))
    var px = 10.2f
    while (px < 12.6f) {
        if (st.sees(px, px + 0.46f)) {
            val r = Rect(st.x(px + 0.03f), (SILL + 0.025f) * u, st.x(px + 0.45f), (GB - 0.03f) * u)
            inkedRound(r, 0.01f * u, Color(0xFFE6EBDD), pen, shade = false)
        }
        px += 0.48f
    }
    // The glazing bars and the roof lattice.
    inScene(st) {
        drawPath(g.roof, Ink.line, style = Stroke(0.0075f * u + pen.lw * 2f))
        drawPath(g.frame, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f))
        drawPath(g.roof, Color(0xFFF2F6EE), style = Stroke(0.0075f * u))
        drawPath(g.frame, Color(0xFFF2F6EE), style = Stroke(0.009f * u))
    }
    roomTrim(st, pen, 5, Color(0xFFF2F6EE), Color(0xFFD6DFCF))
    if (!userFloor(st, 5, floor)) {
        inRoomFloor(st, 5) {
            drawRect(Color(0xFFD08A62), Offset(0f, GB * u), Size(st.w, (FRONT_Y - GB) * u))
            inScene(st) {
                drawPath(g.tiles, Color(0xFFBA6F48))
                drawPoints(g.grout, PointMode.Lines, Color(0xFF8E4F32), strokeWidth = pen.lw * 0.7f)
                drawPath(g.moss, Color(0xFF5E9A55), alpha = 0.5f)
            }
            drawRect(
                Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.2f), Ink.line.copy(alpha = 0f)), startY = GB * u, endY = FRONT_Y * u),
                Offset(0f, GB * u), Size(st.w, (FRONT_Y - GB) * u),
            )
        }
    }
    wallShadow(st, GB)
}

// ------------------------------------------------------------------------------------------------- the openings

/** What each opening looks like: where it begins and ends along the wall's depth, how high it is, and whether it has an arch. */
private class Opening(val d0: Float, val d1: Float, val h: Float, val arch: Boolean, val casing: Color, val glazed: Boolean = false, val columns: Boolean = false)

private val OPENINGS = arrayOf(
    Opening(0.1f, 0.9f, 0.5f, true, Color(0xFFF2EEE6), columns = true),
    Opening(0.08f, 0.92f, 0.46f, false, Color(0xFFB0794F)),
    Opening(0.14f, 0.86f, 0.5f, true, Color(0xFF8A5A3C)),
    Opening(0.2f, 0.8f, 0.46f, false, Color(0xFFFBF6EA)),
    Opening(0.08f, 0.92f, 0.52f, true, Color(0xFFF2F6EE), glazed = true),
)

private val FACE_COLORS = intArrayOf(0xFFEBBF94.toInt(), 0xFF335247.toInt(), 0xFF357D86.toInt(), 0xFFFBFDFB.toInt(), 0xFFD6DFCF.toInt())

/**
 * The thin wall between room j - 1 and room j, from the front edge back to the back wall, with an opening
 * in it. We see its right face (room j's side wall, with wallpaper [paper] if the child has chosen one),
 * its cut front edge and its top.
 */
private fun DrawScope.grDivider(st: Stage, pen: Pen, j: Int, paper: Int) {
    val u = st.u
    val xf = 2f * j - GRC
    val shift = GRB
    if (!st.sees(xf - 0.05f, xf + shift + 0.15f)) return
    val op = OPENINGS[j - 1]
    val face = Color(FACE_COLORS[j - 1])
    val edge = Color(0xFFF7F3EC)
    val hFull = GB - SKY_TOP
    fun px(f: Float, dx: Float = 0f) = st.x(xf + shift * f + dx)
    fun py(f: Float, up: Float = 0f) = (mix(FRONT_Y, GB, f) - up) * u
    // The opening: straight up, then an arch (a half circle in the plane of the wall) or a flat top.
    val depthUnits = (FRONT_Y - GB) / -Oblique.DY
    val fr = (op.d1 - op.d0) / 2f
    val radius = if (op.arch) depthUnits * fr else 0f
    val straight = op.h - radius
    val hole = ArrayList<Offset>(24)
    hole.add(Offset(px(op.d0, TH), py(op.d0)))
    hole.add(Offset(px(op.d0, TH), py(op.d0, straight)))
    if (op.arch) {
        for (k in 1 until 14) {
            val t = PI.toFloat() * k / 14f
            val f = op.d0 + (op.d1 - op.d0) * (1f - cos(t)) / 2f
            hole.add(Offset(px(f, TH), py(f, straight + radius * sin(t))))
        }
    }
    hole.add(Offset(px(op.d1, TH), py(op.d1, straight)))
    hole.add(Offset(px(op.d1, TH), py(op.d1)))
    val facePath = Path().apply {
        fillType = PathFillType.EvenOdd
        moveTo(px(0f, TH), py(0f, hFull))
        lineTo(px(1f, TH), py(1f, hFull))
        lineTo(px(1f, TH), py(1f))
        lineTo(px(0f, TH), py(0f))
        close()
        moveTo(hole[0].x, hole[0].y)
        for (k in 1 until hole.size) lineTo(hole[k].x, hole[k].y)
        close()
    }
    if (paper > 0) {
        drawWallpaperSlanted(paper, facePath, px(0f, TH), u)
        drawPath(facePath, Brush.horizontalGradient(listOf(Ink.line.copy(alpha = 0.26f), Ink.line.copy(alpha = 0.1f)), startX = px(0f), endX = px(1f)))
    } else {
        drawPath(facePath, Brush.horizontalGradient(listOf(face.darken(0.2f), face.darken(0.06f)), startX = px(0f), endX = px(1f)))
        // Panelling on the face, in the room's own wainscot colour.
        val wain = floatArrayOf(0.5f, 0.55f, 0.5f, 0.5f, 0.2f)[j - 1]
        val wc = intArrayOf(0xFFB0794F.toInt(), 0xFF4A2E22.toInt(), 0xFFF2EEE6.toInt(), 0xFFFBFDFB.toInt(), 0xFFD6DFCF.toInt())[j - 1]
        val h = min(GB - 0.2f - wain, 1f)
        if (j != 4) {
            val low = 0.78f - wain
            val lower = Path().apply {
                poly(px(0f, TH), py(0f, low), px(op.d0, TH), py(op.d0, low), px(op.d0, TH), py(op.d0), px(0f, TH), py(0f))
                poly(px(op.d1, TH), py(op.d1, low), px(1f, TH), py(1f, low), px(1f, TH), py(1f), px(op.d1, TH), py(op.d1))
            }
            drawPath(lower, Color(wc).darken(0.22f))
        }
    }
    // Skirting along the foot of the wall, either side of the opening.
    val skirt = Path().apply {
        poly(px(0f, TH), py(0f, 0.024f), px(op.d0, TH), py(op.d0, 0.024f), px(op.d0, TH), py(op.d0), px(0f, TH), py(0f))
        poly(px(op.d1, TH), py(op.d1, 0.024f), px(1f, TH), py(1f, 0.024f), px(1f, TH), py(1f), px(op.d1, TH), py(op.d1))
    }
    drawPath(skirt, edge.darken(0.08f))
    drawPath(facePath, Ink.line, style = pen.stroke)
    // Pilasters of marble on both sides of the grand opening.
    if (op.columns) {
        for (side in 0..1) {
            val f0 = if (side == 0) op.d0 - 0.075f else op.d1
            val f1 = f0 + 0.075f
            val colH = straight + 0.02f
            val col = Path().apply { poly(px(f0, TH), py(f0, colH), px(f1, TH), py(f1, colH), px(f1, TH), py(f1), px(f0, TH), py(f0)) }
            drawPath(col, GrC.marble)
            drawPath(col, Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.3f), Ink.line.copy(alpha = 0.14f)), startX = px(f0), endX = px(f1)))
            drawPath(col, Ink.line, style = pen.thin)
            for (k in 1..3) drawLine(Ink.line.copy(alpha = 0.3f), Offset(px(mix(f0, f1, k / 4f), TH), py(mix(f0, f1, k / 4f), 0.03f)), Offset(px(mix(f0, f1, k / 4f), TH), py(mix(f0, f1, k / 4f), colH - 0.03f)), strokeWidth = pen.lw * 0.5f)
            // Capital and base in brass.
            val cap = Path().apply { poly(px(f0 - 0.008f, TH), py(f0, colH + 0.02f), px(f1 + 0.008f, TH), py(f1, colH + 0.02f), px(f1 + 0.008f, TH), py(f1, colH - 0.004f), px(f0 - 0.008f, TH), py(f0, colH - 0.004f)) }
            drawPath(cap, GrC.brass)
            drawPath(cap, Ink.line, style = pen.thin)
        }
    }
    // The casing round the opening.
    val casing = Path().apply {
        moveTo(hole[0].x, hole[0].y)
        for (k in 1 until hole.size) lineTo(hole[k].x, hole[k].y)
    }
    drawPath(casing, op.casing, style = Stroke(0.014f * u, cap = StrokeCap.Round))
    drawPath(casing, Ink.line, style = pen.thin)
    // A keystone on an arch.
    if (op.arch) {
        val fm = (op.d0 + op.d1) / 2f
        val top = Offset(px(fm, TH), py(fm, straight + radius))
        val key = Path().apply {
            poly(top.x - 0.012f * u, top.y - 0.012f * u, top.x + 0.012f * u, top.y - 0.012f * u, top.x + 0.008f * u, top.y + 0.014f * u, top.x - 0.008f * u, top.y + 0.014f * u)
        }
        drawPath(key, GrC.brass)
        drawPath(key, Ink.line, style = pen.thin)
    }
    // Glass in the opening of the last one: a pale glaze, iron bars, and light.
    if (op.glazed) {
        val glass = Path().apply {
            moveTo(hole[0].x, hole[0].y)
            for (k in 1 until hole.size) lineTo(hole[k].x, hole[k].y)
            close()
        }
        drawPath(glass, Color(0xFFD7F2E8), alpha = 0.28f)
        for (k in 1..3) {
            val f = op.d0 + (op.d1 - op.d0) * k / 4f
            drawLine(Color(0xFFF2F6EE), Offset(px(f, TH), py(f)), Offset(px(f, TH), py(f, straight + radius * sin(PI.toFloat() * k / 4f))), strokeWidth = pen.lw * 1.4f)
        }
        for (k in 1..2) {
            val up = straight * k / 3f
            drawLine(Color(0xFFF2F6EE), Offset(px(op.d0, TH), py(op.d0, up)), Offset(px(op.d1, TH), py(op.d1, up)), strokeWidth = pen.lw * 1.4f)
        }
        drawPath(glass, Ink.line, alpha = 0.6f, style = pen.thin)
    }
    // Cut front edge and top.
    val front = Path().apply { poly(px(0f), py(0f, hFull), px(0f, TH), py(0f, hFull), px(0f, TH), py(0f), px(0f), py(0f)) }
    drawPath(front, edge)
    drawPath(front, Ink.line, style = pen.stroke)
    val topFace = Path().apply { poly(px(0f), py(0f, hFull), px(1f), py(1f, hFull), px(1f, TH), py(1f, hFull), px(0f, TH), py(0f, hFull)) }
    drawPath(topFace, edge.lighten(0.3f))
    drawPath(topFace, Ink.line, style = pen.stroke)
}
