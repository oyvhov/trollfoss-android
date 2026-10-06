package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.First
import app.trollfoss.ui.FirstPop
import app.trollfoss.ui.SO
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** New discoveries: a sticker pops up where it happened, spins once and flies in an arc to the book. */
@Composable
fun FirstPops(vm: TrollfossViewModel, motion: Boolean) {
    LaunchedEffect(vm.firstsPending) { while (vm.firstsPending) withFrameNanos { vm.tickFirsts(it / 1e9f) } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
        val half = with(LocalDensity.current) { 32.dp.toPx() }
        val target = vm.bookAnchor ?: androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.1f)
        for (pop in vm.firstPops.toList()) key(pop) { FlyingSticker(vm, pop, target, w, h, half, motion) }
    }
}

@Composable
private fun FlyingSticker(vm: TrollfossViewModel, pop: FirstPop, target: androidx.compose.ui.geometry.Offset, w: Float, h: Float, half: Float, motion: Boolean) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(pop) { p.animateTo(1f, tween(if (motion) 1400 else 800)); vm.firstPops.remove(pop) }
    val sx = if (pop.x.isNaN()) w / 2f else pop.x
    val sy = if (pop.y.isNaN()) h * 0.45f else pop.y
    val t = p.value
    val up = min(1f, t / 0.3f)
    val fly = ((t - 0.3f) / 0.7f).coerceIn(0f, 1f)
    val x = if (motion) sx + (target.x - sx) * fly else target.x
    val y = if (motion) sy - h * 0.06f * up + (target.y - sy) * fly - sin(fly * PI.toFloat()) * h * 0.12f else target.y
    val label = SO.newFirst.str() + ": " + SO.first(pop.first).str()
    Box(Modifier.offset { IntOffset((x - half).roundToInt(), (y - half).roundToInt()) }
        .graphicsLayer { rotationZ = if (motion) up * 360f else 0f; val s = 1.25f - 0.55f * fly; scaleX = s; scaleY = s }
        .semantics { liveRegion = LiveRegionMode.Polite; contentDescription = label }) {
        FirstSticker(pop.first, Modifier.size(64.dp))
        if (pop.extra > 0) GameText("+${pop.extra}", Modifier.padding(start = 44.dp), fontSize = 18.sp, color = T.Sun)
    }
}

/** A round sticker with the picture of a discovery. */
@Composable
fun FirstSticker(first: First, modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2
            drawCircle(T.Cream, r * 0.94f)
            drawCircle(T.Ink, r * 0.94f, style = Stroke(r * 0.08f))
        }
        FirstPicture(first, Modifier.fillMaxSize().padding(10.dp), found = true)
    }
}
