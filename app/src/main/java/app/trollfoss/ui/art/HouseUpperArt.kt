package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType

/*
 * Storhuset, upper floor (Andre høgda): the art. THIS FILE IS A STARTING POINT. The floor's artist replaces the stand-in
 * background and fills in the fixtures and things (docs/HUSET.md, docs/ART_GUIDE.md). Keep the signatures.
 */

/** The place's back layer: walls, floor, windows, far scenery. */
internal fun DrawScope.upperBack(st: Stage, pen: Pen, styles: List<RoomStyle>) = manorStandIn(PlaceId.MANOR_UPPER, st, pen, styles)

/** What lies in front of everything. */
internal fun DrawScope.upperFront(st: Stage, pen: Pen) {}

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawUpperFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean = false

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawUpperFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean = false

/** A thing that belongs to this floor; true when drawn. */
internal fun DrawScope.drawUpperThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean = false