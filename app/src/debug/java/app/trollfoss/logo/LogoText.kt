package app.trollfoss.logo

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.darken
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.twinkle

/**
 * Lettering. Glyph outlines of a heavy sans are fattened and rounded until they look like toy-box letters,
 * then each letter gets its own bounce, colour, cel shading, ink outline and a block of ink underneath.
 */
// Debug-only render tool, run on an API 36 emulator; it never ships, so the API 28 weight call is fine.
@android.annotation.SuppressLint("NewApi")
internal object Fonts {
    /** The heaviest sans the device has (Roboto Flex black on current Android). */
    val black: Typeface = Typeface.create(Typeface.create("sans-serif", Typeface.NORMAL), 900, false)
    val heavy: Typeface = Typeface.create(Typeface.create("sans-serif", Typeface.NORMAL), 800, false)
    val bold: Typeface = Typeface.create(Typeface.create("sans-serif", Typeface.NORMAL), 700, false)
}

private fun paintFor(tf: Typeface, size: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    typeface = tf
    textSize = size
}

/** Glyph outlines of [text] with the baseline-left at the origin. */
internal fun textPath(text: String, tf: Typeface, size: Float, tracking: Float = 0f): Pair<Path, Float> {
    val paint = paintFor(tf, size)
    val out = android.graphics.Path()
    var x = 0f
    for (ch in text) {
        val s = ch.toString()
        val p = android.graphics.Path()
        paint.getTextPath(s, 0, 1, x, 0f, p)
        out.addPath(p)
        x += paint.measureText(s) + tracking
    }
    return out.asComposePath() to (x - tracking)
}

/** [src] fattened by [grow] on every side with round joins: rounder, chunkier letters. */
internal fun fatten(src: android.graphics.Path, grow: Float): Path {
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = grow * 2f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    val ring = android.graphics.Path()
    stroke.getFillPath(src, ring)
    val union = android.graphics.Path()
    union.op(src, ring, android.graphics.Path.Op.UNION)
    return union.asComposePath()
}

private class Letter(val ch: Char, val path: Path, val bounds: Rect, val advance: Float)

private class Look3(val top: Color, val base: Color, val deep: Color)

private val wordPalette = listOf(
    Look3(L.sunTop, L.sun, L.sunDeep),
    Look3(L.berryTop, L.berry, L.berryDeep),
    Look3(L.seaTop, L.sea, L.seaDeep),
    Look3(L.mintTop, L.mint, L.mintDeep),
    Look3(L.grapeTop, L.grape, L.grapeDeep),
)

private const val WORD = "Trollfoss"
private val colorIndex = intArrayOf(0, 1, 2, 3, 4, 0, 1, 2, 3)
private val bounce = floatArrayOf(0f, -0.024f, 0.016f, -0.02f, 0.02f, -0.016f, 0.022f, -0.02f, 0.012f)
private val tilt = floatArrayOf(-3f, 2.5f, -2f, 2f, -2.5f, 3f, -3f, 2.5f, -2f)
private val sizeBoost = floatArrayOf(1.0f, 0.97f, 1.02f, 1.0f, 1.0f, 1.0f, 1.02f, 1.01f, 1.0f)
private val tuck = floatArrayOf(-0.01f, -0.01f, -0.025f, -0.03f, -0.025f, -0.0f, -0.03f, -0.03f, -0.02f)

private fun buildLetters(size: Float): List<Letter> {
    val paint = paintFor(Fonts.black, size)
    val grow = size * 0.028f
    return WORD.mapIndexed { i, ch ->
        val raw = android.graphics.Path()
        paint.getTextPath(ch.toString(), 0, 1, 0f, 0f, raw)
        val fat = fatten(raw, grow)
        Letter(ch, fat, fat.getBounds(), paint.measureText(ch.toString()) + tuck[i] * size)
    }
}

/** The width the word takes at [size]: letters tuck close together. */
internal fun wordmarkWidth(size: Float): Float {
    val ls = buildLetters(size)
    return ls.sumOf { it.advance.toDouble() }.toFloat() + size * 0.04f
}

/**
 * «Trollfoss» with its baseline starting at [origin]: chunky bouncing letters in Sun, Berry, Sea, Mint and
 * Grape, a sprout on the T, and a rainbow under the word. [size] is the font size in pixels.
 */
