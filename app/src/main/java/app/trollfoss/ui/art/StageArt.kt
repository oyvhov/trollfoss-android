package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import kotlin.math.cos
import kotlin.math.sin

/*
 * The stage: a concert hall seen as a cut-away. Dark plum walls, red velvet curtains framing the stage
 * (the platform is a fixture), a starry backdrop, a lighting truss with spotlights whose coloured beams
 * sweep slowly, a neon note and a dark wooden floor. It glows more at night.
 */

private const val STAGE_L = 0.25f
private const val STAGE_R = 1.75f

private val beamColors = listOf(Color(0xFFFF5AC8), Color(0xFF5CE0FF), Color(0xFFFFC24D), Color(0xFFB983FF))

private class StageStatic(val planks: Planks, val stars: List<List<Offset>>, val slats: List<Offset>)

private val stageStatic = Memo { u ->
    val back = PlaceId.STAGE.back
    val stars = List(3) { ArrayList<Offset>(20) }
    for (i in 0 until 60) stars[i % 3].add(Offset((0.34f + hash01(i, 981) * 1.32f) * u, (0.14f + hash01(i, 982) * 0.6f) * u))
    val slats = ArrayList<Offset>()
    var x = 2.0f
    while (x < 3.6f) {
        slats.add(Offset(x * u, 0.12f * u))
        slats.add(Offset(x * u, (back - 0.03f) * u))
        x += 0.05f
    }
    StageStatic(buildPlanks(u, -0.3f, 3.8f, back, 0.09f, 83), stars, slats)
}

internal fun DrawScope.stageBack(st: Stage, pen: Pen, styles: List<RoomStyle> = emptyList()) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val back = PlaceId.STAGE.back
    val ss = stageStatic.of(u)
    val glow = 0.6f + 0.4f * n
    val paper = styles.wallOf(0)
    if (paper > 0) {
        paperWall(st, paper, back)
    } else {
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFF1B0D24), Color(0xFF3E1F4A)), startY = SKY_TOP * u, endY = back * u),
            Offset(0f, SKY_TOP * u), Size(st.w, (back - SKY_TOP) * u),
        )
    }
    // The starry backdrop behind the stage.
    if (st.sees(STAGE_L, STAGE_R)) {
        val bd = st.rect(0.3f, 0.1f, 1.7f, back)
        drawRect(Brush.verticalGradient(listOf(Color(0xFF12103A), Color(0xFF2A1A5E)), startY = bd.top, endY = bd.bottom), bd.topLeft, bd.size)
        inScene(st) {
            for ((g, pts) in ss.stars.withIndex()) {
                val a = glow * (0.55f + 0.45f * sin(t * (1.4f + g * 0.6f) + g * 2f))
                drawPoints(pts, PointMode.Points, Color(0xFFFFF3C4), strokeWidth = 0.02f * u, cap = StrokeCap.Round, alpha = 0.18f * a)
                drawPoints(pts, PointMode.Points, Color(0xFFFFF3C4), strokeWidth = 0.007f * u, cap = StrokeCap.Round, alpha = a)
            }
        }
    }
    // Acoustic panels on the house side.
    if (st.sees(1.95f, 3.6f)) {
        if (paper == 0) {
            drawRect(Color(0xFF4A2657), st.o(1.98f, 0.1f), Size(2f * u, (back - 0.12f) * u))
            inScene(st) { drawPoints(ss.slats, PointMode.Lines, Color(0xFF5E3470), strokeWidth = 0.02f * u) }
            drawLine(Ink.line, st.o(1.98f, 0.1f), st.o(3.9f, 0.1f), strokeWidth = pen.lw * 0.7f)
        }
        houseWall(st, pen)
    }
    skirting(st, pen, back, Color(0xFF2A1433))
    curtain(st, pen, left = true)
    curtain(st, pen, left = false)
    valance(st, pen)

    if (styles.floorOf(0) > 0) layFloor(st, styles.floorOf(0), back) else plankFloor(st, pen, ss.planks, back, Color(0xFF6A4430))
    wallShadow(st, back)
    truss(st, pen)
    beams(st, pen)
    drawBase(st, pen, Color(0xFF8A5A3E), Color(0xFF2E1A1F))
}

/** One of the big red velvet curtains, gathered by a golden rope. */
private fun DrawScope.curtain(st: Stage, pen: Pen, left: Boolean) {
    fun mx(x: Float) = if (left) x else 2.0f - x
    if (!st.sees(mx(0.02f).coerceAtMost(mx(0.36f)), mx(0.02f).coerceAtLeast(mx(0.36f)))) return
    val u = st.u
    val back = PlaceId.STAGE.back
    val path = Path().apply {
        moveTo(st.x(mx(0.0f)), SKY_TOP * u)
        lineTo(st.x(mx(0.36f)), SKY_TOP * u)
        quadraticTo(st.x(mx(0.35f)), 0.34f * u, st.x(mx(0.22f)), 0.52f * u)
        quadraticTo(st.x(mx(0.26f)), 0.66f * u, st.x(mx(0.33f)), back * u)
        lineTo(st.x(mx(0.0f)), back * u)
        close()
    }
    drawPath(path, Brush.horizontalGradient(listOf(Color(0xFF8E0E26), Color(0xFFD8283F), Color(0xFFA3122C), Color(0xFFE23A4E), Color(0xFF9C1029)), startX = st.x(mx(0f)), endX = st.x(mx(0.36f))))
    val folds = Path()
    for (k in 1..4) {
        val f = k / 5f
        val topX = mix(0.0f, 0.36f, f)
        val tieX = mix(0.0f, 0.22f, f)
        val botX = mix(0.0f, 0.33f, f)
        folds.moveTo(st.x(mx(topX)), SKY_TOP * u)
        folds.quadraticTo(st.x(mx(mix(topX, tieX, 0.3f))), 0.3f * u, st.x(mx(tieX)), 0.52f * u)
        folds.quadraticTo(st.x(mx(mix(tieX, botX, 0.4f))), 0.66f * u, st.x(mx(botX)), back * u)
    }
    drawPath(folds, Color(0xFF6E0A1E), alpha = 0.6f, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
    drawPath(path, Ink.line, style = pen.stroke)
    val rope = Path().apply {
        moveTo(st.x(mx(0.0f)), 0.5f * u)
        quadraticTo(st.x(mx(0.12f)), 0.545f * u, st.x(mx(0.23f)), 0.52f * u)
    }
    drawPath(rope, Ink.line, style = Stroke(0.016f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(rope, Color(0xFFFFC24D), style = Stroke(0.016f * u, cap = StrokeCap.Round))
    val tassel = Path().apply {
        val c = st.o(mx(0.21f), 0.53f)
        poly(c.x - 0.01f * u, c.y, c.x + 0.01f * u, c.y, c.x + 0.02f * u, c.y + 0.07f * u, c.x - 0.02f * u, c.y + 0.07f * u)
    }
    inked(tassel, Color(0xFFFFC24D), pen, shade = false)
}

/** The swagged pelmet across the top of the stage, with a golden fringe. */
private fun DrawScope.valance(st: Stage, pen: Pen) {
    if (!st.sees(0f, 2.0f)) return
    val u = st.u
    val swags = 3
    val path = Path()
    path.moveTo(st.x(0f), SKY_TOP * u)
    path.lineTo(st.x(2.0f), SKY_TOP * u)
    path.lineTo(st.x(2.0f), 0.1f * u)
    val edge = Path()
    edge.moveTo(st.x(2.0f), 0.1f * u)
    for (k in swags - 1 downTo 0) {
        val a = k * 2.0f / swags
        val b = (k + 1) * 2.0f / swags
        path.quadraticTo(st.x((a + b) / 2f), 0.19f * u, st.x(a), 0.1f * u)
        edge.quadraticTo(st.x((a + b) / 2f), 0.19f * u, st.x(a), 0.1f * u)
    }
    path.close()
    drawPath(path, Brush.verticalGradient(listOf(Color(0xFF7A0B20), Color(0xFFC81E36)), startY = 0f, endY = 0.15f * u))
    drawPath(edge, Color(0xFFFFC24D), style = Stroke(0.02f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.006f * u, 0.004f * u))))
    drawPath(edge, Color(0xFFFFC24D), style = Stroke(0.006f * u, cap = StrokeCap.Round))
    drawPath(path, Ink.line, style = pen.stroke)
}

/** The lighting truss across the top, with spotlights hanging from it. */
private fun DrawScope.truss(st: Stage, pen: Pen) {
    val u = st.u
    val metal = Color(0xFF9AA0AE)
    val y0 = 0.02f
    val y1 = 0.05f
    val a = st.cam - 0.2f
    val b = st.cam + st.vw + 0.2f
    val zig = ArrayList<Offset>(60)
    var x = a - wrap(a, 0.06f)
    var up = true
    while (x < b) {
        zig.add(st.o(x, if (up) y0 else y1))
        zig.add(st.o(x + 0.06f, if (up) y1 else y0))
        up = !up
        x += 0.06f
    }
    drawPoints(zig, PointMode.Lines, metal.darken(0.2f), strokeWidth = pen.lw * 0.9f)
    for (y in floatArrayOf(y0, y1)) {
        drawLine(Ink.line, Offset(0f, y * u), Offset(st.w, y * u), strokeWidth = pen.lw * 2.6f)
        drawLine(metal, Offset(0f, y * u), Offset(st.w, y * u), strokeWidth = pen.lw * 1.4f)
    }
}

private val spotX = floatArrayOf(0.42f, 0.78f, 1.22f, 1.58f, 2.35f, 2.95f)

/** Spotlight cans and their coloured beams sweeping slowly over the stage. */
private fun DrawScope.beams(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val strength = 0.1f + 0.14f * n
    for ((i, sx) in spotX.withIndex()) {
        if (!st.sees(sx - 0.6f, sx + 0.6f)) continue
        val col = beamColors[i % beamColors.size]
        val onStage = sx < 2f
        val targetX = if (onStage) 1.0f + (sx - 1.0f) * 0.5f + sin(t * 0.45f + i * 1.7f) * 0.3f else sx - 0.35f + sin(t * 0.4f + i) * 0.15f
        val targetY = if (onStage) 0.72f else 0.9f
        val lens = st.o(sx, 0.085f)
        val spread = 0.11f * u
        val end = st.o(targetX, targetY)
        val beam = Path().apply {
            moveTo(lens.x - 0.012f * u, lens.y)
            lineTo(end.x - spread, end.y)
            lineTo(end.x + spread, end.y)
            lineTo(lens.x + 0.012f * u, lens.y)
            close()
        }
        drawPath(beam, Brush.verticalGradient(listOf(col.copy(alpha = strength * 1.6f), col.copy(alpha = strength * 0.5f)), startY = lens.y, endY = end.y))
        drawOval(col, Offset(end.x - spread, end.y - 0.015f * u), Size(spread * 2f, 0.03f * u), alpha = strength * 1.5f)
        // The can, tilted toward its beam.
        val ang = Math.toDegrees(kotlin.math.atan2((end.x - lens.x).toDouble(), (end.y - lens.y).toDouble())).toFloat()
        rotate(-ang, st.o(sx, 0.055f)) {
            val can = Rect(lens.x - 0.018f * u, 0.052f * u, lens.x + 0.018f * u, 0.09f * u)
            inkedRound(can, 0.006f * u, Color(0xFF3B3346), pen, shade = false)
            drawOval(col.lighten(0.5f), Offset(can.left + 0.004f * u, can.bottom - 0.008f * u), Size(can.width - 0.008f * u, 0.01f * u))
        }
        drawCircle(col, 0.02f * u, lens, alpha = 0.35f + 0.3f * n)
    }
}

/** The audience side: a door with a green way-out sign, wall lamps and a neon music note. */
private fun DrawScope.houseWall(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val back = PlaceId.STAGE.back
    if (st.sees(2.4f, 2.75f)) {
        val door = st.rect(2.46f, 0.42f, 2.72f, back)
        inkedRound(door, 0.006f * u, Color(0xFF2B1633), pen, shade = false)
        drawLine(Color(0xFF6B4A7A), Offset(door.center.x, door.top), Offset(door.center.x, door.bottom), strokeWidth = pen.lw)
        val sign = st.rect(2.52f, 0.36f, 2.66f, 0.4f)
        drawRect(Color(0xFF2FD18B), sign.topLeft, sign.size)
        val arrow = Path().apply {
            moveTo(sign.left + sign.width * 0.25f, sign.center.y)
            lineTo(sign.right - sign.width * 0.25f, sign.center.y)
            moveTo(sign.right - sign.width * 0.38f, sign.top + sign.height * 0.25f)
            lineTo(sign.right - sign.width * 0.25f, sign.center.y)
            lineTo(sign.right - sign.width * 0.38f, sign.bottom - sign.height * 0.25f)
        }
        drawPath(arrow, Color.White, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawRect(Ink.line, sign.topLeft, sign.size, style = pen.thin)
    }
    for (x in floatArrayOf(2.28f, 3.1f)) {
        if (!st.sees(x - 0.1f, x + 0.1f)) continue
        val c = st.o(x, 0.42f)
        drawCircle(Brush.radialGradient(listOf(Color(0x66FFD27A), Color(0x00FFD27A)), center = c, radius = 0.12f * u), 0.12f * u, c, alpha = 0.6f + 0.4f * n)
        val shade = Path().apply { poly(c.x - 0.03f * u, c.y, c.x + 0.03f * u, c.y, c.x + 0.018f * u, c.y + 0.035f * u, c.x - 0.018f * u, c.y + 0.035f * u) }
        inked(shade, Color(0xFFFFC24D), pen, shade = false)
    }
    if (st.sees(2.6f, 3.0f)) {
        val c = st.o(2.82f, 0.24f)
        val s = 0.06f * u
        val note = Path().apply {
            moveTo(c.x - s * 0.5f, c.y + s * 0.6f)
            lineTo(c.x - s * 0.5f, c.y - s * 0.8f)
            lineTo(c.x + s * 0.6f, c.y - s)
            lineTo(c.x + s * 0.6f, c.y + s * 0.4f)
            addOval(Rect(Offset(c.x - s * 0.72f, c.y + s * 0.62f), s * 0.24f))
            addOval(Rect(Offset(c.x + s * 0.38f, c.y + s * 0.42f), s * 0.24f))
        }
        val flick = 0.85f + 0.15f * sin(pen.t * 13f).let { if (it > 0.9f) -1f else 1f }
        drawPath(note, Color(0xFFFF5AC8), alpha = (0.3f + 0.3f * n) * flick, style = Stroke(0.02f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(note, Color(0xFFFFD1F2), alpha = flick, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
