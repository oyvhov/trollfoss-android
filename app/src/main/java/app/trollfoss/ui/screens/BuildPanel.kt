package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Mine
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomKind
import app.trollfoss.ui.SM
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.MineC
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.LocalFeedback
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.launch

private enum class BuildTab { ROOMS, FLOOR, LOOK, PARTY }

/**
 * The builder's panel, sliding in from the right like the home designer's. It is made of pictures: first the four
 * houses to pick from (the foundation), then the rooms (tap an empty frame in the scene, then a room card), the
 * second floor, the look of the house from outside, and the housewarming. [version] changes whenever the house does.
 */
@Composable
fun BuildPanel(vm: TrollfossViewModel, engine: Engine, place: PlaceId, compact: Boolean, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val version = vm.mineVersion
    val h = vm.world.mine
    val sim = vm.sim
    val ui = vm.mineUi
    val shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    val tile = if (compact) 88.dp else 104.dp
    val width = if (compact) 224.dp else 262.dp
    val scope = rememberCoroutineScope()

    fun focusX(x: Float) {
        scope.launch {
            val from = engine.cam + engine.viewport / 2f
            val to = x
            Animatable(from).animateTo(to, tween(420)) { engine.focusOn(value) }
        }
    }

    Column(
        modifier
            .fillMaxHeight()
            .width(width)
            .clip(shape)
            .background(T.Cream.copy(alpha = 0.97f))
            .border(3.dp, T.Ink, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (version < 0) Unit
        val tabs = buildList {
            add(BuildTab.ROOMS); add(BuildTab.FLOOR); add(BuildTab.LOOK)
            if (Mine.canParty(h)) add(BuildTab.PARTY)
        }
        if (!h.started) {
            // The foundation: four houses to choose from.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconCanvas(BuildIcons.Hammer, Modifier.size(34.dp))
                IconCanvas(BuildIcons.HouseHappy, Modifier.size(34.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0 until 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0 until 2) {
                            val i = row * 2 + col
                            Tile(size = tile, desc = SM.template(i).str(), onClick = {
                                if (sim.mine.layFoundation(i)) { focusX(Mine.FACADE_X0 + 0.7f); vm.changed() }
                            }) {
                                CachedThumb("mine:tpl:$i", tile - 8.dp) { drawTemplateThumb(i) }
                            }
                        }
                    }
                }
            }
        } else {
            val tab = tabs.getOrElse(ui.tab) { BuildTab.ROOMS }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                for (t in tabs) {
                    val on = t == tab
                    RoundButton(
                        description = when (t) { BuildTab.ROOMS -> SM.rooms.str(); BuildTab.FLOOR -> SM.floor.str(); BuildTab.LOOK -> SM.look.str(); BuildTab.PARTY -> SM.party.str() },
                        onClick = { ui.tab = tabs.indexOf(t) },
                        size = if (on) 46.dp else 40.dp,
                        tone = if (on) Tones.Sun else Tones.Cream,
                        icon = when (t) { BuildTab.ROOMS -> BuildIcons.RoomPlus; BuildTab.FLOOR -> BuildIcons.Crane; BuildTab.LOOK -> BuildIcons.Brush; BuildTab.PARTY -> BuildIcons.Party },
                    )
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when (tab) {
                    BuildTab.ROOMS -> RoomsTab(vm, engine, place, tile) { slot -> focusX(slot * Mine.SLOT_W + 1.1f) }
                    BuildTab.FLOOR -> FloorTab(vm, place, tile)
                    BuildTab.LOOK -> LookTab(vm, tile, compact)
                    BuildTab.PARTY -> PartyTab(vm, engine)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            RoundButton(SM.done.str(), onClick = onClose, size = if (compact) 48.dp else 56.dp, tone = Tones.Mint, icon = Icons.Check)
        }
    }
}

/** A square card in the panel. */
@Composable
private fun Tile(size: Dp, desc: String = "", chosen: Boolean = false, enabled: Boolean = true, onClick: () -> Unit, content: @Composable () -> Unit) {
    val feedback = LocalFeedback.current
    Box(
        Modifier
            .size(size)
            .semantics { if (desc.isNotEmpty()) contentDescription = desc }
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(18.dp))
            .background(if (chosen) T.SunTop else Color.White)
            .border(if (chosen) 4.dp else 2.5.dp, if (chosen) T.Sun else T.Ink, RoundedCornerShape(18.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled) {
                feedback.sfx(Sfx.TAP, 0.5f, 1f)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}

// ------------------------------------------------------------------------------------------------ rooms

@Composable
private fun RoomsTab(vm: TrollfossViewModel, engine: Engine, place: PlaceId, tile: Dp, focusSlot: (Int) -> Unit) {
    val h = vm.world.mine
    val sim = vm.sim
    val feedback = LocalFeedback.current
    if (place == PlaceId.MINE_YARD) {
        // Rooms are built from the inside: a door takes you there.
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            RoundButton(SM.goInside.str(), onClick = { vm.travel(PlaceId.MINE_GROUND) }, size = 96.dp, tone = Tones.Sea, icon = BuildIcons.DoorIn)
            CachedThumb("mine:house:${h.hash()}", 110.dp) { drawHouseThumb(h) }
        }
        return
    }
    val selected = if (h.selectedPlace == place) h.selected else -1
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // The five slots of the floor: tap one to look at it.
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            for (i in 0 until Mine.SLOTS) {
                val standing = h.standing(place, i)
                val can = Mine.canBuild(h, place, i)
                val chosen = i == selected
                val chip = if (tile < 100.dp) 32.dp else 38.dp
                Box(
                    Modifier
                        .size(chip)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (chosen) T.SunTop else if (standing) Color.White else T.CreamDeep)
                        .border(if (chosen) 3.dp else 2.dp, if (chosen) T.Sun else T.Ink, RoundedCornerShape(8.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            feedback.sfx(Sfx.TAP, 0.5f, 1f)
                            if (i != 0 && (can || h.kind(place, i) != null)) sim.mine.select(place, i)
                            focusSlot(i)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val kind = if (i == 0) null else h.kind(place, i)
                    when {
                        i == 0 -> IconCanvas(Icons.House, Modifier.size(chip - 8.dp))
                        kind != null -> CachedThumb("mine:kind:${kind.name}", chip - 6.dp) { drawKindThumb(kind) }
                        can -> IconCanvas(BuildIcons.RoomPlus, Modifier.size(chip - 8.dp))
                        else -> IconCanvas(Icons.Lock, Modifier.size(chip - 12.dp).alpha(0.5f))
                    }
                }
            }
        }
        val kind = if (selected >= 0) h.kind(place, selected) else null
        when {
            kind != null -> {
                // A built room: its picture and the way to tear it down.
                CachedThumb("mine:kind:${kind.name}", 130.dp) { drawKindThumb(kind) }
                RoundButton(
                    SM.tearDown.str(), onClick = { sim.mine.askDemolish(place, selected) },
                    size = 64.dp, tone = Tones.Berry, enabled = Mine.canDemolish(h, place, selected), icon = BuildIcons.Smash,
                )
            }
            selected >= 0 && Mine.canBuild(h, place, selected) -> {
                for (row in RoomKind.entries.chunked(2)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (k in row) {
                            Tile(size = tile, desc = SM.kind(k).str(), enabled = !vm.sim.mine.busy, onClick = {
                                if (sim.mine.buildRoom(place, selected, k)) { focusSlot(selected); vm.changed() }
                            }) {
                                CachedThumb("mine:kind:${k.name}", tile - 8.dp) { drawKindThumb(k) }
                            }
                        }
                    }
                }
            }
            else -> IconCanvas(BuildIcons.HouseHappy, Modifier.size(96.dp))
        }
    }
}

