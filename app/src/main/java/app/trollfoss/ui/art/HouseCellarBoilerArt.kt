package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * The boiler room: the big copper boiler with its pressure gauge, glowing fire door and safety valve, and the
 * three valve wheels on the pipes (yellow, green and blue). A turned-open valve hisses steam; with all three
 * open the boiler shudders and its gauge swings into the top.
 */

private val valveColors = listOf(Color(0xFFFFC83D), Color(0xFF3BC46B), Color(0xFF4D96FF))

// ============================================================================================ boiler

internal fun DrawScope.ceBoiler(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val rumble = f.timer > 0f
    val pressure = f.angle
    ceShadow(u, 0.34f, 0.2f)
    translate(if (rumble) f.bob * u else 0f, 0f) {
        // A heavy base with four feet.
        fxBox(u, -0.16f, -0.05f, 0.16f, 0f, 0.18f, CeC.iron, pen, rad = 0.005f, z = 0.01f, top = CeC.ironLight, side = CeC.iron.darken(0.3f))
        // The two copper pipes that leave the top for the ceiling.
        for ((i, x) in floatArrayOf(-0.075f, 0.01f).withIndex()) {
            val pipe = Rect(p(x - 0.012f, -0.74f), p(x + 0.012f, -0.5f))
            drawRoundRect(CeC.copper, pipe.topLeft, pipe.size, androidx.compose.ui.geometry.CornerRadius(0.006f * u))
            drawRect(CeC.copper.lighten(0.45f), Offset(pipe.left + pipe.width * 0.2f, pipe.top), Size(pipe.width * 0.2f, pipe.height))
            drawRect(CeC.copperDark, Offset(pipe.left + pipe.width * 0.7f, pipe.top), Size(pipe.width * 0.25f, pipe.height))
            drawRoundRect(Ink.line, pipe.topLeft, pipe.size, androidx.compose.ui.geometry.CornerRadius(0.006f * u), style = pen.stroke)
            for (fy in floatArrayOf(-0.6f, -0.71f)) inkedRound(Rect(p(x - 0.019f, fy - 0.006f), p(x + 0.019f, fy + 0.006f)), 0.003f * u, CeC.brass, pen, shade = false)
            if (i == 0) fxPuffs(p(x, -0.74f).x, p(x, -0.72f).y, t, 0.012f * u, 0.05f * u, CeC.steam, 0.35f, 2, 0.35f, 0.01f * u)
        }
        // The tank: a copper cylinder with a dome.
        val cx = 0f
        fxCyl(cx, -0.05f * u, -0.43f * u, 0.15f * u, 0.15f * u, CeC.copper, pen, top = CeC.copper.lighten(0.25f))
        val dome = Path().apply {
            moveTo(p(-0.15f, -0.43f).x, p(-0.15f, -0.43f).y)
            quadraticTo(p(-0.14f, -0.53f).x, p(-0.14f, -0.53f).y, p(0f, -0.54f).x, p(0f, -0.54f).y)
            quadraticTo(p(0.14f, -0.53f).x, p(0.14f, -0.53f).y, p(0.15f, -0.43f).x, p(0.15f, -0.43f).y)
            close()
        }
        drawPath(dome, androidx.compose.ui.graphics.Brush.horizontalGradient(0f to CeC.copper.lighten(0.3f), 0.5f to CeC.copper, 1f to CeC.copperDark, startX = -0.15f * u, endX = 0.15f * u))
        drawPath(dome, Ink.line, style = pen.stroke)
        drawOval(Color.White.copy(alpha = 0.55f), p(-0.1f, -0.5f), Size(0.04f * u, 0.016f * u))
        // Brass bands with rivets round the tank.
        for (y in floatArrayOf(-0.1f, -0.26f, -0.39f)) {
            val band = Rect(p(-0.152f, y - 0.012f), p(0.152f, y + 0.012f))
            drawRect(CeC.brass, band.topLeft, band.size)
            drawRect(CeC.brass.lighten(0.5f), band.topLeft, Size(band.width, 0.005f * u))
            drawRect(Ink.line, band.topLeft, band.size, style = pen.thin)
            for (k in 0 until 9) fxNail(p(-0.135f + k * 0.0338f, y), 0.0032f * u)
        }
        // The big pressure gauge on the front.
        val gc = p(0f, -0.3f)
        val gr = 0.06f * u
        drawCircle(Ink.line, gr + pen.lw * 1.6f, gc)
        drawCircle(CeC.brass, gr + pen.lw * 0.3f, gc)
        drawCircle(Color(0xFFFFFBF0), gr * 0.84f, gc)
        // Coloured zones: calm green, then orange above.
        for (k in 0..11) {
            val a = (150f + k * 20f) * (PI.toFloat() / 180f)
            val c = if (k < 6) CeC.green else if (k < 9) CeC.yellow else CeC.orange
            drawLine(c, Offset(gc.x + cos(a) * gr * 0.62f, gc.y + sin(a) * gr * 0.62f), Offset(gc.x + cos(a) * gr * 0.8f, gc.y + sin(a) * gr * 0.8f), strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Butt)
        }
        // The needle shivers a little and follows the pressure.
        val value = pressure + (if (rumble) sin(t * 30f) * 0.03f else sin(t * 2f) * 0.005f)
        val na = (150f + value.coerceIn(0f, 1.25f) * 200f) * (PI.toFloat() / 180f)
        drawLine(Ink.line, gc, Offset(gc.x + cos(na) * gr * 0.72f, gc.y + sin(na) * gr * 0.72f), strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
        drawCircle(CeC.brass, gr * 0.12f, gc)
        drawCircle(Ink.line, gr * 0.12f, gc, style = pen.thin)
        drawArc(Color.White.copy(alpha = 0.6f), 205f, 50f, false, Offset(gc.x - gr * 0.78f, gc.y - gr * 0.78f), Size(gr * 1.56f, gr * 1.56f), style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
        // The fire door: a round iron hatch with a glowing window.
        val dc = p(0f, -0.155f)
        inkedCircle(dc, 0.04f * u, CeC.iron.lighten(0.1f), pen)
        val flame = 0.7f + 0.3f * sin(t * 9f) + 0.1f * sin(t * 17f)
        fxGlow(dc, 0.17f * u, CeC.orange, 0.5f * flame + (if (rumble) 0.3f else 0f))
        val window = Path().apply { addOval(Rect(dc.x - 0.022f * u, dc.y - 0.022f * u, dc.x + 0.022f * u, dc.y + 0.022f * u)) }
        drawPath(window, Color(0xFFFFB02E))
        clipPath(window) {
            fxFire(dc.x - 0.008f * u, dc.y + 0.02f * u, 0.016f * u, 0.032f * u * flame, t, 1f, pen, false)
            fxFire(dc.x + 0.009f * u, dc.y + 0.02f * u, 0.014f * u, 0.026f * u * flame, t, 2.3f, pen, false)
        }
        drawPath(window, Ink.line, style = pen.stroke)
        for (a in floatArrayOf(0f, 1.5708f, 3.1416f, 4.7124f)) drawCircle(CeC.steel, 0.0032f * u, Offset(dc.x + cos(a) * 0.034f * u, dc.y + sin(a) * 0.034f * u))
        drawLine(CeC.steel, Offset(dc.x + 0.04f * u, dc.y), Offset(dc.x + 0.056f * u, dc.y + 0.012f * u), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
        // A safety valve on the top that spits steam when the boiler is under pressure.
        val sv = p(0.075f, -0.545f)
        inkedRound(Rect(sv.x - 0.012f * u, sv.y - 0.03f * u, sv.x + 0.012f * u, sv.y), 0.004f * u, CeC.brass, pen, shade = false)
        inkedCircle(Offset(sv.x, sv.y - 0.036f * u), 0.011f * u, CeC.brass, pen, shade = false)
        if (rumble || pressure > 0.6f) {
            fxPuffs(sv.x, sv.y - 0.045f * u, t, 0.016f * u, 0.12f * u, CeC.steam, 0.7f, 5, 0.8f, 0.03f * u)
        }
    }
    // A nameplate with three rivets, and a sooty smudge: this boiler has worked hard.
    inkedRound(Rect(p(-0.04f, -0.075f), p(0.04f, -0.06f)), 0.003f * u, CeC.brass.darken(0.1f), pen, shade = false)
    for (k in -1..1) fxNail(p(k * 0.026f, -0.0675f), 0.0028f * u)
}

// ============================================================================================ valves

/** A handwheel valve on a copper pipe: six spokes that turn when it opens, a brass body and a spout that hisses steam. */
internal fun DrawScope.ceValve(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val open = f.mode == 1
    val color = valveColors[f.variant.mod(valveColors.size)]
    // The brass body on the pipe, flanges above and below.
    for (y in floatArrayOf(-0.235f, -0.065f)) inkedRound(Rect(p(-0.024f, y - 0.007f), p(0.024f, y + 0.007f)), 0.003f * u, CeC.brass, pen, shade = false)
    val body = Rect(p(-0.032f, -0.21f), p(0.032f, -0.09f))
    inkedRound(body, 0.01f * u, CeC.brass, pen)
    // The spout to the right, with a nozzle.
    capsule(p(0.03f, -0.115f), p(0.075f, -0.115f), 0.012f * u, CeC.brass, pen)
    inkedRound(Rect(p(0.066f, -0.126f), p(0.084f, -0.104f)), 0.004f * u, CeC.brass.darken(0.1f), pen, shade = false)
    // The handwheel, turning with the valve.
    val c = p(0f, -0.15f)
    val r = 0.055f * u
    rotate(f.angle * 57.3f, c) {
        for (k in 0 until 6) {
            val a = k * PI.toFloat() / 3f
            val e = Offset(c.x + cos(a) * r * 0.92f, c.y + sin(a) * r * 0.92f)
            drawLine(Ink.line, c, e, strokeWidth = 0.011f * u + pen.lw * 2f, cap = StrokeCap.Round)
            drawLine(color, c, e, strokeWidth = 0.011f * u, cap = StrokeCap.Round)
        }
        drawCircle(Ink.line, r + pen.lw * 1.2f, c, style = Stroke(0.016f * u + pen.lw * 2f))
        drawCircle(color, r, c, style = Stroke(0.016f * u))
        drawCircle(color.lighten(0.5f), r, c, style = Stroke(0.004f * u))
        // A mark on the rim shows how far it has turned.
        drawCircle(Color.White, 0.0045f * u, Offset(c.x + r, c.y))
    }
    inkedCircle(c, 0.016f * u, CeC.brass, pen)
    drawCircle(Color.White.copy(alpha = 0.7f), 0.0045f * u, Offset(c.x - 0.005f * u, c.y - 0.005f * u))
    // A little coloured tag on a chain.
    drawLine(Ink.line, p(-0.03f, -0.1f), p(-0.045f, -0.07f), strokeWidth = pen.lw * 0.8f)
    inkedCircle(p(-0.048f, -0.058f), 0.011f * u, color, pen, shade = false)
    // Open: steam shoots out of the spout; shut: now and then a drip.
    val nozzle = p(0.08f, -0.108f)
    if (open) {
        for (k in 0 until 3) {
            val ph = ceFrac(t * 1.6f, 1f, k / 3f)
            drawCircle(CeC.steam, (0.012f + 0.02f * ph) * u, Offset(nozzle.x + 0.03f * u * ph, nozzle.y + 0.05f * u * ph - 0.01f * u), alpha = 0.8f * (1f - ph))
        }
        fxPuffs(nozzle.x, nozzle.y, t, 0.014f * u, 0.12f * u, CeC.steam, 0.8f, 4, 1f, 0.03f * u)
    } else {
        val ph = ceFrac(t, 2.6f, f.id * 0.17f)
        if (ph < 0.5f) drawCircle(CeC.water, 0.003f * u, Offset(nozzle.x, nozzle.y + 0.012f * u + ph * 0.12f * u), alpha = 1f - ph * 1.6f)
    }
}
