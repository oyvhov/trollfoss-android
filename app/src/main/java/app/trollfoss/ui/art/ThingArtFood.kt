package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.cos
import kotlin.math.sin

// Food. Every drawing works in fractions of its box: x from -0.5 to 0.5, y from -1 (top) to 0 (bottom).

// ------------------------------------------------------------------ fruit and vegetables

internal fun DrawScope.thApple(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val red = Color(0xFFE8413A)
    val stem = thSketch(w, h) { m(0f, -0.74f); q(0f, -0.9f, 0.08f, -0.99f) }
    drawPath(stem, Ink.line, style = Stroke(w * 0.09f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(stem, ThingInk.bark, style = Stroke(w * 0.09f, cap = StrokeCap.Round))
    val body = thSketch(w, h) {
        m(0f, -0.76f)
        c(0.16f, -0.9f, 0.5f, -0.84f, 0.49f, -0.48f)
        c(0.48f, -0.18f, 0.28f, -0.02f, 0.13f, -0.03f)
        q(0.05f, -0.04f, 0f, -0.07f)
        q(-0.05f, -0.04f, -0.13f, -0.03f)
        c(-0.28f, -0.02f, -0.48f, -0.18f, -0.49f, -0.48f)
        c(-0.5f, -0.84f, -0.16f, -0.9f, 0f, -0.76f)
        z()
    }
    inked(body, red, pen)
    clipPath(body) { thSheen(o(0f, -0.45f), w * 0.5f, 0.34f) }
    drawPath(thSketch(w, h) { m(-0.13f, -0.76f); q(0f, -0.68f, 0.13f, -0.76f) }, red.darken(0.35f), style = pen.thin)
    for (i in 0 until 4) drawCircle(red.lighten(0.42f), w * 0.02f, o(APPLE_DOTS[i * 2], APPLE_DOTS[i * 2 + 1]))
    val leaf = thSketch(w, h) { m(0.03f, -0.84f); q(0.16f, -1.02f, 0.44f, -0.94f); q(0.28f, -0.76f, 0.03f, -0.84f); z() }
    inked(leaf, ThingInk.leaf, pen, shade = false)
    drawLine(ThingInk.leaf.darken(0.3f), o(0.08f, -0.855f), o(0.36f, -0.925f), pen.lw * 0.5f, StrokeCap.Round)
    shine(o(-0.25f, -0.58f), w * 0.15f, h * 0.2f)
}

private val APPLE_DOTS = floatArrayOf(0.22f, -0.5f, 0.3f, -0.32f, 0.12f, -0.22f, 0.34f, -0.62f)

internal fun DrawScope.thBanana(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val yellow = Color(0xFFFFD84A)
    val body = thSketch(w, h) {
        m(-0.42f, -0.7f)
        c(-0.36f, 0.14f, 0.24f, 0.16f, 0.47f, -0.6f)
        q(0.47f, -0.67f, 0.42f, -0.65f)
        c(0.2f, -0.28f, -0.2f, -0.28f, -0.33f, -0.82f)
        q(-0.4f, -0.84f, -0.42f, -0.7f)
        z()
    }
    inked(body, yellow, pen)
    val ridge = thSketch(w, h) { m(-0.36f, -0.72f); c(-0.28f, -0.16f, 0.22f, -0.1f, 0.43f, -0.61f) }
    drawPath(ridge, yellow.darken(0.24f), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    val spot = Color(0xFF8A6A2E)
    drawCircle(spot, w * 0.012f, o(-0.1f, -0.3f))
    drawCircle(spot, w * 0.009f, o(0.12f, -0.22f))
    drawCircle(spot, w * 0.01f, o(0.27f, -0.38f))
    capsule(o(-0.38f, -0.74f), o(-0.44f, -0.94f), w * 0.07f, Color(0xFFB4A04A), pen)
    drawCircle(Color(0xFF5A3E26), w * 0.028f, o(-0.44f, -0.94f))
    drawCircle(Color(0xFF5A3E26), w * 0.022f, o(0.455f, -0.63f))
    shine(o(-0.2f, -0.4f), w * 0.1f, h * 0.12f, 0.55f)
}

internal fun DrawScope.thStrawberry(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val red = Color(0xFFF23A4E)
    val body = thSketch(w, h) {
        m(0f, -0.03f)
        c(-0.26f, -0.1f, -0.5f, -0.4f, -0.47f, -0.62f)
        c(-0.44f, -0.82f, -0.2f, -0.84f, 0f, -0.8f)
        c(0.2f, -0.84f, 0.44f, -0.82f, 0.47f, -0.62f)
        c(0.5f, -0.4f, 0.26f, -0.1f, 0f, -0.03f)
        z()
    }
    inked(body, red, pen)
    clipPath(body) { thSheen(o(0f, -0.46f), w * 0.5f, 0.3f) }
    val seed = Color(0xFFFFE27A)
    val pit = red.darken(0.32f)
    var i = 0
    while (i < BERRY_SEEDS.size) {
        val p = o(BERRY_SEEDS[i], BERRY_SEEDS[i + 1])
        drawOval(pit, Offset(p.x - w * 0.035f, p.y - h * 0.028f), Size(w * 0.075f, h * 0.072f))
        drawOval(seed, Offset(p.x - w * 0.028f, p.y - h * 0.036f), Size(w * 0.055f, h * 0.06f))
        i += 2
    }
    shine(o(-0.24f, -0.6f), w * 0.14f, h * 0.1f)
    val green = Color(0xFF3FAE49)
    val calyx = thSketch(w, h) {
        m(-0.14f, -0.95f); l(-0.47f, -0.72f); l(-0.16f, -0.79f); l(-0.2f, -0.6f); l(0f, -0.75f)
        l(0.2f, -0.6f); l(0.16f, -0.79f); l(0.47f, -0.72f); l(0.14f, -0.95f); z()
    }
    drawPath(calyx, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawPath(calyx, green)
    capsule(o(0f, -0.9f), o(0.06f, -0.99f), w * 0.07f, green, pen)
}

private val BERRY_SEEDS = floatArrayOf(
    -0.24f, -0.56f, 0.02f, -0.6f, 0.26f, -0.55f, -0.3f, -0.38f, -0.08f, -0.42f,
    0.16f, -0.4f, 0.33f, -0.33f, -0.16f, -0.23f, 0.08f, -0.23f, -0.02f, -0.1f,
)

internal fun DrawScope.thCarrot(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val green = Color(0xFF4CAF50)
    val bw = w * 0.13f
    inked(thLens(o(0f, -0.72f), o(-0.38f, -0.97f), bw), green, pen, shade = false)
    inked(thLens(o(0f, -0.72f), o(0.36f, -0.96f), bw), green, pen, shade = false)
    inked(thLens(o(0f, -0.72f), o(0.02f, -0.995f), bw * 1.15f), green.lighten(0.12f), pen, shade = false)
    val orange = Color(0xFFFF8A2B)
    val root = thSketch(w, h) {
        m(-0.46f, -0.72f)
        q(0f, -0.8f, 0.46f, -0.72f)
        c(0.42f, -0.42f, 0.14f, -0.1f, 0.03f, -0.015f)
        q(0f, 0.005f, -0.03f, -0.015f)
        c(-0.14f, -0.1f, -0.42f, -0.42f, -0.46f, -0.72f)
        z()
    }
    inked(root, orange, pen)
    val ridge = orange.darken(0.32f)
    val sw = pen.lw * 0.55f
    drawLine(ridge, o(0.33f, -0.6f), o(0.08f, -0.58f), sw, StrokeCap.Round)
    drawLine(ridge, o(-0.3f, -0.46f), o(-0.06f, -0.44f), sw, StrokeCap.Round)
    drawLine(ridge, o(0.24f, -0.32f), o(0.02f, -0.3f), sw, StrokeCap.Round)
    drawLine(ridge, o(-0.14f, -0.18f), o(0.02f, -0.17f), sw, StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.55f), o(-0.24f, -0.64f), o(-0.13f, -0.4f), w * 0.07f, StrokeCap.Round)
}

internal fun DrawScope.thWatermelon(w: Float, h: Float, pen: Pen) {
    val r = w * 0.49f
    val c = Offset(0f, -h * 0.03f - r)
    fun half(k: Float): Path = Path().apply {
        val rr = r * k
        moveTo(c.x + rr, c.y)
        arcTo(Rect(c.x - rr, c.y - rr, c.x + rr, c.y + rr), 0f, 180f, false)
        close()
    }
    val rind = half(1f)
    inked(rind, Color(0xFF3DA64A), pen)
    clipPath(rind) {
        for (i in 0 until 7) {
            val a = Math.toRadians(12.0 + i * 26.0)
            val ca = cos(a).toFloat()
            val sa = sin(a).toFloat()
            drawLine(Color(0xFF257A36), Offset(c.x + r * 0.9f * ca, c.y + r * 0.9f * sa), Offset(c.x + r * 1.1f * ca, c.y + r * 1.1f * sa), pen.lw * 0.9f)
        }
    }
    drawPath(half(0.89f), Color(0xFFEAF6CF))
    inked(half(0.83f), Color(0xFFFF5A64), pen, outline = false)
    drawPath(rind, Ink.line, style = pen.stroke)
    for (i in 0 until 5) {
        val a = 38.0 + i * 26.0
        val rad = Math.toRadians(a)
        val sx = c.x + (r * 0.5f * cos(rad)).toFloat()
        val sy = c.y + (r * 0.5f * sin(rad)).toFloat()
        rotate(a.toFloat() - 90f, pivot = Offset(sx, sy)) {
            drawOval(Ink.line, Offset(sx - r * 0.045f, sy - r * 0.075f), Size(r * 0.09f, r * 0.15f))
            drawOval(Color.White.copy(alpha = 0.6f), Offset(sx - r * 0.02f, sy - r * 0.05f), Size(r * 0.03f, r * 0.05f))
        }
    }
    drawLine(Color.White.copy(alpha = 0.45f), Offset(c.x - r * 0.7f, c.y + r * 0.08f), Offset(c.x - r * 0.3f, c.y + r * 0.08f), pen.lw * 0.9f, StrokeCap.Round)
}

private val HEX_COS = FloatArray(6) { cos(Math.toRadians(it * 60.0 - 60.0)).toFloat() }
private val HEX_SIN = FloatArray(6) { sin(Math.toRadians(it * 60.0 - 60.0)).toFloat() }

internal fun DrawScope.thCloudberry(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val sepal = Color(0xFF7FAE4A)
    val sb = w * 0.07f
    inked(thLens(o(0f, -0.8f), o(-0.47f, -0.9f), sb), sepal, pen, shade = false)
    inked(thLens(o(0f, -0.8f), o(0.47f, -0.9f), sb), sepal, pen, shade = false)
    inked(thLens(o(0f, -0.8f), o(-0.24f, -0.99f), sb), sepal, pen, shade = false)
    inked(thLens(o(0f, -0.8f), o(0.26f, -0.99f), sb), sepal, pen, shade = false)
    val amber = Color(0xFFFFA531)
    val c = o(0f, -0.43f)
    val r = w * 0.41f
    inkedCircle(c, r, amber, pen)
    val rim = amber.darken(0.38f)
    val rr = r * 0.36f
    for (i in 0 until 7) {
        val p = if (i < 6) Offset(c.x + r * 0.56f * HEX_COS[i], c.y + r * 0.56f * HEX_SIN[i]) else c
        val lit = p.x + p.y - c.x - c.y < r * 0.1f
        drawCircle(if (lit) Color(0xFFFFC45E) else Color(0xFFF59A26), rr, p)
        drawCircle(rim, rr, p, style = Stroke(pen.lw * 0.45f))
        drawCircle(Color.White.copy(alpha = 0.85f), rr * 0.26f, Offset(p.x - rr * 0.32f, p.y - rr * 0.36f))
    }
    thSheen(c, r, 0.3f)
    drawCircle(Ink.line, r, c, style = pen.stroke)
}

internal fun DrawScope.thPotato(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val skin = Color(0xFFC9965A)
    val path = blobPath(
        -0.48f * w, -0.44f * h, -0.36f * w, -0.9f * h, 0.02f * w, -0.99f * h, 0.36f * w, -0.9f * h,
        0.5f * w, -0.5f * h, 0.4f * w, -0.08f * h, 0f, -0.02f * h, -0.38f * w, -0.06f * h,
    )
    inked(path, skin, pen)
    val eye = skin.darken(0.42f)
    for (k in 0 until 3) {
        val p = o(POTATO_EYES[k * 2], POTATO_EYES[k * 2 + 1])
        drawArc(eye, 200f, 140f, false, Offset(p.x - w * 0.05f, p.y - h * 0.04f), Size(w * 0.1f, h * 0.08f), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
        drawCircle(eye, w * 0.012f, Offset(p.x, p.y + h * 0.02f))
    }
    for (k in 0 until 5) drawCircle(skin.darken(0.22f), w * 0.011f, o(POTATO_SPECKS[k * 2], POTATO_SPECKS[k * 2 + 1]))
    shine(o(-0.24f, -0.72f), w * 0.12f, h * 0.1f, 0.45f)
}

private val POTATO_EYES = floatArrayOf(-0.22f, -0.6f, 0.2f, -0.4f, 0.1f, -0.78f)
private val POTATO_SPECKS = floatArrayOf(-0.3f, -0.3f, 0.02f, -0.56f, 0.32f, -0.68f, -0.06f, -0.2f, 0.3f, -0.2f)

// ------------------------------------------------------------------ bakery

internal fun DrawScope.thBread(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    // The loaf's side faces us; its length runs back up and to the right (oblique depth).
    fun x(v: Float) = 0.9f * v - 0.045f
    fun y(v: Float) = 0.87f * v
    val crust = Color(0xFFD08A45)
    val loaf = thSketch(w, h) {
        m(x(-0.46f), y(-0.12f))
        c(x(-0.5f), y(-0.62f), x(-0.3f), y(-0.96f), x(0f), y(-0.96f))
        c(x(0.3f), y(-0.96f), x(0.5f), y(-0.62f), x(0.46f), y(-0.12f))
        q(x(0.45f), y(-0.02f), x(0.36f), y(-0.02f))
        l(x(-0.36f), y(-0.02f))
        q(x(-0.45f), y(-0.02f), x(-0.46f), y(-0.12f))
        z()
    }
    val depth = h * 0.36f
    thExtrude(loaf, obliqueX(depth), obliqueY(depth), crust.lighten(0.06f), pen)
    inked(loaf, crust, pen, outline = false)
    clipPath(loaf) { drawRect(crust.darken(0.18f), Offset(-w, -h * 0.14f), Size(w * 2f, h * 0.14f)) }
    drawPath(loaf, Ink.line, style = pen.stroke)
    val crumb = Color(0xFFF6D9A0)
    for (i in 0 until 3) {
        val cx = x(-0.25f + i * 0.25f)
        val cy = y(if (i == 1) -0.76f else -0.68f)
        val cut = thLens(o(cx - 0.063f, cy + 0.104f), o(cx + 0.063f, cy - 0.104f), w * 0.028f)
        drawPath(cut, crumb)
        drawPath(cut, crust.darken(0.4f), style = pen.thin)
    }
    var i = 0
    while (i < FLOUR.size) {
        drawCircle(Color.White.copy(alpha = 0.7f), w * 0.011f, o(x(FLOUR[i]), y(FLOUR[i + 1])))
        i += 2
    }
}

private val FLOUR = floatArrayOf(-0.32f, -0.8f, -0.12f, -0.9f, 0.12f, -0.88f, 0.33f, -0.78f, 0.06f, -0.6f, -0.36f, -0.55f, 0.24f, -0.56f)

private val SPIRAL: FloatArray by lazy {
    val n = 28
    FloatArray(n * 2) { k ->
        val i = k / 2
        val th = i / (n - 1f) * 5.2 * Math.PI
        val f = 0.1f + 0.9f * i / (n - 1f)
        if (k % 2 == 0) (f * cos(th)).toFloat() else (f * sin(th)).toFloat()
    }
}

/** An ordinary cinnamon bun: a golden swirl with a drizzle of icing. */
internal fun DrawScope.thBun(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val dough = Color(0xFFE9A94E)
    val bun = thSketch(w, h) {
        m(-0.44f, -0.12f)
        c(-0.52f, -0.62f, -0.3f, -0.97f, 0f, -0.97f)
        c(0.3f, -0.97f, 0.52f, -0.62f, 0.44f, -0.12f)
        q(0.42f, -0.03f, 0.34f, -0.03f)
        l(-0.34f, -0.03f)
        q(-0.42f, -0.03f, -0.44f, -0.12f)
        z()
    }
    inked(bun, dough, pen)
    // The rolled layers show on the side as soft lines.
    val layer = dough.darken(0.28f)
    drawArc(layer, 20f, 140f, false, o(-0.4f, -0.44f), Size(0.8f * w, 0.36f * h), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
    val cx = 0f
    val cy = -0.6f * h
    val rx = 0.36f * w
    val ry = 0.3f * h
    val swirl = Path()
    val n = SPIRAL.size / 2
    fun sx(i: Int) = cx + SPIRAL[i * 2] * rx
    fun sy(i: Int) = cy + SPIRAL[i * 2 + 1] * ry
    swirl.moveTo(sx(0), sy(0))
    for (i in 1 until n - 1) swirl.quadraticTo(sx(i), sy(i), (sx(i) + sx(i + 1)) / 2, (sy(i) + sy(i + 1)) / 2)
    swirl.lineTo(sx(n - 1), sy(n - 1))
    drawPath(swirl, Color(0xFF7A3A18), style = Stroke(pen.lw * 1.5f, cap = StrokeCap.Round))
    drawPath(swirl, Color(0xFFB8682C), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    val icing = thSketch(w, h) {
        m(-0.33f, -0.58f); q(-0.27f, -0.88f, -0.17f, -0.66f); q(-0.08f, -0.46f, 0.01f, -0.7f)
        q(0.09f, -0.9f, 0.17f, -0.64f); q(0.23f, -0.48f, 0.32f, -0.7f)
    }
    translate(pen.lw * 0.25f, pen.lw * 0.45f) {
        drawPath(icing, Ink.line.copy(alpha = 0.22f), style = Stroke(pen.lw * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    drawPath(icing, Color(0xFFFFFBF2), style = Stroke(pen.lw * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    shine(o(-0.26f, -0.8f), w * 0.12f, h * 0.1f, 0.5f)
}

private val PENTA_COS = FloatArray(5) { cos(Math.toRadians(it * 72.0)).toFloat() }
private val PENTA_SIN = FloatArray(5) { sin(Math.toRadians(it * 72.0)).toFloat() }

// One heart of the waffle: the point at the origin, the lobes up, in units of the waffle radius.
private val WAFFLE_HEART = floatArrayOf(
    0f, 0f,
    -0.22f, -0.16f, -0.52f, -0.46f, -0.47f, -0.76f,
    -0.42f, -1.0f, -0.1f, -1.04f, 0f, -0.82f,
    0.1f, -1.04f, 0.42f, -1.0f, 0.47f, -0.76f,
    0.52f, -0.46f, 0.22f, -0.16f, 0f, 0f,
)

/** A heart waffle: five hearts round the middle, each with its grid. */
internal fun DrawScope.thWaffle(w: Float, h: Float, pen: Pen) {
    val c = Offset(0f, -h * 0.5f)
    val r = h * 0.49f
    val gold = Color(0xFFF2B54E)
    val all = Path()
    for (i in 0 until 5) {
        val cs = PENTA_COS[i]
        val sn = PENTA_SIN[i]
        fun px(k: Int) = c.x + r * (WAFFLE_HEART[k] * cs - WAFFLE_HEART[k + 1] * sn)
        fun py(k: Int) = c.y + r * (WAFFLE_HEART[k] * sn + WAFFLE_HEART[k + 1] * cs)
        val heart = Path()
        heart.moveTo(px(0), py(0))
        var k = 2
        while (k < WAFFLE_HEART.size) {
            heart.cubicTo(px(k), py(k), px(k + 2), py(k + 2), px(k + 4), py(k + 4))
            k += 6
        }
        heart.close()
        inked(heart, gold, pen)
        all.addPath(heart)
    }
    val grid = gold.darken(0.3f)
    val s = r * 0.2f
    clipPath(all) {
        for (k in -6..6) {
            drawLine(grid, Offset(c.x + k * s - r, c.y - r), Offset(c.x + k * s + r, c.y + r), pen.lw * 0.5f)
            drawLine(grid, Offset(c.x + k * s + r, c.y - r), Offset(c.x + k * s - r, c.y + r), pen.lw * 0.5f)
        }
    }
    drawCircle(gold.darken(0.2f), r * 0.07f, c)
    for (i in 0 until 5) drawCircle(Color.White.copy(alpha = 0.75f), r * 0.035f, Offset(c.x + r * 0.55f * PENTA_SIN[i], c.y - r * 0.55f * PENTA_COS[i]))
}

internal fun DrawScope.thCake(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    fun rr(l: Float, t: Float, r: Float, b: Float) = Rect(l * w, t * h, r * w, b * h)
    inkedOval(rr(-0.5f, -0.14f, 0.5f, 0f), Color(0xFFCFE3F5), pen)
    val cream = Color(0xFFFFF6EA)
    val body = thSketch(w, h) { m(-0.44f, -0.62f); l(-0.44f, -0.2f); q(0f, 0.02f, 0.44f, -0.2f); l(0.44f, -0.62f); z() }
    inked(body, cream, pen, outline = false)
    clipPath(body) {
        drawPath(thSketch(w, h) { m(-0.5f, -0.4f); q(0f, -0.2f, 0.5f, -0.4f) }, Color(0xFFFF9EC7), style = Stroke(h * 0.09f))
    }
    drawPath(body, Ink.line, style = pen.stroke)
    // Piped cream: a string of dollops along the base and round the top, inked as one.
    val base = Path()
    for (i in 0 until 8) {
        val x = -0.385f + i * 0.11f
        val t = (x + 0.44f) / 0.88f
        val y = -0.2f + 0.44f * t * (1f - t) - 0.04f
        base.addOval(rect(x * w, y * h, w * 0.075f, w * 0.06f))
    }
    thUnion(base, Color.White, pen)
    inkedOval(rr(-0.44f, -0.74f, 0.44f, -0.5f), Color(0xFFFFFBF4), pen)
    val crown = Path()
    for (i in 0 until 7) {
        val x = -0.36f + i * 0.12f
        val y = -0.62f + 0.12f * kotlin.math.sqrt((1f - (x / 0.44f) * (x / 0.44f)).coerceAtLeast(0f)) - 0.025f
        crown.addOval(rect(x * w, y * h, w * 0.1f, w * 0.085f))
    }
    thUnion(crown, Color.White, pen)
    for (i in 0 until 7) {
        val x = -0.36f + i * 0.12f
        val y = -0.62f + 0.12f * kotlin.math.sqrt((1f - (x / 0.44f) * (x / 0.44f)).coerceAtLeast(0f)) - 0.025f
        drawArc(Color(0xFFD9DDE8), 200f, 140f, false, o(x - 0.03f, y - 0.03f), Size(w * 0.06f, w * 0.045f), style = Stroke(pen.lw * 0.45f, cap = StrokeCap.Round))
    }
    val berry = Color(0xFFF23A4E)
    for (i in 0 until 3) {
        val x = CAKE_BERRIES[i * 2] * w
        val y = CAKE_BERRIES[i * 2 + 1] * h
        val s = w * 0.13f
        inked(thHeart(x, y, s), berry, pen)
        drawCircle(Color(0xFFFFE27A), s * 0.05f, Offset(x - s * 0.15f, y))
        drawCircle(Color(0xFFFFE27A), s * 0.05f, Offset(x + s * 0.12f, y + s * 0.12f))
        drawPath(thLens(Offset(x - s * 0.3f, y - s * 0.3f), Offset(x + s * 0.3f, y - s * 0.3f), s * 0.1f), Color(0xFF3FAE49))
        shine(Offset(x - s * 0.22f, y - s * 0.1f), s * 0.16f, s * 0.12f)
    }
}

private val CAKE_BERRIES = floatArrayOf(-0.2f, -0.68f, 0.03f, -0.74f, 0.23f, -0.66f)

private val CUP_FROST = longArrayOf(0xFFFFF3D6, 0xFFFFE27A, 0xFFFF8FB8, 0xFFB99BFF)
private val CUP_PAPER = longArrayOf(0xFFFF6FA8, 0xFF4AB3FF, 0xFF3BC46B, 0xFFFFC83D)
private val SPRINKLE = longArrayOf(0xFFFF4D6D, 0xFF4AB3FF, 0xFF3DDC97, 0xFFFFC83D, 0xFF8B5CF6, 0xFFFF9F43)
private val SPRINKLE_AT = floatArrayOf(
    -0.3f, -0.47f, 20f, -0.08f, -0.42f, -35f, 0.2f, -0.46f, 60f, 0.36f, -0.5f, -15f,
    -0.2f, -0.64f, 45f, 0.14f, -0.66f, -50f,
)

internal fun DrawScope.thCupcake(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    fun rr(l: Float, t: Float, r: Float, b: Float) = Rect(l * w, t * h, r * w, b * h)
    val i = v.mod(4)
    val frost = Color(CUP_FROST[i])
    val paper = Color(CUP_PAPER[i])
    val cup = thSketch(w, h) {
        m(-0.38f, -0.42f); l(0.38f, -0.42f); l(0.28f, -0.06f); q(0.27f, -0.02f, 0.22f, -0.02f)
        l(-0.22f, -0.02f); q(-0.27f, -0.02f, -0.28f, -0.06f); z()
    }
    inked(cup, paper, pen)
    for (k in -2..2) drawLine(paper.darken(0.32f), o(k * 0.15f, -0.4f), o(k * 0.105f, -0.05f), pen.lw * 0.5f, StrokeCap.Round)
    inkedOval(rr(-0.47f, -0.62f, 0.47f, -0.36f), frost, pen)
    inkedOval(rr(-0.36f, -0.78f, 0.36f, -0.54f), frost, pen)
    inkedOval(rr(-0.22f, -0.88f, 0.22f, -0.7f), frost, pen)
    shine(o(-0.16f, -0.7f), w * 0.12f, h * 0.05f, 0.7f)
    for (k in 0 until 6) {
        val p = o(SPRINKLE_AT[k * 3], SPRINKLE_AT[k * 3 + 1])
        val a = Math.toRadians(SPRINKLE_AT[k * 3 + 2].toDouble())
        val dx = (cos(a) * w * 0.045).toFloat()
        val dy = (sin(a) * w * 0.045).toFloat()
        drawLine(Color(SPRINKLE[k]), Offset(p.x - dx, p.y - dy), Offset(p.x + dx, p.y + dy), pen.lw * 0.9f, StrokeCap.Round)
    }
    val stem = thSketch(w, h) { m(0.03f, -0.9f); q(0.06f, -0.98f, 0.16f, -0.99f) }
    drawPath(stem, ThingInk.bark, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    inkedCircle(o(0.02f, -0.87f), w * 0.1f, Color(0xFFE8263E), pen)
    shine(o(-0.01f, -0.9f), w * 0.05f, h * 0.035f)
}

internal fun DrawScope.thPizza(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val cheese = Color(0xFFFFD35C)
    val slice = thSketch(w, h) { m(-0.36f, -0.93f); l(0.48f, -0.5f); l(-0.36f, -0.07f); q(-0.5f, -0.5f, -0.36f, -0.93f); z() }
    inked(slice, cheese, pen)
    drawPath(thSketch(w, h) { m(0.47f, -0.5f); l(-0.3f, -0.1f) }, Color(0xFFF08A3A), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    drawCircle(cheese.lighten(0.45f), w * 0.02f, o(0.2f, -0.45f))
    drawCircle(cheese.lighten(0.45f), w * 0.014f, o(0.02f, -0.5f))
    drawCircle(cheese.lighten(0.45f), w * 0.016f, o(-0.2f, -0.5f))
    val pep = Color(0xFFD2443A)
    for (k in 0 until 3) {
        val p = o(PEPPERONI[k * 2], PEPPERONI[k * 2 + 1])
        val r = w * 0.075f
        drawCircle(pep, r, p)
        drawCircle(Ink.line, r, p, style = pen.thin)
        drawCircle(pep.darken(0.3f), r * 0.18f, Offset(p.x - r * 0.3f, p.y + r * 0.2f))
        drawCircle(pep.darken(0.3f), r * 0.14f, Offset(p.x + r * 0.35f, p.y - r * 0.25f))
        drawCircle(Color.White.copy(alpha = 0.5f), r * 0.16f, Offset(p.x - r * 0.35f, p.y - r * 0.4f))
    }
    thFill(thLens(o(0.04f, -0.7f), o(0.2f, -0.76f), w * 0.03f), Color(0xFF3FAE49), pen)
    val crust = thSketch(w, h) { m(-0.36f, -0.88f); q(-0.52f, -0.5f, -0.36f, -0.12f) }
    val cw = w * 0.13f
    drawPath(crust, Ink.line, style = Stroke(cw + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(crust, Color(0xFFE3A15A), style = Stroke(cw, cap = StrokeCap.Round))
    translate(-cw * 0.14f, -cw * 0.12f) {
        drawPath(crust, Color(0xFFF2C488), style = Stroke(cw * 0.4f, cap = StrokeCap.Round))
    }
}

private val PEPPERONI = floatArrayOf(-0.12f, -0.66f, -0.1f, -0.32f, 0.18f, -0.52f)

internal fun DrawScope.thPancake(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val cake = Color(0xFFF4C878)
    // A stack seen a little from above: the golden sides with three layers, and the round top.
    val side = Path().apply {
        moveTo(-0.47f * w, -0.64f * h)
        lineTo(-0.47f * w, -0.3f * h)
        arcTo(Rect(-0.47f * w, -0.58f * h, 0.47f * w, -0.02f * h), 180f, -180f, false)
        lineTo(0.47f * w, -0.64f * h)
        close()
    }
    inked(side, cake.darken(0.1f), pen, outline = false)
    val edge = cake.darken(0.38f)
    clipPath(side) {
        for (k in 1..2) {
            val y = -0.3f - k * 0.12f
            drawArc(edge, 0f, 180f, false, Offset(-0.47f * w, (y - 0.28f) * h), Size(0.94f * w, 0.56f * h), style = Stroke(pen.lw * 0.6f))
        }
    }
    drawPath(side, Ink.line, style = pen.stroke)
    val top = Rect(-0.47f * w, -0.94f * h, 0.47f * w, -0.36f * h)
    inkedOval(top, cake, pen)
    drawOval(cake.darken(0.12f), Offset(top.left + w * 0.06f, top.top + h * 0.06f), Size(top.width - w * 0.12f, top.height - h * 0.12f), style = Stroke(pen.lw * 0.5f))
    drawCircle(cake.darken(0.2f), w * 0.012f, o(-0.3f, -0.6f))
    drawCircle(cake.darken(0.2f), w * 0.01f, o(0.32f, -0.54f))
    // Syrup runs over the edge; a pat of butter melts on top.
    val syrup = Color(0xFFD9892B)
    val pool = thSketch(w, h) {
        m(-0.26f, -0.66f); q(-0.24f, -0.86f, 0.02f, -0.85f); q(0.3f, -0.85f, 0.28f, -0.64f)
        q(0.3f, -0.5f, 0.24f, -0.4f); q(0.19f, -0.36f, 0.18f, -0.5f); q(-0.18f, -0.46f, -0.26f, -0.66f); z()
    }
    drawPath(pool, syrup)
    drawPath(pool, syrup.darken(0.35f), style = pen.thin)
    drawOval(Color.White.copy(alpha = 0.55f), o(-0.16f, -0.8f), Size(0.12f * w, 0.06f * h))
    val pat = thSketch(w, h) { m(-0.11f, -0.66f); l(0.03f, -0.8f); l(0.15f, -0.72f); l(0.01f, -0.58f); z() }
    val patSide = thSketch(w, h) { m(-0.11f, -0.66f); l(0.01f, -0.58f); l(0.15f, -0.72f); l(0.15f, -0.64f); l(0.01f, -0.5f); l(-0.11f, -0.58f); z() }
    thFill(patSide, Color(0xFFE8CC6A), pen)
    thFill(pat, Color(0xFFFFF0A8), pen)
    drawCircle(Color.White.copy(alpha = 0.8f), h * 0.03f, o(-0.02f, -0.7f))
}

private val DECA_COS = FloatArray(10) { cos(Math.toRadians(it * 36.0)).toFloat() }
private val DECA_SIN = FloatArray(10) { sin(Math.toRadians(it * 36.0)).toFloat() }
private val COOKIE_R = floatArrayOf(1f, 0.94f, 1f, 0.95f, 0.99f, 0.92f, 1f, 0.96f, 0.93f, 1f)
private val CHIPS = floatArrayOf(-0.2f, -0.62f, 0.14f, -0.72f, 0.24f, -0.42f, -0.06f, -0.4f, -0.26f, -0.3f, 0.06f, -0.2f)

internal fun DrawScope.thCookie(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val c = o(0f, -0.5f)
    val r = w * 0.5f
    val pts = FloatArray(20)
    for (i in 0 until 10) {
        pts[i * 2] = c.x + r * COOKIE_R[i] * DECA_COS[i]
        pts[i * 2 + 1] = c.y + r * COOKIE_R[i] * DECA_SIN[i]
    }
    val dough = Color(0xFFD9A05B)
    inked(blobPath(*pts), dough, pen)
    val crack = dough.darken(0.3f)
    drawPath(thSketch(w, h) { m(-0.1f, -0.84f); l(-0.02f, -0.74f); l(-0.08f, -0.66f) }, crack, style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
    drawPath(thSketch(w, h) { m(0.3f, -0.26f); l(0.38f, -0.3f) }, crack, style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
    val choc = Color(0xFF5A3520)
    for (k in 0 until 6) {
        val p = o(CHIPS[k * 2], CHIPS[k * 2 + 1])
        val cr = w * (0.06f + 0.012f * (k % 3))
        drawCircle(choc, cr, p)
        drawCircle(choc, cr * 0.7f, Offset(p.x + cr * 0.6f, p.y + cr * 0.3f))
        drawCircle(Color.White.copy(alpha = 0.45f), cr * 0.3f, Offset(p.x - cr * 0.35f, p.y - cr * 0.35f))
    }
}

private val ICE_FLAVOUR = longArrayOf(0xFFFFF1C9, 0xFFFF9EC7, 0xFF8A5534, 0xFFA6EBCF, 0xFFA99BFF)
private val ICE_BITS = longArrayOf(0xFF6B4A2B, 0xFFE8415A, 0xFF5A3520, 0xFF4A2E1C, 0xFF5B3FA8)
private val ICE_BIT_AT = floatArrayOf(-0.2f, -0.86f, 0.16f, -0.88f, 0.26f, -0.7f, -0.28f, -0.68f, 0.02f, -0.74f)

internal fun DrawScope.thIceCream(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val i = v.mod(5)
    val wafer = Color(0xFFE8B26A)
    val cone = thSketch(w, h) { m(-0.42f, -0.56f); l(0.42f, -0.56f); l(0.04f, -0.03f); q(0f, 0f, -0.04f, -0.03f); z() }
    inked(cone, wafer, pen, outline = false)
    val hatch = wafer.darken(0.3f)
    val s = w * 0.22f
    val dx = h * 0.26f
    clipPath(cone) {
        for (k in -4..4) {
            drawLine(hatch, Offset(k * s - dx, -h * 0.56f), Offset(k * s + dx, 0f), pen.lw * 0.5f)
            drawLine(hatch, Offset(k * s + dx, -h * 0.56f), Offset(k * s - dx, 0f), pen.lw * 0.5f)
        }
    }
    drawPath(cone, Ink.line, style = pen.stroke)
    val flavour = Color(ICE_FLAVOUR[i])
    val r = w * 0.48f
    val cy = -h * 0.74f
    val scoop = Path().apply {
        moveTo(-r, cy)
        arcTo(Rect(-r, cy - r, r, cy + r), 180f, 180f, false)
        lineTo(0.49f * w, -0.62f * h)
        quadraticTo(0.44f * w, -0.52f * h, 0.32f * w, -0.56f * h)
        quadraticTo(0.24f * w, -0.5f * h, 0.16f * w, -0.53f * h)
        quadraticTo(0.13f * w, -0.4f * h, 0.04f * w, -0.44f * h)
        quadraticTo(-0.01f * w, -0.52f * h, -0.08f * w, -0.54f * h)
        quadraticTo(-0.2f * w, -0.5f * h, -0.28f * w, -0.56f * h)
        quadraticTo(-0.42f * w, -0.52f * h, -0.49f * w, -0.62f * h)
        close()
    }
    inked(scoop, flavour, pen)
    val bit = Color(ICE_BITS[i])
    val bitR = if (i == 0) w * 0.022f else w * 0.042f
    for (k in 0 until 5) drawCircle(bit, bitR, o(ICE_BIT_AT[k * 2], ICE_BIT_AT[k * 2 + 1]))
    shine(o(-0.2f, -0.84f), w * 0.18f, h * 0.05f)
}

private val LOLLY_A = longArrayOf(0xFFFF4D6D, 0xFFFF4D6D, 0xFF3DDC97, 0xFF4AB3FF)
private val LOLLY_B = longArrayOf(0xFFFFFFFF, 0xFFFFE066, 0xFFFFE066, 0xFFC9B3FF)
internal val RAINBOW6 = longArrayOf(0xFFFF4D6D, 0xFFFF9F43, 0xFFFFE066, 0xFF3DDC97, 0xFF4AB3FF, 0xFF9B6BFF)

internal fun DrawScope.thLollipop(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val i = v.mod(4)
    capsule(o(0f, -0.03f), o(0f, -0.62f), w * 0.14f, ThingInk.paper, pen)
    val c = o(0f, -0.765f)
    val r = w * 0.47f
    val a = Color(LOLLY_A[i])
    inkedCircle(c, r, a, pen)
    val box = Rect(c.x - r, c.y - r, c.x + r, c.y + r)
    for (k in 0 until 6) {
        if (i != 1 && k % 2 == 0) continue
        val a0 = k * 60.0
        val a1 = a0 + 60.0
        val p0 = Math.toRadians(a0)
        val c0 = Math.toRadians(a0 + 48.0)
        val c1 = Math.toRadians(a1 + 48.0)
        val wedge = Path().apply {
            moveTo(c.x, c.y)
            quadraticTo(c.x + r * 0.62f * cos(c0).toFloat(), c.y + r * 0.62f * sin(c0).toFloat(), c.x + r * cos(p0).toFloat(), c.y + r * sin(p0).toFloat())
            arcTo(box, a0.toFloat(), 60f, false)
            quadraticTo(c.x + r * 0.62f * cos(c1).toFloat(), c.y + r * 0.62f * sin(c1).toFloat(), c.x, c.y)
            close()
        }
        drawPath(wedge, if (i == 1) Color(RAINBOW6[k]) else Color(LOLLY_B[i]))
    }
    thCrescent(c, r, 0.2f)
    drawCircle(Ink.line, r, c, style = pen.stroke)
    shine(Offset(c.x - r * 0.4f, c.y - r * 0.45f), r * 0.34f, r * 0.22f)
    val ribbon = if (i == 0) ThingInk.sky else ThingInk.pink
    for (s in SIDES) {
        val bow = thSketch(w, h) { m(0f, -0.57f); l(s * 0.36f, -0.65f); l(s * 0.36f, -0.49f); z() }
        thFill(bow, ribbon, pen)
    }
    thDot(o(0f, -0.57f), w * 0.07f, ribbon, pen)
}

/** A crisp shadow crescent on the lower right of a disc, drawn over patterns that hide the shading. */
internal fun DrawScope.thCrescent(c: Offset, r: Float, alpha: Float) {
    clipPath(Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }) {
        drawCircle(Ink.line.copy(alpha = alpha), r * 1.12f, Offset(c.x - r * 0.12f, c.y - r * 0.14f), style = Stroke(r * 0.26f))
    }
}

// ------------------------------------------------------------------ dairy, eggs, fish and meat

/** Brown cheese: a caramel block with the cheese slicer lying on top. */
internal fun DrawScope.thBrownCheese(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val front = Color(0xFFC77A3A)
    val topCol = Color(0xFFDFA062)
    // An oblique block: front face, lit top, shaded right side.
    val lx = -0.3f
    val rx = 0.2f
    val ty = -0.56f
    val dx = 0.26f
    val dy = Oblique.DY / Oblique.DX * dx * w / h
    drawPath(thSketch(w, h) { m(lx, ty); l(rx, ty); l(rx + dx, ty + dy); l(lx + dx, ty + dy); z() }, topCol)
    drawPath(thSketch(w, h) { m(rx, ty); l(rx + dx, ty + dy); l(rx + dx, -0.02f + dy); l(rx, -0.02f); z() }, Color(0xFFA9612C))
    val face = thSketch(w, h) { m(lx, ty); l(rx, ty); l(rx, -0.02f); l(lx, -0.02f); z() }
    drawPath(face, front)
    drawRect(front.darken(0.12f), o(lx, -0.15f), Size((rx - lx) * w, 0.13f * h))
    drawLine(Color.White.copy(alpha = 0.3f), o(lx + 0.05f, -0.46f), o(rx - 0.05f, -0.46f), pen.lw * 0.8f, StrokeCap.Round)
    for (k in 1..2) {
        val t = k / 3f
        drawLine(topCol.darken(0.16f), o(lx + dx * t + 0.02f, ty + dy * t), o(rx + dx * t - 0.02f, ty + dy * t), pen.lw * 0.45f, StrokeCap.Round)
    }
    val outline = thSketch(w, h) { m(lx, -0.02f); l(lx, ty); l(lx + dx, ty + dy); l(rx + dx, ty + dy); l(rx + dx, -0.02f + dy); l(rx, -0.02f); z() }
    drawPath(outline, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
    drawPath(thSketch(w, h) { m(lx, ty); l(rx, ty); l(rx + dx, ty + dy); m(rx, ty); l(rx, -0.02f) }, Ink.line, style = pen.thin)
    // The cheese slicer lies flat on top, its wooden handle reaching out to the left.
    val my = ty + dy * 0.47f
    capsule(o(-0.04f, my), o(-0.22f, my), h * 0.035f, ThingInk.steel, pen)
    capsule(o(-0.22f, my), o(-0.45f, my), h * 0.11f, ThingInk.wood, pen)
    drawLine(ThingInk.wood.lighten(0.35f), o(-0.4f, my - 0.03f), o(-0.27f, my - 0.03f), pen.lw * 0.6f, StrokeCap.Round)
    val f = 0.12f
    val b = 0.88f
    val blade = thSketch(w, h) {
        m(-0.14f + dx * f, ty + dy * f); l(0.12f + dx * f, ty + dy * f); l(0.12f + dx * b, ty + dy * b); l(-0.14f + dx * b, ty + dy * b); z()
    }
    drawPath(blade, Ink.line, style = Stroke(pen.lw * 1.6f, join = StrokeJoin.Round))
    drawPath(blade, Color(0xFFE9ECF3))
    val sy = ty + dy * 0.5f
    drawLine(Ink.line, o(-0.04f + dx * 0.5f, sy), o(0.1f + dx * 0.5f, sy), pen.lw * 0.9f, StrokeCap.Round)
    val curl = thSketch(w, h) { m(-0.01f + dx * 0.5f, sy); q(0.02f + dx * 0.5f, sy - 0.2f, 0.16f + dx * 0.5f, sy - 0.16f); q(0.22f + dx * 0.5f, sy - 0.12f, 0.14f + dx * 0.5f, sy - 0.06f) }
    drawPath(curl, Ink.line, style = Stroke(h * 0.07f + pen.lw * 1.4f, cap = StrokeCap.Round))
    drawPath(curl, Color(0xFFE8AE6E), style = Stroke(h * 0.07f, cap = StrokeCap.Round))
    drawLine(Color.White.copy(alpha = 0.85f), o(-0.08f + dx * f, ty + dy * f - 0.02f), o(0.1f + dx * f, ty + dy * f - 0.02f), pen.lw * 0.6f, StrokeCap.Round)
}

internal fun eggPath(w: Float, h: Float): Path = thSketch(w, h) {
    m(0f, -0.98f)
    c(0.3f, -0.98f, 0.5f, -0.58f, 0.48f, -0.36f)
    c(0.46f, -0.1f, 0.24f, -0.02f, 0f, -0.02f)
    c(-0.24f, -0.02f, -0.46f, -0.1f, -0.48f, -0.36f)
    c(-0.5f, -0.58f, -0.3f, -0.98f, 0f, -0.98f)
    z()
}

/** Variant 0 is an ordinary egg, 1 the golden egg. */
internal fun DrawScope.thEgg(v: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val egg = eggPath(w, h)
    if (v.mod(2) == 1) {
        thGlow(o(0f, -0.48f), w * 0.58f, Color(0xFFFFE27A), 0.35f + 0.12f * sin(pen.t * 2f))
        val gold = Color(0xFFFFC21F)
        inked(egg, gold, pen)
        clipPath(egg) { thSheen(o(0f, -0.5f), w * 0.55f, 0.45f) }
        drawArc(Color.White.copy(alpha = 0.55f), 200f, 60f, false, o(-0.36f, -0.9f), Size(w * 0.6f, h * 0.7f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
        shine(o(-0.2f, -0.7f), w * 0.16f, h * 0.16f, 0.9f)
        drawCircle(Color(0xFFFFF3B0), w * 0.035f, o(0.2f, -0.3f))
        thGlint(o(0.24f, -0.78f), w * 0.2f, pen.t, 2.4f, 0f)
        thGlint(o(-0.3f, -0.26f), w * 0.15f, pen.t, 2.4f, 2.6f)
    } else {
        val shell = Color(0xFFFFF7EA)
        inked(egg, shell, pen)
        clipPath(egg) { thSheen(o(0f, -0.5f), w * 0.55f, 0.5f) }
        val speck = Color(0xFFD9B98C)
        drawCircle(speck, w * 0.02f, o(0.16f, -0.44f))
        drawCircle(speck, w * 0.016f, o(0.26f, -0.62f))
        drawCircle(speck, w * 0.014f, o(0.02f, -0.24f))
        shine(o(-0.2f, -0.7f), w * 0.16f, h * 0.16f)
    }
}

internal fun DrawScope.thFriedEgg(w: Float, h: Float, pen: Pen) {
    val white = blobPath(
        -0.48f * w, -0.45f * h, -0.36f * w, -0.95f * h, -0.02f * w, -1.02f * h, 0.32f * w, -0.9f * h,
        0.5f * w, -0.5f * h, 0.36f * w, -0.03f * h, 0f, -0.01f * h, -0.36f * w, -0.03f * h,
    )
    inked(white, Color(0xFFFFFDF7), pen, outline = false)
    clipPath(white) { drawPath(white, Color(0xFFE9B97A), style = Stroke(pen.lw * 2.4f)) }
    drawPath(white, Ink.line, style = pen.stroke)
    inkedOval(rect(-0.06f * w, -0.5f * h, 0.34f * w, 0.84f * h), Color(0xFFFFB52E), pen)
    shine(Offset(-0.13f * w, -0.66f * h), 0.08f * w, 0.2f * h)
    val pepper = Color(0xFF5A4A40)
    drawCircle(pepper, w * 0.007f, Offset(0.26f * w, -0.62f * h))
    drawCircle(pepper, w * 0.006f, Offset(0.3f * w, -0.4f * h))
    drawCircle(pepper, w * 0.006f, Offset(-0.32f * w, -0.36f * h))
}

private fun fishBody(w: Float, h: Float): Path = thSketch(w, h) {
    m(-0.48f, -0.52f)
    c(-0.44f, -0.86f, -0.2f, -0.96f, 0.06f, -0.86f)
    c(0.2f, -0.8f, 0.28f, -0.66f, 0.32f, -0.58f)
    l(0.45f, -0.9f)
    q(0.5f, -0.92f, 0.49f, -0.84f)
    q(0.44f, -0.5f, 0.49f, -0.16f)
    q(0.5f, -0.08f, 0.45f, -0.1f)
    l(0.32f, -0.42f)
    c(0.24f, -0.2f, 0.02f, -0.06f, -0.18f, -0.12f)
    c(-0.34f, -0.18f, -0.46f, -0.32f, -0.48f, -0.52f)
    z()
}

private val FISH_SPOTS = floatArrayOf(-0.1f, -0.8f, 0.04f, -0.74f, 0.16f, -0.66f, -0.02f, -0.66f, 0.24f, -0.6f, 0.1f, -0.82f, -0.18f, -0.72f)

/** A cod, fresh or grilled with stripes and a sprig of dill. */
internal fun DrawScope.thFish(grilled: Boolean, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val back = if (grilled) Color(0xFFD39A55) else Color(0xFF8FA15E)
    val belly = if (grilled) Color(0xFFF0CD96) else Color(0xFFF1EAD3)
    val fin = back.darken(0.16f)
    inked(thLens(o(-0.2f, -0.9f), o(-0.04f, -0.93f), h * 0.1f), fin, pen, shade = false)
    inked(thLens(o(-0.01f, -0.88f), o(0.13f, -0.84f), h * 0.1f), fin, pen, shade = false)
    inked(thLens(o(0.15f, -0.78f), o(0.27f, -0.68f), h * 0.09f), fin, pen, shade = false)
    inked(thLens(o(-0.02f, -0.1f), o(0.12f, -0.14f), h * 0.08f), fin, pen, shade = false)
    inked(thLens(o(0.14f, -0.18f), o(0.26f, -0.28f), h * 0.07f), fin, pen, shade = false)
    val body = fishBody(w, h)
    inked(body, back, pen, outline = false)
    clipPath(body) {
        drawOval(belly, o(-0.56f, -0.44f), Size(0.9f * w, 0.56f * h))
        if (grilled) {
            for (k in 0 until 4) {
                drawLine(Color(0xFF6B3A1E), o(-0.16f + k * 0.15f, -0.96f), o(-0.3f + k * 0.15f, -0.04f), pen.lw * 1.3f, StrokeCap.Round)
            }
        } else {
            for (k in 0 until 7) drawCircle(back.darken(0.3f), w * 0.012f, o(FISH_SPOTS[k * 2], FISH_SPOTS[k * 2 + 1]))
        }
        drawPath(thSketch(w, h) { m(-0.24f, -0.58f); q(0.06f, -0.66f, 0.34f, -0.5f) }, Color.White.copy(alpha = 0.65f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
    }
    drawPath(body, Ink.line, style = pen.stroke)
    val tailRay = fin.darken(0.2f)
    drawLine(tailRay, o(0.35f, -0.54f), o(0.46f, -0.8f), pen.lw * 0.45f, StrokeCap.Round)
    drawLine(tailRay, o(0.36f, -0.5f), o(0.45f, -0.5f), pen.lw * 0.45f, StrokeCap.Round)
    drawLine(tailRay, o(0.35f, -0.46f), o(0.46f, -0.2f), pen.lw * 0.45f, StrokeCap.Round)
    drawPath(thSketch(w, h) { m(-0.26f, -0.8f); q(-0.16f, -0.55f, -0.28f, -0.28f) }, back.darken(0.4f), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
    thFill(thLens(o(-0.18f, -0.44f), o(-0.02f, -0.36f), h * 0.07f), fin, pen)
    val eye = o(-0.35f, -0.63f)
    val er = h * 0.12f
    drawCircle(Color.White, er, eye)
    drawCircle(Ink.line, er, eye, style = pen.thin)
    if (grilled) {
        drawCircle(Color(0xFF9A8F88), er * 0.4f, eye)
    } else {
        drawCircle(Ink.line, er * 0.55f, Offset(eye.x - er * 0.1f, eye.y))
        drawCircle(Color.White, er * 0.2f, Offset(eye.x - er * 0.3f, eye.y - er * 0.25f))
    }
    drawLine(Ink.line, o(-0.48f, -0.52f), o(-0.41f, -0.47f), pen.lw * 0.6f, StrokeCap.Round)
    drawLine(Ink.line, o(-0.42f, -0.3f), o(-0.45f, -0.14f), pen.lw * 0.5f, StrokeCap.Round)
    drawCircle(Ink.line, pen.lw * 0.5f, o(-0.45f, -0.14f))
    if (grilled) {
        val dill = Color(0xFF3F9E47)
        drawLine(dill, o(-0.08f, -0.52f), o(0.16f, -0.68f), pen.lw * 0.7f, StrokeCap.Round)
        for (k in 0 until 4) {
            val bx = -0.04f + k * 0.055f
            val by = -0.55f - k * 0.037f
            drawLine(dill, o(bx, by), o(bx - 0.02f, by - 0.16f), pen.lw * 0.55f, StrokeCap.Round)
            drawLine(dill, o(bx, by), o(bx + 0.05f, by + 0.1f), pen.lw * 0.55f, StrokeCap.Round)
        }
    }
}

internal fun DrawScope.thSausage(grilled: Boolean, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val col = if (grilled) Color(0xFFB8573A) else Color(0xFFF08C7A)
    for (s in SIDES) thDot(o(s * 0.47f, -0.45f), h * 0.1f, col.darken(0.25f), pen)
    val spine = thSketch(w, h) { m(-0.36f, -0.44f); q(0f, -0.62f, 0.36f, -0.44f) }
    val th = h * 0.7f
    drawPath(spine, Ink.line, style = Stroke(th + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(spine, col.shadow(), style = Stroke(th, cap = StrokeCap.Round))
    translate(-w * 0.01f, -h * 0.08f) { drawPath(spine, col, style = Stroke(th * 0.72f, cap = StrokeCap.Round)) }
    if (grilled) {
        val mark = Color(0xFF5A2A14)
        for (k in 0 until 4) {
            val x = -0.24f + k * 0.16f
            val t = (x + 0.36f) / 0.72f
            val y = -0.44f - 0.36f * t * (1f - t)
            drawLine(mark, o(x - 0.03f, y - 0.24f), o(x + 0.03f, y + 0.24f), pen.lw * 1.2f, StrokeCap.Round)
        }
    }
    drawPath(thSketch(w, h) { m(-0.2f, -0.68f); q(0f, -0.76f, 0.18f, -0.68f) }, Color.White.copy(alpha = 0.7f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
}

internal fun DrawScope.thMarshmallow(toasted: Boolean, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    capsule(o(0f, -0.34f), o(0.26f, -0.42f), w * 0.07f, ThingInk.woodDark, pen)
    capsule(o(0f, -0.02f), o(0f, -0.7f), w * 0.13f, ThingInk.woodDark, pen)
    drawLine(ThingInk.woodDark.darken(0.3f), o(-0.02f, -0.18f), o(-0.02f, -0.28f), pen.lw * 0.4f, StrokeCap.Round)
    val body = Rect(-0.42f * w, -0.95f * h, 0.42f * w, -0.66f * h)
    val col = if (toasted) Color(0xFFE3A152) else Color(0xFFFFF5F8)
    inkedRound(body, w * 0.16f, col, pen)
    if (toasted) {
        clipPath(roundPath(body, w * 0.16f)) {
            drawOval(Color(0xFFB86B2E), o(0.02f, -0.84f), Size(0.36f * w, 0.1f * h))
            drawOval(Color(0xFFB86B2E), o(-0.4f, -0.76f), Size(0.3f * w, 0.08f * h))
            drawCircle(Color(0xFF5A3520), w * 0.04f, o(0.24f, -0.72f))
        }
        drawRoundRect(Ink.line, body.topLeft, body.size, androidx.compose.ui.geometry.CornerRadius(w * 0.16f), style = pen.stroke)
        val drip = thSketch(w, h) { m(0.1f, -0.67f); q(0.14f, -0.58f, 0.2f, -0.6f); q(0.24f, -0.64f, 0.26f, -0.67f); z() }
        thFill(drip, Color(0xFFFFF0E0), pen)
    }
    drawOval(col.lighten(0.35f), o(-0.3f, -0.93f), Size(0.6f * w, 0.07f * h))
    shine(o(-0.22f, -0.8f), w * 0.1f, h * 0.06f, 0.7f)
}

internal fun DrawScope.thDough(w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    val dough = Color(0xFFF6E3BD)
    val path = blobPath(
        -0.48f * w, -0.3f * h, -0.46f * w, -0.72f * h, -0.16f * w, -0.97f * h, 0.2f * w, -0.92f * h,
        0.47f * w, -0.66f * h, 0.49f * w, -0.24f * h, 0.2f * w, -0.02f * h, -0.24f * w, -0.02f * h,
    )
    inked(path, dough, pen)
    val fold = dough.darken(0.2f)
    drawArc(fold, 200f, 110f, false, o(-0.36f, -0.62f), Size(0.5f * w, 0.4f * h), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    drawArc(fold, 20f, 90f, false, o(0.02f, -0.5f), Size(0.36f * w, 0.36f * h), style = Stroke(pen.lw * 0.55f, cap = StrokeCap.Round))
    drawOval(fold.copy(alpha = 0.6f), o(0.08f, -0.62f), Size(0.14f * w, 0.1f * h))
    for (k in 0 until 5) drawCircle(Color.White.copy(alpha = 0.8f), w * 0.014f, o(DOUGH_FLOUR[k * 2], DOUGH_FLOUR[k * 2 + 1]))
    shine(o(-0.22f, -0.76f), w * 0.12f, h * 0.1f, 0.5f)
}

private val DOUGH_FLOUR = floatArrayOf(-0.2f, -0.84f, 0.02f, -0.88f, 0.3f, -0.7f, -0.34f, -0.5f, 0.22f, -0.34f)

// ------------------------------------------------------------------ space food

/** 0 a squeeze tube, 1 a drink pouch with a straw, 2 a pack of freeze-dried food. [used] empties it. */
internal fun DrawScope.thSpaceFood(v: Int, used: Int, w: Float, h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * w, y * h)
    fun rr(l: Float, t: Float, r: Float, b: Float) = Rect(l * w, t * h, r * w, b * h)
    val left = 1f - used.coerceIn(0, 3) / 3f
    val foil = Color(0xFFE6E9F0)
    val band = Color(0xFF3D6BFF)
    when (v.mod(3)) {
        0 -> {
            // The tube rolls up from the bottom as it is eaten.
            val top = -0.99f + (1f - left) * 0.42f
            inkedRound(rr(-0.16f, top, 0.16f, top + 0.13f), w * 0.05f, Color(0xFFFF5A4E), pen)
            for (k in 1..3) drawLine(Color(0xFFFF5A4E).darken(0.3f), o(-0.16f + k * 0.08f, top + 0.02f), o(-0.16f + k * 0.08f, top + 0.11f), pen.lw * 0.45f)
            val tube = thSketch(w, h) {
                m(-0.16f, top + 0.13f); l(0.16f, top + 0.13f); q(0.42f, top + 0.16f, 0.42f, top + 0.26f)
                l(0.46f, -0.16f); l(-0.46f, -0.16f); l(-0.42f, top + 0.26f); q(-0.42f, top + 0.16f, -0.16f, top + 0.13f); z()
            }
            inked(tube, foil, pen, outline = false)
            val mid = (top + 0.26f - 0.16f) / 2f
            clipPath(tube) {
                drawRect(band, o(-0.5f, mid - 0.1f), Size(w, 0.2f * h))
                drawCircle(Color(0xFFFF9F43), w * 0.13f, o(0f, mid))
                drawCircle(Color.White.copy(alpha = 0.7f), w * 0.04f, o(-0.04f, mid - 0.03f))
            }
            drawPath(tube, Ink.line, style = pen.stroke)
            if (left < 1f) {
                // The squeezed end, rolled up like a real tube.
                capsule(o(-0.38f, -0.1f), o(0.38f, -0.1f), h * 0.13f, foil, pen)
                drawLine(foil.darken(0.3f), o(-0.34f, -0.125f), o(0.34f, -0.125f), pen.lw * 0.45f, StrokeCap.Round)
                drawLine(foil.darken(0.3f), o(-0.34f, -0.075f), o(0.34f, -0.075f), pen.lw * 0.45f, StrokeCap.Round)
                drawArc(Ink.line, 90f, 270f, false, o(0.36f, -0.12f), Size(h * 0.05f, h * 0.05f), style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round))
            } else {
                inkedRound(rr(-0.48f, -0.17f, 0.48f, -0.02f), w * 0.04f, foil.darken(0.08f), pen, shade = false)
                for (k in -3..3) drawLine(foil.darken(0.35f), o(k * 0.12f, -0.15f), o(k * 0.12f, -0.04f), pen.lw * 0.45f)
            }
            shine(o(-0.26f, top + 0.4f), w * 0.1f, h * 0.12f, 0.7f)
        }
        1 -> {
            val straw = thSketch(w, h) { m(0.08f, -0.8f); l(0.12f, -0.9f); l(0.34f, -0.99f) }
            drawPath(straw, Ink.line, style = Stroke(w * 0.1f + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(straw, Color.White, style = Stroke(w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(straw, ThingInk.tomato, style = Stroke(w * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            val pouch = rr(-0.44f, -0.84f, 0.44f, -0.02f)
            inkedRound(pouch, w * 0.12f, foil, pen)
            drawRect(foil.darken(0.12f), o(-0.44f, -0.84f), Size(0.88f * w, 0.1f * h))
            for (k in -3..3) drawLine(foil.darken(0.35f), o(k * 0.11f, -0.83f), o(k * 0.11f, -0.76f), pen.lw * 0.4f)
            inkedRound(rr(-0.06f, -0.84f, 0.2f, -0.76f), w * 0.03f, ThingInk.steel, pen, shade = false)
            val window = rr(-0.28f, -0.66f, 0.28f, -0.14f)
            drawRoundRect(Color(0xFFBFD3E6), window.topLeft, window.size, androidx.compose.ui.geometry.CornerRadius(w * 0.08f))
            if (left > 0f) {
                val top = window.bottom - window.height * 0.95f * left
                clipPath(roundPath(window, w * 0.08f)) {
                    drawRect(Color(0xFFFF9A3D), Offset(window.left, top), Size(window.width, window.bottom - top))
                    drawRect(Color(0xFFFFC27A), Offset(window.left, top), Size(window.width, h * 0.03f))
                }
            }
            drawRoundRect(Ink.line, window.topLeft, window.size, androidx.compose.ui.geometry.CornerRadius(w * 0.08f), style = pen.thin)
            drawPath(starPath(o(0f, -0.3f), w * 0.1f, w * 0.045f), Color.White.copy(alpha = 0.9f))
            shine(o(-0.34f, -0.5f), w * 0.07f, h * 0.2f, 0.6f)
        }
        else -> {
            val bag = thSketch(w, h) {
                m(-0.42f, -0.86f)
                l(-0.3f, -0.9f); l(-0.18f, -0.86f); l(-0.06f, -0.9f); l(0.06f, -0.86f); l(0.18f, -0.9f); l(0.3f, -0.86f); l(0.42f, -0.9f)
                l(0.44f, -0.06f); q(0.44f, -0.02f, 0.4f, -0.02f); l(-0.4f, -0.02f); q(-0.44f, -0.02f, -0.44f, -0.06f); z()
            }
            drawPath(bag, Color(0x55E6F2FF))
            val bits = (6 * left + 0.99f).toInt().coerceIn(0, 6)
            clipPath(bag) {
                for (k in 0 until bits) {
                    val c = o(SPACE_BITS[k * 2], SPACE_BITS[k * 2 + 1])
                    val s = w * 0.13f
                    drawRoundRect(Color(SPACE_BIT_COL[k]), Offset(c.x - s / 2, c.y - s / 2), Size(s, s), androidx.compose.ui.geometry.CornerRadius(s * 0.25f))
                    drawRoundRect(Ink.line, Offset(c.x - s / 2, c.y - s / 2), Size(s, s), androidx.compose.ui.geometry.CornerRadius(s * 0.25f), style = pen.thin)
                }
                drawRect(band, o(-0.5f, -0.8f), Size(w, 0.18f * h))
                drawPath(starPath(o(0f, -0.71f), w * 0.08f, w * 0.036f), Color.White)
            }
            drawPath(bag, Ink.line, style = Stroke(pen.lw, join = StrokeJoin.Round))
            drawLine(Color.White.copy(alpha = 0.7f), o(-0.3f, -0.56f), o(-0.3f, -0.14f), w * 0.06f, StrokeCap.Round)
        }
    }
}

private val SPACE_BITS = floatArrayOf(-0.2f, -0.12f, 0.12f, -0.14f, 0.28f, -0.28f, -0.06f, -0.3f, -0.26f, -0.42f, 0.12f, -0.46f)
private val SPACE_BIT_COL = longArrayOf(0xFF3BC46B, 0xFFFF9F43, 0xFFFFD23F, 0xFFFF5A4E, 0xFF3BC46B, 0xFFFF9F43)
