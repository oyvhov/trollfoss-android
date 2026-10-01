package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/*
 * Small shared pieces for the cellar's furniture: the palette, the soft shadow, a sock, a gear, a lightning bolt
 * and the like. Everything is drawn with the origin at the bottom centre of the fixture's front face, up is
 * negative, [u] pixels per scene unit, depth runs up and to the right (see FixtureArt.kt for the 3D kit).
 * Private helpers of the cellar's art files start with `ce`.
 */

internal object CeC {
    val enamel = Color(0xFFF4F6F8)
    val enamelShade = Color(0xFFD5DCE6)
    val mint = Color(0xFFB4E6D6)
    val teal = Color(0xFF2EC4B6)
    val navy = Color(0xFF2B3A67)
    val iron = Color(0xFF3A3844)
    val ironLight = Color(0xFF5A5F6E)
    val steel = Color(0xFFBAC4D4)
    val copper = Color(0xFFC9884A)
    val copperDark = Color(0xFF9A6234)
    val brass = Color(0xFFE0B04A)
    val gold = Color(0xFFFFD447)
    val orange = Color(0xFFFF8A2E)
    val pink = Color(0xFFFF5FA8)
    val purple = Color(0xFFB9A2F0)
    val purpleDark = Color(0xFF8B6FD1)
    val yellow = Color(0xFFFFC83D)
    val water = Color(0xFF7CCBFF)
    val waterDeep = Color(0xFF2F8FD6)
    val glass = Color(0xFFBFE6F8)
    val steam = Color(0xFFF4F8FF)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)
    val woodLight = Color(0xFFE3B27A)
    val stone = Color(0xFFC9BBA8)
    val green = Color(0xFF3BC46B)
    val neon = listOf(Color(0xFFFF4DA6), Color(0xFF2FD6C8), Color(0xFFFFC83D), Color(0xFF8B5CF6), Color(0xFF4D96FF))

    /** The six patterns of the socks: body colour and the contrasting heel, toe and cuff colour. */
    val sockBody = listOf(Color(0xFFFF6F91), Color(0xFF5AA9E6), Color(0xFF6BCB77), Color(0xFFB983FF), Color(0xFFFFC83D), Color(0xFFFF9F43))
    val sockTrim = listOf(Color(0xFFFFF4F0), Color(0xFFFFE08A), Color(0xFFFFF4C2), Color(0xFFFFE08A), Color(0xFF7CCBFF), Color(0xFF2B3A67))
}

