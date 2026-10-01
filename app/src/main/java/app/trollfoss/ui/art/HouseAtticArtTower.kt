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
import kotlin.math.sin

/*
 * Storhuset, attic: the tower. The star map with five constellations that light up one by one, a brass weather
 * vane model with a rooster, an armillary sphere with a star inside, a cluster of weather instruments, and the
 * owl hole where a friendly owl lives. (The telescope and the stool are the lab's, and look fine in brass.)
 */

// ------------------------------------------------------------------------------------------ star map

private val CONSTELLATIONS = listOf(
    // the Plough
    listOf(-0.135f to -0.205f, -0.1f to -0.225f, -0.065f to -0.215f, -0.035f to -0.19f, -0.03f to -0.15f, -0.06f to -0.135f, -0.095f to -0.155f),
    // the W of Cassiopeia
    listOf(0.02f to -0.25f, 0.045f to -0.2f, 0.075f to -0.25f, 0.1f to -0.2f, 0.13f to -0.25f),
    // the little kite
    listOf(0.05f to -0.12f, 0.1f to -0.15f, 0.125f to -0.1f, 0.08f to -0.06f),
    // the crown
    listOf(-0.13f to -0.085f, -0.1f to -0.045f, -0.07f to -0.075f, -0.04f to -0.045f, -0.015f to -0.09f),
    // the arrow
    listOf(0.0f to -0.17f, 0.04f to -0.11f, 0.02f to -0.07f, 0.065f to -0.05f),
)

internal fun DrawScope.atStarMap(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = 0f
    val lit = f.mode.coerceIn(0, 5)
    // The chart hangs on two brass rods from a cord.
    val chart = Rect(p(-0.165f, -0.295f).x, p(0f, -0.295f).y, p(0.165f, -0.025f).x, p(0f, -0.025f).y)
    drawLine(Ink.line, p(-0.12f, -0.32f), p(0f, -0.345f), pen.lw)
    drawLine(Ink.line, p(0.12f, -0.32f), p(0f, -0.345f), pen.lw)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF14224A), Color(0xFF23367A)), startY = chart.top, endY = chart.bottom), chart.topLeft, chart.size)
    clipPath(rectPath(chart)) {
        // The celestial circles in gold, and faint little stars.
        drawCircle(AtC.gold, 0.14f * u, p(0f, -0.16f), alpha = 0.3f, style = Stroke(pen.lw * 0.7f))
        drawCircle(AtC.gold, 0.09f * u, p(0f, -0.16f), alpha = 0.3f, style = Stroke(pen.lw * 0.6f))
        drawLine(AtC.gold, p(-0.17f, -0.16f), p(0.17f, -0.16f), pen.lw * 0.5f, alpha = 0.25f)
        for (i in 0 until 14) twinkle(p(-0.15f + hash01(i, 21) * 0.3f, -0.29f + hash01(i, 22) * 0.26f), 0.006f * u * (0.5f + 0.5f * atWave(t, 1.3f, i.toFloat())), Color(0xFFCFE2FF), 0.5f)
        // The five constellations: dim until lit, then bright gold with a twinkle.
        for ((i, c) in CONSTELLATIONS.withIndex()) {
            val on = i < lit
            val lineCol = if (on) AtC.goldLight else Color(0xFF8FA7DC)
            for (k in 0 until c.size - 1) drawLine(lineCol, p(c[k].first, c[k].second), p(c[k + 1].first, c[k + 1].second), pen.lw * (if (on) 1.1f else 0.6f), StrokeCap.Round, alpha = if (on) 0.95f else 0.4f)
            for ((k, s) in c.withIndex()) {
                val sc = p(s.first, s.second)
                if (on) {
                    fxGlow(sc, 0.03f * u, AtC.goldLight, 0.6f)
                    twinkle(sc, 0.014f * u * (0.7f + 0.3f * sin(t * 3f + k + i)), Color.White, 1f)
                } else {
                    drawCircle(Color(0xFFCFE2FF), 0.0032f * u, sc, alpha = 0.7f)
                }
            }
        }
        // A golden crescent moon in the corner.
        drawCircle(AtC.goldLight, 0.022f * u, p(0.12f, -0.265f))
        drawCircle(Color(0xFF14224A), 0.02f * u, p(0.13f, -0.272f))
    }
    drawRect(Ink.line, chart.topLeft, chart.size, style = pen.stroke)
    // The rods, with a ball on each end.
    for (y in listOf(-0.3f, -0.02f)) {
        atBrass(p(-0.175f, y), p(0.175f, y), 0.012f * u, pen)
        for (x in listOf(-0.18f, 0.18f)) atDot(p(x, y), 0.009f * u, AtC.brassLight, pen)
    }
}

