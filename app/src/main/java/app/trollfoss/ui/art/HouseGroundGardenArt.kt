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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The winter garden of Storstova in oblique 3D: the plants that grow when they are watered, the fountain,
// Sofie the carnivorous plant, and the hammock.

// ------------------------------------------------------------------------------------------------ the plants

internal fun DrawScope.grPlant(f: Fixture, u: Float, pen: Pen) {
    when (f.variant.mod(3)) {
        0 -> grFern(f, u, pen)
        1 -> grPalm(f, u, pen)
        else -> grFlowers(f, u, pen)
    }
}

/** Sway of a plant: gentle all the time, and a flutter while it is shaken or watered. */
private fun plantSway(f: Fixture, pen: Pen, k: Float = 1f): Float {
    val shake = if (f.timer > 0f) sin(pen.t * 22f) * 0.7f * min(1f, f.timer) else 0f
    return (sin(pen.t * 0.9f + f.id) * 0.8f + shake) * k
}

private fun DrawScope.ripeSparkle(f: Fixture, u: Float, pen: Pen, at: Offset) {
    if (f.mode < 3) return
    val a = 0.5f + 0.5f * sin(pen.t * 3f + f.id)
    twinkle(Offset(at.x + 0.03f * u, at.y - 0.02f * u * a), 0.014f * u, Color(0xFFFFE680), a)
    twinkle(Offset(at.x - 0.04f * u, at.y + 0.02f * u), 0.01f * u, Color.White, 1f - a)
}

private fun DrawScope.waterGlints(f: Fixture, u: Float, pen: Pen) {
    if (f.timer <= 0f) return
    val a = min(1f, f.timer)
    for (k in 0 until 5) {
        val ph = fxFrac(pen.t * 1.6f + k * 0.2f)
        drawCircle(Color(0xFF9ADAFF).copy(alpha = a * (1f - ph)), 0.004f * u, Offset((-0.05f + k * 0.025f) * u, (-0.3f + ph * 0.24f) * u))
    }
}

