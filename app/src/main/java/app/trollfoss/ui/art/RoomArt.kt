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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Indoor places as cut-away boxes in oblique 3D: the back wall stands at `place.back` and rises to the
 * top of the screen, the floor band recedes from the front edge to the wall, and the cut floor slab shows
 * below the front edge. Everyday, modern rooms.
 */

// ----------------------------------------------------------------------------------- shared kit

/** The chosen wallpaper of room [i], or 0 for the place's own look. */
internal fun List<RoomStyle>.wallOf(i: Int): Int = getOrNull(i)?.wall ?: 0

/** The chosen floor of room [i], or 0 for the place's own look. */
internal fun List<RoomStyle>.floorOf(i: Int): Int = getOrNull(i)?.floor ?: 0

/** Papers the back wall from scene x [x0] to [x1] (and all the way up) with wallpaper [index]. */
internal fun DrawScope.paperWall(st: Stage, index: Int, back: Float, x0: Float = -9f, x1: Float = 99f) {
    val u = st.u
    drawWallpaper(index, max(st.x(x0), -2f), SKY_TOP * u - 2f, min(st.x(x1), st.w + 2f), back * u, -st.cam * u, 0f, u)
}

/** Lays flooring [index] on the floor band between front-edge x [x0] and [x1], receding to [back]. */
internal fun DrawScope.layFloor(st: Stage, index: Int, back: Float, x0: Float = -9f, x1: Float = 99f) {
    val u = st.u
    val a = max(x0, st.cam - 1f)
    val b = min(x1, st.cam + st.vw + 1f)
    if (b <= a) return
    val area = Path().apply { floorQuad(u, st.cam, a, b, FRONT_Y, back) }
    drawFlooring(index, area, -st.cam * u, 0f, u, -2f, st.w + 2f)
}

internal class Planks(val joints: List<Offset>, val ends: List<Offset>, val grain: List<Offset>)

/** Floor boards running back into the picture every [step] units, in scene pixels. */
internal fun buildPlanks(u: Float, x0: Float, x1: Float, back: Float, step: Float, salt: Int): Planks {
    val shift = recede(back)
    val joints = ArrayList<Offset>()
    val ends = ArrayList<Offset>()
    val grain = ArrayList<Offset>()
    var i = 0
    var x = x0 - shift
    while (x <= x1) {
        joints.add(Offset(x * u, FRONT_Y * u))
        joints.add(Offset((x + shift) * u, back * u))
        for (k in 0 until 2) {
            val f = 0.1f + 0.4f * k + 0.3f * hash01(i * 2 + k, salt)
            val y = mix(FRONT_Y, back, f)
            val sh = recede(y)
            ends.add(Offset((x + sh) * u, y * u))
            ends.add(Offset((x + step + sh) * u, y * u))
        }
        for (k in 0 until 2) {
            val gx = x + step * (0.25f + 0.5f * hash01(i * 3 + k, salt + 1))
            val f0 = 0.05f + hash01(i * 5 + k, salt + 2) * 0.7f
            val f1 = min(0.98f, f0 + 0.12f + 0.1f * hash01(i * 7 + k, salt + 3))
            val y0 = mix(FRONT_Y, back, f0)
            val y1 = mix(FRONT_Y, back, f1)
            grain.add(Offset((gx + recede(y0)) * u, y0 * u))
            grain.add(Offset((gx + recede(y1)) * u, y1 * u))
        }
        x += step
        i++
    }
    return Planks(joints, ends, grain)
}

/** A wooden floor band with soft light falling off toward the back wall. */
internal fun DrawScope.plankFloor(st: Stage, pen: Pen, planks: Planks, back: Float, color: Color) {
    val u = st.u
    drawRect(
        Brush.verticalGradient(0f to color.darken(0.22f), 0.3f to color.darken(0.05f), 1f to color.lighten(0.08f), startY = back * u, endY = FRONT_Y * u),
        Offset(0f, back * u),
        Size(st.w, (FRONT_Y - back) * u),
    )
    inScene(st) {
        drawPoints(planks.grain, PointMode.Lines, color.darken(0.14f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
        drawPoints(planks.joints, PointMode.Lines, color.darken(0.32f), strokeWidth = pen.lw * 0.75f)
        drawPoints(planks.ends, PointMode.Lines, color.darken(0.32f), strokeWidth = pen.lw * 0.75f)
    }
}

/** The soft shadow where the floor meets the back wall. */
internal fun DrawScope.wallShadow(st: Stage, back: Float) {
    val u = st.u
    drawRect(
        Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.28f), Ink.line.copy(alpha = 0f)), startY = back * u, endY = (back + 0.045f) * u),
        Offset(0f, back * u),
        Size(st.w, 0.045f * u),
    )
}

internal fun DrawScope.skirting(st: Stage, pen: Pen, back: Float, color: Color) {
    val u = st.u
    drawRect(color, Offset(0f, (back - 0.024f) * u), Size(st.w, 0.024f * u))
    drawLine(color.lighten(0.4f), Offset(0f, (back - 0.021f) * u), Offset(st.w, (back - 0.021f) * u), strokeWidth = pen.lw * 0.8f)
    drawLine(Ink.line, Offset(0f, (back - 0.024f) * u), Offset(st.w, (back - 0.024f) * u), strokeWidth = pen.lw * 0.8f)
    drawLine(Ink.line, Offset(0f, back * u), Offset(st.w, back * u), strokeWidth = pen.lw)
}

/** A crown moulding near the top of the scene, with the wall above it fading a little into shade. */
internal fun DrawScope.crown(st: Stage, pen: Pen, color: Color) {
    val u = st.u
    drawRect(
        Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.16f), Ink.line.copy(alpha = 0f)), startY = SKY_TOP * u, endY = 0f),
        Offset(0f, SKY_TOP * u),
        Size(st.w, -SKY_TOP * u),
    )
    drawRect(color, Offset(0f, 0f), Size(st.w, 0.02f * u))
    drawLine(color.lighten(0.5f), Offset(0f, 0.004f * u), Offset(st.w, 0.004f * u), strokeWidth = pen.lw)
    drawLine(Ink.line, Offset(0f, 0f), Offset(st.w, 0f), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(0f, 0.02f * u), Offset(st.w, 0.02f * u), strokeWidth = pen.lw * 0.7f)
    drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.1f), Ink.line.copy(alpha = 0f)), startY = 0.02f * u, endY = 0.06f * u), Offset(0f, 0.02f * u), Size(st.w, 0.04f * u))
}

/** A sunny patch on the floor, as if light fell in through a window at [x] on the back wall. */
internal fun DrawScope.lightPatch(st: Stage, x: Float, w: Float, back: Float, night: Float) {
    if (night >= 1f || !st.sees(x - 0.4f, x + 0.3f)) return
    val p = Path().apply { floorQuad(st.u, st.cam, x - w / 2f - 0.15f, x + w / 2f - 0.15f, back + 0.11f, back + 0.012f) }
    drawPath(p, Color(0xFFFFF6D6), alpha = 0.22f * (1f - night))
}

