package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
 * Storhuset, attic: the ghost's nook. Sture's wing chair, a gramophone with silly records, a tower of books, a
 * candelabrum, paper-ghost fairy lights, a blanket fort, the shadow theatre and the grandfather clock.
 */

// ------------------------------------------------------------------------------------------ wing chair

internal fun DrawScope.atWingChair(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val rose = Color(0xFFC77A8A)
    val wood = Color(0xFF6E4630)
    atShadow(u, 0.28f, 0.14f)
    // Stubby legs, back and wings first, then the seat and the rolled arms in front.
    for (x in listOf(-0.115f, 0.115f)) fxBox(u, x - 0.012f, -0.026f, x + 0.012f, 0f, 0.025f, wood, pen, rad = 0.004f)
    fxBox(u, -0.105f, -0.3f, 0.105f, -0.09f, 0.04f, rose, pen, rad = 0.038f, z = 0.07f)
    for (s in listOf(-1f, 1f)) fxBox(u, if (s < 0f) -0.138f else 0.098f, -0.27f, if (s < 0f) -0.098f else 0.138f, -0.1f, 0.09f, rose.lighten(0.06f), pen, rad = 0.015f, z = 0.05f)
    // Tufted back: buttons with creases between them.
    val btn = rose.darken(0.3f)
    val pts = listOf(-0.05f to -0.25f, 0f to -0.25f, 0.05f to -0.25f, -0.025f to -0.2f, 0.025f to -0.2f, 0f to -0.15f)
    for (i in 0 until pts.size - 1) {
        val a = q(pts[i].first, pts[i].second, 0.07f)
        for (j in i + 1 until pts.size) {
            val b = q(pts[j].first, pts[j].second, 0.07f)
            if (abs(pts[i].first - pts[j].first) < 0.06f && abs(pts[i].second - pts[j].second) < 0.06f) drawLine(btn, a, b, pen.lw * 0.6f, alpha = 0.5f)
        }
    }
    for ((x, y) in pts) atDot(q(x, y, 0.07f), 0.0045f * u, btn, pen)
    fxBox(u, -0.11f, -0.095f, 0.11f, -0.02f, 0.12f, rose.darken(0.1f), pen, rad = 0.01f)
    fxBox(u, -0.1f, -0.128f, 0.1f, -0.088f, 0.1f, rose.lighten(0.1f), pen, rad = 0.016f, z = 0.012f)
    for (s in listOf(-1f, 1f)) fxBox(u, if (s < 0f) -0.145f else 0.103f, -0.17f, if (s < 0f) -0.103f else 0.145f, -0.04f, 0.12f, rose, pen, rad = 0.02f)
    // A knitted blanket over the left arm, and a pair of fluffy slippers.
    val blanket = Rect(p(-0.152f, -0.172f).x, p(0f, -0.172f).y, p(-0.1f, 0f).x, p(0f, -0.07f).y)
    val cols = listOf(AtC.blueLight, Color(0xFFE8B04A), AtC.blueDark)
    for (k in 0 until 6) drawRect(cols[k % 3], Offset(blanket.left, blanket.top + k * blanket.height / 6f), Size(blanket.width, blanket.height / 6f + 0.5f))
    fxKnit(blanket, Ink.line.copy(alpha = 0.3f), 3, 8, pen.lw * 0.45f)
    drawRect(Ink.line, blanket.topLeft, blanket.size, style = pen.thin)
    for (k in 0 until 4) drawLine(cols[1], Offset(blanket.left + (k + 0.5f) * blanket.width / 4f, blanket.bottom), Offset(blanket.left + (k + 0.5f) * blanket.width / 4f, blanket.bottom + 0.01f * u), pen.lw, StrokeCap.Round)
    for ((i, x) in listOf(-0.1f, -0.055f).withIndex()) {
        val c = p(x, 0.008f)
        drawOval(Color(0xFFF6EEDC), Offset(c.x - 0.022f * u, c.y - 0.01f * u), Size(0.044f * u, 0.022f * u))
        drawOval(Ink.line, Offset(c.x - 0.022f * u, c.y - 0.01f * u), Size(0.044f * u, 0.022f * u), style = pen.thin)
        atDot(Offset(c.x + (if (i == 0) 0.012f else -0.012f) * u, c.y - 0.012f * u), 0.0075f * u, AtC.blue, pen)
    }
}

// ------------------------------------------------------------------------------------------ gramophone

private val RECORD_COLORS = listOf(Color(0xFFFF8A3D), Color(0xFFFFC83D), Color(0xFF3BC46B), Color(0xFF8B5CF6), Color(0xFF2F9BFF), Color(0xFFFF6FA8))

