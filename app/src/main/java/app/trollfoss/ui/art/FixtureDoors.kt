package app.trollfoss.ui.art

import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType

/** The same projected panel describes both a painted open door and its touch area. */
internal data class DoorPanel(
    val hinge: Float, val top: Float, val bottom: Float,
    val width: Float, val side: Float, val angle: Float,
) {
    val edge = fxDoorVec(1f, width, side, angle)

    fun contains(x: Float, y: Float, margin: Float): Boolean {
        if (x < minOf(hinge, hinge + edge.x) - margin || x > maxOf(hinge, hinge + edge.x) + margin) return false
        val along = if (kotlin.math.abs(edge.x) < 0.0001f) 0.5f else ((x - hinge) / edge.x).coerceIn(0f, 1f)
        return y in (top + edge.y * along - margin)..(bottom + edge.y * along + margin)
    }
}

internal object FixtureDoors {
    val fridge = DoorPanel(-0.085f, -0.352f, -0.016f, 0.17f, -1f, 118f)
    val wardrobeLeft = DoorPanel(-0.12f, -0.358f, -0.028f, 0.106f, -1f, 128f)
    val wardrobeRight = wardrobeLeft.copy(hinge = 0.12f, side = 1f)
    val oven = DoorPanel(-0.085f, -0.182f, -0.022f, 0.17f, -1f, 115f)
    val grandFridge = DoorPanel(-0.1f, -0.42f, -0.03f, 0.2f, -1f, 120f)
    val jamLeft = DoorPanel(-0.11f, -0.19f, -0.025f, 0.11f, -1f, 115f)
    val jamRight = jamLeft.copy(hinge = 0.11f, side = 1f)
    val sideboardLeft = DoorPanel(-0.15f, -0.215f, -0.045f, 0.15f, -1f, 118f)
    val sideboardRight = sideboardLeft.copy(hinge = 0.15f, side = 1f)
    val upstairsLeft = DoorPanel(-0.162f, -0.447f, -0.042f, 0.152f, -1f, 128f)
    val upstairsRight = upstairsLeft.copy(hinge = 0.162f, side = 1f)

    private val panels = mapOf(
        FixtureType.FRIDGE to listOf(fridge),
        FixtureType.WARDROBE to listOf(wardrobeLeft, wardrobeRight),
        FixtureType.OVEN to listOf(oven),
        FixtureType.GR_FRIDGE to listOf(grandFridge),
        FixtureType.GR_JAM_CABINET to listOf(jamLeft, jamRight),
        FixtureType.GR_SIDEBOARD to listOf(sideboardLeft, sideboardRight),
        FixtureType.UP_WARDROBE to listOf(upstairsLeft, upstairsRight),
    )

    fun hit(f: Fixture, x: Float, y: Float, margin: Float): Boolean =
        f.open && panels[f.type]?.any { it.contains(x, y, margin) } == true
}
