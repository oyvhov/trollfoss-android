package app.trollfoss.domain

/** Reusable secret rooms. Append to blueprints, preserving all existing furniture ids. */
object PlaySecrets {
    val nooks = mapOf(
        PlaceId.HOME to (3.75f to 0), PlaceId.CAFE to (2.55f to 1),
        PlaceId.LAB to (2.35f to 2), PlaceId.SPACE to (3.15f to 3),
        PlaceId.FARM to (4.0f to 4), PlaceId.VAGSTADDALEN to (4.25f to 5),
    )

    fun tap(sim: Sim, f: Fixture, dx: Float): Boolean {
        if (f.type != FixtureType.SECRET_NOOK) return false
        // The one red book is the clue. Three quick knocks also work.
        if (!f.open && dx < 0.12f && f.taps < 3) {
            sim.listener.onFx(Fx.KNOCK, f.x + dx, f.top + 0.12f, f)
            return true
        }
        reveal(sim, f)
        return true
    }

    fun drop(sim: Sim, f: Fixture, t: Thing): Boolean {
        if (f.type != FixtureType.SECRET_NOOK) return false
        if (t.type == ThingType.WAND || t.type == ThingType.GOLDEN_KEY || t.type == ThingType.CROWN) {
            if (!f.open) reveal(sim, f) else sim.listener.onFx(Fx.SPARKLE, f.x, f.top, f, t)
        }
        return false // Keep the key or wand available for further adventures.
    }

    private fun reveal(sim: Sim, f: Fixture) {
        f.open = !f.open
        sim.invalidate(f.place)
        sim.listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.top, f)
        if (!f.open) return
        sim.listener.onFx(Fx.SPARKLE, f.x, f.top + 0.15f, f)
        val key = "nook_${f.place.name}_${f.variant}"
        sim.egg(key)
        if (sim.world.flags.add(key)) {
            val surprise = listOf(ThingType.TEDDY, ThingType.MUSHROOM, ThingType.SLIME, ThingType.POTION_FLOAT, ThingType.CROWN, ThingType.DRAGON_EGG)[f.variant.mod(6)]
            val t = sim.world.addThing(surprise, 0, f.place, f.x + 0.19f, f.y - 0.15f)
            t.inside = f.id; t.restOwner = f.id; t.resting = true; t.ground = t.y
            sim.listener.onSpawn(t)
        }
    }
}
