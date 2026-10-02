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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Thing
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// The farm and the space station, in oblique 3D.

// ------------------------------------------------------------------------------------------ farm

/**
 * A wheel seen from the side: its tread runs back in depth [w] units. [spin] turns the spokes and
 * lugs (radians).
 */
private fun DrawScope.fxWheel(u: Float, c: Offset, r: Float, w: Float, spin: Float, hub: Color, pen: Pen) {
    val ext = Oblique.offset(w, u)
    val nx = 0.584f * r
    val ny = 0.811f * r
    fxFace(ovalPath(Rect(Offset(c.x + ext.x, c.y + ext.y), r)), FxC.rubber.darken(0.2f), pen)
    val side = fxQuad(c.x + nx, c.y + ny, c.x + nx + ext.x, c.y + ny + ext.y, c.x - nx + ext.x, c.y - ny + ext.y, c.x - nx, c.y - ny)
    drawPath(side, FxC.rubber.darken(0.1f))
    drawLine(Ink.line, Offset(c.x + nx, c.y + ny), Offset(c.x + nx + ext.x, c.y + ny + ext.y), pen.lw)
    drawLine(Ink.line, Offset(c.x - nx, c.y - ny), Offset(c.x - nx + ext.x, c.y - ny + ext.y), pen.lw)
    inkedCircle(c, r, FxC.rubber, pen)
    for (k in 0 until 14) {
        val a = spin + k * 2f * FX_PI / 14f
        drawLine(FxC.rubber.lighten(0.2f), Offset(c.x + cos(a) * r * 0.78f, c.y + sin(a) * r * 0.78f), Offset(c.x + cos(a + 0.12f) * r * 0.96f, c.y + sin(a + 0.12f) * r * 0.96f), r * 0.07f, StrokeCap.Round)
    }
    inkedCircle(c, r * 0.58f, FxC.cream, pen, shade = false)
    for (k in 0 until 6) {
        val a = spin + k * FX_PI / 3f
        drawCircle(FxC.cream.darken(0.3f), r * 0.07f, Offset(c.x + cos(a) * r * 0.38f, c.y + sin(a) * r * 0.38f))
    }
    inkedCircle(c, r * 0.22f, hub, pen)
    for (k in 0 until 5) {
        val a = spin + k * 2f * FX_PI / 5f
        drawCircle(Ink.line, r * 0.03f, Offset(c.x + cos(a) * r * 0.13f, c.y + sin(a) * r * 0.13f))
    }
}

private const val TRACTOR_RR = 0.12f
private const val TRACTOR_FR = 0.065f

internal fun DrawScope.fxTractor(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val red = FxC.red
    val dark = FxC.charcoal
    val bob = f.bob * u
    val rear = f.angle / TRACTOR_RR
    val front = f.angle / TRACTOR_FR
    // The far wheels peek out behind the body.
    fxWheel(u, q(-0.11f, -0.12f, 0.17f), TRACTOR_RR * u, 0.04f, rear, red, pen)
    fxWheel(u, q(0.17f, -0.065f, 0.16f), TRACTOR_FR * u, 0.03f, front, red, pen)
    translate(0f, bob) {
        // Cab frame: posts at the back of the frame first.
        for (x in floatArrayOf(-0.2f, 0.045f)) capsule(q(x, -0.14f, 0.18f), q(x, -0.47f, 0.18f), 0.008f * u, dark, pen)
        fxBox(u, -0.14f, -0.14f, 0.2f, -0.1f, 0.12f, dark, pen, z = 0.04f)
        val cz = 0.1f
        val air = q(0.06f, -0.2f, cz)
        fxCyl(air.x, air.y, air.y - 0.05f * u, 0.012f * u, 0.012f * u, dark, pen)
        val ex = q(0.15f, -0.2f, cz)
        fxCyl(ex.x, ex.y, ex.y - 0.14f * u, 0.009f * u, 0.009f * u, FxC.steel.darken(0.2f), pen)
        val cap = Offset(ex.x, ex.y - 0.14f * u)
        rotate(if (f.on) sin(t * 30f) * 25f - 10f else -8f, Offset(cap.x - 0.009f * u, cap.y)) {
            drawRoundRect(dark, Offset(cap.x - 0.011f * u, cap.y - 0.004f * u), Size(0.022f * u, 0.005f * u), androidx.compose.ui.geometry.CornerRadius(0.002f * u))
        }
        if (f.on) fxPuffs(cap.x, cap.y - 0.01f * u, t, 0.016f * u, 0.14f * u, Color(0xFF6A6E7A), 0.5f, 5, 1.3f, -0.05f * u)
        // The bonnet with its grille and headlight.
        fxBox(u, -0.02f, -0.2f, 0.235f, -0.11f, 0.12f, red, pen, rad = 0.012f, z = 0.04f)
        val hood = fxFront(u, -0.02f, -0.2f, 0.235f, -0.11f, 0.04f)
        for (k in 0 until 5) fxLine(Offset(hood.left + (0.12f + k * 0.018f) * u, hood.top + 0.03f * u), Offset(hood.left + (0.12f + k * 0.018f) * u, hood.top + 0.06f * u), FxC.cream, 0.004f * u)
        fxLine(Offset(hood.left + 0.01f * u, hood.top + 0.012f * u), Offset(hood.right - 0.012f * u, hood.top + 0.012f * u), red.lighten(0.3f), pen.lw * 0.8f)
        for (k in 0 until 4) {
            val y = -0.19f + k * 0.018f
            fxLine(q(0.235f, y, 0.055f), q(0.235f, y, 0.145f), FxC.cream, 0.004f * u)
        }
        val lamp = q(0.235f, -0.195f, 0.1f)
        fxGlow(lamp, 0.03f * u, FxC.warm, 0.2f + 0.5f * pen.night)
        inkedCircle(lamp, 0.013f * u, FxC.cream, pen)
        shine(Offset(lamp.x - 0.004f * u, lamp.y - 0.004f * u), 0.005f * u, 0.004f * u, 0.8f)
        // Steering wheel, seat and backrest.
        capsule(q(0f, -0.2f, cz), q(-0.03f, -0.28f, cz), 0.006f * u, dark, pen)
        val sw = q(-0.034f, -0.285f, cz)
        drawOval(Ink.line, Offset(sw.x - 0.027f * u, sw.y - 0.008f * u), Size(0.054f * u, 0.016f * u), style = Stroke(0.006f * u + pen.lw * 2f))
        drawOval(dark, Offset(sw.x - 0.027f * u, sw.y - 0.008f * u), Size(0.054f * u, 0.016f * u), style = Stroke(0.006f * u))
        fxBox(u, -0.08f, -0.178f, -0.06f, -0.14f, 0.03f, dark, pen, z = 0.04f)
        fxBox(u, -0.128f, -0.27f, -0.108f, -0.182f, 0.08f, dark.lighten(0.1f), pen, rad = 0.008f, z = 0.015f)
        fxBox(u, -0.11f, -0.19f, -0.03f, -0.178f, 0.1f, dark.lighten(0.1f), pen, rad = 0.004f)
        // Near cab posts and the roof with a beacon.
        for (x in floatArrayOf(-0.2f, 0.045f)) capsule(q(x, -0.14f, 0.02f), q(x, -0.47f, 0.02f), 0.008f * u, dark, pen)
        fxBox(u, -0.225f, -0.49f, 0.07f, -0.47f, 0.2f, red, pen, rad = 0.004f)
        val beacon = q(-0.08f, -0.49f, 0.1f)
        fxCyl(beacon.x, beacon.y, beacon.y - 0.016f * u, 0.009f * u, 0.007f * u, FxC.flame2, pen)
        if (f.on) fxGlow(Offset(beacon.x, beacon.y - 0.01f * u), 0.04f * u, FxC.flame2, 0.4f + 0.4f * sin(t * 8f))
        capsule(q(-0.235f, -0.08f, 0.1f), q(-0.26f, -0.08f, 0.1f), 0.008f * u, dark, pen)
    }
    fxWheel(u, q(0.17f, -0.065f, 0f), TRACTOR_FR * u, 0.03f, front, red, pen)
}

/** The big rear wheel and its mudguard, in front of the driver's legs. */
internal fun DrawScope.fxTractorFront(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val c = q(-0.11f, -0.12f, 0f)
    fxWheel(u, c, TRACTOR_RR * u, 0.045f, f.angle / TRACTOR_RR, FxC.red, pen)
    translate(0f, f.bob * u) {
        val ro = 0.15f * u
        val ri = 0.132f * u
        fun band(o: Offset): Path = Path().apply {
            arcTo(Rect(Offset(c.x + o.x, c.y + o.y), ro), 180f, 180f, true)
            arcTo(Rect(Offset(c.x + o.x, c.y + o.y), ri), 0f, -180f, false)
            close()
        }
        fxFace(band(Oblique.offset(0.05f, u)), FxC.red.darken(0.25f), pen)
        val top = Path().apply {
            val o = Oblique.offset(0.05f, u)
            arcTo(Rect(c, ro), 180f, 180f, true)
            lineTo(c.x + ro + o.x, c.y + o.y)
            arcTo(Rect(Offset(c.x + o.x, c.y + o.y), ro), 0f, -180f, false)
            close()
        }
        fxFace(top, FxC.red.lighten(0.12f), pen)
        inked(band(Offset.Zero), FxC.red, pen)
        inkedCircle(Offset(c.x - ro * 0.92f, c.y - 0.01f * u), 0.007f * u, FxC.flame1, pen, shade = false)
    }
}

internal fun DrawScope.fxHayBale(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val straw = FxC.straw
    val d = 0.13f
    fxBox(u, -0.12f, -0.15f, 0.12f, 0f, d, straw, pen, rad = 0.012f)
    for (i in 0 until 26) {
        val fx = -0.11f + ((i * 37) % 100) / 100f * 0.22f
        val fy = -0.14f + ((i * 53) % 100) / 100f * 0.13f
        val a = if (i % 2 == 0) 0.3f else -0.4f
        fxLine(p(fx, fy), p(fx + cos(a) * 0.016f, fy + sin(a) * 0.016f), straw.darken(0.25f).copy(alpha = 0.7f), pen.lw * 0.5f)
    }
    for (i in 0 until 10) {
        val a = q(-0.1f + ((i * 29) % 100) / 100f * 0.2f, -0.15f, ((i * 41) % 100) / 100f * d)
        fxLine(a, Offset(a.x + 0.014f * u, a.y - 0.002f * u), straw.darken(0.2f).copy(alpha = 0.6f), pen.lw * 0.5f)
    }
    val twine = Color(0xFFFF9F43)
    for (x in floatArrayOf(-0.06f, 0.06f)) {
        fxLine(p(x, -0.15f), p(x, 0f), twine, 0.004f * u)
        fxLine(q(x, -0.15f, 0f), q(x, -0.15f, d), twine, 0.004f * u)
    }
    for (k in 0 until 5) {
        val a = if (k < 3) p(-0.12f, -0.12f + k * 0.04f) else q(0.12f, -0.15f, 0.03f + (k - 3) * 0.06f)
        fxLine(a, Offset(a.x - 0.01f * u, a.y - 0.006f * u), straw.darken(0.1f), pen.lw * 0.6f)
    }
}

