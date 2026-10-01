package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.JobKind
import app.trollfoss.domain.Mine
import app.trollfoss.domain.MineHouse
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomKind
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.MineRooms
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The inside of Mitt hus: five slots a floor, each a cut-away room in oblique 3D. A built room has its own style by
 * kind (walls, floor, window, lamp) until the child picks a wallpaper or a floor; an empty slot is open to the sky
 * with scaffold poles, and in build mode a dashed frame with a plus. Walls between rooms have a door opening.
 */

private const val BACK = 0.80f
private const val SHIFT = Mine.WALL_SHIFT
private const val TH = 0.035f

/** The look of a kind of room: [wall] is the paint, [wall2] its pattern colour, [floor] and [floor2] the floor. */
private class KStyle(val wall: Color, val wall2: Color, val floor: Color, val floor2: Color, val trim: Color, val window: Float, val lamp: Color?, val lampX: Float)

private fun styleOf(kind: RoomKind?, upperFloor: Boolean): KStyle = when (kind) {
    RoomKind.LIVING -> KStyle(Color(0xFFF3E3C6), Color(0xFFC49A62), Color(0xFFD9A873), Color(0xFFC49A62), Color(0xFFFFF8EE), 1.62f, null, 0f)
    RoomKind.KITCHEN -> KStyle(Color(0xFFFBEFB8), Color(0xFFFFFFFF), Color(0xFFF2EBD8), Color(0xFF9FC3A0), Color(0xFFFFFFFF), 1.14f, Color(0xFFF2994A), 0.5f)
    RoomKind.DINING -> KStyle(Color(0xFF4F8F8B), Color(0xFFF3E8D0), Color(0xFFB07848), Color(0xFF9A6638), Color(0xFFF3E8D0), 0.4f, null, 0f)
    RoomKind.BEDROOM -> KStyle(Color(0xFFC9D4F0), Color(0xFFFFE680), Color(0xFFC9D4E8), Color(0xFFB4C2DE), Color(0xFFF7F3EC), 1.35f, Color(0xFFF7B6D0), 1.0f)
    RoomKind.KIDS -> KStyle(Color(0xFFFFE18A), Color(0xFFFFFFFF), Color(0xFF8FD0E8), Color(0xFFFFB3C7), Color(0xFFFFFFFF), 1.7f, Color(0xFFFFC83D), 0.3f)
    RoomKind.BATH -> KStyle(Color(0xFFEAF7F5), Color(0xFFA9D6CF), Color(0xFFD5E6E8), Color(0xFFB4CBD0), Color(0xFF7CC7C0), 0.5f, Color(0xFFFFFFFF), 1.1f)
    RoomKind.LIBRARY -> KStyle(Color(0xFF8C3B4A), Color(0xFFB8606E), Color(0xFF9C6B45), Color(0xFF7E5232), Color(0xFF6B4A3A), 1.3f, Color(0xFF3E8F5A), 0.8f)
    RoomKind.WORKSHOP -> KStyle(Color(0xFFAEB9C6), Color(0xFF7F8A9E), Color(0xFFB4B6BE), Color(0xFF9EA0A8), Color(0xFFE6E2D6), 1.1f, Color(0xFFE6B450), 1.5f)
    RoomKind.MUSIC -> KStyle(Color(0xFF5B4BA0), Color(0xFF473986), Color(0xFF6F5BA8), Color(0xFF5B4BA0), Color(0xFFE9E1F5), 1.55f, null, 0f)
    RoomKind.GREENHOUSE -> KStyle(Color(0xFFBFE9F2), Color(0xFFFFFFFF), Color(0xFFD28C60), Color(0xFFC07850), Color(0xFFF7F3EC), 1f, null, 0f)
    null -> if (upperFloor) KStyle(Color(0xFFF2E4D4), Color(0xFFD9A873), Color(0xFFD9A873), Color(0xFFC49A62), Color(0xFFF7F3EC), 1.0f, Color(0xFFF2C14E), 0.3f)
    else KStyle(Color(0xFFF0DDC2), Color(0xFF86B8E0), Color(0xFFD9D4CA), Color(0xFFC3BDB0), Color(0xFFF7F3EC), 1.0f, Color(0xFFF2C14E), 1.5f)
}

/** The shapes of a room's walls and floor, built once for each scale, in pixels from the left edge of the slot. */
private class RoomGeo(
    val wallLines: List<Offset>, val wallDots: List<Offset>, val dotSize: Float, val panels: Path?,
    val floorQuad: Path, val floorLines: List<Offset>, val floorFill: Path?, val floorFill2: Path?, val wainscot: Float,
)

private fun mixf(a: Float, b: Float, f: Float) = a + (b - a) * f
private fun rec(y: Float) = (FRONT_Y - y) * RECEDE

/** One cell of a floor grid: [x0]..[x1] across at the front edge, depth fractions [f0] (front) to [f1] (back). */
private fun Path.cell(u: Float, x0: Float, x1: Float, f0: Float, f1: Float) {
    val yf = mixf(FRONT_Y, BACK, f0)
    val yb = mixf(FRONT_Y, BACK, f1)
    floorQuad(u, 0f, x0 + rec(yf), x1 + rec(yf), yf, yb)
}

private val roomGeos = HashMap<Int, RoomGeo>()
private var roomGeoU = Float.NaN

private fun geoFor(kind: RoomKind?, upperFloor: Boolean, u: Float): RoomGeo {
    if (roomGeoU != u) {
        roomGeos.clear()
        roomGeoU = u
    }
    val key = (kind?.ordinal ?: 10) + (if (kind == null && upperFloor) 1 else 0)
    return roomGeos.getOrPut(key) { buildGeo(kind, upperFloor, u) }
}

private fun buildGeo(kind: RoomKind?, upperFloor: Boolean, u: Float): RoomGeo {
    val wallLines = ArrayList<Offset>()
    val dots = ArrayList<Offset>()
    var dotSize = 0.01f
    var panels: Path? = null
    val floorLines = ArrayList<Offset>()
    var fill: Path? = null
    var fill2: Path? = null
    var wainscot = 0f
    val quad = Path().apply { floorQuad(u, 0f, 0f, Mine.SLOT_W, FRONT_Y, BACK) }
    val wx0 = SHIFT
    val wx1 = Mine.SLOT_W + SHIFT
    fun wl(x0: Float, y0: Float, x1: Float, y1: Float) { wallLines.add(Offset(x0 * u, y0 * u)); wallLines.add(Offset(x1 * u, y1 * u)) }
    fun boards(color: Int) {
        // Floor boards running back into the picture.
        var x = 0.1f
        while (x < Mine.SLOT_W) {
            floorLines.add(Offset(x * u, FRONT_Y * u)); floorLines.add(Offset((x + SHIFT) * u, BACK * u))
            x += 0.1f
        }
        for (k in 0 until 6) {
            val f = 0.1f + 0.15f * k
            val y = mixf(FRONT_Y, BACK, f)
            var x2 = ((k * 37) % 10) / 10f * 0.1f
            while (x2 < Mine.SLOT_W) {
                floorLines.add(Offset((x2 + rec(y)) * u, y * u)); floorLines.add(Offset((x2 + 0.1f + rec(y)) * u, y * u))
                x2 += 0.2f
            }
        }
    }
    when (kind) {
        RoomKind.LIVING -> {
            wainscot = 0.2f
            boards(0)
            // A picture rail.
            wl(wx0, 0.12f, wx1, 0.12f)
        }
        RoomKind.KITCHEN -> {
            wainscot = 0.28f
            // Subway tiles on the lower wall.
            val top = BACK - wainscot
            var y = top
            while (y < BACK) { wl(wx0, y, wx1, y); y += 0.04f }
            var row = 0
            y = top
            while (y < BACK) {
                var x = wx0 + (if (row % 2 == 0) 0f else 0.05f)
                while (x < wx1) { wl(x, y, x, min(BACK, y + 0.04f)); x += 0.1f }
                y += 0.04f
                row++
            }
            // Chequered floor.
            fill = Path()
            val nx = 10
            val ny = 4
            for (r in 0 until ny) for (c in 0 until nx) if ((r + c) % 2 == 0) fill.cell(u, c * 0.2f, (c + 1) * 0.2f, r / ny.toFloat(), (r + 1) / ny.toFloat())
        }
        RoomKind.DINING -> {
            wainscot = 0.22f
            var x = wx0 + 0.1f
            while (x < wx1) { wl(x, SKY_TOP, x, BACK - wainscot); x += 0.2f }
            // Parquet: boards across.
            for (r in 1 until 7) {
                val y = mixf(FRONT_Y, BACK, r / 7f)
                floorLines.add(Offset(rec(y) * u, y * u)); floorLines.add(Offset((Mine.SLOT_W + rec(y)) * u, y * u))
            }
            for (r in 0 until 7) {
                val y0 = mixf(FRONT_Y, BACK, r / 7f)
                val y1 = mixf(FRONT_Y, BACK, (r + 1) / 7f)
                var x = (if (r % 2 == 0) 0.15f else 0f)
                while (x < Mine.SLOT_W) { floorLines.add(Offset((x + rec(y0)) * u, y0 * u)); floorLines.add(Offset((x + rec(y1)) * u, y1 * u)); x += 0.3f }
            }
        }
        RoomKind.BEDROOM -> {
            // Stars on the wall.
            for (i in 0 until 16) dots.add(Offset((wx0 + 0.1f + hash01(i, 401) * (Mine.SLOT_W - 0.2f)) * u, (0.08f + hash01(i, 402) * 0.6f) * u))
            dotSize = 0.014f
            for (i in 0 until 26) {
                val f = hash01(i, 403)
                val y = mixf(FRONT_Y, BACK, f)
                val x = 0.1f + hash01(i, 404) * 1.8f
                floorLines.add(Offset((x + rec(y)) * u, y * u)); floorLines.add(Offset((x + 0.03f + rec(y)) * u, y * u))
            }
        }
        RoomKind.KIDS -> {
            for (r in 0 until 6) for (c in 0 until 9) dots.add(Offset((wx0 + 0.12f + c * 0.22f + (if (r % 2 == 0) 0f else 0.11f)) * u, (0.04f + r * 0.14f) * u))
            dotSize = 0.018f
            // Soft foam puzzle tiles.
            fill = Path()
            fill2 = Path()
            val nx = 8
            val ny = 3
            for (r in 0 until ny) for (c in 0 until nx) {
                val target = if ((r + c) % 2 == 0) fill else fill2
                target.cell(u, c * 0.25f + 0.008f, (c + 1) * 0.25f - 0.008f, r / ny.toFloat() + 0.02f, (r + 1) / ny.toFloat() - 0.02f)
            }
        }
        RoomKind.BATH -> {
            wainscot = 0.6f
            val top = BACK - wainscot
            var y = top
            while (y <= BACK + 0.001f) { wl(wx0, y, wx1, y); y += 0.06f }
            var x = wx0
            while (x <= wx1 + 0.001f) { wl(x, top, x, BACK); x += 0.1f }
            var gx = 0.2f
            while (gx < Mine.SLOT_W) {
                floorLines.add(Offset(gx * u, FRONT_Y * u)); floorLines.add(Offset((gx + SHIFT) * u, BACK * u))
                gx += 0.2f
            }
            for (r in 1 until 4) {
                val y2 = mixf(FRONT_Y, BACK, r / 4f)
                floorLines.add(Offset(rec(y2) * u, y2 * u)); floorLines.add(Offset((Mine.SLOT_W + rec(y2)) * u, y2 * u))
            }
        }
        RoomKind.LIBRARY -> {
            wainscot = 0.3f
            val p = Path()
            var x = wx0 + 0.08f
            while (x < wx1 - 0.2f) {
                p.addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(x * u, (BACK - wainscot + 0.03f) * u, (x + 0.2f) * u, (BACK - 0.03f) * u), CornerRadius(0.01f * u)))
                x += 0.26f
            }
            panels = p
            wl(wx0, 0.1f, wx1, 0.1f)
            boards(0)
        }
        RoomKind.WORKSHOP -> {
            // A pegboard.
            for (r in 0 until 9) for (c in 0 until 18) dots.add(Offset((wx0 + 0.07f + c * 0.108f) * u, (0.06f + r * 0.075f) * u))
            dotSize = 0.006f
            for (i in 0 until 30) {
                val y = mixf(FRONT_Y, BACK, hash01(i, 411))
                floorLines.add(Offset((0.1f + hash01(i, 412) * 1.8f + rec(y)) * u, y * u)); floorLines.add(Offset((0.14f + hash01(i, 412) * 1.8f + rec(y)) * u, (y - 0.002f) * u))
            }
        }
        RoomKind.MUSIC -> {
            val p = Path()
            for (r in 0 until 3) for (c in 0 until 4) {
                val x = wx0 + 0.1f + c * 0.46f
                val y = 0.04f + r * 0.2f
                p.addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(x * u, y * u, (x + 0.4f) * u, (y + 0.16f) * u), CornerRadius(0.02f * u)))
            }
            panels = p
            boards(0)
        }
        RoomKind.GREENHOUSE -> {
            // Glass panes and terracotta tiles.
            var x = wx0
            while (x <= wx1 + 0.001f) { wl(x, SKY_TOP, x, BACK); x += 0.4f }
            var y = 0.0f
            while (y < BACK) { wl(wx0, y, wx1, y); y += 0.32f }
            fill = Path()
            val nx = 8
            val ny = 3
            for (r in 0 until ny) for (c in 0 until nx) if ((r + c) % 2 == 0) fill.cell(u, c * 0.25f, (c + 1) * 0.25f, r / ny.toFloat(), (r + 1) / ny.toFloat())
        }
        null -> {
            wainscot = 0.2f
            if (upperFloor) boards(0) else {
                // Stone tiles in the hall.
                fill = Path()
                val nx = 5
                val ny = 3
                for (r in 0 until ny) for (c in 0 until nx) if ((r + c) % 2 == 0) fill.cell(u, c * 0.4f, (c + 1) * 0.4f, r / ny.toFloat(), (r + 1) / ny.toFloat())
            }
        }
    }
    return RoomGeo(wallLines, dots, dotSize, panels, quad, floorLines, fill, fill2, wainscot)
}

// ---------------------------------------------------------------------------------------------- composition

internal fun DrawScope.mineInteriorBack(place: PlaceId, st: Stage, pen: Pen, styles: List<RoomStyle>) {
    val u = st.u
    val h = MineView.house ?: MineHouse()
    val upper = place == PlaceId.MINE_UPPER
    val n = pen.night
    val job = h.job
    // What stands: the hall shows once the foundation or the second floor is there.
    val standing = BooleanArray(Mine.SLOTS) { h.standing(place, it) }
    var anyEmpty = false
    for (i in 0 until Mine.SLOTS) if (!standing[i] && st.sees(i * 2f, i * 2f + 2f + SHIFT)) anyEmpty = true
    if (anyEmpty) outdoorPanorama(st, pen, upper)

    // Rooms, left to right: each draws its back wall, floor and left wall.
    for (i in 0 until Mine.SLOTS) {
        if (!st.sees(i * 2f, i * 2f + 2f + SHIFT + 0.1f)) continue
        val kind = if (i == 0) null else h.kind(place, i)
        val reveal = if (job != null && job.kind == JobKind.ROOM && job.place == place && job.slot == i && job.committed) ((job.t - job.commitAt) / 0.7f).coerceIn(0f, 1f) else 1f
        if (standing[i]) {
            if (reveal < 1f) {
                val y = mixf(BACK, SKY_TOP, ramp(reveal)) * u
                clipRect(left = st.x(i * 2f - 0.01f), top = y - 2f, right = st.x(i * 2f + 2f + SHIFT + 0.05f), bottom = st.h) {
                    drawRoom(h, place, i, kind, st, pen, styles, upper)
                }
            } else {
                drawRoom(h, place, i, kind, st, pen, styles, upper)
            }
        }
    }
    // The faces of the walls between the slots: the left wall of a room, or the outside of a house wall.
    for (b in 0..Mine.SLOTS) {
        if (!st.sees(b * 2f - 0.05f, b * 2f + SHIFT + 0.1f)) continue
        val right = standing.getOrNull(b) == true
        val left = standing.getOrNull(b - 1) == true
        if (right) {
            val kind = if (b == 0) null else h.kind(place, b)
            leftWall(h, st, pen, b, kind, doorOpening = left, styles.wallOf(b), upper)
        } else if (left) {
            outsideWall(h, st, pen, b)
        }
    }
    // Per slot: the build frame for empty slots, scaffold and tools for the job at work.
    for (i in 0 until Mine.SLOTS) {
        if (!st.sees(i * 2f, i * 2f + 2f + SHIFT)) continue
        val working = job != null && job.kind == JobKind.ROOM && job.place == place && job.slot == i
        val empty = !standing[i]
        if (empty && working) {
            val p = (job!!.t / job.commitAt).coerceIn(0f, 1f)
            workSite(st, pen, i, p, job.t, upper)
        } else if (empty && h.buildMode && Mine.canBuild(h, place, i) && job == null) {
            val picked = h.selected == i && h.selectedPlace == place
            val l = st.x(i * 2f + 0.55f)
            val r = st.x(i * 2f + 1.75f)
            drawBuildFrame(l, 0.1f * u, r, 0.72f * u, pen, pen.t, picked)
        } else if (empty && !working) {
            stillSite(st, pen, i, upper, Mine.canBuild(h, place, i))
        } else if (!empty && working) {
            val after = job!!.t - job.commitAt
            if (after < 1.2f) drawBuildTools(st.x(i * 2f + 1.1f), 0.5f * u, 0.5f * u, pen.t, pen, 1f - after / 1.2f)
        }
    }
    drawBase(st, pen, Color(0xFFE7C497), Color(0xFF9C6B45))
}

