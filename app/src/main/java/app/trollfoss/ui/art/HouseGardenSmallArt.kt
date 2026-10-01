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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.GardenGnomes
import app.trollfoss.domain.GnomeKind
import app.trollfoss.domain.Weather
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/*
 * Hagen's small furniture in oblique 3D: the garden gnomes (five of them, each with its own colours), the
 * lawn robot with a face, the sprinkler and its rainbow, the kettle grill, the patio table with a parasol,
 * the garden shed, the arbour gate, the snowman and the back door of the house. Origin at the bottom centre
 * of each fixture's front face. Helpers start with `gm` (gnome and mower) or `gs` plus a letter.
 */

private fun DrawScope.gmShadow(u: Float, w: Float, d: Float, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

// ------------------------------------------------------------------------------------------------ gnome

private val GN_HATS = intArrayOf(0xFFD2443A.toInt(), 0xFF2F6FB8.toInt(), 0xFF3BA864.toInt(), 0xFFFFC83D.toInt(), 0xFF9A6BE8.toInt())
private val GN_COATS = intArrayOf(0xFF2F6FB8.toInt(), 0xFF3BA864.toInt(), 0xFFD2443A.toInt(), 0xFF9A6BE8.toInt(), 0xFFFF9A3D.toInt())

/**
 * A garden gnome in one of its places: standing, sitting (on a roof or a table), fishing, digging or asleep.
 * The beard is white and big, the nose is round, the hat is pointed and the colours depend on which gnome it
 * is. It winks (and sparkles) when tapped, blinks now and then, and looks to the left or right.
 */
internal fun DrawScope.gaGnome(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val kind = GardenGnomes.kindOf(f)
    val left = GardenGnomes.facesLeft(f)
    val hat = Color(GN_HATS[f.variant % 5])
    val coat = Color(GN_COATS[f.variant % 5])
    val wink = f.anim > 0.35f
    val blink = fxFrac(t / (3.6f + f.variant * 0.5f)) < 0.045f
    gmShadow(u, 0.1f, 0.06f, 0.8f)
    // Drawn in a frame a quarter bigger than the fixture, then scaled down to fit it.
    val s = u * 0.8f
    fun o(x: Float, y: Float) = Offset(x * s, y * s)
    scale(if (left) -1f else 1f, 1f, pivot = Offset.Zero) {
        // The idle bob: a tiny breath.
        val breath = sin(t * 2.2f + f.variant) * 0.0016f * u
        translate(0f, if (kind == GnomeKind.NAP) breath * 2f else breath) {
            val skin = Color(0xFFFFD0A8)
            val beard = Color(0xFFF7F4EE)
            val boot = Color(0xFF3A2E3E)
            val sitting = kind != GnomeKind.STAND
            // Boots and legs.
            if (!sitting) {
                for (sx in floatArrayOf(-0.017f, 0.017f)) {
                    inkedRound(Rect((sx - 0.015f) * s, -0.017f * s, (sx + 0.015f) * s, 0f), 0.006f * s, boot, pen, shade = false)
                    drawRect(coat.darken(0.3f), o(sx - 0.008f, -0.03f), Size(0.016f * s, 0.016f * s))
                }
            } else {
                // Legs stretched forward, boots up.
                for (k in 0..1) {
                    val z = k * 0.012f
                    capsule(o(0.005f - z * 0.4f, -0.016f - z * 0.2f), o(0.036f, -0.014f - z * 0.2f), 0.014f * s, coat.darken(0.3f), pen)
                    inkedRound(Rect((0.032f) * s, (-0.03f - z * 0.2f) * s, (0.054f) * s, (-0.006f - z * 0.2f) * s), 0.006f * s, boot, pen, shade = false)
                }
            }
            val bodyBase = if (sitting) -0.02f else -0.03f
            // The coat.
            val bodyTop = bodyBase - 0.047f
            val coatPath = Path().apply {
                moveTo(-0.033f * s, bodyBase * s)
                quadraticTo(-0.038f * s, (bodyBase - 0.025f) * s, -0.027f * s, bodyTop * s)
                lineTo(0.027f * s, bodyTop * s)
                quadraticTo(0.038f * s, (bodyBase - 0.025f) * s, 0.033f * s, bodyBase * s)
                close()
            }
            inked(coatPath, coat, pen)
            drawLine(Color(0xFF3A2E3E), o(-0.034f, bodyBase - 0.014f), o(0.034f, bodyBase - 0.014f), strokeWidth = 0.007f * s)
            inkedRound(Rect(-0.007f * s, (bodyBase - 0.0185f) * s, 0.007f * s, (bodyBase - 0.0095f) * s), 0.002f * s, FxC.brass, pen, shade = false)
            // The arms and what they hold.
            val shoulder = bodyTop + 0.012f
            val armSwing = sin(t * 2.2f + f.variant) * 0.002f
            when (kind) {
                GnomeKind.STAND -> {
                    capsule(o(-0.03f, shoulder), o(-0.044f, shoulder + 0.03f + armSwing), 0.011f * s, coat.darken(0.12f), pen)
                    drawCircle(skin, 0.0075f * s, o(-0.044f, shoulder + 0.034f + armSwing))
                    drawCircle(Ink.line, 0.0075f * s, o(-0.044f, shoulder + 0.034f + armSwing), style = pen.thin)
                    // The other arm holds a thing: a lantern, a little can, a red mushroom, a flower or a shovel.
                    capsule(o(0.03f, shoulder), o(0.048f, shoulder + 0.022f), 0.011f * s, coat.darken(0.12f), pen)
                    val hand = o(0.05f, shoulder + 0.026f)
                    when (f.variant % 5) {
                        0 -> {
                            // A lantern that glows at night.
                            val lc = Offset(hand.x + 0.008f * s, hand.y + 0.012f * s)
                            fxGlow(lc, 0.07f * s, FxC.warm, 0.25f + 0.6f * pen.night)
                            fxLine(Offset(hand.x, hand.y - 0.002f * s), Offset(lc.x, lc.y - 0.012f * s), Ink.line, pen.lw * 0.7f)
                            inkedRound(Rect(lc.x - 0.008f * s, lc.y - 0.011f * s, lc.x + 0.008f * s, lc.y + 0.011f * s), 0.003f * s, lerp(Color(0xFFFFF2B0), Color(0xFFFFD66B), pen.night), pen, shade = false)
                        }
                        1 -> {
                            val c = Offset(hand.x + 0.01f * s, hand.y + 0.006f * s)
                            inkedRound(Rect(c.x - 0.011f * s, c.y - 0.008f * s, c.x + 0.011f * s, c.y + 0.012f * s), 0.003f * s, FxC.dusty, pen, shade = false)
                            capsule(Offset(c.x + 0.008f * s, c.y - 0.002f * s), Offset(c.x + 0.022f * s, c.y - 0.012f * s), 0.004f * s, FxC.dusty, pen)
                        }
                        2 -> {
                            val c = Offset(hand.x + 0.004f * s, hand.y - 0.002f * s)
                            capsule(Offset(c.x, c.y + 0.002f * s), Offset(c.x, c.y - 0.014f * s), 0.006f * s, FxC.cream, pen)
                            inked(fxPoly(1f, c.x - 0.014f * s, c.y - 0.012f * s, c.x, c.y - 0.03f * s, c.x + 0.014f * s, c.y - 0.012f * s), Color(0xFFE8473F), pen, shade = false)
                            drawCircle(Color.White, 0.0025f * s, Offset(c.x - 0.004f * s, c.y - 0.018f * s))
                        }
                        3 -> {
                            val c = Offset(hand.x + 0.004f * s, hand.y)
                            capsule(Offset(c.x, c.y + 0.004f * s), Offset(c.x, c.y - 0.026f * s), 0.0035f * s, GaK.leafDark, pen)
                            inkedCircle(Offset(c.x, c.y - 0.032f * s), 0.01f * s, FxC.pink, pen, shade = false)
                            drawCircle(FxC.yellow, 0.0042f * s, Offset(c.x, c.y - 0.032f * s))
                        }
                        else -> {
                            val c = Offset(hand.x + 0.004f * s, hand.y)
                            capsule(Offset(c.x, c.y + 0.012f * s), Offset(c.x + 0.004f * s, c.y - 0.04f * s), 0.0035f * s, FxC.oak, pen)
                            inkedRound(Rect(c.x - 0.007f * s, c.y - 0.062f * s, c.x + 0.013f * s, c.y - 0.036f * s), 0.004f * s, FxC.steel, pen, shade = false)
                        }
                    }
                    drawCircle(skin, 0.0075f * s, hand)
                    drawCircle(Ink.line, 0.0075f * s, hand, style = pen.thin)
                }
                GnomeKind.SIT, GnomeKind.NAP -> {
                    capsule(o(-0.028f, shoulder), o(-0.02f, shoulder + 0.028f), 0.011f * s, coat.darken(0.12f), pen)
                    capsule(o(0.028f, shoulder), o(0.04f, shoulder + 0.026f), 0.011f * s, coat.darken(0.12f), pen)
                    drawCircle(skin, 0.0075f * s, o(0.041f, shoulder + 0.03f))
                    drawCircle(Ink.line, 0.0075f * s, o(0.041f, shoulder + 0.03f), style = pen.thin)
                }
                GnomeKind.FISH -> {
                    capsule(o(0.028f, shoulder), o(0.05f, shoulder + 0.01f), 0.011f * s, coat.darken(0.12f), pen)
                    capsule(o(-0.026f, shoulder), o(0.03f, shoulder + 0.022f), 0.011f * s, coat.darken(0.12f), pen)
                    val hand = o(0.052f, shoulder + 0.012f)
                    drawCircle(skin, 0.0075f * s, hand)
                    // The rod, bending, and a line down to a red-and-white bobber that bobs.
                    val tip = o(0.15f, shoulder - 0.11f)
                    val rod = Path().apply {
                        moveTo(hand.x - 0.01f * s, hand.y + 0.004f * s)
                        quadraticTo((hand.x + tip.x) / 2f, hand.y - 0.08f * s, tip.x, tip.y)
                    }
                    drawPath(rod, Ink.line, style = Stroke(pen.lw * 2.6f, cap = StrokeCap.Round))
                    drawPath(rod, FxC.oak, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
                    val bob = sin(t * 2.3f + f.variant) * 0.003f * s
                    val bob0 = Offset(tip.x + 0.004f * s, tip.y + (0.13f * 1f) * s + bob)
                    drawLine(Ink.line, tip, bob0, strokeWidth = pen.lw * 0.5f)
                    inkedCircle(bob0, 0.007f * s, Color.White, pen, shade = false)
                    drawArc(Color(0xFFE8473F), 180f, 180f, true, Offset(bob0.x - 0.007f * s, bob0.y - 0.007f * s), Size(0.014f * s, 0.014f * s))
                    if (f.anim > 0.2f) drawCircle(GaK.water, 0.012f * s * f.anim, bob0, alpha = 0.4f * f.anim, style = pen.thin)
                }
                GnomeKind.DIG -> {
                    // Leaning forward with a little shovel; sand flies.
                    capsule(o(0.026f, shoulder), o(0.05f, shoulder + 0.025f), 0.011f * s, coat.darken(0.12f), pen)
                    val dig = sin(t * 4f + f.variant) * 0.01f
                    val handle = o(0.05f, shoulder + 0.026f)
                    val blade = o(0.075f + dig, bodyBase + 0.004f)
                    capsule(handle, blade, 0.0045f * s, FxC.oak, pen)
                    inkedRound(Rect(blade.x - 0.008f * s, blade.y - 0.004f * s, blade.x + 0.012f * s, blade.y + 0.012f * s), 0.003f * s, FxC.steel, pen, shade = false)
                    drawCircle(skin, 0.0075f * s, handle)
                    val ph = fxFrac(t * 1.2f + f.variant * 0.3f)
                    drawCircle(Color(0xFFE8C98A), 0.004f * s, Offset(blade.x + (0.01f + ph * 0.02f) * s, blade.y - (0.02f + ph * 0.025f) * s), alpha = 1f - ph)
                    capsule(o(-0.026f, shoulder), o(-0.02f, shoulder + 0.026f), 0.011f * s, coat.darken(0.12f), pen)
                }
            }
            // The beard: white and big, with a wavy bottom edge.
            val faceY = bodyTop - 0.012f
            val beardPath = Path().apply {
                moveTo(-0.034f * s, faceY * s)
                cubicTo(-0.04f * s, (faceY + 0.03f) * s, -0.03f * s, (bodyBase - 0.008f) * s, -0.012f * s, (bodyBase - 0.012f) * s)
                quadraticTo(-0.004f * s, (bodyBase - 0.002f) * s, 0.004f * s, (bodyBase - 0.014f) * s)
                quadraticTo(0.016f * s, (bodyBase - 0.004f) * s, 0.026f * s, (bodyBase - 0.014f) * s)
                cubicTo(0.04f * s, (faceY + 0.02f) * s, 0.04f * s, faceY * s, 0.034f * s, faceY * s)
                close()
            }
            inked(beardPath, beard, pen)
            for (k in -1..1) drawLine(Ink.line, o(k * 0.012f, faceY + 0.014f), o(k * 0.014f, faceY + 0.03f), strokeWidth = pen.lw * 0.5f, alpha = 0.35f, cap = StrokeCap.Round)
            // The face: nose, cheeks and eyes under the hat brim.
            val headY = faceY - 0.02f
            val head = o(0f, headY)
            inkedCircle(Offset(head.x, head.y + 0.001f * s), 0.03f * s, skin, pen, shade = true)
            // The beard covers the lower face again.
            inked(Path().apply {
                moveTo(-0.03f * s, (headY + 0.004f) * s)
                quadraticTo(0f, (headY - 0.003f) * s, 0.03f * s, (headY + 0.004f) * s)
                quadraticTo(0.03f * s, (faceY + 0.012f) * s, 0.0f, (faceY + 0.014f) * s)
                quadraticTo(-0.03f * s, (faceY + 0.012f) * s, -0.03f * s, (headY + 0.004f) * s)
                close()
            }, beard, pen, shade = false)
            // Cheeks and the nose.
            drawCircle(Ink.blush, 0.0075f * s, o(-0.021f, headY - 0.004f))
            drawCircle(Ink.blush, 0.0075f * s, o(0.021f, headY - 0.004f))
            inkedCircle(o(0.004f, headY + 0.002f), 0.0105f * s, Color(0xFFFFA8A0), pen, shade = true)
            // Eyes: two dots, one of them shut when it winks (with a sparkle) or when it sleeps or blinks.
            val shut = kind == GnomeKind.NAP || blink
            for (e in 0..1) {
                val ex = if (e == 0) -0.0135f else 0.0165f
                val ey = headY - 0.012f
                if (shut || (wink && e == 1)) {
                    drawArc(Ink.line, 0f, 180f, false, Offset((ex - 0.005f) * s, (ey - 0.004f) * s), Size(0.01f * s, 0.008f * s), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
                } else {
                    drawCircle(Ink.line, 0.0036f * s, o(ex, ey))
                    drawCircle(Color.White, 0.0012f * s, o(ex - 0.001f, ey - 0.001f))
                }
            }
            if (wink) twinkle(o(0.034f, headY - 0.03f), 0.011f * s, FxC.yellow, 1f)
            // The pointed hat with a band, its tip bent over.
            val hatBase = headY - 0.022f
            val tipX = 0.034f
            val hatPath = Path().apply {
                moveTo(-0.04f * s, (hatBase + 0.008f) * s)
                quadraticTo(-0.03f * s, (hatBase - 0.03f) * s, (tipX - 0.02f) * s, (hatBase - 0.062f) * s)
                quadraticTo((tipX + 0.01f) * s, (hatBase - 0.082f) * s, (tipX + 0.016f) * s, (hatBase - 0.058f) * s)
                quadraticTo((tipX + 0.012f) * s, (hatBase - 0.062f) * s, (tipX + 0.01f) * s, (hatBase - 0.05f) * s)
                quadraticTo(0.03f * s, (hatBase - 0.03f) * s, 0.04f * s, (hatBase + 0.008f) * s)
                quadraticTo(0f, (hatBase + 0.016f) * s, -0.04f * s, (hatBase + 0.008f) * s)
                close()
            }
            val hatPlace = if (kind == GnomeKind.NAP) 1f else 0f
            rotate(hatPlace * 12f, pivot = Offset(0f, hatBase * s)) {
                inked(hatPath, hat, pen)
                drawLine(hat.darken(0.3f), o(-0.039f, hatBase + 0.004f), o(0.039f, hatBase + 0.004f), strokeWidth = 0.0075f * s, cap = StrokeCap.Round)
                inkedCircle(o(tipX + 0.014f, hatBase - 0.056f), 0.0045f * s, Color.White, pen, shade = false)
            }
            if (kind == GnomeKind.NAP) {
                for (k in 0..1) {
                    val ph = fxFrac(t * 0.45f + k * 0.5f)
                    drawLine(FxC.paint.copy(alpha = 1f - ph), o(0.045f + ph * 0.02f + k * 0.012f, headY - 0.05f - ph * 0.06f), o(0.06f + ph * 0.02f + k * 0.012f, headY - 0.05f - ph * 0.06f), strokeWidth = pen.lw * 1.4f, alpha = 1f - ph)
                    drawLine(FxC.paint, o(0.06f + ph * 0.02f + k * 0.012f, headY - 0.05f - ph * 0.06f), o(0.045f + ph * 0.02f + k * 0.012f, headY - 0.036f - ph * 0.06f), strokeWidth = pen.lw * 1.4f, alpha = 1f - ph)
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------ mower

/**
 * The lawn robot: a round green dome on four small wheels with a black visor and two bright eyes that look the
 * way it drives. When it works its blades whirr and grass flies; at night it sleeps with closed eyes.
 */
internal fun DrawScope.gaMower(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val dir = if (f.angleV < 0f) -1f else 1f
    val sleeping = !f.on && pen.night > 0.5f
    gmShadow(u, 0.16f, 0.09f)
    translate(0f, f.bob * u) {
        scale(dir, 1f, pivot = Offset.Zero) {
            val green = Color(0xFF55B34A)
            val white = Color(0xFFF7F5F0)
            // Wheels: two small ones at the sides, black with a pale hub.
            for ((k, wx) in floatArrayOf(-0.052f, 0.052f).withIndex()) {
                val wc = Offset(wx * u, -0.016f * u)
                inkedCircle(wc, 0.0165f * u, FxC.rubber, pen, shade = false)
                drawCircle(Color(0xFFBFC6D6), 0.0065f * u, wc)
                val spin = if (f.on) t * 9f * (if (k == 0) 1f else 1f) else 0f
                drawLine(FxC.rubber, Offset(wc.x + cos(spin) * 0.006f * u, wc.y + sin(spin) * 0.006f * u), Offset(wc.x - cos(spin) * 0.006f * u, wc.y - sin(spin) * 0.006f * u), strokeWidth = pen.lw * 1.2f)
            }
            // The skirt with the blade spinning under it.
            if (f.on) {
                val a = t * 40f
                for (k in 0..2) {
                    val ang = a + k * 2.094f
                    drawLine(Color(0xFFBFC6D6).copy(alpha = 0.7f), Offset(cos(ang) * 0.03f * u, -0.004f * u), Offset(cos(ang + 3.1416f) * 0.03f * u, -0.004f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
                }
            }
            inkedRound(Rect(-0.072f * u, -0.034f * u, 0.072f * u, -0.006f * u), 0.012f * u, white, pen, shade = true)
            // The dome.
            val dome = Path().apply {
                moveTo(-0.068f * u, -0.032f * u)
                cubicTo(-0.07f * u, -0.095f * u, -0.04f * u, -0.108f * u, 0f, -0.108f * u)
                cubicTo(0.04f * u, -0.108f * u, 0.07f * u, -0.095f * u, 0.068f * u, -0.032f * u)
                close()
            }
            inked(dome, green, pen)
            shine(Offset(-0.03f * u, -0.085f * u), 0.028f * u, 0.014f * u, 0.55f)
            // The visor and the eyes that look ahead.
            val visor = Path().apply {
                moveTo(-0.052f * u, -0.05f * u)
                quadraticTo(-0.05f * u, -0.082f * u, 0f, -0.082f * u)
                quadraticTo(0.05f * u, -0.082f * u, 0.052f * u, -0.05f * u)
                quadraticTo(0f, -0.04f * u, -0.052f * u, -0.05f * u)
                close()
            }
            drawPath(visor, Color(0xFF26323A))
            drawPath(visor, Ink.line, style = pen.stroke)
            val eyeGlow = if (sleeping) 0.0f else 1f
            for (e in 0..1) {
                val ex = (if (e == 0) -0.02f else 0.02f) + 0.006f
                val ey = -0.062f
                if (sleeping || (!f.on && fxFrac(t / 4f) < 0.04f)) {
                    drawArc(Color(0xFF7CFFB2), 0f, 180f, false, Offset((ex - 0.008f) * u, (ey - 0.004f) * u), Size(0.016f * u, 0.01f * u), style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
                } else {
                    drawCircle(Color(0xFF7CFFB2).copy(alpha = 0.35f), 0.014f * u, Offset(ex * u, ey * u))
                    drawCircle(Color(0xFF7CFFB2), 0.0085f * u, Offset(ex * u, ey * u))
                    drawCircle(Color.White, 0.0028f * u, Offset((ex - 0.002f) * u, (ey - 0.003f) * u))
                }
            }
            if (eyeGlow > 0f && f.on) drawArc(Color(0xFF7CFFB2), 0f, 180f, false, Offset(-0.014f * u, -0.058f * u), Size(0.028f * u, 0.014f * u), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
            // The antenna with a blinking red ball, and a tiny flag.
            drawLine(Ink.line, Offset(0.03f * u, -0.1f * u), Offset(0.044f * u, -0.138f * u), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            val on = if (f.on) 0.5f + 0.5f * sin(t * 6f) else 0.3f
            inkedCircle(Offset(0.044f * u, -0.141f * u), 0.0065f * u, lerp(Color(0xFF8A2A24), Color(0xFFFF5A5F), on), pen, shade = false)
            // A bumper strip along the front.
            drawLine(white, Offset(0.062f * u, -0.05f * u), Offset(0.066f * u, -0.03f * u), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
        }
    }
    if (sleeping) {
        for (k in 0..1) {
            val ph = fxFrac(t * 0.4f + k * 0.5f)
            val zx = 0.05f * u + ph * 0.03f * u
            val zy = -0.12f * u - ph * 0.08f * u
            drawLine(FxC.paint.copy(alpha = 1f - ph), Offset(zx, zy), Offset(zx + 0.012f * u, zy), strokeWidth = pen.lw * 1.5f)
            drawLine(FxC.paint.copy(alpha = 1f - ph), Offset(zx + 0.012f * u, zy), Offset(zx, zy + 0.012f * u), strokeWidth = pen.lw * 1.5f)
            drawLine(FxC.paint.copy(alpha = 1f - ph), Offset(zx, zy + 0.012f * u), Offset(zx + 0.012f * u, zy + 0.012f * u), strokeWidth = pen.lw * 1.5f)
        }
    }
}

// --------------------------------------------------------------------------------------------- sprinkler

/** The sprinkler on its spike: a turning head; when it is on, jets of drops arc over the lawn and make a small rainbow. */
internal fun DrawScope.gaSprinkler(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    gmShadow(u, 0.1f, 0.06f, 0.7f)
    val green = Color(0xFF3BC46B)
    inkedRound(Rect(-0.046f * u, -0.016f * u, 0.046f * u, 0f), 0.007f * u, green, pen, shade = true)
    drawLine(Ink.line, Offset(-0.03f * u, -0.008f * u), Offset(0.03f * u, -0.008f * u), strokeWidth = pen.lw * 0.5f, alpha = 0.4f)
    inkedRound(Rect(-0.014f * u, -0.034f * u, 0.014f * u, -0.014f * u), 0.004f * u, Color(0xFF2F9BFF), pen, shade = false)
    val spin = t * (if (f.on) 5f else 0.3f)
    for (k in 0 until 3) {
        val a = spin + k * 2.094f
        val nz = Offset(cos(a) * 0.024f * u, -0.036f * u + sin(a) * 0.004f * u)
        drawLine(Ink.line, Offset(0f, -0.034f * u), nz, strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
        drawLine(FxC.steel, Offset(0f, -0.034f * u), nz, strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
    }
    if (!f.on) return
    // The spray: for each of six jets, a fan of drops along a parabola, moving outwards in time.
    val water = Color(0xFF8FD3FF)
    for (j in 0 until 6) {
        val ang = (j / 5f - 0.5f) * 2.4f + sin(t * 1.4f) * 0.3f
        val vx = sin(ang) * 0.32f
        val vy = -0.42f - 0.08f * cos(ang)
        for (k in 0 until 10) {
            val tau = fxFrac(t * 1.1f + k / 10f + j * 0.07f) * 0.9f
            val x = vx * tau
            val y = -0.034f + vy * tau + 0.55f * tau * tau
            if (y > 0.004f) continue
            drawCircle(water, 0.0042f * u, Offset(x * u, y * u), alpha = 0.9f * (1f - tau / 1.1f))
        }
    }
    // A small rainbow in the mist by day.
    val k = (1f - pen.night) * (1f - overcast(pen))
    if (k > 0.05f) {
        for ((i, c) in Pal.rainbow.withIndex()) {
            val r = (0.17f - i * 0.0075f) * u
            drawArc(c, 200f, 140f, false, Offset(-r, -0.034f * u - r * 0.6f), Size(r * 2f, r * 1.1f), alpha = 0.32f * k, style = Stroke(0.0075f * u))
        }
    }
}

// --------------------------------------------------------------------------------------------------- grill

/**
 * The kettle grill: a red bowl on three legs with its lid leaning on the side, a grate on top, coals that glow
 * and flames when it is on, smoke, and tongs on a hook. Sausages rest on the grate.
 */
internal fun DrawScope.gaGrill(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val on = f.on
    gmShadow(u, 0.22f, 0.12f)
    val c = q(0f, 0f, 0.06f)
    val red = Color(0xFFD2443A)
    // Legs and the ash catcher.
    for (sx in floatArrayOf(-0.075f, 0.075f)) {
        capsule(Offset(c.x + sx * u, c.y - 0.1f * u), Offset(c.x + sx * 1.3f * u, c.y - 0.006f * u), 0.0055f * u, FxC.iron, pen)
    }
    capsule(q(0f, -0.1f, 0.12f), q(0f, -0.006f, 0.15f), 0.0055f * u, FxC.iron, pen)
    inkedCircle(Offset(c.x + 0.098f * u, c.y - 0.007f * u), 0.0085f * u, FxC.rubber, pen, shade = false)
    // The bowl: a half sphere.
    val rim = c.y - 0.17f * u
    val bowl = Path().apply {
        moveTo(c.x - 0.1f * u, rim)
        cubicTo(c.x - 0.1f * u, rim + 0.075f * u, c.x - 0.05f * u, rim + 0.095f * u, c.x, rim + 0.095f * u)
        cubicTo(c.x + 0.05f * u, rim + 0.095f * u, c.x + 0.1f * u, rim + 0.075f * u, c.x + 0.1f * u, rim)
        close()
    }
    // The inside seen from above: dark with glowing coals.
    val inside = fxDisc2(c.x, rim, 0.1f * u / 1.118f * 1.1f, 0.075f * u)
    inked(bowl, red, pen)
    shine(Offset(c.x - 0.058f * u, rim + 0.04f * u), 0.026f * u, 0.011f * u, 0.55f)
    drawLine(FxC.iron, Offset(c.x - 0.1f * u, rim), Offset(c.x + 0.1f * u, rim), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
    drawPath(inside, Color(0xFF2B2630))
    drawPath(inside, Ink.line, style = pen.stroke)
    // Coals: dull when off, glowing and flickering when on.
    for (k in 0 until 9) {
        val a = k * 0.7f
        val cx = c.x + cos(a) * (0.012f + 0.05f * hash01(k, 871)) * u
        val cy = rim + sin(a) * 0.03f * u * hash01(k, 872)
        val glow = if (on) 0.6f + 0.4f * sin(t * 5f + k * 1.7f) else 0.0f
        drawCircle(lerp(Color(0xFF4A3A36), Color(0xFFFF7A2E), glow), 0.0095f * u, Offset(cx, cy))
    }
    if (on) {
        fxGlow(Offset(c.x, rim - 0.02f * u), 0.17f * u, FxC.flame2, 0.3f + 0.35f * pen.night)
        for (k in 0 until 4) fxFire(c.x + (k - 1.5f) * 0.03f * u, rim + 0.002f * u, 0.03f * u, (0.05f + 0.015f * (k % 2)) * u, t, k * 1.3f, pen)
        fxPuffs(c.x, rim - 0.1f * u, t, 0.018f * u, 0.18f * u, Color(0xFFB0B4C0), 0.4f, 4, 0.3f, 0.03f * u)
    }
    // The grate: where the food lies.
    val grate = fxFlat(u, -0.078f, 0.078f, -0.17f, 0.03f, 0.13f)
    drawPath(grate, FxC.iron.copy(alpha = 0.25f))
    drawPath(grate, FxC.steel, style = Stroke(0.0035f * u))
    for (k in 0..7) {
        val x = -0.072f + k * 0.0206f
        fxLine(q(x, -0.17f, 0.03f), q(x, -0.17f, 0.13f), FxC.steel, 0.0022f * u)
    }
    // The lid, leaning on the right side, with a handle and a little thermometer.
    rotate(-18f, pivot = Offset(c.x + 0.125f * u, c.y - 0.02f * u)) {
        val lid = Path().apply {
            moveTo(c.x + 0.045f * u, c.y - 0.02f * u)
            cubicTo(c.x + 0.045f * u, c.y - 0.1f * u, c.x + 0.1f * u, c.y - 0.12f * u, c.x + 0.125f * u, c.y - 0.12f * u)
            cubicTo(c.x + 0.15f * u, c.y - 0.12f * u, c.x + 0.205f * u, c.y - 0.1f * u, c.x + 0.205f * u, c.y - 0.02f * u)
            close()
        }
        inked(lid, red.darken(0.05f), pen)
        inkedCircle(Offset(c.x + 0.125f * u, c.y - 0.135f * u), 0.0075f * u, FxC.iron, pen, shade = false)
        inkedCircle(Offset(c.x + 0.16f * u, c.y - 0.075f * u), 0.0095f * u, FxC.paint, pen, shade = false)
        drawLine(Ink.line, Offset(c.x + 0.16f * u, c.y - 0.075f * u), Offset(c.x + 0.167f * u, c.y - 0.082f * u), strokeWidth = pen.lw * 0.9f)
        drawArc(Color(0xFF3BC46B), 200f, 60f, false, Offset(c.x + 0.153f * u, c.y - 0.082f * u), Size(0.014f * u, 0.014f * u), style = Stroke(pen.lw * 0.9f))
    }
    // Tongs hanging on a hook at the left.
    drawLine(FxC.iron, Offset(c.x - 0.1f * u, rim + 0.006f * u), Offset(c.x - 0.116f * u, rim + 0.012f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    capsule(Offset(c.x - 0.114f * u, rim + 0.012f * u), Offset(c.x - 0.12f * u, rim + 0.075f * u), 0.0045f * u, FxC.steel, pen)
    if (f.anim > 0.05f) fxGlow(Offset(c.x, rim), 0.12f * u, FxC.flame3, 0.4f * f.anim)
}

// --------------------------------------------------------------------------------------------------- patio

/** A round table with a checked cloth and a striped parasol, and two chairs with cushions. Mode 1 furls the parasol. */
internal fun DrawScope.gaPatio(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val open = f.mode == 0
    gmShadow(u, 0.54f, 0.14f)
    val tc = q(0f, 0f, 0.07f)
    // Chairs: wooden, with a back and a cushion. They stand either side of the table.
    for (side in floatArrayOf(-1f, 1f)) {
        val cx = side * 0.215f
        val seatY = -0.085f
        for (lx in floatArrayOf(-0.034f, 0.034f)) {
            capsule(q(cx + lx, seatY, 0.03f), q(cx + lx, 0f, 0.03f), 0.005f * u, FxC.oakDark, pen)
            capsule(q(cx + lx, seatY, 0.1f), q(cx + lx, 0f, 0.1f), 0.005f * u, FxC.oakDark, pen)
        }
        // The back: rising behind the seat.
        val bx = cx + side * 0.03f
        for (k in 0..2) fxBox(u, bx - 0.006f, -0.2f + k * 0.035f, bx + 0.006f, -0.18f + k * 0.035f, 0.075f, FxC.oak, pen, z = 0.03f)
        fxBox(u, cx - 0.046f, seatY - 0.01f, cx + 0.046f, seatY + 0.004f, 0.085f, FxC.oak, pen, rad = 0.003f, z = 0.025f)
        val cush = fxFlat(u, cx - 0.04f, cx + 0.04f, seatY - 0.01f, 0.035f, 0.1f, 0.006f)
        fxFace(cush, if (side < 0f) FxC.dusty else FxC.blush, pen)
    }
    // The pedestal and the round top with a checked cloth.
    fxCyl(tc.x, tc.y, tc.y - 0.125f * u, 0.016f * u, 0.011f * u, FxC.iron, pen, cap = false)
    fxFace(fxDisc2(tc.x, tc.y, 0.034f * u, 0.022f * u), FxC.iron, pen)
    val top = q(0f, -0.13f, 0.07f)
    drawPath(fxDisc2(top.x, top.y + 0.006f * u, 0.158f * u, 0.088f * u), FxC.oakDark)
    val cloth = fxDisc2(top.x, top.y, 0.155f * u, 0.086f * u)
    drawPath(cloth, Color(0xFFEAF2FA))
    // Check pattern: crossing blue bands.
    clipPath(cloth) {
        for (k in -3..3) {
            val a = fxMix(Offset(top.x - 0.14f * u, top.y), Offset(top.x + 0.14f * u, top.y), (k + 3.5f) / 7f)
            drawLine(Color(0xFF7FA3D4).copy(alpha = 0.35f), Offset(a.x, top.y - 0.07f * u), Offset(a.x, top.y + 0.07f * u), strokeWidth = 0.011f * u)
        }
        for (k in -2..2) drawLine(Color(0xFF7FA3D4).copy(alpha = 0.35f), Offset(top.x - 0.15f * u, top.y + k * 0.022f * u), Offset(top.x + 0.15f * u, top.y + k * 0.022f * u), strokeWidth = 0.009f * u)
    }
    drawPath(cloth, Ink.line, style = pen.stroke)
    // The parasol: a pole through the middle and a scalloped striped canopy (or a furled one).
    val pole = Offset(top.x, top.y)
    capsule(pole, Offset(pole.x, pole.y - 0.3f * u), 0.0055f * u, FxC.paint, pen)
    if (open) {
        val peakY = pole.y - 0.325f * u
        val hemY = pole.y - 0.235f * u
        val rx = 0.255f * u
        val sway = sin(t * 0.8f + f.id) * 0.004f * u
        val gores = 8
        for (k in 0 until gores) {
            val a0 = pole.x - rx + 2f * rx * k / gores
            val a1 = pole.x - rx + 2f * rx * (k + 1) / gores
            val gore = Path().apply {
                moveTo(pole.x + sway, peakY)
                quadraticTo((pole.x + a0) / 2f + sway, peakY + (hemY - peakY) * 0.7f - 0.012f * u, a0, hemY + 0.006f * u * sin(k * 1.3f))
                quadraticTo((a0 + a1) / 2f, hemY + 0.016f * u, a1, hemY + 0.006f * u * sin((k + 1) * 1.3f))
                quadraticTo((pole.x + a1) / 2f + sway, peakY + (hemY - peakY) * 0.7f - 0.012f * u, pole.x + sway, peakY)
                close()
            }
            drawPath(gore, if (k % 2 == 0) Color(0xFFD2443A) else Color(0xFFF7F5F0))
            drawPath(gore, Ink.line, style = pen.thin)
        }
        inkedCircle(Offset(pole.x + sway, peakY - 0.004f * u), 0.0065f * u, FxC.brass, pen, shade = false)
        // A lantern hanging under the canopy, glowing at night.
        val lan = Offset(pole.x + 0.1f * u, hemY + 0.036f * u)
        fxLine(Offset(lan.x, hemY + 0.006f * u), Offset(lan.x, lan.y - 0.01f * u), Ink.line, pen.lw * 0.6f)
        fxGlow(lan, 0.1f * u, FxC.warm, 0.12f + 0.7f * pen.night)
        inkedRound(Rect(lan.x - 0.007f * u, lan.y - 0.01f * u, lan.x + 0.007f * u, lan.y + 0.012f * u), 0.003f * u, lerp(Color(0xFFFFF2B0), Color(0xFFFFD66B), pen.night), pen, shade = false)
    } else {
        // Furled and tied with a ribbon.
        val furled = Path().apply {
            moveTo(pole.x - 0.004f * u, pole.y - 0.3f * u)
            cubicTo(pole.x - 0.03f * u, pole.y - 0.26f * u, pole.x - 0.03f * u, pole.y - 0.2f * u, pole.x - 0.008f * u, pole.y - 0.18f * u)
            lineTo(pole.x + 0.008f * u, pole.y - 0.18f * u)
            cubicTo(pole.x + 0.03f * u, pole.y - 0.2f * u, pole.x + 0.03f * u, pole.y - 0.26f * u, pole.x + 0.004f * u, pole.y - 0.3f * u)
            close()
        }
        inked(furled, Color(0xFFD2443A), pen)
        for (k in 0..2) drawLine(Color.White.copy(alpha = 0.85f), Offset(pole.x - 0.02f * u + k * 0.02f * u, pole.y - 0.28f * u), Offset(pole.x - 0.012f * u + k * 0.012f * u, pole.y - 0.19f * u), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
        drawLine(Color(0xFFFFC83D), Offset(pole.x - 0.024f * u, pole.y - 0.22f * u), Offset(pole.x + 0.024f * u, pole.y - 0.22f * u), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
    }
}

// ---------------------------------------------------------------------------------------------------- shed

/**
 * The garden shed: red planks with white trim, a gable roof with moss, a flower-box window and a door that opens
 * on a tool wall, a shelf of pots and a lantern. Snow lies on the roof when it snows.
 */
internal fun DrawScope.gaShed(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.24f
    val half = 0.285f
    val eave = -0.335f
    val peak = -0.46f
    val wall = Color(0xFFB8342B)
    val trim = FxC.paint
    gmShadow(u, 0.58f, d)
    // The right side wall (seen in oblique) and the front wall with plank lines.
    fxBox(u, -half, eave, half, 0f, d, wall, pen, rad = 0.003f, top = wall.lighten(0.1f), side = wall.darken(0.22f), front = false)
    val side0 = q(half, eave, 0f)
    val side1 = q(half, eave, d)
    val sideB0 = q(half, 0f, 0f)
    val sideB1 = q(half, 0f, d)
    for (k in 1..6) fxLine(fxMix(side0, side1, k / 7f), fxMix(sideB0, sideB1, k / 7f), wall.darken(0.35f), pen.lw * 0.55f)
    // A window in the side wall with a warm light at night.
    val w0 = q(half, -0.24f, 0.07f)
    val w1 = q(half, -0.24f, 0.17f)
    val w2 = q(half, -0.14f, 0.17f)
    val w3 = q(half, -0.14f, 0.07f)
    val sideWin = fxPath(w0, w1, w2, w3)
    drawPath(sideWin, lerp(GaK.glass, Color(0xFFFFD66B), pen.night))
    drawPath(sideWin, trim, style = Stroke(pen.lw * 1.5f))
    // The front wall.
    val frontRect = fxFront(u, -half, eave, half, 0f, 0f)
    inkedRound(frontRect, 0.003f * u, wall, pen, shade = false)
    for (k in 1..11) fxLine(Offset(frontRect.left + frontRect.width * k / 12f, frontRect.top + 0.004f * u), Offset(frontRect.left + frontRect.width * k / 12f, frontRect.bottom - 0.004f * u), wall.darken(0.3f), pen.lw * 0.55f)
    fxBox(u, -half - 0.006f, -0.014f, half + 0.006f, 0f, 0.02f, trim, pen, rad = 0.002f, z = -0.004f)

    // The window on the right, with curtains and a flower box.
    val win = Rect(0.115f * u, -0.255f * u, 0.235f * u, -0.145f * u)
    inkedRound(Rect(win.left - 0.008f * u, win.top - 0.008f * u, win.right + 0.008f * u, win.bottom + 0.008f * u), 0.004f * u, trim, pen, shade = false)
    drawRect(Brush.verticalGradient(0f to GaK.glass, 1f to GaK.glassEdge, startY = win.top, endY = win.bottom), win.topLeft, win.size)
    if (pen.night > 0.2f) {
        fxGlow(win.center, 0.14f * u, FxC.warm, pen.night * 0.8f)
        drawRect(Color(0xFFFFD66B).copy(alpha = 0.55f * pen.night), win.topLeft, win.size)
    }
    drawLine(trim, Offset(win.center.x, win.top), Offset(win.center.x, win.bottom), strokeWidth = pen.lw * 1.5f)
    drawLine(trim, Offset(win.left, win.center.y), Offset(win.right, win.center.y), strokeWidth = pen.lw * 1.5f)
    drawRect(Ink.line, win.topLeft, win.size, style = pen.thin)
    val box = Rect(win.left - 0.006f * u, win.bottom + 0.004f * u, win.right + 0.006f * u, win.bottom + 0.028f * u)
    inkedRound(box, 0.003f * u, FxC.oakDark, pen, shade = false)
    for (k in 0 until 5) {
        val fx0 = box.left + (0.012f + k * 0.022f) * u
        drawLine(GaK.leaf, Offset(fx0, box.top), Offset(fx0, box.top - 0.016f * u), strokeWidth = pen.lw, cap = StrokeCap.Round)
        drawCircle(arrayOf(FxC.pink, FxC.yellow, FxC.red, Color.White, FxC.pink)[k], 0.0075f * u, Offset(fx0, box.top - 0.02f * u))
    }

    // The door at the left. Shut: white planks with a Z; open: a hollow with the tools, a shelf and a lantern.
    val dl = -0.22f
    val dr = 0.08f
    val dt = -0.325f
    val db = -0.012f
    inkedRound(Rect((dl - 0.008f) * u, (dt - 0.008f) * u, (dr + 0.008f) * u, 0f), 0.003f * u, trim, pen, shade = false)
    if (!f.open) {
        val leaf = Rect(dl * u, dt * u, dr * u, db * u)
        inkedRound(leaf, 0.003f * u, FxC.paint.darken(0.04f), pen, shade = false)
        for (k in 1..6) fxLine(Offset(leaf.left + leaf.width * k / 7f, leaf.top + 0.006f * u), Offset(leaf.left + leaf.width * k / 7f, leaf.bottom - 0.006f * u), FxC.paint.darken(0.28f), pen.lw * 0.55f)
        drawLine(Color(0xFFB8342B).copy(alpha = 0.9f), Offset(leaf.left + 0.012f * u, leaf.top + 0.035f * u), Offset(leaf.right - 0.012f * u, leaf.top + 0.035f * u), strokeWidth = 0.012f * u)
        drawLine(Color(0xFFB8342B).copy(alpha = 0.9f), Offset(leaf.left + 0.012f * u, leaf.bottom - 0.035f * u), Offset(leaf.right - 0.012f * u, leaf.bottom - 0.035f * u), strokeWidth = 0.012f * u)
        drawLine(Color(0xFFB8342B).copy(alpha = 0.9f), Offset(leaf.left + 0.016f * u, leaf.top + 0.04f * u), Offset(leaf.right - 0.016f * u, leaf.bottom - 0.04f * u), strokeWidth = 0.012f * u)
        inkedCircle(Offset(leaf.right - 0.03f * u, leaf.center.y), 0.0085f * u, FxC.brass, pen, shade = false)
        // A latch with a heart cut-out in the top plank.
        val h = fxHeart(leaf.center.x, leaf.top + 0.065f * u, 0.0085f * u)
        drawPath(h, Color(0xFF3A1E2A))
    } else {
        fxHollow(u, dl, dt, dr, db, 0.2f, Color(0xFFB88858), pen, back = Color(0xFF7A5438))
        clipRect(dl * u, dt * u, dr * u, db * u) {
            // The shelf with pots, and tools hanging on the back wall.
            val zb = 0.185f
            fxBox(u, -0.2f, -0.2f, 0.06f, -0.188f, 0.14f, FxC.oak, pen, z = 0.04f)
            for (k in 0 until 3) {
                val pc = q(-0.15f + k * 0.08f, -0.2f, zb - 0.06f)
                inkedRound(Rect(pc.x - 0.014f * u, pc.y - 0.026f * u, pc.x + 0.014f * u, pc.y), 0.003f * u, if (k == 1) FxC.paint else FxC.terracotta, pen, shade = false)
                drawLine(GaK.leaf, Offset(pc.x, pc.y - 0.026f * u), Offset(pc.x + (k - 1) * 0.008f * u, pc.y - 0.05f * u), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            }
            // A rake, a spade and a broom on nails.
            for ((k, tx) in floatArrayOf(-0.19f, -0.15f, -0.11f).withIndex()) {
                val a = q(tx, -0.31f, zb)
                val b = q(tx, -0.05f, zb)
                capsule(a, b, 0.0045f * u, FxC.oak, pen)
                when (k) {
                    0 -> drawLine(FxC.steel, Offset(b.x - 0.014f * u, b.y), Offset(b.x + 0.014f * u, b.y), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
                    1 -> inkedRound(Rect(b.x - 0.011f * u, b.y - 0.002f * u, b.x + 0.011f * u, b.y + 0.026f * u), 0.004f * u, FxC.steel, pen, shade = false)
                    else -> drawPath(fxLeaf(b.x, b.y - 0.02f * u, b.x, b.y + 0.024f * u, 0.5f), Color(0xFFE8C46A))
                }
            }
            // The lantern.
            val lan = q(0.02f, -0.27f, 0.1f)
            fxLine(Offset(lan.x, dt * u), Offset(lan.x, lan.y - 0.012f * u), Ink.line, pen.lw * 0.6f)
            fxGlow(lan, 0.1f * u, FxC.warm, 0.4f + 0.4f * pen.night)
            inkedRound(Rect(lan.x - 0.008f * u, lan.y - 0.011f * u, lan.x + 0.008f * u, lan.y + 0.011f * u), 0.003f * u, Color(0xFFFFE9A8), pen, shade = false)
        }
        fxOpenDoor(u, dl, dt, db, 0.29f, -1f, FxC.paint, FxC.paint.darken(0.1f), pen, 118f)
    }

    // The gable above the front wall, in vertical boards, and the roof with its overhang.
    val gl = q(-half, eave, 0f)
    val gr = q(half, eave, 0f)
    val gp = q(0f, peak, 0f)
    val gable = Path().apply {
        moveTo(gl.x, gl.y)
        lineTo(gp.x, gp.y)
        lineTo(gr.x, gr.y)
        close()
    }
    drawPath(gable, wall.lighten(0.04f))
    drawPath(gable, Ink.line, style = pen.stroke)
    for (k in 1..9) {
        val x = -half + 2f * half * k / 10f
        val topY = peak + (eave - peak) * (abs(x) / half)
        drawLine(wall.darken(0.3f), q(x, eave - 0.004f, 0f), q(x, topY + 0.01f, 0f), strokeWidth = pen.lw * 0.5f)
    }
    drawCircle(Ink.line, 0.0135f * u, Offset(gp.x, gp.y + 0.05f * u))
    drawCircle(lerp(GaK.glass, Color(0xFFFFD66B), pen.night), 0.0105f * u, Offset(gp.x, gp.y + 0.05f * u))
    drawLine(trim, Offset(gp.x, gp.y + 0.04f * u), Offset(gp.x, gp.y + 0.06f * u), strokeWidth = pen.lw)
    val ov = 0.03f
    val roofColor = Color(0xFF6A5444)
    val rl = q(-half - ov, eave + 0.004f, -0.025f)
    val rlb = q(-half - ov, eave + 0.004f, d + 0.025f)
    val rr = q(half + ov, eave + 0.004f, -0.025f)
    val rrb = q(half + ov, eave + 0.004f, d + 0.025f)
    val rp = q(0f, peak - 0.012f, -0.025f)
    val rpb = q(0f, peak - 0.012f, d + 0.025f)
    val leftSlope = fxPath(rl, rlb, rpb, rp)
    val rightSlope = fxPath(rp, rpb, rrb, rr)
    drawPath(leftSlope, roofColor.darken(0.08f))
    drawPath(rightSlope, roofColor.lighten(0.1f))
    for (k in 1..5) {
        fxLine(fxMix(rl, rp, k / 6f), fxMix(rlb, rpb, k / 6f), roofColor.darken(0.35f), pen.lw * 0.6f)
        fxLine(fxMix(rp, rr, k / 6f), fxMix(rpb, rrb, k / 6f), roofColor.darken(0.3f), pen.lw * 0.6f)
    }
    for (k in 0 until 6) {
        val m = fxMix(fxMix(rp, rr, 0.3f + 0.1f * k), fxMix(rpb, rrb, 0.3f + 0.1f * k), 0.2f + 0.1f * (k % 3))
        drawCircle(Color(0xFF6FAE5A).copy(alpha = 0.75f), (0.008f + 0.004f * (k % 2)) * u, m)
    }
    drawPath(leftSlope, Ink.line, style = pen.stroke)
    drawPath(rightSlope, Ink.line, style = pen.stroke)
    if (pen.weather == Weather.SNOW) {
        val snow = Path().apply {
            moveTo(rp.x, rp.y)
            lineTo(rpb.x, rpb.y)
            lineTo(fxMix(rpb, rrb, 0.7f).x, fxMix(rpb, rrb, 0.7f).y)
            lineTo(fxMix(rp, rr, 0.7f).x, fxMix(rp, rr, 0.7f).y)
            close()
        }
        fxFace(snow, FxC.snow, pen)
    }
    // A weather vane in the shape of a fish on the ridge.
    val vane = Offset(rp.x + 0.03f * u, rp.y - 0.002f * u)
    fxLine(vane, Offset(vane.x, vane.y - 0.032f * u), FxC.iron, pen.lw)
    val turn = sin(t * 0.6f + f.id) * 0.7f
    val fish = Path().apply {
        moveTo(vane.x - 0.02f * u * cos(turn), vane.y - 0.032f * u)
        quadraticTo(vane.x, vane.y - 0.044f * u, vane.x + 0.02f * u * cos(turn), vane.y - 0.032f * u)
        quadraticTo(vane.x, vane.y - 0.022f * u, vane.x - 0.02f * u * cos(turn), vane.y - 0.032f * u)
        close()
    }
    inked(fish, FxC.mustard, pen, shade = false)
}

// ---------------------------------------------------------------------------------------------------- gate

/** An arbour with a gate in the back fence: a pointed arch of wood with climbing roses and a gate that swings open. */
internal fun DrawScope.gaGate(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val open = f.mode == 1
    val wood = FxC.oak
    val half = 0.15f
    val top = -0.27f
    // Behind the gate: the lane, when it stands open.
    if (open) {
        drawRect(Brush.verticalGradient(0f to Color(0xFFBFE8F4), 1f to Color(0xFFD9F0B8), startY = top * u, endY = 0f), Offset(-half * u + 0.01f * u, top * u + 0.03f * u), Size((2f * half - 0.02f) * u, -top * u - 0.03f * u))
        drawRect(Color(0xFFC9A66E), Offset(-half * u + 0.01f * u, -0.04f * u), Size((2f * half - 0.02f) * u, 0.04f * u))
        drawCircle(Color(0xFF3BC46B), 0.03f * u, Offset(0.07f * u, -0.07f * u))
    }
    // The posts and the pointed arch.
    for (s in floatArrayOf(-1f, 1f)) {
        inkedRound(Rect((s * half - 0.011f) * u, top * u, (s * half + 0.011f) * u, 0f), 0.003f * u, wood, pen, shade = true)
        inkedRound(Rect((s * half - 0.015f) * u, (top - 0.012f) * u, (s * half + 0.015f) * u, (top + 0.004f) * u), 0.003f * u, FxC.oakDark, pen, shade = false)
    }
    val arch = Path().apply {
        moveTo(-half * u, (top + 0.004f) * u)
        quadraticTo(-half * u, (top - 0.045f) * u, 0f, (top - 0.05f) * u)
        quadraticTo(half * u, (top - 0.045f) * u, half * u, (top + 0.004f) * u)
    }
    drawPath(arch, Ink.line, style = Stroke(0.021f * u, cap = StrokeCap.Round))
    drawPath(arch, wood, style = Stroke(0.0145f * u, cap = StrokeCap.Round))
    // The gate: pickets between two rails, hinged on the left. Open, it stands narrow and slanting.
    val gw = if (open) 0.075f else 2f * half - 0.03f
    val gx = -half + 0.015f
    val gt = -0.2f
    val gb = -0.025f
    val shear = if (open) 0.03f else 0f
    val gate = Path().apply {
        moveTo(gx * u, gb * u)
        lineTo(gx * u, gt * u)
        lineTo((gx + gw) * u, (gt - shear) * u)
        lineTo((gx + gw) * u, (gb + shear * 0.4f) * u)
        close()
    }
    drawPath(gate, FxC.paint)
    val pickets = if (open) 3 else 8
    for (k in 0..pickets) {
        val x = gx + gw * k / pickets
        val ty = gt - shear * k / pickets
        val by = gb + shear * 0.4f * k / pickets
        drawLine(Ink.line, Offset(x * u, by * u), Offset(x * u, ty * u), strokeWidth = pen.lw * 0.7f)
    }
    drawLine(Ink.line, Offset(gx * u, (gt + 0.03f) * u), Offset((gx + gw) * u, (gt + 0.03f - shear) * u), strokeWidth = pen.lw * 1.5f)
    drawLine(Ink.line, Offset(gx * u, (gb - 0.03f) * u), Offset((gx + gw) * u, (gb - 0.03f + shear * 0.4f) * u), strokeWidth = pen.lw * 1.5f)
    drawPath(gate, Ink.line, style = pen.stroke)
    for (k in 0..pickets) {
        val x = gx + gw * (k + 0.5f) / (pickets + 1f)
        val ty = gt - shear * k / pickets
        inked(fxPoly(u, x - 0.012f, ty, x, ty - 0.016f, x + 0.012f, ty), FxC.paint, pen, shade = false)
    }
    if (!open) {
        inkedCircle(Offset((gx + gw - 0.018f) * u, (gt + gb) / 2f * u), 0.007f * u, FxC.brass, pen, shade = false)
    }
    // Roses climbing the posts and the arch, swaying a little.
    val sway = sin(t * 0.9f + f.id) * 0.003f * u
    for (k in 0 until 14) {
        val a = k / 13f
        val rx = -half + 2f * half * a
        val ry = top - 0.05f * (1f - (2f * a - 1f) * (2f * a - 1f)) + 0.006f * sin(k * 2.1f)
        val c = Offset(rx * u + sway * (if (k % 2 == 0) 1f else -1f), (ry + 0.004f * (k % 3)) * u)
        drawPath(fxLeaf(c.x, c.y, c.x + 0.022f * u * (if (k % 2 == 0) 1f else -1f), c.y + 0.012f * u, 0.5f), GaK.leaf)
        if (k % 2 == 1) {
            inkedCircle(c, 0.0105f * u, if (k % 4 == 1) FxC.pink else Color(0xFFFFB3C7), pen, shade = false)
            drawCircle(Ink.line, 0.0032f * u, c, alpha = 0.55f, style = pen.thin)
        }
    }
    for (s in floatArrayOf(-1f, 1f)) {
        for (k in 0 until 5) {
            val c = Offset((s * half + s * 0.006f * (k % 2)) * u, (top + 0.04f * k + 0.012f) * u)
            drawPath(fxLeaf(c.x, c.y, c.x + s * 0.02f * u, c.y + 0.012f * u, 0.5f), GaK.leafLight)
            if (k % 2 == 0) inkedCircle(c, 0.0095f * u, FxC.pink, pen, shade = false)
        }
    }
    // A little lantern from the top of the arch.
    val lan = Offset(0f, (top - 0.02f) * u)
    fxLine(Offset(0f, (top - 0.045f) * u), lan, Ink.line, pen.lw * 0.6f)
    fxGlow(Offset(lan.x, lan.y + 0.014f * u), 0.12f * u, FxC.warm, 0.1f + 0.7f * pen.night)
    inkedRound(Rect(-0.008f * u, lan.y, 0.008f * u, lan.y + 0.024f * u), 0.003f * u, lerp(Color(0xFFFFF2B0), Color(0xFFFFD66B), pen.night), pen, shade = false)
}

// ------------------------------------------------------------------------------------------------ snowman

/** The garden's snowman: three balls, a carrot nose, coal eyes and buttons, a red scarf, twig arms and a beanie. */
internal fun DrawScope.gaSnowman(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    gmShadow(u, 0.16f, 0.1f, 0.6f)
    val wob = sin(t * 1.4f) * 0.002f * u + f.anim * 0.01f * u * sin(f.anim * 18f)
    translate(wob, 0f) {
        val snow = FxC.snow
        inkedCircle(Offset(0f, -0.05f * u), 0.056f * u, snow, pen)
        inkedCircle(Offset(0.002f * u, -0.128f * u), 0.04f * u, snow, pen)
        inkedCircle(Offset(0f, -0.2f * u), 0.03f * u, snow, pen)
        // Twig arms.
        for (s in floatArrayOf(-1f, 1f)) {
            capsule(Offset(s * 0.036f * u, -0.135f * u), Offset(s * 0.1f * u, -0.18f * u + s * 0.01f * u), 0.0045f * u, Color(0xFF6E4A30), pen)
            capsule(Offset(s * 0.085f * u, -0.17f * u), Offset(s * 0.1f * u, -0.2f * u), 0.003f * u, Color(0xFF6E4A30), pen)
        }
        // The face.
        drawCircle(Ink.line, 0.0042f * u, Offset(-0.011f * u, -0.205f * u))
        drawCircle(Ink.line, 0.0042f * u, Offset(0.011f * u, -0.205f * u))
        inked(fxPoly(u, -0.004f, -0.198f, 0.034f, -0.19f, -0.004f, -0.186f), Color(0xFFFF8A2E), pen, shade = false)
        for (k in 0 until 5) drawCircle(Ink.line, 0.0026f * u, Offset((-0.016f + k * 0.008f) * u, (-0.178f + 0.006f * sin(k / 4f * 3.1416f) * -1f) * u))
        for (k in 0..2) drawCircle(Ink.line, 0.0045f * u, Offset(0.002f * u, (-0.145f + k * 0.032f) * u))
        // The scarf and the beanie.
        val scarf = Path().apply {
            moveTo(-0.03f * u, -0.17f * u)
            quadraticTo(0f, -0.158f * u, 0.032f * u, -0.17f * u)
            lineTo(0.034f * u, -0.158f * u)
            quadraticTo(0f, -0.146f * u, -0.032f * u, -0.158f * u)
            close()
        }
        inked(scarf, Color(0xFFD2443A), pen, shade = false)
        inked(fxPoly(u, 0.018f, -0.157f, 0.034f, -0.157f, 0.036f, -0.12f, 0.022f, -0.12f), Color(0xFFD2443A), pen, shade = false)
        for (k in 0..2) drawLine(Color.White, Offset(0.02f * u, (-0.15f + k * 0.012f) * u), Offset(0.035f * u, (-0.15f + k * 0.012f) * u), strokeWidth = pen.lw * 1.1f)
        val beanie = Path().apply {
            moveTo(-0.03f * u, -0.225f * u)
            quadraticTo(-0.03f * u, -0.265f * u, 0f, -0.265f * u)
            quadraticTo(0.03f * u, -0.265f * u, 0.03f * u, -0.225f * u)
            close()
        }
        inked(beanie, Color(0xFF2F6FB8), pen)
        drawRect(Color(0xFF2F6FB8).darken(0.2f), Offset(-0.032f * u, -0.232f * u), Size(0.064f * u, 0.01f * u))
        inkedCircle(Offset(0f, -0.272f * u), 0.0085f * u, Color.White, pen, shade = false)
    }
}

// ------------------------------------------------------------------------------------------------- the door

/**
 * The back door of the house, as seen from the garden: a teal door with a glass top, a brass knocker and a cat
 * flap, under a little gabled hood with a lamp, on two stone steps with a doormat and a pair of boots. When it
 * is used the door swings ajar and the warm light of the hall spills out.
 */
internal fun DrawScope.gaDoor(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val n = pen.night
    val ajar = (f.anim * 1.4f).coerceIn(0f, 1f)
    val teal = Color(0xFF2F8F94)
    val trim = FxC.paint
    // Steps and a doormat in front.
    fxBox(u, -0.2f, -0.018f, 0.2f, 0f, 0.1f, FxC.stone, pen, rad = 0.004f, z = -0.1f, top = FxC.stone.lighten(0.14f), side = FxC.stone.darken(0.2f))
    fxBox(u, -0.17f, -0.034f, 0.17f, -0.018f, 0.08f, FxC.stone.lighten(0.05f), pen, rad = 0.004f, z = -0.04f, top = FxC.stone.lighten(0.18f), side = FxC.stone.darken(0.2f))
    val mat = fxFlat(u, -0.1f, 0.1f, -0.0185f, -0.095f, -0.03f, 0.006f)
    fxFace(mat, Color(0xFFB8342B), pen)
    for (k in 1..4) fxLine(q(-0.1f + k * 0.04f, -0.0186f, -0.093f), q(-0.1f + k * 0.04f, -0.0186f, -0.032f), Color(0xFFFFE9A8), pen.lw * 0.8f)
    // The door frame and the leaf.
    val frame = Rect(-0.11f * u, -0.47f * u, 0.11f * u, -0.034f * u)
    inkedRound(Rect(frame.left - 0.01f * u, frame.top - 0.012f * u, frame.right + 0.01f * u, frame.bottom), 0.004f * u, trim, pen, shade = false)
    // The hall behind, warm when the door is ajar or the day is dark.
    drawRect(Brush.verticalGradient(0f to Color(0xFFFFE9A8), 1f to Color(0xFFE0A860), startY = frame.top, endY = frame.bottom), frame.topLeft, frame.size)
    val leafW = frame.width * (1f - 0.62f * ajar)
    val leaf = Rect(frame.left, frame.top, frame.left + leafW, frame.bottom)
    inkedRound(leaf, 0.003f * u, teal, pen, shade = false)
    // Four panels with raised edges, and a glass top with a warm night glow.
    val pw = leaf.width
    val glass = Rect(leaf.left + pw * 0.14f, leaf.top + 0.03f * u, leaf.right - pw * 0.14f, leaf.top + 0.14f * u)
    drawRect(Brush.verticalGradient(0f to lerp(GaK.glass, Color(0xFFFFD66B), n), 1f to lerp(GaK.glassEdge, Color(0xFFFFB84A), n), startY = glass.top, endY = glass.bottom), glass.topLeft, glass.size)
    drawLine(trim, Offset(glass.center.x, glass.top), Offset(glass.center.x, glass.bottom), strokeWidth = pen.lw * 1.3f)
    drawLine(trim, Offset(glass.left, glass.center.y), Offset(glass.right, glass.center.y), strokeWidth = pen.lw * 1.3f)
    drawRect(Ink.line, glass.topLeft, glass.size, style = pen.thin)
    for (k in 0..1) {
        val p = Rect(leaf.left + pw * 0.12f, leaf.top + (0.18f + k * 0.12f) * u, leaf.right - pw * 0.12f, leaf.top + (0.27f + k * 0.12f) * u)
        drawRect(teal.darken(0.2f), p.topLeft, p.size, style = Stroke(pen.lw * 0.9f))
        drawRect(teal.lighten(0.12f), Offset(p.left + pen.lw, p.top + pen.lw), Size(p.width - 2 * pen.lw, p.height - 2 * pen.lw), alpha = 0.5f)
    }
    // Knocker, handle and the cat flap with a tiny cat's face peeking now and then.
    inkedCircle(Offset(leaf.left + pw * 0.5f, leaf.top + 0.145f * u), 0.0075f * u, FxC.brass, pen, shade = false)
    capsule(Offset(leaf.right - pw * 0.14f, leaf.top + 0.24f * u), Offset(leaf.right - pw * 0.14f, leaf.top + 0.3f * u), 0.005f * u, FxC.brass, pen)
    val flap = Rect(leaf.left + pw * 0.28f, leaf.bottom - 0.07f * u, leaf.right - pw * 0.28f, leaf.bottom - 0.02f * u)
    inkedRound(flap, 0.004f * u, FxC.paint, pen, shade = false)
    drawRect(Color(0xFF2A1E26), Offset(flap.left + 0.008f * u, flap.top + 0.008f * u), Size(flap.width - 0.016f * u, flap.height - 0.016f * u))
    if (fxFrac(t / 9f) in 0.06f..0.2f) {
        val c = Offset(flap.center.x, flap.center.y + 0.004f * u)
        inkedCircle(c, 0.0095f * u, Color(0xFFFFC37A), pen, shade = false)
        inked(fxPoly(1f, c.x - 0.009f * u, c.y - 0.004f * u, c.x - 0.007f * u, c.y - 0.016f * u, c.x - 0.002f * u, c.y - 0.008f * u), Color(0xFFFFC37A), pen, shade = false)
        inked(fxPoly(1f, c.x + 0.009f * u, c.y - 0.004f * u, c.x + 0.007f * u, c.y - 0.016f * u, c.x + 0.002f * u, c.y - 0.008f * u), Color(0xFFFFC37A), pen, shade = false)
    }
    // Light spilling out of the open door onto the steps.
    if (ajar > 0.05f) {
        val spill = Path().apply {
            moveTo(frame.left, frame.bottom)
            lineTo(frame.right, frame.bottom)
            lineTo(frame.right + 0.08f * u, 0.02f * u)
            lineTo(frame.left - 0.08f * u, 0.02f * u)
            close()
        }
        drawPath(spill, Color(0xFFFFE9A8), alpha = 0.35f * ajar)
    }
    // The hood: a little gabled roof on two brackets, and a lamp on the wall beside.
    val hl = q(-0.15f, -0.47f, -0.03f)
    val hr = q(0.15f, -0.47f, -0.03f)
    val hp = q(0f, -0.54f, -0.03f)
    val hlb = q(-0.15f, -0.47f, 0.06f)
    val hrb = q(0.15f, -0.47f, 0.06f)
    val hpb = q(0f, -0.54f, 0.06f)
    fxFace(fxPath(hp, hpb, hrb, hr), Color(0xFF59627F).lighten(0.1f), pen)
    fxFace(fxPath(hl, hp, hr), Color(0xFF59627F), pen)
    drawLine(trim, hl, hr, strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
    for (s in floatArrayOf(-0.11f, 0.11f)) capsule(q(s, -0.43f, 0f), q(s * 1.3f, -0.47f, -0.02f), 0.005f * u, FxC.oakDark, pen)
    val lampC = Offset(0.17f * u, -0.37f * u)
    fxGlow(lampC, 0.15f * u, FxC.warm, 0.1f + 0.75f * n)
    capsule(Offset(0.15f * u, -0.37f * u), lampC, 0.004f * u, FxC.iron, pen)
    inkedRound(Rect(lampC.x - 0.009f * u, lampC.y - 0.001f * u, lampC.x + 0.009f * u, lampC.y + 0.026f * u), 0.003f * u, lerp(Color(0xFFFFF2B0), Color(0xFFFFD66B), n), pen, shade = false)
    // A pair of boots on the mat, and a flower pot on the step.
    for ((k, bc) in listOf(Color(0xFFFFC83D), Color(0xFF2F6FB8)).withIndex()) {
        val b = q(-0.13f + k * 0.045f, -0.036f, -0.04f)
        inkedRound(Rect(b.x - 0.014f * u, b.y - 0.044f * u, b.x + 0.012f * u, b.y), 0.005f * u, bc, pen, shade = true)
        inkedRound(Rect(b.x - 0.016f * u, b.y - 0.01f * u, b.x + 0.024f * u, b.y), 0.004f * u, bc.darken(0.15f), pen, shade = false)
    }
}
