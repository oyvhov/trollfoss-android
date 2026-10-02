package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * The doctor's office, the stage and the sea floor: 19 fixtures in oblique 3D («skrå-3D»), drawn with the
 * origin at the bottom centre of each fixture's front face. Depth recedes up and to the right ([Oblique]).
 * Every helper here is private and starts with `rm`, so it cannot clash with the other fixture files.
 *
 * The engine squashes the back layer on a tap; the art adds its own small reactions on top (a drum face
 * that shouts, a cymbal that wobbles, a woofer that pumps) through [rmAnim], which is 0 when Android has
 * turned animations off.
 */

/**
 * Draws the back layer of the doctor's, stage and sea-floor fixtures. Returns false for any other type,
 * so the caller can fall through to its own art. Floor fixtures draw their own soft shadow.
 */
@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawRoomsBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        FixtureType.HEIGHT_CHART -> rmHeightChart(f, u, pen)
        FixtureType.XRAY -> rmXrayBack(f, u, pen)
        FixtureType.EXAM_BED -> rmExamBed(f, u, pen)
        FixtureType.MEDICINE_CABINET -> rmMedicineCabinet(f, u, pen)
        FixtureType.DOCTOR_DESK -> rmDoctorDesk(f, u, pen)
        FixtureType.EYE_CHART -> rmEyeChart(f, u, pen)
        FixtureType.STAGE_PLATFORM -> rmStage(f, u, pen)
        FixtureType.DRUM_KIT -> rmDrumsBack(f, u, pen)
        FixtureType.MIC_STAND -> rmMicStand(f, u, pen)
        FixtureType.XYLOPHONE -> rmXylophone(f, u, pen)
        FixtureType.SPEAKER -> rmSpeaker(f, u, pen)
        FixtureType.DISCO_BALL -> rmDiscoBall(f, u, pen)
        FixtureType.SMOKE_MACHINE -> rmSmokeMachine(f, u, pen)
        FixtureType.SHIPWRECK -> rmShipwreck(f, u, pen)
        FixtureType.KELP -> rmKelp(f, u, pen)
        FixtureType.GIANT_CLAM -> rmClam(f, u, pen)
        FixtureType.CORAL -> rmCoral(f, u, pen)
        FixtureType.SUBMARINE -> rmSubBack(f, u, pen)
        FixtureType.OCTOPUS -> rmOctopus(f, u, pen)
        else -> return false
    }
    return true
}

/**
 * Draws the front layer (over riders and contents) of the same fixtures: the X-ray screen, the bass drum
 * and snare, and the submarine's near hull. Returns true for all 19 types, drawing nothing for those
 * without a front, and false for any other type.
 */
fun DrawScope.drawRoomsFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.XRAY -> rmXrayFront(f, u, pen)
        FixtureType.DRUM_KIT -> rmDrumsFront(f, u, pen)
        FixtureType.SUBMARINE -> rmSubFront(f, u, pen)
        FixtureType.HEIGHT_CHART, FixtureType.EXAM_BED, FixtureType.MEDICINE_CABINET, FixtureType.DOCTOR_DESK,
        FixtureType.EYE_CHART, FixtureType.STAGE_PLATFORM, FixtureType.MIC_STAND, FixtureType.XYLOPHONE,
        FixtureType.SPEAKER, FixtureType.DISCO_BALL, FixtureType.SMOKE_MACHINE, FixtureType.SHIPWRECK,
        FixtureType.KELP, FixtureType.GIANT_CLAM, FixtureType.CORAL, FixtureType.OCTOPUS -> Unit
        else -> return false
    }
    return true
}

// ============================================================================================== kit

private const val ODX = Oblique.DX
private const val ODY = Oblique.DY
private const val RM_TAU = 6.2831855f
private const val RM_PI = 3.1415927f
private const val RM_KAPPA = 0.5523f
private val RM_SIDES = floatArrayOf(-1f, 1f)

/** Colours for these rooms: clinic white and mint, stage plum and brass, sea-floor wood and sand. */
private object RoomsInk {
    val porcelain = Color(0xFFF3F6F9)
    val paper = Color(0xFFFBF9F4)
    val steel = Color(0xFFBAC4D4)
    val chrome = Color(0xFFDDE3EC)
    val charcoal = Color(0xFF34313F)
    val rubber = Color(0xFF26232E)
    val mint = Color(0xFF6CC7B0)
    val teal = Color(0xFF2EC4B6)
    val pharmacy = Color(0xFF2FB36B)
    val red = Color(0xFFE8413C)
    val coral = Color(0xFFFF7A6B)
    val sun = Color(0xFFFFC83D)
    val gold = Color(0xFFE6B545)
    val oak = Color(0xFFE2BE8A)
    val oakDark = Color(0xFFC49A62)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)
    val fjord = Color(0xFF2F6FB8)
    val sky = Color(0xFF5AA9E6)
    val green = Color(0xFF7CFFB2)
    val pink = Color(0xFFFF6FA8)
    val warm = Color(0xFFFFE08A)
    val sand = Color(0xFFEBD5A0)
    val sandShade = Color(0xFFD2B77E)
}

/** Screen point in pixels of a 3D point: [x] across, [y] height (up is negative), [z] depth, in units. */
private fun rp(u: Float, x: Float, y: Float, z: Float = 0f): Offset = Offset((x + ODX * z) * u, (y + ODY * z) * u)

private fun rmQuad(a: Offset, b: Offset, c: Offset, d: Offset): Path = Path().apply {
    moveTo(a.x, a.y)
    lineTo(b.x, b.y)
    lineTo(c.x, c.y)
    lineTo(d.x, d.y)
    close()
}

/** A quadrilateral with its corners rounded by [rad] pixels, so box faces meet rounded fronts cleanly. */
private fun rmRQuad(a: Offset, b: Offset, c: Offset, d: Offset, rad: Float): Path {
    if (rad <= 0f) return rmQuad(a, b, c, d)
    val xs = floatArrayOf(a.x, b.x, c.x, d.x)
    val ys = floatArrayOf(a.y, b.y, c.y, d.y)
    val path = Path()
    for (i in 0..3) {
        val px = xs[(i + 3) % 4]
        val py = ys[(i + 3) % 4]
        val cx = xs[i]
        val cy = ys[i]
        val nx = xs[(i + 1) % 4]
        val ny = ys[(i + 1) % 4]
        val kp = min(rad / max(0.001f, sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy))), 0.5f)
        val kn = min(rad / max(0.001f, sqrt((nx - cx) * (nx - cx) + (ny - cy) * (ny - cy))), 0.5f)
        val ax = cx + (px - cx) * kp
        val ay = cy + (py - cy) * kp
        if (i == 0) path.moveTo(ax, ay) else path.lineTo(ax, ay)
        path.quadraticTo(cx, cy, cx + (nx - cx) * kn, cy + (ny - cy) * kn)
    }
    path.close()
    return path
}

private fun DrawScope.rmFace(path: Path, color: Color, pen: Pen, stroke: Stroke = pen.stroke) {
    drawPath(path, color)
    drawPath(path, Ink.line, style = stroke)
}

/**
 * A box in units: the front face (l, t)–(r, b) sits [z] units back and the box is [d] deep. Lit top,
 * shaded right side, cel-shaded front with corners rounded by [rad] units.
 */
private fun DrawScope.rmBox(
    u: Float, l: Float, t: Float, r: Float, b: Float, d: Float, color: Color, pen: Pen,
    rad: Float = 0f, z: Float = 0f, top: Color = color.lighten(0.18f), side: Color = color.darken(0.22f), shade: Boolean = true,
) {
    val rr = rad * u * 0.7f
    rmFace(rmRQuad(rp(u, r, t, z), rp(u, r, t, z + d), rp(u, r, b, z + d), rp(u, r, b, z), rr), side, pen)
    rmFace(rmRQuad(rp(u, l, t, z), rp(u, l, t, z + d), rp(u, r, t, z + d), rp(u, r, t, z), rr), top, pen)
    inkedRound(Rect(rp(u, l, t, z), rp(u, r, b, z)), rad * u, color, pen, shade)
}

/** A flat horizontal circle in oblique 3D, [rx] across and [rz] deep, centred on the pixel point (cx, cy). */
private fun rmDisc(cx: Float, cy: Float, rx: Float, rz: Float = rx): Path {
    val kx = RM_KAPPA * rx
    val kz = RM_KAPPA * rz
    fun mx(x: Float, z: Float) = cx + x + ODX * z
    fun my(z: Float) = cy + ODY * z
    return Path().apply {
        moveTo(mx(rx, 0f), my(0f))
        cubicTo(mx(rx, kz), my(kz), mx(kx, rz), my(rz), mx(0f, rz), my(rz))
        cubicTo(mx(-kx, rz), my(rz), mx(-rx, kz), my(kz), mx(-rx, 0f), my(0f))
        cubicTo(mx(-rx, -kz), my(-kz), mx(-kx, -rz), my(-rz), mx(0f, -rz), my(-rz))
        cubicTo(mx(kx, -rz), my(-rz), mx(rx, -kz), my(-kz), mx(rx, 0f), my(0f))
        close()
    }
}

/**
 * An upright cylinder in pixels: bottom disc centred on (cx, yb), top disc on (cx, yt), radius [r] across
 * and [rz] deep, the side shaded from light (left) to dark (right).
 */
private fun DrawScope.rmCyl(
    cx: Float, yb: Float, yt: Float, r: Float, color: Color, pen: Pen,
    top: Color = color.lighten(0.2f), rz: Float = r, cap: Boolean = true, rt: Float = r,
) {
    val zt = rz * rt / r
    val ab = sqrt(r * r + ODX * rz * ODX * rz)
    val ob = ODY * ODX * rz * rz / ab
    val at = sqrt(rt * rt + ODX * zt * ODX * zt)
    val ot = ODY * ODX * zt * zt / at
    rmFace(rmDisc(cx, yb, r, rz), color.darken(0.15f), pen)
    val body = Path().apply {
        moveTo(cx - at, yt - ot)
        lineTo(cx + at, yt + ot)
        lineTo(cx + ab, yb + ob)
        lineTo(cx - ab, yb - ob)
        close()
    }
    drawPath(body, Brush.horizontalGradient(0f to color.lighten(0.16f), 0.55f to color, 1f to color.darken(0.26f), startX = cx - ab, endX = cx + ab))
    inkLine(Offset(cx - at, yt - ot), Offset(cx - ab, yb - ob), pen)
    inkLine(Offset(cx + at, yt + ot), Offset(cx + ab, yb + ob), pen)
    if (cap) rmFace(rmDisc(cx, yt, rt, zt), top, pen)
}

/** The shell of a round thing facing us (a drum, a roll): its back rim and the band between. */
private fun DrawScope.rmShell(c: Offset, r: Float, depth: Float, side: Color, pen: Pen) {
    val vx = ODX * depth
    val vy = ODY * depth
    val len = sqrt(vx * vx + vy * vy)
    val nx = -vy / len * r
    val ny = vx / len * r
    val back = Offset(c.x + vx, c.y + vy)
    drawCircle(side.darken(0.1f), r, back)
    drawCircle(Ink.line, r, back, style = pen.stroke)
    drawPath(rmQuad(Offset(c.x + nx, c.y + ny), Offset(back.x + nx, back.y + ny), Offset(back.x - nx, back.y - ny), Offset(c.x - nx, c.y - ny)), side)
    inkLine(Offset(c.x + nx, c.y + ny), Offset(back.x + nx, back.y + ny), pen)
    inkLine(Offset(c.x - nx, c.y - ny), Offset(back.x - nx, back.y - ny), pen)
}

/** Oblique depth for a flat shape: copies of [front] step back [depth] pixels, inked round the outside only. */
private fun DrawScope.rmExtrude(front: Path, depth: Float, band: Color, pen: Pen) {
    val dx = ODX * depth
    val dy = ODY * depth
    val steps = (sqrt(dx * dx + dy * dy) / (pen.lw * 0.8f)).toInt().coerceIn(3, 10)
    val rim = Stroke(pen.lw * 2f, join = StrokeJoin.Round)
    for (i in steps downTo 0) translate(dx * i / steps, dy * i / steps) { drawPath(front, Ink.line, style = rim) }
    for (i in steps downTo 1) translate(dx * i / steps, dy * i / steps) { drawPath(front, band) }
}

/** A tube or stick with an ink rim and crisp two-tone shading. */
private fun DrawScope.rmTube(a: Offset, b: Offset, w: Float, color: Color, pen: Pen) {
    drawLine(Ink.line, a, b, strokeWidth = w + pen.lw * 2f, cap = StrokeCap.Round)
    drawLine(color.shadow(), a, b, strokeWidth = w, cap = StrokeCap.Round)
    drawLine(color, Offset(a.x - w * 0.12f, a.y - w * 0.08f), Offset(b.x - w * 0.12f, b.y - w * 0.08f), strokeWidth = w * 0.62f, cap = StrokeCap.Round)
}

private fun DrawScope.rmDot(c: Offset, r: Float, color: Color, pen: Pen) {
    drawCircle(color, r, c)
    drawCircle(Ink.line, r, c, style = pen.thin)
}

/** A soft round glow; a gradient, not a blur. */
private fun DrawScope.rmGlow(c: Offset, r: Float, color: Color, alpha: Float) {
    val a = alpha.coerceIn(0f, 1f)
    if (a <= 0.01f || r <= 0f) return
    drawCircle(
        safeRadialGradient(0f to color.copy(alpha = a), 0.45f to color.copy(alpha = a * 0.4f), 1f to color.copy(alpha = 0f), center = c, radius = r),
        r,
        c,
    )
}

/** The soft shadow under a floor fixture [w] wide and [d] deep (units). */
private fun DrawScope.rmShadow(u: Float, w: Float, d: Float, x: Float = 0f, alpha: Float = 1f) {
    val dx = ODX * d * u
    val dy = ODY * d * u
    drawOval(Ink.shadow, Offset((x - w / 2f + 0.01f) * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u), alpha = alpha)
}

/** A wall fixture's shadow on the wall, a little down and to the right. */
private fun DrawScope.rmWallShadow(r: Rect, u: Float, rad: Float) {
    drawRoundRect(Ink.shadow, Offset(r.left + 0.008f * u, r.top + 0.012f * u), r.size, CornerRadius(rad, rad))
}

/** The tap bounce, 1 → 0; 0 when animations are off. */
private fun rmAnim(f: Fixture, pen: Pen): Float = if (pen.t == 0f) 0f else f.anim.coerceIn(0f, 1f)

private fun rmHash(i: Int, salt: Int = 0): Float {
    var x = i * 0x27D4EB2D + salt * 0x165667B1 + 0x5BD1E995
    x = x xor (x ushr 15)
    x *= 0x2C1B3C6D
    x = x xor (x ushr 12)
    x *= 0x297A2D39
    x = x xor (x ushr 15)
    return (x and 0xFFFFFF) / 16777216f
}

private fun rmFrac(x: Float): Float = x - floor(x)

/** A heart [s] wide centred on (cx, cy), point down. */
private fun rmHeart(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + 0.42f * s)
    cubicTo(cx - 0.1f * s, cy + 0.3f * s, cx - 0.5f * s, cy + 0.1f * s, cx - 0.5f * s, cy - 0.15f * s)
    cubicTo(cx - 0.5f * s, cy - 0.42f * s, cx - 0.12f * s, cy - 0.5f * s, cx, cy - 0.25f * s)
    cubicTo(cx + 0.12f * s, cy - 0.5f * s, cx + 0.5f * s, cy - 0.42f * s, cx + 0.5f * s, cy - 0.15f * s)
    cubicTo(cx + 0.5f * s, cy + 0.1f * s, cx + 0.1f * s, cy + 0.3f * s, cx, cy + 0.42f * s)
    close()
}

/** A leaf or lens from [a] to [b], [bulge] pixels wide on each side of the middle. */
private fun rmLens(a: Offset, b: Offset, bulge: Float): Path {
    val mx = (a.x + b.x) / 2
    val my = (a.y + b.y) / 2
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = max(0.001f, sqrt(dx * dx + dy * dy))
    val nx = -dy / len * bulge * 2f
    val ny = dx / len * bulge * 2f
    return Path().apply {
        moveTo(a.x, a.y)
        quadraticTo(mx + nx, my + ny, b.x, b.y)
        quadraticTo(mx - nx, my - ny, a.x, a.y)
        close()
    }
}

/** A plus with rounded ends, [s] across, centred on [c]: the pharmacy sign, never a red cross. */
private fun rmPlus(c: Offset, s: Float): Path {
    val a = s * 0.5f
    val b = s * 0.17f
    return Path().apply {
        moveTo(c.x - b, c.y - a)
        lineTo(c.x + b, c.y - a)
        lineTo(c.x + b, c.y - b)
        lineTo(c.x + a, c.y - b)
        lineTo(c.x + a, c.y + b)
        lineTo(c.x + b, c.y + b)
        lineTo(c.x + b, c.y + a)
        lineTo(c.x - b, c.y + a)
        lineTo(c.x - b, c.y + b)
        lineTo(c.x - a, c.y + b)
        lineTo(c.x - a, c.y - b)
        lineTo(c.x - b, c.y - b)
        close()
    }
}

/** A little musical note, for the stage. */
private fun DrawScope.rmNote(c: Offset, s: Float, color: Color, alpha: Float) {
    if (alpha <= 0.01f) return
    val col = color.copy(alpha = alpha)
    drawOval(Ink.line.copy(alpha = alpha), Offset(c.x - s * 0.62f, c.y - s * 0.42f), Size(s * 1.24f, s * 0.84f))
    drawOval(col, Offset(c.x - s * 0.5f, c.y - s * 0.32f), Size(s, s * 0.64f))
    drawLine(Ink.line.copy(alpha = alpha), Offset(c.x + s * 0.44f, c.y - s * 0.1f), Offset(c.x + s * 0.44f, c.y - s * 1.9f), s * 0.24f, StrokeCap.Round)
    drawLine(Ink.line.copy(alpha = alpha), Offset(c.x + s * 0.44f, c.y - s * 1.9f), Offset(c.x + s * 1.1f, c.y - s * 1.4f), s * 0.26f, StrokeCap.Round)
}

/** Sound-wave arcs to the side of [c]: [side] -1 for left, 1 for right. */
private fun DrawScope.rmWaves(c: Offset, r: Float, side: Float, pen: Pen, alpha: Float) {
    if (alpha <= 0.01f) return
    for (k in 0 until 3) {
        val rr = r * (1f + k * 0.45f)
        val start = if (side > 0f) -35f else 145f
        drawArc(Ink.line, start, 70f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2, rr * 2), alpha = alpha * (1f - k * 0.25f), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
    }
}

/** A few bubbles rising from ([x], [y]) in pixels. */
private fun DrawScope.rmBubbles(at: Offset, u: Float, t: Float, pen: Pen, count: Int, rise: Float, seed: Int, alpha: Float = 1f) {
    if (t == 0f) return
    for (i in 0 until count) {
        val ph = rmFrac(t * (0.35f + 0.1f * rmHash(i, seed)) + rmHash(i, seed + 1))
        val a = alpha * (1f - ph) * min(1f, ph * 6f)
        if (a <= 0.02f) continue
        val r = (0.004f + 0.006f * rmHash(i, seed + 2)) * u * (0.7f + ph * 0.6f)
        val c = Offset(at.x + sin(ph * 9f + i * 2f) * 0.01f * u, at.y - ph * rise * u)
        drawCircle(Color.White.copy(alpha = 0.25f * a), r, c)
        drawCircle(Color.White.copy(alpha = 0.85f * a), r, c, style = Stroke(pen.lw * 0.6f))
        drawCircle(Color.White.copy(alpha = 0.9f * a), r * 0.28f, Offset(c.x - r * 0.35f, c.y - r * 0.35f))
    }
}

// ============================================================================================ DOCTOR

private fun DrawScope.rmHeightChart(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val board = Rect(-0.06f * u, -0.5f * u, 0.06f * u, 0f)
    rmWallShadow(board, u, 0.012f * u)
    box3d(board, 0.012f * u, Color(0xFFFFFBF1), pen, radius = 0.012f * u)
    // Five soft bands, one per tenth of the scene.
    val bands = longArrayOf(0xFFFFE0DC, 0xFFFFECD2, 0xFFFFF5C7, 0xFFDDF4DF, 0xFFDDEBFF)
    val inner = Rect(-0.052f * u, -0.492f * u, 0.052f * u, -0.008f * u)
    clipPath(roundPath(inner, 0.008f * u)) {
        for (i in 0 until 5) {
            val y1 = -i * 0.1f * u
            drawRect(Color(bands[i]), Offset(inner.left, y1 - 0.1f * u), Size(inner.width, 0.1f * u))
        }
    }
    // The giraffe's neck is the ruler.
    val neck = Rect(-0.048f * u, -0.508f * u, -0.016f * u, -0.006f * u)
    val yellow = Color(0xFFFFC94A)
    val patch = Color(0xFFD9832E)
    drawRect(yellow, neck.topLeft, neck.size)
    clipRect(neck.left, neck.top, neck.right, neck.bottom) {
        for (i in 0 until 13) {
            val y = -0.022f - i * 0.039f
            val x = if (i % 2 == 0) -0.041f else -0.026f
            val w = 0.013f + 0.004f * rmHash(i, 3)
            drawRoundRect(patch, o(x - w / 2f, y - 0.011f), Size(w * u, 0.022f * u), CornerRadius(0.005f * u))
            drawRoundRect(patch, o(x + (if (i % 2 == 0) 0.018f else -0.02f), y + 0.006f), Size(0.01f * u, 0.014f * u), CornerRadius(0.004f * u))
        }
    }
    drawRect(Ink.line, neck.topLeft, neck.size, style = pen.thin)
    // Ticks every 0.02, long ones every 0.1 with a dashed guide across.
    val ticks = ArrayList<Offset>(48)
    for (i in 1..24) {
        val y = -i * 0.02f
        val len = if (i % 5 == 0) 0.02f else 0.009f
        ticks.add(o(-0.016f, y))
        ticks.add(o(-0.016f + len, y))
    }
    drawPoints(ticks, PointMode.Lines, Ink.line, strokeWidth = pen.lw * 0.75f, cap = StrokeCap.Round)
    val dash = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(pen.lw * 1.6f, pen.lw * 2.2f)))
    for (k in 1..4) {
        val y = -k * 0.1f * u
        drawLine(Ink.line.copy(alpha = 0.4f), Offset(0.008f * u, y), Offset(0.05f * u, y), strokeWidth = pen.lw * 0.6f, pathEffect = dash.pathEffect)
        // A small arrow pointing at the mark.
        val arrow = Path().apply {
            moveTo(0.004f * u, y)
            lineTo(0.012f * u, y - 0.005f * u)
            lineTo(0.012f * u, y + 0.005f * u)
            close()
        }
        drawPath(arrow, Ink.line)
    }
    // Animals standing on the marks, smallest at the bottom.
    rmIconMouse(o(0.028f, -0.123f), 0.044f * u, pen)
    rmIconBunny(o(0.028f, -0.222f), 0.044f * u, pen)
    rmIconFox(o(0.028f, -0.322f), 0.044f * u, pen)
    rmIconBear(o(0.028f, -0.422f), 0.044f * u, pen)
    // Grass and a flower at the foot of the chart.
    val grass = Path().apply {
        moveTo(-0.014f * u, -0.008f * u)
        for (k in 0 until 7) {
            val x = -0.014f + k * 0.0095f
            quadraticTo((x + 0.003f) * u, -0.028f * u, (x + 0.0048f) * u, -0.026f * u)
            quadraticTo((x + 0.006f) * u, -0.016f * u, (x + 0.0095f) * u, -0.008f * u)
        }
        close()
    }
    drawPath(grass, Color(0xFF5DBB4A))
    drawPath(grass, Ink.line, style = pen.thin)
    inkLine(o(0.036f, -0.008f), o(0.036f, -0.04f), pen, width = pen.lw * 0.7f, color = Color(0xFF3E8E4A))
    for (k in 0 until 5) {
        val a = k * RM_TAU / 5f
        drawCircle(Color(0xFFFF8FB1), 0.0055f * u, o(0.036f + cos(a) * 0.006f, -0.043f + sin(a) * 0.006f))
    }
    rmDot(o(0.036f, -0.043f), 0.004f * u, RoomsInk.sun, pen)
    // Somebody's height was marked in pencil, with a star sticker.
    inkLine(o(0.002f, -0.272f), o(0.042f, -0.27f), pen, width = pen.lw * 1.1f, color = Color(0xFFE8574A))
    val star = starPath(o(0.046f, -0.271f), 0.0105f * u, 0.0048f * u, 8f)
    drawPath(star, RoomsInk.sun)
    drawPath(star, Ink.line, style = pen.thin)
    // The giraffe's head peeks over the top and bobs a little.
    val bob = sin(t * 1.4f) * 0.0025f * u
    translate(0f, bob) {
        for ((x0, x1) in listOf(-0.042f to -0.047f, -0.027f to -0.025f)) {
            rmTube(o(x0, -0.53f), o(x1, -0.566f), 0.006f * u, yellow, pen)
            rmDot(o(x1, -0.568f), 0.0062f * u, Color(0xFF8A5A2B), pen)
        }
        inked(rmLens(o(-0.05f, -0.532f), o(-0.071f, -0.548f), 0.005f * u), yellow, pen)
        inkedOval(rect(-0.03f * u, -0.526f * u, 0.052f * u, 0.036f * u), yellow, pen)
        drawCircle(patch, 0.007f * u, o(-0.04f, -0.535f))
        inkedOval(rect(-0.006f * u, -0.519f * u, 0.032f * u, 0.025f * u), Color(0xFFFFE3A6), pen)
        drawCircle(Ink.line, 0.0048f * u, o(-0.032f, -0.531f))
        drawCircle(Color.White, 0.0017f * u, o(-0.0332f, -0.5325f))
        drawCircle(Ink.line, 0.0018f * u, o(0.004f, -0.523f))
        drawArc(Ink.line, 20f, 110f, false, o(-0.013f, -0.522f), Size(0.016f * u, 0.01f * u), style = pen.thin)
        drawOval(Ink.blush, o(-0.028f, -0.521f), Size(0.012f * u, 0.006f * u))
    }
}

