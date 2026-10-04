package app.trollfoss.ui.play

import kotlin.math.hypot

/** What the corner by the bag offers while something is held: the bag for things and figures, a crate for furniture. */
enum class AwayPicture { BAG, CRATE }

/**
 * The put-away corner. The screen edges carry the camera along, so «away» lives where the bag already is:
 * while the child holds something from the scene the bag grows, and whatever is let go inside goes into the
 * bag (things, figures) or the store (furniture). No panel has to be open.
 */
object AwayCorner {
    /** How much bigger the bag is drawn while something is held. */
    const val DRAW = 1.6f

    /** The touch radius in bag radii, for what was lifted from the scene. */
    const val HIT = DRAW

    /** The bag's own touch radius: what comes out of the bag tray keeps it, so a tap in the tray still takes a thing out. */
    const val BAG_ONLY = 1.4f

    /** Seconds the bag takes to grow and to shrink back. */
    const val GROW = 0.18f

    fun reach(fromScene: Boolean): Float = if (fromScene) HIT else BAG_ONLY

    fun contains(fingerX: Float, fingerY: Float, centerX: Float, centerY: Float, bagRadius: Float, fromScene: Boolean): Boolean {
        val shift = if (fromScene) bagRadius * (DRAW - 1f) else 0f
        return hypot(fingerX - (centerX - shift), fingerY - (centerY - shift)) < bagRadius * reach(fromScene)
    }

    /** Null while nothing from the scene is held; the crate only when everything held is furniture. */
    fun picture(bodies: Int, furniture: Int): AwayPicture? = when {
        bodies > 0 -> AwayPicture.BAG
        furniture > 0 -> AwayPicture.CRATE
        else -> null
    }

    /** Furniture let go outside the corner but in the lower right quarter of the screen: the corner wobbles once, as a hint. */
    fun nearMiss(fingerX: Float, fingerY: Float, width: Float, height: Float): Boolean =
        fingerX > width * 0.5f && fingerY > height * 0.5f
}
