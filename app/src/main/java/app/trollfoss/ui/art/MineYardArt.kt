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
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.JobKind
import app.trollfoss.domain.Mine
import app.trollfoss.domain.MineHouse
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The plot and its front yard: sky, hills and forest, a lawn with a stone path, the house the child has built,
 * and, while nothing is built, a building site with a shed, a sign and a heap of planks. While a job runs, the site
 * shows its scaffold, flying tools and the crane.
 */

/** The ground line of the house's front wall. */
internal const val YARD_BASE = 0.82f

private class YardStatic(val blades: List<Offset>, val flowers: List<List<Offset>>, val stones: List<FloatArray>)

private val yardStatic = Memo { u ->
    val blades = ArrayList<Offset>(240)
    val flowerColors = 4
    val flowers = List(flowerColors) { ArrayList<Offset>(24) }
    for (i in 0 until 70) {
        val x = hash01(i, 301) * 9.0f
        val y = 0.79f + hash01(i, 302) * 0.17f
        val hgt = (0.012f + 0.012f * hash01(i, 303)) * u
        val sx = x * u
        val sy = y * u
        blades.add(Offset(sx - hgt * 0.5f, sy)); blades.add(Offset(sx - hgt * 0.7f, sy - hgt))
        blades.add(Offset(sx, sy)); blades.add(Offset(sx, sy - hgt * 1.2f))
        blades.add(Offset(sx + hgt * 0.5f, sy)); blades.add(Offset(sx + hgt * 0.8f, sy - hgt * 0.9f))
    }
    for (i in 0 until 46) {
        val x = hash01(i, 311) * 9.0f
        val y = 0.8f + hash01(i, 312) * 0.16f
        flowers[i % flowerColors].add(Offset(x * u, y * u))
    }
    // Stepping stones from the door toward the front: x, y, rx, rz in scene units.
    val stones = ArrayList<FloatArray>()
    val door = Mine.FACADE_X0 + Mine.FACADE_MW / 2f
    for (n in 0 until 7) {
        val f = n / 6f
        stones.add(floatArrayOf(door + 0.1f * kotlin.math.sin(f * 3.2f) - f * 0.25f, 0.835f + f * 0.12f, 0.07f + 0.03f * f, 0.034f + 0.014f * f))
    }
    YardStatic(blades, flowers, stones)
}

private fun grassOf(season: Season): Color = when (season) {
    Season.SUMMER -> Color(0xFF6FAE5A)
    Season.SPRING -> Color(0xFF86C26A)
    Season.AUTUMN -> Color(0xFF9DAA52)
    Season.WINTER -> Color(0xFFE6EFF8)
}

private fun leafOf(season: Season): Color = when (season) {
    Season.SUMMER -> Color(0xFF9CCB5A)
    Season.SPRING -> Color(0xFFF4B6C8)
    Season.AUTUMN -> Color(0xFFE8A33D)
    Season.WINTER -> Color(0xFFDDE8F4)
}