internal fun DrawScope.fxCoop(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val red = FxC.falun
    val trim = FxC.paint
    val wood = FxC.woodDark
    val d = 0.18f
    for (x in floatArrayOf(-0.12f, 0.12f)) fxBox(u, x - 0.008f, -0.085f, x + 0.008f, 0f, 0.014f, wood, pen, z = d - 0.02f)
    fxBox(u, -0.14f, -0.09f, 0.14f, -0.078f, d, wood, pen)
    // Walls, then the turf roof over a gable front.
    fxBox(u, -0.13f, -0.25f, 0.13f, -0.088f, d - 0.02f, red, pen, rad = 0.003f, z = 0.01f)
    val wall = fxFront(u, -0.13f, -0.25f, 0.13f, -0.088f, 0.01f)
    for (k in 1..6) fxLine(Offset(wall.left + wall.width * k / 7f, wall.top + 0.004f * u), Offset(wall.left + wall.width * k / 7f, wall.bottom - 0.004f * u), red.darken(0.25f), pen.lw * 0.5f)
    drawRect(trim, wall.topLeft, Size(0.008f * u, wall.height))
    drawRect(trim, Offset(wall.right - 0.008f * u, wall.top), Size(0.008f * u, wall.height))
    // A nest box on the side.
    fxBox(u, 0.13f, -0.19f, 0.175f, -0.13f, 0.08f, red, pen, rad = 0.003f, z = 0.05f)
    fxBox(u, 0.128f, -0.198f, 0.18f, -0.188f, 0.085f, FxC.leaf.darken(0.1f), pen, z = 0.048f)
    val apexF = q(0f, -0.325f, 0f)
    val apexB = q(0f, -0.325f, d)
    val eaveF = q(0.155f, -0.245f, 0f)
    val eaveB = q(0.155f, -0.245f, d)
    fxFace(fxQuad(apexF.x, apexF.y, apexB.x, apexB.y, eaveB.x, eaveB.y, eaveF.x, eaveF.y), FxC.leaf, pen)
    for (k in 0 until 6) {
        val c = fxMix(fxMix(apexF, eaveF, 0.3f + (k % 3) * 0.25f), fxMix(apexB, eaveB, 0.3f + (k % 3) * 0.25f), 0.15f + (k / 3) * 0.5f)
        fxLine(c, Offset(c.x - 0.004f * u, c.y - 0.008f * u), FxC.leaf.darken(0.3f), pen.lw * 0.6f)
        fxLine(c, Offset(c.x + 0.003f * u, c.y - 0.008f * u), FxC.leaf.darken(0.3f), pen.lw * 0.6f)
    }
    drawCircle(FxC.yellow, 0.004f * u, fxMix(fxMix(apexF, eaveF, 0.5f), fxMix(apexB, eaveB, 0.5f), 0.4f))
    drawCircle(Color.White, 0.004f * u, fxMix(fxMix(apexF, eaveF, 0.7f), fxMix(apexB, eaveB, 0.7f), 0.7f))
    inked(fxPoly(u, -0.13f, -0.25f, 0f, -0.315f, 0.13f, -0.25f), red, pen)
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        capsule(p(0.155f * m, -0.243f), p(0f, -0.328f), 0.01f * u, trim, pen)
    }
    val win = p(0f, -0.272f)
    inkedCircle(win, 0.013f * u, Color(0xFF3A3040), pen, shade = false)
    drawLine(trim, Offset(win.x - 0.013f * u, win.y), Offset(win.x + 0.013f * u, win.y), 0.003f * u)
    drawLine(trim, Offset(win.x, win.y - 0.013f * u), Offset(win.x, win.y + 0.013f * u), 0.003f * u)
    // The pop hole with straw, a painted heart above it, and the ramp.
    val hole = Path().apply {
        moveTo(-0.028f * u, -0.09f * u)
        lineTo(-0.028f * u, -0.14f * u)
        quadraticTo(-0.028f * u, -0.172f * u, 0f, -0.172f * u)
        quadraticTo(0.028f * u, -0.172f * u, 0.028f * u, -0.14f * u)
        lineTo(0.028f * u, -0.09f * u)
        close()
    }
    drawPath(hole, trim, style = Stroke(0.012f * u))
    drawPath(hole, Color(0xFF2A1E26))
    drawPath(hole, Ink.line, style = pen.stroke)
    for (k in 0 until 7) fxLine(p(-0.024f + k * 0.008f, -0.09f), p(-0.02f + k * 0.007f, -0.104f + (k % 2) * 0.004f), FxC.straw, pen.lw)
    if (f.anim > 0f) inkedOval(Rect(-0.008f * u, -0.112f * u, 0.008f * u, -0.092f * u), if (f.count % 6 == 0) FxC.yellow else FxC.cream, pen)
    val heart = fxHeart(0f, -0.195f * u, 0.009f * u)
    drawPath(heart, trim)
    drawPath(heart, Ink.line, style = pen.thin)
    val r0 = p(0.02f, -0.088f)
    val r1 = q(0.175f, 0f, -0.02f)
    capsule(r0, r1, 0.016f * u, FxC.oak, pen)
    for (k in 1..4) {
        val c = fxMix(r0, r1, k / 5f)
        fxLine(Offset(c.x - 0.006f * u, c.y - 0.004f * u), Offset(c.x + 0.004f * u, c.y + 0.006f * u), FxC.oakDark, 0.003f * u)
    }
    for (x in floatArrayOf(-0.12f, 0.12f)) fxBox(u, x - 0.008f, -0.078f, x + 0.008f, 0f, 0.014f, wood, pen)
}

internal fun DrawScope.fxVegPatch(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.16f
    val wood = FxC.oakDark
    fxBox(u, -0.22f, -0.045f, 0.22f, 0f, d, wood, pen, rad = 0.003f, top = FxC.soil)
    fxFace(fxFlat(u, -0.21f, 0.21f, -0.045f, 0.01f, d - 0.01f), FxC.soil.darken(0.1f), pen)
    for (k in 0 until 12) {
        val c = q(-0.2f + ((k * 37) % 100) / 100f * 0.4f, -0.046f, 0.02f + ((k * 53) % 100) / 100f * 0.12f)
        drawCircle(FxC.soil.lighten(0.25f), 0.0022f * u, c)
    }
    fxGrain(Rect(-0.21f * u, -0.04f * u, 0.21f * u, -0.005f * u), wood, pen, 2)
    for (x in floatArrayOf(-0.21f, 0.21f)) {
        fxNail(p(x, -0.035f), 0.0018f * u)
        fxNail(p(x, -0.012f), 0.0018f * u)
    }
    // Two rows of plants that grow from sprouts to ripe carrots and potatoes.
    val grow = if (f.mode in 1..2) 0.8f + 0.2f * (1f - (f.timer / 6f).coerceIn(0f, 1f)) else 1f
    for (row in 1 downTo 0) {
        val z = 0.045f + row * 0.065f
        val ridge = fxDisc2(q(0f, -0.046f, z).x, q(0f, -0.046f, z).y, 0.2f * u, 0.018f * u)
        drawPath(ridge, FxC.soil.lighten(0.08f))
        for (i in 0 until 6) {
            val x = -0.165f + i * 0.066f + row * 0.02f
            val base = q(x, -0.048f, z)
            val carrot = (i + row) % 2 == 0
            val sway = sin(t * 1.4f + i + row * 2f) * 3f
            when (f.mode) {
                0 -> drawCircle(FxC.soil.darken(0.2f), 0.003f * u, base)
                1 -> rotate(sway, base) {
                    fxLine(base, Offset(base.x, base.y - 0.012f * u * grow), FxC.leaf, pen.lw)
                    drawOval(FxC.leaf.lighten(0.15f), Offset(base.x - 0.009f * u, base.y - 0.016f * u * grow), Size(0.009f * u, 0.005f * u))
                    drawOval(FxC.leaf.lighten(0.15f), Offset(base.x, base.y - 0.016f * u * grow), Size(0.009f * u, 0.005f * u))
                }
                else -> rotate(sway, base) {
                    val h = (if (f.mode == 3) 0.055f else 0.04f) * grow
                    if (f.mode == 3) {
                        if (carrot) {
                            inked(fxPoly(1f, base.x - 0.008f * u, base.y - 0.004f * u, base.x + 0.008f * u, base.y - 0.004f * u, base.x, base.y + 0.01f * u), Color(0xFFFF8A2E), pen, shade = false)
                        } else {
                            inkedOval(Rect(base.x + 0.004f * u, base.y - 0.008f * u, base.x + 0.022f * u, base.y + 0.004f * u), Color(0xFFC9A06A), pen)
                        }
                    }
                    if (carrot) {
                        for (k in -2..2) {
                            val a = k * 0.35f
                            val tip = Offset(base.x + sin(a) * h * u, base.y - cos(a) * h * u)
                            fxLine(base, tip, FxC.spruceLight, pen.lw * 0.8f)
                            for (j in 1..2) {
                                val m = fxMix(base, tip, j / 3f)
                                fxLine(m, Offset(m.x - 0.004f * u, m.y - 0.003f * u), FxC.leaf, pen.lw * 0.6f)
                                fxLine(m, Offset(m.x + 0.004f * u, m.y - 0.003f * u), FxC.leaf, pen.lw * 0.6f)
                            }
                        }
                    } else {
                        fxCloud(FxC.spruceLight, pen, true, base.x - 0.009f * u, base.y - h * 0.45f * u, h * 0.32f * u, base.x + 0.009f * u, base.y - h * 0.5f * u, h * 0.3f * u, base.x, base.y - h * 0.75f * u, h * 0.3f * u)
                        if (f.mode == 3) drawCircle(Color.White, 0.003f * u, Offset(base.x, base.y - h * 0.95f * u))
                    }
                }
            }
        }
    }
    // A little marker stake with a carrot painted on it.
    val stake = q(-0.2f, -0.045f, 0.012f)
    capsule(stake, Offset(stake.x, stake.y - 0.05f * u), 0.004f * u, FxC.oak, pen)
    val sign = Rect(stake.x - 0.016f * u, stake.y - 0.075f * u, stake.x + 0.016f * u, stake.y - 0.048f * u)
    inkedRound(sign, 0.003f * u, FxC.paint, pen, shade = false)
    inked(fxPoly(1f, sign.center.x - 0.008f * u, sign.center.y - 0.004f * u, sign.center.x + 0.008f * u, sign.center.y - 0.004f * u, sign.center.x, sign.center.y + 0.009f * u), Color(0xFFFF8A2E), pen, shade = false)
    fxLine(Offset(sign.center.x, sign.center.y - 0.004f * u), Offset(sign.center.x, sign.center.y - 0.009f * u), FxC.leaf, pen.lw)
}

