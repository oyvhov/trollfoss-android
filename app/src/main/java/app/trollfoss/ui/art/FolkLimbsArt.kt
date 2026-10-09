package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Look
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import kotlin.math.sin

/** One continuous outline keeps elbows and knees soft instead of stacking outlined circles. */
internal fun DrawScope.bentLimb(start: Offset, bend: Offset, end: Offset, width: Float, color: Color, pen: Pen) {
    val path = Path().apply { moveTo(start.x, start.y); quadraticTo(bend.x, bend.y, end.x, end.y) }
    drawPath(path, Ink.line, style = Stroke(width + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

internal fun DrawScope.folkLegs(look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, t: Float,
    skin: Color, trousers: Color, shoe: Color, overalls: Boolean, bob: Float, stepL: Float, stepR: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val dressed = look.bottom == 0 || look.bottom == 3 || overalls
    val color = if (dressed) trousers else skin
    val dancing = a.motion && a.dance > 0f
    for (side in floatArrayOf(-1f, 1f)) {
        val kick = if (a.motion && (pose == Pose.HELD || pose == Pose.SWIM)) sin(t * 11f) * side * .028f else 0f
        val lift = kick + (if (dancing) side * sin(a.dance) * .021f else 0f) - (if (side < 0f) stepL else stepR) * .05f
        val sit = pose == Pose.SIT
        val hip = o(side * .077f, (if (sit) -.17f else -.25f) + bob)
        val ankle = o(side * (if (pose == Pose.FLOAT) .12f else .088f), -.051f + lift)
        val knee = o(side * .102f, (if (sit) -.075f else -.14f) + lift * .7f)
        bentLimb(hip, knee, ankle, .089f * h, color, pen)
        if (look.bottom == 1 && !overalls) capsule(hip, o(side * .087f, -.17f + bob), .10f * h, trousers, pen)
        if (look.bottom == 4 && !overalls) {
            for (k in 0..2) drawLine(trousers, o(side * .088f - .042f, -.095f + k * .018f + lift),
                o(side * .088f + .042f, -.095f + k * .018f + lift), .012f * h)
        }
        val x = side * .094f
        val boot = Path().apply {
            moveTo((x - .061f) * h, (-.065f + lift) * h)
            quadraticTo(x * h, (-.083f + lift) * h, (x + .043f) * h, (-.051f + lift) * h)
            quadraticTo((x + .087f) * h, (-.051f + lift) * h, (x + .086f) * h, (-.023f + lift) * h)
            quadraticTo((x + .067f) * h, (.002f + lift) * h, (x - .058f) * h, (-.013f + lift) * h)
            quadraticTo((x - .073f) * h, (-.035f + lift) * h, (x - .061f) * h, (-.065f + lift) * h)
            close()
        }
        inked(boot, shoe, pen)
        drawLine(shoe.lighten(.6f), o(x - .045f, -.018f + lift), o(x + .059f, -.019f + lift), pen.lw * .8f, StrokeCap.Round)
        for (k in 0..1) drawLine(shoe.darken(.24f), o(x - .014f + k * .023f, -.051f + lift),
            o(x - .025f + k * .023f, -.037f + lift), pen.lw * .65f, StrokeCap.Round)
    }
}
