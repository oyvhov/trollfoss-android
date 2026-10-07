package app.trollfoss.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * Level 9 «Oppfinnar»: a robot workshop that builds little robot pals, a helper robot that fetches loose things and
 * brings them to a friend, a rocket kit that carries a rider up and back down, and a reaction course of four coloured
 * pads. Only what the world already saves is saved: things on a tray stay on it, pals are ordinary loose things.
 */
class InventorPlay(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world

    private enum class Phase { TO_THING, TO_PERSON, COUNT, UP, HANG, DOWN }
    private class Job(var phase: Phase, var thing: Int, var person: Int = -1)
    private class Flight(var phase: Phase = Phase.COUNT, var t: Float = 0f, var tick: Int = 4)
    private class Course(var lit: Int = -1, var last: Int = -1, var wait: Float = 0.7f, var left: Float = 0f, var streak: Int = 0, var misses: Int = 0)

    /**
     * Seconds left of a workshop build, and of the rest after one; jobs, flights and runs. Not saved: a fresh start is fine.
     * Keyed by the fixture itself (weakly), so a toy stored away and a new one that gets the same id never share a state.
     */
    private val building = java.util.WeakHashMap<Fixture, Float>()
    private val rest = java.util.WeakHashMap<Fixture, Float>()
    private val jobs = java.util.WeakHashMap<Fixture, Job>()
    private val flights = java.util.WeakHashMap<Fixture, Flight>()
    private val courses = java.util.WeakHashMap<Fixture, Course>()

    fun tap(f: Fixture, dx: Float = 0f, dy: Float = 0f): Boolean {
        when (f.type) {
            FixtureType.PLAY_ROBOT_WORKSHOP -> workshop(f)
            FixtureType.PLAY_HELPER_ROBOT -> helper(f)
            FixtureType.PLAY_ROCKET_KIT -> launch(f)
            FixtureType.PLAY_REACTION_COURSE -> course(f, dx, dy)
            else -> return false
        }
        return true
    }

    fun drop(f: Fixture, t: Thing): Boolean {
        if (!accepts(f, t) || t.held) return false
        // A robot on a job has its hands full.
        if (f.type == FixtureType.PLAY_HELPER_ROBOT && jobs.containsKey(f)) return false
        val inside = world.inMachine(f)
        if (inside.size >= capacity(f)) return false
        t.mode = Mode.INSIDE; t.holder = f.id; t.inside = -1; t.resting = false; t.restOwner = -2; t.vx = 0f; t.vy = 0f
        if (f.type == FixtureType.PLAY_ROBOT_WORKSHOP) { t.x = f.x + 0.02f + inside.size * 0.1f; t.y = f.y - BENCH } else { t.x = f.x + f.shiftX + TRAY_DX; t.y = f.y - TRAY }
        f.anim = 1f; sim.listener.onFx(Fx.INTO, f.x, f.top, f, t); return true
    }

    fun step(f: Fixture, dt: Float): Boolean {
        when (f.type) {
            FixtureType.PLAY_ROBOT_WORKSHOP -> stepWorkshop(f, dt)
            FixtureType.PLAY_HELPER_ROBOT -> stepHelper(f, dt)
            FixtureType.PLAY_ROCKET_KIT -> stepRocket(f, dt)
            FixtureType.PLAY_REACTION_COURSE -> stepCourse(f, dt)
            else -> return false
        }
        return true
    }

    // ------------------------------------------------------------------ robot workshop

    private fun workshop(f: Fixture) {
        if (building.containsKey(f)) return
        f.anim = 1f
        if (world.inMachine(f).size < 2) { sim.listener.onFx(Fx.BONK, f.x, f.top, f); return }
        val full = world.bodiesIn(f.place).count { it is Thing && it.type == ThingType.ROBOT_PAL && it.mode == Mode.FREE } >= MAX_PALS
        if (full || (rest[f] ?: 0f) > 0f) { sim.listener.onFx(Fx.SPARKLE, f.x, f.top, f); return }
        building[f] = BUILD_TIME
        sim.listener.onFx(Fx.BUILD, f.x, f.top, f, param = 0)
    }

    private fun stepWorkshop(f: Fixture, dt: Float) {
        rest[f]?.let { rest[f] = it - dt }
        val left = building[f] ?: return
        f.anim = 0.7f
        val next = left - dt
        if ((left * 2f).toInt() != (next * 2f).toInt()) sim.listener.onFx(Fx.BEEP, f.x, f.top, f, param = (next * 2f).toInt())
        if (next > 0f) { building[f] = next; return }
        building.remove(f)
        rest[f] = REST
        val pal = world.addThing(ThingType.ROBOT_PAL, (f.id + world.stickers.size) % 4, f.place, f.x + 0.3f, f.top)
        pal.ground = f.depth; pal.vy = -0.4f; pal.resting = false
        sim.listener.onSpawn(pal)
        sim.listener.onFx(Fx.GIFT, f.x, f.top, f, pal, 0)
        sim.firstTime(First.ROBOT_WORKSHOP, f.x, f.top)
    }

    // ------------------------------------------------------------------ helper robot

    private fun helper(f: Fixture) {
        if (jobs.containsKey(f)) return
        f.anim = 1f
        val cargo = world.inMachine(f).firstOrNull()
        val thing = cargo ?: nearestFetchable(f)
        if (thing == null) {
            // Nothing to fetch: a little robot dance and a cheerful beep.
            sim.listener.onFx(Fx.BEEP, f.x + f.shiftX, f.top, f, param = 1)
            world.people().filter { it.place == f.place && free(it) && abs(it.x - f.x) < 0.8f }
                .forEach { it.anim.face = Face.GRIN; it.anim.faceTime = 1.5f }
            return
        }
        jobs[f] = if (cargo != null) Job(Phase.TO_PERSON, cargo.id, nearestPerson(f)?.id ?: -1) else Job(Phase.TO_THING, thing.id)
        f.on = true
        sim.listener.onFx(Fx.BEEP, f.x + f.shiftX, f.top, f, param = 0)
    }

    private fun stepHelper(f: Fixture, dt: Float) {
        val me = f.x + f.shiftX
        val cargo = world.inMachine(f).firstOrNull()
        cargo?.let { it.x = me + TRAY_DX; it.y = f.y - TRAY }
        val job = jobs[f]
        if (job == null) {
            // No orders: roll quietly back to the spot, carrying whatever lies on the tray.
            if (abs(f.shiftX) > 0.001f) f.shiftX += (-f.shiftX).coerceIn(-dt * SPEED, dt * SPEED) else { f.shiftX = 0f; f.on = false }
            return
        }
        when (job.phase) {
            Phase.TO_THING -> {
                val t = world.bodies[job.thing] as? Thing
                if (t == null || !fetchable(f, t)) { jobs.remove(f); f.on = false; return }
                if (rollTo(f, t.x, dt)) {
                    // Something was put on the tray on the way: deliver that, and leave the thing on the floor.
                    if (cargo != null) { job.phase = Phase.TO_PERSON; job.person = nearestPerson(f)?.id ?: -1; return }
                    t.mode = Mode.INSIDE; t.holder = f.id; t.inside = -1; t.resting = false; t.restOwner = -2; t.vx = 0f; t.vy = 0f
                    sim.listener.onFx(Fx.INTO, t.x, f.top, f, t)
                    job.phase = Phase.TO_PERSON; job.person = nearestPerson(f)?.id ?: -1
                }
            }
            Phase.TO_PERSON -> {
                val c = cargo ?: run { jobs.remove(f); f.on = false; return }
                val p = (world.bodies[job.person] as? Person)?.takeIf { free(it) && it.place == f.place }
                val side = if (p != null && p.x < me) 1f else -1f
                if (rollTo(f, p?.let { it.x + side * 0.2f } ?: me, dt)) { deliver(f, c, p); jobs.remove(f) }
            }
            else -> jobs.remove(f)
        }
    }

    /** Rolls the robot towards [x] inside its room and reach; true when it has got as near as it can. */
    private fun rollTo(f: Fixture, x: Float, dt: Float): Boolean {
        val room = roomOf(f)
        val lo = maxOf(room.start + 0.15f - f.x, -REACH)
        val hi = minOf(room.endInclusive - 0.15f - f.x, REACH)
        val goal = (x - f.x).coerceIn(lo, maxOf(lo, hi))
        val step = (goal - f.shiftX).coerceIn(-dt * SPEED, dt * SPEED)
        f.shiftX += step
        return abs(goal - f.shiftX) < 0.03f
    }

    private fun deliver(f: Fixture, c: Thing, p: Person?) {
        val me = f.x + f.shiftX
        c.mode = Mode.FREE; c.holder = -1; c.restOwner = -2; c.resting = false; c.vx = 0f; c.vy = -0.25f; c.ground = f.depth
        c.x = if (p != null) p.x + (if (p.x < me) 0.12f else -0.12f) else me + 0.12f
        c.y = f.y - TRAY
        sim.listener.onFx(Fx.SPARKLE, c.x, c.y, f, c)
        p?.anim?.let { it.face = Face.LAUGH; it.faceTime = 1.6f; it.hopV = 1.2f }
        sim.firstTime(First.HELPER_ROBOT, c.x, c.y)
    }

    /** A helper robot works in its own room, like the robot vacuum. */
    private fun roomOf(f: Fixture) = Decor.rooms(f.place)[Decor.roomAt(f.place, f.x)]

    private fun free(p: Person) = p.mode == Mode.FREE && !p.held && p.anim.pose != Pose.LIE

    private fun nearestPerson(f: Fixture): Person? =
        world.people().filter { it.place == f.place && free(it) && it.x in roomOf(f) && abs(it.x - (f.x + f.shiftX)) < REACH + 0.4f }.minByOrNull { abs(it.x - (f.x + f.shiftX)) }

    private fun fetchable(f: Fixture, t: Thing): Boolean =
        t.place == f.place && t.mode == Mode.FREE && !t.held && t.resting && t.restOwner == -1 && t.flyT < 0f &&
            t.x in roomOf(f) && abs(t.x - f.x) <= REACH && abs(sim.groundOf(f.place, t) - f.depth) < 0.1f && sim.poolAt(t.x, t.y) == null

    private fun nearestFetchable(f: Fixture): Thing? =
        world.bodiesIn(f.place).filterIsInstance<Thing>().filter { fetchable(f, it) }.minByOrNull { abs(it.x - (f.x + f.shiftX)) }

    // ------------------------------------------------------------------ rocket kit

    private fun launch(f: Fixture) {
        if (flights.containsKey(f)) return
        flights[f] = Flight(); f.on = true; f.anim = 1f
    }

    private fun stepRocket(f: Fixture, dt: Float) {
        val fl = flights[f] ?: run { f.angle = 0f; f.on = false; return }
        val rider = world.seatedAt(f, 0)
        fl.t += dt
        when (fl.phase) {
            Phase.COUNT -> {
                f.anim = 0.5f
                val tick = 3 - (fl.t / 0.5f).toInt()
                if (tick in 1..3 && tick < fl.tick) { fl.tick = tick; sim.listener.onFx(Fx.COUNTDOWN, f.x, f.top, f, param = tick) }
                if (fl.t >= COUNT_TIME) {
                    fl.phase = Phase.UP; fl.t = 0f
                    sim.listener.onFx(Fx.KIT_LAUNCH, f.x, f.y, f)
                    sim.firstTime(First.ROCKET_KIT, f.x, f.top)
                }
            }
            Phase.UP -> {
                f.angle = smooth(fl.t / UP_TIME)
                rider?.anim?.let { it.face = Face.OOH; it.faceTime = 0.5f }
                if (fl.t >= UP_TIME) { f.angle = 1f; fl.phase = Phase.HANG; fl.t = 0f; sim.listener.onFx(Fx.FIREWORK, f.x, f.y - RISE - f.spec.h, f) }
            }
            Phase.HANG -> if (fl.t >= HANG_TIME) { fl.phase = Phase.DOWN; fl.t = 0f }
            Phase.DOWN -> {
                f.angle = 1f - smooth(fl.t / DOWN_TIME)
                if (fl.t >= DOWN_TIME) {
                    f.angle = 0f; f.on = false; flights.remove(f)
                    sim.listener.onFx(Fx.POOF, f.x, f.y, f)
                    rider?.anim?.let { it.face = Face.LAUGH; it.faceTime = 2f }
                }
            }
            else -> flights.remove(f)
        }
    }

    private fun smooth(k: Float): Float { val x = k.coerceIn(0f, 1f); return x * x * (3f - 2f * x) }

    // ------------------------------------------------------------------ reaction course

    private fun course(f: Fixture, dx: Float, dy: Float) {
        val c = courses[f]
        if (c == null) {
            courses[f] = Course(); f.angle = 0f; f.angleV = 0f
            sim.listener.onFx(Fx.BEEP, f.x, f.top, f, param = 0)
            return
        }
        if (c.lit < 0) return // a tap between two lights costs nothing
        // The dialog's Use button taps from high above the board: it presses whichever pad is lit, for those who cannot aim.
        val pad = if (dy <= ASSIST_DY) c.lit else padAt(dx, dy)
        if (pad == c.lit) hit(f, c) else miss(f, c)
    }

    private fun hit(f: Fixture, c: Course) {
        val pad = c.lit
        c.streak++; c.misses = 0; c.lit = -1; c.wait = 0.45f
        f.angle = 0f; f.angleV = c.streak.toFloat(); f.anim = 1f
        sim.listener.onFx(Fx.REACT, f.x + (pad - 1.5f) * PAD, f.top, f, param = pad * 16 + c.streak)
        if (c.streak >= GOAL) {
            courses.remove(f); f.angleV = 0f
            sim.listener.onFx(Fx.CONFETTI, f.x, f.top, f)
            world.people().filter { it.place == f.place && free(it) && abs(it.x - f.x) < 1f }
                .forEach { it.anim.face = Face.LAUGH; it.anim.faceTime = 2f; if (it.anim.hop <= 0f) it.anim.hopV = 1.4f }
            sim.firstTime(First.REACTION_COURSE, f.x, f.top)
        }
    }

    private fun miss(f: Fixture, c: Course) {
        c.streak = 0; c.misses++; c.lit = -1; c.wait = 0.9f
        f.angle = 0f; f.angleV = 0f
        sim.listener.onFx(Fx.BONK, f.x, f.top, f)
        // Three misses in a row and the board takes a little nap, ready for a fresh start.
        if (c.misses >= 3) courses.remove(f)
    }

    private fun stepCourse(f: Fixture, dt: Float) {
        val c = courses[f] ?: return
        if (c.lit < 0) {
            c.wait -= dt
            if (c.wait > 0f) return
            var pad = random.nextInt(4)
            if (pad == c.last) pad = (pad + 1 + random.nextInt(3)) % 4
            c.lit = pad; c.last = pad
            c.left = maxOf(0.9f, 2f - 0.22f * c.streak)
            f.angle = pad + 1f
            sim.listener.onFx(Fx.TICK, f.x + (pad - 1.5f) * PAD, f.top, f)
        } else {
            c.left -= dt
            if (c.left <= 0f) miss(f, c)
        }
    }

    companion object {
        val TYPES = setOf(FixtureType.PLAY_ROBOT_WORKSHOP, FixtureType.PLAY_HELPER_ROBOT, FixtureType.PLAY_ROCKET_KIT, FixtureType.PLAY_REACTION_COURSE)

        /** How high the rocket flies above its pad, in scene units. */
        const val RISE = 0.38f

        /** Width of one reaction pad, and how many rights in a row win. */
        const val PAD = 0.16f
        const val GOAL = 5
        const val MAX_PALS = 8
        const val BUILD_TIME = 2.5f
        const val REST = 45f
        const val REACH = 1.2f
        /** The top of the pads: how high above the board's foot, and how deep in the slanted drawing (see [padAt]). */
        private const val SURFACE = -0.018f
        private const val SURFACE_DEPTH = 0.15f

        /** How far right and up a unit of depth is drawn: the same numbers as `ui.art.Oblique` (a test keeps them equal). */
        internal const val SLANT_X = 0.5f
        internal const val SLANT_Y = -0.36f
        /** A tap this far above a board cannot be a finger on a pad: it is the dialog's Use button. */
        private const val ASSIST_DY = -0.15f
        private const val SPEED = 0.55f
        /** Where a helper robot's tray is: how high above the floor and how far to the right of the robot. */
        private const val TRAY = 0.225f
        private const val TRAY_DX = 0.085f
        /** Height of the workbench top above the floor, where the two things it is given lie. */
        private const val BENCH = 0.15f
        private const val COUNT_TIME = 1.5f
        private const val UP_TIME = 1.4f
        private const val HANG_TIME = 0.5f
        private const val DOWN_TIME = 2.2f
        private val WEARABLE = setOf(Cat.HAT, Cat.GARMENT, Cat.GLASSES)

        /**
         * Pad 0..3 under a tap [dx], [dy] from the board's bottom middle. The pads lie on a surface drawn in slanted 3D: the
         * further back a point is, the higher and the further right it is drawn, so a tap is taken back to the front edge first.
         */
        fun padAt(dx: Float, dy: Float): Int {
            val z = ((dy - SURFACE) / SLANT_Y).coerceIn(0f, SURFACE_DEPTH)
            return ((dx - SLANT_X * z + PAD * 2f) / PAD).toInt().coerceIn(0, 3)
        }

        fun capacity(f: Fixture): Int = if (f.type == FixtureType.PLAY_ROBOT_WORKSHOP) 2 else 1
        fun accepts(f: Fixture, t: Thing): Boolean =
            (f.type == FixtureType.PLAY_ROBOT_WORKSHOP || f.type == FixtureType.PLAY_HELPER_ROBOT) && t.type.cat !in WEARABLE
    }
}