internal fun DrawScope.fxTrough(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val wood = Color(0xFF9C7048)
    val iron = Color(0xFF4A4A58)
    val d = 0.12f
    for (x in floatArrayOf(-0.13f, 0.13f)) fxBox(u, x - 0.009f, -0.03f, x + 0.009f, 0f, 0.014f, wood.darken(0.2f), pen, z = d - 0.02f)
    // A green hand pump at the far left end.
    val pump = q(-0.135f, -0.108f, d * 0.55f)
    fxCyl(pump.x, pump.y, pump.y - 0.09f * u, 0.012f * u, 0.012f * u, FxC.spruceLight, pen)
    val top = Offset(pump.x, pump.y - 0.09f * u)
    capsule(top, Offset(top.x - 0.04f * u, top.y - 0.03f * u), 0.005f * u, iron, pen)
    inkedCircle(Offset(top.x - 0.04f * u, top.y - 0.03f * u), 0.006f * u, FxC.red, pen, shade = false)
    val spout = Offset(pump.x + 0.03f * u, pump.y - 0.065f * u)
    capsule(Offset(pump.x, pump.y - 0.07f * u), spout, 0.007f * u, FxC.spruceLight, pen)
    val ph = fxFrac(t * 0.8f)
    drawCircle(FxC.water, 0.003f * u, Offset(spout.x + 0.002f * u, spout.y + 0.006f * u + ph * 0.04f * u))
    // The trough: a wooden box with water nearly to the brim.
    fxBox(u, -0.16f, -0.108f, 0.16f, -0.02f, d, wood, pen, rad = 0.008f, front = false)
    fxFace(fxFlat(u, -0.146f, 0.146f, -0.108f, 0.012f, d - 0.012f, 0.006f), wood.darken(0.4f), pen)
    val water = fxFlat(u, -0.146f, 0.146f, -0.102f, 0.012f, d - 0.012f, 0.006f)
    drawPath(water, FxC.water)
    clipPath(water) {
        for (k in 0 until 3) {
            val w = fxFrac(t * 0.3f + k * 0.33f)
            val c = q(-0.1f + k * 0.1f, -0.102f, 0.03f + w * 0.06f)
            drawArc(Color.White.copy(alpha = 0.6f), 200f, 140f, false, Offset(c.x - 0.02f * u, c.y - 0.004f * u), Size(0.04f * u, 0.01f * u), style = pen.thin)
        }
        val ring = q(-0.105f, -0.102f, d * 0.55f)
        drawPath(fxDisc2(ring.x, ring.y, 0.01f * u + ph * 0.02f * u, 0.006f * u + ph * 0.012f * u), Color.White.copy(alpha = 0.6f * (1f - ph)), style = pen.thin)
        shine(q(0.05f, -0.102f, 0.06f), 0.04f * u, 0.006f * u, 0.5f)
    }
    drawPath(water, Ink.line, style = pen.thin)
    inkedRound(Rect(-0.16f * u, -0.108f * u, 0.16f * u, -0.02f * u), 0.008f * u, wood, pen)
    for (k in 1..2) fxLine(p(-0.155f, -0.108f + k * 0.03f), p(0.155f, -0.108f + k * 0.03f), wood.darken(0.25f), pen.lw * 0.5f)
    for (x in floatArrayOf(-0.1f, 0.1f)) {
        drawRect(iron, p(x - 0.006f, -0.108f), Size(0.012f * u, 0.088f * u))
        fxNail(p(x, -0.095f), 0.0018f * u)
        fxNail(p(x, -0.035f), 0.0018f * u)
    }
    if (f.anim > 0f) {
        for (k in 0 until 4) {
            val c = q(-0.06f + k * 0.04f, -0.11f - sin((1f - f.anim) * FX_PI) * (0.03f + k % 2 * 0.02f), 0.05f)
            drawCircle(FxC.water.copy(alpha = f.anim), 0.004f * u, c)
        }
    }
    for (x in floatArrayOf(-0.13f, 0.13f)) fxBox(u, x - 0.009f, -0.022f, x + 0.009f, 0f, 0.014f, wood.darken(0.2f), pen)
}

internal fun DrawScope.fxWorkbench(f: Fixture, u: Float, pen: Pen, contents: List<Thing>) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = FxC.oak
    val d = 0.16f
    for (x in floatArrayOf(-0.158f, 0.158f)) fxBox(u, x - 0.012f, -0.176f, x + 0.012f, 0f, 0.024f, wood.darken(0.15f), pen, z = d - 0.026f)
    fxBox(u, -0.165f, -0.06f, 0.165f, -0.048f, d - 0.02f, wood.darken(0.1f), pen, z = 0.01f)
    // A toolbox and a can of nails on the lower shelf.
    fxBox(u, -0.13f, -0.098f, -0.06f, -0.06f, 0.05f, FxC.red, pen, rad = 0.004f, z = 0.04f)
    val tb = q(-0.095f, -0.098f, 0.065f)
    drawArc(Ink.line, 180f, 180f, false, Offset(tb.x - 0.014f * u, tb.y - 0.01f * u), Size(0.028f * u, 0.02f * u), style = Stroke(0.004f * u))
    val can = q(0.09f, -0.06f, 0.07f)
    fxCyl(can.x, can.y, can.y - 0.035f * u, 0.016f * u, 0.016f * u, FxC.steel, pen, top = FxC.steel.darken(0.3f))
    fxBox(u, -0.175f, -0.18f, 0.175f, -0.16f, d - 0.02f, wood.darken(0.08f), pen, z = 0.008f)
    inkedRound(Rect(-0.04f * u, -0.176f * u, 0.04f * u, -0.164f * u), 0.002f * u, wood.darken(0.05f), pen, shade = false)
    drawCircle(FxC.brass, 0.003f * u, p(0f, -0.17f))
    fxBox(u, -0.18f, -0.2f, 0.18f, -0.178f, d, wood, pen, rad = 0.003f)
    for (k in 1..5) fxLine(q(-0.18f + k * 0.06f, -0.2f, 0f), q(-0.18f + k * 0.06f, -0.2f, d), wood.darken(0.2f).copy(alpha = 0.5f), pen.lw * 0.5f)
    fxGrain(Rect(-0.175f * u, -0.199f * u, 0.175f * u, -0.179f * u), wood, pen, 1)
    // Shavings and a pencil on the top.
    for (k in 0 until 3) {
        val c = q(0.1f + k * 0.018f, -0.2f, 0.12f - k * 0.02f)
        drawArc(FxC.oakDark, 180f, 250f, false, Offset(c.x - 0.005f * u, c.y - 0.006f * u), Size(0.01f * u, 0.008f * u), style = Stroke(pen.lw * 0.8f))
    }
    capsule(q(0.13f, -0.202f, 0.03f), q(0.16f, -0.202f, 0.09f), 0.004f * u, FxC.yellow, pen)
    // The vise at the left end.
    fxBox(u, -0.205f, -0.226f, -0.165f, -0.2f, 0.035f, FxC.fjord.darken(0.1f), pen, rad = 0.003f, z = 0.005f)
    fxBox(u, -0.2f, -0.2f, -0.17f, -0.182f, 0.02f, FxC.fjord.darken(0.1f), pen, z = -0.012f)
    val screw = p(-0.185f, -0.19f)
    capsule(Offset(screw.x - 0.03f * u, screw.y + 0.01f * u), Offset(screw.x + 0.03f * u, screw.y - 0.01f * u), 0.004f * u, FxC.steel, pen)
    inkedCircle(Offset(screw.x - 0.03f * u, screw.y + 0.01f * u), 0.005f * u, FxC.steel, pen, shade = false)
    inkedCircle(Offset(screw.x + 0.03f * u, screw.y - 0.01f * u), 0.005f * u, FxC.steel, pen, shade = false)
    // What lies on the bench, waiting for a tool.
    val n = contents.size
    contents.forEachIndexed { i, thing ->
        val x = if (n == 1) 0f else -0.07f + i * 0.14f / (n - 1)
        val at = q(x, -0.2f, 0.035f)
        translate(at.x, at.y) { drawThing(thing.type, thing.variant, thing.used, thing.type.w * u, thing.type.h * u, pen) }
    }
    for (x in floatArrayOf(-0.158f, 0.158f)) fxBox(u, x - 0.012f, -0.178f, x + 0.012f, 0f, 0.024f, wood.darken(0.05f), pen, z = 0.002f)
}

