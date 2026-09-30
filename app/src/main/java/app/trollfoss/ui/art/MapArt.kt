package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.PlaceId

/** Placeholder until the map art lands: sea and one dot per place. Replaced by the real MapArt.kt. */
fun DrawScope.drawIslandMap(pen: Pen, highlight: PlaceId?, t: Float) {
    drawRect(Color(0xFF2F9BFF))
    drawCircle(Color(0xFF7ED957), size.minDimension * 0.42f, center)
    for (p in PlaceId.entries) {
        val s = mapSpot(p)
        inkedCircle(Offset(s.x * size.width, s.y * size.height), size.minDimension * (if (p == highlight) 0.06f else 0.045f), Color(0xFFD2443A), pen)
    }
}

/** Where each place sits on the map, as fractions of the map's width and height. */
fun mapSpot(place: PlaceId): Offset = when (place) {
    PlaceId.HOME -> Offset(0.42f, 0.62f)
    PlaceId.CAFE -> Offset(0.56f, 0.7f)
    PlaceId.SALON -> Offset(0.3f, 0.74f)
    PlaceId.BEACH -> Offset(0.74f, 0.8f)
    PlaceId.FOREST -> Offset(0.5f, 0.42f)
    PlaceId.LAB -> Offset(0.64f, 0.36f)
    PlaceId.MOUNTAIN -> Offset(0.78f, 0.24f)
    PlaceId.FARM -> Offset(0.18f, 0.5f)
    PlaceId.SPACE -> Offset(0.3f, 0.2f)
}