internal fun DrawScope.mineYardBack(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val h = MineView.house ?: MineHouse()
    val season = pen.season
    drawSky(st, pen, Mood.SUMMER, 0.72f)
    drawStars(st, pen, 0.5f)
    drawMoon(Offset(st.fx(0.7f, 0.04f), 0.12f * u), 0.04f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, pen, 1.0f, -0.02f, 0.4f)
    drawSun(Offset(st.fx(0.22f, 0.04f), 0.15f * u), 0.055f * u, pen, (1f - n) * (1f - overcast(pen)))
    drawRainbow(Offset(st.fx(0.5f, 0.08f), 0.7f * u), 0.5f * u, 0.016f * u, pen.rainbow)
    drawClouds(st, pen, 0.0f, 0.3f, 4, 0.07f, salt = 8)
    drawGulls(st, pen, 2, 0.1f, 0.35f, salt = 3)
    drawPeaks(
        st, 0.08f, 0.6f, 0.7f, 0.14f, 0.27f,
        Color(0xFFA9C2E2).atNight(n, 0.75f), Color(0xFF8FACD3).atNight(n, 0.75f),
        Color(0xFFEEF3FC).atNight(n, 0.7f), Color(0xFFD3DFF3).atNight(n, 0.7f), 0.3f, 23,
    )
    drawForestRow(st, 0.2f, 0.66f, 0.04f, 7, 0.05f, 0.06f, 0.1f, Color(0xFF7FAE9A).atNight(n, 0.75f), null)
    drawForestRow(st, 0.38f, 0.74f, 0.03f, 11, 0.09f, 0.14f, 0.24f, Color(0xFF4B8C6C).atNight(n, 0.65f), Color(0xFF3E7B5C).atNight(n, 0.65f))
    drawPath(ridgePath(st, 0.55f, 0.775f, 0.01f, 19), (if (season == Season.WINTER) Color(0xFFD5E2EF) else Color(0xFF82B862)).atNight(n, 0.55f))

    // Trees on the far edge of the plot.
    val leaf = leafOf(season)
    val treeY = (place_back + 0.008f) * u
    for ((i, x) in listOf(0.25f, 0.6f, 8.35f, 8.8f, 6.2f).withIndex()) {
        if (!st.sees(x - 0.4f, x + 0.4f)) continue
        val sx = st.x(x)
        val hgt = (0.5f + 0.1f * hash01(i, 5)) * u
        if (i % 2 == 0) drawBirch(sx, treeY, hgt, pen, leaf, i, n) else drawPine(sx, treeY, hgt * 0.5f, hgt, Pal.granLight.atNight(n, 0.5f), pen, snow = if (season == Season.WINTER) Pal.snow else null)
    }

    // The lawn.
    val grass = grassOf(season).atNight(n, 0.4f)
    drawRect(
        Brush.verticalGradient(0f to grass.lighten(0.06f), 1f to grass.darken(0.12f), startY = place_back * u, endY = FRONT_Y * u),
        Offset(0f, place_back * u), Size(st.w, (FRONT_Y - place_back) * u),
    )
    val ys = yardStatic.of(u)
    // Mowing stripes.
    var sx0 = floorStripeStart(st)
    while (sx0 < st.cam + st.vw + 1f) {
        val p = Path().apply { floorQuad(u, st.cam, sx0, sx0 + 0.4f, FRONT_Y, place_back) }
        drawPath(p, Color.White, alpha = if (season == Season.WINTER) 0.18f else 0.07f)
        sx0 += 0.8f
    }
    if (season != Season.WINTER) {
        inScene(st) {
            drawPoints(ys.blades, PointMode.Lines, grass.darken(0.3f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
            val colors = if (season == Season.AUTUMN) listOf(Color(0xFFE8A33D), Color(0xFFD2443A), Color(0xFFFFE680), Color(0xFFB9A2F0))
            else listOf(Color(0xFFFFE680), Color(0xFFF08CB8), Color.White, Color(0xFFB9A2F0))
            for ((c, pts) in ys.flowers.withIndex()) drawPoints(pts, PointMode.Points, colors[c], strokeWidth = 0.012f * u, cap = StrokeCap.Round)
        }
    }
    // The path of stones from the door.
    if (h.started || h.job != null) {
        val stoneC = Color(0xFFC3C5CF).atNight(n, 0.4f)
        for (s in ys.stones) {
            if (!st.sees(s[0] - 0.2f, s[0] + 0.2f)) continue
            val p = Path().apply { floorDisc(u, st.cam, s[0], s[1], s[2], s[3], 16) }
            drawPath(p, stoneC)
            drawPath(p, Ink.line, style = pen.thin)
        }
    }

    // The house, or the building site.
    val base = YARD_BASE * u
    val fit = if (h.started) mineFit(h, YARD_BASE) else 1f
    val left = st.x(Mine.FACADE_X0)
    val job = h.job
    val t = pen.t
    val k = u * fit
    val winter = season == Season.WINTER
    val groundPop = if (job != null && job.kind == JobKind.FOUNDATION && job.committed) ((job.t - job.commitAt) / 0.8f).coerceIn(0f, 1f) else 1f
    val upperPop = if (job != null && job.kind == JobKind.UPPER && job.committed) ((job.t - job.commitAt) / 0.8f).coerceIn(0f, 1f) else 1f
    if (h.started && st.sees(Mine.FACADE_X0 - 0.8f, Mine.FACADE_X0 + mineHouseWidth(h) + 0.5f)) {
        drawMineHouse(h, left, base, k, pen, n, t, winter, 2, groundPop, upperPop, clipL = -0.4f * u, clipR = st.w + 0.8f * u)
    }
    if (!h.started || (job != null && job.kind == JobKind.FOUNDATION)) drawSite(st, pen, h, n, t)
    if (job != null && job.kind == JobKind.UPPER && h.started) drawUpperJob(st, pen, h, job.t, job.committed, job.commitAt, left, base, k)
    drawBase(st, pen, Color(0xFF8A6B45), Color(0xFF5E4630))
    drawBaseStones(st, pen, Color(0xFF9A8670), 29)
}

private const val place_back = 0.78f

private fun floorStripeStart(st: Stage): Float {
    val a = st.cam - 1f
    return kotlin.math.floor(a / 0.8f) * 0.8f
}

/** The empty plot: dashed outline of the hall, corner posts with string, a shed, a sign, planks and a hammer. */
private fun DrawScope.drawSite(st: Stage, pen: Pen, h: MineHouse, night: Float, t: Float) {
    val u = st.u
    val x0 = Mine.FACADE_X0
    val x1 = x0 + Mine.FACADE_MW
    val job = h.job
    val working = job != null && job.kind == JobKind.FOUNDATION
    val p = if (working) (job!!.t / job.commitAt).coerceIn(0f, 1f) else 0f
    // The plot: a dashed outline on the grass.
    if (st.sees(x0 - 0.3f, x1 + 0.6f)) {
        val plot = Path().apply { floorQuad(u, st.cam, x0, x1, 0.9f, 0.8f) }
        drawPath(plot, Color(0xFF8A6B45), alpha = 0.35f + 0.25f * p)
        drawPath(plot, Color.White, style = Stroke(width = pen.lw * 2f, cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(pen.lw * 6f, pen.lw * 5f), 0f)))
        // Four posts with a string.
        val corners = listOf(Offset(x0, 0.9f), Offset(x1, 0.9f), Offset(x1 + recede(0.8f) - recede(0.9f), 0.8f), Offset(x0 + recede(0.8f) - recede(0.9f), 0.8f))
        for (c in corners) {
            val px = st.x(c.x)
            val py = c.y * u
            drawLine(Ink.line, Offset(px, py), Offset(px, py - 0.06f * u), strokeWidth = pen.lw * 3f, cap = StrokeCap.Round)
            drawLine(Color(0xFFE3B27A), Offset(px, py), Offset(px, py - 0.06f * u), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFFFC83D), pen.lw * 2f, Offset(px, py - 0.062f * u))
        }
        for (i in 0 until 4) {
            val a = corners[i]
            val b = corners[(i + 1) % 4]
            drawLine(Color(0xFFFFE680), Offset(st.x(a.x), a.y * u - 0.05f * u), Offset(st.x(b.x), b.y * u - 0.05f * u), strokeWidth = pen.lw * 0.9f)
        }
    }
    // A scaffold rising while the work goes on, and the tools that work by themselves.
    if (working) {
        val base = 0.86f * u
        val a = st.x(x0 + 0.05f)
        val b = st.x(x1 - 0.05f)
        drawScaffold(a, b, base, 0.34f * u, pen, rise = ramp(p * 1.4f))
        // A slab of foundation.
        val slab = Path().apply { floorQuad(u, st.cam, x0, x0 + (x1 - x0) * ramp(p * 1.6f), 0.9f, 0.8f) }
        drawPath(slab, Color(0xFFB9BDC9))
        drawPath(slab, Ink.line, style = pen.thin)
        val c = Offset(st.x((x0 + x1) / 2f), 0.5f * u)
        drawBuildTools(c.x, c.y, 0.5f * u, t, pen, 1f - (if (job!!.committed) ((job.t - job.commitAt) / 1.2f).coerceIn(0f, 1f) else 0f))
    }
    // The shed, the sign, the planks.
    val sx = Mine.SHED_X
    if (st.sees(sx - 0.8f, sx + 0.8f)) {
        drawShed(st.x(sx), 0.84f * u, u, pen, night, t, !working)
        drawPlankPile(st.x(sx - 1.05f), 0.88f * u, u, pen, if (working) 2 else 5)
        drawHammer(st.x(sx - 0.62f), 0.78f * u, 0.17f * u, -22f, pen)
    }
    val gx = x1 + 0.35f
    if (st.sees(gx - 0.3f, gx + 0.3f)) drawSign(st.x(gx), 0.88f * u, u, pen, t)
}

