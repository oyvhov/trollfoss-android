package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.ui.SX
import app.trollfoss.ui.art.XmasCream
import app.trollfoss.ui.art.XmasGold
import app.trollfoss.ui.art.XmasGreen
import app.trollfoss.ui.art.drawChristmasVillageLandmark
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.PressSurface
import app.trollfoss.ui.components.Tone
import app.trollfoss.ui.str

private val villageSignTone = Tone(Color(0xFF468767), XmasGreen, Color(0xFF204C3D))

@Composable
internal fun ChristmasMapSign(compact: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val label = SX.visit.str()
    PressSurface(
        onClick = onClick,
        modifier = Modifier.widthIn(max = if (compact) 218.dp else 254.dp)
            .heightIn(min = if (compact) 48.dp else 56.dp),
        tone = villageSignTone,
        shape = RoundedCornerShape(18.dp),
        depth = 3.dp,
        enabled = enabled,
        description = label,
        contentPadding = PaddingValues(start = 5.dp, end = 9.dp, top = 3.dp, bottom = 3.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            CachedThumb(
                key = "map-christmas-village",
                side = if (compact) 36.dp else 44.dp,
                modifier = Modifier.background(XmasCream, CircleShape).border(1.5.dp, XmasGold, CircleShape),
            ) {
                drawChristmasVillageLandmark(Offset(size.width * 0.45f, size.height * 0.8f), size.width * 0.63f, size.width * 0.026f)
            }
            GameText(
                label,
                modifier = Modifier.weight(1f, fill = false),
                color = XmasCream,
                outline = Color.Transparent,
                fontSize = if (compact) 17.sp else 20.sp,
                maxLines = 2,
            )
            IconCanvas({ rotate(180f) { Icons.Back(this) } }, Modifier.size(18.dp))
        }
    }
}
