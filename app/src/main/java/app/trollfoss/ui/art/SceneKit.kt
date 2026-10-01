package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Weather
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The shared kit for place backgrounds and the map: the camera, skies, weather, landscape layers and
 * water. Everything here is drawn in screen pixels; [Stage] turns scene units into pixels.
 *
 * Paths built from several shapes are always wound the same way (clockwise on screen: top-left, top-right,
 * bottom-right, bottom-left), so overlapping parts fill as one shape.
 */

/** Colours shared by the places and the map. */
internal object Pal {
    val falun = Color(0xFFB8342B)
    val gran = Color(0xFF1F7048)
    val granLight = Color(0xFF2E8B57)
    val grass = Color(0xFF6FAE5A)
    val fjord = Color(0xFF2F6FB8)
    val fjordLight = Color(0xFF5AA9E6)
    val birch = Color(0xFFF2EEE6)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)
    val woodLight = Color(0xFFE3B27A)
    val snow = Color(0xFFF4F8FF)
    val snowShade = Color(0xFFC9D6EE)
    val auroraGreen = Color(0xFF7CFFB2)
    val auroraViolet = Color(0xFFD77BFF)
    val sun = Color(0xFFFFC83D)
    val night = Color(0xFF231E5C)
    val rainbow = listOf(
        Color(0xFFFF5A5F), Color(0xFFFF9F43), Color(0xFFFFD93D), Color(0xFF6BCB77),
        Color(0xFF4D96FF), Color(0xFF6A5ACD), Color(0xFFB983FF),
    )
}

/**
 * The camera for one frame. [cam] is the left edge of the view in scene units, [u] pixels per unit,
 * [w] and [h] the screen in pixels and [width] the place's width in units.
 */
internal class Stage(val cam: Float, val u: Float, val w: Float, val h: Float, val width: Float) {
    /** The visible upper edge after the engine anchors y = 1 at the bottom of the screen. */
    val backgroundTop: Float = min(SKY_TOP * u, u - h)
    /** Screen width in scene units. */
    val vw: Float = w / u
    val camMax: Float = max(0f, width - vw)
    val left: Float get() = cam - 0.05f
    val right: Float get() = cam + vw + 0.05f

    fun x(sx: Float): Float = (sx - cam) * u
    fun y(sy: Float): Float = sy * u
    fun o(sx: Float, sy: Float): Offset = Offset((sx - cam) * u, sy * u)
    fun rect(x1: Float, y1: Float, x2: Float, y2: Float): Rect = Rect(x(x1), y1 * u, x(x2), y2 * u)

    /** A point on a far layer that moves [p] times as fast as the camera. */
    fun px(lx: Float, p: Float): Float = (lx - cam * p) * u

    /** How much of a far layer can ever be seen, in its own units. */
    fun span(p: Float): Float = camMax * p + vw

    /** A landmark at fraction [f] of what a far layer ever shows, so it is seen on any screen shape. */
    fun fx(f: Float, p: Float): Float = px(f * span(p), p)

    fun sees(x1: Float, x2: Float): Boolean = x2 > left && x1 < right
}

/** Remembers geometry built in scene pixels until the scale changes; draw it with [inScene]. */
internal class Memo<T : Any>(private val make: (Float) -> T) {
    private var key = Float.NaN
    private var value: T? = null

    fun of(u: Float): T {
        val v = value
        if (v != null && key == u) return v
        return make(u).also {
            value = it
            key = u
        }
    }
}

/** Draws in scene pixels: (x * u, y * u) lands where scene point (x, y) is. */
internal inline fun DrawScope.inScene(st: Stage, block: DrawScope.() -> Unit) = translate(-st.cam * st.u, 0f, block)

/** A steady random number from 0 to 1 for [i], so scenery is the same every frame. */
internal fun hash01(i: Int, salt: Int = 0): Float {
    var x = i * 0x27D4EB2D + salt * 0x165667B1 + 0x5BD1E995
    x = x xor (x ushr 15)
    x *= 0x2C1B3C6D
    x = x xor (x ushr 12)
    x *= 0x297A2D39
    x = x xor (x ushr 15)
    return (x and 0xFFFFFF) / 16777216f
}

internal fun wrap(v: Float, m: Float): Float {
    val r = v % m
    return if (r < 0f) r + m else r
}

/** Smooth 0..1 ramp. */
internal fun ramp(v: Float): Float {
    val c = v.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}

internal fun mix(a: Float, b: Float, f: Float): Float = a + (b - a) * f

/** Blue-violet night shading for scenery; far layers take more of it than near ones. */
internal fun Color.atNight(night: Float, k: Float = 0.6f): Color = if (night <= 0f) this else lerp(this, Pal.night, night * k)

internal fun overcast(pen: Pen): Float = when (pen.weather) {
    Weather.SUN -> 0f
    Weather.RAIN -> 0.9f
    Weather.SNOW -> 0.75f
}

/** A smooth line through the points (quadratic curves between midpoints). */
internal fun Path.smoothRun(xs: FloatArray, ys: FloatArray, n: Int, start: Boolean, reverse: Boolean = false) {
    fun px(i: Int) = if (reverse) xs[n - 1 - i] else xs[i]
    fun py(i: Int) = if (reverse) ys[n - 1 - i] else ys[i]
    if (start) moveTo(px(0), py(0)) else lineTo(px(0), py(0))
    for (i in 1 until n - 1) quadraticTo(px(i), py(i), (px(i) + px(i + 1)) / 2f, (py(i) + py(i + 1)) / 2f)
    lineTo(px(n - 1), py(n - 1))
}

/** A closed polygon through x, y pairs. */
internal fun Path.poly(vararg p: Float) {
    moveTo(p[0], p[1])
    var i = 2
    while (i < p.size) {
        lineTo(p[i], p[i + 1])
        i += 2
    }
    close()
}

