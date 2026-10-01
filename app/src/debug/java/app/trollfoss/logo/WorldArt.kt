package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.darken
import app.trollfoss.ui.art.drawHouse3d
import app.trollfoss.ui.art.hash01
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.tuftPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * The world as a panorama, drawn in a 2400 x 600 design space: sky, a hazy far range, the long snowy massif
 * with the great waterfall, forest hills, the fjord with a village, a ferris wheel and a balloon, meadow
 * and foreground grass. Banner and social card both use these layers.
 */

internal const val FALL_X = 1700f
internal val horizon = Color(0xFFFFEFC9)

internal fun DrawScope.worldSky() {
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF3B98EA), 0.3f to Color(0xFF6DBFF5), 0.6f to Color(0xFFBCE6FB), 0.84f to horizon,
            startY = 0f, endY = 520f,
        ),
        Offset(-40f, -40f), Size(2480f, 640f),
    )
}

internal fun DrawScope.worldSun(c: Offset, r: Float) {
    lgGlow(c, r * 6f, Color(0xFFFFE08A), 0.55f)
    for (i in 0 until 14) {
        val a = i * (360f / 14f) + 4f
        drawLine(L.sunTop, polar(c, r * 1.3f, a), polar(c, if (i % 2 == 0) r * 1.85f else r * 1.6f, a), strokeWidth = r * 0.16f, cap = StrokeCap.Round)
    }
    lgInkCircle(c, r, L.sun, r * 0.1f)
    drawCircle(Color.White.copy(alpha = 0.55f), r * 0.22f, Offset(c.x - r * 0.34f, c.y - r * 0.38f))
}

internal fun DrawScope.worldClouds() {
    lgCloud(330f, 120f, 62f, 4f)
    lgCloud(770f, 62f, 40f, 4f)
    lgCloud(1480f, 40f, 34f, 4f)
    lgCloud(2150f, 92f, 44f, 4f)
    lgCloud(2350f, 40f, 30f, 4f)
}

/** The pale far ranges: low hazy peaks all the way across. */
internal fun DrawScope.worldFarRanges() {
    var i = 0
    var x = -60f
    while (x < 2520f) {
        val top = 300f + 70f * hash01(i, 31)
        val h = 0.9f + 0.4f * hash01(i, 32)
        lgMountain(
            x, top, 170f * h, 180f * h, 480f,
            Color(0xFFBFD1EC), Color(0xFFAEC2E4), Color(0xFFEFF4FC), Color(0xFFD7E2F4), 0.38f, 3f, 40 + i,
        )
        x += 210f + 90f * hash01(i, 33)
        i++
    }
}

internal fun DrawScope.haze(y0: Float, y1: Float, alpha: Float, x0: Float = -40f, x1: Float = 2440f) {
    drawRect(
        Brush.verticalGradient(0f to horizon.copy(alpha = 0f), 1f to horizon.copy(alpha = alpha), startY = y0, endY = y1),
        Offset(x0, y0), Size(x1 - x0, y1 - y0),
    )
}

/** The long snowy massif: a hazy back row and a front row of big peaks. */
internal fun DrawScope.worldMassif() {
    val line = Color(0xFF3B4A8A)
    val back = listOf(
        floatArrayOf(1010f, 150f, 210f, 200f), floatArrayOf(1330f, 96f, 230f, 240f), floatArrayOf(1810f, 66f, 270f, 240f),
        floatArrayOf(2160f, 104f, 230f, 250f), floatArrayOf(2460f, 150f, 230f, 200f),
    )
    for ((i, p) in back.withIndex()) {
        lgMountain(p[0], p[1], p[2], p[3], 480f, Color(0xFFA3B8DE), Color(0xFF90A6D2), L.snow, L.snowShade, 0.46f, 3f, 60 + i, outline = 0.35f, outlineColor = line)
    }
    val front = listOf(
        floatArrayOf(850f, 290f, 200f, 180f), floatArrayOf(1100f, 214f, 210f, 220f), floatArrayOf(1470f, 140f, 250f, 230f),
        floatArrayOf(FALL_X, 54f, 270f, 280f), floatArrayOf(2000f, 136f, 220f, 230f), floatArrayOf(2290f, 200f, 240f, 230f),
    )
    for ((i, p) in front.withIndex()) {
        lgMountain(p[0], p[1], p[2], p[3], 480f, Color(0xFF8CA4D3), Color(0xFF7189BF), L.snow, L.snowShade, 0.44f, 4f, 80 + i, outline = 0.6f, outlineColor = line)
    }
    haze(360f, 480f, 0.6f, 700f, 2440f)
}

