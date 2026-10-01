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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

/*
 * What lies above the ceiling. On a tall screen (a tablet) the scene shows a good way above the rooms. This file
 * fills that part the way a doll's house does, cut straight through: the beam of the ceiling, and above it the
 * attic with what a family keeps there, the feet of the furniture on the floor above, or the flat roof and the sky.
 * Someone is always at home up there (a mouse, a bat, a spider, a dust bunny, a cat on the roof): a doll's house is
 * funnier when the walls are lived in.
 *
 * Everything is drawn above scene height 0, where the rooms end, and only the part the screen shows. On a wide
 * phone that is a thin strip, so the work is skipped when less than the beam would show.
 */

/** How thick the ceiling's beam is, how high the attic under the roof, and how thick the roof. */
private const val SLAB = 0.05f
private const val ATTIC = 0.4f
private const val ROOF = 0.055f

/** Rafters stand this far apart; each pair frames one thing kept in the attic. */
private const val BAY = 1.1f

private val beamWood = Color(0xFFD9B487)
private val rafterWood = Color(0xFFBE8A5B)

/** Draws what is above the ceiling of [place], if it has an upstairs or a roof worth showing. */
internal fun DrawScope.cutaway(place: PlaceId, st: Stage, pen: Pen) {
    // The engine shifts the scene down so that height 1 is the bottom of the screen: this much shows above height 0.
    val above = st.h - st.u
    if (above < SLAB * st.u * 0.5f) return
    when (place) {
        PlaceId.HOME -> attic(st, pen, 11)
        PlaceId.CAFE -> attic(st, pen, 23)
        PlaceId.SALON -> attic(st, pen, 37)
        PlaceId.DOCTOR -> attic(st, pen, 41)
        // Above the upper floor of the big house is the attic where Sture the ghost lives: he peeks out now and then.
        PlaceId.MANOR_UPPER -> attic(st, pen, 53, ghost = true)
        PlaceId.SHOP -> flatRoof(st, pen)
        PlaceId.MANOR_GROUND -> floorAbove(st, pen, Color(0xFF4E9B86), Color(0xFFA9774B), Color(0xFFC0473C), 61)
        PlaceId.MANOR_CELLAR -> floorAbove(st, pen, Color(0xFFF1E3C4), Color(0xFF8A5A3A), Color(0xFFB23A3A), 67)
        else -> Unit
    }
}

/**
 * The top of one room of the child's own house, whose back wall runs from scene x [x0] to [x1]: the room upstairs
 * when there is one (in its [wall] and [trim] colours), otherwise the attic under a roof of the child's own [tile].
 */
internal fun DrawScope.cutawayRoom(st: Stage, pen: Pen, x0: Float, x1: Float, roomAbove: Boolean, wall: Color, trim: Color, tile: Color, salt: Int) {
    if (st.h - st.u < SLAB * st.u * 0.5f || !st.sees(x0, x1)) return
    clipRect(left = st.x(x0), top = st.backgroundTop - 2f, right = st.x(x1), bottom = pen.lw) {
        if (roomAbove) floorAbove(st, pen, wall, trim, Color(0xFFE08A5B), salt) else attic(st, pen, salt, tile = tile)
    }
}

// ------------------------------------------------------------------------------------------- shared parts

/** The ceiling cut through: a wooden beam with the ends of the joists showing, from height 0 up. */
private fun DrawScope.slab(st: Stage, pen: Pen, wood: Color = beamWood) {
    val u = st.u
    val y0 = -SLAB * u
    drawRect(Brush.verticalGradient(listOf(wood.lighten(0.14f), wood.darken(0.1f)), startY = y0, endY = 0f), Offset(0f, y0), Size(st.w, -y0))
    var x = floor(st.left / 0.22f) * 0.22f
    while (x < st.right) {
        val o = Offset(st.x(x), y0 + SLAB * u * 0.22f)
        val s = Size(0.05f * u, SLAB * u * 0.56f)
        drawRect(wood.darken(0.28f), o, s)
        drawRect(Ink.line, o, s, style = Stroke(pen.lw * 0.6f))
        x += 0.22f
    }
    drawLine(Ink.line, Offset(0f, y0), Offset(st.w, y0), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(0f, 0f), Offset(st.w, 0f), strokeWidth = pen.lw)
}

/** The sky from the top of the screen down to [bottom] pixels: day, grey weather or night with stars, and two clouds. */
private fun DrawScope.skyAbove(st: Stage, pen: Pen, bottom: Float, salt: Int) {
    val top = st.backgroundTop
    if (bottom <= top) return
    val u = st.u
    val n = pen.night
    val oc = overcast(pen)
    val hi = lerp(lerp(Color(0xFF69B4F0), Color(0xFF98A4BA), oc), Color(0xFF14123D), n)
    val lo = lerp(lerp(Color(0xFFCDEBFB), Color(0xFFCBD2DE), oc), Color(0xFF3A2F7A), n)
    drawRect(Brush.verticalGradient(listOf(hi, lo), startY = top, endY = bottom), Offset(0f, top), Size(st.w, bottom - top))
    if (n > 0.3f) {
        for (i in 0 until 16) {
            val p = Offset(hash01(i, salt) * st.w, top + hash01(i, salt + 1) * (bottom - top) * 0.85f)
            drawCircle(Color.White, u * 0.004f, p, alpha = (n - 0.3f) / 0.7f * (0.55f + 0.45f * sin(pen.t * 1.3f + i * 1.7f)))
        }
    }
    val span = st.w + 0.8f * u
    for (i in 0 until 2) {
        val x = wrap(hash01(i, salt + 2) * span + pen.t * u * 0.006f * (1 + i) - st.cam * u * 0.3f, span) - 0.4f * u
        val y = top + (bottom - top) * (0.3f + 0.28f * i)
        drawPath(cloudPath(x, y, u * 0.05f), lerp(Color.White, Color(0xFFB9C2D2), oc), alpha = 0.9f * (1f - 0.75f * n))
    }
}

/** A wooden beam from [a] to [b], [w] pixels thick, with its outline and a lighter upper edge. */
private fun DrawScope.beam(a: Offset, b: Offset, w: Float, pen: Pen, wood: Color = rafterWood) {
    drawLine(Ink.line, a, b, strokeWidth = w + pen.lw * 2f)
    drawLine(wood, a, b, strokeWidth = w)
    drawLine(wood.lighten(0.25f), Offset(a.x - w * 0.22f, a.y - w * 0.1f), Offset(b.x - w * 0.22f, b.y - w * 0.1f), strokeWidth = w * 0.18f)
}

