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
import app.trollfoss.domain.GroundRules
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// The hall of Storstova in oblique 3D: the grand stairs, the lift with its brass dial, the cellar door,
// the grandfather clock, the coat rack, the umbrella stand, the stained glass window, Riddar Rusten, the
// chandelier, the postal slot and the portraits. Origin at the bottom centre of each fixture's front face.

// ------------------------------------------------------------------------------------------------ the grand stairs

internal fun DrawScope.grStairs(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.2f
    val xf = 0.40f
    val xt = -0.18f
    val xl = -0.42f
    val top = -0.52f
    val n = 8
    val rise = -top / n
    val run = (xf - xt) / n
    val tread = GrC.oak
    val carpet = GrC.burgundy
    grShadow(u, 0.9f, d + 0.04f)

    // The doorway up to the first floor behind the landing, warm with light from above.
    run {
        val a = q(0f, 0f, 0.26f)
        val arch = archPath(a.x - 0.4f * u, a.x - 0.2f * u, a.y + top * u, a.y + (top - 0.16f) * u, a.y + (top - 0.33f) * u)
        drawPath(arch, Brush.verticalGradient(listOf(Color(0xFF3A2A4A), Color(0xFFFFD98A)), startY = a.y + (top - 0.33f) * u, endY = a.y + top * u))
        drawPath(arch, Ink.line, style = pen.stroke)
        drawPath(arch, GrC.ivory, style = Stroke(0.012f * u))
        drawPath(arch, Ink.line, style = pen.thin)
        grGlow(Offset(a.x - 0.3f * u, a.y + (top - 0.08f) * u), 0.16f * u, pen, 0.18f)
    }

    // Treads, risers and the landing as one solid.
    fxFace(fxFlat(u, xl, xt, top, 0f, d), tread.lighten(0.12f), pen)
    for (i in 1..n) {
        val l = xf - i * run
        val r = xf - (i - 1) * run
        val y = -i * rise
        fxFace(fxDeep(u, r, y, y + rise, 0f, d), tread.darken(0.2f), pen)
        fxFace(fxFlat(u, l, r, y, 0f, d), tread.lighten(0.1f), pen)
        // The runner and its brass rod.
        drawPath(fxDeep(u, r, y, y + rise, 0.045f, d - 0.05f), carpet.darken(0.12f))
        drawPath(fxFlat(u, l, r, y, 0.045f, d - 0.05f), carpet)
        drawLine(Ink.line.copy(alpha = 0.4f), q(l + run * 0.5f, y, 0.045f), q(l + run * 0.5f, y, d - 0.05f), pen.lw * 0.5f)
        val rodA = q(r, y, 0.04f)
        val rodB = q(r, y, d - 0.04f)
        drawLine(Ink.line, rodA, rodB, pen.lw * 2.6f, StrokeCap.Round)
        drawLine(GrC.brass, rodA, rodB, pen.lw * 1.2f, StrokeCap.Round)
    }
    // A rounded first step, because it is a grand stair.
    fxBox(u, xf - 0.005f, -rise, xf + 0.07f, 0f, d * 0.8f, tread, pen, rad = 0.03f, z = 0.02f, top = tread.lighten(0.15f), side = tread.darken(0.25f))

    // The front of the stringer: dark walnut with a bevelled line along the slope.
    val face = Path().apply {
        moveTo(xl * u, top * u)
        lineTo(xt * u, top * u)
        for (i in n downTo 1) {
            lineTo((xf - (i - 1) * run) * u, -i * rise * u)
            lineTo((xf - (i - 1) * run) * u, -(i - 1) * rise * u)
        }
        lineTo(xl * u, 0f)
        close()
    }
    inked(face, GrC.walnut, pen)
    clipPath(face) {
        val bevel = Path().apply {
            moveTo(xl * u, (top + 0.04f) * u)
            lineTo(xt * u, (top + 0.04f) * u)
            for (i in n downTo 1) lineTo((xf - (i - 0.5f) * run) * u, (-i * rise + 0.04f) * u)
        }
        drawPath(bevel, GrC.walnutLight, style = pen.thin)
        // Panels under the landing, and a row of small ones along the slope.
        for (k in 0 until 3) {
            val px = xl + 0.02f + k * 0.075f
            drawRect(GrC.walnutDark, Offset(px * u, (top + 0.07f) * u), Size(0.065f * u, 0.4f * u), style = pen.thin)
        }
        for (i in 1..n step 2) {
            val cx = xf - (i - 0.5f) * run
            val topY = -i * rise + 0.06f
            drawRect(GrC.walnutDark, Offset((cx - run * 0.38f) * u, topY * u), Size(run * 0.76f * u, (-topY - 0.01f).coerceAtLeast(0.02f) * u), style = pen.thin)
        }
    }

    // The balustrade: ivory posts, a walnut handrail, and the big newel posts with a lantern at the foot.
    val rail = GrC.walnutDark
    fun railY(i: Int) = -i * rise - 0.115f
    for (i in 1..n) {
        val cx = xf - (i - 0.5f) * run
        val a = q(cx, -i * rise, 0.01f)
        capsule(a, Offset(a.x, a.y - 0.108f * u), 0.0065f * u, GrC.ivory, pen)
    }
    var lx = xl + 0.05f
    while (lx < xt) {
        val a = q(lx, top, 0.01f)
        capsule(a, Offset(a.x, a.y - 0.108f * u), 0.0065f * u, GrC.ivory, pen)
        lx += 0.045f
    }
    val h0 = q(xl + 0.02f, top - 0.115f, 0.01f)
    val h1 = q(xt, top - 0.115f, 0.01f)
    val h2 = q(xf - 0.5f * run, railY(1), 0.01f)
    val hStart = q(xf - (n - 0.5f) * run, railY(n), 0.01f)
    capsule(h0, h1, 0.013f * u, rail, pen)
    capsule(h1, hStart, 0.013f * u, rail, pen)
    capsule(hStart, h2, 0.013f * u, rail, pen)
    capsule(h2, q(xf + 0.03f, railY(1) + 0.01f, 0.01f), 0.013f * u, rail, pen)
    // Newel posts: carved, with brass balls.
    for (x in floatArrayOf(xl + 0.02f, xt)) {
        fxBox(u, x - 0.016f, top - 0.2f, x + 0.016f, top, 0.03f, rail, pen, rad = 0.004f, z = 0f)
        val c = q(x, top - 0.213f, 0.015f)
        grKnob(c, 0.012f * u, pen)
    }
    // The grand newel at the foot, with a glass lantern that glows in the evening.
    run {
        val x = xf + 0.035f
        fxBox(u, x - 0.022f, -0.22f, x + 0.022f, 0f, 0.04f, rail, pen, rad = 0.005f, z = 0f)
        val c = q(x, -0.245f, 0.02f)
        inkedCircle(c, 0.022f * u, GrC.brass, pen, shade = false)
        val globe = Offset(c.x, c.y - 0.04f * u)
        grGlow(globe, 0.11f * u, pen, 0.12f)
        drawCircle(lerp(Color(0xFFF6F1D8), Color(0xFFFFE9A8), 0.25f + 0.75f * pen.night), 0.024f * u, globe)
        drawCircle(Ink.line, 0.024f * u, globe, style = pen.thin)
        shine(Offset(globe.x - 0.007f * u, globe.y - 0.008f * u), 0.008f * u, 0.011f * u, 0.8f)
        grKnob(Offset(globe.x, globe.y - 0.031f * u), 0.007f * u, pen)
    }
}

// ------------------------------------------------------------------------------------------------ the lift

