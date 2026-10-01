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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/*
 * The laundry: a washer and a dryer with round windows and real tumbling clothes, the sock monster, the basket
 * under the chute, a clothes line, an ironing board and the chute itself.
 */

// ====================================================================================== washer and dryer

internal fun DrawScope.ceWasher(f: Fixture, u: Float, pen: Pen) = ceMachine(f, u, pen, dryer = false)

internal fun DrawScope.ceDryer(f: Fixture, u: Float, pen: Pen) = ceMachine(f, u, pen, dryer = true)

private val ceClothes = listOf(Color(0xFFFF8FB1), Color(0xFF5AA9E6), Color(0xFFFFC83D), Color(0xFF6BCB77), Color(0xFFB983FF), Color(0xFFFF9F43))

private fun DrawScope.ceMachine(f: Fixture, u: Float, pen: Pen, dryer: Boolean) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val on = f.on
    val body = if (dryer) CeC.mint else CeC.enamel
    val d = 0.15f
    ceShadow(u, 0.22f, d)
    // The whole machine shakes while it runs.
    translate(if (on) f.bob * u else 0f, 0f) {
        for (s in floatArrayOf(-1f, 1f)) fxBox(u, s * 0.085f - 0.012f, -0.014f, s * 0.085f + 0.012f, 0f, 0.025f, CeC.iron, pen, rad = 0.003f, z = 0.01f)
        fxBox(u, -0.105f, -0.25f, 0.105f, -0.014f, d, body, pen, rad = 0.012f, top = body.lighten(0.5f), side = body.darken(0.2f))
        // The control strip: a drawer for soap, a little display, a knob.
        inkedRound(Rect(-0.097f * u, -0.244f * u, 0.097f * u, -0.208f * u), 0.006f * u, body.darken(0.07f), pen, shade = false)
        val drawer = Rect(-0.09f * u, -0.239f * u, -0.038f * u, -0.214f * u)
        inkedRound(drawer, 0.004f * u, Color.White, pen, shade = false)
        drawLine(Ink.line, Offset(drawer.left + 0.012f * u, drawer.center.y), Offset(drawer.right - 0.012f * u, drawer.center.y), strokeWidth = pen.lw, cap = StrokeCap.Round)
        val display = Rect(-0.008f * u, -0.24f * u, 0.046f * u, -0.216f * u)
        inkedRound(display, 0.004f * u, Color(0xFF1D2433), pen, shade = false)
        if (on) {
            // A bar of little lights fills while the cycle runs (the timer counts down from 3.4 seconds).
            val lit = ((1f - f.timer / 3.4f) * 6f).toInt().coerceIn(0, 6)
            for (k in 0 until 6) {
                val c = Offset(display.left + (k + 0.7f) * display.width / 6.6f, display.center.y)
                drawCircle(if (k <= lit) CeC.teal else Color(0xFF2E3A52), 0.0042f * u, c)
            }
        } else {
            // On standby one dot blinks.
            drawCircle(CeC.teal, 0.0045f * u, Offset(display.left + 0.008f * u, display.center.y), alpha = 0.35f + 0.65f * cePulse(t, 1.6f))
        }
        val knob = p(0.075f, -0.226f)
        inkedCircle(knob, 0.0125f * u, CeC.steel, pen)
        val ka = if (on) (1f - f.timer / 3.4f) * 4.7f - 2.3f else -2.3f
        drawLine(CeC.teal, knob, Offset(knob.x + cos(ka) * 0.01f * u, knob.y + sin(ka) * 0.01f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)

        // The round window: a chrome frame, a dark rim and the glass with the drum behind it.
        val c = p(0f, -0.122f)
        drawCircle(Ink.line.copy(alpha = 0.25f), 0.078f * u, Offset(c.x + 0.003f * u, c.y + 0.004f * u))
        inkedCircle(c, 0.073f * u, if (dryer) Color(0xFFE4F2EC) else CeC.steel, pen)
        inkedCircle(c, 0.06f * u, Color(0xFF2A3350), pen, shade = false)
        val r = 0.052f * u
        val glass = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
        clipPath(glass) {
            drawRect(Color(0xFF1E3A55), Offset(c.x - r, c.y - r), Size(r * 2f, r * 2f))
            if (!dryer && on) {
                // Soapy water slops about in the lower half.
                val level = c.y + r * (0.15f + 0.1f * sin(t * 7f))
                drawRect(CeC.water.copy(alpha = 0.55f), Offset(c.x - r, level), Size(r * 2f, c.y + r - level))
                for (k in 0 until 7) {
                    val ph = ceFrac(t * 0.8f, 1f, hash01(k, 911))
                    drawCircle(Color.White, (0.004f + 0.004f * hash01(k, 912)) * u, Offset(c.x + (hash01(k, 913) - 0.5f) * r * 1.6f, c.y + r * 0.9f - ph * r * 1.3f), alpha = 0.85f * (1f - ph))
                }
            }
            // The washing: five lumps going round, or heaped at the bottom while it stands still.
            for (k in 0 until 5) {
                val a = if (on) f.angle * (if (dryer) 0.8f else 1f) + k * 1.2566f else 1.4f + k * 0.5f
                val radius = if (on) 0.03f * u + (if (dryer) 0.012f * u * sin(t * 13f + k) else 0f) else 0.012f * u * (k % 2)
                val cx = if (on) c.x + cos(a) * radius else c.x - 0.03f * u + k * 0.015f * u
                val cy = if (on) c.y + sin(a) * radius else c.y + r * 0.62f - (k % 2) * 0.014f * u
                rotate(a * 57f, Offset(cx, cy)) {
                    drawOval(ceClothes[(k + (if (dryer) 2 else 0)) % ceClothes.size], Offset(cx - 0.017f * u, cy - 0.011f * u), Size(0.034f * u, 0.022f * u))
                    drawOval(Ink.line, Offset(cx - 0.017f * u, cy - 0.011f * u), Size(0.034f * u, 0.022f * u), style = pen.thin)
                }
            }
            // A sock hangs out of the heap.
            if (!on) translate(c.x + 0.012f * u, c.y + r * 0.74f) { ceSock(0.03f * u, 0.032f * u, if (dryer) 1 else 4, pen) }
        }
        // Glass shine and the door's hinge.
        drawArc(Color.White.copy(alpha = 0.75f), 200f, 55f, false, Offset(c.x - r * 0.82f, c.y - r * 0.82f), Size(r * 1.64f, r * 1.64f), style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
        drawOval(Color.White.copy(alpha = 0.55f), Offset(c.x + r * 0.35f, c.y - r * 0.62f), Size(r * 0.3f, r * 0.16f))
        for (s in floatArrayOf(-0.03f, 0.03f)) inkedRound(Rect(-0.1f * u, (-0.122f + s - 0.007f) * u, -0.074f * u, (-0.122f + s + 0.007f) * u), 0.002f * u, CeC.steel, pen, shade = false)
        if (dryer && on) {
            drawCircle(CeC.orange.copy(alpha = 0.35f + 0.25f * cePulse(t, 0.7f)), 0.067f * u, c, style = Stroke(0.007f * u))
            // Warm air shimmers up from the top.
            for (k in 0 until 3) {
                val x = (-0.05f + k * 0.05f) * u
                val ph = ceFrac(t, 1.3f, k * 0.33f)
                val path = Path().apply {
                    moveTo(x, -0.26f * u)
                    quadraticTo(x + 0.01f * u, (-0.28f - 0.04f * ph) * u, x, (-0.3f - 0.06f * ph) * u)
                    quadraticTo(x - 0.01f * u, (-0.32f - 0.07f * ph) * u, x, (-0.34f - 0.08f * ph) * u)
                }
                drawPath(path, CeC.orange, alpha = 0.5f * (1f - ph), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
            }
        }
        // The bottom: a filter flap with slats (washer) or a lint tray with a handle (dryer); a sock sticker.
        val flap = Rect(-0.045f * u, -0.054f * u, 0.045f * u, -0.03f * u)
        inkedRound(flap, 0.004f * u, body.darken(0.1f), pen, shade = false)
        if (dryer) {
            drawLine(Ink.line, Offset(flap.left + 0.015f * u, flap.center.y), Offset(flap.right - 0.015f * u, flap.center.y), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
        } else {
            for (k in 1..4) drawLine(Ink.line.copy(alpha = 0.7f), Offset(flap.left + 0.008f * u, flap.top + k * flap.height / 5f), Offset(flap.right - 0.008f * u, flap.top + k * flap.height / 5f), strokeWidth = pen.lw * 0.6f)
        }
        translate(-0.085f * u, -0.022f * u) { ceSock(0.026f * u, 0.028f * u, if (dryer) 3 else 0, pen) }
        if (dryer) {
            // A little top vent knob.
            val v = fxQ(u, 0.07f, -0.25f, 0.09f)
            fxCyl(v.x, v.y, v.y - 0.012f * u, 0.014f * u, 0.014f * u, CeC.steel, pen)
        }
    }
}

// ======================================================================================== sock monster

internal fun DrawScope.ceSockMonster(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val hunger = f.angle.coerceIn(0f, 1f)
    val breathe = 1f + 0.015f * sin(t * 2.2f)
    ceShadow(u, 0.25f, 0.13f)
    // A pile of lonely socks round his feet.
    translate(-0.135f * u, -0.004f * u) { ceSock(0.05f * u, 0.05f * u, 1, pen) }
    translate(0.12f * u, -0.002f * u) { ceSock(0.05f * u, 0.05f * u, 3, pen) }
    translate(0.15f * u, -0.004f * u) { ceSock(0.045f * u, 0.045f * u, 5, pen) }
    val purple = CeC.purple
    // Feet and arms behind the body.
    for (s in floatArrayOf(-1f, 1f)) {
        inkedOval(Rect((s * 0.055f - 0.032f) * u, -0.03f * u, (s * 0.055f + 0.032f) * u, 0f), CeC.orange, pen)
    }
    // The furry body.
    val body = ceFurPath(0f, -0.125f * u, 0.108f * u * breathe, 0.1f * u * breathe, 14, 0.12f, 11, sway = sin(t * 1.4f) * 0.003f * u)
    inked(body, purple, pen)
    // Little ears with pink insides.
    for (s in floatArrayOf(-1f, 1f)) {
        val ear = Path().apply {
            moveTo(p(s * 0.085f, -0.205f).x, p(s * 0.085f, -0.205f).y)
            quadraticTo(p(s * 0.1f, -0.265f).x, p(s * 0.1f, -0.265f).y, p(s * 0.05f, -0.23f).x, p(s * 0.05f, -0.23f).y)
            close()
        }
        inked(ear, purple, pen, shade = false)
        drawCircle(CeC.pink, 0.008f * u, p(s * 0.078f, -0.236f))
    }
    // Belly.
    val belly = Rect(-0.058f * u, -0.115f * u, 0.058f * u, -0.034f * u)
    drawOval(purple.lighten(0.5f), belly.topLeft, belly.size)
    for (k in 0 until 4) drawLine(purple.darken(0.2f), Offset(belly.left + (0.2f + k * 0.2f) * belly.width, belly.bottom - 0.012f * u), Offset(belly.left + (0.2f + k * 0.2f) * belly.width + 0.004f * u, belly.bottom - 0.026f * u), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
    // Arms: one waves a sock.
    val arm = Path().apply { addOval(Rect(p(-0.14f, -0.12f), p(-0.095f, -0.07f))) }
    inked(arm, purple, pen, shade = false)
    val wave = sin(t * 2.6f) * 10f
    rotate(wave, p(0.115f, -0.1f)) {
        val hand = Path().apply { addOval(Rect(p(0.09f, -0.14f), p(0.14f, -0.095f))) }
        inked(hand, purple, pen, shade = false)
    }
    // The mouth opens wide when somebody holds a sock near.
    val mouthW = 0.13f * u
    val mouthH = (0.036f + 0.05f * hunger) * u
    val mc = p(0f, -0.1f - 0.012f * hunger)
    val mouth = Path().apply { addOval(Rect(mc.x - mouthW / 2f, mc.y - mouthH / 2f, mc.x + mouthW / 2f, mc.y + mouthH / 2f)) }
    drawPath(mouth, Color(0xFF3A1E4A))
    clipPath(mouth) {
        drawOval(CeC.pink, Offset(mc.x - mouthW * 0.28f, mc.y + mouthH * 0.12f), Size(mouthW * 0.56f, mouthH * 0.5f))
        for (k in 0 until 5) {
            val tx = mc.x - mouthW * 0.4f + k * mouthW * 0.2f
            val tooth = Path().apply { moveTo(tx - 0.009f * u, mc.y - mouthH / 2f); lineTo(tx + 0.009f * u, mc.y - mouthH / 2f); lineTo(tx, mc.y - mouthH / 2f + 0.022f * u); close() }
            drawPath(tooth, Color.White)
        }
        for (k in 0 until 3) {
            val tx = mc.x - mouthW * 0.25f + k * mouthW * 0.25f
            val tooth = Path().apply { moveTo(tx - 0.008f * u, mc.y + mouthH / 2f); lineTo(tx + 0.008f * u, mc.y + mouthH / 2f); lineTo(tx, mc.y + mouthH / 2f - 0.018f * u); close() }
            drawPath(tooth, Color.White)
        }
        // Until the key is out, something golden gleams deep in his belly.
        if (f.count < 3) {
            val gp = Offset(mc.x + mouthW * 0.2f, mc.y + mouthH * 0.12f)
            twinkle(gp, 0.016f * u * (0.8f + 0.5f * cePulse(t, 1.1f)), CeC.gold, 0.9f)
            drawCircle(CeC.gold, 0.004f * u, gp)
        }
    }
    drawPath(mouth, Ink.line, style = pen.stroke)
    // A sock sticks out of the corner of his mouth, swinging.
    translate(mc.x + mouthW * 0.45f, mc.y) {
        rotate(sin(t * 2.1f) * 9f, Offset.Zero) {
            rotate(180f, Offset.Zero) { ceSock(0.04f * u, 0.05f * u, 0, pen) }
        }
    }
    // Eyes: a big one and a small one, and they blink.
    val blink = ceFrac(t, 3.4f, 0.3f) < 0.045f
    val bigger = 1f + 0.25f * hunger
    for ((i, ex) in floatArrayOf(-0.042f, 0.046f).withIndex()) {
        val r = (if (i == 0) 0.033f else 0.025f) * u * bigger
        val c = p(ex, -0.181f)
        if (blink) {
            drawLine(Ink.line, Offset(c.x - r, c.y), Offset(c.x + r, c.y), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
        } else {
            inkedCircle(c, r, Color.White, pen, shade = false)
            val look = Offset(sin(t * 0.9f + i) * r * 0.25f, -r * 0.05f * hunger - r * 0.1f * hunger + sin(t * 0.6f) * r * 0.1f)
            drawCircle(Ink.line, r * 0.48f, Offset(c.x + look.x, c.y + look.y))
            drawCircle(Color.White, r * 0.16f, Offset(c.x + look.x - r * 0.15f, c.y + look.y - r * 0.18f))
        }
    }
    // Fur tufts on the head.
    for (k in -1..1) {
        val tf = Path().apply {
            moveTo(p(k * 0.02f - 0.008f, -0.222f).x, p(k * 0.02f - 0.008f, -0.222f).y)
            lineTo(p(k * 0.02f, -0.25f + abs(k) * 0.008f).x, p(k * 0.02f, -0.25f + abs(k) * 0.008f).y)
            lineTo(p(k * 0.02f + 0.008f, -0.222f).x, p(k * 0.02f + 0.008f, -0.222f).y)
            close()
        }
        inked(tf, purple.darken(0.1f), pen, shade = false)
    }
}

private fun abs(v: Int) = if (v < 0) -v else v

// ============================================================================================== basket

/** The laundry basket: wicker round a heap of washing. Whoever falls from the chute lands on the heap. */
internal fun DrawScope.ceBasket(f: Fixture, u: Float, pen: Pen) {
    val d = 0.12f
    val squash = 1f + f.angle * 0.1f
    ceShadow(u, 0.34f, d)
    // The back of the rim and the inside.
    val rim = fxDisc2(0f, -0.12f * u, 0.165f * u, d * 0.5f * u)
    fxFace(rim, CeC.woodDark.darken(0.15f), pen)
    // Wicker body: a trapezoid, narrower at the bottom, with woven bands.
    ceBasketWall(u, pen)
    // The heap of washing.
    val heap = listOf(
        Triple(-0.1f, -0.13f, 0.055f) to CeC.sockBody[1],
        Triple(0.02f, -0.15f, 0.06f) to CeC.sockTrim[1],
        Triple(0.1f, -0.13f, 0.05f) to Color(0xFFFF8FB1),
        Triple(-0.03f, -0.135f, 0.05f) to Color(0xFF6BCB77),
    )
    translate(0f, -0.12f * u) {
        scale2(squash) {
            for ((spec, color) in heap) {
                val (x, y, w) = spec
                val r = Rect((x - w / 2f) * u, (y + 0.12f - w * 0.45f) * u, (x + w / 2f) * u, (y + 0.12f + w * 0.2f) * u)
                inkedRound(r, w * 0.35f * u, color, pen)
            }
            // A striped sock hangs over the rim.
            translate(0.075f * u, -0.03f * u) { rotate(10f, Offset.Zero) { ceSock(0.04f * u, 0.045f * u, 0, pen) } }
        }
    }
}

private fun DrawScope.scale2(k: Float, block: DrawScope.() -> Unit) = scale(1f, k, Offset.Zero, block)

private fun DrawScope.ceBasketWall(u: Float, pen: Pen) {
    val wall = fxQuad(-0.165f * u, -0.12f * u, 0.165f * u, -0.12f * u, 0.128f * u, 0f, -0.128f * u, 0f, 0.012f * u)
    fxFace(wall, CeC.woodLight.darken(0.05f), pen)
    clipPath(wall) {
        for (k in 0 until 4) {
            val y = (-0.108f + k * 0.03f) * u
            drawLine(CeC.woodDark, Offset(-0.17f * u, y), Offset(0.17f * u, y), strokeWidth = pen.lw * 0.9f)
        }
        for (k in -8..8) {
            val x = k * 0.02f * u
            val row = k.mod(2)
            drawLine(CeC.woodDark.copy(alpha = 0.8f), Offset(x, (-0.12f + row * 0.03f) * u), Offset(x, (-0.09f + row * 0.03f) * u), strokeWidth = pen.lw * 0.7f)
            drawLine(CeC.woodDark.copy(alpha = 0.8f), Offset(x + 0.01f * u, (-0.06f + row * 0.03f) * u), Offset(x + 0.01f * u, (-0.03f + row * 0.03f) * u), strokeWidth = pen.lw * 0.7f)
        }
    }
    // The rim rope.
    drawLine(Ink.line, Offset(-0.168f * u, -0.12f * u), Offset(0.168f * u, -0.12f * u), strokeWidth = 0.011f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(CeC.woodLight, Offset(-0.168f * u, -0.12f * u), Offset(0.168f * u, -0.12f * u), strokeWidth = 0.011f * u, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.5f), Offset(-0.15f * u, -0.123f * u), Offset(0.05f * u, -0.123f * u), strokeWidth = 0.003f * u, cap = StrokeCap.Round)
    // Two little handles.
    for (s in floatArrayOf(-1f, 1f)) {
        val h = Path().apply {
            moveTo(s * 0.166f * u, -0.105f * u)
            quadraticTo(s * 0.2f * u, -0.1f * u, s * 0.17f * u, -0.07f * u)
        }
        drawPath(h, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(h, CeC.woodLight, style = Stroke(0.009f * u, cap = StrokeCap.Round))
    }
}

/** What lies in front of whoever sits in the basket: the wicker front and two lumps of washing. */
internal fun DrawScope.ceBasketFront(f: Fixture, u: Float, pen: Pen) {
    ceBasketWall(u, pen)
    val squash = 1f + f.angle * 0.1f
    translate(0f, -0.12f * u) {
        scale2(squash) {
            inkedRound(Rect(-0.085f * u, -0.04f * u, -0.03f * u, 0.008f * u), 0.014f * u, CeC.sockBody[2], pen)
            inkedRound(Rect(0.05f * u, -0.035f * u, 0.11f * u, 0.008f * u), 0.014f * u, Color(0xFFFFE08A), pen)
            drawLine(Ink.line.copy(alpha = 0.5f), Offset(0.06f * u, -0.012f * u), Offset(0.1f * u, -0.014f * u), strokeWidth = pen.lw * 0.7f)
        }
    }
}

// ========================================================================================== clothes line

internal fun DrawScope.ceClothesline(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val swing = f.angle
    val sag = 0.012f
    fun lineY(x: Float) = -0.185f + sag * (1f - (x / 0.3f) * (x / 0.3f))
    // Two hooks and the line between them.
    for (s in floatArrayOf(-1f, 1f)) {
        inkedCircle(p(s * 0.3f, -0.19f), 0.007f * u, CeC.steel, pen, shade = false)
        drawLine(Ink.line, p(s * 0.3f, -0.19f), p(s * 0.3f, -0.2f), strokeWidth = pen.lw * 1.4f)
    }
    val line = Path().apply {
        moveTo(p(-0.3f, -0.19f).x, p(-0.3f, -0.19f).y)
        quadraticTo(0f, (-0.19f + sag * 2f) * u, p(0.3f, -0.19f).x, p(0.3f, -0.19f).y)
    }
    drawPath(line, Ink.line, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    // Things on the line, each swinging from its peg.
    data class Item(val x: Float, val kind: Int, val color: Color)
    val items = listOf(
        Item(-0.24f, 0, CeC.sockBody[1]), Item(-0.14f, 1, Color(0xFFFFE08A)), Item(-0.06f, 2, CeC.sockBody[0]),
        Item(0.0f, 2, CeC.sockBody[2]), Item(0.07f, 3, Color(0xFF6BCB77)), Item(0.16f, 2, CeC.sockBody[4]), Item(0.25f, 0, CeC.sockBody[3]),
    )
    for ((i, it) in items.withIndex()) {
        val y = lineY(it.x)
        val a = (swing * (0.55f + 0.1f * (i % 3)) + sin(t * 1.1f + i * 0.9f) * 0.025f) * 57.3f
        rotate(a, p(it.x, y)) {
            translate(it.x * u, y * u) {
                when (it.kind) {
                    0 -> { // a T-shirt
                        val sh = Path().apply {
                            moveTo(-0.03f * u, 0f); lineTo(-0.05f * u, 0.02f * u); lineTo(-0.04f * u, 0.04f * u); lineTo(-0.028f * u, 0.032f * u)
                            lineTo(-0.028f * u, 0.11f * u); lineTo(0.028f * u, 0.11f * u); lineTo(0.028f * u, 0.032f * u)
                            lineTo(0.04f * u, 0.04f * u); lineTo(0.05f * u, 0.02f * u); lineTo(0.03f * u, 0f)
                            quadraticTo(0f, 0.014f * u, -0.03f * u, 0f); close()
                        }
                        inked(sh, it.color, pen)
                        drawLine(Color.White, Offset(-0.028f * u, 0.06f * u), Offset(0.028f * u, 0.06f * u), strokeWidth = pen.lw * 1.6f)
                        drawLine(Color.White, Offset(-0.028f * u, 0.08f * u), Offset(0.028f * u, 0.08f * u), strokeWidth = pen.lw * 1.6f)
                    }
                    1 -> { // a spotted towel
                        val tw = Rect(-0.035f * u, 0f, 0.035f * u, 0.12f * u)
                        inkedRound(tw, 0.004f * u, it.color, pen)
                        for (k in 0 until 6) drawCircle(Color.White, 0.005f * u, Offset((-0.016f + (k % 2) * 0.032f) * u, (0.025f + (k / 2) * 0.03f) * u), alpha = 0.8f)
                    }
                    2 -> translate(0.012f * u, 0.065f * u) { rotate(180f, Offset.Zero) { ceSock(0.04f * u, 0.065f * u, if (it.color == CeC.sockBody[0]) 0 else if (it.color == CeC.sockBody[2]) 2 else 4, pen) } }
                    else -> { // a pair of little trousers
                        val tr = Path().apply {
                            moveTo(-0.025f * u, 0f); lineTo(0.025f * u, 0f); lineTo(0.03f * u, 0.1f * u); lineTo(0.005f * u, 0.1f * u)
                            lineTo(0f, 0.04f * u); lineTo(-0.005f * u, 0.1f * u); lineTo(-0.03f * u, 0.1f * u); close()
                        }
                        inked(tr, it.color, pen)
                    }
                }
                // The clothes peg.
                inkedRound(Rect(-0.005f * u, -0.012f * u, 0.005f * u, 0.014f * u), 0.002f * u, CeC.woodLight, pen, shade = false)
            }
        }
    }
}

// ====================================================================================== ironing board

internal fun DrawScope.ceIronBoard(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    ceShadow(u, 0.3f, 0.1f)
    // Legs: two crossing pairs.
    for (s in floatArrayOf(-1f, 1f)) {
        val x = s * 0.07f
        capsule(p(x - 0.045f, -0.002f), p(x + 0.045f, -0.13f), 0.006f * u, CeC.steel, pen)
        capsule(p(x + 0.045f, -0.002f), p(x - 0.045f, -0.13f), 0.006f * u, CeC.steel, pen)
    }
    // The board: a long rounded slab with a pointed nose, a padded blue cover.
    val top = Path().apply {
        moveTo(p(-0.13f, -0.146f).x, p(-0.13f, -0.146f).y)
        lineTo(p(0.09f, -0.146f).x, p(0.09f, -0.146f).y)
        quadraticTo(p(0.15f, -0.146f).x, p(0.15f, -0.146f).y, p(0.155f, -0.134f).x, p(0.155f, -0.134f).y)
        lineTo(p(-0.13f, -0.134f).x, p(-0.13f, -0.134f).y)
        close()
    }
    fxBox(u, -0.13f, -0.146f, 0.09f, -0.134f, 0.08f, Color(0xFF7CCBFF), pen, rad = 0.004f, z = 0f)
    drawPath(top, Color(0xFF9BDAFF))
    drawPath(top, Ink.line, style = pen.stroke)
    for (k in 0 until 8) drawCircle(Color.White, 0.0035f * u, p(-0.11f + k * 0.032f, -0.14f), alpha = 0.7f)
    // The iron glides back and forth while f.timer runs; steam hisses from it.
    val prog = if (f.timer > 0f) 1f - f.timer / 1.3f else 0f
    val ix = -0.05f + 0.15f * (0.5f - 0.5f * cos(prog * 2f * PI.toFloat() * 1.5f).toFloat()) * (if (f.timer > 0f) 1f else 0f)
    val iron = Path().apply {
        moveTo(p(ix - 0.035f, -0.148f).x, p(ix - 0.035f, -0.148f).y)
        lineTo(p(ix + 0.03f, -0.148f).x, p(ix + 0.03f, -0.148f).y)
        quadraticTo(p(ix + 0.05f, -0.15f).x, p(ix + 0.05f, -0.15f).y, p(ix + 0.052f, -0.163f).x, p(ix + 0.052f, -0.163f).y)
        quadraticTo(p(ix + 0.03f, -0.185f).x, p(ix + 0.03f, -0.185f).y, p(ix - 0.03f, -0.185f).x, p(ix - 0.03f, -0.185f).y)
        quadraticTo(p(ix - 0.04f, -0.165f).x, p(ix - 0.04f, -0.165f).y, p(ix - 0.035f, -0.148f).x, p(ix - 0.035f, -0.148f).y)
        close()
    }
    inked(iron, CeC.teal, pen)
    drawRect(Color.White, p(ix - 0.034f, -0.153f), Size(0.082f * u, 0.006f * u))
    val handle = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(p(ix - 0.02f, -0.2f), p(ix + 0.032f, -0.184f)), androidx.compose.ui.geometry.CornerRadius(0.007f * u))) }
    inked(handle, CeC.navy, pen, shade = false)
    drawCircle(CeC.yellow, 0.0042f * u, p(ix + 0.002f, -0.172f))
    drawCircle(Ink.line, 0.0042f * u, p(ix + 0.002f, -0.172f), style = pen.thin)
    if (f.timer > 0f) fxPuffs(p(ix + 0.05f, -0.172f).x, p(ix + 0.05f, -0.172f).y, t, 0.012f * u, 0.07f * u, CeC.steam, 0.8f, 4, 0.9f, 0.015f * u)
    // A folded towel at the left end, and a spot of washing powder that blinks.
    inkedRound(Rect(p(-0.125f, -0.172f), p(-0.085f, -0.146f)), 0.004f * u, Color(0xFFFFE08A), pen)
    drawLine(Ink.line.copy(alpha = 0.5f), p(-0.122f, -0.16f), p(-0.088f, -0.16f), strokeWidth = pen.lw * 0.6f)
    drawCircle(CeC.green, 0.004f * u, p(0.12f, -0.05f), alpha = 0.4f + 0.6f * cePulse(t, 2f))
}

// ==================================================================================================== chute

/** The laundry chute from the bathroom above: a steel duct from the ceiling ending in a flap that bangs open. */
internal fun DrawScope.ceChute(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val open = (f.timer / 0.9f).coerceIn(0f, 1f)
    // The duct runs up to the pipes under the ceiling.
    val duct = Rect(p(-0.045f, -0.34f), p(0.045f, -0.1f))
    fxBox(u, -0.045f, -0.34f, 0.045f, -0.1f, 0.06f, CeC.steel, pen, rad = 0.006f, top = CeC.steel.lighten(0.4f), side = CeC.steel.darken(0.25f))
    for (k in 0 until 3) drawLine(Ink.line.copy(alpha = 0.5f), p(-0.045f, -0.3f + k * 0.07f), p(0.045f, -0.3f + k * 0.07f), strokeWidth = pen.lw * 0.7f)
    for (k in 0 until 2) {
        val y = -0.32f + k * 0.12f
        inkedRound(Rect(p(-0.052f, y), p(0.052f, y + 0.014f)), 0.003f * u, CeC.ironLight, pen, shade = false)
    }
    // A flared mouth with the flap.
    val mouth = fxQuad(-0.045f * u, -0.1f * u, 0.045f * u, -0.1f * u, 0.078f * u, -0.012f * u, -0.078f * u, -0.012f * u, 0.004f * u)
    fxFace(mouth, CeC.steel.darken(0.1f), pen)
    val hole = fxDisc2(0f, -0.014f * u, 0.07f * u, 0.02f * u)
    fxFace(hole, Color(0xFF2A2F3D), pen)
    // The flap swings out from its hinge when somebody comes down.
    val hinge = p(0f, -0.098f)
    rotate(-open * 70f, hinge) {
        val flap = Path().apply {
            moveTo(hinge.x - 0.04f * u, hinge.y)
            lineTo(hinge.x + 0.04f * u, hinge.y)
            lineTo(hinge.x + 0.068f * u, hinge.y + 0.075f * u)
            lineTo(hinge.x - 0.068f * u, hinge.y + 0.075f * u)
            close()
        }
        inked(flap, CeC.iron.lighten(0.2f), pen)
        drawLine(Color.White.copy(alpha = 0.4f), Offset(hinge.x - 0.04f * u, hinge.y + 0.012f * u), Offset(hinge.x + 0.03f * u, hinge.y + 0.012f * u), strokeWidth = pen.lw)
    }
    // A yellow arrow sign pointing down, with a sock.
    val sign = Rect(p(-0.03f, -0.255f), p(0.03f, -0.19f))
    inkedRound(sign, 0.005f * u, CeC.yellow, pen, shade = false)
    val arrow = Path().apply {
        moveTo(sign.center.x, sign.bottom - 0.008f * u)
        lineTo(sign.center.x - 0.016f * u, sign.center.y + 0.006f * u)
        lineTo(sign.center.x - 0.006f * u, sign.center.y + 0.006f * u)
        lineTo(sign.center.x - 0.006f * u, sign.top + 0.01f * u)
        lineTo(sign.center.x + 0.006f * u, sign.top + 0.01f * u)
        lineTo(sign.center.x + 0.006f * u, sign.center.y + 0.006f * u)
        lineTo(sign.center.x + 0.016f * u, sign.center.y + 0.006f * u)
        close()
    }
    drawPath(arrow, CeC.navy)
    // Dust drifts out when the flap bangs.
    if (open > 0.3f) fxPuffs(p(0f, -0.02f).x, p(0f, -0.02f).y, t, 0.016f * u, 0.07f * u, Color(0xFFE6E0F0), 0.6f * open, 4, 1.1f, 0.02f * u)
    // A cobweb in the corner of the duct.
    if (open == 0f) drawCircle(Color.White.copy(alpha = 0.5f + 0.3f * sin(t * 0.9f)), 0.003f * u, p(0.04f, -0.105f))
    min(0f, 0f)
}