// ------------------------------------------------------------------------------------------- the attic

/**
 * An attic under a pitched roof: a dark wooden back wall, pairs of rafters with a collar beam, and in each bay
 * one thing a family keeps (and somebody small who lives there). Above it the roof and the sky.
 */
private fun DrawScope.attic(st: Stage, pen: Pen, salt: Int, ghost: Boolean = false, tile: Color = Color(0xFFB5543F)) {
    val u = st.u
    val floorY = -SLAB * u
    val ridgeY = floorY - ATTIC * u
    val roofTop = ridgeY - ROOF * u
    skyAbove(st, pen, roofTop, salt)

    drawRect(Brush.verticalGradient(listOf(Color(0xFF583928), Color(0xFF8B6042)), startY = ridgeY, endY = floorY), Offset(0f, ridgeY), Size(st.w, floorY - ridgeY))
    val boards = ArrayList<Offset>(64)
    var bx = floor(st.left / 0.11f) * 0.11f
    while (bx < st.right) {
        boards.add(Offset(st.x(bx), ridgeY))
        boards.add(Offset(st.x(bx), floorY))
        bx += 0.11f
    }
    drawPoints(boards, PointMode.Lines, Color(0xFF45291D), strokeWidth = pen.lw * 0.7f, alpha = 0.55f)
    // Dust on the floor boards, and the soft dark under the roof.
    drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.28f), Color.Transparent), startY = ridgeY, endY = ridgeY + 0.12f * u), Offset(0f, ridgeY), Size(st.w, 0.12f * u))

    val first = floor(st.left / BAY).toInt()
    val last = floor(st.right / BAY).toInt()
    for (i in first..last) atticBay(st, pen, i, salt, floorY, ridgeY, ghost)

    // The roof: tiles seen edge on, with snow on top in winter.
    val tiles = lerp(tile, Pal.night, pen.night * 0.25f)
    drawRect(Brush.verticalGradient(listOf(tiles.lighten(0.12f), tiles.darken(0.12f)), startY = roofTop, endY = ridgeY), Offset(0f, roofTop), Size(st.w, ridgeY - roofTop))
    val ticks = ArrayList<Offset>(64)
    var tx = floor(st.left / 0.07f) * 0.07f
    while (tx < st.right) {
        ticks.add(Offset(st.x(tx), roofTop + ROOF * u * 0.3f))
        ticks.add(Offset(st.x(tx), ridgeY))
        tx += 0.07f
    }
    drawPoints(ticks, PointMode.Lines, tiles.darken(0.35f), strokeWidth = pen.lw * 0.6f, alpha = 0.7f)
    if (pen.season == Season.WINTER) drawRect(Pal.snow, Offset(0f, roofTop - 0.012f * u), Size(st.w, 0.02f * u))
    drawLine(Ink.line, Offset(0f, roofTop), Offset(st.w, roofTop), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(0f, ridgeY), Offset(st.w, ridgeY), strokeWidth = pen.lw)
    for (i in first..last) roofLife(st, pen, i, salt, roofTop)
    slab(st, pen)
}

private fun DrawScope.atticBay(st: Stage, pen: Pen, i: Int, salt: Int, floorY: Float, ridgeY: Float, ghost: Boolean) {
    val u = st.u
    val t = pen.t
    val x0 = st.x(i * BAY)
    val x1 = st.x((i + 1) * BAY)
    val xm = (x0 + x1) / 2f
    val bw = 0.034f * u
    val tieY = ridgeY + ATTIC * u * 0.36f
    val kind = (hash01(i, salt) * 6f).toInt().coerceIn(0, 5)
    val cx = xm + (hash01(i, salt + 1) - 0.5f) * 0.16f * u

    // What stands on the wall is behind the rafters: a round window, or a cobweb.
    if (kind == 2) roundWindow(Offset(cx, ridgeY + ATTIC * u * 0.62f), 0.085f * u, floorY, pen)
    if (i % 2 == 0) cobweb(Offset(x0 + bw, ridgeY), 0.16f * u, pen, t + i)

    // The bay's own thing, standing on the floor.
    when (kind) {
        0 -> boxes(cx, floorY, u, pen, ghost && i % 2 == 0, t + i)
        1 -> trunk(cx, floorY, u, pen)
        3 -> mouse(cx, floorY, u, pen, t + i * 1.9f)
        4 -> skis(cx, floorY, u, pen)
        else -> Unit
    }

    // The rafters, the collar beam between them and the post where two bays meet.
    val footL = Offset(x0 + 0.05f * u, floorY)
    val footR = Offset(x1 - 0.05f * u, floorY)
    val peak = Offset(xm, ridgeY + bw * 0.4f)
    val tieL = Offset(mix(footL.x, peak.x, 0.64f), tieY)
    val tieR = Offset(mix(footR.x, peak.x, 0.64f), tieY)
    beam(tieL, tieR, bw * 0.8f, pen)
    beam(footL, peak, bw, pen)
    beam(footR, peak, bw, pen)
    beam(Offset(x0, ridgeY), Offset(x0, floorY), bw, pen, rafterWood.darken(0.12f))

    // A bare bulb in some bays, a bat asleep in others.
    if (hash01(i, salt + 3) < 0.5f) {
        val b = Offset(xm, tieY + 0.07f * u)
        drawLine(Ink.line, Offset(xm, tieY), b, strokeWidth = pen.lw)
        drawCircle(safeRadialGradient(listOf(Color(0xFFFFE9A8).copy(alpha = 0.22f + 0.3f * pen.night), Color(0x00FFE9A8)), center = b, radius = 0.2f * u), 0.2f * u, b)
        inkedCircle(b, 0.016f * u, Color(0xFFFFE27A), pen, shade = false)
    } else if (kind == 5 || hash01(i, salt + 4) < 0.35f) {
        bat(Offset(mix(tieL.x, tieR.x, 0.3f), tieY + bw * 0.4f), u, pen, t + i * 2.3f)
    }
}

