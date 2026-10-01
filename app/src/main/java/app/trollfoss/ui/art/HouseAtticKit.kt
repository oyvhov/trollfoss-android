package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.hypot
import kotlin.math.sin

/*
 * Storhuset, attic: the shared drawing kit of the attic's background, furniture and things. Colours are
 * dusty, warm and friendly; the dark is charming, never scary. Private helpers elsewhere start with `at`.
 */

internal object AtC {
    // Weathered pine: the storage and the beams.
    val plank = Color(0xFFA9835B)
    val plankDark = Color(0xFF6B4D35)
    val plankLight = Color(0xFFC79B6A)
    val beam = Color(0xFF5B4030)
    val beamLight = Color(0xFF7D5C42)
    val beamDark = Color(0xFF3F2C20)

    // Dust sheets.
    val sheet = Color(0xFFEFEADF)
    val sheetShade = Color(0xFFCFC8B8)
    val sheetDark = Color(0xFFA9A291)

    // The ghost's nook: pale blue and cream.
    val blue = Color(0xFFB9D3E6)
    val blueDark = Color(0xFF8FB0CC)
    val blueLight = Color(0xFFDCEBF5)
    val cream = Color(0xFFF6EEDC)
    val creamDark = Color(0xFFE3D3B0)
    val rose = Color(0xFFE8A6B0)

    // The tower: slate, brass and sky.
    val navy = Color(0xFF24384A)
    val navyLight = Color(0xFF3A5670)
    val slate = Color(0xFF4A5A6E)
    val brass = Color(0xFFE0B04A)
    val brassDark = Color(0xFFA9782A)
    val brassLight = Color(0xFFFFE08A)

    // The secret room: velvet and gold.
    val velvet = Color(0xFF7A2A3A)
    val velvetDark = Color(0xFF4F1A28)
    val velvetLight = Color(0xFF9B3D50)
    val gold = Color(0xFFE6B84C)
    val goldLight = Color(0xFFFFE18A)
    val goldDark = Color(0xFFB98A2A)
    val mahogany = Color(0xFF5A3426)

    val warm = Color(0xFFFFD27A)
    val moon = Color(0xFFCFE2FF)
}

/** A straight beam from (ax, ay) to (bx, by), [th] thick (pixels), as a four-cornered path. */
internal fun beamPath(ax: Float, ay: Float, bx: Float, by: Float, th: Float): Path {
    val len = hypot(bx - ax, by - ay).coerceAtLeast(0.001f)
    val nx = -(by - ay) / len * th / 2f
    val ny = (bx - ax) / len * th / 2f
    return Path().apply {
        moveTo(ax + nx, ay + ny)
        lineTo(bx + nx, by + ny)
        lineTo(bx - nx, by - ny)
        lineTo(ax - nx, ay - ny)
        close()
    }
}

/** A path of straight segments through (x, y) pairs, not closed. */
internal fun linePath(vararg xy: Float): Path = Path().apply {
    moveTo(xy[0], xy[1])
    var i = 2
    while (i < xy.size) {
        lineTo(xy[i], xy[i + 1])
        i += 2
    }
}

/** Fills [path], then rims it in ink: the plain way to draw a flat detail. */
internal fun DrawScope.atFill(path: Path, color: Color, pen: Pen) {
    drawPath(path, color)
    drawPath(path, Ink.line, style = pen.stroke)
}

/** A small round ink-rimmed dot (a nail, a button, a jewel). */
internal fun DrawScope.atDot(c: Offset, r: Float, color: Color, pen: Pen) {
    drawCircle(color, r, c)
    drawCircle(Ink.line, r, c, style = pen.thin)
}

/** A soft warm glow of lamplight, brighter by night and in the dark of the attic. */
internal fun DrawScope.atGlow(c: Offset, r: Float, color: Color, alpha: Float) = fxGlow(c, r, color, alpha)

/** A fold line: a thin darker stroke along a curve through three points. */
internal fun DrawScope.atFold(a: Offset, b: Offset, c: Offset, color: Color, width: Float) {
    drawPath(Path().apply { moveTo(a.x, a.y); quadraticTo(b.x, b.y, c.x, c.y) }, color, style = Stroke(width, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}

/** A brass band or rim: a brass line with a darker lower edge and a glint. */
internal fun DrawScope.atBrass(a: Offset, b: Offset, w: Float, pen: Pen) {
    drawLine(Ink.line, a, b, strokeWidth = w + pen.lw * 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(AtC.brassDark, a, b, strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(AtC.brass, Offset(a.x, a.y - w * 0.14f), Offset(b.x, b.y - w * 0.14f), strokeWidth = w * 0.62f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}

/** A wooden stand or leg: a round-capped ink-rimmed stick. */
internal fun DrawScope.atStick(a: Offset, b: Offset, w: Float, color: Color, pen: Pen) = capsule(a, b, w, color, pen)

/** The drop shadow under standing furniture: a soft oval on the floor. */
internal fun DrawScope.atShadow(u: Float, w: Float, d: Float = 0.12f, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), androidx.compose.ui.geometry.Size(w * u + dx, -dy + 0.03f * u))
}

/** True for a moment now and then: a blink or a peek that repeats every [period] seconds and lasts [length]. */
internal fun atEvery(t: Float, period: Float, length: Float, phase: Float = 0f): Boolean {
    val p = (t + phase) % period
    return (if (p < 0f) p + period else p) < length
}

/** A 0 to 1 breathing wave. */
internal fun atWave(t: Float, speed: Float, phase: Float = 0f): Float = (sin(t * speed + phase) + 1f) / 2f

/** A rectangle's four corners as a closed path. */
internal fun rectPath(r: Rect): Path = Path().apply { addRect(r) }

/**
 * Rain or snow outside a window: streaks and flakes that run down the glass of the rectangle ([x0], [y0]) to
 * ([x1], [y1]) (scene pixels), when the weather calls for it. Cheap: a dozen short lines or dots.
 */
internal fun DrawScope.atWindowWeather(pen: Pen, x0: Float, y0: Float, x1: Float, y1: Float, n: Int = 14) {
    when (pen.weather) {
        app.trollfoss.domain.Weather.RAIN -> {
            val pts = ArrayList<Offset>(n * 2)
            for (i in 0 until n) {
                val ph = ((pen.t * (0.7f + 0.5f * hash01(i, 301)) + hash01(i, 302)) % 1f)
                val x = x0 + hash01(i, 303) * (x1 - x0)
                val y = y0 + ph * (y1 - y0)
                pts.add(Offset(x, y))
                pts.add(Offset(x - (x1 - x0) * 0.02f, y + (y1 - y0) * 0.07f))
            }
            drawPoints(pts, androidx.compose.ui.graphics.PointMode.Lines, Color(0xFFCFE8FF), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round, alpha = 0.7f)
        }
        app.trollfoss.domain.Weather.SNOW -> {
            val pts = ArrayList<Offset>(n)
            for (i in 0 until n) {
                val ph = ((pen.t * (0.12f + 0.1f * hash01(i, 301)) + hash01(i, 302)) % 1f)
                pts.add(Offset(x0 + (hash01(i, 303) + 0.04f * sin(pen.t + i)) * (x1 - x0), y0 + ph * (y1 - y0)))
            }
            drawPoints(pts, androidx.compose.ui.graphics.PointMode.Points, Color.White, strokeWidth = (x1 - x0) * 0.012f, cap = StrokeCap.Round, alpha = 0.9f)
        }
        else -> Unit
    }
}
