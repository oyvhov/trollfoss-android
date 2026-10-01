package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Mine
import app.trollfoss.domain.MineHouse
import app.trollfoss.domain.RoomKind
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The facade: the house the child has built, drawn from [MineHouse] in oblique 3D with an ink line round everything.
 * The same function draws it big in the yard, small in the builder panel and tiny on the map. Every built slot is a
 * module with its own window and its own little detail by room kind; the foundation's hall has the front door.
 */

/** The proportions of a template, in scene units: floor heights, roof height and how deep the house is. */
private class Shp(val gh: Float, val uh: Float, val rh: Float, val depth: Float)

private fun shp(shape: Int) = when (shape) {
    0 -> Shp(0.34f, 0.30f, 0.27f, 0.70f)
    1 -> Shp(0.38f, 0.34f, 0.17f, 0.70f)
    2 -> Shp(0.36f, 0.32f, 0.25f, 0.70f)
    else -> Shp(0.32f, 0.30f, 0.20f, 0.80f)
}

/** How tall the house stands in scene units, roofs, chimney and tower included. */
internal fun mineHouseHeight(h: MineHouse): Float {
    val s = shp(h.shape)
    val floors = if (h.upperBuilt) s.gh + s.uh else s.gh
    var total = floors + s.rh - Oblique.DY * s.depth / 2f + (if (h.chimney) 0.12f else 0f)
    if (h.shape == 2) total = max(total, s.gh + s.uh + 0.3f + 0.04f)
    if (h.flag) total += 0.07f
    return total
}

/** The number of columns the house has: from the hall to the last built slot. */
internal fun mineColumns(h: MineHouse): Int {
    if (!h.started) return 0
    var last = 0
    for (i in 1 until Mine.SLOTS) if (h.ground[i] > 0) last = i
    return last + 1
}

/** How wide the house is in scene units, from the left corner to the far side wall (the tower on the left is not counted). */
internal fun mineHouseWidth(h: MineHouse): Float = mineColumns(h) * Mine.FACADE_MW + Oblique.DX * shp(h.shape).depth + (if (h.shape == 2) 0.6f else 0f)

/** The house's depth on the ground plane, shared with the lawn beneath its rear corners. */
internal fun mineHouseDepth(h: MineHouse): Float = shp(h.shape).depth

/** How much the house is scaled so that two floors and a roof fit under the top of a phone screen. */
internal fun mineFit(h: MineHouse, baseY: Float): Float = min(1f, (baseY - 0.04f) / mineHouseHeight(h))

private fun levelsOf(h: MineHouse): IntArray = IntArray(Mine.SLOTS) { i ->
    when {
        !h.started -> 0
        i == 0 -> if (h.upperBuilt) 2 else 1
        h.ground[i] > 0 -> if (h.upper[i] > 0) 2 else 1
        else -> 0
    }
}

private fun kindOf(h: MineHouse, i: Int, upper: Boolean): RoomKind? = if (i == 0) null else h.kind(if (upper) app.trollfoss.domain.PlaceId.MINE_UPPER else app.trollfoss.domain.PlaceId.MINE_GROUND, i)

/**
 * Draws the house with its first slot's front left corner at pixel ([left], [base]) and [k] pixels to a scene unit.
 * [detail] 2 is full, 1 for cards, 0 for the map. [pop] (0 to 1) makes the whole house spring up from the ground as the
 * foundation finishes; [upperPop] does the same for the second floor. Columns outside [clipL]..[clipR] are skipped.
 */
internal fun DrawScope.drawMineHouse(
    h: MineHouse, left: Float, base: Float, k: Float, pen: Pen,
    night: Float = 0f, t: Float = 0f, winter: Boolean = false, detail: Int = 2,
    pop: Float = 1f, upperPop: Float = 1f, clipL: Float = -1e9f, clipR: Float = 1e9f,
) {
    val cols = mineColumns(h)
    if (cols == 0 || pop <= 0f) return
    val s = shp(h.shape)
    val mw = Mine.FACADE_MW * k
    val sc = popScale(pop)
    val pivot = Offset(left + mw * 0.5f, base)
    if (sc != 1f) {
        scale(sc, sc, pivot) { drawMineHouseBody(h, s, left, base, k, mw, pen, night, t, winter, detail, upperPop, clipL, clipR) }
    } else {
        drawMineHouseBody(h, s, left, base, k, mw, pen, night, t, winter, detail, upperPop, clipL, clipR)
    }
}

