package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.GroundRules
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The dining room of Storstova in oblique 3D: the long table that lays itself, the chairs behind and beside
// it, the service bell, the birthday cake, the candelabra and the sideboard.

// ------------------------------------------------------------------------------------------------ the table

/** How laid the table is: 0 bare, 1 set; moves with [Fixture.timer] while the plates fly in or home. */
private fun tableAppear(f: Fixture): Float {
    val p = if (f.timer > 0f) (f.timer / GroundRules.TABLE_SECONDS).coerceIn(0f, 1f) else 0f
    return if (f.mode == 1) 1f - p else p
}

internal fun DrawScope.grDiningTable(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.2f
    val top = -0.13f
    val cloth = Color(0xFFFBF6EA)
    val trim = GrC.brass
    grShadow(u, 1.06f, d + 0.02f)
    // The tablecloth: a lit top, a front that hangs in folds with a scalloped gold edge, and a short drop at each end.
    fxFace(fxDeep(u, 0.5f, top, -0.035f, 0f, d), cloth.darken(0.12f), pen)
    fxFace(fxFlat(u, -0.5f, 0.5f, top, 0f, d), cloth.lighten(0.2f), pen)
    val front = Path().apply {
        moveTo(-0.5f * u, top * u)
        lineTo(0.5f * u, top * u)
        lineTo(0.5f * u, -0.035f * u)
        var x = 0.5f
        while (x > -0.5f + 0.001f) {
            val nx = max(-0.5f, x - 0.05f)
            quadraticTo(((x + nx) / 2f) * u, -0.012f * u, nx * u, -0.035f * u)
            x = nx
        }
        close()
    }
    inked(front, cloth, pen)
    clipPath(front) {
        for (k in 1 until 20) {
            val x = -0.5f + k * 0.05f
            drawLine(cloth.darken(0.1f), Offset(x * u, top * u), Offset((x + 0.004f * (if (k % 2 == 0) 1 else -1)) * u, -0.03f * u), pen.lw * 0.5f)
        }
        drawLine(trim, Offset(-0.5f * u, (top + 0.012f) * u), Offset(0.5f * u, (top + 0.012f) * u), pen.lw * 1.2f)
        drawLine(trim, Offset(-0.5f * u, (top + 0.02f) * u), Offset(0.5f * u, (top + 0.02f) * u), pen.lw * 0.6f)
        // Gold dots along the scallops.
        var x = -0.475f
        while (x < 0.5f) {
            drawCircle(trim, 0.0028f * u, Offset(x * u, -0.03f * u))
            x += 0.05f
        }
    }
    // A little lace on the edge of the top, and a burgundy runner.
    val runner = fxFlat(u, -0.45f, 0.45f, top - 0.0004f, 0.095f, 0.125f, 0.001f)
    drawPath(runner, GrC.burgundy.copy(alpha = 0.9f))
    drawPath(runner, GrC.gold, style = Stroke(pen.lw * 0.5f))
    // Two little posies.
    for (s in -1..1 step 2) {
        val v = q(s * 0.27f, top, 0.15f)
        inkedRound(Rect(v.x - 0.01f * u, v.y - 0.02f * u, v.x + 0.01f * u, v.y), 0.004f * u, Color(0xFF8DC9C4), pen, shade = false)
        for (k in -1..1) {
            val c = Offset(v.x + k * 0.01f * u, v.y - 0.034f * u - (k % 2) * 0.004f * u)
            drawLine(Color(0xFF2E8B57), Offset(v.x, v.y - 0.018f * u), c, pen.lw * 0.9f)
            inkedCircle(c, 0.007f * u, listOf(Color(0xFFFF8FB1), Color(0xFFFFC83D), Color(0xFFB57BFF))[(k + 1 + (if (s > 0) 1 else 0)) % 3], pen, shade = false)
        }
    }
    // The six place settings, flying in from both sides when the bell rings.
    val appear = tableAppear(f)
    if (appear > 0f) {
        for (i in 0 until 6) {
            val pr = ((appear * 1.7f) - i * 0.13f).coerceIn(0f, 1f)
            if (pr <= 0f) continue
            val e = pr * pr * (3f - 2f * pr)
            val fx = -0.4f + i * 0.16f
            val fromX = if (i < 3) -0.85f else 0.85f
            val x = mix(fromX, fx, e)
            val z = 0.085f
            val lift = sin(e * PI.toFloat()) * 0.16f * (1f - e)
            val c = q(x, top, z)
            val pc = Offset(c.x, c.y - lift * u)
            grPlate(pc, u, pen, i, e, pr < 1f, if (pr < 1f) pen.t * 14f + i else 0f)
        }
    }
}

