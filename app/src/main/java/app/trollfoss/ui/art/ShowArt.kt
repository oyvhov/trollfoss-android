package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.ShowPlay
import kotlin.math.cos
import kotlin.math.sin

/** Level 8 show toys, drawn from the bottom centre in scene units × [u]: echo box, dance floor, confetti machine and light rig. */
fun DrawScope.drawShowBack(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in ShowPlay.TYPES) return false
    val wood = Color(0xFFDEA66C); val pale = Color(0xFFFFF0CC); val dark = Color(0xFF564768)
    fun box(l: Float, t: Float, r: Float, b: Float, c: Color) = inkedRound(Rect(l * u, t * u, r * u, b * u), u * 0.012f, c, pen)
    fun dot(x: Float, y: Float, r: Float, c: Color) = inkedCircle(Offset(x * u, y * u), r * u, c, pen)
    fun line(x: Float, y: Float, xx: Float, yy: Float, c: Color = Ink.line, w: Float = pen.lw) = drawLine(c, Offset(x * u, y * u), Offset(xx * u, yy * u), w, StrokeCap.Round)
    when (f.type) {
        FixtureType.PLAY_ECHO_BOX -> {
            // A wooden box with a big speaker and two curious eyes; sound rings while it sings back.
            box(-0.13f, -0.24f, 0.13f, 0f, wood)
            dot(0f, -0.09f, 0.065f, dark); dot(0f, -0.09f, 0.03f, pale)
            dot(-0.045f, -0.19f, 0.022f, Color.White); dot(0.045f, -0.19f, 0.022f, Color.White)
            drawCircle(Ink.line, 0.01f * u, Offset(-0.045f * u, -0.19f * u)); drawCircle(Ink.line, 0.01f * u, Offset(0.045f * u, -0.19f * u))
            inked(Path().apply { moveTo(0.06f * u, -0.24f * u); lineTo(0.13f * u, -0.31f * u); lineTo(0.15f * u, -0.27f * u); lineTo(0.1f * u, -0.24f * u); close() }, ToyColors[3], pen)
            if (f.anim > 0.05f) for (k in 1..3) drawArc(ToyColors[4].copy(alpha = f.anim), -50f, 100f, false,
                Offset((0.1f - k * 0.04f) * u, (-0.09f - k * 0.06f) * u), Size((0.08f + k * 0.08f) * u, (0.12f * k) * u), style = Stroke(pen.lw * 1.4f))
        }
        FixtureType.PLAY_DANCE_FLOOR -> {
            // Four coloured tiles; when the music is on they take turns to light up.
            box(-0.4f, -0.05f, 0.4f, 0f, dark)
            for (i in 0 until 4) {
                val lit = f.on && ((pen.t * 2.5f).toInt() + i) % 2 == 0
                val c = ToyColors[(i * 2 + if (f.on) (pen.t * 1.2f).toInt() else 0) % ToyColors.size]
                drawRect(if (lit) c else c.copy(alpha = 0.45f), Offset((-0.38f + i * 0.19f) * u, -0.042f * u), Size(0.17f * u, 0.034f * u))
            }
        }
        FixtureType.PLAY_CONFETTI -> {
            // A red machine on wheels with a funnel, confetti waiting on top.
            box(-0.13f, -0.3f, 0.13f, -0.05f, ToyColors[0]); dot(-0.08f, -0.03f, 0.03f, dark); dot(0.08f, -0.03f, 0.03f, dark)
            inked(Path().apply { moveTo(-0.07f * u, -0.3f * u); lineTo(-0.11f * u, -0.4f * u); lineTo(0.11f * u, -0.4f * u); lineTo(0.07f * u, -0.3f * u); close() }, ToyColors[3], pen)
            for (i in 0 until 6) drawRect(ToyColors[i % ToyColors.size], Offset((-0.08f + i * 0.03f) * u, (-0.42f - (i % 2) * 0.012f) * u), Size(0.016f * u, 0.016f * u))
            box(-0.09f, -0.22f, 0.09f, -0.14f, pale)
            if (f.anim > 0.05f) for (i in 0 until 5) line(0f, -0.4f, sin(i * 1.2f) * 0.12f, -0.4f - 0.1f * f.anim, ToyColors[i % ToyColors.size], pen.lw * 1.5f)
        }
        FixtureType.PLAY_LIGHT_RIG -> {
            // A stand with a spotlight, and a little disco ball on top that sparkles in disco mode.
            box(-0.1f, -0.03f, 0.1f, 0f, dark); line(0f, -0.03f, 0f, -0.52f, dark, pen.lw * 3)
            val lit = f.mode == 1
            inked(Path().apply { moveTo(0.02f * u, -0.56f * u); lineTo(0.15f * u, -0.5f * u); lineTo(0.12f * u, -0.45f * u); lineTo(0.0f, -0.5f * u); close() }, if (lit) Color(0xFFFFE3A0) else Color(0xFF8C8FA3), pen)
            dot(0f, -0.57f, 0.035f, Color(0xFFCCD3E0))
            val spin = if (f.mode == 2) f.angle else 0f
            for (k in 0 until 4) { val a = spin + k * 1.57f; drawCircle(if (f.mode == 2) ToyColors[k] else Color.White, 0.008f * u, Offset((cos(a) * 0.02f) * u, (-0.57f + sin(a) * 0.02f) * u)) }
        }
        else -> Unit
    }
    return true
}

fun DrawScope.drawShowFront(f: Fixture, u: Float, pen: Pen): Boolean = f.type in ShowPlay.TYPES
