package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate

// The joke things: the whoopee cushion, the banana peel and the pepper shaker.

private val WHOOPEE = longArrayOf(0xFFFF6FA8, 0xFFFF5A4E)

/** A plump, glossy rubber cushion with a floppy neck and a rolled lip on the left. */
internal fun DrawScope.thWhoopee(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val rubber = Color(WHOOPEE[v.mod(WHOOPEE.size)])
    val neck = thSketch(w, h) {
        m(-0.18f, -0.72f); q(-0.3f, -0.68f, -0.39f, -0.62f); l(-0.45f, -0.74f); q(-0.52f, -0.5f, -0.45f, -0.24f)
        l(-0.38f, -0.36f); q(-0.3f, -0.32f, -0.18f, -0.32f); z()
    }
    inked(neck, rubber.darken(0.06f), pen)
    for (k in 0 until 2) {
        val x = -0.27f - k * 0.06f
        drawLine(rubber.darken(0.35f), o(x, -0.64f + k * 0.02f), o(x, -0.37f - k * 0.01f), pen.lw * 0.5f, StrokeCap.Round)
    }
    val lip = rect(-0.455f * w, -0.49f * h, 0.075f * w, 0.52f * h)
    inkedOval(lip, rubber.lighten(0.12f), pen, shade = false)
    drawOval(rubber.darken(0.5f), Offset(lip.left + lip.width * 0.32f, lip.top + lip.height * 0.26f), Size(lip.width * 0.36f, lip.height * 0.48f))
    val body = thSketch(w, h) {
        m(-0.26f, -0.52f); c(-0.24f, -0.94f, 0.08f, -1f, 0.24f, -0.98f); c(0.44f, -0.96f, 0.52f, -0.72f, 0.49f, -0.5f)
        c(0.47f, -0.2f, 0.3f, -0.02f, 0.1f, -0.03f); c(-0.1f, -0.04f, -0.26f, -0.18f, -0.26f, -0.52f); z()
    }
    inked(body, rubber, pen, outline = false)
    clipPath(body) {
        thSheen(o(0.06f, -0.62f), w * 0.42f, 0.45f)
        // The rim where the two rubber sheets are glued, bulging round the middle.
        val seam = thSketch(w, h) { m(-0.3f, -0.42f); c(-0.12f, -0.18f, 0.3f, -0.18f, 0.54f, -0.44f) }
        drawPath(seam, rubber.darken(0.32f), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
        translate(0f, -h * 0.08f) { drawPath(seam, rubber.lighten(0.4f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round)) }
    }
    drawPath(body, Ink.line, style = pen.stroke)
    // Squishy wrinkles where the neck joins.
    val wrinkle = rubber.darken(0.2f)
    drawPath(thSketch(w, h) { m(-0.2f, -0.78f); q(-0.15f, -0.7f, -0.19f, -0.62f) }, wrinkle, style = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round))
    drawPath(thSketch(w, h) { m(-0.2f, -0.28f); q(-0.14f, -0.36f, -0.18f, -0.44f) }, wrinkle, style = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round))
    // Rubbery gloss: a long highlight and a bright dot.
    drawPath(thSketch(w, h) { m(-0.06f, -0.82f); q(0.12f, -0.94f, 0.32f, -0.88f) }, Color.White.copy(alpha = 0.85f), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = 0.9f), pen.lw * 0.7f, o(0.4f, -0.8f))
    drawPath(thSketch(w, h) { m(0.3f, -0.18f); q(0.4f, -0.24f, 0.44f, -0.36f) }, Color.White.copy(alpha = 0.35f), style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
}

