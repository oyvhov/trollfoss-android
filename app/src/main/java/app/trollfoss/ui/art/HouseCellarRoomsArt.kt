package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.CellarFloor
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/*
 * The cellar's background. Five rooms side by side along one back wall, each with its own look:
 * the workshop (honey-coloured boards and a pegboard), the laundry (white tiles, a blue stripe, a drain),
 * the boiler room (soot-dark brick, iron plates, brass pipes and a glow), the pool (turquoise tiles and
 * water light) and the party room (indigo with confetti shapes and bunting), ending in a rough stone
 * alcove where the mine rails run into the tunnel. Pillars with hanging signs (pictures, no words) divide
 * the rooms, and pipes run overhead the whole way.
 *
 * All geometry is built once per scale in [cellarStatic] and drawn in a few batches; the rooms that are not
 * on screen are skipped. Colours that the child changes in the home designer (wallpaper and flooring per
 * room) replace the room's own wall and floor, never the pillars, pipes or the pool.
 */

private val BACK = PlaceId.MANOR_CELLAR.back

/** The x of each wall between rooms, left to right (and the two ends). */
private val EDGES = floatArrayOf(0f, 2.0f, 3.5f, 4.7f, 6.9f, 10f)

/** Where a point of the floor band lies on screen: [boundary] is the x at the depth of the furniture line. */
private fun floorEdge(boundary: Float, y: Float): Float = boundary + (PlaceId.MANOR_CELLAR.floor - y) * (0.5f / 0.36f)

private class CellarStatic(
    // Workshop.
    val boards: List<Offset>, val grain: List<Offset>, val peg: List<Offset>, val tools: Path, val planks: Planks, val sawdust: List<Offset>,
    // Laundry.
    val tileLines: List<Offset>, val checker: Path,
    // Boiler room.
    val bricks: List<Offset>, val brickShade: Path, val rivets: List<Offset>, val diamonds: List<Offset>,
    // Pool.
    val poolWall: List<Offset>, val poolLight: Path, val frieze: Path, val deck: List<Offset>, val basin: List<Offset>, val caustics: Path, val caustics2: Path,
    // Party room.
    val memphis: List<Path>, val dots: List<Offset>, val bunting: List<Path>, val buntingLine: Path, val floorGloss: List<Offset>,
    // The stone alcove at the tunnel.
    val stones: Path, val stoneJoints: List<Offset>, val rails: CellarRails,
)

/** The mine rails on the floor in front of the tunnel door, built from the cart's own path (see the domain's CellarTunnel). */
internal class CellarRails(val railsA: List<Offset>, val railsB: List<Offset>, val sleepers: List<Offset>)

/** Scene x of the cart's rest place and of the tunnel mouth, kept in step with the blueprint in HouseCellar.kt. */
private const val CART_X = 9.58f
private const val DOOR_X = 9.84f
private const val TRACK_Y = 0.93f

