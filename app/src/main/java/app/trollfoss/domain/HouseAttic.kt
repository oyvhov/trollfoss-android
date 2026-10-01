package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, the attic ("Loftet"): trunks and old things, a friendly ghost's nook, the telescope tower and
 * a secret room behind the library. THIS FILE IS A STARTING POINT: the floor's builder replaces the
 * blueprint and adds rules, but keeps the passage ids and arrival names (docs/HUSET.md).
 */
object AtticFloor : Floor {
    override val place = PlaceId.MANOR_ATTIC

    override val rooms = listOf(0f..3f, 3f..5.5f, 5.5f..7.5f, 7.5f..9f)

    override val passages = listOf(
        Passage("attic-stairs-down", place, 0, PassageKind.STAIRS, PlaceId.MANOR_UPPER, "attic-stairs"),
        Passage("attic-secret-down", place, 1, PassageKind.SECRET, PlaceId.MANOR_GROUND, "library-secret", locked = "manor_bookshelf"),
    )

    override val arrivals = listOf(
        Arrival("stairs", 1.3f),
        Arrival("secret-room", 8.0f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("trunks", 1.8f), Arrival("nook", 4.0f), Arrival("tower", 6.4f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.STAIRCASE, 0.8f, depth = -0.07f),     // 0 down to the first floor
                f(FixtureType.SECRET_DOOR, 8.4f, 1, depth = -0.08f), // 1 the way back down to the library
                f(FixtureType.CHEST, 2.2f, depth = -0.02f),         // 2
                f(FixtureType.TELESCOPE, 6.4f, depth = -0.04f),     // 3
            ),
            things = emptyList(),
            people = listOf(PersonDef(Species.GHOST, Look(skin = 9), 4.0f, name = "Sture")),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}