// ------------------------------------------------------------------------------------------------ floor

@Composable
private fun FloorTab(vm: TrollfossViewModel, place: PlaceId, tile: Dp) {
    val h = vm.world.mine
    val can = Mine.canBuildUpper(h)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
        CachedThumb("mine:floors:${h.upperBuilt}", tile + 40.dp) { drawFloorsThumb(h.upperBuilt) }
        when {
            h.upperBuilt -> IconCanvas(Icons.Check, Modifier.size(56.dp))
            else -> {
                RoundButton(
                    SM.buildHouse.str(), onClick = {
                        // The crane works in the yard: the child is taken there to watch.
                        if (place != PlaceId.MINE_YARD) vm.travel(PlaceId.MINE_YARD)
                        if (vm.sim.mine.buildUpper()) vm.changed()
                    },
                    size = 92.dp, tone = if (can) Tones.Mint else Tones.Cream, enabled = can && !vm.sim.mine.busy, icon = BuildIcons.Crane,
                )
                // Two rooms are needed first: two little houses that fill up.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0 until Mine.ROOMS_FOR_FLOOR) {
                        val have = h.roomCount() > i
                        IconCanvas(BuildIcons.RoomPlus, Modifier.size(34.dp).alpha(if (have) 1f else 0.35f))
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------- look

@Composable
private fun LookTab(vm: TrollfossViewModel, tile: Dp, compact: Boolean) {
    val h = vm.world.mine
    val sim = vm.sim
    val small = if (compact) 34.dp else 38.dp
    val med = if (compact) 48.dp else 56.dp
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CachedThumb("mine:house:${h.hash()}", tile + 20.dp) { drawHouseThumb(h) }
        // Wall colours.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in 0 until 6) {
                            val i = row * 6 + c
                            Swatch(small, h.wall == i, MineC.wall(i)) { sim.mine.restyle(wall = i); vm.changed() }
                        }
                    }
                }
            }
        }
        // Roof types, then roof colours.
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until Mine.ROOFS) {
                Tile(size = med, chosen = h.roof == i, onClick = { sim.mine.restyle(roof = i); vm.changed() }) {
                    CachedThumb("mine:roof:$i:${h.roofColor}:${h.wall}", med - 8.dp) { drawRoofThumb(i, h.roofColor, h.wall) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 4) {
                        val i = row * 4 + c
                        Swatch(small, h.roofColor == i, MineC.roof(i)) { sim.mine.restyle(roofColor = i); vm.changed() }
                    }
                }
            }
        }
        // Doors and windows.
        for (row in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0 until 3) {
                    val i = row * 3 + c
                    Tile(size = med, chosen = h.door == i, onClick = { sim.mine.restyle(door = i); vm.changed() }) {
                        CachedThumb("mine:door:$i:${h.wall}", med - 8.dp) { drawDoorThumb(i, h.wall) }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until Mine.WINDOWS) {
                Tile(size = med, chosen = h.windows == i, onClick = { sim.mine.restyle(windows = i); vm.changed() }) {
                    CachedThumb("mine:window:$i:${h.wall}:${h.roofColor}", med - 8.dp) { drawWindowThumb(i, h.wall, h.roofColor) }
                }
            }
        }
        // The chimney and the flag: on or off.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tile(size = med, chosen = h.chimney, onClick = { sim.mine.restyle(chimney = !h.chimney); vm.changed() }) {
                CachedThumb("mine:chimney:${h.chimney}", med - 8.dp) { drawChimneyThumb(h.chimney) }
            }
            Tile(size = med, chosen = h.flag, onClick = { sim.mine.restyle(flag = !h.flag); vm.changed() }) {
                CachedThumb("mine:flag:${h.flag}", med - 8.dp) { drawFlagThumb(h.flag) }
            }
        }
    }
}

