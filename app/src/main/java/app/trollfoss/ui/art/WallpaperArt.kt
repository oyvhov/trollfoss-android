package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/*
 * Wallpapers and floors for the home designer. Every pattern is one tile, 1 unit wide, built once in
 * scene units and drawn scaled and repeated: the same geometry serves the rooms at any size, the
 * slanted faces of the room dividers and the swatches in the designer panel, so they always match.
 *
 * Walls: 1 pink flowers, 2 sky blue clouds, 3 mint leaves, 4 butter stars, 5 lilac dots, 6 peach
 * stripes, 7 night sky, 8 white with hearts, 9 sea-green waves, 10 candy stripes, 11 wood panels.
 * Floors: 1 dark wood, 2 white-washed wood, 3 black-and-white chequers, 4 blue tiles, 5 pink carpet,
 * 6 green carpet, 7 terrazzo, 8 grey stone.
 */

internal const val WALLPAPERS = 11
internal const val FLOORINGS = 8

/** Wall tiles span this height (the tablet top fill down to the floor). */
private const val WALL_TOP = -0.42f
private const val WALL_BOTTOM = 1.0f

/** The floor band of indoor places. */
private const val FLOOR_BACK = 0.8f

private class Layer(
    val path: Path? = null,
    val points: List<Offset>? = null,
    val color: Color,
    /** Stroke width in units; 0 fills the path. For points: the dot size. */
    val width: Float = 0f,
    val mode: PointMode = PointMode.Points,
    val alpha: Float = 1f,
)

private class Pattern(val top: Color, val bottom: Color, val layers: List<Layer>)

private val wallCache = arrayOfNulls<Pattern>(WALLPAPERS + 1)
private val floorCache = arrayOfNulls<Pattern>(FLOORINGS + 1)

private fun wallPattern(i: Int): Pattern = wallCache[i] ?: buildWall(i).also { wallCache[i] = it }
private fun floorPattern(i: Int): Pattern = floorCache[i] ?: buildFloor(i).also { floorCache[i] = it }

/** A grid of spots for one tile: [cols] across, rows every [dy], odd rows shifted half a step. */
private fun grid(cols: Int, dy: Float, top: Float = WALL_TOP, bottom: Float = WALL_BOTTOM, block: (x: Float, y: Float, row: Int, col: Int) -> Unit) {
    val dx = 1f / cols
    var row = 0
    var y = top + dy * 0.5f
    while (y < bottom + dy) {
        for (c in 0 until cols) block(dx * (c + 0.5f + if (row % 2 == 1) 0.5f else 0f) - (if (row % 2 == 1 && c == cols - 1) 1f else 0f), y, row, c)
        y += dy
        row++
    }
}

private fun heartPath(p: Path, cx: Float, cy: Float, s: Float) {
    p.moveTo(cx, cy + s * 0.9f)
    p.cubicTo(cx - s * 1.6f, cy - s * 0.1f, cx - s * 0.6f, cy - s * 1.2f, cx, cy - s * 0.35f)
    p.cubicTo(cx + s * 0.6f, cy - s * 1.2f, cx + s * 1.6f, cy - s * 0.1f, cx, cy + s * 0.9f)
    p.close()
}

