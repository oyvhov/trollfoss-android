package app.trollfoss.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.inkedOval
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.theme.T
import kotlin.math.cos
import kotlin.math.sin

/** Draws [icon] in a square canvas. Icons draw into a 100 × 100 box scaled to the canvas. */
@Composable
fun IconCanvas(icon: DrawScope.() -> Unit, modifier: Modifier = Modifier) {
    Canvas(modifier) { icon() }
}

/**
 * The drawn icons of Trollfoss. Each is a lambda for [IconCanvas] or [RoundButton]; they share the ink
 * style of the rest of the game, so the buttons look like they belong in the world.
 */
object Icons {
    private fun DrawScope.u(block: DrawScope.(s: Float, pen: Pen) -> Unit) {
        val s = size.minDimension / 100f
        val dx = (size.width - 100f * s) / 2f
        val dy = (size.height - 100f * s) / 2f
        drawContext.transform.translate(dx, dy)
        block(s, Pen(lw = 5.5f * s))
        drawContext.transform.translate(-dx, -dy)
    }

    val Map: DrawScope.() -> Unit = {
        u { s, pen ->
            val island = Path().apply {
                moveTo(10f * s, 70f * s)
                quadraticTo(22f * s, 40f * s, 50f * s, 38f * s)
                quadraticTo(80f * s, 40f * s, 90f * s, 70f * s)
                quadraticTo(50f * s, 86f * s, 10f * s, 70f * s)
                close()
            }
            inked(island, T.MintTop, pen)
            val hill = Path().apply {
                moveTo(28f * s, 64f * s)
                lineTo(48f * s, 36f * s)
                lineTo(66f * s, 64f * s)
                close()
            }
            inked(hill, Color(0xFF7CCBFF), pen)
            drawLine(T.Ink, Offset(50f * s, 38f * s), Offset(50f * s, 12f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
            val flag = Path().apply {
                moveTo(50f * s, 12f * s)
                lineTo(74f * s, 20f * s)
                lineTo(50f * s, 28f * s)
                close()
            }
            inked(flag, T.Berry, pen)
        }
    }

    val Sun: DrawScope.() -> Unit = {
        u { s, pen ->
            for (i in 0 until 8) {
                val a = i * Math.PI / 4
                drawLine(T.Ink, Offset((50 + cos(a) * 30).toFloat() * s, (50 + sin(a) * 30).toFloat() * s), Offset((50 + cos(a) * 44).toFloat() * s, (50 + sin(a) * 44).toFloat() * s), strokeWidth = 7f * s, cap = StrokeCap.Round)
            }
            inkedCircle(Offset(50f * s, 50f * s), 22f * s, T.Sun, pen)
        }
    }

    val Moon: DrawScope.() -> Unit = {
        u { s, pen ->
            val moon = Path().apply {
                moveTo(60f * s, 10f * s)
                cubicTo(20f * s, 12f * s, 10f * s, 70f * s, 50f * s, 88f * s)
                cubicTo(70f * s, 94f * s, 86f * s, 80f * s, 90f * s, 64f * s)
                cubicTo(60f * s, 72f * s, 40f * s, 40f * s, 60f * s, 10f * s)
                close()
            }
            inked(moon, Color(0xFFFFF4C9), pen)
            drawPath(starPath(Offset(78f * s, 28f * s), 11f * s, 4.5f * s), T.Sun)
        }
    }

    private fun DrawScope.cloud(s: Float, pen: Pen, color: Color) {
        val cloud = Path().apply {
            moveTo(18f * s, 62f * s)
            cubicTo(4f * s, 60f * s, 8f * s, 38f * s, 26f * s, 40f * s)
            cubicTo(28f * s, 18f * s, 60f * s, 14f * s, 66f * s, 34f * s)
            cubicTo(86f * s, 30f * s, 96f * s, 56f * s, 80f * s, 62f * s)
            close()
        }
        inked(cloud, color, pen)
    }

    val Rain: DrawScope.() -> Unit = {
        u { s, pen ->
            for (i in 0 until 3) drawLine(T.Sea, Offset((30f + i * 18f) * s, 70f * s), Offset((25f + i * 18f) * s, 88f * s), strokeWidth = 7f * s, cap = StrokeCap.Round)
            cloud(s, pen, Color(0xFFE8F4FF))
        }
    }

    val Snow: DrawScope.() -> Unit = {
        u { s, pen ->
            for (i in 0 until 3) inkedCircle(Offset((30f + i * 20f) * s, (78f + (i % 2) * 8f) * s), 6f * s, Color.White, Pen(3f * s), shade = false)
            cloud(s, pen, Color(0xFFD9E6FF))
        }
    }

    val SunCloud: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedCircle(Offset(64f * s, 36f * s), 20f * s, T.Sun, pen)
            val cloud = Path().apply {
                moveTo(16f * s, 78f * s)
                cubicTo(2f * s, 76f * s, 8f * s, 56f * s, 24f * s, 58f * s)
                cubicTo(28f * s, 40f * s, 54f * s, 40f * s, 58f * s, 56f * s)
                cubicTo(76f * s, 54f * s, 84f * s, 76f * s, 70f * s, 78f * s)
                close()
            }
            inked(cloud, Color.White, pen)
        }
    }

