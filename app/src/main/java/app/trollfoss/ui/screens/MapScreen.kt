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
import androidx.compose.ui.graphics.graphicsLayer
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
import app.trollfoss.ui.art.drawIslandMapLive
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.mapSpot
import app.trollfoss.ui.art.rememberMapLayer
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
    // The troll face in the mountain yawns when tapped (an Easter egg).
    var yawnAt by remember { mutableFloatStateOf(-10f) }
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
        // The still scenery is drawn once into a bitmap (off the main thread); each frame draws only what moves.
        val mapLayer = rememberMapLayer(constraints.maxWidth, constraints.maxHeight, if (vm.night) 1f else 0f, vm.weather)
        // The map is its own layer: every frame only it is redrawn, not the labels and buttons over it.
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val pen = Pen(max(1.4f, size.height * 0.0034f), t, if (vm.night) 1f else 0f, vm.weather)
            drawIslandMapLive(pen, target ?: from, t, mapLayer.value)
            val yawn = (t - yawnAt) / 2.4f
            if (yawn in 0f..1f) {
                val open = sin(yawn * Math.PI.toFloat())
                val c = Offset(size.width * 0.41f, size.height * 0.25f)
                val sz = size.height * 0.07f
                // Eyes squeezed shut, a huge yawn, and a few z rising from the mountain.
                for (side in intArrayOf(-1, 1)) drawLine(Color(0xFF3B3346), Offset(c.x + side * sz * 0.56f, c.y - sz * 0.2f), Offset(c.x + side * sz * 0.12f, c.y - sz * 0.2f), strokeWidth = pen.lw * 1.6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawOval(Color(0xFF2B2140), Offset(c.x - sz * 0.22f * open, c.y + sz * 0.12f), androidx.compose.ui.geometry.Size(sz * 0.44f * open, sz * 0.5f * open))
                drawOval(Color(0xFFFF7A9A), Offset(c.x - sz * 0.14f * open, c.y + sz * 0.42f), androidx.compose.ui.geometry.Size(sz * 0.28f * open, sz * 0.14f * open))
                for (k in 0 until 3) {
                    val zt = (yawn * 1.6f - k * 0.22f).coerceIn(0f, 1f)
                    if (zt > 0f && zt < 1f) drawPath(
                        Path().apply {
                            val zx = c.x + sz * (0.7f + k * 0.35f)
                            val zy = c.y - sz * (0.3f + zt * 1.4f + k * 0.2f)
                            val zs = sz * (0.16f + k * 0.05f)
                            moveTo(zx - zs, zy - zs); lineTo(zx + zs, zy - zs); lineTo(zx - zs, zy + zs); lineTo(zx + zs, zy + zs)
                        },
                        Color.White.copy(alpha = 1f - zt), style = Stroke(pen.lw * 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    )
                }
            }
            // The travel balloon: at home over the current place, or flying to the next.
            val a = mapSpot(from)
            val b = mapSpot(target ?: from)
            val p = flight.value
            val x = (a.x + (b.x - a.x) * p) * size.width
            val arc = sin(p * Math.PI.toFloat()) * size.height * 0.18f
            val y = (a.y + (b.y - a.y) * p) * size.height - size.height * 0.1f - arc + sin(t * 1.6f) * size.height * 0.008f
            drawBalloon(Offset(x, y), size.height * 0.075f, pen)
        }

        Box(
            Modifier
                .offset(x = w * 0.41f - 48.dp, y = h * 0.25f - 40.dp)
                .size(96.dp, 80.dp)
                .clickable(remember { MutableInteractionSource() }, indication = null) {
                    if (t - yawnAt < 2.4f) return@clickable
                    yawnAt = t
                    vm.sfx(Sfx.SNORE, 0.9f, 0.55f)
                    vm.sfx(Sfx.ROAR, 0.35f, 0.5f)
                    vm.sim.egg("mountain")
                },
        )
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