/** A cord and a simple lampshade hanging from the top, with a warm glow under it. */
internal fun DrawScope.pendant(st: Stage, pen: Pen, x: Float, y: Float, color: Color) {
    if (!st.sees(x - 0.2f, x + 0.2f)) return
    val u = st.u
    val c = st.o(x, y)
    drawLine(Ink.line, Offset(c.x, SKY_TOP * u), c, strokeWidth = pen.lw)
    val w = 0.1f * u
    val h = 0.05f * u
    val glowAt = Offset(c.x, c.y + h)
    drawCircle(Brush.radialGradient(listOf(Color(0x40FFE9A8), Color(0x00FFE9A8)), center = glowAt, radius = 0.2f * u), 0.2f * u, glowAt)
    val shade = Path().apply {
        moveTo(c.x - w * 0.16f, c.y)
        lineTo(c.x + w * 0.16f, c.y)
        quadraticTo(c.x + w * 0.5f, c.y + h * 0.25f, c.x + w * 0.5f, c.y + h)
        lineTo(c.x - w * 0.5f, c.y + h)
        quadraticTo(c.x - w * 0.5f, c.y + h * 0.25f, c.x - w * 0.16f, c.y)
        close()
    }
    drawOval(Color(0xFFFFF3C4), Offset(c.x - w * 0.18f, c.y + h * 0.8f), Size(w * 0.36f, h * 0.4f))
    inked(shade, color, pen)
    shine(Offset(c.x - w * 0.22f, c.y + h * 0.4f), w * 0.08f, h * 0.3f, 0.6f)
}

/** A picture frame standing a little out from the wall, with its edge in 3D. */
internal fun DrawScope.frame3d(r: Rect, pen: Pen, frame: Color, inside: Color): Rect {
    box3d(r, r.width * 0.12f, frame, pen)
    val m = r.width * 0.1f
    val inner = Rect(r.left + m, r.top + m, r.right - m, r.bottom - m)
    drawRect(inside, inner.topLeft, inner.size)
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.thin)
    return inner
}

// ============================================================================================= HOME

private val HOME_DIVIDERS = floatArrayOf(1.0f, 2.45f, 3.45f)
private const val DIV_TH = 0.035f
private val homeWalls = listOf(Color(0xFFC9DDF2), Color(0xFFF0DDC2), Color(0xFFF9EECB), Color(0xFFCFEBE6))

/** Where a divider meets the back wall; room [i]'s back wall runs between edges i and i + 1. */
private fun homeEdge(i: Int, back: Float): Float = when (i) {
    0 -> -1f
    4 -> 6f
    else -> HOME_DIVIDERS[i - 1] - recede(PlaceId.HOME.floor) + recede(back)
}

private class HomeStatic(
    val planks: Planks, val petals: List<Offset>, val hearts: List<Offset>, val splash: List<Offset>,
    val bathTiles: List<Offset>, val bathFloor: List<Offset>,
)

private val homeStatic = Memo { u ->
    val back = PlaceId.HOME.back
    val petals = ArrayList<Offset>(320)
    val hearts = ArrayList<Offset>(80)
    var row = 0
    var y = SKY_TOP + 0.03f
    while (y < 0.76f) {
        var x = -0.2f + (row % 2) * 0.05f
        while (x < 1.16f) {
            val r = 0.008f
            petals.add(Offset((x - r) * u, y * u))
            petals.add(Offset((x + r) * u, y * u))
            petals.add(Offset(x * u, (y - r) * u))
            petals.add(Offset(x * u, (y + r) * u))
            hearts.add(Offset(x * u, y * u))
            x += 0.1f
        }
        y += 0.1f
        row++
    }
    // Subway tiles behind the kitchen counters.
    val splash = ArrayList<Offset>(80)
    val sx0 = 2.64f
    val sx1 = 3.1f
    for (tr in 0..8) {
        val ty = 0.52f + tr * 0.025f
        splash.add(Offset(sx0 * u, ty * u))
        splash.add(Offset(sx1 * u, ty * u))
        if (tr == 8) continue
        var tx = sx0 + (tr % 2) * 0.03f
        while (tx < sx1) {
            if (tx > sx0) {
                splash.add(Offset(tx * u, ty * u))
                splash.add(Offset(tx * u, (ty + 0.025f) * u))
            }
            tx += 0.06f
        }
    }
    // Square tiles on the bathroom wall.
    val bathTiles = ArrayList<Offset>(80)
    val bx0 = homeEdge(3, back)
    var by = 0.47f
    while (by <= back) {
        bathTiles.add(Offset(bx0 * u, by * u))
        bathTiles.add(Offset(4.9f * u, by * u))
        by += 0.045f
    }
    var bx = bx0
    while (bx < 4.9f) {
        bathTiles.add(Offset(bx * u, 0.47f * u))
        bathTiles.add(Offset(bx * u, back * u))
        bx += 0.045f
    }
    // Tiles on the bathroom floor, receding.
    val bathFloor = ArrayList<Offset>(60)
    val fx0 = HOME_DIVIDERS[2] - recede(PlaceId.HOME.floor) + DIV_TH
    var fy = FRONT_Y
    while (fy >= back) {
        bathFloor.add(Offset((fx0 + recede(fy)) * u, fy * u))
        bathFloor.add(Offset((4.9f + recede(fy)) * u, fy * u))
        fy -= 0.034f
    }
    var fx = fx0 + 0.09f
    while (fx < 4.9f) {
        bathFloor.add(Offset(fx * u, FRONT_Y * u))
        bathFloor.add(Offset((fx + recede(back)) * u, back * u))
        fx += 0.09f
    }
    HomeStatic(buildPlanks(u, -0.3f, 4.9f, back, 0.1f, 11), petals, hearts, splash, bathTiles, bathFloor)
}

