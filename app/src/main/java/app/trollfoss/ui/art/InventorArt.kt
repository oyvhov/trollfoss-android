package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.InventorPlay
import kotlin.math.cos
import kotlin.math.sin

private val Wood = Color(0xFFDEA66C)
private val DarkWood = Color(0xFFB07E48)
private val Steel = Color(0xFFB9C2D3)
private val Dark = Color(0xFF564768)
private val Teal = Color(0xFF45C2B0)
private val Cream = Color(0xFFF5EEDC)
private val Flame = Color(0xFFFF9F43)
private val FlameCore = Color(0xFFFFE066)

/**
 * Level 9 inventor toys, drawn from the bottom centre in scene units × [u]: robot workshop, helper robot, rocket kit
 * and reaction course. Each reads only what the fixture already knows (`anim`, `on`, `angle`), so nothing here is saved.
 */
fun DrawScope.drawInventorBack(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in InventorPlay.TYPES) return false
    fun box(l: Float, t: Float, r: Float, b: Float, c: Color) = inkedRound(Rect(l * u, t * u, r * u, b * u), u * 0.012f, c, pen)
    fun dot(x: Float, y: Float, r: Float, c: Color) = inkedCircle(Offset(x * u, y * u), r * u, c, pen)
    fun line(x: Float, y: Float, xx: Float, yy: Float, c: Color = Ink.line, w: Float = pen.lw) = drawLine(c, Offset(x * u, y * u), Offset(xx * u, yy * u), w, StrokeCap.Round)
    when (f.type) {
        FixtureType.PLAY_ROBOT_WORKSHOP -> {
            // A workbench with a half-built robot, a turning gear and a row of blinking lights.
            val busy = f.anim > 0.05f
            box(-0.23f, -0.1f, -0.18f, 0f, DarkWood); box(0.18f, -0.1f, 0.23f, 0f, DarkWood)
            box(-0.26f, -0.15f, 0.26f, -0.09f, Wood)
            box(-0.2f, -0.23f, -0.09f, -0.15f, Teal)
            box(-0.19f, -0.3f, -0.1f, -0.23f, Steel)
            dot(-0.16f, -0.265f, 0.014f, Color.White); dot(-0.125f, -0.265f, 0.014f, Color.White)
            drawCircle(Ink.line, 0.006f * u, Offset(-0.16f * u, -0.265f * u)); drawCircle(Ink.line, 0.006f * u, Offset(-0.125f * u, -0.265f * u))
            line(-0.145f, -0.3f, -0.145f, -0.335f); dot(-0.145f, -0.34f, 0.013f, if (busy && (pen.t * 6f).toInt() % 2 == 0) Color(0xFFFF4D6D) else Color(0xFFFFC0CB))
            gear(0.14f, -0.2f, 0.045f, pen.t * (if (busy) 4f else 0.3f), Flame, u, pen)
            gear(0.215f, -0.235f, 0.028f, -pen.t * (if (busy) 6f else 0.45f), Steel, u, pen)
            for (i in 0 until 3) dot(-0.02f + i * 0.045f, -0.12f, 0.01f, if (busy && ((pen.t * 4f).toInt() + i) % 3 == 0) FlameCore else ToyColors[(i + 1) % ToyColors.size])
            if (f.anim > 0.5f) for (k in 0 until 6) {
                val a = pen.t * 9f + k * 1.05f
                dot(0.04f + cos(a) * 0.17f, -0.2f + sin(a * 1.3f) * 0.1f, 0.007f, FlameCore)
            }
        }
        FixtureType.PLAY_HELPER_ROBOT -> {
            // A friendly wheeled robot with a tray out to the right; its bulb blinks while it works.
            val working = f.on
            val roll = f.shiftX * 14f
            line(0f, -0.26f, 0f, -0.3f); dot(0f, -0.31f, 0.017f, if (working && (pen.t * 8f).toInt() % 2 == 0) Color(0xFFFF4D6D) else Color(0xFFFFB7C5))
            box(-0.07f, -0.27f, 0.06f, -0.19f, Steel)
            dot(-0.032f, -0.235f, 0.017f, Color.White); dot(0.022f, -0.235f, 0.017f, Color.White)
            drawCircle(Ink.line, 0.007f * u, Offset((-0.032f + if (working) 0.004f else 0f) * u, -0.235f * u)); drawCircle(Ink.line, 0.007f * u, Offset((0.022f + if (working) 0.004f else 0f) * u, -0.235f * u))
            box(-0.08f, -0.19f, 0.07f, -0.07f, Teal)
            drawCircle(Color.White.copy(alpha = 0.7f), 0.012f * u, Offset(-0.005f * u, -0.14f * u))
            // The tray arm and tray: whatever it carries rides on it.
            line(0.07f, -0.14f, 0.1f, -0.2f, Ink.line, pen.lw * 2.2f)
            box(0.03f, -0.225f, 0.14f, -0.205f, Wood)
            for (wx in listOf(-0.04f, 0.04f)) {
                dot(wx, -0.04f, 0.04f, Dark)
                val a = roll
                line(wx, -0.04f, wx + cos(a) * 0.03f, -0.04f + sin(a) * 0.03f, Steel, pen.lw * 1.4f)
            }
        }
        FixtureType.PLAY_ROCKET_KIT -> {
            // The launch pad and gantry stay; the rocket lifts away from them.
            box(-0.16f, -0.03f, 0.16f, 0f, Dark)
            line(-0.14f, -0.03f, -0.14f, -0.38f, Steel, pen.lw * 2.4f); line(-0.1f, -0.03f, -0.1f, -0.38f, Steel, pen.lw * 2.4f)
            for (k in 0 until 5) line(-0.14f, -0.06f - k * 0.065f, -0.1f, -0.095f - k * 0.065f, Steel, pen.lw * 1.2f)
            translate(0f, -f.angle * InventorPlay.RISE * u) {
                if (f.on) rocketFlame(f, u, pen)
                rocketHull(u, pen)
            }
        }
        FixtureType.PLAY_REACTION_COURSE -> {
            // A flat board with four big pads; the lit one is the one to tap, and a row of dots counts the rights.
            val top = -0.018f; val z0 = -0.01f; val z1 = 0.15f
            val a = fxQ(u, -0.32f, top, z0); val b = fxQ(u, 0.32f, top, z0)
            fxFace(fxQuad(a.x, a.y, b.x, b.y, b.x, 0f, a.x, 0f), Dark.darken(0.25f), pen)
            val face = fxFlat(u, -0.32f, 0.32f, top, z0, z1, 0.012f)
            drawPath(face, Dark)
            val lit = f.angle.toInt() - 1
            for (i in 0 until 4) {
                val x0 = -0.31f + i * InventorPlay.PAD
                val c = ToyColors[i]
                val on = i == lit
                val path = fxFlat(u, x0, x0 + InventorPlay.PAD - 0.014f, top, z0 + 0.03f, z1 - 0.025f, 0.008f)
                drawPath(path, if (on) c else c.copy(alpha = 0.58f))
                if (on) {
                    drawPath(fxFlat(u, x0 + 0.02f, x0 + 0.07f, top, z0 + 0.06f, z1 - 0.045f, 0.004f), Color.White.copy(alpha = 0.75f))
                    val mid = fxQ(u, x0 + (InventorPlay.PAD - 0.014f) / 2f, top, (z0 + z1) / 2f)
                    drawCircle(c.copy(alpha = 0.3f), 0.07f * u, mid)
                }
            }
            drawPath(face, Ink.line, style = pen.stroke)
            val streak = f.angleV.toInt()
            for (i in 0 until InventorPlay.GOAL) {
                val p = fxQ(u, -0.2f + i * 0.1f, top, z1 - 0.011f)
                drawCircle(if (i < streak) FlameCore else Color.White.copy(alpha = 0.3f), 0.0075f * u, p)
            }
        }
        else -> Unit
    }
    return true
}