internal fun DrawScope.grLift(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val open = f.anim.coerceIn(0f, 1f)
    grShadow(u, 0.34f, 0.1f)
    // The frame: a brass arch round a cream wall, with a lamp-lit dial in the arch.
    fxBox(u, -0.16f, -0.44f, 0.16f, 0f, 0.07f, GrC.cream, pen, rad = 0.012f)
    val frame = Path().apply {
        moveTo(-0.135f * u, 0f)
        lineTo(-0.135f * u, -0.285f * u)
        lineTo(0.135f * u, -0.285f * u)
        lineTo(0.135f * u, 0f)
        close()
    }
    drawPath(frame, GrC.brass)
    drawPath(frame, Ink.line, style = pen.stroke)
    // The shaft inside: warm wood and a lamp.
    val inner = Rect(-0.115f * u, -0.268f * u, 0.115f * u, 0f)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF8A5A3C), Color(0xFFD9A873)), startY = inner.top, endY = inner.bottom), inner.topLeft, inner.size)
    drawLine(Ink.line.copy(alpha = 0.4f), Offset(inner.center.x, inner.top), Offset(inner.center.x, inner.bottom), pen.lw * 0.7f)
    grGlow(Offset(0f, -0.24f * u), 0.12f * u, pen, 0.45f)
    // A little bench seen through the opening.
    inkedRound(Rect(-0.07f * u, -0.095f * u, 0.07f * u, -0.075f * u), 0.004f * u, GrC.burgundy, pen, shade = false)
    // The two sliding doors, closed or opening with a tap.
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        for (s in 0..1) {
            val dir = if (s == 0) -1f else 1f
            val wLeaf = 0.115f * u
            val x0 = if (s == 0) inner.left else 0f
            val slide = open * wLeaf * 0.92f * dir
            val leaf = Rect(x0 + slide, inner.top, x0 + wLeaf + slide, inner.bottom)
            inkedRound(leaf, 0.003f * u, GrC.silver, pen, shade = false)
            drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent), startY = leaf.top, endY = leaf.bottom), leaf.topLeft, leaf.size)
            val inset = Rect(leaf.left + 0.012f * u, leaf.top + 0.02f * u, leaf.right - 0.012f * u, leaf.bottom - 0.02f * u)
            drawRect(Ink.line.copy(alpha = 0.35f), inset.topLeft, inset.size, style = pen.thin)
            grKnob(Offset(if (s == 0) leaf.right - 0.014f * u else leaf.left + 0.014f * u, leaf.center.y), 0.006f * u, pen)
        }
    }
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.stroke)
    // The dial in the arch above the doors: a brass half-moon with four marks and a needle that wobbles.
    val c = p(0f, -0.345f)
    val r = 0.058f * u
    drawArc(Ink.line, 180f, 180f, true, Offset(c.x - r - pen.lw, c.y - r - pen.lw), Size((r + pen.lw) * 2f, (r + pen.lw) * 2f))
    drawArc(GrC.brass, 180f, 180f, true, Offset(c.x - r, c.y - r), Size(r * 2f, r * 2f))
    drawArc(GrC.ivory, 180f, 180f, true, Offset(c.x - r * 0.82f, c.y - r * 0.82f), Size(r * 1.64f, r * 1.64f))
    for (k in 0..4) {
        val a = PI.toFloat() * (1f + k / 4f)
        val inn = Offset(c.x + cos(a) * r * 0.62f, c.y + sin(a) * r * 0.62f)
        val out = Offset(c.x + cos(a) * r * 0.78f, c.y + sin(a) * r * 0.78f)
        drawLine(Ink.line, inn, out, pen.lw * 1.3f, StrokeCap.Round)
        if (k == 0 || k == 4) drawCircle(GrC.green, r * 0.07f, Offset(c.x + cos(a) * r * 0.5f, c.y + sin(a) * r * 0.5f))
    }
    // The needle rests at the ground floor, jumps when the lift is called and wobbles a little in between.
    val needle = PI.toFloat() * (1.06f + 0.5f * open)
    drawLine(Ink.line, c, Offset(c.x + cos(needle) * r * 0.7f, c.y + sin(needle) * r * 0.7f), pen.lw * 1.6f, StrokeCap.Round)
    drawLine(GrC.burgundy, c, Offset(c.x + cos(needle) * r * 0.7f, c.y + sin(needle) * r * 0.7f), pen.lw * 0.8f, StrokeCap.Round)
    grKnob(c, 0.008f * u, pen)
    // Call buttons beside the door.
    for (k in 0..1) {
        val b = p(0.15f, -0.15f - k * 0.05f)
        inkedCircle(b, 0.008f * u, GrC.brass, pen, shade = false)
        if (k == 0) drawCircle(GrC.warmLight.copy(alpha = 0.8f), 0.004f * u, b)
    }
}

// ------------------------------------------------------------------------------------------------ doors

/** The cellar door under the stairs (variant 0) and the glass door to the garden (variant 1). */
internal fun DrawScope.grDoor(f: Fixture, u: Float, pen: Pen) {
    if (f.variant == 0) grCellarDoor(f, u, pen) else grGardenDoor(f, u, pen)
}

private fun DrawScope.grCellarDoor(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val open = f.anim.coerceIn(0f, 1f)
    val h = 0.34f
    val w = 0.1f
    // Frame and the dark stairs down.
    val frame = archPath(-(w + 0.012f) * u, (w + 0.012f) * u, 0f, -(h - 0.07f) * u, -(h + 0.012f) * u)
    drawPath(frame, GrC.ivory)
    drawPath(frame, Ink.line, style = pen.stroke)
    val hole = archPath(-w * u, w * u, 0f, -(h - 0.07f) * u, -h * u)
    drawPath(hole, Brush.verticalGradient(listOf(Color(0xFF1B1530), Color(0xFF3E3050)), startY = -h * u, endY = 0f))
    clipPath(hole) {
        for (k in 0..4) {
            val y = -0.02f - k * 0.045f
            drawLine(Color(0xFF6A5A7A), p(-w, y), p(w, y), pen.lw * 1.2f)
            drawLine(Color(0xFF2A2040), p(-w, y + 0.012f), p(w, y + 0.012f), pen.lw * 0.8f)
        }
        // A pair of friendly eyes in the dark: the cellar says hello.
        if (open > 0.2f) {
            drawCircle(Color(0xFFFFE9A8), 0.007f * u, p(-0.025f, -0.17f))
            drawCircle(Color(0xFFFFE9A8), 0.007f * u, p(0.025f, -0.17f))
        }
    }
    // The door leaf, swinging open with the tap (it narrows as it turns away).
    val leafW = 2f * w * (1f - 0.7f * open)
    val leaf = Path().apply {
        moveTo(-w * u, 0f)
        lineTo(-w * u, -(h - 0.07f) * u)
        quadraticTo(-w * u, -h * u, (-w + leafW * 0.5f) * u, -h * u * (1f - 0.03f * open))
        quadraticTo((-w + leafW) * u, -(h) * u, (-w + leafW) * u, -(h - 0.07f) * u * (1f - 0.05f * open))
        lineTo((-w + leafW) * u, 0f)
        close()
    }
    inked(leaf, GrC.walnut, pen)
    clipPath(leaf) {
        for (k in 1..3) drawLine(GrC.walnutDark, p(-w + leafW * k / 4f, -h), p(-w + leafW * k / 4f, 0f), pen.lw * 0.7f)
        drawLine(GrC.iron, p(-w, -0.09f), p(-w + leafW, -0.09f), pen.lw * 2f)
        drawLine(GrC.iron, p(-w, -0.24f), p(-w + leafW, -0.24f), pen.lw * 2f)
    }
    grKnob(p(-w + leafW - 0.02f, -0.15f), 0.008f * u, pen)
    // A little brass plaque with stairs on it, above the door.
    val pl = Rect(-0.03f * u, -(h + 0.05f) * u, 0.03f * u, -(h + 0.014f) * u)
    inkedRound(pl, 0.004f * u, GrC.brass, pen, shade = false)
    val st = Path().apply {
        moveTo(pl.left + 0.008f * u, pl.bottom - 0.006f * u)
        lineTo(pl.left + 0.016f * u, pl.bottom - 0.006f * u)
        lineTo(pl.left + 0.016f * u, pl.center.y)
        lineTo(pl.left + 0.025f * u, pl.center.y)
        lineTo(pl.left + 0.025f * u, pl.top + 0.006f * u)
        lineTo(pl.left + 0.034f * u, pl.top + 0.006f * u)
    }
    drawPath(st, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
}

private fun DrawScope.grGardenDoor(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val open = f.anim.coerceIn(0f, 1f)
    val h = 0.44f
    val w = 0.105f
    val night = pen.night
    val outer = archPath(-(w + 0.014f) * u, (w + 0.014f) * u, 0f, -(h - 0.09f) * u, -(h + 0.014f) * u)
    drawPath(outer, Color(0xFFF2F6EE))
    drawPath(outer, Ink.line, style = pen.stroke)
    val glass = archPath(-w * u, w * u, 0f, -(h - 0.09f) * u, -h * u)
    // Garden behind the glass: sky, a green hedge, a path.
    drawPath(glass, lerp(Color(0xFFB8E4F6), Color(0xFF1F2A60), night))
    clipPath(glass) {
        drawRect(lerp(Color(0xFF7BC96B), Color(0xFF244A3E), night), p(-w, -0.2f), Size(2f * w * u, 0.2f * u))
        drawOval(lerp(Color(0xFF5DAE5A), Color(0xFF1E4538), night), p(-w - 0.02f, -0.24f), Size(0.14f * u, 0.12f * u))
        drawOval(lerp(Color(0xFF4F9C52), Color(0xFF1E4538), night), p(0.03f, -0.27f), Size(0.14f * u, 0.14f * u))
        drawRect(lerp(Color(0xFFE9D9A8), Color(0xFF55507A), night), p(-0.03f, -0.1f), Size(0.06f * u, 0.1f * u))
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), startY = -h * u, endY = -0.2f * u), p(-w, -h), Size(2f * w * u, 0.25f * u))
        // The bars of the glass.
        val bars = Color(0xFFF2F6EE)
        drawLine(bars, p(0f, -h), p(0f, 0f), pen.lw * 1.5f)
        for (k in 1..3) drawLine(bars, p(-w, -0.1f * k), p(w, -0.1f * k), pen.lw * 1.5f)
        for (k in 1..2) {
            val a = PI.toFloat() * (1f - k / 3f)
            drawLine(bars, p(0f, -(h - 0.09f)), p(cos(a) * w, -(h - 0.09f) - sin(a) * 0.09f), pen.lw * 1.4f)
        }
    }
    drawPath(glass, Ink.line, style = pen.thin)
    // The leaf: a thin white frame that swings in a little when it is tapped.
    if (open > 0.02f) {
        val shut = Path().apply {
            moveTo(w * u, 0f)
            lineTo(w * u, -(h - 0.09f) * u)
            lineTo((w - 0.05f * open) * u, -(h - 0.08f) * u)
            lineTo((w - 0.05f * open) * u, -0.02f * u)
            close()
        }
        drawPath(shut, Color(0xFFD7E3D6).copy(alpha = 0.75f))
        drawPath(shut, Ink.line, style = pen.thin)
    }
    grKnob(p(w - 0.02f, -0.2f), 0.008f * u, pen)
    // A brass step.
    inkedRound(Rect(-(w + 0.02f) * u, -0.008f * u, (w + 0.02f) * u, 0.012f * u), 0.004f * u, GrC.brass, pen, shade = false)
    // The glow of the day or the lamps of the garden.
    if (night < 0.7f) grGlow(p(0f, -0.2f), 0.18f * u, pen, 0.1f, Color(0xFFE6FFF0))
}

