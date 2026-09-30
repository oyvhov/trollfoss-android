package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import app.trollfoss.ui.art.safeRadialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawThing
import app.trollfoss.ui.art.twinkle
import app.trollfoss.ui.components.CloseButton
import kotlin.math.cos
import kotlin.math.sin

/**
 * Looking through the telescope: a round view that drifts over the sky. At night: the Moon, planets and
 * a comet; by day: clouds and birds.
 */
@Composable
fun TelescopeView(night: Boolean, onClose: () -> Unit) {
    val appear = remember { Animatable(0f) }
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(450)) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { t = (it - start) / 1e9f }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f * appear.value))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClose),
    ) {
        Canvas(Modifier.fillMaxSize().graphicsLayer { scaleX = 0.7f + 0.3f * appear.value; scaleY = 0.7f + 0.3f * appear.value; alpha = appear.value }) {
            val r = size.minDimension * 0.44f
            val c = center
            val lens = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
            clipPath(lens) {
                val pan = Offset(sin(t * 0.15f) * r * 0.5f, cos(t * 0.11f) * r * 0.2f)
                if (night) {
                    drawRect(safeRadialGradient(listOf(Color(0xFF231C5C), Color(0xFF07061A)), c, r * 1.2f))
                    for (i in 0 until 90) {
                        val sx = c.x - r * 1.5f + ((i * 137.5f) % 300f) / 100f * r + pan.x
                        val sy = c.y - r * 1.2f + ((i * 61.8f) % 240f) / 100f * r + pan.y
                        twinkle(Offset(sx, sy), 2f + (i % 4), Color.White, 0.5f + 0.5f * sin(t * 2f + i))
                    }
                    val pen = Pen(3f, t)
                    // The Moon, big and close, with Saturn and Jupiter behind.
                    val moon = Offset(c.x - r * 0.25f + pan.x * 0.6f, c.y + pan.y * 0.6f)
                    drawCircle(Color(0xFFE9E6DE), r * 0.42f, moon)
                    for ((dx, dy, s) in listOf(Triple(-0.12f, -0.1f, 0.08f), Triple(0.15f, 0.05f, 0.11f), Triple(-0.05f, 0.18f, 0.06f), Triple(0.2f, -0.2f, 0.05f))) {
                        drawCircle(Color(0xFFCFCABF), r * s, Offset(moon.x + dx * r, moon.y + dy * r))
                    }
                    drawCircle(Color(0x33000000), r * 0.42f, Offset(moon.x + r * 0.12f, moon.y + r * 0.06f))
                    drawCircle(Ink.line, r * 0.42f, moon, style = Stroke(3f))
                    translate(c.x + r * 0.45f + pan.x, c.y - r * 0.2f + pan.y) { drawThing(ThingType.PLANET, 6, 0, r * 0.35f, r * 0.35f, pen) }
                    translate(c.x + r * 0.2f + pan.x * 1.2f, c.y + r * 0.45f + pan.y) { drawThing(ThingType.PLANET, 5, 0, r * 0.25f, r * 0.25f, pen) }
                    // A comet with its tail.
                    val head = Offset(c.x - r * 0.8f + (t * 0.03f % 1f) * r * 1.8f, c.y - r * 0.6f + (t * 0.03f % 1f) * r * 0.3f)
                    drawLine(Brush.linearGradient(listOf(Color.Transparent, Color(0xFFBEE6FF)), head - Offset(r * 0.5f, r * 0.1f), head), head - Offset(r * 0.5f, r * 0.1f), head, strokeWidth = r * 0.05f)
                    drawCircle(Color.White, r * 0.03f, head)
                } else {
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF6CC6FF), Color(0xFFDDF3FF))))
                    for (i in 0 until 5) {
                        val cx = c.x - r * 1.2f + ((i * 0.37f + t * 0.02f) % 1.6f) * r * 1.6f + pan.x
                        val cy = c.y - r * 0.6f + i * r * 0.28f + pan.y
                        drawOval(Color.White, Offset(cx - r * 0.3f, cy - r * 0.08f), Size(r * 0.6f, r * 0.18f))
                        drawOval(Color.White, Offset(cx - r * 0.15f, cy - r * 0.16f), Size(r * 0.3f, r * 0.2f))
                    }
                    for (i in 0 until 3) {
                        val bx = c.x - r + ((t * 0.08f + i * 0.3f) % 1f) * r * 2f
                        val by = c.y - r * 0.3f + i * r * 0.2f + sin(t * 2f + i) * r * 0.03f
                        val flap = sin(t * 8f + i) * r * 0.03f
                        val bird = Path().apply {
                            moveTo(bx - r * 0.06f, by - flap)
                            quadraticTo(bx - r * 0.03f, by - r * 0.03f, bx, by)
                            quadraticTo(bx + r * 0.03f, by - r * 0.03f, bx + r * 0.06f, by - flap)
                        }
                        drawPath(bird, Ink.line, style = Stroke(4f))
                    }
                }
            }
            drawCircle(Color(0xFF3A3340), r, c, style = Stroke(r * 0.07f))
            drawCircle(Color(0xFFC9A04A), r * 1.03f, c, style = Stroke(r * 0.03f))
        }
        CloseButton(onClose, Modifier.align(Alignment.TopEnd).padding(20.dp))
    }
}

