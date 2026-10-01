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
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Txt
import app.trollfoss.ui.S
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
    // Mitt hus: the child's own plot (placeholder spot; the builder finds the right one).
    PlaceId.MINE_YARD -> Offset(0.35f, 0.4f)
    PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> Offset(0.5f, 0.5f)
    PlaceId.LAB -> Offset(0.62f, 0.25f)
    PlaceId.SPACE -> Offset(0.85f, 0.19f)
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
    // Storhuset, the big house on its hill right of the waterfall: only the ground floor is on the map (the
    // others are inside the house). The estate is wide: its lane runs left to the cave road, the secret path too.
    PlaceId.MANOR_GROUND -> Offset(0.76f, 0.355f)
    PlaceId.MANOR_UPPER, PlaceId.MANOR_ATTIC, PlaceId.MANOR_CELLAR, PlaceId.MANOR_GARDEN -> Offset(0.5f, 0.5f)
}

/**
 * The village of Trollfoss seen from above at a slight angle, filling [size] (any landscape shape):
 * the great long Heileberget across the back with the waterfall pouring out of it, the river valley
 * with rolling hills and terraced fields, the fjord, and one place at [mapSpot] each, standing in the
 * landscape. The [highlight]ed place glows. [t] drives the animation.
 *
 * This draws everything every frame, which is slow. The map screen caches the still scenery in bitmaps
 * ([rememberMapLayer]) and draws only the moving parts per frame ([drawIslandMapLive]); this entry is
 * for tests and tools, and as a fallback. [tunnel] shows the secret path from the cellar of Storhuset to Trollhola.
 */
fun DrawScope.drawIslandMap(pen: Pen, highlight: PlaceId?, t: Float, tunnel: Boolean = false) {
    val g = mapGeo(size.width, size.height)
    val kit = liveKitFor(g, pen.lw, pen.night, pen.weather, pen.rainbow)
    val live = MapPen(size.width, size.height, pen, t, tunnel)
    drawMapSky(this, g.w, g.h, pen.lw, pen.night, pen.weather, pen.rainbow)
    live.drawLiveSky(this, kit)
    drawMapFront(this, g, pen.lw, pen.night, pen.weather, pen.rainbow, tunnel)
    live.drawLive(this, g, kit, highlight)
}

/** The sky that never moves: the gradient with the sun and moon. Only the top half of the map. */
internal fun drawMapSky(d: DrawScope, w: Float, h: Float, lw: Float, night: Float, weather: Weather, rainbow: Float) {
    // The still layers are drawn at time zero. Nothing in them may depend on time.
    MapPen(w, h, Pen(lw, 0f, night, weather, rainbow), 0f).drawSkyStill(d)
}

/**
 * The scenery that never moves, from the mountains to the vignette. The sky is left empty (transparent)
 * where nothing covers it, so the clouds and the northern lights can pass behind the mountains.
 */
internal fun drawMapFront(d: DrawScope, g: MapGeo, lw: Float, night: Float, weather: Weather, rainbow: Float, tunnel: Boolean = false) {
    val m = MapPen(g.w, g.h, Pen(lw, 0f, night, weather, rainbow), 0f, tunnel)
    m.drawFarWorld(d, g)
    m.drawGround(d, g)
    m.drawWaters(d, g)
    m.drawInfra(d, g)
    m.drawDepthPassStill(d, g)
    m.drawCableStatic(d, g)
    // The secret path is a magic hint laid over the whole scene: it must not hide behind the tower or the trees.
    if (tunnel) m.drawTunnelPath(d, g)
    m.drawFinish(d)
}

/** Everything the map needs for one frame. */
internal class MapPen(val w: Float, val h: Float, val pen: Pen, val t: Float, val tunnel: Boolean = false) {
    val n = pen.night
    val snow = pen.weather == Weather.SNOW
    val rain = pen.weather == Weather.RAIN
    val oc = overcast(pen)
    val lw = pen.lw

    /** The size every building is scaled from: a good deal of the map's height, but never too wide. */
    val S = min(h * 0.15f, w * 0.0825f)

    private val hazeColor = lerp(Color(0xFFCFE4F6), Color(0xFF2E2A66), n)

    fun nt(c: Color, k: Float = 0.55f): Color = c.atNight(n, k)

    private var scratchPath: Path? = null

