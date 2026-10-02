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
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * The way down and the way out: the cellar stairs from the hall, the secret tunnel door (five locks that light
 * up as the golden keys are found), the mine cart and the mine door on Trollhola's side.
 */

// ====================================================================================== the cellar stairs

/** A wooden flight of stairs going down to the right from a lit door in the hall above. */
internal fun DrawScope.ceStairs(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.2f
    val steps = 9
    val run = 0.78f / steps
    val rise = 0.47f / steps
    ceShadow(u, 0.84f, d)
    // The door at the top, left ajar: warm light spills down the steps.
    val door = Rect(p(-0.42f, -0.78f), p(-0.27f, -0.5f))
    fxBox(u, -0.43f, -0.8f, -0.26f, -0.5f, 0.04f, Color(0xFFE9E1D3), pen, rad = 0.005f, z = d - 0.02f)
    val leaf = Rect(p(-0.415f, -0.775f), p(-0.275f, -0.5f))
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFF0B8), Color(0xFFFFD27A)), startY = leaf.top, endY = leaf.bottom), leaf.topLeft, leaf.size)
    drawRect(Ink.line, leaf.topLeft, leaf.size, style = pen.thin)
    val beam = Path().apply {
        moveTo(p(-0.41f, -0.5f).x, p(-0.41f, -0.5f).y); lineTo(p(-0.28f, -0.5f).x, p(-0.28f, -0.5f).y)
        lineTo(p(0.25f, -0.06f).x, p(0.25f, -0.06f).y); lineTo(p(-0.12f, -0.06f).x, p(-0.12f, -0.06f).y); close()
    }
    drawPath(beam, Brush.linearGradient(listOf(Color(0xFFFFF0B8).copy(alpha = 0.35f), Color(0xFFFFF0B8).copy(alpha = 0f)), p(-0.34f, -0.5f), p(0.1f, -0.06f)))
    // Dust motes drift in the light.
    for (k in 0 until 7) {
        val ph = ceFrac(t * 0.15f, 1f, hash01(k, 921))
        val x = -0.36f + 0.5f * ph + sin(t + k) * 0.01f
        val y = -0.5f + 0.4f * ph * (0.6f + 0.4f * hash01(k, 922))
        drawCircle(Color.White, 0.003f * u, p(x, y), alpha = 0.6f * (1f - ph))
    }
    // The steps, high end on the left. Each one is a block: its profile faces us, its tread is the top.
    val wood = CeC.wood
    for (k in 0 until steps) {
        val l = -0.4f + k * run
        val top = -0.5f + k * rise
        fxBox(u, l, top, l + run, 0f, d, if (k % 2 == 0) wood else wood.darken(0.06f), pen, rad = 0.003f, top = CeC.woodLight, side = CeC.woodDark)
        // A worn edge along the tread, and grain on the profile.
        val a = q(l, top, 0f)
        val b = q(l + run, top, 0f)
        drawLine(Color.White.copy(alpha = 0.35f), Offset(a.x, a.y + pen.lw), Offset(b.x, b.y + pen.lw), strokeWidth = pen.lw * 0.8f)
        if (k % 3 == 1) drawLine(CeC.woodDark.copy(alpha = 0.55f), p(l + run * 0.2f, top + (0f - top) * 0.4f), p(l + run * 0.8f, top + (0f - top) * 0.4f + 0.004f), strokeWidth = pen.lw * 0.6f)
    }
    // A hand rail on posts along the near edge, rising to the left.
    val railFrom = p(-0.4f + 0.02f, -0.5f - 0.19f)
    val railTo = p(0.37f, -0.19f + 0.0f)
    for (k in 0..3) {
        val f0 = k / 3f
        val x = -0.38f + f0 * 0.74f
        val topY = -0.69f + f0 * 0.5f
        val stepIndex = ((x + 0.4f) / run).toInt().coerceIn(0, steps - 1)
        val floorY = -0.5f + stepIndex * rise
        capsule(p(x, floorY), p(x, topY), 0.006f * u, CeC.woodDark, pen)
    }
    drawLine(Ink.line, railFrom, railTo, strokeWidth = 0.014f * u + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(CeC.woodLight, railFrom, railTo, strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.5f), Offset(railFrom.x, railFrom.y - 0.004f * u), Offset(railTo.x, railTo.y - 0.004f * u), strokeWidth = pen.lw)
    // A small lantern hangs at the bottom of the rail; a mat lies at the foot of the stairs.
    val lan = p(0.36f, -0.215f)
    fxGlow(lan, 0.08f * u, CeC.gold, 0.4f + 0.15f * cePulse(t, 1.6f))
    inkedRound(Rect(lan.x - 0.008f * u, lan.y, lan.x + 0.008f * u, lan.y + 0.022f * u), 0.003f * u, Color(0xFFFFE9A8), pen, shade = false)
    inkedRound(Rect(p(0.2f, -0.012f), p(0.4f, 0.004f)), 0.003f * u, CeC.teal, pen, shade = false)
}

