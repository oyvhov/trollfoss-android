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
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Storhuset, attic (Loftet): the background. Four rooms in a row, each with its own look:
 *  0 the storage: weathered planks, A-frame rafters, a sunbeam full of dust and a few leaning things;
 *  1 the ghost's nook: pale blue boards, stencilled stars, a painted moon and a round rug;
 *  2 the tower: a round brass-ribbed room with a big arched window onto the real sky (pen.night);
 *  3 the secret room: velvet, gold and a deep red carpet.
 * Walls and floors the child has chosen in the home designer replace each room's own paper and boards; the
 * rafters, the window and the posts stay. All geometry is built once per scale (see [Memo]) and drawn in a
 * few batches, so the whole attic costs a couple of milliseconds a frame.
 */

private const val BACK = 0.80f
private const val FLOOR_Y = 0.90f
private val EDGES = floatArrayOf(0f, 3f, 5.5f, 7.5f, 9f)

/** The wall partitions stand a little to the right of the floor boundaries, because the floor recedes up and right. */
private val WALL_SHIFT: Float = recede(BACK) - recede(FLOOR_Y)
private val FRONT_SHIFT: Float = recede(FRONT_Y) - recede(FLOOR_Y)

/** Where room [i] starts on the back wall (scene x). */
internal fun atWallEdge(i: Int): Float = when (i) {
    0 -> -3f
    4 -> 12f
    else -> EDGES[i] + WALL_SHIFT
}

/** Where room [i] starts on the floor's front edge (scene x). */
internal fun atFloorEdge(i: Int): Float = when (i) {
    0 -> -3f
    4 -> 12f
    else -> EDGES[i] + FRONT_SHIFT
}

/** The middle of the tower's back wall. */
internal val AT_TOWER_X: Float = (atWallEdge(2) + atWallEdge(3)) / 2f

// ---------------------------------------------------------------------------------------------- geometry

private class Truss(val legL: Path, val legR: Path, val collar: Path, val post: Path, val lights: List<Offset>, val nails: List<Offset>)

/** An A-frame of two rafters, a collar beam and a king post, feet on the floor line at [xa] and [xb] (scene units). */
private fun truss(u: Float, xa: Float, xb: Float, apexY: Float, collarY: Float, th: Float): Truss {
    val xm = (xa + xb) / 2f
    val t = th * u
    val legL = beamPath(xa * u, BACK * u, xm * u, apexY * u, t)
    val legR = beamPath(xb * u, BACK * u, xm * u, apexY * u, t)
    val k = (collarY - BACK) / (apexY - BACK)
    val xl = xa + k * (xm - xa)
    val xr = xb - k * (xb - xm)
    val collar = Path().apply { addRect(Rect(xl * u, (collarY - th * 0.42f) * u, xr * u, (collarY + th * 0.42f) * u)) }
    val post = Path().apply { addRect(Rect((xm - th * 0.3f) * u, (apexY + th * 0.4f) * u, (xm + th * 0.3f) * u, (collarY - th * 0.4f) * u)) }
    // A pale line along the top edge of each rafter, as if light fell on it.
    val lights = ArrayList<Offset>()
    val tx = (xm - xa)
    val ty = (apexY - BACK)
    val len = kotlin.math.hypot(tx, ty)
    val nx = ty / len * th * 0.28f
    val ny = -tx / len * th * 0.28f
    lights += Offset((xa + nx) * u, (BACK + ny) * u)
    lights += Offset((xm + nx) * u, (apexY + ny) * u)
    lights += Offset((xb - nx) * u, (BACK + ny) * u)
    lights += Offset((xm - nx) * u, (apexY + ny) * u)
    lights += Offset(xl * u, (collarY - th * 0.3f) * u)
    lights += Offset(xr * u, (collarY - th * 0.3f) * u)
    val nails = listOf(
        Offset(xm * u, (apexY + th * 0.8f) * u),
        Offset((xl + 0.03f) * u, collarY * u),
        Offset((xr - 0.03f) * u, collarY * u),
        Offset(xm * u, collarY * u),
    )
    return Truss(legL, legR, collar, post, lights, nails)
}

