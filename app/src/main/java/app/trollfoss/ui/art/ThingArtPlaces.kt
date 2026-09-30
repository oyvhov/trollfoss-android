package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath

// Things from the tivoli, the shop, the doctor, the stage and the sea floor.

private val FLOSS = longArrayOf(0xFFFFA8D2, 0xFF9AD7FF)

internal fun DrawScope.thCandyFloss(v: Int, w: Float, h: Float, pen: Pen) {
    val stick = Offset(0f, -h * 0.02f)
    inkLine(stick, Offset(0f, -h * 0.5f), pen, pen.lw * 2.2f, Color(0xFFF2E3C4))
    drawLine(Ink.line, stick, Offset(0f, -h * 0.5f), strokeWidth = pen.lw * 0.6f)
    val c = Color(FLOSS[v.mod(FLOSS.size)])
    val cloud = blobPath(
        -w * 0.5f, -h * 0.62f, -w * 0.42f, -h * 0.9f, -w * 0.1f, -h * 1.0f, w * 0.25f, -h * 0.97f,
        w * 0.5f, -h * 0.75f, w * 0.4f, -h * 0.5f, 0f, -h * 0.42f, -w * 0.35f, -h * 0.46f,
    )
    inked(cloud, c, pen)
    for (k in 0 until 5) {
        val x = -w * 0.3f + k * w * 0.14f
        val y = -h * (0.62f + (k % 2) * 0.16f)
        drawCircle(Color.White.copy(alpha = 0.45f), w * 0.07f, Offset(x, y))
    }
}

internal fun DrawScope.thPopcorn(w: Float, h: Float, pen: Pen) {
    // Puffs first, the box in front.
    val puff = Color(0xFFFFF6DE)
    for (k in 0 until 7) {
        val x = -w * 0.34f + (k % 4) * w * 0.22f + (k / 4) * w * 0.1f
        val y = -h * (0.7f + (k / 4) * 0.14f)
        inkedCircle(Offset(x, y), w * 0.15f, puff, pen, shade = false)
        drawCircle(Color(0xFFFFD66B), w * 0.05f, Offset(x + w * 0.03f, y + w * 0.03f))
    }
    val box = Path().apply {
        moveTo(-w * 0.46f, -h * 0.7f)
        lineTo(w * 0.46f, -h * 0.7f)
        lineTo(w * 0.34f, 0f)
        lineTo(-w * 0.34f, 0f)
        close()
    }
    drawPath(box, Color.White)
    clipPath(box) {
        for (k in 0 until 5) {
            drawRect(Color(0xFFE8304A), Offset(-w * 0.46f + k * w * 0.2f, -h * 0.7f), Size(w * 0.1f, h * 0.7f))
        }
    }
    drawPath(box, Ink.line, style = pen.stroke)
}

private val SODA = longArrayOf(0xFFD8323F, 0xFFFF8A2A, 0xFFF3D23A)

internal fun DrawScope.thSoda(v: Int, used: Int, w: Float, h: Float, pen: Pen) {
    val c = Color(SODA[v.mod(SODA.size)])
    val body = Rect(-w * 0.45f, -h * 0.92f, w * 0.45f, 0f)
    inkedRound(body, w * 0.18f, c, pen)
    // A white wave band and the ring pull on top.
    val band = Path().apply {
        moveTo(-w * 0.45f, -h * 0.52f)
        quadraticTo(0f, -h * 0.66f, w * 0.45f, -h * 0.5f)
        lineTo(w * 0.45f, -h * 0.4f)
        quadraticTo(0f, -h * 0.55f, -w * 0.45f, -h * 0.42f)
        close()
    }
    clipPath(roundPath(body, w * 0.18f)) { drawPath(band, Color.White.copy(alpha = 0.85f)) }
    drawOval(Color(0xFFD5DAE6), Offset(-w * 0.38f, -h * 1.0f), Size(w * 0.76f, h * 0.14f))
    drawOval(Ink.line, Offset(-w * 0.38f, -h * 1.0f), Size(w * 0.76f, h * 0.14f), style = pen.thin)
    if (used < 3) drawOval(Ink.line, Offset(-w * 0.08f, -h * 0.97f), Size(w * 0.22f, h * 0.06f), style = pen.thin)
    shine(Offset(-w * 0.22f, -h * 0.72f), w * 0.12f, h * 0.24f, 0.6f)
}

internal fun DrawScope.thSyrup(w: Float, h: Float, pen: Pen) {
    val bottle = Rect(-w * 0.45f, -h * 0.72f, w * 0.45f, 0f)
    inkedRound(bottle, w * 0.25f, Color(0xFF9A4B2A), pen)
    inkedRound(Rect(-w * 0.2f, -h * 0.86f, w * 0.2f, -h * 0.7f), w * 0.05f, Color(0xFF9A4B2A), pen)
    inkedRound(Rect(-w * 0.3f, -h * 1.0f, w * 0.3f, -h * 0.84f), w * 0.08f, Color.White, pen)
    // A label with a red cross.
    val label = Rect(-w * 0.34f, -h * 0.5f, w * 0.34f, -h * 0.16f)
    drawRect(Color(0xFFFFF7EA), label.topLeft, label.size)
    drawRect(Ink.line, label.topLeft, label.size, style = pen.thin)
    val cx = 0f
    val cy = -h * 0.33f
    val a = w * 0.08f
    drawRect(Color(0xFFE8304A), Offset(cx - a / 2, cy - a * 1.5f), Size(a, a * 3f))
    drawRect(Color(0xFFE8304A), Offset(cx - a * 1.5f, cy - a / 2), Size(a * 3f, a))
    shine(Offset(-w * 0.22f, -h * 0.6f), w * 0.12f, h * 0.12f, 0.5f)
}

