package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * The building blocks of the three feasts: a decorated Christmas tree with presents, wreaths, strings of
 * lights and Advent stars; Easter eggs, tulips, bunting and a vase of feathered birch twigs; carved
 * pumpkins, lanterns, bats on strings, friendly cardboard ghosts and cobwebs. All of it kind and cute.
 *
 * Each piece is drawn in a small local space (one unit is the piece's height in pixels, origin at its
 * base or its hook), from static paths made once; only the lights move with `pen.t`, and they shine
 * brighter as the night comes. Where the pieces stand in each place is in SeasonArtPlaces.kt.
 */

/** Draws [block] with the origin at [c] and one unit as [s] pixels. */
private inline fun DrawScope.loc(c: Offset, s: Float, block: DrawScope.() -> Unit) =
    withTransform({ translate(c.x, c.y); scale(s, s, Offset.Zero) }, block)

/** The pen for the local space of a piece [s] pixels high: the outline stays as thin as everywhere else. */
private fun Pen.local(s: Float) = Pen(lw / s, t, night, weather, rainbow, season, festival)

/** How strongly lights shine: a little by day, fully at night. */
private fun Pen.lit() = 0.3f + 0.7f * night

private fun DrawScope.halo(c: Offset, r: Float, color: Color, alpha: Float) {
    if (alpha <= 0.01f) return
    drawCircle(safeRadialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)), center = c, radius = r), r, c)
}

private fun quad(a: Offset, c: Offset, b: Offset, s: Float): Offset {
    val k = 1f - s
    return Offset(k * k * a.x + 2f * k * s * c.x + s * s * b.x, k * k * a.y + 2f * k * s * c.y + s * s * b.y)
}

// ========================================================================================= CHRISTMAS

internal val xmasLights = arrayOf(Color(0xFFFF5A6E), Color(0xFFFFD23F), Color(0xFF5CE0A0), Color(0xFF5AA9E6), Color(0xFFFF9EC4))
private val baubleColors = arrayOf(Color(0xFFE94F4F), Color(0xFF4F8BE9), Color(0xFFFFC83D), Color(0xFFB57BFF))

private fun tier(top: Float, bot: Float, hw: Float) = Path().apply {
    moveTo(0f, top)
    lineTo(hw, bot)
    quadraticTo(hw * 0.67f, bot + 0.07f, hw * 0.34f, bot)
    quadraticTo(0f, bot + 0.07f, -hw * 0.34f, bot)
    quadraticTo(-hw * 0.67f, bot + 0.07f, -hw, bot)
    close()
}

/** The three tiers of the tree, the widest first (it is drawn first). */
private val treeTiers by lazy { arrayOf(tier(-0.58f, -0.04f, 0.46f), tier(-0.8f, -0.34f, 0.34f), tier(-1f, -0.62f, 0.22f)) }
private val treeGarland by lazy {
    Path().apply {
        moveTo(-0.17f, -0.62f); quadraticTo(0.02f, -0.55f, 0.17f, -0.66f)
        moveTo(-0.29f, -0.44f); quadraticTo(0.0f, -0.34f, 0.3f, -0.47f)
        moveTo(-0.4f, -0.2f); quadraticTo(0.0f, -0.08f, 0.41f, -0.22f)
    }
}
private val treeBaubles = floatArrayOf(
    -0.2f, -0.17f, 0.05f, 0f, 0.2f, -0.24f, 0.05f, 1f, 0.02f, -0.1f, 0.045f, 2f, -0.1f, -0.4f, 0.045f, 1f,
    0.16f, -0.46f, 0.045f, 0f, -0.04f, -0.66f, 0.04f, 2f, 0.1f, -0.74f, 0.035f, 3f, -0.3f, -0.3f, 0.04f, 3f,
)
private val treeLights by lazy {
    val pts = floatArrayOf(
        -0.38f, -0.1f, -0.3f, -0.2f, -0.12f, -0.2f, 0.1f, -0.15f, 0.3f, -0.18f, 0.38f, -0.1f,
        -0.24f, -0.34f, -0.1f, -0.5f, 0.06f, -0.4f, 0.22f, -0.38f, -0.14f, -0.62f, 0.12f, -0.6f,
        -0.04f, -0.8f, 0.07f, -0.9f, 0.0f, -0.72f,
    )
    Array(5) { c ->
        val l = ArrayList<Offset>(4)
        var i = c
        while (i < pts.size / 2) {
            l.add(Offset(pts[i * 2], pts[i * 2 + 1]))
            i += 5
        }
        l as List<Offset>
    }
}

