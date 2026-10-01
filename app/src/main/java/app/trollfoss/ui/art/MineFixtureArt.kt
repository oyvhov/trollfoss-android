package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/*
 * Mitt hus: the furniture of the rooms and the yard, in oblique 3D like the rest of the home furniture. Origin at the
 * bottom centre of the piece; sizes in scene units times [u]. State comes from the fixture (`on`, `mode`, `count`) so
 * the sprite cache keeps up; motion comes from `pen.t`.
 */

internal fun DrawScope.drawMineFixture(f: Fixture, u: Float, pen: Pen, front: Boolean): Boolean {
    if (front) return when (f.type) {
        FixtureType.MI_SWING, FixtureType.MI_ROCKING_HORSE -> false
        else -> false
    }
    when (f.type) {
        FixtureType.MI_FIREPLACE -> miFireplace(f, u, pen)
        FixtureType.MI_DINING_TABLE -> miDiningTable(f, u, pen)
        FixtureType.MI_CHANDELIER -> miChandelier(f, u, pen)
        FixtureType.MI_COUNTER -> miCounter(f, u, pen)
        FixtureType.MI_BEDSIDE -> miBedside(f, u, pen)
        FixtureType.MI_BLOCKS -> miBlocks(f, u, pen)
        FixtureType.MI_ROCKING_HORSE -> miHorse(f, u, pen)
        FixtureType.MI_BIG_BOOKCASE -> miBigBookcase(f, u, pen)
        FixtureType.MI_GLOBE -> miGlobe(f, u, pen)
        FixtureType.MI_SAW_BENCH -> miSawBench(f, u, pen)
        FixtureType.MI_LAUNDRY_BASKET -> miBasket(f, u, pen)
        FixtureType.MI_GUITAR -> miGuitar(f, u, pen)
        FixtureType.MI_PLANT_BED -> miPlantBed(f, u, pen)
        FixtureType.MI_HANGING_POT -> miHangingPot(f, u, pen)
        FixtureType.MI_COAT_RACK -> miCoatRack(f, u, pen)
        FixtureType.MI_MAILBOX -> miMailbox(f, u, pen)
        FixtureType.MI_FENCE -> miFence(f, u, pen)
        FixtureType.MI_FLOWER_BED -> miFlowerBed(f, u, pen)
        FixtureType.MI_SWING -> miSwing(f, u, pen)
        FixtureType.MI_BIRD_BATH -> miBirdBath(f, u, pen)
        FixtureType.MI_GNOME -> miGnome(f, u, pen)
        FixtureType.MI_SANDBOX -> miSandbox(f, u, pen)
        FixtureType.MI_APPLE_TREE -> miAppleTree(f, u, pen)
        FixtureType.DOOR -> if (f.place.mine) miDoor(f, u, pen) else return false
        FixtureType.STAIRCASE -> if (f.place.mine) miStairs(f, u, pen) else return false
        else -> return false
    }
    return true
}

