package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.PlaceId
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Beach, forest and mountain, in oblique 3D.

// ------------------------------------------------------------------------------------------ beach

private const val PIER_D = 0.16f

/** The sea's surface under the pier, relative to the pier's base on the sea bed. */
private const val PIER_WATER = -0.19f

internal fun DrawScope.fxPier(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = PIER_D
    val t = pen.t
    val wood = Color(0xFFBCA488)
    val post = Color(0xFF8A7560)
    val rope = Color(0xFFD8B878)
    val xs = floatArrayOf(-0.47f, -0.3f, -0.13f, 0.04f, 0.21f, 0.38f, 0.47f)
    fun piling(x: Float, z: Float) {
        val bottom = if (x < -0.15f) -0.09f else 0f
        val b = q(x, bottom, z)
        val top = q(x, -0.24f, z)
        fxCyl(b.x, b.y, top.y, 0.014f * u, 0.014f * u, post, pen, cap = false, bottom = false)
        if (bottom == 0f) {
            val w = q(x, PIER_WATER, z)
            fxCyl(b.x, b.y, w.y, 0.0142f * u, 0.0142f * u, lerp(post, Color(0xFF3E6B4A), 0.45f), pen, cap = false, bottom = false)
            val weed = Path().apply {
                moveTo(w.x - 0.008f * u, w.y)
                quadraticTo(w.x - 0.014f * u + sin(t * 1.6f + x * 9f) * 0.004f * u, w.y + 0.05f * u, w.x - 0.006f * u, w.y + 0.1f * u)
            }
            drawPath(weed, FxC.spruceLight, style = Stroke(0.004f * u, cap = StrokeCap.Round))
            for (k in 0 until 3) drawCircle(Color.White.copy(alpha = 0.7f), 0.0025f * u, Offset(w.x + (k - 1) * 0.006f * u, w.y + (0.012f + k * 0.01f) * u))
        }
    }
    for (x in xs) piling(x, d - 0.02f)
    for (i in 2 until xs.size - 1 step 2) {
        capsule(q(xs[i], -0.23f, d - 0.02f), q(xs[i + 1], -0.08f, d - 0.02f), 0.007f * u, post.darken(0.15f), pen)
        capsule(q(xs[i + 1], -0.23f, d - 0.02f), q(xs[i], -0.08f, d - 0.02f), 0.007f * u, post.darken(0.15f), pen)
    }
    // The steps up from the sand at the left end.
    fxBox(u, -0.625f, -0.145f, -0.56f, -0.133f, d * 0.7f, wood, pen, z = 0.02f)
    fxBox(u, -0.62f, -0.133f, -0.61f, -0.09f, 0.01f, post, pen, z = 0.02f)
    fxBox(u, -0.565f, -0.2f, -0.5f, -0.188f, d * 0.7f, wood, pen, z = 0.02f)
    fxBox(u, -0.56f, -0.188f, -0.55f, -0.09f, 0.01f, post, pen, z = 0.02f)
    // The deck.
    fxBox(u, -0.5f, -0.26f, 0.5f, -0.24f, d, wood, pen, rad = 0.003f, top = wood.lighten(0.14f))
    var x = -0.47f
    while (x < 0.5f) {
        fxLine(q(x, -0.26f, 0f), q(x, -0.26f, d), wood.darken(0.3f).copy(alpha = 0.5f), pen.lw * 0.5f)
        fxNail(p(x - 0.008f, -0.25f), 0.0018f * u)
        fxNail(p(x + 0.008f, -0.25f), 0.0018f * u)
        x += 0.045f
    }
    fxGrain(Rect(-0.5f * u, -0.259f * u, 0.5f * u, -0.241f * u), wood, pen, 1)
    // Lifebuoy on a post at the back.
    fxBox(u, 0.03f, -0.34f, 0.046f, -0.26f, 0.016f, post, pen, z = d - 0.03f)
    val ring = q(0.038f, -0.305f, d - 0.034f)
    drawCircle(Ink.line, 0.022f * u, ring, style = Stroke(0.011f * u + pen.lw * 2f))
    drawCircle(FxC.red, 0.022f * u, ring, style = Stroke(0.011f * u))
    for (k in 0 until 4) drawArc(Color.White, k * 90f + 20f, 30f, false, Offset(ring.x - 0.022f * u, ring.y - 0.022f * u), Size(0.044f * u, 0.044f * u), style = Stroke(0.011f * u))
    // Bollards with a coil of rope, and a rope swag along the edge.
    for (bx0 in floatArrayOf(-0.34f, 0.3f)) {
        val b = q(bx0, -0.26f, 0.03f)
        val top = q(bx0, -0.296f, 0.03f)
        fxCyl(b.x, b.y, top.y, 0.013f * u, 0.013f * u, FxC.charcoal, pen)
        fxFace(fxDisc(top.x, top.y, 0.017f * u), FxC.charcoal.lighten(0.15f), pen)
    }
    val coil = q(-0.34f, -0.262f, 0.03f)
    for (k in 0 until 2) drawPath(fxDisc(coil.x, coil.y - k * 0.005f * u, (0.022f - k * 0.003f) * u), rope, style = Stroke(0.004f * u))
    val swag = Path().apply {
        val a = q(-0.34f, -0.276f, 0.03f)
        val c = q(-0.02f, -0.205f, -0.004f)
        val e = q(0.3f, -0.276f, 0.03f)
        moveTo(a.x, a.y)
        quadraticTo(c.x, c.y, e.x, e.y)
    }
    drawPath(swag, Ink.line, style = Stroke(0.005f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(swag, rope, style = Stroke(0.005f * u, cap = StrokeCap.Round))
    // A lobster pot and its buoy.
    fxBox(u, -0.255f, -0.302f, -0.185f, -0.26f, 0.07f, Color(0xFFD9C29A), pen, rad = 0.018f, z = 0.04f)
    val pot = fxFront(u, -0.255f, -0.302f, -0.185f, -0.26f, 0.04f)
    clipPath(roundPath(pot, 0.018f * u)) {
        var k = pot.left - pot.height
        while (k < pot.right) {
            drawLine(Ink.line.copy(alpha = 0.35f), Offset(k, pot.bottom), Offset(k + pot.height, pot.top), pen.lw * 0.5f)
            drawLine(Ink.line.copy(alpha = 0.35f), Offset(k, pot.top), Offset(k + pot.height, pot.bottom), pen.lw * 0.5f)
            k += 0.012f * u
        }
    }
    drawOval(Ink.line.copy(alpha = 0.7f), Offset(pot.center.x - 0.008f * u, pot.center.y - 0.004f * u), Size(0.016f * u, 0.012f * u))
    val buoy = q(-0.165f, -0.271f, 0.02f)
    inkedCircle(buoy, 0.011f * u, Color(0xFFFF8A3D), pen)
    drawLine(Color.White, Offset(buoy.x - 0.01f * u, buoy.y), Offset(buoy.x + 0.01f * u, buoy.y), 0.004f * u)
    // Front pilings and the swimming ladder at the sea end.
    for (px in xs) piling(px, 0.015f)
    for (lx in floatArrayOf(0.455f, 0.49f)) {
        val a = q(lx, -0.3f, -0.008f)
        val b = q(lx, -0.1f, -0.008f)
        capsule(a, b, 0.005f * u, FxC.steel, pen)
    }
    for (k in 0 until 5) fxLine(q(0.455f, -0.24f + k * 0.03f, -0.008f), q(0.49f, -0.24f + k * 0.03f, -0.008f), FxC.steel.darken(0.25f), 0.004f * u)
}

internal fun DrawScope.fxFishingSpot(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.07f
    val tip = q(0.07f, -0.16f, 0.03f)
    capsule(q(-0.018f, -0.03f, 0.045f), fxMix(q(-0.018f, -0.03f, 0.045f), tip, 0.45f), 0.006f * u, Color(0xFFC89A50), pen)
    capsule(fxMix(q(-0.018f, -0.03f, 0.045f), tip, 0.45f), tip, 0.0035f * u, Color(0xFFD8B46A), pen)
    val reel = fxMix(q(-0.018f, -0.03f, 0.045f), tip, 0.18f)
    inkedCircle(reel, 0.009f * u, FxC.steel, pen)
    drawLine(Ink.line, reel, Offset(reel.x + 0.008f * u, reel.y + 0.004f * u), pen.lw)
    // The fish box.
    fxBox(u, -0.042f, -0.045f, 0.042f, 0f, d, Color(0xFF4F86C6), pen, rad = 0.004f)
    for (k in 1..2) fxLine(p(-0.04f, -0.045f + k * 0.015f), p(0.04f, -0.045f + k * 0.015f), Color(0xFF3A6AA8), pen.lw * 0.6f)
    drawOval(Color(0xFF26467A), p(-0.034f, -0.038f), Size(0.012f * u, 0.006f * u))
    drawOval(Color(0xFF26467A), p(0.022f, -0.038f), Size(0.012f * u, 0.006f * u))
    val tail = q(0.02f, -0.045f, 0.035f)
    inked(fxPoly(1f, tail.x, tail.y, tail.x + 0.014f * u, tail.y - 0.01f * u, tail.x + 0.014f * u, tail.y + 0.004f * u), FxC.steel, pen, shade = false)
    // The line: hanging when idle, cast out to a bobbing float when fishing.
    if (!f.on) {
        val end = Offset(tip.x, tip.y + 0.05f * u)
        drawLine(Ink.line.copy(alpha = 0.6f), tip, end, pen.lw * 0.5f)
        fxBobber(end, 0.007f * u, pen)
        return
    }
    val prog = ((1.5f - f.timer) / 0.3f).coerceIn(0f, 1f)
    val water = q(0.25f, 0.07f, -0.03f)
    val dip = if (f.timer < 0.35f) 0.01f * u else 0f
    val landed = Offset(water.x, water.y + sin(t * 3f) * 0.003f * u + dip)
    val bob = Offset(tip.x + (landed.x - tip.x) * prog, tip.y + (landed.y - tip.y) * prog - sin(prog * FX_PI) * 0.08f * u)
    val line = Path().apply {
        moveTo(tip.x, tip.y)
        quadraticTo((tip.x + bob.x) / 2f, (tip.y + bob.y) / 2f + 0.03f * u * prog, bob.x, bob.y)
    }
    drawPath(line, Ink.line.copy(alpha = 0.6f), style = Stroke(pen.lw * 0.5f))
    if (prog >= 1f) {
        for (k in 0 until 2) {
            val ph = fxFrac(t * 0.6f + k * 0.5f)
            val r = (0.012f + ph * 0.03f) * u
            drawPath(fxDisc2(landed.x, landed.y + 0.004f * u, r, r * 0.6f), Color.White.copy(alpha = 0.6f * (1f - ph)), style = Stroke(pen.lw * 0.6f))
        }
    }
    fxBobber(bob, 0.008f * u, pen)
}

private fun DrawScope.fxBobber(c: Offset, r: Float, pen: Pen) {
    drawCircle(Color.White, r, c)
    drawArc(FxC.red, 180f, 180f, true, Offset(c.x - r, c.y - r), Size(r * 2f, r * 2f))
    drawCircle(Ink.line, r, c, style = pen.thin)
    drawLine(Ink.line, Offset(c.x, c.y - r), Offset(c.x, c.y - r * 1.8f), pen.lw * 0.6f)
}

// A small rowboat, modelled properly: gunwales, a keel and a round hull between them.
private const val BOAT_L = 0.19f
private const val BOAT_B = 0.065f
private const val BOAT_ZC = 0.07f
private const val BOAT_N = 12

private fun fxBoatSheer(x: Float): Float {
    val k = x / BOAT_L
    return -0.08f - 0.065f * k * k * k * k
}

private fun fxBoatHalf(x: Float): Float {
    val k = x / BOAT_L
    return BOAT_B * (1f - k * k)
}

private fun fxBoatKeel(x: Float): Float {
    val k = x / BOAT_L
    return -0.004f - 0.13f * k * k * k * k * k * k
}

private fun fxBoatX(i: Int): Float = -BOAT_L + 2f * BOAT_L * i / BOAT_N

internal fun DrawScope.fxBoat(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val inside = Path()
    for (i in 0..BOAT_N) {
        val x = fxBoatX(i)
        val pt = q(x, fxBoatSheer(x), BOAT_ZC - fxBoatHalf(x))
        if (i == 0) inside.moveTo(pt.x, pt.y) else inside.lineTo(pt.x, pt.y)
    }
    for (i in BOAT_N downTo 0) {
        val x = fxBoatX(i)
        val pt = q(x, fxBoatSheer(x), BOAT_ZC + fxBoatHalf(x))
        inside.lineTo(pt.x, pt.y)
    }
    inside.close()
    val wood = Color(0xFFD9B384)
    drawPath(inside, wood)
    clipPath(inside) {
        for (k in 1..3) {
            val plank = Path()
            for (i in 0..BOAT_N) {
                val x = fxBoatX(i)
                val pt = q(x, fxBoatSheer(x) + 0.014f * k, BOAT_ZC + fxBoatHalf(x) * (1f - 0.22f * k))
                if (i == 0) plank.moveTo(pt.x, pt.y) else plank.lineTo(pt.x, pt.y)
            }
            drawPath(plank, wood.darken(0.25f), style = pen.thin)
        }
        for (sx in floatArrayOf(-0.08f, 0.08f)) {
            fxFace(fxFlat(u, sx - 0.028f, sx + 0.028f, -0.052f, BOAT_ZC - fxBoatHalf(sx) + 0.004f, BOAT_ZC + fxBoatHalf(sx) - 0.004f), FxC.oak, pen)
        }
        capsule(q(-0.15f, -0.075f, BOAT_ZC + 0.035f), q(0.16f, -0.088f, BOAT_ZC + 0.03f), 0.006f * u, FxC.oak, pen)
        val blade = q(-0.135f, -0.074f, BOAT_ZC + 0.035f)
        inkedOval(Rect(blade.x - 0.022f * u, blade.y - 0.007f * u, blade.x + 0.022f * u, blade.y + 0.007f * u), FxC.oak, pen, shade = false)
    }
    drawPath(inside, Ink.line, style = pen.stroke)
    val far = Path()
    for (i in 0..BOAT_N) {
        val x = fxBoatX(i)
        val pt = q(x, fxBoatSheer(x), BOAT_ZC + fxBoatHalf(x))
        if (i == 0) far.moveTo(pt.x, pt.y) else far.lineTo(pt.x, pt.y)
    }
    drawPath(far, Ink.line, style = Stroke(0.008f * u + pen.lw * 2f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawPath(far, FxC.oakDark, style = Stroke(0.008f * u, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    for (s in 0..1) {
        val x = if (s == 0) -BOAT_L else BOAT_L
        val a = q(x, fxBoatSheer(x), BOAT_ZC)
        capsule(a, Offset(a.x + (if (s == 0) -0.006f else 0.006f) * u, a.y - 0.016f * u), 0.008f * u, FxC.oakDark, pen)
    }
}

/** The near side of the hull, in front of whoever sits in the boat. */
internal fun DrawScope.fxBoatFront(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val top = Array(BOAT_N + 1) { i ->
        val x = fxBoatX(i)
        q(x, fxBoatSheer(x), BOAT_ZC - fxBoatHalf(x))
    }
    val bottom = Array(BOAT_N + 1) { i ->
        val x = fxBoatX(i)
        q(x, fxBoatSheer(x) + (fxBoatKeel(x) - fxBoatSheer(x)) * 0.85f, BOAT_ZC - fxBoatHalf(x) * 0.72f)
    }
    val hull = Path()
    for (i in 0..BOAT_N) if (i == 0) hull.moveTo(top[i].x, top[i].y) else hull.lineTo(top[i].x, top[i].y)
    for (i in BOAT_N downTo 0) hull.lineTo(bottom[i].x, bottom[i].y)
    hull.close()
    val white = Color(0xFFF7F4EE)
    inked(hull, white, pen, outline = false)
    fun band(f0: Float, f1: Float): Path {
        val path = Path()
        for (i in 0..BOAT_N) {
            val pt = fxMix(top[i], bottom[i], f0)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        for (i in BOAT_N downTo 0) {
            val pt = fxMix(top[i], bottom[i], f1)
            path.lineTo(pt.x, pt.y)
        }
        path.close()
        return path
    }
    clipPath(hull) {
        drawPath(band(0f, 0.3f), FxC.red)
        drawPath(band(0.82f, 1.2f), FxC.fjord)
        for (fr in floatArrayOf(0.3f, 0.54f, 0.78f)) {
            val seam = Path()
            for (i in 0..BOAT_N) {
                val pt = fxMix(top[i], bottom[i], fr)
                if (i == 0) seam.moveTo(pt.x, pt.y) else seam.lineTo(pt.x, pt.y)
            }
            drawPath(seam, Ink.line.copy(alpha = 0.45f), style = pen.thin)
            translate(0f, 0.003f * u) { drawPath(seam, Color.White.copy(alpha = 0.35f), style = pen.thin) }
        }
        for (i in 1 until BOAT_N step 2) fxNail(fxMix(top[i], bottom[i], 0.42f), 0.0016f * u)
    }
    drawPath(hull, Ink.line, style = pen.stroke)
    val rail = Path()
    for (i in 0..BOAT_N) if (i == 0) rail.moveTo(top[i].x, top[i].y) else rail.lineTo(top[i].x, top[i].y)
    drawPath(rail, Ink.line, style = Stroke(0.008f * u + pen.lw * 2f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawPath(rail, FxC.oak, style = Stroke(0.008f * u, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    for (ox in floatArrayOf(-0.02f, 0.13f)) {
        val a = q(ox, fxBoatSheer(ox), BOAT_ZC - fxBoatHalf(ox))
        capsule(a, Offset(a.x, a.y - 0.012f * u), 0.004f * u, FxC.steel, pen)
    }
    val fend = fxMix(top[8], bottom[8], 0.05f)
    fxLine(fend, Offset(fend.x, fend.y + 0.01f * u), Ink.line, pen.lw * 0.6f)
    inkedRound(Rect(fend.x - 0.006f * u, fend.y + 0.01f * u, fend.x + 0.006f * u, fend.y + 0.034f * u), 0.006f * u, Color.White, pen)
}

internal fun DrawScope.fxUmbrella(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val cz = 0.08f
    val base = q(0f, 0f, cz)
    drawPath(fxDisc2(base.x, base.y, 0.035f * u, 0.03f * u), FxC.sand.darken(0.12f))
    val apex = q(0f, -0.378f, cz)
    capsule(base, apex, 0.007f * u, FxC.paint, pen)
    val joint = q(0f, -0.2f, cz)
    inkedRound(Rect(joint.x - 0.006f * u, joint.y - 0.008f * u, joint.x + 0.006f * u, joint.y + 0.008f * u), 0.002f * u, FxC.steel, pen, shade = false)
    // Eight striped panels, drawn from the back to the front so the near ones cover the far ones.
    val rim = q(0f, -0.3f, cz)
    val r = 0.13f * u
    val n = 8
    val step = 2f * FX_PI / n
    val order = IntArray(n) { it }
    val depth = FloatArray(n) { sin(f.angle + (it + 0.5f) * step) }
    for (i in 1 until n) {
        var j = i
        while (j > 0 && depth[order[j]] > depth[order[j - 1]]) {
            val tmp = order[j]
            order[j] = order[j - 1]
            order[j - 1] = tmp
            j--
        }
    }
    for (k in order) {
        val a0 = f.angle + k * step
        val a1 = a0 + step
        val mid = (a0 + a1) / 2f
        val p0 = fxRim(rim.x, rim.y, r, a0)
        val p1 = fxRim(rim.x, rim.y, r, a1)
        val pm = fxRim(rim.x, rim.y, r * 0.92f, mid)
        val gore = Path().apply {
            moveTo(apex.x, apex.y)
            lineTo(p0.x, p0.y)
            quadraticTo(pm.x, pm.y + 0.016f * u, p1.x, p1.y)
            close()
        }
        val base0 = if (k % 2 == 0) FxC.red else FxC.paint
        val light = (cos(mid) * 0.12f + 0.04f).coerceAtLeast(0f)
        fxFace(gore, base0.darken(light), pen)
    }
    inkedCircle(apex, 0.007f * u, FxC.paint, pen)
}

internal fun DrawScope.fxLounger(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.12f
    val wood = FxC.teak
    val stripe = FxC.fjord
    for (x in floatArrayOf(-0.11f, 0.13f)) fxBox(u, x - 0.006f, -0.05f, x + 0.006f, 0f, 0.012f, wood.darken(0.15f), pen, z = d - 0.015f)
    capsule(q(-0.14f, -0.056f, d * 0.5f), q(-0.155f, -0.118f, d * 0.5f), 0.006f * u, wood.darken(0.2f), pen)
    fxBox(u, -0.125f, -0.058f, 0.15f, -0.046f, d, wood, pen, rad = 0.003f)
    // The raised back at the head end, and the flat cushion.
    val h0 = q(-0.12f, -0.07f, 0f)
    val h1 = q(-0.165f, -0.14f, 0f)
    val h2 = q(-0.165f, -0.14f, d)
    val h3 = q(-0.12f, -0.07f, d)
    fxFace(fxQuad(h0.x, h0.y, h1.x, h1.y, h2.x, h2.y, h3.x, h3.y, 0.006f * u), FxC.paint, pen)
    for (k in 1..3) fxLine(fxMix(h0, h1, k * 0.25f), fxMix(h3, h2, k * 0.25f), stripe, 0.006f * u)
    fxBox(u, -0.12f, -0.07f, 0.15f, -0.057f, d - 0.008f, FxC.paint, pen, rad = 0.005f)
    for (k in 0 until 7) {
        val x = -0.105f + k * 0.04f
        drawPath(fxFlat(u, x, x + 0.016f, -0.0702f, 0.003f, d - 0.012f), stripe.copy(alpha = 0.85f))
        drawRect(stripe.copy(alpha = 0.85f), Offset(x * u, -0.069f * u), Size(0.016f * u, 0.011f * u))
    }
    capsule(q(-0.162f, -0.146f, 0.012f), q(-0.162f, -0.146f, d - 0.012f), 0.014f * u, FxC.mustard, pen)
    for (x in floatArrayOf(-0.11f, 0.13f)) fxBox(u, x - 0.006f, -0.05f, x + 0.006f, 0f, 0.012f, wood, pen)
}

internal fun DrawScope.fxSandcastle(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val sand = FxC.sand
    val mode = f.mode.coerceIn(0, 4)
    val cz = 0.06f
    val c = q(0f, 0f, cz)
    if (mode == 0) {
        fxFace(fxDisc2(c.x, c.y, 0.075f * u, 0.05f * u), sand.darken(0.12f), pen)
        val mound = blobPath(c.x - 0.07f * u, c.y + 0.012f * u, c.x - 0.035f * u, c.y - 0.03f * u, c.x + 0.005f * u, c.y - 0.048f * u, c.x + 0.045f * u, c.y - 0.03f * u, c.x + 0.072f * u, c.y + 0.006f * u, c.x, c.y + 0.02f * u)
        inked(mound, sand, pen)
        for (k in 0 until 6) drawCircle(sand.darken(0.3f), 0.0015f * u, Offset(c.x + (k - 2.5f) * 0.018f * u, c.y - (0.01f + (k % 3) * 0.008f) * u))
        return
    }
    fxCyl(c.x, c.y, c.y - 0.012f * u, 0.082f * u, 0.078f * u, sand.darken(0.05f), pen)
    fun tower(x: Float, z: Float, h: Float, rb: Float, rt: Float, crenel: Boolean) {
        val b = q(x, -0.012f, z)
        val tp = q(x, -0.012f - h, z)
        fxCyl(b.x, b.y, tp.y, rb * u, rt * u, sand, pen)
        for (k in 1..2) {
            val y = b.y + (tp.y - b.y) * k / 3f
            val rr = (rb + (rt - rb) * k / 3f) * u
            fxLine(Offset(b.x - rr * 1.05f, y + rr * 0.15f), Offset(b.x + rr * 1.05f, y - rr * 0.15f), sand.darken(0.2f), pen.lw * 0.6f)
        }
        if (crenel) {
            for (k in 0 until 4) {
                val a = FX_PI + (k + 0.5f) * FX_PI / 4f
                val pt = fxRim(tp.x, tp.y, rt * u * 0.8f, a)
                inkedRound(Rect(pt.x - 0.004f * u, pt.y - 0.009f * u, pt.x + 0.004f * u, pt.y + 0.001f * u), 0.001f * u, sand, pen, shade = false)
            }
        }
    }
    if (mode >= 2) {
        fxBox(u, -0.05f, -0.044f, 0.05f, -0.012f, 0.03f, sand, pen, rad = 0.003f, z = cz + 0.03f)
        if (mode >= 3) {
            for (k in 0 until 5) fxBox(u, -0.045f + k * 0.022f, -0.052f, -0.037f + k * 0.022f, -0.044f, 0.03f, sand, pen, z = cz + 0.03f)
        }
        tower(-0.052f, cz + 0.045f, 0.05f, 0.022f, 0.017f, mode >= 3)
        tower(0.052f, cz + 0.045f, 0.05f, 0.022f, 0.017f, mode >= 3)
    }
    tower(0f, cz, 0.068f, 0.03f, 0.024f, mode >= 3)
    if (mode >= 3) {
        val g = q(0f, -0.012f, cz - 0.03f)
        val gate = Path().apply {
            moveTo(g.x - 0.01f * u, g.y)
            lineTo(g.x - 0.01f * u, g.y - 0.016f * u)
            quadraticTo(g.x, g.y - 0.028f * u, g.x + 0.01f * u, g.y - 0.016f * u)
            lineTo(g.x + 0.01f * u, g.y)
            close()
        }
        drawPath(gate, Color(0xFF5A4028))
        drawPath(gate, Ink.line, style = pen.thin)
        drawRoundRect(Color(0xFF5A4028), Offset(g.x - 0.0025f * u, g.y - 0.055f * u), Size(0.005f * u, 0.012f * u), androidx.compose.ui.geometry.CornerRadius(0.002f * u))
    }
    if (mode == 4) {
        val tp = q(0f, -0.08f, cz)
        val peak = q(0f, -0.11f, cz)
        fxCyl(tp.x, tp.y, peak.y, 0.026f * u, 0.002f * u, sand.darken(0.06f), pen)
        val pole = Offset(peak.x, peak.y - 0.035f * u)
        fxLine(peak, pole, Ink.line, pen.lw)
        val wave = sin(t * 6f) * 0.004f * u
        val flag = Path().apply {
            moveTo(pole.x, pole.y)
            quadraticTo(pole.x + 0.012f * u, pole.y + 0.002f * u + wave, pole.x + 0.026f * u, pole.y + 0.007f * u)
            quadraticTo(pole.x + 0.012f * u, pole.y + 0.012f * u - wave, pole.x, pole.y + 0.014f * u)
            close()
        }
        inked(flag, FxC.red, pen, shade = false)
        for (k in 0 until 3) {
            val s = q(-0.05f + k * 0.05f, -0.004f, cz - 0.075f)
            drawPath(fxHeart(s.x, s.y, 0.005f * u), if (k == 1) FxC.pink.lighten(0.3f) else Color.White)
            drawPath(fxHeart(s.x, s.y, 0.005f * u), Ink.line, style = pen.thin)
        }
    }
}

// ------------------------------------------------------------------------------------------ forest

internal fun DrawScope.fxTent(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val canvas = Color(0xFFE8743B)
    val d = 0.22f
    val apexF = q(0f, -0.25f, 0f)
    val apexB = q(0f, -0.25f, d)
    val rf = q(0.17f, 0f, 0f)
    val rb = q(0.17f, 0f, d)
    fxLine(apexB, q(0f, 0f, d + 0.06f), Color(0xFFD8C8A0), pen.lw * 0.6f)
    fxFace(fxQuad(apexF.x, apexF.y, apexB.x, apexB.y, rb.x, rb.y, rf.x, rf.y), canvas.darken(0.18f), pen)
    for (k in 1..2) fxLine(fxMix(apexF, rf, k / 3f), fxMix(apexB, rb, k / 3f), canvas.darken(0.35f), pen.lw * 0.6f)
    capsule(apexB, Offset(apexB.x + 0.006f * u, apexB.y - 0.012f * u), 0.005f * u, FxC.oakDark, pen)
    val gy = fxMix(apexF, rf, 0.55f)
    fxLine(gy, q(0.25f, 0f, 0.05f), Color(0xFFD8C8A0), pen.lw * 0.6f)
    fxLine(q(0.25f, 0f, 0.05f), q(0.25f, -0.012f, 0.05f), FxC.charcoal, pen.lw)
    val front = fxPoly(u, 0f, -0.25f, 0.17f, 0f, -0.17f, 0f)
    inked(front, canvas, pen)
    val door = fxPoly(u, 0f, -0.228f, 0.118f, -0.004f, -0.118f, -0.004f)
    if (!f.open) {
        fxLine(p(0f, -0.23f), p(-0.11f, -0.005f), canvas.darken(0.3f), pen.lw * 0.6f)
        fxLine(p(0f, -0.23f), p(0.11f, -0.005f), canvas.darken(0.3f), pen.lw * 0.6f)
        fxLine(p(0f, -0.225f), p(0f, -0.005f), FxC.charcoal, 0.003f * u)
        for (k in 0 until 9) {
            val y = -0.21f + k * 0.022f
            fxLine(p(-0.003f, y), p(0.003f, y), FxC.charcoal, pen.lw * 0.5f)
        }
        inkedRound(Rect(-0.004f * u, -0.132f * u, 0.004f * u, -0.114f * u), 0.002f * u, FxC.steel, pen, shade = false)
    } else {
        clipPath(door) {
            drawPath(door, Color(0xFF3A2430))
            fxFace(fxFlat(u, -0.17f, 0.17f, -0.004f, 0f, d), Color(0xFF6B8A6F), pen)
            drawPath(fxPath(q(0f, -0.25f, d), q(0.17f, 0f, d), q(-0.17f, 0f, d)), Color(0xFF5A3448))
            capsule(q(-0.08f, -0.02f, d - 0.03f), q(0.08f, -0.02f, d - 0.03f), 0.022f * u, FxC.fjord, pen)
            fxGlow(q(0f, -0.1f, d * 0.5f), 0.16f * u, FxC.warm, 0.12f + 0.5f * pen.night)
        }
        drawPath(door, Ink.line, style = pen.stroke)
        for (s in 0..1) {
            val m = if (s == 0) -1f else 1f
            capsule(p(0.012f * m, -0.215f), p(0.12f * m, -0.012f), 0.018f * u, canvas.darken(0.1f), pen)
            for (k in 0 until 2) {
                val c = fxMix(p(0.012f * m, -0.215f), p(0.12f * m, -0.012f), 0.35f + k * 0.3f)
                fxLine(Offset(c.x - 0.01f * u, c.y - 0.004f * u), Offset(c.x + 0.01f * u, c.y + 0.004f * u), Color(0xFFF2E6C8), pen.lw)
            }
        }
    }
    val bottomHem = fxPoly(u, -0.17f, 0f, 0.17f, 0f, 0.165f, -0.008f, -0.165f, -0.008f)
    drawPath(bottomHem, canvas.darken(0.25f))
    fxLine(p(-0.085f, -0.125f), p(-0.22f, 0.01f), Color(0xFFD8C8A0), pen.lw * 0.6f)
    fxLine(p(-0.22f, 0.01f), p(-0.22f, -0.004f), FxC.charcoal, pen.lw)
    // A lantern hanging at the front of the ridge; it glows at night.
    val hook = p(0f, -0.25f)
    rotate(sin(t * 1.2f) * 6f, hook) {
        fxLine(hook, p(0f, -0.232f), Ink.line, pen.lw * 0.6f)
        val lamp = Rect(-0.01f * u, -0.232f * u, 0.01f * u, -0.206f * u)
        fxGlow(lamp.center, 0.1f * u, FxC.warm, 0.1f + 0.6f * pen.night)
        inkedRound(lamp, 0.003f * u, lerp(Color(0xFFFFF0B0), FxC.warm, pen.night), pen, shade = false)
        drawLine(FxC.charcoal, Offset(lamp.left, lamp.top), Offset(lamp.right, lamp.top), 0.003f * u)
        drawLine(FxC.charcoal, Offset(lamp.left, lamp.bottom), Offset(lamp.right, lamp.bottom), 0.003f * u)
        drawCircle(FxC.flame2, 0.003f * u, lamp.center)
    }
}

internal fun DrawScope.fxCampfire(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.07f
    val c = q(0f, 0f, cz)
    val on = f.on
    if (on) {
        drawPath(fxDisc2(c.x, c.y, 0.22f * u, 0.16f * u), FxC.flame2.copy(alpha = 0.1f + 0.14f * pen.night))
        fxGlow(Offset(c.x, c.y - 0.05f * u), 0.28f * u, FxC.flame2, 0.2f + 0.4f * pen.night)
    }
    fun stones(back: Boolean) {
        for (k in 0 until 9) {
            val a = k * 2f * FX_PI / 9f + 0.3f
            if ((sin(a) > 0f) != back) continue
            val s = fxRim(c.x, c.y, 0.068f * u, a)
            inkedOval(Rect(s.x - 0.016f * u, s.y - 0.016f * u, s.x + 0.016f * u, s.y + 0.004f * u), if (k % 2 == 0) FxC.stone else FxC.stone.darken(0.15f), pen)
        }
    }
    stones(true)
    // The grate's back legs, then the logs, embers and fire.
    for (x in floatArrayOf(-0.055f, 0.055f)) capsule(q(x, -0.1f, 0.085f), q(x * 1.25f, 0f, 0.1f), 0.0022f * u, FxC.iron, pen)
    val bark = Color(0xFF6E4A30)
    capsule(q(-0.05f, -0.012f, cz - 0.03f), q(0.045f, -0.03f, cz + 0.03f), 0.017f * u, bark, pen)
    capsule(q(0.05f, -0.012f, cz - 0.03f), q(-0.045f, -0.03f, cz + 0.03f), 0.017f * u, bark.lighten(0.08f), pen)
    for (s in 0..1) {
        val e = q(if (s == 0) -0.05f else 0.05f, -0.012f, cz - 0.03f)
        inkedCircle(e, 0.0085f * u, FxC.oak, pen, shade = false)
        drawCircle(FxC.oakDark, 0.004f * u, e, style = pen.thin)
    }
    for (k in 0 until 5) {
        val glow = if (on) 0.6f + 0.4f * sin(t * 4f + k * 1.3f) else 0.25f
        drawCircle(lerp(Color(0xFF5A1E12), FxC.flame1, glow), 0.004f * u, Offset(c.x + (k - 2) * 0.012f * u, c.y - 0.022f * u))
    }
    if (on) {
        fxFire(c.x - 0.022f * u, c.y - 0.02f * u, 0.04f * u, 0.08f * u, t, 0f, pen)
        fxFire(c.x + 0.024f * u, c.y - 0.02f * u, 0.04f * u, 0.09f * u, t, 2f, pen)
        fxFire(c.x, c.y - 0.018f * u, 0.07f * u, 0.13f * u, t, 4f, pen)
        for (k in 0 until 4) {
            val ph = fxFrac(t * 0.8f + k * 0.25f)
            drawCircle((if (k % 2 == 0) FxC.flame3 else FxC.flame2).copy(alpha = 1f - ph), 0.0022f * u, Offset(c.x + sin(ph * 7f + k) * 0.03f * u, c.y - (0.1f + ph * 0.15f) * u))
        }
        fxPuffs(c.x, c.y - 0.16f * u, t, 0.02f * u, 0.25f * u, Color(0xFF8E93A6), 0.35f, 4, 0.3f, 0.03f * u)
    } else {
        fxPuffs(c.x, c.y - 0.04f * u, t, 0.01f * u, 0.12f * u, Color(0xFFB0B4C0), 0.18f, 3, 0.25f, 0.015f * u)
    }
    // The grill grate: its front edge is where food rests.
    val grate = fxFlat(u, -0.058f, 0.058f, -0.1f, 0f, 0.09f)
    drawPath(grate, FxC.iron.copy(alpha = 0.22f))
    drawPath(grate, FxC.iron, style = Stroke(0.0035f * u))
    for (k in 0 until 9) {
        val x = -0.052f + k * 0.013f
        fxLine(q(x, -0.1f, 0f), q(x, -0.1f, 0.09f), FxC.iron.lighten(0.2f), 0.0022f * u)
    }
    drawLine(FxC.iron, q(-0.058f, -0.1f, 0f), q(0.058f, -0.1f, 0f), 0.005f * u, StrokeCap.Round)
    for (x in floatArrayOf(-0.055f, 0.055f)) capsule(q(x, -0.1f, 0.004f), q(x * 1.25f, 0f, -0.012f), 0.0022f * u, FxC.iron, pen)
    stones(false)
}

internal fun DrawScope.fxLog(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val bark = Color(0xFF7A5234)
    val cut = Color(0xFFE8C48A)
    val r = 0.036f * u
    val yc = -0.036f
    val zc = 0.036f
    val lc = q(-0.128f, yc, zc)
    val rc = q(0.125f, yc, zc)
    fun cap(c: Offset, rr: Float): Path {
        val k = 0.5523f * rr
        fun sx(dz: Float) = c.x + FX_DX * dz
        fun sy(dy: Float, dz: Float) = c.y + dy + FX_DY * dz
        return Path().apply {
            moveTo(sx(0f), sy(rr, 0f))
            cubicTo(sx(k), sy(rr, k), sx(rr), sy(k, rr), sx(rr), sy(0f, rr))
            cubicTo(sx(rr), sy(-k, rr), sx(k), sy(-rr, k), sx(0f), sy(-rr, 0f))
            cubicTo(sx(-k), sy(-rr, -k), sx(-rr), sy(-k, -rr), sx(-rr), sy(0f, -rr))
            cubicTo(sx(-rr), sy(k, -rr), sx(-k), sy(rr, -k), sx(0f), sy(rr, 0f))
            close()
        }
    }
    fxFace(cap(lc, r), bark.darken(0.1f), pen)
    val tx = 0.169f * r
    val ty = 1.063f * r
    val body = fxQuad(lc.x + tx, lc.y - ty, rc.x + tx, rc.y - ty, rc.x - tx, rc.y + ty, lc.x - tx, lc.y + ty)
    drawPath(body, Brush.verticalGradient(0f to bark.lighten(0.12f), 0.5f to bark, 1f to bark.darken(0.25f), startY = lc.y - ty, endY = lc.y + ty))
    drawLine(Ink.line, Offset(lc.x + tx, lc.y - ty), Offset(rc.x + tx, rc.y - ty), pen.lw)
    drawLine(Ink.line, Offset(lc.x - tx, lc.y + ty), Offset(rc.x - tx, rc.y + ty), pen.lw)
    for (k in 0 until 4) {
        val y = lc.y - ty * 0.6f + k * ty * 0.45f
        val line = Path().apply {
            moveTo(lc.x + 0.02f * u, y)
            quadraticTo((lc.x + rc.x) / 2f, y + (if (k % 2 == 0) 0.004f else -0.004f) * u, rc.x - 0.02f * u, y)
        }
        drawPath(line, bark.darken(0.35f), style = pen.thin)
    }
    inkedOval(Rect(lc.x + 0.07f * u, lc.y - 0.004f * u, lc.x + 0.084f * u, lc.y + 0.006f * u), bark.darken(0.2f), pen, shade = false)
    // The split top where people sit.
    fxFace(fxFlat(u, -0.116f, 0.113f, -0.064f, 0.0134f, 0.0586f, 0.004f), Color(0xFFC99A62), pen)
    fxLine(q(-0.1f, -0.064f, 0.03f), q(0.1f, -0.064f, 0.035f), Color(0xFFA0763F), pen.lw * 0.5f)
    val face = cap(rc, r)
    fxFace(face, cut, pen)
    for (k in 1..2) drawPath(cap(rc, r * (1f - k * 0.3f)), cut.darken(0.2f), style = pen.thin)
    drawCircle(cut.darken(0.3f), 0.002f * u, rc)
    fxLine(rc, Offset(rc.x + 0.004f * u, rc.y - r * 0.9f), cut.darken(0.35f), pen.lw * 0.6f)
    fxCloud(FxC.leaf, pen, true, lc.x + 0.02f * u, lc.y - ty * 0.8f, 0.01f * u, lc.x + 0.036f * u, lc.y - ty * 0.9f, 0.008f * u)
    val m = q(-0.14f, 0f, 0.01f)
    fxLine(m, Offset(m.x, m.y - 0.012f * u), FxC.paint, 0.004f * u)
    drawArc(FxC.red, 180f, 180f, true, Offset(m.x - 0.01f * u, m.y - 0.02f * u), Size(0.02f * u, 0.016f * u))
    drawArc(Ink.line, 180f, 180f, true, Offset(m.x - 0.01f * u, m.y - 0.02f * u), Size(0.02f * u, 0.016f * u), style = pen.thin)
    drawCircle(Color.White, 0.0018f * u, Offset(m.x - 0.003f * u, m.y - 0.016f * u))
}

internal fun DrawScope.fxStump(f: Fixture, u: Float, pen: Pen) = fxRoundCentred(u, 0.042f) { fxStumpBody(u, pen) }

private fun DrawScope.fxStumpBody(u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val bark = Color(0xFF7A5234)
    val cut = Color(0xFFE8C48A)
    val cz = 0.042f
    val b = q(0f, 0f, cz)
    val top = q(0f, -0.08f, cz)
    for (a in floatArrayOf(3.4f, 4.7f, 5.9f)) {
        val root = fxRim(b.x, b.y, 0.05f * u, a)
        inked(blobPath(root.x - 0.016f * u, root.y + 0.002f * u, root.x, root.y - 0.016f * u, root.x + 0.016f * u, root.y + 0.002f * u, root.x, root.y + 0.006f * u), bark, pen)
    }
    fxCyl(b.x, b.y, top.y, 0.046f * u, 0.04f * u, bark, pen, top = cut)
    for (k in 0 until 4) {
        val x = b.x - 0.03f * u + k * 0.02f * u
        fxLine(Offset(x, top.y + 0.012f * u), Offset(x + 0.002f * u, b.y - 0.004f * u), bark.darken(0.35f), pen.lw * 0.5f)
    }
    drawPath(fxDisc(top.x, top.y, 0.028f * u), cut.darken(0.2f), style = pen.thin)
    drawPath(fxDisc(top.x, top.y, 0.015f * u), cut.darken(0.2f), style = pen.thin)
    drawCircle(cut.darken(0.35f), 0.002f * u, top)
    fxLine(top, Offset(top.x + 0.024f * u, top.y - 0.004f * u), cut.darken(0.35f), pen.lw * 0.6f)
    fxCloud(FxC.leaf, pen, true, b.x - 0.04f * u, b.y - 0.04f * u, 0.01f * u, b.x - 0.042f * u, b.y - 0.026f * u, 0.008f * u)
}

internal fun DrawScope.fxOwlTree(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.06f
    val leafC = Color(0xFF8CC63F)
    val sway = sin(t * 0.8f) * 0.004f * u
    fun clusters(color: Color, vararg xyzr: Float) {
        val arr = FloatArray(xyzr.size)
        var i = 0
        while (i < xyzr.size) {
            val c = q(xyzr[i], xyzr[i + 1], xyzr[i + 2])
            arr[i] = c.x + sway
            arr[i + 1] = c.y
            arr[i + 2] = xyzr[i + 3] * u
            i += 4
        }
        // Reuse the (x, y, r) triples: every fourth slot is spare.
        val tri = FloatArray(xyzr.size / 4 * 3)
        for (k in 0 until xyzr.size / 4) {
            tri[k * 3] = arr[k * 4]
            tri[k * 3 + 1] = arr[k * 4 + 1]
            tri[k * 3 + 2] = arr[k * 4 + 2]
        }
        fxCloud(color, pen, true, *tri)
    }
    clusters(leafC.darken(0.18f), -0.12f, -0.58f, 0.13f, 0.075f, 0.1f, -0.62f, 0.14f, 0.08f, 0f, -0.7f, 0.12f, 0.08f, -0.05f, -0.66f, 0.15f, 0.07f)
    val b = q(0f, 0f, cz)
    val tp = q(0f, -0.66f, cz)
    inked(blobPath(b.x - 0.07f * u, b.y + 0.004f * u, b.x - 0.04f * u, b.y - 0.03f * u, b.x + 0.04f * u, b.y - 0.03f * u, b.x + 0.07f * u, b.y + 0.004f * u, b.x, b.y + 0.012f * u), FxC.birch, pen)
    fxCyl(b.x, b.y, tp.y, 0.042f * u, 0.02f * u, FxC.birch, pen, cap = false)
    capsule(q(0.012f, -0.5f, cz), q(0.12f, -0.6f, cz + 0.02f), 0.01f * u, FxC.birch, pen)
    capsule(q(-0.012f, -0.46f, cz), q(-0.13f, -0.56f, cz - 0.02f), 0.01f * u, FxC.birch, pen)
    capsule(q(0f, -0.6f, cz), q(0.07f, -0.7f, cz + 0.03f), 0.008f * u, FxC.birch, pen)
    // Birch bark: dark dashes across the white trunk.
    val levels = floatArrayOf(-0.05f, -0.11f, -0.18f, -0.25f, -0.32f, -0.37f, -0.5f, -0.56f, -0.62f)
    for ((k, y) in levels.withIndex()) {
        val c = q(0f, y, cz)
        val rr = (0.042f + (0.02f - 0.042f) * (y / -0.66f)) * u
        val left = k % 2 == 0
        val x0 = if (left) c.x - rr * 1.05f else c.x + rr * 0.1f
        val x1 = if (left) c.x - rr * 0.25f else c.x + rr * 1.0f
        drawLine(Ink.line, Offset(x0, c.y), Offset(x1, c.y - 0.002f * u), 0.004f * u, StrokeCap.Round)
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(x0 + rr * 0.3f, c.y + 0.006f * u), Offset(x1 - rr * 0.2f, c.y + 0.005f * u), 0.002f * u, StrokeCap.Round)
    }
    // The hollow, and the owl who lives there.
    val hc = q(0f, -0.42f, cz - 0.03f)
    val hollow = Rect(hc.x - 0.024f * u, hc.y - 0.032f * u, hc.x + 0.024f * u, hc.y + 0.032f * u)
    drawOval(Color(0xFF2A1E26), hollow.topLeft, hollow.size)
    drawOval(Ink.line, hollow.topLeft, hollow.size, style = pen.stroke)
    val show = if (f.mode == 1) ((2.5f - f.timer) * 6f).coerceIn(0f, 1f) * (f.timer * 6f).coerceIn(0f, 1f) else 0f
    if (show > 0f) {
        val oc = Offset(hc.x, hc.y + (1f - show) * 0.04f * u)
        val owl = Color(0xFF9A7B5A)
        for (s in 0..1) {
            val m = if (s == 0) -1f else 1f
            inked(fxPoly(1f, oc.x + m * 0.012f * u, oc.y - 0.014f * u, oc.x + m * 0.022f * u, oc.y - 0.034f * u, oc.x + m * 0.004f * u, oc.y - 0.02f * u), owl.darken(0.1f), pen, shade = false)
        }
        inkedCircle(oc, 0.022f * u, owl, pen)
        for (s in 0..1) {
            val m = if (s == 0) -1f else 1f
            val e = Offset(oc.x + m * 0.009f * u, oc.y - 0.004f * u)
            drawCircle(FxC.cream, 0.009f * u, e)
            val blink = fxFrac(t / 3f) < 0.04f
            if (blink) {
                drawLine(Ink.line, Offset(e.x - 0.006f * u, e.y), Offset(e.x + 0.006f * u, e.y), pen.lw)
            } else {
                drawCircle(FxC.yellow, 0.0065f * u, e)
                drawCircle(Ink.line, 0.0035f * u, e)
                drawCircle(Color.White, 0.0012f * u, Offset(e.x - 0.0015f * u, e.y - 0.0015f * u))
            }
        }
        inked(fxPoly(1f, oc.x - 0.003f * u, oc.y + 0.002f * u, oc.x + 0.003f * u, oc.y + 0.002f * u, oc.x, oc.y + 0.009f * u), FxC.terracotta, pen, shade = false)
    } else if (pen.night > 0.2f) {
        for (s in 0..1) drawCircle(FxC.yellow.copy(alpha = pen.night * (if (fxFrac(t / 4f) < 0.05f) 0f else 0.9f)), 0.003f * u, Offset(hc.x + (s * 2 - 1) * 0.007f * u, hc.y))
    }
    drawArc(FxC.birch, 20f, 140f, false, Offset(hollow.left - 0.003f * u, hollow.top), Size(hollow.width + 0.006f * u, hollow.height + 0.004f * u), style = Stroke(0.007f * u))
    drawArc(Ink.line, 20f, 140f, false, Offset(hollow.left - 0.003f * u, hollow.top), Size(hollow.width + 0.006f * u, hollow.height + 0.004f * u), style = pen.thin)
    clusters(leafC, -0.16f, -0.62f, 0.06f, 0.05f, 0.16f, -0.64f, 0.07f, 0.05f, -0.14f, -0.54f, 0.03f, 0.065f, 0.13f, -0.56f, 0.02f, 0.065f, -0.06f, -0.62f, 0.02f, 0.07f, 0.06f, -0.66f, 0.04f, 0.07f, 0f, -0.75f, 0.08f, 0.06f)
    for (k in 0 until 3) {
        val c = q(-0.1f + k * 0.1f, -0.51f + (k % 2) * 0.02f, 0.02f)
        fxLine(c, Offset(c.x + sway, c.y + 0.02f * u), Color(0xFF8A6A3A), 0.004f * u)
    }
}

// ------------------------------------------------------------------------------------------ mountain

internal fun DrawScope.fxPineTree(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.12f
    val base = q(0f, 0f, cz)
    fxFace(fxDisc2(base.x, base.y, 0.17f * u, 0.12f * u), FxC.snow, pen)
    rotate(f.angle * 57.29578f, base) {
        fxCyl(base.x, base.y, base.y - 0.08f * u, 0.024f * u, 0.02f * u, Color(0xFF6E4A30), pen)
        val tiers = floatArrayOf(-0.05f, -0.32f, 0.19f, -0.18f, -0.43f, 0.16f, -0.3f, -0.53f, 0.13f, -0.41f, -0.63f, 0.1f, -0.52f, -0.74f, 0.068f)
        for (i in 0 until 5) {
            val yb = tiers[i * 3]
            val yt = tiers[i * 3 + 1]
            val rr = tiers[i * 3 + 2] * u
            val cb = Offset(base.x, base.y + yb * u)
            val apex = Offset(base.x, base.y + yt * u)
            val tier = Path()
            tier.moveTo(apex.x, apex.y)
            val a0 = FX_PI + 0.4636f
            val a1 = 2f * FX_PI + 0.4636f
            val n = 8
            for (k in 0..n) {
                val a = a0 + (a1 - a0) * k / n
                val tooth = k % 2 == 0
                val pt = fxRim(cb.x, cb.y, if (tooth) rr else rr * 0.84f, a)
                tier.lineTo(pt.x, pt.y + (if (tooth) 0.012f else -0.004f) * u)
            }
            tier.close()
            inked(tier, if (i % 2 == 0) FxC.spruce else FxC.spruceLight.darken(0.1f), pen)
            val sy = yt + (yb - yt) * 0.5f
            val sc = Offset(base.x, base.y + sy * u)
            val snow = Path()
            snow.moveTo(apex.x, apex.y - 0.004f * u)
            for (k in 0..n) {
                val a = a0 + (a1 - a0) * k / n
                val pt = fxRim(sc.x, sc.y, rr * 0.56f, a)
                snow.lineTo(pt.x, pt.y + (if (k % 2 == 0) 0.008f else -0.002f) * u)
            }
            snow.close()
            fxFace(snow, FxC.snow, pen)
            fxLine(Offset(apex.x - 0.004f * u, apex.y + 0.02f * u), Offset(apex.x - rr * 0.3f, sc.y), FxC.snowShade, pen.lw * 0.8f)
        }
        fxCloud(FxC.snow, pen, true, base.x, base.y - 0.745f * u, 0.012f * u)
    }
    val shake = abs(f.angle)
    if (shake > 0.01f) {
        for (k in 0 until 6) {
            val ph = fxFrac(t * 1.5f + k * 0.17f)
            drawCircle(Color.White.copy(alpha = (shake * 12f).coerceAtMost(1f) * (1f - ph)), 0.004f * u, Offset(base.x + (k - 2.5f) * 0.05f * u, base.y - (0.6f - ph * 0.5f) * u))
        }
    }
}

internal fun DrawScope.fxSauna(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val logC = Color(0xFFB07A48)
    val ends = Color(0xFFE3B27A)
    val doorC = Color(0xFFC99060)
    val d = 0.24f
    val course = 0.03125f
    // Right wall of logs running back.
    for (k in 0 until 8) {
        val y0 = -k * course
        fxFace(fxDeep(u, 0.165f, y0 - course, y0, 0f, d, 0.01f), logC.darken(0.2f), pen)
    }
    // Snowy roof: the right slope, the chimney, the front gable.
    val apexF = q(0f, -0.335f, -0.02f)
    val apexB = q(0f, -0.335f, d + 0.02f)
    val eaveF = q(0.205f, -0.235f, -0.02f)
    val eaveB = q(0.205f, -0.235f, d + 0.02f)
    fxFace(fxQuad(apexF.x, apexF.y, apexB.x, apexB.y, eaveB.x, eaveB.y, eaveF.x, eaveF.y), Color(0xFF6E4A30), pen)
    val snowTop = fxQuad(apexF.x, apexF.y - 0.012f * u, apexB.x, apexB.y - 0.012f * u, eaveB.x, eaveB.y - 0.006f * u, eaveF.x, eaveF.y - 0.006f * u, 0.01f * u)
    fxFace(snowTop, FxC.snow, pen)
    val ch = q(0.1f, -0.29f, 0.15f)
    fxBox(u, 0.085f, -0.4f, 0.115f, -0.28f, 0.03f, FxC.stone, pen, z = 0.14f)
    fxCloud(FxC.snow, pen, true, ch.x + 0.02f * u, ch.y - 0.115f * u, 0.012f * u)
    if (f.on) fxPuffs(ch.x + 0.02f * u, ch.y - 0.13f * u, t, 0.02f * u, 0.18f * u, Color.White, 0.65f, 5, 0.35f, 0.03f * u)
    inked(fxPoly(u, -0.165f, -0.25f, 0f, -0.33f, 0.165f, -0.25f), logC.lighten(0.08f), pen)
    for (k in -3..3) fxLine(p(k * 0.04f, -0.25f), p(k * 0.04f, -0.33f + abs(k) * 0.02f), logC.darken(0.25f), pen.lw * 0.5f)
    drawCircle(Color(0xFF3A2A20), 0.01f * u, p(0f, -0.285f))
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        capsule(p(0.205f * m, -0.232f), p(0f, -0.342f), 0.014f * u, logC.darken(0.15f), pen)
    }
    fxCloud(FxC.snow, pen, true, -0.17f * u, -0.255f * u, 0.014f * u, -0.1f * u, -0.295f * u, 0.016f * u, -0.03f * u, -0.335f * u, 0.016f * u, 0.04f * u, -0.33f * u, 0.015f * u, 0.11f * u, -0.29f * u, 0.015f * u, 0.18f * u, -0.25f * u, 0.013f * u)
    for (k in 0 until 4) {
        val x = if (k < 2) -0.19f + k * 0.02f else 0.17f + (k - 2) * 0.02f
        val len = 0.014f + (k % 2) * 0.008f + sin(k * 3f) * 0.002f
        inked(fxPoly(u, x - 0.004f, -0.232f, x + 0.004f, -0.232f, x, -0.232f + len), Color(0xFFE6F4FF), pen, shade = false)
    }
    if (!f.open) {
        for (k in 0 until 8) {
            val y0 = -k * course
            inkedRound(Rect(-0.165f * u, (y0 - course) * u, 0.165f * u, y0 * u), 0.012f * u, logC, pen)
            fxLine(p(-0.15f, y0 - course * 0.45f), p(0.15f, y0 - course * 0.5f), logC.darken(0.25f).copy(alpha = 0.6f), pen.lw * 0.5f)
        }
        for (k in 0 until 8) {
            val y = -k * course - course / 2f
            for (m in floatArrayOf(-0.17f, 0.17f)) {
                inkedCircle(p(m, y), 0.0145f * u, ends, pen)
                drawCircle(ends.darken(0.25f), 0.007f * u, p(m, y), style = pen.thin)
            }
        }
        fxBox(u, -0.048f, -0.205f, 0.048f, -0.004f, 0.006f, doorC, pen, rad = 0.006f, z = -0.006f)
        val door = fxFront(u, -0.048f, -0.205f, 0.048f, -0.004f, -0.006f)
        for (k in 1..3) fxLine(Offset(door.left + door.width * k / 4f, door.top + 0.004f * u), Offset(door.left + door.width * k / 4f, door.bottom - 0.004f * u), doorC.darken(0.25f), pen.lw * 0.5f)
        val win = Rect(door.center.x - 0.014f * u, door.top + 0.016f * u, door.center.x + 0.014f * u, door.top + 0.044f * u)
        inkedRound(win, 0.004f * u, if (f.on) Color(0xFFFFC870) else Color(0xFF3A3040), pen, shade = false)
        if (f.on) fxGlow(win.center, 0.04f * u, FxC.warm, 0.6f)
        fxLine(Offset(win.center.x, win.top), Offset(win.center.x, win.bottom), doorC, 0.003f * u)
        fxLine(Offset(door.right - 0.012f * u, door.center.y), Offset(door.right - 0.012f * u, door.center.y + 0.025f * u), FxC.oakDark, 0.006f * u)
        val sw = Rect(-0.135f * u, -0.17f * u, -0.085f * u, -0.125f * u)
        inkedRound(sw, 0.003f * u, lerp(Color(0xFF3A3040), FxC.warm, if (f.on) 0.9f else pen.night * 0.5f), pen, shade = false)
        drawLine(logC, Offset(sw.center.x, sw.top), Offset(sw.center.x, sw.bottom), 0.004f * u)
        drawLine(logC, Offset(sw.left, sw.center.y), Offset(sw.right, sw.center.y), 0.004f * u)
    } else {
        // Open: the warm inside with its bench, the stove and a bucket; the door stands open.
        val wall = Color(0xFFE3B27A)
        fxHollow(u, -0.15f, -0.25f, 0.15f, -0.02f, d - 0.02f, wall, pen, back = Color(0xFFD9A15E))
        clipRect(-0.15f * u, -0.25f * u, 0.15f * u, -0.02f * u) {
            val back = fxFront(u, -0.15f, -0.25f, 0.15f, -0.02f, d - 0.02f)
            var x = back.left + 0.02f * u
            while (x < back.right) {
                drawLine(wall.darken(0.2f), Offset(x, back.top), Offset(x, back.bottom), pen.lw * 0.5f)
                x += 0.03f * u
            }
            fxGlow(q(0f, -0.15f, d * 0.5f), 0.2f * u, FxC.warm, 0.4f)
            fxBox(u, 0.11f, -0.07f, 0.148f, -0.02f, 0.04f, FxC.iron, pen, z = d - 0.08f)
            val st = q(0.129f, -0.07f, d - 0.06f)
            fxCloud(FxC.stone, pen, true, st.x - 0.008f * u, st.y - 0.004f * u, 0.007f * u, st.x + 0.006f * u, st.y - 0.006f * u, 0.007f * u, st.x, st.y - 0.012f * u, 0.006f * u)
            fxGlow(st, 0.03f * u, FxC.flame2, 0.5f)
            val bk = q(-0.13f, -0.02f, d - 0.07f)
            fxCyl(bk.x, bk.y, bk.y - 0.03f * u, 0.014f * u, 0.017f * u, FxC.oak, pen, top = Color(0xFF5A3F2A))
            val th = q(0f, -0.2f, d - 0.02f)
            inkedCircle(th, 0.012f * u, FxC.paint, pen, shade = false)
            drawLine(FxC.red, th, Offset(th.x + 0.006f * u, th.y - 0.006f * u), pen.lw)
            fxBox(u, -0.15f, -0.075f, 0.15f, -0.063f, 0.12f, Color(0xFFE8C08A), pen, rad = 0.002f)
            for (k in 1..3) fxLine(q(-0.15f, -0.075f, k * 0.03f), q(0.15f, -0.075f, k * 0.03f), Color(0xFFC49A62), pen.lw * 0.5f)
            for (x0 in floatArrayOf(-0.12f, 0f, 0.12f)) fxBox(u, x0 - 0.006f, -0.063f, x0 + 0.006f, -0.02f, 0.012f, Color(0xFFC49A62), pen, z = 0.01f)
        }
        inkedRound(Rect(-0.165f * u, -0.262f * u, 0.165f * u, -0.25f * u), 0.004f * u, logC, pen)
        for (m in floatArrayOf(-1f, 1f)) {
            val l = if (m < 0f) -0.165f else 0.15f
            inkedRound(Rect(l * u, -0.25f * u, (l + 0.015f) * u, 0f), 0.003f * u, logC, pen)
        }
        fxOpenDoor(u, 0.15f, -0.205f, -0.004f, 0.096f, 1f, doorC, doorC.darken(0.08f), pen, 115f)
    }
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        fxCloud(FxC.snow, pen, true, 0.19f * m * u, 0.002f * u, 0.02f * u, 0.16f * m * u, 0.006f * u, 0.014f * u)
    }
}

internal fun DrawScope.fxSledHill(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.22f
    val n = 14
    val xs = FloatArray(n + 1)
    val ys = FloatArray(n + 1)
    for (i in 0..n) {
        val k = i / n.toFloat()
        xs[i] = -0.19f + 0.43f * k
        ys[i] = -0.32f + 0.29f * (k * k * (3f - 2f * k))
    }
    // The flag at the top, behind the rider.
    val pole = q(-0.225f, -0.322f, d * 0.6f)
    val pt = Offset(pole.x, pole.y - 0.12f * u)
    fxLine(pole, pt, FxC.charcoal, 0.004f * u)
    val wave = sin(t * 6f) * 0.006f * u
    val pennant = Path().apply {
        moveTo(pt.x, pt.y)
        quadraticTo(pt.x + 0.03f * u, pt.y + 0.004f * u + wave, pt.x + 0.06f * u, pt.y + 0.012f * u)
        quadraticTo(pt.x + 0.03f * u, pt.y + 0.02f * u - wave, pt.x, pt.y + 0.026f * u)
        close()
    }
    inked(pennant, FxC.red, pen, shade = false)
    // The slope, seen from above: it runs back from the ride path at its front edge.
    val top = Path()
    val s0 = q(-0.215f, -0.322f, 0f)
    top.moveTo(s0.x, s0.y)
    for (i in 0..n) {
        val a = q(xs[i], ys[i], 0f)
        top.lineTo(a.x, a.y)
    }
    val e0 = q(0.265f, 0f, 0f)
    val e1 = q(0.265f, 0f, d)
    top.lineTo(e0.x, e0.y)
    top.lineTo(e1.x, e1.y)
    for (i in n downTo 0) {
        val a = q(xs[i], ys[i], d)
        top.lineTo(a.x, a.y)
    }
    val s1 = q(-0.215f, -0.322f, d)
    top.lineTo(s1.x, s1.y)
    top.close()
    fxFace(top, FxC.snow, pen)
    for (lane in floatArrayOf(0.35f, 0.47f)) {
        val track = Path()
        for (i in 1..n) {
            val a = q(xs[i], ys[i], d * lane)
            if (i == 1) track.moveTo(a.x, a.y) else track.lineTo(a.x, a.y)
        }
        drawPath(track, FxC.snowShade, style = pen.thin)
    }
    // The front of the hill.
    val front = Path().apply {
        moveTo(-0.27f * u, 0.004f * u)
        cubicTo(-0.255f * u, -0.12f * u, -0.245f * u, -0.3f * u, -0.215f * u, -0.322f * u)
        for (i in 0..n) lineTo(xs[i] * u, ys[i] * u)
        lineTo(0.265f * u, 0.004f * u)
        close()
    }
    drawPath(front, Brush.verticalGradient(listOf(FxC.snow, FxC.snowShade), -0.32f * u, 0f))
    drawPath(front, Ink.line, style = pen.stroke)
    for (k in 0 until 4) {
        val y = -0.07f - k * 0.06f
        val x = -0.262f + k * 0.012f
        fxLine(p(x, y), p(x + 0.022f, y), FxC.snowShade.darken(0.1f), pen.lw * 0.8f)
    }
    for (k in 0 until 4) {
        val a = 0.6f + 0.4f * sin(t * 2.4f + k * 1.7f)
        twinkle(p(-0.15f + k * 0.1f, -0.2f + (k % 2) * 0.1f + k * 0.02f), 0.007f * u, Color.White, a)
    }
    inkedOval(Rect(-0.2f * u, -0.018f * u, -0.17f * u, 0.004f * u), FxC.stone, pen)
    inkedOval(Rect(0.1f * u, -0.012f * u, 0.124f * u, 0.004f * u), FxC.stone.darken(0.1f), pen)
}

internal fun DrawScope.fxSnowman(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val mode = f.mode.coerceIn(0, 4)
    val cz = 0.06f
    val g = q(0f, 0f, cz)
    fxFace(fxDisc2(g.x, g.y, 0.075f * u, 0.06f * u), FxC.snow, pen)
    fun ball(y: Float, r: Float) {
        val c = q(0f, y, cz)
        val rr = r * u
        drawCircle(Ink.line, rr + pen.lw, c)
        drawCircle(FxC.snowShade, rr, c)
        drawCircle(FxC.snow, rr * 0.86f, Offset(c.x - rr * 0.08f, c.y - rr * 0.1f))
        drawCircle(Color.White.copy(alpha = 0.8f), rr * 0.12f, Offset(c.x - rr * 0.4f, c.y - rr * 0.45f))
    }
    if (mode == 0) {
        fxCloud(FxC.snow, pen, true, g.x - 0.03f * u, g.y - 0.012f * u, 0.028f * u, g.x + 0.02f * u, g.y - 0.018f * u, 0.032f * u, g.x - 0.002f * u, g.y - 0.034f * u, 0.024f * u)
        val sb = q(0.06f, -0.012f, 0.0f)
        drawCircle(Ink.line, 0.012f * u + pen.lw, sb)
        drawCircle(FxC.snow, 0.012f * u, sb)
        return
    }
    ball(-0.056f, 0.058f)
    val coal = Color(0xFF2B2140)
    if (mode >= 2) {
        if (mode == 4) {
            for (s in 0..1) {
                val m = if (s == 0) -1f else 1f
                val a = q(0.035f * m, -0.15f, cz)
                val b = q(0.085f * m, -0.19f, cz)
                capsule(a, b, 0.005f * u, Color(0xFF6E4A30), pen)
                capsule(fxMix(a, b, 0.7f), Offset(fxMix(a, b, 0.7f).x + 0.008f * m * u, fxMix(a, b, 0.7f).y - 0.016f * u), 0.003f * u, Color(0xFF6E4A30), pen)
            }
        }
        ball(-0.143f, 0.042f)
        if (mode == 4) for (k in 0 until 3) drawCircle(coal, 0.0045f * u, q(0f, -0.165f + k * 0.022f, cz - 0.04f))
    }
    if (mode >= 3) ball(-0.207f, 0.032f)
    if (mode == 4) {
        val h = q(0f, -0.207f, cz - 0.03f)
        for (s in 0..1) drawCircle(coal, 0.004f * u, Offset(h.x + (s * 2 - 1) * 0.011f * u, h.y - 0.006f * u))
        for (k in 0 until 5) {
            val a = FX_PI * (0.2f + k * 0.15f)
            drawCircle(coal, 0.002f * u, Offset(h.x + cos(a) * 0.014f * u, h.y + 0.004f * u + sin(a) * 0.01f * u))
        }
        inked(fxPoly(1f, h.x, h.y - 0.002f * u, h.x + 0.04f * u, h.y + 0.003f * u, h.x, h.y + 0.006f * u), Color(0xFFFF8A2E), pen, shade = false)
        // A knitted scarf and hat.
        val neck = q(0f, -0.178f, cz)
        inkedRound(Rect(neck.x - 0.036f * u, neck.y - 0.009f * u, neck.x + 0.036f * u, neck.y + 0.008f * u), 0.008f * u, FxC.red, pen)
        fxKnit(Rect(neck.x - 0.034f * u, neck.y - 0.007f * u, neck.x + 0.034f * u, neck.y + 0.006f * u), FxC.red.darken(0.25f), 8, 2, pen.lw * 0.4f)
        val end = Path().apply {
            moveTo(neck.x + 0.012f * u, neck.y)
            lineTo(neck.x + 0.03f * u, neck.y + 0.004f * u)
            lineTo(neck.x + 0.036f * u, neck.y + 0.045f * u)
            lineTo(neck.x + 0.022f * u, neck.y + 0.047f * u)
            close()
        }
        inked(end, FxC.red, pen)
        fxLine(Offset(neck.x + 0.02f * u, neck.y + 0.02f * u), Offset(neck.x + 0.033f * u, neck.y + 0.018f * u), Color.White, 0.003f * u)
        for (k in 0 until 3) fxLine(Offset(neck.x + (0.024f + k * 0.005f) * u, neck.y + 0.047f * u), Offset(neck.x + (0.024f + k * 0.005f) * u, neck.y + 0.055f * u), FxC.red, pen.lw * 0.8f)
        val hc = q(0f, -0.222f, cz)
        val hat = Path().apply {
            moveTo(hc.x - 0.033f * u, hc.y)
            quadraticTo(hc.x - 0.034f * u, hc.y - 0.04f * u, hc.x, hc.y - 0.042f * u)
            quadraticTo(hc.x + 0.034f * u, hc.y - 0.04f * u, hc.x + 0.033f * u, hc.y)
            close()
        }
        inked(hat, FxC.fjord, pen)
        clipPath(hat) { fxKnit(Rect(hc.x - 0.035f * u, hc.y - 0.042f * u, hc.x + 0.035f * u, hc.y), FxC.fjord.darken(0.25f), 7, 4, pen.lw * 0.4f) }
        inkedRound(Rect(hc.x - 0.036f * u, hc.y - 0.006f * u, hc.x + 0.036f * u, hc.y + 0.007f * u), 0.005f * u, Color.White, pen)
        fxCloud(Color.White, pen, true, hc.x, hc.y - 0.048f * u, 0.011f * u)
        twinkle(q(-0.05f, -0.1f, cz - 0.05f), 0.006f * u, Color.White, 0.6f + 0.4f * sin(t * 3f))
    }
}

internal fun DrawScope.fxSkiJump(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.1f
    val n = 14
    val timber = Color(0xFFB07A48)
    fun yAt(x: Float): Float {
        val k = ((x + 0.23f) / 0.44f).coerceIn(0f, 1f)
        return -0.42f + 0.27f * k + 0.06f * sin(k * FX_PI)
    }
    val xs = FloatArray(n + 1)
    val ys = FloatArray(n + 1)
    for (i in 0..n) {
        val k = i / n.toFloat()
        xs[i] = -0.23f + 0.44f * k
        ys[i] = -0.42f + 0.27f * k + 0.06f * sin(k * FX_PI)
    }
    // Landing snow and the back of the trestle.
    val land = q(0.2f, 0f, d * 0.5f)
    fxFace(fxDisc2(land.x, land.y, 0.14f * u, 0.08f * u), FxC.snow, pen)
    val posts = floatArrayOf(-0.21f, -0.11f, -0.01f, 0.09f, 0.19f)
    for (px in posts) fxBox(u, px - 0.006f, yAt(px) + 0.014f, px + 0.006f, 0f, 0.012f, timber.darken(0.15f), pen, z = d - 0.012f)
    for (i in 0 until posts.size - 1) {
        capsule(q(posts[i], yAt(posts[i]) + 0.03f, d - 0.006f), q(posts[i + 1], -0.02f, d - 0.006f), 0.004f * u, timber.darken(0.2f), pen)
    }
    // The start hut at the top.
    fxBox(u, -0.305f, -0.49f, -0.245f, -0.415f, 0.08f, FxC.red, pen, z = 0.01f)
    val hf = fxFront(u, -0.305f, -0.49f, -0.245f, -0.415f, 0.01f)
    inkedRound(Rect(hf.left + 0.018f * u, hf.top + 0.03f * u, hf.right - 0.018f * u, hf.bottom), 0.003f * u, FxC.red.darken(0.3f), pen, shade = false)
    fxCloud(FxC.snow, pen, true, hf.left + 0.012f * u, hf.top, 0.012f * u, hf.center.x, hf.top - 0.004f * u, 0.014f * u, hf.right - 0.004f * u, hf.top - 0.006f * u, 0.012f * u)
    val flag0 = Offset(hf.center.x + 0.02f * u, hf.top - 0.012f * u)
    fxLine(flag0, Offset(flag0.x, flag0.y - 0.06f * u), FxC.charcoal, 0.003f * u)
    val w0 = sin(t * 6f) * 0.004f * u
    inked(fxPoly(1f, flag0.x, flag0.y - 0.06f * u, flag0.x + 0.035f * u, flag0.y - 0.05f * u + w0, flag0.x, flag0.y - 0.04f * u), FxC.fjord, pen, shade = false)
    // The in-run: a snowy ramp whose front edge is the ride path.
    val deck = Path()
    val st = q(-0.25f, -0.42f, 0f)
    deck.moveTo(st.x, st.y)
    for (i in 0..n) {
        val a = q(xs[i], ys[i], 0f)
        deck.lineTo(a.x, a.y)
    }
    for (i in n downTo 0) {
        val a = q(xs[i], ys[i], d)
        deck.lineTo(a.x, a.y)
    }
    val sb = q(-0.25f, -0.42f, d)
    deck.lineTo(sb.x, sb.y)
    deck.close()
    fxFace(deck, FxC.snow, pen)
    for (lane in floatArrayOf(0.35f, 0.65f)) {
        val track = Path()
        for (i in 0..n) {
            val a = q(xs[i], ys[i], d * lane)
            if (i == 0) track.moveTo(a.x, a.y) else track.lineTo(a.x, a.y)
        }
        drawPath(track, FxC.fjord.copy(alpha = 0.4f), style = pen.thin)
    }
    val edge = Path()
    edge.moveTo(-0.25f * u, -0.42f * u)
    for (i in 0..n) edge.lineTo(xs[i] * u, ys[i] * u)
    for (i in n downTo 0) edge.lineTo(xs[i] * u, (ys[i] + 0.014f) * u)
    edge.lineTo(-0.25f * u, -0.406f * u)
    edge.close()
    fxFace(edge, timber, pen)
    for (i in 0..n step 2) {
        val a = q(xs[i], ys[i], d)
        fxLine(a, Offset(a.x, a.y - 0.028f * u), timber.darken(0.2f), pen.lw)
    }
    val rail = Path()
    for (i in 0..n) {
        val a = q(xs[i], ys[i] - 0.028f, d)
        if (i == 0) rail.moveTo(a.x, a.y) else rail.lineTo(a.x, a.y)
    }
    drawPath(rail, timber.darken(0.2f), style = Stroke(pen.lw * 1.2f))
    for (px in posts) fxBox(u, px - 0.007f, yAt(px) + 0.014f, px + 0.007f, 0f, 0.012f, timber, pen, z = 0.004f)
    for (i in 0 until posts.size - 1) {
        capsule(q(posts[i], yAt(posts[i]) + 0.03f, 0.004f), q(posts[i + 1], -0.02f, 0.004f), 0.004f * u, timber.darken(0.08f), pen)
    }
    // Flags at the lip.
    val lip = q(0.23f, -0.15f, d + 0.01f)
    fxLine(lip, Offset(lip.x, lip.y - 0.09f * u), FxC.charcoal, 0.003f * u)
    val w1 = sin(t * 6f + 1f) * 0.004f * u
    inked(fxPoly(1f, lip.x, lip.y - 0.09f * u, lip.x + 0.035f * u, lip.y - 0.08f * u + w1, lip.x, lip.y - 0.07f * u), FxC.red, pen, shade = false)
}

internal fun DrawScope.fxIcePond(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.13f
    val c = q(0f, -0.006f, cz)
    fxFace(fxDisc2(c.x, c.y + 0.006f * u, 0.335f * u, 0.14f * u), FxC.snow, pen)
    val ice = fxDisc2(c.x, c.y, 0.305f * u, 0.125f * u)
    drawPath(ice, Brush.linearGradient(listOf(Color(0xFFDDF3FC), Color(0xFFA6D6EF)), Offset(c.x - 0.3f * u, c.y - 0.05f * u), Offset(c.x + 0.3f * u, c.y + 0.05f * u)))
    clipPath(ice) {
        val crack = Path().apply {
            moveTo(c.x - 0.2f * u, c.y + 0.01f * u)
            lineTo(c.x - 0.14f * u, c.y - 0.005f * u)
            lineTo(c.x - 0.1f * u, c.y + 0.004f * u)
            moveTo(c.x + 0.12f * u, c.y - 0.03f * u)
            lineTo(c.x + 0.17f * u, c.y - 0.012f * u)
            lineTo(c.x + 0.22f * u, c.y - 0.02f * u)
        }
        drawPath(crack, Color.White.copy(alpha = 0.8f), style = pen.thin)
        for (k in 0 until 3) {
            val s = Offset(c.x - 0.18f * u + k * 0.15f * u, c.y - 0.02f * u + (k % 2) * 0.02f * u)
            fxLine(s, Offset(s.x + 0.03f * u, s.y - 0.012f * u), Color.White.copy(alpha = 0.75f), 0.004f * u)
        }
        drawArc(Color.White.copy(alpha = 0.6f), 0f, 300f, false, Offset(c.x + 0.05f * u, c.y + 0.0f * u), Size(0.07f * u, 0.025f * u), style = pen.thin)
        drawArc(Color.White.copy(alpha = 0.6f), 90f, 280f, false, Offset(c.x - 0.12f * u, c.y + 0.01f * u), Size(0.06f * u, 0.02f * u), style = pen.thin)
        if (f.anim > 0f) {
            for (k in 0 until 5) {
                val a = k * 1.3f
                fxLine(c, Offset(c.x + cos(a) * 0.1f * u, c.y + sin(a) * 0.03f * u), Color.White.copy(alpha = f.anim), pen.lw * 0.8f)
            }
        }
    }
    drawPath(ice, Ink.line, style = pen.stroke)
    for (k in 0 until 5) {
        val a = FX_PI * (0.15f + k * 0.17f)
        val rz = 0.135f * sin(a)
        val s = Offset(c.x + (0.32f * cos(a) + FX_DX * rz) * u, c.y + (0.002f + FX_DY * rz) * u)
        fxCloud(FxC.snow, pen, true, s.x, s.y, (0.012f + (k % 2) * 0.006f) * u)
    }
    // The wish hole, with a sparkle over it.
    val h = q(0f, -0.006f, 0.06f)
    drawPath(fxDisc2(h.x, h.y, 0.026f * u, 0.014f * u), Color(0xFF7FB8D8))
    drawPath(fxDisc2(h.x, h.y + 0.001f * u, 0.02f * u, 0.01f * u), Color(0xFF123050))
    drawPath(fxDisc2(h.x, h.y + 0.001f * u, 0.02f * u, 0.01f * u), Ink.line, style = pen.thin)
    twinkle(Offset(h.x + 0.01f * u, h.y - 0.025f * u), 0.012f * u * (0.6f + 0.4f * sin(t * 3f)), FxC.yellow, 0.9f)
}

/** An ordinary snack kiosk: a counter, a menu board, a striped awning and a pot of cocoa. */
internal fun DrawScope.fxKiosk(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.14f
    val body = FxC.red
    val wood = FxC.oak
    // Back wall with the menu board and a shelf of cups.
    val back = fxFront(u, -0.11f, -0.29f, 0.11f, -0.15f, d)
    inkedRound(back, 0.003f * u, wood.darken(0.15f), pen, shade = false)
    val board = Rect(back.left + 0.02f * u, back.top + 0.015f * u, back.right - 0.02f * u, back.top + 0.07f * u)
    inkedRound(board, 0.004f * u, FxC.charcoal, pen, shade = false)
    val cup = Offset(board.left + board.width * 0.2f, board.center.y)
    inkedRound(Rect(cup.x - 0.008f * u, cup.y - 0.008f * u, cup.x + 0.008f * u, cup.y + 0.01f * u), 0.003f * u, Color.White, pen, shade = false)
    val waffle = Offset(board.center.x, board.center.y)
    drawCircle(FxC.mustard, 0.011f * u, waffle)
    drawLine(FxC.mustard.darken(0.3f), Offset(waffle.x - 0.008f * u, waffle.y), Offset(waffle.x + 0.008f * u, waffle.y), pen.lw * 0.5f)
    drawLine(FxC.mustard.darken(0.3f), Offset(waffle.x, waffle.y - 0.008f * u), Offset(waffle.x, waffle.y + 0.008f * u), pen.lw * 0.5f)
    drawCircle(FxC.woodDark, 0.009f * u, Offset(board.left + board.width * 0.8f, board.center.y))
    for (k in 0 until 3) drawCircle(FxC.yellow, 0.002f * u, Offset(board.left + board.width * (0.2f + k * 0.3f), board.bottom - 0.007f * u))
    fxLine(Offset(back.left + 0.01f * u, back.top + 0.1f * u), Offset(back.right - 0.01f * u, back.top + 0.1f * u), wood.darken(0.35f), 0.004f * u)
    for (k in 0 until 4) {
        val c = Offset(back.left + (0.03f + k * 0.05f) * u, back.top + 0.098f * u)
        inkedRound(Rect(c.x - 0.007f * u, c.y - 0.014f * u, c.x + 0.007f * u, c.y), 0.003f * u, arrayOf(FxC.paint, FxC.dusty, FxC.mustard, FxC.sage)[k], pen, shade = false)
    }
    for (x in floatArrayOf(-0.105f, 0.093f)) fxBox(u, x, -0.29f, x + 0.012f, -0.15f, 0.012f, wood, pen, z = d - 0.012f)
    // The counter.
    fxBox(u, -0.11f, -0.145f, 0.11f, 0f, d, body, pen, rad = 0.004f)
    for (k in 1..5) fxLine(p(-0.11f + k * 0.0367f, -0.14f), p(-0.11f + k * 0.0367f, -0.005f), body.darken(0.2f), pen.lw * 0.5f)
    drawRect(FxC.paint, p(-0.108f, -0.11f), Size(0.216f * u, 0.012f * u))
    val icon = p(0f, -0.06f)
    drawCircle(FxC.paint, 0.022f * u, icon)
    drawCircle(Ink.line, 0.022f * u, icon, style = pen.thin)
    inkedRound(Rect(icon.x - 0.01f * u, icon.y - 0.008f * u, icon.x + 0.008f * u, icon.y + 0.012f * u), 0.003f * u, FxC.red, pen, shade = false)
    drawArc(FxC.red, -90f, 180f, false, Offset(icon.x + 0.004f * u, icon.y - 0.004f * u), Size(0.01f * u, 0.01f * u), style = Stroke(0.003f * u))
    fxBox(u, -0.115f, -0.15f, 0.115f, -0.138f, d + 0.008f, wood, pen, rad = 0.003f)
    // A steaming pot of cocoa at the back of the counter.
    val pot = q(-0.07f, -0.15f, 0.1f)
    fxCyl(pot.x, pot.y, pot.y - 0.034f * u, 0.022f * u, 0.022f * u, FxC.fjord, pen, top = Color(0xFF6B3A20))
    for (k in 0 until 3) drawCircle(Color.White, 0.002f * u, Offset(pot.x - 0.012f * u + k * 0.012f * u, pot.y - 0.018f * u))
    capsule(Offset(pot.x + 0.006f * u, pot.y - 0.034f * u), Offset(pot.x + 0.02f * u, pot.y - 0.06f * u), 0.003f * u, FxC.steel, pen)
    fxPuffs(pot.x, pot.y - 0.05f * u, t, 0.012f * u, 0.08f * u, Color.White, 0.55f, 4, 0.4f, 0.01f * u)
    for (x in floatArrayOf(-0.11f, 0.098f)) fxBox(u, x, -0.29f, x + 0.012f, -0.15f, 0.012f, wood, pen, z = 0.004f)
    // Roof with snow, and the striped awning.
    fxBox(u, -0.13f, -0.315f, 0.13f, -0.29f, d + 0.03f, FxC.paint, pen, rad = 0.004f, z = -0.01f)
    val roofTop = fxFlat(u, -0.13f, 0.13f, -0.315f, -0.01f, d + 0.02f, 0.01f)
    fxFace(roofTop, FxC.snow, pen)
    fxCloud(FxC.snow, pen, true, -0.1f * u, -0.322f * u, 0.012f * u, -0.02f * u, -0.324f * u, 0.014f * u, 0.07f * u, -0.322f * u, 0.012f * u)
    val a0 = q(-0.13f, -0.29f, -0.01f)
    val a1 = q(0.13f, -0.29f, -0.01f)
    val a2 = q(0.13f, -0.262f, -0.06f)
    val a3 = q(-0.13f, -0.262f, -0.06f)
    for (k in 0 until 8) {
        val f0 = k / 8f
        val f1 = (k + 1) / 8f
        val s = fxPath(fxMix(a0, a1, f0), fxMix(a0, a1, f1), fxMix(a3, a2, f1), fxMix(a3, a2, f0))
        drawPath(s, if (k % 2 == 0) FxC.red else FxC.paint)
    }
    drawPath(fxPath(a0, a1, a2, a3), Ink.line, style = pen.stroke)
    val hem = FloatArray(8 * 3)
    for (k in 0 until 8) {
        val c = fxMix(a3, a2, (k + 0.5f) / 8f)
        hem[k * 3] = c.x
        hem[k * 3 + 1] = c.y
        hem[k * 3 + 2] = 0.0085f * u
    }
    fxCloud(FxC.red, pen, false, *hem)
    for (k in 0 until 8) {
        if (k % 2 == 1) drawCircle(FxC.paint, 0.0085f * u, Offset(hem[k * 3], hem[k * 3 + 1]))
    }
    // String lights under the awning; they shine at night.
    val bulbs = arrayOf(FxC.red, FxC.yellow, FxC.green, FxC.fjord)
    for (k in 0 until 7) {
        val c = fxMix(a3, a2, (k + 0.5f) / 7f)
        val b = Offset(c.x, c.y + 0.02f * u + sin((k + 0.5f) / 7f * FX_PI) * 0.006f * u)
        val on = 0.5f + 0.5f * sin(t * 2f + k * 1.3f)
        fxGlow(b, 0.018f * u, bulbs[k % 4], pen.night * (0.4f + 0.4f * on))
        drawCircle(Ink.line, 0.0045f * u, b)
        drawCircle(lerp(bulbs[k % 4].darken(0.2f), bulbs[k % 4].lighten(0.4f), pen.night * on), 0.0035f * u, b)
    }
    for (k in 0 until 3) {
        val c = fxMix(a3, a2, 0.15f + k * 0.35f)
        inked(fxPoly(1f, c.x - 0.003f * u, c.y + 0.008f * u, c.x + 0.003f * u, c.y + 0.008f * u, c.x, c.y + (0.022f + k * 0.004f) * u), Color(0xFFE6F4FF), pen, shade = false)
    }
}

internal fun DrawScope.fxBench(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.1f
    val wood = FxC.oak
    val iron = FxC.charcoal
    for (x in floatArrayOf(-0.14f, 0.14f)) {
        capsule(q(x, 0f, d - 0.012f), q(x, -0.064f, d - 0.012f), 0.007f * u, iron, pen)
        capsule(q(x, -0.064f, d - 0.012f), q(x, -0.13f, d + 0.004f), 0.007f * u, iron, pen)
    }
    fxBox(u, -0.155f, -0.128f, 0.155f, -0.113f, 0.012f, wood, pen, rad = 0.003f, z = d - 0.004f)
    fxBox(u, -0.155f, -0.106f, 0.155f, -0.091f, 0.012f, wood, pen, rad = 0.003f, z = d - 0.008f)
    if (f.place == PlaceId.MOUNTAIN) {
        val s = fxFront(u, -0.15f, -0.128f, 0.15f, -0.113f, d - 0.004f)
        fxCloud(FxC.snow, pen, true, s.left + 0.03f * u, s.top, 0.008f * u, s.center.x, s.top - 0.002f * u, 0.009f * u, s.right - 0.03f * u, s.top, 0.008f * u)
    }
    for (k in 2 downTo 0) fxBox(u, -0.155f, -0.07f, 0.155f, -0.058f, 0.028f, wood, pen, rad = 0.003f, z = k * 0.034f)
    for (k in 0 until 2) {
        fxNail(Offset(-0.145f * u, -0.064f * u), 0.0018f * u)
        fxNail(Offset(0.145f * u, -0.064f * u), 0.0018f * u)
    }
    for (x in floatArrayOf(-0.14f, 0.14f)) {
        capsule(q(x, -0.058f, 0.012f), q(x, 0f, 0.006f), 0.007f * u, iron, pen)
        capsule(q(x, -0.1f, 0.004f), q(x, -0.1f, d), 0.006f * u, iron, pen)
        capsule(q(x, -0.07f, 0.01f), q(x, -0.1f, 0.006f), 0.005f * u, iron, pen)
        val foot = q(x, -0.004f, 0.006f)
        drawArc(Ink.line, 90f, 200f, false, Offset(foot.x - 0.012f * u, foot.y - 0.008f * u), Size(0.012f * u, 0.01f * u), style = Stroke(0.004f * u))
    }
}

internal fun DrawScope.fxLampPost(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val iron = Color(0xFF2F2D3A)
    val cz = 0.03f
    val b = q(0f, 0f, cz)
    val on = f.on
    if (on) drawPath(fxDisc2(b.x, b.y, 0.2f * u, 0.14f * u), FxC.warm.copy(alpha = 0.12f + 0.18f * pen.night))
    fxCyl(b.x, b.y, q(0f, -0.05f, cz).y, 0.03f * u, 0.017f * u, iron, pen)
    fxCyl(b.x, q(0f, -0.05f, cz).y, q(0f, -0.41f, cz).y, 0.008f * u, 0.008f * u, iron, pen, cap = false, bottom = false)
    for (y in floatArrayOf(-0.06f, -0.2f, -0.38f)) {
        val c = q(0f, y, cz)
        fxCyl(c.x, c.y + 0.004f * u, c.y - 0.004f * u, 0.012f * u, 0.012f * u, iron.lighten(0.08f), pen)
    }
    capsule(q(-0.03f, -0.39f, cz), q(0.03f, -0.39f, cz), 0.004f * u, iron, pen)
    inkedCircle(q(-0.03f, -0.39f, cz), 0.004f * u, iron, pen, shade = false)
    inkedCircle(q(0.03f, -0.39f, cz), 0.004f * u, iron, pen, shade = false)
    // The lantern: glass panes that glow, and a little roof.
    val lc = q(0f, -0.445f, cz)
    if (on) fxGlow(lc, 0.24f * u, FxC.warm, 0.3f + 0.45f * pen.night)
    val glass = if (on) Color(0xFFFFE680) else Color(0xFFE8E2C8)
    fxCyl(lc.x, lc.y + 0.035f * u, lc.y + 0.028f * u, 0.016f * u, 0.024f * u, iron, pen)
    val face = fxPath(Offset(lc.x - 0.024f * u, lc.y + 0.028f * u), Offset(lc.x + 0.012f * u, lc.y + 0.028f * u), Offset(lc.x + 0.018f * u, lc.y - 0.028f * u), Offset(lc.x - 0.03f * u, lc.y - 0.028f * u))
    val side = fxPath(Offset(lc.x + 0.012f * u, lc.y + 0.028f * u), Offset(lc.x + 0.026f * u, lc.y + 0.018f * u), Offset(lc.x + 0.034f * u, lc.y - 0.036f * u), Offset(lc.x + 0.018f * u, lc.y - 0.028f * u))
    fxFace(side, glass.darken(0.15f), pen)
    fxFace(face, glass, pen)
    if (on) drawCircle(Color.White, 0.006f * u, Offset(lc.x - 0.005f * u, lc.y))
    fxLine(Offset(lc.x - 0.006f * u, lc.y + 0.028f * u), Offset(lc.x - 0.006f * u, lc.y - 0.028f * u), iron, pen.lw)
    val r0 = Offset(lc.x - 0.036f * u, lc.y - 0.028f * u)
    val r1 = Offset(lc.x + 0.022f * u, lc.y - 0.028f * u)
    val r2 = Offset(lc.x + 0.04f * u, lc.y - 0.04f * u)
    val peak = Offset(lc.x + 0.002f * u, lc.y - 0.062f * u)
    fxFace(fxPath(r1, r2, peak), iron.darken(0.2f), pen)
    fxFace(fxPath(r0, r1, peak), iron.lighten(0.1f), pen)
    inkedCircle(Offset(peak.x, peak.y - 0.005f * u), 0.005f * u, iron, pen, shade = false)
    if (f.place == PlaceId.MOUNTAIN) fxCloud(FxC.snow, pen, true, peak.x - 0.008f * u, peak.y + 0.016f * u, 0.01f * u, peak.x + 0.01f * u, peak.y + 0.02f * u, 0.009f * u)
}
