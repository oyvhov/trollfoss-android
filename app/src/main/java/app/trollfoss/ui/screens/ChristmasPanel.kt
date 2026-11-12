package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import app.trollfoss.domain.*
import app.trollfoss.ui.SX
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawThing
import app.trollfoss.ui.components.*
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

@Composable
internal fun ChristmasButton(vm: TrollfossViewModel, size: Dp) {
    var show by remember { mutableStateOf(false) }
    if (vm.screen == Screen.Play && vm.place.christmas) {
        val label = SX.villagePlaces.str()
        PressSurface(
            onClick = { show = true },
            modifier = Modifier.widthIn(max = 120.dp).heightIn(min = maxOf(48.dp, size)),
            description = label,
            tone = Tones.Sun,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconCanvas(Icons.Map, Modifier.size(24.dp))
                GameText(label, color = T.Ink, outline = Color.Transparent, fontSize = 14.sp, maxLines = 2)
            }
        }
    } else {
        RoundButton(SX.title.str(), onClick = { show = true }, size = size, tone = Tones.Berry, icon = {
            val side = this.size.minDimension * .8f
            translate(this.size.width / 2, this.size.height * .9f) {
                drawThing(ThingType.XM_HAT, 0, 0, side, side, Pen(2.dp.toPx()))
            }
        })
    }
    if (show) ChristmasPanel(vm, onClose = { show = false })
}

@Composable
internal fun ChristmasPicture(type: ThingType, v: Int = 0, side: Dp = 48.dp) {
    CachedThumb("christmas:${type.name}:$v", side) {
        val u = size.minDimension * .82f / maxOf(type.w, type.h)
        translate(size.width / 2, (size.height + type.h * u) / 2) {
            drawThing(type, v, 0, type.w * u, type.h * u, Pen(size.minDimension * .028f))
        }
    }
}

internal enum class ChristmasTab(val title: Txt) {
    TRAVEL(SX.travel), SUPPLIES(SX.supplies), CLOTHES(SX.clothes),
    CALENDAR(SX.calendar), IDEAS(SX.ideas), SETTINGS(SX.settings),
}

