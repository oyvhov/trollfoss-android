package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Weather
import kotlin.math.min
import kotlin.math.sin

/**
 * Where each place sits on the map, as fractions of the map's width and height. Spots are far enough
 * apart for 90 dp buttons on a landscape phone (0.13 apart across or 0.27 apart up and down). They
 * keep clear of the close button (top left), the gear (top right) and the two round buttons (bottom
 * left). Heileberget, the great long mountain, has its hut right under the crest.
 */
fun mapSpot(place: PlaceId): Offset = when (place) {
    PlaceId.MOUNTAIN -> Offset(0.17f, 0.25f)
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
    PlaceId.HEILEBERGET -> Offset(0.32f, 0.095f)
}

/**
 * The village of Trollfoss seen from above at a slight angle, filling [size] (any landscape shape):
 * the great long Heileberget across the back with the waterfall pouring out of it, the river valley
 * with rolling hills and terraced fields, the fjord, and one place at [mapSpot] each, standing in the
 * landscape. The [highlight]ed place glows and lifts. [t] drives the animation.
 */
fun DrawScope.drawIslandMap(pen: Pen, highlight: PlaceId?, t: Float) {
    val m = MapPen(size.width, size.height, pen, t)
    val g = mapGeo(size.width, size.height)
    m.drawSky(this)
    m.drawFarWorld(this, g)
    m.drawGround(this, g)
    m.drawWaters(this, g)
    m.drawInfra(this, g)
    m.drawDepthPass(this, g, highlight)
    m.drawOverhead(this, g)
    m.drawLife(this, g)
    m.drawWeatherLife(this, g)
    m.drawFinish(this)
}

/** Everything the map needs for one frame. */
internal class MapPen(val w: Float, val h: Float, val pen: Pen, val t: Float) {
    val n = pen.night
    val snow = pen.weather == Weather.SNOW
    val rain = pen.weather == Weather.RAIN
    val oc = overcast(pen)
    val lw = pen.lw

    /** The size every building is scaled from: a good deal of the map's height, but never too wide. */
    val S = min(h * 0.15f, w * 0.0825f)

    private val hazeColor = lerp(Color(0xFFCFE4F6), Color(0xFF2E2A66), n)

    fun nt(c: Color, k: Float = 0.55f): Color = c.atNight(n, k)

    /** A colour at night and with the haze of distance: things further up the map are paler and bluer. */
    fun tone(c: Color, yFrac: Float, k: Float = 0.55f): Color {
        val a = c.atNight(n, k)
        val hz = ((0.72f - yFrac) / 0.5f).coerceIn(0f, 1f) * 0.16f
        return if (hz <= 0f) a else lerp(a, hazeColor, hz)
    }
}

// ------------------------------------------------------------------------------------- the sky

