package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Festival
import app.trollfoss.domain.MineHouse
import app.trollfoss.domain.Season
import app.trollfoss.domain.Weather
import kotlin.math.ceil
import kotlin.math.max

/*
 * The map is split in layers. The still scenery (mountains, terrain, water, roads, trees and houses:
 * thousands of shapes) is drawn once into a bitmap when the size, the time of day or the weather changes,
 * and so is the plain sky behind it. Every frame then draws, from back to front:
 *   the sky bitmap, what moves in the sky (clouds, stars, northern lights), the scenery bitmap (transparent
 *   where the sky shows through, so the clouds pass behind the mountains), and what moves on the ground.
 */

/**
 * The cached still layers of the map for one size, time of day and weather, with what the live layer
 * needs to draw over them. [image] is null if the bitmaps could not be made; the map is then drawn the slow way.
 */
class MapLayer internal constructor(
    internal val w: Int,
    internal val h: Int,
    internal val night: Float,
    internal val weather: Weather,
    internal val rainbow: Float,
    /** The secret path from the cellar of Storhuset to Trollhola is shown (all five golden keys found). */
    internal val tunnel: Boolean,
    internal val lw: Float,
    internal val sky: ImageBitmap?,
    internal val image: ImageBitmap?,
    internal val g: MapGeo,
    internal val kit: LiveKit,
    internal val mine: MineHouse,
    internal val season: Season = Season.SUMMER,
    internal val festival: Festival = Festival.NONE,
)

/** The pen width for a map [heightPx] tall: the same on every screen. */
internal fun mapPenWidth(heightPx: Float): Float = max(1.4f, heightPx * 0.0034f)

/** Draws the still layers into new bitmaps. Slow (a second or two on a phone): call it off the main thread. */
internal fun buildMapLayer(w: Int, h: Int, night: Float, weather: Weather, rainbow: Float, tunnel: Boolean = false, mine: MineHouse = MineHouse(), season: Season = Season.SUMMER, festival: Festival = Festival.NONE): MapLayer {
    val g = mapGeo(w.toFloat(), h.toFloat())
    val lw = mapPenWidth(h.toFloat())
    val kit = LiveKit(g, lw, night, weather, rainbow, season)
    var sky: ImageBitmap? = null
    var image: ImageBitmap? = null
    try {
        val size = Size(w.toFloat(), h.toFloat())
        val skyBitmap = ImageBitmap(w, ceil(h * 0.5f).toInt() + 1)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(skyBitmap), size) {
            drawMapSky(this, g.w, g.h, lw, night, weather, rainbow, season)
        }
        val bitmap = ImageBitmap(w, h)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), size) {
            drawMapFront(this, g, lw, night, weather, rainbow, tunnel, mine, season, festival)
        }
        kit.findHiddenRiver(bitmap)
        sky = skyBitmap
        image = bitmap
    } catch (e: Throwable) {
        sky = null
        image = null
    }
    return MapLayer(w, h, night, weather, rainbow, tunnel, lw, sky, image, g, kit, mine, season, festival)
}

/**
 * Draws the map from its cached [layer]: the bitmaps of the still scenery and everything that moves in
 * between and on top, and the glow of the [highlight]ed place. Time of day, weather and season come from the layer.
 * Until the layer is ready a plain sky and ground are drawn; if the bitmaps could not be made, the whole map
 * is drawn live.
 */
fun DrawScope.drawIslandMapLive(pen: Pen, highlight: PlaceId?, t: Float, layer: MapLayer?) {
    if (layer == null || layer.w.toFloat() != size.width || layer.h.toFloat() != size.height) {
        drawMapPlaceholder(pen.night, pen.weather)
        return
    }
    val sky = layer.sky
    val image = layer.image
    if (sky == null || image == null) {
        drawIslandMap(Pen(pen.lw, pen.t, layer.night, layer.weather, layer.rainbow, layer.season, layer.festival), highlight, t, layer.tunnel, layer.mine)
        return
    }
    val live = MapPen(layer.w.toFloat(), layer.h.toFloat(), Pen(layer.lw, t, layer.night, layer.weather, layer.rainbow, layer.season, layer.festival), t, layer.tunnel, layer.mine)
    drawImage(sky)
    live.drawLiveSky(this, layer.kit)
    drawImage(image)
    live.drawLive(this, layer.g, layer.kit, highlight)
}