// ------------------------------------------------------------------------------------------ weather vane

internal fun DrawScope.atWeatherVane(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val a = f.angle
    // A little round brass foot, a tall pole and a fixed cross of compass arms with a ball at each end.
    drawPath(fxDisc2(0f, -0.006f * u, 0.05f * u, 0.032f * u), AtC.brassDark)
    drawPath(fxDisc2(0f, -0.012f * u, 0.045f * u, 0.028f * u), AtC.brass)
    drawPath(fxDisc2(0f, -0.012f * u, 0.045f * u, 0.028f * u), Ink.line, style = pen.stroke)
    capsule(p(0f, -0.014f), p(0f, -0.24f), 0.009f * u, AtC.brass, pen)
    val e = q(0.075f, -0.14f, 0f)
    val w = q(-0.075f, -0.14f, 0f)
    val n = q(0f, -0.14f, 0.07f)
    val s = q(0f, -0.14f, -0.07f)
    for ((from, to) in listOf(w to e, s to n)) {
        drawLine(Ink.line, from, to, 0.007f * u + pen.lw * 2f, StrokeCap.Round)
        drawLine(AtC.brass, from, to, 0.007f * u, StrokeCap.Round)
    }
    for (end in listOf(e, w, n, s)) atDot(end, 0.01f * u, AtC.brass, pen)
    atDot(p(0f, -0.14f), 0.011f * u, AtC.brassDark, pen)
    // The rooster turns on top of the pole: flattened as it turns edge-on, flipped when it comes round.
    val c = cos(a)
    val flip = if (kotlin.math.abs(c) < 0.12f) 0.12f else c
    scale(flip, 1f, p(0f, -0.25f)) {
        val body = Path().apply {
            moveTo(p(-0.05f, -0.255f).x, p(-0.05f, -0.255f).y)
            // the tail feathers sweep up and back
            cubicTo(p(-0.1f, -0.3f).x, p(-0.1f, -0.3f).y, p(-0.1f, -0.33f).x, p(-0.1f, -0.33f).y, p(-0.075f, -0.345f).x, p(-0.075f, -0.345f).y)
            cubicTo(p(-0.06f, -0.31f).x, p(-0.06f, -0.31f).y, p(-0.045f, -0.3f).x, p(-0.045f, -0.3f).y, p(-0.035f, -0.3f).x, p(-0.035f, -0.3f).y)
            // back, neck, head with the comb
            cubicTo(p(-0.015f, -0.31f).x, p(-0.015f, -0.31f).y, p(0.02f, -0.31f).x, p(0.02f, -0.31f).y, p(0.035f, -0.33f).x, p(0.035f, -0.33f).y)
            cubicTo(p(0.04f, -0.345f).x, p(0.04f, -0.345f).y, p(0.065f, -0.345f).x, p(0.065f, -0.345f).y, p(0.065f, -0.325f).x, p(0.065f, -0.325f).y)
            lineTo(p(0.09f, -0.318f).x, p(0.09f, -0.318f).y)
            lineTo(p(0.065f, -0.31f).x, p(0.065f, -0.31f).y)
            cubicTo(p(0.06f, -0.29f).x, p(0.06f, -0.29f).y, p(0.07f, -0.265f).x, p(0.07f, -0.265f).y, p(0.045f, -0.245f).x, p(0.045f, -0.245f).y)
            quadraticTo(p(0f, -0.235f).x, p(0f, -0.235f).y, p(-0.05f, -0.255f).x, p(-0.05f, -0.255f).y)
            close()
        }
        inked(body, AtC.brass, pen)
        // comb, wattle, eye and a wing line
        atDot(p(0.045f, -0.347f), 0.007f * u, AtC.brassDark, pen)
        atDot(p(0.056f, -0.35f), 0.006f * u, AtC.brassDark, pen)
        drawCircle(Ink.line, 0.0045f * u, p(0.063f, -0.327f))
        drawCircle(AtC.brassLight, 0.0015f * u, p(0.0645f, -0.329f))
        atFold(p(-0.03f, -0.28f), p(0f, -0.265f), p(0.03f, -0.28f), AtC.brassDark, pen.lw * 1.1f)
        atFold(p(-0.04f, -0.265f), p(-0.01f, -0.255f), p(0.025f, -0.265f), AtC.brassDark, pen.lw)
    }
    atDot(p(0f, -0.245f), 0.007f * u, AtC.brassDark, pen)
    // a sparkle of gilt
    twinkle(p(0.04f, -0.33f), 0.011f * u, Color.White, 0.9f)
}

// ------------------------------------------------------------------------------------------ armillary

internal fun DrawScope.atArmillary(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = f.mode * 0.5236f
    val cx = 0f
    val cy = -0.2f
    val r = 0.088f
    atShadow(u, 0.2f, 0.1f)
    if (f.on) fxGlow(p(cx, cy), 0.25f * u, Color(0xFFFFE18A), 0.55f + 0.1f * sin(t * 2f))
    // A tripod of brass legs.
    capsule(p(-0.002f, -0.105f), p(-0.075f, -0.004f), 0.01f * u, AtC.brassDark, pen)
    capsule(p(0.002f, -0.105f), p(0.075f, -0.004f), 0.01f * u, AtC.brassDark, pen)
    capsule(p(0f, -0.105f), p(0.005f, -0.006f), 0.011f * u, AtC.brass, pen)
    drawOval(AtC.brassDark, Offset(-0.09f * u, -0.01f * u), Size(0.18f * u, 0.014f * u), alpha = 0.0f)
    // The sphere of rings: an equator, a tilted ring, and two meridians that turn slowly.
    val spin = f.mode * 0.2618f
    fun ring(rx: Float, ry: Float, tilt: Float, w: Float) {
        val oval = Path().apply { addOval(Rect(p(cx, cy).x - rx * u, p(cx, cy).y - ry * u, p(cx, cy).x + rx * u, p(cx, cy).y + ry * u)) }
        rotate(tilt, p(cx, cy)) {
            drawPath(oval, Ink.line, style = Stroke(w * u + pen.lw * 2f))
            drawPath(oval, AtC.brass, style = Stroke(w * u))
            drawPath(oval, AtC.brassLight, style = Stroke(w * u * 0.28f), alpha = 0.7f)
        }
    }
    ring(r, r * 0.34f, 0f, 0.011f)
    ring(r * abs(cos(spin)).coerceAtLeast(0.12f), r, 23f, 0.011f)
    ring(r * abs(sin(spin * 0.8f + 1f)).coerceAtLeast(0.12f), r, -23f, 0.01f)
    ring(r * 0.96f, r * 0.96f, 0f, 0.008f)
    // The star in the middle, and a small planet going round it.
    val pulse = 1f + 0.12f * sin(t * 3f)
    drawPath(starPath(p(cx, cy), 0.026f * u * pulse, 0.012f * u * pulse, t * 15f), AtC.goldLight)
    drawPath(starPath(p(cx, cy), 0.026f * u * pulse, 0.012f * u * pulse, t * 15f), Ink.line, style = pen.thin)
    val oa = f.mode * 0.5236f
    val planet = Offset(p(cx, cy).x + cos(oa) * r * 0.96f * u / r * r, p(cx, cy).y + sin(oa) * r * 0.34f * u)
    atDot(planet, 0.011f * u, Color(0xFF6AA7E8), pen)
    drawCircle(Color(0xFF3BC46B), 0.005f * u, Offset(planet.x - 0.003f * u, planet.y - 0.002f * u))
    // A little brass bracket under the sphere.
    atDot(p(0f, -0.108f), 0.014f * u, AtC.brass, pen)
}

