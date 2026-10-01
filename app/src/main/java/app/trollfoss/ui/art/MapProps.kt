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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The things that make the terrain a place: the road network, trees placed where trees grow, cottages
 * along the roads, bridges over the river, the railway and its train, and every little moving thing.
 */

internal const val BAND_H = 0.04f
internal const val NBANDS = 26

// ------------------------------------------------------------------------------------- roads

internal fun buildRoads(w: Float, h: Float): List<Poly> {
    val roads = listOf(
        // 0: the main road through the valley, west to east.
        floatArrayOf(-0.04f, 0.655f, 0.05f, 0.632f, 0.12f, 0.624f, 0.2f, 0.634f, 0.26f, 0.63f, 0.33f, 0.626f, 0.4f, 0.614f, 0.45f, 0.624f, 0.488f, 0.618f, 0.53f, 0.624f, 0.6f, 0.637f, 0.68f, 0.632f, 0.76f, 0.617f, 0.86f, 0.61f, 0.95f, 0.622f, 1.04f, 0.642f),
        // 1: south from the shop to the salon.
        floatArrayOf(0.272f, 0.63f, 0.283f, 0.7f, 0.298f, 0.77f, 0.316f, 0.835f, 0.325f, 0.896f),
        // 2: the southern street by the water, west to the pier.
        floatArrayOf(0.2f, 0.88f, 0.3f, 0.903f, 0.38f, 0.912f, 0.46f, 0.902f, 0.53f, 0.896f, 0.6f, 0.912f, 0.68f, 0.924f, 0.74f, 0.912f, 0.8f, 0.9f),
        // 3: from the bakery to the concert house.
        floatArrayOf(0.665f, 0.634f, 0.655f, 0.72f, 0.632f, 0.8f, 0.612f, 0.914f),
        // 4: from the bakery to the beach.
        floatArrayOf(0.7f, 0.634f, 0.72f, 0.72f, 0.735f, 0.8f, 0.742f, 0.912f),
        // 5: the mountain road, winding up to the cabin.
        floatArrayOf(0.218f, 0.634f, 0.22f, 0.57f, 0.205f, 0.51f, 0.198f, 0.455f, 0.212f, 0.405f, 0.225f, 0.365f, 0.205f, 0.33f, 0.19f, 0.302f),
        // 6: the path up to the troll cave.
        floatArrayOf(0.606f, 0.637f, 0.612f, 0.57f, 0.62f, 0.51f, 0.624f, 0.45f, 0.612f, 0.4f, 0.598f, 0.36f, 0.61f, 0.33f, 0.618f, 0.312f),
        // 7: the farm track, down to the fjord cliffs.
        floatArrayOf(0.86f, 0.61f, 0.883f, 0.655f, 0.9f, 0.69f),
    ).map { smoothPoly(w, h, it, 7) }
    // 8: the lane from the gate of Storhuset to the troll cave road.
    return roads + manorLane(w, h, roads[6])
}

// ------------------------------------------------------------------------------------- trees

internal class TreeBand {
    val trunks = Path()
    val pale = Path()
    val pineLit = Path()
    val pineShade = Path()
    val shadeA = Path()
    val litA = Path()
    val shadeB = Path()
    val litB = Path()
    val hi = Path()
    val cap = Path()
    val fruit = ArrayList<Offset>()
    var any = false
}

internal class SlopeBand(val lit: Path, val shade: Path, val cap: Path)

internal class Cottage(val x: Float, val y: Float, val sc: Float, val wall: Color, val roof: Color, val kind: Int)

internal class Scatter(
    val bands: Array<TreeBand>,
    val cottages: List<Cottage>,
    val slopes: List<SlopeBand?>,
    val poles: Path,
    val heads: List<Offset>,
)

private class Keep(val x: Float, val y: Float, val rx: Float, val ry: Float) {
    fun hit(px: Float, py: Float, pad: Float = 0f): Boolean {
        val dx = (px - x) / (rx + pad)
        val dy = (py - y) / (ry + pad)
        return dx * dx + dy * dy < 1f
    }
}

private fun depthK(y: Float) = 0.84f + 0.22f * ((y - 0.25f) / 0.6f).coerceIn(0f, 1f)

private fun TreeBand.plant(x: Float, y: Float, size: Float, kind: Int, salt: Int, h: Float) {
    any = true
    when (kind) {
        0 -> {
            val pw = size * 0.52f
            trunks.addRect(Rect(x - size * 0.035f, y - size * 0.12f, x + size * 0.035f, y))
            pineLit.addPine(x, y - size * 0.06f, pw, size * 0.94f)
            pineShade.addPineShade(x, y - size * 0.06f, pw, size * 0.94f)
            cap.poly(x, y - size, x + pw * 0.2f, y - size * 0.8f, x - pw * 0.2f, y - size * 0.8f)
        }
        else -> {
            val r = size * 0.42f
            val cy = y - size * 0.55f
            (if (kind == 2) pale else trunks).addRect(Rect(x - size * 0.03f, cy, x + size * 0.03f, y))
            val shade = if (kind == 1) shadeA else shadeB
            val lit = if (kind == 1) litA else litB
            shade.addPath(crownPath(x, cy, r, r * 0.92f, salt))
            lit.addPath(crownPath(x - r * 0.14f, cy - r * 0.16f, r * 0.88f, r * 0.8f, salt + 1))
            hi.addOval(Rect(x - r * 0.62f, cy - r * 0.68f, x - r * 0.12f, cy - r * 0.34f))
            if (kind == 3 && hash01(salt, 77) < 0.14f) fruit.add(Offset(x + r * 0.3f, cy + r * 0.1f))
        }
    }
}