private val cellarStatic = Memo { u ->
    val back = BACK
    fun o(x: Float, y: Float) = Offset(x * u, y * u)

    // ---- workshop: vertical boards, grain, pegboard with tool outlines, planks
    val boards = ArrayList<Offset>(80)
    val grain = ArrayList<Offset>(160)
    var bx = EDGES[0]
    var bi = 0
    while (bx < EDGES[1]) {
        boards.add(o(bx, SKY_TOP)); boards.add(o(bx, back))
        for (k in 0 until 3) {
            val gy = SKY_TOP + (back - SKY_TOP) * (0.1f + 0.3f * k + 0.2f * hash01(bi * 3 + k, 801))
            val gx = bx + 0.03f + 0.06f * hash01(bi * 3 + k, 802)
            grain.add(o(gx, gy)); grain.add(o(gx + 0.01f, gy + 0.08f + 0.06f * hash01(bi * 3 + k, 803)))
        }
        bx += 0.12f
        bi++
    }
    val peg = ArrayList<Offset>(300)
    var py = 0.26f
    while (py < 0.56f) {
        var px = 1.38f
        while (px < 2.0f) {
            peg.add(o(px, py))
            px += 0.03f
        }
        py += 0.03f
    }
    // Painted outlines of tools: a hammer, a saw, a spanner, pliers and a ruler.
    val tools = Path().apply {
        // Hammer.
        addRect(Rect(o(1.46f, 0.3f), o(1.48f, 0.5f)))
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(o(1.43f, 0.28f), o(1.51f, 0.32f)), androidx.compose.ui.geometry.CornerRadius(0.004f * u)))
        // Saw: a long blade with a handle.
        moveTo(o(1.58f, 0.3f).x, o(1.58f, 0.3f).y); lineTo(o(1.7f, 0.3f).x, o(1.7f, 0.3f).y); lineTo(o(1.7f, 0.39f).x, o(1.7f, 0.39f).y); lineTo(o(1.58f, 0.34f).x, o(1.58f, 0.34f).y); close()
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(o(1.7f, 0.29f), o(1.74f, 0.4f)), androidx.compose.ui.geometry.CornerRadius(0.01f * u)))
        // Spanner.
        addRect(Rect(o(1.8f, 0.34f), o(1.82f, 0.5f)))
        addOval(Rect(o(1.77f, 0.28f), o(1.85f, 0.35f)))
        // Pliers.
        moveTo(o(1.9f, 0.3f).x, o(1.9f, 0.3f).y); lineTo(o(1.93f, 0.3f).x, o(1.93f, 0.3f).y); lineTo(o(1.95f, 0.5f).x, o(1.95f, 0.5f).y); lineTo(o(1.92f, 0.5f).x, o(1.92f, 0.5f).y); close()
        moveTo(o(1.95f, 0.3f).x, o(1.95f, 0.3f).y); lineTo(o(1.92f, 0.3f).x, o(1.92f, 0.3f).y); lineTo(o(1.9f, 0.5f).x, o(1.9f, 0.5f).y); lineTo(o(1.93f, 0.5f).x, o(1.93f, 0.5f).y); close()
    }
    val planks = buildPlanks(u, -0.3f, 2.4f, back, 0.1f, 91)
    val sawdust = ArrayList<Offset>(40)
    for (k in 0 until 36) {
        val yy = mix(PlaceId.FRONT, back + 0.01f, hash01(k, 811))
        sawdust.add(o(1.4f + 0.7f * hash01(k, 812) + recede(yy), yy))
    }

    // ---- laundry: square tiles, a checkered floor, a drain grid
    val tileLines = ArrayList<Offset>(260)
    val tileTop = 0.42f
    var ty = tileTop
    while (ty <= back + 0.001f) {
        tileLines.add(o(EDGES[1], ty)); tileLines.add(o(EDGES[2], ty))
        ty += 0.06f
    }
    var tx = EDGES[1]
    while (tx <= EDGES[2] + 0.001f) {
        tileLines.add(o(tx, tileTop)); tileLines.add(o(tx, back))
        tx += 0.06f
    }
    val checker = Path()
    run {
        val rows = 4
        for (j in 0 until rows) {
            val yf = mix(PlaceId.FRONT, back, j / rows.toFloat())
            val yb = mix(PlaceId.FRONT, back, (j + 1) / rows.toFloat())
            var i = 0
            while (i < 20) {
                val x0 = floorEdge(EDGES[1], PlaceId.FRONT) + i * 0.1f
                if ((i + j) % 2 == 0) checker.floorQuad(u, 0f, x0 + recede(yf) - recede(PlaceId.FRONT), x0 + 0.1f + recede(yf) - recede(PlaceId.FRONT), yf, yb)
                i++
            }
        }
    }

    // ---- boiler room: running-bond bricks, rivets on the iron plate, diamond plates on the floor
    val bricks = ArrayList<Offset>(500)
    var brow = 0
    var by = SKY_TOP
    while (by < back) {
        bricks.add(o(EDGES[2], by)); bricks.add(o(EDGES[3], by))
        var jx = EDGES[2] + (brow % 2) * 0.05f
        while (jx < EDGES[3]) {
            bricks.add(o(jx, by)); bricks.add(o(jx, min(by + 0.05f, back)))
            jx += 0.1f
        }
        by += 0.05f
        brow++
    }
    val brickShade = Path()
    for (k in 0 until 26) {
        val rx = EDGES[2] + 0.02f + (EDGES[3] - EDGES[2] - 0.12f) * hash01(k, 821)
        val ry = SKY_TOP + (back - SKY_TOP - 0.06f) * hash01(k, 822)
        brickShade.addRect(Rect(o(rx, ry), o(rx + 0.09f, ry + 0.045f)))
    }
    val rivets = ArrayList<Offset>(60)
    for (i in 0..8) for (j in 0..4) rivets.add(o(3.68f + i * 0.03f, 0.2f + j * 0.025f))
    val diamonds = ArrayList<Offset>(200)
    var dy = back + 0.02f
    while (dy < PlaceId.FRONT) {
        var dx = floorEdge(EDGES[2], PlaceId.FRONT) - 0.1f
        while (dx < EDGES[3] + 0.3f) {
            val sh = recede(dy) - recede(PlaceId.FRONT)
            diamonds.add(o(dx + sh, dy)); diamonds.add(o(dx + sh + 0.02f, dy - 0.012f))
            diamonds.add(o(dx + sh + 0.02f, dy)); diamonds.add(o(dx + sh, dy - 0.012f))
            dx += 0.07f
        }
        dy += 0.035f
    }

    // ---- pool: glossy tiles, a frieze of waves, deck tiles, tiles in the basin, light on the wall
    val poolWall = ArrayList<Offset>(400)
    var wy = SKY_TOP
    while (wy <= back + 0.001f) {
        poolWall.add(o(EDGES[3], wy)); poolWall.add(o(EDGES[4], wy))
        wy += 0.055f
    }
    var wx = EDGES[3]
    while (wx <= EDGES[4] + 0.001f) {
        poolWall.add(o(wx, SKY_TOP)); poolWall.add(o(wx, back))
        wx += 0.055f
    }
    val poolLight = Path()
    var gi = 0
    var gy2 = SKY_TOP + 0.02f
    while (gy2 < back - 0.05f) {
        var gx2 = EDGES[3] + 0.02f
        while (gx2 < EDGES[4] - 0.05f) {
            if (hash01(gi, 831) < 0.28f) poolLight.addRect(Rect(o(gx2, gy2), o(gx2 + 0.055f, gy2 + 0.055f)))
            gx2 += 0.055f
            gi++
        }
        gy2 += 0.055f
    }
    val frieze = Path()
    run {
        var fx = EDGES[3]
        frieze.moveTo(o(fx, 0.52f).x, o(fx, 0.52f).y)
        while (fx < EDGES[4]) {
            frieze.quadraticTo(o(fx + 0.03f, 0.49f).x, o(fx + 0.03f, 0.49f).y, o(fx + 0.06f, 0.52f).x, o(fx + 0.06f, 0.52f).y)
            frieze.quadraticTo(o(fx + 0.09f, 0.55f).x, o(fx + 0.09f, 0.55f).y, o(fx + 0.12f, 0.52f).x, o(fx + 0.12f, 0.52f).y)
            fx += 0.12f
        }
    }
    val deck = ArrayList<Offset>(200)
    var dky = back + 0.03f
    while (dky < PlaceId.FRONT) {
        deck.add(o(floorEdge(EDGES[3], dky) - 0.05f, dky)); deck.add(o(floorEdge(EDGES[4], dky) + 0.05f, dky))
        dky += 0.045f
    }
    var dkx = EDGES[3] - 0.4f
    while (dkx < EDGES[4] + 0.4f) {
        deck.add(o(dkx + recede(PlaceId.FRONT) * 0f, PlaceId.FRONT)); deck.add(o(dkx + (recede(back) - recede(PlaceId.FRONT)) + 0f, back))
        dkx += 0.12f
    }
    val basin = ArrayList<Offset>(200)
    run {
        val x1 = CellarFloor.POOL_X1
        val x2 = CellarFloor.POOL_X2
        var k = 0
        var yy = CellarFloor.POOL_LINE
        while (yy <= CellarFloor.POOL_BED + 0.001f) {
            basin.add(o(x1, yy)); basin.add(o(x2, yy))
            yy += 0.034f
            k++
        }
        var xx = x1
        while (xx <= x2 + 0.001f) {
            basin.add(o(xx, CellarFloor.POOL_LINE)); basin.add(o(xx, CellarFloor.POOL_BED))
            xx += 0.07f
        }
    }
    fun caustic(phase: Float): Path = Path().apply {
        var k = 0
        var cy = CellarFloor.POOL_LINE + 0.01f
        while (cy < CellarFloor.POOL_BED - 0.01f) {
            var cx = CellarFloor.POOL_X1 + 0.04f + 0.05f * hash01(k, 841) + phase
            while (cx < CellarFloor.POOL_X2 - 0.06f) {
                moveTo(o(cx, cy).x, o(cx, cy).y)
                quadraticTo(o(cx + 0.03f, cy - 0.012f).x, o(cx + 0.03f, cy - 0.012f).y, o(cx + 0.06f, cy).x, o(cx + 0.06f, cy).y)
                cx += 0.17f
            }
            cy += 0.045f
            k++
        }
    }
    val caustics = caustic(0f)
    val caustics2 = caustic(0.08f)

    // ---- party room: confetti shapes on an indigo wall, bunting, gloss on the floor
    val memphis = List(4) { Path() }
    for (k in 0 until 46) {
        val mx = EDGES[4] + 0.05f + (EDGES[5] - EDGES[4] - 0.1f) * hash01(k, 851)
        val my = SKY_TOP + 0.1f + (back - SKY_TOP - 0.26f) * hash01(k, 852)
        val s = 0.014f + 0.02f * hash01(k, 853)
        val p = memphis[k % 4]
        when (k % 3) {
            0 -> p.apply { moveTo(o(mx, my - s).x, o(mx, my - s).y); lineTo(o(mx + s, my + s).x, o(mx + s, my + s).y); lineTo(o(mx - s, my + s).x, o(mx - s, my + s).y); close() }
            1 -> p.addOval(Rect(o(mx - s * 0.8f, my - s * 0.8f), o(mx + s * 0.8f, my + s * 0.8f)))
            else -> p.apply {
                moveTo(o(mx - s, my).x, o(mx - s, my).y)
                quadraticTo(o(mx - s * 0.5f, my - s).x, o(mx - s * 0.5f, my - s).y, o(mx, my).x, o(mx, my).y)
                quadraticTo(o(mx + s * 0.5f, my + s).x, o(mx + s * 0.5f, my + s).y, o(mx + s, my).x, o(mx + s, my).y)
            }
        }
    }
    val dots = ArrayList<Offset>(80)
    for (k in 0 until 70) dots.add(o(EDGES[4] + 0.05f + (EDGES[5] - EDGES[4] - 0.1f) * hash01(k, 861), SKY_TOP + 0.1f + (back - SKY_TOP - 0.2f) * hash01(k, 862)))
    val bunting = List(5) { Path() }
    val buntingLine = Path()
    run {
        var hx = EDGES[4]
        var n = 0
        while (hx < EDGES[5] - 0.1f) {
            val y0 = 0.1f
            val sag = 0.03f
            buntingLine.moveTo(o(hx, y0).x, o(hx, y0).y)
            buntingLine.quadraticTo(o(hx + 0.2f, y0 + sag * 2f).x, o(hx + 0.2f, y0 + sag * 2f).y, o(hx + 0.4f, y0).x, o(hx + 0.4f, y0).y)
            var f = 0.1f
            while (f < 0.95f) {
                fun sy(s: Float) = y0 + 4f * sag * s * (1f - s)
                val a = f - 0.06f
                val b = f + 0.06f
                bunting[n % 5].apply {
                    moveTo(o(hx + a * 0.4f, sy(a)).x, o(hx + a * 0.4f, sy(a)).y)
                    lineTo(o(hx + b * 0.4f, sy(b)).x, o(hx + b * 0.4f, sy(b)).y)
                    lineTo(o(hx + f * 0.4f, sy(f) + 0.04f).x, o(hx + f * 0.4f, sy(f) + 0.04f).y)
                    close()
                }
                n++
                f += 0.16f
            }
            hx += 0.4f
        }
    }
    val floorGloss = ArrayList<Offset>(40)
    for (k in 0 until 12) {
        val gx = EDGES[4] - 0.3f + k * 0.34f
        floorGloss.add(o(gx + recede(PlaceId.FRONT) * 0f, PlaceId.FRONT)); floorGloss.add(o(gx + (recede(back) - recede(PlaceId.FRONT)), back))
    }

    // ---- the stone alcove at the end: rough blocks round the tunnel mouth
    val stones = Path()
    val stoneJoints = ArrayList<Offset>(120)
    run {
        var row = 0
        var sy = 0.2f
        while (sy < back) {
            var sx = 9.5f + (row % 2) * 0.07f
            while (sx < 10.05f) {
                val w = 0.1f + 0.05f * hash01(row * 9 + (sx * 10).toInt(), 871)
                stones.addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(o(sx + 0.005f, sy + 0.004f), o(min(sx + w, 10.05f) - 0.005f, sy + 0.071f)), androidx.compose.ui.geometry.CornerRadius(0.012f * u)))
                sx += w
            }
            sy += 0.075f
            row++
        }
    }
    // The mine rails: along the front of the room, then back into the tunnel mouth.
    val railsA = ArrayList<Offset>(8)
    val railsB = ArrayList<Offset>(8)
    val sleepers = ArrayList<Offset>(60)
    run {
        val a0 = CART_X - 0.33f
        val a1 = CART_X + 0.10f
        for (side in 0..1) {
            val yy = TRACK_Y + (if (side == 0) -0.016f else 0.016f)
            railsA.add(o(a0, yy)); railsA.add(o(a1 + recede(yy) * 0f, yy))
        }
        // The curve into the depth: two rails running back to the tunnel mouth.
        for (side in 0..1) {
            val ox = if (side == 0) -0.045f else 0.045f
            railsB.add(o(a1 + ox, TRACK_Y)); railsB.add(o(DOOR_X + ox, 0.815f))
        }
        var sx = a0 + 0.02f
        while (sx < a1) {
            sleepers.add(o(sx, TRACK_Y - 0.026f)); sleepers.add(o(sx, TRACK_Y + 0.026f))
            sx += 0.05f
        }
        for (k in 1..7) {
            val f = k / 8f
            val cx = a1 + (DOOR_X - a1) * f
            val cy = TRACK_Y + (0.815f - TRACK_Y) * f
            sleepers.add(o(cx - 0.065f, cy)); sleepers.add(o(cx + 0.065f, cy))
        }
    }

    CellarStatic(
        boards, grain, peg, tools, planks, sawdust, tileLines, checker, bricks, brickShade, rivets, diamonds,
        poolWall, poolLight, frieze, deck, basin, caustics, caustics2, memphis, dots, bunting, buntingLine, floorGloss,
        stones, stoneJoints, CellarRails(railsA, railsB, sleepers),
    )
}

