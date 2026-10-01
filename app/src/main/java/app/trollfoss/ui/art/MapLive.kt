package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Weather
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/*
 * The live layer of the village map: everything that moves, drawn every frame over the cached still
 * layer (see MapLayer.kt). The shapes and brushes that never change are built once in [LiveKit]; the
 * few paths that follow the clock are rewound and refilled instead of being reallocated.
 */

/** A drifting sky cloud: its shape at the origin and where it travels. */
internal class SkyCloud(val path: Path, val x0: Float, val speed: Float, val y: Float, val shadeDx: Float, val shadeDy: Float)

/** One curtain of the northern lights: reusable paths and a brush that never changes. */
internal class AuroraBand(val color: Color, val brush: Brush) {
    val band = Path()
    val hem = Path()
    val rays = Path()
}

/**
 * What the live layer needs besides the map geometry, for one size, night and weather: colours,
 * prebuilt shapes, clip regions and scratch paths.
 */
internal class LiveKit(val g: MapGeo, val lw: Float, val night: Float, val weather: Weather, val rainbow: Float) {
    val w = g.w
    val h = g.h
    private val pen = Pen(lw, 0f, night, weather, rainbow)
    val oc = overcast(pen)
    val stars = ramp((night - 0.2f) / 0.55f) * (1f - oc * 0.8f)
    val aurora = ramp((night - 0.3f) / 0.55f) * (1f - 0.6f * oc)
    val sunVis = (1f - night / 0.6f).coerceIn(0f, 1f) * (1f - oc)

    // ---- sky clouds
    val cloudBody = lerp(if (oc > 0f) Color(0xFFB7C0D0) else Color.White, Color(0xFF4A4488), night * 0.8f)
    val cloudShade = lerp(if (oc > 0f) Color(0xFF8C97AC) else Color(0xFFD6E6F6), Color(0xFF2E2A66), night * 0.8f)
    val clouds: List<SkyCloud> = List(if (oc > 0f) 6 else 4) { i ->
        val cs = h * (0.04f + 0.02f * hash01(i, 854))
        SkyCloud(
            cloudPath(0f, 0f, cs), hash01(i, 851) * w * 1.3f, w * (0.005f + 0.004f * hash01(i, 852)),
            (0.02f + hash01(i, 853) * 0.09f) * h, cs * 0.07f, cs * 0.13f,
        )
    }

    // ---- the stars of a clear night (the mountains hide the ones behind them) and the few that twinkle
    val starGroups: List<List<Offset>> = List(3) { g0 ->
        val pts = ArrayList<Offset>(30)
        for (i in g0 until 90 step 3) pts.add(Offset(hash01(i, 801) * w, hash01(i, 802) * h * 0.2f))
        pts
    }
    val twinkles: List<Path> = List(5) { i -> twinklePath(hash01(i, 803) * w, (0.02f + hash01(i, 804) * 0.12f) * h, h * 0.018f) }

    private fun twinklePath(cx: Float, cy: Float, r: Float): Path = Path().apply {
        moveTo(cx, cy - r)
        quadraticTo(cx, cy, cx + r, cy)
        quadraticTo(cx, cy, cx, cy + r)
        quadraticTo(cx, cy, cx - r, cy)
        quadraticTo(cx, cy, cx, cy - r)
        close()
    }

    // ---- the northern lights
    val auroras = Array(3) { c ->
        val col = when (c) { 0 -> Pal.auroraGreen; 1 -> Pal.auroraViolet; else -> Color(0xFF63F5D9) }
        AuroraBand(col, Brush.verticalGradient(0f to col.copy(alpha = 0f), 0.6f to col.copy(alpha = 0.28f * aurora), 1f to col.copy(alpha = 0.62f * aurora), startY = 0f, endY = 0.28f * h))
    }
    val auroraXs = FloatArray(AURORA_K + 1) { (-0.05f + 1.1f * it / AURORA_K) * w }
    val auroraTops = FloatArray(AURORA_K + 1)
    val auroraBots = FloatArray(AURORA_K + 1)

