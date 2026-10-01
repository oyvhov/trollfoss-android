package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.domain.Anatomy
import app.trollfoss.domain.Look
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import kotlin.math.sqrt

/*
 * The two figures of the big house: Rolf the polite robot butler (PersonArtRolf.kt) and Sture the shy ghost
 * (PersonArtSture.kt). Both stand upright, so they sit on seats and lie in beds exactly as folk do (see
 * Anatomy), and both come from here, called by drawPerson. They are drawn to the anchors in Anatomy: hats,
 * glasses and held things are drawn by the engine on top of them.
 */


/** Draws Rolf or Sture with the origin at their feet (sitting: hips, lying: the middle of the back). */
internal fun DrawScope.drawHouseFigure(
    species: Species,
    look: Look,
    pose: Pose,
    a: PersonAnim,
    h: Float,
    pen: Pen,
    holding: Boolean,
    seed: Float,
) {
    // A figure scaled down to nothing (a shy ghost popping out of sight) is simply not there.
    if (h < 1.5f) return
    val safe = look.safe()
    when (pose) {
        Pose.LIE -> withTransform({
            translate(0f, -0.22f * h)
            rotate(-90f, Offset.Zero)
            translate(0f, 0.5f * h)
        }) { upright(species, safe, pose, a, h, pen, holding, seed) }
        Pose.SIT -> withTransform({ translate(0f, Anatomy.HIPS * h) }) { upright(species, safe, pose, a, h, pen, holding, seed) }
        else -> upright(species, safe, pose, a, h, pen, holding, seed)
    }
}

private fun DrawScope.upright(species: Species, look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, holding: Boolean, seed: Float) {
    if (species == Species.ROBOT) rolf(look, pose, a, h, pen, holding, seed) else sture(look, pose, a, h, pen, holding, seed)
}

/**
 * A polygon with every corner rounded by up to [r] (the corner never takes more than half of a side).
 * [p] holds x, y pairs in drawing order.
 */
internal fun roundedPoly(r: Float, vararg p: Float): Path {
    val n = p.size / 2
    val path = Path()
    for (i in 0 until n) {
        val px = p[i * 2]
        val py = p[i * 2 + 1]
        val prev = (i + n - 1) % n
        val next = (i + 1) % n
        val ax = p[prev * 2] - px
        val ay = p[prev * 2 + 1] - py
        val bx = p[next * 2] - px
        val by = p[next * 2 + 1] - py
        val la = sqrt(ax * ax + ay * ay).coerceAtLeast(0.0001f)
        val lb = sqrt(bx * bx + by * by).coerceAtLeast(0.0001f)
        val ra = minOf(r, la / 2f)
        val rb = minOf(r, lb / 2f)
        val sx = px + ax / la * ra
        val sy = py + ay / la * ra
        if (i == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
        path.quadraticTo(px, py, px + bx / lb * rb, py + by / lb * rb)
    }
    path.close()
    return path
}

/** A heart of half-width about [s] with its middle at ([cx], [cy]). */
internal fun heartPath(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + 0.9f * s)
    cubicTo(cx - 1.25f * s, cy + 0.1f * s, cx - 1.1f * s, cy - 0.95f * s, cx, cy - 0.35f * s)
    cubicTo(cx + 1.1f * s, cy - 0.95f * s, cx + 1.25f * s, cy + 0.1f * s, cx, cy + 0.9f * s)
    close()
}

/** 0 to 1 smooth step. */
internal fun smooth(x: Float): Float {
    val c = x.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}