// ================================================================================ the whole background

internal fun DrawScope.cellarBackground(st: Stage, pen: Pen, styles: List<RoomStyle>) {
    val u = st.u
    val cs = cellarStatic.of(u)
    val back = BACK
    for (i in 0 until 5) {
        val x0 = EDGES[i]
        val x1 = EDGES[i + 1]
        if (!st.sees(x0 - 0.3f, x1 + 0.4f)) continue
        val paper = styles.wallOf(i)
        if (paper > 0) {
            paperWall(st, paper, back, x0, x1)
        } else {
            when (i) {
                0 -> workshopWall(st, pen, cs, x0, x1)
                1 -> laundryWall(st, pen, cs, x0, x1)
                2 -> boilerWall(st, pen, cs, x0, x1)
                3 -> poolWall(st, pen, cs, x0, x1)
                else -> partyWall(st, pen, cs, x0, x1)
            }
        }
    }
    // Floors, room by room, each slanted like the floor band is.
    for (i in 0 until 5) {
        val x0 = EDGES[i]
        val x1 = EDGES[i + 1]
        if (!st.sees(x0 - 0.5f, x1 + 0.6f)) continue
        val floor = styles.floorOf(i)
        if (floor > 0) {
            layFloor(st, floor, back, floorEdge(x0, PlaceId.FRONT), floorEdge(x1, PlaceId.FRONT))
        } else {
            roomFloor(st, pen, cs, i, x0, x1)
        }
    }
    wallShadow(st, back)
    rooms(st, pen, cs)
    // Pillars, the ceiling beam, the pipes and the hanging signs tie the rooms together.
    for (k in 1..4) pillar(st, pen, EDGES[k])
    ceiling(st, pen)
    for (i in 0 until 5) {
        val sx = if (i == 0) 1.28f else EDGES[i] + (if (i == 3) 0.34f else 0.3f)
        if (st.sees(sx - 0.15f, sx + 0.15f)) roomSign(st, pen, i, sx)
    }
    drawBase(st, pen, Color(0xFFBDB4C4), Color(0xFF6B6382))
    drawBaseStones(st, pen, Color(0xFF8E86A6), 71)
}

/** Everything that belongs to one room beyond its wall and floor: lamps, pictures, windows and what lies about. */
private fun DrawScope.rooms(st: Stage, pen: Pen, cs: CellarStatic) {
    if (st.sees(0f, 2.3f)) workshopThings(st, pen, cs)
    if (st.sees(2f, 3.8f)) laundryThings(st, pen)
    if (st.sees(3.5f, 4.9f)) boilerThings(st, pen)
    if (st.sees(4.7f, 7.0f)) poolBasin(st, pen, cs)
    if (st.sees(6.9f, 10f)) partyThings(st, pen, cs)
}

// ------------------------------------------------------------------------------------------ workshop

