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
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The living room of Storstova in oblique 3D: the stone fireplace with its stockings, the big sofa and the
// wing chairs, the TV with five channels and the film, the coffee table with the popcorn bowl, the globe,
// the wall aquarium and the floor lamps. Origin at the bottom centre of each fixture's front face.

// ------------------------------------------------------------------------------------------------ the fireplace

internal fun DrawScope.grFireplace(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.2f
    val stone = GrC.stone
    val on = f.on
    grShadow(u, 0.56f, d)

    // The chimney breast rises to the ceiling, with a gilded mirror over the mantel.
    val ceiling = -f.y
    fxBox(u, -0.2f, ceiling, 0.2f, -0.46f, 0.08f, Color(0xFFE5AE7F), pen, rad = 0.004f, z = 0.06f, top = Color(0xFFF0C9A0), side = Color(0xFFC98A55))
    val mirror = Rect(-0.1f * u, -0.74f * u, 0.1f * u, -0.52f * u)
    val m3 = q(0f, 0f, 0.06f)
    translate(m3.x, m3.y) {
        val oval = ovalPath(mirror)
        drawPath(oval, Ink.shadow, style = Stroke(0.016f * u))
        inkedOval(mirror, GrC.brass, pen)
        val glass = Rect(mirror.left + 0.012f * u, mirror.top + 0.012f * u, mirror.right - 0.012f * u, mirror.bottom - 0.012f * u)
        val gp = ovalPath(glass)
        drawPath(gp, Brush.linearGradient(listOf(Color(0xFFE6F4FB), Color(0xFFB7D6EA), Color(0xFFD9EEF8)), glass.topLeft, glass.bottomRight))
        clipPath(gp) {
            // The fire seen in the mirror, and a band of light that slides across.
            if (on) drawOval(Color(0x55FF8A3D), Offset(glass.center.x - 0.04f * u, glass.bottom - 0.06f * u), Size(0.08f * u, 0.07f * u))
            val cx = glass.left - 0.05f * u + fxFrac(pen.t * 0.12f) * (glass.width + 0.1f * u)
            val band = Path().apply {
                moveTo(cx - 0.01f * u, glass.top); lineTo(cx + 0.016f * u, glass.top); lineTo(cx - 0.02f * u, glass.bottom); lineTo(cx - 0.046f * u, glass.bottom); close()
            }
            drawPath(band, Color.White.copy(alpha = 0.4f))
        }
        drawPath(gp, Ink.line, style = pen.thin)
        shine(Offset(glass.left + 0.025f * u, glass.top + 0.04f * u), 0.01f * u, 0.03f * u, 0.55f)
        // A bow of ribbon on top.
        inkedCircle(p(0f, -0.76f), 0.008f * u, GrC.burgundy, pen, shade = false)
    }

    // The hearth slab, the two stone pillars and the mantel.
    fxBox(u, -0.27f, -0.026f, 0.27f, 0f, d, stone.lighten(0.12f), pen, rad = 0.004f)
    for (s in 0..1) {
        val x0 = if (s == 0) -0.245f else 0.135f
        fxBox(u, x0, -0.43f, x0 + 0.11f, -0.026f, d - 0.02f, stone, pen, rad = 0.004f, z = 0.01f)
        // Stone courses.
        for (k in 1..7) {
            val y = -0.026f - k * 0.05f
            val a = q(x0, y, 0.01f)
            drawLine(stone.darken(0.3f), a, Offset(a.x + 0.11f * u, a.y), pen.lw * 0.6f)
        }
    }
    // The firebox: bricks at the back, soot, andirons and logs.
    fxHollow(u, -0.135f, -0.4f, 0.135f, -0.026f, 0.11f, Color(0xFF8E4F3A), pen, back = Color(0xFF3A241E))
    clipRect(-0.135f * u, -0.4f * u, 0.135f * u, -0.026f * u) {
        // Soot arch and a glow.
        if (on) {
            grGlow(p(0f, -0.09f), 0.2f * u, pen, 0.55f, Color(0xFFFF9A3D))
        }
        // Logs on andirons.
        val log = Color(0xFF6E4630)
        capsule(p(-0.09f, -0.06f), p(0.09f, -0.075f), 0.024f * u, log, pen)
        capsule(p(-0.07f, -0.085f), p(0.08f, -0.09f), 0.02f * u, log.lighten(0.1f), pen)
        for (s in 0..1) {
            val ax = if (s == 0) -0.105f else 0.105f
            capsule(p(ax, -0.026f), p(ax, -0.1f), 0.008f * u, Ink.line, pen)
            grKnob(p(ax, -0.108f), 0.008f * u, pen)
        }
        if (on) {
            val fl = pen.t
            fxFire(p(-0.045f, -0.085f).x, p(0f, -0.085f).y, 0.07f * u, 0.15f * u, fl, 0.5f, pen, false)
            fxFire(p(0.04f, -0.085f).x, p(0f, -0.085f).y, 0.06f * u, 0.12f * u, fl, 2.1f, pen, false)
            fxFire(p(0f, -0.085f).x, p(0f, -0.085f).y, 0.08f * u, 0.19f * u, fl, 1.2f, pen, false)
            for (k in 0 until 5) {
                val ph = fxFrac(fl * 0.8f + k * 0.2f)
                val a = (1f - ph)
                drawCircle(Color(0xFFFFC96B).copy(alpha = a), 0.0028f * u, p(-0.07f + k * 0.035f + sin(ph * 6f + k) * 0.01f, -0.1f - ph * 0.22f))
            }
        } else {
            // Cold embers and a little grey ash.
            drawOval(Color(0xFF8E887F), p(-0.07f, -0.04f), Size(0.14f * u, 0.014f * u))
            drawCircle(Color(0xFF5E3D2E), 0.004f * u, p(0.02f, -0.045f))
        }
    }
    // The mantel shelf, with a carriage clock in the middle and stockings on the front.
    fxBox(u, -0.275f, -0.46f, 0.275f, -0.43f, d + 0.02f, GrC.walnut, pen, rad = 0.004f, z = -0.01f, top = GrC.walnutLight, side = GrC.walnutDark)
    // The clock.
    run {
        val c = p(0f, -0.46f)
        inkedRound(Rect(c.x - 0.022f * u, c.y - 0.05f * u, c.x + 0.022f * u, c.y), 0.007f * u, GrC.brass, pen)
        inkedCircle(Offset(c.x, c.y - 0.027f * u), 0.014f * u, GrC.ivory, pen, shade = false)
        val a = pen.t * 0.4f
        drawLine(Ink.line, Offset(c.x, c.y - 0.027f * u), Offset(c.x + sin(a) * 0.011f * u, c.y - 0.027f * u - cos(a) * 0.011f * u), pen.lw * 0.9f, StrokeCap.Round)
        drawLine(Ink.line, Offset(c.x, c.y - 0.027f * u), Offset(c.x + 0.007f * u, c.y - 0.03f * u), pen.lw * 1.1f, StrokeCap.Round)
        grKnob(Offset(c.x, c.y - 0.054f * u), 0.005f * u, pen)
    }
    val socks = arrayOf(Color(0xFFD2443A), Color(0xFF3F9A55), Color(0xFFF2EEE6))
    val trim = arrayOf(Color(0xFFF2EEE6), Color(0xFFD2443A), Color(0xFF2F6FB8))
    for (k in 0 until 3) {
        val sx = -0.21f + k * 0.04f
        val sway = sin(pen.t * 1.1f + k * 1.3f) * 0.004f * u
        val top = p(sx, -0.43f)
        val sock = Path().apply {
            moveTo(top.x - 0.014f * u, top.y)
            lineTo(top.x + 0.014f * u, top.y)
            lineTo(top.x + 0.012f * u + sway, top.y + 0.052f * u)
            quadraticTo(top.x + 0.028f * u + sway, top.y + 0.07f * u, top.x + sway, top.y + 0.07f * u)
            quadraticTo(top.x - 0.016f * u + sway, top.y + 0.07f * u, top.x - 0.012f * u + sway, top.y + 0.05f * u)
            close()
        }
        inked(sock, socks[k], pen)
        drawRect(trim[k], Offset(top.x - 0.015f * u, top.y), Size(0.03f * u, 0.011f * u))
        drawRect(Ink.line, Offset(top.x - 0.015f * u, top.y), Size(0.03f * u, 0.011f * u), style = pen.thin)
        drawCircle(Color.White.copy(alpha = 0.8f), 0.004f * u, Offset(top.x + sway, top.y + 0.03f * u))
        drawCircle(Color(0xFFFFC83D), 0.0035f * u, Offset(top.x + sway * 1.2f, top.y + 0.052f * u))
    }
}

