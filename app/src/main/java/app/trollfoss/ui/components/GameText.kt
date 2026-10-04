package app.trollfoss.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import app.trollfoss.ui.theme.T
import app.trollfoss.ui.theme.PlayFont

/**
 * Playful lettering with a fine ink rim on light text. Dark labels stay plain; small words use
 * the reading font, while headings keep the display face.
 */
@Composable
fun GameText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.White,
    outline: Color = T.Ink,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    val size = when {
        fontSize.isSpecified -> fontSize
        style.fontSize.isSpecified -> style.fontSize
        else -> 20.sp
    }
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val rim = (sizePx * 0.075f).coerceIn(with(density) { 0.8.dp.toPx() }, with(density) { 4.5.dp.toPx() })
    val drop = rim * 0.35f
    val lineHeight = if (style.lineHeight.isSp && style.fontSize.isSp && size.isSp) (size.value * style.lineHeight.value / style.fontSize.value).sp else style.lineHeight
    val base = style.merge(
        TextStyle(
            fontFamily = if (size.value < 20f) PlayFont else style.fontFamily,
            fontSize = size,
            lineHeight = lineHeight,
            fontWeight = style.fontWeight ?: FontWeight.Black,
            textAlign = textAlign ?: style.textAlign,
        ),
    )
    val fill = if (textAlign != null && textAlign != TextAlign.Start) Modifier.fillMaxWidth() else Modifier
    Box(modifier, contentAlignment = Alignment.TopStart) {
        if (color != outline && outline.alpha > 0f) BasicText(
            text,
            style = base.copy(color = outline, drawStyle = Stroke(width = rim, join = StrokeJoin.Round), shadow = Shadow(outline, Offset(0f, drop), 0f)),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.matchParentSize(),
        )
        BasicText(
            text,
            style = base.copy(color = color),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = fill,
        )
    }
}
