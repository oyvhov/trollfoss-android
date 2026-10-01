package app.trollfoss.domain

import kotlin.random.Random

/**
 * Mitt hus: the child's own house, as in Toca World where one starts by building a house. A plot with a front
 * yard ([PlaceId.MINE_YARD]) and two floors ([PlaceId.MINE_GROUND], [PlaceId.MINE_UPPER]) of five room slots of
 * [Mine.SLOT_W] units. The child lays the foundation (picks a template), builds rooms into the slots, adds a second
 * floor, and dresses the outside. The stairs and the doors are fixed; every piece of furniture in a built room is
 * ordinary furniture from the home designer, so it can be moved, stored and replaced.
 *
 * This file holds the state, saving, places and passages. The builder's rules are in `MineBuilder.kt`, the room kinds'
 * furniture in `MineRooms.kt`; the art is in `ui/art/Mine*Art.kt` and the panel in `ui/screens/BuildPanel.kt` (docs/BYGG.md).
 */
enum class RoomKind { LIVING, KITCHEN, DINING, BEDROOM, KIDS, BATH, LIBRARY, WORKSHOP, MUSIC, GREENHOUSE }

/** What the builder is busy with: each takes a few seconds of hammering, dust and sparkle. */
enum class JobKind { FOUNDATION, ROOM, UPPER }

/** One building job: the art draws scaffolding, tools and a crane from [t]; the rules commit the result at [commitAt]. Never saved. */
class MineJob(val kind: JobKind, val place: PlaceId, val slot: Int, val room: RoomKind?, val shape: Int) {
    var t = 0f
    var committed = false
    var pieceIndex = 0
    var nextHit = 0f
    var hits = 0
    var pieces: List<MinePiece> = emptyList()

    val total: Float get() = when (kind) {
        JobKind.FOUNDATION -> 5.2f
        JobKind.ROOM -> 4.2f
        JobKind.UPPER -> 5.4f
    }

    val commitAt: Float get() = when (kind) {
        JobKind.FOUNDATION -> 3.4f
        JobKind.ROOM -> 2.3f
        JobKind.UPPER -> 3.4f
    }

    /** How far the work has come, 0 to 1. */
    val progress: Float get() = (t / total).coerceIn(0f, 1f)
}

/** A figure called to the housewarming: where it stood before, so it can go back. Saved while the party lasts. */
class Guest(val id: Int, val place: PlaceId, val x: Float)

/** The housewarming party in progress: seconds gone, and the place it is held in. Never saved. */
class MineParty(val place: PlaceId) {
    var t = 0f
    var nextConfetti = 0f
}

/** The house the child has built so far, and its look from outside. Saved with the world (see WorldStore). */
class MineHouse {
    /** The foundation is laid: a template was chosen and the hall exists. Until then the plot is empty. */
    var started = false

    /** The look from outside; the art knows how many choices each has ([Mine.TEMPLATES] and so on). */
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

    /** Figures called to a housewarming that are still away from home (saved, so nobody is lost if the app closes). */
    val guests = ArrayList<Guest>()

    // ---- never saved ----
    /** Build mode: empty slots show a frame with a plus. */
    var buildMode = false

    /** The slot picked in the panel (in [selectedPlace]), or -1. */
    var selected = -1
    var selectedPlace = PlaceId.MINE_GROUND

    /** The job running now, if any. */
    var job: MineJob? = null

    /** A room the child pressed long on: the app asks before tearing it down (slot in [askPlace]), or -1. */
    var askDemolish = -1
    var askPlace = PlaceId.MINE_GROUND

    /** The shed was tapped: the panel should open. The UI clears it. */
    var wantPanel = false

    var party: MineParty? = null

    /** Bumps with every change worth redrawing a panel for. */
    var version = 0

    fun slots(place: PlaceId): IntArray = if (place == PlaceId.MINE_UPPER) upper else ground

    fun kind(place: PlaceId, slot: Int): RoomKind? = slots(place).getOrNull(slot)?.let { if (it <= 0) null else RoomKind.entries[(it - 1).coerceIn(0, RoomKind.entries.size - 1)] }

