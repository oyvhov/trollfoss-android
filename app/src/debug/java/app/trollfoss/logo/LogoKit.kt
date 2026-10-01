package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.ui.art.twinkle
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.darken
import app.trollfoss.ui.art.hash01
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.safeRadialGradient
import app.trollfoss.ui.art.shadow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The brand palette: the interface colours of the game plus the Nordic landscape colours. */
internal object L {
    val ink = Ink.line

    val sun = Color(0xFFFFC83D)
    val sunTop = Color(0xFFFFE58A)
    val sunDeep = Color(0xFFD98A00)
    val berry = Color(0xFFFF4D6D)
    val berryTop = Color(0xFFFF8FA3)
    val berryDeep = Color(0xFFC21F45)
    val sea = Color(0xFF2F9BFF)
    val seaTop = Color(0xFF7CCBFF)
    val seaDeep = Color(0xFF1560C0)
    val mint = Color(0xFF2FD18B)
    val mintTop = Color(0xFF86F2BF)
    val mintDeep = Color(0xFF14935C)
    val grape = Color(0xFF8B5CF6)
    val grapeTop = Color(0xFFC4A6FF)
    val grapeDeep = Color(0xFF5B32C9)

    val falun = Color(0xFFB8342B)
    val falunLight = Color(0xFFD2443A)
    val ochre = Color(0xFFE8B43C)
    val gran = Color(0xFF1F7048)
    val granLight = Color(0xFF2E8B57)
    val grass = Color(0xFF6FAE5A)
    val fjord = Color(0xFF2F6FB8)
    val fjordLight = Color(0xFF5AA9E6)
    val snow = Color(0xFFF4F8FF)
    val snowShade = Color(0xFFC9D6EE)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)

    val skin = Color(0xFF7FB5A6)
    val hair = Color(0xFF5A3824)
    val shirt = Color(0xFFF7F4EE)
    val mouth = Color(0xFF7A2440)
    val tongue = Color(0xFFFF7F9E)
    val blush = Color(0x59FF6F91)

    val rainbow = listOf(
        Color(0xFFFF5A5F), Color(0xFFFF9F43), Color(0xFFFFD93D), Color(0xFF6BCB77),
        Color(0xFF4D96FF), Color(0xFF6A5ACD), Color(0xFFB983FF),
    )
}

/** A pen for the brand art: outline [lw] pixels wide, time frozen. */
internal fun logoPen(lw: Float) = Pen(lw = lw, t = 1.3f)

// ------------------------------------------------------------------------------------------ shapes

/** A leaf from [base] to [tip], [width] wide at its fullest, bending to one side by [bend]. */
internal fun leafPath(base: Offset, tip: Offset, width: Float, bend: Float = 0f): Path {
    val dx = tip.x - base.x
    val dy = tip.y - base.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    val nx = -dy / len
    val ny = dx / len
    val mid = Offset((base.x + tip.x) / 2f + nx * bend, (base.y + tip.y) / 2f + ny * bend)
    return Path().apply {
        moveTo(base.x, base.y)
        quadraticTo(mid.x + nx * width, mid.y + ny * width, tip.x, tip.y)
        quadraticTo(mid.x - nx * width, mid.y - ny * width, base.x, base.y)
        close()
    }
}

internal fun polyPath(vararg p: Float): Path = Path().apply {
    moveTo(p[0], p[1])
    var i = 2
    while (i < p.size) {
        lineTo(p[i], p[i + 1])
        i += 2
    }
    close()
}

/** A fluffy cloud (flat base, round puffs) with a lit top and a cool shaded belly, optionally inked. */
internal fun DrawScope.lgCloud(cx: Float, cy: Float, s: Float, lw: Float, inkAlpha: Float = 0f, body: Color = Color.White, shade: Color = Color(0xFFD3E6F8)) {
    val p = Path().apply {
        addOval(Rect(cx - 1.25f * s, cy - 0.45f * s, cx - 0.35f * s, cy + 0.3f * s))
        addOval(Rect(cx - 0.75f * s, cy - 0.85f * s, cx + 0.35f * s, cy + 0.25f * s))
        addOval(Rect(cx - 0.05f * s, cy - 0.65f * s, cx + 0.95f * s, cy + 0.3f * s))
        addOval(Rect(cx + 0.55f * s, cy - 0.3f * s, cx + 1.35f * s, cy + 0.3f * s))
        addOval(Rect(cx - 1.1f * s, cy - 0.05f * s, cx + 1.25f * s, cy + 0.32f * s))
    }
    drawPath(p, shade)
    clipPath(p) {
        translate(-s * 0.05f, -s * 0.14f) { drawPath(p, body) }
    }
    if (inkAlpha > 0f) drawPath(p, Ink.line, alpha = inkAlpha, style = Stroke(lw, join = StrokeJoin.Round))
}