private fun DrawScope.rmIconMouse(c: Offset, s: Float, pen: Pen) {
    val grey = Color(0xFFB9BCCB)
    for (side in RM_SIDES) {
        val e = Offset(c.x + side * s * 0.3f, c.y - s * 0.26f)
        rmDot(e, s * 0.2f, grey, pen)
        drawCircle(Color(0xFFFFB3C1), s * 0.11f, e)
    }
    inkedCircle(c, s * 0.3f, grey, pen)
    for (side in RM_SIDES) drawCircle(Ink.line, s * 0.045f, Offset(c.x + side * s * 0.11f, c.y - s * 0.03f))
    drawCircle(Color(0xFFFF7F9A), s * 0.055f, Offset(c.x, c.y + s * 0.1f))
    for (side in RM_SIDES) inkLine(Offset(c.x + side * s * 0.12f, c.y + s * 0.12f), Offset(c.x + side * s * 0.36f, c.y + s * 0.08f), pen, width = pen.lw * 0.4f)
}

private fun DrawScope.rmIconBunny(c: Offset, s: Float, pen: Pen) {
    val white = Color(0xFFF7F4F2)
    for (side in RM_SIDES) {
        rotate(side * 14f, pivot = Offset(c.x + side * s * 0.1f, c.y - s * 0.2f)) {
            inkedOval(Rect(c.x + side * s * 0.12f - s * 0.08f, c.y - s * 0.72f, c.x + side * s * 0.12f + s * 0.08f, c.y - s * 0.16f), white, pen, shade = false)
            drawOval(Color(0xFFFFB3C1), Offset(c.x + side * s * 0.12f - s * 0.035f, c.y - s * 0.62f), Size(s * 0.07f, s * 0.36f))
        }
    }
    inkedCircle(Offset(c.x, c.y + s * 0.04f), s * 0.28f, white, pen)
    for (side in RM_SIDES) drawCircle(Ink.line, s * 0.04f, Offset(c.x + side * s * 0.1f, c.y))
    drawCircle(Color(0xFFFF7F9A), s * 0.045f, Offset(c.x, c.y + s * 0.1f))
    for (side in RM_SIDES) drawOval(Ink.blush, Offset(c.x + side * s * 0.16f - s * 0.05f, c.y + s * 0.08f), Size(s * 0.1f, s * 0.05f))
}

private fun DrawScope.rmIconFox(c: Offset, s: Float, pen: Pen) {
    val orange = Color(0xFFF28A30)
    for (side in RM_SIDES) {
        val ear = Path().apply {
            moveTo(c.x + side * s * 0.08f, c.y - s * 0.2f)
            lineTo(c.x + side * s * 0.3f, c.y - s * 0.46f)
            lineTo(c.x + side * s * 0.34f, c.y - s * 0.1f)
            close()
        }
        inked(ear, orange, pen, shade = false)
        drawPath(Path().apply {
            moveTo(c.x + side * s * 0.16f, c.y - s * 0.2f)
            lineTo(c.x + side * s * 0.28f, c.y - s * 0.36f)
            lineTo(c.x + side * s * 0.3f, c.y - s * 0.16f)
            close()
        }, Ink.line.copy(alpha = 0.7f))
    }
    val head = Path().apply {
        moveTo(c.x - s * 0.38f, c.y - s * 0.12f)
        quadraticTo(c.x, c.y - s * 0.34f, c.x + s * 0.38f, c.y - s * 0.12f)
        quadraticTo(c.x + s * 0.3f, c.y + s * 0.14f, c.x, c.y + s * 0.3f)
        quadraticTo(c.x - s * 0.3f, c.y + s * 0.14f, c.x - s * 0.38f, c.y - s * 0.12f)
        close()
    }
    inked(head, orange, pen)
    val cheeks = Path().apply {
        moveTo(c.x - s * 0.36f, c.y - s * 0.06f)
        quadraticTo(c.x - s * 0.1f, c.y + s * 0.02f, c.x, c.y + s * 0.3f)
        quadraticTo(c.x + s * 0.1f, c.y + s * 0.02f, c.x + s * 0.36f, c.y - s * 0.06f)
        quadraticTo(c.x + s * 0.26f, c.y + s * 0.16f, c.x, c.y + s * 0.3f)
        quadraticTo(c.x - s * 0.26f, c.y + s * 0.16f, c.x - s * 0.36f, c.y - s * 0.06f)
        close()
    }
    drawPath(cheeks, Color.White)
    drawPath(head, Ink.line, style = pen.thin)
    for (side in RM_SIDES) drawCircle(Ink.line, s * 0.04f, Offset(c.x + side * s * 0.12f, c.y - s * 0.06f))
    drawCircle(Ink.line, s * 0.05f, Offset(c.x, c.y + s * 0.26f))
}

private fun DrawScope.rmIconBear(c: Offset, s: Float, pen: Pen) {
    val brown = Color(0xFFA86B3C)
    for (side in RM_SIDES) {
        val e = Offset(c.x + side * s * 0.27f, c.y - s * 0.25f)
        rmDot(e, s * 0.13f, brown, pen)
        drawCircle(Color(0xFFE8B98A), s * 0.065f, e)
    }
    inkedCircle(c, s * 0.32f, brown, pen)
    inkedOval(Rect(c.x - s * 0.14f, c.y + s * 0.0f, c.x + s * 0.14f, c.y + s * 0.2f), Color(0xFFE8B98A), pen, shade = false)
    drawOval(Ink.line, Offset(c.x - s * 0.05f, c.y + s * 0.03f), Size(s * 0.1f, s * 0.065f))
    for (side in RM_SIDES) drawCircle(Ink.line, s * 0.042f, Offset(c.x + side * s * 0.12f, c.y - s * 0.06f))
}

private fun DrawScope.rmEyeChart(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val frame = Rect(-0.08f * u, -0.22f * u, 0.08f * u, 0f)
    rmWallShadow(frame, u, 0.01f * u)
    box3d(frame, 0.018f * u, Color(0xFFE6EBF2), pen, radius = 0.01f * u)
    val panel = Rect(-0.068f * u, -0.208f * u, 0.068f * u, -0.013f * u)
    drawRect(Brush.verticalGradient(0f to Color.White, 1f to Color(0xFFEFF3F8), startY = panel.top, endY = panel.bottom), panel.topLeft, panel.size)
    // A blue header with an eye on it.
    drawRect(RoomsInk.fjord, panel.topLeft, Size(panel.width, 0.026f * u))
    val eye = rmLens(o(-0.02f, -0.195f), o(0.02f, -0.195f), 0.0055f * u)
    drawPath(eye, Color.White)
    drawCircle(RoomsInk.sky, 0.0058f * u, o(0f, -0.195f))
    drawCircle(Ink.line, 0.0028f * u, o(0f, -0.195f))
    drawCircle(Color.White, 0.0012f * u, o(-0.0015f, -0.1965f))
    drawPath(eye, Ink.line, style = pen.thin)
    drawRect(Ink.line, panel.topLeft, panel.size, style = pen.thin)
    // Rows of shrinking shapes; a tap swaps the set with a little flip.
    val a = rmAnim(f, pen)
    val ys = floatArrayOf(-0.153f, -0.111f, -0.079f, -0.054f, -0.032f)
    val sizes = floatArrayOf(0.034f, 0.025f, 0.018f, 0.013f, 0.009f)
    withTransform({ scale(1f, 1f - 0.7f * a, pivot = o(0f, -0.095f)) }) {
        for (row in 0 until 5) {
            val n = row + 1
            val s = sizes[row]
            val gap = min(0.028f, s * 1.75f)
            for (i in 0 until n) {
                val x = (i - (n - 1) / 2f) * gap
                rmChartShape(f.mode, o(x, ys[row]), s * u, row * 7 + i, pen)
            }
        }
    }
    // The line to read, underlined in red.
    inkLine(o(-0.05f, -0.066f), o(0.05f, -0.066f), pen, width = pen.lw * 0.9f, color = RoomsInk.red)
    val pointer = Path().apply {
        moveTo(-0.063f * u, -0.066f * u)
        lineTo(-0.055f * u, -0.071f * u)
        lineTo(-0.055f * u, -0.061f * u)
        close()
    }
    drawPath(pointer, RoomsInk.red)
    // The paddle for covering one eye hangs on a hook.
    inkLine(o(0.081f, -0.15f), o(0.089f, -0.15f), pen, width = pen.lw * 0.9f)
    rmTube(o(0.09f, -0.15f), o(0.093f, -0.075f), 0.006f * u, Color(0xFFFF6FA8), pen)
    inkedOval(rect(0.093f * u, -0.07f * u, 0.03f * u, 0.036f * u), RoomsInk.charcoal, pen)
    shine(o(0.089f, -0.078f), 0.008f * u, 0.005f * u, 0.5f)
}

/** One shape of the eye chart: circles, stars, hearts or ducks by [mode]. */
private fun DrawScope.rmChartShape(mode: Int, c: Offset, s: Float, index: Int, pen: Pen) {
    val big = s > pen.lw * 7f
    when (mode.mod(4)) {
        0 -> {
            val col = RoomsInk.fjord
            drawCircle(col, s * 0.46f, c)
            if (big) {
                drawCircle(Ink.line, s * 0.46f, c, style = pen.thin)
                shine(Offset(c.x - s * 0.15f, c.y - s * 0.17f), s * 0.18f, s * 0.12f, 0.6f)
            }
        }
        1 -> {
            val star = starPath(c, s * 0.52f, s * 0.24f, (index * 23 % 40 - 20).toFloat())
            drawPath(star, RoomsInk.sun)
            drawPath(star, Ink.line, style = if (big) pen.thin else Stroke(pen.lw * 0.35f, join = StrokeJoin.Round))
        }
        2 -> {
            val heart = rmHeart(c.x, c.y + s * 0.04f, s)
            drawPath(heart, RoomsInk.red)
            if (big) {
                drawPath(heart, Ink.line, style = pen.thin)
                shine(Offset(c.x - s * 0.2f, c.y - s * 0.12f), s * 0.14f, s * 0.1f, 0.6f)
            }
        }
        else -> {
            val flip = if (index % 2 == 0) 1f else -1f
            val yellow = Color(0xFFFFD23F)
            val body = Path().apply {
                addOval(Rect(c.x - s * 0.42f, c.y - s * 0.08f, c.x + s * 0.42f, c.y + s * 0.34f))
                moveTo(c.x + flip * s * 0.3f, c.y + s * 0.02f)
                lineTo(c.x + flip * s * 0.52f, c.y - s * 0.14f)
                lineTo(c.x + flip * s * 0.4f, c.y + s * 0.16f)
                close()
                addOval(Rect(c.x - flip * s * 0.22f - s * 0.19f, c.y - s * 0.4f, c.x - flip * s * 0.22f + s * 0.19f, c.y - s * 0.02f))
            }
            if (big) drawPath(body, Ink.line, style = Stroke(pen.lw * 1.2f, join = StrokeJoin.Round))
            drawPath(body, yellow)
            val beak = Path().apply {
                moveTo(c.x - flip * s * 0.36f, c.y - s * 0.26f)
                lineTo(c.x - flip * s * 0.58f, c.y - s * 0.2f)
                lineTo(c.x - flip * s * 0.36f, c.y - s * 0.13f)
                close()
            }
            drawPath(beak, Color(0xFFFF8A3D))
            if (big) drawCircle(Ink.line, s * 0.045f, Offset(c.x - flip * s * 0.24f, c.y - s * 0.25f))
        }
    }
}

private fun DrawScope.rmMedicineCabinet(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val white = Color(0xFFF1F4F8)
    val d = 0.07f
    rmWallShadow(Rect(o(-0.13f, -0.24f), o(0.13f, 0f)), u, 0.012f * u)
    rmBox(u, -0.13f, -0.24f, 0.13f, 0f, d, white, pen, rad = 0.012f, side = Color(0xFFC6CFDB))
    rmBox(u, -0.137f, -0.254f, 0.137f, -0.239f, d + 0.008f, Color(0xFFE4EAF1), pen, rad = 0.005f, z = -0.004f, side = Color(0xFFBCC6D3))
    if (!f.open) {
        val door = Rect(o(-0.12f, -0.23f), o(0.12f, -0.012f))
        inkedRound(door, 0.01f * u, Color(0xFFF8FAFC), pen)
        drawRoundRect(Color.White, Offset(door.left + 0.008f * u, door.top + 0.008f * u), Size(door.width - 0.016f * u, door.height - 0.016f * u), CornerRadius(0.006f * u), style = Stroke(pen.lw * 0.8f))
        // The pharmacy sign: a white plus on a green rounded square.
        val sign = Rect(o(-0.052f, -0.174f), o(0.052f, -0.07f))
        inkedRound(sign, 0.02f * u, RoomsInk.pharmacy, pen)
        val plus = rmPlus(sign.center, 0.07f * u)
        drawPath(plus, Color.White)
        drawPath(plus, Ink.line.copy(alpha = 0.25f), style = Stroke(pen.lw * 0.5f, join = StrokeJoin.Round))
        shine(Offset(sign.left + 0.018f * u, sign.top + 0.013f * u), 0.018f * u, 0.009f * u, 0.55f)
        // Handle.
        inkedRound(Rect(o(0.096f, -0.15f), o(0.106f, -0.095f)), 0.005f * u, RoomsInk.chrome, pen)
        shine(o(0.099f, -0.137f), 0.003f * u, 0.012f * u, 0.8f)
        // A plaster with a smile stuck on the corner.
        rotate(-24f, pivot = o(-0.084f, -0.2f)) {
            val strip = Rect(o(-0.112f, -0.208f), o(-0.056f, -0.192f))
            inkedRound(strip, 0.008f * u, Color(0xFFF3C9A1), pen, shade = false)
            drawRoundRect(Color(0xFFFBE4CB), o(-0.093f, -0.205f), Size(0.018f * u, 0.01f * u), CornerRadius(0.003f * u))
            for (k in 0 until 3) {
                drawCircle(Color(0xFFD9A77E), 0.0011f * u, o(-0.106f + k * 0.004f, -0.2f))
                drawCircle(Color(0xFFD9A77E), 0.0011f * u, o(-0.07f + k * 0.004f, -0.2f))
            }
            drawCircle(Ink.line, 0.0012f * u, o(-0.087f, -0.201f))
            drawCircle(Ink.line, 0.0012f * u, o(-0.081f, -0.201f))
            drawArc(Ink.line, 20f, 140f, false, o(-0.0875f, -0.2015f), Size(0.007f * u, 0.004f * u), style = Stroke(pen.lw * 0.4f))
        }
        return
    }
    // Open: the inside with a middle shelf, lit from the top.
    val l = -0.115f * u
    val r = 0.115f * u
    val tp = -0.228f * u
    val bt = -0.02f * u
    val dx = ODX * 0.06f * u
    val dy = ODY * 0.06f * u
    val back = Color(0xFFE2F2EC)
    clipRect(l, tp, r, bt) {
        drawRect(back.darken(0.08f), Offset(l, tp), Size(r - l, bt - tp))
        drawRect(Brush.verticalGradient(0f to RoomsInk.warm.copy(alpha = 0.55f), 1f to RoomsInk.warm.copy(alpha = 0f), startY = tp, endY = tp + 0.07f * u), Offset(l, tp), Size(r - l, 0.07f * u))
        drawPath(rmQuad(Offset(l, tp), Offset(l + dx, tp + dy), Offset(l + dx, bt + dy), Offset(l, bt)), back.darken(0.16f))
        drawPath(rmQuad(Offset(l, bt), Offset(l + dx, bt + dy), Offset(r + dx, bt + dy), Offset(r, bt)), back.lighten(0.3f))
        val ys = -0.12f * u
        drawRect(Brush.verticalGradient(0f to Ink.line.copy(alpha = 0.18f), 1f to Ink.line.copy(alpha = 0f), startY = ys + dy, endY = ys + dy + 0.03f * u), Offset(l, ys + dy), Size(r - l, 0.03f * u))
        drawPath(rmQuad(Offset(l, ys), Offset(l + dx, ys + dy), Offset(r + dx, ys + dy), Offset(r, ys)), Color.White)
        drawPath(rmQuad(Offset(l, ys), Offset(l + dx, ys + dy), Offset(r + dx, ys + dy), Offset(r, ys)), Ink.line, style = pen.thin)
        drawRect(Color(0xFFD7E3EE), Offset(l, ys), Size(r - l, 0.008f * u))
        drawRect(Ink.line, Offset(l, ys), Size(r - l, 0.008f * u), style = pen.thin)
        drawLine(Ink.line.copy(alpha = 0.45f), Offset(l + dx, tp), Offset(l + dx, bt + dy), pen.lw * 0.6f)
        drawLine(Ink.line.copy(alpha = 0.45f), Offset(l + dx, bt + dy), Offset(r, bt + dy), pen.lw * 0.6f)
        rmGlow(Offset((l + r) / 2f + dx, tp + dy + 0.01f * u), 0.08f * u, RoomsInk.warm, 0.5f)
    }
    drawRect(Ink.line, Offset(l, tp), Size(r - l, bt - tp), style = pen.stroke)
    // The door swung open on its left hinge, a mirror inside.
    val w = 0.26f
    val ang = 115f * RM_PI / 180f
    val vx = w * cos(ang)
    val vz = -w * sin(ang)
    val v = Offset((vx + ODX * vz) * u, ODY * vz * u)
    val h1 = o(-0.13f, -0.235f)
    val h2 = o(-0.13f, -0.005f)
    fun dp(s: Float, k: Float) = Offset(h1.x + v.x * s + (h2.x - h1.x) * k, h1.y + v.y * s + (h2.y - h1.y) * k)
    val edge = 0.01f * u
    rmFace(rmQuad(dp(1f, 0f), Offset(dp(1f, 0f).x - edge, dp(1f, 0f).y - edge * 0.4f), Offset(dp(1f, 1f).x - edge, dp(1f, 1f).y - edge * 0.4f), dp(1f, 1f)), Color(0xFFC6CFDB), pen)
    rmFace(rmQuad(dp(0f, 0f), dp(1f, 0f), dp(1f, 1f), dp(0f, 1f)), Color(0xFFEFF3F7), pen)
    val mirror = rmQuad(dp(0.14f, 0.09f), dp(0.86f, 0.09f), dp(0.86f, 0.91f), dp(0.14f, 0.91f))
    drawPath(mirror, Brush.linearGradient(listOf(Color(0xFFE3F3FF), Color(0xFFA9CDEB)), dp(0.14f, 0.09f), dp(0.86f, 0.91f)))
    clipPath(mirror) {
        drawLine(Color.White.copy(alpha = 0.7f), dp(0.25f, 0.2f), dp(0.55f, 0.02f), strokeWidth = 0.012f * u)
        drawLine(Color.White.copy(alpha = 0.5f), dp(0.3f, 0.45f), dp(0.8f, 0.15f), strokeWidth = 0.006f * u)
    }
    drawPath(mirror, Ink.line, style = pen.thin)
}

// ------------------------------------------------------------------------------------------ X-ray

private const val XR_D = 0.1f