// ---------------------------------------------------------------------------------------------- sky

internal enum class Mood { SUMMER, FOREST, WINTER }

internal fun DrawScope.drawSky(st: Stage, pen: Pen, mood: Mood, horizon: Float) {
    val oc = overcast(pen)
    val dayTop: Color
    val dayLow: Color
    when (mood) {
        Mood.SUMMER -> { dayTop = Color(0xFF45A6EE); dayLow = Color(0xFFD6F2FF) }
        Mood.FOREST -> { dayTop = Color(0xFF58B0EC); dayLow = Color(0xFFE2F5EC) }
        Mood.WINTER -> { dayTop = Color(0xFF74B8E8); dayLow = Color(0xFFFFE0BD) }
    }
    val rain = pen.weather == Weather.RAIN
    val greyTop = if (rain) Color(0xFF7D8AA2) else Color(0xFF9CA8BF)
    val greyLow = if (rain) Color(0xFFC6CEDB) else Color(0xFFE6EAF2)
    val top = lerp(lerp(dayTop, greyTop, oc), Color(0xFF100D35), pen.night)
    val mid = lerp(lerp(lerp(dayTop, dayLow, 0.5f), lerp(greyTop, greyLow, 0.5f), oc), Color(0xFF261E66), pen.night)
    val low = lerp(lerp(dayLow, greyLow, oc), Color(0xFF55409A), pen.night)
    drawRect(
        Brush.verticalGradient(0f to top, 0.55f to mid, 1f to low, startY = SKY_TOP * st.u, endY = horizon * st.u),
        Offset(0f, st.backgroundTop),
        Size(st.w, st.h - st.backgroundTop),
    )
}

internal fun DrawScope.drawSun(c: Offset, r: Float, pen: Pen, vis: Float, rays: Boolean = true, color: Color = Color(0xFFFFD447)) {
    if (vis <= 0.01f) return
    drawCircle(color, r * 2.6f, c, alpha = 0.13f * vis)
    drawCircle(color, r * 1.7f, c, alpha = 0.2f * vis)
    if (rays) {
        val pts = ArrayList<Offset>(24)
        val spin = pen.t * 0.12f
        for (i in 0 until 12) {
            val a = spin + i * 0.5235988f
            val l = r * (1.62f + 0.14f * sin(pen.t * 1.8f + i * 1.7f))
            pts.add(Offset(c.x + cos(a) * r * 1.3f, c.y + sin(a) * r * 1.3f))
            pts.add(Offset(c.x + cos(a) * l, c.y + sin(a) * l))
        }
        drawPoints(pts, PointMode.Lines, color, strokeWidth = r * 0.16f, cap = StrokeCap.Round, alpha = vis)
    }
    drawCircle(color, r, c, alpha = vis)
    drawCircle(color.lighten(0.5f), r * 0.6f, Offset(c.x - r * 0.15f, c.y - r * 0.15f), alpha = vis)
    drawCircle(Ink.line, r, c, alpha = 0.45f * vis, style = pen.stroke)
}

/** A crescent moon with a soft halo. */
internal fun DrawScope.drawMoon(c: Offset, r: Float, pen: Pen, vis: Float) {
    if (vis <= 0.01f) return
    val moon = Color(0xFFFFF0BF)
    drawCircle(moon, r * 3.4f, c, alpha = 0.05f * vis)
    drawCircle(moon, r * 2f, c, alpha = 0.09f * vis)
    val biteCenter = Offset(c.x + r * 0.5f, c.y - r * 0.3f)
    val bite = Path().apply { addOval(Rect(biteCenter, r * 0.86f)) }
    clipPath(bite, ClipOp.Difference) {
        drawCircle(moon, r, c, alpha = vis)
        drawCircle(Color(0xFFF1DB94), r * 0.16f, Offset(c.x - r * 0.45f, c.y + r * 0.3f), alpha = vis)
        drawCircle(Ink.line, r, c, alpha = 0.45f * vis, style = pen.stroke)
    }
    val disc = Path().apply { addOval(Rect(c, r)) }
    clipPath(disc) { drawCircle(Ink.line, r * 0.86f, biteCenter, alpha = 0.45f * vis, style = pen.stroke) }
}

/** Stars that fade in with the night and twinkle in three groups. [maxY] is how far down they reach. */
internal fun DrawScope.drawStars(st: Stage, pen: Pen, maxY: Float, count: Int = 60) {
    val vis = ramp((pen.night - 0.2f) / 0.55f) * (1f - overcast(pen) * 0.8f)
    if (vis <= 0.01f) return
    val span = size.width + 40f
    val shift = st.cam * st.u * 0.03f
    val star = Color(0xFFFFF7DA)
    for (g in 0 until 3) {
        val pts = ArrayList<Offset>(count / 3 + 1)
        var i = g
        while (i < count) {
            pts.add(Offset(wrap(hash01(i, 11) * span - shift, span) - 20f, mix(SKY_TOP, maxY, hash01(i, 12)) * st.u))
            i += 3
        }
        val a = vis * (0.6f + 0.4f * sin(pen.t * (1.1f + g * 0.45f) + g * 2.1f))
        drawPoints(pts, PointMode.Points, star, strokeWidth = st.u * (0.0045f + g * 0.0012f), cap = StrokeCap.Round, alpha = a)
    }
    for (i in 0 until 7) {
        val p = Offset(wrap(hash01(i, 21) * span - shift, span) - 20f, mix(SKY_TOP + 0.04f, maxY - 0.06f, hash01(i, 22)) * st.u)
        twinkle(p, st.u * 0.017f, star, vis * (0.35f + 0.65f * (0.5f + 0.5f * sin(pen.t * 1.7f + i * 1.9f))))
    }
}

