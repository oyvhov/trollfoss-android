package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.domain.Anatomy
import app.trollfoss.domain.Face
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.PersonPlay
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val MouthDark = Color(0xFF7A2440)
private val Tongue = Color(0xFFFF7F9E)
private val EarPink = Color(0xFFFFA3BE)
private val ShirtWhite = Color(0xFFF7F4EE)

/**
 * Draws a figure with its origin at (0, 0): the feet when standing, the hips when sitting and the
 * middle of the back when lying (see [Anatomy]). [h] is the figure's height in pixels. [seed] offsets
 * the idle motion so figures do not breathe in step. [holding] keeps the right hand still where a held
 * thing is drawn.
 */
fun DrawScope.drawPerson(
    species: Species,
    look: Look,
    pose: Pose,
    anim: PersonAnim,
    h: Float,
    pen: Pen,
    holding: Boolean = false,
    seed: Float = 0f,
) {
    if (species == Species.ROBOT || species == Species.GHOST) {
        drawHouseFigure(species, look, pose, anim, h, pen, holding, seed)
        return
    }
    if (species != Species.FOLK) {
        drawPet(species, look, pose, anim, h, pen, seed)
        return
    }
    val safe = look.safe()
    when (pose) {
        Pose.LIE -> withTransform({
            translate(0f, -0.22f * h)
            rotate(-90f, Offset.Zero)
            translate(0f, 0.5f * h)
        }) { folk(safe, pose, anim, h, pen, holding, seed) }
        Pose.SIT -> withTransform({ translate(0f, Anatomy.HIPS * h) }) { folk(safe, pose, anim, h, pen, holding, seed) }
        else -> folk(safe, pose, anim, h, pen, holding, seed)
    }
}

