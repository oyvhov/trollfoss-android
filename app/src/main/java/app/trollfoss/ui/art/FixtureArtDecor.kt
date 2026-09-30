package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Look
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/*
 * The home designer's catalogue and the tidying helpers, in oblique 3D («skrå-3D»): rugs, pictures, an
 * aquarium, a beanbag, an armchair, a bunk bed, a toy box, a desk, flower pots, a hungry bin and a robot
 * vacuum. Origin at the bottom centre of each fixture's front face; depth recedes up and to the right.
 * Floor pieces draw their own soft shadow. Private helpers start with `dc`; the 3D kit (fxBox, fxQ, ...)
 * lives in FixtureArt.kt.
 */

/** Draws the back layer of the home designer's furniture. Returns false for any other type. */
@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawDecorBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        FixtureType.RUG -> dcRug(f, u, pen)
        FixtureType.PICTURE -> dcPicture(f, u, pen)
        FixtureType.AQUARIUM -> dcAquarium(f, u, pen)
        FixtureType.BEANBAG -> dcBeanbag(f, u, pen)
        FixtureType.ARMCHAIR -> dcArmchair(f, u, pen)
        FixtureType.BUNK_BED -> dcBunkBed(f, u, pen)
        FixtureType.TOY_BOX -> dcToyBox(f, u, pen)
        FixtureType.DESK -> dcDesk(f, u, pen)
        FixtureType.FLOWER_POT -> dcFlowerPot(f, u, pen)
        FixtureType.TRASH_BIN -> dcTrashBin(f, u, pen)
        FixtureType.ROBOT_VACUUM -> dcVacuum(f, u, pen)
        else -> return false
    }
    return true
}

/**
 * Draws the front layer of the same furniture: the bunk bed's duvets over its sleepers. Returns true for
 * all eleven types (drawing nothing for those without a front) and false for any other type.
 */
fun DrawScope.drawDecorFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.BUNK_BED -> dcBunkFront(f, u, pen)
        FixtureType.RUG, FixtureType.PICTURE, FixtureType.AQUARIUM, FixtureType.BEANBAG, FixtureType.ARMCHAIR,
        FixtureType.TOY_BOX, FixtureType.DESK, FixtureType.FLOWER_POT, FixtureType.TRASH_BIN, FixtureType.ROBOT_VACUUM -> Unit
        else -> return false
    }
    return true
}

// ---------------------------------------------------------------------------------------------- kit

private fun DrawScope.dcShadow(u: Float, w: Float, d: Float) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow, Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

/** A closed shape lying flat on the floor through (x, z) pairs in units, at height [y]. */
private fun dcFloorBlob(u: Float, y: Float, vararg xz: Float): Path {
    val px = FloatArray(xz.size)
    var i = 0
    while (i < xz.size) {
        val p = fxQ(u, xz[i], y, xz[i + 1])
        px[i] = p.x
        px[i + 1] = p.y
        i += 2
    }
    return blobPath(*px)
}

/** A googly eye: white with a pupil that rolls toward (lx, ly), each in -1..1. */
private fun DrawScope.dcEye(c: Offset, r: Float, lx: Float, ly: Float, pen: Pen) {
    drawCircle(Color.White, r, c)
    drawCircle(Ink.line, r, c, style = pen.thin)
    drawCircle(Ink.line, r * 0.5f, Offset(c.x + lx * r * 0.4f, c.y + ly * r * 0.4f))
    drawCircle(Color.White, r * 0.15f, Offset(c.x + lx * r * 0.4f - r * 0.15f, c.y + ly * r * 0.4f - r * 0.15f))
}

private object DcC {
    val oak = FxC.oak
    val paint = FxC.paint
    val mustard = FxC.mustard
    val dusty = FxC.dusty
    val sage = FxC.sage
    val blush = FxC.blush
    val terracotta = FxC.terracotta
    val mint = FxC.mint
    val charcoal = FxC.charcoal
    val cream = FxC.cream
}

// ---------------------------------------------------------------------------------------------- rugs

private fun DrawScope.dcRug(f: Fixture, u: Float, pen: Pen) {
    when (f.variant.mod(4)) {
        0 -> dcBraidedRug(u, pen)
        1 -> dcRunner(u, pen)
        2 -> dcStarRug(u, pen)
        else -> dcSheepskin(u, pen)
    }
}

/** A round braided rug: rings of plaited colour. */
private fun DrawScope.dcBraidedRug(u: Float, pen: Pen) {
    val c = fxQ(u, 0f, 0f, 0.1f)
    val rx = 0.225f
    val rz = 0.12f
    drawPath(fxDisc2(c.x, c.y + 0.006f * u, rx * u, rz * u), DcC.terracotta.darken(0.3f))
    val cols = arrayOf(DcC.terracotta, DcC.mustard, DcC.sage, DcC.cream, DcC.dusty)
    for (k in cols.indices) {
        val s = 1f - k * 0.19f
        val ring = fxDisc2(c.x, c.y, rx * s * u, rz * s * u)
        drawPath(ring, cols[k])
        val inner = s - 0.095f
        val braid = fxDisc2(c.x, c.y, rx * inner * u, rz * inner * u)
        drawPath(braid, cols[k].darken(0.22f).copy(alpha = 0.7f), style = Stroke(0.004f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.006f * u, 0.005f * u))))
    }
    drawPath(fxDisc2(c.x, c.y, rx * u, rz * u), Ink.line, style = pen.stroke)
}

/** A striped runner with tasselled ends. */
private fun DrawScope.dcRunner(u: Float, pen: Pen) {
    val z0 = -0.005f
    val z1 = 0.13f
    val edge = fxFlat(u, -0.2f, 0.2f, 0f, z0, z1, 0.01f)
    translate(0f, 0.005f * u) { drawPath(edge, DcC.dusty.darken(0.35f)) }
    val cols = arrayOf(DcC.dusty, DcC.cream, DcC.mustard, DcC.cream, DcC.terracotta, DcC.cream)
    val n = 13
    for (k in 0 until n) {
        val x0 = -0.2f + k * 0.4f / n
        drawPath(fxFlat(u, x0, x0 + 0.4f / n + 0.001f, 0f, z0, z1), cols[k % cols.size])
    }
    clipPath(edge) {
        for (k in 0 until 4) {
            val z = z0 + (k + 0.5f) * (z1 - z0) / 4f
            fxLine(fxQ(u, -0.2f, 0f, z), fxQ(u, 0.2f, 0f, z), Ink.line.copy(alpha = 0.12f), pen.lw * 0.6f)
        }
    }
    drawPath(edge, Ink.line, style = pen.stroke)
    for (s in 0..1) {
        val x = if (s == 0) -0.2f else 0.2f
        val m = if (s == 0) -1f else 1f
        for (k in 0 until 8) {
            val a = fxQ(u, x, 0f, z0 + (k + 0.5f) * (z1 - z0) / 8f)
            fxLine(a, Offset(a.x + m * 0.014f * u, a.y), DcC.cream.darken(0.2f), pen.lw * 0.8f)
        }
    }
}

/** A star rug with a sleepy smile, for a child's room. */
private fun DrawScope.dcStarRug(u: Float, pen: Pen) {
    val cz = 0.13f
    val pts = FloatArray(20)
    for (i in 0 until 10) {
        val a = -FX_PI / 2f + i * FX_PI / 5f
        val r = if (i % 2 == 0) 1f else 0.48f
        pts[i * 2] = cos(a) * r * 0.2f
        pts[i * 2 + 1] = cz - sin(a) * r * 0.2f
    }
    fun star(y: Float, scale: Float): Path {
        val path = Path()
        for (i in 0 until 10) {
            val p = fxQ(u, pts[i * 2] * scale, y, cz + (pts[i * 2 + 1] - cz) * scale)
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        path.close()
        return path
    }
    translate(0f, 0.006f * u) { drawPath(star(0f, 1f), FxC.yellow.darken(0.3f)) }
    val top = star(0f, 1f)
    drawPath(top, FxC.yellow)
    drawPath(star(0f, 0.72f), FxC.yellow.lighten(0.35f))
    drawPath(top, Ink.line, style = pen.stroke)
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        val e = fxQ(u, m * 0.035f, 0f, cz + 0.015f)
        drawArc(Ink.line, 0f, 180f, false, Offset(e.x - 0.01f * u, e.y - 0.005f * u), Size(0.02f * u, 0.01f * u), style = Stroke(pen.lw, cap = StrokeCap.Round))
        drawOval(FxC.blush, Offset(e.x - 0.012f * u + m * 0.008f * u, e.y + 0.006f * u), Size(0.018f * u, 0.008f * u))
    }
    val mouth = fxQ(u, 0f, 0f, cz - 0.02f)
    drawArc(Ink.line, 0f, 180f, false, Offset(mouth.x - 0.016f * u, mouth.y - 0.008f * u), Size(0.032f * u, 0.014f * u), style = Stroke(pen.lw, cap = StrokeCap.Round))
}

