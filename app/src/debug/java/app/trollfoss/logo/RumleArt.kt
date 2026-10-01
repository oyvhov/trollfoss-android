package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.capsule
import app.trollfoss.ui.art.darken
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.inkedOval
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.rect
import app.trollfoss.ui.art.shine

/**
 * Rumle the troll as the mascot: the head of the game's Rumle (moss-teal skin, curly brown hair, big
 * pointed ears, overalls) drawn close up with the friendly extras of the brand: a big round nose, one
 * tooth, a tiny sprout and a waving hand. [c] is the middle of the head and [r] its radius; everything
 * below the head is drawn too and left to the caller's clip. [wave] 0 hides the raised hand.
 */
internal fun DrawScope.drawRumleBust(c: Offset, r: Float, lw: Float, wave: Boolean = true, sprout: Boolean = true) {
    val pen = logoPen(lw)
    fun o(x: Float, y: Float) = Offset(c.x + x * r, c.y + y * r)
    val skin = androidx.compose.ui.graphics.lerp(L.skin, L.mint, 0.14f).lighten(0.05f)
    val skinLight = skin.lighten(0.2f)
    val brown = L.woodDark

    // ---- ears (behind the head)
    for (side in listOf(-1f, 1f)) {
        val ear = Path().apply {
            moveTo(o(side * 0.74f, -0.28f).x, o(side * 0.74f, -0.28f).y)
            quadraticTo(o(side * 1.3f, -0.34f).x, o(side * 1.3f, -0.34f).y, o(side * 1.72f, -0.9f).x, o(side * 1.72f, -0.9f).y)
            quadraticTo(o(side * 1.5f, 0.08f).x, o(side * 1.5f, 0.08f).y, o(side * 0.9f, 0.44f).x, o(side * 0.9f, 0.44f).y)
            close()
        }
        inked(ear, skin, pen)
        val inner = Path().apply {
            moveTo(o(side * 0.92f, -0.12f).x, o(side * 0.92f, -0.12f).y)
            quadraticTo(o(side * 1.26f, -0.2f).x, o(side * 1.26f, -0.2f).y, o(side * 1.54f, -0.62f).x, o(side * 1.54f, -0.62f).y)
            quadraticTo(o(side * 1.38f, 0.0f).x, o(side * 1.38f, 0.0f).y, o(side * 0.98f, 0.28f).x, o(side * 0.98f, 0.28f).y)
            close()
        }
        drawPath(inner, skin.darken(0.2f))
    }

    // ---- body: shirt, overalls
    val torso = Path().apply {
        moveTo(o(-0.58f, 0.78f).x, o(-0.58f, 0.78f).y)
        quadraticTo(o(-1.0f, 0.8f).x, o(-1.0f, 0.8f).y, o(-1.06f, 1.3f).x, o(-1.06f, 1.3f).y)
        lineTo(o(-1.2f, 4.6f).x, o(-1.2f, 4.6f).y)
        lineTo(o(1.2f, 4.6f).x, o(1.2f, 4.6f).y)
        lineTo(o(1.06f, 1.3f).x, o(1.06f, 1.3f).y)
        quadraticTo(o(1.0f, 0.8f).x, o(1.0f, 0.8f).y, o(0.58f, 0.78f).x, o(0.58f, 0.78f).y)
        close()
    }
    inked(torso, L.shirt, pen, outline = false)
    clipPath(torso) {
        // Overalls: trousers and bib.
        drawRect(brown, o(-1.4f, 2.7f), Size(2.8f * r, 3f * r))
        drawLine(Ink.line, o(-1.4f, 2.7f), o(1.4f, 2.7f), strokeWidth = lw * 0.8f)
        val bib = Rect(o(-0.66f, 1.32f), o(0.66f, 3.2f))
        inkedRound(bib, 0.16f * r, brown, pen, shade = false)
        drawRoundRectShade(bib, 0.16f * r, brown)
    }
    drawPath(torso, Ink.line, style = pen.stroke)
    for (side in listOf(-1f, 1f)) {
        capsule(o(side * 0.5f, 1.36f), o(side * 0.58f, 0.9f), 0.3f * r, brown, pen)
        inkedCircle(o(side * 0.5f, 1.4f), 0.1f * r, Color(0xFFFFD23F), pen, shade = false)
    }

    // The head casts a soft shadow on the shirt.
    clipPath(torso) { drawCircle(Ink.shadow, r * 1.02f, Offset(c.x, c.y + 0.1f * r)) }

    // ---- arms
    if (wave) {
        val hand = o(1.6f, 0.5f)
        capsule(o(0.94f, 1.2f), o(1.5f, 0.68f), 0.34f * r, L.shirt, pen)
        // Fingers fan out above the palm, the thumb points in.
        for ((k, ang) in listOf(-30f, -10f, 10f, 30f).withIndex()) {
            val a = Math.toRadians((ang - 90f + 8f).toDouble())
            val len = (0.5f + (if (k == 1 || k == 2) 0.1f else 0f)) * r
            val from = Offset(hand.x + (0.1f * r * Math.cos(a)).toFloat(), hand.y + (0.1f * r * Math.sin(a)).toFloat())
            val to = Offset(hand.x + (len * Math.cos(a)).toFloat(), hand.y + (len * Math.sin(a)).toFloat())
            capsule(from, to, 0.19f * r, skin, pen)
        }
        capsule(Offset(hand.x - 0.18f * r, hand.y + 0.04f * r), Offset(hand.x - 0.5f * r, hand.y - 0.16f * r), 0.2f * r, skin, pen)
        inkedCircle(hand, 0.3f * r, skin, pen)
        // Wave marks.
        for ((i, rad) in listOf(0.64f, 0.88f).withIndex()) {
            drawArc(
                Ink.line, -78f + i * 2f, 46f - i * 8f, false,
                Offset(hand.x - rad * r, hand.y - rad * r - 0.1f * r), Size(rad * r * 2f, rad * r * 2f),
                style = Stroke(lw * 0.95f, cap = StrokeCap.Round),
            )
        }
    } else {
        capsule(o(0.94f, 1.2f), o(1.4f, 2.3f), 0.34f * r, L.shirt, pen)
        inkedCircle(o(1.42f, 2.42f), 0.27f * r, skin, pen)
    }

    // ---- head
    inkedCircle(c, r, skin, pen)
    drawOval(Color.White.copy(alpha = 0.16f), o(-0.62f, -0.88f), Size(0.9f * r, 0.4f * r))

    // ---- hair: a ring of curls round the top of the head and a curly fringe, with the sprout growing out of it
    val hair = L.hair
    val head = Path().apply { addOval(Rect(c, r)) }
    clipPath(head) {
        drawRect(hair, Offset(c.x - 1.1f * r, c.y - 1.2f * r), Size(2.2f * r, 0.6f * r))
        drawRect(hair.darken(0.12f), Offset(c.x - 1.1f * r, c.y - 1.2f * r), Size(2.2f * r, 0.3f * r))
    }
    for (a in listOf(208f, 238f, 270f, 302f, 332f)) {
        inkedCircle(polar(c, 0.98f * r, a), 0.36f * r, hair, pen)
    }
    for ((i, x) in listOf(-0.5f, -0.17f, 0.17f, 0.5f).withIndex()) {
        val y = -0.74f + 0.1f * kotlin.math.abs(x) + (if (i % 2 == 0) 0f else -0.03f)
        inkedCircle(o(x, y), 0.24f * r, hair, pen)
    }
    // A glint on the curls.
    drawArc(
        hair.lighten(0.4f), 236f, 30f, false, Offset(c.x - 0.58f * r, c.y - 1.2f * r), Size(1.14f * r, 0.9f * r),
        style = Stroke(lw * 0.8f, cap = StrokeCap.Round),
    )
    if (sprout) {
        val base = o(0.06f, -1.17f)
        val tip = o(0.1f, -1.72f)
        val stem = Path().apply {
            moveTo(base.x, base.y)
            quadraticTo(o(-0.04f, -1.5f).x, o(-0.04f, -1.5f).y, tip.x, tip.y)
        }
        drawPath(stem, Ink.line, style = Stroke(lw * 2.9f, cap = StrokeCap.Round))
        drawPath(stem, L.mintDeep, style = Stroke(lw * 1.15f, cap = StrokeCap.Round))
        val right = leafPath(tip, o(0.78f, -2.0f), 0.17f * r, 0.08f * r)
        val left = leafPath(tip, o(-0.5f, -2.0f), 0.15f * r, -0.06f * r)
        inked(left, L.mint, pen)
        inked(right, L.mint, pen)
        drawLine(L.mintDeep, tip, o(0.52f, -1.9f), strokeWidth = lw * 0.55f, cap = StrokeCap.Round)
    }

    // ---- face
    drawOval(L.blush, o(-0.98f, 0.12f), Size(0.46f * r, 0.28f * r))
    drawOval(L.blush, o(0.52f, 0.12f), Size(0.46f * r, 0.28f * r))
    for (side in listOf(-1f, 1f)) {
        val e = o(side * 0.4f, -0.02f)
        val white = Rect(e.x - 0.25f * r, e.y - 0.3f * r, e.x + 0.25f * r, e.y + 0.3f * r)
        drawOval(Color.White, white.topLeft, white.size)
        drawOval(Ink.line, white.topLeft, white.size, style = pen.stroke)
        val p = Offset(e.x + 0.06f * r, e.y + 0.05f * r)
        drawCircle(Ink.line, 0.17f * r, p)
        drawCircle(Color.White, 0.065f * r, Offset(p.x - 0.06f * r, p.y - 0.07f * r))
        // Cheerful raised brows.
        val brow = Path().apply {
            moveTo(e.x - 0.27f * r, e.y - 0.4f * r)
            quadraticTo(e.x, e.y - 0.55f * r, e.x + 0.27f * r, e.y - 0.42f * r)
        }
        drawPath(brow, Ink.line, style = Stroke(lw * 1.25f, cap = StrokeCap.Round))
    }
    // The big round nose.
    val nose = o(0f, 0.3f)
    inkedCircle(nose, 0.29f * r, skinLight, pen)
    drawOval(Color.White.copy(alpha = 0.55f), Offset(nose.x - 0.16f * r, nose.y - 0.19f * r), Size(0.15f * r, 0.1f * r))
    drawCircle(skin.darken(0.35f), 0.03f * r, Offset(nose.x - 0.07f * r, nose.y + 0.15f * r))
    drawCircle(skin.darken(0.35f), 0.03f * r, Offset(nose.x + 0.07f * r, nose.y + 0.15f * r))
    // The grin with one tooth.
    val w = 0.52f
    val d = 0.3f
    val mouth = Path().apply {
        moveTo(o(-w, 0.66f).x, o(-w, 0.66f).y)
        quadraticTo(o(0f, 0.7f).x, o(0f, 0.7f).y, o(w, 0.66f).x, o(w, 0.66f).y)
        quadraticTo(o(w * 0.92f, 0.66f + d).x, o(w * 0.92f, 0.66f + d).y, o(0f, 0.66f + d).x, o(0f, 0.66f + d).y)
        quadraticTo(o(-w * 0.92f, 0.66f + d).x, o(-w * 0.92f, 0.66f + d).y, o(-w, 0.66f).x, o(-w, 0.66f).y)
        close()
    }
    drawPath(mouth, L.mouth)
    clipPath(mouth) {
        drawOval(L.tongue, o(-0.26f, 0.84f), Size(0.5f * r, 0.34f * r))
        val tooth = Rect(o(-0.2f, 0.62f), o(0.04f, 0.9f))
        drawRoundRect(Color.White, tooth.topLeft, tooth.size, androidx.compose.ui.geometry.CornerRadius(0.07f * r, 0.07f * r))
        drawRoundRect(Ink.line, tooth.topLeft, tooth.size, androidx.compose.ui.geometry.CornerRadius(0.07f * r, 0.07f * r), style = Stroke(lw * 0.7f))
    }
    drawPath(mouth, Ink.line, style = pen.stroke)
}

