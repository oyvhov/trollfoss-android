package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import app.trollfoss.domain.PlaceId
import kotlin.math.cos
import kotlin.math.sin

// Home furniture in oblique 3D: beds, seats, tables, storage, kitchen and bathroom. Everyday
// Scandinavian: light oak, white paint, soft sage and dusty blue, a knitted throw here and there.

// ------------------------------------------------------------------------------------------ bed

private const val BED_D = 0.18f

internal fun DrawScope.fxBed(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = BED_D
    val frame = FxC.oak
    val board = FxC.dusty
    fxBox(u, 0.172f, -0.034f, 0.19f, 0f, 0.018f, frame.darken(0.08f), pen, z = d - 0.02f)
    // The headboard: a painted panel across the head end with an arched top.
    val strip = Path().apply {
        val a = q(-0.205f, -0.165f, 0f)
        val c = q(-0.205f, -0.235f, d / 2f)
        val e = q(-0.205f, -0.165f, d)
        val g = q(-0.18f, -0.165f, d)
        val i = q(-0.18f, -0.235f, d / 2f)
        val k = q(-0.18f, -0.165f, 0f)
        moveTo(a.x, a.y)
        quadraticTo(c.x, c.y, e.x, e.y)
        lineTo(g.x, g.y)
        quadraticTo(i.x, i.y, k.x, k.y)
        close()
    }
    fxFace(strip, board.lighten(0.2f), pen)
    val inner = Path().apply {
        val a = q(-0.18f, -0.03f, 0f)
        val b = q(-0.18f, -0.165f, 0f)
        val c = q(-0.18f, -0.235f, d / 2f)
        val e = q(-0.18f, -0.165f, d)
        val g = q(-0.18f, -0.03f, d)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        quadraticTo(c.x, c.y, e.x, e.y)
        lineTo(g.x, g.y)
        close()
    }
    fxFace(inner, board.darken(0.06f), pen)
    val band = Path().apply {
        val a = q(-0.18f, -0.13f, 0.012f)
        val b = q(-0.18f, -0.13f, d - 0.012f)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
    }
    drawPath(band, board.lighten(0.35f), style = Stroke(0.005f * u, cap = StrokeCap.Round))
    val hc = q(-0.18f, -0.165f, d / 2f)
    val heart = fxHeart(hc.x, hc.y, 0.011f * u)
    drawPath(heart, board.lighten(0.45f))
    drawPath(heart, Ink.line, style = pen.thin)
    inkedRound(Rect(-0.205f * u, -0.165f * u, -0.18f * u, 0f), 0.005f * u, board, pen)
    // Frame, mattress, pillow.
    fxBox(u, -0.18f, -0.088f, 0.19f, -0.03f, d, frame, pen, rad = 0.005f)
    fxGrain(Rect(-0.17f * u, -0.08f * u, 0.18f * u, -0.038f * u), frame, pen, 2)
    fxBox(u, -0.18f, -0.108f, 0.19f, -0.086f, d - 0.006f, FxC.porcelain, pen, rad = 0.008f)
    var x = -0.165f
    while (x < 0.18f) {
        fxLine(p(x, -0.103f), p(x, -0.09f), FxC.dusty.copy(alpha = 0.35f), pen.lw * 0.5f)
        x += 0.02f
    }
    fxBox(u, -0.172f, -0.15f, -0.07f, -0.108f, 0.12f, FxC.porcelain, pen, rad = 0.018f, z = 0.03f)
    fxLine(q(-0.158f, -0.126f, 0.03f), q(-0.084f, -0.128f, 0.03f), FxC.blush, 0.004f * u)
    fxBox(u, 0.172f, -0.034f, 0.19f, 0f, 0.018f, frame.darken(0.08f), pen)
}

/**
 * The duvet over the sleeper, as a thick soft quilt lying on the mattress: head and shoulders stay out
 * on the pillow side, the rest is tucked in under it.
 */
internal fun DrawScope.fxBedFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = BED_D - 0.012f
    val x0 = 0.036f
    val x1 = 0.198f
    val top = -0.19f
    val cover = FxC.dusty.lighten(0.15f)
    // Right side and top of the quilt.
    val s0 = q(x1, top, 0f)
    val s1 = q(x1, top, d)
    val s2 = q(x1, -0.108f, d)
    val s3 = q(x1, -0.046f, 0f)
    fxFace(fxQuad(s0.x, s0.y, s1.x, s1.y, s2.x, s2.y, s3.x, s3.y, 0.012f * u), cover.darken(0.2f), pen)
    val topFace = fxFlat(u, x0, x1, top, 0f, d, 0.022f)
    drawPath(topFace, cover.lighten(0.14f))
    clipPath(topFace) {
        for (j in 0 until 4) {
            for (i in 0 until 5) {
                val c = q(0.06f + i * 0.032f + (if (j % 2 == 0) 0f else 0.016f), top, 0.025f + j * 0.04f)
                drawCircle(Color.White.copy(alpha = 0.8f), 0.004f * u, c)
            }
        }
        fxFace(fxFlat(u, 0.148f, x1 + 0.01f, top - 0.002f, -0.01f, d + 0.01f), FxC.mustard.lighten(0.12f), pen)
        for (k in 0 until 5) fxLine(q(0.156f + k * 0.009f, top, 0f), q(0.156f + k * 0.009f, top, d), FxC.mustard.darken(0.2f), pen.lw * 0.5f)
        fxFace(fxFlat(u, x0 - 0.004f, x0 + 0.028f, top - 0.002f, -0.01f, d + 0.01f), FxC.porcelain, pen)
    }
    drawPath(topFace, Ink.line, style = pen.stroke)
    // The front of the quilt hanging over the edge of the bed.
    val front = Path().apply {
        moveTo(x0 * u, (top + 0.012f) * u)
        quadraticTo(x0 * u, top * u, (x0 + 0.02f) * u, top * u)
        lineTo((x1 - 0.02f) * u, top * u)
        quadraticTo(x1 * u, top * u, x1 * u, (top + 0.02f) * u)
        lineTo((x1 + 0.002f) * u, -0.056f * u)
        quadraticTo(x1 * u, -0.046f * u, (x1 - 0.012f) * u, -0.046f * u)
        quadraticTo((x0 + x1) / 2f * u, -0.039f * u, (x0 + 0.02f) * u, -0.048f * u)
        quadraticTo((x0 - 0.004f) * u, -0.05f * u, (x0 - 0.005f) * u, -0.07f * u)
        close()
    }
    inked(front, cover, pen, outline = false)
    clipPath(front) {
        for (j in 0 until 4) {
            for (i in 0 until 5) {
                val dx = 0.058f + i * 0.032f + (if (j % 2 == 0) 0f else 0.016f)
                drawCircle(Color.White.copy(alpha = 0.8f), 0.0042f * u, p(dx, -0.172f + j * 0.036f))
            }
        }
        val seam = Path().apply {
            moveTo(0.1f * u, -0.2f * u)
            quadraticTo(0.094f * u, -0.12f * u, 0.1f * u, -0.03f * u)
        }
        drawPath(seam, cover.darken(0.25f), style = pen.thin)
        val throwRect = Rect(0.148f * u, -0.2f * u, 0.215f * u, -0.03f * u)
        drawRect(FxC.mustard, throwRect.topLeft, throwRect.size)
        fxKnit(throwRect, FxC.mustard.darken(0.25f), 3, 8, pen.lw * 0.5f)
        drawLine(Ink.line, p(0.148f, -0.2f), p(0.148f, -0.03f), pen.lw * 0.7f)
        drawRect(FxC.porcelain, p(x0 - 0.01f, -0.2f), Size(0.038f * u, 0.17f * u))
        drawLine(Ink.line, p(x0 + 0.028f, -0.2f), p(x0 + 0.026f, -0.03f), pen.lw * 0.7f)
        fxLine(p(x0 + 0.018f, -0.19f), p(x0 + 0.016f, -0.05f), FxC.blush, 0.003f * u)
    }
    drawPath(front, Ink.line, style = pen.stroke)
}

// ------------------------------------------------------------------------------------------ seats

internal fun DrawScope.fxSofa(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.17f
    val fabric = if (f.place == PlaceId.SALON) FxC.lilac else FxC.sage
    for (s in 0..1) {
        val x = if (s == 0) -0.17f else 0.17f
        fxBox(u, x - 0.008f, -0.022f, x + 0.008f, 0f, 0.016f, FxC.oak, pen, z = d - 0.022f)
    }
    fxBox(u, -0.2f, -0.18f, 0.2f, -0.045f, 0.052f, fabric.darken(0.06f), pen, rad = 0.022f, z = d - 0.052f)
    val bz = d - 0.092f
    fxBox(u, -0.172f, -0.165f, -0.004f, -0.072f, 0.04f, fabric, pen, rad = 0.02f, z = bz)
    fxBox(u, 0.004f, -0.165f, 0.172f, -0.072f, 0.04f, fabric, pen, rad = 0.02f, z = bz)
    for (s in 0..1) {
        val x = if (s == 0) -0.088f else 0.088f
        drawCircle(fabric.darken(0.3f), 0.004f * u, q(x, -0.12f, bz))
        fxLine(q(x - 0.02f, -0.126f, bz), q(x - 0.004f, -0.121f, bz), fabric.darken(0.2f), pen.lw * 0.5f)
        fxLine(q(x + 0.02f, -0.126f, bz), q(x + 0.004f, -0.121f, bz), fabric.darken(0.2f), pen.lw * 0.5f)
    }
    // A knitted throw over the right back cushion.
    val tz = bz - 0.003f
    val drape = Path().apply {
        val a = q(0.04f, -0.169f, tz)
        val b = q(0.15f, -0.169f, tz)
        val c = q(0.152f, -0.1f, tz)
        val e = q(0.124f, -0.09f, tz)
        val g = q(0.097f, -0.098f, tz)
        val i = q(0.07f, -0.106f, tz)
        val k = q(0.043f, -0.097f, tz)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        quadraticTo(e.x, e.y, g.x, g.y)
        quadraticTo(i.x, i.y, k.x, k.y)
        close()
    }
    fxFace(fxFlat(u, 0.04f, 0.15f, -0.169f, tz, tz + 0.046f, 0.004f), FxC.mustard.lighten(0.12f), pen)
    inked(drape, FxC.mustard, pen, outline = false)
    clipPath(drape) {
        val o = q(0f, 0f, tz)
        translate(o.x, o.y) {
            fxKnit(Rect(0.04f * u, -0.17f * u, 0.156f * u, -0.09f * u), FxC.mustard.darken(0.22f), 7, 5, pen.lw * 0.5f)
            drawLine(FxC.cream, Offset(0.03f * u, -0.148f * u), Offset(0.16f * u, -0.148f * u), 0.006f * u)
            drawLine(FxC.cream, Offset(0.03f * u, -0.118f * u), Offset(0.16f * u, -0.118f * u), 0.006f * u)
        }
    }
    drawPath(drape, Ink.line, style = pen.stroke)
    for (i in 0 until 6) {
        val fx = 0.05f + i * 0.019f
        fxLine(q(fx, -0.099f + (i % 2) * 0.004f, tz), q(fx, -0.088f + (i % 2) * 0.004f, tz), FxC.mustard.darken(0.3f), pen.lw * 0.6f)
    }
    // Arms, base and seat cushions.
    fxBox(u, -0.205f, -0.118f, -0.162f, -0.02f, d, fabric.darken(0.02f), pen, rad = 0.018f)
    fxBox(u, -0.162f, -0.05f, 0.162f, -0.02f, d - 0.02f, fabric.darken(0.12f), pen, rad = 0.008f)
    fxBox(u, -0.162f, -0.075f, -0.002f, -0.047f, d - 0.07f, fabric.lighten(0.06f), pen, rad = 0.012f)
    fxBox(u, 0.002f, -0.075f, 0.162f, -0.047f, d - 0.07f, fabric.lighten(0.06f), pen, rad = 0.012f)
    fxBox(u, -0.158f, -0.126f, -0.114f, -0.075f, 0.028f, FxC.blush, pen, rad = 0.014f, z = 0.05f)
    fxLine(q(-0.15f, -0.103f, 0.05f), q(-0.122f, -0.1f, 0.05f), Color.White.copy(alpha = 0.7f), 0.004f * u)
    fxBox(u, 0.162f, -0.118f, 0.205f, -0.02f, d, fabric.darken(0.02f), pen, rad = 0.018f)
    for (s in 0..1) {
        val x = if (s == 0) -0.17f else 0.17f
        fxBox(u, x - 0.009f, -0.022f, x + 0.009f, 0f, 0.016f, FxC.oak, pen)
    }
}

internal fun DrawScope.fxChair(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val cafe = f.place == PlaceId.CAFE
    val wood = if (cafe) FxC.paint else FxC.oak
    val trim = if (cafe) FxC.mint else FxC.oakDark
    val sd = 0.09f
    val bz = sd - 0.013f
    for (s in 0..1) {
        val x = if (s == 0) -0.042f else 0.042f
        fxBox(u, x - 0.0065f, -0.2f, x + 0.0065f, 0f, 0.012f, wood.darken(0.08f), pen, rad = 0.003f, z = bz)
    }
    for (s in 0..1) {
        val x = if (s == 0) -0.015f else 0.015f
        fxBox(u, x - 0.003f, -0.145f, x + 0.003f, -0.085f, 0.006f, wood.darken(0.04f), pen, z = bz + 0.003f)
    }
    fxBox(u, -0.047f, -0.2f, 0.047f, -0.142f, 0.01f, wood, pen, rad = 0.016f, z = bz - 0.004f)
    val c = q(0f, -0.169f, bz - 0.004f)
    val heart = fxHeart(c.x, c.y, 0.011f * u)
    drawPath(heart, wood.darken(0.55f))
    drawPath(heart, Ink.line, style = pen.thin)
    fxBox(u, -0.044f, -0.034f, 0.044f, -0.027f, 0.006f, wood.darken(0.1f), pen, z = 0.004f)
    fxBox(u, -0.053f, -0.085f, 0.053f, -0.072f, sd, wood, pen, rad = 0.004f)
    fxLine(Offset(-0.046f * u, -0.076f * u), Offset(0.046f * u, -0.076f * u), trim, 0.003f * u)
    for (s in 0..1) {
        val x = if (s == 0) -0.044f else 0.044f
        fxBox(u, x - 0.0065f, -0.072f, x + 0.0065f, 0f, 0.012f, wood, pen, rad = 0.003f)
    }
}