    // ---- scratch paths and strokes that follow the clock
    val sunRays = Path()
    val fallWhite = Path()
    val fallBlue = Path()
    val veilLines = Path()
    val flow = Path()
    val strokeFallWhite = Stroke(lw * 1.5f, cap = StrokeCap.Round)
    val strokeFallBlue = Stroke(lw * 2.2f, cap = StrokeCap.Round)
    val strokeFlow = Stroke(lw * 1.3f, cap = StrokeCap.Round)
    val strokeWaves = Stroke(lw * 1.1f, cap = StrokeCap.Round)

    /** For [MapPen.scratch]. */
    val scratch = Path()

    // ---- the sailing boat and the far-away balloon: shaped once, moved every frame
    val boatK = h * 0.05f
    val boatHull = Path().apply { poly(-boatK * 0.5f, -boatK * 0.18f, boatK * 0.5f, -boatK * 0.18f, boatK * 0.34f, 0f, -boatK * 0.34f, 0f) }
    val boatSail = Path().apply { poly(boatK * 0.03f, -boatK * 1.0f, boatK * 0.44f, -boatK * 0.25f, boatK * 0.03f, -boatK * 0.25f) }
    val boatJib = Path().apply { poly(-boatK * 0.03f, -boatK * 0.86f, -boatK * 0.03f, -boatK * 0.25f, -boatK * 0.38f, -boatK * 0.25f) }
    val strokeThin = Stroke(lw)
    val strokeHull = Stroke(lw * 1.2f, join = StrokeJoin.Round)
    private val balloonR = h * 0.03f
    val balloonEnv = Path().apply {
        val r = balloonR
        moveTo(0f, r * 0.9f)
        cubicTo(-r * 1.25f, r * 0.3f, -r * 1.05f, -r * 1.05f, 0f, -r * 1.05f)
        cubicTo(r * 1.05f, -r * 1.05f, r * 1.25f, r * 0.3f, 0f, r * 0.9f)
        close()
    }
    val balloonStripe = Path().apply {
        val r = balloonR
        moveTo(0f, -r * 1.05f)
        cubicTo(r * 0.5f, -r * 0.5f, r * 0.5f, r * 0.3f, 0f, r * 0.9f)
        cubicTo(r * 1.25f, r * 0.3f, r * 1.05f, -r * 1.05f, 0f, -r * 1.05f)
        close()
    }

    /** The bridges and the viaduct that cross the river. */
    private val bridgeFs = floatArrayOf(g.riverF1, g.riverF2, g.riverF3)

    /**
     * How many of the first i river samples are hidden behind trees, houses or bridges in the still image,
     * for i in 0..RIVER_N; null if that is not known (then the flow lines are drawn along the whole river).
     */
    var riverHidden: IntArray? = null
        private set

    /** Looks at the still [image] along the river to see where the water is covered by something. */
    fun findHiddenRiver(image: ImageBitmap) {
        try {
            val prefix = IntArray(RIVER_N + 2)
            val px = IntArray(1)
            for (i in 0..RIVER_N) {
                val c = g.river.at(i / RIVER_N.toFloat())
                image.readPixels(px, c.x.toInt().coerceIn(0, image.width - 1), c.y.toInt().coerceIn(0, image.height - 1), 1, 1)
                val r = (px[0] shr 16) and 255
                val gr = (px[0] shr 8) and 255
                val b = px[0] and 255
                // Blue enough to be water, and not the pool at the head, the fjord at the mouth or a bridge.
                val water = b - r >= 30 && b >= gr
                val f = i / RIVER_N.toFloat()
                var covered = !water || g.inSea(c.x, c.y) || onPool(c)
                for (fb in bridgeFs) if (kotlin.math.abs(f - fb) < 0.03f) covered = true
                prefix[i + 1] = prefix[i] + if (covered) 1 else 0
            }
            riverHidden = prefix
        } catch (e: Throwable) {
            riverHidden = null
        }
    }

    private fun onPool(c: Offset): Boolean {
        val p = g.pool
        val dx = (c.x - p.center.x) / (p.width * 0.55f)
        val dy = (c.y - p.center.y) / (p.height * 0.55f)
        return dx * dx + dy * dy < 1f
    }

