package app.trollfoss.domain

import kotlin.random.Random

/**
 * Mitt hus: the child's own house, as in Toca World where one starts by building a house. A plot with a front
 * yard ([PlaceId.MINE_YARD]) and two floors ([PlaceId.MINE_GROUND], [PlaceId.MINE_UPPER]) of five room slots of
 * [Mine.SLOT_W] units. The child lays the foundation (picks a template), builds rooms into the slots, adds a second
 * floor, and dresses the outside. The stairs and the doors are fixed; every piece of furniture in a built room is
 * ordinary furniture from the home designer, so it can be moved, stored and replaced.
 *
 * This file is the skeleton (state, saving, places, passages). The builder's rules, panel and art live in
 * `docs/BYGG.md`'s files: `MineBuilder.kt`, `ui/art/Mine*Art.kt` and `ui/screens/BuildPanel.kt`.
 */
enum class RoomKind { LIVING, KITCHEN, DINING, BEDROOM, KIDS, BATH, LIBRARY, WORKSHOP, MUSIC, GREENHOUSE }

/** The house the child has built so far, and its look from outside. Saved with the world (see WorldStore). */
class MineHouse {
    /** The foundation is laid: a template was chosen and the hall exists. Until then the plot is empty. */
    var started = false

    /** The look from outside; the art knows how many choices each has (see docs/BYGG.md). */
    var shape = 0
    var wall = 0
    var roof = 0
    var roofColor = 0
    var door = 0
    var windows = 0
    var chimney = true
    var flag = false

    /** The second floor has been built; the stairs work. */
    var upperBuilt = false

    /** Per slot: 0 for nothing, otherwise [RoomKind.ordinal] + 1. */
    val ground = IntArray(Mine.SLOTS)
    val upper = IntArray(Mine.SLOTS)

    fun slots(place: PlaceId): IntArray = if (place == PlaceId.MINE_UPPER) upper else ground

    fun kind(place: PlaceId, slot: Int): RoomKind? = slots(place).getOrNull(slot)?.let { if (it <= 0) null else RoomKind.entries[(it - 1).coerceIn(0, RoomKind.entries.size - 1)] }

    fun set(place: PlaceId, slot: Int, kind: RoomKind?) {
        slots(place)[slot] = if (kind == null) 0 else kind.ordinal + 1
    }

    fun roomCount(): Int = ground.count { it > 0 } + upper.count { it > 0 }
}

object Mine {
    const val SLOTS = 5
    const val SLOT_W = 2f

    /** Set in the world's flags when the second floor is built: it opens the stairs up. */
    const val FLAG_UPPER = "mine_upper"

    fun slotRange(slot: Int): ClosedFloatingPointRange<Float> = slot * SLOT_W..(slot + 1) * SLOT_W

    fun slotAt(x: Float): Int = (x / SLOT_W).toInt().coerceIn(0, SLOTS - 1)

    /** The slot ranges as the designer's rooms. */
    val slotRooms: List<ClosedFloatingPointRange<Float>> = List(SLOTS) { slotRange(it) }

    /** Slot 0 of both floors is the hall with the stairs: never built over and never torn down. */
    fun isHall(slot: Int): Boolean = slot == 0

    /** Whether a room may be built in [slot] of [place] now. */
    fun canBuild(house: MineHouse, place: PlaceId, slot: Int): Boolean {
        if (!house.started || slot !in 1 until SLOTS) return false
        return when (place) {
            PlaceId.MINE_GROUND -> house.ground[slot] == 0
            // Upstairs a room needs the second floor, a free slot, and a built room underneath to stand on.
            PlaceId.MINE_UPPER -> house.upperBuilt && house.upper[slot] == 0 && house.ground[slot] != 0
            else -> false
        }
    }

    /** The second floor goes up: the upper hall appears and the stairs work. */
    fun buildUpper(world: World) {
        world.mine.upperBuilt = true
        world.flags += FLAG_UPPER
    }

    /** After loading a save: the flag follows the house. */
    fun sync(world: World) {
        if (world.mine.upperBuilt) world.flags += FLAG_UPPER else world.flags -= FLAG_UPPER
    }
}

/** The plot and its front yard: the facade of the house is art (it follows [MineHouse]); the door leads in. */
object MineYardFloor : Floor {
    override val place = PlaceId.MINE_YARD

    override val rooms = listOf(0f..place.width)

    override val passages = listOf(
        Passage("mine-yard-door", place, 0, PassageKind.DOOR, PlaceId.MINE_GROUND, "front-door"),
    )

    override val arrivals = listOf(
        Arrival("front-door", 4.4f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("door", 4.0f), Arrival("garden", 2.0f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.DOOR, 4.4f, 2, depth = -0.08f),       // 0 the front door of the house
                f(FixtureType.BENCH, 6.6f),                         // 1
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}

/** The ground floor: five slots, the first is the hall with the front door and the stairs. */
object MineGroundFloor : Floor {
    override val place = PlaceId.MINE_GROUND

    override val rooms = Mine.slotRooms

    override val passages = listOf(
        Passage("mine-front-door", place, 0, PassageKind.DOOR, PlaceId.MINE_YARD, "front-door"),
        Passage("mine-stairs-up", place, 1, PassageKind.STAIRS, PlaceId.MINE_UPPER, "landing", locked = Mine.FLAG_UPPER),
    )

    override val arrivals = listOf(
        Arrival("front-door", 0.9f),
        Arrival("stairs-foot", 1.6f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("hall", 1.2f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.DOOR, 0.5f, 2, depth = -0.08f),       // 0 the front door
                f(FixtureType.STAIRCASE, 1.5f, 2, depth = -0.07f),  // 1 up to the second floor
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}

/** The upper floor: five slots above the ground floor's; the first is the landing with the stairs down. */
object MineUpperFloor : Floor {
    override val place = PlaceId.MINE_UPPER

    override val rooms = Mine.slotRooms

    override val passages = listOf(
        Passage("mine-stairs-down", place, 0, PassageKind.STAIRS, PlaceId.MINE_GROUND, "stairs-foot"),
    )

    override val arrivals = listOf(
        Arrival("landing", 1.6f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("landing", 1.2f))

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.STAIRCASE, 1.5f, 2, depth = -0.07f),  // 0 down to the ground floor
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}
