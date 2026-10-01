package app.trollfoss.domain

import kotlin.math.abs

/** Portable results connect the greenhouse, kitchen, workshop and the rest of the village. */
class MinePlay(private val sim: Sim) {
    private val world get() = sim.world

    fun tap(place: PlaceId, f: Fixture): Boolean {
        when (f.type) {
            FixtureType.MI_PLANT_BED -> {
                if (f.count >= 3) {
                    produce(place, f, if (f.variant == 1) ThingType.FLOWER else ThingType.STRAWBERRY)
                    f.count = 1
                    sim.listener.onFx(Fx.GROW, f.x, f.top, f, param = 1)
                } else {
                    if (f.count == 0) offer(place, f, ThingType.SEEDS, -0.23f)
                    offer(place, f, ThingType.WATERING_CAN, 0.25f)
                    sim.listener.onFx(Fx.WATER, f.x, f.top, f)
                }
            }
            FixtureType.MI_COUNTER -> if (!f.on) {
                offer(place, f, ThingType.EGG, -0.1f)
                offer(place, f, ThingType.MILK, 0.12f)
            }
            FixtureType.MI_SAW_BENCH -> if (!f.on) offer(place, f, ThingType.PLANK, -0.2f)
            else -> return false
        }
        return true
    }

    fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean {
        if (!accepts(world, f, t)) return false
        if (f.type == FixtureType.MI_PLANT_BED) {
            if (t.type == ThingType.SEEDS) { sim.removeThing(t, quiet = true); f.count = 1 }
            else { f.count++; sim.listener.onFx(Fx.WATER, f.x, f.top, f) }
            sim.listener.onFx(Fx.GROW, f.x, f.top, f, param = f.count)
            // The watering can is reusable and falls beside the bed.
            return t.type == ThingType.SEEDS
        }
        t.held = false; t.place = place; t.mode = Mode.INSIDE; t.holder = f.id
        t.inside = -1; t.resting = false; t.x = f.x; t.y = f.top; t.vx = 0f; t.vy = 0f
        val inputs = world.inMachine(f).map { it.type }
        f.count = (if (ThingType.EGG in inputs) 1 else 0) + (if (ThingType.MILK in inputs) 2 else 0)
        if (Recipes.houseWork(f.type, inputs) != null) { f.on = true; f.timer = 1.6f }
        sim.listener.onFx(if (f.type == FixtureType.MI_SAW_BENCH) Fx.HAMMER else Fx.INTO, f.x, f.top, f, t)
        return true
    }

    fun step(f: Fixture, dt: Float): Boolean {
        if (f.type != FixtureType.MI_COUNTER && f.type != FixtureType.MI_SAW_BENCH) return false
        if (!f.on) return true
        f.timer = (f.timer - dt).coerceAtLeast(0f)
        if (f.timer > 0f) return true
        f.on = false
        val contents = world.inMachine(f)
        val inputs = contents.map { it.type }
        val made = Recipes.houseWork(f.type, inputs) ?: return true
        contents.forEach { sim.removeThing(it, quiet = true) }
        f.count = 0
        repeat(if (f.type == FixtureType.MI_SAW_BENCH) 2 else 1) { i -> produce(f.place, f, made.type, (i - 0.5f) * 0.08f) }
        Recipes.keyFor(f.type, inputs)?.let(sim::discover)
        sim.listener.onFx(Fx.HAMMER, f.x, f.top, f)
        return true
    }

    private fun offer(place: PlaceId, f: Fixture, type: ThingType, dx: Float) {
        if (world.inMachine(f).any { it.type == type }) return
        if (world.bodiesIn(place).any { it is Thing && it.type == type && abs(it.x - f.x) < 0.75f }) return
        produce(place, f, type, dx)
    }

    private fun produce(place: PlaceId, f: Fixture, type: ThingType, dx: Float = 0f) {
        val t = world.addThing(type, 0, place, f.x + dx, f.top - 0.045f)
        t.vy = -0.45f; t.vx = dx * 0.5f
        t.homePlace = place; t.homeOwner = f.id; t.homeDx = dx; t.homeDy = -f.spec.h
        sim.listener.onSpawn(t)
    }

    companion object {
        fun accepts(world: World, f: Fixture, t: Thing): Boolean = when (f.type) {
            FixtureType.MI_PLANT_BED -> t.type == ThingType.SEEDS && f.count == 0 || t.type == ThingType.WATERING_CAN && f.count in 1..2
            FixtureType.MI_COUNTER -> !f.on && (t.type == ThingType.EGG || t.type == ThingType.MILK) && world.inMachine(f).none { it.type == t.type }
            FixtureType.MI_SAW_BENCH -> !f.on && t.type == ThingType.PLANK && world.inMachine(f).isEmpty()
            else -> false
        }
    }
}