/** A fluffy cloud made of round puffs over a flat base, one shape. */
internal fun cloudPath(cx: Float, cy: Float, s: Float): Path = Path().apply {
    addOval(Rect(cx - 1.25f * s, cy - 0.45f * s, cx - 0.35f * s, cy + 0.3f * s))
    addOval(Rect(cx - 0.75f * s, cy - 0.85f * s, cx + 0.35f * s, cy + 0.25f * s))
    addOval(Rect(cx - 0.05f * s, cy - 0.65f * s, cx + 0.95f * s, cy + 0.3f * s))
    addOval(Rect(cx + 0.55f * s, cy - 0.3f * s, cx + 1.35f * s, cy + 0.3f * s))
    addOval(Rect(cx - 1.1f * s, cy - 0.05f * s, cx + 1.25f * s, cy + 0.32f * s))
}

/** Clouds drifting with time between [top] and [bottom]; grey and more of them in rain or snow. */
internal fun DrawScope.drawClouds(st: Stage, pen: Pen, top: Float, bottom: Float, count: Int, scale: Float, salt: Int = 0) {
    val oc = overcast(pen) > 0f
    val n = if (oc) count + 3 else count
    val body = lerp(if (oc) Color(0xFFB7C0D0) else Color.White, Color(0xFF4A4488), pen.night * 0.8f)
    val shade = lerp(if (oc) Color(0xFF8C97AC) else Color(0xFFD6E6F6), Color(0xFF2E2A66), pen.night * 0.8f)
    val span = size.width + 0.9f * st.u
    for (i in 0 until n) {
        val s = st.u * scale * (0.7f + 0.6f * hash01(i, 31 + salt)) * if (oc) 1.35f else 1f
        val speed = st.u * (0.005f + 0.01f * hash01(i, 32 + salt))
        val x = wrap(hash01(i, 33 + salt) * span + pen.t * speed - st.cam * st.u * 0.08f, span) - 0.45f * st.u
        val y = (top + (bottom - top) * hash01(i, 34 + salt)) * st.u
        val p = cloudPath(x, y, s)
        translate(s * 0.07f, s * 0.13f) { drawPath(p, shade) }
        drawPath(p, body)
    }
}

/** Nordlys: slow waving curtains of green and violet light with bright lower hems and faint rays. */
internal fun DrawScope.drawAurora(st: Stage, pen: Pen, strength: Float, top: Float, depth: Float) {
    var k = strength * ramp((pen.night - 0.3f) / 0.55f)
    if (pen.weather != Weather.SUN) k *= 0.4f
    if (k <= 0.01f) return
    val u = st.u
    val w = size.width
    val t = pen.t
    val shift = st.cam * 0.05f
    val n = 14
    val xs = FloatArray(n)
    val tops = FloatArray(n)
    val bots = FloatArray(n)
    for (c in 0 until 3) {
        val col = when (c) {
            0 -> Pal.auroraGreen
            1 -> Pal.auroraViolet
            else -> Color(0xFF63F5D9)
        }
        val baseY = top + depth * (0.08f + 0.26f * c)
        var minY = Float.MAX_VALUE
        var maxY = 0f
        for (i in 0 until n) {
            val sx = -0.1f * w + i * (1.2f * w / (n - 1))
            val q = sx / u + shift + c * 0.7f
            val ty = baseY + 0.045f * sin(q * 2.3f + t * 0.33f + c * 1.9f) + 0.02f * sin(q * 5.1f - t * 0.52f + c)
            val hh = depth * (0.42f + 0.2f * sin(q * 3.3f + t * 0.41f + c * 2.4f))
            xs[i] = sx
            tops[i] = ty * u
            bots[i] = (ty + hh) * u
            minY = min(minY, tops[i])
            maxY = max(maxY, bots[i])
        }
        val a = k * if (c == 1) 0.8f else 1f
        val band = Path().apply {
            smoothRun(xs, tops, n, start = true)
            smoothRun(xs, bots, n, start = false, reverse = true)
            close()
        }
        drawPath(
            band,
            Brush.verticalGradient(
                0f to col.copy(alpha = 0f),
                0.5f to col.copy(alpha = 0.2f * a),
                0.88f to col.copy(alpha = 0.5f * a),
                1f to col.copy(alpha = 0.3f * a),
                startY = minY,
                endY = maxY,
            ),
        )
        drawPath(Path().apply { smoothRun(xs, bots, n, start = true) }, col, alpha = 0.55f * a, style = Stroke(width = u * 0.004f, cap = StrokeCap.Round))
        val rays = ArrayList<Offset>(36)
        for (j in 0 until 18) {
            val f = (j + 0.5f) / 18f * (n - 1)
            val i0 = f.toInt().coerceAtMost(n - 2)
            val fr = f - i0
            val rx = mix(xs[i0], xs[i0 + 1], fr) + sin(t * 0.7f + j * 1.3f + c) * u * 0.012f
            val rt = mix(tops[i0], tops[i0 + 1], fr)
            val rb = mix(bots[i0], bots[i0 + 1], fr)
            rays.add(Offset(rx, mix(rt, rb, 0.3f)))
            rays.add(Offset(rx, rb))
        }
        drawPoints(rays, PointMode.Lines, col, strokeWidth = u * 0.007f, cap = StrokeCap.Round, alpha = 0.2f * a * (0.6f + 0.4f * sin(t * 1.3f + c)))
    }
}

internal fun DrawScope.drawRainbow(c: Offset, r: Float, band: Float, alpha: Float) {
    if (alpha <= 0.01f) return
    for (i in Pal.rainbow.indices) {
        val rr = r - i * band
        drawArc(
            Pal.rainbow[i], 180f, 180f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f),
            alpha = alpha, style = Stroke(width = band * 1.08f),
        )
    }
}

