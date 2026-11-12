package app.trollfoss.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Species
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.S
import app.trollfoss.ui.SX
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.PressSurface
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

private val villagePlaces = listOf(
    PlaceId.CHRISTMAS_SQUARE, PlaceId.CHRISTMAS_HOME,
    PlaceId.CHRISTMAS_WORKSHOP, PlaceId.CHRISTMAS_STABLE,
)

@Composable
internal fun ChristmasTravelChoices(
    current: PlaceId,
    returnPlace: PlaceId,
    onVisit: (PlaceId) -> Unit,
    onReturn: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 540.dp) 4 else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in villagePlaces.chunked(columns)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (place in row) {
                        val label = when {
                            place != PlaceId.CHRISTMAS_SQUARE -> S.place(place).str()
                            current.christmas -> SX.villageSquare.str()
                            else -> SX.visit.str()
                        }
                        val here = SX.here.str()
                        val selected = place == current
                        PressSurface(
                            onClick = { onVisit(place) },
                            modifier = Modifier.weight(1f).semantics {
                                if (selected) stateDescription = here
                            },
                            description = label,
                            tone = if (selected || (!current.christmas && place == PlaceId.CHRISTMAS_SQUARE)) Tones.Mint else Tones.Cream,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CachedThumb("christmas-route:${place.name}", 54.dp) {
                                        val box = Rect(Offset.Zero, size)
                                        when (place) {
                                            PlaceId.CHRISTMAS_SQUARE -> drawFixtureThumb(FixtureType.XM_TREE, 0, box)
                                            PlaceId.CHRISTMAS_HOME -> drawThingThumb(ThingType.XM_HAT, 0, box)
                                            PlaceId.CHRISTMAS_WORKSHOP -> drawThingThumb(ThingType.XM_TRAIN, 0, box)
                                            PlaceId.CHRISTMAS_STABLE -> drawSpeciesThumb(Species.REINDEER, box)
                                            else -> Unit
                                        }
                                    }
                                    if (selected) IconCanvas(Icons.Check, Modifier.align(Alignment.TopEnd).size(20.dp))
                                }
                                GameText(label, color = T.Ink, outline = Color.Transparent,
                                    fontSize = 15.sp, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
    if (current.christmas) {
        val label = SX.backTo(S.place(returnPlace)).str()
        PressSurface(
            onClick = onReturn,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            tone = Tones.Cream,
            description = label,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconCanvas(Icons.Back, Modifier.size(25.dp))
                GameText(label, color = T.Ink, outline = Color.Transparent, fontSize = 16.sp)
            }
        }
    }
}