/** A flat star lying on the floor: [n] points, outer radius [ro], inner [ri], squashed in depth by [flat]. */
private fun floorStar(u: Float, cx: Float, cy: Float, ro: Float, ri: Float, n: Int, flat: Float): Path = Path().apply {
    for (i in 0 until n * 2) {
        val r = if (i % 2 == 0) ro else ri
        val a = i * 3.1415927f / n - 1.5707964f
        val dz = r * sin(a) * flat
        val x = (cx + r * cos(a) + Oblique.DX * dz) * u
        val y = (cy + Oblique.DY * dz) * u
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private class Prop(val body: Path, val color: Color, val details: Path? = null, val detailColor: Color = Color.Black)

private class AtticStatic(val u: Float) {
    // ---- storage
    val boards = ArrayList<Offset>()
    val joints = ArrayList<Offset>()
    val trusses: List<Truss> = listOf(
        truss(u, -0.05f, 1.5f, -0.3f, 0.1f, 0.075f),
        truss(u, 1.5f, 3.08f, -0.3f, 0.1f, 0.075f),
    )
    val storageWall: Brush = Brush.verticalGradient(0f to Color(0xFF4F3827), 0.4f to Color(0xFF7C5C3C), 1f to Color(0xFFA7805A), startY = SKY_TOP * u, endY = BACK * u)
    val beamBrush: Brush = Brush.verticalGradient(0f to Color(0x66000000), 1f to Color(0x00000000), startY = SKY_TOP * u, endY = 0.25f * u)

    // sunbeam from the round window at (2.15, 0.33), and its patch on the floor
    val beamDay = Path().apply { poly(2.03f * u, 0.3f * u, 2.27f * u, 0.3f * u, 1.82f * u, 0.82f * u, 1.18f * u, 0.82f * u) }
    val beamPatch = Path().apply { floorDisc(u, 0f, 1.58f, 0.885f, 0.34f, 0.2f) }
    val beamBrushDay: Brush = Brush.verticalGradient(0f to Color(0x55FFE7A8), 1f to Color(0x1AFFE7A8), startY = 0.3f * u, endY = 0.82f * u)
    val beamBrushNight: Brush = Brush.verticalGradient(0f to Color(0x4D9CBBFF), 1f to Color(0x0F9CBBFF), startY = 0.3f * u, endY = 0.82f * u)

    // leaning things against the storage wall
    val props = ArrayList<Prop>()

    // ---- nook
    val nookBoards = ArrayList<Offset>()
    val nookStars = ArrayList<Offset>()
    val nookBigStars = ArrayList<Path>()
    val nookTruss: Truss = truss(u, atWallEdge(1) - 0.02f, atWallEdge(2) + 0.02f, -0.45f, 0.1f, 0.07f)
    val nookWall: Brush = Brush.verticalGradient(0f to Color(0xFF9DBBD3), 0.5f to AtC.blue, 1f to Color(0xFFCADFEE), startY = SKY_TOP * u, endY = BACK * u)
    val rug = ArrayList<Path>()
    val rugStar: Path = floorStar(u, 4.1f, 0.885f, 0.2f, 0.085f, 5, 0.24f)

    // ---- tower
    val stoneLines = ArrayList<Offset>()
    val window: Path = Path().apply {
        val x = AT_TOWER_X
        moveTo((x - 0.42f) * u, 0.62f * u)
        lineTo((x - 0.42f) * u, 0.3f * u)
        cubicTo((x - 0.42f) * u, 0.16f * u, (x - 0.22f) * u, 0.07f * u, x * u, 0.07f * u)
        cubicTo((x + 0.22f) * u, 0.07f * u, (x + 0.42f) * u, 0.16f * u, (x + 0.42f) * u, 0.3f * u)
        lineTo((x + 0.42f) * u, 0.62f * u)
        close()
    }
    val ribL: Path = Path().apply {
        val x = atWallEdge(2) + 0.04f
        moveTo(x * u, BACK * u)
        cubicTo(x * u, 0.12f * u, (AT_TOWER_X - 0.5f) * u, -0.28f * u, AT_TOWER_X * u, -0.36f * u)
    }
    val ribR: Path = Path().apply {
        val x = atWallEdge(3) - 0.04f
        moveTo(x * u, BACK * u)
        cubicTo(x * u, 0.12f * u, (AT_TOWER_X + 0.5f) * u, -0.28f * u, AT_TOWER_X * u, -0.36f * u)
    }
    val ring: Path = Path().apply {
        moveTo((atWallEdge(2) + 0.02f) * u, 0.0f * u)
        quadraticTo(AT_TOWER_X * u, -0.1f * u, (atWallEdge(3) - 0.02f) * u, 0.0f * u)
    }
    val hills: Path = Path().apply {
        val x = AT_TOWER_X
        moveTo((x - 0.5f) * u, 0.62f * u)
        lineTo((x - 0.5f) * u, 0.5f * u)
        quadraticTo((x - 0.3f) * u, 0.4f * u, (x - 0.1f) * u, 0.5f * u)
        quadraticTo((x + 0.1f) * u, 0.58f * u, (x + 0.25f) * u, 0.46f * u)
        quadraticTo((x + 0.4f) * u, 0.38f * u, (x + 0.5f) * u, 0.48f * u)
        lineTo((x + 0.5f) * u, 0.62f * u)
        close()
    }
    val pines = Path().apply {
        for ((dx, h) in listOf(-0.3f to 0.1f, -0.22f to 0.075f, 0.06f to 0.085f, 0.29f to 0.11f, 0.35f to 0.07f)) {
            addPine((AT_TOWER_X + dx) * u, 0.52f * u, h * 0.55f * u, h * u)
        }
    }
    val villageLights: List<Offset> = listOf(-0.15f to 0.55f, -0.08f to 0.53f, 0.0f to 0.56f, 0.12f to 0.54f, 0.19f to 0.52f).map { Offset((AT_TOWER_X + it.first) * u, it.second * u) }
    val towerBeamDay = Path().apply { poly((AT_TOWER_X - 0.35f) * u, 0.62f * u, (AT_TOWER_X + 0.35f) * u, 0.62f * u, (AT_TOWER_X + 0.1f) * u, 0.85f * u, (AT_TOWER_X - 0.75f) * u, 0.85f * u) }
    val compass: Path = floorStar(u, 6.5f, 0.885f, 0.5f, 0.15f, 8, 0.2f)
    val compassRing: Path = Path().apply { floorDisc(u, 0f, 6.5f, 0.885f, 0.52f, 0.2f, 24) }
    val compassInner: Path = Path().apply { floorDisc(u, 0f, 6.5f, 0.885f, 0.1f, 0.04f, 16) }

    // ---- secret room
    val lattice = ArrayList<Offset>()
    val latticeDots = ArrayList<Offset>()
    val panels = ArrayList<Path>()
    val glow: Brush = safeRadialGradient(0f to Color(0x55FFD27A), 1f to Color(0x00FFD27A), center = Offset(8.25f * u, 0.4f * u), radius = 1.0f * u)

    // ---- floors, one quad per room
    val floors: List<Path> = (0 until 4).map { i ->
        Path().apply { floorQuad(u, 0f, atFloorEdge(i).coerceAtLeast(-1.5f), atFloorEdge(i + 1).coerceAtMost(11f), FRONT_Y, BACK) }
    }
    val planks0 = buildPlanks(u, -1.2f, atFloorEdge(1) + 0.4f, BACK, 0.17f, 41)
    val planks1 = buildPlanks(u, atFloorEdge(1) - 0.4f, atFloorEdge(2) + 0.4f, BACK, 0.13f, 43)
    val tiles2 = buildPlanks(u, atFloorEdge(2) - 0.3f, atFloorEdge(3) + 0.4f, BACK, 0.22f, 47)
    val planks3 = buildPlanks(u, atFloorEdge(3) - 0.3f, 10.5f, BACK, 0.15f, 49)
    val carpet: Path = Path().apply { floorQuad(u, 0f, atFloorEdge(3) + 0.12f, 9.4f, FRONT_Y - 0.03f, BACK + 0.03f) }
    val carpetBorder: Path = Path().apply { floorQuad(u, 0f, atFloorEdge(3) + 0.2f, 9.3f, FRONT_Y - 0.045f, BACK + 0.045f) }
    val medallion: Path = Path().apply { floorDisc(u, 0f, 8.2f, 0.885f, 0.34f, 0.13f, 22) }
    val medallionStar: Path = floorStar(u, 8.2f, 0.885f, 0.3f, 0.11f, 6, 0.2f)
    val dust = ArrayList<Offset>()

    init {
        // ---- the storage wall: boards and butt joints
        var y = SKY_TOP + 0.02f
        var row = 0
        while (y < BACK) {
            boards.add(Offset(-0.3f * u, y * u))
            boards.add(Offset(atWallEdge(1) * u, y * u))
            for (k in 0 until 2) {
                val x = -0.2f + hash01(row * 2 + k, 61) * (atWallEdge(1) + 0.2f)
                joints.add(Offset(x * u, y * u))
                joints.add(Offset(x * u, (y + 0.075f) * u))
            }
            y += 0.075f
            row++
        }
        // ---- things leaning against the storage wall
        // a barrel
        props += Prop(
            Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(2.54f * u, 0.64f * u, 2.7f * u, 0.8f * u), androidx.compose.ui.geometry.CornerRadius(0.03f * u))) },
            Color(0xFF8A5E3A),
            linePath(2.54f * u, 0.69f * u, 2.7f * u, 0.69f * u).also { it.moveTo(2.54f * u, 0.755f * u); it.lineTo(2.7f * u, 0.755f * u) },
            Color(0xFF3F2C20),
        )
        // rolled carpets
        val rugCols = listOf(Color(0xFFC2584A), Color(0xFF4A8F98), Color(0xFFE0B04A))
        for ((i, c) in rugCols.withIndex()) {
            val cx = 2.9f + i * 0.075f
            val top = 0.5f + (i % 2) * 0.03f
            props += Prop(Path().apply { addRect(Rect((cx - 0.034f) * u, top * u, (cx + 0.034f) * u, BACK * u)) }, c, Path().apply { addOval(Rect((cx - 0.034f) * u, (top - 0.012f) * u, (cx + 0.034f) * u, (top + 0.012f) * u)) }, c.lighten(0.3f))
        }
        // picture frames
        val frames = listOf(Triple(1.45f, 0.14f, 0.2f), Triple(1.6f, 0.12f, 0.16f))
        for ((i, f) in frames.withIndex()) {
            props += Prop(Path().apply { addRect(Rect((f.first - f.second / 2f) * u, (BACK - f.third) * u, (f.first + f.second / 2f) * u, BACK * u)) }, if (i == 0) Color(0xFFC49A62) else Color(0xFF6E4630), Path().apply { addRect(Rect((f.first - f.second / 2f + 0.015f) * u, (BACK - f.third + 0.015f) * u, (f.first + f.second / 2f - 0.015f) * u, (BACK - 0.015f) * u)) }, if (i == 0) Color(0xFFB6D6C8) else Color(0xFFF1D9A8))
        }
        // ---- nook: vertical boards, a chair rail and stencilled stars
        var x = atWallEdge(1)
        while (x < atWallEdge(2)) {
            nookBoards.add(Offset(x * u, SKY_TOP * u))
            nookBoards.add(Offset(x * u, BACK * u))
            x += 0.13f
        }
        for (i in 0 until 46) {
            val sx = atWallEdge(1) + 0.05f + hash01(i, 71) * (atWallEdge(2) - atWallEdge(1) - 0.1f)
            val sy = SKY_TOP + 0.04f + hash01(i, 72) * (0.46f - SKY_TOP)
            nookStars.add(Offset(sx * u, sy * u))
        }
        for (i in 0 until 7) {
            val sx = atWallEdge(1) + 0.15f + hash01(i, 73) * (atWallEdge(2) - atWallEdge(1) - 0.3f)
            val sy = -0.2f + hash01(i, 74) * 0.55f
            nookBigStars += starPath(Offset(sx * u, sy * u), 0.016f * u, 0.0075f * u, hash01(i, 75) * 36f)
        }
        // the rug: a dark ring, a cream ring, a blue middle
        rug += Path().apply { floorDisc(u, 0f, 4.1f, 0.885f, 0.68f, 0.21f, 24) }
        rug += Path().apply { floorDisc(u, 0f, 4.1f, 0.885f, 0.6f, 0.185f, 24) }
        rug += Path().apply { floorDisc(u, 0f, 4.1f, 0.885f, 0.5f, 0.155f, 24) }
        // ---- tower: courses of stone
        var sy = SKY_TOP + 0.03f
        var srow = 0
        while (sy < BACK) {
            stoneLines.add(Offset((atWallEdge(2)) * u, sy * u))
            stoneLines.add(Offset((atWallEdge(3)) * u, sy * u))
            var sx = atWallEdge(2) + (srow % 2) * 0.12f + 0.08f
            while (sx < atWallEdge(3)) {
                stoneLines.add(Offset(sx * u, sy * u))
                stoneLines.add(Offset(sx * u, (sy + 0.09f) * u))
                sx += 0.24f
            }
            sy += 0.09f
            srow++
        }
        // ---- secret room: a gold lattice on velvet
        val lx0 = atWallEdge(3)
        val lx1 = 9.3f
        var d = lx0 - 1.2f
        while (d < lx1 + 0.2f) {
            lattice.add(Offset(d * u, 0.56f * u))
            lattice.add(Offset((d + 0.9f) * u, SKY_TOP * u))
            lattice.add(Offset(d * u, SKY_TOP * u))
            lattice.add(Offset((d + 0.9f) * u, 0.56f * u))
            d += 0.17f
        }
        var gx = lx0 + 0.04f
        while (gx < lx1) {
            var gy = SKY_TOP + 0.06f
            while (gy < 0.54f) {
                latticeDots.add(Offset(gx * u, gy * u))
                gy += 0.17f
            }
            gx += 0.17f
        }
        var px = lx0 + 0.03f
        while (px < lx1) {
            panels += Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(px * u, 0.6f * u, (px + 0.17f) * u, 0.77f * u), androidx.compose.ui.geometry.CornerRadius(0.012f * u))) }
            px += 0.2f
        }
        for (i in 0 until 30) dust.add(Offset(hash01(i, 81), hash01(i, 82)))
    }
}

