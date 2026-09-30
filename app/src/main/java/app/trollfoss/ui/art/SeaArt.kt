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
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.PlaceId
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

/*
 * The sea floor off Trollfoss beach. The shimmering surface near the top with the sky above it, light
 * rays slanting down, blue fading from turquoise to deep, far rocks and kelp swaying with parallax, a
 * school of fish, rising bubbles and a sandy floor band with ripples and shells. At night the water
 * darkens and plankton glows.
 */

private const val SURFACE = 0.085f

private fun seaSandBackY(x: Float) = 0.782f + 0.003f * sin(x * 4.7f) + 0.002f * sin(x * 13.3f)

private class SeaStatic(val sand: Path, val edge: Path, val ripples: Path, val shells: Path, val pebbles: Path, val grass: Path)

private val seaStatic = Memo { u ->
    val sand = Path()
    val edge = Path()
    for (i in 0..46) {
        val x = -0.4f + i * 0.1f
        if (i == 0) {
            sand.moveTo(x * u, seaSandBackY(x) * u)
            edge.moveTo(x * u, seaSandBackY(x) * u)
        } else {
            sand.lineTo(x * u, seaSandBackY(x) * u)
            edge.lineTo(x * u, seaSandBackY(x) * u)
        }
    }
    sand.lineTo(4.2f * u, FRONT_Y * u)
    sand.lineTo(-0.4f * u, FRONT_Y * u)
    sand.close()
    val ripples = Path()
    for (i in 0 until 50) {
        val x = -0.2f + hash01(i, 1001) * 4.2f
        val y = mix(0.8f, FRONT_Y - 0.01f, hash01(i, 1002))
        ripples.moveTo((x - 0.035f) * u, y * u)
        ripples.quadraticTo((x - 0.017f) * u, (y - 0.006f) * u, x * u, y * u)
        ripples.quadraticTo((x + 0.017f) * u, (y + 0.006f) * u, (x + 0.035f) * u, y * u)
    }
    val shells = Path()
    for (i in 0 until 6) {
        val c = Offset((0.3f + i * 0.62f + hash01(i, 1003) * 0.2f) * u, mix(0.84f, 0.95f, hash01(i, 1004)) * u)
        val s = (0.018f + 0.008f * hash01(i, 1005)) * u
        shells.moveTo(c.x, c.y)
        shells.lineTo(c.x - s * 0.5f, c.y - s * 0.5f)
        shells.quadraticTo(c.x, c.y - s * 1.1f, c.x + s * 0.5f, c.y - s * 0.5f)
        shells.close()
    }
    val pebbles = Path()
    for (i in 0 until 12) {
        pebbles.floorDisc(u, 0f, -0.2f + hash01(i, 1006) * 4.1f, mix(0.81f, 0.96f, hash01(i, 1007)), 0.012f + 0.014f * hash01(i, 1008), 0.02f, 10)
    }
    val grass = Path()
    for (i in 0 until 14) {
        val x = -0.2f + i * 0.3f + hash01(i, 1009) * 0.12f
        grass.addPath(tuftPath(x * u, (seaSandBackY(x) + 0.008f) * u, 0.05f * u, 0f))
    }
    SeaStatic(sand, edge, ripples, shells, pebbles, grass)
}