private fun DrawScope.drawMineHouseBody(
    h: MineHouse, s: Shp, left: Float, base: Float, k: Float, mw: Float, pen: Pen,
    night: Float, t: Float, winter: Boolean, detail: Int, upperPop: Float, clipL: Float, clipR: Float,
) {
    val levels = levelsOf(h)
    val last = mineColumns(h) - 1
    val gh = s.gh * k
    val uh = s.uh * k
    val rh = s.rh * k
    val dxs = Oblique.DX * s.depth * k
    val dys = Oblique.DY * s.depth * k
    val lit = litAmount(night)
    val wallC = MineC.wall(h.wall).atNight(night, 0.45f)
    val trim = MineC.trim(MineC.wall(h.wall)).atNight(night, 0.4f)
    val roofC = MineC.roof(h.roofColor).atNight(night, 0.45f)
    val sideC = wallC.darken(0.22f)
    fun colL(i: Int) = left + i * mw
    fun visible(i: Int) = colL(i + 1) + dxs > clipL && colL(i) < clipR
    val upperScale = popScale(upperPop)
    val full = detail >= 1

    // A soft shadow on the ground under the whole house.
    drawOval(Ink.shadow, Offset(left - 0.05f * k, base - 0.01f * k), androidx.compose.ui.geometry.Size((last + 1) * mw + dxs + 0.1f * k, 0.05f * k))
    quadFill(Ink.shadow, null, left, base, left + dxs, base + dys, left + (last + 1) * mw + dxs + 0.035f * k, base + dys, left + (last + 1) * mw + 0.035f * k, base)

    if (h.shape == 2) drawTower(h, left - 0.3f * k, base, k, pen, night, t, lit, wallC, trim, roofC, winter, detail)

    // Pass A: the right sides, behind every front.
    for (i in 0..last) {
        val lv = levels[i]
        if (lv == 0 || !visible(i)) continue
        if (i != last && levels[i + 1] >= lv) continue
        val x1 = colL(i + 1)
        val topY = base - gh - (if (lv == 2) uh * (if (i == 0 || h.upper[i] > 0) upperScale else 1f) else 0f)
        quadFill(sideC, pen, x1, base, x1, topY, x1 + dxs, topY + dys, x1 + dxs, base + dys)
        // A window on the side of the last module.
        if (i == last && full) {
            for (f in 0 until lv) {
                val y0 = base - f * (gh + 0f) - (if (f == 1) 0f else 0f)
                val wy0 = (if (f == 0) base - gh * 0.72f else base - gh - uh * 0.7f * upperScale)
                val wy1 = (if (f == 0) base - gh * 0.3f else base - gh - uh * 0.3f * upperScale)
                val a = 0.3f
                val b = 0.7f
                quadFill(lerpC(MineC.glass, MineC.lit, lit).darken(0.1f), pen, x1 + dxs * a, wy0 + dys * a, x1 + dxs * b, wy0 + dys * b, x1 + dxs * b, wy1 + dys * b, x1 + dxs * a, wy1 + dys * a, thin = true)
                if (y0 < 0f) break
            }
        }
    }

    // Pass B: the fronts.
    for (i in 0..last) {
        if (!visible(i)) continue
        val lv = levels[i]
        val x0 = colL(i)
        val x1 = x0 + mw
        if (lv == 0) {
            drawScaffold(x0 + mw * 0.08f, x1 - mw * 0.08f, base, gh * 0.95f, pen)
            continue
        }
        drawFront(h, i, lv, x0, x1, base, gh, uh, k, pen, night, t, lit, wallC, trim, roofC, i == 0, i == last, detail, upperScale, s.depth)
    }

    // Pass C: the roofs, one for each stretch of equal height.
    var i = 0
    while (i <= last) {
        if (levels[i] == 0) { i++; continue }
        var j = i
        while (j + 1 <= last && levels[j + 1] == levels[i]) j++
        if (j >= 0 && colL(j + 1) + dxs > clipL && colL(i) < clipR) {
            val lv = levels[i]
            val topY = base - gh - (if (lv == 2) uh * upperScale else 0f)
            val rightTaller = j + 1 <= last && levels[j + 1] > lv
            drawRoof(h, colL(i), colL(j + 1), topY, rh, dxs, dys, k, pen, roofC, sideC, wallC, trim, winter, rightTaller, i == 0, night, t)
        }
        i = j + 1
    }
}

private fun lerpC(a: Color, b: Color, f: Float): Color = androidx.compose.ui.graphics.lerp(a, b, f)

/** The front of one module: the wall, its texture, the door or the windows, and the small details of its room. */
private fun DrawScope.drawFront(
    h: MineHouse, i: Int, lv: Int, x0: Float, x1: Float, base: Float, gh: Float, uh: Float, k: Float, pen: Pen,
    night: Float, t: Float, lit: Float, wallC: Color, trim: Color, roofC: Color, first: Boolean, lastCol: Boolean,
    detail: Int, upperScale: Float, depth: Float,
) {
    val mw = x1 - x0
    val kindG = kindOf(h, i, false)
    val greenhouse = kindG == RoomKind.GREENHOUSE
    // The ground floor.
    if (greenhouse) {
        drawGlassHouse(x0, x1, base, gh, k, pen, night, lit, t, if (lv == 2) trim else trim)
    } else {
        drawWall(h.shape, x0, x1, base - gh, base, wallC, pen, k)
        // The foot of the wall.
        val footC = if (h.shape == 3) Color(0xFF9EA3B0).atNight(night, 0.4f) else wallC.darken(0.18f)
        drawRect(footC, Offset(x0, base - k * 0.035f), androidx.compose.ui.geometry.Size(mw, k * 0.035f))
        drawLine(Ink.line, Offset(x0, base - k * 0.035f), Offset(x1, base - k * 0.035f), strokeWidth = pen.lw * 0.6f)
        if (first) drawHall(h, x0, x1, base, gh, k, pen, night, lit, trim, roofC, t, detail) else if (detail >= 1) {
            val ww = mw * 0.34f
            drawMineWindow(h.windows, kindG, x0 + (mw - ww) / 2f, base - gh * 0.78f, x0 + (mw + ww) / 2f, base - gh * 0.34f, pen, lit, trim, roofC, t, detail)
            kindDetail(kindG, x0, x1, base, gh, k, pen, night, t)
        }
    }
    // The upper floor.
    if (lv == 2) {
        val upper = (i == 0) || h.upper[i] > 0
        if (upper) {
            val up = upperScale
            val y1 = base - gh
            scale(1f, up, Offset(x0, y1)) {
                val kindU = kindOf(h, i, true)
                if (kindU == RoomKind.GREENHOUSE) {
                    drawGlassHouse(x0, x1, y1, uh, k, pen, night, lit, t, trim)
                } else {
                    drawWall(h.shape, x0, x1, y1 - uh, y1, wallC.lighten(0.04f), pen, k)
                    // The belt between the floors.
                    drawRect(trim.darken(0.05f), Offset(x0 - k * 0.01f, y1 - k * 0.016f), androidx.compose.ui.geometry.Size(mw + k * 0.02f, k * 0.03f))
                    drawRect(Ink.line, Offset(x0 - k * 0.01f, y1 - k * 0.016f), androidx.compose.ui.geometry.Size(mw + k * 0.02f, k * 0.03f), style = pen.thin)
                    if (detail >= 1) {
                        val ww = mw * 0.34f
                        drawMineWindow(h.windows, kindU, x0 + (mw - ww) / 2f, y1 - uh * 0.8f, x0 + (mw + ww) / 2f, y1 - uh * 0.34f, pen, lit, trim, roofC, t, detail)
                        if (i == 0 && detail >= 2) balcony(x0, x1, y1 - uh * 0.34f, k, pen, trim)
                        kindDetail(kindU, x0, x1, y1, uh, k, pen, night, t)
                    }
                }
            }
        }
    }
    // The corner boards.
    if (detail >= 1) {
        val cb = k * 0.022f
        val topY = base - gh - (if (lv == 2) uh * upperScale else 0f)
        if (first) drawCorner(x0, base, topY, cb, trim, pen)
        if (lastCol) drawCorner(x1 - cb, base, topY, cb, trim, pen)
    }
}

