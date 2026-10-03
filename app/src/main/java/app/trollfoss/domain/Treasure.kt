package app.trollfoss.domain

import kotlin.math.hypot

/** Gems, coins and pearls: what a child collects. Nothing in the game takes them away on its own. */
object Treasure {
    val TYPES = setOf(ThingType.GEM, ThingType.COIN, ThingType.PEARL)

    fun isTreasure(t: Thing): Boolean = t.type in TYPES

    /** How many [type] lie about in [place]: free, not held and not inside any furniture. */
    fun loose(world: World, place: PlaceId, type: ThingType): Int =
        world.bodiesIn(place).count { it is Thing && it.type == type && it.mode == Mode.FREE && !it.held && it.inside < 0 }

    /**
     * The oldest loose thing a full place may let go of: never a treasure, never [keep] (the thing just
     * made) and never one the caller wants to [spare]. Null when there is nothing it may take.
     */
    fun oldestToDrop(things: List<Thing>, keep: Thing?, spare: (Thing) -> Boolean = { false }): Thing? =
        things.filter { it !== keep && it.mode == Mode.FREE && it.inside < 0 && !it.held && !isTreasure(it) && !spare(it) }
            .minByOrNull { it.z }
}

/**
 * The glass-fronted treasure box: the lid lifts for a hand that comes near with a find, the finds line up on
 * three shelves so each can be seen, and a box with finds in it never goes into the store.
 */
class TreasureBox(private val sim: Sim) {
    private val world get() = sim.world
    private val listener get() = sim.listener

    /** What lies in [f], oldest first. */
    fun holds(f: Fixture): List<Thing> =
        world.bodiesIn(f.place).filterIsInstance<Thing>()
            .filter { it.inside == f.id && it.mode == Mode.FREE && !it.held }.sortedBy { it.z }

    /** A tap opens or shuts the box by hand; then it stays the way the child left it ([Fixture.timer] 0). */
    fun tap(place: PlaceId, f: Fixture) {
        f.open = !f.open
        f.timer = 0f
        sim.invalidate(place)
        listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2f, f)
    }

    /** The lid lifts for a held thing that comes near, and falls shut [LINGER] seconds after it is gone. */
    fun step(place: PlaceId, dt: Float) {
        for (f in world.fixturesIn(place)) {
            if (f.type != FixtureType.TREASURE_BOX) continue
            val cy = f.y - f.spec.h / 2f
            val near = world.bodiesIn(place).any { it is Thing && it.held && hypot(it.x - f.x, it.y - it.h / 2f - cy) < REACH }
            when {
                near && !f.open -> {
                    f.open = true
                    f.timer = LINGER
                    sim.invalidate(place)
                    listener.onFx(Fx.OPEN, f.x, cy, f)
                }
                near && f.timer > 0f -> f.timer = LINGER
                !near && f.open && f.timer > 0f -> {
                    f.timer -= dt
                    if (f.timer <= 0f) {
                        f.timer = 0f
                        f.open = false
                        sim.invalidate(place)
                        listener.onFx(Fx.CLOSE, f.x, cy, f)
                    }
                }
            }
        }
    }

    /** A thing let go over the box goes straight onto a shelf. */
    fun put(place: PlaceId, f: Fixture, t: Thing): Boolean {
        t.place = place
        t.mode = Mode.FREE
        t.holder = -1
        t.held = false
        t.inside = f.id
        t.z = world.nextZ()
        received(f)
        return true
    }

    /** A thing that came to rest on a shelf by itself (thrown in, or dropped just above). */
    fun landed(t: Thing, owner: Int) {
        val f = world.fixtures[owner] ?: return
        if (f.type == FixtureType.TREASURE_BOX) received(f)
    }

    private fun received(f: Fixture) {
        val things = arrange(f)
        listener.onFx(Fx.TREASURE_IN, f.x, f.y - f.spec.h / 2f, f)
        if (things.isNotEmpty() && things.size % PARTY == 0) {
            f.anim = 1f
            listener.onFx(Fx.TREASURE_PARTY, f.x, f.top, f)
        }
    }

    /** Lines everything in the box up on its shelves, seven to a row, the oldest at the bottom left. */
    fun arrange(f: Fixture): List<Thing> {
        val things = holds(f)
        things.forEachIndexed { i, t ->
            val slot = i % SLOTS
            t.x = f.x + FIRST_X + (slot % PER_ROW) * STEP_X
            t.y = f.y + ROWS[slot / PER_ROW]
            t.vx = 0f
            t.vy = 0f
            t.vrot = 0f
            t.rot = 0f
            t.resting = true
            t.restOwner = f.id
            t.inside = f.id
            t.ground = f.depth + 0.012f
        }
        return things
    }

    /** A box with finds in it stays out of the store: it opens and wobbles, so the child sees why. */
    fun refuse(place: PlaceId, f: Fixture) {
        if (!f.open) {
            f.open = true
            sim.invalidate(place)
        }
        f.timer = LINGER
        f.anim = 1f
        listener.onFx(Fx.OPEN, f.x, f.y - f.spec.h / 2f, f)
    }

    companion object {
        /** How near a held thing must come for the lid to lift, in scene units. */
        const val REACH = 0.25f

        /** Seconds the lid stays up after the thing is gone. */
        const val LINGER = 1.2f

        /** Every this many finds, the box has a little party. */
        const val PARTY = 5

        const val PER_ROW = 7
        const val SLOTS = 21
        const val FIRST_X = -0.09f
        const val STEP_X = 0.03f

        /** The three shelves, as heights above the bottom of the box (the interior surfaces of its spec). */
        val ROWS = floatArrayOf(-0.015f, -0.07f, -0.125f)
    }
}
