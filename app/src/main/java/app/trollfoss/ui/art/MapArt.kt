package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Weather
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Where each place sits on the map, as fractions of the map's width and height. The spots lie in three
 * rows (mountains, the valley, the waterfront) at least 0.27 apart up and down, and 0.14 apart along a
 * row, so 90 dp buttons never overlap on a landscape phone. They keep clear of the close button (top
 * left), the gear (top right) and the two round buttons (bottom left).
 */
fun mapSpot(place: PlaceId): Offset = when (place) {
    PlaceId.MOUNTAIN -> Offset(0.19f, 0.24f)
    PlaceId.LAB -> Offset(0.62f, 0.25f)
    PlaceId.SPACE -> Offset(0.81f, 0.19f)
    PlaceId.TIVOLI -> Offset(0.12f, 0.54f)
    PlaceId.SHOP -> Offset(0.26f, 0.55f)
    PlaceId.FOREST -> Offset(0.4f, 0.53f)
    PlaceId.DOCTOR -> Offset(0.54f, 0.53f)
    PlaceId.CAFE -> Offset(0.68f, 0.55f)
    PlaceId.FARM -> Offset(0.86f, 0.53f)
    PlaceId.SALON -> Offset(0.32f, 0.83f)
    PlaceId.HOME -> Offset(0.46f, 0.82f)
    PlaceId.STAGE -> Offset(0.6f, 0.84f)
    PlaceId.BEACH -> Offset(0.74f, 0.83f)
    PlaceId.UNDERWATER -> Offset(0.88f, 0.84f)
}

/**
 * The village of Trollfoss seen from above at a slight angle, filling [size] (any landscape shape):
 * snowy mountains with the great waterfall in the middle, the river down to the fjord, forest, fields and
 * one landmark per place at [mapSpot]. The [highlight]ed landmark bounces. [t] drives the animation.
 */
fun DrawScope.drawIslandMap(pen: Pen, highlight: PlaceId?, t: Float) {
    val m = MapPen(size.width, size.height, pen, t)
    m.sky(this)
    m.mountains(this)
    m.valley(this)
    m.fields(this)
    m.fjord(this)
    m.river(this)
    m.waterfall(this)
    m.roads(this)
    m.forest(this)
    m.cottages(this)
    for (p in PlaceId.entries) m.landmark(this, p, p == highlight)
    m.clouds(this)
}

/** Everything the map needs for one frame. Coordinates are fractions of the map's width and height. */
private class MapPen(val w: Float, val h: Float, val pen: Pen, val t: Float) {
    val n = pen.night
    val snow = pen.weather == Weather.SNOW
    val oc = overcast(pen)

    /** Landmark scale: a tenth of the height, but never wider than the map allows. */
    val s = min(h * 0.1f, w * 0.055f)

    fun p(x: Float, y: Float) = Offset(x * w, y * h)
    fun night(c: Color, k: Float = 0.55f) = c.atNight(n, k)

    fun sky(d: DrawScope) = with(d) {
        val top = lerp(lerp(Color(0xFF4AA8EE), Color(0xFF8C99B0), oc), Color(0xFF100D35), n)
        val low = lerp(lerp(Color(0xFFD4F0FF), Color(0xFFD5DBE5), oc), Color(0xFF4B3A8E), n)
        drawRect(Brush.verticalGradient(listOf(top, low), startY = 0f, endY = h * 0.45f))
        val stars = ramp((n - 0.2f) / 0.55f) * (1f - oc * 0.8f)
        if (stars > 0f) {
            val pts = ArrayList<Offset>(40)
            for (i in 0 until 40) pts.add(p(hash01(i, 801), hash01(i, 802) * 0.3f))
            drawPoints(pts, PointMode.Points, Color(0xFFFFF7DA), strokeWidth = h * 0.006f, cap = StrokeCap.Round, alpha = stars * (0.7f + 0.3f * sin(t * 1.3f)))
            for (i in 0 until 4) twinkle(p(hash01(i, 803), 0.03f + hash01(i, 804) * 0.18f), h * 0.018f, Color(0xFFFFF7DA), stars * (0.5f + 0.5f * sin(t * 1.7f + i * 2f)))
        }
        val aurora = ramp((n - 0.3f) / 0.55f) * (1f - 0.6f * oc)
        if (aurora > 0.01f) {
            for (c in 0 until 2) {
                val col = if (c == 0) Pal.auroraGreen else Pal.auroraViolet
                val band = Path()
                val k = 12
                for (i in 0..k) {
                    val x = i / k.toFloat()
                    val y = 0.02f + c * 0.05f + 0.03f * sin(x * 7f + t * 0.35f + c * 2f)
                    if (i == 0) band.moveTo(x * w, y * h) else band.lineTo(x * w, y * h)
                }
                for (i in k downTo 0) {
                    val x = i / k.toFloat()
                    val y = 0.15f + c * 0.05f + 0.03f * sin(x * 7f + t * 0.35f + c * 2f) + 0.02f * sin(x * 13f - t * 0.5f)
                    band.lineTo(x * w, y * h)
                }
                band.close()
                drawPath(band, Brush.verticalGradient(listOf(col.copy(alpha = 0f), col.copy(alpha = 0.75f * aurora)), startY = 0.02f * h, endY = 0.22f * h))
            }
        }
        val sunAt = p(0.3f, 0.075f)
        if (n < 0.6f) drawSun(sunAt, h * 0.045f, pen, (1f - n / 0.6f) * (1f - oc), color = Color(0xFFFFD447))
        drawMoon(p(0.3f, 0.075f), h * 0.035f, pen, ramp((n - 0.35f) / 0.4f))
        if (pen.rainbow > 0.01f) drawRainbow(p(0.5f, 0.5f), h * 0.42f, h * 0.014f, pen.rainbow)
    }

    /** Mountains across the top: a pale far range, then three big massifs with snow and a hidden troll face. */
    fun mountains(d: DrawScope) = with(d) {
        val far = Path().apply {
            val pts = floatArrayOf(0f, 0.2f, 0.06f, 0.12f, 0.12f, 0.17f, 0.24f, 0.08f, 0.34f, 0.15f, 0.44f, 0.05f, 0.56f, 0.11f, 0.66f, 0.1f, 0.76f, 0.22f, 0.86f, 0.27f, 0.95f, 0.22f, 1f, 0.26f)
            moveTo(0f, 0.5f * h)
            for (i in pts.indices step 2) lineTo(pts[i] * w, pts[i + 1] * h)
            lineTo(w, 0.5f * h)
            close()
        }
        drawPath(far, night(Color(0xFFB9C7E0), 0.7f))
        massif(this, 0.16f, 0.09f, 0.0f, 0.34f, 0.44f, 0.24f)
        massif(this, 0.9f, 0.3f, 0.66f, 1.08f, 0.47f, 0.2f)
        massif(this, 0.74f, 0.25f, 0.6f, 0.9f, 0.46f, 0.3f)
        massif(this, 0.5f, 0.06f, 0.3f, 0.7f, 0.46f, 0.2f)
        trollFace(this)
        // A green band where the mountains meet the valley.
        val foot = Path().apply {
            moveTo(0f, 0.43f * h)
            for (i in 0..20) {
                val x = i / 20f
                lineTo(x * w, (0.405f + 0.012f * sin(x * 31f) + 0.008f * sin(x * 13f)) * h)
            }
            lineTo(w, 0.52f * h)
            lineTo(0f, 0.52f * h)
            close()
        }
        drawPath(foot, night(if (snow) Color(0xFFEFF4FB) else Color(0xFF8ACB67)))
    }

