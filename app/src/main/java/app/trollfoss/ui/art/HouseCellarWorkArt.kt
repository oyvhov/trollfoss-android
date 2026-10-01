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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/*
 * The workshop's furniture: Rolf's charging station, a hanging bulb, a stack of crates, the mouse hole and
 * the saw horse with its log.
 */

// ===================================================================================== charging station

/**
 * Rolf's charging station: a teal alcove with a battery meter that fills while somebody stands in it
 * ([Fixture.on], [Fixture.timer] seconds into the 2.8 s charge), turns green and happy when full
 * ([Fixture.mode] 1), a coiled cable with a plug, and electric sparks.
 */
internal fun DrawScope.ceCharger(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val charging = f.on
    val full = f.mode == 1
    val level = if (full) 1f else if (charging) (f.timer / 2.8f).coerceIn(0f, 1f) else 0f
    val d = 0.09f
    ceShadow(u, 0.17f, d)
    // The pad on the floor with two contact plates.
    val pad = fxDisc2(0f, -0.008f * u, 0.075f * u, 0.07f * u)
    fxFace(pad, CeC.iron, pen)
    for (s in floatArrayOf(-1f, 1f)) {
        val c = fxQ(u, s * 0.032f, -0.012f, 0.03f)
        drawOval(CeC.yellow, Offset(c.x - 0.016f * u, c.y - 0.006f * u), Size(0.032f * u, 0.012f * u))
        drawOval(Ink.line, Offset(c.x - 0.016f * u, c.y - 0.006f * u), Size(0.032f * u, 0.012f * u), style = pen.thin)
    }
    // The back panel: an arch of teal with a lighter inner panel.
    val arch = Path().apply {
        moveTo(p(-0.082f, -0.02f).x, p(-0.082f, -0.02f).y)
        lineTo(p(-0.082f, -0.29f).x, p(-0.082f, -0.29f).y)
        quadraticTo(p(-0.082f, -0.36f).x, p(-0.082f, -0.36f).y, p(0f, -0.36f).x, p(0f, -0.36f).y)
        quadraticTo(p(0.082f, -0.36f).x, p(0.082f, -0.36f).y, p(0.082f, -0.29f).x, p(0.082f, -0.29f).y)
        lineTo(p(0.082f, -0.02f).x, p(0.082f, -0.02f).y)
        close()
    }
    translate(FX_DX * 0.06f * u, FX_DY * 0.06f * u) {
        drawPath(arch, CeC.teal.darken(0.35f))
        drawPath(arch, Ink.line, style = pen.stroke)
    }
    inked(arch, CeC.teal, pen)
    val inner = Path().apply {
        moveTo(p(-0.062f, -0.04f).x, p(-0.062f, -0.04f).y)
        lineTo(p(-0.062f, -0.285f).x, p(-0.062f, -0.285f).y)
        quadraticTo(p(-0.062f, -0.335f).x, p(-0.062f, -0.335f).y, p(0f, -0.335f).x, p(0f, -0.335f).y)
        quadraticTo(p(0.062f, -0.335f).x, p(0.062f, -0.335f).y, p(0.062f, -0.285f).x, p(0.062f, -0.285f).y)
        lineTo(p(0.062f, -0.04f).x, p(0.062f, -0.04f).y)
        close()
    }
    drawPath(inner, if (full) Color(0xFFD5F5E1) else Color(0xFFCFF3EE))
    drawPath(inner, Ink.line, style = pen.thin)
    // The battery meter: five bars in a pill at the top.
    val pill = Rect(p(-0.044f, -0.33f), p(0.044f, -0.298f))
    inkedRound(pill, 0.008f * u, Color(0xFF1D2433), pen, shade = false)
    for (k in 0 until 5) {
        val lit = level >= (k + 0.5f) / 5f
        val blinkTop = charging && !full && (level * 5f).toInt() == k && cePulse(t, 0.5f) > 0.5f
        val color = if (full) CeC.green else if (k < 2) CeC.orange else CeC.green
        drawRoundRect(if (lit || blinkTop) color else Color(0xFF3A4660), Offset(pill.left + 0.006f * u + k * 0.0154f * u, pill.top + 0.006f * u), Size(0.0125f * u, pill.height - 0.012f * u), androidx.compose.ui.geometry.CornerRadius(0.002f * u))
    }
    drawRect(Color(0xFF1D2433), Offset(pill.right, pill.center.y - 0.004f * u), Size(0.005f * u, 0.008f * u))
    // A big lightning bolt (or a green heart once it is full).
    if (full) {
        drawPath(fxHeart(0f, -0.2f * u, 0.032f * u), CeC.green)
        drawPath(fxHeart(0f, -0.2f * u, 0.032f * u), Ink.line, style = pen.stroke)
        twinkle(p(0.03f, -0.235f), 0.012f * u, Color.White, cePulse(t, 0.9f))
    } else {
        val bolt = ceBolt(p(0f, -0.2f), 0.045f * u)
        drawPath(bolt, if (charging) CeC.yellow else CeC.yellow.darken(0.15f), alpha = if (charging) 1f else 0.75f)
        drawPath(bolt, Ink.line, style = pen.stroke)
    }
    // The cable: a curly line from the top of the arch; its plug sits on whoever stands there, or dangles.
    val top = p(0.07f, -0.35f)
    val plug = if (charging || full) p(0.02f, -0.14f) else p(0.095f + sin(t * 1.1f) * 0.004f, -0.1f)
    val cable = Path().apply {
        moveTo(top.x, top.y)
        cubicTo(p(0.12f, -0.3f).x, p(0.12f, -0.3f).y, p(0.1f, -0.2f).x, p(0.1f, -0.2f).y, plug.x, plug.y)
    }
    drawPath(cable, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(cable, CeC.orange, style = Stroke(0.009f * u, cap = StrokeCap.Round))
    for (k in 1..5) {
        val f0 = k / 6f
        val c = Offset(top.x + (plug.x - top.x) * f0 + 0.018f * u * sin(f0 * 3f) , top.y + (plug.y - top.y) * f0)
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(c.x - 0.005f * u, c.y), Offset(c.x + 0.005f * u, c.y + 0.004f * u), strokeWidth = pen.lw * 0.7f)
    }
    inkedRound(Rect(plug.x - 0.011f * u, plug.y - 0.008f * u, plug.x + 0.011f * u, plug.y + 0.014f * u), 0.004f * u, CeC.steel, pen, shade = false)
    drawLine(Ink.line, Offset(plug.x - 0.004f * u, plug.y + 0.014f * u), Offset(plug.x - 0.004f * u, plug.y + 0.022f * u), strokeWidth = pen.lw * 1.2f)
    drawLine(Ink.line, Offset(plug.x + 0.004f * u, plug.y + 0.014f * u), Offset(plug.x + 0.004f * u, plug.y + 0.022f * u), strokeWidth = pen.lw * 1.2f)
    // Sparks while it charges: zig-zags between the plates and the plug.
    if (charging) {
        fxGlow(plug, 0.06f * u, Color(0xFF7CFFB2), 0.5f + 0.3f * cePulse(t, 0.25f))
        for (k in 0 until 3) {
            val ph = ceFrac(t * 4f, 1f, k / 3f)
            if (ph > 0.6f) continue
            val a = Path().apply {
                moveTo(plug.x, plug.y + 0.02f * u)
                for (s in 1..4) lineTo(plug.x + (hash01(k * 7 + s, 951) - 0.5f) * 0.06f * u, plug.y + 0.02f * u + s * 0.018f * u)
            }
            drawPath(a, Color(0xFF7CFFB2), style = Stroke(pen.lw * 1.5f, cap = StrokeCap.Round))
        }
    } else {
        // A standby light.
        drawCircle(if (full) CeC.green else CeC.orange, 0.005f * u, p(-0.07f, -0.31f), alpha = 0.4f + 0.6f * cePulse(t, if (full) 1.2f else 2.4f))
    }
}