// ======================================================================================= the tunnel door

/**
 * The secret door at the end of the party room: a stone arch with timber posts. Five round locks over the
 * door light up gold, one for each key found ([Fixture.count]); with all five ([Fixture.on]) the door glows
 * and, while the cart sets off ([Fixture.mode]), swings open on the dark tunnel with its rails.
 */
internal fun DrawScope.ceTunnelDoor(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val open = f.mode == 1
    val unlocked = f.on
    val keys = f.count.coerceIn(0, 5)
    val d = 0.1f
    val opening = Path().apply {
        moveTo(p(-0.105f, 0f).x, p(-0.105f, 0f).y)
        lineTo(p(-0.105f, -0.31f).x, p(-0.105f, -0.31f).y)
        quadraticTo(p(-0.105f, -0.4f).x, p(-0.105f, -0.4f).y, p(0f, -0.4f).x, p(0f, -0.4f).y)
        quadraticTo(p(0.105f, -0.4f).x, p(0.105f, -0.4f).y, p(0.105f, -0.31f).x, p(0.105f, -0.31f).y)
        lineTo(p(0.105f, 0f).x, p(0.105f, 0f).y)
        close()
    }
    // The tunnel behind: dark, with timber ribs running back into it and the two rails coming out.
    drawPath(opening, Color(0xFF14101E))
    clipPath(opening) {
        val glow = if (open) 0.9f else if (unlocked) 0.35f else 0.12f
        val c = p(0f, -0.15f)
        drawCircle(safeRadialGradient(listOf(CeC.orange.copy(alpha = 0.55f * glow), Color(0x00FF8A2E)), c, 0.12f * u), 0.12f * u, c)
        for (k in 0 until 4) {
            val s = 1f - k * 0.2f
            val ry = -0.38f * s * 0.9f
            drawLine(CeC.woodDark.copy(alpha = 0.55f - k * 0.1f), p(-0.1f * s, ry + 0.2f * (1f - s)), p(0.1f * s, ry + 0.2f * (1f - s)), strokeWidth = pen.lw * (2.2f - k * 0.4f))
            drawLine(CeC.woodDark.copy(alpha = 0.55f - k * 0.1f), p(-0.1f * s, ry + 0.2f * (1f - s)), p(-0.1f * s, 0f - 0.1f * (1f - s)), strokeWidth = pen.lw * (2f - k * 0.4f))
            drawLine(CeC.woodDark.copy(alpha = 0.55f - k * 0.1f), p(0.1f * s, ry + 0.2f * (1f - s)), p(0.1f * s, 0f - 0.1f * (1f - s)), strokeWidth = pen.lw * (2f - k * 0.4f))
        }
        for (side in floatArrayOf(-0.04f, 0.04f)) drawLine(CeC.steel, p(side * 0.45f, -0.06f), p(side, 0f), strokeWidth = pen.lw * 1.4f)
        // Two glints of eyes? No: a far lantern swings in the dark.
        val lx = sin(t * 1.3f) * 0.006f
        fxGlow(p(lx, -0.17f), 0.03f * u, CeC.gold, 0.5f * glow)
    }
    drawPath(opening, Ink.line, style = pen.stroke)
    // The door: arched planks with iron straps, or swung open to both sides.
    if (!open) {
        val door = Path().apply {
            moveTo(p(-0.1f, 0f).x, p(-0.1f, 0f).y)
            lineTo(p(-0.1f, -0.31f).x, p(-0.1f, -0.31f).y)
            quadraticTo(p(-0.1f, -0.385f).x, p(-0.1f, -0.385f).y, p(0f, -0.385f).x, p(0f, -0.385f).y)
            quadraticTo(p(0.1f, -0.385f).x, p(0.1f, -0.385f).y, p(0.1f, -0.31f).x, p(0.1f, -0.31f).y)
            lineTo(p(0.1f, 0f).x, p(0.1f, 0f).y)
            close()
        }
        inked(door, Color(0xFF8A5A34), pen)
        clipPath(door) {
            for (k in 1..5) drawLine(CeC.woodDark.darken(0.2f), p(-0.1f + k * 0.0333f, -0.39f), p(-0.1f + k * 0.0333f, 0f), strokeWidth = pen.lw * 0.7f)
            for (y in floatArrayOf(-0.07f, -0.2f, -0.32f)) {
                drawRect(CeC.iron, p(-0.105f, y - 0.011f), Size(0.21f * u, 0.022f * u))
                drawLine(Color.White.copy(alpha = 0.25f), p(-0.1f, y - 0.008f), p(0.1f, y - 0.008f), strokeWidth = pen.lw * 0.6f)
                for (k in 0 until 5) drawCircle(CeC.steel, 0.0035f * u, p(-0.08f + k * 0.04f, y))
            }
        }
        // A ring handle.
        drawCircle(Ink.line, 0.012f * u, p(0.06f, -0.15f), style = Stroke(pen.lw * 2.6f))
        drawCircle(CeC.brass, 0.012f * u, p(0.06f, -0.15f), style = Stroke(pen.lw * 1.4f))
        if (unlocked) {
            // The edges glow gold and a few sparks wander.
            drawPath(door, CeC.gold.copy(alpha = 0.35f + 0.3f * cePulse(t, 1.2f)), style = Stroke(pen.lw * 3f))
            for (k in 0 until 3) {
                val ph = ceFrac(t, 1.8f, k / 3f)
                twinkle(p(-0.07f + 0.07f * k, -0.05f - 0.25f * ph), 0.01f * u, CeC.gold, 1f - ph)
            }
        }
    } else {
        for (s in floatArrayOf(-1f, 1f)) {
            val hx = s * 0.105f
            val leaf = fxQuad(hx * u, -0.385f * u, (hx + s * 0.06f) * u, -0.36f * u, (hx + s * 0.06f) * u, 0.02f * u, hx * u, 0f, 0.003f * u)
            fxFace(leaf, Color(0xFF8A5A34), pen)
            for (y in floatArrayOf(-0.07f, -0.2f, -0.32f)) drawLine(CeC.iron, Offset(hx * u, y * u), Offset((hx + s * 0.06f) * u, (y + 0.02f) * u), strokeWidth = pen.lw * 2.2f)
        }
    }
    // The frame: two timber posts, a beam over the arch and a keystone.
    fxBox(u, -0.15f, -0.43f, -0.105f, 0f, d, CeC.woodDark, pen, rad = 0.004f, z = 0f, top = CeC.wood, side = CeC.woodDark.darken(0.3f))
    fxBox(u, 0.105f, -0.43f, 0.15f, 0f, d, CeC.woodDark, pen, rad = 0.004f, z = 0f, top = CeC.wood, side = CeC.woodDark.darken(0.3f))
    fxBox(u, -0.16f, -0.46f, 0.16f, -0.41f, d, CeC.wood, pen, rad = 0.004f, z = 0f, top = CeC.woodLight, side = CeC.woodDark.darken(0.2f))
    for (k in 0 until 3) {
        val y = -0.1f - k * 0.1f
        fxNail(p(-0.1275f, y), 0.0035f * u)
        fxNail(p(0.1275f, y), 0.0035f * u)
    }
    // Five lock sockets in the beam: gold for each key found, grey for the rest.
    for (k in 0 until 5) {
        val c = p(-0.1f + k * 0.05f, -0.435f)
        val found = k < keys
        drawCircle(Ink.line, 0.0125f * u, c)
        drawCircle(if (found) CeC.gold else Color(0xFF6B6382), 0.0105f * u, c)
        if (found) {
            drawCircle(Color.White.copy(alpha = 0.7f), 0.0035f * u, Offset(c.x - 0.004f * u, c.y - 0.004f * u))
            fxGlow(c, 0.03f * u, CeC.gold, if (unlocked) 0.5f + 0.3f * cePulse(t, 1.1f, k * 0.15f) else 0.25f)
        } else {
            drawRect(Ink.line, Offset(c.x - 0.0012f * u, c.y - 0.004f * u), Size(0.0024f * u, 0.008f * u))
            drawCircle(Ink.line, 0.0025f * u, Offset(c.x, c.y - 0.003f * u))
        }
    }
    // A lantern on a bracket by the door, flickering.
    val lan = p(-0.185f, -0.33f)
    val flick = 0.75f + 0.2f * sin(t * 9f) + 0.1f * sin(t * 15f + 1f)
    capsule(p(-0.15f, -0.36f), p(-0.185f, -0.36f), 0.005f * u, CeC.iron, pen)
    fxGlow(lan, 0.12f * u, CeC.gold, 0.45f * flick)
    inkedRound(Rect(lan.x - 0.012f * u, lan.y - 0.02f * u, lan.x + 0.012f * u, lan.y + 0.02f * u), 0.005f * u, Color(0xFFFFE9A8), pen, shade = false)
    drawRect(Ink.line, Offset(lan.x - 0.014f * u, lan.y - 0.022f * u), Size(0.028f * u, 0.004f * u))
    fxFire(lan.x, lan.y + 0.016f * u, 0.014f * u, 0.022f * u, t, 2f, pen, false)
}

