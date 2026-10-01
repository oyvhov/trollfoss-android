package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.sin

/*
 * The tivoli: an amusement park by the fjord in oblique 3D. Strings of coloured bulbs, an entrance
 * arch at the far left, tents and a roller coaster far back, a paved ground band with confetti and a
 * bumper-car arena on the right. Lovely in the evening: the bulbs and tents glow at night.
 */

private const val TIVOLI_HZ = 0.64f
private const val ARENA_X0 = 3.15f
private const val ARENA_X1 = 4.35f

private val bulbColors = listOf(Color(0xFFFF5A6E), Color(0xFFFFC83D), Color(0xFF5CE0A0), Color(0xFF5AA9E6), Color(0xFFD77BFF))

/** Where the strings of bulbs hang from, left to right (scene x, height). */
private val stringAnchors = floatArrayOf(-0.4f, 0.1f, 1.22f, 0.1f, 2.47f, 0.1f, 3.39f, 0.16f)

private class TivoliStatic(
    val pave: Path, val paveEdge: Path, val joints: List<Offset>, val verge: Path, val confetti: List<List<Offset>>,
    val arena: Path, val arenaTiles: Path, val strings: Path, val bulbs: List<List<Offset>>,
)

private fun paveBackY(x: Float) = 0.782f + 0.002f * sin(x * 7.3f)

private val tivoliStatic = Memo { u ->
    val back = PlaceId.TIVOLI.back
    val pave = Path()
    val paveEdge = Path()
    for (i in 0..52) {
        val x = -0.4f + i * 0.1f
        if (i == 0) {
            pave.moveTo(x * u, paveBackY(x) * u)
            paveEdge.moveTo(x * u, paveBackY(x) * u)
        } else {
            pave.lineTo(x * u, paveBackY(x) * u)
            paveEdge.lineTo(x * u, paveBackY(x) * u)
        }
    }
    pave.lineTo(4.8f * u, FRONT_Y * u)
    pave.lineTo(-0.4f * u, FRONT_Y * u)
    pave.close()
    val joints = ArrayList<Offset>()
    for (k in 1..3) {
        val y = mix(FRONT_Y, back, k / 4f)
        joints.add(Offset((-0.4f + recede(y)) * u, y * u))
        joints.add(Offset((4.8f + recede(y)) * u, y * u))
    }
    var jx = -0.7f
    while (jx < 4.7f) {
        joints.add(Offset(jx * u, FRONT_Y * u))
        joints.add(Offset((jx + recede(back)) * u, back * u))
        jx += 0.16f
    }
    val verge = Path()
    verge.moveTo(-0.4f * u, (back - 0.004f) * u)
    for (i in 0..52) {
        val x = -0.4f + i * 0.1f
        verge.lineTo(x * u, (paveBackY(x) + 0.012f + 0.004f * sin(x * 19f)) * u)
    }
    verge.lineTo(4.8f * u, (back - 0.004f) * u)
    verge.close()
    val confetti = List(bulbColors.size) { ArrayList<Offset>(20) }
    for (i in 0 until 90) {
        val y = mix(0.8f, FRONT_Y - 0.008f, hash01(i, 901))
        val x = -0.3f + hash01(i, 902) * 4.9f
        if (x > ARENA_X0 + recede(y) - 0.02f && x < ARENA_X1 + recede(y) + 0.02f) continue
        confetti[i % confetti.size].add(Offset(x * u, y * u))
    }
    val arena = Path().apply { floorQuad(u, 0f, ARENA_X0, ARENA_X1, FRONT_Y, 0.8f) }
    val arenaTiles = Path()
    for (j in 0 until 4) {
        val yf = mix(FRONT_Y, 0.8f, j / 4f)
        val yb = mix(FRONT_Y, 0.8f, (j + 1) / 4f)
        var i = 0
        while (ARENA_X0 + (i + 1) * 0.12f <= ARENA_X1 + 0.001f) {
            if ((i + j) % 2 == 0) arenaTiles.floorQuad(u, 0f, ARENA_X0 + i * 0.12f + recede(yf) - recede(FRONT_Y), ARENA_X0 + (i + 1) * 0.12f + recede(yf), yf, yb)
            i++
        }
    }
    val strings = Path()
    val bulbs = List(bulbColors.size) { ArrayList<Offset>(40) }
    var b = 0
    for (k in 0 until stringAnchors.size / 2 - 1) {
        val ax = stringAnchors[k * 2]
        val ay = stringAnchors[k * 2 + 1]
        val bx = stringAnchors[k * 2 + 2]
        val by = stringAnchors[k * 2 + 3]
        val sag = 0.06f
        strings.moveTo(ax * u, ay * u)
        strings.quadraticTo((ax + bx) / 2f * u, ((ay + by) / 2f + sag * 2f) * u, bx * u, by * u)
        val n = ((bx - ax) / 0.075f).toInt()
        for (j in 1 until n) {
            val s = j / n.toFloat()
            val x = mix(ax, bx, s)
            val y = mix(ay, by, s) + 4f * sag * s * (1f - s) + 0.012f
            bulbs[b % bulbs.size].add(Offset(x * u, y * u))
            b++
        }
    }
    TivoliStatic(pave, paveEdge, joints, verge, confetti, arena, arenaTiles, strings, bulbs)
}