/** The rocket kit's lower hull and cockpit glass cover a rider's legs, in front of the figure. */
fun DrawScope.drawInventorFront(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in InventorPlay.TYPES) return false
    if (f.type == FixtureType.PLAY_ROCKET_KIT) translate(0f, -f.angle * InventorPlay.RISE * u) {
        inkedRound(Rect(-0.085f * u, -0.118f * u, 0.085f * u, -0.045f * u), 0.014f * u, Cream, pen)
        drawLine(Color(0xFFE5484D), Offset(-0.085f * u, -0.085f * u), Offset(0.085f * u, -0.085f * u), pen.lw * 3.2f)
        drawLine(Color.White.copy(alpha = 0.5f), Offset(-0.045f * u, -0.3f * u), Offset(-0.045f * u, -0.17f * u), pen.lw * 1.6f, StrokeCap.Round)
    }
    return true
}

private fun DrawScope.rocketHull(u: Float, pen: Pen) {
    val red = Color(0xFFE5484D)
    fun path(block: Path.() -> Unit) = Path().apply(block)
    // Fins behind the body, then the body, nose cone, cockpit and nozzle.
    inked(path { moveTo(-0.08f * u, -0.16f * u); lineTo(-0.15f * u, -0.04f * u); lineTo(-0.15f * u, -0.01f * u); lineTo(-0.08f * u, -0.045f * u); close() }, red, pen)
    inked(path { moveTo(0.08f * u, -0.16f * u); lineTo(0.15f * u, -0.04f * u); lineTo(0.15f * u, -0.01f * u); lineTo(0.08f * u, -0.045f * u); close() }, red, pen)
    inked(path { moveTo(-0.04f * u, -0.045f * u); lineTo(-0.055f * u, -0.015f * u); lineTo(0.055f * u, -0.015f * u); lineTo(0.04f * u, -0.045f * u); close() }, Color(0xFF3C3550), pen)
    inked(path {
        moveTo(-0.085f * u, -0.045f * u); lineTo(-0.085f * u, -0.32f * u); quadraticTo(-0.085f * u, -0.42f * u, 0f, -0.465f * u)
        quadraticTo(0.085f * u, -0.42f * u, 0.085f * u, -0.32f * u); lineTo(0.085f * u, -0.045f * u); close()
    }, Cream, pen)
    inked(path { moveTo(-0.072f * u, -0.375f * u); quadraticTo(-0.05f * u, -0.43f * u, 0f, -0.465f * u); quadraticTo(0.05f * u, -0.43f * u, 0.072f * u, -0.375f * u); close() }, red, pen, shade = false)
    inkedRound(Rect(-0.066f * u, -0.345f * u, 0.066f * u, -0.11f * u), 0.04f * u, Color(0xFF3A4A78), pen, shade = false)
}

