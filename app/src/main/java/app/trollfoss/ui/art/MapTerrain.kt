package app.trollfoss.ui.art

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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * The terrain of the village map: mountains with layered haze, the great waterfall and its cliff, rolling
 * hills with lit and shaded slopes, terraced fields, a river valley, the fjord with cliffs and a beach.
 * Everything static is built once per screen size in [MapGeo] (in pixels) and only painted every frame;
 * light falls from the upper left, so slopes face lit on the left and shaded on the right.
 */

// ------------------------------------------------------------------------------------- polylines

/** A smooth open line in pixels with arc-length lookup, used for rivers, roads, the railway and walkers. */
internal class Poly(val x: FloatArray, val y: FloatArray) {
    val n: Int = x.size
    val cum = FloatArray(n)

    init {
        for (i in 1 until n) cum[i] = cum[i - 1] + hypot(x[i] - x[i - 1], y[i] - y[i - 1])
    }

    val len: Float get() = cum[n - 1]

    private fun seg(d: Float): Int {
        var lo = 0
        var hi = n - 1
        while (hi - lo > 1) {
            val mid = (lo + hi) ushr 1
            if (cum[mid] <= d) lo = mid else hi = mid
        }
        return lo
    }

    fun at(f: Float): Offset {
        val d = f.coerceIn(0f, 1f) * len
        val i = seg(d)
        val l = cum[i + 1] - cum[i]
        val r = if (l <= 1e-4f) 0f else (d - cum[i]) / l
        return Offset(mix(x[i], x[i + 1], r), mix(y[i], y[i + 1], r))
    }

    fun dir(f: Float): Offset {
        val i = seg(f.coerceIn(0f, 1f) * len)
        val dx = x[i + 1] - x[i]
        val dy = y[i + 1] - y[i]
        val l = hypot(dx, dy).coerceAtLeast(1e-4f)
        return Offset(dx / l, dy / l)
    }

    fun path(): Path = Path().apply {
        moveTo(x[0], y[0])
        for (i in 1 until n) lineTo(x[i], y[i])
    }

    /** The smallest distance from ([px], [py]) to the line. */
    fun dist(px: Float, py: Float): Float {
        var best = Float.MAX_VALUE
        for (i in 0 until n - 1) {
            val dx = x[i + 1] - x[i]
            val dy = y[i + 1] - y[i]
            val l2 = dx * dx + dy * dy
            val r = if (l2 <= 1e-4f) 0f else (((px - x[i]) * dx + (py - y[i]) * dy) / l2).coerceIn(0f, 1f)
            best = min(best, hypot(px - (x[i] + dx * r), py - (y[i] + dy * r)))
        }
        return best
    }

    /** The place along the line (0..1) that is nearest to ([px], [py]). */
    fun nearestF(px: Float, py: Float): Float {
        var best = Float.MAX_VALUE
        var bf = 0f
        for (i in 0 until n - 1) {
            val dx = x[i + 1] - x[i]
            val dy = y[i + 1] - y[i]
            val l2 = dx * dx + dy * dy
            val r = if (l2 <= 1e-4f) 0f else (((px - x[i]) * dx + (py - y[i]) * dy) / l2).coerceIn(0f, 1f)
            val d = hypot(px - (x[i] + dx * r), py - (y[i] + dy * r))
            if (d < best) {
                best = d
                bf = (cum[i] + r * (cum[i + 1] - cum[i])) / len
            }
        }
        return bf
    }
}

/** The line chopped into dashes of length [on] with gaps of [off], as one cached path. */
internal fun Poly.dashes(on: Float, off: Float): Path {
    val p = Path()
    var d = 0f
    val total = len
    while (d < total) {
        val a = at(d / total)
        val b = at(min(d + on, total) / total)
        p.moveTo(a.x, a.y)
        p.lineTo(b.x, b.y)
        d += on + off
    }
    return p
}

/** A Catmull-Rom curve through control points given as fractions of the map (x, y pairs). */
internal fun smoothPoly(w: Float, h: Float, pts: FloatArray, sub: Int = 8): Poly {
    val n = pts.size / 2
    fun px(i: Int) = pts[i.coerceIn(0, n - 1) * 2] * w
    fun py(i: Int) = pts[i.coerceIn(0, n - 1) * 2 + 1] * h
    val xs = ArrayList<Float>(n * sub + 1)
    val ys = ArrayList<Float>(n * sub + 1)
    for (i in 0 until n - 1) {
        for (s in 0 until sub) {
            val t = s / sub.toFloat()
            val t2 = t * t
            val t3 = t2 * t
            xs.add(0.5f * (2f * px(i) + (-px(i - 1) + px(i + 1)) * t + (2f * px(i - 1) - 5f * px(i) + 4f * px(i + 1) - px(i + 2)) * t2 + (-px(i - 1) + 3f * px(i) - 3f * px(i + 1) + px(i + 2)) * t3))
            ys.add(0.5f * (2f * py(i) + (-py(i - 1) + py(i + 1)) * t + (2f * py(i - 1) - 5f * py(i) + 4f * py(i + 1) - py(i + 2)) * t2 + (-py(i - 1) + 3f * py(i) - 3f * py(i + 1) + py(i + 2)) * t3))
        }
    }
    xs.add(px(n - 1))
    ys.add(py(n - 1))
    return Poly(xs.toFloatArray(), ys.toFloatArray())
}

/** The two edges of a ribbon of varying [width] around [p]: left then right, as a closed outline. */
internal fun ribbon(p: Poly, extra: Float = 0f, width: (Float) -> Float): Path {
    val n = p.n
    val lx = FloatArray(n)
    val ly = FloatArray(n)
    val rx = FloatArray(n)
    val ry = FloatArray(n)
    for (i in 0 until n) {
        val f = p.cum[i] / p.len
        val a = max(0, i - 1)
        val b = min(n - 1, i + 1)
        var dx = p.x[b] - p.x[a]
        var dy = p.y[b] - p.y[a]
        val l = hypot(dx, dy).coerceAtLeast(1e-4f)
        dx /= l
        dy /= l
        val hw = (width(f) + extra) * 0.5f
        lx[i] = p.x[i] + dy * hw
        ly[i] = p.y[i] - dx * hw
        rx[i] = p.x[i] - dy * hw
        ry[i] = p.y[i] + dx * hw
    }
    return Path().apply {
        moveTo(lx[0], ly[0])
        for (i in 1 until n) lineTo(lx[i], ly[i])
        for (i in n - 1 downTo 0) lineTo(rx[i], ry[i])
        close()
    }
}

// ------------------------------------------------------------------------------------- mountains

/** One mountain as fractions of the map: peak, foot, snow line and how far its spine leans. */
internal class Mount(
    val px: Float, val py: Float, val x0: Float, val x1: Float, val base: Float,
    val snowY: Float, val lean: Float, val seed: Int,
) {
    private fun jx(f: Float, left: Boolean) = 0.005f * sin(f * 19f + seed + if (left) 0f else 2f) + 0.0035f * sin(f * 43f + seed * 1.7f)
    private fun jy(f: Float) = 0.004f * sin(f * 23f + seed * 0.7f) + 0.002f * sin(f * 51f + seed)

    /** The silhouette at height [f] (0 at the foot, 1 at the peak) on the left or right, in pixels. */
    fun sil(f: Float, left: Boolean, w: Float, h: Float): Offset {
        val ff = f.coerceIn(0f, 1f)
        val edge = if (left) x0 else x1
        val x = mix(edge, px, ff) + if (ff in 0.02f..0.98f) jx(ff, left) else 0f
        val y = base + (py - base) * ff.pow(1.45f) + if (ff in 0.02f..0.98f) jy(ff) else 0f
        return Offset(x * w, y * h)
    }

    /** The spine from the peak ([g] = 0) to the foot ([g] = 1). */
    fun spine(g: Float, w: Float, h: Float): Offset {
        val x = px + lean * (x1 - x0) * g + if (g in 0.02f..0.98f) 0.006f * sin(g * 14f + seed) else 0f
        return Offset(x * w, mix(py, base, g) * h)
    }

    /** The top of the mountain at map x [xf] (fractions), or 9 when it is not there. */
    fun topY(xf: Float, w: Float, h: Float): Float {
        if (xf < x0 || xf > x1) return 9f
        val left = xf < px
        val f = if (left) (xf - x0) / (px - x0) else (x1 - xf) / (x1 - px)
        return sil(f, left, w, h).y / h
    }
}

/** The shapes of one mountain. */
internal class MtnPart {
    val body = Path()
    val shade = Path()
    val facet = Path()
    val snow = Path()
    val snowShade = Path()
    val ridge = Path()
    val strata = Path()
}

/** A group of mountains that share colours, each with its own parts so they overlap properly. */
internal class MtnBatch(val cLit: Color, val cShade: Color, val cFacet: Color, val cSnow: Color, val cSnowShade: Color) {
    val parts = ArrayList<MtnPart>()
    val mounts = ArrayList<Mount>()
}