internal fun DrawScope.thBandage(w: Float, h: Float, pen: Pen) {
    val strip = Rect(-w * 0.5f, -h * 0.85f, w * 0.5f, -h * 0.15f)
    inkedRound(strip, h * 0.35f, Color(0xFFF0C9A2), pen, shade = false)
    val pad = Rect(-w * 0.16f, -h * 0.8f, w * 0.16f, -h * 0.2f)
    drawRoundRect(Color(0xFFFFF1E4), pad.topLeft, pad.size, androidx.compose.ui.geometry.CornerRadius(h * 0.1f))
    for (k in 0 until 2) for (s in listOf(-1f, 1f)) drawCircle(Color(0xFFC89A72), pen.lw * 0.45f, Offset(s * w * (0.3f + k * 0.08f), -h * 0.5f))
}

internal fun DrawScope.thThermometer(w: Float, h: Float, pen: Pen) {
    val tube = Rect(-w * 0.35f, -h, w * 0.35f, -h * 0.15f)
    inkedRound(tube, w * 0.35f, Color(0xFFEAF6FF), pen, shade = false)
    drawLine(Color(0xFFE8304A), Offset(0f, -h * 0.2f), Offset(0f, -h * 0.62f), strokeWidth = w * 0.3f, cap = StrokeCap.Round)
    inkedCircle(Offset(0f, -h * 0.13f), w * 0.5f, Color(0xFFE8304A), pen)
    for (k in 0 until 5) drawLine(Ink.line, Offset(w * 0.15f, -h * (0.35f + k * 0.12f)), Offset(w * 0.33f, -h * (0.35f + k * 0.12f)), strokeWidth = pen.lw * 0.5f)
}

internal fun DrawScope.thStethoscope(w: Float, h: Float, pen: Pen) {
    val tube = Color(0xFF3A3548)
    val y = Path().apply {
        moveTo(-w * 0.32f, -h * 0.95f)
        quadraticTo(-w * 0.34f, -h * 0.45f, 0f, -h * 0.45f)
        quadraticTo(w * 0.34f, -h * 0.45f, w * 0.32f, -h * 0.95f)
        moveTo(0f, -h * 0.45f)
        quadraticTo(-w * 0.05f, -h * 0.2f, w * 0.22f, -h * 0.22f)
    }
    drawPath(y, Ink.line, style = Stroke(pen.lw * 3.2f, cap = StrokeCap.Round))
    drawPath(y, tube, style = Stroke(pen.lw * 1.8f, cap = StrokeCap.Round))
    for (s in listOf(-1f, 1f)) inkedCircle(Offset(s * w * 0.32f, -h * 0.95f), w * 0.06f, Color(0xFFD5DAE6), pen, shade = false)
    inkedCircle(Offset(w * 0.28f, -h * 0.2f), w * 0.17f, Color(0xFFD5DAE6), pen)
    drawCircle(Color(0xFF8A94A8), w * 0.09f, Offset(w * 0.28f, -h * 0.2f))
}

internal fun DrawScope.thMicrophone(w: Float, h: Float, pen: Pen) {
    inkedRound(Rect(-w * 0.28f, -h * 0.62f, w * 0.28f, 0f), w * 0.25f, Color(0xFF2E2A3A), pen)
    inkedCircle(Offset(0f, -h * 0.76f), w * 0.5f, Color(0xFFB8C0CF), pen)
    for (k in -1..1) {
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(k * w * 0.2f, -h * 0.94f), Offset(k * w * 0.2f, -h * 0.58f), strokeWidth = pen.lw * 0.5f)
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(-w * 0.44f, -h * 0.76f + k * w * 0.2f), Offset(w * 0.44f, -h * 0.76f + k * w * 0.2f), strokeWidth = pen.lw * 0.5f)
    }
    shine(Offset(-w * 0.16f, -h * 0.84f), w * 0.2f, w * 0.14f, 0.7f)
}

internal fun DrawScope.thPearl(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h / 2)
    inkedCircle(c, w * 0.5f, Color(0xFFF7F1FF), pen)
    drawCircle(Color(0xFFFFD6EC).copy(alpha = 0.6f), w * 0.3f, c + Offset(w * 0.1f, w * 0.08f))
    shine(c + Offset(-w * 0.15f, -w * 0.16f), w * 0.26f, w * 0.18f, 0.95f)
}

/** Goggles with a snorkel, centred in their box like glasses. */
internal fun DrawScope.thDivingMask(w: Float, h: Float, pen: Pen) {
    val cy = -h / 2
    // The snorkel rises on the right.
    val snorkel = Path().apply {
        moveTo(w * 0.42f, cy + h * 0.2f)
        lineTo(w * 0.48f, cy - h * 0.9f)
    }
    drawPath(snorkel, Ink.line, style = Stroke(w * 0.09f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(snorkel, Color(0xFFFF8A2A), style = Stroke(w * 0.09f, cap = StrokeCap.Round))
    val frame = Rect(-w * 0.42f, cy - h * 0.4f, w * 0.42f, cy + h * 0.4f)
    inkedRound(frame, h * 0.3f, Color(0xFF2F9BFF), pen)
    val glass = Rect(-w * 0.35f, cy - h * 0.28f, w * 0.35f, cy + h * 0.28f)
    drawRoundRect(Color(0xFFCFF0FF).copy(alpha = 0.55f), glass.topLeft, glass.size, androidx.compose.ui.geometry.CornerRadius(h * 0.22f))
    drawRoundRect(Ink.line, glass.topLeft, glass.size, androidx.compose.ui.geometry.CornerRadius(h * 0.22f), style = pen.thin)
    shine(Offset(-w * 0.2f, cy - h * 0.12f), w * 0.14f, h * 0.12f, 0.8f)
}