private fun DrawScope.rmXrayBack(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val white = Color(0xFFEFF3F7)
    val sideCol = Color(0xFFC3CEDA)
    rmShadow(u, 0.36f, 0.14f)
    // The machine: a column behind the booth and an arm holding the X-ray head over it.
    rmBox(u, 0.15f, -0.47f, 0.178f, -0.012f, 0.03f, white, pen, rad = 0.006f, z = 0.05f, side = sideCol)
    val shoulder = rp(u, 0.164f, -0.47f, 0.065f)
    val elbow = o(0.13f, -0.545f)
    val wrist = o(0.058f, -0.535f)
    rmTube(shoulder, elbow, 0.013f * u, RoomsInk.steel, pen)
    rmTube(elbow, wrist, 0.011f * u, RoomsInk.steel, pen)
    rmDot(shoulder, 0.01f * u, RoomsInk.teal, pen)
    rmDot(elbow, 0.009f * u, RoomsInk.teal, pen)
    rotate(-16f, pivot = o(0.03f, -0.51f)) {
        inkedRound(Rect(o(-0.012f, -0.532f), o(0.078f, -0.49f)), 0.018f * u, white, pen)
        drawRect(RoomsInk.teal, o(-0.004f, -0.516f), Size(0.074f * u, 0.007f * u))
        inkedRound(Rect(o(0.004f, -0.492f), o(0.056f, -0.476f)), 0.005f * u, Color(0xFF4A5363), pen)
        drawOval(if (f.on) RoomsInk.green else Color(0xFF6B7788), o(0.011f, -0.487f), Size(0.038f * u, 0.007f * u))
        val warn = Path().apply {
            moveTo(0.058f * u, -0.527f * u)
            lineTo(0.068f * u, -0.509f * u)
            lineTo(0.048f * u, -0.509f * u)
            close()
        }
        drawPath(warn, RoomsInk.sun)
        drawPath(warn, Ink.line, style = Stroke(pen.lw * 0.5f, join = StrokeJoin.Round))
        drawCircle(Ink.line, 0.0022f * u, o(0.058f, -0.5145f))
    }
    if (f.on) rmGlow(o(0.035f, -0.47f), 0.06f * u, RoomsInk.green, 0.35f + 0.2f * pen.night)
    // Floor plate with two painted footprints.
    rmBox(u, -0.15f, -0.012f, 0.15f, 0f, XR_D, Color(0xFFD3DBE5), pen, rad = 0.003f)
    for (side in RM_SIDES) {
        val c = rp(u, side * 0.04f, -0.012f, 0.052f)
        drawOval(RoomsInk.teal.copy(alpha = 0.55f), Offset(c.x - 0.014f * u, c.y - 0.0055f * u), Size(0.028f * u, 0.011f * u))
        for (k in 0 until 3) drawCircle(RoomsInk.teal.copy(alpha = 0.55f), 0.0028f * u, Offset(c.x + (0.014f + 0.002f * k) * u * side * 0.1f + (k - 1) * 0.006f * u, c.y - 0.0095f * u))
    }
    // The back plate with the detector's grid.
    val bp = Rect(rp(u, -0.15f, -0.44f, XR_D), rp(u, 0.15f, -0.012f, XR_D))
    inkedRound(bp, 0.008f * u, Color(0xFFCAD6E2), pen, shade = false)
    val det = Rect(rp(u, -0.118f, -0.39f, XR_D), rp(u, 0.118f, -0.035f, XR_D))
    drawRoundRect(if (f.on) Color(0xFF1F4A4C) else Color(0xFF2B3A4C), det.topLeft, det.size, CornerRadius(0.006f * u))
    val grid = ArrayList<Offset>(32)
    var gx = det.left + 0.03f * u
    while (gx < det.right) {
        grid.add(Offset(gx, det.top))
        grid.add(Offset(gx, det.bottom))
        gx += 0.03f * u
    }
    var gy = det.top + 0.03f * u
    while (gy < det.bottom) {
        grid.add(Offset(det.left, gy))
        grid.add(Offset(det.right, gy))
        gy += 0.03f * u
    }
    drawPoints(grid, PointMode.Lines, RoomsInk.green.copy(alpha = if (f.on) 0.35f else 0.14f), strokeWidth = pen.lw * 0.5f)
    drawRoundRect(Ink.line, det.topLeft, det.size, CornerRadius(0.006f * u), style = pen.thin)
    // Left inner wall, then the outside of the right wall with the controls.
    rmFace(rmQuad(rp(u, -0.15f, -0.4f, 0f), rp(u, -0.15f, -0.4f, XR_D), rp(u, -0.15f, -0.012f, XR_D), rp(u, -0.15f, -0.012f, 0f)), Color(0xFFDDE5EE), pen)
    rmFace(rmQuad(rp(u, 0.15f, -0.44f, 0f), rp(u, 0.15f, -0.44f, XR_D), rp(u, 0.15f, 0f, XR_D), rp(u, 0.15f, 0f, 0f)), sideCol, pen)
    rmFace(rmQuad(rp(u, 0.15f, -0.33f, 0.022f), rp(u, 0.15f, -0.33f, 0.078f), rp(u, 0.15f, -0.2f, 0.078f), rp(u, 0.15f, -0.2f, 0.022f)), Color(0xFF4A5363), pen)
    rmDot(rp(u, 0.15f, -0.3f, 0.038f), 0.0065f * u, if (f.on) RoomsInk.green else Color(0xFF5E8F73), pen)
    rmDot(rp(u, 0.15f, -0.3f, 0.062f), 0.0065f * u, RoomsInk.coral, pen)
    rmDot(rp(u, 0.15f, -0.25f, 0.05f), 0.011f * u, RoomsInk.chrome, pen)
    val dial = rp(u, 0.15f, -0.25f, 0.05f)
    val da = if (f.on) -0.6f + sin(t * 3f) * 0.3f else 0.9f
    inkLine(dial, Offset(dial.x + cos(da) * 0.008f * u, dial.y + sin(da) * 0.008f * u), pen, width = pen.lw * 0.7f, color = RoomsInk.red)
    // The header's top face and the warning lamp on it.
    rmFace(rmQuad(rp(u, -0.15f, -0.44f, 0f), rp(u, -0.15f, -0.44f, XR_D), rp(u, 0.15f, -0.44f, XR_D), rp(u, 0.15f, -0.44f, 0f)), Color(0xFFFFFFFF), pen)
    val lamp = rp(u, -0.085f, -0.44f, 0.05f)
    rmFace(rmDisc(lamp.x, lamp.y, 0.017f * u, 0.017f * u), RoomsInk.steel, pen)
    val lit = f.on
    val lampCol = if (lit) Color(0xFFFF5A4E) else Color(0xFFB05A5A)
    if (lit) rmGlow(Offset(lamp.x, lamp.y - 0.012f * u), 0.07f * u, Color(0xFFFF6A50), 0.45f + 0.3f * pen.night + 0.12f * sin(t * 7f))
    val dome = Path().apply {
        moveTo(lamp.x - 0.014f * u, lamp.y)
        cubicTo(lamp.x - 0.014f * u, lamp.y - 0.026f * u, lamp.x + 0.014f * u, lamp.y - 0.026f * u, lamp.x + 0.014f * u, lamp.y)
        close()
    }
    inked(dome, lampCol, pen)
    if (lit) drawCircle(Color(0xFFFFE0C8), 0.005f * u, Offset(lamp.x - 0.003f * u, lamp.y - 0.011f * u))
    shine(Offset(lamp.x - 0.005f * u, lamp.y - 0.013f * u), 0.005f * u, 0.004f * u)
}

private fun DrawScope.rmXrayFront(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val scr = Rect(o(-0.13f, -0.4f), o(0.13f, -0.012f))
    if (f.on) {
        val k = 1f + 0.35f * pen.night
        drawRect(
            Brush.verticalGradient(0f to Color(0xFFBFFFF0).copy(alpha = 0.3f * k), 1f to Color(0xFF3FE6C8).copy(alpha = 0.42f * k), startY = scr.top, endY = scr.bottom),
            scr.topLeft,
            scr.size,
        )
        val lines = ArrayList<Offset>(80)
        var y = scr.top + 0.006f * u
        while (y < scr.bottom) {
            lines.add(Offset(scr.left, y))
            lines.add(Offset(scr.right, y))
            y += 0.011f * u
        }
        drawPoints(lines, PointMode.Lines, Color.White.copy(alpha = 0.13f), strokeWidth = pen.lw * 0.55f)
        val sweep = scr.top + (0.5f + 0.5f * sin(t * 1.7f)) * (scr.height - 0.03f * u)
        drawRect(
            Brush.verticalGradient(0f to Color(0x0080FFE8), 0.5f to Color(0x8080FFE8), 1f to Color(0x0080FFE8), startY = sweep, endY = sweep + 0.03f * u),
            Offset(scr.left, sweep),
            Size(scr.width, 0.03f * u),
        )
        drawLine(Color.White.copy(alpha = 0.8f), Offset(scr.left, sweep + 0.015f * u), Offset(scr.right, sweep + 0.015f * u), pen.lw * 0.8f)
        val c = 0.028f * u
        val m = 0.01f * u
        val br = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round)
        for (sx in RM_SIDES) for (sy in RM_SIDES) {
            val cx = if (sx < 0) scr.left + m else scr.right - m
            val cy = if (sy < 0) scr.top + m else scr.bottom - m
            val p = Path().apply {
                moveTo(cx, cy - sy * c)
                lineTo(cx, cy)
                lineTo(cx - sx * c, cy)
            }
            drawPath(p, Color.White.copy(alpha = 0.85f), style = br)
        }
        rmGlow(scr.center, 0.34f * u, RoomsInk.green, 0.12f + 0.2f * pen.night)
    } else {
        // Just a glint on the glass, up in the corner, clear of the face.
        drawLine(Color.White.copy(alpha = 0.45f), o(-0.122f, -0.3f), o(-0.095f, -0.39f), pen.lw * 1.6f, StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.3f), o(-0.122f, -0.25f), o(-0.113f, -0.28f), pen.lw * 1.1f, StrokeCap.Round)
    }
    // The frame: two posts and the header, drawn over whoever stands inside.
    val white = Color(0xFFF4F7FA)
    inkedRound(Rect(o(-0.152f, -0.44f), o(-0.13f, 0f)), 0.005f * u, white, pen)
    inkedRound(Rect(o(0.13f, -0.44f), o(0.152f, 0f)), 0.005f * u, white, pen)
    for (x in floatArrayOf(-0.141f, 0.141f)) drawLine(RoomsInk.teal, o(x, -0.36f), o(x, -0.06f), pen.lw * 1.2f, StrokeCap.Round)
    val head = Rect(o(-0.152f, -0.44f), o(0.152f, -0.4f))
    inkedRound(head, 0.006f * u, white, pen)
    drawRect(RoomsInk.teal, Offset(head.left + 0.006f * u, head.top + 0.026f * u), Size(head.width - 0.012f * u, 0.006f * u))
    for (k in -1..1) {
        val c = o(k * 0.022f, -0.425f)
        if (f.on) rmGlow(c, 0.012f * u, RoomsInk.green, 0.8f)
        rmDot(c, 0.0048f * u, if (f.on) RoomsInk.green else Color(0xFF8A94A6), pen)
    }
    shine(o(-0.12f, -0.432f), 0.018f * u, 0.004f * u, 0.7f)
}

private fun DrawScope.rmExamBed(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val white = Color(0xFFF2F5F8)
    val side = Color(0xFFC5CEDA)
    rmShadow(u, 0.44f, 0.14f)
    // Castors.
    for (x in floatArrayOf(-0.17f, 0.17f)) {
        rmDot(rp(u, x, -0.008f, 0.1f), 0.008f * u, RoomsInk.rubber, pen)
        rmDot(o(x, -0.008f), 0.008f * u, RoomsInk.rubber, pen)
        drawCircle(RoomsInk.steel, 0.003f * u, o(x, -0.008f))
    }
    // Cabinet base: a niche with rolled towels, drawers and a cupboard door.
    rmBox(u, -0.19f, -0.125f, 0.19f, -0.014f, 0.12f, white, pen, rad = 0.008f, side = side)
    val niche = Rect(o(-0.178f, -0.114f), o(-0.072f, -0.026f))
    drawRoundRect(Color(0xFFB9C4D1), niche.topLeft, niche.size, CornerRadius(0.005f * u))
    drawRect(Color(0xFF9EABBB), niche.topLeft, Size(niche.width, 0.01f * u))
    for ((i, col) in listOf(Color(0xFF8FD9C0), Color(0xFFFFB3C1), Color(0xFFFFE08A)).withIndex()) {
        val c = o(-0.158f + i * 0.033f, -0.048f)
        inkedCircle(c, 0.017f * u, col, pen)
        drawArc(col.darken(0.3f), 0f, 300f, false, Offset(c.x - 0.009f * u, c.y - 0.009f * u), Size(0.018f * u, 0.018f * u), style = Stroke(pen.lw * 0.6f))
        drawCircle(col.darken(0.3f), 0.0025f * u, c)
    }
    val towel = o(-0.125f, -0.08f)
    inkedCircle(towel, 0.017f * u, Color(0xFFBFE3FF), pen)
    drawArc(Color(0xFF7FA3C4), 0f, 300f, false, Offset(towel.x - 0.009f * u, towel.y - 0.009f * u), Size(0.018f * u, 0.018f * u), style = Stroke(pen.lw * 0.6f))
    drawRoundRect(Ink.line, niche.topLeft, niche.size, CornerRadius(0.005f * u), style = pen.thin)
    for (k in 0 until 2) {
        val d = Rect(o(-0.062f, -0.114f + k * 0.046f), o(0.062f, -0.072f + k * 0.046f))
        inkedRound(d, 0.004f * u, Color(0xFFF8FAFC), pen, shade = false)
        inkedRound(Rect(o(-0.018f, d.center.y / u - 0.004f), o(0.018f, d.center.y / u + 0.004f)), 0.004f * u, RoomsInk.mint, pen, shade = false)
    }
    val door = Rect(o(0.072f, -0.114f), o(0.178f, -0.026f))
    inkedRound(door, 0.004f * u, Color(0xFFF8FAFC), pen, shade = false)
    rmDot(o(0.084f, -0.07f), 0.0055f * u, RoomsInk.mint, pen)
    // A little heart sticker.
    val heart = rmHeart(0.155f * u, -0.098f * u, 0.022f * u)
    drawPath(heart, RoomsInk.coral)
    drawPath(heart, Ink.line, style = pen.thin)
    // The padded top.
    val pad = Color(0xFF63C2AC)
    rmBox(u, -0.212f, -0.17f, 0.212f, -0.125f, 0.14f, pad, pen, rad = 0.018f, top = pad.lighten(0.22f), side = pad.darken(0.25f))
    drawLine(
        pad.lighten(0.45f), o(-0.195f, -0.133f), o(0.195f, -0.133f), pen.lw * 0.7f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(pen.lw * 2.2f, pen.lw * 1.6f)),
    )
    for (x in floatArrayOf(-0.1f, 0f, 0.1f)) drawCircle(pad.darken(0.3f), 0.003f * u, o(x, -0.149f))
    // A pillow at the head end.
    val pc = rp(u, -0.14f, -0.182f, 0.07f)
    val pillow = blobPath(
        pc.x - 0.055f * u, pc.y - 0.004f * u, pc.x - 0.02f * u, pc.y - 0.016f * u, pc.x + 0.03f * u, pc.y - 0.014f * u,
        pc.x + 0.058f * u, pc.y + 0.002f * u, pc.x + 0.03f * u, pc.y + 0.014f * u, pc.x - 0.03f * u, pc.y + 0.014f * u,
    )
    inked(pillow, Color(0xFFF7FBFF), pen)
    drawLine(Color(0xFF9CC6EA), Offset(pc.x - 0.04f * u, pc.y), Offset(pc.x + 0.042f * u, pc.y - 0.002f * u), pen.lw * 0.8f, StrokeCap.Round)
    // The paper sheet, fed from a roll at the foot end.
    val paper = RoomsInk.paper
    val sheet = rmQuad(rp(u, -0.2f, -0.17f, 0.025f), rp(u, -0.2f, -0.17f, 0.115f), rp(u, 0.176f, -0.17f, 0.115f), rp(u, 0.176f, -0.17f, 0.025f))
    drawPath(sheet, paper.copy(alpha = 0.92f))
    clipPath(sheet) {
        for (k in 0 until 3) {
            val x = -0.1f + k * 0.1f
            drawLine(Ink.line.copy(alpha = 0.25f), rp(u, x, -0.17f, 0.025f), rp(u, x, -0.17f, 0.115f), pen.lw * 0.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(pen.lw, pen.lw)))
        }
    }
    drawPath(sheet, Ink.line, style = pen.thin)
    drawPath(pillow, Ink.line, style = pen.thin)
    val core = rp(u, 0.198f, -0.19f, 0.02f)
    inkLine(rp(u, 0.198f, -0.17f, 0.02f), rp(u, 0.2f, -0.19f, 0.02f), pen, width = pen.lw * 1.4f, color = RoomsInk.steel)
    rmShell(core, 0.022f * u, 0.1f * u, paper.darken(0.1f), pen)
    inkedCircle(core, 0.022f * u, paper, pen)
    drawCircle(Color(0xFFC9A27A), 0.008f * u, core)
    drawCircle(Ink.line, 0.008f * u, core, style = pen.thin)
    drawArc(Ink.line.copy(alpha = 0.3f), 200f, 250f, false, Offset(core.x - 0.015f * u, core.y - 0.015f * u), Size(0.03f * u, 0.03f * u), style = Stroke(pen.lw * 0.5f))
}