internal fun DrawScope.homeBack(st: Stage, pen: Pen, styles: List<RoomStyle> = emptyList()) {
    val u = st.u
    val n = pen.night
    val back = PlaceId.HOME.back
    val hs = homeStatic.of(u)
    for (i in 0 until 4) {
        val a = homeEdge(i, back)
        val b = homeEdge(i + 1, back)
        if (!st.sees(a, b)) continue
        val paper = styles.wallOf(i)
        if (paper > 0) {
            drawWallpaper(paper, st.x(a), SKY_TOP * u - 2f, st.x(b), back * u, -st.cam * u, 0f, u)
        } else {
            drawRect(homeWalls[i], Offset(st.x(a), SKY_TOP * u), Size((b - a) * u, (back - SKY_TOP) * u))
        }
    }
    // Bedroom: soft blue wallpaper with small flowers.
    if (st.sees(-0.2f, homeEdge(1, back))) {
        if (styles.wallOf(0) == 0) clipRect(right = st.x(homeEdge(1, back))) {
            inScene(st) {
                drawPoints(hs.petals, PointMode.Points, Color.White, strokeWidth = 0.011f * u, cap = StrokeCap.Round, alpha = 0.8f)
                drawPoints(hs.hearts, PointMode.Points, Color(0xFFF49AB6), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
            }
        }
        if (st.sees(0.58f, 0.75f)) {
            val inner = frame3d(st.rect(0.6f, 0.23f, 0.73f, 0.36f), pen, Color(0xFFF7F3EC), Color(0xFFBFE3FA))
            val m = Path().apply { poly(inner.left, inner.bottom, inner.left + inner.width * 0.4f, inner.top + inner.height * 0.3f, inner.left + inner.width * 0.75f, inner.bottom) }
            drawPath(m, Color(0xFF6FA0C8))
            drawCircle(Pal.sun, inner.width * 0.12f, Offset(inner.right - inner.width * 0.22f, inner.top + inner.height * 0.25f))
        }
    }
    // Living room: warm paint, a picture rail, photos and a child's drawing.
    if (st.sees(homeEdge(1, back), homeEdge(2, back))) {
        if (styles.wallOf(1) == 0) {
            drawLine(Color(0xFFFFF8EE), st.o(homeEdge(1, back), 0.12f), st.o(homeEdge(2, back), 0.12f), strokeWidth = 0.008f * u)
            drawLine(Ink.line, st.o(homeEdge(1, back), 0.124f), st.o(homeEdge(2, back), 0.124f), strokeWidth = pen.lw * 0.6f)
        }
        if (st.sees(1.5f, 1.75f)) childDrawing(st, pen, st.o(1.62f, 0.3f))
        if (st.sees(2.0f, 2.36f)) {
            val a = frame3d(st.rect(2.02f, 0.21f, 2.16f, 0.37f), pen, Color(0xFF3B3346), Color(0xFFFFE4B5))
            drawCircle(Color(0xFFF08A5D), a.width * 0.2f, Offset(a.center.x, a.top + a.height * 0.38f))
            drawRect(Color(0xFF7BB661), Offset(a.left, a.bottom - a.height * 0.28f), Size(a.width, a.height * 0.28f))
            val b = frame3d(st.rect(2.2f, 0.28f, 2.3f, 0.38f), pen, Color(0xFFE3B27A), Color(0xFFFFD1DC))
            val heart = Path().apply {
                val c = b.center
                val s = b.width * 0.3f
                moveTo(c.x, c.y + s * 0.8f)
                cubicTo(c.x - s * 1.4f, c.y - s * 0.2f, c.x - s * 0.5f, c.y - s * 1.1f, c.x, c.y - s * 0.3f)
                cubicTo(c.x + s * 0.5f, c.y - s * 1.1f, c.x + s * 1.4f, c.y - s * 0.2f, c.x, c.y + s * 0.8f)
                close()
            }
            drawPath(heart, Color(0xFFE94F6A))
        }
    }
    // Kitchen: tiles behind the counters and a wall cupboard.
    if (st.sees(homeEdge(2, back), homeEdge(3, back))) {
        if (st.sees(2.6f, 3.15f)) {
            drawRect(Color(0xFFFBFDFB), st.o(2.64f, 0.52f), Size(0.46f * u, 0.2f * u))
            inScene(st) { drawPoints(hs.splash, PointMode.Lines, Color(0xFFB9CBD2), strokeWidth = pen.lw * 0.6f) }
            drawRect(Ink.line, st.o(2.64f, 0.52f), Size(0.46f * u, 0.2f * u), style = pen.thin)
        }
        if (st.sees(2.6f, 2.95f)) {
            val cup = st.rect(2.64f, 0.13f, 2.88f, 0.33f)
            box3d(cup, 0.09f * u, Color(0xFFD7E6D8), pen)
            drawLine(Ink.line, Offset(cup.center.x, cup.top + 0.012f * u), Offset(cup.center.x, cup.bottom - 0.012f * u), strokeWidth = pen.lw * 0.7f)
            drawLine(Color(0xFF8A8F99), Offset(cup.center.x - 0.018f * u, cup.bottom - 0.05f * u), Offset(cup.center.x - 0.018f * u, cup.bottom - 0.02f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
            drawLine(Color(0xFF8A8F99), Offset(cup.center.x + 0.018f * u, cup.bottom - 0.05f * u), Offset(cup.center.x + 0.018f * u, cup.bottom - 0.02f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
        }
    }
    // Bathroom: tiled wall.
    if (st.sees(homeEdge(3, back), homeEdge(4, back))) {
        val x0 = st.x(homeEdge(3, back))
        drawRect(Color(0xFFEFF9F7), Offset(x0, 0.47f * u), Size(st.w - x0 + 2f, (back - 0.47f) * u))
        inScene(st) { drawPoints(hs.bathTiles, PointMode.Lines, Color(0xFFA9D6CF), strokeWidth = pen.lw * 0.6f) }
        drawRect(Color(0xFF7CC7C0), Offset(x0, 0.455f * u), Size(st.w - x0 + 2f, 0.018f * u))
        drawLine(Ink.line, Offset(x0, 0.455f * u), Offset(st.w, 0.455f * u), strokeWidth = pen.lw * 0.6f)
    }
    skirting(st, pen, back, Color(0xFFF7F3EC))
    crown(st, pen, Color(0xFFF7F3EC))

    // The floor: light oak boards, tiles in the bathroom, or what the child has chosen per room.
    if ((0 until 4).any { styles.floorOf(it) == 0 }) plankFloor(st, pen, hs.planks, back, Color(0xFFD9A873))
    val bath = HOME_DIVIDERS[2] - recede(PlaceId.HOME.floor) + DIV_TH
    if (styles.floorOf(3) == 0 && st.sees(bath, 5f)) {
        val tiles = Path().apply { floorQuad(u, st.cam, bath, 5f, FRONT_Y, back) }
        drawPath(tiles, Brush.verticalGradient(listOf(Color(0xFFB8C6C9), Color(0xFFE6EEEF)), startY = back * u, endY = FRONT_Y * u))
        inScene(st) { drawPoints(hs.bathFloor, PointMode.Lines, Color(0xFF93A5AA), strokeWidth = pen.lw * 0.6f) }
    }
    for (i in 0 until 4) {
        val floor = styles.floorOf(i)
        if (floor == 0) continue
        val x0 = if (i == 0) -9f else HOME_DIVIDERS[i - 1] - recede(PlaceId.HOME.floor) + DIV_TH
        val x1 = if (i == 3) 9f else HOME_DIVIDERS[i] - recede(PlaceId.HOME.floor)
        layFloor(st, floor, back, x0, x1)
    }
    wallShadow(st, back)
    lightPatch(st, 0.4f, 0.2f, back, n)
    lightPatch(st, 1.9f, 0.2f, back, n)
    lightPatch(st, 3.25f, 0.2f, back, n)
    homeRugs(st, pen)
    for ((i, x) in HOME_DIVIDERS.withIndex()) divider(st, pen, x, back, homeWalls[i + 1], Color(0xFFF7F3EC), styles.wallOf(i + 1))
    pendant(st, pen, 1.66f, 0.12f, Color(0xFFF2C14E))
    pendant(st, pen, 3.25f, 0.1f, Color(0xFF6FB7A8))
    drawBase(st, pen, Color(0xFFE7C497), Color(0xFF9C6B45))
}

private fun DrawScope.childDrawing(st: Stage, pen: Pen, c: Offset) {
    val u = st.u
    val w = 0.1f * u
    val h = 0.075f * u
    rotate(-4f, c) {
        drawRect(Ink.shadow, Offset(c.x - w / 2f + 0.004f * u, c.y - h / 2f + 0.005f * u), Size(w, h))
        drawRect(Color.White, Offset(c.x - w / 2f, c.y - h / 2f), Size(w, h))
        drawRect(Ink.line, Offset(c.x - w / 2f, c.y - h / 2f), Size(w, h), style = pen.thin)
        val crayon = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round)
        drawRect(Color(0xFFE94F4F), Offset(c.x - w * 0.22f, c.y - h * 0.05f), Size(w * 0.26f, h * 0.3f), style = crayon)
        drawPath(Path().apply { moveTo(c.x - w * 0.26f, c.y - h * 0.05f); lineTo(c.x - w * 0.09f, c.y - h * 0.3f); lineTo(c.x + w * 0.08f, c.y - h * 0.05f) }, Color(0xFF3E7BD6), style = crayon)
        drawCircle(Color(0xFFFFB800), w * 0.07f, Offset(c.x + w * 0.28f, c.y - h * 0.25f), style = crayon)
        drawLine(Color(0xFF4CAF50), Offset(c.x - w * 0.42f, c.y + h * 0.28f), Offset(c.x + w * 0.42f, c.y + h * 0.26f), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
        drawRect(Color(0x88FFF3B0), Offset(c.x - w * 0.12f, c.y - h / 2f - 0.006f * u), Size(w * 0.24f, 0.012f * u))
    }
}

private fun DrawScope.homeRugs(st: Stage, pen: Pen) {
    val u = st.u
    if (st.sees(0.4f, 0.85f)) {
        val rug = Path().apply { floorDisc(u, st.cam, 0.6f, 0.905f, 0.14f, 0.15f, 28) }
        drawPath(rug, Color(0xFFF4B8C8))
        drawPath(rug, Ink.line, style = pen.thin)
        drawPath(Path().apply { floorDisc(u, st.cam, 0.6f, 0.905f, 0.1f, 0.105f, 24) }, Color(0xFFFAD3DD))
        drawPath(Path().apply { floorDisc(u, st.cam, 0.6f, 0.905f, 0.05f, 0.055f, 18) }, Color(0xFFF4B8C8))
    }
    if (st.sees(1.3f, 2.1f)) {
        val outer = Path().apply { floorQuad(u, st.cam, 1.38f, 1.96f, 0.955f, 0.845f) }
        drawPath(outer, Color(0xFF7F97B3))
        drawPath(Path().apply { floorQuad(u, st.cam, 1.42f, 1.92f, 0.945f, 0.855f) }, Color(0xFFF3E7D3))
        drawPath(Path().apply { floorQuad(u, st.cam, 1.45f, 1.89f, 0.935f, 0.865f) }, Color(0xFF8FA7C2))
        val stripes = Path()
        for (k in 0 until 4) {
            val x = 1.5f + k * 0.11f
            stripes.floorQuad(u, st.cam, x, x + 0.04f, 0.935f, 0.865f)
        }
        drawPath(stripes, Color(0xFFA9BCD2))
        drawPath(outer, Ink.line, style = pen.thin)
    }
    if (st.sees(2.6f, 3.1f)) {
        val runner = Path().apply { floorQuad(u, st.cam, 2.62f, 3.02f, 0.958f, 0.918f) }
        drawPath(runner, Color(0xFF9AA3AE))
        drawPath(Path().apply { floorQuad(u, st.cam, 2.65f, 2.99f, 0.95f, 0.926f) }, Color(0xFFB5BDC6))
        drawPath(runner, Ink.line, style = pen.thin)
    }
    if (st.sees(3.55f, 3.95f)) {
        val mat = Path().apply { floorQuad(u, st.cam, 3.6f, 3.86f, 0.962f, 0.924f) }
        drawPath(mat, Color(0xFF8FD3CB))
        val lines = Path()
        for (k in 0 until 3) lines.floorQuad(u, st.cam, 3.63f + k * 0.08f, 3.65f + k * 0.08f, 0.962f, 0.924f)
        drawPath(lines, Color.White, alpha = 0.7f)
        drawPath(mat, Ink.line, style = pen.thin)
    }
}

/**
 * A thin inner wall from the front edge back to the back wall, meeting the furniture line at scene x
 * [x]. We see its right face (the next room's wall, [face]) with a door opening, its cut front edge and
 * its top, in [edge].
 */
private fun DrawScope.divider(st: Stage, pen: Pen, x: Float, back: Float, face: Color, edge: Color, paper: Int = 0) {
    val u = st.u
    val xf = x - recede(PlaceId.HOME.floor)
    val shift = recede(back)
    if (!st.sees(xf - 0.05f, xf + shift + 0.1f)) return
    val th = DIV_TH
    val h = back - SKY_TOP
    fun px(f: Float, dx: Float = 0f) = st.x(xf + shift * f + dx)
    fun py(f: Float, up: Float = 0f) = (mix(FRONT_Y, back, f) - up) * u
    val d0 = 0.2f
    val d1 = 0.72f
    val dh = 0.46f
    val facePath = Path().apply {
        fillType = PathFillType.EvenOdd
        moveTo(px(0f, th), py(0f, h))
        lineTo(px(1f, th), py(1f, h))
        lineTo(px(1f, th), py(1f))
        lineTo(px(0f, th), py(0f))
        close()
        moveTo(px(d0, th), py(d0, dh))
        lineTo(px(d1, th), py(d1, dh))
        lineTo(px(d1, th), py(d1))
        lineTo(px(d0, th), py(d0))
        close()
    }
    if (paper > 0) {
        // The next room's wallpaper runs on along its side wall, in shade.
        drawWallpaperSlanted(paper, facePath, px(0f, th), u)
        drawPath(facePath, Brush.horizontalGradient(listOf(Ink.line.copy(alpha = 0.26f), Ink.line.copy(alpha = 0.1f)), startX = px(0f), endX = px(1f)))
    } else {
        drawPath(facePath, Brush.horizontalGradient(listOf(face.darken(0.18f), face.darken(0.06f)), startX = px(0f), endX = px(1f)))
    }
    // Skirting along the foot of the wall, either side of the door.
    val skirt = Path().apply {
        poly(px(0f, th), py(0f, 0.024f), px(d0, th), py(d0, 0.024f), px(d0, th), py(d0), px(0f, th), py(0f))
        poly(px(d1, th), py(d1, 0.024f), px(1f, th), py(1f, 0.024f), px(1f, th), py(1f), px(d1, th), py(d1))
    }
    drawPath(skirt, edge.darken(0.08f))
    drawPath(facePath, Ink.line, style = pen.stroke)
    // The door: the far jamb shows the wall's thickness, and a casing runs round the opening.
    val jamb = Path().apply { poly(px(d1), py(d1, dh), px(d1, th), py(d1, dh), px(d1, th), py(d1), px(d1), py(d1)) }
    drawPath(jamb, edge.darken(0.12f))
    drawPath(jamb, Ink.line, style = pen.thin)
    val casing = Path().apply {
        moveTo(px(d0, th), py(d0))
        lineTo(px(d0, th), py(d0, dh))
        lineTo(px(d1, th), py(d1, dh))
        lineTo(px(d1, th), py(d1))
    }
    drawPath(casing, edge, style = Stroke(0.012f * u))
    drawPath(casing, Ink.line, style = pen.thin)
    // Cut front edge and top.
    val front = Path().apply { poly(px(0f), py(0f, h), px(0f, th), py(0f, h), px(0f, th), py(0f), px(0f), py(0f)) }
    drawPath(front, edge)
    drawPath(front, Ink.line, style = pen.stroke)
    val top = Path().apply { poly(px(0f), py(0f, h), px(1f), py(1f, h), px(1f, th), py(1f, h), px(0f, th), py(0f, h)) }
    drawPath(top, edge.lighten(0.3f))
    drawPath(top, Ink.line, style = pen.stroke)
}

// ============================================================================================= CAFE

private class CafeStatic(
    val stripes: Path, val beads: List<Offset>, val dark: Path, val string: Path, val flags: List<Path>, val flagLines: Path,
    val chalk: Path,
)

private val cafeFlagColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFC83D), Color(0xFF5ED1A4), Color(0xFFFF9EC4), Color(0xFF5AA9E6))

private val cafeStatic = Memo { u ->
    val back = PlaceId.CAFE.back
    val stripes = Path()
    var x = -0.3f
    while (x < 3.9f) {
        stripes.addRect(Rect(x * u, SKY_TOP * u, (x + 0.065f) * u, 0.6f * u))
        x += 0.14f
    }
    val beads = ArrayList<Offset>(90)
    x = -0.3f
    while (x < 3.9f) {
        beads.add(Offset(x * u, 0.615f * u))
        beads.add(Offset(x * u, (back - 0.024f) * u))
        x += 0.05f
    }
    // Checkered floor tiles receding into the picture.
    val dark = Path()
    val rows = 4
    for (j in 0 until rows) {
        val yf = mix(FRONT_Y, back, j / rows.toFloat())
        val yb = mix(FRONT_Y, back, (j + 1) / rows.toFloat())
        var i = -8
        while (i < 36) {
            if ((i + j) % 2 == 0) dark.floorQuad(u, 0f, i * 0.12f + recede(yf), (i + 1) * 0.12f + recede(yf), yf, yb)
            i++
        }
    }
    // Bunting sagging between hooks along the top of the wall.
    val string = Path()
    val flags = List(cafeFlagColors.size) { Path() }
    val flagLines = Path()
    var k = 0
    var hx = -0.3f
    while (hx < 3.9f) {
        val y0 = 0.06f
        val sag = 0.035f
        string.moveTo(hx * u, y0 * u)
        string.quadraticTo((hx + 0.3f) * u, (y0 + sag * 2f) * u, (hx + 0.6f) * u, y0 * u)
        var f = 0.08f
        while (f < 0.95f) {
            val a = f - 0.05f
            val b = f + 0.05f
            fun sy(s: Float) = y0 + 4f * sag * s * (1f - s)
            val fl = Path().apply {
                moveTo((hx + a * 0.6f) * u, sy(a) * u)
                lineTo((hx + b * 0.6f) * u, sy(b) * u)
                lineTo((hx + f * 0.6f) * u, (sy(f) + 0.05f) * u)
                close()
            }
            flags[k % flags.size].addPath(fl)
            flagLines.addPath(fl)
            k++
            f += 0.14f
        }
        hx += 0.6f
    }
    // Chalk pictograms for the menu board: a cup, a cupcake and a waffle heart.
    val chalk = Path()
    fun cx(v: Float) = v * u
    chalk.moveTo(cx(1.33f), cx(0.25f))
    chalk.lineTo(cx(1.335f), cx(0.285f))
    chalk.lineTo(cx(1.375f), cx(0.285f))
    chalk.lineTo(cx(1.38f), cx(0.25f))
    chalk.close()
    chalk.addArc(Rect(cx(1.372f), cx(0.255f), cx(1.395f), cx(0.275f)), -90f, 180f)
    chalk.moveTo(cx(1.35f), cx(0.24f))
    chalk.quadraticTo(cx(1.34f), cx(0.23f), cx(1.35f), cx(0.22f))
    chalk.moveTo(cx(1.365f), cx(0.24f))
    chalk.quadraticTo(cx(1.355f), cx(0.23f), cx(1.365f), cx(0.22f))
    chalk.moveTo(cx(1.335f), cx(0.33f))
    chalk.lineTo(cx(1.345f), cx(0.365f))
    chalk.lineTo(cx(1.375f), cx(0.365f))
    chalk.lineTo(cx(1.385f), cx(0.33f))
    chalk.close()
    chalk.moveTo(cx(1.33f), cx(0.33f))
    chalk.quadraticTo(cx(1.36f), cx(0.28f), cx(1.39f), cx(0.33f))
    val hc = Offset(cx(1.36f), cx(0.41f))
    val hsz = 0.022f * u
    chalk.moveTo(hc.x, hc.y + hsz)
    chalk.cubicTo(hc.x - hsz * 1.6f, hc.y - hsz * 0.1f, hc.x - hsz * 0.6f, hc.y - hsz * 1.2f, hc.x, hc.y - hsz * 0.35f)
    chalk.cubicTo(hc.x + hsz * 0.6f, hc.y - hsz * 1.2f, hc.x + hsz * 1.6f, hc.y - hsz * 0.1f, hc.x, hc.y + hsz)
    chalk.moveTo(hc.x - hsz * 0.8f, hc.y - hsz * 0.2f)
    chalk.lineTo(hc.x + hsz * 0.8f, hc.y - hsz * 0.2f)
    chalk.moveTo(hc.x - hsz * 0.5f, hc.y + hsz * 0.3f)
    chalk.lineTo(hc.x + hsz * 0.5f, hc.y + hsz * 0.3f)
    chalk.moveTo(hc.x, hc.y - hsz * 0.35f)
    chalk.lineTo(hc.x, hc.y + hsz)
    CafeStatic(stripes, beads, dark, string, flags, flagLines, chalk)
}

internal fun DrawScope.cafeBack(st: Stage, pen: Pen, styles: List<RoomStyle> = emptyList()) {
    val u = st.u
    val back = PlaceId.CAFE.back
    val cs = cafeStatic.of(u)
    val paper = styles.wallOf(0)
    if (paper > 0) {
        paperWall(st, paper, back)
    } else {
        drawRect(Color(0xFFFFF5E4), Offset(0f, SKY_TOP * u), Size(st.w, (back - SKY_TOP) * u))
        inScene(st) { drawPath(cs.stripes, Color(0xFFCDEFDF)) }
        drawRect(Color(0xFF8ED6B7), Offset(0f, 0.6f * u), Size(st.w, (back - 0.6f) * u))
        inScene(st) { drawPoints(cs.beads, PointMode.Lines, Color(0xFF6DBF9C), strokeWidth = pen.lw * 0.7f) }
        drawRect(Color(0xFFFFFBF2), Offset(0f, 0.592f * u), Size(st.w, 0.022f * u))
        drawLine(Ink.line, Offset(0f, 0.592f * u), Offset(st.w, 0.592f * u), strokeWidth = pen.lw * 0.7f)
        drawLine(Ink.line, Offset(0f, 0.614f * u), Offset(st.w, 0.614f * u), strokeWidth = pen.lw * 0.7f)
    }
    skirting(st, pen, back, Color(0xFF5DB892))
    crown(st, pen, Color(0xFF7FD3B0))
    inScene(st) {
        drawPath(cs.string, Ink.line, style = pen.thin)
        for ((i, p) in cs.flags.withIndex()) drawPath(p, cafeFlagColors[i])
        drawPath(cs.flagLines, Ink.line, style = pen.thin)
    }
    if (st.sees(0f, 0.7f)) breadShelves(st, pen)
    if (st.sees(1.2f, 1.7f)) {
        val board = st.rect(1.28f, 0.2f, 1.58f, 0.45f)
        box3d(board, 0.025f * u, Color(0xFFA0663B), pen)
        val m = 0.014f * u
        drawRect(Color(0xFF2F4F48), Offset(board.left + m, board.top + m), Size(board.width - 2 * m, board.height - 2 * m))
        inScene(st) { drawPath(cs.chalk, Color.White, alpha = 0.9f, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round)) }
        val dots = ArrayList<Offset>(6)
        for ((row, count) in listOf(0.265f to 2, 0.345f to 3, 0.415f to 1)) {
            for (c in 0 until count) dots.add(st.o(1.47f + c * 0.025f, row))
        }
        drawPoints(dots, PointMode.Points, Color(0xFFFFD66B), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    }
    if (styles.floorOf(0) > 0) {
        layFloor(st, styles.floorOf(0), back)
    } else {
        drawRect(Color(0xFFFFF6E6), Offset(0f, back * u), Size(st.w, (FRONT_Y - back) * u))
        inScene(st) { drawPath(cs.dark, Color(0xFF6CC3A0)) }
        drawRect(
            Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.16f), Ink.line.copy(alpha = 0f)), startY = back * u, endY = FRONT_Y * u),
            Offset(0f, back * u),
            Size(st.w, (FRONT_Y - back) * u),
        )
    }
    wallShadow(st, back)
    lightPatch(st, 2.46f, 0.2f, back, pen.night)
    pendant(st, pen, 0.95f, 0.12f, Color(0xFFFFC83D))
    pendant(st, pen, 2.2f, 0.12f, Color(0xFFFF8A7A))
    pendant(st, pen, 2.72f, 0.12f, Color(0xFF4FC79A))
    drawBase(st, pen, Color(0xFF9FE0C4), Color(0xFF3F9C78))
}

