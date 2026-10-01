package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.GardenLayout
import app.trollfoss.domain.GardenView
import app.trollfoss.domain.Weather
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Hagen, the garden of Storhuset, as a place background: a bright sky with a sun that has rays, far blue
 * mountains, forested hills with parallax, a row of trees peeking over a hedge and a picket fence, the back of
 * the big house, the lawn with mowing stripes, a stone path, flowers along the house, the pond and the slide;
 * and in front of everything the tall grass and flowers, butterflies by day and fireflies by night.
 *
 * Everything big is built once per scale (see [gsGeo]) and drawn with a translate; a frame only strokes and
 * fills ready-made paths. The place works in sun, rain, snow and night; it reads nothing but [Pen].
 * Helpers start with `gs`.
 */

private const val GS_CHUNK = 1f
private const val GS_CHUNKS = 10
private const val GS_BACK = 0.78f

private fun gsChunk(x: Float): Int = (x / GS_CHUNK).toInt().coerceIn(0, GS_CHUNKS - 1)

private val GS_FLOWERS = arrayOf(Color(0xFFFFFFFF), Color(0xFFFFD84A), Color(0xFFFF8FB1), Color(0xFF7FB2FF), Color(0xFFE8473F), Color(0xFFB98CFF))

private class GsGeo(
    val hedge: Array<Path>, val hedgeLight: Array<Path>, val hedgeDots: Array<Array<ArrayList<Offset>>>,
    val pickets: Array<Path>, val rails: Array<Path>,
    val stones: Path, val stonesShade: Path,
    val tufts: Array<Path>, val border: Array<Array<ArrayList<Offset>>>, val bushes: Path, val bushesLight: Path, val patches: Path,
    val frontTufts: Array<Array<Path>>, val frontStems: Array<List<Offset>>, val frontHeads: Array<Array<List<Offset>>>,
)

private val gsGeo = Memo { u -> gsBuild(u) }

private fun gsPathY(x: Float): Float = 0.905f + 0.02f * sin(x * 2.3f)