/** A little wooden shed with its door ajar and tools in the doorway. */
private fun DrawScope.drawShed(cx: Float, base: Float, k: Float, pen: Pen, night: Float, t: Float, invite: Boolean) {
    val w = 0.78f * k
    val h = 0.38f * k
    val l = cx - w / 2f
    val wood = Color(0xFFB98650).atNight(night, 0.45f)
    val dxs = 0.14f * k
    val dys = -0.1f * k
    // Side, front, roof.
    quadFill(wood.darken(0.25f), pen, l + w, base, l + w, base - h, l + w + dxs, base - h + dys, l + w + dxs, base + dys)
    drawRect(wood, Offset(l, base - h), Size(w, h))
    var x = l + 0.06f * k
    val lines = ArrayList<Offset>()
    while (x < l + w) { lines.add(Offset(x, base - h)); lines.add(Offset(x, base)); x += 0.065f * k }
    drawPoints(lines, PointMode.Lines, wood.darken(0.25f), strokeWidth = pen.lw * 0.6f)
    drawRect(Ink.line, Offset(l, base - h), Size(w, h), style = pen.stroke)
    val roof = Color(0xFF8E4A3A).atNight(night, 0.45f)
    quadFill(roof, pen, l - 0.05f * k, base - h + 0.01f * k, l + w + 0.05f * k, base - h + 0.01f * k, l + w + 0.05f * k + dxs, base - h + dys - 0.045f * k, l - 0.05f * k + dxs, base - h + dys - 0.045f * k)
    // The door, half open: warm inside, with tools.
    val dl = l + w * 0.3f
    val dr = l + w * 0.7f
    val dt = base - h * 0.82f
    drawRect(Color(0xFF4A3A33), Offset(dl, dt), Size(dr - dl, base - dt))
    drawHammer(dl + (dr - dl) * 0.35f, dt + 0.06f * k, 0.12f * k, 0f, pen)
    drawSaw(dl + (dr - dl) * 0.52f, dt + 0.05f * k, 0.12f * k, 90f, pen)
    quadFill(wood.lighten(0.08f), pen, dl, dt, dl - (dr - dl) * 0.45f, dt - 0.01f * k, dl - (dr - dl) * 0.45f, base - 0.01f * k, dl, base)
    drawRect(Ink.line, Offset(dl, dt), Size(dr - dl, base - dt), style = pen.thin)
    if (invite) {
        // A twinkle that says: here is where it starts.
        val p = (t * 0.7f) % 1f
        twinkle(Offset(cx + sin(t * 2f) * 0.1f * k, base - h - 0.08f * k - p * 0.05f * k), 0.035f * k, Color(0xFFFFD447), 1f - p)
    }
}

