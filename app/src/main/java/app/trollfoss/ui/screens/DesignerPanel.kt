package app.trollfoss.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
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
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.components.DesignIcons
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.LocalFeedback
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.theme.T
import app.trollfoss.ui.SM
import app.trollfoss.ui.SP
import app.trollfoss.ui.S
import app.trollfoss.ui.str
import kotlin.math.max
import kotlin.math.min

private enum class DesignTab { FURNITURE, WALL, FLOOR, STORE, TIDY, TRASH }

/**
 * The home designer's panel along the bottom of the screen: furniture from the catalogue, wallpaper and
 * floors for the room in the middle of the screen, the store (drag furniture onto the panel to put it
 * away) and the broom that tidies the whole place. Everything is pictures; nothing needs reading.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignerPanel(engine: Engine, world: World, place: PlaceId, onClose: () -> Unit, modifier: Modifier = Modifier) {
    var tab by remember(place) { mutableIntStateOf(DesignTab.FURNITURE.ordinal) }
    val feedback = LocalFeedback.current
    val version = engine.designVersion
    val compact = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 520
    val currentRoom = engine.visibleRoom
    val glow by animateFloatAsState(if (engine.overStore) 1f else 0f, label = "store glow")
    val tabs = DesignTab.entries.filter { (it != DesignTab.WALL && it != DesignTab.FLOOR) || Decor.decoratable(place) }
    // The store as a list of its own for every change: the grid below only redraws when it is handed a new list.
    val stored = remember(version, world.storage.size) { world.storage.toList() }
    val discarded = remember(version, world.discardedStorage.size) { world.discardedStorage.toList() }
    val thumbSide = if (compact) 88.dp else 112.dp
    // Furniture dropped on the panel flies into the box: open the box, so the child sees where it went.
    var storedBefore by remember(place) { mutableIntStateOf(stored.size) }
    var storageNotice by remember(place) { mutableStateOf<app.trollfoss.domain.Stored?>(null) }
    LaunchedEffect(stored.size) {
        if (stored.size > storedBefore) { tab = DesignTab.STORE.ordinal; storageNotice=stored.lastOrNull() }
        storedBefore = stored.size
    }
    val shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)

    // A slim panel on the right, so the floor with the furniture stays in view.
    Column(
        modifier
            .fillMaxHeight()
            .width(playPanelWidth(compact, designer = true))
            .onGloballyPositioned { engine.storeZone = it.boundsInRoot() }
            .clip(shape)
            .background(if (glow > 0.01f) androidx.compose.ui.graphics.lerp(T.Cream, T.SunTop, glow) else T.Cream.copy(alpha = 0.97f))
            .border(3.dp, T.Ink, shape)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            // The heading names the open tab, so the pictures below get the room a second heading would take.
            GameText(when (DesignTab.entries[tab]) { DesignTab.FURNITURE -> SM.furnish; DesignTab.WALL -> SM.wallpaper; DesignTab.FLOOR -> SM.flooring; DesignTab.STORE -> SM.storage; DesignTab.TIDY -> SM.tidy; DesignTab.TRASH -> SM.trash }.str(), fontSize = 20.sp, color = T.Ink)
            CloseButton(onClose, size = if (compact) 40.dp else 48.dp)
        }
        FlowRow(maxItemsInEachRow = 3, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (t in tabs) {
                val on = t.ordinal == tab
                Box {
                RoundButton(
                    description = when (t) { DesignTab.FURNITURE -> SM.furnish; DesignTab.WALL -> SM.wallpaper; DesignTab.FLOOR -> SM.flooring; DesignTab.STORE -> SM.storage; DesignTab.TIDY -> SM.tidy; DesignTab.TRASH -> SM.trash }.str(),
                    onClick = { tab = t.ordinal },
                    size = 44.dp,
                    tone = if (on) Tones.Sun else Tones.Cream,
                    icon = when (t) {
                        DesignTab.FURNITURE -> DesignIcons.Sofa
                        DesignTab.WALL -> DesignIcons.Wallpaper
                        DesignTab.FLOOR -> DesignIcons.Floor
                        DesignTab.STORE -> DesignIcons.Box
                        DesignTab.TIDY -> DesignIcons.Broom
                        DesignTab.TRASH -> DesignIcons.Bin
                    },
                )
                // How many pieces wait in the box, ready to come along to another house.
                val count=if(t==DesignTab.STORE) stored.size else if(t==DesignTab.TRASH) discarded.size else 0
                if (count>0) {
                    Box(
                        Modifier.align(Alignment.TopEnd).size(20.dp).background(T.Berry, androidx.compose.foundation.shape.CircleShape).border(2.dp, T.Ink, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { GameText("$count", fontSize = 11.sp, style = MaterialTheme.typography.titleMedium, color = Color.White) }
                }
                }
            }
        }
        if (Decor.rooms(place).size > 1) GameText(roomLabel(world, place, currentRoom).str(), fontSize = 13.sp, color = T.Ink)
        if(storageNotice!=null && stored.any { it===storageNotice }) Row(Modifier.fillMaxWidth().background(T.Mint.copy(alpha=0.2f),RoundedCornerShape(16.dp)).padding(6.dp),verticalAlignment=Alignment.CenterVertically) {
            GameText(app.trollfoss.ui.SF.stored.str(),Modifier.weight(1f),fontSize=13.sp,color=T.Ink)
            RoundButton(app.trollfoss.ui.SF.bringBack.str(),onClick={ val index=world.storage.indexOfFirst { it===storageNotice };if(index>=0) engine.addFromStore(index);storageNotice=null },size=44.dp,tone=Tones.Mint,icon=DesignIcons.Undo)
        }
        if (tab == DesignTab.FURNITURE.ordinal || tab == DesignTab.STORE.ordinal)
            GameText(S.furnitureDragHint.str(), fontSize = 12.sp, color = T.Ink)
        if (engine.placementFailed) GameText(
            (if ((place == PlaceId.MINE_GROUND || place == PlaceId.MINE_UPPER) &&
                (0 until app.trollfoss.domain.Mine.SLOTS).none { world.mine.standing(place, it) }) SM.buildBeforeFurnishing else SM.placeFull).str(),
            fontSize = 13.sp, color = T.Ink,
        )

        Box(Modifier.fillMaxWidth().weight(1f)) {
            // Read the version so the panel redraws after every change.
            if (version < 0) Unit
            when (DesignTab.entries[tab]) {
                DesignTab.FURNITURE -> {
                    val items = remember(place) { Decor.catalogue(place) }
                    LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(items) { index, item ->
                            val reward = Decor.reward(item)
                            val label = app.trollfoss.ui.FurnitureLabels.name(item.type).str()
                            val locked = !Decor.available(world, item)
                            val hangs = item.type.spec.wall
                            Tile(modifier = furnitureDrag(engine, item.type, item.variant, locked = locked).semantics { contentDescription = label }, look = TileLook.ROOM, tint = index / 2 + index % 2, feet = if (hangs) null else thumbSide * fixtureThumbFeet(item.type), onClick = {
                                if (locked && reward != null) {
                                    engine.cancel(); engine.designMode = false; engine.wantedToy = reward
                                } else if (locked) feedback.sfx(Sfx.HMM, 0.6f, 0.8f) else engine.addFurniture(item.type, item.variant)
                            }) {
                                // What hangs on the wall hangs a little higher, clear of the floor.
                                Box(Modifier.padding(bottom = if (hangs) 22.dp else 0.dp)) { FurnitureThumb(item.type, item.variant, place, locked && reward == null) }
                                if (locked) LockBadge(item)
                            }
                        }
                    }
                }
                DesignTab.WALL, DesignTab.FLOOR -> {
                    val wall = DesignTab.entries[tab] == DesignTab.WALL
                    val current = Decor.style(world, place, currentRoom)
                    val n = if (wall) Decor.WALLS else Decor.FLOORS
                    LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(List(n) { it }) { _, i ->
                            val chosen = if (wall) current.wall == i else current.floor == i
                            Tile(chosen = chosen, onClick = { if (wall) engine.restyle(wall = i) else engine.restyle(floor = i) }) {
                                CachedThumb(if (wall) "wall:$i" else "floor:$i", 80.dp) {
                                    val r = Rect(4.dp.toPx(), 4.dp.toPx(), size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
                                    val pen = Pen(2.dp.toPx())
                                    if (wall) drawWallSwatch(i, r, pen) else drawFloorSwatch(i, r, pen)
                                }
                            }
                        }
                    }
                }
                DesignTab.STORE -> {
                    if (stored.isEmpty()) {
                        // An empty store shows how to fill it: furniture flying into the box.
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconCanvas(DesignIcons.Sofa, Modifier.size(56.dp).graphicsLayer { rotationZ = -12f; translationX = 30f })
                            IconCanvas(DesignIcons.Box, Modifier.size(96.dp))
                        }
                    } else {
                        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(stored) { index, item ->
                                // In the store the furniture just stands there, on a pale pad with no frame round it.
                                Box {
                                Tile(modifier = furnitureDrag(engine, item.type, item.variant, storeIndex = index), look = TileLook.SOFT, feet = if (item.type.spec.wall) null else thumbSide * fixtureThumbFeet(item.type), onClick = { engine.addFromStore(index) }) {
                                    FurnitureThumb(item.type, item.variant, place, false)
                                }
                                RoundButton(SM.deleteStored.str(), onClick = { engine.discardFromStore(index) },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp), size = 40.dp,
                                    tone = Tones.Cream, icon = DesignIcons.Bin)
                                }
                            }
                        }
                    }
                }
                DesignTab.TIDY -> {
                    RoundButton(SM.tidy.str(), onClick = { engine.tidy() }, modifier = Modifier.align(Alignment.Center), size = 120.dp, tone = Tones.Sun, icon = DesignIcons.Broom)
                }
                DesignTab.TRASH -> {
                    if (discarded.isEmpty()) GameText(SM.emptyTrash.str(), fontSize = 16.sp, color = T.Ink)
                    else LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(discarded) { index, removed ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Tile(look = TileLook.SOFT, onClick = { engine.restoreStorage(index) }) {
                                    FurnitureThumb(removed.second.type, removed.second.variant, place, false)
                                }
                                RoundButton(SM.restoreStored.str(), onClick = { engine.restoreStorage(index) }, size = 40.dp, tone = Tones.Mint, icon = DesignIcons.Undo)
                            }
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            if (tab == DesignTab.STORE.ordinal && engine.canUndoStorage) {
                RoundButton(SM.undoDelete.str(), engine::undoStorage, size = 48.dp, tone = Tones.Sun, icon = DesignIcons.Undo)
            }
            // How many stickers the child has: they open the special furniture.
            Row(
                Modifier.background(T.Grape, RoundedCornerShape(50)).border(2.dp, T.Ink, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconCanvas(DesignIcons.Sticker, Modifier.size(28.dp))
                GameText("${world.stickers.size}", fontSize = 20.sp, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            RoundButton(SM.done.str(), onClick = onClose, size = 56.dp, tone = Tones.Mint, icon = Icons.Check)
        }
    }
}
/** How a card is dressed: a little showroom with a wall and a floor, a soft pad with no frame, or a plain swatch. */
private enum class TileLook { ROOM, SOFT, PLAIN }

