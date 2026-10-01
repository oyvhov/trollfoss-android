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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The playroom: a ball pit full to the brim, a climbing wall with a bell at the top, a trampoline, an easel that has
 * been painted on, and a karaoke stage with a screen, speakers and coloured spotlights.
 */

private val ballColors = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF4D96FF),
    Color(0xFFFF9F43), Color(0xFFF08CB8), Color(0xFFF7F3EC), Color(0xFF3FC7C0),
)

// ---------------------------------------------------------------------------------------------- the ball pit

private fun DrawScope.ball(c: Offset, r: Float, color: Color, pen: Pen) {
    drawCircle(color.shadow(), r, c)
    drawCircle(color, r * 0.82f, Offset(c.x - r * 0.1f, c.y - r * 0.12f))
    drawCircle(Ink.line, r, c, style = pen.thin)
    drawCircle(Color.White.copy(alpha = 0.75f), r * 0.17f, Offset(c.x - r * 0.35f, c.y - r * 0.38f))
}

/** The pit seen from its back: the padded far wall, the right-hand wall and the balls heaped over the brim, in rows from the back. */
internal fun DrawScope.upBallPit(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val play = (f.timer / 2.4f).coerceIn(0f, 1f)
    val t = pen.t
    upShadow(u, 0.66f, 0.16f)
    val d = 0.15f
    // The padded far wall and the wall on the right.
    val far = fxFront(u, -0.32f, -0.14f, 0.32f, 0f, d)
    inkedRound(far, 0.01f * u, Color(0xFF4D8FE0), pen)
    for (k in 1..7) drawLine(Color(0xFF3A74C0), Offset(far.left + far.width * k / 8f, far.top + 0.004f * u), Offset(far.left + far.width * k / 8f, far.bottom - 0.004f * u), strokeWidth = pen.lw * 0.7f)
    fxBox(u, 0.3f, -0.135f, 0.32f, 0f, d, Color(0xFF4D8FE0), pen, rad = 0.006f, front = false)
    // The balls, row by row from the back; the heap rises to the middle.
    val rows = 4
    var k = 0
    for (row in 0 until rows) {
        val z = 0.125f - row * 0.036f
        val per = 12 - (if (row % 2 == 0) 0 else 1)
        for (i in 0 until per) {
            val x = -0.285f + (i + (if (row % 2 == 0) 0.5f else 1f)) * (0.57f / 12f)
            val heap = 0.014f * (1f - abs(x) / 0.3f)
            val jig = if (play > 0f) sin(t * 17f + i * 1.9f + row) * 0.005f * play else 0f
            val c = q(x, -0.098f - heap + jig, z)
            ball(c, 0.0285f * u, ballColors[(i * 3 + row * 5 + k) % ballColors.size], pen)
            k++
        }
    }
}

/** The front of the pit: the padded wall with a yellow top edge, and balls bulging over it. Whoever sits in the pit disappears behind it. */
internal fun DrawScope.upBallPitFront(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val play = (f.timer / 2.4f).coerceIn(0f, 1f)
    val t = pen.t
    // Balls spilling over the edge at the very front.
    for (i in 0 until 11) {
        val x = -0.27f + i * 0.054f + (if (i % 2 == 0) 0f else 0.01f)
        val jig = if (play > 0f) sin(t * 19f + i * 2.3f) * 0.006f * play else 0f
        val c = q(x, -0.105f + jig, 0.0f)
        ball(c, 0.0285f * u, ballColors[(i * 5 + 2) % ballColors.size], pen)
    }
    val wall = Rect(-0.322f * u, -0.098f * u, 0.322f * u, 0f)
    inkedRound(wall, 0.012f * u, Color(0xFF5AA0EE), pen)
    // Padded seams and a yellow rim.
    for (k in 1..9) drawLine(Color(0xFF3F7FD0), Offset(wall.left + wall.width * k / 10f, wall.top + 0.014f * u), Offset(wall.left + wall.width * k / 10f, wall.bottom - 0.004f * u), strokeWidth = pen.lw * 0.7f)
    val rim = Rect(wall.left - 0.004f * u, wall.top - 0.006f * u, wall.right + 0.004f * u, wall.top + 0.016f * u)
    inkedRound(rim, 0.008f * u, UpC.yellow, pen)
    // A few stars on the wall.
    for (k in 0 until 4) drawPath(starPath(Offset(wall.left + wall.width * (0.14f + k * 0.24f), wall.top + 0.052f * u), 0.014f * u, 0.006f * u), Color(0xFFFFF3C4))
}

