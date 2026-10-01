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
import kotlin.math.sin

// Things for Heileberget: field binoculars worn at the eyes, and a steel flask of cocoa.

/** Field binoculars seen from the front, like glasses: two big lenses, a hinge and a red neck strap. */
internal fun DrawScope.thBinoculars(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val rubber = Color(0xFF3B4357)
    val strap = Color(0xFFE8573F)
    val r = h * 0.45f
    val cy = -0.5f * h
    val cx = 0.26f * w
    // The strap runs back from each outer lug, like the arms of glasses.
    for (s in SIDES) {
        capsule(o(s * 0.46f, -0.68f), o(s * 0.56f, -0.8f), h * 0.11f, strap, pen)
        drawLine(Color.White.copy(alpha = 0.55f), o(s * 0.49f, -0.71f), o(s * 0.53f, -0.76f), pen.lw * 0.6f, StrokeCap.Round)
    }
    // The hinge between the barrels, and the focus wheel on top of it.
    inkedRound(rect(0f, cy, 0.22f * w, h * 0.3f), h * 0.07f, Color(0xFF59627A), pen)
    val wheel = rect(0f, cy - h * 0.2f, 0.1f * w, h * 0.26f)
    inkedRound(wheel, wheel.width * 0.25f, Color(0xFFD5DAE5), pen)
    for (k in 1..3) {
        val y = wheel.top + wheel.height * k / 4f
        drawLine(Ink.line, Offset(wheel.left + wheel.width * 0.15f, y), Offset(wheel.right - wheel.width * 0.15f, y), pen.lw * 0.5f, StrokeCap.Round)
    }
    for (s in SIDES) {
        val c = Offset(s * cx, cy)
        // The rubber armour with a ring of lighter ridges.
        inkedCircle(c, r, rubber, pen)
        drawCircle(Color.White.copy(alpha = 0.16f), r * 0.9f, c, style = Stroke(r * 0.1f))
        // The steel rim and the lens, blue and violet with the green flash of coated glass.
        inkedCircle(c, r * 0.83f, Color(0xFFC3CCDC), pen)
        val lr = r * 0.66f
        drawCircle(
            safeRadialGradient(0f to Color(0xFF8FE3F0), 0.45f to Color(0xFF2E6CC8), 1f to Color(0xFF231B63), center = Offset(c.x - lr * 0.3f, c.y - lr * 0.3f), radius = lr * 1.6f),
            lr, c,
        )
        drawArc(Color(0xB35DF0B0), 20f, 70f, false, Offset(c.x - lr * 0.78f, c.y - lr * 0.78f), Size(lr * 1.56f, lr * 1.56f), style = Stroke(lr * 0.14f, cap = StrokeCap.Round))
        shine(Offset(c.x - lr * 0.38f, c.y - lr * 0.42f), lr * 0.5f, lr * 0.3f, 0.85f)
        drawCircle(Ink.line, lr, c, style = pen.stroke)
        thGlint(Offset(c.x + lr * 0.45f, c.y - lr * 0.5f), lr * 0.55f, pen.t, 1.7f, if (s < 0f) 0f else 2.2f)
    }
}

private fun sipsLeft(used: Int): Float = 1f - used.coerceIn(0, 3) / 3f

/** A steel flask of hot cocoa with a red cup for a lid, a level window and a wisp of steam; three sips. */
internal fun DrawScope.thThermos(used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val steel = Color(0xFFC8D1DE)
    val red = Color(0xFFD2443A)
    val lv = sipsLeft(used)
    val body = thSketch(w, h) {
        m(-0.5f, -0.7f); q(-0.5f, -0.76f, -0.44f, -0.76f); l(0.44f, -0.76f); q(0.5f, -0.76f, 0.5f, -0.7f)
        l(0.5f, -0.12f); q(0.5f, -0.02f, 0.4f, -0.02f); l(-0.4f, -0.02f); q(-0.5f, -0.02f, -0.5f, -0.12f); z()
    }
    inked(body, steel, pen, outline = false)
    clipPath(body) {
        // A red band, a dark foot and a bright strip of light down the left, a darker one on the right.
        drawRect(red, o(-0.55f, -0.36f), Size(1.1f * w, 0.14f * h))
        drawRect(red.shadow(), o(0.3f, -0.36f), Size(0.3f * w, 0.14f * h))
        drawRect(Color(0xFF3A3340), o(-0.55f, -0.1f), Size(1.1f * w, 0.1f * h))
        // A tiny white mountain on the band.
        drawPath(thSketch(w, h) { m(-0.24f, -0.23f); l(-0.1f, -0.33f); l(-0.02f, -0.27f); l(0.08f, -0.35f); l(0.24f, -0.23f); z() }, Color.White.copy(alpha = 0.92f))
        drawRect(Color.White.copy(alpha = 0.55f), o(-0.36f, -0.76f), Size(0.1f * w, 0.74f * h))
        drawRect(Ink.line.copy(alpha = 0.16f), o(0.3f, -0.76f), Size(0.25f * w, 0.74f * h))
    }
    drawPath(body, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    // The level window: how much cocoa is left shows as a brown column.
    val win = Rect(-0.12f * w, -0.7f * h, 0.12f * w, -0.42f * h)
    inkedRound(win, w * 0.06f, Color(0xFFDDEBF5), pen, shade = false)
    if (lv > 0f) {
        val top = win.bottom - win.height * lv
        clipPath(roundPath(win, w * 0.06f)) {
            drawRect(Color(0xFF7A4A2C), Offset(win.left, top), Size(win.width, win.bottom - top))
            drawRect(Color(0xFFA8704A), Offset(win.left, top), Size(win.width, pen.lw * 1.2f))
        }
    }
    drawRoundRect(Ink.line, win.topLeft, win.size, androidx.compose.ui.geometry.CornerRadius(w * 0.06f), style = pen.thin)
    // The cup that screws on top, with a lip.
    val cup = Rect(-0.42f * w, -0.98f * h, 0.42f * w, -0.72f * h)
    inkedRound(cup, w * 0.1f, red, pen)
    drawLine(Color.White.copy(alpha = 0.5f), Offset(cup.left + w * 0.1f, cup.top + cup.height * 0.28f), Offset(cup.left + w * 0.1f, cup.bottom - cup.height * 0.2f), pen.lw * 0.9f, StrokeCap.Round)
    drawLine(Ink.line, Offset(cup.left, cup.bottom - cup.height * 0.28f), Offset(cup.right, cup.bottom - cup.height * 0.28f), pen.lw * 0.7f, StrokeCap.Round)
    // Hot cocoa still steams from the vent while there is some left.
    if (lv > 0f) {
        for (k in 0 until 2) {
            val ph = (pen.t * 0.45f + k * 0.5f) % 1f
            val x = 0.2f + k * 0.12f + sin(pen.t * 2f + k * 2f) * 0.04f
            val y0 = -1.0f - ph * 0.08f
            val wisp = thSketch(w, h) { m(x, y0); q(x + 0.1f, y0 - 0.05f, x, y0 - 0.1f); q(x - 0.1f, y0 - 0.15f, x, y0 - 0.2f) }
            drawPath(wisp, Color.White.copy(alpha = 0.8f * (1f - ph)), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        }
    }
}