/**
 * A mountain with a lit left face, a shaded right face and a snow cap with a ragged hem. [cap] is how far
 * down the snow comes, as a fraction of the height. [outline] draws a soft ink line.
 */
internal fun DrawScope.lgMountain(
    ax: Float, ay: Float, hwL: Float, hwR: Float, by: Float,
    body: Color, shade: Color, snow: Color, snowShade: Color, cap: Float,
    lw: Float, seed: Int, outline: Float = 0f, outlineColor: Color = Ink.line,
) {
    val h = by - ay
    fun j(i: Int) = hash01(i, seed) - 0.5f
    val lx = ax - hwL
    val rx = ax + hwR
    val shape = Path().apply {
        moveTo(lx, by)
        lineTo(ax - hwL * 0.66f + j(1) * hwL * 0.1f, ay + h * 0.56f)
        lineTo(ax - hwL * 0.46f + j(2) * hwL * 0.08f, ay + h * 0.38f)
        lineTo(ax - hwL * 0.2f + j(3) * hwL * 0.06f, ay + h * 0.2f)
        lineTo(ax - hwL * 0.06f, ay + h * 0.05f)
        quadraticTo(ax, ay - h * 0.02f, ax + hwR * 0.06f, ay + h * 0.06f)
        lineTo(ax + hwR * 0.24f + j(4) * hwR * 0.05f, ay + h * 0.24f)
        lineTo(ax + hwR * 0.48f + j(5) * hwR * 0.08f, ay + h * 0.45f)
        lineTo(ax + hwR * 0.7f + j(6) * hwR * 0.08f, ay + h * 0.66f)
        lineTo(rx, by)
        close()
    }
    drawPath(shape, body)
    val face = polyPath(
        ax + hwR * 0.02f, ay + h * 0.02f,
        ax + hwR * 1.4f, by + h * 0.05f,
        ax + hwR * 0.1f + j(7) * hwR * 0.1f, by + h * 0.05f,
        ax - hwL * 0.1f + j(8) * hwL * 0.06f, ay + h * 0.6f,
        ax - hwL * 0.02f, ay + h * 0.3f,
    )
    clipPath(shape) {
        drawPath(face, shade)
        if (cap > 0f) {
            val yf = ay + h * cap
            val tooth = h * 0.05f
            val x0 = lx - 20f
            val x1 = rx + 20f
            val w = x1 - x0
            val snowArea = Path().apply {
                moveTo(x0, ay - 40f)
                lineTo(x1, ay - 40f)
                lineTo(x1, yf)
                val n = 7
                for (i in n downTo 0) {
                    val px = x0 + w * (i / n.toFloat()) + (if (i in 1 until n) (hash01(i, seed + 11) - 0.5f) * w * 0.05f else 0f)
                    val py = yf + (if (i % 2 == 0) -tooth else tooth * 1.1f) * (0.7f + 0.6f * hash01(i, seed + 12))
                    lineTo(px, py)
                }
                close()
            }
            drawPath(snowArea, snow)
            clipPath(snowArea) { drawPath(face, snowShade) }
        }
    }
    if (outline > 0f) drawPath(shape, outlineColor, alpha = outline, style = Stroke(lw, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** A leafy pine, ink outlined with a shade half and a short trunk. */
internal fun DrawScope.lgPine(bx: Float, by: Float, w: Float, h: Float, color: Color, lw: Float, snow: Color? = null) {
    val tw = w * 0.14f
    drawRect(Color(0xFF6E4A33), Offset(bx - tw / 2f, by - h * 0.16f), androidx.compose.ui.geometry.Size(tw, h * 0.16f))
    drawRect(Ink.line, Offset(bx - tw / 2f, by - h * 0.16f), androidx.compose.ui.geometry.Size(tw, h * 0.16f), style = Stroke(lw * 0.6f))
    val by2 = by - h * 0.08f
    val hh = h * 0.92f
    val hw = w / 2f
    val body = Path().apply {
        moveTo(bx, by2 - hh)
        lineTo(bx + hw * 0.52f, by2 - hh * 0.6f)
        lineTo(bx + hw * 0.3f, by2 - hh * 0.6f)
        lineTo(bx + hw * 0.8f, by2 - hh * 0.28f)
        lineTo(bx + hw * 0.5f, by2 - hh * 0.28f)
        lineTo(bx + hw, by2)
        lineTo(bx - hw, by2)
        lineTo(bx - hw * 0.5f, by2 - hh * 0.28f)
        lineTo(bx - hw * 0.8f, by2 - hh * 0.28f)
        lineTo(bx - hw * 0.3f, by2 - hh * 0.6f)
        lineTo(bx - hw * 0.52f, by2 - hh * 0.6f)
        close()
    }
    val shadeP = Path().apply {
        moveTo(bx + hw * 0.02f, by2 - hh * 0.98f)
        lineTo(bx + hw * 0.52f, by2 - hh * 0.6f)
        lineTo(bx + hw * 0.3f, by2 - hh * 0.6f)
        lineTo(bx + hw * 0.8f, by2 - hh * 0.28f)
        lineTo(bx + hw * 0.5f, by2 - hh * 0.28f)
        lineTo(bx + hw, by2)
        lineTo(bx + hw * 0.15f, by2)
        close()
    }
    drawPath(body, color)
    drawPath(shadeP, color.shadow())
    drawPath(body, Ink.line, style = Stroke(lw, join = StrokeJoin.Round))
    if (snow != null) {
        val cap = Path().apply {
            moveTo(bx, by2 - hh * 1.005f)
            lineTo(bx + hw * 0.38f, by2 - hh * 0.71f)
            quadraticTo(bx + hw * 0.15f, by2 - hh * 0.65f, bx, by2 - hh * 0.7f)
            quadraticTo(bx - hw * 0.15f, by2 - hh * 0.65f, bx - hw * 0.38f, by2 - hh * 0.71f)
            close()
        }
        drawPath(cap, snow)
    }
}

/** A flat soft glow: [color] fading to nothing over [radius]. */
internal fun DrawScope.lgGlow(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(
        safeRadialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.45f), color.copy(alpha = 0f)), center = center, radius = radius),
        radius, center,
    )
}

