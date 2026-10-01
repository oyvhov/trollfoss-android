package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * The pool's rules. The water itself belongs to the blueprint (figures and things float in it); here are the
 * springboard that launches whoever stands on it, the slide, the rubber duck with two seats that drifts about,
 * a tap on the water that sends ripples, the sauna bucket, the shower and the lifebuoy.
 */
internal class CellarPool(private val c: CellarCtx) : CellarPart {
    override fun tap(f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.CE_DIVING_BOARD -> {
            dive(f)
            true
        }
        FixtureType.CE_POOL_SLIDE -> {
            go(f)
            true
        }
        FixtureType.CE_POOL_FLOAT -> {
            // Pushed from the side you tap, and a squeak.
            f.angleV = if (dx < 0f) 0.32f else -0.32f
            c.fx(CellarCode.FLOAT_PUSH, f.x + f.shiftX, f.y - 0.1f, f)
            true
        }
        FixtureType.CE_POOL_WATER -> {
            ripple(f.x + dx)
            c.fx(CellarCode.SPLASH_TAP, f.x + dx, CellarFloor.POOL_LINE, f)
            true
        }
        FixtureType.CE_SAUNA_BUCKET -> {
            val sauna = c.fixture(CellarIx.SAUNA)
            if (sauna != null) {
                val inside = (0 until sauna.spec.spots.size).count { c.world.seatedAt(sauna, it) != null }
                c.fx(CellarCode.LADLE, sauna.x, sauna.top, sauna, arg = inside)
            }
            true
        }
        FixtureType.CE_SHOWER -> {
            f.on = !f.on
            f.timer = if (f.on) SHOWER_SECONDS else 0f
            c.fx(CellarCode.SHOWER, f.x, f.y - 0.3f, f, arg = if (f.on) 1 else 0)
            true
        }
        FixtureType.CE_LIFEBUOY -> {
            f.angleV += 7f
            c.fx(CellarCode.LIFEBUOY, f.x, f.y - 0.06f, f)
            true
        }
        else -> false
    }