// ------------------------------------------------------------------------------------------------ the grandfather clock

internal fun DrawScope.grClock(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.075f
    grShadow(u, 0.13f, d)
    val case = GrC.walnut
    // Base, trunk and hood.
    fxBox(u, -0.062f, -0.07f, 0.062f, 0f, d, case, pen, rad = 0.006f)
    fxBox(u, -0.045f, -0.285f, 0.045f, -0.065f, d - 0.015f, case.lighten(0.05f), pen, rad = 0.004f, z = 0.008f)
    fxBox(u, -0.056f, -0.44f, 0.056f, -0.285f, d, case, pen, rad = 0.008f)
    // Base panel and a carved crest.
    drawRect(GrC.walnutDark, p(-0.04f, -0.055f), Size(0.08f * u, 0.04f * u), style = pen.thin)
    // The trunk window with the swinging pendulum.
    val win = Rect(-0.03f * u, -0.265f * u, 0.03f * u, -0.095f * u)
    val glass = roundPath(win, 0.014f * u)
    drawPath(glass, Color(0xFFE9D9A8))
    clipPath(glass) {
        val swing = sin(pen.t * PI.toFloat()) * (0.4f + f.angleV * 0.3f)
        val pivot = Offset(0f, win.top)
        rotate(swing * 57.3f * 0.35f, pivot) {
            drawLine(Ink.line, pivot, Offset(0f, win.bottom - 0.03f * u), pen.lw * 1.2f)
            val c = Offset(0f, win.bottom - 0.035f * u)
            inkedCircle(c, 0.021f * u, GrC.brass, pen, shade = false)
            drawCircle(GrC.brassDark, 0.012f * u, c, style = pen.thin)
            shine(Offset(c.x - 0.007f * u, c.y - 0.007f * u), 0.008f * u, 0.011f * u, 0.7f)
        }
    }
    drawPath(glass, Ink.line, style = pen.stroke)
    drawPath(glass, Color.White.copy(alpha = 0.3f), style = Stroke(pen.lw * 0.5f))
    // The face: cream dial, twelve dots, two hands, set by the hour it last struck.
    val c = p(0f, -0.365f)
    val r = 0.036f * u
    inkedCircle(c, r + 0.006f * u, GrC.brass, pen, shade = false)
    inkedCircle(c, r, GrC.ivory, pen, shade = false)
    for (k in 0 until 12) {
        val a = k * PI.toFloat() / 6f
        drawCircle(Ink.line, if (k % 3 == 0) 0.0035f * u else 0.002f * u, Offset(c.x + sin(a) * r * 0.82f, c.y - cos(a) * r * 0.82f))
    }
    val hour = (f.count % 12) * PI.toFloat() / 6f
    val minute = pen.t * 0.12f
    drawLine(Ink.line, c, Offset(c.x + sin(hour) * r * 0.5f, c.y - cos(hour) * r * 0.5f), pen.lw * 2f, StrokeCap.Round)
    drawLine(Ink.line, c, Offset(c.x + sin(minute) * r * 0.78f, c.y - cos(minute) * r * 0.78f), pen.lw * 1.2f, StrokeCap.Round)
    drawCircle(Ink.line, 0.004f * u, c)
    // A little crown and ball on the hood.
    val crown = Path().apply {
        moveTo(-0.056f * u, -0.44f * u)
        quadraticTo(-0.05f * u, -0.475f * u, 0f, -0.485f * u)
        quadraticTo(0.05f * u, -0.475f * u, 0.056f * u, -0.44f * u)
        close()
    }
    inked(crown, GrC.walnutLight, pen)
    grKnob(p(0f, -0.492f), 0.01f * u, pen)
    // Feet.
    for (s in 0..1) inkedRound(Rect((-0.066f + s * 0.1f) * u, -0.01f * u, (-0.034f + s * 0.1f) * u, 0.004f * u), 0.003f * u, GrC.walnutDark, pen, shade = false)
    // A shimmer when it has just struck.
    if (f.angleV > 0.05f) twinkle(p(0.04f, -0.42f), 0.02f * u * f.angleV, Color.White, f.angleV)
}

// ------------------------------------------------------------------------------------------------ coat rack

