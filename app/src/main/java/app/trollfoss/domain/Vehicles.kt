package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.sin

/** Vehicles share real positions, passengers and controls, including after a trip through storage. */
class Vehicles(private val sim: Sim) {
    private val world get() = sim.world

    fun drive(f: Fixture, direction: Int) {
        if (!controllable(f) || world.fixtures[f.id] !== f) return
        f.mode = direction.coerceIn(-1, 1)
        f.angleV = 0f
        f.on = f.mode != 0
        f.shiftX = 0f
        f.shiftY = 0f
        if (f.on) sim.listener.onFx(if (f.type == FixtureType.BOAT) Fx.TOOT else Fx.VROOM, f.x, f.top, f, param = 2)
    }

    /** The submarine can rise and dive; a new arrow replaces the previous direction. */
    fun dive(f: Fixture, direction: Int) {
        if (f.type != FixtureType.SUBMARINE || world.fixtures[f.id] !== f) return
        drive(f, 0)
        f.angleV = direction.coerceIn(-1, 1).toFloat()
        f.on = f.angleV != 0f
        if (f.on) sim.listener.onFx(Fx.VROOM, f.x, f.top, f, param = 2)
    }

    fun step(place: PlaceId, f: Fixture, dt: Float) {
        if (!f.on || f.lift > 0f) {
            f.bob = if (f.type == FixtureType.BOAT) sin(sim.time * 1.7f + f.id) * 0.007f else 0f
            return
        }
        val speed = when (f.type) {
            FixtureType.BOAT -> 0.36f
            FixtureType.SUBMARINE -> 0.43f
            FixtureType.BUMPER_CAR -> 0.65f
            else -> 0.55f
        }
        val at = sim.clampFixture(place, f, f.x + f.mode * speed * dt,
            f.y + (if (f.type == FixtureType.SUBMARINE) f.angleV * 0.28f * dt else 0f))
        val dx = at[0] - f.x
        val dy = at[1] - f.y
        if (abs(dx) + abs(dy) < 0.00001f) { drive(f, 0); return }
        sim.moveFixture(place, f, at[0], at[1])
        f.angle = if (f.type == FixtureType.SUBMARINE) f.mode.toFloat() else f.angle + dx
        f.bob = sin(sim.time * (if (f.type == FixtureType.BOAT) 1.7f else 30f)) * 0.003f
        // Only vehicles on the floor nudge the scenery. Floating boats and submarines sail past it.
        if (f.type != FixtureType.TRACTOR && f.type != FixtureType.BUMPER_CAR) return
        for (other in world.fixturesIn(place).toList()) {
            if (other === f || other.host >= 0 || other.spec.wall || abs(other.depth - f.depth) > 0.07f) continue
            if (abs(other.x - f.x) >= (other.spec.w + f.spec.w) / 2f) continue
            if (f.type == FixtureType.BUMPER_CAR && dx * (other.x - f.x) <= 0f) continue
            other.anim = 1f
            if (f.type == FixtureType.BUMPER_CAR) {
                // Back off before turning, so two parked cars cannot stick together.
                sim.moveFixture(place, f, f.x - dx, f.y)
                f.mode = -f.mode
                if (other.type == FixtureType.BUMPER_CAR && other.on) other.mode = -other.mode
            } else if (sim.movable(other)) {
                val pushed = sim.clampFixture(place, other, other.x + dx * 2f, other.y)
                sim.moveFixture(place, other, pushed[0], pushed[1])
            }
            if (sim.time - f.bumpTime > 0.4f) {
                f.bumpTime = sim.time
                sim.listener.onFx(Fx.BUMP, other.x, other.top, f)
            }
            if (f.type == FixtureType.BUMPER_CAR) break
        }
        for (b in world.bodiesIn(place)) {
            if (b.mode != Mode.FREE || b.held || b.inside >= 0 || b.restOwner == f.id || b.cool > 0f) continue
            if (abs(b.x - f.x) > f.spec.w / 2f + b.w / 2f || abs(b.y - f.y) > 0.09f) continue
            b.resting = false; b.restOwner = -2; b.vx = f.mode * 0.9f; b.vy = -0.9f; b.cool = 0.8f
            sim.listener.onFx(Fx.BUMP, b.x, b.y - b.h / 2f, f, param = if (b is Person) b.id else 0)
        }
    }

    companion object {
        fun controllable(f: Fixture): Boolean = f.type in TYPES
        private val TYPES = setOf(FixtureType.TRACTOR, FixtureType.BOAT, FixtureType.SUBMARINE, FixtureType.BUMPER_CAR)
    }
}