@Composable
private fun Swatch(size: Dp, chosen: Boolean, color: Color, onClick: () -> Unit) {
    val feedback = LocalFeedback.current
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(if (chosen) 4.dp else 2.dp, if (chosen) T.Ink else T.Ink.copy(alpha = 0.6f), CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                feedback.sfx(Sfx.TAP, 0.5f, 1f)
                onClick()
            },
    ) {
        if (chosen) Box(Modifier.align(Alignment.Center).size(size / 3).clip(CircleShape).background(Color.White).border(2.dp, T.Ink, CircleShape))
    }
}

// ------------------------------------------------------------------------------------------------ party

@Composable
private fun PartyTab(vm: TrollfossViewModel, engine: Engine) {
    val h = vm.world.mine
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        IconCanvas(BuildIcons.Party, Modifier.size(110.dp))
        RoundButton(
            SM.party.str(), onClick = { if (vm.sim.mine.housewarming(engine.cam + engine.viewport / 2f)) vm.changed() },
            size = 92.dp, tone = Tones.Berry, enabled = h.party == null && !vm.sim.mine.busy, icon = BuildIcons.Party,
        )
    }
}

/** A speech bubble with a hammer and a house that points at the build button (down on a tablet, up on a phone). */
@Composable
fun BuildHint(pointDown: Boolean, modifier: Modifier = Modifier) {
    val bounce = androidx.compose.animation.core.rememberInfiniteTransition(label = "hint")
    val dy by bounce.animateFloat(
        initialValue = 0f, targetValue = 8f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(520), androidx.compose.animation.core.RepeatMode.Reverse), label = "hint-bounce",
    )
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.offset(y = (if (pointDown) dy else -dy).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!pointDown) Arrow(up = true)
        Row(
            Modifier.clip(shape).background(Color.White).border(3.dp, T.Ink, shape).padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCanvas(BuildIcons.Hammer, Modifier.size(34.dp))
            IconCanvas(BuildIcons.HouseHappy, Modifier.size(34.dp))
        }
        if (pointDown) Arrow(up = false)
    }
}

