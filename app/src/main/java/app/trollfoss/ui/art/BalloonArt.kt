package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.*
import app.trollfoss.domain.*

/** A flying iron bed: mattress, pillows and passengers suspended beneath the balloon. */
fun DrawScope.drawBalloon(c: Offset, r: Float, pen: Pen, riders: List<Look> = emptyList()) {
    val envelope = Path().apply {
        moveTo(c.x, c.y + r * 0.95f)
        cubicTo(c.x - r * 1.3f, c.y + r * 0.3f, c.x - r * 1.1f, c.y - r * 1.1f, c.x, c.y - r * 1.1f)
        cubicTo(c.x + r * 1.1f, c.y - r * 1.1f, c.x + r * 1.3f, c.y + r * 0.3f, c.x, c.y + r * 0.95f)
        close()
    }
    drawPath(envelope, Color(0xFFD2443A))
    clipPath(envelope) {
        for (i in -2..2 step 2) {
            drawOval(Color(0xFFFFC83D), Offset(c.x + i * r * 0.38f - r * 0.16f, c.y - r * 1.2f), androidx.compose.ui.geometry.Size(r * 0.32f, r * 2.4f))
        }
        drawOval(Color.White.copy(alpha = 0.3f), Offset(c.x - r * 0.7f, c.y - r * 0.9f), androidx.compose.ui.geometry.Size(r * 0.5f, r * 0.7f))
    }
    drawPath(envelope, Ink.line, style = Stroke(pen.lw))
    val bed = Rect(c.x - r * 1.08f, c.y + r * 1.27f, c.x + r * 1.08f, c.y + r * 1.57f)
    for (side in floatArrayOf(-1f, 1f)) {
        drawLine(Ink.line, Offset(c.x + side * r * 0.6f, c.y + r * 0.6f), Offset(c.x + side * r, bed.top), strokeWidth = pen.lw)
        drawLine(Color(0xFFF2DEAC), Offset(c.x + side * r * 0.6f, c.y + r * 0.6f), Offset(c.x + side * r, bed.top), strokeWidth = pen.lw * 0.45f)
    }
    inkedRound(bed, r * 0.12f, Color(0xFFFFF3D8), pen)
    for (i in 0..1) inkedRound(Rect(c.x - r * 0.9f + i * r * 1.25f, bed.top - r * 0.16f, c.x - r * 0.4f + i * r * 1.25f, bed.top + r * 0.02f), r * 0.08f, Color.White, pen)
    val a = PersonAnim().apply { face = app.trollfoss.domain.Face.HAPPY; wave = if (riders.size == 1) 1f else 0f }
    for ((i, look) in riders.withIndex()) {
        val x = c.x + (i - (riders.size - 1) / 2f) * r * (1.65f / riders.size.coerceAtLeast(3))
        translate(x, bed.top + r * 0.1f) { drawPerson(Species.FOLK, look, Pose.SIT, a, r * 0.88f, Pen(pen.lw * 0.5f, pen.t), seed = i.toFloat()) }
    }
    inkedRound(Rect(bed.left + r * 0.1f, bed.top + r * 0.12f, bed.right - r * 0.1f, bed.bottom), r * 0.06f, Color(0xFF68A4CB), pen)
    for (i in -3..3) drawLine(Color(0xFFE9F2F8), Offset(c.x + i * r * 0.27f, bed.top + r * 0.15f), Offset(c.x + i * r * 0.27f, bed.bottom), strokeWidth = pen.lw * 0.6f)
    // Curved iron headboard and footboard, with little brass knobs.
    for (side in floatArrayOf(-1f, 1f)) {
        val x = c.x + side * r * 1.08f
        val rail = Path().apply {
            moveTo(x, bed.bottom + r * 0.13f); lineTo(x, bed.top - r * 0.46f)
            quadraticTo(x - side * r * 0.08f, bed.top - r * 0.75f, x - side * r * 0.34f, bed.top - r * 0.46f)
            lineTo(x - side * r * 0.34f, bed.bottom)
        }
        drawPath(rail, Ink.line, style = Stroke(pen.lw * 1.4f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        inkedCircle(Offset(x, bed.top - r * 0.46f), r * 0.055f, Color(0xFFFFC83D), pen, shade = false)
    }
}