/** Two shelves of bread high on the wall, boards seen a little from above. */
private fun DrawScope.breadShelves(st: Stage, pen: Pen) {
    val u = st.u
    val crust = Color(0xFFC98440)
    val scores = ArrayList<Offset>(40)
    for ((row, y) in floatArrayOf(0.3f, 0.45f).withIndex()) {
        val l = st.x(0.04f)
        val r = st.x(0.62f)
        for (bx in floatArrayOf(0.1f, 0.56f)) {
            val b = Path().apply { poly(st.x(bx - 0.012f), y * u, st.x(bx + 0.012f), y * u, st.x(bx + 0.004f), (y + 0.04f) * u, st.x(bx - 0.004f), (y + 0.04f) * u) }
            drawPath(b, Color(0xFF8C5A33))
            drawPath(b, Ink.line, style = pen.thin)
        }
        val depth = 0.07f * u
        topFace3d(l, r, y * u, depth, Color(0xFFE3B27A), pen)
        inkedRound(Rect(l, y * u, r, (y + 0.016f) * u), 0.004f * u, Color(0xFFC98A55), pen, shade = false)
        val by = (y - 0.009f) * u
        val loaves = if (row == 0) floatArrayOf(0.12f, 0.28f, 0.43f, 0.55f) else floatArrayOf(0.14f, 0.3f, 0.47f)
        for ((i, lx) in loaves.withIndex()) {
            val long = (i + row) % 2 == 1
            val w = (if (long) 0.13f else 0.085f) * u
            val h = (if (long) 0.042f else 0.055f) * u
            val cx = st.x(lx)
            inkedOval(Rect(cx - w / 2f, by - h, cx + w / 2f, by), crust, pen)
            val n = if (long) 3 else 2
            for (k in 0 until n) {
                val sx = cx + (k - (n - 1) / 2f) * w * 0.25f
                scores.add(Offset(sx - w * 0.06f, by - h * 0.35f))
                scores.add(Offset(sx + w * 0.06f, by - h * 0.75f))
            }
        }
    }
    drawPoints(scores, PointMode.Lines, Color(0xFFF4D29C), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
}

// ============================================================================================ SALON

private class SalonStatic(val dots: List<Offset>, val arches: Path, val chips: List<List<Offset>>, val joints: List<Offset>)

private val salonChipColors = listOf(Color(0xFFF28DB2), Color(0xFFB69AE6), Color(0xFF8FD9C0), Color(0xFFB8B0BC))

private val salonStatic = Memo { u ->
    val back = PlaceId.SALON.back
    val dots = ArrayList<Offset>(260)
    var row = 0
    var y = SKY_TOP + 0.02f
    while (y < 0.56f) {
        var x = -0.3f + (row % 2) * 0.045f
        while (x < 3.7f) {
            dots.add(Offset(x * u, y * u))
            x += 0.09f
        }
        y += 0.07f
        row++
    }
    val arches = Path()
    var ax = -0.2f
    while (ax < 3.7f) {
        arches.moveTo((ax + 0.03f) * u, (back - 0.04f) * u)
        arches.lineTo((ax + 0.03f) * u, 0.66f * u)
        arches.quadraticTo((ax + 0.1f) * u, 0.61f * u, (ax + 0.17f) * u, 0.66f * u)
        arches.lineTo((ax + 0.17f) * u, (back - 0.04f) * u)
        arches.close()
        ax += 0.2f
    }
    val chips = List(salonChipColors.size) { ArrayList<Offset>(60) }
    for (i in 0 until 220) {
        val fy = hash01(i, 401)
        val yy = mix(FRONT_Y - 0.006f, back + 0.006f, fy)
        val xx = -0.3f + hash01(i, 402) * 3.9f + recede(yy)
        chips[i % chips.size].add(Offset(xx * u, yy * u))
    }
    val joints = ArrayList<Offset>(60)
    var jx = -0.6f
    while (jx < 3.7f) {
        joints.add(Offset(jx * u, FRONT_Y * u))
        joints.add(Offset((jx + recede(back)) * u, back * u))
        jx += 0.34f
    }
    val mid = mix(FRONT_Y, back, 0.5f)
    joints.add(Offset(-0.3f * u, mid * u))
    joints.add(Offset(3.9f * u, mid * u))
    SalonStatic(dots, arches, chips, joints)
}

internal fun DrawScope.salonBack(st: Stage, pen: Pen, styles: List<RoomStyle> = emptyList()) {
    val u = st.u
    val back = PlaceId.SALON.back
    val ss = salonStatic.of(u)
    val paper = styles.wallOf(0)
    if (paper > 0) {
        paperWall(st, paper, back)
    } else {
        drawRect(Color(0xFFFFD6E6), Offset(0f, SKY_TOP * u), Size(st.w, (back - SKY_TOP) * u))
        inScene(st) { drawPoints(ss.dots, PointMode.Points, Color(0xFFE3C3F2), strokeWidth = 0.012f * u, cap = StrokeCap.Round) }
        drawRect(Color(0xFFD7C4F4), Offset(0f, 0.58f * u), Size(st.w, (back - 0.58f) * u))
        inScene(st) { drawPath(ss.arches, Color(0xFFB79CE3), style = Stroke(pen.lw * 0.9f)) }
        drawRect(Color.White, Offset(0f, 0.572f * u), Size(st.w, 0.02f * u))
        drawLine(Ink.line, Offset(0f, 0.572f * u), Offset(st.w, 0.572f * u), strokeWidth = pen.lw * 0.7f)
        drawLine(Ink.line, Offset(0f, 0.592f * u), Offset(st.w, 0.592f * u), strokeWidth = pen.lw * 0.7f)
    }
    skirting(st, pen, back, Color(0xFFA88BD6))
    crown(st, pen, Color(0xFFC9B2F0))

    val portraits = floatArrayOf(0.9f, 0.17f, 1.44f, 0.24f, 1.98f, 0.2f)
    for (k in 0 until 3) {
        val px = portraits[k * 2]
        val py = portraits[k * 2 + 1]
        if (st.sees(px - 0.1f, px + 0.1f)) hairPortrait(st, pen, px, py, k)
    }
    if (st.sees(0f, 0.6f)) yarnShelf(st, pen)
    if (st.sees(2.4f, 2.9f)) {
        val l = st.x(2.44f)
        val r = st.x(2.78f)
        topFace3d(l, r, 0.2f * u, 0.07f * u, Color(0xFFF3E7FF), pen)
        inkedRound(Rect(l, 0.2f * u, r, 0.215f * u), 0.004f * u, Color(0xFFE2D2FA), pen, shade = false)
        trailingPlant(st, pen, 2.6f, 0.19f)
    }
    if (st.sees(1.15f, 1.45f)) {
        drawLine(Ink.line, st.o(1.26f, 0f), st.o(1.3f, 0.12f), strokeWidth = pen.lw * 0.8f)
        drawLine(Ink.line, st.o(1.34f, 0f), st.o(1.3f, 0.12f), strokeWidth = pen.lw * 0.8f)
        trailingPlant(st, pen, 1.3f, 0.17f)
    }

    if (styles.floorOf(0) > 0) {
        layFloor(st, styles.floorOf(0), back)
    } else {
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFFE2D4DF), Color(0xFFF8F1F6)), startY = back * u, endY = FRONT_Y * u),
            Offset(0f, back * u),
            Size(st.w, (FRONT_Y - back) * u),
        )
        inScene(st) {
            for ((i, pts) in ss.chips.withIndex()) {
                drawPoints(pts, PointMode.Points, salonChipColors[i], strokeWidth = (0.006f + 0.003f * (i % 2)) * u, cap = StrokeCap.Round)
            }
            drawPoints(ss.joints, PointMode.Lines, Color(0xFFCDBBCB), strokeWidth = pen.lw * 0.7f)
        }
    }
    wallShadow(st, back)
    lightPatch(st, 2.3f, 0.2f, back, pen.night)
    drawBase(st, pen, Color(0xFFF1E3EE), Color(0xFFB79CC9))
}

