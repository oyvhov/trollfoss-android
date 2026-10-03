package app.trollfoss.ui.art

import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId

/** A doorway painted on a staircase, in scene units from the bottom centre of the fixture (y is negative upwards). */
internal data class Doorway(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float, margin: Float): Boolean =
        x in (left - margin)..(right + margin) && y in (top - margin)..(bottom + margin)
}

/**
 * The same numbers describe both the painted doorway of a staircase and its touch area, the way
 * [FixtureDoors] does for cupboard doors: a child taps the door it wants to go through, not the steps.
 */
internal object StairDoorways {
    /** Storstova: the arch in the wall behind the landing of the grand stairs, [HALL_Z] deep. */
    const val HALL_Z = 0.26f
    val hall = Doorway(-0.4f + FX_DX * HALL_Z, -0.85f + FX_DY * HALL_Z, -0.2f + FX_DX * HALL_Z, -0.52f + FX_DY * HALL_Z)

    /** The cellar: the lit door at the top of the wooden flight. */
    val cellar = Doorway(-0.43f, -0.80f, -0.26f, -0.50f)

    /** The first floor: the little door up to the attic, in the wall [ATTIC_Z] deep. */
    const val ATTIC_Z = 0.07f
    const val ATTIC_LEFT = 0.1f
    const val ATTIC_RIGHT = 0.33f
    const val ATTIC_SILL = -0.53f
    const val ATTIC_TOP = -0.80f
    val attic = Doorway(ATTIC_LEFT + FX_DX * ATTIC_Z, ATTIC_TOP - 0.02f + FX_DY * ATTIC_Z, ATTIC_RIGHT + FX_DX * ATTIC_Z, ATTIC_SILL + FX_DY * ATTIC_Z)

    fun of(f: Fixture): Doorway? = when {
        f.type != FixtureType.STAIRCASE -> null
        f.place == PlaceId.MANOR_GROUND -> hall
        f.place == PlaceId.MANOR_CELLAR -> cellar
        f.place == PlaceId.MANOR_UPPER && f.variant == 1 -> attic
        else -> null
    }

    fun hit(f: Fixture, x: Float, y: Float, margin: Float): Boolean = of(f)?.contains(x, y, margin) == true
}
