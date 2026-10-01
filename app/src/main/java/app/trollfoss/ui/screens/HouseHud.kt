package app.trollfoss.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.trollfoss.ui.theme.T

/**
 * The five golden keys of Storhuset, shown in a little pill at the top of every floor of the house:
 * the keys found so far are gold, the ones still hiding are grey. Four and a half is not a thing.
 */
@Composable
fun HouseKeysHud(found: Int, total: Int = 5, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(T.Cream, RoundedCornerShape(26.dp))
            .border(3.dp, T.Ink, RoundedCornerShape(26.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            if (i > 0) Spacer(Modifier.width(6.dp))
            Key(i < found)
        }
    }
}

@Composable
private fun Key(lit: Boolean) {
    val body = if (lit) Color(0xFFFFC83D) else Color(0xFFC9C4D6)
    val edge = if (lit) Color(0xFF9A6B00) else Color(0xFF8E88A3)
    Canvas(Modifier.size(width = 26.dp, height = 26.dp)) {
        val s = size.minDimension
        val w = s * 0.09f
        // The bow, the shaft and two teeth.
        drawCircle(body, s * 0.22f, Offset(s * 0.3f, s * 0.5f))
        drawCircle(edge, s * 0.22f, Offset(s * 0.3f, s * 0.5f), style = Stroke(w))
        drawCircle(Color(0xFF2B2140).copy(alpha = if (lit) 0.35f else 0.2f), s * 0.08f, Offset(s * 0.3f, s * 0.5f))
        drawLine(edge, Offset(s * 0.5f, s * 0.5f), Offset(s * 0.92f, s * 0.5f), strokeWidth = w * 2.2f, cap = StrokeCap.Round)
        drawLine(body, Offset(s * 0.5f, s * 0.5f), Offset(s * 0.92f, s * 0.5f), strokeWidth = w * 1.2f, cap = StrokeCap.Round)
        drawLine(edge, Offset(s * 0.78f, s * 0.5f), Offset(s * 0.78f, s * 0.7f), strokeWidth = w * 1.8f, cap = StrokeCap.Round)
        drawLine(edge, Offset(s * 0.9f, s * 0.5f), Offset(s * 0.9f, s * 0.66f), strokeWidth = w * 1.8f, cap = StrokeCap.Round)
        if (lit) drawCircle(Color.White.copy(alpha = 0.7f), s * 0.05f, Offset(s * 0.22f, s * 0.4f))
    }
}
