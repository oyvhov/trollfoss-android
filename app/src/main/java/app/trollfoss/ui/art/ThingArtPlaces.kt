package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin

// Things from the tivoli, the shop, the doctor, the stage and the sea floor.

private val FLOSS = longArrayOf(0xFFFFA8D2, 0xFF9AD7FF)

// Puffs of the candy-floss cloud: x, y (fractions of the box) and radius (fraction of the width).
private val FLOSS_PUFFS = floatArrayOf(
    -0.28f, -0.6f, 0.2f, 0f, -0.57f, 0.22f, 0.28f, -0.6f, 0.2f, -0.3f, -0.76f, 0.2f, 0.3f, -0.76f, 0.2f,
    -0.14f, -0.87f, 0.22f, 0.14f, -0.87f, 0.22f, 0f, -0.74f, 0.3f,
)

/** A fluffy cloud of candy floss on a paper stick. */
internal fun DrawScope.thCandyFloss(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val c = Color(FLOSS[v.mod(FLOSS.size)])
    val stick = thSketch(w, h) { m(-0.05f, -0.02f); l(0.05f, -0.02f); l(0.1f, -0.52f); l(-0.1f, -0.52f); z() }
    inked(stick, Color(0xFFFFF7EA), pen, shade = false)
    clipPath(stick) {
        for (k in 0 until 4) drawLine(c.darken(0.15f), o(-0.2f, -0.12f - k * 0.12f), o(0.2f, -0.2f - k * 0.12f), w * 0.04f)
    }
    val cloud = Path()
    for (k in 0 until FLOSS_PUFFS.size / 3) {
        val p = o(FLOSS_PUFFS[k * 3], FLOSS_PUFFS[k * 3 + 1])
        val r = FLOSS_PUFFS[k * 3 + 2] * w
        cloud.addOval(Rect(p.x - r, p.y - r, p.x + r, p.y + r))
    }
    thUnion(cloud, c, pen)
    clipPath(cloud) {
        thSheen(o(-0.05f, -0.76f), w * 0.5f, 0.4f)
        val wisp = c.lighten(0.5f)
        val deep = c.darken(0.14f)
        for (k in 0 until 4) {
            val cx = FLOSS_WISPS[k * 2]
            val cy = FLOSS_WISPS[k * 2 + 1]
            drawArc(wisp, 200f + k * 30f, 150f, false, o(cx - 0.14f, cy - 0.06f), Size(0.28f * w, 0.12f * h), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
            drawArc(deep, 20f + k * 30f, 120f, false, o(cx - 0.1f, cy + 0.02f), Size(0.2f * w, 0.08f * h), style = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round))
        }
    }
    thGlint(o(0.26f, -0.82f), w * 0.1f, pen.t, 2.2f, v.toFloat())
    thGlint(o(-0.2f, -0.64f), w * 0.08f, pen.t, 2.2f, 2.4f + v)
}

private val FLOSS_WISPS = floatArrayOf(-0.2f, -0.82f, 0.14f, -0.9f, 0.18f, -0.66f, -0.18f, -0.62f)

// Popcorn heaped in the box: x, y (fractions of the box) and size (fraction of the width), back row first.
private val KERNELS = floatArrayOf(
    -0.06f, -0.88f, 0.26f, 0.18f, -0.86f, 0.24f,
    -0.26f, -0.76f, 0.26f, 0.02f, -0.74f, 0.28f, 0.28f, -0.74f, 0.26f,
    -0.36f, -0.64f, 0.24f, -0.12f, -0.62f, 0.26f, 0.14f, -0.62f, 0.26f, 0.37f, -0.63f, 0.24f,
)

