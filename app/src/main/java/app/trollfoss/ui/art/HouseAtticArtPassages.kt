package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.sin

/*
 * Storhuset, attic: the two ways out. The stairs down are a hole in the floor with a banister and a warm glow
 * from the first floor; the secret door is the back of the library bookcase, locked until the lever is pulled
 * down in the library (f.mode 1 when it can be used).
 */

internal fun DrawScope.atStairs(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    atShadow(u, 0.7f, 0.24f, 0.4f)
    val oak = Color(0xFFC49A62)
    // The boards around the opening, then the opening itself.
    fxFace(fxFlat(u, -0.37f, 0.37f, 0f, 0.0f, 0.27f, 0.004f), oak, pen)
    val hole = fxFlat(u, -0.3f, 0.3f, 0f, 0.05f, 0.22f)
    drawPath(hole, Color(0xFF2B1E28))
    clipPath(hole) {
        // The steps going down, nearest first; the last ones fade into the glow of the room below.
        for (k in 5 downTo 0) {
            val z0 = 0.05f + k * 0.028f
            val shade = lerp(Color(0xFFD7A66B), Color(0xFF5A3B2E), k / 5f)
            drawPath(fxFlat(u, -0.27f, 0.27f, k * 0.02f, z0, z0 + 0.026f), shade)
            drawPath(fxFlat(u, -0.27f, 0.27f, k * 0.02f, z0, z0 + 0.026f), Ink.line, alpha = 0.55f, style = pen.thin)
        }
        fxGlow(q(0f, 0.05f, 0.2f), 0.33f * u, AtC.warm, 0.75f)
    }
    drawPath(hole, Ink.line, style = pen.stroke)
    // Newel posts with brass knobs, balusters and a handrail along the front and the right side.
    val rail = Color(0xFF8A5A3A)
    for (x in listOf(-0.33f, 0.33f)) {
        fxPost(u, x, 0.03f, 0f, -0.2f, 0.014f, rail, pen)
        val top = q(x, -0.2f, 0.03f)
        atDot(top, 0.017f * u, AtC.brass, pen)
        shine(Offset(top.x - 0.005f * u, top.y - 0.006f * u), 0.006f * u, 0.004f * u, 0.8f)
    }
    for (k in 0 until 7) {
        val x = -0.27f + k * 0.09f
        fxPost(u, x, 0.03f, 0f, -0.13f, 0.0065f, oak.darken(0.1f), pen)
    }
    capsule(q(-0.33f, -0.15f, 0.03f), q(0.33f, -0.15f, 0.03f), 0.014f * u, rail.lighten(0.12f), pen)
    for (z in listOf(0.1f, 0.17f)) fxPost(u, 0.33f, z, 0f, -0.12f, 0.0065f, oak.darken(0.1f), pen)
    capsule(q(0.33f, -0.14f, 0.03f), q(0.33f, -0.14f, 0.24f), 0.012f * u, rail.lighten(0.12f), pen)
    fxPost(u, 0.33f, 0.24f, 0f, -0.18f, 0.012f, rail, pen)
    // A golden arrow over the opening: «down here». (Still, so the picture is drawn once.)
    for (k in 0..1) {
        val c = Offset(0f, (-0.32f - k * 0.035f) * u)
        val v = Path().apply {
            moveTo(c.x - 0.04f * u, c.y - 0.026f * u)
            lineTo(c.x, c.y)
            lineTo(c.x + 0.04f * u, c.y - 0.026f * u)
        }
        val a = 0.95f - k * 0.4f
        drawPath(v, Ink.line, alpha = a, style = Stroke(0.02f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(v, AtC.brassLight, alpha = a, style = Stroke(0.014f * u, cap = StrokeCap.Round))
    }
}

internal fun DrawScope.atSecretDoor(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = 0.7f
    val usable = f.mode == 1
    atShadow(u, 0.34f, 0.1f, 0.6f)
    fun arch(hw: Float, top: Float): Path = Path().apply {
        moveTo(-hw * u, 0f)
        lineTo(-hw * u, (top + 0.1f) * u)
        quadraticTo(-hw * u, top * u, 0f, top * u)
        quadraticTo(hw * u, top * u, hw * u, (top + 0.1f) * u)
        lineTo(hw * u, 0f)
        close()
    }
    // A carved frame with gold studs; the opening is dark or full of warm light.
    val frame = arch(0.15f, -0.455f)
    drawPath(frame, AtC.mahogany)
    drawPath(frame, Ink.line, style = pen.stroke)
    val opening = arch(0.115f, -0.425f)
    drawPath(opening, Color(0xFF2A1218))
    clipPath(opening) {
        if (usable) {
            // The library behind: golden lamplight, a bookshelf, and motes of dust that rise.
            drawRect(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFFFE9A8), Color(0xFFE7A94A)), startY = -0.43f * u, endY = 0f), p(-0.12f, -0.44f), androidx.compose.ui.geometry.Size(0.24f * u, 0.44f * u))
            val spines = listOf(Color(0xFF3F8F6C), Color(0xFFD2443A), Color(0xFF2F6FB8), Color(0xFFE8B04A), Color(0xFF8B5CF6), Color(0xFFD2443A), Color(0xFF3F8F6C))
            for ((i, c) in spines.withIndex()) {
                val h = 0.09f + 0.03f * ((i * 5) % 3)
                drawRect(c, p(-0.115f + i * 0.033f, -h - 0.04f), androidx.compose.ui.geometry.Size(0.03f * u, h * u))
                drawRect(Ink.line, p(-0.115f + i * 0.033f, -h - 0.04f), androidx.compose.ui.geometry.Size(0.03f * u, h * u), style = pen.thin)
            }
            drawRect(Color(0xFF7A4A2A), p(-0.12f, -0.04f), androidx.compose.ui.geometry.Size(0.24f * u, 0.04f * u))
            for (k in 0 until 5) {
                val ph = fxFrac(t * 0.25f + k * 0.2f)
                twinkle(p(-0.08f + k * 0.04f + sin(t + k) * 0.01f, -0.05f - ph * 0.32f), 0.008f * u * (1f - ph), Color.White, 1f - ph)
            }
        } else {
            // The door is shut tight: boards, an iron band, a brass lock plate with a keyhole and a lever.
            drawRect(Color(0xFF6B3E2E), p(-0.12f, -0.44f), androidx.compose.ui.geometry.Size(0.24f * u, 0.44f * u))
            for (k in 1..3) drawLine(Ink.line, p(-0.12f + k * 0.06f, -0.44f), p(-0.12f + k * 0.06f, 0f), pen.lw * 0.6f, alpha = 0.6f)
            for (y in listOf(-0.34f, -0.1f)) {
                drawRect(Color(0xFF3A3844), p(-0.12f, y - 0.014f), androidx.compose.ui.geometry.Size(0.24f * u, 0.028f * u))
                for (x in listOf(-0.09f, -0.03f, 0.03f, 0.09f)) drawCircle(AtC.brassLight, 0.004f * u, p(x, y))
            }
        }
    }
    drawPath(opening, Ink.line, style = pen.stroke)
    if (!usable) {
        // The lock: a brass plate, a round keyhole and a tiny glint that hints at something behind it.
        val c = p(0f, -0.2f)
        inkedRound(Rect(c.x - 0.04f * u, c.y - 0.05f * u, c.x + 0.04f * u, c.y + 0.05f * u), 0.012f * u, AtC.brass, pen)
        drawCircle(Ink.line, 0.008f * u, p(0f, -0.21f))
        drawLine(Ink.line, p(0f, -0.21f), p(0f, -0.17f), 0.006f * u, StrokeCap.Round)
        shine(p(-0.018f, -0.235f), 0.012f * u, 0.006f * u, 0.9f)
        twinkle(p(0.025f, -0.24f), 0.013f * u, Color.White, 0.9f)
    } else {
        // Gold light spills over the sill.
        fxGlow(p(0f, -0.02f), 0.2f * u, AtC.warm, 0.6f)
    }
    // Studs along the frame and a carved star at the top.
    for (x in listOf(-0.135f, 0.135f)) for (y in listOf(-0.06f, -0.16f, -0.26f)) drawCircle(AtC.gold, 0.005f * u, p(x, y))
    drawPath(starPath(p(0f, -0.44f), 0.014f * u, 0.006f * u), AtC.gold)
    drawPath(starPath(p(0f, -0.44f), 0.014f * u, 0.006f * u), Ink.line, style = pen.thin)
}
