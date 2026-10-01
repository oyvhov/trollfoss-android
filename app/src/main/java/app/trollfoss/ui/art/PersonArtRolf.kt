package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.domain.Face
import app.trollfoss.domain.FigurarPose
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

/*
 * Rolf, the robot butler: a boxy, round-cornered little robot in a shell of mint (or butter, coral, sky,
 * lilac), with a glowing face screen, white butler gloves, a red bow tie and a serving tray that he always
 * holds out on his right hand. His antenna wobbles, his eyes glow and blink, and his screen shows every
 * Face. When he greets he bows: head and hats dip together (see Anatomy.at).
 */

private val ScreenDark = Color(0xFF1B2638)
private val ScreenAsleep = Color(0xFF111A28)
private val Glow = Color(0xFF8FF7FF)
private val GlowDim = Color(0xFF4F8F9E)
private val Pink = Color(0xFFFF6F91)
private val Glove = Color(0xFFFAF7EF)
private val Trim = Color(0xFFFFF3D9)
private val Metal = Color(0xFFB9C0D2)
private val Boot = Color(0xFF4C5078)
private val BowRed = Color(0xFFD2443A)
private val Silver = Color(0xFFDDE2EE)
private val PanelDark = Color(0xFF22304A)

/** Shapes that depend only on the figure's size, built once per size instead of once per picture. */
private class RolfGeo(h: Float) {
    val torso: Path = roundedPoly(0.075f * h, -0.185f * h, -0.50f * h, 0.185f * h, -0.50f * h, 0.215f * h, -0.15f * h, -0.215f * h, -0.15f * h)
    val head: Path = roundedPoly(0.115f * h, -0.285f * h, -0.93f * h, 0.285f * h, -0.93f * h, 0.30f * h, -0.52f * h, -0.30f * h, -0.52f * h)
    val screen: Path = roundedPoly(0.08f * h, -0.255f * h, -0.88f * h, 0.255f * h, -0.88f * h, 0.255f * h, -0.575f * h, -0.255f * h, -0.575f * h)
    val panel: Path = roundedPoly(0.035f * h, -0.125f * h, -0.415f * h, 0.125f * h, -0.415f * h, 0.125f * h, -0.255f * h, -0.125f * h, -0.255f * h)
    val gloss: Path = Path().apply {
        moveTo(-0.235f * h, -0.87f * h)
        lineTo(-0.115f * h, -0.87f * h)
        lineTo(-0.2f * h, -0.69f * h)
        lineTo(-0.25f * h, -0.69f * h)
        close()
    }
}

private val geoCache = HashMap<Int, RolfGeo>()

private fun rolfGeo(h: Float): RolfGeo {
    val key = (h * 2f).toInt()
    geoCache[key]?.let { return it }
    if (geoCache.size > 16) geoCache.clear()
    return RolfGeo(h).also { geoCache[key] = it }
}