    /** Is the river between fractions [f0] and [f1] of its length covered by something? */
    fun riverCovered(f0: Float, f1: Float): Boolean {
        val p = riverHidden ?: return false
        val i0 = (f0 * RIVER_N).toInt().coerceIn(0, RIVER_N)
        val i1 = kotlin.math.ceil(f1 * RIVER_N).toInt().coerceIn(i0, RIVER_N)
        return p[i1 + 1] - p[i0] > 0
    }

    companion object {
        const val AURORA_K = 14
        const val RIVER_N = 600
        const val TRAIN_CYCLE = 38f
    }
}

private var kitCache: LiveKit? = null

/** A live kit for [g] and these settings, remembered until they change (for [drawIslandMap]). */
internal fun liveKitFor(g: MapGeo, lw: Float, night: Float, weather: Weather, rainbow: Float): LiveKit {
    val c = kitCache
    if (c != null && c.g === g && c.lw == lw && c.night == night && c.weather == weather && c.rainbow == rainbow) return c
    return LiveKit(g, lw, night, weather, rainbow).also { kitCache = it }
}

// ------------------------------------------------------------------------------------- the live pass

/**
 * Draws what moves in the sky, over the still sky and behind the still scenery (which has the mountains
 * and everything else in it, and is transparent where the sky shows through).
 */
internal fun MapPen.drawLiveSky(d: DrawScope, kit: LiveKit) = with(d) {
    if (kit.stars > 0f) {
        for ((g0, pts) in kit.starGroups.withIndex()) {
            drawPoints(pts, PointMode.Points, Color(0xFFFFF7DA), strokeWidth = h * (0.004f + 0.0015f * g0), cap = StrokeCap.Round, alpha = kit.stars * (0.6f + 0.4f * sin(t * (1.1f + 0.5f * g0) + g0 * 2f)))
        }
        for ((i, p) in kit.twinkles.withIndex()) drawPath(p, Color(0xFFFFF7DA), alpha = kit.stars * (0.5f + 0.5f * sin(t * 1.7f + i * 2f)))
    }
    drawAurora(d, kit)
    drawSunRays(d, kit)
    drawCloudsLive(d, kit)
    if (pen.rainbow > 0.01f) drawRainbow(Offset(0.5f * w, 0.5f * h), h * 0.44f, h * 0.014f, pen.rainbow)
}

/** Draws everything that moves, over the still scenery, and the highlight of the chosen place. */
internal fun MapPen.drawLive(d: DrawScope, g: MapGeo, kit: LiveKit, highlight: PlaceId?) {
    scratch = kit.scratch
    // The glow of the chosen place lies on the ground, under everything that moves.
    drawHighlight(d, g, highlight)
    drawFallLive(d, g, kit)
    drawTrollBlink(d)
    drawWatersLive(d, g, kit)
    drawTrain(d, g)
    // The train only goes into the tunnel for a little while of each lap.
    if (wrap(t / LiveKit.TRAIN_CYCLE, 1f) * 1.3f - 0.14f > 0.85f) drawTunnelPortal(d, g)
    for (c in g.scatter.cottages) drawCottageLive(d, c)
    for (p in PlaceId.entries) drawLandmarkLive(d, g, p, p == highlight)
    drawMovers(d, g, 0f, h)
    drawHeileLife(d, g)
    drawCabins(d, g)
    drawEagle(d)
    drawLife(d, g, kit)
    drawWeatherLife(d, g)
}

/** A four-pointed sparkle drawn with the shared scratch path. */
internal fun MapPen.twinkleLive(d: DrawScope, center: Offset, radius: Float, color: Color, alpha: Float = 1f) {
    val p = scratch
    p.rewind()
    p.moveTo(center.x, center.y - radius)
    p.quadraticTo(center.x, center.y, center.x + radius, center.y)
    p.quadraticTo(center.x, center.y, center.x, center.y + radius)
    p.quadraticTo(center.x, center.y, center.x - radius, center.y)
    p.quadraticTo(center.x, center.y, center.x, center.y - radius)
    p.close()
    d.drawPath(p, color.copy(alpha = color.alpha * alpha))
}