private fun gsBuild(u: Float): GsGeo {
    val fenceFrom = 3.28f
    val hedge = Array(GS_CHUNKS) { Path() }
    val hedgeLight = Array(GS_CHUNKS) { Path() }
    val pickets = Array(GS_CHUNKS) { Path() }
    val rails = Array(GS_CHUNKS) { Path() }
    val dots = Array(GS_CHUNKS) { Array(3) { ArrayList<Offset>() } }
    for (c in 0 until GS_CHUNKS) {
        val a = max(c.toFloat(), fenceFrom)
        val b = (c + 1).toFloat()
        if (b <= a) continue
        hedge[c].addRect(Rect(a * u, 0.69f * u, b * u, 0.78f * u))
        var x = a
        var i = c * 40
        while (x < b) {
            val r = 0.045f + 0.02f * hash01(i, 901)
            val cy = 0.675f + 0.016f * sin(x * 9.1f) + 0.01f * (hash01(i, 902) - 0.5f)
            hedge[c].addOval(Rect((x - r) * u, (cy - r * 0.9f) * u, (x + r) * u, (cy + r * 0.9f) * u))
            hedgeLight[c].addOval(Rect((x - r * 0.75f) * u, (cy - r * 0.85f) * u, (x + r * 0.2f) * u, (cy + r * 0.1f) * u))
            if (hash01(i, 903) > 0.55f) dots[c][(hash01(i, 904) * 3f).toInt().coerceAtMost(2)].add(Offset((x + (hash01(i, 905) - 0.5f) * 0.05f) * u, (cy + 0.02f * (hash01(i, 906) - 0.3f)) * u))
            x += 0.066f
            i++
        }
        // Pickets: pointed boards with a gap, and two rails behind them.
        rails[c].addRect(Rect(a * u, 0.727f * u, b * u, 0.739f * u))
        rails[c].addRect(Rect(a * u, 0.757f * u, b * u, 0.769f * u))
        var px = floor(a / 0.052f) * 0.052f + 0.026f
        while (px < b) {
            if (px >= a) {
                pickets[c].apply {
                    moveTo((px - 0.017f) * u, 0.775f * u)
                    lineTo((px - 0.017f) * u, 0.712f * u)
                    lineTo(px * u, 0.697f * u)
                    lineTo((px + 0.017f) * u, 0.712f * u)
                    lineTo((px + 0.017f) * u, 0.775f * u)
                    close()
                }
            }
            px += 0.052f
        }
    }

    // Stepping stones: from the door to the pond's bank, and from the pond to the play corner.
    val stones = Path()
    val shade = Path()
    var sx = 1.5f
    var si = 0
    while (sx < 4.12f) {
        val cy = gsPathY(sx) + 0.01f * (hash01(si, 911) - 0.5f)
        val rx = 0.052f + 0.014f * hash01(si, 912)
        stones.floorDisc(u, 0f, sx, cy, rx, 0.04f, 14)
        shade.floorDisc(u, 0f, sx + 0.008f, cy + 0.006f, rx, 0.036f, 14)
        sx += 0.17f
        si++
    }
    sx = 6.42f
    while (sx < 7.9f) {
        val cy = 0.935f + 0.014f * sin(sx * 3f) + 0.008f * (hash01(si, 913) - 0.5f)
        val rx = 0.05f + 0.014f * hash01(si, 914)
        stones.floorDisc(u, 0f, sx, cy, rx, 0.038f, 14)
        shade.floorDisc(u, 0f, sx + 0.008f, cy + 0.006f, rx, 0.034f, 14)
        sx += 0.18f
        si++
    }

    // Tufts, patches and the flower border along the house.
    val tufts = Array(GS_CHUNKS) { Path() }
    val patches = Path()
    for (c in 0 until GS_CHUNKS) {
        for (k in 0 until 7) {
            val x = c + (k + hash01(c * 9 + k, 921)) / 7f
            val y = 0.8f + 0.15f * hash01(c * 9 + k, 922)
            tufts[c].addPath(tuftPath(x * u, y * u, (0.022f + 0.016f * hash01(c * 9 + k, 923)) * u, 0f))
        }
    }
    for (k in 0 until 16) {
        patches.floorDisc(u, 0f, 0.2f + 8.6f * hash01(k, 931), 0.82f + 0.13f * hash01(k, 932), 0.14f + 0.12f * hash01(k, 933), 0.05f, 16)
    }
    val border = Array(GS_CHUNKS) { Array(6) { ArrayList<Offset>() } }
    for (k in 0 until 150) {
        val x = -0.4f + 3.75f * hash01(k, 941)
        if (x < 0f) continue
        val c = gsChunk(x)
        val col = (hash01(k, 942) * 6f).toInt().coerceAtMost(5)
        border[c][col].add(Offset(x * u, (0.752f + 0.03f * hash01(k, 943)) * u))
    }
    val bushes = Path()
    val bushesLight = Path()
    for ((k, bx) in floatArrayOf(0.02f, 1.07f, 3.18f).withIndex()) {
        val by = 0.745f + 0.008f * k
        val r = 0.055f + 0.01f * k
        bushes.addOval(Rect((bx - r * 1.4f) * u, (by - r) * u, (bx + r * 1.4f) * u, (by + r * 0.7f) * u))
        bushesLight.addOval(Rect((bx - r * 1.1f) * u, (by - r * 0.9f) * u, (bx + r * 0.3f) * u, (by + r * 0.1f) * u))
    }

    // The tall grass and flowers in the very front.
    val frontTufts = Array(GS_CHUNKS) { c ->
        Array(3) { g ->
            Path().apply {
                for (k in 0 until 4) {
                    val x = c + (k + hash01(c * 31 + g * 7 + k, 951)) / 4f
                    val h = (0.04f + 0.035f * hash01(c * 31 + g * 7 + k, 952)) * u
                    addPath(tuftPath(x * u, (0.986f + 0.006f * g) * u, h, 0f))
                }
            }
        }
    }
    val stems = Array<List<Offset>>(GS_CHUNKS) { c ->
        val l = ArrayList<Offset>()
        for (k in 0 until 6) {
            val x = c + (k + hash01(c * 17 + k, 961)) / 6f
            val h = 0.07f + 0.04f * hash01(c * 17 + k, 962)
            l.add(Offset(x * u, 0.99f * u))
            l.add(Offset((x + 0.01f * (hash01(c * 17 + k, 963) - 0.5f)) * u, (0.99f - h) * u))
        }
        l
    }
    val heads = Array(GS_CHUNKS) { c ->
        Array<List<Offset>>(6) { col ->
            val l = ArrayList<Offset>()
            for (k in 0 until 6) {
                if ((c * 17 + k) % 6 != col) continue
                val x = c + (k + hash01(c * 17 + k, 961)) / 6f
                val h = 0.07f + 0.04f * hash01(c * 17 + k, 962)
                l.add(Offset((x + 0.01f * (hash01(c * 17 + k, 963) - 0.5f)) * u, (0.99f - h) * u))
            }
            l
        }
    }
    return GsGeo(hedge, hedgeLight, dots, pickets, rails, stones, shade, tufts, border, bushes, bushesLight, patches, frontTufts, stems, heads)
}

