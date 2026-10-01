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
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.GardenLayout
import app.trollfoss.domain.Weather
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Hagen's pond: real water, 1.9 units wide, in the middle of the garden. The back layer draws the bank, the
 * receding surface with its waves and lily pads, the open face of the water, the reeds, the stones and the
 * little waterfall at the back. The front layer draws the see-through face of the water over whoever wades,
 * and the rim of stones at both ends. Helpers start with `gp`.
 */

private const val GP_X1 = GardenLayout.POND_X1
private const val GP_X2 = GardenLayout.POND_X2
private const val GP_LINE = GardenLayout.POND_LINE
private const val GP_BED = GardenLayout.POND_BED
private const val GP_FAR = 0.783f

/** Where the little waterfall pours into the pond. */
private const val GP_FALL_X = 5.95f

private class GpGeo(
    val soil: Path, val plane: Path, val body: Path, val bedStrip: Path,
    val backRocks: Path, val backRocksShade: Path, val backRocksLight: Path, val frontRocks: Path, val frontRocksShade: Path,
    val pads: Path, val padNotches: Path, val padFlowers: List<Offset>,
    val cascadeRocks: Path, val cascadeRocksShade: Path, val moss: Path, val pebbles: Path,
)

private val gpGeo = Memo { u -> gpBuild(u) }

