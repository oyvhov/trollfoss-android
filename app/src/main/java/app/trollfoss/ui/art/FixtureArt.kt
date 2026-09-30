package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Thing

/**
 * Draws the back layer of a fixture — its body, and its inside when it is open — with the origin at
 * the fixture's bottom centre. [u] is pixels per scene unit, so the fixture is spec.w × spec.h units.
 * [contents] are the things a machine has taken (blender, cauldron), for machines that show them.
 */
fun DrawScope.drawFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing> = emptyList()) {
    val w = f.spec.w * u
    val h = f.spec.h * u
    val color = if (f.spec.wall) Color(0xFFE8D5B5) else Color(0xFFC98A55)
    inkedRound(Rect(-w / 2, -h, w / 2, 0f), minOf(w, h) * 0.12f, color, pen)
    if (f.open) inkedRound(Rect(-w / 2 + w * 0.1f, -h * 0.9f, w / 2 - w * 0.1f, -h * 0.05f), minOf(w, h) * 0.08f, color.darken(0.35f), pen)
}

/** Draws the part of a fixture that sits in front of whoever uses it (a blanket, a bath side, glass). */
fun DrawScope.drawFixtureFront(f: Fixture, u: Float, pen: Pen) {
    if (!f.spec.front) return
    val w = f.spec.w * u
    val h = f.spec.h * u
    inkedRound(Rect(-w / 2, -h * 0.45f, w / 2, 0f), h * 0.1f, Color(0xFF4AB3FF), pen)
}
