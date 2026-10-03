package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Cat
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Palette
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Café, salon and the troll lab, in oblique 3D.

// ------------------------------------------------------------------------------------------ café

internal fun DrawScope.fxRegister(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val brass = FxC.brass
    val d = 0.07f
    val a = f.anim
    // The ticket tower at the back, with its little pop-up tab.
    if (a > 0f) {
        val tab = fxFront(u, -0.012f, -0.1f - a * 0.012f, 0.012f, -0.08f, 0.05f)
        inkedRound(tab, 0.002f * u, FxC.paint, pen, shade = false)
        drawCircle(FxC.brass, 0.004f * u, tab.center)
    }
    fxBox(u, -0.026f, -0.088f, 0.026f, -0.056f, 0.022f, brass, pen, rad = 0.004f, z = 0.045f)
    val win = fxFront(u, -0.018f, -0.083f, 0.018f, -0.066f, 0.045f)
    inkedRound(win, 0.002f * u, FxC.cream, pen, shade = false)
    drawCircle(FxC.brass.darken(0.1f), 0.004f * u, win.center)
    drawCircle(Ink.line, 0.004f * u, win.center, style = pen.thin)
    // The drawer base, and the drawer sliding out with coins when it rings.
    fxBox(u, -0.05f, -0.022f, 0.05f, 0f, d, brass.darken(0.18f), pen, rad = 0.003f)
    // The sloping key bed.
    val k0 = q(-0.046f, -0.024f, 0.004f)
    val k1 = q(0.046f, -0.024f, 0.004f)
    val k2 = q(0.046f, -0.062f, 0.05f)
    val k3 = q(-0.046f, -0.062f, 0.05f)
    val s0 = q(0.046f, -0.022f, 0f)
    val s1 = q(0.046f, -0.022f, d)
    fxFace(fxQuad(k1.x, k1.y, k2.x, k2.y, s1.x, s1.y, s0.x, s0.y), brass.darken(0.25f), pen)
    fxFace(fxQuad(k0.x, k0.y, k3.x, k3.y, k2.x, k2.y, k1.x, k1.y), brass.lighten(0.1f), pen)
    for (row in 0 until 3) {
        val z = 0.012f + row * 0.016f
        val y = -0.03f - row * 0.013f
        for (i in 0 until 5) {
            val c = q(-0.032f + i * 0.016f, y, z)
            drawCircle(Ink.line, 0.0052f * u, c)
            drawCircle(if (row == 2) FxC.red else FxC.cream, 0.0042f * u, c)
        }
    }
    val cr = q(0.05f, -0.036f, d * 0.5f)
    capsule(cr, Offset(cr.x + 0.014f * u, cr.y + 0.006f * u), 0.004f * u, brass.darken(0.2f), pen)
    inkedCircle(Offset(cr.x + 0.014f * u, cr.y + 0.006f * u), 0.004f * u, FxC.red, pen, shade = false)
    if (a > 0f) {
        val z = -0.03f * a
        fxBox(u, -0.046f, -0.02f, 0.046f, -0.003f, 0.06f, brass.darken(0.08f), pen, rad = 0.002f, z = z, top = Color(0xFF3A2A20))
        for (i in 0 until 4) drawCircle(FxC.yellow, 0.004f * u, q(-0.03f + i * 0.02f, -0.02f, z + 0.02f + (i % 2) * 0.015f))
        twinkle(q(0.03f, -0.1f, 0.05f), 0.01f * u * a, FxC.yellow, a)
    } else {
        fxLine(p(-0.02f, -0.011f), p(0.02f, -0.011f), Ink.line.copy(alpha = 0.6f), pen.lw)
    }
}

internal fun DrawScope.fxOven(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cream = FxC.cream
    val d = 0.14f
    for (s in 0..1) {
        val x = if (s == 0) -0.08f else 0.08f
        fxBox(u, x - 0.008f, -0.01f, x + 0.008f, 0f, 0.014f, FxC.charcoal, pen, z = d - 0.02f)
    }
    fxBox(u, -0.1f, -0.24f, 0.1f, -0.008f, d, cream, pen, rad = 0.012f)
    for (s in 0..1) {
        val x = if (s == 0) -0.08f else 0.08f
        fxBox(u, x - 0.008f, -0.01f, x + 0.008f, 0f, 0.014f, FxC.charcoal, pen)
    }
    inkedRound(Rect(-0.092f * u, -0.235f * u, 0.092f * u, -0.192f * u), 0.008f * u, FxC.mint, pen)
    for (k in 0 until 3) {
        val c = p(-0.06f + k * 0.04f, -0.2135f)
        inkedCircle(c, 0.009f * u, FxC.steel, pen)
        drawLine(Ink.line, c, Offset(c.x + 0.006f * u, c.y - 0.004f * u), pen.lw * 0.8f)
    }
    val dial = p(0.062f, -0.2135f)
    inkedCircle(dial, 0.012f * u, FxC.paint, pen, shade = false)
    val turn = if (f.on) f.timer / 2.6f * 2f * FX_PI else 0f
    drawLine(FxC.red, dial, Offset(dial.x + sin(turn) * 0.009f * u, dial.y - cos(turn) * 0.009f * u), 0.0025f * u, StrokeCap.Round)
    if (f.on) fxGlow(p(0.083f, -0.2135f), 0.012f * u, FxC.flame1, 0.9f)
    drawCircle(if (f.on) FxC.flame1 else FxC.stone, 0.003f * u, p(0.083f, -0.2135f))
    if (!f.open) {
        inkedRound(Rect(-0.085f * u, -0.182f * u, 0.085f * u, -0.022f * u), 0.012f * u, cream.lighten(0.3f), pen)
        capsule(p(-0.06f, -0.172f), p(0.06f, -0.172f), 0.008f * u, FxC.steel, pen)
        val win = Rect(-0.06f * u, -0.158f * u, 0.06f * u, -0.06f * u)
        val glow = if (f.on) 0.8f + 0.2f * sin(t * 4f) else 0f
        inkedRound(win, 0.01f * u, lerp(Color(0xFF2E2A33), Color(0xFFFF8A3A), glow), pen, shade = false)
        if (f.on) {
            fxGlow(win.center, 0.08f * u, FxC.flame2, 0.5f * glow)
            fxLine(p(-0.05f, -0.09f), p(0.05f, -0.09f), Color(0xFF7A2E10), pen.lw)
            for (k in 0 until 2) {
                val ph = fxFrac(t * 0.7f + k * 0.5f)
                val w0 = q(-0.03f + k * 0.06f, -0.245f, d * 0.5f)
                val wave = Path().apply {
                    moveTo(w0.x, w0.y - ph * 0.05f * u)
                    quadraticTo(w0.x + 0.008f * u, w0.y - (0.02f + ph * 0.05f) * u, w0.x, w0.y - (0.04f + ph * 0.05f) * u)
                }
                drawPath(wave, Color.White.copy(alpha = 0.45f * (1f - ph)), style = pen.thin)
            }
        } else {
            fxLine(p(-0.05f, -0.09f), p(0.05f, -0.09f), FxC.stone.copy(alpha = 0.5f), pen.lw * 0.6f)
        }
        shine(p(-0.04f, -0.14f), 0.024f * u, 0.008f * u, 0.35f)
        return
    }
    // Open: the baking chamber with its rack, and the door folded down in front.
    fxHollow(u, -0.08f, -0.178f, 0.08f, -0.03f, d - 0.02f, Color(0xFF4A3C3A), pen)
    clipRect(-0.08f * u, -0.178f * u, 0.08f * u, -0.03f * u) {
        fxGlow(q(-0.05f, -0.17f, 0.05f), 0.06f * u, FxC.warm, 0.7f)
        for (k in 0 until 9) {
            val x = -0.08f + k * 0.02f
            fxLine(q(x, -0.035f, 0f), q(x, -0.035f, d - 0.02f), FxC.steel.darken(0.1f), pen.lw * 0.6f)
        }
        fxLine(p(-0.08f, -0.035f), p(0.08f, -0.035f), FxC.steel, pen.lw * 1.2f)
    }
    // The door swung out on its left hinge, its window facing us.
    val w = 0.17f
    val v = fxDoorVec(u, w, -1f, 115f)
    fxOpenDoor(u, FixtureDoors.oven, cream, cream.lighten(0.25f), pen)
    fun onDoor(k: Float, y: Float) = Offset(-0.085f * u + v.x * k, y * u + v.y * k)
    val win = fxPath(onDoor(0.18f, -0.158f), onDoor(0.82f, -0.158f), onDoor(0.82f, -0.06f), onDoor(0.18f, -0.06f))
    fxFace(win, Color(0xFF3A3040), pen)
    fxLine(onDoor(0.93f, -0.15f), onDoor(0.93f, -0.07f), FxC.steel, 0.007f * u)
}

/** The fruit colour of a thing in the blender or the pot; other things are a soft grey-green. */
private fun fxTint(type: ThingType): Color {
    val i = ThingType.FRUITS.indexOf(type)
    if (i >= 0) return argb(Palette.juice[i])
    return when (type.cat) {
        Cat.NATURE -> Color(0xFF7ACB6A)
        Cat.MAGIC -> Color(0xFFFF6FD8)
        Cat.POTION -> Color(0xFF6FE0FF)
        Cat.FOOD -> Color(0xFFFFA64D)
        Cat.TOY -> Color(0xFFFFD84A)
        else -> Color(0xFF9DB08A)
    }
}

