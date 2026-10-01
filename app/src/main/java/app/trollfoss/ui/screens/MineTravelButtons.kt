package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.House
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.WorldFactory
import app.trollfoss.ui.SM
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Camera-independent ways through the house. Use the real passages so nearby figures travel along too. */
@Composable
fun MineTravelButtons(vm: TrollfossViewModel, place: PlaceId, compact: Boolean, modifier: Modifier = Modifier) {
    val house = remember(vm.mineVersion) { vm.world.mine }
    if (!house.started) return
    val ways = House.floor(place)?.passages.orEmpty().filter { it.to.mine && House.usable(vm.world, it) }
    if (ways.isEmpty()) return
    Row(modifier.background(T.Cream.copy(alpha = 0.96f), RoundedCornerShape(22.dp))
        .border(2.dp, T.Ink, RoundedCornerShape(22.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (way in ways) {
            val label = when (way.to) {
                PlaceId.MINE_YARD -> SM.outside
                PlaceId.MINE_UPPER -> SM.upstairs
                else -> if (place == PlaceId.MINE_UPPER) SM.downstairs else SM.inside
            }.str()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RoundButton(label, onClick = {
                    vm.world.fixtures[WorldFactory.fixtureId(place, way.fixture)]?.let { vm.sim.house.usePassage(way, it) }
                }, size = if (compact) 40.dp else 48.dp, tone = Tones.Mint,
                    icon = when (way.to) { PlaceId.MINE_UPPER -> Icons.Up; PlaceId.MINE_YARD -> BuildIcons.DoorIn; else -> if (place == PlaceId.MINE_UPPER) Icons.Down else BuildIcons.DoorIn })
                GameText(label, fontSize = 12.sp, color = T.Ink)
            }
        }
    }
}