// ======================================================================================= the hanging bulb

/** A bare bulb under a green enamel shade, swinging a little on its cord; lit ([Fixture.on]) it glows warm. */
internal fun DrawScope.ceBulb(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val on = f.on
    val swing = sin(t * 0.9f + f.id) * 2.4f + (if (f.anim > 0.05f) sin(t * 18f) * 6f * f.anim else 0f)
    val pivot = p(0f, -0.15f)
    rotate(swing, pivot) {
        drawLine(Ink.line, pivot, p(0f, -0.1f), strokeWidth = pen.lw * 1.1f)
        // The shade: a dome with a rim.
        val shade = Path().apply {
            moveTo(p(-0.012f, -0.108f).x, p(-0.012f, -0.108f).y)
            lineTo(p(0.012f, -0.108f).x, p(0.012f, -0.108f).y)
            quadraticTo(p(0.04f, -0.095f).x, p(0.04f, -0.095f).y, p(0.045f, -0.06f).x, p(0.045f, -0.06f).y)
            lineTo(p(-0.045f, -0.06f).x, p(-0.045f, -0.06f).y)
            quadraticTo(p(-0.04f, -0.095f).x, p(-0.04f, -0.095f).y, p(-0.012f, -0.108f).x, p(-0.012f, -0.108f).y)
            close()
        }
        inked(shade, Color(0xFF3F9E6E), pen)
        drawRect(Color.White.copy(alpha = 0.35f), p(-0.03f, -0.098f), Size(0.012f * u, 0.032f * u))
        inkedRound(Rect(p(-0.049f, -0.065f), p(0.049f, -0.056f)), 0.003f * u, Color(0xFF2F7E55), pen, shade = false)
        // The bulb itself.
        val c = p(0f, -0.04f)
        if (on) {
            fxGlow(c, 0.34f * u, Color(0xFFFFE08A), 0.35f + 0.05f * sin(t * 7f))
            fxGlow(c, 0.1f * u, Color.White, 0.55f)
        }
        drawCircle(Ink.line, 0.021f * u, c)
        drawCircle(if (on) Color(0xFFFFF4C2) else Color(0xFFB9C4CF), 0.018f * u, c)
        drawCircle(Color.White.copy(alpha = if (on) 0.9f else 0.6f), 0.0055f * u, Offset(c.x - 0.006f * u, c.y - 0.007f * u))
        if (on) {
            val filament = Path().apply {
                moveTo(c.x - 0.006f * u, c.y + 0.006f * u); lineTo(c.x - 0.003f * u, c.y - 0.003f * u); lineTo(c.x + 0.003f * u, c.y + 0.003f * u); lineTo(c.x + 0.006f * u, c.y - 0.004f * u)
            }
            drawPath(filament, CeC.orange, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
        }
    }
}

// ============================================================================================ crates

/** A stack of two wooden crates with slats, nails and a heart on the top one. */
internal fun DrawScope.ceCrates(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val d = 0.14f
    ceShadow(u, 0.21f, d)
    val wood = CeC.wood
    // The lower crate.
    fxBox(u, -0.1f, -0.105f, 0.1f, 0f, d, wood, pen, rad = 0.004f, top = CeC.woodLight, side = CeC.woodDark)
    val lower = fxFront(u, -0.1f, -0.105f, 0.1f, 0f)
    for (k in 1..2) drawLine(CeC.woodDark, Offset(lower.left, lower.top + lower.height * k / 3f), Offset(lower.right, lower.top + lower.height * k / 3f), strokeWidth = pen.lw * 0.9f)
    drawLine(CeC.woodDark.darken(0.2f), p(-0.1f, -0.105f), p(0.1f, 0f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    drawLine(CeC.woodDark.darken(0.2f), p(0.1f, -0.105f), p(-0.1f, 0f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    for (c in listOf(p(-0.09f, -0.095f), p(0.09f, -0.095f), p(-0.09f, -0.01f), p(0.09f, -0.01f))) fxNail(c, 0.0035f * u)
    // The upper crate, a little smaller and turned a touch.
    fxBox(u, -0.075f, -0.19f, 0.065f, -0.105f, d * 0.8f, wood.lighten(0.08f), pen, rad = 0.004f, z = 0.02f, top = CeC.woodLight, side = CeC.woodDark)
    val up = fxFront(u, -0.075f, -0.19f, 0.065f, -0.105f, 0.02f)
    for (k in 1..2) drawLine(CeC.woodDark, Offset(up.left, up.top + up.height * k / 3f), Offset(up.right, up.top + up.height * k / 3f), strokeWidth = pen.lw * 0.9f)
    drawPath(fxHeart(up.center.x, up.center.y, 0.017f * u), CeC.pink)
    drawPath(fxHeart(up.center.x, up.center.y, 0.017f * u), Ink.line, style = pen.thin)
    // A hanging tag.
    val tag = Rect(p(0.04f, -0.1f), p(0.075f, -0.065f))
    inkedRound(tag, 0.003f * u, Color(0xFFFFF4C2), pen, shade = false)
    drawLine(Ink.line, p(0.052f, -0.088f), p(0.064f, -0.078f), strokeWidth = pen.lw)
    drawCircle(Ink.line, 0.0025f * u, p(0.057f, -0.095f))
}

// ========================================================================================== mouse hole

/** A little arch in the skirting: two eyes glint in the dark, and when tapped a mouse peeks out (and dances for cheese). */
internal fun DrawScope.ceMouseHole(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val timer = f.timer
    val e = if (timer > 0f) min(1f, timer * 2.5f) else 0f
    val party = timer > 2.4f
    // The mat and the hole.
    inkedRound(Rect(p(-0.05f, -0.004f), p(0.05f, 0.012f)), 0.004f * u, Color(0xFFFFB3C7), pen, shade = false)
    val hole = Path().apply {
        moveTo(p(-0.032f, -0.004f).x, p(-0.032f, -0.004f).y)
        lineTo(p(-0.032f, -0.04f).x, p(-0.032f, -0.04f).y)
        quadraticTo(p(-0.032f, -0.07f).x, p(-0.032f, -0.07f).y, p(0f, -0.07f).x, p(0f, -0.07f).y)
        quadraticTo(p(0.032f, -0.07f).x, p(0.032f, -0.07f).y, p(0.032f, -0.04f).x, p(0.032f, -0.04f).y)
        lineTo(p(0.032f, -0.004f).x, p(0.032f, -0.004f).y)
        close()
    }
    drawPath(hole, Color(0xFF1A1220))
    clipPath(hole) {
        // Eyes in the dark that blink now and then, or the mouse.
        if (e < 0.05f) {
            val open = ceFrac(t, 4.2f, f.id * 0.1f) > 0.05f
            if (open) for (s in floatArrayOf(-0.009f, 0.009f)) drawCircle(Color(0xFFFFF4C2), 0.0034f * u, p(s, -0.034f))
        } else {
            val rise = (1f - e) * 0.05f
            val dance = if (party) sin(t * 14f) * 0.006f else 0f
            translate((dance) * u, rise * u) {
                // Body, head, ears, nose and whiskers.
                drawOval(Color(0xFFB9B4C4), p(-0.026f, -0.04f), Size(0.052f * u, 0.05f * u))
                drawCircle(Color(0xFFCFCADA), 0.022f * u, p(0f, -0.045f))
                for (s in floatArrayOf(-0.016f, 0.016f)) {
                    drawCircle(Color(0xFFCFCADA), 0.012f * u, p(s, -0.065f))
                    drawCircle(Color(0xFFFFB3C7), 0.0072f * u, p(s, -0.065f))
                }
                for (s in floatArrayOf(-0.008f, 0.008f)) drawCircle(Ink.line, 0.0032f * u, p(s, -0.048f))
                drawCircle(Color(0xFFFF6F91), 0.0035f * u, p(0f, -0.038f))
                for (s in floatArrayOf(-1f, 1f)) {
                    drawLine(Ink.line, p(s * 0.01f, -0.037f), p(s * 0.032f, -0.042f), strokeWidth = pen.lw * 0.6f)
                    drawLine(Ink.line, p(s * 0.01f, -0.035f), p(s * 0.032f, -0.033f), strokeWidth = pen.lw * 0.6f)
                }
                if (party) {
                    // A wedge of brown cheese in its paws.
                    val cheese = Path().apply { moveTo(p(-0.012f, -0.014f).x, p(-0.012f, -0.014f).y); lineTo(p(0.014f, -0.02f).x, p(0.014f, -0.02f).y); lineTo(p(0.014f, -0.006f).x, p(0.014f, -0.006f).y); close() }
                    inked(cheese, Color(0xFFC98A55), pen, shade = false)
                }
            }
        }
    }
    drawPath(hole, Ink.line, style = pen.stroke)
    // A bit of baseboard chewed round the hole, and a cobweb thread.
    drawLine(Ink.line.copy(alpha = 0.6f), p(-0.04f, -0.045f), p(-0.034f, -0.06f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.5f), p(0.038f, -0.07f), p(0.038f + sin(t) * 0.004f, -0.03f), strokeWidth = pen.lw * 0.5f)
}

// ============================================================================================ saw horse

/** A saw horse with a log across it and a hand saw in its cut. While sawing the saw runs to and fro and dust falls. */
internal fun DrawScope.ceSaw(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val on = f.on
    val d = 0.1f
    ceShadow(u, 0.3f, d)
    // Two A-frames and the beam between.
    for (s in floatArrayOf(-0.1f, 0.1f)) {
        capsule(q(s - 0.035f, 0f, 0.03f), q(s, -0.1f, 0.03f), 0.01f * u, CeC.wood, pen)
        capsule(q(s + 0.035f, 0f, 0.03f), q(s, -0.1f, 0.03f), 0.01f * u, CeC.woodDark, pen)
        capsule(q(s - 0.035f, 0f, 0.07f), q(s, -0.1f, 0.07f), 0.01f * u, CeC.woodDark, pen)
    }
    fxBox(u, -0.14f, -0.108f, 0.14f, -0.094f, 0.06f, CeC.wood, pen, rad = 0.003f, z = 0.02f, top = CeC.woodLight, side = CeC.woodDark)
    // The log lying along the beam: a long cylinder with its end showing rings.
    val body = Rect(p(-0.115f, -0.158f), p(0.125f, -0.1f))
    drawRoundRect(Ink.line, Offset(body.left - pen.lw, body.top - pen.lw), Size(body.width + pen.lw * 2f, body.height + pen.lw * 2f), androidx.compose.ui.geometry.CornerRadius(0.012f * u))
    drawRoundRect(CeBark.bark(body), body.topLeft, body.size, androidx.compose.ui.geometry.CornerRadius(0.012f * u))
    for (k in 0 until 6) {
        val x = body.left + body.width * (0.1f + 0.15f * k)
        drawLine(CeC.woodDark.darken(0.3f), Offset(x, body.top + 0.008f * u), Offset(x + 0.004f * u, body.top + 0.02f * u), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
    }
    val end = Offset(body.right, body.center.y)
    drawOval(Ink.line, Offset(end.x - 0.016f * u - pen.lw, end.y - 0.029f * u - pen.lw), Size(0.032f * u + pen.lw * 2f, 0.058f * u + pen.lw * 2f))
    drawOval(Color(0xFFE9C58F), Offset(end.x - 0.016f * u, end.y - 0.029f * u), Size(0.032f * u, 0.058f * u))
    drawOval(CeC.woodDark, Offset(end.x - 0.01f * u, end.y - 0.02f * u), Size(0.02f * u, 0.04f * u), style = pen.thin)
    drawOval(CeC.woodDark, Offset(end.x - 0.005f * u, end.y - 0.01f * u), Size(0.01f * u, 0.02f * u), style = pen.thin)
    // The cut, a hand saw sticking in it, to and fro while it saws.
    val cutX = 0.02f
    val stroke = if (on) sin(f.angle * 22f) * 0.03f else 0f
    drawRect(Color(0xFF3A2A1E), p(cutX - 0.004f, -0.158f), Size(0.008f * u, 0.058f * u))
    translate((cutX + stroke) * u, -0.14f * u) {
        val blade = Path().apply {
            moveTo(-0.07f * u, -0.03f * u); lineTo(0.05f * u, -0.03f * u); lineTo(0.05f * u, 0.016f * u); lineTo(-0.07f * u, 0.0f * u); close()
        }
        // The blade is seen edge-on from the front: a thin steel strip with teeth along its lower edge.
        drawPath(blade, CeC.steel)
        drawPath(blade, Ink.line, style = pen.stroke)
        for (k in 0 until 9) {
            val tx = -0.066f * u + k * 0.0125f * u
            drawLine(Ink.line, Offset(tx, 0.0f), Offset(tx + 0.005f * u, 0.012f * u), strokeWidth = pen.lw * 0.7f)
        }
        val handle = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(0.05f * u, -0.052f * u, 0.095f * u, 0.0f), androidx.compose.ui.geometry.CornerRadius(0.012f * u))) }
        inked(handle, CeC.orange, pen)
        drawOval(Color(0xFF2B2140), Offset(0.062f * u, -0.04f * u), Size(0.018f * u, 0.022f * u))
    }
    // Dust: a growing pile under the cut, and drifting flakes while it saws.
    val pile = min(f.count, 6) * 0.0035f + 0.006f
    val dust = Path().apply {
        moveTo(p(cutX - 0.03f - pile * 3f, -0.002f).x, p(cutX - 0.03f - pile * 3f, -0.002f).y)
        quadraticTo(p(cutX, -0.004f - pile * 2f).x, p(cutX, -0.004f - pile * 2f).y, p(cutX + 0.03f + pile * 3f, -0.002f).x, p(cutX + 0.03f + pile * 3f, -0.002f).y)
        close()
    }
    drawPath(dust, Color(0xFFE8C98A))
    drawPath(dust, Ink.line, alpha = 0.6f, style = pen.thin)
    if (on) {
        for (k in 0 until 6) {
            val ph = ceFrac(t * 2f, 1f, k / 6f)
            drawCircle(Color(0xFFE8C98A), 0.003f * u, p(cutX + (hash01(k, 961) - 0.5f) * 0.05f + stroke * 0.5f, -0.1f + 0.095f * ph), alpha = 1f - ph * 0.6f)
        }
    }
}

/** A tiny helper for the log's bark: a vertical gradient from light to dark brown. */
private object CeBark {
    fun bark(r: Rect) = androidx.compose.ui.graphics.Brush.verticalGradient(
        0f to Color(0xFFB07A48), 0.5f to Color(0xFF8E5E38), 1f to Color(0xFF6E4630), startY = r.top, endY = r.bottom,
    )
}

@Suppress("unused")
private fun unusedCos() = cos(0f)
