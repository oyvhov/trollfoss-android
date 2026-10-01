package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, the cellar ("Kjellaren"): workshop, laundry, boiler room, pool and sauna, party room and the
 * secret tunnel to Trollhola. THIS FILE IS A STARTING POINT: the floor's builder replaces the blueprint
 * and adds rules, but keeps the passage ids and arrival names (docs/HUSET.md).
 */
object CellarFloor : Floor {
    override val place = PlaceId.MANOR_CELLAR

    override val rooms = listOf(0f..2.5f, 2.5f..4.5f, 4.5f..6f, 6f..8.5f, 8.5f..10f)

    override val passages = listOf(
        Passage("cellar-stairs-up", place, 0, PassageKind.STAIRS, PlaceId.MANOR_GROUND, "cellar-door"),
        // The way back from Trollhola is added with the tunnel; until then it only goes one way.
        Passage("cellar-tunnel", place, 1, PassageKind.SECRET, PlaceId.LAB, "tunnel-end", oneWay = true, locked = "manor_tunnel"),
    )

    override val arrivals = listOf(
        Arrival("stairs-top", 1.4f),
        Arrival("laundry-chute-end", 3.5f),
        Arrival("tunnel", 9.0f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("workshop", 1.5f), Arrival("laundry", 3.4f), Arrival("pool", 7.0f), Arrival("party", 9.2f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.STAIRCASE, 0.9f, depth = -0.07f),     // 0 up to the hall
                f(FixtureType.SECRET_DOOR, 9.5f, 2, depth = -0.08f), // 1 the tunnel
                f(FixtureType.WORKBENCH, 1.9f, depth = -0.06f),     // 2
                f(FixtureType.DISCO_BALL, 9.0f, depth = 0f),        // 3
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}
