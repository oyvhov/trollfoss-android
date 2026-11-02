package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The ways between the floors, drawn for the upper floor: stairs down to the hall, stairs up to the attic, the lift,
 * the playroom slide and the long balcony slide, the fireman's pole, the laundry chute and the dumbwaiter.
 * Private helpers start with `up`.
 */

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

// ---------------------------------------------------------------------------------------------- stairs down

/** A stairwell in the floor with banisters round it: warm light from the hall below, steps going down out of sight. */
internal fun DrawScope.upStairsDown(f: Fixture, u: Float, pen: Pen) {
    upShadow(u, 0.84f, 0.1f, 0.4f)
    stairWell(u, StairDoorways.upperDown, pen)
}

// ---------------------------------------------------------------------------------------------- stairs up

/** A wooden flight rising to the right up to a little door in the wall: the way to the attic. */
internal fun DrawScope.upStairsUp(f: Fixture, u: Float, pen: Pen) {
    upShadow(u, 0.84f, 0.12f, 0.8f)
    stairPortal(u, StairDoorways.attic, pen)
    fxBox(u, 0.1f, -0.53f, 0.33f, -0.5f, 0.07f, Color(0xFFE3B984), pen, rad = 0.005f)
    stairFlight(u, pen, low = -0.4f, high = 0.3f, height = 0.5f, depth = 0.075f)
}

// ---------------------------------------------------------------------------------------------- the lift

