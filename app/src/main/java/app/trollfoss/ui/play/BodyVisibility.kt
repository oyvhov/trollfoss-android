package app.trollfoss.ui.play

import app.trollfoss.domain.Body
import app.trollfoss.domain.Person
import kotlin.math.abs
import kotlin.math.max

/**
 * Conservative horizontal drawing bounds, in scene units. This is deliberately wider than the hit
 * box: lying/spinning figures, potion sizes, carried clothes, shadows and thought bubbles can extend
 * beyond it. Only distant bodies are rejected; there is no vertical culling of flying or jumping art.
 * Active name tags stay in the list because their width depends on measured text.
 */
internal fun bodyMayReachViewport(body: Body, camera: Float, viewport: Float): Boolean {
    if (body.held || body is Person && body.anim.nameTag > 0f) return true
    val reach = max(body.w, body.h) * 2f + 0.5f + abs(body.vx) * 0.05f
    return body.x + reach >= camera && body.x - reach <= camera + viewport
}
