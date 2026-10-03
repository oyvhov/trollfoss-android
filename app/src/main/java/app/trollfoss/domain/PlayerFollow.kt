package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** A camera journey is a walk, never a second arrival. Runtime state only; bodies keep their IDs. */
class PlayerFollow(private val sim: Sim) {
    private val world get() = sim.world
    private data class Walker(var target: Float, var speed: Float = 0f)
    private val walkers = mutableMapOf<Int, Walker>()
    private val excluded = mutableSetOf<Int>()
    private val order = mutableListOf<Int>()
    private var place: PlaceId? = null
    private var left = 0f
    private var width = 1f
    private var active = false
    private var chosenRoom: Int? = null
    private var still = 0f
    var dirty = false
        private set

    /** Called by a deliberate scene swipe or room choice, not by a resize or driving camera. */
    fun navigate(to: PlaceId, camera: Float, viewport: Float, room: Int? = null) {
        if (!active || place != to) {
            cancel()
            excluded.clear()
            active = true
            place = to
            order.clear()
            order += Players.team(world).filter { it.place == to }.sortedWith(compareBy<Person> { it.x }.thenBy { it.id }).map { it.id }
        }
        view(camera, viewport)
        chosenRoom = room
        still = 0f
    }

    /** Camera inertia keeps the same journey; an idle camera does not keep recruiting figures. */
    fun view(camera: Float, viewport: Float) {
        if (!active || !camera.isFinite() || !viewport.isFinite() || viewport <= 0f) return
        if (abs(camera - left) > 0.001f) still = 0f
        left = camera
        width = viewport
    }

    fun acknowledge() { dirty = false }

    fun cancel() {
        walkers.keys.forEach { id -> (world.bodies[id] as? Person)?.let(::stopAnimation) }
        walkers.clear()
        active = false
        still = 0f
    }

    private fun stopAnimation(p: Person) {
        if (!p.anim.following) return
        p.anim.following = false
        p.anim.followSpeed = 0f
        p.anim.walkTo = Float.NaN
        p.anim.nextWalk = 4f
    }

