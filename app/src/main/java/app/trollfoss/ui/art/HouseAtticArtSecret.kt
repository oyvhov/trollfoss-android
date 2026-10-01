package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import app.trollfoss.domain.Fixture
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/*
 * Storhuset, attic: the secret room. The treasure map table, a chest that showers coins, a globe that spins,
 * the family tree with six portraits that wink, a chandelier and two candle sconces. Gold, velvet and warmth.
 */

// ------------------------------------------------------------------------------------------ map table

internal fun DrawScope.atMapTable(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    fun mp(x: Float, z: Float) = q(x, -0.17f, z)
    val t = pen.t
    val wood = AtC.mahogany.lighten(0.1f)
    val mode = f.mode
    atShadow(u, 0.42f, 0.2f)
    // Turned legs: back pair first, then the front pair, with a stretcher.
    for (x in listOf(-0.18f, 0.18f)) fxPost(u, x, 0.17f, 0f, -0.15f, 0.011f, wood.darken(0.15f), pen)
    for (x in listOf(-0.18f, 0.18f)) {
        fxPost(u, x, 0.03f, 0f, -0.15f, 0.013f, wood, pen)
        atDot(q(x, -0.075f, 0.03f), 0.014f * u, wood.lighten(0.1f), pen)
    }
    // The top slab with a gold line along its front edge, and an apron below it.
    fxBox(u, -0.2f, -0.17f, 0.2f, -0.145f, 0.2f, wood, pen, rad = 0.004f, top = wood.lighten(0.14f))
    drawLine(AtC.gold, p(-0.19f, -0.152f), p(0.19f, -0.152f), 0.003f * u)
    fxBox(u, -0.17f, -0.145f, 0.17f, -0.115f, 0.02f, wood.darken(0.1f), pen, rad = 0.003f, z = 0.02f)
    atDot(p(0f, -0.13f), 0.007f * u, AtC.gold, pen)
    for (x in listOf(-0.1f, 0.1f)) drawLine(AtC.gold, p(x - 0.03f, -0.13f), p(x + 0.03f, -0.13f), 0.003f * u)
    // An inkwell and a feather quill at the back corner.
    val ink = mp(0.15f, 0.05f)
    drawOval(Color(0xFF241A38), Offset(ink.x - 0.015f * u, ink.y - 0.02f * u), Size(0.03f * u, 0.03f * u))
    drawOval(Ink.line, Offset(ink.x - 0.015f * u, ink.y - 0.02f * u), Size(0.03f * u, 0.03f * u), style = pen.thin)
    drawLine(Ink.line, Offset(ink.x + 0.002f * u, ink.y - 0.012f * u), Offset(ink.x + 0.03f * u, ink.y - 0.075f * u), 0.005f * u + pen.lw, StrokeCap.Round)
    val quill = Path().apply { moveTo(ink.x + 0.003f * u, ink.y - 0.014f * u); quadraticTo(ink.x + 0.045f * u, ink.y - 0.06f * u, ink.x + 0.035f * u, ink.y - 0.09f * u); quadraticTo(ink.x + 0.01f * u, ink.y - 0.06f * u, ink.x + 0.003f * u, ink.y - 0.014f * u); close() }
    inked(quill, Color(0xFFF7F3EC), pen, shade = false)
    if (mode == 0) {
        // The map is rolled up: a scroll with a green ribbon and a gold seal.
        capsule(mp(-0.1f, 0.1f), mp(0.07f, 0.09f), 0.036f * u, Color(0xFFE9D3A0), pen)
        for (x in listOf(-0.1f, 0.07f)) {
            val e = mp(x, if (x < 0f) 0.1f else 0.09f)
            drawOval(Color(0xFFD1B47A), Offset(e.x - 0.01f * u, e.y - 0.017f * u), Size(0.02f * u, 0.034f * u))
            drawOval(Ink.line, Offset(e.x - 0.01f * u, e.y - 0.017f * u), Size(0.02f * u, 0.034f * u), style = pen.thin)
            drawCircle(Color(0xFF8A6A48), 0.004f * u, e)
        }
        val mid = mp(-0.02f, 0.095f)
        drawRect(Color(0xFF3BC46B), Offset(mid.x - 0.007f * u, mid.y - 0.019f * u), Size(0.014f * u, 0.038f * u))
        drawRect(Ink.line, Offset(mid.x - 0.007f * u, mid.y - 0.019f * u), Size(0.014f * u, 0.038f * u), style = pen.thin)
        atDot(Offset(mid.x, mid.y + 0.005f * u), 0.008f * u, AtC.gold, pen)
        val g = atWave(t, 2f)
        if (g > 0.6f) twinkle(Offset(mid.x + 0.02f * u, mid.y - 0.025f * u), 0.014f * u * g, Color.White, g)
        return
    }
    // The map unrolled across the table: an old parchment with a sea, an island, mountains and a dotted path.
    val sheet = fxQuad(mp(-0.17f, 0.03f).x, mp(-0.17f, 0.03f).y, mp(0.12f, 0.03f).x, mp(0.12f, 0.03f).y, mp(0.12f, 0.17f).x, mp(0.12f, 0.17f).y, mp(-0.17f, 0.17f).x, mp(-0.17f, 0.17f).y)
    drawPath(sheet, Color(0xFFEAD7A6))
    clipPath(sheet) {
        drawRect(Color(0xFFBFD9D6), mp(-0.17f, 0.03f), Size(0.29f * u + 0.07f * u, 0.14f * u))
        // islands
        val island = Path().apply {
            moveTo(mp(-0.14f, 0.1f).x, mp(-0.14f, 0.1f).y)
            quadraticTo(mp(-0.12f, 0.15f).x, mp(-0.12f, 0.15f).y, mp(-0.08f, 0.13f).x, mp(-0.08f, 0.13f).y)
            quadraticTo(mp(-0.02f, 0.16f).x, mp(-0.02f, 0.16f).y, mp(0.0f, 0.1f).x, mp(0.0f, 0.1f).y)
            quadraticTo(mp(-0.04f, 0.05f).x, mp(-0.04f, 0.05f).y, mp(-0.1f, 0.06f).x, mp(-0.1f, 0.06f).y)
            close()
        }
        drawPath(island, Color(0xFFE9D3A0))
        drawPath(island, Color(0xFF8A6A48), style = pen.thin)
        val island2 = Path().apply { addOval(Rect(mp(0.04f, 0.1f).x, mp(0.04f, 0.14f).y, mp(0.1f, 0.1f).x, mp(0.04f, 0.07f).y)) }
        drawPath(island2, Color(0xFFD9C48A))
        drawPath(island2, Color(0xFF8A6A48), style = pen.thin)
        // mountains and a tree
        for ((x, z) in listOf(-0.07f to 0.11f, -0.045f to 0.1f)) {
            val a = mp(x, z)
            drawPath(Path().apply { moveTo(a.x - 0.012f * u, a.y); lineTo(a.x, a.y - 0.022f * u); lineTo(a.x + 0.012f * u, a.y); close() }, Color(0xFF8E9BB4))
            drawPath(Path().apply { moveTo(a.x - 0.012f * u, a.y); lineTo(a.x, a.y - 0.022f * u); lineTo(a.x + 0.012f * u, a.y); close() }, Ink.line, style = pen.thin)
        }
        val tr = mp(-0.12f, 0.09f)
        drawCircle(Color(0xFF3F8F6C), 0.008f * u, Offset(tr.x, tr.y - 0.012f * u))
        drawLine(Color(0xFF6E4630), tr, Offset(tr.x, tr.y - 0.006f * u), pen.lw)
        // the dotted path, longer with every tap, ending in a green heart that glows when it is found
        val route = listOf(mp(-0.12f, 0.12f), mp(-0.08f, 0.14f), mp(-0.04f, 0.12f), mp(0.0f, 0.15f), mp(0.04f, 0.12f), mp(0.07f, 0.1f))
        val upTo = when (mode) { 1 -> 0; 2 -> 2; 3 -> 4; else -> 5 }
        for (k in 0 until upTo) {
            for (s in 0..2) {
                val a = Offset(route[k].x + (route[k + 1].x - route[k].x) * s / 3f, route[k].y + (route[k + 1].y - route[k].y) * s / 3f)
                drawCircle(Color(0xFF3F8F6C), 0.0035f * u, a)
            }
        }
        // the compass rose in the corner
        val rose = mp(0.095f, 0.15f)
        for (k in 0 until 4) {
            val a = k * 1.5708f
            drawLine(Color(0xFF8A6A48), rose, Offset(rose.x + cos(a) * 0.014f * u, rose.y + sin(a) * 0.01f * u), pen.lw * 0.8f)
        }
        drawCircle(Color(0xFF8A6A48), 0.003f * u, rose)
        // the end of the path: a green heart
        val goal = route[5]
        val glow = if (mode >= 4) atWave(t, 4f) else 0.2f
        if (mode >= 4) fxGlow(goal, 0.05f * u, Color(0xFF8FE3B5), 0.6f * glow + 0.3f)
        drawPath(fxHeart(goal.x, goal.y, 0.009f * u * (1f + 0.15f * glow)), Color(0xFF3BC46B))
        drawPath(fxHeart(goal.x, goal.y, 0.009f * u * (1f + 0.15f * glow)), Ink.line, style = pen.thin)
    }
    drawPath(sheet, Ink.line, style = pen.stroke)
    // Curled corners and two brass weights holding it flat.
    for (x in listOf(-0.15f, 0.1f)) atDot(mp(x, 0.04f), 0.01f * u, AtC.brass, pen)
}

