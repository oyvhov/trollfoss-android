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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.UpperFloor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The six rooms of the upper floor as one long cut-away house in oblique 3D. Every room has its own look and respects the
 * wallpaper and floor the child chooses in the home designer (`styles`): the landing is a gallery with warm wood and
 * a red runner, the children's room a painted meadow with foam mats, the playroom bright with bunting, the bathroom white
 * tiles, the bedroom soft with stars, and the balcony is outdoors with a view over the village and the fjord.
 * Everything that never changes is built once per scale (see [upStatic]) and drawn with a translate.
 */

private val FLOOR = PlaceId.MANOR_UPPER.floor
private val BACK = PlaceId.MANOR_UPPER.back
private const val TH = 0.035f

/** The x of the dividers between the rooms, on the furniture line. */
private val DIVIDERS = floatArrayOf(3.3f, 5.2f, 8.2f, 9.4f, 11.0f)

/** Where room [i]'s back wall begins and ends (a divider meets the back wall further right than it stands at the front). */
private fun roomEdge(i: Int): Float = when (i) {
    0 -> -1f
    6 -> 14f
    else -> DIVIDERS[i - 1] - recede(FLOOR) + recede(BACK)
}

/** Front-edge x range of room [i]'s floor. */
private fun floorFrom(i: Int): Float = if (i == 0) -9f else DIVIDERS[i - 1] - recede(FLOOR) + TH
private fun floorTo(i: Int): Float = if (i == 5) 99f else DIVIDERS[i] - recede(FLOOR)

private class UpStatic(
    val planks: Planks,
    // Landing
    val diamonds: List<Offset>, val panels: Path, val runnerOrnaments: List<Offset>,
    // Children's room
    val hillFar: Path, val hillNear: Path, val clouds: Path, val sunRays: List<Offset>, val tiles: List<Path>, val tileLines: List<Offset>,
    val flowers: List<Offset>,
    // Playroom
    val dots: List<Path>, val bunting: Path, val flags: List<Path>, val playLines: List<Offset>, val playDots: List<Path>,
    // Bathroom
    val bathTiles: List<Offset>, val bathFloor: List<Offset>, val bathBorder: List<Offset>,
    // Bedroom
    val stars: List<Offset>, val bigStars: List<Path>, val moon: Path, val carpetDots: List<Offset>,
    // Balcony
    val deck: List<Offset>, val deckBoards: Path,
)

private val flagColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFC83D), Color(0xFF5ED1A4), Color(0xFFFF9EC4), Color(0xFF5AA9E6))