/** Colours of the lawn: bright and warm in sun, grey-green in rain, white in snow. */
private fun gsGrass(pen: Pen, base: Color): Color = base.gaSnow(pen, 0.62f).ga(pen, 0.5f, 0.35f)

internal fun DrawScope.gardenBack(st: Stage, pen: Pen) {
    // The gnomes move where nobody looks: they ask where the camera is.
    GardenView.set(st.cam, st.cam + st.vw)
    gsSky(st, pen)
    gsFar(st, pen)
    gsTrees(st, pen)
    gsBackWall(st, pen)
    gardenHouse(st, pen)
    gsLawn(st, pen)
    gardenPondBack(st, pen)
    gardenSlide(st, pen)
}

internal fun DrawScope.gardenFront(st: Stage, pen: Pen) {
    gardenPondFront(st, pen)
    gsFront(st, pen)
}

// ---------------------------------------------------------------------------------------------------------- sky

private fun DrawScope.gsSky(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    drawSky(st, pen, Mood.SUMMER, 0.72f)
    drawStars(st, pen, 0.5f)
    drawMoon(Offset(st.fx(0.82f, 0.03f), 0.1f * u), 0.04f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, pen, 0.55f, -0.12f, 0.42f)
    val vis = (1f - n) * (1f - overcast(pen))
    drawSun(Offset(st.fx(0.3f, 0.03f), 0.1f * u), 0.055f * u, pen, vis)
    drawRainbow(Offset(st.fx(0.62f, 0.08f), 0.72f * u), 0.5f * u, 0.016f * u, pen.rainbow)
    drawClouds(st, pen, -0.02f, 0.3f, 3, 0.07f, salt = 9)
    drawGulls(st, pen, 2, 0.04f, 0.28f, salt = 3)
}