private fun DrawScope.workshopWall(st: Stage, pen: Pen, cs: CellarStatic, x0: Float, x1: Float) {
    val u = st.u
    val honey = Color(0xFFD9A15E)
    drawRect(honey, Offset(st.x(x0), st.backgroundTop), Size((x1 - x0) * u, BACK * u - st.backgroundTop))
    // A darker wainscot, the boards, and their grain.
    drawRect(Color(0xFFB97F46), Offset(st.x(x0), 0.56f * u), Size((x1 - x0) * u, (BACK - 0.56f) * u))
    inScene(st) {
        drawPoints(cs.boards, PointMode.Lines, Color(0xFF8D5E34), strokeWidth = pen.lw * 0.8f)
        drawPoints(cs.grain, PointMode.Lines, Color(0xFFB27B43), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
    }
    drawRect(Color(0xFFE8BC7C), Offset(st.x(x0), 0.545f * u), Size((x1 - x0) * u, 0.016f * u))
    drawLine(Ink.line, Offset(st.x(x0), 0.545f * u), Offset(st.x(x1), 0.545f * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(st.x(x0), 0.561f * u), Offset(st.x(x1), 0.561f * u), strokeWidth = pen.lw * 0.7f)
    // The pegboard behind the workbench.
    if (st.sees(1.3f, 2.1f)) {
        val board = Rect(st.o(1.35f, 0.22f), st.o(2.0f, 0.55f))
        inkedRound(board, 0.008f * u, Color(0xFFE2C28E), pen, shade = false)
        inScene(st) { drawPoints(cs.peg, PointMode.Points, Color(0xFF9A7345), strokeWidth = pen.lw * 1.3f, cap = StrokeCap.Round) }
        inScene(st) { drawPath(cs.tools, Color(0xFF7A5230), alpha = 0.55f) }
    }
    skirtingColor(st, pen, x0, x1, Color(0xFF8D5E34))
}

private fun DrawScope.workshopThings(st: Stage, pen: Pen, cs: CellarStatic) {
    val u = st.u
    // A high basement window with bars: sky by day, moon and stars at night.
    if (st.sees(0.1f, 0.5f)) {
        val frame = Rect(st.o(0.14f, 0.05f), st.o(0.36f, 0.2f))
        val sky = lerp(Color(0xFF9ADCF6), Color(0xFF1A1748), pen.night)
        box3d(frame, 0.03f * u, Color(0xFFE9E1D3), pen, radius = 0.006f * u)
        val glass = Rect(frame.left + 0.012f * u, frame.top + 0.012f * u, frame.right - 0.012f * u, frame.bottom - 0.012f * u)
        drawRect(Brush.verticalGradient(listOf(sky, lerp(Color(0xFFD9F4E4), Color(0xFF2E2A6A), pen.night)), startY = glass.top, endY = glass.bottom), glass.topLeft, glass.size)
        // A strip of grass and a tuft outside; stars and a moon by night.
        clipPath(Path().apply { addRect(glass) }) {
            drawRect(lerp(Color(0xFF6DBB5A), Color(0xFF1F3A3A), pen.night), Offset(glass.left, glass.bottom - glass.height * 0.25f), Size(glass.width, glass.height * 0.25f))
            if (pen.night > 0.3f) {
                drawCircle(Color(0xFFFFF0BF), glass.height * 0.17f, Offset(glass.left + glass.width * 0.7f, glass.top + glass.height * 0.3f), alpha = min(1f, pen.night * 1.2f))
                for (k in 0 until 5) drawCircle(Color.White, pen.lw * 0.9f, Offset(glass.left + glass.width * hash01(k, 881), glass.top + glass.height * 0.6f * hash01(k, 882)), alpha = pen.night)
            } else {
                drawCircle(Color.White, glass.height * 0.1f, Offset(glass.left + glass.width * 0.3f, glass.top + glass.height * 0.3f), alpha = 0.8f)
            }
        }
        for (k in 1..3) drawLine(Color(0xFF4B4A55), Offset(glass.left + glass.width * k / 4f, glass.top), Offset(glass.left + glass.width * k / 4f, glass.bottom), strokeWidth = pen.lw * 1.6f)
        drawRect(Ink.line, glass.topLeft, glass.size, style = pen.stroke)
        // A beam of dusty light falls from it by day.
        val day = 1f - pen.night
        if (day > 0.05f) {
            val beam = Path().apply {
                moveTo(glass.left, glass.bottom); lineTo(glass.right, glass.bottom)
                lineTo(st.x(0.88f), BACK * u); lineTo(st.x(0.52f), BACK * u); close()
            }
            drawPath(beam, Brush.verticalGradient(listOf(Color(0xFFFFF3C8).copy(alpha = 0.28f * day), Color(0xFFFFF3C8).copy(alpha = 0.05f * day)), startY = glass.bottom, endY = BACK * u))
        }
    }
    // A plan pinned to the wall: a blue-print of a bird house.
    if (st.sees(1.25f, 1.45f)) {
        val paper = Rect(st.o(1.2f, 0.25f), st.o(1.34f, 0.43f))
        drawRect(Ink.shadow, Offset(paper.left + 0.004f * u, paper.top + 0.005f * u), paper.size)
        drawRect(Color(0xFF3F78C8), paper.topLeft, paper.size)
        drawRect(Ink.line, paper.topLeft, paper.size, style = pen.thin)
        val white = Color.White.copy(alpha = 0.85f)
        val cx = paper.center.x
        val cy = paper.center.y
        val s = paper.width * 0.28f
        val house = Path().apply {
            moveTo(cx - s, cy + s); lineTo(cx - s, cy - s * 0.2f); lineTo(cx, cy - s * 1.1f); lineTo(cx + s, cy - s * 0.2f); lineTo(cx + s, cy + s); close()
        }
        drawPath(house, white, style = Stroke(pen.lw * 0.7f))
        drawCircle(white, s * 0.28f, Offset(cx, cy + s * 0.1f), style = Stroke(pen.lw * 0.7f))
        drawLine(white, Offset(paper.left + 0.01f * u, paper.bottom - 0.012f * u), Offset(paper.right - 0.01f * u, paper.bottom - 0.012f * u), strokeWidth = pen.lw * 0.6f)
        fxNail(Offset(paper.left + 0.008f * u, paper.top + 0.008f * u), 0.004f * u)
        fxNail(Offset(paper.right - 0.008f * u, paper.top + 0.008f * u), 0.004f * u)
    }
    // Sawdust on the floor.
    inScene(st) { drawPoints(cs.sawdust, PointMode.Points, Color(0xFFE8C98A), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round, alpha = 0.8f) }
    // A cobweb in the corner, swaying a little.
    if (st.sees(0f, 0.3f)) {
        val c = st.o(0f, 0.0f)
        val sway = sin(pen.t * 0.9f) * 0.004f * u
        for (k in 0..3) {
            val a = 0.2f + k * 0.4f
            drawLine(Color.White.copy(alpha = 0.55f), c, Offset(c.x + cos(a) * 0.1f * u + sway, c.y + sin(a) * 0.1f * u), strokeWidth = pen.lw * 0.6f)
        }
        for (r in 1..3) {
            val rr = r * 0.03f * u
            drawArc(Color.White.copy(alpha = 0.5f), 12f, 76f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f), style = Stroke(pen.lw * 0.5f))
        }
    }
}

// -------------------------------------------------------------------------------------------- laundry

private fun DrawScope.laundryWall(st: Stage, pen: Pen, cs: CellarStatic, x0: Float, x1: Float) {
    val u = st.u
    val w = (x1 - x0) * u
    drawRect(Color(0xFFBFE3F2), Offset(st.x(x0), st.backgroundTop), Size(w, BACK * u - st.backgroundTop))
    // White tiles up to a blue stripe.
    drawRect(Color(0xFFF3FBFA), Offset(st.x(x0), 0.42f * u), Size(w, (BACK - 0.42f) * u))
    inScene(st) { drawPoints(cs.tileLines, PointMode.Lines, Color(0xFF9ED6CF), strokeWidth = pen.lw * 0.6f) }
    drawRect(Color(0xFF4FB3C8), Offset(st.x(x0), 0.395f * u), Size(w, 0.025f * u))
    drawRect(Color(0xFF7FD3E4), Offset(st.x(x0), 0.395f * u), Size(w, 0.008f * u))
    drawLine(Ink.line, Offset(st.x(x0), 0.395f * u), Offset(st.x(x1), 0.395f * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(st.x(x0), 0.42f * u), Offset(st.x(x1), 0.42f * u), strokeWidth = pen.lw * 0.7f)
    skirtingColor(st, pen, x0, x1, Color(0xFF4FB3C8))
}

private fun DrawScope.laundryThings(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    // A family portrait of socks: two odd ones in a frame, with a heart.
    if (st.sees(3.15f, 3.5f)) {
        val inner = frame3d(Rect(st.o(3.2f, 0.12f), st.o(3.34f, 0.28f)), pen, Color(0xFFF7F3EC), Color(0xFFFFE9F0))
        val sock = Path().apply {
            moveTo(inner.left + inner.width * 0.3f, inner.top + inner.height * 0.12f)
            lineTo(inner.left + inner.width * 0.55f, inner.top + inner.height * 0.12f)
            lineTo(inner.left + inner.width * 0.55f, inner.top + inner.height * 0.6f)
            quadraticTo(inner.left + inner.width * 0.85f, inner.top + inner.height * 0.62f, inner.left + inner.width * 0.82f, inner.top + inner.height * 0.82f)
            quadraticTo(inner.left + inner.width * 0.55f, inner.top + inner.height * 0.9f, inner.left + inner.width * 0.3f, inner.top + inner.height * 0.72f)
            close()
        }
        inked(sock, Color(0xFF7CCBFF), pen, shade = false)
        drawLine(Color.White, Offset(inner.left + inner.width * 0.3f, inner.top + inner.height * 0.28f), Offset(inner.left + inner.width * 0.55f, inner.top + inner.height * 0.28f), strokeWidth = pen.lw * 1.6f)
        drawPath(fxHeart(inner.left + inner.width * 0.22f, inner.top + inner.height * 0.82f, inner.width * 0.12f), Color(0xFFFF6F91))
    }
    // The floor drain.
    if (st.sees(3.0f, 3.5f)) {
        val c = st.o(3.2f, 0.935f)
        val drain = Path().apply { floorDisc(u, st.cam, 3.2f, 0.935f, 0.045f, 0.045f, 18) }
        drawPath(drain, Color(0xFF8A9AA6))
        drawPath(Path().apply { floorDisc(u, st.cam, 3.2f, 0.935f, 0.033f, 0.033f, 16) }, Color(0xFF3E4752))
        for (k in -2..2) drawLine(Color(0xFF8A9AA6), Offset(c.x + k * 0.01f * u, c.y - 0.012f * u), Offset(c.x + k * 0.01f * u, c.y + 0.012f * u), strokeWidth = pen.lw)
        drawPath(drain, Ink.line, style = pen.thin)
    }
    // Soap bubbles drifting about, and warm steam from the dryer's side of the room.
    for (k in 0 until 7) {
        val ph = wrap(t * (0.04f + 0.02f * hash01(k, 891)) + hash01(k, 892), 1f)
        val bx = 2.15f + 1.3f * hash01(k, 893) + sin(t * 0.7f + k * 2f) * 0.03f
        val by = 0.7f - ph * 0.55f
        if (!st.sees(bx - 0.05f, bx + 0.05f)) continue
        val c = st.o(bx, by)
        val r = (0.011f + 0.008f * hash01(k, 894)) * u
        drawCircle(Color.White.copy(alpha = 0.2f), r, c)
        drawCircle(Color(0xFF9ADAFF).copy(alpha = 0.7f), r, c, style = Stroke(pen.lw * 0.7f))
        drawCircle(Color.White.copy(alpha = 0.9f), r * 0.28f, Offset(c.x - r * 0.35f, c.y - r * 0.35f))
    }
    // A pipe drips into a little puddle by the chute.
    if (st.sees(3.3f, 3.7f)) {
        val x = 3.58f
        val ph = wrap(t * 0.9f, 1f)
        val head = st.o(x, 0.07f)
        drawCircle(Color(0xFF8FA3B8), 0.012f * u, head)
        if (ph < 0.85f) {
            val dy = ph * ph * 0.8f
            val c = st.o(x, 0.1f + dy * 0.8f)
            val drop = Path().apply {
                moveTo(c.x, c.y - 0.012f * u)
                quadraticTo(c.x + 0.008f * u, c.y + 0.002f * u, c.x, c.y + 0.008f * u)
                quadraticTo(c.x - 0.008f * u, c.y + 0.002f * u, c.x, c.y - 0.012f * u)
                close()
            }
            drawPath(drop, Color(0xFF7CCBFF))
            drawPath(drop, Ink.line, style = pen.thin)
        }
        val pud = Path().apply { floorDisc(u, st.cam, x - 0.1f, 0.92f, 0.03f, 0.02f, 14) }
        drawPath(pud, Color(0xFF7CCBFF).copy(alpha = 0.55f))
        if (ph > 0.85f) {
            val r = (ph - 0.85f) / 0.15f
            drawPath(Path().apply { floorDisc(u, st.cam, x - 0.1f, 0.92f, 0.01f + 0.03f * r, 0.007f + 0.02f * r, 14) }, Color.White.copy(alpha = 0.6f * (1f - r)), style = pen.thin)
        }
    }
}

// ----------------------------------------------------------------------------------------- boiler room

private fun DrawScope.boilerWall(st: Stage, pen: Pen, cs: CellarStatic, x0: Float, x1: Float) {
    val u = st.u
    val w = (x1 - x0) * u
    drawRect(Color(0xFF8A4A3A), Offset(st.x(x0), st.backgroundTop), Size(w, BACK * u - st.backgroundTop))
    inScene(st) { drawPath(cs.brickShade, Color(0xFF9A5644), alpha = 0.7f) }
    inScene(st) { drawPoints(cs.bricks, PointMode.Lines, Color(0xFF3E2820), strokeWidth = pen.lw * 0.8f) }
    // Soot above, a warm glow below (it breathes).
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1C1410).copy(alpha = 0.55f), Color.Transparent), startY = SKY_TOP * u, endY = 0.45f * u), Offset(st.x(x0), st.backgroundTop), Size(w, 0.45f * u - st.backgroundTop))
    // The iron plate behind the boiler, with rivets.
    if (st.sees(3.6f, 4.4f)) {
        val plate = Rect(st.o(3.66f, 0.12f), st.o(4.34f, BACK - 0.02f))
        inkedRound(plate, 0.008f * u, Color(0xFF5A5F6E), pen, shade = false)
        inScene(st) { drawPoints(cs.rivets, PointMode.Points, Color(0xFF8E96A8), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round) }
        drawRect(Color.White.copy(alpha = 0.08f), plate.topLeft, Size(plate.width * 0.3f, plate.height))
    }
    skirtingColor(st, pen, x0, x1, Color(0xFF3E2820))
}