/** Exhaust under the rocket: short and flickering on the pad, long once it flies. */
private fun DrawScope.rocketFlame(f: Fixture, u: Float, pen: Pen) {
    val long = if (f.angle > 0.01f) 0.12f else 0.045f
    val len = long + 0.02f * sin(pen.t * 40f)
    drawPath(Path().apply { moveTo(-0.045f * u, -0.018f * u); lineTo(0f, (-0.018f + len) * u); lineTo(0.045f * u, -0.018f * u); close() }, Flame)
    drawPath(Path().apply { moveTo(-0.025f * u, -0.018f * u); lineTo(0f, (-0.018f + len * 0.65f) * u); lineTo(0.025f * u, -0.018f * u); close() }, FlameCore)
}

/** A cog: ink-rimmed teeth round a coloured disc with a dark hub. [rot] turns it. */
private fun DrawScope.gear(cx: Float, cy: Float, r: Float, rot: Float, color: Color, u: Float, pen: Pen) {
    for (k in 0 until 8) {
        val a = rot + k * 0.7854f
        val from = Offset((cx + cos(a) * r * 0.8f) * u, (cy + sin(a) * r * 0.8f) * u)
        val to = Offset((cx + cos(a) * r * 1.3f) * u, (cy + sin(a) * r * 1.3f) * u)
        drawLine(Ink.line, from, to, r * u * 0.62f, StrokeCap.Butt)
        drawLine(color, from, to, r * u * 0.38f, StrokeCap.Butt)
    }
    inkedCircle(Offset(cx * u, cy * u), r * u, color, pen)
    drawCircle(Ink.line, r * u * 0.3f, Offset(cx * u, cy * u))
}

/** A little robot pal from the workshop: a box head with two eyes and an antenna, a body and two feet. The variant is its colour. */
internal fun DrawScope.thRobotPal(variant: Int, w: Float, h: Float, pen: Pen) {
    val body = listOf(ToyColors[1], ToyColors[2], ToyColors[3], ToyColors[5])[variant.mod(4)]
    inkedRound(Rect(-0.30f * w, -0.13f * h, -0.06f * w, 0f), w * 0.05f, Color(0xFF564768), pen, shade = false)
    inkedRound(Rect(0.06f * w, -0.13f * h, 0.30f * w, 0f), w * 0.05f, Color(0xFF564768), pen, shade = false)
    inkedRound(Rect(-0.34f * w, -0.52f * h, 0.34f * w, -0.1f * h), w * 0.07f, body, pen)
    drawCircle(Color.White.copy(alpha = 0.75f), w * 0.07f, Offset(0f, -0.31f * h))
    inkedRound(Rect(-0.42f * w, -0.88f * h, 0.42f * w, -0.48f * h), w * 0.09f, Color(0xFFE6EAF2), pen)
    for (sx in listOf(-0.19f, 0.19f)) {
        inkedCircle(Offset(sx * w, -0.68f * h), w * 0.1f, Color.White, pen, shade = false)
        drawCircle(Ink.line, w * 0.045f, Offset(sx * w, -0.68f * h))
    }
    drawLine(Ink.line, Offset(0f, -0.88f * h), Offset(0f, -0.97f * h), pen.lw, StrokeCap.Round)
    drawCircle(Color(0xFFFF4D6D), w * 0.06f, Offset(0f, -0.98f * h))
}