/** Sky, hills and a meadow behind the empty slots. */
private fun DrawScope.outdoorPanorama(st: Stage, pen: Pen, upper: Boolean) {
    val u = st.u
    val n = pen.night
    drawSky(st, pen, Mood.SUMMER, BACK)
    drawStars(st, pen, 0.45f)
    drawMoon(Offset(st.fx(0.7f, 0.03f), 0.1f * u), 0.035f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawSun(Offset(st.fx(0.25f, 0.03f), 0.12f * u), 0.045f * u, pen, (1f - n) * (1f - overcast(pen)))
    drawClouds(st, pen, 0.0f, 0.3f, 3, 0.06f, salt = 4)
    drawPeaks(
        st, 0.08f, 0.62f, 0.7f, 0.1f, 0.2f,
        Color(0xFFA9C2E2).atNight(n, 0.75f), Color(0xFF8FACD3).atNight(n, 0.75f),
        Color(0xFFEEF3FC).atNight(n, 0.7f), Color(0xFFD3DFF3).atNight(n, 0.7f), 0.3f, 23,
    )
    drawForestRow(st, 0.25f, 0.7f, 0.03f, 11, 0.08f, 0.08f, 0.16f, Color(0xFF4B8C6C).atNight(n, 0.65f), Color(0xFF3E7B5C).atNight(n, 0.65f))
    drawPath(ridgePath(st, 0.55f, 0.79f, 0.008f, 19), Color(0xFF82B862).atNight(n, 0.55f))
    val grass = if (upper) Color(0xFFD9B27A) else Color(0xFF6FAE5A)
    val g = grass.atNight(n, 0.4f)
    drawRect(Brush.verticalGradient(0f to g.lighten(0.05f), 1f to g.darken(0.15f), startY = BACK * u, endY = FRONT_Y * u), Offset(0f, BACK * u), Size(st.w, (FRONT_Y - BACK) * u))
    if (upper) {
        // A scaffold deck: planks running across.
        val lines = ArrayList<Offset>()
        var y = BACK + 0.03f
        while (y < FRONT_Y) { lines.add(Offset(0f, y * u)); lines.add(Offset(st.w, y * u)); y += 0.035f }
        drawPoints(lines, PointMode.Lines, g.darken(0.3f), strokeWidth = pen.lw * 0.7f)
    }
}

/** The wall at the back, the floor, the skirting, the window and the lamp of one built room. */
private fun DrawScope.drawRoom(h: MineHouse, place: PlaceId, i: Int, kind: RoomKind?, st: Stage, pen: Pen, styles: List<RoomStyle>, upper: Boolean) {
    val u = st.u
    val n = pen.night
    val s = styleOf(kind, upper)
    val geo = geoFor(kind, upper, u)
    val x0 = i * 2f + SHIFT
    val x1 = i * 2f + 2f + SHIFT
    val l = st.x(x0)
    val r = st.x(x1)
    val chosenWall = styles.wallOf(i)
    val chosenFloor = styles.floorOf(i)
    val dark = n * 0.0f
    // The wall.
    if (chosenWall > 0) {
        drawWallpaper(chosenWall, l, SKY_TOP * u - 2f, r, BACK * u, -st.cam * u, 0f, u)
    } else if (kind == RoomKind.GREENHOUSE) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFBFE9F2).atNight(n, 0.5f), Color(0xFFE9F8F0).atNight(n, 0.4f)), startY = SKY_TOP * u, endY = BACK * u), Offset(l, SKY_TOP * u), Size(r - l, (BACK - SKY_TOP) * u))
    } else {
        drawRect(s.wall.atNight(n, 0.15f), Offset(l, SKY_TOP * u), Size(r - l, (BACK - SKY_TOP) * u))
        translate(st.x(i * 2f)) {
            val px = geo.wallLines
            if (px.isNotEmpty()) drawPoints(px, PointMode.Lines, s.wall2.copy(alpha = if (kind == RoomKind.BEDROOM) 0f else 0.55f), strokeWidth = pen.lw * 0.7f)
            if (geo.wallDots.isNotEmpty()) {
                val col = when (kind) { RoomKind.BEDROOM -> Color(0xFFFFE680); RoomKind.KIDS -> Color.White.copy(alpha = 0.7f); else -> s.wall2.copy(alpha = 0.6f) }
                drawPoints(geo.wallDots, PointMode.Points, col, strokeWidth = geo.dotSize * u, cap = StrokeCap.Round)
            }
            geo.panels?.let { drawPath(it, s.wall2.copy(alpha = 0.7f)); drawPath(it, Ink.line.copy(alpha = 0.25f), style = pen.thin) }
        }
        // The lower wall: wooden panelling, tiles or a band, by kind.
        if (geo.wainscot > 0f && kind != RoomKind.KITCHEN && kind != RoomKind.BATH) {
            val top = (BACK - geo.wainscot) * u
            drawRect(s.wall2.lighten(if (kind == RoomKind.LIBRARY) 0f else 0.1f).atNight(n, 0.15f), Offset(l, top), Size(r - l, BACK * u - top))
            drawLine(s.trim.atNight(n, 0.15f), Offset(l, top), Offset(r, top), strokeWidth = pen.lw * 2.2f)
            drawLine(Ink.line, Offset(l, top), Offset(r, top), strokeWidth = pen.lw * 0.7f)
        } else if (kind == RoomKind.KITCHEN || kind == RoomKind.BATH) {
            val top = (BACK - geo.wainscot) * u
            drawLine(s.trim, Offset(l, top), Offset(r, top), strokeWidth = pen.lw * 2.4f)
            drawLine(Ink.line, Offset(l, top), Offset(r, top), strokeWidth = pen.lw * 0.7f)
        }
        // Windows and a lamp, by kind.
        if (kind != RoomKind.GREENHOUSE) roomWindow(h, st, pen, i, kind, s, n)
    }
    if (kind == RoomKind.GREENHOUSE && chosenWall == 0) {
        translate(st.x(i * 2f)) {
            drawPoints(geo.wallLines, PointMode.Lines, Color.White, strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
        }
        // Plants behind the glass.
        for (k in 0 until 4) {
            val cx = st.x(i * 2f + SHIFT + 0.25f + k * 0.5f)
            drawCircle(Color(0xFF3BC46B).atNight(n, 0.4f), 0.1f * u, Offset(cx, (BACK - 0.06f) * u))
            drawCircle(Color(0xFF2E8B57).atNight(n, 0.4f), 0.07f * u, Offset(cx + 0.08f * u, (BACK - 0.11f) * u))
        }
    }
    // Skirting and crown.
    val skirt = if (chosenWall > 0) Color(0xFFF7F3EC) else s.trim
    drawRect(skirt, Offset(l, (BACK - 0.024f) * u), Size(r - l, 0.024f * u))
    drawLine(Ink.line, Offset(l, (BACK - 0.024f) * u), Offset(r, (BACK - 0.024f) * u), strokeWidth = pen.lw * 0.8f)
    drawRect(skirt, Offset(l, 0f), Size(r - l, 0.02f * u))
    drawLine(Ink.line, Offset(l, 0.02f * u), Offset(r, 0.02f * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(l, 0f), Offset(r, 0f), strokeWidth = pen.lw * 0.7f)
    drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.16f), Ink.line.copy(alpha = 0f)), startY = SKY_TOP * u, endY = 0f), Offset(l, SKY_TOP * u), Size(r - l, -SKY_TOP * u))
    // The floor.
    if (chosenFloor > 0) {
        layFloor(st, chosenFloor, BACK, i * 2f, i * 2f + 2f)
    } else {
        translate(st.x(i * 2f)) {
            drawPath(geo.floorQuad, Brush.verticalGradient(0f to s.floor.darken(0.18f).atNight(n, 0.2f), 0.35f to s.floor.atNight(n, 0.2f), 1f to s.floor.lighten(0.08f).atNight(n, 0.2f), startY = BACK * u, endY = FRONT_Y * u))
            geo.floorFill?.let { drawPath(it, (if (kind == RoomKind.KIDS) Color(0xFFFFB3C7) else s.floor2).atNight(n, 0.2f)) }
            geo.floorFill2?.let { drawPath(it, Color(0xFFFFE680).atNight(n, 0.2f)) }
            if (geo.floorLines.isNotEmpty()) drawPoints(geo.floorLines, PointMode.Lines, s.floor.darken(0.3f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
        }
    }
    // The shadow where wall and floor meet, and a patch of sun from the window.
    drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.28f), Ink.line.copy(alpha = 0f)), startY = BACK * u, endY = (BACK + 0.045f) * u), Offset(l, BACK * u), Size(r - l, 0.045f * u))
    if (kind != null && kind != RoomKind.GREENHOUSE && kind != RoomKind.MUSIC) lightPatch(st, i * 2f + s.window, 0.2f, BACK, n)
    // A lamp hanging from the ceiling.
    s.lamp?.let { if (kind != RoomKind.BATH || true) pendant(st, pen, i * 2f + s.lampX + SHIFT * 0.5f, 0.1f, it) }
    // The floor edge towards the viewer.
    drawLine(Ink.line, Offset(st.x(i * 2f), FRONT_Y * u), Offset(st.x(i * 2f + 2f), FRONT_Y * u), strokeWidth = pen.lw)
}