private fun buildWall(i: Int): Pattern {
    val (base, motif) = DecorPalette.walls[i]
    val layers = ArrayList<Layer>()
    var top = base.lighten(0.08f)
    var bottom = base.darken(0.04f)
    when (i) {
        1 -> {
            val petals = ArrayList<Offset>()
            val centres = ArrayList<Offset>()
            val leaves = Path()
            grid(8, 0.13f) { x, y, _, _ ->
                for (k in 0 until 5) {
                    val a = k * 1.2566f
                    petals.add(Offset(x + cos(a) * 0.011f, y + sin(a) * 0.011f))
                }
                centres.add(Offset(x, y))
                leaves.moveTo(x + 0.012f, y + 0.012f)
                leaves.quadraticTo(x + 0.03f, y + 0.012f, x + 0.034f, y + 0.03f)
                leaves.quadraticTo(x + 0.016f, y + 0.03f, x + 0.012f, y + 0.012f)
                leaves.close()
            }
            layers += Layer(path = leaves, color = Color(0xFF8BCB8E))
            layers += Layer(points = petals, color = motif, width = 0.014f)
            layers += Layer(points = centres, color = Color(0xFFFFD24A), width = 0.009f)
        }
        2 -> {
            val clouds = Path()
            grid(4, 0.2f) { x, y, _, _ -> clouds.addPath(cloudPath(x, y, 0.026f)) }
            layers += Layer(path = clouds, color = Color(0xFFD9EEFB))
            val front = Path().apply { addPath(clouds, Offset(-0.003f, -0.004f)) }
            layers += Layer(path = front, color = motif)
        }
        3 -> {
            val leaves = Path()
            val veins = Path()
            grid(8, 0.12f) { x, y, row, col ->
                val a = if ((row + col) % 2 == 0) -0.6f else 0.6f
                val l = 0.024f
                val tx = x + sin(a) * l
                val ty = y - cos(a) * l
                val bx = x - sin(a) * l
                val by = y + cos(a) * l
                val nx = cos(a) * 0.012f
                val ny = sin(a) * 0.012f
                leaves.moveTo(bx, by)
                leaves.quadraticTo(x + nx, y + ny, tx, ty)
                leaves.quadraticTo(x - nx, y - ny, bx, by)
                leaves.close()
                veins.moveTo(bx, by)
                veins.lineTo(mix(bx, tx, 0.8f), mix(by, ty, 0.8f))
            }
            layers += Layer(path = leaves, color = motif)
            layers += Layer(path = veins, color = base.lighten(0.3f), width = 0.0025f)
        }
        4 -> {
            val stars = Path()
            val dots = ArrayList<Offset>()
            grid(8, 0.14f) { x, y, row, _ ->
                stars.addPath(starPath(Offset(x, y), 0.019f, 0.008f, row * 13f))
                dots.add(Offset(x + 0.0625f, y + 0.07f))
            }
            layers += Layer(points = dots, color = motif.lighten(0.35f), width = 0.008f)
            layers += Layer(path = stars, color = motif)
        }
        5 -> {
            val dots = ArrayList<Offset>()
            val small = ArrayList<Offset>()
            grid(10, 0.1f) { x, y, _, _ ->
                dots.add(Offset(x, y))
                small.add(Offset(x + 0.05f, y + 0.05f))
            }
            layers += Layer(points = small, color = motif.lighten(0.4f), width = 0.009f)
            layers += Layer(points = dots, color = motif, width = 0.024f)
        }
        6 -> {
            val wide = Path()
            val thin = Path()
            for (k in 0 until 10) {
                val x = k * 0.1f
                wide.addRect(Rect(x, WALL_TOP, x + 0.045f, WALL_BOTTOM))
                thin.moveTo(x + 0.07f, WALL_TOP)
                thin.lineTo(x + 0.07f, WALL_BOTTOM)
            }
            layers += Layer(path = wide, color = motif)
            layers += Layer(path = thin, color = Color.White, width = 0.004f, alpha = 0.8f)
            top = base
            bottom = base
        }
        7 -> {
            top = Color(0xFF141C47)
            bottom = Color(0xFF2E3F85)
            val tiny = ArrayList<Offset>()
            for (k in 0 until 90) tiny.add(Offset(hash01(k, 1101), mix(WALL_TOP, WALL_BOTTOM, hash01(k, 1102))))
            val stars = Path()
            for (k in 0 until 16) stars.addPath(starPath(Offset(hash01(k, 1103), mix(WALL_TOP, WALL_BOTTOM, hash01(k, 1104))), 0.016f, 0.007f, k * 17f))
            val moons = Path().apply {
                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                for (k in 0 until 2) {
                    val c = Offset(0.25f + k * 0.5f, WALL_TOP + 0.3f + k * 0.62f)
                    addOval(Rect(c, 0.03f))
                    addOval(Rect(Offset(c.x + 0.014f, c.y - 0.008f), 0.026f))
                }
            }
            layers += Layer(points = tiny, color = Color(0xFFFFF7DA), width = 0.006f, alpha = 0.85f)
            layers += Layer(path = stars, color = motif)
            layers += Layer(path = moons, color = motif)
        }
        8 -> {
            val hearts = Path()
            val dots = ArrayList<Offset>()
            grid(8, 0.13f) { x, y, _, _ ->
                heartPath(hearts, x, y, 0.014f)
                dots.add(Offset(x + 0.0625f, y + 0.065f))
            }
            layers += Layer(points = dots, color = Color(0xFFE9DCE4), width = 0.007f)
            layers += Layer(path = hearts, color = motif)
        }
        9 -> {
            val waves = Path()
            var y = WALL_TOP
            var row = 0
            while (y < WALL_BOTTOM + 0.07f) {
                val shift = if (row % 2 == 0) 0f else 0.0625f
                waves.moveTo(-0.0625f + shift, y)
                var x = -0.0625f + shift
                while (x < 1.07f) {
                    waves.quadraticTo(x + 0.03125f, y - 0.018f, x + 0.0625f, y)
                    waves.quadraticTo(x + 0.09375f, y + 0.018f, x + 0.125f, y)
                    x += 0.125f
                }
                y += 0.07f
                row++
            }
            layers += Layer(path = waves, color = motif, width = 0.007f)
        }
        10 -> {
            val stripes = Path()
            val h = WALL_BOTTOM - WALL_TOP
            for (k in 0 until 10) {
                val x = k * 0.1f
                stripes.moveTo(x, WALL_TOP)
                stripes.lineTo(x + 0.04f, WALL_TOP)
                stripes.lineTo(x + 0.04f + h, WALL_BOTTOM)
                stripes.lineTo(x + h, WALL_BOTTOM)
                stripes.close()
            }
            layers += Layer(path = stripes, color = motif)
            top = base
            bottom = base
        }
        else -> {
            val seams = Path()
            val grain = Path()
            for (k in 0 until 10) {
                val x = k * 0.1f
                seams.moveTo(x, WALL_TOP)
                seams.lineTo(x, WALL_BOTTOM)
                for (g in 0 until 2) {
                    val gx = x + 0.03f + g * 0.04f + hash01(k * 2 + g, 1111) * 0.01f
                    var y = WALL_TOP + hash01(k * 2 + g, 1112) * 0.2f
                    while (y < WALL_BOTTOM) {
                        grain.moveTo(gx, y)
                        grain.quadraticTo(gx + 0.006f, y + 0.06f, gx, y + 0.12f)
                        y += 0.2f
                    }
                }
            }
            layers += Layer(path = grain, color = base.lighten(0.18f), width = 0.003f)
            layers += Layer(path = seams, color = motif, width = 0.004f)
        }
    }
    return Pattern(top, bottom, layers)
}

