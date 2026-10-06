package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.ToyReward
import app.trollfoss.ui.SO
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class ParcelPhase { FALL, WAIT, POP }

/**
 * A new level: a gift parcel floats down on a parachute where the child plays, wiggles and giggles until
 * tapped, then pops: four new toys spring out, one stays in the room and the rest fly to the furniture button.
 * Only the parcel takes touches; the scene around it plays on.
 */
@Composable
fun GiftParcel(vm: TrollfossViewModel, motion: Boolean) {
    val level = remember { vm.levelGift.takeIf { it > 0 } ?: vm.parcelOpening }
    var phase by remember { mutableStateOf(if (motion) ParcelPhase.FALL else ParcelPhase.WAIT) }
    val fall = remember { Animatable(if (motion) 0f else 1f) }
    val pop = remember { Animatable(0f) }
    var t by remember { mutableFloatStateOf(0f) }
    var placed by remember { mutableIntStateOf(-1) }
    val rewards = remember(level) { ToyReward.entries.filter { it.level == level } }
    LaunchedEffect(Unit) { var start = -1L; while (true) withFrameNanos { if (start < 0) start = it; t = (it - start) / 1e9f } }
    LaunchedEffect(Unit) { if (motion) { fall.animateTo(1f, tween(1600, easing = LinearEasing)); phase = ParcelPhase.WAIT } }
    LaunchedEffect(phase) {
        if (phase == ParcelPhase.WAIT) while (true) { kotlinx.coroutines.delay(3000); vm.sfx(Sfx.GIGGLE, 0.4f, 1.2f) }
        if (phase == ParcelPhase.POP) { pop.animateTo(1f, tween(if (motion) 1300 else 500)); vm.parcelDone() }
    }
    val label = SO.parcel.str()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
        val density = LocalDensity.current
        val side = with(density) { (if (h / density.density < 520f) 72.dp else 96.dp).toPx() }
        val cx = w / 2f
        val ground = h * 0.62f
        val y = -side * 2f + (ground + side * 2f) * fall.value
        if (phase != ParcelPhase.POP) {
            val wiggle = if (motion && phase == ParcelPhase.WAIT) sin(t * 6f) * 8f else 0f
            Box(Modifier.offset { IntOffset((cx - side / 2).roundToInt(), (y - side / 2).roundToInt()) }
                .size(with(density) { side.toDp() })
                .graphicsLayer { rotationZ = wiggle }
                .semantics { contentDescription = label; role = Role.Button }
                .clickable(remember { MutableInteractionSource() }, indication = null) {
                    if (phase == ParcelPhase.POP) return@clickable
                    placed = if (vm.openParcel()) 0 else -1
                    phase = ParcelPhase.POP
                }) {
                Canvas(Modifier.fillMaxSize()) { drawParcel(t, phase == ParcelPhase.FALL, motion) }
            }
        } else {
            val p = pop.value
            Canvas(Modifier.fillMaxSize()) { drawConfetti(Offset(cx, ground), p, motion) }
            val target = (if (vm.screen == Screen.Play) vm.furnishAnchor else vm.bookAnchor) ?: Offset(w * 0.9f, h * 0.9f)
            val toy = with(density) { 64.dp.toPx() }
            rewards.forEachIndexed { i, reward ->
                val a = (-0.75f + i * 0.5f) * PI.toFloat() / 2f
                val out = Offset(cx + sin(a) * w * 0.18f, ground - h * 0.22f - kotlin.math.cos(a) * h * 0.06f)
                val stay = i == placed
                val k = ((p - 0.45f) / 0.55f).coerceIn(0f, 1f)
                val spring = (p / 0.45f).coerceIn(0f, 1f)
                val base = Offset(cx + (out.x - cx) * spring, ground + (out.y - ground) * spring)
                val end = if (stay) Offset(cx, ground) else target
                val at = if (motion) Offset(base.x + (end.x - base.x) * k, base.y + (end.y - base.y) * k) else out
                Box(Modifier.offset { IntOffset((at.x - toy / 2).roundToInt(), (at.y - toy / 2).roundToInt()) }
                    .graphicsLayer { val s = 1f - 0.6f * k; scaleX = s; scaleY = s; alpha = 1f - 0.4f * k }) {
                    ToyPicture(reward, 64.dp)
                }
            }
        }
    }
}

/** A pink parcel with a yellow ribbon, two curious eyes and, while falling, a cream parachute. */
private fun DrawScope.drawParcel(t: Float, falling: Boolean, motion: Boolean) {
    val s = size.minDimension; val lw = s * 0.035f
    val box = Offset(s * 0.18f, s * 0.42f); val bs = Size(s * 0.64f, s * 0.52f)
    if (falling) {
        val top = Offset(s * 0.5f, s * 0.02f)
        drawArc(T.Cream, 180f, 180f, true, Offset(s * 0.08f, -s * 0.18f), Size(s * 0.84f, s * 0.5f))
        drawArc(T.Ink, 180f, 180f, true, Offset(s * 0.08f, -s * 0.18f), Size(s * 0.84f, s * 0.5f), style = Stroke(lw))
        for (x in floatArrayOf(0.12f, 0.5f, 0.88f)) drawLine(T.Ink, Offset(s * x, top.y + s * 0.05f), Offset(s * 0.5f, box.y), strokeWidth = lw * 0.6f)
    }
    drawRoundRect(Color(0xFFE45B78), box, bs, CornerRadius(s * 0.06f))
    drawRoundRect(T.Ink, box, bs, CornerRadius(s * 0.06f), style = Stroke(lw))
    drawRect(T.Sun, Offset(s * 0.45f, box.y), Size(s * 0.1f, bs.height))
    drawCircle(T.Sun, s * 0.09f, Offset(s * 0.4f, box.y - s * 0.04f)); drawCircle(T.Sun, s * 0.09f, Offset(s * 0.6f, box.y - s * 0.04f))
    val blink = motion && t % 3.2f < 0.14f
    for (side in intArrayOf(-1, 1)) {
        val e = Offset(s * 0.5f + side * s * 0.15f, box.y + bs.height * 0.45f)
        if (blink) drawLine(T.Ink, Offset(e.x - s * 0.05f, e.y), Offset(e.x + s * 0.05f, e.y), strokeWidth = lw)
        else { drawCircle(Color.White, s * 0.06f, e); drawCircle(T.Ink, s * 0.03f, e) }
    }
    drawArc(T.Ink, 20f, 140f, false, Offset(s * 0.42f, box.y + bs.height * 0.55f), Size(s * 0.16f, s * 0.1f), style = Stroke(lw))
}

private val CONFETTI = listOf(T.Sun, T.Berry, T.Sea, T.Mint)

private fun DrawScope.drawConfetti(from: Offset, p: Float, motion: Boolean) {
    if (!motion) return
    for (i in 0 until 40) {
        val a = i * 2.39996f
        val speed = 0.6f + (i % 7) / 7f
        val x = from.x + kotlin.math.cos(a) * size.width * 0.3f * speed * p
        val y = from.y - kotlin.math.sin(a).coerceAtLeast(-0.3f) * size.height * 0.35f * speed * p + size.height * 0.4f * p * p
        drawRect(CONFETTI[i % 4], Offset(x, y), Size(size.minDimension * 0.012f, size.minDimension * 0.02f), alpha = 1f - p * 0.8f)
    }
}