internal fun DrawScope.underwaterBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val oc = overcast(pen)
    drawSky(st, pen, Mood.SUMMER, SURFACE)
    // Above the surface (seen on tall screens): the sun, clouds and gulls.
    drawSun(Offset(st.fx(0.25f, 0.04f), -0.2f * u), 0.05f * u, pen, (1f - n) * (1f - oc))
    drawMoon(Offset(st.fx(0.7f, 0.04f), -0.22f * u), 0.04f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawClouds(st, pen, -0.3f, -0.08f, 3, 0.05f, salt = 21)
    drawGulls(st, pen, 2, -0.26f, -0.08f, salt = 13)
    // The water, turquoise under the surface and deep blue near the floor.
    val count = 24
    val xs = FloatArray(count)
    val ys = FloatArray(count)
    for (i in 0 until count) {
        val sx = -0.05f * st.w + i * st.w * 1.1f / (count - 1)
        val lx = sx / u + st.cam
        xs[i] = sx
        ys[i] = (SURFACE + 0.006f * sin(lx * 9f + t * 1.6f) + 0.003f * sin(lx * 23f - t * 2.4f)) * u
    }
    val water = Path().apply {
        smoothRun(xs, ys, count, start = true)
        lineTo(xs[count - 1], st.h + 4f)
        lineTo(xs[0], st.h + 4f)
        close()
    }
    val dim = oc * 0.3f
    drawPath(
        water,
        Brush.verticalGradient(
            0f to Color(0xFF7EDCE8).darken(dim).atNight(n, 0.75f),
            0.28f to Color(0xFF3FB2D2).darken(dim).atNight(n, 0.78f),
            0.62f to Color(0xFF2477B8).darken(dim).atNight(n, 0.8f),
            1f to Color(0xFF174E8A).darken(dim).atNight(n, 0.8f),
            startY = SURFACE * u, endY = u,
        ),
    )
    clipPath(water) {
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f * (1f - 0.6f * n)), Color.Transparent), startY = SURFACE * u, endY = (SURFACE + 0.07f) * u), Offset(0f, SURFACE * u - 4f), Size(st.w, 0.08f * u))
        surfaceShimmer(st, pen)
        lightRays(st, pen)
    }
    drawPath(Path().apply { smoothRun(xs, ys, count, start = true) }, Color.White, alpha = 0.85f, style = Stroke(pen.lw * 1.8f, cap = StrokeCap.Round))
    drawPath(Path().apply { smoothRun(xs, ys, count, start = true) }, Ink.line, alpha = 0.35f, style = pen.thin)

    // Far rocks and kelp, then nearer ones.
    val farRock = Color(0xFF3A7FAE).atNight(n, 0.8f)
    kelpRow(st, pen, 0.2f, 0.7f, 0.08f, 1011, farRock.lighten(0.08f), 0.18f, 0.32f)
    drawPath(ridgePath(st, 0.2f, 0.7f, 0.08f, 1011), farRock)
    fishSchool(st, pen)
    val midRock = Color(0xFF2B6594).atNight(n, 0.8f)
    kelpRow(st, pen, 0.45f, 0.775f, 0.045f, 1013, Color(0xFF2E8A86).atNight(n, 0.75f), 0.14f, 0.26f)
    drawPath(ridgePath(st, 0.45f, 0.775f, 0.045f, 1013), midRock)
    drawPath(ridgePath(st, 0.45f, 0.775f, 0.045f, 1013, closed = false), Ink.line, alpha = 0.3f, style = pen.thin)

    val ss = seaStatic.of(u)
    val back = PlaceId.UNDERWATER.back
    inScene(st) {
        drawPath(ss.grass, Color(0xFF3F9E72).atNight(n, 0.6f))
        drawPath(
            ss.sand,
            Brush.verticalGradient(listOf(Color(0xFFB9B88E).atNight(n, 0.65f), Color(0xFFE8D6A4).atNight(n, 0.6f)), startY = back * u, endY = FRONT_Y * u),
        )
        drawPath(ss.ripples, Color(0xFFC9B27E).atNight(n, 0.6f), alpha = 0.7f, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
        drawPath(ss.pebbles, Color(0xFF9DA7B5).atNight(n, 0.6f))
        drawPath(ss.pebbles, Ink.line, alpha = 0.5f, style = pen.thin)
        drawPath(ss.shells, Color(0xFFFFC9D2).atNight(n, 0.55f))
        drawPath(ss.shells, Ink.line, style = pen.thin)
        drawPath(ss.edge, Ink.line, alpha = 0.5f, style = pen.thin)
    }
    // Dappled light on the sand.
    if (n < 1f) {
        val dapple = Path()
        for (i in 0 until 10) {
            val x = floor(st.cam / 0.4f) * 0.4f + i * 0.4f + sin(t * 0.7f + i * 1.9f) * 0.05f
            dapple.floorDisc(u, st.cam, x, mix(0.82f, 0.94f, hash01(i, 1015)), 0.05f + 0.02f * sin(t * 1.1f + i), 0.04f, 12)
        }
        drawPath(dapple, Color.White, alpha = 0.12f * (1f - n) * (1f - oc))
    }
    bubbles(st, pen)
    plankton(st, pen)
    drawBase(st, pen, Color(0xFFE8D6A4).atNight(n, 0.6f), Color(0xFF8E7A5A).atNight(n, 0.6f))
    drawBaseStones(st, pen, Color(0xFF8C97A8).atNight(n, 0.6f), 1017)
}