/** A framed picture of a hairstyle: a round face with different hair. */
private fun DrawScope.hairPortrait(st: Stage, pen: Pen, x: Float, y: Float, style: Int) {
    val u = st.u
    val bg = listOf(Color(0xFFFFF1C9), Color(0xFFD7F2FF), Color(0xFFE6FFE3))[style]
    val inner = frame3d(st.rect(x - 0.065f, y - 0.08f, x + 0.065f, y + 0.08f), pen, Color(0xFFF7F3EC), bg)
    val skin = listOf(Color(0xFFF2C29B), Color(0xFF8D5A3B), Color(0xFFE9A77F))[style]
    val hair = listOf(Color(0xFF8B5CF6), Color(0xFFFF8A3D), Color(0xFF3B2A25))[style]
    val r = inner.width * 0.22f
    val c = Offset(inner.center.x, inner.center.y + inner.height * 0.05f)
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        drawOval(hair.darken(0.1f), Offset(c.x - r * 1.5f, inner.bottom - r * 0.9f), Size(r * 3f, r * 2f))
        when (style) {
            0 -> inkedCircle(Offset(c.x, c.y - r * 0.35f), r * 1.55f, hair, pen, shade = false)
            1 -> {
                inkedCircle(Offset(c.x, c.y - r * 1.25f), r * 0.55f, hair, pen, shade = false)
                inkedRound(Rect(c.x - r * 1.2f, c.y - r * 1.1f, c.x + r * 1.2f, c.y + r * 0.9f), r * 0.6f, hair, pen, shade = false)
            }
            else -> inkedRound(Rect(c.x - r * 1.3f, c.y - r * 1.2f, c.x + r * 1.3f, c.y + r * 1.6f), r * 0.8f, hair, pen, shade = false)
        }
        inkedCircle(c, r, skin, pen, shade = false)
        val fringe = Path().apply {
            moveTo(c.x - r, c.y - r * 0.05f)
            quadraticTo(c.x - r, c.y - r * 1.05f, c.x, c.y - r * 1.02f)
            quadraticTo(c.x + r, c.y - r * 1.05f, c.x + r, c.y - r * 0.05f)
            quadraticTo(c.x + r * 0.3f, c.y - r * 0.6f, c.x - r, c.y - r * 0.05f)
            close()
        }
        if (style != 0) drawPath(fringe, hair)
        drawPoints(listOf(Offset(c.x - r * 0.35f, c.y + r * 0.05f), Offset(c.x + r * 0.35f, c.y + r * 0.05f)), PointMode.Points, Ink.line, strokeWidth = r * 0.22f, cap = StrokeCap.Round)
        drawArc(Ink.line, 20f, 140f, false, Offset(c.x - r * 0.35f, c.y + r * 0.05f), Size(r * 0.7f, r * 0.5f), style = pen.thin)
    }
}

