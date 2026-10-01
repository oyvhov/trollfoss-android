package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import app.trollfoss.domain.MineHouse
import app.trollfoss.domain.RoomKind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Mitt hus: the kit shared by the facade, the yard and the rooms: palettes, doors, windows, scaffolding, the flying
 * tools of the builders. Everything is drawn in pixels; sizes come from a scale [k] (pixels per scene unit).
 */

/** The house the art draws from: set by the app when a world starts (the fixtures and the backgrounds have no other way to ask). */
internal object MineView {
    @JvmField var house: MineHouse? = null
}

internal object MineC {
    val walls = listOf(
        Color(0xFFC2453A), Color(0xFFE6B450), Color(0xFFF3ECDD), Color(0xFF86B8E0), Color(0xFF94C48F), Color(0xFFF4A9BC),
        Color(0xFF53A89E), Color(0xFFC98A55), Color(0xFF7F8A9E), Color(0xFFB7A3E3), Color(0xFFF08C42), Color(0xFF6A5A78),
    )
    val roofs = listOf(
        Color(0xFFB5473A), Color(0xFF6B4A3A), Color(0xFF5C6577), Color(0xFF3F6FB5), Color(0xFF3E8F5A), Color(0xFF34323C),
        Color(0xFFE07A3C), Color(0xFFD9B84E),
    )
    val doors = listOf(
        Color(0xFFC0463B), Color(0xFF3F6FB5), Color(0xFF3E8F5A), Color(0xFFE6B450), Color(0xFF6B4A3A), Color(0xFFF4F1EA),
    )
    val glass = Color(0xFF9CD2F2)
    val glassDeep = Color(0xFF5FA8DC)
    val lit = Color(0xFFFFD66B)
    val grass = Color(0xFF6FAE5A)
    val grassDark = Color(0xFF4F9248)
    val soil = Color(0xFF8A6B45)
    val plank = Color(0xFFE3B27A)
    val plankDark = Color(0xFFB98650)
    val steel = Color(0xFFBAC4D4)

    fun wall(i: Int): Color = walls[i.mod(walls.size)]
    fun roof(i: Int): Color = roofs[i.mod(roofs.size)]

    /** Window frames and corner boards: white on dark walls, a warm grey on light ones. */
    fun trim(wall: Color): Color = if (wall.luminance() > 0.6f) Color(0xFFD9CFBC) else Color(0xFFF7F3EC)
}

/** Windows light up when it gets dark. */
internal fun litAmount(night: Float): Float = ramp((night - 0.3f) / 0.4f)

private val scratch = Path()

/** A flat filled four-sided shape (and its ink rim when [pen] is given), without allocating a path. */
internal fun DrawScope.quadFill(c: Color, pen: Pen?, x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, thin: Boolean = false) {
    scratch.reset()
    scratch.moveTo(x0, y0)
    scratch.lineTo(x1, y1)
    scratch.lineTo(x2, y2)
    scratch.lineTo(x3, y3)
    scratch.close()
    drawPath(scratch, c)
    if (pen != null) drawPath(scratch, Ink.line, style = if (thin) pen.thin else pen.stroke)
}

internal fun DrawScope.triFill(c: Color, pen: Pen?, x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, thin: Boolean = false) {
    scratch.reset()
    scratch.moveTo(x0, y0)
    scratch.lineTo(x1, y1)
    scratch.lineTo(x2, y2)
    scratch.close()
    drawPath(scratch, c)
    if (pen != null) drawPath(scratch, Ink.line, style = if (thin) pen.thin else pen.stroke)
}

/** A rounded rectangle with its ink rim, flat. */
internal fun DrawScope.roundFill(c: Color, pen: Pen?, l: Float, t: Float, r: Float, b: Float, rad: Float, thin: Boolean = false) {
    drawRoundRect(c, Offset(l, t), Size(r - l, b - t), CornerRadius(rad, rad))
    if (pen != null) drawRoundRect(Ink.line, Offset(l, t), Size(r - l, b - t), CornerRadius(rad, rad), style = if (thin) pen.thin else pen.stroke)
}

/** The top of a door or window that is round: a rectangle from [t] down to [b] with a half circle on top. */
internal fun archPath(l: Float, t: Float, r: Float, b: Float): Path = Path().apply {
    val rad = (r - l) / 2f
    moveTo(l, b)
    lineTo(l, t + rad)
    arcTo(Rect(l, t, r, t + rad * 2f), 180f, 180f, false)
    lineTo(r, b)
    close()
}