internal fun DrawScope.fxToolWall(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val board = Color(0xFFD9B98A)
    fxBox(u, -0.22f, -0.22f, 0.22f, 0f, 0.03f, board, pen, rad = 0.006f)
    drawRoundRect(FxC.woodDark, p(-0.214f, -0.214f), Size(0.428f * u, 0.208f * u), androidx.compose.ui.geometry.CornerRadius(0.005f * u), style = Stroke(0.006f * u))
    for (j in 0 until 5) for (i in 0 until 12) drawCircle(board.darken(0.35f), 0.0024f * u, p(-0.195f + i * 0.0355f, -0.19f + j * 0.04f))
    // Six tools, left to right: hammer, saw, wrench, screwdriver, watering can, seed packet.
    val iron = Color(0xFF7A8294)
    for (i in 0 until 6) {
        val x = -0.22f + (i + 0.5f) * 0.44f / 6f
        val sway = if (f.anim > 0f) sin(pen.t * 20f + i) * 4f * f.anim else 0f
        val peg = p(x, -0.195f)
        fxLine(peg, Offset(peg.x - 0.004f * u, peg.y + 0.006f * u), FxC.charcoal, pen.lw * 1.2f)
        rotate(sway, peg) {
            translate(-0.006f * u, 0.004f * u) {
                when (i) {
                    0 -> {
                        capsule(p(x, -0.16f), p(x, -0.04f), 0.01f * u, FxC.oak, pen)
                        inkedRound(Rect((x - 0.026f) * u, -0.185f * u, (x + 0.026f) * u, -0.162f * u), 0.004f * u, iron, pen)
                        drawLine(Ink.line, p(x + 0.018f, -0.185f), p(x + 0.026f, -0.175f), pen.lw)
                    }
                    1 -> {
                        val blade = fxPoly(u, x - 0.016f, -0.16f, x + 0.012f, -0.16f, x + 0.006f, -0.03f, x - 0.016f, -0.03f)
                        inked(blade, FxC.steel, pen)
                        for (k in 0 until 10) {
                            val y = -0.155f + k * 0.0125f
                            inked(fxPoly(u, x - 0.016f, y, x - 0.022f, y + 0.006f, x - 0.016f, y + 0.0125f), FxC.steel.darken(0.1f), pen, shade = false)
                        }
                        val handle = Rect((x - 0.022f) * u, -0.2f * u, (x + 0.02f) * u, -0.155f * u)
                        inkedRound(handle, 0.012f * u, FxC.red, pen)
                        drawOval(Color(0xFF3A2A20), Offset(handle.left + 0.012f * u, handle.top + 0.012f * u), Size(0.018f * u, 0.02f * u))
                    }
                    2 -> {
                        capsule(p(x, -0.165f), p(x, -0.06f), 0.01f * u, iron, pen)
                        inkedCircle(p(x, -0.176f), 0.015f * u, iron, pen)
                        drawRect(board, p(x - 0.005f, -0.195f), Size(0.01f * u, 0.02f * u))
                        inkedCircle(p(x, -0.055f), 0.011f * u, iron, pen)
                        drawCircle(board.darken(0.3f), 0.005f * u, p(x, -0.055f))
                    }
                    3 -> {
                        capsule(p(x, -0.12f), p(x, -0.05f), 0.005f * u, FxC.steel, pen)
                        inkedRound(Rect((x - 0.01f) * u, -0.18f * u, (x + 0.01f) * u, -0.12f * u), 0.008f * u, FxC.yellow, pen)
                        for (k in 0 until 3) fxLine(p(x - 0.004f + k * 0.004f, -0.172f), p(x - 0.004f + k * 0.004f, -0.13f), FxC.yellow.darken(0.3f), pen.lw * 0.5f)
                    }
                    4 -> {
                        drawArc(Ink.line, 180f, 180f, false, p(x - 0.018f, -0.15f), Size(0.03f * u, 0.03f * u), style = Stroke(0.006f * u))
                        inkedRound(Rect((x - 0.026f) * u, -0.14f * u, (x + 0.018f) * u, -0.07f * u), 0.01f * u, FxC.spruceLight, pen)
                        capsule(p(x + 0.016f, -0.085f), p(x + 0.036f, -0.13f), 0.005f * u, FxC.spruceLight, pen)
                        inkedCircle(p(x + 0.038f, -0.134f), 0.006f * u, FxC.spruceLight.darken(0.1f), pen, shade = false)
                    }
                    else -> {
                        val pack = Rect((x - 0.022f) * u, -0.15f * u, (x + 0.022f) * u, -0.08f * u)
                        inkedRound(pack, 0.004f * u, FxC.cream, pen)
                        drawRect(FxC.sage, Offset(pack.left, pack.top), Size(pack.width, 0.012f * u))
                        inked(fxPoly(u, x - 0.008f, -0.118f, x + 0.008f, -0.118f, x, -0.092f), Color(0xFFFF8A2E), pen, shade = false)
                        fxLine(p(x, -0.118f), p(x - 0.005f, -0.13f), FxC.leaf, pen.lw)
                        fxLine(p(x, -0.118f), p(x + 0.005f, -0.13f), FxC.leaf, pen.lw)
                        inkedRound(Rect((x - 0.004f) * u, -0.162f * u, (x + 0.004f) * u, -0.142f * u), 0.002f * u, FxC.oak, pen, shade = false)
                    }
                }
            }
        }
    }
}

internal fun DrawScope.fxWoodPile(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val woods = arrayOf(FxC.oak, Color(0xFFD9A86A), FxC.wood)
    for (layer in 0 until 5) {
        val y = -layer * 0.027f
        if (layer > 0) {
            for (x in floatArrayOf(-0.08f, 0.08f)) fxBox(u, x - 0.006f, y, x + 0.006f, y + 0.005f, 0.11f, FxC.woodDark, pen, z = 0.005f)
        }
        val shift = if (layer % 2 == 0) 0f else 0.012f
        for (row in 1 downTo 0) {
            val z = row * 0.058f
            val l = -0.125f + shift - row * 0.006f
            val r = 0.115f + shift - row * 0.004f
            val top = y - 0.022f
            val bottom = y - (if (layer > 0) 0.005f else 0f)
            val c = woods[(layer + row) % woods.size]
            fxBox(u, l, top, r, bottom, 0.052f, c, pen, rad = 0.002f, z = z, side = c.lighten(0.1f))
            val end = fxQ(u, r, (top + bottom) / 2f, z + 0.026f)
            drawArc(c.darken(0.3f), 200f, 140f, false, Offset(end.x - 0.008f * u, end.y - 0.006f * u), Size(0.012f * u, 0.012f * u), style = pen.thin)
            fxLine(fxQ(u, l + 0.02f, top + 0.011f, z), fxQ(u, r - 0.02f, top + 0.012f, z), c.darken(0.25f).copy(alpha = 0.6f), pen.lw * 0.5f)
        }
    }
}