    fun set(place: PlaceId, slot: Int, kind: RoomKind?) {
        slots(place)[slot] = if (kind == null) 0 else kind.ordinal + 1
    }

    fun roomCount(): Int = ground.count { it > 0 } + upper.count { it > 0 }

    /** True when something stands in [slot] of [place]: a room, or the hall that comes with the foundation or the second floor. */
    fun standing(place: PlaceId, slot: Int): Boolean = when {
        slot !in 0 until Mine.SLOTS -> false
        slot == 0 -> if (place == PlaceId.MINE_UPPER) upperBuilt else started
        else -> slots(place)[slot] > 0
    }

    /** One number for everything the outside shows: the key of the map's picture of the house. */
    fun hash(): Int {
        var h = if (started) 1 else 0
        for (v in intArrayOf(shape, wall, roof, roofColor, door, windows, if (chimney) 1 else 0, if (flag) 1 else 0, if (upperBuilt) 1 else 0)) h = h * 31 + v
        for (v in ground) h = h * 31 + v
        for (v in upper) h = h * 31 + v
        return h
    }
}

/** The events of the builder, in the [HouseFx.MINE] block (see [HouseFx.pack]); the effect player is `ui/play/MineFx.kt`. */
object MineEvent {
    const val CHANGED = HouseFx.MINE + 1
    const val HAMMER = HouseFx.MINE + 2
    const val SAW = HouseFx.MINE + 3
    const val DRILL = HouseFx.MINE + 4
    const val PLANK = HouseFx.MINE + 5
    const val DUST = HouseFx.MINE + 6
    const val WALLS_UP = HouseFx.MINE + 7
    const val ROOM_DONE = HouseFx.MINE + 8
    const val FOUNDATION_START = HouseFx.MINE + 9
    const val FOUNDATION_DONE = HouseFx.MINE + 10
    const val CRANE = HouseFx.MINE + 11
    const val UPPER_DONE = HouseFx.MINE + 12
    const val DEMOLISH = HouseFx.MINE + 13
    const val SELECT = HouseFx.MINE + 14
    const val LOOK = HouseFx.MINE + 15
    const val PARTY_START = HouseFx.MINE + 16
    const val PARTY_END = HouseFx.MINE + 17
    const val GUEST = HouseFx.MINE + 18
    const val DOOR_PASS = HouseFx.MINE + 19
    const val DENIED = HouseFx.MINE + 20
    const val PIECE = HouseFx.MINE + 21
    const val CONFETTI = HouseFx.MINE + 22
    const val STARTED_TAP = HouseFx.MINE + 23

    // What the furniture of the rooms and the yard does when it is tapped.
    const val FLARE = HouseFx.MINE + 30
    const val CANDLE = HouseFx.MINE + 31
    const val CHANDELIER = HouseFx.MINE + 32
    const val TOAST = HouseFx.MINE + 33
    const val NIGHT_LIGHT = HouseFx.MINE + 34
    const val BLOCKS_FALL = HouseFx.MINE + 35
    const val BLOCKS_BUILD = HouseFx.MINE + 36
    const val HORSE = HouseFx.MINE + 37
    const val LADDER = HouseFx.MINE + 38
    const val GLOBE = HouseFx.MINE + 39
    const val SAW_BENCH = HouseFx.MINE + 40
    const val SOCKS = HouseFx.MINE + 41
    const val STRUM = HouseFx.MINE + 42
    const val WATER_BED = HouseFx.MINE + 43
    const val HANGING_POT = HouseFx.MINE + 44
    const val COAT_RACK = HouseFx.MINE + 45
    const val MAILBOX = HouseFx.MINE + 46
    const val FENCE = HouseFx.MINE + 47
    const val FLOWER_BED = HouseFx.MINE + 48
    const val SWING = HouseFx.MINE + 49
    const val BIRD_BATH = HouseFx.MINE + 50
    const val GNOME = HouseFx.MINE + 51
    const val SANDBOX = HouseFx.MINE + 52
    const val APPLE_TREE = HouseFx.MINE + 53
    const val APPLE_BONK = HouseFx.MINE + 54
}

object Mine {
    const val SLOTS = 5
    const val SLOT_W = 2f