internal fun DrawScope.grCoatRack(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    // A walnut board with a carved top, brass pegs, and the hats of the house hanging in a row.
    val board = Path().apply {
        moveTo(-0.15f * u, -0.115f * u)
        lineTo(-0.15f * u, -0.17f * u)
        quadraticTo(-0.15f * u, -0.19f * u, -0.13f * u, -0.19f * u)
        lineTo(-0.04f * u, -0.19f * u)
        quadraticTo(0f, -0.215f * u, 0.04f * u, -0.19f * u)
        lineTo(0.13f * u, -0.19f * u)
        quadraticTo(0.15f * u, -0.19f * u, 0.15f * u, -0.17f * u)
        lineTo(0.15f * u, -0.115f * u)
        close()
    }
    drawPath(board, Ink.shadow, style = Stroke(0.012f * u))
    inked(board, GrC.walnut, pen)
    drawLine(GrC.walnutLight, p(-0.13f, -0.16f), p(0.13f, -0.16f), pen.lw * 0.8f)
    val hats = GroundRules.COAT_HATS
    for (i in 0 until 5) {
        val px = -0.108f + i * 0.054f
        val (type, variant) = hats[(f.count + i) % hats.size]
        val sway = 0f // (still: a swaying hat would redraw the whole rack ten times a second)
        // The peg, and the hat on it (drawn by the very same art as the loose hat).
        val peg = p(px, -0.14f)
        drawLine(Ink.line, peg, Offset(peg.x, peg.y + 0.02f * u), pen.lw * 3f, StrokeCap.Round)
        drawLine(GrC.brass, peg, Offset(peg.x, peg.y + 0.02f * u), pen.lw * 1.4f, StrokeCap.Round)
        val hatH = type.h * u * 0.9f
        val hatW = type.w * u * 0.9f
        translate(peg.x + sway, peg.y + 0.016f * u + hatH * 0.62f) {
            rotate(sway * 6f, Offset(0f, -hatH * 0.5f)) {
                drawThing(type, variant, 0, hatW, hatH, pen)
            }
        }
        grKnob(Offset(peg.x, peg.y), 0.006f * u, pen)
    }
}

// ------------------------------------------------------------------------------------------------ umbrella stand

internal fun DrawScope.grUmbrellaStand(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    grShadow(u, 0.1f, 0.07f)
    // Three umbrellas leaning in the stand: two with their canopies up, one with a crook handle.
    val cols = intArrayOf(0xFFD2443A.toInt(), 0xFF2F6FB8.toInt(), 0xFFFFC83D.toInt())
    val tilt = floatArrayOf(-0.5f, 0.12f, 0.55f)
    for (k in 0 until 3) {
        val base = p(-0.02f + k * 0.02f, -0.09f)
        val len = (0.15f + 0.02f * (k % 2)) * u
        val a = tilt[k] * 0.55f
        val tip = Offset(base.x + sin(a) * len, base.y - cos(a) * len)
        if (k == 1) {
            // The crook handle.
            drawLine(Ink.line, base, tip, 0.012f * u + pen.lw * 2f, StrokeCap.Round)
            drawLine(Color(0xFF5A3F2A), base, tip, 0.012f * u, StrokeCap.Round)
            val hook = Path().apply {
                moveTo(tip.x, tip.y)
                quadraticTo(tip.x, tip.y - 0.03f * u, tip.x + 0.02f * u, tip.y - 0.028f * u)
                quadraticTo(tip.x + 0.034f * u, tip.y - 0.026f * u, tip.x + 0.03f * u, tip.y - 0.006f * u)
            }
            drawPath(hook, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(hook, Color(0xFF5A3F2A), style = Stroke(0.012f * u, cap = StrokeCap.Round))
        } else {
            drawLine(Ink.line, base, tip, 0.005f * u + pen.lw * 2f, StrokeCap.Round)
            drawLine(GrC.steel, base, tip, 0.005f * u, StrokeCap.Round)
            // The closed canopy: a long pointed cone with a ribbed look, a tip of brass.
            val c0 = Offset(base.x + sin(a) * len * 0.45f, base.y - cos(a) * len * 0.45f)
            val nx = cos(a)
            val ny = sin(a)
            val cone = Path().apply {
                moveTo(c0.x - nx * 0.016f * u, c0.y - ny * 0.016f * u)
                lineTo(tip.x, tip.y)
                lineTo(c0.x + nx * 0.016f * u, c0.y + ny * 0.016f * u)
                quadraticTo(c0.x, c0.y + 0.012f * u, c0.x - nx * 0.016f * u, c0.y - ny * 0.016f * u)
            }
            inked(cone, Color(cols[k]), pen)
            drawLine(Color(cols[k]).darken(0.3f), Offset(c0.x, c0.y), tip, pen.lw * 0.6f)
            drawCircle(GrC.brass, 0.004f * u, tip)
        }
    }
    // The stand: a blue-and-white china cylinder with a gold rim.
    val body = Path().apply {
        moveTo(-0.045f * u, -0.095f * u)
        lineTo(0.045f * u, -0.095f * u)
        lineTo(0.04f * u, 0f)
        lineTo(-0.04f * u, 0f)
        close()
    }
    inked(body, Color(0xFFF6F2E8), pen)
    clipPath(body) {
        for (k in 0..2) {
            val y = -0.075f + k * 0.03f
            drawOval(Color(0xFF3B6EA5), Offset(-0.03f * u, y * u), Size(0.06f * u, 0.016f * u))
        }
        drawLine(Color(0xFF3B6EA5), p(-0.05f, -0.09f), p(0.05f, -0.09f), pen.lw * 1.4f)
    }
    drawLine(GrC.brass, p(-0.047f, -0.097f), p(0.047f, -0.097f), pen.lw * 2f, StrokeCap.Round)
    shine(p(-0.026f, -0.06f), 0.007f * u, 0.03f * u, 0.7f)
}

// ------------------------------------------------------------------------------------------------ the windows

internal fun DrawScope.grWindow(f: Fixture, u: Float, pen: Pen) {
    if (f.variant == 0) grStainedGlass(f, u, pen) else grTallWindow(f, u, pen)
}

private fun DrawScope.grStainedGlass(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val night = pen.night
    val w = 0.085f
    val h = 0.37f
    val outer = archPath(-(w + 0.02f) * u, (w + 0.02f) * u, 0f, -(h - 0.09f) * u, -(h + 0.02f) * u)
    drawPath(outer, GrC.stone)
    drawPath(outer, Ink.line, style = pen.stroke)
    val glass = archPath(-w * u, w * u, 0f, -(h - 0.085f) * u, -h * u)
    drawPath(glass, Color(0xFF5A4A7A))
    clipPath(glass) {
        // A mosaic: a rose at the top and diamonds of coloured glass below, lit from outside.
        val cols = arrayOf(Color(0xFFE8473F), Color(0xFFFFC83D), Color(0xFF2F9BFF), Color(0xFF2FD18B), Color(0xFFB57BFF), Color(0xFFFF8FB1))
        val glow = if (f.timer > 0f) 0.25f + 0.2f * sin(pen.t * 12f) else 0f
        for (row in 0 until 6) {
            for (col in 0 until 4) {
                val cx = -w + w * 0.5f + col * w * 0.5f + (row % 2) * 0.0f
                val cy = -0.05f - row * 0.047f
                val c = cols[(row * 3 + col * 2) % cols.size]
                val dia = Path().apply {
                    moveTo(cx * u, (cy - 0.026f) * u)
                    lineTo((cx + 0.04f) * u, cy * u)
                    lineTo(cx * u, (cy + 0.026f) * u)
                    lineTo((cx - 0.04f) * u, cy * u)
                    close()
                }
                drawPath(dia, lerp(c, Color.White, glow + (1f - night) * 0.12f))
            }
        }
        val rose = p(0f, -(h - 0.085f))
        drawCircle(Color(0xFFFFC83D), 0.052f * u, rose)
        for (k in 0 until 8) {
            val a = k * PI.toFloat() / 4f
            val pe = Path().apply {
                moveTo(rose.x, rose.y)
                lineTo(rose.x + cos(a - 0.3f) * 0.05f * u, rose.y + sin(a - 0.3f) * 0.05f * u)
                lineTo(rose.x + cos(a + 0.3f) * 0.05f * u, rose.y + sin(a + 0.3f) * 0.05f * u)
                close()
            }
            drawPath(pe, lerp(cols[(k + 2) % cols.size], Color.White, glow))
        }
        drawCircle(Color(0xFFE8473F), 0.016f * u, rose)
        // The lead lines.
        drawRect(Color.Transparent, p(-w, -h), Size(0f, 0f))
        for (k in 0..4) drawLine(Ink.line.copy(alpha = 0.7f), p(-w + k * w * 0.5f, 0f), p(-w + k * w * 0.5f, -(h - 0.12f)), pen.lw * 0.8f)
        // Daylight: a pale sheen across the glass.
        drawRect(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f * (1f - night)), Color.Transparent), p(-w, -h), p(w, -0.1f)), p(-w, -h), Size(2f * w * u, h * u))
    }
    drawPath(glass, Ink.line, style = pen.stroke)
    inkedRound(Rect(-(w + 0.03f) * u, -0.01f * u, (w + 0.03f) * u, 0.012f * u), 0.004f * u, GrC.stone, pen, shade = false)
    if (f.timer > 0f) {
        val a = (f.timer / 1.6f).coerceIn(0f, 1f)
        twinkle(p(-0.04f, -0.25f), 0.03f * u * a, Color.White, a)
        twinkle(p(0.05f, -0.15f), 0.024f * u * a, Color(0xFFFFE680), a)
    }
}