/** A decorated Christmas tree [h] pixels tall standing at [base]: garlands, baubles, blinking lights and a star. */
internal fun DrawScope.xmasTree(base: Offset, h: Float, pen: Pen) {
    val lp = pen.local(h)
    val n = pen.night
    val t = pen.t
    loc(base, h) {
        halo(Offset(0f, -0.5f), 1.1f, Color(0xFFFFD27A), 0.3f * n)
        groundShadow(0f, 0f, 0.8f)
        inkedRound(Rect(-0.055f, -0.12f, 0.055f, 0f), 0.015f, Color(0xFF7A4B2E), lp, shade = false)
        val greens = arrayOf(Color(0xFF23764A), Color(0xFF2A8553), Color(0xFF30935B))
        for (k in 0 until 3) inked(treeTiers[k], greens[k], lp)
        drawPath(treeGarland, Color(0xFFFFD25A), style = Stroke(0.03f, cap = StrokeCap.Round))
        var i = 0
        while (i < treeBaubles.size) {
            val c = Offset(treeBaubles[i], treeBaubles[i + 1])
            val r = treeBaubles[i + 2]
            drawCircle(Ink.line, r + lp.lw, c)
            drawCircle(baubleColors[treeBaubles[i + 3].toInt()], r, c)
            drawCircle(Color.White, r * 0.28f, Offset(c.x - r * 0.35f, c.y - r * 0.35f), alpha = 0.85f)
            i += 4
        }
        val k = pen.lit()
        for (g in 0 until 5) {
            val blink = 0.55f + 0.45f * sin(t * 2.2f + g * 1.7f)
            drawPoints(treeLights[g], PointMode.Points, xmasLights[g], strokeWidth = 0.12f, cap = StrokeCap.Round, alpha = 0.2f * k * blink)
            drawPoints(treeLights[g], PointMode.Points, Ink.line, strokeWidth = 0.05f, cap = StrokeCap.Round)
            drawPoints(treeLights[g], PointMode.Points, lerp(xmasLights[g], Color.White, 0.3f * k), strokeWidth = 0.036f, cap = StrokeCap.Round, alpha = 0.6f + 0.4f * blink)
        }
        val pulse = 1f + 0.06f * sin(t * 2.6f)
        halo(Offset(0f, -1.04f), 0.28f * pulse, Color(0xFFFFE27A), 0.5f * k)
        inked(starPath(Offset(0f, -1.04f), 0.1f * pulse, 0.045f * pulse), Color(0xFFFFD23F), lp, shade = false)
    }
}

/** Three wrapped presents with ribbons and bows, [w] pixels wide, standing at [base]. */
internal fun DrawScope.presents(base: Offset, w: Float, pen: Pen) {
    val lp = pen.local(w)
    loc(base, w) {
        groundShadow(0f, 0f, 1.1f)
        val boxes = floatArrayOf(-0.42f, 0.34f, 0.3f, 0f, 0.0f, 0.26f, 0.22f, 1f, 0.3f, 0.3f, 0.2f, 2f)
        val fills = arrayOf(Color(0xFFE94F4F), Color(0xFF4F8BE9), Color(0xFF3FB57A))
        val ribbons = arrayOf(Color(0xFFFFD25A), Color(0xFFFFFFFF), Color(0xFFE94F4F))
        var i = 0
        while (i < boxes.size) {
            val cx = boxes[i]
            val bw = boxes[i + 1]
            val bh = boxes[i + 2]
            val k = boxes[i + 3].toInt()
            inkedRound(Rect(cx - bw / 2f, -bh, cx + bw / 2f, 0f), 0.015f, fills[k], lp)
            drawRect(ribbons[k], Offset(cx - 0.02f, -bh), Size(0.04f, bh))
            drawRect(ribbons[k], Offset(cx - bw / 2f, -bh * 0.55f), Size(bw, 0.035f))
            // The bow: two loops and a knot.
            drawOval(ribbons[k], Offset(cx - 0.085f, -bh - 0.06f), Size(0.085f, 0.07f))
            drawOval(ribbons[k], Offset(cx, -bh - 0.06f), Size(0.085f, 0.07f))
            drawOval(Ink.line, Offset(cx - 0.085f, -bh - 0.06f), Size(0.17f, 0.07f), style = lp.thin)
            drawCircle(ribbons[k].darken(0.15f), 0.022f, Offset(cx, -bh - 0.025f))
            i += 4
        }
    }
}

