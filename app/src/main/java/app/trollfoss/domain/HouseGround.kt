package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, ground floor: hall with the grand stairs, living room, library, dining room, kitchen and
 * winter garden, six rooms of two units each. THIS FILE IS A STARTING POINT: the floor's builder replaces
 * the blueprint and adds rules, but keeps the passage ids and arrival names (docs/HUSET.md).
 */
object GroundFloor : Floor {
    override val place = PlaceId.MANOR_GROUND

    override val rooms = listOf(0f..2f, 2f..4f, 4f..6f, 6f..8f, 8f..10f, 10f..12f)

    override val passages = listOf(
        Passage("ground-stairs-up", place, 0, PassageKind.STAIRS, PlaceId.MANOR_UPPER, "landing"),
        Passage("ground-lift", place, 1, PassageKind.LIFT, PlaceId.MANOR_UPPER, "lift"),
        Passage("ground-cellar-door", place, 2, PassageKind.STAIRS, PlaceId.MANOR_CELLAR, "stairs-top"),
        Passage("ground-bookshelf", place, 3, PassageKind.SECRET, PlaceId.MANOR_ATTIC, "secret-room", locked = "manor_bookshelf"),
        Passage("ground-dumbwaiter", place, 4, PassageKind.DUMBWAITER, PlaceId.MANOR_UPPER, "dumbwaiter"),
        Passage("ground-garden-door", place, 5, PassageKind.DOOR, PlaceId.MANOR_GARDEN, "house-door"),
    )

    override val arrivals = listOf(
        Arrival("stairs-foot", 1.75f),
        Arrival("lift", 2.3f),
        Arrival("cellar-door", 0.9f),
        Arrival("library-secret", 5.1f),
        Arrival("dumbwaiter", 9.0f),
        Arrival("garden-door", 11.0f),
        Arrival("slide-end", 3.2f),
        Arrival("pole-end", 1.0f),
    )

    override fun hangouts(night: Boolean) = listOf(
        Arrival("hall", 1.0f), Arrival("living", 3.0f), Arrival("library", 4.8f), Arrival("dining", 7.0f), Arrival("kitchen", 8.8f),
    )

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.STAIRCASE, 1.2f, depth = -0.07f),     // 0 up to the first floor
                f(FixtureType.LIFT, 2.0f, depth = -0.08f),          // 1
                f(FixtureType.DOOR, 0.35f, depth = -0.08f),         // 2 cellar door
                f(FixtureType.SECRET_DOOR, 5.6f, depth = -0.08f),   // 3 the bookshelf in the library
                FixtureDef(FixtureType.DUMBWAITER, 9.4f, 0.55f),    // 4 hatch in the kitchen wall
                f(FixtureType.DOOR, 11.5f, depth = -0.08f),         // 5 door to the garden
                f(FixtureType.SOFA, 3.0f, depth = -0.04f),          // 6
                f(FixtureType.TABLE, 6.9f, depth = 0.03f),          // 7
                f(FixtureType.CHAIR, 6.6f, depth = 0.03f),          // 8
                f(FixtureType.CHAIR, 7.2f, depth = 0.03f),          // 9
            ),
            things = emptyList(),
            people = listOf(PersonDef(Species.ROBOT, Look(skin = 10), 2.6f, name = "Rolf")),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}