    override fun step(f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.CE_DIVING_BOARD, FixtureType.CE_LIFEBUOY -> {
                // The board flexes and the lifebuoy spins, both as springs that settle down.
                val stiff = if (f.type == FixtureType.CE_DIVING_BOARD) 80f else 14f
                f.angleV += (-f.angle * stiff - f.angleV * 4f) * dt
                f.angle += f.angleV * dt
            }
            FixtureType.CE_POOL_FLOAT -> drift(f, dt)
            FixtureType.CE_POOL_SLIDE -> ride(f, dt)
            FixtureType.CE_SHOWER -> if (f.on) {
                f.timer -= dt
                if (f.timer <= 0f) {
                    f.on = false
                    c.fx(CellarCode.SHOWER, f.x, f.y - 0.3f, f, arg = 0)
                }
            }
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ springboard

    /** A tap on the board: whoever stands on it dives into the pool. */
    private fun dive(board: Fixture) {
        board.angleV = 7f
        val divers = c.persons().filter { it.restOwner == board.id && it.resting && !it.held && it.mode == Mode.FREE }
        for (p in divers) {
            p.resting = false
            p.restOwner = -2
            p.vx = DIVE_VX
            p.vy = DIVE_VY
            p.anim.face = Face.LAUGH
            p.anim.faceTime = 1.6f
            c.sim.tasks.record(Deed.CE_DIVE, c.place, null, board.type)
        }
        c.fx(CellarCode.DIVE, board.x + 0.18f, board.y - 0.13f, board, arg = divers.size)
    }

    // ------------------------------------------------------------------ the slide

    private fun go(f: Fixture) {
        if (f.on) return
        if (c.world.seatedAt(f, 0) != null) {
            f.on = true
            f.angle = 0f
            c.fx(CellarCode.SLIDE_GO, f.x + 0.1f, f.y - 0.4f, f)
        } else {
            f.anim = 1f
            c.fx(CellarCode.SPLASH_TAP, f.x - 0.05f, f.y - 0.3f, f)
        }
    }

    /** Whoever sits at the top slides after a moment; at the bottom they fly out over the water. */
    private fun ride(f: Fixture, dt: Float) {
        val rider = c.world.seatedAt(f, 0)
        if (rider == null) {
            f.on = false
            f.timer = 0f
            return
        }
        if (!f.on) {
            f.timer += dt
            if (f.timer > WAIT_SECONDS) {
                f.on = true
                f.angle = 0f
                c.fx(CellarCode.SLIDE_GO, f.x + 0.1f, f.y - 0.4f, f)
            }
            return
        }
        f.angle += dt / SLIDE_SECONDS
        if (f.angle >= 1f) {
            f.angle = 1f
            val end = slidePoint(f)
            rider.mode = Mode.FREE
            rider.holder = -1
            rider.x = end[0]
            rider.y = end[1]
            rider.vx = -0.95f
            rider.vy = -0.4f
            rider.resting = false
            rider.restOwner = -2
            rider.z = c.world.nextZ()
            rider.anim.face = Face.LAUGH
            rider.anim.faceTime = 1.6f
            f.on = false
            f.timer = 0f
            f.angle = 0f
        }
    }

    /** Where a rider is [f]'s way down: from the top seat, curving to the lip above the water. */
    private fun slidePoint(f: Fixture): FloatArray {
        val u = if (f.on) f.angle.coerceIn(0f, 1f) else 0f
        val down = 1f - (1f - u) * (1f - u)
        return floatArrayOf(f.x + 0.11f - 0.27f * u, f.y - 0.4f + 0.285f * down)
    }

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? = when (f.type) {
        FixtureType.CE_POOL_SLIDE -> slidePoint(f)
        FixtureType.CE_POOL_FLOAT -> {
            val s = f.spec.spots[spot]
            floatArrayOf(f.x + s.dx + f.shiftX, f.y + s.dy + f.bob)
        }
        else -> null
    }

    // ------------------------------------------------------------------ duck and water

    /** The duck bobs and drifts slowly to and fro between the two ends of the pool. */
    private fun drift(f: Fixture, dt: Float) {
        f.bob = sin(c.clock * 1.7f + f.id) * 0.006f
        f.angleV += sin(c.clock * 0.4f + f.id) * 0.02f * dt
        f.angleV *= exp(-0.5f * dt)
        f.shiftX += f.angleV * dt
        val lo = CellarFloor.POOL_X1 + 0.18f
        val hi = CellarFloor.POOL_X2 - 0.18f
        val x = f.x + f.shiftX
        if (x < lo) {
            f.shiftX = lo - f.x
            f.angleV = abs(f.angleV) * 0.8f
        } else if (x > hi) {
            f.shiftX = hi - f.x
            f.angleV = -abs(f.angleV) * 0.8f
        }
    }

    /** A tap on the water: whatever floats near it bobs up and is nudged away. */
    private fun ripple(x: Float) {
        for (b in c.world.bodiesIn(c.place)) {
            if (b.mode != Mode.FREE || b.held || abs(b.x - x) > RIPPLE_REACH || c.sim.poolAt(b.x, b.y) == null) continue
            b.vy = min(b.vy, -0.5f)
            b.vx += (if (b.x >= x) 1f else -1f) * 0.18f * max(0.2f, 1f - abs(b.x - x) / RIPPLE_REACH)
        }
    }

    companion object {
        /** How a diver leaves the end of the board. */
        const val DIVE_VX = 0.75f
        const val DIVE_VY = -1.85f
        const val WAIT_SECONDS = 0.55f
        const val SLIDE_SECONDS = 1.15f
        const val SHOWER_SECONDS = 7f
        const val RIPPLE_REACH = 0.35f
    }
}
