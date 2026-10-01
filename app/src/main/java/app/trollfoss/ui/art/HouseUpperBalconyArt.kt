package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The balcony: a white railing with a flower box, a hanging bird feeder where robins, blue tits, goldfinches and sparrows
 * come to eat, and a rattan egg chair that swings on its chain. (The long slide to the garden is in HouseUpperPassageArt.kt.)
 */

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

// ---------------------------------------------------------------------------------------------- the railing

/** A white railing along the back of the balcony with a long flower box in front of it. */
internal fun DrawScope.upRailing(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = 0f
    val d = 0.05f
    val white = Color(0xFFF7F3EC)
    // Posts at the ends and between, a bottom rail, balusters, and the top rail.
    for (x in listOf(-0.49f, -0.25f, 0.0f, 0.25f, 0.49f)) fxBox(u, x - 0.012f, -0.146f, x + 0.012f, 0f, 0.024f, white, pen, rad = 0.004f, z = 0.01f)
    fxBox(u, -0.5f, -0.04f, 0.5f, -0.026f, 0.02f, white, pen, rad = 0.003f, z = 0.015f)
    var x = -0.46f
    while (x < 0.47f) {
        if (abs(x) % 0.25f > 0.03f && abs(abs(x) - 0.25f) > 0.03f && abs(x) > 0.03f) fxPost(u, x, 0.025f, -0.026f, -0.128f, 0.0045f, white, pen)
        x += 0.04f
    }
    fxBox(u, -0.51f, -0.15f, 0.51f, -0.128f, 0.06f, white, pen, rad = 0.005f, top = Color(0xFFFFFFFF), side = Color(0xFFD9D4CB))
    // A string of small flags hangs along the front of the top rail.
    val flags = listOf(UpC.red, UpC.yellow, UpC.sky, UpC.green, UpC.pink)
    for (k in 0 until 14) {
        val fx = -0.46f + k * 0.07f
        val sway = sin(t * 2.4f + k) * 0.003f
        drawPath(Path().apply { poly((fx - 0.012f) * u, -0.128f * u, (fx + 0.012f) * u, -0.128f * u, (fx + sway) * u, -0.1f * u) }, flags[k % flags.size])
        drawPath(Path().apply { poly((fx - 0.012f) * u, -0.128f * u, (fx + 0.012f) * u, -0.128f * u, (fx + sway) * u, -0.1f * u) }, Ink.line, style = pen.thin)
    }
    // The long flower box.
    fxBox(u, -0.16f, -0.075f, 0.14f, -0.02f, 0.07f, Color(0xFFC98A55), pen, rad = 0.005f, z = 0.06f, top = Color(0xFF6B4A33))
    for (k in 0 until 6) fxLine(q(-0.14f + k * 0.05f, -0.07f, 0.06f), q(-0.14f + k * 0.05f, -0.025f, 0.06f), Color(0xFFA0663B), pen.lw * 0.6f)
    val cols = listOf(Color(0xFFFF6B8A), Color(0xFFFFD447), Color(0xFFF7F3EC), Color(0xFFFF9F43), Color(0xFFB983FF))
    for (k in 0 until 9) {
        val fx = -0.145f + k * 0.034f
        val sway = sin(t * 1.5f + k * 0.8f) * 0.003f
        val stem = q(fx, -0.075f, 0.09f)
        val head = Offset(stem.x + sway * u, stem.y - (0.04f + 0.012f * (k % 3)) * u)
        drawLine(UpC.leaf.darken(0.2f), stem, head, strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
        drawPath(fxLeaf(stem.x, stem.y - 0.012f * u, stem.x + 0.016f * u, stem.y - 0.03f * u, 0.4f), UpC.leaf)
        inkedCircle(head, 0.011f * u, cols[k % cols.size], pen, shade = false)
        drawCircle(UpC.yellow, 0.004f * u, head)
    }
    // Trailing green over the front edge of the box.
    for (k in 0 until 5) {
        val sx = -0.13f + k * 0.065f
        val a = q(sx, -0.03f, 0.06f)
        drawLine(UpC.leaf, a, Offset(a.x + sin(t + k) * 0.003f * u, a.y + 0.03f * u), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
    }
}

// ---------------------------------------------------------------------------------------------- birds and the feeder

/** A garden bird: 0 robin, 1 blue tit, 2 goldfinch, 3 sparrow. It faces [dir] (1 right, -1 left); [flap] spreads the wings; [peck] dips the head. */
private fun DrawScope.bird(c: Offset, s: Float, dir: Float, species: Int, flap: Float, peck: Float, pen: Pen) {
    val body = listOf(Color(0xFF8A6A4A), Color(0xFF7FB2E0), Color(0xFFC9A66B), Color(0xFFA6805A))[species]
    val belly = listOf(Color(0xFFF2763A), Color(0xFFFFE066), Color(0xFFF7F0DC), Color(0xFFE6D8BC))[species]
    val wing = body.darken(0.22f)
    scale(dir, 1f, pivot = c) {
        // The tail, the body, the belly.
        val tail = Path().apply { poly(c.x - s * 0.8f, c.y - s * 0.1f, c.x - s * 1.5f, c.y - s * 0.15f + flap * s * 0.3f, c.x - s * 1.4f, c.y + s * 0.2f, c.x - s * 0.7f, c.y + s * 0.25f) }
        inked(tail, wing, pen, shade = false)
        val b = Path().apply { addOval(Rect(c.x - s * 1.0f, c.y - s * 0.6f, c.x + s * 0.9f, c.y + s * 0.55f)) }
        inked(b, body, pen)
        drawOval(belly, Offset(c.x - s * 0.5f, c.y - s * 0.05f), Size(s * 1.3f, s * 0.6f))
        drawOval(Ink.line, Offset(c.x - s * 0.5f, c.y - s * 0.05f), Size(s * 1.3f, s * 0.6f), style = pen.thin)
        // A wing: folded, or flapping up.
        val wp = Path().apply {
            moveTo(c.x - s * 0.5f, c.y - s * 0.2f)
            quadraticTo(c.x - s * 0.1f, c.y - s * (0.7f + flap * 1.2f), c.x + s * 0.4f, c.y - s * (0.1f + flap * 0.6f))
            quadraticTo(c.x, c.y + s * 0.1f, c.x - s * 0.5f, c.y - s * 0.2f); close()
        }
        inked(wp, wing, pen, shade = false)
        // The head with eye and beak, dipping to peck.
        val hc = Offset(c.x + s * 0.75f, c.y - s * (0.55f - peck * 0.55f))
        inkedCircle(hc, s * 0.5f, if (species == 1) Color(0xFFF7F3EC) else body, pen)
        if (species == 0) drawOval(belly, Offset(hc.x - s * 0.1f, hc.y - s * 0.05f), Size(s * 0.55f, s * 0.45f))
        if (species == 2) drawCircle(Color(0xFFE23A4A), s * 0.2f, Offset(hc.x + s * 0.2f, hc.y + s * 0.02f))
        if (species == 1) drawArc(Color(0xFF3D7FD6), 180f, 180f, true, Offset(hc.x - s * 0.5f, hc.y - s * 0.5f), Size(s, s * 0.7f))
        drawPath(Path().apply { poly(hc.x + s * 0.4f, hc.y - s * 0.08f, hc.x + s * 0.8f, hc.y + s * 0.02f, hc.x + s * 0.4f, hc.y + s * 0.12f) }, UpC.orange)
        drawCircle(Ink.line, s * 0.09f, Offset(hc.x + s * 0.18f, hc.y - s * 0.12f))
        drawCircle(Color.White, s * 0.03f, Offset(hc.x + s * 0.2f, hc.y - s * 0.15f))
        // Two thin legs.
        drawLine(Ink.line, Offset(c.x - s * 0.1f, c.y + s * 0.5f), Offset(c.x - s * 0.1f, c.y + s * 0.85f), strokeWidth = pen.lw * 0.8f)
        drawLine(Ink.line, Offset(c.x + s * 0.2f, c.y + s * 0.5f), Offset(c.x + s * 0.2f, c.y + s * 0.85f), strokeWidth = pen.lw * 0.8f)
    }
}

/** A hanging bird feeder: a little house with a red roof and a seed tray; birds fly in, peck and fly off when it is full. */
internal fun DrawScope.upBirdFeeder(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val sway = 0f
    val visit = f.angle
    val empty = f.count >= 6
    translate(sway * u * 0.5f, 0f) {
        // Chains up to the beam.
        for (x in listOf(-0.055f, 0.055f)) {
            drawLine(Ink.line, p(x, -0.27f), p(0.0f, -0.55f + 0.0f), strokeWidth = pen.lw * 0.9f)
        }
        drawCircle(UpC.brass, 0.008f * u, p(0f, -0.56f))
        // The roof.
        val roof = Path().apply { poly(-0.09f * u, -0.2f * u, 0f, -0.285f * u, 0.09f * u, -0.2f * u) }
        inked(roof, UpC.falun, pen)
        drawLine(UpC.cream, p(-0.085f, -0.205f), p(0.085f, -0.205f), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
        drawPath(starPath(p(0f, -0.235f), 0.012f * u, 0.005f * u), UpC.yellow)
        // Posts, the seed hopper behind, and the tray with seeds.
        for (x in listOf(-0.07f, 0.07f)) capsule(p(x, -0.2f), p(x, -0.03f), 0.007f * u, UpC.wood, pen)
        val hop = Rect(-0.04f * u, -0.2f * u, 0.04f * u, -0.07f * u)
        drawRect(Color(0xCCCFEFFF), hop.topLeft, hop.size)
        drawRect(Ink.line, hop.topLeft, hop.size, style = pen.thin)
        if (!empty) drawRect(Color(0xFFE8C85A), Offset(hop.left + 0.003f * u, hop.top + 0.04f * u), Size(hop.width - 0.006f * u, hop.height - 0.04f * u - 0.003f * u))
        fxBox(u, -0.09f, -0.04f, 0.09f, -0.02f, 0.05f, UpC.oak, pen, rad = 0.004f)
        if (!empty) for (k in 0 until 9) drawCircle(if (k % 3 == 0) Color(0xFFC9A04A) else Color(0xFFE8C85A), 0.0035f * u, p(-0.075f + k * 0.019f, -0.046f - (k % 2) * 0.004f))
        // A perch along the front.
        capsule(p(-0.1f, -0.03f), p(0.1f, -0.03f), 0.005f * u, UpC.walnut, pen)
    }
    // The visitor.
    if (visit > 0f) {
        val sp = f.mode.coerceIn(0, 3)
        val perch = p(0.045f + sway * 0.5f, -0.05f)
        val inFrom = p(0.42f, -0.48f)
        val outTo = p(-0.42f, -0.42f)
        when {
            visit < 0.14f -> {
                val k = visit / 0.14f
                val e = 1f - (1f - k) * (1f - k)
                val c = Offset(lerpF(inFrom.x, perch.x, e), lerpF(inFrom.y, perch.y, e) + sin(k * 3.14f) * 0.03f * u)
                bird(c, 0.016f * u, -1f, sp, 0.5f + 0.5f * sin(t * 40f), 0f, pen)
            }
            visit < 0.84f -> {
                val peck = max(0f, sin(t * 7f + visit * 20f))
                bird(Offset(perch.x, perch.y - 0.0f), 0.016f * u, -1f, sp, 0f, peck, pen)
            }
            else -> {
                val k = (visit - 0.84f) / 0.16f
                val e = k * k
                val c = Offset(lerpF(perch.x, outTo.x, e), lerpF(perch.y, outTo.y, e) - sin(k * 3.14f) * 0.02f * u)
                bird(c, 0.016f * u, -1f, sp, 0.5f + 0.5f * sin(t * 40f), 0f, pen)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------- the hanging chair

/** A rattan egg chair with a mustard cushion on a chain from the beam. It swings. */
internal fun DrawScope.upHangingChair(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = 0f
    upShadow(u, 0.22f, 0.1f, 0.5f)
    // The seat swings about the beam; its swing is a little smaller than the seat's own.
    val pivot = p(0f, -0.79f)
    rotate(f.angle * 57.3f * 0.48f, pivot) {
        // The chain, a hook plate at the beam.
        for (k in 0 until 14) {
            val y = -0.78f + k * 0.0315f
            drawOval(Ink.line, Offset(-0.005f * u, y * u), Size(0.01f * u, 0.016f * u), style = Stroke(pen.lw * 1.1f))
        }
        drawOval(UpC.brassDark, Offset(-0.022f * u, -0.805f * u), Size(0.044f * u, 0.016f * u))
        drawOval(Ink.line, Offset(-0.022f * u, -0.805f * u), Size(0.044f * u, 0.016f * u), style = pen.thin)
        // The egg: a big woven shell open at the front.
        val shell = Path().apply {
            moveTo(0f, -0.34f * u)
            cubicTo(0.12f * u, -0.34f * u, 0.14f * u, -0.17f * u, 0.07f * u, -0.04f * u)
            quadraticTo(0f, -0.012f * u, -0.07f * u, -0.04f * u)
            cubicTo(-0.14f * u, -0.17f * u, -0.12f * u, -0.34f * u, 0f, -0.34f * u)
            close()
        }
        drawPath(shell, Color(0xFFB98B5A))
        clipPath(shell) {
            // The weave: criss-crossing lines.
            for (k in -8..8) {
                drawLine(Color(0xFF8A6038).copy(alpha = 0.7f), p(k * 0.02f - 0.1f, -0.34f), p(k * 0.02f + 0.1f, -0.02f), strokeWidth = pen.lw * 0.8f)
                drawLine(Color(0xFFD9AE7C).copy(alpha = 0.8f), p(k * 0.02f + 0.1f, -0.34f), p(k * 0.02f - 0.1f, -0.02f), strokeWidth = pen.lw * 0.8f)
            }
            // The opening where you sit: a darker oval hollow.
            drawOval(Color(0xFF6B4A33), Offset(-0.085f * u, -0.25f * u), Size(0.17f * u, 0.22f * u))
            drawOval(Color(0xFF8A6038), Offset(-0.075f * u, -0.24f * u), Size(0.15f * u, 0.2f * u))
        }
        drawPath(shell, Ink.line, style = pen.stroke)
        // The cushion, and a small pillow with a heart.
        inkedRound(Rect(-0.075f * u, -0.17f * u, 0.075f * u, -0.115f * u), 0.014f * u, Color(0xFFE8B04A), pen)
        for (k in 0 until 4) drawLine(Color(0xFFC98E2E), p(-0.05f + k * 0.033f, -0.165f), p(-0.05f + k * 0.033f, -0.12f), strokeWidth = pen.lw * 0.7f)
        inkedRound(Rect(0.012f * u, -0.235f * u, 0.07f * u, -0.17f * u), 0.014f * u, Color(0xFFF08CB8), pen)
        drawPath(fxHeart(0.041f * u, -0.2f * u, 0.011f * u), Color.White)
        // A tassel on the front edge.
        val ts = p(-0.07f, -0.05f)
        drawLine(Ink.line, ts, Offset(ts.x, ts.y + 0.03f * u + sin(t * 2f) * 0.002f * u), strokeWidth = pen.lw * 0.8f)
        inkedCircle(Offset(ts.x, ts.y + 0.036f * u), 0.007f * u, Color(0xFFF08CB8), pen, shade = false)
    }
}