internal fun DrawScope.fxTireStack(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.07f
    val r = 0.068f * u
    val core = q(0f, 0f, cz)
    fxCyl(core.x, core.y, q(0f, -0.19f, cz).y, r * 0.72f, r * 0.72f, FxC.rubber.darken(0.3f), pen, cap = false)
    // Three separate tyres: each a squat drum with rounded edges, tread across its face.
    for (k in 0 until 3) {
        val b = q(0f, -k * 0.066f - 0.005f, cz)
        val tp = q(0f, -(k + 1) * 0.066f + 0.005f, cz)
        fxCyl(b.x, b.y, tp.y, r * 0.95f, r * 0.95f, FxC.rubber, pen, cap = k == 2)
        val mid = (b.y + tp.y) / 2f
        val bulge = Path()
        for (j in 0..8) {
            val a = FX_PI + 0.4636f + j * FX_PI / 8f
            val pt = fxRim(b.x, mid, r, a)
            if (j == 0) bulge.moveTo(pt.x, pt.y) else bulge.lineTo(pt.x, pt.y)
        }
        drawPath(bulge, Color.White.copy(alpha = 0.16f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
        for (j in 0 until 7) {
            val a = FX_PI + 0.5f + j * (FX_PI - 1f) / 6f
            val lo = fxRim(b.x, b.y - 0.012f * u, r * 0.97f, a)
            val hi = fxRim(tp.x, tp.y + 0.012f * u, r * 0.97f, a + 0.12f)
            fxLine(lo, hi, FxC.rubber.lighten(0.22f), pen.lw * 0.9f)
        }
    }
    // A planter in the top tyre.
    val top = q(0f, -0.193f, cz)
    val hole = fxDisc(top.x, top.y, r * 0.55f)
    drawPath(hole, FxC.soil)
    drawPath(hole, Ink.line, style = pen.thin)
    for (k in 0 until 3) {
        val base = Offset(top.x + (k - 1) * 0.014f * u, top.y - 0.002f * u)
        val sway = sin(t * 1.3f + k) * 0.003f * u
        val head = Offset(base.x + sway, base.y - (0.03f + k % 2 * 0.012f) * u)
        fxLine(base, head, FxC.spruceLight, pen.lw)
        fxCloud(if (k == 1) FxC.red else FxC.yellow, pen, false, head.x, head.y, 0.006f * u)
        drawCircle(FxC.mustard.darken(0.2f), 0.002f * u, head)
    }
}

// ------------------------------------------------------------------------------------------ space

private val ROCKET_Y = floatArrayOf(-0.06f, -0.1f, -0.16f, -0.3f, -0.45f, -0.52f, -0.58f, -0.64f, -0.69f, -0.73f, -0.76f, -0.78f)
private val ROCKET_R = floatArrayOf(0.1f, 0.118f, 0.125f, 0.125f, 0.124f, 0.121f, 0.114f, 0.098f, 0.074f, 0.048f, 0.022f, 0f)
private const val ROCKET_WIN_Y = -0.57f
private const val ROCKET_WIN_R = 0.08f

/** The rocket's radius at height [y]. */
private fun fxRocketR(y: Float): Float {
    for (i in 0 until ROCKET_Y.size - 1) {
        if (y <= ROCKET_Y[i] && y >= ROCKET_Y[i + 1]) {
            val k = (y - ROCKET_Y[i]) / (ROCKET_Y[i + 1] - ROCKET_Y[i])
            return ROCKET_R[i] + (ROCKET_R[i + 1] - ROCKET_R[i]) * k
        }
    }
    return 0f
}

/** The round hull's outline in pixels from height [from] up to the tip (or down to [from] when drawing the lower part). */
private fun fxRocketHull(u: Float, lowerOnly: Boolean): Path {
    val path = Path()
    val bottom = ROCKET_Y[0]
    val rb = ROCKET_R[0] * u
    // Bottom front arc of the skirt.
    val a0 = FX_PI + 0.4636f
    for (k in 0..6) {
        val a = a0 + k * FX_PI / 6f
        val pt = fxRim(0f, bottom * u, rb, a)
        if (k == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    var last = ROCKET_Y.size - 1
    if (lowerOnly) while (ROCKET_Y[last] < ROCKET_WIN_Y) last--
    for (i in 0..last) {
        val r = ROCKET_R[i] * u
        path.lineTo(r * 1.118f, ROCKET_Y[i] * u - r * 0.161f)
    }
    if (lowerOnly) {
        val r = fxRocketR(ROCKET_WIN_Y) * u
        path.lineTo(r * 1.118f, ROCKET_WIN_Y * u - r * 0.161f)
        path.lineTo(ROCKET_WIN_R * u, ROCKET_WIN_Y * u)
        path.arcTo(Rect(Offset(0f, ROCKET_WIN_Y * u), ROCKET_WIN_R * u), 0f, 180f, false)
        path.lineTo(-r * 1.118f, ROCKET_WIN_Y * u + r * 0.161f)
    }
    for (i in last downTo 0) {
        val r = ROCKET_R[i] * u
        path.lineTo(-r * 1.118f, ROCKET_Y[i] * u + r * 0.161f)
    }
    path.close()
    return path
}

/** A painted band round the hull between heights [y0] and [y1]: its front halves follow the curve. */
private fun DrawScope.fxRocketBand(u: Float, y0: Float, y1: Float, color: Color) {
    val path = Path()
    val a0 = FX_PI + 0.4636f
    for (k in 0..6) {
        val a = a0 + k * FX_PI / 6f
        val pt = fxRim(0f, y0 * u, fxRocketR(y0) * u, a)
        if (k == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    for (k in 6 downTo 0) {
        val a = a0 + k * FX_PI / 6f
        val pt = fxRim(0f, y1 * u, fxRocketR(y1) * u, a)
        path.lineTo(pt.x, pt.y)
    }
    path.close()
    drawPath(path, color)
}

internal fun DrawScope.fxRocket(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val white = Color(0xFFF4F4F8)
    val red = FxC.red
    // Engine fire and smoke while it launches.
    if (f.on) {
        val flying = f.shiftY < -0.01f
        val len = if (flying) 0.2f else 0.1f + 0.03f * sin(t * 30f)
        fxGlow(p(0f, 0.02f), 0.2f * u, FxC.flame2, 0.7f)
        rotate(180f, p(0f, -0.01f)) {
            fxFire(0f, -0.02f * u, 0.08f * u, len * u, t, 0f, pen)
            fxFire(0f, -0.02f * u, 0.05f * u, len * 0.8f * u, t, 2f, pen, false)
        }
        fxPuffs(0f, (0.04f + len) * u, t, 0.04f * u, -0.25f * u, Color(0xFFD5D8E2), 0.6f, 6, 0.9f, 0.1f * u)
        fxPuffs(-0.06f * u, 0.02f * u, t + 0.5f, 0.035f * u, 0.06f * u, Color.White, if (flying) 0.2f else 0.6f, 4, 0.7f, -0.12f * u)
        fxPuffs(0.06f * u, 0.02f * u, t + 0.2f, 0.035f * u, 0.06f * u, Color.White, if (flying) 0.2f else 0.6f, 4, 0.7f, 0.12f * u)
    }
    // The back fin, reaching back in depth.
    val bf0 = q(0f, -0.26f, 0.1f)
    val bf1 = q(0f, -0.06f, 0.1f)
    val bf2 = q(0f, 0f, 0.19f)
    val bf3 = q(0f, -0.12f, 0.19f)
    fxFace(fxQuad(bf0.x, bf0.y, bf3.x, bf3.y, bf2.x, bf2.y, bf1.x, bf1.y, 0.01f * u), red.darken(0.2f), pen)
    // Nozzle and side fins.
    val nz = p(0f, -0.06f)
    fxCyl(nz.x, nz.y + 0.05f * u, nz.y, 0.066f * u, 0.05f * u, FxC.steel.darken(0.25f), pen, cap = false)
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        val fin = Path().apply {
            moveTo(0.125f * m * u, -0.3f * u)
            cubicTo(0.17f * m * u, -0.22f * u, 0.195f * m * u, -0.12f * u, 0.19f * m * u, 0f)
            lineTo(0.152f * m * u, 0f)
            quadraticTo(0.14f * m * u, -0.06f * u, 0.1f * m * u, -0.07f * u)
            close()
        }
        inked(fin, red, pen)
    }
    // The hull, and the cockpit seen through the window.
    val hull = fxRocketHull(u, lowerOnly = false)
    inked(hull, white, pen, outline = false)
    clipPath(hull) {
        fxRocketBand(u, -0.68f, -0.8f, red)
        fxRocketBand(u, -0.16f, -0.2f, red)
    }
    drawPath(hull, Ink.line, style = pen.stroke)
    val win = p(0f, ROCKET_WIN_Y)
    val wr = ROCKET_WIN_R * u
    drawCircle(Color(0xFF1C2440), wr, win)
    clipPath(ovalPath(Rect(win, wr))) {
        inkedRound(Rect(win.x - 0.05f * u, win.y - 0.05f * u, win.x + 0.05f * u, win.y + 0.09f * u), 0.03f * u, FxC.charcoal.lighten(0.15f), pen)
        for (k in 0 until 4) {
            val on = fxFrac(t * 0.7f + k * 0.29f) < 0.5f
            drawCircle(if (on) arrayOf(FxC.red, FxC.green, FxC.yellow, FxC.sky)[k] else FxC.charcoal, 0.004f * u, Offset(win.x - 0.06f * u + k * 0.04f * u, win.y + 0.058f * u))
        }
    }
    drawCircle(Ink.line, wr, win, style = pen.thin)
}

/** The hull below the window, in front of the pilot; the window frame and glass go on top. */
internal fun DrawScope.fxRocketFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val white = Color(0xFFF4F4F8)
    val red = FxC.red
    val lower = fxRocketHull(u, lowerOnly = true)
    inked(lower, white, pen, outline = false)
    clipPath(lower) {
        fxRocketBand(u, -0.16f, -0.2f, red)
        for (y in floatArrayOf(-0.22f, -0.45f)) {
            val a0 = FX_PI + 0.6f
            for (k in 0 until 7) {
                val pt = fxRim(0f, y * u, fxRocketR(y) * u, a0 + k * (FX_PI - 1.2f) / 6f)
                fxNail(pt, 0.0022f * u)
            }
        }
        val star = starPath(p(0.02f, -0.33f), 0.03f * u, 0.013f * u, 8f)
        drawPath(star, FxC.yellow)
        drawPath(star, Ink.line, style = pen.thin)
    }
    // The outline, except along the cut through the window.
    val edge = Path()
    val a0 = FX_PI + 0.4636f
    for (k in 0..6) {
        val pt = fxRim(0f, ROCKET_Y[0] * u, ROCKET_R[0] * u, a0 + k * FX_PI / 6f)
        if (k == 0) edge.moveTo(pt.x, pt.y) else edge.lineTo(pt.x, pt.y)
    }
    for (i in ROCKET_Y.indices) {
        if (ROCKET_Y[i] < ROCKET_WIN_Y) break
        edge.lineTo(ROCKET_R[i] * 1.118f * u, ROCKET_Y[i] * u - ROCKET_R[i] * 0.161f * u)
    }
    val rw = fxRocketR(ROCKET_WIN_Y)
    edge.lineTo(rw * 1.118f * u, ROCKET_WIN_Y * u - rw * 0.161f * u)
    edge.moveTo(-rw * 1.118f * u, ROCKET_WIN_Y * u + rw * 0.161f * u)
    for (i in ROCKET_Y.indices.reversed()) {
        if (ROCKET_Y[i] < ROCKET_WIN_Y) continue
        edge.lineTo(-ROCKET_R[i] * 1.118f * u, ROCKET_Y[i] * u + ROCKET_R[i] * 0.161f * u)
    }
    val first = fxRim(0f, ROCKET_Y[0] * u, ROCKET_R[0] * u, a0)
    edge.lineTo(first.x, first.y)
    drawPath(edge, Ink.line, style = pen.stroke)
    // The front fin.
    val ff0 = q(0f, -0.24f, -0.1f)
    val ff1 = q(0f, -0.07f, -0.1f)
    val ff2 = q(0f, 0f, -0.18f)
    val ff3 = q(0f, -0.1f, -0.18f)
    fxFace(fxQuad(ff0.x, ff0.y, ff3.x, ff3.y, ff2.x, ff2.y, ff1.x, ff1.y, 0.01f * u), red, pen)
    // Window: a bolted ring and a pane of glass with a glint.
    val win = p(0f, ROCKET_WIN_Y)
    val wr = ROCKET_WIN_R * u
    drawCircle(Color(0x33BFE8FF), wr, win)
    drawArc(Color.White.copy(alpha = 0.6f), 200f, 60f, false, Offset(win.x - wr * 0.75f, win.y - wr * 0.75f), Size(wr * 1.5f, wr * 1.5f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
    drawCircle(Ink.line, wr + 0.006f * u, win, style = Stroke(0.012f * u + pen.lw * 2f))
    drawCircle(FxC.steel, wr + 0.006f * u, win, style = Stroke(0.012f * u))
    for (k in 0 until 8) {
        val a = k * FX_PI / 4f
        fxBolt(Offset(win.x + cos(a) * (wr + 0.006f * u), win.y + sin(a) * (wr + 0.006f * u)), 0.0028f * u, pen)
    }
}

/** A little screen picture for the control panel: one of eight patterns. */
private fun DrawScope.fxPanelScreen(mode: Int, r: Rect, u: Float, pen: Pen) {
    val t = pen.t
    val c = FxC.auroraGreen
    val w = r.width
    val h = r.height
    drawRect(Color(0xFF0E1A2E), r.topLeft, r.size)
    when (mode) {
        0 -> {
            val m = r.center
            for (k in 1..2) drawCircle(c.copy(alpha = 0.5f), h * 0.2f * k, m, style = Stroke(pen.lw * 0.5f))
            val a = t * 3f
            drawLine(c, m, Offset(m.x + cos(a) * h * 0.42f, m.y + sin(a) * h * 0.42f), pen.lw * 0.8f)
            drawCircle(c, 0.002f * u, Offset(m.x + h * 0.2f, m.y - h * 0.1f))
        }
        1 -> {
            val wave = Path()
            for (k in 0..10) {
                val x = r.left + w * k / 10f
                val y = r.center.y + sin(k * 0.9f + t * 4f) * h * 0.3f
                if (k == 0) wave.moveTo(x, y) else wave.lineTo(x, y)
            }
            drawPath(wave, c, style = Stroke(pen.lw * 0.8f))
        }
        2 -> for (k in 0 until 6) {
            val bh = h * (0.3f + 0.5f * (0.5f + 0.5f * sin(t * 3f + k * 1.3f)))
            drawRect(if (k % 2 == 0) c else FxC.yellow, Offset(r.left + w * (0.08f + k * 0.15f), r.bottom - bh), Size(w * 0.1f, bh))
        }
        3 -> for (k in 0 until 8) drawCircle(Color.White.copy(alpha = 0.5f + 0.5f * sin(t * 2f + k)), 0.0018f * u, Offset(r.left + w * fxFrac(k * 0.37f + t * 0.03f), r.top + h * fxFrac(k * 0.61f)))
        4 -> {
            val m = r.center
            drawOval(c.copy(alpha = 0.5f), Offset(m.x - w * 0.35f, m.y - h * 0.25f), Size(w * 0.7f, h * 0.5f), style = Stroke(pen.lw * 0.5f))
            drawCircle(FxC.yellow, h * 0.1f, m)
            drawCircle(FxC.sky, h * 0.07f, Offset(m.x + cos(t * 1.5f) * w * 0.35f, m.y + sin(t * 1.5f) * h * 0.25f))
        }
        5 -> {
            val ecg = Path()
            val off = fxFrac(t * 0.6f)
            for (k in 0..12) {
                val fx = k / 12f
                val spike = ((fx + off) % 1f) in 0.45f..0.55f
                val y = r.center.y - (if (spike) (if (k % 2 == 0) h * 0.35f else -h * 0.2f) else 0f)
                if (k == 0) ecg.moveTo(r.left + fx * w, y) else ecg.lineTo(r.left + fx * w, y)
            }
            drawPath(ecg, FxC.red, style = Stroke(pen.lw * 0.8f))
        }
        6 -> for (j in 0 until 3) for (i in 0 until 4) {
            val on = fxFrac(t * 0.8f + i * 0.21f + j * 0.37f) < 0.5f
            drawRect(if (on) c else c.copy(alpha = 0.2f), Offset(r.left + w * (0.08f + i * 0.22f), r.top + h * (0.12f + j * 0.28f)), Size(w * 0.16f, h * 0.2f))
        }
        else -> {
            val y = r.bottom - fxFrac(t * 0.4f) * h * 1.2f
            drawPath(fxPoly(1f, r.center.x, y - h * 0.2f, r.center.x + w * 0.06f, y, r.center.x - w * 0.06f, y), Color.White)
            drawCircle(FxC.flame2, h * 0.05f, Offset(r.center.x, y + h * 0.05f))
        }
    }
}

internal fun DrawScope.fxControlPanel(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val body = Color(0xFFDDE3EE)
    val d = 0.16f
    // Screens on a raised back panel.
    fxBox(u, -0.17f, -0.3f, 0.17f, -0.2f, 0.03f, Color(0xFF3C4A66), pen, rad = 0.006f, z = d - 0.035f)
    val back = fxFront(u, -0.17f, -0.3f, 0.17f, -0.2f, d - 0.035f)
    val scrA = Rect(back.left + 0.012f * u, back.top + 0.012f * u, back.left + 0.16f * u, back.bottom - 0.014f * u)
    val scrB = Rect(back.left + 0.18f * u, back.top + 0.012f * u, back.right - 0.012f * u, back.bottom - 0.014f * u)
    for ((i, r) in listOf(scrA, scrB).withIndex()) {
        fxGlow(r.center, 0.1f * u, FxC.auroraGreen, 0.08f + 0.2f * pen.night)
        clipRect(r.left, r.top, r.right, r.bottom) { fxPanelScreen((f.mode + i * 4) % 8, r, u, pen) }
        drawRect(Ink.line, r.topLeft, r.size, style = pen.thin)
        drawLine(Color.White.copy(alpha = 0.25f), Offset(r.left + 0.006f * u, r.top + 0.004f * u), Offset(r.left + 0.03f * u, r.top + 0.004f * u), pen.lw)
    }
    // The desk.
    fxBox(u, -0.18f, -0.2f, 0.18f, 0f, d, body, pen, rad = 0.006f, top = body.lighten(0.2f))
    fxFace(fxFlat(u, -0.15f, 0.15f, -0.2f, 0.02f, 0.1f, 0.006f), Color(0xFF3C4A66), pen)
    val cols = arrayOf(FxC.red, FxC.yellow, FxC.green, FxC.sky, FxC.flame2, FxC.lilac)
    for (k in 0 until 12) {
        val c = q(-0.13f + (k % 6) * 0.034f, -0.2f, 0.035f + (k / 6) * 0.035f)
        val lit = fxFrac(t * (0.6f + (k % 3) * 0.3f) + k * 0.37f) < 0.55f
        val col = cols[k % cols.size]
        if (lit) fxGlow(c, 0.012f * u, col, 0.6f)
        drawPath(fxDisc(c.x, c.y, 0.008f * u), if (lit) col.lighten(0.3f) else col.darken(0.35f))
        drawPath(fxDisc(c.x, c.y, 0.008f * u), Ink.line, style = pen.thin)
    }
    for (k in 0 until 2) {
        val on = (f.mode shr k) and 1 == 1
        val base = q(0.09f + k * 0.03f, -0.2f, 0.05f)
        val slot = q(0.09f + k * 0.03f, -0.2f, 0.09f)
        fxLine(base, slot, Ink.line, pen.lw * 1.5f)
        val knob = fxMix(base, slot, if (on) 0.85f else 0.15f)
        capsule(knob, Offset(knob.x, knob.y - 0.022f * u), 0.004f * u, FxC.steel, pen)
        inkedCircle(Offset(knob.x, knob.y - 0.024f * u), 0.006f * u, if (k == 0) FxC.red else FxC.sky, pen)
    }
    val big = q(0.15f, -0.2f, 0.12f)
    fxCyl(big.x, big.y, big.y - 0.008f * u, 0.016f * u, 0.016f * u, FxC.charcoal, pen)
    fxCloud(FxC.red, pen, true, big.x, big.y - 0.012f * u, 0.012f * u)
    shine(Offset(big.x - 0.004f * u, big.y - 0.017f * u), 0.006f * u, 0.004f * u, 0.8f)
    // Vents and a stripe on the front.
    drawRect(FxC.flame2, p(-0.18f, -0.16f), Size(0.36f * u, 0.01f * u))
    for (k in 0 until 5) fxLine(p(-0.14f, -0.12f + k * 0.018f), p(-0.06f, -0.12f + k * 0.018f), FxC.charcoal.copy(alpha = 0.5f), pen.lw * 0.8f)
    drawRoundRect(Ink.line.copy(alpha = 0.5f), p(0.02f, -0.13f), Size(0.13f * u, 0.11f * u), androidx.compose.ui.geometry.CornerRadius(0.006f * u), style = pen.thin)
    for (k in 0 until 3) {
        val on = fxFrac(t * 0.5f + k * 0.33f) < 0.3f
        drawCircle(if (on) FxC.green else FxC.green.darken(0.5f), 0.003f * u, p(0.12f + k * 0.012f, -0.115f))
    }
}

/** A real body seen through the porthole: Earth, Moon, Mars, Jupiter, Saturn or the Sun. */
private fun DrawScope.fxPlanetView(mode: Int, c: Offset, r: Float, u: Float, pen: Pen) {
    val t = pen.t
    when (mode) {
        0 -> {
            val e = Offset(c.x + r * 0.15f, c.y + r * 0.05f)
            val er = r * 0.72f
            fxGlow(e, er * 1.25f, Color(0xFF7FD4FF), 0.5f)
            drawCircle(Color(0xFF2F7FD8), er, e)
            clipPath(ovalPath(Rect(e, er))) {
                val drift = fxFrac(t * 0.01f) * er * 2f
                for (k in -1..1) {
                    val ox = e.x - er + drift + k * er * 2f
                    drawPath(blobPath(ox - er * 0.4f, e.y - er * 0.5f, ox + er * 0.1f, e.y - er * 0.65f, ox + er * 0.3f, e.y - er * 0.1f, ox - er * 0.1f, e.y + er * 0.4f, ox - er * 0.5f, e.y + er * 0.1f), Color(0xFF5DBB63))
                    drawPath(blobPath(ox + er * 0.5f, e.y + er * 0.2f, ox + er * 0.85f, e.y + er * 0.1f, ox + er * 0.9f, e.y + er * 0.6f, ox + er * 0.55f, e.y + er * 0.7f), Color(0xFFC9A96A))
                }
                for (k in 0 until 3) drawArc(Color.White.copy(alpha = 0.85f), 200f + k * 40f, 80f, false, Offset(e.x - er * (0.8f - k * 0.2f), e.y - er * (0.6f - k * 0.3f)), Size(er * 1.2f, er * 0.5f), style = Stroke(r * 0.07f, cap = StrokeCap.Round))
                drawCircle(Color(0xFF0A1024).copy(alpha = 0.5f), er * 1.1f, Offset(e.x + er * 0.75f, e.y + er * 0.3f))
            }
            drawCircle(Color(0xFF9FE4FF), er, e, style = Stroke(pen.lw))
        }
        1 -> {
            val m = Offset(c.x + r * 0.1f, c.y)
            val mr = r * 0.7f
            drawCircle(Color(0xFFC6C8CE), mr, m)
            clipPath(ovalPath(Rect(m, mr))) {
                drawPath(blobPath(m.x - mr * 0.5f, m.y - mr * 0.3f, m.x, m.y - mr * 0.45f, m.x + mr * 0.2f, m.y - mr * 0.1f, m.x - mr * 0.3f, m.y + mr * 0.1f), Color(0xFF9EA2AC))
                val craters = floatArrayOf(-0.3f, 0.35f, 0.14f, 0.25f, 0.3f, 0.1f, -0.1f, -0.1f, 0.09f, 0.45f, -0.35f, 0.08f, 0.05f, 0.55f, 0.07f)
                for (k in 0 until 5) {
                    val cc = Offset(m.x + craters[k * 3] * mr, m.y + craters[k * 3 + 1] * mr)
                    val cr = craters[k * 3 + 2] * mr
                    drawCircle(Color(0xFF9EA2AC), cr, cc)
                    drawArc(Color.White.copy(alpha = 0.6f), 30f, 150f, false, Offset(cc.x - cr, cc.y - cr), Size(cr * 2f, cr * 2f), style = Stroke(pen.lw * 0.6f))
                }
                drawCircle(Color(0xFF0A1024).copy(alpha = 0.55f), mr * 1.1f, Offset(m.x + mr * 0.8f, m.y + mr * 0.2f))
            }
        }
        2 -> {
            val m = Offset(c.x + r * 0.1f, c.y + r * 0.05f)
            val mr = r * 0.62f
            fxGlow(m, mr * 1.2f, Color(0xFFFFB38A), 0.3f)
            drawCircle(Color(0xFFD9653B), mr, m)
            clipPath(ovalPath(Rect(m, mr))) {
                drawPath(blobPath(m.x - mr * 0.6f, m.y, m.x - mr * 0.1f, m.y - mr * 0.2f, m.x + mr * 0.5f, m.y + mr * 0.1f, m.x, m.y + mr * 0.35f), Color(0xFFA8452A))
                drawOval(Color.White, Offset(m.x - mr * 0.45f, m.y - mr * 1.05f), Size(mr * 0.9f, mr * 0.35f))
                drawCircle(Color(0xFF0A1024).copy(alpha = 0.45f), mr * 1.1f, Offset(m.x + mr * 0.85f, m.y + mr * 0.2f))
            }
        }
        3 -> {
            val j = Offset(c.x + r * 0.25f, c.y + r * 0.1f)
            val jr = r * 0.95f
            drawCircle(Color(0xFFE8D2B0), jr, j)
            clipPath(ovalPath(Rect(j, jr))) {
                val bands = arrayOf(Color(0xFFC99A6B), Color(0xFFE8D2B0), Color(0xFFB57E52), Color(0xFFF0DEC0), Color(0xFFC99A6B), Color(0xFFE0C49C))
                for (k in bands.indices) {
                    val y = j.y - jr + k * jr * 2f / bands.size
                    val wave = Path().apply {
                        moveTo(j.x - jr, y)
                        quadraticTo(j.x, y + sin(t * 0.3f + k) * jr * 0.05f, j.x + jr, y)
                        lineTo(j.x + jr, y + jr * 0.2f)
                        lineTo(j.x - jr, y + jr * 0.2f)
                        close()
                    }
                    drawPath(wave, bands[k])
                }
                drawOval(Color(0xFFC9553A), Offset(j.x - jr * 0.1f, j.y + jr * 0.25f), Size(jr * 0.34f, jr * 0.18f))
                drawCircle(Color(0xFF0A1024).copy(alpha = 0.4f), jr * 1.1f, Offset(j.x + jr * 0.85f, j.y + jr * 0.1f))
            }
        }
        4 -> {
            val s = Offset(c.x + r * 0.05f, c.y)
            val sr = r * 0.42f
            rotate(-14f, s) {
                val ring = Rect(s.x - sr * 2.1f, s.y - sr * 0.55f, s.x + sr * 2.1f, s.y + sr * 0.55f)
                drawArc(Color(0xFFD9C08A), 180f, 180f, false, ring.topLeft, ring.size, style = Stroke(sr * 0.28f))
                drawArc(Color(0xFFB09868), 180f, 180f, false, Offset(ring.left + sr * 0.3f, ring.top + sr * 0.1f), Size(ring.width - sr * 0.6f, ring.height - sr * 0.2f), style = Stroke(sr * 0.1f))
                drawCircle(Color(0xFFE8CF8F), sr, s)
                clipPath(ovalPath(Rect(s, sr))) {
                    for (k in 0 until 3) drawRect(Color(0xFFD4B478), Offset(s.x - sr, s.y - sr * 0.5f + k * sr * 0.45f), Size(sr * 2f, sr * 0.12f))
                    drawCircle(Color(0xFF0A1024).copy(alpha = 0.35f), sr * 1.1f, Offset(s.x + sr * 0.8f, s.y + sr * 0.2f))
                }
                drawArc(Color(0xFFD9C08A), 0f, 180f, false, ring.topLeft, ring.size, style = Stroke(sr * 0.28f))
                drawArc(Color(0xFFB09868), 0f, 180f, false, Offset(ring.left + sr * 0.3f, ring.top + sr * 0.1f), Size(ring.width - sr * 0.6f, ring.height - sr * 0.2f), style = Stroke(sr * 0.1f))
            }
        }
        else -> {
            val s = Offset(c.x + r * 0.2f, c.y + r * 0.1f)
            val sr = r * 0.75f
            fxGlow(s, sr * 1.6f, FxC.flame2, 0.9f)
            drawCircle(safeRadialGradient(0f to Color(0xFFFFF2A8), 0.6f to Color(0xFFFFB02E), 1f to Color(0xFFFF7A1E), center = s, radius = sr), sr, s)
            for (k in 0 until 6) drawCircle(Color(0xFFFF8A2E).copy(alpha = 0.6f), sr * 0.08f, Offset(s.x + cos(k * 1.9f) * sr * 0.55f, s.y + sin(k * 2.3f) * sr * 0.5f))
            for (k in 0 until 3) {
                val a = k * 2.1f + t * 0.2f
                val e = Offset(s.x + cos(a) * sr, s.y + sin(a) * sr)
                val h = sr * (0.18f + 0.06f * sin(t * 2f + k))
                drawArc(FxC.flame1, a * 57.3f + 60f, 180f, false, Offset(e.x - h, e.y - h), Size(h * 2f, h * 2f), style = Stroke(r * 0.05f, cap = StrokeCap.Round))
            }
        }
    }
}

internal fun DrawScope.fxPorthole(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val frame = if (f.variant == 1) Color(0xFFE0934A) else Color(0xFF9AA6BC)
    val c = Offset(0f, -0.15f * u)
    val rr = 0.115f * u
    val o = Oblique.offset(0.05f, u)
    // A thick bolted ring set into the wall.
    drawCircle(frame.darken(0.3f), 0.15f * u, Offset(c.x + o.x * 0.3f, c.y + o.y * 0.3f))
    inkedCircle(c, 0.15f * u, frame, pen)
    for (k in 0 until 8) {
        val a = k * FX_PI / 4f + FX_PI / 8f
        fxBolt(Offset(c.x + cos(a) * 0.134f * u, c.y + sin(a) * 0.134f * u), 0.005f * u, pen)
    }
    val glass = ovalPath(Rect(c, rr))
    clipPath(glass) {
        drawRect(frame.darken(0.5f), Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f))
        translate(o.x, o.y) {
            val view = ovalPath(Rect(c, rr))
            clipPath(view) {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF070B1E), Color(0xFF1C1640)), c.y - rr, c.y + rr), Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f))
                for (k in 0 until 14) {
                    val sx = c.x - rr + fxFrac(k * 0.137f + t * 0.004f) * rr * 2f
                    val sy = c.y - rr + fxFrac(k * 0.611f) * rr * 2f
                    drawCircle(Color.White.copy(alpha = 0.5f + 0.4f * sin(t * 2f + k)), (0.0012f + (k % 3) * 0.0006f) * u, Offset(sx, sy))
                }
                fxPlanetView(f.mode.mod(6), c, rr, u, pen)
            }
            drawPath(view, Ink.line, style = pen.thin)
        }
        drawArc(Color.White.copy(alpha = 0.3f), 200f, 60f, false, Offset(c.x - rr * 0.8f, c.y - rr * 0.8f), Size(rr * 1.6f, rr * 1.6f), style = Stroke(0.005f * u, cap = StrokeCap.Round))
    }
    drawCircle(Ink.line, rr, c, style = pen.stroke)
    drawCircle(frame.lighten(0.3f), rr + 0.006f * u, c, style = Stroke(pen.lw))
}