private fun DrawScope.rmDoctorDesk(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    val white = Color(0xFFF3F6F9)
    rmShadow(u, 0.36f, 0.16f)
    // Back panel and legs on the left, drawers on the right.
    val panel = Rect(rp(u, -0.17f, -0.165f, 0.14f), rp(u, 0.05f, -0.06f, 0.14f))
    inkedRound(panel, 0.003f * u, Color(0xFFE9E2D4), pen, shade = false)
    rmBox(u, -0.172f, -0.166f, -0.16f, 0f, 0.012f, RoomsInk.steel, pen, z = 0.135f)
    rmBox(u, -0.172f, -0.166f, -0.16f, 0f, 0.012f, RoomsInk.steel, pen, z = 0.005f)
    rmBox(u, 0.042f, -0.166f, 0.172f, -0.008f, 0.14f, white, pen, rad = 0.006f, side = Color(0xFFC5CEDA))
    for (x in floatArrayOf(0.05f, 0.162f)) drawRect(RoomsInk.rubber, o(x, -0.008f), Size(0.012f * u, 0.008f * u))
    val handles = listOf(RoomsInk.mint, RoomsInk.sun, RoomsInk.coral)
    for (k in 0 until 3) {
        val d = Rect(o(0.05f, -0.158f + k * 0.05f), o(0.164f, -0.114f + k * 0.05f))
        inkedRound(d, 0.004f * u, Color(0xFFF9FBFD), pen, shade = false)
        inkedRound(Rect(o(0.085f, d.center.y / u - 0.0045f), o(0.13f, d.center.y / u + 0.0045f)), 0.0045f * u, handles[k], pen, shade = false)
    }
    // The oak top.
    rmBox(u, -0.182f, -0.182f, 0.182f, -0.166f, 0.16f, RoomsInk.oak, pen, rad = 0.004f, top = RoomsInk.oak.lighten(0.2f), side = RoomsInk.oakDark)
    for (k in 0 until 3) {
        val z = 0.035f + k * 0.045f
        drawLine(RoomsInk.oakDark.copy(alpha = 0.5f), rp(u, -0.15f + k * 0.07f, -0.182f, z), rp(u, 0.02f + k * 0.07f, -0.182f, z), pen.lw * 0.5f, StrokeCap.Round)
    }
    // Keyboard and mouse.
    val kb = rmRQuad(rp(u, -0.16f, -0.182f, 0.02f), rp(u, -0.16f, -0.182f, 0.058f), rp(u, -0.035f, -0.182f, 0.058f), rp(u, -0.035f, -0.182f, 0.02f), 0.004f * u)
    rmFace(kb, Color(0xFFE4E8EF), pen, pen.thin)
    val keys = ArrayList<Offset>(40)
    for (row in 0 until 3) for (col in 0 until 9) keys.add(rp(u, -0.15f + col * 0.0125f, -0.182f, 0.028f + row * 0.011f))
    drawPoints(keys, PointMode.Points, Color(0xFF9AA4B5), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
    val mouse = rp(u, 0.0f, -0.182f, 0.04f)
    inkedOval(Rect(mouse.x - 0.011f * u, mouse.y - 0.007f * u, mouse.x + 0.011f * u, mouse.y + 0.007f * u), Color(0xFFE4E8EF), pen, shade = false)
    // The monitor with a heartbeat running across it.
    val base = rp(u, -0.09f, -0.182f, 0.11f)
    rmFace(rmDisc(base.x, base.y, 0.03f * u, 0.022f * u), Color(0xFF4A4A56), pen)
    rmBox(u, -0.096f, -0.23f, -0.084f, -0.182f, 0.01f, Color(0xFF4A4A56), pen, z = 0.105f)
    rmBox(u, -0.158f, -0.322f, -0.022f, -0.222f, 0.012f, Color(0xFF3A3A46), pen, rad = 0.008f, z = 0.098f)
    val scr = Rect(rp(u, -0.15f, -0.314f, 0.098f), rp(u, -0.03f, -0.232f, 0.098f))
    val glowA = 0.35f + 0.4f * pen.night
    rmGlow(scr.center, 0.12f * u, RoomsInk.green, glowA * 0.35f)
    drawRoundRect(Brush.verticalGradient(0f to Color(0xFF123A40), 1f to Color(0xFF0B2228), startY = scr.top, endY = scr.bottom), scr.topLeft, scr.size, CornerRadius(0.004f * u))
    clipRect(scr.left, scr.top, scr.right, scr.bottom) {
        val grid = ArrayList<Offset>(16)
        for (k in 1..5) {
            val x = scr.left + scr.width * k / 6f
            grid.add(Offset(x, scr.top))
            grid.add(Offset(x, scr.bottom))
        }
        drawPoints(grid, PointMode.Lines, RoomsInk.green.copy(alpha = 0.1f), strokeWidth = pen.lw * 0.4f)
        val n = 64
        val base0 = scr.top + scr.height * 0.66f
        val amp = scr.height * 0.42f * (1f + a * 0.6f)
        val sweep = rmFrac(t * 0.42f)
        val trace = Path()
        for (i in 0..n) {
            val p = i / n.toFloat()
            val x = scr.left + p * scr.width
            val y = base0 - rmEcg(rmFrac(p * 2.2f)) * amp
            if (i == 0) trace.moveTo(x, y) else trace.lineTo(x, y)
        }
        drawPath(trace, RoomsInk.green.copy(alpha = 0.3f), style = Stroke(pen.lw * 0.8f, join = StrokeJoin.Round))
        clipRect(scr.left + (sweep - 0.4f) * scr.width, scr.top, scr.left + sweep * scr.width, scr.bottom) {
            drawPath(trace, RoomsInk.green, style = Stroke(pen.lw * 1.1f, join = StrokeJoin.Round))
        }
        if (sweep < 0.4f) {
            clipRect(scr.left + (sweep + 0.6f) * scr.width, scr.top, scr.right, scr.bottom) {
                drawPath(trace, RoomsInk.green, style = Stroke(pen.lw * 1.1f, join = StrokeJoin.Round))
            }
        }
        val cx = scr.left + sweep * scr.width
        val cy = base0 - rmEcg(rmFrac(sweep * 2.2f)) * amp
        rmGlow(Offset(cx, cy), 0.012f * u, Color.White, 0.9f)
        drawCircle(Color.White, pen.lw * 0.9f, Offset(cx, cy))
        // Beating heart and three little vitals bars.
        val beat = exp(-rmFrac(sweep * 2.2f + 0.68f) * 7f)
        val hs = scr.height * 0.26f * (1f + 0.25f * beat + 0.4f * a)
        val hc = Offset(scr.right - scr.height * 0.2f, scr.top + scr.height * 0.22f)
        drawPath(rmHeart(hc.x, hc.y, hs), Color(0xFFFF5A6E))
        drawPath(rmPlus(Offset(hc.x, hc.y - hs * 0.04f), hs * 0.42f), Color.White)
        for (k in 0 until 3) {
            val w = scr.width * (0.12f + 0.08f * k)
            drawRoundRect(listOf(RoomsInk.sun, RoomsInk.sky, RoomsInk.pink)[k].copy(alpha = 0.85f), Offset(scr.left + scr.width * 0.06f, scr.top + scr.height * (0.12f + k * 0.12f)), Size(w, scr.height * 0.06f), CornerRadius(scr.height * 0.03f))
        }
    }
    drawRoundRect(Ink.line, scr.topLeft, scr.size, CornerRadius(0.004f * u), style = pen.thin)
    shine(Offset(scr.left + scr.width * 0.12f, scr.top + scr.height * 0.12f), scr.width * 0.08f, scr.height * 0.05f, 0.3f)
    // A cup of pens with a reflex hammer sticking out.
    val cup = rp(u, 0.115f, -0.182f, 0.1f)
    val cr = 0.019f * u
    val top = cup.y - 0.045f * u
    rmFace(rmDisc(cup.x, top, cr, cr), Color(0xFF3A3040), pen)
    rmTube(Offset(cup.x - 0.006f * u, top), Offset(cup.x - 0.02f * u, top - 0.04f * u), 0.006f * u, RoomsInk.sun, pen)
    drawLine(Color(0xFFFFA3B5), Offset(cup.x - 0.019f * u, top - 0.037f * u), Offset(cup.x - 0.02f * u, top - 0.04f * u), 0.006f * u, StrokeCap.Round)
    rmTube(Offset(cup.x + 0.002f * u, top), Offset(cup.x + 0.006f * u, top - 0.045f * u), 0.005f * u, RoomsInk.fjord, pen)
    rmTube(Offset(cup.x + 0.008f * u, top), Offset(cup.x + 0.018f * u, top - 0.05f * u), 0.004f * u, RoomsInk.steel, pen)
    rotate(-20f, pivot = Offset(cup.x + 0.018f * u, top - 0.052f * u)) {
        val tri = Path().apply {
            moveTo(cup.x + 0.004f * u, top - 0.058f * u)
            lineTo(cup.x + 0.036f * u, top - 0.058f * u)
            lineTo(cup.x + 0.02f * u, top - 0.045f * u)
            close()
        }
        inked(tri, RoomsInk.red, pen, shade = false)
    }
    rmCyl(cup.x, cup.y, top, cr, RoomsInk.teal, pen, cap = false)
    drawArc(Ink.line, 150f, 240f, false, Offset(cup.x - cr * 1.12f, top - cr * 0.62f), Size(cr * 2.24f, cr * 1.24f), style = pen.stroke)
}

/** An ECG trace for one beat, [p] from 0 to 1: flat, a small bump, a tall spike, a dip and a bump. */
private fun rmEcg(p: Float): Float = when {
    p < 0.1f -> 0f
    p < 0.2f -> sin((p - 0.1f) / 0.1f * RM_PI) * 0.12f
    p < 0.28f -> 0f
    p < 0.31f -> -(p - 0.28f) / 0.03f * 0.2f
    p < 0.36f -> -0.2f + (p - 0.31f) / 0.05f * 1.2f
    p < 0.41f -> 1f - (p - 0.36f) / 0.05f * 1.35f
    p < 0.45f -> -0.35f + (p - 0.41f) / 0.04f * 0.35f
    p < 0.55f -> 0f
    p < 0.72f -> sin((p - 0.55f) / 0.17f * RM_PI) * 0.22f
    else -> 0f
}

// ============================================================================================= STAGE

private fun DrawScope.rmStage(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val d = 0.1f
    val wood = Color(0xFFD9A066)
    val skirt = Color(0xFF3E2C74)
    rmShadow(u, 1.5f, d, alpha = 0.8f)
    // The far end and the planked top.
    rmFace(rmQuad(rp(u, 0.75f, -0.12f, 0f), rp(u, 0.75f, -0.12f, d), rp(u, 0.75f, 0f, d), rp(u, 0.75f, 0f, 0f)), skirt.darken(0.3f), pen)
    val topFace = rmQuad(rp(u, -0.75f, -0.12f, 0f), rp(u, -0.75f, -0.12f, d), rp(u, 0.75f, -0.12f, d), rp(u, 0.75f, -0.12f, 0f))
    drawPath(topFace, Brush.verticalGradient(0f to wood.darken(0.12f), 1f to wood.lighten(0.1f), startY = (-0.12f + ODY * d) * u, endY = -0.12f * u))
    val seams = ArrayList<Offset>(64)
    for (row in 1..3) {
        val z = row * d / 4f
        seams.add(rp(u, -0.75f, -0.12f, z))
        seams.add(rp(u, 0.75f, -0.12f, z))
    }
    for (row in 0 until 4) {
        val z0 = row * d / 4f
        var x = -0.75f + 0.12f + row * 0.11f
        while (x < 0.72f) {
            seams.add(rp(u, x, -0.12f, z0))
            seams.add(rp(u, x, -0.12f, z0 + d / 4f))
            x += 0.42f
        }
    }
    drawPoints(seams, PointMode.Lines, wood.darken(0.35f), strokeWidth = pen.lw * 0.6f)
    drawPath(topFace, Ink.line, style = pen.stroke)
    // Nosing and the pleated skirt.
    inkedRound(Rect(o(-0.75f, -0.12f), o(0.75f, -0.104f)), 0.004f * u, Color(0xFFEAB877), pen, shade = false)
    val sk = Rect(o(-0.75f, -0.104f), o(0.75f, 0f))
    drawRect(skirt, sk.topLeft, sk.size)
    var x = -0.75f
    var i = 0
    while (x < 0.75f) {
        drawRect(skirt.lighten(0.12f), o(x + 0.006f, -0.104f), Size(0.016f * u, 0.104f * u))
        drawLine(skirt.darken(0.35f), o(x + 0.036f, -0.1f), o(x + 0.036f, -0.004f), pen.lw * 0.7f)
        x += 0.05f
        i++
    }
    drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to Ink.line.copy(alpha = 0.3f), startY = -0.06f * u, endY = 0f), o(-0.75f, -0.06f), Size(1.5f * u, 0.06f * u))
    drawRect(RoomsInk.gold, o(-0.75f, -0.1f), Size(1.5f * u, 0.005f * u))
    drawRect(Ink.line, sk.topLeft, sk.size, style = pen.stroke)
    // Footlights along the front edge.
    val n = 8
    val glow = 0.45f + 0.55f * pen.night
    for (k in 0 until n) {
        val cx = -0.75f + (k + 0.5f) * 1.5f / n
        val flick = 1f + 0.06f * sin(t * 13f + k * 2.1f)
        // A soft beam of light rising up and back from each lamp.
        val beam = Path().apply {
            moveTo((cx - 0.018f) * u, -0.14f * u)
            lineTo((cx - 0.05f) * u, -0.42f * u)
            lineTo((cx + 0.11f) * u, -0.42f * u)
            lineTo((cx + 0.022f) * u, -0.14f * u)
            close()
        }
        drawPath(beam, Brush.verticalGradient(0f to RoomsInk.warm.copy(alpha = 0f), 1f to RoomsInk.warm.copy(alpha = 0.3f * glow * flick), startY = -0.42f * u, endY = -0.14f * u))
        rmGlow(o(cx + 0.01f, -0.16f), 0.1f * u, RoomsInk.warm, glow * 0.6f * flick)
        val hood = Path().apply {
            moveTo((cx - 0.032f) * u, -0.12f * u)
            lineTo((cx - 0.028f) * u, -0.134f * u)
            quadraticTo(cx * u, -0.146f * u, (cx + 0.028f) * u, -0.134f * u)
            lineTo((cx + 0.032f) * u, -0.12f * u)
            close()
        }
        inked(hood, RoomsInk.charcoal, pen)
        drawOval(RoomsInk.warm.lighten(0.3f), o(cx - 0.02f, -0.143f), Size(0.04f * u, 0.009f * u))
        drawOval(Ink.line, o(cx - 0.02f, -0.143f), Size(0.04f * u, 0.009f * u), style = pen.thin)
        rmGlow(o(cx, -0.139f), 0.03f * u, Color.White, 0.5f * glow * flick)
    }
}

private fun DrawScope.rmDrumsBack(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    // A round rug under the kit.
    val rug = rp(u, 0.01f, 0f, 0.07f)
    drawPath(rmDisc(rug.x, rug.y, 0.2f * u, 0.1f * u), Color(0xFF8E2C47))
    drawPath(rmDisc(rug.x, rug.y, 0.17f * u, 0.083f * u), Color(0xFFB8465F), style = Stroke(pen.lw * 1.3f))
    drawPath(rmDisc(rug.x, rug.y, 0.2f * u, 0.1f * u), Ink.line, style = pen.stroke)
    // Crash cymbal on the left.
    rmStandLegs(u, -0.13f, pen)
    rmTube(o(-0.13f, -0.05f), o(-0.13f, -0.25f), 0.007f * u, RoomsInk.chrome, pen)
    val wob = if (f.mode == 2) a * sin(t * 26f) * 16f else a * sin(t * 26f) * 4f
    rotate(-10f + wob, pivot = o(-0.13f, -0.262f)) { rmCymbal(o(-0.13f, -0.262f), 0.074f * u, 0.02f * u, pen) }
    // Hi-hat on the right.
    rmStandLegs(u, 0.158f, pen)
    drawRoundRect(RoomsInk.rubber, o(0.145f, -0.006f), Size(0.03f * u, 0.006f * u), CornerRadius(0.003f * u))
    rmTube(o(0.158f, -0.05f), o(0.158f, -0.238f), 0.006f * u, RoomsInk.chrome, pen)
    rmCymbal(o(0.158f, -0.214f), 0.045f * u, 0.011f * u, pen)
    rmCymbal(o(0.158f, -0.226f), 0.045f * u, 0.011f * u, pen)
    // The drummer's stool, behind the bass drum.
    rmStandLegs(u, 0.02f, pen)
    rmTube(o(0.02f, -0.05f), o(0.02f, -0.1f), 0.008f * u, RoomsInk.chrome, pen)
    rmCyl(0.02f * u, -0.098f * u, -0.112f * u, 0.036f * u, RoomsInk.rubber, pen, top = Color(0xFF4A4458))
}

private fun DrawScope.rmStandLegs(u: Float, x: Float, pen: Pen) {
    val hub = Offset(x * u, -0.05f * u)
    inkLine(hub, rp(u, x + 0.006f, 0f, 0.04f), pen, width = pen.lw * 1.3f, color = RoomsInk.chrome.darken(0.2f))
    rmTube(hub, Offset((x - 0.03f) * u, 0f), 0.005f * u, RoomsInk.chrome, pen)
    rmTube(hub, Offset((x + 0.03f) * u, 0f), 0.005f * u, RoomsInk.chrome, pen)
}

/** A brass cymbal seen almost edge-on: [rx] wide, [ry] tall, centred on [c]. */
private fun DrawScope.rmCymbal(c: Offset, rx: Float, ry: Float, pen: Pen) {
    val brass = Color(0xFFE8B84A)
    val r = Rect(c.x - rx, c.y - ry, c.x + rx, c.y + ry)
    drawOval(brass.darken(0.2f), r.topLeft, r.size)
    drawOval(Brush.horizontalGradient(0f to brass.lighten(0.35f), 0.45f to brass, 1f to brass.darken(0.15f), startX = r.left, endX = r.right), Offset(r.left, r.top), Size(r.width, r.height * 0.82f))
    for (k in 1..2) {
        val f = k / 3f
        drawOval(brass.darken(0.3f).copy(alpha = 0.6f), Offset(c.x - rx * f, c.y - ry * f), Size(rx * 2 * f, ry * 2 * f), style = Stroke(pen.lw * 0.45f))
    }
    drawOval(Ink.line, r.topLeft, r.size, style = pen.stroke)
    inkedOval(Rect(c.x - rx * 0.18f, c.y - ry * 1.1f, c.x + rx * 0.18f, c.y + ry * 0.2f), brass, pen, shade = false)
    shine(Offset(c.x - rx * 0.45f, c.y - ry * 0.25f), rx * 0.3f, ry * 0.3f, 0.7f)
}

private fun DrawScope.rmDrumsFront(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    val kick = if (f.mode == 0) a else 0f
    val snare = if (f.mode == 1) a else 0f
    val shell = Color(0xFFE23B4E)
    // Snare on its stand, sticks resting on it.
    val jump = -snare * 0.014f * u
    val shake = snare * sin(t * 50f) * 0.003f * u
    rmStandLegs(u, 0.125f, pen)
    rmTube(o(0.125f, -0.05f), o(0.125f, -0.12f), 0.006f * u, RoomsInk.chrome, pen)
    translate(shake, jump) {
        rmCyl(0.125f * u, -0.123f * u, -0.155f * u, 0.042f * u, RoomsInk.chrome, pen, top = Color(0xFFFFF7EA))
        for (k in 0 until 4) {
            val x = 0.125f - 0.036f + k * 0.024f
            drawLine(Color(0xFF8F98AA), o(x, -0.148f), o(x, -0.13f), pen.lw * 0.8f)
        }
        drawLine(Ink.line, o(0.08f, -0.152f), o(0.17f, -0.157f), pen.lw * 0.6f)
        translate(0f, -snare * 0.012f * u) {
            rmTube(o(0.1f, -0.162f), o(0.158f, -0.182f), 0.0055f * u, Color(0xFFF0CD8E), pen)
            rmTube(o(0.106f, -0.18f), o(0.152f, -0.16f), 0.0055f * u, Color(0xFFF0CD8E), pen)
        }
    }
    // Rack tom off to the left, so the drummer's face stays free.
    rmTube(o(-0.03f, -0.14f), o(-0.07f, -0.165f), 0.007f * u, RoomsInk.chrome, pen)
    rmCyl(-0.085f * u, -0.16f * u, -0.195f * u, 0.034f * u, shell, pen, top = Color(0xFFFFF7EA))
    drawPath(rmDisc(-0.085f * u, -0.195f * u, 0.034f * u, 0.034f * u), Ink.line, style = Stroke(pen.lw * 1.8f))
    // Spurs, then the bass drum with its funny face.
    for (side in RM_SIDES) rmTube(o(side * 0.046f, -0.028f), o(side * 0.08f, 0f), 0.006f * u, RoomsInk.chrome, pen)
    withTransform({ scale(1f + kick * 0.07f, 1f - kick * 0.05f, pivot = Offset(0f, 0f)) }) {
        val c = o(0f, -0.075f)
        val r = 0.071f * u
        rmShell(c, r, 0.05f * u, shell.darken(0.12f), pen)
        for (k in 0 until 7) {
            val ang = -0.3f + k * 0.5f
            val p = Offset(c.x + cos(ang) * r * 0.97f + ODX * 0.022f * u, c.y + sin(ang) * r * 0.97f + ODY * 0.022f * u)
            if (sin(ang) > 0.35f || cos(ang) > 0.1f) drawRect(RoomsInk.chrome, Offset(p.x - 0.004f * u, p.y - 0.004f * u), Size(0.008f * u, 0.008f * u))
        }
        drawCircle(Ink.line, r, c)
        drawCircle(Color(0xFFFFF6E6), r - pen.lw * 2.5f, c)
        drawCircle(shell, r - pen.lw * 1.2f, c, style = Stroke(pen.lw * 2.2f))
        for (k in 0 until 8) {
            val ang = k * RM_TAU / 8f + 0.2f
            drawCircle(RoomsInk.chrome, 0.0042f * u, Offset(c.x + cos(ang) * (r - pen.lw * 1.2f), c.y + sin(ang) * (r - pen.lw * 1.2f)))
        }
        rmDrumFace(c, r * 0.8f, kick, t, pen)
        drawCircle(Ink.line, r, c, style = pen.stroke)
        shine(Offset(c.x - r * 0.55f, c.y - r * 0.55f), r * 0.2f, r * 0.12f, 0.7f)
    }
    if (kick > 0.05f) {
        rmWaves(o(0.08f, -0.075f), 0.02f * u, 1f, pen, kick)
        rmWaves(o(-0.08f, -0.075f), 0.02f * u, -1f, pen, kick)
    }
}