internal fun DrawScope.drawWordmark(origin: Offset, size: Float, rainbow: Boolean = true, keyline: Float = 0f, sprout: Boolean = true) {
    val letters = buildLetters(size)
    val ol = size * 0.036f
    val depth = size * 0.075f
    val xs = FloatArray(letters.size)
    var x = origin.x
    for (i in letters.indices) {
        xs[i] = x
        x += letters[i].advance
    }
    val total = x - origin.x

    fun place(i: Int, block: DrawScope.() -> Unit) {
        val l = letters[i]
        val cx = l.bounds.center.x
        withTransform({
            translate(xs[i], origin.y + bounce[i] * size)
            rotate(tilt[i], pivot = Offset(cx, l.bounds.center.y))
            scale(sizeBoost[i], sizeBoost[i], pivot = Offset(cx, 0f))
        }) { block() }
    }

    // ---- a white keyline round everything, for dark backgrounds
    if (keyline > 0f) {
        for (i in letters.indices) {
            place(i) {
                val steps = 14
                for (k in 0..steps) {
                    val f = k / steps.toFloat()
                    translate(depth * 0.3f * f, depth * f) {
                        drawPath(letters[i].path, Color.White, style = Stroke(ol + keyline * 2f, join = StrokeJoin.Round))
                    }
                }
            }
        }
    }

    // ---- the rainbow swoosh under the word
    if (rainbow) drawWordRainbow(origin, size, total, keyline)

    // ---- the block of ink under every letter
    for (i in letters.indices) {
        place(i) {
            val steps = 18
            for (k in 1..steps) {
                val f = k / steps.toFloat()
                translate(depth * 0.3f * f, depth * f) {
                    drawPath(letters[i].path, Ink.line)
                    drawPath(letters[i].path, Ink.line, style = Stroke(ol, join = StrokeJoin.Round))
                }
            }
        }
    }

    // ---- the letters themselves
    for (i in letters.indices) {
        val look = wordPalette[colorIndex[i]]
        place(i) {
            val l = letters[i]
            drawPath(l.path, Ink.line, style = Stroke(ol, join = StrokeJoin.Round))
            val s = size * 0.05f
            // The shaded side, then the lit face nudged up and to the left so a hard crescent is left.
            drawPath(l.path, look.deep)
            clipPath(l.path) {
                translate(-s * 0.55f, -s) {
                    drawPath(l.path, look.base)
                    // A lighter cap of colour at the top.
                    clipPath(l.path) {
                        drawRect(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                0f to look.top, 0.5f to look.top.copy(alpha = 0f),
                                startY = l.bounds.top, endY = l.bounds.bottom,
                            ),
                            Offset(l.bounds.left - 10f, l.bounds.top - 10f), Size(l.bounds.width + 20f, l.bounds.height + 20f),
                        )
                    }
                }
            }
            drawPath(l.path, Ink.line, style = Stroke(ol, join = StrokeJoin.Round))
            // Glints.
            val b = l.bounds
            val hl = Color.White.copy(alpha = 0.78f)
            when (l.ch) {
                'o', 's' -> {
                    drawArc(hl, 205f, 55f, false, Offset(b.left + b.width * 0.2f, b.top + b.height * 0.16f), Size(b.width * 0.6f, b.height * 0.6f), style = Stroke(size * 0.03f, cap = StrokeCap.Round))
                }
                'T' -> {
                    drawLine(hl, Offset(b.left + b.width * 0.12f, b.top + b.height * 0.13f), Offset(b.left + b.width * 0.34f, b.top + b.height * 0.13f), strokeWidth = size * 0.03f, cap = StrokeCap.Round)
                    drawLine(hl, Offset(b.left + b.width * 0.42f, b.top + b.height * 0.34f), Offset(b.left + b.width * 0.42f, b.top + b.height * 0.52f), strokeWidth = size * 0.03f, cap = StrokeCap.Round)
                }
                else -> {
                    drawLine(hl, Offset(b.left + b.width * 0.26f, b.top + b.height * 0.18f), Offset(b.left + b.width * 0.26f, b.top + b.height * 0.34f), strokeWidth = size * 0.03f, cap = StrokeCap.Round)
                }
            }
        }
    }

    // ---- the sprout on the T, Rumle's own
    if (sprout) {
        val l = letters[0]
        place(0) {
            val base = Offset(l.bounds.left + l.bounds.width * 0.2f, l.bounds.top + size * 0.03f)
            val tip = Offset(base.x - size * 0.03f, base.y - size * 0.19f)
            val stem = Path().apply {
                moveTo(base.x, base.y)
                quadraticTo(base.x + size * 0.03f, base.y - size * 0.1f, tip.x, tip.y)
            }
            drawPath(stem, Ink.line, style = Stroke(size * 0.062f, cap = StrokeCap.Round))
            drawPath(stem, L.mintDeep, style = Stroke(size * 0.028f, cap = StrokeCap.Round))
            val pen = logoPen(size * 0.026f)
            inked(leafPath(tip, Offset(tip.x + size * 0.26f, tip.y - size * 0.08f), size * 0.065f, size * 0.022f), L.mint, pen)
            inked(leafPath(tip, Offset(tip.x - size * 0.2f, tip.y - size * 0.1f), size * 0.058f, -size * 0.016f), L.mint, pen)
        }
    }
    twinkle(Offset(origin.x + total + size * 0.06f, origin.y - size * 0.78f), size * 0.1f, Color.White)
    twinkle(Offset(origin.x + total * 0.58f, origin.y - size * 0.98f), size * 0.06f, Color.White, 0.9f)
}