private val upStatic = Memo { u ->
    val planks = buildPlanks(u, -0.3f, 3.4f, BACK, 0.12f, 71)
    // The landing's wallpaper: a lattice of small diamonds.
    val diamonds = ArrayList<Offset>(1600)
    var row = 0
    var y = SKY_TOP + 0.03f
    while (y < 0.55f) {
        var x = -0.1f + (row % 2) * 0.07f
        while (x < 3.8f) {
            val r = 0.022f
            diamonds.add(Offset(x * u, (y - r) * u)); diamonds.add(Offset((x + r) * u, y * u))
            diamonds.add(Offset((x + r) * u, y * u)); diamonds.add(Offset(x * u, (y + r) * u))
            diamonds.add(Offset(x * u, (y + r) * u)); diamonds.add(Offset((x - r) * u, y * u))
            diamonds.add(Offset((x - r) * u, y * u)); diamonds.add(Offset(x * u, (y - r) * u))
            x += 0.14f
        }
        y += 0.07f
        row++
    }
    // Wood panels on the lower wall: rectangles with a little step in them.
    val panels = Path()
    var px = -0.1f
    while (px < 3.6f) {
        panels.addRect(Rect((px + 0.035f) * u, 0.605f * u, (px + 0.265f) * u, 0.775f * u))
        panels.addRect(Rect((px + 0.06f) * u, 0.625f * u, (px + 0.24f) * u, 0.755f * u))
        px += 0.3f
    }
    val ornaments = ArrayList<Offset>(80)
    var ox = 0.12f
    while (ox < 3.2f) {
        val yy = 0.905f
        val s = 0.018f
        ornaments.add(Offset((ox) * u, (yy - s * 0.6f) * u)); ornaments.add(Offset((ox + s) * u, yy * u))
        ornaments.add(Offset((ox + s) * u, yy * u)); ornaments.add(Offset((ox) * u, (yy + s * 0.6f) * u))
        ornaments.add(Offset((ox) * u, (yy + s * 0.6f) * u)); ornaments.add(Offset((ox - s) * u, yy * u))
        ornaments.add(Offset((ox - s) * u, yy * u)); ornaments.add(Offset((ox) * u, (yy - s * 0.6f) * u))
        ox += 0.24f
    }

    // The children's room: a painted meadow with hills, a sun and clouds, and foam mats on the floor.
    fun hill(base: Float, amp: Float, salt: Int, x0: Float, x1: Float): Path = Path().apply {
        moveTo(x0 * u, BACK * u)
        var x = x0
        lineTo(x0 * u, (base - amp * 0.5f) * u)
        while (x <= x1) {
            val y = base - amp * (0.55f + 0.45f * sin((x - x0) * 2.6f + salt))
            lineTo(x * u, y * u)
            x += 0.05f
        }
        lineTo(x1 * u, BACK * u)
        close()
    }
    val hillFar = hill(0.67f, 0.07f, 3, 3.2f, 5.6f)
    val hillNear = hill(0.74f, 0.05f, 7, 3.2f, 5.6f)
    val clouds = Path().apply {
        addPath(cloudPath(3.85f * u, 0.17f * u, 0.05f * u))
        addPath(cloudPath(4.62f * u, 0.29f * u, 0.04f * u))
        addPath(cloudPath(5.12f * u, 0.12f * u, 0.032f * u))
    }
    val rays = ArrayList<Offset>(24)
    for (k in 0 until 12) {
        val a = k * 0.5235988f
        rays.add(Offset((4.98f + cos(a) * 0.075f) * u, (0.2f + sin(a) * 0.075f) * u))
        rays.add(Offset((4.98f + cos(a) * 0.105f) * u, (0.2f + sin(a) * 0.105f) * u))
    }
    // Foam mats: squares in four colours on the floor, receding.
    val tiles = List(4) { Path() }
    val lines = ArrayList<Offset>(80)
    val cols = 9
    val rows = 3
    for (j in 0 until rows) {
        val yf = mix(FRONT_Y, BACK, j / rows.toFloat())
        val yb = mix(FRONT_Y, BACK, (j + 1) / rows.toFloat())
        for (i in 0 until cols) {
            val x0 = 3.1f + i * 0.225f
            tiles[(i + j * 2) % 4].floorQuad(u, 0f, x0 + recede(yf), x0 + 0.225f + recede(yf), yf, yb)
            // Re-draw with the back edge's own shift: the quad takes the slant from its own edges.
        }
        lines.add(Offset((3.1f + recede(yf)) * u, yf * u)); lines.add(Offset((3.1f + cols * 0.225f + recede(yf)) * u, yf * u))
    }
    val flowers = ArrayList<Offset>(30)
    for (k in 0 until 16) {
        val fx = 3.35f + hash01(k, 91) * 2.1f
        val fy = 0.74f + hash01(k, 92) * 0.045f
        flowers.add(Offset(fx * u, fy * u))
    }

    // The playroom: big soft dots on the wall, a string of bunting, a stripe of colours and dots on the floor.
    val dots = List(3) { Path() }
    for (k in 0 until 16) {
        val cx = 5.1f + hash01(k, 101) * 3.6f
        val cy = 0.04f + hash01(k, 102) * 0.62f
        val r = 0.05f + 0.09f * hash01(k, 103)
        dots[k % 3].addOval(Rect((cx - r) * u, (cy - r) * u, (cx + r) * u, (cy + r) * u))
    }
    val bunting = Path()
    val flags = List(flagColors.size) { Path() }
    var k = 0
    var hx = 5.2f
    while (hx < 8.6f) {
        val y0 = 0.045f
        val sag = 0.04f
        bunting.moveTo(hx * u, y0 * u)
        bunting.quadraticTo((hx + 0.3f) * u, (y0 + sag * 2f) * u, (hx + 0.6f) * u, y0 * u)
        var f = 0.1f
        while (f < 0.95f) {
            val a = f - 0.055f
            val b = f + 0.055f
            fun sy(s: Float) = y0 + 4f * sag * s * (1f - s)
            val fl = Path().apply {
                moveTo((hx + a * 0.6f) * u, sy(a) * u)
                lineTo((hx + b * 0.6f) * u, sy(b) * u)
                lineTo((hx + f * 0.6f) * u, (sy(f) + 0.055f) * u)
                close()
            }
            flags[k % flags.size].addPath(fl)
            k++
            f += 0.16f
        }
        hx += 0.6f
    }
    // Floor lines of the soft mat, and big painted dots.
    val playLines = ArrayList<Offset>(60)
    var gx = 5.0f
    while (gx < 8.6f) {
        playLines.add(Offset(gx * u, FRONT_Y * u)); playLines.add(Offset((gx + recede(BACK)) * u, BACK * u))
        gx += 0.3f
    }
    for (j in 1..3) {
        val yy = mix(FRONT_Y, BACK, j / 4f)
        playLines.add(Offset((5.0f + recede(yy)) * u, yy * u)); playLines.add(Offset((8.8f + recede(yy)) * u, yy * u))
    }
    val playDots = List(3) { Path() }
    val dotAt = listOf(5.7f to 0.88f, 6.9f to 0.93f, 7.9f to 0.86f, 6.3f to 0.84f, 7.4f to 0.91f)
    for ((i, d) in dotAt.withIndex()) playDots[i % 3].floorDisc(u, 0f, d.first, d.second, 0.11f, 0.12f, 22)

    // The bathroom: square tiles on the wall and the floor, and a border of small blue tiles.
    val bathTiles = ArrayList<Offset>(120)
    val bx0 = 7.9f
    var by = 0.43f
    while (by <= BACK) {
        bathTiles.add(Offset(bx0 * u, by * u)); bathTiles.add(Offset(9.9f * u, by * u))
        by += 0.045f
    }
    var bx = bx0
    while (bx < 9.9f) {
        bathTiles.add(Offset(bx * u, 0.43f * u)); bathTiles.add(Offset(bx * u, BACK * u))
        bx += 0.06f
    }
    val bathFloor = ArrayList<Offset>(60)
    var fy = FRONT_Y
    while (fy >= BACK) {
        bathFloor.add(Offset((7.9f + recede(fy)) * u, fy * u)); bathFloor.add(Offset((9.7f + recede(fy)) * u, fy * u))
        fy -= 0.034f
    }
    var fx = 7.9f
    while (fx < 9.7f) {
        bathFloor.add(Offset(fx * u, FRONT_Y * u)); bathFloor.add(Offset((fx + recede(BACK)) * u, BACK * u))
        fx += 0.09f
    }
    val border = ArrayList<Offset>(60)
    var bdx = 7.95f
    while (bdx < 9.9f) {
        border.add(Offset(bdx * u, 0.4435f * u))
        bdx += 0.045f
    }

    // The bedroom: tiny stars all over the wall, a few big ones, and a crescent.
    val stars = ArrayList<Offset>(70)
    for (i in 0 until 60) {
        val sx = 9.2f + hash01(i, 111) * 2.0f
        val sy = SKY_TOP + 0.05f + hash01(i, 112) * (BACK - SKY_TOP - 0.2f)
        stars.add(Offset(sx * u, sy * u))
    }
    val bigStars = List(6) { i ->
        val cx = 9.45f + hash01(i, 121) * 1.6f
        val cy = 0.04f + hash01(i, 122) * 0.4f
        starPath(Offset(cx * u, cy * u), (0.018f + 0.01f * hash01(i, 123)) * u, 0.008f * u, hash01(i, 124) * 70f)
    }
    val moon = Path().apply {
        addOval(Rect(10.15f * u, 0.07f * u, 10.33f * u, 0.25f * u))
    }
    val carpetDots = ArrayList<Offset>(90)
    for (i in 0 until 80) {
        val yy = mix(FRONT_Y - 0.005f, BACK + 0.01f, hash01(i, 131))
        val xx = 9.3f + hash01(i, 132) * 1.8f + recede(yy)
        carpetDots.add(Offset(xx * u, yy * u))
    }

    // The balcony deck: boards running back, with their joints.
    val deck = ArrayList<Offset>(60)
    var dx = 10.9f
    while (dx < 12.3f) {
        deck.add(Offset(dx * u, FRONT_Y * u)); deck.add(Offset((dx + recede(BACK)) * u, BACK * u))
        dx += 0.11f
    }
    val deckBoards = Path()
    for (j in 1..5) {
        val yy = mix(FRONT_Y, BACK, j / 6f)
        deckBoards.moveTo((10.9f + recede(yy)) * u, yy * u)
        deckBoards.lineTo((12.4f + recede(yy)) * u, yy * u)
    }
    UpStatic(
        planks, diamonds, panels, ornaments, hillFar, hillNear, clouds, rays, tiles, lines, flowers,
        dots, bunting, flags, playLines, playDots, bathTiles, bathFloor, border, stars, bigStars, moon, carpetDots, deck, deckBoards,
    )
}