internal fun MapPen.drawSky(d: DrawScope) = with(d) {
    val top = lerp(lerp(Color(0xFF3B98EA), Color(0xFF8793AB), oc), Color(0xFF0E0B30), n)
    val mid = lerp(lerp(Color(0xFF86C9F5), Color(0xFFB4BFD0), oc), Color(0xFF2A2265), n)
    val low = lerp(lerp(Color(0xFFDDF2FF), Color(0xFFD3D9E3), oc), Color(0xFF5C449E), n)
    drawRect(Brush.verticalGradient(0f to top, 0.45f to mid, 1f to low, startY = 0f, endY = h * 0.46f), Offset.Zero, Size(w, h * 0.5f))
    // Stars and the milky glimmer of a clear night.
    val stars = ramp((n - 0.2f) / 0.55f) * (1f - oc * 0.8f)
    if (stars > 0f) {
        for (g in 0 until 3) {
            val pts = ArrayList<Offset>(30)
            for (i in g until 90 step 3) pts.add(Offset(hash01(i, 801) * w, hash01(i, 802) * h * 0.2f))
            drawPoints(pts, PointMode.Points, Color(0xFFFFF7DA), strokeWidth = h * (0.004f + 0.0015f * g), cap = StrokeCap.Round, alpha = stars * (0.6f + 0.4f * sin(t * (1.1f + 0.5f * g) + g * 2f)))
        }
        for (i in 0 until 5) twinkle(Offset(hash01(i, 803) * w, (0.02f + hash01(i, 804) * 0.12f) * h), h * 0.018f, Color(0xFFFFF7DA), stars * (0.5f + 0.5f * sin(t * 1.7f + i * 2f)))
    }
    // The northern lights: three slow curtains with bright hems and faint rays.
    val au = ramp((n - 0.3f) / 0.55f) * (1f - 0.6f * oc)
    if (au > 0.01f) {
        for (c in 0 until 3) {
            val col = when (c) { 0 -> Pal.auroraGreen; 1 -> Pal.auroraViolet; else -> Color(0xFF63F5D9) }
            val k = 14
            val xs = FloatArray(k + 1)
            val tops = FloatArray(k + 1)
            val bots = FloatArray(k + 1)
            for (i in 0..k) {
                val x = -0.05f + 1.1f * i / k
                val tp = 0.015f + c * 0.045f + 0.035f * sin(x * 6.5f + t * 0.3f + c * 1.9f) + 0.015f * sin(x * 13f - t * 0.45f + c)
                xs[i] = x * w
                tops[i] = tp * h
                bots[i] = (tp + 0.1f + 0.045f * sin(x * 5f + t * 0.35f + c * 2.4f)) * h
            }
            val band = Path().apply {
                smoothRun(xs, tops, k + 1, start = true)
                smoothRun(xs, bots, k + 1, start = false, reverse = true)
                close()
            }
            drawPath(band, Brush.verticalGradient(0f to col.copy(alpha = 0f), 0.6f to col.copy(alpha = 0.28f * au), 1f to col.copy(alpha = 0.62f * au), startY = 0f, endY = 0.28f * h))
            drawPath(Path().apply { smoothRun(xs, bots, k + 1, start = true) }, col, alpha = 0.55f * au, style = Stroke(h * 0.004f, cap = StrokeCap.Round))
            val rays = ArrayList<Offset>(40)
            for (j in 0 until 20) {
                val f = (j + 0.5f) / 20f * k
                val i0 = f.toInt().coerceAtMost(k - 1)
                val fr = f - i0
                val rx = mix(xs[i0], xs[i0 + 1], fr) + sin(t * 0.7f + j * 1.3f + c) * h * 0.012f
                rays.add(Offset(rx, mix(mix(tops[i0], tops[i0 + 1], fr), mix(bots[i0], bots[i0 + 1], fr), 0.3f)))
                rays.add(Offset(rx, mix(bots[i0], bots[i0 + 1], fr)))
            }
            drawPoints(rays, PointMode.Lines, col, strokeWidth = h * 0.006f, cap = StrokeCap.Round, alpha = 0.2f * au * (0.6f + 0.4f * sin(t * 1.3f + c)))
        }
    }
    drawSun(Offset(0.14f * w, 0.055f * h), h * 0.045f, pen, (1f - n / 0.6f).coerceIn(0f, 1f) * (1f - oc), color = Color(0xFFFFD447))
    drawMoon(Offset(0.14f * w, 0.055f * h), h * 0.035f, pen, ramp((n - 0.35f) / 0.4f))
    // Clouds drifting across the strip of sky above the crest.
    val body = lerp(if (oc > 0f) Color(0xFFB7C0D0) else Color.White, Color(0xFF4A4488), n * 0.8f)
    val shade = lerp(if (oc > 0f) Color(0xFF8C97AC) else Color(0xFFD6E6F6), Color(0xFF2E2A66), n * 0.8f)
    val count = if (oc > 0f) 6 else 4
    for (i in 0 until count) {
        val span = w * 1.3f
        val x = wrap(hash01(i, 851) * span + t * w * (0.005f + 0.004f * hash01(i, 852)), span) - w * 0.15f
        val y = (0.02f + hash01(i, 853) * 0.09f) * h
        val cs = h * (0.04f + 0.02f * hash01(i, 854))
        val path = cloudPath(x, y, cs)
        translate(cs * 0.07f, cs * 0.13f) { drawPath(path, shade) }
        drawPath(path, body)
    }
    if (pen.rainbow > 0.01f) drawRainbow(Offset(0.5f * w, 0.5f * h), h * 0.44f, h * 0.014f, pen.rainbow)
}

// ------------------------------------------------------------------------------------- the pass by depth

/** Trees, cottages, landmarks and the people between them, drawn from the back of the map to the front. */
internal fun MapPen.drawDepthPass(d: DrawScope, g: MapGeo, hl: PlaceId?) {
    val sc = g.scatter
    var ci = 0
    var li = 0
    var stationDone = false
    val order = g.plotOrder
    for (b in 0 until NBANDS) {
        val yMin = b * BAND_H * h
        val yMax = (b + 1) * BAND_H * h
        drawTreeBand(d, sc.bands[b])
        while (ci < sc.cottages.size && sc.cottages[ci].y < yMax) {
            drawCottage(d, sc.cottages[ci])
            ci++
        }
        if (!stationDone && g.cableBottom.y < yMax) {
            drawCableStation(d, g)
            stationDone = true
        }
        while (li < order.size && g.bases[order[li]]!!.y < yMax) {
            drawLandmark(d, g, order[li], order[li] == hl)
            li++
        }
        drawMovers(d, g, yMin, yMax)
    }
}

/** Things that hang in the air in front of everything: cable-car cables, cabins and the eagle. */
internal fun MapPen.drawOverhead(d: DrawScope, g: MapGeo) {
    drawCableCar(d, g)
    drawEagle(d)
}

/** A soft vignette round the edges, so the map feels like a framed picture. */
internal fun MapPen.drawFinish(d: DrawScope) = with(d) {
    val r = kotlin.math.hypot(w, h) * 0.5f
    drawRect(
        safeRadialGradient(0f to Color.Transparent, 0.62f to Color.Transparent, 1f to Ink.line.copy(alpha = 0.34f), center = Offset(w / 2f, h / 2f), radius = r),
        Offset.Zero, Size(w, h),
    )
}