private fun DrawScope.boilerThings(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    // Pipes drop from the ceiling to the valves; brass flanges sit on each of them.
    for (x in floatArrayOf(3.62f, 4.4f, 4.62f)) {
        if (!st.sees(x - 0.1f, x + 0.1f)) continue
        val top = if (x == 4.4f) 0.08f else 0.08f
        val bottom = if (x == 4.4f) 0.36f else 0.62f
        val rect = Rect(st.o(x - 0.013f, top), st.o(x + 0.013f, bottom))
        fxPipe(rect, pen, Color(0xFFC9884A))
        for (fy in floatArrayOf(top + 0.02f, bottom - 0.31f * 0f + 0.0f)) {
            val flange = Rect(st.o(x - 0.022f, fy - 0.007f), st.o(x + 0.022f, fy + 0.007f))
            inkedRound(flange, 0.003f * u, Color(0xFFE0B04A), pen, shade = false)
        }
    }
    // Steam puffs from a leaky joint, and the furnace glow on the wall.
    if (st.sees(3.5f, 4.9f)) {
        val glow = 0.55f + 0.2f * sin(t * 1.7f)
        val c = st.o(4.38f, 0.6f)
        drawCircle(safeRadialGradient(listOf(Color(0xFFFF8A2E).copy(alpha = 0.3f * glow), Color(0x00FF8A2E)), c, 0.5f * u), 0.5f * u, c)
        fxPuffs(st.x(3.62f) + 0.012f * u, 0.52f * u, t, 0.014f * u, 0.12f * u, Color.White, 0.5f, 3, 0.3f, 0.02f * u)
    }
    // A gauge or two on the wall, and a hazard stripe along the base.
    if (st.sees(3.55f, 3.9f)) cellarGauge(st, pen, 3.72f, 0.3f, 0.034f, 0.6f)
}

// ------------------------------------------------------------------------------------------- the pool

private fun DrawScope.poolWall(st: Stage, pen: Pen, cs: CellarStatic, x0: Float, x1: Float) {
    val u = st.u
    val w = (x1 - x0) * u
    drawRect(Color(0xFF54CFCB), Offset(st.x(x0), st.backgroundTop), Size(w, BACK * u - st.backgroundTop))
    inScene(st) { drawPath(cs.poolLight, Color(0xFF7BE0DA)) }
    inScene(st) { drawPoints(cs.poolWall, PointMode.Lines, Color(0xFFB8F0EE), strokeWidth = pen.lw * 0.6f) }
    // A frieze of waves, on a blue band.
    drawRect(Color(0xFF2F8FD6), Offset(st.x(x0), 0.5f * u), Size(w, 0.07f * u))
    inScene(st) { drawPath(cs.frieze, Color.White, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round)) }
    drawLine(Ink.line, Offset(st.x(x0), 0.5f * u), Offset(st.x(x1), 0.5f * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(st.x(x0), 0.57f * u), Offset(st.x(x1), 0.57f * u), strokeWidth = pen.lw * 0.7f)
    // Light from the water dances on the wall above it.
    if (st.sees(CellarFloor.POOL_X1 - 0.2f, CellarFloor.POOL_X2 + 0.2f)) {
        val k = 0.5f + 0.5f * sin(pen.t * 1.3f)
        clipRectScene(st, CellarFloor.POOL_X1 - 0.1f, CellarFloor.POOL_X2 + 0.1f, 0.33f, BACK) {
            inScene(st) {
                translate(0f, -0.3f * u) {
                    drawPath(cs.caustics2, Color.White, alpha = 0.1f + 0.1f * k, style = Stroke(pen.lw * 2f, cap = StrokeCap.Round))
                }
            }
        }
    }
    skirtingColor(st, pen, x0, x1, Color(0xFF2F8FD6))
}

