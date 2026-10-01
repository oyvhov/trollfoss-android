package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

/**
 * The golden key of Storhuset, lying on its side: a round bow with a three-leaf cut-out, a shaft with a
 * collar and two teeth. Origin at the bottom centre of its box (`-w/2..w/2` by `-h..0`). It twinkles.
 */
internal fun DrawScope.thGoldenKey(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val gold = Color(0xFFFFC83D)
    val goldLight = Color(0xFFFFE27A)
    val goldDark = Color(0xFFC98A00)
    val bowR = h * 0.5f
    val bow = Offset(-w / 2f + bowR * 1.05f, -h * 0.5f)
    // Shaft with a collar, then the two teeth at the end.
    val shaftY = -h * 0.5f
    val shaftEnd = w / 2f
    drawLine(Ink.line, Offset(bow.x + bowR * 0.7f, shaftY), Offset(shaftEnd, shaftY), strokeWidth = h * 0.34f + pen.lw * 1.2f, cap = StrokeCap.Round)
    drawLine(gold, Offset(bow.x + bowR * 0.7f, shaftY), Offset(shaftEnd, shaftY), strokeWidth = h * 0.34f, cap = StrokeCap.Round)
    drawLine(goldLight, Offset(bow.x + bowR * 0.9f, shaftY - h * 0.07f), Offset(shaftEnd - h * 0.1f, shaftY - h * 0.07f), strokeWidth = h * 0.07f, cap = StrokeCap.Round)
    for (k in 0 until 2) {
        val tx = shaftEnd - h * (0.3f + 0.34f * k)
        drawLine(Ink.line, Offset(tx, shaftY), Offset(tx, shaftY + h * 0.46f), strokeWidth = h * 0.2f + pen.lw * 1.2f, cap = StrokeCap.Round)
        drawLine(goldDark, Offset(tx, shaftY), Offset(tx, shaftY + h * 0.44f), strokeWidth = h * 0.2f, cap = StrokeCap.Round)
    }
    // The bow: a ring with a clover hole.
    drawCircle(Ink.line, bowR + pen.lw * 0.7f, bow)
    drawCircle(gold, bowR, bow)
    drawCircle(goldDark, bowR, bow, style = Stroke(h * 0.1f))
    drawCircle(Ink.line, bowR * 0.5f, bow)
    for (a in 0 until 3) {
        val ang = a * 2.0944f - 1.5708f
        drawCircle(Color(0xFF3A2F55), bowR * 0.22f, Offset(bow.x + kotlin.math.cos(ang) * bowR * 0.26f, bow.y + sin(ang) * bowR * 0.26f))
    }
    drawCircle(goldLight, bowR * 0.2f, Offset(bow.x - bowR * 0.45f, bow.y - bowR * 0.5f))
    // A twinkle that comes and goes.
    val tw = (sin(pen.t * 3.1f) + 1f) / 2f
    if (tw > 0.2f) twinkle(Offset(bow.x - bowR * 0.5f, bow.y - bowR * 1.1f), bowR * 0.5f * tw, Color.White, tw)
}
