package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Thing
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The kitchen of Storstova in oblique 3D: the range with its hood, the sink, the mixer that sprays, the brick
// pizza oven, the big fridge, the jam cupboard, the island with its stools, and the dumbwaiter hatch.

private val MINT = Color(0xFF8FD9C0)
private val MINT_DARK = Color(0xFF5DB892)
private val ENAMEL = Color(0xFFFBF6EA)

// ------------------------------------------------------------------------------------------------ the range

internal fun DrawScope.grRange(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.16f
    val baking = f.mode == 1
    grShadow(u, 0.34f, d)
    // The hood and its flue go up to the ceiling above.
    fxBox(u, -0.04f, -f.y, 0.04f, -0.47f, 0.05f, GrC.silver, pen, rad = 0.004f, z = 0.07f)
    val hood = Path().apply {
        moveTo(-0.15f * u, -0.42f * u)
        lineTo(0.15f * u, -0.42f * u)
        lineTo(0.09f * u, -0.5f * u)
        lineTo(-0.09f * u, -0.5f * u)
        close()
    }
    val h3 = q(0f, 0f, 0.05f)
    translate(h3.x, h3.y) {
        inked(hood, GrC.silver, pen)
        drawLine(Color.White.copy(alpha = 0.7f), p(-0.13f, -0.435f), p(-0.07f, -0.495f), pen.lw)
        drawLine(Ink.line.copy(alpha = 0.4f), p(-0.15f, -0.43f), p(0.15f, -0.43f), pen.lw * 0.6f)
        drawCircle(if (f.on) GrC.warmLight else Color(0xFF8E9AB2), 0.006f * u, p(-0.05f, -0.425f))
        drawCircle(if (f.on) GrC.warmLight else Color(0xFF8E9AB2), 0.006f * u, p(0.05f, -0.425f))
    }
    if (f.on) grGlow(p(0f, -0.36f), 0.14f * u, pen, 0.2f)
    // The cream enamel body and the black top plate.
    fxBox(u, -0.17f, -0.3f, 0.17f, 0f, d, ENAMEL, pen, rad = 0.006f, top = Color(0xFF3A3844), side = ENAMEL.darken(0.15f))
    // The back panel with knobs and a little clock.
    fxBox(u, -0.17f, -0.36f, 0.17f, -0.3f, 0.025f, ENAMEL.darken(0.05f), pen, rad = 0.004f, z = d - 0.03f)
    for (k in 0 until 4) {
        val c = q(-0.12f + k * 0.052f, -0.33f, d - 0.03f)
        inkedCircle(c, 0.009f * u, Color(0xFF3A3844), pen, shade = false)
        drawLine(Color.White, c, Offset(c.x + 0.005f * u * cos(k * 1.1f + (if (f.on) 1f else 0f)), c.y - 0.005f * u * sin(k * 1.1f + (if (f.on) 1f else 0f))), pen.lw * 0.9f, StrokeCap.Round)
    }
    val clock = q(0.135f, -0.33f, d - 0.03f)
    inkedCircle(clock, 0.012f * u, GrC.ivory, pen, shade = false)
    drawLine(Ink.line, clock, Offset(clock.x + sin(pen.t * 0.2f) * 0.008f * u, clock.y - cos(pen.t * 0.2f) * 0.008f * u), pen.lw * 0.9f, StrokeCap.Round)
    // Four hobs with grates; blue flames when the hobs are lit.
    for (row in 0..1) for (col in 0..1) {
        val c = q(-0.075f + col * 0.15f, -0.3f, 0.045f + row * 0.065f)
        val ring = fxDisc2(c.x, c.y, 0.032f * u, 0.026f * u)
        drawPath(ring, Color(0xFF1B1B26))
        drawPath(ring, Ink.line, style = pen.thin)
        drawPath(fxDisc2(c.x, c.y, 0.018f * u, 0.014f * u), Color(0xFF55556A))
        if (f.on) {
            for (k in 0 until 6) {
                val a = k * PI.toFloat() / 3f + pen.t
                val fx = c.x + cos(a) * 0.016f * u
                val fy = c.y + sin(a) * 0.0105f * u
                drawCircle(Color(0xFF4FB3F0), 0.0035f * u * (1f + 0.3f * sin(pen.t * 20f + k)), Offset(fx, fy))
            }
        }
        for (g in -1..1) drawLine(GrC.iron, Offset(c.x + g * 0.026f * u, c.y - 0.021f * u), Offset(c.x - g * 0.026f * u, c.y + 0.021f * u), pen.lw)
    }
    // A saucepan with steam on one hob.
    run {
        val c = q(0.075f, -0.3f, 0.045f)
        val pot = Rect(c.x - 0.022f * u, c.y - 0.036f * u, c.x + 0.022f * u, c.y)
        inkedRound(pot, 0.004f * u, GrC.silver, pen)
        drawLine(GrC.iron, Offset(pot.right, pot.top + 0.01f * u), Offset(pot.right + 0.026f * u, pot.top + 0.006f * u), 0.006f * u, StrokeCap.Round)
        inkedOval(Rect(pot.left - 0.002f * u, pot.top - 0.006f * u, pot.right + 0.002f * u, pot.top + 0.006f * u), GrC.silver, pen, shade = false)
        if (f.on) fxPuffs(c.x, c.y - 0.042f * u, pen.t, 0.012f * u, 0.07f * u, Color.White, 0.55f, 3, 0.4f)
    }
    // The oven door: open (hinged down) with the rack and a glow, or shut with a window.
    val l = -0.12f
    val r = 0.12f
    val t = -0.14f
    val b = -0.025f
    if (f.open) {
        fxHollow(u, l, t, r, b, 0.12f, Color(0xFF8E887F), pen, back = Color(0xFF3A3844))
        fxInShelf(u, l, r, -0.075f, 0.12f, GrC.iron, pen)
        if (baking) grGlow(p(0f, -0.08f), 0.1f * u, pen, 0.6f, Color(0xFFFF9A3D))
        val door = Path().apply {
            moveTo(l * u, b * u)
            lineTo(r * u, b * u)
            lineTo((r + 0.012f) * u, (b + 0.07f) * u)
            lineTo((l - 0.012f) * u, (b + 0.07f) * u)
            close()
        }
        inked(door, ENAMEL, pen)
        drawLine(GrC.silver, p(l - 0.006f, b + 0.06f), p(r + 0.006f, b + 0.06f), pen.lw * 2.4f, StrokeCap.Round)
    } else {
        val doorR = Rect(l * u, t * u, r * u, b * u)
        inkedRound(doorR, 0.004f * u, ENAMEL, pen, shade = false)
        val win = Rect(doorR.left + 0.02f * u, doorR.top + 0.025f * u, doorR.right - 0.02f * u, doorR.bottom - 0.02f * u)
        inkedRound(win, 0.004f * u, if (baking) Color(0xFFFF9A3D) else Color(0xFF2B2B3C), pen, shade = false)
        if (baking) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFFFC96B), Color(0xFFFF7A3D)), win.top, win.bottom), win.topLeft, win.size)
            grGlow(win.center, 0.09f * u, pen, 0.5f, Color(0xFFFF9A3D))
            // Something rises in there.
            val rise = sin(pen.t * 3f) * 0.004f * u
            drawOval(Color(0xFFE8B04A), Offset(win.center.x - 0.03f * u, win.bottom - 0.04f * u - rise), Size(0.06f * u, 0.032f * u))
        } else {
            drawLine(Color.White.copy(alpha = 0.35f), Offset(win.left + 0.008f * u, win.bottom - 0.004f * u), Offset(win.left + 0.04f * u, win.top + 0.006f * u), pen.lw)
        }
        drawLine(Ink.line, Offset(doorR.left + 0.012f * u, doorR.top + 0.014f * u), Offset(doorR.right - 0.012f * u, doorR.top + 0.014f * u), 0.007f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(GrC.silver, Offset(doorR.left + 0.012f * u, doorR.top + 0.014f * u), Offset(doorR.right - 0.012f * u, doorR.top + 0.014f * u), 0.007f * u, StrokeCap.Round)
    }
    // A drawer under the oven, and little feet.
    inkedRound(Rect(-0.15f * u, -0.022f * u, 0.15f * u, -0.003f * u), 0.003f * u, ENAMEL.darken(0.04f), pen, shade = false)
    grKnob(p(0f, -0.0125f), 0.004f * u, pen)
}