// ---------------------------------------------------------------------------------------------- the dividers

/** A divider wall: how its door looks. */
private class UpOpening(val d0: Float, val d1: Float, val height: Float)

private val openings = listOf(
    UpOpening(0.16f, 0.74f, 0.5f),  // landing to children's room
    UpOpening(0.1f, 0.8f, 0.56f),   // children's room to playroom: a wide arch
    UpOpening(0.16f, 0.74f, 0.5f),  // playroom to bathroom
    UpOpening(0.16f, 0.74f, 0.5f),  // bathroom to bedroom
    UpOpening(0.04f, 0.88f, 0.6f),  // bedroom to the balcony: a glass door
)

private val faces = listOf(Color(0xFFBFE3FA), Color(0xFFFFF1BD), Color(0xFFEFF9F7), Color(0xFFDCCFF2), Color(0xFFB8503E))
private val edges = listOf(Color(0xFFF7F3EC), Color(0xFFF7F3EC), Color(0xFFF7F3EC), Color(0xFFF7F3EC), Color(0xFFF7F3EC))

private class DividerGeo(val face: Path, val skirt: Path, val jamb: Path, val casing: Path, val front: Path, val top: Path, val bars: Path)

private val dividerGeo = Memo { u ->
    List(5) { i ->
        val xf = DIVIDERS[i] - recede(FLOOR)
        val shift = recede(BACK)
        val h = BACK - SKY_TOP
        val o = openings[i]
        fun px(f: Float, dx: Float = 0f) = (xf + shift * f + dx) * u
        fun py(f: Float, up: Float = 0f) = (mix(FRONT_Y, BACK, f) - up) * u
        val d0 = o.d0
        val d1 = o.d1
        val dh = o.height
        val face = Path().apply {
            fillType = PathFillType.EvenOdd
            moveTo(px(0f, TH), py(0f, h)); lineTo(px(1f, TH), py(1f, h)); lineTo(px(1f, TH), py(1f)); lineTo(px(0f, TH), py(0f)); close()
            moveTo(px(d0, TH), py(d0, dh)); lineTo(px(d1, TH), py(d1, dh)); lineTo(px(d1, TH), py(d1)); lineTo(px(d0, TH), py(d0)); close()
        }
        val skirt = Path().apply {
            poly(px(0f, TH), py(0f, 0.024f), px(d0, TH), py(d0, 0.024f), px(d0, TH), py(d0), px(0f, TH), py(0f))
            poly(px(d1, TH), py(d1, 0.024f), px(1f, TH), py(1f, 0.024f), px(1f, TH), py(1f), px(d1, TH), py(d1))
        }
        val jamb = Path().apply { poly(px(d1), py(d1, dh), px(d1, TH), py(d1, dh), px(d1, TH), py(d1), px(d1), py(d1)) }
        val casing = Path().apply {
            moveTo(px(d0, TH), py(d0)); lineTo(px(d0, TH), py(d0, dh)); lineTo(px(d1, TH), py(d1, dh)); lineTo(px(d1, TH), py(d1))
        }
        val front = Path().apply { poly(px(0f), py(0f, h), px(0f, TH), py(0f, h), px(0f, TH), py(0f), px(0f), py(0f)) }
        val top = Path().apply { poly(px(0f), py(0f, h), px(1f), py(1f, h), px(1f, TH), py(1f, h), px(0f, TH), py(0f, h)) }
        // The glass door at the balcony: bars across the UpOpening.
        val bars = Path().apply {
            if (i == 4) {
                for (k in 1..2) {
                    val f = d0 + (d1 - d0) * k / 3f
                    moveTo(px(f, TH), py(f, dh)); lineTo(px(f, TH), py(f))
                }
                val f0 = d0 + 0.02f
                val f1 = d1 - 0.02f
                val mid = dh * 0.55f
                moveTo(px(f0, TH), py(f0, mid)); lineTo(px(f1, TH), py(f1, mid))
            }
        }
        DividerGeo(face, skirt, jamb, casing, front, top, bars)
    }
}

