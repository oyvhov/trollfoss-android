package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle

/**
 * Draws a place's background in screen pixels: sky or walls, far scenery with parallax, and the floor
 * or ground band. [cam] is the left edge of the view in scene units and [u] pixels per unit; scene point
 * (x, y) is at ((x - cam) * u, y * u). The world is drawn in oblique 3D («skrå-3D»): the floor band runs
 * from `place.back` to [PlaceId.FRONT], and depth recedes up and to the right (see [Oblique]).
 *
 * [styles] are the home designer's choices, one per room of `Decor.rooms(place)`: a wallpaper (1..11)
 * and a floor (1..8), where 0 keeps the place's own look. Only indoor places that can be decorated use them.
 */
fun DrawScope.drawPlaceBack(place: PlaceId, cam: Float, u: Float, pen: Pen, styles: List<RoomStyle> = emptyList()) {
    CreativeArt.place=place
    val st = Stage(cam, u, size.width, size.height, place.width)
    when (place) {
        PlaceId.BEACH -> beachBack(st, pen)
        PlaceId.FOREST -> forestBack(st, pen)
        PlaceId.MOUNTAIN -> mountainBack(st, pen)
        PlaceId.HEILEBERGET -> bergBack(st, pen)
        PlaceId.VAGSTADDALEN -> valleyBack(st, pen)
        PlaceId.CLOUD_ISLAND -> cloudIslandBack(cam,u,pen)
        PlaceId.HOME -> homeBack(st, pen, styles)
        PlaceId.CAFE -> cafeBack(st, pen, styles)
        PlaceId.SALON -> salonBack(st, pen, styles)
        PlaceId.LAB -> labBack(st, pen)
        PlaceId.FARM -> farmBack(st, pen)
        PlaceId.SPACE -> spaceBack(st, pen)
        PlaceId.TIVOLI -> tivoliBack(st, pen)
        PlaceId.SHOP -> shopBack(st, pen, styles)
        PlaceId.DOCTOR -> doctorBack(st, pen, styles)
        PlaceId.STAGE -> stageBack(st, pen, styles)
        PlaceId.UNDERWATER -> underwaterBack(st, pen)
        PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER, PlaceId.MANOR_ATTIC, PlaceId.MANOR_CELLAR, PlaceId.MANOR_GARDEN -> manorBack(place, st, pen, styles)
        PlaceId.MINE_YARD, PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> mineBack(place, st, pen, styles)
    }
    // A doll's house cut through: what is above the ceiling (see CutawayArt.kt).
    cutaway(place, st, pen)
    seasonBack(place, st, pen)
}

/** Draws what lies in front of everything: the water's front face, snowdrifts, grass tufts. */
fun DrawScope.drawPlaceFront(place: PlaceId, cam: Float, u: Float, pen: Pen) {
    val st = Stage(cam, u, size.width, size.height, place.width)
    when (place) {
        PlaceId.BEACH -> beachFront(st, pen)
        PlaceId.FOREST -> forestFront(st, pen)
        PlaceId.MOUNTAIN -> mountainFront(st, pen)
        PlaceId.HEILEBERGET -> bergFront(st, pen)
        PlaceId.VAGSTADDALEN -> valleyFront(st, pen)
        PlaceId.FARM -> farmFront(st, pen)
        PlaceId.TIVOLI -> tivoliFront(st, pen)
        PlaceId.UNDERWATER -> underwaterFront(st, pen)
        PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER, PlaceId.MANOR_ATTIC, PlaceId.MANOR_CELLAR, PlaceId.MANOR_GARDEN -> manorFront(place, st, pen)
        PlaceId.MINE_YARD, PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> mineFront(place, st, pen)
        else -> Unit
    }
    seasonFront(place, st, pen)
}
