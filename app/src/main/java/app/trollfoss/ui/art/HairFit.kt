package app.trollfoss.ui.art

import app.trollfoss.domain.Look

/** Dimensions in head radii. The temples and hairline stay fixed when the hair changes. */
internal class HairFit(look: Look) {
    val headWidth = when (look.face) { 1 -> 0.925f; 3 -> 1.09f; else -> 1f }
    val headHeight = when (look.face) { 1 -> 1.04f; 2 -> 0.96f; 3 -> 0.95f; else -> 1.01f }
    private val fullness = (look.hairSize.coerceIn(0.8f, 1.5f) - 0.8f) / 0.7f
    private val length = (look.hairLength.coerceIn(0.65f, 1.6f) - 0.65f) / 0.95f
    val outerWidth = 1.04f + fullness * 0.38f
    val crown = 1.04f + fullness * 0.35f
    val end = 0.15f + length * 2.1f
    val shortEnd = -0.2f + length * 0.8f
    val tuft = 0.18f + length * 0.85f
    val lockWidth = 0.18f + fullness * 0.25f
    val fringe = -0.78f + length * 0.18f
}
