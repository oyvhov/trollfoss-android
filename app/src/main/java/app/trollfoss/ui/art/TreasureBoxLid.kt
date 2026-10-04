package app.trollfoss.ui.art

import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType

/** The painted upright lid, including its projected top and side, also takes taps and holds. */
internal object TreasureBoxLid {
    const val LEFT = -0.134f
    const val RIGHT = 0.134f
    const val TOP = -0.33f
    const val BOTTOM = -0.2f
    const val DEPTH = 0.016f
    const val Z = 0.096f
    const val STAR_Y = (TOP + BOTTOM) / 2f

    private val face = Doorway(LEFT + FX_DX * Z, TOP + FX_DY * (Z + DEPTH),
        RIGHT + FX_DX * (Z + DEPTH), BOTTOM + FX_DY * Z)

    fun hit(f: Fixture, x: Float, y: Float, margin: Float): Boolean =
        f.type == FixtureType.TREASURE_BOX && f.open && face.contains(x, y, margin)
}