internal fun MtnBatch.add(m: Mount, w: Float, h: Float) {
    mounts.add(m)
    val part = MtnPart()
    parts.add(part)
    val body = part.body
    val shade = part.shade
    val facet = part.facet
    val snow = part.snow
    val snowShade = part.snowShade
    val ridge = part.ridge
    val strata = part.strata
    val steps = 14
    body.moveTo(m.sil(0f, true, w, h).x, m.sil(0f, true, w, h).y)
    ridge.moveTo(m.sil(0f, true, w, h).x, m.sil(0f, true, w, h).y)
    for (i in 1..steps) {
        val o = m.sil(i / steps.toFloat(), true, w, h)
        body.lineTo(o.x, o.y)
        ridge.lineTo(o.x, o.y)
    }
    for (i in steps - 1 downTo 0) {
        val o = m.sil(i / steps.toFloat(), false, w, h)
        body.lineTo(o.x, o.y)
        ridge.lineTo(o.x, o.y)
    }
    body.close()
    // The shaded right side of the spine.
    val peak = m.sil(1f, true, w, h)
    shade.moveTo(peak.x, peak.y)
    for (i in steps - 1 downTo 0) {
        val o = m.sil(i / steps.toFloat(), false, w, h)
        shade.lineTo(o.x, o.y)
    }
    for (i in 8 downTo 0) {
        val o = m.spine(i / 8f, w, h)
        shade.lineTo(o.x, o.y)
    }
    shade.close()
    // A shoulder on the lit side: a slightly darker facet that breaks up the big face.
    val a = m.sil(0.66f, true, w, h)
    val b = m.spine(0.34f, w, h)
    val c = m.spine(0.96f, w, h)
    val d = m.sil(0.2f, true, w, h)
    facet.moveTo(a.x, a.y)
    facet.lineTo(b.x, b.y)
    facet.lineTo(c.x, c.y)
    facet.lineTo(d.x, d.y)
    facet.close()
    // Snow above the snow line, with a jagged lower edge.
    val fs = ((m.snowY - m.base) / (m.py - m.base)).coerceIn(0.05f, 0.98f).pow(1f / 1.45f)
    val sl = m.sil(fs, true, w, h)
    val sr = m.sil(fs, false, w, h)
    val gs = ((m.snowY - m.py) / (m.base - m.py)).coerceIn(0f, 1f)
    val sp = m.spine(gs, w, h)
    val ns = 10
    for (i in 0..ns) {
        val o = m.sil(fs + (1f - fs) * i / ns, true, w, h)
        if (i == 0) snow.moveTo(o.x, o.y) else snow.lineTo(o.x, o.y)
    }
    for (i in ns downTo 0) {
        val o = m.sil(fs + (1f - fs) * i / ns, false, w, h)
        snow.lineTo(o.x, o.y)
    }
    val teeth = 9
    for (i in 1 until teeth) {
        val f = i / teeth.toFloat()
        val x = mix(sr.x, sl.x, f)
        val y = mix(sr.y, sl.y, f) + (if (i % 2 == 0) -1f else 1f) * 0.012f * h * (0.6f + 0.6f * hash01(i, m.seed))
        snow.lineTo(x, y)
    }
    snow.lineTo(sl.x, sl.y)
    snow.close()
    snowShade.moveTo(peak.x, peak.y)
    for (i in ns downTo 0) {
        val o = m.sil(fs + (1f - fs) * i / ns, false, w, h)
        snowShade.lineTo(o.x, o.y)
    }
    val steps2 = 5
    for (i in 1..steps2) {
        val f = i / steps2.toFloat()
        snowShade.lineTo(mix(sr.x, sp.x, f), mix(sr.y, sp.y, f) + (if (i % 2 == 1) 1f else -1f) * 0.012f * h * hash01(i, m.seed + 4))
    }
    for (i in 8 downTo 0) {
        val g = gs * i / 8f
        val o = m.spine(g, w, h)
        snowShade.lineTo(o.x, o.y)
    }
    snowShade.close()
    // Rock strata and gullies below the snow.
    for (k in 0 until 7) {
        val g = gs + 0.06f + (1f - gs - 0.14f) * k / 6f
        val s = m.spine(g, w, h)
        val fr = (1f - g).pow(1f / 1.45f)
        val r = m.sil(fr, false, w, h)
        val l = m.sil(fr, true, w, h)
        val wob = (hash01(k, m.seed + 9) - 0.5f) * 0.01f * h
        strata.moveTo(s.x, s.y)
        strata.lineTo(mix(s.x, r.x, 0.5f), mix(s.y, r.y, 0.5f) + 0.012f * h + wob)
        strata.moveTo(s.x, s.y + 0.004f * h)
        strata.lineTo(mix(s.x, l.x, 0.38f), mix(s.y, l.y, 0.38f) + 0.01f * h - wob)
    }
}

// ------------------------------------------------------------------------------------- hills

/** A rounded hill seen from above at a slant: a dome with a shaded crescent on its lower right. */
internal class HillGeo(
    val body: Path, val shade: Path, val contour: Path, val rim: Path, val base: Path,
    val cx: Float, val cy: Float, val rx: Float, val ry: Float, val pines: Boolean, val tone: Int,
)

internal fun buildHill(w: Float, h: Float, cx: Float, cy: Float, rx: Float, ry: Float, seed: Int, pines: Boolean, tone: Int): HillGeo {
    val x = cx * w
    val y = cy * h
    val rX = rx * w
    val rY = ry * h
    val n = 28
    fun wob(a: Float) = 1f + 0.05f * sin(a * 3f + seed) + 0.03f * sin(a * 7f + seed * 2f)
    fun pt(a: Float, s: Float = 1f, ox: Float = 0f, oy: Float = 0f) = Offset(x + rX * s * wob(a) * cos(a) + ox * rX, y + rY * s * wob(a) * sin(a) + oy * rY)
    val body = Path()
    for (i in 0 until n) {
        val o = pt(i * 6.2831855f / n)
        if (i == 0) body.moveTo(o.x, o.y) else body.lineTo(o.x, o.y)
    }
    body.close()
    val shade = Path()
    val a0 = -0.45f
    val a1 = 2.35f
    val m = 14
    for (i in 0..m) {
        val o = pt(a0 + (a1 - a0) * i / m)
        if (i == 0) shade.moveTo(o.x, o.y) else shade.lineTo(o.x, o.y)
    }
    for (i in m downTo 0) {
        val o = pt(a0 + (a1 - a0) * i / m, 0.84f, -0.14f, -0.1f)
        shade.lineTo(o.x, o.y)
    }
    shade.close()
    val contour = Path()
    for ((k, s) in floatArrayOf(0.68f, 0.4f).withIndex()) {
        for (i in 0..10) {
            val a = -2.75f + 2.1f * i / 10f
            val o = pt(a, s, -0.05f * (k + 1), -0.06f * (k + 1))
            if (i == 0) contour.moveTo(o.x, o.y) else contour.lineTo(o.x, o.y)
        }
    }
    val rim = Path()
    for (i in 0..16) {
        val a = -3.3f + 3.5f * i / 16f
        val o = pt(a)
        if (i == 0) rim.moveTo(o.x, o.y) else rim.lineTo(o.x, o.y)
    }
    // A soft contact shadow where the hill meets the ground, on the lower right.
    val baseSh = Path()
    for (i in 0..12) {
        val o = pt(-0.2f + 2.4f * i / 12f, 1.02f, 0.01f, 0.03f)
        if (i == 0) baseSh.moveTo(o.x, o.y) else baseSh.lineTo(o.x, o.y)
    }
    return HillGeo(body, shade, contour, rim, baseSh, x, y, rX, rY, pines, tone)
}

/** A terrace: a flat field with rows and a stone retaining wall in front of it. */
internal class TerraceGeo(val top: Path, val wall: Path, val lip: Path, val rows: Path, val color: Color, val wallRow: Int)

// ------------------------------------------------------------------------------------- waterfall

internal class FallGeo(
    val cliff: Path, val cliffShade: Path, val cracks: Path, val ledges: Path, val moss: Path,
    val water: Path, val lip: Path, val stream: Path, val rim: Path,
    val cx: Float, val top: Float, val bottom: Float, val tw: Float, val bw: Float,
    val veil: Path, val veilX: Float, val veilTop: Float, val veilBottom: Float,
)

// ------------------------------------------------------------------------------------- the map

@Volatile
private var geoCache: MapGeo? = null
private val geoLock = Any()

/**
 * The geometry for a map of [w] × [h] pixels, rebuilt only when the size changes. It may be asked for
 * from a background thread (while the map bitmap is made) and from the main thread, so it is built under a lock.
 */
internal fun mapGeo(w: Float, h: Float): MapGeo {
    val c = geoCache
    if (c != null && c.w == w && c.h == h) return c
    synchronized(geoLock) {
        val c2 = geoCache
        if (c2 != null && c2.w == w && c2.h == h) return c2
        return MapGeo(w, h).also { geoCache = it }
    }
}

internal class MapGeo(val w: Float, val h: Float) {
    /** The landmark unit: what a building is sized by. */
    val S = min(h * 0.15f, w * 0.0825f)