private fun DrawScope.grTallWindow(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val night = pen.night
    val w = 0.09f
    val h = 0.35f
    val closed = f.mode == 1
    val outer = archPath(-(w + 0.02f) * u, (w + 0.02f) * u, 0f, -(h - 0.09f) * u, -(h + 0.02f) * u)
    drawPath(outer, GrC.walnutLight)
    drawPath(outer, Ink.line, style = pen.stroke)
    val glass = archPath(-w * u, w * u, 0f, -(h - 0.09f) * u, -h * u)
    drawPath(glass, lerp(Color(0xFF8FD0F2), Color(0xFF1C2358), night))
    clipPath(glass) {
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, lerp(Color(0xFFE6F6FF), Color(0xFF3B3A82), night)), startY = -h * u, endY = 0f), p(-w, -h), Size(2f * w * u, h * u))
        drawOval(lerp(Color(0xFF7FC27A), Color(0xFF254A4A), night), p(-w - 0.05f, -0.1f), Size(0.2f * u, 0.14f * u))
        drawOval(lerp(Color(0xFF5DAE5A), Color(0xFF1E4538), night), p(0f, -0.07f), Size(0.18f * u, 0.12f * u))
        if (night > 0.3f) for (k in 0 until 6) drawCircle(Color(0xFFFFF7DA), 0.0025f * u, p(-0.07f + k * 0.03f, -0.3f + (k % 3) * 0.04f))
        val bars = Color(0xFFF7F3EC)
        drawLine(bars, p(0f, -h), p(0f, 0f), pen.lw * 1.5f)
        for (k in 1..2) drawLine(bars, p(-w, -0.11f * k), p(w, -0.11f * k), pen.lw * 1.5f)
    }
    drawPath(glass, Ink.line, style = pen.thin)
    inkedRound(Rect(-(w + 0.03f) * u, -0.01f * u, (w + 0.03f) * u, 0.012f * u), 0.004f * u, GrC.walnutLight, pen, shade = false)
    // Heavy curtains of green velvet, open at the sides or drawn across the glass.
    val vel = if (f.place == app.trollfoss.domain.PlaceId.MANOR_GROUND) Color(0xFF2E6A4A) else Color(0xFF2E6A4A)
    val sway = 0f
    for (s in 0..1) {
        val dir = if (s == 0) -1f else 1f
        val inner = if (closed) 0.0f else w * 0.62f
        val x0 = dir * (w + 0.03f)
        val x1 = dir * inner
        val drape = Path().apply {
            moveTo(x0 * u, -(h + 0.01f) * u)
            lineTo(x1 * u, -(h + 0.01f) * u)
            quadraticTo((x1 + dir * 0.012f) * u + sway, -0.2f * u, (x1 + dir * 0.01f) * u, -0.005f * u)
            lineTo(x0 * u, -0.005f * u)
            close()
        }
        inked(drape, vel, pen)
        for (k in 1..3) {
            val fx = mix(x0, x1, k / 4f)
            drawLine(vel.darken(0.3f), p(fx, -h), p(fx + dir * 0.004f, -0.01f), pen.lw * 0.7f)
        }
        if (!closed) {
            drawLine(GrC.brass, p(x1, -0.16f), p(dir * (w + 0.03f), -0.16f), pen.lw * 1.4f)
            grKnob(p(x1, -0.16f), 0.007f * u, pen)
        }
    }
    drawLine(Ink.line, p(-(w + 0.06f), -(h + 0.015f)), p(w + 0.06f, -(h + 0.015f)), pen.lw * 3.4f, StrokeCap.Round)
    drawLine(GrC.brass, p(-(w + 0.06f), -(h + 0.015f)), p(w + 0.06f, -(h + 0.015f)), pen.lw * 1.6f, StrokeCap.Round)
    grKnob(p(-(w + 0.065f), -(h + 0.015f)), 0.008f * u, pen)
    grKnob(p(w + 0.065f, -(h + 0.015f)), 0.008f * u, pen)
}

// ------------------------------------------------------------------------------------------------ Riddar Rusten

