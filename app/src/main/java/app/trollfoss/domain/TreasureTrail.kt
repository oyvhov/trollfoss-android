package app.trollfoss.domain

/** Three real picture notes, found in any order. Earned clues and the one lantern survive undo. */
class TreasureTrail(private val sim: Sim) {
    private val world get() = sim.world
    val started get() = sim.magic.started(Adventure.RUMLE)
    val complete get() = (0..2).all(::found)
    fun found(index: Int) = index in 0..2 && "adventure:RUMLE:${index + 1}" in world.flags
    val nextIndex get() = (0..2).firstOrNull { !found(it) } ?: 2

    fun note(index: Int): Thing? = (world.bodies[world.rumleNotes[index]] as? Thing)
        ?.takeIf { it.type == ThingType.RUMLE_NOTE && it.variant == index }
    fun lamp(): Thing? = (world.bodies[world.rumleLantern] as? Thing)?.takeIf { it.type == ThingType.TROLL_LANTERN }
    fun target(): Thing? = if (complete) lamp() else note(nextIndex)
    fun nextPlace(): PlaceId = target()?.place ?: if (target() != null) world.place else stops[nextIndex].place

    /** Only a missing quest item is supplied. An item the child moved or packed is never taken away. */
    fun prepare() {
        if (!started) return
        for (i in 0..2) {
            val existing = note(i)
            if (existing != null) { existing.used = if (found(i)) 1 else 0; continue }
            if (found(i)) continue
            val stop = stops[i]
            val anchor = world.fixturesIn(stop.place).firstOrNull { it.type == stop.landmark }
            val x = ((anchor?.x ?: stop.x) + 0.2f).coerceIn(0.15f, stop.place.width - 0.15f)
            val t = world.addThing(ThingType.RUMLE_NOTE, i, stop.place, x, stop.place.floor)
            t.ground = stop.place.floor
            world.rumleNotes[i] = t.id
        }
        if (complete && lamp() == null) supplyLantern()
    }

    /** Picking up or reading a note both count. Keeping the paper is part of the pretend play. */
    fun find(t: Thing): Boolean {
        if (!started || t.type != ThingType.RUMLE_NOTE || world.bodies[t.id] !== t || t.mode !in listOf(Mode.FREE, Mode.WORN)) return false
        val i = t.variant
        if (i !in 0..2 || note(i) !== t || !world.flags.add("adventure:RUMLE:${i + 1}")) return false
        t.place?.let { sim.here = it }
        t.used = 1
        sim.magic.refresh()
        sim.listener.onFx(Fx.PAGE, t.x, t.y - t.h, thing = t)
        t.place?.let { sim.magic.cheer(it, t.x) }
        if (complete) {
            val reward = lamp() ?: supplyLantern()
            sim.firstTime(First.RUMLE_TREASURE, t.x, t.y - t.h)
            sim.listener.onFx(Fx.GIFT, t.x, t.y - t.h, thing = reward, param = 1)
        }
        return true
    }

    fun use(t: Thing): Boolean {
        if (world.bodies[t.id] !== t || t.held || t.mode !in listOf(Mode.FREE, Mode.WORN)) return false
        if (t.mode == Mode.WORN && (world.bodies[t.holder] as? Person)?.held == true) return false
        when (t.type) {
            ThingType.RUMLE_NOTE -> if (!find(t)) sim.listener.onFx(Fx.PAGE, t.x, t.y - t.h, thing = t)
            ThingType.TROLL_LANTERN -> {
                t.place?.let { sim.here = it }
                t.used = (t.used.coerceIn(0, 3) + 1) % 4
                sim.listener.onFx(Fx.LANTERN, t.x, t.y - t.h, thing = t, param = t.used)
                if (t.used != 0) sim.firstTime(First.TROLL_LANTERN, t.x, t.y - t.h)
            }
            else -> return false
        }
        return true
    }

    private fun supplyLantern(): Thing = world.addThing(ThingType.TROLL_LANTERN, 0, null, 0f, 0f).also {
        it.mode = Mode.BAG; it.z = world.nextZ()
        world.rumleLantern = it.id
        sim.listener.onSpawn(it)
    }

    data class Stop(val place: PlaceId, val landmark: FixtureType, val x: Float)
    companion object {
        val stops = listOf(
            Stop(PlaceId.FOREST, FixtureType.OWL_TREE, 0.3f),
            Stop(PlaceId.BEACH, FixtureType.SANDCASTLE, 0.9f),
            Stop(PlaceId.LAB, FixtureType.CRYSTAL_BALL, 1.65f),
        )
        fun lit(t: Thing) = t.type == ThingType.TROLL_LANTERN && t.used in 1..3
    }
}