private fun DrawScope.poolBasin(st: Stage, pen: Pen, cs: CellarStatic) {
    val u = st.u
    val x1 = CellarFloor.POOL_X1
    val x2 = CellarFloor.POOL_X2
    if (!st.sees(x1 - 0.1f, x2 + 0.1f)) return
    val line = CellarFloor.POOL_LINE
    val bed = CellarFloor.POOL_BED
    val basin = Rect(st.o(x1, line), st.o(x2, bed))
    // The inside of the basin, deeper and darker toward the bottom, with tiles and a lane line.
    drawRect(Brush.verticalGradient(listOf(Color(0xFF7BE8F0), Color(0xFF2FA6D6), Color(0xFF1C74B8)), startY = basin.top, endY = basin.bottom), basin.topLeft, basin.size)
    inScene(st) { drawPoints(cs.basin, PointMode.Lines, Color.White, strokeWidth = pen.lw * 0.6f, alpha = 0.3f) }
    val laneY = (line + 0.1f) * u
    drawLine(Color(0xFF1A4F8F), Offset(basin.left, laneY), Offset(basin.right, laneY), strokeWidth = pen.lw * 3f)
    // Caustics, moving.
    val k = 0.5f + 0.5f * sin(pen.t * 1.7f)
    clipPath(Path().apply { addRect(basin) }) {
        inScene(st) {
            drawPath(cs.caustics, Color.White, alpha = 0.18f + 0.18f * k, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
            drawPath(cs.caustics2, Color.White, alpha = 0.36f - 0.18f * k, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        }
        // Round lights in the basin wall glow turquoise.
        for (lx in floatArrayOf(x1 + 0.15f, x2 - 0.15f)) {
            val c = st.o(lx, line + 0.075f)
            drawCircle(safeRadialGradient(listOf(Color(0xFFFFFFFF).copy(alpha = 0.8f), Color(0x007BE8F0)), c, 0.07f * u), 0.07f * u, c)
            drawCircle(Color.White, 0.008f * u, c)
        }
    }
    // The coping: a white lip along the deck edge at either end, and the ladder at the back.
    for (ex in floatArrayOf(x1, x2)) {
        val lip = Rect(st.o(ex - 0.012f, line - 0.012f), st.o(ex + 0.012f, bed + 0.005f))
        drawRect(Color(0xFFF3E6CF), lip.topLeft, lip.size)
        drawLine(Ink.line, lip.topLeft, Offset(lip.left, lip.bottom), strokeWidth = pen.lw * 0.8f)
        drawLine(Ink.line, Offset(lip.right, lip.top), Offset(lip.right, lip.bottom), strokeWidth = pen.lw * 0.8f)
    }
    drawLine(Ink.line, basin.topLeft, Offset(basin.right, basin.top), strokeWidth = pen.lw * 0.9f)
    poolLadder(st, pen, 5.98f)
}

private fun DrawScope.poolLadder(st: Stage, pen: Pen, x: Float) {
    val u = st.u
    val chrome = Color(0xFFD8E2EC)
    for (dx in floatArrayOf(-0.03f, 0.03f)) {
        val p = Path().apply {
            moveTo(st.x(x + dx), 0.9f * u)
            lineTo(st.x(x + dx), 0.72f * u)
            quadraticTo(st.x(x + dx), 0.66f * u, st.x(x + dx * 2.2f), 0.66f * u)
        }
        drawPath(p, Ink.line, style = Stroke(0.013f * u, cap = StrokeCap.Round))
        drawPath(p, chrome, style = Stroke(0.009f * u, cap = StrokeCap.Round))
    }
    for (k in 0..2) {
        val y = (0.8f + k * 0.045f) * u
        drawLine(Ink.line, Offset(st.x(x - 0.03f), y), Offset(st.x(x + 0.03f), y), strokeWidth = 0.011f * u, cap = StrokeCap.Round)
        drawLine(chrome, Offset(st.x(x - 0.03f), y), Offset(st.x(x + 0.03f), y), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
    }
}

// ------------------------------------------------------------------------------------------ party room

private fun DrawScope.partyWall(st: Stage, pen: Pen, cs: CellarStatic, x0: Float, x1: Float) {
    val u = st.u
    val w = (x1 - x0) * u
    drawRect(Color(0xFF3A2670), Offset(st.x(x0), st.backgroundTop), Size(w, BACK * u - st.backgroundTop))
    drawRect(Brush.verticalGradient(listOf(Color(0xFF241548), Color(0x00241548)), startY = SKY_TOP * u, endY = 0.5f * u), Offset(st.x(x0), st.backgroundTop), Size(w, 0.5f * u - st.backgroundTop))
    val colors = listOf(Color(0xFFFF5FA8), Color(0xFF2FD6C8), Color(0xFFFFC83D), Color(0xFF8B7BFF))
    inScene(st) {
        for ((i, p) in cs.memphis.withIndex()) {
            drawPath(p, colors[i], alpha = 0.85f)
            drawPath(p, Ink.line, alpha = 0.5f, style = pen.thin)
        }
        drawPoints(cs.dots, PointMode.Points, Color.White, strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round, alpha = 0.6f)
    }
    // A stripe of mirror tiles along the lower wall.
    drawRect(Color(0xFF241548), Offset(st.x(x0), 0.62f * u), Size(w, (BACK - 0.62f) * u))
    drawRect(Color(0xFF4A3A8F), Offset(st.x(x0), 0.62f * u), Size(w, 0.01f * u))
    drawLine(Ink.line, Offset(st.x(x0), 0.62f * u), Offset(st.x(x1), 0.62f * u), strokeWidth = pen.lw * 0.7f)
    skirtingColor(st, pen, x0, x1, Color(0xFF1B1038))
}

private fun DrawScope.partyThings(st: Stage, pen: Pen, cs: CellarStatic) {
    val u = st.u
    val t = pen.t
    val flagColors = listOf(Color(0xFFFF5FA8), Color(0xFF2FD6C8), Color(0xFFFFC83D), Color(0xFF8B7BFF), Color(0xFFFF9F43))
    // Bunting along the top, fluttering a little.
    inScene(st) {
        drawPath(cs.buntingLine, Ink.line, style = pen.thin)
        for ((i, p) in cs.bunting.withIndex()) drawPath(p, flagColors[i])
        for (p in cs.bunting) drawPath(p, Ink.line, alpha = 0.7f, style = pen.thin)
    }
    // The stone alcove round the tunnel, and the rails.
    if (st.sees(9.2f, 10.1f)) {
        inScene(st) {
            drawPath(cs.stones, Color(0xFF8D8498))
            drawPath(cs.stones, Ink.line, alpha = 0.7f, style = pen.thin)
        }
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1C1430).copy(alpha = 0.55f), Color.Transparent), startY = 0.18f * u, endY = 0.5f * u), Offset(st.x(9.5f), 0.18f * u), Size(0.6f * u, 0.32f * u))
        val shadow = Path().apply { floorQuad(u, st.cam, floorEdge(9.2f, PlaceId.FRONT), 10.4f, PlaceId.FRONT, BACK) }
        drawPath(shadow, Color(0xFF1C1430), alpha = 0.18f)
        inScene(st) {
            drawPoints(cs.rails.sleepers, PointMode.Lines, Color(0xFF6E4A30), strokeWidth = pen.lw * 3f, cap = StrokeCap.Round)
            drawPoints(cs.rails.railsA, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 3.4f, cap = StrokeCap.Round)
            drawPoints(cs.rails.railsB, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 3.4f, cap = StrokeCap.Round)
            drawPoints(cs.rails.railsA, PointMode.Lines, Color(0xFFB8C0D0), strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
            drawPoints(cs.rails.railsB, PointMode.Lines, Color(0xFFB8C0D0), strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
        }
    }
    // Mirror-glints on the dark wall panel.
    for (k in 0 until 4) {
        val gx = 7.2f + k * 0.8f
        if (!st.sees(gx, gx + 0.2f)) continue
        val a = 0.12f + 0.1f * sin(t * 1.3f + k * 1.7f)
        drawLine(Color.White, st.o(gx, 0.8f), st.o(gx + 0.05f, 0.63f), strokeWidth = pen.lw * 3f, alpha = a)
    }
}

// =============================================================================================== floors

private fun DrawScope.roomFloor(st: Stage, pen: Pen, cs: CellarStatic, i: Int, x0: Float, x1: Float) {
    val u = st.u
    val xf0 = floorEdge(x0, PlaceId.FRONT)
    val xf1 = floorEdge(x1, PlaceId.FRONT)
    val area = Path().apply { floorQuad(u, st.cam, xf0, xf1, PlaceId.FRONT, BACK) }
    val (top, bottom) = when (i) {
        0 -> Color(0xFF9C6736) to Color(0xFFC48C54)
        1 -> Color(0xFFCFE4EA) to Color(0xFFF1FAFB)
        2 -> Color(0xFF2B2D38) to Color(0xFF4A4D5C)
        3 -> Color(0xFFD9CDB6) to Color(0xFFF6EBD6)
        else -> Color(0xFF1B1038) to Color(0xFF38256E)
    }
    clipPath(area) {
        drawRect(Brush.verticalGradient(listOf(top, bottom), startY = BACK * u, endY = PlaceId.FRONT * u), Offset(st.x(xf0 - 1f), BACK * u), Size((xf1 - xf0 + 3f) * u, (PlaceId.FRONT - BACK) * u))
        inScene(st) {
            when (i) {
                0 -> {
                    drawPoints(cs.planks.grain, PointMode.Lines, Color(0xFF8A5A30), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
                    drawPoints(cs.planks.joints, PointMode.Lines, Color(0xFF6E4524), strokeWidth = pen.lw * 0.75f)
                    drawPoints(cs.planks.ends, PointMode.Lines, Color(0xFF6E4524), strokeWidth = pen.lw * 0.75f)
                }
                1 -> {
                    drawPath(cs.checker, Color(0xFF9FD3DD))
                    drawPath(cs.checker, Color(0xFF6FA8B4), alpha = 0.5f, style = pen.thin)
                }
                2 -> {
                    drawPoints(cs.diamonds, PointMode.Lines, Color(0xFF6E7288), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
                }
                3 -> {
                    drawPoints(cs.deck, PointMode.Lines, Color(0xFFA9CBE0), strokeWidth = pen.lw * 0.7f)
                }
                else -> {
                    drawPoints(cs.floorGloss, PointMode.Lines, Color(0xFF6B5AC0), strokeWidth = pen.lw * 2.2f, alpha = 0.35f)
                }
            }
        }
        if (i == 2) {
            // The furnace glow on the dark floor.
            val c = st.o(4.2f, 0.92f)
            val k = 0.55f + 0.2f * sin(pen.t * 1.7f)
            drawCircle(safeRadialGradient(listOf(Color(0xFFFF8A2E).copy(alpha = 0.4f * k), Color(0x00FF8A2E)), c, 0.45f * u), 0.45f * u, c)
        }
        drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.18f), Color.Transparent), startY = BACK * u, endY = (BACK + 0.06f) * u), Offset(st.x(xf0 - 1f), BACK * u), Size((xf1 - xf0 + 3f) * u, 0.06f * u))
    }
    // The cut edge of the floor slab shows a little line where one room's floor meets the next.
    if (i > 0) {
        val a = Offset(st.x(xf0), PlaceId.FRONT * u)
        val b = Offset(st.x(floorEdge(x0, BACK)), BACK * u)
        drawLine(Ink.line, a, b, strokeWidth = pen.lw * 0.8f, alpha = 0.6f)
    }
}