/** Three cardboard boxes, one with a face drawn on it. With [ghost], a small white ghost looks out from behind them. */
private fun DrawScope.boxes(cx: Float, fy: Float, u: Float, pen: Pen, ghost: Boolean, t: Float) {
    if (ghost) {
        // Sture peeks up, looks about and ducks again.
        val up = max(0f, sin(t * 0.5f)) * 0.09f * u
        val c = Offset(cx + 0.02f * u, fy - 0.2f * u - up)
        val body = Path().apply {
            moveTo(c.x - 0.045f * u, c.y + 0.12f * u)
            lineTo(c.x - 0.045f * u, c.y)
            quadraticTo(c.x - 0.045f * u, c.y - 0.05f * u, c.x, c.y - 0.05f * u)
            quadraticTo(c.x + 0.045f * u, c.y - 0.05f * u, c.x + 0.045f * u, c.y)
            lineTo(c.x + 0.045f * u, c.y + 0.12f * u)
            close()
        }
        inked(body, Color(0xFFF7FAFF), pen)
        val look = sin(t * 1.3f) * 0.006f * u
        drawCircle(Ink.line, 0.008f * u, Offset(c.x - 0.016f * u + look, c.y - 0.012f * u))
        drawCircle(Ink.line, 0.008f * u, Offset(c.x + 0.016f * u + look, c.y - 0.012f * u))
        drawOval(Ink.blush, Offset(c.x - 0.036f * u, c.y + 0.004f * u), Size(0.018f * u, 0.01f * u))
        drawOval(Ink.blush, Offset(c.x + 0.018f * u, c.y + 0.004f * u), Size(0.018f * u, 0.01f * u))
    }
    val card = Color(0xFFD6A772)
    box3d(Rect(cx - 0.14f * u, fy - 0.12f * u, cx + 0.03f * u, fy), 0.05f * u, card, pen)
    drawRect(Color(0xFFF2E3BE), Offset(cx - 0.07f * u, fy - 0.12f * u), Size(0.028f * u, 0.12f * u))
    box3d(Rect(cx - 0.1f * u, fy - 0.2f * u, cx + 0.0f * u, fy - 0.12f * u - pen.lw), 0.04f * u, card.darken(0.08f), pen)
    val face = Rect(cx + 0.07f * u, fy - 0.1f * u, cx + 0.2f * u, fy)
    box3d(face, 0.05f * u, card.lighten(0.1f), pen)
    // Somebody drew a happy face on the last box.
    drawCircle(Ink.line, 0.007f * u, Offset(face.center.x - 0.022f * u, face.center.y - 0.012f * u))
    drawCircle(Ink.line, 0.007f * u, Offset(face.center.x + 0.022f * u, face.center.y - 0.012f * u))
    drawArc(Ink.line, 20f, 140f, false, Offset(face.center.x - 0.025f * u, face.center.y - 0.012f * u), Size(0.05f * u, 0.035f * u), style = Stroke(pen.lw, cap = StrokeCap.Round))
}

/** An old travelling trunk with a rounded lid, straps and a brass lock. */
private fun DrawScope.trunk(cx: Float, fy: Float, u: Float, pen: Pen) {
    val w = 0.14f * u
    val lid = Path().apply {
        moveTo(cx - w, fy - 0.1f * u)
        quadraticTo(cx - w, fy - 0.18f * u, cx, fy - 0.18f * u)
        quadraticTo(cx + w, fy - 0.18f * u, cx + w, fy - 0.1f * u)
        close()
    }
    inked(lid, Color(0xFF4F86A8), pen)
    inkedRound(Rect(cx - w, fy - 0.1f * u, cx + w, fy), 0.012f * u, Color(0xFF3F6E8E), pen)
    for (s in floatArrayOf(-0.6f, 0.6f)) {
        drawRect(Color(0xFF8A5A3A), Offset(cx + s * w - 0.012f * u, fy - 0.165f * u), Size(0.024f * u, 0.165f * u))
        drawRect(Ink.line, Offset(cx + s * w - 0.012f * u, fy - 0.165f * u), Size(0.024f * u, 0.165f * u), style = Stroke(pen.lw * 0.6f))
    }
    inkedRound(Rect(cx - 0.02f * u, fy - 0.115f * u, cx + 0.02f * u, fy - 0.075f * u), 0.006f * u, Color(0xFFF2C14E), pen, shade = false)
    drawCircle(Ink.line, 0.005f * u, Offset(cx, fy - 0.092f * u))
}

/** A round attic window with the sky in it; by day a beam of light falls from it to the floor, by night the moon shows. */
private fun DrawScope.roundWindow(c: Offset, r: Float, fy: Float, pen: Pen) {
    val n = pen.night
    val oc = overcast(pen)
    val glass = lerp(lerp(Color(0xFF9BD6F7), Color(0xFFB9C3D2), oc), Color(0xFF2A2466), n)
    if (n < 0.9f) {
        val light = Path().apply { poly(c.x - r, c.y + r * 0.3f, c.x + r, c.y + r * 0.3f, c.x + r * 2.6f, fy, c.x - r * 0.4f, fy) }
        drawPath(light, Color(0xFFFFF3C4), alpha = 0.13f * (1f - n) * (1f - 0.6f * oc))
    }
    drawCircle(Color(0xFFF2E6D0), r * 1.2f, c)
    drawCircle(Ink.line, r * 1.2f, c, style = Stroke(pen.lw))
    drawCircle(glass, r, c)
    if (n > 0.4f) drawCircle(Color(0xFFFFF6D6), r * 0.3f, Offset(c.x + r * 0.25f, c.y - r * 0.2f), alpha = (n - 0.4f) / 0.6f)
    drawLine(Color(0xFFF2E6D0), Offset(c.x - r, c.y), Offset(c.x + r, c.y), strokeWidth = pen.lw * 2.2f)
    drawLine(Color(0xFFF2E6D0), Offset(c.x, c.y - r), Offset(c.x, c.y + r), strokeWidth = pen.lw * 2.2f)
    drawCircle(Ink.line, r, c, style = Stroke(pen.lw))
    shine(Offset(c.x - r * 0.45f, c.y - r * 0.45f), r * 0.2f, r * 0.12f, 0.7f)
}