internal fun DrawScope.fxGravityLever(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val base = Color(0xFF5A6B8C)
    val d = 0.08f
    // The lever swings on a hub on top of the base: up when gravity is on.
    val hub = q(0f, -0.085f, d * 0.5f)
    val target = if (f.on) 15f else 118f
    val other = if (f.on) 118f else 15f
    val ang = target + (other - target) * f.anim * f.anim
    rotate(ang, hub) {
        capsule(hub, Offset(hub.x, hub.y - 0.12f * u), 0.012f * u, FxC.steel, pen)
        for (k in 0 until 3) {
            val y = hub.y - (0.02f + k * 0.012f) * u
            drawLine(if (k % 2 == 0) FxC.yellow else FxC.charcoal, Offset(hub.x - 0.006f * u, y), Offset(hub.x + 0.006f * u, y - 0.004f * u), 0.004f * u)
        }
        inkedCircle(Offset(hub.x, hub.y - 0.125f * u), 0.017f * u, FxC.red, pen)
        shine(Offset(hub.x - 0.006f * u, hub.y - 0.131f * u), 0.007f * u, 0.005f * u, 0.8f)
    }
    fxBox(u, -0.045f, -0.085f, 0.045f, 0f, d, base, pen, rad = 0.01f)
    fxCyl(hub.x, hub.y, hub.y - 0.01f * u, 0.016f * u, 0.016f * u, FxC.steel, pen)
    inkedRound(Rect(-0.035f * u, -0.07f * u, 0.035f * u, -0.015f * u), 0.006f * u, base.lighten(0.25f), pen, shade = false)
    val apple = p(0f, -0.04f)
    inkedCircle(apple, 0.013f * u, FxC.red, pen)
    fxLine(Offset(apple.x, apple.y - 0.012f * u), Offset(apple.x + 0.003f * u, apple.y - 0.02f * u), FxC.walnut, pen.lw)
    drawPath(fxLeaf(apple.x + 0.003f * u, apple.y - 0.017f * u, apple.x + 0.013f * u, apple.y - 0.022f * u, 0.3f), FxC.green)
    shine(Offset(apple.x - 0.005f * u, apple.y - 0.004f * u), 0.004f * u, 0.003f * u, 0.8f)
    val led = q(0.03f, -0.085f, 0.015f)
    val col = if (f.on) FxC.green else FxC.sky
    fxGlow(led, 0.014f * u, col, 0.6f + 0.2f * sin(t * 4f))
    drawPath(fxDisc(led.x, led.y, 0.005f * u), col)
    for (k in 0 until 4) {
        val x = if (k % 2 == 0) -0.04f else 0.04f
        val y = if (k < 2) -0.078f else -0.007f
        fxBolt(p(x, y), 0.0025f * u, pen)
    }
}

