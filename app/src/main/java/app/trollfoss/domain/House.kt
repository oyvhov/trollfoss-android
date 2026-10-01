package app.trollfoss.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * Storhuset, the big house. It is five places (the floors and the garden, see [PlaceId.manor]) joined by
 * [Passage]s: stairs, a lift, slides, a fireman's pole, hatches and secret doors. Everything a floor
 * brings is described by its [Floor] object: blueprint, rooms, passages, arrivals, and rules.
 *
 * See docs/HUSET.md for the design of the house and for how to build a floor.
 */
enum class PassageKind { STAIRS, LIFT, SLIDE, POLE, HATCH, LADDER, SECRET, DOOR, DUMBWAITER }

/**
 * A way from one place of the house to another. The fixture at [fixture] (a blueprint index in [place])
 * takes whoever stands at it to the [Arrival] named [arrive] in [to]. A [locked] passage works once the
 * world's `unlocked` or `found` contains that key.
 */
data class Passage(
    val id: String,
    val place: PlaceId,
    val fixture: Int,
    val kind: PassageKind,
    val to: PlaceId,
    val arrive: String,
    val oneWay: Boolean = false,
    val locked: String? = null,
)

/** Where figures come out when they arrive by a passage. A NaN [y] means the floor of the place. */
data class Arrival(val name: String, val x: Float, val y: Float = Float.NaN)

/** What one place of the house brings. Each floor lives in its own file: `HouseGround.kt` and so on. */
interface Floor {
    val place: PlaceId

    /** Furniture, things and figures to start with. Fixture indices are the ids' blueprint indices. */
    fun blueprint(): PlaceSpec

    /** The rooms as x ranges, for the designer (wallpaper and floor per room). */
    val rooms: List<ClosedFloatingPointRange<Float>>

    /** Ways out of this floor. */
    val passages: List<Passage>

    /** Where figures arrive on this floor, by name; passages on other floors refer to these names. */
    val arrivals: List<Arrival>

    /**
     * How dark the floor is in the middle of the day, 0 to 1 (the attic and the cellar have no windows to speak of).
     * It works like night: lamps and glowing things light their surroundings, and a finger on the screen is a flashlight.
     */
    val darkness: Float get() = 0f

    /** Where figures like to be here: by day, and at night. Used when the house shuffles who is where. */
    fun hangouts(night: Boolean): List<Arrival> = emptyList()

    /** The spec of a fixture type that belongs to this floor, or null for any other type. */
    fun specOf(type: FixtureType): FixtureSpec? = null

    /** The floor's rules: taps, ticks, seats and drops. */
    fun rules(sim: Sim, random: Random): FloorRules = FloorRules.None
}

/** What a floor does when something is tapped or time passes. Returning true from [tap] says «handled». */
interface FloorRules {
    fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean = false
    fun step(place: PlaceId, f: Fixture, dt: Float) {}

    /** Once per frame for the place on screen, for things that do not belong to one fixture. */
    fun tick(place: PlaceId, dt: Float) {}
    fun seatPoint(f: Fixture, spot: Int): FloatArray? = null
    fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean = false

    object None : FloorRules
}

/** Effects of the house, for [Fx.HOUSE]: the code travels in the high bits of the event's param. */
object HouseFx {
    fun pack(code: Int, arg: Int = 0): Int = (code shl 16) or (arg and 0xFFFF)
    fun code(param: Int): Int = param ushr 16
    fun arg(param: Int): Int = param and 0xFFFF

    /** Codes 0..99 belong to the house itself; each floor has its own block (see docs/HUSET.md). */
    const val PASSAGE = 1
    const val LOCKED = 2
    const val ARRIVED = 3

    /** A golden key was found; arg is how many of the five the child has now. */
    const val KEY_FOUND = 4

    /** All five keys are found: the tunnel door opens. */
    const val KEYS_DONE = 5

    const val GROUND = 100
    const val UPPER = 200
    const val ATTIC = 300
    const val CELLAR = 400
    const val GARDEN = 500
    const val STORY = 600

    /** Mitt hus, the child's own house: building sounds and sparkle (see docs/BYGG.md). */
    const val MINE = 700
}

/** The golden keys of the house: one hides on each floor, in a funny place that takes a small action. */
object HouseKeys {
    val ids = listOf("manor_key_ground", "manor_key_upper", "manor_key_attic", "manor_key_cellar", "manor_key_garden")
    const val TUNNEL = "manor_tunnel"
    fun found(world: World): Int = ids.count { it in world.flags }
}