private fun DrawScope.drawCorner(x: Float, base: Float, top: Float, w: Float, trim: Color, pen: Pen) {
    drawRect(trim, Offset(x, top), androidx.compose.ui.geometry.Size(w, base - top))
    drawRect(Ink.line, Offset(x, top), androidx.compose.ui.geometry.Size(w, base - top), style = pen.thin)
}

/** A wall in the template's own build: logs, smooth plaster with a belt of stones, a tower's plaster, or red-painted boards. */
private fun DrawScope.drawWall(shape: Int, x0: Float, x1: Float, top: Float, bottom: Float, color: Color, pen: Pen, k: Float) {
    drawRect(color, Offset(x0, top), androidx.compose.ui.geometry.Size(x1 - x0, bottom - top))
    // The shade on the right of every module.
    drawRect(Ink.line.copy(alpha = 0.07f), Offset(x1 - (x1 - x0) * 0.12f, top), androidx.compose.ui.geometry.Size((x1 - x0) * 0.12f, bottom - top))
    val lines = ArrayList<Offset>(24)
    when (shape) {
        0 -> {
            var y = top + k * 0.045f
            while (y < bottom - k * 0.02f) {
                lines.add(Offset(x0, y)); lines.add(Offset(x1, y))
                y += k * 0.052f
            }
            drawPoints(lines, PointMode.Lines, color.darken(0.3f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        }
        3 -> {
            var x = x0 + k * 0.06f
            while (x < x1 - k * 0.02f) {
                lines.add(Offset(x, top)); lines.add(Offset(x, bottom))
                x += k * 0.07f
            }
            drawPoints(lines, PointMode.Lines, color.darken(0.26f), strokeWidth = pen.lw * 0.7f)
        }
        1 -> {
            var y = top + k * 0.06f
            while (y < bottom - k * 0.02f) {
                lines.add(Offset(x0, y)); lines.add(Offset(x1, y))
                y += k * 0.1f
            }
            drawPoints(lines, PointMode.Lines, color.darken(0.1f), strokeWidth = pen.lw * 0.5f)
        }
        else -> {
            var y = top + k * 0.05f
            var row = 0
            while (y < bottom - k * 0.02f) {
                lines.add(Offset(x0, y)); lines.add(Offset(x1, y))
                var x = x0 + (if (row % 2 == 0) k * 0.05f else k * 0.1f)
                while (x < x1) {
                    lines.add(Offset(x, y)); lines.add(Offset(x, y + k * 0.05f))
                    x += k * 0.1f
                }
                y += k * 0.05f
                row++
            }
            drawPoints(lines, PointMode.Lines, color.darken(0.12f), strokeWidth = pen.lw * 0.5f)
        }
    }
    drawRect(Ink.line, Offset(x0, top), androidx.compose.ui.geometry.Size(x1 - x0, bottom - top), style = pen.stroke)
}

/** The hall: the front door with its steps and porch, and a window on each side. */
private fun DrawScope.drawHall(h: MineHouse, x0: Float, x1: Float, base: Float, gh: Float, k: Float, pen: Pen, night: Float, lit: Float, trim: Color, roofC: Color, t: Float, detail: Int) {
    val mw = x1 - x0
    val dw = mw * 0.22f
    val dl = x0 + mw * 0.5f - dw / 2f
    val dh = k * 0.3f
    // Steps.
    val stone = Color(0xFFB9BDC9).atNight(night, 0.4f)
    drawRoundRect(stone, Offset(dl - dw * 0.3f, base), androidx.compose.ui.geometry.Size(dw * 1.6f, k * 0.03f), androidx.compose.ui.geometry.CornerRadius(k * 0.01f))
    drawRoundRect(Ink.line, Offset(dl - dw * 0.3f, base), androidx.compose.ui.geometry.Size(dw * 1.6f, k * 0.03f), androidx.compose.ui.geometry.CornerRadius(k * 0.01f), style = pen.thin)
    drawMineDoor(h.door, dl, base - dh, dl + dw, base, pen, 0f, lit, trim)
    if (detail >= 1) {
        // The porch: a little roof on two posts (cabin), columns (villa), a lantern (the others).
        when (h.shape) {
            0 -> {
                drawLine(Ink.line, Offset(dl - dw * 0.35f, base), Offset(dl - dw * 0.35f, base - dh - k * 0.03f), strokeWidth = pen.lw * 3f, cap = StrokeCap.Round)
                drawLine(Color(0xFFC98A55), Offset(dl - dw * 0.35f, base), Offset(dl - dw * 0.35f, base - dh - k * 0.03f), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
                drawLine(Ink.line, Offset(dl + dw * 1.35f, base), Offset(dl + dw * 1.35f, base - dh - k * 0.03f), strokeWidth = pen.lw * 3f, cap = StrokeCap.Round)
                drawLine(Color(0xFFC98A55), Offset(dl + dw * 1.35f, base), Offset(dl + dw * 1.35f, base - dh - k * 0.03f), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
                quadFill(roofC, pen, dl - dw * 0.55f, base - dh - k * 0.01f, dl + dw * 1.55f, base - dh - k * 0.01f, dl + dw * 1.4f, base - dh - k * 0.07f, dl - dw * 0.4f, base - dh - k * 0.07f)
            }
            1 -> {
                for (side in 0 until 2) {
                    val cx = if (side == 0) dl - dw * 0.38f else dl + dw * 1.38f
                    roundFill(trim, pen, cx - k * 0.014f, base - dh - k * 0.02f, cx + k * 0.014f, base, k * 0.005f, thin = true)
                }
                triFill(trim, pen, dl - dw * 0.55f, base - dh - k * 0.02f, dl + dw * 1.55f, base - dh - k * 0.02f, dl + dw * 0.5f, base - dh - k * 0.1f)
            }
            else -> {
                // A lantern beside the door.
                val lx = dl + dw * 1.28f
                drawLine(Ink.line, Offset(lx, base - dh * 0.86f), Offset(lx, base - dh * 0.72f), strokeWidth = pen.lw * 1.4f)
                roundFill(lerpC(Color(0xFFFFE9A8), MineC.lit, lit), pen, lx - k * 0.014f, base - dh * 0.72f, lx + k * 0.014f, base - dh * 0.55f, k * 0.005f, thin = true)
                if (lit > 0f) drawCircle(MineC.lit.copy(alpha = 0.25f * lit), k * 0.07f, Offset(lx, base - dh * 0.63f))
            }
        }
        // A small window on each side of the door.
        val ww = mw * 0.17f
        val wy1 = base - gh * 0.42f
        val wy0 = base - gh * 0.76f
        drawMineWindow(0, null, x0 + mw * 0.1f, wy0, x0 + mw * 0.1f + ww, wy1, pen, lit, trim, roofC, t, detail)
        drawMineWindow(0, null, x1 - mw * 0.1f - ww, wy0, x1 - mw * 0.1f, wy1, pen, lit, trim, roofC, t, detail)
    }
}

private fun DrawScope.balcony(x0: Float, x1: Float, sillY: Float, k: Float, pen: Pen, trim: Color) {
    val mw = x1 - x0
    val l = x0 + mw * 0.22f
    val r = x1 - mw * 0.22f
    val top = sillY - k * 0.0f
    drawLine(Ink.line, Offset(l, top + k * 0.035f), Offset(r, top + k * 0.035f), strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
    drawLine(trim, Offset(l, top + k * 0.035f), Offset(r, top + k * 0.035f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    var x = l
    while (x <= r + 1f) {
        drawLine(Ink.line, Offset(x, top + k * 0.035f), Offset(x, top + k * 0.085f), strokeWidth = pen.lw * 0.9f)
        x += (r - l) / 6f
    }
    drawLine(Ink.line, Offset(l, top + k * 0.085f), Offset(r, top + k * 0.085f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
}

/** The small detail of each room kind on the outside of its module. */
private fun DrawScope.kindDetail(kind: RoomKind?, x0: Float, x1: Float, floorY: Float, h: Float, k: Float, pen: Pen, night: Float, t: Float) {
    val mw = x1 - x0
    val cx = x0 + mw / 2f
    when (kind) {
        RoomKind.KITCHEN -> {
            // Steam from a little vent.
            val vx = x0 + mw * 0.82f
            drawRoundRect(Color(0xFF8E98A8), Offset(vx - k * 0.02f, floorY - h * 0.5f), androidx.compose.ui.geometry.Size(k * 0.04f, k * 0.03f), androidx.compose.ui.geometry.CornerRadius(k * 0.008f))
            for (n in 0 until 2) {
                val p = ((t * 0.5f + n * 0.5f) % 1f)
                drawCircle(Color.White.copy(alpha = 0.55f * (1f - p)), k * (0.012f + 0.02f * p), Offset(vx + sin(t + n) * k * 0.01f, floorY - h * 0.5f - p * k * 0.12f))
            }
        }
        RoomKind.DINING -> {
            // A lantern.
            val lx = x0 + mw * 0.12f
            drawLine(Ink.line, Offset(lx, floorY - h * 0.8f), Offset(lx, floorY - h * 0.7f), strokeWidth = pen.lw)
            drawCircle(MineC.lit.copy(alpha = 0.5f + 0.4f * litAmount(night)), k * 0.016f, Offset(lx, floorY - h * 0.66f))
            drawCircle(Ink.line, k * 0.016f, Offset(lx, floorY - h * 0.66f), style = pen.thin)
        }
        RoomKind.BEDROOM -> {
            // A tiny balcony of stars: a hanging star mobile.
            val sx = x0 + mw * 0.86f
            drawPath(starPath(Offset(sx, floorY - h * 0.62f), k * 0.026f, k * 0.011f), Color(0xFFFFE680))
            drawPath(starPath(Offset(sx, floorY - h * 0.62f), k * 0.026f, k * 0.011f), Ink.line, style = pen.thin)
        }
        RoomKind.KIDS -> {
            // A string of flags across the front.
            val y0 = floorY - h * 0.9f
            val y1 = floorY - h * 0.86f
            drawLine(Ink.line, Offset(x0 + mw * 0.06f, y0), Offset(x1 - mw * 0.06f, y0), strokeWidth = pen.lw * 0.8f)
            val colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFC83D), Color(0xFF5ED1A4), Color(0xFF5AA9E6))
            for (n in 0 until 6) {
                val fx = x0 + mw * (0.12f + 0.15f * n)
                triFill(colors[n % 4], null, fx - k * 0.014f, y0, fx + k * 0.014f, y0, fx, y1 + k * 0.025f)
            }
        }
        RoomKind.WORKSHOP -> {
            // A pipe with a puff of dust.
            val vx = x0 + mw * 0.84f
            drawRoundRect(Color(0xFF7F8A9E), Offset(vx - k * 0.012f, floorY - h * 0.95f), androidx.compose.ui.geometry.Size(k * 0.024f, k * 0.12f), androidx.compose.ui.geometry.CornerRadius(k * 0.006f))
            val p = ((t * 0.4f) % 1f)
            drawCircle(Color(0xFFD9CFBC).copy(alpha = 0.6f * (1f - p)), k * (0.012f + 0.02f * p), Offset(vx + sin(t * 1.3f) * k * 0.01f, floorY - h * 0.95f - p * k * 0.12f))
        }
        RoomKind.MUSIC -> {
            // A note drifting out.
            val p = ((t * 0.35f) % 1f)
            val nx = cx + sin(t * 1.2f) * k * 0.04f
            val ny = floorY - h * 0.9f - p * k * 0.12f
            val c = Color(0xFF8B5CF6).copy(alpha = 1f - p)
            drawOval(c, Offset(nx - k * 0.012f, ny), androidx.compose.ui.geometry.Size(k * 0.024f, k * 0.017f))
            drawLine(c, Offset(nx + k * 0.011f, ny + k * 0.008f), Offset(nx + k * 0.011f, ny - k * 0.035f), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
        }
        RoomKind.LIBRARY -> {
            // A lamp over the window.
            drawCircle(MineC.lit.copy(alpha = 0.3f + 0.4f * litAmount(night)), k * 0.014f, Offset(cx, floorY - h * 0.93f))
        }
        RoomKind.BATH -> {
            // Bubbles from the window.
            for (n in 0 until 2) {
                val p = ((t * 0.3f + n * 0.5f) % 1f)
                drawCircle(Color(0xFFBFE3FA).copy(alpha = 0.8f * (1f - p)), k * 0.012f, Offset(cx + (n - 0.5f) * k * 0.2f + sin(t * 2f + n) * k * 0.01f, floorY - h * 0.85f - p * k * 0.14f), style = pen.thin)
            }
        }
        else -> Unit
    }
}

/** A module of glass: white frame, panes with a shine, and green plants inside. */
private fun DrawScope.drawGlassHouse(x0: Float, x1: Float, base: Float, h: Float, k: Float, pen: Pen, night: Float, lit: Float, t: Float, trim: Color) {
    val mw = x1 - x0
    val top = base - h
    val glass = lerpC(Color(0xFFBFE9F2).atNight(night, 0.3f), MineC.lit, lit * 0.8f)
    drawRect(glass, Offset(x0, top), androidx.compose.ui.geometry.Size(mw, h))
    // Plants behind the glass.
    drawCircle(Color(0xFF3BC46B), mw * 0.2f, Offset(x0 + mw * 0.3f, base - h * 0.3f))
    drawCircle(Color(0xFF2E8B57), mw * 0.16f, Offset(x0 + mw * 0.65f, base - h * 0.26f))
    drawCircle(Color(0xFFF08CB8), mw * 0.045f, Offset(x0 + mw * 0.34f, base - h * 0.4f))
    drawCircle(Color(0xFFFFC83D), mw * 0.04f, Offset(x0 + mw * 0.7f, base - h * 0.34f))
    // The frame.
    val frame = Color(0xFFF7F3EC).atNight(night, 0.3f)
    for (c in 0..3) {
        val x = x0 + mw * c / 3f
        drawLine(Ink.line, Offset(x, top), Offset(x, base), strokeWidth = pen.lw * 3f)
        drawLine(frame, Offset(x, top), Offset(x, base), strokeWidth = pen.lw * 1.5f)
    }
    for (r in 0..2) {
        val y = top + h * r / 2f
        drawLine(Ink.line, Offset(x0, y), Offset(x1, y), strokeWidth = pen.lw * 3f)
        drawLine(frame, Offset(x0, y), Offset(x1, y), strokeWidth = pen.lw * 1.5f)
    }
    // Shines.
    for (c in 0 until 3) drawLine(Color.White.copy(alpha = 0.6f), Offset(x0 + mw * (c + 0.2f) / 3f, top + h * 0.1f), Offset(x0 + mw * (c + 0.2f) / 3f, top + h * 0.3f), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
    drawRect(Ink.line, Offset(x0, top), androidx.compose.ui.geometry.Size(mw, h), style = pen.stroke)
    // A glass cap on top, like a little ridge.
    triFill(glass.lighten(0.1f), pen, x0 - mw * 0.02f, top, x1 + mw * 0.02f, top, (x0 + x1) / 2f, top - h * 0.28f)
    drawLine(frame, Offset((x0 + x1) / 2f, top), Offset((x0 + x1) / 2f, top - h * 0.28f), strokeWidth = pen.lw)
}

/** The roof over a stretch of columns, in one of the four kinds, with its colour, snow in winter, chimney, smoke and flag. */
private fun DrawScope.drawRoof(
    h: MineHouse, xl: Float, xr: Float, topY: Float, rh: Float, dxs: Float, dys: Float, k: Float, pen: Pen,
    roofC: Color, sideC: Color, wallC: Color, trim: Color, winter: Boolean, rightTaller: Boolean, firstRun: Boolean, night: Float, t: Float,
) {
    val ov = k * 0.05f
    val l = xl - ov
    val r = xr + (if (rightTaller) 0f else ov)
    val rx = (dxs / 2f)
    val ry = (dys / 2f) - rh
    val ridgeL = Offset(xl + rx, topY + ry)
    val ridgeR = Offset(xr + rx, topY + ry)
    val dark = roofC.darken(0.28f)
    val body: DrawScope.() -> Unit = {
        when (h.roof.mod(4)) {
            0, 2 -> {
                val turf = h.roof.mod(4) == 2
                val fill = if (turf) Color(0xFF6FAE5A).atNight(night, 0.45f) else roofC
                // The gable end on the right.
                if (!rightTaller) {
                    triFill(wallC.darken(0.22f), pen, xr, topY, xr + dxs, topY + dys, xr + rx, topY + ry)
                    triFill(if (turf) fill.darken(0.12f) else dark, pen, xr + ov * 0.2f, topY + k * 0.004f, xr + dxs + ov * 0.2f, topY + dys + k * 0.004f, xr + rx + ov * 0.2f, topY + ry - k * 0.006f, thin = true)
                }
                quadFill(fill, pen, l, topY + k * 0.012f, r, topY + k * 0.012f, ridgeR.x + ov * 0.3f, ridgeR.y, ridgeL.x - ov * 0.3f, ridgeL.y)
                if (turf) {
                    // Grass tufts and little flowers along the edge, and the fascia board.
                    val board = roofC
                    drawRect(board, Offset(l, topY + k * 0.006f), androidx.compose.ui.geometry.Size(r - l, k * 0.022f))
                    drawRect(Ink.line, Offset(l, topY + k * 0.006f), androidx.compose.ui.geometry.Size(r - l, k * 0.022f), style = pen.thin)
                    var x = l + k * 0.04f
                    var n = 0
                    while (x < r - k * 0.02f) {
                        val f = (x - l) / (r - l)
                        val yy = (topY + k * 0.012f) + (ridgeL.y - (topY + k * 0.012f)) * (0.35f + 0.5f * ((n * 7) % 5) / 5f)
                        drawCircle(if (n % 3 == 0) Color(0xFFFFE680) else Color(0xFF8BD06A), k * 0.012f, Offset(x, yy))
                        x += k * 0.09f
                        n++
                    }
                } else {
                    // Rows of shingles.
                    val pts = ArrayList<Offset>(16)
                    for (row in 1..4) {
                        val f = row / 5f
                        val a = Offset(l + (ridgeL.x - l) * f, topY + k * 0.012f + (ridgeL.y - topY - k * 0.012f) * f)
                        val b = Offset(r + (ridgeR.x - r) * f, a.y)
                        pts.add(a); pts.add(b)
                    }
                    drawPoints(pts, PointMode.Lines, dark.copy(alpha = 0.6f), strokeWidth = pen.lw * 0.7f)
                    // The eave's shadow.
                    drawRect(Ink.line.copy(alpha = 0.18f), Offset(l, topY + k * 0.012f - k * 0.016f), androidx.compose.ui.geometry.Size(r - l, k * 0.016f))
                }
                if (winter) snowOn(l, r, topY + k * 0.012f, ridgeL, ridgeR, k, pen)
                drawLine(Ink.line, ridgeL, ridgeR, strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            }
            1 -> {
                val hip = (xr - xl) * 0.28f
                // The hip on the right.
                if (!rightTaller) {
                    triFill(roofC.darken(0.16f), pen, xr + ov, topY + k * 0.012f, xr + dxs + ov, topY + dys + k * 0.012f, ridgeR.x - hip * 0.2f, ridgeR.y)
                }
                val rl = Offset(ridgeL.x + hip, ridgeL.y)
                val rr = Offset(ridgeR.x - hip, ridgeR.y)
                quadFill(roofC, pen, l, topY + k * 0.012f, r, topY + k * 0.012f, rr.x, rr.y, rl.x, rl.y)
                val pts = ArrayList<Offset>(16)
                for (row in 1..3) {
                    val f = row / 4f
                    val a = Offset(l + (rl.x - l) * f, topY + k * 0.012f + (rl.y - topY - k * 0.012f) * f)
                    val b = Offset(r + (rr.x - r) * f, a.y)
                    pts.add(a); pts.add(b)
                }
                drawPoints(pts, PointMode.Lines, dark.copy(alpha = 0.6f), strokeWidth = pen.lw * 0.7f)
                drawRect(Ink.line.copy(alpha = 0.18f), Offset(l, topY + k * 0.012f - k * 0.016f), androidx.compose.ui.geometry.Size(r - l, k * 0.016f))
                if (winter) snowOn(l, r, topY + k * 0.012f, rl, rr, k, pen)
                drawLine(Ink.line, rl, rr, strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            }
            else -> {
                // A flat roof with a parapet round it.
                val pt = k * 0.04f
                quadFill(roofC.lighten(0.12f), pen, xl, topY, xr, topY, xr + dxs, topY + dys, xl + dxs, topY + dys)
                if (!rightTaller) quadFill(wallC.darken(0.22f), pen, xr, topY - pt, xr + dxs, topY + dys - pt, xr + dxs, topY + dys, xr, topY)
                quadFill(roofC, pen, xl - ov * 0.4f, topY - pt, xr + (if (rightTaller) 0f else ov * 0.4f), topY - pt, xr + (if (rightTaller) 0f else ov * 0.4f), topY + k * 0.012f, xl - ov * 0.4f, topY + k * 0.012f)
                drawRect(Ink.line.copy(alpha = 0.18f), Offset(xl, topY - pt - k * 0.014f), androidx.compose.ui.geometry.Size(xr - xl, k * 0.014f))
                if (winter) quadFill(Color(0xFFF4F8FF), null, xl, topY - pt, xr, topY - pt, xr + dxs * 0.9f, topY + dys * 0.9f - pt, xl + dxs * 0.9f, topY + dys * 0.9f - pt)
                // Pots on the roof terrace.
                if (xr - xl > k * 1.1f) {
                    val px = xl + (xr - xl) * 0.7f + dxs * 0.4f
                    drawRoundRect(Color(0xFFD9774F), Offset(px - k * 0.02f, topY + dys * 0.45f - k * 0.04f), androidx.compose.ui.geometry.Size(k * 0.04f, k * 0.04f), androidx.compose.ui.geometry.CornerRadius(k * 0.008f))
                    drawCircle(Color(0xFF3BC46B), k * 0.032f, Offset(px, topY + dys * 0.45f - k * 0.055f))
                }
            }
        }
    }
    val clipRight = if (rightTaller) xr else Float.MAX_VALUE
    if (rightTaller) clipRect(right = clipRight) { body() } else body()
    // The chimney and the flag stand on the first stretch.
    if (firstRun) {
        if (h.chimney && h.shape != 2) {
            val cx = xl + Mine.FACADE_MW * k * 0.78f + rx
            val flat = h.roof.mod(4) == 3
            val baseY = if (flat) topY + dys * 0.55f - k * 0.04f else topY + ry + k * 0.045f
            val cw = k * 0.095f
            val chH = k * 0.17f
            box(cx - cw / 2f, baseY - chH, cx + cw / 2f, baseY, Color(0xFFB7664F).atNight(night, 0.4f), pen, k)
            // Bricks.
            drawLine(Ink.line.copy(alpha = 0.4f), Offset(cx - cw / 2f, baseY - chH * 0.66f), Offset(cx + cw / 2f, baseY - chH * 0.66f), strokeWidth = pen.lw * 0.6f)
            drawLine(Ink.line.copy(alpha = 0.4f), Offset(cx - cw / 2f, baseY - chH * 0.33f), Offset(cx + cw / 2f, baseY - chH * 0.33f), strokeWidth = pen.lw * 0.6f)
            // Smoke.
            for (n in 0 until 3) {
                val p = ((t * 0.22f + n / 3f) % 1f)
                drawCircle(Color.White.copy(alpha = 0.7f * (1f - p)), k * (0.025f + 0.045f * p), Offset(cx + p * k * 0.12f + sin(p * 5f + n) * k * 0.012f, baseY - chH - k * 0.03f - p * k * 0.3f))
            }
        }
        if (h.flag && h.shape != 2) {
            val fx = xr - k * 0.18f + rx
            flag(fx, topY + ry + k * 0.02f, k, pen, t)
        }
    }
}

private fun DrawScope.box(l: Float, t: Float, r: Float, b: Float, color: Color, pen: Pen, k: Float) {
    val d = k * 0.04f
    quadFill(color.darken(0.2f), pen, r, t, r + d * 0.5f, t - d * 0.36f, r + d * 0.5f, b - d * 0.36f, r, b, thin = true)
    quadFill(color.lighten(0.2f), pen, l, t, l + d * 0.5f, t - d * 0.36f, r + d * 0.5f, t - d * 0.36f, r, t, thin = true)
    drawRect(color, Offset(l, t), androidx.compose.ui.geometry.Size(r - l, b - t))
    drawRect(Ink.line, Offset(l, t), androidx.compose.ui.geometry.Size(r - l, b - t), style = pen.thin)
}

/** A flagpole with a waving flag. */
internal fun DrawScope.flag(x: Float, baseY: Float, k: Float, pen: Pen, t: Float) {
    val ph = k * 0.2f
    drawLine(Ink.line, Offset(x, baseY), Offset(x, baseY - ph), strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
    drawLine(Color(0xFFE8E4DC), Offset(x, baseY), Offset(x, baseY - ph), strokeWidth = pen.lw * 1f, cap = StrokeCap.Round)
    val w = k * 0.13f
    val fh = k * 0.07f
    val y0 = baseY - ph
    val p = Path().apply {
        moveTo(x, y0)
        cubicTo(x + w * 0.3f, y0 - fh * 0.2f * sin(t * 5f), x + w * 0.7f, y0 + fh * 0.2f * sin(t * 5f + 1f), x + w, y0 + fh * 0.05f * sin(t * 5f))
        lineTo(x + w * 0.92f, y0 + fh + fh * 0.1f * sin(t * 5f + 2f))
        cubicTo(x + w * 0.7f, y0 + fh * 1.2f * 0.9f, x + w * 0.3f, y0 + fh * 0.8f, x, y0 + fh)
        close()
    }
    drawPath(p, Color(0xFFD2443A))
    drawPath(p, Ink.line, style = pen.thin)
    drawLine(Color(0xFFFFC83D), Offset(x, y0 + fh / 2f), Offset(x + w * 0.9f, y0 + fh / 2f + fh * 0.05f * sin(t * 5f + 0.5f)), strokeWidth = pen.lw * 1.3f)
    drawCircle(Color(0xFFFFC83D), pen.lw * 1.6f, Offset(x, baseY - ph))
}

/** Snow lying along the top of a roof slope: a white cap with a round lower edge. */
private fun DrawScope.snowOn(l: Float, r: Float, eaveY: Float, ridgeL: Offset, ridgeR: Offset, k: Float, pen: Pen) {
    val f = 0.55f
    val a = Offset(l + (ridgeL.x - l) * f, eaveY + (ridgeL.y - eaveY) * f)
    val b = Offset(r + (ridgeR.x - r) * f, a.y)
    quadFill(Color(0xFFF4F8FF), null, a.x, a.y, b.x, b.y, ridgeR.x, ridgeR.y - k * 0.006f, ridgeL.x, ridgeL.y - k * 0.006f)
    var x = a.x + k * 0.03f
    while (x < b.x) {
        drawCircle(Color(0xFFF4F8FF), k * 0.022f, Offset(x, a.y))
        x += k * 0.06f
    }
    drawLine(Color(0xFFC9D6EE), Offset(a.x, a.y + k * 0.02f), Offset(b.x, a.y + k * 0.02f), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
}

/** A round stone tower with a pointed roof, on the left of the house; a flag flies from its top. */
private fun DrawScope.drawTower(h: MineHouse, cx: Float, base: Float, k: Float, pen: Pen, night: Float, t: Float, lit: Float, wallC: Color, trim: Color, roofC: Color, winter: Boolean, detail: Int) {
    val s = shp(2)
    val r = k * 0.3f
    val bodyH = (s.gh + s.uh + 0.04f) * k
    val coneH = k * 0.3f
    val stone = wallC.lighten(0.04f)
    fxCyl(cx, base, base - bodyH, r, r, stone, pen, cap = false)
    // Stones.
    val pts = ArrayList<Offset>(20)
    var y = base - k * 0.06f
    var row = 0
    while (y > base - bodyH + k * 0.04f) {
        pts.add(Offset(cx - r * 1.1f, y)); pts.add(Offset(cx + r * 1.1f, y))
        y -= k * 0.075f
        row++
    }
    drawPoints(pts, PointMode.Lines, stone.darken(0.15f), strokeWidth = pen.lw * 0.5f)
    // Windows: round, one above the other.
    if (detail >= 1) {
        for (f in 0 until 2) {
            val wy = base - bodyH * (0.28f + 0.4f * f)
            val wr = k * 0.055f
            drawCircle(trim, wr * 1.25f, Offset(cx, wy))
            drawCircle(Ink.line, wr * 1.25f, Offset(cx, wy), style = pen.stroke)
            drawCircle(lerpC(MineC.glass, MineC.lit, lit), wr, Offset(cx, wy))
            drawLine(Color.White, Offset(cx - wr, wy), Offset(cx + wr, wy), strokeWidth = pen.lw)
            drawLine(Color.White, Offset(cx, wy - wr), Offset(cx, wy + wr), strokeWidth = pen.lw)
        }
    }
    // The roof: a cone with a rim.
    val ry = base - bodyH
    fxCyl(cx, ry, ry - coneH, r * 1.2f, r * 0.01f, roofC, pen, top = roofC, cap = false, bottom = false)
    drawOval(Ink.shadow, Offset(cx - r * 1.2f, ry - k * 0.01f), androidx.compose.ui.geometry.Size(r * 2.4f, k * 0.03f))
    if (winter) {
        drawCircle(Color(0xFFF4F8FF), k * 0.035f, Offset(cx, ry - coneH + k * 0.04f))
    }
    if (h.flag) flag(cx, ry - coneH + k * 0.005f, k, pen, t)
    // The door of the tower is a little arched window on its foot, and ivy.
    drawCircle(Color(0xFF3BC46B), k * 0.04f, Offset(cx - r * 0.7f, base - k * 0.05f))
    drawCircle(Color(0xFF2E8B57), k * 0.03f, Offset(cx - r * 0.55f, base - k * 0.1f))
}