private fun DrawScope.upDivider(st: Stage, pen: Pen, i: Int, geo: DividerGeo, styles: List<RoomStyle>) {
    val u = st.u
    val xf = DIVIDERS[i] - recede(FLOOR)
    if (!st.sees(xf - 0.05f, xf + recede(BACK) + 0.1f)) return
    val face = faces[i]
    val edge = edges[i]
    val paper = styles.wallOf(i + 1)
    inScene(st) {
        if (paper > 0 && i < 4) {
            drawWallpaperSlanted(paper, geo.face, (xf + TH) * u, u)
            drawPath(geo.face, Brush.horizontalGradient(listOf(Ink.line.copy(alpha = 0.26f), Ink.line.copy(alpha = 0.1f)), startX = xf * u, endX = (xf + recede(BACK)) * u))
        } else if (i == 4) {
            // The outside of the house, seen from the balcony: red boards with white trim.
            drawPath(geo.face, Brush.horizontalGradient(listOf(face.darken(0.18f), face.darken(0.06f)), startX = xf * u, endX = (xf + recede(BACK)) * u))
            clipPath(geo.face) {
                var yy = SKY_TOP
                while (yy < BACK) {
                    drawLine(Ink.line.copy(alpha = 0.25f), Offset((xf - 0.1f) * u, yy * u), Offset((xf + 0.4f) * u, (yy - 0.06f) * u), strokeWidth = pen.lw * 0.7f)
                    yy += 0.06f
                }
            }
        } else {
            drawPath(geo.face, Brush.horizontalGradient(listOf(face.darken(0.18f), face.darken(0.06f)), startX = xf * u, endX = (xf + recede(BACK)) * u))
        }
        drawPath(geo.skirt, edge.darken(0.08f))
        drawPath(geo.face, Ink.line, style = pen.stroke)
        drawPath(geo.jamb, edge.darken(0.12f))
        drawPath(geo.jamb, Ink.line, style = pen.thin)
        drawPath(geo.casing, edge, style = Stroke(0.012f * u))
        drawPath(geo.casing, Ink.line, style = pen.thin)
        if (i == 4) {
            drawPath(geo.bars, edge, style = Stroke(0.007f * u))
            drawPath(geo.bars, Ink.line, style = Stroke(pen.lw * 0.4f))
        }
        drawPath(geo.front, edge)
        drawPath(geo.front, Ink.line, style = pen.stroke)
        drawPath(geo.top, edge.lighten(0.3f))
        drawPath(geo.top, Ink.line, style = pen.stroke)
    }
}

// ---------------------------------------------------------------------------------------------- the rooms

internal fun DrawScope.upperRooms(st: Stage, pen: Pen, styles: List<RoomStyle>) {
    val u = st.u
    val n = pen.night
    val s = upStatic.of(u)
    val geo = dividerGeo.of(u)
    // The balcony first: it is open air, and the walls of the house are drawn over its left edge.
    if (st.sees(roomEdge(5), 14f)) balconyView(st, pen)
    for (i in 0 until 5) {
        val a = roomEdge(i)
        val b = roomEdge(i + 1)
        if (!st.sees(a, b)) continue
        val paper = styles.wallOf(i)
        val l = st.x(max(a, -1f))
        val r = st.x(b)
        if (paper > 0) {
            drawWallpaper(paper, l, st.backgroundTop - 2f, r, BACK * u, -st.cam * u, 0f, u)
        } else {
            clipRect(l, st.backgroundTop - 2f, r + 1f, BACK * u) { roomWall(st, pen, i, s) }
        }
    }
    // Floors, each in its own room.
    for (i in 0 until 6) {
        val x0 = floorFrom(i)
        val x1 = floorTo(i)
        if (!st.sees(x0, x1 + recede(BACK))) continue
        val floor = styles.floorOf(i)
        if (floor > 0 && i < 5) {
            layFloor(st, floor, BACK, x0, x1)
        } else {
            val area = Path().apply { floorQuad(u, st.cam, max(x0, st.cam - 1f), min(x1, st.cam + st.vw + 1f), FRONT_Y, BACK) }
            clipPath(area) { roomFloor(st, pen, i, s) }
        }
    }
    // Skirting and crown: not on the balcony.
    clipRect(left = st.x(roomEdge(0)), right = st.x(roomEdge(5))) {
        skirting(st, pen, BACK, Color(0xFFF7F3EC))
        crown(st, pen, Color(0xFFF7F3EC))
    }
    wallShadow(st, BACK)
    // Details of each room that belong to the wall, and the light that falls in.
    if (st.sees(0f, 3.4f)) {
        lightPatch(st, 1.27f, 0.3f, BACK, n)
        pendant(st, pen, 1.1f, 0.1f, Color(0xFFE8B048))
        pendant(st, pen, 2.4f, 0.1f, Color(0xFFE8B048))
    }
    if (st.sees(3.3f, 5.5f)) pendant(st, pen, 4.4f, 0.12f, Color(0xFFFFC83D))
    if (st.sees(5.4f, 8.3f)) {
        pendant(st, pen, 6.3f, 0.2f, Color(0xFFFF8A7A))
        pendant(st, pen, 7.5f, 0.2f, Color(0xFF4FC79A))
    }
    if (st.sees(8.2f, 9.5f)) {
        lightPatch(st, 8.8f, 0.2f, BACK, n)
        pendant(st, pen, 8.75f, 0.1f, Color(0xFF8FD9E0))
    }
    if (st.sees(9.4f, 11.2f)) pendant(st, pen, 10.1f, 0.12f, Color(0xFFD9C2F2))
    for (i in 0 until 5) upDivider(st, pen, i, geo[i], styles)
    // The balcony's frame: the roof beam over it and a post at the edge of the house.
    if (st.sees(10.9f, 14f)) balconyFrame(st, pen)
    drawBase(st, pen, Color(0xFFE7C497), Color(0xFF9C6B45))
}

