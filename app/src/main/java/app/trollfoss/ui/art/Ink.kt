package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp

/**
 * The drawing kit behind every figure, thing and place (docs/DESIGN.md §6, «mjuk leikekasse»):
 * warm ink outlines of one width, two-tone cel shading, a single small shine, and soft shadows.
 */
object Ink {
    val line = Color(0xFF2B2140)
    val shadow = Color(0x2E2B2140)
    val blush = Color(0x59FF6F91)
    val white = Color(0xFFFFFFFF)
}

/**
 * What every art function needs besides its own data: [lw] is the outline width in pixels, [t] the
 * clock in seconds for idle motion, [night] from 0 (day) to 1 (night), the [weather], and [rainbow]
 * from 0 to 1 while a rainbow shows after rain.
 */
class Pen(
    val lw: Float,
    val t: Float = 0f,
    val night: Float = 0f,
    val weather: app.trollfoss.domain.Weather = app.trollfoss.domain.Weather.SUN,
    val rainbow: Float = 0f,
    /** The season and the feast of the day; the art may dress the scene for them. Summer is the plain look. */
    val season: app.trollfoss.domain.Season = app.trollfoss.domain.Season.SUMMER,
    val festival: app.trollfoss.domain.Festival = app.trollfoss.domain.Festival.NONE,
) {
    val stroke: Stroke = Stroke(width = lw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thin: Stroke = Stroke(width = lw * 0.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
}

/**
 * A radial gradient that can never crash: Android refuses a radius of zero or less (a growing star, a
 * shrunk light, a figure scaled to nothing), so the radius is kept at a tiny positive value instead.
 */
fun safeRadialGradient(colors: List<Color>, center: Offset = Offset.Unspecified, radius: Float = Float.POSITIVE_INFINITY): Brush =
    Brush.radialGradient(colors, center, if (radius.isFinite()) radius.coerceAtLeast(0.5f) else radius)

/** The same with explicit stops: `safeRadialGradient(0f to a, 1f to b, center = c, radius = r)`. */
fun safeRadialGradient(vararg colorStops: Pair<Float, Color>, center: Offset = Offset.Unspecified, radius: Float = Float.POSITIVE_INFINITY, tileMode: androidx.compose.ui.graphics.TileMode = androidx.compose.ui.graphics.TileMode.Clamp): Brush =
    Brush.radialGradient(*colorStops, center = center, radius = if (radius.isFinite()) radius.coerceAtLeast(0.5f) else radius, tileMode = tileMode)

fun Color.lighten(f: Float): Color = lerp(this, Color.White, f)

/** Shades toward the warm ink rather than black, so shadows stay friendly. */
fun Color.darken(f: Float): Color = lerp(this, Ink.line, f)

fun argb(value: Int): Color = Color(value)

/** Light top, true colour in the middle, a little darker at the bottom. */
fun shadeBrush(color: Color, top: Float, bottom: Float): Brush = Brush.verticalGradient(
    0f to color.lighten(0.24f),
    0.45f to color,
    1f to color.darken(0.16f),
    startY = top,
    endY = bottom,
)

/** The shadow side of a colour in the crisp two-tone shading. */
fun Color.shadow(): Color = lerp(this, Ink.line, 0.22f)

/** The lit face: a touch lighter at the top, so round things still read as round. */
private fun litBrush(color: Color, top: Float, bottom: Float): Brush =
    Brush.verticalGradient(0f to color.lighten(0.14f), 0.5f to color, 1f to color, startY = top, endY = bottom)

/**
 * Fills [path] with crisp cel shading — the shadow sits as a hard-edged crescent on the lower right,
 * like light from the upper left — and outlines it in ink.
 */
fun DrawScope.inked(path: Path, color: Color, pen: Pen, shade: Boolean = true, outline: Boolean = true) {
    if (shade) {
        val b = path.getBounds()
        val s = minOf(b.width, b.height) * 0.13f
        drawPath(path, color.shadow())
        clipPath(path) {
            translate(-s * 0.5f, -s) { drawPath(path, litBrush(color, b.top - s, b.bottom - s)) }
        }
    } else {
        drawPath(path, color)
    }
    if (outline) drawPath(path, Ink.line, style = pen.stroke)
}

fun DrawScope.inkedCircle(center: Offset, radius: Float, color: Color, pen: Pen, shade: Boolean = true) {
    if (shade) {
        drawCircle(color.shadow(), radius, center)
        // A lit circle nudged up and left leaves a sharp crescent of shadow.
        val nudge = radius * 0.1f
        drawCircle(litBrush(color, center.y - radius, center.y + radius), radius - nudge * 1.25f, Offset(center.x - nudge * 0.55f, center.y - nudge))
    } else {
        drawCircle(color, radius, center)
    }
    drawCircle(Ink.line, radius, center, style = pen.stroke)
}

fun DrawScope.inkedOval(rect: Rect, color: Color, pen: Pen, shade: Boolean = true) {
    if (shade) {
        drawOval(color.shadow(), rect.topLeft, rect.size)
        val sx = rect.width * 0.1f
        val sy = rect.height * 0.12f
        drawOval(litBrush(color, rect.top, rect.bottom), rect.topLeft, Size(rect.width - sx, rect.height - sy))
    } else {
        drawOval(color, rect.topLeft, rect.size)
    }
    drawOval(Ink.line, rect.topLeft, rect.size, style = pen.stroke)
}

fun DrawScope.inkedRound(rect: Rect, radius: Float, color: Color, pen: Pen, shade: Boolean = true) {
    val corner = CornerRadius(radius, radius)
    if (shade) {
        drawRoundRect(color.shadow(), rect.topLeft, rect.size, corner)
        val s = minOf(rect.width, rect.height) * 0.12f
        drawRoundRect(litBrush(color, rect.top, rect.bottom), rect.topLeft, Size(rect.width - s * 0.6f, rect.height - s), corner)
    } else {
        drawRoundRect(color, rect.topLeft, rect.size, corner)
    }
    drawRoundRect(Ink.line, rect.topLeft, rect.size, corner, style = pen.stroke)
}

/** A limb or a stick: a thick round-capped line with an ink rim. */
fun DrawScope.capsule(from: Offset, to: Offset, width: Float, color: Color, pen: Pen) {
    drawLine(Ink.line, from, to, strokeWidth = width + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(color, from, to, strokeWidth = width, cap = StrokeCap.Round)
}

/** A plain ink line. */
fun DrawScope.inkLine(from: Offset, to: Offset, pen: Pen, width: Float = pen.lw, color: Color = Ink.line) {
    drawLine(color, from, to, strokeWidth = width, cap = StrokeCap.Round)
}

/** The small white shine on anything glossy. */
fun DrawScope.shine(center: Offset, w: Float, h: Float, alpha: Float = 0.8f) {
    drawOval(Color.White.copy(alpha = alpha), Offset(center.x - w / 2, center.y - h / 2), Size(w, h))
}

/** A soft oval shadow on the ground, centred on [x] at [y]. */
fun DrawScope.groundShadow(x: Float, y: Float, w: Float, alpha: Float = 1f) {
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(x - w / 2, y - w * 0.09f), Size(w, w * 0.18f))
}

fun rect(cx: Float, cy: Float, w: Float, h: Float): Rect = Rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)

fun roundPath(rect: Rect, radius: Float): Path = Path().apply { addRoundRect(RoundRect(rect, CornerRadius(radius, radius))) }

fun ovalPath(rect: Rect): Path = Path().apply { addOval(rect) }

/** A closed smooth shape through [points] (x, y pairs), rounded with quadratic curves between midpoints. */
fun blobPath(vararg points: Float): Path {
    val n = points.size / 2
    val path = Path()
    fun px(i: Int) = points[(i.mod(n)) * 2]
    fun py(i: Int) = points[(i.mod(n)) * 2 + 1]
    path.moveTo((px(0) + px(1)) / 2, (py(0) + py(1)) / 2)
    for (i in 1..n) {
        val mx = (px(i) + px(i + 1)) / 2
        val my = (py(i) + py(i + 1)) / 2
        path.quadraticTo(px(i), py(i), mx, my)
    }
    path.close()
    return path
}

/**
 * «Skrå-3D»: the world is drawn in oblique projection. Depth runs up and to the right: a point [d]
 * units further back is drawn at (+DX·d, +DY·d) units on screen. No shrinking with distance, so every
 * size in the domain stays true.
 */
object Oblique {
    const val DX = 0.5f
    const val DY = -0.36f

    /** Screen offset in pixels for [depth] units of depth at [u] pixels per unit. */
    fun offset(depth: Float, u: Float): Offset = Offset(DX * depth * u, DY * depth * u)
}

/**
 * A box in oblique 3D: the [front] face as given, a lit top face and a shaded right side receding by
 * [depth] pixels of depth (converted with [Oblique]). Draw details on the front face afterwards.
 */
fun DrawScope.box3d(front: Rect, depth: Float, color: Color, pen: Pen, top: Color = color.lighten(0.18f), side: Color = color.darken(0.22f), radius: Float = 0f) {
    val dx = Oblique.DX * depth
    val dy = Oblique.DY * depth
    val topFace = Path().apply {
        moveTo(front.left, front.top)
        lineTo(front.left + dx, front.top + dy)
        lineTo(front.right + dx, front.top + dy)
        lineTo(front.right, front.top)
        close()
    }
    val sideFace = Path().apply {
        moveTo(front.right, front.top)
        lineTo(front.right + dx, front.top + dy)
        lineTo(front.right + dx, front.bottom + dy)
        lineTo(front.right, front.bottom)
        close()
    }
    drawPath(sideFace, side)
    drawPath(sideFace, Ink.line, style = pen.stroke)
    drawPath(topFace, top)
    drawPath(topFace, Ink.line, style = pen.stroke)
    if (radius > 0f) inkedRound(front, radius, color, pen) else inked(Path().apply { addRect(front) }, color, pen)
}

/** A flat oblique top (a table top, a shelf board) from [left] to [right] at height [y], [depth] pixels deep. */
fun DrawScope.topFace3d(left: Float, right: Float, y: Float, depth: Float, color: Color, pen: Pen) {
    val dx = Oblique.DX * depth
    val dy = Oblique.DY * depth
    val face = Path().apply {
        moveTo(left, y)
        lineTo(left + dx, y + dy)
        lineTo(right + dx, y + dy)
        lineTo(right, y)
        close()
    }
    drawPath(face, color)
    drawPath(face, Ink.line, style = pen.stroke)
}

/** A four-pointed twinkle, used for sparkles and glimt. */
fun DrawScope.twinkle(center: Offset, radius: Float, color: Color, alpha: Float = 1f) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color.copy(alpha = color.alpha * alpha))
}

/** A five-pointed star with ink rim, used for glimt and the counter. */
fun starPath(center: Offset, outer: Float, inner: Float, turn: Float = 0f): Path = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = Math.toRadians((turn - 90 + i * 36).toDouble())
        val x = center.x + (r * kotlin.math.cos(a)).toFloat()
        val y = center.y + (r * kotlin.math.sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