    // ---- mountains: far ranges, the great long Heileberget as the backbone, three nearer mountains
    val far1 = MtnBatch(Color(0xFFD0DDF0), Color(0xFFBCCBE6), Color(0xFFC6D4EB), Color(0xFFF8FAFF), Color(0xFFE0E9F8))
    val far2 = MtnBatch(Color(0xFFB9C9E4), Color(0xFF9FB3D6), Color(0xFFADBEDD), Color(0xFFF3F7FF), Color(0xFFD4DFF2))
    val heileBack = MtnBatch(Color(0xFFA9B6D2), Color(0xFF8391B4), Color(0xFF9BA9C7), Color(0xFFF4F8FF), Color(0xFFCFDAF0))
    val heile = MtnBatch(Color(0xFF9AA6C4), Color(0xFF5C6A92), Color(0xFF8795B6), Color(0xFFF7FAFF), Color(0xFFB4C4E6))
    val massC = MtnBatch(Color(0xFF9DAAC6), Color(0xFF71809F), Color(0xFF8E9CBA), Color(0xFFF6F9FF), Color(0xFFC8D5EE))
    val massA = MtnBatch(Color(0xFFA4B0CA), Color(0xFF7987A8), Color(0xFF96A3C0), Color(0xFFF8FAFF), Color(0xFFCFDAF0))
    val massD = MtnBatch(Color(0xFFA0ACC7), Color(0xFF7685A6), Color(0xFF93A0BD), Color(0xFFF6F9FF), Color(0xFFCBD7EE))
    val batches = listOf(far1, far2, heileBack, heile, massC, massA, massD)

    init {
        far1.add(Mount(0.0f, 0.07f, -0.16f, 0.2f, 0.45f, 0.25f, 0.05f, 1), w, h)
        far1.add(Mount(0.85f, 0.03f, 0.7f, 1.02f, 0.45f, 0.22f, -0.04f, 2), w, h)
        far1.add(Mount(0.62f, 0.12f, 0.4f, 0.84f, 0.45f, 0.26f, 0.05f, 3), w, h)
        far2.add(Mount(0.1f, 0.12f, -0.08f, 0.3f, 0.46f, 0.27f, 0.06f, 5), w, h)
        far2.add(Mount(0.78f, 0.1f, 0.6f, 0.98f, 0.46f, 0.28f, -0.05f, 7), w, h)
        heileBack.add(Mount(0.0f, 0.2f, -0.12f, 0.2f, 0.47f, 0.3f, 0.05f, 21), w, h)
        heileBack.add(Mount(0.34f, 0.08f, 0.18f, 0.5f, 0.47f, 0.27f, 0.0f, 22), w, h)
        heileBack.add(Mount(0.62f, 0.075f, 0.42f, 0.8f, 0.47f, 0.28f, 0.06f, 23), w, h)
        heileBack.add(Mount(0.99f, 0.17f, 0.8f, 1.15f, 0.47f, 0.3f, 0.0f, 24), w, h)
        heile.add(Mount(0.065f, 0.15f, -0.1f, 0.25f, 0.47f, 0.24f, 0.06f, 31), w, h)
        heile.add(Mount(0.91f, 0.125f, 0.72f, 1.12f, 0.47f, 0.25f, -0.05f, 35), w, h)
        heile.add(Mount(0.69f, 0.06f, 0.46f, 0.92f, 0.47f, 0.2f, 0.04f, 34), w, h)
        heile.add(Mount(0.235f, 0.07f, 0.04f, 0.44f, 0.47f, 0.2f, -0.04f, 32), w, h)
        heile.add(Mount(0.14f, 0.105f, 0.03f, 0.26f, 0.47f, 0.3f, 0.0f, 36), w, h)
        heile.add(Mount(0.385f, 0.05f, 0.27f, 0.49f, 0.47f, 0.3f, 0.0f, 37), w, h)
        heile.add(Mount(0.795f, 0.09f, 0.68f, 0.91f, 0.47f, 0.3f, 0.0f, 38), w, h)
        heile.add(Mount(0.47f, 0.012f, 0.2f, 0.8f, 0.47f, 0.22f, 0.03f, 33), w, h)
        massC.add(Mount(0.68f, 0.17f, 0.44f, 0.9f, 0.46f, 0.27f, 0.02f, 12), w, h)
        massA.add(Mount(0.17f, 0.2f, -0.02f, 0.33f, 0.46f, 0.3f, 0.05f, 13), w, h)
        massD.add(Mount(0.96f, 0.26f, 0.84f, 1.1f, 0.46f, 0.36f, 0.0f, 14), w, h)
    }

    // Heileberget's rock face and scree slopes.
    val rockFace = Path()
    val rockFaceShade = Path()
    val rockStrata = Path()
    val snowLedges = Path()
    val scree = Path()
    val screeShade = Path()
    val screeDots = ArrayList<Offset>()

    init {
        fun poly(target: Path, vararg p: Float) {
            for (i in 0 until p.size / 2) if (i == 0) target.moveTo(p[i * 2] * w, p[i * 2 + 1] * h) else target.lineTo(p[i * 2] * w, p[i * 2 + 1] * h)
            target.close()
        }
        poly(rockFace, 0.262f, 0.128f, 0.28f, 0.088f, 0.304f, 0.062f, 0.335f, 0.052f, 0.362f, 0.075f, 0.369f, 0.112f, 0.356f, 0.152f, 0.328f, 0.172f, 0.292f, 0.166f)
        poly(rockFaceShade, 0.322f, 0.056f, 0.335f, 0.052f, 0.362f, 0.075f, 0.369f, 0.112f, 0.356f, 0.152f, 0.328f, 0.172f, 0.322f, 0.11f)
        for (i in 0 until 8) {
            val x = 0.276f + 0.0115f * i
            rockStrata.moveTo(x * w, (0.09f + 0.012f * hash01(i, 13)) * h)
            rockStrata.lineTo((x + 0.004f * (if (i % 2 == 0) 1 else -1)) * w, (0.165f - 0.008f * hash01(i, 14)) * h)
        }
        for ((i, y) in floatArrayOf(0.085f, 0.11f, 0.138f).withIndex()) {
            val x = 0.285f + 0.012f * i
            poly(snowLedges, x, y + 0.004f, x + 0.02f, y - 0.004f, x + 0.044f, y + 0.002f, x + 0.04f, y + 0.009f, x + 0.016f, y + 0.011f)
        }
        // Scree: grey fans of loose rock under the steep faces, with a scatter of stones.
        val fans = floatArrayOf(0.565f, 0.318f, 0.04f, 0.83f, 0.33f, 0.035f, 0.075f, 0.31f, 0.04f, 0.36f, 0.355f, 0.025f, 0.43f, 0.385f, 0.02f)
        for (i in 0 until fans.size / 3) {
            val ax = fans[i * 3]
            val ay = fans[i * 3 + 1]
            val fw = fans[i * 3 + 2]
            poly(scree, ax, ay, ax + fw * 0.4f, ay + 0.04f, ax + fw, ay + 0.09f, ax - fw, ay + 0.09f, ax - fw * 0.5f, ay + 0.04f)
            poly(screeShade, ax, ay, ax + fw * 0.4f, ay + 0.04f, ax + fw, ay + 0.09f, ax + fw * 0.15f, ay + 0.09f)
            for (k in 0 until 12) {
                val f = hash01(i * 12 + k, 15)
                screeDots.add(Offset((ax + (hash01(i * 12 + k, 16) - 0.5f) * fw * 1.6f * f) * w, (ay + 0.01f + 0.075f * f) * h))
            }
        }
    }

    /** The great waterfall: a rock cliff in massif B, the falling water, a veil beside the cave. */
    val fall: FallGeo = buildFall()