internal fun DrawScope.upperRoomsFront(st: Stage, pen: Pen) {
    // Nothing stands in front of the floor.
}

// ---------------------------------------------------------------------------------------------- walls

private fun DrawScope.roomWall(st: Stage, pen: Pen, i: Int, s: UpStatic) {
    val u = st.u
    val top = st.backgroundTop
    val w = st.w + 4f
    when (i) {
        0 -> {
            // The gallery: deep sage with a lattice, dark wood panels below a chair rail.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF4F8A7C), Color(0xFF5E9A8B)), startY = top, endY = 0.56f * u), Offset(-2f, top), Size(w, 0.56f * u - top))
            inScene(st) { drawPoints(s.diamonds, PointMode.Lines, Color(0xFF7DB3A5), strokeWidth = pen.lw * 0.6f) }
            drawRect(Color(0xFFA9714A), Offset(-2f, 0.563f * u), Size(w, (BACK - 0.563f) * u))
            inScene(st) { drawPath(s.panels, Color(0xFF7B4E30), style = Stroke(pen.lw * 0.8f)) }
            drawRect(Color(0xFFD7A064), Offset(-2f, 0.545f * u), Size(w, 0.02f * u))
            drawLine(Color(0xFFF1CE97), Offset(-2f, 0.548f * u), Offset(w, 0.548f * u), strokeWidth = pen.lw)
            drawLine(Ink.line, Offset(-2f, 0.545f * u), Offset(w, 0.545f * u), strokeWidth = pen.lw * 0.8f)
            drawLine(Ink.line, Offset(-2f, 0.565f * u), Offset(w, 0.565f * u), strokeWidth = pen.lw * 0.8f)
        }
        1 -> {
            // The meadow: sky, sun, clouds, a rainbow and two hills.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8FD0F5), Color(0xFFD9F2FF)), startY = top, endY = BACK * u), Offset(-2f, top), Size(w, BACK * u - top))
            inScene(st) {
                drawCircle(Color(0xFFFFE066), 0.11f * u, Offset(4.98f * u, 0.2f * u), alpha = 0.3f)
                drawPoints(s.sunRays, PointMode.Lines, Color(0xFFFFC83D), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFFFD447), 0.062f * u, Offset(4.98f * u, 0.2f * u))
                drawCircle(Color(0xFFFFEE9A), 0.036f * u, Offset(4.965f * u, 0.188f * u))
                drawCircle(Ink.line, 0.062f * u, Offset(4.98f * u, 0.2f * u), alpha = 0.5f, style = pen.thin)
                translate0(0.004f * u, 0.006f * u) { drawPath(s.clouds, Color(0xFFBBD9F0)) }
                drawPath(s.clouds, Color.White)
                drawPath(s.clouds, Ink.line, alpha = 0.35f, style = pen.thin)
                // The rainbow stands behind the hills.
                val colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFA24D), Color(0xFFFFE066), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFF8B5CF6))
                for ((k, c) in colors.withIndex()) {
                    val r = (0.42f - k * 0.02f) * u
                    drawArc(c, 180f, 180f, false, Offset(4.1f * u - r, 0.7f * u - r), Size(r * 2f, r * 2f), alpha = 0.85f, style = Stroke(0.021f * u))
                }
                drawPath(s.hillFar, Color(0xFF9AD27F))
                drawPath(s.hillFar, Ink.line, alpha = 0.5f, style = pen.thin)
                drawPath(s.hillNear, Color(0xFF6FB164))
                drawPath(s.hillNear, Ink.line, alpha = 0.5f, style = pen.thin)
                // A friendly tree and some flowers.
                tree(4.45f, 0.7f, u, pen)
                tree(3.6f, 0.74f, u, pen)
                for ((k, f) in s.flowers.withIndex()) {
                    drawCircle(listOf(Color(0xFFFFFFFF), Color(0xFFFF9EC4), Color(0xFFFFD447), Color(0xFFFF6B6B))[k % 4], 0.007f * u, f)
                }
            }
        }
        2 -> {
            // Bright: warm cream with big soft dots, a stripe of colours and bunting.
            drawRect(Brush.verticalGradient(listOf(Color(0xFFFFF6CF), Color(0xFFFFEDA8)), startY = top, endY = BACK * u), Offset(-2f, top), Size(w, BACK * u - top))
            inScene(st) {
                drawPath(s.dots[0], Color(0xFFFFC2D6), alpha = 0.55f)
                drawPath(s.dots[1], Color(0xFFB8EBD6), alpha = 0.6f)
                drawPath(s.dots[2], Color(0xFFB9DDF5), alpha = 0.6f)
            }
            val stripe = listOf(Color(0xFFFF6B6B), Color(0xFFFFA24D), Color(0xFFFFE066), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFF8B5CF6))
            for ((k, c) in stripe.withIndex()) drawRect(c, Offset(-2f, (0.665f + k * 0.0145f) * u), Size(w, 0.0145f * u))
            drawLine(Ink.line, Offset(-2f, 0.665f * u), Offset(w, 0.665f * u), strokeWidth = pen.lw * 0.6f)
            drawLine(Ink.line, Offset(-2f, 0.752f * u), Offset(w, 0.752f * u), strokeWidth = pen.lw * 0.6f)
            drawRect(Color(0xFFFFF8E6), Offset(-2f, 0.752f * u), Size(w, (BACK - 0.752f) * u))
            inScene(st) {
                drawPath(s.bunting, Ink.line, style = pen.thin)
                for ((k, p) in s.flags.withIndex()) drawPath(p, flagColors[k])
                for (p in s.flags) drawPath(p, Ink.line, style = pen.thin)
            }
        }
        3 -> {
            // Tiles: white, with a border of small blue ones and aqua paint above.
            drawRect(Color(0xFFD7F0F2), Offset(-2f, top), Size(w, 0.43f * u - top))
            drawRect(Color(0xFFF7FBFB), Offset(-2f, 0.43f * u), Size(w, (BACK - 0.43f) * u))
            inScene(st) { drawPoints(s.bathTiles, PointMode.Lines, Color(0xFFA9D6CF), strokeWidth = pen.lw * 0.6f) }
            drawRect(Color(0xFF5BB8B8), Offset(-2f, 0.4f * u), Size(w, 0.03f * u))
            drawLine(Ink.line, Offset(-2f, 0.4f * u), Offset(w, 0.4f * u), strokeWidth = pen.lw * 0.6f)
            drawLine(Ink.line, Offset(-2f, 0.43f * u), Offset(w, 0.43f * u), strokeWidth = pen.lw * 0.6f)
            inScene(st) { drawPoints(s.bathBorder, PointMode.Points, Color(0xFFD7F5F0), strokeWidth = 0.009f * u, cap = StrokeCap.Round) }
        }
        4 -> {
            // Soft lilac with stars, the sky brought indoors.
            drawRect(Brush.verticalGradient(listOf(Color(0xFFCDBFEA), Color(0xFFE6DAF8)), startY = top, endY = BACK * u), Offset(-2f, top), Size(w, BACK * u - top))
            inScene(st) {
                drawPoints(s.stars, PointMode.Points, Color(0xFFFFF3B8), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
                for (p in s.bigStars) drawPath(p, Color(0xFFFFE680))
                drawOval(Color(0xFFFFF3B8), Offset(10.15f * u, 0.07f * u), Size(0.18f * u, 0.18f * u))
                drawOval(Color(0xFFD9CCF2), Offset(10.22f * u, 0.055f * u), Size(0.16f * u, 0.17f * u))
            }
            drawRect(Color(0xFFF3EAFB), Offset(-2f, 0.74f * u), Size(w, (BACK - 0.74f) * u))
            drawLine(Ink.line, Offset(-2f, 0.74f * u), Offset(w, 0.74f * u), strokeWidth = pen.lw * 0.6f)
            drawLine(Color(0xFFB9A2F0), Offset(-2f, 0.7685f * u), Offset(w, 0.7685f * u), strokeWidth = pen.lw * 0.8f)
        }
    }
}

