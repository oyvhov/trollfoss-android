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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import app.trollfoss.domain.Season
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/*
 * The seasons' kit. One place of code decides what the year does to the colours: every function below
 * takes the colour a place already uses (its summer colour) and returns the colour for `pen.season`.
 * In summer they all return the colour untouched, so the plain look stays pixel-identical.
 *
 *   ground      meadow, moss, fields, ferns          snowy / fresh / dry and golden
 *   farGround   far hills and ridges                 the same, gentler
 *   farTrees    far rows of pines                    frosted / fresher / warmer
 *   conifer     near pines and spruces               a little frost, new growth, olive
 *   foliage     crowns of leafy trees and bushes     (bare in winter: callers draw twigs) fresh / one of five autumn colours per tree
 *   sandy       sand, paving, dirt tracks            snow dust / damp
 *   snowy       anything that carries snow on top    roofs, ledges, caps
 *   blade       grass blades and tufts               straw sticking out of the snow
 */

internal object SeasonPal {
    val fresh = Color(0xFFA4E052)
    val dry = Color(0xFFB8A04C)
    val straw = Color(0xFFC2A867)
    val autumn = arrayOf(Color(0xFFE8742A), Color(0xFFD9A62E), Color(0xFFD2443A), Color(0xFFF0B63C), Color(0xFFC9692A))
    val birchGold = Color(0xFFF0C23F)
    val blossom = arrayOf(Color(0xFFFFC2D6), Color(0xFFFFFFFF), Color(0xFFFFA8C5))
    val flowers = arrayOf(Color(0xFFFFFFFF), Color(0xFFFFD84A), Color(0xFFFF7FA8), Color(0xFF7FB2FF), Color(0xFFC59BFF))
    val litter = arrayOf(Color(0xFFE8742A), Color(0xFFD2443A), Color(0xFFF0B63C), Color(0xFFB5651D))
    val twig = Color(0xFF4A3B3B)
    val ice = Color(0xFFDDF1FB)
    val iceDeep = Color(0xFF9CCBE8)
    val water = Color(0xFF7CC8EE)
}

/** [c]'s lightness in the hue of [hue], blended in by [k]: cel shading survives a change of colour. */
internal fun recolor(c: Color, hue: Color, k: Float): Color {
    val f = (c.luminance() / max(0.04f, hue.luminance())).coerceIn(0.35f, 2.4f)
    val tinted = Color((hue.red * f).coerceIn(0f, 1f), (hue.green * f).coerceIn(0f, 1f), (hue.blue * f).coerceIn(0f, 1f), c.alpha)
    return lerp(c, tinted, k)
}

internal val Pen.winter: Boolean get() = season == Season.WINTER

/** Meadows, moss, fields and ferns. */
internal fun Pen.ground(c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> recolor(c, SeasonPal.fresh, 0.6f).lighten(0.06f)
    Season.AUTUMN -> recolor(c, SeasonPal.dry, 0.75f)
    Season.WINTER -> lerp(c, Pal.snow, 0.86f)
}

/** Far hills and ridges. */
internal fun Pen.farGround(c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> recolor(c, SeasonPal.fresh, 0.4f).lighten(0.04f)
    Season.AUTUMN -> recolor(c, SeasonPal.dry, 0.55f)
    Season.WINTER -> lerp(c, Pal.snow, 0.8f)
}

/** Far rows of pines. */
internal fun Pen.farTrees(c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> recolor(c, Color(0xFF6FC38A), 0.3f).lighten(0.05f)
    Season.AUTUMN -> lerp(c, Color(0xFFA08A4A), 0.4f)
    Season.WINTER -> lerp(c, Color(0xFFE4EDF7), 0.5f)
}

/** Near pines and spruces: they stay green all year. */
internal fun Pen.conifer(c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> lerp(c, Color(0xFF5DC070), 0.12f)
    Season.AUTUMN -> lerp(c, Color(0xFF4F6B35), 0.2f)
    Season.WINTER -> lerp(c, Color(0xFFCFE0EE), 0.14f)
}

/** The crown of a leafy tree or a bush; [salt] picks the autumn colour of this tree. Winter is bare: callers draw twigs. */
internal fun Pen.foliage(c: Color, salt: Int = 0): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> recolor(c, Color(0xFFB4E26E), 0.7f).lighten(0.05f)
    Season.AUTUMN -> {
        val k = (hash01(salt, 77) * SeasonPal.autumn.size).toInt().coerceIn(0, SeasonPal.autumn.size - 1)
        recolor(c, SeasonPal.autumn[k], 0.9f)
    }
    Season.WINTER -> lerp(c, Pal.snow, 0.55f)
}

