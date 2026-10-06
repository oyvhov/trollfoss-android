package app.trollfoss.domain

import kotlin.math.abs

/** Level 7: cable car, diving bell, digger and treasure table. Riders, cargo and parts keep their ids. */
class AdventurePlay(private val sim: Sim) {
    private val world get() = sim.world
    /** Seconds a treasure table rests after a surprise, so a room never fills up. Not saved: a fresh start is fine. */
    private val rest = HashMap<Int, Float>()

    fun tap(f: Fixture): Boolean = when (f.type) {
        FixtureType.PLAY_CABLE_CAR -> { f.mode = 1 - f.mode.coerceIn(0, 1); f.on = true; f.anim = 1f; sim.listener.onFx(Fx.WHEE, f.x, f.top, f); sim.firstTime(First.CABLE_CAR, f.x, f.top); true }
        FixtureType.PLAY_DIVING_BELL -> { f.mode = 1 - f.mode.coerceIn(0, 1); f.on = true; f.anim = 1f; sim.listener.onFx(Fx.BUBBLES, f.x, f.top, f); sim.firstTime(First.DIVING_BELL, f.x, f.top); true }
        FixtureType.PLAY_TREASURE_TABLE -> { surprise(f); f.anim = 1f; true }
        else -> false // The digger drives on through the vehicle rules, like the bus.
    }

    fun drop(f: Fixture, t: Thing): Boolean {
        if (!accepts(f, t) || t.held) return false
        val inside = world.inMachine(f)
        if (if (f.type == FixtureType.PLAY_TREASURE_TABLE) inside.size >= 3 else inside.isNotEmpty()) return false
        t.mode = Mode.INSIDE; t.holder = f.id; t.inside = -1; t.resting = false; t.restOwner = -2; t.x = f.x; t.y = f.top; t.vx = 0f; t.vy = 0f
        if (f.type == FixtureType.PLAY_TREASURE_TABLE) t.x = f.x + (inside.size - 1) * 0.12f
        f.anim = 1f; sim.listener.onFx(Fx.INTO, f.x, f.top, f, t); return true
    }

    fun step(f: Fixture, dt: Float): Boolean {
        when (f.type) {
            FixtureType.PLAY_CABLE_CAR, FixtureType.PLAY_DIVING_BELL -> {
                val target = f.mode.coerceIn(0, 1).toFloat()
                f.angle += (target - f.angle).coerceIn(-dt * 0.35f, dt * 0.35f)
                if (abs(target - f.angle) < 0.001f) f.on = false
                val cargo = world.inMachine(f).firstOrNull()
                if (f.type == FixtureType.PLAY_CABLE_CAR) {
                    cargo?.let { it.x = f.x - 0.36f + f.angle * 0.72f; it.y = f.y - 0.3f }
                    // High up on the line, the rider holds on tight and goes «oooh».
                    if (f.on) world.seatedAt(f, 0)?.anim?.let { it.face = Face.OOH; it.faceTime = 0.5f }
                }
                else {
                    val bx = bellX(f); f.reach = bx - f.x; f.dive = depth(f)
                    cargo?.let { it.x = bx; it.y = f.y - 0.08f + f.angle * depth(f) }
                    // Down in real water, a loose thing below comes along on the way up.
                    if (cargo == null && f.angle > 0.95f && overWater(f)) world.bodiesIn(f.place).filterIsInstance<Thing>()
                        .firstOrNull { it.mode == Mode.FREE && !it.held && abs(it.x - bx) < 0.2f && it.type.cat !in setOf(Cat.HAT, Cat.GARMENT) }
                        ?.let { t -> t.mode = Mode.INSIDE; t.holder = f.id; t.inside = -1; t.resting = false; t.restOwner = -2; t.vx = 0f; t.vy = 0f }
                }
            }
            FixtureType.PLAY_TREASURE_TABLE -> rest[f.id]?.let { rest[f.id] = it - dt }
            else -> return false
        }
        return true
    }

    /** How deep the bell goes: into real water, or just a little way on dry land. */
    fun depth(f: Fixture): Float = if (overWater(f)) 0.22f else 0.08f
    fun overWater(f: Fixture): Boolean = sim.underwater(f.place) || sim.pools(f.place).any { bellX(f) in it.x1..it.x2 }

    /** Furniture stands on the shore, so the bell's arm reaches out over water close by; elsewhere it hangs straight down. */
    fun bellX(f: Fixture): Float {
        if (sim.underwater(f.place)) return f.x
        val p = sim.pools(f.place).minByOrNull { minOf(abs(it.x1 - f.x), abs(it.x2 - f.x)) } ?: return f.x
        return when {
            f.x in p.x1..p.x2 -> f.x
            p.x1 > f.x && p.x1 - f.x < REACH -> p.x1 + 0.1f
            p.x2 < f.x && f.x - p.x2 < REACH -> p.x2 - 0.1f
            else -> f.x
        }
    }

    private fun surprise(f: Fixture) {
        val parts = world.inMachine(f)
        if (parts.size < 3) { sim.listener.onFx(Fx.BONK, f.x, f.top, f); return }
        sim.firstTime(First.TREASURE_TABLE, f.x, f.top)
        if ((rest[f.id] ?: 0f) > 0f) { sim.listener.onFx(Fx.SPARKLE, f.x, f.top, f); return }
        rest[f.id] = 45f
        val (type, variant) = when ((world.stickers.size + parts.sumOf { it.id }) % 4) {
            0 -> ThingType.CROWN to 0
            1 -> ThingType.PARTY_HAT to (f.id % 4)
            2 -> ThingType.FLOWER_CROWN to 0
            else -> ThingType.DUCK to 0
        }
        val t = world.addThing(type, variant, f.place, f.x + 0.3f, f.top)
        t.ground = f.depth; t.vy = -0.4f; t.resting = false
        sim.listener.onSpawn(t); sim.listener.onFx(Fx.GIFT, f.x, f.top, f, t, 0)
    }

    companion object {
        val TYPES = setOf(FixtureType.PLAY_CABLE_CAR, FixtureType.PLAY_DIVING_BELL, FixtureType.PLAY_DIGGER, FixtureType.PLAY_TREASURE_TABLE)
        /** Toys whose position (angle 0–1) is saved. */
        const val REACH = 0.4f
        val ANGLED = setOf(FixtureType.PLAY_LIFT, FixtureType.PLAY_CABLE_CAR, FixtureType.PLAY_DIVING_BELL)
        fun accepts(f: Fixture, t: Thing): Boolean = f.type in setOf(FixtureType.PLAY_CABLE_CAR, FixtureType.PLAY_DIVING_BELL, FixtureType.PLAY_TREASURE_TABLE) && t.type.cat !in setOf(Cat.HAT, Cat.GARMENT)
    }
}
