package app.trollfoss.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Players
import app.trollfoss.ui.S
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Select persistent figures together; changing a look uses the same figure workshop. */
@Composable
fun PlayersScreen(vm: TrollfossViewModel) {
    vm.playersVersion
    val team = Players.team(vm.world)
    var friendsOpen by remember { mutableStateOf(false) }
    val compact = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 520
    Column(Modifier.fillMaxSize().background(T.Cream).padding(if (compact) 12.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                GameText(S.choosePlayers.str(), fontSize = if (compact) 22.sp else 30.sp)
                GameText(S.playersHint.str(), fontSize = if (compact) 13.sp else 17.sp)
            }
            RoundButton(S.newFigure.str(), { vm.open(Screen.Creator(null)) },
                size = if (compact) 44.dp else 56.dp, tone = Tones.Grape, icon = Icons.Workshop)
            Spacer(Modifier.width(12.dp))
            RoundButton(S.friendsHere.str(), { friendsOpen = true },
                size = if (compact) 44.dp else 56.dp, tone = Tones.Mint, icon = Icons.House)
            Spacer(Modifier.width(12.dp))
            CloseButton(vm::back, size = if (compact) 44.dp else 56.dp)
        }
        Row(Modifier.fillMaxWidth().height(if (compact) 52.dp else 68.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            team.forEachIndexed { index, person ->
                Row(Modifier.background(T.Sun, RoundedCornerShape(18.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Canvas(Modifier.size(if (compact) 40.dp else 56.dp)) {
                        drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look)
                    }
                    Column {
                        GameText("${S.playerNumber.str()} ${index + 1}", fontSize = 12.sp)
                        GameText(person.name, fontSize = 15.sp)
                    }
                    RoundButton("${S.editPlayer.str()}: ${person.name}", { vm.open(Screen.Creator(person.id)) },
                        size = 40.dp, tone = Tones.Cream, icon = Icons.Workshop)
                }
            }
        }
        LazyVerticalGrid(GridCells.Adaptive(if (compact) 108.dp else 140.dp), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.folk(), key = { it.id }) { person ->
                val index = team.indexOfFirst { it.id == person.id }
                val description = if (index >= 0) "${S.playerNumber.str()} ${index + 1}: ${person.name}" else person.name
                Column(Modifier.background(if (index >= 0) T.Sun else androidx.compose.ui.graphics.Color.White, RoundedCornerShape(18.dp))
                    .border(if (index >= 0) 3.dp else 2.dp, T.Ink, RoundedCornerShape(18.dp))
                    .semantics { contentDescription = description; selected = index >= 0 }
                    .clickable(role = Role.Checkbox) { vm.togglePlayer(person) }.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.height(if (compact) 68.dp else 100.dp).fillMaxWidth()) {
                        drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look)
                    }
                    GameText(person.name, fontSize = 14.sp, maxLines = 1)
                    Box(Modifier.height(20.dp),contentAlignment=Alignment.Center) {
                        if(index>=0) GameText("${S.playerNumber.str()} ${index + 1} ✓",fontSize=12.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GameText(S.togetherHint.str(), Modifier.weight(1f), fontSize = if (compact) 12.sp else 16.sp)
            GameText(if (team.isEmpty()) S.chooseLater.str() else S.playTogether.str(), fontSize = 16.sp)
            RoundButton(if (team.isEmpty()) S.chooseLater.str() else S.playTogether.str(), {
                vm.recallPlayers(); vm.back()
            }, size = if (compact) 44.dp else 60.dp, tone = Tones.Mint, icon = Icons.Check)
        }
    }
    if (friendsOpen) FriendsPanel(vm.world, vm.place,
        onInvite = { vm.engine?.invite(it) }, onPack = { vm.engine?.packPerson(it) },
        onClose = { friendsOpen = false })
}
