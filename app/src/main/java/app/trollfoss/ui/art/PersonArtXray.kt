package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.domain.Species
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * What the X-ray at the doctor's shows of Rolf and Sture. Rolf: gears that turn, a heart, and, of course,
 * a rubber duck in his head. Sture: a tiny ghost inside the ghost, giggling, and a few floating hearts.
 * Drawn in the standing frame, like the skeleton of folk.
 */

private val Bone = Color(0xFFF4FFF8)
private val XGlow = Color(0xFF7CFFB2)

/** The X-ray picture of [species]; origin at the feet, [h] the height in pixels. Only for Rolf and Sture. */
fun DrawScope.drawHouseXray(species: Species, h: Float, pen: Pen, t: Float) {
    if (h < 2f) return
    if (species == Species.ROBOT) rolfXray(h, pen, t) else stureXray(h, pen, t)
}

private fun DrawScope.glowLine(path: Path, width: Float, pen: Pen) {
    drawPath(path, XGlow.copy(alpha = 0.35f), style = Stroke(width + pen.lw * 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, Ink.line, style = Stroke(width + pen.lw * 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, Bone, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** A cogwheel: [teeth] teeth around a hole, turned by [turn] radians. */
private fun gearPath(cx: Float, cy: Float, r: Float, teeth: Int, turn: Float): Path = Path().apply {
    val n = teeth * 4
    for (i in 0 until n) {
        val a = turn + i * (2f * PI.toFloat() / n)
        val rr = if (i % 4 == 0 || i % 4 == 1) r else r * 0.78f
        val x = cx + cos(a) * rr
        val y = cy + sin(a) * rr
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun DrawScope.rolfXray(h: Float, pen: Pen, t: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    // The shell, as glowing outlines: head, body, legs and arms.
    glowLine(roundedPoly(0.115f * h, -0.285f * h, -0.93f * h, 0.285f * h, -0.93f * h, 0.30f * h, -0.52f * h, -0.30f * h, -0.52f * h), 0.018f * h, pen)
    glowLine(roundedPoly(0.075f * h, -0.185f * h, -0.50f * h, 0.185f * h, -0.50f * h, 0.215f * h, -0.15f * h, -0.215f * h, -0.15f * h), 0.018f * h, pen)
    for (side in SIDES) {
        val leg = Path().apply {
            moveTo(side * 0.11f * h, -0.15f * h)
            lineTo(side * 0.12f * h, -0.05f * h)
        }
        glowLine(leg, 0.03f * h, pen)
        glowLine(roundedPoly(0.03f * h, side * 0.125f * h - 0.09f * h, -0.065f * h, side * 0.125f * h + 0.09f * h, -0.065f * h, side * 0.125f * h + 0.09f * h, 0f, side * 0.125f * h - 0.09f * h, 0f), 0.014f * h, pen)
        val arm = Path().apply {
            moveTo(side * 0.205f * h, -0.45f * h)
            lineTo(side * 0.285f * h, -0.28f * h)
        }
        glowLine(arm, 0.03f * h, pen)
    }
    // Two gears that mesh and turn the other way round each other.
    val g1 = o(-0.075f, -0.33f)
    val g2 = o(0.045f, -0.285f)
    for ((c, r, teeth, dir) in listOf(Quad(g1, 0.085f * h, 8, 1f), Quad(g2, 0.062f * h, 6, -1.37f))) {
        val gear = gearPath(c.x, c.y, r, teeth, t * 1.6f * dir)
        drawPath(gear, XGlow.copy(alpha = 0.35f), style = Stroke(pen.lw * 4f, join = StrokeJoin.Round))
        drawPath(gear, Bone)
        drawPath(gear, Ink.line, style = pen.stroke)
        drawCircle(Ink.line, r * 0.3f, c)
        drawCircle(XGlow.copy(alpha = 0.7f), r * 0.2f, c)
    }
    // The heart.
    val beat = 1f + 0.16f * kotlin.math.max(0f, sin(t * 5f))
    val heart = heartPath(0f, -0.43f * h, 0.045f * h * beat)
    drawPath(heart, Color(0xFFFF6F91))
    drawPath(heart, Ink.line, style = pen.thin)
    // The head: two bright eyes and a rubber duck.
    for (side in SIDES) {
        drawCircle(XGlow.copy(alpha = 0.35f), 0.062f * h, o(side * 0.125f, -0.742f))
        drawCircle(Bone, 0.045f * h, o(side * 0.125f, -0.742f))
        drawCircle(Ink.line, 0.045f * h, o(side * 0.125f, -0.742f), style = pen.thin)
        drawCircle(Ink.line, 0.02f * h, o(side * 0.125f, -0.742f))
    }
    val bob = sin(t * 3f) * 0.008f
    withTransform({ translate(0f, bob * h) }) {
        drawOval(Color(0xFFFFD23F), o(-0.05f, -0.715f), Size(0.1f * h, 0.06f * h))
        drawOval(Ink.line, o(-0.05f, -0.715f), Size(0.1f * h, 0.06f * h), style = pen.thin)
        drawCircle(Color(0xFFFFD23F), 0.027f * h, o(0.03f, -0.745f))
        drawCircle(Ink.line, 0.027f * h, o(0.03f, -0.745f), style = pen.thin)
        val beak = Path().apply {
            moveTo(0.052f * h, -0.745f * h)
            lineTo(0.078f * h, -0.738f * h)
            lineTo(0.052f * h, -0.73f * h)
            close()
        }
        drawPath(beak, Color(0xFFFF9F43))
    }
}

private data class Quad(val c: Offset, val r: Float, val teeth: Int, val dir: Float)

private fun DrawScope.stureXray(h: Float, pen: Pen, t: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    // The outer sheet, glowing; there is nothing in it but a smaller ghost, giggling.
    glowLine(sheetPath(h, t, 1f), 0.016f * h, pen)
    val giggle = sin(t * 14f) * 0.008f
    withTransform({
        translate(giggle * h, 0f)
        scale(0.48f, 0.48f, pivot = o(0f, -0.3f))
    }) {
        val inner = sheetPath(h, t * 1.5f, 1.5f)
        drawPath(inner, XGlow.copy(alpha = 0.3f), style = Stroke(pen.lw * 7f, join = StrokeJoin.Round))
        drawPath(inner, Bone)
        drawPath(inner, Ink.line, style = Stroke(pen.lw * 2f, join = StrokeJoin.Round))
        for (side in SIDES) drawOval(Ink.line, o(side * 0.12f - 0.05f, -0.78f), Size(0.1f * h, 0.15f * h))
        val smile = Path().apply {
            moveTo(-0.07f * h, -0.62f * h)
            quadraticTo(0f, -0.52f * h, 0.07f * h, -0.62f * h)
        }
        drawPath(smile, Ink.line, style = Stroke(pen.lw * 2f, cap = StrokeCap.Round))
    }
    // Hearts and a twinkle drifting up.
    for (k in 0 until 3) {
        val phase = (t * 0.5f + k * 0.33f).mod(1f)
        val x = (k - 1) * 0.2f + sin(t * 2f + k) * 0.03f
        val y = -0.3f - phase * 0.55f
        drawPath(heartPath(x * h, y * h, 0.03f * h), Color(0xFFFF8FB0).copy(alpha = 1f - phase))
    }
}