    private fun buildFall(): FallGeo {
        val cx = 0.485f * w
        val top = 0.158f * h
        val bottom = 0.392f * h
        val tw = 0.03f * w
        val bw = 0.052f * w
        val cliff = Path()
        val cp = floatArrayOf(
            0.432f, 0.205f, 0.44f, 0.176f, 0.452f, 0.168f, 0.462f, 0.172f, 0.474f, 0.157f, 0.496f, 0.156f, 0.508f, 0.168f,
            0.522f, 0.16f, 0.536f, 0.17f, 0.548f, 0.19f, 0.552f, 0.25f, 0.555f, 0.32f, 0.566f, 0.392f,
            0.53f, 0.402f, 0.49f, 0.398f, 0.455f, 0.406f, 0.425f, 0.398f, 0.424f, 0.33f, 0.427f, 0.26f,
        )
        for (i in 0 until cp.size / 2) {
            val px = cp[i * 2] * w
            val py = cp[i * 2 + 1] * h
            if (i == 0) cliff.moveTo(px, py) else cliff.lineTo(px, py)
        }
        cliff.close()
        // The shaded right half, with a zig-zag edge down the middle.
        val shade = Path()
        shade.moveTo(0.505f * w, 0.158f * h)
        shade.lineTo(0.508f * w, 0.168f * h)
        shade.lineTo(0.522f * w, 0.16f * h)
        shade.lineTo(0.536f * w, 0.17f * h)
        shade.lineTo(0.548f * w, 0.19f * h)
        shade.lineTo(0.552f * w, 0.25f * h)
        shade.lineTo(0.555f * w, 0.32f * h)
        shade.lineTo(0.566f * w, 0.392f * h)
        shade.lineTo(0.53f * w, 0.402f * h)
        shade.lineTo(0.522f * w, 0.36f * h)
        shade.lineTo(0.528f * w, 0.3f * h)
        shade.lineTo(0.521f * w, 0.24f * h)
        shade.lineTo(0.527f * w, 0.19f * h)
        shade.close()
        val cracks = Path()
        for ((i, x) in floatArrayOf(0.44f, 0.452f, 0.546f, 0.535f, 0.43f, 0.55f).withIndex()) {
            val y0 = 0.2f + 0.04f * hash01(i, 71)
            cracks.moveTo(x * w, y0 * h)
            cracks.lineTo((x + 0.004f * (if (i % 2 == 0) 1 else -1)) * w, (y0 + 0.07f) * h)
            cracks.lineTo((x - 0.002f) * w, (y0 + 0.13f + 0.03f * hash01(i, 72)) * h)
        }
        val ledges = Path()
        for ((i, yy) in floatArrayOf(0.25f, 0.3f, 0.345f).withIndex()) {
            val x0 = if (i % 2 == 0) 0.43f else 0.535f
            ledges.moveTo(x0 * w, yy * h)
            ledges.lineTo((x0 + 0.02f) * w, (yy - 0.006f) * h)
            ledges.lineTo((x0 + 0.03f) * w, (yy + 0.002f) * h)
            ledges.lineTo((x0 + 0.004f) * w, (yy + 0.012f) * h)
            ledges.close()
        }
        val moss = Path()
        for ((i, p) in floatArrayOf(0.44f, 0.372f, 0.546f, 0.36f, 0.47f, 0.385f, 0.52f, 0.388f).toList().chunked(2).withIndex()) {
            moss.addPath(crownPath(p[0] * w, p[1] * h, 0.014f * w, 0.007f * h, 80 + i))
        }
        // The water: narrow at the lip, widening and bulging a little as it falls.
        val water = Path()
        water.moveTo(cx - tw / 2f, top)
        water.quadraticTo(cx - tw * 0.66f, (top + bottom) / 2f, cx - bw / 2f, bottom)
        water.lineTo(cx + bw / 2f, bottom)
        water.quadraticTo(cx + tw * 0.66f, (top + bottom) / 2f, cx + tw / 2f, top)
        water.close()
        val rim = Path()
        rim.moveTo(cx - tw / 2f, top)
        rim.quadraticTo(cx - tw * 0.66f, (top + bottom) / 2f, cx - bw / 2f, bottom)
        rim.moveTo(cx + tw / 2f, top)
        rim.quadraticTo(cx + tw * 0.66f, (top + bottom) / 2f, cx + bw / 2f, bottom)
        // Water curling over the lip.
        val lip = Path()
        lip.moveTo(cx - tw * 0.62f, top - 0.004f * h)
        lip.quadraticTo(cx, top + 0.022f * h, cx + tw * 0.62f, top - 0.004f * h)
        lip.lineTo(cx + tw * 0.5f, top + 0.012f * h)
        lip.quadraticTo(cx, top + 0.032f * h, cx - tw * 0.5f, top + 0.012f * h)
        lip.close()
        // The snow-melt stream winding down the snowfield to the lip.
        val stream = Path()
        stream.moveTo(0.5f * w, 0.07f * h)
        stream.cubicTo(0.492f * w, 0.09f * h, 0.51f * w, 0.11f * h, 0.495f * w, 0.135f * h)
        stream.quadraticTo(0.487f * w, 0.148f * h, cx, top)
        // A thin veil of water beside the troll cave, on massif C.
        val vx = 0.596f * w
        val vt = 0.2f * h
        val vb = 0.322f * h
        val veil = Path()
        veil.moveTo(vx - 0.006f * w, vt)
        veil.lineTo(vx + 0.006f * w, vt)
        veil.quadraticTo(vx + 0.012f * w, (vt + vb) / 2f, vx + 0.01f * w, vb)
        veil.lineTo(vx - 0.01f * w, vb)
        veil.quadraticTo(vx - 0.01f * w, (vt + vb) / 2f, vx - 0.006f * w, vt)
        veil.close()
        return FallGeo(cliff, shade, cracks, ledges, moss, water, lip, stream, rim, cx, top, bottom, tw, bw, veil, vx, vt, vb)
    }

    // ---- the valley
    val valleyTop = 0.405f * h
    val valley = Path().apply {
        moveTo(-20f, valleyTop)
        var xx = 0f
        while (xx <= 1.04f) {
            lineTo(xx * w, valleyTop + 0.006f * h * sin(xx * 23f) + 0.004f * h * sin(xx * 51f))
            xx += 0.02f
        }
        lineTo(w + 20f, h + 20f)
        lineTo(-20f, h + 20f)
        close()
    }
    val meadowLight = Path()
    val meadowDark = Path()
    val daisiesA = ArrayList<Offset>()
    val daisiesB = ArrayList<Offset>()
    val ticks = Path()

    init {
        for (i in 0 until 46) {
            val cx = hash01(i, 301) * 1.1f - 0.05f
            val cy = 0.44f + hash01(i, 302) * 0.56f
            val rw = (0.03f + 0.06f * hash01(i, 303)) * w
            val rh = rw * (0.14f + 0.12f * hash01(i, 304)) * (w / h) * 0.5f
            val target = if (i % 2 == 0) meadowLight else meadowDark
            target.addPath(crownPath(cx * w, cy * h, rw, rh, 310 + i))
        }
        for (i in 0 until 240) {
            (if (i % 3 == 0) daisiesB else daisiesA).add(Offset((hash01(i, 321) * 1.06f - 0.03f) * w, (0.44f + hash01(i, 322) * 0.56f) * h))
        }
        for (i in 0 until 360) {
            val x = (hash01(i, 331) * 1.06f - 0.03f) * w
            val y = (0.43f + hash01(i, 332) * 0.57f) * h
            val s = (0.004f + 0.004f * hash01(i, 333)) * h
            ticks.moveTo(x - s * 0.7f, y - s)
            ticks.lineTo(x, y)
            ticks.lineTo(x + s * 0.7f, y - s)
        }
    }

    // ---- hills, sorted from far to near
    val hills: List<HillGeo> = listOf(
        // The foothills at the foot of the mountains (dark with pines).
        buildHill(w, h, 0.03f, 0.425f, 0.11f, 0.06f, 1, true, 2),
        buildHill(w, h, 0.17f, 0.415f, 0.12f, 0.055f, 2, true, 2),
        buildHill(w, h, 0.3f, 0.43f, 0.1f, 0.05f, 3, true, 2),
        buildHill(w, h, 0.365f, 0.425f, 0.06f, 0.045f, 4, true, 2),
        buildHill(w, h, 0.61f, 0.43f, 0.07f, 0.05f, 5, true, 2),
        buildHill(w, h, 0.73f, 0.425f, 0.1f, 0.055f, 6, true, 2),
        // Rolling green hills in the valley.
        buildHill(w, h, 0.04f, 0.73f, 0.085f, 0.055f, 7, false, 0),
        buildHill(w, h, 0.215f, 0.735f, 0.065f, 0.038f, 8, false, 1),
        buildHill(w, h, 0.68f, 0.745f, 0.08f, 0.045f, 9, false, 0),
        buildHill(w, h, 0.36f, 0.68f, 0.05f, 0.03f, 10, false, 1),
        // The broad hill Storhuset stands on, right of the waterfall (the estate's terrace sits on top of it).
        buildHill(w, h, mapSpot(PlaceId.MANOR_GROUND).x, mapSpot(PlaceId.MANOR_GROUND).y + 0.039f, 0.145f, 0.085f, 11, false, 1),
    ).sortedBy { it.cy }

    // ---- the farm hill with terraced fields
    val farmHillGeo: HillGeo = buildHill(w, h, 0.925f, 0.625f, 0.22f, 0.175f, 20, false, 0)
    val terraces = ArrayList<TerraceGeo>()

    init {
        val cxh = 0.925f
        val cyh = 0.625f
        val rxh = 0.22f
        val ryh = 0.175f
        fun xl(y: Float): Float {
            val dy = (y - cyh) / ryh
            return if (abs(dy) >= 1f) cxh else cxh - rxh * sqrt(1f - dy * dy)
        }
        val fieldColors = listOf(Color(0xFFEDCB5A), Color(0xFFB9DC78), Color(0xFFC08F5C), Color(0xFF92C86A), Color(0xFFE4D07A), Color(0xFFA6D27A))
        val ys = floatArrayOf(0.468f, 0.503f, 0.538f, 0.573f, 0.61f, 0.648f, 0.688f)
        for (k in 0 until ys.size - 1) {
            val y0 = ys[k]
            val y1 = ys[k + 1]
            fun yt(x: Float, yy: Float, ph: Float) = (yy + 0.0045f * sin(x * 21f + k * 1.7f + ph)) * h
            val top = Path()
            val lip = Path()
            val x0a = xl(y0 + 0.006f)
            val x0b = xl(y1)
            var xx = x0a
            top.moveTo(xx * w, yt(xx, y0, 0f))
            lip.moveTo(xx * w, yt(xx, y0, 0f))
            while (xx < 1.06f) {
                xx += 0.02f
                top.lineTo(xx * w, yt(xx, y0, 0f))
                lip.lineTo(xx * w, yt(xx, y0, 0f))
            }
            top.lineTo(1.06f * w, yt(1.06f, y1, 1f))
            xx = 1.06f
            val wall = Path()
            wall.moveTo(1.06f * w, yt(1.06f, y1, 1f))
            while (xx > x0b) {
                xx -= 0.02f
                top.lineTo(max(xx, x0b) * w, yt(max(xx, x0b), y1, 1f))
                wall.lineTo(max(xx, x0b) * w, yt(max(xx, x0b), y1, 1f))
            }
            top.close()
            // The retaining wall below this terrace.
            var bx = x0b
            while (bx <= 1.06f) {
                wall.lineTo(bx * w, yt(bx, y1, 1f) + 0.014f * h)
                bx += 0.02f
            }
            wall.close()
            val rows = Path()
            var rx = x0b + 0.012f
            while (rx < 1.05f) {
                val topY = yt(rx, y0, 0f) + 0.004f * h
                val botY = yt(rx, y1, 1f) - 0.002f * h
                if (rx > xl((y0 + y1) / 2f)) {
                    rows.moveTo(rx * w, topY)
                    rows.lineTo((rx - 0.008f) * w, botY)
                }
                rx += if (k % 3 == 1) 0.02f else 0.013f
            }
            terraces.add(TerraceGeo(top, wall, lip, rows, fieldColors[k % fieldColors.size], k))
        }
    }