@Composable
private fun ChristmasTabs(tab: ChristmasTab, select: (ChristmasTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(end = 8.dp).horizontalScroll(rememberScrollState()).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (choice in ChristmasTab.entries) {
            val active = choice == tab
            val requester = remember { BringIntoViewRequester() }
            LaunchedEffect(active) { if (active) requester.bringIntoView() }
            Box(
                Modifier.bringIntoViewRequester(requester)
                    .background(if (active) T.SunTop else T.Cream, RoundedCornerShape(12.dp))
                    .selectable(active, role = Role.Tab) { select(choice) }
                    .heightIn(min = 48.dp).padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                GameText(choice.title.str(), fontSize = 15.sp, color = T.Ink, outline = Color.Transparent, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun ChristmasPanel(
    vm: TrollfossViewModel,
    onClose: () -> Unit,
    initialTab: ChristmasTab = ChristmasTab.TRAVEL,
) {
    var tab by remember(initialTab) { mutableStateOf(initialTab) }
    var revision by remember { mutableIntStateOf(0) }
    var notice by remember { mutableStateOf<Txt?>(null) }
    var openSleigh by remember { mutableStateOf(false) }
    val place = vm.place
    val sleigh = vm.world.fixturesIn(place).firstOrNull { it.type == FixtureType.XM_SLEIGH }
    if (openSleigh && sleigh != null) {
        ChristmasSleigh(vm, sleigh, onClose)
        return
    }

    // The 3D mountain has its own props. Deliver dollhouse toys at the village tree there.
    val delivery = if (place == PlaceId.HEILEBERGET) PlaceId.CHRISTMAS_SQUARE else place
    val x = if (delivery == place) {
        vm.engine?.takeIf { it.place == place }?.let { it.cam + it.visibleViewport / 2f } ?: 1.15f
    } else {
        vm.world.fixturesIn(delivery).firstOrNull { it.type == FixtureType.XM_TREE }?.x ?: 1.15f
    }
    fun refresh() { revision++; vm.changed() }
    fun showDelivery() {
        onClose()
        if (delivery != place || vm.screen != Screen.Play) vm.travelPlayCard(delivery, x)
    }
    val opened = remember(vm.world.christmas, revision) { vm.world.christmas.opened.toSet() }

    TrollDialog(onClose, maxWidth = 740.dp, scrollContent = false) {
        ChristmasTabs(tab) { tab = it; notice = null }
        notice?.let { GameText(it.str(), fontSize = 14.sp, color = T.Ink, outline = Color.Transparent) }
        val body = Modifier.fillMaxWidth().weight(1f, fill = false)
        key(tab) {
            when (tab) {
                ChristmasTab.TRAVEL -> Column(
                    body.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ChristmasTravelChoices(place, vm.christmasReturnPlace, onVisit = { destination ->
                        onClose()
                        if (destination == place) {
                            if (vm.screen != Screen.Play) vm.open(Screen.Play)
                        } else {
                            vm.travel(destination)
                        }
                    }, onReturn = { onClose(); vm.returnFromChristmas() })
                }
                ChristmasTab.SUPPLIES, ChristmasTab.CLOTHES -> {
                    val choices = remember(tab) {
                        if (tab == ChristmasTab.SUPPLIES) Christmas.things.map { it to 0 } + (ThingType.XM_STOCKING to 0)
                        else Christmas.clothes
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(92.dp),
                        modifier = body,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GameText((if (delivery != place) SX.collectAtSquare else SX.hint).str(), fontSize = 14.sp,
                                outline = Color.Transparent, color = T.Ink)
                        }
                        items(choices.size) { index ->
                            val (type, variant) = choices[index]
                            Column(
                                Modifier.background(T.Cream, RoundedCornerShape(12.dp)).clickable(role = Role.Button) {
                                    if (vm.sim.christmas.supply(type, variant, delivery, x) == null) notice = SX.full
                                    else { refresh(); showDelivery() }
                                }.padding(7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                ChristmasPicture(type, variant)
                                GameText(SX.thing(type).str(), fontSize = 13.sp, color = T.Ink,
                                    outline = Color.Transparent, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
                ChristmasTab.CALENDAR -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(75.dp),
                    modifier = body,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (delivery != place) item(span = { GridItemSpan(maxLineSpan) }) {
                        GameText(SX.collectAtSquare.str(), fontSize = 14.sp, color = T.Ink, outline = Color.Transparent)
                    }
                    items(24) { index ->
                        val done = index in opened
                        val label = SX.door(index + 1).str()
                        val openedLabel = SX.opened.str()
                        Column(
                            Modifier.background(if (done) Color(0xFFDDE9DC) else Color(0xFFFFE9CF), RoundedCornerShape(12.dp))
                                .clickable(enabled = !done, role = Role.Button) {
                                    if (vm.sim.christmas.openDoor(index, delivery, x)) {
                                        notice = null
                                        refresh()
                                        if (delivery != place || vm.screen != Screen.Play) showDelivery()
                                    } else notice = SX.full
                                }.semantics {
                                    contentDescription = label
                                    if (done) stateDescription = openedLabel
                                }.heightIn(min = 48.dp).padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (done) IconCanvas(Icons.Check, Modifier.size(32.dp))
                            else ChristmasPicture(ThingType.XM_PARCEL, side = 32.dp)
                            GameText((index + 1).toString(), fontSize = 14.sp, color = T.Ink, outline = Color.Transparent)
                        }
                    }
                }
                ChristmasTab.IDEAS -> Column(
                    body.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (sleigh != null) BigButton(SX.fly.str(), onClick = { openSleigh = true }, tone = Tones.Mint, icon = Icons.Map)
                    ChristmasStoryCards(vm) { refresh(); onClose() }
                    ChristmasIdeas(vm, onClose)
                    val party = vm.world.community.partyPlace?.takeIf { "christmas:party" in vm.world.flags }
                    BigButton((if (party == null) SX.party else SX.endParty).str(), onClick = {
                        vm.sim.christmas.party(party ?: delivery, x)
                        refresh()
                        if (party == null) showDelivery() else onClose()
                    }, tone = Tones.Berry, icon = Icons.Friends)
                }
                ChristmasTab.SETTINGS -> Column(
                    body.verticalScroll(rememberScrollState()).selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for ((choice, title) in listOf(FestivalChoice.AUTO to SX.auto, FestivalChoice.CHRISTMAS to SX.allYear, FestivalChoice.NONE to SX.off)) {
                        val selected = vm.settings.festival == choice
                        Row(
                            Modifier.fillMaxWidth().background(if (selected) T.SunTop else T.Cream, RoundedCornerShape(12.dp))
                                .selectable(selected, role = Role.RadioButton) { vm.setFestival(choice); refresh() }
                                .heightIn(min = 48.dp).padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GameText(title.str(), fontSize = 16.sp, color = T.Ink, outline = Color.Transparent)
                            if (selected) IconCanvas(Icons.Check, Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}