private val atticStatic = Memo { u -> AtticStatic(u) }

// ---------------------------------------------------------------------------------------------- drawing

/** The place's back layer: walls, floor, windows, far scenery. */
internal fun DrawScope.atticBack(st: Stage, pen: Pen, styles: List<RoomStyle>) {
    val u = st.u
    val g = atticStatic.of(u)
    val n = pen.night
    for (i in 0 until 4) {
        val a = atWallEdge(i)
        val b = atWallEdge(i + 1)
        if (!st.sees(a - 0.1f, b + 0.1f)) continue
        val paper = styles.wallOf(i)
        val left = max(st.x(a), -2f)
        val right = min(st.x(b), st.w + 2f)
        if (paper > 0) {
            paperWall(st, paper, BACK, a, b)
        } else {
            when (i) {
                0 -> storageWall(st, g, left, right)
                1 -> nookWall(st, g, left, right)
                2 -> towerWall(st, g, left, right)
                else -> secretWall(st, g, left, right, pen)
            }
        }
    }
    // The roof's shadow lies over the top of every wall.
    drawRect(g.beamBrush, Offset(0f, SKY_TOP * u), Size(st.w, (0.25f - SKY_TOP) * u))
    // Floors, and the thresholds between them.
    for (i in 0 until 4) {
        if (!st.sees(atFloorEdge(i) - 0.1f, atFloorEdge(i + 1) + atWallEdge(1) - atFloorEdge(1) + 0.4f)) continue
        atticFloor(st, g, pen, styles, i)
    }
    // Rafters, ribs, posts and everything else that is built into the walls.
    if (st.sees(-0.3f, atWallEdge(1))) storageBeams(st, g, pen)
    if (st.sees(atWallEdge(1) - 0.2f, atWallEdge(2) + 0.2f)) nookBeams(st, g, pen)
    if (st.sees(atWallEdge(2) - 0.2f, atWallEdge(3) + 0.2f)) towerWindow(st, g, pen)
    if (st.sees(atWallEdge(3) - 0.2f, 9.3f)) secretTrim(st, g, pen)
    for (i in 1..3) if (st.sees(atWallEdge(i) - 0.1f, atWallEdge(i) + 0.1f)) post(st, pen, atWallEdge(i), i)
    // Light in the air: the sunbeam in the storage (a moonbeam by night), and dust that dances in it.
    if (st.sees(1.1f, 2.4f)) sunbeam(st, g, pen)
    dustMotes(st, g, pen)
    drawBase(st, pen, Color(0xFF7A5A3E), Color(0xFF3F2C20))
    if (n < 0f) Unit
}

