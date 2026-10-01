package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.cos
import kotlin.math.sin

/*
 * The kit of Storstova's art: the palette of the grand house, soft shadows, light, and a few shapes that
 * several pieces of furniture share (a candle flame, a fluted pillar, a framed picture, a brass knob).
 * Private helpers of each file start with their own prefix; everything shared by the ground floor starts with `gr`.
 */

internal object GrC {
    val walnut = Color(0xFF6E4630)
    val walnutDark = Color(0xFF4A2E22)
    val walnutLight = Color(0xFF8A5A3C)
    val mahogany = Color(0xFF8A4A33)
    val oak = Color(0xFFD9A873)
    val oakDark = Color(0xFFB9824C)
    val brass = Color(0xFFE0B04A)
    val brassDark = Color(0xFFB98A2E)
    val gold = Color(0xFFFFD66B)
    val cream = Color(0xFFF6EEDC)
    val ivory = Color(0xFFFBF6EA)
    val burgundy = Color(0xFF8E2F3E)
    val burgundyLight = Color(0xFFB0485A)
    val green = Color(0xFF2E5E4A)
    val greenLight = Color(0xFF4F8F6A)
    val teal = Color(0xFF2D6C73)
    val navy = Color(0xFF2B3F6B)
    val marble = Color(0xFFF2F0EC)
    val marbleDark = Color(0xFFA9B9B2)
    val stone = Color(0xFFB5AFA5)
    val stoneDark = Color(0xFF8E887F)
    val iron = Color(0xFF3A3844)
    val steel = Color(0xFFBAC4D4)
    val silver = Color(0xFFDDE3EC)
    val glass = Color(0xFFBFE6F0)
    val warmLight = Color(0xFFFFD98A)
    val flame1 = Color(0xFFFF5A2E)
    val flame2 = Color(0xFFFFA23A)
    val flame3 = Color(0xFFFFE680)
    val leaf = Color(0xFF4FAE5A)
    val leafDark = Color(0xFF2E7D45)
    val terracotta = Color(0xFFC8744F)
}

/** A soft shadow under a piece of furniture on the floor: [w] units wide and [d] units deep. */
internal fun DrawScope.grShadow(u: Float, w: Float, d: Float, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

/** Warm light from a lamp, a window or a fire: a soft glow that grows at night. */
internal fun DrawScope.grGlow(c: Offset, r: Float, pen: Pen, day: Float = 0.2f, color: Color = GrC.warmLight) {
    fxGlow(c, r, color, (day + 0.7f * pen.night).coerceAtMost(1f))
}

/** A candle flame at pixel [c]: [h] tall, flickering; [seed] keeps neighbours out of step. */
internal fun DrawScope.grFlame(c: Offset, h: Float, pen: Pen, seed: Float) {
    val flick = 1f + 0.12f * sin(pen.t * 9f + seed) + 0.05f * sin(pen.t * 17f + seed * 2f)
    val sway = h * 0.18f * sin(pen.t * 5f + seed)
    drawPath(fxFlamePath(c.x, c.y, h * 0.55f, h * flick, sway), GrC.flame2)
    drawPath(fxFlamePath(c.x, c.y, h * 0.3f, h * 0.62f * flick, sway * 0.7f), GrC.flame3)
}

/** A round brass knob or finial. */
internal fun DrawScope.grKnob(c: Offset, r: Float, pen: Pen) {
    inkedCircle(c, r, GrC.brass, pen, shade = false)
    shine(Offset(c.x - r * 0.3f, c.y - r * 0.3f), r * 0.5f, r * 0.35f, 0.7f)
}

/** A picture frame in pixels: gilt frame, a mat, and the picture left to the caller; returns the inner rect. */
internal fun DrawScope.grFrame(r: Rect, pen: Pen, frame: Color = GrC.brass, matte: Color = GrC.cream): Rect {
    inkedRound(r, r.width * 0.06f, frame, pen)
    val m = r.width * 0.09f
    val inner = Rect(r.left + m, r.top + m, r.right - m, r.bottom - m)
    drawRect(matte, inner.topLeft, inner.size)
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.thin)
    return inner
}

/** A pointed arch path (pixels): from the bottom left up to the springing line and over the top. */
internal fun archPath(l: Float, r: Float, bottom: Float, spring: Float, top: Float, steps: Int = 14): Path = Path().apply {
    val cx = (l + r) / 2f
    val rx = (r - l) / 2f
    val ry = spring - top
    moveTo(l, bottom)
    lineTo(l, spring)
    for (k in 1..steps) {
        val a = Math.PI.toFloat() * (1f - k / steps.toFloat())
        lineTo(cx + cos(a) * rx, spring - sin(a) * ry)
    }
    lineTo(r, bottom)
    close()
}

/** A flat ring-shaped detail: a thin outlined rounded rect (a panel moulding). */
internal fun DrawScope.grPanel(r: Rect, fill: Color, line: Color, pen: Pen, radius: Float = 0f) {
    if (radius > 0f) {
        inkedRound(r, radius, fill, pen, shade = false)
    } else {
        drawRect(fill, r.topLeft, r.size)
        drawRect(line, r.topLeft, r.size, style = Stroke(pen.lw * 0.6f))
    }
}

/** Mixes a colour toward the night blue, for things that glow less in the dark. */
internal fun Color.grDusk(night: Float): Color = if (night <= 0f) this else lerp(this, Color(0xFF1B1A3D), night * 0.35f)

/** Round-capped stroke of [width] pixels. */
internal fun roundStroke(width: Float): Stroke = Stroke(width = width, cap = StrokeCap.Round)