// ------------------------------------------------------------------------------------------ instruments

internal fun DrawScope.atBarometer(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = 0f
    val mode = f.mode.mod(3)
    val wood = Color(0xFF6E4630)
    inkedRound(Rect(-0.108f * u, -0.215f * u, 0.108f * u, -0.005f * u), 0.02f * u, wood, pen)
    // The barometer: brass rim, cream face with three pictures, a needle that points at today's weather.
    val c = p(-0.025f, -0.11f)
    val r = 0.072f * u
    drawCircle(AtC.brassDark, r + 0.008f * u, c)
    drawCircle(AtC.brass, r + 0.005f * u, c)
    drawCircle(Ink.line, r + 0.008f * u, c, style = pen.stroke)
    drawCircle(AtC.cream, r, c)
    drawCircle(Ink.line, r, c, style = pen.thin)
    // rain at the left, sun at the top, a rainbow at the right
    val cloud = Offset(c.x - r * 0.52f, c.y - r * 0.12f)
    drawCircle(Color(0xFF8E9BB4), r * 0.15f, cloud)
    drawCircle(Color(0xFF8E9BB4), r * 0.11f, Offset(cloud.x + r * 0.16f, cloud.y + r * 0.04f))
    for (k in 0..2) drawLine(Color(0xFF4AB3FF), Offset(cloud.x - r * 0.1f + k * r * 0.12f, cloud.y + r * 0.2f), Offset(cloud.x - r * 0.14f + k * r * 0.12f, cloud.y + r * 0.32f), pen.lw * 0.9f, StrokeCap.Round)
    drawCircle(Color(0xFFFFC83D), r * 0.14f, Offset(c.x, c.y - r * 0.55f))
    for (k in 0 until 8) {
        val a = k * 0.7854f
        drawLine(Color(0xFFFFC83D), Offset(c.x + cos(a) * r * 0.2f, c.y - r * 0.55f + sin(a) * r * 0.2f), Offset(c.x + cos(a) * r * 0.27f, c.y - r * 0.55f + sin(a) * r * 0.27f), pen.lw * 0.9f, StrokeCap.Round)
    }
    for ((i, col) in listOf(Color(0xFFFF9EC7), Color(0xFFFFE18A), Color(0xFF8FD9C0), Color(0xFF8FC8FF)).withIndex()) {
        drawArc(col, 180f, 180f, false, Offset(c.x + r * (0.24f - 0.035f * i), c.y - r * (0.04f + 0.02f * i) - r * 0.18f), Size(r * (0.5f + 0.07f * i), r * (0.36f + 0.07f * i)), style = Stroke(r * 0.045f))
    }
    val ang = (mode - 1) * 55f + sin(t * 2f) * 1.5f
    rotate(ang, c) {
        drawLine(Ink.line, c, Offset(c.x, c.y - r * 0.82f), pen.lw * 1.4f, StrokeCap.Round)
        drawPath(Path().apply { moveTo(c.x - r * 0.05f, c.y - r * 0.7f); lineTo(c.x, c.y - r * 0.9f); lineTo(c.x + r * 0.05f, c.y - r * 0.7f); close() }, AtC.gold)
    }
    atDot(c, 0.007f * u, AtC.brass, pen)
    shine(Offset(c.x - r * 0.45f, c.y - r * 0.5f), r * 0.2f, r * 0.12f, 0.5f)
    // A thermometer: a glass tube and bulb, the liquid rising with the sunshine.
    val tube = Rect(p(0.07f, -0.19f).x, p(0f, -0.19f).y, p(0.088f, -0.04f).x, p(0f, -0.04f).y)
    inkedRound(tube, 0.008f * u, Color(0xFFF7F3EC), pen, shade = false)
    val level = 0.35f + 0.28f * mode
    drawRect(Color(0xFFFF8A3D), Offset(tube.left + 0.004f * u, tube.bottom - tube.height * level), Size(tube.width - 0.008f * u, tube.height * level - 0.004f * u))
    atDot(Offset(tube.center.x, tube.bottom + 0.006f * u), 0.012f * u, Color(0xFFFF8A3D), pen)
    for (k in 0 until 5) drawLine(Ink.line, Offset(tube.right, tube.top + 0.012f * u + k * (tube.height - 0.03f * u) / 4f), Offset(tube.right + 0.006f * u, tube.top + 0.012f * u + k * (tube.height - 0.03f * u) / 4f), pen.lw * 0.6f)
    // A small compass in the lower corner whose needle swings.
    val cc = p(0.06f, -0.025f)
    drawCircle(AtC.brass, 0.012f * u, cc)
    drawCircle(Ink.line, 0.012f * u, cc, style = pen.thin)
    rotate(sin(t * 0.8f) * 30f, cc) { drawLine(Color(0xFF2F6FB8), Offset(cc.x, cc.y - 0.009f * u), Offset(cc.x, cc.y + 0.009f * u), pen.lw * 1.1f, StrokeCap.Round) }
}