    const val TEMPLATES = 4
    const val WALL_COLORS = 12
    const val ROOFS = 4
    const val ROOF_COLORS = 8
    const val DOORS = 6
    const val WINDOWS = 4

    /** Set in the world's flags when the second floor is built: it opens the stairs up. */
    const val FLAG_UPPER = "mine_upper"

    /** Set when the foundation is laid: it opens the front door of the yard. */
    const val FLAG_STARTED = "mine_started"

    /** Set the first time a housewarming has been held (it earns a sticker). */
    const val FLAG_PARTY = "mine_party"

    /** The house seen from the yard: the first slot's left edge, and the width of each slot, in yard units. */
    const val FACADE_X0 = 2.0f
    const val FACADE_MW = 1.2f

    /** The shed on the empty plot: a tap on it opens the builder panel. */
    const val SHED_X = 4.7f

    /** How far a point on the back wall is shifted right in the oblique picture (see `recede` in the art). */
    const val WALL_SHIFT = 0.236f
    private const val RECEDE = 1.3889f

    /** Rooms needed before the second floor can go up, and before the housewarming button shows. */
    const val ROOMS_FOR_FLOOR = 2
    const val ROOMS_FOR_PARTY = 3

    fun slotRange(slot: Int): ClosedFloatingPointRange<Float> = slot * SLOT_W..(slot + 1) * SLOT_W

    fun slotAt(x: Float): Int = (x / SLOT_W).toInt().coerceIn(0, SLOTS - 1)

    /** The slot under a point of the picture: the walls are shifted to the right by their depth, the floor by its height. */
    fun slotAtScene(x: Float, y: Float): Int {
        val yy = y.coerceIn(0.80f, PlaceId.FRONT)
        return slotAt(x - (PlaceId.FRONT - yy) * RECEDE)
    }

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

    /** The second floor may go up: the foundation is laid, it is not built yet, and two rooms stand. */
    fun canBuildUpper(house: MineHouse): Boolean = house.started && !house.upperBuilt && house.roomCount() >= ROOMS_FOR_FLOOR

    /** A room may be torn down: it is built, it is not a hall, and no room stands on top of it. */
    fun canDemolish(house: MineHouse, place: PlaceId, slot: Int): Boolean {
        if (slot !in 1 until SLOTS || house.kind(place, slot) == null) return false
        return place != PlaceId.MINE_GROUND || house.upper[slot] == 0
    }

    fun canParty(house: MineHouse): Boolean = house.started && house.roomCount() >= ROOMS_FOR_PARTY

    /** The slots of [place] where a room could be built right now. */
    fun buildable(house: MineHouse, place: PlaceId): List<Int> = (1 until SLOTS).filter { canBuild(house, place, it) }

    /** The second floor goes up: the upper hall appears and the stairs work. */
    fun buildUpper(world: World) {
        world.mine.upperBuilt = true
        world.flags += FLAG_UPPER
        syncFixtures(world)
    }

    /** After loading a save: the flags follow the house. */
    fun sync(world: World) {
        if (world.mine.upperBuilt) world.flags += FLAG_UPPER else world.flags -= FLAG_UPPER
        if (world.mine.started) world.flags += FLAG_STARTED else world.flags -= FLAG_STARTED
        syncFixtures(world)
    }

    /** The fixtures whose picture depends on the house carry it in their mode, so their cached pictures follow. */
    fun syncFixtures(world: World) {
        val h = world.mine
        val door = if (h.started) h.door + 1 else 0
        for (id in listOf(WorldFactory.fixtureId(PlaceId.MINE_YARD, 0), WorldFactory.fixtureId(PlaceId.MINE_GROUND, 0))) world.fixtures[id]?.mode = door
        val stairs = if (h.upperBuilt) 1 else 0
        for (id in listOf(WorldFactory.fixtureId(PlaceId.MINE_GROUND, 1), WorldFactory.fixtureId(PlaceId.MINE_UPPER, 0))) world.fixtures[id]?.mode = stairs
    }

