package app.trollfoss.domain

import kotlin.math.*

/** Shared positions, water level and drop timings for the spring in the rear hall. */
object BergSpring {
    const val X = -1.8f
    const val Z = -20f
    const val WATER = .31f
    const val RESPONSE = 3.45f
    val bank = BergSpot(BergAction.SPRING, X, Z + 2.0f, true, 2.15f)
    val caps = listOf(BergPoint(X-1.30f,Z+.14f), BergPoint(X+.92f,Z-.57f), BergPoint(X+.89f,Z+.49f))
    val sizes = listOf(.66f,.85f,.57f)
    val drip = BergPoint(X-.33f,Z-.19f)
    fun contains(x:Float,z:Float):Boolean {
        val dx=(x-X)/1.72f
        val dz=(z-Z)/1.12f
        return dx*dx+dz*dz<=1f
    }
    fun inReach(x:Float,z:Float) = hypot(x-X,z-Z)<4.1f
    // The cleft catches a longer second drop. The renderer and the sound share these impacts.
    private const val PERIOD=10.6f
    private const val SECOND=4.15f
    fun sinceDrop(time:Float):Float {
        val phase=time%PERIOD
        return if(phase<SECOND) phase else phase-SECOND
    }
    fun untilDrop(time:Float):Float {
        val phase=time%PERIOD
        return if(phase<SECOND) SECOND-phase else PERIOD-phase
    }
}
