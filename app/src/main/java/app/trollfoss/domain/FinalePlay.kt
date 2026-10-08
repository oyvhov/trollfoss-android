package app.trollfoss.domain

import kotlin.math.abs
import kotlin.random.Random

/** Level ten stays a toy box: two rides, an unhurried hiding game and a short, repeatable party. */
class FinalePlay(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    private class Hiding(var hole: Int, var found: Int = 0)
    private val hiding = java.util.WeakHashMap<Fixture, Hiding>()
    private val parties = java.util.WeakHashMap<Fixture, Float>()

    fun tap(f: Fixture, dx: Float = 0f, dy: Float = ASSIST_Y): Boolean {
        if (world.fixtures[f.id] !== f) return false
        when (f.type) {
            FixtureType.PLAY_HIDE_TROLL -> {
                val game = hiding[f]
                if (game == null) {
                    val fresh = Hiding(random.nextInt(3))
                    hiding[f] = fresh; f.angle = fresh.hole + 1f; f.on = true
                    sim.listener.onFx(Fx.SQUEAK, f.x, f.top, f)
                } else if (dy <= ASSIST_Y || holeAt(dx) == game.hole) {
                    game.found++; f.anim = 1f
                    sim.listener.onFx(Fx.BOING, f.x + (game.hole - 1) * HOLE_STEP, f.top, f)
                    cheer(f)
                    if (game.found == 3) {
                        hiding.remove(f); f.angle = 0f; f.on = false
                        sim.listener.onFx(Fx.CONFETTI, f.x, f.top, f)
                        sim.firstTime(First.HIDE_TROLL, f.x, f.top)
                    } else {
                        game.hole = (game.hole + 1 + random.nextInt(2)) % 3
                        f.angle = game.hole + 1f
                    }
                } else {
                    // No lost progress and no clock. The eyes keep showing where the troll is.
                    sim.listener.onFx(Fx.TICK, f.x, f.top, f)
                }
            }
            FixtureType.PLAY_TROLL_PARTY -> {
                if (parties.remove(f) != null) stopParty(f)
                else {
                    parties[f] = 0f; f.on = true; f.anim = 1f
                    sim.listener.onFx(Fx.DISCO, f.x, f.top, f, param = 1)
                    sim.listener.onFx(Fx.CONFETTI, f.x, f.top, f)
                    cheer(f); sim.firstTime(First.TROLL_PARTY, f.x, f.top)
                }
            }
            else -> return false // Rides go through the shared vehicle arrows.
        }
        return true
    }

    fun drop(f: Fixture, t: Thing): Boolean {
        if (!accepts(f, t) || t.held || world.fixtures[f.id] !== f || world.inMachine(f).isNotEmpty()) return false
        t.mode = Mode.INSIDE; t.holder = f.id; t.inside = -1
        t.vx = 0f; t.vy = 0f; t.rot = 0f; t.resting = false; t.restOwner = -2
        cargoPoint(f, t)
        sim.listener.onFx(Fx.INTO, t.x, t.y, f, t)
        return true
    }

    fun step(f: Fixture, dt: Float): Boolean {
        when (f.type) {
            FixtureType.PLAY_DRAGON_CART, FixtureType.PLAY_AIRSHIP -> {
                // Move first, then put the cargo on the same deck as the passengers.
                val x = f.x; val y = f.y
                sim.vehicles.step(f.place, f, dt)
                if (abs(x - f.x) + abs(y - f.y) > 0.00001f) {
                    sim.firstTime(if (f.type == FixtureType.PLAY_AIRSHIP) First.AIRSHIP else First.DRAGON_CART, f.x, f.top)
                    f.timer += dt
                    if (f.type == FixtureType.PLAY_DRAGON_CART && f.timer >= 2.8f) {
                        f.timer = 0f
                        sim.listener.onFx(Fx.BUBBLES, f.x + 0.26f, f.y - 0.2f, f)
                    }
                }
                if (f.type == FixtureType.PLAY_AIRSHIP) world.inMachine(f).forEach { cargoPoint(f, it) }
            }
            FixtureType.PLAY_HIDE_TROLL -> if (!hiding.containsKey(f)) { f.on = false; f.angle = 0f }
            FixtureType.PLAY_TROLL_PARTY -> {
                val old = parties[f]
                if (old == null) { f.on = false; return true }
                if (f.lift > 0f) return true
                val next = old + dt
                if (next >= PARTY_TIME) { parties.remove(f); stopParty(f) }
                else {
                    parties[f] = next
                    if ((old / 3f).toInt() != (next / 3f).toInt()) {
                        sim.listener.onFx(Fx.CONFETTI, f.x, f.top, f); cheer(f)
                    }
                }
            }
            else -> return false
        }
        return true
    }

    fun dances(p: Person): Boolean {
        if (p.place == null) return false
        return free(p) && parties.keys.any { world.fixtures[it.id] === it && it.on && nearby(p, it) }
    }

    private fun nearby(p: Person, f: Fixture) = p.place == f.place && abs(p.x - f.x) < 1.3f &&
        Decor.roomAt(f.place, p.x) == Decor.roomAt(f.place, f.x)

    private fun free(p: Person) = p.mode == Mode.FREE && !p.held && p.anim.pose == Pose.STAND

    private fun cheer(f: Fixture) {
        world.people().filter { free(it) && nearby(it, f) }.forEach {
            it.anim.face = Face.LAUGH; it.anim.faceTime = 1.8f; it.anim.wave = 1f
        }
    }

    private fun stopParty(f: Fixture) {
        f.on = false
        sim.listener.onFx(Fx.DISCO, f.x, f.top, f, param = 0)
    }

    private fun cargoPoint(f: Fixture, t: Thing) {
        t.x = f.x + CARGO_X; t.y = f.y + CARGO_Y; t.ground = f.place.floor
    }

    companion object {
        val TYPES = setOf(FixtureType.PLAY_DRAGON_CART, FixtureType.PLAY_AIRSHIP, FixtureType.PLAY_HIDE_TROLL, FixtureType.PLAY_TROLL_PARTY)
        val DIRECT = setOf(FixtureType.PLAY_HIDE_TROLL, FixtureType.PLAY_TROLL_PARTY)
        const val HOLE_STEP = 0.22f
        const val ASSIST_Y = -0.19f
        const val PARTY_TIME = 9f
        const val CARGO_X = 0.23f
        const val CARGO_Y = -0.095f
        fun holeAt(dx: Float): Int = ((dx + HOLE_STEP * 1.5f) / HOLE_STEP).toInt().coerceIn(0, 2)
        fun accepts(f: Fixture, t: Thing): Boolean = f.type == FixtureType.PLAY_AIRSHIP && t.type.cat !in setOf(Cat.HAT, Cat.GARMENT, Cat.GLASSES)
    }
}
