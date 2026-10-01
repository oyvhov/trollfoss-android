package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, first floor ("Andre høgda"): landing, a children's room, the playroom with the slide, the
 * bathroom, the bedroom and the balcony. THIS FILE IS A STARTING POINT: the floor's builder replaces the
 * blueprint and adds rules, but keeps the passage ids and arrival names (docs/HUSET.md).
 */
object UpperFloor : Floor {
    override val place = PlaceId.MANOR_UPPER

    override val rooms = listOf(0f..2.2f, 2.2f..4.2f, 4.2f..6.5f, 6.5f..8.5f, 8.5f..10.5f, 10.5f..12f)

    override val passages = listOf(
        Passage("upper-stairs-down", place, 0, PassageKind.STAIRS, PlaceId.MANOR_GROUND, "stairs-foot"),
        Passage("upper-lift", place, 1, PassageKind.LIFT, PlaceId.MANOR_GROUND, "lift"),
        Passage("upper-stairs-up", place, 2, PassageKind.STAIRS, PlaceId.MANOR_ATTIC, "stairs"),
        Passage("upper-slide", place, 3, PassageKind.SLIDE, PlaceId.MANOR_GROUND, "slide-end", oneWay = true),
        Passage("upper-pole", place, 4, PassageKind.POLE, PlaceId.MANOR_GROUND, "pole-end", oneWay = true),
        Passage("upper-dumbwaiter", place, 5, PassageKind.DUMBWAITER, PlaceId.MANOR_GROUND, "dumbwaiter"),
        Passage("upper-balcony-slide", place, 6, PassageKind.SLIDE, PlaceId.MANOR_GARDEN, "slide-end", oneWay = true),
        Passage("upper-laundry-chute", place, 7, PassageKind.HATCH, PlaceId.MANOR_CELLAR, "laundry-chute-end", oneWay = true),
    )

    override val arrivals = listOf(
        Arrival("landing", 1.75f),
        Arrival("lift", 2.1f),
        Arrival("dumbwaiter", 1.0f),
        Arrival("attic-stairs", 0.9f),
    )

    override fun hangouts(night: Boolean) =
        if (night) listOf(Arrival("kids-bed", 3.0f), Arrival("bedroom", 9.4f), Arrival("bath", 7.4f))
        else listOf(Arrival("landing", 1.5f), Arrival("kids", 3.2f), Arrival("play", 5.2f), Arrival("balcony", 11.2f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.STAIRCASE, 1.4f, depth = -0.07f),     // 0 down to the hall
                f(FixtureType.LIFT, 2.1f, depth = -0.08f),          // 1
                f(FixtureType.STAIRCASE, 0.45f, 1, depth = -0.07f), // 2 up to the attic
                f(FixtureType.SLIDE, 5.0f, depth = -0.02f),         // 3 playroom slide down to the living room
                f(FixtureType.FIRE_POLE, 3.9f, depth = 0.0f),       // 4 pole to the hall
                FixtureDef(FixtureType.DUMBWAITER, 1.0f, 0.55f),    // 5 hatch to the kitchen
                f(FixtureType.SLIDE, 11.4f, 1, depth = 0.0f),       // 6 balcony slide down to the garden
                f(FixtureType.HATCH, 7.4f, depth = 0.04f),          // 7 laundry chute in the bathroom
                f(FixtureType.BED, 9.4f, depth = -0.06f),           // 8
                f(FixtureType.BUNK_BED, 3.1f, depth = -0.06f),      // 9
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}