/** A floor quad in unit space: front edge x0..x1 at [yf], receding to [yb]. */
private fun Path.quad(x0: Float, x1: Float, yf: Float, yb: Float) = floorQuad(1f, 0f, x0, x1, yf, yb)

private fun buildFloor(i: Int): Pattern {
    val (base, motif) = DecorPalette.floors[i]
    val back = FLOOR_BACK
    val front = FRONT_Y
    val layers = ArrayList<Layer>()
    val shift = recede(back)
    fun depthLines(step: Float, out: ArrayList<Offset>) {
        var x = 0f
        while (x < 1f - 1e-4f) {
            out.add(Offset(x, front))
            out.add(Offset(x + shift, back))
            x += step
        }
    }
    fun rowLines(rows: Int, out: ArrayList<Offset>) {
        for (j in 1 until rows) {
            val y = mix(front, back, j / rows.toFloat())
            out.add(Offset(recede(y), y))
            out.add(Offset(1f + recede(y), y))
        }
    }
    when (i) {
        1, 2 -> {
            val joints = ArrayList<Offset>()
            val ends = ArrayList<Offset>()
            val grain = ArrayList<Offset>()
            depthLines(0.1f, joints)
            for (lane in 0 until 10) {
                val x = lane * 0.1f
                for (k in 0 until 2) {
                    val f = 0.1f + 0.4f * k + 0.3f * hash01(lane * 2 + k, 1121 + i)
                    val y = mix(front, back, f)
                    ends.add(Offset(x + recede(y), y))
                    ends.add(Offset(x + 0.1f + recede(y), y))
                }
                for (k in 0 until 2) {
                    val gx = x + 0.1f * (0.25f + 0.5f * hash01(lane * 3 + k, 1131 + i))
                    val f0 = 0.05f + hash01(lane * 5 + k, 1141 + i) * 0.7f
                    val f1 = (f0 + 0.15f).coerceAtMost(0.98f)
                    val y0 = mix(front, back, f0)
                    val y1 = mix(front, back, f1)
                    grain.add(Offset(gx + recede(y0), y0))
                    grain.add(Offset(gx + recede(y1), y1))
                }
            }
            layers += Layer(points = grain, color = base.darken(0.12f), width = 0.0022f, mode = PointMode.Lines)
            layers += Layer(points = joints, color = motif, width = 0.0028f, mode = PointMode.Lines)
            layers += Layer(points = ends, color = motif, width = 0.0028f, mode = PointMode.Lines)
        }
        3 -> {
            val dark = Path()
            for (j in 0 until 4) {
                val yf = mix(front, back, j / 4f)
                val yb = mix(front, back, (j + 1) / 4f)
                for (k in 0 until 8) if ((k + j) % 2 == 0) dark.quad(k * 0.125f + recede(yf), (k + 1) * 0.125f + recede(yf), yf, yb)
            }
            layers += Layer(path = dark, color = motif)
        }
        4 -> {
            val deep = Path()
            for (j in 0 until 5) {
                val yf = mix(front, back, j / 5f)
                val yb = mix(front, back, (j + 1) / 5f)
                for (k in 0 until 10) if ((k * 3 + j * 2) % 5 == 0) deep.quad(k * 0.1f + recede(yf), (k + 1) * 0.1f + recede(yf), yf, yb)
            }
            val grout = ArrayList<Offset>()
            depthLines(0.1f, grout)
            rowLines(5, grout)
            layers += Layer(path = deep, color = motif)
            layers += Layer(points = grout, color = Color(0xFFF4FAFF), width = 0.004f, mode = PointMode.Lines)
        }
        5, 6 -> {
            val light = ArrayList<Offset>()
            val dark = ArrayList<Offset>()
            for (k in 0 until 160) {
                val y = mix(front, back, hash01(k, 1151 + i))
                val p = Offset(hash01(k, 1161 + i) + recede(y), y)
                if (k % 2 == 0) light.add(p) else dark.add(p)
            }
            layers += Layer(points = light, color = base.lighten(0.25f), width = 0.006f)
            layers += Layer(points = dark, color = motif, width = 0.006f)
        }
        7 -> {
            val chips = listOf(Color(0xFFF28DB2), Color(0xFFB69AE6), Color(0xFF8FD9C0), Color(0xFFB8B0BC))
            for ((c, col) in chips.withIndex()) {
                val pts = ArrayList<Offset>(40)
                for (k in 0 until 40) {
                    val y = mix(front - 0.004f, back + 0.004f, hash01(k * 4 + c, 1171))
                    pts.add(Offset(hash01(k * 4 + c, 1172) + recede(y), y))
                }
                layers += Layer(points = pts, color = col, width = 0.005f + 0.002f * (c % 2))
            }
            val joints = ArrayList<Offset>()
            depthLines(0.5f, joints)
            rowLines(2, joints)
            layers += Layer(points = joints, color = Color(0xFFD2C6CF), width = 0.003f, mode = PointMode.Lines)
        }
        else -> {
            val stones = List(3) { Path() }
            val rows = 3
            for (j in 0 until rows) {
                val yf = mix(front, back, j / rows.toFloat()) - 0.004f
                val yb = mix(front, back, (j + 1) / rows.toFloat()) + 0.004f
                var x = if (j % 2 == 0) 0f else -0.12f
                var k = 0
                while (x < 1f) {
                    val w = 0.2f + 0.12f * hash01(j * 10 + k, 1181)
                    val x1 = minOf(x + w, if (j % 2 == 0) 1f else 0.88f)
                    stones[(j + k) % 3].quad(x + 0.008f + recede(yf + 0.004f), x1 - 0.008f + recede(yf + 0.004f), yf, yb)
                    x = x1
                    k++
                    if (j % 2 == 1 && x >= 0.88f) break
                }
            }
            for ((k, p) in stones.withIndex()) layers += Layer(path = p, color = listOf(base.lighten(0.08f), base, base.darken(0.08f))[k])
        }
    }
    return Pattern(base, base, layers)
}