/** Rolf standing: feet at the origin, head centre at -0.725 h, antenna tip at about -1.1 h. */
internal fun DrawScope.rolf(look: Look, pose: Pose, a: PersonAnim, h: Float, pen: Pen, holding: Boolean, seed: Float) {
    val t = pen.t + seed
    val g = rolfGeo(h)
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val shell = argb(Palette.furFor(Species.ROBOT, look.skin))
    val sleeping = a.face == Face.SLEEP
    val breath = sin(t * 2.6f)
    val dancing = a.dance > 0f
    val walking = pose == Pose.STAND && !a.walkTo.isNaN() && !dancing
    val stepL = if (walking) max(0f, sin(a.walkPhase * PI.toFloat())) else 0f
    val stepR = if (walking) max(0f, -sin(a.walkPhase * PI.toFloat())) else 0f
    val bow = if (pose == Pose.STAND || pose == Pose.SIT) FigurarPose.bow(a.wave) else 0f
    val dip = bow * FigurarPose.DIP
    val bob = when {
        dancing -> -abs(sin(a.dance)) * 0.025f
        pose == Pose.STAND -> breath * 0.004f
        else -> breath * 0.003f
    }

    // ---- thrusters: only when a float potion lifts him
    if (pose == Pose.FLOAT) {
        for (side in SIDES) {
            val fx = side * 0.106f
            val len = 0.12f + 0.045f * sin(t * 26f + side * 2f)
            val flame = Path().apply {
                moveTo((fx - 0.05f) * h, -0.02f * h)
                quadraticTo(fx * h, (-0.02f + len * 3.4f) * h, (fx + 0.05f) * h, -0.02f * h)
                close()
            }
            drawPath(flame, Color(0xFFFFA63D))
            val core = Path().apply {
                moveTo((fx - 0.02f) * h, -0.02f * h)
                quadraticTo(fx * h, (-0.02f + len * 2.0f) * h, (fx + 0.02f) * h, -0.02f * h)
                close()
            }
            drawPath(core, Color(0xFFFFE27A))
        }
    }

    // ---- legs: little pistons on flat boots
    val kick = when (pose) {
        Pose.HELD -> sin(t * 13f) * 0.03f
        Pose.SWIM -> sin(t * 7f) * 0.02f
        else -> 0f
    }
    val danceStep = if (dancing) sin(a.dance) * 0.012f else 0f
    val spread = if (pose == Pose.FLOAT) 0.85f else 1f
    for (side in SIDES) {
        val lift = side * kick + (if (side < 0) danceStep else -danceStep) - (if (side < 0) stepL else stepR) * 0.05f
        val hip = o(side * 0.11f * spread, -0.17f)
        val ankle = o(side * 0.12f * spread, -0.058f + lift)
        capsule(hip, ankle, 0.05f * h, Metal, pen)
        val foot = rect(side * 0.125f * spread * h, (-0.032f + lift) * h, 0.18f * h, 0.068f * h)
        inkedRound(foot, 0.03f * h, Boot, pen)
        drawLine(Boot.lighten(0.45f), o(side * 0.125f * spread - 0.05f, -0.046f + lift), o(side * 0.125f * spread + 0.025f, -0.046f + lift), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
    }

    withTransform({ translate(0f, bob * h) }) {
        // ---- torso
        inked(g.torso, shell, pen, outline = false)
        clipPath(g.torso) {
            // The cream belt and a lighter hem.
            drawRect(Trim, o(-0.25f, -0.205f), Size(0.5f * h, 0.036f * h))
            drawLine(Ink.line.copy(alpha = 0.35f), o(-0.25f, -0.205f), o(0.25f, -0.205f), strokeWidth = pen.lw * 0.5f)
        }
        drawPath(g.torso, Ink.line, style = pen.stroke)
        // The control panel: a dial, a heart lamp that beats, and two buttons.
        drawPath(g.panel, PanelDark)
        drawPath(g.panel, Ink.line, style = pen.thin)
        val dial = o(-0.068f, -0.338f)
        drawCircle(Trim, 0.032f * h, dial)
        drawCircle(Ink.line, 0.032f * h, dial, style = pen.thin)
        val needle = (-2.2f + sin(t * 1.3f) * 0.5f)
        drawLine(Ink.line, dial, Offset(dial.x + kotlin.math.cos(needle) * 0.024f * h, dial.y + sin(needle) * 0.024f * h), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
        val fast = a.face == Face.GRIN || a.face == Face.LAUGH || a.face == Face.YUM
        val beat = if (sleeping) 0.1f * (0.5f + 0.5f * sin(t * 1.3f)) else max(0f, sin(t * (if (fast) 9f else 4.6f)))
        drawCircle(Pink.copy(alpha = 0.18f + 0.22f * beat), 0.052f * h, o(0.05f, -0.335f))
        drawPath(heartPath(0.05f * h, -0.335f * h, 0.03f * h * (1f + 0.14f * beat)), if (sleeping) Pink.darken(0.4f) else Pink)
        drawPath(heartPath(0.05f * h, -0.335f * h, 0.03f * h * (1f + 0.14f * beat)), Ink.line, style = pen.thin)
        drawCircle(Color(0xFFFFD23F), 0.014f * h, o(-0.07f, -0.28f))
        drawCircle(Color(0xFF3BC46B), 0.014f * h, o(-0.02f, -0.28f))
        drawCircle(Color(0xFFFF8A5B), 0.014f * h, o(0.03f, -0.28f))

        // ---- arms
        val armColor = shell.darken(0.07f)
        val shoulderL = o(-0.205f, -0.45f)
        val shoulderR = o(0.205f, -0.45f)
        var handL = o(-0.285f, -0.265f + breath * 0.004f)
        when {
            bow > 0.01f -> handL = lerp(handL, o(-0.08f, -0.35f), bow)
            pose == Pose.HELD -> handL = o(-0.30f, -0.64f + sin(t * 11f) * 0.02f)
            pose == Pose.FLOAT -> handL = o(-0.34f, -0.44f + sin(t * 3f) * 0.03f)
            pose == Pose.SWIM -> handL = o(-0.31f, -0.40f + sin(t * 6f) * 0.05f)
            dancing -> handL = o(-0.30f, -0.36f + sin(a.dance) * 0.16f)
            walking -> handL = o(-0.285f, -0.265f - (stepR - stepL) * 0.04f)
        }
        capsule(shoulderL, handL, 0.064f * h, armColor, pen)
        inkedCircle(handL, 0.05f * h, Glove, pen)
        // The right arm never moves: the tray is on it, and whatever is on the tray.
        val elbow = o(0.262f, -0.335f)
        val trayHand = o(0.335f, -0.322f)
        capsule(shoulderR, elbow, 0.064f * h, armColor, pen)
        capsule(elbow, trayHand, 0.058f * h, armColor, pen)
        inkedCircle(trayHand, 0.05f * h, Glove, pen)
        val tray = rect(0.335f * h, -0.378f * h, 0.30f * h, 0.056f * h)
        inkedOval(tray, Silver, pen)
        drawOval(Silver.lighten(0.55f), Offset(tray.left + tray.width * 0.14f, tray.top + tray.height * 0.12f), Size(tray.width * 0.72f, tray.height * 0.4f))
        shine(Offset(tray.left + tray.width * 0.3f, tray.top + tray.height * 0.28f), tray.width * 0.16f, tray.height * 0.14f, 0.9f)
        inkedCircle(shoulderL, 0.046f * h, shell.darken(0.12f), pen)
        inkedCircle(shoulderR, 0.046f * h, shell.darken(0.12f), pen)
        // A napkin over his left arm, as a butler should.
        if (bow < 0.5f && pose != Pose.HELD && pose != Pose.FLOAT && pose != Pose.SWIM && !dancing) {
            // Draped over the forearm: wider than the arm, with two folds hanging down on either side.
            val c = Offset(shoulderL.x + (handL.x - shoulderL.x) * 0.55f, shoulderL.y + (handL.y - shoulderL.y) * 0.55f)
            val along = (kotlin.math.atan2(handL.y - shoulderL.y, handL.x - shoulderL.x) * 180f / PI.toFloat()) - 90f
            rotate(along, pivot = c) {
                val napkin = roundedPoly(0.012f * h, c.x - 0.056f * h, c.y - 0.034f * h, c.x + 0.056f * h, c.y - 0.034f * h, c.x + 0.05f * h, c.y + 0.04f * h, c.x - 0.05f * h, c.y + 0.04f * h)
                inked(napkin, Color.White, pen)
                drawLine(Ink.line.copy(alpha = 0.3f), Offset(c.x - 0.02f * h, c.y - 0.03f * h), Offset(c.x - 0.022f * h, c.y + 0.034f * h), strokeWidth = pen.lw * 0.5f)
            }
        }
    }

    // ---- head, with the antenna, the ears and the face screen
    val tilt = when {
        dancing -> sin(a.dance) * 6f
        pose == Pose.HELD -> sin(t * 5f) * 5f
        a.face == Face.DIZZY -> sin(t * 9f) * 5f
        else -> 0f
    }
    withTransform({
        translate(0f, (bob + dip) * h)
        if (tilt != 0f) rotate(tilt, pivot = o(0f, -0.52f))
    }) {
        // The antenna: it wobbles on its own, more when he walks, dances or is held.
        var wob = sin(t * 3.1f) * 0.02f + sin(t * 1.7f + 1f) * 0.01f
        if (walking) wob += sin(a.walkPhase * PI.toFloat() * 2f) * 0.03f
        if (dancing) wob += sin(a.dance * 2f) * 0.05f
        if (pose == Pose.HELD) wob += sin(t * 11f) * 0.06f
        if (a.face == Face.DIZZY) wob += sin(t * 20f) * 0.07f
        wob += bow * sin(t * 14f) * 0.03f
        val tipY = if (sleeping) -1.0f else if (a.face == Face.WOW || a.face == Face.OOH) -1.12f else -1.085f
        val tipX = if (sleeping) -0.07f else wob
        val stalk = Path().apply {
            moveTo(0f, -0.92f * h)
            quadraticTo(tipX * 0.3f * h, (tipY + 0.06f) * h, tipX * h, tipY * h)
        }
        drawPath(stalk, Ink.line, style = Stroke(0.02f * h + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(stalk, Metal, style = Stroke(0.02f * h, cap = StrokeCap.Round))
        val ballColor = if (sleeping) Color(0xFF9A7680) else Color(0xFFFF6B6B)
        val ball = o(tipX, tipY - 0.012f)
        val pulse = if (sleeping) 0.1f else 0.5f + 0.5f * sin(t * 4.2f)
        drawCircle(ballColor.copy(alpha = 0.14f + 0.2f * pulse), 0.072f * h, ball)
        inkedCircle(ball, if (a.face == Face.WOW) 0.046f * h else 0.037f * h, ballColor, pen)

        // The speaker ears.
        for (side in SIDES) {
            inkedCircle(o(side * 0.305f, -0.725f), 0.056f * h, Trim, pen)
            drawCircle(Metal, 0.024f * h, o(side * 0.305f, -0.725f))
            drawCircle(Ink.line, 0.024f * h, o(side * 0.305f, -0.725f), style = pen.thin)
        }

        inked(g.head, shell.lighten(0.06f), pen)
        drawPath(g.screen, if (sleeping) ScreenAsleep else ScreenDark)
        rolfFace(a, h, pen, t, sleeping)
        clipPath(g.screen) { drawPath(g.gloss, Color.White.copy(alpha = if (sleeping) 0.05f else 0.1f)) }
        drawPath(g.screen, Ink.line, style = pen.stroke)
    }

    // ---- the bow tie, over his chin
    withTransform({ translate(0f, bob * h) }) {
        val c = o(0f, -0.488f)
        for (side in SIDES) {
            val wing = Path().apply {
                moveTo(c.x, c.y)
                lineTo(c.x + side * 0.12f * h, c.y - 0.05f * h)
                quadraticTo(c.x + side * 0.1f * h, c.y, c.x + side * 0.12f * h, c.y + 0.05f * h)
                close()
            }
            inked(wing, BowRed, pen)
        }
        inkedRound(rect(c.x, c.y, 0.045f * h, 0.055f * h), 0.014f * h, BowRed.darken(0.1f), pen, shade = false)
    }
}

/** The face on the screen: glowing eyes and mouth for every [Face], with blinks, looks and talking. */
private fun DrawScope.rolfFace(a: PersonAnim, h: Float, pen: Pen, t: Float, sleeping: Boolean) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val face = a.face
    val glow = if (sleeping) GlowDim else Glow
    val sneezing = a.sneeze > 0f || a.achoo > 0f
    val lx = a.lookX * 0.016f
    val ly = a.lookY * 0.012f
    val line = Stroke(pen.lw * 1.9f, cap = StrokeCap.Round)
    val halo = Stroke(pen.lw * 5f, cap = StrokeCap.Round)
    val haloColor = glow.copy(alpha = 0.18f)

    for (side in SIDES) {
        val e = o(side * 0.125f + lx, -0.742f + ly)
        when {
            sneezing -> {
                // «Ah … ah …»: eyes squeezed into little arrows.
                val dir = -side
                val chevron = Path().apply {
                    moveTo(e.x - dir * 0.04f * h, e.y - 0.045f * h)
                    lineTo(e.x + dir * 0.03f * h, e.y)
                    lineTo(e.x - dir * 0.04f * h, e.y + 0.045f * h)
                }
                drawPath(chevron, haloColor, style = halo)
                drawPath(chevron, glow, style = line)
            }
            face == Face.DIZZY -> {
                rotate(t * 400f * side, pivot = e) {
                    drawArc(glow, 0f, 300f, false, Offset(e.x - 0.042f * h, e.y - 0.042f * h), Size(0.084f * h, 0.084f * h), style = line)
                    drawArc(glow, 0f, 270f, false, Offset(e.x - 0.018f * h, e.y - 0.018f * h), Size(0.036f * h, 0.036f * h), style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
                }
            }
            sleeping || a.blink > 0f -> {
                val dash = Path().apply {
                    moveTo(e.x - 0.05f * h, e.y)
                    lineTo(e.x + 0.05f * h, e.y)
                }
                if (!sleeping) drawPath(dash, haloColor, style = halo)
                drawPath(dash, glow, style = line)
            }
            face == Face.GRIN || face == Face.LAUGH -> {
                val arc = Path().apply {
                    moveTo(e.x - 0.052f * h, e.y + 0.024f * h)
                    quadraticTo(e.x, e.y - 0.07f * h, e.x + 0.052f * h, e.y + 0.024f * h)
                }
                drawPath(arc, haloColor, style = halo)
                drawPath(arc, glow, style = Stroke(pen.lw * 2.2f, cap = StrokeCap.Round))
            }
            face == Face.YUM -> {
                // Heart eyes: he loves his fuel.
                drawCircle(Pink.copy(alpha = 0.2f), 0.07f * h, e)
                val heart = heartPath(e.x, e.y, 0.05f * h * (1f + 0.1f * sin(t * 9f)))
                drawPath(heart, Pink.lighten(0.15f))
                drawPath(heart, Ink.line, style = pen.thin)
            }
            else -> {
                val big = if (face == Face.WOW) 1.28f else if (face == Face.OOH) 1.15f else 1f
                val ew = 0.092f * h * big
                val eh = 0.125f * h * big
                drawOval(haloColor, Offset(e.x - ew * 0.85f, e.y - eh * 0.85f), Size(ew * 1.7f, eh * 1.7f))
                drawOval(glow, Offset(e.x - ew / 2f, e.y - eh / 2f), Size(ew, eh))
                drawOval(Color.White.copy(alpha = 0.9f), Offset(e.x - ew * 0.34f, e.y - eh * 0.36f), Size(ew * 0.32f, eh * 0.27f))
            }
        }
    }

    // ---- mouth
    val m = o(0f, -0.645f)
    val chewing = a.chew > 0f || face == Face.CHOMP
    val talking = a.talk > 0f
    when {
        talking && face != Face.SLEEP -> {
            // Talking: a little sound meter.
            for (k in -2..2) {
                val hgt = (0.014f + 0.05f * abs(sin(t * 17f + k * 1.9f))) * h
                drawLine(glow, Offset(m.x + k * 0.038f * h, m.y - hgt / 2f), Offset(m.x + k * 0.038f * h, m.y + hgt / 2f), strokeWidth = 0.022f * h, cap = StrokeCap.Round)
            }
        }
        chewing -> {
            val open = (0.02f + 0.05f * abs(sin(t * 20f))) * h
            val r = CornerRadius(0.012f * h)
            drawRoundRect(Color(0xFF0B1220), Offset(m.x - 0.1f * h, m.y - open / 2f), Size(0.2f * h, open), r)
            drawRoundRect(glow, Offset(m.x - 0.1f * h, m.y - open / 2f), Size(0.2f * h, open), r, style = Stroke(pen.lw * 1.2f))
            for (k in -1..1) drawLine(glow, Offset(m.x + k * 0.05f * h, m.y - open / 2f), Offset(m.x + k * 0.05f * h, m.y + open / 2f), strokeWidth = pen.lw * 0.8f)
        }
        face == Face.GRIN || face == Face.LAUGH -> {
            val w = 0.105f * h
            val top = m.y - 0.018f * h
            val depth = (0.07f + (if (face == Face.LAUGH) 0.016f * abs(sin(t * 14f)) else 0f)) * h
            val mouth = Path().apply {
                moveTo(m.x - w, top)
                quadraticTo(m.x, top + 0.012f * h, m.x + w, top)
                quadraticTo(m.x + w * 0.9f, top + depth, m.x, top + depth)
                quadraticTo(m.x - w * 0.9f, top + depth, m.x - w, top)
                close()
            }
            drawPath(mouth, glow)
            clipPath(mouth) { for (k in -1..1) drawLine(ScreenDark, Offset(m.x + k * 0.05f * h, top), Offset(m.x + k * 0.05f * h, top + depth), strokeWidth = pen.lw * 0.8f) }
        }
        face == Face.OOH || face == Face.WOW || sneezing -> {
            val s = if (face == Face.WOW) 1.4f else 1f
            val ring = Offset(m.x - 0.027f * h * s, m.y - 0.034f * h * s)
            drawOval(Color(0xFF0B1220), ring, Size(0.054f * h * s, 0.068f * h * s))
            drawOval(glow, ring, Size(0.054f * h * s, 0.068f * h * s), style = line)
        }
        face == Face.SLEEP -> drawLine(glow, Offset(m.x - 0.03f * h, m.y), Offset(m.x + 0.03f * h, m.y), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
        face == Face.DIZZY -> {
            val zig = Path().apply {
                moveTo(m.x - 0.075f * h, m.y)
                for (k in 1..5) lineTo(m.x - 0.075f * h + k * 0.03f * h, m.y + (if (k % 2 == 1) -0.02f else 0.02f) * h)
            }
            drawPath(zig, glow, style = line)
        }
        else -> {
            val smile = Path().apply {
                moveTo(m.x - 0.078f * h, m.y - 0.012f * h)
                quadraticTo(m.x, m.y + 0.05f * h, m.x + 0.078f * h, m.y - 0.012f * h)
            }
            drawPath(smile, haloColor, style = halo)
            drawPath(smile, glow, style = line)
        }
    }

    // ---- warm cheeks, and a sparkle of wonder
    if (!sleeping) {
        val cheek = Color(0xFFFF8FB0).copy(alpha = 0.42f)
        drawOval(cheek, o(-0.24f, -0.688f), Size(0.07f * h, 0.042f * h))
        drawOval(cheek, o(0.17f, -0.688f), Size(0.07f * h, 0.042f * h))
        if (face == Face.WOW) {
            val k = 0.6f + 0.4f * sin(t * 6f)
            twinkle(o(-0.205f, -0.84f), 0.04f * h, Color.White, k)
            twinkle(o(0.2f, -0.81f), 0.03f * h, Color.White, 1f - k * 0.5f)
        }
    }
}
