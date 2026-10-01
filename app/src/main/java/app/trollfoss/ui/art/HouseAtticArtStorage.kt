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
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import kotlin.math.cos
import kotlin.math.sin

/*
 * Storhuset, attic: the storage. Sheets over old furniture (one of them is Sture), the costume trunk, a
 * rocking horse, cartons of old toys, knitting spiders, a lantern and the round window.
 */

// ------------------------------------------------------------------------------------------ sheets

/** A dust sheet's hem: scallops along the floor from [x1] to [x0] (right to left), y at [y]. */
private fun Path.hem(u: Float, x1: Float, x0: Float, y: Float, scallops: Int) {
    val w = (x1 - x0) / scallops
    for (k in 0 until scallops) {
        val a = x1 - (k + 1) * w
        lineTo(a * u, y * u)
        // each scallop dips a little below the hem line
        quadraticTo((a + w / 2f) * u, (y + 0.014f) * u, (a + w) * u, y * u)
    }
}

private fun sheetShape(u: Float, v: Int): Path = Path().apply {
    when (v) {
        0 -> {
            // An armchair: back and two arms.
            moveTo(-0.105f * u, 0f)
            quadraticTo(-0.12f * u, -0.09f * u, -0.1f * u, -0.15f * u)
            quadraticTo(-0.095f * u, -0.19f * u, -0.065f * u, -0.17f * u)
            quadraticTo(-0.07f * u, -0.26f * u, 0f, -0.26f * u)
            quadraticTo(0.07f * u, -0.26f * u, 0.065f * u, -0.17f * u)
            quadraticTo(0.095f * u, -0.19f * u, 0.1f * u, -0.15f * u)
            quadraticTo(0.12f * u, -0.09f * u, 0.105f * u, 0f)
            hem(u, 0.105f, -0.105f, 0f, 4)
        }
        1 -> {
            // A tall mirror or a standing lamp, narrow at the top.
            moveTo(-0.085f * u, 0f)
            quadraticTo(-0.09f * u, -0.12f * u, -0.04f * u, -0.21f * u)
            quadraticTo(-0.03f * u, -0.27f * u, 0f, -0.285f * u)
            quadraticTo(0.03f * u, -0.27f * u, 0.04f * u, -0.21f * u)
            quadraticTo(0.09f * u, -0.12f * u, 0.085f * u, 0f)
            hem(u, 0.085f, -0.085f, 0f, 3)
        }
        2 -> {
            // A round table: wide top, narrow waist.
            moveTo(-0.105f * u, 0f)
            quadraticTo(-0.09f * u, -0.07f * u, -0.108f * u, -0.13f * u)
            quadraticTo(-0.115f * u, -0.18f * u, -0.06f * u, -0.185f * u)
            quadraticTo(0f, -0.2f * u, 0.06f * u, -0.185f * u)
            quadraticTo(0.115f * u, -0.18f * u, 0.108f * u, -0.13f * u)
            quadraticTo(0.09f * u, -0.07f * u, 0.105f * u, 0f)
            hem(u, 0.105f, -0.105f, 0f, 4)
        }
        else -> {
            // A birdcage on a stand, with a knob on the top.
            moveTo(-0.09f * u, 0f)
            quadraticTo(-0.095f * u, -0.1f * u, -0.07f * u, -0.2f * u)
            quadraticTo(-0.05f * u, -0.255f * u, 0f, -0.26f * u)
            quadraticTo(0.05f * u, -0.255f * u, 0.07f * u, -0.2f * u)
            quadraticTo(0.095f * u, -0.1f * u, 0.09f * u, 0f)
            hem(u, 0.09f, -0.09f, 0f, 3)
        }
    }
    close()
}

internal fun DrawScope.atSheeted(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val v = f.variant.mod(4)
    val t = pen.t
    atShadow(u, 0.24f, 0.12f)
    if (f.open) {
        sheetUncovered(f, u, pen, v)
        return
    }
    val shape = sheetShape(u, v)
    inked(shape, AtC.sheet, pen)
    // Folds that fall from the top, and the hem's dark inside at the bottom.
    val fold = AtC.sheetDark.copy(alpha = 0.75f)
    when (v) {
        0 -> {
            atFold(p(-0.02f, -0.25f), p(-0.045f, -0.14f), p(-0.05f, -0.01f), fold, pen.lw * 0.8f)
            atFold(p(0.03f, -0.25f), p(0.05f, -0.13f), p(0.06f, -0.01f), fold, pen.lw * 0.8f)
            atFold(p(-0.09f, -0.15f), p(-0.085f, -0.08f), p(-0.095f, -0.01f), fold, pen.lw * 0.7f)
            atFold(p(0.09f, -0.15f), p(0.085f, -0.08f), p(0.095f, -0.01f), fold, pen.lw * 0.7f)
        }
        1 -> {
            atFold(p(-0.01f, -0.27f), p(-0.035f, -0.14f), p(-0.05f, -0.01f), fold, pen.lw * 0.8f)
            atFold(p(0.015f, -0.27f), p(0.04f, -0.14f), p(0.055f, -0.01f), fold, pen.lw * 0.8f)
        }
        2 -> {
            atFold(p(-0.06f, -0.18f), p(-0.07f, -0.1f), p(-0.06f, -0.01f), fold, pen.lw * 0.8f)
            atFold(p(0.0f, -0.19f), p(0.01f, -0.1f), p(-0.005f, -0.01f), fold, pen.lw * 0.8f)
            atFold(p(0.06f, -0.18f), p(0.07f, -0.1f), p(0.06f, -0.01f), fold, pen.lw * 0.8f)
        }
        else -> {
            atFold(p(-0.02f, -0.25f), p(-0.04f, -0.13f), p(-0.05f, -0.01f), fold, pen.lw * 0.8f)
            atFold(p(0.025f, -0.25f), p(0.05f, -0.13f), p(0.055f, -0.01f), fold, pen.lw * 0.8f)
            // the knob of the cage's ring pokes up through the sheet
            atDot(p(0f, -0.272f), 0.011f * u, AtC.sheet, pen)
        }
    }
    // A few specks of dust that settled on top, and one thread of cobweb.
    for (i in 0 until 5) {
        val x = (hash01(i + v * 7, 3) - 0.5f) * 0.12f
        val y = -0.16f - hash01(i + v * 7, 4) * 0.08f
        drawCircle(AtC.sheetDark.copy(alpha = 0.5f), 0.003f * u, p(x, y))
    }
    // Sture inside: now and then two eyes peek out under the hem, then vanish again. Shh!
    if (f.mode == 2 && atEvery(t, 4.6f, 0.8f, f.id * 0.73f)) {
        val slit = Path().apply {
            moveTo(-0.045f * u, 0.002f * u)
            quadraticTo(0f, -0.045f * u, 0.045f * u, 0.002f * u)
            close()
        }
        drawPath(slit, Color(0xFF241A38))
        for (s in listOf(-1f, 1f)) {
            val c = p(0.016f * s, -0.012f)
            drawOval(Color.White, Offset(c.x - 0.009f * u, c.y - 0.011f * u), Size(0.018f * u, 0.022f * u))
            drawCircle(Ink.line, 0.005f * u, Offset(c.x + sin(t * 2.2f) * 0.0025f * u, c.y + 0.002f * u))
        }
        // a lid blink
        if (atEvery(t, 1.3f, 0.12f)) drawRect(Color(0xFF241A38), p(-0.04f, -0.026f), Size(0.08f * u, 0.014f * u))
    }
}