private fun DrawScope.drawLayers(p: Pattern) {
    for (l in p.layers) {
        if (l.path != null) {
            drawPath(l.path, l.color, alpha = l.alpha, style = if (l.width > 0f) Stroke(l.width, cap = StrokeCap.Round) else androidx.compose.ui.graphics.drawscope.Fill)
        } else if (l.points != null) {
            drawPoints(l.points, l.mode, l.color, strokeWidth = l.width, cap = StrokeCap.Round, alpha = l.alpha)
        }
    }
}

/**
 * Wallpaper [index] over the screen area [left]..[right] × [top]..[bottom], with unit point (0, 0)
 * at ([ox], [oy]) and [scale] pixels per unit.
 */
internal fun DrawScope.drawWallpaper(index: Int, left: Float, top: Float, right: Float, bottom: Float, ox: Float, oy: Float, scale: Float) {
    if (right <= left || bottom <= top || index !in 1..WALLPAPERS) return
    val p = wallPattern(index)
    clipRect(left, top, right, bottom) {
        drawRect(Brush.verticalGradient(listOf(p.top, p.bottom), startY = oy + WALL_TOP * scale, endY = oy + WALL_BOTTOM * scale), Offset(left, top), Size(right - left, bottom - top))
        val k0 = floor((left - ox) / scale).toInt() - (if (index == 10) 2 else 0)
        val k1 = ceil((right - ox) / scale).toInt()
        for (k in k0..k1) {
            withTransform({
                translate(ox + k * scale, oy)
                scale(scale, scale, Offset.Zero)
            }) { drawLayers(p) }
        }
    }
}