private fun DrawScope.gsFar(st: Stage, pen: Pen) {
    val n = pen.night
    val snowy = pen.weather == Weather.SNOW
    // Blue mountains with snow caps, then pale hills and a ridge of forest.
    drawPeaks(
        st, 0.05f, 0.6f, 0.95f, 0.1f, 0.26f,
        Color(0xFFB4C7E6).ga(pen, 0.75f, 0.5f), Color(0xFF9BB3DA).ga(pen, 0.75f, 0.5f),
        Color(0xFFF2F6FF).ga(pen, 0.7f, 0.5f), Color(0xFFD5E0F5).ga(pen, 0.7f, 0.5f), 0.34f, 21,
    )
    drawPath(ridgePath(st, 0.12f, 0.66f, 0.035f, 5), lerp(Color(0xFFA6CFB4), Color.White, if (snowy) 0.45f else 0f).ga(pen, 0.7f, 0.5f))
    drawForestRow(st, 0.22f, 0.7f, 0.03f, 9, 0.075f, 0.08f, 0.15f, Color(0xFF5F9C80).ga(pen, 0.7f, 0.45f), Color(0xFF518A6F).ga(pen, 0.7f, 0.45f))
    drawPath(ridgePath(st, 0.4f, 0.755f, 0.016f, 17), lerp(Color(0xFF88C468), Color(0xFFEAF2F8), if (snowy) 0.6f else 0f).ga(pen, 0.6f, 0.4f))
}

private fun DrawScope.gsTrees(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val p = 0.9f
    val spacing = 0.78f
    val snowy = pen.weather == Weather.SNOW
    val l0 = st.cam * p - 0.5f
    val l1 = st.cam * p + st.vw + 0.5f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        if (hash01(i, 971) < 0.18f) continue
        val sx = st.px(i * spacing + (hash01(i, 972) - 0.5f) * 0.4f, p)
        val h = (0.38f + 0.2f * hash01(i, 973)) * u
        val kind = hash01(i, 974)
        val baseY = 0.772f * u
        val leaf = lerp(Color(0xFF8DC85A), Color(0xFFE8F0F4), if (snowy) 0.55f else 0f)
        when {
            kind < 0.38f -> drawBirch(sx, baseY, h, pen, leaf, i, n)
            kind < 0.7f -> drawPine(sx, baseY, h * 0.5f, h, Pal.granLight.atNight(n, 0.5f), pen, if (snowy) Pal.snow else null)
            else -> {
                // A round apple tree: a thick trunk and a bumpy crown, with red apples in summer.
                val tw = h * 0.07f
                val trunk = Path().apply { poly(sx - tw * 0.5f, baseY - h * 0.45f, sx + tw * 0.5f, baseY - h * 0.45f, sx + tw * 0.75f, baseY, sx - tw * 0.75f, baseY) }
                drawPath(trunk, Color(0xFF8A5E3C).atNight(n, 0.5f))
                drawPath(trunk, Ink.line, style = pen.stroke)
                val crown = crownPath(sx, baseY - h * 0.62f, h * 0.3f, h * 0.27f, i)
                inked(crown, leaf.ga(pen, 0.5f), pen)
                if (!snowy) for (k in 0 until 4) drawCircle(Color(0xFFE8473F).ga(pen, 0.4f), h * 0.018f, Offset(sx + (k - 1.5f) * h * 0.12f, baseY - h * (0.58f + 0.05f * (k % 2))))
            }
        }
    }
}

private fun DrawScope.gsBackWall(st: Stage, pen: Pen) {
    val u = st.u
    val g = gsGeo.of(u)
    val first = max(0, floor(st.cam / GS_CHUNK).toInt() - 1)
    val last = min(GS_CHUNKS - 1, floor((st.cam + st.vw) / GS_CHUNK).toInt() + 1)
    if (last < 3) return
    val snowy = pen.weather == Weather.SNOW
    val hedgeC = Color(0xFF3F8F4A).ga(pen, 0.55f, 0.35f).gaSnow(pen, 0.45f)
    val hedgeL = Color(0xFF68B85A).ga(pen, 0.55f, 0.35f).gaSnow(pen, 0.5f)
    val fence = Color(0xFFF6F0E0).ga(pen, 0.5f, 0.3f)
    val fenceShade = Color(0xFFD9CFB8).ga(pen, 0.5f, 0.3f)
    inScene(st) {
        for (c in first..last) {
            drawPath(g.hedge[c], hedgeC)
            drawPath(g.hedgeLight[c], hedgeL)
            drawPath(g.hedge[c], Ink.line, alpha = 0.5f, style = pen.thin)
            if (!snowy) for (d in 0 until 3) {
                val pts = g.hedgeDots[c][d]
                if (pts.isEmpty()) continue
                val col = arrayOf(Color(0xFFFFFFFF), Color(0xFFFF8FB1), Color(0xFFFFD84A))[d].ga(pen, 0.4f)
                drawPoints(pts, PointMode.Points, col, strokeWidth = 0.011f * u, cap = StrokeCap.Round)
            }
            drawPath(g.rails[c], fenceShade)
            drawPath(g.rails[c], Ink.line, alpha = 0.7f, style = pen.thin)
            drawPath(g.pickets[c], fence)
            drawPath(g.pickets[c], Ink.line, style = pen.thin)
        }
    }
}