/** The standing frame: feet at the origin, head centre at -0.70 h. */
private fun DrawScope.folk(look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, holding: Boolean, seed: Float) {
    val t = pen.t + seed
    fun o(x: Float, y: Float) = Offset(x * h, y * h)

    val skin = argb(Palette.skins[look.skin])
    val hair = argb(Palette.hairs[look.hairColor])
    val topColor = argb(Palette.cloth[look.topColor])
    val overalls = look.top == 4
    val bottomColor = if (overalls) topColor else argb(Palette.cloth[look.bottomColor])
    val shoe = argb(Palette.cloth[look.shoes])
    val sleeping = a.face == Face.SLEEP

    val breath = sin(t * 2.6f)
    val dancing = a.dance > 0f
    // Walking: feet lift in turn and the hands swing the other way, frontal so it reads as a waddle.
    val walking = pose == Pose.STAND && !a.walkTo.isNaN() && !dancing
    val stepL = if (walking) kotlin.math.max(0f, sin(a.walkPhase * PI.toFloat())) else 0f
    val stepR = if (walking) kotlin.math.max(0f, -sin(a.walkPhase * PI.toFloat())) else 0f
    val bob = when {
        dancing -> -abs(sin(a.dance)) * 0.03f
        pose == Pose.STAND -> breath * 0.004f
        else -> breath * 0.003f
    }

    // Long hair falls behind the clothes and hands, including at the slider extremes.
    withTransform({ translate(0f, bob * h) }) { backHair(look, hair, h, pen) }

    // ---- legs
    val legW = 0.092f
    val legColor = if (look.bottom == 0 || look.bottom == 3 || overalls) bottomColor else skin
    val kick = when (pose) {
        Pose.HELD -> sin(t * 13f) * 0.03f
        Pose.SWIM -> sin(t * 7f) * 0.02f
        else -> 0f
    }
    val danceStep = if (dancing) sin(a.dance) * 0.012f else 0f
    if (pose == Pose.SIT) {
        // Legs come towards us, so they look short; feet dangle.
        val swing = sin(t * 3f) * 0.012f
        for (side in listOf(-1f, 1f)) {
            val hip = o(side * 0.07f, -0.16f)
            val foot = o(side * 0.085f, -0.055f + side * swing)
            capsule(hip, foot, legW * h, legColor, pen)
            inkedOval(rect(foot.x, foot.y + 0.012f * h, 0.13f * h, 0.085f * h), shoe, pen)
        }
    } else {
        for (side in listOf(-1f, 1f)) {
            val lift = side * kick + (if (side < 0) danceStep else -danceStep) - (if (side < 0) stepL else stepR) * 0.05f
            val spread = if (pose == Pose.FLOAT) 0.9f else 1f
            val hip = o(side * 0.07f * spread, -0.16f + bob)
            val ankle = o(side * 0.078f * spread, -0.055f + lift)
            capsule(hip, ankle, legW * h, legColor, pen)
            if (look.bottom == 1 && !overalls) capsule(hip, o(side * 0.073f, -0.105f + bob), legW * h * 1.18f, bottomColor, pen)
            if (look.bottom == 3 && !overalls) inkedRound(Rect((side * 0.075f - 0.03f) * h, -0.14f * h, (side * 0.075f + 0.03f) * h, -0.095f * h), h * 0.008f, bottomColor.darken(0.15f), pen, shade = false)
            if (look.bottom == 4 && !overalls) for (k in 0..2) drawLine(argb(Palette.cloth[look.accent]), o(side * 0.078f - 0.04f, -0.105f + k * 0.026f + lift), o(side * 0.078f + 0.04f, -0.105f + k * 0.026f + lift), strokeWidth = h * 0.016f)
            inkedOval(rect(side * 0.086f * h, (-0.036f + lift) * h, 0.14f * h, 0.075f * h), shoe, pen)
            drawLine(shoe.lighten(0.6f), o(side * 0.086f - 0.05f, -0.012f + lift), o(side * 0.086f + 0.05f, -0.012f + lift), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        }
    }

    withTransform({ translate(0f, bob * h) }) {
        // ---- skirt sits under the top
        if (look.bottom == 2 && !overalls && look.top != 2) {
            val skirt = Path().apply {
                moveTo(-0.17f * h, -0.2f * h)
                lineTo(0.17f * h, -0.2f * h)
                quadraticTo(0.24f * h, -0.12f * h, 0.24f * h, -0.085f * h)
                quadraticTo(0f, -0.06f * h, -0.24f * h, -0.085f * h)
                quadraticTo(-0.24f * h, -0.12f * h, -0.17f * h, -0.2f * h)
                close()
            }
            inked(skirt, bottomColor, pen)
        }

        // ---- hood behind the neck
        if (look.top == 1) inkedOval(rect(0f, -0.475f * h, 0.36f * h, 0.13f * h), topColor.darken(0.12f), pen)
        if (look.top == 11) inked(blobPath(-0.15f * h, -0.49f * h, -0.3f * h, -0.12f * h, 0f, -0.07f * h, 0.3f * h, -0.12f * h, 0.15f * h, -0.49f * h), topColor.darken(0.12f), pen)

        // ---- torso
        val dress = look.top == 2
        val bottomY = if (dress) -0.075f else -0.125f
        val hipHalf = if (dress) 0.24f else 0.185f
        val torso = Path().apply {
            moveTo(-0.14f * h, -0.455f * h)
            quadraticTo(-0.15f * h, -0.49f * h, -0.1f * h, -0.49f * h)
            lineTo(0.1f * h, -0.49f * h)
            quadraticTo(0.15f * h, -0.49f * h, 0.14f * h, -0.455f * h)
            lineTo(hipHalf * h, (bottomY - 0.025f) * h)
            quadraticTo((hipHalf + 0.005f) * h, bottomY * h, (hipHalf - 0.03f) * h, bottomY * h)
            lineTo(-(hipHalf - 0.03f) * h, bottomY * h)
            quadraticTo(-(hipHalf + 0.005f) * h, bottomY * h, -hipHalf * h, (bottomY - 0.025f) * h)
            close()
        }
        val shirt = if (overalls || look.top == 6) ShirtWhite else topColor
        inked(torso, shirt, pen, outline = false)
        clipPath(torso) { garmentDetails(look, topColor, h, pen); folkPattern(look, h, pen) }
        drawPath(torso, Ink.line, style = pen.stroke)
        if (look.top == 0 || look.top == 3) {
            drawArc(Ink.line, 20f, 140f, false, o(-0.07f, -0.53f), Size(0.14f * h, 0.07f * h), style = pen.thin)
        }

        // ---- arms
        val shoulderL = o(-0.135f, -0.445f)
        val shoulderR = o(0.135f, -0.445f)
        var handL = o(-0.235f, -0.25f + breath * 0.004f)
        var handR = o(0.245f, -0.245f)
        when {
            pose == Pose.HELD -> {
                handL = o(-0.27f, -0.64f + sin(t * 11f) * 0.02f)
                if (!holding) handR = o(0.27f, -0.64f - sin(t * 11f) * 0.02f)
            }
            pose == Pose.FLOAT -> {
                handL = o(-0.32f, -0.44f + sin(t * 3f) * 0.03f)
                if (!holding) handR = o(0.32f, -0.44f - sin(t * 3f) * 0.03f)
            }
            pose == Pose.SWIM -> {
                handL = o(-0.29f, -0.4f + sin(t * 6f) * 0.05f)
                if (!holding) handR = o(0.29f, -0.4f - sin(t * 6f) * 0.05f)
            }
            dancing -> {
                handL = o(-0.27f, -0.36f + sin(a.dance) * 0.14f)
                if (!holding) handR = o(0.27f, -0.36f - sin(a.dance) * 0.14f)
            }
            a.wave > 0f -> {
                if (!holding) handR = o(0.28f + sin(t * 14f) * 0.03f, -0.66f) else handL = o(-0.28f + sin(t * 14f) * 0.03f, -0.66f)
            }
        }
        if (walking && !holding) {
            handL = o(-0.235f, -0.25f - (stepR - stepL) * 0.04f)
            handR = o(0.245f, -0.245f + (stepR - stepL) * 0.04f)
        } else if (walking) {
            handL = o(-0.235f, -0.25f - (stepR - stepL) * 0.04f)
        }
        PersonPlay.hand(a.activity)?.let {
            if (holding || a.activity == PersonPlay.BRUSH) handR = o(it[0], it[1])
            if (a.activity == PersonPlay.READ || a.activity == PersonPlay.HUG) handL = o(-0.14f, -0.35f)
        }
        val longSleeves = look.top in setOf(1, 5, 6, 7, 8, 9) || overalls
        val sleeve = if (overalls || look.top == 6) ShirtWhite else topColor
        for ((shoulder, hand) in listOf(shoulderL to handL, shoulderR to handR)) {
            val armColor = if (longSleeves) sleeve else skin
            capsule(shoulder, hand, 0.078f * h, armColor, pen)
            if (!longSleeves) {
                val end = Offset(shoulder.x + (hand.x - shoulder.x) * 0.42f, shoulder.y + (hand.y - shoulder.y) * 0.42f)
                capsule(shoulder, end, 0.104f * h, sleeve, pen)
            }
            if (look.top == 5) {
                val cuff = Offset(shoulder.x + (hand.x - shoulder.x) * 0.84f, shoulder.y + (hand.y - shoulder.y) * 0.84f)
                drawLine(ShirtWhite, cuff, Offset(shoulder.x + (hand.x - shoulder.x) * 0.9f, shoulder.y + (hand.y - shoulder.y) * 0.9f), strokeWidth = 0.078f * h, cap = StrokeCap.Butt)
            }
            inkedCircle(hand, 0.048f * h, skin, pen)
        }

        // ---- head
        val tilt = if (dancing) sin(a.dance) * 7f else if (pose == Pose.HELD) sin(t * 5f) * 5f else 0f
        rotate(tilt, pivot = o(0f, -0.47f)) {
            head(look, a, pose, h, pen, t, skin, hair, sleeping)
        }
    }
}

/** Patterns and pockets, drawn clipped to the torso. */
private fun DrawScope.garmentDetails(look: Look, color: Color, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    when (look.top) {
        1 -> {
            inkedRound(Rect(-0.1f * h, -0.27f * h, 0.1f * h, -0.17f * h), 0.03f * h, color.darken(0.08f), pen, shade = false)
            drawLine(Ink.line, o(-0.03f, -0.48f), o(-0.035f, -0.4f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
            drawLine(Ink.line, o(0.03f, -0.48f), o(0.035f, -0.4f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
        }
        2 -> drawLine(color.darken(0.25f), o(-0.2f, -0.3f), o(0.2f, -0.3f), strokeWidth = pen.lw * 1.2f)
        3 -> for (y in listOf(-0.43f, -0.35f, -0.27f, -0.19f)) {
            drawRect(Color.White.copy(alpha = 0.78f), o(-0.3f, y), Size(0.6f * h, 0.035f * h))
        }
        4 -> {
            val bib = Rect(-0.11f * h, -0.4f * h, 0.11f * h, -0.1f * h)
            drawRect(color, Offset(-0.3f * h, -0.2f * h), Size(0.6f * h, 0.2f * h))
            inkedRound(bib, 0.03f * h, color, pen, shade = false)
            drawLine(color, o(-0.09f, -0.39f), o(-0.12f, -0.5f), strokeWidth = 0.045f * h)
            drawLine(color, o(0.09f, -0.39f), o(0.12f, -0.5f), strokeWidth = 0.045f * h)
            drawCircle(Color(0xFFFFD23F), 0.018f * h, o(-0.07f, -0.37f))
            drawCircle(Color(0xFFFFD23F), 0.018f * h, o(0.07f, -0.37f))
            inkedRound(Rect(-0.05f * h, -0.33f * h, 0.05f * h, -0.26f * h), 0.012f * h, color.darken(0.1f), pen, shade = false)
        }
        5 -> {
            // Lusekofte: a white yoke with a band of rosettes, and a dotted band near the hem.
            drawRect(ShirtWhite, o(-0.3f, -0.5f), Size(0.6f * h, 0.105f * h))
            for (i in -3..3) rosette(o(i * 0.07f, -0.445f), 0.026f * h, color)
            drawLine(color.darken(0.2f), o(-0.3f, -0.395f), o(0.3f, -0.395f), strokeWidth = pen.lw * 0.7f)
            drawRect(ShirtWhite, o(-0.3f, -0.2f), Size(0.6f * h, 0.035f * h))
            for (i in -4..4) drawCircle(color, 0.009f * h, o(i * 0.045f, -0.1825f))
        }
        6 -> {
            // Bunad: a dark vest with rose embroidery over a white shirt, and a silver sølje at the collar.
            for (side in listOf(-1f, 1f)) {
                val vest = Path().apply {
                    moveTo(side * 0.035f * h, -0.49f * h)
                    lineTo(side * 0.16f * h, -0.47f * h)
                    lineTo(side * 0.22f * h, -0.09f * h)
                    lineTo(side * 0.05f * h, -0.09f * h)
                    quadraticTo(side * 0.07f * h, -0.3f * h, side * 0.035f * h, -0.49f * h)
                    close()
                }
                drawPath(vest, color)
                drawPath(vest, Ink.line, style = pen.thin)
                for (k in 0 until 4) {
                    val y = -0.43f + k * 0.09f
                    val x = side * (0.1f + k * 0.012f)
                    drawCircle(Color(0xFFFF5A4E), 0.016f * h, o(x, y))
                    drawCircle(Color(0xFFFFD23F), 0.007f * h, o(x, y))
                    drawCircle(Color(0xFF3BC46B), 0.009f * h, o(x + side * 0.025f, y + 0.02f))
                }
            }
            drawLine(Color(0xFFD9DDE8), o(0f, -0.47f), o(0f, -0.4f), strokeWidth = pen.lw * 0.8f)
            drawCircle(Color(0xFFE8ECF5), 0.03f * h, o(0f, -0.47f))
            drawCircle(Ink.line, 0.03f * h, o(0f, -0.47f), style = pen.thin)
            for (k in -1..1) drawCircle(Color(0xFFD9DDE8), 0.009f * h, o(k * 0.02f, -0.42f))
        }
        7, 8 -> {
            drawLine(Ink.line, o(0f, -0.48f), o(0f, -0.14f), strokeWidth = pen.lw * 0.75f)
            for (k in 0..3) inkedCircle(o(0.025f, -0.42f + k * 0.07f), h * 0.009f, argb(Palette.cloth[look.accent]), pen, shade = false)
            for (s in floatArrayOf(-1f, 1f)) inkedRound(Rect(o(s * 0.10f - 0.035f, -0.3f), o(s * 0.10f + 0.035f, -0.24f)), h * 0.008f, color.darken(0.1f), pen, shade = false)
        }
        9 -> for (k in -4..4) drawLine(color.darken(0.1f), o(k * 0.042f, -0.47f), o(k * 0.042f, -0.15f), strokeWidth = pen.lw * 0.5f)
        10 -> { drawLine(argb(Palette.cloth[look.accent]), o(-0.13f, -0.46f), o(-0.16f, -0.16f), strokeWidth = h * 0.04f); drawLine(argb(Palette.cloth[look.accent]), o(0.13f, -0.46f), o(0.16f, -0.16f), strokeWidth = h * 0.04f) }
        11 -> inkedCircle(o(0f, -0.45f), h * 0.025f, argb(Palette.cloth[look.accent]), pen)
    }
}

/** An eight-pointed Selbu rose, the classic lusekofte star. */
private fun DrawScope.rosette(center: Offset, r: Float, color: Color) {
    for (i in 0 until 4) {
        val angle = i * 45f
        rotate(angle, pivot = center) {
            drawOval(color, Offset(center.x - r * 0.28f, center.y - r), Size(r * 0.56f, r * 2f))
        }
    }
    drawCircle(ShirtWhite, r * 0.22f, center)
}

private fun DrawScope.backHair(look: Look, hair: Color, h: Float, pen: Pen) {
    val c = Offset(0f, -0.70f * h)
    val r = 0.238f * h
    withTransform({ scale(look.hairSize, look.hairSize, c) }) {
        if (look.hair >= 9) newHair(look.hair, true, hair, argb(Palette.cloth[look.accent]), c, r, pen, look.hairLength)
        else withTransform({ scale(1f, look.hairLength, Offset(c.x, c.y - r * 0.8f)) }) { hairBack(look.hair, hair, c, r, pen, h) }
    }
}

private fun DrawScope.head(look: Look, a: PersonAnim, pose: Pose, h: Float, pen: Pen, t: Float, skin: Color, hair: Color, sleeping: Boolean) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val c = o(0f, -0.70f)
    val r = 0.238f * h

    // Ears that sit on top of the head go behind it.
    when (look.ears) {
        1 -> for (side in listOf(-1f, 1f)) {
            val ear = Path().apply {
                moveTo((side * 0.22f) * h, -0.84f * h)
                lineTo((side * 0.19f) * h, -1.02f * h)
                lineTo((side * 0.07f) * h, -0.93f * h)
                close()
            }
            inked(ear, skin, pen)
            val inner = Path().apply {
                moveTo((side * 0.19f) * h, -0.87f * h)
                lineTo((side * 0.18f) * h, -0.97f * h)
                lineTo((side * 0.11f) * h, -0.92f * h)
                close()
            }
            drawPath(inner, EarPink)
        }
        2 -> for (side in listOf(-1f, 1f)) {
            val sway = sin(t * 2f + side) * 4f
            rotate(side * 8f + sway, pivot = o(side * 0.09f, -0.9f)) {
                inkedOval(Rect((side * 0.09f - 0.05f) * h, -1.24f * h, (side * 0.09f + 0.05f) * h, -0.86f * h), skin, pen)
                drawOval(EarPink, Offset((side * 0.09f - 0.025f) * h, -1.19f * h), Size(0.05f * h, 0.26f * h))
            }
        }
        3 -> for (side in listOf(-1f, 1f)) {
            inkedCircle(o(side * 0.19f, -0.9f), 0.075f * h, skin, pen)
            drawCircle(EarPink, 0.038f * h, o(side * 0.19f, -0.9f))
        }
    }
    // Side ears.
    when (look.ears) {
        0 -> for (side in listOf(-1f, 1f)) {
            inkedCircle(o(side * 0.245f, -0.685f), 0.052f * h, skin, pen)
            drawArc(skin.darken(0.2f), if (side < 0) 110f else -70f, 140f, false, o(side * 0.245f - 0.025f, -0.71f), Size(0.05f * h, 0.05f * h), style = pen.thin)
        }
        4 -> for (side in listOf(-1f, 1f)) {
            val ear = Path().apply {
                moveTo(side * 0.21f * h, -0.75f * h)
                quadraticTo(side * 0.33f * h, -0.78f * h, side * 0.39f * h, -0.86f * h)
                quadraticTo(side * 0.33f * h, -0.66f * h, side * 0.22f * h, -0.63f * h)
                close()
            }
            inked(ear, skin, pen)
        }
    }

    when (look.face) {
        1 -> inkedOval(rect(c.x, c.y, r * 1.85f, r * 2.08f), skin, pen)
        2 -> inkedRound(Rect(c.x - r, c.y - r * 0.96f, c.x + r, c.y + r * 0.96f), r * 0.58f, skin, pen)
        3 -> inkedOval(rect(c.x, c.y + r * 0.02f, r * 2.18f, r * 1.9f), skin, pen)
        else -> inkedOval(rect(c.x, c.y, r * 2f, r * 2.02f), skin, pen)
    }
    // Soft light on the forehead.
    drawOval(Color.White.copy(alpha = 0.16f), o(-0.15f, -0.9f), Size(0.2f * h, 0.1f * h))

    // Beard goes under the face features.
    if (look.extra == 2) {
        val beard = blobPath(
            -0.23f * h, -0.66f * h, -0.2f * h, -0.52f * h, -0.1f * h, -0.44f * h, 0f, -0.42f * h,
            0.1f * h, -0.44f * h, 0.2f * h, -0.52f * h, 0.23f * h, -0.66f * h, 0f, -0.6f * h,
        )
        inked(beard, hair, pen)
    }

    // Cheeks, nose, freckles
    drawOval(Ink.blush, o(-0.205f, -0.65f), Size(0.09f * h, 0.052f * h))
    drawOval(Ink.blush, o(0.115f, -0.65f), Size(0.09f * h, 0.052f * h))
    when (look.nose) {
        1 -> inkedOval(rect(0f, -0.653f * h, 0.062f * h, 0.045f * h), skin.darken(0.05f), pen, shade = false)
        2 -> { drawLine(skin.darken(0.3f), o(-0.005f, -0.68f), o(0.028f, -0.642f), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round); drawLine(skin.darken(0.3f), o(0.028f, -0.642f), o(-0.005f, -0.637f), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round) }
        3 -> inkedCircle(o(0f, -0.654f), h * 0.028f, Color(0xFFF37998), pen, shade = false)
        else -> drawOval(skin.darken(0.16f), o(-0.018f, -0.66f), Size(0.036f * h, 0.026f * h))
    }
    if (look.extra == 1 || look.extra == 4) {
        for (side in listOf(-1f, 1f)) {
            drawCircle(skin.darken(0.35f), 0.008f * h, o(side * 0.13f, -0.645f))
            drawCircle(skin.darken(0.35f), 0.008f * h, o(side * 0.155f, -0.625f))
            drawCircle(skin.darken(0.35f), 0.008f * h, o(side * 0.175f, -0.65f))
        }
    }
    if (look.extra == 4) for (k in -2..2) drawCircle(hair, h * 0.008f, o(k * 0.035f, -0.53f + abs(k) * 0.01f))
    if (look.extra == 5) for (s in floatArrayOf(-1f, 1f)) twinkle(o(s * 0.16f, -0.63f), h * 0.032f, argb(Palette.cloth[look.accent]), 1f)
    if (look.extra == 6) inkedCircle(o(-0.17f, -0.57f), h * 0.035f, skin.lighten(0.35f), pen, shade = false)
    if (look.extra == 7) for (s in floatArrayOf(-1f, 1f)) inkedCircle(o(s * 0.255f, -0.645f), h * 0.019f, argb(Palette.cloth[look.accent]), pen)
    if (look.extra == 8) for (s in floatArrayOf(-1f, 1f)) drawArc(skin.darken(0.3f), if (s < 0) 70f else -70f, 140f, false, o(s * 0.19f - 0.028f, -0.63f), Size(h * 0.055f, h * 0.04f), style = pen.thin)

    eyes(look, a, h, pen, t, skin, hair, sleeping)
    if (a.face == Face.HAPPY && a.chew <= 0f && a.talk <= 0f && look.mouth != 0) {
        when (look.mouth) {
            1 -> drawArc(Ink.line, 0f, 180f, false, o(-0.08f, -0.61f), Size(h * 0.16f, h * 0.1f), style = pen.thin)
            2 -> { inkedOval(rect(0f, -0.57f * h, h * 0.095f, h * 0.058f), MouthDark, pen, shade = false); inkedRound(Rect(o(-0.035f, -0.597f), o(0.035f, -0.572f)), h * 0.006f, Color.White, pen, shade = false) }
            3 -> drawArc(Ink.line, 10f, 140f, false, o(-0.025f, -0.59f), Size(h * 0.09f, h * 0.045f), style = pen.thin)
            else -> inkedOval(rect(0f, -0.575f * h, h * 0.055f, h * 0.04f), Color(0xFFC5647E), pen, shade = false)
        }
    } else mouth(a, h, pen, t)
    if (look.extra == 3) {
        for (side in listOf(-1f, 1f)) {
            inkedOval(Rect(if (side < 0) -0.085f * h else 0.005f * h, -0.625f * h, if (side < 0) -0.005f * h else 0.085f * h, -0.585f * h), hair, pen)
        }
    }
    withTransform({ scale(look.hairSize, look.hairSize, c) }) {
        if (look.hair >= 9) newHair(look.hair, false, hair, argb(Palette.cloth[look.accent]), c, r, pen, look.hairLength)
        else hairFront(look.hair, hair, c, r, pen, h, t)
    }
}

private fun DrawScope.eyes(look: Look, a: PersonAnim, h: Float, pen: Pen, t: Float, skin: Color, hair: Color, sleeping: Boolean) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val face = a.face
    val lift = if (face == Face.OOH || face == Face.WOW) -0.025f else 0f
    // Brows
    for (side in listOf(-1f, 1f)) {
        val bx = side * 0.1f
        val tiltBrow = if (face == Face.DIZZY) side * 0.01f else 0f
        drawLine(hair.darken(0.3f), o(bx - 0.035f, -0.8f + lift + tiltBrow), o(bx + 0.035f, -0.805f + lift - tiltBrow), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
    }
    val closed = sleeping || a.blink > 0f
    for (side in listOf(-1f, 1f)) {
        val e = o(side * 0.095f * look.eyeSpacing, -0.705f)
        withTransform({ scale(look.eyeSize, look.eyeSize, e) }) {
        when {
            face == Face.LAUGH || face == Face.GRIN -> {
                val arc = Path().apply {
                    moveTo(e.x - 0.05f * h, e.y + 0.012f * h)
                    quadraticTo(e.x, e.y - 0.05f * h, e.x + 0.05f * h, e.y + 0.012f * h)
                }
                drawPath(arc, Ink.line, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
            }
            face == Face.DIZZY -> {
                val spin = t * 400f
                rotate(spin * side, pivot = e) {
                    drawArc(Ink.line, 0f, 300f, false, Offset(e.x - 0.04f * h, e.y - 0.04f * h), Size(0.08f * h, 0.08f * h), style = Stroke(pen.lw, cap = StrokeCap.Round))
                    drawArc(Ink.line, 0f, 270f, false, Offset(e.x - 0.018f * h, e.y - 0.018f * h), Size(0.036f * h, 0.036f * h), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
                }
            }
            closed -> {
                val arc = Path().apply {
                    moveTo(e.x - 0.05f * h, e.y - 0.004f * h)
                    quadraticTo(e.x, e.y + 0.035f * h, e.x + 0.05f * h, e.y - 0.004f * h)
                }
                drawPath(arc, Ink.line, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
            }
            look.eyes == 4 -> {
                drawOval(argb(Palette.eyes[look.eyeColor]), Offset(e.x - 0.036f * h + a.lookX * 0.012f * h, e.y - 0.05f * h + a.lookY * 0.012f * h), Size(0.072f * h, 0.1f * h))
                drawCircle(Ink.line, h * 0.025f, e)
                drawCircle(Color.White, 0.014f * h, Offset(e.x - 0.01f * h + a.lookX * 0.012f * h, e.y - 0.022f * h))
            }
            else -> {
                val big = if (face == Face.WOW || face == Face.OOH) 1.15f else 1f
                val ey = if (look.eyes == 5) 0.047f else if (look.eyes == 6) 0.060f else 0.065f
                val white = Rect(e.x - 0.061f * h * big, e.y - ey * h * big, e.x + 0.061f * h * big, e.y + ey * h * big)
                drawOval(Color.White, white.topLeft, white.size)
                drawOval(Ink.line, white.topLeft, white.size, style = pen.thin)
                val p = Offset(e.x + a.lookX * 0.022f * h, e.y + a.lookY * 0.026f * h + 0.006f * h)
                val pupil = (if (face == Face.WOW) 0.026f else 0.038f) * h
                drawCircle(argb(Palette.eyes[look.eyeColor]), pupil * 1.24f, p)
                if (look.eyes == 7) twinkle(p, pupil * 0.85f, Ink.line, 1f) else drawCircle(Ink.line, pupil * 0.58f, p)
                drawCircle(Color.White, pupil * 0.36f, Offset(p.x - pupil * 0.35f, p.y - pupil * 0.4f))
                if (look.eyes == 2) {
                    // Heavy lids: the top of the eye is covered by skin.
                    clipPath(ovalPath(white)) {
                        drawRect(skin.darken(0.06f), white.topLeft, Size(white.width, white.height * 0.42f))
                    }
                    drawLine(Ink.line, Offset(white.left, white.top + white.height * 0.42f), Offset(white.right, white.top + white.height * 0.42f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
                }
                if (look.eyes == 3) {
                    val outer = e.x + side * 0.05f * h
                    for (k in 0 until 3) {
                        val ang = Math.toRadians((-60.0 - k * 25.0) * (if (side < 0) -1 else 1) + if (side < 0) 180.0 else 0.0)
                        val from = Offset(outer - side * 0.01f * h * k, e.y - 0.06f * h)
                        drawLine(Ink.line, from, Offset(from.x + side * 0.03f * h * cos(Math.toRadians(40.0 + k * 20)).toFloat(), from.y - 0.03f * h * sin(Math.toRadians(40.0 + k * 20)).toFloat()), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
                        ang.hashCode()
                    }
                }
            }
        }
        }
    }
}

private fun DrawScope.mouth(a: PersonAnim, h: Float, pen: Pen, t: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val stroke = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round)
    val chewing = a.chew > 0f
    val talking = a.talk > 0f
    when {
        a.face == Face.YUCK -> {
            val path = Path().apply {
                moveTo(-0.058f * h, -0.56f * h)
                quadraticTo(0f, -0.62f * h, 0.058f * h, -0.56f * h)
            }
            drawPath(path, Ink.line, style = stroke)
            inkedOval(rect(0.01f * h, -0.54f * h, 0.04f * h, 0.035f * h), Tongue, pen, shade = false)
        }
        chewing || a.face == Face.CHOMP -> {
            val open = 0.012f + 0.03f * abs(sin(t * 20f))
            val m = rect(0f, -0.575f * h, 0.075f * h, open * h)
            drawOval(MouthDark, m.topLeft, m.size)
            drawOval(Ink.line, m.topLeft, m.size, style = pen.thin)
        }
        talking -> {
            val open = 0.01f + 0.03f * abs(sin(t * 16f))
            val m = rect(0f, -0.575f * h, 0.06f * h, open * h)
            drawOval(MouthDark, m.topLeft, m.size)
            drawOval(Ink.line, m.topLeft, m.size, style = pen.thin)
        }
        a.face == Face.GRIN || a.face == Face.LAUGH -> {
            val depth = if (a.face == Face.LAUGH) 0.11f + abs(sin(t * 14f)) * 0.015f else 0.085f
            val width = if (a.face == Face.LAUGH) 0.085f else 0.07f
            val path = Path().apply {
                moveTo(-width * h, -0.6f * h)
                quadraticTo(0f, -0.585f * h, width * h, -0.6f * h)
                quadraticTo(width * 0.9f * h, (-0.6f + depth) * h, 0f, (-0.6f + depth) * h)
                quadraticTo(-width * 0.9f * h, (-0.6f + depth) * h, -width * h, -0.6f * h)
                close()
            }
            drawPath(path, MouthDark)
            clipPath(path) { drawOval(Tongue, o(-0.045f, -0.6f + depth - 0.04f), Size(0.09f * h, 0.07f * h)) }
            drawPath(path, Ink.line, style = pen.thin)
        }
        a.face == Face.OOH || a.face == Face.WOW -> {
            val s = if (a.face == Face.WOW) 1.4f else 1f
            val m = rect(0f, -0.57f * h, 0.05f * h * s, 0.06f * h * s)
            drawOval(MouthDark, m.topLeft, m.size)
            drawOval(Ink.line, m.topLeft, m.size, style = pen.thin)
        }
        a.face == Face.SLEEP -> {
            val s = 0.8f + 0.2f * sin(t * 1.6f)
            val m = rect(0f, -0.57f * h, 0.03f * h * s, 0.028f * h * s)
            drawOval(MouthDark, m.topLeft, m.size)
        }
        a.face == Face.DIZZY -> {
            val path = Path().apply {
                moveTo(-0.06f * h, -0.575f * h)
                quadraticTo(-0.03f * h, -0.6f * h, 0f, -0.575f * h)
                quadraticTo(0.03f * h, -0.55f * h, 0.06f * h, -0.575f * h)
            }
            drawPath(path, Ink.line, style = stroke)
        }
        else -> {
            val path = Path().apply {
                moveTo(-0.058f * h, -0.59f * h)
                quadraticTo(0f, -0.535f * h, 0.058f * h, -0.59f * h)
            }
            drawPath(path, Ink.line, style = stroke)
            if (a.face == Face.YUM) {
                val lick = sin(t * 9f) * 0.008f
                inkedOval(rect(0.028f * h, (-0.556f + lick) * h, 0.04f * h, 0.035f * h), Tongue, pen, shade = false)
            }
        }
    }
}

private fun DrawScope.hairBack(style: Int, color: Color, c: Offset, r: Float, pen: Pen, h: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    when (style) {
        3 -> {
            val puffs = Path()
            for (i in 0..12) {
                val ang = Math.toRadians(150.0 + i * 20.0)
                val p = Offset(c.x + (r * 1.05f * cos(ang)).toFloat(), c.y + (r * 1.05f * sin(ang)).toFloat())
                puffs.addOval(Rect(p.x - r * 0.26f, p.y - r * 0.26f, p.x + r * 0.26f, p.y + r * 0.26f))
            }
            drawPath(puffs, Ink.line, style = Stroke(pen.lw * 2f))
            drawPath(puffs, color)
        }
        4 -> {
            val back = Path().apply {
                moveTo(c.x - r * 1.12f, c.y)
                quadraticTo(c.x - r * 1.2f, o(0f, -0.42f).y, c.x - r * 1.05f, o(0f, -0.4f).y)
                lineTo(c.x + r * 1.05f, o(0f, -0.4f).y)
                quadraticTo(c.x + r * 1.2f, o(0f, -0.42f).y, c.x + r * 1.12f, c.y)
                arcTo(Rect(c.x - r * 1.12f, c.y - r * 1.12f, c.x + r * 1.12f, c.y + r * 1.12f), 0f, -180f, false)
                close()
            }
            inked(back, color.darken(0.08f), pen)
        }
        5 -> for (side in listOf(-1f, 1f)) {
            for (k in 0 until 3) {
                inkedOval(rect(side * 0.245f * h, (-0.6f + k * 0.07f) * h, 0.08f * h, 0.085f * h), color, pen)
            }
            inkedCircle(o(side * 0.245f, -0.38f), 0.03f * h, Color(0xFFFF6FA8), pen, shade = false)
        }
        6 -> inkedCircle(o(0f, -0.975f), 0.09f * h, color, pen)
        8 -> {
            val back = Path().apply {
                moveTo(c.x - r * 1.16f, c.y)
                lineTo(c.x - r * 1.16f, o(0f, -0.54f).y)
                quadraticTo(c.x - r * 1.16f, o(0f, -0.49f).y, c.x - r * 0.9f, o(0f, -0.49f).y)
                lineTo(c.x + r * 0.9f, o(0f, -0.49f).y)
                quadraticTo(c.x + r * 1.16f, o(0f, -0.49f).y, c.x + r * 1.16f, o(0f, -0.54f).y)
                lineTo(c.x + r * 1.16f, c.y)
                arcTo(Rect(c.x - r * 1.16f, c.y - r * 1.16f, c.x + r * 1.16f, c.y + r * 1.16f), 0f, -180f, false)
                close()
            }
            inked(back, color.darken(0.06f), pen)
        }
    }
}

private fun DrawScope.hairFront(style: Int, color: Color, c: Offset, r: Float, pen: Pen, h: Float, t: Float) {
    fun cap(bottomEdge: Path.() -> Unit): Path = Path().apply {
        arcTo(Rect(c.x - r * 1.06f, c.y - r * 1.06f, c.x + r * 1.06f, c.y + r * 1.06f), 185f, 170f, true)
        bottomEdge()
        close()
    }
    val y = { f: Float -> f * h }
    when (style) {
        0 -> drawOval(Color.White.copy(alpha = 0.3f), Offset(c.x - r * 0.45f, c.y - r * 0.95f), Size(r * 0.5f, r * 0.22f))
        1 -> inked(cap {
            lineTo(c.x + r * 0.95f, y(-0.64f))
            quadraticTo(c.x + r * 0.9f, y(-0.78f), c.x + r * 0.6f, y(-0.8f))
            quadraticTo(c.x + r * 0.35f, y(-0.84f), c.x + r * 0.15f, y(-0.8f))
            quadraticTo(c.x - r * 0.1f, y(-0.85f), c.x - r * 0.35f, y(-0.8f))
            quadraticTo(c.x - r * 0.7f, y(-0.83f), c.x - r * 0.88f, y(-0.76f))
            lineTo(c.x - r * 0.95f, y(-0.64f))
        }, color, pen)
        2 -> inked(cap {
            lineTo(c.x + r * 1.02f, y(-0.6f))
            lineTo(c.x + r * 0.8f, y(-0.62f))
            lineTo(c.x + r * 0.78f, y(-0.78f))
            quadraticTo(c.x, y(-0.775f), c.x - r * 0.78f, y(-0.78f))
            lineTo(c.x - r * 0.8f, y(-0.62f))
            lineTo(c.x - r * 1.02f, y(-0.6f))
        }, color, pen)
        3 -> for (i in -3..3) {
            val p = Offset(c.x + i * r * 0.26f, c.y - r * (0.86f - abs(i) * 0.05f))
            inkedCircle(p, r * 0.2f, color, pen)
        }
        4 -> inked(cap {
            lineTo(c.x + r * 1.05f, y(-0.5f))
            lineTo(c.x + r * 0.82f, y(-0.52f))
            quadraticTo(c.x + r * 0.78f, y(-0.72f), c.x + r * 0.35f, y(-0.8f))
            quadraticTo(c.x - r * 0.35f, y(-0.86f), c.x - r * 0.85f, y(-0.7f))
            lineTo(c.x - r * 1.05f, y(-0.55f))
        }, color, pen)
        5 -> inked(cap {
            lineTo(c.x + r * 0.96f, y(-0.62f))
            quadraticTo(c.x + r * 0.7f, y(-0.8f), c.x + r * 0.05f, y(-0.86f))
            lineTo(c.x - r * 0.05f, y(-0.86f))
            quadraticTo(c.x - r * 0.7f, y(-0.8f), c.x - r * 0.96f, y(-0.62f))
        }, color, pen)
        6 -> inked(cap {
            lineTo(c.x + r * 0.95f, y(-0.66f))
            quadraticTo(c.x + r * 0.6f, y(-0.84f), c.x, y(-0.86f))
            quadraticTo(c.x - r * 0.6f, y(-0.84f), c.x - r * 0.95f, y(-0.66f))
        }, color, pen)
        7 -> {
            val spikes = Path().apply {
                val n = 9
                for (i in 0..n) {
                    val ang = Math.toRadians(195.0 + i * (150.0 / n))
                    val rr = if (i % 2 == 0) r * 1.02f else r * (1.32f + 0.03f * sin(t * 3f + i))
                    val p = Offset(c.x + (rr * cos(ang)).toFloat(), c.y + (rr * sin(ang)).toFloat())
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                lineTo(c.x + r * 0.9f, y(-0.72f))
                quadraticTo(c.x, y(-0.84f), c.x - r * 0.9f, y(-0.72f))
                close()
            }
            inked(spikes, color, pen)
        }
        8 -> inked(cap {
            lineTo(c.x + r * 1.0f, y(-0.62f))
            quadraticTo(c.x + r * 0.85f, y(-0.8f), c.x + r * 0.5f, y(-0.8f))
            lineTo(c.x - r * 0.5f, y(-0.8f))
            quadraticTo(c.x - r * 0.85f, y(-0.8f), c.x - r * 1.0f, y(-0.62f))
        }, color, pen)
    }
    // Fine strands give the hair direction and a little shine.
    if (style in STRANDED) {
        val strand = color.darken(0.3f)
        for (k in -1..1) {
            val path = Path().apply {
                moveTo(c.x + k * r * 0.3f, c.y - r * 1.0f)
                quadraticTo(c.x + k * r * 0.5f + r * 0.12f, c.y - r * 0.85f, c.x + k * r * 0.58f + r * 0.06f, c.y - r * 0.58f)
            }
            drawPath(path, strand, style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
        }
        drawArc(Color.White.copy(alpha = 0.35f), 205f, 40f, false, Offset(c.x - r * 0.9f, c.y - r * 0.95f), Size(r * 1.2f, r * 0.9f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    }
}

private val STRANDED = setOf(1, 2, 4, 5, 6, 8)

// ---------------------------------------------------------------------------------------- pets

/** Animals sit up facing us. Origin at the bottom centre; head centre at -0.62 h. */
private fun DrawScope.drawPet(species: Species, look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, seed: Float) {
    val t = pen.t + seed
    when (species) {
        Species.PUFFIN -> return drawPuffin(pose, a, h, pen, t)
        Species.COW, Species.SHEEP, Species.HORSE, Species.GOAT -> return drawFarmAnimal(species, look, pose, a, h, pen, t)
        Species.CHICKEN -> return drawChicken(look, pose, a, h, pen, t)
        else -> Unit
    }
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val fur = argb(Palette.furFor(species, look.skin))
    val belly = fur.lighten(0.45f)
    val breath = sin(t * 3f) * 0.006f
    val sleeping = a.face == Face.SLEEP || pose == Pose.LIE
    val held = pose == Pose.HELD

    // Tail
    val sway = sin(t * (if (species == Species.DOG) 9f else 2.5f)) * 0.08f
    when (species) {
        Species.CAT, Species.DOG -> {
            val tail = Path().apply {
                moveTo(0.22f * h, -0.08f * h)
                quadraticTo((0.5f + sway) * h, -0.12f * h, (0.42f + sway) * h, -0.42f * h)
            }
            drawPath(tail, Ink.line, style = Stroke(0.1f * h + pen.lw * 2, cap = StrokeCap.Round))
            drawPath(tail, fur, style = Stroke(0.1f * h, cap = StrokeCap.Round))
        }
        Species.BUNNY -> inkedCircle(o(0.26f, -0.1f), 0.09f * h, belly, pen)
        Species.DRAGON -> {
            val tail = Path().apply {
                moveTo(0.2f * h, -0.06f * h)
                quadraticTo((0.5f + sway) * h, -0.02f * h, (0.52f + sway) * h, -0.26f * h)
            }
            drawPath(tail, Ink.line, style = Stroke(0.1f * h + pen.lw * 2, cap = StrokeCap.Round))
            drawPath(tail, fur, style = Stroke(0.1f * h, cap = StrokeCap.Round))
            val tip = Path().apply {
                moveTo((0.52f + sway) * h, -0.36f * h)
                lineTo((0.6f + sway) * h, -0.24f * h)
                lineTo((0.46f + sway) * h, -0.22f * h)
                close()
            }
            inked(tip, fur.darken(0.15f), pen)
            // Little wings that flap.
            val flap = sin(t * (if (held) 16f else 4f)) * 12f
            for (side in listOf(-1f, 1f)) {
                rotate(side * (20f + flap), pivot = o(side * 0.12f, -0.36f)) {
                    val wing = Path().apply {
                        moveTo(side * 0.12f * h, -0.36f * h)
                        quadraticTo(side * 0.45f * h, -0.62f * h, side * 0.5f * h, -0.4f * h)
                        quadraticTo(side * 0.38f * h, -0.36f * h, side * 0.36f * h, -0.26f * h)
                        quadraticTo(side * 0.26f * h, -0.3f * h, side * 0.12f * h, -0.22f * h)
                        close()
                    }
                    inked(wing, fur.darken(0.12f), pen)
                }
            }
        }
        else -> Unit
    }

    // Body
    val body = blobPath(
        -0.26f * h, -0.02f * h, -0.31f * h, -0.24f * h, -0.18f * h, (-0.47f + breath) * h, 0.18f * h, (-0.47f + breath) * h,
        0.31f * h, -0.24f * h, 0.26f * h, -0.02f * h,
    )
    inked(body, fur, pen)
    drawOval(belly, o(-0.13f, -0.34f), Size(0.26f * h, 0.3f * h))
    if (species == Species.DRAGON) {
        for (k in 0 until 3) drawLine(fur.darken(0.1f), o(-0.1f, -0.28f + k * 0.07f), o(0.1f, -0.28f + k * 0.07f), strokeWidth = pen.lw * 0.6f)
    }
    // Paws
    val dangle = if (held) sin(t * 12f) * 0.02f else 0f
    inkedOval(rect(-0.1f * h, (-0.03f + dangle) * h, 0.15f * h, 0.085f * h), fur.lighten(0.1f), pen)
    inkedOval(rect(0.1f * h, (-0.03f - dangle) * h, 0.15f * h, 0.085f * h), fur.lighten(0.1f), pen)

    // Head
    val c = o(0f, -0.62f + breath)
    val r = 0.33f * h
    when (species) {
        Species.CAT -> for (side in listOf(-1f, 1f)) {
            val ear = Path().apply {
                moveTo(c.x + side * r * 0.85f, c.y - r * 0.35f)
                lineTo(c.x + side * r * 0.8f, c.y - r * 1.25f)
                lineTo(c.x + side * r * 0.2f, c.y - r * 0.85f)
                close()
            }
            inked(ear, fur, pen)
            val inner = Path().apply {
                moveTo(c.x + side * r * 0.72f, c.y - r * 0.55f)
                lineTo(c.x + side * r * 0.72f, c.y - r * 1.05f)
                lineTo(c.x + side * r * 0.35f, c.y - r * 0.82f)
                close()
            }
            drawPath(inner, EarPink)
        }
        Species.BUNNY -> for (side in listOf(-1f, 1f)) {
            rotate(side * (10f + sin(t * 2f + side) * 5f), pivot = Offset(c.x + side * r * 0.35f, c.y - r * 0.7f)) {
                inkedOval(Rect(c.x + side * r * 0.35f - r * 0.22f, c.y - r * 2.1f, c.x + side * r * 0.35f + r * 0.22f, c.y - r * 0.6f), fur, pen)
                drawOval(EarPink, Offset(c.x + side * r * 0.35f - r * 0.1f, c.y - r * 1.9f), Size(r * 0.2f, r * 1.1f))
            }
        }
        Species.DRAGON -> for (side in listOf(-1f, 1f)) {
            val horn = Path().apply {
                moveTo(c.x + side * r * 0.55f, c.y - r * 0.7f)
                quadraticTo(c.x + side * r * 0.8f, c.y - r * 1.2f, c.x + side * r * 0.95f, c.y - r * 1.25f)
                quadraticTo(c.x + side * r * 0.75f, c.y - r * 0.95f, c.x + side * r * 0.85f, c.y - r * 0.5f)
                close()
            }
            inked(horn, Color(0xFFFFE9B0), pen)
        }
        Species.ELK -> {
            for (side in listOf(-1f, 1f)) {
                rotate(side * (22f + sin(t * 2.2f + side) * 6f), pivot = Offset(c.x + side * r * 0.75f, c.y - r * 0.45f)) {
                    inkedOval(Rect(c.x + side * r * 0.75f - r * 0.32f, c.y - r * 0.62f, c.x + side * r * 0.75f + r * 0.32f, c.y - r * 0.3f), fur, pen)
                    drawOval(EarPink.copy(alpha = 0.7f), Offset(c.x + side * r * 0.75f - r * 0.18f, c.y - r * 0.53f), Size(r * 0.36f, r * 0.14f))
                }
                // Velvet antler buds.
                inkedCircle(Offset(c.x + side * r * 0.36f, c.y - r * 0.95f), r * 0.16f, Color(0xFFD9B98C), pen)
            }
        }
        else -> Unit
    }
    inkedCircle(c, r, fur, pen)
    if (species == Species.DOG) {
        for (side in listOf(-1f, 1f)) {
            rotate(side * (-15f + sin(t * 3f) * 4f), pivot = Offset(c.x + side * r * 0.8f, c.y - r * 0.6f)) {
                inkedOval(Rect(c.x + side * r * 0.8f - r * 0.24f, c.y - r * 0.7f, c.x + side * r * 0.8f + r * 0.24f, c.y + r * 0.35f), fur.darken(0.2f), pen)
            }
        }
    }
    if (species == Species.DRAGON) {
        for (k in -1..1) {
            val spike = Path().apply {
                moveTo(c.x + k * r * 0.3f - r * 0.12f, c.y - r * 0.95f)
                lineTo(c.x + k * r * 0.3f, c.y - r * 1.25f)
                lineTo(c.x + k * r * 0.3f + r * 0.12f, c.y - r * 0.95f)
                close()
            }
            inked(spike, fur.darken(0.18f), pen)
        }
        inkedCircle(c, r, fur, pen)
    }

    // Muzzle: the moose has its big soft nose, the others a pale patch.
    val elk = species == Species.ELK
    if (elk) {
        inkedOval(Rect(c.x - r * 0.52f, c.y + r * 0.12f, c.x + r * 0.52f, c.y + r * 1.02f), fur.lighten(0.18f), pen)
    } else {
        val muzzle = Offset(c.x, c.y + r * 0.35f)
        drawOval(belly, Offset(muzzle.x - r * 0.48f, muzzle.y - r * 0.28f), Size(r * 0.96f, r * 0.62f))
    }
    // Eyes
    for (side in listOf(-1f, 1f)) {
        val e = Offset(c.x + side * r * 0.4f, c.y - r * 0.12f)
        if (sleeping || a.blink > 0f) {
            val arc = Path().apply {
                moveTo(e.x - r * 0.16f, e.y)
                quadraticTo(e.x, e.y + r * 0.12f, e.x + r * 0.16f, e.y)
            }
            drawPath(arc, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        } else {
            val white = rect(e.x, e.y, r * 0.38f, r * 0.46f)
            drawOval(Color.White, white.topLeft, white.size)
            drawOval(Ink.line, white.topLeft, white.size, style = pen.thin)
            val p = Offset(e.x + a.lookX * r * 0.07f, e.y + a.lookY * r * 0.08f + r * 0.03f)
            val pupil = if (species == Species.CAT) Size(r * 0.13f, r * 0.3f) else Size(r * 0.24f, r * 0.26f)
            drawOval(Ink.line, Offset(p.x - pupil.width / 2, p.y - pupil.height / 2), pupil)
            drawCircle(Color.White, r * 0.05f, Offset(p.x - r * 0.04f, p.y - r * 0.07f))
        }
    }
    // Nose and mouth
    val nose = Offset(c.x, c.y + r * 0.22f)
    when (species) {
        Species.DOG -> drawOval(Ink.line, Offset(nose.x - r * 0.16f, nose.y - r * 0.09f), Size(r * 0.32f, r * 0.2f))
        Species.DRAGON -> {
            drawCircle(fur.darken(0.4f), r * 0.04f, Offset(nose.x - r * 0.12f, nose.y))
            drawCircle(fur.darken(0.4f), r * 0.04f, Offset(nose.x + r * 0.12f, nose.y))
        }
        Species.ELK -> {
            drawOval(fur.darken(0.45f), Offset(c.x - r * 0.3f, c.y + r * 0.42f), Size(r * 0.16f, r * 0.2f))
            drawOval(fur.darken(0.45f), Offset(c.x + r * 0.14f, c.y + r * 0.42f), Size(r * 0.16f, r * 0.2f))
        }
        else -> {
            val tri = Path().apply {
                moveTo(nose.x - r * 0.09f, nose.y - r * 0.05f)
                lineTo(nose.x + r * 0.09f, nose.y - r * 0.05f)
                lineTo(nose.x, nose.y + r * 0.05f)
                close()
            }
            drawPath(tri, EarPink)
            drawPath(tri, Ink.line, style = pen.thin)
        }
    }
    val m = Offset(c.x, c.y + r * (if (elk) 0.8f else 0.38f))
    if (a.chew > 0f || a.talk > 0f || a.face == Face.OOH || a.face == Face.WOW || a.face == Face.LAUGH) {
        val open = if (a.chew > 0f || a.talk > 0f) 0.06f + 0.1f * abs(sin(t * 18f)) else 0.16f
        drawOval(MouthDark, Offset(m.x - r * 0.12f, m.y - r * open / 2), Size(r * 0.24f, r * open))
        if (species == Species.DOG) drawOval(Tongue, Offset(m.x - r * 0.08f, m.y), Size(r * 0.16f, r * 0.18f))
    } else {
        val w = Path().apply {
            moveTo(m.x - r * 0.2f, m.y - r * 0.04f)
            quadraticTo(m.x - r * 0.1f, m.y + r * 0.08f, m.x, m.y - r * 0.02f)
            quadraticTo(m.x + r * 0.1f, m.y + r * 0.08f, m.x + r * 0.2f, m.y - r * 0.04f)
        }
        drawPath(w, Ink.line, style = Stroke(pen.lw, cap = StrokeCap.Round))
        if (species == Species.BUNNY) {
            inkedRound(Rect(m.x - r * 0.07f, m.y, m.x + r * 0.07f, m.y + r * 0.12f), r * 0.02f, Color.White, pen, shade = false)
        }
    }
    if (species == Species.CAT) {
        for (side in listOf(-1f, 1f)) for (k in 0 until 2) {
            drawLine(Ink.line.copy(alpha = 0.6f), Offset(c.x + side * r * 0.35f, c.y + r * (0.3f + k * 0.1f)), Offset(c.x + side * r * 0.85f, c.y + r * (0.25f + k * 0.16f)), strokeWidth = pen.lw * 0.5f)
        }
    }
    drawOval(Ink.blush, Offset(c.x - r * 0.75f, c.y + r * 0.1f), Size(r * 0.3f, r * 0.18f))
    drawOval(Ink.blush, Offset(c.x + r * 0.45f, c.y + r * 0.1f), Size(r * 0.3f, r * 0.18f))
    if (species == Species.DRAGON && a.face == Face.GRIN) {
        // A tiny puff of flame when the dragon is happy.
        val f = 0.6f + 0.4f * abs(sin(t * 10f))
        drawOval(Color(0xFFFFB02E), Offset(m.x - r * 0.1f, m.y + r * 0.1f), Size(r * 0.2f * f, r * 0.35f * f))
    }
}

/** The puffin from the bird cliffs: black coat, white face and a striped beak. Origin at the feet. */
private fun DrawScope.drawPuffin(pose: Pose, a: PersonAnim, h: Float, pen: Pen, t: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val coat = Color(0xFF2B2A3A)
    val held = pose == Pose.HELD
    val sleeping = a.face == Face.SLEEP || pose == Pose.LIE
    val waddle = if (a.dance > 0f) sin(a.dance) * 8f else sin(t * 1.8f) * 2f
    // Feet
    for (side in listOf(-1f, 1f)) {
        val kick = if (held) sin(t * 12f + side) * 0.03f else 0f
        val foot = Path().apply {
            moveTo(side * 0.05f * h, (-0.06f + kick) * h)
            lineTo(side * 0.2f * h, (0f + kick) * h)
            lineTo(side * 0.02f * h, (0f + kick) * h)
            close()
        }
        inked(foot, Color(0xFFFF7A3D), pen)
    }
    rotate(waddle, pivot = o(0f, 0f)) {
        // Wings flap when the bird is lifted.
        val flap = if (held) sin(t * 18f) * 30f else sin(t * 2f) * 4f
        for (side in listOf(-1f, 1f)) {
            rotate(side * (15f + flap), pivot = o(side * 0.24f, -0.5f)) {
                inkedOval(Rect((side * 0.3f - 0.09f) * h, -0.52f * h, (side * 0.3f + 0.09f) * h, -0.18f * h), coat, pen)
            }
        }
        val body = blobPath(
            -0.28f * h, -0.06f * h, -0.33f * h, -0.36f * h, -0.2f * h, -0.66f * h, 0.2f * h, -0.66f * h,
            0.33f * h, -0.36f * h, 0.28f * h, -0.06f * h,
        )
        inked(body, coat, pen)
        drawOval(Color(0xFFF7F4EE), o(-0.2f, -0.52f), Size(0.4f * h, 0.46f * h))
        // Head
        val c = o(0f, -0.72f)
        inkedCircle(c, 0.25f * h, coat, pen)
        drawOval(Color(0xFFEDEEF4), Offset(c.x - 0.2f * h, c.y - 0.13f * h), Size(0.4f * h, 0.3f * h))
        for (side in listOf(-1f, 1f)) {
            val e = Offset(c.x + side * 0.1f * h, c.y - 0.02f * h)
            if (sleeping || a.blink > 0f) {
                drawLine(Ink.line, Offset(e.x - 0.03f * h, e.y), Offset(e.x + 0.03f * h, e.y), strokeWidth = pen.lw, cap = StrokeCap.Round)
            } else {
                drawCircle(Ink.line, 0.03f * h, Offset(e.x + a.lookX * 0.01f * h, e.y + a.lookY * 0.01f * h))
                drawCircle(Color.White, 0.011f * h, Offset(e.x - 0.008f * h, e.y - 0.01f * h))
            }
            // The little dark triangle above each eye.
            val mark = Path().apply {
                moveTo(e.x - 0.03f * h, e.y - 0.05f * h)
                lineTo(e.x + 0.03f * h, e.y - 0.05f * h)
                lineTo(e.x, e.y - 0.1f * h)
                close()
            }
            drawPath(mark, Color(0xFF7D7F92))
            drawOval(Ink.blush, Offset(e.x - 0.04f * h, e.y + 0.04f * h), Size(0.08f * h, 0.04f * h))
        }
        // Beak: grey root, a yellow band and a bright orange tip. It opens when the puffin eats.
        val open = if (a.chew > 0f || a.talk > 0f) 0.02f + 0.03f * abs(sin(t * 18f)) else if (a.face == Face.OOH || a.face == Face.WOW) 0.03f else 0f
        val top = c.y + 0.03f * h
        val beak = Path().apply {
            moveTo(c.x - 0.1f * h, top)
            lineTo(c.x + 0.1f * h, top)
            lineTo(c.x, top + 0.2f * h)
            close()
        }
        drawPath(beak, Color(0xFFFF6A3D))
        clipPath(beak) {
            drawRect(Color(0xFF8E93A6), Offset(c.x - 0.12f * h, top), Size(0.24f * h, 0.045f * h))
            drawRect(Color(0xFFFFD23F), Offset(c.x - 0.12f * h, top + 0.045f * h), Size(0.24f * h, 0.03f * h))
            if (open > 0f) drawRect(MouthDark, Offset(c.x - 0.12f * h, top + 0.1f * h), Size(0.24f * h, open * h))
        }
        drawPath(beak, Ink.line, style = pen.stroke)
    }
}

/** Eyes for animals: white with a pupil that follows, or closed arcs. */
private fun DrawScope.animalEyes(c: Offset, r: Float, spread: Float, y: Float, a: PersonAnim, pen: Pen, closed: Boolean) {
    for (side in listOf(-1f, 1f)) {
        val e = Offset(c.x + side * r * spread, c.y + r * y)
        if (closed || a.blink > 0f) {
            val arc = Path().apply {
                moveTo(e.x - r * 0.15f, e.y)
                quadraticTo(e.x, e.y + r * 0.11f, e.x + r * 0.15f, e.y)
            }
            drawPath(arc, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        } else {
            val white = rect(e.x, e.y, r * 0.34f, r * 0.4f)
            drawOval(Color.White, white.topLeft, white.size)
            drawOval(Ink.line, white.topLeft, white.size, style = pen.thin)
            val p = Offset(e.x + a.lookX * r * 0.06f, e.y + a.lookY * r * 0.07f + r * 0.03f)
            drawCircle(Ink.line, r * 0.11f, p)
            drawCircle(Color.White, r * 0.04f, Offset(p.x - r * 0.04f, p.y - r * 0.05f))
        }
    }
}

/** A small curly goat horn: a tapered arch sweeping up, out and back down, with a few growth rings. */
private fun DrawScope.goatHorn(c: Offset, r: Float, side: Float, pen: Pen) {
    val p0 = Offset(c.x + side * r * 0.3f, c.y - r * 0.8f)
    val p1 = Offset(c.x + side * r * 0.24f, c.y - r * 1.38f)
    val p2 = Offset(c.x + side * r * 0.96f, c.y - r * 1.46f)
    val p3 = Offset(c.x + side * r * 0.88f, c.y - r * 1.0f)
    val n = 14
    val lx = FloatArray(n + 1)
    val ly = FloatArray(n + 1)
    val rx = FloatArray(n + 1)
    val ry = FloatArray(n + 1)
    for (i in 0..n) {
        val s = i / n.toFloat()
        val q = 1f - s
        val bx = q * q * q * p0.x + 3f * q * q * s * p1.x + 3f * q * s * s * p2.x + s * s * s * p3.x
        val by = q * q * q * p0.y + 3f * q * q * s * p1.y + 3f * q * s * s * p2.y + s * s * s * p3.y
        val dx = 3f * q * q * (p1.x - p0.x) + 6f * q * s * (p2.x - p1.x) + 3f * s * s * (p3.x - p2.x)
        val dy = 3f * q * q * (p1.y - p0.y) + 6f * q * s * (p2.y - p1.y) + 3f * s * s * (p3.y - p2.y)
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.0001f)
        val hw = r * 0.12f * (1f - 0.8f * s)
        val nx = -dy / len
        val ny = dx / len
        lx[i] = bx + nx * hw
        ly[i] = by + ny * hw
        rx[i] = bx - nx * hw
        ry[i] = by - ny * hw
    }
    val horn = Path().apply {
        moveTo(lx[0], ly[0])
        for (i in 1..n) lineTo(lx[i], ly[i])
        for (i in n downTo 0) lineTo(rx[i], ry[i])
        close()
    }
    inked(horn, Color(0xFFEFE0BC), pen)
    for (i in intArrayOf(3, 6, 9)) {
        drawLine(Ink.line.copy(alpha = 0.45f), Offset(lx[i] * 0.8f + rx[i] * 0.2f, ly[i] * 0.8f + ry[i] * 0.2f), Offset(lx[i] * 0.2f + rx[i] * 0.8f, ly[i] * 0.2f + ry[i] * 0.8f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
    }
}

/** Goat eyes: a big white with an amber iris and the goat's sideways rectangular pupil. */
private fun DrawScope.goatEyes(c: Offset, r: Float, a: PersonAnim, pen: Pen, closed: Boolean) {
    for (side in listOf(-1f, 1f)) {
        val e = Offset(c.x + side * r * 0.47f, c.y - r * 0.14f)
        if (closed || a.blink > 0f) {
            val arc = Path().apply {
                moveTo(e.x - r * 0.17f, e.y)
                quadraticTo(e.x, e.y + r * 0.13f, e.x + r * 0.17f, e.y)
            }
            drawPath(arc, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        } else {
            val white = rect(e.x, e.y, r * 0.4f, r * 0.38f)
            drawOval(Color.White, white.topLeft, white.size)
            val ix = e.x + a.lookX * r * 0.05f
            val iy = e.y + a.lookY * r * 0.05f + r * 0.01f
            clipPath(ovalPath(white)) {
                drawOval(Color(0xFFF2B63A), Offset(ix - r * 0.15f, iy - r * 0.15f), Size(r * 0.3f, r * 0.3f))
            }
            // The pupil lies on its side, like a little bar.
            drawRoundRect(Ink.line, Offset(ix - r * 0.12f, iy - r * 0.045f), Size(r * 0.24f, r * 0.09f), androidx.compose.ui.geometry.CornerRadius(r * 0.03f))
            drawCircle(Color.White, r * 0.04f, Offset(ix - r * 0.07f, iy - r * 0.09f))
            drawOval(Ink.line, white.topLeft, white.size, style = pen.thin)
        }
    }
}

/**
 * Cow, sheep and fjord horse: sitting up and facing us like the pets, with the head around -0.62 h and
 * the muzzle over the mouth point at -0.49 h.
 */
private fun DrawScope.drawFarmAnimal(species: Species, look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, t: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val coat = argb(Palette.furFor(species, look.skin))
    val held = pose == Pose.HELD
    val closed = a.face == Face.SLEEP || pose == Pose.LIE
    val breath = sin(t * 2.6f) * 0.005f
    val chewing = a.chew > 0f || a.talk > 0f
    val sway = sin(t * 2f) * 0.06f

    // Tail
    if (species == Species.GOAT) {
        // A short perky tail that flicks up behind the rump.
        val flick = sin(t * 3.2f) * 0.025f
        val tuft = Path().apply {
            moveTo(0.22f * h, -0.16f * h)
            quadraticTo((0.34f + flick) * h, -0.2f * h, (0.36f + flick) * h, -0.33f * h)
            quadraticTo((0.27f + flick) * h, -0.27f * h, 0.24f * h, -0.08f * h)
            close()
        }
        inked(tuft, coat.darken(0.05f), pen)
    } else {
        val tail = Path().apply {
            moveTo(0.24f * h, -0.1f * h)
            quadraticTo((0.46f + sway) * h, -0.14f * h, (0.4f + sway) * h, -0.36f * h)
        }
        drawPath(tail, Ink.line, style = Stroke(0.05f * h + pen.lw * 2, cap = StrokeCap.Round))
        drawPath(tail, if (species == Species.HORSE) Color(0xFF3A3340) else coat.darken(0.15f), style = Stroke(0.05f * h, cap = StrokeCap.Round))
    }

    // Body
    if (species == Species.SHEEP) {
        // A cloud of wool.
        val wool = Path()
        for (i in 0 until 9) {
            val ang = i / 9f * 2f * PI.toFloat()
            val c = o(cos(ang) * 0.24f, -0.26f + sin(ang) * 0.2f + breath)
            wool.addOval(Rect(c.x - 0.1f * h, c.y - 0.1f * h, c.x + 0.1f * h, c.y + 0.1f * h))
        }
        wool.addOval(Rect(-0.26f * h, -0.46f * h, 0.26f * h, -0.06f * h))
        drawPath(wool, Ink.line, style = Stroke(pen.lw * 2f))
        drawPath(wool, coat)
        for (i in 0 until 6) drawArc(coat.darken(0.12f), 200f, 120f, false, o(-0.18f + i * 0.07f, -0.3f + (i % 2) * 0.08f), Size(0.07f * h, 0.05f * h), style = pen.thin)
    } else {
        val body = blobPath(
            -0.27f * h, -0.02f * h, -0.32f * h, -0.25f * h, -0.19f * h, (-0.48f + breath) * h, 0.19f * h, (-0.48f + breath) * h,
            0.32f * h, -0.25f * h, 0.27f * h, -0.02f * h,
        )
        inked(body, coat, pen)
        if (species == Species.COW && look.skin == 0) {
            clipPath(body) {
                drawOval(Color(0xFF3A3340), o(-0.3f, -0.36f), Size(0.2f * h, 0.16f * h))
                drawOval(Color(0xFF3A3340), o(0.08f, -0.2f), Size(0.22f * h, 0.14f * h))
            }
            drawPath(body, Ink.line, style = pen.stroke)
        }
    }
    // Hooves
    val dangle = if (held) sin(t * 12f) * 0.02f else 0f
    for (side in listOf(-1f, 1f)) {
        inkedRound(Rect((side * 0.1f - 0.06f) * h, (-0.09f + side * dangle) * h, (side * 0.1f + 0.06f) * h, (0f + side * dangle) * h), 0.03f * h, if (species == Species.SHEEP) Color(0xFF3A3340) else coat.darken(0.1f), pen)
        drawRect(Color(0xFF3A3340), o(side * 0.1f - 0.06f, -0.025f + side * dangle), Size(0.12f * h, 0.025f * h))
    }

    val c = o(0f, -0.64f + breath)
    val r = 0.3f * h
    val face = when (species) {
        Species.SHEEP -> if (look.skin == 2) Color(0xFF6E6A78) else Color(0xFF3A3340)
        else -> coat
    }
    // Ears and horns
    for (side in listOf(-1f, 1f)) {
        if (species == Species.GOAT) {
            // Long goat ears stick out sideways like little wings, with a flick now and then.
            val base = Offset(c.x + side * r * 0.7f, c.y - r * 0.32f)
            rotate(side * (sin(t * 2.4f + side) * 5f - 8f), pivot = base) {
                val ear = Path().apply {
                    moveTo(c.x + side * r * 0.62f, c.y - r * 0.5f)
                    quadraticTo(c.x + side * r * 1.2f, c.y - r * 0.66f, c.x + side * r * 1.66f, c.y - r * 0.28f)
                    quadraticTo(c.x + side * r * 1.2f, c.y - r * 0.06f, c.x + side * r * 0.66f, c.y - r * 0.12f)
                    close()
                }
                inked(ear, face, pen)
                val inner = Path().apply {
                    moveTo(c.x + side * r * 0.86f, c.y - r * 0.4f)
                    quadraticTo(c.x + side * r * 1.2f, c.y - r * 0.5f, c.x + side * r * 1.46f, c.y - r * 0.28f)
                    quadraticTo(c.x + side * r * 1.2f, c.y - r * 0.18f, c.x + side * r * 0.86f, c.y - r * 0.2f)
                    close()
                }
                drawPath(inner, EarPink.copy(alpha = 0.6f))
            }
        } else rotate(side * (28f + sin(t * 2.4f + side) * 5f), pivot = Offset(c.x + side * r * 0.8f, c.y - r * 0.35f)) {
            inkedOval(Rect(c.x + side * r * 0.8f - r * 0.3f, c.y - r * 0.5f, c.x + side * r * 0.8f + r * 0.3f, c.y - r * 0.22f), face, pen)
            drawOval(EarPink.copy(alpha = 0.6f), Offset(c.x + side * r * 0.8f - r * 0.16f, c.y - r * 0.43f), Size(r * 0.32f, r * 0.13f))
        }
        if (species == Species.COW) {
            val horn = Path().apply {
                moveTo(c.x + side * r * 0.42f, c.y - r * 0.8f)
                quadraticTo(c.x + side * r * 0.7f, c.y - r * 1.15f, c.x + side * r * 0.62f, c.y - r * 1.3f)
                quadraticTo(c.x + side * r * 0.55f, c.y - r * 1.0f, c.x + side * r * 0.25f, c.y - r * 0.9f)
                close()
            }
            inked(horn, Color(0xFFF3E6C8), pen)
        }
        if (species == Species.GOAT) goatHorn(c, r, side, pen)
        if (species == Species.HORSE) {
            val ear = Path().apply {
                moveTo(c.x + side * r * 0.25f, c.y - r * 0.85f)
                lineTo(c.x + side * r * 0.42f, c.y - r * 1.35f)
                lineTo(c.x + side * r * 0.55f, c.y - r * 0.75f)
                close()
            }
            inked(ear, coat, pen)
        }
    }
    if (species == Species.HORSE) {
        // A long face with a pale nose, like a fjord horse.
        inkedOval(Rect(c.x - r * 0.62f, c.y - r * 1.0f, c.x + r * 0.62f, c.y + r * 0.95f), coat, pen)
        drawOval(coat.lighten(0.35f), Offset(c.x - r * 0.46f, c.y + r * 0.25f), Size(r * 0.92f, r * 0.66f))
        // The upright mane: cream at the sides with a dark stripe down the middle.
        val mane = Path().apply {
            moveTo(c.x - r * 0.22f, c.y - r * 0.82f)
            lineTo(c.x - r * 0.16f, c.y - r * 1.32f)
            lineTo(c.x + r * 0.16f, c.y - r * 1.32f)
            lineTo(c.x + r * 0.22f, c.y - r * 0.82f)
            close()
        }
        inked(mane, Color(0xFFF7EFD9), pen)
        drawRect(Color(0xFF3A3340), Offset(c.x - r * 0.05f, c.y - r * 1.3f), Size(r * 0.1f, r * 0.48f))
        animalEyes(c, r, 0.34f, -0.35f, a, pen, closed)
        drawOval(Ink.line, Offset(c.x - r * 0.28f, c.y + r * 0.6f), Size(r * 0.13f, r * 0.1f))
        drawOval(Ink.line, Offset(c.x + r * 0.15f, c.y + r * 0.6f), Size(r * 0.13f, r * 0.1f))
        val m = Offset(c.x, c.y + r * 0.8f)
        if (chewing || a.face == Face.OOH || a.face == Face.LAUGH) drawOval(MouthDark, Offset(m.x - r * 0.14f, m.y - r * 0.04f), Size(r * 0.28f, r * (0.08f + 0.06f * abs(sin(t * 16f)))))
        else drawArc(Ink.line, 20f, 140f, false, Offset(m.x - r * 0.14f, m.y - r * 0.12f), Size(r * 0.28f, r * 0.14f), style = Stroke(pen.lw, cap = StrokeCap.Round))
        return
    }
    if (species == Species.GOAT) {
        // The goatee hangs from the chin, tucked in behind the head.
        val sw = sin(t * 2.1f) * 0.04f
        val beard = Path().apply {
            moveTo(c.x - r * 0.2f, c.y + r * 0.72f)
            cubicTo(c.x - r * 0.26f, c.y + r * 1.1f, c.x - r * 0.18f, c.y + r * 1.36f, c.x - r * (0.12f - sw), c.y + r * 1.52f)
            lineTo(c.x - r * (0.05f - sw), c.y + r * 1.38f)
            lineTo(c.x + r * sw, c.y + r * 1.7f)
            lineTo(c.x + r * (0.06f + sw), c.y + r * 1.38f)
            lineTo(c.x + r * (0.12f + sw), c.y + r * 1.52f)
            cubicTo(c.x + r * 0.18f, c.y + r * 1.36f, c.x + r * 0.26f, c.y + r * 1.1f, c.x + r * 0.2f, c.y + r * 0.72f)
            close()
        }
        inked(beard, if (look.skin == 0) Color(0xFFEDE5D4) else coat.lighten(0.3f), pen)
    }
    inkedCircle(c, r, face, pen)
    if (species == Species.SHEEP) {
        // A woolly fringe on top.
        for (i in -2..2) inkedCircle(Offset(c.x + i * r * 0.22f, c.y - r * (0.9f - abs(i) * 0.06f)), r * 0.2f, coat, pen)
    }
    if (species == Species.COW && look.skin != 1) drawOval(Color(0xFF3A3340), Offset(c.x - r * 0.9f, c.y - r * 0.8f), Size(r * 0.7f, r * 0.6f))
    // Muzzle
    val muzzle = Rect(c.x - r * 0.6f, c.y + r * 0.12f, c.x + r * 0.6f, c.y + r * 0.82f)
    inkedOval(muzzle, if (species == Species.COW) Color(0xFFFFB3C1) else face.lighten(0.25f), pen)
    if (species == Species.GOAT) {
        // A small pink nose with two nostrils.
        inkedOval(Rect(c.x - r * 0.2f, c.y + r * 0.22f, c.x + r * 0.2f, c.y + r * 0.42f), Color(0xFFE8A0A8), pen, shade = false)
        drawOval(Ink.line.copy(alpha = 0.8f), Offset(c.x - r * 0.12f, c.y + r * 0.28f), Size(r * 0.08f, r * 0.08f))
        drawOval(Ink.line.copy(alpha = 0.8f), Offset(c.x + r * 0.04f, c.y + r * 0.28f), Size(r * 0.08f, r * 0.08f))
        goatEyes(c, r, a, pen, closed)
    } else {
        drawOval(Ink.line.copy(alpha = 0.8f), Offset(c.x - r * 0.32f, c.y + r * 0.34f), Size(r * 0.14f, r * 0.12f))
        drawOval(Ink.line.copy(alpha = 0.8f), Offset(c.x + r * 0.18f, c.y + r * 0.34f), Size(r * 0.14f, r * 0.12f))
        animalEyes(c, r, 0.42f, -0.2f, a, pen, closed)
    }
    val m = Offset(c.x, c.y + r * 0.62f)
    if (chewing || a.face == Face.OOH || a.face == Face.WOW || a.face == Face.LAUGH) {
        drawOval(MouthDark, Offset(m.x - r * 0.16f, m.y - r * 0.05f), Size(r * 0.32f, r * (0.1f + 0.08f * abs(sin(t * 14f)))))
    } else if (species == Species.GOAT) {
        // A little cat-like smile under the nose.
        drawLine(Ink.line, Offset(c.x, c.y + r * 0.42f), Offset(c.x, m.y - r * 0.03f), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
        drawArc(Ink.line, 10f, 160f, false, Offset(m.x - r * 0.2f, m.y - r * 0.1f), Size(r * 0.2f, r * 0.14f), style = Stroke(pen.lw, cap = StrokeCap.Round))
        drawArc(Ink.line, 10f, 160f, false, Offset(m.x, m.y - r * 0.1f), Size(r * 0.2f, r * 0.14f), style = Stroke(pen.lw, cap = StrokeCap.Round))
    } else {
        drawArc(Ink.line, 20f, 140f, false, Offset(m.x - r * 0.16f, m.y - r * 0.12f), Size(r * 0.32f, r * 0.14f), style = Stroke(pen.lw, cap = StrokeCap.Round))
    }
    if (species == Species.GOAT) {
        // A tuft of hair between the horns.
        val tuftTop = Path().apply {
            moveTo(c.x - r * 0.2f, c.y - r * 0.92f)
            lineTo(c.x - r * 0.14f, c.y - r * 1.2f)
            lineTo(c.x - r * 0.04f, c.y - r * 1.0f)
            lineTo(c.x + r * 0.06f, c.y - r * 1.26f)
            lineTo(c.x + r * 0.1f, c.y - r * 1.0f)
            lineTo(c.x + r * 0.2f, c.y - r * 1.14f)
            lineTo(c.x + r * 0.22f, c.y - r * 0.92f)
        }
        val tuft = Path().apply { addPath(tuftTop); lineTo(c.x - r * 0.2f, c.y - r * 0.8f); close() }
        drawPath(tuft, face)
        drawPath(tuftTop, Ink.line, style = pen.stroke)
    }
    if (species == Species.COW) {
        // A cowbell on a red collar.
        drawLine(Color(0xFFD2443A), o(-0.14f, -0.44f), o(0.14f, -0.44f), strokeWidth = 0.03f * h, cap = StrokeCap.Round)
        val swing = sin(t * 3f) * 8f
        rotate(swing, pivot = o(0f, -0.44f)) {
            val bell = Path().apply {
                moveTo(-0.035f * h, -0.44f * h)
                lineTo(0.035f * h, -0.44f * h)
                lineTo(0.05f * h, -0.36f * h)
                lineTo(-0.05f * h, -0.36f * h)
                close()
            }
            inked(bell, Color(0xFFFFC83D), pen)
        }
    }
    drawOval(Ink.blush, Offset(c.x - r * 0.85f, c.y + r * 0.05f), Size(r * 0.3f, r * 0.16f))
    drawOval(Ink.blush, Offset(c.x + r * 0.55f, c.y + r * 0.05f), Size(r * 0.3f, r * 0.16f))
}

/** A round little hen with a red comb. Origin at her feet. */
private fun DrawScope.drawChicken(look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, t: Float) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val feathers = argb(Palette.furFor(Species.CHICKEN, look.skin))
    val held = pose == Pose.HELD
    val closed = a.face == Face.SLEEP || pose == Pose.LIE
    val peck = if (a.talk > 0f || a.chew > 0f) abs(sin(t * 14f)) * 0.03f else 0f
    // Legs
    for (side in listOf(-1f, 1f)) {
        val kick = if (held) sin(t * 14f + side) * 0.04f else 0f
        drawLine(Color(0xFFFF9F43), o(side * 0.08f, -0.2f), o(side * 0.1f, -0.03f + kick), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
        for (k in -1..1) drawLine(Color(0xFFFF9F43), o(side * 0.1f, -0.03f + kick), o(side * 0.1f + k * 0.05f, 0f + kick), strokeWidth = pen.lw * 1.3f, cap = StrokeCap.Round)
    }
    // Tail feathers
    for (k in 0 until 3) {
        rotate(-20f + k * 18f + sin(t * 2f) * 4f, pivot = o(0.2f, -0.45f)) {
            inkedOval(Rect(0.18f * h, -0.72f * h, 0.3f * h, -0.44f * h), if (k == 1) feathers.darken(0.2f) else feathers, pen)
        }
    }
    // Body
    val flap = if (held) sin(t * 20f) * 25f else sin(t * 1.5f) * 3f
    val body = blobPath(-0.3f * h, -0.24f * h, -0.24f * h, -0.52f * h, 0.12f * h, -0.56f * h, 0.3f * h, -0.34f * h, 0.2f * h, -0.14f * h, -0.16f * h, -0.14f * h)
    inked(body, feathers, pen)
    for (side in listOf(-1f, 1f)) {
        rotate(side * flap, pivot = o(side * 0.18f, -0.4f)) {
            inkedOval(Rect((side * 0.2f - 0.1f) * h, -0.44f * h, (side * 0.2f + 0.1f) * h, -0.24f * h), feathers.darken(0.1f), pen)
        }
    }
    // Head
    val c = o(0f, -0.68f + peck)
    val r = 0.2f * h
    for (k in -1..1) inkedCircle(Offset(c.x + k * r * 0.32f, c.y - r * (1.0f - abs(k) * 0.1f)), r * 0.24f, Color(0xFFE8413A), pen)
    inkedCircle(c, r, feathers, pen)
    animalEyes(c, r, 0.45f, -0.12f, a, pen, closed)
    val beak = Path().apply {
        moveTo(c.x - r * 0.24f, c.y + r * 0.12f)
        lineTo(c.x + r * 0.24f, c.y + r * 0.12f)
        lineTo(c.x, c.y + r * (0.55f + if (a.talk > 0f) 0.08f else 0f))
        close()
    }
    inked(beak, Color(0xFFFFC83D), pen)
    inkedOval(Rect(c.x - r * 0.12f, c.y + r * 0.5f, c.x + r * 0.12f, c.y + r * 0.9f), Color(0xFFE8413A), pen, shade = false)
    drawOval(Ink.blush, Offset(c.x - r * 0.9f, c.y + r * 0.1f), Size(r * 0.34f, r * 0.2f))
    drawOval(Ink.blush, Offset(c.x + r * 0.56f, c.y + r * 0.1f), Size(r * 0.34f, r * 0.2f))
}

/** Width of a figure's head in scene units, used to size hats and glasses to fit. */
fun headWidth(species: Species, height: Float): Float = height * Anatomy.headRadius(species) * 2f
