package app.trollfoss.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Person
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Mode
import app.trollfoss.domain.World
import app.trollfoss.ui.S
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.TrollDialog
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Named friends from the whole village, including those in the bag and the child's own figures. */
@Composable
fun FriendsPanel(world: World, place: PlaceId, onInvite: (Person) -> Unit, onPack: (Person) -> Unit, onClose: () -> Unit) {
    val friends = world.people().filter { it.name.isNotBlank() }.sortedBy { it.name }
    val here = friends.filter { it.place == place && it.mode != Mode.BAG }
    val away = friends - here.toSet()
    TrollDialog(onClose) {
        GameText(S.friends.str(), fontSize = 24.sp)
        GameText(S.packedFriendHint.str(), fontSize = 15.sp)
        if (here.isNotEmpty()) {
            GameText(S.friendsHere.str(), fontSize = 20.sp)
            FriendCards(here, S.packPerson.str()) { onPack(it); onClose() }
        }
        if (away.isNotEmpty()) {
            GameText(S.bringFriend.str(), fontSize = 20.sp)
            FriendCards(away, S.bringFriend.str()) { onInvite(it); onClose() }
        }
    }
}

@Composable
private fun FriendCards(people: List<Person>, action: String, onClick: (Person) -> Unit) {
    for (row in people.chunked(4)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (person in row) {
                Column(
                    Modifier.weight(1f).background(T.Sea.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button, onClickLabel = "$action: ${person.name}") { onClick(person) }
                        .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Canvas(Modifier.size(64.dp)) { drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look) }
                    GameText(person.name, fontSize = if (person.name.length > 8) 12.sp else 14.sp, maxLines = 1)
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                        GameText(action, fontSize = 12.sp, maxLines = 2)
                    }
                }
            }
            repeat(4 - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
        }
    }
}