internal fun DrawScope.tivoliBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    drawSky(st, pen, Mood.SUMMER, TIVOLI_HZ)
    drawStars(st, pen, 0.5f)
    drawMoon(Offset(st.fx(0.66f, 0.04f), 0.12f * u), 0.038f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, pen, 0.6f, 0.0f, 0.26f)
    drawSun(Offset(st.fx(0.8f, 0.04f), 0.15f * u), 0.05f * u, pen, (1f - n) * (1f - overcast(pen)))
    drawRainbow(Offset(st.fx(0.4f, 0.08f), TIVOLI_HZ * u), 0.46f * u, 0.016f * u, pen.rainbow)
    drawClouds(st, pen, 0.04f, 0.24f, 3, 0.055f, salt = 11)
    drawGulls(st, pen, 2, 0.12f, 0.3f, salt = 9)
    drawPeaks(
        st, 0.07f, 0.57f, 0.5f, 0.07f, 0.15f,
        Color(0xFFB0C6E6).atNight(n, 0.75f), Color(0xFF98B0D8).atNight(n, 0.75f),
        Color(0xFFF1F6FF).atNight(n, 0.7f), Color(0xFFD2DEF2).atNight(n, 0.7f), 0.3f, 51,
    )
    // The fjord behind the park.
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF8ACBF0).atNight(n, 0.75f), Color(0xFF4A98DA).atNight(n, 0.7f)), startY = 0.565f * u, endY = 0.66f * u),
        Offset(0f, 0.565f * u), Size(st.w, 0.1f * u),
    )
    drawWaterPlane(st, pen, -9f, 99f, 0.57f, 0.6f, Color.White, 3, 0.1f, 0.14f, 0.5f * (1f - 0.5f * n))
    drawForestRow(st, 0.2f, 0.64f, 0.03f, 53, 0.06f, 0.03f, 0.05f, pen.farTrees(Color(0xFF5E8F74)).atNight(n, 0.72f), null, skip = 0.55f, ground = pen.farGround(Color(0xFF8DBF8B)).atNight(n, 0.72f))
    rollerCoaster(st, pen)
    drawPath(ridgePath(st, 0.5f, 0.77f, 0.006f, 55), pen.farGround(Color(0xFF93C96F)).atNight(n, 0.6f))
    farTents(st, pen)
    entranceArch(st, pen)

    val ts = tivoliStatic.of(u)
    val back = PlaceId.TIVOLI.back
    inScene(st) {
        drawPath(ts.pave, Brush.verticalGradient(listOf(pen.sandy(Color(0xFFC9BFB2)).atNight(n, 0.45f), pen.sandy(Color(0xFFE7DED2)).atNight(n, 0.45f)), startY = back * u, endY = FRONT_Y * u))
        drawPoints(ts.joints, PointMode.Lines, Color(0xFFB3A898).atNight(n, 0.45f), strokeWidth = pen.lw * 0.7f)
        drawPath(ts.verge, pen.ground(Color(0xFF7DBA5C)).atNight(n, 0.45f))
        drawPath(ts.paveEdge, Ink.line, alpha = 0.6f, style = pen.thin)
        for ((i, pts) in ts.confetti.withIndex()) drawPoints(pts, PointMode.Points, bulbColors[i].atNight(n, 0.35f), strokeWidth = 0.009f * u, cap = StrokeCap.Square)
    }
    bumperArena(st, pen, ts)
    lightPoles(st, pen)
    inScene(st) {
        drawPath(ts.strings, Ink.line, style = pen.thin)
        val glow = ramp((n - 0.2f) / 0.5f)
        for ((i, pts) in ts.bulbs.withIndex()) {
            val c = bulbColors[i]
            val tw = 0.75f + 0.25f * sin(t * 2.3f + i * 1.7f)
            if (glow > 0f) drawPoints(pts, PointMode.Points, c, strokeWidth = 0.05f * u, cap = StrokeCap.Round, alpha = 0.25f * glow * tw)
            drawPoints(pts, PointMode.Points, Ink.line, strokeWidth = 0.02f * u, cap = StrokeCap.Round)
            drawPoints(pts, PointMode.Points, if (glow > 0f) c.lighten(0.3f * glow) else c, strokeWidth = 0.015f * u, cap = StrokeCap.Round, alpha = 0.85f + 0.15f * tw)
        }
    }
    drawBase(st, pen, pen.sandy(Color(0xFFE7DED2)).atNight(n, 0.45f), Color(0xFF8E8173).atNight(n, 0.45f))
    drawBaseStones(st, pen, Color(0xFFB1A99E).atNight(n, 0.45f), 57)
}