// ------------------------------------------------------------------------------------------ owl hole

internal fun DrawScope.atOwlHole(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = if (f.mode == 1) pen.t else 0f
    val c = p(0f, -0.11f)
    val r = 0.085f * u
    val out = f.mode == 1
    // Broken boards round a hole in the wall.
    val jag = Path()
    for (k in 0 until 14) {
        val a = k * 0.4488f
        val rr = r * (1.28f + 0.16f * hash01(k, 5) * (if (k % 2 == 0) 1f else -0.2f))
        val pt = Offset(c.x + cos(a) * rr, c.y + sin(a) * rr * 1.05f)
        if (k == 0) jag.moveTo(pt.x, pt.y) else jag.lineTo(pt.x, pt.y)
    }
    jag.close()
    inked(jag, Color(0xFFB8824D), pen)
    for (k in 0 until 7) {
        val a = k * 0.8976f + 0.2f
        drawLine(Color(0xFF6E4630), Offset(c.x + cos(a) * r * 1.05f, c.y + sin(a) * r * 1.05f), Offset(c.x + cos(a) * r * 1.3f, c.y + sin(a) * r * 1.3f), pen.lw * 0.8f)
    }
    drawCircle(Color(0xFF1C1430), r, c)
    clipPath(Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }) {
        drawCircle(safeRadialGradient(listOf(Color(0xFF3A2A5C), Color(0xFF14102A)), c, r), r, c)
        // fireflies in the dark
        for (k in 0 until 2) {
            val a = t * 0.7f + k * 3f
            twinkle(Offset(c.x + cos(a) * r * 0.55f, c.y - r * 0.25f + sin(a * 1.3f) * r * 0.35f), r * 0.14f, Color(0xFFFFF3A0), 0.6f)
        }
        val look = sin(t * 0.5f) * 0.015f * u
        if (out) {
            // The owl is out: a round brown body with a cream chest, tufts, wings and a hooting beak.
            val bob = sin(t * 7f) * 0.003f * u
            val body = Rect(c.x - 0.06f * u, c.y - 0.075f * u + bob, c.x + 0.06f * u, c.y + 0.09f * u)
            drawOval(Color(0xFF8A6A48), body.topLeft, body.size)
            drawOval(Color(0xFFF1E0BC), Offset(body.left + 0.02f * u, body.top + 0.06f * u), Size(0.08f * u, 0.1f * u))
            for (k in 0 until 3) for (j in 0 until 2) drawArc(Color(0xFFB89868), 0f, 180f, false, Offset(c.x - 0.035f * u + j * 0.03f * u, body.top + 0.09f * u + k * 0.022f * u), Size(0.03f * u, 0.02f * u), style = Stroke(pen.lw * 0.8f))
            for (s in listOf(-1f, 1f)) {
                val tuft = Path().apply { moveTo(c.x + s * 0.02f * u, body.top + 0.012f * u); lineTo(c.x + s * 0.052f * u, body.top - 0.03f * u); lineTo(c.x + s * 0.058f * u, body.top + 0.025f * u); close() }
                drawPath(tuft, Color(0xFF6E5236))
                drawPath(tuft, Ink.line, style = pen.thin)
            }
            val flap = sin(t * 9f) * 6f
            for (s in listOf(-1f, 1f)) rotate(s * (12f + flap), Offset(c.x + s * 0.055f * u, c.y)) {
                drawOval(Color(0xFF6E5236), Offset(c.x + (if (s < 0f) -0.085f else 0.045f) * u, c.y - 0.01f * u), Size(0.04f * u, 0.09f * u))
            }
        }
        // The eyes: two big yellow discs with dark pupils, blinking now and then.
        val blink = if (out) atEvery(t, 4.3f, 0.14f, f.id.toFloat()) else f.count == 1
        for (s in listOf(-1f, 1f)) {
            val ec = Offset(c.x + s * 0.03f * u, c.y - (if (out) 0.03f else 0.005f) * u)
            drawCircle(Color(0xFFFFD447), 0.026f * u, ec)
            drawCircle(Ink.line, 0.026f * u, ec, style = pen.thin)
            if (blink) {
                drawLine(Ink.line, Offset(ec.x - 0.024f * u, ec.y), Offset(ec.x + 0.024f * u, ec.y), pen.lw * 1.4f, StrokeCap.Round)
            } else {
                drawCircle(Ink.line, 0.013f * u, Offset(ec.x + look, ec.y + 0.002f * u))
                drawCircle(Color.White, 0.004f * u, Offset(ec.x + look - 0.004f * u, ec.y - 0.005f * u))
            }
        }
        // brows / ear tufts when peeking from the dark
        if (!out) for (s in listOf(-1f, 1f)) drawLine(Color(0xFF8A6A48), Offset(c.x + s * 0.052f * u, c.y - 0.03f * u), Offset(c.x + s * 0.062f * u, c.y - 0.062f * u), 0.012f * u, StrokeCap.Round)
        // the beak
        val beakY = c.y + (if (out) 0.015f else 0.03f) * u
        val open = if (out) (sin(t * 6f) + 1f) / 2f * 0.012f * u else 0f
        drawPath(Path().apply { moveTo(c.x - 0.009f * u, beakY); lineTo(c.x + 0.009f * u, beakY); lineTo(c.x, beakY + 0.018f * u + open); close() }, Color(0xFFFF9F43))
        if (out && open > 0.004f * u) drawLine(Ink.line, Offset(c.x - 0.008f * u, beakY + 0.008f * u), Offset(c.x + 0.008f * u, beakY + 0.008f * u), pen.lw * 0.7f)
    }
    drawCircle(Ink.line, r, c, style = pen.stroke)
    // «Hoo»: little rings of sound while it hoots.
    if (out) {
        for (k in 0 until 2) {
            val ph = fxFrac(t * 0.9f + k * 0.5f)
            drawCircle(Color.White, r * (1.1f + ph * 0.5f), c, alpha = 0.35f * (1f - ph), style = Stroke(pen.lw))
        }
    }
}