private fun DrawScope.roomWindow(h: MineHouse, st: Stage, pen: Pen, i: Int, kind: RoomKind?, s: KStyle, n: Float) {
    val u = st.u
    val cx = i * 2f + s.window + SHIFT * 0.8f
    if (!st.sees(cx - 0.4f, cx + 0.4f)) return
    val w = when (kind) { RoomKind.KITCHEN -> 0.3f; RoomKind.KIDS, RoomKind.MUSIC -> 0.3f; RoomKind.BATH -> 0.26f; RoomKind.LIBRARY -> 0.26f; else -> 0.32f }
    val top: Float
    val bottom: Float
    when (kind) {
        RoomKind.KITCHEN -> { top = 0.16f; bottom = 0.5f }
        RoomKind.KIDS -> { top = 0.16f; bottom = 0.16f + w }
        RoomKind.MUSIC -> { top = 0.12f; bottom = 0.12f + w }
        RoomKind.LIBRARY -> { top = 0.08f; bottom = 0.5f }
        RoomKind.BATH -> { top = 0.2f; bottom = 0.5f }
        null -> { top = 0.12f; bottom = 0.36f }
        else -> { top = 0.1f; bottom = 0.34f }
    }
    val wall = MineC.wall(h.wall)
    drawMineWindow(h.windows, kind, st.x(cx - w / 2f), top * u, st.x(cx + w / 2f), bottom * u, pen, 0f, s.trim, MineC.roof(h.roofColor), pen.t, 2, dark = n)
}