internal fun DrawScope.grArmour(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val action = f.bob.toInt()
    val t = f.timer
    val steel = Color(0xFFBAC4D4)
    val steelDark = Color(0xFF8E9AB2)
    val steelLight = Color(0xFFE3EAF4)
    grShadow(u, 0.15f, 0.07f)
    // A hiccup makes him jump; a clank makes him tremble.
    val jump = if (action == 3) max(0f, sin(min(1f, t / 0.5f) * PI.toFloat())) * 0.014f else 0f
    val shake = if (action == 1) sin(pen.t * 50f) * 2.4f * min(1f, t / 0.7f) else 0f
    val breath = 0f
    // The plinth.
    inkedRound(Rect(-0.065f * u, -0.03f * u, 0.065f * u, 0f), 0.004f * u, GrC.stoneDark, pen)
    inkedRound(Rect(-0.055f * u, -0.045f * u, 0.055f * u, -0.028f * u), 0.003f * u, GrC.stone, pen)
    translate(0f, -jump * u) {
        rotate(shake, p(0f, -0.045f)) {
            // The halberd in the left hand (our right): a long pole with an axe head, standing on the plinth.
            val pole0 = p(0.062f, -0.04f)
            val pole1 = p(0.062f, -0.39f)
            drawLine(Ink.line, pole0, pole1, 0.009f * u + pen.lw * 2f, StrokeCap.Round)
            drawLine(GrC.walnutLight, pole0, pole1, 0.009f * u, StrokeCap.Round)
            val axe = Path().apply {
                moveTo(0.062f * u, -0.37f * u)
                quadraticTo(0.1f * u, -0.38f * u, 0.095f * u, -0.335f * u)
                quadraticTo(0.082f * u, -0.345f * u, 0.062f * u, -0.335f * u)
                close()
            }
            inked(axe, steel, pen)
            drawPath(Path().apply { moveTo(0.062f * u, -0.39f * u); lineTo(0.058f * u, -0.375f * u); lineTo(0.066f * u, -0.375f * u); close() }, steelLight)
            // Legs: greaves and boots.
            for (s in 0..1) {
                val x = (-0.032f + s * 0.064f)
                val greave = Rect((x - 0.021f) * u, -0.175f * u, (x + 0.021f) * u, -0.045f * u)
                inkedRound(greave, 0.008f * u, steel, pen)
                drawLine(steelDark, p(x - 0.018f, -0.11f), p(x + 0.018f, -0.11f), pen.lw * 0.8f)
                inkedRound(Rect((x - 0.026f) * u, -0.062f * u, (x + 0.03f) * u, -0.044f * u), 0.007f * u, steelDark, pen, shade = false)
            }
            // The skirt of plates and the body.
            val skirt = Path().apply {
                moveTo(-0.056f * u, -0.17f * u)
                lineTo(0.056f * u, -0.17f * u)
                lineTo(0.065f * u, -0.115f * u)
                lineTo(-0.065f * u, -0.115f * u)
                close()
            }
            inked(skirt, steel, pen)
            for (k in 1..2) drawLine(steelDark, p(-0.06f + k * 0.0f, -0.17f + k * 0.018f), p(0.06f, -0.17f + k * 0.018f), pen.lw * 0.7f)
            val chest = Path().apply {
                moveTo(-0.052f * u, -0.17f * u)
                lineTo(-0.058f * u, (-0.26f - breath) * u)
                quadraticTo(0f, (-0.285f - breath) * u, 0.058f * u, (-0.26f - breath) * u)
                lineTo(0.052f * u, -0.17f * u)
                close()
            }
            inked(chest, steel, pen)
            drawLine(steelDark, p(0f, -0.172f), p(0f, -0.27f), pen.lw * 0.8f)
            drawPath(Path().apply { moveTo(-0.035f * u, -0.245f * u); quadraticTo(-0.03f * u, -0.205f * u, -0.012f * u, -0.2f * u) }, steelLight, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
            // The red heart of the house on his chest (a green plus? no: a heart).
            val h = fxHeart(0.0f, -0.215f * u, 0.014f * u)
            drawPath(h, Color(0xFFD2443A))
            drawPath(h, Ink.line, style = pen.thin)
            // The arm that holds the pole (on the right) and the arm that salutes (on the left).
            capsule(p(0.055f, -0.25f), p(0.066f, -0.185f), 0.02f * u, steel, pen)
            inkedCircle(p(0.064f, -0.18f), 0.013f * u, steelDark, pen, shade = false)
            val sal = if (action == 2) min(1f, (1.4f - t) / 0.25f).coerceIn(0f, 1f) * min(1f, t / 0.25f).coerceIn(0f, 1f) else 0f
            val shoulder = p(-0.056f, -0.25f)
            val hand = Offset(
                mix(-0.075f * u, -0.034f * u, sal),
                mix(-0.178f * u, -0.352f * u, sal),
            )
            capsule(shoulder, Offset((shoulder.x + hand.x) * 0.5f - 0.012f * u * sal, (shoulder.y + hand.y) * 0.5f), 0.02f * u, steel, pen)
            capsule(Offset((shoulder.x + hand.x) * 0.5f - 0.012f * u * sal, (shoulder.y + hand.y) * 0.5f), hand, 0.018f * u, steel, pen)
            inkedCircle(hand, 0.013f * u, steelDark, pen, shade = false)
            // Pauldrons.
            inkedOval(Rect(-0.082f * u, -0.285f * u, -0.034f * u, -0.235f * u), steelLight, pen)
            inkedOval(Rect(0.034f * u, -0.285f * u, 0.082f * u, -0.235f * u), steelLight, pen)
            // The neck piece, and the head: helmet on, or the mouse that lives inside.
            val gorget = Rect(-0.03f * u, -0.3f * u, 0.03f * u, -0.268f * u)
            inkedRound(gorget, 0.01f * u, steelDark, pen)
            val off = f.mode == 1 || (action == 4 && t > 0f)
            if (off) {
                // The mouse peeks out of the neck, a tiny acorn cap on its head.
                val m = p(0f, -0.31f)
                val wiggle = sin(pen.t * 6f) * 0.003f * u
                inkedCircle(Offset(m.x - 0.022f * u, m.y - 0.018f * u), 0.012f * u, Color(0xFFC7B8B0), pen, shade = false)
                inkedCircle(Offset(m.x + 0.022f * u, m.y - 0.018f * u), 0.012f * u, Color(0xFFC7B8B0), pen, shade = false)
                drawCircle(Color(0xFFFFB3C7), 0.007f * u, Offset(m.x - 0.022f * u, m.y - 0.018f * u))
                drawCircle(Color(0xFFFFB3C7), 0.007f * u, Offset(m.x + 0.022f * u, m.y - 0.018f * u))
                inkedCircle(Offset(m.x, m.y + wiggle), 0.03f * u, Color(0xFFD7CBC4), pen)
                drawCircle(Ink.line, 0.0045f * u, Offset(m.x - 0.011f * u, m.y - 0.004f * u + wiggle))
                drawCircle(Ink.line, 0.0045f * u, Offset(m.x + 0.011f * u, m.y - 0.004f * u + wiggle))
                drawCircle(Color.White, 0.0015f * u, Offset(m.x - 0.0125f * u, m.y - 0.0055f * u + wiggle))
                drawCircle(Color.White, 0.0015f * u, Offset(m.x + 0.0095f * u, m.y - 0.0055f * u + wiggle))
                drawCircle(Color(0xFFFF8FB1), 0.005f * u, Offset(m.x, m.y + 0.01f * u + wiggle))
                for (k in -1..1) drawLine(Ink.line.copy(alpha = 0.6f), Offset(m.x + k * 0.012f * u, m.y + 0.012f * u + wiggle), Offset(m.x + k * 0.035f * u, m.y + 0.014f * u + wiggle * 2f), pen.lw * 0.5f)
                val acorn = Path().apply {
                    moveTo(m.x - 0.018f * u, m.y - 0.026f * u + wiggle)
                    quadraticTo(m.x, m.y - 0.052f * u + wiggle, m.x + 0.018f * u, m.y - 0.026f * u + wiggle)
                    close()
                }
                inked(acorn, Color(0xFFA0663B), pen, shade = false)
                drawCircle(Color(0xFF6E4630), 0.003f * u, Offset(m.x, m.y - 0.05f * u + wiggle))
            } else {
                val m = p(0f, -0.332f)
                grHelmet(m, u, pen, 1f)
            }
        }
    }
    // The helmet flying through the air and landing on the floor, or lying there when it has landed.
    if (f.mode == 1 || action == 4) {
        val fly = if (action == 4 && f.mode == 1) 1f - (t / 1.2f).coerceIn(0f, 1f) else if (action == 4) (t / 0.8f).coerceIn(0f, 1f) else 1f
        val land = p(-0.115f, -0.016f)
        val start = p(0f, -0.332f)
        val k = if (f.mode == 1) fly else 1f - fly
        val pos = Offset(mix(start.x, land.x, k), mix(start.y, land.y, k) - sin(k * PI.toFloat()) * 0.21f * u)
        val spin = if (k < 1f) k * 720f else 0f
        translate(pos.x, pos.y) {
            rotate(spin + (if (k >= 1f) -14f else 0f), Offset.Zero) { grHelmet(Offset.Zero, u, pen, 0.9f) }
        }
        if (k >= 1f && f.mode == 1) {
            // It rocks a little where it landed.
            drawOval(Ink.shadow, Offset(land.x - 0.03f * u, land.y + 0.012f * u), Size(0.06f * u, 0.012f * u))
        }
    }
}

/** A knight's helmet centred on [c]: rounded, with a visor slit, a crest and a red plume. */
private fun DrawScope.grHelmet(c: Offset, u: Float, pen: Pen, scale: Float) {
    val s = scale
    val steel = Color(0xFFBAC4D4)
    val plume = Path().apply {
        moveTo(c.x - 0.004f * u * s, c.y - 0.028f * u * s)
        quadraticTo(c.x - 0.03f * u * s, c.y - 0.075f * u * s, c.x + 0.018f * u * s, c.y - 0.06f * u * s)
        quadraticTo(c.x + 0.004f * u * s, c.y - 0.045f * u * s, c.x + 0.012f * u * s, c.y - 0.028f * u * s)
        close()
    }
    inked(plume, Color(0xFFD2443A), pen)
    val dome = Path().apply {
        moveTo(c.x - 0.034f * u * s, c.y + 0.03f * u * s)
        lineTo(c.x - 0.034f * u * s, c.y - 0.005f * u * s)
        quadraticTo(c.x - 0.034f * u * s, c.y - 0.035f * u * s, c.x, c.y - 0.036f * u * s)
        quadraticTo(c.x + 0.034f * u * s, c.y - 0.035f * u * s, c.x + 0.034f * u * s, c.y - 0.005f * u * s)
        lineTo(c.x + 0.034f * u * s, c.y + 0.03f * u * s)
        close()
    }
    inked(dome, steel, pen)
    drawRect(Color(0xFF2B2140), Offset(c.x - 0.026f * u * s, c.y - 0.006f * u * s), Size(0.052f * u * s, 0.009f * u * s))
    for (k in -1..1) drawLine(Color(0xFF2B2140), Offset(c.x + k * 0.02f * u * s, c.y + 0.012f * u * s), Offset(c.x + k * 0.02f * u * s, c.y + 0.026f * u * s), pen.lw * 0.8f)
    shine(Offset(c.x - 0.016f * u * s, c.y - 0.02f * u * s), 0.008f * u * s, 0.014f * u * s, 0.8f)
    drawLine(Color(0xFF8E9AB2), Offset(c.x, c.y - 0.036f * u * s), Offset(c.x, c.y - 0.008f * u * s), pen.lw * 0.9f)
}

// ------------------------------------------------------------------------------------------------ the chandelier

internal fun DrawScope.grChandelier(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val small = f.variant == 1
    val sc = if (small) 0.82f else 1f
    val on = f.on
    val ceiling = -f.y
    val swing = f.angle * 57.2958f
    // A glow that pools below it.
    if (on) grGlow(p(0f, -0.1f * sc), 0.32f * u * sc, pen, 0.3f)
    rotate(swing, p(0f, ceiling - 0.33f)) {
        // The chain up to the ceiling rose.
        var y = ceiling - 0.33f
        val chainBottom = -0.255f * sc
        while (y < chainBottom) {
            val link = p(0f, y)
            drawOval(GrC.brassDark, Offset(link.x - 0.004f * u, link.y), Size(0.008f * u, 0.013f * u), style = Stroke(pen.lw * 1.4f))
            y += 0.011f
        }
        val rose = Rect(-0.03f * u, (ceiling - 0.342f) * u, 0.03f * u, (ceiling - 0.318f) * u)
        inkedOval(rose, GrC.ivory, pen, shade = false)
        // The brass body: a vase-shaped column.
        val body = Path().apply {
            moveTo(-0.012f * sc * u, -0.255f * sc * u)
            quadraticTo(-0.03f * sc * u, -0.22f * sc * u, -0.016f * sc * u, -0.175f * sc * u)
            quadraticTo(-0.008f * sc * u, -0.15f * sc * u, 0f, -0.105f * sc * u)
            quadraticTo(0.008f * sc * u, -0.15f * sc * u, 0.016f * sc * u, -0.175f * sc * u)
            quadraticTo(0.03f * sc * u, -0.22f * sc * u, 0.012f * sc * u, -0.255f * sc * u)
            close()
        }
        inked(body, GrC.brass, pen)
        // Two tiers of curved arms with a candle at each end and crystals hanging between.
        val tiers = if (small) 1 else 2
        for (tier in 0 until tiers) {
            val ay = (-0.19f + tier * 0.06f) * sc
            val reach = (if (tier == 0) 0.15f else 0.09f) * sc
            val arms = if (small) 3 else if (tier == 0) 3 else 2
            for (a in -arms..arms) {
                if (a == 0) continue
                val side = if (a < 0) -1f else 1f
                val k = (kotlin.math.abs(a) / arms.toFloat())
                val ex = side * reach * (0.45f + 0.55f * k)
                val ey = ay + (0.012f + 0.04f * (1f - k)) * sc
                val arm = Path().apply {
                    moveTo(0f, (ay + 0.01f) * u)
                    quadraticTo(ex * 0.6f * u, (ay + 0.05f * sc) * u, ex * u, ey * u)
                }
                drawPath(arm, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
                drawPath(arm, GrC.brass, style = Stroke(0.009f * u, cap = StrokeCap.Round))
                // Cup, candle and flame.
                val cup = p(ex, ey)
                inkedRound(Rect(cup.x - 0.011f * u, cup.y - 0.004f * u, cup.x + 0.011f * u, cup.y + 0.008f * u), 0.003f * u, GrC.brass, pen, shade = false)
                inkedRound(Rect(cup.x - 0.0055f * u, cup.y - 0.032f * u, cup.x + 0.0055f * u, cup.y - 0.004f * u), 0.002f * u, GrC.ivory, pen, shade = false)
                if (on) grFlame(Offset(cup.x, cup.y - 0.032f * u), 0.026f * u, pen, f.id + a * 1.7f + tier)
                // The prisms: a short string of drops under each arm, glinting in turn.
                for (dr in 0 until 3) {
                    val dx = ex * (0.35f + dr * 0.28f)
                    val dyy = ay + (0.045f + (if (tier == 0) 0.01f else 0f)) * sc + (dr % 2) * 0.012f * sc
                    val c = p(dx, dyy)
                    val glint = max(0f, sin(pen.t * 2.1f + a * 1.9f + dr * 2.3f + tier))
                    val drop = Path().apply {
                        moveTo(c.x, c.y)
                        lineTo(c.x - 0.006f * u, c.y + 0.016f * u)
                        lineTo(c.x, c.y + 0.03f * u)
                        lineTo(c.x + 0.006f * u, c.y + 0.016f * u)
                        close()
                    }
                    drawPath(drop, lerp(Color(0xFFDDEFFF), Color.White, glint))
                    drawPath(drop, Ink.line, style = pen.thin)
                    if (glint > 0.85f) twinkle(Offset(c.x, c.y + 0.012f * u), 0.014f * u, Color.White, (glint - 0.85f) * 6f)
                }
            }
        }
        // The big drop at the bottom, and a ring of small ones.
        val bottom = p(0f, -0.105f * sc)
        val big = Path().apply {
            moveTo(bottom.x, bottom.y)
            lineTo(bottom.x - 0.016f * u, bottom.y + 0.034f * u)
            lineTo(bottom.x, bottom.y + 0.072f * u)
            lineTo(bottom.x + 0.016f * u, bottom.y + 0.034f * u)
            close()
        }
        drawPath(big, Color(0xFFDDEFFF))
        drawPath(big, Ink.line, style = pen.stroke)
        drawLine(Color.White.copy(alpha = 0.7f), Offset(bottom.x - 0.005f * u, bottom.y + 0.024f * u), Offset(bottom.x, bottom.y + 0.05f * u), pen.lw)
        val gl = max(0f, sin(pen.t * 1.4f + f.id))
        if (gl > 0.9f) twinkle(Offset(bottom.x - 0.004f * u, bottom.y + 0.03f * u), 0.02f * u, Color.White, (gl - 0.9f) * 10f)
        if (f.anim > 0.05f || abs(f.angle) > 0.02f) twinkle(p(0.1f * sc, -0.12f * sc), 0.02f * u, Color.White, 0.8f)
    }
}

// ------------------------------------------------------------------------------------------------ the postal slot

internal fun DrawScope.grPostSlot(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val flap = f.anim.coerceIn(0f, 1f)
    // A walnut panel with a brass plate and a flap that lifts when a letter comes.
    val panel = Rect(-0.05f * u, -0.1f * u, 0.05f * u, 0f)
    inkedRound(panel, 0.01f * u, GrC.walnut, pen)
    drawRect(GrC.walnutDark, Offset(panel.left + 0.008f * u, panel.top + 0.008f * u), Size(panel.width - 0.016f * u, panel.height - 0.016f * u), style = pen.thin)
    val plate = Rect(-0.037f * u, -0.074f * u, 0.037f * u, -0.03f * u)
    inkedRound(plate, 0.004f * u, GrC.brass, pen, shade = false)
    drawRect(Color(0xFF2B2140), Offset(plate.left + 0.008f * u, plate.center.y - 0.004f * u - 0.004f * flap * u), Size(plate.width - 0.016f * u, 0.009f * u + 0.008f * flap * u))
    // The flap and the corner of a letter in it.
    val fl = Path().apply {
        moveTo(plate.left + 0.006f * u, plate.center.y - 0.007f * u)
        lineTo(plate.right - 0.006f * u, plate.center.y - 0.007f * u)
        lineTo(plate.right - 0.006f * u, plate.center.y - 0.007f * u + 0.011f * u * (1f - flap))
        lineTo(plate.left + 0.006f * u, plate.center.y - 0.007f * u + 0.011f * u * (1f - flap))
        close()
    }
    drawPath(fl, GrC.brassDark)
    drawPath(fl, Ink.line, style = pen.thin)
    if (f.count != 0) {
        val l = Path().apply {
            moveTo(-0.014f * u, plate.center.y - 0.002f * u)
            lineTo(0.014f * u, plate.center.y - 0.002f * u)
            lineTo(0.014f * u, plate.center.y + 0.018f * u)
            lineTo(-0.014f * u, plate.center.y + 0.018f * u)
            close()
        }
        // A letter always sticks out a little, to say that the slot works.
        clipRect(plate.left, plate.center.y, plate.right, plate.bottom + 0.02f * u) {
            drawPath(l, Color.White)
            drawPath(l, Ink.line, style = pen.thin)
            drawCircle(GrC.burgundy, 0.004f * u, p(0f, -0.034f + (-0.0f)))
        }
    }
    // Small brass screws in the corners.
    for ((sx, sy) in listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)) fxNail(p(sx * 0.037f, -0.052f + sy * 0.042f), 0.0032f * u)
}

// ------------------------------------------------------------------------------------------------ the portraits

internal fun DrawScope.grPortrait(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val v = f.variant.mod(3)
    val act = f.timer > 0f
    val a = (f.timer / 2.4f).coerceIn(0f, 1f)
    // The frame: gilded, with a little lamp bracket on top.
    val frame = Rect(-0.07f * u, -0.2f * u, 0.07f * u, 0f)
    box3d(frame, 0.02f * u, GrC.brass, pen, radius = 0.008f * u)
    val inner = Rect(frame.left + 0.012f * u, frame.top + 0.012f * u, frame.right - 0.012f * u, frame.bottom - 0.012f * u)
    val bg = intArrayOf(0xFF4F7F6E.toInt(), 0xFF6E5A8A.toInt(), 0xFF8A4A5A.toInt())[v]
    drawRect(Color(bg), inner.topLeft, inner.size)
    drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent), startY = inner.top, endY = inner.bottom), inner.topLeft, inner.size)
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        val cx = inner.center.x
        val cy = inner.center.y + 0.015f * u
        when (v) {
            0 -> {
                // A lady with a tall white wig and pearls. She sticks out her tongue when she is tapped.
                inkedOval(Rect(cx - 0.052f * u, inner.top - 0.01f * u, cx + 0.052f * u, cy - 0.005f * u), Color(0xFFF2EFE8), pen, shade = false)
                inkedOval(Rect(cx - 0.062f * u, cy + 0.02f * u, cx + 0.062f * u, inner.bottom + 0.05f * u), Color(0xFF3B6EA5), pen, shade = false)
                inkedCircle(Offset(cx, cy), 0.036f * u, Color(0xFFF9D0B0), pen, shade = false)
                drawCircle(Ink.line, 0.0035f * u, Offset(cx - 0.014f * u, cy - 0.005f * u))
                drawCircle(Ink.line, 0.0035f * u, Offset(cx + 0.014f * u, cy - 0.005f * u))
                drawCircle(Color(0x66FF6F91), 0.007f * u, Offset(cx - 0.022f * u, cy + 0.008f * u))
                drawCircle(Color(0x66FF6F91), 0.007f * u, Offset(cx + 0.022f * u, cy + 0.008f * u))
                val mouth = Path().apply { moveTo(cx - 0.012f * u, cy + 0.016f * u); quadraticTo(cx, cy + 0.024f * u, cx + 0.012f * u, cy + 0.016f * u) }
                drawPath(mouth, Color(0xFFB2334A), style = Stroke(pen.lw, cap = StrokeCap.Round))
                if (act) inkedRound(Rect(cx - 0.007f * u, cy + 0.016f * u, cx + 0.007f * u, cy + 0.036f * u), 0.006f * u, Color(0xFFFF7F9E), pen, shade = false)
                for (k in 0 until 6) drawCircle(Color.White, 0.005f * u, Offset(cx - 0.045f * u + k * 0.018f * u, cy + 0.04f * u + sin(k * 1.0f) * 0.005f * u))
            }
            1 -> {
                // A gentleman with a great moustache and a monocle that pops out when he is startled.
                inkedRound(Rect(cx - 0.05f * u, cy + 0.025f * u, cx + 0.05f * u, inner.bottom + 0.05f * u), 0.02f * u, Color(0xFF2B3F6B), pen, shade = false)
                inkedCircle(Offset(cx, cy), 0.036f * u, Color(0xFFEDB58D), pen, shade = false)
                val hair = Path().apply { moveTo(cx - 0.038f * u, cy - 0.01f * u); quadraticTo(cx, cy - 0.06f * u, cx + 0.038f * u, cy - 0.01f * u); quadraticTo(cx, cy - 0.03f * u, cx - 0.038f * u, cy - 0.01f * u) }
                drawPath(hair, Color(0xFF5A3824))
                drawCircle(Ink.line, 0.0035f * u, Offset(cx - 0.014f * u, cy - 0.003f * u))
                val ex = if (act) 0.014f * u + sin(pen.t * 20f) * 0.003f * u else 0.014f * u
                drawCircle(Ink.line, 0.0035f * u, Offset(cx + ex, cy - 0.003f * u))
                drawCircle(GrC.brass, 0.011f * u, Offset(cx + 0.014f * u, cy - 0.003f * u + (if (act) 0.03f * u * (1f - a) else 0f)), style = pen.thin)
                val must = Path().apply {
                    moveTo(cx, cy + 0.013f * u)
                    quadraticTo(cx - 0.025f * u, cy + 0.006f * u, cx - 0.036f * u, cy + 0.02f * u - (if (act) 0.008f * u else 0f))
                    quadraticTo(cx - 0.02f * u, cy + 0.024f * u, cx, cy + 0.017f * u)
                    quadraticTo(cx + 0.02f * u, cy + 0.024f * u, cx + 0.036f * u, cy + 0.02f * u - (if (act) 0.008f * u else 0f))
                    quadraticTo(cx + 0.025f * u, cy + 0.006f * u, cx, cy + 0.013f * u)
                }
                drawPath(must, Color(0xFF5A3824))
                drawPath(must, Ink.line, style = pen.thin)
            }
            else -> {
                // A dignified dog in a ruff, with a ribbon. When tapped it licks its nose.
                inkedCircle(Offset(cx, cy + 0.045f * u), 0.05f * u, Color(0xFFFFF4E0), pen, shade = false)
                inkedCircle(Offset(cx, cy), 0.036f * u, Color(0xFFC98A55), pen, shade = false)
                inkedOval(Rect(cx - 0.052f * u, cy - 0.028f * u, cx - 0.026f * u, cy + 0.03f * u), Color(0xFF8A5A3C), pen, shade = false)
                inkedOval(Rect(cx + 0.026f * u, cy - 0.028f * u, cx + 0.052f * u, cy + 0.03f * u), Color(0xFF8A5A3C), pen, shade = false)
                drawCircle(Ink.line, 0.0035f * u, Offset(cx - 0.014f * u, cy - 0.006f * u))
                drawCircle(Ink.line, 0.0035f * u, Offset(cx + 0.014f * u, cy - 0.006f * u))
                drawOval(Ink.line, Offset(cx - 0.008f * u, cy + 0.004f * u), Size(0.016f * u, 0.011f * u))
                if (act) inkedRound(Rect(cx - 0.005f * u, cy + 0.012f * u, cx + 0.005f * u, cy + 0.03f * u), 0.005f * u, Color(0xFFFF7F9E), pen, shade = false)
                drawLine(Color(0xFFD2443A), Offset(cx - 0.03f * u, cy + 0.05f * u), Offset(cx + 0.03f * u, cy + 0.05f * u), pen.lw * 3f)
            }
        }
    }
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.thin)
    // A picture lamp: a small brass hood over the frame.
    val lampTop = p(0f, -0.2f)
    drawLine(Ink.line, lampTop, p(0f, -0.215f), pen.lw)
    val hood = Path().apply {
        moveTo(-0.032f * u, -0.222f * u)
        lineTo(0.032f * u, -0.222f * u)
        lineTo(0.022f * u, -0.208f * u)
        lineTo(-0.022f * u, -0.208f * u)
        close()
    }
    inked(hood, GrC.brass, pen, shade = false)
    if (act) twinkle(p(0.05f, -0.17f), 0.018f * u * a, Color.White, a)
}
