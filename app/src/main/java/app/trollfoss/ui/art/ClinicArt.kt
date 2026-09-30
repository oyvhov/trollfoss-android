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
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.cos
import kotlin.math.sin

/*
 * The doctor's: a friendly children's clinic as a cut-away room. Pastel walls, a door with a round
 * window, a window onto a big tree, cheerful posters of a smiling tooth and a smiling heart, a clock,
 * and a clean speckled vinyl floor with a coloured guide line. (The height chart, eye chart and medicine
 * cabinet are fixtures.)
 */

private class ClinicStatic(val specks: List<List<Offset>>, val seams: List<Offset>, val guide: Path)

private val speckColors = listOf(Color(0xFFB9C7D8), Color(0xFFF2B8C6), Color(0xFFA8DCCB))

private val clinicStatic = Memo { u ->
    val back = PlaceId.DOCTOR.back
    val specks = List(speckColors.size) { ArrayList<Offset>(70) }
    for (i in 0 until 200) {
        val y = mix(FRONT_Y - 0.005f, back + 0.005f, hash01(i, 961))
        specks[i % specks.size].add(Offset((-0.4f + hash01(i, 962) * 3.8f + recede(y)) * u, y * u))
    }
    val seams = ArrayList<Offset>()
    var x = -0.6f
    while (x < 3.6f) {
        seams.add(Offset(x * u, FRONT_Y * u))
        seams.add(Offset((x + recede(back)) * u, back * u))
        x += 0.5f
    }
    val guide = Path().apply { floorQuad(u, 0f, -0.6f, 3.8f, 0.935f, 0.918f) }
    ClinicStatic(specks, seams, guide)
}

internal fun DrawScope.doctorBack(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val back = PlaceId.DOCTOR.back
    val cs = clinicStatic.of(u)
    drawRect(Color(0xFFD9F2EA), Offset(0f, SKY_TOP * u), Size(st.w, (back - SKY_TOP) * u))
    // Soft polka dots on the upper wall.
    val dots = ArrayList<Offset>(60)
    var row = 0
    var y = SKY_TOP + 0.04f
    while (y < 0.56f) {
        var x = st.cam - wrap(st.cam, 0.24f) - 0.24f + (row % 2) * 0.12f
        while (x < st.cam + st.vw + 0.2f) {
            dots.add(st.o(x, y))
            x += 0.24f
        }
        y += 0.12f
        row++
    }
    drawPoints(dots, PointMode.Points, Color(0xFFC3E8DC), strokeWidth = 0.022f * u, cap = StrokeCap.Round)
    drawRect(Color(0xFFCFE2F7), Offset(0f, 0.6f * u), Size(st.w, (back - 0.6f) * u))
    drawRect(Color.White, Offset(0f, 0.592f * u), Size(st.w, 0.02f * u))
    drawLine(Ink.line, Offset(0f, 0.592f * u), Offset(st.w, 0.592f * u), strokeWidth = pen.lw * 0.7f)
    drawLine(Ink.line, Offset(0f, 0.612f * u), Offset(st.w, 0.612f * u), strokeWidth = pen.lw * 0.7f)
    if (st.sees(-0.05f, 0.25f)) clinicDoor(st, pen)
    if (st.sees(1.4f, 1.95f)) treeWindow(st, pen)
    if (st.sees(1.0f, 1.25f)) posterTooth(st, pen, st.rect(1.03f, 0.1f, 1.21f, 0.3f))
    if (st.sees(2.42f, 2.68f)) posterHeart(st, pen, st.rect(2.46f, 0.14f, 2.64f, 0.34f))
    if (st.sees(2.8f, 3.0f)) wallClock(st, pen, st.o(2.9f, 0.2f))
    skirting(st, pen, back, Color(0xFFF7F3EC))
    crown(st, pen, Color(0xFFF7F3EC))

    drawRect(
        Brush.verticalGradient(listOf(Color(0xFFD6DEE8), Color(0xFFEEF2F7)), startY = back * u, endY = FRONT_Y * u),
        Offset(0f, back * u), Size(st.w, (FRONT_Y - back) * u),
    )
    inScene(st) {
        for ((i, pts) in cs.specks.withIndex()) drawPoints(pts, PointMode.Points, speckColors[i], strokeWidth = 0.007f * u, cap = StrokeCap.Round)
        drawPoints(cs.seams, PointMode.Lines, Color(0xFFC4CEDA), strokeWidth = pen.lw * 0.6f)
        drawPath(cs.guide, Color(0xFF7FD3B0))
    }
    wallShadow(st, back)
    lightPatch(st, 1.68f, 0.3f, back, n)
    drawBase(st, pen, Color(0xFFEEF2F7), Color(0xFF9AA8BA))
}

/** A soft yellow door with a round window at the far left. */
private fun DrawScope.clinicDoor(st: Stage, pen: Pen) {
    val u = st.u
    val r = st.rect(0.01f, 0.36f, 0.19f, PlaceId.DOCTOR.back)
    drawRect(Color(0xFFF7F3EC), Offset(r.left - 0.012f * u, r.top - 0.012f * u), Size(r.width + 0.024f * u, r.height + 0.012f * u))
    drawRect(Ink.line, Offset(r.left - 0.012f * u, r.top - 0.012f * u), Size(r.width + 0.024f * u, r.height + 0.012f * u), style = pen.thin)
    inkedRound(r, 0.008f * u, Color(0xFFFFE08A), pen)
    val c = Offset(r.center.x, r.top + r.height * 0.25f)
    drawCircle(Color(0xFFBFE6F7), 0.04f * u, c)
    drawPath(Path().apply { poly(c.x - 0.02f * u, c.y + 0.03f * u, c.x + 0.03f * u, c.y - 0.02f * u, c.x + 0.035f * u, c.y - 0.01f * u, c.x - 0.01f * u, c.y + 0.035f * u) }, Color.White, alpha = 0.6f)
    drawCircle(Color(0xFFF7F3EC), 0.04f * u, c, style = Stroke(0.008f * u))
    drawCircle(Ink.line, 0.044f * u, c, style = pen.thin)
    inkedRound(Rect(r.right - 0.04f * u, r.center.y, r.right - 0.012f * u, r.center.y + 0.012f * u), 0.006f * u, Color(0xFFB9C0CC), pen, shade = false)
}

/** A window onto a big leafy tree and a little bird, with pastel curtains. */
private fun DrawScope.treeWindow(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val r = st.rect(1.47f, 0.15f, 1.89f, 0.46f)
    clipPath(Path().apply { addRect(r) }) {
        drawRect(Brush.verticalGradient(listOf(lerp(Color(0xFF7CC4F2), Color(0xFF1B1850), n), lerp(Color(0xFFD6F0FF), Color(0xFF4B3A8E), n)), startY = r.top, endY = r.bottom), r.topLeft, r.size)
        val trunkX = st.px(1.72f, 0.7f)
        drawRect(Color(0xFF8A5A3A).atNight(n, 0.5f), Offset(trunkX - 0.018f * u, r.top + r.height * 0.45f), Size(0.036f * u, r.height))
        drawPath(crownPath(trunkX, r.top + r.height * 0.35f, 0.18f * u, 0.14f * u, 971), Color(0xFF6DBB5A).atNight(n, 0.5f))
        drawPath(crownPath(trunkX - 0.08f * u, r.top + r.height * 0.3f, 0.09f * u, 0.07f * u, 972), Color(0xFF86CC66).atNight(n, 0.5f))
        drawRect(Color(0xFF8CC96A).atNight(n, 0.5f), Offset(r.left, r.bottom - 0.03f * u), Size(r.width, 0.03f * u))
        val by = r.top + r.height * 0.2f + sin(pen.t * 1.3f) * 0.005f * u
        val bx = r.left + r.width * (0.2f + 0.05f * sin(pen.t * 0.4f))
        drawGull(Offset(bx, by), 0.012f * u, 0.5f + 0.5f * sin(pen.t * 6f), Stroke(pen.lw, cap = StrokeCap.Round), 1f - n)
    }
    val frame = Color.White
    drawRect(frame, r.topLeft, r.size, style = Stroke(0.014f * u))
    drawLine(frame, Offset(r.center.x, r.top), Offset(r.center.x, r.bottom), strokeWidth = 0.01f * u)
    drawLine(frame, Offset(r.left, r.center.y), Offset(r.right, r.center.y), strokeWidth = 0.01f * u)
    drawRect(Ink.line, Offset(r.left - 0.007f * u, r.top - 0.007f * u), Size(r.width + 0.014f * u, r.height + 0.014f * u), style = pen.stroke)
    for (side in intArrayOf(-1, 1)) {
        val x0 = if (side < 0) r.left - 0.04f * u else r.right + 0.04f * u
        val x1 = if (side < 0) r.left + 0.05f * u else r.right - 0.05f * u
        val curtain = Path().apply {
            moveTo(x0, r.top - 0.03f * u)
            lineTo(x1, r.top - 0.03f * u)
            quadraticTo(mix(x0, x1, 0.2f), r.center.y, x0 + (x1 - x0) * 0.35f, r.bottom + 0.03f * u)
            lineTo(x0, r.bottom + 0.03f * u)
            close()
        }
        inked(curtain, Color(0xFFF7B8CC), pen)
    }
    drawLine(Ink.line, Offset(r.left - 0.06f * u, r.top - 0.03f * u), Offset(r.right + 0.06f * u, r.top - 0.03f * u), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
}

private fun DrawScope.poster(pen: Pen, r: Rect, bg: Color) {
    drawRect(Ink.shadow, Offset(r.left + r.width * 0.04f, r.top + r.width * 0.05f), r.size)
    drawRect(Color.White, r.topLeft, r.size)
    drawRect(bg, Offset(r.left + r.width * 0.07f, r.top + r.width * 0.07f), Size(r.width * 0.86f, r.height - r.width * 0.14f))
    drawRect(Ink.line, r.topLeft, r.size, style = pen.thin)
}

private fun DrawScope.smile(c: Offset, s: Float, pen: Pen) {
    drawPoints(listOf(Offset(c.x - s * 0.3f, c.y - s * 0.1f), Offset(c.x + s * 0.3f, c.y - s * 0.1f)), PointMode.Points, Ink.line, strokeWidth = s * 0.16f, cap = StrokeCap.Round)
    drawArc(Ink.line, 20f, 140f, false, Offset(c.x - s * 0.28f, c.y - s * 0.12f), Size(s * 0.56f, s * 0.4f), style = Stroke(pen.lw, cap = StrokeCap.Round))
    drawCircle(Ink.blush, s * 0.1f, Offset(c.x - s * 0.45f, c.y + s * 0.1f))
    drawCircle(Ink.blush, s * 0.1f, Offset(c.x + s * 0.45f, c.y + s * 0.1f))
}

/** A poster of a happy tooth with a sparkle. */
private fun DrawScope.posterTooth(st: Stage, pen: Pen, r: Rect) {
    poster(pen, r, Color(0xFFCDEBFF))
    val c = Offset(r.center.x, r.center.y)
    val s = r.width * 0.3f
    val tooth = Path().apply {
        moveTo(c.x - s, c.y - s * 0.5f)
        cubicTo(c.x - s, c.y - s * 1.3f, c.x - s * 0.2f, c.y - s * 1.1f, c.x, c.y - s * 0.9f)
        cubicTo(c.x + s * 0.2f, c.y - s * 1.1f, c.x + s, c.y - s * 1.3f, c.x + s, c.y - s * 0.5f)
        cubicTo(c.x + s, c.y + s * 0.3f, c.x + s * 0.7f, c.y + s * 1.3f, c.x + s * 0.4f, c.y + s * 1.2f)
        quadraticTo(c.x + s * 0.2f, c.y + s * 0.4f, c.x, c.y + s * 0.4f)
        quadraticTo(c.x - s * 0.2f, c.y + s * 0.4f, c.x - s * 0.4f, c.y + s * 1.2f)
        cubicTo(c.x - s * 0.7f, c.y + s * 1.3f, c.x - s, c.y + s * 0.3f, c.x - s, c.y - s * 0.5f)
        close()
    }
    inked(tooth, Color.White, pen)
    smile(Offset(c.x, c.y - s * 0.2f), s, pen)
    twinkle(Offset(c.x + s * 1.1f, c.y - s * 1.1f), s * 0.35f, Color(0xFFFFC83D))
}

/** A poster of a happy heart. */
private fun DrawScope.posterHeart(st: Stage, pen: Pen, r: Rect) {
    poster(pen, r, Color(0xFFFFE3EA))
    val c = Offset(r.center.x, r.center.y)
    val s = r.width * 0.34f
    val beat = 1f + 0.05f * sin(pen.t * 5f).let { if (it > 0.6f) it else 0f }
    val heart = Path().apply {
        moveTo(c.x, c.y + s * 0.95f * beat)
        cubicTo(c.x - s * 1.6f * beat, c.y - s * 0.05f, c.x - s * 0.7f * beat, c.y - s * 1.3f * beat, c.x, c.y - s * 0.45f)
        cubicTo(c.x + s * 0.7f * beat, c.y - s * 1.3f * beat, c.x + s * 1.6f * beat, c.y - s * 0.05f, c.x, c.y + s * 0.95f * beat)
        close()
    }
    inked(heart, Color(0xFFE94F6A), pen)
    smile(Offset(c.x, c.y - s * 0.05f), s * 0.8f, pen)
}

private fun DrawScope.wallClock(st: Stage, pen: Pen, c: Offset) {
    val u = st.u
    val r = 0.045f * u
    inkedCircle(c, r, Color.White, pen)
    drawCircle(Color(0xFF7FD3B0), r * 0.92f, c, style = Stroke(r * 0.12f))
    val ticks = ArrayList<Offset>(24)
    for (k in 0 until 12) {
        val a = k * 0.5236f
        ticks.add(Offset(c.x + sin(a) * r * 0.62f, c.y - cos(a) * r * 0.62f))
        ticks.add(Offset(c.x + sin(a) * r * 0.72f, c.y - cos(a) * r * 0.72f))
    }
    drawPoints(ticks, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 0.6f)
    val m = pen.t * 0.1f
    val hr = pen.t * 0.1f / 12f + 1.2f
    drawLine(Ink.line, c, Offset(c.x + sin(hr) * r * 0.4f, c.y - cos(hr) * r * 0.4f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    drawLine(Ink.line, c, Offset(c.x + sin(m) * r * 0.62f, c.y - cos(m) * r * 0.62f), strokeWidth = pen.lw, cap = StrokeCap.Round)
    drawCircle(Color(0xFFE94F6A), r * 0.08f, c)
}