// ======================================================================================== the mine cart

private val cartYellow = Color(0xFFF0A93A)

internal fun DrawScope.ceMineCart(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val running = f.timer > 0.9f
    val d = 0.12f
    ceShadow(u, 0.28f, d, 0.8f)
    translate(0f, f.bob * u) {
        // The rear wheels peek out behind.
        for (s in floatArrayOf(-1f, 1f)) ceWheel(p(s * 0.085f, -0.03f), 0.029f * u, f.angle, pen, u, z = d * 0.55f)
        // The rear wall and the dark inside of the bucket.
        val rear = fxQuad(
            fxQ(u, -0.14f, -0.125f, d).x, fxQ(u, -0.14f, -0.125f, d).y, fxQ(u, 0.14f, -0.125f, d).x, fxQ(u, 0.14f, -0.125f, d).y,
            fxQ(u, 0.115f, -0.04f, d).x, fxQ(u, 0.115f, -0.04f, d).y, fxQ(u, -0.115f, -0.04f, d).x, fxQ(u, -0.115f, -0.04f, d).y, 0.008f * u,
        )
        fxFace(rear, cartYellow.darken(0.25f), pen)
        // The open top seen from above: dark inside.
        val topFace = fxQuad(
            fxQ(u, -0.15f, -0.125f, 0f).x, fxQ(u, -0.15f, -0.125f, 0f).y, fxQ(u, -0.15f, -0.125f, d).x, fxQ(u, -0.15f, -0.125f, d).y,
            fxQ(u, 0.15f, -0.125f, d).x, fxQ(u, 0.15f, -0.125f, d).y, fxQ(u, 0.15f, -0.125f, 0f).x, fxQ(u, 0.15f, -0.125f, 0f).y, 0.006f * u,
        )
        fxFace(topFace, Color(0xFF3A2A1E), pen)
        // A little pile of shiny stones inside.
        for (k in 0 until 4) {
            val c = fxQ(u, -0.08f + k * 0.055f, -0.126f, 0.03f + (k % 2) * 0.04f)
            inkedCircle(c, 0.011f * u, listOf(Color(0xFF6FF2FF), Color(0xFFFF7BD8), Color(0xFF7CFFB2), Color(0xFFB58CFF))[k], pen, shade = false)
            drawCircle(Color.White.copy(alpha = 0.7f), 0.003f * u, Offset(c.x - 0.003f * u, c.y - 0.003f * u))
        }
        // The right side of the bucket, running back.
        val side = fxQuad(
            fxQ(u, 0.15f, -0.125f, 0f).x, fxQ(u, 0.15f, -0.125f, 0f).y, fxQ(u, 0.15f, -0.125f, d).x, fxQ(u, 0.15f, -0.125f, d).y,
            fxQ(u, 0.12f, -0.035f, d).x, fxQ(u, 0.12f, -0.035f, d).y, fxQ(u, 0.12f, -0.035f, 0f).x, fxQ(u, 0.12f, -0.035f, 0f).y,
        )
        fxFace(side, cartYellow.darken(0.18f), pen)
        // The coupling at the back of the cart.
        capsule(p(-0.14f, -0.05f), p(-0.18f, -0.045f), 0.006f * u, CeC.iron, pen)
    }
    if (running) {
        // Sparks fly from the wheels.
        for (k in 0 until 4) {
            val ph = ceFrac(t * 3f, 1f, k / 4f)
            drawCircle(CeC.gold, 0.003f * u * (1f - ph), p(-0.1f - 0.05f * ph + 0.2f * (k % 2), -0.005f - 0.04f * ph), alpha = 1f - ph)
        }
    }
}