/** The great waterfall: a bright plume pouring out of a gorge into a turquoise pool, with a rainbow in the mist. */
internal fun DrawScope.worldFall() {
    val cx = FALL_X
    val lw = 4f
    val pen = logoPen(lw)
    val topY = 214f
    val baseY = 440f
    // Two rock shoulders with snow on top, the water pours between them.
    val rock = Color(0xFF5A6FA0)
    val wallL = Path().apply {
        moveTo(cx - 30f, topY + 6f)
        lineTo(cx - 62f, topY - 8f)
        lineTo(cx - 98f, topY - 30f)
        lineTo(cx - 130f, topY - 18f)
        lineTo(cx - 152f, topY + 22f)
        lineTo(cx - 170f, topY + 110f)
        lineTo(cx - 178f, baseY + 40f)
        lineTo(cx - 10f, baseY + 40f)
        close()
    }
    val wallR = Path().apply {
        moveTo(cx + 30f, topY + 6f)
        lineTo(cx + 62f, topY - 8f)
        lineTo(cx + 98f, topY - 30f)
        lineTo(cx + 130f, topY - 18f)
        lineTo(cx + 152f, topY + 22f)
        lineTo(cx + 170f, topY + 110f)
        lineTo(cx + 178f, baseY + 40f)
        lineTo(cx + 10f, baseY + 40f)
        close()
    }
    for ((i, wall) in listOf(wallL, wallR).withIndex()) {
        fun x(dx: Float) = if (i == 0) cx - dx else cx + dx
        inked(wall, rock, pen)
        clipPath(wall) {
            drawPath(
                polyPath(
                    x(200f), topY - 50f, x(0f), topY - 50f, x(0f), topY + 24f, x(36f), topY + 30f, x(60f), topY + 14f,
                    x(86f), topY + 44f, x(112f), topY + 28f, x(134f), topY + 56f, x(160f), topY + 40f, x(200f), topY + 60f,
                ),
                L.snow,
            )
            if (i == 0) {
                drawPath(polyPath(x(60f), topY - 50f, x(0f), topY - 50f, x(0f), topY + 24f, x(36f), topY + 30f, x(60f), topY + 14f), L.snowShade)
            } else {
                drawPath(polyPath(x(100f), topY - 50f, x(200f), topY - 50f, x(200f), topY + 60f, x(160f), topY + 40f, x(134f), topY + 56f, x(112f), topY + 28f, x(86f), topY + 44f), L.snowShade)
            }
            drawPath(polyPath(x(14f), topY + 70f, x(70f), topY + 100f, x(70f), baseY + 60f, x(14f), baseY + 60f), rock.darken(0.18f))
            for ((dy, dx, w) in listOf(Triple(104f, 150f, 36f), Triple(160f, 138f, 30f))) {
                val x0 = x(dx)
                val x1 = x(dx - w)
                drawLine(rock.lighten(0.24f), Offset(x0, topY + dy), Offset(x1, topY + dy - 4f), strokeWidth = 4f, cap = StrokeCap.Round)
            }
        }
        drawPath(wall, Ink.line, style = pen.stroke)
    }
    // The rainbow in the mist, in front of the walls.
    lgRainbow(Offset(cx, baseY + 4f), 178f, 11.5f, 0.92f)
    // The plume.
    val fall = Path().apply {
        moveTo(cx - 34f, topY + 4f)
        quadraticTo(cx, topY + 12f, cx + 34f, topY + 4f)
        quadraticTo(cx + 36f, topY + 130f, cx + 92f, baseY + 10f)
        lineTo(cx - 92f, baseY + 10f)
        quadraticTo(cx - 36f, topY + 130f, cx - 34f, topY + 4f)
        close()
    }
    drawPath(fall, Brush.verticalGradient(0f to Color(0xFFD6F0FF), 0.3f to Color.White, 1f to Color(0xFFD2EEFF), startY = topY, endY = baseY))
    clipPath(fall) {
        drawPath(polyPath(cx + 8f, topY - 10f, cx + 120f, topY - 10f, cx + 120f, baseY + 20f, cx + 34f, baseY + 20f), Color(0xFFB9E0F7))
        for ((i, dx) in listOf(-18f, -6f, 8f, 22f).withIndex()) {
            drawLine(Color(0xFF8CCBF3), Offset(cx + dx, topY + 20f + i * 12f), Offset(cx + dx * 2.2f, baseY - 10f), strokeWidth = 4.5f, cap = StrokeCap.Round)
        }
        drawLine(Color.White, Offset(cx - 22f, topY + 18f), Offset(cx - 30f, topY + 100f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawArc(Color.White, 190f, 160f, false, Offset(cx - 64f, topY + 110f), Size(128f, 22f), style = Stroke(6f, cap = StrokeCap.Round))
    }
    drawPath(
        Path().apply {
            moveTo(cx - 34f, topY + 4f)
            quadraticTo(cx - 36f, topY + 130f, cx - 92f, baseY + 10f)
            moveTo(cx + 34f, topY + 4f)
            quadraticTo(cx + 36f, topY + 130f, cx + 92f, baseY + 10f)
        },
        Ink.line, style = Stroke(lw, cap = StrokeCap.Round),
    )
    drawPath(Path().apply { moveTo(cx - 34f, topY + 4f); quadraticTo(cx, topY + 12f, cx + 34f, topY + 4f) }, Ink.line, style = Stroke(lw, cap = StrokeCap.Round))
}

/** The pool at the foot of the falls: turquoise, foam at the plume, mist above. */
internal fun DrawScope.worldPool() {
    val cx = FALL_X
    val lw = 4f
    val pool = Path().apply {
        moveTo(cx - 196f, 478f)
        quadraticTo(cx - 150f, 436f, cx - 90f, 438f)
        quadraticTo(cx, 428f, cx + 90f, 438f)
        quadraticTo(cx + 150f, 436f, cx + 196f, 478f)
        lineTo(cx + 196f, 510f)
        lineTo(cx - 196f, 510f)
        close()
    }
    drawPath(pool, Brush.verticalGradient(0f to Color(0xFF63D3DC), 1f to Color(0xFF2394B4), startY = 428f, endY = 510f))
    drawPath(Path().apply { moveTo(cx - 196f, 478f); quadraticTo(cx - 150f, 436f, cx - 90f, 438f); quadraticTo(cx, 428f, cx + 90f, 438f); quadraticTo(cx + 150f, 436f, cx + 196f, 478f) }, Ink.line, style = Stroke(lw, cap = StrokeCap.Round))
    for ((dx, y, w) in listOf(Triple(-150f, 462f, 40f), Triple(-110f, 478f, 30f), Triple(90f, 464f, 44f), Triple(130f, 480f, 28f))) {
        drawLine(Color.White.copy(alpha = 0.7f), Offset(cx + dx, y), Offset(cx + dx + w, y), strokeWidth = 4f, cap = StrokeCap.Round)
    }
    for ((i, dx) in listOf(-78f, -52f, -26f, 0f, 26f, 52f, 78f).withIndex()) {
        val r = 15f - kotlin.math.abs(i - 3) * 1.2f
        val y = 442f - (if (i % 2 == 0) 0f else 7f)
        drawCircle(Color(0xFFF4FBFF), r, Offset(cx + dx, y))
        drawCircle(Color(0xFFBFE3F5), r, Offset(cx + dx + 2f, y + 5f), alpha = 0.0f)
    }
    drawLine(Color.White, Offset(cx - 70f, 450f), Offset(cx + 70f, 450f), strokeWidth = 8f, cap = StrokeCap.Round)
    val mist = Path()
    for ((x, y, r) in listOf(Triple(cx - 96f, 424f, 26f), Triple(cx - 72f, 412f, 22f), Triple(cx - 120f, 430f, 20f), Triple(cx + 100f, 424f, 28f), Triple(cx + 74f, 410f, 22f), Triple(cx + 124f, 430f, 20f), Triple(cx - 40f, 400f, 18f), Triple(cx + 42f, 398f, 18f))) {
        mist.addOval(Rect(x - r, y - r * 0.8f, x + r, y + r * 0.8f))
    }
    drawPath(mist, Color.White, alpha = 0.6f)
}

/** One ridge of pines: a hill in [body] with flat pines on it. */
internal fun DrawScope.forestRow(x0: Float, x1: Float, base: Float, amp: Float, salt: Int, spacing: Float, minH: Float, maxH: Float, body: Color, shade: Color?, skip: Float = 0f, trees: Boolean = true) {
    fun ridge(x: Float) = base - amp * (0.55f + 0.3f * sin(x * 0.011f + salt * 1.3f) + 0.15f * sin(x * 0.027f + salt * 2.9f))
    val hill = Path().apply {
        moveTo(x0, 620f)
        lineTo(x0, ridge(x0))
        var x = x0
        while (x < x1) {
            x += 24f
            lineTo(x, ridge(x))
        }
        lineTo(x1, 620f)
        close()
    }
    val pines = Path()
    val shades = Path()
    if (trees) {
        var i = 0
        var x = x0
        while (x < x1) {
            if (hash01(i, salt + 7) >= skip) {
                val lx = x + (hash01(i, salt + 8) - 0.5f) * spacing * 0.8f
                val hgt = minH + (maxH - minH) * hash01(i, salt + 9)
                val gy = ridge(lx) + hgt * 0.06f
                pines.addPinePath(lx, gy, hgt * 0.5f, hgt)
                if (shade != null) shades.addPineShadePath(lx, gy, hgt * 0.5f, hgt)
            }
            x += spacing
            i++
        }
    }
    drawPath(hill, body)
    drawPath(pines, body)
    if (shade != null) drawPath(shades, shade)
}

internal fun Path.addPinePath(cx: Float, by: Float, w: Float, h: Float) {
    val hw = w / 2f
    moveTo(cx, by - h)
    lineTo(cx + hw * 0.52f, by - h * 0.6f)
    lineTo(cx + hw * 0.3f, by - h * 0.6f)
    lineTo(cx + hw * 0.8f, by - h * 0.28f)
    lineTo(cx + hw * 0.5f, by - h * 0.28f)
    lineTo(cx + hw, by)
    lineTo(cx - hw, by)
    lineTo(cx - hw * 0.5f, by - h * 0.28f)
    lineTo(cx - hw * 0.8f, by - h * 0.28f)
    lineTo(cx - hw * 0.3f, by - h * 0.6f)
    lineTo(cx - hw * 0.52f, by - h * 0.6f)
    close()
}

internal fun Path.addPineShadePath(cx: Float, by: Float, w: Float, h: Float) {
    val hw = w / 2f
    moveTo(cx + hw * 0.02f, by - h * 0.98f)
    lineTo(cx + hw * 0.52f, by - h * 0.6f)
    lineTo(cx + hw * 0.3f, by - h * 0.6f)
    lineTo(cx + hw * 0.8f, by - h * 0.28f)
    lineTo(cx + hw * 0.5f, by - h * 0.28f)
    lineTo(cx + hw, by)
    lineTo(cx + hw * 0.15f, by)
    close()
}

/** The fjord: calm blue water with a sailing boat, and the far shore beyond. */
internal fun DrawScope.worldFjord(x0: Float, x1: Float) {
    val lw = 4f
    // Far shore.
    forestRow(x0 - 20f, x1 + 20f, 420f, 12f, 5, 30f, 22f, 40f, Color(0xFF8FB3BF), null)
    val water = Path().apply {
        moveTo(x0, 416f)
        lineTo(x1, 416f)
        lineTo(x1, 484f)
        lineTo(x0, 484f)
        close()
    }
    drawPath(water, Brush.verticalGradient(0f to Color(0xFF7AC0EC), 1f to Color(0xFF2F73BD), startY = 416f, endY = 484f))
    for (i in 0 until 9) {
        val y = 428f + i * 6.5f
        val w = 26f + 20f * hash01(i, 71)
        val x = x0 + 20f + (x1 - x0 - 80f) * hash01(i, 72)
        drawLine(Color.White.copy(alpha = 0.5f), Offset(x, y), Offset(x + w, y), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

internal fun DrawScope.sailboat(bx: Float, by: Float, s: Float, lw: Float) {
    val hull = Path().apply {
        moveTo(bx - 0.55f * s, by - 0.16f * s)
        lineTo(bx + 0.55f * s, by - 0.16f * s)
        quadraticTo(bx + 0.45f * s, by + 0.02f * s, bx + 0.3f * s, by + 0.02f * s)
        lineTo(bx - 0.3f * s, by + 0.02f * s)
        quadraticTo(bx - 0.45f * s, by + 0.02f * s, bx - 0.55f * s, by - 0.16f * s)
        close()
    }
    val main = polyPath(bx + 0.04f * s, by - 1.0f * s, bx + 0.5f * s, by - 0.22f * s, bx + 0.04f * s, by - 0.22f * s)
    val jib = polyPath(bx - 0.04f * s, by - 0.85f * s, bx - 0.04f * s, by - 0.22f * s, bx - 0.4f * s, by - 0.22f * s)
    drawLine(Ink.line, Offset(bx, by - 0.14f * s), Offset(bx, by - 1.04f * s), strokeWidth = lw, cap = StrokeCap.Round)
    lgInked(main, Color.White, lw * 0.8f)
    lgInked(jib, L.sunTop, lw * 0.8f)
    lgInked(hull, L.berry, lw)
    // A reflection.
    drawLine(Color.White.copy(alpha = 0.5f), Offset(bx - 0.4f * s, by + 0.12f * s), Offset(bx + 0.4f * s, by + 0.12f * s), strokeWidth = lw, cap = StrokeCap.Round)
}

internal fun DrawScope.ferrisWheel(cx: Float, cy: Float, r: Float) {
    val lw = r * 0.04f
    val white = Color(0xFFF7F4EE)
    // Legs.
    for (side in listOf(-1f, 1f)) {
        val foot = Offset(cx + side * r * 0.62f, cy + r * 1.28f)
        drawLine(Ink.line, Offset(cx, cy), foot, strokeWidth = r * 0.12f, cap = StrokeCap.Round)
        drawLine(Color(0xFFE2504A), Offset(cx, cy), foot, strokeWidth = r * 0.07f, cap = StrokeCap.Round)
    }
    drawLine(Ink.line, Offset(cx - r * 0.74f, cy + r * 1.28f), Offset(cx + r * 0.74f, cy + r * 1.28f), strokeWidth = r * 0.12f, cap = StrokeCap.Round)
    drawLine(white, Offset(cx - r * 0.74f, cy + r * 1.28f), Offset(cx + r * 0.74f, cy + r * 1.28f), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
    // Spokes and rim.
    for (i in 0 until 8) {
        val a = i * 45f + 10f
        drawLine(Ink.line, Offset(cx, cy), polar(Offset(cx, cy), r, a), strokeWidth = lw * 1.4f)
    }
    drawCircle(Ink.line, r, Offset(cx, cy), style = Stroke(r * 0.1f))
    drawCircle(white, r, Offset(cx, cy), style = Stroke(r * 0.05f))
    drawCircle(Ink.line, r * 0.55f, Offset(cx, cy), style = Stroke(lw * 1.4f))
    // Gondolas.
    val cols = listOf(L.berry, L.sun, L.sea, L.mint, L.grape)
    for (i in 0 until 10) {
        val a = i * 36f + 10f
        val p = polar(Offset(cx, cy), r, a)
        val cab = Rect(p.x - r * 0.12f, p.y + r * 0.02f, p.x + r * 0.12f, p.y + r * 0.26f)
        drawLine(Ink.line, p, Offset(p.x, p.y + r * 0.04f), strokeWidth = lw * 1.4f)
        inkedRound(cab, r * 0.05f, cols[i % cols.size], logoPen(lw * 1.3f))
    }
    lgInkCircle(Offset(cx, cy), r * 0.14f, L.sun, lw * 1.4f)
}

internal fun DrawScope.balloon(cx: Float, cy: Float, r: Float) {
    val lw = r * 0.07f
    val env = Path().apply {
        moveTo(cx - r * 0.34f, cy + r * 1.28f)
        cubicTo(cx - r * 1.12f, cy + r * 0.5f, cx - r * 1.0f, cy - r * 1.0f, cx, cy - r * 1.0f)
        cubicTo(cx + r * 1.0f, cy - r * 1.0f, cx + r * 1.12f, cy + r * 0.5f, cx + r * 0.34f, cy + r * 1.28f)
        close()
    }
    drawPath(env, L.berry)
    clipPath(env) {
        val cols = listOf(L.berry, L.sunTop, L.berry, L.sunTop, L.berry)
        for (i in 0 until 5) {
            val x0 = cx - r * 1.2f + i * r * 0.48f
            drawRect(cols[i], Offset(x0, cy - r * 1.2f), Size(r * 0.48f + 1f, r * 2.6f))
        }
        // The shaded right side.
        drawOval(Ink.line.copy(alpha = 0.18f), Offset(cx + r * 0.2f, cy - r * 1.2f), Size(r * 1.4f, r * 2.8f))
        drawArc(Color.White.copy(alpha = 0.5f), 200f, 40f, false, Offset(cx - r * 0.85f, cy - r * 0.8f), Size(r * 1.1f, r * 1.1f), style = Stroke(lw * 0.9f, cap = StrokeCap.Round))
    }
    drawPath(env, Ink.line, style = Stroke(lw, join = StrokeJoin.Round))
    val basket = Rect(cx - r * 0.2f, cy + r * 1.62f, cx + r * 0.2f, cy + r * 1.92f)
    drawLine(Ink.line, Offset(cx - r * 0.3f, cy + r * 1.28f), Offset(basket.left + 3f, basket.top), strokeWidth = lw * 0.6f)
    drawLine(Ink.line, Offset(cx + r * 0.3f, cy + r * 1.28f), Offset(basket.right - 3f, basket.top), strokeWidth = lw * 0.6f)
    inkedRound(basket, r * 0.05f, L.wood, logoPen(lw * 0.9f))
}

internal fun DrawScope.birds(points: List<Triple<Float, Float, Float>>) {
    for ((x, y, s) in points) {
        val p = Path().apply {
            moveTo(x - s, y - s * 0.1f)
            quadraticTo(x - s * 0.45f, y - s * 0.6f, x, y)
            quadraticTo(x + s * 0.45f, y - s * 0.6f, x + s, y - s * 0.1f)
        }
        drawPath(p, Ink.line, alpha = 0.85f, style = Stroke(s * 0.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** The village on its knoll: red and yellow houses with smoke, in oblique 3D like the game. */
internal fun DrawScope.worldVillage() = translate(-70f, 0f) {
    val lw = 4f
    val pen = logoPen(lw)
    val knoll = Path().apply {
        moveTo(1930f, 480f)
        quadraticTo(1990f, 424f, 2090f, 430f)
        quadraticTo(2170f, 432f, 2230f, 480f)
        close()
    }
    inked(knoll, Color(0xFF6FAE5A), pen)
    val slate = Color(0xFF4B4756)
    drawHouse3d(1990f, 462f, 62f, 28f, 22f, 52f, L.falun, slate, pen, 0f, door = Color(0xFF6E4A33), sideWindows = 1, chimney = true, t = 1.3f)
    drawHouse3d(2080f, 452f, 56f, 26f, 20f, 46f, L.ochre, slate, pen, 0f, door = Color(0xFF6E4A33), sideWindows = 1)
    drawHouse3d(2150f, 462f, 60f, 27f, 21f, 50f, L.falunLight, slate, pen, 0f, door = Color(0xFF6E4A33), sideWindows = 1, chimney = true, t = 2.4f)
    smoke(2020f, 398f)
    smoke(2179f, 401f)
    // A few pines on the knoll.
    lgPine(1950f, 474f, 26f, 56f, L.gran, lw)
    lgPine(2214f, 476f, 24f, 50f, L.granLight, lw)
}

/** The meadow in front of the water and trees, and the foreground grass with tufts and flowers. */
internal fun DrawScope.worldMeadow(w: Float = 2400f, top: Float = 474f) {
    val lw = 4f
    val meadowTop = Path().apply {
        moveTo(-40f, top + 4f)
        var x = -40f
        while (x < w + 40f) {
            x += 30f
            lineTo(x, top + 6f * sin(x * 0.01f) + 3f * sin(x * 0.027f + 1f) + 30f * kotlin.math.exp(-((x - FALL_X) / 190f) * ((x - FALL_X) / 190f)))
        }
    }
    val meadow = Path().apply {
        addPath(meadowTop)
        lineTo(w + 40f, 640f)
        lineTo(-40f, 640f)
        close()
    }
    drawPath(meadow, Brush.verticalGradient(0f to Color(0xFF9AD070), 0.5f to Color(0xFF6FB354), 1f to Color(0xFF4E9A48), startY = top, endY = 600f))
    drawPath(meadowTop, Ink.line, style = Stroke(lw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Soft patches of darker grass.
    for (i in 0 until 14) {
        val x = w * hash01(i, 81)
        val y = top + 40f + (120f) * hash01(i, 82)
        drawOval(Color(0xFF4E9A48).copy(alpha = 0.28f), Offset(x - 60f, y), Size(120f, 14f))
    }
}

internal fun DrawScope.worldTufts(w: Float = 2400f, y0: Float = 560f, span: Float = 36f) {
    val lw = 3.5f
    var i = 0
    var x = 20f
    while (x < w) {
        val y = y0 + span * hash01(i, 91)
        val h = 20f + 14f * hash01(i, 92)
        val p = tuftPath(x, y, h, 0f)
        drawPath(p, Color(0xFF3F8A3C))
        drawPath(p, Ink.line, style = Stroke(lw, join = StrokeJoin.Round))
        x += 70f + 90f * hash01(i, 93)
        i++
    }
}

internal fun DrawScope.worldFlowers(w: Float = 2400f, y0: Float = 520f, y1: Float = 596f, count: Int = 70) {
    val petal = listOf(Color.White, L.sunTop, L.berryTop, Color.White, Color(0xFFFFF4B0))
    for (i in 0 until count) {
        val x = w * hash01(i, 101)
        val y = y0 + (y1 - y0) * hash01(i, 102)
        val r = 5f + 3f * hash01(i, 103)
        val col = petal[i % petal.size]
        drawCircle(Ink.line, r * 1.6f, Offset(x, y))
        for (a in 0 until 5) drawCircle(col, r * 0.9f, polar(Offset(x, y), r * 0.9f, a * 72f + 18f))
        drawCircle(if (col == L.sunTop || col == Color(0xFFFFF4B0)) L.berry else L.sun, r * 0.7f, Offset(x, y))
    }
}

/** A mid-ground bush: a lumpy green blob with a shaded side. */
internal fun DrawScope.bush(cx: Float, by: Float, w: Float, color: Color, lw: Float) {
    val p = Path().apply {
        addOval(Rect(cx - w * 0.5f, by - w * 0.36f, cx - w * 0.04f, by))
        addOval(Rect(cx - w * 0.3f, by - w * 0.52f, cx + w * 0.26f, by))
        addOval(Rect(cx + w * 0.02f, by - w * 0.4f, cx + w * 0.5f, by))
    }
    drawPath(p, Ink.line, style = Stroke(lw * 2f, join = StrokeJoin.Round))
    lgInked(p, color, lw)
}


/** Smoke curling up from a chimney: three round puffs, growing as they rise. */
internal fun DrawScope.smoke(x: Float, y: Float) {
    for ((i, p) in listOf(Triple(0f, 0f, 5f), Triple(7f, -13f, 7f), Triple(17f, -29f, 9.5f)).withIndex()) {
        val c = Offset(x + p.first, y + p.second)
        drawCircle(Color.White, p.third, c, alpha = 0.92f - i * 0.12f)
        drawCircle(Ink.line, p.third, c, alpha = 0.28f, style = Stroke(2.4f))
    }
}

/** Big blades of grass and a few daisies cropped by the bottom edge, to put the cast in the middle distance. */
internal fun DrawScope.worldForeground(xs: List<Float>, y: Float = 604f) {
    for ((i, x) in xs.withIndex()) {
        val h = 58f + 22f * hash01(i, 111)
        val p = tuftPath(x, y, h, 0f)
        drawPath(p, Color(0xFF3F8A3C))
        drawPath(p, Ink.line, style = Stroke(4.5f, join = StrokeJoin.Round))
        val fx = x + 34f
        val fy = y - h * 0.9f
        drawLine(Ink.line, Offset(fx, y), Offset(fx - 4f, fy), strokeWidth = 7f, cap = StrokeCap.Round)
        drawLine(Color(0xFF3F8A3C), Offset(fx, y), Offset(fx - 4f, fy), strokeWidth = 3.4f, cap = StrokeCap.Round)
        drawCircle(Ink.line, 16f, Offset(fx - 4f, fy))
        for (a in 0 until 5) drawCircle(if (i % 2 == 0) Color.White else L.berryTop, 8.5f, polar(Offset(fx - 4f, fy), 9.5f, a * 72f + 18f))
        drawCircle(L.sun, 6f, Offset(fx - 4f, fy))
    }
}
