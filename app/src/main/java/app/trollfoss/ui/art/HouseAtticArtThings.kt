package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.domain.ThingType
import kotlin.math.cos
import kotlin.math.sin

/*
 * Storhuset, attic: the things. A torch, the costumes from the trunk (a pirate's hat and eye patch, a knight's
 * helmet, a giant flower, a ghost's sheet, funny glasses) and the silly records for the gramophone. Hats are
 * drawn as they sit on a head; glasses are centred in their box (docs/ART_GUIDE.md).
 */

internal fun DrawScope.drawAtticThingImpl(type: ThingType, variant: Int, w: Float, h: Float, pen: Pen): Boolean {
    when (type) {
        ThingType.AT_FLASHLIGHT -> thFlashlight(w, h, pen)
        ThingType.AT_PIRATE_HAT -> thPirateHat(w, h, pen)
        ThingType.AT_KNIGHT_HELMET -> thKnightHelmet(w, h, pen)
        ThingType.AT_FLOWER_HAT -> thFlowerHat(w, h, pen)
        ThingType.AT_SHEET_HAT -> thSheetHat(w, h, pen)
        ThingType.AT_EYE_PATCH -> thEyePatch(w, h, pen)
        ThingType.AT_FUNNY_GLASSES -> thFunnyGlasses(w, h, pen)
        ThingType.AT_RECORD -> thRecord(variant, w, h, pen)
        else -> return false
    }
    return true
}

private fun DrawScope.thFlashlight(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val body = Color(0xFF2FB5A8)
    // The barrel with a grip band, then the wider head with its lens.
    inkedRound(Rect(-0.46f * w, -0.8f * h, 0.2f * w, -0.1f * h), 0.2f * h, body, pen)
    drawRect(body.darken(0.3f), o(-0.28f, -0.78f), Size(0.07f * w, 0.66f * h))
    drawRect(body.darken(0.3f), o(-0.12f, -0.78f), Size(0.07f * w, 0.66f * h))
    val head = thSketch(w, h) { m(0.18f, -0.95f); l(0.46f, -1f); l(0.46f, 0f); l(0.18f, -0.05f); z() }
    inked(head, Color(0xFFFFC83D), pen)
    // The lens glows and sparkles; a switch sits on the barrel.
    val lens = Rect(0.38f * w, -0.92f * h, 0.52f * w, -0.08f * h)
    drawOval(Color(0xFFFFF7DA), lens.topLeft, lens.size)
    drawOval(Ink.line, lens.topLeft, lens.size, style = pen.thin)
    val g = 0.6f + 0.4f * sin(pen.t * 3f)
    drawCircle(Color(0xFFFFF1C2).copy(alpha = 0.35f * g), 0.45f * h, o(0.5f, -0.5f))
    thGlint(o(0.47f, -0.78f), 0.12f * h, pen.t, 2.6f, 0f)
    inkedRound(Rect(-0.1f * w, -0.92f * h, 0.04f * w, -0.78f * h), 0.05f * h, Color(0xFFFF8A3D), pen, shade = false)
    shine(o(-0.3f, -0.62f), 0.2f * w, 0.1f * h, 0.5f)
}