/** A little blob of sparkle. */
internal fun DrawScope.lgSparkle(c: Offset, r: Float, color: Color, alpha: Float = 1f) {
    twinkle(c, r, color, alpha)
}

/** A rainbow arch of seven bands over [center], [outer] radius, drawn as arcs of stroke. */
internal fun DrawScope.lgRainbow(center: Offset, outer: Float, band: Float, alpha: Float, startAngle: Float = 180f, sweep: Float = 180f, inkWidth: Float = 0f) {
    if (inkWidth > 0f) {
        val rr = outer - band * 3f
        drawArc(
            Ink.line, startAngle, sweep, false, Offset(center.x - rr, center.y - rr), androidx.compose.ui.geometry.Size(rr * 2f, rr * 2f),
            alpha = alpha, style = Stroke(width = band * L.rainbow.size + inkWidth * 2f, cap = StrokeCap.Butt),
        )
    }
    for (i in L.rainbow.indices) {
        val rr = outer - band * (i + 0.5f)
        drawArc(
            L.rainbow[i], startAngle, sweep, false, Offset(center.x - rr, center.y - rr), androidx.compose.ui.geometry.Size(rr * 2f, rr * 2f),
            alpha = alpha, style = Stroke(width = band * 1.04f, cap = StrokeCap.Butt),
        )
    }
}

/** Round puffs of mist/foam. */
internal fun DrawScope.lgPuffs(points: List<Triple<Float, Float, Float>>, color: Color, alpha: Float) {
    for ((x, y, r) in points) drawCircle(color, r, Offset(x, y), alpha = alpha)
}

internal fun polar(c: Offset, r: Float, deg: Float): Offset {
    val a = Math.toRadians(deg.toDouble())
    return Offset(c.x + (r * cos(a)).toFloat(), c.y + (r * sin(a)).toFloat())
}

internal fun absf(v: Float) = abs(v)

internal fun DrawScope.lgInkCircle(c: Offset, r: Float, color: Color, lw: Float, shade: Boolean = true) {
    inkedCircle(c, r, color, logoPen(lw), shade)
}

internal fun DrawScope.lgInked(path: Path, color: Color, lw: Float, shade: Boolean = true) {
    inked(path, color, logoPen(lw), shade)
}

internal fun Brush.Companion.sky(top: Float, bottom: Float, vararg stops: Pair<Float, Color>): Brush =
    verticalGradient(*stops, startY = top, endY = bottom)

internal fun Color.dk(f: Float): Color = darken(f)
internal fun Color.lt(f: Float): Color = lighten(f)