// ------------------------------------------------------------------------------------------------ the sink

internal fun DrawScope.grSink(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.15f
    val on = f.on
    grShadow(u, 0.3f, d)
    // The mint cabinet with two doors and a white stone top with the basin cut into it.
    fxBox(u, -0.15f, -0.2f, 0.15f, 0f, d, MINT, pen, rad = 0.005f, top = ENAMEL, side = MINT_DARK)
    fxBox(u, -0.155f, -0.222f, 0.155f, -0.2f, d + 0.012f, ENAMEL, pen, rad = 0.004f, z = -0.006f, top = Color.White, side = ENAMEL.darken(0.12f))
    for (s in 0..1) {
        val x0 = if (s == 0) -0.14f else 0.004f
        val r = Rect(x0 * u, -0.186f * u, (x0 + 0.136f) * u, -0.012f * u)
        drawRoundRect(MINT_DARK, r.topLeft, r.size, CornerRadius(0.004f * u), style = pen.thin)
        grKnob(Offset(if (s == 0) r.right - 0.012f * u else r.left + 0.012f * u, r.top + 0.02f * u), 0.0055f * u, pen)
    }
    // The basin: a white oval, with the water in it.
    val b = q(-0.07f, -0.2225f, 0.075f)
    val basin = fxDisc2(b.x, b.y, 0.058f * u, 0.052f * u)
    drawPath(basin, Color(0xFFD7E3E6))
    drawPath(fxDisc2(b.x, b.y - 0.002f * u, 0.048f * u, 0.042f * u), Color(0xFF9FB8C0))
    if (on) {
        drawPath(fxDisc2(b.x, b.y - 0.003f * u, 0.046f * u, 0.04f * u), Color(0xFF7FD3E8), alpha = 0.85f)
        for (k in 0 until 3) {
            val ph = fxFrac(pen.t * 1.4f + k * 0.33f)
            drawPath(fxDisc2(b.x - 0.01f * u, b.y - 0.003f * u, 0.01f * u * (1f + ph * 2f), 0.008f * u * (1f + ph * 2f)), Color.White.copy(alpha = 0.7f * (1f - ph)), style = Stroke(pen.lw * 0.7f))
        }
    }
    drawPath(basin, Ink.line, style = pen.stroke)
    // The brass tap with a swan neck, and the water.
    val tap = q(-0.07f, -0.2225f, 0.12f)
    capsule(tap, Offset(tap.x, tap.y - 0.05f * u), 0.008f * u, GrC.brass, pen)
    val neck = Path().apply {
        moveTo(tap.x, tap.y - 0.05f * u)
        quadraticTo(tap.x, tap.y - 0.075f * u, tap.x + 0.016f * u, tap.y - 0.065f * u)
        quadraticTo(tap.x + 0.03f * u, tap.y - 0.055f * u, tap.x + 0.03f * u, tap.y - 0.042f * u)
    }
    drawPath(neck, Ink.line, style = Stroke(0.008f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(neck, GrC.brass, style = Stroke(0.008f * u, cap = StrokeCap.Round))
    grKnob(Offset(tap.x - 0.012f * u, tap.y - 0.042f * u), 0.005f * u, pen)
    if (on) {
        val sx = tap.x + 0.03f * u
        val sy = tap.y - 0.04f * u
        for (k in 0 until 4) {
            val ph = fxFrac(pen.t * 3f + k * 0.25f)
            drawLine(Color(0xFF7FD3E8), Offset(sx, sy + ph * 0.032f * u), Offset(sx, sy + (ph + 0.2f) * 0.032f * u), pen.lw * 1.6f, StrokeCap.Round)
        }
        drawLine(Color(0xFFBFEFFF), Offset(sx, sy), Offset(sx, sy + 0.03f * u), pen.lw * 1.2f, StrokeCap.Round)
    }
    // A bottle of soap, a sponge and a small herb pot on the counter at the right and the back.
    val soap = q(0.12f, -0.2225f, 0.1f)
    inkedRound(Rect(soap.x - 0.006f * u, soap.y - 0.03f * u, soap.x + 0.006f * u, soap.y), 0.003f * u, Color(0xFFF2B8B0), pen, shade = false)
    inkedRound(Rect(soap.x - 0.003f * u, soap.y - 0.036f * u, soap.x + 0.003f * u, soap.y - 0.03f * u), 0.0015f * u, GrC.brass, pen, shade = false)
    inkedRound(Rect(soap.x - 0.034f * u, soap.y - 0.01f * u, soap.x - 0.016f * u, soap.y), 0.003f * u, Color(0xFFFFE066), pen, shade = false)
}

// ------------------------------------------------------------------------------------------------ the mixer

internal fun DrawScope.grMixer(f: Fixture, u: Float, pen: Pen, contents: List<Thing>) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val on = f.on
    val shake = if (on) sin(pen.t * 60f) * 0.0015f * u else 0f
    val body = Color(0xFFD2443A)
    // Splats on the wall behind from every time it has sprayed.
    val splats = f.count.coerceAtMost(9)
    val sc = listOf(Color(0xFFFF8FB1), Color(0xFF7FD3E8), Color(0xFFFFC83D), Color(0xFF8BD450), Color(0xFFB57BFF))
    for (k in 0 until splats) {
        val x = -0.05f + hash01(k, 901) * 0.1f - 0.02f
        val y = -0.22f - hash01(k, 902) * 0.16f
        val r = (0.01f + 0.012f * hash01(k, 903)) * u
        val c = q(x, y, 0.1f)
        drawCircle(sc[k % sc.size].copy(alpha = 0.85f), r, c)
        for (j in 0 until 4) drawCircle(sc[k % sc.size].copy(alpha = 0.85f), r * 0.28f, Offset(c.x + cos(j * 1.6f + k) * r * 1.5f, c.y + sin(j * 1.6f + k) * r * 1.2f))
        drawLine(sc[k % sc.size].copy(alpha = 0.7f), c, Offset(c.x, c.y + 0.03f * u * hash01(k, 904) + r), pen.lw * 1.6f, StrokeCap.Round)
    }
    // The base, the pillar, the tilting head with its beater.
    val base = Rect(-0.05f * u, -0.018f * u, 0.05f * u, 0f)
    inkedRound(base, 0.006f * u, body, pen)
    fxBox(u, -0.04f, -0.12f, 0.04f, -0.016f, 0.05f, body, pen, rad = 0.012f, z = 0.04f)
    translate(shake, 0f) {
        val head = Path().apply {
            moveTo(-0.045f * u, -0.115f * u)
            quadraticTo(-0.06f * u, -0.17f * u, -0.01f * u, -0.175f * u)
            lineTo(0.05f * u, -0.17f * u)
            quadraticTo(0.062f * u, -0.145f * u, 0.05f * u, -0.115f * u)
            close()
        }
        inked(head, body, pen)
        shine(p(-0.025f, -0.15f), 0.008f * u, 0.022f * u, 0.7f)
        drawCircle(GrC.silver, 0.007f * u, p(0.052f, -0.14f))
        // The beater.
        val bx = -0.01f
        val spin = if (on) pen.t * 24f else 0.6f
        capsule(p(bx, -0.116f), p(bx, -0.075f), 0.004f * u, GrC.silver, pen)
        rotate(spin * 3f, p(bx, -0.075f)) {
            drawOval(GrC.silver, Offset((bx - 0.012f) * u, -0.08f * u), Size(0.024f * u, 0.012f * u), style = Stroke(pen.lw * 1.2f))
        }
    }
    // The steel bowl: with batter or fruit in it, and a lip.
    val bowl = Path().apply {
        moveTo(-0.044f * u, -0.04f * u)
        lineTo(0.044f * u, -0.04f * u)
        quadraticTo(0.04f * u, -0.004f * u, 0f, -0.004f * u)
        quadraticTo(-0.04f * u, -0.004f * u, -0.044f * u, -0.04f * u)
        close()
    }
    val mix = if (contents.isEmpty()) Color(0xFFF6EEDC) else lerp(Color(0xFFFFB3C7), Color(0xFF8BD450), (contents.sumOf { it.type.ordinal } % 5) / 5f)
    translate(-0.01f * u, 0f) {
        if (contents.isNotEmpty() || on) {
            val swirl = if (on) sin(pen.t * 20f) * 0.004f * u else 0f
            drawOval(mix, Offset(-0.04f * u, -0.054f * u + swirl), Size(0.08f * u, 0.028f * u))
            for (k in contents.indices) {
                drawCircle(Color(0xFFE8473F).copy(alpha = 0.9f), 0.008f * u, Offset((-0.016f + k * 0.016f) * u, -0.052f * u - swirl))
            }
            if (on) for (k in 0 until 4) {
                val ph = fxFrac(pen.t * 3f + k * 0.25f)
                drawCircle(mix, 0.004f * u, Offset((-0.03f + k * 0.02f) * u, -0.054f * u - ph * 0.05f * u))
            }
        }
        inked(bowl, GrC.silver, pen)
        shine(p(-0.026f, -0.026f), 0.007f * u, 0.016f * u, 0.7f)
        drawLine(GrC.steel, p(-0.046f, -0.04f), p(0.046f, -0.04f), pen.lw * 1.6f, StrokeCap.Round)
    }
}

// ------------------------------------------------------------------------------------------------ the pizza oven

internal fun DrawScope.grPizzaOven(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.2f
    val on = f.on
    val baking = f.mode == 1
    val brick = Color(0xFFC8553D)
    val brickDark = Color(0xFF8E3A2E)
    grShadow(u, 0.4f, d)
    if (on) grGlow(p(0f, -0.11f), 0.34f * u, pen, 0.35f, Color(0xFFFF9A3D))
    // The stone plinth with a wood store in an arch under the oven.
    fxBox(u, -0.19f, -0.14f, 0.19f, 0f, d, GrC.stone, pen, rad = 0.006f, top = GrC.stone.lighten(0.12f))
    val store = archPath(-0.13f * u, 0.13f * u, -0.012f * u, -0.075f * u, -0.126f * u)
    drawPath(store, Color(0xFF2E1D18))
    drawPath(store, Ink.line, style = pen.stroke)
    clipPath(store) {
        for (row in 0..2) for (col in 0..5) {
            val x = -0.115f + col * 0.04f + (row % 2) * 0.02f
            if (x > 0.115f) continue
            val c = p(x, -0.03f - row * 0.032f)
            inkedCircle(c, 0.016f * u, if ((row + col) % 2 == 0) Color(0xFFB9824C) else Color(0xFFA0663B), pen, shade = false)
            drawCircle(Color(0xFFE3B27A), 0.007f * u, c, style = pen.thin)
        }
    }
    // Courses of stone along the plinth.
    for (k in 1..2) drawLine(GrC.stoneDark, p(-0.19f, -k * 0.045f), p(-0.135f, -k * 0.045f), pen.lw * 0.6f)
    for (k in 1..2) drawLine(GrC.stoneDark, p(0.135f, -k * 0.045f), p(0.19f, -k * 0.045f), pen.lw * 0.6f)
    // The brick dome.
    val domeBase = -0.14f
    val dome = Path().apply {
        moveTo(-0.18f * u, domeBase * u)
        cubicTo(-0.18f * u, -0.3f * u, -0.1f * u, -0.335f * u, 0f, -0.335f * u)
        cubicTo(0.1f * u, -0.335f * u, 0.18f * u, -0.3f * u, 0.18f * u, domeBase * u)
        close()
    }
    inked(dome, brick, pen)
    clipPath(dome) {
        for (row in 0 until 9) {
            val y = domeBase - 0.022f - row * 0.022f
            drawLine(brickDark, p(-0.2f, y), p(0.2f, y), pen.lw * 0.7f)
            var x = -0.2f + (row % 2) * 0.02f
            while (x < 0.2f) {
                drawLine(brickDark, p(x, y), p(x, y - 0.022f), pen.lw * 0.7f)
                x += 0.04f
            }
        }
        drawRect(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent, Ink.line.copy(alpha = 0.18f)), -0.18f * u, 0.18f * u), p(-0.18f, -0.34f), Size(0.36f * u, 0.2f * u))
    }
    // The mouth of the oven: black arch with fire and, when baking, a pizza.
    val mouth = archPath(-0.085f * u, 0.085f * u, domeBase * u + 0.0f, -0.205f * u, -0.255f * u)
    drawPath(mouth, Color(0xFF1B1226))
    clipPath(mouth) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF2A1B22), if (on) Color(0xFF8E3A2E) else Color(0xFF3A2A2A)), -0.26f * u, domeBase * u), p(-0.09f, -0.26f), Size(0.18f * u, 0.12f * u))
        if (on) {
            fxFire(-0.04f * u, -0.145f * u, 0.06f * u, 0.1f * u, pen.t, 0.4f, pen, false)
            fxFire(0.03f * u, -0.145f * u, 0.07f * u, 0.12f * u, pen.t, 1.7f, pen, false)
            fxFire(0f, -0.145f * u, 0.08f * u, 0.14f * u, pen.t, 2.9f, pen, false)
            for (k in 0 until 4) {
                val ph = fxFrac(pen.t * 0.9f + k * 0.25f)
                drawCircle(Color(0xFFFFC96B).copy(alpha = 1f - ph), 0.0025f * u, p(-0.05f + k * 0.034f + sin(ph * 6f + k) * 0.01f, -0.16f - ph * 0.08f))
            }
        }
        if (baking) {
            val pz = Offset(0f, -0.152f * u)
            drawOval(Color(0xFFE8B04A), Offset(pz.x - 0.04f * u, pz.y - 0.01f * u), Size(0.08f * u, 0.022f * u))
            drawOval(Color(0xFFD2443A), Offset(pz.x - 0.034f * u, pz.y - 0.007f * u), Size(0.068f * u, 0.016f * u))
            for (k in 0 until 5) drawCircle(Color(0xFFFFF1B0), 0.003f * u, Offset(pz.x + (-0.025f + k * 0.0125f) * u, pz.y + sin(k * 2f) * 0.0025f * u))
        }
    }
    drawPath(mouth, Ink.line, style = pen.stroke)
    drawPath(mouth, brickDark, style = Stroke(0.01f * u))
    // The chimney: an iron flue with a cowl, going up and out.
    val flue = q(0.07f, -0.3f, 0.11f)
    fxBox(u, 0.055f, -0.69f, 0.085f, -0.31f, 0.03f, GrC.iron, pen, rad = 0.004f, z = 0.1f)
    inkedRound(Rect(flue.x - 0.026f * u, flue.y - 0.4f * u, flue.x + 0.026f * u, flue.y - 0.385f * u), 0.003f * u, GrC.iron.lighten(0.15f), pen)
    if (on) fxPuffs(flue.x, flue.y - 0.405f * u, pen.t, 0.01f * u, 0.03f * u, Color(0xFFD8DCE8), 0.5f, 3, 0.3f)
    // A wooden peel leaning at the side, and a little thermometer dial over the mouth.
    val peel = Path().apply {
        moveTo(0.21f * u, 0f); lineTo(0.2f * u, -0.18f * u); lineTo(0.228f * u, -0.18f * u); lineTo(0.222f * u, 0f); close()
    }
    inked(peel, GrC.oak, pen)
    inkedOval(Rect(0.19f * u, -0.26f * u, 0.238f * u, -0.17f * u), GrC.oak.lighten(0.1f), pen)
    val dial = p(0f, -0.285f)
    inkedCircle(dial, 0.014f * u, GrC.ivory, pen, shade = false)
    val needle = -PI.toFloat() * 0.8f + (if (on) 1.8f else 0.2f)
    drawLine(Ink.line, dial, Offset(dial.x + cos(needle) * 0.011f * u, dial.y + sin(needle) * 0.011f * u), pen.lw * 1.1f, StrokeCap.Round)
    if (baking) twinkle(p(0.12f, -0.2f), 0.02f * u, Color(0xFFFFE680), 0.8f)
}