/** The soft shadow of a floor piece, [w] units wide and [d] deep. */
internal fun DrawScope.ceShadow(u: Float, w: Float, d: Float, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

/** A smooth 0..1..0 pulse with period [period] seconds. */
internal fun cePulse(t: Float, period: Float, phase: Float = 0f): Float = 0.5f + 0.5f * sin((t / period + phase) * 2f * PI.toFloat())

/** A saw tooth 0..1 repeating every [period] seconds. */
internal fun ceFrac(t: Float, period: Float, phase: Float = 0f): Float {
    val v = t / period + phase
    return v - floor(v)
}

/** The fur outline of a blob: [n] soft spikes round an ellipse. */
internal fun ceFurPath(cx: Float, cy: Float, rx: Float, ry: Float, n: Int, jag: Float, seed: Int, sway: Float = 0f): Path = Path().apply {
    val pts = n * 2
    fun px(i: Int): Float {
        val a = i * 2f * PI.toFloat() / pts
        val r = if (i % 2 == 0) 1f + jag * (0.6f + 0.4f * hash01(i, seed)) else 1f - jag * 0.2f
        return cx + cos(a) * rx * r + (if (i % 2 == 0) sway * sin(a) else 0f)
    }
    fun py(i: Int): Float {
        val a = i * 2f * PI.toFloat() / pts
        val r = if (i % 2 == 0) 1f + jag * (0.6f + 0.4f * hash01(i, seed)) else 1f - jag * 0.2f
        return cy + sin(a) * ry * r
    }
    moveTo(px(0), py(0))
    for (i in 1..pts) {
        val j = i % pts
        val mx = (px(i - 1) + px(j)) / 2f
        val my = (py(i - 1) + py(j)) / 2f
        quadraticTo(px(i - 1), py(i - 1), mx, my)
    }
    close()
}

/**
 * A sock of pattern [variant] standing on (0, 0), [w] wide and [h] tall, its leg at the left and its foot
 * pointing right. The six patterns: stripes, dots, heel and toe, argyle, hearts and a zig-zag.
 */
internal fun DrawScope.ceSock(w: Float, h: Float, variant: Int, pen: Pen) {
    val v = variant.mod(6)
    val body = CeC.sockBody[v]
    val trim = CeC.sockTrim[v]
    fun p(x: Float, y: Float) = Offset(x * w, y * h)
    val sock = Path().apply {
        moveTo(p(-0.4f, -1f).x, p(-0.4f, -1f).y)
        lineTo(p(0.12f, -1f).x, p(0.12f, -1f).y)
        lineTo(p(0.12f, -0.42f).x, p(0.12f, -0.42f).y)
        quadraticTo(p(0.52f, -0.44f).x, p(0.52f, -0.44f).y, p(0.5f, -0.2f).x, p(0.5f, -0.2f).y)
        quadraticTo(p(0.48f, 0f).x, p(0.48f, 0f).y, p(0.2f, 0f).x, p(0.2f, 0f).y)
        lineTo(p(-0.18f, 0f).x, p(-0.18f, 0f).y)
        quadraticTo(p(-0.46f, 0f).x, p(-0.46f, 0f).y, p(-0.4f, -0.34f).x, p(-0.4f, -0.34f).y)
        close()
    }
    inked(sock, body, pen)
    clipPath(sock) {
        // The cuff, then the pattern.
        drawRect(trim, p(-0.5f, -1f), Size(w * 1f, h * 0.17f))
        drawLine(Ink.line, p(-0.4f, -0.83f), p(0.12f, -0.83f), strokeWidth = pen.lw * 0.5f)
        when (v) {
            0 -> for (k in 0 until 3) drawRect(trim, p(-0.5f, -0.72f + k * 0.17f), Size(w * 1f, h * 0.07f))
            1 -> for (k in 0 until 6) drawCircle(trim, w * 0.04f, p(-0.28f + (k % 3) * 0.2f, -0.68f + (k / 3) * 0.2f))
            2 -> {
                drawOval(trim, p(-0.5f, -0.4f), Size(w * 0.55f, h * 0.45f))
                drawOval(trim, p(0.25f, -0.42f), Size(w * 0.4f, h * 0.45f))
            }
            3 -> for (k in 0 until 4) {
                val c = p(-0.14f + (k % 2) * 0.2f, -0.68f + (k / 2) * 0.22f)
                val d = w * 0.1f
                val dia = Path().apply { moveTo(c.x, c.y - d); lineTo(c.x + d, c.y); lineTo(c.x, c.y + d); lineTo(c.x - d, c.y); close() }
                drawPath(dia, trim)
            }
            4 -> for (k in 0 until 2) drawPath(fxHeart(p(-0.2f + k * 0.22f, -0.66f + k * 0.12f).x, p(-0.2f + k * 0.22f, -0.66f + k * 0.12f).y, w * 0.08f), trim)
            else -> {
                val z = Path().apply {
                    moveTo(p(-0.4f, -0.66f).x, p(-0.4f, -0.66f).y)
                    for (k in 1..5) lineTo(p(-0.4f + k * 0.104f, if (k % 2 == 0) -0.66f else -0.5f).x, p(-0.4f + k * 0.104f, if (k % 2 == 0) -0.66f else -0.5f).y)
                }
                drawPath(z, trim, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
            }
        }
        // Heel and toe.
        if (v != 2) {
            drawOval(trim, p(-0.5f, -0.3f), Size(w * 0.4f, h * 0.3f))
            drawOval(trim, p(0.26f, -0.32f), Size(w * 0.3f, h * 0.34f))
        }
    }
    drawPath(sock, Ink.line, style = pen.stroke)
}

/** A gear with [teeth] teeth, centred on [c]. */
internal fun DrawScope.ceGear(c: Offset, r: Float, teeth: Int, color: Color, pen: Pen, turn: Float = 0f) {
    val path = Path()
    val n = teeth * 2
    for (i in 0 until n) {
        val a = turn + i * 2f * PI.toFloat() / n
        val rr = if (i % 2 == 0) r else r * 0.8f
        val a2 = a + PI.toFloat() / n * 0.0f
        val x = c.x + cos(a2) * rr
        val y = c.y + sin(a2) * rr
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    inked(path, color, pen, shade = false)
    drawCircle(Ink.line, r * 0.3f, c, style = pen.thin)
}

/** A zig-zag lightning bolt, as a friendly yellow shape. */
internal fun ceBolt(c: Offset, s: Float): Path = Path().apply {
    moveTo(c.x + s * 0.1f, c.y - s)
    lineTo(c.x - s * 0.5f, c.y + s * 0.1f)
    lineTo(c.x - s * 0.05f, c.y + s * 0.1f)
    lineTo(c.x - s * 0.15f, c.y + s)
    lineTo(c.x + s * 0.5f, c.y - s * 0.2f)
    lineTo(c.x + s * 0.05f, c.y - s * 0.2f)
    close()
}

/** A hanging round light: the glow, drawn only when [lit]. */
internal fun DrawScope.ceHalo(c: Offset, r: Float, color: Color, alpha: Float) {
    fxGlow(c, r, color, alpha)
}

/** Round drops falling in a line from [from] down to [toY], moving with time, as short bright dashes. */
internal fun DrawScope.ceRain(x: Float, fromY: Float, toY: Float, width: Float, t: Float, n: Int, color: Color, pen: Pen, seed: Int = 0) {
    for (i in 0 until n) {
        val ph = ceFrac(t * 1.3f, 1f, hash01(i, 900 + seed))
        val yy = fromY + (toY - fromY) * ph
        val xx = x + (hash01(i, 901 + seed) - 0.5f) * width
        drawLine(color, Offset(xx, yy), Offset(xx, yy + (toY - fromY) * 0.06f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    }
}

internal fun absf(v: Float) = abs(v)

internal inline fun DrawScope.ceRotate(deg: Float, pivot: Offset, block: DrawScope.() -> Unit) = rotate(deg, pivot) { block() }

internal fun ceRect(l: Float, t: Float, r: Float, b: Float, u: Float) = Rect(l * u, t * u, r * u, b * u)
