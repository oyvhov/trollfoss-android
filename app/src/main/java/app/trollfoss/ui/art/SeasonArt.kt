package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Festival
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season

/*
 * Season and feast dressing for every place. The two hooks below run after each place's own background
 * and front layer (see PlaceArt.kt), with the season and the feast of the day in `pen.season` and
 * `pen.festival`.
 *
 *   SeasonKit.kt          the palette (one place decides the colours of the year) and shared pieces
 *   SeasonArtOutdoor.kt   what the seasons add to the outdoor places: drifts, ice, leaves, flowers, brooks, mist
 *   SeasonArtFeast.kt     the decorations: Christmas, Easter and pumpkin time (the building blocks)
 *   SeasonArtPlaces.kt    where the decorations stand in each place
 *
 * The places' own art already reads `pen.season` through the palette (trees, ground, roofs, windows).
 * Summer without a feast draws nothing, so the plain look stays exactly as it was. The big house
 * (`PlaceId.manor`) dresses itself.
 */

/** After the place's back layer: ground cover and decorations that stand behind the furniture. */
internal fun DrawScope.seasonBack(place: PlaceId, st: Stage, pen: Pen) {
    if (pen.season == Season.SUMMER && pen.festival == Festival.NONE) return
    if (place.manor) return
    if (pen.season != Season.SUMMER) seasonScenery(place, st, pen)
    if (pen.festival != Festival.NONE) feastBack(place, st, pen)
}

/** After the place's front layer: things in front of everything (blossom by the front edge, hanging lights, ...). */
internal fun DrawScope.seasonFront(place: PlaceId, st: Stage, pen: Pen) {
    if (pen.season == Season.SUMMER && pen.festival == Festival.NONE) return
    if (place.manor) return
    if (pen.season != Season.SUMMER) seasonSceneryFront(place, st, pen)
    if (pen.festival != Festival.NONE) feastFront(place, st, pen)
}