/** The front wall, rim, wheels, lantern and bell of the cart: what stands in front of whoever sits in it. */
internal fun DrawScope.ceMineCartFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    translate(0f, f.bob * u) {
        val wall = fxQuad(-0.15f * u, -0.125f * u, 0.15f * u, -0.125f * u, 0.12f * u, -0.036f * u, -0.12f * u, -0.036f * u, 0.01f * u)
        fxFace(wall, cartYellow, pen)
        clipPath(wall) {
            // Iron bands and rivets, and a big stencilled gold star.
            for (y in floatArrayOf(-0.1f, -0.06f)) {
                drawRect(CeC.iron.copy(alpha = 0.85f), p(-0.16f, y - 0.007f), Size(0.32f * u, 0.014f * u))
                for (k in 0 until 7) drawCircle(CeC.steel, 0.0032f * u, p(-0.125f + k * 0.042f, y))
            }
            drawRect(Color.White.copy(alpha = 0.3f), p(-0.15f, -0.122f), Size(0.07f * u, 0.09f * u))
        }
        drawPath(starPath(p(0f, -0.081f), 0.017f * u, 0.0075f * u), CeC.gold)
        drawPath(starPath(p(0f, -0.081f), 0.017f * u, 0.0075f * u), Ink.line, style = pen.thin)
        // The rim.
        drawLine(Ink.line, p(-0.155f, -0.125f), p(0.155f, -0.125f), strokeWidth = 0.011f * u + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(cartYellow.lighten(0.3f), p(-0.155f, -0.125f), p(0.155f, -0.125f), strokeWidth = 0.011f * u, cap = StrokeCap.Round)
        // The front wheels.
        for (s in floatArrayOf(-1f, 1f)) ceWheel(p(s * 0.085f, -0.03f), 0.031f * u, f.angle, pen, u, z = 0f)
        // A bell on the front, and a lantern on a pole.
        capsule(p(0.15f, -0.07f), p(0.185f, -0.075f), 0.005f * u, CeC.iron, pen)
        val bell = p(0.19f, -0.065f)
        val ring = if (f.anim > 0.05f) sin(t * 40f) * 6f else 0f
        rotate(ring, p(0.185f, -0.075f)) {
            val b = Path().apply {
                moveTo(bell.x - 0.011f * u, bell.y + 0.012f * u)
                quadraticTo(bell.x - 0.01f * u, bell.y - 0.012f * u, bell.x, bell.y - 0.013f * u)
                quadraticTo(bell.x + 0.01f * u, bell.y - 0.012f * u, bell.x + 0.011f * u, bell.y + 0.012f * u)
                close()
            }
            inked(b, CeC.brass, pen)
            drawCircle(Ink.line, 0.003f * u, Offset(bell.x, bell.y + 0.015f * u))
        }
        val lan = p(-0.17f, -0.17f)
        capsule(p(-0.15f, -0.125f), p(-0.17f, -0.15f), 0.004f * u, CeC.iron, pen)
        val flick = 0.8f + 0.15f * sin(t * 8f)
        fxGlow(lan, 0.09f * u, CeC.gold, 0.5f * flick)
        inkedRound(Rect(lan.x - 0.008f * u, lan.y - 0.015f * u, lan.x + 0.008f * u, lan.y + 0.012f * u), 0.003f * u, Color(0xFFFFE9A8), pen, shade = false)
    }
}

