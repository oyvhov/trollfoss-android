package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType

/*
 * Mitt hus: the art. THIS FILE IS A STARTING POINT for the builder (docs/BYGG.md): the stand-in background and
 * empty fixture and thing hooks. The builder replaces them with a yard that shows the house the child has built,
 * interiors that show built rooms (and scaffolding or sky for empty slots), and the room kinds' art.
 */

/** The place's back layer. */
internal fun DrawScope.mineBack(place: PlaceId, st: Stage, pen: Pen, styles: List<RoomStyle>) = manorStandIn(place, st, pen, styles)

/** What lies in front of everything. */
internal fun DrawScope.mineFront(place: PlaceId, st: Stage, pen: Pen) {}

/** The back layer of a fixture in one of the three places of the house; true when drawn. */
internal fun DrawScope.drawMineFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean = false

/** The front layer of a fixture in one of the three places of the house. */
internal fun DrawScope.drawMineFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean = false

/** A thing that belongs to the builder; true when drawn. */
internal fun DrawScope.drawMineThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean = false
