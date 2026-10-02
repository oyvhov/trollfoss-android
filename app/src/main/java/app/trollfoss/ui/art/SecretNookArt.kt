package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId

/** The red book conceals a room, with a real seat and shelf aligned to the physical surfaces. */
internal fun DrawScope.drawSecretNook(f: Fixture, u: Float, pen: Pen) {
    val wood = Color(0xFFAD7456)
    val rock = f.place == PlaceId.VAGSTADDALEN
    if (rock) {
        val cover = Path().apply {
            moveTo(-0.31f * u, 0f); lineTo(-0.32f * u, -0.25f * u)
            lineTo(-0.22f * u, -0.44f * u); lineTo(0.07f * u, -0.49f * u)
            lineTo(0.29f * u, -0.39f * u); lineTo(0.33f * u, -0.17f * u)
            lineTo(0.31f * u, 0f); close()
        }
        inked(cover, Color(0xFF9FA6A0).atNight(pen.night, 0.5f), pen)
        if (!f.open) {
            drawLine(Ink.line.copy(alpha = 0.35f), Offset(-0.20f * u, -0.30f * u), Offset(0.06f * u, -0.25f * u), pen.lw)
            drawLine(Ink.line.copy(alpha = 0.35f), Offset(0.06f * u, -0.25f * u), Offset(0.15f * u, -0.12f * u), pen.lw)
            inkedRound(Rect(0.18f * u, -0.40f * u, 0.23f * u, -0.31f * u), 0.003f * u, Color(0xFFD44747), pen)
            return
        }
    } else fxBox(u, -0.31f, -0.48f, 0.31f, 0f, 0.055f, wood, pen)
    inkedRound(Rect(-0.27f * u, -0.45f * u, 0.27f * u, -0.02f * u), 0.012f * u,
        if (f.open) Color(0xFF719EA7) else wood.darken(0.3f), pen)
    if (!f.open) {
        for (row in 0..2) {
            val y = -0.05f - row * 0.13f
            for (i in 0..6) {
                val x = -0.245f + i * 0.07f
                val color = if (i == 6 && row == 2) Color(0xFFD44747) else
                    listOf(Color(0xFF88B6AA), Color(0xFFD1AF75), Color(0xFF849BC2))[(i + row + f.variant).mod(3)]
                inkedRound(Rect(x * u, (y - 0.09f) * u, (x + 0.052f) * u, y * u), 0.003f * u, color, pen, shade = false)
            }
            drawLine(Ink.line, Offset(-0.27f * u, y * u), Offset(0.27f * u, y * u), pen.lw)
        }
    } else {
        fxBox(u, -0.27f, -0.02f, 0.27f, 0f, 0.04f, Color(0xFFE3B27A), pen)
        fxBox(u, -0.23f, -0.07f, -0.07f, -0.02f, 0.04f, Color(0xFFB87A9F), pen)
        fxBox(u, 0.13f, -0.15f, 0.25f, -0.13f, 0.04f, wood, pen)
        inkedCircle(Offset(0.18f * u, -0.35f * u), 0.042f * u, Color(0xFFFFDE87), pen)
        for (k in 0..4) drawCircle(Color(0xFFFFE7A0), 0.005f * u, Offset((-0.20f + k * 0.08f) * u, (-0.41f + (k % 2) * 0.035f) * u))
    }
}