// ------------------------------------------------------------------------------------------ treasure chest

private fun chestTop(x: Float): Float = -0.115f - 0.06f * (1f - (x / 0.145f) * (x / 0.145f))

internal fun DrawScope.atTreasureChest(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.14f
    val wood = Color(0xFF6B3E2E)
    atShadow(u, 0.32f, 0.15f)
    if (f.open) {
        // The lid stands open behind; its inside is padded velvet.
        val lid = Path().apply {
            val a = q(-0.145f, -0.1f, d)
            moveTo(a.x, a.y)
            val b = q(-0.145f, -0.2f, d)
            lineTo(b.x, b.y)
            val e = q(-0.145f, -0.29f, d)
            val c = q(0f, -0.31f, d)
            quadraticTo(e.x, e.y, c.x, c.y)
            val g = q(0.145f, -0.29f, d)
            val h = q(0.145f, -0.2f, d)
            quadraticTo(g.x, g.y, h.x, h.y)
            val i = q(0.145f, -0.1f, d)
            lineTo(i.x, i.y)
            close()
        }
        drawPath(lid, wood.darken(0.15f))
        clipPath(lid) {
            drawRect(Brush.verticalGradient(listOf(AtC.velvetLight, AtC.velvetDark), startY = q(0f, -0.31f, d).y, endY = q(0f, -0.1f, d).y), q(-0.15f, -0.31f, d), Size(0.3f * u, 0.22f * u))
            for (k in 0 until 8) drawCircle(AtC.gold, 0.006f * u, q(-0.11f + (k % 4) * 0.075f, -0.14f - (k / 4) * 0.09f, d), alpha = 0.8f)
        }
        drawPath(lid, AtC.gold, style = Stroke(pen.lw * 2f))
        drawPath(lid, Ink.line, style = pen.stroke)
        fxBox(u, -0.14f, -0.105f, 0.14f, -0.01f, d, wood, pen, rad = 0.006f, top = Color(0xFF3A1E14))
        // A mound of coins, jewels and a golden glow.
        fxGlow(q(0f, -0.15f, 0.07f), 0.22f * u, AtC.warm, 0.55f + 0.1f * sin(t * 4f))
        for (k in 0 until 14) {
            val x = -0.1f + (k % 7) * 0.033f
            val z = 0.035f + (k / 7) * 0.05f
            val h = -0.105f - 0.03f * (1f - abs(x) / 0.11f) - (k / 7) * 0.012f
            val c = q(x, h, z)
            drawOval(AtC.goldDark, Offset(c.x - 0.016f * u, c.y - 0.007f * u), Size(0.032f * u, 0.017f * u))
            drawOval(AtC.gold, Offset(c.x - 0.016f * u, c.y - 0.01f * u), Size(0.032f * u, 0.017f * u))
            drawOval(Ink.line, Offset(c.x - 0.016f * u, c.y - 0.01f * u), Size(0.032f * u, 0.017f * u), style = pen.thin)
            drawOval(AtC.goldLight, Offset(c.x - 0.008f * u, c.y - 0.008f * u), Size(0.011f * u, 0.005f * u))
        }
        for ((k, col) in listOf(Color(0xFF4AB3FF), Color(0xFF3BC46B), Color(0xFFFF9EC7), Color(0xFFB9A2F0)).withIndex()) {
            val c = q(-0.07f + k * 0.045f, -0.14f - (k % 2) * 0.012f, 0.05f + (k % 2) * 0.03f)
            val gem = Path().apply { moveTo(c.x, c.y - 0.014f * u); lineTo(c.x + 0.012f * u, c.y); lineTo(c.x, c.y + 0.012f * u); lineTo(c.x - 0.012f * u, c.y); close() }
            inked(gem, col, pen, shade = false)
            drawLine(Color.White.copy(alpha = 0.7f), Offset(c.x - 0.004f * u, c.y - 0.006f * u), Offset(c.x, c.y - 0.01f * u), pen.lw * 0.8f)
            twinkle(Offset(c.x + 0.01f * u, c.y - 0.012f * u), 0.014f * u * atWave(t, 3f, k * 1.5f), Color.White, 0.95f)
        }
        fxBox(u, -0.14f, -0.105f, 0.14f, -0.01f, 0.02f, wood, pen, rad = 0.006f, top = Color(0xFF3A1E14))
        for (sx in listOf(-0.09f, 0.0f, 0.09f)) drawRect(AtC.gold, p(sx - 0.007f, -0.105f), Size(0.014f * u, 0.095f * u), alpha = 0.95f)
        return
    }
    fxBox(u, -0.14f, -0.1f, 0.14f, -0.01f, d, wood, pen, rad = 0.006f)
    val n = 10
    val front = Array(n + 1) { i -> val x = -0.145f + 0.29f * i / n; q(x, chestTop(x), 0f) }
    val back = Array(n + 1) { i -> val x = -0.145f + 0.29f * i / n; q(x, chestTop(x), d) }
    val side = Path().apply {
        val a = q(0.145f, -0.1f, 0f)
        moveTo(a.x, a.y)
        val b = q(0.145f, chestTop(0.145f), 0f)
        lineTo(b.x, b.y)
        val c = q(0.145f, chestTop(0.145f), d)
        lineTo(c.x, c.y)
        val e = q(0.145f, -0.1f, d)
        lineTo(e.x, e.y)
        close()
    }
    fxFace(side, wood.darken(0.25f), pen)
    val top = Path().apply {
        moveTo(front[0].x, front[0].y)
        for (i in 1..n) lineTo(front[i].x, front[i].y)
        for (i in n downTo 0) lineTo(back[i].x, back[i].y)
        close()
    }
    drawPath(top, wood.lighten(0.14f))
    drawPath(top, Ink.line, style = pen.stroke)
    val face = Path().apply {
        val a = q(-0.145f, -0.1f, 0f)
        moveTo(a.x, a.y)
        for (i in 0..n) lineTo(front[i].x, front[i].y)
        val b = q(0.145f, -0.1f, 0f)
        lineTo(b.x, b.y)
        close()
    }
    inked(face, wood, pen)
    // Gold bands, rivets, corner caps and the big lock with a jewel.
    for (sx in listOf(-0.1f, 0.1f)) {
        drawRect(AtC.gold, p(sx - 0.01f, chestTop(sx)), Size(0.02f * u, (-0.01f - chestTop(sx)) * u))
        drawRect(Ink.line, p(sx - 0.01f, chestTop(sx)), Size(0.02f * u, (-0.01f - chestTop(sx)) * u), style = pen.thin)
        for (k in 0..3) drawCircle(AtC.goldLight, 0.003f * u, p(sx, chestTop(sx) + 0.015f + k * 0.03f))
    }
    drawLine(Ink.line, p(-0.14f, -0.1f), p(0.14f, -0.1f), pen.lw * 1.1f)
    drawLine(AtC.gold, p(-0.14f, -0.098f), p(0.14f, -0.098f), 0.006f * u)
    for (sx in listOf(-0.14f, 0.14f)) inkedRound(Rect(p(sx - 0.012f, -0.03f).x, p(sx, -0.03f).y, p(sx + 0.012f, 0f).x, p(sx, -0.004f).y), 0.004f * u, AtC.gold, pen, shade = false)
    val lock = Rect(p(-0.024f, -0.13f).x, p(0f, -0.13f).y, p(0.024f, 0f).x, p(0f, -0.07f).y)
    inkedRound(lock, 0.006f * u, AtC.gold, pen)
    drawCircle(Ink.line, 0.006f * u, p(0f, -0.108f))
    drawLine(Ink.line, p(0f, -0.108f), p(0f, -0.092f), 0.004f * u, StrokeCap.Round)
    atDot(p(0f, -0.125f), 0.007f * u, Color(0xFF4AB3FF), pen)
    shine(p(-0.002f, -0.128f), 0.004f * u, 0.003f * u, 0.9f)
    // A thin gleam through the seam: there is something very shiny inside.
    val g = atWave(t, 1.6f)
    drawLine(AtC.goldLight, p(-0.13f, -0.101f), p(0.13f, -0.101f), 0.003f * u, alpha = 0.3f + 0.5f * g)
    twinkle(p(0.08f + 0.03f * sin(t), -0.115f), 0.012f * u * g, Color.White, g)
    for (sx in listOf(-0.12f, 0.12f)) fxBox(u, sx - 0.012f, -0.012f, sx + 0.012f, 0f, 0.02f, AtC.gold, pen)
}