/** The sheet lifted: what was under it, and the sheet in a heap on the floor. */
private fun DrawScope.sheetUncovered(f: Fixture, u: Float, pen: Pen, v: Int) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    when (v) {
        0 -> {
            val velvet = Color(0xFF8E3B5A)
            fxBox(u, -0.085f, -0.1f, 0.085f, -0.02f, 0.1f, velvet, pen, rad = 0.012f)
            fxBox(u, -0.07f, -0.2f, 0.07f, -0.09f, 0.02f, velvet.darken(0.1f), pen, rad = 0.016f, z = 0.085f)
            fxBox(u, -0.11f, -0.13f, -0.075f, -0.02f, 0.1f, velvet.lighten(0.05f), pen, rad = 0.014f)
            fxBox(u, 0.075f, -0.13f, 0.11f, -0.02f, 0.1f, velvet.lighten(0.05f), pen, rad = 0.014f)
            fxBox(u, -0.07f, -0.115f, 0.07f, -0.085f, 0.09f, velvet.lighten(0.18f), pen, rad = 0.012f, z = 0.005f)
            for (s in listOf(-1f, 1f)) fxPost(u, 0.08f * s, 0.02f, -0.022f, 0f, 0.008f, Color(0xFF6E4630), pen)
            for (i in 0 until 3) atDot(fxQ(u, -0.03f + i * 0.03f, -0.15f, 0.07f), 0.005f * u, AtC.brassLight, pen)
        }
        1 -> {
            // A tall oval mirror in a gold frame on two feet.
            fxBox(u, -0.07f, -0.02f, 0.07f, 0f, 0.07f, Color(0xFF6E4630), pen, rad = 0.006f)
            val c = p(0f, -0.15f)
            drawOval(AtC.gold, Offset(c.x - 0.075f * u, c.y - 0.125f * u), Size(0.15f * u, 0.25f * u))
            drawOval(Color(0xFFCFE8F7), Offset(c.x - 0.062f * u, c.y - 0.112f * u), Size(0.124f * u, 0.224f * u))
            drawOval(Ink.line, Offset(c.x - 0.075f * u, c.y - 0.125f * u), Size(0.15f * u, 0.25f * u), style = pen.stroke)
            drawOval(Ink.line, Offset(c.x - 0.062f * u, c.y - 0.112f * u), Size(0.124f * u, 0.224f * u), style = pen.thin)
            drawLine(Color.White.copy(alpha = 0.8f), p(-0.03f, -0.22f), p(0.005f, -0.14f), 0.012f * u, StrokeCap.Round)
            drawLine(Color.White.copy(alpha = 0.6f), p(0.0f, -0.23f), p(0.03f, -0.17f), 0.007f * u, StrokeCap.Round)
            twinkle(p(0.03f, -0.07f), 0.016f * u * atWave(t, 3f), Color.White, 0.9f)
        }
        2 -> {
            // A little round table with a lace cloth and a teapot.
            fxPost(u, 0f, 0.05f, 0f, -0.11f, 0.012f, Color(0xFF6E4630), pen)
            fxBox(u, -0.07f, -0.01f, 0.07f, 0f, 0.07f, Color(0xFF6E4630), pen, rad = 0.004f, z = 0.015f)
            drawPath(fxDisc2(0f, -0.125f * u, 0.105f * u, 0.07f * u), Color(0xFFF1E4C4))
            drawPath(fxDisc2(0f, -0.125f * u, 0.105f * u, 0.07f * u), Ink.line, style = pen.stroke)
            drawPath(fxDisc2(0f, -0.125f * u, 0.075f * u, 0.05f * u), Color(0xFFE3D3B0), style = pen.thin)
            val teapot = fxBlob(u, -0.03f, -0.15f, 0f, -0.175f, 0.03f, -0.15f, 0.03f, -0.13f, -0.03f, -0.13f)
            drawPath(teapot, AtC.rose)
            drawPath(teapot, Ink.line, style = pen.stroke)
            drawLine(Ink.line, p(0.03f, -0.15f), p(0.055f, -0.165f), 0.006f * u, StrokeCap.Round)
            atDot(p(0f, -0.182f), 0.006f * u, AtC.cream, pen)
            fxPuffs(0.055f * u, -0.17f * u, t, 0.008f * u, 0.07f * u, Color.White, 0.5f, 3, 0.4f, 0.01f * u)
        }
        else -> {
            // A birdcage with a small blue bird that sings.
            fxPost(u, 0f, 0.05f, 0f, -0.16f, 0.008f, AtC.brassDark, pen)
            fxBox(u, -0.05f, -0.012f, 0.05f, 0f, 0.07f, AtC.brassDark, pen, rad = 0.004f, z = 0.015f)
            val cage = Path().apply {
                moveTo(-0.07f * u, -0.16f * u)
                lineTo(-0.07f * u, -0.24f * u)
                quadraticTo(-0.07f * u, -0.3f * u, 0f, -0.3f * u)
                quadraticTo(0.07f * u, -0.3f * u, 0.07f * u, -0.24f * u)
                lineTo(0.07f * u, -0.16f * u)
                close()
            }
            drawPath(cage, Color(0x55FFF3C4))
            drawPath(cage, AtC.brass, style = Stroke(pen.lw * 1.3f))
            for (k in -2..2) drawLine(AtC.brass, p(k * 0.028f, -0.16f), p(k * 0.026f, -0.298f + 0.006f * k * k), pen.lw * 0.8f)
            val hop = (sin(t * 4f).coerceAtLeast(0f)) * 0.01f
            val b = p(0.01f, -0.205f - hop)
            drawOval(Color(0xFF4AB3FF), Offset(b.x - 0.025f * u, b.y - 0.02f * u), Size(0.05f * u, 0.04f * u))
            drawOval(Ink.line, Offset(b.x - 0.025f * u, b.y - 0.02f * u), Size(0.05f * u, 0.04f * u), style = pen.thin)
            drawCircle(Ink.line, 0.004f * u, Offset(b.x + 0.012f * u, b.y - 0.005f * u))
            drawPath(Path().apply { moveTo(b.x + 0.024f * u, b.y - 0.004f * u); lineTo(b.x + 0.036f * u, b.y); lineTo(b.x + 0.024f * u, b.y + 0.004f * u); close() }, AtC.brassLight)
            fxNote(p(0.06f, -0.27f - atWave(t, 2f) * 0.03f), 0.012f * u, Color(0xFF8B5CF6), 0.85f * atWave(t, 2f, 1f))
        }
    }
    // The sheet lies in a heap on the floor beside it, with a puff of dust.
    val heap = Path().apply {
        moveTo(0.09f * u, 0f)
        quadraticTo(0.1f * u, -0.05f * u, 0.14f * u, -0.04f * u)
        quadraticTo(0.19f * u, -0.05f * u, 0.2f * u, 0f)
        quadraticTo(0.15f * u, 0.012f * u, 0.09f * u, 0f)
        close()
    }
    inked(heap, AtC.sheet, pen)
    atFold(p(0.12f, -0.035f), p(0.13f, -0.02f), p(0.125f, -0.002f), AtC.sheetDark, pen.lw * 0.7f)
    atFold(p(0.16f, -0.04f), p(0.17f, -0.02f), p(0.175f, -0.002f), AtC.sheetDark, pen.lw * 0.7f)
    fxPuffs(0.14f * u, -0.05f * u, t, 0.014f * u, 0.07f * u, Color(0xFFE9E1D0), 0.5f, 3, 0.35f, 0.02f * u)
}