/** Seagulls gliding across the sky by day, flapping now and then. */
internal fun DrawScope.drawGulls(st: Stage, pen: Pen, count: Int, top: Float, bottom: Float, salt: Int = 0) {
    val vis = 1f - ramp(pen.night * 1.6f)
    if (vis <= 0.01f) return
    val span = size.width + 0.6f * st.u
    val stroke = Stroke(width = pen.lw * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    for (i in 0 until count) {
        val speed = st.u * (0.03f + 0.02f * hash01(i, 41 + salt))
        val x = wrap(hash01(i, 42 + salt) * span + pen.t * speed - st.cam * st.u * 0.3f, span) - 0.3f * st.u
        val y = (top + (bottom - top) * hash01(i, 43 + salt)) * st.u + sin(pen.t * 0.8f + i * 2f) * st.u * 0.015f
        val s = st.u * (0.02f + 0.01f * hash01(i, 44 + salt))
        val flap = sin(pen.t * (3.5f + i) + i).let { if (it > 0.3f) it else 0.3f }
        drawGull(Offset(x, y), s, flap, stroke, vis)
    }
}

internal fun DrawScope.drawGull(c: Offset, s: Float, flap: Float, stroke: Stroke, alpha: Float) {
    val tip = s * (0.05f + 0.25f * flap)
    val p = Path().apply {
        moveTo(c.x - s, c.y - tip)
        quadraticTo(c.x - s * 0.45f, c.y - s * (0.2f + 0.45f * flap), c.x, c.y)
        quadraticTo(c.x + s * 0.45f, c.y - s * (0.2f + 0.45f * flap), c.x + s, c.y - tip)
    }
    drawPath(p, Ink.line, alpha = 0.85f * alpha, style = stroke)
}

// ---------------------------------------------------------------------------------------- landscape

/** A sharp mountain range at parallax [p]; each peak has a shaded side and optionally a snow cap. */
internal fun DrawScope.drawPeaks(
    st: Stage, p: Float, base: Float, spacing: Float, minH: Float, maxH: Float,
    body: Color, shade: Color, snow: Color?, snowShade: Color?, capFrac: Float, salt: Int,
) {
    val u = st.u
    val l0 = st.cam * p - spacing * 1.6f
    val l1 = st.cam * p + st.vw + spacing * 1.6f
    val bodyP = Path()
    val shadeP = Path()
    val capP = Path()
    val capShadeP = Path()
    val b = (base + 0.012f) * u
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt() + 1) {
        val cx = st.px(i * spacing + (hash01(i, salt) - 0.5f) * spacing * 0.5f, p)
        val hgt = (minH + (maxH - minH) * hash01(i, salt + 1)) * u
        val hw = spacing * (0.62f + 0.32f * hash01(i, salt + 2)) * u
        val ax = cx + (hash01(i, salt + 3) - 0.5f) * hw * 0.3f
        val ay = b - hgt
        val lx = cx - hw
        val rx = cx + hw
        bodyP.moveTo(lx, b)
        bodyP.lineTo(ax - hw * 0.05f, ay + hgt * 0.025f)
        bodyP.quadraticTo(ax, ay - hgt * 0.01f, ax + hw * 0.05f, ay + hgt * 0.025f)
        bodyP.lineTo(rx, b)
        bodyP.close()
        shadeP.poly(ax + hw * 0.03f, ay + hgt * 0.02f, rx, b, ax + hw * 0.22f, b, ax - hw * 0.04f, ay + hgt * 0.5f)
        if (snow != null && capFrac > 0f) {
            val f = capFrac
            val yb = ay + hgt * f
            val lxF = mix(ax, lx, f)
            val rxF = mix(ax, rx, f)
            val tooth = hgt * 0.07f
            capP.moveTo(ax, ay - hgt * 0.005f)
            capP.lineTo(rxF, yb)
            capP.lineTo(mix(rxF, lxF, 0.25f), yb - tooth)
            capP.lineTo(mix(rxF, lxF, 0.45f), yb + tooth * 0.7f)
            capP.lineTo(mix(rxF, lxF, 0.65f), yb - tooth * 0.8f)
            capP.lineTo(mix(rxF, lxF, 0.82f), yb + tooth * 0.5f)
            capP.lineTo(lxF, yb)
            capP.close()
            if (snowShade != null) {
                capShadeP.moveTo(ax + hw * 0.02f, ay + hgt * 0.01f)
                capShadeP.lineTo(rxF, yb)
                capShadeP.lineTo(mix(rxF, lxF, 0.25f), yb - tooth)
                capShadeP.lineTo(mix(ax + hw * 0.03f, ax - hw * 0.04f, 0.9f), mix(ay, ay + hgt * 0.5f, f * 0.95f))
                capShadeP.close()
            }
        }
    }
    drawPath(bodyP, body)
    drawPath(shadeP, shade)
    if (snow != null) drawPath(capP, snow)
    if (snowShade != null) drawPath(capShadeP, snowShade)
}

internal fun ridgeY(lx: Float, base: Float, amp: Float, salt: Int): Float =
    base - amp * (0.55f + 0.3f * sin(lx * 1.9f + salt * 1.3f) + 0.15f * sin(lx * 4.7f + salt * 2.9f))

/** A rolling hill line across the screen for a layer at parallax [p], filled down past the bottom. */
internal fun ridgePath(st: Stage, p: Float, base: Float, amp: Float, salt: Int, closed: Boolean = true): Path {
    val n = 20
    val xs = FloatArray(n)
    val ys = FloatArray(n)
    val x0 = -0.05f * st.w
    val step = st.w * 1.1f / (n - 1)
    for (i in 0 until n) {
        val sx = x0 + i * step
        xs[i] = sx
        ys[i] = ridgeY(sx / st.u + st.cam * p, base, amp, salt) * st.u
    }
    return Path().apply {
        smoothRun(xs, ys, n, start = true)
        if (closed) {
            lineTo(xs[n - 1], st.h + 8f)
            lineTo(xs[0], st.h + 8f)
            close()
        }
    }
}