    // ---- the river
    val river: Poly = smoothPoly(
        w, h,
        floatArrayOf(
            0.485f, 0.436f, 0.478f, 0.475f, 0.477f, 0.52f, 0.486f, 0.572f, 0.497f, 0.625f, 0.508f, 0.68f,
            0.518f, 0.735f, 0.523f, 0.79f, 0.53f, 0.845f, 0.536f, 0.895f, 0.542f, 0.945f, 0.548f, 1.0f,
        ),
    )

    fun riverWidth(f: Float): Float = (0.019f + 0.03f * f.pow(1.3f)) * h

    val riverLine = river.path()
    val riverRim = ribbon(river, 0.012f * h) { riverWidth(it) }
    val riverWater = ribbon(river) { riverWidth(it) }
    val riverLit = ribbon(river) { riverWidth(it) * 0.7f }
    val bankStones = Path()
    val bankStonesLit = Path()
    val reeds = Path()
    val cattails = ArrayList<Offset>()
    val rapids = Path()

    init {
        for (i in 0 until 46) {
            val f = 0.04f + 0.9f * hash01(i, 401)
            val c = river.at(f)
            val d = river.dir(f)
            val side = if (i % 2 == 0) 1f else -1f
            val off = riverWidth(f) * 0.5f + (0.004f + 0.008f * hash01(i, 402)) * h
            val x = c.x - d.y * off * side
            val y = c.y + d.x * off * side
            if (hash01(i, 403) < 0.45f) {
                val r = (0.0035f + 0.004f * hash01(i, 404)) * h
                bankStones.addOval(Rect(x - r * 1.3f, y - r * 0.7f, x + r * 1.3f, y + r * 0.7f))
                bankStonesLit.addOval(Rect(x - r * 1.2f, y - r * 0.75f, x + r * 0.7f, y + r * 0.35f))
            } else {
                for (k in 0 until 4) {
                    val dx = (k - 1.5f) * 0.0026f * h
                    val hh = (0.012f + 0.01f * hash01(i * 4 + k, 405)) * h
                    val sway = 0.002f * h * (k - 1.5f)
                    reeds.moveTo(x + dx, y)
                    reeds.quadraticTo(x + dx + sway * 0.5f, y - hh * 0.6f, x + dx + sway, y - hh)
                    if (k % 2 == 1) cattails.add(Offset(x + dx + sway, y - hh))
                }
            }
        }
        for (f in floatArrayOf(0.12f, 0.2f, 0.36f, 0.58f, 0.74f)) {
            val c = river.at(f)
            val d = river.dir(f)
            val hw = riverWidth(f) * 0.42f
            for (k in -1..1) {
                val ox = c.x - d.y * hw * k * 0.7f - d.x * k * 0.004f * h
                val oy = c.y + d.x * hw * k * 0.7f - d.y * k * 0.004f * h
                rapids.moveTo(ox - d.y * 0.4f * hw - 0.004f * h * d.x, oy + d.x * 0.4f * hw - 0.004f * h * d.y)
                rapids.lineTo(ox + d.y * 0.4f * hw + 0.004f * h * d.x, oy - d.x * 0.4f * hw + 0.004f * h * d.y)
            }
        }
    }

    // ---- the plunge pool
    val pool = Rect(0.415f * w, 0.382f * h, 0.56f * w, 0.446f * h)

    // ---- the fjord
    val shore: Poly = smoothPoly(
        w, h,
        floatArrayOf(
            0.36f, 1.07f, 0.44f, 1.0f, 0.5f, 0.962f, 0.58f, 0.94f, 0.68f, 0.926f, 0.76f, 0.917f, 0.81f, 0.895f,
            0.835f, 0.862f, 0.843f, 0.81f, 0.86f, 0.752f, 0.9f, 0.72f, 0.955f, 0.706f, 1.07f, 0.69f,
        ),
    )
    val sea = Path().apply {
        moveTo(shore.x[0], shore.y[0])
        for (i in 1 until shore.n) lineTo(shore.x[i], shore.y[i])
        lineTo(w + 40f, shore.y[shore.n - 1] - 40f)
        lineTo(w + 40f, h + 40f)
        lineTo(shore.x[0], h + 40f)
        close()
    }
    val shoreLine = shore.path()
    /** Is the point in the sea? The shore curve is a polygon together with the map's right-bottom corner. */
    fun inSea(px: Float, py: Float): Boolean {
        var inside = false
        val n = shore.n
        val xs = FloatArray(n + 3)
        val ys = FloatArray(n + 3)
        for (i in 0 until n) {
            xs[i] = shore.x[i]
            ys[i] = shore.y[i]
        }
        xs[n] = w + 40f; ys[n] = shore.y[n - 1] - 40f
        xs[n + 1] = w + 40f; ys[n + 1] = h + 40f
        xs[n + 2] = shore.x[0]; ys[n + 2] = h + 40f
        var j = n + 2
        for (i in 0 until n + 3) {
            if ((ys[i] > py) != (ys[j] > py) && px < (xs[j] - xs[i]) * (py - ys[i]) / (ys[j] - ys[i]) + xs[i]) inside = !inside
            j = i
        }
        return inside
    }

    /**
     * Little arcs of white on the fjord. The ones out in the open water drift to and fro (live, [waves]);
     * those that the pier, its boat, the dive buoy or the shore stand in front of stay put ([wavesStill]).
     */
    private val wavePair: Pair<Path, Path> by lazy {
        val drifting = Path()
        val fixed = Path()
        val beach = bases[PlaceId.BEACH]!!
        val buoy = bases[PlaceId.UNDERWATER]!!
        for (i in 0 until 40) {
            val x = 0.52f + 0.5f * hash01(i, 821)
            val y = 0.72f + hash01(i, 822) * 0.27f
            if (!inSea(x * w, y * h)) continue
            // A drifting wave must stay in the water at both ends and clear of what stands in the sea.
            val free = inSea((x - 0.02f) * w, y * h) && inSea((x + 0.02f) * w, y * h) &&
                !(x * w > beach.x + 0.2f * S && x * w < beach.x + 1.9f * S && y * h > beach.y - 0.45f * S && y * h < beach.y + 0.6f * S) &&
                hypot(x * w - buoy.x, y * h - buoy.y) >= 1.1f * S
            val p = if (free) drifting else fixed
            p.moveTo((x - 0.011f) * w, y * h)
            p.quadraticTo(x * w, (y - 0.012f) * h, (x + 0.011f) * w, y * h)
        }
        drifting to fixed
    }
    val waves: Path get() = wavePair.first
    val wavesStill: Path get() = wavePair.second

    /** The line of foam along the shore. */
    val shoreFoam: Path by lazy {
        val p = Path()
        for (i in 0 until 90) {
            val f0 = i / 90f
            val a = shore.at(f0)
            val b = shore.at(min(1f, f0 + 0.0075f))
            p.moveTo(a.x, a.y)
            p.lineTo(b.x, b.y)
        }
        p
    }

    val sandBand = Path()
    val cliffWall = Path()
    val cliffLit = Path()
    val cliffCracks = Path()
    val cliffFoot = Path()

    init {
        var first = true
        for (i in 0 until shore.n) {
            val xf = shore.x[i] / w
            if (xf in 0.58f..0.83f) {
                if (first) sandBand.moveTo(shore.x[i], shore.y[i]) else sandBand.lineTo(shore.x[i], shore.y[i])
                first = false
            }
        }
        // Cliff walls where the farm hill drops into the fjord.
        val idx = (0 until shore.n).filter { shore.x[it] / w > 0.848f && shore.y[it] / h < 0.80f }
        if (idx.size > 3) {
            val hgt = 0.05f * h
            cliffWall.moveTo(shore.x[idx[0]], shore.y[idx[0]] - hgt)
            for (k in idx.indices) {
                val i = idx[k]
                cliffWall.lineTo(shore.x[i], shore.y[i] - hgt - 0.006f * h * sin(k * 1.7f) - 0.004f * h * hash01(k, 501))
            }
            for (k in idx.indices.reversed()) cliffWall.lineTo(shore.x[idx[k]], shore.y[idx[k]] + 0.004f * h)
            cliffWall.close()
            var k = 0
            while (k < idx.size - 2) {
                val a = idx[k]
                val b = idx[min(k + 2, idx.size - 1)]
                val ax = shore.x[a]
                val bx = shore.x[b]
                cliffLit.moveTo(ax, shore.y[a] - hgt - 0.004f * h)
                cliffLit.lineTo((ax + bx) / 2f, shore.y[a] - hgt - 0.004f * h)
                cliffLit.lineTo((ax + bx) / 2f - 0.006f * w, shore.y[b] + 0.002f * h)
                cliffLit.lineTo(ax, shore.y[a] + 0.002f * h)
                cliffLit.close()
                cliffCracks.moveTo((ax + bx) / 2f, shore.y[a] - hgt * 0.8f)
                cliffCracks.lineTo((ax + bx) / 2f - 0.004f * w, shore.y[a] - hgt * 0.3f)
                k += 2
            }
            cliffFoot.moveTo(shore.x[idx[0]], shore.y[idx[0]])
            for (k2 in idx.indices) cliffFoot.lineTo(shore.x[idx[k2]], shore.y[idx[k2]])
        }
    }

