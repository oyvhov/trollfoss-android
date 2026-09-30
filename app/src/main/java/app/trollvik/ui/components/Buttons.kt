package app.trollvik.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.trollvik.audio.Sfx
import app.trollvik.ui.theme.LocalMotion
import app.trollvik.ui.theme.T

/** Sound and vibration for controls, provided once from the root so buttons stay plain composables. */
interface Feedback {
    fun sfx(effect: Sfx, volume: Float = 0.9f, rate: Float = 1f)
    fun tap() = sfx(Sfx.TAP)
}

val LocalFeedback = staticCompositionLocalOf<Feedback> { object : Feedback { override fun sfx(effect: Sfx, volume: Float, rate: Float) = Unit } }

/** The three colours of a glossy surface. */
data class Tone(val top: Color, val face: Color, val edge: Color)

object Tones {
    val Sun = Tone(T.SunTop, T.Sun, T.SunDeep)
    val Berry = Tone(T.BerryTop, T.Berry, T.BerryDeep)
    val Sea = Tone(T.SeaTop, T.Sea, T.SeaDeep)
    val Mint = Tone(T.MintTop, T.Mint, T.MintDeep)
    val Grape = Tone(T.GrapeTop, T.Grape, T.GrapeDeep)
    val Cream = Tone(Color.White, T.Cream, T.CreamLine)
    val Night = Tone(T.NightTop, lerp(T.Night, T.NightTop, 0.5f), T.Night)
}

/**
 * Paints a shape the way toy buttons are painted: a gradient from a lighter top, a soft gloss over the
 * upper half and a thin ink rim.
 */
fun Modifier.gloss(face: Color, shape: Shape, top: Color = lerp(face, Color.White, 0.22f), rim: Float = 0.5f): Modifier =
    drawWithCache {
        val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        val fill = Brush.verticalGradient(0f to top, 0.55f to face, 1f to lerp(face, T.Ink, 0.1f))
        val shine = if (face.luminance() > 0.8f) 0.08f else 0.22f
        val highlight = Brush.verticalGradient(0f to Color.White.copy(alpha = shine), 0.48f to Color.White.copy(alpha = shine * 0.3f), 0.52f to Color.Transparent)
        val rimStroke = Stroke(width = 2.2.dp.toPx())
        onDrawBehind {
            drawPath(path, fill)
            drawPath(path, highlight)
            drawPath(path, T.Ink.copy(alpha = rim), style = rimStroke)
        }
    }

/**
 * A pressable slab with a darker edge underneath. On press the face sinks onto the edge and squashes a
 * little, which gives a physical click without any ripple.
 */
@Composable
fun PressSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tones.Cream,
    shape: Shape = RoundedCornerShape(24.dp),
    depth: Dp = 5.dp,
    enabled: Boolean = true,
    tapSound: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    contentAlignment: Alignment = Alignment.Center,
    description: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = LocalMotion.current
    val sink by animateDpAsState(if (pressed && enabled) depth else 0.dp, tween(if (motion) 70 else 0), label = "press")
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.95f else 1f,
        animationSpec = if (motion) spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium) else snap(),
        label = "press-scale",
    )
    val feedback = LocalFeedback.current
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .graphicsLayer {
                scaleX = scale * (if (pressed) 1.02f else 1f)
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    if (tapSound) feedback.tap()
                    onClick()
                },
            )
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
        propagateMinConstraints = true,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(top = depth)
                .gloss(tone.edge, shape, top = tone.edge, rim = 0.6f),
        )
        Box(
            modifier = Modifier
                .padding(bottom = depth)
                .offset { IntOffset(0, sink.roundToPx()) }
                .gloss(tone.face, shape, top = if (pressed && enabled) lerp(tone.top, Color.White, 0.15f) else tone.top)
                .padding(contentPadding),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tones.Sun,
    textColor: Color = Color.White,
    icon: (DrawScope.() -> Unit)? = null,
    enabled: Boolean = true,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        tone = tone,
        depth = 6.dp,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) IconCanvas(icon, Modifier.size(30.dp))
            GameText(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

/** A round glossy button with a drawn icon, used for everything in the play screen's corners. */
@Composable
fun RoundButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    tone: Tone = Tones.Cream,
    enabled: Boolean = true,
    tapSound: Boolean = true,
    icon: DrawScope.() -> Unit,
) {
    PressSurface(
        onClick = onClick,
        modifier = modifier.size(size),
        tone = tone,
        shape = CircleShape,
        depth = 5.dp,
        enabled = enabled,
        tapSound = tapSound,
        contentPadding = PaddingValues(0.dp),
        description = description,
    ) {
        IconCanvas(icon, Modifier.size(size * 0.62f))
    }
}