/** A pine silhouette: three tiers, bottom centre at ([cx], [by]). */
internal fun Path.addPine(cx: Float, by: Float, w: Float, h: Float) {
    val hw = w / 2f
    moveTo(cx, by - h)
    lineTo(cx + hw * 0.52f, by - h * 0.6f)
    lineTo(cx + hw * 0.3f, by - h * 0.6f)
    lineTo(cx + hw * 0.8f, by - h * 0.28f)
    lineTo(cx + hw * 0.5f, by - h * 0.28f)
    lineTo(cx + hw, by)
    lineTo(cx - hw, by)
    lineTo(cx - hw * 0.5f, by - h * 0.28f)
    lineTo(cx - hw * 0.8f, by - h * 0.28f)
    lineTo(cx - hw * 0.3f, by - h * 0.6f)
    lineTo(cx - hw * 0.52f, by - h * 0.6f)
    close()
}

/** The shadow half of [addPine]. */
internal fun Path.addPineShade(cx: Float, by: Float, w: Float, h: Float) {
    val hw = w / 2f
    moveTo(cx + hw * 0.02f, by - h * 0.98f)
    lineTo(cx + hw * 0.52f, by - h * 0.6f)
    lineTo(cx + hw * 0.3f, by - h * 0.6f)
    lineTo(cx + hw * 0.8f, by - h * 0.28f)
    lineTo(cx + hw * 0.5f, by - h * 0.28f)
    lineTo(cx + hw, by)
    lineTo(cx + hw * 0.15f, by)
    close()
}

/**
 * A hill at parallax [p] with pines standing on it, flat colours (far layers have no outlines). The hill
 * takes the trees' colour unless [ground] is given (a snowy hill with dark trees).
 */
internal fun DrawScope.drawForestRow(
    st: Stage, p: Float, base: Float, amp: Float, salt: Int, spacing: Float, minH: Float, maxH: Float,
    body: Color, shade: Color?, skip: Float = 0f, ground: Color? = null,
) {
    val u = st.u
    val ridge = ridgePath(st, p, base, amp, salt)
    val trees = if (ground == null) ridge else Path()
    val shadeP = if (shade != null) Path() else null
    val l0 = st.cam * p - 0.2f
    val l1 = st.cam * p + st.vw + 0.2f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt() + 1) {
        if (hash01(i, salt + 7) < skip) continue
        val lx = i * spacing + (hash01(i, salt + 8) - 0.5f) * spacing * 0.8f
        val hgt = (minH + (maxH - minH) * hash01(i, salt + 9)) * u
        val sx = st.px(lx, p)
        val gy = ridgeY(lx, base, amp, salt) * u + hgt * 0.06f
        trees.addPine(sx, gy, hgt * 0.5f, hgt)
        shadeP?.addPineShade(sx, gy, hgt * 0.5f, hgt)
    }
    if (ground != null) drawPath(ridge, ground)
    drawPath(trees, body)
    if (shadeP != null && shade != null) drawPath(shadeP, shade)
}

/** A near pine with ink outline; [snow] lays snow on its tiers. */
internal fun DrawScope.drawPine(bx: Float, by: Float, w: Float, h: Float, color: Color, pen: Pen, snow: Color? = null) {
    val body0 = pen.conifer(color)
    val snow0 = snow ?: if (pen.winter) Pal.snow.atNight(pen.night, 0.45f) else null
    val tw = w * 0.13f
    drawRect(Color(0xFF6E4A33), Offset(bx - tw / 2f, by - h * 0.16f), Size(tw, h * 0.16f))
    drawRect(Ink.line, Offset(bx - tw / 2f, by - h * 0.16f), Size(tw, h * 0.16f), style = pen.thin)
    val by2 = by - h * 0.08f
    val hh = h * 0.92f
    val body = Path().apply { addPine(bx, by2, w, hh) }
    drawPath(body, body0)
    drawPath(Path().apply { addPineShade(bx, by2, w, hh) }, body0.shadow())
    drawPath(body, Ink.line, style = pen.stroke)
    if (snow0 != null) {
        val hw = w / 2f
        val s = Path().apply {
            moveTo(bx, by2 - hh * 1.005f)
            lineTo(bx + hw * 0.38f, by2 - hh * 0.71f)
            quadraticTo(bx + hw * 0.15f, by2 - hh * 0.65f, bx, by2 - hh * 0.7f)
            quadraticTo(bx - hw * 0.15f, by2 - hh * 0.65f, bx - hw * 0.38f, by2 - hh * 0.71f)
            close()
            addOval(Rect(bx + hw * 0.25f, by2 - hh * 0.66f, bx + hw * 0.6f, by2 - hh * 0.58f))
            addOval(Rect(bx - hw * 0.6f, by2 - hh * 0.66f, bx - hw * 0.25f, by2 - hh * 0.58f))
            addOval(Rect(bx + hw * 0.42f, by2 - hh * 0.34f, bx + hw * 0.9f, by2 - hh * 0.26f))
            addOval(Rect(bx - hw * 0.9f, by2 - hh * 0.34f, bx - hw * 0.42f, by2 - hh * 0.26f))
            addOval(Rect(bx - hw * 0.3f, by2 - hh * 0.4f, bx + hw * 0.2f, by2 - hh * 0.33f))
        }
        drawPath(s, snow0)
        drawPath(s, Ink.line, style = pen.thin)
    }
}

/** A bumpy leafy crown. */
internal fun crownPath(cx: Float, cy: Float, rx: Float, ry: Float, salt: Int): Path {
    val n = 10
    val pts = FloatArray(n * 2)
    for (i in 0 until n) {
        val a = i * (6.2831855f / n)
        val k = 0.8f + 0.32f * hash01(i, salt)
        pts[i * 2] = cx + cos(a) * rx * k
        pts[i * 2 + 1] = cy + sin(a) * ry * k
    }
    return blobPath(*pts)
}