/**
 * Round tops are centred on the fixture's middle on screen, half their radius back, so the surface line
 * at the front plane runs across the top.
 */
internal inline fun DrawScope.fxRoundCentred(u: Float, r: Float, block: DrawScope.() -> Unit) {
    translate(-FX_DX * r * u, -FX_DY * r * 0.5f * u) { block() }
}

internal fun DrawScope.fxStool(f: Fixture, u: Float, pen: Pen) = fxRoundCentred(u, 0.042f) { fxStoolBody(u, pen) }

private fun DrawScope.fxStoolBody(u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = FxC.oak
    val r = 0.042f
    capsule(q(0f, -0.1f, r + 0.025f), q(0f, -0.004f, r + 0.032f), 0.008f * u, wood.darken(0.2f), pen)
    capsule(q(-0.026f, -0.1f, r - 0.012f), q(-0.037f, 0f, r - 0.03f), 0.009f * u, wood, pen)
    capsule(q(0.026f, -0.1f, r - 0.012f), q(0.037f, 0f, r - 0.03f), 0.009f * u, wood, pen)
    capsule(q(-0.031f, -0.04f, r - 0.022f), q(0.031f, -0.04f, r - 0.022f), 0.004f * u, wood.darken(0.1f), pen)
    val b = q(0f, -0.103f, r)
    val t = q(0f, -0.12f, r)
    fxCyl(b.x, b.y, t.y, r * u, r * u, wood, pen)
    drawPath(fxDisc(t.x, t.y, r * u * 0.55f), wood.darken(0.2f), style = pen.thin)
}

// ------------------------------------------------------------------------------------------ tables

internal fun DrawScope.fxTable(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val lab = f.place == PlaceId.LAB
    val wood = if (lab) FxC.walnut else FxC.oak
    val d = 0.16f
    for (s in 0..1) {
        val x = if (s == 0) -0.128f else 0.128f
        fxBox(u, x - 0.009f, -0.114f, x + 0.009f, 0f, 0.018f, wood.darken(0.1f), pen, z = d - 0.026f)
    }
    fxBox(u, -0.138f, -0.116f, 0.138f, -0.1f, d - 0.03f, wood.darken(0.1f), pen, z = 0.012f)
    fxBox(u, -0.152f, -0.13f, 0.152f, -0.114f, d, wood, pen, rad = 0.004f)
    for (k in 0 until 2) fxLine(q(-0.13f, -0.13f, 0.05f + k * 0.06f), q(0.13f, -0.13f, 0.05f + k * 0.06f), wood.darken(0.25f).copy(alpha = 0.4f), pen.lw * 0.5f)
    fxGrain(Rect(-0.145f * u, -0.128f * u, 0.145f * u, -0.116f * u), wood, pen, 1)
    // A runner along the table, hanging over the front edge.
    val cloth = if (lab) Color(0xFF6A4FB3) else Color(0xFFF1E6D2)
    fxFace(fxFlat(u, -0.05f, 0.05f, -0.1305f, -0.001f, d - 0.012f), cloth.lighten(0.1f), pen)
    val hang = Rect(-0.05f * u, -0.131f * u, 0.05f * u, -0.097f * u)
    inkedRound(hang, 0.003f * u, cloth, pen, shade = false)
    if (lab) {
        twinkle(p(-0.025f, -0.112f), 0.006f * u, FxC.yellow)
        twinkle(p(0.022f, -0.118f), 0.005f * u, FxC.yellow)
        twinkle(q(0.0f, -0.13f, 0.08f), 0.006f * u, FxC.yellow)
    } else {
        fxLine(p(-0.05f, -0.107f), p(0.05f, -0.107f), FxC.dusty, 0.004f * u)
        fxLine(p(-0.05f, -0.102f), p(0.05f, -0.102f), FxC.dusty, 0.002f * u)
        fxLine(q(-0.05f, -0.1305f, 0.1f), q(0.05f, -0.1305f, 0.1f), FxC.dusty, 0.003f * u)
    }
    for (i in 0 until 9) {
        val fx = -0.044f + i * 0.011f
        fxLine(p(fx, -0.097f), p(fx, -0.091f), cloth.darken(0.25f), pen.lw * 0.5f)
    }
    for (s in 0..1) {
        val x = if (s == 0) -0.128f else 0.128f
        fxBox(u, x - 0.01f, -0.114f, x + 0.01f, 0f, 0.02f, wood.darken(0.04f), pen, z = 0.006f)
    }
}

internal fun DrawScope.fxRoundTable(f: Fixture, u: Float, pen: Pen) = fxRoundCentred(u, 0.08f) { fxRoundTableBody(f, u, pen) }

private fun DrawScope.fxRoundTableBody(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = if (f.place == PlaceId.LAB) FxC.walnut else FxC.oak
    val r = 0.08f
    val fb = q(0f, 0f, r)
    val ft = q(0f, -0.012f, r)
    fxCyl(fb.x, fb.y, ft.y, 0.05f * u, 0.046f * u, wood.darken(0.1f), pen)
    val pb = q(0f, -0.012f, r)
    val pt = q(0f, -0.114f, r)
    fxCyl(pb.x, pb.y, pt.y, 0.015f * u, 0.011f * u, wood.darken(0.04f), pen, cap = false)
    val tb = q(0f, -0.114f, r)
    val tt = q(0f, -0.13f, r)
    when (f.place) {
        PlaceId.CAFE -> {
            val cb = q(0f, -0.1f, r)
            fxCyl(cb.x, cb.y, tt.y, (r + 0.004f) * u, (r + 0.004f) * u, FxC.paint, pen, top = FxC.paint, cap = false)
            val disc = fxDisc(tt.x, tt.y, (r + 0.004f) * u)
            drawPath(disc, FxC.paint)
            clipPath(disc) {
                val bounds = disc.getBounds()
                fxGingham(bounds, FxC.pink, 8)
            }
            drawPath(disc, Ink.line, style = pen.stroke)
        }
        PlaceId.LAB -> {
            val cb = q(0f, -0.098f, r)
            fxCyl(cb.x, cb.y, tt.y, (r + 0.005f) * u, (r + 0.005f) * u, Color(0xFF5B3F9E), pen)
            for (k in 0..8) {
                val a = FX_PI + k * FX_PI / 8f
                val rr = (r + 0.005f) * u
                drawCircle(FxC.brass, 0.0035f * u, Offset(cb.x + cos(a) * rr + FX_DX * sin(a) * rr, cb.y + FX_DY * sin(a) * rr + 0.003f * u))
            }
            twinkle(q(-0.03f, -0.13f, r - 0.02f), 0.006f * u, FxC.yellow)
            twinkle(q(0.035f, -0.13f, r + 0.02f), 0.005f * u, FxC.yellow)
        }
        else -> {
            fxCyl(tb.x, tb.y, tt.y, r * u, r * u, wood, pen)
            drawPath(fxDisc(tt.x, tt.y, r * u * 0.62f), wood.darken(0.18f).copy(alpha = 0.5f), style = pen.thin)
        }
    }
}

