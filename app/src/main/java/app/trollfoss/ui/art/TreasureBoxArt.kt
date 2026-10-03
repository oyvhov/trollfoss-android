package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture

private val TreasureWood = Color(0xFF8A5A3A)
private val TreasureBrass = Color(0xFFD9A441)

/** The treasure box, back layer: a dark inside with three shelves, and the lid standing up when it is open. */
internal fun DrawScope.fxTreasureBox(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val d = 0.1f
    // Little brass feet.
    for (x in floatArrayOf(-0.11f, 0.11f)) fxBox(u, x - 0.012f, -0.012f, x + 0.012f, 0f, 0.014f, TreasureBrass, pen, z = d - 0.02f)
    if (f.open) {
        // The lid, tipped up behind the box, with a star on it.
        fxBox(u, -0.134f, -0.33f, 0.134f, -0.2f, 0.016f, TreasureWood.darken(0.08f), pen, rad = 0.008f, z = d - 0.004f)
        val star = starPath(p(0f, -0.265f), 0.02f * u, 0.009f * u)
        drawPath(star, TreasureBrass)
        drawPath(star, Ink.line, style = pen.thin)
    }
    // The box itself, then its dark inside and the shelves the finds lie on.
    fxBox(u, -0.13f, -0.198f, 0.13f, -0.008f, d, TreasureWood, pen, rad = 0.006f)
    val inside = Rect(p(-0.12f, -0.19f), p(0.12f, -0.015f))
    drawRect(Color(0xFF2B2140), inside.topLeft, inside.size)
    for (y in floatArrayOf(-0.07f, -0.125f)) {
        drawLine(TreasureWood.lighten(0.1f), p(-0.12f, y), p(0.12f, y), strokeWidth = 0.006f * u)
        drawLine(Ink.line, p(-0.12f, y + 0.004f), p(0.12f, y + 0.004f), strokeWidth = pen.lw * 0.6f)
    }
    drawRect(Ink.line, inside.topLeft, inside.size, style = pen.thin)
    if (!f.open) {
        // The shut lid: a low band with a brass star for a lock.
        fxBox(u, -0.134f, -0.21f, 0.134f, -0.194f, d + 0.004f, TreasureWood.darken(0.08f), pen, rad = 0.005f)
        val star = starPath(p(0f, -0.202f), 0.012f * u, 0.005f * u)
        drawPath(star, TreasureBrass)
        drawPath(star, Ink.line, style = pen.thin)
    }
}

/** The treasure box, front layer: the glass over the finds, a brass frame and one streak of light. */
internal fun DrawScope.fxTreasureGlass(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val pane = Rect(p(-0.12f, -0.19f), p(0.12f, -0.015f))
    drawRect(Color(0x30BFE8FF), pane.topLeft, pane.size)
    val streak = Path().apply {
        moveTo(pane.left + 0.03f * u, pane.bottom)
        lineTo(pane.left + 0.06f * u, pane.bottom)
        lineTo(pane.left + 0.11f * u, pane.top)
        lineTo(pane.left + 0.08f * u, pane.top)
        close()
    }
    drawPath(streak, Color.White.copy(alpha = 0.28f))
    drawRect(TreasureBrass, pane.topLeft, pane.size, style = androidx.compose.ui.graphics.drawscope.Stroke(0.008f * u))
    drawRect(Ink.line, pane.topLeft, pane.size, style = pen.thin)
}