private fun DrawScope.fernFrond(base: Offset, ang: Float, len: Float, u: Float, pen: Pen, color: Color, sway: Float) {
    // An arching frond: a spine with leaflets on both sides, tapering to the tip.
    val a = ang + sway * 0.02f
    val tipX = base.x + sin(a) * len * u
    val tipY = base.y - cos(a) * len * u * 0.9f + len * u * 0.1f
    val ctrl = Offset(base.x + sin(a * 0.5f) * len * u * 0.7f, base.y - len * u * 0.85f)
    val spine = Path().apply { moveTo(base.x, base.y); quadraticTo(ctrl.x, ctrl.y, tipX, tipY) }
    drawPath(spine, Ink.line, style = Stroke(0.006f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(spine, color.darken(0.25f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
    val n = (len * 70f).toInt().coerceIn(5, 18)
    for (i in 1..n) {
        val tt = i / (n + 1f)
        val px = (1 - tt) * (1 - tt) * base.x + 2 * (1 - tt) * tt * ctrl.x + tt * tt * tipX
        val py = (1 - tt) * (1 - tt) * base.y + 2 * (1 - tt) * tt * ctrl.y + tt * tt * tipY
        val s = (0.022f * (1f - tt * 0.7f)) * u
        for (side in -1..1 step 2) {
            val leaf = Path().apply {
                moveTo(px, py)
                quadraticTo(px + side * s * 0.8f, py - s * 0.9f, px + side * s * 1.6f, py - s * 0.2f)
                quadraticTo(px + side * s * 0.8f, py - s * 0.1f, px, py)
            }
            drawPath(leaf, color)
            drawPath(leaf, Ink.line, style = Stroke(pen.lw * 0.5f))
        }
    }
}

private fun DrawScope.grFern(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val stage = f.mode.coerceIn(0, 3)
    grShadow(u, 0.16f, 0.08f)
    val sway = plantSway(f, pen)
    val fronds = intArrayOf(3, 5, 7, 9)[stage]
    val len = floatArrayOf(0.1f, 0.17f, 0.24f, 0.29f)[stage]
    val col = lerp(Color(0xFF6FC27A), Color(0xFF3F9A55), stage / 3f)
    val base = p(0f, -0.075f)
    for (i in 0 until fronds) {
        val a = -1.15f + (2.3f * i / (fronds - 1).coerceAtLeast(1))
        val l = len * (0.78f + 0.22f * cos(a * 1.4f)) * (0.9f + 0.1f * hash01(i, f.id))
        fernFrond(base, a, l, u, pen, if (i % 2 == 0) col else col.lighten(0.1f), sway)
    }
    if (stage >= 2) {
        // Fiddleheads curling up in the middle.
        for (k in -1..1 step 2) {
            val c = Offset(base.x + k * 0.02f * u, base.y - 0.06f * u)
            drawArc(Color(0xFF8BD450), 0f, 280f, false, Offset(c.x - 0.012f * u, c.y - 0.012f * u), Size(0.024f * u, 0.024f * u), style = Stroke(0.005f * u, cap = StrokeCap.Round))
        }
    }
    grPot(f, u, pen, Color(0xFFC8744F), 0.062f, 0.085f)
    waterGlints(f, u, pen)
    ripeSparkle(f, u, pen, p(0f, -0.3f))
}

private fun DrawScope.grPot(f: Fixture, u: Float, pen: Pen, color: Color, halfTop: Float, h: Float) {
    val pot = Path().apply {
        moveTo(-halfTop * u, -h * u)
        lineTo(halfTop * u, -h * u)
        lineTo((halfTop * 0.78f) * u, 0f)
        lineTo(-(halfTop * 0.78f) * u, 0f)
        close()
    }
    inked(pot, color, pen)
    inkedRound(Rect((-halfTop - 0.006f) * u, (-h - 0.012f) * u, (halfTop + 0.006f) * u, -h * u + 0.004f * u), 0.003f * u, color.lighten(0.1f), pen)
    // Soil, and a pattern of little dots.
    drawOval(Color(0xFF4A3426), Offset((-halfTop + 0.006f) * u, (-h - 0.014f) * u), Size((2f * halfTop - 0.012f) * u, 0.012f * u))
    for (k in 0..3) drawCircle(Color.White.copy(alpha = 0.55f), 0.0042f * u, Offset((-halfTop * 0.6f + k * halfTop * 0.4f) * u, (-h * 0.45f + (k % 2) * 0.02f) * u))
    shine(Offset(-halfTop * 0.55f * u, -h * 0.55f * u), 0.006f * u, 0.03f * u, 0.5f)
}

private fun DrawScope.grPalm(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val stage = f.mode.coerceIn(0, 3)
    grShadow(u, 0.16f, 0.08f)
    val sway = plantSway(f, pen)
    val leaves = intArrayOf(2, 3, 5, 7)[stage]
    val h = floatArrayOf(0.12f, 0.17f, 0.25f, 0.3f)[stage]
    val trunkTop = p(sin(sway * 0.02f) * 0.01f, -0.1f - h * 0.65f)
    // A slim trunk.
    val trunk = Path().apply { moveTo(0f, -0.1f * u); quadraticTo(0.008f * u, (-0.1f - h * 0.3f) * u, trunkTop.x, trunkTop.y) }
    drawPath(trunk, Ink.line, style = Stroke(0.014f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(trunk, Color(0xFF8A5A3C), style = Stroke(0.014f * u, cap = StrokeCap.Round))
    for (k in 1..4) drawLine(Color(0xFF6E4630), Offset(trunkTop.x - 0.007f * u, mix(-0.1f * u, trunkTop.y, k / 5f)), Offset(trunkTop.x + 0.007f * u, mix(-0.1f * u, trunkTop.y, k / 5f) + 0.004f * u), pen.lw * 0.7f)
    // Big paddle leaves that fan out and droop at the tips.
    for (i in 0 until leaves) {
        val a = -1.3f + 2.6f * i / (leaves - 1).coerceAtLeast(1)
        val l = (0.1f + h * 0.55f) * (0.85f + 0.15f * cos(a))
        val s = sway * 0.02f * (1f + abs(a))
        val tip = Offset(trunkTop.x + sin(a + s) * l * u, trunkTop.y - cos(a + s) * l * u * 0.55f + l * u * 0.2f)
        val ctrl = Offset(trunkTop.x + sin(a + s) * l * u * 0.5f, trunkTop.y - l * u * 0.75f)
        val spine = Path().apply { moveTo(trunkTop.x, trunkTop.y); quadraticTo(ctrl.x, ctrl.y, tip.x, tip.y) }
        val leaf = Path().apply {
            moveTo(trunkTop.x, trunkTop.y)
            quadraticTo(ctrl.x - 0.02f * u, ctrl.y - 0.01f * u, tip.x, tip.y)
            quadraticTo(ctrl.x + 0.03f * u, ctrl.y + 0.02f * u, trunkTop.x, trunkTop.y)
            close()
        }
        inked(leaf, if (i % 2 == 0) Color(0xFF3F9A55) else Color(0xFF5DB36B), pen)
        drawPath(spine, Color(0xFF2E7D45), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
        for (j in 1..4) {
            val tt = j / 5f
            val px = (1 - tt) * (1 - tt) * trunkTop.x + 2 * (1 - tt) * tt * ctrl.x + tt * tt * tip.x
            val py = (1 - tt) * (1 - tt) * trunkTop.y + 2 * (1 - tt) * tt * ctrl.y + tt * tt * tip.y
            drawLine(Color(0xFF2E7D45), Offset(px, py), Offset(px + 0.014f * u, py + 0.012f * u), pen.lw * 0.6f)
            drawLine(Color(0xFF2E7D45), Offset(px, py), Offset(px - 0.014f * u, py + 0.012f * u), pen.lw * 0.6f)
        }
    }
    // Small pink flowers in the crown when it is ripe.
    if (stage >= 3) {
        for (k in 0 until 3) {
            val c = Offset(trunkTop.x + (k - 1) * 0.022f * u, trunkTop.y + 0.01f * u - (k % 2) * 0.012f * u)
            for (j in 0 until 5) {
                val a = j * 2f * PI.toFloat() / 5f
                drawCircle(Color(0xFFFF8FB1), 0.005f * u, Offset(c.x + cos(a) * 0.007f * u, c.y + sin(a) * 0.007f * u))
            }
            drawCircle(Color(0xFFFFE066), 0.004f * u, c)
        }
    }
    grPot(f, u, pen, Color(0xFF3B6EA5), 0.07f, 0.1f)
    drawLine(GrC.brass, p(-0.065f, -0.088f), p(0.065f, -0.088f), pen.lw * 1.4f)
    waterGlints(f, u, pen)
    ripeSparkle(f, u, pen, p(0f, -0.3f))
}

private fun DrawScope.grFlowers(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val stage = f.mode.coerceIn(0, 3)
    grShadow(u, 0.16f, 0.08f)
    val sway = plantSway(f, pen)
    val stems = intArrayOf(3, 4, 5, 6)[stage]
    val cols = intArrayOf(0xFFFF8FB1.toInt(), 0xFFFFC83D.toInt(), 0xFFB57BFF.toInt(), 0xFFFF6B6B.toInt(), 0xFF7FD3E8.toInt(), 0xFFFFFFFF.toInt())
    // The wooden planter box.
    val box = Rect(-0.075f * u, -0.07f * u, 0.075f * u, 0f)
    // Stems with leaves and flowers, in front of the soil.
    for (i in 0 until stems) {
        val x = -0.06f + 0.12f * i / (stems - 1)
        val h = (0.05f + stage * 0.055f) * (0.8f + 0.4f * hash01(i, 77 + f.id))
        val s = sway * 0.012f * (0.5f + hash01(i, 5)) * u
        val top = Offset(x * u + s, (-0.07f - h) * u)
        val stem = Path().apply { moveTo(x * u, -0.07f * u); quadraticTo(x * u + s * 0.4f, (-0.07f - h * 0.5f) * u, top.x, top.y) }
        drawPath(stem, Ink.line, style = Stroke(0.006f * u + pen.lw * 1.6f, cap = StrokeCap.Round))
        drawPath(stem, Color(0xFF2E8B57), style = Stroke(0.006f * u, cap = StrokeCap.Round))
        // Two leaves.
        for (side in -1..1 step 2) {
            val ly = -0.07f - h * 0.35f
            val leaf = Path().apply {
                moveTo(x * u + s * 0.2f, ly * u)
                quadraticTo(x * u + side * 0.022f * u, (ly - 0.012f) * u, x * u + side * 0.03f * u, (ly + 0.002f) * u)
                quadraticTo(x * u + side * 0.014f * u, (ly + 0.008f) * u, x * u + s * 0.2f, ly * u)
            }
            inked(leaf, Color(0xFF4FAE5A), pen, shade = false)
        }
        val col = Color(cols[i % cols.size])
        if (stage == 0) {
            inkedCircle(top, 0.004f * u, Color(0xFF8BD450), pen, shade = false)
        } else if (stage == 1) {
            inkedCircle(top, 0.007f * u, col.darken(0.15f), pen, shade = false)
        } else {
            // A flower with petals around a golden heart.
            val n = if (stage == 2) 5 else 7
            val r = (0.011f + stage * 0.002f) * u
            for (k in 0 until n) {
                val a = k * 2f * PI.toFloat() / n + sway * 0.05f
                inkedCircle(Offset(top.x + cos(a) * r, top.y + sin(a) * r), r * 0.72f, col, pen, shade = false)
            }
            inkedCircle(top, r * 0.55f, Color(0xFFFFE066), pen, shade = false)
        }
    }
    // The planter in front.
    inkedRound(box, 0.006f * u, GrC.oak, pen)
    for (k in 1..3) drawLine(GrC.oakDark, Offset(box.left + box.width * k / 4f, box.top), Offset(box.left + box.width * k / 4f, box.bottom), pen.lw * 0.6f)
    drawLine(GrC.oakDark, Offset(box.left, box.top + 0.012f * u), Offset(box.right, box.top + 0.012f * u), pen.lw * 0.7f)
    // A butterfly visits the ripe flowers.
    if (stage >= 3) {
        val bx = sin(pen.t * 0.7f) * 0.08f
        val by = -0.24f + sin(pen.t * 1.4f) * 0.02f
        val flap = abs(cos(pen.t * 12f))
        val c = p(bx, by)
        val s = 0.01f * u
        val wing = Path().apply {
            moveTo(c.x, c.y)
            cubicTo(c.x - s * 1.4f, c.y - s * (1.4f * flap + 0.1f), c.x - s * 1.6f, c.y + s * 0.2f, c.x, c.y + s * 0.3f)
            cubicTo(c.x + s * 1.6f, c.y + s * 0.2f, c.x + s * 1.4f, c.y - s * (1.4f * flap + 0.1f), c.x, c.y)
            close()
        }
        drawPath(wing, Color(0xFFFFC83D))
        drawPath(wing, Ink.line, style = pen.thin)
    }
    waterGlints(f, u, pen)
    ripeSparkle(f, u, pen, p(0f, -0.26f))
}

// ------------------------------------------------------------------------------------------------ the fountain

internal fun DrawScope.grFountain(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val splash = if (f.timer > 0f) min(1f, f.timer) else 0f
    val c0 = q(0f, 0f, 0.11f)
    grShadow(u, 0.42f, 0.22f)
    // The round basin: a stone wall with a rim and the water inside.
    val rimY = -0.12f
    val outer = fxDisc(c0.x, c0.y + rimY * u, 0.2f * u)
    fxCyl(c0.x, c0.y, c0.y + rimY * u, 0.2f * u, 0.2f * u, GrC.stone, pen, top = GrC.stone.lighten(0.14f))
    // Stone courses on the wall.
    for (k in 1..2) drawLine(GrC.stoneDark.copy(alpha = 0.5f), Offset(c0.x - 0.2f * u, c0.y - k * 0.04f * u + 0.014f * u), Offset(c0.x + 0.2f * u, c0.y - k * 0.04f * u - 0.012f * u), pen.lw * 0.6f)
    val wc = Offset(c0.x, c0.y + (rimY + 0.012f) * u)
    val water = fxDisc(wc.x, wc.y, 0.17f * u)
    drawPath(water, Brush.verticalGradient(listOf(Color(0xFF7FD3E8), Color(0xFF3FA7C8)), wc.y - 0.06f * u, wc.y + 0.06f * u))
    drawPath(water, Ink.line, style = pen.thin)
    // Coins glinting on the bottom, ripples on the top.
    for (k in 0 until 5) {
        val a = k * 1.3f
        val g = max(0f, sin(t * 2f + k * 1.9f))
        drawCircle(Color(0xFFFFD66B).copy(alpha = 0.5f + 0.4f * g), 0.004f * u, Offset(wc.x + cos(a) * 0.1f * u, wc.y + sin(a) * 0.03f * u))
    }
    for (k in 0 until 3) {
        val ph = fxFrac(t * 0.5f + k * 0.33f + splash * 0.2f)
        drawPath(fxDisc2(wc.x, wc.y, 0.03f * u + ph * 0.12f * u, 0.02f * u + ph * 0.08f * u), Color.White.copy(alpha = 0.6f * (1f - ph)), style = Stroke(pen.lw * 0.8f))
    }
    // The pillar, the upper bowl and the finial.
    val pb = q(0f, rimY + 0.008f, 0.11f)
    val pt = q(0f, -0.25f, 0.11f)
    fxCyl(pb.x, pb.y, pt.y, 0.026f * u, 0.02f * u, GrC.stone.lighten(0.05f), pen, cap = false)
    val bowl = Path().apply {
        moveTo(pt.x - 0.075f * u, pt.y)
        quadraticTo(pt.x - 0.06f * u, pt.y + 0.03f * u, pt.x, pt.y + 0.034f * u)
        quadraticTo(pt.x + 0.06f * u, pt.y + 0.03f * u, pt.x + 0.075f * u, pt.y)
        close()
    }
    inked(bowl, GrC.stone, pen)
    val bowlTop = fxDisc2(pt.x, pt.y, 0.075f * u, 0.05f * u)
    drawPath(bowlTop, GrC.stone.lighten(0.12f))
    drawPath(fxDisc2(pt.x, pt.y, 0.062f * u, 0.04f * u), Color(0xFF7FD3E8))
    drawPath(bowlTop, Ink.line, style = pen.thin)
    // A smiling stone fish on top spouting water.
    val fc = Offset(pt.x, pt.y - 0.03f * u)
    inkedOval(Rect(fc.x - 0.018f * u, fc.y - 0.02f * u, fc.x + 0.018f * u, fc.y + 0.02f * u), GrC.stone.lighten(0.2f), pen)
    drawCircle(Color.White, 0.004f * u, Offset(fc.x - 0.006f * u, fc.y - 0.008f * u))
    drawCircle(Ink.line, 0.0022f * u, Offset(fc.x - 0.006f * u, fc.y - 0.008f * u))
    drawArc(Ink.line, 20f, 140f, false, Offset(fc.x - 0.008f * u, fc.y - 0.002f * u), Size(0.014f * u, 0.01f * u), style = pen.thin)
    val tail = Path().apply { moveTo(fc.x + 0.016f * u, fc.y + 0.01f * u); lineTo(fc.x + 0.032f * u, fc.y - 0.006f * u); lineTo(fc.x + 0.03f * u, fc.y + 0.018f * u); close() }
    inked(tail, GrC.stone.lighten(0.1f), pen, shade = false)
    // The jets: arcs of drops from the top of the fish, curtains over the edge of the bowl, and splash.
    val top = Offset(fc.x - 0.012f * u, fc.y - 0.014f * u)
    for (arm in -2..2) {
        for (k in 0 until 7) {
            val ph = fxFrac(t * 0.9f + k / 7f + arm * 0.07f)
            val reach = (0.055f + 0.02f * abs(arm) + 0.03f * splash) * u
            val x = top.x + arm * 0.35f * ph * reach
            val y = top.y - (sin(ph * PI.toFloat()) * (0.06f + 0.03f * splash) - ph * ph * 0.06f) * u + ph * 0.075f * u
            drawCircle(Color(0xFFBFEFFF).copy(alpha = 0.8f * (1f - ph * 0.5f)), 0.0034f * u, Offset(x, y))
        }
    }
    for (k in 0 until 10) {
        val a = k * 2f * PI.toFloat() / 10f
        val ph = fxFrac(t * 1.1f + k * 0.1f)
        val x = pt.x + cos(a) * 0.072f * u
        val y = pt.y + sin(a) * 0.045f * u + ph * 0.1f * u
        drawLine(Color(0xFFBFEFFF).copy(alpha = 0.7f * (1f - ph)), Offset(x, y), Offset(x, y + 0.014f * u), pen.lw * 1.1f, StrokeCap.Round)
    }
    // A frog sits on the rim, its throat pulsing.
    val fr = Offset(c0.x + 0.16f * u, c0.y + (rimY - 0.008f) * u)
    val puff = 1f + 0.18f * sin(t * 4f) + (if (splash > 0f) 0.3f * splash else 0f)
    inkedOval(Rect(fr.x - 0.017f * u, fr.y - 0.022f * u, fr.x + 0.017f * u, fr.y + 0.002f * u), Color(0xFF6FC27A), pen)
    inkedOval(Rect(fr.x - 0.009f * u * puff, fr.y - 0.008f * u, fr.x + 0.009f * u * puff, fr.y + 0.004f * u), Color(0xFFE8F8D0), pen, shade = false)
    for (s in -1..1 step 2) {
        val e = Offset(fr.x + s * 0.008f * u, fr.y - 0.024f * u)
        drawCircle(Color.White, 0.0052f * u, e)
        drawCircle(Ink.line, 0.0052f * u, e, style = pen.thin)
        drawCircle(Ink.line, 0.0024f * u, Offset(e.x, e.y + 0.001f * u))
    }
}

// ------------------------------------------------------------------------------------------------ Sofie

internal fun DrawScope.grSofie(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val chewing = f.timer > 0f
    val burping = f.angleV > 0f && f.angleV < 0.6f
    val hasKey = f.count < 2
    grShadow(u, 0.22f, 0.1f)
    val green = Color(0xFF4FAE5A)
    val greenDark = Color(0xFF2E7D45)
    val pink = Color(0xFFE85A8A)
    // How wide her mouth is: a lazy yawn now and then, a snap when she chews, a big O for a burp.
    val yawn = max(0f, sin(t * 0.9f + f.id)) * 0.25f
    val chomp = if (chewing) 0.15f + 0.45f * abs(sin(t * 11f)) else 0f
    val bigO = if (burping) 0.85f else 0f
    val open = max(max(yawn, chomp), bigO)
    val sway = sin(t * 1.1f + f.id) * 0.012f + (if (chewing) sin(t * 15f) * 0.006f else 0f)
    // The pot: burgundy with white spots, a golden rim.
    val pot = Path().apply {
        moveTo(-0.085f * u, -0.1f * u); lineTo(0.085f * u, -0.1f * u); lineTo(0.065f * u, 0f); lineTo(-0.065f * u, 0f); close()
    }
    // The stem: a thick curve that sways, with a belly where the key hides.
    val head = p(0.0f + sway * 4f, -0.245f)
    val stem = Path().apply {
        moveTo(0f, -0.09f * u)
        cubicTo(-0.05f * u, -0.14f * u, 0.05f * u, -0.18f * u, head.x, head.y + 0.045f * u)
    }
    drawPath(stem, Ink.line, style = Stroke(0.05f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(stem, green, style = Stroke(0.05f * u, cap = StrokeCap.Round))
    drawPath(stem, green.lighten(0.15f), style = Stroke(0.012f * u, cap = StrokeCap.Round), alpha = 0.6f)
    // The belly: a bulge with a golden glint of the key she swallowed.
    val belly = p(0.005f, -0.155f)
    inkedCircle(belly, 0.03f * u, green, pen, shade = false)
    if (hasKey) {
        val g = 0.55f + 0.45f * sin(t * 3f)
        val key = Path().apply {
            addOval(Rect(belly.x - 0.016f * u, belly.y - 0.012f * u, belly.x - 0.004f * u, belly.y))
            moveTo(belly.x - 0.004f * u, belly.y - 0.006f * u); lineTo(belly.x + 0.016f * u, belly.y - 0.006f * u)
            moveTo(belly.x + 0.01f * u, belly.y - 0.006f * u); lineTo(belly.x + 0.01f * u, belly.y + 0.004f * u)
        }
        drawPath(key, GrC.gold.copy(alpha = 0.5f + 0.5f * g), style = Stroke(0.004f * u, cap = StrokeCap.Round))
        twinkle(Offset(belly.x + 0.012f * u, belly.y - 0.014f * u), 0.016f * u * g, Color.White, g)
    } else {
        // Content and round, after a good meal.
        drawArc(Ink.line.copy(alpha = 0.6f), 20f, 140f, false, Offset(belly.x - 0.01f * u, belly.y), Size(0.02f * u, 0.012f * u), style = pen.thin)
    }
    // Two small leaves on the stem, waving.
    for (s in -1..1 step 2) {
        val lv = Path().apply {
            moveTo(0f, -0.125f * u)
            quadraticTo(s * 0.06f * u, -0.15f * u + sin(t * 1.5f + s) * 0.004f * u, s * 0.085f * u, -0.115f * u)
            quadraticTo(s * 0.05f * u, -0.115f * u, 0f, -0.125f * u)
        }
        inked(lv, greenDark.lighten(0.1f), pen, shade = false)
    }
    // The head: two big lobes with soft teeth, a pink mouth, long lashes and rosy cheeks.
    translate(head.x, head.y) {
        rotate(sway * 40f, Offset.Zero) {
            val jaw = open * 0.05f * u
            // The lower jaw.
            val lower = Path().apply {
                moveTo(-0.075f * u, 0.0f)
                quadraticTo(-0.06f * u, 0.07f * u + jaw, 0f, 0.075f * u + jaw)
                quadraticTo(0.06f * u, 0.07f * u + jaw, 0.075f * u, 0.0f)
                close()
            }
            inked(lower, green, pen)
            // The pink inside of the mouth.
            val mouth = Path().apply {
                moveTo(-0.07f * u, -0.002f * u)
                quadraticTo(0f, 0.045f * u + jaw * 0.9f, 0.07f * u, -0.002f * u)
                quadraticTo(0f, -0.012f * u, -0.07f * u, -0.002f * u)
                close()
            }
            drawPath(mouth, pink)
            drawPath(mouth, Ink.line, style = pen.thin)
            // A tongue.
            drawOval(Color(0xFFFF8FB1), Offset(-0.022f * u, 0.016f * u + jaw * 0.5f), Size(0.044f * u, 0.022f * u))
            // Soft round teeth along the lower lip.
            for (k in 0 until 7) {
                val x = -0.06f + k * 0.02f
                val ty = 0.0f + 0.02f * (1f - (abs(x) / 0.07f) * (abs(x) / 0.07f)) * 0.5f
                inkedCircle(Offset(x * u, (ty + 0.004f) * u + jaw * 0.1f), 0.0065f * u, Color(0xFFFFFDF6), pen, shade = false)
            }
            // The upper hood, domed, with darker spots.
            val upper = Path().apply {
                moveTo(-0.08f * u, 0.0f)
                cubicTo(-0.09f * u, -0.09f * u - jaw * 0.6f, 0.09f * u, -0.09f * u - jaw * 0.6f, 0.08f * u, 0.0f)
                quadraticTo(0f, -0.022f * u, -0.08f * u, 0.0f)
                close()
            }
            inked(upper, green, pen)
            for ((sx, sy) in listOf(-0.04f to -0.05f, 0.03f to -0.06f, 0.055f to -0.03f, -0.065f to -0.025f)) {
                drawCircle(greenDark.copy(alpha = 0.6f), 0.008f * u, Offset(sx * u, (sy - jaw / u * 0.3f) * u))
            }
            // The upper teeth.
            for (k in 0 until 5) {
                val x = -0.045f + k * 0.0225f
                inkedCircle(Offset(x * u, 0.004f * u), 0.0058f * u, Color(0xFFFFFDF6), pen, shade = false)
            }
            // Big friendly eyes with lashes.
            val blink = fxFrac(t * 0.27f) > 0.94f
            for (s in -1..1 step 2) {
                val e = Offset(s * 0.034f * u, (-0.052f - open * 0.01f) * u)
                drawCircle(Color.White, 0.019f * u, e)
                drawCircle(Ink.line, 0.019f * u, e, style = pen.thin)
                if (blink) {
                    drawLine(Ink.line, Offset(e.x - 0.014f * u, e.y), Offset(e.x + 0.014f * u, e.y), pen.lw * 1.4f, StrokeCap.Round)
                } else {
                    val look = if (chewing) 0f else sin(t * 0.6f) * 0.004f
                    drawCircle(Ink.line, 0.009f * u, Offset(e.x + look * u, e.y + 0.002f * u))
                    drawCircle(Color.White, 0.003f * u, Offset(e.x + look * u - 0.003f * u, e.y - 0.002f * u))
                }
                for (k in 0..2) {
                    val a = -PI.toFloat() * (0.15f + 0.3f * k) * (if (s < 0) 1f else -1f) - PI.toFloat() / 2f
                    drawLine(Ink.line, Offset(e.x + cos(a) * 0.019f * u, e.y + sin(a) * 0.019f * u), Offset(e.x + cos(a) * 0.029f * u, e.y + sin(a) * 0.029f * u), pen.lw * 1.1f, StrokeCap.Round)
                }
                drawOval(Color(0x66FF6F91), Offset(e.x + s * 0.01f * u - 0.01f * u, e.y + 0.024f * u), Size(0.02f * u, 0.011f * u))
            }
            // A burp: a green bubble drifting out of the mouth.
            if (burping) {
                val ph = 1f - (f.angleV / 0.6f).coerceIn(0f, 1f)
                drawCircle(Color(0xFFBFF0B0).copy(alpha = 0.7f), 0.02f * u * (0.5f + ph), Offset(0.02f * u, (0.05f + jaw / u - ph * 0.05f) * u))
            }
        }
    }
    inked(pot, Color(0xFFA73A4A), pen)
    inkedRound(Rect(-0.092f * u, -0.112f * u, 0.092f * u, -0.096f * u), 0.004f * u, GrC.gold, pen)
    clipPath(pot) { for (k in 0..4) drawCircle(Color.White.copy(alpha = 0.7f), 0.0055f * u, p(-0.06f + k * 0.03f, -0.07f + (k % 2) * 0.03f)) }
    shine(p(-0.055f, -0.06f), 0.006f * u, 0.03f * u, 0.5f)
}

// ------------------------------------------------------------------------------------------------ the hammock


internal fun DrawScope.grHammock(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val sway = f.angle
    grShadow(u, 0.5f, 0.12f)
    // Two posts of dark wood with brass rings and a finial, one at each end.
    for (s in -1..1 step 2) {
        val x = s * 0.235f
        val post = Rect((x - 0.011f) * u, -0.2f * u, (x + 0.011f) * u, 0f)
        inkedRound(post, 0.004f * u, GrC.walnutLight, pen)
        grKnob(p(x, -0.205f), 0.011f * u, pen)
        drawLine(GrC.brass, p(x - 0.011f, -0.1f), p(x + 0.011f, -0.1f), pen.lw * 1.4f)
        drawCircle(GrC.brass, 0.006f * u, p(x, -0.17f), style = Stroke(pen.lw * 1.4f))
    }
    // Ropes to the cloth, the cloth's far edge, a pillow, a knitted blanket.
    val l = p(-0.235f, -0.17f)
    val r = p(0.235f, -0.17f)
    val sagY = -0.04f
    val back = Path().apply {
        moveTo(l.x, l.y)
        quadraticTo(sway * 0.1f * u, (sagY - 0.03f) * u, r.x, r.y)
        lineTo(r.x, r.y + 0.016f * u)
        quadraticTo(sway * 0.1f * u, (sagY - 0.01f) * u, l.x, l.y + 0.016f * u)
        close()
    }
    inked(back, Color(0xFF3B6EA5), pen)
    for (k in 1..9) {
        val tt = k / 10f
        val x = mix(l.x, r.x, tt)
        val y = (1f - tt) * (1f - tt) * l.y + 2f * (1f - tt) * tt * (sagY - 0.03f) * u + tt * tt * r.y
        drawLine(Color.White.copy(alpha = 0.55f), Offset(x, y + 0.002f * u), Offset(x, y + 0.014f * u + 0.012f * u * sin(tt * PI.toFloat())), pen.lw * 0.9f)
    }
    // The cloth's inside: pale, with a stitched seam, seen over the far edge.
    val inside = Path().apply {
        moveTo(l.x + 0.01f * u, l.y + 0.012f * u)
        quadraticTo(sway * 0.1f * u, (sagY + 0.01f) * u, r.x - 0.01f * u, r.y + 0.012f * u)
        quadraticTo(sway * 0.1f * u, (sagY + 0.07f) * u, l.x + 0.01f * u, l.y + 0.012f * u)
        close()
    }
    drawPath(inside, Color(0xFFF2EEE6))
    drawPath(inside, Ink.line, style = pen.thin)
    // A pillow at the head end, and a knitted blanket at the feet.
    val pil = Rect(-0.2f * u, (sagY + 0.02f - 0.028f) * u, -0.14f * u, (sagY + 0.045f) * u)
    inkedRound(pil, 0.012f * u, Color(0xFFFFE9A8), pen)
    drawLine(Color(0xFFD9A93E), Offset(pil.left + 0.006f * u, pil.center.y), Offset(pil.right - 0.006f * u, pil.center.y), pen.lw * 0.8f)
    val blanket = Rect(0.1f * u, (sagY + 0.012f) * u, 0.19f * u, (sagY + 0.055f) * u)
    inkedRound(blanket, 0.006f * u, Color(0xFFD9774F), pen)
    fxKnit(blanket, Color(0xFF8E2F3E), 5, 3, pen.lw * 0.45f)
    // Ropes up to the rings.
    for (s in -1..1 step 2) {
        val a = p(s * 0.235f, -0.17f)
        for (k in 0..3) drawLine(Ink.line, Offset(a.x, a.y), Offset(a.x - s * (0.02f + 0.012f * k) * u, a.y + 0.026f * u), pen.lw * 0.9f)
    }
}

/** The near edge of the hammock, in front of whoever lies in it. */
internal fun DrawScope.grHammockFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val sway = f.angle
    val l = p(-0.235f, -0.17f)
    val r = p(0.235f, -0.17f)
    val sagY = -0.03f
    val near = Path().apply {
        moveTo(l.x, l.y)
        quadraticTo(sway * 0.1f * u, (sagY + 0.04f) * u, r.x, r.y)
        lineTo(r.x, r.y + 0.028f * u)
        quadraticTo(sway * 0.1f * u, (sagY + 0.095f) * u, l.x, l.y + 0.028f * u)
        close()
    }
    inked(near, Color(0xFF3B6EA5), pen)
    val band = Path().apply {
        moveTo(l.x, l.y + 0.012f * u)
        quadraticTo(sway * 0.1f * u, (sagY + 0.058f) * u, r.x, r.y + 0.012f * u)
    }
    drawPath(band, Color.White.copy(alpha = 0.75f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
    drawPath(Path().apply {
        moveTo(l.x, l.y + 0.022f * u)
        quadraticTo(sway * 0.1f * u, (sagY + 0.082f) * u, r.x, r.y + 0.022f * u)
    }, Color(0xFFFFC83D), style = Stroke(0.004f * u, cap = StrokeCap.Round))
    // Tassels at the ends.
    for (s in -1..1 step 2) {
        val e = Offset(if (s < 0) l.x else r.x, l.y + 0.03f * u)
        for (k in -1..1) drawLine(Color(0xFFFFC83D), Offset(e.x + k * 0.004f * u, e.y), Offset(e.x + k * 0.006f * u, e.y + 0.02f * u), pen.lw * 1.2f, StrokeCap.Round)
    }
}