/** What lies in front of everything: a few big, slow, out-of-focus specks of dust. */
internal fun DrawScope.atticFront(st: Stage, pen: Pen) {
    val u = st.u
    for (i in 0 until 9) {
        val x = wrap(hash01(i, 91) * (st.w + 80f) + pen.t * (3f + i) - st.cam * u * 0.5f, st.w + 80f) - 40f
        val y = (0.12f + 0.8f * hash01(i, 92)) * u + sin(pen.t * 0.4f + i * 1.7f) * 0.02f * u
        val r = (0.006f + 0.006f * hash01(i, 93)) * u
        drawCircle(Color(0xFFFFEFD0), r, Offset(x, y), alpha = 0.1f + 0.06f * atWave(pen.t, 0.8f, i * 1.3f))
    }
}

// ------------------------------------------------------------------------------------------------ walls

private fun DrawScope.storageWall(st: Stage, g: AtticStatic, left: Float, right: Float) {
    val u = st.u
    drawRect(g.storageWall, Offset(left, SKY_TOP * u - 2f), Size(right - left, (BACK - SKY_TOP) * u + 2f))
    clipRect(left, SKY_TOP * u - 2f, right, BACK * u) {
        inScene(st) {
            drawPoints(g.boards, PointMode.Lines, AtC.beamDark, strokeWidth = 1.3f * 1.3f, cap = StrokeCap.Butt, alpha = 0.45f)
            drawPoints(g.joints, PointMode.Lines, AtC.beamDark, strokeWidth = 1.2f * 1.3f, cap = StrokeCap.Butt, alpha = 0.32f)
        }
    }
}

