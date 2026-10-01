package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

/**
 * Sture, the shy and friendly ghost of the attic. He hides, pops up, tickles, swaps things around, sneezes
 * dust, jumps at his own shadow, blows out candles, and plays hide-and-seek with the child's finger: find him
 * three times and there is a sticker. Everything he does is kind. Nobody is ever really frightened.
 *
 * He is an ordinary [Person] (Species.GHOST), so [Life] already makes him wander, wish and giggle. This class adds
 * the plans on top, and only while the attic is on screen. When he drifts to another floor (see [House.shuffle]),
 * he floats back a few seconds after the child arrives.
 */
internal class AtticSture(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    private val place = PlaceId.MANOR_ATTIC

    private var clock = 0f
    private var lastTap = Float.NaN
    private var lastTapAt = -99f
    private var recentTaps = 0
    private var actIn = 8f
    private var hideIn = -1f
    private var tellIn = 6f
    private var hiddenFor = 0f
    private var scareCool = 0f
    private var returnIn = -1f
    private var hintIn = 14f
    private var lastHide = -1
    private var chasing = -1
    private var chaseKind = 0
    private var chaseFor = 0f

    /** Catches beyond the first three, for a present now and then (the first three are in the saved flags). */
    private var extraCatches = 0

    fun find(): Person? = world.people().firstOrNull { it.species == Species.GHOST && it.name == NAME }

    private fun fx(code: Int, x: Float, y: Float, f: Fixture? = null, t: Thing? = null, arg: Int = 0) {
        sim.listener.onFx(Fx.HOUSE, x, y, f, t, HouseFx.pack(code, arg))
    }

    fun keyFound(): Boolean = "manor_key_attic" in world.flags

    /** How many times the child has found him. */
    fun catches(): Int = (1..3).count { "attic_catch_$it" in world.flags } + extraCatches

    // ------------------------------------------------------------------ the plan

    fun tick(dt: Float) {
        val s = find() ?: return
        clock += dt
        scareCool = max(0f, scareCool - dt)
        if (s.place != place) {
            fetch(s, dt)
            return
        }
        returnIn = -1f
        watch(s)
        if (!keyFound() && s.mode == Mode.FREE && !s.held && s.anim.tickle >= 1.5f) popKey(s)
        if (s.mode == Mode.SEATED && hiderOf(s) != null) {
            hiding(s, dt)
            return
        }
        if (s.held || s.mode != Mode.FREE) return
        roam(s, dt)
    }

    /** Drifted to another floor: he floats back up a few seconds after the child comes here. */
    private fun fetch(s: Person, dt: Float) {
        if (s.place == null || s.mode != Mode.FREE || s.held) return
        if (returnIn < 0f) returnIn = 4f + random.nextFloat() * 3f
        returnIn -= dt
        if (returnIn > 0f) return
        returnIn = -1f
        House.moveTo(world, s, place, 3.7f + random.nextFloat() * 0.8f)
        s.anim.hopV = 1.6f
        s.anim.face = Face.LAUGH
        s.anim.faceTime = 1.4f
        fx(AtticCode.RETURN, s.x, s.y - s.h * 0.5f)
    }

    /** A tap on him (the engine makes him giggle) means: «find me!» He slips away to hide, unless he is being tickled. */
    private fun watch(s: Person) {
        val tap = s.anim.lastTap
        if (lastTap.isNaN()) {
            lastTap = tap
            return
        }
        if (tap == lastTap) return
        lastTap = tap
        if (s.mode != Mode.FREE || s.held) return
        if (clock - lastTapAt > 1.2f) recentTaps = 0
        recentTaps++
        lastTapAt = clock
        if (recentTaps < 3 && hideIn < 0f) hideIn = 1.1f
    }

    private fun tickling(s: Person) = s.anim.tickle > 0.3f || recentTaps >= 3 && clock - lastTapAt < 1.4f

    private fun roam(s: Person, dt: Float) {
        if (hideIn >= 0f) {
            hideIn -= dt
            if (hideIn < 0f) {
                if (!tickling(s)) hide(s)
                return
            }
        }
        if (chasing >= 0) {
            chase(s, dt)
        } else {
            actIn -= dt
            if (actIn <= 0f) {
                actIn = 9f + random.nextFloat() * 11f
                act(s)
            }
        }
        // Passing close to a lit lamp, he catches sight of his own shadow.
        if (scareCool <= 0f && s.resting && random.nextFloat() < dt * 0.4f) {
            val lamp = world.fixturesIn(place).firstOrNull { it.on && it.type in SHADOW_MAKERS && abs(it.x - s.x) < 0.24f }
            if (lamp != null) scare(s, 0)
        }
        // The key is still in his sheet: a shy glint now and then.
        if (!keyFound()) {
            hintIn -= dt
            if (hintIn <= 0f) {
                hintIn = 16f + random.nextFloat() * 10f
                fx(AtticCode.KEY_HINT, s.x, s.y - s.h * 0.35f)
            }
        }
    }

    private fun act(s: Person) {
        val visitors = visitors(s)
        val lit = lampsNear(s, 1.6f)
        val things = swappable()
        val options = ArrayList<Pair<Int, Int>>()
        options += 3 to ACT_HIDE
        options += 2 to ACT_SNEEZE
        if (things.size >= 2) options += 2 to ACT_SWAP
        if (visitors.isNotEmpty()) {
            options += 3 to ACT_TICKLE
            options += 2 to ACT_BOO
        }
        if (lit.isNotEmpty()) options += 2 to ACT_BLOW
        options += 3 to ACT_NOTHING
        var roll = random.nextInt(options.sumOf { it.first })
        var pick = ACT_NOTHING
        for ((w, a) in options) {
            roll -= w
            if (roll < 0) {
                pick = a
                break
            }
        }
        when (pick) {
            ACT_HIDE -> hide(s)
            ACT_SNEEZE -> sneeze(s)
            ACT_SWAP -> swap(things)
            ACT_TICKLE, ACT_BOO -> {
                chasing = visitors[random.nextInt(visitors.size)].id
                chaseKind = pick
                chaseFor = 0f
            }
            ACT_BLOW -> blow(lit[random.nextInt(lit.size)])
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ pranks

    /** Folk in the attic who are free to be tickled or startled. */
    private fun visitors(s: Person): List<Person> = world.bodiesIn(place).filterIsInstance<Person>()
        .filter { it.species == Species.FOLK && !it.held && it.mode == Mode.FREE && it.resting && abs(it.x - s.x) < 3f && it.anim.face != Face.SLEEP }

    private fun lampsNear(s: Person, reach: Float): List<Fixture> =
        world.fixturesIn(place).filter { it.on && it.type in CANDLES && abs(it.x - s.x) < reach }

    private fun chase(s: Person, dt: Float) {
        val target = world.bodies[chasing] as? Person
        chaseFor += dt
        if (target == null || target.place != place || target.held || target.mode != Mode.FREE || chaseFor > 12f) {
            stopChase(s)
            return
        }
        if (abs(s.x - target.x) < 0.3f) {
            if (chaseKind == ACT_TICKLE) {
                target.anim.tickle = 1.8f
                target.anim.face = Face.LAUGH
                target.anim.faceTime = 2f
                fx(AtticCode.TICKLE, target.x, target.y - target.h * 0.5f, arg = target.id)
            } else {
                target.anim.hopV = 2.3f
                target.anim.face = Face.OOH
                target.anim.faceTime = 0.6f
                fx(AtticCode.BOO, target.x, target.y - target.h, arg = target.id)
            }
            s.anim.face = Face.LAUGH
            s.anim.faceTime = 1.6f
            s.anim.hopV = 1.5f
            stopChase(s)
            return
        }
        // He glides there: a ghost is quicker than a stroll. (Life would only let him amble.)
        val dir = if (target.x > s.x) 1f else -1f
        val goal = target.x - dir * 0.2f
        s.anim.walkTo = Float.NaN
        if (abs(goal - s.x) > 0.04f && s.resting && s.restOwner == -1) {
            s.x += dir * minOf(abs(goal - s.x), GLIDE * dt)
            s.anim.facing = dir
            val g = sim.groundOf(place, target)
            sim.groundOf(place, s)
            s.ground += (g - s.ground) * minOf(1f, dt * 1.5f)
            s.y = s.ground
        }
    }

    private fun stopChase(s: Person) {
        chasing = -1
        chaseFor = 0f
        s.anim.walkTo = Float.NaN
    }

    private fun sneeze(s: Person) {
        fx(AtticCode.SNEEZE, s.x, s.y - s.h * 0.65f)
        // The dust makes loose things on the floor hop, and whoever stands close sneezes too.
        for (b in world.bodiesIn(place)) {
            if (b is Thing && b.mode == Mode.FREE && !b.held && b.resting && b.restOwner == -1 && abs(b.x - s.x) < 0.35f) {
                b.resting = false
                b.vy = -0.8f
                b.vx += (random.nextFloat() - 0.5f) * 0.4f
            }
        }
        visitors(s).filter { abs(it.x - s.x) < 0.7f }.minByOrNull { abs(it.x - s.x) }
            ?.takeIf { random.nextFloat() < 0.6f }
            ?.let { sim.jokes.pepper(it) }
    }

    /** Two loose things trade places while nobody is looking. */
    private fun swap(things: List<Thing>) {
        val a = things[random.nextInt(things.size)]
        val b = things.filter { it.type != a.type }.takeIf { it.isNotEmpty() }?.let { it[random.nextInt(it.size)] } ?: return
        val x = a.x; val y = a.y; val g = a.ground; val owner = a.restOwner
        a.x = b.x; a.y = b.y; a.ground = b.ground; a.restOwner = b.restOwner
        b.x = x; b.y = y; b.ground = g; b.restOwner = owner
        a.vx = 0f; b.vx = 0f
        a.squashV += 5f
        b.squashV += 5f
        fx(AtticCode.SWAP, a.x, a.y - a.h / 2, null, a, arg = b.id)
        fx(AtticCode.SWAP, b.x, b.y - b.h / 2, null, b, arg = a.id)
    }

    /** Things that lie still on something, which Sture may carry off and swap. */
    private fun swappable(): List<Thing> = world.bodiesIn(place).filterIsInstance<Thing>().filter {
        it.mode == Mode.FREE && !it.held && it.resting && it.inside < 0 && it.restOwner != -2 && it.type != ThingType.GOLDEN_KEY && it.flyT < 0f
    }

    private fun blow(lamp: Fixture) {
        lamp.on = false
        fx(AtticCode.BLOW, lamp.x, lamp.y - lamp.spec.h * 0.8f, lamp, arg = place.indexOf(lamp.id))
        find()?.let {
            it.anim.face = Face.LAUGH
            it.anim.faceTime = 1.2f
        }
    }

    /** Something made him jump: up in the air, a squeak, and then off to hide. Lamps and the clock do it. */
    fun scareNear(x: Float, reach: Float) {
        val s = find() ?: return
        if (s.place != place || abs(s.x - x) > reach) return
        scare(s, 1)
    }

    private fun scare(s: Person, kind: Int) {
        if (s.mode != Mode.FREE || s.held || scareCool > 0f) return
        scareCool = 25f
        s.resting = false
        s.restOwner = -2
        s.vy = -1.9f
        s.vx = (if (random.nextBoolean()) 1f else -1f) * 0.3f
        s.anim.face = Face.OOH
        s.anim.faceTime = 1.2f
        hideIn = 1.0f
        fx(AtticCode.SCARED, s.x, s.y - s.h, arg = kind)
    }

    // ------------------------------------------------------------------ hide and seek

    private fun hiderOf(s: Person): Fixture? = world.fixtures[s.holder]?.takeIf { it.type in HouseAtticRules.HIDERS }

    fun hidingIn(f: Fixture): Boolean {
        val s = find() ?: return false
        return s.mode == Mode.SEATED && s.holder == f.id && f.type in HouseAtticRules.HIDERS
    }

    /** He slips into a sheet, a carton, the trunk or the blanket fort: wherever nobody is. */
    private fun hide(s: Person) {
        stopChase(s)
        val spots = world.fixturesIn(place).filter { f ->
            f.type in HouseAtticRules.HIDERS && !f.open && f.spec.spots.indices.any { world.seatedAt(f, it) == null }
        }
        if (spots.isEmpty()) return
        val choices = spots.filter { place.indexOf(it.id) != lastHide }.ifEmpty { spots }
        val f = choices[random.nextInt(choices.size)]
        val spot = f.spec.spots.indices.first { world.seatedAt(f, it) == null }
        val fromX = s.x
        val fromY = s.y - s.h * 0.5f
        s.anim.walkTo = Float.NaN
        s.anim.face = Face.GRIN
        s.anim.faceTime = 0.5f
        if (!sim.seat(s, f, spot)) return
        lastHide = place.indexOf(f.id)
        f.mode = 2
        f.anim = 1f
        hiddenFor = 0f
        tellIn = 3.5f + random.nextFloat() * 2f
        hideIn = -1f
        fx(AtticCode.HIDE, fromX, fromY, f, arg = place.indexOf(f.id))
    }

    private fun hiding(s: Person, dt: Float) {
        val f = hiderOf(s) ?: return
        hiddenFor += dt
        tellIn -= dt
        if (tellIn <= 0f) {
            tellIn = 5f + random.nextFloat() * 5f
            f.anim = 1f
            fx(AtticCode.TELL, f.x + f.shiftX, f.y - f.spec.h * 0.5f, f, arg = place.indexOf(f.id))
        }
        // He gets tired of waiting and floats out by himself, a little shyly.
        if (hiddenFor > 55f) {
            reveal(s, f)
            f.open = true
            f.timer = HouseAtticRules.REVEAL
            sim.invalidate(place)
            s.anim.face = Face.GRIN
        }
    }

    /** He steps out of his hiding place into the room. */
    private fun reveal(s: Person, f: Fixture) {
        s.mode = Mode.FREE
        s.holder = -1
        s.resting = false
        s.restOwner = -2
        s.ground = f.depth + 0.045f
        s.x = f.x + f.shiftX + (if (random.nextBoolean()) 0.14f else -0.14f)
        s.y = f.y + f.shiftY - 0.02f
        s.vy = -1.5f
        s.vx = if (s.x > f.x) 0.3f else -0.3f
        s.anim.hopV = 1.6f
        if (f.mode == 2) f.mode = 0
        hiddenFor = 0f
        actIn = 7f + random.nextFloat() * 4f
    }

    /** The child lifted the sheet he was under: found him! */
    fun found(f: Fixture) {
        val s = find() ?: return
        reveal(s, f)
        s.anim.face = Face.LAUGH
        s.anim.faceTime = 2.2f
        // Counting: three catches are kept in the saved flags, the rest only for the evening.
        val before = catches()
        if (before < 3) world.flags += "attic_catch_${before + 1}" else extraCatches++
        val total = before + 1
        fx(AtticCode.CAUGHT, s.x, s.y - s.h * 0.6f, f, arg = total.coerceAtMost(999))
        sim.tasks.record(Deed.AT_CATCH, place, ThingType.AT_SHEET_HAT, f.type, Species.GHOST)
        if (total == 1) sim.unlock("attic_ghost")
        if (total == 3) {
            // The third time he laughs so hard that the sticker tumbles out, and the key from his sheet with it.
            sim.egg("attic_sture")
            fx(AtticCode.STICKER, s.x, s.y - s.h, f)
            if (!keyFound()) popKey(s)
        } else if (total > 3 && total % 3 == 0) {
            val gift = world.addThing(ThingType.GIFT, random.nextInt(ThingType.GIFT.variants), place, s.x + 0.06f, s.y - s.h * 0.6f)
            gift.ground = sim.groundOf(place, s) + 0.01f
            gift.vy = -1.7f
            gift.vx = 0.35f
            gift.vrot = 160f
            sim.listener.onSpawn(gift)
        }
    }

    /** Called before the child takes the stairs or the secret door: Sture stays up here, mostly hiding. */
    fun beforeLeaving() {
        val s = find() ?: return
        if (s.place == place && s.mode == Mode.FREE && !s.held && random.nextFloat() < 0.6f) hide(s)
    }

    // ------------------------------------------------------------------ the golden key

    /** The golden key was in his sheet all along; a tickle (or the third catch) shakes it loose. */
    fun popKey(s: Person) {
        if (keyFound()) return
        val key = world.addThing(ThingType.GOLDEN_KEY, 0, place, s.x, s.y - s.h * 0.45f)
        key.ground = sim.groundOf(place, s) + 0.012f
        key.vy = -2.1f
        key.vx = if (random.nextBoolean()) 0.5f else -0.5f
        key.vrot = 260f
        sim.listener.onSpawn(key)
        fx(AtticCode.KEY, key.x, key.y, null, key)
        sim.flag("manor_key_attic")
        s.anim.face = Face.LAUGH
        s.anim.faceTime = 2f
    }

    private companion object {
        const val NAME = "Sture"
        /** How fast he glides to someone he wants to tickle, in scene units a second. */
        const val GLIDE = 0.4f
        const val ACT_NOTHING = 0
        const val ACT_HIDE = 1
        const val ACT_SNEEZE = 2
        const val ACT_SWAP = 3
        const val ACT_TICKLE = 4
        const val ACT_BOO = 5
        const val ACT_BLOW = 6

        /** What throws a shadow he can be frightened of. */
        val SHADOW_MAKERS = setOf(
            FixtureType.AT_CANDELABRA, FixtureType.AT_LANTERN, FixtureType.AT_CHANDELIER, FixtureType.AT_SCONCE, FixtureType.AT_ARMILLARY,
        )

        /** What he can blow out. */
        val CANDLES = setOf(FixtureType.AT_CANDELABRA, FixtureType.AT_LANTERN, FixtureType.AT_SCONCE, FixtureType.AT_CHANDELIER)
    }
}
