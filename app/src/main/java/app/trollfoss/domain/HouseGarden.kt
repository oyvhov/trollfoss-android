package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, the garden ("Hagen"): the house's back door, a greenhouse, a pond with the balcony slide, a
 * treehouse, a shed and a trampoline. THIS FILE IS A STARTING POINT: the floor's builder replaces the
 * blueprint and adds rules, but keeps the passage ids and arrival names (docs/HUSET.md).
 */
object GardenFloor : Floor {
    override val place = PlaceId.MANOR_GARDEN

    override val rooms = listOf(0f..place.width)

    override val passages = listOf(
        Passage("garden-house-door", place, 0, PassageKind.DOOR, PlaceId.MANOR_GROUND, "garden-door"),
    )

    override val arrivals = listOf(
        Arrival("house-door", 1.4f),
        Arrival("slide-end", 5.0f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("door", 2.0f), Arrival("pond", 5.0f), Arrival("trees", 7.5f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.DOOR, 0.8f, 1, depth = -0.08f),       // 0 the back door of the house
                f(FixtureType.BENCH, 2.6f, depth = 0.0f),           // 1
                f(FixtureType.PINE_TREE, 7.4f, depth = -0.06f),     // 2
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}