/** A mouse looking out of its hole in the wall, with a piece of cheese waiting just out of reach. */
private fun DrawScope.mouse(cx: Float, fy: Float, u: Float, pen: Pen, t: Float) {
    val hole = Path().apply {
        moveTo(cx - 0.045f * u, fy)
        lineTo(cx - 0.045f * u, fy - 0.03f * u)
        quadraticTo(cx - 0.045f * u, fy - 0.075f * u, cx, fy - 0.075f * u)
        quadraticTo(cx + 0.045f * u, fy - 0.075f * u, cx + 0.045f * u, fy - 0.03f * u)
        lineTo(cx + 0.045f * u, fy)
        close()
    }
    drawPath(hole, Color(0xFF24160F))
    drawPath(hole, Ink.line, style = Stroke(pen.lw))
    // It leans out, sniffs, and pulls back.
    val out = (0.5f + 0.5f * sin(t * 0.8f)) * 0.03f * u
    val h = Offset(cx + 0.012f * u + out, fy - 0.03f * u)
    val grey = Color(0xFFB9B4C4)
    for (s in floatArrayOf(-1f, 1f)) {
        inkedCircle(Offset(h.x + s * 0.018f * u - 0.006f * u, h.y - 0.022f * u), 0.014f * u, grey, pen, shade = false)
        drawCircle(Color(0xFFF7B8C8), 0.008f * u, Offset(h.x + s * 0.018f * u - 0.006f * u, h.y - 0.022f * u))
    }
    inkedOval(Rect(h.x - 0.026f * u, h.y - 0.02f * u, h.x + 0.03f * u, h.y + 0.02f * u), grey, pen, shade = false)
    val blink = sin(t * 0.9f) > 0.97f
    if (blink) drawLine(Ink.line, Offset(h.x + 0.002f * u, h.y - 0.004f * u), Offset(h.x + 0.012f * u, h.y - 0.004f * u), strokeWidth = pen.lw)
    else drawCircle(Ink.line, 0.005f * u, Offset(h.x + 0.008f * u, h.y - 0.005f * u))
    drawCircle(Color(0xFFF07A9A), 0.005f * u, Offset(h.x + 0.03f * u, h.y + 0.002f * u))
    val twitch = sin(t * 9f) * 0.004f * u
    drawLine(Ink.line, Offset(h.x + 0.024f * u, h.y + 0.006f * u), Offset(h.x + 0.05f * u, h.y + twitch), strokeWidth = pen.lw * 0.5f)
    drawLine(Ink.line, Offset(h.x + 0.024f * u, h.y + 0.008f * u), Offset(h.x + 0.048f * u, h.y + 0.014f * u - twitch), strokeWidth = pen.lw * 0.5f)
    // The cheese.
    val k = cx + 0.15f * u
    val wedge = Path().apply { poly(k - 0.035f * u, fy, k + 0.035f * u, fy, k + 0.035f * u, fy - 0.04f * u) }
    inked(wedge, Color(0xFFFFD34D), pen, shade = false)
    drawCircle(Color(0xFFE2A92B), 0.006f * u, Offset(k + 0.012f * u, fy - 0.012f * u))
    drawCircle(Color(0xFFE2A92B), 0.004f * u, Offset(k + 0.024f * u, fy - 0.024f * u))
}