// ------------------------------------------------------------------------------------------------ chairs and sofa

/** Variant colours for the wing chairs: burgundy (the hearth), mustard, forest green (the library). */
private fun wingColor(v: Int): Color = when (v.mod(3)) {
    0 -> Color(0xFFA73A4A)
    1 -> Color(0xFFD9A93E)
    else -> Color(0xFF2E6A4A)
}

internal fun DrawScope.grWingChair(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.12f
    val fab = wingColor(f.variant)
    grShadow(u, 0.2f, d)
    for (s in 0..1) {
        val x = if (s == 0) -0.075f else 0.075f
        fxBox(u, x - 0.007f, -0.03f, x + 0.007f, 0f, 0.012f, GrC.walnutDark, pen, z = d - 0.02f)
        fxBox(u, x - 0.007f, -0.03f, x + 0.007f, 0f, 0.012f, GrC.walnutDark, pen, z = 0.008f)
    }
    // The tall back with its two wings.
    fxBox(u, -0.085f, -0.26f, 0.085f, -0.06f, 0.035f, fab, pen, rad = 0.035f, z = d - 0.045f)
    for (s in 0..1) {
        val x0 = if (s == 0) -0.1f else 0.068f
        fxBox(u, x0, -0.225f, x0 + 0.032f, -0.07f, 0.075f, fab.darken(0.08f), pen, rad = 0.014f, z = d - 0.09f)
    }
    // Tufting on the back: buttons and creases.
    for (row in 0..1) for (col in 0..2) {
        val c = q(-0.045f + col * 0.045f + row * 0.0f, -0.2f + row * 0.05f, d - 0.012f)
        drawCircle(fab.darken(0.35f), 0.0035f * u, c)
        if (col < 2) drawLine(fab.darken(0.2f), c, Offset(c.x + 0.045f * u, c.y + (if (row == 0) 0.025f else -0.025f) * u), pen.lw * 0.5f)
    }
    // The base and the seat cushion.
    fxBox(u, -0.1f, -0.075f, 0.1f, -0.026f, d, fab.darken(0.1f), pen, rad = 0.008f)
    fxBox(u, -0.068f, -0.098f, 0.068f, -0.07f, d - 0.025f, fab.lighten(0.07f), pen, rad = 0.012f, z = 0.012f)
    // The rolled arms in front.
    for (s in 0..1) {
        val x0 = if (s == 0) -0.1f else 0.068f
        fxBox(u, x0, -0.13f, x0 + 0.032f, -0.05f, d, fab, pen, rad = 0.014f)
        // Nail-head trim.
        for (k in 0..3) drawCircle(GrC.brass, 0.0022f * u, p(x0 + 0.004f + k * 0.0075f, -0.056f))
    }
    // A knitted cushion for the hearth chair.
    if (f.variant == 0) {
        fxBox(u, -0.035f, -0.145f, 0.0f, -0.098f, 0.03f, Color(0xFFF2EEE6), pen, rad = 0.012f, z = 0.04f)
        fxKnit(fxFront(u, -0.03f, -0.14f, -0.005f, -0.104f, 0.04f), Color(0xFFD9774F), 3, 3, pen.lw * 0.45f)
    }
}

internal fun DrawScope.grSofa(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.17f
    val fab = Color(0xFF2F6A78)
    grShadow(u, 0.52f, d)
    for (s in 0..1) {
        val x = if (s == 0) -0.215f else 0.215f
        capsule(q(x, -0.024f, d - 0.03f), q(x, -0.002f, d - 0.03f), 0.014f * u, GrC.walnutDark, pen)
        capsule(q(x, -0.024f, 0.02f), q(x, -0.002f, 0.02f), 0.014f * u, GrC.walnutDark, pen)
    }
    // The tufted back.
    fxBox(u, -0.235f, -0.245f, 0.235f, -0.06f, 0.045f, fab, pen, rad = 0.032f, z = d - 0.055f)
    for (row in 0..2) for (col in 0..9) {
        val x = -0.2f + col * 0.0445f + (row % 2) * 0.0222f
        if (x > 0.215f) continue
        val c = q(x, -0.21f + row * 0.045f, d - 0.0115f)
        drawCircle(fab.darken(0.4f), 0.0032f * u, c)
        if (col < 9 && x + 0.0445f < 0.215f) {
            drawLine(fab.darken(0.22f), c, Offset(c.x + 0.0222f * u, c.y + (if (row == 0) 0.02f else if (row == 1) -0.02f else 0f) * u), pen.lw * 0.5f)
        }
    }
    // The seat base and three cushions.
    fxBox(u, -0.2f, -0.07f, 0.2f, -0.026f, d, fab.darken(0.1f), pen, rad = 0.008f)
    for (k in 0 until 3) {
        val x0 = -0.2f + k * 0.1333f
        fxBox(u, x0 + 0.002f, -0.1f, x0 + 0.1313f, -0.066f, d - 0.03f, fab.lighten(0.06f), pen, rad = 0.012f, z = 0.012f)
        val c = q(x0 + 0.0667f, -0.094f, 0.012f)
        drawCircle(fab.darken(0.35f), 0.003f * u, c)
    }
    // Rolled arms with brass nail heads.
    for (s in 0..1) {
        val x0 = if (s == 0) -0.255f else 0.2f
        fxBox(u, x0, -0.165f, x0 + 0.055f, -0.03f, d, fab, pen, rad = 0.02f)
        for (k in 0..3) drawCircle(GrC.brass, 0.0024f * u, p(x0 + 0.007f + k * 0.0135f, -0.045f))
    }
    // Two scatter cushions and a knitted blanket over one arm.
    fxBox(u, -0.17f, -0.185f, -0.115f, -0.1f, 0.03f, Color(0xFFD9774F), pen, rad = 0.014f, z = 0.07f)
    drawCircle(Color(0xFFA85A36), 0.0035f * u, q(-0.1425f, -0.145f, 0.07f))
    fxBox(u, 0.12f, -0.18f, 0.18f, -0.1f, 0.03f, Color(0xFFF2EEE6), pen, rad = 0.014f, z = 0.07f)
    fxKnit(fxFront(u, 0.125f, -0.175f, 0.175f, -0.105f, 0.07f), Color(0xFF8E2F3E), 4, 5, pen.lw * 0.45f)
}

// ------------------------------------------------------------------------------------------------ tables, bowl, globe

internal fun DrawScope.grCoffeeTable(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.14f
    grShadow(u, 0.26f, d)
    // Brass hairpin legs and a round marble top.
    for ((x, z) in listOf(-0.1f to 0.015f, 0.1f to 0.015f, -0.1f to d - 0.02f, 0.1f to d - 0.02f)) {
        capsule(q(x, -0.07f, z), q(x * 1.08f, 0f, z), 0.006f * u, GrC.brass, pen)
    }
    fxBox(u, -0.13f, -0.088f, 0.13f, -0.07f, d, GrC.marble, pen, rad = 0.008f, top = Color.White, side = GrC.marbleDark)
    val top = fxFlat(u, -0.12f, 0.12f, -0.0885f, 0.01f, d - 0.01f, 0.004f)
    clipPath(top) {
        for (k in 0 until 4) drawLine(GrC.marbleDark.copy(alpha = 0.6f), q(-0.1f + k * 0.07f, -0.0885f, 0.02f), q(-0.07f + k * 0.07f, -0.0885f, d - 0.02f), pen.lw * 0.5f)
    }
    // A lace mat under the bowl.
    drawPath(fxDisc2(q(0f, -0.089f, 0.06f).x, q(0f, -0.089f, 0.06f).y, 0.035f * u, 0.03f * u), Color(0xFFFFF8EA).copy(alpha = 0.9f), style = Stroke(pen.lw * 0.5f))
}

internal fun DrawScope.grPopcornBowl(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    // A cinema bucket in red and white stripes, heaped with popcorn that now and then pops.
    val body = Path().apply {
        moveTo(-0.034f * u, -0.032f * u)
        lineTo(0.034f * u, -0.032f * u)
        lineTo(0.027f * u, 0f)
        lineTo(-0.027f * u, 0f)
        close()
    }
    val heap = floatArrayOf(-0.026f, -0.04f, -0.012f, -0.052f, 0.006f, -0.056f, 0.02f, -0.046f, 0.03f, -0.038f, -0.002f, -0.04f, 0.012f, -0.04f, -0.02f, -0.034f)
    var i = 0
    while (i < heap.size) {
        val c = p(heap[i], heap[i + 1])
        val r = (0.0105f + 0.002f * ((i / 2) % 3)) * u
        inkedCircle(c, r, if ((i / 2) % 2 == 0) Color(0xFFFFF1B0) else Color(0xFFFFE9A8), pen, shade = false)
        drawCircle(Color(0xFFE8B84A), r * 0.3f, Offset(c.x + r * 0.2f, c.y + r * 0.1f))
        i += 2
    }
    // One kernel pops up and down, out of step with the next.
    for (k in 0 until 2) {
        val ph = fxFrac(t * 0.45f + k * 0.5f)
        val h = sin(ph * PI.toFloat()) * 0.05f
        if (ph < 0.8f) {
            val c = p(-0.01f + k * 0.03f + (ph - 0.4f) * 0.03f, -0.05f - h)
            drawCircle(Color(0xFFFFF1B0), 0.006f * u, c)
            drawCircle(Ink.line, 0.006f * u, c, style = pen.thin)
        }
    }
    inked(body, Color(0xFFF2EEE6), pen)
    clipPath(body) {
        for (k in 0..3) drawRect(Color(0xFFD2443A), p(-0.034f + k * 0.0175f, -0.032f), Size(0.0088f * u, 0.032f * u))
    }
    drawPath(body, Ink.line, style = pen.stroke)
    drawLine(Ink.line, p(-0.036f, -0.032f), p(0.036f, -0.032f), pen.lw * 1.4f, StrokeCap.Round)
}

internal fun DrawScope.grGlobe(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    grShadow(u, 0.12f, 0.08f)
    // Three curved wooden legs and a stem.
    for (s in -1..1) {
        val leg = Path().apply {
            moveTo(0f, -0.07f * u)
            quadraticTo(s * 0.03f * u, -0.04f * u, s * 0.05f * u, 0f)
        }
        drawPath(leg, Ink.line, style = Stroke(0.01f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(leg, GrC.walnutLight, style = Stroke(0.01f * u, cap = StrokeCap.Round))
    }
    val c = p(0f, -0.145f)
    val r = 0.06f * u
    // The sphere, tilted, with continents that turn with the spin.
    rotate(-23f, c) {
        drawCircle(Ink.shadow, r + 0.004f * u, Offset(c.x + 0.004f * u, c.y + 0.004f * u))
        drawCircle(Color(0xFF4F9BD6), r, c)
        val sphere = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
        clipPath(sphere) {
            val ang = f.angle
            val land = Color(0xFF6FAE5A)
            val continents = arrayOf(
                floatArrayOf(0.4f, 0.3f, 0.55f), floatArrayOf(1.9f, 0.1f, 0.5f), floatArrayOf(3.1f, -0.3f, 0.45f),
                floatArrayOf(4.4f, 0.4f, 0.4f), floatArrayOf(5.5f, -0.2f, 0.5f), floatArrayOf(2.5f, 0.85f, 0.3f), floatArrayOf(0.2f, -0.85f, 0.45f),
            )
            for (cn in continents) {
                val lon = cn[0] + ang
                val lat = cn[1]
                val vis = cos(lon)
                if (vis <= -0.2f) continue
                val x = c.x + sin(lon) * cos(lat) * r
                val y = c.y - sin(lat) * r
                val w = cn[2] * r * max(0.1f, vis)
                val h = cn[2] * r * 0.8f
                drawOval(land, Offset(x - w, y - h), Size(w * 2f, h * 2f))
                drawOval(land.darken(0.2f), Offset(x - w * 0.5f, y), Size(w, h * 0.7f))
            }
            // Meridians that swing across.
            for (k in 0 until 3) {
                val lon = k * PI.toFloat() / 3f + ang
                val wd = abs(cos(lon)) * r
                drawOval(Color.White.copy(alpha = 0.25f), Offset(c.x - wd, c.y - r), Size(wd * 2f, r * 2f), style = Stroke(pen.lw * 0.5f))
            }
            drawLine(Color.White.copy(alpha = 0.3f), Offset(c.x - r, c.y), Offset(c.x + r, c.y), pen.lw * 0.5f)
            // Shade on the lower right, light on the upper left.
            drawCircle(Brush.radialGradient(0f to Color.Transparent, 1f to Ink.line.copy(alpha = 0.3f), center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.5f), r, c)
        }
        drawCircle(Ink.line, r, c, style = pen.stroke)
        shine(Offset(c.x - r * 0.4f, c.y - r * 0.45f), r * 0.2f, r * 0.34f, 0.65f)
        // The brass meridian ring and its poles.
        drawArc(GrC.brass, -80f, 160f, false, Offset(c.x - r * 1.12f, c.y - r * 1.12f), Size(r * 2.24f, r * 2.24f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
        drawLine(GrC.brass, Offset(c.x, c.y - r * 1.12f), Offset(c.x, c.y + r * 1.12f), 0.003f * u)
        grKnob(Offset(c.x, c.y - r * 1.12f), 0.006f * u, pen)
    }
    // The stand's ring under the globe.
    drawOval(GrC.brass, Offset(c.x - 0.012f * u, c.y + r * 1.0f), Size(0.024f * u, 0.01f * u))
    if (f.angleV > 3f) twinkle(p(0.06f, -0.2f), 0.014f * u, Color.White, 0.8f)
}

// ------------------------------------------------------------------------------------------------ the TV

internal fun DrawScope.grTv(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.13f
    val film = f.count == 1
    val mode = f.mode.mod(6)
    val on = f.on || film
    grShadow(u, 0.3f, d)
    // The glow in front of the screen.
    if (on) grGlow(p(0f, -0.2f), 0.26f * u, pen, 0.12f, if (film) Color(0xFFFFE0A8) else tvTint(mode))
    // The low walnut console on slim legs, with two cupboard doors.
    for ((x, z) in listOf(-0.125f to 0.02f, 0.125f to 0.02f, -0.125f to d - 0.02f, 0.125f to d - 0.02f)) {
        capsule(q(x, -0.04f, z), q(x * 1.06f, 0f, z), 0.008f * u, GrC.walnutDark, pen)
    }
    fxBox(u, -0.15f, -0.115f, 0.15f, -0.036f, d, GrC.walnut, pen, rad = 0.006f)
    fxGrain(Rect(-0.145f * u, -0.11f * u, 0.145f * u, -0.041f * u), GrC.walnut, pen, 2)
    drawRoundRect(GrC.walnutDark, p(-0.14f, -0.106f), Size(0.136f * u, 0.062f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
    drawRoundRect(GrC.walnutDark, p(0.004f, -0.106f), Size(0.136f * u, 0.062f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
    grKnob(p(-0.012f, -0.075f), 0.004f * u, pen)
    grKnob(p(0.012f, -0.075f), 0.004f * u, pen)
    // The thin set on its little foot.
    capsule(q(0f, -0.118f, 0.05f), q(0f, -0.145f, 0.05f), 0.01f * u, Color(0xFF2E2B36), pen)
    fxBox(u, -0.05f, -0.15f, 0.05f, -0.118f, 0.02f, GrC.iron, pen, rad = 0.004f, z = 0.04f)
    val bezelRect = Rect(-0.135f * u, -0.335f * u, 0.135f * u, -0.14f * u)
    val o = q(0f, 0f, 0.045f)
    translate(o.x, o.y) {
        val bezel = roundPath(bezelRect, 0.012f * u)
        drawPath(bezel, Color(0xFF1B1B26))
        drawPath(bezel, Ink.line, style = pen.stroke)
        val inner = Rect(bezelRect.left + 0.01f * u, bezelRect.top + 0.01f * u, bezelRect.right - 0.01f * u, bezelRect.bottom - 0.01f * u)
        val screen = roundPath(inner, 0.006f * u)
        clipPath(screen) {
            if (film) tvFilm(inner, u, pen) else tvScreen(mode, inner, u, pen)
            // Scan lines and a glassy sheen.
            if (on) for (k in 1 until 14) {
                val y = inner.top + inner.height * k / 14f
                drawLine(Color.Black.copy(alpha = 0.06f), Offset(inner.left, y), Offset(inner.right, y), pen.lw * 0.5f)
            }
        }
        drawPath(screen, Ink.line, style = pen.thin)
        drawArc(Color.White.copy(alpha = if (on) 0.25f else 0.12f), 195f, 60f, false, Offset(inner.left + inner.width * 0.06f, inner.top + inner.height * 0.08f), Size(inner.width * 0.55f, inner.height * 0.6f), style = Stroke(0.004f * u, cap = StrokeCap.Round))
        // The tiny standby light.
        drawCircle(if (on) Color(0xFF3BC46B) else Color(0xFFD2443A), 0.003f * u, Offset(inner.right - 0.012f * u, bezelRect.bottom - 0.004f * u))
    }
}

private fun tvTint(mode: Int): Color = when (mode) {
    1 -> Color(0xFFE56FD8)
    2 -> Color(0xFF6F8CFF)
    3 -> Color(0xFF4FB3F0)
    4 -> Color(0xFFFFB347)
    else -> Color(0xFFDCE9FF)
}

private fun DrawScope.tvScreen(mode: Int, r: Rect, u: Float, pen: Pen) {
    val t = pen.t
    val w = r.width
    val h = r.height
    fun q(fx: Float, fy: Float) = Offset(r.left + fx * w, r.top + fy * h)
    when (mode) {
        0 -> {
            drawRect(Brush.linearGradient(listOf(Color(0xFF222633), Color(0xFF12141C)), r.topLeft, r.bottomRight), r.topLeft, r.size)
        }
        1 -> {
            // Channel 1: a green troll dancing on a stage under swinging lights.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B1E7A)), r.top, r.bottom), r.topLeft, r.size)
            for (k in 0 until 3) {
                val ang = sin(t * 2f + k * 2.1f) * 0.5f
                val b = Path().apply {
                    moveTo(r.left + w * (0.2f + k * 0.3f), r.top)
                    lineTo(r.left + w * (0.2f + k * 0.3f) + sin(ang) * w * 0.35f - w * 0.06f, r.bottom)
                    lineTo(r.left + w * (0.2f + k * 0.3f) + sin(ang) * w * 0.35f + w * 0.06f, r.bottom)
                    close()
                }
                drawPath(b, listOf(Color(0xFFFFC83D), Color(0xFFFF4D6D), Color(0xFF2FD18B))[k].copy(alpha = 0.3f))
            }
            drawRect(Color(0xFF2B1B52), q(0f, 0.82f), Size(w, h * 0.18f))
            val sway = sin(t * 7f)
            val hop = abs(sin(t * 7f)) * h * 0.06f
            val troll = Color(0xFF6FD08C)
            val hip = Offset(q(0.5f, 0f).x + sway * w * 0.03f, r.top + h * 0.7f - hop)
            val neck = Offset(hip.x - sway * w * 0.02f, hip.y - h * 0.24f)
            drawLine(Ink.line, hip, neck, h * 0.12f + 3f, StrokeCap.Round)
            drawLine(troll, hip, neck, h * 0.12f, StrokeCap.Round)
            val head = Offset(neck.x, neck.y - h * 0.1f)
            drawCircle(Ink.line, h * 0.1f + 1.5f, head)
            drawCircle(troll, h * 0.1f, head)
            for (s in -1..1 step 2) drawCircle(troll, h * 0.035f, Offset(head.x + s * h * 0.11f, head.y - h * 0.03f))
            drawCircle(Color.White, h * 0.022f, Offset(head.x - h * 0.035f, head.y - h * 0.01f))
            drawCircle(Color.White, h * 0.022f, Offset(head.x + h * 0.035f, head.y - h * 0.01f))
            drawCircle(Ink.line, h * 0.011f, Offset(head.x - h * 0.035f, head.y - h * 0.01f))
            drawCircle(Ink.line, h * 0.011f, Offset(head.x + h * 0.035f, head.y - h * 0.01f))
            drawLine(Ink.line, Offset(head.x - h * 0.03f, head.y + h * 0.04f), Offset(head.x + h * 0.03f, head.y + h * 0.04f), 2f, StrokeCap.Round)
            drawLine(troll, neck, Offset(neck.x - w * 0.12f, neck.y - h * 0.08f - sway * h * 0.1f), h * 0.05f, StrokeCap.Round)
            drawLine(troll, neck, Offset(neck.x + w * 0.12f, neck.y - h * 0.08f + sway * h * 0.1f), h * 0.05f, StrokeCap.Round)
            drawLine(troll, hip, Offset(hip.x - w * 0.05f, r.top + h * 0.86f), h * 0.05f, StrokeCap.Round)
            drawLine(troll, hip, Offset(hip.x + w * 0.05f, r.top + h * 0.86f - (if (sway > 0f) hop else 0f)), h * 0.05f, StrokeCap.Round)
            for (k in 0 until 6) drawCircle(Color.White.copy(alpha = 0.4f + 0.6f * sin(t * 4f + k * 1.9f)), 1.6f, q(0.1f + k * 0.16f, 0.1f + (k % 2) * 0.1f))
        }
        2 -> {
            // Channel 2: a rocket rising through the stars, again and again.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0C1438), Color(0xFF26306A)), r.top, r.bottom), r.topLeft, r.size)
            for (k in 0 until 9) drawCircle(Color.White.copy(alpha = 0.5f + 0.5f * sin(t * 2f + k)), 1.5f, q(0.07f + k * 0.11f, 0.08f + ((k * 37) % 7) * 0.1f))
            drawCircle(Color(0xFFE8473F), h * 0.12f, q(0.82f, 0.28f))
            drawCircle(Color(0xFFB33630), h * 0.03f, q(0.8f, 0.3f))
            val ph = fxFrac(t * 0.22f)
            val ry = r.bottom + h * 0.2f - ph * (h * 1.5f)
            val rx = q(0.35f, 0f).x + sin(ph * 6f) * w * 0.02f
            val fl = Path().apply {
                moveTo(rx - w * 0.03f, ry + h * 0.14f)
                lineTo(rx, ry + h * (0.34f + 0.05f * sin(t * 30f)))
                lineTo(rx + w * 0.03f, ry + h * 0.14f)
                close()
            }
            drawPath(fl, Color(0xFFFFB02E))
            val body = Path().apply {
                moveTo(rx, ry - h * 0.16f)
                quadraticTo(rx + w * 0.05f, ry - h * 0.04f, rx + w * 0.04f, ry + h * 0.14f)
                lineTo(rx - w * 0.04f, ry + h * 0.14f)
                quadraticTo(rx - w * 0.05f, ry - h * 0.04f, rx, ry - h * 0.16f)
                close()
            }
            drawPath(body, Color(0xFFF2EEE6))
            drawPath(body, Ink.line, style = Stroke(2f))
            drawCircle(Color(0xFF4FB3F0), h * 0.035f, Offset(rx, ry - h * 0.03f))
            drawPath(Path().apply { moveTo(rx - w * 0.04f, ry + h * 0.14f); lineTo(rx - w * 0.07f, ry + h * 0.19f); lineTo(rx - w * 0.04f, ry + h * 0.07f); close() }, Color(0xFFD2443A))
            drawPath(Path().apply { moveTo(rx + w * 0.04f, ry + h * 0.14f); lineTo(rx + w * 0.07f, ry + h * 0.19f); lineTo(rx + w * 0.04f, ry + h * 0.07f); close() }, Color(0xFFD2443A))
        }
        3 -> {
            // Channel 3: fish.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF4FB8EA), Color(0xFF1C5C99)), r.top, r.bottom), r.topLeft, r.size)
            drawRect(Color(0xFFF0CF8A), q(0f, 0.88f), Size(w, h * 0.12f))
            for (k in 0 until 3) {
                val x0 = r.left + w * (0.15f + k * 0.28f)
                val weed = Path().apply {
                    moveTo(x0, r.bottom)
                    quadraticTo(x0 + sin(t * 1.5f + k) * w * 0.05f, r.top + h * 0.7f, x0 + sin(t * 1.2f + k) * w * 0.03f, r.top + h * 0.5f)
                }
                drawPath(weed, Color(0xFF2E8B57), style = Stroke(w * 0.015f, cap = StrokeCap.Round))
            }
            tvFish(Offset(r.left - w * 0.1f + fxFrac(t * 0.1f) * w * 1.2f, r.top + h * (0.35f + 0.05f * sin(t * 2f))), h * 0.1f, Color(0xFFFF9A3D), 1f)
            tvFish(Offset(r.right + w * 0.1f - fxFrac(t * 0.08f + 0.4f) * w * 1.2f, r.top + h * (0.62f + 0.04f * sin(t * 1.7f))), h * 0.085f, Color(0xFFFFC83D), -1f)
            tvFish(Offset(r.left - w * 0.1f + fxFrac(t * 0.06f + 0.7f) * w * 1.2f, r.top + h * (0.2f + 0.03f * sin(t * 2.4f))), h * 0.06f, Color(0xFFFF8FB1), 1f)
            for (k in 0 until 4) {
                val ph = fxFrac(t * 0.4f + k * 0.25f)
                drawCircle(Color.White.copy(alpha = 0.7f), 2.2f, q(0.76f + k * 0.05f, 0.85f - ph * 0.8f), style = Stroke(1.2f))
            }
        }
        4 -> {
            // Channel 4: cooking. A chef tosses a pancake, and it flies up and lands in the pan.
            drawRect(Color(0xFFFFF1D6), r.topLeft, r.size)
            drawRect(Color(0xFFE5AE7F), q(0f, 0.7f), Size(w, h * 0.3f))
            val chef = Offset(q(0.38f, 0f).x, r.top + h * 0.7f)
            drawRect(Color(0xFFF2EEE6), Offset(chef.x - w * 0.09f, chef.y - h * 0.34f), Size(w * 0.18f, h * 0.34f))
            drawCircle(Color(0xFFF9D0B0), h * 0.1f, Offset(chef.x, chef.y - h * 0.44f))
            val hat = Path().apply {
                addOval(Rect(chef.x - w * 0.08f, chef.y - h * 0.68f, chef.x + w * 0.08f, chef.y - h * 0.5f))
            }
            drawPath(hat, Color.White)
            drawPath(hat, Ink.line, style = Stroke(1.5f))
            drawCircle(Ink.line, h * 0.012f, Offset(chef.x - h * 0.035f, chef.y - h * 0.45f))
            drawCircle(Ink.line, h * 0.012f, Offset(chef.x + h * 0.035f, chef.y - h * 0.45f))
            val toss = fxFrac(t * 0.7f)
            val pan = Offset(chef.x + w * 0.16f, chef.y - h * 0.18f - sin(toss * 3.14f).coerceAtLeast(0f) * 0f)
            drawLine(Ink.line, Offset(chef.x + w * 0.06f, chef.y - h * 0.25f), pan, h * 0.035f, StrokeCap.Round)
            drawOval(Color(0xFF3A3844), Offset(pan.x - w * 0.09f, pan.y - h * 0.03f), Size(w * 0.2f, h * 0.06f))
            val py = pan.y - sin(toss * 3.14f) * h * 0.45f
            drawOval(Color(0xFFE8B04A), Offset(pan.x - w * 0.05f + 2f, py - h * 0.06f), Size(w * 0.14f, h * 0.05f))
            drawOval(Ink.line, Offset(pan.x - w * 0.05f + 2f, py - h * 0.06f), Size(w * 0.14f, h * 0.05f), style = Stroke(1.2f))
            for (k in 0 until 3) {
                val ph = fxFrac(t * 0.9f + k * 0.33f)
                drawCircle(Color.White.copy(alpha = 0.6f * (1f - ph)), h * 0.025f * (1f + ph), Offset(pan.x + sin(ph * 5f + k) * w * 0.03f, pan.y - h * 0.08f - ph * h * 0.2f))
            }
        }
        else -> {
            // Channel 5: a still picture of a snowy cabin, with snow falling very slowly.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF2A3A6E), Color(0xFFB7C7E8)), r.top, r.bottom), r.topLeft, r.size)
            drawCircle(Color(0xFFFFF0BF), h * 0.07f, q(0.78f, 0.2f))
            val snow = Path().apply {
                moveTo(r.left, r.bottom)
                lineTo(r.left, r.top + h * 0.72f)
                quadraticTo(q(0.3f, 0f).x, r.top + h * 0.62f, q(0.6f, 0f).x, r.top + h * 0.74f)
                quadraticTo(q(0.85f, 0f).x, r.top + h * 0.8f, r.right, r.top + h * 0.7f)
                lineTo(r.right, r.bottom)
                close()
            }
            drawPath(snow, Color(0xFFF4F8FF))
            val cab = Offset(q(0.36f, 0f).x, r.top + h * 0.72f)
            drawRect(Color(0xFFB8342B), Offset(cab.x - w * 0.1f, cab.y - h * 0.2f), Size(w * 0.2f, h * 0.2f))
            drawPath(Path().apply { moveTo(cab.x - w * 0.12f, cab.y - h * 0.2f); lineTo(cab.x, cab.y - h * 0.34f); lineTo(cab.x + w * 0.12f, cab.y - h * 0.2f); close() }, Color(0xFFF4F8FF))
            drawRect(Color(0xFFFFD66B), Offset(cab.x - w * 0.02f, cab.y - h * 0.15f), Size(w * 0.04f, h * 0.06f))
            for (k in 0 until 14) {
                val ph = fxFrac(t * 0.05f + k * 0.0713f)
                drawCircle(Color.White, 1.6f, Offset(r.left + w * fxFrac(k * 0.37f + sin(t * 0.3f + k) * 0.02f), r.top + ph * h))
            }
        }
    }
}

private fun DrawScope.tvFish(c: Offset, s: Float, color: Color, dir: Float) {
    val tail = Path().apply {
        moveTo(c.x - dir * s * 0.8f, c.y)
        lineTo(c.x - dir * s * 1.5f, c.y - s * 0.6f)
        lineTo(c.x - dir * s * 1.5f, c.y + s * 0.6f)
        close()
    }
    drawPath(tail, color.darken(0.1f))
    drawOval(color, Offset(c.x - s, c.y - s * 0.55f), Size(s * 2f, s * 1.1f))
    drawCircle(Ink.line, s * 0.15f, Offset(c.x + dir * s * 0.5f, c.y - s * 0.1f))
}

/** Film night: a flickering old film, black bars above and below, a little troll walking over a moonlit hill. */
private fun DrawScope.tvFilm(r: Rect, u: Float, pen: Pen) {
    val t = pen.t
    val w = r.width
    val h = r.height
    drawRect(Color(0xFF2A1F14), r.topLeft, r.size)
    val flick = 0.85f + 0.15f * sin(t * 37f) + 0.05f * sin(t * 11f)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF6B5A8A), Color(0xFFE8C98A)), r.top + h * 0.15f, r.bottom - h * 0.15f), Offset(r.left, r.top + h * 0.15f), Size(w, h * 0.7f), alpha = flick)
    drawCircle(Color(0xFFFFF6D6), h * 0.08f, Offset(r.left + w * 0.72f, r.top + h * 0.35f), alpha = flick)
    val hill = Path().apply {
        moveTo(r.left, r.bottom - h * 0.15f)
        lineTo(r.left, r.top + h * 0.62f)
        quadraticTo(r.left + w * 0.4f, r.top + h * 0.45f, r.right, r.top + h * 0.66f)
        lineTo(r.right, r.bottom - h * 0.15f)
        close()
    }
    drawPath(hill, Color(0xFF2B2140), alpha = flick)
    // The troll walks across the hill again and again.
    val ph = fxFrac(t * 0.12f)
    val x = r.left + w * (-0.1f + ph * 1.2f)
    val y = r.top + h * 0.6f - sin(ph * 3.1416f) * h * 0.04f - abs(sin(t * 6f)) * h * 0.03f
    drawCircle(Color(0xFF8ED08A), h * 0.045f, Offset(x, y - h * 0.09f))
    drawRect(Color(0xFF8ED08A), Offset(x - h * 0.03f, y - h * 0.07f), Size(h * 0.06f, h * 0.08f))
    drawCircle(Color(0xFF1B1530), h * 0.007f, Offset(x + h * 0.012f, y - h * 0.095f))
    // Scratches and dust on the old film.
    val s1 = fxFrac(t * 3.1f)
    drawLine(Color.White.copy(alpha = 0.25f), Offset(r.left + w * s1, r.top), Offset(r.left + w * s1 + 2f, r.bottom), 1.2f)
    for (k in 0 until 4) drawCircle(Color.White.copy(alpha = 0.3f * fxFrac(t * 5f + k * 0.37f)), 1.6f, Offset(r.left + w * fxFrac(k * 0.31f + t * 0.7f), r.top + h * fxFrac(k * 0.57f + t * 1.3f)))
    // The black bars.
    drawRect(Color(0xFF0C0A12), r.topLeft, Size(w, h * 0.15f))
    drawRect(Color(0xFF0C0A12), Offset(r.left, r.bottom - h * 0.15f), Size(w, h * 0.15f))
}

// ------------------------------------------------------------------------------------------------ the aquarium

internal fun DrawScope.grAquarium(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val w = 0.44f
    val h = 0.2f
    // A walnut frame, a glass front, a cool light strip under a hood on top.
    val frame = Rect(-w / 2f * u, -h * u, w / 2f * u, 0f)
    box3d(frame, 0.04f * u, GrC.walnut, pen, radius = 0.008f * u)
    val inner = Rect(frame.left + 0.012f * u, frame.top + 0.03f * u, frame.right - 0.012f * u, frame.bottom - 0.014f * u)
    val water = Brush.verticalGradient(listOf(Color(0xFF7FD3E8), Color(0xFF2F8FB8)), inner.top, inner.bottom)
    drawRect(water, inner.topLeft, inner.size)
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        val t = pen.t
        // Light rays from the top.
        for (k in 0 until 4) {
            val x = inner.left + inner.width * (0.15f + k * 0.23f) + sin(t * 0.4f + k) * 0.01f * u
            drawPath(Path().apply { moveTo(x, inner.top); lineTo(x + 0.03f * u, inner.top); lineTo(x + 0.07f * u, inner.bottom); lineTo(x + 0.02f * u, inner.bottom); close() }, Color.White, alpha = 0.07f)
        }
        // Gravel, a castle, plants.
        drawRect(Color(0xFFE8D6A8), Offset(inner.left, inner.bottom - 0.022f * u), Size(inner.width, 0.022f * u))
        for (k in 0 until 14) drawCircle(listOf(Color(0xFFC9A46A), Color(0xFFFFFFFF), Color(0xFFA8B4C8))[k % 3], 0.003f * u, Offset(inner.left + inner.width * (k + 0.5f) / 14f, inner.bottom - 0.011f * u))
        val castle = Offset(inner.left + inner.width * 0.76f, inner.bottom - 0.02f * u)
        inkedRound(Rect(castle.x - 0.025f * u, castle.y - 0.05f * u, castle.x + 0.025f * u, castle.y), 0.003f * u, Color(0xFFB5AFA5), pen, shade = false)
        for (k in 0..2) drawRect(Color(0xFFB5AFA5), Offset(castle.x - 0.025f * u + k * 0.02f * u, castle.y - 0.06f * u), Size(0.01f * u, 0.012f * u))
        drawPath(archPath(castle.x - 0.008f * u, castle.x + 0.008f * u, castle.y, castle.y - 0.012f * u, castle.y - 0.024f * u), Color(0xFF2B2140))
        for (k in 0 until 3) {
            val bx = inner.left + inner.width * (0.1f + k * 0.12f)
            for (j in 0 until 3) {
                val sway = sin(t * 1.4f + k + j) * 0.006f * u
                val leaf = Path().apply {
                    moveTo(bx + (j - 1) * 0.008f * u, inner.bottom - 0.015f * u)
                    quadraticTo(bx + (j - 1) * 0.02f * u + sway, inner.bottom - 0.06f * u, bx + (j - 1) * 0.014f * u + sway * 1.5f, inner.bottom - (0.09f + 0.02f * (j % 2)) * u)
                }
                drawPath(leaf, Color(0xFF2E8B57), style = Stroke(0.007f * u, cap = StrokeCap.Round))
            }
        }
        // The fish: each swims its own loop; after a tap they gather round the finger.
        val pull = (f.timer / 3.5f * 2.2f).coerceIn(0f, 1f)
        val gather = pull * pull * (3f - 2f * pull)
        val tx = inner.center.x + f.angle * inner.width * 0.45f
        val ty = inner.center.y + f.angleV * inner.height * 0.4f
        val colors = intArrayOf(0xFFFF9A3D.toInt(), 0xFFFFC83D.toInt(), 0xFFFF6B8A.toInt(), 0xFF6FD08C.toInt(), 0xFFB57BFF.toInt(), 0xFFFFFFFF.toInt())
        for (k in 0 until 6) {
            val speed = 0.35f + 0.12f * k
            val ang = t * speed + k * 1.1f
            val ix = inner.center.x + cos(ang) * inner.width * (0.3f + 0.04f * (k % 3))
            val iy = inner.center.y + sin(ang * 1.3f) * inner.height * 0.28f
            val vx = -sin(ang) * speed
            val ring = Offset(tx + cos(t * 3f + k * 1.05f) * 0.03f * u, ty + sin(t * 3f + k * 1.05f) * 0.018f * u)
            val pos = Offset(mix(ix, ring.x, gather), mix(iy, ring.y, gather))
            val dir = if (gather > 0.5f) (if (tx > pos.x) 1f else -1f) else if (vx >= 0f) 1f else -1f
            val s = (0.016f + 0.003f * (k % 3)) * u
            fishShape(pos, s, Color(colors[k]), dir, t * 8f + k, pen)
        }
        // Bubbles from the pump.
        for (k in 0 until 4) {
            val ph = fxFrac(t * 0.35f + k * 0.25f)
            drawCircle(Color.White.copy(alpha = 0.7f * (1f - ph)), 0.0035f * u * (1f + ph * 0.5f), Offset(inner.left + inner.width * 0.9f + sin(ph * 8f + k) * 0.005f * u, inner.bottom - ph * inner.height), style = Stroke(pen.lw * 0.5f))
        }
    }
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.stroke)
    // The glass: a sheen, and the top hood with its strip light.
    drawRect(Color.White.copy(alpha = 0.12f), inner.topLeft, Size(inner.width * 0.18f, inner.height))
    inkedRound(Rect(frame.left - 0.004f * u, frame.top - 0.012f * u, frame.right + 0.004f * u, frame.top + 0.03f * u), 0.006f * u, GrC.walnutLight, pen)
    drawRect(Color(0xFFEAF7FF).copy(alpha = 0.6f + 0.4f * pen.night), Offset(frame.left + 0.03f * u, frame.top + 0.016f * u), Size(frame.width - 0.06f * u, 0.006f * u))
    grGlow(Offset(0f, -h * u * 0.5f), 0.3f * u, pen, 0.03f, Color(0xFFBFEFFF))
}

private fun DrawScope.fishShape(c: Offset, s: Float, color: Color, dir: Float, wag: Float, pen: Pen) {
    val w = sin(wag) * s * 0.25f
    val tail = Path().apply {
        moveTo(c.x - dir * s * 0.8f, c.y)
        lineTo(c.x - dir * s * 1.6f, c.y - s * 0.65f + w)
        lineTo(c.x - dir * s * 1.6f, c.y + s * 0.65f + w)
        close()
    }
    drawPath(tail, color.darken(0.12f))
    drawPath(tail, Ink.line, style = pen.thin)
    inkedOval(Rect(c.x - s, c.y - s * 0.58f, c.x + s, c.y + s * 0.58f), color, pen, shade = false)
    drawPath(Path().apply { moveTo(c.x - s * 0.2f, c.y - s * 0.55f); lineTo(c.x + dir * s * 0.2f, c.y - s * 0.95f); lineTo(c.x + dir * s * 0.5f, c.y - s * 0.5f); close() }, color.darken(0.12f))
    drawCircle(Color.White, s * 0.2f, Offset(c.x + dir * s * 0.52f, c.y - s * 0.12f))
    drawCircle(Ink.line, s * 0.1f, Offset(c.x + dir * s * 0.55f, c.y - s * 0.12f))
}

// ------------------------------------------------------------------------------------------------ floor lamps

internal fun DrawScope.grFloorLamp(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val on = f.on
    val reading = f.variant == 1
    grShadow(u, 0.1f, 0.07f)
    val shadeTop = -0.39f
    val shadeBottom = -0.29f
    if (on) {
        grGlow(p(0f, shadeBottom - 0.03f), 0.34f * u, pen, 0.3f)
        drawPath(fxDisc(0f, 0f, 0.15f * u), GrC.warmLight.copy(alpha = 0.12f + 0.14f * pen.night))
    }
    // Tripod feet, a slender brass stem, a small shelf.
    for (s in -1..1) {
        val leg = Path().apply { moveTo(0f, -0.1f * u); lineTo(s * 0.04f * u, 0f) }
        drawPath(leg, Ink.line, style = Stroke(0.007f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(leg, GrC.brass, style = Stroke(0.007f * u, cap = StrokeCap.Round))
    }
    capsule(p(0f, -0.1f), p(0f, shadeBottom - 0.01f), 0.008f * u, GrC.brass, pen)
    grKnob(p(0f, -0.2f), 0.009f * u, pen)
    // The shade: a pleated cream drum or a green banker's dome.
    if (reading) {
        val dome = Path().apply {
            moveTo(-0.05f * u, shadeBottom * u)
            quadraticTo(-0.05f * u, (shadeTop - 0.01f) * u, 0f, shadeTop * u)
            quadraticTo(0.05f * u, (shadeTop - 0.01f) * u, 0.05f * u, shadeBottom * u)
            close()
        }
        inked(dome, if (on) Color(0xFF4FA878) else Color(0xFF2E6A4A), pen)
        drawLine(GrC.brass, p(-0.05f, shadeBottom), p(0.05f, shadeBottom), pen.lw * 1.6f, StrokeCap.Round)
    } else {
        val drum = Path().apply {
            moveTo(-0.05f * u, shadeBottom * u)
            lineTo(-0.035f * u, shadeTop * u)
            lineTo(0.035f * u, shadeTop * u)
            lineTo(0.05f * u, shadeBottom * u)
            close()
        }
        inked(drum, if (on) Color(0xFFFFEDB0) else Color(0xFFF1E4CC), pen)
        for (k in 1..5) drawLine((if (on) Color(0xFFD9A93E) else Color(0xFFC9B58A)).copy(alpha = 0.7f), p(-0.05f + k * 0.0167f, shadeBottom), p(-0.035f + k * 0.0117f, shadeTop), pen.lw * 0.5f)
        drawLine(Color(0xFFD9774F), p(-0.047f, shadeBottom - 0.012f), p(0.047f, shadeBottom - 0.012f), pen.lw * 1.8f, StrokeCap.Round)
    }
    if (on) drawOval(Color.White.copy(alpha = 0.85f), Offset(-0.013f * u, (shadeBottom - 0.004f) * u), Size(0.026f * u, 0.008f * u))
    shine(p(-0.025f, (shadeTop + shadeBottom) / 2f), 0.006f * u, 0.04f * u, 0.45f)
}