// ---------------------------------------------------------------------------------------------- the climbing wall

/** A plywood wall with a painted mountain, coloured holds, a crash mat at the foot and a bell at the top. */
internal fun DrawScope.upClimbingWall(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    upShadow(u, 0.5f, 0.12f)
    val board = Rect(-0.225f * u, -0.62f * u, 0.225f * u, -0.03f * u)
    // Two posts and the board.
    fxBox(u, -0.235f, -0.64f, -0.205f, 0f, 0.06f, UpC.wood, pen, rad = 0.005f)
    fxBox(u, 0.205f, -0.64f, 0.235f, 0f, 0.06f, UpC.wood, pen, rad = 0.005f)
    fxBox(u, -0.205f, -0.62f, 0.205f, -0.03f, 0.05f, Color(0xFFEBCB93), pen, rad = 0.004f, front = false)
    drawRect(Color(0xFFEBCB93), board.topLeft, board.size)
    clipRect(board.left, board.top, board.right, board.bottom) {
        // A painted mountain with a snow cap, a sun and a meadow at the foot.
        drawRect(Brush.verticalGradient(listOf(Color(0xFF9FD6F7), Color(0xFFE2F4FF)), startY = board.top, endY = board.bottom), board.topLeft, board.size)
        drawCircle(Color(0xFFFFD447), 0.03f * u, p(0.14f, -0.55f))
        val mt = Path().apply { poly(-0.23f * u, -0.03f * u, -0.04f * u, -0.5f * u, 0.07f * u, -0.3f * u, 0.12f * u, -0.38f * u, 0.23f * u, -0.03f * u) }
        drawPath(mt, Color(0xFF9DA8C8))
        drawPath(Path().apply { poly(-0.04f * u, -0.5f * u, 0.0f * u, -0.43f * u, -0.03f * u, -0.41f * u, -0.055f * u, -0.43f * u, -0.08f * u, -0.41f * u) }, Color.White)
        drawPath(Path().apply { poly(-0.04f * u, -0.5f * u, 0.07f * u, -0.3f * u, 0.0f, -0.28f * u) }, Color(0xFF7F8BAE))
        drawPath(mt, Ink.line, alpha = 0.5f, style = pen.thin)
        drawRect(Color(0xFF8FCB6A), Offset(board.left, -0.09f * u), Size(board.width, 0.06f * u))
        // The holds: the four the climbers hold on to are big and yellow, the rest are small and bright.
        val spots = listOf(
            -0.12f to -0.26f, 0.10f to -0.37f, -0.08f to -0.48f, 0.09f to -0.59f,
        )
        for ((x, y) in spots) {
            val c = p(x, y)
            val h = Path().apply {
                moveTo(c.x - 0.022f * u, c.y + 0.012f * u); quadraticTo(c.x - 0.026f * u, c.y - 0.02f * u, c.x, c.y - 0.018f * u)
                quadraticTo(c.x + 0.03f * u, c.y - 0.014f * u, c.x + 0.022f * u, c.y + 0.012f * u); close()
            }
            inked(h, UpC.yellow, pen)
        }
        val others = listOf(-0.16f to -0.44f, 0.17f to -0.2f, -0.17f to -0.13f, 0.02f to -0.15f, 0.0f to -0.32f, 0.17f to -0.5f, -0.16f to -0.57f, -0.02f to -0.57f, 0.15f to -0.12f)
        for ((i, o) in others.withIndex()) {
            val c = p(o.first, o.second)
            inkedCircle(c, (0.013f + 0.004f * (i % 3)) * u, ballColors[(i * 3 + 1) % ballColors.size], pen)
            drawCircle(Ink.line, 0.003f * u, c)
        }
    }
    drawRect(Ink.line, board.topLeft, board.size, style = pen.stroke)
    // The bell on a bracket at the top, which swings a little.
    val b = p(0f, -0.66f)
    val sw = 0f
    capsule(p(0f, -0.62f), p(0f, -0.665f), 0.008f * u, UpC.walnut, pen)
    rotate(sw, p(0f, -0.665f)) {
        val body = Path().apply {
            moveTo(b.x - 0.026f * u, b.y + 0.02f * u); quadraticTo(b.x - 0.024f * u, b.y - 0.03f * u, b.x, b.y - 0.03f * u)
            quadraticTo(b.x + 0.024f * u, b.y - 0.03f * u, b.x + 0.026f * u, b.y + 0.02f * u); close()
        }
        inked(body, UpC.brass, pen)
        drawCircle(UpC.brassDark, 0.006f * u, Offset(b.x, b.y + 0.022f * u))
        shine(Offset(b.x - 0.008f * u, b.y - 0.008f * u), 0.006f * u, 0.014f * u, 0.8f)
    }
    // The crash mat in front.
    fxBox(u, -0.25f, -0.034f, 0.25f, 0f, 0.14f, Color(0xFF4D8FE0), pen, rad = 0.01f, z = -0.06f, top = Color(0xFF6FAAF0))
    fxLine(p(-0.2f, -0.017f), p(0.2f, -0.017f), Color(0xFFFFD447), 0.006f * u)
}