private fun gpBuild(u: Float): GpGeo {
    val r = 0.1f
    val soil = Path().apply {
        moveTo((GP_X1 - 0.025f) * u, GardenLayoutFront * u)
        lineTo((GP_X1 - 0.025f) * u, (GP_FAR - 0.004f + r) * u)
        quadraticTo((GP_X1 - 0.025f) * u, (GP_FAR - 0.014f) * u, (GP_X1 - 0.025f + r) * u, (GP_FAR - 0.014f) * u)
        lineTo((GP_X2 + 0.025f - r) * u, (GP_FAR - 0.014f) * u)
        quadraticTo((GP_X2 + 0.025f) * u, (GP_FAR - 0.014f) * u, (GP_X2 + 0.025f) * u, (GP_FAR - 0.004f + r) * u)
        lineTo((GP_X2 + 0.025f) * u, GardenLayoutFront * u)
        close()
    }
    val plane = Path().apply {
        moveTo(GP_X1 * u, GP_LINE * u)
        lineTo(GP_X1 * u, (GP_FAR + r * 0.5f) * u)
        quadraticTo(GP_X1 * u, GP_FAR * u, (GP_X1 + r * 0.7f) * u, GP_FAR * u)
        lineTo((GP_X2 - r * 0.7f) * u, GP_FAR * u)
        quadraticTo(GP_X2 * u, GP_FAR * u, GP_X2 * u, (GP_FAR + r * 0.5f) * u)
        lineTo(GP_X2 * u, GP_LINE * u)
        close()
    }
    val body = Path().apply { addRect(Rect(GP_X1 * u, GP_LINE * u, GP_X2 * u, GP_BED * u)) }
    val bed = Path().apply { addRect(Rect(GP_X1 * u, GP_BED * u, GP_X2 * u, GardenLayoutFront * u)) }

    // Stones along the back and the two sides.
    val backRocks = Path()
    val backShade = Path()
    val backLight = Path()
    var i = 0
    var x = GP_X1 - 0.06f
    while (x < GP_X2 + 0.08f) {
        val h = hash01(i, 811)
        val rx = (0.04f + 0.03f * h)
        val ry = rx * (0.55f + 0.2f * hash01(i, 812))
        val cy = GP_FAR - 0.004f + 0.01f * hash01(i, 813) + (if (x < GP_X1 + 0.1f || x > GP_X2 - 0.1f) 0.04f else 0f)
        val rect = Rect((x - rx) * u, (cy - ry) * u, (x + rx) * u, (cy + ry) * u)
        backRocks.addOval(rect)
        backShade.addOval(Rect(rect.left + rect.width * 0.25f, rect.top + rect.height * 0.35f, rect.right, rect.bottom))
        backLight.addOval(Rect(rect.left + rect.width * 0.15f, rect.top + rect.height * 0.12f, rect.left + rect.width * 0.55f, rect.top + rect.height * 0.42f))
        x += 0.085f + 0.04f * hash01(i, 814)
        i++
    }
    val front = Path()
    val frontShade = Path()
    for ((k, cx) in floatArrayOf(GP_X1 - 0.04f, GP_X1 + 0.005f, GP_X1 - 0.07f, GP_X2 + 0.04f, GP_X2 - 0.005f, GP_X2 + 0.07f).withIndex()) {
        val cy = floatArrayOf(0.945f, 0.9f, 0.9f, 0.945f, 0.9f, 0.9f)[k]
        val rx = if (k % 3 == 0) 0.05f else 0.038f
        val ry = rx * 0.62f
        val rect = Rect((cx - rx) * u, (cy - ry) * u, (cx + rx) * u, (cy + ry) * u)
        front.addOval(rect)
        frontShade.addOval(Rect(rect.left + rect.width * 0.25f, rect.top + rect.height * 0.35f, rect.right, rect.bottom))
    }

    // Lily pads on the receding surface (the ones with frogs on are fixtures); some carry a flower.
    val pads = Path()
    val notches = Path()
    val flowers = ArrayList<Offset>()
    val padAt = arrayOf(floatArrayOf(4.88f, 0.806f, 0.04f), floatArrayOf(5.62f, 0.802f, 0.045f), floatArrayOf(5.78f, 0.812f, 0.03f), floatArrayOf(4.58f, 0.796f, 0.032f), floatArrayOf(6.08f, 0.804f, 0.036f))
    for ((k, p) in padAt.withIndex()) {
        pads.floorDisc(u, 0f, p[0], p[1], p[2], p[2] * 0.62f, 16)
        notches.moveTo(p[0] * u, p[1] * u)
        notches.lineTo((p[0] + p[2] * 0.9f) * u, (p[1] - p[2] * 0.2f) * u)
        if (k % 2 == 0) flowers.add(Offset((p[0] - p[2] * 0.2f) * u, (p[1] - 0.01f) * u))
    }

    // The little waterfall's rocks: a heap of three layers at the back.
    val cr = Path()
    val crs = Path()
    val moss = Path()
    val rockAt = arrayOf(
        floatArrayOf(5.8f, 0.77f, 0.075f, 0.045f), floatArrayOf(6.04f, 0.77f, 0.08f, 0.05f), floatArrayOf(5.92f, 0.735f, 0.08f, 0.045f),
        floatArrayOf(5.82f, 0.7f, 0.06f, 0.04f), floatArrayOf(6.02f, 0.7f, 0.06f, 0.04f), floatArrayOf(5.93f, 0.665f, 0.065f, 0.04f),
    )
    for (rk in rockAt) {
        val rect = Rect((rk[0] - rk[2]) * u, (rk[1] - rk[3]) * u, (rk[0] + rk[2]) * u, (rk[1] + rk[3]) * u)
        cr.addOval(rect)
        crs.addOval(Rect(rect.left + rect.width * 0.3f, rect.top + rect.height * 0.4f, rect.right, rect.bottom))
        moss.addOval(Rect(rect.left + rect.width * 0.1f, rect.top, rect.left + rect.width * 0.65f, rect.top + rect.height * 0.35f))
    }
    val pebbles = Path()
    for (k in 0 until 10) {
        val px = GP_X1 + 0.1f + (GP_X2 - GP_X1 - 0.2f) * hash01(k, 821)
        pebbles.addOval(Rect((px - 0.012f) * u, (GP_BED - 0.005f - 0.01f * hash01(k, 822)) * u, (px + 0.012f) * u, (GP_BED + 0.006f) * u))
    }
    return GpGeo(soil, plane, body, bed, backRocks, backShade, backLight, front, frontShade, pads, notches, flowers, cr, crs, moss, pebbles)
}

private const val GardenLayoutFront = 0.97f

private fun gpWater(pen: Pen): Triple<Color, Color, Color> {
    val rain = pen.weather == Weather.RAIN
    val light = (if (rain) Color(0xFF8FC2D6) else Color(0xFF8BDDEB)).ga(pen, 0.55f, 0.5f)
    val mid = (if (rain) Color(0xFF4F93AB) else Color(0xFF4DB8D4)).ga(pen, 0.55f, 0.45f)
    val deep = (if (rain) Color(0xFF28627A) else Color(0xFF1F6D8B)).ga(pen, 0.5f, 0.4f)
    return Triple(light, mid, deep)
}

