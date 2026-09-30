package app.trollfoss.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.theme.T

/** Icons for the home designer, tidying and tasks, in the same 100 × 100 ink style as [Icons]. */
object DesignIcons {
    private fun DrawScope.u(block: DrawScope.(s: Float, pen: Pen) -> Unit) {
        val s = size.minDimension / 100f
        val dx = (size.width - 100f * s) / 2f
        val dy = (size.height - 100f * s) / 2f
        drawContext.transform.translate(dx, dy)
        block(s, Pen(lw = 5.5f * s))
        drawContext.transform.translate(-dx, -dy)
    }

    /** A paint roller: the home designer. */
    val Roller: DrawScope.() -> Unit = {
        u { s, pen ->
            val handle = Path().apply {
                moveTo(78f * s, 34f * s); lineTo(86f * s, 34f * s); lineTo(86f * s, 52f * s); lineTo(52f * s, 52f * s); lineTo(52f * s, 64f * s)
            }
            drawPath(handle, Ink.line, style = androidx.compose.ui.graphics.drawscope.Stroke(6f * s, cap = StrokeCap.Round))
            inkedRound(Rect(44f * s, 62f * s, 60f * s, 92f * s), 5f * s, T.Berry, pen)
            inkedRound(Rect(12f * s, 18f * s, 80f * s, 42f * s), 10f * s, T.Sun, pen)
            drawLine(Color.White.copy(alpha = 0.7f), Offset(20f * s, 25f * s), Offset(66f * s, 25f * s), 4f * s, StrokeCap.Round)
        }
    }

    val Sofa: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(16f * s, 28f * s, 84f * s, 62f * s), 12f * s, T.MintTop, pen)
            inkedRound(Rect(8f * s, 50f * s, 92f * s, 78f * s), 10f * s, T.Mint, pen)
            inkedRound(Rect(4f * s, 42f * s, 22f * s, 80f * s), 8f * s, T.Mint, pen)
            inkedRound(Rect(78f * s, 42f * s, 96f * s, 80f * s), 8f * s, T.Mint, pen)
            drawLine(Ink.line, Offset(18f * s, 80f * s), Offset(18f * s, 90f * s), 5f * s, StrokeCap.Round)
            drawLine(Ink.line, Offset(82f * s, 80f * s), Offset(82f * s, 90f * s), 5f * s, StrokeCap.Round)
        }
    }

    /** A strip of wallpaper with flowers. */
    val Wallpaper: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(20f * s, 8f * s, 80f * s, 92f * s), 6f * s, Color(0xFFFFE3EE), pen, shade = false)
            for (k in 0 until 3) {
                val c = Offset(50f * s + (if (k % 2 == 0) -12f else 12f) * s, 26f * s + k * 24f * s)
                for (a in 0 until 5) {
                    val ang = a * 1.2566f
                    drawCircle(T.Berry, 5f * s, Offset(c.x + kotlin.math.cos(ang) * 7f * s, c.y + kotlin.math.sin(ang) * 7f * s))
                }
                drawCircle(T.Sun, 4f * s, c)
            }
        }
    }

    /** Floorboards seen in perspective. */
    val Floor: DrawScope.() -> Unit = {
        u { s, pen ->
            val floor = Path().apply {
                moveTo(28f * s, 30f * s); lineTo(72f * s, 30f * s); lineTo(94f * s, 82f * s); lineTo(6f * s, 82f * s); close()
            }
            inked(floor, Color(0xFFD9A066), pen)
            for (k in 1 until 5) {
                val t = k / 5f
                drawLine(Ink.line, Offset((28f + 44f * t) * s, 30f * s), Offset((6f + 88f * t) * s, 82f * s), 3f * s)
            }
        }
    }

    /** A cardboard box: the store. */
    val Box: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(14f * s, 40f * s, 86f * s, 88f * s), 4f * s, Color(0xFFD6A26A), pen)
            val lid1 = Path().apply { moveTo(14f * s, 40f * s); lineTo(4f * s, 24f * s); lineTo(46f * s, 24f * s); lineTo(50f * s, 40f * s); close() }
            val lid2 = Path().apply { moveTo(86f * s, 40f * s); lineTo(96f * s, 24f * s); lineTo(54f * s, 24f * s); lineTo(50f * s, 40f * s); close() }
            inked(lid1, Color(0xFFE8BC86), pen)
            inked(lid2, Color(0xFFE8BC86), pen)
            drawLine(Color(0xFFB8844E), Offset(50f * s, 42f * s), Offset(50f * s, 86f * s), 5f * s)
        }
    }

    /** A broom with a sparkle: tidy up. */
    val Broom: DrawScope.() -> Unit = {
        u { s, pen ->
            drawLine(Ink.line, Offset(70f * s, 8f * s), Offset(42f * s, 58f * s), 9f * s, StrokeCap.Round)
            drawLine(Color(0xFFC98A55), Offset(70f * s, 8f * s), Offset(42f * s, 58f * s), 5f * s, StrokeCap.Round)
            val bristles = Path().apply {
                moveTo(34f * s, 52f * s); lineTo(52f * s, 62f * s); lineTo(40f * s, 92f * s); lineTo(10f * s, 78f * s); close()
            }
            inked(bristles, T.Sun, pen)
            for (k in 0 until 3) drawLine(Ink.line, Offset((40f - k * 6f) * s, (60f + k * 2f) * s), Offset((32f - k * 7f) * s, (84f - k * 3f) * s), 3f * s)
            drawPath(starPath(Offset(80f * s, 64f * s), 12f * s, 5f * s), T.SunTop)
            drawPath(starPath(Offset(80f * s, 64f * s), 12f * s, 5f * s), Ink.line, style = pen.thin)
        }
    }

    /** A clipboard with ticks: the tasks. */
    val Tasks: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(18f * s, 14f * s, 82f * s, 94f * s), 8f * s, Color(0xFFC98A55), pen)
            inkedRound(Rect(26f * s, 24f * s, 74f * s, 86f * s), 4f * s, Color.White, pen, shade = false)
            inkedRound(Rect(36f * s, 6f * s, 64f * s, 22f * s), 5f * s, T.Sea, pen)
            for (k in 0 until 3) {
                val y = (38f + k * 16f) * s
                val tick = Path().apply { moveTo(32f * s, y); lineTo(37f * s, y + 5f * s); lineTo(45f * s, y - 5f * s) }
                drawPath(tick, if (k < 2) T.Mint else T.CreamLine, style = androidx.compose.ui.graphics.drawscope.Stroke(5f * s, cap = StrokeCap.Round))
                drawLine(T.CreamLine, Offset(52f * s, y), Offset(68f * s, y), 4f * s, StrokeCap.Round)
            }
        }
    }

    /** A round sticker with a star. */
    val Sticker: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedCircle(Offset(50f * s, 50f * s), 40f * s, T.Berry, pen)
            drawPath(starPath(Offset(50f * s, 52f * s), 24f * s, 11f * s), T.SunTop)
            drawPath(starPath(Offset(50f * s, 52f * s), 24f * s, 11f * s), Ink.line, style = pen.thin)
            drawOval(Color.White.copy(alpha = 0.5f), Offset(26f * s, 20f * s), Size(20f * s, 12f * s))
        }
    }
}
