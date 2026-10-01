package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType

/*
 * Storhuset, cellar (Kjellaren): the art. THIS FILE IS A STARTING POINT. The floor's artist replaces the stand-in
 * background and fills in the fixtures and things (docs/HUSET.md, docs/ART_GUIDE.md). Keep the signatures.
 */

/** The place's back layer: walls, floor, windows, far scenery. */
internal fun DrawScope.cellarBack(st: Stage, pen: Pen, styles: List<RoomStyle>) = manorStandIn(PlaceId.MANOR_CELLAR, st, pen, styles)

/** What lies in front of everything. */
internal fun DrawScope.cellarFront(st: Stage, pen: Pen) {}

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawCellarFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean = false

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawCellarFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean = false

/** A thing that belongs to this floor; true when drawn. */
internal fun DrawScope.drawCellarThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean = false