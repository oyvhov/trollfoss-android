package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.sin

/*
 * The shop: a bright modern grocery store as a cut-away room. A big window onto the street on the left,
 * shelves full of goods along the back wall, picture signs hanging from the ceiling, long light tubes,
 * sliding glass doors on the right and a shiny tiled floor.
 */

private const val WIN_L = 0.03f
private const val WIN_R = 0.97f
private const val WIN_T = 0.1f
private const val WIN_B = 0.47f

private class ShopStatic(val tiles: Path, val joints: List<Offset>, val boards: Path, val goods: List<Path>, val shelfBack: Path)

private val goodsColors = listOf(Color(0xFFFF8A7A), Color(0xFFFFD166), Color(0xFF7BD389), Color(0xFF7FB8F0), Color(0xFFD7A6F2), Color(0xFFFFB36B))

private val shopStatic = Memo { u ->
    val back = PlaceId.SHOP.back
    val tiles = Path()
    val rows = 4
    for (j in 0 until rows) {
        val yf = mix(FRONT_Y, back, j / rows.toFloat())
        val yb = mix(FRONT_Y, back, (j + 1) / rows.toFloat())
        for (i in -6 until 36) {
            if ((i + j) % 2 == 0) tiles.floorQuad(u, 0f, i * 0.12f + recede(yf), (i + 1) * 0.12f + recede(yf), yf, yb)
        }
    }
    val joints = ArrayList<Offset>()
    for (j in 1 until rows) {
        val y = mix(FRONT_Y, back, j / rows.toFloat())
        joints.add(Offset((-0.8f + recede(y)) * u, y * u))
        joints.add(Offset((4.2f + recede(y)) * u, y * u))
    }
    var jx = -0.72f
    while (jx < 4.0f) {
        joints.add(Offset(jx * u, FRONT_Y * u))
        joints.add(Offset((jx + recede(back)) * u, back * u))
        jx += 0.12f
    }
    // Shelving along the back wall, a little faded as it is far back.
    val shelfBack = Path().apply { addRect(Rect(1.0f * u, 0.47f * u, 3.02f * u, back * u)) }
    val boards = Path()
    val goods = List(goodsColors.size) { Path() }
    var k = 0
    for (y in floatArrayOf(0.555f, 0.645f, 0.735f)) {
        boards.addRect(Rect(1.0f * u, y * u, 3.02f * u, (y + 0.008f) * u))
        var x = 1.03f
        while (x < 3.0f) {
            val w = 0.03f + 0.03f * hash01(k, 941)
            val h = 0.04f + 0.035f * hash01(k, 942)
            if (x + w > 3.0f) break
            goods[k % goods.size].addRoundRect(RoundRect(Rect(x * u, (y - h) * u, (x + w) * u, y * u), CornerRadius(0.004f * u)))
            x += w + 0.008f
            k++
        }
    }
    ShopStatic(tiles, joints, boards, goods, shelfBack)
}