private fun DrawScope.nookWall(st: Stage, g: AtticStatic, left: Float, right: Float) {
    val u = st.u
    drawRect(g.nookWall, Offset(left, SKY_TOP * u - 2f), Size(right - left, (BACK - SKY_TOP) * u + 2f))
    clipRect(left, SKY_TOP * u - 2f, right, BACK * u) {
        inScene(st) {
            drawPoints(g.nookBoards, PointMode.Lines, AtC.blueDark, strokeWidth = 1.4f, alpha = 0.5f)
            drawPoints(g.nookStars, PointMode.Points, Color(0xFFFFF3C4), strokeWidth = 0.007f * u, cap = StrokeCap.Round, alpha = 0.85f)
            for (s in g.nookBigStars) drawPath(s, Color(0xFFFFE18A), alpha = 0.9f)
            // The chair rail and a darker panelled lower wall.
            drawRect(AtC.blueDark, Offset(atWallEdge(1) * u, 0.55f * u), Size((atWallEdge(2) - atWallEdge(1)) * u, (BACK - 0.55f) * u), alpha = 0.55f)
            drawRect(AtC.cream, Offset(atWallEdge(1) * u, 0.53f * u), Size((atWallEdge(2) - atWallEdge(1)) * u, 0.025f * u))
            drawLine(Ink.line, Offset(atWallEdge(1) * u, 0.53f * u), Offset(atWallEdge(2) * u, 0.53f * u), strokeWidth = 1.5f)
            drawLine(Ink.line, Offset(atWallEdge(1) * u, 0.555f * u), Offset(atWallEdge(2) * u, 0.555f * u), strokeWidth = 1.2f)
        }
    }
}

private fun DrawScope.towerWall(st: Stage, g: AtticStatic, left: Float, right: Float) {
    val u = st.u
    val l = st.x(atWallEdge(2))
    val r = st.x(atWallEdge(3))
    // Darker toward the curved sides, lighter in the middle where the window throws its light.
    drawRect(
        Brush.horizontalGradient(0f to Color(0xFF16222E), 0.5f to AtC.navy, 1f to Color(0xFF16222E), startX = l, endX = r),
        Offset(left, SKY_TOP * u - 2f), Size(right - left, (BACK - SKY_TOP) * u + 2f),
    )
    clipRect(left, SKY_TOP * u - 2f, right, BACK * u) {
        inScene(st) { drawPoints(g.stoneLines, PointMode.Lines, Color(0xFF0E1822), strokeWidth = 1.5f, alpha = 0.55f) }
    }
}

private fun DrawScope.secretWall(st: Stage, g: AtticStatic, left: Float, right: Float, pen: Pen) {
    val u = st.u
    drawRect(
        Brush.verticalGradient(0f to AtC.velvetDark, 0.5f to AtC.velvet, 1f to Color(0xFF6A2332), startY = SKY_TOP * u, endY = BACK * u),
        Offset(left, SKY_TOP * u - 2f), Size(right - left, (BACK - SKY_TOP) * u + 2f),
    )
    clipRect(left, SKY_TOP * u - 2f, right, BACK * u) {
        inScene(st) {
            drawPoints(g.lattice, PointMode.Lines, AtC.gold, strokeWidth = 1.5f, alpha = 0.3f)
            drawPoints(g.latticeDots, PointMode.Points, AtC.goldLight, strokeWidth = 0.009f * u, cap = StrokeCap.Round, alpha = 0.6f)
            drawRect(AtC.mahogany, Offset(atWallEdge(3) * u, 0.58f * u), Size((9.6f - atWallEdge(3)) * u, (BACK - 0.58f) * u))
            for (p in g.panels) {
                drawPath(p, Color(0xFF6B3E2E))
                drawPath(p, Ink.line, style = pen.thin, alpha = 0.7f)
            }
            drawRect(AtC.gold, Offset(atWallEdge(3) * u, 0.565f * u), Size((9.6f - atWallEdge(3)) * u, 0.02f * u))
            drawLine(Ink.line, Offset(atWallEdge(3) * u, 0.565f * u), Offset(9.6f * u, 0.565f * u), strokeWidth = pen.lw * 0.8f)
            drawLine(Ink.line, Offset(atWallEdge(3) * u, 0.585f * u), Offset(9.6f * u, 0.585f * u), strokeWidth = pen.lw * 0.8f)
            drawRect(g.glow, Offset(atWallEdge(3) * u, SKY_TOP * u), Size(2.2f * u, (BACK - SKY_TOP) * u))
        }
    }
}

// ------------------------------------------------------------------------------------------------ floors