internal fun DrawScope.atGramophone(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = if (f.on) pen.t else 0f
    val wood = Color(0xFF7A4A2E)
    val playing = f.on
    atShadow(u, 0.22f, 0.12f)
    // The cabinet with a speaker cloth front and a brass knob.
    fxBox(u, -0.09f, -0.13f, 0.09f, 0f, 0.1f, wood, pen, rad = 0.006f)
    val door = Rect(p(-0.07f, -0.115f).x, p(0f, -0.115f).y, p(0.07f, 0f).x, p(0f, -0.02f).y)
    drawRect(Color(0xFFE3C98F), door.topLeft, door.size)
    for (k in 0 until 5) for (j in 0 until 3) drawCircle(Color(0xFFB98A4A), 0.0035f * u, Offset(door.left + (k + 0.5f) * door.width / 5f, door.top + (j + 0.5f) * door.height / 3f))
    drawRect(Ink.line, door.topLeft, door.size, style = pen.thin)
    atDot(p(0.05f, -0.07f), 0.006f * u, AtC.brass, pen)
    for (x in listOf(-0.075f, 0.075f)) fxBox(u, x - 0.01f, -0.012f, x + 0.01f, 0f, 0.02f, wood.darken(0.3f), pen)
    // The turntable on top: a green felt mat, the record and the tonearm.
    val top = -0.13f
    drawPath(fxDisc2(0f, top * u, 0.075f * u, 0.052f * u), Color(0xFF2E5C48))
    drawPath(fxDisc2(0f, top * u, 0.075f * u, 0.052f * u), Ink.line, style = pen.stroke)
    val rec = f.mode - 1
    if (rec in 0..5) {
        drawPath(fxDisc2(0f, (top - 0.004f) * u, 0.07f * u, 0.048f * u), Color(0xFF241A38))
        drawPath(fxDisc2(0f, (top - 0.004f) * u, 0.07f * u, 0.048f * u), Ink.line, style = pen.thin)
        for (k in 1..3) drawPath(fxDisc2(0f, (top - 0.004f) * u, (0.07f - 0.014f * k) * u, (0.048f - 0.0095f * k) * u), Color.White.copy(alpha = 0.12f), style = pen.thin)
        drawPath(fxDisc2(0f, (top - 0.004f) * u, 0.025f * u, 0.017f * u), RECORD_COLORS[rec])
        drawPath(fxDisc2(0f, (top - 0.004f) * u, 0.025f * u, 0.017f * u), Ink.line, style = pen.thin)
        // A white mark that turns round while it plays, so the record looks like it spins.
        val a = if (playing) t * 4.2f else 0.6f
        val m = fxRim(0f, (top - 0.004f) * u, 0.05f * u, a)
        drawCircle(Color.White.copy(alpha = 0.85f), 0.0045f * u, Offset(m.x * 0.95f, m.y))
        drawCircle(Color(0xFF241A38), 0.003f * u, Offset(0f, (top - 0.004f) * u))
    }
    // The tonearm: a brass pivot at the back right, an arm that rests on the record while it plays.
    val pivot = q(0.075f, top, 0.06f)
    val head = if (playing) q(0.015f, top - 0.004f, 0.02f) else q(0.06f, top - 0.004f, 0.075f)
    drawLine(Ink.line, pivot, head, 0.008f * u + pen.lw * 2f, StrokeCap.Round)
    drawLine(AtC.brass, pivot, head, 0.008f * u, StrokeCap.Round)
    atDot(pivot, 0.01f * u, AtC.brassDark, pen)
    // The horn: a morning-glory bell on a bent brass neck.
    val bob = if (playing) sin(t * 9f) * 0.003f else 0f
    val neck = Path().apply {
        moveTo(p(0.03f, top).x, p(0.03f, top).y)
        cubicTo(p(0.075f, -0.17f).x, p(0.075f, -0.17f).y, p(0.07f, -0.23f).x, p(0.07f, -0.23f).y, p(0.025f, -0.235f + bob).x, p(0.025f, -0.235f + bob).y)
    }
    drawPath(neck, Ink.line, style = Stroke(0.016f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(neck, AtC.brass, style = Stroke(0.016f * u, cap = StrokeCap.Round))
    val bell = p(-0.01f, -0.225f + bob)
    rotate(-18f, bell) {
        val flower = Color(0xFF6AA7E8)
        drawOval(flower, Offset(bell.x - 0.062f * u, bell.y - 0.074f * u), Size(0.124f * u, 0.148f * u))
        drawOval(Ink.line, Offset(bell.x - 0.062f * u, bell.y - 0.074f * u), Size(0.124f * u, 0.148f * u), style = pen.stroke)
        // petals: five scallops of white edging
        for (k in 0 until 6) {
            val ang = k * 1.0472f
            drawCircle(Color.White, 0.012f * u, Offset(bell.x + cos(ang) * 0.058f * u, bell.y + sin(ang) * 0.07f * u), alpha = 0.9f)
        }
        drawOval(Color(0xFF1E2A5C), Offset(bell.x - 0.046f * u, bell.y - 0.058f * u), Size(0.092f * u, 0.116f * u))
        drawOval(Ink.line, Offset(bell.x - 0.046f * u, bell.y - 0.058f * u), Size(0.092f * u, 0.116f * u), style = pen.thin)
        drawOval(Color(0xFF3F5AA8), Offset(bell.x - 0.03f * u, bell.y - 0.04f * u), Size(0.05f * u, 0.07f * u), alpha = 0.8f)
        shine(Offset(bell.x - 0.04f * u, bell.y - 0.045f * u), 0.014f * u, 0.03f * u, 0.55f)
    }
    // A crank on the right side, turning while a record plays.
    val hub = q(0.09f, -0.07f, 0.05f)
    val ang = if (playing) t * 6f else 0.9f
    val grip = Offset(hub.x + cos(ang) * 0.022f * u, hub.y + sin(ang) * 0.022f * u)
    drawLine(Ink.line, hub, grip, 0.007f * u + pen.lw * 2f, StrokeCap.Round)
    drawLine(AtC.brass, hub, grip, 0.007f * u, StrokeCap.Round)
    atDot(grip, 0.007f * u, wood.lighten(0.2f), pen)
    atDot(hub, 0.006f * u, AtC.brassDark, pen)
    // Notes float out of the horn while it plays.
    if (playing) {
        for (k in 0 until 3) {
            val ph = fxFrac(t * 0.55f + k / 3f)
            fxNote(p(-0.07f - ph * 0.06f + sin(ph * 6f + k) * 0.01f, -0.25f - ph * 0.14f), 0.011f * u, Color(0xFF8B5CF6).lighten(0.1f * k), 0.9f * (1f - ph))
        }
    }
}

// ------------------------------------------------------------------------------------------ book tower

private val BOOK_COLORS = listOf(
    Color(0xFF3F8F6C), Color(0xFFD2443A), Color(0xFF2F6FB8), Color(0xFFE8B04A), Color(0xFF8B5CF6), Color(0xFFF08CB8), Color(0xFF6AB7C2),
)

internal fun DrawScope.atBookTower(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = if (f.mode == 1) pen.t else 0f
    atShadow(u, 0.16f, 0.1f)
    val shift = f.count % 7
    if (f.mode == 1) {
        // Toppled: books strewn about, spines every which way.
        for (i in 0 until 6) {
            val col = BOOK_COLORS[(i + shift) % 7]
            val x = -0.14f + i * 0.054f
            val ang = (hash01(i, 11) - 0.5f) * 80f
            val c = p(x, -0.02f - hash01(i, 12) * 0.03f)
            rotate(ang, c) {
                val r = Rect(c.x - 0.045f * u, c.y - 0.018f * u, c.x + 0.045f * u, c.y + 0.018f * u)
                inkedRound(r, 0.003f * u, col, pen)
                drawLine(AtC.goldLight, Offset(r.left + 0.008f * u, r.top + 0.006f * u), Offset(r.right - 0.008f * u, r.top + 0.006f * u), 0.003f * u)
                drawRect(Color(0xFFF7F0DC), Offset(r.right - 0.005f * u, r.top + 0.003f * u), Size(0.005f * u, r.height - 0.006f * u))
            }
        }
        for (k in 0 until 3) {
            val ph = fxFrac(t * 0.8f + k / 3f)
            drawRect(Color(0xFFF7F0DC).copy(alpha = 1f - ph), Offset(p(-0.05f + k * 0.05f, -0.06f).x, p(0f, -0.06f - ph * 0.15f).y), Size(0.016f * u, 0.02f * u))
        }
        return
    }
    // Standing, leaning a little more toward the top.
    var y = 0f
    for (i in 0 until 7) {
        val col = BOOK_COLORS[(i + shift) % 7]
        val hgt = 0.044f + 0.004f * (i % 2)
        val w = 0.1f + 0.014f * ((i * 3) % 3)
        val off = (hash01(i, 13) - 0.5f) * 0.02f + i * 0.0012f * (if (i % 2 == 0) 1f else -1f)
        fxBox(u, off - w / 2f, y - hgt, off + w / 2f, y, 0.08f, col, pen, rad = 0.004f)
        val fr = fxFront(u, off - w / 2f, y - hgt, off + w / 2f, y, 0f)
        drawLine(AtC.goldLight, Offset(fr.left + 0.007f * u, fr.top + 0.008f * u), Offset(fr.right - 0.007f * u, fr.top + 0.008f * u), 0.003f * u)
        drawLine(AtC.goldLight, Offset(fr.left + 0.007f * u, fr.bottom - 0.008f * u), Offset(fr.right - 0.007f * u, fr.bottom - 0.008f * u), 0.003f * u)
        when (i % 3) {
            0 -> drawCircle(AtC.goldLight, 0.005f * u, fr.center)
            1 -> drawPath(starPath(fr.center, 0.008f * u, 0.0035f * u), AtC.goldLight)
            else -> drawLine(AtC.goldLight, Offset(fr.center.x - 0.012f * u, fr.center.y), Offset(fr.center.x + 0.012f * u, fr.center.y), 0.003f * u)
        }
        y -= hgt
    }
    // A pair of round spectacles resting on the top book.
    val gl = p(0.005f, y - 0.011f)
    for (s in listOf(-1f, 1f)) drawCircle(Ink.line, 0.012f * u, Offset(gl.x + s * 0.017f * u, gl.y), style = Stroke(pen.lw * 1.3f))
    drawLine(Ink.line, Offset(gl.x - 0.005f * u, gl.y), Offset(gl.x + 0.005f * u, gl.y), pen.lw)
    for (s in listOf(-1f, 1f)) drawCircle(Color(0x55CFE8F7), 0.0105f * u, Offset(gl.x + s * 0.017f * u, gl.y))
}

// ------------------------------------------------------------------------------------------ candelabrum

internal fun DrawScope.atCandelabra(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = f.mode * 0.9f
    atShadow(u, 0.14f, 0.09f)
    if (f.on) fxGlow(p(0f, -0.26f), 0.28f * u, AtC.warm, 0.5f + 0.1f * sin(t * 5f))
    // Foot, stem with two knobs, and three curling arms.
    drawPath(fxDisc2(0f, -0.008f * u, 0.05f * u, 0.032f * u), AtC.brassDark)
    drawPath(fxDisc2(0f, -0.012f * u, 0.045f * u, 0.028f * u), AtC.brass)
    drawPath(fxDisc2(0f, -0.012f * u, 0.045f * u, 0.028f * u), Ink.line, style = pen.stroke)
    capsule(p(0f, -0.016f), p(0f, -0.2f), 0.012f * u, AtC.brass, pen)
    for (y in listOf(-0.06f, -0.12f)) atDot(p(0f, y), 0.014f * u, AtC.brass, pen)
    val armL = Path().apply { moveTo(p(0f, -0.1f).x, p(0f, -0.1f).y); cubicTo(p(-0.06f, -0.1f).x, p(-0.06f, -0.1f).y, p(-0.06f, -0.17f).x, p(-0.06f, -0.17f).y, p(-0.05f, -0.2f).x, p(-0.05f, -0.2f).y) }
    val armR = Path().apply { moveTo(p(0f, -0.1f).x, p(0f, -0.1f).y); cubicTo(p(0.06f, -0.1f).x, p(0.06f, -0.1f).y, p(0.06f, -0.17f).x, p(0.06f, -0.17f).y, p(0.05f, -0.2f).x, p(0.05f, -0.2f).y) }
    for (arm in listOf(armL, armR)) {
        drawPath(arm, Ink.line, style = Stroke(0.011f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(arm, AtC.brass, style = Stroke(0.011f * u, cap = StrokeCap.Round))
    }
    drawLine(AtC.brassLight, p(-0.003f, -0.2f), p(-0.003f, -0.03f), 0.003f * u, StrokeCap.Round, alpha = 0.7f)
    // Candles in cups; cream wax with a drip, a black wick and a flame.
    val spots = listOf(Triple(-0.05f, -0.2f, 0.07f), Triple(0f, -0.2f, 0.1f), Triple(0.05f, -0.2f, 0.07f))
    for ((i, s) in spots.withIndex()) {
        val (x, y, h) = s
        val cup = Path().apply { moveTo(p(x - 0.014f, y).x, p(x - 0.014f, y).y); lineTo(p(x + 0.014f, y).x, p(x + 0.014f, y).y); lineTo(p(x + 0.009f, y + 0.014f).x, p(x + 0.009f, y + 0.014f).y); lineTo(p(x - 0.009f, y + 0.014f).x, p(x - 0.009f, y + 0.014f).y); close() }
        inked(cup, AtC.brass, pen)
        val wax = Rect(p(x - 0.0085f, y - h).x, p(x, y - h).y, p(x + 0.0085f, y).x, p(x, y).y)
        inkedRound(wax, 0.003f * u, AtC.cream, pen)
        drawOval(Color(0xFFF7F0DC), Offset(wax.left - 0.001f * u, wax.top + 0.012f * u), Size(0.005f * u, 0.016f * u))
        drawLine(Ink.line, Offset(wax.center.x, wax.top), Offset(wax.center.x, wax.top - 0.008f * u), pen.lw * 0.8f, StrokeCap.Round)
        if (f.on) {
            fxFire(wax.center.x, wax.top - 0.004f * u, 0.016f * u, 0.04f * u, t, i * 1.7f + 2f, pen)
        } else {
            fxPuffs(wax.center.x, wax.top - 0.012f * u, t + i, 0.005f * u, 0.05f * u, Color(0xFFD8D4DC), 0.4f, 2, 0.3f, 0.008f * u)
        }
    }
}

// ------------------------------------------------------------------------------------------ fairy lights

internal fun DrawScope.atStringLights(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = f.mode * 0.8f
    val p0 = p(-0.32f, -0.13f)
    val p1 = p(0f, 0.04f)
    val p2 = p(0.32f, -0.13f)
    fun at(s: Float) = Offset((1 - s) * (1 - s) * p0.x + 2 * s * (1 - s) * p1.x + s * s * p2.x, (1 - s) * (1 - s) * p0.y + 2 * s * (1 - s) * p1.y + s * s * p2.y)
    val string = Path().apply { moveTo(p0.x, p0.y); quadraticTo(p1.x, p1.y, p2.x, p2.y) }
    drawPath(string, Ink.line, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    // Little paper ghosts hanging from the string, swaying, each with a smile.
    for (i in 0 until 5) {
        val s = 0.1f + i * 0.2f
        val a = at(s)
        val sway = sin(t * 1.4f + i * 1.3f) * 7f
        rotate(sway, a) {
            val h = 0.05f * u
            val w = 0.032f * u
            drawLine(Ink.line, a, Offset(a.x, a.y + 0.012f * u), pen.lw * 0.7f)
            val body = Path().apply {
                moveTo(a.x - w / 2f, a.y + h + 0.012f * u)
                lineTo(a.x - w / 2f, a.y + 0.03f * u)
                quadraticTo(a.x - w / 2f, a.y + 0.012f * u, a.x, a.y + 0.012f * u)
                quadraticTo(a.x + w / 2f, a.y + 0.012f * u, a.x + w / 2f, a.y + 0.03f * u)
                lineTo(a.x + w / 2f, a.y + h + 0.012f * u)
                lineTo(a.x + w * 0.25f, a.y + h - 0.004f * u)
                lineTo(a.x, a.y + h + 0.012f * u)
                lineTo(a.x - w * 0.25f, a.y + h - 0.004f * u)
                close()
            }
            inked(body, Color(0xFFF7F3EC), pen, shade = false)
            drawCircle(Ink.line, 0.0028f * u, Offset(a.x - 0.006f * u, a.y + 0.03f * u))
            drawCircle(Ink.line, 0.0028f * u, Offset(a.x + 0.006f * u, a.y + 0.03f * u))
            drawArc(Ink.line, 10f, 160f, false, Offset(a.x - 0.005f * u, a.y + 0.034f * u), Size(0.01f * u, 0.008f * u), style = pen.thin)
            drawCircle(Color(0xFFFF6F91).copy(alpha = 0.5f), 0.003f * u, Offset(a.x - 0.011f * u, a.y + 0.035f * u))
            drawCircle(Color(0xFFFF6F91).copy(alpha = 0.5f), 0.003f * u, Offset(a.x + 0.011f * u, a.y + 0.035f * u))
        }
    }
    // Between them, round bulbs: warm yellow, pink, mint and blue, glowing when it is on.
    val cols = listOf(Color(0xFFFFE18A), Color(0xFFFF9EC7), Color(0xFF8FE3B5), Color(0xFF8FC8FF))
    for (i in 0 until 6) {
        val s = 0.02f + i * 0.192f + 0.04f
        val a = at(s.coerceAtMost(0.98f))
        val col = cols[i % 4]
        if (f.on) fxGlow(Offset(a.x, a.y + 0.008f * u), 0.045f * u, col, 0.45f * (0.75f + 0.25f * sin(t * 2.5f + i * 1.7f)))
        drawLine(Ink.line, a, Offset(a.x, a.y + 0.006f * u), pen.lw * 0.7f)
        atDot(Offset(a.x, a.y + 0.013f * u), 0.0075f * u, if (f.on) col else col.darken(0.35f), pen)
        if (f.on) shine(Offset(a.x - 0.002f * u, a.y + 0.01f * u), 0.003f * u, 0.004f * u, 0.9f)
    }
}

// ------------------------------------------------------------------------------------------ blanket fort

private val QUILT = listOf(Color(0xFFE8A6B0), Color(0xFFE8B04A), Color(0xFF6AB7C2), Color(0xFFB9A2F0), Color(0xFF8FD9C0))

internal fun DrawScope.atBlanketFort(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = 0.8f
    val d = 0.15f
    atShadow(u, 0.4f, 0.17f)
    // The right slope, running back, patched with squares.
    val slope = fxQuad(q(0f, -0.25f, 0f).x, q(0f, -0.25f, 0f).y, q(0f, -0.25f, d).x, q(0f, -0.25f, d).y, q(0.17f, -0.005f, d).x, q(0.17f, -0.005f, d).y, q(0.17f, -0.005f, 0f).x, q(0.17f, -0.005f, 0f).y)
    drawPath(slope, QUILT[2].darken(0.12f))
    clipPath(slope) {
        for (k in 0 until 6) for (j in 0 until 3) {
            val c = q(0.03f + k * 0.03f, -0.22f + k * 0.04f, 0.02f + j * 0.05f)
            drawCircle(QUILT[(k + j) % 5].darken(0.2f), 0.014f * u, c, alpha = 0.7f)
        }
    }
    drawPath(slope, Ink.line, style = pen.stroke)
    // The front gable: a quilt of patches.
    val gable = Path().apply {
        moveTo(p(-0.18f, -0.005f).x, p(-0.18f, -0.005f).y)
        lineTo(p(0f, -0.25f).x, p(0f, -0.25f).y)
        lineTo(p(0.17f, -0.005f).x, p(0.17f, -0.005f).y)
        close()
    }
    drawPath(gable, QUILT[0])
    clipPath(gable) {
        for (j in 0 until 5) for (k in 0 until 7) {
            val r = Rect(p(-0.2f + k * 0.056f, -0.27f + j * 0.056f).x, p(0f, -0.27f + j * 0.056f).y, p(-0.2f + (k + 1) * 0.056f, 0f).x, p(0f, -0.27f + (j + 1) * 0.056f).y)
            drawRect(QUILT[(j * 2 + k) % 5], r.topLeft, r.size)
            drawRect(Ink.line.copy(alpha = 0.35f), r.topLeft, r.size, style = pen.thin)
        }
        // stitches
        for (k in 0 until 8) drawLine(Color.White.copy(alpha = 0.5f), p(-0.17f + k * 0.045f, -0.24f), p(-0.17f + k * 0.045f, -0.236f), pen.lw * 0.8f)
        // the entrance: a dark doorway, or cosy glow when the flap is up
        val door = Path().apply {
            moveTo(p(-0.05f, -0.005f).x, p(-0.05f, -0.005f).y)
            lineTo(p(-0.03f, -0.12f).x, p(-0.03f, -0.12f).y)
            quadraticTo(p(0f, -0.15f).x, p(0f, -0.15f).y, p(0.03f, -0.12f).x, p(0.03f, -0.12f).y)
            lineTo(p(0.05f, -0.005f).x, p(0.05f, -0.005f).y)
            close()
        }
        if (f.open) {
            drawPath(door, Color(0xFF3A2438))
            fxGlow(p(0f, -0.06f), 0.1f * u, AtC.warm, 0.8f)
            // two pairs of cosy cushions inside
            drawCircle(Color(0xFFE8A6B0), 0.02f * u, p(-0.02f, -0.025f))
            drawCircle(Color(0xFF8FD9C0), 0.018f * u, p(0.025f, -0.022f))
        } else {
            drawPath(door, QUILT[3].darken(0.1f))
            drawLine(Ink.line, p(0f, -0.14f), p(0f, -0.005f), pen.lw * 0.8f)
            atDot(p(-0.006f, -0.07f), 0.004f * u, AtC.cream, pen)
            atDot(p(0.006f, -0.07f), 0.004f * u, AtC.cream, pen)
        }
        drawPath(door, Ink.line, style = pen.stroke)
    }
    drawPath(gable, Ink.line, style = pen.stroke)
    // A broomstick for a ridge pole with a star pennant, and pegs holding the blanket.
    capsule(p(-0.04f, -0.27f), p(0.14f, -0.275f), 0.008f * u, Color(0xFFB8824D), pen)
    val flag = Path().apply { moveTo(p(0.13f, -0.275f).x, p(0.13f, -0.275f).y); lineTo(p(0.13f, -0.31f).x, p(0.13f, -0.31f).y); lineTo(p(0.17f, -0.295f).x, p(0.17f, -0.295f).y); close() }
    inked(flag, Color(0xFFFFC83D), pen, shade = false)
    // Cushions in front, and tiny fairy lights along the ridge.
    inkedOval(Rect(-0.19f * u, -0.03f * u, -0.115f * u, 0.012f * u), Color(0xFFE8A6B0), pen)
    inkedOval(Rect(0.09f * u, -0.025f * u, 0.17f * u, 0.014f * u), Color(0xFF8FD9C0), pen)
    for (k in 0 until 6) {
        val a = 0.8f + 0.2f * sin(t * 2f + k * 1.4f)
        val x = -0.16f + k * 0.032f
        val y = -0.005f - (x + 0.18f) * 0.7f
        twinkle(p(x, -0.01f - (1f - kotlin.math.abs(x) / 0.18f) * 0.24f), 0.008f * u * a, listOf(AtC.goldLight, Color(0xFFFF9EC7), Color(0xFF8FE3B5))[k % 3], a)
        if (y > 1f) Unit
    }
}

// ------------------------------------------------------------------------------------------ shadow theatre

internal fun DrawScope.atShadowTheatre(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = 0f
    val phase = f.mode * 0.5236f
    val wood = Color(0xFFB8824D)
    val screen = Rect(p(-0.115f, -0.235f).x, p(0f, -0.235f).y, p(0.115f, -0.03f).x, p(0f, -0.03f).y)
    // Frame, then the paper screen with a lamp's glow behind it.
    inkedRound(Rect(-0.16f * u, -0.27f * u, 0.16f * u, 0f), 0.012f * u, wood, pen)
    drawRect(Color(0xFFFFF3D2), screen.topLeft, screen.size)
    clipPath(rectPath(screen)) {
        drawRect(safeRadialGradient(0f to Color(0xFFFFE9A8), 0.55f to Color(0xFFFFF3D2), 1f to Color(0xFFF1D9A0), center = Offset(screen.center.x, screen.bottom - 0.03f * u), radius = 0.2f * u), screen.topLeft, screen.size)
        val shadow = Color(0xFF2A1F44).copy(alpha = 0.92f)
        val ox = screen.left + 0.02f * u
        val base = screen.bottom
        // The wrist: an arm shadow coming in from the lower left.
        val arm = Path().apply {
            moveTo(ox - 0.03f * u, base)
            lineTo(ox - 0.03f * u, base - 0.045f * u)
            lineTo(ox + 0.04f * u, base - 0.075f * u)
            lineTo(ox + 0.045f * u, base - 0.045f * u)
            lineTo(ox + 0.03f * u, base)
            close()
        }
        drawPath(arm, shadow)
        val hx = ox + 0.065f * u
        val hy = base - 0.06f * u
        when (f.count.mod(4)) {
            0 -> {
                // A rabbit: round head, two ears that wiggle.
                drawOval(shadow, Offset(hx - 0.02f * u, hy - 0.012f * u), Size(0.05f * u, 0.032f * u))
                for ((i, a) in listOf(-8f, 10f).withIndex()) {
                    val wig = sin(phase + i * 1.5f) * 9f
                    rotate(a + wig, Offset(hx + 0.004f * u + i * 0.014f * u, hy - 0.006f * u)) {
                        drawOval(shadow, Offset(hx + i * 0.014f * u - 0.0035f * u, hy - 0.08f * u), Size(0.011f * u, 0.07f * u))
                    }
                }
                drawCircle(Color(0xFFFFF3D2), 0.0035f * u, Offset(hx + 0.022f * u, hy))
            }
            1 -> {
                // A bird: body, head, a pointed beak and two flapping wings.
                drawOval(shadow, Offset(hx - 0.025f * u, hy - 0.012f * u), Size(0.055f * u, 0.032f * u))
                drawCircle(shadow, 0.015f * u, Offset(hx + 0.035f * u, hy - 0.016f * u))
                drawPath(Path().apply { moveTo(hx + 0.046f * u, hy - 0.02f * u); lineTo(hx + 0.07f * u, hy - 0.012f * u); lineTo(hx + 0.046f * u, hy - 0.01f * u); close() }, shadow)
                val flap = sin(phase * 2f) * 30f
                rotate(-30f + flap, Offset(hx, hy - 0.005f * u)) { drawOval(shadow, Offset(hx - 0.04f * u, hy - 0.06f * u), Size(0.045f * u, 0.065f * u)) }
                drawPath(Path().apply { moveTo(hx - 0.025f * u, hy); lineTo(hx - 0.055f * u, hy + 0.012f * u); lineTo(hx - 0.04f * u, hy + 0.02f * u); close() }, shadow)
                drawCircle(Color(0xFFFFF3D2), 0.003f * u, Offset(hx + 0.038f * u, hy - 0.02f * u))
            }
            2 -> {
                // A dog that barks: a snout with a jaw that opens and shuts.
                val bark = (sin(phase * 2f) + 1f) / 2f
                drawOval(shadow, Offset(hx - 0.02f * u, hy - 0.03f * u), Size(0.05f * u, 0.04f * u))
                drawPath(Path().apply { moveTo(hx + 0.015f * u, hy - 0.025f * u); lineTo(hx + 0.075f * u, hy - 0.02f * u); lineTo(hx + 0.07f * u, hy - 0.008f * u); lineTo(hx + 0.015f * u, hy - 0.002f * u); close() }, shadow)
                rotate(bark * 22f, Offset(hx + 0.02f * u, hy - 0.004f * u)) {
                    drawPath(Path().apply { moveTo(hx + 0.015f * u, hy - 0.004f * u); lineTo(hx + 0.065f * u, hy - 0.002f * u); lineTo(hx + 0.062f * u, hy + 0.012f * u); lineTo(hx + 0.02f * u, hy + 0.014f * u); close() }, shadow)
                }
                drawPath(Path().apply { moveTo(hx - 0.01f * u, hy - 0.03f * u); lineTo(hx - 0.02f * u, hy - 0.065f * u); lineTo(hx + 0.01f * u, hy - 0.038f * u); close() }, shadow)
                drawCircle(Color(0xFFFFF3D2), 0.003f * u, Offset(hx + 0.012f * u, hy - 0.022f * u))
            }
            else -> {
                // A butterfly with two big wings.
                val flap = 0.55f + 0.45f * sin(phase * 3f)
                val c = Offset(hx + 0.02f * u, hy - 0.03f * u)
                drawOval(shadow, Offset(c.x - 0.004f * u, c.y - 0.03f * u), Size(0.008f * u, 0.06f * u))
                for (s in listOf(-1f, 1f)) {
                    scale(flap, 1f, c) {
                        drawOval(shadow, Offset(c.x + (if (s < 0f) -0.05f else 0.004f) * u, c.y - 0.045f * u), Size(0.046f * u, 0.05f * u))
                        drawOval(shadow, Offset(c.x + (if (s < 0f) -0.04f else 0.004f) * u, c.y - 0.002f * u), Size(0.036f * u, 0.036f * u))
                    }
                }
            }
        }
    }
    drawRect(Ink.line, screen.topLeft, screen.size, style = pen.stroke)
    // Curtains draped to both sides with gold tassels, and a scalloped valance on top.
    for (s in listOf(-1f, 1f)) {
        val cx = s * 0.138f
        val cur = Path().apply {
            moveTo(p(cx - 0.022f, -0.255f).x, p(cx - 0.022f, -0.255f).y)
            lineTo(p(cx + 0.022f, -0.255f).x, p(cx + 0.022f, -0.255f).y)
            quadraticTo(p(cx + 0.03f * s, -0.12f).x, p(cx + 0.03f * s, -0.12f).y, p(cx + 0.018f * s, -0.01f).x, p(cx + 0.018f * s, -0.01f).y)
            lineTo(p(cx - 0.022f, -0.01f).x, p(cx - 0.022f, -0.01f).y)
            close()
        }
        inked(cur, Color(0xFF5B6FD1), pen)
        atFold(p(cx - 0.005f, -0.25f), p(cx - 0.01f, -0.12f), p(cx - 0.004f, -0.015f), Color(0xFF3E4FA0), pen.lw)
        atDot(p(cx, -0.03f), 0.006f * u, AtC.gold, pen)
    }
    for (k in 0 until 6) {
        val x = -0.15f + k * 0.06f
        val sc = Path().apply { addArc(Rect(p(x, -0.285f).x, p(0f, -0.285f).y, p(x + 0.06f, 0f).x, p(0f, -0.235f).y), 0f, 180f) }
        inked(sc, if (k % 2 == 0) Color(0xFFD9774F) else Color(0xFFE8B04A), pen, shade = false)
    }
}

// ------------------------------------------------------------------------------------------ grandfather clock

internal fun DrawScope.atGrandfather(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = 0f
    val wood = Color(0xFF8A5A3A)
    atShadow(u, 0.17f, 0.1f)
    // Plinth, waist and hood as three boxes.
    fxBox(u, -0.072f, -0.1f, 0.072f, 0f, 0.09f, wood.darken(0.1f), pen, rad = 0.004f)
    fxBox(u, -0.056f, -0.34f, 0.056f, -0.098f, 0.08f, wood, pen, rad = 0.003f, z = 0.005f)
    fxBox(u, -0.068f, -0.45f, 0.068f, -0.338f, 0.085f, wood.lighten(0.05f), pen, rad = 0.006f)
    // The long window with the pendulum behind glass.
    val win = Rect(p(-0.04f, -0.31f).x, p(0f, -0.31f).y, p(0.04f, -0.12f).x, p(0f, -0.12f).y)
    drawRect(Color(0xFF3A2438), win.topLeft, win.size)
    clipPath(rectPath(win)) {
        val sw = floatArrayOf(-0.34f, 0f, 0.34f, 0f)[f.mode.mod(4)]
        val top = p(0f, -0.3f)
        val end = Offset(top.x + sin(sw) * 0.14f * u, top.y + cos(sw) * 0.14f * u)
        drawLine(AtC.brassLight, top, end, 0.003f * u)
        drawCircle(AtC.brassDark, 0.022f * u, end)
        drawCircle(AtC.brass, 0.018f * u, end)
        drawCircle(AtC.brassLight, 0.006f * u, Offset(end.x - 0.006f * u, end.y - 0.006f * u))
        drawRect(Color.White.copy(alpha = 0.1f), win.topLeft, Size(win.width * 0.35f, win.height))
    }
    drawRect(Ink.line, win.topLeft, win.size, style = pen.stroke)
    // The face: cream, with twelve marks, an hour hand, a minute hand and a ticking second hand.
    val c = p(0f, -0.395f)
    val r = 0.044f * u
    drawCircle(AtC.brassDark, r + 0.006f * u, c)
    drawCircle(AtC.cream, r, c)
    drawCircle(Ink.line, r + 0.006f * u, c, style = pen.stroke)
    for (k in 0 until 12) {
        val a = k * 0.5236f
        drawLine(Ink.line, Offset(c.x + sin(a) * r * 0.82f, c.y - cos(a) * r * 0.82f), Offset(c.x + sin(a) * r * 0.95f, c.y - cos(a) * r * 0.95f), pen.lw * (if (k % 3 == 0) 1.2f else 0.7f), StrokeCap.Round)
    }
    val hour = 0.3f + t * 0.0003f
    val minute = 1.7f + t * 0.004f
    drawLine(Ink.line, c, Offset(c.x + sin(hour) * r * 0.5f, c.y - cos(hour) * r * 0.5f), pen.lw * 1.6f, StrokeCap.Round)
    drawLine(Ink.line, c, Offset(c.x + sin(minute) * r * 0.78f, c.y - cos(minute) * r * 0.78f), pen.lw * 1.2f, StrokeCap.Round)
    val sec = (t.toInt() % 60) * 0.10472f
    drawLine(AtC.gold, c, Offset(c.x + sin(sec) * r * 0.85f, c.y - cos(sec) * r * 0.85f), pen.lw * 0.7f, StrokeCap.Round)
    atDot(c, 0.005f * u, AtC.brass, pen)
    // A little arch on top with a golden ball, and brass feet.
    val arch = Path().apply { addArc(Rect(p(-0.068f, -0.5f).x, p(0f, -0.5f).y, p(0.068f, 0f).x, p(0f, -0.4f).y), 180f, 180f) }
    inked(arch, wood.lighten(0.08f), pen)
    atDot(p(0f, -0.502f), 0.009f * u, AtC.brass, pen)
    for (x in listOf(-0.06f, 0.06f)) fxBox(u, x - 0.01f, -0.01f, x + 0.01f, 0f, 0.015f, AtC.brass, pen)
    shine(p(-0.045f, -0.42f), 0.008f * u, 0.03f * u, 0.4f)
}