/** Sand, paving and dirt: dusted with snow in winter, a little damp and grey in autumn. */
internal fun Pen.sandy(c: Color): Color = when (season) {
    Season.SUMMER, Season.SPRING -> c
    Season.AUTUMN -> lerp(c, Color(0xFFA89A82), 0.22f)
    Season.WINTER -> lerp(c, Pal.snow, 0.72f)
}

/** Anything that carries snow on top in winter: roofs, ledges, caps. */
internal fun Pen.snowy(c: Color, k: Float = 0.88f): Color = if (season == Season.WINTER) lerp(c, Pal.snow, k) else c

/** Blades of grass: bright in spring, straw in autumn, a few dry stems sticking out of the snow in winter. */
internal fun Pen.blade(c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> recolor(c, SeasonPal.fresh, 0.5f)
    Season.AUTUMN -> recolor(c, SeasonPal.dry, 0.8f)
    Season.WINTER -> lerp(c, SeasonPal.straw, 0.85f)
}

/** Ferns, hedges and bushes near the ground: fresh in spring, copper bracken in autumn, dry and frosted in winter. */
internal fun Pen.plant(c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> recolor(c, SeasonPal.fresh, 0.5f)
    Season.AUTUMN -> recolor(c, Color(0xFFC0702E), 0.75f)
    Season.WINTER -> lerp(c, Color(0xFFD8CBB0), 0.7f)
}

private val fieldsSpring = arrayOf(Color(0xFFF0E04A), Color(0xFF9AD66A), Color(0xFF9A6E48), Color(0xFF7FD05C))
private val fieldsAutumn = arrayOf(Color(0xFFD9B44C), Color(0xFFB98B5A), Color(0xFF9C6A3C), Color(0xFFC4A455))
private val fieldsWinter = arrayOf(Color(0xFFEAF1FB), Color(0xFFDCE7F6), Color(0xFFE6EEF8), Color(0xFFD6E2F2))

/** Patchwork fields: [i] is the field's index in the place's own list, [c] its summer colour. */
internal fun Pen.field(i: Int, c: Color): Color = when (season) {
    Season.SUMMER -> c
    Season.SPRING -> fieldsSpring[i.mod(4)]
    Season.AUTUMN -> fieldsAutumn[i.mod(4)]
    Season.WINTER -> fieldsWinter[i.mod(4)]
}

// ------------------------------------------------------------------------------------- twigs, blossom