private inline fun DrawScope.translate0(dx: Float, dy: Float, block: DrawScope.() -> Unit) = translate(dx, dy, block)

private fun DrawScope.tree(x: Float, y: Float, u: Float, pen: Pen) {
    val b = Offset(x * u, y * u)
    drawRect(Color(0xFF8A5A3A), Offset(b.x - 0.008f * u, b.y - 0.07f * u), Size(0.016f * u, 0.07f * u))
    drawCircle(Color(0xFF3FA561), 0.045f * u, Offset(b.x, b.y - 0.1f * u))
    drawCircle(Color(0xFF55BD73), 0.032f * u, Offset(b.x - 0.01f * u, b.y - 0.11f * u))
    drawCircle(Ink.line, 0.045f * u, Offset(b.x, b.y - 0.1f * u), alpha = 0.5f, style = pen.thin)
}

// ---------------------------------------------------------------------------------------------- floors

private fun DrawScope.roomFloor(st: Stage, pen: Pen, i: Int, s: UpStatic) {
    val u = st.u
    val rect = Offset(-2f, BACK * u)
    val size = Size(st.w + 4f, (FRONT_Y - BACK) * u)
    when (i) {
        0 -> {
            plankFloor(st, pen, s.planks, BACK, Color(0xFFC8905A))
            // The long red runner with a gold border and diamonds down its middle.
            val outer = Path().apply { floorQuad(u, st.cam, 0.04f, 3.3f, 0.955f, 0.865f) }
            val inner = Path().apply { floorQuad(u, st.cam, 0.08f, 3.26f, 0.947f, 0.873f) }
            val core = Path().apply { floorQuad(u, st.cam, 0.12f, 3.22f, 0.94f, 0.88f) }
            drawPath(outer, Color(0xFFE0B04A))
            drawPath(inner, Color(0xFF9C2F2F))
            drawPath(core, Color(0xFFC4473E))
            drawPath(outer, Ink.line, style = pen.thin)
            inScene(st) { drawPoints(s.runnerOrnaments, PointMode.Lines, Color(0xFFF1D58A), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round) }
        }
        1 -> {
            // Foam mats.
            val cols = listOf(Color(0xFFFF7A7A), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF5AA9F5))
            drawRect(Color(0xFFFFFFFF), rect, size)
            inScene(st) {
                for ((k, p) in s.tiles.withIndex()) drawPath(p, cols[k], alpha = 0.95f)
                drawPoints(s.tileLines, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 0.5f)
            }
            drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.18f), Color.Transparent), startY = BACK * u, endY = FRONT_Y * u), rect, size)
        }
        2 -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFBFE0D4), Color(0xFFE2F4EA)), startY = BACK * u, endY = FRONT_Y * u), rect, size)
            inScene(st) {
                drawPoints(s.playLines, PointMode.Lines, Color(0xFF9CC9B8), strokeWidth = pen.lw * 0.7f)
                drawPath(s.playDots[0], Color(0xFFFF9EC4), alpha = 0.7f)
                drawPath(s.playDots[1], Color(0xFFFFE066), alpha = 0.75f)
                drawPath(s.playDots[2], Color(0xFF8FCBFF), alpha = 0.75f)
            }
        }
        3 -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFB8C6C9), Color(0xFFE6EEEF)), startY = BACK * u, endY = FRONT_Y * u), rect, size)
            inScene(st) { drawPoints(s.bathFloor, PointMode.Lines, Color(0xFF93A5AA), strokeWidth = pen.lw * 0.6f) }
        }
        4 -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFE5D7F2), Color(0xFFF6EEFA)), startY = BACK * u, endY = FRONT_Y * u), rect, size)
            inScene(st) { drawPoints(s.carpetDots, PointMode.Points, Color(0xFFC9B3E6), strokeWidth = 0.007f * u, cap = StrokeCap.Round) }
        }
        else -> {
            // The deck: weathered boards running back.
            drawRect(Brush.verticalGradient(listOf(Color(0xFFB98B5A), Color(0xFFD9AE7C)), startY = BACK * u, endY = FRONT_Y * u), rect, size)
            inScene(st) {
                drawPoints(s.deck, PointMode.Lines, Color(0xFF7A5230), strokeWidth = pen.lw * 0.9f)
                drawPath(s.deckBoards, Color(0xFF8A6038), alpha = 0.55f, style = Stroke(pen.lw * 0.6f))
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------- the balcony

/** Where far things stand on the balcony's view: at [x] on screen when the camera is at its far right, moving with parallax [p]. */
private fun Stage.vx(x: Float, p: Float): Float = (x + (camMax - cam) * p) * u

/** The view from the balcony: sky, sun and moon, mountains with a waterfall, a fjord with a village on its shore, pines. */
private fun DrawScope.balconyView(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val t = pen.t
    val hz = 0.6f
    val left = st.x(roomEdge(5)) - 2f
    clipRect(left = left, top = SKY_TOP * u - 2f, right = st.w + 2f, bottom = BACK * u + 2f) {
        drawSky(st, pen, Mood.SUMMER, hz)
        drawStars(st, pen, 0.5f, 90)
        drawAurora(st, pen, 0.6f, 0.02f, 0.25f)
        val sunVis = (1f - n) * (1f - overcast(pen))
        drawSun(Offset(st.vx(11.5f, 0.05f), 0.2f * u), 0.055f * u, pen, sunVis)
        drawMoon(Offset(st.vx(11.7f, 0.05f), 0.16f * u), 0.04f * u, pen, ramp((n - 0.3f) / 0.5f))
        drawRainbow(Offset(st.vx(11.4f, 0.1f), (hz + 0.02f) * u), 0.5f * u, 0.016f * u, pen.rainbow)
        drawClouds(st, pen, 0.05f, 0.3f, 4, 0.06f, salt = 9)
        drawGulls(st, pen, 2, 0.12f, 0.34f, salt = 13)
        // Far mountains, one of them with the waterfall of Trollfoss.
        drawPeaks(
            st, 0.05f, 0.54f, 0.9f, 0.14f, 0.3f,
            Color(0xFFAFC7E6).atNight(n, 0.75f), Color(0xFF96B1D8).atNight(n, 0.75f),
            Color(0xFFF1F6FF).atNight(n, 0.7f), Color(0xFFD2DEF2).atNight(n, 0.7f), 0.3f, 61,
        )
        balconyFall(st, pen)
        drawPeaks(st, 0.1f, 0.57f, 0.55f, 0.05f, 0.12f, Color(0xFF86ACD8).atNight(n, 0.7f), Color(0xFF7299C8).atNight(n, 0.7f), null, null, 0f, 67)
        // The fjord.
        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF8ED0F0).atNight(n, 0.72f), 1f to Color(0xFF3A8BD6).atNight(n, 0.62f),
                startY = hz * u, endY = BACK * u,
            ),
            Offset(left, hz * u), Size(st.w - left + 4f, (BACK - hz) * u + 2f),
        )
        drawWaterPlane(st, pen, 10f, 99f, hz + 0.008f, BACK - 0.004f, Color.White, 6, 0.15f, 0.6f, 0.5f * (1f - 0.5f * n))
        balconyVillage(st, pen)
        drawForestRow(st, 0.28f, 0.72f, 0.04f, 53, 0.05f, 0.05f, 0.1f, Color(0xFF3F8A5E).atNight(n, 0.7f), Color(0xFF2E7350).atNight(n, 0.7f), skip = 0.25f, ground = Color(0xFF8DBF8B).atNight(n, 0.7f))
        drawForestRow(st, 0.42f, 0.775f, 0.03f, 59, 0.06f, 0.07f, 0.14f, Color(0xFF2F7A52).atNight(n, 0.6f), Color(0xFF256545).atNight(n, 0.6f), skip = 0.3f, ground = Color(0xFF6FAE5A).atNight(n, 0.6f))
    }
}