// -------------------------------------------------------------------------------------------------------- lawn

private fun DrawScope.gsLawn(st: Stage, pen: Pen) {
    val u = st.u
    val g = gsGeo.of(u)
    val n = pen.night
    val snowy = pen.weather == Weather.SNOW
    val first = max(0, floor(st.cam / GS_CHUNK).toInt() - 1)
    val last = min(GS_CHUNKS - 1, floor((st.cam + st.vw) / GS_CHUNK).toInt() + 1)
    val grassBack = gsGrass(pen, Color(0xFF82C95F))
    val grassFront = gsGrass(pen, Color(0xFF5FAE48))
    drawRect(
        Brush.verticalGradient(0f to grassBack, 1f to grassFront, startY = GS_BACK * u, endY = FRONT_Y * u),
        Offset(0f, GS_BACK * u), Size(st.w, (FRONT_Y - GS_BACK) * u + 1f),
    )
    // Mowing stripes, slanting with the depth like everything on the ground.
    if (!snowy) {
        val stripe = Path()
        val w = 0.6f
        var x = floor((st.cam - 1f) / (2f * w)) * 2f * w
        while (x < st.cam + st.vw + 1f) {
            stripe.floorQuad(u, st.cam, x, x + w, FRONT_Y, GS_BACK)
            x += 2f * w
        }
        drawPath(stripe, Color.White, alpha = 0.075f * (1f - n * 0.6f))
    }
    inScene(st) {
        drawPath(g.patches, gsGrass(pen, Color(0xFF5E9E45)), alpha = if (snowy) 0.0f else 0.4f)
        if (snowy) drawPath(g.patches, Color.White, alpha = 0.5f)
        for (c in first..last) {
            drawPath(g.tufts[c], gsGrass(pen, Color(0xFF4F9440)))
        }
        // The flower border along the house, and the bushes at the corners.
        for (c in first..min(last, 3)) {
            for (k in 0 until 6) {
                val pts = g.border[c][k]
                if (pts.isEmpty() || snowy) continue
                drawPoints(pts, PointMode.Points, Ink.line, strokeWidth = 0.0155f * u, cap = StrokeCap.Round, alpha = 0.7f)
                drawPoints(pts, PointMode.Points, GS_FLOWERS[k].ga(pen, 0.4f, 0.2f), strokeWidth = 0.011f * u, cap = StrokeCap.Round)
            }
        }
        val bush = Color(0xFF4C9A4F).ga(pen, 0.55f, 0.3f).gaSnow(pen, 0.5f)
        drawPath(g.bushes, bush)
        drawPath(g.bushesLight, Color(0xFF73C063).ga(pen, 0.55f, 0.3f).gaSnow(pen, 0.5f))
        drawPath(g.bushes, Ink.line, style = pen.thin)
        // The stepping stones.
        val stone = Color(0xFFCFC8BC).ga(pen, 0.5f).gaSnow(pen, 0.55f)
        drawPath(g.stonesShade, stone.darken(0.25f), alpha = 0.9f)
        drawPath(g.stones, stone)
        drawPath(g.stones, Ink.line, alpha = 0.75f, style = pen.thin)
    }
    drawBase(st, pen, gsGrass(pen, Color(0xFF6FAE5A)), Color(0xFF6B4A34).ga(pen, 0.5f))
    drawBaseStones(st, pen, Color(0xFF9A8F86).ga(pen, 0.5f), 31)
}