/** A net of light just under the surface. */
private fun DrawScope.surfaceShimmer(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val c = Path()
    for (k in 0 until 4) {
        val depth = SURFACE + 0.025f + k * 0.03f
        val off = wrap(t * 0.02f * (k + 1) + k * 0.07f, 0.12f)
        var x = floor((st.left - off) / 0.12f) * 0.12f + off
        while (x < st.right) {
            val x0 = st.x(x)
            val len = 0.06f * u
            val yy = (depth + 0.006f * sin(x * 11f + t * 1.7f + k)) * u
            c.moveTo(x0, yy)
            c.quadraticTo(x0 + len * 0.5f, yy - 0.014f * u * sin(t * 2.2f + x * 9f + k), x0 + len, yy)
            x += 0.12f
        }
    }
    drawPath(c, Color.White, alpha = 0.3f * (1f - 0.6f * pen.night), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
}

/** Sunbeams slanting down through the water, breathing slowly. */
private fun DrawScope.lightRays(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val k = (1f - 0.75f * pen.night) * (1f - 0.6f * overcast(pen))
    if (k <= 0.02f) return
    val p = 0.6f
    val spacing = 0.55f
    val l0 = st.cam * p - 0.5f
    val l1 = st.cam * p + st.vw + 0.3f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        val lx = i * spacing + hash01(i, 1021) * 0.2f
        val x0 = st.px(lx, p) + sin(t * 0.3f + i) * 0.01f * u
        val w0 = (0.04f + 0.03f * hash01(i, 1022)) * u
        val x1 = x0 + 0.2f * u
        val w1 = w0 * 3f
        val ray = Path().apply { poly(x0 - w0 / 2f, SURFACE * u, x0 + w0 / 2f, SURFACE * u, x1 + w1 / 2f, 0.8f * u, x1 - w1 / 2f, 0.8f * u) }
        val a = k * (0.1f + 0.06f * sin(t * 0.6f + i * 2.1f))
        drawPath(ray, Brush.verticalGradient(listOf(Color.White.copy(alpha = a), Color.White.copy(alpha = 0f)), startY = SURFACE * u, endY = 0.8f * u))
    }
}

/** Kelp swaying on a far rock ridge at parallax [p]. */
private fun DrawScope.kelpRow(st: Stage, pen: Pen, p: Float, base: Float, amp: Float, salt: Int, color: Color, minH: Float, maxH: Float) {
    val u = st.u
    val t = pen.t
    val spacing = 0.16f
    val kelp = Path()
    val l0 = st.cam * p - 0.2f
    val l1 = st.cam * p + st.vw + 0.2f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        if (hash01(i, salt + 1) < 0.35f) continue
        val lx = i * spacing + hash01(i, salt + 2) * 0.08f
        val sx = st.px(lx, p)
        val by = (ridgeY(lx, base, amp, salt) + 0.02f) * u
        val h = mix(minH, maxH, hash01(i, salt + 3)) * u
        val sway = sin(t * 0.8f + i * 1.3f) * 0.03f * u
        kelp.moveTo(sx, by)
        kelp.cubicTo(sx + 0.02f * u, by - h * 0.35f, sx - 0.02f * u + sway * 0.5f, by - h * 0.7f, sx + sway, by - h)
    }
    drawPath(kelp, color, style = Stroke(0.012f * u, cap = StrokeCap.Round))
}

/** A school of small fish drifting by far away. */
private fun DrawScope.fishSchool(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val col = Color(0xFF1E5586).atNight(pen.night, 0.6f)
    val fish = Path()
    val span = st.vw + 1.2f
    for (school in 0 until 2) {
        val cx = st.cam * 0.3f + wrap(school * 1.1f + t * 0.025f, span) - 0.6f
        val cy = 0.36f + school * 0.14f + sin(t * 0.3f + school) * 0.02f
        for (k in 0 until 9) {
            val fx = st.px(cx + (hash01(k, 1031 + school) - 0.5f) * 0.28f + sin(t * 0.9f + k) * 0.01f, 0.3f)
            val fy = (cy + (hash01(k, 1033 + school) - 0.5f) * 0.1f) * u
            val s = 0.012f * u
            fish.addOval(Rect(fx - s, fy - s * 0.45f, fx + s, fy + s * 0.45f))
            fish.moveTo(fx - s * 0.8f, fy)
            fish.lineTo(fx - s * 1.6f, fy - s * 0.5f)
            fish.lineTo(fx - s * 1.6f, fy + s * 0.5f)
            fish.close()
        }
    }
    drawPath(fish, col, alpha = 0.55f)
}