/** A striped box of popcorn, heaped over the top. */
internal fun DrawScope.thPopcorn(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val puff = Color(0xFFFFF6DE)
    val hull = Color(0xFFE8B04A)
    for (k in 0 until KERNELS.size / 3) {
        val c = o(KERNELS[k * 3], KERNELS[k * 3 + 1])
        val s = KERNELS[k * 3 + 2] * w
        val kernel = Path().apply {
            addOval(rect(c.x - s * 0.22f, c.y + s * 0.02f, s * 0.56f, s * 0.5f))
            addOval(rect(c.x + s * 0.2f, c.y - s * 0.04f, s * 0.54f, s * 0.5f))
            addOval(rect(c.x, c.y - s * 0.24f, s * 0.56f, s * 0.5f))
            addOval(rect(c.x + s * 0.02f, c.y + s * 0.2f, s * 0.46f, s * 0.36f))
        }
        thUnion(kernel, puff, pen)
        drawCircle(hull, s * 0.1f, Offset(c.x + s * 0.06f, c.y + s * 0.26f))
        drawCircle(Color.White, s * 0.08f, Offset(c.x - s * 0.1f, c.y - s * 0.22f))
    }
    val box = thSketch(w, h) { m(-0.42f, -0.62f); l(0.42f, -0.62f); l(0.32f, -0.04f); q(0.31f, -0.02f, 0.28f, -0.02f); l(-0.28f, -0.02f); q(-0.31f, -0.02f, -0.32f, -0.04f); z() }
    inked(box, Color.White, pen, outline = false)
    val red = Color(0xFFE8304A)
    clipPath(box) {
        for (k in -2..2) {
            val top = k * 0.18f
            val stripe = thSketch(w, h) { m(top - 0.05f, -0.62f); l(top + 0.05f, -0.62f); l(top * 0.76f + 0.04f, 0f); l(top * 0.76f - 0.04f, 0f); z() }
            drawPath(stripe, red)
        }
        drawRect(Ink.line.copy(alpha = 0.12f), o(0.2f, -0.62f), Size(0.3f * w, 0.62f * h))
    }
    drawPath(box, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    inkedRound(Rect(-0.45f * w, -0.68f * h, 0.45f * w, -0.58f * h), h * 0.03f, Color.White, pen, shade = false)
    thDot(o(0f, -0.34f), w * 0.13f, ThingInk.sun, pen)
    drawPath(starPath(o(0f, -0.34f), w * 0.09f, w * 0.04f), red)
}

private val SODA = longArrayOf(0xFFD8323F, 0xFFFF8A2A, 0xFFF3D23A)
private val COLA_FIZZ = floatArrayOf(-0.12f, 0.02f, 0.1f, 0.12f, -0.06f, 0.07f, 0.06f, 0.08f, 0.05f, -0.2f, -0.12f, 0.04f)

/** A can of fizzy pop: 0 cola, 1 orange, 2 lemon. Opened after the first sip, dented by the last. */
internal fun DrawScope.thSoda(v: Int, used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val i = v.mod(SODA.size)
    val c = Color(SODA[i])
    val silver = ThingInk.silver
    val body = thSketch(w, h) {
        m(-0.36f, -0.9f); l(0.36f, -0.9f); q(0.44f, -0.86f, 0.44f, -0.78f); l(0.44f, -0.12f); q(0.44f, -0.04f, 0.36f, -0.02f)
        l(-0.36f, -0.02f); q(-0.44f, -0.04f, -0.44f, -0.12f); l(-0.44f, -0.78f); q(-0.44f, -0.86f, -0.36f, -0.9f); z()
    }
    inked(body, c, pen, outline = false)
    clipPath(body) {
        drawRect(silver, o(-0.5f, -0.95f), Size(w, 0.13f * h))
        drawRect(silver, o(-0.5f, -0.1f), Size(w, 0.1f * h))
        drawLine(silver.darken(0.25f), o(-0.5f, -0.82f), o(0.5f, -0.82f), pen.lw * 0.45f)
        val band = thSketch(w, h) {
            m(-0.5f, -0.52f); q(0f, -0.68f, 0.5f, -0.5f); l(0.5f, -0.4f); q(0f, -0.56f, -0.5f, -0.42f); z()
        }
        drawPath(band, Color.White.copy(alpha = 0.9f))
        val icon = o(0f, -0.3f)
        when (i) {
            0 -> for (k in 0 until 4) {
                val b = Offset(icon.x + COLA_FIZZ[k * 3] * w, icon.y + COLA_FIZZ[k * 3 + 1] * h)
                val r = COLA_FIZZ[k * 3 + 2] * w
                drawCircle(Color.White.copy(alpha = 0.3f), r, b)
                drawCircle(Color.White, r, b, style = Stroke(pen.lw * 0.5f))
                drawCircle(Color.White, r * 0.3f, Offset(b.x - r * 0.35f, b.y - r * 0.35f))
            }
            1 -> {
                thDot(icon, w * 0.16f, Color(0xFFFFB44A), pen)
                drawCircle(Color(0xFFFFE0A0), w * 0.11f, icon)
                for (k in 0 until 6) {
                    val a = Math.toRadians(k * 60.0)
                    drawLine(Color(0xFFFFB44A), icon, Offset(icon.x + w * 0.11f * kotlin.math.cos(a).toFloat(), icon.y + w * 0.11f * kotlin.math.sin(a).toFloat()), pen.lw * 0.4f)
                }
            }
            else -> {
                thFill(thLens(Offset(icon.x - w * 0.18f, icon.y + h * 0.02f), Offset(icon.x + w * 0.18f, icon.y - h * 0.02f), w * 0.1f), Color(0xFFFFF27A), pen)
                drawPath(thLens(Offset(icon.x + w * 0.1f, icon.y - h * 0.05f), Offset(icon.x + w * 0.26f, icon.y - h * 0.12f), w * 0.04f), Color(0xFF3FAE49))
            }
        }
        // Cold drops on the can.
        drawCircle(Color.White.copy(alpha = 0.7f), w * 0.03f, o(0.28f, -0.66f))
        drawCircle(Color.White.copy(alpha = 0.6f), w * 0.022f, o(0.3f, -0.2f))
        drawCircle(Color.White.copy(alpha = 0.6f), w * 0.02f, o(-0.2f, -0.72f))
        drawLine(Color.White.copy(alpha = 0.6f), o(-0.28f, -0.74f), o(-0.28f, -0.16f), w * 0.08f, StrokeCap.Round)
        if (used >= 2) {
            val dent = c.darken(0.35f)
            drawPath(thSketch(w, h) { m(-0.44f, -0.46f); l(-0.2f, -0.54f); l(0.02f, -0.44f); l(0.2f, -0.52f) }, dent, style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(thSketch(w, h) { m(-0.44f, -0.46f); l(-0.2f, -0.5f) }, Color.White.copy(alpha = 0.5f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
        }
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    val lid = Rect(-0.38f * w, -0.99f * h, 0.38f * w, -0.85f * h)
    inkedOval(lid, silver, pen, shade = false)
    drawOval(silver.darken(0.15f), Offset(lid.left + w * 0.05f, lid.top + h * 0.02f), Size(lid.width - w * 0.1f, lid.height - h * 0.04f), style = Stroke(pen.lw * 0.5f))
    if (used == 0) {
        drawRoundRect(silver.darken(0.08f), o(-0.1f, -0.955f), Size(0.24f * w, 0.05f * h), CornerRadius(w * 0.04f))
        drawRoundRect(Ink.line, o(-0.1f, -0.955f), Size(0.24f * w, 0.05f * h), CornerRadius(w * 0.04f), style = pen.thin)
    } else {
        drawOval(Ink.line, o(-0.14f, -0.95f), Size(0.18f * w, 0.05f * h))
        drawRoundRect(silver.darken(0.08f), o(0.06f, -0.975f), Size(0.2f * w, 0.05f * h), CornerRadius(w * 0.04f))
        drawRoundRect(Ink.line, o(0.06f, -0.975f), Size(0.2f * w, 0.05f * h), CornerRadius(w * 0.04f), style = pen.thin)
        // Fizz pops out of the opening.
        for (k in 0 until 3) {
            val ph = (pen.t * 1.3f + k * 0.33f) % 1f
            drawCircle(Color.White.copy(alpha = 0.9f * (1f - ph)), w * (0.03f + 0.015f * k), o(-0.05f + sin(k * 2.3f + pen.t * 4f) * 0.1f, -0.95f - ph * 0.14f), style = Stroke(pen.lw * 0.45f))
        }
    }
}

/** A bottle of medicine: amber glass, a label with a green pharmacy plus, a dosing cup on the cap. */
internal fun DrawScope.thSyrup(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val amber = Color(0xFFC0692E)
    val bottle = thSketch(w, h) {
        m(-0.2f, -0.7f); l(0.2f, -0.7f); q(0.44f, -0.66f, 0.44f, -0.46f); l(0.44f, -0.1f); q(0.44f, -0.02f, 0.34f, -0.02f)
        l(-0.34f, -0.02f); q(-0.44f, -0.02f, -0.44f, -0.1f); l(-0.44f, -0.46f); q(-0.44f, -0.66f, -0.2f, -0.7f); z()
    }
    inked(bottle, amber, pen, outline = false)
    clipPath(bottle) {
        drawRect(Color(0xFF8A2E1E), o(-0.5f, -0.56f), Size(w, 0.56f * h))
        drawRect(Color(0xFFB0432A), o(-0.5f, -0.56f), Size(w, 0.03f * h))
        drawLine(Color.White.copy(alpha = 0.5f), o(-0.3f, -0.58f), o(-0.3f, -0.12f), w * 0.07f, StrokeCap.Round)
    }
    drawPath(bottle, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    inkedRound(Rect(-0.18f * w, -0.8f * h, 0.18f * w, -0.68f * h), w * 0.04f, amber, pen, shade = false)
    val label = Rect(-0.36f * w, -0.46f * h, 0.36f * w, -0.14f * h)
    drawRoundRect(ThingInk.cream, label.topLeft, label.size, CornerRadius(w * 0.05f))
    drawRoundRect(Ink.line, label.topLeft, label.size, CornerRadius(w * 0.05f), style = pen.thin)
    val cx = label.center.x
    val cy = label.center.y
    val a = w * 0.09f
    val cross = Path().apply {
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(cx - a / 2, cy - a * 1.4f, cx + a / 2, cy + a * 1.4f), CornerRadius(a * 0.2f)))
        addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(cx - a * 1.4f, cy - a / 2, cx + a * 1.4f, cy + a / 2), CornerRadius(a * 0.2f)))
    }
    drawPath(cross, Color(0xFF2FB36B))
    // A see-through dosing cup sits upside down on the cap.
    val cup = thSketch(w, h) { m(-0.3f, -0.78f); l(0.3f, -0.78f); l(0.24f, -0.985f); l(-0.24f, -0.985f); z() }
    drawPath(cup, Color(0x88FFFFFF))
    for (k in 1..2) drawLine(Ink.line.copy(alpha = 0.4f), o(-0.12f, -0.78f - k * 0.07f), o(0.02f, -0.78f - k * 0.07f), pen.lw * 0.4f)
    drawPath(cup, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    shine(o(-0.14f, -0.92f), w * 0.07f, h * 0.05f, 0.8f)
}

/** A children's plaster with little hearts, stuck on at a jaunty angle; centred in its box. */
internal fun DrawScope.thBandage(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    rotate(-10f, pivot = o(0f, -0.5f)) {
        val strip = Rect(-0.45f * w, -0.8f * h, 0.45f * w, -0.2f * h)
        inkedRound(strip, h * 0.3f, Color(0xFFF2C9A0), pen)
        val pad = Rect(-0.15f * w, -0.74f * h, 0.15f * w, -0.26f * h)
        drawRoundRect(Color(0xFFFFF4EA), pad.topLeft, pad.size, CornerRadius(h * 0.08f))
        drawRoundRect(Color(0xFFE0B08A), pad.topLeft, pad.size, CornerRadius(h * 0.08f), style = pen.thin)
        for (s in SIDES) {
            drawPath(thHeart(s * 0.3f * w, -0.52f * h, h * 0.2f), Color(0xFFFF6FA8))
            for (k in 0 until 2) drawCircle(Color(0xFFC89A72), pen.lw * 0.35f, o(s * (0.22f + k * 0.16f), -0.3f))
            for (k in 0 until 2) drawCircle(Color(0xFFC89A72), pen.lw * 0.35f, o(s * (0.22f + k * 0.16f), -0.72f))
        }
        drawLine(Color.White.copy(alpha = 0.6f), o(-0.32f, -0.7f), o(-0.2f, -0.7f), pen.lw * 0.7f, StrokeCap.Round)
    }
}

/** A glass thermometer; the red line creeps up and down a little. */
internal fun DrawScope.thThermometer(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val red = Color(0xFFE8304A)
    val tube = Rect(-0.32f * w, -0.99f * h, 0.32f * w, -0.16f * h)
    drawRoundRect(Color(0xFFEAF6FF), tube.topLeft, tube.size, CornerRadius(w * 0.32f))
    val level = -0.6f + 0.05f * sin(pen.t * 0.9f)
    drawLine(red, o(0f, -0.2f), o(0f, level), w * 0.24f, StrokeCap.Round)
    for (k in 0 until 7) {
        val y = -0.3f - k * 0.09f
        drawLine(Ink.line, o(0.1f, y), o(if (k % 2 == 0) 0.3f else 0.22f, y), pen.lw * 0.4f)
    }
    drawRoundRect(Ink.line, tube.topLeft, tube.size, CornerRadius(w * 0.32f), style = pen.stroke)
    drawLine(Color.White.copy(alpha = 0.85f), o(-0.16f, -0.92f), o(-0.16f, -0.3f), w * 0.1f, StrokeCap.Round)
    val bulb = o(0f, -0.13f)
    inkedCircle(bulb, w * 0.48f, red, pen)
    thSheen(bulb, w * 0.48f, 0.4f)
    shine(Offset(bulb.x - w * 0.16f, bulb.y - w * 0.16f), w * 0.2f, w * 0.14f)
}

/** A doctor's stethoscope: silver ear tubes, a blue rubber tube and a chest piece with a heart. */
internal fun DrawScope.thStethoscope(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val rubber = Color(0xFF4A8BFF)
    val metal = ThingInk.silver
    val tube = thSketch(w, h) {
        m(-0.22f, -0.62f); q(-0.1f, -0.5f, 0f, -0.46f); q(0.1f, -0.5f, 0.22f, -0.62f)
        m(0f, -0.46f); c(0f, -0.2f, -0.24f, -0.02f, 0.02f, -0.05f); c(0.12f, -0.06f, 0.14f, -0.14f, 0.16f, -0.2f)
    }
    val tw = w * 0.05f
    drawPath(tube, Ink.line, style = Stroke(tw + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(tube, rubber, style = Stroke(tw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(tube, rubber.lighten(0.4f), style = Stroke(tw * 0.25f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    for (s in SIDES) {
        val ear = thSketch(w, h) { m(s * 0.3f, -0.93f); q(s * 0.38f, -0.74f, s * 0.22f, -0.62f) }
        drawPath(ear, Ink.line, style = Stroke(w * 0.035f + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(ear, metal, style = Stroke(w * 0.035f, cap = StrokeCap.Round))
        inkedOval(rect(s * 0.3f * w, -0.95f * h, w * 0.1f, h * 0.08f), Color(0xFF3A3340), pen, shade = false)
    }
    val chest = o(0.28f, -0.25f)
    val r = w * 0.17f
    capsule(o(0.16f, -0.2f), Offset(chest.x - r * 0.8f, chest.y + r * 0.2f), w * 0.04f, metal, pen)
    inkedCircle(chest, r, metal, pen)
    drawCircle(Color(0xFFEFF2F8), r * 0.7f, chest)
    drawCircle(Ink.line, r * 0.7f, chest, style = pen.thin)
    drawPath(thHeart(chest.x, chest.y, r * 0.7f), Color(0xFFFF4D6D))
    shine(Offset(chest.x - r * 0.4f, chest.y - r * 0.45f), r * 0.4f, r * 0.24f)
}

/** A stage microphone: a meshed silver head, a gold ring and a black handle. */
internal fun DrawScope.thMicrophone(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val handle = thSketch(w, h) { m(-0.26f, -0.6f); l(0.26f, -0.6f); l(0.14f, -0.06f); q(0.12f, -0.02f, 0.06f, -0.02f); l(-0.06f, -0.02f); q(-0.12f, -0.02f, -0.14f, -0.06f); z() }
    inked(handle, Color(0xFF2E2A3A), pen)
    drawLine(Color.White.copy(alpha = 0.35f), o(-0.12f, -0.54f), o(-0.06f, -0.1f), w * 0.07f, StrokeCap.Round)
    inkedRound(Rect(-0.08f * w, -0.4f * h, 0.08f * w, -0.3f * h), w * 0.04f, Color(0xFFFF4D6D), pen, shade = false)
    val head = o(0f, -0.78f)
    val r = w * 0.48f
    inkedCircle(head, r, Color(0xFFC9D0DC), pen)
    clipPath(Path().apply { addOval(Rect(head.x - r, head.y - r, head.x + r, head.y + r)) }) {
        val mesh = Ink.line.copy(alpha = 0.35f)
        for (k in -3..3) {
            drawLine(mesh, Offset(head.x + k * r * 0.34f - r, head.y - r), Offset(head.x + k * r * 0.34f + r, head.y + r), pen.lw * 0.4f)
            drawLine(mesh, Offset(head.x + k * r * 0.34f + r, head.y - r), Offset(head.x + k * r * 0.34f - r, head.y + r), pen.lw * 0.4f)
        }
        thSheen(head, r, 0.5f)
    }
    drawCircle(Ink.line, r, head, style = pen.stroke)
    inkedRound(Rect(-0.3f * w, -0.66f * h, 0.3f * w, -0.58f * h), w * 0.06f, ThingInk.gold, pen)
    shine(Offset(head.x - r * 0.4f, head.y - r * 0.42f), r * 0.4f, r * 0.26f)
    thGlint(Offset(head.x + r * 0.7f, head.y - r * 0.7f), r * 0.6f, pen.t, 2f, 0f)
}

/** A shimmering pearl with a soft glow. */
internal fun DrawScope.thPearl(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h / 2)
    val r = w * 0.47f
    thGlow(c, w * 0.62f, Color(0xFFFFF0FA), 0.35f + 0.12f * sin(pen.t * 1.8f))
    inkedCircle(c, r, Color(0xFFF6F0FA), pen)
    clipPath(Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }) {
        drawCircle(
            safeRadialGradient(listOf(Color(0x99FFC8E6), Color(0x00FFC8E6)), Offset(c.x + r * 0.35f, c.y + r * 0.35f), r * 0.9f),
            r, c,
        )
        drawCircle(
            safeRadialGradient(listOf(Color(0x66B8E8FF), Color(0x00B8E8FF)), Offset(c.x - r * 0.45f, c.y + r * 0.2f), r * 0.7f),
            r, c,
        )
        thSheen(c, r, 0.7f)
    }
    drawCircle(Ink.line, r, c, style = pen.stroke)
    shine(Offset(c.x - r * 0.35f, c.y - r * 0.38f), r * 0.5f, r * 0.34f, 0.95f)
    thGlint(Offset(c.x + r * 0.55f, c.y - r * 0.6f), r * 0.7f, pen.t, 1.8f, 0f)
}

/** A diving mask with a nose pocket and a snorkel, centred in its box like glasses. */
internal fun DrawScope.thDivingMask(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val strap = Color(0xFF2E2A3A)
    for (s in SIDES) {
        drawLine(Ink.line, o(s * 0.4f, -0.6f), o(s * 0.5f, -0.62f), h * 0.12f + pen.lw * 2f, StrokeCap.Butt)
        drawLine(strap, o(s * 0.4f, -0.6f), o(s * 0.5f, -0.62f), h * 0.12f, StrokeCap.Butt)
    }
    // The snorkel runs up beside the head, with its bright top above it.
    val snorkel = thSketch(w, h) { m(0.16f, 0.02f); q(0.46f, 0.08f, 0.47f, -0.3f); l(0.47f, -1.3f) }
    drawPath(snorkel, Ink.line, style = Stroke(w * 0.05f + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(snorkel, Color(0xFF4AB3FF), style = Stroke(w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    val top = thSketch(w, h) { m(0.47f, -1.14f); l(0.47f, -1.34f) }
    drawPath(top, Ink.line, style = Stroke(w * 0.07f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(top, Color(0xFFFF8A2A), style = Stroke(w * 0.07f, cap = StrokeCap.Round))
    inkedRound(rect(0.13f * w, 0f, 0.07f * w, 0.16f * h), w * 0.02f, Color(0xFF3A3340), pen, shade = false)
    inkedRound(rect(0.47f * w, -0.56f * h, 0.07f * w, 0.18f * h), w * 0.015f, Color(0xFFFF8A2A), pen, shade = false)
    val frame = thSketch(w, h) {
        m(-0.38f, -0.9f); q(0f, -0.98f, 0.38f, -0.9f); q(0.46f, -0.86f, 0.45f, -0.6f); q(0.44f, -0.3f, 0.3f, -0.22f)
        q(0.16f, -0.18f, 0.1f, -0.28f); l(0.07f, -0.12f); q(0f, -0.04f, -0.07f, -0.12f); l(-0.1f, -0.28f)
        q(-0.16f, -0.18f, -0.3f, -0.22f); q(-0.44f, -0.3f, -0.45f, -0.6f); q(-0.46f, -0.86f, -0.38f, -0.9f); z()
    }
    inked(frame, Color(0xFF2F9BFF), pen)
    val glass = thSketch(w, h) {
        m(-0.32f, -0.8f); q(0f, -0.86f, 0.32f, -0.8f); q(0.38f, -0.76f, 0.37f, -0.6f); q(0.36f, -0.38f, 0.26f, -0.34f)
        q(0.16f, -0.32f, 0.08f, -0.42f); l(0.05f, -0.28f); q(0f, -0.22f, -0.05f, -0.28f); l(-0.08f, -0.42f)
        q(-0.16f, -0.32f, -0.26f, -0.34f); q(-0.36f, -0.38f, -0.37f, -0.6f); q(-0.38f, -0.76f, -0.32f, -0.8f); z()
    }
    drawPath(glass, Color(0x66CFF0FF))
    clipPath(glass) {
        drawLine(Color.White.copy(alpha = 0.65f), o(-0.3f, -0.42f), o(-0.12f, -0.9f), w * 0.05f)
        drawLine(Color.White.copy(alpha = 0.4f), o(-0.16f, -0.42f), o(0.0f, -0.86f), w * 0.02f)
    }
    drawPath(glass, Ink.line, style = Stroke(pen.lw * 0.7f, join = StrokeJoin.Round))
}