    // ---- the road and rail network, bridges and the landmarks' ground (see MapProps.kt)
    val bases: Map<PlaceId, Offset> = PlaceId.entries.filter { it.onMap }.associateWith { Offset(mapSpot(it).x * w, (mapSpot(it).y + 0.045f) * h) }
    val roads: List<Poly> = buildRoads(w, h)
    val roadPaths: List<Path> = roads.map { it.path() }
    val roadDashes: List<Path> = roads.map { it.dashes(0.007f * h, 0.02f * h) }
    val plotOrder: List<PlaceId> = PlaceId.entries.filter { it.onMap }.sortedBy { bases[it]!!.y }
    val plots: Map<PlaceId, PlotGeo> by lazy { PlaceId.entries.filter { it.onMap }.mapNotNull { p -> buildPlot(this, p)?.let { p to it } }.toMap() }
    val doorPaths: List<Path> = PlaceId.entries.filter { it.onMap }.mapNotNull { p ->
        if (p == PlaceId.SPACE || p == PlaceId.UNDERWATER || p == PlaceId.MOUNTAIN || p == PlaceId.LAB || p == PlaceId.MANOR_GROUND || p.name == "HEILEBERGET") return@mapNotNull null
        val b = bases[p]!!
        var best = Float.MAX_VALUE
        var bo = Offset.Zero
        for (r in roads) {
            val f = r.nearestF(b.x, b.y + 0.004f * h)
            val o = r.at(f)
            val dd = hypot(o.x - b.x, o.y - b.y)
            if (dd < best) {
                best = dd
                bo = o
            }
        }
        if (best < 0.012f * h || best > 0.12f * h) null
        else Path().apply {
            moveTo(b.x, b.y + 0.004f * h)
            quadraticTo(b.x + (bo.x - b.x) * 0.2f + 0.008f * h, (b.y + bo.y) / 2f, bo.x, bo.y)
        }
    }
    val railPath: Path by lazy { rail.path() }
    val railTies: Path by lazy { rail.dashes(0.0022f * h, 0.0062f * h) }
    val rail: Poly = smoothPoly(
        w, h,
        floatArrayOf(-0.04f, 0.5f, 0.06f, 0.478f, 0.2f, 0.47f, 0.34f, 0.478f, 0.485f, 0.482f, 0.6f, 0.476f, 0.72f, 0.468f, 0.82f, 0.448f, 0.9f, 0.43f, 0.96f, 0.414f),
    )
    val riverF1 = river.fAtY(0.615f * h)
    val riverF2 = river.fAtY(0.893f * h)
    val riverF3 = river.fAtY(0.483f * h)

    // ---- Heileberget's cable car and hiking trail
    val cableTop = Offset(0.315f * w, 0.135f * h)
    val cableBottom = Offset(0.375f * w, 0.432f * h)
    val trail: Poly = smoothPoly(
        w, h,
        floatArrayOf(0.372f, 0.436f, 0.33f, 0.405f, 0.305f, 0.375f, 0.335f, 0.345f, 0.3f, 0.315f, 0.33f, 0.285f, 0.298f, 0.255f, 0.325f, 0.225f, 0.296f, 0.2f, 0.318f, 0.175f, 0.325f, 0.155f),
        6,
    )
    val trailPath: Path = trail.path()

    /** A point on the cable car's cable, [s] from the top (0) to the valley station (1). */
    fun cablePoint(s: Float): Offset = Offset(mix(cableTop.x, cableBottom.x, s), mix(cableTop.y, cableBottom.y, s) + 4f * 0.012f * h * s * (1f - s))

    val cablePath: Path = Path().apply {
        moveTo(cableTop.x, cableTop.y)
        for (k in 1..16) {
            val o = cablePoint(k / 16f)
            lineTo(o.x, o.y)
        }
    }
    val trailDashes: Path = trail.dashes(0.010f * h, 0.007f * h)

    /** The secret path from the cellar of Storhuset to the troll cave; drawn only when all five golden keys are found. */
    val tunnel: TunnelGeo by lazy { buildTunnel(this) }

    // ---- trees, houses, lamps and other little things placed on the terrain
    val scatter: Scatter = buildScatter(this)
}

/** The first place along the line that reaches map height [yy] (pixels). */
internal fun Poly.fAtY(yy: Float): Float {
    for (i in 0 until n - 1) {
        val a = y[i] - yy
        val b = y[i + 1] - yy
        if (a * b <= 0f) {
            val l = y[i + 1] - y[i]
            val r = if (abs(l) < 1e-4f) 0f else (yy - y[i]) / l
            return (cum[i] + r * (cum[i + 1] - cum[i])) / len
        }
    }
    return 0.5f
}

// ===================================================================================== drawing

private fun MapPen.gc(day: Color, snowC: Color): Color = if (snow) snowC else day