internal fun buildScatter(g: MapGeo): Scatter {
    val w = g.w
    val h = g.h
    val bands = Array(NBANDS) { TreeBand() }
    fun band(y: Float) = bands[(y / (BAND_H * h)).toInt().coerceIn(0, NBANDS - 1)]

    // ---- keep-out zones around the landmarks and the waterfall
    val sU = min(h * 0.15f, w * 0.0825f)
    val keeps = ArrayList<Keep>()
    for (p in PlaceId.entries.filter { it.onMap }) {
        val b = g.bases[p]!!
        val rx = sU * when (p) {
            PlaceId.TIVOLI -> 1.9f
            PlaceId.FARM -> 1.55f
            PlaceId.BEACH -> 1.4f
            PlaceId.MOUNTAIN, PlaceId.LAB -> 1.3f
            PlaceId.MANOR_GROUND -> 2.3f
            else -> 1.15f
        }
        val ry = h * if (p == PlaceId.TIVOLI) 0.19f else if (p == PlaceId.MANOR_GROUND) 0.2f else 0.13f
        keeps.add(Keep(b.x, b.y - ry * 0.55f, rx, ry))
        keeps.add(Keep(b.x, b.y + 0.02f * h, rx, 0.05f * h))
    }
    val special = listOf(
        Keep(0.49f * w, 0.29f * h, 0.11f * w, 0.17f * h), // waterfall and pool
        Keep(0.41f * w, 0.25f * h, 0.05f * w, 0.05f * h), // the troll
        Keep(0.19f * w, 0.25f * h, 0.07f * w, 0.14f * h), // the ski slope
        Keep(0.61f * w, 0.26f * h, 0.05f * w, 0.1f * h), // the cave
        Keep(0.95f * w, 0.42f * h, 0.04f * w, 0.03f * h), // the tunnel
        Keep(0.335f * w, 0.29f * h, 0.045f * w, 0.15f * h), // cable car and trail
    )
    fun blocked(x: Float, y: Float, pad: Float): Boolean {
        for (k in keeps) if (k.hit(x, y, pad)) return true
        for (k in special) if (k.hit(x, y, pad)) return true
        return false
    }
    fun wet(x: Float, y: Float, margin: Float): Boolean {
        if (g.inSea(x, y + margin)) return true
        if (g.river.dist(x, y) < g.riverWidth(g.river.nearestF(x, y)) * 0.5f + margin) return true
        return false
    }
    fun onRoad(x: Float, y: Float, pad: Float): Boolean {
        for (r in g.roads) if (r.dist(x, y) < pad) return true
        if (g.rail.dist(x, y) < pad * 1.2f) return true
        return false
    }

    // ---- pines climbing the slopes of the big mountains
    val slopes = ArrayList<SlopeBand?>()
    for ((bi, b) in g.batches.withIndex()) {
        if (bi < 2) {
            slopes.add(null)
            continue
        }
        val lit = Path()
        val shade = Path()
        val cap = Path()
        var count = 0
        for (m in b.mounts) {
            for (i in 0 until 160) {
                val x = mix(m.x0, m.x1, hash01(i, 600 + bi))
                val top = m.topY(x, w, h)
                val yLow = 0.412f
                val y = mix(max(top + 0.05f, 0.27f), yLow, hash01(i, 610 + bi))
                if (y <= top + 0.03f) continue
                val r = (yLow - y) / 0.15f
                if (hash01(i, 620 + bi) > 0.95f - r * 0.85f) continue
                if (special.any { it.hit(x * w, y * h, 0.004f * w) }) continue
                if (keeps.any { it.hit(x * w, y * h) }) continue
                val sz = (0.04f + 0.03f * hash01(i, 630 + bi)) * h * (0.75f + 0.25f * (1f - r))
                val px = x * w
                val py = y * h
                lit.addPine(px, py, sz * 0.5f, sz)
                shade.addPineShade(px, py, sz * 0.5f, sz)
                cap.poly(px, py - sz, px + sz * 0.12f, py - sz * 0.78f, px - sz * 0.12f, py - sz * 0.78f)
                count++
                if (count > 150) break
            }
        }
        slopes.add(SlopeBand(lit, shade, cap))
    }

    // ---- trees on the land
    val placed = ArrayList<Offset>()
    var planted = 0
    for (i in 0 until 3200) {
        if (planted >= 330) break
        val xf = hash01(i, 501) * 1.06f - 0.03f
        val yf = 0.36f + hash01(i, 502) * 0.66f
        val x = xf * w
        val y = yf * h
        var p = 0.05f
        var pine = 0.35f
        if (yf < 0.472f) { p = 0.8f; pine = 0.93f }
        if (xf < 0.085f && yf > 0.47f) { p = 0.75f; pine = 0.75f }
        if (xf < 0.26f && yf > 0.69f) { p = max(p, 0.28f); pine = 0.45f }
        if (yf > 0.66f && xf in 0.36f..0.5f && yf < 0.8f) p = max(p, 0.16f)
        val rd = g.river.dist(x, y)
        if (rd < 0.07f * h) { p = max(p, 0.3f); pine = 0.15f }
        if (xf > 0.7f && yf in 0.44f..0.76f) p = 0f // the terraced farm hill
        if (xf > 0.66f && yf in 0.66f..0.85f) p = min(p, 0.06f)
        if (hash01(i, 503) > p) continue
        if (blocked(x, y, 0.012f * h)) continue
        if (wet(x, y, 0.014f * h)) continue
        if (onRoad(x, y, 0.016f * h)) continue
        if (placed.any { hypot(it.x - x, it.y - y) < 0.028f * h }) continue
        placed.add(Offset(x, y))
        val k = depthK(yf)
        val kind = if (hash01(i, 504) < pine) 0 else if (hash01(i, 505) < 0.3f) 2 else if (hash01(i, 506) < 0.3f) 3 else 1
        val size = (0.048f + 0.03f * hash01(i, 507)) * h * k
        band(y).plant(x, y, if (kind == 0) size else size * 0.9f, kind, i, h)
        planted++
    }

    // ---- cottages along the roads
    val cottages = ArrayList<Cottage>()
    val walls = listOf(Color(0xFFF5E3B5), Color(0xFFFFF6EE), Color(0xFFB9DAF0), Color(0xFFC84A3C), Color(0xFFE3A08A), Color(0xFFD9E8B8))
    val roofs = listOf(Color(0xFF55505E), Color(0xFFA6453A), Color(0xFF4F6E8C), Color(0xFF6E5A4A))
    var ci = 0
    val plan = listOf(0 to 0.06f, 0 to 0.17f, 0 to 0.3f, 0 to 0.52f, 0 to 0.7f, 0 to 0.93f, 1 to 0.3f, 1 to 0.5f, 1 to 0.7f, 2 to 0.07f, 2 to 0.2f, 2 to 0.43f, 3 to 0.35f, 3 to 0.6f, 4 to 0.4f, 4 to 0.72f, 5 to 0.25f, 5 to 0.5f, 6 to 0.55f, 6 to 0.3f)
    for ((ri, f0) in plan) {
        val r = g.roads[ri]
        for (attempt in 0 until 6) {
            val f = (f0 + attempt * 0.013f).coerceIn(0.03f, 0.97f)
            val c = r.at(f)
            val d = r.dir(f)
            val side = if ((ci + attempt) % 2 == 0) 1f else -1f
            val off = (0.045f + 0.008f * hash01(ci, 801)) * h
            val x = c.x - d.y * off * side
            val y = c.y + d.x * off * side
            if (x < 0.02f * w || x > 0.97f * w) continue
            if (blocked(x, y, 0.012f * h)) continue
            if (wet(x, y, 0.02f * h)) continue
            if (g.rail.dist(x, y) < 0.03f * h) continue
            if (onRoad(x, y, 0.014f * h)) continue
            if (cottages.any { hypot(it.x - x, it.y - y) < 0.075f * h }) continue
            if (x > 0.72f * w && y in 0.45f * h..0.7f * h) continue
            val kk = depthK(y / h)
            cottages.add(Cottage(x, y, (0.46f + 0.1f * hash01(ci, 802)) * kk, walls[(ci * 5 + 2) % walls.size], roofs[(ci * 3 + 1) % roofs.size], ci % 3))
            ci++
            break
        }
    }
    cottages.sortBy { it.y }

    // ---- lamp posts beside the roads (lit at night)
    val poles = Path()
    val heads = ArrayList<Offset>()
    for ((ri, r) in g.roads.withIndex()) {
        if (ri == 7 || ri == 8) continue
        var f = 0.07f + 0.02f * ri
        while (f < 0.95f) {
            val c = r.at(f)
            val d = r.dir(f)
            val off = 0.02f * h
            val x = c.x + d.y * off
            val y = c.y - d.x * off
            if (!blocked(x, y, 0f) && !wet(x, y, 0.01f * h) && x in 0f..w && g.rail.dist(x, y) > 0.03f * h) {
                val hh = 0.026f * h
                poles.moveTo(x, y)
                poles.lineTo(x, y - hh)
                heads.add(Offset(x, y - hh))
            }
            f += 0.16f
        }
    }
    return Scatter(bands, cottages, slopes, poles, heads)
}