/** The left wall of a room: the next room's face, with a door opening when there is a room on the other side. */
private fun DrawScope.leftWall(h: MineHouse, st: Stage, pen: Pen, b: Int, kind: RoomKind?, doorOpening: Boolean, paper: Int, upper: Boolean) {
    val u = st.u
    val s = styleOf(kind, upper)
    val xf = b * 2f
    val th = TH
    val hh = BACK - SKY_TOP
    fun px(f: Float, dx: Float = 0f) = st.x(xf + SHIFT * f + dx)
    fun py(f: Float, up: Float = 0f) = (mixf(FRONT_Y, BACK, f) - up) * u
    val d0 = 0.2f
    val d1 = 0.72f
    val dh = 0.46f
    val face = Path().apply {
        fillType = PathFillType.EvenOdd
        moveTo(px(0f, th), py(0f, hh)); lineTo(px(1f, th), py(1f, hh)); lineTo(px(1f, th), py(1f)); lineTo(px(0f, th), py(0f)); close()
        if (doorOpening) {
            moveTo(px(d0, th), py(d0, dh)); lineTo(px(d1, th), py(d1, dh)); lineTo(px(d1, th), py(d1)); lineTo(px(d0, th), py(d0)); close()
        }
    }
    if (paper > 0) {
        drawWallpaperSlanted(paper, face, px(0f, th), u)
        drawPath(face, Brush.horizontalGradient(listOf(Ink.line.copy(alpha = 0.26f), Ink.line.copy(alpha = 0.1f)), startX = px(0f), endX = px(1f)))
    } else {
        drawPath(face, Brush.horizontalGradient(listOf(s.wall.darken(0.2f), s.wall.darken(0.08f)), startX = px(0f), endX = px(1f)))
        // The lower panelling continues along the side.
        val geo = geoFor(kind, upper, u)
        if (geo.wainscot > 0f && kind != RoomKind.KITCHEN && kind != RoomKind.BATH) {
            val w = geo.wainscot
            val band = Path().apply { poly(px(0f, th), py(0f, w), px(1f, th), py(1f, w), px(1f, th), py(1f), px(0f, th), py(0f)) }
            drawPath(band, s.wall2.darken(0.14f))
        }
    }
    val edge = if (paper > 0) Color(0xFFF7F3EC) else s.trim
    val skirt = Path().apply {
        if (doorOpening) {
            poly(px(0f, th), py(0f, 0.024f), px(d0, th), py(d0, 0.024f), px(d0, th), py(d0), px(0f, th), py(0f))
            poly(px(d1, th), py(d1, 0.024f), px(1f, th), py(1f, 0.024f), px(1f, th), py(1f), px(d1, th), py(d1))
        } else poly(px(0f, th), py(0f, 0.024f), px(1f, th), py(1f, 0.024f), px(1f, th), py(1f), px(0f, th), py(0f))
    }
    drawPath(skirt, edge.darken(0.08f))
    drawPath(face, Ink.line, style = pen.stroke)
    if (doorOpening) {
        val jamb = Path().apply { poly(px(d1), py(d1, dh), px(d1, th), py(d1, dh), px(d1, th), py(d1), px(d1), py(d1)) }
        drawPath(jamb, edge.darken(0.12f))
        drawPath(jamb, Ink.line, style = pen.thin)
        val casing = Path().apply { moveTo(px(d0, th), py(d0)); lineTo(px(d0, th), py(d0, dh)); lineTo(px(d1, th), py(d1, dh)); lineTo(px(d1, th), py(d1)) }
        drawPath(casing, edge, style = androidx.compose.ui.graphics.drawscope.Stroke(0.012f * u))
        drawPath(casing, Ink.line, style = pen.thin)
    }
    // The cut front edge and the top.
    val front = Path().apply { poly(px(0f), py(0f, hh), px(0f, th), py(0f, hh), px(0f, th), py(0f), px(0f), py(0f)) }
    drawPath(front, edge)
    drawPath(front, Ink.line, style = pen.stroke)
    val top = Path().apply { poly(px(0f), py(0f, hh), px(1f), py(1f, hh), px(1f, th), py(1f, hh), px(0f, th), py(0f, hh)) }
    drawPath(top, edge.lighten(0.3f))
    drawPath(top, Ink.line, style = pen.stroke)
}