internal fun DrawScope.fxBlender(f: Fixture, u: Float, pen: Pen, contents: List<Thing>) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val shake = if (f.on) sin(t * 60f) * 0.0012f * u else 0f
    translate(shake, 0f) {
        fxBox(u, -0.032f, -0.048f, 0.032f, 0f, 0.06f, FxC.red, pen, rad = 0.01f)
        fxLine(p(-0.028f, -0.04f), p(0.028f, -0.04f), FxC.steel, 0.004f * u)
        for (k in 0 until 3) inkedCircle(p(-0.016f + k * 0.016f, -0.022f), 0.0045f * u, if (f.on && k == 1) FxC.yellow else FxC.paint, pen, shade = false)
        val cz = 0.03f
        val jb = q(0f, -0.05f, cz)
        val jt = q(0f, -0.14f, cz)
        val rb = 0.021f * u
        val rt = 0.03f * u
        val jar = Path().apply {
            moveTo(jt.x - rt * 1.118f, jt.y + rt * 0.161f)
            lineTo(jt.x + rt * 1.118f, jt.y - rt * 0.161f)
            lineTo(jb.x + rb * 1.118f, jb.y - rb * 0.161f)
            lineTo(jb.x - rb * 1.118f, jb.y + rb * 0.161f)
            close()
        }
        drawPath(jar, Color(0x40CFEFFF))
        // What is inside: chunks piled up, whirling when on.
        val mid = Offset((jb.x + jt.x) / 2f, (jb.y + jt.y) / 2f)
        if (contents.isNotEmpty()) {
            clipPath(jar) {
                if (f.on) {
                    var r = 0f
                    var g = 0f
                    var b = 0f
                    for (c in contents) {
                        val col = fxTint(c.type)
                        r += col.red
                        g += col.green
                        b += col.blue
                    }
                    val n = contents.size.toFloat()
                    val mix = Color(r / n, g / n, b / n)
                    drawRect(mix.copy(alpha = 0.6f), Offset(jb.x - rt * 1.3f, mid.y - 0.01f * u), Size(rt * 2.6f, jb.y - mid.y + 0.02f * u))
                    for (k in 0 until 2) drawArc(Color.White.copy(alpha = 0.5f), t * 900f + k * 180f, 120f, false, Offset(mid.x - rt * 0.8f, mid.y - 0.01f * u), Size(rt * 1.6f, 0.03f * u), style = Stroke(0.003f * u, cap = StrokeCap.Round))
                }
                contents.forEachIndexed { i, c ->
                    val col = fxTint(c.type)
                    for (k in 0 until 2) {
                        val c0 = if (f.on) {
                            val ang = t * 14f + i * 2.1f + k * FX_PI
                            Offset(mid.x + cos(ang) * rt * 0.7f, mid.y + sin(ang * 0.7f) * 0.02f * u)
                        } else {
                            val slot = i * 2 + k
                            Offset(jb.x - rb * 0.8f + (slot % 3) * rb * 0.8f, jb.y - 0.012f * u - (slot / 3) * 0.012f * u)
                        }
                        drawRoundRect(Ink.line, Offset(c0.x - 0.0065f * u, c0.y - 0.0065f * u), Size(0.013f * u, 0.013f * u), androidx.compose.ui.geometry.CornerRadius(0.003f * u))
                        drawRoundRect(col, Offset(c0.x - 0.0055f * u, c0.y - 0.0055f * u), Size(0.011f * u, 0.011f * u), androidx.compose.ui.geometry.CornerRadius(0.0025f * u))
                    }
                }
            }
        }
        fxLine(Offset(jb.x - 0.01f * u, jb.y - 0.004f * u), Offset(jb.x + 0.01f * u, jb.y - 0.006f * u), FxC.steel, 0.003f * u)
        drawLine(Ink.line, Offset(jt.x - rt * 1.118f, jt.y + rt * 0.161f), Offset(jb.x - rb * 1.118f, jb.y + rb * 0.161f), pen.lw)
        drawLine(Ink.line, Offset(jt.x + rt * 1.118f, jt.y - rt * 0.161f), Offset(jb.x + rb * 1.118f, jb.y - rb * 0.161f), pen.lw)
        drawPath(fxDisc(jb.x, jb.y, rb), Ink.line, style = pen.thin)
        fxLine(Offset(jt.x - rt * 0.8f, jt.y + 0.01f * u), Offset(jb.x - rb * 0.8f, jb.y - 0.008f * u), Color.White.copy(alpha = 0.7f), 0.003f * u)
        for (k in 0 until 3) fxLine(Offset(jb.x + rb * 0.7f, jb.y - (0.02f + k * 0.02f) * u), Offset(jb.x + rb * 1.0f, jb.y - (0.021f + k * 0.02f) * u), Ink.line.copy(alpha = 0.5f), pen.lw * 0.5f)
        val handle = Path().apply {
            moveTo(jt.x + rt * 1.0f, jt.y + 0.012f * u)
            quadraticTo(jt.x + rt * 1.9f, jt.y + 0.02f * u, jb.x + rb * 1.8f, jb.y - 0.03f * u)
            quadraticTo(jb.x + rb * 1.5f, jb.y - 0.012f * u, jb.x + rb * 0.9f, jb.y - 0.012f * u)
        }
        drawPath(handle, Ink.line, style = Stroke(0.007f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(handle, FxC.charcoal, style = Stroke(0.007f * u, cap = StrokeCap.Round))
        fxCyl(jt.x, jt.y + 0.002f * u, jt.y - 0.01f * u, 0.031f * u, 0.031f * u, FxC.charcoal, pen)
        val cap = Offset(jt.x, jt.y - 0.01f * u)
        fxCyl(cap.x, cap.y, cap.y - 0.008f * u, 0.009f * u, 0.009f * u, FxC.charcoal.lighten(0.1f), pen)
    }
}

internal fun DrawScope.fxFruitCrate(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = FxC.oak
    val d = 0.1f
    fxFace(fxDeep(u, 0.08f, -0.07f, 0f, 0f, d), wood.darken(0.22f), pen)
    fxFace(fxFlat(u, -0.08f, 0.08f, -0.07f, 0f, d), Color(0xFF5A3F2A), pen)
    // A heap of fruit: bananas at the back, apples and cloudberries in front.
    for (k in 0 until 2) {
        val b = q(0.025f + k * 0.016f, -0.082f - k * 0.008f, 0.075f)
        val banana = Path().apply {
            moveTo(b.x - 0.032f * u, b.y - 0.012f * u)
            quadraticTo(b.x - 0.004f * u, b.y + 0.03f * u, b.x + 0.032f * u, b.y - 0.016f * u)
            quadraticTo(b.x, b.y - 0.002f * u, b.x - 0.032f * u, b.y - 0.012f * u)
            close()
        }
        inked(banana, Color(0xFFFFD84A), pen)
        drawCircle(FxC.walnut, 0.0025f * u, Offset(b.x + 0.032f * u, b.y - 0.016f * u))
        fxLine(Offset(b.x - 0.01f * u, b.y + 0.003f * u), Offset(b.x + 0.012f * u, b.y + 0.001f * u), Color(0xFFE0B030), pen.lw * 0.6f)
    }
    val apples = floatArrayOf(-0.058f, 0.05f, -0.03f, 0.03f, -0.002f, 0.055f, 0.03f, 0.02f)
    for (i in 0 until 4) {
        val c = q(apples[i * 2], -0.078f, apples[i * 2 + 1])
        inkedCircle(c, 0.017f * u, FxC.red, pen)
        drawLine(FxC.walnut, Offset(c.x, c.y - 0.016f * u), Offset(c.x + 0.003f * u, c.y - 0.024f * u), pen.lw * 0.9f, StrokeCap.Round)
        drawPath(fxLeaf(c.x + 0.003f * u, c.y - 0.021f * u, c.x + 0.014f * u, c.y - 0.026f * u, 0.3f), FxC.green)
        shine(Offset(c.x - 0.006f * u, c.y - 0.006f * u), 0.006f * u, 0.004f * u, 0.7f)
    }
    for (i in 0 until 3) {
        val c = q(-0.05f + i * 0.045f, -0.072f, 0.008f)
        drawPath(starPath(Offset(c.x, c.y + 0.006f * u), 0.009f * u, 0.004f * u), FxC.green)
        fxCloud(
            Color(0xFFFFA84A), pen, false,
            c.x - 0.004f * u, c.y, 0.0045f * u, c.x + 0.004f * u, c.y, 0.0045f * u,
            c.x, c.y - 0.006f * u, 0.0045f * u, c.x, c.y + 0.001f * u, 0.004f * u,
        )
        drawCircle(Color.White.copy(alpha = 0.7f), 0.0014f * u, Offset(c.x - 0.002f * u, c.y - 0.007f * u))
    }
    // The front of the crate: slats with dark gaps and corner posts.
    drawRect(Color(0xFF4A3322), p(-0.08f, -0.07f), Size(0.16f * u, 0.07f * u))
    for (y in floatArrayOf(-0.07f, -0.042f, -0.016f)) {
        inkedRound(Rect(-0.08f * u, y * u, 0.08f * u, (y + 0.02f) * u), 0.002f * u, wood, pen, shade = false)
        fxNail(p(-0.074f, y + 0.01f), 0.0018f * u)
        fxNail(p(0.074f, y + 0.01f), 0.0018f * u)
    }
    fxGrain(Rect(-0.07f * u, -0.068f * u, 0.07f * u, -0.052f * u), wood, pen, 1)
    val stamp = p(0f, -0.032f)
    drawCircle(FxC.red.copy(alpha = 0.7f), 0.006f * u, stamp)
    drawPath(fxLeaf(stamp.x, stamp.y - 0.005f * u, stamp.x + 0.008f * u, stamp.y - 0.009f * u, 0.3f), FxC.green.copy(alpha = 0.7f))
}

internal fun DrawScope.fxIceCream(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val cream = FxC.cream
    val d = 0.1f
    // The big soft-serve sign on top.
    val sc = q(0f, -0.205f, d * 0.5f)
    translate(sc.x, sc.y) {
        inked(fxPoly(u, -0.016f, 0f, 0.016f, 0f, 0.012f, -0.014f, -0.012f, -0.014f), Color(0xFFE8B070), pen, shade = false)
        fxCloud(Color(0xFFFFF4E2), pen, true, 0f, -0.02f * u, 0.016f * u)
        fxCloud(FxC.pink.lighten(0.3f), pen, true, 0f, -0.034f * u, 0.012f * u)
        fxCloud(Color(0xFFFFF4E2), pen, true, 0.002f * u, -0.045f * u, 0.008f * u)
        val tip = fxPoly(u, -0.004f, -0.05f, 0.004f, -0.05f, 0.002f, -0.06f)
        inked(tip, FxC.pink.lighten(0.3f), pen, shade = false)
        for (k in 0 until 4) drawLine(arrayOf(FxC.red, FxC.yellow, FxC.fjord, FxC.green)[k], Offset((-0.008f + k * 0.005f) * u, -0.03f * u + (k % 2) * 0.004f * u), Offset((-0.006f + k * 0.005f) * u, -0.028f * u + (k % 2) * 0.004f * u), 0.002f * u, StrokeCap.Round)
    }
    fxBox(u, -0.055f, -0.205f, 0.055f, -0.188f, d - 0.02f, FxC.mint, pen, rad = 0.006f, z = 0.01f)
    fxBox(u, -0.065f, -0.19f, 0.065f, -0.01f, d, cream, pen, rad = 0.012f)
    val panel = Rect(-0.055f * u, -0.085f * u, 0.055f * u, -0.018f * u)
    clipRect(panel.left, panel.top, panel.right, panel.bottom) {
        for (k in 0 until 6) drawRect(FxC.pink.copy(alpha = 0.55f), Offset(panel.left + k * 0.02f * u, panel.top), Size(0.01f * u, panel.height))
    }
    drawRoundRect(Ink.line, panel.topLeft, panel.size, androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
    for (k in 0 until 3) inkedCircle(p(-0.03f + k * 0.03f, -0.105f), 0.006f * u, arrayOf(FxC.pink, Color(0xFFFFF4E2), Color(0xFF8A5A3A))[k], pen, shade = false)
    // Drip tray and the chrome head with its lever.
    fxBox(u, -0.045f, -0.07f, 0.045f, -0.058f, 0.05f, FxC.steel, pen, rad = 0.003f, z = -0.02f)
    fxBox(u, -0.03f, -0.165f, 0.03f, -0.13f, 0.04f, FxC.steel, pen, rad = 0.008f, z = -0.02f)
    val head = q(0f, -0.165f, 0f)
    capsule(head, Offset(head.x + 0.02f * u, head.y - 0.035f * u), 0.005f * u, FxC.steel, pen)
    inkedCircle(Offset(head.x + 0.02f * u, head.y - 0.035f * u), 0.007f * u, FxC.red, pen)
    val spout = fxFront(u, -0.008f, -0.13f, 0.008f, -0.12f, -0.02f)
    inkedRound(spout, 0.002f * u, FxC.steel.darken(0.1f), pen, shade = false)
    if (f.anim > 0f) {
        val a = f.anim
        val s = spout.center
        fxCloud(Color(0xFFFFF4E2), pen, false, s.x, s.y + 0.012f * u * a + 0.006f * u, 0.007f * u * a + 0.002f * u)
    }
    twinkle(q(-0.02f, -0.15f, -0.02f), 0.005f * u, Color.White, 0.8f)
}

internal fun DrawScope.fxDisplayCase(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.12f
    val frame = FxC.cream
    val back = fxFront(u, -0.122f, -0.165f, 0.122f, -0.02f, d - 0.006f)
    inkedRound(back, 0.002f * u, Color(0xFFFBE3EC), pen, shade = false)
    clipRect(back.left, back.top, back.right, back.bottom) {
        var x = back.left + 0.012f * u
        while (x < back.right) {
            drawLine(Color.White.copy(alpha = 0.6f), Offset(x, back.top), Offset(x, back.bottom), 0.004f * u)
            x += 0.024f * u
        }
    }
    fxBox(u, 0.122f, -0.165f, 0.132f, -0.02f, 0.01f, frame, pen, z = d - 0.01f)
    fxBox(u, -0.132f, -0.165f, -0.122f, -0.02f, 0.01f, frame, pen, z = d - 0.01f)
    fxBox(u, -0.13f, -0.02f, 0.13f, 0f, d, FxC.mint, pen, rad = 0.003f)
    for (k in 0 until 3) {
        val c = q(-0.08f + k * 0.08f, -0.02f, 0.05f)
        drawPath(fxDisc(c.x, c.y, 0.03f * u), Color.White.copy(alpha = 0.9f))
        drawPath(fxDisc(c.x, c.y, 0.03f * u), Ink.line.copy(alpha = 0.3f), style = pen.thin)
    }
    // The glass shelf.
    val shelf = fxFlat(u, -0.122f, 0.122f, -0.09f, 0f, d - 0.008f)
    drawPath(shelf, Color(0x66DFF4FF))
    drawPath(shelf, Ink.line.copy(alpha = 0.5f), style = pen.thin)
    drawLine(Color.White.copy(alpha = 0.8f), Offset(-0.122f * u, -0.09f * u), Offset(0.122f * u, -0.09f * u), pen.lw)
    // Right side glass, top board and the front posts.
    val side = fxDeep(u, 0.126f, -0.165f, -0.02f, 0f, d)
    drawPath(side, Color(0x44CFEFFF))
    drawPath(side, Ink.line, style = pen.thin)
    fxLine(q(0.126f, -0.15f, 0.02f), q(0.126f, -0.11f, 0.08f), Color.White.copy(alpha = 0.5f), 0.003f * u)
    fxBox(u, -0.132f, -0.18f, 0.132f, -0.165f, d + 0.004f, frame, pen, rad = 0.003f)
    for (k in 0 until 3) {
        val c = q(-0.08f + k * 0.08f, -0.163f, 0.05f)
        fxGlow(c, 0.02f * u, FxC.warm, 0.5f + 0.3f * pen.night)
        drawCircle(Color(0xFFFFF4C8), 0.003f * u, c)
    }
    fxBox(u, -0.132f, -0.165f, -0.122f, -0.02f, 0.01f, frame, pen)
    fxBox(u, 0.122f, -0.165f, 0.132f, -0.02f, 0.01f, frame, pen)
}

/** The display case's glass front, which slides up when it opens. */
internal fun DrawScope.fxDisplayGlass(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val ease = if (f.open) 1f - f.anim.coerceIn(0f, 1f) else f.anim.coerceIn(0f, 1f)
    val slide = -0.12f * ease
    val pane = Rect(-0.122f * u, (-0.165f + slide) * u, 0.122f * u, (-0.02f + slide) * u)
    drawRect(Color(0x30BFE8FF), pane.topLeft, pane.size)
    val streak = Path().apply {
        moveTo(pane.left + 0.03f * u, pane.bottom)
        lineTo(pane.left + 0.06f * u, pane.bottom)
        lineTo(pane.left + 0.11f * u, pane.top)
        lineTo(pane.left + 0.08f * u, pane.top)
        close()
    }
    drawPath(streak, Color.White.copy(alpha = 0.3f))
    fxLine(Offset(pane.left + 0.13f * u, pane.bottom - 0.01f * u), Offset(pane.left + 0.165f * u, pane.top + 0.02f * u), Color.White.copy(alpha = 0.45f), 0.004f * u)
    drawRect(FxC.steel, pane.topLeft, pane.size, style = Stroke(0.003f * u))
    drawRect(Ink.line, pane.topLeft, pane.size, style = pen.thin)
    inkedRound(Rect(-0.012f * u, pane.bottom - 0.01f * u, 0.012f * u, pane.bottom - 0.004f * u), 0.002f * u, FxC.steel, pen, shade = false)
    if (ease < 0.01f) drawLine(Color.White.copy(alpha = 0.6f), p(-0.11f, -0.16f), p(-0.09f, -0.16f), pen.lw)
}

internal fun DrawScope.fxFlourSack(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val burlap = Color(0xFFD9BC8C)
    val sack = fxBlob(u, -0.05f, -0.1f, -0.058f, -0.05f, -0.052f, 0f, 0f, 0.004f, 0.052f, 0f, 0.058f, -0.05f, 0.05f, -0.1f, 0f, -0.095f)
    for (k in 3 downTo 1) {
        val o = q(0f, 0f, k * 0.018f)
        translate(o.x, o.y) {
            drawPath(sack, burlap.darken(0.25f))
            if (k == 3) drawPath(sack, Ink.line, style = pen.stroke)
        }
    }
    // The open top with its rolled rim and a mound of flour.
    val rim = q(0f, -0.105f, 0.03f)
    drawPath(fxDisc(rim.x, rim.y, 0.05f * u), burlap.darken(0.1f))
    drawPath(fxDisc(rim.x, rim.y, 0.05f * u), Ink.line, style = pen.stroke)
    val flour = fxDisc(rim.x, rim.y - 0.002f * u, 0.04f * u)
    drawPath(flour, Color.White)
    fxCloud(Color.White, pen, true, rim.x - 0.012f * u, rim.y - 0.008f * u, 0.013f * u, rim.x + 0.008f * u, rim.y - 0.012f * u, 0.014f * u)
    capsule(Offset(rim.x + 0.01f * u, rim.y - 0.01f * u), Offset(rim.x + 0.032f * u, rim.y - 0.042f * u), 0.005f * u, FxC.steel, pen)
    inked(sack, burlap, pen, outline = false)
    clipPath(sack) {
        var x = -0.06f
        while (x < 0.06f) {
            drawLine(burlap.darken(0.25f).copy(alpha = 0.3f), Offset(x * u, -0.11f * u), Offset(x * u, 0.01f * u), pen.lw * 0.5f)
            x += 0.01f
        }
        var y = -0.1f
        while (y < 0.01f) {
            drawLine(burlap.darken(0.25f).copy(alpha = 0.3f), Offset(-0.06f * u, y * u), Offset(0.06f * u, y * u), pen.lw * 0.5f)
            y += 0.01f
        }
        val c = Offset(0f, -0.05f * u)
        drawCircle(FxC.red.copy(alpha = 0.55f), 0.022f * u, c, style = Stroke(0.003f * u))
        drawLine(FxC.fjord.copy(alpha = 0.7f), Offset(c.x, c.y + 0.016f * u), Offset(c.x, c.y - 0.016f * u), 0.0025f * u)
        for (k in 0 until 3) {
            val yy = c.y - 0.012f * u + k * 0.008f * u
            drawOval(FxC.fjord.copy(alpha = 0.7f), Offset(c.x - 0.009f * u, yy - 0.002f * u), Size(0.007f * u, 0.004f * u))
            drawOval(FxC.fjord.copy(alpha = 0.7f), Offset(c.x + 0.002f * u, yy - 0.002f * u), Size(0.007f * u, 0.004f * u))
        }
    }
    drawPath(sack, Ink.line, style = pen.stroke)
    inkedRound(fxFront(u, -0.052f, -0.112f, 0.052f, -0.094f, 0f), 0.008f * u, burlap.darken(0.06f), pen)
    for (k in 0 until 4) {
        val ph = fxFrac(t * 0.3f + k * 0.25f)
        drawCircle(Color.White.copy(alpha = 0.5f * (1f - ph)), (0.003f + ph * 0.004f) * u, Offset(rim.x + (k - 1.5f) * 0.015f * u + sin(t + k) * 0.004f * u, rim.y - (0.015f + ph * 0.05f) * u))
    }
    if (f.anim > 0f) fxPuffs(rim.x, rim.y - 0.01f * u, 1f - f.anim, 0.02f * u, 0.05f * u, Color.White, 0.7f * f.anim, 3, 1f, 0.01f * u)
}

// ------------------------------------------------------------------------------------------ salon

internal fun DrawScope.fxSalonChair(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val lift = if (f.mode == 1) 0.04f else 0f
    val vinyl = FxC.pink
    val cz = 0.055f
    val b = q(0f, 0f, cz)
    fxCyl(b.x, b.y, b.y - 0.012f * u, 0.052f * u, 0.04f * u, FxC.steel, pen)
    val pedal = q(0.05f, -0.006f, cz)
    capsule(pedal, Offset(pedal.x + 0.02f * u, pedal.y + 0.002f * u), 0.006f * u, FxC.charcoal, pen)
    val pt = q(0f, -0.075f - lift, cz)
    fxCyl(b.x, b.y - 0.012f * u, pt.y, 0.009f * u, 0.009f * u, FxC.steel, pen, cap = false, bottom = false)
    fxBox(u, -0.05f, -0.205f - lift, 0.05f, -0.095f - lift, 0.025f, vinyl.darken(0.06f), pen, rad = 0.02f, z = 0.09f)
    val bf = fxFront(u, -0.05f, -0.205f - lift, 0.05f, -0.095f - lift, 0.09f)
    for (k in 0 until 4) drawCircle(vinyl.darken(0.35f), 0.003f * u, Offset(bf.left + (0.3f + (k % 2) * 0.4f) * bf.width, bf.top + (0.3f + (k / 2) * 0.35f) * bf.height))
    fxBox(u, -0.072f, -0.134f - lift, -0.054f, -0.12f - lift, 0.1f, FxC.steel, pen, rad = 0.004f, z = 0.005f)
    fxBox(u, -0.066f, -0.12f - lift, -0.06f, -0.09f - lift, 0.006f, FxC.steel, pen, z = 0.02f)
    fxBox(u, -0.055f, -0.1f - lift, 0.055f, -0.075f - lift, 0.11f, vinyl, pen, rad = 0.01f)
    drawLine(Color.White.copy(alpha = 0.6f), q(-0.046f, -0.093f - lift, 0f), q(0.046f, -0.093f - lift, 0f), pen.lw * 0.6f)
    fxBox(u, 0.054f, -0.134f - lift, 0.072f, -0.12f - lift, 0.1f, FxC.steel, pen, rad = 0.004f, z = 0.005f)
    fxBox(u, 0.06f, -0.12f - lift, 0.066f, -0.09f - lift, 0.006f, FxC.steel, pen, z = 0.02f)
    val fr = Path().apply {
        val a = q(-0.03f, -0.075f - lift, 0f)
        val c = q(-0.036f, -0.035f - lift * 0.5f, -0.012f)
        val e = q(0.036f, -0.035f - lift * 0.5f, -0.012f)
        val g = q(0.03f, -0.075f - lift, 0f)
        moveTo(a.x, a.y)
        lineTo(c.x, c.y)
        lineTo(e.x, e.y)
        lineTo(g.x, g.y)
    }
    drawPath(fr, Ink.line, style = Stroke(0.005f * u + pen.lw * 2f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawPath(fr, FxC.steel, style = Stroke(0.005f * u, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}

private fun fxDome(c: Offset, r: Float): Path = Path().apply {
    moveTo(c.x - r, c.y)
    cubicTo(c.x - r, c.y - r * 1.35f, c.x + r, c.y - r * 1.35f, c.x + r, c.y)
    quadraticTo(c.x + r * 0.2f, c.y + r * 0.42f, c.x - r, c.y)
    close()
}

private const val DRYER_CZ = 0.05f
private const val DRYER_Y = -0.252f
private const val DRYER_R = 0.095f

internal fun DrawScope.fxDryer(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val lilac = FxC.lilac
    val pole = q(0.03f, 0f, 0.13f)
    val poleTop = q(0.03f, -0.33f, 0.13f)
    fxCyl(pole.x, pole.y, pole.y - 0.01f * u, 0.04f * u, 0.04f * u, FxC.steel, pen)
    fxCyl(pole.x, pole.y - 0.01f * u, poleTop.y, 0.007f * u, 0.007f * u, FxC.steel, pen)
    val c = q(0f, DRYER_Y, DRYER_CZ)
    capsule(poleTop, Offset(c.x + 0.05f * u, c.y - 0.05f * u), 0.008f * u, FxC.steel, pen)
    // Inside the hood, behind the head.
    val dome = fxDome(c, DRYER_R * u)
    drawPath(dome, lilac.darken(0.45f))
    drawPath(dome, Ink.line, style = pen.thin)
    for (k in 0 until 3) drawArc(lilac.darken(0.25f), 200f + k * 10f, 140f - k * 20f, false, Offset(c.x - (0.06f - k * 0.015f) * u, c.y - (0.09f - k * 0.02f) * u), Size((0.12f - k * 0.03f) * u, (0.12f - k * 0.03f) * u), style = pen.thin)
    // The chair.
    for (s in 0..1) {
        val x = if (s == 0) -0.045f else 0.045f
        capsule(q(x, -0.066f, 0.09f), q(x * 1.2f, 0f, 0.1f), 0.006f * u, FxC.steel, pen)
    }
    fxBox(u, -0.05f, -0.19f, 0.05f, -0.085f, 0.025f, lilac.darken(0.06f), pen, rad = 0.018f, z = 0.085f)
    fxBox(u, -0.058f, -0.09f, 0.058f, -0.066f, 0.11f, lilac, pen, rad = 0.01f)
    for (s in 0..1) {
        val x = if (s == 0) -0.045f else 0.045f
        capsule(q(x, -0.066f, 0.01f), q(x * 1.2f, 0f, 0.0f), 0.006f * u, FxC.steel, pen)
    }
}

/** The see-through hood over the sitter's head; warm air ripples inside while it runs. */
internal fun DrawScope.fxDryerFront(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val shake = if (f.on) sin(t * 40f) * 0.001f * u else 0f
    val c = fxQ(u, 0f, DRYER_Y, DRYER_CZ) + Offset(shake, 0f)
    val r = DRYER_R * u
    val dome = fxDome(c, r)
    drawPath(dome, FxC.lilac.copy(alpha = 0.42f))
    if (f.on) {
        clipPath(dome) {
            fxGlow(Offset(c.x, c.y - r * 0.4f), r, FxC.flame2, 0.25f)
            for (k in 0 until 3) {
                val ph = fxFrac(t * 1.2f + k * 0.33f)
                val y = c.y - r * (0.1f + ph * 0.8f)
                val wave = Path().apply {
                    moveTo(c.x - r * 0.5f, y)
                    quadraticTo(c.x - r * 0.25f, y - 0.01f * u, c.x, y)
                    quadraticTo(c.x + r * 0.25f, y + 0.01f * u, c.x + r * 0.5f, y)
                }
                drawPath(wave, Color.White.copy(alpha = 0.5f * (1f - ph)), style = pen.thin)
            }
        }
    }
    for (k in 0 until 4) {
        val x = c.x - r * 0.45f + k * r * 0.3f
        fxLine(Offset(x, c.y - r * 0.95f), Offset(x + r * 0.05f, c.y - r * 0.78f), FxC.lilac.darken(0.35f).copy(alpha = 0.7f), 0.003f * u)
    }
    drawPath(dome, Ink.line, style = pen.stroke)
    val rim = Path().apply {
        moveTo(c.x - r, c.y)
        quadraticTo(c.x + r * 0.2f, c.y + r * 0.42f, c.x + r, c.y)
    }
    drawPath(rim, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(rim, if (f.on) lerp(FxC.lilac.darken(0.1f), FxC.flame2, 0.3f) else FxC.lilac.darken(0.1f), style = Stroke(0.012f * u, cap = StrokeCap.Round))
    drawArc(Color.White.copy(alpha = 0.55f), 200f, 55f, false, Offset(c.x - r * 0.8f, c.y - r * 1.05f), Size(r * 1.2f, r * 1.4f), style = Stroke(0.005f * u, cap = StrokeCap.Round))
}

internal fun DrawScope.fxHairWash(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val pink = FxC.pink
    val bz = 0.13f
    // The basin behind the chair, with its tall spray tap.
    val col = q(0f, -0.06f, bz)
    val colTop = q(0f, -0.16f, bz)
    fxCyl(col.x, col.y, colTop.y, 0.02f * u, 0.02f * u, FxC.porcelain, pen, cap = false)
    val bowlTop = q(0f, -0.2f, bz)
    fxCyl(colTop.x, colTop.y, bowlTop.y, 0.035f * u, 0.062f * u, FxC.porcelain, pen, top = FxC.porcelain)
    drawPath(fxDisc(bowlTop.x, bowlTop.y + 0.003f * u, 0.048f * u), Color(0xFFD6E6F2))
    drawPath(fxDisc(bowlTop.x, bowlTop.y + 0.003f * u, 0.048f * u), Ink.line, style = pen.thin)
    if (f.on) drawPath(fxDisc(bowlTop.x, bowlTop.y + 0.005f * u, 0.036f * u), FxC.water.copy(alpha = 0.7f))
    val tapBase = q(-0.035f, -0.2f, bz + 0.035f)
    val tapTop = q(-0.035f, -0.365f, bz + 0.035f)
    val head = q(0.0f, -0.352f, bz - 0.03f)
    capsule(tapBase, tapTop, 0.008f * u, FxC.steel, pen)
    capsule(tapTop, head, 0.008f * u, FxC.steel, pen)
    inkedCircle(head, 0.009f * u, FxC.steel, pen)
    if (f.on) {
        for (k in 0 until 5) {
            val dx = (k - 2) * 0.01f * u
            val ph = fxFrac(t * 3f + k * 0.2f)
            drawLine(FxC.water.copy(alpha = 0.7f), Offset(head.x + dx * 0.3f, head.y + 0.008f * u), Offset(head.x + dx * 1.6f, bowlTop.y), 0.003f * u, StrokeCap.Round)
            drawCircle(FxC.water, 0.003f * u, Offset(head.x + dx * (0.3f + ph * 1.3f), head.y + 0.008f * u + ph * (bowlTop.y - head.y)))
        }
        for (k in 0 until 3) {
            val ph = fxFrac(t * 0.4f + k * 0.33f)
            drawCircle(Color.White.copy(alpha = 0.8f * (1f - ph)), 0.006f * u, Offset(bowlTop.x + (k - 1) * 0.025f * u, bowlTop.y - ph * 0.08f * u), style = pen.thin)
        }
    }
    // The chair.
    fxBox(u, -0.08f, -0.058f, 0.08f, 0f, 0.1f, FxC.lilac.darken(0.12f), pen, rad = 0.006f)
    fxBox(u, -0.055f, -0.178f, 0.055f, -0.075f, 0.022f, pink.darken(0.06f), pen, rad = 0.02f, z = 0.08f)
    fxBox(u, -0.065f, -0.08f, 0.065f, -0.058f, 0.1f, pink, pen, rad = 0.012f)
    drawLine(Color.White.copy(alpha = 0.6f), q(-0.055f, -0.074f, 0f), q(0.055f, -0.074f, 0f), pen.lw * 0.6f)
}

internal fun DrawScope.fxClothesRack(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val metal = FxC.brass
    val rz = 0.04f
    for (s in 0..1) {
        val x = if (s == 0) -0.15f else 0.15f
        capsule(q(x, -0.01f, -0.01f), q(x, -0.01f, 0.09f), 0.006f * u, metal.darken(0.1f), pen)
        inkedCircle(q(x, -0.006f, 0.09f), 0.007f * u, FxC.charcoal, pen, shade = false)
    }
    capsule(q(-0.15f, -0.012f, rz), q(-0.15f, -0.28f, rz), 0.007f * u, metal, pen)
    // Six hangers: tee, hoodie, dress, stripes, overalls and a lusekofte.
    val o = q(0f, 0f, rz)
    translate(o.x, o.y) {
        for (i in 0 until 6) {
            val x = -0.16f + (i + 0.5f) * (0.32f / 6f)
            val sway = sin(t * 1.5f + i * 1.3f) * 2f + f.anim * sin(t * 20f + i) * 6f
            rotate(sway, p(x, -0.28f)) {
                drawArc(Ink.line, 180f, 220f, false, p(x - 0.006f, -0.292f), Size(0.012f * u, 0.014f * u), style = Stroke(pen.lw))
                val hanger = Path().apply {
                    moveTo(x * u, -0.274f * u)
                    lineTo((x - 0.022f) * u, -0.259f * u)
                    lineTo((x + 0.022f) * u, -0.259f * u)
                    close()
                }
                drawPath(hanger, FxC.oakDark, style = Stroke(0.003f * u, cap = StrokeCap.Round))
                fxGarment(i, x, u, pen)
            }
        }
    }
    capsule(q(-0.158f, -0.28f, rz), q(0.158f, -0.28f, rz), 0.006f * u, metal, pen)
    capsule(q(0.15f, -0.012f, rz), q(0.15f, -0.28f, rz), 0.007f * u, metal, pen)
    inkedCircle(q(-0.15f, -0.006f, -0.01f), 0.007f * u, FxC.charcoal, pen, shade = false)
    inkedCircle(q(0.15f, -0.006f, -0.01f), 0.007f * u, FxC.charcoal, pen, shade = false)
    inkedCircle(q(-0.16f, -0.28f, rz), 0.006f * u, metal, pen, shade = false)
    inkedCircle(q(0.16f, -0.28f, rz), 0.006f * u, metal, pen, shade = false)
}

private fun DrawScope.fxGarment(style: Int, x: Float, u: Float, pen: Pen) {
    val top = -0.262f
    fun shirt(color: Color, long: Boolean, bottom: Float): Path {
        val sleeveX = 0.03f
        val sleeveY = if (long) 0.058f else 0.02f
        val body = 0.019f
        return Path().apply {
            moveTo((x - 0.01f) * u, top * u)
            lineTo((x - 0.021f) * u, (top + 0.002f) * u)
            lineTo((x - sleeveX) * u, (top + sleeveY) * u)
            lineTo((x - sleeveX + 0.009f) * u, (top + sleeveY + 0.005f) * u)
            lineTo((x - body) * u, (top + 0.022f) * u)
            lineTo((x - body) * u, bottom * u)
            lineTo((x + body) * u, bottom * u)
            lineTo((x + body) * u, (top + 0.022f) * u)
            lineTo((x + sleeveX - 0.009f) * u, (top + sleeveY + 0.005f) * u)
            lineTo((x + sleeveX) * u, (top + sleeveY) * u)
            lineTo((x + 0.021f) * u, (top + 0.002f) * u)
            lineTo((x + 0.01f) * u, top * u)
            quadraticTo(x * u, (top + 0.009f) * u, (x - 0.01f) * u, top * u)
            close()
        }
    }
    val c = argb(Palette.cloth[when (style) {
        0 -> 0
        1 -> 5
        2 -> 7
        3 -> 11
        4 -> 6
        else -> 11
    }])
    when (style) {
        0 -> {
            inked(shirt(c, false, -0.195f), c, pen)
            fxLine(Offset((x - 0.008f) * u, -0.25f * u), Offset((x + 0.008f) * u, -0.25f * u), Color.White.copy(alpha = 0.5f), 0.002f * u)
        }
        1 -> {
            inkedOval(Rect((x - 0.014f) * u, -0.27f * u, (x + 0.014f) * u, -0.254f * u), c.darken(0.12f), pen)
            inked(shirt(c, true, -0.19f), c, pen)
            inkedRound(Rect((x - 0.012f) * u, -0.214f * u, (x + 0.012f) * u, -0.2f * u), 0.003f * u, c.darken(0.08f), pen, shade = false)
            fxLine(Offset((x - 0.004f) * u, -0.258f * u), Offset((x - 0.005f) * u, -0.24f * u), Ink.line, pen.lw * 0.6f)
            fxLine(Offset((x + 0.004f) * u, -0.258f * u), Offset((x + 0.005f) * u, -0.24f * u), Ink.line, pen.lw * 0.6f)
        }
        2 -> {
            val dress = Path().apply {
                moveTo((x - 0.01f) * u, top * u)
                lineTo((x - 0.016f) * u, (top + 0.002f) * u)
                lineTo((x - 0.015f) * u, -0.222f * u)
                lineTo((x - 0.028f) * u, -0.15f * u)
                quadraticTo(x * u, -0.145f * u, (x + 0.028f) * u, -0.15f * u)
                lineTo((x + 0.015f) * u, -0.222f * u)
                lineTo((x + 0.016f) * u, (top + 0.002f) * u)
                lineTo((x + 0.01f) * u, top * u)
                quadraticTo(x * u, (top + 0.009f) * u, (x - 0.01f) * u, top * u)
                close()
            }
            inked(dress, c, pen, outline = false)
            clipPath(dress) {
                for (k in 0 until 8) drawCircle(Color.White.copy(alpha = 0.8f), 0.002f * u, Offset((x - 0.018f + (k % 4) * 0.012f + (k / 4) * 0.006f) * u, (-0.2f + (k / 4) * 0.025f) * u))
            }
            drawPath(dress, Ink.line, style = pen.stroke)
            fxLine(Offset((x - 0.015f) * u, -0.222f * u), Offset((x + 0.015f) * u, -0.222f * u), c.darken(0.3f), 0.003f * u)
        }
        3 -> {
            val s = shirt(c, true, -0.19f)
            inked(s, c, pen, outline = false)
            clipPath(s) {
                for (k in 0 until 6) drawRect(Color.White.copy(alpha = 0.85f), Offset((x - 0.04f) * u, (-0.25f + k * 0.011f) * u), Size(0.08f * u, 0.004f * u))
            }
            drawPath(s, Ink.line, style = pen.stroke)
        }
        4 -> {
            inked(shirt(Color(0xFFF7F4EE), false, -0.22f), Color(0xFFF7F4EE), pen)
            val overalls = Path().apply {
                moveTo((x - 0.012f) * u, -0.245f * u)
                lineTo((x + 0.012f) * u, -0.245f * u)
                lineTo((x + 0.012f) * u, -0.222f * u)
                lineTo((x + 0.021f) * u, -0.218f * u)
                lineTo((x + 0.022f) * u, -0.16f * u)
                lineTo((x + 0.003f) * u, -0.16f * u)
                lineTo(x * u, -0.19f * u)
                lineTo((x - 0.003f) * u, -0.16f * u)
                lineTo((x - 0.022f) * u, -0.16f * u)
                lineTo((x - 0.021f) * u, -0.218f * u)
                lineTo((x - 0.012f) * u, -0.222f * u)
                close()
            }
            inked(overalls, c, pen)
            fxLine(Offset((x - 0.01f) * u, -0.245f * u), Offset((x - 0.014f) * u, -0.262f * u), c, 0.004f * u)
            fxLine(Offset((x + 0.01f) * u, -0.245f * u), Offset((x + 0.014f) * u, -0.262f * u), c, 0.004f * u)
            drawCircle(FxC.yellow, 0.0025f * u, Offset((x - 0.008f) * u, -0.24f * u))
            drawCircle(FxC.yellow, 0.0025f * u, Offset((x + 0.008f) * u, -0.24f * u))
        }
        else -> {
            val s = shirt(c, true, -0.19f)
            inked(s, c, pen, outline = false)
            clipPath(s) {
                drawRect(Color(0xFFF7F4EE), Offset((x - 0.04f) * u, -0.265f * u), Size(0.08f * u, 0.022f * u))
                for (k in 0 until 3) fxStar8(Offset((x - 0.012f + k * 0.012f) * u, -0.254f * u), 0.004f * u, c, 0.0016f * u)
                drawRect(Color(0xFFF7F4EE), Offset((x - 0.04f) * u, -0.2f * u), Size(0.08f * u, 0.005f * u))
                for (k in 0 until 4) drawCircle(Color(0xFFF7F4EE), 0.0014f * u, Offset((x - 0.015f + k * 0.01f) * u, -0.208f * u))
            }
            drawPath(s, Ink.line, style = pen.stroke)
        }
    }
}

// ------------------------------------------------------------------------------------------ lab

/** Liquid colours of the rack's potions, left to right (grow, shrink, rainbow, float, normal). */
private val POTION_COLORS = arrayOf(Color(0xFFFF4D6D), Color(0xFF4F8BFF), Color(0xFFFFD23F), Color(0xFF4FE3F0), Color(0xFFE6F2F7))

internal fun DrawScope.fxPotionRack(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.06f
    val wood = FxC.walnut
    val back = fxFront(u, -0.16f, -0.1f, 0.16f, -0.004f, d)
    inkedRound(back, 0.01f * u, wood.darken(0.2f), pen)
    val moon = Offset(back.left + 0.03f * u, back.top + 0.028f * u)
    drawCircle(FxC.yellow.copy(alpha = 0.8f), 0.01f * u, moon)
    drawCircle(wood.darken(0.2f), 0.009f * u, Offset(moon.x + 0.005f * u, moon.y - 0.003f * u))
    for (k in 0 until 4) twinkle(Offset(back.left + (0.07f + k * 0.07f) * u, back.top + (0.018f + (k % 2) * 0.014f) * u), 0.005f * u, FxC.yellow, 0.7f)
    fxBox(u, -0.168f, -0.1f, -0.158f, 0f, d, wood, pen)
    fxBox(u, -0.165f, -0.014f, 0.165f, 0f, d, wood, pen, rad = 0.002f)
    // Five potions on the shelf, standing halfway back.
    val bz = 0.028f
    val o = q(0f, 0f, bz)
    translate(o.x, o.y) {
        for (i in 0 until 5) {
            val x = -0.16f + (i + 0.5f) * 0.064f
            val col = POTION_COLORS[i]
            fxGlow(p(x, -0.045f), 0.04f * u, col, 0.22f + 0.35f * pen.night)
            val bottle: Path = when (i) {
                0 -> Path().apply {
                    addOval(Rect(p(x, -0.037f), 0.022f * u))
                    addRect(Rect((x - 0.007f) * u, -0.074f * u, (x + 0.007f) * u, -0.055f * u))
                }
                1 -> roundPath(Rect((x - 0.012f) * u, -0.046f * u, (x + 0.012f) * u, -0.014f * u), 0.005f * u).apply {
                    addRect(Rect((x - 0.005f) * u, -0.054f * u, (x + 0.005f) * u, -0.044f * u))
                }
                2 -> roundPath(Rect((x - 0.014f) * u, -0.076f * u, (x + 0.014f) * u, -0.014f * u), 0.006f * u).apply {
                    addRect(Rect((x - 0.005f) * u, -0.086f * u, (x + 0.005f) * u, -0.074f * u))
                }
                3 -> fxPoly(u, x - 0.02f, -0.014f, x + 0.02f, -0.014f, x + 0.006f, -0.058f, x + 0.006f, -0.072f, x - 0.006f, -0.072f, x - 0.006f, -0.058f)
                else -> roundPath(Rect((x - 0.013f) * u, -0.066f * u, (x + 0.013f) * u, -0.014f * u), 0.009f * u).apply {
                    addRect(Rect((x - 0.005f) * u, -0.078f * u, (x + 0.005f) * u, -0.064f * u))
                }
            }
            drawPath(bottle, Color(0x55E8F4FF))
            clipPath(bottle) {
                when (i) {
                    2 -> {
                        val bands = arrayOf(Color(0xFFFF5A4E), Color(0xFFFF9F43), Color(0xFFFFD23F), Color(0xFF3BC46B), Color(0xFF4AB3FF), Color(0xFF8B5CF6))
                        for (k in bands.indices) drawRect(bands[k], p(x - 0.02f, -0.07f + k * 0.0093f), Size(0.04f * u, 0.0095f * u))
                    }
                    4 -> drawRect(col.copy(alpha = 0.6f), p(x - 0.02f, -0.05f), Size(0.04f * u, 0.04f * u))
                    else -> drawRect(col, p(x - 0.025f, if (i == 3) -0.05f else -0.042f), Size(0.05f * u, 0.04f * u))
                }
            }
            drawPath(bottle, Ink.line, style = pen.stroke)
            val corkTop = when (i) {
                0 -> -0.082f
                1 -> -0.06f
                2 -> -0.093f
                3 -> -0.08f
                else -> -0.085f
            }
            inkedRound(Rect((x - 0.006f) * u, corkTop * u, (x + 0.006f) * u, (corkTop + 0.009f) * u), 0.002f * u, FxC.oakDark, pen, shade = false)
            fxLine(p(x - 0.008f, -0.05f), p(x - 0.008f, -0.03f), Color.White.copy(alpha = 0.6f), 0.0025f * u)
            when (i) {
                0 -> {
                    val h = fxHeart(x * u, -0.036f * u, 0.007f * u)
                    drawPath(h, Color.White)
                    drawPath(h, Ink.line, style = pen.thin)
                }
                1 -> drawCircle(Color.White.copy(alpha = 0.8f), 0.003f * u, p(x, -0.03f))
                3 -> for (k in 0 until 3) {
                    val ph = fxFrac(t * 0.6f + k * 0.33f)
                    drawCircle(col.copy(alpha = 1f - ph), (0.003f + ph * 0.002f) * u, p(x + sin(t * 3f + k) * 0.004f, -0.085f - ph * 0.045f), style = Stroke(pen.lw * 0.6f))
                }
                else -> Unit
            }
        }
    }
    fxBox(u, 0.158f, -0.1f, 0.168f, 0f, d, wood, pen)
    capsule(p(-0.16f, -0.035f), p(0.16f, -0.035f), 0.003f * u, FxC.brass, pen)
}

internal fun DrawScope.fxCauldron(f: Fixture, u: Float, pen: Pen, contents: List<Thing>) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val iron = Color(0xFF2E2C38)
    val cz = 0.1f
    val brew = when (contents.size) {
        0 -> Color(0xFF9B6BFF)
        1 -> lerp(Color(0xFF9B6BFF), fxTint(contents[0].type), 0.65f)
        else -> lerp(fxTint(contents[0].type), fxTint(contents[1].type), 0.5f)
    }
    val boil = if (f.on) 1f else 0f
    val g = q(0f, 0f, cz)
    fxGlow(Offset(g.x, g.y - 0.02f * u), 0.14f * u, FxC.flame2, 0.3f + 0.3f * pen.night + 0.2f * boil)
    // Back leg, embers and flames under the pot.
    capsule(q(0f, -0.05f, cz + 0.07f), q(0f, 0f, cz + 0.09f), 0.011f * u, iron, pen)
    for (k in 0 until 6) {
        val c = q(-0.05f + k * 0.02f, -0.006f, cz + (k % 2) * 0.02f - 0.01f)
        val glow = 0.6f + 0.4f * sin(t * 3f + k * 1.3f)
        drawCircle(Ink.line, 0.011f * u, c)
        drawCircle(lerp(Color(0xFF6B1E12), FxC.flame1, glow), 0.01f * u, c)
    }
    val fh = if (f.on) 0.07f else 0.04f
    fxFire(g.x - 0.03f * u, g.y - 0.004f * u, 0.03f * u, fh * u, t, 0f, pen)
    fxFire(g.x + 0.028f * u, g.y - 0.004f * u, 0.028f * u, fh * 0.85f * u, t, 2.1f, pen)
    fxFire(g.x, g.y, 0.036f * u, fh * 1.15f * u, t, 4.2f, pen)
    // The iron belly with the brew inside.
    val c = q(0f, -0.095f, cz)
    val belly = Path().apply {
        moveTo(c.x - 0.095f * u, c.y - 0.06f * u)
        cubicTo(c.x - 0.125f * u, c.y - 0.005f * u, c.x - 0.1f * u, c.y + 0.065f * u, c.x, c.y + 0.068f * u)
        cubicTo(c.x + 0.1f * u, c.y + 0.065f * u, c.x + 0.125f * u, c.y - 0.005f * u, c.x + 0.095f * u, c.y - 0.06f * u)
        close()
    }
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        capsule(Offset(c.x + m * 0.058f * u, c.y + 0.035f * u), Offset(c.x + m * 0.088f * u, c.y + 0.126f * u), 0.013f * u, iron, pen)
    }
    inked(belly, iron, pen)
    clipPath(belly) {
        fxGlow(Offset(c.x - 0.02f * u, c.y - 0.07f * u), 0.08f * u, brew, 0.35f)
        drawLine(Color.White.copy(alpha = 0.12f), Offset(c.x - 0.1f * u, c.y - 0.03f * u), Offset(c.x + 0.1f * u, c.y - 0.03f * u), 0.004f * u)
    }
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        drawCircle(Ink.line, 0.013f * u, Offset(c.x + m * 0.108f * u, c.y - 0.045f * u), style = Stroke(0.006f * u))
        drawCircle(iron.lighten(0.2f), 0.013f * u, Offset(c.x + m * 0.108f * u, c.y - 0.045f * u), style = Stroke(0.0035f * u))
    }
    val rim = q(0f, -0.162f, cz)
    fxFace(fxDisc(rim.x, rim.y, 0.097f * u), iron.lighten(0.14f), pen)
    val wob = if (f.on) sin(t * 9f) * 0.002f * u else 0f
    val surface = fxDisc(rim.x, rim.y + 0.004f * u + wob, 0.082f * u)
    drawPath(surface, brew)
    clipPath(surface) {
        fxGlow(rim, 0.07f * u, Color.White, 0.35f)
        val n = if (f.on) 7 else 4
        for (k in 0 until n) {
            val ph = fxFrac(t * (if (f.on) 1.4f else 0.6f) + k * 0.37f)
            val bc = Offset(rim.x + sin(k * 2.3f) * 0.05f * u, rim.y + cos(k * 1.7f) * 0.012f * u)
            drawCircle(brew.lighten(0.35f), (0.004f + ph * 0.008f) * u, bc)
            drawCircle(Ink.line.copy(alpha = 0.6f), (0.004f + ph * 0.008f) * u, bc, style = pen.thin)
        }
    }
    drawPath(surface, Ink.line, style = pen.thin)
    val lift = if (f.on) 0.14f else 0.08f
    for (k in 0 until (if (f.on) 6 else 3)) {
        val ph = fxFrac(t * (if (f.on) 0.9f else 0.45f) + k * 0.23f)
        val bc = Offset(rim.x + sin(k * 1.9f + t) * 0.04f * u, rim.y - ph * lift * u)
        drawCircle(brew.lighten(0.3f).copy(alpha = 1f - ph), (0.004f + (k % 3) * 0.002f) * u, bc)
        drawCircle(Ink.line.copy(alpha = 0.5f * (1f - ph)), (0.004f + (k % 3) * 0.002f) * u, bc, style = pen.thin)
    }
    fxGlow(Offset(rim.x, rim.y - 0.02f * u), 0.13f * u, brew, 0.3f + 0.35f * pen.night + 0.2f * boil)
}

internal fun DrawScope.fxTelescope(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = FxC.teak
    val brass = FxC.brass
    val apex = q(0f, -0.165f, 0.06f)
    capsule(apex, q(0.01f, 0f, 0.13f), 0.008f * u, wood.darken(0.2f), pen)
    capsule(apex, q(-0.06f, 0f, 0.02f), 0.009f * u, wood, pen)
    capsule(apex, q(0.06f, 0f, 0.02f), 0.009f * u, wood, pen)
    val tray = Path().apply {
        val a = q(-0.03f, -0.07f, 0.04f)
        val b = q(0.03f, -0.07f, 0.04f)
        val c = q(0.005f, -0.07f, 0.1f)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        close()
    }
    drawPath(tray, Ink.line, style = pen.thin)
    inkedRound(Rect(apex.x - 0.014f * u, apex.y - 0.02f * u, apex.x + 0.014f * u, apex.y + 0.006f * u), 0.004f * u, brass.darken(0.15f), pen)
    val pivot = Offset(apex.x, apex.y - 0.02f * u)
    rotate(-32f, pivot) {
        val l = pivot.x - 0.07f * u
        val r = pivot.x + 0.1f * u
        val tube = Path().apply {
            moveTo(l, pivot.y - 0.011f * u)
            lineTo(r, pivot.y - 0.017f * u)
            lineTo(r, pivot.y + 0.017f * u)
            lineTo(l, pivot.y + 0.011f * u)
            close()
        }
        inked(tube, brass, pen)
        for (k in 0 until 3) {
            val x = l + (0.02f + k * 0.07f) * u
            drawRect(brass.darken(0.25f), Offset(x, pivot.y - 0.016f * u), Size(0.006f * u, 0.032f * u))
        }
        inkedRound(Rect(l - 0.016f * u, pivot.y - 0.006f * u, l, pivot.y + 0.006f * u), 0.002f * u, FxC.charcoal, pen, shade = false)
        inkedOval(Rect(r - 0.006f * u, pivot.y - 0.018f * u, r + 0.006f * u, pivot.y + 0.018f * u), Color(0xFF2F4F8F), pen, shade = false)
        shine(Offset(r, pivot.y - 0.007f * u), 0.004f * u, 0.01f * u, 0.7f)
        inkedRound(Rect(l + 0.05f * u, pivot.y - 0.026f * u, l + 0.1f * u, pivot.y - 0.018f * u), 0.003f * u, brass.darken(0.1f), pen, shade = false)
    }
    fxBolt(pivot, 0.005f * u, pen, brass)
    if (pen.night > 0.2f) {
        twinkle(Offset(pivot.x + 0.12f * u, pivot.y - 0.1f * u), 0.008f * u, Color.White, pen.night * (0.6f + 0.4f * sin(pen.t * 3f)))
        twinkle(Offset(pivot.x + 0.07f * u, pivot.y - 0.14f * u), 0.006f * u, FxC.yellow, pen.night * (0.6f + 0.4f * sin(pen.t * 2.3f + 1f)))
    }
}

internal fun DrawScope.fxCrystalBall(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val mode = f.mode.mod(5)
    val cz = 0.04f
    val base = q(0f, 0f, cz)
    fxCyl(base.x, base.y, base.y - 0.018f * u, 0.04f * u, 0.034f * u, FxC.walnut, pen)
    val c = q(0f, -0.078f, cz)
    val r = 0.04f * u
    val tint = when (mode) {
        0 -> Color(0xFFD9A6FF)
        1 -> Color(0xFF8FB8FF)
        2 -> Color(0xFFFF8FC0)
        3 -> FxC.auroraGreen
        else -> FxC.yellow
    }
    fxGlow(c, 0.11f * u, tint, 0.3f + 0.45f * pen.night + 0.3f * f.anim)
    for (s in -1..1) capsule(Offset(base.x + s * 0.026f * u, base.y - 0.016f * u), Offset(c.x + s * 0.034f * u, c.y + 0.022f * u - (if (s == 0) 0.01f * u else 0f)), 0.006f * u, FxC.brass, pen)
    val ball = ovalPath(Rect(c, r))
    drawCircle(safeRadialGradient(0f to Color(0xFFF4ECFF), 1f to tint.darken(0.35f), center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.4f), r, c)
    clipPath(ball) {
        when (mode) {
            0 -> for (k in 0 until 3) {
                drawArc(Color.White.copy(alpha = 0.6f), t * 60f + k * 120f, 110f, false, Offset(c.x - r * (0.8f - k * 0.2f), c.y - r * (0.8f - k * 0.2f)), Size(r * (1.6f - k * 0.4f), r * (1.6f - k * 0.4f)), style = Stroke(0.006f * u, cap = StrokeCap.Round))
            }
            1 -> {
                drawCircle(Color(0xFF1C2A5A).copy(alpha = 0.85f), r, c)
                drawCircle(Color(0xFFFFF4C8), r * 0.3f, Offset(c.x + r * 0.2f, c.y - r * 0.2f))
                drawCircle(Color(0xFF1C2A5A), r * 0.27f, Offset(c.x + r * 0.33f, c.y - r * 0.3f))
                for (k in 0 until 4) twinkle(Offset(c.x - r * 0.5f + k * r * 0.3f, c.y + r * (0.2f - (k % 2) * 0.5f)), r * 0.12f, Color.White, 0.6f + 0.4f * sin(t * 3f + k))
            }
            2 -> {
                val s = r * 0.42f * (1f + 0.1f * sin(t * 5f))
                val h = fxHeart(c.x, c.y, s)
                drawPath(h, Color(0xFFFF5A8A))
                drawPath(h, Ink.line, style = pen.thin)
            }
            3 -> {
                drawCircle(Color(0xFF0E1A3A).copy(alpha = 0.85f), r, c)
                for (band in 0 until 2) {
                    val aur = Path()
                    for (k in 0..6) {
                        val fx = k / 6f
                        val y = c.y - r * (0.25f - band * 0.25f) + sin(fx * 6f + t * (1.2f + band * 0.5f)) * r * 0.15f
                        if (k == 0) aur.moveTo(c.x - r + fx * 2f * r, y) else aur.lineTo(c.x - r + fx * 2f * r, y)
                    }
                    drawPath(aur, (if (band == 0) FxC.auroraGreen else FxC.auroraPink).copy(alpha = 0.8f), style = Stroke(0.006f * u, cap = StrokeCap.Round))
                }
                drawPath(fxPoly(1f, c.x - r, c.y + r, c.x - r * 0.3f, c.y + r * 0.3f, c.x + r * 0.1f, c.y + r * 0.6f, c.x + r * 0.6f, c.y + r * 0.2f, c.x + r, c.y + r), Color(0xFF0A0F24))
            }
            else -> rotate(t * 30f, c) {
                val star = starPath(c, r * 0.5f, r * 0.22f)
                drawPath(star, Color(0xFFFFD84A))
                drawPath(star, Ink.line, style = pen.thin)
            }
        }
    }
    drawCircle(Ink.line, r, c, style = pen.stroke)
    shine(Offset(c.x - r * 0.42f, c.y - r * 0.45f), r * 0.35f, r * 0.22f, 0.85f)
    drawCircle(Color.White.copy(alpha = 0.7f), r * 0.07f, Offset(c.x + r * 0.45f, c.y + r * 0.35f))
}

private val SPELL_PAGES = arrayOf(
    ThingType.APPLE to ThingType.FLOWER,
    ThingType.MUSHROOM to ThingType.ROCK,
    ThingType.FEATHER to ThingType.BALLOON,
    ThingType.FLOWER to ThingType.GEM,
    ThingType.TEDDY to ThingType.WAND,
    ThingType.EGG to ThingType.GEM,
)

/** Draws a thing as a little picture centred on [c], fitted into a [box]-pixel square. */
private fun DrawScope.fxPictogram(type: ThingType, c: Offset, box: Float, pen: Pen) {
    val s = min(box / type.w, box / type.h)
    val w = type.w * s
    val h = type.h * s
    val small = Pen(pen.lw * 0.6f, pen.t, pen.night, pen.weather, pen.rainbow)
    translate(c.x, c.y + h / 2f) { drawThing(type, 0, 0, w, h, small) }
}

internal fun DrawScope.fxSpellbook(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val wood = FxC.walnut
    val cz = 0.06f
    val b = q(0f, 0f, cz)
    fxCyl(b.x, b.y, b.y - 0.012f * u, 0.05f * u, 0.046f * u, wood.darken(0.1f), pen)
    val col = q(0f, -0.095f, cz)
    fxCyl(b.x, b.y - 0.012f * u, col.y, 0.013f * u, 0.011f * u, wood, pen, cap = false)
    val knob = Offset(col.x, (b.y + col.y) / 2f)
    inkedOval(Rect(knob.x - 0.018f * u, knob.y - 0.008f * u, knob.x + 0.018f * u, knob.y + 0.008f * u), wood, pen)
    // The sloping book rest and the open book on it.
    val fz = 0.015f
    val bz = 0.105f
    fun slope(x: Float, k: Float): Offset = q(x, -0.098f - 0.035f * k, fz + (bz - fz) * k)
    val r0 = slope(-0.078f, 0f)
    val r1 = slope(0.078f, 0f)
    val r2 = slope(0.078f, 1f)
    val r3 = slope(-0.078f, 1f)
    fxFace(fxQuad(r0.x, r0.y, r1.x, r1.y, r2.x, r2.y, r3.x, r3.y, 0.004f * u), wood.lighten(0.1f), pen)
    val c0 = slope(-0.084f, -0.04f)
    val c1 = slope(0.084f, -0.04f)
    val c2 = slope(0.084f, 1.04f)
    val c3 = slope(-0.084f, 1.04f)
    fxFace(fxQuad(c0.x, c0.y + 0.004f * u, c1.x, c1.y + 0.004f * u, c2.x, c2.y + 0.004f * u, c3.x, c3.y + 0.004f * u, 0.004f * u), Color(0xFF7A2B3A), pen)
    val paper = Color(0xFFFFF4DC)
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        val a = slope(0f, 0.02f)
        val e = slope(0.074f * m, 0.02f)
        val g = slope(0.074f * m, 0.98f)
        val i = slope(0f, 0.98f)
        val page = Path().apply {
            moveTo(a.x, a.y)
            quadraticTo((a.x + e.x) / 2f, (a.y + e.y) / 2f - 0.006f * u, e.x, e.y)
            lineTo(g.x, g.y)
            quadraticTo((g.x + i.x) / 2f, (g.y + i.y) / 2f - 0.006f * u, i.x, i.y)
            close()
        }
        fxFace(page, paper, pen)
    }
    fxLine(slope(0f, 0.02f), slope(0f, 0.98f), paper.darken(0.3f), pen.lw * 0.8f)
    // The recipe hint: two pictures and an arrow to a sparkle.
    val (first, second) = SPELL_PAGES[f.mode.mod(SPELL_PAGES.size)]
    val box = 0.03f * u
    fxPictogram(first, slope(-0.037f, 0.55f), box, pen)
    fxPictogram(second, slope(0.037f, 0.55f), box, pen)
    val plus = slope(0f, 0.55f)
    drawLine(FxC.red, Offset(plus.x - 0.005f * u, plus.y), Offset(plus.x + 0.005f * u, plus.y), 0.0025f * u, StrokeCap.Round)
    drawLine(FxC.red, Offset(plus.x, plus.y - 0.005f * u), Offset(plus.x, plus.y + 0.005f * u), 0.0025f * u, StrokeCap.Round)
    val a0 = slope(0.012f, 0.14f)
    val a1 = slope(0.052f, 0.14f)
    fxLine(a0, a1, Ink.line, pen.lw)
    fxLine(a1, Offset(a1.x - 0.006f * u, a1.y - 0.004f * u), Ink.line, pen.lw)
    fxLine(a1, Offset(a1.x - 0.005f * u, a1.y + 0.004f * u), Ink.line, pen.lw)
    twinkle(Offset(a1.x + 0.009f * u, a1.y), 0.007f * u * (0.8f + 0.2f * sin(t * 4f)), FxC.yellow)
    val rib = slope(0f, 0f)
    fxLine(rib, Offset(rib.x + 0.002f * u, rib.y + 0.03f * u), FxC.red, 0.004f * u)
    // Turning the page.
    if (f.anim > 0f) {
        val k = 1f - f.anim
        val ex = 0.074f * cos(k * FX_PI)
        val lift = sin(k * FX_PI) * 0.025f * u
        val a = slope(0f, 0.02f)
        val e = slope(ex, 0.02f)
        val g = slope(ex, 0.98f)
        val i = slope(0f, 0.98f)
        val page = Path().apply {
            moveTo(a.x, a.y)
            quadraticTo((a.x + e.x) / 2f, (a.y + e.y) / 2f - lift * 1.5f, e.x, e.y - lift)
            lineTo(g.x, g.y - lift)
            quadraticTo((g.x + i.x) / 2f, (g.y + i.y) / 2f - lift * 1.5f, i.x, i.y)
            close()
        }
        fxFace(page, paper.darken(0.05f), pen)
    }
    for (k in 0 until 3) {
        val ph = fxFrac(t * 0.35f + k * 0.33f)
        val c = slope(-0.04f + k * 0.04f, 0.5f)
        twinkle(Offset(c.x + sin(t + k) * 0.01f * u, c.y - (0.02f + ph * 0.07f) * u), 0.006f * u * (1f - ph), if (k == 1) FxC.yellow else FxC.lilac, 1f - ph)
    }
}
