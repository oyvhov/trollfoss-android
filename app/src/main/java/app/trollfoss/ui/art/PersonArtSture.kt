package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp as lerpOffset
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
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Face
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/*
 * Sture, the shy ghost: a pale, slightly see-through sheet with a round head, big dark eyes, blushing
 * cheeks, a wavy hem and two tiny arms. He floats a hand's breadth above the floor (the engine adds a slow
 * bob), waves shyly, hides his eyes behind his arms now and then, squints before a sneeze, and holds
 * things in front of his tummy. Everything is layered pale colour with alpha: no blur, no layers.
 */

private val Aura = Color(0xFFBDF1FF)
private val Cool = Color(0xFF8DA2D6)
private val GhostMouth = Color(0xFF6B2C4E)
private val GhostTongue = Color(0xFFFF8FA8)
private val Blush = Color(0xFFFF8FB0)

/** Sture: the hem floats at -0.14 h, the top of his head at -0.99 h, his eyes at -0.705 h. */
internal fun DrawScope.sture(look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, holding: Boolean, seed: Float) {
    val t = if (a.motion) pen.t + seed else 0f
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val tint = argb(Palette.furFor(Species.GHOST, look.skin))
    val face = a.face
    val sleeping = face == Face.SLEEP
    val sit = pose == Pose.SIT
    val held = pose == Pose.HELD
    val dancing = a.dance > 0f
    val walking = pose == Pose.STAND && !a.walkTo.isNaN()
    val sneezing = a.sneeze > 0f || a.achoo > 0f
    val laughing = face == Face.LAUGH || a.tickle > 0f
    val flutter = (if (held) 2.4f else if (laughing || dancing) 1.7f else if (walking) 1.35f else 1f) * (if (sit) 0.3f else if (sleeping) 0.5f else 1f)

    // ---- shy: now and then he hides his eyes behind his arms for a moment
    val idle = face == Face.HAPPY && !holding && (pose == Pose.STAND || sit) && !dancing && a.talk <= 0f && a.wave <= 0f && !sneezing
    val cycle = (t + 3f).mod(12f)
    val peek = if (idle && cycle < 1.3f) sin(PI.toFloat() * cycle / 1.3f).coerceIn(0f, 1f) else 0f

    // ---- a faint glow around him, stronger at night
    val glowAlpha = (0.2f + 0.2f * pen.night) * (if (sleeping) 0.6f else 1f)
    drawCircle(
        safeRadialGradient(listOf(Aura.copy(alpha = glowAlpha), Aura.copy(alpha = 0f)), o(0f, -0.55f), 0.68f * h),
        0.68f * h,
        o(0f, -0.55f),
    )

    // ---- the sheet
    val body = sheetPath(h, t, flutter)
    ghostSheet(body, tint, pen, h)
    clipPath(body) {
        // Soft folds and a shine on the dome.
        rotate(-14f, pivot = o(-0.22f, -0.8f)) {
            drawOval(Color.White.copy(alpha = 0.55f), o(-0.27f, -0.93f), Size(0.085f * h, 0.19f * h))
        }
        val foldL = Path().apply {
            moveTo(-0.19f * h, -0.5f * h)
            quadraticTo(-0.27f * h, -0.36f * h, -0.25f * h, -0.17f * h)
        }
        val foldR = Path().apply {
            moveTo(0.16f * h, -0.5f * h)
            quadraticTo(0.26f * h, -0.34f * h, 0.24f * h, -0.17f * h)
        }
        drawPath(foldL, Cool.copy(alpha = 0.3f), style = pen.thin)
        drawPath(foldR, Cool.copy(alpha = 0.3f), style = pen.thin)
    }

    // ---- face
    val ink = Ink.line
    val lx = a.lookX * 0.016f + (if (peek > 0f) -0.01f * peek else 0f)
    val ly = a.lookY * 0.012f + 0.01f * peek
    val closedHappy = face == Face.GRIN || face == Face.LAUGH || face == Face.YUM
    for (side in SIDES) {
        val e = o(side * 0.12f + lx, -0.705f + ly)
        when {
            sneezing -> {
                val dir = -side
                val chevron = Path().apply {
                    moveTo(e.x - dir * 0.04f * h, e.y - 0.045f * h)
                    lineTo(e.x + dir * 0.03f * h, e.y)
                    lineTo(e.x - dir * 0.04f * h, e.y + 0.045f * h)
                }
                drawPath(chevron, ink, style = Stroke(pen.lw * 1.5f, cap = StrokeCap.Round))
            }
            face == Face.DIZZY -> {
                rotate(t * 400f * side, pivot = e) {
                    drawArc(ink, 0f, 300f, false, Offset(e.x - 0.045f * h, e.y - 0.045f * h), Size(0.09f * h, 0.09f * h), style = Stroke(pen.lw, cap = StrokeCap.Round))
                    drawArc(ink, 0f, 270f, false, Offset(e.x - 0.02f * h, e.y - 0.02f * h), Size(0.04f * h, 0.04f * h), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
                }
            }
            closedHappy -> {
                val arc = Path().apply {
                    moveTo(e.x - 0.05f * h, e.y + 0.02f * h)
                    quadraticTo(e.x, e.y - 0.06f * h, e.x + 0.05f * h, e.y + 0.02f * h)
                }
                drawPath(arc, ink, style = Stroke(pen.lw * 1.5f, cap = StrokeCap.Round))
            }
            sleeping || a.blink > 0f || peek > 0.5f -> {
                val arc = Path().apply {
                    moveTo(e.x - 0.05f * h, e.y - 0.006f * h)
                    quadraticTo(e.x, e.y + 0.04f * h, e.x + 0.05f * h, e.y - 0.006f * h)
                }
                drawPath(arc, ink, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
            }
            else -> {
                val big = if (face == Face.WOW) 1.3f else if (face == Face.OOH) 1.16f else 1f
                val ew = 0.105f * h * big
                val eh = 0.15f * h * big
                drawOval(ink, Offset(e.x - ew / 2f, e.y - eh / 2f), Size(ew, eh))
                // Two shines make the eyes big and wet.
                drawCircle(Color.White, 0.021f * h * big, Offset(e.x - ew * 0.2f, e.y - eh * 0.22f))
                drawCircle(Color.White.copy(alpha = 0.85f), 0.009f * h * big, Offset(e.x + ew * 0.2f, e.y + eh * 0.25f))
            }
        }
    }
    // Cheeks blush more when he is shy or laughing.
    val blush = Blush.copy(alpha = (0.5f + 0.3f * peek + (if (laughing || a.wave > 0f) 0.25f else 0f)).coerceAtMost(0.9f))
    drawOval(blush, o(-0.265f, -0.65f), Size(0.098f * h, 0.054f * h))
    drawOval(blush, o(0.167f, -0.65f), Size(0.098f * h, 0.054f * h))
    ghostMouth(a, h, pen, t, sneezing)

    // ---- tiny arms
    val armColor = tint.darken(0.05f)
    val shoulderL = o(-0.285f, -0.52f)
    val shoulderR = o(0.285f, -0.52f)
    var handL = o(-0.372f, -0.41f + sin(t * 2f) * 0.01f)
    var handR = o(0.372f, -0.41f + sin(t * 2f + 1f) * 0.01f)
    when {
        peek > 0.01f -> {
            val k = smooth(peek * 1.4f)
            handL = lerpOffset(handL, o(-0.085f, -0.7f), k)
            handR = lerpOffset(handR, o(0.085f, -0.7f), k)
        }
        sneezing -> {
            handL = o(-0.075f, -0.62f)
            handR = o(0.075f, -0.62f)
        }
        held -> {
            handL = o(-0.41f, -0.8f + sin(t * 11f) * 0.03f)
            handR = o(0.41f, -0.8f - sin(t * 11f) * 0.03f)
        }
        pose == Pose.FLOAT -> {
            handL = o(-0.44f, -0.55f + sin(t * 3f) * 0.03f)
            handR = o(0.44f, -0.55f - sin(t * 3f) * 0.03f)
        }
        dancing -> {
            handL = o(-0.40f, -0.52f + sin(a.dance) * 0.18f)
            handR = o(0.40f, -0.52f - sin(a.dance) * 0.18f)
        }
        // Held things ride on his right arm, a little out from his tummy so they never hide his face.
        a.wave > 0f -> handR = o(0.405f + sin(t * 14f) * 0.03f, -0.7f)
        face == Face.OOH || face == Face.WOW -> {
            handL = o(-0.42f, -0.62f)
            handR = o(0.42f, -0.62f)
        }
    }
    if (holding) {
        handR = o(.27f, -.38f)
        if (a.wave > 0f) handL = o(-.405f - sin(t * 14f) * .03f, -.7f)
    }
    capsule(shoulderL, handL, 0.074f * h, armColor, pen)
    capsule(shoulderR, handR, 0.074f * h, armColor, pen)

    // ---- a snore bubble when he sleeps
    if (sleeping) {
        val s = 0.026f + 0.02f * (0.5f + 0.5f * sin(t * 1.6f))
        val c = o(0.06f, -0.585f)
        drawCircle(Color.White.copy(alpha = 0.6f), s * h, c)
        drawCircle(ink.copy(alpha = 0.6f), s * h, c, style = pen.thin)
    }
}

/** His sheet: a dome on top, a bell below, and a hem of five scallops that ripples by itself. */
internal fun sheetPath(h: Float, t: Float, flutter: Float): Path {
    val hem = -0.2f
    val joint = FloatArray(6)
    for (j in 0..5) joint[j] = hem + sin(t * 2.1f + j * 1.25f) * 0.01f * flutter
    return Path().apply {
        moveTo(-0.38f * h, joint[5] * h)
        cubicTo(-0.345f * h, -0.40f * h, -0.31f * h, -0.52f * h, -0.31f * h, -0.68f * h)
        cubicTo(-0.31f * h, -0.90f * h, -0.17f * h, -0.99f * h, 0f, -0.99f * h)
        cubicTo(0.17f * h, -0.99f * h, 0.31f * h, -0.90f * h, 0.31f * h, -0.68f * h)
        cubicTo(0.31f * h, -0.52f * h, 0.345f * h, -0.40f * h, 0.38f * h, joint[0] * h)
        for (j in 0 until 5) {
            val x0 = 0.38f - j * 0.152f
            val x1 = 0.38f - (j + 1) * 0.152f
            val ctrl = hem + 0.115f + sin(t * 2.4f + j * 1.7f) * 0.02f * flutter
            quadraticTo((x0 + x1) / 2f * h, ctrl * h, x1 * h, joint[j + 1] * h)
        }
        close()
    }
}

/** The two-tone sheet: pale and a little see-through towards the hem, a cool shadow on the lower right, an ink rim. */
private fun DrawScope.ghostSheet(path: Path, tint: Color, pen: Pen, h: Float) {
    val top = -0.99f * h
    val bottom = -0.09f * h
    fun fill(c: Color): Brush = Brush.verticalGradient(
        0f to c.copy(alpha = 0.99f),
        0.6f to c.copy(alpha = 0.95f),
        1f to c.copy(alpha = 0.66f),
        startY = top,
        endY = bottom,
    )
    drawPath(path, fill(lerp(tint, Cool, 0.5f)))
    val s = h * 0.034f
    clipPath(path) {
        translate(-s * 0.5f, -s) { drawPath(path, fill(tint)) }
    }
    drawPath(
        path,
        Brush.verticalGradient(0f to Ink.line, 0.6f to Ink.line, 1f to Ink.line.copy(alpha = 0.72f), startY = top, endY = bottom),
        style = pen.stroke,
    )
}

private fun DrawScope.ghostMouth(a: PersonAnim, h: Float, pen: Pen, t: Float, sneezing: Boolean) {
    val m = Offset(0f, -0.598f * h)
    val face = a.face
    val chewing = a.chew > 0f || face == Face.CHOMP
    val talking = a.talk > 0f
    when {
        chewing || talking -> {
            val open = 0.012f + 0.034f * abs(sin(t * (if (chewing) 20f else 16f)))
            drawOval(GhostMouth, Offset(m.x - 0.03f * h, m.y - open * h / 2f), Size(0.06f * h, open * h))
            drawOval(Ink.line, Offset(m.x - 0.03f * h, m.y - open * h / 2f), Size(0.06f * h, open * h), style = pen.thin)
        }
        face == Face.GRIN || face == Face.LAUGH -> {
            val depth = (if (face == Face.LAUGH) 0.08f + 0.014f * abs(sin(t * 14f)) else 0.065f) * h
            val w = (if (face == Face.LAUGH) 0.065f else 0.055f) * h
            val top = m.y - 0.012f * h
            val mouth = Path().apply {
                moveTo(m.x - w, top)
                quadraticTo(m.x, top + 0.012f * h, m.x + w, top)
                quadraticTo(m.x + w * 0.9f, top + depth, m.x, top + depth)
                quadraticTo(m.x - w * 0.9f, top + depth, m.x - w, top)
                close()
            }
            drawPath(mouth, GhostMouth)
            clipPath(mouth) { drawOval(GhostTongue, Offset(m.x - 0.035f * h, top + depth - 0.04f * h), Size(0.07f * h, 0.06f * h)) }
            drawPath(mouth, Ink.line, style = pen.thin)
        }
        face == Face.OOH || face == Face.WOW || sneezing -> {
            val s = if (face == Face.WOW) 1.35f else 1f
            val r = Offset(m.x - 0.026f * h * s, m.y - 0.03f * h * s)
            drawOval(GhostMouth, r, Size(0.052f * h * s, 0.066f * h * s))
            drawOval(Ink.line, r, Size(0.052f * h * s, 0.066f * h * s), style = pen.thin)
        }
        face == Face.SLEEP -> {
            val s = 0.8f + 0.2f * sin(t * 1.6f)
            drawOval(GhostMouth, Offset(m.x - 0.014f * h * s, m.y - 0.012f * h * s), Size(0.028f * h * s, 0.026f * h * s))
        }
        face == Face.DIZZY -> {
            val wave = Path().apply {
                moveTo(m.x - 0.05f * h, m.y)
                quadraticTo(m.x - 0.025f * h, m.y - 0.026f * h, m.x, m.y)
                quadraticTo(m.x + 0.025f * h, m.y + 0.026f * h, m.x + 0.05f * h, m.y)
            }
            drawPath(wave, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        }
        else -> {
            val smile = Path().apply {
                moveTo(m.x - 0.042f * h, m.y - 0.01f * h)
                quadraticTo(m.x, m.y + 0.036f * h, m.x + 0.042f * h, m.y - 0.01f * h)
            }
            drawPath(smile, Ink.line, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
            if (face == Face.YUM) {
                val lick = sin(t * 9f) * 0.008f
                inkedOval(rect(m.x + 0.026f * h, m.y + (0.026f + lick) * h, 0.036f * h, 0.03f * h), GhostTongue, pen, shade = false)
            }
        }
    }
}
