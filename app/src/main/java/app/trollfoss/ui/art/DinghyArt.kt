package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

/** BOAT variant 1: two existing seats sit between the inflatable back and front tubes. */
internal fun DrawScope.drawDinghy(u: Float, pen: Pen, front: Boolean) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val tube = Color(0xFFFFB94F)
    val rubber = Color(0xFF42626B)
    if (!front) {
        inkedRound(Rect(-.18f * u, -.13f * u, .18f * u, -.01f * u), .053f * u, tube, pen)
        inkedOval(Rect(-.13f * u, -.109f * u, .13f * u, -.027f * u), rubber, pen)
        inkedRound(Rect(-.106f * u, -.081f * u, .106f * u, -.035f * u), .015f * u, Color(0xFFB7D3C5), pen)
        for (x in floatArrayOf(-.08f, .08f)) {
            capsule(p(x, -.077f), p(x, -.042f), .028f * u, tube.lighten(.2f), pen)
        }
        capsule(p(-.135f, -.114f), p(.11f, -.115f), .006f * u, Color(0xFFE4C798), pen)
        inkedOval(Rect(.102f * u, -.124f * u, .15f * u, -.109f * u), Color(0xFFE4C798), pen, shade = false)
    } else {
        inkedRound(Rect(-.18f * u, -.06f * u, .18f * u, 0f), .029f * u, tube, pen)
        val rope = Path().apply {
            moveTo(-.14f * u, -.038f * u)
            quadraticTo(0f, -.009f * u, .14f * u, -.038f * u)
        }
        drawPath(rope, rubber, style = pen.thin)
        for (x in floatArrayOf(-.12f, 0f, .12f)) {
            inkedRound(Rect((x - .013f) * u, -.047f * u, (x + .013f) * u, -.027f * u), .005f * u, rubber, pen, shade = false)
        }
        shine(p(-.073f, -.050f), .09f * u, .008f * u, .6f)
        inkedCircle(p(.153f, -.018f), .008f * u, rubber, pen, shade = false)
    }
}