/**
 * Wallpaper [index] on a slanted face that recedes into the picture: unit x along the face is depth,
 * so one unit moves (DX, DY) × [u]. The face starts at screen x [x0]; heights are scene y × [u].
 */
internal fun DrawScope.drawWallpaperSlanted(index: Int, face: Path, x0: Float, u: Float) {
    if (index !in 1..WALLPAPERS) return
    val p = wallPattern(index)
    val m = Matrix().apply {
        this[0, 0] = Oblique.DX * u
        this[0, 1] = Oblique.DY * u
        this[1, 1] = u
        this[3, 0] = x0
    }
    clipPath(face) {
        withTransform({ transform(m) }) {
            drawRect(Brush.verticalGradient(listOf(p.top, p.bottom), startY = WALL_TOP, endY = WALL_BOTTOM), Offset(-1f, WALL_TOP), Size(2f, WALL_BOTTOM - WALL_TOP))
            // Diagonal stripes start further left, so draw the tiles before this one too.
            for (k in (if (index == 10) -2 else 0)..0) withTransform({ translate(k.toFloat(), 0f) }) { drawLayers(p) }
        }
    }
}

/**
 * Floor [index] inside [area] (screen), with unit point (0, 0) at ([ox], [oy]) and [scale] pixels per
 * unit; the band runs from the back wall (0.80) to the front edge, with soft light toward the front.
 */
internal fun DrawScope.drawFlooring(index: Int, area: Path, ox: Float, oy: Float, scale: Float, left: Float, right: Float) {
    if (index !in 1..FLOORINGS) return
    val p = floorPattern(index)
    clipPath(area) {
        val y0 = oy + FLOOR_BACK * scale
        val y1 = oy + FRONT_Y * scale
        drawRect(p.top, Offset(left, y0), Size(right - left, y1 - y0))
        val k0 = floor((left - ox) / scale).toInt() - 1
        val k1 = ceil((right - ox) / scale).toInt()
        for (k in k0..k1) {
            withTransform({
                translate(ox + k * scale, oy)
                scale(scale, scale, Offset.Zero)
            }) { drawLayers(p) }
        }
        drawRect(
            Brush.verticalGradient(0f to Ink.line.copy(alpha = 0.2f), 0.35f to Ink.line.copy(alpha = 0.04f), 1f to Color.White.copy(alpha = 0.06f), startY = y0, endY = y1),
            Offset(left, y0), Size(right - left, y1 - y0),
        )
    }
}

/** A wallpaper swatch for the designer panel: a strip of wall with its skirting. */
internal fun DrawScope.wallSwatch(index: Int, r: Rect, pen: Pen) {
    val scale = r.height / 0.62f
    drawWallpaper(index, r.left, r.top, r.right, r.bottom, r.left, r.top - 0.1f * scale, scale)
    drawRect(Color(0xFFF7F3EC), Offset(r.left, r.bottom - 0.02f * scale), Size(r.width, 0.02f * scale))
    drawLine(Ink.line, Offset(r.left, r.bottom - 0.02f * scale), Offset(r.right, r.bottom - 0.02f * scale), strokeWidth = pen.lw * 0.7f)
}

/** A floor swatch for the designer panel: a piece of the receding floor band. */
internal fun DrawScope.floorSwatch(index: Int, r: Rect) {
    val scale = r.height / (FRONT_Y - FLOOR_BACK)
    val area = Path().apply { addRect(r) }
    drawFlooring(index, area, r.left - 0.2f * scale, r.top - FLOOR_BACK * scale, scale, r.left, r.right)
}
