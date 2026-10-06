package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.ui.theme.T
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Trollfossen as the way to level 10: ten stones up the fall, and a glow that rises with every sticker. */
object FallProgress {
    const val STONES = 10

    /** Height of stone [i] on the fall, 0 at the bottom and 1 at the top. */
    fun stone(i: Int): Float = i.coerceIn(0, STONES - 1) / (STONES - 1f)

    /** How high the glow stands: at the stone of the current level, and part of the way to the next. */
    fun fill(stickers: Int, thresholds: List<Int>): Float {
        val level = thresholds.count { stickers >= it }.coerceAtLeast(1)
        if (level >= thresholds.size) return stone(level - 1)
        val from = thresholds[level - 1]; val to = thresholds[level]
        return stone(level - 1) + (stone(level) - stone(level - 1)) * ((stickers - from).toFloat() / (to - from))
    }

    /** One sticker left to the next level: the gift on the next stone starts to hop. */
    fun ready(stickers: Int, thresholds: List<Int>): Boolean {
        val level = thresholds.count { stickers >= it }.coerceAtLeast(1)
        return level < thresholds.size && thresholds[level] - stickers <= 1
    }
}

/** A little waterfall for the book: the same glow and stones as on the map, in a small frame. */
fun DrawScope.drawMiniFall(stickers: Int, thresholds: List<Int>) {
    val w = size.width; val h = size.height; val lw = w * 0.05f
    val fx = w * 0.6f; val fw = w * 0.5f
    drawRoundRect(Color(0xFFE4F6FD), Offset(fx - fw / 2, 0f), Size(fw, h), CornerRadius(fw * 0.3f))
    val top = h * (1f - FallProgress.fill(stickers, thresholds))
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFB8F0FF), Color(0xFF5FD4FF)), top, h), Offset(fx - fw / 2, top), Size(fw, h - top), CornerRadius(fw * 0.3f))
    drawRoundRect(T.Ink, Offset(fx - fw / 2, 0f), Size(fw, h), CornerRadius(fw * 0.3f), style = Stroke(lw))
    val level = thresholds.count { stickers >= it }.coerceAtLeast(1)
    for (i in 0 until FallProgress.STONES) drawCircle(if (i < level) T.Sun else T.Cream, w * 0.09f, Offset(w * 0.14f, h * 0.06f + (h * 0.88f) * (1f - FallProgress.stone(i))))
}

/** Draws the rising glow, the ten stones, the next gift and the waving troll over the map's waterfall. */
internal fun DrawScope.drawFallProgress(g: MapGeo, pen: Pen, stickers: Int, thresholds: List<Int>, t: Float, motion: Boolean) {
    val fall = g.fall
    val h = size.height
    val level = thresholds.count { stickers >= it }.coerceAtLeast(1)
    val span = fall.bottom - fall.top
    val glowTop = fall.bottom - span * FallProgress.fill(stickers, thresholds)
    val gw = fall.bw * 0.8f
    // The glow: brighter water that rises from the pool.
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFB8F0FF), Color(0xFF5FD4FF)), glowTop, fall.bottom),
        Offset(fall.cx - gw / 2, glowTop), Size(gw, fall.bottom - glowTop), CornerRadius(gw * 0.3f), alpha = 0.55f)
    for (i in 0 until 6) {
        val k = if (motion) ((t * 0.25f + i / 6f) % 1f) else i / 6f
        val y = fall.bottom - (fall.bottom - glowTop) * k
        drawCircle(Color.White, h * 0.0045f, Offset(fall.cx + sin(i * 2.1f + (if (motion) t else 0f)) * gw * 0.35f, y), alpha = 0.9f)
    }
    // Ten stones up the left side: gold for levels reached.
    val r = h * 0.016f
    val sx = fall.cx - fall.bw * 0.5f - r * 1.8f
    for (i in 0 until FallProgress.STONES) {
        val c = Offset(sx, fall.bottom - span * FallProgress.stone(i))
        drawCircle(if (i < level) T.Sun else T.Cream, r, c)
        drawCircle(T.Ink, r, c, style = Stroke(pen.lw))
    }
    // The next gift waits on the next stone, and hops when one sticker is left.
    if (level < thresholds.size) {
        val hop = if (motion && FallProgress.ready(stickers, thresholds)) -abs(sin(t * 5f)) * r * 0.9f else 0f
        val c = Offset(sx - r * 2.6f, fall.bottom - span * FallProgress.stone(level) + hop)
        val b = r * 1.1f
        drawRoundRect(Color(0xFFE45B78), Offset(c.x - b, c.y - b), Size(b * 2, b * 1.7f), CornerRadius(b * 0.25f))
        drawRoundRect(T.Ink, Offset(c.x - b, c.y - b), Size(b * 2, b * 1.7f), CornerRadius(b * 0.25f), style = Stroke(pen.lw))
        drawRect(T.Sun, Offset(c.x - b * 0.18f, c.y - b), Size(b * 0.36f, b * 1.7f))
        drawCircle(T.Sun, b * 0.32f, Offset(c.x - b * 0.3f, c.y - b * 1.1f)); drawCircle(T.Sun, b * 0.32f, Offset(c.x + b * 0.3f, c.y - b * 1.1f))
    }
    // The troll on top of the fall blinks and waves, waiting for level 10.
    val tr = h * 0.028f
    val tc = Offset(fall.cx + fall.bw * 0.95f, fall.top + h * 0.004f)
    val green = Color(0xFF6AA86A)
    val wave = if (motion) sin(t * 3f) * 0.5f else 0f
    val hand = Offset(tc.x + tr * 1.6f + cos(-1.2f + wave) * tr * 0.4f, tc.y - tr * 0.9f + sin(-1.2f + wave) * tr * 0.6f)
    drawLine(T.Ink, Offset(tc.x + tr * 0.8f, tc.y + tr * 0.2f), hand, strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
    drawLine(green, Offset(tc.x + tr * 0.8f, tc.y + tr * 0.2f), hand, strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    for (side in intArrayOf(-1, 1)) {
        val e = Offset(tc.x + side * tr * 0.95f, tc.y - tr * 0.35f)
        drawOval(green, Offset(e.x - tr * 0.3f, e.y - tr * 0.45f), Size(tr * 0.6f, tr * 0.9f))
        drawOval(T.Ink, Offset(e.x - tr * 0.3f, e.y - tr * 0.45f), Size(tr * 0.6f, tr * 0.9f), style = Stroke(pen.lw))
    }
    drawCircle(green, tr, tc); drawCircle(T.Ink, tr, tc, style = Stroke(pen.lw))
    val blink = motion && t % 4f < 0.15f
    for (side in intArrayOf(-1, 1)) {
        val e = Offset(tc.x + side * tr * 0.35f, tc.y - tr * 0.12f)
        if (blink) drawLine(T.Ink, Offset(e.x - tr * 0.12f, e.y), Offset(e.x + tr * 0.12f, e.y), strokeWidth = pen.lw)
        else drawCircle(T.Ink, tr * 0.12f, e)
    }
    drawArc(T.Ink, 20f, 140f, false, Offset(tc.x - tr * 0.45f, tc.y - tr * 0.05f), Size(tr * 0.9f, tr * 0.6f), style = Stroke(pen.lw))
}
