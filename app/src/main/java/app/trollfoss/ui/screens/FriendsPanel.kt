package app.trollfoss.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import app.trollfoss.domain.World
import app.trollfoss.ui.S
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.TrollDialog
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Named friends from the whole village, including those in the bag and the child's own figures. */
@Composable
fun FriendsPanel(world: World, onInvite: (Person) -> Unit, onClose: () -> Unit) {
    TrollDialog(onClose) {
        GameText(S.bringFriend.str(), fontSize = 24.sp)
        for (row in world.people().filter { it.name.isNotBlank() }.sortedBy { it.name }.chunked(4)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (person in row) {
                    Column(
                        Modifier.weight(1f).background(T.Sea.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .clickable(role = Role.Button, onClickLabel = S.bringFriend.str()) { onInvite(person); onClose() }
                            .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Canvas(Modifier.size(64.dp)) { drawSpeciesThumb(person.species, Rect(0f, 0f, size.width, size.height), person.look) }
                        GameText(person.name, fontSize = if (person.name.length > 8) 12.sp else 14.sp, maxLines = 1)
                    }
                }
                repeat(4 - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
