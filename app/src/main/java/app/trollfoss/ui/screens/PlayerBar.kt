package app.trollfoss.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Players
import app.trollfoss.ui.S
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Tap your portrait to join the room on screen; hold it to change your clothes and look. */
@Composable
fun PlayerBar(vm: TrollfossViewModel, compact: Boolean, modifier: Modifier = Modifier) {
    vm.playersVersion
    vm.tasksVersion // Includes packing and undo while keeping the same selected IDs.
    val team = Players.team(vm.world)
    val bring = S.recallPlayers.str()
    val edit = S.editPlayer.str()
    val playerLabel = S.playerNumber.str()
    Row(modifier.widthIn(max = 240.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        team.forEachIndexed { index, person ->
            Row(Modifier.background(T.Cream.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
                .border(2.dp, T.Sun, RoundedCornerShape(14.dp))
                .semantics { contentDescription = "$playerLabel ${index + 1}: ${person.name}" }
                .combinedClickable(role = Role.Button, onClickLabel = bring, onLongClickLabel = edit,
                    onClick = { vm.recallPlayer(person) }, onLongClick = { vm.open(Screen.Creator(person.id)) })
                .padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(if (compact) 30.dp else 42.dp)) {
                    drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look)
                }
                GameText(if (Players.paused(vm.world, person)) "${index + 1} · zZ" else "${index + 1}", fontSize = if (compact) 12.sp else 15.sp, color = T.Ink)
            }
        }
    }
}