/** The pond behind everything: bank, surface, face of the water, stones, reeds, lilies and the waterfall. */
internal fun DrawScope.gardenPondBack(st: Stage, pen: Pen) {
    if (!st.sees(GP_X1 - 0.3f, GP_X2 + 0.3f)) return
    val u = st.u
    val t = pen.t
    val n = pen.night
    val g = gpGeo.of(u)
    val (light, mid, deep) = gpWater(pen)
    inScene(st) {
        // The dark soil of the bank around the water.
        drawPath(g.soil, Color(0xFF6B5A3F).ga(pen, 0.5f))
        drawPath(g.soil, Ink.line, alpha = 0.55f, style = pen.thin)
        // The receding surface, light at the back where it mirrors the sky, then the face of the water.
        drawPath(g.plane, Brush.verticalGradient(0f to light, 1f to mid, startY = GP_FAR * u, endY = GP_LINE * u))
        drawPath(g.body, Brush.verticalGradient(0f to mid, 1f to deep, startY = GP_LINE * u, endY = GP_BED * u))
        drawPath(g.bedStrip, Color(0xFF7A6A52).ga(pen, 0.5f))
        // Three koi gliding to and fro under the surface, turning at the ends.
        for (k in 0 until 3) {
            val ph = wrap(t / (24f + 7f * k) + hash01(k, 861), 1f)
            val fwd = ph < 0.5f
            val q = ramp(if (fwd) ph * 2f else (1f - ph) * 2f)
            val fx = GP_X1 + 0.16f + (GP_X2 - GP_X1 - 0.32f) * q
            val fy = 0.865f + 0.038f * k + 0.008f * sin(t * 0.8f + k * 1.9f)
            val dir = if (fwd) 1f else -1f
            val wag = sin(t * 5f + k * 2f) * 0.008f
            val body = when (k) {
                0 -> Color(0xFFFF8A3D)
                1 -> Color(0xFFFFD447)
                else -> Color(0xFFF2E6D8)
            }.ga(pen, 0.5f)
            val c = Offset(fx * u, fy * u)
            val len = 0.055f * u
            val fish = Path().apply {
                moveTo(c.x + dir * len, c.y)
                quadraticTo(c.x + dir * len * 0.2f, c.y - 0.022f * u, c.x - dir * len * 0.7f, c.y + wag * u * 0.4f)
                quadraticTo(c.x + dir * len * 0.2f, c.y + 0.022f * u, c.x + dir * len, c.y)
                close()
            }
            val tail = Path().apply {
                moveTo(c.x - dir * len * 0.65f, c.y + wag * u * 0.4f)
                lineTo(c.x - dir * len * 1.15f, c.y - 0.016f * u + wag * u)
                lineTo(c.x - dir * len * 1.05f, c.y + wag * u)
                lineTo(c.x - dir * len * 1.15f, c.y + 0.016f * u + wag * u)
                close()
            }
            drawPath(tail, body, alpha = 0.8f)
            drawPath(fish, body, alpha = 0.9f)
            if (k != 1) drawCircle(if (k == 0) Color(0xFFFFFFFF) else Color(0xFFFF8A3D), 0.011f * u, Offset(c.x - dir * len * 0.1f, c.y - 0.004f * u), alpha = 0.8f)
            drawCircle(Ink.line, 0.0032f * u, Offset(c.x + dir * len * 0.62f, c.y - 0.004f * u), alpha = 0.8f)
            drawPath(fish, Ink.line, alpha = 0.4f, style = pen.thin)
        }
        drawPath(g.pebbles, Color(0xFFA8A29A).ga(pen, 0.5f), alpha = 0.7f)
        drawLine(Ink.line, Offset(GP_X1 * u, GP_BED * u), Offset(GP_X2 * u, GP_BED * u), strokeWidth = pen.lw * 0.8f, alpha = 0.45f)
        // A reflection of the sky in the surface, a soft light band.
        drawRect(
            Brush.verticalGradient(0f to Color.White.copy(alpha = 0.28f * (1f - n)), 1f to Color.White.copy(alpha = 0f), startY = GP_FAR * u, endY = (GP_FAR + 0.02f) * u),
            Offset(GP_X1 * u, GP_FAR * u), Size((GP_X2 - GP_X1) * u, 0.02f * u),
        )
        drawPath(g.plane, Ink.line, alpha = 0.5f, style = pen.thin)
        if (pen.weather == Weather.SNOW) {
            // A thin skin of ice and snow at the edges.
            drawPath(g.plane, Color(0xFFE6F2FF), alpha = 0.35f)
        }
    }
    drawWaterPlane(st, pen, GP_X1, GP_X2, 0.79f, 0.817f, Color.White, 3, 1f, 1f, 0.55f)
    inScene(st) {
        // Sun glints on the surface by day, moon glints at night.
        val glints = ArrayList<Offset>(14)
        for (k in 0 until 14) {
            val a = max(0f, sin(t * 1.9f + k * 2.3f))
            if (a < 0.55f) continue
            val gx = GP_X1 + 0.1f + (GP_X2 - GP_X1 - 0.2f) * hash01(k, 831)
            val gy = GP_FAR + 0.008f + 0.025f * hash01(k, 832)
            glints.add(Offset(gx * u, gy * u))
            glints.add(Offset((gx + 0.018f) * u, gy * u))
        }
        drawPoints(glints, PointMode.Lines, Color.White, strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round, alpha = 0.7f * (1f - 0.35f * n))
        // Lily pads with flowers.
        drawPath(g.pads, Color(0xFF4FA35A).ga(pen, 0.5f))
        drawPath(g.pads, Ink.line, alpha = 0.7f, style = pen.thin)
        drawPath(g.padNotches, Ink.line, alpha = 0.5f, style = pen.thin)
        for ((k, f) in g.padFlowers.withIndex()) {
            val bob = sin(t * 0.9f + k * 1.7f) * 0.002f * u
            val c = Offset(f.x, f.y + bob)
            drawCircle(Ink.line, 0.0155f * u, c)
            drawCircle(if (k % 2 == 0) Color(0xFFFFFFFF).ga(pen, 0.3f) else Color(0xFFFF9EC4).ga(pen, 0.3f), 0.0125f * u, c)
            drawCircle(Color(0xFFFFD447), 0.0045f * u, c)
        }
        // Rings spreading on the surface now and then, and in rain everywhere.
        val rain = pen.weather == Weather.RAIN
        val rings = if (rain) 12 else 3
        for (k in 0 until rings) {
            val ph = wrap(t * (if (rain) 0.9f else 0.25f) + hash01(k, 833), 1f)
            val rx = GP_X1 + 0.15f + (GP_X2 - GP_X1 - 0.3f) * hash01(k, 834)
            val ry = GP_FAR + 0.012f + 0.03f * hash01(k, 835)
            val rr = (0.01f + 0.045f * ph) * u
            drawOval(Color.White, Offset(rx * u - rr, ry * u - rr * 0.32f), Size(rr * 2f, rr * 0.64f), alpha = 0.6f * (1f - ph), style = pen.thin)
        }
        // Stones around the back of the pond.
        val stone = Color(0xFFB4B2BE).ga(pen, 0.5f).gaSnow(pen, 0.5f)
        drawPath(g.backRocks, stone)
        drawPath(g.backRocksShade, stone.darken(0.22f), alpha = 0.9f)
        drawPath(g.backRocksLight, Color.White, alpha = 0.3f)
        drawPath(g.backRocks, Ink.line, style = pen.thin)
        // The waterfall rocks and the water running down them.
        val rock = Color(0xFF9A98A8).ga(pen, 0.5f).gaSnow(pen, 0.5f)
        drawPath(g.cascadeRocks, rock)
        drawPath(g.cascadeRocksShade, rock.darken(0.25f))
        drawPath(g.moss, Color(0xFF6FAE5A).ga(pen, 0.5f).gaSnow(pen, 0.6f), alpha = 0.9f)
        drawPath(g.cascadeRocks, Ink.line, style = pen.thin)
        val fall = Path().apply {
            moveTo((GP_FALL_X - 0.025f) * u, 0.655f * u)
            lineTo((GP_FALL_X + 0.025f) * u, 0.655f * u)
            quadraticTo((GP_FALL_X + 0.04f) * u, 0.72f * u, (GP_FALL_X + 0.03f) * u, 0.79f * u)
            lineTo((GP_FALL_X - 0.03f) * u, 0.79f * u)
            quadraticTo((GP_FALL_X - 0.04f) * u, 0.72f * u, (GP_FALL_X - 0.025f) * u, 0.655f * u)
            close()
        }
        drawPath(fall, Brush.verticalGradient(0f to light, 1f to Color(0xFFEAFBFF), startY = 0.655f * u, endY = 0.79f * u), alpha = 0.92f)
        clipPath(fall) {
            val lines = ArrayList<Offset>(20)
            for (k in 0 until 8) {
                val lane = GP_FALL_X - 0.024f + 0.048f * (k + 0.5f) / 8f
                val y0 = 0.655f + wrap(t * (0.5f + 0.2f * hash01(k, 841)) + hash01(k, 842), 1f) * 0.14f
                lines.add(Offset(lane * u, y0 * u))
                lines.add(Offset(lane * u, min(0.79f, y0 + 0.04f + 0.03f * hash01(k, 843)) * u))
            }
            drawPoints(lines, PointMode.Lines, Color.White, strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round, alpha = 0.85f)
        }
        drawPath(fall, Ink.line, alpha = 0.55f, style = pen.thin)
        // Foam and mist at the foot.
        for (k in 0 until 5) {
            val ph = wrap(t * 0.5f + k / 5f, 1f)
            drawCircle(Color.White, (0.012f + 0.01f * (1f - ph)) * u, Offset((GP_FALL_X + (k - 2) * 0.016f) * u, (0.792f + 0.003f * sin(t * 4f + k)) * u), alpha = 0.85f)
            drawCircle(Color.White, (0.02f + ph * 0.03f) * u, Offset((GP_FALL_X + (k - 2) * 0.02f + ph * 0.02f) * u, (0.775f - ph * 0.06f) * u), alpha = 0.22f * (1f - ph))
        }
        // Reeds and cattails at the two back corners, swaying.
        for ((c, bx) in floatArrayOf(GP_X1 + 0.05f, GP_X1 + 0.14f, GP_X2 - 0.12f, GP_X2 - 0.04f).withIndex()) {
            val by = GP_FAR + 0.012f + 0.006f * (c % 2)
            for (s in 0 until 4) {
                val sway = sin(t * 1.1f + c * 1.7f + s * 0.8f) * 0.007f
                val tipH = 0.11f + 0.05f * hash01(c * 5 + s, 851)
                val tx = bx + (s - 1.5f) * 0.012f + sway
                val ty = by - tipH
                val stem = Path().apply {
                    moveTo((bx + (s - 1.5f) * 0.01f) * u, by * u)
                    quadraticTo((bx + (s - 1.5f) * 0.014f + sway * 0.4f) * u, (by - tipH * 0.5f) * u, tx * u, ty * u)
                }
                drawPath(stem, Ink.line, style = Stroke(pen.lw * 2.2f, cap = StrokeCap.Round))
                drawPath(stem, Color(0xFF4F9A48).ga(pen, 0.5f), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
                if (s % 2 == 0) {
                    drawLine(Ink.line, Offset(tx * u, (ty - 0.002f) * u), Offset((tx + sway * 0.3f) * u, (ty - 0.045f) * u), strokeWidth = 0.014f * u, cap = StrokeCap.Round)
                    drawLine(Color(0xFF7A4B2E).ga(pen, 0.5f), Offset(tx * u, (ty - 0.004f) * u), Offset((tx + sway * 0.3f) * u, (ty - 0.043f) * u), strokeWidth = 0.009f * u, cap = StrokeCap.Round)
                }
            }
        }
    }
}

/** The front of the pond: the see-through face of the water over whoever wades in it, and the stones at the ends. */
internal fun DrawScope.gardenPondFront(st: Stage, pen: Pen) {
    if (!st.sees(GP_X1 - 0.3f, GP_X2 + 0.3f)) return
    val u = st.u
    val g = gpGeo.of(u)
    val (light, _, deep) = gpWater(pen)
    drawWaterFront(st, pen, GP_X1, GP_X2, GP_LINE, GP_BED, light, deep)
    inScene(st) {
        val stone = Color(0xFFB4B2BE).ga(pen, 0.5f).gaSnow(pen, 0.5f)
        drawPath(g.frontRocks, stone)
        drawPath(g.frontRocksShade, stone.darken(0.22f), alpha = 0.9f)
        drawPath(g.frontRocks, Ink.line, style = pen.thin)
    }
    // After the sun shines through the rain: a rainbow shimmer on the water.
    if (pen.rainbow > 0.05f) {
        drawRect(
            Brush.horizontalGradient(
                0f to Color(0x00FFFFFF), 0.3f to Color(0x30FF9F43), 0.5f to Color(0x306BCB77), 0.7f to Color(0x304D96FF), 1f to Color(0x00FFFFFF),
                startX = st.x(GP_X1), endX = st.x(GP_X2),
            ),
            Offset(st.x(GP_X1), GP_LINE * u), Size((GP_X2 - GP_X1) * u, (GP_BED - GP_LINE) * u), alpha = pen.rainbow, blendMode = BlendMode.Screen,
        )
    }
}