object House {
    val floors: List<Floor> by lazy { listOf(GroundFloor, UpperFloor, AtticFloor, CellarFloor, GardenFloor, MineYardFloor, MineGroundFloor, MineUpperFloor) }

    fun floor(place: PlaceId): Floor? = if (place.big) floors.firstOrNull { it.place == place } else null

    val passages: List<Passage> by lazy { floors.flatMap { it.passages } }

    /**
     * True for the places whose fixtures may be ways between places: the floors of the house, and Trollhola
     * (its end of the secret tunnel from the cellar).
     */
    fun hasPassages(place: PlaceId): Boolean = place.manor || place == PlaceId.LAB

    /** The passage that [f] is the way in of, or null for any other fixture. */
    fun passageAt(f: Fixture): Passage? {
        if (!hasPassages(f.place)) return null
        return passages.firstOrNull { it.place == f.place && f.place.idBase + it.fixture == f.id }
    }

    fun arrival(to: PlaceId, name: String): Arrival? =
        (floor(to)?.arrivals ?: outside[to])?.firstOrNull { it.name == name }

    /** A locked passage works once its key is in the world's flags (or an event glimt of that id is out or found). */
    fun usable(world: World, p: Passage): Boolean = p.locked == null || p.locked in world.flags || p.locked in world.unlocked || p.locked in world.found

    /** The spec of a house fixture type: the shared ways between floors, then whatever a floor knows. */
    fun specOf(type: FixtureType): FixtureSpec? = shared(type) ?: floors.firstNotNullOfOrNull { it.specOf(type) }

    private fun shared(type: FixtureType): FixtureSpec? = when (type) {
        FixtureType.STAIRCASE -> FixtureSpec(0.84f, 0.58f, light = RRect(-0.4f, -0.6f, 0.4f, 0f))
        FixtureType.LIFT -> FixtureSpec(0.34f, 0.46f, light = RRect(-0.2f, -0.5f, 0.2f, 0f))
        FixtureType.SLIDE -> FixtureSpec(0.70f, 0.48f, surfaces = listOf(SurfaceSpec(-0.3f, -0.15f, -0.48f)))
        FixtureType.FIRE_POLE -> FixtureSpec(0.14f, 0.70f)
        FixtureType.HATCH -> FixtureSpec(0.32f, 0.06f)
        FixtureType.LADDER -> FixtureSpec(0.18f, 0.62f)
        FixtureType.SECRET_DOOR -> FixtureSpec(0.30f, 0.46f)
        FixtureType.DOOR -> FixtureSpec(0.24f, 0.46f, light = RRect(-0.2f, -0.5f, 0.2f, 0f))
        FixtureType.DUMBWAITER -> FixtureSpec(0.20f, 0.26f, wall = true)
        else -> null
    }

    /** Arrival spots outside the house that a passage may lead to (the tunnel from the cellar, for one). */
    val outside: Map<PlaceId, List<Arrival>> = mapOf(
        PlaceId.LAB to listOf(Arrival("tunnel-end", 0.6f)),
        PlaceId.HOME to listOf(Arrival("front-step", 4.0f)),
    )

    /** The rooms of [place] when it is a floor of the house. */
    fun rooms(place: PlaceId): List<ClosedFloatingPointRange<Float>>? = floor(place)?.rooms

    /**
     * People wander the house on their own: whoever is in a floor other than [current], and is not in bed,
     * on a seat or held, may move to another floor, to a place that suits the time of day. Called when the
     * child comes into the house and when someone uses a passage.
     */
    fun shuffle(world: World, current: PlaceId?, random: Random, night: Boolean = world.night) {
        val weights = if (night) {
            mapOf(PlaceId.MANOR_UPPER to 6, PlaceId.MANOR_GROUND to 3, PlaceId.MANOR_ATTIC to 1, PlaceId.MANOR_CELLAR to 1)
        } else {
            mapOf(PlaceId.MANOR_GROUND to 5, PlaceId.MANOR_GARDEN to 3, PlaceId.MANOR_UPPER to 2, PlaceId.MANOR_ATTIC to 1, PlaceId.MANOR_CELLAR to 1)
        }
        val total = weights.values.sum()
        for (p in world.people()) {
            val here = p.place ?: continue
            if (!here.manor || here == current || p.held || p.mode != Mode.FREE) continue
            if (random.nextFloat() > 0.45f) continue
            var roll = random.nextInt(total)
            var target = PlaceId.MANOR_GROUND
            for ((place, w) in weights) {
                roll -= w
                if (roll < 0) { target = place; break }
            }
            if (target == here) continue
            val spot = floor(target)?.hangouts(night)?.takeIf { it.isNotEmpty() }?.let { it[random.nextInt(it.size)] }
            moveTo(world, p, target, spot?.x ?: (0.4f + random.nextFloat() * (target.width - 0.8f)), spot?.y ?: Float.NaN)
        }
    }