/** Skis, poles and a sledge put away for the summer. */
private fun DrawScope.skis(cx: Float, fy: Float, u: Float, pen: Pen) {
    for ((k, col) in listOf(Color(0xFFE5484D), Color(0xFF3E7BD6)).withIndex()) {
        val a = Offset(cx - 0.05f * u + k * 0.03f * u, fy)
        val b = Offset(cx + 0.03f * u + k * 0.03f * u, fy - 0.3f * u)
        drawLine(Ink.line, a, b, strokeWidth = 0.02f * u + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(col, a, b, strokeWidth = 0.02f * u, cap = StrokeCap.Round)
    }
    val p0 = Offset(cx + 0.08f * u, fy)
    val p1 = Offset(cx + 0.12f * u, fy - 0.24f * u)
    drawLine(Ink.line, p0, p1, strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
    drawCircle(Ink.line, 0.012f * u, Offset(p0.x + 0.004f * u, p0.y - 0.03f * u), style = Stroke(pen.lw))
    // The sledge.
    val sx = cx - 0.2f * u
    inkedRound(Rect(sx - 0.07f * u, fy - 0.055f * u, sx + 0.07f * u, fy - 0.035f * u), 0.006f * u, Color(0xFFD9A066), pen, shade = false)
    drawLine(Ink.line, Offset(sx - 0.05f * u, fy - 0.035f * u), Offset(sx - 0.05f * u, fy - 0.008f * u), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(sx + 0.05f * u, fy - 0.035f * u), Offset(sx + 0.05f * u, fy - 0.008f * u), strokeWidth = pen.lw)
    drawLine(Color(0xFFB23A3A), Offset(sx - 0.085f * u, fy - 0.006f * u), Offset(sx + 0.08f * u, fy - 0.006f * u), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
}

/** A bat asleep under the beam at [top]; it breathes, and by night its eyes are open. */
private fun DrawScope.bat(top: Offset, u: Float, pen: Pen, t: Float) {
    val breathe = 1f + 0.06f * sin(t * 1.6f)
    val col = Color(0xFF5B4A7A)
    val c = Offset(top.x, top.y + 0.032f * u)
    drawLine(Ink.line, top, Offset(top.x, top.y + 0.012f * u), strokeWidth = pen.lw * 1.4f)
    for (s in floatArrayOf(-1f, 1f)) {
        val wing = Path().apply { poly(c.x, c.y - 0.02f * u, c.x + s * 0.03f * u * breathe, c.y - 0.006f * u, c.x + s * 0.012f * u, c.y + 0.03f * u) }
        inked(wing, col.darken(0.15f), pen, shade = false)
    }
    inkedOval(Rect(c.x - 0.014f * u, c.y - 0.022f * u, c.x + 0.014f * u, c.y + 0.03f * u), col, pen, shade = false)
    // Hanging upside down: the ears point at the floor.
    for (s in floatArrayOf(-1f, 1f)) {
        val ear = Path().apply { poly(c.x + s * 0.004f * u, c.y + 0.026f * u, c.x + s * 0.014f * u, c.y + 0.026f * u, c.x + s * 0.012f * u, c.y + 0.045f * u) }
        inked(ear, col, pen, shade = false)
    }
    if (pen.night > 0.5f) {
        drawCircle(Color.White, 0.004f * u, Offset(c.x - 0.006f * u, c.y + 0.016f * u))
        drawCircle(Color.White, 0.004f * u, Offset(c.x + 0.006f * u, c.y + 0.016f * u))
    } else {
        drawLine(Ink.line, Offset(c.x - 0.009f * u, c.y + 0.016f * u), Offset(c.x - 0.003f * u, c.y + 0.016f * u), strokeWidth = pen.lw * 0.7f)
        drawLine(Ink.line, Offset(c.x + 0.003f * u, c.y + 0.016f * u), Offset(c.x + 0.009f * u, c.y + 0.016f * u), strokeWidth = pen.lw * 0.7f)
    }
}

/** A cobweb in the corner at [corner], and a spider going up and down on its thread. */
private fun DrawScope.cobweb(corner: Offset, r: Float, pen: Pen, t: Float) {
    val silk = Color(0xFFF4EFE6)
    for (k in 0..3) {
        val a = k / 3f
        drawLine(silk, corner, Offset(corner.x + r * (1f - a * 0.9f), corner.y + r * (0.1f + a * 0.9f)), strokeWidth = pen.lw * 0.5f, alpha = 0.7f)
    }
    for (ring in 1..3) {
        val f = ring / 3.4f
        drawArc(silk, 0f, 90f, false, Offset(corner.x - r * f, corner.y - r * f), Size(r * f * 2f, r * f * 2f), alpha = 0.6f, style = Stroke(pen.lw * 0.5f))
    }
    val x = corner.x + r * 0.75f
    val len = r * (0.55f + 0.3f * sin(t * 0.7f))
    drawLine(silk, Offset(x, corner.y), Offset(x, corner.y + len), strokeWidth = pen.lw * 0.5f, alpha = 0.8f)
    val s = Offset(x, corner.y + len)
    for (k in -1..1) {
        drawLine(Ink.line, s, Offset(s.x - r * 0.1f, s.y + k * r * 0.05f), strokeWidth = pen.lw * 0.6f)
        drawLine(Ink.line, s, Offset(s.x + r * 0.1f, s.y + k * r * 0.05f), strokeWidth = pen.lw * 0.6f)
    }
    drawCircle(Ink.line, r * 0.065f, s)
    drawCircle(Color.White, r * 0.02f, Offset(s.x - r * 0.025f, s.y - r * 0.01f))
    drawCircle(Color.White, r * 0.02f, Offset(s.x + r * 0.025f, s.y - r * 0.01f))
}

/** What stands on the roof over bay [i]: a chimney with smoke, a cat keeping watch, or two small birds. */
private fun DrawScope.roofLife(st: Stage, pen: Pen, i: Int, salt: Int, roofTop: Float) {
    val u = st.u
    val t = pen.t
    val x = st.x(i * BAY + 0.25f + 0.5f * hash01(i, salt + 7))
    when (((i % 4) + 4) % 4) {
        1 -> {
            val r = Rect(x - 0.04f * u, roofTop - 0.13f * u, x + 0.04f * u, roofTop)
            drawRect(Color(0xFFB5604A), r.topLeft, r.size)
            drawRect(Ink.line, r.topLeft, r.size, style = Stroke(pen.lw))
            inkedRound(Rect(r.left - 0.01f * u, r.top - 0.02f * u, r.right + 0.01f * u, r.top), 0.004f * u, Color(0xFF7A6B66), pen, shade = false)
            for (k in 0 until 3) {
                val p = wrap(t * 0.25f + k / 3f, 1f)
                drawCircle(Color.White, (0.018f + 0.03f * p) * u, Offset(x + p * 0.05f * u, r.top - 0.03f * u - p * 0.16f * u), alpha = 0.55f * (1f - p) * (1f - 0.5f * pen.night))
            }
        }
        2 -> {
            // A cat on the roof, tail swishing; its eyes shine at night.
            val fur = Color(0xFF4A4458)
            val tail = Path().apply {
                moveTo(x + 0.03f * u, roofTop - 0.012f * u)
                quadraticTo(x + 0.08f * u, roofTop - 0.02f * u, x + 0.075f * u + sin(t * 1.4f) * 0.015f * u, roofTop - 0.07f * u)
            }
            drawPath(tail, Ink.line, style = Stroke(0.014f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(tail, fur, style = Stroke(0.014f * u, cap = StrokeCap.Round))
            inkedOval(Rect(x - 0.035f * u, roofTop - 0.075f * u, x + 0.035f * u, roofTop), fur, pen, shade = false)
            val hc = Offset(x - 0.01f * u, roofTop - 0.09f * u)
            for (s in floatArrayOf(-1f, 1f)) {
                val ear = Path().apply { poly(hc.x + s * 0.008f * u, hc.y - 0.02f * u, hc.x + s * 0.03f * u, hc.y - 0.012f * u, hc.x + s * 0.024f * u, hc.y - 0.045f * u) }
                inked(ear, fur, pen, shade = false)
            }
            inkedCircle(hc, 0.03f * u, fur, pen, shade = false)
            val eye = if (pen.night > 0.4f) Color(0xFFFFE27A) else Color.White
            drawCircle(eye, 0.006f * u, Offset(hc.x - 0.011f * u, hc.y - 0.002f * u))
            drawCircle(eye, 0.006f * u, Offset(hc.x + 0.011f * u, hc.y - 0.002f * u))
            drawCircle(Ink.line, 0.003f * u, Offset(hc.x - 0.011f * u, hc.y - 0.002f * u))
            drawCircle(Ink.line, 0.003f * u, Offset(hc.x + 0.011f * u, hc.y - 0.002f * u))
        }
        0 -> if (pen.night < 0.5f) {
            // Two small birds that hop now and then.
            for (k in 0 until 2) {
                val hop = max(0f, sin(t * 2.2f + k * 1.7f + i)) * 0.012f * u
                val c = Offset(x + k * 0.07f * u, roofTop - 0.02f * u - hop)
                inkedOval(Rect(c.x - 0.02f * u, c.y - 0.016f * u, c.x + 0.02f * u, c.y + 0.016f * u), if (k == 0) Color(0xFFE8734A) else Color(0xFF6FA8DC), pen, shade = false)
                val beak = Path().apply { poly(c.x - 0.02f * u, c.y - 0.006f * u, c.x - 0.034f * u, c.y - 0.001f * u, c.x - 0.02f * u, c.y + 0.004f * u) }
                drawPath(beak, Color(0xFFFFC83D))
                drawCircle(Ink.line, 0.003f * u, Offset(c.x - 0.009f * u, c.y - 0.006f * u))
            }
        }
        else -> Unit
    }
}

// ------------------------------------------------------------------------------------------- the floor above

/**
 * A floor above this one: between the two a hollow with a pipe, a mouse on the run and dust bunnies, and over it
 * the room upstairs as far as the screen shows, which is the legs of its furniture. [wall], [trim] and [rug] are
 * the colours of the room upstairs.
 */
private fun DrawScope.floorAbove(st: Stage, pen: Pen, wall: Color, trim: Color, rug: Color, salt: Int) {
    val u = st.u
    val t = pen.t
    val gap = 0.1f * u
    val floorY = -gap
    // The room upstairs: its wall, its skirting board and what stands on its floor.
    drawRect(Brush.verticalGradient(listOf(wall.darken(0.12f), wall), startY = st.backgroundTop, endY = floorY), Offset(0f, st.backgroundTop), Size(st.w, floorY - st.backgroundTop))
    drawRect(trim, Offset(0f, floorY - 0.03f * u), Size(st.w, 0.03f * u))
    drawLine(Ink.line, Offset(0f, floorY - 0.03f * u), Offset(st.w, floorY - 0.03f * u), strokeWidth = pen.lw * 0.7f)
    val first = floor(st.left / BAY).toInt()
    val last = floor(st.right / BAY).toInt()
    for (i in first..last) {
        val cx = st.x(i * BAY + 0.3f + 0.5f * hash01(i, salt + 1))
        when ((hash01(i, salt) * 5f).toInt().coerceIn(0, 4)) {
            0 -> bedLegs(cx, floorY, u, pen, rug)
            1 -> tableLegs(cx, floorY, u, pen)
            2 -> toys(cx, floorY, u, pen)
            3 -> sleepingCat(cx, floorY, u, pen, rug, t + i)
            else -> dresser(cx, floorY, u, pen, trim)
        }
    }
    // The hollow between the floors.
    drawRect(Color(0xFF2A1B14), Offset(0f, floorY), Size(st.w, gap))
    drawLine(Color(0xFF8E97A6), Offset(0f, -gap * 0.62f), Offset(st.w, -gap * 0.62f), strokeWidth = gap * 0.16f)
    drawLine(Color(0xFFC3CAD6), Offset(0f, -gap * 0.66f), Offset(st.w, -gap * 0.66f), strokeWidth = gap * 0.04f)
    // Dust bunnies, which here really are bunnies made of dust.
    val firstD = floor(st.left / 0.7f).toInt()
    val lastD = floor(st.right / 0.7f).toInt()
    for (i in firstD..lastD) {
        if (hash01(i, salt + 5) < 0.45f) continue
        val c = Offset(st.x(i * 0.7f + 0.2f + 0.3f * hash01(i, salt + 6)), -gap * 0.3f)
        val fluff = Color(0xFFB7AFA6)
        val r = gap * 0.16f
        drawOval(fluff, Offset(c.x - r * 0.75f, c.y - r * 2.3f), Size(r * 0.5f, r * 1.6f))
        drawOval(fluff, Offset(c.x + r * 0.2f, c.y - r * 2.3f), Size(r * 0.5f, r * 1.6f))
        drawCircle(fluff, r * 1.15f, c)
        drawCircle(fluff, r * 0.8f, Offset(c.x + r * 1.1f, c.y + r * 0.2f))
        drawCircle(Ink.line, r * 0.16f, Offset(c.x - r * 0.35f, c.y - r * 0.15f))
        drawCircle(Ink.line, r * 0.16f, Offset(c.x + r * 0.35f, c.y - r * 0.15f))
    }
    // A mouse on the run through the hollow, behind the joists.
    val span = st.w + 0.6f * u
    val mx = wrap(t * 0.22f * u + salt * 37f, span) - 0.3f * u
    val my = -gap * 0.28f - abs(sin(t * 11f)) * gap * 0.06f
    val grey = Color(0xFFB9B4C4)
    drawLine(Color(0xFFF7B8C8), Offset(mx - gap * 0.3f, my), Offset(mx - gap * 0.62f, my - gap * 0.1f), strokeWidth = pen.lw, cap = StrokeCap.Round)
    drawOval(grey, Offset(mx - gap * 0.3f, my - gap * 0.16f), Size(gap * 0.5f, gap * 0.32f))
    drawCircle(grey, gap * 0.1f, Offset(mx + gap * 0.08f, my - gap * 0.16f))
    drawCircle(Ink.line, gap * 0.035f, Offset(mx + gap * 0.14f, my - gap * 0.04f))
    // The joists, and the boards above and below them.
    var x = floor(st.left / 0.45f) * 0.45f
    while (x < st.right) {
        val o = Offset(st.x(x), floorY)
        drawRect(beamWood.darken(0.18f), o, Size(0.04f * u, gap))
        drawRect(Ink.line, o, Size(0.04f * u, gap), style = Stroke(pen.lw * 0.7f))
        x += 0.45f
    }
    drawRect(beamWood, Offset(0f, floorY), Size(st.w, gap * 0.2f))
    drawRect(beamWood, Offset(0f, -gap * 0.2f), Size(st.w, gap * 0.2f))
    for (y in floatArrayOf(floorY, floorY + gap * 0.2f, -gap * 0.2f, 0f)) drawLine(Ink.line, Offset(0f, y), Offset(st.w, y), strokeWidth = pen.lw * 0.8f)
}

private fun DrawScope.leg(x: Float, fy: Float, h: Float, w: Float, pen: Pen, wood: Color) {
    drawRect(wood, Offset(x - w / 2f, fy - h), Size(w, h))
    drawRect(Ink.line, Offset(x - w / 2f, fy - h), Size(w, h), style = Stroke(pen.lw * 0.7f))
}

/** The lower half of a bed: legs, the rail, a blanket hanging over the side, and a pair of slippers. */
private fun DrawScope.bedLegs(cx: Float, fy: Float, u: Float, pen: Pen, blanket: Color) {
    val wood = Color(0xFFC98A55)
    leg(cx - 0.2f * u, fy, 0.07f * u, 0.026f * u, pen, wood)
    leg(cx + 0.2f * u, fy, 0.07f * u, 0.026f * u, pen, wood)
    inkedRound(Rect(cx - 0.23f * u, fy - 0.11f * u, cx + 0.23f * u, fy - 0.065f * u), 0.008f * u, wood, pen, shade = false)
    inkedRound(Rect(cx - 0.22f * u, fy - 0.17f * u, cx + 0.22f * u, fy - 0.11f * u), 0.02f * u, Color(0xFFF7F3EC), pen, shade = false)
    val drape = Path().apply {
        moveTo(cx - 0.12f * u, fy - 0.17f * u)
        lineTo(cx + 0.22f * u, fy - 0.17f * u)
        lineTo(cx + 0.22f * u, fy - 0.07f * u)
        for (k in 0 until 6) {
            val x0 = cx + 0.22f * u - k * 0.057f * u
            quadraticTo(x0 - 0.028f * u, fy - 0.035f * u, x0 - 0.057f * u, fy - 0.07f * u)
        }
        close()
    }
    inked(drape, blanket, pen)
    for (k in 0 until 2) inkedOval(Rect(cx - 0.34f * u + k * 0.06f * u, fy - 0.025f * u, cx - 0.29f * u + k * 0.06f * u, fy - 0.003f * u), Color(0xFF6FA8DC), pen, shade = false)
}

/** A table and a chair, seen up to the seat: four legs, a cloth hanging down and a ball that rolled under. */
private fun DrawScope.tableLegs(cx: Float, fy: Float, u: Float, pen: Pen) {
    val wood = Color(0xFFB07A4C)
    leg(cx - 0.15f * u, fy, 0.2f * u, 0.022f * u, pen, wood)
    leg(cx + 0.15f * u, fy, 0.2f * u, 0.022f * u, pen, wood)
    val cloth = Path().apply {
        moveTo(cx - 0.2f * u, fy - 0.22f * u)
        lineTo(cx + 0.2f * u, fy - 0.22f * u)
        lineTo(cx + 0.2f * u, fy - 0.15f * u)
        for (k in 0 until 5) {
            val x0 = cx + 0.2f * u - k * 0.08f * u
            lineTo(x0 - 0.04f * u, fy - 0.12f * u)
            lineTo(x0 - 0.08f * u, fy - 0.15f * u)
        }
        close()
    }
    inked(cloth, Color(0xFFF7F3EC), pen)
    drawRect(Color(0xFFE5484D), Offset(cx - 0.2f * u, fy - 0.205f * u), Size(0.4f * u, 0.012f * u))
    // The chair beside it.
    val chair = Color(0xFF6FB7A8)
    leg(cx + 0.29f * u, fy, 0.11f * u, 0.018f * u, pen, chair)
    leg(cx + 0.39f * u, fy, 0.11f * u, 0.018f * u, pen, chair)
    inkedRound(Rect(cx + 0.27f * u, fy - 0.13f * u, cx + 0.41f * u, fy - 0.105f * u), 0.006f * u, chair, pen, shade = false)
    leg(cx + 0.395f * u, fy - 0.13f * u, 0.13f * u, 0.018f * u, pen, chair)
    inkedCircle(Offset(cx - 0.02f * u, fy - 0.03f * u), 0.03f * u, Color(0xFFFFC83D), pen)
    drawArc(Color(0xFFE5484D), 200f, 140f, false, Offset(cx - 0.05f * u, fy - 0.05f * u), Size(0.06f * u, 0.04f * u), style = Stroke(pen.lw * 1.6f))
}

/** Toys left on the floor: a tower of blocks, a striped ball and a little train. */
private fun DrawScope.toys(cx: Float, fy: Float, u: Float, pen: Pen) {
    val cols = listOf(Color(0xFFE5484D), Color(0xFF3E7BD6), Color(0xFFFFC83D), Color(0xFF6BCB77))
    for (k in 0 until 4) {
        val s = 0.05f * u
        val x = cx - 0.2f * u + (if (k % 2 == 0) 0f else 0.012f * u)
        inkedRound(Rect(x, fy - (k + 1) * s, x + s, fy - k * s), 0.006f * u, cols[k], pen, shade = false)
    }
    inkedCircle(Offset(cx - 0.02f * u, fy - 0.045f * u), 0.045f * u, Color(0xFFF7F3EC), pen)
    drawArc(Color(0xFFE5484D), -60f, 120f, true, Offset(cx - 0.065f * u, fy - 0.09f * u), Size(0.09f * u, 0.09f * u), alpha = 0.9f)
    drawCircle(Ink.line, 0.045f * u, Offset(cx - 0.02f * u, fy - 0.045f * u), style = Stroke(pen.lw))
    // The train: an engine and a wagon.
    for (k in 0 until 2) {
        val x = cx + 0.1f * u + k * 0.1f * u
        inkedRound(Rect(x, fy - 0.06f * u, x + 0.085f * u, fy - 0.018f * u), 0.008f * u, if (k == 0) Color(0xFF2EC4B6) else Color(0xFFFF8FB8), pen, shade = false)
        inkedCircle(Offset(x + 0.02f * u, fy - 0.014f * u), 0.014f * u, Color(0xFF3B3346), pen, shade = false)
        inkedCircle(Offset(x + 0.065f * u, fy - 0.014f * u), 0.014f * u, Color(0xFF3B3346), pen, shade = false)
    }
    inkedRound(Rect(cx + 0.115f * u, fy - 0.1f * u, cx + 0.14f * u, fy - 0.06f * u), 0.004f * u, Color(0xFF3B3346), pen, shade = false)
}

/** A cat asleep on a round rug, breathing slowly. */
private fun DrawScope.sleepingCat(cx: Float, fy: Float, u: Float, pen: Pen, rug: Color, t: Float) {
    inkedOval(Rect(cx - 0.2f * u, fy - 0.022f * u, cx + 0.2f * u, fy + 0.004f * u), rug, pen, shade = false)
    val fur = Color(0xFFF2A65A)
    val breathe = 0.004f * u * sin(t * 1.4f)
    val tail = Path().apply {
        moveTo(cx + 0.07f * u, fy - 0.03f * u)
        quadraticTo(cx + 0.15f * u, fy - 0.02f * u, cx + 0.02f * u, fy - 0.012f * u)
    }
    drawPath(tail, Ink.line, style = Stroke(0.02f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(tail, fur, style = Stroke(0.02f * u, cap = StrokeCap.Round))
    inkedOval(Rect(cx - 0.09f * u, fy - 0.085f * u - breathe, cx + 0.09f * u, fy - 0.012f * u), fur, pen)
    val hc = Offset(cx - 0.075f * u, fy - 0.05f * u)
    for (s in floatArrayOf(-1f, 1f)) {
        val ear = Path().apply { poly(hc.x + s * 0.008f * u, hc.y - 0.028f * u, hc.x + s * 0.034f * u, hc.y - 0.016f * u, hc.x + s * 0.028f * u, hc.y - 0.055f * u) }
        inked(ear, fur, pen, shade = false)
    }
    inkedCircle(hc, 0.038f * u, fur, pen, shade = false)
    drawArc(Ink.line, 20f, 140f, false, Offset(hc.x - 0.024f * u, hc.y - 0.012f * u), Size(0.016f * u, 0.012f * u), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    drawArc(Ink.line, 20f, 140f, false, Offset(hc.x + 0.006f * u, hc.y - 0.012f * u), Size(0.016f * u, 0.012f * u), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
}

/** A chest of drawers with round knobs, and a plant in a pot beside it. */
private fun DrawScope.dresser(cx: Float, fy: Float, u: Float, pen: Pen, wood: Color) {
    val r = Rect(cx - 0.17f * u, fy - 0.3f * u, cx + 0.12f * u, fy - 0.02f * u)
    leg(r.left + 0.03f * u, fy, 0.02f * u, 0.02f * u, pen, wood.darken(0.2f))
    leg(r.right - 0.03f * u, fy, 0.02f * u, 0.02f * u, pen, wood.darken(0.2f))
    inkedRound(r, 0.012f * u, wood.lighten(0.1f), pen)
    for (k in 0 until 3) {
        val y = r.top + (k + 0.5f) * r.height / 3f
        if (k > 0) drawLine(Ink.line, Offset(r.left, r.top + k * r.height / 3f), Offset(r.right, r.top + k * r.height / 3f), strokeWidth = pen.lw * 0.7f)
        drawCircle(Color(0xFFF2C14E), 0.009f * u, Offset(r.center.x - 0.05f * u, y))
        drawCircle(Color(0xFFF2C14E), 0.009f * u, Offset(r.center.x + 0.05f * u, y))
    }
    val px = cx + 0.24f * u
    for (k in -2..2) {
        val leaf = Path().apply {
            moveTo(px, fy - 0.09f * u)
            quadraticTo(px + k * 0.05f * u, fy - 0.2f * u, px + k * 0.07f * u, fy - 0.27f * u + abs(k) * 0.03f * u)
            quadraticTo(px + k * 0.02f * u, fy - 0.17f * u, px, fy - 0.09f * u)
            close()
        }
        inked(leaf, Color(0xFF5DB04F), pen, shade = false)
    }
    val pot = Path().apply { poly(px - 0.045f * u, fy - 0.1f * u, px + 0.045f * u, fy - 0.1f * u, px + 0.033f * u, fy, px - 0.033f * u, fy) }
    inked(pot, Color(0xFFD9825B), pen)
}

// ------------------------------------------------------------------------------------------- a flat roof

/** A flat roof under the open sky: a low wall round it, a fan that turns, a skylight and two pigeons. */
private fun DrawScope.flatRoof(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val roofY = -SLAB * u - 0.05f * u
    skyAbove(st, pen, roofY, 71)
    val first = floor(st.left / 1.3f).toInt()
    val last = floor(st.right / 1.3f).toInt()
    for (i in first..last) {
        val x = st.x(i * 1.3f + 0.3f + 0.5f * hash01(i, 73))
        when (((i % 3) + 3) % 3) {
            0 -> {
                // The fan of the cooling unit.
                val r = Rect(x - 0.09f * u, roofY - 0.11f * u, x + 0.09f * u, roofY)
                box3d(r, 0.05f * u, Color(0xFFD5D9E0), pen)
                val c = Offset(r.center.x, r.center.y)
                drawCircle(Color(0xFF6E7785), 0.04f * u, c)
                for (k in 0 until 3) {
                    val a = t * 4f + k * 2.094f
                    drawLine(Color(0xFFE9EDF3), c, Offset(c.x + kotlin.math.cos(a) * 0.035f * u, c.y + sin(a) * 0.035f * u), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
                }
                drawCircle(Ink.line, 0.04f * u, c, style = Stroke(pen.lw))
            }
            1 -> {
                // A skylight: light from the shop glows in it at night.
                val dome = Path().apply {
                    moveTo(x - 0.12f * u, roofY)
                    quadraticTo(x - 0.12f * u, roofY - 0.09f * u, x, roofY - 0.09f * u)
                    quadraticTo(x + 0.12f * u, roofY - 0.09f * u, x + 0.12f * u, roofY)
                    close()
                }
                inked(dome, lerp(Color(0xFFBFE6F5), Color(0xFFFFE9A0), pen.night), pen, shade = false)
                drawLine(Ink.line, Offset(x, roofY - 0.09f * u), Offset(x, roofY), strokeWidth = pen.lw * 0.7f)
                shine(Offset(x - 0.06f * u, roofY - 0.055f * u), 0.03f * u, 0.015f * u, 0.7f)
            }
            else -> for (k in 0 until 2) {
                if (k == 0) {
                    // The shop's sign from behind the low wall: a basket on a round board, on two posts.
                    val sc = Offset(x - 0.32f * u, roofY - 0.3f * u)
                    for (s in floatArrayOf(-1f, 1f)) drawLine(Ink.line, Offset(sc.x + s * 0.07f * u, roofY), Offset(sc.x + s * 0.07f * u, sc.y + 0.08f * u), strokeWidth = pen.lw * 2.4f)
                    inkedCircle(sc, 0.12f * u, Color.White, pen)
                    val basket = Path().apply { poly(sc.x - 0.07f * u, sc.y - 0.01f * u, sc.x + 0.07f * u, sc.y - 0.01f * u, sc.x + 0.05f * u, sc.y + 0.06f * u, sc.x - 0.05f * u, sc.y + 0.06f * u) }
                    inked(basket, Color(0xFF3E7BD6), pen, shade = false)
                    drawArc(Ink.line, 180f, 180f, false, Offset(sc.x - 0.045f * u, sc.y - 0.07f * u), Size(0.09f * u, 0.11f * u), style = Stroke(pen.lw * 1.6f))
                }
                // Pigeons nodding as they walk.
                val nod = sin(t * 3f + k * 2f) * 0.006f * u
                val c = Offset(x + k * 0.09f * u, roofY - 0.035f * u)
                inkedOval(Rect(c.x - 0.03f * u, c.y - 0.022f * u, c.x + 0.03f * u, c.y + 0.022f * u), Color(0xFF9AA3B5), pen, shade = false)
                inkedCircle(Offset(c.x - 0.03f * u + nod, c.y - 0.03f * u), 0.015f * u, Color(0xFF7D879B), pen, shade = false)
                drawCircle(Ink.line, 0.003f * u, Offset(c.x - 0.034f * u + nod, c.y - 0.033f * u))
                drawLine(Color(0xFFFFC83D), Offset(c.x - 0.044f * u + nod, c.y - 0.028f * u), Offset(c.x - 0.054f * u + nod, c.y - 0.026f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
                drawLine(Color(0xFFE5745A), Offset(c.x - 0.005f * u, c.y + 0.022f * u), Offset(c.x - 0.005f * u, roofY), strokeWidth = pen.lw)
            }
        }
    }
    // The low wall round the roof, with the shop's red band.
    drawRect(Color(0xFFF3E7CF), Offset(0f, roofY), Size(st.w, -SLAB * u - roofY))
    drawRect(Color(0xFFE8574A), Offset(0f, roofY + 0.012f * u), Size(st.w, 0.016f * u))
    drawLine(Ink.line, Offset(0f, roofY), Offset(st.w, roofY), strokeWidth = pen.lw)
    slab(st, pen, Color(0xFFD5D9E0))
}