/** The outside of a room's right wall, where the slot next to it is still empty: the house's own wall colour with its cut edge. */
private fun DrawScope.outsideWall(h: MineHouse, st: Stage, pen: Pen, b: Int) {
    val u = st.u
    val wall = MineC.wall(h.wall).atNight(pen.night, 0.4f)
    val xf = b * 2f
    val hh = BACK - SKY_TOP
    fun px(f: Float, dx: Float = 0f) = st.x(xf + SHIFT * f + dx)
    fun py(f: Float, up: Float = 0f) = (mixf(FRONT_Y, BACK, f) - up) * u
    val face = Path().apply { poly(px(0f), py(0f, hh), px(1f), py(1f, hh), px(1f), py(1f), px(0f), py(0f)) }
    drawPath(face, Brush.horizontalGradient(listOf(wall.darken(0.2f), wall.darken(0.3f)), startX = px(0f), endX = px(1f)))
    val lines = ArrayList<Offset>()
    var f = 0.1f
    while (f < 1f) { lines.add(Offset(px(f), py(f, hh))); lines.add(Offset(px(f), py(f))); f += 0.1f }
    drawPoints(lines, PointMode.Lines, wall.darken(0.4f), strokeWidth = pen.lw * 0.6f)
    drawPath(face, Ink.line, style = pen.stroke)
    val edge = MineC.trim(MineC.wall(h.wall))
    val front = Path().apply { poly(px(0f), py(0f, hh), px(0f, -TH), py(0f, hh), px(0f, -TH), py(0f), px(0f), py(0f)) }
    drawPath(front, edge)
    drawPath(front, Ink.line, style = pen.stroke)
}

/** An empty slot at rest: two scaffold poles with a plank, and a crate. */
private fun DrawScope.stillSite(st: Stage, pen: Pen, i: Int, upper: Boolean, buildable: Boolean) {
    val u = st.u
    val x0 = st.x(i * 2f + 0.35f)
    val x1 = st.x(i * 2f + 1.7f)
    if (x1 < -10f || x0 > st.w + 10f) return
    drawScaffold(x0, x1, (BACK + 0.09f) * u, 0.45f * u, pen)
    // A saw horse and planks on the floor.
    drawPlankPile(st.x(i * 2f + 1.2f), 0.93f * u, u, pen, 3)
    if (upper) {
        // A railing along the front of the deck.
        val y = 0.96f * u
        drawLine(Ink.line, Offset(st.x(i * 2f + 0.1f), y), Offset(st.x(i * 2f + 1.95f), y), strokeWidth = pen.lw * 2.6f, cap = StrokeCap.Round)
    }
}

/** The work going on in an empty slot: poles, a floor of planks, and the tools flying about. */
private fun DrawScope.workSite(st: Stage, pen: Pen, i: Int, p: Float, t: Float, upper: Boolean) {
    val u = st.u
    val x0 = st.x(i * 2f + 0.3f)
    val x1 = st.x(i * 2f + 1.75f)
    drawScaffold(x0, x1, (BACK + 0.09f) * u, 0.55f * u, pen, rise = ramp(p * 1.5f))
    // Boards appear on the floor one after another.
    val boards = (p * 8f).toInt()
    for (k in 0 until boards) {
        val f = 0.08f + k * 0.11f
        val y = mixf(FRONT_Y, BACK, f)
        val q = Path().apply { floorQuad(u, st.cam, i * 2f + 0.1f + rec(y), i * 2f + 1.9f + rec(y), y + 0.012f, y - 0.012f) }
        drawPath(q, MineC.plank)
        drawPath(q, Ink.line, style = pen.thin)
    }
    drawBuildTools(st.x(i * 2f + 1.1f), 0.45f * u, 0.55f * u, t, pen, 1f)
}



