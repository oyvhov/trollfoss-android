package app.trollfoss.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.CatalogueItem
import app.trollfoss.domain.Decor
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.World
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawFixtureBack
import app.trollfoss.ui.art.drawFixtureFront
import app.trollfoss.ui.art.drawFloorSwatch
import app.trollfoss.ui.art.drawWallSwatch
import app.trollfoss.ui.components.DesignIcons
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.LocalFeedback
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.theme.T
import kotlin.math.max
import kotlin.math.min

private enum class DesignTab { FURNITURE, WALL, FLOOR, STORE, TIDY }

/**
 * The home designer's panel along the bottom of the screen: furniture from the catalogue, wallpaper and
 * floors for the room in the middle of the screen, the store (drag furniture onto the panel to put it
 * away) and the broom that tidies the whole place. Everything is pictures; nothing needs reading.
 */
@Composable
fun DesignerPanel(engine: Engine, world: World, place: PlaceId, onClose: () -> Unit, modifier: Modifier = Modifier) {
    var tab by remember(place) { mutableIntStateOf(DesignTab.FURNITURE.ordinal) }
    val feedback = LocalFeedback.current
    val version = engine.designVersion
    val glow by animateFloatAsState(if (engine.overStore) 1f else 0f, label = "store glow")
    val tabs = DesignTab.entries.filter { (it != DesignTab.WALL && it != DesignTab.FLOOR) || Decor.decoratable(place) }
    val shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)

    // A slim panel on the right, so the floor with the furniture stays in view.
    Column(
        modifier
            .fillMaxHeight()
            .width(248.dp)
            .onGloballyPositioned { engine.storeZone = it.boundsInRoot() }
            .clip(shape)
            .background(if (glow > 0.01f) androidx.compose.ui.graphics.lerp(T.Cream, T.SunTop, glow) else T.Cream.copy(alpha = 0.97f))
            .border(3.dp, T.Ink, shape)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            for (t in tabs) {
                val on = t.ordinal == tab
                RoundButton(
                    description = t.name,
                    onClick = { tab = t.ordinal },
                    size = if (on) 46.dp else 40.dp,
                    tone = if (on) Tones.Sun else Tones.Cream,
                    icon = when (t) {
                        DesignTab.FURNITURE -> DesignIcons.Sofa
                        DesignTab.WALL -> DesignIcons.Wallpaper
                        DesignTab.FLOOR -> DesignIcons.Floor
                        DesignTab.STORE -> DesignIcons.Box
                        DesignTab.TIDY -> DesignIcons.Broom
                    },
                )
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            // Read the version so the panel redraws after every change.
            if (version < 0) Unit
            when (DesignTab.entries[tab]) {
                DesignTab.FURNITURE -> {
                    val items = remember(place) { Decor.catalogue(place) }
                    LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(items) { _, item ->
                            val locked = item.stickers > world.stickers.size
                            Tile(onClick = {
                                if (locked) feedback.sfx(Sfx.HMM, 0.6f, 0.8f) else engine.addFurniture(item.type, item.variant)
                            }) {
                                FurnitureThumb(item.type, item.variant, place, locked)
                                if (locked) LockBadge(item)
                            }
                        }
                    }
                }
                DesignTab.WALL, DesignTab.FLOOR -> {
                    val wall = DesignTab.entries[tab] == DesignTab.WALL
                    val current = Decor.style(world, place, engine.room)
                    val n = if (wall) Decor.WALLS else Decor.FLOORS
                    LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(List(n) { it }) { _, i ->
                            val chosen = if (wall) current.wall == i else current.floor == i
                            Tile(chosen = chosen, onClick = { if (wall) engine.restyle(wall = i) else engine.restyle(floor = i) }) {
                                Canvas(Modifier.size(80.dp)) {
                                    val r = Rect(4.dp.toPx(), 4.dp.toPx(), size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
                                    val pen = Pen(2.dp.toPx())
                                    if (wall) drawWallSwatch(i, r, pen) else drawFloorSwatch(i, r, pen)
                                }
                            }
                        }
                    }
                }
                DesignTab.STORE -> {
                    if (world.storage.isEmpty()) {
                        // An empty store shows how to fill it: furniture flying into the box.
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconCanvas(DesignIcons.Sofa, Modifier.size(56.dp).graphicsLayer { rotationZ = -12f; translationX = 30f })
                            IconCanvas(DesignIcons.Box, Modifier.size(96.dp))
                        }
                    } else {
                        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(world.storage.toList()) { index, stored ->
                                Tile(onClick = { engine.addFromStore(index) }) { FurnitureThumb(stored.type, stored.variant, place, false) }
                            }
                        }
                    }
                }
                DesignTab.TIDY -> {
                    RoundButton("Rydd", onClick = { engine.tidy() }, modifier = Modifier.align(Alignment.Center), size = 120.dp, tone = Tones.Sun, icon = DesignIcons.Broom)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            // How many stickers the child has: they open the special furniture.
            Row(
                Modifier.background(T.Grape, RoundedCornerShape(50)).border(2.dp, T.Ink, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconCanvas(DesignIcons.Sticker, Modifier.size(28.dp))
                GameText("${world.stickers.size}", fontSize = 20.sp, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            RoundButton("Ferdig", onClick = onClose, size = 56.dp, tone = Tones.Mint, icon = Icons.Check)
        }
    }
}
/** A square card in the panel. */
@Composable
private fun Tile(chosen: Boolean = false, onClick: () -> Unit, content: @Composable () -> Unit) {
    val feedback = LocalFeedback.current
    Box(
        Modifier
            .size(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (chosen) T.SunTop else Color.White)
            .border(if (chosen) 4.dp else 2.5.dp, if (chosen) T.Sun else T.Ink, RoundedCornerShape(18.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                feedback.sfx(Sfx.TAP, 0.5f, 1f)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun LockBadge(item: CatalogueItem) {
    Box(Modifier.size(104.dp)) {
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).background(T.Grape, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconCanvas(DesignIcons.Sticker, Modifier.size(18.dp))
            GameText("${item.stickers}", fontSize = 15.sp, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        IconCanvas(Icons.Lock, Modifier.align(Alignment.TopEnd).padding(6.dp).size(26.dp))
    }
}

/** A small drawing of a piece of furniture, scaled to fit its card. */
@Composable
private fun FurnitureThumb(type: FixtureType, variant: Int, place: PlaceId, locked: Boolean) {
    Canvas(Modifier.size(88.dp).graphicsLayer { alpha = if (locked) 0.45f else 1f }) {
        drawFixtureThumb(type, variant, Rect(0f, 0f, size.width, size.height))
    }
}
