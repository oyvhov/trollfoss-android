package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType

/*
 * Mitt hus: the art. The routing to the yard (MineYardArt.kt, with the facade of MineHouseArt.kt), the rooms
 * (MineRoomArt.kt) and the furniture of the rooms and the yard (MineFixtureArt.kt). The kit is in MineKit.kt.
 */

/** The place's back layer. */
internal fun DrawScope.mineBack(place: PlaceId, st: Stage, pen: Pen, styles: List<RoomStyle>) {
    if (place == PlaceId.MINE_YARD) mineYardBack(st, pen) else mineInteriorBack(place, st, pen, styles)
}

/** What lies in front of everything. */
internal fun DrawScope.mineFront(place: PlaceId, st: Stage, pen: Pen) {}

/** The back layer of a fixture in one of the three places of the house, and of the pieces of its rooms anywhere; true when drawn. */
internal fun DrawScope.drawMineFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean = drawMineFixture(f, u, pen, false)

/** The front layer of a fixture in one of the three places of the house. */
internal fun DrawScope.drawMineFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean = drawMineFixture(f, u, pen, true)

/** A thing that belongs to the builder; true when drawn. */
internal fun DrawScope.drawMineThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean = false