private val ORRERY_SPEED = floatArrayOf(3.0f, 2.2f, 1.8f, 1.4f, 0.9f, 0.7f, 0.5f, 0.4f)
private val ORRERY_SIZE = floatArrayOf(0.006f, 0.008f, 0.0085f, 0.007f, 0.016f, 0.013f, 0.01f, 0.01f)
private val ORRERY_COLOR = arrayOf(
    Color(0xFFA9A9B0), Color(0xFFE8D29A), Color(0xFF3D8FE0), Color(0xFFD9653B),
    Color(0xFFD9B07A), Color(0xFFE8CF8F), Color(0xFF8FE0E8), Color(0xFF3D6BFF),
)

internal fun DrawScope.fxOrrery(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val cz = 0.12f
    val brass = FxC.brass
    fxBox(u, -0.06f, -0.03f, 0.06f, 0f, 0.1f, FxC.walnut, pen, rad = 0.006f, z = cz - 0.05f)
    val gear = fxQ(u, 0f, -0.015f, cz - 0.05f)
    drawCircle(brass, 0.01f * u, gear, style = Stroke(0.003f * u))
    for (k in 0 until 8) {
        val a = k * FX_PI / 4f + f.angle
        drawLine(brass, Offset(gear.x + cos(a) * 0.01f * u, gear.y + sin(a) * 0.01f * u), Offset(gear.x + cos(a) * 0.014f * u, gear.y + sin(a) * 0.014f * u), 0.003f * u)
    }
    val c = q(0f, -0.2f, cz)
    val base = q(0f, -0.03f, cz)
    fxCyl(base.x, base.y, c.y, 0.007f * u, 0.007f * u, brass, pen, cap = false)
    for (i in 0 until 8) drawPath(fxDisc(c.x, c.y, (0.028f + i * 0.0165f) * u), brass.copy(alpha = 0.7f), style = Stroke(pen.lw * 0.7f))
    fun planet(i: Int, back: Boolean) {
        val a = f.angle * ORRERY_SPEED[i] + i * 0.9f
        if ((sin(a) > 0f) != back) return
        val rim = fxRim(c.x, c.y, (0.028f + i * 0.0165f) * u, a)
        drawLine(brass, c, rim, pen.lw * 0.7f)
        val pc = Offset(rim.x, rim.y - 0.014f * u)
        drawLine(brass, rim, pc, pen.lw * 0.6f)
        val r = ORRERY_SIZE[i] * u
        if (i == 5) drawOval(Color(0xFFD9C08A), Offset(pc.x - r * 2.1f, pc.y - r * 0.6f), Size(r * 4.2f, r * 1.2f), style = Stroke(r * 0.35f))
        inkedCircle(pc, r, ORRERY_COLOR[i], pen)
        if (i == 2) drawCircle(FxC.green, r * 0.4f, Offset(pc.x - r * 0.2f, pc.y - r * 0.1f))
        if (i == 4) {
            drawLine(ORRERY_COLOR[i].darken(0.25f), Offset(pc.x - r * 0.9f, pc.y - r * 0.2f), Offset(pc.x + r * 0.9f, pc.y - r * 0.2f), r * 0.18f)
            drawLine(ORRERY_COLOR[i].darken(0.25f), Offset(pc.x - r * 0.8f, pc.y + r * 0.35f), Offset(pc.x + r * 0.8f, pc.y + r * 0.35f), r * 0.14f)
        }
        if (i == 5) drawArc(Color(0xFFD9C08A), 0f, 180f, false, Offset(pc.x - r * 2.1f, pc.y - r * 0.6f), Size(r * 4.2f, r * 1.2f), style = Stroke(r * 0.35f))
    }
    for (i in 7 downTo 0) planet(i, true)
    val sun = Offset(c.x, c.y - 0.012f * u)
    fxGlow(sun, 0.09f * u, FxC.yellow, 0.35f + 0.35f * pen.night + 0.3f * min(1f, f.timer))
    inkedCircle(sun, 0.024f * u, Color(0xFFFFD84A), pen)
    shine(Offset(sun.x - 0.008f * u, sun.y - 0.009f * u), 0.008f * u, 0.006f * u, 0.7f)
    for (i in 0 until 8) planet(i, false)
    if (f.timer > 0f) {
        for (k in 0 until 3) twinkle(Offset(c.x + cos(pen.t * 2f + k * 2.1f) * 0.12f * u, c.y - 0.03f * u + sin(pen.t * 2f + k * 2.1f) * 0.04f * u), 0.008f * u, FxC.yellow, min(1f, f.timer))
    }
}

