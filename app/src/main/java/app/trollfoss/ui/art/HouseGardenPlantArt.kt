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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/*
 * Hagen's growing things in oblique 3D: the greenhouse with its glass roof and hanging plants, the three
 * beds where vegetables grow into giants you can sit in (pumpkin, tomato, pea pod), the flower beds with
 * bees, the pinwheels, the compost heap with a face, the rain barrel under the drainpipe and the bird house on
 * its pole. Origin at the bottom centre of each fixture's front face. Helpers start with `gp` after `ga`.
 */

internal object GaK {
    val glass = Color(0xFFBFE8F4)
    val glassEdge = Color(0xFF8FC9DE)
    val frame = Color(0xFFF7F5F0)
    val leaf = Color(0xFF55B34A)
    val leafDark = Color(0xFF2F8A44)
    val leafLight = Color(0xFF8FD65F)
    val soil = Color(0xFF6B4A33)
    val soilLight = Color(0xFF8A6446)
    val pumpkin = Color(0xFFFF9A2E)
    val pumpkinDark = Color(0xFFE67B1A)
    val tomato = Color(0xFFE8473F)
    val pea = Color(0xFF7FD05A)
    val peaPod = Color(0xFF4EAD48)
    val sand = Color(0xFFF0CF8A)
    val gold = Color(0xFFFFD447)
    val red = Color(0xFFD2443A)
    val falun = Color(0xFFB8342B)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)
    val woodLight = Color(0xFFE3B27A)
    val water = Color(0xFF6CC3EE)
    val rope = Color(0xFFD9C08A)
    val ropeDark = Color(0xFFA9906A)
    val rubber = Color(0xFF3A3844)
    val steel = Color(0xFFBAC4D4)
    val teal = Color(0xFF3AA6A0)
    val cream = Color(0xFFF6EEDC)
}

private fun DrawScope.gaShadow(u: Float, w: Float, d: Float, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

// ---------------------------------------------------------------------------------------------- greenhouse

/** The greenhouse: a white-framed glass house with an open front, shelves of pots, hanging plants and a misting pipe. */
internal fun DrawScope.gaGreenhouse(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.22f
    val half = 0.43f
    val eave = -0.34f
    val ridge = -0.55f
    val knee = -0.06f
    val frame = GaK.frame
    val glass = GaK.glass.copy(alpha = 0.3f)
    gaShadow(u, 0.88f, d)

    // The floor: planks inside the house.
    fxFace(fxFlat(u, -half, half, -0.012f, 0.0f, d, 0.004f), GaK.woodLight, pen)
    for (k in 1..5) fxLine(q(-half, -0.012f, d * k / 6f), q(half, -0.012f, d * k / 6f), GaK.woodDark.copy(alpha = 0.5f), pen.lw * 0.5f)

    // The back glass wall, seen from inside: shelves with pots, a hanging basket, string lights.
    val backTl = q(-half, eave, d)
    val backBr = q(half, knee, d)
    drawPath(fxPath(backTl, q(half, eave, d), backBr, q(-half, knee, d)), glass)
    for (k in 0..4) {
        val x = -half + 2f * half * k / 4f
        fxLine(q(x, eave, d), q(x, knee, d), frame, pen.lw * 1.3f)
        fxLine(q(x, eave, d), q(x, knee, d), Ink.line.copy(alpha = 0.35f), pen.lw * 0.4f)
    }
    // Two shelves along the back wall with terracotta pots and little plants.
    for ((s, sy) in floatArrayOf(-0.12f, -0.24f).withIndex()) {
        fxBox(u, -0.36f, sy, 0.36f, sy + 0.012f, 0.05f, GaK.wood, pen, z = d - 0.06f)
        for (k in 0 until 6) {
            val px = -0.31f + k * 0.12f + 0.02f * (s % 2)
            val c = q(px, sy, d - 0.04f)
            val potColor = if ((k + s) % 3 == 0) FxC.terracotta else if ((k + s) % 3 == 1) FxC.paint else FxC.sage
            inkedRound(Rect(c.x - 0.014f * u, c.y - 0.024f * u, c.x + 0.014f * u, c.y), 0.003f * u, potColor, pen, shade = false)
            val sway = sin(t * 1.3f + k + s) * 0.004f * u
            for (l in -1..1) fxLine(Offset(c.x, c.y - 0.024f * u), Offset(c.x + l * 0.014f * u + sway, c.y - 0.05f * u + abs(l) * 0.008f * u), GaK.leaf, pen.lw * 1.3f)
            if ((k + s) % 2 == 0) drawCircle(if (k % 2 == 0) FxC.pink else FxC.yellow, 0.007f * u, Offset(c.x + sway, c.y - 0.054f * u))
        }
    }
    // Hanging baskets under the ridge, swaying.
    for ((k, hx) in floatArrayOf(-0.22f, 0.04f, 0.26f).withIndex()) {
        val top = q(hx, ridge + 0.04f + abs(hx) * 0.14f, 0.1f)
        val sway = sin(t * 1.1f + k * 1.9f) * 0.006f * u
        val basket = Offset(top.x + sway, top.y + 0.09f * u)
        fxLine(top, basket, FxC.charcoal, pen.lw * 0.5f)
        inkedRound(Rect(basket.x - 0.017f * u, basket.y, basket.x + 0.017f * u, basket.y + 0.02f * u), 0.004f * u, GaK.woodDark, pen, shade = false)
        fxCloud(GaK.leaf, pen, true, basket.x - 0.012f * u, basket.y - 0.002f * u, 0.011f * u, basket.x + 0.012f * u, basket.y - 0.004f * u, 0.012f * u, basket.x, basket.y - 0.014f * u, 0.011f * u)
        for (v in 0..2) fxLine(Offset(basket.x + (v - 1) * 0.013f * u, basket.y + 0.02f * u), Offset(basket.x + (v - 1) * 0.02f * u + sway * 0.4f, basket.y + (0.06f + 0.015f * v) * u), GaK.leaf, pen.lw * 1.1f)
        if (k != 1) drawCircle(FxC.pink, 0.006f * u, Offset(basket.x + 0.01f * u, basket.y - 0.016f * u))
    }
    // The misting pipe along the ridge with its nozzles, and the mist when somebody has tapped.
    val pipeA = q(-0.38f, ridge + 0.14f + 0.04f, 0.11f)
    val pipeB = q(0.38f, ridge + 0.14f + 0.04f, 0.11f)
    for (k in 0 until 7) {
        val c = fxMix(pipeA, pipeB, k / 6f)
        val y = ridge + 0.04f + abs(-0.38f + k * 0.127f) * 0.5f
        val cc = q(-0.38f + k * 0.127f, y, 0.11f)
        fxLine(Offset(cc.x, cc.y - 0.006f * u), Offset(cc.x, cc.y + 0.004f * u), FxC.steel, pen.lw)
        if (f.anim > 0.02f) {
            for (j in 0 until 4) {
                val ph = fxFrac(t * 3f + j * 0.25f + k * 0.13f)
                drawCircle(FxC.water.copy(alpha = f.anim * (1f - ph)), 0.0035f * u, Offset(cc.x + (j - 1.5f) * 0.01f * ph * u, cc.y + (0.01f + ph * 0.22f) * u))
            }
        }
    }
    // String lights along the back wall: they glow when it is dark.
    for (k in 0 until 9) {
        val x = -0.4f + k * 0.1f
        val c = q(x, eave + 0.03f + sin((k / 8f) * 3.1416f) * 0.02f, d - 0.01f)
        val on = 0.5f + 0.5f * sin(t * 2f + k * 1.3f)
        val col = floatArrayOf(0f, 1f, 2f, 3f)[k % 4].toInt()
        val bulb = arrayOf(FxC.yellow, FxC.pink, FxC.mint, FxC.lilac)[col]
        fxGlow(c, 0.03f * u, bulb, pen.night * (0.3f + 0.5f * on) + (if (f.on) 0.15f else 0f))
        drawCircle(Ink.line, 0.0045f * u, c)
        drawCircle(lerp(bulb.darken(0.2f), bulb.lighten(0.4f), pen.night * on), 0.0034f * u, c)
    }
    // A warm glow inside when the lamps are on at night.
    if (f.on) fxGlow(q(0f, -0.2f, 0.1f), 0.5f * u, FxC.warm, 0.35f)

    // The right side wall: glass in a white frame, with water drops on the panes.
    val sideTf = q(half, eave, 0f)
    val sideBf = q(half, knee, 0f)
    val sideTb = q(half, eave, d)
    val sideBb = q(half, knee, d)
    val side = fxPath(sideTf, sideTb, sideBb, sideBf)
    drawPath(side, GaK.glassEdge.copy(alpha = 0.34f))
    drawPath(side, Ink.line, style = pen.stroke)
    for (k in 1..3) fxLine(fxMix(sideTf, sideTb, k / 4f), fxMix(sideBf, sideBb, k / 4f), frame, pen.lw * 1.4f)
    fxLine(fxMix(sideTf, sideBf, 0.5f), fxMix(sideTb, sideBb, 0.5f), frame, pen.lw * 1.4f)
    // A door in the side wall: a glass door with a brass handle.
    val dA = q(half, eave + 0.05f, 0.05f)
    val dB = q(half, eave + 0.05f, 0.17f)
    val dC = q(half, knee, 0.17f)
    val dD = q(half, knee, 0.05f)
    fxFace(fxPath(dA, dB, dC, dD), FxC.paint.copy(alpha = 0.55f), pen)
    drawCircle(FxC.brass, 0.005f * u, fxMix(dB, dC, 0.45f))
    for (k in 0 until 7) {
        val p = fxMix(sideTf, sideBb, 0.1f + k * 0.12f)
        drawCircle(Color.White.copy(alpha = 0.7f), 0.0028f * u, Offset(p.x + (k % 3) * 0.002f * u, p.y + (k % 2) * 0.012f * u))
    }

    // The knee wall in front and along the right side.
    fxBox(u, -half, knee, half, 0f, d, GaK.woodDark, pen, rad = 0.004f, top = GaK.woodLight, side = GaK.woodDark.darken(0.15f))
    fxFace(fxFlat(u, -half + 0.02f, half - 0.02f, knee, 0.02f, d - 0.02f), GaK.soil, pen)
    for (k in 1..8) fxLine(Offset((-half + k * 0.1f) * u, -0.058f * u), Offset((-half + k * 0.1f) * u, -0.004f * u), GaK.woodDark.darken(0.3f), pen.lw * 0.5f)

    // The roof: two slopes of glass with glazing bars, the ridge cap and a little finial.
    val lf = q(-half - 0.03f, eave, -0.02f)
    val lb = q(-half - 0.03f, eave, d + 0.02f)
    val rf = q(half + 0.03f, eave, -0.02f)
    val rb = q(half + 0.03f, eave, d + 0.02f)
    val tf = q(0f, ridge, -0.02f)
    val tb = q(0f, ridge, d + 0.02f)
    val rightSlope = fxPath(tf, tb, rb, rf)
    val leftSlope = fxPath(lf, lb, tb, tf)
    drawPath(leftSlope, GaK.glass.copy(alpha = 0.42f))
    drawPath(rightSlope, GaK.glassEdge.copy(alpha = 0.5f))
    drawPath(leftSlope, Ink.line, style = pen.stroke)
    drawPath(rightSlope, Ink.line, style = pen.stroke)
    for (k in 1..5) {
        fxLine(fxMix(lf, lb, k / 6f), fxMix(tf, tb, k / 6f), frame, pen.lw * 1.2f)
        fxLine(fxMix(tf, tb, k / 6f), fxMix(rf, rb, k / 6f), frame, pen.lw * 1.2f)
    }
    // A shine sweeping across the roof glass.
    val sh = fxFrac(t * 0.12f) * 1.4f - 0.2f
    if (sh in 0f..1f) {
        val a = fxMix(tf, tb, sh)
        val b = fxMix(rf, rb, sh)
        fxLine(a, b, Color.White.copy(alpha = 0.55f), pen.lw * 3f)
    }
    drawLine(frame, tf, tb, strokeWidth = pen.lw * 3.2f, cap = StrokeCap.Round)
    drawLine(Ink.line, tf, tb, strokeWidth = pen.lw * 0.6f)
    inkedCircle(Offset(tf.x, tf.y - 0.006f * u), 0.007f * u, FxC.brass, pen, shade = false)

    // The front: posts, the eave beam, and the glass gable with its truss, drawn last.
    for (x in floatArrayOf(-half, half)) {
        fxBox(u, x - 0.012f, eave, x + 0.012f, 0f, 0.024f, frame, pen, rad = 0.002f, z = 0f)
    }
    fxBox(u, -half - 0.02f, eave - 0.014f, half + 0.02f, eave + 0.01f, 0.03f, frame, pen, rad = 0.003f, z = -0.02f)
    val gable = Path().apply {
        val a = q(-half, eave - 0.014f, 0f)
        val b = q(0f, ridge, 0f)
        val c = q(half, eave - 0.014f, 0f)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        close()
    }
    drawPath(gable, GaK.glass.copy(alpha = 0.28f))
    drawPath(gable, Ink.line, style = pen.stroke)
    val gTop = q(0f, ridge + 0.005f, 0f)
    for (k in -2..2) fxLine(gTop, q(k * half / 2.5f, eave - 0.014f, 0f), frame, pen.lw * 1.3f)
    fxLine(q(-half * 0.5f, eave - 0.014f - 0.1f, 0f), q(half * 0.5f, eave - 0.014f - 0.1f, 0f), frame, pen.lw * 1.3f)
    // Vines climbing the left post.
    val vine = Path().apply {
        moveTo(-half * u, -0.01f * u)
        for (k in 1..8) quadraticTo((-half + 0.03f * sin(k * 1.7f)) * u, (-0.01f - k * 0.04f + 0.02f) * u, (-half + 0.012f * cos(k * 2.3f)) * u, (-0.01f - k * 0.04f) * u)
    }
    drawPath(vine, Ink.line, style = Stroke(pen.lw * 2.2f, cap = StrokeCap.Round))
    drawPath(vine, GaK.leaf, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
    for (k in 1..7) {
        val vx = -half + 0.02f * sin(k * 2.1f) * (if (k % 2 == 0) 1f else -1f)
        val vy = -0.01f - k * 0.04f
        drawPath(fxLeaf(vx * u, vy * u, (vx + 0.03f * (if (k % 2 == 0) 1f else -1f)) * u, (vy - 0.012f) * u, 0.5f), GaK.leafLight)
        drawPath(fxLeaf(vx * u, vy * u, (vx + 0.03f * (if (k % 2 == 0) 1f else -1f)) * u, (vy - 0.012f) * u, 0.5f), Ink.line, style = pen.thin)
    }
}

// ------------------------------------------------------------------------------------------------ planter

/** One vegetable's colours: the body, the shade and the little highlight. */
private fun gaVegColor(variant: Int): Color = when (variant) {
    0 -> GaK.pumpkin
    1 -> GaK.tomato
    else -> GaK.peaPod
}

/**
 * A raised bed. Mode 0 is bare soil with a seed packet on a stick, 1 a sprout, 2 a leafy plant with little
 * green fruit, 3 a giant vegetable with a seat inside it (the front half is drawn in front of whoever sits).
 */
internal fun DrawScope.gaPlanter(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.15f
    val wood = GaK.wood
    gaShadow(u, 0.2f, d)
    // The bed: four planks round a soil face.
    fxBox(u, -0.1f, -0.05f, 0.1f, 0f, d, wood, pen, rad = 0.003f, top = GaK.soil, side = wood.darken(0.2f))
    fxFace(fxFlat(u, -0.092f, 0.092f, -0.05f, 0.012f, d - 0.012f, 0.003f), GaK.soil, pen)
    fxGrain(Rect(-0.095f * u, -0.047f * u, 0.095f * u, -0.003f * u), wood, pen, 2)
    for (k in 0 until 6) drawCircle(GaK.soilLight, 0.0025f * u, q(-0.08f + (k * 37 % 16) * 0.01f, -0.0505f, 0.02f + (k * 53 % 100) / 100f * 0.1f))
    val base = q(0f, -0.05f, d * 0.55f)
    val sway = sin(t * 1.4f + f.variant) * 0.004f * u
    when (f.mode) {
        0 -> {
            // A seed packet on a stick, ready for planting.
            val stake = q(0.05f, -0.05f, 0.05f)
            capsule(stake, Offset(stake.x, stake.y - 0.06f * u), 0.003f * u, FxC.oak, pen)
            val packet = Rect(stake.x - 0.017f * u, stake.y - 0.1f * u, stake.x + 0.017f * u, stake.y - 0.05f * u)
            inkedRound(packet, 0.003f * u, FxC.paint, pen, shade = false)
            val c = gaVegColor(f.variant)
            when (f.variant) {
                0 -> drawCircle(c, 0.011f * u, Offset(packet.center.x, packet.center.y + 0.004f * u))
                1 -> drawCircle(c, 0.011f * u, Offset(packet.center.x, packet.center.y + 0.004f * u))
                else -> drawOval(c, Offset(packet.center.x - 0.012f * u, packet.center.y - 0.002f * u), Size(0.024f * u, 0.012f * u))
            }
            fxLine(Offset(packet.center.x, packet.top + 0.006f * u), Offset(packet.center.x, packet.top + 0.014f * u), GaK.leaf, pen.lw)
            // A small mound where a seed could go.
            drawPath(fxDisc2(base.x - 0.02f * u, base.y, 0.03f * u, 0.014f * u), GaK.soilLight.copy(alpha = 0.6f))
        }
        1 -> {
            // A sprout: a thin stem, two round seed leaves.
            val tip = Offset(base.x + sway, base.y - 0.055f * u)
            fxLine(base, tip, GaK.leafDark, pen.lw * 2f)
            fxLine(base, tip, GaK.leaf, pen.lw)
            for (s in 0..1) {
                val m = if (s == 0) -1f else 1f
                val lf = fxLeaf(tip.x, tip.y, tip.x + m * 0.035f * u, tip.y - 0.014f * u, 0.6f)
                inked(lf, GaK.leafLight, pen, shade = false)
            }
            drawPath(fxLeaf(tip.x, tip.y, tip.x, tip.y - 0.026f * u, 0.5f), GaK.leaf)
            drawPath(fxLeaf(tip.x, tip.y, tip.x, tip.y - 0.026f * u, 0.5f), Ink.line, style = pen.thin)
        }
        2 -> {
            // A leafy plant: big leaves and a few little green fruits.
            for (k in -2..2) {
                val a = k * 0.4f
                val tip = Offset(base.x + sin(a) * 0.11f * u + sway, base.y - cos(a) * 0.11f * u)
                fxLine(base, tip, GaK.leafDark, pen.lw * 1.4f)
                val lf = fxLeaf(tip.x, tip.y, tip.x + sin(a) * 0.045f * u, tip.y - 0.03f * u, 0.7f)
                inked(lf, if (k % 2 == 0) GaK.leaf else GaK.leafLight, pen, shade = false)
            }
            for ((k, fx) in floatArrayOf(-0.05f, 0.04f).withIndex()) {
                val c = Offset(base.x + fx * u + sway * 0.5f, base.y - (0.026f + 0.01f * k) * u)
                when (f.variant) {
                    0 -> inkedCircle(c, 0.015f * u, Color(0xFFA8D86A), pen, shade = true)
                    1 -> inkedCircle(c, 0.014f * u, Color(0xFFB6E06A), pen, shade = true)
                    else -> inkedOval(Rect(c.x - 0.02f * u, c.y - 0.007f * u, c.x + 0.02f * u, c.y + 0.007f * u), GaK.peaPod, pen)
                }
            }
        }
        else -> gaGiantBack(f, u, pen)
    }
    if (f.mode in 1..2) {
        // Dewdrops after watering: a little sparkle that comes and goes.
        val tw = fxFrac(t * 0.4f + f.variant * 0.3f)
        if (tw < 0.2f) twinkle(Offset(base.x + 0.06f * u, base.y - 0.08f * u), 0.012f * u, Color.White, 1f - tw * 4f)
    }
}

/** The back half of a giant vegetable: its body, its open top with an inside, a leaf or two and a lid leaning on it. */
private fun DrawScope.gaGiantBack(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val c = q(0f, -0.05f, 0.075f)
    val body = gaVegColor(f.variant)
    val dark = body.darken(0.22f)
    val glow = 0.5f + 0.5f * sin(t * 2.2f)
    when (f.variant) {
        0 -> {
            // A pumpkin cut open at the top: the lid is leaning against the side.
            val rx = 0.098f * u
            val top = -0.168f * u
            val bodyPath = Path().apply {
                moveTo(c.x - rx, c.y - 0.02f * u)
                cubicTo(c.x - rx * 1.1f, c.y - 0.1f * u, c.x - rx * 0.8f, top, c.x - rx * 0.5f, top)
                lineTo(c.x + rx * 0.5f, top)
                cubicTo(c.x + rx * 0.8f, top, c.x + rx * 1.1f, c.y - 0.1f * u, c.x + rx, c.y - 0.02f * u)
                cubicTo(c.x + rx * 0.7f, c.y + 0.012f * u, c.x - rx * 0.7f, c.y + 0.012f * u, c.x - rx, c.y - 0.02f * u)
                close()
            }
            inked(bodyPath, body, pen)
            // Ribs.
            for (k in -2..2) {
                val xk = c.x + k * rx * 0.36f
                val rib = Path().apply {
                    moveTo(xk * 0.0f + c.x + k * rx * 0.2f, top + 0.012f * u)
                    quadraticTo(xk + k * rx * 0.14f, c.y - 0.08f * u, c.x + k * rx * 0.34f, c.y - 0.012f * u)
                }
                drawPath(rib, dark.copy(alpha = 0.55f), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
            }
            // The open top: a dark ellipse with an orange rim, the inside of the pumpkin.
            val cavity = fxDisc2(c.x, top + 0.003f * u, rx * 0.5f, 0.052f * u)
            drawPath(cavity, dark.darken(0.25f))
            drawPath(fxDisc2(c.x, top + 0.006f * u, rx * 0.44f, 0.044f * u), Color(0xFFFFC36B).copy(alpha = 0.8f))
            drawPath(cavity, Ink.line, style = pen.stroke)
            // The stem and a curly tendril.
            val stem = Path().apply {
                moveTo(c.x + rx * 0.36f, top + 0.004f * u)
                lineTo(c.x + rx * 0.4f, top - 0.026f * u)
                lineTo(c.x + rx * 0.58f, top - 0.022f * u)
                lineTo(c.x + rx * 0.5f, top + 0.006f * u)
                close()
            }
            inked(stem, GaK.leafDark, pen, shade = false)
            val curl = Path().apply {
                moveTo(c.x - rx * 0.95f, c.y - 0.06f * u)
                cubicTo(c.x - rx * 1.5f, c.y - 0.05f * u, c.x - rx * 1.4f, c.y - 0.12f * u, c.x - rx * 1.2f, c.y - 0.1f * u)
            }
            drawPath(curl, GaK.leafDark, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
            // Big leaves.
            for (s in 0..1) {
                val m = if (s == 0) -1f else 1f
                val a = Offset(c.x + m * rx * 0.9f, c.y - 0.03f * u)
                val lf = fxLeaf(a.x, a.y, a.x + m * 0.06f * u, a.y - 0.07f * u + 0.006f * u * sin(t + s), 0.55f)
                inked(lf, GaK.leaf, pen, shade = false)
            }
        }
        1 -> {
            // A giant tomato with its top cut away; the green leaves are folded back.
            val rx = 0.1f * u
            val top = -0.15f * u
            val bodyPath = Path().apply {
                moveTo(c.x - rx, c.y - 0.04f * u)
                cubicTo(c.x - rx * 1.08f, top - 0.002f * u, c.x - rx * 0.6f, top, c.x - rx * 0.5f, top)
                lineTo(c.x + rx * 0.5f, top)
                cubicTo(c.x + rx * 0.6f, top, c.x + rx * 1.08f, top - 0.002f * u, c.x + rx, c.y - 0.04f * u)
                cubicTo(c.x + rx * 0.9f, c.y + 0.014f * u, c.x - rx * 0.9f, c.y + 0.014f * u, c.x - rx, c.y - 0.04f * u)
                close()
            }
            inked(bodyPath, body, pen)
            shine(Offset(c.x - rx * 0.62f, c.y - 0.085f * u), 0.03f * u, 0.016f * u, 0.7f)
            val cavity = fxDisc2(c.x, top + 0.002f * u, rx * 0.55f, 0.05f * u)
            drawPath(cavity, Color(0xFFB3261E))
            drawPath(fxDisc2(c.x, top + 0.005f * u, rx * 0.47f, 0.042f * u), Color(0xFFFF9A8A).copy(alpha = 0.85f))
            for (k in 0 until 5) drawCircle(Color(0xFFFFF0B8), 0.004f * u, Offset(c.x + (k - 2) * 0.026f * u, top + 0.006f * u + (k % 2) * 0.01f * u))
            drawPath(cavity, Ink.line, style = pen.stroke)
            // The calyx: a star of green leaves lying back on the rim.
            for (k in 0 until 5) {
                val a = -0.9f + k * 0.45f
                val bx = c.x + sin(a) * rx * 0.6f
                val by = top - 0.01f * u - cos(a) * 0.02f * u
                val lf = fxLeaf(bx, by + 0.008f * u, bx + sin(a) * 0.05f * u, by - 0.02f * u, 0.5f)
                inked(lf, GaK.leaf, pen, shade = false)
            }
            // A vine with leaves.
            val vine = Path().apply {
                moveTo(c.x + rx, c.y - 0.04f * u)
                cubicTo(c.x + rx * 1.5f, c.y - 0.06f * u, c.x + rx * 1.4f, c.y - 0.12f * u, c.x + rx * 1.1f, c.y - 0.14f * u)
            }
            drawPath(vine, GaK.leafDark, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
            inked(fxLeaf(c.x + rx * 1.15f, c.y - 0.12f * u, c.x + rx * 1.45f, c.y - 0.15f * u, 0.55f), GaK.leafLight, pen, shade = false)
        }
        else -> {
            // A pea pod as long as a boat, split open along the top: a row of round peas to sit between.
            val rx = 0.105f * u
            val top = -0.1f * u
            val pod = Path().apply {
                moveTo(c.x - rx * 1.05f, c.y - 0.03f * u)
                cubicTo(c.x - rx * 0.9f, c.y + 0.016f * u, c.x + rx * 0.9f, c.y + 0.016f * u, c.x + rx * 1.15f, c.y - 0.045f * u)
                quadraticTo(c.x + rx * 1.3f, c.y - 0.06f * u, c.x + rx * 1.2f, c.y - 0.065f * u)
                cubicTo(c.x + rx * 0.8f, c.y - 0.1f * u, c.x - rx * 0.8f, c.y - 0.1f * u, c.x - rx * 1.05f, c.y - 0.03f * u)
                close()
            }
            inked(pod, body, pen)
            val inner = Path().apply {
                moveTo(c.x - rx * 0.92f, c.y - 0.05f * u)
                cubicTo(c.x - rx * 0.6f, top - 0.012f * u, c.x + rx * 0.6f, top - 0.012f * u, c.x + rx * 1.0f, c.y - 0.062f * u)
                cubicTo(c.x + rx * 0.6f, c.y - 0.052f * u, c.x - rx * 0.6f, c.y - 0.052f * u, c.x - rx * 0.92f, c.y - 0.05f * u)
                close()
            }
            drawPath(inner, Color(0xFFBFE87F))
            drawPath(inner, Ink.line, style = pen.thin)
            // The peas in a row, with a tiny sparkle on one.
            for (k in 0 until 6) {
                val px = c.x + (k - 2.5f) * rx * 0.34f
                val py = c.y - 0.07f * u - sin((k / 5f) * 3.1416f) * 0.012f * u
                inkedCircle(Offset(px, py), 0.0125f * u, GaK.pea, pen, shade = true)
            }
            // A curly stem tip and tendrils.
            val curl = Path().apply {
                moveTo(c.x + rx * 1.18f, c.y - 0.06f * u)
                cubicTo(c.x + rx * 1.5f, c.y - 0.1f * u, c.x + rx * 1.4f, c.y - 0.15f * u, c.x + rx * 1.15f, c.y - 0.13f * u)
            }
            drawPath(curl, GaK.leafDark, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
            inked(fxLeaf(c.x - rx * 0.9f, c.y - 0.06f * u, c.x - rx * 1.4f, c.y - 0.12f * u, 0.5f), GaK.leaf, pen, shade = false)
            inked(fxLeaf(c.x - rx * 0.9f, c.y - 0.06f * u, c.x - rx * 1.45f, c.y - 0.04f * u, 0.5f), GaK.leafLight, pen, shade = false)
        }
    }
    // The vegetable is ripe: a little star that twinkles over it.
    twinkle(Offset(c.x + 0.09f * u, c.y - 0.19f * u), (0.012f + 0.006f * glow) * u, GaK.gold, 0.5f + 0.5f * glow)
}

/** The front half of a giant vegetable: the rim and the belly that sit in front of whoever sits inside. */
internal fun DrawScope.gaPlanterFront(f: Fixture, u: Float, pen: Pen) {
    if (f.mode < 3) return
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val c = q(0f, -0.05f, 0.075f)
    val body = gaVegColor(f.variant)
    val dark = body.darken(0.22f)
    when (f.variant) {
        0 -> {
            val rx = 0.098f * u
            val front = Path().apply {
                moveTo(c.x - rx * 0.97f, c.y - 0.075f * u)
                cubicTo(c.x - rx * 0.8f, c.y - 0.1f * u, c.x - rx * 0.4f, c.y - 0.112f * u, c.x, c.y - 0.112f * u)
                cubicTo(c.x + rx * 0.4f, c.y - 0.112f * u, c.x + rx * 0.8f, c.y - 0.1f * u, c.x + rx * 0.97f, c.y - 0.075f * u)
                cubicTo(c.x + rx * 0.7f, c.y + 0.014f * u, c.x - rx * 0.7f, c.y + 0.014f * u, c.x - rx * 0.97f, c.y - 0.075f * u)
                close()
            }
            inked(front, body, pen)
            for (k in -2..2) {
                val rib = Path().apply {
                    moveTo(c.x + k * rx * 0.3f, c.y - 0.108f * u)
                    quadraticTo(c.x + k * rx * 0.42f, c.y - 0.05f * u, c.x + k * rx * 0.33f, c.y - 0.006f * u)
                }
                drawPath(rib, dark.copy(alpha = 0.55f), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
            }
            shine(Offset(c.x - rx * 0.55f, c.y - 0.07f * u), 0.03f * u, 0.014f * u, 0.55f)
        }
        1 -> {
            val rx = 0.1f * u
            val front = Path().apply {
                moveTo(c.x - rx * 0.98f, c.y - 0.075f * u)
                cubicTo(c.x - rx * 0.8f, c.y - 0.09f * u, c.x - rx * 0.4f, c.y - 0.098f * u, c.x, c.y - 0.098f * u)
                cubicTo(c.x + rx * 0.4f, c.y - 0.098f * u, c.x + rx * 0.8f, c.y - 0.09f * u, c.x + rx * 0.98f, c.y - 0.075f * u)
                cubicTo(c.x + rx * 0.85f, c.y + 0.014f * u, c.x - rx * 0.85f, c.y + 0.014f * u, c.x - rx * 0.98f, c.y - 0.075f * u)
                close()
            }
            inked(front, body, pen)
            shine(Offset(c.x - rx * 0.55f, c.y - 0.062f * u), 0.034f * u, 0.015f * u, 0.7f)
        }
        else -> {
            val rx = 0.105f * u
            val front = Path().apply {
                moveTo(c.x - rx * 1.0f, c.y - 0.042f * u)
                cubicTo(c.x - rx * 0.5f, c.y - 0.062f * u, c.x + rx * 0.5f, c.y - 0.062f * u, c.x + rx * 1.1f, c.y - 0.052f * u)
                cubicTo(c.x + rx * 0.8f, c.y + 0.02f * u, c.x - rx * 0.8f, c.y + 0.02f * u, c.x - rx * 1.0f, c.y - 0.042f * u)
                close()
            }
            inked(front, body.lighten(0.05f), pen)
            for (k in 1..3) drawLine(dark.copy(alpha = 0.5f), Offset(c.x + (k - 2) * rx * 0.5f, c.y - 0.052f * u), Offset(c.x + (k - 2) * rx * 0.55f, c.y - 0.004f * u), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
            shine(Offset(c.x - rx * 0.5f, c.y - 0.04f * u), 0.04f * u, 0.012f * u, 0.55f)
        }
    }
}

// -------------------------------------------------------------------------------------------- flower beds

private val GA_BED_COLORS = arrayOf(
    intArrayOf(0xFFE8473F.toInt(), 0xFFFFC83D.toInt(), 0xFFFF8FB1.toInt()),
    intArrayOf(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFE680.toInt()),
    intArrayOf(0xFFFFC83D.toInt(), 0xFFFF9A3D.toInt(), 0xFFFFD84A.toInt()),
    intArrayOf(0xFFB98CFF.toInt(), 0xFF8E6BE8.toInt(), 0xFFD6B8FF.toInt()),
)

/** A flower bed with a stone edge; the flowers sway, a bee buzzes, a butterfly rests. Variants: tulips, daisies, sunflowers, lavender. */
internal fun DrawScope.gaFlowerBed(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.1f
    val kind = f.variant % 4
    val cols = GA_BED_COLORS[kind]
    gaShadow(u, 0.34f, d, 0.8f)
    // A low stone edge, front and right.
    fxBox(u, -0.17f, -0.034f, 0.17f, 0f, d, FxC.stone, pen, rad = 0.008f, top = FxC.stone.lighten(0.12f), side = FxC.stone.darken(0.2f))
    for (k in 1..5) fxLine(Offset((-0.17f + k * 0.0567f) * u, -0.032f * u), Offset((-0.17f + k * 0.0567f) * u, -0.002f * u), FxC.stone.darken(0.3f), pen.lw * 0.5f)
    fxFace(fxFlat(u, -0.155f, 0.155f, -0.034f, 0.016f, d - 0.016f, 0.004f), GaK.soil, pen)
    // The plants, back row first.
    val rows = if (kind == 2) 2 else 3
    for (row in rows - 1 downTo 0) {
        val z = 0.025f + row * 0.034f
        val n = 6 - row
        for (i in 0 until n) {
            val x = -0.14f + (i + 0.5f) * (0.28f / n) + 0.01f * (row % 2)
            val base = q(x, -0.034f, z)
            val sway = sin(t * 1.5f + i * 1.3f + row * 2f + f.id) * (0.006f + f.anim * 0.02f) * u
            val pick = cols[(i + row) % 3]
            when (kind) {
                0 -> {
                    // A tulip: a cup on a stem with two long leaves.
                    val h = (0.06f + 0.015f * ((i + row) % 3)) * u
                    val head = Offset(base.x + sway, base.y - h)
                    fxLine(base, head, GaK.leafDark, pen.lw * 1.6f)
                    drawPath(fxLeaf(base.x, base.y, base.x - 0.014f * u + sway * 0.4f, base.y - h * 0.8f, 0.4f), GaK.leaf)
                    drawPath(fxLeaf(base.x, base.y, base.x + 0.014f * u + sway * 0.4f, base.y - h * 0.75f, 0.4f), GaK.leafLight)
                    val cup = Path().apply {
                        moveTo(head.x - 0.011f * u, head.y - 0.012f * u)
                        quadraticTo(head.x - 0.012f * u, head.y + 0.012f * u, head.x, head.y + 0.014f * u)
                        quadraticTo(head.x + 0.012f * u, head.y + 0.012f * u, head.x + 0.011f * u, head.y - 0.012f * u)
                        lineTo(head.x + 0.005f * u, head.y - 0.004f * u)
                        lineTo(head.x, head.y - 0.016f * u)
                        lineTo(head.x - 0.005f * u, head.y - 0.004f * u)
                        close()
                    }
                    inked(cup, Color(pick), pen, shade = false)
                }
                1 -> {
                    // A daisy: white petals round a yellow heart.
                    val h = (0.05f + 0.014f * ((i + row) % 3)) * u
                    val head = Offset(base.x + sway, base.y - h)
                    fxLine(base, head, GaK.leafDark, pen.lw * 1.4f)
                    drawPath(fxLeaf(base.x, base.y, base.x + 0.013f * u, base.y - h * 0.5f, 0.45f), GaK.leaf)
                    for (p in 0 until 8) {
                        val a = p * 0.785f
                        drawCircle(Color.White, 0.0072f * u, Offset(head.x + cos(a) * 0.011f * u, head.y + sin(a) * 0.011f * u))
                        drawCircle(Ink.line, 0.0072f * u, Offset(head.x + cos(a) * 0.011f * u, head.y + sin(a) * 0.011f * u), alpha = 0.5f, style = pen.thin)
                    }
                    inkedCircle(head, 0.0075f * u, Color(0xFFFFC83D), pen, shade = false)
                }
                2 -> {
                    // A sunflower: tall, with a brown face and long yellow petals.
                    val h = (0.1f + 0.02f * ((i + row) % 2)) * u
                    val head = Offset(base.x + sway * 1.3f, base.y - h)
                    fxLine(base, head, GaK.leafDark, pen.lw * 2f)
                    drawPath(fxLeaf(base.x, base.y - h * 0.4f, base.x - 0.03f * u, base.y - h * 0.55f, 0.5f), GaK.leaf)
                    drawPath(fxLeaf(base.x, base.y - h * 0.3f, base.x + 0.03f * u, base.y - h * 0.45f, 0.5f), GaK.leafLight)
                    for (p in 0 until 12) {
                        val a = p * 0.5236f
                        val tip = Offset(head.x + cos(a) * 0.024f * u, head.y + sin(a) * 0.024f * u)
                        drawPath(fxLeaf(head.x + cos(a) * 0.008f * u, head.y + sin(a) * 0.008f * u, tip.x, tip.y, 0.5f), Color(pick))
                    }
                    inkedCircle(head, 0.011f * u, Color(0xFF7A4B2E), pen)
                }
                else -> {
                    // Lavender: spikes of small purple flowers.
                    val h = (0.065f + 0.02f * ((i + row) % 3)) * u
                    val head = Offset(base.x + sway, base.y - h)
                    fxLine(base, head, GaK.leafDark, pen.lw * 1.2f)
                    for (p in 0 until 5) {
                        val c = Offset(head.x + (p % 2 - 0.5f) * 0.006f * u, head.y + p * 0.0095f * u)
                        drawCircle(Color(pick), 0.0068f * u, c)
                        drawCircle(Ink.line, 0.0068f * u, c, alpha = 0.45f, style = pen.thin)
                    }
                }
            }
        }
    }
    // A bee buzzing about, and a butterfly resting now and then.
    val bee = Offset(sin(t * 1.7f + f.id) * 0.1f * u, (-0.1f + 0.02f * sin(t * 3.1f)) * u)
    val flap = abs(sin(t * 40f))
    drawOval(Color(0xFFFFD447), Offset(bee.x - 0.007f * u, bee.y - 0.004f * u), Size(0.014f * u, 0.009f * u))
    drawLine(Ink.line, Offset(bee.x - 0.001f * u, bee.y - 0.004f * u), Offset(bee.x - 0.001f * u, bee.y + 0.005f * u), strokeWidth = pen.lw * 1.0f)
    drawOval(Color.White, Offset(bee.x - 0.006f * u, bee.y - 0.011f * u * flap), Size(0.008f * u, 0.008f * u), alpha = 0.8f)
}

/** A pinwheel stuck in the soil: four sails in two colours that spin with the wind and when tapped. */
internal fun DrawScope.gaPinwheel(f: Fixture, u: Float, pen: Pen) {
    val kind = f.variant % 2
    val a = if (kind == 0) Color(0xFFE8473F) else Color(0xFF2F9BFF)
    val b = if (kind == 0) Color(0xFFFFC83D) else Color.White
    val hub = Offset(0f, -0.19f * u)
    capsule(Offset(0f, 0f), Offset(0f, -0.17f * u), 0.004f * u, FxC.oak, pen)
    val spin = (f.angle + pen.t * 0.9f) * 57.29578f
    rotate(spin, hub) {
        for (k in 0 until 4) {
            val ang = k * 90f
            rotate(ang, hub) {
                val sail = Path().apply {
                    moveTo(hub.x, hub.y)
                    lineTo(hub.x + 0.034f * u, hub.y - 0.004f * u)
                    lineTo(hub.x + 0.034f * u, hub.y - 0.034f * u)
                    close()
                }
                inked(sail, if (k % 2 == 0) a else b, pen, shade = false)
                drawLine(Ink.line, hub, Offset(hub.x + 0.034f * u, hub.y - 0.034f * u), strokeWidth = pen.lw * 0.5f, alpha = 0.5f)
            }
        }
    }
    inkedCircle(hub, 0.0052f * u, FxC.brass, pen, shade = false)
}

// ----------------------------------------------------------------------------------------------- compost

/**
 * The compost heap: a mound of leaves and peelings in a slatted bin, with a friendly face that looks around and
 * opens its mouth when it is fed. While the golden key is still down in it, a gold sparkle blinks on the heap.
 */
internal fun DrawScope.gaCompost(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.16f
    gaShadow(u, 0.3f, d)
    // The slatted bin behind and at the left: three boards high on posts.
    val wood = GaK.wood.darken(0.05f)
    for (k in 0..2) {
        fxBox(u, -0.15f, -0.15f + k * 0.04f, 0.15f, -0.122f + k * 0.04f, 0.012f, wood, pen, rad = 0.002f, z = d - 0.02f)
    }
    for (x in floatArrayOf(-0.15f, 0.14f)) fxBox(u, x - 0.006f, -0.17f, x + 0.006f, 0f, 0.016f, GaK.woodDark, pen, z = d - 0.022f)
    // The heap: a brown mound with leaves, peels and shells in it.
    val c = q(0f, 0f, 0.08f)
    val mound = Path().apply {
        moveTo(c.x - 0.145f * u, c.y)
        cubicTo(c.x - 0.14f * u, c.y - 0.1f * u, c.x - 0.07f * u, c.y - 0.16f * u, c.x, c.y - 0.16f * u)
        cubicTo(c.x + 0.07f * u, c.y - 0.16f * u, c.x + 0.14f * u, c.y - 0.1f * u, c.x + 0.145f * u, c.y)
        cubicTo(c.x + 0.07f * u, c.y + 0.012f * u, c.x - 0.07f * u, c.y + 0.012f * u, c.x - 0.145f * u, c.y)
        close()
    }
    inked(mound, Color(0xFF7A5A3C), pen)
    // Bits: leaves, an apple core, eggshells, a corn cob, a banana peel.
    fun bit(x: Float, y: Float, color: Color, w: Float, h: Float) {
        inkedOval(Rect(c.x + (x - w / 2f) * u, c.y + (y - h / 2f) * u, c.x + (x + w / 2f) * u, c.y + (y + h / 2f) * u), color, pen, shade = false)
    }
    bit(-0.09f, -0.045f, Color(0xFF8FB04A), 0.035f, 0.016f)
    bit(-0.05f, -0.1f, Color(0xFFE8C46A), 0.03f, 0.014f)
    bit(0.07f, -0.07f, Color(0xFFD9774F), 0.03f, 0.016f)
    bit(0.1f, -0.03f, Color(0xFF9A6A3A), 0.034f, 0.014f)
    bit(-0.01f, -0.135f, FxC.cream, 0.022f, 0.014f)
    bit(0.045f, -0.12f, FxC.cream, 0.018f, 0.012f)
    bit(-0.115f, -0.02f, Color(0xFFFFD54A), 0.036f, 0.012f)
    bit(0.025f, -0.04f, Color(0xFFD8452F), 0.02f, 0.016f)
    for (k in 0 until 10) {
        val a = hash01(k + f.id, 861)
        val b = hash01(k + f.id, 862)
        drawCircle(Color(0xFF5A402A), 0.003f * u, Offset(c.x + (a - 0.5f) * 0.26f * u, c.y - (0.02f + 0.1f * b) * u))
    }
    // The face: two sleepy eyes that look about, and a mouth that opens when fed.
    val look = sin(t * 0.7f) * 0.006f * u
    val blink = fxFrac(t / 3.6f) < 0.04f
    val eyeY = c.y - 0.075f * u
    for (s in 0..1) {
        val ex = c.x + (if (s == 0) -0.036f else 0.036f) * u
        drawCircle(Ink.line, 0.0165f * u, Offset(ex, eyeY))
        drawCircle(Color.White, 0.0135f * u, Offset(ex, eyeY))
        if (blink) {
            drawLine(Ink.line, Offset(ex - 0.012f * u, eyeY), Offset(ex + 0.012f * u, eyeY), strokeWidth = pen.lw * 1.4f)
        } else {
            drawCircle(Ink.line, 0.0062f * u, Offset(ex + look, eyeY + 0.001f * u))
            drawCircle(Color.White, 0.0019f * u, Offset(ex + look - 0.0018f * u, eyeY - 0.002f * u))
        }
    }
    val open = (f.anim * 1.6f).coerceIn(0f, 1f)
    val mouth = Path().apply {
        moveTo(c.x - 0.04f * u, c.y - 0.04f * u)
        quadraticTo(c.x, c.y - 0.03f * u + (0.02f + 0.028f * open) * u, c.x + 0.04f * u, c.y - 0.04f * u)
        quadraticTo(c.x, c.y - 0.04f * u - 0.01f * open * u, c.x - 0.04f * u, c.y - 0.04f * u)
        close()
    }
    drawPath(mouth, if (open > 0.1f) Color(0xFF5A1E2A) else Color(0xFF3A2418))
    drawPath(mouth, Ink.line, style = pen.thin)
    if (open > 0.3f) drawOval(Color(0xFFFF8FA0), Offset(c.x - 0.014f * u, c.y - 0.036f * u + 0.012f * u), Size(0.028f * u, 0.014f * u * open))
    // The golden gleam: it blinks on the heap while the key is still inside.
    if (f.mode == 0) {
        val ph = fxFrac(t / 2.8f)
        if (ph < 0.22f) {
            val k = 1f - ph / 0.22f
            twinkle(Offset(c.x + 0.056f * u, c.y - 0.1f * u), 0.017f * u * (0.5f + 0.5f * k), GaK.gold, k)
        }
    }
    // Steam and a pair of flies.
    fxPuffs(c.x - 0.02f * u, c.y - 0.15f * u, t, 0.012f * u, 0.1f * u, Color(0xFFD9D2C0), 0.3f, 3, 0.25f, 0.015f * u)
    for (k in 0..1) {
        val fa = t * (2.4f + k) + k * 3f
        val fp = Offset(c.x + cos(fa) * 0.07f * u + 0.03f * u * (k - 0.5f), c.y - 0.19f * u + sin(fa * 1.7f) * 0.02f * u)
        drawCircle(Ink.line, 0.0034f * u, fp)
        drawOval(Color.White, Offset(fp.x - 0.004f * u, fp.y - 0.006f * u), Size(0.005f * u, 0.004f * u), alpha = 0.7f)
    }
}

// ------------------------------------------------------------------------------------------------ barrel

/** The rain barrel under the drainpipe: oak staves, iron hoops, a tap that drips and water to the brim. */
internal fun DrawScope.gaBarrel(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    gaShadow(u, 0.14f, 0.1f)
    val c = q(0f, 0f, 0.05f)
    val rb = 0.062f * u
    val rt = 0.058f * u
    val top = c.y - 0.2f * u
    // The body, wider in the middle: staves shown as vertical lines.
    val body = Path().apply {
        moveTo(c.x - rb, c.y)
        cubicTo(c.x - rb * 1.22f, c.y - 0.07f * u, c.x - rb * 1.22f, c.y - 0.13f * u, c.x - rt, top)
        lineTo(c.x + rt, top)
        cubicTo(c.x + rb * 1.22f, c.y - 0.13f * u, c.x + rb * 1.22f, c.y - 0.07f * u, c.x + rb, c.y)
        close()
    }
    drawPath(body, Brush.horizontalGradient(0f to GaK.wood.lighten(0.15f), 0.5f to GaK.wood, 1f to GaK.woodDark, startX = c.x - rb * 1.2f, endX = c.x + rb * 1.2f))
    for (k in 1..6) {
        val x = c.x - rb * 1.1f + k * rb * 2.2f / 7f
        drawLine(GaK.woodDark.copy(alpha = 0.55f), Offset(x, top + 0.004f * u), Offset(x, c.y - 0.004f * u), strokeWidth = pen.lw * 0.5f)
    }
    drawPath(body, Ink.line, style = pen.stroke)
    // Iron hoops.
    for (hy in floatArrayOf(0.035f, 0.1f, 0.165f)) {
        val y = c.y - hy * u
        val spread = rb * (1f + 0.2f * sin((hy / 0.2f) * 3.1416f))
        drawLine(Ink.line, Offset(c.x - spread * 1.05f, y), Offset(c.x + spread * 1.05f, y), strokeWidth = 0.011f * u, cap = StrokeCap.Round)
        drawLine(FxC.iron.lighten(0.1f), Offset(c.x - spread * 1.04f, y), Offset(c.x + spread * 1.04f, y), strokeWidth = 0.0075f * u, cap = StrokeCap.Round)
    }
    // The water at the brim, with a ripple under the drip.
    drawPath(fxDisc2(c.x, top, rt, 0.034f * u), GaK.woodDark)
    drawPath(fxDisc2(c.x, top + 0.002f * u, rt * 0.88f, 0.028f * u), GaK.water)
    drawPath(fxDisc2(c.x, top, rt, 0.034f * u), Ink.line, style = pen.stroke)
    val ph = fxFrac(t * 0.9f + f.id * 0.1f)
    val ripple = fxDisc2(c.x - 0.004f * u, top + 0.001f * u, (0.006f + ph * 0.03f) * u, (0.003f + ph * 0.012f) * u)
    drawPath(ripple, Color.White.copy(alpha = 0.7f * (1f - ph)), style = pen.thin)
    // A drip from the drainpipe, falling into the barrel.
    val dpy = top - 0.06f * u + ph * 0.06f * u
    drawOval(GaK.water, Offset(c.x - 0.0045f * u - 0.04f * u, dpy), Size(0.009f * u, 0.014f * u), alpha = 1f - ph * 0.3f)
    // The tap at the bottom with a slow drip.
    val tap = Offset(c.x + rb * 1.15f, c.y - 0.03f * u)
    capsule(Offset(c.x + rb * 1.0f, tap.y), Offset(tap.x + 0.02f * u, tap.y), 0.006f * u, FxC.brass, pen)
    val ph2 = fxFrac(t * 0.6f)
    drawCircle(GaK.water, 0.0034f * u, Offset(tap.x + 0.02f * u, tap.y + (0.008f + ph2 * 0.03f) * u), alpha = 1f - ph2)
    if (f.anim > 0.05f) {
        for (k in 0 until 5) {
            val a = k * 1.2f
            drawCircle(GaK.water.copy(alpha = f.anim), 0.004f * u, Offset(c.x + cos(a) * 0.03f * u, top - sin((1f - f.anim) * 3.1416f) * (0.03f + 0.01f * k) * u))
        }
    }
}

// -------------------------------------------------------------------------------------------- bird house

/** A bird house on a tall pole: red with white trim and a round door, a perch, a seed dish, a weather vane and a small bird. */
internal fun DrawScope.gaBirdhouse(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    gaShadow(u, 0.1f, 0.07f, 0.7f)
    val base = q(0f, 0f, 0.04f)
    // The pole, tapering a little, with a foot.
    fxCyl(base.x, base.y, base.y - 0.34f * u, 0.014f * u, 0.011f * u, GaK.woodDark, pen, top = GaK.wood, cap = false)
    inkedOval(Rect(base.x - 0.03f * u, base.y - 0.012f * u, base.x + 0.03f * u, base.y + 0.006f * u), FxC.stone, pen, shade = false)
    // The house: a box in oblique 3D with a gable roof.
    val hy = -0.34f
    fxBox(u, -0.055f, hy - 0.1f, 0.055f, hy, 0.07f, GaK.falun, pen, rad = 0.004f, z = 0.005f, top = GaK.falun.lighten(0.2f), side = GaK.falun.darken(0.22f))
    val front = fxFront(u, -0.055f, hy - 0.1f, 0.055f, hy, 0.005f)
    // The round door and its perch.
    val hole = Offset(front.center.x, front.top + 0.04f * u)
    drawCircle(Ink.line, 0.0185f * u, hole)
    drawCircle(Color(0xFF2A1E26), 0.0165f * u, hole)
    drawArc(Color.White, 180f, 180f, false, Offset(hole.x - 0.0225f * u, hole.y - 0.0225f * u), Size(0.045f * u, 0.045f * u), style = Stroke(0.006f * u))
    capsule(Offset(hole.x, hole.y + 0.026f * u), Offset(hole.x, hole.y + 0.036f * u), 0.0035f * u, FxC.oak, pen)
    // The bird that comes to look: out of the door when tapped, now and then on its own.
    val show = if (f.anim > 0.05f) f.anim.coerceIn(0f, 1f) else if (fxFrac(t / 7f) in 0.1f..0.28f) 0.8f else 0f
    if (show > 0f) {
        val bc = Offset(hole.x, hole.y - 0.002f * u + (1f - show) * 0.012f * u)
        inkedCircle(bc, 0.0135f * u, Color(0xFF6FA8E8), pen)
        inked(fxPoly(1f, bc.x + 0.011f * u, bc.y - 0.002f * u, bc.x + 0.024f * u, bc.y + 0.002f * u, bc.x + 0.011f * u, bc.y + 0.006f * u), FxC.yellow, pen, shade = false)
        val e = Offset(bc.x + 0.0035f * u, bc.y - 0.0035f * u)
        drawCircle(Ink.line, 0.0025f * u, e)
        drawCircle(Color.White, 0.001f * u, Offset(e.x - 0.0007f * u, e.y - 0.0008f * u))
        for (k in 0..1) inked(fxPoly(1f, bc.x - 0.006f * u + k * 0.004f * u, bc.y - 0.012f * u, bc.x - 0.004f * u + k * 0.004f * u, bc.y - 0.022f * u, bc.x - 0.002f * u + k * 0.004f * u, bc.y - 0.012f * u), Color(0xFF3F78C8), pen, shade = false)
    }
    // White trim, then the gable roof with shingles on top of it all.
    fxBox(u, -0.06f, hy - 0.006f, 0.06f, hy + 0.004f, 0.075f, FxC.paint, pen, rad = 0.002f, z = 0.0f)
    val rl = q(-0.07f, hy - 0.1f, 0.0f)
    val rr = q(0.07f, hy - 0.1f, 0.0f)
    val peak = q(0f, hy - 0.155f, 0.0f)
    val rrb = q(0.07f, hy - 0.1f, 0.085f)
    val peakB = q(0f, hy - 0.155f, 0.085f)
    fxFace(fxPath(peak, peakB, rrb, rr), GaK.woodDark, pen)
    fxFace(fxPath(rl, peak, rr), GaK.woodDark.lighten(0.1f), pen)
    for (k in 1..2) fxLine(fxMix(rl, peak, k / 3f), fxMix(rr, peak, k / 3f), GaK.woodDark.darken(0.3f), pen.lw * 0.6f)
    // The weather vane: a rooster that turns slowly.
    val vane = Offset(peak.x + 0.014f * u, peak.y - 0.004f * u)
    fxLine(Offset(vane.x, vane.y + 0.01f * u), Offset(vane.x, vane.y - 0.03f * u), FxC.iron, pen.lw)
    val turn = sin(t * 0.5f + f.id) * 0.8f
    drawLine(FxC.iron, Offset(vane.x - 0.02f * u * cos(turn), vane.y - 0.03f * u), Offset(vane.x + 0.02f * u * cos(turn), vane.y - 0.03f * u), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    inked(fxPoly(1f, vane.x + 0.02f * u * cos(turn), vane.y - 0.036f * u, vane.x + 0.032f * u * cos(turn), vane.y - 0.03f * u, vane.x + 0.02f * u * cos(turn), vane.y - 0.024f * u), FxC.red, pen, shade = false)
    // The seed dish on a bracket.
    val dish = q(0.075f, hy + 0.03f, 0.04f)
    capsule(q(0.055f, hy + 0.01f, 0.04f), dish, 0.004f * u, GaK.woodDark, pen)
    inkedOval(Rect(dish.x - 0.016f * u, dish.y - 0.005f * u, dish.x + 0.016f * u, dish.y + 0.007f * u), FxC.oak, pen, shade = false)
    for (k in 0 until 4) drawCircle(Color(0xFFE8C46A), 0.0022f * u, Offset(dish.x - 0.009f * u + k * 0.006f * u, dish.y - 0.004f * u))
}