@Composable
private fun Arrow(up: Boolean) {
    androidx.compose.foundation.Canvas(Modifier.size(width = 26.dp, height = 16.dp)) {
        val p = androidx.compose.ui.graphics.Path().apply {
            if (up) { moveTo(size.width / 2f, 0f); lineTo(size.width, size.height); lineTo(0f, size.height) } else { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height) }
            close()
        }
        drawPath(p, Color.White)
        drawPath(p, T.Ink, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

/** «Riv rommet?»: the room's picture, a hammer, and two big buttons. Everything goes to the store. */
@Composable
fun DemolishDialog(vm: TrollfossViewModel) {
    val h = vm.world.mine
    val place = h.askPlace
    val slot = h.askDemolish
    val kind = h.kind(place, slot) ?: run { vm.sim.mine.cancelDemolish(); return }
    app.trollfoss.ui.components.TrollDialog(onClose = { vm.sim.mine.cancelDemolish(); vm.changed() }, maxWidth = 440.dp) {
        app.trollfoss.ui.components.GameText(SM.tearDown.str(), fontSize = 26.sp, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CachedThumb("mine:kind:${kind.name}", 120.dp) { drawKindThumb(kind) }
            IconCanvas(BuildIcons.Smash, Modifier.size(70.dp))
            IconCanvas(app.trollfoss.ui.components.DesignIcons.Box, Modifier.size(70.dp))
        }
        app.trollfoss.ui.components.GameText(SM.tearDownBody.str(), fontSize = 16.sp, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            app.trollfoss.ui.components.BigButton(SM.tearDownNo.str(), onClick = { vm.sim.mine.cancelDemolish(); vm.changed() }, tone = Tones.Mint, icon = Icons.Check)
            app.trollfoss.ui.components.BigButton(SM.tearDownYes.str(), onClick = { vm.sim.mine.demolish(place, slot); vm.changed() }, tone = Tones.Berry, icon = BuildIcons.Smash)
        }
    }
}