/** Streams of bubbles rising from the sand. */
private fun DrawScope.bubbles(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val stroke = Stroke(pen.lw * 0.9f)
    for ((j, bx) in floatArrayOf(0.22f, 1.55f, 2.7f, 3.55f).withIndex()) {
        if (!st.sees(bx - 0.1f, bx + 0.1f)) continue
        for (k in 0 until 6) {
            val ph = wrap(t * (0.12f + 0.03f * j) + k / 6f, 1f)
            val y = mix(0.8f, SURFACE + 0.02f, ph)
            val x = bx + sin(ph * 14f + k + j) * 0.012f
            val r = (0.006f + 0.008f * ph) * u
            val c = st.o(x, y)
            drawCircle(Color.White, r, c, alpha = 0.7f * (1f - ph * 0.4f), style = stroke)
            drawCircle(Color.White, r * 0.3f, Offset(c.x - r * 0.35f, c.y - r * 0.35f), alpha = 0.8f)
        }
    }
}

/** Glowing plankton drifting about at night. */
private fun DrawScope.plankton(st: Stage, pen: Pen) {
    val k = ramp((pen.night - 0.25f) / 0.5f)
    if (k <= 0f) return
    val u = st.u
    val t = pen.t
    for (g in 0 until 2) {
        val pts = ArrayList<Offset>(30)
        for (i in g until 60 step 2) {
            val x = st.cam - 0.2f + wrap(hash01(i, 1041) * 4.2f + sin(t * 0.2f + i) * 0.05f - st.cam, st.vw + 0.4f)
            val y = mix(SURFACE + 0.05f, 0.95f, hash01(i, 1042)) + sin(t * 0.3f + i * 1.7f) * 0.02f
            pts.add(st.o(x, y))
        }
        val a = k * (0.5f + 0.5f * sin(t * (1.3f + g) + g * 2.4f))
        drawPoints(pts, PointMode.Points, Color(0xFF7CFFE0), strokeWidth = 0.026f * u, cap = StrokeCap.Round, alpha = 0.18f * a)
        drawPoints(pts, PointMode.Points, Color(0xFFCFFFF4), strokeWidth = 0.008f * u, cap = StrokeCap.Round, alpha = max(0.2f, a))
    }
}

internal fun DrawScope.underwaterFront(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val kelp = Path()
    for ((i, x) in floatArrayOf(0.12f, 0.95f, 2.2f, 3.3f).withIndex()) {
        if (!st.sees(x - 0.1f, x + 0.1f)) continue
        for (k in 0 until 3) {
            val bx = st.x(x + k * 0.025f)
            val by = (FRONT_Y + 0.02f) * u
            val h = (0.07f + 0.03f * ((k + i) % 3)) * u
            val sway = sin(t * 1.2f + i * 1.7f + k) * 0.015f * u
            kelp.moveTo(bx - 0.006f * u, by)
            kelp.quadraticTo(bx - 0.01f * u + sway * 0.5f, by - h * 0.5f, bx + sway, by - h)
            kelp.quadraticTo(bx + 0.012f * u + sway * 0.5f, by - h * 0.5f, bx + 0.006f * u, by)
            kelp.close()
        }
    }
    drawPath(kelp, Color(0xFF2E9E6A).atNight(n, 0.5f))
    drawPath(kelp, Ink.line, style = pen.thin)
    for (i in 0 until 5) {
        val ph = wrap(t * 0.2f + i / 5f, 1f)
        val x = hash01(i, 1051) * 3.8f + sin(ph * 10f + i) * 0.01f
        if (!st.sees(x, x)) continue
        val c = st.o(x, mix(1.0f, 0.86f, ph))
        drawCircle(Color.White, (0.006f + 0.004f * ph) * u, c, alpha = 0.7f * (1f - ph), style = Stroke(pen.lw * 0.8f))
    }
}
