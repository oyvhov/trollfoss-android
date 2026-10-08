package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/** A quiet glint in the water, with no sign that gives away what lies behind it. */
internal fun DrawScope.waterfallGlimmer(center: Offset, radius: Float, pen: Pen, time: Float = pen.t) {
    val r = radius * (0.92f + 0.08f * kotlin.math.sin(time * 1.5f))
    val path = starPath(center, r, r * 0.3f)
    drawPath(path, Color(0xFF315879), style = Stroke(pen.lw * 2f))
    drawPath(path, Color(0xFFFFF1AC))
    drawCircle(Color.White, r * 0.18f, center)
}

/** The cave's bright water curtain is also the way out; its arrow remains still. */
internal fun DrawScope.waterfallWayOut(st: Stage, pen: Pen) {
    val c = st.o(2.735f, 0.235f)
    val r = 0.035f * st.u
    val arrow = Path().apply {
        moveTo(c.x-r, c.y); lineTo(c.x+r*0.7f, c.y)
        moveTo(c.x, c.y-r*0.65f); lineTo(c.x+r*0.7f, c.y); lineTo(c.x, c.y+r*0.65f)
    }
    drawPath(arrow, Color(0xFF315879), style = Stroke(pen.lw * 4f, cap = StrokeCap.Round))
    drawPath(arrow, Color(0xFFFFF4CD), style = Stroke(pen.lw * 2f, cap = StrokeCap.Round))
}