/** Sky-ward things: the ranges with layered haze, Heileberget with its snowfields, the waterfall and the nearer mountains. */
internal fun MapPen.drawFarWorld(d: DrawScope, g: MapGeo) = with(d) {
    val haze = lerp(Color(0xFFCDE6F8), Color(0xFF3A2F7A), n)
    for ((i, b) in g.batches.withIndex()) {
        val k = if (i < 2) 0.65f else 0.5f
        val lit = nt(b.cLit, k)
        val sh = nt(b.cShade, k)
        for (part in b.parts) {
            drawPath(part.body, lit)
            drawPath(part.facet, nt(b.cFacet, k))
            drawPath(part.shade, sh)
            drawPath(part.strata, sh.darken(0.22f), alpha = 0.55f, style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
            drawPath(part.snow, nt(b.cSnow, 0.5f))
            drawPath(part.snowShade, nt(b.cSnowShade, 0.5f))
            drawPath(part.ridge, Ink.line, alpha = if (i < 2) 0.25f else if (i == 2) 0.45f else 0.85f, style = Stroke(lw * if (i < 3) 0.9f else 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        // Each step nearer is less hazy: the far ranges fade into the sky and the valley mist.
        val mist = when (i) { 0 -> 0.42f; 1 -> 0.3f; 2 -> 0.2f; else -> 0f }
        if (mist > 0f) drawRect(Brush.verticalGradient(listOf(haze.copy(alpha = mist * 0.35f), haze.copy(alpha = mist)), startY = 0f, endY = h * 0.47f), Offset(0f, 0f), Size(w, h * 0.47f))
        if (i == 3) {
            drawHeileExtras(this, g)
            drawWaterfall(this, g)
        }
        if (i >= 3) drawSlopePines(this, g, i)
        if (i == 3) drawCloudBanner(this)
    }
    drawTrollFace(this)
    drawTrail(this, g)
    // On dark nights the northern lights wash the peaks in green and violet.
    val aw = ramp((n - 0.3f) / 0.55f) * (1f - 0.6f * oc)
    if (aw > 0.01f) {
        drawRect(Brush.verticalGradient(listOf(Pal.auroraGreen.copy(alpha = 0.16f * aw), Pal.auroraGreen.copy(alpha = 0f)), startY = 0f, endY = h * 0.36f), Offset.Zero, Size(w, h * 0.36f))
        drawRect(Brush.horizontalGradient(listOf(Pal.auroraViolet.copy(alpha = 0f), Pal.auroraViolet.copy(alpha = 0.12f * aw)), startX = w * 0.4f, endX = w), Offset(w * 0.4f, 0f), Size(w * 0.6f, h * 0.3f))
    }
}

/**
 * A long banner of cloud along Heileberget's flank. It hangs in one place: it has to lie behind the cable car,
 * the trail and the houses, so it belongs to the still scenery.
 */
private fun MapPen.drawCloudBanner(d: DrawScope) = with(d) {
    val body = lerp(if (oc > 0.3f) Color(0xFFDDE3EE) else Color.White, Color(0xFF5A5496), n * 0.75f)
    val shade = lerp(if (oc > 0.3f) Color(0xFFB4BDD0) else Color(0xFFCFE0F4), Color(0xFF3A3578), n * 0.75f)
    val shadePath = Path()
    val bodyPath = Path()
    for (k in 0 until 2) {
        val cx = wrap(0.1f + k * 0.55f, 1.5f) - 0.25f
        val cy = (0.235f + 0.06f * k) * h
        for (j in 0 until 6) {
            val x = (cx + (j - 2.5f) * 0.045f) * w
            val rw = w * (0.05f + 0.022f * hash01(j + k * 6, 961))
            val rh = h * (0.016f + 0.01f * hash01(j + k * 6, 962))
            val y = cy + (hash01(j + k * 6, 963) - 0.5f) * h * 0.012f
            bodyPath.addOval(Rect(x - rw, y - rh, x + rw, y + rh))
            shadePath.addOval(Rect(x - rw * 0.96f, y - rh * 0.5f, x + rw * 1.02f, y + rh * 1.35f))
        }
    }
    drawPath(shadePath, shade, alpha = 0.45f)
    drawPath(bodyPath, body, alpha = 0.55f)
}

/** The great rock face, scree slopes and the summit flag of Heileberget. */
private fun MapPen.drawHeileExtras(d: DrawScope, g: MapGeo) = with(d) {
    // The big rock face behind the hut.
    drawPath(g.rockFace, nt(Color(0xFF6A769A), 0.5f))
    drawPath(g.rockFaceShade, nt(Color(0xFF4A5478), 0.5f))
    drawPath(g.rockStrata, nt(Color(0xFF38426A), 0.5f), alpha = 0.7f, style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
    drawPath(g.snowLedges, nt(Color(0xFFF7FAFF), 0.45f))
    drawPath(g.rockFace, Ink.line, alpha = 0.8f, style = Stroke(lw * 1.3f, join = StrokeJoin.Round))
    // Scree.
    drawPath(g.scree, nt(if (snow) Color(0xFFE6EBF5) else Color(0xFFB9B0A2), 0.5f))
    drawPath(g.screeShade, nt(if (snow) Color(0xFFC3CEE2) else Color(0xFF928A80), 0.5f))
    drawPoints(g.screeDots, PointMode.Points, nt(Color(0xFF6E6A78), 0.5f), strokeWidth = lw * 1.8f, cap = StrokeCap.Round, alpha = 0.7f)
    drawPath(g.scree, Ink.line, alpha = 0.4f, style = Stroke(lw * 0.8f, join = StrokeJoin.Round))
    // A summit cross with a little flag on the highest peak.
    val top = Offset(0.47f * w, 0.012f * h)
    drawLine(Ink.line, top, Offset(top.x, top.y - h * 0.03f), strokeWidth = lw * 1.6f, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset(top.x - h * 0.009f, top.y - h * 0.021f), Offset(top.x + h * 0.009f, top.y - h * 0.021f), strokeWidth = lw * 1.6f, cap = StrokeCap.Round)
}

private fun MapPen.drawSlopePines(d: DrawScope, g: MapGeo, i: Int) {
    val band = g.scatter.slopes.getOrNull(i) ?: return
    with(d) {
        drawPath(band.shade, nt(Color(0xFF1F5C44), 0.5f))
        drawPath(band.lit, nt(if (snow) Color(0xFF3F7F63) else Color(0xFF2C7A52), 0.5f))
        if (snow) drawPath(band.cap, nt(Color.White, 0.4f))
        drawPath(band.lit, Ink.line, alpha = 0.4f, style = Stroke(lw * 0.6f))
    }
}

/** A big friendly troll face hidden in the rock left of the waterfall. It blinks now and then. */
private fun MapPen.drawTrollFace(d: DrawScope) = with(d) {
    val c = Offset(0.41f * w, 0.25f * h)
    val sz = h * 0.07f
    val rock = nt(Color(0xFF9DA8C4), 0.5f)
    val line = rock.darken(0.5f)
    for (side in intArrayOf(-1, 1)) {
        val e = Offset(c.x + side * sz * 0.34f, c.y - sz * 0.18f)
        drawArc(line, 200f, 140f, false, Offset(e.x - sz * 0.22f, e.y - sz * 0.3f), Size(sz * 0.44f, sz * 0.3f), alpha = 0.4f, style = Stroke(lw * 1.3f, cap = StrokeCap.Round))
        drawOval(Color(0xFF3B3346), Offset(e.x - sz * 0.06f, e.y - sz * 0.06f), Size(sz * 0.12f, sz * 0.12f), alpha = 0.5f)
    }
    val nose = Path().apply {
        moveTo(c.x - sz * 0.08f, c.y - sz * 0.12f)
        quadraticTo(c.x - sz * 0.22f, c.y + sz * 0.2f, c.x, c.y + sz * 0.2f)
        quadraticTo(c.x + sz * 0.2f, c.y + sz * 0.2f, c.x + sz * 0.1f, c.y - sz * 0.05f)
    }
    drawPath(nose, rock.lighten(0.12f), alpha = 0.6f)
    drawPath(nose, line, alpha = 0.35f, style = Stroke(lw, cap = StrokeCap.Round))
    drawArc(line, 15f, 150f, false, Offset(c.x - sz * 0.36f, c.y + sz * 0.05f), Size(sz * 0.72f, sz * 0.42f), alpha = 0.4f, style = Stroke(lw * 1.2f, cap = StrokeCap.Round))
}

/** The cliff, the water pouring from its lip and the side veil beside the troll cave. */
private fun MapPen.drawWaterfall(d: DrawScope, g: MapGeo) = with(d) {
    val f = g.fall
    val rock = nt(Color(0xFF82869C), 0.55f)
    drawPath(f.cliff, rock)
    drawPath(f.ledges, rock.lighten(0.2f))
    drawPath(f.cliffShade, nt(Color(0xFF5E6380), 0.55f))
    drawPath(f.cracks, Ink.line, alpha = 0.5f, style = Stroke(lw, cap = StrokeCap.Round))
    drawPath(f.moss, nt(Color(0xFF5FA05A), 0.5f))
    drawPath(f.cliff, Ink.line, style = Stroke(lw * 1.5f, join = StrokeJoin.Round))
    // The snow-melt stream.
    drawPath(f.stream, Ink.line, style = Stroke(0.011f * h + lw * 2f, cap = StrokeCap.Round))
    drawPath(f.stream, nt(Color(0xFFA8DDF5), 0.4f), style = Stroke(0.011f * h, cap = StrokeCap.Round))
    // The falling water.
    drawPath(f.water, Brush.verticalGradient(listOf(nt(Color(0xFFB9E8FB), 0.3f), nt(Color(0xFFEFFAFF), 0.25f)), startY = f.top, endY = f.bottom))
    // Left half in light, right half a little shaded.
    val shadeHalf = Path().apply {
        moveTo(f.cx + f.tw * 0.05f, f.top)
        lineTo(f.cx + f.tw / 2f, f.top)
        quadraticTo(f.cx + f.tw * 0.66f, (f.top + f.bottom) / 2f, f.cx + f.bw / 2f, f.bottom)
        lineTo(f.cx + f.bw * 0.1f, f.bottom)
        quadraticTo(f.cx + f.tw * 0.2f, (f.top + f.bottom) / 2f, f.cx + f.tw * 0.05f, f.top)
        close()
    }
    drawPath(shadeHalf, nt(Color(0xFF6FB6DC), 0.4f), alpha = 0.35f)
    drawPath(f.rim, Ink.line, alpha = 0.7f, style = Stroke(lw * 1.3f, cap = StrokeCap.Round))
    drawPath(f.lip, nt(Color(0xFFE9F8FF), 0.3f))
    drawPath(f.lip, Ink.line, alpha = 0.6f, style = Stroke(lw))
    // A second, thin fall beside the cave.
    drawPath(f.veil, Brush.verticalGradient(listOf(nt(Color(0xFFBFE9FA), 0.3f), nt(Color(0xFFF2FBFF), 0.25f)), startY = f.veilTop, endY = f.veilBottom), alpha = 0.92f)
}

/** The valley floor, foothills, hills and the terraced farm hill. */
internal fun MapPen.drawGround(d: DrawScope, g: MapGeo) = with(d) {
    val top = nt(gc(Color(0xFF88C66A), Color(0xFFEAF0FA)), 0.5f)
    val bottom = nt(gc(Color(0xFF63AE50), Color(0xFFDDE7F5)), 0.5f)
    val dim = oc * 0.12f
    drawPath(g.valley, Brush.verticalGradient(listOf(top.darken(dim), bottom.darken(dim)), startY = g.valleyTop, endY = h))
    drawPath(g.meadowLight, nt(gc(Color(0xFFA7D86F), Color(0xFFF7FAFF)), 0.5f), alpha = 0.55f)
    drawPath(g.meadowDark, nt(gc(Color(0xFF5DA84E), Color(0xFFCBD8EC)), 0.5f), alpha = 0.45f)
    drawPath(g.ticks, nt(gc(Color(0xFF4C9A48), Color(0xFFB4C4DE)), 0.5f), alpha = 0.6f, style = Stroke(lw * 0.8f, cap = StrokeCap.Round))
    if (!snow) {
        drawPoints(g.daisiesA, PointMode.Points, nt(Color(0xFFFFFFF0), 0.4f), strokeWidth = h * 0.005f, cap = StrokeCap.Round)
        drawPoints(g.daisiesB, PointMode.Points, nt(Color(0xFFFFD85A), 0.4f), strokeWidth = h * 0.004f, cap = StrokeCap.Round)
    }
    for (hill in g.hills) drawHill(this, hill)
    drawFarmHill(this, g)
}

private fun MapPen.hillColors(tone: Int): Pair<Color, Color> = when (tone) {
    2 -> gc(Color(0xFF4F9A58), Color(0xFFDDE8F5)) to gc(Color(0xFF357A4A), Color(0xFFB9C9E2))
    1 -> gc(Color(0xFF9ACF64), Color(0xFFF2F6FD)) to gc(Color(0xFF6DB056), Color(0xFFC7D5EA))
    else -> gc(Color(0xFF86C862), Color(0xFFEDF2FB)) to gc(Color(0xFF5AA550), Color(0xFFC1D0E6))
}

private fun MapPen.drawHill(d: DrawScope, hill: HillGeo) = with(d) {
    val (lit, sh) = hillColors(hill.tone)
    drawPath(hill.base, nt(Color(0xFF2B5A35), 0.4f), alpha = 0.22f, style = Stroke(hill.ry * 0.12f, cap = StrokeCap.Round))
    drawPath(hill.body, nt(lit, 0.5f))
    drawPath(hill.contour, nt(sh, 0.5f), alpha = 0.55f, style = Stroke(lw * 0.9f, cap = StrokeCap.Round))
    drawPath(hill.shade, nt(sh, 0.5f))
    drawPath(hill.rim, Ink.line, alpha = 0.75f, style = Stroke(lw * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun MapPen.drawFarmHill(d: DrawScope, g: MapGeo) = with(d) {
    val hill = g.farmHillGeo
    val (lit, sh) = hillColors(0)
    drawPath(hill.body, nt(lit, 0.5f))
    drawPath(hill.shade, nt(sh, 0.5f))
    for (t in g.terraces) {
        val field = if (snow) Color(0xFFF1F5FC) else t.color
        drawPath(t.top, nt(field, 0.5f))
        if (!snow) drawPath(t.rows, nt(Color(0xFF6E5A36), 0.5f), alpha = 0.38f, style = Stroke(lw * 0.8f, cap = StrokeCap.Round))
        drawPath(t.wall, nt(if (snow) Color(0xFFB8C4DA) else Color(0xFF9B8B75), 0.5f))
        drawPath(t.wall, Ink.line, alpha = 0.6f, style = Stroke(lw * 0.8f, join = StrokeJoin.Round))
        drawPath(t.lip, nt(if (snow) Color.White else Color(0xFFB9DD6F), 0.5f), alpha = 0.9f, style = Stroke(lw * 1.6f, cap = StrokeCap.Round))
        drawPath(t.lip, Ink.line, alpha = 0.45f, style = Stroke(lw * 0.7f))
    }
    drawPath(hill.rim, Ink.line, alpha = 0.75f, style = Stroke(lw * 1.2f, cap = StrokeCap.Round))
}

/** Rivers, the pool, the fjord with its cliffs, the beach band and the mouth of the river. */
internal fun MapPen.drawWaters(d: DrawScope, g: MapGeo) = with(d) {
    // ---- the river
    val rim = nt(gc(Color(0xFFBFA173), Color(0xFFD7DFEC)), 0.5f)
    drawPath(g.riverRim, rim)
    drawPath(g.riverRim, Ink.line, alpha = 0.7f, style = Stroke(lw * 1.2f, join = StrokeJoin.Round))
    val deep = nt(if (snow) Color(0xFF8FBCE4) else Color(0xFF2C86C8), 0.5f)
    val lit = nt(if (snow) Color(0xFFB9D9F3) else Color(0xFF52B6E2), 0.5f)
    drawPath(g.riverWater, deep)
    translate(h * 0.0035f, h * 0.0035f) { drawPath(g.riverLit, lit) }
    drawPath(g.rapids, Color.White, alpha = 0.85f, style = Stroke(lw * 1.5f, cap = StrokeCap.Round))
    drawPath(g.bankStones, nt(Color(0xFF8C8FA3), 0.5f))
    drawPath(g.bankStonesLit, nt(Color(0xFFB5B8C8), 0.5f))
    drawPath(g.bankStones, Ink.line, alpha = 0.5f, style = Stroke(lw * 0.6f))
    drawPath(g.reeds, nt(Color(0xFF4F9A48), 0.5f), style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
    drawPoints(g.cattails, PointMode.Points, nt(Color(0xFF8A5A3A), 0.4f), strokeWidth = lw * 2.6f, cap = StrokeCap.Round)
    // ---- the plunge pool
    val p = g.pool
    drawOval(nt(Color(0xFFBFA173), 0.5f), Offset(p.left - h * 0.006f, p.top - h * 0.002f), Size(p.width + h * 0.012f, p.height + h * 0.014f))
    drawOval(Ink.line, Offset(p.left - h * 0.006f, p.top - h * 0.002f), Size(p.width + h * 0.012f, p.height + h * 0.014f), alpha = 0.7f, style = Stroke(lw * 1.3f))
    drawOval(nt(Color(0xFF1FA6C0), 0.5f), p.topLeft, p.size)
    drawOval(nt(Color(0xFF6CDCDD), 0.5f), Offset(p.left + p.width * 0.08f, p.top + p.height * 0.06f), Size(p.width * 0.8f, p.height * 0.6f), alpha = 0.85f)
    drawOval(nt(Color(0xFFB8F2EC), 0.5f), Offset(p.left + p.width * 0.26f, p.top + p.height * 0.1f), Size(p.width * 0.4f, p.height * 0.3f), alpha = 0.6f)
    val ringStroke = Stroke(lw * 1.2f, cap = StrokeCap.Round)
    drawArc(Color.White, 10f, 160f, false, Offset(p.left + p.width * 0.05f, p.top + p.height * 0.12f), Size(p.width * 0.9f, p.height * 0.78f), alpha = 0.75f, style = ringStroke)
    // Rocks around the pool.
    val rocks = Path()
    for (i in 0 until 11) {
        val a = -0.25f + (3.4f) * i / 10f
        val rx = p.center.x + cos(a) * p.width * 0.52f
        val ry = p.center.y + sin(a) * p.height * 0.52f + p.height * 0.06f
        val r = h * (0.006f + 0.005f * hash01(i, 431))
        rocks.addOval(Rect(rx - r * 1.4f, ry - r, rx + r * 1.4f, ry + r))
    }
    drawPath(rocks, nt(Color(0xFF8C8FA3), 0.5f))
    drawPath(rocks, Ink.line, alpha = 0.6f, style = Stroke(lw * 0.7f))
    // (The little rainbow in the spray is drawn live, over the mist.)
    if (n > 0f) drawCircle(Color(0xFFBFE6FF), h * 0.05f, Offset(g.fall.cx, p.top + p.height * 0.1f), alpha = 0.16f * n)

    // ---- the fjord
    val sandC = nt(gc(Color(0xFFF0D9A0), Color(0xFFF7F9FF)), 0.5f)
    drawPath(g.sandBand, sandC, style = Stroke(h * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(g.sandBand, nt(Color(0xFFD9BC82), 0.5f), alpha = 0.6f, style = Stroke(h * 0.012f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    val deepSea = nt(Color(0xFF2A62B2), 0.6f)
    val midSea = nt(Color(0xFF3B8ACB), 0.6f)
    drawPath(g.sea, Brush.verticalGradient(listOf(midSea, deepSea), startY = h * 0.68f, endY = h))
    clipPath(g.sea) {
        drawPath(g.shoreLine, nt(Color(0xFF6ED3E0), 0.6f), alpha = 0.75f, style = Stroke(h * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(g.shoreLine, nt(Color(0xFF9BE8EC), 0.6f), alpha = 0.6f, style = Stroke(h * 0.022f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // A deeper swirl of colour out in the middle of the fjord.
        for (k in 0 until 3) {
            val ph = k / 3f
            drawOval(deepSea.darken(0.1f), Offset((0.62f + 0.3f * ph) * w, (0.95f - 0.02f * k) * h), Size(0.2f * w, 0.04f * h), alpha = 0.22f)
        }
        // Waves behind the pier and the buoy stay put; the others drift (live).
        drawPath(g.wavesStill, Color.White, alpha = 0.65f * (1f - 0.4f * n), style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
        // The foam line along the shore.
        drawPath(g.shoreFoam, Color.White, alpha = 0.9f * (1f - 0.35f * n), style = Stroke(lw * 2.2f, cap = StrokeCap.Round))
    }
    drawPath(g.shoreLine, Ink.line, alpha = 0.55f, style = Stroke(lw * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Cliff walls where the farm hill meets the water.
    val rock = nt(Color(0xFF8B93A9), 0.5f)
    drawPath(g.cliffWall, nt(Color(0xFF666E8C), 0.5f))
    drawPath(g.cliffLit, rock)
    drawPath(g.cliffCracks, Ink.line, alpha = 0.5f, style = Stroke(lw, cap = StrokeCap.Round))
    drawPath(g.cliffWall, Ink.line, alpha = 0.85f, style = Stroke(lw * 1.4f, join = StrokeJoin.Round))
    drawPath(g.cliffFoot, Color.White, alpha = 0.85f, style = Stroke(lw * 2f, cap = StrokeCap.Round))
    // The river's mouth: a little fan of lighter water.
    val mouth = g.river.at(1f)
    val mo = g.shore.nearestF(mouth.x, mouth.y).let { g.shore.at(it) }
    drawOval(nt(Color(0xFF7EE0E8), 0.5f), Offset(mo.x - h * 0.05f, mo.y - h * 0.006f), Size(h * 0.1f, h * 0.03f), alpha = 0.7f)
}