/** A fluffy white sheepskin with curly wool. */
private fun DrawScope.dcSheepskin(u: Float, pen: Pen) {
    val wool = Color(0xFFFBF8F1)
    val skin = dcFloorBlob(
        u, 0f,
        -0.2f, 0.04f, -0.17f, -0.01f, -0.08f, 0.0f, 0f, -0.02f, 0.09f, 0.0f, 0.18f, -0.01f, 0.2f, 0.05f,
        0.17f, 0.12f, 0.08f, 0.15f, 0f, 0.18f, -0.09f, 0.15f, -0.18f, 0.13f,
    )
    translate(0f, 0.007f * u) { drawPath(skin, Color(0xFFD9D2C4)) }
    drawPath(skin, wool)
    val b = skin.getBounds()
    val puffs = FloatArray(14 * 3)
    for (k in 0 until 14) {
        val a = k * 2f * FX_PI / 14f
        val p = fxQ(u, cos(a) * 0.19f, 0f, 0.075f + sin(a) * 0.075f)
        puffs[k * 3] = p.x
        puffs[k * 3 + 1] = p.y
        puffs[k * 3 + 2] = (0.018f + (k % 3) * 0.004f) * u
    }
    fxCloud(wool, pen, false, *puffs)
    drawPath(skin, wool)
    clipPath(skin) {
        for (k in 0 until 16) {
            val c = Offset(b.left + b.width * fxFrac(k * 0.37f + 0.1f), b.top + b.height * fxFrac(k * 0.61f + 0.2f))
            drawArc(Color(0xFFD9D2C4), 200f + k * 23f, 200f, false, Offset(c.x - 0.008f * u, c.y - 0.005f * u), Size(0.016f * u, 0.01f * u), style = pen.thin)
        }
    }
}

// ---------------------------------------------------------------------------------------------- pictures

private val RUMLE = Look(skin = 13, height = 1.14f, hair = 3, hairColor = 1, ears = 4, top = 4, topColor = 12, bottom = 0, bottomColor = 12, shoes = 12, extra = 2)
private val PORTRAIT_ANIM = PersonAnim()

private fun DrawScope.dcPicture(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val v = f.variant.mod(6)
    if (v == 2) {
        dcCrayonDrawing(u, pen)
        return
    }
    val frame = when (v) {
        0 -> DcC.oak
        1 -> DcC.paint
        3 -> DcC.charcoal
        4 -> DcC.mustard
        else -> Color(0xFFD9A441)
    }
    val nail = fxQ(u, 0f, -0.16f, 0.014f)
    fxLine(fxQ(u, -0.05f, -0.135f, 0.006f), nail, Ink.line, pen.lw * 0.5f)
    fxLine(fxQ(u, 0.05f, -0.135f, 0.006f), nail, Ink.line, pen.lw * 0.5f)
    drawCircle(FxC.steel, 0.003f * u, nail)
    fxBox(u, -0.08f, -0.14f, 0.08f, 0f, 0.012f, frame, pen, rad = 0.004f)
    if (v == 5) {
        // A gilded frame, with little beads round the edge.
        for (k in 0 until 14) {
            val t = k / 14f
            drawCircle(frame.lighten(0.35f), 0.0035f * u, p(-0.072f + t * 0.144f, -0.133f))
            drawCircle(frame.lighten(0.35f), 0.0035f * u, p(-0.072f + t * 0.144f, -0.007f))
        }
    }
    val mat = Rect(-0.066f * u, -0.126f * u, 0.066f * u, -0.014f * u)
    drawRect(if (v == 5) Color(0xFF3A2A20) else DcC.cream, mat.topLeft, mat.size)
    val art = Rect(-0.056f * u, -0.116f * u, 0.056f * u, -0.024f * u)
    clipRect(art.left, art.top, art.right, art.bottom) { dcArt(v, art, u, pen) }
    drawRect(Ink.line, art.topLeft, art.size, style = pen.thin)
    drawLine(Color.White.copy(alpha = 0.35f), Offset(art.left + 0.01f * u, art.bottom - 0.004f * u), Offset(art.left + 0.05f * u, art.top + 0.004f * u), 0.004f * u)
    if (v == 5) {
        val plaque = Rect(-0.016f * u, -0.013f * u, 0.016f * u, -0.004f * u)
        inkedRound(plaque, 0.002f * u, FxC.brass, pen, shade = false)
    }
}

private fun DrawScope.dcArt(v: Int, r: Rect, u: Float, pen: Pen) {
    val w = r.width
    val h = r.height
    fun q(fx: Float, fy: Float) = Offset(r.left + fx * w, r.top + fy * h)
    when (v) {
        0 -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8ACBF2), Color(0xFFDDF1FB)), r.top, r.bottom), r.topLeft, r.size)
            drawPath(fxPoly(1f, r.left, q(0f, 0.7f).y, q(0.25f, 0.28f).x, q(0.25f, 0.28f).y, q(0.45f, 0.55f).x, q(0.45f, 0.55f).y, q(0.7f, 0.2f).x, q(0.7f, 0.2f).y, r.right, q(1f, 0.5f).y, r.right, r.bottom, r.left, r.bottom), Color(0xFF7F95B8))
            drawPath(fxPoly(1f, q(0.25f, 0.28f).x, q(0.25f, 0.28f).y, q(0.3f, 0.36f).x, q(0.3f, 0.36f).y, q(0.2f, 0.36f).x, q(0.2f, 0.36f).y), Color.White)
            drawPath(fxPoly(1f, q(0.7f, 0.2f).x, q(0.7f, 0.2f).y, q(0.77f, 0.3f).x, q(0.77f, 0.3f).y, q(0.63f, 0.3f).x, q(0.63f, 0.3f).y), Color.White)
            drawRect(Color(0xFF3F7FC8), q(0f, 0.68f), Size(w, h * 0.32f))
            drawPath(blobPath(r.left - w * 0.1f, r.bottom, r.left, q(0f, 0.6f).y, q(0.3f, 0.75f).x, q(0.3f, 0.75f).y, q(0.35f, 1f).x, r.bottom + h * 0.1f), Color(0xFF5DAA5A))
            drawPath(blobPath(q(0.75f, 1f).x, r.bottom + h * 0.1f, q(0.8f, 0.72f).x, q(0.8f, 0.72f).y, r.right + w * 0.1f, q(1f, 0.6f).y, r.right + w * 0.1f, r.bottom), Color(0xFF4E9A52))
            val boat = q(0.55f, 0.8f)
            drawPath(fxPoly(1f, boat.x - 0.008f * u, boat.y, boat.x + 0.008f * u, boat.y, boat.x + 0.005f * u, boat.y + 0.004f * u, boat.x - 0.005f * u, boat.y + 0.004f * u), FxC.red)
            fxLine(Offset(boat.x - 0.02f * u, boat.y + 0.008f * u), Offset(boat.x + 0.02f * u, boat.y + 0.008f * u), Color.White.copy(alpha = 0.6f), pen.lw * 0.5f)
            drawCircle(Color(0xFFFFE680), 0.006f * u, q(0.15f, 0.2f))
        }
        1 -> {
            drawRect(Color(0xFF3FB3A1), r.topLeft, r.size)
            val c = q(0.5f, 0.58f)
            val cat = Color(0xFFF5A04A)
            drawOval(cat.darken(0.1f), Offset(c.x - 0.03f * u, c.y + 0.012f * u), Size(0.06f * u, 0.04f * u))
            for (s in 0..1) {
                val m = if (s == 0) -1f else 1f
                drawPath(fxPoly(1f, c.x + m * 0.012f * u, c.y - 0.02f * u, c.x + m * 0.026f * u, c.y - 0.034f * u, c.x + m * 0.028f * u, c.y - 0.008f * u), cat)
                drawPath(fxPoly(1f, c.x + m * 0.016f * u, c.y - 0.018f * u, c.x + m * 0.024f * u, c.y - 0.028f * u, c.x + m * 0.025f * u, c.y - 0.012f * u), FxC.blush)
            }
            drawCircle(cat, 0.026f * u, c)
            drawCircle(Color.White, 0.012f * u, Offset(c.x, c.y + 0.012f * u))
            for (s in 0..1) {
                val m = if (s == 0) -1f else 1f
                drawOval(Color(0xFF7ED957), Offset(c.x + m * 0.011f * u - 0.005f * u, c.y - 0.008f * u), Size(0.01f * u, 0.009f * u))
                drawOval(Ink.line, Offset(c.x + m * 0.011f * u - 0.0015f * u, c.y - 0.008f * u), Size(0.003f * u, 0.009f * u))
                fxLine(Offset(c.x + m * 0.012f * u, c.y + 0.008f * u), Offset(c.x + m * 0.035f * u, c.y + 0.004f * u), Ink.line, pen.lw * 0.4f)
                fxLine(Offset(c.x + m * 0.012f * u, c.y + 0.011f * u), Offset(c.x + m * 0.035f * u, c.y + 0.013f * u), Ink.line, pen.lw * 0.4f)
            }
            drawPath(fxPoly(1f, c.x - 0.003f * u, c.y + 0.004f * u, c.x + 0.003f * u, c.y + 0.004f * u, c.x, c.y + 0.008f * u), FxC.pink)
            val bow = Offset(c.x, c.y + 0.03f * u)
            drawPath(fxPoly(1f, bow.x, bow.y, bow.x - 0.012f * u, bow.y - 0.006f * u, bow.x - 0.012f * u, bow.y + 0.006f * u), FxC.red)
            drawPath(fxPoly(1f, bow.x, bow.y, bow.x + 0.012f * u, bow.y - 0.006f * u, bow.x + 0.012f * u, bow.y + 0.006f * u), FxC.red)
        }
        3 -> {
            drawRect(Color(0xFFF4EEE2), r.topLeft, r.size)
            drawCircle(Color(0xFFE8B04A), h * 0.34f, q(0.3f, 0.4f))
            drawPath(fxPoly(1f, q(0.5f, 0.95f).x, q(0.5f, 0.95f).y, q(0.8f, 0.2f).x, q(0.8f, 0.2f).y, q(1.05f, 0.95f).x, q(1.05f, 0.95f).y), Color(0xFFD9774F))
            drawRect(Color(0xFF2F6FB8), q(0.12f, 0.62f), Size(w * 0.3f, h * 0.3f))
            drawArc(Color(0xFF2B2140), 180f, 180f, true, q(0.45f, 0.1f), Size(w * 0.3f, h * 0.36f))
            drawCircle(Color(0xFF9CB8A0), h * 0.12f, q(0.75f, 0.72f))
            fxLine(q(0.05f, 0.9f), q(0.55f, 0.55f), Ink.line, pen.lw * 0.8f)
        }
        4 -> {
            drawRect(Color(0xFF8ACBF2), r.topLeft, r.size)
            val c = q(0.5f, 0.5f)
            val t = pen.t * 0.3f
            for (k in 0 until 12) {
                val a = k * FX_PI / 6f + t
                drawLine(Color(0xFFFFB02E), Offset(c.x + cos(a) * 0.024f * u, c.y + sin(a) * 0.024f * u), Offset(c.x + cos(a) * 0.038f * u, c.y + sin(a) * 0.038f * u), 0.005f * u, StrokeCap.Round)
            }
            drawCircle(Color(0xFFFFD84A), 0.022f * u, c)
            for (s in 0..1) {
                val m = if (s == 0) -1f else 1f
                drawCircle(Ink.line, 0.003f * u, Offset(c.x + m * 0.008f * u, c.y - 0.004f * u))
                drawOval(FxC.blush, Offset(c.x + m * 0.012f * u - 0.005f * u, c.y + 0.003f * u), Size(0.01f * u, 0.005f * u))
            }
            drawArc(Ink.line, 20f, 140f, false, Offset(c.x - 0.009f * u, c.y - 0.004f * u), Size(0.018f * u, 0.014f * u), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
        }
        else -> {
            // Rumle the troll, sitting for his portrait.
            drawRect(safeRadialGradient(0f to Color(0xFF4F6B4A), 1f to Color(0xFF1F2E24), center = q(0.4f, 0.35f), radius = w), r.topLeft, r.size)
            val h0 = 0.13f * u
            val small = Pen(pen.lw * 0.45f, 0f, 0f)
            translate(r.center.x, r.top + h * 0.3f + 0.7f * h0) {
                drawPerson(Species.FOLK, RUMLE, Pose.STAND, PORTRAIT_ANIM, h0, small)
            }
        }
    }
}