/** The long white waterfall that gives the village its name, pouring down a far cliff. */
private fun DrawScope.balconyFall(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val t = pen.t
    val x = st.vx(11.9f, 0.05f)
    val top = 0.25f * u
    val bottom = 0.545f * u
    val w = 0.05f * u
    val cliff = Path().apply {
        moveTo(x - w * 1.8f, bottom)
        lineTo(x - w * 1.5f, top + 0.02f * u)
        lineTo(x + w * 1.5f, top)
        lineTo(x + w * 1.9f, bottom)
        close()
    }
    drawPath(cliff, Color(0xFF9DB1D4).atNight(n, 0.7f))
    drawPath(Path().apply { moveTo(x + w * 0.4f, top); lineTo(x + w * 1.5f, top); lineTo(x + w * 1.9f, bottom); lineTo(x + w * 0.9f, bottom); close() }, Color(0xFF7F95BD).atNight(n, 0.7f))
    val fall = Path().apply {
        moveTo(x - w * 0.5f, top + 0.01f * u)
        lineTo(x + w * 0.5f, top)
        lineTo(x + w * 0.65f, bottom)
        lineTo(x - w * 0.65f, bottom)
        close()
    }
    drawPath(fall, Color.White.atNight(n, 0.35f), alpha = 0.92f)
    // Streaks running down and mist at the foot.
    for (k in 0 until 6) {
        val sx = x - w * 0.4f + k * w * 0.16f
        val ph = ((t * 0.7f + k * 0.37f) % 1f)
        drawLine(Color(0xFFBFE3FA).atNight(n, 0.4f), Offset(sx, top + (bottom - top) * ph * 0.8f), Offset(sx, top + (bottom - top) * (ph * 0.8f + 0.2f)), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
    }
    for (k in 0 until 4) {
        val ph = ((t * 0.3f + k * 0.25f) % 1f)
        drawCircle(Color.White.atNight(n, 0.3f), (0.016f + ph * 0.026f) * u, Offset(x + sin(k * 2.1f + t) * w * 0.7f, bottom - 0.004f * u - ph * 0.02f * u), alpha = 0.55f * (1f - ph))
    }
}

/** A handful of houses on the far shore, red, yellow and white, with a church spire and a boat shed. */
private fun DrawScope.balconyVillage(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val t = pen.t
    val hz = 0.6f
    val p = 0.2f
    val land = Path().apply {
        moveTo(st.vx(10.3f, p), (hz + 0.012f) * u)
        quadraticTo(st.vx(10.9f, p), (hz - 0.04f) * u, st.vx(11.5f, p), (hz - 0.022f) * u)
        quadraticTo(st.vx(12.1f, p), (hz - 0.006f) * u, st.vx(12.9f, p), (hz + 0.012f) * u)
        close()
    }
    drawPath(land, Color(0xFF86B585).atNight(n, 0.7f))
    val s = 0.05f * u
    val roof = Color(0xFF5B5566)
    val walls = listOf(Color(0xFFC0463A), Color(0xFFFFE08A), Color(0xFFF7F4EE), Color(0xFFA9D3F0), Color(0xFFC0463A))
    val xs = floatArrayOf(10.7f, 11.0f, 11.33f, 11.62f, 11.95f)
    val ys = floatArrayOf(0.03f, 0.036f, 0.034f, 0.026f, 0.012f)
    for (k in xs.indices) {
        val bx = st.vx(xs[k], p)
        if (bx < -0.3f * u || bx > st.w + 0.3f * u) continue
        drawHouse3d(bx, (hz - ys[k]) * u, s * (0.9f + 0.1f * (k % 2)), 0.5f * s, 0.36f * s, 0.9f * s, walls[k].atNight(n, 0.3f), roof, pen, n, outline = false, sideWindows = 1, chimney = k == 1, t = t)
    }
    // The church spire.
    val cx = st.vx(11.18f, p)
    val cy = (hz - 0.04f) * u
    drawRect(Color(0xFFF7F4EE).atNight(n, 0.3f), Offset(cx - 0.012f * u, cy - 0.04f * u), Size(0.024f * u, 0.04f * u))
    drawPath(Path().apply { poly(cx - 0.016f * u, cy - 0.04f * u, cx, cy - 0.095f * u, cx + 0.016f * u, cy - 0.04f * u) }, Color(0xFF55607A).atNight(n, 0.4f))
    // A little boat on the water.
    val bx = st.vx(11.4f, 0.4f)
    val by = (hz + 0.07f + 0.002f * sin(t * 1.3f)) * u
    drawPath(Path().apply { poly(bx - 0.03f * u, by, bx + 0.03f * u, by, bx + 0.02f * u, by + 0.012f * u, bx - 0.02f * u, by + 0.012f * u) }, Color(0xFFF7F4EE).atNight(n, 0.4f))
    drawPath(Path().apply { poly(bx, by - 0.005f * u, bx, by - 0.05f * u, bx + 0.025f * u, by - 0.005f * u) }, Color(0xFFFF6B6B).atNight(n, 0.4f))
}

/** The beam over the balcony, the post at the house corner and a string of lights. */
private fun DrawScope.balconyFrame(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val left = st.x(roomEdge(5)) - 2f
    clipRect(left = left, top = SKY_TOP * u - 2f, right = st.w + 2f, bottom = st.h + 4f) {
        // The roof over the balcony: boards, a beam and the post at the right.
        drawRect(Color(0xFF8A5E3A), Offset(left, st.backgroundTop - 2f), Size(st.w - left + 4f, 0.0f * u - st.backgroundTop + 2f))
        var yy = SKY_TOP
        while (yy < 0f) {
            drawLine(Color(0xFF6E4630), Offset(left, yy * u), Offset(st.w + 2f, yy * u), strokeWidth = pen.lw * 0.8f)
            yy += 0.07f
        }
        drawRect(Color(0xFFC98A55), Offset(left, 0f), Size(st.w - left + 4f, 0.05f * u))
        drawRect(Color(0xFFE3B27A), Offset(left, 0f), Size(st.w - left + 4f, 0.014f * u))
        drawLine(Ink.line, Offset(left, 0f), Offset(st.w + 2f, 0f), strokeWidth = pen.lw)
        drawLine(Ink.line, Offset(left, 0.05f * u), Offset(st.w + 2f, 0.05f * u), strokeWidth = pen.lw)
        // Little lights hanging in a swag under the beam.
        val lit = ramp((n - 0.2f) / 0.5f)
        val a = st.x(11.05f)
        val b = st.x(12.0f)
        val swag = Path().apply {
            moveTo(a, 0.052f * u)
            quadraticTo((a + b) / 2f, 0.14f * u, b, 0.052f * u)
        }
        drawPath(swag, Ink.line, style = pen.thin)
        for (k in 1..7) {
            val f = k / 8f
            val bx = a + (b - a) * f
            val by = 0.052f * u + 4f * 0.044f * u * f * (1f - f)
            if (lit > 0.01f) fxGlow(Offset(bx, by + 0.008f * u), 0.04f * u, Color(0xFFFFE066), 0.7f * lit)
            drawCircle(lerp(Color(0xFFFFF3C4), Color(0xFFFFE066), lit), 0.006f * u, Offset(bx, by + 0.006f * u))
            drawCircle(Ink.line, 0.006f * u, Offset(bx, by + 0.006f * u), style = pen.thin)
        }
    }
}