    /** One mountain: lit left face, shaded right face, snow cap, ink outline. */
    private fun massif(d: DrawScope, px: Float, py: Float, x0: Float, x1: Float, base: Float, snowTo: Float) = with(d) {
        val body = Path().apply {
            moveTo(x0 * w, base * h)
            lineTo((px - 0.015f) * w, (py + 0.01f) * h)
            quadraticTo(px * w, (py - 0.008f) * h, (px + 0.015f) * w, (py + 0.01f) * h)
            lineTo(x1 * w, base * h)
            close()
        }
        val rock = night(Color(0xFF8F9AB0), 0.6f)
        drawPath(body, rock)
        val shade = Path().apply { poly((px + 0.01f) * w, (py + 0.01f) * h, x1 * w, base * h, (px + 0.12f) * w, base * h, (px + 0.02f) * w, (py + 0.2f) * h) }
        drawPath(shade, rock.darken(0.14f))
        if (snowTo <= py + 0.02f) {
            drawPath(body, Ink.line, style = pen.stroke)
            return@with
        }
        val f = ((snowTo - py) / (base - py)).coerceIn(0f, 1f)
        val lx = mix(px, x0, f)
        val rx = mix(px, x1, f)
        val cap = Path().apply {
            moveTo(px * w, (py - 0.006f) * h)
            lineTo(rx * w, snowTo * h)
            lineTo(mix(rx, lx, 0.3f) * w, (snowTo - 0.03f) * h)
            lineTo(mix(rx, lx, 0.5f) * w, (snowTo + 0.015f) * h)
            lineTo(mix(rx, lx, 0.72f) * w, (snowTo - 0.025f) * h)
            lineTo(lx * w, snowTo * h)
            close()
        }
        drawPath(cap, night(Pal.snow, 0.5f))
        val capShade = Path().apply { poly((px + 0.004f) * w, py * h, rx * w, snowTo * h, mix(rx, lx, 0.3f) * w, (snowTo - 0.03f) * h, (px + 0.02f) * w, (py + 0.15f) * h) }
        clipPath(cap) { drawPath(capShade, night(Pal.snowShade, 0.5f)) }
        drawPath(body, Ink.line, style = pen.stroke)
    }

