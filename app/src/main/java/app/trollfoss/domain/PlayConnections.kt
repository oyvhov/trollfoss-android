package app.trollfoss.domain

import kotlin.math.max

/** Rules shared by every room: the same thing keeps its meaning when taken somewhere else. */
object PlayConnections {
    private val raw = setOf(ThingType.FISH, ThingType.EGG, ThingType.SAUSAGE, ThingType.DOUGH)
    fun canTaste(type: ThingType) = type.edible || type in raw || type == ThingType.MUSHROOM || type == ThingType.SLIME
    fun canTaste(t: Thing) = canTaste(t.type) || (t.type == ThingType.CUP && t.used > 0)
    fun raw(type: ThingType) = type in raw

    fun strange(p: Person, type: ThingType): Boolean {
        when (type) {
            ThingType.MUSHROOM -> {
                if (p.scaleTime <= 0f) p.scaleBefore = p.scale
                p.scale = 2.6f
                p.scaleTime = 20f
            }
            ThingType.SLIME -> p.floatTime = 20f
            else -> return false
        }
        p.anim.sparkle = 1f
        return true
    }

    fun step(p: Person, dt: Float) {
        if (p.scaleTime <= 0f) return
        p.scaleTime = max(0f, p.scaleTime - dt)
        if (p.scaleTime == 0f) {
            p.scale = p.scaleBefore
            p.anim.sparkle = 1f
        }
    }

    /** Bring the existing friend, with their clothes and belongings. Never create a duplicate. */
    fun invite(sim: Sim, p: Person, to: PlaceId, x: Float): Boolean {
        if (sim.world.bodies[p.id] !== p) return false
        var cx = x.coerceIn(p.w / 2f + 0.03f, to.width - p.w / 2f - 0.03f)
        if (to == PlaceId.MINE_GROUND || to == PlaceId.MINE_UPPER) {
            if ((0 until Mine.SLOTS).none { sim.world.mine.standing(to, it) }) return false
            cx = Mine.clampX(sim.world.mine, to, cx, p.w / 2f)
        }
        val bands = sim.surfaces(to).filter { it.band }
        val band = bands.firstOrNull { cx in it.x1..it.x2 }
            ?: bands.minByOrNull { minOf(kotlin.math.abs(cx - it.x1), kotlin.math.abs(cx - it.x2)) }
        if (band == null) return false
        val inset = minOf(p.w / 2f, (band.x2 - band.x1) / 2f)
        cx = cx.coerceIn(band.x1 + inset, band.x2 - inset)
        House.moveTo(sim.world, p, to, cx)
        sim.world.mine.guests.removeAll { it.id == p.id }
        p.held = false
        p.rot = 0f
        p.vrot = 0f
        p.cool = 1f
        p.anim.wave = 2f
        p.anim.nameTag = 2.2f
        p.z = sim.world.nextZ()
        // Ground bands keep invited friends on a bank, including in the valley.
        p.ground = to.floor
        sim.updatePose(p)
        return true
    }
}
