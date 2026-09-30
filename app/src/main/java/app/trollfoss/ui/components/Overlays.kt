package app.trollfoss.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.trollfoss.audio.Sfx
import app.trollfoss.ui.S
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T

/** The one way to close things: a big round red button with a thick white X, always top right. */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 60.dp) {
    RoundButton(S.close.str(), onClick = onClick, modifier = modifier, size = size, tone = Tones.Berry, icon = Icons.Close)
}

/**
 * A popup in the game's style: a cream panel with an ink rim over a dark veil, with the red X on its
 * top corner. A tap on the veil closes it too.
 */
@Composable
fun TrollDialog(onClose: () -> Unit, maxWidth: Dp = 560.dp, content: @Composable ColumnScope.() -> Unit) {
    val motion = LocalMotion.current
    val appear = remember { Animatable(if (motion) 0f else 1f) }
    val feedback = LocalFeedback.current
    LaunchedEffect(Unit) {
        feedback.sfx(Sfx.OPEN, 0.4f)
        if (motion) appear.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.widthIn(max = maxWidth).fillMaxWidth().graphicsLayer {
                    val p = appear.value
                    scaleX = 0.88f + 0.12f * p
                    scaleY = 0.88f + 0.12f * p
                    alpha = p.coerceIn(0f, 1f)
                },
            ) {
                val shape = RoundedCornerShape(30.dp)
                Column(
                    Modifier
                        .padding(top = 22.dp, end = 14.dp)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(Brush.verticalGradient(listOf(Color.White, T.Cream)))
                        .border(3.dp, T.Ink, shape)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .verticalScroll(rememberScrollState())
                        .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content,
                )
                CloseButton(onClose, Modifier.align(Alignment.TopEnd), size = 56.dp)
            }
        }
    }
}
