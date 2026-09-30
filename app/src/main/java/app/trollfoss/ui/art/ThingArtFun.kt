package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

// The joke things: the whoopee cushion, the banana peel and the pepper shaker.

private val WHOOPEE = longArrayOf(0xFFFF6FA8, 0xFFFF5A4E)

/** A flat rubber cushion with its little mouthpiece on the left. */
internal fun DrawScope.thWhoopee(v: Int, w: Float, h: Float, pen: Pen) {
    val color = Color(WHOOPEE[v.mod(WHOOPEE.size)])
    // Mouthpiece first, so the cushion overlaps its root.
    val neck = Rect(-w * 0.5f, -h * 0.62f, -w * 0.28f, -h * 0.3f)
    inkedRound(neck, h * 0.12f, color.darken(0.12f), pen)
    drawLine(Ink.line, Offset(neck.left + w * 0.035f, neck.top), Offset(neck.left + w * 0.035f, neck.bottom), strokeWidth = pen.lw * 0.8f)
    val body = blobPath(
        -w * 0.32f, -h * 0.5f,
        -w * 0.18f, -h * 0.98f,
        w * 0.2f, -h * 1.0f,
        w * 0.5f, -h * 0.62f,
        w * 0.42f, -h * 0.06f,
        0f, 0f,
        -w * 0.3f, -h * 0.12f,
    )
    inked(body, color, pen)
    // A seam round the middle and a rubbery shine.
    val seam = Path().apply {
        moveTo(-w * 0.28f, -h * 0.5f)
        quadraticTo(w * 0.05f, -h * 0.38f, w * 0.44f, -h * 0.52f)
    }
    drawPath(seam, color.darken(0.3f), style = pen.thin)
    shine(Offset(-w * 0.02f, -h * 0.78f), w * 0.22f, h * 0.18f, 0.7f)
}

/** A peel lying flat with its four flaps flopped out. */
internal fun DrawScope.thBananaPeel(w: Float, h: Float, pen: Pen) {
    val yellow = Color(0xFFFFD84A)
    val inside = Color(0xFFFFF4C2)
    val tip = Color(0xFF7A5A2E)
    val flaps = listOf(-0.5f to -0.35f, -0.22f to -0.95f, 0.26f to -0.9f, 0.5f to -0.3f)
    for ((fx, fy) in flaps) {
        val flap = Path().apply {
            moveTo(-w * 0.1f, -h * 0.25f)
            quadraticTo(fx * w * 0.6f, fy * h * 1.1f - h * 0.1f, fx * w, fy * h)
            quadraticTo(fx * w * 0.7f + w * 0.06f, fy * h * 0.6f, w * 0.1f, -h * 0.2f)
            close()
        }
        inked(flap, yellow, pen, shade = false)
        drawCircle(tip, pen.lw * 1.3f, Offset(fx * w, fy * h))
    }
    val middle = Rect(-w * 0.16f, -h * 0.62f, w * 0.16f, -h * 0.02f)
    drawOval(inside, middle.topLeft, middle.size)
    drawOval(Ink.line, middle.topLeft, middle.size, style = pen.stroke)
    drawOval(tip, Offset(-w * 0.04f, -h * 0.45f), androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f))
}

/** A pepper shaker: dark body, shiny cap with holes. */
internal fun DrawScope.thPepper(w: Float, h: Float, pen: Pen) {
    val body = Rect(-w * 0.45f, -h * 0.72f, w * 0.45f, 0f)
    box3d(body, w * 0.18f, Color(0xFF3A3548), pen, radius = w * 0.2f)
    shine(Offset(-w * 0.2f, -h * 0.5f), w * 0.14f, h * 0.2f, 0.45f)
    val cap = Path().apply {
        moveTo(-w * 0.42f, -h * 0.72f)
        quadraticTo(-w * 0.42f, -h * 1.0f, 0f, -h * 1.0f)
        quadraticTo(w * 0.42f, -h * 1.0f, w * 0.42f, -h * 0.72f)
        close()
    }
    inked(cap, Color(0xFFD5DAE6), pen)
    for (k in -1..1) drawCircle(Ink.line, pen.lw * 0.55f, Offset(k * w * 0.16f, -h * 0.86f))
}
