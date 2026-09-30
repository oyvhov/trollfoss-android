package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.S
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawIslandMap
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.mapSpot
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.sin

/** The village seen from above. Tap a place and the balloon flies you there. */
@Composable
fun MapScreen(vm: TrollfossViewModel) {
    val motion = LocalMotion.current
    var t by remember { mutableFloatStateOf(0f) }
    var target by remember { mutableStateOf<PlaceId?>(null) }
    val flight = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { t = if (motion) (it - start) / 1e9f else 0f }
    }
    val from = vm.place

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            val pen = Pen(max(1.4f, size.height * 0.0034f), t, if (vm.night) 1f else 0f, vm.weather)
            drawIslandMap(pen, target ?: from, t)
            // The travel balloon: at home over the current place, or flying to the next.
            val a = mapSpot(from)
            val b = mapSpot(target ?: from)
            val p = flight.value
            val x = (a.x + (b.x - a.x) * p) * size.width
            val arc = sin(p * Math.PI.toFloat()) * size.height * 0.18f
            val y = (a.y + (b.y - a.y) * p) * size.height - size.height * 0.1f - arc + sin(t * 1.6f) * size.height * 0.008f
            drawBalloon(Offset(x, y), size.height * 0.075f, pen)
        }

        for (place in PlaceId.entries) {
            val spot = mapSpot(place)
            val label = S.place(place).str()
            Column(
                Modifier
                    .offset(x = w * spot.x - 70.dp, y = h * spot.y - 52.dp)
                    .size(140.dp, 110.dp)
                    .semantics { contentDescription = label; role = Role.Button }
                    .clickable(remember { MutableInteractionSource() }, indication = null) {
                        if (target != null) return@clickable
                        if (place == from) {
                            vm.back()
                            return@clickable
                        }
                        target = place
                        vm.sfx(Sfx.WHOOSH, 0.8f)
                        scope.launch {
                            flight.snapTo(0f)
                            flight.animateTo(1f, tween(if (motion) 1400 else 1, easing = FastOutSlowInEasing))
                            vm.travel(place)
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                GameText(label, fontSize = 19.sp, style = MaterialTheme.typography.titleLarge, color = if (place == (target ?: from)) T.Sun else Color.White, textAlign = TextAlign.Center, maxLines = 1)
            }
        }

        CloseButton({ vm.back() }, Modifier.align(Alignment.TopStart).padding(16.dp))
        Row(Modifier.align(Alignment.BottomStart).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoundButton(S.book.str(), onClick = { vm.open(Screen.Book) }, tone = Tones.Grape, icon = Icons.Book)
            RoundButton(S.workshop.str(), onClick = { vm.open(Screen.Creator(null)) }, tone = Tones.Grape, icon = Icons.Workshop)
        }
        RoundButton(S.parents.str(), onClick = { vm.open(Screen.ParentGate) }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp), size = 52.dp, tone = Tones.Cream, icon = Icons.Gear)
        Box(Modifier) {}
    }
}

/** A striped hot-air balloon with a basket. */
fun DrawScope.drawBalloon(c: Offset, r: Float, pen: Pen) {
    val basket = Rect(c.x - r * 0.28f, c.y + r * 1.25f, c.x + r * 0.28f, c.y + r * 1.62f)
    drawLine(Ink.line, Offset(c.x - r * 0.6f, c.y + r * 0.6f), Offset(basket.left, basket.top), strokeWidth = pen.lw * 0.8f)
    drawLine(Ink.line, Offset(c.x + r * 0.6f, c.y + r * 0.6f), Offset(basket.right, basket.top), strokeWidth = pen.lw * 0.8f)
    inkedRound(basket, r * 0.08f, Color(0xFFC9824A), pen)
    val envelope = Path().apply {
        moveTo(c.x, c.y + r * 0.95f)
        cubicTo(c.x - r * 1.3f, c.y + r * 0.3f, c.x - r * 1.1f, c.y - r * 1.1f, c.x, c.y - r * 1.1f)
        cubicTo(c.x + r * 1.1f, c.y - r * 1.1f, c.x + r * 1.3f, c.y + r * 0.3f, c.x, c.y + r * 0.95f)
        close()
    }
    drawPath(envelope, Color(0xFFD2443A))
    clipPath(envelope) {
        for (i in -2..2 step 2) {
            drawOval(Color(0xFFFFC83D), Offset(c.x + i * r * 0.38f - r * 0.16f, c.y - r * 1.2f), androidx.compose.ui.geometry.Size(r * 0.32f, r * 2.4f))
        }
        drawOval(Color.White.copy(alpha = 0.3f), Offset(c.x - r * 0.7f, c.y - r * 0.9f), androidx.compose.ui.geometry.Size(r * 0.5f, r * 0.7f))
    }
    drawPath(envelope, Ink.line, style = Stroke(pen.lw))
}