// ------------------------------------------------------------------------------------------ globe

private class Land(val lon: Float, val lat: Float, val w: Float, val h: Float, val color: Color)

private val LANDS = listOf(
    Land(0.2f, 0.3f, 0.5f, 0.45f, Color(0xFF6FAE5A)),
    Land(0.9f, 0.5f, 0.6f, 0.4f, Color(0xFF8DBE5A)),
    Land(1.2f, -0.15f, 0.4f, 0.55f, Color(0xFFC9A55A)),
    Land(2.3f, 0.45f, 0.9f, 0.45f, Color(0xFF6FAE5A)),
    Land(-1.4f, 0.35f, 0.45f, 0.55f, Color(0xFF8DBE5A)),
    Land(-1.3f, -0.4f, 0.35f, 0.6f, Color(0xFF6FAE5A)),
    Land(3.0f, -0.4f, 0.4f, 0.3f, Color(0xFFC9A55A)),
)

internal fun DrawScope.atGlobe(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val a = f.angle
    val cx = 0f
    val cy = -0.2f
    val r = 0.085f * u
    val c = p(cx, cy)
    val wood = Color(0xFF6E4630)
    atShadow(u, 0.2f, 0.11f)
    // A turned wooden stand: three legs and a little brass cup.
    capsule(p(-0.002f, -0.1f), p(-0.075f, -0.004f), 0.012f * u, wood, pen)
    capsule(p(0.002f, -0.1f), p(0.075f, -0.004f), 0.012f * u, wood, pen)
    capsule(p(0f, -0.1f), p(0.004f, -0.006f), 0.013f * u, wood.lighten(0.1f), pen)
    atDot(p(0f, -0.1f), 0.016f * u, AtC.brass, pen)
    val sphere = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    // The sea, the lands moving round as it spins, and the poles with ice.
    rotate(18f, c) {
        drawCircle(Color(0xFF3F86C8), r, c)
        clipPath(sphere) {
            for (l in LANDS) {
                val lonA = l.lon + a
                val cs = cos(lonA)
                if (cs < -0.35f) continue
                val x = c.x + r * cos(l.lat) * sin(lonA)
                val y = c.y - r * sin(l.lat)
                val wr = r * l.w * 0.5f * (0.3f + 0.7f * max(cs, 0f)) * 0.8f
                val hr = r * l.h * 0.5f * 0.7f
                drawOval(l.color, Offset(x - wr, y - hr), Size(wr * 2f, hr * 2f))
                drawOval(Ink.line, Offset(x - wr, y - hr), Size(wr * 2f, hr * 2f), alpha = 0.4f, style = pen.thin)
            }
            drawOval(Color(0xFFF4F8FF), Offset(c.x - r * 0.5f, c.y - r * 1.05f), Size(r, r * 0.3f))
            drawOval(Color(0xFFF4F8FF), Offset(c.x - r * 0.5f, c.y + r * 0.78f), Size(r, r * 0.3f))
            // a green heart pin that marks Trollfoss, and turns with the world
            val pin = a + 0.45f
            if (cos(pin) > 0.1f) {
                val px = c.x + r * 0.8f * sin(pin) * 0.9f
                val py = c.y - r * 0.38f
                drawLine(Ink.line, Offset(px, py), Offset(px, py - r * 0.35f), pen.lw)
                drawPath(fxHeart(px, py - r * 0.42f, r * 0.14f), Color(0xFF3BC46B))
                drawPath(fxHeart(px, py - r * 0.42f, r * 0.14f), Ink.line, style = pen.thin)
            }
            // light from the upper left, a hard shadow on the lower right
            drawCircle(Color(0x33241A38), r, Offset(c.x + r * 0.28f, c.y + r * 0.2f), style = Stroke(r * 0.5f))
        }
        drawCircle(Ink.line, r, c, style = pen.stroke)
    }
    shine(Offset(c.x - r * 0.4f, c.y - r * 0.45f), r * 0.28f, r * 0.15f, 0.65f)
    // The brass meridian arc on the left, and the axis pins.
    drawArc(Ink.line, 100f, 160f, false, Offset(c.x - r * 1.14f, c.y - r * 1.14f), Size(r * 2.28f, r * 2.28f), style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawArc(AtC.brass, 100f, 160f, false, Offset(c.x - r * 1.14f, c.y - r * 1.14f), Size(r * 2.28f, r * 2.28f), style = Stroke(0.012f * u, cap = StrokeCap.Round))
    drawArc(AtC.brassLight, 110f, 60f, false, Offset(c.x - r * 1.14f, c.y - r * 1.14f), Size(r * 2.28f, r * 2.28f), style = Stroke(0.003f * u, cap = StrokeCap.Round), alpha = 0.7f)
    atDot(Offset(c.x - r * 0.45f, c.y - r * 1.11f), 0.008f * u, AtC.brass, pen)
    atDot(Offset(c.x + r * 0.45f, c.y + r * 1.11f), 0.008f * u, AtC.brass, pen)
}

// ------------------------------------------------------------------------------------------ family tree

internal fun DrawScope.atFamilyTree(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val frame = Rect(p(-0.275f, -0.415f).x, p(0f, -0.415f).y, p(0.275f, -0.005f).x, p(0f, -0.005f).y)
    // A gilded frame round a painted canvas: a pale sky, a hill and a big tree.
    drawRoundRect(AtC.gold, frame.topLeft, frame.size, androidx.compose.ui.geometry.CornerRadius(0.014f * u))
    drawRoundRect(Ink.line, frame.topLeft, frame.size, androidx.compose.ui.geometry.CornerRadius(0.014f * u), style = pen.stroke)
    val canvas = Rect(frame.left + 0.014f * u, frame.top + 0.014f * u, frame.right - 0.014f * u, frame.bottom - 0.014f * u)
    clipPath(rectPath(canvas)) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFCDE9F2), Color(0xFFF6EEDC)), startY = canvas.top, endY = canvas.bottom), canvas.topLeft, canvas.size)
        drawOval(Color(0xFF8DBE5A), Offset(canvas.left - 0.05f * u, canvas.bottom - 0.07f * u), Size(canvas.width + 0.1f * u, 0.16f * u))
        // trunk and branches
        val bark = Color(0xFF8A5A3A)
        val trunk = Path().apply {
            moveTo(p(-0.03f, -0.015f).x, p(-0.03f, -0.015f).y)
            quadraticTo(p(-0.015f, -0.1f).x, p(-0.015f, -0.1f).y, p(-0.02f, -0.15f).x, p(-0.02f, -0.15f).y)
            lineTo(p(0.02f, -0.15f).x, p(0.02f, -0.15f).y)
            quadraticTo(p(0.015f, -0.1f).x, p(0.015f, -0.1f).y, p(0.03f, -0.015f).x, p(0.03f, -0.015f).y)
            close()
        }
        // canopy first: puffs of leaves behind the portraits
        val leaf = Color(0xFF5DBB4A)
        for ((x, y, r) in listOf(Triple(-0.18f, -0.3f, 0.08f), Triple(0f, -0.33f, 0.09f), Triple(0.18f, -0.3f, 0.08f), Triple(-0.17f, -0.17f, 0.075f), Triple(0.17f, -0.17f, 0.075f), Triple(0f, -0.2f, 0.085f), Triple(-0.09f, -0.37f, 0.06f), Triple(0.09f, -0.37f, 0.06f))) {
            drawCircle(leaf.darken(0.15f), r * u, p(x + 0.006f, y + 0.008f))
            drawCircle(leaf, r * u, p(x, y))
            drawCircle(leaf.lighten(0.2f), r * 0.5f * u, p(x - r * 0.3f, y - r * 0.3f), alpha = 0.7f)
        }
        for (bx in listOf(-0.18f, 0f, 0.18f)) {
            val br = Path().apply { moveTo(p(0f, -0.15f).x, p(0f, -0.15f).y); quadraticTo(p(bx * 0.4f, -0.2f).x, p(bx * 0.4f, -0.2f).y, p(bx, -0.28f).x, p(bx, -0.28f).y) }
            drawPath(br, Ink.line, style = Stroke(0.014f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(br, bark, style = Stroke(0.014f * u, cap = StrokeCap.Round))
        }
        drawPath(trunk, bark)
        drawPath(trunk, Ink.line, style = pen.stroke)
        // a butterfly drifting about
        val bx = canvas.left + ((t * 0.02f) % 1f) * canvas.width
        val by = canvas.bottom - 0.11f * u + sin(t * 1.7f) * 0.03f * u
        val flap = abs(sin(t * 10f))
        drawOval(Color(0xFFFF9EC7), Offset(bx - 0.012f * u * flap, by - 0.008f * u), Size(0.012f * u * flap, 0.014f * u))
        drawOval(Color(0xFFFF9EC7), Offset(bx, by - 0.008f * u), Size(0.012f * u * flap, 0.014f * u))
        // little flowers at the roots
        for (k in 0 until 5) drawCircle(listOf(Color(0xFFFFE18A), Color(0xFFFF9EC7), Color.White)[k % 3], 0.006f * u, Offset(canvas.left + (0.05f + k * 0.11f) * u, canvas.bottom - 0.02f * u - (k % 2) * 0.01f * u))
    }
    drawRoundRect(Ink.line, canvas.topLeft, canvas.size, androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
    // Six portraits in two rows, each in a gold oval. Tapped ones have a small star under them.
    val xs = listOf(-0.18f, 0f, 0.18f)
    val ys = listOf(-0.31f, -0.15f)
    for (i in 0 until 6) {
        val cx = xs[i % 3]
        val cy = ys[i / 3]
        val winking = f.mode == i + 1
        val pulse = if (winking) 1f + 0.12f * sin(t * 14f) else 1f
        val c = p(cx, cy)
        scale(pulse, pulse, c) { portrait(c, 0.036f * u, i, pen, t, winking) }
        if ((f.count shr i) and 1 == 1) {
            drawPath(starPath(Offset(c.x, c.y + 0.058f * u), 0.009f * u, 0.004f * u), AtC.goldLight)
            drawPath(starPath(Offset(c.x, c.y + 0.058f * u), 0.009f * u, 0.004f * u), Ink.line, style = pen.thin)
        }
        if (winking) for (k in 0 until 4) twinkle(Offset(c.x + cos(t * 5f + k * 1.6f) * 0.055f * u, c.y + sin(t * 5f + k * 1.6f) * 0.065f * u), 0.014f * u, AtC.goldLight, 0.9f)
    }
}

/** One portrait: a gold oval frame and a cartoon face; [i] picks who it is. */
private fun DrawScope.portrait(c: Offset, r: Float, i: Int, pen: Pen, t: Float, winking: Boolean) {
    val w = r
    val h = r * 1.27f
    drawOval(AtC.gold, Offset(c.x - w, c.y - h), Size(w * 2f, h * 2f))
    drawOval(Ink.line, Offset(c.x - w, c.y - h), Size(w * 2f, h * 2f), style = pen.stroke)
    val inner = Rect(c.x - w * 0.82f, c.y - h * 0.84f, c.x + w * 0.82f, c.y + h * 0.84f)
    val bg = listOf(Color(0xFFBFD9D6), Color(0xFF9DBBD3), Color(0xFFE8D2A0), Color(0xFFD9B9E8), Color(0xFFE8D2A0), Color(0xFFBFD9D6))[i]
    clipPath(Path().apply { addOval(inner) }) {
        drawRect(bg, inner.topLeft, inner.size)
        val skin = listOf(Color(0xFFF9D0B0), Color(0xFFF7F8FA), Color(0xFFEDB58D), Color(0xFFD9986B), Color(0xFFF5A04A), Color(0xFFFFE3CC))[i]
        val fc = Offset(c.x, c.y + h * 0.1f)
        when (i) {
            1 -> {
                // Sture, the family ghost: a sheet, big blue eyes and a shy smile.
                val body = Path().apply {
                    moveTo(c.x - w * 0.6f, c.y + h)
                    lineTo(c.x - w * 0.6f, c.y - h * 0.1f)
                    quadraticTo(c.x - w * 0.6f, c.y - h * 0.65f, c.x, c.y - h * 0.65f)
                    quadraticTo(c.x + w * 0.6f, c.y - h * 0.65f, c.x + w * 0.6f, c.y - h * 0.1f)
                    lineTo(c.x + w * 0.6f, c.y + h)
                    close()
                }
                drawPath(body, Color(0xFFF7F8FA))
                drawPath(body, Ink.line, style = pen.thin)
            }
            4 -> {
                drawCircle(skin, w * 0.62f, fc)
                for (s in listOf(-1f, 1f)) drawPath(Path().apply { moveTo(c.x + s * w * 0.55f, fc.y - w * 0.3f); lineTo(c.x + s * w * 0.4f, fc.y - w * 0.95f); lineTo(c.x + s * w * 0.1f, fc.y - w * 0.55f); close() }, skin)
            }
            else -> {
                drawCircle(skin, w * 0.58f, fc)
                // hair or hat
                when (i) {
                    0 -> { drawCircle(Color(0xFFCFCFD8), w * 0.64f, Offset(fc.x, fc.y - w * 0.12f)); drawCircle(skin, w * 0.55f, Offset(fc.x, fc.y + w * 0.05f)); drawCircle(Color(0xFFCFCFD8), w * 0.22f, Offset(fc.x, fc.y - w * 0.68f)) }
                    2 -> { drawArc(Color(0xFF5A3824), 180f, 180f, true, Offset(fc.x - w * 0.6f, fc.y - w * 0.66f), Size(w * 1.2f, w * 1.0f)); drawRect(Color(0xFF5A3824), Offset(fc.x - w * 0.26f, fc.y + w * 0.22f), Size(w * 0.52f, w * 0.1f)) }
                    3 -> { drawArc(Color(0xFF2B1D16), 180f, 180f, true, Offset(fc.x - w * 0.6f, fc.y - w * 0.66f), Size(w * 1.2f, w * 1.0f)); for (s in listOf(-1f, 1f)) { drawCircle(Color(0xFF2B1D16), w * 0.15f, Offset(fc.x + s * w * 0.6f, fc.y + w * 0.25f)); drawCircle(Color(0xFFFF6FA8), w * 0.08f, Offset(fc.x + s * w * 0.6f, fc.y + w * 0.4f)) } }
                    else -> { drawCircle(Color(0xFFE8B04A), w * 0.12f, Offset(fc.x, fc.y - w * 0.55f)); drawPath(Path().apply { moveTo(fc.x - w * 0.3f, fc.y - w * 0.52f); lineTo(fc.x - w * 0.5f, fc.y - w * 0.72f); lineTo(fc.x - w * 0.5f, fc.y - w * 0.4f); close() }, Color(0xFFFF6FA8)); drawPath(Path().apply { moveTo(fc.x + w * 0.3f, fc.y - w * 0.52f); lineTo(fc.x + w * 0.5f, fc.y - w * 0.72f); lineTo(fc.x + w * 0.5f, fc.y - w * 0.4f); close() }, Color(0xFFFF6FA8)) }
                }
            }
        }
        // eyes, one of them winking when tapped, and a smile
        val eyeY = if (i == 1) c.y - h * 0.12f else fc.y - w * 0.05f
        val blink = atEvery(t, 4f + i * 0.7f, 0.12f, i * 1.3f)
        for ((k, s) in listOf(-1f, 1f).withIndex()) {
            val ex = c.x + s * w * 0.24f
            if ((winking && k == 1) || blink) {
                drawLine(Ink.line, Offset(ex - w * 0.12f, eyeY), Offset(ex + w * 0.12f, eyeY), pen.lw * 0.9f, StrokeCap.Round)
            } else {
                drawCircle(if (i == 1) Color(0xFF4AB3FF) else Ink.line, w * (if (i == 1) 0.14f else 0.085f), Offset(ex, eyeY))
                if (i == 1) drawCircle(Color.White, w * 0.05f, Offset(ex - w * 0.03f, eyeY - w * 0.04f))
            }
        }
        drawArc(Ink.line, 10f, 160f, false, Offset(c.x - w * 0.16f, eyeY + w * 0.14f), Size(w * 0.32f, w * 0.2f), style = pen.thin)
        if (i == 2) drawLine(Color(0xFF5A3824), Offset(c.x - w * 0.28f, eyeY + w * 0.2f), Offset(c.x + w * 0.28f, eyeY + w * 0.2f), w * 0.1f, StrokeCap.Round)
        if (i == 0) for (s in listOf(-1f, 1f)) drawCircle(Ink.line, w * 0.17f, Offset(c.x + s * w * 0.24f, eyeY), style = Stroke(pen.lw * 0.8f))
        drawCircle(Color(0xFFFF6F91).copy(alpha = 0.4f), w * 0.09f, Offset(c.x - w * 0.36f, eyeY + w * 0.2f))
        drawCircle(Color(0xFFFF6F91).copy(alpha = 0.4f), w * 0.09f, Offset(c.x + w * 0.36f, eyeY + w * 0.2f))
    }
    drawOval(Ink.line, inner.topLeft, inner.size, style = pen.thin)
}

// ------------------------------------------------------------------------------------------ chandelier

internal fun DrawScope.atChandelier(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val pivot = p(0f, -0.55f)
    rotate(f.angle * 57.2958f, pivot) {
        // Chain up to the ceiling.
        drawLine(Ink.line, p(0f, -0.55f), p(0f, -0.2f), pen.lw * 2f)
        for (k in 0 until 7) atDot(p(0f, -0.22f - k * 0.05f), 0.006f * u, AtC.brass, pen)
        if (f.on) fxGlow(p(0f, -0.07f), 0.36f * u, AtC.warm, 0.5f + 0.1f * sin(t * 4f))
        // A crown, the brass hoop with six candles, and crystal drops.
        val crown = Path().apply { moveTo(p(-0.03f, -0.19f).x, p(-0.03f, -0.19f).y); lineTo(p(0.03f, -0.19f).x, p(0.03f, -0.19f).y); lineTo(p(0.012f, -0.15f).x, p(0.012f, -0.15f).y); lineTo(p(-0.012f, -0.15f).x, p(-0.012f, -0.15f).y); close() }
        inked(crown, AtC.brass, pen)
        for (s in listOf(-1f, 1f)) {
            val arm = Path().apply { moveTo(p(0f, -0.16f).x, p(0f, -0.16f).y); cubicTo(p(s * 0.09f, -0.16f).x, p(s * 0.09f, -0.16f).y, p(s * 0.15f, -0.12f).x, p(s * 0.15f, -0.12f).y, p(s * 0.15f, -0.08f).x, p(s * 0.15f, -0.08f).y) }
            drawPath(arm, Ink.line, style = Stroke(0.01f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(arm, AtC.brass, style = Stroke(0.01f * u, cap = StrokeCap.Round))
        }
        val ring = Rect(-0.16f * u, -0.1f * u, 0.16f * u, -0.04f * u)
        drawOval(Ink.line, ring.topLeft, ring.size, style = Stroke(0.012f * u + pen.lw * 2f))
        drawOval(AtC.brass, ring.topLeft, ring.size, style = Stroke(0.012f * u))
        drawOval(AtC.brassLight, Offset(ring.left, ring.top), ring.size, style = Stroke(0.003f * u), alpha = 0.5f)
        for ((i, cxp) in listOf(-0.15f, -0.09f, -0.03f, 0.03f, 0.09f, 0.15f).withIndex()) {
            val back = i == 1 || i == 3 || i == 4 && false
            val y = -0.07f - (if (i % 2 == 1) 0.026f else 0f) * 1f
            val bx = cxp
            val wax = Rect(p(bx - 0.0075f, y - 0.05f).x, p(0f, y - 0.05f).y, p(bx + 0.0075f, y).x, p(0f, y).y)
            inkedRound(wax, 0.003f * u, AtC.cream, pen)
            if (f.on) fxFire(wax.center.x, wax.top - 0.003f * u, 0.014f * u, 0.034f * u, t, i * 1.3f, pen) else drawLine(Ink.line, Offset(wax.center.x, wax.top), Offset(wax.center.x, wax.top - 0.007f * u), pen.lw * 0.8f, StrokeCap.Round)
            if (back) Unit
        }
        for (k in 0 until 7) {
            val x = -0.14f + k * 0.047f
            val y = -0.038f
            drawLine(Ink.line, p(x, y), p(x, y + 0.02f), pen.lw * 0.6f)
            val gem = Path().apply { moveTo(p(x, y + 0.016f).x, p(x, y + 0.016f).y); lineTo(p(x + 0.008f, y + 0.032f).x, p(x + 0.008f, y + 0.032f).y); lineTo(p(x, y + 0.05f).x, p(x, y + 0.05f).y); lineTo(p(x - 0.008f, y + 0.032f).x, p(x - 0.008f, y + 0.032f).y); close() }
            drawPath(gem, Color(0xFFDDF3FF).copy(alpha = 0.85f))
            drawPath(gem, Ink.line, style = pen.thin)
            val g = atWave(t, 2.5f, k * 1.1f)
            if (g > 0.75f) twinkle(p(x + 0.004f, y + 0.03f), 0.012f * u * g, Color.White, g)
        }
        // the big pendant in the middle
        val pend = Path().apply { moveTo(p(0f, -0.04f).x, p(0f, -0.04f).y); lineTo(p(0.016f, 0.012f).x, p(0.016f, 0.012f).y); lineTo(p(0f, 0.05f).x, p(0f, 0.05f).y); lineTo(p(-0.016f, 0.012f).x, p(-0.016f, 0.012f).y); close() }
        drawPath(pend, Color(0xFFDDF3FF))
        drawPath(pend, Ink.line, style = pen.stroke)
        drawLine(Color.White, p(-0.005f, -0.01f), p(0f, 0.025f), 0.003f * u, StrokeCap.Round)
    }
}

// ------------------------------------------------------------------------------------------ sconce

internal fun DrawScope.atSconce(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    if (f.on) fxGlow(p(0f, -0.1f), 0.2f * u, AtC.warm, 0.5f + 0.1f * sin(t * 5f + f.id))
    // A little oval mirror plate that doubles the light, a curled arm, a cup and a candle.
    val plate = Rect(-0.032f * u, -0.15f * u, 0.032f * u, -0.04f * u)
    drawOval(AtC.gold, plate.topLeft, plate.size)
    drawOval(Color(0xFFFFF3C4), Offset(plate.left + 0.006f * u, plate.top + 0.006f * u), Size(plate.width - 0.012f * u, plate.height - 0.012f * u), alpha = 0.55f)
    drawOval(Ink.line, plate.topLeft, plate.size, style = pen.stroke)
    shine(p(-0.01f, -0.12f), 0.006f * u, 0.03f * u, 0.5f)
    val arm = Path().apply { moveTo(p(0f, -0.06f).x, p(0f, -0.06f).y); cubicTo(p(0.03f, -0.03f).x, p(0.03f, -0.03f).y, p(-0.03f, -0.02f).x, p(-0.03f, -0.02f).y, p(0f, -0.005f).x, p(0f, -0.005f).y) }
    drawPath(arm, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(arm, AtC.brass, style = Stroke(0.009f * u, cap = StrokeCap.Round))
    val cup = Path().apply { moveTo(p(-0.018f, -0.075f).x, p(-0.018f, -0.075f).y); lineTo(p(0.018f, -0.075f).x, p(0.018f, -0.075f).y); lineTo(p(0.01f, -0.055f).x, p(0.01f, -0.055f).y); lineTo(p(-0.01f, -0.055f).x, p(-0.01f, -0.055f).y); close() }
    inked(cup, AtC.brass, pen)
    val wax = Rect(p(-0.008f, -0.13f).x, p(0f, -0.13f).y, p(0.008f, -0.075f).x, p(0f, -0.075f).y)
    inkedRound(wax, 0.003f * u, AtC.cream, pen)
    if (f.on) fxFire(wax.center.x, wax.top - 0.003f * u, 0.016f * u, 0.04f * u, t, f.id.toFloat(), pen) else {
        drawLine(Ink.line, Offset(wax.center.x, wax.top), Offset(wax.center.x, wax.top - 0.008f * u), pen.lw * 0.8f, StrokeCap.Round)
        fxPuffs(wax.center.x, wax.top - 0.012f * u, t + f.id, 0.005f * u, 0.05f * u, Color(0xFFD8D4DC), 0.4f, 2, 0.3f, 0.008f * u)
    }
}