internal fun DrawScope.shopBack(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val back = PlaceId.SHOP.back
    val ss = shopStatic.of(u)
    drawRect(Color(0xFFF7F4EF), Offset(0f, SKY_TOP * u), Size(st.w, (back - SKY_TOP) * u))
    drawRect(Color(0xFF2FB57A), Offset(0f, 0.025f * u), Size(st.w, 0.028f * u))
    drawRect(Color(0xFFFFD166), Offset(0f, 0.053f * u), Size(st.w, 0.008f * u))
    drawLine(Ink.line, Offset(0f, 0.025f * u), Offset(st.w, 0.025f * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(0f, 0.061f * u), Offset(st.w, 0.061f * u), strokeWidth = pen.lw * 0.7f)
    if (st.sees(WIN_L, WIN_R)) shopWindow(st, pen, st.rect(WIN_L, WIN_T, WIN_R, WIN_B), 0.34f)
    if (st.sees(1.0f, 3.05f)) {
        inScene(st) {
            drawPath(ss.shelfBack, Color(0xFFE3E7EC))
            for ((i, g) in ss.goods.withIndex()) drawPath(g, goodsColors[i].lighten(0.25f))
            drawPath(ss.boards, Color(0xFFC3CAD3))
            drawPath(ss.shelfBack, Ink.line, alpha = 0.5f, style = pen.thin)
        }
    }
    if (st.sees(1.45f, 1.7f)) {
        // A sunburst price sticker: a star with a heart, no words.
        val c = st.o(1.56f, 0.3f)
        inked(starPath(c, 0.07f * u, 0.05f * u, 18f), Color(0xFFFFC83D), pen)
        val h = Path().apply {
            val s = 0.018f * u
            moveTo(c.x, c.y + s * 0.9f)
            cubicTo(c.x - s * 1.6f, c.y - s * 0.1f, c.x - s * 0.6f, c.y - s * 1.2f, c.x, c.y - s * 0.35f)
            cubicTo(c.x + s * 0.6f, c.y - s * 1.2f, c.x + s * 1.6f, c.y - s * 0.1f, c.x, c.y + s * 0.9f)
            close()
        }
        drawPath(h, Color(0xFFE94F6A))
    }
    if (st.sees(3.0f, 3.5f)) slidingDoors(st, pen)
    skirting(st, pen, back, Color(0xFFD5DBE2))
    crown(st, pen, Color(0xFFF7F4EF))

    drawRect(
        Brush.verticalGradient(listOf(Color(0xFFD9DEE4), Color(0xFFF4F6F8)), startY = back * u, endY = FRONT_Y * u),
        Offset(0f, back * u), Size(st.w, (FRONT_Y - back) * u),
    )
    inScene(st) {
        drawPath(ss.tiles, Color(0xFFE2E7EC))
        drawPoints(ss.joints, PointMode.Lines, Color(0xFFC4CCD5), strokeWidth = pen.lw * 0.6f)
    }
    for (x in floatArrayOf(0.5f, 1.4f, 2.3f, 3.1f)) {
        if (!st.sees(x - 0.4f, x + 0.3f)) continue
        val streak = Path().apply { floorQuad(u, st.cam, x - 0.22f, x + 0.08f, 0.93f, 0.84f) }
        drawPath(streak, Color.White, alpha = 0.35f)
    }
    wallShadow(st, back)
    for ((x, icon) in listOf(1.3f to 0, 1.92f to 1, 2.72f to 2)) if (st.sees(x - 0.12f, x + 0.12f)) aisleSign(st, pen, x, icon)
    for (x in floatArrayOf(0.5f, 1.4f, 2.3f, 3.1f)) if (st.sees(x - 0.25f, x + 0.25f)) lightTube(st, pen, x)
    drawBase(st, pen, Color(0xFFE9EDF1), Color(0xFF8C96A3))
}

/** The street outside, seen through glass: houses across the road, a tree, a lamp and the sky. */
private fun DrawScope.street(st: Stage, pen: Pen, r: Rect) {
    val u = st.u
    val n = pen.night
    drawRect(Brush.verticalGradient(listOf(lerp(Color(0xFF7CC4F2), Color(0xFF1B1850), n), lerp(Color(0xFFD6F0FF), Color(0xFF4B3A8E), n)), startY = r.top, endY = r.bottom), r.topLeft, r.size)
    val lit = ramp((n - 0.3f) / 0.4f)
    val walls = listOf(Color(0xFFF2C6A0), Color(0xFFBFD7EA), Color(0xFFF7E3A1), Color(0xFFD8C4F0), Color(0xFFBFE3C7))
    // The street moves a little slower than the shop, so it feels far away.
    for (i in 0 until 9) {
        val lx = -0.3f + i * 0.42f
        val x = st.px(lx, 0.6f)
        if (x > r.right + 0.3f * u || x + 0.4f * u < r.left) continue
        val w = (0.34f + 0.05f * hash01(i, 951)) * u
        val h = (0.2f + 0.08f * hash01(i, 952)) * u
        val top = r.bottom - h
        drawRect(walls[i % walls.size].atNight(n, 0.55f), Offset(x, top), Size(w, h))
        val roof = Path().apply { poly(x - 0.01f * u, top, x + w / 2f, top - 0.06f * u, x + w + 0.01f * u, top) }
        drawPath(roof, Color(0xFF6B6672).atNight(n, 0.5f))
        val win = Path()
        for (row in 0 until 2) for (col in 0 until 3) {
            val wx = x + w * (0.12f + col * 0.3f)
            val wy = top + h * (0.18f + row * 0.36f)
            win.addRect(Rect(wx, wy, wx + w * 0.16f, wy + h * 0.18f))
        }
        drawPath(win, lerp(Color(0xFF8FB6D6).atNight(n, 0.4f), Color(0xFFFFD66B), lit))
    }
    val tx = st.px(0.55f, 0.6f)
    drawLine(Color(0xFF7A5134).atNight(n, 0.5f), Offset(tx, r.bottom), Offset(tx, r.bottom - 0.12f * u), strokeWidth = 0.018f * u)
    drawPath(crownPath(tx, r.bottom - 0.17f * u, 0.09f * u, 0.08f * u, 953), Color(0xFF6DBB5A).atNight(n, 0.5f))
    val lamp = st.px(1.2f, 0.6f)
    drawLine(Color(0xFF55505E), Offset(lamp, r.bottom), Offset(lamp, r.bottom - 0.2f * u), strokeWidth = 0.008f * u)
    drawCircle(Color(0xFFFFE27A), 0.014f * u, Offset(lamp, r.bottom - 0.2f * u), alpha = 0.4f + 0.6f * lit)
    if (lit > 0f) drawCircle(Color(0xFFFFE27A), 0.07f * u, Offset(lamp, r.bottom - 0.2f * u), alpha = 0.3f * lit)
    drawRect(Color(0xFF9097A3).atNight(n, 0.5f), Offset(r.left, r.bottom - 0.03f * u), Size(r.width, 0.03f * u))
}

private fun DrawScope.shopWindow(st: Stage, pen: Pen, r: Rect, pane: Float) {
    val u = st.u
    clipPath(Path().apply { addRect(r) }) {
        street(st, pen, r)
        for (k in 0 until 3) {
            val x0 = r.left + r.width * (0.1f + k * 0.33f)
            drawPath(Path().apply { poly(x0, r.top, x0 + 0.06f * u, r.top, x0 - 0.06f * u, r.bottom, x0 - 0.12f * u, r.bottom) }, Color.White, alpha = 0.12f)
        }
    }
    val frame = Color(0xFFFFFFFF)
    drawRect(frame, r.topLeft, r.size, style = Stroke(0.014f * u))
    var x = r.left + pane * u
    while (x < r.right - 0.05f * u) {
        drawLine(frame, Offset(x, r.top), Offset(x, r.bottom), strokeWidth = 0.012f * u)
        drawLine(Ink.line, Offset(x - 0.006f * u, r.top), Offset(x - 0.006f * u, r.bottom), strokeWidth = pen.lw * 0.5f)
        x += pane * u
    }
    drawRect(Ink.line, Offset(r.left - 0.007f * u, r.top - 0.007f * u), Size(r.width + 0.014f * u, r.height + 0.014f * u), style = pen.stroke)
    topFace3d(r.left - 0.01f * u, r.right + 0.01f * u, r.bottom + 0.012f * u, 0.05f * u, Color(0xFFE9EDF1), pen)
}

private fun DrawScope.slidingDoors(st: Stage, pen: Pen) {
    val u = st.u
    val r = st.rect(3.06f, 0.34f, 3.4f, PlaceId.SHOP.back)
    clipPath(Path().apply { addRect(r) }) {
        street(st, pen, r)
        drawRect(Color(0xFFBFE6F2), r.topLeft, r.size, alpha = 0.25f)
    }
    val frame = Color(0xFF9AA4B1)
    drawRect(frame, r.topLeft, r.size, style = Stroke(0.012f * u))
    drawLine(frame, Offset(r.center.x, r.top), Offset(r.center.x, r.bottom), strokeWidth = 0.01f * u)
    drawRect(Ink.line, r.topLeft, r.size, style = pen.stroke)
    val sign = st.o(3.23f, 0.28f)
    inkedCircle(sign, 0.03f * u, Color(0xFF2FB57A), pen, shade = false)
    val arrow = Path().apply {
        moveTo(sign.x - 0.015f * u, sign.y)
        lineTo(sign.x + 0.012f * u, sign.y)
        moveTo(sign.x + 0.002f * u, sign.y - 0.01f * u)
        lineTo(sign.x + 0.013f * u, sign.y)
        lineTo(sign.x + 0.002f * u, sign.y + 0.01f * u)
    }
    drawPath(arrow, Color.White, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
}

/** A long light tube hanging on two wires, glowing. */
private fun DrawScope.lightTube(st: Stage, pen: Pen, x: Float) {
    val u = st.u
    val y = 0.09f
    for (dx in floatArrayOf(-0.12f, 0.12f)) drawLine(Ink.line, st.o(x + dx, SKY_TOP), st.o(x + dx, y), strokeWidth = pen.lw * 0.6f)
    val c = st.o(x, y + 0.012f)
    drawOval(Brush.radialGradient(listOf(Color(0x44FFFBE6), Color(0x00FFFBE6)), center = c, radius = 0.25f * u), Offset(c.x - 0.3f * u, c.y - 0.05f * u), Size(0.6f * u, 0.3f * u))
    inkedRound(st.rect(x - 0.18f, y, x + 0.18f, y + 0.022f), 0.01f * u, Color(0xFFE9EEF3), pen, shade = false)
    drawRoundRect(Color(0xFFFFFDF0), st.o(x - 0.165f, y + 0.012f), Size(0.33f * u, 0.008f * u), CornerRadius(0.004f * u))
}

/** A hanging aisle sign with a picture: an apple, an ice cream or a shopping basket. */
private fun DrawScope.aisleSign(st: Stage, pen: Pen, x: Float, icon: Int) {
    val u = st.u
    val y0 = 0.15f
    for (dx in floatArrayOf(-0.05f, 0.05f)) drawLine(Ink.line, st.o(x + dx, SKY_TOP), st.o(x + dx, y0), strokeWidth = pen.lw * 0.6f)
    val swing = sin(pen.t * 0.9f + x) * 1.5f
    val r = st.rect(x - 0.08f, y0, x + 0.08f, y0 + 0.1f)
    rotate(swing, Offset(r.center.x, r.top)) {
        inkedRound(r, 0.015f * u, Color.White, pen, shade = false)
        drawRoundRect(Color(0xFF2FB57A), Offset(r.left, r.top), Size(r.width, 0.012f * u), CornerRadius(0.006f * u))
        val c = Offset(r.center.x, r.center.y + 0.006f * u)
        val s = 0.028f * u
        when (icon) {
            0 -> {
                inkedCircle(c, s, Color(0xFFE94F4F), pen, shade = false)
                drawLine(Color(0xFF6E4A33), Offset(c.x, c.y - s), Offset(c.x + s * 0.2f, c.y - s * 1.4f), strokeWidth = pen.lw * 1.2f)
                drawOval(Color(0xFF5DB35B), Offset(c.x + s * 0.1f, c.y - s * 1.5f), Size(s * 0.7f, s * 0.35f))
            }
            1 -> {
                val cone = Path().apply { poly(c.x - s * 0.55f, c.y - s * 0.2f, c.x + s * 0.55f, c.y - s * 0.2f, c.x, c.y + s * 1.1f) }
                inked(cone, Color(0xFFE3B27A), pen, shade = false)
                inkedCircle(Offset(c.x, c.y - s * 0.45f), s * 0.6f, Color(0xFFFF9EC4), pen, shade = false)
            }
            else -> {
                val basket = Path().apply { poly(c.x - s, c.y - s * 0.2f, c.x + s, c.y - s * 0.2f, c.x + s * 0.75f, c.y + s * 0.8f, c.x - s * 0.75f, c.y + s * 0.8f) }
                drawArc(Ink.line, 180f, 180f, false, Offset(c.x - s * 0.6f, c.y - s * 0.9f), Size(s * 1.2f, s * 1.4f), style = Stroke(pen.lw * 1.2f))
                inked(basket, Color(0xFF3E7BD6), pen, shade = false)
            }
        }
    }
}