/** The bass drum's logo: a grinning face that shouts with its eyes squeezed shut when kicked. */
private fun DrawScope.rmDrumFace(c: Offset, r: Float, kick: Float, t: Float, pen: Pen) {
    val loud = kick > 0.15f
    val eyeY = c.y - r * 0.28f
    for (side in RM_SIDES) {
        val e = Offset(c.x + side * r * 0.36f, eyeY)
        if (loud) {
            val p = Path().apply {
                moveTo(e.x - side * r * 0.18f, e.y - r * 0.14f)
                lineTo(e.x + side * r * 0.14f, e.y)
                lineTo(e.x - side * r * 0.18f, e.y + r * 0.14f)
            }
            drawPath(p, Ink.line, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else {
            drawCircle(Color.White, r * 0.22f, e)
            drawCircle(Ink.line, r * 0.22f, e, style = pen.stroke)
            val look = sin(t * 0.9f) * r * 0.05f
            drawCircle(Ink.line, r * 0.1f, Offset(e.x + look + side * r * 0.03f, e.y + r * 0.03f))
            drawCircle(Color.White, r * 0.035f, Offset(e.x + look + side * r * 0.03f - r * 0.03f, e.y))
        }
        drawLine(Ink.line, Offset(e.x - r * 0.16f, eyeY - r * (if (loud) 0.34f else 0.3f) + side * r * 0.03f), Offset(e.x + r * 0.16f, eyeY - r * (if (loud) 0.26f else 0.34f) - side * r * 0.03f), pen.lw * 1.3f, StrokeCap.Round)
        drawOval(Ink.blush, Offset(c.x + side * r * 0.58f - r * 0.13f, c.y + r * 0.08f), Size(r * 0.26f, r * 0.13f))
    }
    val mouth = Path()
    if (loud) {
        mouth.addOval(Rect(c.x - r * 0.3f, c.y + r * 0.02f, c.x + r * 0.3f, c.y + r * 0.62f))
    } else {
        mouth.moveTo(c.x - r * 0.46f, c.y + r * 0.1f)
        mouth.quadraticTo(c.x, c.y + r * 0.2f, c.x + r * 0.46f, c.y + r * 0.1f)
        mouth.quadraticTo(c.x + r * 0.34f, c.y + r * 0.62f, c.x, c.y + r * 0.62f)
        mouth.quadraticTo(c.x - r * 0.34f, c.y + r * 0.62f, c.x - r * 0.46f, c.y + r * 0.1f)
        mouth.close()
    }
    drawPath(mouth, Color(0xFF5A1E2E))
    clipPath(mouth) {
        drawOval(Color(0xFFFF7F9A), Offset(c.x - r * 0.22f, c.y + r * 0.36f), Size(r * 0.44f, r * 0.4f))
        if (!loud) drawRect(Color.White, Offset(c.x - r * 0.4f, c.y + r * 0.06f), Size(r * 0.8f, r * 0.13f))
    }
    drawPath(mouth, Ink.line, style = pen.stroke)
}

private fun DrawScope.rmMicStand(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    rmShadow(u, 0.08f, 0.05f)
    val black = Color(0xFF3A3746)
    // Tripod.
    val hub = o(0f, -0.036f)
    inkLine(hub, rp(u, 0.004f, 0f, 0.04f), pen, width = pen.lw * 1.4f, color = black)
    for (side in RM_SIDES) {
        rmTube(hub, o(side * 0.034f, 0f), 0.005f * u, black, pen)
        drawCircle(RoomsInk.rubber, 0.0045f * u, o(side * 0.034f, -0.001f))
    }
    // A cable curling from the mic down the pole to the floor.
    val sway = sin(t * 1.3f) * 0.003f
    val cable = Path().apply {
        moveTo(0.004f * u, -0.3f * u)
        cubicTo(0.03f * u, -0.26f * u, -0.02f * u, -0.22f * u, 0.012f * u, -0.18f * u)
        cubicTo(0.035f * u, -0.14f * u, (-0.015f + sway) * u, -0.09f * u, 0.01f * u, -0.04f * u)
        cubicTo(0.02f * u, -0.01f * u, 0.04f * u, 0.004f * u, 0.07f * u, -0.002f * u)
        cubicTo(0.09f * u, -0.006f * u, 0.085f * u, -0.02f * u, 0.075f * u, -0.016f * u)
    }
    drawPath(cable, Ink.line, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    val rot = a * sin(t * 30f) * 5f
    rotate(rot, pivot = Offset(0f, 0f)) {
        rmTube(hub, o(0f, -0.2f), 0.012f * u, black, pen)
        rmTube(o(0f, -0.2f), o(0f, -0.292f), 0.006f * u, RoomsInk.chrome, pen)
        inkedRound(Rect(o(-0.011f, -0.21f), o(0.011f, -0.194f)), 0.004f * u, RoomsInk.chrome, pen)
        rotate(32f, pivot = o(0f, -0.292f)) {
            inkedRound(Rect(o(-0.007f, -0.3f), o(0.007f, -0.284f)), 0.003f * u, black, pen, shade = false)
            val handle = Path().apply {
                moveTo(-0.006f * u, -0.298f * u)
                lineTo(0.006f * u, -0.298f * u)
                lineTo(0.009f * u, -0.338f * u)
                lineTo(-0.009f * u, -0.338f * u)
                close()
            }
            inked(handle, black, pen)
            drawRect(RoomsInk.sun, o(-0.0095f, -0.343f), Size(0.019f * u, 0.006f * u))
            drawRect(Ink.line, o(-0.0095f, -0.343f), Size(0.019f * u, 0.006f * u), style = pen.thin)
            val head = o(0f, -0.358f)
            val hr = 0.017f * u
            inkedCircle(head, hr, Color(0xFFCBD2DE), pen)
            clipPath(ovalPath(Rect(head, hr))) {
                val mesh = ArrayList<Offset>(24)
                for (k in -3..3) {
                    mesh.add(Offset(head.x + k * hr * 0.3f - hr, head.y - hr))
                    mesh.add(Offset(head.x + k * hr * 0.3f + hr, head.y + hr))
                    mesh.add(Offset(head.x + k * hr * 0.3f + hr, head.y - hr))
                    mesh.add(Offset(head.x + k * hr * 0.3f - hr, head.y + hr))
                }
                drawPoints(mesh, PointMode.Lines, Color(0xFF6B7488), strokeWidth = pen.lw * 0.45f)
            }
            drawCircle(Ink.line, hr, head, style = pen.stroke)
            shine(Offset(head.x - hr * 0.4f, head.y - hr * 0.45f), hr * 0.45f, hr * 0.3f, 0.9f)
        }
    }
    if (a > 0.05f) rmWaves(o(0.035f, -0.34f), 0.018f * u, 1f, pen, a)
}

private val RM_RAINBOW = longArrayOf(0xFFFF5A5F, 0xFFFF9F43, 0xFFFFD93D, 0xFF6BCB77, 0xFF2EC4B6, 0xFF4D96FF, 0xFF6A5ACD, 0xFFB983FF)

private fun DrawScope.rmXylophone(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    val wood = RoomsInk.wood
    rmShadow(u, 0.36f, 0.13f)
    // Two A-frame stands joined by a dowel.
    for (x in floatArrayOf(-0.145f, 0.145f)) {
        rmTube(rp(u, x, -0.112f, 0.085f), rp(u, x + 0.014f, 0f, 0.1f), 0.008f * u, wood.darken(0.1f), pen)
    }
    rmTube(rp(u, -0.145f, -0.04f, 0.05f), rp(u, 0.145f, -0.04f, 0.05f), 0.007f * u, wood.lighten(0.1f), pen)
    for (x in floatArrayOf(-0.145f, 0.145f)) {
        rmTube(o(x, -0.112f), o(x - 0.016f, 0f), 0.008f * u, wood, pen)
    }
    // Rails under the bars: the back one follows the bars' ends.
    val bw = 0.031f
    val z0 = 0.012f
    fun len(i: Int) = 0.118f - i * 0.0075f
    val zb0 = z0 + len(0) - 0.016f
    val zb1 = z0 + len(7) - 0.016f
    val rail = wood.darken(0.05f)
    rmFace(rmQuad(rp(u, -0.172f, -0.122f, zb0 - 0.008f), rp(u, -0.172f, -0.122f, zb0 + 0.008f), rp(u, 0.172f, -0.122f, zb1 + 0.008f), rp(u, 0.172f, -0.122f, zb1 - 0.008f)), rail.lighten(0.15f), pen)
    rmFace(rmQuad(rp(u, -0.172f, -0.122f, zb0 - 0.008f), rp(u, 0.172f, -0.122f, zb1 - 0.008f), rp(u, 0.172f, -0.108f, zb1 - 0.008f), rp(u, -0.172f, -0.108f, zb0 - 0.008f)), rail, pen)
    rmBox(u, -0.172f, -0.122f, 0.172f, -0.106f, 0.018f, rail, pen, z = 0.008f, rad = 0.003f)
    // Eight rainbow bars, longest (lowest) on the left.
    for (i in 0 until 8) {
        val cx = -0.153f + (i + 0.5f) * 0.03825f
        val col = Color(RM_RAINBOW[i])
        val lit = if (f.mode == i) a else 0f
        val c = lerp(col, Color.White, lit * 0.6f)
        val press = lit * 0.005f
        rmBox(u, cx - bw / 2f, -0.136f + press, cx + bw / 2f, -0.124f + press, len(i), c, pen, z = z0, rad = 0.003f, top = c.lighten(0.2f), side = c.darken(0.28f), shade = false)
        drawCircle(RoomsInk.chrome, 0.0032f * u, rp(u, cx, -0.136f + press, z0 + 0.012f))
        drawCircle(RoomsInk.chrome, 0.0032f * u, rp(u, cx, -0.136f + press, z0 + len(i) - 0.014f))
        val mid = rp(u, cx, -0.136f, z0 + len(i) * 0.5f)
        drawLine(Color.White.copy(alpha = 0.5f), rp(u, cx - bw * 0.3f, -0.136f + press, z0 + 0.022f), rp(u, cx - bw * 0.3f, -0.136f + press, z0 + len(i) - 0.024f), pen.lw * 0.7f, StrokeCap.Round)
        if (lit > 0.02f) {
            rmGlow(mid, 0.09f * u, col.lighten(0.3f), lit)
            twinkle(Offset(mid.x - 0.02f * u, mid.y - 0.02f * u), 0.018f * u * lit, Color.White, lit)
            twinkle(Offset(mid.x + 0.03f * u, mid.y - 0.035f * u), 0.011f * u * lit, Color.White, lit)
            rmNote(Offset(mid.x + 0.004f * u, mid.y - (0.05f + (1f - lit) * 0.06f) * u), 0.013f * u, col, lit)
        }
    }
    // Two mallets hang on the right stand.
    inkLine(o(0.147f, -0.1f), o(0.162f, -0.1f), pen, width = pen.lw * 1.2f)
    val swing = sin(t * 1.1f) * 0.002f
    rmTube(o(0.158f, -0.1f), o(0.166f + swing, -0.036f), 0.004f * u, Color(0xFFF0CD8E), pen)
    rmTube(o(0.162f, -0.1f), o(0.186f + swing, -0.044f), 0.004f * u, Color(0xFFF0CD8E), pen)
    inkedCircle(o(0.167f + swing, -0.03f), 0.011f * u, Color(0xFFFF5A5F), pen)
    inkedCircle(o(0.188f + swing, -0.038f), 0.011f * u, Color(0xFF4D96FF), pen)
}

private fun DrawScope.rmSpeaker(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    val retro = f.variant % 2 == 1
    val cab = if (retro) Color(0xFFA9683A) else Color(0xFF2E2B38)
    val baffle = if (retro) Color(0xFFF1E3C8) else Color(0xFF3B3848)
    val cone = if (retro) Color(0xFFE8963F) else Color(0xFF55526A)
    rmShadow(u, 0.2f, 0.14f)
    for (x in floatArrayOf(-0.075f, 0.065f)) drawRoundRect(RoomsInk.rubber, o(x, -0.013f), Size(0.012f * u, 0.013f * u), CornerRadius(0.003f * u))
    drawRoundRect(RoomsInk.rubber, rp(u, 0.065f, -0.013f, 0.11f), Size(0.012f * u, 0.013f * u), CornerRadius(0.003f * u))
    rmBox(u, -0.09f, -0.34f, 0.09f, -0.012f, 0.13f, cab, pen, rad = 0.012f, top = cab.lighten(if (retro) 0.14f else 0.1f), side = cab.darken(0.25f))
    // Handle recess on the side.
    val hs = rmQuad(rp(u, 0.09f, -0.24f, 0.04f), rp(u, 0.09f, -0.24f, 0.09f), rp(u, 0.09f, -0.22f, 0.09f), rp(u, 0.09f, -0.22f, 0.04f))
    rmFace(hs, cab.darken(0.5f), pen, pen.thin)
    if (retro) {
        for (k in 0 until 3) drawLine(cab.darken(0.2f).copy(alpha = 0.6f), rp(u, -0.07f + k * 0.06f, -0.34f, 0.02f), rp(u, -0.04f + k * 0.06f, -0.34f, 0.11f), pen.lw * 0.5f)
    }
    val bf = Rect(o(-0.077f, -0.328f), o(0.077f, -0.024f))
    inkedRound(bf, 0.008f * u, baffle, pen, shade = false)
    if (!retro) {
        // Chrome corner guards.
        for (sx in RM_SIDES) for (sy in RM_SIDES) {
            val cx = if (sx < 0) -0.09f else 0.09f
            val cy = if (sy < 0) -0.34f else -0.012f
            val g = Rect(o(cx - (if (sx < 0) 0f else 0.022f), cy - (if (sy < 0) 0f else 0.022f)), o(cx + (if (sx < 0) 0.022f else 0f), cy + (if (sy < 0) 0.022f else 0f)))
            inkedRound(g, 0.006f * u, RoomsInk.chrome, pen, shade = false)
            drawCircle(Color(0xFF8F98AA), 0.0025f * u, g.center)
        }
    }
    // A gentle thump with the music, a big pump on a tap.
    val beat = if (t == 0f) 0f else exp(-rmFrac(t * 2f) * 8f)
    val pump = 1f + a * 0.12f + beat * 0.025f
    val wc = o(0f, -0.108f)
    val fr = 0.068f * u
    inkedCircle(wc, fr, if (retro) Color(0xFF6E4630) else Color(0xFF232129), pen, shade = false)
    for (k in 0 until 6) {
        val ang = k * RM_TAU / 6f + 0.5f
        drawCircle(RoomsInk.chrome, 0.0032f * u, Offset(wc.x + cos(ang) * fr * 0.9f, wc.y + sin(ang) * fr * 0.9f))
    }
    val sr = 0.058f * u * pump
    drawCircle(if (retro) Color(0xFF3A2A20) else Color(0xFF15141A), sr, wc)
    drawCircle(Ink.line, sr, wc, style = pen.thin)
    val cr = 0.05f * u * pump
    drawCircle(safeRadialGradient(0f to cone.lighten(0.25f), 0.7f to cone, 1f to cone.darken(0.3f), center = Offset(wc.x - cr * 0.2f, wc.y - cr * 0.25f), radius = cr * 1.2f), cr, wc)
    for (k in 1..2) drawCircle(cone.darken(0.25f), cr * (0.4f + k * 0.2f), wc, style = Stroke(pen.lw * 0.5f))
    drawCircle(Ink.line, cr, wc, style = pen.thin)
    val cap = 0.02f * u * (1f + a * 0.25f + beat * 0.05f)
    inkedCircle(wc, cap, if (retro) Color(0xFF8A4A1E) else Color(0xFF1D1C24), pen)
    shine(Offset(wc.x - cap * 0.35f, wc.y - cap * 0.4f), cap * 0.5f, cap * 0.32f, 0.8f)
    // Bass port.
    inkedRound(Rect(o(-0.048f, -0.214f), o(0.048f, -0.194f)), 0.01f * u, Color(0xFF15141A), pen, shade = false)
    // Tweeter.
    val tc = o(0f, -0.272f)
    inkedCircle(tc, 0.034f * u, if (retro) Color(0xFF6E4630) else Color(0xFF232129), pen, shade = false)
    drawCircle(Ink.line, 0.024f * u, tc, style = pen.thin)
    inkedCircle(tc, 0.017f * u, if (retro) RoomsInk.gold else Color(0xFFD5DBE6), pen)
    shine(Offset(tc.x - 0.006f * u, tc.y - 0.006f * u), 0.007f * u, 0.005f * u)
    // Power light.
    val led = o(-0.058f, -0.312f)
    val ledCol = if (retro) Color(0xFFFFB347) else Color(0xFF5AD8FF)
    rmGlow(led, 0.014f * u, ledCol, 0.5f + 0.4f * pen.night)
    drawCircle(ledCol, 0.004f * u, led)
    if (a > 0.05f) {
        rmWaves(Offset(wc.x + 0.075f * u, wc.y), 0.02f * u, 1f, pen, a)
        rmWaves(Offset(wc.x - 0.075f * u, wc.y), 0.02f * u, -1f, pen, a)
    }
}

private fun DrawScope.rmDiscoBall(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val c = o(0f, -0.07f)
    val r = 0.055f * u
    val on = f.on
    // Wire up to the ceiling, and a little cap.
    drawLine(Ink.line, o(0f, -0.62f), o(0f, -0.13f), pen.lw * 1.1f)
    inkedRound(Rect(o(-0.011f, -0.136f), o(0.011f, -0.122f)), 0.004f * u, RoomsInk.chrome, pen)
    if (on) rmGlow(c, r * 2.8f, Color(0xFFD9A6FF), 0.22f + 0.3f * pen.night)
    drawCircle(Color(0xFF4A5063), r, c)
    val ball = ovalPath(Rect(c, r))
    val spin = f.angle
    val lx = -0.45f
    val ly = -0.55f
    val lz = 0.7f
    val party = listOf(Color(0xFFFF6FA8), Color(0xFF5AD8FF), Color(0xFFFFD23F), Color(0xFF8BF5A8))
    clipPath(ball) {
        val rows = 8
        val cols = 16
        val gap = max(0.6f, pen.lw * 0.35f)
        for (j in 0 until rows) {
            val lat0 = -RM_PI / 2f + j * RM_PI / rows
            val lat1 = lat0 + RM_PI / rows
            val latM = (lat0 + lat1) / 2f
            val y0 = c.y + r * sin(lat0)
            val y1 = c.y + r * sin(lat1)
            val rr = r * cos(latM)
            for (k in 0 until cols) {
                val p0 = spin + k * RM_TAU / cols
                val p1 = p0 + RM_TAU / cols
                val pm = (p0 + p1) / 2f
                if (cos(pm) < -0.05f) continue
                val x0 = rr * sin(p0)
                val x1 = rr * sin(p1)
                val nx = cos(latM) * sin(pm)
                val ny = sin(latM)
                val nz = cos(latM) * cos(pm)
                val d = (nx * lx + ny * ly + nz * lz).coerceIn(-1f, 1f)
                var col = lerp(Color(0xFF6B7488), Color(0xFFF2F6FC), ((d + 0.3f) / 1.25f).coerceIn(0f, 1f))
                val h = rmHash(j * 31 + k, 7)
                if (on && h < 0.22f) col = lerp(col, party[(k + j + (t * 2f).toInt()) % party.size], 0.55f)
                if (d > 0.9f) col = Color.White
                val left = c.x + min(x0, x1)
                val w = abs(x1 - x0)
                if (w <= gap * 2f) continue
                drawRect(col, Offset(left + gap, y0 + gap), Size(w - gap * 2f, y1 - y0 - gap * 2f))
            }
        }
        drawCircle(
            safeRadialGradient(0f to Color.Transparent, 0.75f to Color.Transparent, 1f to Ink.line.copy(alpha = 0.35f), center = Offset(c.x - r * 0.3f, c.y - r * 0.35f), radius = r * 1.5f),
            r,
            c,
        )
    }
    drawCircle(Ink.line, r, c, style = pen.stroke)
    shine(Offset(c.x - r * 0.42f, c.y - r * 0.5f), r * 0.28f, r * 0.18f, 0.9f)
    val gl = 0.5f + 0.5f * sin(t * 3f + spin * 2f)
    twinkle(Offset(c.x - r * 0.3f, c.y - r * 0.2f), r * 0.35f * gl, Color.White, gl)
    if (on) {
        for (k in 0 until 6) {
            val ang = spin * 0.8f + k * RM_TAU / 6f
            val rad = r * (1.5f + 0.5f * rmHash(k, 3))
            val p = Offset(c.x + cos(ang) * rad * 1.3f, c.y + sin(ang) * rad * 0.9f)
            val s = 0.5f + 0.5f * sin(t * 5f + k * 1.7f)
            twinkle(p, r * 0.28f * (0.5f + s * 0.5f), party[k % party.size], 0.4f + 0.6f * s)
        }
    }
}

private fun DrawScope.rmSmokeMachine(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    rmShadow(u, 0.13f, 0.08f, x = -0.01f)
    translate(sin(t * 55f) * a * 0.003f * u, 0f) {
        for (x in floatArrayOf(-0.058f, 0.03f)) drawRoundRect(RoomsInk.rubber, o(x, -0.012f), Size(0.012f * u, 0.012f * u), CornerRadius(0.003f * u))
        val body = Color(0xFF3A3746)
        rmBox(u, -0.068f, -0.08f, 0.046f, -0.01f, 0.07f, body, pen, rad = 0.008f, top = body.lighten(0.15f), side = body.darken(0.25f))
        // A window on the side showing the fog juice.
        val win = rmQuad(rp(u, 0.046f, -0.064f, 0.015f), rp(u, 0.046f, -0.064f, 0.055f), rp(u, 0.046f, -0.026f, 0.055f), rp(u, 0.046f, -0.026f, 0.015f))
        drawPath(win, Color(0xFF1C1B24))
        clipPath(win) {
            val lvl = -0.045f + sin(t * 2f) * 0.002f
            drawPath(rmQuad(rp(u, 0.046f, lvl, 0.015f), rp(u, 0.046f, lvl, 0.055f), rp(u, 0.046f, -0.02f, 0.055f), rp(u, 0.046f, -0.02f, 0.015f)), Color(0xFF7FD4FF))
        }
        drawPath(win, Ink.line, style = pen.thin)
        drawRect(Color(0xFFB06CFF), o(-0.062f, -0.074f), Size(0.102f * u, 0.006f * u))
        // Vents, knobs, a cloud sticker and a light.
        for (k in 0 until 4) {
            val y = -0.058f + k * 0.009f
            drawLine(Color(0xFF15141A), o(-0.058f, y), o(-0.022f, y), pen.lw * 1.1f, StrokeCap.Round)
            drawLine(body.lighten(0.25f), o(-0.058f, y + 0.003f), o(-0.022f, y + 0.003f), pen.lw * 0.5f, StrokeCap.Round)
        }
        val cloud = Path().apply {
            addOval(Rect(o(-0.004f, -0.062f), o(0.012f, -0.05f)))
            addOval(Rect(o(0.004f, -0.068f), o(0.022f, -0.052f)))
            addOval(Rect(o(0.014f, -0.063f), o(0.03f, -0.05f)))
            addRect(Rect(o(0.002f, -0.057f), o(0.026f, -0.05f)))
        }
        drawPath(cloud, Color.White)
        for ((i, kc) in listOf(RoomsInk.coral, RoomsInk.sun).withIndex()) {
            val kp = o(-0.002f + i * 0.022f, -0.028f)
            inkedCircle(kp, 0.0075f * u, kc, pen)
            val ang = -1.2f + i * 0.9f + a * 3f
            drawLine(Ink.line, kp, Offset(kp.x + cos(ang) * 0.006f * u, kp.y + sin(ang) * 0.006f * u), pen.lw * 0.7f, StrokeCap.Round)
        }
        val led = o(0.036f, -0.06f)
        val ledCol = if (a > 0.1f) Color(0xFFFF5A4E) else RoomsInk.green
        rmGlow(led, 0.012f * u, ledCol, 0.6f)
        drawCircle(ledCol, 0.0035f * u, led)
        // Handle on top.
        val h0 = rp(u, -0.045f, -0.08f, 0.035f)
        val h1 = rp(u, 0.025f, -0.08f, 0.035f)
        val arc = Path().apply {
            moveTo(h0.x, h0.y)
            cubicTo(h0.x, h0.y - 0.026f * u, h1.x, h1.y - 0.026f * u, h1.x, h1.y)
        }
        drawPath(arc, Ink.line, style = Stroke(0.007f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(arc, RoomsInk.chrome, style = Stroke(0.007f * u, cap = StrokeCap.Round))
        // The nozzle, pointing right.
        rmTube(o(0.038f, -0.046f), o(0.062f, -0.05f), 0.013f * u, RoomsInk.chrome, pen)
        inkedOval(rect(0.064f * u, -0.05f * u, 0.009f * u, 0.017f * u), Color(0xFF1C1B24), pen, shade = false)
        if (a > 0.05f) rmGlow(o(0.068f, -0.05f), 0.03f * u, Color.White, a * 0.8f)
    }
    // A lazy wisp left over from the last puff.
    if (t != 0f) {
        for (k in 0 until 3) {
            val ph = rmFrac(t * 0.3f + k / 3f)
            val al = 0.22f * (1f - ph) * min(1f, ph * 5f)
            drawCircle(Color.White.copy(alpha = al), (0.006f + ph * 0.012f) * u, o(0.074f + ph * 0.03f, -0.052f - ph * 0.03f + sin(ph * 6f + k) * 0.004f))
        }
    }
}

// ========================================================================================= SEA FLOOR

private fun DrawScope.rmShipwreck(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val hullCol = Color(0xFF8C6A4C)
    val hullDark = Color(0xFF5E4632)
    val deckCol = Color(0xFFA8855F)
    val patina = Color(0xFF6DB5A0)
    val depth = 0.12f
    rmShadow(u, 0.86f, depth, alpha = 0.7f)
    // Tall seaweed behind the ship.
    rmWeed(o(-0.3f, -0.02f), 0.26f * u, t, 1, Color(0xFF3E8E5A), pen)
    rmWeed(o(0.18f, -0.03f), 0.3f * u, t, 2, Color(0xFF4FA36A), pen)
    // The side of the hull: a raised stern on the left, the sheer rising to the prow on the right.
    val hull = Path().apply {
        moveTo(-0.405f * u, -0.445f * u)
        lineTo(-0.35f * u, -0.445f * u)
        lineTo(-0.35f * u, -0.36f * u)
        lineTo(0.2f * u, -0.36f * u)
        cubicTo(0.3f * u, -0.36f * u, 0.38f * u, -0.375f * u, 0.43f * u, -0.435f * u)
        cubicTo(0.43f * u, -0.3f * u, 0.4f * u, -0.12f * u, 0.3f * u, -0.02f * u)
        lineTo(-0.28f * u, -0.02f * u)
        quadraticTo(-0.4f * u, -0.12f * u, -0.405f * u, -0.445f * u)
        close()
    }
    withTransform({ rotate(-2.5f, pivot = o(0f, -0.2f)) }) {
        // The hull's depth, then the deck narrowing to the bow, its far rail and the broken mast.
        rmExtrude(hull, depth * u, Color(0xFF6E5238), pen)
        val deck = Path().apply {
            val a = rp(u, -0.35f, -0.36f, 0f)
            val b = rp(u, 0.2f, -0.36f, 0f)
            val c = rp(u, 0.39f, -0.372f, depth * 0.5f)
            val d = rp(u, 0.2f, -0.36f, depth)
            val e = rp(u, -0.35f, -0.36f, depth)
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            quadraticTo(rp(u, 0.33f, -0.365f, 0.01f).x, rp(u, 0.33f, -0.365f, 0.01f).y, c.x, c.y)
            quadraticTo(rp(u, 0.33f, -0.365f, depth - 0.01f).x, rp(u, 0.33f, -0.365f, depth - 0.01f).y, d.x, d.y)
            lineTo(e.x, e.y)
            close()
        }
        drawPath(deck, deckCol)
        clipPath(deck) {
            val planks = ArrayList<Offset>(24)
            for (k in 1..3) {
                planks.add(rp(u, -0.35f, -0.36f, k * depth / 4f))
                planks.add(rp(u, 0.42f, -0.36f, k * depth / 4f))
            }
            drawPoints(planks, PointMode.Lines, hullDark, strokeWidth = pen.lw * 0.6f)
            drawPath(rmQuad(rp(u, -0.12f, -0.36f, 0.06f), rp(u, -0.12f, -0.36f, 0.09f), rp(u, -0.04f, -0.36f, 0.09f), rp(u, -0.04f, -0.36f, 0.06f)), Color(0xFF2A2230))
        }
        drawPath(deck, Ink.line, style = pen.stroke)
        for (k in 0 until 9) {
            if (k == 3 || k == 7) continue
            val x = -0.33f + k * 0.066f
            val top = if (k == 4) -0.38f else -0.395f
            rmTube(rp(u, x, -0.36f, depth - 0.008f), rp(u, x, top, depth - 0.008f), 0.006f * u, deckCol.lighten(0.05f), pen)
        }
        rmTube(rp(u, -0.33f, -0.395f, depth - 0.008f), rp(u, -0.13f, -0.395f, depth - 0.008f), 0.006f * u, deckCol, pen)
        rmTube(rp(u, -0.1f, -0.395f, depth - 0.008f), rp(u, 0.0f, -0.382f, depth - 0.008f), 0.006f * u, deckCol, pen)
        rmTube(rp(u, 0.07f, -0.395f, depth - 0.008f), rp(u, 0.2f, -0.395f, depth - 0.008f), 0.006f * u, deckCol, pen)
        rmMast(u, t, pen)
        // The hull front with planks, a wale, algae and barnacles.
        drawPath(hull, hullCol)
        clipPath(hull) {
            drawRect(Brush.verticalGradient(0f to hullCol.lighten(0.1f), 1f to hullCol.darken(0.1f), startY = -0.44f * u, endY = 0f), o(-0.45f, -0.45f), Size(0.9f * u, 0.45f * u))
            val lines = Path()
            for (k in 0 until 8) {
                val y = -0.315f + k * 0.04f
                lines.moveTo(-0.45f * u, y * u)
                lines.quadraticTo(0f, (y + 0.012f) * u, 0.45f * u, (y - 0.01f) * u)
            }
            drawPath(lines, hullDark.copy(alpha = 0.7f), style = Stroke(pen.lw * 0.7f))
            val joints = ArrayList<Offset>(24)
            for (k in 0 until 8) {
                val y = -0.315f + k * 0.04f
                var x = -0.36f + (k % 3) * 0.09f
                while (x < 0.4f) {
                    joints.add(o(x, y + 0.004f))
                    joints.add(o(x, y + 0.036f))
                    x += 0.27f
                }
            }
            drawPoints(joints, PointMode.Lines, hullDark.copy(alpha = 0.6f), strokeWidth = pen.lw * 0.6f)
            drawRect(Brush.verticalGradient(0f to Color(0x005FA36A), 1f to Color(0xAA5FA36A), startY = -0.2f * u, endY = -0.02f * u), o(-0.45f, -0.2f), Size(0.9f * u, 0.18f * u))
            drawRect(hullDark, o(-0.45f, -0.35f), Size(0.9f * u, 0.016f * u))
            drawLine(hullCol.lighten(0.2f), o(-0.45f, -0.35f), o(0.45f, -0.35f), pen.lw * 0.6f)
            for (k in 0 until 14) drawCircle(Color(0xFF3A2C22), 0.0028f * u, o(-0.33f + k * 0.052f, -0.342f))
            drawRect(Brush.verticalGradient(0f to Color(0x995FA36A), 1f to Color(0x005FA36A), startY = -0.36f * u, endY = -0.32f * u), o(-0.45f, -0.36f), Size(0.9f * u, 0.04f * u))
        }
        drawPath(hull, Ink.line, style = pen.stroke)
        // The bowsprit, snapped off.
        val sprit = Path().apply {
            moveTo(0.405f * u, -0.43f * u)
            lineTo(0.49f * u, -0.478f * u)
            lineTo(0.482f * u, -0.462f * u)
            lineTo(0.497f * u, -0.46f * u)
            lineTo(0.486f * u, -0.447f * u)
            lineTo(0.415f * u, -0.41f * u)
            close()
        }
        inked(sprit, Color(0xFF7A5A3E), pen)
        rmBarnacles(o(-0.31f, -0.1f), u, pen)
        rmBarnacles(o(0.27f, -0.13f), u, pen)
        rmBarnacles(o(-0.18f, -0.3f), u, pen)
        // Portholes; a crab lives in the right one.
        for ((px, py) in listOf(-0.245f to -0.25f, 0.225f to -0.245f)) {
            val c = o(px, py)
            drawCircle(Color(0xFF1B2A44), 0.024f * u, c)
            drawCircle(patina, 0.028f * u, c, style = Stroke(0.009f * u))
            drawCircle(Ink.line, 0.0325f * u, c, style = pen.stroke)
            drawCircle(Ink.line, 0.0235f * u, c, style = pen.thin)
            drawLine(Color.White.copy(alpha = 0.5f), Offset(c.x - 0.012f * u, c.y - 0.004f * u), Offset(c.x - 0.004f * u, c.y - 0.013f * u), pen.lw, StrokeCap.Round)
        }
        rmCrab(o(0.225f, -0.245f), u, t, pen)
        // The hole in the hull: boarded up, or open to a dark hold with treasure.
        if (f.open) rmWreckHoleOpen(u, t, pen, hullCol) else rmWreckHoleShut(u, pen)
        // A rusty anchor leaning on the bow, its chain up to the hawse hole.
        drawCircle(Color(0xFF2A2230), 0.009f * u, o(0.35f, -0.31f))
        val chain = Path().apply {
            moveTo(0.35f * u, -0.305f * u)
            quadraticTo(0.4f * u, -0.2f * u, 0.37f * u, -0.13f * u)
        }
        drawPath(chain, Ink.line, style = Stroke(pen.lw * 2.8f, cap = StrokeCap.Round))
        drawPath(chain, Color(0xFF7A7686), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Butt, pathEffect = PathEffect.dashPathEffect(floatArrayOf(pen.lw * 2f, pen.lw * 1.4f))))
        rmAnchor(o(0.37f, -0.05f), 0.085f * u, pen)
        // A tattered flag on the stern post.
        rmTube(o(-0.39f, -0.44f), o(-0.39f, -0.51f), 0.006f * u, hullDark, pen)
        val wave = sin(t * 1.8f) * 0.006f
        val flag = Path().apply {
            moveTo(-0.387f * u, -0.508f * u)
            quadraticTo(-0.36f * u, (-0.515f + wave) * u, -0.33f * u, (-0.5f - wave) * u)
            lineTo(-0.34f * u, (-0.49f - wave) * u)
            lineTo(-0.33f * u, (-0.478f - wave) * u)
            quadraticTo(-0.36f * u, (-0.478f + wave) * u, -0.387f * u, -0.48f * u)
            close()
        }
        drawPath(flag, Color(0xFFE8E0CC))
        clipPath(flag) { drawRect(Color(0xFFD2443A).copy(alpha = 0.8f), o(-0.39f, -0.5f), Size(0.07f * u, 0.009f * u)) }
        drawPath(flag, Ink.line, style = pen.thin)
    }
    // Sand heaped round the keel, with shells and tufts.
    val sand = Path().apply {
        moveTo(-0.47f * u, 0.004f * u)
        cubicTo(-0.44f * u, -0.03f * u, -0.36f * u, -0.05f * u, -0.25f * u, -0.045f * u)
        cubicTo(-0.12f * u, -0.04f * u, -0.05f * u, -0.03f * u, 0.06f * u, -0.04f * u)
        cubicTo(0.2f * u, -0.055f * u, 0.36f * u, -0.05f * u, 0.47f * u, 0.004f * u)
        close()
    }
    inked(sand, RoomsInk.sand, pen)
    val ripples = Path()
    for (k in 0 until 5) {
        val x = -0.36f + k * 0.17f
        ripples.moveTo(x * u, -0.02f * u)
        ripples.quadraticTo((x + 0.03f) * u, -0.028f * u, (x + 0.06f) * u, -0.02f * u)
    }
    drawPath(ripples, RoomsInk.sandShade, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
    rmSeashell(o(-0.2f, -0.022f), u, 0, pen)
    rmSeashell(o(0.12f, -0.026f), u, 1, pen)
    for (k in 0 until 3) drawOval(Color(0xFFB8A58A), o(-0.1f + k * 0.13f, -0.012f), Size(0.012f * u, 0.007f * u))
    rmWeed(o(-0.42f, -0.004f), 0.1f * u, t, 3, Color(0xFF5DBB6A), pen)
    rmWeed(o(0.43f, -0.004f), 0.08f * u, t, 4, Color(0xFF5DBB6A), pen)
    rmBubbles(o(-0.245f, -0.28f), u, t, pen, 3, 0.2f, 11, 0.8f)
}

/** The broken mast with a spar and a torn sail, standing on the deck. */
private fun DrawScope.rmMast(u: Float, t: Float, pen: Pen) {
    val wood = Color(0xFF7A5A3E)
    val b = rp(u, -0.03f, -0.36f, 0.07f)
    val top = Offset(b.x + 0.02f * u, b.y - 0.16f * u)
    val w = 0.012f * u
    val mast = Path().apply {
        moveTo(b.x - w, b.y)
        lineTo(top.x - w, top.y + 0.006f * u)
        lineTo(top.x - w * 0.4f, top.y - 0.01f * u)
        lineTo(top.x, top.y + 0.004f * u)
        lineTo(top.x + w * 0.5f, top.y - 0.016f * u)
        lineTo(top.x + w, top.y + 0.008f * u)
        lineTo(b.x + w, b.y)
        close()
    }
    inked(mast, wood, pen)
    // A spar, broken off on the right.
    val y = top.y + 0.045f * u
    val sway = sin(t * 1.2f) * 0.006f * u
    rmTube(Offset(top.x - 0.11f * u, y + 0.008f * u), Offset(top.x + 0.06f * u, y - 0.006f * u), 0.009f * u, wood.lighten(0.08f), pen)
    val sail = Path().apply {
        moveTo(top.x - 0.1f * u, y + 0.012f * u)
        lineTo(top.x + 0.05f * u, y)
        lineTo(top.x + 0.045f * u + sway, y + 0.05f * u)
        lineTo(top.x + 0.025f * u + sway, y + 0.038f * u)
        lineTo(top.x + 0.01f * u + sway, y + 0.07f * u)
        lineTo(top.x - 0.02f * u + sway, y + 0.05f * u)
        lineTo(top.x - 0.05f * u + sway, y + 0.08f * u)
        lineTo(top.x - 0.07f * u + sway * 0.5f, y + 0.055f * u)
        lineTo(top.x - 0.095f * u + sway * 0.3f, y + 0.07f * u)
        close()
        addOval(Rect(Offset(top.x - 0.035f * u, y + 0.03f * u), 0.009f * u))
        addOval(Rect(Offset(top.x + 0.02f * u, y + 0.018f * u), 0.006f * u))
        fillType = PathFillType.EvenOdd
    }
    inked(sail, Color(0xFFE6DDC6), pen)
    val rope = Path().apply {
        moveTo(top.x - 0.004f * u, top.y + 0.01f * u)
        quadraticTo(top.x - 0.2f * u, top.y + 0.16f * u, -0.39f * u, -0.5f * u)
    }
    drawPath(rope, Color(0xFF6E5A44), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
}

private fun DrawScope.rmWreckHoleShut(u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val hole = rmHolePath(u)
    drawPath(hole, Color(0xFF15203A))
    drawPath(hole, Ink.line, style = pen.stroke)
    val board = Color(0xFFB08A62)
    val boards = listOf(Triple(-0.225f, -4f, 0.05f), Triple(-0.15f, 3f, 0.048f), Triple(-0.078f, -2f, 0.05f))
    for ((i, b) in boards.withIndex()) {
        val (yc, ang, h) = b
        rotate(ang, pivot = o(0f, yc)) {
            val r = Rect(o(-0.14f, yc - h / 2f), o(0.14f, yc + h / 2f))
            inkedRound(r, 0.006f * u, board.darken(i * 0.05f), pen)
            drawLine(board.darken(0.3f).copy(alpha = 0.6f), o(-0.12f, yc - 0.006f), o(0.05f, yc - 0.004f), pen.lw * 0.5f, StrokeCap.Round)
            drawLine(board.darken(0.3f).copy(alpha = 0.6f), o(-0.03f, yc + 0.008f), o(0.12f, yc + 0.009f), pen.lw * 0.5f, StrokeCap.Round)
            for (sx in RM_SIDES) {
                drawCircle(Color(0xFF4A4E5E), 0.004f * u, o(sx * 0.122f, yc))
                drawCircle(Color.White.copy(alpha = 0.5f), 0.0014f * u, o(sx * 0.122f - 0.001f, yc - 0.001f))
            }
        }
    }
}

private fun rmHolePath(u: Float): Path {
    val pts = floatArrayOf(
        -0.125f, -0.2f, -0.1f, -0.255f, -0.06f, -0.238f, -0.03f, -0.268f, 0.01f, -0.25f, 0.05f, -0.27f,
        0.085f, -0.245f, 0.12f, -0.258f, 0.128f, -0.2f, 0.11f, -0.15f, 0.132f, -0.1f, 0.115f, -0.06f,
        0.125f, -0.034f, 0.07f, -0.04f, 0.03f, -0.03f, -0.02f, -0.042f, -0.07f, -0.032f, -0.12f, -0.045f,
        -0.108f, -0.09f, -0.132f, -0.14f,
    )
    return Path().apply {
        moveTo(pts[0] * u, pts[1] * u)
        var i = 2
        while (i < pts.size) {
            lineTo(pts[i] * u, pts[i + 1] * u)
            i += 2
        }
        close()
    }
}

private fun DrawScope.rmWreckHoleOpen(u: Float, t: Float, pen: Pen, hullCol: Color) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val hole = rmHolePath(u)
    clipPath(hole) {
        drawRect(Brush.verticalGradient(0f to Color(0xFF1E2E50), 1f to Color(0xFF0C1326), startY = -0.27f * u, endY = -0.03f * u), o(-0.14f, -0.28f), Size(0.28f * u, 0.26f * u))
        // Ribs of the ship inside.
        for (x in floatArrayOf(-0.07f, 0.06f)) {
            val rib = Path().apply {
                moveTo((x + 0.04f) * u, -0.27f * u)
                quadraticTo((x + 0.01f) * u, -0.16f * u, (x + 0.03f) * u, -0.05f * u)
            }
            drawPath(rib, Color(0xFF3A3040), style = Stroke(0.014f * u, cap = StrokeCap.Round))
        }
        // The floor of the hold, where things rest.
        val floorP = rmQuad(rp(u, -0.14f, -0.05f, 0f), rp(u, -0.14f, -0.05f, 0.12f), rp(u, 0.14f, -0.05f, 0.12f), rp(u, 0.14f, -0.05f, 0f))
        drawPath(floorP, Color(0xFF3B2C24))
        for (k in 1..2) drawLine(Color(0xFF2A1F1A), rp(u, -0.14f, -0.05f, k * 0.04f), rp(u, 0.14f, -0.05f, k * 0.04f), pen.lw * 0.6f)
        drawRect(Color(0xFF2A1F1A), o(-0.14f, -0.05f), Size(0.28f * u, 0.03f * u))
        // Treasure glinting at the back.
        val gold = Color(0xFFFFC83D)
        val pile = rp(u, 0.05f, -0.05f, 0.08f)
        for (k in 0 until 5) {
            val cx = pile.x + (k - 2) * 0.011f * u
            val cy = pile.y - (if (k == 2) 0.008f else if (k % 2 == 1) 0.004f else 0f) * u
            drawOval(gold.darken(0.2f), Offset(cx - 0.009f * u, cy - 0.003f * u), Size(0.018f * u, 0.008f * u))
            drawOval(gold, Offset(cx - 0.009f * u, cy - 0.005f * u), Size(0.018f * u, 0.007f * u))
        }
        drawPath(Path().apply {
            moveTo(pile.x + 0.02f * u, pile.y - 0.018f * u)
            lineTo(pile.x + 0.028f * u, pile.y - 0.008f * u)
            lineTo(pile.x + 0.02f * u, pile.y + 0.001f * u)
            lineTo(pile.x + 0.012f * u, pile.y - 0.008f * u)
            close()
        }, Color(0xFF5AD8FF))
        rmGlow(pile, 0.05f * u, gold, 0.4f)
        for (k in 0 until 3) {
            val s = sin(t * 2.6f + k * 2.1f)
            if (s > 0f) twinkle(Offset(pile.x + (k - 1) * 0.02f * u, pile.y - (0.012f + 0.01f * k) * u), 0.014f * u * s, Color.White, s)
        }
        if (t == 0f) twinkle(Offset(pile.x, pile.y - 0.016f * u), 0.012f * u, Color.White, 1f)
        // Somebody shy lives in there.
        val blink = rmFrac(t * 0.33f) < 0.05f
        for (sx in RM_SIDES) {
            val e = o(-0.085f + sx * 0.011f, -0.205f)
            if (blink) {
                drawLine(Color(0xFFFFF3A0), Offset(e.x - 0.004f * u, e.y), Offset(e.x + 0.004f * u, e.y), pen.lw * 0.7f, StrokeCap.Round)
            } else {
                rmGlow(e, 0.012f * u, Color(0xFFFFF3A0), 0.5f)
                drawCircle(Color(0xFFFFF3A0), 0.0045f * u, e)
                drawCircle(Ink.line, 0.002f * u, Offset(e.x + 0.001f * u, e.y + 0.0005f * u))
            }
        }
    }
    drawPath(hole, Ink.line, style = pen.stroke)
    // One board hangs from its last nail; another lies at the bottom.
    rotate(78f, pivot = o(-0.13f, -0.235f)) {
        val r = Rect(o(-0.14f, -0.26f), o(0.08f, -0.212f))
        inkedRound(r, 0.006f * u, Color(0xFFB08A62), pen)
        drawCircle(Color(0xFF4A4E5E), 0.004f * u, o(-0.124f, -0.235f))
    }
    rotate(-8f, pivot = o(0.1f, -0.03f)) {
        inkedRound(Rect(o(0.02f, -0.05f), o(0.17f, -0.022f)), 0.006f * u, Color(0xFFA57F58), pen)
    }
    drawLine(hullCol.lighten(0.25f), o(-0.1f, -0.255f), o(-0.06f, -0.238f), pen.lw * 0.8f, StrokeCap.Round)
}

private fun DrawScope.rmBarnacles(c: Offset, u: Float, pen: Pen) {
    for (k in 0 until 5) {
        val a = k * 1.3f
        val r = (0.005f + 0.003f * rmHash(k, (c.x / u * 100f).toInt())) * u
        val p = Offset(c.x + cos(a) * 0.012f * u * (k % 3), c.y + sin(a) * 0.009f * u * (k % 3))
        drawCircle(Color(0xFFE4E0D6), r, p)
        drawCircle(Ink.line, r, p, style = pen.thin)
        drawCircle(Color(0xFF8A8478), r * 0.35f, p)
    }
}

private fun DrawScope.rmCrab(c: Offset, u: Float, t: Float, pen: Pen) {
    val red = Color(0xFFFF6B4A)
    val peek = sin(t * 0.9f) * 0.002f * u
    clipPath(ovalPath(Rect(c, 0.024f * u))) {
        val shell = Rect(Offset(c.x - 0.019f * u, c.y + 0.004f * u + peek), Offset(c.x + 0.019f * u, c.y + 0.034f * u + peek))
        inkedOval(shell, red, pen)
        for (sx in RM_SIDES) {
            val stalk = Offset(c.x + sx * 0.006f * u, c.y + 0.008f * u + peek)
            val eye = Offset(c.x + sx * 0.009f * u, c.y - 0.006f * u + peek)
            drawLine(Ink.line, stalk, eye, pen.lw * 2.4f, StrokeCap.Round)
            drawLine(red, stalk, eye, pen.lw * 1.1f, StrokeCap.Round)
            rmDot(eye, 0.0062f * u, Color.White, pen)
            drawCircle(Ink.line, 0.003f * u, Offset(eye.x + sx * 0.0012f * u, eye.y + 0.001f * u))
        }
    }
    val wave = sin(t * 3.2f) * 18f
    rotate(wave, pivot = Offset(c.x + 0.02f * u, c.y + 0.01f * u)) {
        val claw = Path().apply {
            moveTo(c.x + 0.018f * u, c.y + 0.012f * u)
            quadraticTo(c.x + 0.05f * u, c.y + 0.01f * u, c.x + 0.048f * u, c.y - 0.012f * u)
            lineTo(c.x + 0.036f * u, c.y - 0.004f * u)
            lineTo(c.x + 0.04f * u, c.y - 0.018f * u)
            quadraticTo(c.x + 0.02f * u, c.y - 0.012f * u, c.x + 0.018f * u, c.y + 0.012f * u)
            close()
        }
        inked(claw, red, pen)
    }
}

private fun DrawScope.rmAnchor(c: Offset, s: Float, pen: Pen) {
    val iron = Color(0xFF5E5A6A)
    rotate(18f, pivot = c) {
        rmTube(Offset(c.x, c.y - s * 0.95f), Offset(c.x, c.y), s * 0.09f, iron, pen)
        rmTube(Offset(c.x - s * 0.22f, c.y - s * 0.72f), Offset(c.x + s * 0.22f, c.y - s * 0.72f), s * 0.07f, iron, pen)
        drawCircle(Ink.line, s * 0.12f, Offset(c.x, c.y - s), style = Stroke(s * 0.05f + pen.lw * 2f))
        drawCircle(iron, s * 0.12f, Offset(c.x, c.y - s), style = Stroke(s * 0.05f))
        val arms = Path().apply {
            moveTo(c.x - s * 0.4f, c.y - s * 0.26f)
            quadraticTo(c.x - s * 0.34f, c.y + s * 0.06f, c.x, c.y + s * 0.04f)
            quadraticTo(c.x + s * 0.34f, c.y + s * 0.06f, c.x + s * 0.4f, c.y - s * 0.26f)
        }
        drawPath(arms, Ink.line, style = Stroke(s * 0.1f + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(arms, iron, style = Stroke(s * 0.1f, cap = StrokeCap.Round))
        for (sx in RM_SIDES) {
            val tip = Path().apply {
                moveTo(c.x + sx * s * 0.4f, c.y - s * 0.34f)
                lineTo(c.x + sx * s * 0.5f, c.y - s * 0.18f)
                lineTo(c.x + sx * s * 0.3f, c.y - s * 0.2f)
                close()
            }
            inked(tip, iron, pen, shade = false)
        }
        drawCircle(Color(0xFFB0643A).copy(alpha = 0.6f), s * 0.05f, Offset(c.x + s * 0.02f, c.y - s * 0.5f))
        drawCircle(Color(0xFFB0643A).copy(alpha = 0.5f), s * 0.035f, Offset(c.x - s * 0.25f, c.y - s * 0.05f))
    }
}

/** A small seashell lying on the sand: a fan ([kind] 0) or a spiral (1). */
private fun DrawScope.rmSeashell(c: Offset, u: Float, kind: Int, pen: Pen) {
    if (kind == 0) {
        val fan = Path().apply {
            moveTo(c.x, c.y + 0.006f * u)
            lineTo(c.x - 0.014f * u, c.y - 0.008f * u)
            quadraticTo(c.x, c.y - 0.02f * u, c.x + 0.014f * u, c.y - 0.008f * u)
            close()
        }
        inked(fan, Color(0xFFFFB3A7), pen, shade = false)
        for (k in -1..1) drawLine(Color(0xFFE07F70), Offset(c.x, c.y + 0.004f * u), Offset(c.x + k * 0.008f * u, c.y - 0.012f * u), pen.lw * 0.5f)
    } else {
        inkedOval(Rect(c.x - 0.013f * u, c.y - 0.009f * u, c.x + 0.013f * u, c.y + 0.005f * u), Color(0xFFF7E6C4), pen, shade = false)
        drawArc(Color(0xFFC9A27A), 180f, 300f, false, Offset(c.x - 0.007f * u, c.y - 0.007f * u), Size(0.012f * u, 0.009f * u), style = Stroke(pen.lw * 0.6f))
    }
}

/** A strand of seaweed from [b] swaying with the water, [h] pixels tall. */
private fun DrawScope.rmWeed(b: Offset, h: Float, t: Float, seed: Int, color: Color, pen: Pen) {
    val n = 6
    val xs = FloatArray(n + 1)
    val ys = FloatArray(n + 1)
    for (i in 0..n) {
        val s = i / n.toFloat()
        xs[i] = b.x + (sin(t * 1.1f + seed * 1.7f + s * 2.5f) * 0.12f * h) * s * s + sin(s * 7f + seed) * 0.03f * h
        ys[i] = b.y - s * h
    }
    val w = h * 0.08f
    val p = Path()
    p.moveTo(xs[0] - w, ys[0])
    for (i in 1..n) {
        val ww = w * (1f - i / (n + 1f))
        p.lineTo(xs[i] - ww, ys[i])
    }
    p.quadraticTo(xs[n], ys[n] - w, xs[n] + w * 0.2f, ys[n])
    for (i in n downTo 0) {
        val ww = w * (1f - i / (n + 1f))
        p.lineTo(xs[i] + ww, ys[i])
    }
    p.close()
    inked(p, color, pen)
}

private fun DrawScope.rmKelp(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    rmShadow(u, 0.12f, 0.06f)
    val heights = floatArrayOf(0.47f, 0.6f, 0.4f)
    val bases = floatArrayOf(0.018f, -0.006f, -0.03f)
    val blades = listOf(Color(0xFF5B9140), Color(0xFF7DB443), Color(0xFF94C44B))
    val stemCol = Color(0xFF8C8A36)
    val float = Color(0xFFD9C45A)
    val n = 16
    val pts = FloatArray((n + 1) * 2)
    var horse = Offset.Zero
    for (k in 0 until 3) {
        val hgt = heights[k]
        val sway = sin(t * 0.9f + k * 1.3f) * 0.04f
        for (i in 0..n) {
            val s = i / n.toFloat()
            val bend = (f.angle * 0.6f + sway) * s * s * 1.2f + sin(t * 1.5f + s * 5f + k) * 0.012f * s
            pts[i * 2] = (bases[k] + bend + sin(s * 7f + k * 2f) * 0.01f * s) * u
            pts[i * 2 + 1] = -s * hgt * u
        }
        if (k == 1) horse = Offset(pts[8 * 2] + 0.034f * u, pts[8 * 2 + 1] + 0.016f * u)
        // The stem first, so the blades and their floats sit on it.
        val stem = Path().apply {
            moveTo(pts[0], pts[1])
            for (i in 1 until n) quadraticTo(pts[i * 2], pts[i * 2 + 1], (pts[i * 2] + pts[i * 2 + 2]) / 2f, (pts[i * 2 + 1] + pts[i * 2 + 3]) / 2f)
            lineTo(pts[n * 2], pts[n * 2 + 1])
        }
        drawPath(stem, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(stem, stemCol, style = Stroke(0.009f * u, cap = StrokeCap.Round))
        val col = blades[k]
        // Long ruffled blades streaming up with the water, each with a float at its base.
        var i = 3
        while (i <= n) {
            val s = i / n.toFloat()
            val top = i == n
            val side = if (((i - 3) / 2 + k) % 2 == 0) 1f else -1f
            val tx = pts[min(n, i + 1) * 2] - pts[(i - 1) * 2]
            val ty = pts[min(n, i + 1) * 2 + 1] - pts[(i - 1) * 2 + 1]
            val stemAng = kotlin.math.atan2(ty, tx)
            val flow = sin(t * 1.3f + i * 0.7f + k) * 0.16f + f.angle * 0.8f
            val ang = stemAng + (if (top) flow * 0.5f else side * (0.6f - 0.25f * s) + flow)
            val len = (if (top) 0.1f else 0.13f - 0.05f * s) * u
            val wid = 0.03f * u * (1f - 0.3f * s)
            val b = Offset(pts[i * 2], pts[i * 2 + 1])
            val blade = rmKelpBlade(b, ang, len, wid, t, i + k * 5f)
            inked(blade, if (i % 4 == 1) col.lighten(0.08f) else col, pen)
            drawLine(col.lighten(0.4f).copy(alpha = 0.6f), b, Offset(b.x + cos(ang) * len * 0.72f, b.y + sin(ang) * len * 0.72f), pen.lw * 0.55f, StrokeCap.Round)
            if (!top) {
                rmDot(b, 0.0085f * u, float, pen)
                drawCircle(Color.White.copy(alpha = 0.7f), 0.0025f * u, Offset(b.x - 0.0028f * u, b.y - 0.003f * u))
            }
            i = if (i == n - 1) n else if (i + 2 > n) n + 1 else i + 2
        }
    }
    // A little seahorse holds on to the tallest strand.
    rmSeahorse(horse, 0.1f * u, t, pen)
    // The holdfast.
    val hold = blobPath(-0.04f * u, -0.002f * u, -0.02f * u, -0.024f * u, 0.015f * u, -0.028f * u, 0.042f * u, -0.004f * u, 0.01f * u, 0.004f * u)
    inked(hold, Color(0xFF8A7A3B), pen)
    for (sx in RM_SIDES) drawLine(Ink.line, o(sx * 0.03f, -0.004f), o(sx * 0.05f, 0.002f), pen.lw * 0.9f, StrokeCap.Round)
}

/** One kelp blade from [b] at [ang] radians: long, with a rounded tip and ruffled, rippling edges. */
private fun rmKelpBlade(b: Offset, ang: Float, len: Float, wid: Float, t: Float, seed: Float): Path {
    val m = 12
    val dx = cos(ang)
    val dy = sin(ang)
    val nx = -dy
    val ny = dx
    val lx = FloatArray(m + 1)
    val ly = FloatArray(m + 1)
    val rx = FloatArray(m + 1)
    val ry = FloatArray(m + 1)
    for (j in 0..m) {
        val s = j / m.toFloat()
        val bend = sin(s * 2.6f + t * 1.7f + seed) * len * 0.1f * s
        val cx = b.x + dx * s * len + nx * bend
        val cy = b.y + dy * s * len + ny * bend
        val prof = sqrt(sin(RM_PI * (0.06f + s * 0.94f)).coerceAtLeast(0f))
        val w = wid * 0.5f * prof * (1f + 0.2f * sin(s * 19f + t * 3.1f + seed))
        lx[j] = cx + nx * w
        ly[j] = cy + ny * w
        rx[j] = cx - nx * w
        ry[j] = cy - ny * w
    }
    return Path().apply {
        moveTo(lx[0], ly[0])
        for (j in 1 until m) quadraticTo(lx[j], ly[j], (lx[j] + lx[j + 1]) / 2f, (ly[j] + ly[j + 1]) / 2f)
        lineTo(lx[m], ly[m])
        lineTo(rx[m], ry[m])
        for (j in m - 1 downTo 1) quadraticTo(rx[j], ry[j], (rx[j] + rx[j - 1]) / 2f, (ry[j] + ry[j - 1]) / 2f)
        lineTo(rx[0], ry[0])
        close()
    }
}

private fun DrawScope.rmSeahorse(c: Offset, s: Float, t: Float, pen: Pen) {
    val col = Color(0xFFFF9F43)
    val bob = sin(t * 2f) * s * 0.03f
    translate(0f, bob) {
        val body = Path().apply {
            moveTo(c.x - s * 0.02f, c.y - s * 0.34f)
            cubicTo(c.x + s * 0.2f, c.y - s * 0.34f, c.x + s * 0.22f, c.y - s * 0.05f, c.x + s * 0.08f, c.y + s * 0.08f)
            cubicTo(c.x + s * 0.02f, c.y + s * 0.16f, c.x + s * 0.02f, c.y + s * 0.3f, c.x - s * 0.08f, c.y + s * 0.3f)
            cubicTo(c.x - s * 0.18f, c.y + s * 0.3f, c.x - s * 0.16f, c.y + s * 0.18f, c.x - s * 0.08f, c.y + s * 0.2f)
            cubicTo(c.x - s * 0.02f, c.y + s * 0.12f, c.x - s * 0.06f, c.y + s * 0.02f, c.x - s * 0.08f, c.y - s * 0.08f)
            cubicTo(c.x - s * 0.1f, c.y - s * 0.2f, c.x - s * 0.12f, c.y - s * 0.3f, c.x - s * 0.02f, c.y - s * 0.34f)
            close()
        }
        val snout = Path().apply {
            moveTo(c.x - s * 0.06f, c.y - s * 0.32f)
            lineTo(c.x - s * 0.24f, c.y - s * 0.28f)
            lineTo(c.x - s * 0.24f, c.y - s * 0.22f)
            lineTo(c.x - s * 0.08f, c.y - s * 0.22f)
            close()
        }
        inked(snout, col, pen, shade = false)
        inked(body, col, pen)
        val fin = rmLens(Offset(c.x + s * 0.12f, c.y - s * 0.12f), Offset(c.x + s * 0.22f, c.y - s * 0.02f), s * 0.04f)
        inked(fin, Color(0xFFFFD23F), pen, shade = false)
        for (k in 0 until 4) drawLine(col.darken(0.3f), Offset(c.x - s * 0.02f, c.y - s * (0.14f - k * 0.07f)), Offset(c.x + s * 0.08f, c.y - s * (0.16f - k * 0.07f)), pen.lw * 0.5f)
        drawCircle(Ink.line, s * 0.035f, Offset(c.x - s * 0.02f, c.y - s * 0.27f))
        drawCircle(Color.White, s * 0.012f, Offset(c.x - s * 0.03f, c.y - s * 0.28f))
    }
}

private fun DrawScope.rmClam(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val shell = Color(0xFFF1E4CF)
    val rib = Color(0xFFC9B08F)
    val mantle = Color(0xFF3F8FE0)
    rmShadow(u, 0.28f, 0.14f)
    val hw = 0.128f
    val teeth = 9
    fun lipY(x: Float) = -0.05f + 0.018f * (1f - (x / hw) * (x / hw))
    val lower = Path().apply {
        moveTo(-hw * u, lipY(-hw) * u)
        cubicTo(-hw * u, 0.004f * u, -0.05f * u, 0.004f * u, 0f, 0.004f * u)
        cubicTo(0.05f * u, 0.004f * u, hw * u, 0.004f * u, hw * u, lipY(hw) * u)
        for (i in teeth - 1 downTo 0) {
            val x0 = -hw + (i + 0.5f) * 2f * hw / teeth
            val x1 = -hw + i * 2f * hw / teeth
            lineTo(x0 * u, (lipY(x0) - 0.009f) * u)
            lineTo(x1 * u, lipY(x1) * u)
        }
        close()
    }
    val lowerRibs = Path()
    for (k in -3..3) {
        val x = k * hw / 3.6f
        lowerRibs.moveTo(0f, 0f)
        lowerRibs.quadraticTo(x * 0.5f * u, -0.01f * u, x * u, (lipY(x) + 0.004f) * u)
    }
    if (!f.open) {
        // Volume behind, then the mantle peeking out and a pair of curious eyes.
        val outline = Path().apply {
            moveTo(-hw * u, lipY(-hw) * u)
            cubicTo(-hw * u, -0.1f * u, -0.07f * u, -0.142f * u, 0f, -0.142f * u)
            cubicTo(0.07f * u, -0.142f * u, hw * u, -0.1f * u, hw * u, lipY(hw) * u)
            cubicTo(hw * u, 0.004f * u, 0.05f * u, 0.004f * u, 0f, 0.004f * u)
            cubicTo(-0.05f * u, 0.004f * u, -hw * u, 0.004f * u, -hw * u, lipY(-hw) * u)
            close()
        }
        rmExtrude(outline, 0.045f * u, shell.darken(0.16f), pen)
        val band = Path().apply {
            moveTo(-hw * u, (lipY(-hw) - 0.012f) * u)
            for (i in 0..16) {
                val x = -hw + i * 2f * hw / 16f
                lineTo(x * u, (lipY(x) - 0.012f + sin(i * 1.7f + t * 1.5f) * 0.002f) * u)
            }
            for (i in 16 downTo 0) {
                val x = -hw + i * 2f * hw / 16f
                lineTo(x * u, (lipY(x) + 0.004f) * u)
            }
            close()
        }
        drawPath(band, mantle)
        val upper = Path().apply {
            moveTo(-hw * u, lipY(-hw) * u)
            for (i in 0 until teeth) {
                val x0 = -hw + (i + 0.5f) * 2f * hw / teeth
                val x1 = -hw + (i + 1) * 2f * hw / teeth
                lineTo(x0 * u, (lipY(x0) + 0.001f - 0.012f) * u)
                lineTo(x1 * u, (lipY(x1) - 0.004f) * u)
            }
            cubicTo(hw * u, -0.1f * u, 0.07f * u, -0.142f * u, 0f, -0.142f * u)
            cubicTo(-0.07f * u, -0.142f * u, -hw * u, -0.1f * u, -hw * u, lipY(-hw) * u)
            close()
        }
        inked(lower, shell, pen)
        drawPath(lowerRibs, rib, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
        inked(upper, shell, pen)
        clipPath(upper) {
            val ribs = Path()
            for (k in -3..3) {
                val x = k * hw / 3.4f
                ribs.moveTo(0.01f * u, -0.16f * u)
                ribs.quadraticTo(x * 0.7f * u, -0.12f * u, x * u, lipY(x) * u)
            }
            drawPath(ribs, rib, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
            drawOval(Color(0xFFE6C9E0).copy(alpha = 0.5f), o(-0.09f, -0.13f), Size(0.07f * u, 0.03f * u))
        }
        drawPath(upper, Ink.line, style = pen.stroke)
        shine(o(-0.05f, -0.118f), 0.03f * u, 0.012f * u, 0.7f)
        // The shell is open just a crack, and somebody is peeking out.
        val gap = o(0f, lipY(0f) - 0.007f)
        val crack = Path().apply { addOval(Rect(Offset(gap.x - 0.036f * u, gap.y - 0.009f * u), Offset(gap.x + 0.036f * u, gap.y + 0.009f * u))) }
        drawPath(crack, Color(0xFF14203A))
        drawPath(crack, Ink.line, style = pen.thin)
        val blink = rmFrac(t * 0.25f) < 0.04f
        for (sx in RM_SIDES) {
            val e = Offset(gap.x + sx * 0.015f * u, gap.y)
            if (blink) {
                drawLine(Color.White, Offset(e.x - 0.005f * u, e.y), Offset(e.x + 0.005f * u, e.y), pen.lw * 0.8f, StrokeCap.Round)
            } else {
                drawCircle(Color.White, 0.0068f * u, e)
                drawCircle(Ink.line, 0.0034f * u, Offset(e.x + sin(t * 0.8f) * 0.002f * u, e.y + 0.001f * u))
                drawCircle(Color.White, 0.0012f * u, Offset(e.x - 0.0012f * u, e.y - 0.0012f * u))
            }
        }
        return
    }
    // Open: the top shell stands up at the back, the mantle bed shows in the bottom one.
    val hinge = rp(u, 0f, -0.05f, 0.11f)
    val topShell = Path().apply {
        val n = 9
        moveTo(hinge.x - hw * u, hinge.y)
        for (i in 0..n) {
            val ang = RM_PI - i * RM_PI / n
            val x = hinge.x + cos(ang) * hw * u
            val y = hinge.y - sin(ang) * 0.15f * u
            if (i > 0) {
                val am = ang + RM_PI / n * 0.5f
                quadraticTo(hinge.x + cos(am) * hw * 1.12f * u, hinge.y - sin(am) * 0.168f * u, x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }
    drawPath(topShell, shell.darken(0.1f))
    drawPath(topShell, Ink.line, style = pen.stroke)
    val inner = Path().apply { addOval(Rect(Offset(hinge.x - hw * 0.86f * u, hinge.y - 0.13f * u), Offset(hinge.x + hw * 0.86f * u, hinge.y + 0.08f * u))) }
    clipPath(topShell) {
        clipPath(inner) {
            drawRect(
                Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF7D9EC), Color(0xFFD9CCF7), Color(0xFFCFF3F0)), Offset(hinge.x - 0.1f * u, hinge.y - 0.14f * u), Offset(hinge.x + 0.1f * u, hinge.y)),
                Offset(hinge.x - hw * u, hinge.y - 0.17f * u),
                Size(hw * 2f * u, 0.17f * u),
            )
            val gl = Path()
            for (k in -3..3) {
                gl.moveTo(hinge.x, hinge.y)
                gl.lineTo(hinge.x + k * 0.035f * u, hinge.y - 0.15f * u)
            }
            drawPath(gl, Color.White.copy(alpha = 0.6f), style = Stroke(pen.lw * 0.8f))
        }
    }
    drawPath(inner, Ink.line.copy(alpha = 0.3f), style = pen.thin)
    shine(Offset(hinge.x - 0.05f * u, hinge.y - 0.09f * u), 0.025f * u, 0.012f * u, 0.8f)
    // The mantle: soft, blue and spotted, with a pale bed for the pearl in front.
    val bedC = rp(u, 0f, -0.046f, 0.055f)
    val bed = rmDisc(bedC.x, bedC.y, 0.118f * u, 0.058f * u)
    drawPath(bed, safeRadialGradient(0f to Color(0xFF6FC4FF), 0.6f to mantle, 1f to Color(0xFF2B5FB8), center = bedC, radius = 0.13f * u))
    clipPath(bed) {
        for (k in 0 until 14) {
            val px = bedC.x + (rmHash(k, 21) - 0.5f) * 0.2f * u
            val py = bedC.y + (rmHash(k, 22) - 0.5f) * 0.05f * u
            drawCircle(Color(0xFF7FF0E0), (0.003f + 0.003f * rmHash(k, 23)) * u, Offset(px, py))
        }
        drawOval(Color(0xFFFCE8F2), o(-0.05f, -0.052f), Size(0.1f * u, 0.022f * u))
        drawOval(Color(0xFFE9C7DE), o(-0.035f, -0.046f), Size(0.07f * u, 0.012f * u))
    }
    drawPath(bed, Ink.line, style = pen.thin)
    inked(lower, shell, pen)
    drawPath(lowerRibs, rib, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    drawPath(lower, Ink.line, style = pen.stroke)
    for (k in 0 until 3) {
        val s = if (t == 0f) 1f else sin(t * 2.4f + k * 2.2f)
        if (s > 0f) twinkle(o(-0.07f + k * 0.07f, -0.075f - (k % 2) * 0.02f), 0.012f * u * s, Color.White, s)
    }
}

private fun DrawScope.rmCoral(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val purple = f.variant % 2 == 1
    val plate = if (purple) Color(0xFF9D6BFF) else Color(0xFFFF7B8C)
    val branch = if (purple) Color(0xFFFFD23F) else Color(0xFFFF9A3D)
    val brain = if (purple) Color(0xFF38C9B8) else Color(0xFFF9C74F)
    val fan = if (purple) Color(0xFFFF6FA8) else Color(0xFFB983FF)
    rmShadow(u, 0.3f, 0.12f)
    // Tube sponges behind everything.
    for ((i, h) in floatArrayOf(0.13f, 0.1f, 0.075f).withIndex()) {
        val b = rp(u, 0.045f + i * 0.034f, -0.04f, 0.07f - i * 0.012f)
        val r = (0.017f - i * 0.002f) * u
        rmCyl(b.x, b.y, b.y - h * u, r, fan, pen, top = fan.darken(0.55f), rz = r * 0.8f)
        drawPath(rmDisc(b.x, b.y - h * u, r * 0.62f, r * 0.5f), fan.darken(0.75f))
        drawCircle(fan.lighten(0.5f), 0.0035f * u, Offset(b.x - r * 0.45f, b.y - h * u * 0.6f))
    }
    // The rock it all grows on.
    val rock = blobPath(
        -0.15f * u, -0.004f * u, -0.13f * u, -0.04f * u, -0.06f * u, -0.058f * u, 0.02f * u, -0.05f * u,
        0.1f * u, -0.06f * u, 0.15f * u, -0.03f * u, 0.14f * u, 0.004f * u, 0f, 0.006f * u,
    )
    inked(rock, Color(0xFF8E8AA8), pen)
    for ((x, y) in listOf(-0.09f to -0.03f, 0.05f to -0.022f, 0.11f to -0.03f)) drawOval(Color(0xFF5E5A78), o(x - 0.008f, y - 0.004f), Size(0.016f * u, 0.008f * u))
    // Staghorn branches on the left.
    val segs = floatArrayOf(
        -0.08f, -0.04f, -0.09f, -0.1f, 0.022f,
        -0.09f, -0.1f, -0.125f, -0.155f, 0.017f,
        -0.09f, -0.1f, -0.065f, -0.165f, 0.016f,
        -0.125f, -0.155f, -0.14f, -0.195f, 0.012f,
        -0.125f, -0.155f, -0.105f, -0.19f, 0.012f,
        -0.065f, -0.165f, -0.07f, -0.205f, 0.011f,
        -0.105f, -0.075f, -0.14f, -0.1f, 0.013f,
        -0.14f, -0.1f, -0.15f, -0.13f, 0.01f,
    )
    val sw = sin(t * 1.3f) * 0.003f
    fun sx(x: Float, y: Float) = (x + sw * (-y) * 4f) * u
    for (pass in 0 until 3) {
        var i = 0
        while (i < segs.size) {
            val a = Offset(sx(segs[i], segs[i + 1]), segs[i + 1] * u)
            val b = Offset(sx(segs[i + 2], segs[i + 3]), segs[i + 3] * u)
            val w = segs[i + 4] * u
            when (pass) {
                0 -> drawLine(Ink.line, a, b, w + pen.lw * 2f, StrokeCap.Round)
                1 -> drawLine(branch.shadow(), a, b, w, StrokeCap.Round)
                else -> drawLine(branch, Offset(a.x - w * 0.12f, a.y - w * 0.06f), Offset(b.x - w * 0.12f, b.y - w * 0.06f), w * 0.6f, StrokeCap.Round)
            }
            i += 5
        }
    }
    for (k in intArrayOf(3, 4, 5, 7)) {
        val i = k * 5
        val tip = Offset(sx(segs[i + 2], segs[i + 3]), segs[i + 3] * u)
        drawCircle(branch.lighten(0.5f), segs[i + 4] * u * 0.34f, tip)
    }
    // Brain coral on the right.
    val bc = o(0.088f, -0.055f)
    val bR = Rect(Offset(bc.x - 0.058f * u, bc.y - 0.05f * u), Offset(bc.x + 0.058f * u, bc.y + 0.03f * u))
    inkedOval(bR, brain, pen)
    clipPath(ovalPath(bR)) {
        val g = Path()
        for (k in 0 until 4) {
            val y = bc.y - 0.035f * u + k * 0.018f * u
            g.moveTo(bc.x - 0.06f * u, y)
            for (j in 0 until 6) {
                val x = bc.x - 0.06f * u + (j + 1) * 0.02f * u
                g.quadraticTo(x - 0.01f * u, y + (if (j % 2 == 0) -0.012f else 0.012f) * u, x, y)
            }
        }
        drawPath(g, brain.darken(0.3f), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    }
    shine(Offset(bc.x - 0.025f * u, bc.y - 0.03f * u), 0.014f * u, 0.007f * u, 0.6f)
    // The table coral: a trumpet stalk and a flat plate on top, where things can rest.
    val stalk = Path().apply {
        moveTo(-0.022f * u, -0.04f * u)
        cubicTo(-0.018f * u, -0.12f * u, -0.02f * u, -0.17f * u, -0.06f * u, -0.206f * u)
        lineTo(0.07f * u, -0.206f * u)
        cubicTo(0.03f * u, -0.17f * u, 0.022f * u, -0.12f * u, 0.026f * u, -0.04f * u)
        close()
    }
    inked(stalk, plate.darken(0.08f), pen)
    val pc = rp(u, 0f, -0.22f, 0.06f)
    val prx = 0.115f * u
    val prz = 0.06f * u
    fun plateEdge(dy: Float): Path = Path().apply {
        val n = 22
        for (i in 0..n) {
            val ang = i * RM_TAU / n
            val k = 1f + 0.05f * sin(i * 2.3f)
            val x = pc.x + cos(ang) * prx * k + ODX * sin(ang) * prz * k
            val y = pc.y + dy + ODY * sin(ang) * prz * k
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    val under = plateEdge(0.016f * u)
    inked(under, plate.darken(0.2f), pen, shade = false)
    val topP = plateEdge(0f)
    drawPath(topP, Brush.verticalGradient(0f to plate.lighten(0.2f), 1f to plate, startY = pc.y - prz * 0.4f, endY = pc.y + prz * 0.4f))
    clipPath(topP) {
        val dots = ArrayList<Offset>(40)
        for (i in 0 until 6) for (j in 0 until 4) {
            val x = pc.x + (-0.09f + i * 0.036f + (j % 2) * 0.018f) * u
            val y = pc.y + (-0.022f + j * 0.014f) * u
            dots.add(Offset(x, y))
        }
        drawPoints(dots, PointMode.Points, plate.lighten(0.45f), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
    }
    drawPath(topP, Ink.line, style = pen.stroke)
    // A small anemone in front, waving its tentacles.
    val an = o(-0.03f, -0.042f)
    for (k in 0 until 7) {
        val ang = -RM_PI / 2f + (k - 3) * 0.32f + sin(t * 2f + k) * 0.12f
        val tip = Offset(an.x + cos(ang) * 0.03f * u, an.y + sin(ang) * 0.03f * u)
        rmTube(an, tip, 0.006f * u, Color(0xFF8FE3C0), pen)
        drawCircle(Color(0xFFFF8FB1), 0.0045f * u, tip)
    }
    inkedOval(Rect(Offset(an.x - 0.016f * u, an.y - 0.006f * u), Offset(an.x + 0.016f * u, an.y + 0.012f * u)), Color(0xFF5FBF9A), pen)
}

// ---------------------------------------------------------------------------------------- submarine

private const val SUB_PY = -0.215f
private const val SUB_PR = 0.066f
private val SUB_PX = floatArrayOf(-0.08f, 0.1f)

private fun rmSubHull(u: Float): Path = Path().apply {
    moveTo(-0.12f * u, -0.325f * u)
    lineTo(0.07f * u, -0.325f * u)
    cubicTo(0.2f * u, -0.325f * u, 0.245f * u, -0.25f * u, 0.245f * u, -0.165f * u)
    cubicTo(0.245f * u, -0.07f * u, 0.195f * u, -0.01f * u, 0.08f * u, -0.01f * u)
    lineTo(-0.1f * u, -0.01f * u)
    cubicTo(-0.19f * u, -0.01f * u, -0.24f * u, -0.08f * u, -0.25f * u, -0.145f * u)
    lineTo(-0.252f * u, -0.19f * u)
    cubicTo(-0.245f * u, -0.275f * u, -0.2f * u, -0.325f * u, -0.12f * u, -0.325f * u)
    close()
}

private fun rmSubPortholes(u: Float, r: Float): Path = Path().apply {
    for (x in SUB_PX) addOval(Rect(Offset(x * u, SUB_PY * u), r * u))
}

private inline fun DrawScope.rmSubLean(f: Fixture, u: Float, block: DrawScope.() -> Unit) {
    val lean = f.angle.coerceIn(-1f, 1f) * 4f
    if (lean == 0f) block() else rotate(lean, pivot = Offset(0.01f * u, -0.2f * u), block = block)
}

private fun DrawScope.rmSubBack(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    // Its shadow stays on the sea floor while it rises.
    val lift = (f.place.floor - f.y - f.shiftY).coerceAtLeast(0f)
    translate(0f, lift * u) { rmShadow(u, 0.46f, 0.1f, alpha = (1f - lift * 2.2f).coerceIn(0.2f, 1f)) }
    rmSubLean(f, u) {
        rmExtrude(rmSubHull(u), 0.07f * u, Color(0xFFD9A021), pen)
        // The cabin inside, seen through the portholes.
        clipPath(rmSubPortholes(u, SUB_PR + 0.004f)) {
            drawRect(Brush.verticalGradient(0f to Color(0xFF2B5670), 1f to Color(0xFF16304A), startY = -0.29f * u, endY = -0.14f * u), o(-0.16f, -0.3f), Size(0.35f * u, 0.17f * u))
            drawLine(Color(0xFF6E8CA8), o(-0.16f, -0.262f), o(0.2f, -0.262f), 0.008f * u)
            drawLine(Color(0xFF9FB8CE), o(-0.16f, -0.265f), o(0.2f, -0.265f), pen.lw * 0.6f)
            for (x in SUB_PX) {
                inkedRound(Rect(o(x - 0.05f, -0.255f), o(x + 0.05f, -0.13f)), 0.02f * u, Color(0xFFE04E5A), pen)
                drawLine(Color(0xFFB83A48), o(x, -0.245f), o(x, -0.14f), pen.lw * 0.7f)
                rmGlow(o(x + 0.04f, -0.27f), 0.03f * u, RoomsInk.warm, 0.6f)
                drawCircle(RoomsInk.warm, 0.005f * u, o(x + 0.04f, -0.272f))
            }
        }
    }
}

private fun DrawScope.rmSubFront(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val yellow = Color(0xFFFFC83D)
    val stripe = Color(0xFFEF5B3E)
    val brass = Color(0xFFD9A93F)
    rmSubLean(f, u) {
        // Fins at the stern and the propeller.
        val finTop = Path().apply {
            moveTo(-0.19f * u, -0.305f * u)
            lineTo(-0.26f * u, -0.35f * u)
            quadraticTo(-0.27f * u, -0.3f * u, -0.245f * u, -0.25f * u)
            close()
        }
        val finBot = Path().apply {
            moveTo(-0.2f * u, -0.035f * u)
            lineTo(-0.262f * u, 0.0f)
            quadraticTo(-0.272f * u, -0.05f * u, -0.25f * u, -0.1f * u)
            close()
        }
        inked(finTop, stripe, pen)
        inked(finBot, stripe, pen)
        rmPropeller(o(-0.265f, -0.168f), u, if (f.on) t * 16f else 0.6f, brass, pen)
        // The near hull, with holes for the portholes.
        val hull = rmSubHull(u)
        val holes = Path().apply {
            addPath(hull)
            addPath(rmSubPortholes(u, SUB_PR))
            fillType = PathFillType.EvenOdd
        }
        val b = hull.getBounds()
        val s = min(b.width, b.height) * 0.1f
        drawPath(holes, yellow.shadow())
        clipPath(holes) {
            translate(-s * 0.5f, -s) {
                drawPath(hull, Brush.verticalGradient(0f to yellow.lighten(0.2f), 0.5f to yellow, 1f to yellow, startY = b.top, endY = b.bottom))
            }
            drawRect(stripe, o(-0.3f, -0.075f), Size(0.6f * u, 0.028f * u))
            drawLine(Color.White.copy(alpha = 0.8f), o(-0.3f, -0.079f), o(0.3f, -0.079f), pen.lw * 0.9f)
            for (x in floatArrayOf(-0.185f, 0.185f)) drawLine(yellow.darken(0.3f), o(x, -0.33f), o(x, 0f), pen.lw * 0.7f)
            val rivets = ArrayList<Offset>(24)
            for (k in 0 until 11) rivets.add(o(-0.2f + k * 0.04f, -0.3f))
            for (k in 0 until 6) rivets.add(o(-0.185f + 0.007f, -0.26f + k * 0.04f))
            for (k in 0 until 6) rivets.add(o(0.185f + 0.007f, -0.26f + k * 0.04f))
            drawPoints(rivets, PointMode.Points, yellow.darken(0.35f), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
        }
        drawPath(holes, Ink.line, style = pen.stroke)
        // Brass rims round the portholes, with a hint of glass.
        for (x in SUB_PX) {
            val c = o(x, SUB_PY)
            drawCircle(brass, (SUB_PR + 0.008f) * u, c, style = Stroke(0.016f * u))
            drawCircle(brass.lighten(0.35f), (SUB_PR + 0.011f) * u, c, style = Stroke(0.004f * u))
            drawCircle(Ink.line, (SUB_PR + 0.016f) * u, c, style = pen.stroke)
            drawCircle(Ink.line, SUB_PR * u, c, style = pen.stroke)
            for (k in 0 until 8) {
                val ang = k * RM_TAU / 8f + 0.39f
                drawCircle(brass.darken(0.35f), 0.0028f * u, Offset(c.x + cos(ang) * (SUB_PR + 0.008f) * u, c.y + sin(ang) * (SUB_PR + 0.008f) * u))
            }
            drawArc(Color.White.copy(alpha = 0.55f), 195f, 60f, false, Offset(c.x - (SUB_PR - 0.012f) * u, c.y - (SUB_PR - 0.012f) * u), Size((SUB_PR - 0.012f) * 2f * u, (SUB_PR - 0.012f) * 2f * u), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
            drawCircle(Color(0xFFBFEFFF).copy(alpha = 0.12f), SUB_PR * u, c)
        }
        // Headlight at the bow.
        val hl = o(0.238f, -0.13f)
        if (f.on) {
            val beam = Path().apply {
                moveTo(hl.x, hl.y - 0.012f * u)
                lineTo(hl.x + 0.3f * u, hl.y - 0.08f * u)
                lineTo(hl.x + 0.3f * u, hl.y + 0.08f * u)
                lineTo(hl.x, hl.y + 0.012f * u)
                close()
            }
            drawPath(beam, Brush.horizontalGradient(0f to RoomsInk.warm.copy(alpha = 0.45f + 0.25f * pen.night), 1f to RoomsInk.warm.copy(alpha = 0f), startX = hl.x, endX = hl.x + 0.3f * u))
            rmGlow(hl, 0.05f * u, RoomsInk.warm, 0.8f)
        }
        inkedRound(Rect(o(0.225f, -0.148f), o(0.25f, -0.112f)), 0.008f * u, Color(0xFFC0C8D6), pen)
        drawOval(if (f.on) Color(0xFFFFF6C8) else Color(0xFF9FB8CE), o(0.24f, -0.145f), Size(0.014f * u, 0.03f * u))
        drawOval(Ink.line, o(0.24f, -0.145f), Size(0.014f * u, 0.03f * u), style = pen.thin)
        // The tower with a hatch wheel, the periscope and a stowaway duck.
        rmBox(u, -0.01f, -0.378f, 0.085f, -0.322f, 0.05f, yellow, pen, rad = 0.014f, top = yellow.lighten(0.25f), side = Color(0xFFD9A021))
        val wheel = o(0.038f, -0.35f)
        drawCircle(brass, 0.013f * u, wheel, style = Stroke(0.004f * u))
        drawCircle(Ink.line, 0.015f * u, wheel, style = pen.thin)
        for (k in 0 until 4) {
            val ang = k * RM_PI / 4f
            drawLine(brass.darken(0.2f), Offset(wheel.x - cos(ang) * 0.013f * u, wheel.y - sin(ang) * 0.013f * u), Offset(wheel.x + cos(ang) * 0.013f * u, wheel.y + sin(ang) * 0.013f * u), pen.lw * 0.7f)
        }
        drawCircle(brass.darken(0.2f), 0.004f * u, wheel)
        val dir = if (f.angle < -0.05f) -1f else 1f
        val pb = rp(u, 0.065f, -0.378f, 0.025f)
        rmTube(pb, Offset(pb.x, pb.y - 0.06f * u), 0.009f * u, Color(0xFF9AA4B5), pen)
        rmTube(Offset(pb.x, pb.y - 0.06f * u), Offset(pb.x + dir * 0.025f * u, pb.y - 0.06f * u), 0.011f * u, Color(0xFF9AA4B5), pen)
        drawCircle(Color(0xFF5AD8FF), 0.004f * u, Offset(pb.x + dir * 0.03f * u, pb.y - 0.06f * u))
        rmDuck(rp(u, 0.012f, -0.378f, 0.02f), 0.032f * u, t, pen)
        // Bubbles trail from the propeller while it runs.
        if (f.on) rmBubbles(o(-0.29f, -0.17f), u, t, pen, 6, 0.16f, 5)
    }
}

/** A three-bladed propeller seen from the side, turning with [spin] radians. */
private fun DrawScope.rmPropeller(c: Offset, u: Float, spin: Float, color: Color, pen: Pen) {
    val len = 0.056f * u
    for (pass in 0 until 2) {
        for (k in 0 until 3) {
            val ph = spin + k * RM_TAU / 3f
            val front = sin(ph) > 0f
            if ((pass == 0) == front) continue
            val l = cos(ph) * len
            val w = (0.004f + 0.01f * abs(sin(ph))) * u
            val r = Rect(c.x - w, min(c.y, c.y + l), c.x + w, max(c.y, c.y + l))
            if (r.height < pen.lw) continue
            inkedOval(r, if (front) color else color.darken(0.2f), pen, shade = false)
        }
        if (pass == 0) {
            rmTube(Offset(c.x + 0.02f * u, c.y), Offset(c.x, c.y), 0.012f * u, Color(0xFF9AA4B5), pen)
        }
    }
    inkedCircle(c, 0.008f * u, color.lighten(0.2f), pen, shade = false)
}

private fun DrawScope.rmDuck(b: Offset, s: Float, t: Float, pen: Pen) {
    val yellow = Color(0xFFFFE14D)
    val bob = sin(t * 2.4f) * s * 0.04f
    translate(0f, bob) {
        val body = Path().apply {
            moveTo(b.x - s * 0.45f, b.y - s * 0.4f)
            quadraticTo(b.x - s * 0.5f, b.y, b.x, b.y)
            quadraticTo(b.x + s * 0.45f, b.y, b.x + s * 0.4f, b.y - s * 0.3f)
            lineTo(b.x - s * 0.1f, b.y - s * 0.32f)
            close()
        }
        inked(body, yellow, pen)
        inkedCircle(Offset(b.x + s * 0.18f, b.y - s * 0.52f), s * 0.2f, yellow, pen)
        val beak = Path().apply {
            moveTo(b.x + s * 0.34f, b.y - s * 0.56f)
            lineTo(b.x + s * 0.54f, b.y - s * 0.5f)
            lineTo(b.x + s * 0.34f, b.y - s * 0.44f)
            close()
        }
        inked(beak, Color(0xFFFF8A3D), pen, shade = false)
        drawCircle(Ink.line, s * 0.04f, Offset(b.x + s * 0.22f, b.y - s * 0.56f))
    }
}

// ------------------------------------------------------------------------------------------ octopus

private const val TN = 14

private val OCTO_COLOURS = longArrayOf(0xFFFF8A3D, 0xFF9B6BFF, 0xFFFF6FA8, 0xFF2EC4B6)

/** Fills [out] with (x, y, heading, width) for each point along a tentacle, in pixels. */
private fun rmTentacleSpine(bx: Float, by: Float, heading: Float, len: Float, w0: Float, curl: Float, t: Float, seed: Float, out: FloatArray) {
    var x = bx
    var y = by
    var a = heading
    val step = len / TN
    for (i in 0..TN) {
        val s = i / TN.toFloat()
        out[i * 4] = x
        out[i * 4 + 1] = y
        out[i * 4 + 2] = a
        out[i * 4 + 3] = w0 * (1f - 0.8f * s)
        val wave = sin(t * 2.3f + seed - s * 5f) * 0.5f
        a += (curl * s * s * 3.4f + wave * (0.25f + s)) / TN
        x += cos(a) * step
        y += sin(a) * step
    }
}

private fun rmTentaclePath(sp: FloatArray): Path {
    val n = TN
    fun lx(i: Int) = sp[i * 4] + sin(sp[i * 4 + 2]) * sp[i * 4 + 3] * 0.5f
    fun ly(i: Int) = sp[i * 4 + 1] - cos(sp[i * 4 + 2]) * sp[i * 4 + 3] * 0.5f
    fun rx(i: Int) = sp[i * 4] - sin(sp[i * 4 + 2]) * sp[i * 4 + 3] * 0.5f
    fun ry(i: Int) = sp[i * 4 + 1] + cos(sp[i * 4 + 2]) * sp[i * 4 + 3] * 0.5f
    return Path().apply {
        moveTo(lx(0), ly(0))
        for (i in 1 until n) quadraticTo(lx(i), ly(i), (lx(i) + lx(i + 1)) / 2f, (ly(i) + ly(i + 1)) / 2f)
        lineTo(lx(n), ly(n))
        val tw = sp[n * 4 + 3]
        quadraticTo(sp[n * 4] + cos(sp[n * 4 + 2]) * tw * 1.2f, sp[n * 4 + 1] + sin(sp[n * 4 + 2]) * tw * 1.2f, rx(n), ry(n))
        for (i in n - 1 downTo 1) quadraticTo(rx(i), ry(i), (rx(i) + rx(i - 1)) / 2f, (ry(i) + ry(i - 1)) / 2f)
        lineTo(rx(0), ry(0))
        close()
    }
}

private fun DrawScope.rmOctopus(f: Fixture, u: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val a = rmAnim(f, pen)
    val body = Color(OCTO_COLOURS[f.mode.mod(4)])
    val back = body.darken(0.12f)
    val sucker = body.lighten(0.5f)
    rmShadow(u, 0.3f, 0.1f)
    val sp = FloatArray((TN + 1) * 4)
    // (base x, base y, heading in degrees, length, curl, front) for the eight arms.
    val arms = floatArrayOf(
        -0.045f, -0.11f, 200f, 0.15f, 1.2f, 0f,
        0.045f, -0.11f, -20f, 0.15f, -1.2f, 0f,
        -0.03f, -0.1f, 150f, 0.13f, 1.4f, 0f,
        0.03f, -0.1f, 30f, 0.13f, -1.4f, 0f,
        -0.055f, -0.1f, 118f, 0.125f, 1.3f, 1f,
        0.055f, -0.1f, 62f, 0.125f, -1.3f, 1f,
        -0.02f, -0.095f, 100f, 0.105f, 1.6f, 1f,
        0.02f, -0.095f, 80f, 0.105f, -1.6f, 1f,
    )
    fun arm(k: Int) {
        val i = k * 6
        val heading = arms[i + 2] * RM_PI / 180f
        rmTentacleSpine(arms[i] * u, arms[i + 1] * u, heading, arms[i + 3] * u, 0.032f * u, arms[i + 4], t, k * 1.9f, sp)
        val front = arms[i + 5] > 0f
        val path = rmTentaclePath(sp)
        inked(path, if (front) body else back, pen)
        if (front) {
            val side = if (arms[i + 4] > 0f) 1f else -1f
            for (j in 3 until TN - 1 step 2) {
                val w = sp[j * 4 + 3]
                val ang = sp[j * 4 + 2]
                val c = Offset(sp[j * 4] - sin(ang) * w * 0.22f * side, sp[j * 4 + 1] + cos(ang) * w * 0.22f * side)
                drawCircle(sucker, w * 0.2f, c)
                drawCircle(Ink.line, w * 0.2f, c, style = Stroke(pen.lw * 0.45f))
            }
        }
    }
    for (k in 0 until 4) arm(k)
    // The head, which puffs up after squirting ink.
    val puff = 1f + a * 0.22f
    withTransform({ scale(puff, puff, pivot = o(0f, -0.1f)) }) {
        val siphon = Path().apply {
            moveTo(0.07f * u, -0.15f * u)
            quadraticTo(0.1f * u, -0.15f * u, 0.108f * u, -0.128f * u)
            lineTo(0.094f * u, -0.122f * u)
            quadraticTo(0.085f * u, -0.135f * u, 0.07f * u, -0.132f * u)
            close()
        }
        inked(siphon, body.darken(0.05f), pen)
        val head = Path().apply {
            moveTo(0f, -0.272f * u)
            cubicTo(0.06f * u, -0.272f * u, 0.094f * u, -0.225f * u, 0.09f * u, -0.17f * u)
            cubicTo(0.087f * u, -0.12f * u, 0.06f * u, -0.092f * u, 0f, -0.092f * u)
            cubicTo(-0.06f * u, -0.092f * u, -0.087f * u, -0.12f * u, -0.09f * u, -0.17f * u)
            cubicTo(-0.094f * u, -0.225f * u, -0.06f * u, -0.272f * u, 0f, -0.272f * u)
            close()
        }
        inked(head, body, pen, outline = false)
        clipPath(head) {
            for ((x, y, r) in listOf(Triple(0.036f, -0.232f, 0.012f), Triple(-0.048f, -0.22f, 0.009f), Triple(0.062f, -0.2f, 0.007f), Triple(-0.02f, -0.25f, 0.006f))) {
                drawCircle(body.darken(0.18f), r * u, o(x, y))
            }
        }
        drawPath(head, Ink.line, style = pen.stroke)
        shine(o(-0.04f, -0.245f), 0.024f * u, 0.012f * u, 0.7f)
        // Big eyes that look around and blink; a wink and a tongue after a squirt.
        val cheeky = a > 0.12f
        val blink = !cheeky && rmFrac(t * 0.3f + 0.4f) < 0.035f
        val look = Offset(sin(t * 0.7f) * 0.005f * u, 0.003f * u)
        for (side in RM_SIDES) {
            val e = o(side * 0.035f, -0.175f)
            val wink = cheeky && side > 0f
            if (blink || wink) {
                val lid = Path().apply {
                    moveTo(e.x - 0.016f * u, e.y + (if (wink) 0.002f else 0f) * u)
                    quadraticTo(e.x, e.y - (if (wink) 0.014f else -0.008f) * u, e.x + 0.016f * u, e.y + (if (wink) 0.002f else 0f) * u)
                }
                drawPath(lid, Ink.line, style = Stroke(pen.lw * 1.3f, cap = StrokeCap.Round))
            } else {
                val er = Rect(Offset(e.x - 0.019f * u, e.y - 0.023f * u), Offset(e.x + 0.019f * u, e.y + 0.021f * u))
                drawOval(Color.White, er.topLeft, er.size)
                drawOval(Ink.line, er.topLeft, er.size, style = pen.stroke)
                val pc = Offset(e.x + look.x + side * 0.002f * u, e.y + look.y)
                drawCircle(Ink.line, 0.011f * u, pc)
                drawCircle(Color.White, 0.004f * u, Offset(pc.x - 0.004f * u, pc.y - 0.004f * u))
                drawCircle(Color.White, 0.0018f * u, Offset(pc.x + 0.004f * u, pc.y + 0.004f * u))
            }
            val cheek = if (cheeky) 0.03f else 0.022f
            drawOval(Ink.blush, Offset(e.x + side * 0.022f * u - cheek * 0.5f * u, -0.142f * u), Size(cheek * u, cheek * 0.5f * u))
        }
        val mouth = Path().apply {
            moveTo(-0.012f * u, -0.136f * u)
            quadraticTo(0f, (if (cheeky) -0.128f else -0.126f) * u, 0.012f * u, -0.136f * u)
        }
        if (cheeky) {
            val tongue = Path().apply {
                moveTo(-0.007f * u, -0.132f * u)
                lineTo(0.007f * u, -0.132f * u)
                quadraticTo(0.009f * u, -0.114f * u, 0f, -0.114f * u)
                quadraticTo(-0.009f * u, -0.114f * u, -0.007f * u, -0.132f * u)
                close()
            }
            inked(tongue, Color(0xFFFF7F9A), pen, shade = false)
            drawLine(Color(0xFFD9546E), Offset(0f, -0.13f * u), Offset(0f, -0.12f * u), pen.lw * 0.5f)
        }
        drawPath(mouth, Ink.line, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
    }
    for (k in 4 until 8) arm(k)
}