/** A gull's two wings as one stroke, drawn with the shared scratch path. */
internal fun MapPen.gullLive(d: DrawScope, c: Offset, s: Float, flap: Float, stroke: Stroke, alpha: Float) {
    val tip = s * (0.05f + 0.25f * flap)
    val p = scratch
    p.rewind()
    p.moveTo(c.x - s, c.y - tip)
    p.quadraticTo(c.x - s * 0.45f, c.y - s * (0.2f + 0.45f * flap), c.x, c.y)
    p.quadraticTo(c.x + s * 0.45f, c.y - s * (0.2f + 0.45f * flap), c.x + s, c.y - tip)
    d.drawPath(p, Ink.line, alpha = 0.85f * alpha, style = stroke)
}

/** The sun's turning rays. */
private fun MapPen.drawSunRays(d: DrawScope, kit: LiveKit) = with(d) {
    if (kit.sunVis <= 0.01f) return@with
    val c = Offset(0.14f * w, 0.055f * h)
    val r = h * 0.045f
    val p = kit.sunRays
    p.rewind()
    val spin = t * 0.12f
    for (i in 0 until 12) {
        val a = spin + i * 0.5235988f
        val l = r * (1.62f + 0.14f * sin(t * 1.8f + i * 1.7f))
        p.moveTo(c.x + cos(a) * r * 1.3f, c.y + sin(a) * r * 1.3f)
        p.lineTo(c.x + cos(a) * l, c.y + sin(a) * l)
    }
    drawPath(p, Color(0xFFFFD447), alpha = kit.sunVis, style = Stroke(r * 0.16f, cap = StrokeCap.Round))
}

/** The northern lights: three slow curtains with bright hems and faint rays. */
private fun MapPen.drawAurora(d: DrawScope, kit: LiveKit) = with(d) {
    val au = kit.aurora
    if (au <= 0.01f) return@with
    val k = LiveKit.AURORA_K
    for ((c, a) in kit.auroras.withIndex()) {
        for (i in 0..k) {
            val x = -0.05f + 1.1f * i / k
            val tp = 0.015f + c * 0.045f + 0.035f * sin(x * 6.5f + t * 0.3f + c * 1.9f) + 0.015f * sin(x * 13f - t * 0.45f + c)
            kit.auroraTops[i] = tp * h
            kit.auroraBots[i] = (tp + 0.1f + 0.045f * sin(x * 5f + t * 0.35f + c * 2.4f)) * h
        }
        a.band.rewind()
        a.band.smoothRun(kit.auroraXs, kit.auroraTops, k + 1, start = true)
        a.band.smoothRun(kit.auroraXs, kit.auroraBots, k + 1, start = false, reverse = true)
        a.band.close()
        drawPath(a.band, a.brush)
        a.hem.rewind()
        a.hem.smoothRun(kit.auroraXs, kit.auroraBots, k + 1, start = true)
        drawPath(a.hem, a.color, alpha = 0.55f * au, style = Stroke(h * 0.004f, cap = StrokeCap.Round))
        a.rays.rewind()
        for (j in 0 until 20) {
            val f = (j + 0.5f) / 20f * k
            val i0 = f.toInt().coerceAtMost(k - 1)
            val fr = f - i0
            val rx = mix(kit.auroraXs[i0], kit.auroraXs[i0 + 1], fr) + sin(t * 0.7f + j * 1.3f + c) * h * 0.012f
            a.rays.moveTo(rx, mix(mix(kit.auroraTops[i0], kit.auroraTops[i0 + 1], fr), mix(kit.auroraBots[i0], kit.auroraBots[i0 + 1], fr), 0.3f))
            a.rays.lineTo(rx, mix(kit.auroraBots[i0], kit.auroraBots[i0 + 1], fr))
        }
        drawPath(a.rays, a.color, alpha = 0.2f * au * (0.6f + 0.4f * sin(t * 1.3f + c)), style = Stroke(h * 0.006f, cap = StrokeCap.Round))
    }
}

/** Sky clouds drifting over the crest, behind the mountains. */
private fun MapPen.drawCloudsLive(d: DrawScope, kit: LiveKit) = with(d) {
    val span = w * 1.3f
    for (c in kit.clouds) {
        val x = wrap(c.x0 + t * c.speed, span) - w * 0.15f
        translate(x, c.y) {
            translate(c.shadeDx, c.shadeDy) { drawPath(c.path, kit.cloudShade) }
            drawPath(c.path, kit.cloudBody)
        }
    }
}