// ------------------------------------------------------------------------------------- drawing

/** Trees of one depth band: trunks, pines and round crowns, each with a lit and a shaded side. */
internal fun MapPen.drawTreeBand(d: DrawScope, b: TreeBand) = with(d) {
    if (!b.any) return@with
    val trunk = nt(Color(0xFF7A5134), 0.4f)
    drawPath(b.trunks, trunk)
    drawPath(b.pale, nt(Color(0xFFF2EEE6), 0.4f))
    val pineC = nt(if (snow) Color(0xFF3F7F63) else pen.conifer(Color(0xFF2E8552)), 0.5f)
    drawPath(b.pineLit, pineC)
    drawPath(b.pineShade, pineC.darken(0.25f))
    drawPath(b.pineLit, Ink.line, alpha = 0.55f, style = Stroke(lw * 0.75f, join = StrokeJoin.Round))
    if (snow) drawPath(b.cap, nt(Color.White, 0.3f))
    // Leafy crowns follow the year: two greens in summer, orange and gold in autumn, fresh in spring.
    val a = nt(if (frost) Color(0xFFC6D4DE) else if (snow) Color(0xFF6E8F7C) else leaf(Color(0xFF5DB04F), 0), 0.5f)
    val bb = nt(if (frost) Color(0xFFDCE6EE) else if (snow) Color(0xFF8BA58A) else leaf(Color(0xFF9CCB54), 3), 0.5f)
    drawPath(b.shadeA, a.darken(if (frost) 0.14f else 0.28f))
    drawPath(b.litA, a)
    drawPath(b.shadeB, bb.darken(if (frost) 0.12f else 0.26f))
    drawPath(b.litB, bb)
    drawPath(b.hi, Color.White, alpha = if (snow) 0.5f else 0.22f)
    drawPath(b.shadeA, Ink.line, alpha = 0.55f, style = Stroke(lw * 0.75f, join = StrokeJoin.Round))
    drawPath(b.shadeB, Ink.line, alpha = 0.55f, style = Stroke(lw * 0.75f, join = StrokeJoin.Round))
    if (b.fruit.isNotEmpty() && !snow) drawPoints(b.fruit, PointMode.Points, nt(fruit, 0.4f), strokeWidth = h * 0.006f, cap = StrokeCap.Round)
}

/** The ground work of the infrastructure: plots, label shading, roads, the railway, bridges and lamps. */
internal fun MapPen.drawInfra(d: DrawScope, g: MapGeo) = with(d) {
    for (p in g.plotOrder) drawPlot(d, g, p)
    // A soft dark pool under each place's label so the white lettering reads on any ground.
    for (p in PlaceId.entries.filter { it.onMap }) {
        val s = mapSpot(p)
        val c = Offset(s.x * w, (s.y + 0.115f) * h)
        drawOval(Ink.line, Offset(c.x - w * 0.07f, c.y - h * 0.032f), Size(w * 0.14f, h * 0.064f), alpha = 0.06f)
        drawOval(Ink.line, Offset(c.x - w * 0.052f, c.y - h * 0.022f), Size(w * 0.104f, h * 0.044f), alpha = 0.07f)
    }
    drawRoads(d, g)
    drawRail(d, g)
    drawBridges(d, g)
    // Lamp posts.
    drawPath(g.scatter.poles, Ink.line, alpha = 0.85f, style = Stroke(lw * 1.6f, cap = StrokeCap.Round))
    val lit = ramp((n - 0.25f) / 0.4f)
    if (lit > 0f) {
        drawPoints(g.scatter.heads, PointMode.Points, Color(0xFFFFD76B), strokeWidth = h * 0.05f, cap = StrokeCap.Round, alpha = 0.16f * lit)
        drawPoints(g.scatter.heads, PointMode.Points, Color(0xFFFFE9A0), strokeWidth = h * 0.022f, cap = StrokeCap.Round, alpha = 0.3f * lit)
    }
    drawPoints(g.scatter.heads, PointMode.Points, lerp(Color(0xFFFFF3C0), Color(0xFFFFFFFF), lit), strokeWidth = h * 0.009f, cap = StrokeCap.Round)
}