/** A lift door in the wall with a brass arch of lamps above it and a call button; the doors slide open when tapped. */
internal fun DrawScope.upLift(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val open = sin(PI.toFloat() * (1f - upBeat(f))).coerceIn(0f, 1f) * (if (upBeat(f) > 0.01f) 1f else 0f)
    upShadow(u, 0.34f, 0.06f, 0.6f)
    // The frame: dark wood with a brass edge, and the arch with three lamps.
    fxBox(u, -0.17f, -0.43f, 0.17f, 0f, 0.05f, UpC.walnut, pen, rad = 0.008f)
    inkedRound(Rect(-0.15f * u, -0.41f * u, 0.15f * u, -0.005f * u), 0.006f * u, UpC.brass, pen, shade = false)
    val inner = Rect(-0.125f * u, -0.385f * u, 0.125f * u, -0.012f * u)
    // The cabin behind the doors: warm wood, a rail and a little bench.
    drawRect(Color(0xFFE9C58C), inner.topLeft, inner.size)
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        fxGlow(Offset(inner.center.x, inner.top + 0.05f * u), 0.18f * u, Color(0xFFFFE9A8), 0.8f)
        drawRect(UpC.walnut.lighten(0.15f), Offset(inner.left, inner.bottom - 0.05f * u), Size(inner.width, 0.05f * u))
        drawLine(UpC.brass, Offset(inner.left, inner.center.y + 0.02f * u), Offset(inner.right, inner.center.y + 0.02f * u), strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
        for (k in 1..4) drawLine(UpC.walnut.copy(alpha = 0.3f), Offset(inner.left + inner.width * k / 5f, inner.top), Offset(inner.left + inner.width * k / 5f, inner.bottom), strokeWidth = pen.lw * 0.6f)
        drawRect(Ink.line.copy(alpha = 0.3f), inner.topLeft, Size(inner.width * 0.05f, inner.height))
    }
    // The two doors, brushed steel, sliding apart.
    val half = inner.width / 2f
    val gap = half * open
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        for (side in listOf(-1f, 1f)) {
            val l = if (side < 0f) inner.left - gap else inner.center.x + gap
            val leaf = Rect(l, inner.top, l + half, inner.bottom)
            inkedRound(leaf, 0f, UpC.steel, pen, shade = false)
            drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.Transparent, Color.Black.copy(alpha = 0.1f)), startY = leaf.top, endY = leaf.bottom), leaf.topLeft, leaf.size)
            val winH = 0.07f * u
            val w = Rect(leaf.left + leaf.width * 0.22f, leaf.top + 0.06f * u, leaf.right - leaf.width * 0.22f, leaf.top + 0.06f * u + winH)
            inkedRound(w, 0.004f * u, Color(0xFFCFEFFF), pen, shade = false)
            shine(Offset(w.left + w.width * 0.3f, w.top + w.height * 0.35f), w.width * 0.2f, w.height * 0.4f, 0.8f)
            // Panels
            drawRect(Ink.line.copy(alpha = 0.25f), Offset(leaf.left + leaf.width * 0.15f, leaf.top + 0.17f * u), Size(leaf.width * 0.7f, leaf.height - 0.23f * u), style = pen.thin)
        }
    }
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.stroke)
    // The arch of lamps: the lit one walks round, so the lift always seems to be on its way.
    val arc = Path().apply {
        moveTo(-0.12f * u, -0.425f * u)
        quadraticTo(0f, -0.5f * u, 0.12f * u, -0.425f * u)
    }
    drawPath(arc, Ink.line, style = Stroke(0.02f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(arc, UpC.brass, style = Stroke(0.02f * u, cap = StrokeCap.Round))
    val lit = if (upBeat(f) > 0.01f) (((1f - upBeat(f)) * 7f).toInt() % 3) else 1
    for (k in 0..2) {
        val s = k / 2f
        val c = p(-0.09f + 0.18f * s, -0.45f - 0.03f * sin(PI.toFloat() * s) * 0.9f)
        val on = k == lit
        if (on) fxGlow(c, 0.04f * u, Color(0xFFFFE066), 0.9f)
        drawCircle(if (on) Color(0xFFFFF3A8) else Color(0xFF8A6A2A), 0.0075f * u, c)
        drawCircle(Ink.line, 0.0075f * u, c, style = pen.thin)
    }
    // The call panel: two round buttons.
    inkedRound(Rect(0.18f * u, -0.24f * u, 0.215f * u, -0.15f * u), 0.006f * u, UpC.steel, pen, shade = false)
    for ((i, c) in listOf(Color(0xFF7CFFB2), Color(0xFFFFD447)).withIndex()) {
        val cc = p(0.1975f, -0.215f + i * 0.04f)
        drawCircle(c.copy(alpha = if (open > 0.1f && i == 1) 1f else 0.85f), 0.009f * u, cc)
        drawCircle(Ink.line, 0.009f * u, cc, style = pen.thin)
    }
}

// ---------------------------------------------------------------------------------------------- the slides

/** Points on a cubic curve. */
private fun cubic(a: Offset, b: Offset, c: Offset, d: Offset, t: Float): Offset {
    val s = 1f - t
    return Offset(
        s * s * s * a.x + 3f * s * s * t * b.x + 3f * s * t * t * c.x + t * t * t * d.x,
        s * s * s * a.y + 3f * s * s * t * b.y + 3f * s * t * t * c.y + t * t * t * d.y,
    )
}

private fun cubicDirection(a: Offset, b: Offset, c: Offset, d: Offset, t: Float): Offset {
    val s = 1f - t
    val x = 3f * s * s * (b.x - a.x) + 6f * s * t * (c.x - b.x) + 3f * t * t * (d.x - c.x)
    val y = 3f * s * s * (b.y - a.y) + 6f * s * t * (c.y - b.y) + 3f * t * t * (d.y - c.y)
    val l = max(0.0001f, kotlin.math.sqrt(x * x + y * y))
    return Offset(x / l, y / l)
}

/** The ladder of a slide tower, leaning on its platform: two rails and rungs, seen from the front. */
private fun DrawScope.upLadder(u: Float, x: Float, top: Float, color: Color, rung: Color, pen: Pen, rungs: Int = 5, lean: Float = 0.04f) {
    fun p(px: Float, py: Float) = Offset(px * u, py * u)
    for (s in listOf(-0.028f, 0.028f)) capsule(p(x + s - lean, 0f), p(x + s, top), 0.012f * u, color, pen)
    for (k in 1..rungs) {
        val y = top * k / (rungs + 0.6f)
        val l = lerpF(-lean, 0f, k / (rungs + 0.6f))
        capsule(p(x - 0.028f + l * 0.0f - lean * (1f - k / (rungs + 0.6f)), y), p(x + 0.028f - lean * (1f - k / (rungs + 0.6f)), y), 0.009f * u, rung, pen)
    }
}

/** The playroom slide: a tower with a ladder and a glittering chute that dives into a hole in the floor, down to the living room. */
internal fun DrawScope.upSlideIndoor(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val platTop = -0.455f
    upShadow(u, 0.7f, 0.13f)
    // The round hole in the floor where the chute disappears, with a rubber rim.
    val hole = q(0.215f, 0f, 0.05f)
    drawPath(fxDisc2(hole.x, hole.y, 0.095f * u, 0.07f * u), UpC.red.darken(0.1f))
    drawPath(fxDisc2(hole.x, hole.y, 0.095f * u, 0.07f * u), Ink.line, style = pen.stroke)
    drawPath(fxDisc2(hole.x, hole.y + 0.004f * u, 0.07f * u, 0.052f * u), Color(0xFF2B1A22))
    drawPath(fxDisc2(hole.x, hole.y + 0.004f * u, 0.07f * u, 0.052f * u), Ink.line, style = pen.thin)

    // The tower: four legs (two seen), the platform and its railing.
    for (x in listOf(-0.31f, -0.13f)) fxPost(u, x, 0.03f, 0f, platTop + 0.01f, 0.012f, UpC.blue, pen)
    fxPost(u, -0.31f, 0.11f, 0f, platTop + 0.01f, 0.012f, UpC.blue, pen)
    fxPost(u, -0.13f, 0.11f, 0f, platTop + 0.01f, 0.012f, UpC.blue, pen)
    fxBox(u, -0.345f, platTop, -0.095f, platTop + 0.034f, 0.14f, UpC.yellow, pen, rad = 0.005f, z = 0f)
    // The railing: posts, two bars, and a pennant on a pole.
    for (x in listOf(-0.335f, -0.105f)) fxPost(u, x, 0.01f, platTop, platTop - 0.115f, 0.007f, UpC.sky, pen)
    capsule(p(-0.335f, platTop - 0.105f), p(-0.105f, platTop - 0.105f), 0.012f * u, UpC.sky, pen)
    capsule(p(-0.335f, platTop - 0.055f), p(-0.105f, platTop - 0.055f), 0.009f * u, UpC.sky.lighten(0.3f), pen)
    capsule(q(-0.335f, platTop - 0.105f, 0.01f), q(-0.335f, platTop - 0.105f, 0.14f), 0.01f * u, UpC.sky, pen)
    val flagBase = p(-0.22f, platTop)
    capsule(flagBase, Offset(flagBase.x, flagBase.y - 0.14f * u), 0.006f * u, UpC.walnut, pen)
    val wave = 0.004f * u
    val flag = Path().apply {
        moveTo(flagBase.x, flagBase.y - 0.14f * u)
        quadraticTo(flagBase.x + 0.03f * u, flagBase.y - 0.145f * u + wave, flagBase.x + 0.055f * u, flagBase.y - 0.128f * u)
        quadraticTo(flagBase.x + 0.03f * u, flagBase.y - 0.12f * u - wave, flagBase.x, flagBase.y - 0.1f * u)
        close()
    }
    inked(flag, UpC.green, pen, shade = false)
    // The ladder, red.
    upLadder(u, -0.385f, platTop, UpC.red, UpC.oak, pen, rungs = 5, lean = 0.03f)

    // The chute: a long yellow swoosh from the platform to the hole.
    val a = p(-0.095f, platTop - 0.004f)
    val b = p(0.01f, platTop - 0.01f)
    val c = p(0.12f, -0.22f)
    val d = Offset(hole.x - 0.005f * u, hole.y - 0.012f * u)
    val chute = Path().apply {
        moveTo(a.x, a.y)
        cubicTo(b.x, b.y, c.x, c.y, d.x, d.y)
    }
    val wide = 0.07f * u
    drawPath(chute, Ink.shadow, style = Stroke(wide * 1.1f, cap = StrokeCap.Round))
    drawPath(chute, Ink.line, style = Stroke(wide + pen.lw * 2.4f, cap = StrokeCap.Round))
    drawPath(chute, UpC.yellow, style = Stroke(wide, cap = StrokeCap.Round))
    drawPath(chute, UpC.yellow.lighten(0.35f), style = Stroke(wide * 0.45f, cap = StrokeCap.Round))
    // The two side rails of the chute, blue.
    for (s in listOf(-1f, 1f)) {
        val off = Path().apply {
            for (i in 0..24) {
                val tt = i / 24f
                val pt = cubic(a, b, c, d, tt)
                val dir = cubicDirection(a, b, c, d, tt)
                val nx = -dir.y * s * wide * 0.5f
                val ny = dir.x * s * wide * 0.5f
                if (i == 0) moveTo(pt.x + nx, pt.y + ny) else lineTo(pt.x + nx, pt.y + ny)
            }
        }
        drawPath(off, Ink.line, style = Stroke(0.014f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(off, UpC.blue, style = Stroke(0.014f * u, cap = StrokeCap.Round))
    }
    // Glints running down the chute.
    for (k in 0 until 3) {
        val tt = 0.15f + k * 0.3f
        val pt = cubic(a, b, c, d, tt)
        twinkle(pt, 0.02f * u * sin(PI.toFloat() * tt), Color.White, 0.95f)
    }
    // Where the chute goes into the hole: a dark mouth.
    drawPath(fxDisc2(d.x, d.y + 0.012f * u, 0.03f * u, 0.02f * u), Color(0xFF2B1A22))
    // A tap: a rider's cheer, drawn as a little star shower along the chute.
    val beat = upBeat(f)
    if (beat > 0.01f) {
        val tt = 1f - beat
        val pt = cubic(a, b, c, d, tt)
        twinkle(pt, 0.05f * u, UpC.yellow, 1f)
        twinkle(Offset(pt.x - 0.03f * u, pt.y - 0.02f * u), 0.03f * u, Color.White, 0.8f)
    }
}

/** The balcony slide: a tower with a ladder and a long blue tube with white spiral bands that sweeps off the balcony to the garden. */
internal fun DrawScope.upSlideBalcony(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val platTop = -0.44f
    upShadow(u, 0.5f, 0.12f, 0.7f)
    for (x in listOf(-0.3f, -0.12f)) fxPost(u, x, 0.03f, 0f, platTop + 0.01f, 0.012f, UpC.walnut, pen)
    fxPost(u, -0.3f, 0.12f, 0f, platTop + 0.01f, 0.012f, UpC.walnut, pen)
    fxPost(u, -0.12f, 0.12f, 0f, platTop + 0.01f, 0.012f, UpC.walnut, pen)
    fxBox(u, -0.335f, platTop, -0.085f, platTop + 0.034f, 0.15f, UpC.oak, pen, rad = 0.005f)
    // A little roof over the top, so you can wait out of the rain.
    for (x in listOf(-0.322f, -0.098f)) fxPost(u, x, 0.01f, platTop, platTop - 0.2f, 0.007f, UpC.walnut, pen)
    val roof = Path().apply {
        moveTo(-0.37f * u, (platTop - 0.19f) * u)
        lineTo(-0.21f * u, (platTop - 0.265f) * u)
        lineTo(-0.05f * u, (platTop - 0.19f) * u)
        close()
    }
    inked(roof, UpC.falun, pen)
    drawLine(UpC.cream, p(-0.34f, platTop - 0.2f), p(-0.08f, platTop - 0.2f), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    upLadder(u, -0.385f, platTop, UpC.walnut, UpC.oak, pen, rungs = 5, lean = 0.035f)

    // The tube.
    val a = p(-0.085f, platTop - 0.01f)
    val b = p(0.12f, platTop - 0.03f)
    val c = p(0.3f, -0.3f)
    val d = p(0.36f, 0.08f)
    val tube = Path().apply { moveTo(a.x, a.y); cubicTo(b.x, b.y, c.x, c.y, d.x, d.y) }
    val wide = 0.082f * u
    drawPath(tube, Ink.shadow, style = Stroke(wide * 1.1f, cap = StrokeCap.Round))
    drawPath(tube, Ink.line, style = Stroke(wide + pen.lw * 2.4f, cap = StrokeCap.Round))
    drawPath(tube, Color(0xFF4FB3D9), style = Stroke(wide, cap = StrokeCap.Round))
    // White spiral bands, short strokes across the tube.
    for (i in 1..13) {
        val tt = i / 14f
        val pt = cubic(a, b, c, d, tt)
        val dir = cubicDirection(a, b, c, d, tt)
        val nx = -dir.y
        val ny = dir.x
        val w2 = wide * 0.5f
        drawLine(Color.White.copy(alpha = 0.9f), Offset(pt.x + nx * w2 - dir.x * 0.012f * u, pt.y + ny * w2 - dir.y * 0.012f * u), Offset(pt.x - nx * w2 + dir.x * 0.012f * u, pt.y - ny * w2 + dir.y * 0.012f * u), strokeWidth = 0.011f * u, cap = StrokeCap.Round)
    }
    // The light side of the tube, a shine.
    val shineP = Path().apply {
        for (i in 0..24) {
            val tt = i / 24f
            val pt = cubic(a, b, c, d, tt)
            val dir = cubicDirection(a, b, c, d, tt)
            val nx = -dir.y * wide * 0.28f
            val ny = dir.x * wide * 0.28f
            if (i == 0) moveTo(pt.x + nx, pt.y + ny) else lineTo(pt.x + nx, pt.y + ny)
        }
    }
    drawPath(shineP, Color.White.copy(alpha = 0.55f), style = Stroke(0.007f * u, cap = StrokeCap.Round))
    // A breeze: speed lines that blow along the tube.
    for (k in 0 until 3) {
        val tt = 0.2f + k * 0.3f
        val pt = cubic(a, b, c, d, tt)
        twinkle(pt, 0.022f * u * sin(PI.toFloat() * tt), Color.White, 0.95f)
    }
    val beat = upBeat(f)
    if (beat > 0.01f) {
        val pt = cubic(a, b, c, d, 1f - beat)
        twinkle(pt, 0.05f * u, UpC.yellow, 1f)
    }
}

// ---------------------------------------------------------------------------------------------- the pole

/** A brass fireman's pole from a round hole in the floor to the ceiling, with a bell and a brass shine that runs down it when tapped. */
internal fun DrawScope.upPole(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val top = -(f.y - 0.015f)
    val floorC = p(0f, 0.012f)
    // The hole, with a ring of brass.
    drawPath(fxDisc2(floorC.x, floorC.y, 0.085f * u, 0.062f * u), UpC.brassDark)
    drawPath(fxDisc2(floorC.x, floorC.y, 0.085f * u, 0.062f * u), Ink.line, style = pen.stroke)
    drawPath(fxDisc2(floorC.x, floorC.y + 0.004f * u, 0.066f * u, 0.047f * u), Color(0xFF2B1A22))
    drawPath(fxDisc2(floorC.x, floorC.y + 0.004f * u, 0.066f * u, 0.047f * u), Ink.line, style = pen.thin)
    // A soft pad round the hole.
    drawPath(fxDisc2(floorC.x, floorC.y + 0.006f * u, 0.12f * u, 0.085f * u), Ink.shadow)
    // The pole.
    val w = 0.019f * u
    drawLine(Ink.line, p(0f, -0.005f), p(0f, top + 0.012f), strokeWidth = w + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(UpC.brass, p(0f, -0.005f), p(0f, top + 0.012f), strokeWidth = w, cap = StrokeCap.Round)
    drawLine(UpC.brassDark, p(0.0055f, -0.005f), p(0.0055f, top + 0.012f), strokeWidth = w * 0.3f, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.8f), p(-0.0045f, -0.01f), p(-0.0045f, top + 0.03f), strokeWidth = w * 0.18f, cap = StrokeCap.Round)
    // The ceiling flange.
    drawOval(UpC.brassDark, Offset(-0.03f * u, (top - 0.004f) * u), Size(0.06f * u, 0.026f * u))
    drawOval(Ink.line, Offset(-0.03f * u, (top - 0.004f) * u), Size(0.06f * u, 0.026f * u), style = pen.thin)
    // The bell on a bracket.
    val sway = if (upBeat(f) > 0.01f) sin(upBeat(f) * 40f) * 8f * upBeat(f) else 0f
    val bell = p(0.058f, top + 0.3f)
    capsule(p(0.004f, top + 0.3f), p(0.035f, top + 0.3f), 0.008f * u, UpC.brassDark, pen)
    rotate(sway, Offset(bell.x, bell.y - 0.03f * u)) {
        val body = Path().apply {
            moveTo(bell.x - 0.028f * u, bell.y + 0.012f * u)
            quadraticTo(bell.x - 0.026f * u, bell.y - 0.035f * u, bell.x, bell.y - 0.035f * u)
            quadraticTo(bell.x + 0.026f * u, bell.y - 0.035f * u, bell.x + 0.028f * u, bell.y + 0.012f * u)
            close()
        }
        inked(body, UpC.brass, pen)
        drawCircle(UpC.brassDark, 0.007f * u, Offset(bell.x, bell.y + 0.014f * u))
        shine(Offset(bell.x - 0.01f * u, bell.y - 0.012f * u), 0.007f * u, 0.016f * u, 0.8f)
    }
    // The shine that runs down the pole when somebody takes the way down.
    val beat = upBeat(f)
    if (beat > 0.01f) {
        val y = lerpF(top + 0.05f, -0.02f, 1f - beat)
        twinkle(p(0f, y), 0.045f * u, Color.White, 1f)
        twinkle(p(0f, y - 0.07f), 0.028f * u, UpC.yellow, 0.9f)
    }
}

// ---------------------------------------------------------------------------------------------- the laundry chute

/** A square trapdoor in the floor with brass corners and a sock painted on it; the lid flips up when tapped. */
internal fun DrawScope.upHatch(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val open = sin(PI.toFloat() * (1f - upBeat(f))).coerceIn(0f, 1f) * (if (upBeat(f) > 0.01f) 1f else 0f)
    val z0 = 0.0f
    val z1 = 0.17f
    // Frame and the dark chute.
    fxFace(fxFlat(u, -0.16f, 0.16f, 0.002f, z0 - 0.015f, z1 + 0.015f, 0.01f), UpC.walnut, pen)
    val hole = fxFlat(u, -0.14f, 0.14f, 0.002f, z0, z1, 0.006f)
    drawPath(hole, Color(0xFF211520))
    clipPath(hole) {
        val c = q(0f, 0f, 0.09f)
        fxGlow(c, 0.1f * u, Color(0xFFFFD27A), 0.25f + 0.5f * open)
    }
    drawPath(hole, Ink.line, style = pen.thin)
    if (open < 0.05f) {
        // Closed: the lid with its planks, brass corners and ring.
        val lid = fxFlat(u, -0.14f, 0.14f, 0f, z0, z1, 0.006f)
        fxFace(lid, UpC.teal, pen)
        clipPath(lid) {
            for (k in 1..4) {
                val z = z0 + (z1 - z0) * k / 5f
                drawLine(UpC.teal.darken(0.3f), q(-0.14f, 0f, z), q(0.14f, 0f, z), strokeWidth = pen.lw * 0.7f)
            }
            // The sock on the lid: white, with a red heel and toe.
            val s = q(-0.03f, 0f, 0.085f)
            val sock = Path().apply {
                moveTo(s.x - 0.012f * u, s.y - 0.014f * u)
                lineTo(s.x + 0.014f * u, s.y - 0.016f * u)
                lineTo(s.x + 0.015f * u, s.y + 0.0f * u)
                quadraticTo(s.x + 0.05f * u, s.y + 0.004f * u, s.x + 0.05f * u, s.y + 0.012f * u)
                quadraticTo(s.x + 0.04f * u, s.y + 0.022f * u, s.x - 0.012f * u, s.y + 0.012f * u)
                close()
            }
            drawPath(sock, Color.White)
            drawPath(sock, Ink.line, style = pen.thin)
            drawCircle(UpC.red, 0.007f * u, Offset(s.x + 0.045f * u, s.y + 0.011f * u))
            drawLine(UpC.red, Offset(s.x - 0.012f * u, s.y - 0.008f * u), Offset(s.x + 0.014f * u, s.y - 0.01f * u), strokeWidth = 0.006f * u)
        }
        for (x in listOf(-0.14f, 0.14f)) for (z in listOf(z0, z1)) {
            val c = q(x, 0f, z)
            drawCircle(UpC.brass, 0.011f * u, c)
            drawCircle(Ink.line, 0.011f * u, c, style = pen.thin)
        }
        val ring = q(0.09f, 0f, 0.085f)
        drawOval(UpC.brassDark, Offset(ring.x - 0.017f * u, ring.y - 0.008f * u), Size(0.034f * u, 0.016f * u), style = Stroke(pen.lw * 1.6f))
        // The tip of a sock sticks out at the edge, as if something has been stuffed in.
        val tip = q(0.115f, 0f, z0 + 0.0f)
        inkedRound(Rect(tip.x - 0.012f * u, tip.y - 0.006f * u, tip.x + 0.03f * u, tip.y + 0.012f * u), 0.006f * u, UpC.pink, pen, shade = false)
    } else {
        // Open: the lid stands on its hinge at the back, and a sock flies out.
        val lift = open * 0.15f
        val a = q(-0.14f, 0f, z1)
        val b = q(0.14f, 0f, z1)
        val c = q(0.14f, -lift, z1 - 0.1f * (1f - open * 0.2f))
        val d = q(-0.14f, -lift, z1 - 0.1f * (1f - open * 0.2f))
        val lid = Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close() }
        fxFace(lid, UpC.teal.darken(0.1f), pen)
        val sock = q(0.02f, -0.2f * open, 0.06f)
        rotate(40f * open - 20f, sock) {
            inkedRound(Rect(sock.x - 0.015f * u, sock.y - 0.035f * u, sock.x + 0.015f * u, sock.y + 0.005f * u), 0.008f * u, UpC.pink, pen)
            inkedRound(Rect(sock.x - 0.02f * u, sock.y + 0.0f * u, sock.x + 0.03f * u, sock.y + 0.022f * u), 0.009f * u, UpC.pink, pen)
        }
    }
}

// ---------------------------------------------------------------------------------------------- the dumbwaiter

/** A small hatch in the wall with a rolling shutter and a rope that runs up to a pulley; the shutter rolls up when tapped. */
internal fun DrawScope.upDumbwaiter(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val open = sin(PI.toFloat() * (1f - upBeat(f))).coerceIn(0f, 1f) * (if (upBeat(f) > 0.01f) 1f else 0f)
    // The rope up to the pulley, high on the wall.
    drawLine(Ink.line, p(-0.03f, -0.26f), p(-0.03f, -0.52f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    drawLine(Ink.line, p(0.03f, -0.26f), p(0.03f, -0.52f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    inkedCircle(p(0f, -0.54f), 0.032f * u, UpC.steel, pen, shade = false)
    drawCircle(UpC.steelDark, 0.01f * u, p(0f, -0.54f))
    fxBox(u, -0.11f, -0.275f, 0.11f, 0f, 0.03f, UpC.cream, pen, rad = 0.006f)
    // The opening and what is in it.
    val hole = Rect(-0.08f * u, -0.235f * u, 0.08f * u, -0.025f * u)
    drawRect(Color(0xFF3A2A33), hole.topLeft, hole.size)
    clipRect(hole.left, hole.top, hole.right, hole.bottom) {
        fxGlow(Offset(hole.center.x, hole.center.y), 0.11f * u, Color(0xFFFFD27A), 0.35f + 0.5f * open)
        // A tray with a bell.
        val trayY = hole.bottom - 0.03f * u
        drawRect(UpC.oak, Offset(hole.left + 0.01f * u, trayY), Size(hole.width - 0.02f * u, 0.016f * u))
        drawCircle(UpC.brass, 0.014f * u, Offset(hole.center.x - 0.02f * u, trayY - 0.008f * u))
        drawRect(UpC.cream, Offset(hole.center.x + 0.012f * u, trayY - 0.032f * u), Size(0.03f * u, 0.032f * u))
    }
    drawRect(Ink.line, hole.topLeft, hole.size, style = pen.stroke)
    // The shutter rolls up out of the way.
    val shutterH = hole.height * (1f - open * 0.9f)
    clipRect(hole.left, hole.top, hole.right, hole.bottom) {
        drawRect(UpC.oak, hole.topLeft, Size(hole.width, shutterH))
        var y = hole.top + 0.014f * u
        while (y < hole.top + shutterH) {
            drawLine(UpC.oakDark, Offset(hole.left, y), Offset(hole.right, y), strokeWidth = pen.lw * 0.7f)
            y += 0.02f * u
        }
        drawLine(Ink.line, Offset(hole.left, hole.top + shutterH), Offset(hole.right, hole.top + shutterH), strokeWidth = pen.lw)
        if (shutterH > 0.05f * u) {
            drawCircle(UpC.brass, 0.008f * u, Offset(hole.center.x, hole.top + shutterH - 0.016f * u))
            drawCircle(Ink.line, 0.008f * u, Offset(hole.center.x, hole.top + shutterH - 0.016f * u), style = pen.thin)
        }
    }
    // A bell on the top, that rings for the kitchen.
    val bell = p(0.075f, -0.285f)
    drawArc(UpC.brass, 180f, 180f, true, Offset(bell.x - 0.014f * u, bell.y - 0.014f * u), Size(0.028f * u, 0.028f * u))
    drawArc(Ink.line, 180f, 180f, true, Offset(bell.x - 0.014f * u, bell.y - 0.014f * u), Size(0.028f * u, 0.028f * u), style = pen.thin)
}
