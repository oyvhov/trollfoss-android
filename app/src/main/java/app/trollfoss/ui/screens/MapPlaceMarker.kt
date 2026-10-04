package app.trollfoss.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawBalloon
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T

/** A sticker on the landscape. Its whole building target remains tappable above the name. */
@Composable
internal fun MapPlaceMarker(place: PlaceId, label: String, here: Boolean, compact: Boolean,
                            onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember(place) { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val motion = LocalMotion.current
    val pop by animateFloatAsState(if (pressed && motion) .94f else 1f,
        spring(dampingRatio = .5f, stiffness = 450f), label = "map sticker")
    val accent = when(place) {
        PlaceId.HOME, PlaceId.MINE_YARD, PlaceId.FARM -> T.Mint
        PlaceId.CAFE, PlaceId.SHOP, PlaceId.BEACH -> T.Sun
        PlaceId.SALON, PlaceId.STAGE, PlaceId.TIVOLI -> T.Berry
        PlaceId.LAB, PlaceId.SPACE, PlaceId.CLOUD_ISLAND -> T.Grape
        else -> T.Sea
    }
    val shape = RoundedCornerShape(50)
    Box(modifier.semantics { contentDescription = label; role = Role.Button; selected = here }
        .clickable(source, indication = null, onClick = onClick), contentAlignment = Alignment.BottomCenter) {
        Row(Modifier.graphicsLayer { scaleX = pop; scaleY = pop }
            .height(if(compact) 34.dp else 40.dp)
            .background(T.Ink.copy(alpha = .18f), shape).padding(bottom = 3.dp)
            .background(if(pressed || here) accent else T.Cream, shape)
            .border(if(here) 3.dp else 2.dp, T.Ink, shape).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(if(here) 22.dp else 10.dp)) {
                if(here) drawBalloon(Offset(size.width / 2, size.height * .42f), size.minDimension * .32f,
                    Pen(size.minDimension * .055f))
                else {
                    drawCircle(T.Ink, size.minDimension / 2)
                    drawCircle(accent, size.minDimension * .32f)
                }
            }
            GameText(label, style = MaterialTheme.typography.titleMedium, color = T.Ink,
                fontSize = if(compact) 13.sp else 15.sp, maxLines = 1)
        }
    }
}