internal fun DrawScope.fxCounter(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val d = 0.14f
    if (f.variant == 1) {
        // The café counter: mint beadboard, a scalloped trim and a marble top.
        fxBox(u, -0.148f, -0.02f, 0.148f, -0.004f, d - 0.02f, FxC.mint.darken(0.22f), pen, z = 0.008f)
        fxBox(u, -0.15f, -0.158f, 0.15f, -0.018f, d - 0.004f, FxC.mint, pen, rad = 0.004f)
        var x = -0.13f
        while (x < 0.135f) {
            fxLine(p(x, -0.135f), p(x, -0.026f), FxC.mint.darken(0.2f), pen.lw * 0.5f)
            x += 0.02f
        }
        val scallops = FloatArray(15 * 3)
        for (i in 0 until 15) {
            scallops[i * 3] = (-0.14f + i * 0.02f) * u
            scallops[i * 3 + 1] = -0.153f * u
            scallops[i * 3 + 2] = 0.009f * u
        }
        fxCloud(FxC.cream, pen, false, *scallops)
        inked(fxPoly(u, -0.018f, -0.075f, 0.018f, -0.075f, 0.012f, -0.045f, -0.012f, -0.045f), FxC.pink, pen, shade = false)
        for (i in 0 until 3) fxLine(p(-0.01f + i * 0.01f, -0.072f), p(-0.008f + i * 0.008f, -0.048f), FxC.pink.darken(0.25f), pen.lw * 0.5f)
        fxCloud(FxC.cream, pen, false, -0.012f * u, -0.08f * u, 0.011f * u, 0.01f * u, -0.08f * u, 0.011f * u, 0f, -0.09f * u, 0.012f * u)
        inkedCircle(p(0f, -0.103f), 0.006f * u, FxC.red, pen, shade = false)
        fxBox(u, -0.156f, -0.17f, 0.156f, -0.156f, d + 0.006f, Color(0xFFF4F1EC), pen, rad = 0.003f)
        val vein = Path().apply {
            moveTo(-0.12f * u, -0.166f * u)
            quadraticTo(-0.07f * u, -0.158f * u, -0.02f * u, -0.165f * u)
            moveTo(0.04f * u, -0.16f * u)
            quadraticTo(0.09f * u, -0.168f * u, 0.13f * u, -0.161f * u)
        }
        drawPath(vein, FxC.stone.copy(alpha = 0.6f), style = pen.thin)
        return
    }
    // A kitchen counter: white doors and drawers with slim bar handles, an oak top.
    val body = if (f.place == PlaceId.CAFE) FxC.cream else FxC.paint
    fxBox(u, -0.145f, -0.014f, 0.145f, 0f, d - 0.02f, FxC.charcoal, pen, z = 0.01f)
    fxBox(u, -0.15f, -0.158f, 0.15f, -0.012f, d - 0.004f, body, pen, rad = 0.003f)
    for (s in 0..1) {
        val l = if (s == 0) -0.143f else 0.003f
        inkedRound(Rect(l * u, -0.152f * u, (l + 0.14f) * u, -0.127f * u), 0.004f * u, body, pen, shade = false)
        inkedRound(Rect(l * u, -0.122f * u, (l + 0.14f) * u, -0.018f * u), 0.004f * u, body, pen, shade = false)
        fxLine(p(l + 0.055f, -0.14f), p(l + 0.085f, -0.14f), FxC.charcoal, 0.004f * u)
    }
    fxLine(p(-0.016f, -0.11f), p(-0.016f, -0.082f), FxC.charcoal, 0.004f * u)
    fxLine(p(0.016f, -0.11f), p(0.016f, -0.082f), FxC.charcoal, 0.004f * u)
    fxBox(u, -0.155f, -0.17f, 0.155f, -0.156f, d + 0.004f, FxC.oak, pen, rad = 0.003f)
    fxGrain(Rect(-0.15f * u, -0.169f * u, 0.15f * u, -0.157f * u), FxC.oak, pen, 1)
}

// ------------------------------------------------------------------------------------------ storage

internal fun DrawScope.fxShelf(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = when (f.place) {
        PlaceId.LAB -> FxC.walnut
        PlaceId.SALON, PlaceId.CAFE -> FxC.paint
        else -> FxC.oak
    }
    val d = 0.05f
    for (s in 0..1) {
        val x = if (s == 0) -0.09f else 0.09f
        val a = q(x, -0.019f, 0.006f)
        val b = q(x, -0.019f, d)
        val c = q(x, 0f, d)
        val bracket = Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            close()
        }
        fxFace(bracket, FxC.charcoal, pen)
    }
    fxBox(u, -0.13f, -0.03f, 0.13f, -0.019f, d, wood, pen, rad = 0.002f)
    fxGrain(Rect(-0.125f * u, -0.029f * u, 0.125f * u, -0.02f * u), wood, pen, 1)
}

private fun DrawScope.fxBook(u: Float, x0: Float, w: Float, h: Float, bottom: Float, color: Color, pen: Pen) {
    fxBox(u, x0, bottom - h, x0 + w, bottom, 0.072f, color, pen, rad = 0.002f, z = 0.012f, top = FxC.cream, side = color.darken(0.25f))
    val r = fxFront(u, x0, bottom - h, x0 + w, bottom, 0.012f)
    val band = color.lighten(0.45f)
    drawLine(band, Offset(r.left + pen.lw, r.top + h * u * 0.18f), Offset(r.right - pen.lw, r.top + h * u * 0.18f), 0.0025f * u)
    drawLine(band, Offset(r.left + pen.lw, r.bottom - h * u * 0.18f), Offset(r.right - pen.lw, r.bottom - h * u * 0.18f), 0.0025f * u)
}

internal fun DrawScope.fxBookcase(f: Fixture, u: Float, pen: Pen) {
    val wood = if (f.place == PlaceId.LAB) FxC.walnut else FxC.oak
    val d = 0.11f
    fxHollow(u, -0.088f, -0.365f, 0.088f, -0.02f, d, wood.darken(0.12f), pen)
    val cols = arrayOf(FxC.red, FxC.dusty, FxC.mustard, FxC.sage, FxC.blush, FxC.charcoal, FxC.teak, FxC.fjord, FxC.cream)
    clipRect(-0.088f * u, -0.365f * u, 0.088f * u, -0.02f * u) {
        var x = -0.086f
        var i = 0
        while (x < 0.06f) {
            val w = 0.015f + (i % 3) * 0.003f
            val h = 0.074f + ((i * 7) % 4) * 0.006f
            fxBook(u, x, w, h, -0.02f, cols[i % cols.size], pen)
            x += w + 0.0015f
            i++
        }
        fxInShelf(u, -0.088f, 0.088f, -0.14f, d, wood, pen, 0.011f)
        fxBook(u, 0.052f, 0.016f, 0.08f, -0.14f, FxC.fjord, pen)
        fxBook(u, 0.069f, 0.014f, 0.072f, -0.14f, FxC.mustard, pen)
        rotate(-14f, Offset(-0.086f * u, -0.14f * u)) { fxBook(u, -0.086f, 0.015f, 0.078f, -0.14f, FxC.red, pen) }
        fxInShelf(u, -0.088f, 0.088f, -0.26f, d, wood, pen, 0.011f)
        fxBook(u, -0.086f, 0.014f, 0.08f, -0.26f, FxC.sage, pen)
        fxBook(u, -0.071f, 0.017f, 0.086f, -0.26f, FxC.charcoal, pen)
        fxBook(u, -0.053f, 0.013f, 0.072f, -0.26f, FxC.blush, pen)
        fxBook(u, 0.056f, 0.013f, 0.07f, -0.26f, FxC.dusty, pen)
        fxBook(u, 0.07f, 0.0145f, 0.08f, -0.26f, FxC.red, pen)
    }
    inkedRound(Rect(-0.1f * u, -0.372f * u, -0.088f * u, 0f), 0.002f * u, wood, pen)
    fxBox(u, 0.088f, -0.372f, 0.1f, 0f, d, wood, pen, rad = 0.002f)
    inkedRound(Rect(-0.1f * u, -0.02f * u, 0.1f * u, 0f), 0.002f * u, wood.darken(0.08f), pen)
    fxBox(u, -0.103f, -0.38f, 0.103f, -0.365f, d + 0.004f, wood, pen, rad = 0.003f)
}

