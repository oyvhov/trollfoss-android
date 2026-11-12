package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Christmas
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.S
import app.trollfoss.ui.SX
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.*
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

@Composable
internal fun ChristmasSleigh(vm: TrollfossViewModel, f: Fixture, onClose: () -> Unit) {
    val sleigh = vm.sim.christmas
    var revision by remember(f) { mutableIntStateOf(0) }
    val riders = remember(f, revision) { sleigh.sleighRiders(f) }
    val choices = remember(f, revision) { sleigh.boardingChoices(f) }
    TrollDialog(onClose, maxWidth = 580.dp) {
        GameText(SX.fly.str(), fontSize = 22.sp, color = T.Ink)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconCanvas(Icons.Friends, Modifier.size(40.dp))
            GameText("${riders.size} / ${f.spec.spots.size}", fontSize = 20.sp, color = T.Ink)
            ChristmasPicture(ThingType.XM_PARCEL, side = 38.dp)
            GameText("${vm.world.inMachine(f).size} / 6", fontSize = 20.sp, color = T.Ink)
        }
        if (riders.isEmpty()) GameText(SX.passengers.str(), fontSize = 15.sp, color = T.Ink)
        if (choices.isNotEmpty()) {
            LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(choices, key = { it.id }) { person ->
                    val shape = RoundedCornerShape(16.dp)
                    Row(
                        Modifier.background(T.Cream, shape).clickable(role = Role.Button) {
                            if (sleigh.boardSleigh(f, person)) vm.changed()
                            revision++
                        }.heightIn(min = 48.dp).padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconCanvas({ drawSpeciesThumb(person.species, Rect(Offset.Zero, size), person.look) }, Modifier.size(40.dp))
                        GameText(SX.boardSleigh(person.name).str(), fontSize = 15.sp, color = T.Ink)
                    }
                }
            }
        }
        for (stop in Christmas.stops) {
            BigButton(
                S.place(stop).str(),
                onClick = {
                    if (sleigh.fly(f, stop)) {
                        vm.changed()
                        onClose()
                        vm.travelPlayCard(stop, 1.15f)
                    } else revision++
                },
                modifier = Modifier.fillMaxWidth(),
                tone = Tones.Sea,
                enabled = riders.isNotEmpty(),
                icon = Icons.Map,
            )
        }
    }
}