// --------------------------------------------------------------------------------------------------------- front

private fun DrawScope.gsFront(st: Stage, pen: Pen) {
    val u = st.u
    val g = gsGeo.of(u)
    val t = pen.t
    val n = pen.night
    val snowy = pen.weather == Weather.SNOW
    val first = max(0, floor(st.cam / GS_CHUNK).toInt() - 1)
    val last = min(GS_CHUNKS - 1, floor((st.cam + st.vw) / GS_CHUNK).toInt() + 1)
    val grassA = gsGrass(pen, Color(0xFF6FAE5A))
    inScene(st) {
        for (c in first..last) {
            for (gi in 0 until 3) {
                val sway = sin(t * (1.1f + 0.2f * gi) + gi * 2f + c) * 0.0028f * u
                translate(sway, 0f) {
                    val col = if (gi == 1) grassA.lighten(0.12f) else grassA.darken(0.06f * gi)
                    drawPath(g.frontTufts[c][gi], col)
                    drawPath(g.frontTufts[c][gi], Ink.line, style = pen.thin, alpha = 0.7f)
                }
            }
            if (snowy) continue
            val stems = g.frontStems[c]
            if (stems.isNotEmpty()) drawPoints(stems, PointMode.Lines, Color(0xFF4F9A48).ga(pen, 0.5f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            for (k in 0 until 6) {
                val pts = g.frontHeads[c][k]
                if (pts.isEmpty()) continue
                drawPoints(pts, PointMode.Points, Ink.line, strokeWidth = 0.0175f * u, cap = StrokeCap.Round)
                drawPoints(pts, PointMode.Points, GS_FLOWERS[k].ga(pen, 0.4f, 0.2f), strokeWidth = 0.0135f * u, cap = StrokeCap.Round)
                drawPoints(pts, PointMode.Points, if (k == 1) Color(0xFFFF9A3D) else Color(0xFFFFE680), strokeWidth = 0.005f * u, cap = StrokeCap.Round)
            }
        }
    }
    // Butterflies and dragonflies by day; pollen drifting; none in rain or snow.
    val calm = 1f - overcast(pen)
    val day = (1f - n) * calm
    if (day > 0.05f) {
        val cols = intArrayOf(0xFFFFC83D.toInt(), 0xFF4F8BFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF8FB1.toInt())
        for (k in 0 until 4) {
            val bx = st.px(1.0f + k * 2.2f + sin(t * 0.21f + k * 1.7f) * 0.9f, 1.0f)
            val by = (0.83f + 0.04f * sin(t * 0.8f + k * 2f) + 0.012f * sin(t * 2.3f + k)) * u
            if (bx < -0.05f * u || bx > st.w + 0.05f * u) continue
            gsButterfly(bx, by, 0.011f * u, abs(cos(t * 13f + k * 2f)), Color(cols[k]).copy(alpha = day), pen)
        }
        for (k in 0 until 2) {
            val dx = st.px(GardenLayout.POND_X1 + 0.3f + k * 0.9f + sin(t * 0.4f + k * 2f) * 0.35f, 1.0f)
            val dy = (0.76f + 0.025f * sin(t * 0.9f + k * 1.9f)) * u
            if (dx < -0.05f * u || dx > st.w + 0.05f * u) continue
            gsDragonfly(dx, dy, 0.014f * u, t * 30f + k, pen, day)
        }
        val specks = ArrayList<Offset>(10)
        for (k in 0 until 10) {
            val px = wrap(hash01(k, 981) * (st.w + 0.4f * u) + t * 0.012f * u * (1f + hash01(k, 982)), st.w + 0.4f * u) - 0.2f * u
            specks.add(Offset(px, (0.45f + 0.4f * hash01(k, 983) + 0.015f * sin(t * 0.7f + k)) * u))
        }
        drawPoints(specks, PointMode.Points, Color(0xFFFFF6C8), strokeWidth = 0.004f * u, cap = StrokeCap.Round, alpha = 0.6f * day)
    }
    if (n > 0.3f && calm > 0.3f) {
        val a = ramp((n - 0.3f) / 0.5f) * calm
        for (k in 0 until 12) {
            val fx = st.px(0.4f + k * 0.78f + sin(t * 0.3f + k * 2.1f) * 0.5f, 1.0f)
            val fy = (0.62f + 0.3f * hash01(k, 991) + 0.025f * sin(t * 0.7f + k * 1.3f)) * u
            if (fx < -0.05f * u || fx > st.w + 0.05f * u) continue
            val gl = 0.5f + 0.5f * sin(t * 1.6f + k * 2.7f)
            drawCircle(
                safeRadialGradient(listOf(Color(0xFFE8FF9A).copy(alpha = 0.8f * gl * a), Color(0xFFE8FF9A).copy(alpha = 0f)), center = Offset(fx, fy), radius = 0.022f * u),
                0.022f * u, Offset(fx, fy), blendMode = BlendMode.Screen,
            )
            drawCircle(Color(0xFFFFFFD0), 0.0025f * u, Offset(fx, fy), alpha = gl * a)
        }
    }
}

private fun DrawScope.gsButterfly(bx: Float, by: Float, s: Float, flap: Float, color: Color, pen: Pen) {
    val wing = Path().apply {
        moveTo(bx, by)
        cubicTo(bx - s * 1.4f, by - s * (1.4f * flap + 0.1f), bx - s * 1.6f, by + s * 0.2f, bx, by + s * 0.3f)
        cubicTo(bx + s * 1.6f, by + s * 0.2f, bx + s * 1.4f, by - s * (1.4f * flap + 0.1f), bx, by)
        close()
    }
    drawPath(wing, color)
    drawPath(wing, Ink.line, alpha = 0.7f * color.alpha, style = pen.thin)
    drawLine(Ink.line, Offset(bx, by - s * 0.2f), Offset(bx, by + s * 0.35f), strokeWidth = pen.lw, alpha = color.alpha, cap = StrokeCap.Round)
}

private fun DrawScope.gsDragonfly(x: Float, y: Float, s: Float, phase: Float, pen: Pen, alpha: Float) {
    val beat = 0.6f + 0.4f * abs(sin(phase))
    val body = Color(0xFF2F9BFF)
    drawLine(Ink.line, Offset(x - s * 2.4f, y), Offset(x + s * 1.2f, y), strokeWidth = s * 0.55f, alpha = alpha, cap = StrokeCap.Round)
    drawLine(body, Offset(x - s * 2.4f, y), Offset(x + s * 1.2f, y), strokeWidth = s * 0.34f, alpha = alpha, cap = StrokeCap.Round)
    for (k in 0..1) {
        val wx = x - s * (0.2f + k * 0.9f)
        val w = Path().apply { addOval(Rect(wx - s * 0.1f, y - s * 1.5f * beat, wx + s * 0.3f, y)) }
        drawPath(w, Color.White, alpha = 0.55f * alpha)
        drawPath(w, Ink.line, alpha = 0.5f * alpha, style = pen.thin)
        val w2 = Path().apply { addOval(Rect(wx - s * 0.1f, y, wx + s * 0.3f, y + s * 1.2f * beat)) }
        drawPath(w2, Color.White, alpha = 0.45f * alpha)
    }
    drawCircle(Ink.line, s * 0.42f, Offset(x + s * 1.35f, y), alpha = alpha)
    drawCircle(Color(0xFF8FD9FF), s * 0.3f, Offset(x + s * 1.38f, y - s * 0.04f), alpha = alpha)
}
