package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Rolf, the robot butler. He is polite, takes everything literally and trips over his own carefulness.
 * Now and then he sets out on an errand: he tidies a thing from the floor back to where it belongs, or he
 * serves a snack to somebody who sits down. When something goes wrong he bows too deep, bumps his head and
 * says sorry. When somebody taps him he answers in his own way (and once in a while he taps something back,
 * because that is what he was told). He lives on the floor he is on, and the floor's rules call [tick].
 */
internal class GroundRolf(private val sim: Sim, private val random: Random, private val rules: GroundRules) {
    private val world get() = sim.world
    private val place = PlaceId.MANOR_GROUND

    private enum class State { IDLE, TO_THING, CARRY, TO_PERSON, SERVE, BUMBLE }

    private var state = State.IDLE
    private var idle = 14f
    private var timer = 0f
    private var rolfId = -1
    private var scanClock = 0f
    private var seenTap = Float.NaN
    private var reaction = 0

    private var thing: Thing? = null
    private var person: Person? = null
    private var destX = 0f
    private var destY = 0f
    private var destGround = 0f
    private var clonked = false
    private var errands = 0

    private fun find(dt: Float): Person? {
        (world.bodies[rolfId] as? Person)?.let { if (it.species == Species.ROBOT) return it }
        scanClock -= dt
        if (scanClock > 0f) return null
        scanClock = 2f
        rolfId = world.people().firstOrNull { it.species == Species.ROBOT && it.name == "Rolf" }?.id ?: -1
        return world.bodies[rolfId] as? Person
    }

    /** At dusk and dawn he says «click» to the lamps. */
    fun lampsSwitched() {
        val me = find(0f) ?: return
        if (me.place != place) return
        rules.emit(GroundCode.ROLF, 6, me.x, me.y - me.h, null)
        me.anim.face = Face.GRIN
        me.anim.faceTime = 1.2f
        me.anim.wave = 1f
    }

    fun tick(dt: Float) {
        val me = find(dt) ?: return
        val a = me.anim
        if (me.place != place || me.held || me.mode != Mode.FREE) {
            abandon(me)
            return
        }
        // Somebody tapped him.
        if (seenTap.isNaN()) seenTap = a.lastTap
        if (a.lastTap != seenTap) {
            seenTap = a.lastTap
            reactToTap(me)
        }
        // A bow comes back up by itself.
        if (state != State.BUMBLE && abs(a.tilt) > 0.5f) a.tilt *= (1f - min(1f, dt * 5f))

        when (state) {
            State.IDLE -> {
                idle -= dt
                if (idle <= 0f && a.walkTo.isNaN() && a.pose != Pose.HELD) chooseErrand(me)
            }
            State.TO_THING -> {
                val t = thing
                if (t == null || t.place != place || t.mode != Mode.FREE || t.held || !t.resting) {
                    giveUp(me)
                } else if (walk(me, t.x, sim.groundOf(place, t), dt)) {
                    pickUp(me, t)
                }
            }
            State.CARRY -> {
                val t = world.worn(me, Slot.HAND)
                if (t == null || t !== thing) {
                    giveUp(me)
                } else if (walk(me, destX, destGround, dt)) {
                    putDown(me, t)
                }
            }
            State.TO_PERSON -> {
                val p = person
                if (p == null || p.mode != Mode.SEATED || p.place != place) {
                    giveUp(me)
                } else {
                    val side = if (me.x < p.x) -0.19f else 0.19f
                    if (walk(me, p.x + side, sim.groundOf(place, me).coerceAtLeast(place.floor - 0.03f), dt)) startServing(me, p)
                }
            }
            State.SERVE -> {
                a.still = 0f
                timer -= dt
                // A deep bow while the cloche lifts.
                a.tilt = 18f * sin(min(1f, (SERVE_SECONDS - timer) / SERVE_SECONDS) * 3.1416f)
                if (timer <= 0f) finishServing(me)
            }
            State.BUMBLE -> {
                a.still = 0f
                timer -= dt
                val phase = BUMBLE_SECONDS - timer
                a.tilt = if (phase < 0.9f) 32f * sin(phase / 0.9f * 1.5708f) else 32f * (1f - min(1f, (phase - 0.9f) / 0.9f))
                if (!clonked && phase >= 0.9f) {
                    clonked = true
                    rules.emit(GroundCode.ROLF, 2, me.x + 0.05f * a.facing, me.y - me.h * 0.8f, null)
                    me.squashV -= 5f
                    a.face = Face.DIZZY
                    a.faceTime = 0.9f
                    for (o in world.bodiesIn(place)) if (o is Person && o !== me && !o.held && abs(o.x - me.x) < 0.8f && o.anim.face != Face.SLEEP) {
                        o.anim.face = Face.LAUGH
                        o.anim.faceTime = 1.5f
                    }
                }
                if (timer <= 0f) {
                    a.tilt = 0f
                    a.face = Face.HAPPY
                    state = State.IDLE
                    idle = 16f + random.nextFloat() * 18f
                    rules.emit(GroundCode.ROLF, 0, me.x, me.y - me.h, null)
                }
            }
        }
    }