internal fun DrawScope.drawBirch(bx: Float, by: Float, h: Float, pen: Pen, leaf: Color, salt: Int, night: Float) {
    val tw = h * 0.045f
    val trunk = Path().apply { poly(bx - tw * 0.4f, by - h * 0.8f, bx + tw * 0.4f, by - h * 0.8f, bx + tw * 0.6f, by, bx - tw * 0.6f, by) }
    drawPath(trunk, Pal.birch.atNight(night, 0.45f))
    drawPath(trunk, Ink.line, style = pen.stroke)
    val marks = ArrayList<Offset>(14)
    for (k in 0 until 7) {
        val y = by - h * (0.06f + 0.1f * k)
        val side = if (k % 2 == 0) -1f else 1f
        marks.add(Offset(bx + side * tw * 0.5f, y))
        marks.add(Offset(bx + side * tw * 0.02f, y + h * 0.01f))
    }
    drawPoints(marks, PointMode.Lines, Ink.line, strokeWidth = h * 0.012f, cap = StrokeCap.Round)
    inkLine(Offset(bx, by - h * 0.55f), Offset(bx - h * 0.1f, by - h * 0.68f), pen, width = pen.lw * 1.2f)
    if (pen.winter) {
        bareCrown(bx, by - h * 0.78f, h * 0.26f, h * 0.3f, pen, salt, night)
        return
    }
    val crown = crownPath(bx, by - h * 0.8f, h * 0.24f, h * 0.22f, salt)
    val leafC = if (pen.season == app.trollfoss.domain.Season.AUTUMN) recolor(leaf, SeasonPal.birchGold, 0.92f) else pen.foliage(leaf, salt)
    inked(crown, leafC.atNight(night, 0.5f), pen)
    if (pen.season == app.trollfoss.domain.Season.SPRING) blossomDots(bx, by - h * 0.8f, h * 0.24f, h * 0.22f, salt, night)
}

/** A grass tuft of five blades, bottom centre at ([bx], [by]). */
internal fun tuftPath(bx: Float, by: Float, h: Float, sway: Float): Path = Path().apply {
    val w = h * 0.9f
    moveTo(bx - w * 0.45f, by)
    quadraticTo(bx - w * 0.45f, by - h * 0.5f, bx - w * 0.62f + sway, by - h * 0.82f)
    quadraticTo(bx - w * 0.25f, by - h * 0.5f, bx - w * 0.14f, by - h * 0.28f)
    quadraticTo(bx - w * 0.12f, by - h * 0.7f, bx + sway, by - h)
    quadraticTo(bx + w * 0.12f, by - h * 0.62f, bx + w * 0.14f, by - h * 0.28f)
    quadraticTo(bx + w * 0.3f, by - h * 0.55f, bx + w * 0.6f + sway, by - h * 0.78f)
    quadraticTo(bx + w * 0.45f, by - h * 0.45f, bx + w * 0.45f, by)
    close()
}

internal fun DrawScope.drawTuft(bx: Float, by: Float, h: Float, color: Color, pen: Pen, sway: Float) {
    val p = tuftPath(bx, by, h, sway)
    drawPath(p, pen.blade(color))
    drawPath(p, Ink.line, style = pen.thin)
}

/**
 * The see-through front face of a body of water, in front of everything, from scene x [x1] to [x2]:
 * a gently moving wave line on top, light caustics and a tint that deepens toward the [bed].
 */
internal fun DrawScope.drawWaterFront(st: Stage, pen: Pen, x1: Float, x2: Float, line: Float, bed: Float, light: Color, deep: Color): FloatArray? {
    val a = max(x1, st.left)
    val b = min(x2, st.right)
    if (b <= a) return null
    val u = st.u
    val t = pen.t
    val n = max(3, ((b - a) / 0.035f).toInt() + 2)
    val xs = FloatArray(n)
    val ys = FloatArray(n)
    for (i in 0 until n) {
        val x = a + (b - a) * i / (n - 1)
        xs[i] = st.x(x)
        ys[i] = (line + 0.005f * sin(x * 12f + t * 2.1f) + 0.0025f * sin(x * 27f - t * 3.3f)) * u
    }
    val bottom = bed * u
    val body = Path().apply {
        smoothRun(xs, ys, n, start = true)
        lineTo(xs[n - 1], bottom)
        lineTo(xs[0], bottom)
        close()
    }
    val night = pen.night
    drawPath(
        body,
        Brush.verticalGradient(
            0f to light.atNight(night, 0.5f).copy(alpha = 0.38f),
            1f to deep.atNight(night, 0.5f).copy(alpha = 0.5f),
            startY = line * u,
            endY = bed * u,
        ),
    )
    val c = Path()
    for (k in 0 until 3) {
        val depth = line + 0.03f + k * 0.035f
        if (depth > bed - 0.01f) break
        val off = wrap(t * 0.03f * (k + 1) + k * 0.05f, 0.16f)
        var x = floor((a - off) / 0.16f) * 0.16f + off
        while (x < b) {
            val x0 = st.x(x)
            val len = 0.075f * u
            val yy = (depth + 0.006f * sin(x * 9f + t * 1.5f + k)) * u
            c.moveTo(x0, yy)
            c.quadraticTo(x0 + len * 0.5f, yy - 0.012f * u * sin(t * 2f + x * 7f + k), x0 + len, yy)
            x += 0.16f
        }
    }
    clipPath(body) {
        drawPath(c, Color.White, alpha = 0.24f * (1f - 0.5f * night), style = Stroke(width = pen.lw * 1.1f, cap = StrokeCap.Round))
    }
    val surface = Path().apply { smoothRun(xs, ys, n, start = true) }
    drawPath(surface, Color.White, alpha = 0.75f, style = Stroke(width = pen.lw * 1.7f, cap = StrokeCap.Round))
    drawPath(surface, Ink.line, alpha = 0.45f, style = pen.thin)
    drawLine(deep.darken(0.2f), Offset(xs[0], bottom), Offset(xs[n - 1], bottom), strokeWidth = pen.lw, alpha = 0.5f)
    return ys
}

