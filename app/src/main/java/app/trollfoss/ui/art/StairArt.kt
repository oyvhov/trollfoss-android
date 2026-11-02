package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.max
import kotlin.math.min

/** Shared joinery for the houses. Static shapes can stay in the fixture sprite cache. */
private val stairOak = Color(0xFFE3B984)
private val stairSide = Color(0xFFBA8659)
private val stairPaint = Color(0xFFF2EEE6)
internal val stairTeal = Color(0xFF4D8984)

/** The existing doorway rectangle remains the source of truth for both art and travel. */
internal fun DrawScope.stairPortal(u: Float, door: Doorway, pen: Pen) {
    val arch = archPath(door.left * u, door.right * u, door.bottom * u,
        (door.top + 0.09f) * u, door.top * u)
    drawPath(arch, Color(0xFF536C70))
    clipPath(arch) {
        drawPath(fxFlat(u, door.left, door.right, door.bottom, 0f, 0.14f), stairOak)
    }
    drawPath(arch, Ink.line, style = Stroke(0.025f * u))
    drawPath(arch, stairPaint, style = Stroke(0.017f * u))
    drawPath(arch, Ink.line, style = pen.thin)
    stairDirection(u, (door.left + door.right) / 2f, door.top + (door.bottom - door.top) * 0.43f, false, pen)
}

/** A small enamel direction sign, with an arrow that remains legible without reading. */
internal fun DrawScope.stairDirection(u: Float, x: Float, y: Float, down: Boolean, pen: Pen) {
    val c = Offset(x * u, y * u)
    inkedCircle(c, 0.045f * u, stairTeal, pen, shade = false)
    val sign = if (down) 1f else -1f
    val path = Path().apply {
        moveTo(c.x, c.y - sign * 0.021f * u)
        lineTo(c.x, c.y + sign * 0.021f * u)
        moveTo(c.x - 0.018f * u, c.y)
        lineTo(c.x, c.y + sign * 0.021f * u)
        lineTo(c.x + 0.018f * u, c.y)
    }
    drawPath(path, stairPaint, style = Stroke(0.009f * u, cap = StrokeCap.Round))
}

/** Open risers, two slim supporting beams and pale balusters; low/high are the flight's outer edges. */
internal fun DrawScope.stairFlight(
    u: Float, pen: Pen, low: Float, high: Float, height: Float, depth: Float = 0.18f, steps: Int = 8,
) {
    val run = (high - low) / steps
    val rise = height / steps
    fun q(x: Float, y: Float, z: Float = 0f) = fxQ(u, x, y, z)
    val first = low + run * 0.5f
    val last = high - run * 0.5f
    fun rail(z: Float, front: Boolean) {
        for (k in 0 until steps) {
            val x = low + run * (k + 0.5f)
            val y = -rise * (k + 1)
            capsule(q(x, y, z), q(x, y - 0.145f, z),
                (if (k == 0 || k == steps - 1) 0.009f else 0.005f) * u,
                if (front) stairPaint else stairTeal, pen)
        }
        capsule(q(first, -rise - 0.15f, z), q(last, -height - 0.15f, z), 0.011f * u, stairTeal, pen)
    }
    rail(depth, false)
    for (z in floatArrayOf(0.015f, depth - 0.015f)) {
        capsule(q(first, -rise + 0.03f, z), q(last, -height + 0.03f, z), 0.018f * u, stairSide, pen)
    }
    // Paint from the high end to the foot so the nearer tread covers the end behind it.
    for (k in steps - 1 downTo 0) {
        val a = low + run * k
        val b = a + run
        val y = -rise * (k + 1)
        fxBox(u, min(a, b) - 0.006f, y, max(a, b) + 0.006f, y + 0.024f,
            depth, stairOak, pen, rad = 0.004f, top = stairOak.lighten(0.13f), side = stairSide)
        drawLine(stairPaint.copy(alpha = 0.7f), q(min(a, b) + 0.007f, y + 0.005f),
            q(max(a, b) - 0.007f, y + 0.005f), pen.lw * 0.7f, StrokeCap.Round)
    }
    rail(0f, true)
}

/** Three guarded edges, with the near edge open so the entrance and descending treads stay visible. */
internal fun DrawScope.stairWell(u: Float, opening: StairOpening, pen: Pen) {
    val l = opening.left
    val r = opening.right
    val near = opening.near
    val far = opening.far
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    fxFace(fxFlat(u, l - 0.035f, r + 0.035f, 0f, near - 0.025f, far + 0.025f, 0.008f), stairOak, pen)
    val hole = fxFlat(u, l, r, 0f, near, far)
    drawPath(hole, Color(0xFF554B4A))
    clipPath(hole) {
        val stride = (far - near) / 6f
        for (k in 5 downTo 0) {
            val z = near + k * stride
            val tread = fxFlat(u, l + 0.012f, r - 0.012f, k * 0.002f, z, z + stride * 0.88f)
            drawPath(tread, stairOak.darken(k * 0.085f))
            drawPath(tread, Ink.line.copy(alpha = 0.5f), style = pen.thin)
        }
    }
    drawPath(hole, Ink.line, style = pen.thin)
    for (k in 0..4) {
        val x = l + (r - l) * k / 4f
        capsule(q(x, 0f, far), q(x, -0.16f, far), 0.006f * u, stairPaint, pen)
    }
    capsule(q(l, -0.17f, far), q(r, -0.17f, far), 0.012f * u, stairTeal, pen)
    for (x in floatArrayOf(l, r)) {
        for (k in 0..2) {
            val z = near + (far - near) * k / 2f
            capsule(q(x, 0f, z), q(x, -0.17f, z), 0.008f * u, stairPaint, pen)
        }
        capsule(q(x, -0.17f, near), q(x, -0.17f, far), 0.012f * u, stairTeal, pen)
    }
    val sign = q((l + r) / 2f, -0.17f, far)
    stairDirection(u, sign.x / u, sign.y / u, true, pen)
}