internal fun DrawScope.fxFridge(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val body = Color(0xFFA8DCD9)
    val d = 0.12f
    fxBox(u, 0.05f, -0.014f, 0.07f, 0f, 0.012f, FxC.charcoal, pen, z = d - 0.02f)
    fxBox(u, -0.085f, -0.36f, 0.085f, -0.01f, d, body, pen, rad = 0.032f)
    fxBox(u, -0.07f, -0.014f, -0.05f, 0f, 0.012f, FxC.charcoal, pen)
    fxBox(u, 0.05f, -0.014f, 0.07f, 0f, 0.012f, FxC.charcoal, pen)
    if (!f.open) {
        drawRoundRect(Ink.line, p(-0.078f, -0.352f), Size(0.156f * u, 0.334f * u), CornerRadius(0.028f * u), style = pen.thin)
        capsule(p(0.064f, -0.24f), p(0.064f, -0.17f), 0.009f * u, FxC.steel, pen)
        fxLine(p(-0.03f, -0.33f), p(0.03f, -0.33f), FxC.steel, 0.006f * u)
        inkedRound(Rect(-0.086f * u, -0.33f * u, -0.078f * u, -0.305f * u), 0.002f * u, FxC.steel, pen, shade = false)
        inkedRound(Rect(-0.086f * u, -0.065f * u, -0.078f * u, -0.04f * u), 0.002f * u, FxC.steel, pen, shade = false)
        drawRect(FxC.charcoal.copy(alpha = 0.7f), p(-0.05f, -0.032f), Size(0.1f * u, 0.01f * u))
        val heart = fxHeart(-0.04f * u, -0.262f * u, 0.011f * u)
        drawPath(heart, FxC.red)
        drawPath(heart, Ink.line, style = pen.thin)
        val star = starPath(p(0.025f, -0.205f), 0.012f * u, 0.005f * u)
        drawPath(star, FxC.yellow)
        drawPath(star, Ink.line, style = pen.thin)
        inkedRound(Rect(-0.028f * u, -0.14f * u, 0.036f * u, -0.085f * u), 0.002f * u, FxC.porcelain, pen, shade = false)
        drawCircle(FxC.yellow, 0.008f * u, p(0.02f, -0.126f))
        inked(fxPoly(u, -0.018f, -0.095f, -0.018f, -0.11f, -0.008f, -0.12f, 0.002f, -0.11f, 0.002f, -0.095f), FxC.red, pen, shade = false)
        fxLine(p(-0.026f, -0.093f), p(0.034f, -0.093f), FxC.green, 0.003f * u)
        inkedCircle(p(0.004f, -0.14f), 0.005f * u, FxC.fjord, pen, shade = false)
        inkedCircle(p(-0.048f, -0.16f), 0.008f * u, FxC.mustard, pen, shade = false)
        return
    }
    // Open: a lit inside with glass shelves; the door swings out on its left hinge.
    fxHollow(u, -0.075f, -0.345f, 0.075f, -0.02f, d - 0.014f, Color(0xFFE6F2FA), pen, back = Color(0xFFD2E6F2))
    clipRect(-0.075f * u, -0.345f * u, 0.075f * u, -0.02f * u) {
        fxGlow(p(0.02f, -0.33f), 0.12f * u, Color.White, 0.9f)
        fxInShelf(u, -0.075f, 0.075f, -0.12f, d - 0.014f, Color(0xFFDDEEF8), pen, 0.006f)
        fxInShelf(u, -0.075f, 0.075f, -0.22f, d - 0.014f, Color(0xFFDDEEF8), pen, 0.006f)
    }
    inkedCircle(p(0.02f, -0.337f), 0.006f * u, Color(0xFFFFF6C8), pen, shade = false)
    val v = fxDoorVec(u, 0.17f, -1f, 118f)
    fxOpenDoor(u, FixtureDoors.fridge, body, Color(0xFFEFF7FA), pen)
    for (k in 0 until 3) {
        val y = -0.27f + k * 0.09f
        val a = p(-0.085f, y)
        val b = Offset(a.x + v.x, a.y + v.y)
        fxLine(Offset(a.x + (b.x - a.x) * 0.1f, a.y + (b.y - a.y) * 0.1f), Offset(a.x + (b.x - a.x) * 0.9f, a.y + (b.y - a.y) * 0.9f), FxC.steel.darken(0.3f), pen.lw)
        val m = Offset(a.x + (b.x - a.x) * 0.35f, a.y + (b.y - a.y) * 0.35f)
        inkedRound(Rect(m.x - 0.005f * u, m.y - 0.03f * u, m.x + 0.005f * u, m.y), 0.002f * u, if (k == 1) FxC.red else FxC.sage, pen, shade = false)
        val n = Offset(a.x + (b.x - a.x) * 0.6f, a.y + (b.y - a.y) * 0.6f)
        inkedRound(Rect(n.x - 0.005f * u, n.y - 0.022f * u, n.x + 0.005f * u, n.y), 0.002f * u, FxC.porcelain, pen, shade = false)
    }
}

