package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Person
import app.trollfoss.domain.World
import kotlin.random.Random

/**
 * What an effect of the big house can use on stage: sound, particles, timers and faces. The [Engine]
 * implements it, so the floors' effect files (HouseGroundFx.kt and so on) never touch the engine itself.
 * Coordinates are scene units; `y` is up the screen as everywhere (0 is the top, 1 the bottom).
 */
interface FxStage {
    val world: World
    val place: PlaceId

    /** Seconds since this view opened; use it with [after] for sequences. */
    val time: Float
    val random: Random

    fun sfx(effect: Sfx, volume: Float = 0.8f, rate: Float = 1f)

    /** Runs [block] after [seconds]. */
    fun after(seconds: Float, block: () -> Unit)

    fun burst(kind: PKind, x: Float, y: Float, n: Int, speed: Float = 0.5f, size: Float = 0.012f, color: Color? = null, up: Float = 0.2f, life: Float = 0.9f)
    fun particle(p: Particle)

    /** A face now, another a moment later. */
    fun faces(p: Person, first: Face, firstTime: Float, then: Face, thenTime: Float)

    /** A figure's voice at its own pitch (animals use their own sounds unless [own]). */
    fun voice(p: Person, sfx: Sfx, volume: Float, own: Boolean = false)

    /** Everyone near [x] laughs, a moment later. */
    fun laughAround(x: Float, except: Person?, delay: Float = 0.45f, reach: Float = 0.7f)

    fun person(id: Int): Person?
    fun haptic()
}