/** The water streaks pouring down the great fall and the thin veil beside the cave. */
private fun MapPen.drawFallLive(d: DrawScope, g: MapGeo, kit: LiveKit) = with(d) {
    val f = g.fall
    val white = kit.fallWhite
    val blue = kit.fallBlue
    white.rewind()
    blue.rewind()
    val span = f.bottom - f.top
    for (i in 0 until 14) {
        val u = -0.88f + 1.76f * (i + 0.5f) / 14f
        val speed = 0.5f + 0.3f * hash01(i, 61)
        for (k in 0 until 3) {
            val y0 = f.top + wrap(t * speed * 0.6f + hash01(i * 3 + k, 62), 1f) * (span + 0.08f * h) - 0.04f * h
            val ya = if (y0 > f.top) y0 else f.top
            val yb = min(y0 + (0.03f + 0.04f * hash01(i * 3 + k, 63)) * h, f.bottom)
            if (yb <= ya) continue
            val hwa = mix(f.tw, f.bw, ((ya - f.top) / span).coerceIn(0f, 1f)) * 0.5f
            val hwb = mix(f.tw, f.bw, ((yb - f.top) / span).coerceIn(0f, 1f)) * 0.5f
            val p = if ((i + k) % 2 == 0) white else blue
            p.moveTo(f.cx + u * hwa, ya)
            p.lineTo(f.cx + u * hwb, yb)
        }
    }
    drawPath(white, Color.White, alpha = 0.9f, style = kit.strokeFallWhite)
    drawPath(blue, nt(Color(0xFF78C4E8), 0.3f), alpha = 0.6f, style = kit.strokeFallBlue)
    val v = kit.veilLines
    v.rewind()
    for (i in 0 until 5) {
        val x = f.veilX + (i - 2) * 0.0035f * w
        val y0 = f.veilTop + wrap(t * 0.5f + hash01(i, 91), 1f) * (f.veilBottom - f.veilTop) * 0.8f
        v.moveTo(x, y0)
        v.lineTo(x, y0 + 0.025f * h)
    }
    drawPath(v, Color.White, alpha = 0.9f, style = kit.strokeFlow)
}

/** The troll in the mountain blinks now and then. */
private fun MapPen.drawTrollBlink(d: DrawScope) = with(d) {
    if (wrap(t, 6.5f) >= 0.18f) return@with
    val c = Offset(0.41f * w, 0.25f * h)
    val sz = h * 0.07f
    val rock = nt(Color(0xFF9DA8C4), 0.5f)
    for (side in intArrayOf(-1, 1)) {
        val e = Offset(c.x + side * sz * 0.34f, c.y - sz * 0.18f)
        drawCircle(rock, sz * 0.09f, e)
        drawLine(rock.darken(0.5f), Offset(e.x - sz * 0.07f, e.y), Offset(e.x + sz * 0.07f, e.y), strokeWidth = lw * 1.3f, cap = StrokeCap.Round, alpha = 0.7f)
    }
}