/** The classic slippy peel: a stump with two fat flaps flopped to the floor and one folded down its front. */
internal fun DrawScope.thBananaPeel(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val yellow = Color(0xFFFFD84A)
    val inside = Color(0xFFFFF4C8)
    val brown = Color(0xFF6B4A2B)
    // The peel is very flat, so its rim is a touch finer than usual to leave room for the colour.
    val rim = Stroke(pen.lw * 0.8f, join = StrokeJoin.Round)
    for (s in SIDES) {
        val flap = thSketch(w, h) {
            m(s * 0.06f, -0.97f); q(s * 0.36f, -1.02f, s * 0.49f, -0.14f); q(s * 0.5f, -0.02f, s * 0.41f, -0.04f)
            q(s * 0.3f, -0.46f, s * 0.12f, -0.4f); z()
        }
        drawPath(flap, yellow)
        clipPath(flap) {
            drawPath(thSketch(w, h) { m(s * 0.3f, -0.1f); q(s * 0.36f, -0.5f, s * 0.14f, -0.46f); l(s * 0.6f, -0.3f); l(s * 0.6f, 0f); z() }, yellow.darken(0.12f))
            // A thin pale edge where the inside shows.
            drawPath(thSketch(w, h) { m(s * 0.06f, -0.95f); q(s * 0.32f, -0.98f, s * 0.42f, -0.46f); q(s * 0.28f, -0.8f, s * 0.08f, -0.8f); z() }, inside)
        }
        drawPath(flap, Ink.line, style = rim)
        drawCircle(brown, w * 0.026f, o(s * 0.455f, -0.08f))
    }
    val stump = Rect(-0.15f * w, -0.9f * h, 0.15f * w, -0.03f * h)
    drawRoundRect(yellow, stump.topLeft, stump.size, androidx.compose.ui.geometry.CornerRadius(w * 0.07f))
    drawRect(yellow.darken(0.12f), Offset(0.06f * w, stump.top), Size(0.09f * w, stump.height))
    drawRoundRect(Ink.line, stump.topLeft, stump.size, androidx.compose.ui.geometry.CornerRadius(w * 0.07f), style = rim)
    val top = rect(0f, -0.9f * h, 0.26f * w, 0.22f * h)
    drawOval(inside, top.topLeft, top.size)
    drawOval(Ink.line, top.topLeft, top.size, style = rim)
    val front = thSketch(w, h) { m(-0.07f, -0.86f); q(-0.09f, -0.3f, -0.12f, -0.05f); q(0f, 0.01f, 0.12f, -0.05f); q(0.09f, -0.3f, 0.07f, -0.86f); z() }
    drawPath(front, inside)
    drawPath(front, Ink.line, style = rim)
    drawCircle(brown, w * 0.022f, o(0f, -0.07f))
    drawCircle(brown.copy(alpha = 0.6f), w * 0.009f, o(-0.3f, -0.62f))
    drawCircle(brown.copy(alpha = 0.6f), w * 0.008f, o(0.34f, -0.5f))
    shine(o(-0.26f, -0.8f), w * 0.07f, h * 0.1f, 0.7f)
}

/** A glass pepper shaker, full of black pepper, with a silver cap; a pinch puffs out now and then. */
internal fun DrawScope.thPepper(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val body = thSketch(w, h) {
        m(-0.3f, -0.68f); l(0.3f, -0.68f); q(0.46f, -0.62f, 0.46f, -0.42f); l(0.46f, -0.12f); q(0.46f, -0.02f, 0.34f, -0.02f)
        l(-0.34f, -0.02f); q(-0.46f, -0.02f, -0.46f, -0.12f); l(-0.46f, -0.42f); q(-0.46f, -0.62f, -0.3f, -0.68f); z()
    }
    drawPath(body, ThingInk.glass)
    val pepper = Color(0xFF3A3340)
    clipPath(body) {
        drawRect(pepper, o(-0.5f, -0.46f), Size(w, 0.46f * h))
        drawOval(pepper.lighten(0.2f), o(-0.46f, -0.5f), Size(0.92f * w, 0.08f * h))
        for (k in 0 until 9) drawCircle(Color(0xFF8E93A6), w * 0.03f, o(PEPPER_GRAINS[k * 2], PEPPER_GRAINS[k * 2 + 1]))
        drawRect(Color.White.copy(alpha = 0.3f), o(-0.5f, -0.1f), Size(w, 0.1f * h))
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawLine(Color.White.copy(alpha = 0.7f), o(-0.3f, -0.56f), o(-0.3f, -0.16f), w * 0.08f, StrokeCap.Round)
    val cap = thSketch(w, h) { m(-0.34f, -0.7f); q(-0.36f, -0.97f, 0f, -0.97f); q(0.36f, -0.97f, 0.34f, -0.7f); z() }
    inked(cap, ThingInk.silver, pen)
    inkedRound(Rect(-0.38f * w, -0.74f * h, 0.38f * w, -0.66f * h), w * 0.05f, ThingInk.steel, pen, shade = false)
    for (k in -1..1) drawCircle(Ink.line, w * 0.04f, o(k * 0.14f, -0.86f + if (k == 0) -0.03f else 0f))
    shine(o(-0.12f, -0.9f), w * 0.1f, h * 0.03f)
    // A little pinch of pepper puffs out of the holes.
    val ph = (pen.t * 0.45f) % 1f
    if (ph < 0.5f) {
        val a = 1f - ph * 2f
        for (k in 0 until 3) drawCircle(pepper.copy(alpha = 0.8f * a), w * 0.035f, o(-0.12f + k * 0.12f, -0.96f - ph * 0.12f - (k % 2) * 0.03f))
    }
}

private val PEPPER_GRAINS = floatArrayOf(-0.3f, -0.38f, -0.1f, -0.3f, 0.14f, -0.4f, 0.32f, -0.3f, -0.22f, -0.2f, 0.04f, -0.16f, 0.26f, -0.12f, -0.34f, -0.08f, 0.1f, -0.06f)