    /** Puts [p] (and what it carries) into [to] at ([x], [y]), standing, with no walk or goal left over. */
    fun moveTo(world: World, p: Person, to: PlaceId, x: Float, y: Float = Float.NaN) {
        p.place = to
        p.x = x
        p.y = if (y.isNaN()) to.floor else y
        p.mode = Mode.FREE
        p.holder = -1
        p.vx = 0f
        p.vy = 0f
        p.ground = Float.NaN
        p.restOwner = -2
        p.resting = false
        p.inside = -1
        p.anim.walkTo = Float.NaN
        p.anim.goalFixture = -1
        p.anim.goal = 0
        p.anim.auto = 0
        p.anim.still = 0f
        for (t in world.carried(p)) t.place = to
    }
}

/** Sends taps, ticks, seats and drops to the right floor, and runs the passages between floors. */
class HouseRules(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    private val listener get() = sim.listener
    private val rules: Map<PlaceId, FloorRules> by lazy {
        val floors = House.floors.associate { it.place to it.rules(sim, random) }
        // Trollhola's end of the secret tunnel belongs to the cellar's rules too.
        floors + (PlaceId.LAB to floors.getValue(PlaceId.MANOR_CELLAR))
    }

    fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean {
        if (rules[place]?.tap(place, f, dx, dy) == true) return true
        val passage = House.passageAt(f) ?: return false
        usePassage(passage, f)
        return true
    }

    fun step(place: PlaceId, f: Fixture, dt: Float) {
        rules[place]?.step(place, f, dt)
    }

    fun tick(place: PlaceId, dt: Float) {
        rules[place]?.tick(place, dt)
    }

    fun seatPoint(f: Fixture, spot: Int): FloatArray? = rules[f.place]?.seatPoint(f, spot)

    fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean = rules[place]?.drop(place, f, t) ?: false

    /**
     * Takes whoever stands at [f] (and the child's view) through [passage]. A locked passage only wiggles.
     * Riders arrive at the named spot in the other place and the listener hears [SimListener.onPassage].
     */
    fun usePassage(passage: Passage, f: Fixture) {
        if (!House.usable(world, passage)) {
            listener.onFx(Fx.HOUSE, f.x, f.y - f.spec.h / 2, f, param = HouseFx.pack(HouseFx.LOCKED, passage.kind.ordinal))
            return
        }
        val arrival = House.arrival(passage.to, passage.arrive) ?: return
        val riders = world.bodiesIn(passage.place).filterIsInstance<Person>()
            .filter { !it.held && it.mode == Mode.FREE && abs(it.x - f.x) <= RIDER_REACH && abs(it.y - f.y) <= RIDER_DEPTH }
            .sortedBy { it.x }
        listener.onFx(Fx.HOUSE, f.x, f.y - f.spec.h / 2, f, param = HouseFx.pack(HouseFx.PASSAGE, passage.kind.ordinal))
        for ((i, rider) in riders.withIndex()) {
            val spread = (i - (riders.size - 1) / 2f) * 0.16f
            House.moveTo(world, rider, passage.to, (arrival.x + spread).coerceIn(0.1f, passage.to.width - 0.1f), arrival.y)
            // Slides and poles throw you out of the bottom with a hop.
            if (passage.kind == PassageKind.SLIDE || passage.kind == PassageKind.POLE) {
                rider.vx = 0.25f
                rider.anim.hopV = 1.2f
                rider.anim.face = Face.LAUGH
                rider.anim.faceTime = 1.4f
            }
        }
        House.shuffle(world, passage.to, random)
        listener.onPassage(passage, arrival.x, riders.size)
    }

    private companion object {
        /** Figures within this many scene units of a stair or lift ride along. */
        const val RIDER_REACH = 0.55f
        const val RIDER_DEPTH = 0.3f
    }
}