internal fun DrawScope.fxWardrobe(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val body = FxC.paint
    val wood = FxC.oak
    val d = 0.12f
    for (s in 0..1) {
        val x = if (s == 0) -0.1f else 0.1f
        fxBox(u, x - 0.007f, -0.02f, x + 0.007f, 0f, 0.012f, wood.darken(0.08f), pen, z = d - 0.018f)
    }
    fxBox(u, -0.12f, -0.37f, 0.12f, -0.016f, d, body, pen, rad = 0.005f)
    fxBox(u, -0.124f, -0.38f, 0.124f, -0.362f, d + 0.006f, wood, pen, rad = 0.003f)
    inkedRound(Rect(-0.122f * u, -0.026f * u, 0.122f * u, -0.014f * u), 0.003f * u, wood, pen)
    for (s in 0..1) {
        val x = if (s == 0) -0.1f else 0.1f
        fxBox(u, x - 0.007f, -0.016f, x + 0.007f, 0f, 0.012f, wood.darken(0.08f), pen)
    }
    if (!f.open) {
        for (s in 0..1) {
            val l = if (s == 0) -0.108f else 0.003f
            inkedRound(Rect(l * u, -0.356f * u, (l + 0.105f) * u, -0.03f * u), 0.005f * u, body, pen, shade = false)
            drawRoundRect(body.darken(0.12f), p(l + 0.014f, -0.335f), Size(0.077f * u, 0.285f * u), CornerRadius(0.012f * u), style = pen.thin)
        }
        inkedCircle(p(-0.012f, -0.19f), 0.008f * u, wood, pen)
        inkedCircle(p(0.012f, -0.19f), 0.008f * u, wood, pen)
        val heart = fxHeart(0f, -0.318f * u, 0.007f * u)
        drawPath(heart, FxC.blush)
        drawPath(heart, Ink.line, style = pen.thin)
        return
    }
    // Open: oak inside, a clothes rail above and a shelf in the middle; both doors swung out.
    val id = d - 0.012f
    fxHollow(u, -0.106f, -0.356f, 0.106f, -0.02f, id, wood.darken(0.1f), pen)
    clipRect(-0.106f * u, -0.356f * u, 0.106f * u, -0.02f * u) {
        val rz = id * 0.5f
        fxLine(q(-0.106f, -0.315f, rz), q(0.106f, -0.315f, rz), FxC.steel, 0.006f * u)
        for (k in 0 until 3) {
            val hx = -0.06f + k * 0.05f
            val hook = q(hx, -0.315f, rz)
            drawArc(Ink.line, 180f, 200f, false, Offset(hook.x - 0.005f * u, hook.y - 0.006f * u), Size(0.01f * u, 0.012f * u), style = pen.thin)
            val a = q(hx, -0.31f, rz)
            val hanger = Path().apply {
                moveTo(a.x, a.y)
                lineTo(a.x - 0.022f * u, a.y + 0.016f * u)
                lineTo(a.x + 0.022f * u, a.y + 0.016f * u)
                close()
            }
            drawPath(hanger, wood.darken(0.1f), style = Stroke(0.004f * u, cap = StrokeCap.Round))
        }
        fxInShelf(u, -0.106f, 0.106f, -0.19f, id, wood, pen, 0.009f)
    }
    fxOpenDoor(u, FixtureDoors.wardrobeLeft, body, body.darken(0.05f), pen)
    val lv = fxDoorVec(u, 0.106f, -1f, 128f)
    inkedCircle(Offset(-0.12f * u + lv.x * 0.85f, -0.19f * u + lv.y * 0.85f), 0.006f * u, wood, pen, shade = false)
    fxOpenDoor(u, FixtureDoors.wardrobeRight, body, body.darken(0.05f), pen)
    val rv = fxDoorVec(u, 0.106f, 1f, 128f)
    inkedCircle(Offset(0.12f * u + rv.x * 0.85f, -0.19f * u + rv.y * 0.85f), 0.006f * u, wood, pen, shade = false)
}

internal fun DrawScope.fxChest(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    // A painted toy chest with rope handles.
    val paint = FxC.dusty
    val d = 0.1f
    if (f.open) {
        fxBox(u, -0.094f, -0.19f, 0.094f, -0.09f, 0.016f, paint.darken(0.08f), pen, rad = 0.006f, z = d - 0.004f)
        val lid = fxFront(u, -0.084f, -0.182f, 0.084f, -0.098f, d - 0.004f)
        inkedRound(lid, 0.004f * u, FxC.oak, pen, shade = false)
        val star = starPath(lid.center, 0.014f * u, 0.006f * u)
        drawPath(star, FxC.yellow)
        drawPath(star, Ink.line, style = pen.thin)
    }
    for (s in 0..1) {
        val x = if (s == 0) -0.078f else 0.078f
        fxBox(u, x - 0.01f, -0.012f, x + 0.01f, 0f, 0.014f, FxC.oakDark, pen, z = d - 0.02f)
    }
    fxBox(u, -0.09f, -0.088f, 0.09f, -0.008f, d, paint, pen, rad = 0.005f)
    // The rope handle on the right side.
    val hc = q(0.09f, -0.058f, d / 2f)
    drawArc(FxC.straw.darken(0.1f), 0f, 180f, false, Offset(hc.x - 0.012f * u, hc.y - 0.014f * u), Size(0.024f * u, 0.024f * u), style = Stroke(0.005f * u, cap = StrokeCap.Round))
    if (f.open) {
        fxFace(fxFlat(u, -0.08f, 0.08f, -0.088f, 0.01f, d - 0.01f), paint.darken(0.55f), pen)
        fxHollow(u, -0.08f, -0.08f, 0.08f, -0.015f, d - 0.02f, FxC.oak.darken(0.1f), pen)
        for (k in 0 until 3) {
            val ph = fxFrac(pen.t * 0.5f + k * 0.33f)
            twinkle(q(-0.05f + k * 0.05f, -0.03f - ph * 0.1f, 0.03f), 0.007f * u * (1f - ph), FxC.yellow, 1f - ph)
        }
    } else {
        fxBox(u, -0.094f, -0.1f, 0.094f, -0.084f, d + 0.004f, paint.darken(0.08f), pen, rad = 0.005f)
        val star = starPath(p(0f, -0.05f), 0.016f * u, 0.007f * u)
        drawPath(star, FxC.yellow)
        drawPath(star, Ink.line, style = pen.thin)
        drawLine(FxC.paint, p(-0.09f, -0.074f), p(0.09f, -0.074f), 0.004f * u)
    }
    drawLine(FxC.paint, p(-0.09f, -0.024f), p(0.09f, -0.024f), 0.007f * u)
    fxBox(u, -0.088f, -0.012f, -0.068f, 0f, 0.014f, FxC.oakDark, pen)
    fxBox(u, 0.068f, -0.012f, 0.088f, 0f, 0.014f, FxC.oakDark, pen)
}

// ------------------------------------------------------------------------------------------ kitchen

internal fun DrawScope.fxStove(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val enamel = FxC.paint
    val t = pen.t
    val d = 0.13f
    fxBox(u, -0.09f, -0.232f, 0.09f, -0.2f, 0.012f, enamel, pen, rad = 0.004f, z = d - 0.012f)
    drawCircle(if (f.on) FxC.flame1 else FxC.stone, 0.0035f * u, q(0.06f, -0.216f, d - 0.012f))
    if (f.on) fxGlow(q(0.06f, -0.216f, d - 0.012f), 0.012f * u, FxC.flame1, 0.8f)
    fxBox(u, -0.09f, -0.19f, 0.09f, -0.01f, d, enamel, pen, rad = 0.006f)
    fxBox(u, -0.094f, -0.2f, 0.094f, -0.188f, d, FxC.charcoal, pen, rad = 0.003f, top = FxC.charcoal.lighten(0.12f))
    // Two burners on the cooktop; red hot when on.
    for (s in 0..1) {
        val x = if (s == 0) -0.045f else 0.042f
        val c = q(x, -0.2f, d * 0.5f)
        val ring = fxDisc(c.x, c.y, 0.03f * u)
        if (f.on) {
            fxGlow(c, 0.05f * u, FxC.flame1, 0.45f + 0.15f * sin(t * 6f + s))
            drawPath(ring, Color(0xFFFF4A2E))
            drawPath(fxDisc(c.x, c.y, 0.017f * u), Color(0xFFFFA23A), style = pen.thin)
            for (k in 0 until 2) {
                val ph = fxFrac(t * 0.8f + k * 0.5f + s * 0.25f)
                val wx = c.x + (k - 0.5f) * 0.024f * u
                val wave = Path().apply {
                    moveTo(wx, c.y - (0.01f + ph * 0.06f) * u)
                    quadraticTo(wx + 0.006f * u, c.y - (0.025f + ph * 0.06f) * u, wx, c.y - (0.04f + ph * 0.06f) * u)
                }
                drawPath(wave, Color.White.copy(alpha = 0.5f * (1f - ph)), style = pen.thin)
            }
        } else {
            drawPath(ring, FxC.charcoal.lighten(0.25f))
            drawPath(fxDisc(c.x, c.y, 0.017f * u), FxC.charcoal.darken(0.2f), style = pen.thin)
        }
        drawPath(ring, Ink.line, style = pen.thin)
    }
    inkedRound(Rect(-0.084f * u, -0.184f * u, 0.084f * u, -0.162f * u), 0.004f * u, FxC.steel, pen, shade = false)
    for (k in 0 until 4) {
        val c = p(-0.06f + k * 0.04f, -0.173f)
        inkedCircle(c, 0.008f * u, FxC.charcoal, pen, shade = false)
        val a = if (f.on && (k == 0 || k == 3)) 1.2f else 0f
        drawLine(Color.White, c, Offset(c.x + sin(a) * 0.006f * u, c.y - cos(a) * 0.006f * u), 0.0022f * u, StrokeCap.Round)
    }
    inkedRound(Rect(-0.078f * u, -0.155f * u, 0.078f * u, -0.03f * u), 0.01f * u, enamel.lighten(0.3f), pen)
    inkedRound(Rect(-0.054f * u, -0.13f * u, 0.054f * u, -0.062f * u), 0.01f * u, Color(0xFF3A3040), pen, shade = false)
    fxLine(p(-0.046f, -0.08f), p(0.046f, -0.08f), FxC.stone.copy(alpha = 0.5f), pen.lw * 0.6f)
    shine(p(-0.034f, -0.117f), 0.02f * u, 0.007f * u, 0.4f)
    capsule(p(-0.056f, -0.145f), p(0.056f, -0.145f), 0.006f * u, FxC.steel, pen)
    drawRect(FxC.charcoal, p(-0.085f, -0.024f), Size(0.17f * u, 0.01f * u))
}