/** Bare branches on top of a crown spot, with a little snow on them. Everything in one path. */
internal fun DrawScope.bareCrown(cx: Float, cy: Float, rx: Float, ry: Float, pen: Pen, salt: Int, night: Float, snow: Boolean = true) {
    val path = Path()
    val dots = ArrayList<Offset>(16)
    val nb = 7
    for (i in 0 until nb) {
        val a = (-1f + 2f * (i + 0.5f) / nb) * 1.2f + (hash01(i, salt) - 0.5f) * 0.3f
        val len = 0.8f + 0.35f * hash01(i, salt + 1)
        val sx = sin(a)
        val cs = cos(a)
        val mx = cx + sx * rx * len * 0.55f
        val my = cy - cs * ry * len * 0.55f
        val tx = cx + sx * rx * len + (hash01(i, salt + 2) - 0.5f) * rx * 0.2f
        val ty = cy - cs * ry * len
        path.moveTo(cx, cy)
        path.quadraticTo(mx + rx * 0.05f, my, tx, ty)
        for (side in intArrayOf(-1, 1)) {
            val fa = a + side * (0.6f + 0.2f * hash01(i * 2 + side, salt + 3))
            val fx = mx + sin(fa) * rx * len * 0.34f
            val fy = my - cos(fa) * ry * len * 0.34f
            path.moveTo(mx, my)
            path.lineTo(fx, fy)
            if (snow && (i + side) % 2 == 0) dots.add(Offset(fx, fy - ry * 0.02f))
        }
        if (snow) {
            dots.add(Offset(mx, my - ry * 0.03f))
            if (i % 2 == 0) dots.add(Offset(tx, ty))
        }
    }
    drawPath(path, SeasonPal.twig.atNight(night, 0.4f), style = Stroke(pen.lw * 1.25f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    if (snow && dots.isNotEmpty()) drawPoints(dots, PointMode.Points, Color.White.atNight(night, 0.4f), strokeWidth = max(pen.lw * 1.8f, ry * 0.1f), cap = StrokeCap.Round)
}

/** Pink and white blossom scattered over a crown. */
internal fun DrawScope.blossomDots(cx: Float, cy: Float, rx: Float, ry: Float, salt: Int, night: Float, count: Int = 15, size: Float = ry * 0.15f) {
    val a = ArrayList<Offset>(count / 3 + 1)
    val b = ArrayList<Offset>(count / 3 + 1)
    val c = ArrayList<Offset>(count / 3 + 1)
    for (i in 0 until count) {
        val ang = hash01(i, salt + 5) * 6.2831855f
        val r = 0.25f + 0.75f * hash01(i, salt + 6)
        val p = Offset(cx + cos(ang) * rx * 0.85f * r, cy + sin(ang) * ry * 0.8f * r)
        when (i % 3) {
            0 -> a.add(p)
            1 -> b.add(p)
            else -> c.add(p)
        }
    }
    drawPoints(a, PointMode.Points, SeasonPal.blossom[0].atNight(night, 0.3f), strokeWidth = size, cap = StrokeCap.Round)
    drawPoints(b, PointMode.Points, SeasonPal.blossom[1].atNight(night, 0.3f), strokeWidth = size, cap = StrokeCap.Round)
    drawPoints(c, PointMode.Points, SeasonPal.blossom[2].atNight(night, 0.3f), strokeWidth = size * 0.9f, cap = StrokeCap.Round)
}

/**
 * A whole tree for any season, standing at ([bx], [by]) and [h] tall: round crown on a brown trunk. Used where a
 * place has no trees of its own (the park, the slope). Summer draws the plain green tree too.
 */
internal fun DrawScope.seasonTree(bx: Float, by: Float, h: Float, pen: Pen, salt: Int, night: Float, base: Color = Color(0xFF6DBB5A), trunk: Color = Color(0xFF8A5A3A)) {
    val tw = h * 0.07f
    val tp = Path().apply { poly(bx - tw * 0.45f, by - h * 0.62f, bx + tw * 0.45f, by - h * 0.62f, bx + tw * 0.7f, by, bx - tw * 0.7f, by) }
    inked(tp, trunk.atNight(night, 0.45f), pen)
    val cy = by - h * 0.74f
    if (pen.winter) {
        bareCrown(bx, by - h * 0.55f, h * 0.3f, h * 0.38f, pen, salt, night)
        return
    }
    val crown = crownPath(bx, cy, h * 0.3f, h * 0.26f, salt)
    inked(crown, pen.foliage(base, salt).atNight(night, 0.45f), pen)
    if (pen.season == Season.SPRING) blossomDots(bx, cy, h * 0.3f, h * 0.26f, salt, night)
}

// ------------------------------------------------------------------------------------- ground cover

/**
 * What lies on a stretch of ground in each season, built once per scale: dry leaves in autumn, flowers in
 * spring, drifts and sparkles in winter. Scene x [x0]..[x1] and heights [y0]..[y1], [salt] for the dice.
 */
internal class GroundCover(
    private val x0: Float, private val x1: Float, private val y0: Float, private val y1: Float,
    private val salt: Int, private val density: Float = 1f,
) {
    private class Geo(
        val litter: Array<ArrayList<Offset>>, val flowers: Array<ArrayList<Offset>>, val hearts: List<Offset>,
        val drifts: Path, val sparks: List<Offset>,
    )

    private val memo = Memo { u -> build(u) }

    private fun build(u: Float): Geo {
        val w = x1 - x0
        // Keep the walking strip clear; seasonal colour gathers beside the play area.
        val n = max(2, (w * 16f * density * if (y0 > 0.93f) 0.12f else 0.38f).toInt())
        val litter = Array(4) { ArrayList<Offset>(n * 2) }
        val flowers = Array(5) { ArrayList<Offset>(n) }
        val hearts = ArrayList<Offset>(n * 2)
        for (i in 0 until n * 4) {
            val x = mix(x0, x1, hash01(i, salt))
            val y = mix(y0, y1, hash01(i, salt + 1))
            val a = hash01(i, salt + 2) * 6.2831855f
            val l = (0.007f + 0.008f * hash01(i, salt + 3)) * u
            litter[i % 4].add(Offset(x * u, y * u))
            litter[i % 4].add(Offset(x * u + cos(a) * l, y * u + sin(a) * l * 0.5f))
        }
        // Flowers grow in little clusters, each mostly one colour.
        val clusters = max(2, n / 4)
        for (i in 0 until n * 2) {
            val c = (hash01(i, salt + 10) * clusters).toInt().coerceIn(0, clusters - 1)
            val x = mix(x0, x1, hash01(c, salt + 14)) + (hash01(i, salt + 11) - 0.5f) * 0.11f
            val y = mix(y0, y1, hash01(c, salt + 15)) + (hash01(i, salt + 12) - 0.5f) * 0.026f
            val colour = (c + if (hash01(i, salt + 13) > 0.75f) 1 else 0) % 5
            flowers[colour].add(Offset(x * u, y * u))
            hearts.add(Offset(x * u, y * u))
        }
        val drifts = Path()
        for (i in 0 until max(3, (w * 3.2f * density).toInt())) {
            val x = mix(x0, x1, hash01(i, salt + 21))
            val y = mix(y0, y1, hash01(i, salt + 22))
            drifts.floorDisc(u, 0f, x, y, 0.07f + 0.08f * hash01(i, salt + 23), 0.045f, 14)
        }
        val sparks = ArrayList<Offset>(8)
        for (i in 0 until max(4, (w * 2.5f * density).toInt())) {
            sparks.add(Offset(mix(x0, x1, hash01(i, salt + 31)) * u, mix(y0, y1, hash01(i, salt + 32)) * u))
        }
        return Geo(litter, flowers, hearts, drifts, sparks)
    }

    fun draw(scope: DrawScope, st: Stage, pen: Pen) = with(scope) {
        if (pen.season == Season.SUMMER || !st.sees(x0, x1)) return@with
        val u = st.u
        val g = memo.of(u)
        val n = pen.night
        inScene(st) {
            when (pen.season) {
                Season.AUTUMN -> for (i in 0 until 4) {
                    drawPoints(g.litter[i], PointMode.Lines, SeasonPal.litter[i].atNight(n, 0.4f), strokeWidth = 0.0075f * u, cap = StrokeCap.Round)
                }
                Season.SPRING -> {
                    for (i in 0 until 5) drawPoints(g.flowers[i], PointMode.Points, SeasonPal.flowers[i].atNight(n, 0.4f), strokeWidth = 0.0125f * u, cap = StrokeCap.Round)
                    drawPoints(g.hearts, PointMode.Points, Color(0xFFFFE680).atNight(n, 0.4f), strokeWidth = 0.0042f * u, cap = StrokeCap.Round)
                }
                Season.WINTER -> {
                    translate(0.01f * u, 0.006f * u) { drawPath(g.drifts, Pal.snowShade.atNight(n, 0.5f), alpha = 0.5f) }
                    drawPath(g.drifts, Color.White.atNight(n, 0.5f), alpha = 0.7f)
                    drawPath(g.drifts, Ink.line, alpha = 0.18f, style = pen.thin)
                }
                Season.SUMMER -> Unit
            }
        }
        if (pen.season == Season.WINTER) {
            for ((i, p) in g.sparks.withIndex()) {
                val sx = p.x - st.cam * u
                if (sx < -0.05f * u || sx > st.w + 0.05f * u) continue
                val a = max(0f, sin(pen.t * 1.3f + i * 2.2f))
                if (a > 0.05f) twinkle(Offset(sx, p.y), 0.011f * u, Color.White, a * (1f - 0.4f * n))
            }
        }
    }
}

// ------------------------------------------------------------------------------------- water and air

/** A little stream winding over the ground through points of scene (x, y) pairs; it ripples with [Pen.t]. */
internal class Brook(private val pts: FloatArray) {
    private val memo = Memo { u ->
        val n = pts.size / 2
        val xs = FloatArray(n) { pts[it * 2] * u }
        val ys = FloatArray(n) { pts[it * 2 + 1] * u }
        Path().apply { smoothRun(xs, ys, n, start = true) }
    }

    fun draw(scope: DrawScope, st: Stage, pen: Pen, width: Float, color: Color = SeasonPal.water) = with(scope) {
        val u = st.u
        val p = memo.of(u)
        val n = pen.night
        val c = color.atNight(n, 0.45f)
        inScene(st) {
            drawPath(p, Ink.line, style = Stroke(width * u + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(p, c, style = Stroke(width * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            val dash = PathEffect.dashPathEffect(floatArrayOf(0.03f * u, 0.07f * u), wrap(-pen.t * 0.05f * u, 0.1f * u))
            drawPath(p, Color.White, alpha = 0.7f * (1f - 0.4f * n), style = Stroke(width * u * 0.28f, cap = StrokeCap.Round, pathEffect = dash))
        }
    }
}

/** A band of mist lying over the ground: a soft gradient with a few slow wisps. [p] is its parallax. */
internal fun DrawScope.mistBand(st: Stage, pen: Pen, y: Float, thick: Float, alpha: Float, p: Float, salt: Int) {
    val u = st.u
    val oc = overcast(pen)
    val col = lerp(Color(0xFFF7F2E8), Color(0xFFD5DBE8), oc).atNight(pen.night, 0.7f)
    val a = (alpha * (1f + 0.5f * oc)).coerceAtMost(0.9f)
    drawRect(
        Brush.verticalGradient(0f to col.copy(alpha = 0f), 0.5f to col.copy(alpha = a), 1f to col.copy(alpha = 0f), startY = (y - thick) * u, endY = (y + thick) * u),
        Offset(0f, (y - thick) * u), Size(st.w, 2f * thick * u),
    )
    val span = st.w + 1.2f * u
    for (i in 0 until 4) {
        val s = u * 0.06f * (0.8f + 0.7f * hash01(i, salt))
        val x = wrap(hash01(i, salt + 1) * span + pen.t * u * 0.004f * (1f + hash01(i, salt + 2)) - st.cam * u * p, span) - 0.6f * u
        val yy = (y + (hash01(i, salt + 3) - 0.5f) * thick * 1.1f) * u
        val w = s * 7f
        val h = s * 0.55f
        drawOval(col, Offset(x - w * 0.5f, yy - h * 0.5f), Size(w, h), alpha = 0.5f * a)
        drawOval(col, Offset(x - w * 0.3f, yy - h * 0.95f), Size(w * 0.56f, h), alpha = 0.45f * a)
    }
}

/** A sheet of ice over an area (a path in pixels): pale glass with a soft edge and a few cracks. */
internal fun DrawScope.iceSheet(area: Path, cracks: List<Offset>, pen: Pen, alpha: Float = 0.9f) {
    val n = pen.night
    drawPath(area, SeasonPal.ice.atNight(n, 0.45f), alpha = alpha)
    drawPath(area, Color.White, alpha = 0.55f * alpha, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
    if (cracks.isNotEmpty()) drawPoints(cracks, PointMode.Lines, SeasonPal.iceDeep.atNight(n, 0.4f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round, alpha = 0.8f)
}

/** The shape of a row of icicles hanging from the points [xs]/[ys] (pixels), each [w] wide and [lens] long. Build once per scale. */
internal fun iciclePath(xs: FloatArray, ys: FloatArray, lens: FloatArray, w: Float): Path {
    val p = Path()
    for (i in xs.indices) {
        p.moveTo(xs[i] - w, ys[i])
        p.lineTo(xs[i] + w, ys[i])
        p.lineTo(xs[i], ys[i] + lens[i])
        p.close()
    }
    return p
}

/** Draws icicles made by [iciclePath]. */
internal fun DrawScope.icicles(path: Path, pen: Pen) {
    drawPath(path, Color(0xFFE6F5FF).atNight(pen.night, 0.4f))
    drawPath(path, Ink.line, alpha = 0.55f, style = pen.thin)
}

/** A small snow cap on a post or a rail: a white lozenge lying on [rect]'s top edge. */
internal fun DrawScope.snowCap(left: Float, right: Float, y: Float, h: Float, pen: Pen) {
    val r = Rect(left, y - h, right, y + h * 0.3f)
    val p = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(r, androidx.compose.ui.geometry.CornerRadius(h * 0.9f))) }
    drawPath(p, Color.White.atNight(pen.night, 0.4f))
    drawPath(p, Ink.line, alpha = 0.4f, style = pen.thin)
}