private fun DrawScope.thPirateHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val black = Color(0xFF3A3340)
    // The big crescent brim, turned up at both ends, and a dome of a crown.
    val brim = thSketch(w, h) {
        m(-0.5f, -0.78f); q(-0.35f, -0.2f, 0f, -0.08f); q(0.35f, -0.2f, 0.5f, -0.78f)
        q(0.3f, -0.55f, 0.18f, -0.5f); l(-0.18f, -0.5f); q(-0.3f, -0.55f, -0.5f, -0.78f); z()
    }
    inked(brim, black, pen)
    val crown = thSketch(w, h) { m(-0.3f, -0.45f); q(-0.28f, -0.95f, 0f, -0.95f); q(0.28f, -0.95f, 0.3f, -0.45f); q(0f, -0.35f, -0.3f, -0.45f); z() }
    inked(crown, black.lighten(0.08f), pen)
    // A cream band along the brim, and a gold star badge: a friendly pirate, not a scary one.
    drawPath(thSketch(w, h) { m(-0.46f, -0.74f); q(-0.33f, -0.24f, 0f, -0.14f); q(0.33f, -0.24f, 0.46f, -0.74f) }, Color(0xFFF7F0DC), style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
    inked(starPath(o(0f, -0.62f), 0.17f * w, 0.075f * w), Color(0xFFFFC83D), pen, shade = false)
    thGlint(o(0.03f, -0.66f), 0.06f * w, pen.t, 2.4f, 0f)
    drawPath(thSketch(w, h) { m(-0.15f, -0.8f); q(-0.05f, -0.92f, 0.1f, -0.9f) }, Color.White.copy(alpha = 0.3f), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
}

private fun DrawScope.thKnightHelmet(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val steel = Color(0xFFBAC4D4)
    // A fluffy plume first, behind the dome.
    val plume = Color(0xFF6AB7C2)
    for ((i, p) in listOf(Triple(-0.12f, -1.0f, 0.2f), Triple(0.0f, -1.08f, 0.24f), Triple(0.13f, -1.0f, 0.2f)).withIndex()) {
        drawCircle(plume.darken(0.12f), p.third * w, o(p.first, p.second + 0.04f))
        drawCircle(plume, p.third * w * 0.88f, o(p.first - 0.01f, p.second))
        if (i == 1) drawCircle(plume.lighten(0.3f), p.third * w * 0.4f, o(-0.03f, p.second - 0.05f))
    }
    val dome = thSketch(w, h) {
        m(-0.46f, -0.08f); l(-0.46f, -0.4f); q(-0.46f, -0.95f, 0f, -0.95f); q(0.46f, -0.95f, 0.46f, -0.4f); l(0.46f, -0.08f); q(0f, -0.2f, -0.46f, -0.08f); z()
    }
    inked(dome, steel, pen)
    // A ridge down the middle, a brow band with rivets and a raised visor with a slot.
    drawPath(thSketch(w, h) { m(0f, -0.95f); l(0f, -0.25f) }, steel.darken(0.25f), style = Stroke(pen.lw * 1.6f))
    drawRect(steel.darken(0.15f), o(-0.46f, -0.3f), Size(0.92f * w, 0.1f * h))
    drawRect(Ink.line, o(-0.46f, -0.3f), Size(0.92f * w, 0.1f * h), style = pen.thin)
    for (x in listOf(-0.36f, -0.12f, 0.12f, 0.36f)) drawCircle(Color(0xFFFFE18A), 0.018f * w, o(x, -0.25f))
    val slot = thSketch(w, h) { m(-0.32f, -0.6f); l(0.32f, -0.6f); l(0.3f, -0.45f); l(-0.3f, -0.45f); z() }
    drawPath(slot, Color(0xFF2B2140))
    drawPath(slot, Ink.line, style = pen.thin)
    // two friendly eyes peeking out of the slot
    for (s in SIDES) {
        drawCircle(Color.White, 0.05f * w, o(s * 0.15f, -0.52f))
        drawCircle(Ink.line, 0.025f * w, o(s * 0.15f + 0.005f, -0.51f))
    }
    shine(o(-0.28f, -0.8f), 0.12f * w, 0.2f * h, 0.7f)
}

private fun DrawScope.thFlowerHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val c = o(0f, -0.56f)
    // Two big leaves out to the sides.
    for (s in SIDES) {
        val leaf = thSketch(w, h) { m(s * 0.2f, -0.3f); q(s * 0.55f, -0.15f, s * 0.58f, -0.5f); q(s * 0.4f, -0.45f, s * 0.2f, -0.3f); z() }
        inked(leaf, Color(0xFF5DBB4A), pen)
        drawPath(thSketch(w, h) { m(s * 0.24f, -0.31f); q(s * 0.42f, -0.33f, s * 0.52f, -0.44f) }, Color(0xFF3F8F6C), style = Stroke(pen.lw * 0.7f))
    }
    // Twelve yellow petals round a smiling middle.
    val sway = sin(pen.t * 1.8f) * 0.02f
    for (k in 0 until 12) {
        val a = k * 0.5236f + sway
        val pc = Offset(c.x + cos(a) * 0.3f * h, c.y + sin(a) * 0.3f * h)
        rotate(a * 57.2958f, pc) {
            val r = Rect(pc.x - 0.19f * h, pc.y - 0.085f * h, pc.x + 0.19f * h, pc.y + 0.085f * h)
            drawOval(if (k % 2 == 0) Color(0xFFFFC83D) else Color(0xFFFFB02E), r.topLeft, r.size)
            drawOval(Ink.line, r.topLeft, r.size, style = pen.thin)
        }
    }
    drawCircle(Color(0xFF9A5B2E), 0.27f * h, c)
    drawCircle(Ink.line, 0.27f * h, c, style = pen.stroke)
    for (k in 0 until 7) drawCircle(Color(0xFF6E3F1E), 0.014f * h, Offset(c.x + cos(k * 0.9f + 0.4f) * 0.19f * h, c.y + sin(k * 0.9f + 0.4f) * 0.19f * h))
    for (s in SIDES) {
        drawCircle(Color.White, 0.05f * h, Offset(c.x + s * 0.09f * h, c.y - 0.05f * h))
        drawCircle(Ink.line, 0.028f * h, Offset(c.x + s * 0.09f * h, c.y - 0.04f * h))
    }
    drawArc(Ink.line, 10f, 160f, false, Offset(c.x - 0.08f * h, c.y + 0.0f * h), Size(0.16f * h, 0.1f * h), style = Stroke(pen.lw))
    drawCircle(Color(0xFFFF6F91).copy(alpha = 0.5f), 0.03f * h, Offset(c.x - 0.15f * h, c.y + 0.04f * h))
    drawCircle(Color(0xFFFF6F91).copy(alpha = 0.5f), 0.03f * h, Offset(c.x + 0.15f * h, c.y + 0.04f * h))
}