// ------------------------------------------------------------------------------------------------ the fridge

internal fun DrawScope.grFridge(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.15f
    val shell = Color(0xFF8FD9C0)
    grShadow(u, 0.26f, d)
    fxBox(u, -0.125f, -0.455f, 0.125f, 0f, d, shell, pen, rad = 0.02f, top = shell.lighten(0.25f), side = MINT_DARK)
    // Chrome trim at the top and the foot, a little brand badge.
    drawLine(GrC.silver, p(-0.11f, -0.44f), p(0.11f, -0.44f), pen.lw * 1.6f, StrokeCap.Round)
    if (f.open) {
        fxHollow(u, -0.1f, -0.42f, 0.1f, -0.03f, 0.12f, Color(0xFFF2F6F8), pen, back = Color(0xFFDCE6EA))
        // The shelves and a cold light.
        for (y in floatArrayOf(-0.15f, -0.27f, -0.39f)) fxInShelf(u, -0.1f, 0.1f, y, 0.12f, Color(0xFFBFE6F0), pen, 0.006f)
        grGlow(p(0f, -0.25f), 0.14f * u, pen, 0.35f, Color(0xFFE6F8FF))
        fxOpenDoor(u, FixtureDoors.grandFridge, shell, shell.lighten(0.15f), pen)
    } else {
        // The door with a chrome handle down the side, magnets and a child's drawing.
        val door = Rect(-0.105f * u, -0.425f * u, 0.105f * u, -0.03f * u)
        inkedRound(door, 0.012f * u, shell.lighten(0.05f), pen, shade = false)
        drawLine(Color.White.copy(alpha = 0.6f), Offset(door.left + 0.01f * u, door.top + 0.02f * u), Offset(door.left + 0.01f * u, door.top + 0.12f * u), pen.lw * 1.4f, StrokeCap.Round)
        drawLine(Ink.line, p(0.085f, -0.38f), p(0.085f, -0.1f), 0.01f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(GrC.silver, p(0.085f, -0.38f), p(0.085f, -0.1f), 0.01f * u, StrokeCap.Round)
        drawLine(Color.White, p(0.083f, -0.36f), p(0.083f, -0.2f), pen.lw * 0.8f)
        // The drawing under a magnet: a sun over a house.
        val paper = Rect(-0.07f * u, -0.35f * u, -0.005f * u, -0.27f * u)
        drawRect(Color.White, paper.topLeft, paper.size)
        drawRect(Ink.line, paper.topLeft, paper.size, style = pen.thin)
        drawCircle(Color(0xFFFFC83D), 0.012f * u, Offset(paper.left + 0.014f * u, paper.top + 0.016f * u))
        drawRect(Color(0xFFD2443A), Offset(paper.left + 0.026f * u, paper.bottom - 0.026f * u), Size(0.024f * u, 0.018f * u))
        drawPath(Path().apply { moveTo(paper.left + 0.022f * u, paper.bottom - 0.026f * u); lineTo(paper.left + 0.038f * u, paper.bottom - 0.04f * u); lineTo(paper.left + 0.054f * u, paper.bottom - 0.026f * u); close() }, Color(0xFF2F6FB8))
        grKnob(Offset(paper.center.x, paper.top + 0.004f * u), 0.006f * u, pen)
        // Fruit magnets.
        inkedCircle(p(-0.07f, -0.2f), 0.009f * u, Color(0xFFE8473F), pen, shade = false)
        inkedCircle(p(-0.045f, -0.18f), 0.008f * u, Color(0xFFFFC83D), pen, shade = false)
        inkedCircle(p(-0.02f, -0.21f), 0.007f * u, Color(0xFF3BC46B), pen, shade = false)
        val star = starPath(p(0.03f, -0.19f), 0.011f * u, 0.005f * u)
        drawPath(star, Color(0xFFFF8FB1))
        drawPath(star, Ink.line, style = pen.thin)
        // The freezer drawer below.
        drawLine(GrC.silver, p(-0.105f, -0.092f), p(0.105f, -0.092f), pen.lw * 1.2f)
        // A badge.
        inkedCircle(p(0f, -0.405f), 0.007f * u, GrC.silver, pen, shade = false)
    }
    // Rounded feet.
    for (s in -1..1 step 2) inkedRound(Rect((s * 0.1f - 0.012f) * u, -0.006f * u, (s * 0.1f + 0.012f) * u, 0.004f * u), 0.003f * u, GrC.silver, pen, shade = false)
}

// ------------------------------------------------------------------------------------------------ the jam cupboard

internal fun DrawScope.grJamCabinet(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.1f
    val wood = Color(0xFFF2EEE6)
    // A little wall cupboard with glass doors, painted cream with mint trim.
    fxBox(u, -0.13f, -0.215f, 0.13f, 0f, d, wood, pen, rad = 0.006f)
    inkedRound(Rect(-0.138f * u, -0.228f * u, 0.138f * u, -0.212f * u), 0.003f * u, MINT_DARK, pen, shade = false)
    if (f.open) {
        fxHollow(u, -0.11f, -0.19f, 0.11f, -0.025f, 0.08f, Color(0xFFFFF4DC), pen, back = Color(0xFFD7B98A))
        fxInShelf(u, -0.11f, 0.11f, -0.11f, 0.08f, Color(0xFFC49A62), pen, 0.007f)
        fxOpenDoor(u, FixtureDoors.jamLeft, wood, wood.darken(0.08f), pen)
        fxOpenDoor(u, FixtureDoors.jamRight, wood, wood.darken(0.08f), pen)
    } else {
        for (s in 0..1) {
            val x0 = if (s == 0) -0.11f else 0.001f
            val r = Rect(x0 * u, -0.19f * u, (x0 + 0.109f) * u, -0.025f * u)
            inkedRound(r, 0.004f * u, Color(0xFFE6F4FB), pen, shade = false)
            // Jars of jam shining through the glass.
            val cols = intArrayOf(0xFFD2443A.toInt(), 0xFF3B4F9E.toInt(), 0xFFFFA23A.toInt(), 0xFF7A3B6E.toInt())
            for (row in 0..1) for (col in 0..1) {
                val jc = Offset(r.left + (0.027f + col * 0.055f) * u, r.bottom - 0.016f * u - row * 0.07f * u)
                inkedRound(Rect(jc.x - 0.014f * u, jc.y - 0.034f * u, jc.x + 0.014f * u, jc.y), 0.004f * u, Color(cols[(s * 2 + col + row) % 4]), pen, shade = false)
                drawRect(Color(0xFFF2EEE6), Offset(jc.x - 0.015f * u, jc.y - 0.04f * u), Size(0.03f * u, 0.008f * u))
                drawRect(Color(0xFFD2443A), Offset(jc.x - 0.015f * u, jc.y - 0.04f * u), Size(0.03f * u, 0.003f * u))
            }
            drawLine(Ink.line, Offset(r.left, r.center.y + 0.01f * u), Offset(r.right, r.center.y + 0.01f * u), pen.lw * 1.4f)
            drawLine(Color.White.copy(alpha = 0.6f), Offset(r.left + 0.01f * u, r.top + 0.01f * u), Offset(r.left + 0.02f * u, r.top + 0.05f * u), pen.lw * 1.2f)
            drawRoundRect(wood.darken(0.2f), r.topLeft, r.size, CornerRadius(0.004f * u), style = Stroke(pen.lw * 1.6f))
            grKnob(Offset(if (s == 0) r.right - 0.008f * u else r.left + 0.008f * u, r.center.y), 0.005f * u, pen)
        }
    }
}

// ------------------------------------------------------------------------------------------------ the island and its stools

internal fun DrawScope.grIsland(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.2f
    grShadow(u, 0.72f, d)
    // Pendant lamps over the island, glowing in the evening.
    for (s in -1..1 step 2) {
        val c = q(s * 0.2f, -0.52f, 0.1f)
        drawLine(Ink.line, Offset(c.x, -f.y * u), c, pen.lw)
        val shade = Path().apply {
            moveTo(c.x - 0.012f * u, c.y)
            lineTo(c.x + 0.012f * u, c.y)
            quadraticTo(c.x + 0.036f * u, c.y + 0.01f * u, c.x + 0.036f * u, c.y + 0.034f * u)
            lineTo(c.x - 0.036f * u, c.y + 0.034f * u)
            quadraticTo(c.x - 0.036f * u, c.y + 0.01f * u, c.x - 0.012f * u, c.y)
            close()
        }
        grGlow(Offset(c.x, c.y + 0.04f * u), 0.12f * u, pen, 0.12f)
        inked(shade, Color(0xFF2D6C73), pen)
        drawOval(Color(0xFFFFF3C4), Offset(c.x - 0.02f * u, c.y + 0.03f * u), Size(0.04f * u, 0.012f * u))
        shine(Offset(c.x - 0.02f * u, c.y + 0.014f * u), 0.006f * u, 0.016f * u, 0.6f)
    }
    // The base cabinet in navy with brass pulls; a big butcher's block on top with a lip.
    fxBox(u, -0.33f, -0.2f, 0.33f, 0f, d - 0.03f, Color(0xFF2B3F6B), pen, rad = 0.006f, z = 0.015f, top = Color(0xFF3B5486), side = Color(0xFF1E2C4C))
    for (k in 0 until 3) {
        val x0 = -0.31f + k * 0.21f
        val r = Rect(x0 * u, -0.182f * u, (x0 + 0.19f) * u, -0.02f * u)
        drawRoundRect(Color(0xFF1E2C4C), r.topLeft, r.size, CornerRadius(0.004f * u), style = pen.thin)
        drawLine(GrC.brass, Offset(r.center.x - 0.02f * u, r.top + 0.02f * u), Offset(r.center.x + 0.02f * u, r.top + 0.02f * u), 0.005f * u, StrokeCap.Round)
        drawRect(Ink.line.copy(alpha = 0.3f), Offset(r.left + 0.014f * u, r.top + 0.045f * u), Size(r.width - 0.028f * u, r.height - 0.07f * u), style = pen.thin)
    }
    fxBox(u, -0.35f, -0.222f, 0.35f, -0.2f, d + 0.03f, GrC.oak, pen, rad = 0.004f, z = -0.015f, top = GrC.oak.lighten(0.18f), side = GrC.oak.darken(0.2f))
    val top = fxFlat(u, -0.34f, 0.34f, -0.2225f, -0.01f, d + 0.01f, 0.002f)
    clipPath(top) {
        for (k in 0 until 6) drawLine(GrC.oakDark.copy(alpha = 0.35f), q(-0.32f, -0.2225f, 0.0f + k * 0.035f), q(0.32f, -0.2225f, 0.0f + k * 0.035f), pen.lw * 0.5f)
    }
    // The fruit bowl at the left end: red apples, a banana, oranges.
    val bowlC = q(-0.25f, -0.2225f, 0.12f)
    val bowl = Path().apply {
        moveTo(bowlC.x - 0.05f * u, bowlC.y - 0.026f * u)
        lineTo(bowlC.x + 0.05f * u, bowlC.y - 0.026f * u)
        quadraticTo(bowlC.x + 0.04f * u, bowlC.y, bowlC.x, bowlC.y)
        quadraticTo(bowlC.x - 0.04f * u, bowlC.y, bowlC.x - 0.05f * u, bowlC.y - 0.026f * u)
        close()
    }
    val fr = listOf(Color(0xFFE8473F) to -0.03f, Color(0xFFFF9A3D) to 0.0f, Color(0xFFD2443A) to 0.03f, Color(0xFFFFC83D) to 0.015f, Color(0xFF8BD450) to -0.012f)
    for ((i, pr) in fr.withIndex()) {
        val c = Offset(bowlC.x + pr.second * u, bowlC.y - 0.034f * u - (i % 2) * 0.014f * u)
        inkedCircle(c, 0.014f * u, pr.first, pen, shade = false)
        shine(Offset(c.x - 0.005f * u, c.y - 0.005f * u), 0.004f * u, 0.006f * u, 0.7f)
    }
    inked(bowl, Color(0xFFF2EEE6), pen)
    drawArc(Color(0xFF3B6EA5), 10f, 160f, false, Offset(bowlC.x - 0.036f * u, bowlC.y - 0.026f * u), Size(0.072f * u, 0.02f * u), style = Stroke(pen.lw * 1.2f))
    // A cutting board with a knife at the right.
    val board = fxFlat(u, 0.17f, 0.3f, -0.2229f, 0.06f, 0.14f, 0.003f)
    drawPath(board, GrC.oakDark)
    drawPath(board, Ink.line, style = pen.thin)
    val kn = q(0.25f, -0.2232f, 0.1f)
    drawLine(Ink.line, kn, Offset(kn.x + 0.05f * u, kn.y - 0.004f * u), 0.006f * u + pen.lw * 2f, StrokeCap.Round)
    drawLine(GrC.silver, kn, Offset(kn.x + 0.04f * u, kn.y - 0.003f * u), 0.006f * u, StrokeCap.Round)
}

internal fun DrawScope.grStools(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    grShadow(u, 0.5f, 0.08f)
    for (k in 0 until 3) {
        val cx = -0.17f + k * 0.17f
        // Four slim legs and a foot ring.
        for (s in -1..1 step 2) {
            val a = p(cx + s * 0.012f, -0.1f)
            val b = p(cx + s * 0.028f, 0f)
            drawLine(Ink.line, a, b, 0.006f * u + pen.lw * 2f, StrokeCap.Round)
            drawLine(GrC.brassDark, a, b, 0.006f * u, StrokeCap.Round)
        }
        drawOval(GrC.brassDark, Offset((cx - 0.026f) * u, -0.04f * u), Size(0.052f * u, 0.012f * u), style = Stroke(pen.lw * 1.6f))
        // The round leather seat.
        inkedRound(Rect((cx - 0.04f) * u, -0.115f * u, (cx + 0.04f) * u, -0.09f * u), 0.012f * u, listOf(Color(0xFFD9774F), Color(0xFF2D6C73), Color(0xFFD9A93E))[k], pen)
        drawLine(Color.White.copy(alpha = 0.4f), p(cx - 0.03f, -0.108f), p(cx - 0.014f, -0.108f), pen.lw * 1.2f, StrokeCap.Round)
    }
}

// ------------------------------------------------------------------------------------------------ the dumbwaiter

internal fun DrawScope.grDumbwaiter(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val open = f.anim.coerceIn(0f, 1f)
    // A walnut frame with brass trim, two sliding doors and a rope that runs up into the wall.
    val frame = Rect(-0.1f * u, -0.26f * u, 0.1f * u, 0f)
    inkedRound(frame, 0.008f * u, GrC.walnut, pen)
    val hole = Rect(-0.08f * u, -0.235f * u, 0.08f * u, -0.022f * u)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2B1C16), Color(0xFF4A3426)), hole.top, hole.bottom), hole.topLeft, hole.size)
    clipRect(hole.left, hole.top, hole.right, hole.bottom) {
        // The little lift car with a tray and a covered dish.
        val rise = sin(pen.t * 0.8f) * 0.004f * u
        inkedRound(Rect(-0.06f * u, hole.bottom - 0.07f * u + rise, 0.06f * u, hole.bottom - 0.01f * u + rise), 0.004f * u, GrC.oak, pen)
        inkedRound(Rect(-0.052f * u, hole.bottom - 0.078f * u + rise, 0.052f * u, hole.bottom - 0.068f * u + rise), 0.003f * u, GrC.silver, pen, shade = false)
        val cloche = Path().apply {
            moveTo(-0.03f * u, hole.bottom - 0.078f * u + rise)
            quadraticTo(-0.03f * u, hole.bottom - 0.115f * u + rise, 0f, hole.bottom - 0.115f * u + rise)
            quadraticTo(0.03f * u, hole.bottom - 0.115f * u + rise, 0.03f * u, hole.bottom - 0.078f * u + rise)
            close()
        }
        inked(cloche, GrC.silver, pen)
        grKnob(Offset(0f, hole.bottom - 0.12f * u + rise), 0.005f * u, pen)
        // Two sliding doors that part when the hatch is used.
        for (s in 0..1) {
            val dir = if (s == 0) -1f else 1f
            val slide = open * 0.07f * u * dir
            val leaf = Rect(hole.left + s * hole.width / 2f + slide, hole.top, hole.left + (s + 1) * hole.width / 2f + slide, hole.bottom)
            inkedRound(leaf, 0.003f * u, GrC.oak, pen, shade = false)
            for (k in 1..3) drawLine(GrC.oakDark.copy(alpha = 0.6f), Offset(leaf.left + leaf.width * k / 4f, leaf.top), Offset(leaf.left + leaf.width * k / 4f, leaf.bottom), pen.lw * 0.6f)
            grKnob(Offset(if (s == 0) leaf.right - 0.012f * u else leaf.left + 0.012f * u, leaf.center.y), 0.006f * u, pen)
        }
    }
    drawRect(Ink.line, hole.topLeft, hole.size, style = pen.stroke)
    // The brass bell on top and the rope.
    val bell = p(0f, -0.275f)
    val swing = sin(pen.t * 1.4f) * 3f * (0.3f + open)
    drawLine(Ink.line, p(0.06f, -0.26f), p(0.06f, -0.34f), pen.lw)
    rotate(swing, bell) {
        inkedRound(Rect(bell.x - 0.012f * u, bell.y - 0.014f * u, bell.x + 0.012f * u, bell.y + 0.006f * u), 0.006f * u, GrC.brass, pen)
        drawCircle(Ink.line, 0.002f * u, Offset(bell.x, bell.y + 0.009f * u))
    }
    for ((sx, sy) in listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)) fxNail(p(sx * 0.09f, -0.13f + sy * 0.1f), 0.003f * u)
}