/** A small rainbow like a smile under the word, with clean ribbon ends. */
private fun DrawScope.drawWordRainbow(origin: Offset, size: Float, total: Float, keyline: Float = 0f) {
    val band = size * 0.036f
    val n = 5
    val ol = size * 0.03f
    val colors = listOf(L.grape, L.sea, L.mint, L.sun, L.berry)
    val x0 = origin.x + total * 0.3f
    val x1 = origin.x + total * 0.985f
    val chord = x1 - x0
    val rr = chord * 1.15f
    val cx = (x0 + x1) / 2f
    val low = origin.y + size * 0.40f
    val cy = low - band * n - rr
    val sweep = Math.toDegrees(2.0 * Math.asin((chord / 2.0) / (rr + band * n / 2.0))).toFloat()
    val start = 90f - sweep / 2f
    val mid = rr + band * n / 2f
    val cap = Math.toDegrees((ol / mid).toDouble()).toFloat()
    if (keyline > 0f) {
        val kc = Math.toDegrees(((ol + keyline) / mid).toDouble()).toFloat()
        drawArc(Color.White, start - kc, sweep + 2f * kc, false, Offset(cx - mid, cy - mid), Size(mid * 2f, mid * 2f), style = Stroke(band * n + (ol + keyline) * 2f, cap = StrokeCap.Butt))
    }
    drawArc(Ink.line, start - cap, sweep + 2f * cap, false, Offset(cx - mid, cy - mid), Size(mid * 2f, mid * 2f), style = Stroke(band * n + ol * 2f, cap = StrokeCap.Butt))
    for (i in 0 until n) {
        val r = rr + band * (i + 0.5f)
        drawArc(colors[i], start, sweep, false, Offset(cx - r, cy - r), Size(r * 2f, r * 2f), style = Stroke(band * 1.04f, cap = StrokeCap.Butt))
    }
}

/** Outlined lettering for taglines: ink rim, fill, a soft drop underneath. */
internal fun DrawScope.drawOutlinedText(
    text: String, origin: Offset, size: Float, tf: Typeface, fill: Color,
    rim: Float = size * 0.14f, drop: Float = size * 0.05f, tracking: Float = 0f, rimColor: Color = Ink.line,
): Float {
    val (path, width) = textPath(text, tf, size, tracking)
    translate(origin.x, origin.y) {
        translate(0f, drop) { drawPath(path, rimColor, style = Stroke(rim, join = StrokeJoin.Round)) }
        drawPath(path, rimColor, style = Stroke(rim, join = StrokeJoin.Round))
        drawPath(path, fill)
    }
    return width
}

internal fun measureText(text: String, tf: Typeface, size: Float, tracking: Float = 0f): Float = textPath(text, tf, size, tracking).second

internal fun lightenColor(c: Color, f: Float) = c.lighten(f)
internal fun darkenColor(c: Color, f: Float) = c.darken(f)