    fun step(here: PlaceId, dt: Float) {
        if (!active || here != place || dt <= 0f) return
        still += dt
        val team = Players.team(world).filter { it.place == here }
        val ids = team.map { it.id }.toSet()
        walkers.keys.filter { it !in ids }.forEach { id ->
            (world.bodies[id] as? Person)?.let(::stopAnimation)
            walkers.remove(id)
        }
        // Keep world order, including a held child, so letting one go never reshuffles the other.
        team.filter { it.id !in order }.forEach { order += it.id }
        val ordered = team.sortedBy { order.indexOf(it.id) }
        val margin = min(width * 0.22f, 0.38f)
        val comfortLeft = left + margin
        val comfortRight = left + width - margin
        val room = Decor.rooms(here).getOrNull(chosenRoom ?: Decor.roomAt(here, left + width / 2f))
        val bandLeft = max(if (chosenRoom == null) comfortLeft else left + 0.18f, room?.start?.plus(0.10f) ?: comfortLeft)
        val bandRight = max(bandLeft + 0.20f, min(if (chosenRoom == null) comfortRight else left + width - 0.18f, room?.endInclusive?.minus(0.10f) ?: comfortRight))
        val spacing = min(0.28f, (bandRight - bandLeft - 0.20f) / max(1f, ordered.size - 1f))
        // Reserve separate destinations before anyone moves. A friend already near the edge can
        // take a small step to make room, instead of the followers stacking on the same point.
        val targets = ordered.map { p -> (walkers[p.id]?.target ?: p.x).coerceIn(bandLeft + 0.06f, bandRight - 0.06f) }.toMutableList()
        for (i in targets.indices) {
            val lo = if (i == 0) bandLeft + 0.06f else targets[i - 1] + spacing
            val hi = bandRight - 0.06f - (targets.lastIndex - i) * spacing
            targets[i] = max(lo, targets[i]).coerceAtMost(max(lo, hi))
        }
        val needsSpace = ordered.any { !it.held && it.x !in comfortLeft..comfortRight }
        for ((index, p) in ordered.withIndex()) {
            if (p.held || p.mode == Mode.BAG || p.inside >= 0) excluded += p.id
            if (p.id in excluded || p.place != here) {
                walkers.remove(p.id)
                stopAnimation(p)
                continue
            }
            val outside = p.x < comfortLeft || p.x > comfortRight
            var walker = walkers[p.id]
            val start = outside || chosenRoom != null && Decor.roomAt(here, p.x) != chosenRoom ||
                needsSpace && abs(targets[index] - p.x) > 0.04f
            if (walker == null && (!start || still > 0.45f)) continue
            if (p.mode == Mode.SEATED) {
                val seat = world.fixtures[p.holder]
                // A child can keep playing in a vehicle or a bed while the other explores.
                if (seat == null || Vehicles.controllable(seat) || p.anim.pose == Pose.LIE ||
                    p.x in (left - p.w)..(left + width + p.w)) {
                    walkers.remove(p.id)
                    stopAnimation(p)
                    continue
                }
                House.moveTo(world, p, here, p.x, p.y)
                dirty = true
            }
            if (p.mode != Mode.FREE || p.floatTime > 0f || p.flyT >= 0f) {
                walkers.remove(p.id)
                stopAnimation(p)
                continue
            }
            val target = targets[index].coerceIn(p.w / 2f + 0.02f, here.width - p.w / 2f - 0.02f)
            if (!walkable(here, p.x, target)) {
                walkers.remove(p.id)
                stopAnimation(p)
                continue
            }
            if (walker == null) {
                walker = Walker(target)
                walkers[p.id] = walker
                p.anim.goal = 0
                p.anim.goalFixture = -1
                p.anim.auto = 0
            }
            walker.target = target
            p.anim.following = true
            p.anim.nextWalk = 4f
            // Keep real falling, floating and swimming. On solid floor, walk gently towards the
            // front of the floor band so a following child does not vanish behind a chair or sofa.
            if (p.resting && p.restOwner == -1 && !sim.zeroG(here) && !sim.underwater(here)) {
                val ground = sim.groundOf(here, p)
                val depthStep = (PlaceId.FRONT - 0.012f - ground).coerceIn(0f, dt * 0.12f)
                p.ground = ground + depthStep
                p.y = p.ground
                if (depthStep > 0f) dirty = true
            }
            val dx = target - p.x
            val desired = (dx * 4f).coerceIn(-MAX_SPEED, MAX_SPEED)
            walker.speed += (desired - walker.speed).coerceIn(-ACCELERATION * dt, ACCELERATION * dt)
            p.anim.followSpeed = abs(walker.speed)
            var movement = walker.speed * dt
            if (movement * dx > 0f && abs(movement) > abs(dx)) movement = dx
            // Never step through an unfinished room, including while braking on reversal.
            if (!walkable(here, p.x, p.x + movement)) movement = 0f
            p.x = (p.x + movement).coerceIn(p.w / 2f, here.width - p.w / 2f)
            p.vx = 0f
            if (abs(movement) > 0.00001f) {
                dirty = true
                p.anim.walkTo = target
                // Keep a readable walking cadence even when a small figure hurries to catch up.
                p.anim.walkPhase += min(abs(movement) / max(0.04f, p.h * 0.26f), dt * 6f)
                p.anim.facing = if (movement < 0f) -1f else 1f
            }
            if (abs(dx) < 0.008f && abs(walker.speed) < 0.06f) {
                walkers.remove(p.id)
                stopAnimation(p)
            }
        }
        if (still > 0.5f && walkers.isEmpty()) {
            active = false
            excluded.clear()
        }
    }

    private fun walkable(here: PlaceId, from: Float, to: Float): Boolean {
        if (here != PlaceId.MINE_GROUND && here != PlaceId.MINE_UPPER) return true
        val first = Mine.slotAt(min(from, to))
        val last = Mine.slotAt(max(from, to))
        return (first..last).all { world.mine.standing(here, it) }
    }

    companion object {
        const val MAX_SPEED = 1.65f
        const val ACCELERATION = 4.5f
    }
}
