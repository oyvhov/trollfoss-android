package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Decor
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType

/*
 * Storhuset's art. Every floor has its own art file (HouseGroundArt.kt, HouseUpperArt.kt, HouseAtticArt.kt,
 * HouseCellarArt.kt, HouseGardenArt.kt) with the same five entry points: the place's back and front, the
 * back and front of its fixtures, and its things. The functions here only route to the right file. Each
 * line of a `when` is separated by a blank line, so floors can be merged without conflicts.
 */

internal fun DrawScope.manorBack(place: PlaceId, st: Stage, pen: Pen, styles: List<RoomStyle>) {
    when (place) {
        PlaceId.MANOR_GROUND -> groundBack(st, pen, styles)

        PlaceId.MANOR_UPPER -> upperBack(st, pen, styles)

        PlaceId.MANOR_ATTIC -> atticBack(st, pen, styles)

        PlaceId.MANOR_CELLAR -> cellarBack(st, pen, styles)

        PlaceId.MANOR_GARDEN -> gardenBack(st, pen)

        else -> Unit
    }
}

internal fun DrawScope.manorFront(place: PlaceId, st: Stage, pen: Pen) {
    when (place) {
        PlaceId.MANOR_GROUND -> groundFront(st, pen)

        PlaceId.MANOR_UPPER -> upperFront(st, pen)

        PlaceId.MANOR_ATTIC -> atticFront(st, pen)

        PlaceId.MANOR_CELLAR -> cellarFront(st, pen)

        PlaceId.MANOR_GARDEN -> gardenFront(st, pen)

        else -> Unit
    }
}

/** True when [f] belongs to the big house and its floor's art drew it. */
internal fun DrawScope.drawManorFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean = when (f.place) {
    PlaceId.MANOR_GROUND -> drawGroundFixtureBack(f, u, pen, contents)

    PlaceId.MANOR_UPPER -> drawUpperFixtureBack(f, u, pen, contents)

    PlaceId.MANOR_ATTIC -> drawAtticFixtureBack(f, u, pen, contents)

    PlaceId.MANOR_CELLAR -> drawCellarFixtureBack(f, u, pen, contents)

    PlaceId.MANOR_GARDEN -> drawGardenFixtureBack(f, u, pen, contents)

    else -> false
}

internal fun DrawScope.drawManorFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean = when (f.place) {
    PlaceId.MANOR_GROUND -> drawGroundFixtureFront(f, u, pen)

    PlaceId.MANOR_UPPER -> drawUpperFixtureFront(f, u, pen)

    PlaceId.MANOR_ATTIC -> drawAtticFixtureFront(f, u, pen)

    PlaceId.MANOR_CELLAR -> drawCellarFixtureFront(f, u, pen)

    PlaceId.MANOR_GARDEN -> drawGardenFixtureFront(f, u, pen)

    else -> false
}

/** Things made for the house: the floors are asked one after the other until one draws the type. */
internal fun DrawScope.drawManorThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float) {
    if (type == ThingType.GOLDEN_KEY) return thGoldenKey(w, h, pen)
    if (drawGroundThing(type, variant, used, w, h, pen, cook)) return
    if (drawUpperThing(type, variant, used, w, h, pen, cook)) return
    if (drawAtticThing(type, variant, used, w, h, pen, cook)) return
    if (drawCellarThing(type, variant, used, w, h, pen, cook)) return
    if (drawGardenThing(type, variant, used, w, h, pen, cook)) return
    if (drawMineThing(type, variant, used, w, h, pen, cook)) return
    // Nobody knows this thing yet: a plain round marker, so it can still be found and picked up.
    drawCircle(Color(0xFFFFC83D), minOf(w, h) * 0.45f, Offset(0f, -h / 2f))
    drawCircle(Ink.line, minOf(w, h) * 0.45f, Offset(0f, -h / 2f), style = pen.thin)
}

/** A background for a floor that has no art of its own yet. */
internal fun DrawScope.manorStandIn(place: PlaceId, st: Stage, pen: Pen, styles: List<RoomStyle>) {
    val u = st.u
    val back = place.back
    if (place.outdoor) {
        val sky = Brush.verticalGradient(listOf(Color(0xFF7FC4F2), Color(0xFFD9F0FF)), startY = 0f, endY = back * u)
        drawRect(sky, Offset.Zero, Size(st.w, back * u))
        drawRect(Color(0xFF6DBB5A), Offset(0f, back * u), Size(st.w, (FRONT_Y - back) * u))
        drawBase(st, pen, Color(0xFF8A6B45), Color(0xFF5E4630))
        return
    }
    val rooms = Decor.rooms(place)
    for ((i, r) in rooms.withIndex()) {
        if (!st.sees(r.start, r.endInclusive)) continue
        val wall = styles.wallOf(i).takeIf { it > 0 } ?: (1 + (i * 3 + place.ordinal) % (Decor.WALLS - 1))
        paperWall(st, wall, back, r.start, r.endInclusive)
        val floor = styles.floorOf(i).takeIf { it > 0 } ?: (1 + (i * 2 + place.ordinal) % (Decor.FLOORS - 1))
        layFloor(st, floor, back, r.start, r.endInclusive)
    }
    skirting(st, pen, back, Color(0xFFF7F3EC))
    crown(st, pen, Color(0xFFF7F3EC))
    wallShadow(st, back)
    drawBase(st, pen, Color(0xFFE7C497), Color(0xFF9C6B45))
}