/** A high shelf with two baskets of yarn. */
private fun DrawScope.yarnShelf(st: Stage, pen: Pen) {
    val u = st.u
    val l = st.x(0.04f)
    val r = st.x(0.52f)
    val y = 0.2f * u
    topFace3d(l, r, y, 0.07f * u, Color(0xFFF3E7FF), pen)
    inkedRound(Rect(l, y, r, y + 0.015f * u), 0.004f * u, Color(0xFFE2D2FA), pen, shade = false)
    val yarn = listOf(Color(0xFFFF6B9E), Color(0xFF5AA9E6), Color(0xFFFFC83D), Color(0xFF6BCB77), Color(0xFFB983FF), Color(0xFFFF8A3D))
    for ((b, bx) in floatArrayOf(0.15f, 0.38f).withIndex()) {
        val cx = st.x(bx)
        val w = 0.17f * u
        val h = 0.07f * u
        val by = y - 0.01f * u
        for (k in 0 until 3) {
            val c = Offset(cx + (k - 1) * w * 0.3f, by - h * 0.9f + (k % 2) * h * 0.12f)
            val rr = w * 0.16f
            inkedCircle(c, rr, yarn[b * 3 + k], pen, shade = false)
            drawArc(yarn[b * 3 + k].darken(0.3f), 200f, 120f, false, Offset(c.x - rr * 0.7f, c.y - rr * 0.7f), Size(rr * 1.4f, rr * 1.4f), style = pen.thin)
            drawArc(yarn[b * 3 + k].darken(0.3f), 20f, 110f, false, Offset(c.x - rr * 0.45f, c.y - rr * 0.45f), Size(rr * 0.9f, rr * 0.9f), style = pen.thin)
        }
        val basket = Rect(cx - w / 2f, by - h * 0.62f, cx + w / 2f, by)
        inkedRound(basket, h * 0.2f, Color(0xFFC99A5E), pen)
        val weave = ArrayList<Offset>(16)
        for (k in 1..5) {
            val wx = basket.left + basket.width * k / 6f
            weave.add(Offset(wx, basket.top + 2f))
            weave.add(Offset(wx, basket.bottom - 2f))
        }
        weave.add(Offset(basket.left + 2f, basket.center.y))
        weave.add(Offset(basket.right - 2f, basket.center.y))
        drawPoints(weave, PointMode.Lines, Color(0xFFA0763F), strokeWidth = pen.lw * 0.6f)
    }
}

/** A pot with long trailing leaves, hanging or standing at scene ([x], [y]) (the pot's rim). */
private fun DrawScope.trailingPlant(st: Stage, pen: Pen, x: Float, y: Float) {
    val u = st.u
    val c = st.o(x, y)
    val w = 0.07f * u
    val leaves = Path()
    val vines = Path()
    for (k in 0 until 4) {
        val vx = c.x + (k - 1.5f) * w * 0.35f
        val len = (0.1f + 0.05f * (k % 2)) * u
        val sway = sin(pen.t * 0.8f + k) * 0.006f * u
        vines.moveTo(vx, c.y)
        vines.quadraticTo(vx + (k - 1.5f) * w * 0.4f, c.y + len * 0.5f, vx + sway, c.y + len)
        for (j in 1..3) {
            val f = j / 3.2f
            val lx = mix(vx, vx + sway, f) + (k - 1.5f) * w * 0.3f * f * (1f - f) * 2f
            val ly = c.y + len * f
            leaves.addOval(Rect(lx - 0.012f * u, ly - 0.007f * u, lx + 0.012f * u, ly + 0.007f * u))
        }
    }
    drawPath(vines, Color(0xFF3F8A4A), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    drawPath(leaves, Color(0xFF5DB35B))
    drawPath(leaves, Ink.line, style = pen.thin)
    val pot = Path().apply { poly(c.x - w / 2f, c.y - w * 0.55f, c.x + w / 2f, c.y - w * 0.55f, c.x + w * 0.36f, c.y, c.x - w * 0.36f, c.y) }
    inked(pot, Color(0xFFF08A5D), pen)
    val top = Path().apply { floorDisc(1f, 0f, c.x, c.y - w * 0.55f, w * 0.5f, w * 0.12f, 14) }
    drawPath(top, Color(0xFF6B4A34))
    for (k in 0 until 3) {
        val a = -1.2f + k * 1.2f
        val tip = Offset(c.x + sin(a) * w * 0.6f, c.y - w * 0.55f - cos(a) * w * 0.55f)
        val leaf = Path().apply {
            moveTo(c.x, c.y - w * 0.55f)
            quadraticTo(c.x + sin(a - 0.5f) * w * 0.5f, c.y - w * 0.55f - cos(a - 0.5f) * w * 0.5f, tip.x, tip.y)
            quadraticTo(c.x + sin(a + 0.5f) * w * 0.5f, c.y - w * 0.55f - cos(a + 0.5f) * w * 0.5f, c.x, c.y - w * 0.55f)
            close()
        }
        inked(leaf, Color(0xFF5DB35B), pen, shade = false)
    }
}