/**
 * A small picture of a room of [kind]: its wall, floor and window with its furniture, for the builder's cards
 * (drawn once into a bitmap, see `CachedThumb`).
 */
internal fun DrawScope.drawRoomPreview(kind: RoomKind, w: Float, h: Float) {
    val s = styleOf(kind, false)
    val u = min(w / 2.3f, h / 0.98f)
    val pen = Pen(max(1.1f, u * 0.006f))
    val ox = (w - 2.236f * u) / 2f
    val oy = (h - 0.95f * u) / 2f - 0.04f * u
    val geo = buildGeo(kind, false, u)
    translate(ox, oy) {
        drawRect(s.wall, Offset(0f, 0.04f * u), Size(2.236f * u, (BACK - 0.04f) * u))
        if (kind == RoomKind.GREENHOUSE) drawRect(Color(0xFFE9F8F0), Offset(0f, 0.04f * u), Size(2.236f * u, (BACK - 0.04f) * u))
        if (geo.wallLines.isNotEmpty()) drawPoints(geo.wallLines, PointMode.Lines, s.wall2.copy(alpha = if (kind == RoomKind.BEDROOM) 0f else 0.55f), strokeWidth = pen.lw * 0.7f)
        if (geo.wallDots.isNotEmpty()) {
            val col = when (kind) { RoomKind.BEDROOM -> Color(0xFFFFE680); RoomKind.KIDS -> Color.White.copy(alpha = 0.7f); else -> s.wall2.copy(alpha = 0.6f) }
            drawPoints(geo.wallDots, PointMode.Points, col, strokeWidth = geo.dotSize * u, cap = StrokeCap.Round)
        }
        geo.panels?.let { drawPath(it, s.wall2.copy(alpha = 0.7f)) }
        if (geo.wainscot > 0f && kind != RoomKind.KITCHEN && kind != RoomKind.BATH) {
            drawRect(s.wall2.lighten(0.1f), Offset(0f, (BACK - geo.wainscot) * u), Size(2.236f * u, geo.wainscot * u))
            drawLine(Ink.line, Offset(0f, (BACK - geo.wainscot) * u), Offset(2.236f * u, (BACK - geo.wainscot) * u), strokeWidth = pen.lw * 0.7f)
        }
        // The window, as in the rooms.
        if (kind != RoomKind.GREENHOUSE) {
            val cx = s.window + SHIFT * 0.8f
            val ww = 0.32f
            val top: Float
            val bottom: Float
            when (kind) {
                RoomKind.KITCHEN -> { top = 0.16f; bottom = 0.5f }
                RoomKind.KIDS -> { top = 0.16f; bottom = 0.48f }
                RoomKind.MUSIC -> { top = 0.12f; bottom = 0.44f }
                RoomKind.LIBRARY -> { top = 0.08f; bottom = 0.5f }
                RoomKind.BATH -> { top = 0.2f; bottom = 0.5f }
                else -> { top = 0.1f; bottom = 0.34f }
            }
            drawMineWindow(0, kind, (cx - ww / 2f) * u, top * u, (cx + ww / 2f) * u, bottom * u, pen, 0f, s.trim, Color(0xFF3F6FB5), 0f, 1)
        } else {
            drawPoints(geo.wallLines, PointMode.Lines, Color.White, strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
            for (k in 0 until 4) drawCircle(Color(0xFF3BC46B), 0.1f * u, Offset((SHIFT + 0.25f + k * 0.5f) * u, (BACK - 0.06f) * u))
        }
        drawRect(s.trim, Offset(0f, (BACK - 0.024f) * u), Size(2.236f * u, 0.024f * u))
        drawLine(Ink.line, Offset(0f, BACK * u), Offset(2.236f * u, BACK * u), strokeWidth = pen.lw)
        // The floor.
        drawPath(geo.floorQuad, Brush.verticalGradient(0f to s.floor.darken(0.18f), 0.35f to s.floor, 1f to s.floor.lighten(0.08f), startY = BACK * u, endY = FRONT_Y * u))
        geo.floorFill?.let { drawPath(it, if (kind == RoomKind.KIDS) Color(0xFFFFB3C7) else s.floor2) }
        geo.floorFill2?.let { drawPath(it, Color(0xFFFFE680)) }
        if (geo.floorLines.isNotEmpty()) drawPoints(geo.floorLines, PointMode.Lines, s.floor.darken(0.3f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset(0f, FRONT_Y * u), Offset(2f * u, FRONT_Y * u), strokeWidth = pen.lw)
        // The furniture, back to front.
        val pieces = MineRooms.preset(kind)
        val placed = pieces.map { pc ->
            val y = if (pc.wallY.isNaN()) PlaceId.MINE_GROUND.floor + pc.shift else pc.wallY
            Triple(pc, y, app.trollfoss.domain.Fixture(-1, PlaceId.MINE_GROUND, pc.type, pc.dx, y, pc.variant, y).also { it.on = pc.on })
        }.sortedBy { (pc, y, _) -> if (!pc.wallY.isNaN()) -10f else y }
        for ((pc, y, f) in placed) {
            translate(pc.dx * u, y * u) {
                drawFixtureBack(f, u, pen)
                if (f.spec.front || f.spec.glass) drawFixtureFront(f, u, pen)
            }
        }
    }
}