/** The moving water: river flow, ripples and foam under the fall, mist, and the drifting waves. */
private fun MapPen.drawWatersLive(d: DrawScope, g: MapGeo, kit: LiveKit) = with(d) {
    // River flow: little white dashes sliding downstream, skipping the bridges.
    val flow = kit.flow
    flow.rewind()
    for (i in 0 until 40) {
        val f0 = wrap(i / 40f + t * 0.035f, 1f)
        val f1 = min(1f, f0 + 0.018f)
        if (kit.riverCovered(f0, f1)) continue
        val a = g.river.at(f0)
        val b = g.river.at(f1)
        flow.moveTo(a.x, a.y)
        flow.lineTo(b.x, b.y)
    }
    drawPath(flow, Color.White, alpha = 0.6f * (1f - 0.4f * n), style = kit.strokeFlow)
    // Where the fall lands: ripples, foam and rising mist.
    val p = g.pool
    for (k in 0 until 3) {
        val ph = wrap(t * 0.35f + k / 3f, 1f)
        val rw = p.width * (0.25f + 0.35f * ph)
        drawOval(Color.White, Offset(g.fall.cx - rw, p.top + p.height * 0.35f - rw * 0.12f), Size(rw * 2f, rw * 0.36f), alpha = 0.55f * (1f - ph), style = Stroke(lw))
    }
    for (i in 0 until 9) {
        val fxx = g.fall.cx - g.fall.bw * 0.5f + g.fall.bw * i / 8f
        val r = h * (0.009f + 0.004f * sin(t * 3f + i * 1.7f))
        drawCircle(Color.White, r, Offset(fxx, p.top + p.height * 0.18f + 0.003f * h * (i % 2)))
        drawCircle(nt(Color(0xFFD9F5FF), 0.3f), r * 0.55f, Offset(fxx + r * 0.3f, p.top + p.height * 0.2f))
    }
    for (i in 0 until 5) {
        val ph = wrap(t * 0.14f + i / 5f, 1f)
        val mx = g.fall.cx + (hash01(i, 441) - 0.5f) * p.width * 0.6f - ph * 0.02f * w
        val my = p.top + p.height * 0.1f - ph * h * 0.1f
        drawCircle(Color.White, h * (0.022f + ph * 0.03f), Offset(mx, my), alpha = 0.3f * sin(ph * 3.1416f) * (1f - 0.35f * n))
    }
    // On sunny days a small rainbow in the spray.
    val spray = 0.6f * (1f - n) * (1f - oc) + 0.4f * pen.rainbow
    if (spray > 0.03f) drawRainbow(Offset(g.fall.cx + 0.02f * w, p.top + p.height * 0.45f), h * 0.1f, h * 0.0065f, spray.coerceAtMost(0.75f))
    // Waves drifting to and fro on the fjord.
    translate(sin(t * 0.25f) * w * 0.006f, 0f) {
        drawPath(g.waves, Color.White, alpha = 0.65f * (1f - 0.4f * n), style = kit.strokeWaves)
    }
}

/** A glowing ring on the ground in front of the chosen place, pulsing gently. */
private fun MapPen.drawHighlight(d: DrawScope, g: MapGeo, hl: PlaceId?) = with(d) {
    if (hl == null) return@with
    val pulse = 0.5f + 0.5f * sin(t * 3.4f)
    if (hl == PlaceId.SPACE) {
        val spot = mapSpot(PlaceId.SPACE)
        val c = Offset(spot.x * w, spot.y * h)
        val r = S * 0.5f * (1.2f + 0.06f * pulse)
        drawCircle(Pal.sun, r, c, alpha = 0.9f, style = Stroke(lw * 2.6f))
        drawCircle(Color.White, r, c, alpha = 0.5f, style = Stroke(lw * 1f))
        return@with
    }
    val b = g.bases[hl]!!
    val sc = S * depthScale(b.y / h)
    // Only the front half of the ring is drawn: it passes in front of the building, never across it.
    drawArc(Pal.sun, 0f, 180f, true, Offset(b.x - sc * 1.6f, b.y - sc * 0.26f), Size(sc * 3.2f, sc * 0.58f), alpha = 0.12f + 0.07f * pulse)
    drawArc(Pal.sun, 0f, 180f, true, Offset(b.x - sc * 1.3f, b.y - sc * 0.2f), Size(sc * 2.6f, sc * 0.46f), alpha = 0.24f + 0.1f * pulse)
    val tl = Offset(b.x - sc * 1.3f - pulse * sc * 0.1f, b.y - sc * 0.2f - pulse * sc * 0.02f)
    val sz = Size(sc * 2.6f + pulse * sc * 0.2f, sc * 0.46f + pulse * sc * 0.04f)
    drawArc(Color.White, 0f, 180f, false, tl, sz, alpha = 0.9f, style = Stroke(lw * 3.4f, cap = StrokeCap.Round))
    drawArc(Pal.sun, 0f, 180f, false, tl, sz, alpha = 1f, style = Stroke(lw * 1.8f, cap = StrokeCap.Round))
    // Sparkles travelling round the front of the ring.
    for (k in 0 until 3) {
        val a = wrap(t * 0.5f + k / 3f, 1f) * 3.1416f
        val c = Offset(b.x + cos(a) * sc * 1.3f, b.y + sin(a) * sc * 0.23f)
        twinkleLive(d, c, lw * 4f, Color.White, 0.5f + 0.5f * sin(a))
    }
}