/** Wall colours of the showroom cards, one after the other down the panel. */
private val ROOM_WALLS = listOf(Color(0xFFDDF3E6), Color(0xFFFFE4D2), Color(0xFFDCEEFC), Color(0xFFEAE2FB), Color(0xFFFFF1C4))
private val ROOM_FLOOR = Color(0xFFE9C99A)
private val ROOM_FLOOR_LINE = Color(0xFFCDA670)

/**
 * A square card in the panel. It gives a little under the finger and springs back. A [TileLook.ROOM] card is a
 * corner of a showroom (the furniture stands on its floor, [tint] picks the wall), a [TileLook.SOFT] card is just
 * the furniture on a pale pad with its shadow, no frame round it. [feet] is how far below the middle of the card the
 * furniture stands (null when it hangs on the wall): the floor of the showroom begins just above its feet, so a
 * rug lies on a wide floor and a wardrobe stands at the back of a narrow one.
 */
@Composable
private fun Tile(modifier: Modifier = Modifier, chosen: Boolean = false, look: TileLook = TileLook.PLAIN, tint: Int = 0, feet: Dp? = null, onClick: () -> Unit, content: @Composable () -> Unit) {
    val feedback = LocalFeedback.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.92f else 1f, spring(dampingRatio = 0.45f, stiffness = 600f), label = "tile press")
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .fillMaxWidth().aspectRatio(1f)
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(shape)
            .background(if (look == TileLook.SOFT) Color.White.copy(alpha = 0.6f) else if (chosen) T.SunTop else Color.White)
            .drawWithContent {
                when (look) {
                    TileLook.ROOM -> drawRoomCard(ROOM_WALLS[tint.mod(ROOM_WALLS.size)], feet?.toPx())
                    TileLook.SOFT -> feet?.toPx()?.let { drawOval(T.Ink.copy(alpha = 0.12f), Offset(size.width * 0.2f, size.height / 2f + it - size.height * 0.04f), Size(size.width * 0.6f, size.height * 0.08f)) }
                    TileLook.PLAIN -> Unit
                }
                drawContent()
            }
            .then(if (look == TileLook.SOFT) Modifier else Modifier.border(if (chosen) 4.dp else 2.5.dp, if (chosen) T.Sun else T.Ink, shape))
            .clickable(interactionSource = source, indication = null) {
                feedback.sfx(Sfx.TAP, 0.5f, 1f)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Horizontal pulls take furniture out; vertical gestures still scroll the catalogue. */
@Composable
private fun furnitureDrag(engine: Engine, type: FixtureType, variant: Int, locked: Boolean = false, storeIndex: Int? = null): Modifier {
    var origin by remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }
    val description = app.trollfoss.ui.FurnitureLabels.name(type).str()+" · ${variant+1}. "+S.furnitureDragHint.str()
    return Modifier.onGloballyPositioned { origin = it.boundsInRoot().topLeft }
        .semantics { contentDescription = description }
        .pointerInput(engine, type, variant, locked, storeIndex) {
            if (!locked) detectHorizontalDragGestures(
                onDragStart = { engine.beginCatalogueDrag(type, variant, origin + it, storeIndex) },
                onHorizontalDrag = { change, _ -> engine.moveCatalogueDrag(origin + change.position) },
                onDragEnd = engine::finishCatalogueDrag,
                onDragCancel = engine::cancelCatalogueDrag,
            )
        }
}