private val wreathLeaves by lazy {
    val l = ArrayList<Offset>(16)
    for (i in 0 until 16) {
        val a = i * (2f * PI.toFloat() / 16f)
        l.add(Offset(cos(a) * 0.82f, sin(a) * 0.82f))
    }
    l as List<Offset>
}
private val wreathBerries = listOf(Offset(0.58f, -0.58f), Offset(0.66f, -0.5f), Offset(-0.7f, -0.45f), Offset(-0.45f, -0.7f), Offset(0.1f, -0.84f))

/** A Christmas wreath of fir with berries and a red bow, [r] pixels across from the middle to the outer edge. */
internal fun DrawScope.wreath(c: Offset, r: Float, pen: Pen) {
    val lp = pen.local(r)
    loc(c, r) {
        val green = Color(0xFF2A8553)
        drawCircle(Ink.line, 0.82f, Offset.Zero, style = Stroke(0.44f + lp.lw * 2f))
        drawCircle(green, 0.82f, Offset.Zero, style = Stroke(0.44f))
        drawCircle(green.lighten(0.2f), 0.9f, Offset.Zero, style = Stroke(0.1f), alpha = 0.7f)
        drawPoints(wreathLeaves, PointMode.Points, Color(0xFF3FA866), strokeWidth = 0.15f, cap = StrokeCap.Round)
        drawPoints(wreathBerries, PointMode.Points, Ink.line, strokeWidth = 0.12f, cap = StrokeCap.Round)
        drawPoints(wreathBerries, PointMode.Points, Color(0xFFE94F4F), strokeWidth = 0.09f, cap = StrokeCap.Round)
        val bow = Path().apply {
            moveTo(0f, 0.86f)
            quadraticTo(-0.36f, 0.62f, -0.34f, 0.98f)
            quadraticTo(-0.14f, 0.96f, 0f, 0.86f)
            moveTo(0f, 0.86f)
            quadraticTo(0.36f, 0.62f, 0.34f, 0.98f)
            quadraticTo(0.14f, 0.96f, 0f, 0.86f)
        }
        inked(bow, Color(0xFFE94F4F), lp, shade = false)
        drawCircle(Color(0xFFC7343A), 0.07f, Offset(0f, 0.86f))
    }
}