private fun MapPen.drawRoads(d: DrawScope, g: MapGeo) = with(d) {
    val edge = nt(if (snow) Color(0xFFBFC9DA) else Color(0xFFB08A5A), 0.5f)
    val body = nt(if (snow) Color(0xFFF7F9FF) else Color(0xFFEBD6A2), 0.5f)
    val rw = 0.0125f * h
    for ((i, r) in g.roadPaths.withIndex()) {
        val wdt = if (i == 0) rw * 1.15f else rw
        drawPath(r, edge, style = Stroke(wdt + lw * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    for ((i, r) in g.roadPaths.withIndex()) {
        val wdt = if (i == 0) rw * 1.15f else rw
        drawPath(r, body, style = Stroke(wdt, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    for (r in g.roadDashes) drawPath(r, edge, alpha = 0.45f, style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
    for (r in g.doorPaths) {
        drawPath(r, edge, style = Stroke(rw * 0.72f + lw * 2f, cap = StrokeCap.Round))
        drawPath(r, body, style = Stroke(rw * 0.72f, cap = StrokeCap.Round))
    }
}

/** Rail bed and sleepers, the stone viaduct over the river, the tunnel portal and the steam train. */
private fun MapPen.drawRail(d: DrawScope, g: MapGeo) = with(d) {
    val rail = g.rail
    val stone = nt(if (snow) Color(0xFFDDE4F0) else Color(0xFFB7A98F), 0.5f)
    val stoneShade = nt(if (snow) Color(0xFFB8C4DA) else Color(0xFF8C7F6B), 0.5f)
    // The viaduct across the river.
    val vf = g.riverF3
    val vc = g.river.at(vf)
    val vw = 0.085f * w
    val vTop = g.rail.at(g.rail.nearestF(vc.x, vc.y)).y
    val body = Path().apply {
        addRect(Rect(vc.x - vw / 2f, vTop - h * 0.004f, vc.x + vw / 2f, vTop + h * 0.05f))
    }
    drawPath(body, stone)
    drawRect(stoneShade, Offset(vc.x + vw * 0.36f, vTop - h * 0.004f), Size(vw * 0.14f, h * 0.054f))
    for (k in 0 until 3) {
        val ax = vc.x + (k - 1) * vw * 0.3f
        val aw = if (k == 1) vw * 0.2f else vw * 0.13f
        val top = vTop + h * 0.014f
        val arch = Path().apply {
            moveTo(ax - aw / 2f, vTop + h * 0.05f)
            lineTo(ax - aw / 2f, top + aw * 0.45f)
            quadraticTo(ax - aw / 2f, top, ax, top)
            quadraticTo(ax + aw / 2f, top, ax + aw / 2f, top + aw * 0.45f)
            lineTo(ax + aw / 2f, vTop + h * 0.05f)
            close()
        }
        drawPath(arch, if (k == 1) nt(Color(0xFF2C86C8), 0.5f) else nt(Color(0xFF3B3340), 0.4f))
        drawPath(arch, Ink.line, alpha = 0.8f, style = Stroke(lw))
    }
    drawRect(Ink.line, Offset(vc.x - vw / 2f, vTop - h * 0.004f), Size(vw, h * 0.054f), alpha = 0.85f, style = Stroke(lw * 1.2f))
    drawRect(stone.lighten(0.25f), Offset(vc.x - vw / 2f - h * 0.004f, vTop - h * 0.01f), Size(vw + h * 0.008f, h * 0.008f))
    drawRect(Ink.line, Offset(vc.x - vw / 2f - h * 0.004f, vTop - h * 0.01f), Size(vw + h * 0.008f, h * 0.008f), alpha = 0.8f, style = Stroke(lw))
    // Track: ballast, two rails and sleepers.
    val bed = nt(if (snow) Color(0xFFCBD3E2) else Color(0xFF8E8372), 0.5f)
    drawPath(g.railPath, bed, style = Stroke(h * 0.011f, cap = StrokeCap.Butt))
    drawPath(g.railTies, nt(Color(0xFF5B4632), 0.4f), style = Stroke(h * 0.011f, cap = StrokeCap.Butt))
    drawPath(g.railPath, nt(Color(0xFFC9CFD9), 0.4f), style = Stroke(lw * 1.2f))
    drawTunnelPortal(d, g)
}

/** The tunnel portal in the foot of the right mountain; drawn again over the train so it can drive in. */
internal fun MapPen.drawTunnelPortal(d: DrawScope, g: MapGeo) = with(d) {
    val rail = g.rail
    val stone = nt(if (snow) Color(0xFFDDE4F0) else Color(0xFFB7A98F), 0.5f)
    val end = rail.at(1f)
    val pw = h * 0.05f
    val portal = Path().apply {
        moveTo(end.x - pw * 0.6f, end.y + h * 0.008f)
        lineTo(end.x - pw * 0.6f, end.y - pw * 0.35f)
        quadraticTo(end.x - pw * 0.6f, end.y - pw * 0.95f, end.x, end.y - pw * 0.95f)
        quadraticTo(end.x + pw * 0.6f, end.y - pw * 0.95f, end.x + pw * 0.6f, end.y - pw * 0.35f)
        lineTo(end.x + pw * 0.6f, end.y + h * 0.008f)
        close()
    }
    drawPath(portal, stone)
    val inner = Path().apply {
        moveTo(end.x - pw * 0.4f, end.y + h * 0.008f)
        lineTo(end.x - pw * 0.4f, end.y - pw * 0.3f)
        quadraticTo(end.x - pw * 0.4f, end.y - pw * 0.72f, end.x, end.y - pw * 0.72f)
        quadraticTo(end.x + pw * 0.4f, end.y - pw * 0.72f, end.x + pw * 0.4f, end.y - pw * 0.3f)
        lineTo(end.x + pw * 0.4f, end.y + h * 0.008f)
        close()
    }
    drawPath(inner, Color(0xFF1E1A26))
    drawPath(portal, Ink.line, style = Stroke(lw * 1.4f, join = StrokeJoin.Round))
    drawPath(inner, Ink.line, alpha = 0.6f, style = Stroke(lw))
    drawCircle(nt(Color(0xFFFFD56B), 0.3f), h * 0.004f, Offset(end.x, end.y - pw * 0.66f), alpha = 0.3f + 0.7f * n)
}

/** The steam train running out of the left edge into the tunnel and round again (live). */
internal fun MapPen.drawTrain(d: DrawScope, g: MapGeo) = with(d) {
    val rail = g.rail
    val cycle = LiveKit.TRAIN_CYCLE
    val f = wrap(t / cycle, 1f) * 1.3f - 0.14f
    val carLen = 0.027f * w
    val cars = 4
    for (k in cars - 1 downTo 0) {
        val ff = f - k * (carLen * 1.12f) / rail.len
        if (ff < -0.02f || ff > 0.985f) continue
        val c = rail.at(ff)
        val dr = rail.dir(ff)
        val ang = atan2(dr.y, dr.x) * 57.29578f
        withTransform({ translate(c.x, c.y); rotate(ang, Offset.Zero) }) {
            val ch = h * 0.022f
            val y0 = -ch - h * 0.004f
            if (k == 0) {
                // The little steam engine.
                val body = nt(Color(0xFF2F6E5A), 0.4f)
                drawRoundRect(Ink.line, Offset(-carLen * 0.5f, y0 + ch * 0.3f), Size(carLen * 0.65f, ch * 0.55f), CornerRadius(ch * 0.25f))
                drawRoundRect(body, Offset(-carLen * 0.5f + lw, y0 + ch * 0.3f + lw), Size(carLen * 0.65f - lw * 2f, ch * 0.55f - lw * 2f), CornerRadius(ch * 0.2f))
                drawRect(nt(Color(0xFFC8423A), 0.4f), Offset(carLen * 0.06f, y0 - ch * 0.05f), Size(carLen * 0.36f, ch * 0.9f))
                drawRect(Ink.line, Offset(carLen * 0.06f, y0 - ch * 0.05f), Size(carLen * 0.36f, ch * 0.9f), style = Stroke(lw))
                drawRect(Ink.line, Offset(carLen * 0.4f, y0 - ch * 0.15f), Size(carLen * 0.12f, ch * 0.2f))
                drawRect(nt(Color(0xFF3B3340), 0.4f), Offset(-carLen * 0.36f, y0 - ch * 0.2f), Size(ch * 0.26f, ch * 0.55f))
                drawCircle(nt(Color(0xFFFFE9A0), 0.3f), ch * 0.1f, Offset(carLen * 0.5f, y0 + ch * 0.55f))
            } else {
                val color = if (k % 2 == 1) Color(0xFFFFF0D2) else Color(0xFFE9A0B0)
                drawRoundRect(Ink.line, Offset(-carLen * 0.5f, y0), Size(carLen, ch * 0.88f), CornerRadius(ch * 0.15f))
                drawRoundRect(nt(color, 0.4f), Offset(-carLen * 0.5f + lw, y0 + lw), Size(carLen - lw * 2f, ch * 0.88f - lw * 2f), CornerRadius(ch * 0.12f))
                drawRect(nt(Color(0xFFC8423A), 0.4f), Offset(-carLen * 0.5f + lw, y0 + ch * 0.6f), Size(carLen - lw * 2f, ch * 0.12f))
                for (wi in 0 until 3) {
                    val wx = -carLen * 0.36f + wi * carLen * 0.3f
                    drawRect(lerp(Color(0xFF9FCDE6), Color(0xFFFFD76B), ramp((n - 0.3f) / 0.4f)), Offset(wx, y0 + ch * 0.16f), Size(carLen * 0.16f, ch * 0.3f))
                }
            }
            for (wx in floatArrayOf(-0.36f, -0.12f, 0.12f, 0.36f)) {
                drawCircle(Ink.line, ch * 0.13f, Offset(carLen * wx, -h * 0.003f))
                drawCircle(nt(Color(0xFFB9B2C0), 0.4f), ch * 0.08f, Offset(carLen * wx, -h * 0.003f))
            }
        }
        if (k == 0) {
            // Steam from the funnel.
            for (p in 0 until 4) {
                val ph = wrap(t * 0.8f + p * 0.25f, 1f)
                val sx = c.x - carLen * 0.36f - ph * carLen * 0.7f
                val sy = c.y - h * 0.05f - ph * h * 0.03f
                drawCircle(Color.White, h * (0.006f + 0.008f * ph), Offset(sx, sy), alpha = 0.65f * (1f - ph))
            }
        }
    }
}

/** A stone bridge on the main road and a timber bridge on the street by the water. */
private fun MapPen.drawBridges(d: DrawScope, g: MapGeo) = with(d) {
    // ---- the stone arch bridge
    val c1 = g.river.at(g.riverF1)
    val rw1 = g.riverWidth(g.riverF1)
    val len1 = rw1 + 0.038f * h
    val stone = nt(if (snow) Color(0xFFDDE4F0) else Color(0xFFC9BBA0), 0.5f)
    val stoneSh = nt(if (snow) Color(0xFFB8C4DA) else Color(0xFF9B8D77), 0.5f)
    val top = c1.y - h * 0.012f
    val bodyL = c1.x - len1 / 2f
    val bodyR = c1.x + len1 / 2f
    val deck = Path().apply {
        moveTo(bodyL - h * 0.006f, top)
        quadraticTo(c1.x, top - h * 0.012f, bodyR + h * 0.006f, top)
        lineTo(bodyR + h * 0.006f, top + h * 0.012f)
        quadraticTo(c1.x, top, bodyL - h * 0.006f, top + h * 0.012f)
        close()
    }
    val faceB = c1.y + h * 0.028f
    val face = Path().apply {
        moveTo(bodyL, top + h * 0.012f)
        quadraticTo(c1.x, top, bodyR, top + h * 0.012f)
        lineTo(bodyR, faceB)
        lineTo(bodyL, faceB)
        close()
    }
    drawPath(face, stone)
    drawRect(stoneSh, Offset(bodyR - len1 * 0.16f, top + h * 0.006f), Size(len1 * 0.16f, faceB - top - h * 0.006f))
    val aw = rw1 * 0.95f
    val arch = Path().apply {
        moveTo(c1.x - aw / 2f, faceB)
        lineTo(c1.x - aw / 2f, c1.y + h * 0.008f)
        quadraticTo(c1.x - aw / 2f, c1.y - h * 0.008f, c1.x, c1.y - h * 0.008f)
        quadraticTo(c1.x + aw / 2f, c1.y - h * 0.008f, c1.x + aw / 2f, c1.y + h * 0.008f)
        lineTo(c1.x + aw / 2f, faceB)
        close()
    }
    drawPath(arch, nt(Color(0xFF2C86C8), 0.5f))
    drawPath(Path().apply { addRect(Rect(c1.x - aw / 2f, c1.y - h * 0.008f, c1.x + aw / 2f, c1.y + h * 0.004f)) }, Color(0xFF2B2438), alpha = 0.45f)
    drawPath(arch, Ink.line, style = Stroke(lw * 1.2f, join = StrokeJoin.Round))
    drawPath(face, Ink.line, style = Stroke(lw * 1.3f, join = StrokeJoin.Round))
    drawPath(deck, nt(if (snow) Color(0xFFF7F9FF) else Color(0xFFEBD6A2), 0.5f))
    drawPath(deck, Ink.line, style = Stroke(lw * 1.3f, join = StrokeJoin.Round))
    for (k in 0..5) {
        val px = mix(bodyL, bodyR, k / 5f)
        val py = top - h * 0.0045f - 0.004f * h * sin(k / 5f * 3.1416f)
        drawRect(stone.lighten(0.15f), Offset(px - h * 0.0028f, py - h * 0.012f), Size(h * 0.0056f, h * 0.014f))
        drawRect(Ink.line, Offset(px - h * 0.0028f, py - h * 0.012f), Size(h * 0.0056f, h * 0.014f), alpha = 0.8f, style = Stroke(lw * 0.8f))
    }
    // ---- the timber bridge
    val c2 = g.river.at(g.riverF2)
    val rw2 = g.riverWidth(g.riverF2)
    val len2 = rw2 + 0.04f * h
    val wood = nt(Color(0xFFC98A55), 0.5f)
    val woodD = nt(Color(0xFF8C5A32), 0.5f)
    val l2 = c2.x - len2 / 2f
    val r2 = c2.x + len2 / 2f
    val dy = c2.y - h * 0.004f
    for (k in 0..4) {
        val px = mix(l2 + h * 0.008f, r2 - h * 0.008f, k / 4f)
        drawRect(woodD, Offset(px - h * 0.0025f, dy + h * 0.008f), Size(h * 0.005f, h * 0.03f))
    }
    drawRect(woodD, Offset(l2, dy + h * 0.004f), Size(len2, h * 0.012f))
    drawRect(Ink.line, Offset(l2, dy + h * 0.004f), Size(len2, h * 0.012f), alpha = 0.8f, style = Stroke(lw))
    drawRect(wood, Offset(l2, dy - h * 0.012f), Size(len2, h * 0.018f))
    val planks = ArrayList<Offset>(24)
    var px = l2 + h * 0.008f
    while (px < r2) {
        planks.add(Offset(px, dy - h * 0.012f))
        planks.add(Offset(px, dy + h * 0.006f))
        px += h * 0.009f
    }
    drawPoints(planks, PointMode.Lines, woodD, strokeWidth = lw * 0.8f)
    drawRect(Ink.line, Offset(l2, dy - h * 0.012f), Size(len2, h * 0.018f), style = Stroke(lw * 1.2f))
    for (k in 0..5) {
        val ppx = mix(l2 + h * 0.003f, r2 - h * 0.003f, k / 5f)
        drawLine(Ink.line, Offset(ppx, dy + h * 0.004f), Offset(ppx, dy - h * 0.03f), strokeWidth = lw * 1.8f, cap = StrokeCap.Round)
        drawLine(woodD, Offset(ppx, dy + h * 0.004f), Offset(ppx, dy - h * 0.03f), strokeWidth = lw * 0.9f, cap = StrokeCap.Round)
    }
    drawLine(Ink.line, Offset(l2, dy - h * 0.027f), Offset(r2, dy - h * 0.027f), strokeWidth = lw * 2.4f, cap = StrokeCap.Round)
    drawLine(wood.lighten(0.15f), Offset(l2, dy - h * 0.027f), Offset(r2, dy - h * 0.027f), strokeWidth = lw * 1.2f, cap = StrokeCap.Round)
}

// ------------------------------------------------------------------------------------- life

private class Walker(val road: Int, val f0: Float, val speed: Float, val shirt: Color, val skin: Color, val hair: Color, val dog: Boolean, val hat: Boolean)

private val walkers = listOf(
    Walker(0, 0.12f, 0.0042f, Color(0xFFE94F4F), Color(0xFFF2C29B), Color(0xFF5B3A29), false, false),
    Walker(0, 0.55f, -0.0036f, Color(0xFF3E7BD6), Color(0xFF8D5A3B), Color(0xFF2B2140), true, false),
    Walker(0, 0.8f, 0.0031f, Color(0xFFFFC83D), Color(0xFFE9A77F), Color(0xFFE8B84B), false, true),
    Walker(1, 0.35f, 0.0075f, Color(0xFF6BCB77), Color(0xFFF2C29B), Color(0xFFB5502B), false, false),
    Walker(2, 0.25f, 0.0038f, Color(0xFFB983FF), Color(0xFFF2C29B), Color(0xFF3A2A25), true, false),
    Walker(2, 0.7f, -0.0044f, Color(0xFFFF8A3D), Color(0xFF8D5A3B), Color(0xFF231A1A), false, true),
    Walker(3, 0.45f, 0.0082f, Color(0xFFFF9EC4), Color(0xFFF2C29B), Color(0xFFD9A441), false, false),
    Walker(4, 0.6f, -0.0075f, Color(0xFF5CE0A0), Color(0xFFE9A77F), Color(0xFF5B3A29), false, false),
    Walker(5, 0.3f, 0.006f, Color(0xFFE94F4F), Color(0xFFF2C29B), Color(0xFF2B2140), false, true),
    Walker(6, 0.5f, -0.0062f, Color(0xFF3E7BD6), Color(0xFFF2C29B), Color(0xFFE8B84B), true, false),
)

private class CarSpec(val road: Int, val f0: Float, val speed: Float, val color: Color)

private val cars = listOf(
    CarSpec(0, 0.3f, 0.0135f, Color(0xFFE94F4F)),
    CarSpec(0, 0.8f, -0.012f, Color(0xFF3E7BD6)),
    CarSpec(2, 0.4f, 0.011f, Color(0xFFFFC83D)),
)

/** Where a walker stands now: the point on its road (pingponging), plus the facing and a gait phase. */
private fun MapPen.onRoad(g: MapGeo, road: Int, f0: Float, speed: Float, lat: Float): Triple<Offset, Float, Float> {
    val r = g.roads[road]
    val raw = f0 + speed * t
    val span = 0.9f
    val tri = abs(wrap(raw, 2f * span) - span)
    val f = (0.05f + tri).coerceIn(0.03f, 0.97f)
    val dirSign = if (wrap(raw, 2f * span) < span) 1f else -1f
    val c = r.at(f)
    val dv = r.dir(f)
    val s = if (speed >= 0f) dirSign else -dirSign
    return Triple(Offset(c.x - dv.y * lat, c.y + dv.x * lat), if (dv.x * s >= 0f) 1f else -1f, f)
}

private fun MapPen.drawWalker(d: DrawScope, p: Offset, face: Float, phase: Float, wk: Walker) = with(d) {
    val k = h * 0.03f
    val step = sin(phase)
    val bob = abs(step) * k * 0.04f
    val base = Offset(p.x, p.y - bob)
    // Contact shadow.
    drawOval(Ink.line, Offset(base.x - k * 0.3f, base.y - k * 0.04f), Size(k * 0.6f, k * 0.14f), alpha = 0.18f)
    drawLine(Ink.line, Offset(base.x - k * 0.08f, base.y - k * 0.3f), Offset(base.x - k * 0.08f + step * k * 0.14f, base.y), strokeWidth = k * 0.13f, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset(base.x + k * 0.08f, base.y - k * 0.3f), Offset(base.x + k * 0.08f - step * k * 0.14f, base.y), strokeWidth = k * 0.13f, cap = StrokeCap.Round)
    val torso = Rect(base.x - k * 0.17f, base.y - k * 0.66f, base.x + k * 0.17f, base.y - k * 0.28f)
    drawRoundRect(Ink.line, torso.topLeft - Offset(lw * 0.5f, lw * 0.5f), Size(torso.width + lw, torso.height + lw), CornerRadius(k * 0.12f))
    drawRoundRect(nt(wk.shirt, 0.35f), torso.topLeft, torso.size, CornerRadius(k * 0.1f))
    val hc = Offset(base.x + face * k * 0.03f, base.y - k * 0.82f)
    drawCircle(Ink.line, k * 0.22f + lw * 0.5f, hc)
    drawCircle(nt(wk.skin, 0.3f), k * 0.21f, hc)
    if (wk.hat) {
        drawRect(Ink.line, Offset(hc.x - k * 0.25f, hc.y - k * 0.2f), Size(k * 0.5f, k * 0.09f))
        drawRoundRect(nt(Color(0xFFD2443A), 0.35f), Offset(hc.x - k * 0.17f, hc.y - k * 0.36f), Size(k * 0.34f, k * 0.2f), CornerRadius(k * 0.08f))
    } else {
        drawArc(nt(wk.hair, 0.3f), 180f, 180f, true, Offset(hc.x - k * 0.22f, hc.y - k * 0.22f), Size(k * 0.44f, k * 0.36f))
    }
    val arm = step * k * 0.12f
    drawLine(nt(wk.skin, 0.3f), Offset(base.x, base.y - k * 0.58f), Offset(base.x + arm * face, base.y - k * 0.36f), strokeWidth = k * 0.09f, cap = StrokeCap.Round)
    if (wk.dog) {
        val dp = Offset(base.x - face * k * 0.85f, base.y + k * 0.02f)
        drawOval(Ink.line, Offset(dp.x - k * 0.26f, dp.y - k * 0.3f), Size(k * 0.52f, k * 0.27f))
        drawOval(Color(0xFFE6B36E), Offset(dp.x - k * 0.24f + lw * 0.3f, dp.y - k * 0.28f), Size(k * 0.48f - lw * 0.6f, k * 0.23f))
        drawCircle(Ink.line, k * 0.13f, Offset(dp.x + face * k * 0.26f, dp.y - k * 0.3f))
        drawCircle(Color(0xFFE6B36E), k * 0.1f, Offset(dp.x + face * k * 0.26f, dp.y - k * 0.3f))
        drawLine(Ink.line, Offset(dp.x - face * k * 0.26f, dp.y - k * 0.25f), Offset(dp.x - face * k * 0.38f, dp.y - k * 0.42f + step * k * 0.05f), strokeWidth = k * 0.06f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset(dp.x - k * 0.1f, dp.y - k * 0.06f), Offset(dp.x - k * 0.1f + step * k * 0.1f, dp.y + k * 0.03f), strokeWidth = k * 0.07f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset(dp.x + k * 0.12f, dp.y - k * 0.06f), Offset(dp.x + k * 0.12f - step * k * 0.1f, dp.y + k * 0.03f), strokeWidth = k * 0.07f, cap = StrokeCap.Round)
    }
}

private fun MapPen.drawCar(d: DrawScope, p: Offset, face: Float, color: Color) = with(d) {
    val k = h * 0.034f
    withTransform({ translate(p.x, p.y); scale(face, 1f, Offset.Zero) }) {
        drawOval(Ink.line, Offset(-k * 0.6f, -k * 0.08f), Size(k * 1.2f, k * 0.22f), alpha = 0.2f)
        drawRoundRect(Ink.line, Offset(-k * 0.56f - lw * 0.5f, -k * 0.42f - lw * 0.5f), Size(k * 1.12f + lw, k * 0.44f + lw), CornerRadius(k * 0.14f))
        drawRoundRect(nt(color, 0.35f), Offset(-k * 0.56f, -k * 0.42f), Size(k * 1.12f, k * 0.44f), CornerRadius(k * 0.12f))
        drawRoundRect(nt(color.darken(0.2f), 0.35f), Offset(-k * 0.3f, -k * 0.7f), Size(k * 0.56f, k * 0.34f), CornerRadius(k * 0.12f))
        drawRect(lerp(Color(0xFFB9E4F5), Color(0xFFFFD76B), ramp((n - 0.3f) / 0.4f)), Offset(-k * 0.23f, -k * 0.63f), Size(k * 0.2f, k * 0.2f))
        drawRect(lerp(Color(0xFFB9E4F5), Color(0xFFFFD76B), ramp((n - 0.3f) / 0.4f)), Offset(k * 0.03f, -k * 0.63f), Size(k * 0.18f, k * 0.2f))
        drawCircle(Ink.line, k * 0.13f, Offset(-k * 0.32f, -k * 0.02f))
        drawCircle(Ink.line, k * 0.13f, Offset(k * 0.32f, -k * 0.02f))
        drawCircle(Color(0xFFB9B2C0), k * 0.06f, Offset(-k * 0.32f, -k * 0.02f))
        drawCircle(Color(0xFFB9B2C0), k * 0.06f, Offset(k * 0.32f, -k * 0.02f))
        if (n > 0.2f) {
            drawCircle(Color(0xFFFFE9A0), k * 0.12f, Offset(k * 0.56f, -k * 0.26f), alpha = n)
            drawCircle(Color(0xFFFFE9A0), k * 0.3f, Offset(k * 0.66f, -k * 0.24f), alpha = 0.22f * n)
        }
    }
}

/** A distant hot-air balloon, a boat, birds, petals, sparkles, fireflies, drifting weather and cloud shadows. */
internal fun MapPen.drawLife(d: DrawScope, g: MapGeo, kit: LiveKit) = with(d) {
    // Walkers and cars are drawn in depth order by the depth pass; see drawDepthPass.
    // Sailing boat crossing the upper arm of the fjord.
    val period = 34f
    val ph = wrap(t / period, 2f)
    val pp = if (ph < 1f) ph else 2f - ph
    val bx = mix(0.925f, 1.045f, pp) * w
    val by = mix(0.775f, 0.905f, pp) * h + sin(t * 1.4f) * h * 0.003f
    val dirSign = if (ph < 1f) 1f else -1f
    withTransform({ translate(bx, by); scale(dirSign, 1f, Offset.Zero) }) {
        val k = kit.boatK
        drawOval(Ink.line, Offset(-k * 0.6f, -k * 0.03f), Size(k * 1.2f, k * 0.12f), alpha = 0.22f)
        drawLine(Ink.line, Offset(0f, -k * 0.18f), Offset(0f, -k * 1.02f), strokeWidth = lw * 1.4f)
        drawPath(kit.boatSail, nt(Color.White, 0.4f))
        drawPath(kit.boatSail, Ink.line, style = kit.strokeThin)
        drawPath(kit.boatJib, nt(Color(0xFFFFE08A), 0.4f))
        drawPath(kit.boatJib, Ink.line, style = kit.strokeThin)
        drawPath(kit.boatHull, nt(Color(0xFFE2504A), 0.4f))
        drawPath(kit.boatHull, Ink.line, style = kit.strokeHull)
    }
    // Gulls.
    if (n < 0.75f) {
        val stroke = Stroke(lw * 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (i in 0 until 6) {
            val a = t * (0.2f + 0.05f * i) + i * 2.1f
            val gx = (0.72f + 0.14f * i / 5f + 0.05f * cos(a)) * w
            val gy = (0.8f + 0.03f * sin(a * 1.3f) + 0.02f * i) * h
            gullLive(d, Offset(gx, gy), h * 0.017f, 0.5f + 0.5f * sin(t * 4f + i), stroke, 1f - n / 0.75f)
        }
        for (i in 0 until 5) {
            val sx = wrap(hash01(i, 901) * 1.2f + t * 0.006f * (1f + 0.3f * i), 1.3f) - 0.15f
            val sy = 0.1f + 0.22f * hash01(i, 902) + 0.012f * sin(t * 0.7f + i)
            gullLive(d, Offset(sx * w, sy * h), h * 0.014f, 0.5f + 0.5f * sin(t * 5f + i * 2f), stroke, (1f - n / 0.75f) * 0.85f)
        }
    }
    // A hot-air balloon drifting far away.
    val hx = wrap(0.15f + t * 0.0035f, 1.3f) - 0.15f
    val hy = 0.135f + 0.012f * sin(t * 0.3f)
    val r = h * 0.03f
    translate(hx * w, hy * h) {
        drawLine(Ink.line, Offset(-r * 0.55f, r * 0.6f), Offset(-r * 0.25f, r * 1.3f), strokeWidth = lw * 0.8f)
        drawLine(Ink.line, Offset(r * 0.55f, r * 0.6f), Offset(r * 0.25f, r * 1.3f), strokeWidth = lw * 0.8f)
        drawRoundRect(nt(Color(0xFFC9824A), 0.4f), Offset(-r * 0.25f, r * 1.28f), Size(r * 0.5f, r * 0.34f), CornerRadius(r * 0.06f))
        drawPath(kit.balloonEnv, nt(Color(0xFF5AA9E6), 0.4f))
        drawPath(kit.balloonStripe, nt(Color(0xFFFFC83D), 0.4f))
        drawPath(kit.balloonEnv, Ink.line, style = kit.strokeThin)
    }
    // Petals and leaves on the breeze.
    if (!snow) {
        val petals = ArrayList<Offset>(14)
        val leaves = ArrayList<Offset>(10)
        for (i in 0 until 22) {
            val x = wrap(hash01(i, 911) * 1.2f + t * (0.006f + 0.004f * hash01(i, 912)), 1.2f) - 0.1f
            val y = 0.45f + 0.5f * hash01(i, 913) + 0.02f * sin(t * 0.8f + i * 1.3f)
            (if (i % 3 == 0) leaves else petals).add(Offset(x * w, y * h))
        }
        // Summer has both; autumn is all leaves, spring all petals.
        val autumn = season == app.trollfoss.domain.Season.AUTUMN
        val spring = season == app.trollfoss.domain.Season.SPRING
        val petalC = if (autumn) SeasonPal.litter[1] else Color(0xFFFFC2D8)
        val leafC = if (spring) Color.White else Color(0xFFE8A23A)
        val big = if (autumn || spring) 1.3f else 1f
        drawPoints(petals, PointMode.Points, nt(petalC, 0.4f), strokeWidth = h * 0.006f * big, cap = StrokeCap.Round, alpha = 0.9f * (1f - 0.6f * n))
        drawPoints(leaves, PointMode.Points, nt(leafC, 0.4f), strokeWidth = h * 0.0055f * big, cap = StrokeCap.Round, alpha = 0.85f * (1f - 0.6f * n))
    }
    // Water sparkle.
    val sp = ArrayList<Offset>(24)
    val spAlpha = ArrayList<Float>(24)
    for (i in 0 until 26) {
        val f = hash01(i, 921)
        val on = if (i % 2 == 0) g.river.at(f.coerceIn(0.05f, 0.95f)) else Offset((0.6f + 0.4f * hash01(i, 922)) * w, (0.74f + 0.25f * hash01(i, 923)) * h)
        if (i % 2 == 1 && !g.inSea(on.x, on.y)) continue
        val a = max(0f, sin(t * 1.6f + i * 2.3f))
        if (a > 0.3f) twinkleLive(d, on, h * 0.011f, Color.White, a * (1f - 0.3f * n))
    }
    // Fireflies along the river and at the forest edge on dark evenings.
    val fire = ramp((n - 0.4f) / 0.4f)
    if (fire > 0f) {
        val a = ArrayList<Offset>(10)
        val b = ArrayList<Offset>(10)
        for (i in 0 until 18) {
            val f = 0.1f + 0.8f * hash01(i, 931)
            val c2 = g.river.at(f)
            val x = c2.x + (hash01(i, 932) - 0.5f) * 0.1f * w + sin(t * 0.5f + i) * 0.01f * w
            val y = c2.y + (hash01(i, 933) - 0.5f) * 0.05f * h + sin(t * 0.7f + i * 1.7f) * 0.012f * h
            (if (i % 2 == 0) a else b).add(Offset(x, y))
        }
        for ((pts, phase) in listOf(a to 0f, b to 3.1f)) {
            val pulse = 0.5f + 0.5f * sin(t * 2.1f + phase)
            drawPoints(pts, PointMode.Points, Color(0xFFE9FF7A), strokeWidth = h * 0.03f, cap = StrokeCap.Round, alpha = 0.18f * fire * pulse)
            drawPoints(pts, PointMode.Points, Color(0xFFE9FF7A), strokeWidth = h * 0.008f, cap = StrokeCap.Round, alpha = fire * (0.3f + 0.7f * pulse))
        }
    }
}

/** Soft shadows of clouds gliding over the land, and snow or rain when that is the weather. */
internal fun MapPen.drawWeatherLife(d: DrawScope, g: MapGeo) = with(d) {
    if (oc < 0.5f) {
        for (i in 0 until 3) {
            val span = w * 1.5f
            val x = wrap(hash01(i, 941) * span + t * w * (0.007f + 0.002f * i), span) - w * 0.25f
            val y = (0.5f + 0.2f * i) * h
            drawOval(Ink.line, Offset(x - w * 0.11f, y - h * 0.07f), Size(w * 0.22f, h * 0.14f), alpha = 0.07f * (1f - 0.6f * n))
            drawOval(Ink.line, Offset(x - w * 0.07f, y - h * 0.045f), Size(w * 0.14f, h * 0.09f), alpha = 0.05f * (1f - 0.6f * n))
        }
    }
    if (snowing) {
        val fl = ArrayList<Offset>(60)
        for (i in 0 until 70) {
            val x = wrap(hash01(i, 951) * 1.1f + sin(t * 0.4f + i) * 0.01f, 1.1f) - 0.05f
            val y = wrap(hash01(i, 952) + t * (0.05f + 0.04f * hash01(i, 953)), 1.1f) - 0.05f
            fl.add(Offset(x * w, y * h))
        }
        drawPoints(fl, PointMode.Points, Color.White, strokeWidth = h * 0.007f, cap = StrokeCap.Round, alpha = 0.85f)
    } else if (rain) {
        val ln = ArrayList<Offset>(120)
        for (i in 0 until 80) {
            val x = wrap(hash01(i, 961) * 1.1f + t * 0.03f, 1.1f) - 0.05f
            val y = wrap(hash01(i, 962) + t * (0.9f + 0.4f * hash01(i, 963)), 1.1f) - 0.05f
            ln.add(Offset(x * w, y * h))
            ln.add(Offset(x * w - h * 0.008f, y * h + h * 0.028f))
        }
        drawPoints(ln, PointMode.Lines, Color(0xFFDCEBFF), strokeWidth = lw * 0.9f, cap = StrokeCap.Round, alpha = 0.5f)
    }
}

/** Walkers, cars and dogs in their depth slot; [yMin]..[yMax] are pixel heights. */
internal fun MapPen.drawMovers(d: DrawScope, g: MapGeo, yMin: Float, yMax: Float) {
    for ((i, wk) in walkers.withIndex()) {
        val (p, face, f) = onRoad(g, wk.road, wk.f0, wk.speed, (if (i % 2 == 0) 1f else -1f) * 0.004f * h)
        if (p.y < yMin || p.y >= yMax) continue
        drawWalker(d, p, face, t * 6f + i * 1.7f, wk)
        if (f < 0f) continue
    }
    for ((i, c) in cars.withIndex()) {
        val (p, face, _) = onRoad(g, c.road, c.f0, c.speed, (if (i % 2 == 0) 1f else -1f) * 0.0028f * h)
        if (p.y < yMin || p.y >= yMax) continue
        drawCar(d, p, face, c.color)
    }
}
