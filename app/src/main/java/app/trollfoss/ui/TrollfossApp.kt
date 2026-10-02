package app.trollfoss.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawPlaceBack
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.LocalFeedback
import app.trollfoss.ui.screens.BookScreen
import app.trollfoss.ui.screens.TasksScreen
import app.trollfoss.ui.screens.CreatorScreen
import app.trollfoss.ui.screens.MapScreen
import app.trollfoss.ui.screens.ParentGateScreen
import app.trollfoss.ui.screens.ParentScreen
import app.trollfoss.ui.screens.PlayScreen
import app.trollfoss.ui.screens.PlayersScreen
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun TrollfossApp(vm: TrollfossViewModel) {
    val motion = LocalMotion.current

    // Look for a new version whenever the app comes to the front; the updater rate-limits itself.
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(lifecycle) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.updater.checkIfDue()
            awaitCancellation()
        }
    }

    CompositionLocalProvider(LocalMaalform provides vm.settings.maalform, LocalFeedback provides vm.feedback) {
        BackHandler(enabled = vm.screen != Screen.Play) { vm.back() }
        Box(Modifier.fillMaxSize().background(T.Night)) {
            // The play screen stays underneath so the world keeps its place; others slide over it.
            PlayScreen(vm)
            AnimatedContent(
                targetState = vm.screen,
                transitionSpec = {
                    if (motion) (fadeIn(tween(260)) + scaleIn(tween(320), initialScale = 0.94f)) togetherWith fadeOut(tween(180))
                    else EnterTransition.None togetherWith ExitTransition.None
                },
                label = "screen",
            ) { target ->
                when (target) {
                    Screen.Play -> Box(Modifier)
                    Screen.Map -> MapScreen(vm)
                    Screen.Players -> PlayersScreen(vm)
                    is Screen.Creator -> CreatorScreen(vm, target.editId)
                    Screen.Book -> BookScreen(vm)
                Screen.Tasks -> TasksScreen(vm)
                    Screen.ParentGate -> ParentGateScreen(vm)
                    Screen.Parent -> ParentScreen(vm)
                }
            }
            if (vm.splash) Splash(onDone = { vm.splash = false })
        }
    }
}

/** The name bounces in letter by letter over the waterfall, then the village appears. */
@Composable
private fun Splash(onDone: () -> Unit) {
    val fade = remember { Animatable(1f) }
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (t < 2.2f) withFrameNanos { t = (it - start) / 1e9f }
    }
    LaunchedEffect(Unit) {
        delay(1900)
        fade.animateTo(0f, tween(500))
        onDone()
    }
    Box(Modifier.fillMaxSize().graphicsLayer { alpha = fade.value }) {
        Canvas(Modifier.fillMaxSize()) {
            drawPlaceBack(PlaceId.FOREST, 2.0f, size.height, Pen(size.height * 0.0034f, t))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0x882B2140))))
        }
        Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            val colors = listOf(T.Sun, T.Berry, T.Sea, T.Mint, T.Grape, T.Sun, T.Berry, T.Sea, T.Mint)
            "Trollfoss".forEachIndexed { i, ch ->
                val local = (t - i * 0.08f).coerceAtLeast(0f)
                val drop = if (local < 0.5f) (1f - local / 0.5f) else 0f
                val squash = if (local in 0.5f..0.9f) sin((local - 0.5f) / 0.4f * Math.PI.toFloat()) * 0.18f else 0f
                GameText(
                    ch.toString(),
                    fontSize = 88.sp,
                    style = MaterialTheme.typography.displayLarge,
                    color = colors[i % colors.size],
                    modifier = Modifier.graphicsLayer {
                        translationY = -drop * drop * 600f - abs(sin(t * 3f + i)) * 6f
                        scaleX = 1f + squash
                        scaleY = 1f - squash
                        alpha = if (local > 0f) 1f else 0f
                    },
                )
            }
        }
    }
}