/** A far roller coaster silhouette with a little train running along it. */
private fun DrawScope.rollerCoaster(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val p = 0.33f
    val span = st.span(p)
    val l0 = 0.5f * span
    val l1 = 0.95f * span
    if (st.px(l1, p) < 0f || st.px(l0, p) > st.w) return
    fun ty(f: Float): Float {
        val hill = 0.2f * exp(-((f - 0.14f) / 0.07f).let { it * it })
        return 0.7f - hill - 0.07f * (0.5f + 0.5f * sin(f * 19f + 1.2f)) * (1f - f * 0.5f)
    }
    val count = 40
    val xs = FloatArray(count)
    val ys = FloatArray(count)
    val supports = ArrayList<Offset>(40)
    for (i in 0 until count) {
        val f = i / (count - 1f)
        xs[i] = st.px(mix(l0, l1, f), p)
        ys[i] = ty(f) * u
        if (i % 2 == 0) {
            supports.add(Offset(xs[i], ys[i]))
            supports.add(Offset(xs[i], 0.74f * u))
        }
    }
    val col = Color(0xFF8FA3C6).atNight(n, 0.7f)
    drawPoints(supports, PointMode.Lines, col.lighten(0.25f), strokeWidth = pen.lw * 0.8f)
    val track = Path().apply { smoothRun(xs, ys, count, start = true) }
    drawPath(track, col, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
    val cars = Path()
    for (k in 0 until 3) {
        val f = wrap(pen.t * 0.04f + k * 0.012f, 1f)
        val i = (f * (count - 1)).toInt().coerceIn(0, count - 2)
        val fr = f * (count - 1) - i
        val cx = mix(xs[i], xs[i + 1], fr)
        val cy = mix(ys[i], ys[i + 1], fr)
        cars.addRoundRect(RoundRect(Rect(cx - 0.011f * u, cy - 0.014f * u, cx + 0.011f * u, cy), CornerRadius(0.004f * u)))
    }
    drawPath(cars, Color(0xFFE94F4F).atNight(n, 0.55f))
}

/** A row of striped tents and booths far back in the park; they glow at night. */
private fun DrawScope.farTents(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val p = 0.5f
    val spacing = 0.42f
    val l0 = st.cam * p - 0.3f
    val l1 = st.cam * p + st.vw + 0.3f
    val cols = listOf(Color(0xFFE94F4F), Color(0xFF3E7BD6), Color(0xFFFFB82E), Color(0xFF2FB57A), Color(0xFFB45CE0))
    val lit = ramp((n - 0.3f) / 0.4f)
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        if (hash01(i, 911) < 0.15f) continue
        val lx = i * spacing + (hash01(i, 912) - 0.5f) * 0.12f
        val bx = st.px(lx, p)
        val by = (ridgeY(lx, 0.77f, 0.006f, 55) + 0.002f) * u
        val s = (0.13f + 0.04f * hash01(i, 913)) * u
        val c = cols[(hash01(i, 914) * cols.size).toInt().coerceIn(0, cols.size - 1)].atNight(n, 0.55f)
        val round = hash01(i, 915) < 0.6f
        val body = Path()
        if (round) {
            body.poly(bx - 0.5f * s, by - 0.42f * s, bx + 0.5f * s, by - 0.42f * s, bx + 0.5f * s, by, bx - 0.5f * s, by)
            body.poly(bx - 0.62f * s, by - 0.4f * s, bx, by - 1.0f * s, bx + 0.62f * s, by - 0.4f * s)
        } else {
            body.poly(bx - 0.5f * s, by - 0.6f * s, bx + 0.5f * s, by - 0.6f * s, bx + 0.5f * s, by, bx - 0.5f * s, by)
        }
        drawPath(body, Color(0xFFFFF6EC).atNight(n, 0.55f))
        clipPath(body) {
            for (k in 0 until 5) drawRect(c, Offset(bx - 0.5f * s + k * 0.24f * s, by - 1.05f * s), Size(0.12f * s, 1.1f * s))
        }
        if (!round) {
            val awning = Path()
            for (k in 0 until 5) awning.addOval(Rect(bx - 0.55f * s + k * 0.22f * s, by - 0.64f * s, bx - 0.33f * s + k * 0.22f * s, by - 0.5f * s))
            drawPath(awning, c.darken(0.1f))
        }
        drawPath(body, Ink.line, alpha = 0.55f, style = pen.thin)
        val door = Rect(bx - 0.14f * s, by - 0.28f * s, bx + 0.14f * s, by)
        drawRect(lerp3(Color(0xFF4A3140).atNight(n, 0.4f), Color(0xFFFFD66B), lit), door.topLeft, door.size)
        if (lit > 0f) drawCircle(Color(0xFFFFD66B), 0.4f * s, door.center, alpha = 0.25f * lit)
        val top = if (round) by - 1.0f * s else by - 0.6f * s
        drawLine(Ink.line, Offset(bx, top), Offset(bx, top - 0.22f * s), strokeWidth = pen.lw * 0.7f, alpha = 0.7f)
        val flag = Path().apply { poly(bx, top - 0.22f * s, bx + 0.16f * s + sin(pen.t * 3f + i) * 0.02f * s, top - 0.18f * s, bx, top - 0.13f * s) }
        drawPath(flag, cols[(i + 2).mod(cols.size)].atNight(n, 0.5f))
    }
}