// ============================================================================== pillars, beam, signs

private fun DrawScope.skirtingColor(st: Stage, pen: Pen, x0: Float, x1: Float, color: Color) {
    val u = st.u
    val l = st.x(x0)
    val w = (x1 - x0) * u
    drawRect(color, Offset(l, (BACK - 0.026f) * u), Size(w, 0.026f * u))
    drawLine(color.lighten(0.35f), Offset(l, (BACK - 0.023f) * u), Offset(l + w, (BACK - 0.023f) * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(l, (BACK - 0.026f) * u), Offset(l + w, (BACK - 0.026f) * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(l, BACK * u), Offset(l + w, BACK * u), strokeWidth = pen.lw)
}

private fun DrawScope.pillar(st: Stage, pen: Pen, x: Float) {
    if (!st.sees(x - 0.1f, x + 0.15f)) return
    val u = st.u
    val stone = Color(0xFFC9BBA8)
    val half = 0.034f
    val front = Rect(st.o(x - half, SKY_TOP), st.o(x + half, BACK))
    box3d(front, 0.05f * u, stone, pen, top = stone.lighten(0.2f), side = stone.darken(0.25f))
    // Stone blocks: joints across, and a capital and a base.
    var y = 0.12f
    var k = 0
    while (y < BACK - 0.03f) {
        drawLine(stone.darken(0.35f), st.o(x - half, y), st.o(x + half, y), strokeWidth = pen.lw * 0.7f)
        drawLine(stone.darken(0.35f), st.o(x - half + (k % 2) * 0.034f + 0.017f, y), st.o(x - half + (k % 2) * 0.034f + 0.017f, y + 0.08f), strokeWidth = pen.lw * 0.6f)
        y += 0.08f
        k++
    }
    val capital = Rect(st.o(x - half - 0.012f, 0.045f), st.o(x + half + 0.012f, 0.085f))
    box3d(capital, 0.058f * u, stone.lighten(0.08f), pen, radius = 0.004f * u)
    val base = Rect(st.o(x - half - 0.012f, BACK - 0.04f), st.o(x + half + 0.012f, BACK))
    box3d(base, 0.058f * u, stone.lighten(0.08f), pen, radius = 0.004f * u)
}

/** The ceiling beam with its brackets, a blue water pipe and a thinner brass one running the whole way. */
private fun DrawScope.ceiling(st: Stage, pen: Pen) {
    val u = st.u
    val wood = Color(0xFF6E4630)
    drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.55f), Ink.line.copy(alpha = 0f)), startY = SKY_TOP * u, endY = 0.0f), Offset(0f, st.backgroundTop), Size(st.w, -st.backgroundTop))
    // The beam.
    drawRect(wood, Offset(0f, 0f), Size(st.w, 0.05f * u))
    drawRect(wood.lighten(0.18f), Offset(0f, 0.004f * u), Size(st.w, 0.01f * u))
    drawRect(wood.darken(0.2f), Offset(0f, 0.04f * u), Size(st.w, 0.01f * u))
    drawLine(Ink.line, Offset(0f, 0.05f * u), Offset(st.w, 0.05f * u), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(0f, 0f), Offset(st.w, 0f), strokeWidth = pen.lw * 0.7f)
    var bx = kotlin.math.floor(st.left / 0.5f) * 0.5f
    while (bx < st.right) {
        val l = st.x(bx)
        drawLine(wood.darken(0.35f), Offset(l, 0f), Offset(l, 0.05f * u), strokeWidth = pen.lw * 0.7f)
        bx += 0.5f
    }
    // The pipes.
    val blue = Color(0xFF8FA3B8)
    drawLine(Ink.line, Offset(0f, 0.075f * u), Offset(st.w, 0.075f * u), strokeWidth = 0.026f * u + pen.lw * 2f, cap = StrokeCap.Butt)
    drawLine(blue, Offset(0f, 0.075f * u), Offset(st.w, 0.075f * u), strokeWidth = 0.026f * u)
    drawLine(blue.lighten(0.5f), Offset(0f, 0.069f * u), Offset(st.w, 0.069f * u), strokeWidth = 0.006f * u)
    drawLine(blue.darken(0.3f), Offset(0f, 0.085f * u), Offset(st.w, 0.085f * u), strokeWidth = 0.005f * u)
    val brass = Color(0xFFE0B04A)
    drawLine(Ink.line, Offset(0f, 0.113f * u), Offset(st.w, 0.113f * u), strokeWidth = 0.013f * u + pen.lw * 2f)
    drawLine(brass, Offset(0f, 0.113f * u), Offset(st.w, 0.113f * u), strokeWidth = 0.013f * u)
    drawLine(brass.lighten(0.55f), Offset(0f, 0.109f * u), Offset(st.w, 0.109f * u), strokeWidth = 0.003f * u)
    // Pipe clamps and flanges.
    var cx = kotlin.math.floor(st.left / 0.6f) * 0.6f + 0.1f
    while (cx < st.right) {
        val l = st.x(cx)
        drawRect(Color(0xFF5A6078), Offset(l - 0.008f * u, 0.058f * u), Size(0.016f * u, 0.06f * u))
        drawRect(Ink.line, Offset(l - 0.008f * u, 0.058f * u), Size(0.016f * u, 0.06f * u), style = pen.thin)
        cx += 0.6f
    }
    var fx = kotlin.math.floor(st.left / 0.9f) * 0.9f + 0.45f
    while (fx < st.right) {
        val l = st.x(fx)
        inkedRound(Rect(l - 0.008f * u, 0.06f * u, l + 0.008f * u, 0.09f * u), 0.003f * u, Color(0xFF6E7A94), pen, shade = false)
        fx += 0.9f
    }
}