internal fun DrawScope.fxSink(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.13f
    val bd = d - 0.035f
    fxBox(u, -0.084f, -0.012f, 0.084f, 0f, d - 0.02f, FxC.charcoal, pen, z = 0.01f)
    fxBox(u, -0.09f, -0.19f, 0.09f, -0.01f, d - 0.004f, FxC.paint, pen, rad = 0.003f)
    for (s in 0..1) {
        val l = if (s == 0) -0.084f else 0.003f
        inkedRound(Rect(l * u, -0.16f * u, (l + 0.081f) * u, -0.016f * u), 0.004f * u, FxC.paint, pen, shade = false)
    }
    fxLine(p(-0.014f, -0.14f), p(-0.014f, -0.112f), FxC.charcoal, 0.004f * u)
    fxLine(p(0.014f, -0.14f), p(0.014f, -0.112f), FxC.charcoal, 0.004f * u)
    // The worktop with the basin sunk into it; its floor is where things rest.
    fxBox(u, -0.094f, -0.2f, 0.094f, -0.186f, d, FxC.oak, pen, rad = 0.002f)
    fxFace(fxFlat(u, -0.066f, 0.066f, -0.2f, 0.006f, bd, 0.01f), FxC.steel.darken(0.3f), pen)
    fxHollow(u, -0.066f, -0.2f, 0.066f, -0.165f, bd, FxC.steel, pen)
    drawPath(fxDisc(q(0f, -0.165f, bd * 0.5f).x, q(0f, -0.165f, bd * 0.5f).y, 0.007f * u), FxC.charcoal)
    if (f.on) {
        clipRect(-0.066f * u, -0.2f * u, 0.066f * u, -0.165f * u) {
            fxFace(fxFlat(u, -0.07f, 0.07f, -0.171f, 0f, bd), FxC.water.copy(alpha = 0.55f), pen)
        }
    }
    // The tap at the back of the worktop.
    val zs = 0.05f
    val base = q(0f, -0.2f, d - 0.018f)
    val topP = q(0f, -0.262f, d - 0.018f)
    val arc = q(0f, -0.272f, zs + 0.02f)
    val spout = q(0f, -0.252f, zs)
    capsule(base, topP, 0.009f * u, FxC.steel, pen)
    val neck = Path().apply {
        moveTo(topP.x, topP.y)
        quadraticTo(arc.x, arc.y, spout.x, spout.y)
    }
    drawPath(neck, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(neck, FxC.steel, style = Stroke(0.009f * u, cap = StrokeCap.Round))
    inkedCircle(q(-0.022f, -0.206f, d - 0.02f), 0.006f * u, FxC.red, pen, shade = false)
    inkedCircle(q(0.022f, -0.206f, d - 0.02f), 0.006f * u, FxC.fjord, pen, shade = false)
    if (f.on) {
        val end = q(0f, -0.171f, zs)
        val stream = Path().apply {
            moveTo(spout.x, spout.y)
            for (k in 1..4) {
                val fk = k / 4f
                lineTo(spout.x + sin(t * 25f + k * 1.7f) * 0.0015f * u, spout.y + (end.y - spout.y) * fk)
            }
        }
        drawPath(stream, FxC.water.copy(alpha = 0.85f), style = Stroke(0.008f * u, cap = StrokeCap.Round))
        drawPath(stream, Color.White.copy(alpha = 0.8f), style = Stroke(0.0025f * u, cap = StrokeCap.Round))
        for (k in 0 until 3) {
            val ph = fxFrac(t * 2.2f + k * 0.33f)
            drawCircle(FxC.water, 0.003f * u, Offset(end.x + (k - 1) * 0.012f * u * ph, end.y - sin(ph * FX_PI) * 0.015f * u))
        }
    }
}

// ------------------------------------------------------------------------------------------ bathroom

private const val BATH_D = 0.16f

/** The tub's silhouette seen from the front: a rolled rim at -0.12 and a deep, round belly. */
private fun fxTubPath(u: Float): Path = Path().apply {
    moveTo(-0.197f * u, -0.12f * u)
    lineTo(0.197f * u, -0.12f * u)
    cubicTo(0.203f * u, -0.055f * u, 0.185f * u, -0.012f * u, 0.13f * u, -0.008f * u)
    lineTo(-0.13f * u, -0.008f * u)
    cubicTo(-0.185f * u, -0.012f * u, -0.203f * u, -0.055f * u, -0.197f * u, -0.12f * u)
    close()
}

private fun DrawScope.fxClawFoot(u: Float, x: Float, z: Float, pen: Pen) {
    val o = fxQ(u, 0f, 0f, z)
    translate(o.x, o.y) {
        val foot = fxBlob(u, x - 0.022f, 0.002f, x - 0.016f, -0.042f, x + 0.016f, -0.042f, x + 0.022f, 0.002f, x, 0.006f)
        inked(foot, FxC.brass, pen)
        fxLine(Offset((x - 0.007f) * u, -0.008f * u), Offset((x - 0.007f) * u, 0f), FxC.brass.darken(0.35f), pen.lw * 0.6f)
        fxLine(Offset((x + 0.007f) * u, -0.008f * u), Offset((x + 0.007f) * u, 0f), FxC.brass.darken(0.35f), pen.lw * 0.6f)
    }
}

internal fun DrawScope.fxBath(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = BATH_D
    fxClawFoot(u, -0.16f, d - 0.03f, pen)
    fxClawFoot(u, 0.16f, d - 0.03f, pen)
    // The tub's body swept back in depth, then its rim and the inside seen from above.
    val tub = fxTubPath(u)
    val side = FxC.dusty.darken(0.22f)
    for (k in 5 downTo 1) {
        val o = q(0f, 0f, d * k / 5f)
        translate(o.x, o.y) {
            drawPath(tub, side)
            if (k == 5) drawPath(tub, Ink.line, style = pen.stroke)
        }
    }
    val rim = fxFlat(u, -0.197f, 0.197f, -0.12f, 0f, d, 0.035f)
    fxFace(rim, FxC.porcelain, pen)
    val inside = fxFlat(u, -0.18f, 0.18f, -0.12f, 0.014f, d - 0.014f, 0.03f)
    fxFace(inside, Color(0xFFDCE7F1), pen)
    if (f.on) {
        clipPath(inside) {
            fxFace(fxFlat(u, -0.2f, 0.2f, -0.113f, 0f, d), FxC.water, pen)
            val cloud = FloatArray(6 * 3)
            for (i in 0 until 6) {
                val c = q(-0.12f + i * 0.05f, -0.116f, 0.03f + (i % 3) * 0.04f)
                cloud[i * 3] = c.x
                cloud[i * 3 + 1] = c.y
                cloud[i * 3 + 2] = (0.011f + (i % 2) * 0.004f) * u
            }
            fxCloud(Color.White, pen, false, *cloud)
        }
    }
    // Tap on the rim at the back.
    val base = q(-0.155f, -0.12f, d - 0.012f)
    val top = q(-0.155f, -0.195f, d - 0.012f)
    val spout = q(-0.128f, -0.182f, d - 0.03f)
    capsule(base, top, 0.009f * u, FxC.steel, pen)
    capsule(top, spout, 0.009f * u, FxC.steel, pen)
    inkedCircle(q(-0.172f, -0.175f, d - 0.012f), 0.006f * u, FxC.porcelain, pen, shade = false)
    fxClawFoot(u, -0.16f, 0.02f, pen)
    fxClawFoot(u, 0.16f, 0.02f, pen)
}

/**
 * The tub's front side over whoever bathes, with a fluffy mat on the floor in front; bubbles pile up
 * along the rim when it is full.
 */
internal fun DrawScope.fxBathFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val tub = fxTubPath(u)
    inked(tub, FxC.dusty, pen)
    fxBox(u, -0.1f, -0.012f, 0.1f, 0f, 0.065f, FxC.sage, pen, rad = 0.01f, z = -0.07f, top = FxC.sage.lighten(0.2f))
    val mat = fxFront(u, -0.1f, -0.012f, 0.1f, 0f, -0.07f)
    for (k in 0 until 10) fxLine(Offset(mat.left + (k + 0.5f) * mat.width / 10f, mat.bottom), Offset(mat.left + (k + 0.5f) * mat.width / 10f, mat.bottom + 0.006f * u), FxC.sage.darken(0.2f), pen.lw * 0.7f)
    for (k in 0 until 6) {
        val c = fxQ(u, -0.085f + k * 0.034f, -0.012f, -0.04f + (k % 2) * 0.02f)
        drawCircle(Color.White.copy(alpha = 0.45f), 0.004f * u, c)
    }
    val roll = Path().apply {
        moveTo(-0.195f * u, -0.12f * u)
        lineTo(0.195f * u, -0.12f * u)
    }
    drawPath(roll, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(roll, FxC.porcelain, style = Stroke(0.012f * u, cap = StrokeCap.Round))
    drawLine(Color.White, p(-0.1f, -0.123f), p(0.05f, -0.123f), 0.003f * u, StrokeCap.Round)
    fxLine(p(-0.16f, -0.095f), p(0.17f, -0.095f), FxC.porcelain.copy(alpha = 0.7f), 0.004f * u)
    if (!f.on) return
    val t = pen.t
    val foam = FloatArray(10 * 3)
    for (i in 0 until 10) {
        foam[i * 3] = (-0.13f + i * 0.032f) * u
        foam[i * 3 + 1] = (-0.126f - (i % 3) * 0.004f + sin(t * 2f + i) * 0.002f) * u
        foam[i * 3 + 2] = (0.012f + (i % 2) * 0.006f) * u
    }
    fxCloud(Color.White, pen, false, *foam)
    for (i in 0 until 4) {
        val ph = fxFrac(t * 0.35f + i * 0.27f)
        val c = p(-0.1f + i * 0.07f + sin(ph * 6f + i) * 0.01f, -0.14f - ph * 0.12f)
        drawCircle(Color.White.copy(alpha = 0.7f * (1f - ph)), 0.007f * u, c, style = pen.thin)
        drawCircle(Color.White.copy(alpha = 0.8f * (1f - ph)), 0.0018f * u, Offset(c.x - 0.002f * u, c.y - 0.002f * u))
    }
}

internal fun DrawScope.fxToilet(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val white = FxC.porcelain
    val cz = 0.056f
    fxBox(u, -0.045f, -0.176f, 0.045f, -0.1f, 0.04f, white, pen, rad = 0.008f, z = 0.085f)
    fxBox(u, -0.049f, -0.184f, 0.049f, -0.174f, 0.046f, white.darken(0.03f), pen, rad = 0.004f, z = 0.082f)
    val btn = q(0f, -0.184f, 0.105f)
    drawPath(fxDisc(btn.x, btn.y, 0.008f * u), FxC.steel)
    drawPath(fxDisc(btn.x, btn.y, 0.008f * u), Ink.line, style = pen.thin)
    inkedOval(fxFront(u, -0.042f, -0.16f, 0.042f, -0.088f, 0.082f), white.darken(0.02f), pen)
    val pb = q(0f, 0f, cz)
    val pm = q(0f, -0.045f, cz)
    fxCyl(pb.x, pb.y, pm.y, 0.03f * u, 0.034f * u, white, pen, cap = false)
    val bt = q(0f, -0.08f, cz)
    fxCyl(pm.x, pm.y, bt.y, 0.036f * u, 0.054f * u, white, pen, cap = false)
    val seat = q(0f, -0.085f, cz)
    fxFace(fxDisc(seat.x, seat.y, 0.056f * u), Color.White, pen)
    val hole = fxDisc(seat.x + 0.004f * u, seat.y - 0.003f * u, 0.036f * u)
    drawPath(hole, Color(0xFFD4E3EE))
    drawPath(hole, Ink.line, style = pen.thin)
    drawPath(fxDisc(seat.x + 0.006f * u, seat.y - 0.005f * u, 0.02f * u), FxC.water.copy(alpha = 0.6f))
    if (f.anim > 0f) {
        // Flushing: a blue swirl in the bowl and a few leaping drops.
        val a = f.anim
        val b = hole.getBounds()
        drawPath(hole, FxC.water.copy(alpha = a))
        for (k in 0 until 2) {
            drawArc(Color.White.copy(alpha = a), a * 900f + k * 180f, 120f, false, b.topLeft, b.size, style = Stroke(0.003f * u, cap = StrokeCap.Round))
        }
        for (k in 0 until 3) {
            val ph = 1f - a
            drawCircle(FxC.water.copy(alpha = a), 0.004f * u, Offset(seat.x + (k - 1) * 0.02f * u, seat.y - sin(ph * FX_PI) * (0.03f + k * 0.008f) * u))
        }
    }
}