    /** A big friendly troll face hidden in the rock, left of the waterfall. It blinks now and then. */
    private fun trollFace(d: DrawScope) = with(d) {
        val c = p(0.41f, 0.25f)
        val sz = h * 0.07f
        val rock = night(Color(0xFF8F9AB0), 0.6f)
        val line = rock.darken(0.45f)
        val blink = if (wrap(t, 6.5f) < 0.18f) 0.15f else 1f
        for (side in intArrayOf(-1, 1)) {
            val e = Offset(c.x + side * sz * 0.34f, c.y - sz * 0.18f)
            drawArc(line, 200f, 140f, false, Offset(e.x - sz * 0.22f, e.y - sz * 0.3f), Size(sz * 0.44f, sz * 0.3f), alpha = 0.45f, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
            drawOval(Color(0xFF3B3346), Offset(e.x - sz * 0.06f, e.y - sz * 0.06f * blink), Size(sz * 0.12f, sz * 0.12f * blink), alpha = 0.55f)
        }
        val nose = Path().apply {
            moveTo(c.x - sz * 0.08f, c.y - sz * 0.12f)
            quadraticTo(c.x - sz * 0.22f, c.y + sz * 0.2f, c.x, c.y + sz * 0.2f)
            quadraticTo(c.x + sz * 0.2f, c.y + sz * 0.2f, c.x + sz * 0.1f, c.y - sz * 0.05f)
        }
        drawPath(nose, rock.lighten(0.12f))
        drawPath(nose, line, alpha = 0.4f, style = Stroke(pen.lw, cap = StrokeCap.Round))
        drawArc(line, 15f, 150f, false, Offset(c.x - sz * 0.36f, c.y + sz * 0.05f), Size(sz * 0.72f, sz * 0.42f), alpha = 0.45f, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    }

    fun valley(d: DrawScope) = with(d) {
        val top = if (snow) Color(0xFFEFF4FB) else Color(0xFF8ACB67)
        val bottom = if (snow) Color(0xFFDCE6F4) else Color(0xFF6DB356)
        drawRect(Brush.verticalGradient(listOf(night(top), night(bottom)), startY = 0.44f * h, endY = h), Offset(0f, 0.45f * h), Size(w, 0.56f * h))
        val patches = Path()
        for (i in 0 until 14) {
            val c = p(hash01(i, 811), 0.5f + hash01(i, 812) * 0.45f)
            patches.addOval(Rect(c.x - w * 0.05f, c.y - h * 0.015f, c.x + w * 0.05f, c.y + h * 0.015f))
        }
        drawPath(patches, night(if (snow) Color(0xFFD2DDEE) else Color(0xFF7DBE5E)), alpha = 0.6f)
    }

    /** Patchwork fields around the farm, each with rows. */
    fun fields(d: DrawScope) = with(d) {
        val colors = listOf(Color(0xFFEBCB5E), Color(0xFFB5DC76), Color(0xFFB98B5A), Color(0xFFD8E48A))
        val f = floatArrayOf(
            0.77f, 0.46f, 0.86f, 0.45f, 0.86f, 0.52f, 0.77f, 0.53f,
            0.86f, 0.45f, 0.94f, 0.44f, 0.94f, 0.5f, 0.86f, 0.51f,
            0.94f, 0.44f, 1.0f, 0.44f, 1.0f, 0.52f, 0.94f, 0.52f,
            0.77f, 0.53f, 0.83f, 0.53f, 0.83f, 0.63f, 0.76f, 0.64f,
            0.92f, 0.53f, 1.0f, 0.53f, 1.0f, 0.62f, 0.93f, 0.63f,
        )
        val rows = ArrayList<Offset>(80)
        for (k in 0 until 5) {
            val q = Path().apply { poly(f[k * 8] * w, f[k * 8 + 1] * h, f[k * 8 + 2] * w, f[k * 8 + 3] * h, f[k * 8 + 4] * w, f[k * 8 + 5] * h, f[k * 8 + 6] * w, f[k * 8 + 7] * h) }
            val col = if (snow) Color(0xFFF4F8FF) else colors[k % colors.size]
            drawPath(q, night(col))
            drawPath(q, night(Color(0xFF4F8A45)), style = Stroke(pen.lw * 1.2f, join = StrokeJoin.Round))
            if (!snow) {
                for (j in 1..5) {
                    val a = j / 6f
                    rows.add(Offset(mix(f[k * 8 + 6], f[k * 8 + 4], a) * w, mix(f[k * 8 + 7], f[k * 8 + 5], a) * h))
                    rows.add(Offset(mix(f[k * 8], f[k * 8 + 2], a) * w, mix(f[k * 8 + 1], f[k * 8 + 3], a) * h))
                }
            }
        }
        drawPoints(rows, PointMode.Lines, night(Color(0xFF6E5A36)), strokeWidth = pen.lw * 0.6f, alpha = 0.35f)
    }

    fun fjord(d: DrawScope) = with(d) {
        val sea = Path().apply {
            moveTo(0.42f * w, 1.02f * h)
            quadraticTo(0.49f * w, 0.945f * h, 0.58f * w, 0.93f * h)
            quadraticTo(0.68f * w, 0.915f * h, 0.78f * w, 0.905f * h)
            quadraticTo(0.84f * w, 0.9f * h, 0.86f * w, 0.8f * h)
            quadraticTo(0.89f * w, 0.7f * h, 1.02f * w, 0.66f * h)
            lineTo(1.02f * w, 1.02f * h)
            close()
        }
        drawPath(sea, night(if (snow) Color(0xFFF7F9FF) else Color(0xFFF1D9A2)), style = Stroke(h * 0.04f, join = StrokeJoin.Round))
        drawPath(sea, Brush.verticalGradient(listOf(night(Color(0xFF5AA9E6), 0.6f), night(Color(0xFF2F6FB8), 0.6f)), startY = 0.72f * h, endY = h))
        drawPath(sea, Ink.line, style = pen.stroke)
        val waves = Path()
        for (i in 0 until 9) {
            val x = 0.6f + wrap(hash01(i, 821) * 0.4f + t * 0.006f, 0.4f)
            val y = 0.93f + hash01(i, 822) * 0.06f
            if (x > 0.84f && y < 0.95f) continue
            waves.moveTo((x - 0.012f) * w, y * h)
            waves.quadraticTo(x * w, (y - 0.012f) * h, (x + 0.012f) * w, y * h)
        }
        drawPath(waves, Color.White, alpha = 0.7f * (1f - 0.4f * n), style = Stroke(pen.lw, cap = StrokeCap.Round))
        // A little sailboat tacking to and fro.
        val bx = 0.95f + 0.025f * sin(t * 0.08f)
        val dir = if (cos(t * 0.08f) >= 0f) 1f else -1f
        val c = p(bx, 0.76f + 0.004f * sin(t * 1.3f))
        withTransform({ scale(dir, 1f, pivot = c) }) {
            val bs = s * 0.55f
            val hull = Path().apply { poly(c.x - bs * 0.5f, c.y - bs * 0.15f, c.x + bs * 0.5f, c.y - bs * 0.15f, c.x + bs * 0.34f, c.y, c.x - bs * 0.34f, c.y) }
            val sail = Path().apply { poly(c.x + bs * 0.02f, c.y - bs * 0.95f, c.x + bs * 0.4f, c.y - bs * 0.22f, c.x + bs * 0.02f, c.y - bs * 0.22f) }
            drawLine(Ink.line, Offset(c.x, c.y - bs * 0.15f), Offset(c.x, c.y - bs * 0.97f), strokeWidth = pen.lw)
            inked(sail, night(Color.White, 0.4f), pen, shade = false)
            inked(hull, night(Color(0xFFE2504A), 0.4f), pen, shade = false)
        }
        if (n < 0.7f) {
            val stroke = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            for (i in 0 until 3) {
                val a = t * 0.3f + i * 2.1f
                val g = p(0.94f + 0.03f * cos(a), 0.88f + 0.025f * sin(a) + i * 0.02f)
                drawGull(g, h * 0.018f, 0.5f + 0.5f * sin(t * 4f + i), stroke, 1f - n / 0.7f)
            }
        }
    }

    private val riverX = floatArrayOf(0.5f, 0.482f, 0.5f, 0.53f, 0.535f, 0.52f)
    private val riverY = floatArrayOf(0.47f, 0.6f, 0.7f, 0.79f, 0.87f, 0.97f)

    fun river(d: DrawScope) = with(d) {
        val xs = FloatArray(riverX.size) { riverX[it] * w }
        val ys = FloatArray(riverY.size) { riverY[it] * h }
        val path = Path().apply { smoothRun(xs, ys, xs.size, start = true) }
        val rw = h * 0.032f
        drawPath(path, Ink.line, style = Stroke(rw + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path, night(if (snow) Color(0xFFBFD8F2) else Color(0xFF4F9CE0), 0.6f), style = Stroke(rw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(
            path, Color.White, alpha = 0.55f * (1f - 0.4f * n),
            style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(h * 0.03f, h * 0.05f), -t * h * 0.04f)),
        )
        // Wooden footbridges: by the waterfall and down by the concert house.
        for ((a, b) in listOf(p(0.455f, 0.6f) to p(0.51f, 0.595f), p(0.505f, 0.87f) to p(0.565f, 0.868f))) {
            capsule(a, b, h * 0.024f, night(Color(0xFFC98A55), 0.5f), pen)
            drawLine(night(Color(0xFFA0663B), 0.5f), Offset(a.x, a.y - h * 0.012f), Offset(b.x, b.y - h * 0.012f), strokeWidth = pen.lw)
            drawLine(night(Color(0xFFA0663B), 0.5f), Offset(a.x, a.y + h * 0.012f), Offset(b.x, b.y + h * 0.012f), strokeWidth = pen.lw)
        }
    }

    /** The great waterfall pouring from the central mountain into its pool, with foam, mist and a rainbow. */
    fun waterfall(d: DrawScope) = with(d) {
        val pool = Rect(0.435f * w, 0.43f * h, 0.565f * w, 0.49f * h)
        drawOval(night(Color(0xFF3E8FD6), 0.6f), pool.topLeft, pool.size)
        drawOval(night(Color(0xFF8FD3F5), 0.5f), Offset(pool.left + pool.width * 0.15f, pool.top + pool.height * 0.1f), Size(pool.width * 0.7f, pool.height * 0.45f), alpha = 0.6f)
        drawOval(Ink.line, pool.topLeft, pool.size, style = pen.stroke)
        val stream = Path().apply {
            moveTo(0.505f * w, 0.07f * h)
            quadraticTo(0.49f * w, 0.12f * h, 0.5f * w, 0.17f * h)
        }
        drawPath(stream, night(Color(0xFF8FD3F5), 0.4f), style = Stroke(h * 0.01f, cap = StrokeCap.Round))
        val fall = Path().apply { poly(0.485f * w, 0.165f * h, 0.515f * w, 0.165f * h, 0.525f * w, 0.455f * h, 0.475f * w, 0.455f * h) }
        drawPath(fall, Brush.verticalGradient(listOf(night(Color(0xFFBDE8FB), 0.3f), night(Color(0xFFF2FBFF), 0.25f)), startY = 0.165f * h, endY = 0.455f * h))
        clipPath(fall) {
            val streaks = ArrayList<Offset>(24)
            for (i in 0 until 8) {
                val x = (0.478f + i * 0.0062f) * w
                for (k in 0 until 2) {
                    val y0 = 0.14f + wrap(t * (0.18f + 0.05f * hash01(i, 831)) + hash01(i * 2 + k, 832), 0.36f)
                    streaks.add(Offset(x, y0 * h))
                    streaks.add(Offset(x, (y0 + 0.05f) * h))
                }
            }
            drawPoints(streaks, PointMode.Lines, night(Color(0xFF8FCDEE), 0.3f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
        }
        drawPath(fall, Ink.line, style = pen.thin)
        val lip = Path().apply {
            moveTo(0.47f * w, 0.17f * h)
            quadraticTo(0.5f * w, 0.15f * h, 0.53f * w, 0.17f * h)
        }
        drawPath(lip, night(Color(0xFF6E7890), 0.5f), style = Stroke(h * 0.012f, cap = StrokeCap.Round))
        for (i in 0 until 6) {
            val r = h * (0.011f + 0.004f * sin(t * 3f + i * 1.7f))
            drawCircle(Color.White, r, p(0.47f + i * 0.012f, 0.452f + 0.004f * (i % 2)))
        }
        for (i in 0 until 3) {
            val ph = wrap(t * 0.15f + i / 3f, 1f)
            drawCircle(Color.White, h * (0.03f + ph * 0.03f), p(0.5f + (i - 1) * 0.02f, 0.44f - ph * 0.08f), alpha = 0.3f * sin(ph * 3.1416f))
        }
        val spray = 0.55f * (1f - n) * (1f - oc)
        if (spray > 0.02f) drawRainbow(p(0.5f, 0.47f), h * 0.07f, h * 0.0055f, spray)
        if (n > 0f) drawCircle(Brush.radialGradient(listOf(Color(0x55BFE6FF), Color(0x00BFE6FF)), center = p(0.5f, 0.35f), radius = h * 0.2f), h * 0.2f, p(0.5f, 0.35f), alpha = n)
    }

    fun roads(d: DrawScope) = with(d) {
        val r = floatArrayOf(
            0.12f, 0.585f, 0.19f, 0.6f, 0.26f, 0.595f,
            0.26f, 0.595f, 0.33f, 0.59f, 0.4f, 0.575f,
            0.4f, 0.575f, 0.47f, 0.6f, 0.54f, 0.575f,
            0.54f, 0.575f, 0.61f, 0.6f, 0.68f, 0.595f,
            0.68f, 0.595f, 0.77f, 0.6f, 0.86f, 0.575f,
            0.26f, 0.595f, 0.31f, 0.72f, 0.32f, 0.875f,
            0.32f, 0.875f, 0.39f, 0.88f, 0.46f, 0.865f,
            0.46f, 0.865f, 0.53f, 0.87f, 0.6f, 0.885f,
            0.6f, 0.885f, 0.67f, 0.9f, 0.74f, 0.875f,
            0.68f, 0.595f, 0.73f, 0.72f, 0.74f, 0.875f,
            0.26f, 0.595f, 0.2f, 0.42f, 0.19f, 0.285f,
            0.68f, 0.595f, 0.66f, 0.42f, 0.62f, 0.295f,
        )
        val path = Path()
        for (i in r.indices step 6) {
            path.moveTo(r[i] * w, r[i + 1] * h)
            path.quadraticTo(r[i + 2] * w, r[i + 3] * h, r[i + 4] * w, r[i + 5] * h)
        }
        drawPath(path, night(Color(0xFFB08A58), 0.5f), style = Stroke(h * 0.016f, cap = StrokeCap.Round))
        drawPath(path, night(if (snow) Color(0xFFF7F9FF) else Color(0xFFEBD3A2), 0.5f), style = Stroke(h * 0.01f, cap = StrokeCap.Round))
    }

    private val trees = floatArrayOf(
        0.34f, 0.43f, 0.31f, 0.46f, 0.37f, 0.46f, 0.28f, 0.44f,
        0.03f, 0.46f, 0.07f, 0.43f, 0.04f, 0.65f, 0.09f, 0.68f, 0.05f, 0.74f, 0.13f, 0.72f,
        0.72f, 0.44f, 0.75f, 0.41f, 0.7f, 0.47f,
        0.06f, 0.84f, 0.12f, 0.88f, 0.04f, 0.93f, 0.18f, 0.94f, 0.25f, 0.72f,
        0.9f, 0.66f, 0.96f, 0.64f,
    )

    fun forest(d: DrawScope) = with(d) {
        val order = (0 until trees.size / 2).sortedBy { trees[it * 2 + 1] }
        val green = night(Pal.granLight, 0.5f)
        for (i in order) {
            val b = p(trees[i * 2], trees[i * 2 + 1])
            val th = s * (0.55f + 0.2f * hash01(i, 841))
            val body = Path().apply { addPine(b.x, b.y, th * 0.55f, th) }
            drawPath(body, green)
            drawPath(Path().apply { addPineShade(b.x, b.y, th * 0.55f, th) }, green.shadow())
            if (snow) drawPath(Path().apply { poly(b.x, b.y - th * 1.005f, b.x + th * 0.1f, b.y - th * 0.72f, b.x - th * 0.1f, b.y - th * 0.72f) }, Pal.snow)
            drawPath(body, Ink.line, style = pen.thin)
        }
    }

    fun cottages(d: DrawScope) = with(d) {
        val c = floatArrayOf(0.18f, 0.7f, 0.39f, 0.7f, 0.62f, 0.7f, 0.8f, 0.7f, 0.44f, 0.66f, 0.66f, 0.74f)
        val walls = listOf(Color(0xFFFFE08A), Color(0xFFF7F4EE), Color(0xFFA9D3F0), Color(0xFFC0463A), Color(0xFFF7F4EE), Color(0xFFFFB9A0))
        for (i in c.indices step 2) {
            val b = p(c[i], c[i + 1])
            val cw = s * 0.4f
            drawHouse3d(b.x, b.y, cw, cw * 0.55f, cw * 0.4f, cw * 0.7f, walls[i / 2], if (snow) Pal.snow else Color(0xFF55505E), pen, n, sideWindows = 1)
        }
    }

    fun landmark(d: DrawScope, place: PlaceId, hl: Boolean) = with(d) {
        val spot = mapSpot(place)
        val base = p(spot.x, spot.y + 0.045f)
        val bounce = if (hl) abs(sin(t * 3.4f)) * h * 0.03f else 0f
        val grow = if (hl) 1.12f + 0.03f * sin(t * 6.8f) else 1f
        if (place != PlaceId.SPACE && place != PlaceId.UNDERWATER) {
            if (hl) drawOval(Pal.sun, Offset(base.x - s * 0.75f, base.y - s * 0.14f), Size(s * 1.5f, s * 0.28f), alpha = 0.55f)
            drawOval(Ink.shadow, Offset(base.x - s * 0.55f, base.y - s * 0.08f), Size(s * 1.1f, s * 0.16f))
        }
        withTransform({
            translate(0f, -bounce)
            scale(grow, grow, pivot = base)
        }) {
            when (place) {
                PlaceId.HOME -> drawHouse3d(base.x, base.y, s * 0.75f, s * 0.42f, s * 0.32f, s * 0.5f, Pal.falun, Color(0xFF4A4A57), pen, n, door = Color(0xFFF7F3EC), sideWindows = 2, chimney = true, t = t)
                PlaceId.CAFE -> cafe(this, base)
                PlaceId.SALON -> salon(this, base)
                PlaceId.BEACH -> beach(this, base)
                PlaceId.FOREST -> camp(this, base)
                PlaceId.LAB -> cave(this, base)
                PlaceId.MOUNTAIN -> cabin(this, base)
                PlaceId.FARM -> barn(this, base)
                PlaceId.SPACE -> station(this, p(spot.x, spot.y), hl)
                PlaceId.TIVOLI -> ferrisWheel(this, base)
                PlaceId.SHOP -> shop(this, base)
                PlaceId.DOCTOR -> clinic(this, base)
                PlaceId.STAGE -> concertHouse(this, base)
                PlaceId.UNDERWATER -> diveSpot(this, base)
            }
        }
    }

    /** The tivoli: a turning ferris wheel with coloured gondolas, and a striped tent. */
    private fun ferrisWheel(d: DrawScope, b: Offset) = with(d) {
        val tent = Path().apply {
            poly(b.x + s * 0.3f, b.y - s * 0.26f, b.x + s * 0.62f, b.y - s * 0.26f, b.x + s * 0.62f, b.y, b.x + s * 0.3f, b.y)
            poly(b.x + s * 0.26f, b.y - s * 0.24f, b.x + s * 0.46f, b.y - s * 0.5f, b.x + s * 0.66f, b.y - s * 0.24f)
        }
        drawPath(tent, night(Color(0xFFFFF6EC), 0.4f))
        clipPath(tent) { for (k in 0 until 4) drawRect(night(Color(0xFFE94F4F), 0.4f), Offset(b.x + s * (0.28f + k * 0.1f), b.y - s * 0.52f), Size(s * 0.05f, s * 0.52f)) }
        drawPath(tent, Ink.line, style = pen.thin)
        val hub = Offset(b.x - s * 0.1f, b.y - s * 0.52f)
        val r = s * 0.42f
        val legs = Path().apply {
            moveTo(hub.x - s * 0.24f, b.y)
            lineTo(hub.x, hub.y)
            lineTo(hub.x + s * 0.24f, b.y)
        }
        drawPath(legs, Ink.line, style = Stroke(s * 0.05f + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(legs, night(Color(0xFFB9C0CC), 0.4f), style = Stroke(s * 0.05f, cap = StrokeCap.Round))
        val turn = t * 0.25f
        val spokes = ArrayList<Offset>(16)
        for (k in 0 until 8) {
            val a = turn + k * 0.785f
            spokes.add(hub)
            spokes.add(Offset(hub.x + cos(a) * r, hub.y + sin(a) * r))
        }
        drawPoints(spokes, PointMode.Lines, night(Color(0xFF8A93A3), 0.4f), strokeWidth = pen.lw)
        drawCircle(Ink.line, r, hub, style = Stroke(s * 0.045f + pen.lw * 2f))
        drawCircle(night(Color(0xFFFF6B8A), 0.35f), r, hub, style = Stroke(s * 0.045f))
        val lit = ramp((n - 0.2f) / 0.5f)
        if (lit > 0f) {
            val bulbs = ArrayList<Offset>(16)
            for (k in 0 until 16) bulbs.add(Offset(hub.x + cos(turn + k * 0.3927f) * r, hub.y + sin(turn + k * 0.3927f) * r))
            drawPoints(bulbs, PointMode.Points, Color(0xFFFFE27A), strokeWidth = s * 0.09f, cap = StrokeCap.Round, alpha = 0.4f * lit)
            drawPoints(bulbs, PointMode.Points, Color(0xFFFFF3B0), strokeWidth = s * 0.04f, cap = StrokeCap.Round, alpha = lit)
        }
        val cols = listOf(Color(0xFFFFC83D), Color(0xFF5AA9E6), Color(0xFF5CE0A0), Color(0xFFD77BFF))
        for (k in 0 until 8) {
            val a = turn + k * 0.785f
            val c = Offset(hub.x + cos(a) * r, hub.y + sin(a) * r)
            inkedRound(Rect(c.x - s * 0.06f, c.y, c.x + s * 0.06f, c.y + s * 0.09f), s * 0.025f, night(cols[k % cols.size], 0.4f), pen, shade = false)
        }
        inkedCircle(hub, s * 0.05f, night(Color(0xFFFFC83D), 0.4f), pen, shade = false)
    }

    /** The shop: a bright box with a big window, a striped awning and a basket sign. */
    private fun shop(d: DrawScope, b: Offset) = with(d) {
        val body = Rect(b.x - s * 0.4f, b.y - s * 0.5f, b.x + s * 0.4f, b.y)
        box3d(body, s * 0.4f, night(Color(0xFFF7F4EF), 0.4f), pen, top = night(Color(0xFFB9C0CC), 0.4f))
        val lit = ramp((n - 0.35f) / 0.4f)
        val glass = lerp(night(Color(0xFF9FD3F2), 0.4f), Color(0xFFFFE9A0), lit)
        drawRect(glass, Offset(body.left + s * 0.07f, body.top + s * 0.2f), Size(s * 0.42f, s * 0.24f))
        drawRect(Ink.line, Offset(body.left + s * 0.07f, body.top + s * 0.2f), Size(s * 0.42f, s * 0.24f), style = pen.thin)
        drawRect(night(Color(0xFF2FB57A), 0.4f), Offset(body.right - s * 0.26f, body.top + s * 0.22f), Size(s * 0.18f, s * 0.28f))
        drawRect(Ink.line, Offset(body.right - s * 0.26f, body.top + s * 0.22f), Size(s * 0.18f, s * 0.28f), style = pen.thin)
        val aw = Path().apply { poly(body.left - s * 0.02f, body.top + s * 0.1f, body.right + s * 0.02f, body.top + s * 0.1f, body.right + s * 0.06f, body.top + s * 0.2f, body.left - s * 0.06f, body.top + s * 0.2f) }
        drawPath(aw, night(Color.White, 0.4f))
        clipPath(aw) { for (k in 0 until 6) drawRect(night(Color(0xFF2FB57A), 0.4f), Offset(body.left - s * 0.06f + k * s * 0.16f, body.top + s * 0.08f), Size(s * 0.08f, s * 0.14f)) }
        drawPath(aw, Ink.line, style = pen.thin)
        val sign = Offset(b.x, body.top - s * 0.14f)
        inkedCircle(sign, s * 0.12f, night(Color.White, 0.4f), pen, shade = false)
        drawArc(Ink.line, 180f, 180f, false, Offset(sign.x - s * 0.045f, sign.y - s * 0.07f), Size(s * 0.09f, s * 0.09f), style = Stroke(pen.lw))
        val basket = Path().apply { poly(sign.x - s * 0.07f, sign.y - s * 0.02f, sign.x + s * 0.07f, sign.y - s * 0.02f, sign.x + s * 0.05f, sign.y + s * 0.06f, sign.x - s * 0.05f, sign.y + s * 0.06f) }
        drawPath(basket, night(Color(0xFF3E7BD6), 0.4f))
    }

    /** The doctor's: a white clinic with a friendly heart sign. */
    private fun clinic(d: DrawScope, b: Offset) = with(d) {
        drawHouse3d(b.x, b.y, s * 0.72f, s * 0.46f, s * 0.26f, s * 0.42f, Color(0xFFF7F7F4), Color(0xFFF08CA8), pen, n, door = Color(0xFF7FD3B0), sideWindows = 2)
        val c = Offset(b.x + s * 0.5f, b.y - s * 0.7f)
        drawLine(Ink.line, Offset(c.x, c.y + s * 0.12f), Offset(c.x, b.y - s * 0.44f), strokeWidth = pen.lw * 1.2f)
        val hs = s * 0.14f
        val heart = Path().apply {
            moveTo(c.x, c.y + hs * 0.95f)
            cubicTo(c.x - hs * 1.6f, c.y - hs * 0.05f, c.x - hs * 0.7f, c.y - hs * 1.3f, c.x, c.y - hs * 0.45f)
            cubicTo(c.x + hs * 0.7f, c.y - hs * 1.3f, c.x + hs * 1.6f, c.y - hs * 0.05f, c.x, c.y + hs * 0.95f)
            close()
        }
        inked(heart, night(Color(0xFFE94F6A), 0.3f), pen, shade = false)
        drawLine(Color.White, Offset(c.x - hs * 0.35f, c.y), Offset(c.x + hs * 0.35f, c.y), strokeWidth = hs * 0.22f, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(c.x, c.y - hs * 0.35f), Offset(c.x, c.y + hs * 0.35f), strokeWidth = hs * 0.22f, cap = StrokeCap.Round)
    }

    /** The concert house: round, with a pointed roof, glowing windows, a star and notes floating up. */
    private fun concertHouse(d: DrawScope, b: Offset) = with(d) {
        val rx = s * 0.42f
        val ry = s * 0.12f
        val top = b.y - s * 0.46f
        val wall = night(Color(0xFFE9DDFB), 0.4f)
        val body = Path().apply {
            moveTo(b.x - rx, top)
            lineTo(b.x - rx, b.y)
            arcTo(Rect(b.x - rx, b.y - ry, b.x + rx, b.y + ry), 180f, -180f, false)
            lineTo(b.x + rx, top)
            close()
        }
        drawPath(body, Brush.horizontalGradient(listOf(wall.lighten(0.2f), wall, wall.darken(0.2f)), startX = b.x - rx, endX = b.x + rx))
        drawPath(body, Ink.line, style = pen.stroke)
        val lit = ramp((n - 0.3f) / 0.4f)
        val win = lerp(night(Color(0xFF8C6BD9), 0.4f), Color(0xFFFFD66B), lit)
        for (k in 0 until 5) {
            val wx = b.x - rx * 0.8f + k * rx * 0.4f
            drawRoundRect(win, Offset(wx - s * 0.03f, top + s * 0.14f), Size(s * 0.06f, s * 0.16f), androidx.compose.ui.geometry.CornerRadius(s * 0.03f))
        }
        val roof = Path().apply {
            moveTo(b.x - rx * 1.08f, top + ry * 0.2f)
            quadraticTo(b.x, top + ry * 1.8f, b.x + rx * 1.08f, top + ry * 0.2f)
            lineTo(b.x, top - s * 0.42f)
            close()
        }
        inked(roof, night(Color(0xFF8B5CF6), 0.4f), pen)
        val star = starPath(Offset(b.x, top - s * 0.5f), s * 0.1f, s * 0.045f, sin(t) * 10f)
        inked(star, Pal.sun, pen, shade = false)
        for (k in 0 until 2) {
            val ph = wrap(t * 0.3f + k * 0.5f, 1f)
            val nc = Offset(b.x + s * (0.5f + 0.12f * sin(ph * 6f + k)), top - s * (0.1f + ph * 0.5f))
            drawCircle(Ink.line, s * 0.035f, nc, alpha = 1f - ph)
            drawLine(Ink.line, Offset(nc.x + s * 0.03f, nc.y), Offset(nc.x + s * 0.03f, nc.y - s * 0.12f), strokeWidth = pen.lw, alpha = 1f - ph)
        }
    }

    /** The sea floor dive spot: a bobbing buoy with a flag, a periscope peeking up and rising bubbles. */
    private fun diveSpot(d: DrawScope, b: Offset) = with(d) {
        val bob = sin(t * 1.6f) * s * 0.03f
        for (k in 0 until 2) {
            val ph = wrap(t * 0.4f + k * 0.5f, 1f)
            val rw = s * (0.4f + ph * 0.6f)
            drawOval(Color.White, Offset(b.x - rw, b.y - rw * 0.18f), Size(rw * 2f, rw * 0.36f), alpha = 0.6f * (1f - ph), style = Stroke(pen.lw))
        }
        for (k in 0 until 4) {
            val ph = wrap(t * 0.5f + k / 4f, 1f)
            drawCircle(Color.White, s * (0.025f + 0.02f * ph), Offset(b.x + s * (0.28f + 0.05f * sin(ph * 8f + k)), b.y - s * 0.02f - ph * s * 0.35f), alpha = 0.8f * (1f - ph), style = Stroke(pen.lw))
        }
        // Periscope.
        val px = b.x - s * 0.34f
        val look = sin(t * 0.5f)
        drawLine(Ink.line, Offset(px, b.y + s * 0.02f), Offset(px, b.y - s * 0.36f), strokeWidth = s * 0.06f + pen.lw * 2f, cap = StrokeCap.Round)
        drawLine(night(Color(0xFFFFC83D), 0.4f), Offset(px, b.y + s * 0.02f), Offset(px, b.y - s * 0.36f), strokeWidth = s * 0.06f, cap = StrokeCap.Round)
        val head = Rect(px - s * 0.04f + look * s * 0.04f, b.y - s * 0.44f, px + s * 0.1f + look * s * 0.04f, b.y - s * 0.34f)
        inkedRound(head, s * 0.03f, night(Color(0xFFFFC83D), 0.4f), pen, shade = false)
        drawCircle(night(Color(0xFF5AA9E6), 0.3f), s * 0.025f, Offset(head.right - s * 0.03f, head.center.y))
        // Buoy.
        val c = Offset(b.x, b.y + bob)
        val float = Path().apply {
            moveTo(c.x - s * 0.22f, c.y)
            quadraticTo(c.x - s * 0.24f, c.y - s * 0.3f, c.x, c.y - s * 0.34f)
            quadraticTo(c.x + s * 0.24f, c.y - s * 0.3f, c.x + s * 0.22f, c.y)
            quadraticTo(c.x, c.y + s * 0.08f, c.x - s * 0.22f, c.y)
            close()
        }
        drawPath(float, night(Color.White, 0.4f))
        clipPath(float) { drawRect(night(Color(0xFFE94F4F), 0.4f), Offset(c.x - s * 0.3f, c.y - s * 0.22f), Size(s * 0.6f, s * 0.1f)) }
        drawPath(float, Ink.line, style = pen.stroke)
        drawLine(Ink.line, Offset(c.x, c.y - s * 0.34f), Offset(c.x, c.y - s * 0.7f), strokeWidth = pen.lw * 1.2f)
        val flag = Path().apply { poly(c.x, c.y - s * 0.7f, c.x + s * 0.24f + sin(t * 4f) * s * 0.02f, c.y - s * 0.62f, c.x, c.y - s * 0.54f) }
        inked(flag, night(Color(0xFFE94F4F), 0.4f), pen, shade = false)
        drawLine(Color.White, Offset(c.x + s * 0.02f, c.y - s * 0.66f), Offset(c.x + s * 0.16f, c.y - s * 0.6f), strokeWidth = pen.lw * 1.4f)
        val lit = ramp((n - 0.3f) / 0.4f)
        if (lit > 0f && wrap(t, 1.4f) < 0.5f) drawCircle(Color(0xFFFFE27A), s * 0.06f, Offset(c.x, c.y - s * 0.72f), alpha = lit)
    }

    private fun cafe(d: DrawScope, b: Offset) = with(d) {
        drawHouse3d(b.x, b.y, s * 0.72f, s * 0.42f, s * 0.28f, s * 0.45f, Color(0xFF9FE0C4), Color(0xFFFF8A7A), pen, n, door = Color(0xFFFFF5E4), sideWindows = 1)
        val aw = Path().apply { poly(b.x - s * 0.38f, b.y - s * 0.3f, b.x + s * 0.38f, b.y - s * 0.3f, b.x + s * 0.44f, b.y - s * 0.2f, b.x - s * 0.44f, b.y - s * 0.2f) }
        drawPath(aw, night(Color.White, 0.4f))
        clipPath(aw) {
            for (k in 0 until 5) drawRect(night(Color(0xFFFF6B8A), 0.4f), Offset(b.x - s * 0.44f + k * s * 0.18f, b.y - s * 0.31f), Size(s * 0.09f, s * 0.12f))
        }
        drawPath(aw, Ink.line, style = pen.thin)
        val sign = Offset(b.x - s * 0.5f, b.y - s * 0.62f)
        inkedCircle(sign, s * 0.12f, night(Color(0xFFFFF5E4), 0.4f), pen, shade = false)
        drawCircle(night(Color(0xFFE9A0C0), 0.4f), s * 0.05f, Offset(sign.x, sign.y - s * 0.02f))
        drawRect(night(Color(0xFFC98440), 0.4f), Offset(sign.x - s * 0.045f, sign.y + s * 0.01f), Size(s * 0.09f, s * 0.05f))
    }

    private fun salon(d: DrawScope, b: Offset) = with(d) {
        drawHouse3d(b.x, b.y, s * 0.68f, s * 0.44f, s * 0.3f, s * 0.45f, Color(0xFFFFC6DC), Color(0xFF9A7BD6), pen, n, door = Color(0xFFB69AE6), sideWindows = 1)
        val sign = Offset(b.x + s * 0.52f, b.y - s * 0.62f)
        drawLine(Ink.line, Offset(sign.x - s * 0.12f, sign.y), Offset(sign.x - s * 0.2f, sign.y), strokeWidth = pen.lw)
        inkedCircle(sign, s * 0.12f, night(Color.White, 0.4f), pen, shade = false)
        val sc = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round)
        drawCircle(Ink.line, s * 0.025f, Offset(sign.x - s * 0.04f, sign.y + s * 0.045f), style = sc)
        drawCircle(Ink.line, s * 0.025f, Offset(sign.x + s * 0.04f, sign.y + s * 0.045f), style = sc)
        drawLine(Ink.line, Offset(sign.x - s * 0.025f, sign.y + s * 0.02f), Offset(sign.x + s * 0.05f, sign.y - s * 0.07f), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset(sign.x + s * 0.025f, sign.y + s * 0.02f), Offset(sign.x - s * 0.05f, sign.y - s * 0.07f), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
    }

    private fun beach(d: DrawScope, b: Offset) = with(d) {
        drawOval(night(Color(0xFFF1D9A2)), Offset(b.x - s * 0.7f, b.y - s * 0.12f), Size(s * 1.4f, s * 0.24f))
        drawHouse3d(b.x - s * 0.18f, b.y, s * 0.5f, s * 0.3f, s * 0.22f, s * 0.35f, Color(0xFFA9D3F0), Color(0xFFF7F3EC), pen, n, door = Color(0xFF3E6FA8), sideWindows = 0)
        val top = Offset(b.x + s * 0.42f, b.y - s * 0.55f)
        drawLine(Ink.line, top, Offset(top.x, b.y), strokeWidth = pen.lw * 1.2f)
        val dome = Path().apply {
            moveTo(top.x - s * 0.3f, top.y + s * 0.12f)
            quadraticTo(top.x, top.y - s * 0.22f, top.x + s * 0.3f, top.y + s * 0.12f)
            close()
        }
        drawPath(dome, night(Color.White, 0.4f))
        clipPath(dome) {
            for (k in 0 until 3) drawRect(night(Color(0xFFE94F4F), 0.4f), Offset(top.x - s * 0.3f + k * s * 0.2f, top.y - s * 0.2f), Size(s * 0.1f, s * 0.35f))
        }
        drawPath(dome, Ink.line, style = pen.thin)
    }

    private fun camp(d: DrawScope, b: Offset) = with(d) {
        val tent = Path().apply { poly(b.x - s * 0.4f, b.y, b.x - s * 0.05f, b.y - s * 0.6f, b.x + s * 0.3f, b.y) }
        prismTent(this, tent, b)
        val fire = Offset(b.x + s * 0.5f, b.y)
        val logs = ArrayList<Offset>(4)
        logs.add(Offset(fire.x - s * 0.12f, fire.y))
        logs.add(Offset(fire.x + s * 0.12f, fire.y - s * 0.04f))
        logs.add(Offset(fire.x + s * 0.12f, fire.y))
        logs.add(Offset(fire.x - s * 0.12f, fire.y - s * 0.04f))
        drawPoints(logs, PointMode.Lines, night(Color(0xFF7A4B2E), 0.4f), strokeWidth = s * 0.04f, cap = StrokeCap.Round)
        val fl = 1f + 0.15f * sin(t * 11f)
        val flame = Path().apply {
            moveTo(fire.x - s * 0.08f, fire.y - s * 0.03f)
            quadraticTo(fire.x - s * 0.1f, fire.y - s * 0.2f * fl, fire.x, fire.y - s * 0.3f * fl)
            quadraticTo(fire.x + s * 0.1f, fire.y - s * 0.2f * fl, fire.x + s * 0.08f, fire.y - s * 0.03f)
            close()
        }
        drawCircle(Color(0xFFFFB84D), s * 0.3f, Offset(fire.x, fire.y - s * 0.12f), alpha = 0.25f + 0.3f * n)
        inked(flame, Color(0xFFFF8A3D), pen, shade = false)
        drawCircle(Color(0xFFFFE066), s * 0.035f, Offset(fire.x, fire.y - s * 0.08f))
    }

    private fun prismTent(d: DrawScope, front: Path, b: Offset) = with(d) {
        val depth = s * 0.5f
        val vx = Oblique.DX * depth
        val vy = Oblique.DY * depth
        val roof = Path().apply { poly(b.x - s * 0.05f, b.y - s * 0.6f, b.x - s * 0.05f + vx, b.y - s * 0.6f + vy, b.x + s * 0.3f + vx, b.y + vy, b.x + s * 0.3f, b.y) }
        drawPath(roof, night(Color(0xFFE8742F), 0.4f))
        drawPath(roof, Ink.line, style = pen.thin)
        drawPath(front, night(Color(0xFFFF9A4D), 0.4f))
        drawPath(Path().apply { poly(b.x - s * 0.05f, b.y - s * 0.45f, b.x + s * 0.08f, b.y, b.x - s * 0.18f, b.y) }, night(Color(0xFF7A3E1E), 0.3f))
        drawPath(front, Ink.line, style = pen.stroke)
    }

    private fun cave(d: DrawScope, b: Offset) = with(d) {
        val mound = Path().apply {
            moveTo(b.x - s * 0.6f, b.y)
            quadraticTo(b.x - s * 0.55f, b.y - s * 0.7f, b.x, b.y - s * 0.75f)
            quadraticTo(b.x + s * 0.6f, b.y - s * 0.7f, b.x + s * 0.62f, b.y)
            close()
        }
        inked(mound, night(Color(0xFF7D7390), 0.5f), pen)
        val glow = 0.6f + 0.4f * sin(t * 1.6f)
        val mouth = Path().apply {
            moveTo(b.x - s * 0.3f, b.y)
            quadraticTo(b.x - s * 0.3f, b.y - s * 0.48f, b.x, b.y - s * 0.48f)
            quadraticTo(b.x + s * 0.3f, b.y - s * 0.48f, b.x + s * 0.3f, b.y)
            close()
        }
        drawPath(mouth, Color(0xFF231A3E))
        drawCircle(Brush.radialGradient(listOf(Color(0xFF6FF2FF).copy(alpha = 0.6f * glow), Color(0x006FF2FF)), center = Offset(b.x, b.y - s * 0.15f), radius = s * 0.4f), s * 0.4f, Offset(b.x, b.y - s * 0.15f))
        for ((k, col) in listOf(Color(0xFF6FF2FF), Color(0xFFFF7BD8), Color(0xFFB58CFF)).withIndex()) {
            val cx = b.x + (k - 1) * s * 0.12f
            val hgt = s * (0.2f + 0.08f * (k % 2))
            val cr = Path().apply { poly(cx - s * 0.04f, b.y, cx - s * 0.04f, b.y - hgt * 0.7f, cx, b.y - hgt, cx + s * 0.04f, b.y - hgt * 0.7f, cx + s * 0.04f, b.y) }
            drawPath(cr, col)
            drawPath(cr, Ink.line, style = pen.thin)
        }
        drawPath(mouth, Ink.line, style = pen.stroke)
        twinkle(Offset(b.x + s * 0.4f, b.y - s * 0.55f), s * 0.08f, Color.White, glow)
    }

    private fun cabin(d: DrawScope, b: Offset) = with(d) {
        val cable = Path().apply {
            moveTo(b.x + s * 0.3f, b.y - s * 0.35f)
            quadraticTo(b.x - s * 0.1f, b.y - s * 0.75f, b.x - s * 0.55f, b.y - s * 1.1f)
        }
        drawPath(cable, Ink.line, style = pen.thin)
        drawLine(Ink.line, Offset(b.x - s * 0.2f, b.y - s * 0.78f), Offset(b.x - s * 0.2f, b.y - s * 0.4f), strokeWidth = pen.lw * 1.4f)
        val ch = Offset(b.x - s * 0.02f, b.y - s * 0.66f + sin(t) * s * 0.02f)
        drawLine(Ink.line, ch, Offset(ch.x, ch.y + s * 0.12f), strokeWidth = pen.lw)
        drawRoundRect(Color(0xFFE24C4C), Offset(ch.x - s * 0.06f, ch.y + s * 0.1f), Size(s * 0.12f, s * 0.05f), CornerRadius(s * 0.015f))
        drawHouse3d(b.x + s * 0.1f, b.y, s * 0.58f, s * 0.34f, s * 0.3f, s * 0.42f, Color(0xFF9A6A48), Pal.snow, pen, n, door = Color(0xFF6E4A33), sideWindows = 1, chimney = true, t = t)
    }

    private fun barn(d: DrawScope, b: Offset) = with(d) {
        val silo = Rect(b.x + s * 0.42f, b.y - s * 0.78f, b.x + s * 0.64f, b.y)
        box3d(silo, s * 0.14f, night(Color(0xFFB9C0CC), 0.45f), pen, radius = s * 0.06f)
        drawOval(night(Color(0xFF8E97A6), 0.45f), Offset(silo.left, silo.top - s * 0.06f), Size(silo.width, s * 0.12f))
        drawOval(Ink.line, Offset(silo.left, silo.top - s * 0.06f), Size(silo.width, s * 0.12f), style = pen.thin)
        val bw = s * 0.8f
        val pts = floatArrayOf(
            b.x - bw * 0.5f, b.y - s * 0.4f, b.x - bw * 0.38f, b.y - s * 0.62f, b.x, b.y - s * 0.76f,
            b.x + bw * 0.38f, b.y - s * 0.62f, b.x + bw * 0.5f, b.y - s * 0.4f, b.x + bw * 0.5f, b.y, b.x - bw * 0.5f, b.y,
        )
        val red = night(Pal.falun, 0.45f)
        prism(pts, s * 0.45f, red, pen) { i, _ -> if (i in 0..3) night(Color(0xFF55505C), 0.45f) else red.darken(0.22f) }
        val door = Rect(b.x - bw * 0.2f, b.y - s * 0.3f, b.x + bw * 0.2f, b.y)
        drawRect(night(Color(0xFFF7F3EC), 0.45f), door.topLeft, door.size)
        drawLine(red, door.topLeft, Offset(door.right, door.bottom), strokeWidth = pen.lw * 1.3f)
        drawLine(red, Offset(door.right, door.top), Offset(door.left, door.bottom), strokeWidth = pen.lw * 1.3f)
        drawRect(Ink.line, door.topLeft, door.size, style = pen.thin)
        drawRect(night(Color(0xFF3B3040), 0.3f), Offset(b.x - s * 0.06f, b.y - s * 0.56f), Size(s * 0.12f, s * 0.1f))
    }

    /** The space station orbiting high above, with a twinkling trail. */
    private fun station(d: DrawScope, c0: Offset, hl: Boolean) = with(d) {
        val a = t * 0.5f
        val rx = w * 0.035f
        val ry = h * 0.025f
        val c = Offset(c0.x + cos(a) * rx, c0.y + sin(a) * ry)
        for (k in 1..9) {
            val b = a - k * 0.22f
            val q = Offset(c0.x + cos(b) * rx, c0.y + sin(b) * ry)
            val tw = 0.5f + 0.5f * sin(t * 5f + k * 1.3f)
            twinkle(q, s * (0.07f - k * 0.005f), Color(0xFFFFF3B0), (1f - k / 10f) * (0.4f + 0.6f * tw))
        }
        if (hl) drawCircle(Pal.sun, s * 0.45f, c, alpha = 0.35f)
        val body = Rect(c.x - s * 0.18f, c.y - s * 0.12f, c.x + s * 0.18f, c.y + s * 0.12f)
        for (side in intArrayOf(-1, 1)) {
            val panel = Rect(c.x + side * s * 0.24f - s * 0.18f, c.y - s * 0.09f, c.x + side * s * 0.24f + s * 0.18f, c.y + s * 0.09f)
            drawLine(Ink.line, Offset(c.x + side * s * 0.17f, c.y), Offset(c.x + side * s * 0.08f, c.y), strokeWidth = pen.lw * 1.4f)
            drawRect(Color(0xFF3E6FD6), panel.topLeft, panel.size)
            val grid = ArrayList<Offset>(8)
            for (k in 1..3) {
                val gx = panel.left + panel.width * k / 4f
                grid.add(Offset(gx, panel.top))
                grid.add(Offset(gx, panel.bottom))
            }
            grid.add(Offset(panel.left, panel.center.y))
            grid.add(Offset(panel.right, panel.center.y))
            drawPoints(grid, PointMode.Lines, Color(0xFF9CC4FF), strokeWidth = pen.lw * 0.6f)
            drawRect(Ink.line, panel.topLeft, panel.size, style = pen.thin)
        }
        inkedRound(body, s * 0.1f, Color(0xFFE9EEF3), pen)
        drawCircle(Color(0xFF6FD8FF), s * 0.05f, Offset(c.x - s * 0.06f, c.y))
        drawCircle(Ink.line, s * 0.05f, Offset(c.x - s * 0.06f, c.y), style = pen.thin)
        drawLine(Ink.line, Offset(c.x + s * 0.06f, c.y - s * 0.12f), Offset(c.x + s * 0.1f, c.y - s * 0.24f), strokeWidth = pen.lw)
        val blink = if (wrap(t, 1.2f) < 0.35f) 1f else 0.2f
        drawCircle(Color(0xFFFF5A6E), s * 0.03f, Offset(c.x + s * 0.1f, c.y - s * 0.25f), alpha = blink)
    }

    fun clouds(d: DrawScope) = with(d) {
        val body = lerp(if (oc > 0f) Color(0xFFB7C0D0) else Color.White, Color(0xFF4A4488), n * 0.8f)
        val shade = lerp(if (oc > 0f) Color(0xFF8C97AC) else Color(0xFFD6E6F6), Color(0xFF2E2A66), n * 0.8f)
        val count = if (oc > 0f) 5 else 3
        for (i in 0 until count) {
            val span = w * 1.3f
            val x = wrap(hash01(i, 851) * span + t * w * (0.006f + 0.004f * hash01(i, 852)), span) - w * 0.15f
            val y = (0.04f + hash01(i, 853) * 0.1f) * h
            val cs = h * (0.045f + 0.02f * hash01(i, 854))
            val path = cloudPath(x, y, cs)
            translate(cs * 0.07f, cs * 0.13f) { drawPath(path, shade) }
            drawPath(path, body)
        }
    }
}