    /**
     * A path for the little shapes that are different every frame (a gull's wings, a sparkle): the live pass
     * hands over one that lives as long as the map layer, and each shape rewinds and refills it.
     */
    var scratch: Path
        get() = scratchPath ?: Path().also { scratchPath = it }
        set(v) { scratchPath = v }

    /** A colour at night and with the haze of distance: things further up the map are paler and bluer. */
    fun tone(c: Color, yFrac: Float, k: Float = 0.55f): Color {
        val a = c.atNight(n, k)
        val hz = ((0.72f - yFrac) / 0.5f).coerceIn(0f, 1f) * 0.16f
        return if (hz <= 0f) a else lerp(a, hazeColor, hz)
    }
}

// ------------------------------------------------------------------------------------- the sky (still)

/** The sky gradient with the sun and the moon (the sun's rays, the stars, the northern lights, clouds and rainbow are live). */
internal fun MapPen.drawSkyStill(d: DrawScope) = with(d) {
    val top = lerp(lerp(Color(0xFF3B98EA), Color(0xFF8793AB), oc), Color(0xFF0E0B30), n)
    val mid = lerp(lerp(Color(0xFF86C9F5), Color(0xFFB4BFD0), oc), Color(0xFF2A2265), n)
    val low = lerp(lerp(Color(0xFFDDF2FF), Color(0xFFD3D9E3), oc), Color(0xFF5C449E), n)
    drawRect(Brush.verticalGradient(0f to top, 0.45f to mid, 1f to low, startY = 0f, endY = h * 0.46f), Offset.Zero, Size(w, h * 0.5f))
    drawSun(Offset(0.14f * w, 0.055f * h), h * 0.045f, pen, (1f - n / 0.6f).coerceIn(0f, 1f) * (1f - oc), rays = false, color = Color(0xFFFFD447))
    drawMoon(Offset(0.14f * w, 0.055f * h), h * 0.035f, pen, ramp((n - 0.35f) / 0.4f))
}

// ------------------------------------------------------------------------------------- the pass by depth (still)

/** Trees, cottages and landmarks drawn from the back of the map to the front. */
internal fun MapPen.drawDepthPassStill(d: DrawScope, g: MapGeo) {
    val sc = g.scatter
    var ci = 0
    var li = 0
    var stationDone = false
    val order = g.plotOrder
    for (b in 0 until NBANDS) {
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
            drawLandmark(d, g, order[li])
            li++
        }
    }
}

/** A soft vignette round the edges, so the map feels like a framed picture. */
internal fun MapPen.drawFinish(d: DrawScope) = with(d) {
    val r = kotlin.math.hypot(w, h) * 0.5f
    drawRect(
        safeRadialGradient(0f to Color.Transparent, 0.62f to Color.Transparent, 1f to Ink.line.copy(alpha = 0.34f), center = Offset(w / 2f, h / 2f), radius = r),
        Offset.Zero, Size(w, h),
    )
}

/** A cheap stand-in until the real map is ready: sky above, green ground below. */
internal fun DrawScope.drawMapPlaceholder(night: Float, weather: Weather) {
    val oc = overcast(Pen(1f, 0f, night, weather))
    val top = lerp(lerp(Color(0xFF3B98EA), Color(0xFF8793AB), oc), Color(0xFF0E0B30), night)
    val low = lerp(lerp(Color(0xFFDDF2FF), Color(0xFFD3D9E3), oc), Color(0xFF5C449E), night)
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(top, low), startY = 0f, endY = h * 0.46f), Offset.Zero, Size(size.width, h * 0.5f))
    val g0 = lerp(if (weather == Weather.SNOW) Color(0xFFEAF0FA) else Color(0xFF88C66A), Color(0xFF231E5C), night * 0.5f)
    val g1 = lerp(if (weather == Weather.SNOW) Color(0xFFDDE7F5) else Color(0xFF63AE50), Color(0xFF231E5C), night * 0.5f)
    drawRect(Brush.verticalGradient(listOf(g0, g1), startY = h * 0.42f, endY = h), Offset(0f, h * 0.42f), Size(size.width, h * 0.58f))
}

/** On the map the big house is called Storhuset (inside it, the ground floor is the Storstova). */
private val STORHUSET = Txt("Storhuset")

/** The word under a place on the map. */
internal fun mapLabel(place: PlaceId): Txt = if (place == PlaceId.MANOR_GROUND) STORHUSET else S.place(place)