/** A hanging sign for a room: a rounded board on two chains with a picture of what happens there. */
private fun DrawScope.roomSign(st: Stage, pen: Pen, room: Int, x: Float) {
    val u = st.u
    val sway = sin(pen.t * 0.8f + room) * 0.003f * u
    val c = st.o(x, 0.19f)
    val w = 0.1f * u
    val h = 0.082f * u
    for (s in floatArrayOf(-1f, 1f)) drawLine(Ink.line, Offset(c.x + s * w * 0.34f, 0.115f * u), Offset(c.x + s * w * 0.34f + sway, c.y - h / 2f), strokeWidth = pen.lw * 0.8f)
    val board = Rect(c.x - w / 2f + sway, c.y - h / 2f, c.x + w / 2f + sway, c.y + h / 2f)
    val color = listOf(Color(0xFFE3B27A), Color(0xFF7FD3E4), Color(0xFFE0B04A), Color(0xFF54CFCB), Color(0xFFFF5FA8))[room]
    inkedRound(board, 0.012f * u, color, pen)
    inkedRound(Rect(board.left + 0.006f * u, board.top + 0.006f * u, board.right - 0.006f * u, board.bottom - 0.006f * u), 0.008f * u, Color(0xFFFFFBF0), pen, shade = false)
    val m = board.center
    val s = h * 0.34f
    when (room) {
        0 -> { // a hammer
            val handle = Path().apply { moveTo(m.x - s * 0.12f, m.y - s * 0.2f); lineTo(m.x + s * 0.12f, m.y - s * 0.2f); lineTo(m.x + s * 0.1f, m.y + s); lineTo(m.x - s * 0.1f, m.y + s); close() }
            inked(handle, Color(0xFFA0663B), pen, shade = false)
            inkedRound(Rect(m.x - s * 0.62f, m.y - s * 0.72f, m.x + s * 0.62f, m.y - s * 0.16f), 0.004f * u, Color(0xFF8A93AA), pen, shade = false)
        }
        1 -> { // a sock and a bubble
            val sock = Path().apply {
                moveTo(m.x - s * 0.4f, m.y - s); lineTo(m.x + s * 0.1f, m.y - s); lineTo(m.x + s * 0.1f, m.y + s * 0.2f)
                quadraticTo(m.x + s * 0.8f, m.y + s * 0.25f, m.x + s * 0.7f, m.y + s * 0.8f)
                quadraticTo(m.x, m.y + s, m.x - s * 0.4f, m.y + s * 0.4f); close()
            }
            inked(sock, Color(0xFFFF8FB1), pen, shade = false)
            drawLine(Color.White, Offset(m.x - s * 0.4f, m.y - s * 0.55f), Offset(m.x + s * 0.1f, m.y - s * 0.55f), strokeWidth = pen.lw * 1.5f)
            drawCircle(Color(0xFF7CCBFF), s * 0.26f, Offset(m.x + s * 0.55f, m.y - s * 0.55f))
            drawCircle(Ink.line, s * 0.26f, Offset(m.x + s * 0.55f, m.y - s * 0.55f), style = pen.thin)
        }
        2 -> { // a flame
            val flame = fxFlamePath(m.x, m.y + s, s * 1.2f, s * 2f, 0f)
            inked(flame, Color(0xFFFF7A2E), pen, shade = false)
            drawPath(fxFlamePath(m.x, m.y + s, s * 0.6f, s * 1.1f, 0f), Color(0xFFFFE680))
        }
        3 -> { // waves
            for (k in -1..1) {
                val y = m.y + k * s * 0.55f
                val p = Path().apply {
                    moveTo(m.x - s * 1.2f, y)
                    quadraticTo(m.x - s * 0.6f, y - s * 0.4f, m.x, y)
                    quadraticTo(m.x + s * 0.6f, y + s * 0.4f, m.x + s * 1.2f, y)
                }
                drawPath(p, Color(0xFF2F8FD6), style = Stroke(pen.lw * 2.2f, cap = StrokeCap.Round))
            }
        }
        else -> { // a note and a star
            drawOval(Color(0xFF8B5CF6), Offset(m.x - s * 0.9f, m.y + s * 0.2f), Size(s * 0.8f, s * 0.6f))
            drawLine(Color(0xFF8B5CF6), Offset(m.x - s * 0.15f, m.y + s * 0.5f), Offset(m.x - s * 0.15f, m.y - s * 0.8f), strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
            drawLine(Color(0xFF8B5CF6), Offset(m.x - s * 0.15f, m.y - s * 0.8f), Offset(m.x + s * 0.45f, m.y - s * 0.5f), strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
            drawPath(starPath(Offset(m.x + s * 0.7f, m.y + s * 0.3f), s * 0.5f, s * 0.22f), Color(0xFFFFC83D))
        }
    }
}

// ================================================================================================ bits

/** A vertical pipe: a rounded barrel with a light stripe, a shaded side and an ink rim. */
private fun DrawScope.fxPipe(r: Rect, pen: Pen, color: Color) {
    drawRoundRect(color, r.topLeft, r.size, androidx.compose.ui.geometry.CornerRadius(r.width * 0.3f))
    drawRect(color.lighten(0.5f), Offset(r.left + r.width * 0.2f, r.top), Size(r.width * 0.2f, r.height))
    drawRect(color.darken(0.3f), Offset(r.left + r.width * 0.7f, r.top), Size(r.width * 0.2f, r.height))
    drawRoundRect(Ink.line, r.topLeft, r.size, androidx.compose.ui.geometry.CornerRadius(r.width * 0.3f), style = pen.stroke)
}

/** A round pressure gauge on the wall with a needle that shivers. */
internal fun DrawScope.cellarGauge(st: Stage, pen: Pen, x: Float, y: Float, r: Float, level: Float) {
    val u = st.u
    val c = st.o(x, y)
    val rr = r * u
    drawCircle(Ink.line, rr + pen.lw * 1.4f, c)
    drawCircle(Color(0xFFE0B04A), rr + pen.lw * 0.2f, c)
    drawCircle(Color(0xFFFFFBF0), rr * 0.82f, c)
    for (k in 0..8) {
        val a = (150f + k * 30f) * 0.017453f
        val green = k in 3..5
        drawLine(if (green) Color(0xFF3BC46B) else Ink.line, Offset(c.x + cos(a) * rr * 0.6f, c.y + sin(a) * rr * 0.6f), Offset(c.x + cos(a) * rr * 0.78f, c.y + sin(a) * rr * 0.78f), strokeWidth = pen.lw * (if (green) 2f else 1f), cap = StrokeCap.Round)
    }
    val a = (150f + (level + sin(0f) * 0f) * 240f) * 0.017453f
    drawLine(Ink.line, c, Offset(c.x + cos(a) * rr * 0.7f, c.y + sin(a) * rr * 0.7f), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
    drawCircle(Color(0xFFE0B04A), rr * 0.12f, c)
    drawCircle(Ink.line, rr * 0.12f, c, style = pen.thin)
    drawCircle(Color.White.copy(alpha = 0.6f), rr * 0.18f, Offset(c.x - rr * 0.4f, c.y - rr * 0.45f))
}

/** Clips to a scene-space rectangle (x from [x0] to [x1], y from [y0] to [y1] in scene units). */
private fun DrawScope.clipRectScene(st: Stage, x0: Float, x1: Float, y0: Float, y1: Float, block: DrawScope.() -> Unit) {
    clipRect(st.x(x0), y0 * st.u, st.x(x1), y1 * st.u, block = block)
}