/** A child's crayon drawing of the family, taped to the wall. */
private fun DrawScope.dcCrayonDrawing(u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    rotate(-4f, p(0f, -0.07f)) {
        val sheet = Rect(-0.075f * u, -0.135f * u, 0.075f * u, -0.005f * u)
        drawRect(Ink.shadow, Offset(sheet.left + 0.004f * u, sheet.top + 0.004f * u), sheet.size)
        drawRect(Color.White, sheet.topLeft, sheet.size)
        drawRect(Ink.line, sheet.topLeft, sheet.size, style = pen.thin)
        fun crayon(color: Color, vararg xy: Float) {
            val path = Path()
            var i = 0
            while (i < xy.size) {
                if (i == 0) path.moveTo(xy[i] * u, xy[i + 1] * u) else path.lineTo(xy[i] * u, xy[i + 1] * u)
                i += 2
            }
            drawPath(path, color, style = Stroke(0.0035f * u, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        // The sun in the corner, grass, and four smiling people holding hands.
        drawCircle(Color(0xFFFFC83D), 0.012f * u, p(0.05f, -0.112f))
        for (k in 0 until 7) {
            val a = k * FX_PI / 3.5f
            crayon(Color(0xFFFFC83D), 0.05f + cos(a) * 0.016f, -0.112f + sin(a) * 0.016f, 0.05f + cos(a) * 0.024f, -0.112f + sin(a) * 0.024f)
        }
        crayon(Color(0xFF3BC46B), -0.07f, -0.02f, -0.05f, -0.024f, -0.03f, -0.018f, -0.01f, -0.025f, 0.01f, -0.019f, 0.03f, -0.025f, 0.05f, -0.018f, 0.07f, -0.023f)
        val people = floatArrayOf(-0.05f, 0.03f, -0.018f, 0.026f, 0.012f, 0.018f, 0.038f, 0.015f)
        val colors = arrayOf(Color(0xFF2F6FB8), Color(0xFFE85D75), Color(0xFF8B5CF6), Color(0xFFFF9F43))
        for (k in 0 until 4) {
            val x = people[k * 2]
            val size = people[k * 2 + 1]
            val feet = -0.026f
            val hip = feet - size * 0.9f
            val neck = hip - size * 0.9f
            crayon(colors[k], x - size * 0.4f, feet, x, hip, x + size * 0.4f, feet)
            crayon(colors[k], x, hip, x, neck)
            crayon(colors[k], x - size * 0.7f, neck + size * 0.3f, x + size * 0.7f, neck + size * 0.3f)
            drawCircle(Color(0xFFD9986B), size * 0.45f * u, p(x, neck - size * 0.45f))
            drawCircle(Ink.line, size * 0.45f * u, p(x, neck - size * 0.45f), style = Stroke(0.002f * u))
            drawArc(Ink.line, 20f, 140f, false, p(x - size * 0.22f, neck - size * 0.6f), Size(size * 0.44f * u, size * 0.3f * u), style = Stroke(0.0018f * u))
        }
        crayon(Color(0xFFE85D75), 0.058f, -0.06f, 0.052f, -0.066f, 0.058f, -0.058f, 0.064f, -0.066f, 0.058f, -0.06f)
    }
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        rotate(m * 30f, p(m * 0.07f, -0.132f)) {
            drawRect(Color(0xCCFFD84A), Offset((m * 0.07f - 0.015f) * u, -0.137f * u), Size(0.03f * u, 0.01f * u))
        }
    }
}

// ---------------------------------------------------------------------------------------------- aquarium

private fun DrawScope.dcFish(c: Offset, s: Float, color: Color, dir: Float, pen: Pen) {
    val tail = Path().apply {
        moveTo(c.x - dir * s * 0.8f, c.y)
        lineTo(c.x - dir * s * 1.6f, c.y - s * 0.65f)
        lineTo(c.x - dir * s * 1.6f, c.y + s * 0.65f)
        close()
    }
    drawPath(tail, color.darken(0.15f))
    drawPath(tail, Ink.line, style = pen.thin)
    drawOval(color, Offset(c.x - s, c.y - s * 0.6f), Size(s * 2f, s * 1.2f))
    drawOval(Ink.line, Offset(c.x - s, c.y - s * 0.6f), Size(s * 2f, s * 1.2f), style = pen.thin)
    drawLine(Color.White.copy(alpha = 0.8f), Offset(c.x - dir * s * 0.1f, c.y - s * 0.55f), Offset(c.x - dir * s * 0.1f, c.y + s * 0.55f), s * 0.22f)
    drawCircle(Color.White, s * 0.22f, Offset(c.x + dir * s * 0.5f, c.y - s * 0.12f))
    drawCircle(Ink.line, s * 0.12f, Offset(c.x + dir * s * 0.55f, c.y - s * 0.12f))
}

private fun DrawScope.dcAquarium(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.14f
    dcShadow(u, 0.3f, d)
    fxGlow(q(0f, -0.23f, d * 0.5f), 0.26f * u, Color(0xFF7FE6F2), 0.12f + 0.35f * pen.night)
    // The stand.
    fxBox(u, -0.15f, -0.14f, 0.15f, 0f, d, DcC.oak, pen, rad = 0.004f)
    inkedRound(Rect(-0.14f * u, -0.13f * u, -0.004f * u, -0.012f * u), 0.004f * u, DcC.oak, pen, shade = false)
    inkedRound(Rect(0.004f * u, -0.13f * u, 0.14f * u, -0.012f * u), 0.004f * u, DcC.oak, pen, shade = false)
    drawCircle(FxC.brass, 0.004f * u, p(-0.016f, -0.07f))
    drawCircle(FxC.brass, 0.004f * u, p(0.016f, -0.07f))
    fxGrain(Rect(-0.14f * u, -0.13f * u, 0.14f * u, -0.012f * u), DcC.oak, pen, 2)
    // The tank: back wall, water, gravel, plants, fish, bubbles, then the front glass.
    val l = -0.14f
    val r = 0.14f
    val bottom = -0.145f
    val top = -0.32f
    val water = -0.305f
    val back = fxFront(u, l, top, r, bottom, d - 0.01f)
    drawRect(Color(0xFF2E7F9A), back.topLeft, back.size)
    val front = Rect(l * u, top * u, r * u, bottom * u)
    val tank = Path().apply {
        val a = q(l, top, 0f)
        val b = q(l, top, d - 0.01f)
        val c = q(r, top, d - 0.01f)
        val e = q(r, bottom, d - 0.01f)
        val g = q(r, bottom, 0f)
        val i = q(l, bottom, 0f)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        lineTo(e.x, e.y)
        lineTo(g.x, g.y)
        lineTo(i.x, i.y)
        close()
    }
    clipPath(tank) {
        val wet = Path().apply {
            val a = q(l, water, 0f)
            val b = q(l, water, d - 0.01f)
            val c = q(r, water, d - 0.01f)
            val e = q(r, bottom, d - 0.01f)
            val g = q(r, bottom, 0f)
            val i = q(l, bottom, 0f)
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            lineTo(e.x, e.y)
            lineTo(g.x, g.y)
            lineTo(i.x, i.y)
            close()
        }
        drawPath(wet, Brush.verticalGradient(listOf(Color(0xFF7FD6EA), Color(0xFF2E8FB0)), (water - 0.05f) * u, bottom * u))
        fxGlow(q(-0.05f, -0.28f, 0.03f), 0.12f * u, Color.White, 0.35f)
        // Gravel and a little treasure chest.
        fxFace(fxFlat(u, l, r, bottom, 0f, d - 0.01f), Color(0xFFE8C98A), pen)
        for (k in 0 until 16) drawCircle(if (k % 3 == 0) Color(0xFFFF9F68) else Color(0xFFC9A96A), 0.0035f * u, q(l + 0.01f + fxFrac(k * 0.37f) * 0.26f, bottom, 0.01f + fxFrac(k * 0.53f) * 0.11f))
        fxBox(u, 0.06f, -0.175f, 0.1f, bottom, 0.025f, FxC.woodDark, pen, rad = 0.004f, z = 0.07f)
        val lid = fxFront(u, 0.06f, -0.175f, 0.1f, -0.165f, 0.07f)
        drawRect(FxC.yellow, Offset(lid.center.x - 0.003f * u, lid.top), Size(0.006f * u, lid.height))
        // Swaying plants.
        for (k in 0 until 4) {
            val x = -0.11f + k * 0.055f
            val z = if (k % 2 == 0) 0.1f else 0.05f
            val base = q(x, bottom, z)
            val sway = sin(t * 1.3f + k) * 0.012f * u
            val leaf = Path().apply {
                moveTo(base.x - 0.006f * u, base.y)
                quadraticTo(base.x - 0.012f * u + sway, base.y - 0.07f * u, base.x + sway * 1.5f, base.y - (0.11f + k % 2 * 0.03f) * u)
                quadraticTo(base.x + 0.012f * u + sway, base.y - 0.07f * u, base.x + 0.006f * u, base.y)
                close()
            }
            val col = if (k % 2 == 0) FxC.spruceLight else FxC.leaf
            drawPath(leaf, col)
            drawPath(leaf, Ink.line, style = pen.thin)
        }
        // Fish swimming to and fro.
        val fishCols = arrayOf(Color(0xFFFF8A3D), FxC.yellow, Color(0xFF4AB3FF))
        for (k in 0 until 3) {
            val ph = t * (0.45f + k * 0.12f) + k * 2.1f
            val x = sin(ph) * 0.09f
            val dir = if (cos(ph) >= 0f) 1f else -1f
            val y = -0.26f + k * 0.035f + sin(t * 1.7f + k) * 0.006f
            dcFish(q(x, y, 0.03f + k * 0.035f), (0.011f - k * 0.0015f) * u, fishCols[k], dir, pen)
        }
        // Bubbles from the air stone.
        val stone = q(-0.1f, bottom, 0.1f)
        drawOval(FxC.stone, Offset(stone.x - 0.008f * u, stone.y - 0.004f * u), Size(0.016f * u, 0.006f * u))
        for (k in 0 until 5) {
            val ph = fxFrac(t * 0.5f + k * 0.2f)
            val c = Offset(stone.x + sin(ph * 9f + k) * 0.004f * u, stone.y - ph * (stone.y - water * u + 0.01f * u) - 0.004f * u)
            drawCircle(Color.White.copy(alpha = 0.8f), (0.003f + ph * 0.002f) * u, c, style = pen.thin)
        }
        // The water's surface.
        val s = fxFlat(u, l, r, water, 0f, d - 0.01f)
        drawPath(s, Color(0x559FE8F5))
        fxLine(q(l, water, 0f), q(r, water, 0f), Color.White.copy(alpha = 0.7f), pen.lw)
    }
    drawRect(Color(0x1FCFEFFF), front.topLeft, front.size)
    fxLine(p(l + 0.015f, bottom - 0.02f), p(l + 0.05f, top + 0.02f), Color.White.copy(alpha = 0.45f), 0.005f * u)
    fxLine(p(l + 0.035f, bottom - 0.01f), p(l + 0.06f, top + 0.06f), Color.White.copy(alpha = 0.25f), 0.003f * u)
    drawPath(tank, Ink.line, style = pen.stroke)
    fxLine(p(l, top), p(l, bottom), Ink.line, pen.lw)
    fxLine(p(r, top), p(r, bottom), Ink.line, pen.lw)
    fxLine(p(r, top), q(r, top, d - 0.01f), Ink.line, pen.lw * 0.8f)
    // The lid with its lamp: the top face is where things stand.
    fxBox(u, -0.145f, -0.34f, 0.145f, -0.32f, d, DcC.charcoal, pen, rad = 0.004f, top = DcC.charcoal.lighten(0.15f))
    val lamp = fxFront(u, -0.12f, -0.333f, 0.12f, -0.326f, 0f)
    drawRoundRect(lerp(Color(0xFFBFEFFF), Color.White, pen.night), lamp.topLeft, lamp.size, CornerRadius(0.003f * u))
}

// ---------------------------------------------------------------------------------------------- seats

private val BEANBAG_COLORS = arrayOf(FxC.mustard, FxC.dusty, FxC.pink)

private fun DrawScope.dcBeanbag(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val c = BEANBAG_COLORS[f.variant.mod(BEANBAG_COLORS.size)]
    dcShadow(u, 0.2f, 0.12f)
    // A soft hump behind the sitter, then the squashy front with a dent where you sit.
    val o = q(0f, 0f, 0.07f)
    translate(o.x, o.y) {
        val hump = fxBlob(u, -0.075f, -0.03f, -0.085f, -0.09f, -0.04f, -0.13f, 0.03f, -0.135f, 0.085f, -0.095f, 0.08f, -0.03f, 0f, -0.01f)
        inked(hump, c.darken(0.08f), pen)
        val seam = Path().apply {
            moveTo(-0.05f * u, -0.12f * u)
            quadraticTo(0f, -0.1f * u, 0.05f * u, -0.12f * u)
        }
        drawPath(seam, c.darken(0.3f), style = pen.thin)
    }
    val body = fxBlob(u, -0.092f, 0.002f, -0.1f, -0.042f, -0.07f, -0.074f, 0f, -0.057f, 0.07f, -0.076f, 0.1f, -0.045f, 0.094f, 0.002f, 0f, 0.008f)
    inked(body, c, pen)
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        val crease = Path().apply {
            moveTo(m * 0.06f * u, -0.066f * u)
            quadraticTo(m * 0.045f * u, -0.045f * u, m * 0.05f * u, -0.028f * u)
        }
        drawPath(crease, c.darken(0.25f), style = pen.thin)
    }
    drawPath(Path().apply {
        moveTo(-0.07f * u, -0.01f * u)
        quadraticTo(0f, 0.004f * u, 0.07f * u, -0.012f * u)
    }, c.darken(0.25f), style = pen.thin)
    // A little label with a smile.
    val tag = Rect(0.052f * u, -0.03f * u, 0.07f * u, -0.018f * u)
    inkedRound(tag, 0.002f * u, Color.White, pen, shade = false)
    drawArc(Ink.line, 20f, 140f, false, Offset(tag.left + 0.004f * u, tag.top + 0.002f * u), Size(0.01f * u, 0.006f * u), style = Stroke(pen.lw * 0.5f))
    shine(Offset(-0.045f * u, -0.055f * u), 0.024f * u, 0.008f * u, 0.3f)
}

private val ARMCHAIR_COLORS = arrayOf(FxC.mustard, FxC.dusty)

private fun DrawScope.dcArmchair(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val c = ARMCHAIR_COLORS[f.variant.mod(ARMCHAIR_COLORS.size)]
    val d = 0.15f
    dcShadow(u, 0.2f, d)
    for (s in 0..1) {
        val x = if (s == 0) -0.085f else 0.085f
        fxBox(u, x - 0.007f, -0.024f, x + 0.007f, 0f, 0.014f, DcC.oak, pen, z = d - 0.02f)
    }
    // A tall, rounded back with a pleated cushion.
    fxBox(u, -0.1f, -0.2f, 0.1f, -0.045f, 0.045f, c.darken(0.06f), pen, rad = 0.03f, z = d - 0.045f)
    fxBox(u, -0.075f, -0.18f, 0.075f, -0.078f, 0.03f, c, pen, rad = 0.025f, z = d - 0.075f)
    val bz = d - 0.075f
    for (k in 0 until 3) {
        val x = -0.04f + k * 0.04f
        drawCircle(c.darken(0.3f), 0.003f * u, q(x, -0.135f, bz))
    }
    // The left arm, the seat, the knitted blanket and the right arm.
    fxBox(u, -0.105f, -0.125f, -0.075f, -0.022f, d, c.darken(0.02f), pen, rad = 0.014f)
    fxBox(u, -0.078f, -0.05f, 0.078f, -0.022f, d - 0.02f, c.darken(0.12f), pen, rad = 0.006f)
    fxBox(u, -0.076f, -0.08f, 0.076f, -0.05f, d - 0.075f, c.lighten(0.06f), pen, rad = 0.012f)
    fxLine(Offset(-0.066f * u, -0.074f * u), Offset(0.066f * u, -0.074f * u), c.lighten(0.35f), pen.lw * 0.6f)
    fxBox(u, 0.075f, -0.125f, 0.105f, -0.022f, d, c.darken(0.02f), pen, rad = 0.014f)
    val drape = Path().apply {
        val a = q(0.072f, -0.127f, 0.03f)
        val b = q(0.108f, -0.127f, 0.03f)
        val e = q(0.108f, -0.127f, 0.09f)
        val g = q(0.072f, -0.127f, 0.09f)
        moveTo(a.x, a.y)
        lineTo(g.x, g.y)
        lineTo(e.x, e.y)
        lineTo(b.x, b.y)
        lineTo(b.x + 0.002f * u, b.y + 0.07f * u)
        quadraticTo(b.x - 0.012f * u, b.y + 0.078f * u, a.x - 0.002f * u, a.y + 0.066f * u)
        close()
    }
    val blanket = if (f.variant.mod(2) == 0) DcC.sage else DcC.blush
    inked(drape, blanket, pen, outline = false)
    clipPath(drape) { fxKnit(drape.getBounds(), blanket.darken(0.25f), 4, 8, pen.lw * 0.5f) }
    drawPath(drape, Ink.line, style = pen.stroke)
    for (s in 0..1) {
        val x = if (s == 0) -0.085f else 0.085f
        fxBox(u, x - 0.008f, -0.024f, x + 0.008f, 0f, 0.014f, DcC.oak, pen)
    }
}

// ---------------------------------------------------------------------------------------------- bunk bed

private const val BUNK_D = 0.16f

private fun DrawScope.dcBunkBed(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = BUNK_D
    val frame = DcC.paint
    dcShadow(u, 0.4f, d)
    // The back posts, then each bunk from the bottom up.
    for (s in 0..1) {
        val x = if (s == 0) -0.19f else 0.19f
        fxBox(u, x - 0.012f, -0.42f, x + 0.012f, 0f, 0.02f, frame, pen, rad = 0.004f, z = d - 0.02f)
    }
    for (level in 0..1) {
        val top = if (level == 0) -0.1f else -0.3f
        val board = if (level == 0) DcC.dusty else DcC.mustard
        // A painted board at the head end.
        val hb = fxDeep(u, -0.178f, top - 0.07f, top + 0.02f, 0.004f, d - 0.004f, 0.012f)
        fxFace(hb, board, pen)
        val hc = q(-0.178f, top - 0.035f, d * 0.5f)
        val mark = if (level == 0) fxHeart(hc.x, hc.y, 0.01f * u) else starPath(hc, 0.012f * u, 0.005f * u)
        drawPath(mark, Color.White.copy(alpha = 0.85f))
        drawPath(mark, Ink.line, style = pen.thin)
        fxBox(u, -0.178f, top + 0.02f, 0.178f, top + 0.065f, d, frame, pen, rad = 0.004f)
        fxBox(u, -0.172f, top, 0.18f, top + 0.02f, d - 0.006f, FxC.porcelain, pen, rad = 0.006f)
        fxBox(u, -0.168f, top - 0.04f, -0.07f, top, 0.11f, FxC.porcelain, pen, rad = 0.016f, z = 0.03f)
        fxLine(q(-0.155f, top - 0.018f, 0.03f), q(-0.085f, top - 0.02f, 0.03f), board, 0.004f * u)
    }
    // The front posts and the ladder hooked on at the foot end.
    for (s in 0..1) {
        val x = if (s == 0) -0.19f else 0.19f
        fxBox(u, x - 0.012f, -0.42f, x + 0.012f, 0f, 0.02f, frame, pen, rad = 0.004f)
        inkedCircle(Offset(x * u, -0.424f * u), 0.012f * u, DcC.oak, pen)
    }
    val lz = d * 0.5f
    for (s in 0..1) {
        val x0 = if (s == 0) 0.205f else 0.245f
        capsule(q(x0 + 0.02f, 0f, lz), q(x0, -0.33f, lz), 0.008f * u, DcC.oak, pen)
    }
    for (k in 0 until 5) {
        val y = -0.05f - k * 0.065f
        val shift = 0.02f * (1f + y / 0.33f)
        capsule(q(0.205f + shift, y, lz), q(0.245f + shift, y, lz), 0.006f * u, DcC.oak.darken(0.05f), pen)
    }
}

/** The duvets over both sleepers. */
private fun DrawScope.dcBunkFront(f: Fixture, u: Float, pen: Pen) {
    dcDuvet(u, -0.1f, DcC.dusty.lighten(0.15f), DcC.sage, pen)
    dcDuvet(u, -0.3f, FxC.yellow.lighten(0.2f), DcC.blush, pen)
}

/** A soft quilt lying on a mattress whose top is at [top]; shoulders and head stay out on the left. */
private fun DrawScope.dcDuvet(u: Float, top: Float, cover: Color, throwColor: Color, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = BUNK_D - 0.012f
    val x0 = 0.036f
    val x1 = 0.186f
    val puff = top - 0.08f
    val hem = top + 0.045f
    val s0 = q(x1, puff, 0f)
    val s1 = q(x1, puff, d)
    val s2 = q(x1, top, d)
    val s3 = q(x1, hem, 0f)
    fxFace(fxQuad(s0.x, s0.y, s1.x, s1.y, s2.x, s2.y, s3.x, s3.y, 0.012f * u), cover.darken(0.2f), pen)
    val topFace = fxFlat(u, x0, x1, puff, 0f, d, 0.02f)
    drawPath(topFace, cover.lighten(0.14f))
    clipPath(topFace) {
        for (j in 0 until 3) {
            for (i in 0 until 4) {
                val c = q(0.06f + i * 0.035f + (if (j % 2 == 0) 0f else 0.017f), puff, 0.03f + j * 0.045f)
                drawCircle(Color.White.copy(alpha = 0.8f), 0.004f * u, c)
            }
        }
        fxFace(fxFlat(u, 0.14f, x1 + 0.01f, puff - 0.002f, -0.01f, d + 0.01f), throwColor.lighten(0.12f), pen)
        fxFace(fxFlat(u, x0 - 0.004f, x0 + 0.026f, puff - 0.002f, -0.01f, d + 0.01f), FxC.porcelain, pen)
    }
    drawPath(topFace, Ink.line, style = pen.stroke)
    val front = Path().apply {
        moveTo(x0 * u, (puff + 0.012f) * u)
        quadraticTo(x0 * u, puff * u, (x0 + 0.02f) * u, puff * u)
        lineTo((x1 - 0.02f) * u, puff * u)
        quadraticTo(x1 * u, puff * u, x1 * u, (puff + 0.02f) * u)
        lineTo((x1 + 0.002f) * u, (hem - 0.008f) * u)
        quadraticTo(x1 * u, hem * u, (x1 - 0.012f) * u, hem * u)
        quadraticTo((x0 + x1) / 2f * u, (hem + 0.006f) * u, (x0 + 0.02f) * u, (hem - 0.002f) * u)
        quadraticTo((x0 - 0.004f) * u, (hem - 0.004f) * u, (x0 - 0.005f) * u, (hem - 0.02f) * u)
        close()
    }
    inked(front, cover, pen, outline = false)
    clipPath(front) {
        for (j in 0 until 3) {
            for (i in 0 until 4) {
                val dx = 0.058f + i * 0.035f + (if (j % 2 == 0) 0f else 0.017f)
                drawCircle(Color.White.copy(alpha = 0.8f), 0.0042f * u, p(dx, puff + 0.018f + j * 0.035f))
            }
        }
        val throwRect = Rect(0.14f * u, (puff - 0.01f) * u, 0.2f * u, (hem + 0.01f) * u)
        drawRect(throwColor, throwRect.topLeft, throwRect.size)
        fxKnit(throwRect, throwColor.darken(0.25f), 3, 7, pen.lw * 0.5f)
        drawLine(Ink.line, p(0.14f, puff - 0.01f), p(0.14f, hem + 0.01f), pen.lw * 0.7f)
        drawRect(FxC.porcelain, p(x0 - 0.01f, puff - 0.01f), Size(0.036f * u, (hem - puff + 0.02f) * u))
        drawLine(Ink.line, p(x0 + 0.026f, puff - 0.01f), p(x0 + 0.024f, hem + 0.01f), pen.lw * 0.7f)
    }
    drawPath(front, Ink.line, style = pen.stroke)
}

// ---------------------------------------------------------------------------------------------- toy box

private fun DrawScope.dcToyBox(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val paint = DcC.mint
    val d = 0.11f
    dcShadow(u, 0.2f, d)
    if (f.open) {
        // The lid stands up at the back, and toys pile up out of the top.
        fxBox(u, -0.104f, -0.225f, 0.104f, -0.112f, 0.016f, paint.darken(0.08f), pen, rad = 0.006f, z = d - 0.004f)
        val lid = fxFront(u, -0.094f, -0.215f, 0.094f, -0.12f, d - 0.004f)
        inkedRound(lid, 0.004f * u, DcC.oak, pen, shade = false)
        for (k in 0 until 3) drawPath(starPath(Offset(lid.left + lid.width * (0.25f + k * 0.25f), lid.center.y), 0.009f * u, 0.004f * u), FxC.yellow)
        val t = pen.t
        val bear = q(-0.045f, -0.13f, 0.06f)
        val bob = sin(t * 2f) * 0.002f * u
        for (s in 0..1) inkedCircle(Offset(bear.x + (s * 2 - 1) * 0.02f * u, bear.y - 0.022f * u + bob), 0.01f * u, Color(0xFFB9824C), pen)
        inkedCircle(Offset(bear.x, bear.y + bob), 0.026f * u, Color(0xFFB9824C), pen)
        inkedOval(Rect(bear.x - 0.012f * u, bear.y + 0.002f * u + bob, bear.x + 0.012f * u, bear.y + 0.016f * u + bob), Color(0xFFE3C08F), pen, shade = false)
        drawCircle(Ink.line, 0.003f * u, Offset(bear.x - 0.009f * u, bear.y - 0.006f * u + bob))
        drawCircle(Ink.line, 0.003f * u, Offset(bear.x + 0.009f * u, bear.y - 0.006f * u + bob))
        drawCircle(Ink.line, 0.003f * u, Offset(bear.x, bear.y + 0.006f * u + bob))
        val rocket = q(0.045f, -0.12f, 0.07f)
        rotate(18f, rocket) {
            inkedRound(Rect(rocket.x - 0.01f * u, rocket.y - 0.06f * u, rocket.x + 0.01f * u, rocket.y), 0.008f * u, FxC.paint, pen)
            drawPath(fxPoly(1f, rocket.x - 0.01f * u, rocket.y - 0.05f * u, rocket.x, rocket.y - 0.072f * u, rocket.x + 0.01f * u, rocket.y - 0.05f * u), FxC.red)
            drawCircle(FxC.sky, 0.004f * u, Offset(rocket.x, rocket.y - 0.035f * u))
        }
        inkedCircle(q(0.015f, -0.118f, 0.04f), 0.02f * u, FxC.red, pen)
        drawArc(Color.White, 200f, 60f, false, Offset(q(0.015f, -0.118f, 0.04f).x - 0.014f * u, q(0.015f, -0.118f, 0.04f).y - 0.014f * u), Size(0.028f * u, 0.028f * u), style = Stroke(0.004f * u))
        fxBox(u, 0.055f, -0.13f, 0.08f, -0.108f, 0.02f, FxC.yellow, pen, rad = 0.003f, z = 0.02f)
    }
    for (s in 0..1) {
        val x = if (s == 0) -0.08f else 0.08f
        fxBox(u, x - 0.01f, -0.012f, x + 0.01f, 0f, 0.014f, DcC.oak, pen, z = d - 0.02f)
    }
    fxBox(u, -0.1f, -0.11f, 0.1f, -0.008f, d, paint, pen, rad = 0.006f)
    if (f.open) {
        fxFace(fxFlat(u, -0.09f, 0.09f, -0.11f, 0.01f, d - 0.01f), paint.darken(0.55f), pen)
        fxHollow(u, -0.09f, -0.102f, 0.09f, -0.015f, d - 0.02f, DcC.oak.darken(0.1f), pen)
        clipRect(-0.09f * u, -0.102f * u, 0.09f * u, -0.015f * u) {
            fxBox(u, -0.08f, -0.04f, -0.05f, -0.015f, 0.03f, FxC.fjord, pen, rad = 0.003f, z = 0.06f)
            inkedCircle(q(0.05f, -0.03f, 0.06f), 0.014f * u, FxC.green, pen)
        }
    } else {
        fxBox(u, -0.104f, -0.12f, 0.104f, -0.104f, d + 0.004f, paint.darken(0.08f), pen, rad = 0.006f)
        drawLine(Color.White.copy(alpha = 0.6f), p(-0.1f, -0.112f), p(0.1f, -0.112f), 0.002f * u)
    }
    // Polka dots and a big painted star on the front.
    val dots = arrayOf(FxC.yellow, FxC.pink, FxC.sky, FxC.flame2)
    for (k in 0 until 8) {
        val x = -0.085f + (k % 4) * 0.057f
        val y = if (f.open) -0.004f - (k / 4) * 0.004f else -0.085f + (k / 4) * 0.055f
        if (f.open && k >= 4) continue
        drawCircle(dots[k % 4], 0.006f * u, p(x, y - (if (f.open) 0.004f else 0f)))
    }
    if (!f.open) {
        val star = starPath(p(0f, -0.058f), 0.022f * u, 0.01f * u)
        drawPath(star, FxC.yellow)
        drawPath(star, Ink.line, style = pen.thin)
    }
    for (s in 0..1) {
        val x = if (s == 0) -0.08f else 0.08f
        fxBox(u, x - 0.01f, -0.012f, x + 0.01f, 0f, 0.014f, DcC.oak, pen)
    }
}

// ---------------------------------------------------------------------------------------------- desk

private fun DrawScope.dcDesk(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.14f
    val t = pen.t
    dcShadow(u, 0.3f, d)
    for (s in 0..1) {
        val x = if (s == 0) -0.135f else 0.135f
        fxBox(u, x - 0.008f, -0.135f, x + 0.008f, 0f, 0.016f, DcC.oak, pen, z = d - 0.022f)
    }
    // A little lamp and a pencil cup at the back of the top.
    val lampBase = q(0.1f, -0.15f, d - 0.03f)
    fxCyl(lampBase.x, lampBase.y, lampBase.y - 0.006f * u, 0.016f * u, 0.014f * u, DcC.charcoal, pen)
    val elbow = Offset(lampBase.x - 0.02f * u, lampBase.y - 0.06f * u)
    val head = Offset(lampBase.x - 0.05f * u, lampBase.y - 0.075f * u)
    capsule(Offset(lampBase.x, lampBase.y - 0.006f * u), elbow, 0.004f * u, DcC.charcoal, pen)
    capsule(elbow, head, 0.004f * u, DcC.charcoal, pen)
    fxGlow(Offset(head.x - 0.006f * u, head.y + 0.03f * u), 0.07f * u, FxC.warm, 0.15f + 0.5f * pen.night)
    rotate(-30f, head) {
        val shade = Path().apply {
            moveTo(head.x - 0.008f * u, head.y - 0.008f * u)
            lineTo(head.x + 0.008f * u, head.y - 0.008f * u)
            lineTo(head.x + 0.018f * u, head.y + 0.012f * u)
            quadraticTo(head.x, head.y + 0.016f * u, head.x - 0.018f * u, head.y + 0.012f * u)
            close()
        }
        inked(shade, FxC.red, pen)
        drawOval(Color(0xFFFFF4C8), Offset(head.x - 0.012f * u, head.y + 0.01f * u), Size(0.024f * u, 0.006f * u))
    }
    val cup = q(-0.105f, -0.15f, d - 0.035f)
    val pencils = arrayOf(FxC.red, FxC.yellow, FxC.fjord, FxC.green)
    for (k in 0 until 4) {
        val a = (k - 1.5f) * 0.25f
        val tip = Offset(cup.x + sin(a) * 0.04f * u, cup.y - 0.03f * u - cos(a) * 0.035f * u)
        capsule(Offset(cup.x, cup.y - 0.02f * u), tip, 0.004f * u, pencils[k], pen)
    }
    fxCyl(cup.x, cup.y, cup.y - 0.034f * u, 0.014f * u, 0.016f * u, FxC.pink, pen, top = Color(0xFF5A3F3A))
    // The top, the drawer and the front legs.
    fxBox(u, -0.15f, -0.15f, 0.15f, -0.134f, d, DcC.paint, pen, rad = 0.004f)
    fxBox(u, -0.13f, -0.134f, 0.13f, -0.098f, d - 0.02f, DcC.mint, pen, rad = 0.003f, z = 0.004f)
    val drawer = fxFront(u, -0.07f, -0.13f, 0.07f, -0.102f, 0.004f)
    inkedRound(drawer, 0.004f * u, DcC.mint.lighten(0.15f), pen, shade = false)
    inkedCircle(drawer.center, 0.005f * u, DcC.oak, pen, shade = false)
    // A smiley sticker and a sticky note, because it is a child's desk.
    val sticker = Offset(drawer.right - 0.014f * u, drawer.top + 0.012f * u)
    drawCircle(FxC.yellow, 0.007f * u, sticker)
    drawCircle(Ink.line, 0.0012f * u, Offset(sticker.x - 0.0025f * u, sticker.y - 0.002f * u))
    drawCircle(Ink.line, 0.0012f * u, Offset(sticker.x + 0.0025f * u, sticker.y - 0.002f * u))
    drawArc(Ink.line, 20f, 140f, false, Offset(sticker.x - 0.004f * u, sticker.y - 0.002f * u), Size(0.008f * u, 0.005f * u), style = Stroke(pen.lw * 0.4f))
    val note = Rect(-0.122f * u, -0.13f * u, -0.098f * u, -0.106f * u)
    rotate(-6f, note.center) {
        drawRect(Color(0xFFFFE680), note.topLeft, note.size)
        drawRect(Ink.line, note.topLeft, note.size, style = pen.thin)
        for (k in 0 until 2) fxLine(Offset(note.left + 0.004f * u, note.top + (0.008f + k * 0.007f) * u), Offset(note.right - 0.004f * u, note.top + (0.008f + k * 0.007f) * u), FxC.fjord.copy(alpha = 0.6f), pen.lw * 0.4f)
    }
    for (s in 0..1) {
        val x = if (s == 0) -0.135f else 0.135f
        fxBox(u, x - 0.009f, -0.134f, x + 0.009f, 0f, 0.016f, DcC.oak, pen, z = 0.004f)
    }
    if (pen.night > 0.3f) twinkle(Offset(head.x - 0.01f * u, head.y + 0.02f * u), 0.006f * u, Color.White, pen.night * (0.6f + 0.4f * sin(t * 3f)))
}

// ---------------------------------------------------------------------------------------------- pot

private fun DrawScope.dcFlowerPot(f: Fixture, u: Float, pen: Pen) = fxRoundCentred(u, 0.035f) { dcFlowerPotBody(f, u, pen) }

private fun DrawScope.dcFlowerPotBody(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val v = f.variant.mod(3)
    val cz = 0.035f
    val base = q(0f, 0f, cz)
    drawOval(Ink.shadow, Offset(base.x - 0.045f * u, base.y - 0.012f * u), Size(0.1f * u, 0.028f * u))
    val potColor = when (v) {
        0 -> DcC.paint
        1 -> DcC.terracotta
        else -> Color(0xFF8FA2B8)
    }
    val rim = q(0f, -0.055f, cz)
    val soil = q(0f, -0.052f, cz)
    val shake = f.anim * sin(t * 30f) * 6f
    fun plant(back: Boolean) {
        rotate(shake + sin(t * 1.2f + v) * 1.5f, soil) {
            when (v) {
                0 -> {
                    val heads = floatArrayOf(-0.02f, -0.13f, 0.004f, -0.14f, 0.024f, -0.125f)
                    val cols = arrayOf(FxC.red, FxC.yellow, FxC.pink)
                    for (k in 0 until 3) {
                        if ((k == 1) != back) continue
                        val hx = soil.x + heads[k * 2] * u
                        val hy = rim.y + (heads[k * 2 + 1] + 0.055f) * u
                        fxLine(Offset(soil.x + heads[k * 2] * 0.3f * u, soil.y), Offset(hx, hy), FxC.spruceLight, 0.004f * u)
                        inked(fxLeaf(soil.x, soil.y, soil.x + (k - 1) * 0.03f * u, soil.y - 0.05f * u, 0.22f), FxC.leaf, pen)
                        val tulip = Path().apply {
                            moveTo(hx - 0.011f * u, hy - 0.012f * u)
                            lineTo(hx - 0.005f * u, hy - 0.004f * u)
                            lineTo(hx, hy - 0.014f * u)
                            lineTo(hx + 0.005f * u, hy - 0.004f * u)
                            lineTo(hx + 0.011f * u, hy - 0.012f * u)
                            quadraticTo(hx + 0.012f * u, hy + 0.008f * u, hx, hy + 0.008f * u)
                            quadraticTo(hx - 0.012f * u, hy + 0.008f * u, hx - 0.011f * u, hy - 0.012f * u)
                            close()
                        }
                        inked(tulip, cols[k], pen)
                    }
                }
                1 -> if (!back) {
                    val body = Path().apply {
                        moveTo(soil.x - 0.018f * u, soil.y)
                        lineTo(soil.x - 0.02f * u, soil.y - 0.06f * u)
                        quadraticTo(soil.x - 0.02f * u, soil.y - 0.085f * u, soil.x, soil.y - 0.085f * u)
                        quadraticTo(soil.x + 0.02f * u, soil.y - 0.085f * u, soil.x + 0.02f * u, soil.y - 0.06f * u)
                        lineTo(soil.x + 0.018f * u, soil.y)
                        close()
                    }
                    inked(body, FxC.spruceLight, pen)
                    for (k in 0 until 2) {
                        val m = if (k == 0) -1f else 1f
                        val arm = Path().apply {
                            moveTo(soil.x + m * 0.018f * u, soil.y - 0.03f * u)
                            lineTo(soil.x + m * 0.032f * u, soil.y - 0.03f * u)
                            quadraticTo(soil.x + m * 0.038f * u, soil.y - 0.03f * u, soil.x + m * 0.038f * u, soil.y - 0.04f * u)
                            lineTo(soil.x + m * 0.038f * u, soil.y - 0.056f * u - k * 0.006f * u)
                            quadraticTo(soil.x + m * 0.031f * u, soil.y - 0.062f * u - k * 0.006f * u, soil.x + m * 0.028f * u, soil.y - 0.056f * u - k * 0.006f * u)
                            lineTo(soil.x + m * 0.028f * u, soil.y - 0.04f * u)
                            lineTo(soil.x + m * 0.018f * u, soil.y - 0.04f * u)
                            close()
                        }
                        inked(arm, FxC.spruceLight, pen)
                    }
                    for (k in 0 until 3) fxLine(Offset(soil.x + (k - 1) * 0.009f * u, soil.y - 0.004f * u), Offset(soil.x + (k - 1) * 0.009f * u, soil.y - 0.078f * u), FxC.spruce.copy(alpha = 0.5f), pen.lw * 0.5f)
                    for (k in 0 until 8) drawCircle(Color.White, 0.0012f * u, Offset(soil.x + (k % 3 - 1) * 0.012f * u, soil.y - (0.012f + k * 0.009f) * u))
                    val flower = Offset(soil.x + 0.004f * u, soil.y - 0.09f * u)
                    for (k in 0 until 5) {
                        val a = k * 2f * FX_PI / 5f
                        drawCircle(FxC.pink, 0.006f * u, Offset(flower.x + cos(a) * 0.006f * u, flower.y + sin(a) * 0.006f * u))
                    }
                    drawCircle(FxC.yellow, 0.004f * u, flower)
                }
                else -> {
                    val angles = floatArrayOf(-55f, -20f, 15f, 50f, -35f)
                    for (k in angles.indices) {
                        if ((k % 2 == 1) != back) continue
                        val a = angles[k] * FX_PI / 180f
                        val len = (0.07f + (k % 3) * 0.012f) * u
                        val tip = Offset(soil.x + sin(a) * len, soil.y - cos(a) * len)
                        fxLine(Offset(soil.x, soil.y), fxMix(soil, tip, 0.4f), FxC.spruce, 0.003f * u)
                        val lc = fxMix(soil, tip, 0.72f)
                        val leaf = blobPath(
                            lc.x - 0.022f * u, lc.y + 0.004f * u, lc.x - 0.012f * u, lc.y - 0.02f * u, lc.x + 0.012f * u, lc.y - 0.022f * u,
                            lc.x + 0.024f * u, lc.y, lc.x + 0.01f * u, lc.y + 0.02f * u, lc.x - 0.012f * u, lc.y + 0.018f * u,
                        )
                        inked(leaf, if (back) FxC.spruce else FxC.spruceLight, pen)
                        for (j in 0 until 2) {
                            val m = if (j == 0) -1f else 1f
                            fxLine(Offset(lc.x + m * 0.02f * u, lc.y + m * 0.003f * u), Offset(lc.x + m * 0.01f * u, lc.y), Color(0xFFFBF4E6), 0.003f * u)
                        }
                        drawLine(FxC.spruce.darken(0.2f), Offset(lc.x, lc.y - 0.018f * u), Offset(lc.x, lc.y + 0.016f * u), pen.lw * 0.5f)
                    }
                }
            }
        }
    }
    plant(true)
    fxCyl(base.x, base.y, rim.y, 0.028f * u, 0.036f * u, potColor, pen, top = FxC.soil)
    if (v == 0) fxLine(Offset(base.x - 0.036f * u, rim.y + 0.018f * u), Offset(base.x + 0.036f * u, rim.y + 0.01f * u), FxC.fjord, 0.004f * u)
    if (v == 1) {
        val band = rim.y + 0.01f * u
        drawLine(DcC.terracotta.darken(0.15f), Offset(base.x - 0.04f * u, band + 0.006f * u), Offset(base.x + 0.04f * u, band - 0.006f * u), 0.008f * u)
    }
    plant(false)
}

// ---------------------------------------------------------------------------------------------- bin

private fun DrawScope.dcTrashBin(f: Fixture, u: Float, pen: Pen) = fxRoundCentred(u, 0.042f) { dcTrashBinBody(f, u, pen) }

private fun DrawScope.dcTrashBinBody(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val body = DcC.mint
    val cz = 0.042f
    val r = 0.042f * u
    val base = q(0f, 0f, cz)
    drawOval(Ink.shadow, Offset(base.x - 0.05f * u, base.y - 0.014f * u), Size(0.11f * u, 0.032f * u))
    val a = f.anim.coerceIn(0f, 1f)
    val chomp = if (a > 0f) sin(a * FX_PI * 3f).let { abs(it) } else 0f
    val rimY = q(0f, -0.13f, cz).y
    // The lid is hinged at the back: when the bin eats, it flips up like a mouth.
    val open = a > 0f
    if (open) {
        // Standing up from its hinge at the back rim; the wider it opens, the more of it we see.
        val hinge = rimY - r * 0.36f
        val tall = r * (0.55f + 0.9f * chomp)
        val lid = Rect(base.x - r * 1.05f + r * 0.35f, hinge - tall, base.x + r * 1.15f + r * 0.35f, hinge + r * 0.2f)
        drawOval(body.darken(0.12f), lid.topLeft, lid.size)
        drawOval(Ink.line, lid.topLeft, lid.size, style = pen.stroke)
        drawOval(body.lighten(0.15f), Offset(lid.left + lid.width * 0.15f, lid.top + lid.height * 0.15f), Size(lid.width * 0.7f, lid.height * 0.45f))
    }
    fxCyl(base.x, base.y, rimY, r * 0.94f, r, body, pen, cap = false)
    val mouth = fxDisc(base.x, rimY, r)
    if (open) {
        drawPath(mouth, Color(0xFF3A1E2A))
        clipPath(mouth) {
            drawOval(FxC.pink, Offset(base.x - r * 0.5f, rimY - r * 0.1f), Size(r * 1.1f, r * 0.8f))
            drawLine(FxC.pink.darken(0.3f), Offset(base.x + 0.004f * u, rimY), Offset(base.x + 0.004f * u, rimY + r * 0.4f), pen.lw * 0.6f)
        }
        drawPath(mouth, Ink.line, style = pen.stroke)
    } else {
        fxFace(mouth, body.lighten(0.15f), pen)
        val knob = Offset(base.x + r * 0.2f, rimY - r * 0.12f)
        drawOval(DcC.charcoal, Offset(knob.x - 0.006f * u, knob.y - 0.003f * u), Size(0.012f * u, 0.005f * u))
    }
    // A cheeky face: sleepy when idle, wide awake when it gets a treat.
    val face = Offset(base.x - r * 0.35f, (rimY + base.y) / 2f - 0.01f * u)
    for (s in 0..1) {
        val e = Offset(face.x + (s * 2 - 1) * 0.013f * u, face.y - 0.012f * u)
        if (open) {
            dcEye(e, 0.008f * u, 0.3f + 0.3f * sin(t * 20f), -0.5f, pen)
        } else {
            drawArc(Ink.line, 20f, 140f, false, Offset(e.x - 0.006f * u, e.y - 0.004f * u), Size(0.012f * u, 0.008f * u), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
        }
    }
    drawOval(FxC.blush, Offset(face.x - 0.026f * u, face.y), Size(0.012f * u, 0.006f * u))
    drawOval(FxC.blush, Offset(face.x + 0.014f * u, face.y), Size(0.012f * u, 0.006f * u))
    if (open) {
        drawArc(Ink.line, 0f, 180f, true, Offset(face.x - 0.008f * u, face.y), Size(0.016f * u, 0.012f * u))
    } else {
        drawArc(Ink.line, 20f, 140f, false, Offset(face.x - 0.007f * u, face.y - 0.002f * u), Size(0.014f * u, 0.008f * u), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    }
    // The pedal.
    val pedal = q(-0.01f, -0.006f, cz - 0.05f)
    fxBox(u, -0.022f, -0.012f, 0.012f, -0.004f, 0.02f, DcC.charcoal, pen, rad = 0.002f, z = -0.012f)
    fxLine(pedal, Offset(pedal.x + 0.004f * u, pedal.y - 0.01f * u), DcC.charcoal, pen.lw)
    if (open && a > 0.5f) {
        for (k in 0 until 3) {
            val ph = 1f - a
            twinkle(Offset(base.x + (k - 1) * 0.03f * u, rimY - (0.03f + ph * 0.05f) * u - k * 0.004f * u), 0.006f * u, FxC.yellow, a)
        }
    }
}

// ---------------------------------------------------------------------------------------------- robot vacuum

private fun DrawScope.dcVacuum(f: Fixture, u: Float, pen: Pen) = fxRoundCentred(u, 0.05f) { dcVacuumBody(f, u, pen) }

private fun DrawScope.dcVacuumBody(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.05f
    val r = 0.05f * u
    val base = q(0f, 0f, cz)
    val top = q(0f, -0.024f, cz)
    val dir = if (f.angleV < 0f) -1f else 1f
    val rumble = if (f.on) sin(t * 40f) * 0.0008f * u else 0f
    drawOval(Ink.shadow, Offset(base.x - 0.055f * u, base.y - 0.014f * u), Size(0.12f * u, 0.034f * u))
    // Side brushes peeking out at the front corners.
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        val c = fxRim(base.x, base.y - 0.002f * u, r * 0.92f, FX_PI * 1.5f + m * 0.9f)
        val spin = if (f.on) t * 18f * m else 0.4f
        for (k in 0 until 3) {
            val a = spin + k * 2f * FX_PI / 3f
            drawLine(DcC.charcoal, c, Offset(c.x + cos(a) * 0.016f * u, c.y + sin(a) * 0.006f * u), pen.lw * 0.7f, StrokeCap.Round)
        }
        drawCircle(DcC.charcoal, 0.003f * u, c)
    }
    translate(0f, rumble) {
        fxCyl(base.x, base.y - 0.002f * u, top.y, r, r, Color(0xFF3C4A66), pen, top = Color(0xFFE8ECF3))
        // The bumper round the front.
        val bumper = Path()
        for (k in 0..8) {
            val a = FX_PI + 0.4636f + k * FX_PI / 8f
            val pt = fxRim(base.x, base.y - 0.012f * u, r * 1.005f, a)
            if (k == 0) bumper.moveTo(pt.x, pt.y) else bumper.lineTo(pt.x, pt.y)
        }
        drawPath(bumper, DcC.charcoal, style = Stroke(0.007f * u, cap = StrokeCap.Round))
        drawPath(fxDisc(top.x, top.y, r * 0.62f), Color(0xFFCFD6E2), style = Stroke(pen.lw))
        // The status light, and googly eyes looking where it is going.
        val light = if (f.on) (if (fxFrac(t * 1.5f) < 0.5f) FxC.green else FxC.green.darken(0.3f)) else Color(0xFF9AA3B5)
        val lc = q(0f, -0.024f, cz + 0.03f)
        if (f.on) fxGlow(lc, 0.012f * u, FxC.green, 0.6f)
        drawPath(fxDisc(lc.x, lc.y, 0.0045f * u), light)
        for (s in 0..1) {
            val e = q((s * 2 - 1) * 0.019f, -0.032f, cz - 0.012f)
            val jiggle = if (f.on) sin(t * 23f + s * 2f) * 0.3f else 0f
            dcEye(e, 0.011f * u, dir * 0.7f + jiggle, -0.2f + jiggle * 0.5f, pen)
        }
        if (f.on) {
            val mouth = q(0f, -0.024f, cz - 0.03f)
            drawArc(Ink.line, 20f, 140f, false, Offset(mouth.x - 0.008f * u, mouth.y - 0.003f * u), Size(0.016f * u, 0.006f * u), style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
            for (k in 0 until 3) {
                val ph = fxFrac(t * 2f + k * 0.33f)
                drawCircle(Color(0xFFB0B4C0).copy(alpha = 0.5f * (1f - ph)), (0.003f + ph * 0.004f) * u, Offset(base.x - dir * (r + ph * 0.03f * u), base.y - 0.008f * u - ph * 0.01f * u))
            }
        }
    }
}