    // ------------------------------------------------------------------ errands

    private fun chooseErrand(me: Person) {
        val loose = tidyCandidate(me)
        val diner = servable(me)
        val wantsServe = diner != null && (loose == null || random.nextBoolean())
        when {
            wantsServe && diner != null -> {
                person = diner
                state = State.TO_PERSON
                me.anim.walkTo = diner.x
            }
            loose != null -> {
                thing = loose
                state = State.TO_THING
            }
            else -> idle = 10f + random.nextFloat() * 12f
        }
    }

    /** The loose thing on the floor nearest to him, that is not where it belongs. */
    private fun tidyCandidate(me: Person): Thing? {
        var best: Thing? = null
        var bestD = 99f
        for (b in world.bodiesIn(place)) {
            val t = b as? Thing ?: continue
            if (t.mode != Mode.FREE || !t.resting || t.held || t.inside >= 0 || t.restOwner != -1) continue
            if (t.type == ThingType.GOLDEN_KEY || t.type == ThingType.GR_TRAY) continue
            if (atHome(t)) continue
            val d = abs(t.x - me.x)
            if (d < bestD) {
                bestD = d
                best = t
            }
        }
        return best
    }

    private fun atHome(t: Thing): Boolean {
        if (t.homePlace != place) return false
        if (t.homeOwner < 0) return abs(t.x - t.homeDx) < 0.06f && abs(t.y - t.homeDy) < 0.05f
        val f = world.fixtures[t.homeOwner] ?: return false
        return abs(t.x - (f.x + t.homeDx)) < 0.05f && abs(t.y - (f.y + t.homeDy)) < 0.03f
    }

    /** Somebody sitting down with nothing in their hands. */
    private fun servable(me: Person): Person? = world.bodiesIn(place).asSequence()
        .filterIsInstance<Person>()
        .filter { it.species == Species.FOLK && it.mode == Mode.SEATED && !it.held && world.worn(it, Slot.HAND) == null && it.anim.face != Face.SLEEP }
        .filter { world.fixtures[it.holder] != null && abs(it.x - me.x) < 6f }
        .toList()
        .let { list -> if (list.isEmpty()) null else list[random.nextInt(list.size)] }

    private fun pickUp(me: Person, t: Thing) {
        sim.give(me, t, Part.HAND)
        destinationFor(t)
        rules.emit(GroundCode.ROLF, 3, t.x, t.y - t.h, null, t)
        me.anim.face = Face.GRIN
        me.anim.faceTime = 1f
        state = State.CARRY
    }