/** A place setting at pixel [c]: gold-rimmed plate, folded napkin, fork and knife, a glass. */
private fun DrawScope.grPlate(c: Offset, u: Float, pen: Pen, i: Int, e: Float, flying: Boolean, spin: Float) {
    val napkin = intArrayOf(0xFF8E2F3E.toInt(), 0xFF2D6C73.toInt(), 0xFFD9A93E.toInt())[i % 3]
    // Fork and knife.
    if (!flying) {
        drawLine(Ink.line, Offset(c.x - 0.052f * u, c.y + 0.003f * u), Offset(c.x - 0.052f * u, c.y - 0.014f * u), 0.004f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(GrC.silver, Offset(c.x - 0.052f * u, c.y + 0.003f * u), Offset(c.x - 0.052f * u, c.y - 0.014f * u), 0.004f * u, StrokeCap.Round)
        drawLine(Ink.line, Offset(c.x + 0.052f * u, c.y + 0.003f * u), Offset(c.x + 0.052f * u, c.y - 0.016f * u), 0.004f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(GrC.silver, Offset(c.x + 0.052f * u, c.y + 0.003f * u), Offset(c.x + 0.052f * u, c.y - 0.016f * u), 0.004f * u, StrokeCap.Round)
    }
    val plate = fxDisc2(c.x, c.y - 0.002f * u, 0.039f * u, 0.032f * u)
    drawPath(plate, Ink.shadow, style = Stroke(0.004f * u))
    drawPath(plate, Color(0xFFFFFDF6))
    drawPath(plate, GrC.brass, style = Stroke(pen.lw * 0.8f))
    drawPath(plate, Ink.line, style = pen.thin)
    drawPath(fxDisc2(c.x, c.y - 0.002f * u, 0.026f * u, 0.021f * u), Color(0xFFEDE5D0), style = Stroke(pen.lw * 0.5f))
    // A glint that turns with the spin of the plate.
    val g = Offset(c.x + cos(spin) * 0.022f * u, c.y - 0.004f * u + sin(spin) * 0.008f * u)
    drawCircle(Color.White, 0.004f * u, g)
    // The napkin, folded as a triangle, standing in the plate.
    val nap = Path().apply {
        moveTo(c.x - 0.014f * u, c.y - 0.002f * u)
        lineTo(c.x + 0.0f, c.y - 0.03f * u)
        lineTo(c.x + 0.014f * u, c.y - 0.002f * u)
        close()
    }
    drawPath(nap, Color(napkin))
    drawPath(nap, Ink.line, style = pen.thin)
    // A stemmed glass at the upper right.
    val gc = Offset(c.x + 0.06f * u, c.y - 0.036f * u)
    drawLine(Ink.line, Offset(gc.x, gc.y), Offset(gc.x, gc.y + 0.02f * u), pen.lw * 1.2f)
    drawOval(Color(0xAAE6F4FB), Offset(gc.x - 0.012f * u, gc.y + 0.018f * u), Size(0.024f * u, 0.006f * u))
    val bowl = Path().apply {
        moveTo(gc.x - 0.012f * u, gc.y - 0.022f * u)
        lineTo(gc.x + 0.012f * u, gc.y - 0.022f * u)
        quadraticTo(gc.x + 0.012f * u, gc.y, gc.x, gc.y + 0.002f * u)
        quadraticTo(gc.x - 0.012f * u, gc.y, gc.x - 0.012f * u, gc.y - 0.022f * u)
        close()
    }
    drawPath(bowl, Color(0xAAE6F4FB))
    drawPath(Path().apply { moveTo(gc.x - 0.011f * u, gc.y - 0.01f * u); lineTo(gc.x + 0.011f * u, gc.y - 0.01f * u); lineTo(gc.x + 0.008f * u, gc.y - 0.0f); lineTo(gc.x - 0.008f * u, gc.y - 0.0f); close() }, Color(if (i % 2 == 0) 0xFFE8473F.toInt() else 0xFF7FD3E8.toInt()), alpha = 0.7f)
    drawPath(bowl, Ink.line, style = pen.thin)
    shine(Offset(gc.x - 0.005f * u, gc.y - 0.014f * u), 0.003f * u, 0.012f * u, 0.8f)
}

// ------------------------------------------------------------------------------------------------ chairs

/** One tall upholstered dining chair seen from the front, standing on [cx] pixels; [fabric] is the cushion. */
private fun DrawScope.grHighChair(cx: Float, u: Float, pen: Pen, fabric: Color, w: Float, h: Float) {
    val wood = GrC.walnut
    val half = w / 2f
    // Legs, a seat rail, the carved back with its padded oval.
    for (s in -1..1 step 2) {
        inkedRound(Rect(cx + (s * (half - 0.006f) - 0.005f) * u, -0.075f * u, cx + (s * (half - 0.006f) + 0.005f) * u, 0f), 0.002f * u, wood.darken(0.1f), pen, shade = false)
    }
    inkedRound(Rect(cx - half * u, -0.095f * u, cx + half * u, -0.072f * u), 0.003f * u, wood, pen)
    // The back posts and the crest.
    for (s in -1..1 step 2) {
        inkedRound(Rect(cx + (s * (half - 0.008f) - 0.006f) * u, -h * u, cx + (s * (half - 0.008f) + 0.006f) * u, -0.09f * u), 0.003f * u, wood, pen)
    }
    val crest = Path().apply {
        moveTo(cx - half * u, (-h + 0.04f) * u)
        quadraticTo(cx - half * u, -h * u, cx, (-h - 0.008f) * u)
        quadraticTo(cx + half * u, -h * u, cx + half * u, (-h + 0.04f) * u)
        lineTo(cx + half * u, (-h + 0.052f) * u)
        lineTo(cx - half * u, (-h + 0.052f) * u)
        close()
    }
    inked(crest, wood.lighten(0.06f), pen)
    grKnob(Offset(cx, (-h + 0.012f) * u), 0.005f * u, pen)
    val pad = Rect(cx - (half - 0.016f) * u, (-h + 0.062f) * u, cx + (half - 0.016f) * u, -0.108f * u)
    inkedRound(pad, 0.014f * u, fabric, pen)
    for (k in 0..1) for (j in 0..1) drawCircle(fabric.darken(0.4f), 0.002f * u, Offset(pad.left + pad.width * (0.3f + 0.4f * k), pad.top + pad.height * (0.35f + 0.3f * j)))
    // A seat cushion edge in front.
    inkedRound(Rect(cx - (half - 0.004f) * u, -0.108f * u, cx + (half - 0.004f) * u, -0.086f * u), 0.006f * u, fabric.lighten(0.08f), pen)
}

internal fun DrawScope.grChairRow(f: Fixture, u: Float, pen: Pen) {
    grShadow(u, 0.74f, 0.1f, 0.7f)
    val fab = arrayOf(Color(0xFF8E2F3E), Color(0xFF2D6C73), Color(0xFFD9A93E), Color(0xFF8E2F3E))
    for (i in 0 until 4) grHighChair((-0.27f + i * 0.18f) * u, u, pen, fab[i], 0.15f, 0.26f)
}

internal fun DrawScope.grDiningChair(f: Fixture, u: Float, pen: Pen) {
    grShadow(u, 0.12f, 0.08f)
    grHighChair(0f, u, pen, if (f.variant == 0) Color(0xFF2D6C73) else Color(0xFF8E2F3E), 0.1f, 0.24f)
}

// ------------------------------------------------------------------------------------------------ bell and cake

internal fun DrawScope.grBell(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val ring = f.anim.coerceIn(0f, 1f)
    val wob = sin(pen.t * 40f) * 0.8f * ring
    // A brass service bell: round dome on a flat base, a push button on top that dips when it rings.
    inkedRound(Rect(-0.026f * u, -0.008f * u, 0.026f * u, 0f), 0.003f * u, GrC.brassDark, pen)
    rotate(wob, p(0f, -0.008f)) {
        val dome = Path().apply {
            moveTo(-0.024f * u, -0.008f * u)
            quadraticTo(-0.024f * u, -0.04f * u, 0f, -0.04f * u)
            quadraticTo(0.024f * u, -0.04f * u, 0.024f * u, -0.008f * u)
            close()
        }
        inked(dome, GrC.brass, pen)
        shine(p(-0.011f, -0.026f), 0.007f * u, 0.014f * u, 0.8f)
        inkedRound(Rect(-0.004f * u, (-0.052f + 0.006f * ring) * u, 0.004f * u, (-0.04f + 0.006f * ring) * u), 0.002f * u, GrC.brassDark, pen, shade = false)
    }
    if (ring > 0.05f) {
        twinkle(p(0.034f, -0.046f), 0.016f * u * ring, Color.White, ring)
        for (s in -1..1 step 2) drawArc(Color.White.copy(alpha = 0.7f * ring), if (s < 0) 200f else 320f, 30f, false, p(-0.04f - (1f - ring) * 0.02f, -0.075f), Size(0.08f * u + (1f - ring) * 0.04f * u, 0.08f * u), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    }
}

internal fun DrawScope.grCake(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val lit = f.on
    val singing = f.mode == 1
    val t = pen.t
    // A footed glass stand, a big pink tier and a smaller cream one, berries and frosting.
    drawLine(Ink.line, p(0f, -0.02f), p(0f, -0.006f), 0.01f * u + pen.lw * 2f)
    drawLine(Color(0xFFE6F4FB), p(0f, -0.02f), p(0f, -0.006f), 0.01f * u)
    inkedOval(Rect(-0.034f * u, -0.01f * u, 0.034f * u, 0.002f * u), Color(0xFFE6F4FB), pen, shade = false)
    inkedOval(Rect(-0.04f * u, -0.03f * u, 0.04f * u, -0.016f * u), Color(0xFFE6F4FB), pen, shade = false)
    // The bottom tier.
    val low = Rect(-0.04f * u, -0.066f * u, 0.04f * u, -0.022f * u)
    inkedRound(low, 0.008f * u, Color(0xFFFFB3C7), pen)
    val frostL = Path().apply {
        moveTo(low.left, low.top + 0.008f * u)
        var x = low.left
        while (x < low.right) {
            quadraticTo(x + 0.008f * u, low.top + 0.026f * u, x + 0.016f * u, low.top + 0.008f * u)
            x += 0.016f * u
        }
        lineTo(low.right, low.top)
        lineTo(low.left, low.top)
        close()
    }
    drawPath(frostL, Color(0xFFFFF8EA))
    drawPath(frostL, Ink.line, style = pen.thin)
    for (k in 0..4) drawCircle(Color(0xFFD2443A), 0.003f * u, Offset(low.left + (0.008f + k * 0.016f) * u, low.bottom - 0.01f * u))
    // The top tier.
    val up = Rect(-0.026f * u, -0.098f * u, 0.026f * u, -0.066f * u)
    inkedRound(up, 0.007f * u, Color(0xFFFFF1D6), pen)
    val frostU = Path().apply {
        moveTo(up.left, up.top + 0.006f * u)
        var x = up.left
        while (x < up.right - 0.001f * u) {
            quadraticTo(x + 0.0065f * u, up.top + 0.02f * u, x + 0.013f * u, up.top + 0.006f * u)
            x += 0.013f * u
        }
        lineTo(up.right, up.top)
        lineTo(up.left, up.top)
        close()
    }
    drawPath(frostU, Color(0xFFFFB3C7))
    drawPath(frostU, Ink.line, style = pen.thin)
    for (k in 0..6) drawCircle(listOf(Color(0xFF8E2F3E), Color(0xFF2F6FB8), Color(0xFFFFC83D))[k % 3], 0.0018f * u, Offset(up.left + (0.005f + k * 0.007f) * u, up.bottom - 0.01f * u - (k % 2) * 0.006f * u))
    // Candles with flames, or with little wisps of smoke when they have been blown out.
    for (k in 0 until 5) {
        val cx = (-0.02f + k * 0.01f)
        val cs = Offset(cx * u, up.top)
        val col = listOf(Color(0xFFFF4D6D), Color(0xFF4FB3F0), Color(0xFFFFC83D), Color(0xFF6FD08C), Color(0xFFB57BFF))[k]
        inkedRound(Rect(cs.x - 0.0026f * u, cs.y - 0.024f * u, cs.x + 0.0026f * u, cs.y), 0.001f * u, col, pen, shade = false)
        if (lit) {
            val big = if (singing) 1.25f else 1f
            grFlame(Offset(cs.x, cs.y - 0.024f * u), 0.017f * u * big, pen, k * 1.7f)
        } else {
            val ph = fxFrac(t * 0.5f + k * 0.2f)
            if (f.count > 0) drawCircle(Color(0xFFC8C4D8).copy(alpha = 0.5f * (1f - ph)), 0.004f * u * (1f + ph * 2f), Offset(cs.x + sin(ph * 6f + k) * 0.004f * u, cs.y - 0.03f * u - ph * 0.04f * u))
        }
    }
    if (lit) grGlow(p(0f, -0.1f), 0.12f * u, pen, 0.25f)
    if (singing) {
        for (k in 0 until 3) {
            val ph = fxFrac(t * 0.8f + k * 0.33f)
            fxNote(p(0.05f + ph * 0.04f, -0.12f - ph * 0.08f), 0.008f * u, listOf(Color(0xFFD2443A), Color(0xFF2F6FB8), Color(0xFF3F9A55))[k], 1f - ph)
        }
    }
}

// ------------------------------------------------------------------------------------------------ the candelabra

internal fun DrawScope.grCandelabra(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val on = f.on
    grShadow(u, 0.1f, 0.07f)
    if (on) grGlow(p(0f, -0.3f), 0.32f * u, pen, 0.3f)
    // A round foot, a stem of knobs and cups, and five arms with candles.
    inkedOval(Rect(-0.04f * u, -0.016f * u, 0.04f * u, 0.002f * u), GrC.brass, pen)
    val stem = Path().apply {
        moveTo(-0.01f * u, -0.014f * u)
        quadraticTo(-0.022f * u, -0.05f * u, -0.008f * u, -0.09f * u)
        quadraticTo(-0.004f * u, -0.14f * u, -0.012f * u, -0.2f * u)
        lineTo(0.012f * u, -0.2f * u)
        quadraticTo(0.004f * u, -0.14f * u, 0.008f * u, -0.09f * u)
        quadraticTo(0.022f * u, -0.05f * u, 0.01f * u, -0.014f * u)
        close()
    }
    inked(stem, GrC.brass, pen)
    for (y in floatArrayOf(-0.05f, -0.11f, -0.17f)) grKnob(p(0f, y), 0.012f * u, pen)
    for (a in -2..2) {
        val ex = a * 0.032f
        val ey = -0.2f - (if (a == 0) 0.08f else 0.04f - kotlin.math.abs(a) * 0.01f + 0.04f)
        val arm = Path().apply {
            moveTo(0f, -0.15f * u)
            quadraticTo(ex * 0.9f * u, -0.17f * u, ex * u, (ey + 0.02f) * u)
        }
        if (a != 0) {
            drawPath(arm, Ink.line, style = Stroke(0.007f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(arm, GrC.brass, style = Stroke(0.007f * u, cap = StrokeCap.Round))
        }
        val cy = if (a == 0) -0.2f else ey + 0.02f
        val cx = if (a == 0) 0f else ex
        inkedRound(Rect((cx - 0.008f) * u, (cy - 0.004f) * u, (cx + 0.008f) * u, (cy + 0.008f) * u), 0.002f * u, GrC.brass, pen, shade = false)
        inkedRound(Rect((cx - 0.0045f) * u, (cy - 0.04f) * u, (cx + 0.0045f) * u, (cy - 0.004f) * u), 0.0015f * u, GrC.ivory, pen, shade = false)
        if (on) grFlame(p(cx, cy - 0.04f), 0.022f * u, pen, a * 1.9f + 3f)
    }
}

// ------------------------------------------------------------------------------------------------ the sideboard

internal fun DrawScope.grSideboard(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.14f
    val wood = GrC.walnutLight
    grShadow(u, 0.36f, d)
    for ((x, z) in listOf(-0.16f to 0.02f, 0.16f to 0.02f, -0.16f to d - 0.02f, 0.16f to d - 0.02f)) {
        capsule(q(x, -0.03f, z), q(x * 1.04f, 0f, z), 0.008f * u, GrC.walnutDark, pen)
    }
    fxBox(u, -0.18f, -0.235f, 0.18f, -0.03f, d, wood, pen, rad = 0.005f)
    // Marble top with a lip.
    fxBox(u, -0.19f, -0.26f, 0.19f, -0.235f, d + 0.014f, GrC.marble, pen, rad = 0.004f, z = -0.006f, top = Color.White, side = GrC.marbleDark)
    // Decorations on top: two decanters, a framed photograph, a lace mat.
    for ((x, c) in listOf(-0.11f to Color(0xFFD9A93E), -0.07f to Color(0xFF8E2F3E))) {
        val b = q(x, -0.26f, 0.06f)
        val dec = Path().apply {
            moveTo(b.x - 0.012f * u, b.y)
            quadraticTo(b.x - 0.014f * u, b.y - 0.02f * u, b.x - 0.004f * u, b.y - 0.032f * u)
            lineTo(b.x - 0.004f * u, b.y - 0.05f * u)
            lineTo(b.x + 0.004f * u, b.y - 0.05f * u)
            lineTo(b.x + 0.004f * u, b.y - 0.032f * u)
            quadraticTo(b.x + 0.014f * u, b.y - 0.02f * u, b.x + 0.012f * u, b.y)
            close()
        }
        drawPath(dec, c.copy(alpha = 0.8f))
        drawPath(dec, Ink.line, style = pen.thin)
        drawCircle(GrC.brass, 0.005f * u, Offset(b.x, b.y - 0.054f * u))
        shine(Offset(b.x - 0.005f * u, b.y - 0.018f * u), 0.003f * u, 0.014f * u, 0.7f)
    }
    val ph = q(0.09f, -0.26f, 0.06f)
    val frame = Rect(ph.x - 0.025f * u, ph.y - 0.052f * u, ph.x + 0.025f * u, ph.y)
    val inner = grFrame(frame, pen, GrC.brass, Color(0xFFD7F2E8))
    drawCircle(Color(0xFFF9D0B0), 0.008f * u, Offset(inner.center.x - 0.004f * u, inner.center.y - 0.004f * u))
    drawCircle(Color(0xFFF2C14E), 0.008f * u, Offset(inner.center.x + 0.006f * u, inner.center.y - 0.006f * u))
    drawLine(Color(0xFF3B6EA5), Offset(inner.left, inner.bottom - 0.008f * u), Offset(inner.right, inner.bottom - 0.008f * u), pen.lw * 2f)
    // The doors, shut or swung open with plates stacked inside.
    val doorT = -0.215f
    val doorB = -0.045f
    if (f.open) {
        fxHollow(u, -0.15f, doorT, 0.15f, doorB, 0.1f, Color(0xFFE2BE8A), pen, back = Color(0xFF8A5A3C))
        fxInShelf(u, -0.15f, 0.15f, -0.115f, 0.1f, Color(0xFFC49A62), pen)
        for ((k, x) in listOf(-0.1f, -0.03f, 0.05f, 0.11f).withIndex()) {
            val b = q(x, -0.115f, 0.05f)
            for (s in 0 until 3) {
                drawPath(fxDisc2(b.x, b.y - s * 0.004f * u, 0.022f * u, 0.014f * u), Color(0xFFFFFDF6))
                drawPath(fxDisc2(b.x, b.y - s * 0.004f * u, 0.022f * u, 0.014f * u), Ink.line, style = pen.thin)
            }
            if (k == 1) drawPath(fxDisc2(b.x, b.y - 0.014f * u, 0.014f * u, 0.009f * u), GrC.brass, style = pen.thin)
        }
        fxOpenDoor(u, FixtureDoors.sideboardLeft, wood, wood.darken(0.1f), pen)
        fxOpenDoor(u, FixtureDoors.sideboardRight, wood, wood.darken(0.1f), pen)
    } else {
        for (s in 0..1) {
            val x0 = if (s == 0) -0.15f else 0.002f
            val r = Rect(x0 * u, doorT * u, (x0 + 0.148f) * u, doorB * u)
            inkedRound(r, 0.004f * u, wood.lighten(0.04f), pen, shade = false)
            val panel = Rect(r.left + 0.014f * u, r.top + 0.014f * u, r.right - 0.014f * u, r.bottom - 0.014f * u)
            drawRoundRect(wood.darken(0.25f), panel.topLeft, panel.size, androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
            // A carved garland: a small gilded flower in the middle of each door.
            val c = panel.center
            for (k in 0 until 5) {
                val a = k * 2f * PI.toFloat() / 5f
                drawCircle(GrC.gold, 0.005f * u, Offset(c.x + cos(a) * 0.012f * u, c.y + sin(a) * 0.012f * u))
            }
            drawCircle(GrC.burgundy, 0.005f * u, c)
            grKnob(Offset(if (s == 0) r.right - 0.012f * u else r.left + 0.012f * u, r.center.y), 0.006f * u, pen)
        }
    }
    // The apron below the doors.
    inkedRound(Rect(-0.18f * u, -0.04f * u, 0.18f * u, -0.03f * u), 0.003f * u, wood.darken(0.1f), pen, shade = false)
}
