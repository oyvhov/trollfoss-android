package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * The laundry's rules: a washer and a dryer that take things in and spin them round, the basket that
 * catches whoever comes down the chute (and hands out socks), a clothes line, an ironing board, and the sock
 * monster, who eats socks, burps them out in odd pairs and keeps the golden key of the cellar in his belly.
 */
internal class CellarLaundry(private val c: CellarCtx) : CellarPart {
    /** Figures that are falling out of the chute right now. */
    private val falling = HashSet<Int>()
    private var scan = 0f

    /** 1 while somebody holds a sock near the monster's mouth: he opens wide and his eyes grow. */
    private var hunger = 0f

    override fun tap(f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.CE_WASHER, FixtureType.CE_DRYER -> {
            start(f)
            true
        }
        FixtureType.CE_SOCK_MONSTER -> {
            c.fx(CellarCode.SOCK_PLAY, f.x, f.y - 0.14f, f, arg = f.taps)
            true
        }
        FixtureType.CE_BASKET -> {
            pop(f)
            true
        }
        FixtureType.CE_CLOTHESLINE -> {
            swing(f)
            true
        }
        FixtureType.CE_IRON_BOARD -> {
            iron(f)
            true
        }
        FixtureType.CE_CHUTE -> {
            f.timer = FLAP_SECONDS
            c.fx(CellarCode.CHUTE, f.x, f.y - 0.1f, f, arg = 1)
            true
        }
        else -> false
    }