// ------------------------------------------------------------------------------------------------ doors

/**
 * One of the six front doors in the rectangle ([l], [t])–([r], [b]). [open] from 0 to 1 swings it open, showing a warm hall.
 * [lit] glows through its glass at night.
 */
internal fun DrawScope.drawMineDoor(style: Int, l: Float, t: Float, r: Float, b: Float, pen: Pen, open: Float = 0f, lit: Float = 0f, trim: Color = Color(0xFFF7F3EC)) {
    val w = r - l
    val h = b - t
    val body = MineC.doors[style.mod(MineC.doors.size)]
    val arched = style == 1
    // The frame.
    val m = w * 0.09f
    if (arched) {
        val p = archPath(l - m, t - m, r + m, b)
        drawPath(p, trim)
        drawPath(p, Ink.line, style = pen.stroke)
    } else {
        roundFill(trim, pen, l - m, t - m, r + m, b, w * 0.08f)
    }
    // The opening: a warm hall seen through the open door.
    val hall = lerp(Color(0xFF6B4A3A), MineC.lit, 0.35f + 0.5f * lit)
    val leafW = w * (1f - 0.62f * open)
    val hole = if (arched) archPath(l, t, r, b) else Path().apply { addRect(Rect(l, t, r, b)) }
    drawPath(hole, if (open > 0.02f) hall else body)
    if (open > 0.02f) {
        drawOval(Color.White.copy(alpha = 0.14f * open), Offset(l + w * 0.15f, b - h * 0.14f), Size(w * 0.7f, h * 0.1f))
    }
    // The leaf, hinged on the left.
    val dark = body.darken(0.22f)
    if (arched) {
        val leaf = archPath(l, t, l + leafW, b)
        drawPath(leaf, body)
        drawPath(leaf, Ink.line, style = pen.thin)
    } else {
        drawRect(body, Offset(l, t), Size(leafW, h))
        drawRect(Ink.line, Offset(l, t), Size(leafW, h), style = pen.thin)
    }
    val cx = l + leafW / 2f
    when (style.mod(6)) {
        0 -> {
            for (row in 0 until 2) {
                val y0 = t + h * (0.1f + 0.45f * row)
                roundFill(dark, pen, l + leafW * 0.16f, y0, l + leafW * 0.84f, y0 + h * 0.36f, w * 0.05f, thin = true)
                roundFill(body.lighten(0.1f), null, l + leafW * 0.24f, y0 + h * 0.05f, l + leafW * 0.76f, y0 + h * 0.31f, w * 0.03f)
            }
        }
        1 -> {
            drawCircle(dark, leafW * 0.28f, Offset(cx, t + h * 0.3f))
            drawCircle(Color(0xFFBFE3FA).lighten(0.2f * lit), leafW * 0.22f, Offset(cx, t + h * 0.3f))
            drawLine(Ink.line, Offset(l + leafW * 0.1f, t + h * 0.62f), Offset(l + leafW * 0.9f, t + h * 0.62f), strokeWidth = pen.lw * 0.7f)
            drawCircle(Ink.line, leafW * 0.22f, Offset(cx, t + h * 0.3f), style = pen.thin)
        }
        2 -> {
            val gl = Rect(l + leafW * 0.2f, t + h * 0.12f, l + leafW * 0.8f, t + h * 0.46f)
            drawRoundRect(lerp(MineC.glass, MineC.lit, lit), gl.topLeft, gl.size, CornerRadius(w * 0.04f))
            drawLine(Color.White, Offset(gl.center.x, gl.top), Offset(gl.center.x, gl.bottom), strokeWidth = pen.lw * 0.8f)
            drawRoundRect(Ink.line, gl.topLeft, gl.size, CornerRadius(w * 0.04f), style = pen.thin)
            roundFill(dark, pen, l + leafW * 0.2f, t + h * 0.56f, l + leafW * 0.8f, t + h * 0.9f, w * 0.04f, thin = true)
        }
        3 -> {
            drawCircle(Color.White, leafW * 0.3f, Offset(cx, t + h * 0.32f))
            drawCircle(lerp(MineC.glass, MineC.lit, lit), leafW * 0.23f, Offset(cx, t + h * 0.32f))
            drawLine(Color.White, Offset(cx - leafW * 0.23f, t + h * 0.32f), Offset(cx + leafW * 0.23f, t + h * 0.32f), strokeWidth = pen.lw * 0.8f)
            drawLine(Color.White, Offset(cx, t + h * 0.32f - leafW * 0.23f), Offset(cx, t + h * 0.32f + leafW * 0.23f), strokeWidth = pen.lw * 0.8f)
            drawCircle(Ink.line, leafW * 0.3f, Offset(cx, t + h * 0.32f), style = pen.thin)
            for (k in 0 until 3) drawLine(dark, Offset(l + leafW * 0.12f, t + h * (0.62f + 0.11f * k)), Offset(l + leafW * 0.88f, t + h * (0.62f + 0.11f * k)), strokeWidth = pen.lw * 0.7f)
        }
        4 -> {
            for (k in 1 until 4) drawLine(dark, Offset(l + leafW * k / 4f, t + h * 0.04f), Offset(l + leafW * k / 4f, b - h * 0.02f), strokeWidth = pen.lw * 0.7f)
            val c = Offset(cx, t + h * 0.3f)
            val s = leafW * 0.17f
            drawPath(heartPath(c.x, c.y, s), MineC.lit.copy(alpha = 0.55f + 0.4f * lit))
            drawPath(heartPath(c.x, c.y, s), Ink.line, style = pen.thin)
        }
        else -> {
            val mid = l + leafW * 0.5f
            for (side in 0 until 2) {
                val x0 = if (side == 0) l + leafW * 0.1f else mid + leafW * 0.04f
                val x1 = if (side == 0) mid - leafW * 0.04f else l + leafW * 0.9f
                drawRoundRect(lerp(MineC.glass, MineC.lit, lit), Offset(x0, t + h * 0.1f), Size(x1 - x0, h * 0.56f), CornerRadius(w * 0.03f))
                drawRoundRect(Ink.line, Offset(x0, t + h * 0.1f), Size(x1 - x0, h * 0.56f), CornerRadius(w * 0.03f), style = pen.thin)
                drawLine(Color.White.copy(alpha = 0.7f), Offset(x0 + (x1 - x0) * 0.2f, t + h * 0.14f), Offset(x0 + (x1 - x0) * 0.2f, t + h * 0.4f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
            }
            drawLine(Ink.line, Offset(mid, t + h * 0.04f), Offset(mid, b), strokeWidth = pen.lw * 0.8f)
        }
    }
    // The handle.
    val hx = l + leafW * (if (style == 5) 0.42f else 0.8f)
    drawCircle(Color(0xFFE0B04A), w * 0.045f, Offset(hx, t + h * 0.58f))
    drawCircle(Ink.line, w * 0.045f, Offset(hx, t + h * 0.58f), style = pen.thin)
    if (style == 5) {
        drawCircle(Color(0xFFE0B04A), w * 0.045f, Offset(l + leafW * 0.58f, t + h * 0.58f))
        drawCircle(Ink.line, w * 0.045f, Offset(l + leafW * 0.58f, t + h * 0.58f), style = pen.thin)
    }
}

internal fun heartPath(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + s * 0.75f)
    cubicTo(cx - s * 1.3f, cy - s * 0.05f, cx - s * 0.7f, cy - s * 1.05f, cx, cy - s * 0.45f)
    cubicTo(cx + s * 0.7f, cy - s * 1.05f, cx + s * 1.3f, cy - s * 0.05f, cx, cy + s * 0.75f)
    close()
}

// ---------------------------------------------------------------------------------------------- windows

/**
 * A window in the rectangle ([l], [t])–([r], [b]) in one of the four styles, with what a room of [kind] shows in the glass.
 * [trim] is the frame; [shutter] colours the shutters of style 3. Warm light fills the glass when it is [lit].
 */
internal fun DrawScope.drawMineWindow(style: Int, kind: RoomKind?, l: Float, t: Float, r: Float, b: Float, pen: Pen, lit: Float, trim: Color, shutter: Color, time: Float, detail: Int = 2, dark: Float = 0f) {
    val w = r - l
    val h = b - t
    val s = if (kind == RoomKind.KIDS || kind == RoomKind.MUSIC) 2 else if (kind == RoomKind.LIBRARY) 1 else if (kind == RoomKind.WORKSHOP) 3 else style.mod(4)
    val cx = (l + r) / 2f
    val glass = lerp(lerp(MineC.glass, Color(0xFF1D2A5C), dark), MineC.lit, lit)
    val frame = pen.lw * 1.2f
    when (s) {
        1 -> {
            val m = w * 0.08f
            val outer = archPath(l - m, t - m, r + m, b + m * 0.6f)
            drawPath(outer, trim)
            drawPath(outer, Ink.line, style = pen.stroke)
            val inner = archPath(l, t, r, b)
            drawPath(inner, glass)
            if (detail > 1) windowContents(kind, l, t, r, b, lit, pen, time)
            drawLine(Color.White, Offset(cx, t), Offset(cx, b), strokeWidth = frame)
            drawLine(Color.White, Offset(l, t + h * 0.45f), Offset(r, t + h * 0.45f), strokeWidth = frame)
            drawPath(inner, Ink.line, style = pen.thin)
            // The sill with a flower box.
            roundFill(trim, pen, l - m * 1.4f, b, r + m * 1.4f, b + h * 0.09f, h * 0.03f, thin = true)
            if (detail > 1 && kind != RoomKind.BATH) {
                for (i in 0 until 4) {
                    val fx = l + w * (0.15f + 0.23f * i)
                    drawCircle(Color(0xFFF08CB8), w * 0.07f, Offset(fx, b - h * 0.01f))
                    drawCircle(Color(0xFF3BC46B), w * 0.05f, Offset(fx + w * 0.04f, b + h * 0.02f))
                }
            }
        }
        2 -> {
            val rad = min(w, h) / 2f
            val c = Offset(cx, (t + b) / 2f)
            drawCircle(trim, rad * 1.18f, c)
            drawCircle(Ink.line, rad * 1.18f, c, style = pen.stroke)
            drawCircle(glass, rad, c)
            if (detail > 1) windowContents(kind, l, t, r, b, lit, pen, time, round = true)
            drawLine(Color.White, Offset(c.x - rad, c.y), Offset(c.x + rad, c.y), strokeWidth = frame)
            drawLine(Color.White, Offset(c.x, c.y - rad), Offset(c.x, c.y + rad), strokeWidth = frame)
            drawCircle(Ink.line, rad, c, style = pen.thin)
        }
        3 -> {
            val sw = w * 0.26f
            // Shutters, slatted.
            for (side in 0 until 2) {
                val x0 = if (side == 0) l - sw - w * 0.04f else r + w * 0.04f
                roundFill(shutter, pen, x0, t - h * 0.02f, x0 + sw, b + h * 0.02f, w * 0.03f, thin = true)
                var yy = t + h * 0.08f
                while (yy < b) {
                    drawLine(shutter.darken(0.3f), Offset(x0 + sw * 0.15f, yy), Offset(x0 + sw * 0.85f, yy), strokeWidth = pen.lw * 0.6f)
                    yy += h * 0.14f
                }
            }
            roundFill(trim, pen, l - w * 0.05f, t - w * 0.05f, r + w * 0.05f, b + w * 0.05f, w * 0.04f)
            drawRect(glass, Offset(l, t), Size(w, h))
            if (detail > 1) windowContents(kind, l, t, r, b, lit, pen, time)
            drawLine(Color.White, Offset(cx, t), Offset(cx, b), strokeWidth = frame)
            drawRect(Ink.line, Offset(l, t), Size(w, h), style = pen.thin)
        }
        else -> {
            roundFill(trim, pen, l - w * 0.07f, t - w * 0.07f, r + w * 0.07f, b + w * 0.07f, w * 0.05f)
            drawRect(glass, Offset(l, t), Size(w, h))
            if (detail > 1) windowContents(kind, l, t, r, b, lit, pen, time)
            drawLine(Color.White, Offset(cx, t), Offset(cx, b), strokeWidth = frame)
            drawLine(Color.White, Offset(l, (t + b) / 2f), Offset(r, (t + b) / 2f), strokeWidth = frame)
            drawRect(Ink.line, Offset(l, t), Size(w, h), style = pen.thin)
            roundFill(trim.darken(0.06f), pen, l - w * 0.12f, b + w * 0.07f, r + w * 0.12f, b + w * 0.15f, w * 0.03f, thin = true)
        }
    }
    // A little shine on the glass.
    if (detail > 1 && lit < 0.5f) drawLine(Color.White.copy(alpha = 0.5f), Offset(l + w * 0.14f, t + h * 0.18f), Offset(l + w * 0.14f, t + h * 0.38f), strokeWidth = pen.lw * 1.3f, cap = StrokeCap.Round)
}

/** What each kind of room shows in its window: curtains, a plant, a lamp, books, stars, bunting, frost. */
private fun DrawScope.windowContents(kind: RoomKind?, l: Float, t: Float, r: Float, b: Float, lit: Float, pen: Pen, time: Float, round: Boolean = false) {
    val w = r - l
    val h = b - t
    val cx = (l + r) / 2f
    when (kind) {
        RoomKind.LIVING -> {
            val c = Color(0xFF5A8FCB)
            drawRect(c, Offset(l, t), Size(w * 0.24f, h))
            drawRect(c, Offset(r - w * 0.24f, t), Size(w * 0.24f, h))
            drawLine(c.darken(0.3f), Offset(l + w * 0.12f, t), Offset(l + w * 0.12f, b), strokeWidth = pen.lw * 0.6f)
            drawLine(c.darken(0.3f), Offset(r - w * 0.12f, t), Offset(r - w * 0.12f, b), strokeWidth = pen.lw * 0.6f)
        }
        RoomKind.KITCHEN -> {
            drawRect(Color(0xFFD2443A), Offset(cx - w * 0.14f, b - h * 0.2f), Size(w * 0.28f, h * 0.2f))
            drawCircle(Color(0xFF3BC46B), w * 0.14f, Offset(cx, b - h * 0.3f))
            drawCircle(Color(0xFF2E8B57), w * 0.08f, Offset(cx + w * 0.1f, b - h * 0.36f))
        }
        RoomKind.DINING -> {
            drawLine(Ink.line, Offset(cx, t), Offset(cx, t + h * 0.25f), strokeWidth = pen.lw * 0.7f)
            drawPath(Path().apply { moveTo(cx - w * 0.14f, t + h * 0.4f); lineTo(cx - w * 0.06f, t + h * 0.25f); lineTo(cx + w * 0.06f, t + h * 0.25f); lineTo(cx + w * 0.14f, t + h * 0.4f); close() }, Color(0xFFD9774F))
            drawCircle(MineC.lit.copy(alpha = 0.45f), w * 0.2f, Offset(cx, t + h * 0.45f))
        }
        RoomKind.BEDROOM -> {
            val c = Color(0xFFF2B8D0)
            drawRect(c, Offset(l, t), Size(w, h))
            drawCircle(Color(0xFFFFF0BF), w * 0.12f, Offset(cx - w * 0.1f, t + h * 0.34f))
            drawCircle(c, w * 0.11f, Offset(cx - w * 0.04f, t + h * 0.3f))
            drawCircle(Color(0xFFFFF0BF), w * 0.025f, Offset(cx + w * 0.22f, t + h * 0.55f))
            drawCircle(Color(0xFFFFF0BF), w * 0.02f, Offset(cx + w * 0.1f, t + h * 0.72f))
        }
        RoomKind.KIDS -> {
            val colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFC83D), Color(0xFF5ED1A4), Color(0xFF5AA9E6))
            for (i in 0 until 4) {
                val x0 = l + w * (0.08f + 0.22f * i)
                triFill(colors[i], null, x0, t + h * 0.1f, x0 + w * 0.2f, t + h * 0.1f, x0 + w * 0.1f, t + h * 0.4f)
            }
            drawCircle(Color(0xFFFFE680), w * 0.08f, Offset(cx, b - h * 0.22f))
        }
        RoomKind.BATH -> {
            drawRect(Color.White.copy(alpha = 0.55f), Offset(l, t), Size(w, h))
            for (i in 0 until 4) drawLine(Color(0xFFBFE3FA), Offset(l + w * (0.15f + 0.23f * i), t), Offset(l + w * (0.15f + 0.23f * i), b), strokeWidth = pen.lw * 1.2f)
        }
        RoomKind.LIBRARY -> {
            val cols = listOf(Color(0xFFD2443A), Color(0xFF3F6FB5), Color(0xFFE6B450), Color(0xFF3E8F5A), Color(0xFF8B5CF6))
            for (i in 0 until 5) {
                val bw = w * 0.15f
                val bh = h * (0.34f + 0.06f * ((i * 7) % 3))
                drawRect(cols[i], Offset(l + w * 0.05f + bw * i, b - bh), Size(bw * 0.9f, bh))
            }
        }
        RoomKind.WORKSHOP -> {
            drawCircle(Color(0xFF5B5568), w * 0.12f, Offset(cx, (t + b) / 2f))
            for (i in 0 until 6) {
                val a = i * PI.toFloat() / 3f + time * 0.6f
                drawCircle(Color(0xFF5B5568), w * 0.035f, Offset(cx + cos(a) * w * 0.15f, (t + b) / 2f + sin(a) * w * 0.15f))
            }
            drawCircle(MineC.glass, w * 0.05f, Offset(cx, (t + b) / 2f))
        }
        RoomKind.MUSIC -> {
            val c = Color(0xFF3B2F7A)
            drawRect(c, Offset(l, t), Size(w, h))
            for (i in 0 until 2) {
                val x = cx + (i - 0.5f) * w * 0.3f
                drawOval(Color(0xFFFFE680), Offset(x - w * 0.07f, b - h * (0.4f - 0.12f * i)), Size(w * 0.14f, w * 0.1f))
                drawLine(Color(0xFFFFE680), Offset(x + w * 0.065f, b - h * (0.35f - 0.12f * i)), Offset(x + w * 0.065f, b - h * (0.75f - 0.12f * i)), strokeWidth = pen.lw * 1.2f)
            }
        }
        RoomKind.GREENHOUSE -> {
            drawCircle(Color(0xFF3BC46B), w * 0.2f, Offset(cx, b - h * 0.25f))
            drawCircle(Color(0xFFF08CB8), w * 0.06f, Offset(cx + w * 0.1f, b - h * 0.34f))
        }
        null -> {
            // The hall: a lamp's glow.
            drawCircle(MineC.lit.copy(alpha = 0.25f), min(w, h) * 0.3f, Offset(cx, (t + b) / 2f))
        }
    }
    if (lit > 0f) drawRect(MineC.lit.copy(alpha = 0.18f * lit), Offset(l, t), Size(w, h))
}

// ----------------------------------------------------------------------------------- scaffolding and tools

/** A scaffold: four poles, a plank floor and diagonal braces, from the ground at [base] up [h] pixels, [x0] to [x1]. */
internal fun DrawScope.drawScaffold(x0: Float, x1: Float, base: Float, h: Float, pen: Pen, depth: Float = 0f, rise: Float = 1f) {
    val top = base - h * rise
    val pole = Color(0xFFC49A62)
    val w = x1 - x0
    for (i in 0..2) {
        val x = x0 + w * i / 2f
        drawLine(Ink.line, Offset(x, base), Offset(x, top), strokeWidth = pen.lw * 3.1f, cap = StrokeCap.Round)
        drawLine(pole, Offset(x, base), Offset(x, top), strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
    }
    val floors = 3
    for (k in 1..floors) {
        val y = base - h * rise * k / floors
        drawLine(Ink.line, Offset(x0, y), Offset(x1, y), strokeWidth = pen.lw * 2.8f, cap = StrokeCap.Round)
        drawLine(MineC.plank, Offset(x0, y), Offset(x1, y), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
    }
    drawLine(Ink.line, Offset(x0, base), Offset(x1, top + h * rise * 0.33f), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(pole, Offset(x0, base), Offset(x1, top + h * rise * 0.33f), strokeWidth = pen.lw, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset(x1, base), Offset(x0, top + h * rise * 0.33f), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(pole, Offset(x1, base), Offset(x0, top + h * rise * 0.33f), strokeWidth = pen.lw, cap = StrokeCap.Round)
}

/** A hammer standing with its head at (cx, cy), [s] pixels long, turned [angle] degrees. */
internal fun DrawScope.drawHammer(cx: Float, cy: Float, s: Float, angle: Float, pen: Pen) {
    rotate(angle, Offset(cx, cy)) {
        val hw = s * 0.07f
        drawLine(Ink.line, Offset(cx, cy), Offset(cx, cy + s), strokeWidth = hw * 2f + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(Color(0xFFC98A55), Offset(cx, cy), Offset(cx, cy + s), strokeWidth = hw * 2f, cap = StrokeCap.Round)
        roundFill(Color(0xFF8E98A8), pen, cx - s * 0.26f, cy - s * 0.12f, cx + s * 0.2f, cy + s * 0.1f, s * 0.04f)
        drawLine(Color.White.copy(alpha = 0.6f), Offset(cx - s * 0.2f, cy - s * 0.07f), Offset(cx + s * 0.1f, cy - s * 0.07f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    }
}

/** A hand saw with its handle at (cx, cy), [s] pixels long, pointing right. */
internal fun DrawScope.drawSaw(cx: Float, cy: Float, s: Float, angle: Float, pen: Pen) {
    rotate(angle, Offset(cx, cy)) {
        val blade = Path().apply {
            moveTo(cx + s * 0.12f, cy - s * 0.08f)
            lineTo(cx + s, cy - s * 0.02f)
            lineTo(cx + s, cy + s * 0.1f)
            lineTo(cx + s * 0.12f, cy + s * 0.16f)
            close()
        }
        drawPath(blade, Color(0xFFD7DEE8))
        drawPath(blade, Ink.line, style = pen.stroke)
        var x = cx + s * 0.2f
        while (x < cx + s * 0.96f) {
            drawLine(Ink.line, Offset(x, cy + s * 0.12f), Offset(x + s * 0.03f, cy + s * 0.17f), strokeWidth = pen.lw * 0.8f)
            x += s * 0.06f
        }
        roundFill(Color(0xFFE6B450), pen, cx - s * 0.08f, cy - s * 0.1f, cx + s * 0.16f, cy + s * 0.18f, s * 0.06f)
    }
}

/** A cordless drill pointing right, centred on (cx, cy). */
internal fun DrawScope.drawDrill(cx: Float, cy: Float, s: Float, pen: Pen, spin: Float) {
    roundFill(Color(0xFF3F8FCB), pen, cx - s * 0.3f, cy - s * 0.1f, cx + s * 0.2f, cy + s * 0.08f, s * 0.05f)
    roundFill(Color(0xFF3F8FCB).darken(0.2f), pen, cx - s * 0.16f, cy + s * 0.06f, cx - s * 0.02f, cy + s * 0.3f, s * 0.04f)
    drawRect(Color(0xFFBAC4D4), Offset(cx + s * 0.2f, cy - s * 0.04f), Size(s * 0.1f, s * 0.08f))
    drawLine(Ink.line, Offset(cx + s * 0.3f, cy), Offset(cx + s * 0.55f, cy), strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
    drawLine(Color(0xFFD7DEE8), Offset(cx + s * 0.3f, cy), Offset(cx + s * 0.55f, cy), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    for (i in 0 until 3) {
        val x = cx + s * (0.34f + 0.07f * ((i + spin * 6f) % 3f) )
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(x, cy - s * 0.03f), Offset(x + s * 0.03f, cy + s * 0.03f), strokeWidth = pen.lw * 0.7f)
    }
}

/**
 * The tools that build by themselves: a hammer that hits, a saw that saws and a drill that whirrs, hovering about
 * the middle (cx, cy) of a place of work. [t] is the clock, [amount] from 0 to 1 how busy they are.
 */
internal fun DrawScope.drawBuildTools(cx: Float, cy: Float, s: Float, t: Float, pen: Pen, amount: Float) {
    if (amount <= 0.02f) return
    val a = amount.coerceIn(0f, 1f)
    // The hammer: up and down with a snap.
    val beat = (sin(t * 11f) * 0.5f + 0.5f)
    val swing = -50f + 70f * beat * beat
    drawHammer(cx - s * 0.55f, cy - s * 0.1f - 0.05f * s * sin(t * 2.3f), s * 0.5f * a, swing, pen)
    // The saw: back and forth, rocking.
    val sawX = sin(t * 8.4f) * s * 0.07f
    drawSaw(cx - s * 0.1f + sawX, cy + s * 0.12f + 0.03f * s * sin(t * 1.7f), s * 0.5f * a, 12f + 6f * sin(t * 8.4f), pen)
    // The drill: shivering.
    drawDrill(cx + s * 0.5f + 0.01f * s * sin(t * 37f), cy - s * 0.04f + 0.04f * s * sin(t * 2.9f + 1f), s * 0.5f * a, pen, t * 5f)
    // Sparkles round the work.
    for (i in 0 until 3) {
        val p = ((t * 1.3f + i / 3f) % 1f)
        val x = cx + (i - 1) * s * 0.4f + sin(t + i) * s * 0.05f
        val y = cy - s * 0.22f - p * s * 0.25f
        twinkle(Offset(x, y), s * 0.03f * (1f - p) + pen.lw, Color(0xFFFFD447), a * (1f - p))
    }
}

/** A crane: a tall tower with a jib reaching right, and a hook at ([hx], [hy]) on a cable. [k] is pixels per unit. */
internal fun DrawScope.drawCrane(tx: Float, base: Float, towerH: Float, jib: Float, hx: Float, hy: Float, pen: Pen, k: Float) {
    val steel = Color(0xFFE6B450)
    val w = k * 0.1f
    val top = base - towerH
    // The lattice tower.
    drawRect(steel, Offset(tx - w / 2f, top), Size(w, towerH))
    drawRect(Ink.line, Offset(tx - w / 2f, top), Size(w, towerH), style = pen.stroke)
    var y = base
    var flip = true
    while (y > top + w) {
        val y2 = y - w * 1.1f
        drawLine(Ink.line, Offset(if (flip) tx - w / 2f else tx + w / 2f, y), Offset(if (flip) tx + w / 2f else tx - w / 2f, y2), strokeWidth = pen.lw * 0.9f)
        flip = !flip
        y = y2
    }
    // The jib and the counterweight.
    quadFill(steel, pen, tx - k * 0.3f, top - w * 0.2f, tx + jib, top - w * 0.2f, tx + jib, top + w * 0.35f, tx - k * 0.3f, top + w * 0.35f)
    roundFill(Color(0xFF7F8A9E), pen, tx - k * 0.32f, top + w * 0.35f, tx - k * 0.14f, top + w * 1.1f, w * 0.1f)
    triFill(steel, pen, tx - w * 0.5f, top - w * 0.2f, tx + w * 0.5f, top - w * 0.2f, tx, top - w * 1.5f)
    // The cable and the hook.
    val cy = top + w * 0.35f
    drawLine(Ink.line, Offset(hx, cy), Offset(hx, hy), strokeWidth = pen.lw * 1.6f)
    drawCircle(Color(0xFFD2443A), w * 0.22f, Offset(hx, hy))
    drawCircle(Ink.line, w * 0.22f, Offset(hx, hy), style = pen.thin)
    drawRect(Color(0xFFD2443A), Offset(hx - w * 0.3f, cy - w * 0.1f), Size(w * 0.6f, w * 0.3f))
}

/** Dust and sawdust lying about a place of work. */
internal fun DrawScope.drawPlankPile(x: Float, y: Float, k: Float, pen: Pen, count: Int = 4) {
    for (i in 0 until count) {
        val yy = y - i * k * 0.018f
        val off = if (i % 2 == 0) 0f else k * 0.015f
        roundFill(if (i % 2 == 0) MineC.plank else MineC.plankDark.lighten(0.2f), pen, x + off, yy - k * 0.016f, x + off + k * 0.2f, yy, k * 0.004f, thin = true)
        drawLine(MineC.plankDark, Offset(x + off + k * 0.04f, yy - k * 0.008f), Offset(x + off + k * 0.1f, yy - k * 0.008f), strokeWidth = pen.lw * 0.5f)
    }
}

/** A frame of dashes round the rectangle, and a plus in a ball in its middle: «build here». */
internal fun DrawScope.drawBuildFrame(l: Float, t: Float, r: Float, b: Float, pen: Pen, time: Float, picked: Boolean) {
    val pulse = 0.5f + 0.5f * sin(time * 3.2f)
    val col = if (picked) Color(0xFFFFC83D) else Color(0xFFFFFFFF)
    val stroke = Stroke(width = pen.lw * (if (picked) 3.2f else 2.4f), cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(pen.lw * 7f, pen.lw * 6f), time * pen.lw * 8f))
    drawRoundRect(Color.Black.copy(alpha = 0.12f), Offset(l, t), Size(r - l, b - t), CornerRadius(pen.lw * 10f))
    drawRoundRect(col, Offset(l, t), Size(r - l, b - t), CornerRadius(pen.lw * 10f), style = stroke)
    // The ball with the plus.
    val c = Offset((l + r) / 2f, (t + b) / 2f)
    val rad = min(r - l, b - t) * (0.11f + 0.012f * pulse + if (picked) 0.02f else 0f)
    drawCircle(Ink.line.copy(alpha = 0.35f), rad * 1.1f, Offset(c.x + rad * 0.08f, c.y + rad * 0.14f))
    drawCircle(if (picked) Color(0xFFFFC83D) else Color(0xFF3BC46B), rad, c)
    drawCircle(Color.White.copy(alpha = 0.35f), rad * 0.55f, Offset(c.x - rad * 0.25f, c.y - rad * 0.3f))
    drawCircle(Ink.line, rad, c, style = pen.stroke)
    val arm = rad * 0.5f
    drawLine(Color.White, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), strokeWidth = rad * 0.3f, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(c.x, c.y - arm), Offset(c.x, c.y + arm), strokeWidth = rad * 0.3f, cap = StrokeCap.Round)
}

/** A simple smooth bounce from 0 up past 1 and back to 1, for things that pop into place. */
internal fun popScale(p: Float): Float {
    val x = p.coerceIn(0f, 1f)
    if (x >= 1f) return 1f
    return 1f + 0.18f * sin(x * PI.toFloat()) * (1f - x) - (1f - x) * (1f - x) * 0.9f
}