    val Camera: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(34f * s, 18f * s, 64f * s, 34f * s), 6f * s, T.Sun, pen)
            inkedRound(Rect(10f * s, 28f * s, 90f * s, 82f * s), 14f * s, T.SeaTop, pen)
            inkedCircle(Offset(50f * s, 55f * s), 18f * s, T.Cream, pen)
            inkedCircle(Offset(50f * s, 55f * s), 9f * s, T.Ink, Pen(2f * s), shade = false)
            drawCircle(Color.White, 3.5f * s, Offset(46f * s, 51f * s))
        }
    }

    val Workshop: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedCircle(Offset(46f * s, 50f * s), 32f * s, Color(0xFFFFC9A6), pen)
            drawCircle(T.Ink, 4.5f * s, Offset(35f * s, 46f * s))
            drawCircle(T.Ink, 4.5f * s, Offset(57f * s, 46f * s))
            drawArc(T.Ink, 20f, 140f, false, Offset(32f * s, 50f * s), Size(28f * s, 18f * s), style = Stroke(5f * s, cap = StrokeCap.Round))
            val hair = Path().apply {
                moveTo(14f * s, 46f * s)
                cubicTo(14f * s, 10f * s, 78f * s, 10f * s, 78f * s, 46f * s)
                quadraticTo(60f * s, 30f * s, 46f * s, 34f * s)
                quadraticTo(28f * s, 30f * s, 14f * s, 46f * s)
                close()
            }
            inked(hair, Color(0xFFC74B2A), pen)
            drawPath(starPath(Offset(82f * s, 78f * s), 16f * s, 7f * s), T.Sun)
            drawPath(starPath(Offset(82f * s, 78f * s), 16f * s, 7f * s), T.Ink, style = Stroke(3.5f * s))
        }
    }

    val Book: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(14f * s, 14f * s, 86f * s, 86f * s), 10f * s, T.Grape, pen)
            inkedRound(Rect(24f * s, 14f * s, 86f * s, 78f * s), 8f * s, T.Cream, Pen(3.5f * s), shade = false)
            drawPath(starPath(Offset(55f * s, 46f * s), 20f * s, 8.5f * s), T.Sun)
            drawPath(starPath(Offset(55f * s, 46f * s), 20f * s, 8.5f * s), T.Ink, style = Stroke(4f * s))
        }
    }

    val Gear: DrawScope.() -> Unit = {
        u { s, pen ->
            for (i in 0 until 8) {
                val a = i * Math.PI / 4
                drawLine(T.Ink, Offset(50f * s, 50f * s), Offset((50 + cos(a) * 40).toFloat() * s, (50 + sin(a) * 40).toFloat() * s), strokeWidth = 16f * s, cap = StrokeCap.Round)
                drawLine(Color(0xFFB9C0D8), Offset(50f * s, 50f * s), Offset((50 + cos(a) * 38).toFloat() * s, (50 + sin(a) * 38).toFloat() * s), strokeWidth = 9f * s, cap = StrokeCap.Round)
            }
            inkedCircle(Offset(50f * s, 50f * s), 27f * s, Color(0xFFB9C0D8), pen)
            inkedCircle(Offset(50f * s, 50f * s), 10f * s, T.Cream, pen, shade = false)
        }
    }

    val Close: DrawScope.() -> Unit = {
        u { s, _ ->
            val a = Offset(26f * s, 26f * s)
            val b = Offset(74f * s, 74f * s)
            val c = Offset(74f * s, 26f * s)
            val d = Offset(26f * s, 74f * s)
            drawLine(T.Ink, a, b, strokeWidth = 24f * s, cap = StrokeCap.Round)
            drawLine(T.Ink, c, d, strokeWidth = 24f * s, cap = StrokeCap.Round)
            drawLine(Color.White, a, b, strokeWidth = 13f * s, cap = StrokeCap.Round)
            drawLine(Color.White, c, d, strokeWidth = 13f * s, cap = StrokeCap.Round)
        }
    }

    val Check: DrawScope.() -> Unit = {
        u { s, _ ->
            val path = Path().apply {
                moveTo(20f * s, 52f * s)
                lineTo(42f * s, 74f * s)
                lineTo(82f * s, 28f * s)
            }
            drawPath(path, T.Ink, style = Stroke(22f * s, cap = StrokeCap.Round))
            drawPath(path, Color.White, style = Stroke(12f * s, cap = StrokeCap.Round))
        }
    }

    val Dice: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(16f * s, 16f * s, 84f * s, 84f * s), 16f * s, Color.White, pen)
            for ((x, y) in listOf(34f to 34f, 66f to 34f, 50f to 50f, 34f to 66f, 66f to 66f)) drawCircle(T.Ink, 6.5f * s, Offset(x * s, y * s))
        }
    }

    val Star: DrawScope.() -> Unit = {
        u { s, pen ->
            val star = starPath(Offset(50f * s, 54f * s), 42f * s, 18f * s)
            inked(star, T.Sun, pen)
        }
    }

    val Lock: DrawScope.() -> Unit = {
        u { s, pen ->
            drawArc(T.Ink, 180f, 180f, false, Offset(30f * s, 14f * s), Size(40f * s, 40f * s), style = Stroke(10f * s))
            inkedRound(Rect(20f * s, 40f * s, 80f * s, 88f * s), 10f * s, T.Sun, pen)
            drawCircle(T.Ink, 6f * s, Offset(50f * s, 62f * s))
        }
    }

    val Refresh: DrawScope.() -> Unit = {
        u { s, _ ->
            drawArc(T.Ink, 30f, 280f, false, Offset(20f * s, 20f * s), Size(60f * s, 60f * s), style = Stroke(14f * s, cap = StrokeCap.Round))
            drawArc(Color.White, 30f, 280f, false, Offset(20f * s, 20f * s), Size(60f * s, 60f * s), style = Stroke(6f * s, cap = StrokeCap.Round))
            val tip = Path().apply {
                moveTo(66f * s, 16f * s)
                lineTo(90f * s, 30f * s)
                lineTo(66f * s, 44f * s)
                close()
            }
            drawPath(tip, Color.White)
            drawPath(tip, T.Ink, style = Stroke(4f * s))
        }
    }

    val Bag: DrawScope.() -> Unit = {
        u { s, pen ->
            drawArc(T.Ink, 180f, 180f, false, Offset(32f * s, 10f * s), Size(36f * s, 30f * s), style = Stroke(9f * s))
            inkedRound(Rect(16f * s, 26f * s, 84f * s, 90f * s), 18f * s, Color(0xFFC9824A), pen)
            inkedRound(Rect(30f * s, 50f * s, 70f * s, 74f * s), 8f * s, T.SunTop, pen)
            drawLine(T.Ink, Offset(16f * s, 44f * s), Offset(84f * s, 44f * s), strokeWidth = 5f * s)
        }
    }

    val Back: DrawScope.() -> Unit = {
        u { s, _ ->
            val path = Path().apply {
                moveTo(62f * s, 18f * s)
                lineTo(30f * s, 50f * s)
                lineTo(62f * s, 82f * s)
            }
            drawPath(path, T.Ink, style = Stroke(22f * s, cap = StrokeCap.Round))
            drawPath(path, Color.White, style = Stroke(12f * s, cap = StrokeCap.Round))
        }
    }

    val House: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(20f * s, 44f * s, 80f * s, 88f * s), 6f * s, Color(0xFFD2443A), pen)
            val roof = Path().apply {
                moveTo(10f * s, 50f * s)
                lineTo(50f * s, 14f * s)
                lineTo(90f * s, 50f * s)
                close()
            }
            inked(roof, Color(0xFF6FAE5A), pen)
            inkedRound(Rect(42f * s, 60f * s, 58f * s, 88f * s), 4f * s, T.Cream, pen)
        }
    }
}