    /** The look each template starts with: wall, roof, roof colour, door, windows, chimney, flag. */
    fun applyTemplate(h: MineHouse, shape: Int) {
        h.shape = shape.coerceIn(0, TEMPLATES - 1)
        when (h.shape) {
            // Hytte: a log cabin with a grassy roof.
            0 -> { h.wall = 7; h.roof = 2; h.roofColor = 4; h.door = 2; h.windows = 3; h.chimney = true; h.flag = false }
            // Villa: pale walls, a hipped slate roof and arched windows.
            1 -> { h.wall = 2; h.roof = 1; h.roofColor = 2; h.door = 1; h.windows = 1; h.chimney = true; h.flag = false }
            // Tårnhus: lilac with a round tower and a flag.
            2 -> { h.wall = 9; h.roof = 0; h.roofColor = 3; h.door = 3; h.windows = 2; h.chimney = false; h.flag = true }
            // Gamalt bondehus: falu red with white trim and a dark roof.
            else -> { h.wall = 0; h.roof = 0; h.roofColor = 5; h.door = 4; h.windows = 0; h.chimney = true; h.flag = false }
        }
    }

    /**
     * Keeps furniture inside the rooms that stand: an empty slot is open air. A point is moved into the nearest slot
     * that has a room, and kept [half] units from its walls.
     */
    fun clampX(house: MineHouse, place: PlaceId, x: Float, half: Float): Float {
        if (place != PlaceId.MINE_GROUND && place != PlaceId.MINE_UPPER) return x
        val stand = (0 until SLOTS).filter { house.standing(place, it) }
        if (stand.isEmpty()) return x
        val slot = slotAt(x).takeIf { it in stand } ?: stand.minByOrNull { kotlin.math.abs(x - (it * SLOT_W + SLOT_W / 2f)) }!!
        val lo = slot * SLOT_W + 0.03f + minOf(half, SLOT_W / 2f - 0.05f)
        val hi = (slot + 1) * SLOT_W - 0.03f - minOf(half, SLOT_W / 2f - 0.05f)
        return x.coerceIn(lo, hi)
    }

    /** Sends the guests of a housewarming home (also after a save was loaded in the middle of one). */
    fun returnGuests(world: World) {
        for (g in world.mine.guests) {
            val p = world.bodies[g.id] as? Person ?: continue
            if (p.place?.mine == true) House.moveTo(world, p, g.place, g.x)
        }
        world.mine.guests.clear()
        world.mine.party = null
    }
}

/** The plot and its front yard: the facade of the house is art (it follows [MineHouse]); the door leads in. */
object MineYardFloor : Floor {
    override val place = PlaceId.MINE_YARD

    override val rooms = listOf(0f..place.width)

    override val passages = listOf(
        // The door works once the foundation is laid.
        Passage("mine-yard-door", place, 0, PassageKind.DOOR, PlaceId.MINE_GROUND, "front-door", locked = Mine.FLAG_STARTED),
    )

    override val arrivals = listOf(
        Arrival("front-door", Mine.FACADE_X0 + Mine.FACADE_MW / 2f),
    )

    override fun hangouts(night: Boolean) = listOf(Arrival("door", Mine.FACADE_X0 + 0.6f), Arrival("garden", 1.2f))

    override fun specOf(type: FixtureType): FixtureSpec? = MineRooms.specOf(type)

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = listOf(
                f(FixtureType.DOOR, Mine.FACADE_X0 + Mine.FACADE_MW / 2f, 2, depth = -0.08f),  // 0 the front door of the house
                f(FixtureType.BENCH, 3.9f, depth = 0.05f),                                    // 1
                f(FixtureType.MI_MAILBOX, 1.35f, depth = 0.04f),                              // 2
                f(FixtureType.MI_APPLE_TREE, 0.75f, depth = -0.02f),                          // 3
            ),
            things = emptyList(),
            people = emptyList(),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = sim.mine.rules
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

    override fun specOf(type: FixtureType): FixtureSpec? = MineRooms.specOf(type)

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

    override fun rules(sim: Sim, random: Random): FloorRules = sim.mine.rules
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

    override fun specOf(type: FixtureType): FixtureSpec? = MineRooms.specOf(type)

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

    override fun rules(sim: Sim, random: Random): FloorRules = sim.mine.rules
}