internal fun DrawScope.fxSpaceBed(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val bag = FxC.flame2
    fxBox(u, -0.07f, -0.32f, 0.07f, 0f, 0.02f, Color(0xFFC9D2E0), pen, rad = 0.02f, z = 0.03f)
    val panel = fxFront(u, -0.07f, -0.32f, 0.07f, 0f, 0.03f)
    for (k in 0 until 4) fxBolt(Offset(if (k % 2 == 0) panel.left + 0.01f * u else panel.right - 0.01f * u, if (k < 2) panel.top + 0.01f * u else panel.bottom - 0.01f * u), 0.003f * u, pen)
    val lamp = q(0.05f, -0.31f, 0.02f)
    fxGlow(lamp, 0.04f * u, FxC.warm, 0.2f + 0.6f * pen.night)
    inkedRound(Rect(lamp.x - 0.01f * u, lamp.y - 0.006f * u, lamp.x + 0.01f * u, lamp.y + 0.006f * u), 0.003f * u, FxC.warm, pen, shade = false)
    fxBox(u, -0.05f, -0.3f, 0.05f, -0.235f, 0.03f, Color.White, pen, rad = 0.02f, z = 0.004f)
    fxBox(u, -0.055f, -0.245f, 0.055f, -0.012f, 0.035f, bag, pen, rad = 0.035f, z = -0.008f)
    val front = fxFront(u, -0.055f, -0.245f, 0.055f, -0.012f, -0.008f)
    clipPath(roundPath(front, 0.035f * u)) {
        for (k in 1..5) {
            val y = front.top + front.height * k / 6f
            val seam = Path().apply {
                moveTo(front.left, y)
                quadraticTo(front.center.x, y + 0.006f * u, front.right, y)
            }
            drawPath(seam, bag.darken(0.25f), style = pen.thin)
        }
        drawRect(bag.lighten(0.3f), front.topLeft, Size(front.width, 0.025f * u))
        for (k in 0 until 3) twinkle(Offset(front.left + (0.02f + k * 0.035f) * u, front.top + (0.08f + (k % 2) * 0.07f) * u), 0.006f * u, Color.White, 0.8f)
        var y = front.top + 0.03f * u
        while (y < front.bottom - 0.01f * u) {
            drawLine(FxC.charcoal, Offset(front.right - 0.012f * u, y), Offset(front.right - 0.012f * u, y + 0.004f * u), pen.lw)
            y += 0.008f * u
        }
    }
    for (y in floatArrayOf(-0.18f, -0.065f)) {
        fxBox(u, -0.068f, y - 0.008f, 0.068f, y + 0.008f, 0.05f, FxC.charcoal, pen, rad = 0.002f, z = -0.012f)
        val buckle = fxFront(u, -0.01f, y - 0.01f, 0.01f, y + 0.01f, -0.012f)
        inkedRound(buckle, 0.002f * u, FxC.steel, pen, shade = false)
    }
}

internal fun DrawScope.fxFoodDispenser(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val white = Color(0xFFEEF1F6)
    val d = 0.12f
    fxBox(u, -0.08f, -0.28f, 0.08f, 0f, d, white, pen, rad = 0.016f)
    drawRect(FxC.flame2, p(-0.08f, -0.25f), Size(0.012f * u, 0.22f * u))
    // The window full of food packs on spiral shelves.
    fxHollow(u, -0.058f, -0.262f, 0.045f, -0.135f, d - 0.03f, Color(0xFF2A3A5A), pen, back = Color(0xFF1C2A44))
    clipRect(-0.058f * u, -0.262f * u, 0.045f * u, -0.135f * u) {
        val packs = arrayOf(FxC.flame2, FxC.green, FxC.pink)
        for (row in 0 until 2) {
            val y = -0.2f + row * 0.062f
            fxInShelf(u, -0.058f, 0.045f, y, d - 0.03f, FxC.steel, pen, 0.004f)
            for (k in 0 until 3) {
                val x = -0.05f + k * 0.032f
                fxBox(u, x, y - 0.04f, x + 0.024f, y, 0.02f, packs[(k + row) % 3], pen, rad = 0.004f, z = 0.02f)
                val lab = fxFront(u, x + 0.004f, y - 0.03f, x + 0.02f, y - 0.018f, 0.02f)
                drawRect(Color.White.copy(alpha = 0.85f), lab.topLeft, lab.size)
            }
        }
    }
    val glint = Path().apply {
        moveTo(-0.05f * u, -0.14f * u)
        lineTo(-0.035f * u, -0.14f * u)
        lineTo(0.0f, -0.26f * u)
        lineTo(-0.015f * u, -0.26f * u)
        close()
    }
    drawPath(glint, Color.White.copy(alpha = 0.18f))
    // Buttons and a little screen.
    inkedRound(Rect(0.052f * u, -0.26f * u, 0.072f * u, -0.235f * u), 0.003f * u, Color(0xFF0E1A2E), pen, shade = false)
    drawCircle(FxC.auroraGreen.copy(alpha = 0.6f + 0.4f * sin(t * 3f)), 0.003f * u, p(0.062f, -0.2475f))
    for (k in 0 until 3) inkedCircle(p(0.062f, -0.22f + k * 0.022f), 0.006f * u, arrayOf(FxC.flame2, FxC.green, FxC.pink)[k], pen, shade = false)
    // The hatch where food comes out.
    fxHollow(u, -0.05f, -0.122f, 0.05f, -0.078f, 0.05f, Color(0xFF3A4660), pen)
    val a = f.anim
    if (a > 0f) {
        val lift = Rect(-0.05f * u, -0.122f * u, 0.05f * u, (-0.122f + 0.044f * (1f - a)) * u)
        inkedRound(lift, 0.004f * u, FxC.steel, pen, shade = false)
    } else {
        inkedRound(Rect(-0.05f * u, -0.122f * u, 0.05f * u, -0.078f * u), 0.006f * u, FxC.steel, pen)
        fxLine(p(-0.02f, -0.106f), p(0f, -0.094f), Ink.line.copy(alpha = 0.6f), pen.lw)
        fxLine(p(0f, -0.094f), p(0.02f, -0.106f), Ink.line.copy(alpha = 0.6f), pen.lw)
    }
    for (k in 0 until 2) {
        val on = fxFrac(t * 0.8f + k * 0.5f) < 0.5f
        drawCircle(if (on) FxC.red else FxC.red.darken(0.5f), 0.004f * u, p(-0.04f + k * 0.08f, -0.03f))
    }
}