    private fun destinationFor(t: Thing) {
        if (t.homePlace == place) {
            if (t.homeOwner >= 0) {
                val f = world.fixtures[t.homeOwner]
                if (f != null) {
                    destX = f.x + t.homeDx
                    destY = f.y + t.homeDy
                    destGround = f.depth + 0.03f
                    return
                }
            } else {
                destX = t.homeDx
                destY = t.homeDy
                destGround = t.homeDy
                return
            }
        }
        // No home: a tidy table will do.
        val tables = world.fixturesIn(place).filter { it.type in TIDY_TOPS }
        val f = tables.minByOrNull { abs(it.x - t.x) + random.nextFloat() * 0.6f }
        if (f != null) {
            val top = f.spec.surfaces.lastOrNull { !it.interior }?.dy ?: -f.spec.h
            destX = f.x + (random.nextFloat() - 0.5f) * 0.2f
            destY = f.y + top
            destGround = f.depth + 0.03f
        } else {
            destX = t.x + 0.5f
            destY = place.floor
            destGround = place.floor
        }
    }

    private fun putDown(me: Person, t: Thing) {
        t.mode = Mode.FREE
        t.holder = -1
        t.place = place
        t.x = destX
        t.y = destY - 0.05f
        t.ground = destGround
        t.vx = 0f
        t.vy = 0.3f
        t.resting = false
        t.restOwner = -2
        t.inside = -1
        t.held = false
        rules.emit(GroundCode.ROLF, 4, destX, destY - 0.03f, null, t)
        me.anim.face = Face.GRIN
        me.anim.faceTime = 1f
        me.anim.hopV = 1.2f
        thing = null
        errands++
        finish(me)
    }

    private fun startServing(me: Person, p: Person) {
        val tray = world.addThing(ThingType.GR_TRAY, 0, place, me.x, me.y)
        sim.give(me, tray, Part.HAND)
        timer = SERVE_SECONDS
        state = State.SERVE
        me.anim.facing = if (p.x > me.x) 1f else -1f
        rules.emit(GroundCode.ROLF, 5, me.x, me.y - me.h, null)
    }

    private fun finishServing(me: Person) {
        val p = person
        world.worn(me, Slot.HAND)?.let { if (it.type == ThingType.GR_TRAY) sim.removeThing(it, quiet = true) }
        if (p != null && p.mode == Mode.SEATED && world.worn(p, Slot.HAND) == null) {
            val seat = world.fixtures[p.holder]
            val food = foodFor(seat?.type)
            val t = world.addThing(food.first, food.second, place, p.x, p.y)
            sim.listener.onSpawn(t)
            sim.give(p, t, Part.HAND)
            rules.emit(GroundCode.ROLF, 1, p.x, p.y - p.h * 0.9f, null, t)
            p.anim.face = Face.GRIN
            p.anim.faceTime = 1.5f
            p.anim.cheer = 1.2f
            me.anim.face = Face.GRIN
            me.anim.faceTime = 1.2f
        }
        person = null
        me.anim.tilt = 0f
        errands++
        finish(me)
    }

    private fun foodFor(seat: FixtureType?): Pair<ThingType, Int> = when (seat) {
        FixtureType.GR_CHAIR_ROW, FixtureType.GR_DINING_CHAIR -> listOf(ThingType.PIZZA to 0, ThingType.WAFFLE to 0, ThingType.BREAD to 0, ThingType.CAKE to 0)
        FixtureType.GR_SOFA -> listOf(ThingType.POPCORN to 0, ThingType.COCOA to 0, ThingType.COOKIE to 0)
        FixtureType.GR_STOOLS -> listOf(ThingType.SMOOTHIE to random.nextInt(8), ThingType.JUICE to 0, ThingType.CUPCAKE to random.nextInt(4), ThingType.ICE_CREAM to random.nextInt(5))
        FixtureType.GR_WINGCHAIR, FixtureType.GR_LADDER -> listOf(ThingType.COCOA to 0, ThingType.COOKIE to 0, ThingType.BUN to 0)
        else -> listOf(ThingType.JUICE to 0, ThingType.LOLLIPOP to random.nextInt(4), ThingType.WAFFLE to 0)
    }.let { it[random.nextInt(it.size)] }

    /** After an errand: sometimes he gets it wrong and bumps his head. */
    private fun finish(me: Person) {
        me.anim.walkTo = Float.NaN
        if (random.nextFloat() < 0.38f) {
            bumble(me)
        } else {
            state = State.IDLE
            idle = 18f + random.nextFloat() * 22f
        }
    }

