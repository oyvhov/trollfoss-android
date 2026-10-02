package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import app.trollfoss.domain.Fixture

/** Vagstaddalen photo: long red-brown plank wall, blue divided windows, red trim and a low turf roof. */
internal fun DrawScope.drawValleyCabin(f: Fixture, u: Float, pen: Pen) {
    scale(1f, 0.75f, pivot = Offset.Zero) { valleyCabinFace(f, u, pen) }
}

private fun DrawScope.valleyCabinFace(f: Fixture, u: Float, pen: Pen) {
    val wall = Color(0xFF794E48).atNight(pen.night, 0.55f)
    val red = Color(0xFFB84439).atNight(pen.night, 0.55f)
    val blue = Color(0xFF559DCC).atNight(pen.night, 0.5f)
    val turf = pen.ground(Color(0xFF8A9D59)).atNight(pen.night, 0.5f)
    groundShadow(0f, 0.01f * u, 0.48f * u)
    fxBox(u, -0.43f, -0.50f, 0.43f, 0f, 0.14f, wall, pen, top = wall.lighten(0.12f), side = wall.darken(0.16f))
    for (k in 1..8) drawLine(wall.darken(0.3f), Offset(-0.43f * u, -k * 0.055f * u), Offset(0.43f * u, -k * 0.055f * u), pen.lw * 0.65f)
    for (x in floatArrayOf(-0.43f, 0.42f)) inkedRound(Rect(x * u, -0.51f * u, (x + 0.025f) * u, 0f), 0.003f * u, red, pen, shade = false)
    for (x in floatArrayOf(-0.31f, 0.12f)) {
        inkedRound(Rect((x - 0.015f) * u, -0.415f * u, (x + 0.185f) * u, -0.15f * u), 0.006f * u, red, pen)
        val r = Rect(x * u, -0.4f * u, (x + 0.17f) * u, -0.165f * u)
        inkedRound(r, 0.004f * u, Color(0xFFC2D9CA).atNight(pen.night, 0.45f), pen, shade = false)
        drawLine(blue, Offset(r.center.x, r.top), Offset(r.center.x, r.bottom), pen.lw * 1.8f)
        for (y in floatArrayOf(-0.322f, -0.245f)) drawLine(blue, Offset(r.left, y * u), Offset(r.right, y * u), pen.lw * 1.8f)
        drawRect(blue, r.topLeft, r.size, style = androidx.compose.ui.graphics.drawscope.Stroke(pen.lw * 1.6f))
    }
    if (f.open) {
        // Opening the wall exposes a usable floor; figures and things remain in the same scene.
        inkedRound(Rect(-0.3f * u, -0.36f * u, 0.3f * u, -0.02f * u), 0.005f * u, Color(0xFFE2BE8A).atNight(pen.night, 0.45f), pen)
        fxBox(u, -0.3f, -0.02f, 0.3f, 0f, 0.10f, FxC.oak, pen)
        for (k in 0..5) drawLine(wall.copy(alpha = 0.3f), Offset((-0.25f + k * 0.1f) * u, -0.35f * u), Offset((-0.25f + k * 0.1f) * u, -0.02f * u), pen.lw * 0.5f)
    }
    // Low grassy roof with a visible red fascia; its front is the physics roof surface.
    fxBox(u, -0.46f, -0.56f, 0.46f, -0.50f, 0.18f, red, pen, top = turf, side = turf.darken(0.15f))
    val roof = fxFlat(u, -0.46f, 0.46f, -0.56f, 0f, 0.18f)
    clipPath(roof) {
        for (k in 0..18) {
            val x = -0.43f + hash01(k, 2081) * 0.86f
            val z = hash01(k, 2082) * 0.18f
            val p = fxQ(u, x, -0.56f, z)
            drawPath(tuftPath(p.x, p.y, 0.016f * u, 0f), turf.darken(0.16f))
        }
    }
    fxBox(u, -0.22f, -0.72f, -0.14f, -0.56f, 0.07f, Color(0xFF535D64), pen, z = 0.09f)
    fxBox(u, -0.23f, -0.735f, -0.13f, -0.715f, 0.09f, Color(0xFFAAB5BC), pen, z = 0.08f)
    // Small side gable, with the blue shutter from the reference.
    val gable = Path().apply {
        moveTo(-0.46f * u, -0.50f * u); lineTo(-0.39f * u, -0.62f * u)
        lineTo(-0.28f * u, -0.54f * u); close()
    }
    inked(gable, red, pen, shade = false)
    inkedRound(Rect(-0.46f * u, -0.39f * u, -0.40f * u, -0.17f * u), 0.004f * u, blue, pen)
}