/** A sign on a post: a house and a hammer, no words. */
private fun DrawScope.drawSign(cx: Float, base: Float, k: Float, pen: Pen, t: Float) {
    drawLine(Ink.line, Offset(cx, base), Offset(cx, base - 0.3f * k), strokeWidth = pen.lw * 3.4f, cap = StrokeCap.Round)
    drawLine(Color(0xFFC98A55), Offset(cx, base), Offset(cx, base - 0.3f * k), strokeWidth = pen.lw * 2f, cap = StrokeCap.Round)
    val w = 0.2f * k
    val top = base - 0.38f * k
    val sway = sin(t * 1.6f) * 1.2f
    roundFill(Color(0xFFFFF7EA), pen, cx - w / 2f + sway, top, cx + w / 2f + sway, top + 0.16f * k, 0.02f * k)
    // The picture: a little house.
    val hc = Offset(cx - 0.03f * k + sway, top + 0.08f * k)
    triFill(Color(0xFFD2443A), pen, hc.x - 0.05f * k, hc.y, hc.x + 0.05f * k, hc.y, hc.x, hc.y - 0.05f * k, thin = true)
    drawRect(Color(0xFFFFC83D), Offset(hc.x - 0.04f * k, hc.y), Size(0.08f * k, 0.05f * k))
    drawRect(Ink.line, Offset(hc.x - 0.04f * k, hc.y), Size(0.08f * k, 0.05f * k), style = pen.thin)
    drawHammer(cx + 0.05f * k + sway, top + 0.06f * k, 0.08f * k, 30f, pen)
}

/** The crane that lifts the second floor onto the hall. */
private fun DrawScope.drawUpperJob(st: Stage, pen: Pen, h: MineHouse, jt: Float, committed: Boolean, commitAt: Float, left: Float, base: Float, k: Float) {
    val s = jt / commitAt
    val mw = Mine.FACADE_MW * k
    val towerX = left - 0.55f * k
    val groundTop = base - 0.4f * k
    val hall = left + mw / 2f
    val towerH = 1.0f * k
    val lift = ramp(((s - 0.15f) / 0.75f).coerceIn(0f, 1f))
    val hy = if (committed) base - towerH * 0.9f else (base - towerH * 0.95f) + (groundTop - 0.30f * k - (base - towerH * 0.95f)) * lift
    val hx = hall + sin(jt * 2.1f) * 0.012f * k * (1f - lift)
    if (!committed) {
        // The floor module hangs from two cables.
        val mh = 0.3f * k
        val x0 = hx - mw / 2f
        drawLine(Ink.line, Offset(x0 + mw * 0.1f, hy), Offset(hx, hy - 0.08f * k), strokeWidth = pen.lw * 1.2f)
        drawLine(Ink.line, Offset(x0 + mw * 0.9f, hy), Offset(hx, hy - 0.08f * k), strokeWidth = pen.lw * 1.2f)
        val wall = MineC.wall(h.wall)
        drawRect(wall, Offset(x0, hy), Size(mw, mh))
        drawRect(Ink.line, Offset(x0, hy), Size(mw, mh), style = pen.stroke)
        drawMineWindow(h.windows, null, x0 + mw * 0.33f, hy + mh * 0.2f, x0 + mw * 0.67f, hy + mh * 0.66f, pen, 0f, MineC.trim(wall), MineC.roof(h.roofColor), jt, 1)
    }
    drawCrane(towerX, base, towerH * 0.98f, mw * 0.7f + 0.25f * k, hx, if (committed) hy else hy - 0.08f * k, pen, k)
}
