package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.*
import app.trollfoss.ui.SM
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun playPanelWidth(compact: Boolean, designer: Boolean) =
    (if (compact) { if (designer) 248 else 224 } else { if (designer) 304 else 288 }).dp

private val familyRooms = listOf(RoomKind.BEDROOM, RoomKind.LIVING, RoomKind.KITCHEN, RoomKind.BATH)
private fun roomSymbol(kind: RoomKind): FixtureType = when (kind) {
    RoomKind.LIVING -> FixtureType.SOFA
    RoomKind.KITCHEN -> FixtureType.STOVE
    RoomKind.DINING -> FixtureType.MI_DINING_TABLE
    RoomKind.BEDROOM -> FixtureType.BED
    RoomKind.KIDS -> FixtureType.TOY_BOX
    RoomKind.BATH -> FixtureType.BATH
    RoomKind.LIBRARY -> FixtureType.BOOKCASE
    RoomKind.WORKSHOP -> FixtureType.WORKBENCH
    RoomKind.MUSIC -> FixtureType.PIANO
    RoomKind.GREENHOUSE -> FixtureType.MI_PLANT_BED
}
private val manorRooms = mapOf(
    PlaceId.MANOR_GROUND to listOf(null, RoomKind.LIVING, RoomKind.LIBRARY, RoomKind.DINING, RoomKind.KITCHEN, RoomKind.GREENHOUSE),
    PlaceId.MANOR_UPPER to listOf(null, RoomKind.KIDS, RoomKind.KIDS, RoomKind.BATH, RoomKind.BEDROOM, RoomKind.GREENHOUSE),
    PlaceId.MANOR_ATTIC to listOf(RoomKind.WORKSHOP, RoomKind.LIVING, RoomKind.LIBRARY, RoomKind.KIDS),
    PlaceId.MANOR_CELLAR to listOf(RoomKind.WORKSHOP, RoomKind.BATH, RoomKind.WORKSHOP, RoomKind.BATH, RoomKind.MUSIC),
)

internal fun roomKind(world: World, place: PlaceId, index: Int): RoomKind? = when {
    place == PlaceId.HOME -> familyRooms.getOrNull(index)
    place.mine -> world.mine.kind(place, index)
    else -> manorRooms[place]?.getOrNull(index)
}

internal fun roomLabel(world: World, place: PlaceId, index: Int): Txt {
    if (place.mine) return if (index == 0) txt("Gang") else world.mine.kind(place, index)?.let(SM::kind) ?: txt("Bygg her")
    val names = when (place) {
        PlaceId.MANOR_GROUND -> listOf(txt("Hall"), txt("Stove", "Stue"), txt("Bibliotek"), txt("Spisestove", "Spisestue"), txt("Kjøken", "Kjøkken"), txt("Vinterhage"))
        PlaceId.MANOR_UPPER -> listOf(txt("Gang"), txt("Barnerom"), txt("Leikerom", "Lekerom"), txt("Bad"), txt("Soverom"), txt("Altan"))
        PlaceId.MANOR_ATTIC -> listOf(txt("Lager"), txt("Sture"), txt("Tårnet"), txt("Løyndom", "Hemmelighet"))
        PlaceId.MANOR_CELLAR -> listOf(txt("Verkstad", "Verksted"), txt("Vaskerom"), txt("Fyrrom"), txt("Basseng"), txt("Festrom"))
        else -> emptyList()
    }
    return names.getOrNull(index) ?: roomKind(world, place, index)?.let(SM::kind) ?: SM.rooms
}

/** A little floor plan: the child sees the neighbouring rooms and can glide straight to one. */
@Composable
internal fun RoomNavigator(vm: TrollfossViewModel, engine: Engine, modifier: Modifier = Modifier) {
    val place = vm.place
    if (place.outdoor || place !in manorRooms.keys && place != PlaceId.HOME && !place.mine) return
    val rooms = remember(place) { Decor.rooms(place) }
    val house = remember(vm.mineVersion) { vm.world.mine }
    val current = engine.visibleRoom
    val scope = rememberCoroutineScope()
    var journey by remember { mutableStateOf<Job?>(null) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(Modifier.widthIn(max = 620.dp).clip(RoundedCornerShape(22.dp))
        .background(T.Cream.copy(alpha = 0.94f)).border(2.dp, T.CreamLine, RoundedCornerShape(22.dp))
        .horizontalScroll(rememberScrollState()).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        rooms.forEachIndexed { index, range ->
            val label = roomLabel(vm.world, place, index).str()
            val kind = roomKind(vm.world, place, index)
            val built = !place.mine || house.standing(place, index)
            val on = index == current
            Column(Modifier.width(76.dp).clip(RoundedCornerShape(16.dp))
                .background(if (on) T.SunTop else Color.Transparent)
                .border(if (on) 2.dp else 0.dp, if (on) T.Sun else Color.Transparent, RoundedCornerShape(16.dp))
                .clickable {
                    journey?.cancel()
                    if (place.mine && !built) {
                        vm.sim.mine.select(place,index)
                        vm.mineUi.open = true
                        vm.changed()
                    }
                    journey = scope.launch {
                        Animatable(engine.cam + engine.visibleViewport / 2f).animateTo((range.start + range.endInclusive) / 2f, tween(420)) { engine.focusOn(value) }
                        engine.selectRoom(index)
                    }
                }.semantics { contentDescription = label; selected = on }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                if (kind != null && built) CachedThumb("room-symbol:${kind.name}", 44.dp) {
                    drawFixtureThumb(roomSymbol(kind), 0, Rect(0f, 0f, size.width, size.height))
                }
                else IconCanvas(if (built) Icons.House else BuildIcons.RoomPlus, Modifier.size(44.dp))
                GameText(label, fontSize = 12.sp, color = T.Ink, maxLines = 1)
            }
        }
    }
    if (place.mine) RoomPlayTrail(house.kind(place, current))
    }
}

/** Picture recipes provide a starting point; there is no required order or task to complete. */
@Composable
private fun RoomPlayTrail(kind: RoomKind?) {
    val recipe = when (kind) {
        RoomKind.KITCHEN -> Recipe("idea-dough", FixtureType.MI_COUNTER, listOf(ThingType.EGG, ThingType.MILK), Made(ThingType.DOUGH))
        RoomKind.GREENHOUSE -> Recipe("idea-berries", FixtureType.MI_PLANT_BED, listOf(ThingType.SEEDS, ThingType.WATERING_CAN), Made(ThingType.STRAWBERRY))
        RoomKind.WORKSHOP -> Recipe("idea-saw", FixtureType.MI_SAW_BENCH, listOf(ThingType.PLANK), Made(ThingType.STICK))
        else -> return
    }
    Row(Modifier.clip(RoundedCornerShape(18.dp)).background(T.Cream.copy(alpha = 0.94f))
        .padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (type in recipe.inputs) CachedThumb("idea:$type", 34.dp) { drawThingThumb(type, 0, Rect(0f, 0f, size.width, size.height)) }
        CachedThumb("idea:${recipe.machine}", 44.dp) { drawFixtureThumb(recipe.machine, 0, Rect(0f, 0f, size.width, size.height)) }
        GameText("→", color = T.Ink, fontSize = 20.sp)
        CachedThumb("idea:${recipe.result.type}", 34.dp) { drawThingThumb(recipe.result.type, 0, Rect(0f, 0f, size.width, size.height)) }
    }
}