    override fun step(f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.CE_WASHER, FixtureType.CE_DRYER -> spin(f, dt)
            FixtureType.CE_CLOTHESLINE -> {
                f.angleV += (-f.angle * 26f - f.angleV * 1.6f) * dt
                f.angle += f.angleV * dt
            }
            FixtureType.CE_BASKET -> {
                f.angleV += (-f.angle * 70f - f.angleV * 5f) * dt
                f.angle += f.angleV * dt
            }
            FixtureType.CE_IRON_BOARD -> {
                f.timer = max(0f, f.timer - dt)
                f.on = f.timer > 0f
            }
            FixtureType.CE_CHUTE -> f.timer = max(0f, f.timer - dt)
            FixtureType.CE_SOCK_MONSTER -> f.angle += (hunger - f.angle) * min(1f, dt * 8f)
            else -> Unit
        }
    }

    override fun tick(dt: Float) {
        scan -= dt
        if (scan > 0f) return
        scan = SCAN_SECONDS
        val monster = c.fixture(CellarIx.SOCK_MONSTER)
        val chute = c.fixture(CellarIx.CHUTE)
        var hungry = false
        for (b in c.world.bodiesIn(c.place)) {
            if (b is Thing && monster != null && b.held && b.type == ThingType.CE_SOCK &&
                abs(b.x - monster.x) < 0.3f && abs(b.y - (monster.y - 0.12f)) < 0.3f
            ) hungry = true
            if (b is Person && chute != null) {
                val dropping = b.mode == Mode.FREE && !b.held && !b.resting && b.y < 0.66f && abs(b.x - chute.x) < 0.5f
                if (dropping) {
                    if (falling.add(b.id)) {
                        chute.timer = FLAP_SECONDS
                        c.fx(CellarCode.CHUTE, chute.x, chute.y - 0.1f, chute, arg = 0)
                    }
                } else if (b.resting || b.held || b.mode != Mode.FREE) {
                    falling.remove(b.id)
                }
            }
        }
        hunger = if (hungry) 1f else 0f
    }

    // ------------------------------------------------------------------ washer and dryer

    private fun start(f: Fixture) {
        if (f.on) return
        f.on = true
        f.timer = CYCLE_SECONDS
        f.angle = 0f
        f.angleV = 0f
        c.fx(CellarCode.WASH, f.x, f.y - 0.12f, f, arg = if (f.type == FixtureType.CE_DRYER) 1 else 0)
    }

    private fun spin(f: Fixture, dt: Float) {
        if (!f.on) {
            f.bob = 0f
            return
        }
        f.timer -= dt
        f.angle += dt * 9f
        f.bob = sin(c.clock * 42f) * 0.0017f
        f.angleV -= dt
        if (f.angleV <= 0f) {
            f.angleV = 1.3f
            c.fx(CellarCode.WASH, f.x, f.y - 0.12f, f, arg = if (f.type == FixtureType.CE_DRYER) 3 else 2)
        }
        if (f.timer <= 0f) finish(f)
    }

    /** The cycle ends: the door bangs and everything inside tumbles out. The dryer swallows one sock for the monster. */
    private fun finish(f: Fixture) {
        f.on = false
        f.bob = 0f
        f.count++
        val dryer = f.type == FixtureType.CE_DRYER
        val inside = c.world.inMachine(f)
        val eaten = if (dryer) inside.firstOrNull { it.type == ThingType.CE_SOCK } else null
        val out = inside.filter { it !== eaten }
        for ((i, t) in out.withIndex()) release(f, t, i, out.size)
        if (eaten != null) {
            c.sim.removeThing(eaten, quiet = true)
            c.fx(CellarCode.DRYER_LOST, f.x, f.y - 0.12f, f)
            // It turns up again, burped out by the sock monster.
            c.after(1.2f) { c.fixture(CellarIx.SOCK_MONSTER)?.let { m -> burp(m, 1) } }
        }
        c.fx(CellarCode.WASH_DONE, f.x, f.y - 0.1f, f, arg = out.size)
        // Every third wash finds two forgotten socks in the drum.
        if (!dryer && f.count % 3 == 0) {
            val a = c.random.nextInt(SOCK_PATTERNS)
            val b = (a + 1 + c.random.nextInt(SOCK_PATTERNS - 1)) % SOCK_PATTERNS
            c.spawn(ThingType.CE_SOCK, a, f.x - 0.02f, f.y - 0.26f, f, -0.4f, -1.9f, 250f)
            c.spawn(ThingType.CE_SOCK, b, f.x + 0.02f, f.y - 0.26f, f, 0.5f, -2.1f, -250f)
            c.limit(ThingType.CE_SOCK, MAX_SOCKS)
        }
    }

    private fun release(f: Fixture, t: Thing, i: Int, n: Int) {
        val side = i - (n - 1) / 2f
        t.mode = Mode.FREE
        t.holder = -1
        t.inside = -1
        t.held = false
        t.resting = false
        t.x = f.x + side * 0.04f
        t.y = f.y - 0.12f
        c.inFront(t, f)
        t.vy = -1.6f
        t.vx = side * 0.3f + 0.15f
        t.vrot = if (i % 2 == 0) 220f else -220f
    }

    // ------------------------------------------------------------------ the sock monster

    override fun drop(f: Fixture, t: Thing): Boolean = when (f.type) {
        FixtureType.CE_WASHER, FixtureType.CE_DRYER -> {
            if (f.on || c.world.inMachine(f).size >= MACHINE_CAPACITY) {
                false
            } else {
                t.mode = Mode.INSIDE
                t.holder = f.id
                t.held = false
                t.resting = false
                t.inside = -1
                t.x = f.x
                t.y = f.top
                c.listener.onFx(Fx.INTO, f.x, f.top, f, t)
                true
            }
        }
        FixtureType.CE_SOCK_MONSTER -> feed(f, t)
        else -> false
    }

    private fun feed(f: Fixture, t: Thing): Boolean {
        if (t.type != ThingType.CE_SOCK) {
            // Not a sock: «bleh!», and the thing is handed back with a hop.
            t.resting = false
            t.restOwner = -2
            t.vy = -1.5f
            t.vx = if (t.x >= f.x) 0.55f else -0.55f
            c.inFront(t, f)
            t.ground += 0.02f
            c.fx(CellarCode.SOCK_BLEH, f.x, f.y - 0.14f, f, t)
            return true
        }
        c.sim.removeThing(t, quiet = true)
        f.count++
        c.sim.tasks.record(Deed.CE_SOCK_FED, c.place, ThingType.CE_SOCK, f.type)
        c.fx(CellarCode.SOCK_EAT, f.x, f.y - 0.12f, f)
        c.after(0.55f) { burp(f, 2) }
        // The golden key sits in his belly: the third sock shakes it loose.
        if (f.count >= KEY_AFTER && CELLAR_KEY !in c.world.flags) c.after(1.3f) { spitKey(f) }
        return true
    }

    /** A burp and [n] socks, never a matching pair. */
    private fun burp(f: Fixture, n: Int) {
        c.fx(CellarCode.SOCK_BURP, f.x, f.y - 0.14f, f, arg = n)
        val a = c.random.nextInt(SOCK_PATTERNS)
        val b = (a + 1 + c.random.nextInt(SOCK_PATTERNS - 1)) % SOCK_PATTERNS
        c.spawn(ThingType.CE_SOCK, a, f.x - 0.05f, f.y - 0.17f, f, -0.55f, -1.8f, -240f)
        if (n > 1) c.spawn(ThingType.CE_SOCK, b, f.x + 0.05f, f.y - 0.17f, f, 0.6f, -2.0f, 260f)
        c.limit(ThingType.CE_SOCK, MAX_SOCKS)
    }

    private fun spitKey(f: Fixture) {
        // The first time only: the key is found, the jingle rings, and the key itself flies out of his mouth.
        if (!c.sim.flag(CELLAR_KEY)) return
        val key = c.spawn(ThingType.GOLDEN_KEY, 0, f.x, f.y - 0.17f, f, 0.15f, -2.6f, 360f)
        c.fx(CellarCode.SOCK_KEY, f.x, f.y - 0.17f, f, key)
    }

    // ------------------------------------------------------------------ the rest of the laundry

    /** The basket hands out a sock (every fourth time a top to dress someone in). */
    private fun pop(f: Fixture) {
        f.count++
        f.angleV = 3f
        val garment = f.count % 4 == 0
        val t = if (garment) {
            c.spawn(ThingType.GARMENT, Garment.pack(c.random.nextInt(Styles.TOPS), c.random.nextInt(Palette.cloth.size)), f.x, f.y - 0.17f, f, 0.3f, -1.7f, 180f)
        } else {
            c.spawn(ThingType.CE_SOCK, c.random.nextInt(SOCK_PATTERNS), f.x, f.y - 0.17f, f, (c.random.nextFloat() - 0.5f) * 0.8f, -1.7f, 180f)
        }
        c.limit(ThingType.CE_SOCK, MAX_SOCKS)
        c.limit(ThingType.GARMENT, 6)
        c.fx(CellarCode.BASKET_POP, f.x, f.y - 0.17f, f, t, arg = if (garment) 1 else 0)
    }

    private fun swing(f: Fixture) {
        f.angleV += 2.4f
        val drops = c.random.nextBoolean()
        if (drops) {
            c.spawn(ThingType.CE_SOCK, c.random.nextInt(SOCK_PATTERNS), f.x + (c.random.nextFloat() - 0.5f) * 0.4f, f.y - 0.05f, f, 0f, 0f, 120f)
            c.limit(ThingType.CE_SOCK, MAX_SOCKS)
        }
        c.fx(CellarCode.LINE_SWING, f.x, f.y - 0.1f, f, arg = if (drops) 1 else 0)
    }

    /** The iron slides across; whatever lies on the board is pressed flat and springs back. */
    private fun iron(f: Fixture) {
        f.timer = IRON_SECONDS
        for (b in c.world.bodiesIn(c.place)) {
            if (b is Thing && b.restOwner == f.id && b.resting && !b.held) b.squashV += 16f
        }
        c.fx(CellarCode.IRON, f.x, f.y - 0.17f, f)
    }

    companion object {
        const val CYCLE_SECONDS = 3.4f
        const val FLAP_SECONDS = 0.9f
        const val IRON_SECONDS = 1.3f
        const val SCAN_SECONDS = 0.1f
        const val MACHINE_CAPACITY = 3
        const val SOCK_PATTERNS = 6
        const val MAX_SOCKS = 12

        /** How many socks the monster must eat before the key comes up. */
        const val KEY_AFTER = 3
        const val CELLAR_KEY = "manor_key_cellar"
    }
}