private fun DrawScope.thSheetHat(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val sheet = Color(0xFFF7F8FA)
    // A bed sheet draped over the head: a dome with a wavy hem hanging down at the sides.
    val body = thSketch(w, h) {
        m(-0.5f, -0.1f); q(-0.52f, -0.5f, -0.3f, -0.8f); q(0f, -1.05f, 0.3f, -0.8f); q(0.52f, -0.5f, 0.5f, -0.1f)
        q(0.4f, 0.0f, 0.3f, -0.08f); q(0.2f, 0.0f, 0.1f, -0.1f); q(0f, 0f, -0.1f, -0.1f); q(-0.2f, 0f, -0.3f, -0.08f); q(-0.4f, 0f, -0.5f, -0.1f); z()
    }
    inked(body, sheet, pen)
    for (s in SIDES) drawPath(thSketch(w, h) { m(s * 0.2f, -0.9f); q(s * 0.32f, -0.5f, s * 0.3f, -0.12f) }, Color(0xFFCFC8D8), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    // Googly eyes on the front: the funniest thing to wear on a head.
    for (s in SIDES) {
        val e = o(s * 0.16f, -0.58f)
        drawCircle(Color.White, 0.13f * w, e)
        drawCircle(Ink.line, 0.13f * w, e, style = pen.stroke)
        val wob = sin(pen.t * 4f + s) * 0.03f * w
        drawCircle(Ink.line, 0.06f * w, Offset(e.x + wob, e.y + 0.03f * w))
        drawCircle(Color.White, 0.02f * w, Offset(e.x + wob - 0.015f * w, e.y + 0.015f * w))
    }
}

private fun DrawScope.thEyePatch(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val black = Color(0xFF3A3340)
    // The strap goes round the head on a slant.
    drawPath(thSketch(w, h) { m(-0.5f, -0.9f); q(0f, -0.4f, 0.5f, -0.75f) }, Ink.line, style = Stroke(pen.lw * 3.2f, cap = StrokeCap.Round))
    drawPath(thSketch(w, h) { m(-0.5f, -0.9f); q(0f, -0.4f, 0.5f, -0.75f) }, black, style = Stroke(pen.lw * 1.8f, cap = StrokeCap.Round))
    val patch = Rect(-0.36f * w, -0.9f * h, -0.02f * w, -0.1f * h)
    inkedOval(patch, black, pen)
    shine(o(-0.25f, -0.62f), 0.07f * w, 0.28f * h, 0.5f)
    // A golden stitch: the patch has a heart sewn on it.
    drawPath(fxHeart(o(-0.19f, -0.5f).x, o(-0.19f, -0.5f).y, 0.06f * w), Color(0xFFFFC83D))
}

private fun DrawScope.thFunnyGlasses(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val rim = Color(0xFF3A3340)
    // Round glasses, huge bushy brows, a big pink nose and a moustache: the classic disguise.
    for (s in SIDES) {
        val c = o(s * 0.24f, -0.58f)
        drawCircle(Color.White.copy(alpha = 0.55f), 0.22f * w, c)
        drawCircle(rim, 0.22f * w, c, style = Stroke(pen.lw * 2.4f))
        drawCircle(Ink.line, 0.22f * w + pen.lw * 1.2f, c, style = Stroke(pen.lw * 0.6f))
        drawCircle(Color.White, 0.04f * w, Offset(c.x + s * 0.0f - 0.07f * w, c.y - 0.08f * w), alpha = 0.7f)
        // the brow
        drawPath(thSketch(w, h) { m(s * 0.02f, -1.0f); q(s * 0.24f, -1.12f, s * 0.46f, -0.98f) }, rim, style = Stroke(pen.lw * 3f, cap = StrokeCap.Round))
    }
    drawLine(rim, o(-0.02f, -0.58f), o(0.02f, -0.58f), pen.lw * 2.2f)
    val nose = Rect(-0.1f * w, -0.62f * h, 0.1f * w, -0.2f * h)
    inkedOval(nose, Color(0xFFF08CB8), pen)
    val mous = thSketch(w, h) {
        m(-0.34f, -0.2f); q(-0.2f, -0.45f, 0f, -0.3f); q(0.2f, -0.45f, 0.34f, -0.2f); q(0.2f, -0.04f, 0f, -0.14f); q(-0.2f, -0.04f, -0.34f, -0.2f); z()
    }
    inked(mous, Color(0xFF4A2E1C), pen)
    for (k in 0 until 5) drawLine(Color(0xFF6E4630), o(-0.26f + k * 0.13f, -0.2f), o(-0.24f + k * 0.13f, -0.12f), pen.lw * 0.8f, StrokeCap.Round)
}

private val RECORD_LABELS = listOf(Color(0xFFFF8A3D), Color(0xFFFFC83D), Color(0xFF3BC46B), Color(0xFF8B5CF6), Color(0xFF2F9BFF), Color(0xFFFF6FA8))

private fun DrawScope.thRecord(v: Int, w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h / 2f)
    val r = h * 0.5f
    val label = RECORD_LABELS[v.mod(RECORD_LABELS.size)]
    drawCircle(Color(0xFF2B2140), r, c)
    drawCircle(Ink.line, r, c, style = pen.stroke)
    for (k in 1..3) drawCircle(Color.White.copy(alpha = 0.14f), r * (0.95f - 0.15f * k), c, style = Stroke(pen.lw * 0.5f))
    drawCircle(label, r * 0.4f, c)
    drawCircle(Ink.line, r * 0.4f, c, style = pen.thin)
    // A small picture on the label tells the records apart: star, heart, note, zigzag, spiral and flower.
    val s = r * 0.22f
    when (v.mod(6)) {
        0 -> drawPath(starPath(c, s, s * 0.45f), Color.White)
        1 -> drawPath(fxHeart(c.x, c.y, s * 0.9f), Color.White)
        2 -> fxNote(c, s * 0.8f, Color.White, 1f)
        3 -> drawPath(Path().apply { moveTo(c.x - s, c.y + s * 0.4f); lineTo(c.x - s * 0.4f, c.y - s * 0.5f); lineTo(c.x + s * 0.2f, c.y + s * 0.5f); lineTo(c.x + s, c.y - s * 0.4f) }, Color.White, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
        4 -> {
            val spiral = Path().apply {
                moveTo(c.x, c.y)
                for (k in 1..16) {
                    val a = k * 0.6f
                    lineTo(c.x + cos(a) * s * k / 16f * 1.1f, c.y + sin(a) * s * k / 16f * 1.1f)
                }
            }
            drawPath(spiral, Color.White, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        }
        else -> {
            for (k in 0 until 5) drawCircle(Color.White, s * 0.38f, Offset(c.x + cos(k * 1.2566f) * s * 0.6f, c.y + sin(k * 1.2566f) * s * 0.6f))
            drawCircle(Color(0xFFFFC83D), s * 0.3f, c)
        }
    }
    drawCircle(Ink.line, r * 0.07f, c)
    drawArc(Color.White.copy(alpha = 0.6f), 200f, 50f, false, Offset(c.x - r * 0.85f, c.y - r * 0.85f), Size(r * 1.7f, r * 1.7f), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
}
