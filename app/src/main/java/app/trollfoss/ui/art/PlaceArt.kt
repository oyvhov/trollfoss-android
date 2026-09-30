package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.PlaceId

/**
 * Draws a place's background in screen pixels: sky or walls, far scenery with parallax, and the floor
 * or ground band. [cam] is the left edge of the view in scene units and [u] pixels per unit; scene point
 * (x, y) is at ((x - cam) * u, y * u). The world is drawn in oblique 3D («skrå-3D»): the floor band runs
 * from `place.back` to [PlaceId.FRONT], and depth recedes up and to the right (see [Oblique]).
 */
fun DrawScope.drawPlaceBack(place: PlaceId, cam: Float, u: Float, pen: Pen) {
    val st = Stage(cam, u, size.width, size.height, place.width)
    when (place) {
        PlaceId.BEACH -> beachBack(st, pen)
        PlaceId.FOREST -> forestBack(st, pen)
        PlaceId.MOUNTAIN -> mountainBack(st, pen)
        PlaceId.HOME -> homeBack(st, pen)
        PlaceId.CAFE -> cafeBack(st, pen)
        PlaceId.SALON -> salonBack(st, pen)
        PlaceId.LAB -> labBack(st, pen)
        PlaceId.FARM -> farmBack(st, pen)
        PlaceId.SPACE -> spaceBack(st, pen)
        PlaceId.TIVOLI -> tivoliBack(st, pen)
        PlaceId.SHOP -> shopBack(st, pen)
        PlaceId.DOCTOR -> doctorBack(st, pen)
        PlaceId.STAGE -> stageBack(st, pen)
        PlaceId.UNDERWATER -> underwaterBack(st, pen)
    }
}

/** Draws what lies in front of everything: the water's front face, snowdrifts, grass tufts. */
fun DrawScope.drawPlaceFront(place: PlaceId, cam: Float, u: Float, pen: Pen) {
    val st = Stage(cam, u, size.width, size.height, place.width)
    when (place) {
        PlaceId.BEACH -> beachFront(st, pen)
        PlaceId.FOREST -> forestFront(st, pen)
        PlaceId.MOUNTAIN -> mountainFront(st, pen)
        PlaceId.FARM -> farmFront(st, pen)
        PlaceId.TIVOLI -> tivoliFront(st, pen)
        PlaceId.UNDERWATER -> underwaterFront(st, pen)
        else -> Unit
    }
}