private fun DrawScope.miShadow(u: Float, w: Float, d: Float = 0.1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow, Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

private fun p(u: Float, x: Float, y: Float) = Offset(x * u, y * u)

// ------------------------------------------------------------------------------------------------ rooms

private fun DrawScope.miFireplace(f: Fixture, u: Float, pen: Pen) {
    val d = 0.14f
    val brick = Color(0xFFC0755C)
    miShadow(u, 0.36f, d)
    // The brick breast and the mantel.
    fxBox(u, -0.17f, -0.31f, 0.17f, 0f, d, brick, pen, rad = 0.004f)
    val lines = ArrayList<Offset>()
    var y = -0.28f
    var row = 0
    while (y < -0.01f) {
        lines.add(p(u, -0.17f, y)); lines.add(p(u, 0.17f, y))
        var x = -0.17f + (if (row % 2 == 0) 0.03f else 0.06f)
        while (x < 0.17f) { lines.add(p(u, x, y)); lines.add(p(u, x, y + 0.03f)); x += 0.06f }
        y += 0.03f
        row++
    }
    drawPoints(lines, androidx.compose.ui.graphics.PointMode.Lines, brick.darken(0.3f), strokeWidth = pen.lw * 0.5f)
    fxBox(u, -0.2f, -0.34f, 0.2f, -0.305f, d + 0.03f, FxC.oak, pen, rad = 0.004f, z = -0.015f)
    // The fire opening.
    val hole = Path().apply {
        moveTo(-0.095f * u, 0f); lineTo(-0.095f * u, -0.14f * u)
        quadraticTo(0f, -0.23f * u, 0.095f * u, -0.14f * u); lineTo(0.095f * u, 0f); close()
    }
    drawPath(hole, Color(0xFF2B1F2E))
    drawPath(hole, Ink.line, style = pen.stroke)
    // Logs and the fire.
    drawLine(Ink.line, p(u, -0.07f, -0.015f), p(u, 0.07f, -0.03f), strokeWidth = 0.026f * u + pen.lw * 2, cap = StrokeCap.Round)
    drawLine(FxC.walnut, p(u, -0.07f, -0.015f), p(u, 0.07f, -0.03f), strokeWidth = 0.026f * u, cap = StrokeCap.Round)
    drawLine(Ink.line, p(u, -0.06f, -0.03f), p(u, 0.06f, -0.012f), strokeWidth = 0.022f * u + pen.lw * 2, cap = StrokeCap.Round)
    drawLine(FxC.wood, p(u, -0.06f, -0.03f), p(u, 0.06f, -0.012f), strokeWidth = 0.022f * u, cap = StrokeCap.Round)
    if (f.on) {
        val big = 1f + f.anim * 0.5f
        drawCircle(FxC.warm.copy(alpha = 0.22f), 0.13f * u, p(u, 0f, -0.07f))
        fxFire(0f, -0.03f * u, 0.12f * u * big, 0.15f * u * big, pen.t, 1f, pen)
        fxFire(-0.04f * u, -0.025f * u, 0.07f * u, 0.09f * u, pen.t, 2.3f, pen, outline = false)
    } else {
        drawOval(Color(0xFF6E6A76), Offset(-0.05f * u, -0.03f * u), Size(0.1f * u, 0.02f * u))
    }
    // Two stockings on the mantel, swinging a little.
    for (k in 0 until 2) {
        val x = -0.11f + k * 0.22f
        val sw = sin(pen.t * 1.7f + k * 2f) * 0.006f * u + f.anim * sin(pen.t * 20f) * 0.01f * u
        val col = if (k == 0) FxC.red else FxC.green
        val s = Path().apply {
            moveTo(x * u + sw, -0.305f * u); lineTo((x + 0.032f) * u + sw, -0.305f * u); lineTo((x + 0.032f) * u + sw, -0.24f * u)
            quadraticTo((x + 0.05f) * u + sw, -0.215f * u, (x + 0.015f) * u + sw, -0.22f * u); lineTo(x * u + sw, -0.24f * u); close()
        }
        drawPath(s, col)
        drawPath(s, Ink.line, style = pen.thin)
        drawRect(Color.White, Offset(x * u + sw, -0.305f * u), Size(0.032f * u, 0.016f * u))
    }
}

private fun DrawScope.miDiningTable(f: Fixture, u: Float, pen: Pen) {
    val d = 0.18f
    miShadow(u, 0.48f, d)
    for (x in listOf(-0.2f, 0.2f)) for (z in listOf(0.02f, d - 0.04f)) fxBox(u, x - 0.012f, -0.115f, x + 0.012f, 0f, 0.02f, FxC.walnut, pen, z = z)
    fxBox(u, -0.23f, -0.13f, 0.23f, -0.105f, d, FxC.oakDark, pen, rad = 0.005f)
    // A runner down the middle, plates and glasses.
    fxFace(fxFlat(u, -0.21f, 0.21f, -0.13f, d * 0.38f, d * 0.62f), Color(0xFFD2443A).lighten(0.1f), pen)
    for (x in listOf(-0.15f, -0.05f, 0.05f, 0.15f)) {
        for (z in listOf(0.03f, d - 0.03f)) {
            val c = fxQ(u, x, -0.13f, z)
            drawPath(fxDisc2(c.x, c.y, 0.026f * u, 0.017f * u), Color.White)
            drawPath(fxDisc2(c.x, c.y, 0.026f * u, 0.017f * u), Ink.line, style = pen.thin)
            drawPath(fxDisc2(c.x, c.y, 0.014f * u, 0.009f * u), Color(0xFFBFE3FA), style = pen.thin)
        }
    }
    // The candle in the middle.
    val c = fxQ(u, 0f, -0.13f, d / 2f)
    drawRoundRect(Color(0xFFFFF7EA), Offset(c.x - 0.007f * u, c.y - 0.05f * u), Size(0.014f * u, 0.05f * u), androidx.compose.ui.geometry.CornerRadius(0.003f * u))
    drawRoundRect(Ink.line, Offset(c.x - 0.007f * u, c.y - 0.05f * u), Size(0.014f * u, 0.05f * u), androidx.compose.ui.geometry.CornerRadius(0.003f * u), style = pen.thin)
    if (f.on) {
        drawCircle(FxC.warm.copy(alpha = 0.3f), 0.06f * u, Offset(c.x, c.y - 0.06f * u))
        fxFire(c.x, c.y - 0.05f * u, 0.016f * u, 0.032f * u, pen.t, 3f, pen)
    }
}

private fun DrawScope.miChandelier(f: Fixture, u: Float, pen: Pen) {
    val swing = if (f.anim > 0.01f) sin(pen.t * 14f) * 7f * f.anim else 0f
    val top = Offset(0f, -0.5f * u)
    drawLine(Ink.line, top, Offset(0f, -0.12f * u), strokeWidth = pen.lw * 1.6f)
    rotate(swing, Offset(0f, -0.5f * u)) {
        drawLine(FxC.brass, Offset(0f, -0.5f * u), Offset(0f, -0.115f * u), strokeWidth = pen.lw * 0.8f)
        // Arms and candle bulbs.
        for (k in -2..2) {
            val x = k * 0.045f
            val y = -0.07f + abs(k) * 0.012f
            drawLine(Ink.line, Offset(0f, -0.09f * u), Offset(x * u, y * u), strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
            drawLine(FxC.brass, Offset(0f, -0.09f * u), Offset(x * u, y * u), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
            drawCircle(if (f.on) FxC.warm else Color(0xFFFFF3C4), 0.011f * u, Offset(x * u, (y - 0.016f) * u))
            drawCircle(Ink.line, 0.011f * u, Offset(x * u, (y - 0.016f) * u), style = pen.thin)
            if (f.on) drawCircle(FxC.warm.copy(alpha = 0.25f), 0.04f * u, Offset(x * u, (y - 0.016f) * u))
        }
        inkedCircle(Offset(0f, -0.09f * u), 0.02f * u, FxC.brass, pen)
        // Crystals.
        for (k in -2..2) drawPath(fxLeaf(k * 0.04f * u, -0.055f * u, k * 0.04f * u, (-0.025f + abs(k) * 0.006f) * u, 0.4f), Color(0xFFBFE9F2).copy(alpha = 0.85f))
    }
}

private fun DrawScope.miCounter(f: Fixture, u: Float, pen: Pen) {
    val d = 0.15f
    miShadow(u, 0.36f, d)
    fxBox(u, -0.17f, -0.17f, 0.17f, 0f, d, Color(0xFFF1EEE6), pen, rad = 0.005f)
    // Two cupboard doors with knobs.
    for (k in 0 until 2) {
        val x0 = -0.15f + k * 0.15f
        inkedRound(Rect(x0 * u, -0.155f * u, (x0 + 0.14f) * u, -0.02f * u), 0.004f * u, Color(0xFF8FD9C0), pen, shade = false)
        drawCircle(FxC.brass, 0.005f * u, Offset((x0 + (if (k == 0) 0.125f else 0.015f)) * u, -0.09f * u))
    }
    fxBox(u, -0.185f, -0.2f, 0.185f, -0.17f, d + 0.015f, FxC.oak, pen, rad = 0.004f)
    // The toaster, with two slices that pop up.
    val tx = -0.06f
    val tz = 0.04f
    val tp = fxQ(u, tx, -0.2f, tz)
    inkedRound(Rect(tp.x - 0.032f * u, tp.y - 0.04f * u, tp.x + 0.032f * u, tp.y), 0.008f * u, FxC.steel, pen)
    if (f.on) {
        for (k in 0 until 2) {
            val sx = tp.x + (k - 0.5f) * 0.03f * u
            inkedRound(Rect(sx - 0.011f * u, tp.y - 0.075f * u, sx + 0.011f * u, tp.y - 0.034f * u), 0.004f * u, Color(0xFFD9A058), pen, shade = false)
        }
    } else drawLine(Ink.line, Offset(tp.x - 0.02f * u, tp.y - 0.033f * u), Offset(tp.x + 0.02f * u, tp.y - 0.033f * u), strokeWidth = pen.lw * 1.4f)
    drawCircle(Color(0xFFD2443A), 0.004f * u, Offset(tp.x + 0.024f * u, tp.y - 0.02f * u))
    // A kettle and a bowl of fruit.
    val kp = fxQ(u, 0.05f, -0.2f, 0.05f)
    inkedRound(Rect(kp.x - 0.022f * u, kp.y - 0.05f * u, kp.x + 0.022f * u, kp.y), 0.012f * u, FxC.sky, pen)
    drawLine(Ink.line, Offset(kp.x + 0.02f * u, kp.y - 0.035f * u), Offset(kp.x + 0.04f * u, kp.y - 0.05f * u), strokeWidth = pen.lw * 1.5f, cap = StrokeCap.Round)
    val bp = fxQ(u, 0.13f, -0.2f, 0.07f)
    inkedRound(Rect(bp.x - 0.025f * u, bp.y - 0.018f * u, bp.x + 0.025f * u, bp.y), 0.01f * u, FxC.cream, pen)
    drawCircle(Color(0xFFD2443A), 0.012f * u, Offset(bp.x - 0.01f * u, bp.y - 0.022f * u))
    drawCircle(Color(0xFFFFC83D), 0.012f * u, Offset(bp.x + 0.008f * u, bp.y - 0.024f * u))
}

private fun DrawScope.miBedside(f: Fixture, u: Float, pen: Pen) {
    val d = 0.1f
    miShadow(u, 0.12f, d)
    fxBox(u, -0.05f, -0.12f, 0.05f, 0f, d, FxC.oakDark, pen, rad = 0.004f)
    inkedRound(Rect(-0.04f * u, -0.095f * u, 0.04f * u, -0.05f * u), 0.003f * u, FxC.oak, pen, shade = false)
    drawCircle(FxC.brass, 0.004f * u, Offset(0f, -0.072f * u))
    if (f.variant == 0) {
        // A crescent night light with stars.
        val c = fxQ(u, 0f, -0.145f, 0.05f)
        val glow = if (f.on) 1f else 0f
        if (f.on) drawCircle(FxC.warm.copy(alpha = 0.3f), 0.11f * u, Offset(c.x, c.y - 0.01f * u))
        drawCircle(Ink.line, 0.026f * u + pen.lw, c)
        drawCircle(if (f.on) FxC.warm else Color(0xFFD9D4E6), 0.026f * u, c)
        drawCircle(FxC.paint.copy(alpha = 0f), 0.01f * u, Offset(c.x + 0.012f * u, c.y - 0.006f * u))
        if (f.on) for (k in 0 until 4) {
            val a = k * 1.7f + pen.t * 0.4f
            twinkle(Offset(c.x + cos(a) * 0.1f * u, c.y - 0.08f * u + sin(a) * 0.05f * u), 0.014f * u, FxC.warm, glow)
        }
    } else {
        // A little alarm clock and a book.
        val c = fxQ(u, -0.015f, -0.12f, 0.05f)
        drawCircle(Ink.line, 0.022f * u + pen.lw, Offset(c.x, c.y - 0.022f * u))
        drawCircle(FxC.mint, 0.022f * u, Offset(c.x, c.y - 0.022f * u))
        drawCircle(Color.White, 0.015f * u, Offset(c.x, c.y - 0.022f * u))
        drawLine(Ink.line, Offset(c.x, c.y - 0.022f * u), Offset(c.x + 0.007f * u, c.y - 0.028f * u), strokeWidth = pen.lw)
        drawLine(Ink.line, Offset(c.x, c.y - 0.022f * u), Offset(c.x, c.y - 0.034f * u), strokeWidth = pen.lw)
        fxBox(u, 0.01f, -0.134f, 0.045f, -0.12f, 0.05f, FxC.dusty, pen, z = 0.02f)
    }
}

private fun DrawScope.miBlocks(f: Fixture, u: Float, pen: Pen) {
    val colors = listOf(FxC.red, FxC.yellow, FxC.sky, FxC.green, FxC.pink)
    miShadow(u, 0.16f, 0.08f)
    if (f.mode == 0) {
        // A tower of five, a little crooked.
        for (k in 0 until 5) {
            val off = sin(k * 2.1f) * 0.012f
            val s = 0.058f - k * 0.002f
            fxBox(u, -s / 2f + off, -(k + 1) * 0.04f, s / 2f + off, -k * 0.04f, 0.05f, colors[k], pen, rad = 0.003f, z = 0.02f)
            // A dot or a stripe on each.
            val c = fxQ(u, off, -(k + 0.5f) * 0.04f, 0.02f)
            when (k % 3) {
                0 -> drawCircle(Color.White.copy(alpha = 0.8f), 0.008f * u, c)
                1 -> drawLine(Color.White.copy(alpha = 0.8f), Offset(c.x - 0.014f * u, c.y), Offset(c.x + 0.014f * u, c.y), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
                else -> drawPath(starPath(c, 0.011f * u, 0.005f * u), Color.White.copy(alpha = 0.85f))
            }
        }
        // A triangle on top.
        val top = fxQ(u, 0.004f, -0.2f, 0.045f)
        drawPath(Path().apply { moveTo(top.x - 0.03f * u, top.y); lineTo(top.x + 0.03f * u, top.y); lineTo(top.x, top.y - 0.04f * u); close() }, FxC.mustard)
        drawPath(Path().apply { moveTo(top.x - 0.03f * u, top.y); lineTo(top.x + 0.03f * u, top.y); lineTo(top.x, top.y - 0.04f * u); close() }, Ink.line, style = pen.stroke)
    } else {
        // Fallen blocks all over the floor.
        val spots = listOf(-0.06f to 0.03f, 0.0f to 0.06f, 0.06f to 0.02f, -0.03f to 0.0f, 0.045f to 0.065f)
        for ((k, s) in spots.withIndex()) {
            rotate(k * 23f - 30f, fxQ(u, s.first, -0.015f, s.second)) {
                fxBox(u, s.first - 0.025f, -0.035f, s.first + 0.025f, 0f, 0.05f, colors[k], pen, rad = 0.003f, z = s.second)
            }
        }
    }
}

private fun DrawScope.miHorse(f: Fixture, u: Float, pen: Pen) {
    val rock = if (f.on) sin(pen.t * 5f) * 7f else 0f
    miShadow(u, 0.24f, 0.1f)
    // The rockers.
    val rk = Path().apply {
        moveTo(-0.12f * u, -0.02f * u)
        quadraticTo(0f, 0.025f * u, 0.12f * u, -0.02f * u)
    }
    drawPath(rk, Ink.line, style = Stroke(0.02f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(rk, FxC.oak, style = Stroke(0.02f * u, cap = StrokeCap.Round))
    rotate(rock, Offset(0f, 0f)) {
        // Legs and body.
        for (x in listOf(-0.075f, 0.075f)) {
            drawLine(Ink.line, Offset(x * u, -0.08f * u), Offset(x * 1.4f * u, -0.025f * u), strokeWidth = 0.022f * u + pen.lw * 2f, cap = StrokeCap.Round)
            drawLine(FxC.wood, Offset(x * u, -0.08f * u), Offset(x * 1.4f * u, -0.025f * u), strokeWidth = 0.022f * u, cap = StrokeCap.Round)
        }
        inkedRound(Rect(-0.1f * u, -0.14f * u, 0.09f * u, -0.07f * u), 0.035f * u, FxC.wood.lighten(0.15f), pen)
        // Neck, head, mane, ear.
        val neck = Path().apply { moveTo(0.05f * u, -0.12f * u); lineTo(0.1f * u, -0.2f * u); lineTo(0.135f * u, -0.17f * u); lineTo(0.1f * u, -0.09f * u); close() }
        drawPath(neck, FxC.wood.lighten(0.15f)); drawPath(neck, Ink.line, style = pen.stroke)
        inkedRound(Rect(0.09f * u, -0.215f * u, 0.17f * u, -0.17f * u), 0.018f * u, FxC.wood.lighten(0.15f), pen)
        drawCircle(Ink.line, 0.005f * u, Offset(0.135f * u, -0.2f * u))
        drawCircle(Color.White, 0.0018f * u, Offset(0.136f * u, -0.2022f * u))
        drawPath(Path().apply { moveTo(0.095f * u, -0.215f * u); lineTo(0.09f * u, -0.24f * u); lineTo(0.115f * u, -0.218f * u); close() }, FxC.wood.darken(0.1f))
        for (k in 0 until 4) drawCircle(FxC.mustard, 0.012f * u, Offset((0.06f + k * 0.012f) * u, (-0.115f - k * 0.025f) * u))
        // Saddle and tail.
        inkedRound(Rect(-0.04f * u, -0.152f * u, 0.03f * u, -0.125f * u), 0.01f * u, FxC.red, pen)
        drawLine(FxC.mustard, Offset(-0.1f * u, -0.12f * u), Offset(-0.14f * u, -0.08f * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    }
}

private fun DrawScope.miBigBookcase(f: Fixture, u: Float, pen: Pen) {
    val d = 0.14f
    miShadow(u, 0.52f, d)
    fxBox(u, -0.25f, -0.44f, 0.25f, 0f, d, FxC.walnut.lighten(0.1f), pen, rad = 0.005f)
    val books = listOf(FxC.red, FxC.fjord, FxC.mustard, FxC.green, FxC.lilac, FxC.terracotta, FxC.sky, FxC.pink)
    for (shelf in 0 until 3) {
        val yb = -0.03f - shelf * 0.14f
        drawRect(Color(0xFF3A2A2E), Offset(-0.235f * u, (yb - 0.125f) * u), Size(0.47f * u, 0.125f * u))
        var x = -0.23f
        var k = shelf * 3
        while (x < 0.22f) {
            val bw = 0.016f + 0.01f * ((k * 7) % 3)
            val bh = 0.07f + 0.04f * ((k * 5) % 3) / 2f
            drawRect(books[k % books.size], Offset(x * u, (yb - bh) * u), Size(bw * u, bh * u))
            drawRect(Ink.line, Offset(x * u, (yb - bh) * u), Size(bw * u, bh * u), style = pen.thin)
            x += bw + 0.003f
            k++
        }
        fxLine(p(u, -0.235f, yb), p(u, 0.235f, yb), FxC.walnut, 0.012f * u)
    }
    // The rolling ladder: left, middle or right.
    val lx = -0.17f + f.mode * 0.17f
    drawLine(Ink.line, Offset(lx * u, -0.46f * u), Offset((lx + 0.03f) * u, 0f), strokeWidth = 0.016f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(FxC.oak, Offset(lx * u, -0.46f * u), Offset((lx + 0.03f) * u, 0f), strokeWidth = 0.016f * u, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset((lx + 0.05f) * u, -0.46f * u), Offset((lx + 0.08f) * u, 0f), strokeWidth = 0.016f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(FxC.oak, Offset((lx + 0.05f) * u, -0.46f * u), Offset((lx + 0.08f) * u, 0f), strokeWidth = 0.016f * u, cap = StrokeCap.Round)
    for (k in 1..4) {
        val y = -0.46f + k * 0.09f
        drawLine(FxC.oakDark, Offset((lx + 0.003f * k) * u, y * u), Offset((lx + 0.053f + 0.003f * k) * u, y * u), strokeWidth = 0.01f * u, cap = StrokeCap.Round)
    }
    // A plant on top.
    inkedRound(Rect(0.14f * u, -0.5f * u, 0.2f * u, -0.44f * u), 0.008f * u, FxC.terracotta, pen)
    drawCircle(Color(0xFF3BC46B), 0.035f * u, Offset(0.17f * u, -0.53f * u))
    drawCircle(Ink.line, 0.035f * u, Offset(0.17f * u, -0.53f * u), style = pen.thin)
}

private fun DrawScope.miGlobe(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.12f, 0.08f)
    // The stand.
    inkedRound(Rect(-0.04f * u, -0.02f * u, 0.04f * u, 0f), 0.008f * u, FxC.walnut, pen)
    drawLine(Ink.line, Offset(0f, -0.02f * u), Offset(0f, -0.075f * u), strokeWidth = 0.012f * u + pen.lw * 2f)
    drawLine(FxC.brass, Offset(0f, -0.02f * u), Offset(0f, -0.075f * u), strokeWidth = 0.012f * u)
    val c = Offset(0f, -0.105f * u)
    val r = 0.052f * u
    drawCircle(FxC.fjord, r, c)
    // The land, drifting round when the globe spins.
    val spin = if (f.on) pen.t * 2.2f else 0f
    for (k in 0 until 4) {
        val a = spin + k * 1.7f
        val x = cos(a) * r * 0.7f
        if (sin(a) > -0.2f) drawOval(FxC.leaf, Offset(c.x + x - r * 0.22f, c.y + (k - 1.5f) * r * 0.32f - r * 0.12f), Size(r * 0.44f * (0.6f + 0.4f * sin(a)), r * 0.26f))
    }
    drawCircle(Ink.line, r, c, style = pen.stroke)
    drawArc(Ink.line.copy(alpha = 0.5f), 200f, 140f, false, Offset(c.x - r * 1.12f, c.y - r * 1.12f), Size(r * 2.24f, r * 2.24f), style = Stroke(pen.lw * 1.4f))
    shine(Offset(c.x - r * 0.35f, c.y - r * 0.4f), r * 0.3f, r * 0.2f)
}

private fun DrawScope.miSawBench(f: Fixture, u: Float, pen: Pen) {
    val d = 0.16f
    miShadow(u, 0.32f, d)
    for (x in listOf(-0.12f, 0.12f)) for (z in listOf(0.02f, d - 0.04f)) fxBox(u, x - 0.012f, -0.17f, x + 0.012f, 0f, 0.02f, FxC.iron, pen, z = z)
    fxBox(u, -0.14f, -0.18f, 0.14f, -0.165f, d, FxC.steel, pen, rad = 0.004f)
    // The blade: a disc with teeth, standing up through the table.
    val c = fxQ(u, 0.03f, -0.19f, d / 2f)
    val r = 0.045f * u
    if (f.on) drawCircle(Color.White.copy(alpha = 0.25f), r * 1.2f, Offset(c.x, c.y - r * 0.6f))
    drawCircle(Ink.line, r + pen.lw, Offset(c.x, c.y - r * 0.5f))
    drawCircle(Color(0xFFD7DEE8), r, Offset(c.x, c.y - r * 0.5f))
    val spin = if (f.on) pen.t * 30f else 0.3f
    for (k in 0 until 12) {
        val a = spin + k * (2f * PI.toFloat() / 12f)
        drawLine(Ink.line, Offset(c.x + cos(a) * r * 0.85f, c.y - r * 0.5f + sin(a) * r * 0.85f), Offset(c.x + cos(a + 0.2f) * r * 1.05f, c.y - r * 0.5f + sin(a + 0.2f) * r * 1.05f), strokeWidth = pen.lw * 1.2f)
    }
    drawCircle(Ink.line, r * 0.16f, Offset(c.x, c.y - r * 0.5f))
    // A plank being cut, and sawdust.
    fxBox(u, -0.13f, -0.2f, -0.02f, -0.18f, 0.06f, FxC.plankColor(), pen, z = 0.05f)
    drawOval(Color(0xFFE3B27A), Offset(0.06f * u, -0.19f * u), Size(0.07f * u, 0.014f * u))
    // The motor box.
    inkedRound(Rect(0.09f * u, -0.17f * u, 0.14f * u, -0.05f * u), 0.006f * u, FxC.sky, pen)
}

private fun FxC.plankColor(): Color = Color(0xFFE3B27A)

private fun DrawScope.miBasket(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.13f, 0.08f)
    val b = Path().apply {
        moveTo(-0.055f * u, -0.115f * u); lineTo(0.055f * u, -0.115f * u); lineTo(0.045f * u, 0f); lineTo(-0.045f * u, 0f); close()
    }
    drawPath(b, Color(0xFFD9A873))
    // Weave.
    for (k in 1..4) drawLine(Color(0xFFB98650), Offset(-0.05f * u, -0.115f * u + k * 0.022f * u), Offset(0.05f * u, -0.115f * u + k * 0.022f * u), strokeWidth = pen.lw * 0.8f)
    for (k in -2..2) drawLine(Color(0xFFB98650), Offset(k * 0.02f * u, -0.115f * u), Offset(k * 0.017f * u, 0f), strokeWidth = pen.lw * 0.6f)
    drawPath(b, Ink.line, style = pen.stroke)
    // Socks poking out: odd ones.
    val cols = listOf(FxC.red, FxC.sky, FxC.mustard, FxC.green, FxC.pink)
    for (k in 0 until 4) {
        val x = -0.04f + k * 0.027f
        val wob = sin(pen.t * 2f + k) * 0.004f * u * (if (f.anim > 0.01f) 4f else 0f)
        val s = Path().apply {
            moveTo(x * u + wob, -0.11f * u); lineTo((x + 0.018f) * u + wob, -0.11f * u); lineTo((x + 0.02f) * u + wob, -0.145f * u - k % 2 * 0.012f * u)
            quadraticTo((x + 0.035f) * u + wob, -0.15f * u, (x + 0.025f) * u + wob, -0.163f * u - k % 2 * 0.012f * u); lineTo((x - 0.002f) * u + wob, -0.14f * u); close()
        }
        drawPath(s, cols[k % cols.size])
        drawPath(s, Ink.line, style = pen.thin)
        drawLine(Color.White, Offset((x + 0.002f) * u + wob, -0.128f * u), Offset((x + 0.018f) * u + wob, -0.128f * u), strokeWidth = pen.lw)
    }
    inkedRound(Rect(-0.06f * u, -0.125f * u, 0.06f * u, -0.108f * u), 0.006f * u, Color(0xFFD9A873), pen, shade = false)
}

private fun DrawScope.miGuitar(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.1f, 0.06f)
    val shake = if (f.on) sin(pen.t * 50f) * 0.0025f * u else 0f
    // The stand.
    drawLine(Ink.line, Offset(-0.03f * u, 0f), Offset(0f, -0.05f * u), strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset(0.03f * u, 0f), Offset(0f, -0.05f * u), strokeWidth = pen.lw * 2.4f, cap = StrokeCap.Round)
    rotate(-8f, Offset(0f, -0.07f * u)) {
        // The neck and head.
        drawLine(Ink.line, Offset(0f, -0.1f * u), Offset(0f, -0.19f * u), strokeWidth = 0.014f * u + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(FxC.walnut, Offset(0f, -0.1f * u), Offset(0f, -0.19f * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
        inkedRound(Rect(-0.012f * u, -0.215f * u, 0.012f * u, -0.185f * u), 0.005f * u, FxC.walnut, pen)
        // The body: two round lobes.
        inkedCircle(Offset(0f, -0.055f * u), 0.04f * u, FxC.terracotta.lighten(0.1f), pen)
        inkedCircle(Offset(0f, -0.1f * u), 0.03f * u, FxC.terracotta.lighten(0.1f), pen)
        drawCircle(Color(0xFF2B1F2E), 0.012f * u, Offset(0f, -0.07f * u))
        for (k in -1..1) drawLine(Color.White.copy(alpha = 0.8f), Offset((k * 0.004f + shake) * u, -0.19f * u), Offset((k * 0.004f - shake) * u, -0.065f * u), strokeWidth = pen.lw * 0.5f)
        drawLine(FxC.brass, Offset(-0.014f * u, -0.04f * u), Offset(0.014f * u, -0.04f * u), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    }
}

private fun DrawScope.miPlantBed(f: Fixture, u: Float, pen: Pen) {
    val d = 0.14f
    miShadow(u, 0.36f, d)
    fxBox(u, -0.17f, -0.1f, 0.17f, 0f, d, FxC.wood, pen, rad = 0.004f)
    fxGrain(Rect(-0.16f * u, -0.095f * u, 0.16f * u, -0.005f * u), FxC.wood, pen, 2)
    drawPath(fxFlat(u, -0.16f, 0.16f, -0.1f, 0.01f, d - 0.01f), FxC.soil)
    val stage = f.count.coerceIn(0, 3)
    val sunflower = f.variant == 1
    for (k in 0 until 4) {
        val x = -0.12f + k * 0.08f
        val base = fxQ(u, x, -0.1f, d * (0.35f + 0.3f * (k % 2)))
        val grow = (0.3f + stage * 0.28f) * (0.85f + 0.15f * ((k * 5) % 3) / 2f)
        val sway = sin(pen.t * 1.4f + k) * 0.004f * u
        val topY = base.y - (if (sunflower) 0.2f else 0.15f) * grow * u
        drawLine(Ink.line, base, Offset(base.x + sway, topY), strokeWidth = 0.007f * u + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(Color(0xFF3BC46B), base, Offset(base.x + sway, topY), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
        if (stage >= 1) {
            drawPath(fxLeaf(base.x, base.y - (base.y - topY) * 0.4f, base.x - 0.03f * u * grow, base.y - (base.y - topY) * 0.55f, 0.4f), Color(0xFF3BC46B))
            drawPath(fxLeaf(base.x, base.y - (base.y - topY) * 0.6f, base.x + 0.03f * u * grow, base.y - (base.y - topY) * 0.75f, 0.4f), Color(0xFF2E8B57))
        }
        if (stage == 3) {
            val c = Offset(base.x + sway, topY)
            if (sunflower) {
                for (a in 0 until 8) drawCircle(FxC.yellow, 0.011f * u, Offset(c.x + cos(a * 0.785f) * 0.02f * u, c.y + sin(a * 0.785f) * 0.02f * u))
                drawCircle(FxC.walnut, 0.014f * u, c)
                drawCircle(Ink.line, 0.014f * u, c, style = pen.thin)
            } else {
                drawCircle(FxC.red, 0.015f * u, Offset(c.x - 0.012f * u, c.y + 0.02f * u))
                drawCircle(FxC.red, 0.012f * u, Offset(c.x + 0.014f * u, c.y + 0.034f * u))
                drawCircle(Ink.line, 0.015f * u, Offset(c.x - 0.012f * u, c.y + 0.02f * u), style = pen.thin)
                drawCircle(Ink.line, 0.012f * u, Offset(c.x + 0.014f * u, c.y + 0.034f * u), style = pen.thin)
            }
        }
    }
}

private fun DrawScope.miHangingPot(f: Fixture, u: Float, pen: Pen) {
    val sway = if (f.on) sin(pen.t * 4f) * 8f else sin(pen.t * 0.9f) * 1.5f
    val top = Offset(0f, -0.3f * u)
    rotate(sway, top) {
        drawLine(Ink.line, top, Offset(-0.035f * u, -0.1f * u), strokeWidth = pen.lw * 1.2f)
        drawLine(Ink.line, top, Offset(0.035f * u, -0.1f * u), strokeWidth = pen.lw * 1.2f)
        inkedRound(Rect(-0.04f * u, -0.115f * u, 0.04f * u, -0.04f * u), 0.018f * u, FxC.terracotta, pen)
        drawLine(Color.White.copy(alpha = 0.6f), Offset(-0.03f * u, -0.085f * u), Offset(0.03f * u, -0.085f * u), strokeWidth = pen.lw * 1.2f)
        // Trailing leaves.
        for (k in 0 until 5) {
            val x = -0.035f + k * 0.0175f
            val len = 0.05f + 0.035f * ((k * 3) % 3)
            drawLine(Color(0xFF3BC46B), Offset(x * u, -0.04f * u), Offset((x + sin(pen.t + k) * 0.004f) * u, (-0.04f + len) * u), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
            drawCircle(Color(0xFF2E8B57), 0.011f * u, Offset((x + sin(pen.t + k) * 0.004f) * u, (-0.04f + len) * u))
        }
        drawCircle(Color(0xFF3BC46B), 0.035f * u, Offset(0f, -0.13f * u))
        drawCircle(Color(0xFFF08CB8), 0.012f * u, Offset(0.012f * u, -0.14f * u))
        drawCircle(Ink.line, 0.035f * u, Offset(0f, -0.13f * u), style = pen.thin)
    }
}

private fun DrawScope.miCoatRack(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.14f, 0.06f)
    val sway = if (f.on) sin(pen.t * 9f) * 6f * (if (f.anim > 0f) 1f else 0.6f) else 0f
    // The pole and its three feet.
    drawLine(Ink.line, Offset(0f, 0f), Offset(0f, -0.36f * u), strokeWidth = 0.014f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(FxC.walnut, Offset(0f, 0f), Offset(0f, -0.36f * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    for (s in listOf(-1f, 1f)) drawLine(Ink.line, Offset(0f, -0.01f * u), Offset(s * 0.06f * u, 0f), strokeWidth = 0.012f * u + pen.lw * 2f, cap = StrokeCap.Round)
    for (s in listOf(-1f, 1f)) drawLine(FxC.walnut, Offset(0f, -0.01f * u), Offset(s * 0.06f * u, 0f), strokeWidth = 0.012f * u, cap = StrokeCap.Round)
    // Hooks with a jacket, a scarf and a hat.
    for (s in listOf(-1f, 1f)) drawLine(FxC.brass, Offset(0f, -0.33f * u), Offset(s * 0.045f * u, -0.35f * u), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    rotate(sway, Offset(-0.045f * u, -0.35f * u)) {
        val jacket = Path().apply {
            moveTo(-0.075f * u, -0.345f * u); lineTo(-0.015f * u, -0.345f * u); lineTo(-0.012f * u, -0.22f * u); lineTo(-0.078f * u, -0.22f * u); close()
        }
        drawPath(jacket, FxC.dusty)
        drawPath(jacket, Ink.line, style = pen.stroke)
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(-0.045f * u, -0.345f * u), Offset(-0.045f * u, -0.22f * u), strokeWidth = pen.lw * 0.8f)
        drawCircle(FxC.yellow, 0.005f * u, Offset(-0.052f * u, -0.3f * u))
    }
    rotate(-sway, Offset(0.045f * u, -0.35f * u)) {
        val scarf = Path().apply { moveTo(0.03f * u, -0.35f * u); lineTo(0.06f * u, -0.35f * u); lineTo(0.065f * u, -0.26f * u); lineTo(0.045f * u, -0.26f * u); lineTo(0.04f * u, -0.3f * u); close() }
        drawPath(scarf, FxC.mustard)
        drawPath(scarf, Ink.line, style = pen.stroke)
        for (k in 0 until 3) drawLine(FxC.red, Offset(0.036f * u, (-0.33f + k * 0.022f) * u), Offset(0.06f * u, (-0.33f + k * 0.022f) * u), strokeWidth = pen.lw * 1.2f)
    }
    // A hat on top.
    inkedRound(Rect(-0.04f * u, -0.385f * u, 0.04f * u, -0.365f * u), 0.008f * u, FxC.fjord, pen)
    inkedRound(Rect(-0.025f * u, -0.41f * u, 0.025f * u, -0.38f * u), 0.012f * u, FxC.fjord, pen)
}

// ------------------------------------------------------------------------------------------------- yard

private fun DrawScope.miMailbox(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.1f, 0.06f)
    val col = listOf(FxC.fjord, FxC.red, FxC.mustard, FxC.green)[f.variant.mod(4)]
    fxBox(u, -0.008f, -0.15f, 0.008f, 0f, 0.016f, FxC.oakDark, pen)
    // The box on top, rounded.
    val body = Path().apply {
        moveTo(-0.045f * u, -0.15f * u); lineTo(-0.045f * u, -0.185f * u)
        quadraticTo(-0.045f * u, -0.215f * u, 0f, -0.215f * u); quadraticTo(0.045f * u, -0.215f * u, 0.045f * u, -0.185f * u); lineTo(0.045f * u, -0.15f * u); close()
    }
    drawPath(body, col.lighten(0.05f))
    drawPath(body, Ink.line, style = pen.stroke)
    drawRoundRect(Color(0xFF2B1F2E), Offset(-0.028f * u, -0.18f * u), Size(0.056f * u, 0.012f * u), androidx.compose.ui.geometry.CornerRadius(0.003f * u))
    // The flag: up when there is post.
    val fy = if (f.on) -0.225f else -0.175f
    drawLine(Ink.line, Offset(0.045f * u, -0.17f * u), Offset(0.045f * u, fy * u), strokeWidth = pen.lw * 1.4f)
    drawRect(FxC.yellow, Offset(0.045f * u, fy * u), Size(0.028f * u, 0.016f * u))
    drawRect(Ink.line, Offset(0.045f * u, fy * u), Size(0.028f * u, 0.016f * u), style = pen.thin)
    drawPath(fxHeart(0f, -0.2f * u, 0.008f * u), Color.White.copy(alpha = 0.9f))
}

private fun DrawScope.miFence(f: Fixture, u: Float, pen: Pen) {
    val col = listOf(Color(0xFFF7F3EC), FxC.oak, FxC.fjord.lighten(0.2f))[f.variant.mod(3)]
    miShadow(u, 0.3f, 0.04f)
    fxBox(u, -0.15f, -0.075f, 0.15f, -0.062f, 0.012f, col.darken(0.08f), pen, z = 0.004f)
    fxBox(u, -0.15f, -0.03f, 0.15f, -0.017f, 0.012f, col.darken(0.08f), pen, z = 0.004f)
    for (k in 0 until 5) {
        val x = -0.125f + k * 0.0625f
        val h = 0.12f + (if (k % 2 == 0) 0f else -0.006f)
        val tip = Path().apply {
            moveTo((x - 0.0165f) * u, 0f); lineTo((x - 0.0165f) * u, -(h - 0.012f) * u); lineTo(x * u, -h * u); lineTo((x + 0.0165f) * u, -(h - 0.012f) * u); lineTo((x + 0.0165f) * u, 0f); close()
        }
        drawPath(tip, col)
        drawPath(tip, Ink.line, style = pen.stroke)
        drawLine(Ink.line.copy(alpha = 0.18f), Offset((x + 0.008f) * u, -0.01f * u), Offset((x + 0.008f) * u, -(h - 0.02f) * u), strokeWidth = pen.lw)
    }
}

private fun DrawScope.miFlowerBed(f: Fixture, u: Float, pen: Pen) {
    val d = 0.07f
    miShadow(u, 0.3f, d)
    fxBox(u, -0.15f, -0.03f, 0.15f, 0f, d, FxC.stone, pen, rad = 0.004f)
    drawPath(fxFlat(u, -0.145f, 0.145f, -0.03f, 0.008f, d - 0.008f), FxC.soil)
    val v = f.variant.mod(4)
    val colors = when (v) {
        0 -> listOf(FxC.red, FxC.yellow)
        1 -> listOf(Color.White, FxC.yellow)
        2 -> listOf(FxC.lilac, Color(0xFF8B5CF6))
        else -> listOf(FxC.pink, FxC.yellow, FxC.sky)
    }
    for (k in 0 until 7) {
        val x = -0.125f + k * 0.0415f
        val z = d * (0.3f + 0.4f * ((k * 5) % 3) / 2f)
        val base = fxQ(u, x, -0.03f, z)
        val h = (0.05f + 0.04f * ((k * 7) % 3) / 2f) * u
        val sway = sin(pen.t * 1.5f + k * 1.3f) * 0.004f * u
        drawLine(Color(0xFF3BC46B), base, Offset(base.x + sway, base.y - h), strokeWidth = pen.lw * 1.5f, cap = StrokeCap.Round)
        val c = Offset(base.x + sway, base.y - h)
        val col = colors[k % colors.size]
        when (v) {
            0 -> {
                drawPath(Path().apply { moveTo(c.x - 0.011f * u, c.y); quadraticTo(c.x - 0.012f * u, c.y - 0.026f * u, c.x, c.y - 0.03f * u); quadraticTo(c.x + 0.012f * u, c.y - 0.026f * u, c.x + 0.011f * u, c.y); close() }, col)
                drawPath(Path().apply { moveTo(c.x - 0.011f * u, c.y); quadraticTo(c.x - 0.012f * u, c.y - 0.026f * u, c.x, c.y - 0.03f * u); quadraticTo(c.x + 0.012f * u, c.y - 0.026f * u, c.x + 0.011f * u, c.y); close() }, Ink.line, style = pen.thin)
            }
            else -> {
                for (a in 0 until 6) drawCircle(col, 0.0095f * u, Offset(c.x + cos(a * 1.047f) * 0.014f * u, c.y + sin(a * 1.047f) * 0.014f * u))
                drawCircle(if (v == 1) FxC.yellow else FxC.mustard, 0.009f * u, c)
                drawCircle(Ink.line, 0.022f * u, c, style = pen.thin)
            }
        }
    }
}

private fun DrawScope.miSwing(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.42f, 0.08f)
    val wood = FxC.oak
    // Two A-frames and the bar.
    for (s in listOf(-1f, 1f)) {
        val x = s * 0.17f
        drawLine(Ink.line, Offset((x - 0.03f) * u, 0f), Offset(x * u, -0.3f * u), strokeWidth = 0.014f * u + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset((x + 0.03f) * u, 0f), Offset(x * u, -0.3f * u), strokeWidth = 0.014f * u + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(wood, Offset((x - 0.03f) * u, 0f), Offset(x * u, -0.3f * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
        drawLine(wood, Offset((x + 0.03f) * u, 0f), Offset(x * u, -0.3f * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    }
    drawLine(Ink.line, Offset(-0.17f * u, -0.3f * u), Offset(0.17f * u, -0.3f * u), strokeWidth = 0.016f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(FxC.oakDark, Offset(-0.17f * u, -0.3f * u), Offset(0.17f * u, -0.3f * u), strokeWidth = 0.016f * u, cap = StrokeCap.Round)
    // The seat swings when it has been pushed.
    val a = if (f.on) sin(pen.t * 3.2f) * 12f else sin(pen.t * 1f) * 1.2f
    rotate(a, Offset(0f, -0.3f * u)) {
        drawLine(Ink.line, Offset(-0.035f * u, -0.3f * u), Offset(-0.035f * u, -0.1f * u), strokeWidth = pen.lw * 1.3f)
        drawLine(Ink.line, Offset(0.035f * u, -0.3f * u), Offset(0.035f * u, -0.1f * u), strokeWidth = pen.lw * 1.3f)
        inkedRound(Rect(-0.05f * u, -0.1f * u, 0.05f * u, -0.082f * u), 0.005f * u, FxC.red.lighten(0.05f), pen)
    }
}

private fun DrawScope.miBirdBath(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.13f, 0.08f)
    drawPath(fxDisc2(0f, -0.005f * u, 0.04f * u, 0.025f * u), FxC.stone.darken(0.1f))
    drawPath(fxDisc2(0f, -0.005f * u, 0.04f * u, 0.025f * u), Ink.line, style = pen.stroke)
    inkedRound(Rect(-0.014f * u, -0.1f * u, 0.014f * u, -0.01f * u), 0.006f * u, FxC.stone, pen)
    val c = Offset(0f, -0.105f * u)
    drawPath(fxDisc2(c.x, c.y, 0.06f * u, 0.032f * u), FxC.stone)
    drawPath(fxDisc2(c.x, c.y, 0.06f * u, 0.032f * u), Ink.line, style = pen.stroke)
    drawPath(fxDisc2(c.x, c.y, 0.047f * u, 0.024f * u), Color(0xFF8FD0F0))
    drawPath(fxDisc2(c.x, c.y, 0.047f * u, 0.024f * u), Ink.line.copy(alpha = 0.5f), style = pen.thin)
    drawLine(Color.White.copy(alpha = 0.8f), Offset(-0.02f * u, c.y), Offset(0.005f * u, c.y), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    if (f.on) {
        // A bird has a splashing bath.
        val bx = sin(pen.t * 9f) * 0.004f * u
        inkedCircle(Offset(bx, c.y - 0.026f * u), 0.022f * u, FxC.sky, pen)
        drawPath(Path().apply { moveTo(0.02f * u, c.y - 0.028f * u); lineTo(0.036f * u, c.y - 0.024f * u); lineTo(0.02f * u, c.y - 0.02f * u); close() }, FxC.mustard)
        drawCircle(Ink.line, 0.003f * u, Offset(0.01f * u + bx, c.y - 0.032f * u))
        for (k in 0 until 4) drawCircle(Color(0xFFBFE3FA), 0.005f * u, Offset((-0.04f + k * 0.027f) * u, c.y - (0.03f + 0.025f * abs(sin(pen.t * 8f + k))) * u))
    }
}

private fun DrawScope.miGnome(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.07f, 0.05f)
    val wink = f.count % 2 == 1
    // Boots, blue coat, white beard, a pointed red hat.
    inkedRound(Rect(-0.026f * u, -0.012f * u, 0.026f * u, 0f), 0.005f * u, FxC.walnut, pen)
    val coat = Path().apply { moveTo(-0.03f * u, -0.012f * u); lineTo(-0.024f * u, -0.06f * u); lineTo(0.024f * u, -0.06f * u); lineTo(0.03f * u, -0.012f * u); close() }
    drawPath(coat, FxC.fjord); drawPath(coat, Ink.line, style = pen.stroke)
    drawLine(FxC.brass, Offset(-0.028f * u, -0.03f * u), Offset(0.028f * u, -0.03f * u), strokeWidth = pen.lw * 1.4f)
    val beard = Path().apply { moveTo(-0.022f * u, -0.058f * u); lineTo(0.022f * u, -0.058f * u); lineTo(0f, -0.018f * u); close() }
    drawPath(beard, Color.White); drawPath(beard, Ink.line, style = pen.thin)
    inkedCircle(Offset(0f, -0.07f * u), 0.014f * u, Color(0xFFF2C7A0), pen)
    drawCircle(Ink.line, 0.0025f * u, Offset(-0.005f * u, -0.073f * u))
    if (wink) drawLine(Ink.line, Offset(0.002f * u, -0.073f * u), Offset(0.01f * u, -0.073f * u), strokeWidth = pen.lw) else drawCircle(Ink.line, 0.0025f * u, Offset(0.006f * u, -0.073f * u))
    drawCircle(FxC.blush, 0.004f * u, Offset(0f, -0.066f * u))
    val hat = Path().apply { moveTo(-0.022f * u, -0.078f * u); lineTo(0.022f * u, -0.078f * u); lineTo(0.004f * u, -0.125f * u); close() }
    drawPath(hat, FxC.red); drawPath(hat, Ink.line, style = pen.stroke)
}

private fun DrawScope.miSandbox(f: Fixture, u: Float, pen: Pen) {
    val d = 0.14f
    miShadow(u, 0.34f, d)
    fxBox(u, -0.16f, -0.07f, 0.16f, 0f, d, FxC.wood, pen, rad = 0.004f)
    drawPath(fxFlat(u, -0.15f, 0.15f, -0.07f, 0.01f, d - 0.01f), FxC.sand)
    // A bucket, a spade and a castle.
    val b = fxQ(u, -0.08f, -0.07f, 0.06f)
    val bucket = Path().apply { moveTo(b.x - 0.02f * u, b.y - 0.036f * u); lineTo(b.x + 0.02f * u, b.y - 0.036f * u); lineTo(b.x + 0.015f * u, b.y); lineTo(b.x - 0.015f * u, b.y); close() }
    drawPath(bucket, FxC.red); drawPath(bucket, Ink.line, style = pen.stroke)
    val c = fxQ(u, 0.06f, -0.07f, 0.06f)
    for (k in 0 until 3) drawRoundRect(FxC.sand.darken(0.08f), Offset(c.x - (0.03f - k * 0.008f) * u, c.y - (k + 1) * 0.016f * u), Size((0.06f - k * 0.016f) * u, 0.016f * u), androidx.compose.ui.geometry.CornerRadius(0.003f * u))
    drawRoundRect(Ink.line, Offset(c.x - 0.03f * u, c.y - 0.048f * u), Size(0.06f * u, 0.048f * u), androidx.compose.ui.geometry.CornerRadius(0.003f * u), style = pen.thin)
    drawLine(Ink.line, Offset(c.x, c.y - 0.048f * u), Offset(c.x, c.y - 0.07f * u), strokeWidth = pen.lw)
    drawRect(FxC.yellow, Offset(c.x, c.y - 0.07f * u), Size(0.016f * u, 0.01f * u))
}

private fun DrawScope.miAppleTree(f: Fixture, u: Float, pen: Pen) {
    miShadow(u, 0.3f, 0.12f)
    val season = pen.season
    val shake = if (f.anim > 0.01f) sin(pen.t * 30f) * 0.01f * u * f.anim else 0f
    // The trunk, with a fork.
    val trunk = Path().apply {
        moveTo(-0.025f * u, 0f); lineTo(-0.018f * u, -0.22f * u); lineTo(0.018f * u, -0.22f * u); lineTo(0.026f * u, 0f); close()
    }
    drawPath(trunk, FxC.wood.darken(0.1f)); drawPath(trunk, Ink.line, style = pen.stroke)
    drawLine(FxC.walnut, Offset(0f, -0.1f * u), Offset(-0.05f * u, -0.2f * u), strokeWidth = 0.016f * u, cap = StrokeCap.Round)
    drawLine(FxC.walnut, Offset(0f, -0.14f * u), Offset(0.06f * u, -0.24f * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    val crown = when (season) {
        Season.SPRING -> Color(0xFFF4B6C8)
        Season.AUTUMN -> Color(0xFFE8A33D)
        Season.WINTER -> Color(0xFFDDE8F4)
        else -> Color(0xFF6FAE5A)
    }
    val cx = shake
    val blob = fxBlob(u, -0.2f + cx / u, -0.3f, -0.12f + cx / u, -0.5f, 0.0f + cx / u, -0.58f, 0.14f + cx / u, -0.52f, 0.21f + cx / u, -0.32f, 0.1f + cx / u, -0.22f, -0.1f + cx / u, -0.22f)
    drawPath(blob, crown)
    inked(blob, crown, pen)
    drawCircle(crown.lighten(0.2f), 0.03f * u, Offset(-0.07f * u + shake, -0.48f * u))
    // Apples (blossom in spring, none in winter).
    if (season == Season.SUMMER || season == Season.AUTUMN) {
        for ((k, a) in listOf(-0.12f to -0.34f, -0.02f to -0.28f, 0.1f to -0.4f, 0.14f to -0.3f, -0.08f to -0.47f, 0.03f to -0.5f).withIndex()) {
            val c = Offset(a.first * u + shake, a.second * u)
            drawCircle(if (k % 3 == 2) FxC.yellow else FxC.red, 0.017f * u, c)
            drawCircle(Ink.line, 0.017f * u, c, style = pen.thin)
            drawLine(Color(0xFF3BC46B), Offset(c.x, c.y - 0.015f * u), Offset(c.x + 0.006f * u, c.y - 0.022f * u), strokeWidth = pen.lw)
        }
    } else if (season == Season.SPRING) {
        for (k in 0 until 8) drawCircle(Color.White, 0.008f * u, Offset((-0.15f + k * 0.04f) * u + shake, (-0.32f - 0.08f * sin(k * 2.1f) - 0.08f) * u))
    }
}

// ------------------------------------------------------------------------------------- doors and stairs

/** The front door of the hall seen from inside: the style the child chose, in a white frame. */
private fun DrawScope.miDoor(f: Fixture, u: Float, pen: Pen) {
    // The yard's door is drawn by the house itself.
    if (f.place == PlaceId.MINE_YARD) return
    val style = (f.mode - 1).coerceAtLeast(0)
    val w = 0.2f * u
    val h = 0.38f * u
    // A step and a mat in front, a coat-hook board beside.
    drawOval(Ink.shadow, Offset(-w * 0.7f, -0.012f * u), Size(w * 1.4f, 0.03f * u))
    drawMineDoor(style, -w / 2f, -h, w / 2f, 0f, pen, open = if (f.anim > 0.01f) f.anim * 0.8f else 0f, lit = 0f)
    // A lamp over the door.
    drawCircle(MineC.lit.copy(alpha = 0.5f), 0.012f * u, Offset(0f, -h - 0.04f * u))
    drawCircle(Ink.line, 0.012f * u, Offset(0f, -h - 0.04f * u), style = pen.thin)
}

/** The stairs of the hall: up to the second floor (boarded off with tape until it is built), or down to the ground floor. */
private fun DrawScope.miStairs(f: Fixture, u: Float, pen: Pen) {
    val built = f.mode == 1
    val upstairs = f.place == PlaceId.MINE_UPPER
    val steps = 8
    val w = 0.84f
    val h = 0.5f
    val d = 0.2f
    scale(if (upstairs) -1f else 1f, 1f, Offset(0f, 0f)) {
        miShadow(u, w, d)
        // The stringer: a side board under the steps.
        val sw = w / steps
        for (k in 0 until steps) {
            val x0 = -w / 2f + k * sw
            val top = -(k + 1) * (h / steps)
            fxBox(u, x0, top, x0 + sw, 0f, d, FxC.oak.lighten(0.04f), pen, rad = 0.002f, top = FxC.oak.lighten(0.2f), side = FxC.oakDark)
        }
        // A banister along the front.
        val rail = Path().apply { moveTo(-w / 2f * u, -0.06f * u); lineTo(w / 2f * u, -(h + 0.06f) * u) }
        for (k in 0..steps step 2) {
            val x = -w / 2f + k * sw
            val y = -(k + 0.0f) * (h / steps)
            drawLine(Ink.line, Offset(x * u, (y + 0.0f) * u), Offset(x * u, (y - 0.09f) * u), strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
            drawLine(FxC.paint, Offset(x * u, (y + 0.0f) * u), Offset(x * u, (y - 0.09f) * u), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
        }
        drawPath(rail, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(rail, FxC.walnut, style = Stroke(0.012f * u, cap = StrokeCap.Round))
        if (!built && !upstairs) {
            // Not built yet: a barrier of striped tape across the top of the stairs, and a little lamp.
            val y = -(h + 0.03f)
            for (k in 0 until 6) {
                val x = -0.12f + k * 0.07f
                drawLine(Ink.line, Offset((x + 0.2f) * u, y * u), Offset((x + 0.26f) * u, (y + 0.035f) * u), strokeWidth = 0.028f * u + pen.lw * 2f)
            }
            drawLine(FxC.yellow, Offset(0.08f * u, y * u + 0.0f), Offset(0.4f * u, (y + 0.018f) * u), strokeWidth = 0.028f * u)
            for (k in 0 until 5) {
                val x = 0.1f + k * 0.065f
                drawLine(Ink.line, Offset(x * u, (y - 0.012f) * u), Offset((x + 0.025f) * u, (y + 0.04f) * u), strokeWidth = 0.012f * u)
            }
            drawCircle(FxC.yellow, 0.014f * u, Offset(0.08f * u, (y - 0.035f) * u))
            drawCircle(Ink.line, 0.014f * u, Offset(0.08f * u, (y - 0.035f) * u), style = pen.thin)
        }
    }
}