/** The shade crescent along the lower right of a rounded rectangle, for flat clipped shapes. */
private fun DrawScope.drawRoundRectShade(rect: Rect, corner: Float, color: Color) {
    val s = minOf(rect.width, rect.height) * 0.1f
    clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, androidx.compose.ui.geometry.CornerRadius(corner, corner))) }) {
        withTransform({ translate(-s * 0.6f, -s) }) {
            drawRoundRect(color.lighten(0.08f), rect.topLeft, Size(rect.width - s * 0.6f, rect.height - s), androidx.compose.ui.geometry.CornerRadius(corner, corner))
        }
    }
}

/**
 * The same extras laid over the game's own Rumle (drawn with `drawPerson`): sprout, big round nose and one
 * tooth, so the cast on the banner shows the mascot. [feet] is the figure's origin and [h] its height.
 */
internal fun DrawScope.drawRumleAccents(feet: Offset, h: Float, lw: Float, grin: Boolean) {
    val pen = logoPen(lw)
    fun o(x: Float, y: Float) = Offset(feet.x + x * h, feet.y + y * h)
    val skin = L.skin
    // The sprout grows from the curls on top of the head.
    val base = o(0.012f, -0.93f)
    val tip = o(0.03f, -1.04f)
    val stem = Path().apply {
        moveTo(base.x, base.y)
        quadraticTo(o(-0.01f, -1.0f).x, o(-0.01f, -1.0f).y, tip.x, tip.y)
    }
    drawPath(stem, Ink.line, style = Stroke(lw * 2.6f, cap = StrokeCap.Round))
    drawPath(stem, L.mintDeep, style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
    inked(leafPath(tip, o(0.15f, -1.085f), 0.03f * h, 0.012f * h), L.mint, pen)
    inked(leafPath(tip, o(-0.1f, -1.08f), 0.027f * h, -0.01f * h), L.mint, pen)
    // The big round nose, over the small one the game draws.
    val nose = o(0f, -0.645f)
    inkedCircle(nose, 0.052f * h, skin.lighten(0.2f), pen)
    drawOval(Color.White.copy(alpha = 0.55f), Offset(nose.x - 0.03f * h, nose.y - 0.034f * h), Size(0.028f * h, 0.018f * h))
    // One tooth, hanging from the smile.
    if (!grin) {
        val tooth = Rect(o(-0.03f, -0.593f), o(0.0f, -0.566f))
        drawRoundRect(Color.White, tooth.topLeft, tooth.size, androidx.compose.ui.geometry.CornerRadius(0.006f * h, 0.006f * h))
        drawRoundRect(Ink.line, tooth.topLeft, tooth.size, androidx.compose.ui.geometry.CornerRadius(0.006f * h, 0.006f * h), style = Stroke(lw * 0.6f))
    }
}

/**
 * Rumle's waving hand over the game's figure: the game's own raised hand sits behind the big ear, so a
 * clear arm and an open hand are drawn outside it, with wave marks.
 */
internal fun DrawScope.drawRumleWave(feet: Offset, h: Float, lw: Float, mirror: Boolean = false) {
    val pen = logoPen(lw)
    val m = if (mirror) -1f else 1f
    fun o(x: Float, y: Float) = Offset(feet.x + m * x * h, feet.y + y * h)
    val skin = L.skin
    val sleeve = L.shirt
    val hand = o(0.47f, -0.5f)
    capsule(o(0.165f, -0.43f), o(0.4f, -0.49f), 0.078f * h, sleeve, pen)
    for ((k, ang) in listOf(-30f, -10f, 10f, 30f).withIndex()) {
        val a = Math.toRadians((ang - 90f + 8f).toDouble())
        val len = (0.105f + (if (k == 1 || k == 2) 0.02f else 0f)) * h
        val from = Offset(hand.x + m * (0.02f * h * Math.cos(a)).toFloat(), hand.y + (0.02f * h * Math.sin(a)).toFloat())
        val to = Offset(hand.x + m * (len * Math.cos(a)).toFloat(), hand.y + (len * Math.sin(a)).toFloat())
        capsule(from, to, 0.038f * h, skin, pen)
    }
    capsule(Offset(hand.x - m * 0.035f * h, hand.y + 0.01f * h), Offset(hand.x - m * 0.095f * h, hand.y - 0.03f * h), 0.04f * h, skin, pen)
    inkedCircle(hand, 0.06f * h, skin, pen)
    for ((i, rad) in listOf(0.13f, 0.18f).withIndex()) {
        val start = if (mirror) 180f - (-75f + i * 2f) - (48f - i * 8f) else -75f + i * 2f
        drawArc(
            Ink.line, start, 48f - i * 8f, false,
            Offset(hand.x - rad * h, hand.y - rad * h - 0.02f * h), Size(rad * h * 2f, rad * h * 2f),
            style = Stroke(lw * 0.9f, cap = StrokeCap.Round),
        )
    }
}
