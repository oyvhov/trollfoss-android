package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.ui.theme.T
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Short local responses, drawn over the cached landscape. They never alter the saved world. */
internal data class MapTap(val at: Offset, val born: Float, val water: Boolean)

internal fun DrawScope.drawMapTap(tap: MapTap, now: Float, pen: Pen) {
    val age = (now - tap.born).coerceAtLeast(0f)
    if(age > 1.1f) return
    val p = (age / 1.1f).coerceIn(0f, 1f)
    val fade = (1f - p) * (1f - p)
    val reach = size.height * .065f
    if(tap.water) {
        val radius = reach * (.2f + p)
        drawOval(Color.White.copy(alpha = fade), tap.at - Offset(radius, radius * .28f),
            Size(radius * 2, radius * .56f), style = Stroke(pen.lw * .9f))
    }
    for(k in 0 until 7) {
        val angle = k * (2f * PI.toFloat() / 7f)
        val dx = cos(angle) * reach * p
        val dy = if(tap.water) -sin(k * .38f + .3f) * reach * sin(p * PI.toFloat())
                 else sin(angle) * reach * p - reach * p * .3f
        val c = tap.at + Offset(dx, dy)
        val radius = pen.lw * (1.8f + (k % 3)) * (1f - p * .7f)
        val color = if(tap.water) T.SeaTop else when(k % 3) { 0 -> T.Sun; 1 -> T.Berry; else -> T.Mint }
        if(tap.water) {
            drawCircle(color.copy(alpha = fade), radius, c)
            drawCircle(Color.White.copy(alpha = fade), radius * .35f, c - Offset(radius * .25f, radius * .25f))
        } else {
            drawLine(color.copy(alpha = fade), c - Offset(radius, 0f), c + Offset(radius, 0f), pen.lw, StrokeCap.Round)
            drawLine(color.copy(alpha = fade), c - Offset(0f, radius), c + Offset(0f, radius), pen.lw, StrokeCap.Round)
        }
    }
}

/** A dotted arc behind the travelling balloon, never a full-scene particle field. */
internal fun DrawScope.drawBalloonTrail(from: Offset, to: Offset, flight: Float, pen: Pen) {
    if(flight <= 0f || flight >= 1f) return
    for(k in 1..8) {
        val p = flight - k * .022f
        if(p <= 0f) continue
        val at = Offset((from.x + (to.x - from.x) * p) * size.width,
            ((from.y + (to.y - from.y) * p) * size.height - size.height * .1f - sin(p * PI.toFloat()) * size.height * .18f)
                .coerceIn(size.height * .12f, size.height * .83f))
        drawCircle(if(k % 2 == 0) T.Sun else Color.White, pen.lw * (3f - k * .23f), at,
            alpha = (1f - k / 9f) * sin(flight * PI.toFloat()))
    }
}