    private fun bumble(me: Person) {
        state = State.BUMBLE
        timer = BUMBLE_SECONDS
        clonked = false
        me.anim.walkTo = Float.NaN
    }

    /** Whatever he was doing, he stops: somebody picked him up, or he went to another floor. */
    private fun abandon(me: Person) {
        thing = null
        person = null
        world.worn(me, Slot.HAND)?.let { if (it.type == ThingType.GR_TRAY) sim.removeThing(it, quiet = true) }
        me.anim.tilt = 0f
        state = State.IDLE
        idle = 10f
    }

    private fun giveUp(me: Person) {
        thing = null
        person = null
        world.worn(me, Slot.HAND)?.let { if (it.type == ThingType.GR_TRAY) sim.removeThing(it, quiet = true) }
        me.anim.walkTo = Float.NaN
        state = State.IDLE
        idle = 6f + random.nextFloat() * 6f
    }

    // ------------------------------------------------------------------ taps

    private fun reactToTap(me: Person) {
        val a = me.anim
        when (reaction++ % 5) {
            0 -> {
                a.face = Face.GRIN
                a.faceTime = 1.1f
                a.wave = 1.2f
                rules.emit(GroundCode.ROLF, 0, me.x, me.y - me.h, null)
            }
            1 -> {
                // «Press», he was told, so he presses the nearest thing there is to press.
                a.face = Face.HAPPY
                a.faceTime = 0.8f
                rules.emit(GroundCode.ROLF, 0, me.x, me.y - me.h, null)
                val target = world.fixturesIn(place).filter { it.type in PRESSABLE }.minByOrNull { abs(it.x - me.x) }
                if (target != null) sim.tap(place, target, 0f, -target.spec.h / 2f)
            }
            2 -> {
                a.face = Face.WOW
                a.faceTime = 1f
                a.hopV = 1.6f
                rules.emit(GroundCode.ROLF, 1, me.x, me.y - me.h, null)
            }
            3 -> if (state == State.IDLE) bumble(me) else rules.emit(GroundCode.ROLF, 0, me.x, me.y - me.h, null)
            else -> {
                if (state == State.IDLE) idle = 0f
                a.wave = 1.2f
                rules.emit(GroundCode.ROLF, 0, me.x, me.y - me.h, null)
            }
        }
    }

    // ------------------------------------------------------------------ walking

    /** Walks one step towards a point on the floor; true when he is there. */
    private fun walk(me: Person, x: Float, ground: Float, dt: Float): Boolean {
        val a = me.anim
        sim.groundOf(place, me)
        val dx = x - me.x
        val dg = ground - me.ground
        val d = hypot(dx, dg * 1.6f)
        a.still = 0f
        if (d < 0.015f) {
            a.walkTo = Float.NaN
            return true
        }
        val step = min(d, SPEED * dt)
        me.x += dx / d * step
        me.ground += dg / d * step
        me.y = me.ground
        if (abs(dx) > 0.004f) a.facing = if (dx < 0f) -1f else 1f
        a.walkPhase += step / (me.h * 0.18f)
        a.walkTo = x
        a.walkGround = ground
        return false
    }

    private companion object {
        const val SPEED = 0.2f
        const val SERVE_SECONDS = 1.4f
        const val BUMBLE_SECONDS = 2.2f

        /** Where a thing with no home is put: the tops of tables. */
        val TIDY_TOPS = setOf(FixtureType.GR_COFFEE_TABLE, FixtureType.GR_SIDEBOARD, FixtureType.GR_ISLAND, FixtureType.GR_DINING_TABLE, FixtureType.GR_DESK)

        /** What he presses when he is pressed. */
        val PRESSABLE = setOf(
            FixtureType.GR_CHANDELIER, FixtureType.GR_CLOCK, FixtureType.GR_GLOBE, FixtureType.GR_BELL, FixtureType.GR_ARMOUR,
            FixtureType.GR_PORTRAIT, FixtureType.GR_LECTERN, FixtureType.GR_BUST,
        )
    }
}