private val adventStarPath by lazy {
    Path().apply {
        for (i in 0 until 16) {
            val rr = if (i % 2 == 0) 1f else 0.46f
            val a = (i * 22.5f - 90f) * PI.toFloat() / 180f
            val x = cos(a) * rr
            val y = sin(a) * rr
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

/** The Advent star in the window: a glowing paper star on a thread; [hang] is the length of the thread in pixels. */
internal fun DrawScope.adventStar(c: Offset, r: Float, pen: Pen, hang: Float = 0f) {
    val lp = pen.local(r)
    val k = pen.lit()
    if (hang > 0f) drawLine(Ink.line, Offset(c.x, c.y - hang), Offset(c.x, c.y - r), strokeWidth = pen.lw * 0.7f)
    halo(c, r * 2.6f, Color(0xFFFFD86B), 0.5f * k)
    loc(c, r) {
        inked(adventStarPath, Color(0xFFFFE27A), lp, shade = false)
        drawCircle(Color(0xFFFFF8D6), 0.3f, Offset.Zero, alpha = 0.6f + 0.4f * sin(pen.t * 2.4f))
    }
}

/**
 * A swag of lights from [a] to [b] hanging [sag] pixels in the middle: a thin wire and [count] bulbs in the
 * [palette]'s colours that blink gently in groups and shine brighter at night.
 */
internal fun DrawScope.lightStrand(a: Offset, b: Offset, sag: Float, count: Int, size: Float, pen: Pen, palette: Array<Color> = xmasLights, phase: Float = 0f) {
    val ctrl = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f + sag * 2f)
    val wire = Path().apply {
        moveTo(a.x, a.y)
        quadraticTo(ctrl.x, ctrl.y, b.x, b.y)
    }
    drawPath(wire, Ink.line, style = pen.thin)
    val groups = Array(palette.size) { ArrayList<Offset>(count / palette.size + 1) }
    for (j in 0 until count) {
        val p = quad(a, ctrl, b, (j + 0.5f) / count)
        groups[j % palette.size].add(Offset(p.x, p.y + size * 0.7f))
    }
    val k = pen.lit()
    for (g in groups.indices) {
        if (groups[g].isEmpty()) continue
        val blink = 0.6f + 0.4f * sin(pen.t * 2.3f + g * 1.7f + phase)
        drawPoints(groups[g], PointMode.Points, palette[g], strokeWidth = size * 4.2f, cap = StrokeCap.Round, alpha = 0.22f * k * blink)
        drawPoints(groups[g], PointMode.Points, Ink.line, strokeWidth = size * 1.5f, cap = StrokeCap.Round)
        drawPoints(groups[g], PointMode.Points, lerp(palette[g], Color.White, 0.3f * k), strokeWidth = size * 1.1f, cap = StrokeCap.Round, alpha = 0.7f + 0.3f * blink)
    }
}

// ============================================================================================ EASTER

private val eggColors = arrayOf(Color(0xFFFF9EC4), Color(0xFFFFE066), Color(0xFF7FE0C0), Color(0xFF8FC8FF), Color(0xFFC6A5FF), Color(0xFFFFA85C))
private val tulipColors = arrayOf(Color(0xFFFF5A7A), Color(0xFFFFC83D), Color(0xFFFF8FB8), Color(0xFFB57BFF), Color(0xFFFF7A3D))
internal val easterFlags = arrayOf(Color(0xFFFF9EC4), Color(0xFFFFE066), Color(0xFF7FE0C0), Color(0xFF8FC8FF), Color(0xFFC6A5FF))

private val eggShape by lazy {
    Path().apply {
        moveTo(0f, -1f)
        cubicTo(0.3f, -1f, 0.4f, -0.55f, 0.4f, -0.35f)
        cubicTo(0.4f, -0.12f, 0.24f, 0f, 0f, 0f)
        cubicTo(-0.24f, 0f, -0.4f, -0.12f, -0.4f, -0.35f)
        cubicTo(-0.4f, -0.55f, -0.3f, -1f, 0f, -1f)
        close()
    }
}

private val eggZig by lazy {
    Path().apply {
        moveTo(-0.45f, -0.5f)
        for (i in 0 until 6) lineTo(-0.45f + (i + 1) * 0.15f, if (i % 2 == 0) -0.4f else -0.5f)
    }
}

/** A painted Easter egg [h] pixels tall, standing at [base]; [style] picks its colour and its pattern. */
internal fun DrawScope.egg(base: Offset, h: Float, pen: Pen, style: Int) {
    val lp = pen.local(h)
    val col = eggColors[style.mod(eggColors.size)]
    loc(base, h) {
        drawOval(Ink.shadow, Offset(-0.34f, -0.05f), Size(0.78f, 0.1f))
        inked(eggShape, col, lp)
        clipPath(eggShape) {
            when (style.mod(4)) {
                0 -> for (k in 0 until 3) drawRect(Color.White, Offset(-0.5f, -0.78f + k * 0.2f), Size(1f, 0.07f), alpha = 0.85f)
                1 -> {
                    drawPoints(listOf(Offset(-0.12f, -0.7f), Offset(0.14f, -0.62f), Offset(-0.02f, -0.45f), Offset(0.18f, -0.3f), Offset(-0.18f, -0.28f)), PointMode.Points, Color.White, strokeWidth = 0.1f, cap = StrokeCap.Round, alpha = 0.9f)
                }
                2 -> drawPath(eggZig, Color.White, alpha = 0.9f, style = Stroke(0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                else -> drawRect(col.darken(0.2f), Offset(-0.5f, -0.58f), Size(1f, 0.14f))
            }
        }
        drawPath(eggShape, Ink.line, style = lp.stroke)
        shine(Offset(-0.14f, -0.74f), 0.09f, 0.17f)
    }
}

/** A tulip [h] pixels tall with two leaves; [sway] leans the head (in units of its height). */
internal fun DrawScope.tulip(base: Offset, h: Float, pen: Pen, style: Int, sway: Float = 0f) {
    val lp = pen.local(h)
    val col = tulipColors[style.mod(tulipColors.size)]
    loc(base, h) {
        val green = Color(0xFF3FA866)
        val leaf = Path().apply {
            moveTo(0f, -0.04f)
            quadraticTo(-0.3f, -0.28f, -0.24f, -0.62f)
            quadraticTo(-0.08f, -0.34f, 0f, -0.04f)
            moveTo(0f, -0.04f)
            quadraticTo(0.3f, -0.24f, 0.26f, -0.54f)
            quadraticTo(0.08f, -0.3f, 0f, -0.04f)
        }
        inked(leaf, green, lp, shade = false)
        drawLine(Ink.line, Offset(0f, 0f), Offset(sway, -0.8f), strokeWidth = 0.08f + lp.lw * 2f, cap = StrokeCap.Round)
        drawLine(green, Offset(0f, 0f), Offset(sway, -0.8f), strokeWidth = 0.05f, cap = StrokeCap.Round)
        val cup = Path().apply {
            moveTo(sway - 0.14f, -0.86f)
            lineTo(sway - 0.16f, -1.1f)
            lineTo(sway - 0.06f, -0.99f)
            lineTo(sway, -1.16f)
            lineTo(sway + 0.06f, -0.99f)
            lineTo(sway + 0.16f, -1.1f)
            lineTo(sway + 0.14f, -0.86f)
            quadraticTo(sway, -0.74f, sway - 0.14f, -0.86f)
            close()
        }
        inked(cup, col, lp)
        drawLine(col.lighten(0.3f), Offset(sway, -0.8f), Offset(sway, -1.05f), strokeWidth = 0.03f, cap = StrokeCap.Round, alpha = 0.7f)
    }
}

/** A string of pennants from [a] to [b], [sag] pixels low in the middle, in soft Easter colours. */
internal fun DrawScope.bunting(a: Offset, b: Offset, sag: Float, count: Int, flag: Float, pen: Pen, palette: Array<Color> = easterFlags) {
    val ctrl = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f + sag * 2f)
    val cord = Path().apply {
        moveTo(a.x, a.y)
        quadraticTo(ctrl.x, ctrl.y, b.x, b.y)
    }
    drawPath(cord, Ink.line, style = pen.thin)
    val flags = Array(palette.size) { Path() }
    val sway = sin(pen.t * 1.4f) * flag * 0.04f
    for (j in 0 until count) {
        val p = quad(a, ctrl, b, (j + 0.5f) / count)
        val q = quad(a, ctrl, b, (j + 0.5f) / count + 0.45f / count)
        val r = quad(a, ctrl, b, (j + 0.5f) / count - 0.45f / count)
        val f = flags[j % palette.size]
        f.moveTo(r.x, r.y)
        f.lineTo(q.x, q.y)
        f.lineTo(p.x + sway, p.y + flag)
        f.close()
    }
    for (g in palette.indices) {
        drawPath(flags[g], palette[g])
        drawPath(flags[g], Ink.line, style = pen.thin)
    }
}

/** A bunch of eggs on a string: each hangs from [a]..[b] on its own thread. */
internal fun DrawScope.eggGarland(a: Offset, b: Offset, sag: Float, count: Int, eggH: Float, pen: Pen) {
    val ctrl = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f + sag * 2f)
    val cord = Path().apply {
        moveTo(a.x, a.y)
        quadraticTo(ctrl.x, ctrl.y, b.x, b.y)
    }
    drawPath(cord, Ink.line, style = pen.thin)
    for (j in 0 until count) {
        val p = quad(a, ctrl, b, (j + 0.5f) / count)
        val len = eggH * (0.25f + 0.35f * hash01(j, 1301))
        drawLine(Ink.line, p, Offset(p.x, p.y + len), strokeWidth = pen.lw * 0.7f)
        egg(Offset(p.x, p.y + len + eggH), eggH, pen, j + 1)
    }
}

/** A vase of birch twigs with coloured feathers: the Easter twigs («påskeris»), [h] pixels tall. */
internal fun DrawScope.twigVase(base: Offset, h: Float, pen: Pen) {
    val lp = pen.local(h)
    loc(base, h) {
        val twigs = floatArrayOf(-0.34f, -0.98f, -0.14f, -1.1f, 0.06f, -1.14f, 0.26f, -1.06f, 0.42f, -0.92f, -0.22f, -0.8f, 0.3f, -0.78f)
        val cols = arrayOf(Color(0xFFFF7FA8), Color(0xFFFFD84A), Color(0xFF7FE0C0), Color(0xFF8FC8FF), Color(0xFFC59BFF))
        var k = 0
        for (i in twigs.indices step 2) {
            val tip = Offset(twigs[i], twigs[i + 1])
            drawLine(SeasonPal.twig, Offset(0f, -0.4f), tip, strokeWidth = 0.025f, cap = StrokeCap.Round)
            // A feather: a pointed leaf of colour with a darker quill, just below the tip.
            val fe = Offset(tip.x * 0.96f, tip.y + 0.03f)
            val feather = Path().apply {
                moveTo(fe.x, fe.y)
                quadraticTo(fe.x + 0.07f, fe.y + 0.04f, fe.x + 0.03f, fe.y + 0.17f)
                quadraticTo(fe.x - 0.05f, fe.y + 0.1f, fe.x, fe.y)
                close()
            }
            drawPath(feather, Ink.line, style = Stroke(lp.lw * 2f, join = StrokeJoin.Round))
            drawPath(feather, cols[k % cols.size])
            drawLine(cols[k % cols.size].darken(0.3f), fe, Offset(fe.x + 0.03f, fe.y + 0.16f), strokeWidth = 0.012f, cap = StrokeCap.Round)
            k++
        }
        val vase = Path().apply {
            moveTo(-0.16f, -0.44f)
            lineTo(0.16f, -0.44f)
            quadraticTo(0.3f, -0.2f, 0.2f, 0f)
            lineTo(-0.2f, 0f)
            quadraticTo(-0.3f, -0.2f, -0.16f, -0.44f)
            close()
        }
        inked(vase, Color(0xFF7FC8E8), lp)
        drawRect(Color.White, Offset(-0.24f, -0.26f), Size(0.48f, 0.05f), alpha = 0.55f)
        shine(Offset(-0.13f, -0.2f), 0.04f, 0.14f)
    }
}

// ============================================================================================ PUMPKIN

private val pumpkinBody by lazy {
    Path().apply {
        addOval(Rect(-0.58f, -0.93f, -0.02f, -0.05f))
        addOval(Rect(0.02f, -0.93f, 0.58f, -0.05f))
    }
}
private val pumpkinMiddle by lazy { Path().apply { addOval(Rect(-0.36f, -1f, 0.36f, -0.0f)) } }
private val pumpkinRibs by lazy {
    Path().apply {
        moveTo(-0.17f, -0.97f); quadraticTo(-0.3f, -0.5f, -0.17f, -0.03f)
        moveTo(0.17f, -0.97f); quadraticTo(0.3f, -0.5f, 0.17f, -0.03f)
    }
}
private val pumpkinEyes by lazy {
    Path().apply {
        moveTo(-0.27f, -0.6f); lineTo(-0.09f, -0.6f); lineTo(-0.18f, -0.76f); close()
        moveTo(0.09f, -0.6f); lineTo(0.27f, -0.6f); lineTo(0.18f, -0.76f); close()
    }
}
private val pumpkinMouth by lazy {
    Path().apply {
        moveTo(-0.32f, -0.44f)
        quadraticTo(0f, -0.14f, 0.32f, -0.44f)
        quadraticTo(0f, -0.3f, -0.32f, -0.44f)
        close()
    }
}

/**
 * A pumpkin [h] pixels tall standing at [base]. With a [face] it is a friendly jack-o'-lantern whose eyes and
 * smile glow ([glow] from 0 to 1; by night it glows on its own and flickers a little).
 */
internal fun DrawScope.pumpkin(base: Offset, h: Float, pen: Pen, face: Boolean, glow: Float, night: Float) {
    val lp = pen.local(h)
    val orange = Color(0xFFF4891C).atNight(night, 0.3f)
    val g = (glow + (0.35f + 0.65f * night) * if (face) 1f else 0f).coerceIn(0f, 1f) * (0.92f + 0.08f * sin(pen.t * 7f + base.x))
    loc(base, h) {
        drawOval(Ink.shadow, Offset(-0.55f, -0.07f), Size(1.14f, 0.14f))
        if (face && night > 0.05f) halo(Offset(0f, -0.5f), 1.1f, Color(0xFFFFB347), 0.38f * night)
        inked(pumpkinBody, orange.darken(0.08f), lp)
        inked(pumpkinMiddle, orange, lp)
        drawPath(pumpkinRibs, orange.darken(0.3f), style = Stroke(0.025f, cap = StrokeCap.Round), alpha = 0.7f)
        val stem = Path().apply { poly(-0.07f, -0.97f, 0.08f, -0.97f, 0.06f, -1.12f, -0.04f, -1.14f) }
        inked(stem, Color(0xFF5C7A2E), lp, shade = false)
        if (face) {
            val cut = lerp(Color(0xFF8A3E0C), Color(0xFFFFE27A), g)
            val rim = Color(0xFF3A1E10)
            drawPath(pumpkinEyes, rim, style = Stroke(0.05f, join = StrokeJoin.Round))
            drawPath(pumpkinMouth, rim, style = Stroke(0.05f, join = StrokeJoin.Round))
            drawPath(pumpkinEyes, cut)
            drawPath(pumpkinMouth, cut)
            drawPath(pumpkinEyes, cut, style = Stroke(0.02f, join = StrokeJoin.Round))
        }
    }
}

/** A paper lantern hanging from [top], [h] pixels long, swaying a little; it glows warmer at night. */
internal fun DrawScope.lantern(top: Offset, h: Float, pen: Pen, phase: Float, color: Color = Color(0xFFFFB347)) {
    val lp = pen.local(h)
    val sway = sin(pen.t * 1.3f + phase) * 4f
    val k = pen.lit()
    rotate(sway, top) {
        loc(top, h) {
            drawLine(Ink.line, Offset(0f, -0.3f), Offset(0f, 0.06f), strokeWidth = lp.lw)
            halo(Offset(0f, 0.42f), 0.8f, color, 0.4f * k)
            val body = Path().apply {
                moveTo(-0.18f, 0.12f)
                lineTo(0.18f, 0.12f)
                quadraticTo(0.34f, 0.4f, 0.18f, 0.7f)
                lineTo(-0.18f, 0.7f)
                quadraticTo(-0.34f, 0.4f, -0.18f, 0.12f)
                close()
            }
            inked(body, lerp(color.darken(0.1f), Color(0xFFFFE9A8), 0.3f * k), lp)
            drawPath(Path().apply { moveTo(0f, 0.12f); quadraticTo(0.14f, 0.4f, 0f, 0.7f); moveTo(0f, 0.12f); quadraticTo(-0.14f, 0.4f, 0f, 0.7f) }, Ink.line, style = lp.thin, alpha = 0.6f)
            inkedRound(Rect(-0.14f, 0.05f, 0.14f, 0.13f), 0.02f, Color(0xFF5B4A8A), lp, shade = false)
            inkedRound(Rect(-0.14f, 0.69f, 0.14f, 0.77f), 0.02f, Color(0xFF5B4A8A), lp, shade = false)
            shine(Offset(-0.09f, 0.3f), 0.04f, 0.12f)
        }
    }
}

private val batWing by lazy {
    Path().apply {
        moveTo(0.1f, -0.06f)
        quadraticTo(0.45f, -0.4f, 0.85f, -0.16f)
        quadraticTo(0.74f, -0.06f, 0.72f, 0.04f)
        quadraticTo(0.6f, -0.02f, 0.5f, 0.1f)
        quadraticTo(0.38f, 0.0f, 0.26f, 0.14f)
        quadraticTo(0.2f, 0.06f, 0.1f, 0.1f)
        close()
    }
}

/** A cute bat hanging from a string at [top]: round, smiling, with flapping wings. [s] is its body height in pixels. */
internal fun DrawScope.bat(top: Offset, hang: Float, s: Float, pen: Pen, phase: Float) {
    val lp = pen.local(s)
    drawLine(Ink.line, top, Offset(top.x, top.y + hang), strokeWidth = pen.lw * 0.7f)
    val flap = 0.7f + 0.3f * sin(pen.t * 5f + phase)
    loc(Offset(top.x, top.y + hang + s * 0.5f), s) {
        val plum = Color(0xFF5B4A8A)
        val dark = Color(0xFF3F3466)
        withTransform({ scale(1f, flap, Offset(0f, -0.1f)) }) {
            inked(batWing, dark, lp, shade = false)
            withTransform({ scale(-1f, 1f, Offset.Zero) }) { inked(batWing, dark, lp, shade = false) }
        }
        // Ears, body and face.
        inked(Path().apply { poly(-0.17f, -0.3f, -0.12f, -0.52f, -0.04f, -0.34f) }, plum, lp, shade = false)
        inked(Path().apply { poly(0.17f, -0.3f, 0.12f, -0.52f, 0.04f, -0.34f) }, plum, lp, shade = false)
        inked(Path().apply { addOval(Rect(-0.22f, -0.38f, 0.22f, 0.26f)) }, plum, lp)
        drawCircle(Color.White, 0.06f, Offset(-0.08f, -0.14f))
        drawCircle(Color.White, 0.06f, Offset(0.08f, -0.14f))
        drawCircle(Ink.line, 0.03f, Offset(-0.075f, -0.13f))
        drawCircle(Ink.line, 0.03f, Offset(0.085f, -0.13f))
        drawArc(Ink.line, 20f, 140f, false, Offset(-0.07f, -0.08f), Size(0.14f, 0.1f), style = Stroke(lp.lw * 1.2f, cap = StrokeCap.Round))
        drawCircle(Ink.blush, 0.04f, Offset(-0.14f, -0.07f))
        drawCircle(Ink.blush, 0.04f, Offset(0.14f, -0.07f))
    }
}

private val ghostShape by lazy {
    Path().apply {
        moveTo(-0.4f, -0.12f)
        lineTo(-0.4f, -0.62f)
        cubicTo(-0.4f, -1.05f, 0.4f, -1.05f, 0.4f, -0.62f)
        lineTo(0.4f, -0.12f)
        quadraticTo(0.34f, 0.1f, 0.27f, -0.06f)
        quadraticTo(0.2f, 0.12f, 0.13f, -0.06f)
        quadraticTo(0.06f, 0.12f, 0f, -0.06f)
        quadraticTo(-0.06f, 0.12f, -0.13f, -0.06f)
        quadraticTo(-0.2f, 0.12f, -0.27f, -0.06f)
        quadraticTo(-0.34f, 0.1f, -0.4f, -0.12f)
        close()
    }
}

/** A friendly cardboard ghost hanging from [top] by a string: [h] pixels tall, smiling, swaying a little. */
internal fun DrawScope.ghost(top: Offset, hang: Float, h: Float, pen: Pen, phase: Float) {
    val lp = pen.local(h)
    drawLine(Ink.line, top, Offset(top.x, top.y + hang), strokeWidth = pen.lw * 0.7f)
    val sway = sin(pen.t * 1.1f + phase) * 5f
    val pivot = Offset(top.x, top.y + hang)
    rotate(sway, pivot) {
        loc(Offset(pivot.x, pivot.y + h * 0.98f), h) {
            inked(ghostShape, Color(0xFFF7F3EA), lp)
            drawOval(Ink.line, Offset(-0.16f, -0.72f), Size(0.1f, 0.15f))
            drawOval(Ink.line, Offset(0.06f, -0.72f), Size(0.1f, 0.15f))
            drawArc(Ink.line, 15f, 150f, false, Offset(-0.1f, -0.58f), Size(0.2f, 0.16f), style = Stroke(lp.lw * 1.3f, cap = StrokeCap.Round))
            drawCircle(Ink.blush, 0.055f, Offset(-0.27f, -0.55f))
            drawCircle(Ink.blush, 0.055f, Offset(0.27f, -0.55f))
            // A bit of tape on top, like a paper cut-out.
            drawRect(Color(0xFFFFE9A8), Offset(-0.1f, -1.0f), Size(0.2f, 0.07f), alpha = 0.8f)
        }
    }
}

private val cobwebPath by lazy {
    Path().apply {
        // Rays from the corner (0, 0) out over a quarter turn, and sagging threads between them.
        val rays = 5
        for (i in 0 until rays) {
            val a = (i / (rays - 1f)) * (PI.toFloat() / 2f)
            moveTo(0f, 0f)
            lineTo(cos(a), sin(a))
        }
        for (ring in 1..3) {
            val r = ring * 0.27f
            for (i in 0 until rays - 1) {
                val a0 = (i / (rays - 1f)) * (PI.toFloat() / 2f)
                val a1 = ((i + 1) / (rays - 1f)) * (PI.toFloat() / 2f)
                val am = (a0 + a1) / 2f
                moveTo(cos(a0) * r, sin(a0) * r)
                quadraticTo(cos(am) * r * 0.82f, sin(am) * r * 0.82f, cos(a1) * r, sin(a1) * r)
            }
        }
    }
}

/** A cobweb in a corner: [corner] is the corner, [r] its reach in pixels, [dir] -1 for a corner on the right. */
internal fun DrawScope.cobweb(corner: Offset, r: Float, dir: Float, pen: Pen) {
    loc(corner, r) {
        withTransform({ scale(dir, 1f, Offset.Zero) }) {
            drawPath(cobwebPath, Color.White, alpha = 0.7f, style = Stroke(pen.lw / r * 0.8f, cap = StrokeCap.Round))
            drawPoints(listOf(Offset(0.27f, 0.24f), Offset(0.5f, 0.45f), Offset(0.62f, 0.14f)), PointMode.Points, Color.White, strokeWidth = pen.lw / r * 2.2f, cap = StrokeCap.Round, alpha = 0.9f)
        }
    }
}
