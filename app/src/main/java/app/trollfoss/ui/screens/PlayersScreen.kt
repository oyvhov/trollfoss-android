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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
                Text(S.choosePlayers.str(), color = T.Ink, style = MaterialTheme.typography.headlineLarge,
                    fontSize = if (compact) 22.sp else 30.sp)
                Text(S.playersHint.str(), color = T.InkSoft, style = MaterialTheme.typography.bodyLarge,
                    fontSize = if (compact) 13.sp else 17.sp)
            }
            RoundButton(S.newFigure.str(), { vm.open(Screen.Creator(null)) },
                size = if (compact) 44.dp else 56.dp, tone = Tones.Grape, icon = Icons.Workshop)
            Spacer(Modifier.width(12.dp))
            RoundButton(S.friendsHere.str(), { friendsOpen = true },
                size = if (compact) 44.dp else 56.dp, tone = Tones.Cream, icon = Icons.House)
            Spacer(Modifier.width(12.dp))
            CloseButton(vm::back, size = if (compact) 44.dp else 56.dp)
        }
        Row(Modifier.fillMaxWidth().height(if (compact) 52.dp else 68.dp).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (team.isEmpty()) {
                Canvas(Modifier.size(if (compact) 32.dp else 44.dp)) { Icons.Friends(this) }
                Text(S.chooseTeamHint.str(), color = T.InkSoft, style = MaterialTheme.typography.bodyMedium)
            }
            team.forEachIndexed { index, person ->
                Row(Modifier.background(T.SunTop, RoundedCornerShape(18.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Canvas(Modifier.size(if (compact) 40.dp else 56.dp)) {
                        drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look)
                    }
                    Column {
                        Text("${S.playerNumber.str()} ${index + 1}", color = T.InkSoft, style = MaterialTheme.typography.bodySmall)
                        Text(person.name, color = T.Ink, style = MaterialTheme.typography.titleMedium, fontSize = 15.sp)
                    }
                    RoundButton("${S.editPlayer.str()}: ${person.name}", { vm.open(Screen.Creator(person.id)) },
                        size = 40.dp, tone = Tones.Cream, icon = Icons.Workshop)
                }
            }
        }
        LazyVerticalGrid(GridCells.Adaptive(if (compact) 108.dp else 154.dp), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.folk(), key = { it.id }) { person ->
                val index = team.indexOfFirst { it.id == person.id }
                val description = if (index >= 0) "${S.playerNumber.str()} ${index + 1}: ${person.name}" else person.name
                val shape = RoundedCornerShape(22.dp)
                Column(Modifier.clip(shape).background(if (index >= 0) T.SunTop else Color.White)
                    .border(if (index >= 0) 3.dp else 1.dp, if (index >= 0) T.Grape else T.CreamLine, shape)
                    .semantics { contentDescription = description; selected = index >= 0 }
                    .clickable(role = Role.Checkbox) { vm.togglePlayer(person) }.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Canvas(Modifier.height(if (compact) 74.dp else 112.dp).fillMaxWidth()) {
                            val backdrop = if (index >= 0) T.Sun else lerp(T.Cream, T.MintTop, 0.22f)
                            drawCircle(backdrop, size.height * 0.43f, Offset(size.width / 2f, size.height * 0.48f))
                            drawOval(T.Ink.copy(alpha = 0.08f),
                                Offset(size.width / 2f - size.height * 0.25f, size.height * 0.9f),
                                Size(size.height * 0.5f, size.height * 0.07f))
                            drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look)
                        }
                        if (index >= 0) Canvas(Modifier.align(Alignment.TopEnd).size(24.dp)
                            .background(T.Grape, CircleShape).padding(4.dp)) { Icons.Check(this) }
                    }
                    Text(person.name, color = T.Ink, style = MaterialTheme.typography.titleMedium,
                        fontSize = if (compact) 14.sp else 16.sp, maxLines = 1)
                    Box(Modifier.height(20.dp),contentAlignment=Alignment.Center) {
                        if(index>=0) Text("${S.playerNumber.str()} ${index + 1}", color = T.InkSoft,
                            style = MaterialTheme.typography.bodySmall, fontSize=12.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(S.togetherHint.str(), Modifier.weight(1f), color = T.InkSoft,
                style = MaterialTheme.typography.bodyMedium, fontSize = if (compact) 12.sp else 16.sp)
            Text(if (team.isEmpty()) S.chooseLater.str() else S.playTogether.str(), color = T.Ink,
                style = MaterialTheme.typography.titleMedium, fontSize = 16.sp)
            RoundButton(if (team.isEmpty()) S.chooseLater.str() else S.playTogether.str(), {
                vm.recallPlayers(); vm.back()
            }, size = if (compact) 44.dp else 60.dp, tone = Tones.Mint, icon = Icons.Check)
        }
    }
    if (friendsOpen) FriendsPanel(vm.world, vm.place,
        onInvite = { vm.engine?.invite(it) }, onPack = { vm.engine?.packPerson(it) },
        onClose = { friendsOpen = false })
}
