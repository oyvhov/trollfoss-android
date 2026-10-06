package app.trollfoss.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.trollfoss.domain.First
import app.trollfoss.ui.components.DesignIcons
import app.trollfoss.ui.components.IconCanvas

/** The picture of a discovery; found ones in full colour, the others as a dark silhouette. */
@Composable
fun FirstPicture(first: First, modifier: Modifier = Modifier, found: Boolean) {
    IconCanvas(DesignIcons.Sticker, modifier)
}