/**
 * The receding top of a body of water between screen heights [far] and [near] and scene x [x1]..[x2]:
 * short wave strokes that get finer toward the back, drifting slowly.
 */
internal fun DrawScope.drawWaterPlane(st: Stage, pen: Pen, x1: Float, x2: Float, far: Float, near: Float, color: Color, rows: Int, pFar: Float, pNear: Float, alpha: Float) {
    val u = st.u
    val t = pen.t
    val waves = Path()
    for (r in 0 until rows) {
        val f = (r + 0.5f) / rows
        val y = mix(far, near, f * f)
        val p = mix(pFar, pNear, f * f)
        val gap = mix(0.1f, 0.26f, f)
        val len = mix(0.018f, 0.05f, f)
        val off = wrap(t * 0.01f * (1f + f) + r * 0.37f, gap)
        val l0 = st.cam * p - gap
        var lx = floor((l0 - off) / gap) * gap + off + (r % 2) * gap * 0.5f
        val lEnd = st.cam * p + st.vw + gap
        while (lx < lEnd) {
            val sx = st.px(lx, p)
            val sceneX = sx / u + st.cam
            if (sceneX > x1 && sceneX < x2) {
                val yy = y * u + sin(t * 1.3f + lx * 5f) * 0.002f * u
                waves.moveTo(sx - len * u * 0.5f, yy)
                waves.quadraticTo(sx, yy - len * u * 0.35f, sx + len * u * 0.5f, yy)
            }
            lx += gap
        }
    }
    drawPath(waves, color, alpha = alpha, style = Stroke(width = pen.lw * 0.9f, cap = StrokeCap.Round))
}

// ------------------------------------------------------------------------------------ diorama base

/** The cut front of the diorama below the front edge: a slab of [body] with a [lip] on top. */
internal fun DrawScope.drawBase(st: Stage, pen: Pen, lip: Color, body: Color, x0: Float = -9f, x1: Float = 99f) {
    val u = st.u
    val y = FRONT_Y * u
    val l = max(-2f, st.x(x0))
    val r = min(st.w + 2f, st.x(x1))
    if (r <= l) return
    drawRect(Brush.verticalGradient(listOf(body, body.darken(0.35f)), startY = y, endY = st.h), Offset(l, y), Size(r - l, st.h - y + 2f))
    drawRect(lip, Offset(l, y), Size(r - l, 0.008f * u))
    drawLine(Ink.line, Offset(l, y), Offset(r, y), strokeWidth = pen.lw)
}

/** Pebbles and stones showing in the cut front of the diorama. */
internal fun DrawScope.drawBaseStones(st: Stage, pen: Pen, color: Color, salt: Int, x0: Float = -9f, x1: Float = 99f) {
    val u = st.u
    val p = Path()
    val spacing = 0.19f
    for (i in floor(st.left / spacing).toInt()..floor(st.right / spacing).toInt()) {
        val x = i * spacing + hash01(i, salt) * spacing * 0.6f
        if (x < x0 || x > x1 || hash01(i, salt + 1) < 0.3f) continue
        val w = (0.022f + 0.03f * hash01(i, salt + 2)) * u
        val cy = (0.982f + 0.012f * hash01(i, salt + 3)) * u
        p.addOval(Rect(st.x(x) - w / 2f, cy - w * 0.3f, st.x(x) + w / 2f, cy + w * 0.3f))
    }
    drawPath(p, color)
    drawPath(p, Ink.line, alpha = 0.5f, style = pen.thin)
}

// ------------------------------------------------------------------------------------ oblique 3D

/**
 * The upper edge of the original scenery geometry. Background fills reach further through
 * [Stage.backgroundTop] when a tablet shows more sky or wall above the scene.
 */
internal const val SKY_TOP = -0.34f

/** The front edge of every floor band: the band runs from the place's back up to here. */
internal const val FRONT_Y = PlaceId.FRONT

/** How far right (in units) a point on the ground moves per unit it rises on screen, going back. */
internal const val RECEDE = Oblique.DX / -Oblique.DY

/** Screen x shift of a ground point at screen height [y] compared with the front edge. */
internal fun recede(y: Float): Float = (FRONT_Y - y) * RECEDE

/** A flat rectangle lying on the ground: front edge from [x0] to [x1] at [yf], back edge at [yb]. */
internal fun Path.floorQuad(u: Float, cam: Float, x0: Float, x1: Float, yf: Float, yb: Float) {
    val s = (yf - yb) * RECEDE
    moveTo((x0 + s - cam) * u, yb * u)
    lineTo((x1 + s - cam) * u, yb * u)
    lineTo((x1 - cam) * u, yf * u)
    lineTo((x0 - cam) * u, yf * u)
    close()
}

