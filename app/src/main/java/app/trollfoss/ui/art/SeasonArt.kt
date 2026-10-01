package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.PlaceId

/*
 * Season and feast dressing for every place. THIS FILE IS A STARTING POINT for the seasons work: the two
 * hooks below run after each place's own background and front layer (see PlaceArt.kt), with the season and
 * the feast of the day in `pen.season` and `pen.festival`. The engine already adds a light colour grade,
 * leaves, petals, fireflies and winter snow; the hooks are for things drawn into the scene: bare or golden
 * trees, flowers, wreaths and lights at Christmas, pumpkins, Easter eggs. Summer without a feast draws nothing.
 */

/** After the place's back layer: ground cover and trees' colours (drawn behind the furniture). */
internal fun DrawScope.seasonBack(place: PlaceId, st: Stage, pen: Pen) {
    if (pen.season == app.trollfoss.domain.Season.SUMMER && pen.festival == app.trollfoss.domain.Festival.NONE) return
}

/** After the place's front layer: things in front of everything (snow drifts, hanging lights, falling decorations). */
internal fun DrawScope.seasonFront(place: PlaceId, st: Stage, pen: Pen) {
    if (pen.season == app.trollfoss.domain.Season.SUMMER && pen.festival == app.trollfoss.domain.Festival.NONE) return
}
