package app.trollfoss.domain

import kotlin.math.max

/**
 * The workshop's rules: Rolf's charging station, the mouse in the wall (who loves brown cheese) and the
 * saw horse that saws planks for the workbench.
 */
internal class CellarWork(private val c: CellarCtx) : CellarPart {
    override fun tap(f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.CE_CHARGER -> {
            val who = c.world.seatedAt(f, 0)
            // Whoever stands in it charges again; an empty station only fizzles.
            if (who != null) f.mode = 0
            c.fx(CellarCode.CHARGE, f.x, f.y - 0.2f, f, arg = if (who == null) 2 else if (who.species == Species.ROBOT) 1 else 0)
            true
        }
        FixtureType.CE_MOUSE_HOLE -> {
            f.timer = MOUSE_PEEK
            c.fx(CellarCode.MOUSE_PEEK, f.x, f.y - 0.04f, f)
            true
        }
        FixtureType.CE_SAW -> {
            if (!f.on) {
                f.on = true
                f.timer = SAW_SECONDS
                f.angle = 0f
                c.fx(CellarCode.SAW_START, f.x, f.y - 0.1f, f)
            }
            true
        }
        else -> false
    }

    override fun step(f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.CE_CHARGER -> charge(f, dt)
            FixtureType.CE_MOUSE_HOLE -> f.timer = max(0f, f.timer - dt)
            FixtureType.CE_SAW -> if (f.on) {
                f.timer -= dt
                f.angle = SAW_SECONDS - f.timer
                if (f.timer <= 0f) {
                    f.on = false
                    f.angle = 0f
                    f.count++
                    c.spawn(ThingType.PLANK, 0, f.x + 0.15f, f.y - 0.1f, f, 0.5f, -1.1f, 90f)
                    c.limit(ThingType.PLANK, MAX_PLANKS)
                    c.fx(CellarCode.SAW_DONE, f.x + 0.1f, f.y - 0.1f, f)
                }
            }
            else -> Unit
        }
    }

    /** Whoever stands in the station charges up: the battery fills, then a ding, sparkles and a hop. */
    private fun charge(f: Fixture, dt: Float) {
        val who = c.world.seatedAt(f, 0)
        if (who == null) {
            if (f.on || f.mode != 0 || f.timer != 0f) {
                f.on = false
                f.mode = 0
                f.timer = 0f
            }
            return
        }
        if (f.mode != 0) return
        if (!f.on) {
            f.on = true
            f.timer = 0f
            c.fx(CellarCode.CHARGE, f.x, f.y - 0.2f, f, arg = if (who.species == Species.ROBOT) 1 else 0)
        }
        f.timer += dt
        if (f.timer >= CHARGE_SECONDS) {
            f.on = false
            f.mode = 1
            f.count++
            who.anim.sparkle = 1f
            who.anim.hopV = 1.7f
            who.anim.face = Face.GRIN
            who.anim.faceTime = 1.6f
            c.fx(CellarCode.CHARGED, who.x, who.y - who.h, f)
        }
    }

    override fun drop(f: Fixture, t: Thing): Boolean {
        if (f.type != FixtureType.CE_MOUSE_HOLE || t.type != ThingType.BROWN_CHEESE) return false
        c.sim.removeThing(t, quiet = true)
        f.count++
        f.timer = MOUSE_PARTY
        // Every third piece buys a coin: the mouse has a little hoard.
        val coin = f.count % 3 == 0
        if (coin) c.spawn(ThingType.COIN, 0, f.x + 0.06f, f.y - 0.02f, f, 0.5f, -1.4f, 200f)
        c.fx(CellarCode.MOUSE_CHEESE, f.x, f.y - 0.04f, f, arg = if (coin) 1 else 0)
        return true
    }

    companion object {
        const val SAW_SECONDS = 1.6f
        const val CHARGE_SECONDS = 2.8f
        const val MOUSE_PEEK = 2.4f
        const val MOUSE_PARTY = 3.2f
        const val MAX_PLANKS = 8
    }
}