/** A round thing of radius [r] units lying on the ground, centred on scene ([cx], [cy]). */
internal fun Path.floorDisc(u: Float, cam: Float, cx: Float, cy: Float, rx: Float, rz: Float = rx, n: Int = 20) {
    for (i in 0 until n) {
        val a = i * 6.2831855f / n
        val dz = rz * sin(a)
        val x = (cx + rx * cos(a) + Oblique.DX * dz - cam) * u
        val y = (cy + Oblique.DY * dz) * u
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/**
 * An oblique prism: the clockwise polygon [pts] (x, y pairs in pixels) is the front face, extruded
 * [depth] pixels back into the picture. Visible sides are drawn first, coloured by [sides] (edge index,
 * whether it faces up), then the front face.
 */
internal fun DrawScope.prism(
    pts: FloatArray, depth: Float, front: Color, pen: Pen,
    outline: Boolean = true, shadeFront: Boolean = false, sides: (Int, Boolean) -> Color,
) {
    val vx = Oblique.DX * depth
    val vy = Oblique.DY * depth
    val n = pts.size / 2
    for (i in 0 until n) {
        val ax = pts[2 * i]
        val ay = pts[2 * i + 1]
        val bx = pts[(2 * i + 2) % (2 * n)]
        val by = pts[(2 * i + 3) % (2 * n)]
        val nx = by - ay
        val ny = ax - bx
        if (nx * vx + ny * vy <= 0f) continue
        val quad = Path().apply {
            moveTo(ax, ay)
            lineTo(bx, by)
            lineTo(bx + vx, by + vy)
            lineTo(ax + vx, ay + vy)
            close()
        }
        drawPath(quad, sides(i, ny < 0f && -ny > abs(nx) * 0.35f))
        if (outline) drawPath(quad, Ink.line, style = pen.stroke)
    }
    val f = Path().apply { poly(*pts) }
    if (shadeFront) {
        inked(f, front, pen, outline = outline)
    } else {
        drawPath(f, front)
        if (outline) drawPath(f, Ink.line, style = pen.stroke)
    }
}

/**
 * An ordinary house with its gable toward us and its long side receding: bottom centre of the gable at
 * ([bx], [by]), [w] wide, walls [wallH] and roof [roofH] high, [depth] deep, all in pixels.
 */
internal fun DrawScope.drawHouse3d(
    bx: Float, by: Float, w: Float, wallH: Float, roofH: Float, depth: Float,
    wall: Color, roof: Color, pen: Pen, night: Float,
    outline: Boolean = true, door: Color? = null, sideWindows: Int = 2, chimney: Boolean = false, t: Float = 0f,
) {
    val h = w / 2f
    val wl = wall.atNight(night, 0.5f)
    val rf = pen.snowy(roof).atNight(night, 0.5f)
    val vx = Oblique.DX * depth
    val vy = Oblique.DY * depth
    val lit = ramp((night - 0.35f) / 0.4f)
    val glass = lerp(Color(0xFF7FA6CC).atNight(night, 0.4f), Color(0xFFFFD66B), lit)
    if (chimney) {
        val cx = bx + h * 0.35f + vx * 0.6f
        val cy = by - wallH - roofH * 0.35f + vy * 0.6f
        for (k in 0 until 3) {
            val ph = wrap(t * 0.22f + k / 3f, 1f)
            drawCircle(Color.White, (0.06f + ph * 0.1f) * w, Offset(cx + ph * 0.25f * w + sin(ph * 5f + k) * 0.04f * w, cy - 0.25f * w - ph * 0.7f * w), alpha = 0.5f * (1f - ph))
        }
        box3d(Rect(cx - 0.06f * w, cy - 0.22f * w, cx + 0.06f * w, cy), 0.1f * w, Color(0xFF8B8793).atNight(night, 0.5f), pen)
    }
    val pts = floatArrayOf(bx - h, by - wallH, bx, by - wallH - roofH, bx + h, by - wallH, bx + h, by, bx - h, by)
    prism(pts, depth, wl, pen, outline = outline) { i, _ ->
        when (i) {
            0 -> rf.lighten(0.12f)
            1 -> rf
            else -> wl.darken(0.2f)
        }
    }
    // Windows along the long side.
    for (k in 0 until sideWindows) {
        val f0 = (k + 0.3f) / (sideWindows + 0.1f)
        val f1 = f0 + 0.45f / (sideWindows + 0.1f)
        val y0 = by - wallH * 0.72f
        val y1 = by - wallH * 0.3f
        val win = Path().apply {
            moveTo(bx + h + vx * f0, y0 + vy * f0)
            lineTo(bx + h + vx * f1, y0 + vy * f1)
            lineTo(bx + h + vx * f1, y1 + vy * f1)
            lineTo(bx + h + vx * f0, y1 + vy * f0)
            close()
        }
        drawPath(win, glass.darken(0.1f))
        if (outline) drawPath(win, Color.White, style = Stroke(pen.lw * 1.2f))
    }
    // Front window and door.
    val ww = w * 0.24f
    val wy = by - wallH * 0.78f
    val wx = if (door != null) bx - h * 0.45f else bx
    if (lit > 0f) drawCircle(Color(0xFFFFD66B), ww * 1.2f, Offset(wx, wy + ww * 0.5f), alpha = 0.22f * lit)
    drawRect(glass, Offset(wx - ww / 2f, wy), Size(ww, ww))
    if (outline) {
        drawLine(Color.White, Offset(wx, wy), Offset(wx, wy + ww), strokeWidth = pen.lw)
        drawLine(Color.White, Offset(wx - ww / 2f, wy + ww / 2f), Offset(wx + ww / 2f, wy + ww / 2f), strokeWidth = pen.lw)
        drawRect(Color.White, Offset(wx - ww / 2f, wy), Size(ww, ww), style = Stroke(pen.lw * 1.4f))
    }
    if (door != null) {
        val d = Rect(bx + h * 0.12f, by - wallH * 0.62f, bx + h * 0.62f, by)
        drawRect(door.atNight(night, 0.5f), d.topLeft, d.size)
        if (outline) {
            drawRect(Ink.line, d.topLeft, d.size, style = pen.thin)
            drawCircle(Pal.sun, w * 0.018f, Offset(d.right - d.width * 0.22f, d.center.y))
        }
    }
    // The roof's front edge.
    val eave = Path().apply {
        moveTo(bx - h * 1.1f, by - wallH + roofH * 0.1f)
        lineTo(bx, by - wallH - roofH * 1.06f)
        lineTo(bx + h * 1.1f, by - wallH + roofH * 0.1f)
    }
    drawPath(eave, rf.darken(0.15f), style = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