// ----------------------------------------------------------------------------------------- the trunk

private fun lidTop(x: Float): Float = -0.125f - 0.048f * (1f - (x / 0.145f) * (x / 0.145f))

private val STICKERS = listOf(
    Triple(-0.09f, -0.135f, 0), Triple(0.02f, -0.15f, 1), Triple(0.09f, -0.125f, 2),
    Triple(-0.03f, -0.055f, 3), Triple(0.1f, -0.055f, 4), Triple(-0.11f, -0.055f, 5),
)

internal fun DrawScope.atTrunk(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val teal = Color(0xFF3E6F86)
    val d = 0.12f
    atShadow(u, 0.3f, 0.14f)
    if (f.open) {
        // The lid stands open behind: its pretty lining faces us.
        val lid = Path().apply {
            val a = q(-0.145f, -0.095f, d)
            moveTo(a.x, a.y)
            val b = q(-0.145f, -0.22f, d)
            lineTo(b.x, b.y)
            val c = q(0f, -0.275f, d)
            val e = q(-0.145f, -0.265f, d)
            quadraticTo(e.x, e.y, c.x, c.y)
            val g = q(0.145f, -0.265f, d)
            val h = q(0.145f, -0.22f, d)
            quadraticTo(g.x, g.y, h.x, h.y)
            val i = q(0.145f, -0.095f, d)
            lineTo(i.x, i.y)
            close()
        }
        drawPath(lid, teal.darken(0.15f))
        drawPath(lid, Ink.line, style = pen.stroke)
        clipPath(lid) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFF6E3C8), Color(0xFFE8B6A6)), startY = q(0f, -0.28f, d).y, endY = q(0f, -0.09f, d).y), q(-0.15f, -0.28f, d), Size(0.3f * u, 0.19f * u))
            for (k in 0 until 14) {
                val c = q(-0.12f + (k % 5) * 0.06f + (k / 5) * 0.03f, -0.115f - (k / 5) * 0.05f, d)
                drawCircle(Color(0xFFD28A9A), 0.008f * u, c)
            }
        }
        drawPath(lid, Ink.line, style = pen.stroke)
        // The inside of the trunk: dark, with a heap of dressing-up clothes sticking up.
        fxBox(u, -0.14f, -0.1f, 0.14f, -0.01f, d, teal, pen, rad = 0.006f, top = Color(0xFF1B2C36))
        fxFace(fxFlat(u, -0.125f, 0.125f, -0.1f, 0.012f, d - 0.01f), Color(0xFF241A38), pen)
        val sway = sin(t * 2.1f) * 0.004f
        fxCloud(Color(0xFFFFE18A), pen, true, q(-0.07f, -0.115f + sway, 0.05f).x, q(-0.07f, -0.115f + sway, 0.05f).y, 0.03f * u, q(-0.045f, -0.13f + sway, 0.06f).x, q(-0.045f, -0.13f + sway, 0.06f).y, 0.025f * u)
        fxCloud(Color(0xFFE8A6B0), pen, true, q(0.0f, -0.12f - sway, 0.07f).x, q(0.0f, -0.12f - sway, 0.07f).y, 0.033f * u, q(0.03f, -0.133f - sway, 0.06f).x, q(0.03f, -0.133f - sway, 0.06f).y, 0.026f * u)
        fxCloud(Color(0xFF6AA7E8), pen, true, q(0.08f, -0.112f + sway, 0.05f).x, q(0.08f, -0.112f + sway, 0.05f).y, 0.028f * u, q(0.095f, -0.125f, 0.06f).x, q(0.095f, -0.125f, 0.06f).y, 0.022f * u)
        // stripes of a pirate's jumper and a silver helmet's gleam
        for (k in 0 until 3) drawLine(Color(0xFFD2443A), q(-0.1f + k * 0.012f, -0.1f, 0.075f), q(-0.095f + k * 0.012f, -0.135f, 0.085f), 0.006f * u, StrokeCap.Round)
        twinkle(q(-0.01f, -0.16f, 0.07f), 0.015f * u * atWave(t, 3f), Color.White, 0.9f)
        twinkle(q(0.07f, -0.15f, 0.07f), 0.012f * u * atWave(t, 2.4f, 2f), AtC.brassLight, 0.9f)
        fxGlow(q(0f, -0.14f, 0.07f), 0.15f * u, AtC.warm, 0.35f)
        // front of the body
        fxBox(u, -0.14f, -0.1f, 0.14f, -0.01f, 0.02f, teal, pen, rad = 0.006f, front = true, top = Color(0xFF1B2C36))
    } else {
        fxBox(u, -0.14f, -0.095f, 0.14f, -0.01f, d, teal, pen, rad = 0.006f)
        // The domed lid: right side, top, and front arch.
        val n = 10
        val front = Array(n + 1) { i -> val x = -0.145f + 0.29f * i / n; q(x, lidTop(x), 0f) }
        val back = Array(n + 1) { i -> val x = -0.145f + 0.29f * i / n; q(x, lidTop(x), d) }
        val side = Path().apply {
            val a = q(0.145f, -0.095f, 0f)
            moveTo(a.x, a.y)
            val b = q(0.145f, lidTop(0.145f), 0f)
            lineTo(b.x, b.y)
            val c = q(0.145f, lidTop(0.145f), d)
            lineTo(c.x, c.y)
            val e = q(0.145f, -0.095f, d)
            lineTo(e.x, e.y)
            close()
        }
        fxFace(side, teal.darken(0.22f), pen)
        val top = Path().apply {
            moveTo(front[0].x, front[0].y)
            for (i in 1..n) lineTo(front[i].x, front[i].y)
            for (i in n downTo 0) lineTo(back[i].x, back[i].y)
            close()
        }
        drawPath(top, teal.lighten(0.14f))
        drawPath(top, Ink.line, style = pen.stroke)
        val face = Path().apply {
            val a = q(-0.145f, -0.095f, 0f)
            moveTo(a.x, a.y)
            for (i in 0..n) lineTo(front[i].x, front[i].y)
            val b = q(0.145f, -0.095f, 0f)
            lineTo(b.x, b.y)
            close()
        }
        inked(face, teal, pen)
        // Brass straps over the lid and down the front, corner caps, a lock and a handle.
        for (sx in listOf(-0.092f, 0.092f)) {
            drawRect(AtC.brassDark, p(sx - 0.009f, lidTop(sx)), Size(0.018f * u, (-0.01f - lidTop(sx)) * u))
            drawRect(Ink.line, p(sx - 0.009f, lidTop(sx)), Size(0.018f * u, (-0.01f - lidTop(sx)) * u), style = pen.thin)
            for (k in 0..2) drawCircle(AtC.brassLight, 0.0028f * u, p(sx, lidTop(sx) + 0.012f + k * 0.03f))
        }
        drawLine(Ink.line, p(-0.14f, -0.095f), p(0.14f, -0.095f), pen.lw * 1.1f)
        drawLine(AtC.brass, p(-0.14f, -0.093f), p(0.14f, -0.093f), 0.006f * u)
        for (sx in listOf(-0.14f, 0.14f)) {
            inkedRound(Rect(p(sx - 0.011f, -0.026f).x, p(sx, -0.026f).y, p(sx + 0.011f, 0f).x, p(sx, -0.004f).y), 0.004f * u, AtC.brass, pen, shade = false)
        }
        val lock = Rect(p(-0.017f, -0.115f).x, p(0f, -0.115f).y, p(0.017f, 0f).x, p(0f, -0.07f).y)
        inkedRound(lock, 0.004f * u, AtC.brass, pen)
        drawCircle(Ink.line, 0.004f * u, p(0f, -0.098f))
        drawLine(Ink.line, p(0f, -0.098f), p(0f, -0.088f), 0.004f * u, StrokeCap.Round)
        val hc = q(0.14f, -0.055f, d / 2f)
        drawArc(Color(0xFF3A2C20), 0f, 180f, false, Offset(hc.x - 0.012f * u, hc.y - 0.012f * u), Size(0.024f * u, 0.026f * u), style = Stroke(0.006f * u, cap = StrokeCap.Round))
        // Travel stickers: one more every time the trunk has been opened (up to six).
        for (i in 0 until minOf(f.count, STICKERS.size)) {
            val (sx, sy, kind) = STICKERS[i]
            val c = p(sx, sy)
            when (kind) {
                0 -> { drawCircle(Color(0xFFFFC83D), 0.016f * u, c); drawPath(starPath(c, 0.011f * u, 0.005f * u), Color(0xFFFF8A3D)) }
                1 -> { drawCircle(Color.White, 0.016f * u, c); drawPath(fxHeart(c.x, c.y, 0.009f * u), Color(0xFFFF4D6D)) }
                2 -> { drawCircle(Color(0xFF3BC46B), 0.016f * u, c); drawLine(Color.White, Offset(c.x - 0.008f * u, c.y), Offset(c.x + 0.008f * u, c.y), 0.005f * u, StrokeCap.Round); drawLine(Color.White, Offset(c.x, c.y - 0.008f * u), Offset(c.x, c.y + 0.008f * u), 0.005f * u, StrokeCap.Round) }
                3 -> { drawCircle(Color(0xFF8B5CF6), 0.015f * u, c); drawCircle(Color(0xFFFFE18A), 0.006f * u, c) }
                4 -> { drawCircle(Color(0xFF2F6FB8), 0.015f * u, c); atFold(Offset(c.x - 0.01f * u, c.y), Offset(c.x - 0.004f * u, c.y - 0.01f * u), Offset(c.x + 0.002f * u, c.y), Color.White, 0.004f * u); atFold(Offset(c.x, c.y), Offset(c.x + 0.006f * u, c.y + 0.01f * u), Offset(c.x + 0.012f * u, c.y), Color.White, 0.004f * u) }
                else -> { drawCircle(Color(0xFFF08CB8), 0.015f * u, c); drawCircle(Color.White, 0.005f * u, c) }
            }
            drawCircle(Ink.line, if (kind == 0 || kind == 2) 0.016f * u else 0.015f * u, c, style = pen.thin)
        }
        // Feet.
        for (sx in listOf(-0.12f, 0.12f)) fxBox(u, sx - 0.012f, -0.012f, sx + 0.012f, 0.0f, 0.02f, Color(0xFF3A2C20), pen)
    }
}