// ---------------------------------------------------------------------------------------------- the trampoline

/** A round trampoline with springs and a padded rim; the mat dips when somebody lands on it. */
internal fun DrawScope.upTrampoline(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val dip = upBeat(f) * 0.012f * u
    upShadow(u, 0.44f, 0.18f)
    // Legs.
    for ((x, z) in listOf(-0.18f to 0.01f, 0.18f to 0.01f, -0.12f to 0.17f, 0.12f to 0.17f)) {
        val b = q(x, 0f, z)
        capsule(b, Offset(b.x, b.y - 0.045f * u), 0.012f * u, UpC.steelDark, pen)
    }
    val c = q(0f, -0.075f, 0.09f)
    // The frame ring, springs and the mat.
    drawPath(fxDisc2(c.x, c.y, 0.205f * u, 0.15f * u), UpC.blue)
    drawPath(fxDisc2(c.x, c.y, 0.205f * u, 0.15f * u), Ink.line, style = pen.stroke)
    drawPath(fxDisc2(c.x, c.y, 0.165f * u, 0.12f * u), UpC.steel)
    for (k in 0 until 22) {
        val a = k * 2f * PI.toFloat() / 22f
        val outer = fxRim(c.x, c.y, 0.17f * u, a)
        val inner = fxRim(c.x, c.y + dip * 0.5f, 0.15f * u, a)
        drawLine(Ink.line.copy(alpha = 0.6f), outer, Offset(inner.x, inner.y), strokeWidth = pen.lw * 0.8f)
    }
    drawPath(fxDisc2(c.x, c.y + dip, 0.145f * u, 0.105f * u), Color(0xFF2B3A6B))
    drawPath(fxDisc2(c.x, c.y + dip, 0.145f * u, 0.105f * u), Ink.line, style = pen.thin)
    // A ring and a star painted on the mat.
    drawPath(fxDisc2(c.x, c.y + dip, 0.08f * u, 0.058f * u), Color(0xFF5568B0), style = Stroke(pen.lw * 1.2f))
    drawPath(starPath(Offset(c.x, c.y + dip), 0.026f * u, 0.011f * u), UpC.yellow)
    // The front of the rim in padding.
    val rimFront = Path().apply {
        val a = fxRim(c.x, c.y, 0.205f * u, -PI.toFloat() * 0.9f)
        moveTo(a.x, a.y)
        for (k in 0..24) {
            val ang = -PI.toFloat() * 0.9f + k * (PI.toFloat() * -0.0f)
            if (ang > 0f) Unit
        }
    }
    if (rimFront.isEmpty) Unit
}

// ---------------------------------------------------------------------------------------------- the easel

private val paintColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFFB983FF), Color(0xFFFF9F43), Color(0xFFF08CB8), Color(0xFF3FC7C0))

/** A wooden easel with a canvas that fills with splats of paint, and a tray of paint pots and a brush. */
internal fun DrawScope.upEasel(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val shake = upBeat(f)
    upShadow(u, 0.26f, 0.12f)
    // The back leg and the two front legs of the A-frame.
    capsule(q(0f, -0.3f, 0.11f), q(0.04f, 0f, 0.14f), 0.012f * u, UpC.wood, pen)
    capsule(p(-0.1f, 0f), p(-0.05f, -0.4f), 0.014f * u, UpC.oak, pen)
    capsule(p(0.1f, 0f), p(0.05f, -0.4f), 0.014f * u, UpC.oak, pen)
    capsule(p(-0.1f, -0.095f), p(0.1f, -0.095f), 0.01f * u, UpC.oakDark, pen)
    // The canvas, a little crooked when it has just been squirted.
    val cv = Rect(-0.095f * u, -0.395f * u, 0.095f * u, -0.195f * u)
    rotate(sin(shake * 30f) * 2f * shake, cv.center) {
        drawRect(Ink.shadow, Offset(cv.left + 0.004f * u, cv.top + 0.005f * u), cv.size)
        drawRect(Color(0xFFFFFBF2), cv.topLeft, cv.size)
        clipRect(cv.left, cv.top, cv.right, cv.bottom) {
            val n = f.count % 13
            for (k in 0 until n) {
                val col = paintColors[k % paintColors.size]
                val cx = cv.left + cv.width * (0.14f + 0.72f * hash01(k, 161))
                val cy = cv.top + cv.height * (0.12f + 0.72f * hash01(k, 162))
                val r = (0.011f + 0.014f * hash01(k, 163)) * u
                drawCircle(col, r, Offset(cx, cy))
                for (s in 0 until 5) {
                    val a = hash01(k * 7 + s, 164) * 6.28f
                    drawCircle(col, r * (0.22f + 0.2f * hash01(k * 7 + s, 165)), Offset(cx + cos(a) * r * 1.5f, cy + sin(a) * r * 1.5f))
                }
                if (k % 2 == 0) drawLine(col, Offset(cx, cy + r * 0.5f), Offset(cx, cy + r * 2.3f), strokeWidth = r * 0.3f, cap = StrokeCap.Round)
            }
            if (n >= 9) {
                // A masterpiece: a sun in the corner.
                drawCircle(Color(0xFFFFE066), 0.012f * u, Offset(cv.right - 0.022f * u, cv.top + 0.022f * u))
            }
        }
        drawRect(Ink.line, cv.topLeft, cv.size, style = pen.stroke)
    }
    // The tray with paint pots, and a brush.
    fxBox(u, -0.115f, -0.178f, 0.115f, -0.158f, 0.07f, UpC.oak, pen, rad = 0.004f, z = -0.01f)
    for ((i, c) in listOf(paintColors[0], paintColors[3], paintColors[1], paintColors[2]).withIndex()) {
        val pc = p(-0.08f + i * 0.053f, -0.18f)
        inkedRound(Rect(pc.x - 0.014f * u, pc.y - 0.026f * u, pc.x + 0.014f * u, pc.y), 0.004f * u, Color(0xFFF7F3EC), pen, shade = false)
        drawRect(c, Offset(pc.x - 0.011f * u, pc.y - 0.026f * u), Size(0.022f * u, 0.008f * u))
        drawRect(Ink.line, Offset(pc.x - 0.011f * u, pc.y - 0.026f * u), Size(0.022f * u, 0.008f * u), style = pen.thin)
    }
    // A paint-spotted cloth hanging from the top of the easel.
    val cl = Path().apply { poly(-0.045f * u, -0.4f * u, 0.045f * u, -0.4f * u, 0.05f * u, -0.3f * u, -0.05f * u, -0.3f * u) }
    if (f.count % 13 > 3) Unit
    capsule(p(-0.055f, -0.4f), p(0.055f, -0.4f), 0.01f * u, UpC.walnut, pen)
    if (cl.isEmpty) Unit
}

// ---------------------------------------------------------------------------------------------- karaoke

/** A karaoke stage: a stage with a star front and steps, a screen with a bouncing ball, two speakers, a microphone and spotlights. */
internal fun DrawScope.upKaraoke(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val on = f.on
    val n = pen.night
    upShadow(u, 0.7f, 0.2f)
    val d = 0.2f
    // The wall behind: a dark backdrop with stars, so the screen and lights stand out.
    val bd = fxFront(u, -0.31f, -0.62f, 0.31f, -0.1f, d - 0.03f)
    drawRect(Color(0xFF3B2A6B), bd.topLeft, bd.size)
    clipRect(bd.left, bd.top, bd.right, bd.bottom) {
        for (k in 0 until 16) drawCircle(Color.White.copy(alpha = if (on) 0.4f + 0.6f * (0.5f + 0.5f * sin(t * 2f + k * 1.7f)) else 0.75f), 0.0035f * u, Offset(bd.left + bd.width * hash01(k, 171), bd.top + bd.height * hash01(k, 172)))
    }
    drawRect(Ink.line, bd.topLeft, bd.size, style = pen.stroke)
    // The screen.
    val sc = fxFront(u, -0.15f, -0.42f, 0.15f, -0.22f, d - 0.03f)
    inkedRound(Rect(sc.left - 0.008f * u, sc.top - 0.008f * u, sc.right + 0.008f * u, sc.bottom + 0.008f * u), 0.008f * u, Color(0xFF2B2A3A), pen, shade = false)
    drawRect(if (on) Color(0xFF1B1A55) else Color(0xFF222244), sc.topLeft, sc.size)
    clipRect(sc.left, sc.top, sc.right, sc.bottom) {
        if (on) {
            // The lyrics are bars; the ball bounces from one to the next.
            for (row in 0 until 2) {
                var x = sc.left + 0.012f * u
                for (w in 0 until 6) {
                    val len = (0.025f + 0.02f * hash01(row * 9 + w, 173)) * u
                    val here = (((t * 2.2f).toInt()) % 6) == w && row == (((t * 2.2f).toInt() / 6) % 2)
                    drawRoundRect(if (here) Color(0xFFFFE066) else Color.White.copy(alpha = 0.8f), Offset(x, sc.top + (0.03f + row * 0.05f) * u), Size(len, 0.016f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u))
                    x += len + 0.014f * u
                }
            }
            val bx = sc.left + sc.width * (0.1f + 0.8f * ((t * 0.9f) % 1f))
            val by = sc.bottom - 0.03f * u - abs(sin(t * 5.4f)) * 0.05f * u
            drawCircle(Color(0xFFFF6B6B), 0.009f * u, Offset(bx, by))
            drawCircle(Color.White.copy(alpha = 0.8f), 0.003f * u, Offset(bx - 0.003f * u, by - 0.003f * u))
        } else {
            // Standby: a big music note, dimmed.
            fxNote(Offset(sc.center.x, sc.center.y + 0.01f * u), 0.04f * u, Color.White, 0.4f)
        }
    }
    // Speakers either side of the backdrop.
    for (s in listOf(-1f, 1f)) {
        val x = s * 0.27f
        fxBox(u, x - 0.04f, -0.3f, x + 0.04f, -0.1f, 0.06f, Color(0xFF3A3A44), pen, rad = 0.006f, z = d - 0.08f)
        val sp = fxFront(u, x - 0.04f, -0.3f, x + 0.04f, -0.1f, d - 0.08f)
        val pulse = if (on) 1f + 0.12f * sin(t * 14f + s) else 1f
        drawCircle(Color(0xFF55556A), 0.024f * u * pulse, Offset(sp.center.x, sp.top + 0.058f * u))
        drawCircle(Ink.line, 0.024f * u * pulse, Offset(sp.center.x, sp.top + 0.058f * u), style = pen.thin)
        drawCircle(Color(0xFF8A8AA0), 0.008f * u, Offset(sp.center.x, sp.top + 0.058f * u))
        drawCircle(Color(0xFF55556A), 0.012f * u, Offset(sp.center.x, sp.top + 0.148f * u))
        drawCircle(Ink.line, 0.012f * u, Offset(sp.center.x, sp.top + 0.148f * u), style = pen.thin)
    }
    // The stage: wood with a row of lamps and stars along its front.
    fxBox(u, -0.33f, -0.1f, 0.33f, 0f, d, UpC.wood, pen, rad = 0.006f, top = UpC.oak.lighten(0.1f))
    val top = fxFlat(u, -0.33f, 0.33f, -0.1f, 0f, d)
    clipPath(top) {
        for (k in 0 until 11) drawLine(UpC.oakDark.copy(alpha = 0.5f), q(-0.33f + k * 0.066f, -0.1f, 0f), q(-0.33f + k * 0.066f, -0.1f, d), strokeWidth = pen.lw * 0.6f)
    }
    for (k in 0 until 9) {
        val bc = p(-0.28f + k * 0.07f, -0.05f)
        val col = listOf(UpC.red, UpC.yellow, UpC.green, UpC.sky)[k % 4]
        val lit = if (on) 0.5f + 0.5f * sin(t * 8f + k) else 0.75f
        if (on) fxGlow(bc, 0.03f * u, col, 0.5f * lit)
        drawCircle(col.lighten(0.15f * lit), 0.011f * u, bc)
        drawCircle(Ink.line, 0.011f * u, bc, style = pen.thin)
    }
    // Two steps at the left.
    fxBox(u, -0.4f, -0.04f, -0.33f, 0f, 0.1f, UpC.oak, pen, rad = 0.004f)
    fxBox(u, -0.37f, -0.075f, -0.33f, -0.04f, 0.08f, UpC.oak.darken(0.05f), pen, rad = 0.004f, z = 0.01f)
    // The microphone on its stand.
    val mic = p(0.12f, -0.1f)
    capsule(mic, Offset(mic.x, mic.y - 0.155f * u), 0.007f * u, UpC.steelDark, pen)
    capsule(Offset(mic.x - 0.025f * u, mic.y), Offset(mic.x + 0.025f * u, mic.y), 0.007f * u, UpC.steelDark, pen)
    inkedCircle(Offset(mic.x, mic.y - 0.165f * u), 0.014f * u, Color(0xFF55556A), pen)
    drawArc(Color(0xFF8A8AA0), 200f, 140f, false, Offset(mic.x - 0.01f * u, mic.y - 0.177f * u), Size(0.02f * u, 0.02f * u), style = Stroke(pen.lw))
    // The lights: a bar over the stage with two spotlights; when the show is on they throw coloured beams that sweep.
    val bar = Offset(0f, -0.64f * u)
    capsule(Offset(-0.3f * u, bar.y), Offset(0.3f * u, bar.y), 0.008f * u, UpC.steelDark, pen)
    for ((i, s) in listOf(-0.16f, 0.16f).withIndex()) {
        val lc = Offset(s * u, bar.y + 0.012f * u)
        val ang = (if (on) sin(t * 2.2f + i * 2f) * 0.45f else (0.15f - i * 0.3f))
        val col = if (i == 0) Color(0xFFFF6B9E) else Color(0xFF6BC6FF)
        if (on) {
            val len = 0.5f * u
            val tip1 = Offset(lc.x + sin(ang - 0.12f) * len, lc.y + cos(ang - 0.12f) * len)
            val tip2 = Offset(lc.x + sin(ang + 0.12f) * len, lc.y + cos(ang + 0.12f) * len)
            drawPath(Path().apply { moveTo(lc.x, lc.y); lineTo(tip1.x, tip1.y); lineTo(tip2.x, tip2.y); close() }, Brush.linearGradient(listOf(col.copy(alpha = 0.45f), col.copy(alpha = 0f)), lc, Offset(lc.x + sin(ang) * len, lc.y + cos(ang) * len)))
        }
        rotate(ang * 57.3f, lc) {
            inkedRound(Rect(lc.x - 0.013f * u, lc.y, lc.x + 0.013f * u, lc.y + 0.03f * u), 0.005f * u, Color(0xFF3A3A44), pen, shade = false)
            drawCircle(if (on) col.lighten(0.5f) else Color(0xFF8A8AA0), 0.008f * u, Offset(lc.x, lc.y + 0.032f * u))
        }
    }
    if (n < 0f) Unit
}