/** A cart wheel with spokes that turn with [turn] (radians). */
private fun DrawScope.ceWheel(c0: Offset, r: Float, turn: Float, pen: Pen, u: Float, z: Float) {
    val c = Offset(c0.x + FX_DX * z * u, c0.y + FX_DY * z * u)
    drawCircle(Ink.line, r + pen.lw, c)
    drawCircle(CeC.iron, r, c)
    drawCircle(CeC.ironLight, r * 0.72f, c)
    for (k in 0 until 6) {
        val a = turn + k * PI.toFloat() / 3f
        drawLine(CeC.steel, c, Offset(c.x + cos(a) * r * 0.7f, c.y + sin(a) * r * 0.7f), strokeWidth = pen.lw * 1.3f, cap = StrokeCap.Round)
    }
    drawCircle(CeC.brass, r * 0.22f, c)
    drawCircle(Ink.line, r * 0.22f, c, style = pen.thin)
}

// ===================================================================================== Trollhola's door

/** The other end of the tunnel, in the crystal cave: a timber-framed mine mouth with rails, a lantern and a pickaxe sign. */
internal fun DrawScope.ceLabDoor(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val d = 0.1f
    ceShadow(u, 0.3f, d, 0.8f)
    val opening = Path().apply {
        moveTo(p(-0.105f, 0f).x, p(-0.105f, 0f).y)
        lineTo(p(-0.105f, -0.3f).x, p(-0.105f, -0.3f).y)
        quadraticTo(p(-0.105f, -0.39f).x, p(-0.105f, -0.39f).y, p(0f, -0.39f).x, p(0f, -0.39f).y)
        quadraticTo(p(0.105f, -0.39f).x, p(0.105f, -0.39f).y, p(0.105f, -0.3f).x, p(0.105f, -0.3f).y)
        lineTo(p(0.105f, 0f).x, p(0.105f, 0f).y)
        close()
    }
    drawPath(opening, Color(0xFF120D22))
    clipPath(opening) {
        val c = p(0f, -0.14f)
        drawCircle(safeRadialGradient(listOf(CeC.orange.copy(alpha = 0.5f), Color(0x00FF8A2E)), c, 0.13f * u), 0.13f * u, c)
        for (k in 0 until 4) {
            val s = 1f - k * 0.22f
            drawLine(CeC.woodDark.copy(alpha = 0.6f - k * 0.1f), p(-0.1f * s, -0.36f * s), p(0.1f * s, -0.36f * s), strokeWidth = pen.lw * (2.2f - k * 0.4f))
        }
        for (side in floatArrayOf(-0.045f, 0.045f)) drawLine(CeC.steel, p(side * 0.45f, -0.06f), p(side, 0f), strokeWidth = pen.lw * 1.4f)
        fxGlow(p(sin(t * 1.2f) * 0.005f, -0.16f), 0.03f * u, CeC.gold, 0.5f)
    }
    drawPath(opening, Ink.line, style = pen.stroke)
    // Rough log posts and a beam.
    for (s in floatArrayOf(-1f, 1f)) {
        val x = s * 0.128f
        val post = Rect(p(x - 0.024f, -0.42f), p(x + 0.024f, 0f))
        inkedRound(post, 0.01f * u, CeC.woodDark, pen)
        for (k in 0 until 4) drawLine(CeC.woodDark.darken(0.35f), p(x - 0.016f, -0.38f + k * 0.1f), p(x + 0.012f, -0.37f + k * 0.1f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
    }
    val beam = Rect(p(-0.16f, -0.45f), p(0.16f, -0.405f))
    inkedRound(beam, 0.012f * u, CeC.wood, pen)
    drawLine(CeC.woodDark, p(-0.14f, -0.43f), p(0.14f, -0.427f), strokeWidth = pen.lw * 0.7f)
    // A picture of Storhuset shows where the secret tunnel leads, without reading.
    for (s in floatArrayOf(-0.03f, 0.03f)) drawLine(Ink.line, p(s, -0.405f), p(s, -0.385f), strokeWidth = pen.lw)
    val sign = Rect(p(-0.085f, -0.385f), p(0.085f, -0.30f))
    inkedRound(sign, 0.005f * u, CeC.woodLight, pen)
    inkedRound(Rect(p(-0.037f, -0.354f), p(0.045f, -0.31f)), 0.003f * u, Color(0xFFEBD196), pen, shade = false)
    val roof = Path().apply { moveTo(p(-0.047f, -0.352f).x, p(-0.047f, -0.352f).y); lineTo(p(0.004f, -0.378f).x, p(0.004f, -0.378f).y); lineTo(p(0.055f, -0.352f).x, p(0.055f, -0.352f).y); close() }
    inked(roof, Color(0xFF85566B), pen, shade = false)
    for (x in floatArrayOf(-0.02f, 0.025f)) inkedRound(Rect(p(x - 0.006f, -0.343f), p(x + 0.006f, -0.326f)), 0.002f * u, Color(0xFF79BCCC), pen, shade = false)
    drawLine(CeC.iron, p(-0.074f, -0.326f), p(-0.048f, -0.326f), strokeWidth = pen.lw, cap = StrokeCap.Round)
    drawLine(CeC.iron, p(-0.058f, -0.334f), p(-0.048f, -0.326f), strokeWidth = pen.lw, cap = StrokeCap.Round)
    drawLine(CeC.iron, p(-0.058f, -0.318f), p(-0.048f, -0.326f), strokeWidth = pen.lw, cap = StrokeCap.Round)
    // A crystal glints on each side.
    for ((i, s) in floatArrayOf(-1f, 1f).withIndex()) {
        val cx = s * 0.17f
        val col = if (i == 0) Color(0xFF6FF2FF) else Color(0xFFFF7BD8)
        val cr = Path().apply { moveTo(p(cx, 0f).x, p(cx, 0f).y); lineTo(p(cx - 0.014f, -0.04f).x, p(cx - 0.014f, -0.04f).y); lineTo(p(cx + 0.002f, -0.085f).x, p(cx + 0.002f, -0.085f).y); lineTo(p(cx + 0.018f, -0.035f).x, p(cx + 0.018f, -0.035f).y); close() }
        inked(cr, col, pen)
        twinkle(p(cx + 0.004f, -0.06f), 0.012f * u, Color.White, cePulse(t, 1.7f, i * 0.4f))
    }
    // Rails come out toward us, with a lantern hanging by the post.
    val lan = p(-0.185f, -0.28f)
    fxGlow(lan, 0.11f * u, CeC.gold, 0.4f + 0.12f * sin(t * 8f))
    inkedRound(Rect(lan.x - 0.01f * u, lan.y - 0.016f * u, lan.x + 0.01f * u, lan.y + 0.016f * u), 0.004f * u, Color(0xFFFFE9A8), pen, shade = false)
    capsule(p(-0.152f, -0.3f), lan.let { Offset(it.x / u, it.y / u).let { v -> p(v.x, v.y - 0.016f) } }, 0.004f * u, CeC.iron, pen)
}