// ----------------------------------------------------------------------------------- rocking horse

internal fun DrawScope.atRockingHorse(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val a = f.angle
    val r = 0.12f
    atShadow(u, 0.32f, 0.12f)
    val body = Color(0xFFF2E8D4)
    val dapple = Color(0xFFBFB6A4)
    val wood = Color(0xFFB8824D)
    val yarn = Color(0xFFE8B04A)
    translate(r * a * u, 0f) {
        rotate(a * 57.2958f, p(0f, -r)) {
            // The far side first: far legs and the far rocker.
            val zf = 0.08f
            capsule(q(-0.09f, -0.15f, zf), q(-0.115f, -0.045f, zf), 0.017f * u, body.darken(0.12f), pen)
            capsule(q(0.075f, -0.15f, zf), q(0.11f, -0.045f, zf), 0.017f * u, body.darken(0.12f), pen)
            val rockerFar = Path().apply {
                val s = q(-0.16f, -0.05f, zf)
                moveTo(s.x, s.y)
                val c = q(0f, 0.05f, zf)
                val e = q(0.16f, -0.05f, zf)
                quadraticTo(c.x, c.y, e.x, e.y)
            }
            drawPath(rockerFar, Ink.line, style = Stroke(0.02f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(rockerFar, wood.darken(0.2f), style = Stroke(0.02f * u, cap = StrokeCap.Round))
            capsule(q(-0.08f, -0.03f, 0f), q(-0.08f, -0.03f, zf), 0.01f * u, wood.darken(0.1f), pen)
            capsule(q(0.08f, -0.03f, 0f), q(0.08f, -0.03f, zf), 0.01f * u, wood.darken(0.1f), pen)
            // The tail: a bundle of yarn.
            val tail = Path().apply {
                moveTo(p(-0.115f, -0.185f).x, p(-0.115f, -0.185f).y)
                cubicTo(p(-0.17f, -0.2f).x, p(-0.17f, -0.2f).y, p(-0.175f, -0.13f).x, p(-0.175f, -0.13f).y, p(-0.15f, -0.095f).x, p(-0.15f, -0.095f).y)
                cubicTo(p(-0.145f, -0.13f).x, p(-0.145f, -0.13f).y, p(-0.13f, -0.16f).x, p(-0.13f, -0.16f).y, p(-0.112f, -0.16f).x, p(-0.112f, -0.16f).y)
                close()
            }
            inked(tail, yarn, pen)
            // Body, then the neck and head.
            inkedOval(Rect(-0.125f * u, -0.225f * u, 0.125f * u, -0.115f * u), body, pen)
            for ((dx, dy) in listOf(-0.07f to -0.19f, -0.03f to -0.145f, 0.04f to -0.17f, 0.08f to -0.15f)) drawCircle(dapple, 0.011f * u, p(dx, dy), alpha = 0.7f)
            val neck = Path().apply {
                moveTo(p(0.05f, -0.215f).x, p(0.05f, -0.215f).y)
                quadraticTo(p(0.075f, -0.275f).x, p(0.075f, -0.275f).y, p(0.1f, -0.305f).x, p(0.1f, -0.305f).y)
                lineTo(p(0.165f, -0.285f).x, p(0.165f, -0.285f).y)
                quadraticTo(p(0.14f, -0.24f).x, p(0.14f, -0.24f).y, p(0.115f, -0.2f).x, p(0.115f, -0.2f).y)
                close()
            }
            inked(neck, body, pen)
            val snout = Rect(0.135f * u, -0.3f * u, 0.205f * u, -0.255f * u)
            inkedOval(snout, body, pen)
            drawCircle(Color(0xFFE8A6B0), 0.007f * u, p(0.14f, -0.268f), alpha = 0.75f)
            drawCircle(Ink.line, 0.0035f * u, p(0.197f, -0.275f))
            drawArc(Ink.line, 20f, 110f, false, Offset(0.16f * u, -0.285f * u), Size(0.035f * u, 0.022f * u), style = pen.thin)
            // The eye: big and kind.
            val e = p(0.14f, -0.292f)
            drawCircle(Color.White, 0.0085f * u, e)
            drawCircle(Ink.line, 0.0055f * u, Offset(e.x + 0.0015f * u, e.y + 0.001f * u))
            drawCircle(Color.White, 0.002f * u, Offset(e.x + 0.003f * u, e.y - 0.002f * u))
            drawCircle(Ink.line, 0.0085f * u, e, style = pen.thin)
            // Ears and a yarn forelock and mane.
            for ((ex, ey, s) in listOf(Triple(0.108f, -0.305f, 1f), Triple(0.128f, -0.312f, 0.8f))) {
                val ear = Path().apply {
                    moveTo(p(ex, ey).x, p(ex, ey).y)
                    lineTo(p(ex + 0.004f, ey - 0.03f * s).x, p(ex + 0.004f, ey - 0.03f * s).y)
                    lineTo(p(ex + 0.024f, ey + 0.003f).x, p(ex + 0.024f, ey + 0.003f).y)
                    close()
                }
                inked(ear, if (s == 1f) body else body.darken(0.1f), pen, shade = false)
            }
            for (k in 0 until 6) {
                val bx = 0.058f + k * 0.012f
                val by = -0.22f - k * 0.016f
                val strand = Path().apply {
                    moveTo(p(bx, by).x, p(bx, by).y)
                    quadraticTo(p(bx - 0.035f, by - 0.01f).x, p(bx - 0.035f, by - 0.01f).y, p(bx - 0.04f, by + 0.02f).x, p(bx - 0.04f, by + 0.02f).y)
                }
                drawPath(strand, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
                drawPath(strand, if (k % 2 == 0) yarn else yarn.darken(0.12f), style = Stroke(0.012f * u, cap = StrokeCap.Round))
            }
            // Saddle and a strap.
            val saddle = Path().apply {
                moveTo(p(-0.065f, -0.21f).x, p(-0.065f, -0.21f).y)
                quadraticTo(p(-0.01f, -0.245f).x, p(-0.01f, -0.245f).y, p(0.045f, -0.21f).x, p(0.045f, -0.21f).y)
                lineTo(p(0.04f, -0.16f).x, p(0.04f, -0.16f).y)
                lineTo(p(-0.06f, -0.16f).x, p(-0.06f, -0.16f).y)
                close()
            }
            inked(saddle, Color(0xFF3E6FB0), pen)
            drawLine(AtC.brass, p(-0.06f, -0.168f), p(0.04f, -0.168f), 0.006f * u, StrokeCap.Round)
            // The near legs and the near rocker.
            capsule(q(-0.09f, -0.15f, 0f), q(-0.115f, -0.045f, 0f), 0.019f * u, body, pen)
            capsule(q(0.075f, -0.15f, 0f), q(0.11f, -0.045f, 0f), 0.019f * u, body, pen)
            val rocker = Path().apply {
                val s = q(-0.16f, -0.05f, 0f)
                moveTo(s.x, s.y)
                val c = q(0f, 0.05f, 0f)
                val e2 = q(0.16f, -0.05f, 0f)
                quadraticTo(c.x, c.y, e2.x, e2.y)
            }
            drawPath(rocker, Ink.line, style = Stroke(0.022f * u + pen.lw * 2f, cap = StrokeCap.Round))
            drawPath(rocker, wood, style = Stroke(0.022f * u, cap = StrokeCap.Round))
            drawPath(rocker, wood.lighten(0.3f), style = Stroke(0.006f * u, cap = StrokeCap.Round), alpha = 0.7f)
        }
    }
    // Dust kicked up by a gallop.
    if (f.on && kotlin.math.abs(f.angleV) > 3f) fxPuffs(0f, -0.01f * u, pen.t, 0.012f * u, 0.05f * u, Color(0xFFE9E1D0), 0.4f, 3, 0.8f, 0.03f * u)
}

// ------------------------------------------------------------------------------------------- spiders

internal fun DrawScope.atSpider(f: Fixture, u: Float, pen: Pen) {
    val flip = f.variant == 1
    val t = pen.t
    scale(if (flip) -1f else 1f, 1f, Offset.Zero) {
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        // The web in the corner: rays, and curved threads between them.
        val corner = p(-0.17f, -0.34f)
        val web = Color(0xFFE9EEF2)
        val rays = 6
        for (k in 0..rays) {
            val ang = k * (3.1415927f / 2f) / rays
            val e = Offset(corner.x + cos(ang) * 0.36f * u, corner.y + sin(ang) * 0.36f * u)
            drawLine(web, corner, e, pen.lw * 0.7f, alpha = 0.85f)
        }
        for (ring in 1..4) {
            val rr = ring * 0.085f
            val path = Path()
            for (k in 0..rays) {
                val ang = k * (3.1415927f / 2f) / rays
                val pt = Offset(corner.x + cos(ang) * rr * u, corner.y + sin(ang) * rr * u)
                if (k == 0) {
                    path.moveTo(pt.x, pt.y)
                } else {
                    val mid = (k - 0.5f) * (3.1415927f / 2f) / rays
                    path.quadraticTo(corner.x + cos(mid) * rr * 0.93f * u, corner.y + sin(mid) * rr * 0.93f * u, pt.x, pt.y)
                }
            }
            drawPath(path, web, style = Stroke(pen.lw * 0.6f), alpha = 0.8f)
        }
        // A dew drop on the web.
        drawCircle(Color(0xFFBFE6FF), 0.005f * u, p(-0.1f, -0.29f))
        // The spider hangs on a thread and bobs.
        val bob = sin(t * 1.6f + f.id) * 0.012f
        val sy = -0.17f + bob
        val sx = 0.04f
        drawLine(web, p(sx, -0.34f), p(sx, sy - 0.03f), pen.lw * 0.7f)
        val ink = Ink.line
        val bodyCol = if (flip) Color(0xFF3F9A8F) else Color(0xFF7A4DAF)
        // The knitting grows with every tap (mode 0 to 3): a striped scarf with stitches.
        val len = 0.03f + f.mode * 0.034f
        if (f.mode >= 0) {
            val scarf = Rect((sx - 0.026f) * u, (sy + 0.03f) * u, (sx + 0.026f) * u, (sy + 0.03f + len) * u)
            val cols = listOf(Color(0xFFE8A6B0), Color(0xFFE8B04A), Color(0xFF6AB7C2))
            val rows = (len / 0.016f).toInt().coerceAtLeast(1)
            for (r in 0 until rows) {
                val y0 = scarf.top + r * (scarf.height / rows)
                drawRect(cols[r % 3], Offset(scarf.left, y0), Size(scarf.width, scarf.height / rows + 0.5f))
            }
            fxKnit(scarf, Ink.line.copy(alpha = 0.35f), 4, rows * 2, pen.lw * 0.5f)
            drawRect(Ink.line, scarf.topLeft, scarf.size, style = pen.thin)
            // fringe
            for (k in 0 until 4) drawLine(cols[2], Offset(scarf.left + (k + 0.5f) * scarf.width / 4f, scarf.bottom), Offset(scarf.left + (k + 0.5f) * scarf.width / 4f, scarf.bottom + 0.01f * u), pen.lw * 0.9f, StrokeCap.Round)
        }
        // Eight legs, four on each side, curving down and in.
        for (s in listOf(-1f, 1f)) {
            for (k in 0 until 4) {
                val ly = sy + (k - 1.5f) * 0.006f
                val legEnd = Offset((sx + s * (0.062f + 0.006f * k)) * u, (sy + 0.012f + 0.016f * k + sin(t * 3f + k + s) * 0.004f) * u)
                val leg = Path().apply {
                    moveTo((sx + s * 0.02f) * u, ly * u)
                    quadraticTo((sx + s * (0.055f + 0.004f * k)) * u, (ly - 0.03f + 0.006f * k) * u, legEnd.x, legEnd.y)
                }
                drawPath(leg, ink, style = Stroke(0.007f * u + pen.lw, cap = StrokeCap.Round))
                drawPath(leg, bodyCol.darken(0.1f), style = Stroke(0.007f * u, cap = StrokeCap.Round))
            }
        }
        // Body: a round abdomen with a pattern, a head with big eyes and a smile.
        inkedCircle(p(sx, sy + 0.012f), 0.034f * u, bodyCol, pen)
        drawCircle(bodyCol.lighten(0.35f), 0.008f * u, p(sx - 0.01f, sy + 0.022f))
        drawCircle(bodyCol.lighten(0.35f), 0.006f * u, p(sx + 0.012f, sy + 0.028f))
        inkedCircle(p(sx, sy - 0.016f), 0.026f * u, bodyCol.lighten(0.08f), pen)
        for (s in listOf(-1f, 1f)) {
            val e = p(sx + s * 0.011f, sy - 0.02f)
            drawCircle(Color.White, 0.0085f * u, e)
            drawCircle(ink, 0.0045f * u, Offset(e.x + s * -0.0005f * u + sin(t * 0.8f) * 0.002f * u, e.y + 0.0015f * u))
            drawCircle(Color.White, 0.0016f * u, Offset(e.x + 0.002f * u, e.y - 0.002f * u))
        }
        drawArc(ink, 20f, 140f, false, Offset((sx - 0.009f) * u, (sy - 0.014f) * u), Size(0.018f * u, 0.014f * u), style = pen.thin)
        drawCircle(Color(0xFFFF6F91).copy(alpha = 0.5f), 0.005f * u, p(sx - 0.019f, sy - 0.011f))
        drawCircle(Color(0xFFFF6F91).copy(alpha = 0.5f), 0.005f * u, p(sx + 0.019f, sy - 0.011f))
        // Two needles in her front legs, clicking.
        val click = sin(t * 7f) * 0.006f
        drawLine(ink, p(sx - 0.05f, sy + 0.03f + click), p(sx + 0.02f, sy + 0.036f), 0.005f * u + pen.lw, StrokeCap.Round)
        drawLine(Color(0xFFE0B04A), p(sx - 0.05f, sy + 0.03f + click), p(sx + 0.02f, sy + 0.036f), 0.005f * u, StrokeCap.Round)
        drawLine(ink, p(sx + 0.05f, sy + 0.03f - click), p(sx - 0.02f, sy + 0.036f), 0.005f * u + pen.lw, StrokeCap.Round)
        drawLine(Color(0xFFE0B04A), p(sx + 0.05f, sy + 0.03f - click), p(sx - 0.02f, sy + 0.036f), 0.005f * u, StrokeCap.Round)
        // A beret on one spider, a bow on the other.
        if (!flip) {
            val beret = Path().apply {
                addOval(Rect((sx - 0.03f) * u, (sy - 0.05f) * u, (sx + 0.03f) * u, (sy - 0.03f) * u))
            }
            inked(beret, Color(0xFFD2443A), pen)
            atDot(p(sx, sy - 0.054f), 0.004f * u, Color(0xFFD2443A), pen)
        } else {
            for (s in listOf(-1f, 1f)) {
                val bow = Path().apply {
                    moveTo(p(sx + 0.012f, sy - 0.04f).x, p(sx + 0.012f, sy - 0.04f).y)
                    lineTo(p(sx + 0.012f + s * 0.02f, sy - 0.05f).x, p(sx + 0.012f + s * 0.02f, sy - 0.05f).y)
                    lineTo(p(sx + 0.012f + s * 0.02f, sy - 0.03f).x, p(sx + 0.012f + s * 0.02f, sy - 0.03f).y)
                    close()
                }
                inked(bow, Color(0xFFFF6FA8), pen, shade = false)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------- the carton

internal fun DrawScope.atCarton(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val kraft = Color(0xFFC9A06A)
    atShadow(u, 0.28f, 0.13f)
    // The lower box.
    fxBox(u, -0.125f, -0.135f, 0.125f, -0.005f, 0.12f, kraft, pen, rad = 0.004f)
    drawRect(Color(0xFFE8D8B0).copy(alpha = 0.85f), p(-0.012f, -0.135f), Size(0.024f * u, 0.13f * u))
    // Labels with pictures instead of words: an apple, a duck, a sun.
    val lab = Rect(p(-0.1f, -0.1f).x, p(0f, -0.1f).y, p(-0.04f, 0f).x, p(0f, -0.045f).y)
    drawRect(Color(0xFFF7F0DC), lab.topLeft, lab.size)
    drawRect(Ink.line, lab.topLeft, lab.size, style = pen.thin)
    drawCircle(Color(0xFFD2443A), 0.012f * u, lab.center)
    drawLine(Color(0xFF3F8F6C), Offset(lab.center.x, lab.center.y - 0.012f * u), Offset(lab.center.x + 0.006f * u, lab.center.y - 0.02f * u), 0.004f * u, StrokeCap.Round)
    drawCircle(Color(0xFFFFC83D), 0.013f * u, p(0.07f, -0.075f))
    for (k in 0 until 8) {
        val a = k * 0.7854f
        drawLine(Color(0xFFFFC83D), p(0.07f + cos(a) * 0.019f, -0.075f + sin(a) * 0.019f), p(0.07f + cos(a) * 0.027f, -0.075f + sin(a) * 0.027f), 0.004f * u, StrokeCap.Round)
    }
    // A dent in one corner.
    drawPath(Path().apply { moveTo(p(0.125f, -0.135f).x, p(0.125f, -0.135f).y); lineTo(p(0.095f, -0.135f).x, p(0.095f, -0.135f).y); lineTo(p(0.125f, -0.105f).x, p(0.125f, -0.105f).y); close() }, kraft.darken(0.18f))
    // The upper box, a bit smaller and turned a little.
    val zOff = 0.012f
    if (!f.open) {
        fxBox(u, -0.105f, -0.245f, 0.095f, -0.135f, 0.1f, kraft.lighten(0.06f), pen, rad = 0.004f, z = zOff)
        val fl = fxFront(u, -0.105f, -0.245f, 0.095f, -0.135f, zOff)
        drawRect(Color(0xFFE8D8B0).copy(alpha = 0.85f), Offset(fl.center.x - 0.012f * u, fl.top), Size(0.024f * u, fl.height))
        drawLine(Ink.line, Offset(fl.center.x, fl.top), Offset(fl.center.x, fl.bottom), pen.lw * 0.5f)
        // A knot of string and a pencil doodle: someone drew a smile on it.
        drawArc(Ink.line, 20f, 140f, false, Offset(fl.left + fl.width * 0.2f, fl.top + fl.height * 0.45f), Size(0.025f * u, 0.016f * u), style = pen.thin)
        drawCircle(Ink.line, 0.0035f * u, Offset(fl.left + fl.width * 0.2f, fl.top + fl.height * 0.38f))
        drawCircle(Ink.line, 0.0035f * u, Offset(fl.left + fl.width * 0.2f + 0.025f * u, fl.top + fl.height * 0.38f))
    } else {
        // Flaps flung open; the dark inside shows a spring waiting to pop.
        val d = 0.1f
        fxBox(u, -0.105f, -0.245f, 0.095f, -0.135f, d, kraft.lighten(0.06f), pen, rad = 0.004f, z = zOff, top = Color(0xFF2A1E2E))
        fxFace(fxFlat(u, -0.1f, 0.09f, -0.245f, zOff + 0.005f, zOff + d - 0.005f), Color(0xFF241A38), pen)
        // the spring, bobbing
        val springTop = -0.3f + sin(t * 5f) * 0.008f
        val path = Path()
        val c0 = q(0f, -0.245f, 0.05f)
        path.moveTo(c0.x, c0.y)
        for (k in 1..6) path.lineTo(c0.x + (if (k % 2 == 0) -1f else 1f) * 0.012f * u, c0.y + (springTop + 0.245f) * u * k / 6f)
        drawPath(path, Ink.line, style = Stroke(0.007f * u + pen.lw, cap = StrokeCap.Round))
        drawPath(path, Color(0xFFBAC4D4), style = Stroke(0.005f * u, cap = StrokeCap.Round))
        val top = q(0f, springTop, 0.05f)
        drawPath(starPath(top, 0.02f * u, 0.009f * u, t * 20f), Color(0xFFFFC83D))
        drawPath(starPath(top, 0.02f * u, 0.009f * u, t * 20f), Ink.line, style = pen.thin)
        // flaps: two small ones to the sides, a big one folded down in front, one standing up at the back.
        val frontFlap = fxQuad(q(-0.105f, -0.245f, zOff).x, q(-0.105f, -0.245f, zOff).y, q(0.095f, -0.245f, zOff).x, q(0.095f, -0.245f, zOff).y, q(0.085f, -0.205f, zOff - 0.02f).x, q(0.085f, -0.205f, zOff - 0.02f).y, q(-0.095f, -0.205f, zOff - 0.02f).x, q(-0.095f, -0.205f, zOff - 0.02f).y)
        fxFace(frontFlap, kraft.darken(0.05f), pen)
        val backFlap = fxQuad(q(-0.105f, -0.245f, zOff + d).x, q(-0.105f, -0.245f, zOff + d).y, q(0.095f, -0.245f, zOff + d).x, q(0.095f, -0.245f, zOff + d).y, q(0.09f, -0.315f, zOff + d).x, q(0.09f, -0.315f, zOff + d).y, q(-0.1f, -0.315f, zOff + d).x, q(-0.1f, -0.315f, zOff + d).y)
        fxFace(backFlap, kraft.lighten(0.1f), pen)
    }
}

// --------------------------------------------------------------------------------------------- lantern

internal fun DrawScope.atLantern(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val iron = Color(0xFF3A3844)
    // The chain up to the rafter.
    for (k in 0 until 4) {
        val y = -0.215f - k * 0.04f
        drawOval(iron, Offset(-0.007f * u, y * u), Size(0.014f * u, 0.032f * u), style = Stroke(0.004f * u + pen.lw))
    }
    val sway = sin(t * 0.9f + f.id) * 0.002f * u
    translate(sway, 0f) {
        if (f.on) {
            fxGlow(p(0f, -0.09f), 0.17f * u, AtC.warm, 0.5f + 0.12f * sin(t * 6f))
        }
        // Cap, glass cage, base.
        val cap = Path().apply { moveTo(p(-0.05f, -0.17f).x, p(-0.05f, -0.17f).y); lineTo(p(-0.015f, -0.205f).x, p(-0.015f, -0.205f).y); lineTo(p(0.015f, -0.205f).x, p(0.015f, -0.205f).y); lineTo(p(0.05f, -0.17f).x, p(0.05f, -0.17f).y); close() }
        inked(cap, iron, pen)
        atDot(p(0f, -0.21f), 0.008f * u, iron, pen)
        val glass = Rect(p(-0.04f, -0.17f).x, p(-0.04f, -0.17f).y, p(0.04f, -0.04f).x, p(0.04f, -0.04f).y)
        drawRoundRect(if (f.on) Color(0x66FFE9A8) else Color(0x44CFE2F0), glass.topLeft, glass.size, androidx.compose.ui.geometry.CornerRadius(0.008f * u))
        if (f.on) {
            fxFire(p(0f, -0.045f).x, p(0f, -0.045f).y, 0.03f * u, 0.07f * u, t, f.id.toFloat(), pen)
        } else {
            drawLine(Color(0xFFE9E1D0), p(0f, -0.045f), p(0f, -0.08f), 0.01f * u, StrokeCap.Round)
            fxPuffs(0f, -0.085f * u, t, 0.008f * u, 0.07f * u, Color(0xFFD8D4DC), 0.45f, 3, 0.35f, 0.012f * u)
        }
        drawRoundRect(Ink.line, glass.topLeft, glass.size, androidx.compose.ui.geometry.CornerRadius(0.008f * u), style = pen.stroke)
        for (x in listOf(-0.04f, 0.04f)) drawLine(iron, p(x, -0.17f), p(x, -0.04f), pen.lw * 1.5f)
        drawLine(iron, p(0f, -0.17f), p(0f, -0.04f), pen.lw * 0.8f, alpha = 0.7f)
        drawLine(iron, p(-0.04f, -0.105f), p(0.04f, -0.105f), pen.lw * 0.8f, alpha = 0.7f)
        val base = Path().apply { moveTo(p(-0.045f, -0.04f).x, p(-0.045f, -0.04f).y); lineTo(p(0.045f, -0.04f).x, p(0.045f, -0.04f).y); lineTo(p(0.03f, -0.015f).x, p(0.03f, -0.015f).y); lineTo(p(-0.03f, -0.015f).x, p(-0.03f, -0.015f).y); close() }
        inked(base, iron, pen)
        shine(p(-0.025f, -0.14f), 0.008f * u, 0.03f * u, 0.55f)
    }
}

// ------------------------------------------------------------------------------------ round window

internal fun DrawScope.atRoundWindow(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val n = pen.night
    val c = p(0f, -0.14f)
    val r = 0.115f * u
    val wood = Color(0xFFD9B98A)
    // The sky outside: blue with a cloud by day, deep blue with stars and a moon by night.
    val skyTop = androidx.compose.ui.graphics.lerp(Color(0xFF6FB8EE), Color(0xFF0F0D35), n)
    val skyLow = androidx.compose.ui.graphics.lerp(Color(0xFFDDF3FF), Color(0xFF4A3B8F), n)
    val glass = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    drawCircle(wood, r + 0.016f * u, c)
    drawCircle(Ink.line, r + 0.016f * u, c, style = pen.stroke)
    clipPath(glass) {
        drawRect(Brush.verticalGradient(listOf(skyTop, skyLow), startY = c.y - r, endY = c.y + r), Offset(c.x - r, c.y - r), Size(2f * r, 2f * r))
        if (n > 0.3f) {
            for (i in 0 until 6) twinkle(Offset(c.x + (hash01(i, 5) - 0.5f) * 1.6f * r, c.y + (hash01(i, 6) - 0.7f) * 1.2f * r), 0.01f * u * (0.5f + 0.5f * atWave(t, 1.5f + i * 0.3f, i * 1.1f)), Color(0xFFFFF7DA), ramp((n - 0.3f) / 0.5f))
            drawCircle(Color(0xFFFFF0BF), 0.026f * u, Offset(c.x + 0.04f * u, c.y - 0.04f * u), alpha = ramp((n - 0.3f) / 0.5f))
            drawCircle(skyTop, 0.022f * u, Offset(c.x + 0.052f * u, c.y - 0.048f * u), alpha = ramp((n - 0.3f) / 0.5f))
        } else {
            drawPath(cloudPath(c.x - 0.02f * u, c.y - 0.03f * u, 0.032f * u), Color.White, alpha = 1f - n)
        }
        // A bird (or a bat, but a friendly one) flies by now and then: a small ink wing-beat.
        if (atEvery(t, 9f, 2.5f, f.id * 1.7f)) {
            val ph = ((t + f.id * 1.7f) % 9f) / 2.5f
            val bx = c.x - r + ph * 2f * r
            val flap = sin(t * 9f) * 0.008f * u
            val wing = Path().apply { moveTo(bx - 0.012f * u, by(c, ph) - flap); quadraticTo(bx - 0.004f * u, by(c, ph) - 0.012f * u, bx, by(c, ph)); quadraticTo(bx + 0.004f * u, by(c, ph) - 0.012f * u, bx + 0.012f * u, by(c, ph) - flap) }
            drawPath(wing, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        }
        if (f.on) {
            // The pane swings in: the opening is open air, with the breeze blowing leaves.
            for (i in 0 until 3) {
                val ph = fxFrac(t * 0.5f + i / 3f)
                drawLine(Color.White.copy(alpha = 0.7f * (1f - ph)), Offset(c.x - r + ph * 2f * r, c.y - 0.04f * u + i * 0.04f * u), Offset(c.x - r + ph * 2f * r + 0.05f * u, c.y - 0.04f * u + i * 0.04f * u), 0.004f * u, StrokeCap.Round)
            }
        }
    }
    if (f.on) {
        // The open pane seen from the side: a narrow oval at the left.
        val pane = Path().apply { addOval(Rect(c.x - r - 0.03f * u, c.y - r, c.x - r + 0.02f * u, c.y + r)) }
        drawPath(pane, Color(0x66CFE2F0))
        drawPath(pane, wood, style = Stroke(0.012f * u))
        drawPath(pane, Ink.line, style = pen.stroke)
    } else {
        // Muntins: a white cross and a ring.
        drawLine(Color.White, Offset(c.x - r, c.y), Offset(c.x + r, c.y), 0.01f * u)
        drawLine(Color.White, Offset(c.x, c.y - r), Offset(c.x, c.y + r), 0.01f * u)
        drawLine(Ink.line, Offset(c.x - r, c.y), Offset(c.x + r, c.y), pen.lw * 0.5f)
        drawLine(Ink.line, Offset(c.x, c.y - r), Offset(c.x, c.y + r), pen.lw * 0.5f)
        shine(Offset(c.x - r * 0.45f, c.y - r * 0.4f), 0.022f * u, 0.05f * u, 0.45f)
    }
    drawCircle(Ink.line, r, c, style = pen.stroke)
    // The sill and a flowerpot with a happy flower.
    inkedRound(Rect(-0.15f * u, -0.018f * u, 0.15f * u, 0f), 0.004f * u, wood.darken(0.1f), pen)
    val pot = Path().apply { moveTo(p(0.065f, -0.018f).x, p(0.065f, -0.018f).y); lineTo(p(0.12f, -0.018f).x, p(0.12f, -0.018f).y); lineTo(p(0.113f, -0.06f).x, p(0.113f, -0.06f).y); lineTo(p(0.072f, -0.06f).x, p(0.072f, -0.06f).y); close() }
    inked(pot, Color(0xFFD9774F), pen)
    val sway = sin(t * 1.3f) * 0.004f
    drawLine(Color(0xFF3F8F6C), p(0.092f, -0.06f), p(0.092f + sway, -0.1f), 0.005f * u, StrokeCap.Round)
    for (k in 0 until 5) {
        val a = k * 1.2566f
        drawCircle(Color(0xFFF7F3EC), 0.008f * u, p(0.092f + sway + cos(a) * 0.012f, -0.108f + sin(a) * 0.012f))
    }
    drawCircle(Color(0xFFFFC83D), 0.007f * u, p(0.092f + sway, -0.108f))
    drawLine(Color(0xFF3F8F6C), p(0.092f, -0.07f), p(0.075f, -0.085f), 0.005f * u, StrokeCap.Round)
}

private fun by(c: Offset, ph: Float): Float = c.y - 0.05f * cos(ph * 3.1415927f)
