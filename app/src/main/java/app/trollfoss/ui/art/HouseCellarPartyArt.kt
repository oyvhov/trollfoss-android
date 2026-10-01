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
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

/*
 * The party room: a dance floor whose tiles light up under dancing feet, a jukebox with five coloured
 * buttons, a karaoke screen with a bouncing ball, a snack bar with a popcorn machine, bar stools, neon
 * signs and a confetti cannon.
 */

// ====================================================================================== the dance floor

private const val FLOOR_COLS = 7
private const val FLOOR_ROWS = 3

/**
 * The dance floor: 7 by 3 tiles lying on the floor, the origin at the middle of its front edge. A tile glows
 * in a neon colour when the bit for it is set in the mask kept in [Fixture.angleV] (somebody dances on it),
 * the whole floor chases lights while a party goes on without dancers, and a tap sends a rainbow wave.
 */
internal fun DrawScope.ceDanceFloor(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val party = f.on
    val mask = f.angleV.toInt()
    val w = f.spec.w
    val depth = f.spec.h / 0.36f
    val tw = w / FLOOR_COLS
    val dz = depth / FLOOR_ROWS
    val wave = if (f.timer > 0f) (2.4f - f.timer) * 3.4f - 1f else -9f
    val neon = CeC.neon
    // A chrome frame round the whole floor.
    val a = fxQ(u, -w / 2f - 0.012f, 0f, -0.012f)
    val b = fxQ(u, w / 2f + 0.012f, 0f, -0.012f)
    val c = fxQ(u, w / 2f + 0.012f, 0f, depth + 0.012f)
    val d = fxQ(u, -w / 2f - 0.012f, 0f, depth + 0.012f)
    val frame = fxPath(a, b, c, d)
    drawPath(frame, Color(0xFFD8E2EC))
    drawPath(frame, Ink.line, style = pen.stroke)
    for (r in 0 until FLOOR_ROWS) {
        for (col in 0 until FLOOR_COLS) {
            val x0 = -w / 2f + col * tw
            val z0 = r * dz
            val p0 = fxQ(u, x0, 0f, z0)
            val p1 = fxQ(u, x0 + tw, 0f, z0)
            val p2 = fxQ(u, x0 + tw, 0f, z0 + dz)
            val p3 = fxQ(u, x0, 0f, z0 + dz)
            val tile = fxPath(p0, p1, p2, p3)
            val lit = mask and (1 shl (r * FLOOR_COLS + col)) != 0
            val chase = party && !lit && ((col + r * 2 + floor(t * 5f).toInt()) % 9 == 0)
            val rainbow = abs(col - wave) < 1.1f
            val color = neon[(col + r + floor(t * 2f).toInt()) % neon.size]
            val base = if ((col + r) % 2 == 0) Color(0xFF241A4E) else Color(0xFF2E2260)
            drawPath(tile, base)
            if (lit || rainbow || chase) {
                val k = if (lit) 1f else if (rainbow) 0.9f else 0.5f
                val tint = if (rainbow) neon[(col + floor(t * 6f).toInt()) % neon.size] else color
                drawPath(tile, tint.copy(alpha = 0.85f * k))
                val center = Offset((p0.x + p2.x) / 2f, (p0.y + p2.y) / 2f)
                fxGlow(center, tw * u * 0.9f, tint, 0.55f * k)
                // A bright gleam across the tile.
                drawLine(Color.White.copy(alpha = 0.45f * k), Offset(p3.x + (p2.x - p3.x) * 0.15f, p3.y + (p2.y - p3.y) * 0.15f), Offset(p3.x + (p2.x - p3.x) * 0.55f, p3.y + (p2.y - p3.y) * 0.55f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
            } else if (party) {
                drawPath(tile, color.copy(alpha = 0.12f))
            }
            drawPath(tile, Ink.line.copy(alpha = 0.8f), style = pen.thin)
        }
    }
    // A soft shine across the glossy floor.
    drawLine(Color.White.copy(alpha = if (party) 0.12f else 0.07f), fxQ(u, -w * 0.4f, 0f, depth * 0.85f), fxQ(u, -w * 0.12f, 0f, depth * 0.15f), strokeWidth = pen.lw * 3f, cap = StrokeCap.Round)
}

// ============================================================================================ jukebox

/**
 * A jukebox of chrome and violet with a glowing arch, a record that spins, five coloured buttons (the pressed
 * one is the tune that plays, [Fixture.mode] 1 to 5) and notes that rise when it plays.
 */
internal fun DrawScope.ceJukebox(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val tune = f.mode
    val playing = tune > 0
    val d = 0.12f
    ceShadow(u, 0.2f, d)
    val violet = Color(0xFF5B3FA8)
    fun bodyPath(i: Float = 0f) = Path().apply {
        moveTo(p(-0.1f + i, -0.02f).x, p(-0.1f + i, -0.02f).y)
        lineTo(p(-0.1f + i, -0.27f).x, p(-0.1f + i, -0.27f).y)
        quadraticTo(p(-0.1f + i, -0.355f).x, p(-0.1f + i, -0.355f).y, p(0f, -0.355f).x, p(0f, -0.355f).y)
        quadraticTo(p(0.1f - i, -0.355f).x, p(0.1f - i, -0.355f).y, p(0.1f - i, -0.27f).x, p(0.1f - i, -0.27f).y)
        lineTo(p(0.1f - i, -0.02f).x, p(0.1f - i, -0.02f).y)
        close()
    }
    // The right side, running back.
    val side = Path().apply {
        moveTo(p(0.1f, -0.02f).x, p(0.1f, -0.02f).y)
        lineTo(p(0.1f, -0.27f).x, p(0.1f, -0.27f).y)
        quadraticTo(p(0.1f, -0.355f).x, p(0.1f, -0.355f).y, q(0.0f, -0.355f, d * 0.4f).x, q(0.0f, -0.355f, d * 0.4f).y)
        val e = q(0.1f, -0.27f, d * 0.7f)
        lineTo(e.x, e.y)
        val g = q(0.1f, -0.02f, d * 0.7f)
        lineTo(g.x, g.y)
        close()
    }
    drawPath(side, violet.darken(0.3f))
    drawPath(side, Ink.line, style = pen.stroke)
    // Feet.
    for (s in floatArrayOf(-0.075f, 0.075f)) inkedRound(Rect(p(s - 0.014f, -0.022f), p(s + 0.014f, 0f)), 0.003f * u, Color(0xFFD8E2EC), pen, shade = false)
    val body = bodyPath()
    inked(body, violet, pen)
    // The chrome rim of the arch with tubes of neon light inside it.
    val arch = Path().apply {
        moveTo(p(-0.078f, -0.17f).x, p(-0.078f, -0.17f).y)
        lineTo(p(-0.078f, -0.27f).x, p(-0.078f, -0.27f).y)
        quadraticTo(p(-0.078f, -0.335f).x, p(-0.078f, -0.335f).y, p(0f, -0.335f).x, p(0f, -0.335f).y)
        quadraticTo(p(0.078f, -0.335f).x, p(0.078f, -0.335f).y, p(0.078f, -0.27f).x, p(0.078f, -0.27f).y)
        lineTo(p(0.078f, -0.17f).x, p(0.078f, -0.17f).y)
        close()
    }
    drawPath(arch, Color(0xFFD8E2EC))
    drawPath(arch, Ink.line, style = pen.stroke)
    val window = Path().apply {
        moveTo(p(-0.066f, -0.18f).x, p(-0.066f, -0.18f).y)
        lineTo(p(-0.066f, -0.27f).x, p(-0.066f, -0.27f).y)
        quadraticTo(p(-0.066f, -0.322f).x, p(-0.066f, -0.322f).y, p(0f, -0.322f).x, p(0f, -0.322f).y)
        quadraticTo(p(0.066f, -0.322f).x, p(0.066f, -0.322f).y, p(0.066f, -0.27f).x, p(0.066f, -0.27f).y)
        lineTo(p(0.066f, -0.18f).x, p(0.066f, -0.18f).y)
        close()
    }
    drawPath(window, Color(0xFF1A1230))
    clipPath(window) {
        // Neon arcs follow the shape; they shimmer through colours while it plays.
        for (k in 0 until 3) {
            val r = (0.062f - k * 0.016f) * u
            val color = if (playing) CeC.neon[(k * 2 + floor(t * 3f).toInt()) % CeC.neon.size] else CeC.neon[k * 2 % 5].darken(0.55f)
            val cy = -0.27f * u
            drawArc(color, 180f, 180f, false, Offset(-r, cy - r), Size(r * 2f, r * 2f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
        }
        // The record: a black disc with a coloured label, spinning on its turntable.
        val c = p(0f, -0.215f)
        val rr = 0.04f * u
        drawCircle(Color(0xFF0E0A1A), rr, c)
        drawCircle(Color(0xFF3A3070), rr * 0.8f, c, style = Stroke(pen.lw * 0.6f))
        drawCircle(Color(0xFF3A3070), rr * 0.6f, c, style = Stroke(pen.lw * 0.6f))
        val spin = if (playing) t * 6f else 0f
        drawCircle(CeC.neon[(tune.coerceAtLeast(1) - 1) % 5], rr * 0.32f, c)
        drawCircle(Color.White, rr * 0.06f, c)
        drawLine(Color.White.copy(alpha = 0.7f), Offset(c.x + cos(spin) * rr * 0.4f, c.y + sin(spin) * rr * 0.4f), Offset(c.x + cos(spin) * rr * 0.9f, c.y + sin(spin) * rr * 0.9f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
        // The tone arm.
        drawLine(CeC.steel, p(0.058f, -0.26f), Offset(c.x + 0.012f * u, c.y - 0.012f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
        if (playing) {
            // Equaliser bars dance along the bottom of the window.
            for (k in 0 until 7) {
                val h = (0.01f + 0.022f * (0.5f + 0.5f * sin(t * 9f + k * 1.7f))) * u
                drawRect(CeC.neon[k % 5], Offset(-0.054f * u + k * 0.0165f * u, -0.18f * u - h), Size(0.011f * u, h))
            }
        }
    }
    drawPath(window, Ink.line, style = pen.stroke)
    drawArc(Color.White.copy(alpha = 0.5f), 205f, 40f, false, Offset(-0.05f * u, -0.31f * u), Size(0.1f * u, 0.08f * u), style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
    // A coin slot, the five buttons and the speaker.
    inkedRound(Rect(p(-0.014f, -0.15f), p(0.014f, -0.142f)), 0.002f * u, Color(0xFF2B2140), pen, shade = false)
    for (k in 0 until 5) {
        val c = p(-0.064f + (k + 0.5f) * 0.0256f, -0.112f)
        val color = CeC.neon[k % 5]
        val pressed = tune == k + 1
        drawCircle(Ink.line, 0.0125f * u, c)
        drawCircle(if (pressed) color else color.darken(0.25f), (if (pressed) 0.0095f else 0.0108f) * u, c)
        if (pressed) {
            fxGlow(c, 0.045f * u, color, 0.65f + 0.2f * sin(t * 6f))
            drawCircle(Color.White, 0.003f * u, Offset(c.x, c.y))
        } else {
            drawCircle(Color.White.copy(alpha = 0.6f), 0.0028f * u, Offset(c.x - 0.003f * u, c.y - 0.003f * u))
        }
    }
    val grille = Rect(p(-0.07f, -0.085f), p(0.07f, -0.035f))
    inkedRound(grille, 0.008f * u, Color(0xFF241A4E), pen, shade = false)
    for (k in 1..8) drawLine(Color(0xFF8B7BFF).copy(alpha = 0.6f), Offset(grille.left + grille.width * k / 9f, grille.top + 0.008f * u), Offset(grille.left + grille.width * k / 9f, grille.bottom - 0.008f * u), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
    // Light and notes while it plays.
    if (playing) {
        fxGlow(p(0f, -0.25f), 0.2f * u, CeC.neon[tune % 5], 0.3f + 0.1f * sin(t * 5f))
        for (k in 0 until 3) {
            val ph = ceFrac(t * 0.7f, 1f, k / 3f)
            fxNote(p(0.06f * sin(ph * 6f + k), -0.36f - 0.16f * ph), 0.012f * u, CeC.neon[(k + tune) % 5], 1f - ph)
        }
    } else {
        // A tiny standby light under the arch blinks.
        drawCircle(CeC.neon[1], 0.0045f * u, p(0.085f, -0.04f), alpha = 0.3f + 0.7f * cePulse(t, 2.2f))
    }
}

// =========================================================================================== karaoke

/** A karaoke screen: coloured lyric bars that fill up one by one with a ball bouncing along them. It wakes up when somebody sings. */
internal fun DrawScope.ceKaraoke(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val on = f.on
    val singing = f.mode == 1
    // A wall bracket and the frame.
    val frame = Rect(p(-0.16f, -0.2f), p(0.16f, -0.04f))
    box3d(frame, 0.035f * u, Color(0xFF2B2140), pen, radius = 0.01f * u)
    val screen = Rect(frame.left + 0.012f * u, frame.top + 0.012f * u, frame.right - 0.012f * u, frame.bottom - 0.02f * u)
    clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(screen, CornerRadius(0.005f * u))) }) {
        if (on) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF5B3FA8), Color(0xFF2F8FD6)), startY = screen.top, endY = screen.bottom), screen.topLeft, screen.size)
            // Three lines of bars, the first two partly filled, the ball hopping over the third.
            val prog = ceFrac(t * (if (singing) 0.5f else 0.28f), 1f)
            val rows = listOf(0.82f to CeC.neon[0], 0.62f to CeC.neon[1], 0.74f to CeC.neon[2])
            for ((i, row) in rows.withIndex()) {
                val y = screen.top + screen.height * (0.4f + i * 0.2f)
                val width = screen.width * 0.82f * row.first
                val x0 = screen.left + (screen.width - width) / 2f
                drawRoundRect(Color.White.copy(alpha = 0.35f), Offset(x0, y), Size(width, screen.height * 0.1f), CornerRadius(screen.height * 0.05f))
                val fill = (prog * 3f - i).coerceIn(0f, 1f)
                if (fill > 0f) drawRoundRect(row.second, Offset(x0, y), Size(width * fill, screen.height * 0.1f), CornerRadius(screen.height * 0.05f))
            }
            val line = (prog * 3f).toInt().coerceIn(0, 2)
            val within = (prog * 3f) - line
            val row = rows[line]
            val width = screen.width * 0.82f * row.first
            val bx = screen.left + (screen.width - width) / 2f + width * within
            val by = screen.top + screen.height * (0.4f + line * 0.2f) - screen.height * 0.1f - abs(sin(within * 3f * PI.toFloat() * 2f)) * screen.height * 0.13f
            drawCircle(Color.White, screen.height * 0.055f, Offset(bx, by))
            drawCircle(Ink.line, screen.height * 0.055f, Offset(bx, by), style = pen.thin)
            if (singing) {
                twinkle(Offset(screen.left + screen.width * 0.15f, screen.top + screen.height * 0.2f), screen.height * 0.1f, Color.White, cePulse(t, 0.6f))
                twinkle(Offset(screen.left + screen.width * 0.85f, screen.top + screen.height * 0.25f), screen.height * 0.08f, CeC.gold, cePulse(t, 0.6f, 0.5f))
            }
        } else {
            drawRect(Color(0xFF16121F), screen.topLeft, screen.size)
            drawLine(Color.White.copy(alpha = 0.12f), Offset(screen.left + screen.width * 0.1f, screen.bottom), Offset(screen.left + screen.width * 0.4f, screen.top), strokeWidth = pen.lw * 4f)
        }
    }
    drawRoundRect(Ink.line, screen.topLeft, screen.size, CornerRadius(0.005f * u), style = pen.stroke)
    if (on) fxGlow(screen.center, 0.22f * u, Color(0xFF8B7BFF), 0.3f)
    // A little speaker bar below and a standby light.
    for (k in 0 until 7) drawLine(Color(0xFF8B7BFF).copy(alpha = 0.6f), Offset(frame.left + 0.05f * u + k * 0.037f * u, frame.bottom - 0.013f * u), Offset(frame.left + 0.05f * u + k * 0.037f * u, frame.bottom - 0.005f * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    drawCircle(if (on) CeC.green else CeC.orange, 0.0045f * u, Offset(frame.right - 0.016f * u, frame.bottom - 0.009f * u), alpha = 0.4f + 0.6f * cePulse(t, 1.7f))
}

// ========================================================================================== snack bar

/** The snack bar: a striped diner counter with a popping popcorn machine, a soda tap, a cupcake dome and a pink neon strip. */
internal fun DrawScope.ceSnackBar(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.14f
    ceShadow(u, 0.46f, d)
    // Machines on the back of the counter top first.
    // Popcorn machine at the left: a glass cabinet with a golden roof and kernels that pop.
    fxBox(u, -0.195f, -0.4f, -0.105f, -0.3f, 0.06f, Color(0xFFFFF4C2), pen, rad = 0.004f, z = 0.05f, top = Color.White, side = Color(0xFFE9D9A0))
    val cab = fxFront(u, -0.195f, -0.4f, -0.105f, -0.3f, 0.05f)
    val glass = Rect(cab.left + 0.01f * u, cab.top + 0.022f * u, cab.right - 0.01f * u, cab.bottom - 0.008f * u)
    drawRect(Color(0xFFBFE6F8).copy(alpha = 0.6f), glass.topLeft, glass.size)
    clipPath(Path().apply { addRect(glass) }) {
        drawRect(Color(0xFFFFF0B8), Offset(glass.left, glass.bottom - glass.height * 0.4f), Size(glass.width, glass.height * 0.4f))
        for (k in 0 until 7) {
            val ph = ceFrac(t * (0.9f + 0.4f * hash01(k, 971)), 1f, hash01(k, 972))
            val cx = glass.left + glass.width * (0.12f + 0.76f * hash01(k, 973))
            val cy = glass.bottom - glass.height * 0.3f - sin(ph * PI.toFloat()) * glass.height * 0.55f
            drawCircle(Color.White, 0.0075f * u, Offset(cx, cy))
            drawCircle(Ink.line.copy(alpha = 0.6f), 0.0075f * u, Offset(cx, cy), style = pen.thin)
        }
    }
    drawRect(Ink.line, glass.topLeft, glass.size, style = pen.thin)
    val roof = Path().apply {
        moveTo(cab.left - 0.008f * u, cab.top + 0.022f * u); lineTo(cab.right + 0.008f * u, cab.top + 0.022f * u)
        lineTo(cab.right - 0.012f * u, cab.top - 0.016f * u); lineTo(cab.left + 0.012f * u, cab.top - 0.016f * u); close()
    }
    inked(roof, CeC.yellow, pen)
    for (k in 0 until 3) drawLine(Color.White.copy(alpha = 0.7f), Offset(cab.left + 0.014f * u + k * 0.026f * u, cab.top + 0.02f * u), Offset(cab.left + 0.02f * u + k * 0.026f * u, cab.top - 0.012f * u), strokeWidth = pen.lw * 2f)
    // Soda tap in the middle: a small tower with two spouts and a cup under it.
    fxBox(u, -0.03f, -0.37f, 0.04f, -0.3f, 0.06f, Color(0xFFD8E2EC), pen, rad = 0.004f, z = 0.05f, top = Color.White, side = Color(0xFF9CA8BC))
    val tower = fxFront(u, -0.03f, -0.37f, 0.04f, -0.3f, 0.05f)
    for ((i, c) in listOf(CeC.orange, CeC.green).withIndex()) {
        drawCircle(c, 0.006f * u, Offset(tower.left + (0.35f + i * 0.3f) * tower.width, tower.top + 0.014f * u))
        drawCircle(Ink.line, 0.006f * u, Offset(tower.left + (0.35f + i * 0.3f) * tower.width, tower.top + 0.014f * u), style = pen.thin)
        drawLine(CeC.steel, Offset(tower.left + (0.35f + i * 0.3f) * tower.width, tower.top + 0.03f * u), Offset(tower.left + (0.35f + i * 0.3f) * tower.width, tower.top + 0.045f * u), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
    }
    // Cupcakes under a glass dome at the right.
    val dome = q(0.15f, -0.3f, 0.07f)
    drawOval(Color(0xFFD8E2EC), Offset(dome.x - 0.045f * u, dome.y - 0.008f * u), Size(0.09f * u, 0.016f * u))
    for ((i, c) in listOf(Color(0xFFFF8FB1), Color.White, Color(0xFFFFE08A)).withIndex()) {
        val cx = dome.x + (i - 1) * 0.026f * u
        inkedRound(Rect(cx - 0.009f * u, dome.y - 0.018f * u, cx + 0.009f * u, dome.y - 0.004f * u), 0.002f * u, Color(0xFFE3B27A), pen, shade = false)
        inkedCircle(Offset(cx, dome.y - 0.024f * u), 0.011f * u, c, pen, shade = false)
        drawCircle(CeC.pink, 0.003f * u, Offset(cx, dome.y - 0.033f * u))
    }
    drawArc(Color(0xFFBFE6F8).copy(alpha = 0.55f), 180f, 180f, true, Offset(dome.x - 0.045f * u, dome.y - 0.06f * u), Size(0.09f * u, 0.1f * u))
    drawArc(Ink.line, 180f, 180f, false, Offset(dome.x - 0.045f * u, dome.y - 0.06f * u), Size(0.09f * u, 0.1f * u), style = pen.thin)
    drawArc(Color.White.copy(alpha = 0.7f), 200f, 40f, false, Offset(dome.x - 0.035f * u, dome.y - 0.05f * u), Size(0.07f * u, 0.08f * u), style = Stroke(pen.lw, cap = StrokeCap.Round))
    // The counter: teal body with a striped front, a white top.
    fxBox(u, -0.23f, -0.3f, 0.23f, 0f, d, Color(0xFF2EC4B6), pen, rad = 0.008f, top = Color.White, side = Color(0xFF1F8F86))
    val front = fxFront(u, -0.23f, -0.3f, 0.23f, 0f)
    clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(front, CornerRadius(0.008f * u))) }) {
        var k = 0
        var x = front.left
        while (x < front.right) {
            if (k % 2 == 0) drawRect(Color(0xFFFF8FB1), Offset(x, front.top + front.height * 0.28f), Size(front.width / 16f, front.height * 0.55f))
            x += front.width / 16f
            k++
        }
        drawRect(Color.White.copy(alpha = 0.3f), Offset(front.left, front.top), Size(front.width, 0.012f * u))
    }
    drawRoundRect(Ink.line, front.topLeft, front.size, CornerRadius(0.008f * u), style = pen.stroke)
    drawLine(Ink.line, Offset(front.left, front.top + front.height * 0.28f), Offset(front.right, front.top + front.height * 0.28f), strokeWidth = pen.lw * 0.8f)
    drawLine(Ink.line, Offset(front.left, front.top + front.height * 0.83f), Offset(front.right, front.top + front.height * 0.83f), strokeWidth = pen.lw * 0.8f)
    // The neon strip along the foot: it glows pink and breathes.
    val glow = 0.45f + 0.25f * cePulse(t, 2.4f)
    fxGlow(p(0f, -0.012f), 0.26f * u, CeC.pink, glow * 0.7f)
    drawLine(Ink.line, p(-0.2f, -0.012f), p(0.2f, -0.012f), strokeWidth = 0.008f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(Color(0xFFFFB3D9), p(-0.2f, -0.012f), p(0.2f, -0.012f), strokeWidth = 0.005f * u, cap = StrokeCap.Round)
}

// ========================================================================================= bar stool

/** A chrome bar stool with a round cushion (pink, or teal for variant 1). */
internal fun DrawScope.ceBarStool(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    ceShadow(u, 0.09f, 0.08f)
    val chrome = Color(0xFFD8E2EC)
    val cushion = if (f.variant == 1) Color(0xFF2EC4B6) else Color(0xFFFF8FB1)
    // The foot disc, the pole, a foot ring.
    fxCyl(0f, -0.004f * u, -0.012f * u, 0.04f * u, 0.036f * u, chrome, pen)
    capsule(p(0f, -0.01f), p(0f, -0.115f), 0.009f * u, chrome, pen)
    val ring = fxDisc2(0f, -0.05f * u, 0.032f * u, 0.028f * u)
    drawPath(ring, chrome, style = Stroke(0.006f * u + pen.lw * 2f))
    drawPath(ring, Ink.line, style = Stroke(0.006f * u + pen.lw * 2f), alpha = 0.0f)
    drawPath(ring, chrome, style = Stroke(0.006f * u))
    // The seat: a fat round cushion.
    fxCyl(0f, -0.108f * u, -0.138f * u, 0.042f * u, 0.042f * u, cushion, pen, top = cushion.lighten(0.3f))
    drawLine(cushion.darken(0.3f), p(-0.04f, -0.123f), p(0.04f, -0.12f), strokeWidth = pen.lw * 0.8f)
    drawCircle(Color.White.copy(alpha = 0.65f), 0.0035f * u, p(-0.02f, -0.148f))
}

// ============================================================================================== neon

/** A neon sign on the wall: a star, a music note or a heart in glowing tube. Off, the tube is grey. */
internal fun DrawScope.ceNeon(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val on = f.on
    val color = CeC.neon[(f.variant * 2) % CeC.neon.size]
    val flicker = if (on) 0.85f + 0.12f * sin(t * 7f + f.id) + (if (ceFrac(t, 5.3f, f.id * 0.13f) < 0.03f) -0.5f else 0f) else 0f
    val shape = Path()
    val c = p(0f, -0.075f)
    when (f.variant) {
        0 -> {
            for (k in 0 until 5) {
                val a = (-90f + k * 144f) * (PI.toFloat() / 180f)
                val x = c.x + cos(a) * 0.055f * u
                val y = c.y + sin(a) * 0.055f * u
                if (k == 0) shape.moveTo(x, y) else shape.lineTo(x, y)
            }
            shape.close()
        }
        1 -> {
            shape.addOval(Rect(c.x - 0.045f * u, c.y + 0.012f * u, c.x - 0.005f * u, c.y + 0.04f * u))
            shape.moveTo(c.x - 0.007f * u, c.y + 0.026f * u); shape.lineTo(c.x - 0.007f * u, c.y - 0.05f * u)
            shape.quadraticTo(c.x + 0.03f * u, c.y - 0.04f * u, c.x + 0.045f * u, c.y - 0.01f * u)
        }
        else -> shape.addPath(fxHeart(c.x, c.y, 0.05f * u))
    }
    // Two small brackets hold the tube to the wall.
    for (s in floatArrayOf(-0.04f, 0.04f)) drawLine(Ink.line, p(s, -0.03f), p(s, -0.01f), strokeWidth = pen.lw * 1.2f)
    if (on) {
        fxGlow(c, 0.16f * u, color, 0.55f * flicker)
        drawPath(shape, color.copy(alpha = 0.35f * flicker), style = Stroke(0.016f * u, cap = StrokeCap.Round))
        drawPath(shape, color, style = Stroke(0.008f * u, cap = StrokeCap.Round))
        drawPath(shape, Color.White.copy(alpha = 0.85f * flicker), style = Stroke(0.0035f * u, cap = StrokeCap.Round))
    } else {
        drawPath(shape, Color(0xFF6B6382), style = Stroke(0.008f * u, cap = StrokeCap.Round))
        drawPath(shape, Ink.line, style = Stroke(0.0015f * u))
    }
}

// ================================================================================= confetti cannon

/** A party cannon on a stand: striped barrel, a fizzing fuse and, just after a shot, a curl of streamers. */
internal fun DrawScope.ceConfetti(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val fired = (f.timer / 1f).coerceIn(0f, 1f)
    ceShadow(u, 0.11f, 0.07f)
    // Two wheels and the carriage.
    for (s in floatArrayOf(-0.03f, 0.03f)) {
        drawCircle(Ink.line, 0.02f * u + pen.lw, p(s, -0.02f))
        drawCircle(CeC.woodDark, 0.02f * u, p(s, -0.02f))
        drawCircle(CeC.woodLight, 0.008f * u, p(s, -0.02f))
        for (k in 0 until 4) {
            val a = k * PI.toFloat() / 4f
            drawLine(CeC.wood, Offset(p(s, -0.02f).x + cos(a) * 0.017f * u, p(s, -0.02f).y + sin(a) * 0.017f * u), Offset(p(s, -0.02f).x - cos(a) * 0.017f * u, p(s, -0.02f).y - sin(a) * 0.017f * u), strokeWidth = pen.lw * 0.8f)
        }
    }
    inkedRound(Rect(p(-0.04f, -0.07f), p(0.04f, -0.05f)), 0.004f * u, CeC.wood, pen)
    // The barrel: a fat cone pointing up and to the right.
    val pivot = p(-0.01f, -0.085f)
    rotate(-38f + (if (fired > 0f) sin(t * 40f) * 2f * fired else 0f), pivot) {
        val barrel = Path().apply {
            moveTo(pivot.x - 0.03f * u, pivot.y - 0.02f * u)
            lineTo(pivot.x + 0.08f * u, pivot.y - 0.035f * u)
            lineTo(pivot.x + 0.08f * u, pivot.y + 0.035f * u)
            lineTo(pivot.x - 0.03f * u, pivot.y + 0.02f * u)
            close()
        }
        inked(barrel, CeC.pink, pen)
        clipPath(barrel) {
            for (k in 0 until 3) drawRect(Color.White.copy(alpha = 0.85f), Offset(pivot.x - 0.01f * u + k * 0.034f * u, pivot.y - 0.05f * u), Size(0.014f * u, 0.1f * u))
        }
        drawPath(barrel, Ink.line, style = pen.stroke)
        val mouth = Rect(pivot.x + 0.072f * u, pivot.y - 0.04f * u, pivot.x + 0.092f * u, pivot.y + 0.04f * u)
        inkedRound(mouth, 0.008f * u, CeC.yellow, pen, shade = false)
        drawOval(Color(0xFF3A1E4A), Offset(mouth.left + 0.004f * u, mouth.top + 0.01f * u), Size(0.012f * u, 0.06f * u))
        if (fired > 0f) {
            // Streamers curl out of the mouth and a puff of smoke.
            for (k in 0 until 5) {
                val col = CeC.neon[k % 5]
                val len = (0.04f + 0.05f * k % 3 * 0.5f) * fired
                val s = Path().apply {
                    moveTo(mouth.right, mouth.center.y)
                    cubicTo(mouth.right + 0.03f * u, mouth.center.y - (0.03f + k * 0.01f) * u * fired, mouth.right + 0.05f * u, mouth.center.y + (0.02f - k * 0.01f) * u, mouth.right + len * u, mouth.center.y - 0.02f * u * (k - 2))
                }
                drawPath(s, col, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
            }
            fxPuffs(mouth.right, mouth.center.y, t, 0.016f * u, 0.07f * u, Color(0xFFEDE8F5), 0.8f * fired, 3, 1.4f, 0.03f * u)
        }
    }
    // The fuse at the back, fizzing.
    val back = p(-0.045f, -0.075f)
    drawLine(Ink.line, back, p(-0.06f, -0.065f), strokeWidth = pen.lw * 1.2f)
    twinkle(p(-0.062f, -0.067f), 0.01f * u * (0.8f + 0.5f * sin(t * 11f)), CeC.gold, 1f)
    drawCircle(Color.White, 0.0025f * u, p(-0.062f, -0.067f))
}