private fun lerp3(a: Color, b: Color, f: Float): Color = androidx.compose.ui.graphics.lerp(a, b, f)

/** The big entrance arch at the far left, studded with bulbs and crowned with a star. */
private fun DrawScope.entranceArch(st: Stage, pen: Pen) {
    if (!st.sees(-0.3f, 0.35f)) return
    val u = st.u
    val n = pen.night
    val back = PlaceId.TIVOLI.back
    val red = Color(0xFFE94F4F).atNight(n, 0.45f)
    val glow = ramp((n - 0.2f) / 0.5f)
    for (x in floatArrayOf(-0.16f, 0.2f)) {
        val r = st.rect(x - 0.03f, 0.2f, x + 0.03f, back)
        box3d(r, 0.04f * u, Color(0xFFFFF6EC).atNight(n, 0.45f), pen)
        clipPath(Path().apply { addRect(r) }) {
            var y = 0.22f
            while (y < back) {
                drawRect(red, Offset(r.left, y * u), Size(r.width, 0.035f * u))
                y += 0.07f
            }
        }
        drawRect(Ink.line, r.topLeft, r.size, style = pen.thin)
    }
    val arc = Rect(st.x(-0.16f), 0.04f * u, st.x(0.2f), 0.36f * u)
    drawArc(Ink.line, 180f, 180f, false, arc.topLeft, arc.size, style = Stroke(0.05f * u + pen.lw * 2f))
    drawArc(Color(0xFF3E7BD6).atNight(n, 0.4f), 180f, 180f, false, arc.topLeft, arc.size, style = Stroke(0.05f * u))
    val dots = ArrayList<Offset>(12)
    for (k in 0..10) {
        val a = Math.PI.toFloat() * (1f + k / 10f)
        dots.add(Offset(arc.center.x + kotlin.math.cos(a) * arc.width / 2f, arc.center.y + sin(a) * arc.height / 2f))
    }
    if (glow > 0f) drawPoints(dots, PointMode.Points, Color(0xFFFFE27A), strokeWidth = 0.045f * u, cap = StrokeCap.Round, alpha = 0.3f * glow)
    drawPoints(dots, PointMode.Points, Color(0xFFFFE27A), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
    val star = starPath(Offset(arc.center.x, 0.03f * u), 0.045f * u, 0.02f * u, sin(pen.t * 0.8f) * 8f)
    if (glow > 0f) drawCircle(Color(0xFFFFE27A), 0.08f * u, Offset(arc.center.x, 0.03f * u), alpha = 0.25f * glow)
    inked(star, Pal.sun, pen)
}

private fun DrawScope.lightPoles(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val back = PlaceId.TIVOLI.back
    for (x in floatArrayOf(1.22f, 2.47f)) {
        if (!st.sees(x - 0.05f, x + 0.05f)) continue
        inkedRound(st.rect(x - 0.008f, 0.09f, x + 0.008f, back + 0.004f), 0.004f * u, Color(0xFF6B7282).atNight(n, 0.45f), pen, shade = false)
        inkedCircle(st.o(x, 0.09f), 0.014f * u, Color(0xFF6B7282).atNight(n, 0.45f), pen, shade = false)
    }
}

/** The bumper-car arena: a rubber checker floor with a low padded fence and a striped canopy. */
private fun DrawScope.bumperArena(st: Stage, pen: Pen, ts: TivoliStatic) {
    if (!st.sees(ARENA_X0 - 0.1f, ARENA_X1 + 0.4f)) return
    val u = st.u
    val n = pen.night
    val back = 0.8f
    val sh = recede(back)
    inScene(st) {
        drawPath(ts.arena, Color(0xFF3D4966).atNight(n, 0.45f))
        drawPath(ts.arenaTiles, Color(0xFF34405A).atNight(n, 0.45f))
        drawPath(ts.arena, Ink.line, style = pen.thin)
    }
    val yellow = Color(0xFFFFC83D).atNight(n, 0.45f)
    // Canopy: poles at the back corners, a roof seen from a little above and a scalloped edge.
    val poles = floatArrayOf(ARENA_X0 + sh + 0.02f, (ARENA_X0 + ARENA_X1) / 2f + sh)
    for (x in poles) inkedRound(st.rect(x - 0.012f, 0.25f, x + 0.012f, back), 0.005f * u, Color(0xFFB9C0CC).atNight(n, 0.45f), pen, shade = false)
    val roof = Path().apply { poly(st.x(ARENA_X0 + sh - 0.06f), 0.16f * u, st.x(4.8f), 0.16f * u, st.x(4.8f - 0.08f), 0.22f * u, st.x(ARENA_X0 + sh - 0.14f), 0.22f * u) }
    drawPath(roof, Color(0xFFF7F3EC).atNight(n, 0.45f))
    drawPath(roof, Ink.line, style = pen.stroke)
    val scallops = Path()
    val red = Path()
    var k = 0
    var x = ARENA_X0 + sh - 0.14f
    while (x < 4.8f) {
        val r = Rect(st.x(x), 0.2f * u, st.x(x + 0.08f), 0.26f * u)
        (if (k % 2 == 0) red else scallops).addArc(r, 0f, 180f)
        x += 0.08f
        k++
    }
    drawPath(red, Color(0xFFE94F4F).atNight(n, 0.45f))
    drawPath(scallops, Color(0xFFFFF6EC).atNight(n, 0.45f))
    drawLine(Ink.line, st.o(ARENA_X0 + sh - 0.14f, 0.22f), st.o(4.8f, 0.22f), strokeWidth = pen.lw)
    // Low padded fence along the back and the left side of the arena.
    val backRail = Rect(st.x(ARENA_X0 + sh), (back - 0.03f) * u, st.x(ARENA_X1 + sh), back * u)
    inkedRound(backRail, 0.01f * u, yellow, pen, shade = false)
    val stripes = Path()
    var sx = ARENA_X0 + sh + 0.02f
    while (sx < ARENA_X1 + sh - 0.02f) {
        stripes.poly(st.x(sx), (back - 0.028f) * u, st.x(sx + 0.03f), (back - 0.028f) * u, st.x(sx + 0.015f), (back - 0.002f) * u, st.x(sx - 0.015f), (back - 0.002f) * u)
        sx += 0.09f
    }
    drawPath(stripes, Ink.line, alpha = 0.7f)
    capsule(st.o(ARENA_X0, FRONT_Y - 0.012f), st.o(ARENA_X0 + sh, back - 0.012f), 0.018f * u, yellow, pen)
}

internal fun DrawScope.tivoliFront(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    if (st.sees(ARENA_X0 - 0.05f, ARENA_X1 + 0.05f)) {
        val rail = Rect(st.x(ARENA_X0), (FRONT_Y - 0.022f) * u, st.x(ARENA_X1), (FRONT_Y + 0.004f) * u)
        inkedRound(rail, 0.01f * u, Color(0xFFFFC83D).atNight(n, 0.45f), pen, shade = false)
        val marks = ArrayList<Offset>(24)
        var x = ARENA_X0 + 0.03f
        while (x < ARENA_X1 - 0.02f) {
            marks.add(st.o(x, FRONT_Y - 0.02f))
            marks.add(st.o(x + 0.02f, FRONT_Y + 0.002f))
            x += 0.08f
        }
        drawPoints(marks, PointMode.Lines, Ink.line, strokeWidth = 0.012f * u, alpha = 0.75f)
    }
    // A few pieces of confetti twirling down.
    for (i in 0 until 7) {
        val ph = wrap(pen.t * (0.05f + 0.02f * hash01(i, 921)) + hash01(i, 922), 1f)
        val x = hash01(i, 923) * 4.4f + sin(ph * 12f + i) * 0.03f
        if (!st.sees(x, x)) continue
        val y = mix(SKY_TOP, FRONT_Y, ph)
        val c = st.o(x, y)
        val w = 0.012f * u * (0.3f + 0.7f * kotlin.math.abs(sin(ph * 20f + i)))
        drawRect(bulbColors[i % bulbColors.size].atNight(n, 0.35f), Offset(c.x - w / 2f, c.y - 0.005f * u), Size(w, 0.01f * u))
    }
}
