package app.trollfoss.domain

import kotlin.math.sin

/**
 * The boiler room's rules: three valves on the pipes. Each one that is turned open hisses and clangs; when
 * all three are open at once the boiler shudders, the whole house rumbles and a steam whistle blows, and then
 * a safety valve lets the pressure go and the three valves spring shut again, one after the other.
 */
internal class CellarBoiler(private val c: CellarCtx) : CellarPart {
    override fun tap(f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.CE_VALVE -> {
            turn(f)
            true
        }
        FixtureType.CE_BOILER -> {
            c.fx(CellarCode.BOILER_TAP, f.x, f.y - 0.3f, f)
            true
        }
        else -> false
    }

    override fun step(f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.CE_VALVE -> {
                // The wheel turns half a round when the valve opens, and back when it shuts.
                val target = f.mode * HALF_TURN
                f.angle += (target - f.angle) * kotlin.math.min(1f, dt * 9f)
            }
            FixtureType.CE_BOILER -> {
                f.on = true
                val open = openCount()
                // The gauge follows the number of open valves, and swings into the top while the boiler rumbles.
                val target = if (f.timer > 0f) 1.12f else open / 3f
                f.angle += (target - f.angle) * kotlin.math.min(1f, dt * 2.5f)
                if (f.timer > 0f) {
                    f.timer -= dt
                    f.bob = sin(c.clock * 55f) * 0.0018f
                    if (f.timer <= 0f) release(f)
                } else {
                    f.bob = 0f
                }
            }
            else -> Unit
        }
    }

    private fun openCount(): Int = CellarIx.VALVES.count { c.fixture(it)?.mode == 1 }

    private fun turn(valve: Fixture) {
        valve.mode = 1 - valve.mode
        val which = CellarIx.VALVES.indexOf(c.indexOf(valve))
        c.fx(CellarCode.VALVE, valve.x, valve.y - 0.15f, valve, arg = which * 2 + valve.mode)
        if (valve.mode == 1 && openCount() == CellarIx.VALVES.size) rumble()
    }

    /** All three valves are open. */
    private fun rumble() {
        val boiler = c.fixture(CellarIx.BOILER) ?: return
        if (boiler.timer > 0f) return
        boiler.timer = RUMBLE_SECONDS
        // The whole house rumbles (the engine shakes the screen and everyone yelps and laughs).
        c.listener.onFx(Fx.QUAKE, boiler.x, boiler.y, boiler)
        c.fx(CellarCode.VALVES_ALL, boiler.x, boiler.y - 0.3f, boiler)
        c.sim.tasks.record(Deed.CE_VALVES, c.place, null, FixtureType.CE_VALVE)
    }

    /** The safety valve lets go, and the three valves spring shut with a clang each. */
    private fun release(boiler: Fixture) {
        c.fx(CellarCode.RELEASE, boiler.x, boiler.y - 0.5f, boiler)
        for ((k, index) in CellarIx.VALVES.withIndex()) {
            val valve = c.fixture(index) ?: continue
            c.after(0.3f * (k + 1)) {
                valve.mode = 0
                valve.anim = 1f
                c.fx(CellarCode.VALVE, valve.x, valve.y - 0.15f, valve, arg = k * 2)
            }
        }
    }

    companion object {
        const val RUMBLE_SECONDS = 4.2f
        const val HALF_TURN = 3.1416f
    }
}