/** A corner of a showroom: a painted wall, a skirting board, a wooden floor and the shadow of what stands on it. */
private fun DrawScope.drawRoomCard(wall: Color, feet: Float?) {
    val w = size.width
    val h = size.height
    val standY = if (feet == null) h * 0.9f else h / 2f + feet
    val floorY = (standY - h * 0.1f).coerceIn(h * 0.4f, h * 0.8f)
    drawRect(Brush.verticalGradient(listOf(wall.lighten(0.35f), wall), endY = floorY), size = Size(w, floorY))
    drawRect(ROOM_FLOOR, Offset(0f, floorY), Size(w, h - floorY))
    // Floorboards running towards the viewer, and a white skirting board.
    for (i in 1 until 4) {
        val x = w * i / 4f
        drawLine(ROOM_FLOOR_LINE, Offset(x + (x - w / 2f) * 0.1f, floorY), Offset(x + (x - w / 2f) * 0.55f, h), strokeWidth = 1.5.dp.toPx())
    }
    drawRect(Color.White, Offset(0f, floorY - 3.dp.toPx()), Size(w, 3.dp.toPx()))
    drawLine(T.Ink.copy(alpha = 0.25f), Offset(0f, floorY), Offset(w, floorY), strokeWidth = 1.dp.toPx())
    if (feet != null) drawOval(T.Ink.copy(alpha = 0.13f), Offset(w * 0.2f, standY - h * 0.045f), Size(w * 0.6f, h * 0.09f))
}

@Composable
private fun LockBadge(item: CatalogueItem) {
    Box(Modifier.size(104.dp)) {
        if (Decor.reward(item) != app.trollfoss.domain.ToyReward.TRAIN) Row(
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
    val compact = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 520
    CachedThumb("fixture:$type:$variant", if (compact) 88.dp else 112.dp, Modifier.graphicsLayer { alpha = if (locked) 0.45f else 1f }) {
        drawFixtureThumb(type, variant, Rect(0f, 0f, size.width, size.height))
    }
}