private fun DrawScope.atticFloor(st: Stage, g: AtticStatic, pen: Pen, styles: List<RoomStyle>, i: Int) {
    val u = st.u
    val area = g.floors[i]
    val chosen = styles.floorOf(i)
    if (chosen > 0) {
        layFloor(st, chosen, BACK, atFloorEdge(i), atFloorEdge(i + 1))
        return
    }
    val base = when (i) {
        0 -> Color(0xFF8A6A48)
        1 -> Color(0xFFD9C6A2)
        2 -> Color(0xFF38465A)
        else -> Color(0xFF3F2420)
    }
    inScene(st) {
        clipPath(area) {
            drawRect(
                Brush.verticalGradient(0f to base.darken(0.28f), 0.35f to base.darken(0.06f), 1f to base.lighten(0.08f), startY = BACK * u, endY = FRONT_Y * u),
                Offset(-1.5f * u, BACK * u), Size(13f * u, (FRONT_Y - BACK) * u),
            )
            val planks = when (i) {
                0 -> g.planks0
                1 -> g.planks1
                2 -> g.tiles2
                else -> g.planks3
            }
            drawPoints(planks.grain, PointMode.Lines, base.darken(0.14f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
            drawPoints(planks.joints, PointMode.Lines, base.darken(0.34f), strokeWidth = pen.lw * 0.75f)
            drawPoints(planks.ends, PointMode.Lines, base.darken(0.34f), strokeWidth = pen.lw * 0.75f)
        }
        when (i) {
            0 -> if (st.sees(0.9f, 2.3f)) drawPath(g.beamPatch, Color(0xFFFFE7A8), alpha = 0.14f * (1f - pen.night))
            1 -> if (st.sees(3.3f, 4.9f)) {
                drawPath(g.rug[0], Color(0xFF2F4F7A))
                drawPath(g.rug[0], Ink.line, style = pen.thin)
                drawPath(g.rug[1], AtC.cream)
                drawPath(g.rug[2], Color(0xFF79A7D3))
                drawPath(g.rug[2], Ink.line, style = pen.thin, alpha = 0.6f)
                drawPath(g.rugStar, Color(0xFFFFE18A))
                drawPath(g.rugStar, Ink.line, style = pen.thin, alpha = 0.7f)
            }
            2 -> if (st.sees(5.8f, 7.2f)) {
                drawPath(g.compassRing, Color(0xFF273446))
                drawPath(g.compassRing, AtC.brassDark, style = Stroke(pen.lw * 1.6f))
                drawPath(g.compass, AtC.brass)
                drawPath(g.compass, Ink.line, style = pen.stroke)
                drawPath(g.compassInner, AtC.brassDark)
                drawPath(g.compassInner, Ink.line, style = pen.thin)
            }
            else -> if (st.sees(7.6f, 9.1f)) {
                drawPath(g.carpet, AtC.velvet)
                drawPath(g.carpet, AtC.goldDark, style = Stroke(pen.lw * 2f))
                drawPath(g.carpetBorder, Color(0xFF5A1E2E))
                drawPath(g.carpetBorder, AtC.gold, style = Stroke(pen.lw * 1.4f))
                drawPath(g.medallion, Color(0xFF3F1220))
                drawPath(g.medallion, AtC.gold, style = Stroke(pen.lw * 1.4f))
                drawPath(g.medallionStar, AtC.gold)
                drawPath(g.medallionStar, Ink.line, style = pen.thin)
            }
        }
    }
    // The threshold between this room and the next: a darker board along the depth of the floor.
    if (i > 0) {
        val x0 = EDGES[i] + FRONT_SHIFT
        val x1 = EDGES[i] + WALL_SHIFT
        drawLine(Ink.line, Offset(st.x(x0), FRONT_Y * u), Offset(st.x(x1), BACK * u), strokeWidth = pen.lw * 1.1f, alpha = 0.5f)
    }
}

// ------------------------------------------------------------------------------------------------ built-in beams

private fun DrawScope.storageBeams(st: Stage, g: AtticStatic, pen: Pen) {
    inScene(st) {
        for (t in g.trusses) drawTruss(t, AtC.beam, pen)
        // The leaning things at the foot of the wall.
        for (p in g.props) {
            drawPath(p.body, p.color)
            p.details?.let {
                if (p.color == Color(0xFF8A5E3A)) drawPath(it, p.detailColor, style = Stroke(pen.lw * 1.4f)) else drawPath(it, p.detailColor)
                if (p.color != Color(0xFF8A5E3A)) drawPath(it, Ink.line, style = pen.thin)
            }
            drawPath(p.body, Ink.line, style = pen.stroke)
        }
    }
}

private fun DrawScope.drawTruss(t: Truss, wood: Color, pen: Pen) {
    drawPath(t.legL, wood)
    drawPath(t.legR, wood)
    drawPath(t.collar, wood.darken(0.06f))
    drawPath(t.post, wood.darken(0.12f))
    drawPoints(t.lights, PointMode.Lines, wood.lighten(0.35f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round, alpha = 0.5f)
    drawPath(t.legL, Ink.line, style = pen.stroke)
    drawPath(t.legR, Ink.line, style = pen.stroke)
    drawPath(t.collar, Ink.line, style = pen.stroke)
    drawPath(t.post, Ink.line, style = pen.stroke)
    for (n in t.nails) drawCircle(Ink.line.copy(alpha = 0.7f), pen.lw * 1.2f, n)
}

private fun DrawScope.nookBeams(st: Stage, g: AtticStatic, pen: Pen) {
    val u = st.u
    inScene(st) {
        drawTruss(g.nookTruss, Color(0xFFF1EBDD), pen)
        // A painted moon in a frame above the wing chair.
        val c = Offset(3.78f * u, 0.22f * u)
        val frame = Rect(c.x - 0.1f * u, c.y - 0.11f * u, c.x + 0.1f * u, c.y + 0.11f * u)
        drawRoundRect(Color(0xFFC49A62), frame.topLeft, frame.size, androidx.compose.ui.geometry.CornerRadius(0.012f * u))
        drawRoundRect(Ink.line, frame.topLeft, frame.size, androidx.compose.ui.geometry.CornerRadius(0.012f * u), style = pen.stroke)
        val inner = Rect(frame.left + 0.012f * u, frame.top + 0.012f * u, frame.right - 0.012f * u, frame.bottom - 0.012f * u)
        drawRect(Color(0xFF1B2A58), inner.topLeft, inner.size)
        clipRect(inner.left, inner.top, inner.right, inner.bottom) {
            drawCircle(Color(0xFFFFE9A8), 0.055f * u, Offset(c.x - 0.01f * u, c.y - 0.01f * u))
            drawCircle(Color(0xFF1B2A58), 0.048f * u, Offset(c.x + 0.022f * u, c.y - 0.026f * u))
            for ((dx, dy) in listOf(0.06f to -0.07f, -0.07f to 0.05f, 0.07f to 0.06f)) twinkle(Offset(c.x + dx * u, c.y + dy * u), 0.012f * u, Color.White, 0.5f + 0.5f * atWave(pen.t, 1.2f, dx * 20f))
            // a little house on a hill
            drawRect(Color(0xFF0C1230), Offset(inner.left, inner.bottom - 0.03f * u), Size(inner.width, 0.03f * u))
            drawRect(Color(0xFFFFD27A), Offset(c.x + 0.05f * u, inner.bottom - 0.04f * u), Size(0.012f * u, 0.012f * u))
        }
        drawRect(Ink.line, inner.topLeft, inner.size, style = pen.thin)
    }
}

private fun DrawScope.post(st: Stage, pen: Pen, x: Float, i: Int) {
    val u = st.u
    val w = (if (i == 2) 0.07f else 0.1f) * u
    val l = st.x(x) - w / 2f
    val col = when (i) {
        1 -> AtC.beam
        2 -> AtC.brassDark
        else -> AtC.goldDark
    }
    drawRect(Brush.horizontalGradient(0f to col.lighten(0.18f), 1f to col.darken(0.2f), startX = l, endX = l + w), Offset(l, SKY_TOP * u), Size(w, (BACK - SKY_TOP) * u + 2f))
    drawLine(Ink.line, Offset(l, SKY_TOP * u), Offset(l, BACK * u), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(l + w, SKY_TOP * u), Offset(l + w, BACK * u), strokeWidth = pen.lw)
    // Nail heads (or brass studs) up the post.
    var y = 0.05f
    while (y < BACK - 0.03f) {
        drawCircle(if (i == 1) Ink.line.copy(alpha = 0.6f) else AtC.brassLight, pen.lw * 1.2f, Offset(l + w / 2f, y * u))
        y += 0.17f
    }
}

// ------------------------------------------------------------------------------------------------ the tower window

private fun DrawScope.towerWindow(st: Stage, g: AtticStatic, pen: Pen) {
    val u = st.u
    val n = pen.night
    val oc = overcast(pen)
    inScene(st) {
        // The sky seen through the arch.
        clipPath(g.window) {
            val top = lerp(lerp(Color(0xFF6FB8EE), Color(0xFF9CA8BF), oc), Color(0xFF0F0D35), n)
            val low = lerp(lerp(Color(0xFFDDF3FF), Color(0xFFE6EAF2), oc), Color(0xFF4A3B8F), n)
            drawRect(Brush.verticalGradient(listOf(top, low), startY = 0.07f * u, endY = 0.62f * u), Offset((AT_TOWER_X - 0.45f) * u, 0.05f * u), Size(0.9f * u, 0.6f * u))
            translate(st.cam * u, 0f) {
                if (n > 0.2f) drawStars(st, pen, 0.5f, 55)
                if (n > 0.4f) drawAurora(st, pen, 0.8f, 0.04f, 0.36f)
                drawSun(Offset(st.x(AT_TOWER_X + 0.2f), 0.24f * u), 0.034f * u, pen, (1f - n * 1.4f).coerceIn(0f, 1f) * (1f - oc), rays = false)
                drawMoon(Offset(st.x(AT_TOWER_X - 0.2f), 0.23f * u), 0.04f * u, pen, ramp((n - 0.2f) / 0.6f))
                drawClouds(st, pen, 0.12f, 0.36f, 3, 0.045f, 17)
            }
            drawPath(g.hills, lerp(Color(0xFF5E9A6E), Color(0xFF14183A), n))
            drawPath(g.pines, lerp(Color(0xFF2E6B4C), Color(0xFF0B0F2A), n))
            if (n > 0.25f) drawPoints(g.villageLights, PointMode.Points, Color(0xFFFFD66B), strokeWidth = 0.007f * u, cap = StrokeCap.Round, alpha = ramp((n - 0.25f) / 0.5f))
        }
        // Dome ribs, a brass ring at the top, and the window's frame with its cross bars.
        for (rib in listOf(g.ribL, g.ribR)) {
            drawPath(rib, Ink.line, style = Stroke(0.05f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(rib, AtC.brassDark, style = Stroke(0.05f * u, cap = StrokeCap.Round))
            drawPath(rib, AtC.brass, style = Stroke(0.032f * u, cap = StrokeCap.Round))
            drawPath(rib, AtC.brassLight, style = Stroke(0.008f * u, cap = StrokeCap.Round), alpha = 0.6f)
        }
        drawPath(g.ring, Ink.line, style = Stroke(0.035f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(g.ring, AtC.brass, style = Stroke(0.035f * u, cap = StrokeCap.Round))
        drawPath(g.window, Ink.line, style = Stroke(0.05f * u + pen.lw * 2f))
        drawPath(g.window, AtC.brassDark, style = Stroke(0.05f * u))
        drawPath(g.window, AtC.brass, style = Stroke(0.03f * u))
        val x = AT_TOWER_X
        for (barX in listOf(x)) drawLine(AtC.brass, Offset(barX * u, 0.07f * u), Offset(barX * u, 0.62f * u), strokeWidth = 0.014f * u)
        drawLine(AtC.brass, Offset((x - 0.42f) * u, 0.4f * u), Offset((x + 0.42f) * u, 0.4f * u), strokeWidth = 0.014f * u)
        drawLine(Ink.line, Offset(x * u, 0.07f * u), Offset(x * u, 0.62f * u), strokeWidth = pen.lw * 0.6f)
        drawLine(Ink.line, Offset((x - 0.42f) * u, 0.4f * u), Offset((x + 0.42f) * u, 0.4f * u), strokeWidth = pen.lw * 0.6f)
        // The sill: a brass-edged stone shelf.
        drawRect(Color(0xFF55657A), Offset((x - 0.5f) * u, 0.62f * u), Size(1.0f * u, 0.03f * u))
        drawRect(AtC.brass, Offset((x - 0.5f) * u, 0.62f * u), Size(1.0f * u, 0.008f * u))
        drawRect(Ink.line, Offset((x - 0.5f) * u, 0.62f * u), Size(1.0f * u, 0.03f * u), style = pen.thin)
        // Light from the window falls on the floor: warm by day, a cool moonbeam by night.
        drawPath(g.towerBeamDay, Color(0xFFFFE7A8), alpha = 0.1f * (1f - n) * (1f - oc))
        drawPath(g.towerBeamDay, AtC.moon, alpha = 0.12f * n)
    }
}

private fun DrawScope.secretTrim(st: Stage, g: AtticStatic, pen: Pen) {
    val u = st.u
    inScene(st) {
        // A gold crown moulding along the top.
        val l = atWallEdge(3) * u
        val w = (9.6f - atWallEdge(3)) * u
        drawRect(AtC.gold, Offset(l, -0.02f * u), Size(w, 0.04f * u))
        drawRect(AtC.goldLight, Offset(l, -0.02f * u), Size(w, 0.01f * u), alpha = 0.8f)
        drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.25f), Color.Transparent), startY = 0.02f * u, endY = 0.1f * u), Offset(l, 0.02f * u), Size(w, 0.08f * u))
        drawLine(Ink.line, Offset(l, -0.02f * u), Offset(l + w, -0.02f * u), strokeWidth = pen.lw)
        drawLine(Ink.line, Offset(l, 0.02f * u), Offset(l + w, 0.02f * u), strokeWidth = pen.lw)
        var x = atWallEdge(3) + 0.1f
        while (x < 9.5f) {
            drawCircle(AtC.goldDark, 0.008f * u, Offset(x * u, 0.0f))
            x += 0.17f
        }
    }
}

// ------------------------------------------------------------------------------------------------ light and dust

private fun DrawScope.sunbeam(st: Stage, g: AtticStatic, pen: Pen) {
    val n = pen.night
    val oc = overcast(pen)
    inScene(st) {
        if (n < 0.95f && oc < 0.9f) drawPath(g.beamDay, g.beamBrushDay, alpha = (1f - n) * (1f - oc) * (0.85f + 0.15f * sin(pen.t * 0.7f)))
        if (n > 0.05f) drawPath(g.beamDay, g.beamBrushNight, alpha = n)
    }
}

/** Specks of dust turning slowly in the lamplight, most of them in the sunbeam. */
private fun DrawScope.dustMotes(st: Stage, g: AtticStatic, pen: Pen) {
    val u = st.u
    val big = ArrayList<Offset>(14)
    val small = ArrayList<Offset>(20)
    for (i in g.dust.indices) {
        val d = g.dust[i]
        val inBeam = i < 14
        val x: Float
        val y: Float
        if (inBeam) {
            // Inside the beam, rising slowly and swaying.
            val ph = wrap(d.y + pen.t * (0.012f + 0.01f * d.x), 1f)
            val yy = 0.32f + ph * 0.5f
            val xx = 2.15f - (ph) * 0.45f + (d.x - 0.5f) * (0.2f + ph * 0.5f) + sin(pen.t * 0.6f + i) * 0.015f
            x = st.x(xx)
            y = yy * u
        } else {
            x = wrap(d.x * (st.w + 60f) + pen.t * (2f + 3f * d.y) + sin(pen.t * 0.5f + i) * 8f - st.cam * u * 0.2f, st.w + 60f) - 30f
            y = (0.05f + d.y * 0.8f) * u + sin(pen.t * 0.35f + i * 1.9f) * 0.012f * u
        }
        if (i % 2 == 0) big.add(Offset(x, y)) else small.add(Offset(x, y))
    }
    val vis = 1f - overcast(pen) * 0.5f
    drawPoints(big, PointMode.Points, Color(0xFFFFF3D0), strokeWidth = 0.0075f * u, cap = StrokeCap.Round, alpha = 0.55f * vis)
    drawPoints(small, PointMode.Points, Color(0xFFFFF3D0), strokeWidth = 0.0045f * u, cap = StrokeCap.Round, alpha = 0.4f * vis)
}

// ---------------------------------------------------------------------------------------------- fixtures and things

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawAtticFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        FixtureType.STAIRCASE -> atStairs(f, u, pen)
        FixtureType.SECRET_DOOR -> atSecretDoor(f, u, pen)
        FixtureType.AT_SHEETED -> atSheeted(f, u, pen)
        FixtureType.AT_TRUNK -> atTrunk(f, u, pen)
        FixtureType.AT_ROCKING_HORSE -> atRockingHorse(f, u, pen)
        FixtureType.AT_SPIDER -> atSpider(f, u, pen)
        FixtureType.AT_CARTON -> atCarton(f, u, pen)
        FixtureType.AT_LANTERN -> atLantern(f, u, pen)
        FixtureType.AT_ROUND_WINDOW -> atRoundWindow(f, u, pen)
        FixtureType.AT_WING_CHAIR -> atWingChair(f, u, pen)
        FixtureType.AT_GRAMOPHONE -> atGramophone(f, u, pen)
        FixtureType.AT_BOOK_TOWER -> atBookTower(f, u, pen)
        FixtureType.AT_CANDELABRA -> atCandelabra(f, u, pen)
        FixtureType.AT_STRING_LIGHTS -> atStringLights(f, u, pen)
        FixtureType.AT_BLANKET_FORT -> atBlanketFort(f, u, pen)
        FixtureType.AT_SHADOW_THEATRE -> atShadowTheatre(f, u, pen)
        FixtureType.AT_GRANDFATHER -> atGrandfather(f, u, pen)
        FixtureType.AT_STAR_MAP -> atStarMap(f, u, pen)
        FixtureType.AT_WEATHER_VANE -> atWeatherVane(f, u, pen)
        FixtureType.AT_ARMILLARY -> atArmillary(f, u, pen)
        FixtureType.AT_BAROMETER -> atBarometer(f, u, pen)
        FixtureType.AT_OWL_HOLE -> atOwlHole(f, u, pen)
        FixtureType.AT_MAP_TABLE -> atMapTable(f, u, pen)
        FixtureType.AT_TREASURE_CHEST -> atTreasureChest(f, u, pen)
        FixtureType.AT_GLOBE -> atGlobe(f, u, pen)
        FixtureType.AT_FAMILY_TREE -> atFamilyTree(f, u, pen)
        FixtureType.AT_CHANDELIER -> atChandelier(f, u, pen)
        FixtureType.AT_SCONCE -> atSconce(f, u, pen)
        else -> return false
    }
    return true
}

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawAtticFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean {
    return false
}

/** A thing that belongs to this floor; true when drawn. */
internal fun DrawScope.drawAtticThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean =
    drawAtticThingImpl(type, variant, w, h, pen)
