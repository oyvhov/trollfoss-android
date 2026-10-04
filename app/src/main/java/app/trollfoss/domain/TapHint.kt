package app.trollfoss.domain

import kotlin.math.hypot
import kotlin.math.max

/**
 * A hint in the moment: when a tap hits nothing, the nearest piece of furniture that would have answered
 * wobbles and glows for a blink. Nothing is shown before the child tries, and nothing stays afterwards.
 */
object TapHint {
    /** How far from a missed tap a piece may be and still answer, in scene units. */
    const val REACH = 0.35f

    /** Seconds the glow lasts. */
    const val SECONDS = 0.6f

    /** Seconds before the next hint may come. */
    const val PAUSE = 1.5f

    /** Furniture that does something when tapped: usable ways between floors, cupboards, machines and vehicles. */
    fun answers(world: World, f: Fixture): Boolean =
        House.passageAt(f)?.let { House.usable(world, it) } == true ||
            f.spec.container != null || f.spec.machine != Machine.NONE || Vehicles.controllable(f)

    /** How far the point lies from the box of [f]; 0 inside it. */
    fun distance(f: Fixture, x: Float, y: Float): Float {
        val fx = f.x + f.shiftX
        val dx = max(0f, max(fx - f.spec.w / 2f - x, x - (fx + f.spec.w / 2f)))
        val dy = max(0f, max(f.top - y, y - f.y))
        return hypot(dx, dy)
    }

    fun nearest(world: World, place: PlaceId, x: Float, y: Float): Fixture? =
        world.fixturesIn(place).filter { answers(world, it) }
            .map { it to distance(it, x, y) }
            .filter { it.second <= REACH }
            .minByOrNull { it.second }?.first
}
