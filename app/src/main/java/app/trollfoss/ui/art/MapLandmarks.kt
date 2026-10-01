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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * One scene per place, each drawn back to front in its own space (see Bx): shadow, things behind, the
 * building, then what stands in front (fences, tables, flowers) and grass tufts that hug the ground.
 */

internal fun MapPen.depthScale(yFrac: Float) = 0.84f + 0.22f * ((yFrac - 0.25f) / 0.6f).coerceIn(0f, 1f)

/** Storhuset, the big house, is drawn a little smaller than the unit (its estate is wide); the others at full size. */
internal fun landmarkScale(place: PlaceId): Float = if (place == PlaceId.MANOR_GROUND) MANOR_SCALE else 1f

/** Draws the still part of the landmark of [place]: everything that does not move, at its base. */
internal fun MapPen.drawLandmark(d: DrawScope, g: MapGeo, place: PlaceId) {
    val b = g.bases[place]!!
    val yf = b.y / h
    if (place == PlaceId.SPACE) return
    val sc = S * depthScale(yf) * landmarkScale(place)
    if (place == PlaceId.MINE_YARD) {
        mine?.let { d.drawMapMine(it, b.x, b.y, sc * MAP_MINE_SCALE, lw, n, snow, grass(Color(0xFF6FAE5A), Color(0xFFEAF0FA))) }
        return
    }
    if (place == PlaceId.VAGSTADDALEN) {
        d.drawMapValley(b.x, b.y, sc, pen)
        return
    }
    d.withTransform({
        translate(b.x, b.y)
        scale(sc, sc, Offset.Zero)
    }) {
        val bx = Bx(this, this@drawLandmark, sc, yf)
        with(bx) {
            when (place.name) {
                "HOME" -> home()
                "CAFE" -> cafe()
                "SALON" -> salon()
                "SHOP" -> shop()
                "DOCTOR" -> doctor()
                "TIVOLI" -> tivoli()
                "STAGE" -> concert()
                "FOREST" -> camp()
                "LAB" -> cave()
                "MOUNTAIN" -> fjellet()
                "FARM" -> farm()
                "BEACH" -> beach()
                "MANOR_GROUND" -> manor()
                "UNDERWATER" -> dive()
                "HEILEBERGET" -> heileberget((g.cableTop.x - b.x) / sc, (g.cableTop.y - b.y) / sc)
                else -> Unit
            }
        }
    }
}

/** Draws what moves in the landmark of [place] (smoke, flags, wheels, animals, boats...), on top of the still layer. */
internal fun MapPen.drawLandmarkLive(d: DrawScope, g: MapGeo, place: PlaceId, hl: Boolean = false) {
    if (place == PlaceId.SPACE) {
        drawSpace(d, g, hl)
        return
    }
    val b = g.bases[place]!!
    val yf = b.y / h
    val sc = S * depthScale(yf) * landmarkScale(place)
    if (place == PlaceId.MINE_YARD) {
        mine?.let { d.drawMapMineSmoke(it, b.x, b.y, sc * MAP_MINE_SCALE, (t * 0.4f) % 1f) }
        return
    }
    d.withTransform({
        translate(b.x, b.y)
        scale(sc, sc, Offset.Zero)
    }) {
        val bx = Bx(this, this@drawLandmarkLive, sc, yf)
        with(bx) {
            when (place.name) {
                "HOME" -> homeLive()
                "CAFE" -> cafeLive()
                "SALON" -> salonLive()
                "TIVOLI" -> tivoliLive()
                "STAGE" -> concertLive()
                "FOREST" -> campLive()
                "LAB" -> caveLive()
                "MOUNTAIN" -> fjelletLive()
                "FARM" -> farmLive()
                "BEACH" -> beachLive()
                "UNDERWATER" -> diveLive()
                "HEILEBERGET" -> heilebergetLive((g.cableTop.x - b.x) / sc, (g.cableTop.y - b.y) / sc)
                "MANOR_GROUND" -> manorLive()
                else -> Unit
            }
        }
    }
}

// ------------------------------------------------------------------------------------- helpers

private fun Bx.sideWin(x: Float, dep: Float, f0: Float, f1: Float, yTop: Float, yBot: Float) {
    val ax = x + 0.5f * dep * f0
    val ay = -0.36f * dep * f0
    val bx = x + 0.5f * dep * f1
    val by = -0.36f * dep * f1
    val q = path(ax, ay + yTop, bx, by + yTop, bx, by + yBot, ax, ay + yBot)
    glow((ax + bx) / 2f, (ay + by) / 2f + (yTop + yBot) / 2f, 0.12f)
    d.drawPath(q, lerp(c(Color(0xFF8DB9D6)), Color(0xFFFFD76B), lit))
    d.drawPath(q, c(Color(0xFFF7F3EC)), style = Stroke(lw * 1.8f, join = StrokeJoin.Round))
    ink(q, 0.7f, 0.8f)
}

private fun Bx.awning(x0: Float, x1: Float, yTop: Float, yFront: Float, c1: Color, c2: Color, stripes: Int) {
    val sw = (x1 - x0) / stripes
    for (i in 0 until stripes) {
        val xa = x0 + i * sw
        val p = Path().apply {
            moveTo(xa, yTop)
            lineTo(xa + sw, yTop)
            lineTo(xa + sw, yFront)
            arcTo(Rect(xa, yFront - sw * 0.5f, xa + sw, yFront + sw * 0.5f), 0f, 180f, false)
            close()
        }
        fill(p, if (i % 2 == 0) c1 else c2)
        ink(p, 0.8f, 0.9f)
    }
    line(x0, yTop, x1, yTop, Ink.line, 1.3f)
    // The shadow the awning throws on the wall below it.
    d.drawRect(Ink.line, Offset(x0, yFront + sw * 0.5f), Size(x1 - x0, 0.05f), alpha = 0.12f)
}

/** The pole of a flag (the pennant is waved by [flagLive]). */
private fun Bx.flag(x: Float, y: Float, hgt: Float, @Suppress("UNUSED_PARAMETER") col: Color, @Suppress("UNUSED_PARAMETER") len: Float = 0.2f) {
    line(x, y, x, y - hgt, Ink.line, 1.6f)
}

/** The waving pennant of a flag drawn by [flag] with the same arguments. */
private fun Bx.flagLive(x: Float, y: Float, hgt: Float, col: Color, len: Float = 0.2f) {
    val wave = sin(t * 4f + x * 7f) * 0.02f
    val p = path(x, y - hgt, x + len, y - hgt + 0.045f + wave, x, y - hgt + 0.1f)
    fill(p, col)
    ink(p, 0.9f)
}

private fun Bx.parasol(x: Float, y: Float, r: Float, c1: Color, c2: Color) {
    d.drawOval(Ink.line, Offset(x - r * 0.9f, y - 0.01f), Size(r * 1.8f, 0.07f), alpha = 0.16f)
    line(x, y, x, y - r * 1.5f, Color(0xFF7A5134), 2f)
    val n2 = 6
    for (i in 0 until n2) {
        val a0 = 3.1416f * i / n2
        val a1 = 3.1416f * (i + 1) / n2
        val p = Path().apply {
            moveTo(x, y - r * 1.75f)
            lineTo(x - r * cos(a0), y - r * 1.05f - r * 0.12f * sin(a0))
            lineTo(x - r * cos(a1), y - r * 1.05f - r * 0.12f * sin(a1))
            close()
        }
        fill(p, if (i % 2 == 0) c1 else c2)
        ink(p, 0.7f, 0.85f)
    }
}

private fun Bx.roundTable(x: Float, y: Float, stools: Boolean = true) {
    line(x, y, x, y - 0.11f, Ink.line, 3f)
    d.drawOval(Ink.line, Offset(x - 0.12f, y - 0.01f), Size(0.24f, 0.05f), alpha = 0.18f)
    oval(x, y - 0.12f, 0.1f, 0.032f, Color(0xFFFFF6E6), 1f)
    if (stools) for (sx in floatArrayOf(-0.15f, 0.15f)) {
        line(x + sx, y + 0.02f, x + sx, y - 0.05f, Ink.line, 2.4f)
        oval(x + sx, y - 0.06f, 0.045f, 0.016f, Color(0xFFE07A6A), 0.9f)
    }
}

private fun Bx.pot(x: Float, y: Float, flower: Color) {
    val p = path(x - 0.04f, y - 0.07f, x + 0.04f, y - 0.07f, x + 0.03f, y, x - 0.03f, y)
    fill(p, Color(0xFFC9824A))
    ink(p, 0.9f)
    for (k in -1..1) disc(x + k * 0.03f, y - 0.1f - 0.012f * (k * k), 0.022f, flower, 0.7f)
    line(x, y - 0.07f, x, y - 0.1f, Color(0xFF3F8A4A), 1.3f)
}

private fun Bx.heart(x: Float, y: Float, s: Float, col: Color) {
    val p = Path().apply {
        moveTo(x, y + s * 0.9f)
        cubicTo(x - s * 1.6f, y - s * 0.1f, x - s * 0.6f, y - s * 1.2f, x, y - s * 0.35f)
        cubicTo(x + s * 0.6f, y - s * 1.2f, x + s * 1.6f, y - s * 0.1f, x, y + s * 0.9f)
        close()
    }
    fill(p, col)
    ink(p, 1f)
}

private fun Bx.cow(x: Float, y: Float, s: Float, phase: Float, flip: Boolean = false) {
    val f = if (flip) -1f else 1f
    d.drawOval(Ink.line, Offset(x - s * 0.55f, y - s * 0.04f), Size(s * 1.1f, s * 0.2f), alpha = 0.2f)
    for (lx in floatArrayOf(-0.32f, -0.18f, 0.16f, 0.3f)) line(x + lx * s * f, y - s * 0.22f, x + lx * s * f, y, Ink.line, 2.6f)
    oval(x, y - s * 0.4f, s * 0.46f, s * 0.26f, Color.White, 1.1f)
    val patch = Path().apply { addOval(Rect(x - s * 0.28f, y - s * 0.56f, x - s * 0.06f, y - s * 0.4f)); addOval(Rect(x + s * 0.1f, y - s * 0.46f, x + s * 0.3f, y - s * 0.3f)) }
    fill(patch, Color(0xFF2B2140))
    val dip = max(0f, sin(phase)) * s * 0.16f
    val hx = x + f * s * 0.52f
    val hy = y - s * 0.46f + dip
    oval(hx, hy, s * 0.17f, s * 0.15f, Color.White, 1f)
    oval(hx + f * s * 0.08f, hy + s * 0.06f, s * 0.1f, s * 0.07f, Color(0xFFFFB6C1), 0.8f)
    disc(hx - f * s * 0.04f, hy - s * 0.04f, s * 0.018f, Color(0xFF2B2140), 0f)
    line(hx - f * s * 0.1f, hy - s * 0.12f, hx - f * s * 0.14f, hy - s * 0.2f, Ink.line, 1.6f)
    line(x - f * s * 0.46f, y - s * 0.46f, x - f * s * 0.52f + sin(t * 2f) * s * 0.03f, y - s * 0.22f, Ink.line, 1.6f)
}

private fun Bx.sheep(x: Float, y: Float, s: Float, phase: Float) {
    d.drawOval(Ink.line, Offset(x - s * 0.4f, y - s * 0.03f), Size(s * 0.8f, s * 0.14f), alpha = 0.2f)
    for (lx in floatArrayOf(-0.16f, 0.12f)) line(x + lx * s, y - s * 0.12f, x + lx * s, y, Ink.line, 2.2f)
    val wool = Path()
    for (k in 0 until 6) wool.addOval(Rect(x + s * (-0.3f + 0.12f * k) - s * 0.15f, y - s * (0.34f + 0.05f * (k % 2)) - s * 0.12f, x + s * (-0.3f + 0.12f * k) + s * 0.15f, y - s * (0.34f + 0.05f * (k % 2)) + s * 0.12f))
    fill(wool, Color(0xFFFFFDF4))
    ink(wool, 0.8f, 0.8f)
    val dip = max(0f, sin(phase)) * s * 0.1f
    oval(x + s * 0.36f, y - s * 0.28f + dip, s * 0.1f, s * 0.085f, Color(0xFF3B3346), 0.8f)
}

private fun Bx.cabinBox(x: Float, y: Float, col: Color) {
    line(x, y, x, y + 0.06f, Ink.line, 1.4f)
    round(x - 0.07f, y + 0.06f, x + 0.07f, y + 0.17f, 0.02f, col)
    d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFD76B), lit), Offset(x - 0.045f, y + 0.08f), Size(0.09f, 0.045f))
}

// ------------------------------------------------------------------------------------- HOME

private fun Bx.home() {
    val hw = 0.4f
    val wallH = 0.42f
    val roofH = 0.34f
    val dep = 0.55f
    pineTree(0.88f, -0.14f, 0.62f)
    roundTree(-0.92f, 0.04f, 0.66f, fruit = true)
    shadowHouse(-hw, hw, dep, wallH, roofH, true)
    contact(0f, 0.6f)
    gable(hw, wallH, roofH, dep, Color(0xFFC0463A), Color(0xFF4A4A57))
    win(-0.3f, -0.34f, -0.12f, -0.16f, shutters = true, box = true)
    win(-0.065f, -0.64f, 0.065f, -0.5f, round = true)
    door(0.1f, 0.27f, 0.27f, Color(0xFF3E6FA8), steps = true)
    sideWin(hw, dep, 0.22f, 0.42f, -wallH * 0.75f, -wallH * 0.3f)
    sideWin(hw, dep, 0.58f, 0.78f, -wallH * 0.75f, -wallH * 0.3f)
    chimney(0.16f, -wallH - roofH * 0.52f, 0.08f, 0.2f)
    // The garden out front: flower beds, picket fence with a gate, a mailbox and a bike.
    val beds = ArrayList<Offset>()
    for (k in 0 until 7) beds.add(Offset(-0.78f + k * 0.11f, 0.15f + 0.012f * (k % 2)))
    val cols = listOf(Color(0xFFFF6B8A), Color(0xFFFFC83D), Color(0xFFFFFFFF), Color(0xFFB983FF))
    for ((i, p) in beds.withIndex()) {
        line(p.x, p.y, p.x, p.y - 0.05f, Color(0xFF3F8A4A), 1.3f)
        disc(p.x, p.y - 0.06f, 0.024f, cols[i % 4], 0.7f)
    }
    val veg = path(0.52f, 0.1f, 0.94f, 0.1f, 1.0f, 0.2f, 0.46f, 0.2f)
    fill(veg, Color(0xFF8C6A45))
    ink(veg, 0.8f, 0.7f)
    for (k in 0 until 5) {
        val x = 0.56f + k * 0.085f
        line(x, 0.19f, x + 0.03f, 0.11f, Color(0xFF4F9A48), 2.2f)
        disc(x + 0.03f, 0.11f, 0.02f, Color(0xFF6BCB77), 0.6f)
    }
    fence(-1.0f, 1.0f, 0.24f, 0.08f, 0.3f)
    line(0.36f, 0.24f, 0.36f, 0.12f, Color(0xFF7A5134), 2.4f)
    box(0.33f, 0.07f, 0.4f, 0.12f, Color(0xFFE94F4F), 0.9f)
    // A bicycle leaning on the fence.
    val bikeY = 0.22f
    d.drawCircle(Ink.line, 0.055f, Offset(-0.55f, bikeY), style = Stroke(lw * 1.6f))
    d.drawCircle(Ink.line, 0.055f, Offset(-0.38f, bikeY), style = Stroke(lw * 1.6f))
    d.drawPath(Path().apply { moveTo(-0.55f, bikeY); lineTo(-0.48f, bikeY - 0.1f); lineTo(-0.4f, bikeY - 0.1f); lineTo(-0.38f, bikeY); moveTo(-0.48f, bikeY - 0.1f); lineTo(-0.46f, bikeY); lineTo(-0.38f, bikeY) }, c(Color(0xFFE94F4F)), style = Stroke(lw * 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // The clothesline with laundry swaying.
    line(0.8f, 0.04f, 0.8f, -0.42f, Ink.line, 2.6f)
    line(1.18f, 0.04f, 1.18f, -0.42f, Ink.line, 2.6f)
    val sag = Path().apply { moveTo(0.8f, -0.4f); quadraticTo(0.99f, -0.33f, 1.18f, -0.4f) }
    d.drawPath(sag, Ink.line, style = Stroke(lw * 1.1f))
    val laundry = listOf(Color(0xFFFF6B8A), Color(0xFF5AA9E6), Color(0xFFFFC83D), Color(0xFFFFFFFF), Color(0xFF6BCB77))
    for (k in 0 until 5) {
        val lx = 0.86f + k * 0.065f
        val ly = -0.4f + 0.05f * sin((lx - 0.8f) / 0.38f * 3.1416f)
        val sw = 0f
        val p = path(lx - 0.022f, ly, lx + 0.022f, ly, lx + 0.026f + sw, ly + 0.09f, lx - 0.026f + sw, ly + 0.09f)
        fill(p, laundry[k])
        ink(p, 0.7f, 0.8f)
    }
    tufts(-1.05f, -0.5f, 0.45f, 0.9f, 1.1f, y = 0.27f, s = 0.06f)
}

private fun Bx.homeLive() {
    chimneySmoke(0.16f, -0.42f - 0.34f * 0.52f, 0.08f, 0.2f)
}

// ------------------------------------------------------------------------------------- CAFE

private fun Bx.cafe() {
    val hw = 0.52f
    val wallH = 0.4f
    val roofH = 0.27f
    val dep = 0.5f
    roundTree(0.98f, -0.04f, 0.6f, Color(0xFF6DBB5A))
    shadowHouse(-hw, hw, dep, wallH, roofH, false)
    contact(0f, 0.62f)
    longHouse(hw, wallH, roofH, dep, Color(0xFF9FE0C4), Color(0xFFE0705F))
    // The bakery window with loaves in it, under a striped awning.
    val gx0 = -0.44f
    val gx1 = 0.08f
    glow((gx0 + gx1) / 2f, -0.13f, 0.3f, Color(0xFFFFC76B), 0.3f)
    d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFD98A), lit), Offset(gx0, -0.22f), Size(gx1 - gx0, 0.16f))
    for (k in 0 until 5) oval(gx0 + 0.06f + k * 0.1f, -0.08f - 0.012f * (k % 2), 0.04f, 0.022f, Color(0xFFC98440), 0.8f)
    line(gx0, -0.14f, gx1, -0.14f, Color(0xFFF7F3EC), 1.4f)
    d.drawRect(c(Color(0xFFFFFFFF)), Offset(gx0, -0.22f), Size(gx1 - gx0, 0.16f), style = Stroke(lw * 2f))
    d.drawRect(Ink.line, Offset(gx0 - lw, -0.22f - lw), Size(gx1 - gx0 + 2f * lw, 0.16f + 2f * lw), style = Stroke(lw * 0.8f))
    awning(gx0 - 0.04f, gx1 + 0.04f, -0.3f, -0.22f, Color(0xFFE0705F), Color(0xFFFFFFFF), 7)
    door(0.22f, 0.36f, 0.25f, Color(0xFFFFF0D2), steps = true, arch = true)
    win(0.255f, -0.23f, 0.325f, -0.17f, round = true)
    chimney(-0.3f, -wallH - roofH * 0.6f, 0.08f, 0.17f)
    // A hanging bread sign on a bracket.
    val sway = 0f
    line(-hw, -0.34f, -hw - 0.16f, -0.34f, Ink.line, 2.2f)
    line(-hw - 0.14f, -0.34f, -hw - 0.14f + sway, -0.3f, Ink.line, 1f)
    disc(-hw - 0.14f + sway, -0.23f, 0.075f, Color(0xFFFFF0D2), 1.2f)
    oval(-hw - 0.14f + sway, -0.23f, 0.05f, 0.03f, Color(0xFFC98440), 0.9f)
    for (k in -1..1) line(-hw - 0.14f + sway + k * 0.02f - 0.008f, -0.24f, -hw - 0.14f + sway + k * 0.02f + 0.008f, -0.21f, Color(0xFFF4D29C), 1f)
    // Tables outside under parasols, and a chalkboard.
    roundTable(-0.22f, 0.22f)
    parasol(-0.22f, 0.2f, 0.17f, Color(0xFFE0705F), Color(0xFFFFFFFF))
    roundTable(0.42f, 0.26f)
    parasol(0.42f, 0.24f, 0.17f, Color(0xFF6DBF9C), Color(0xFFFFFFFF))
    pot(-0.58f, 0.12f, Color(0xFFFF6B8A))
    pot(0.46f, 0.0f, Color(0xFFFFC83D))
    val cb = path(0.72f, 0.2f, 0.92f, 0.2f, 0.88f, 0.03f, 0.76f, 0.03f)
    fill(cb, Color(0xFF2F4F48))
    ink(cb, 1.1f)
    oval(0.82f, 0.12f, 0.04f, 0.022f, Color(0xFFF4D29C), 0.6f)
    tufts(-0.9f, -0.5f, 0.1f, 0.7f, 1.0f, y = 0.32f, s = 0.055f)
}

private fun Bx.cafeLive() {
    chimneySmoke(-0.3f, -0.4f - 0.27f * 0.6f, 0.08f, 0.17f)
}

// ------------------------------------------------------------------------------------- SALON

private fun Bx.salon() {
    val hw = 0.36f
    val wallH = 0.44f
    val roofH = 0.3f
    val dep = 0.45f
    shadowHouse(-hw, hw, dep, wallH, roofH, true)
    contact(0f, 0.55f)
    gable(hw, wallH, roofH, dep, Color(0xFFFFC6DC), Color(0xFF9A7BD6))
    win(-0.065f, -0.68f, 0.065f, -0.55f, round = true, frame = Color(0xFFE9DCFF))
    // The big arched window and the door.
    val wx0 = -0.29f
    val wx1 = -0.02f
    val arch = Path().apply {
        moveTo(wx0, -0.1f)
        lineTo(wx0, -0.3f)
        quadraticTo(wx0, -0.42f, (wx0 + wx1) / 2f, -0.42f)
        quadraticTo(wx1, -0.42f, wx1, -0.3f)
        lineTo(wx1, -0.1f)
        close()
    }
    glow((wx0 + wx1) / 2f, -0.26f, 0.3f)
    d.drawPath(arch, lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFD98A), lit))
    d.drawPath(arch, c(Color(0xFFE9DCFF)), style = Stroke(lw * 2.4f))
    ink(arch, 1f)
    oval((wx0 + wx1) / 2f, -0.23f, 0.06f, 0.075f, Color(0xFFFFF6FA), 0.8f)
    door(0.1f, 0.25f, 0.27f, Color(0xFFB69AE6), steps = true, arch = true)
    // The barber pole on the wall, stripes climbing.
    val px0 = -0.5f
    val px1 = -0.43f
    d.drawRect(c(Color(0xFFFFFFFF)), Offset(px0, -0.44f), Size(px1 - px0, 0.36f))
    d.drawRect(Ink.line, Offset(px0, -0.44f), Size(px1 - px0, 0.36f), style = Stroke(lw * 1.3f))
    disc((px0 + px1) / 2f, -0.46f, 0.04f, Color(0xFFE94F4F), 1f)
    disc((px0 + px1) / 2f, -0.06f, 0.035f, Color(0xFFB9C0CC), 1f)
    // The scissors sign on a bracket.
    val sway = 0f
    line(hw, -0.34f, hw + 0.2f, -0.34f, Ink.line, 2.2f)
    val sx = hw + 0.17f + sway
    disc(sx, -0.25f, 0.085f, Color(0xFFFFFFFF), 1.2f)
    d.drawCircle(Ink.line, 0.02f, Offset(sx - 0.03f, -0.21f), style = Stroke(lw * 1.2f))
    d.drawCircle(Ink.line, 0.02f, Offset(sx + 0.03f, -0.21f), style = Stroke(lw * 1.2f))
    line(sx - 0.02f, -0.23f, sx + 0.045f, -0.31f, Ink.line, 1.3f)
    line(sx + 0.02f, -0.23f, sx - 0.045f, -0.31f, Ink.line, 1.3f)
    flag(0f, -wallH - roofH - 0.02f, 0.2f, Color(0xFFFF9EC4), 0.17f)
    // Flowers, a topiary and a bench.
    pot(-0.1f, 0.0f, Color(0xFFFF6B8A))
    pot(0.36f, 0.0f, Color(0xFFFFC83D))
    line(0.98f, 0.06f, 0.98f, -0.16f, Color(0xFF7A5134), 2.6f)
    disc(0.98f, -0.28f, 0.12f, Color(0xFF5DB04F), 1.1f)
    d.drawOval(Ink.line, Offset(0.88f, 0.03f), Size(0.2f, 0.05f), alpha = 0.18f)
    bench(-0.84f, 0.16f)
    val beds = ArrayList<Offset>()
    for (k in 0 until 5) beds.add(Offset(-0.3f + k * 0.14f, 0.2f))
    for ((i, p) in beds.withIndex()) {
        line(p.x, p.y, p.x, p.y - 0.06f, Color(0xFF3F8A4A), 1.3f)
        disc(p.x, p.y - 0.07f, 0.025f, if (i % 2 == 0) Color(0xFFFF9EC4) else Color(0xFFFFFFFF), 0.7f)
    }
    tufts(-0.95f, -0.55f, 0.6f, 0.95f, y = 0.3f, s = 0.055f)
}

private fun Bx.salonLive() {
    // The barber pole's stripes climbing, inside its outline.
    val px0 = -0.5f
    val px1 = -0.43f
    d.clipRect(px0 + lw, -0.44f + lw, px1 - lw, -0.08f - lw) {
        val cols = listOf(Color(0xFFE94F4F), Color(0xFFFFFFFF), Color(0xFF3E7BD6), Color(0xFFFFFFFF))
        val ph = wrap(t * 0.25f, 1f) * 0.24f
        for (k in -2 until 10) {
            val y = -0.08f - k * 0.06f - ph
            val p = path(px0, y, px1, y - 0.035f, px1, y - 0.035f - 0.06f, px0, y - 0.06f)
            d.drawPath(p, c(cols[((k % 4) + 4) % 4]))
        }
    }
    flagLive(0f, -0.44f - 0.3f - 0.02f, 0.2f, Color(0xFFFF9EC4), 0.17f)
}

// ------------------------------------------------------------------------------------- SHOP

private fun Bx.shop() {
    val hw = 0.52f
    val wallH = 0.4f
    val dep = 0.5f
    val vx = 0.5f * dep
    val vy = -0.36f * dep
    shadowHouse(-hw, hw, dep, wallH, 0.02f, false)
    contact(0f, 0.64f)
    // A flat blue roof behind a red sign band, a shaded end wall with two windows and the lit front.
    val roofC = roofRaw(Color(0xFF6FA8DC))
    val bandC = Color(0xFFE8574A)
    face(Color(0xFFF5E6C8).darken(0.24f), hw, -wallH, hw + vx, -wallH + vy, hw + vx, vy, hw, 0f)
    sideWin(hw, dep, 0.2f, 0.44f, -wallH * 0.74f, -wallH * 0.36f)
    sideWin(hw, dep, 0.56f, 0.8f, -wallH * 0.74f, -wallH * 0.36f)
    face(roofC, -hw, -wallH - 0.07f, hw, -wallH - 0.07f, hw + vx, -wallH - 0.07f + vy, -hw + vx, -wallH - 0.07f + vy)
    // A skylight and a vent on the roof.
    face(roofC.lighten(0.45f), -0.2f + vx * 0.5f, -wallH - 0.07f + vy * 0.3f, 0.02f + vx * 0.5f, -wallH - 0.07f + vy * 0.3f, 0.02f + vx * 0.82f, -wallH - 0.07f + vy * 0.72f, -0.2f + vx * 0.82f, -wallH - 0.07f + vy * 0.72f, w = 0.9f)
    box(0.3f + vx * 0.5f, -wallH - 0.15f + vy * 0.5f, 0.42f + vx * 0.5f, -wallH - 0.07f + vy * 0.5f, Color(0xFFD9DEE8), 1f)
    face(bandC.darken(0.2f), hw, -wallH - 0.07f, hw + vx, -wallH - 0.07f + vy, hw + vx, -wallH + vy, hw, -wallH, w = 1f)
    face(bandC, -hw, -wallH - 0.07f, hw, -wallH - 0.07f, hw, -wallH, -hw, -wallH, w = 1f)
    line(-hw + 0.03f, -wallH - 0.035f, hw - 0.03f, -wallH - 0.035f, Color(0xFFFFF3D6), 1.6f)
    box(-hw, -wallH, hw, 0f, Color(0xFFFFF3DC))
    d.drawRect(c(Color(0xFF6E6A78)), Offset(-hw, -0.035f), Size(2f * hw, 0.035f))
    d.drawRect(Ink.line, Offset(-hw, -0.035f), Size(2f * hw, 0.035f), style = Stroke(lw))
    // The rooftop basket sign on posts.
    line(-0.14f, -wallH - 0.04f, -0.14f, -wallH - 0.26f, Ink.line, 2.2f)
    line(0.14f, -wallH - 0.04f, 0.14f, -wallH - 0.26f, Ink.line, 2.2f)
    disc(0f, -wallH - 0.3f, 0.12f, Color(0xFFFFFFFF), 1.3f)
    d.drawArc(Ink.line, 180f, 180f, false, Offset(-0.05f, -wallH - 0.4f), Size(0.1f, 0.12f), style = Stroke(lw * 1.4f))
    face(Color(0xFF3E7BD6), -0.075f, -wallH - 0.31f, 0.075f, -wallH - 0.31f, 0.055f, -wallH - 0.23f, -0.055f, -wallH - 0.23f)
    // A big window and a glass door under a green-and-white awning.
    glow(-0.22f, -0.15f, 0.34f, Color(0xFFFFE9A0), 0.3f)
    d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFE9A0), lit), Offset(-0.44f, -0.26f), Size(0.4f, 0.17f))
    d.drawRect(c(Color(0xFFFFFFFF)), Offset(-0.44f, -0.26f), Size(0.4f, 0.17f), style = Stroke(lw * 2f))
    d.drawRect(Ink.line, Offset(-0.44f - lw, -0.26f - lw), Size(0.4f + 2 * lw, 0.17f + 2 * lw), style = Stroke(lw * 0.8f))
    val fruit = listOf(Color(0xFFE0463A), Color(0xFFFFA63D), Color(0xFF6BCB77), Color(0xFFFFD83D))
    for (k in 0 until 8) disc(-0.4f + k * 0.05f, -0.11f, 0.02f, fruit[k % 4], 0.6f)
    door(0.12f, 0.3f, 0.26f, Color(0xFF6BCB77), steps = true)
    d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFE9A0), lit), Offset(0.145f, -0.22f), Size(0.11f, 0.15f))
    awning(-0.48f, 0.36f, -0.34f, -0.27f, Color(0xFF2FB57A), Color(0xFFFFFFFF), 9)
    val sw = 0f
    for (lx in floatArrayOf(-0.3f, 0.12f)) {
        line(lx, -0.27f, lx + sw, -0.22f, Ink.line, 1f)
        disc(lx + sw, -0.2f, 0.028f, Color(0xFFFFC83D), 0.9f)
        glow(lx + sw, -0.2f, 0.1f)
    }
    // Crates of produce, a hand cart and barrels in front.
    crate(-0.52f, 0.2f, 0.2f, 0.11f, Color(0xFFE0463A))
    crate(-0.29f, 0.22f, 0.2f, 0.11f, Color(0xFFFFA63D))
    crate(-0.06f, 0.2f, 0.2f, 0.11f, Color(0xFF6BCB77))
    val cart = path(0.36f, 0.12f, 0.72f, 0.12f, 0.68f, 0.2f, 0.4f, 0.2f)
    fill(cart, Color(0xFFC98A55))
    ink(cart, 1.1f)
    for (k in 0 until 4) disc(0.43f + k * 0.08f, 0.1f, 0.04f, Color(0xFF6BCB77), 0.8f)
    line(0.72f, 0.12f, 0.92f, 0.04f, Ink.line, 2.4f)
    d.drawCircle(Ink.line, 0.05f, Offset(0.5f, 0.24f), style = Stroke(lw * 1.8f))
    d.drawCircle(c(Color(0xFF7A5134)), 0.04f, Offset(0.5f, 0.24f))
    barrel(0.6f, 0.36f, 0.05f)
    // A little market stall with a striped canopy.
    val sx = 1.0f
    line(sx - 0.16f, 0.15f, sx - 0.16f, -0.25f, Ink.line, 2.2f)
    line(sx + 0.16f, 0.15f, sx + 0.16f, -0.25f, Ink.line, 2.2f)
    val canopy = path(sx - 0.22f, -0.22f, sx + 0.22f, -0.22f, sx + 0.3f, -0.12f, sx - 0.3f, -0.12f)
    fill(canopy, Color(0xFFE0705F))
    ink(canopy, 1f)
    for (k in 0 until 3) {
        val q = path(sx - 0.22f + k * 0.15f, -0.22f, sx - 0.14f + k * 0.15f, -0.22f, sx - 0.18f + k * 0.17f, -0.12f, sx - 0.3f + k * 0.17f, -0.12f)
        fill(q, Color(0xFFFFFFFF))
    }
    box(sx - 0.2f, 0.03f, sx + 0.2f, 0.17f, Color(0xFFC98A55))
    for (k in 0 until 5) disc(sx - 0.15f + k * 0.075f, 0.0f, 0.03f, fruit[(k + 1) % 4], 0.7f)
    tufts(-0.9f, -0.6f, 0.8f, 1.3f, y = 0.4f, s = 0.055f)
}

// ------------------------------------------------------------------------------------- DOCTOR

private fun Bx.doctor() {
    val hw = 0.46f
    val wallH = 0.44f
    val roofH = 0.2f
    val dep = 0.5f
    roundTree(1.0f, -0.05f, 0.62f, Color(0xFF6DBB5A))
    shadowHouse(-hw, hw, dep, wallH, roofH, false)
    contact(0f, 0.6f)
    longHouse(hw, wallH, roofH, dep, Color(0xFFF7F7F4), Color(0xFF7FC4A0))
    win(-0.38f, -0.32f, -0.22f, -0.16f, frame = Color(0xFF7FC4A0))
    win(-0.17f, -0.32f, -0.01f, -0.16f, frame = Color(0xFF7FC4A0))
    win(0.22f, -0.32f, 0.38f, -0.16f, frame = Color(0xFF7FC4A0))
    // The entrance porch with a small gable and a round window in the door.
    face(Color(0xFFF7F7F4), 0.04f, -0.36f, 0.16f, -0.44f, 0.28f, -0.36f, 0.28f, 0f, 0.04f, 0f)
    face(Color(0xFF7FC4A0).lighten(0.05f), 0.0f, -0.35f, 0.16f, -0.5f, 0.32f, -0.35f, 0.3f, -0.33f, 0.16f, -0.45f, 0.02f, -0.33f)
    door(0.09f, 0.23f, 0.27f, Color(0xFF7FC4A0), steps = false)
    win(0.135f, -0.21f, 0.185f, -0.16f, round = true)
    heart(0.16f, -0.385f, 0.028f, Color(0xFFFF6B8A))
    // The pharmacy sign: a green plus on a white sign, with a heart below. Never a red cross.
    val px = -0.66f
    line(px, 0.04f, px, -0.5f, Ink.line, 2.6f)
    val sway = 0f
    round(px - 0.12f + sway, -0.68f, px + 0.12f + sway, -0.44f, 0.04f, Color(0xFFFFFFFF), 1.3f)
    val pl = path(px - 0.028f + sway, -0.66f, px + 0.028f + sway, -0.66f, px + 0.028f + sway, -0.595f, px + 0.085f + sway, -0.595f, px + 0.085f + sway, -0.54f, px + 0.028f + sway, -0.54f, px + 0.028f + sway, -0.475f, px - 0.028f + sway, -0.475f, px - 0.028f + sway, -0.54f, px - 0.085f + sway, -0.54f, px - 0.085f + sway, -0.595f, px - 0.028f + sway, -0.595f)
    fill(pl, Color(0xFF2FB57A))
    ink(pl, 0.9f)
    // A ramp, a bench and flower beds.
    face(Color(0xFFD5DBE2), -0.06f, 0.0f, 0.04f, -0.02f, 0.04f, 0.0f, -0.36f, 0.0f, -0.06f, 0.0f, w = 1f)
    val ramp = path(0.04f, 0.0f, 0.3f, 0.0f, 0.3f, 0.06f, 0.6f, 0.15f, -0.1f, 0.15f)
    fill(ramp, Color(0xFFD5DBE2))
    ink(ramp, 1f, 0.8f)
    line(0.3f, 0.06f, 0.6f, 0.15f, Ink.line, 1.4f)
    bench(-0.3f, 0.23f, 0.28f, Color(0xFF7FC4A0))
    val cols = listOf(Color(0xFFFF9EC4), Color(0xFFFFC83D), Color(0xFFFFFFFF))
    for (k in 0 until 6) {
        val x = 0.66f + k * 0.07f
        line(x, 0.17f, x, 0.11f, Color(0xFF3F8A4A), 1.3f)
        disc(x, 0.095f, 0.022f, cols[k % 3], 0.7f)
    }
    tufts(-0.9f, -0.5f, 0.1f, 0.9f, y = 0.33f, s = 0.055f)
}

// ------------------------------------------------------------------------------------- TIVOLI

private val gondolaColors = listOf(Color(0xFFFF5A6E), Color(0xFFFFC83D), Color(0xFF5CE0A0), Color(0xFF5AA9E6), Color(0xFFD77BFF))

private fun Bx.ferris(cx: Float, cy: Float, r: Float) {
    // A-frame legs and braces, and the rim; the spokes, gondolas and bulbs turn in [ferrisLive].
    val legs = Path().apply {
        moveTo(cx - 0.34f, 0f); lineTo(cx, cy); lineTo(cx + 0.34f, 0f)
        moveTo(cx - 0.22f, -cy * 0.35f * 0f - 0.3f); lineTo(cx + 0.22f, -0.3f)
    }
    d.drawPath(legs, Ink.line, style = Stroke(lw * 5.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    d.drawPath(legs, c(Color(0xFFB9C0CC)), style = Stroke(lw * 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    d.drawCircle(Ink.line, r, Offset(cx, cy), style = Stroke(lw * 4.2f))
    d.drawCircle(c(Color(0xFFFF6B8A)), r, Offset(cx, cy), style = Stroke(lw * 2.6f))
    // The hub: the strings of bulbs run over it, so it is still; the spokes start at its rim.
    disc(cx, cy, 0.05f, Color(0xFFFFC83D), 1.2f)
}

private fun Bx.ferrisLive(cx: Float, cy: Float, r: Float) {
    val rot = t * 0.22f
    // The spokes end at the inner edge of the rim (in the still), which hid their ends before.
    val reach = r - lw * 2.1f
    val hub = 0.05f + lw * 0.5f
    val spokes = ArrayList<Offset>(24)
    for (k in 0 until 12) {
        val a = rot + k * 0.5236f
        spokes.add(Offset(cx + cos(a) * hub, cy + sin(a) * hub))
        spokes.add(Offset(cx + cos(a) * reach, cy + sin(a) * reach))
    }
    d.drawPoints(spokes, PointMode.Lines, c(Color(0xFFD4DAE4)), strokeWidth = lw * 1.4f, cap = StrokeCap.Butt)
    d.drawCircle(Ink.line, r * 0.6f, Offset(cx, cy), alpha = 0.6f, style = Stroke(lw * 1.4f))
    // Gondolas hang straight down from the rim.
    for (k in 0 until 10) {
        val a = rot + k * 0.6283f
        val gx = cx + cos(a) * r
        val gy = cy + sin(a) * r
        line(gx, gy, gx, gy + 0.05f, Ink.line, 1.3f)
        round(gx - 0.05f, gy + 0.05f, gx + 0.05f, gy + 0.13f, 0.02f, gondolaColors[k % 5], 1f)
        d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFD76B), lit), Offset(gx - 0.03f, gy + 0.065f), Size(0.06f, 0.035f))
    }
    // Bulbs along the rim, bright at night.
    val bulbs = ArrayList<Offset>(24)
    for (k in 0 until 24) bulbs.add(Offset(cx + cos(rot + k * 0.2618f) * r, cy + sin(rot + k * 0.2618f) * r))
    if (lit > 0f) d.drawPoints(bulbs, PointMode.Points, Color(0xFFFFE27A), strokeWidth = 0.07f, cap = StrokeCap.Round, alpha = 0.3f * lit)
    d.drawPoints(bulbs, PointMode.Points, lerp(Color(0xFFFFF6D0), Color(0xFFFFF3B0), lit), strokeWidth = 0.022f, cap = StrokeCap.Round)
}

private fun Bx.bulbString(x0: Float, y0: Float, x1: Float, y1: Float, sag: Float, count: Int) {
    val p = Path().apply { moveTo(x0, y0); quadraticTo((x0 + x1) / 2f, (y0 + y1) / 2f + sag * 2f, x1, y1) }
    d.drawPath(p, Ink.line, style = Stroke(lw * 0.9f))
    val pts = ArrayList<Offset>()
    val cols = listOf(Color(0xFFFF5A6E), Color(0xFFFFC83D), Color(0xFF5CE0A0), Color(0xFF5AA9E6), Color(0xFFD77BFF))
    for (k in 1 until count) {
        val f = k / count.toFloat()
        val x = mix(x0, x1, f)
        val y = mix(y0, y1, f) + 4f * sag * f * (1f - f) + 0.014f
        val col = cols[k % 5]
        if (lit > 0f) d.drawCircle(col, 0.04f, Offset(x, y), alpha = 0.3f * lit * 0.875f)
        d.drawCircle(Ink.line, 0.02f, Offset(x, y))
        d.drawCircle(lerp(c(col), Color.White, 0.25f * lit), 0.0155f, Offset(x, y))
    }
}

private fun Bx.tivoli() {
    // The big arch at the entrance.
    val ax = -1.02f
    for (px in floatArrayOf(ax - 0.17f, ax + 0.17f)) {
        box(px - 0.035f, -0.62f, px + 0.035f, 0f, Color(0xFFFFF6EC))
        for (k in 0 until 6) d.drawRect(c(Color(0xFFE94F4F)), Offset(px - 0.035f, -0.6f + k * 0.2f), Size(0.07f, 0.09f))
    }
    val arc = Path().apply { moveTo(ax - 0.17f, -0.62f); quadraticTo(ax, -0.95f, ax + 0.17f, -0.62f) }
    d.drawPath(arc, Ink.line, style = Stroke(lw * 9f, cap = StrokeCap.Round))
    d.drawPath(arc, c(Color(0xFF3E7BD6)), style = Stroke(lw * 6.4f, cap = StrokeCap.Round))
    val star = starPath(Offset(ax, -0.84f), 0.07f, 0.03f, 0f)
    glow(ax, -0.84f, 0.2f, Color(0xFFFFE27A), 0.3f)
    fill(star, Pal.sun)
    ink(star, 1f)
    // The ticket booth.
    val bx = 0.12f
    shadowOf(bx - 0.14f, 0f, bx + 0.14f, 0f, bx + 0.05f, -0.3f, bx + 0.3f, 0.1f)
    box(bx - 0.14f, -0.26f, bx + 0.14f, 0f, Color(0xFFFFF6EC))
    awning(bx - 0.17f, bx + 0.17f, -0.32f, -0.26f, Color(0xFFE94F4F), Color(0xFFFFFFFF), 5)
    d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFD76B), lit), Offset(bx - 0.08f, -0.22f), Size(0.16f, 0.1f))
    // The ferris wheel.
    shadowOf(-0.62f, 0f, -0.04f, 0f, -0.28f + 0.6f, 0.12f, -0.28f + 0.75f, 0.2f)
    contact(-0.28f, 0.4f)
    ferris(-0.28f, -1.0f, 0.55f)
    // The carousel tent with horses.
    val tx = 0.74f
    val rx = 0.4f
    val ry = 0.1f
    shadowOf(tx - rx, 0f, tx + rx, 0f, tx + 0.5f, 0.1f, tx + 0.78f, 0.12f)
    oval(tx, 0f, rx, ry, Color(0xFFC98A55), 1.2f)
    for (k in 0 until 3) {
        val hx = tx + (k - 1) * 0.22f
        line(hx, 0.02f, hx, -0.34f, Color(0xFFFFD98A), 2.2f)
    }
    val apex = Offset(tx, -0.78f)
    for (i in 0 until 8) {
        val a0 = 3.1416f * i / 8f
        val a1 = 3.1416f * (i + 1) / 8f
        val p = Path().apply {
            moveTo(apex.x, apex.y)
            lineTo(tx + (rx + 0.06f) * cos(a0), -0.42f + 0.1f * sin(a0))
            lineTo(tx + (rx + 0.06f) * cos(a1), -0.42f + 0.1f * sin(a1))
            close()
        }
        val base = if (i % 2 == 0) Color(0xFFE94F4F) else Color(0xFFFFFFFF)
        fill(p, if (i >= 5) base.darken(0.2f) else base)
        ink(p, 0.8f, 0.85f)
    }
    for (k in 0 until 7) {
        val a = 3.1416f * (k + 0.5f) / 7f
        disc(tx + (rx + 0.06f) * cos(a), -0.42f + 0.1f * sin(a) + 0.04f, 0.02f, Color(0xFFFFC83D), 0.6f)
    }
    flag(tx, apex.y + 0.02f, 0.14f, Color(0xFFFFC83D), 0.16f)
    // Strings of bulbs between the arch, the wheel and the tent.
    bulbString(ax, -0.88f, -0.28f, -1.0f, 0.08f, 12)
    bulbString(-0.28f, -1.0f, tx, apex.y + 0.06f, 0.1f, 14)
    bulbString(ax + 0.17f, -0.62f, bx, -0.32f, 0.06f, 9)
    // Balloons tied to the booth.
    val cols = listOf(Color(0xFFFF5A6E), Color(0xFF5AA9E6), Color(0xFFFFC83D), Color(0xFF5CE0A0))
    for (k in 0 until 4) {
        val sway = 0f
        val bxp = bx + 0.15f + (k - 1.5f) * 0.06f + sway
        val byp = -0.62f - 0.06f * (k % 2)
        line(bx + 0.14f, -0.32f, bxp, byp + 0.06f, Ink.line, 0.8f)
        oval(bxp, byp, 0.05f, 0.062f, cols[k], 1f)
        d.drawOval(Color.White, Offset(bxp - 0.03f, byp - 0.045f), Size(0.02f, 0.03f), alpha = 0.55f)
    }
    tufts(-0.6f, 0.4f, 1.15f, y = 0.4f, s = 0.055f)
}

private fun Bx.tivoliLive() {
    ferrisLive(-0.28f, -1.0f, 0.55f)
    // The carousel horses bob up and down on their poles.
    val tx = 0.74f
    for (k in 0 until 3) {
        val hx = tx + (k - 1) * 0.22f
        val bob = sin(t * 2.4f + k * 2.1f) * 0.025f
        oval(hx, -0.15f + bob, 0.075f, 0.04f, if (k == 1) Color(0xFFFFFFFF) else Color(0xFFC98A55), 0.9f)
        disc(hx + 0.07f, -0.2f + bob, 0.028f, if (k == 1) Color(0xFFFFFFFF) else Color(0xFFC98A55), 0.8f)
    }
    flagLive(tx, -0.78f + 0.02f, 0.14f, Color(0xFFFFC83D), 0.16f)
}

// ------------------------------------------------------------------------------------- STAGE

private fun Bx.concert() {
    val rx = 0.5f
    val ry = 0.13f
    val hgt = 0.5f
    shadowOf(-rx, 0f, rx, 0f, rx + 0.6f, 0.08f, rx + 0.25f, -hgt * 0.15f)
    contact(0f, 0.6f, 0.09f)
    // The drum: lit on the left, shaded on the right.
    val body = Path().apply {
        moveTo(-rx, -hgt)
        lineTo(-rx, 0f)
        arcTo(Rect(-rx, -ry, rx, ry), 180f, -180f, false)
        lineTo(rx, -hgt)
        close()
    }
    fill(body, Color(0xFFE9DDFB))
    val shade = Path().apply {
        moveTo(0.2f, -hgt)
        lineTo(rx, -hgt)
        lineTo(rx, 0f)
        for (i in 0..8) {
            val x = rx - (rx - 0.2f) * i / 8f
            lineTo(x, ry * sqrt(max(0f, 1f - (x / rx) * (x / rx))))
        }
        close()
    }
    fill(shade, Color(0xFFE9DDFB).darken(0.24f))
    d.drawRect(c(Color(0xFF8B5CF6)), Offset(-rx, -hgt), Size(2f * rx, 0.045f))
    ink(body, 1.3f)
    // Tall arched windows that curve round the drum, the middle one with red curtains.
    for (k in -2..2) {
        val phi = k * 0.52f
        val wx = rx * sin(phi)
        val hwin = 0.045f * cos(phi)
        val yb = -0.06f + ry * cos(phi) * 0.75f
        val top = -0.4f
        val arch = Path().apply {
            moveTo(wx - hwin, yb)
            lineTo(wx - hwin, top + 0.06f)
            quadraticTo(wx - hwin, top, wx, top)
            quadraticTo(wx + hwin, top, wx + hwin, top + 0.06f)
            lineTo(wx + hwin, yb)
            close()
        }
        glow(wx, (top + yb) / 2f, 0.16f, Color(0xFFFFB0F0), 0.3f)
        d.drawPath(arch, lerp(c(if (k == 0) Color(0xFFB9203A) else Color(0xFF8C6BD9)), Color(0xFFFFD6F2), lit * (if (k == 0) 0.3f else 1f)))
        d.drawPath(arch, c(Color(0xFFFFFFFF)), style = Stroke(lw * 1.6f))
        ink(arch, 0.7f, 0.8f)
    }
    // The pointed roof with a star.
    val apexY = -hgt - 0.46f
    for (i in 0 until 9) {
        val a0 = 3.1416f * i / 9f
        val a1 = 3.1416f * (i + 1) / 9f
        val rr = rx + 0.07f
        val p = Path().apply {
            moveTo(0f, apexY)
            lineTo(rr * cos(a0), -hgt + 0.11f * sin(a0))
            lineTo(rr * cos(a1), -hgt + 0.11f * sin(a1))
            close()
        }
        fill(p, (if (i % 2 == 0) Color(0xFF8B5CF6) else Color(0xFF7A4BE0)).let { if (i >= 6) it.darken(0.22f) else it })
        ink(p, 0.8f, 0.85f)
    }
    val rimP = Path().apply { addOval(Rect(-(rx + 0.07f), -hgt - 0.11f, rx + 0.07f, -hgt + 0.11f)) }
    d.drawPath(rimP, Ink.line, alpha = 0.6f, style = Stroke(lw * 1.1f))
    glow(0f, apexY - 0.04f, 0.22f, Color(0xFFFFE27A), 0.35f)
    val star = starPath(Offset(0f, apexY - 0.04f), 0.08f, 0.035f, 0f)
    fill(star, Pal.sun)
    ink(star, 1f)
    // The entrance: a canopy with bulbs over wide steps.
    val can = Path().apply { moveTo(-0.22f, -0.22f); quadraticTo(0f, -0.3f, 0.22f, -0.22f); lineTo(0.22f, -0.15f); quadraticTo(0f, -0.22f, -0.22f, -0.15f); close() }
    fill(can, Color(0xFFE94F4F))
    ink(can, 1f)
    for (k in 0 until 7) {
        val f = k / 6f
        val x = mix(-0.2f, 0.2f, f)
        val y = -0.165f - 0.06f * sin(f * 3.1416f)
        disc(x, y + 0.055f, 0.012f, Color(0xFFFFE27A), 0.4f)
    }
    for (k in 0 until 3) d.drawRect(c(Color(0xFFB9B2C0).darken(0.06f * k)), Offset(-0.24f - 0.03f * k, 0.03f * k), Size(0.48f + 0.06f * k, 0.03f))
    // Flags on poles and a poster board with a music note.
    for (fx in floatArrayOf(-0.72f, 0.72f)) {
        flag(fx, 0.02f, 0.7f, if (fx < 0f) Color(0xFFFF5AC8) else Color(0xFF5CE0FF), 0.24f)
    }
    val board = path(-1.0f, 0.2f, -0.82f, 0.2f, -0.86f, 0.03f, -0.96f, 0.03f)
    fill(board, Color(0xFF3B2A52))
    ink(board, 1f)
    val note = Path().apply { moveTo(-0.905f, 0.17f); lineTo(-0.905f, 0.07f); lineTo(-0.86f, 0.06f); lineTo(-0.86f, 0.15f); addOval(Rect(Offset(-0.94f, 0.16f), 0.03f)); addOval(Rect(Offset(-0.895f, 0.145f), 0.03f)) }
    d.drawPath(note, c(Color(0xFFFFD1F2)), style = Stroke(lw * 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    tufts(-1.1f, 0.85f, 1.1f, y = 0.3f, s = 0.055f)
}

private fun Bx.concertLive() {
    // Searchlight beams sweeping over the roof.
    val hgt = 0.5f
    val strength = 0.1f + 0.22f * lit
    for (k in 0 until 2) {
        val sw = sin(t * 0.7f + k * 2.4f) * 0.35f
        val sx = (k - 0.5f) * 0.3f
        val beam = path(sx - 0.03f, -hgt - 0.5f, sx + 0.03f, -hgt - 0.5f, sx + 0.3f + sw + (k - 0.5f) * 0.5f, -hgt - 1.35f, sx - 0.1f + sw + (k - 0.5f) * 0.5f, -hgt - 1.35f)
        fill(beam, if (k == 0) Color(0xFFFF5AC8) else Color(0xFF5CE0FF), strength)
    }
    for (fx in floatArrayOf(-0.72f, 0.72f)) {
        flagLive(fx, 0.02f, 0.7f, if (fx < 0f) Color(0xFFFF5AC8) else Color(0xFF5CE0FF), 0.24f)
    }
}

// ------------------------------------------------------------------------------------- FOREST (camp)

private fun Bx.camp() {
    pineTree(-0.98f, -0.06f, 0.95f)
    pineTree(1.05f, -0.1f, 1.0f)
    pineTree(-0.62f, -0.2f, 0.8f, Color(0xFF3C8A5A))
    // The main tent: a lit front, a shaded side, a dark doorway with a folded flap.
    shadowOf(-0.52f, 0f, 0.22f, 0f, 0.42f, 0.16f, 0.66f, 0.1f, -0.15f + 0.4f, 0.09f)
    contact(-0.15f, 0.5f)
    prism(floatArrayOf(-0.52f, 0f, -0.15f, -0.62f, 0.22f, 0f), 0.5f, Color(0xFFFF9A4D), Color(0xFFD9692B), Color(0xFFE8742F))
    val door = path(-0.22f, 0f, -0.15f, -0.34f, -0.08f, 0f)
    fill(door, Color(0xFF2B1E2F))
    glow(-0.15f, -0.12f, 0.22f, Color(0xFFFFB84D), 0.35f)
    val flap = path(-0.15f, -0.34f, -0.2f, -0.06f, -0.28f, 0f, -0.3f, -0.1f)
    fill(flap, Color(0xFFFF9A4D).darken(0.1f))
    ink(flap, 0.9f)
    line(-0.15f, -0.62f, -0.15f, -0.34f, Ink.line, 0.9f, 0.7f)
    line(-0.52f, 0f, -0.66f, 0.06f, Ink.line, 1.2f)
    line(0.22f, 0f, 0.36f, 0.06f, Ink.line, 1.2f)
    // A small blue tent behind.
    prism(floatArrayOf(0.52f, -0.06f, 0.74f, -0.42f, 0.96f, -0.06f), 0.3f, Color(0xFF4FC3D9), Color(0xFF2E93A8), Color(0xFF3AA7BF))
    fill(path(0.68f, -0.06f, 0.74f, -0.24f, 0.8f, -0.06f), Color(0xFF1E3F4A))
    // The campfire in a ring of stones, with logs to sit on.
    val fx = 0.42f
    val fy = 0.2f
    for (k in 0 until 9) {
        val a = k * 0.698f
        oval(fx + cos(a) * 0.15f, fy + sin(a) * 0.045f, 0.03f, 0.02f, Color(0xFF9C9AA8), 0.8f)
    }
    line(fx - 0.11f, fy + 0.01f, fx + 0.1f, fy - 0.045f, Color(0xFF7A4B2E), 3.4f)
    line(fx + 0.11f, fy + 0.01f, fx - 0.1f, fy - 0.045f, Color(0xFF7A4B2E), 3.4f)
    glow(fx, fy - 0.1f, 0.4f, Color(0xFFFFB84D), 0.3f)
    d.drawCircle(Color(0xFFFFB84D), 0.26f, Offset(fx, fy - 0.1f), alpha = 0.1f)
    for ((lx, ly) in listOf(0.14f to 0.3f, 0.76f to 0.26f, 0.42f to 0.36f)) {
        oval(lx, ly, 0.1f, 0.035f, Color(0xFFA0663B), 1f)
        oval(lx - 0.09f, ly - 0.004f, 0.022f, 0.03f, Color(0xFFE3B27A), 0.8f)
    }
    // A lantern post with pennants down to the tent, and a stack of firewood.
    val px = -0.78f
    line(px, 0.04f, px, -0.55f, Ink.line, 2.6f)
    line(px, -0.52f, px + 0.12f, -0.52f, Ink.line, 1.6f)
    box(px + 0.085f, -0.5f, px + 0.155f, -0.42f, Color(0xFFFFE9A0), 0.9f)
    glow(px + 0.12f, -0.46f, 0.25f, Color(0xFFFFD76B), 0.35f)
    val pen0 = Path().apply { moveTo(px, -0.52f); quadraticTo((px - 0.15f) / 2f, -0.46f, -0.15f, -0.6f) }
    d.drawPath(pen0, Ink.line, style = Stroke(lw * 0.9f))
    val cols = listOf(Color(0xFFFF5A6E), Color(0xFFFFC83D), Color(0xFF5CE0A0), Color(0xFF5AA9E6))
    for (k in 0 until 5) {
        val f = 0.12f + k * 0.17f
        val x = mix(px, -0.15f, f)
        val y = mix(-0.52f, -0.6f, f) + 0.14f * f * (1f - f) * 2f
        fill(path(x - 0.03f, y, x + 0.03f, y, x, y + 0.07f), cols[k % 4])
    }
    for (r in 0 until 2) for (k in 0 until 3 - r) oval(-0.98f + k * 0.1f + r * 0.05f, 0.15f - r * 0.06f, 0.05f, 0.03f, Color(0xFFC98A55), 0.9f)
    tufts(-0.5f, 0.1f, 0.8f, 1.1f, y = 0.42f, s = 0.06f)
}

private fun Bx.campLive() {
    val fx = 0.42f
    val fy = 0.2f
    for ((k, pr) in listOf(0.085f to 0f, 0.06f to 1.7f, 0.05f to 3.1f).withIndex()) {
        val sp = pr.first * 0.5f
        val fl = 1f + 0.18f * sin(t * 11f + pr.second)
        val ox = (k - 1) * 0.045f
        val p = Path().apply {
            moveTo(fx + ox - sp * 0.6f, fy - 0.02f)
            quadraticTo(fx + ox - sp * 0.7f, fy - 0.1f * fl - sp, fx + ox + sin(t * 7f + k) * 0.012f, fy - 0.2f * fl - sp * 0.9f)
            quadraticTo(fx + ox + sp * 0.7f, fy - 0.1f * fl - sp, fx + ox + sp * 0.6f, fy - 0.02f)
            close()
        }
        fill(p, if (k == 0) Color(0xFFFF8A3D) else if (k == 1) Color(0xFFFFB84D) else Color(0xFFFFE066))
        ink(p, 0.8f, 0.7f)
    }
    smokeAt(fx, fy - 0.3f, 1.3f, dark = true)
}

// ------------------------------------------------------------------------------------- LAB (cave)

/** The glow of a crystal cluster: a steady base here, and a pulse on top from [crystalPulse]. */
private fun Bx.crystals(x: Float, y: Float, s: Float, lean: Float, col: Color, @Suppress("UNUSED_PARAMETER") phase: Float) {
    glow(x, y - s * 0.4f, s * 1.6f, col, 0.35f * 0.5f)
    if (lit <= 0f) d.drawCircle(col, s * 1.2f, Offset(x, y - s * 0.4f), alpha = 0.12f * 0.5f)
    val specs = floatArrayOf(-0.3f, 0.6f, 0.16f, -18f, 0f, 1f, 0.2f, 0f, 0.3f, 0.7f, 0.17f, 16f)
    for (k in 0 until 3) {
        val ang = Math.toRadians((specs[k * 4 + 3] + lean).toDouble()).toFloat()
        val ux = sin(ang)
        val uy = -cos(ang)
        val qx = cos(ang)
        val qy = sin(ang)
        val bx = x + specs[k * 4] * s
        val len = specs[k * 4 + 1] * s
        val wd = specs[k * 4 + 2] * s
        val p = path(bx - qx * wd / 2f, y - qy * wd / 2f, bx - qx * wd / 2f + ux * len * 0.7f, y - qy * wd / 2f + uy * len * 0.7f, bx + ux * len, y + uy * len, bx + qx * wd / 2f + ux * len * 0.7f, y + qy * wd / 2f + uy * len * 0.7f, bx + qx * wd / 2f, y + qy * wd / 2f)
        fill(p, col.darken(0.1f))
        val hi = path(bx - qx * wd / 2f, y - qy * wd / 2f, bx - qx * wd / 2f + ux * len * 0.7f, y - qy * wd / 2f + uy * len * 0.7f, bx + ux * len, y + uy * len, bx, y)
        fill(hi, col.lighten(0.4f))
        ink(p, 0.9f)
    }
}

private fun Bx.crystalPulse(x: Float, y: Float, s: Float, col: Color, phase: Float) {
    val pulse = 0.5f + 0.5f * sin(t * 1.6f + phase)
    glow(x, y - s * 0.4f, s * 1.6f, col, 0.35f * 0.5f * pulse)
    if (lit <= 0f) d.drawCircle(col, s * 1.2f, Offset(x, y - s * 0.4f), alpha = 0.12f * 0.5f * pulse)
}

private fun Bx.caveLive() {
    crystalPulse(-0.46f, 0.02f, 0.3f, Color(0xFF6FF2FF), 0f)
    crystalPulse(0.52f, 0.0f, 0.26f, Color(0xFFFF7BD8), 2.1f)
    crystalPulse(0.05f, -0.76f, 0.2f, Color(0xFFB58CFF), 4f)
    val pulse = 0.5f + 0.5f * sin(t * 1.7f)
    d.drawCircle(Color(0xFF6FF2FF), 0.3f, Offset(0f, -0.15f), alpha = 0.12f * 0.5f * pulse)
    d.drawCircle(Color(0xFFB58CFF), 0.17f, Offset(0f, -0.15f), alpha = 0.22f * 0.5f * pulse)
}

private fun Bx.cave() {
    // Shadow, the rocky knoll and its shaded side.
    shadowOf(-0.75f, 0f, 0.75f, 0f, 0.9f, 0.12f, 1.25f, 0.16f)
    contact(0f, 0.75f, 0.1f)
    val mound = Path().apply {
        moveTo(-0.78f, 0f)
        quadraticTo(-0.82f, -0.55f, -0.36f, -0.78f)
        quadraticTo(0.05f, -0.98f, 0.42f, -0.72f)
        quadraticTo(0.86f, -0.5f, 0.8f, 0f)
        close()
    }
    fill(mound, Color(0xFF8E8CA4))
    val shade = Path().apply {
        moveTo(0.2f, -0.9f)
        quadraticTo(0.5f, -0.86f, 0.42f, -0.72f)
        quadraticTo(0.86f, -0.5f, 0.8f, 0f)
        lineTo(0.3f, 0f)
        quadraticTo(0.38f, -0.4f, 0.2f, -0.9f)
        close()
    }
    fill(shade, Color(0xFF615F7C))
    val facets = Path().apply {
        moveTo(-0.6f, -0.3f); lineTo(-0.45f, -0.62f); lineTo(-0.36f, -0.3f); close()
        moveTo(-0.1f, -0.7f); lineTo(0.04f, -0.88f); lineTo(0.14f, -0.6f); close()
    }
    fill(facets, Color(0xFFA9A7BC))
    ink(mound, 1.4f)
    // The mouth: dark with a glowing heart and a fringe of stalactites.
    val mouth = Path().apply {
        moveTo(-0.27f, 0f)
        lineTo(-0.27f, -0.3f)
        quadraticTo(-0.27f, -0.55f, 0f, -0.55f)
        quadraticTo(0.27f, -0.55f, 0.27f, -0.3f)
        lineTo(0.27f, 0f)
        close()
    }
    fill(mouth, Color(0xFF1E1730))
    d.drawCircle(Color(0xFF6FF2FF), 0.3f, Offset(0f, -0.15f), alpha = 0.12f * 0.5f)
    d.drawCircle(Color(0xFFB58CFF), 0.17f, Offset(0f, -0.15f), alpha = 0.22f * 0.5f)
    for (k in 0 until 4) {
        val x = -0.16f + k * 0.107f
        val ht = 0.035f + 0.03f * ((k * 7) % 3) / 2f
        fill(path(x - 0.028f, -0.5f + 0.02f * sin(k * 1.4f), x + 0.028f, -0.5f + 0.02f * sin(k * 1.4f), x, -0.5f + ht), Color(0xFF5E5A7C))
    }
    ink(mouth, 1.3f)
    // Steps up to the cave, crystals, mushrooms and a runic signpost.
    for (k in 0 until 3) {
        val w2 = 0.3f + 0.08f * k
        d.drawRect(c(Color(0xFFB9B2C0).darken(0.05f * k)), Offset(-w2 / 2f, 0.0f + 0.025f * k), Size(w2, 0.028f))
        d.drawRect(Ink.line, Offset(-w2 / 2f, 0.0f + 0.025f * k), Size(w2, 0.028f), alpha = 0.7f, style = Stroke(lw * 0.8f))
    }
    crystals(-0.46f, 0.02f, 0.3f, -14f, Color(0xFF6FF2FF), 0f)
    crystals(0.52f, 0.0f, 0.26f, 12f, Color(0xFFFF7BD8), 2.1f)
    crystals(0.05f, -0.76f, 0.2f, 4f, Color(0xFFB58CFF), 4f)
    for ((mx, my, mc) in listOf(Triple(-0.72f, 0.1f, Color(0xFF5FF0D0)), Triple(0.75f, 0.14f, Color(0xFFFFA8F0)), Triple(-0.25f, 0.2f, Color(0xFF5FF0D0)))) {
        line(mx, my, mx, my - 0.05f, Color(0xFFF1E8FF), 2.4f)
        oval(mx, my - 0.06f, 0.05f, 0.032f, mc, 0.9f)
        glow(mx, my - 0.05f, 0.14f, mc, 0.3f)
    }
    val sx = -0.98f
    line(sx, 0.08f, sx, -0.3f, Ink.line, 2.6f)
    box(sx - 0.1f, -0.42f, sx + 0.1f, -0.25f, Color(0xFFC98A55), 1f)
    val spiral = Path().apply {
        for (k in 0..18) {
            val a = k * 0.55f
            val r2 = 0.045f * k / 18f
            val px = sx + cos(a) * r2
            val py = -0.335f + sin(a) * r2
            if (k == 0) moveTo(px, py) else lineTo(px, py)
        }
    }
    d.drawPath(spiral, c(Color(0xFF6FF2FF)), style = Stroke(lw * 1.3f, cap = StrokeCap.Round))
    tufts(-0.6f, 0.4f, 0.95f, y = 0.32f, s = 0.05f, )
}

// ------------------------------------------------------------------------------------- MOUNTAIN (Fjellet)

private fun Bx.fjellet() {
    val snowW = Color(0xFFF7FAFF)
    // The groomed run on the slope above the cabin with ski tracks and slalom poles.
    val run = Path().apply {
        moveTo(-0.08f, -0.64f); quadraticTo(-0.42f, -0.5f, -0.3f, -0.3f); quadraticTo(-0.18f, -0.14f, -0.6f, -0.02f)
        lineTo(-0.2f, -0.02f); quadraticTo(0.1f, -0.16f, 0.05f, -0.32f); quadraticTo(0.0f, -0.5f, 0.1f, -0.64f); close()
    }
    fill(run, snowW)
    d.drawPath(run, Ink.line, alpha = 0.45f, style = Stroke(lw))
    for (off in floatArrayOf(-0.04f, 0.02f)) {
        val tr = Path().apply { moveTo(0.0f + off, -0.6f); quadraticTo(-0.3f + off, -0.48f, -0.2f + off, -0.3f); quadraticTo(-0.1f + off, -0.14f, -0.4f + off, -0.04f) }
        d.drawPath(tr, c(Color(0xFFA9BCDF)), style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
    }
    for (k in 0 until 4) {
        val f = 0.15f + k * 0.22f
        val px = -0.32f + 0.3f * sin(f * 5f) * 0.5f - f * 0.05f
        val py = -0.56f + f * 0.52f
        line(px, py, px, py - 0.08f, if (k % 2 == 0) Color(0xFFE94F4F) else Color(0xFF3E7BD6), 2f)
    }
    // The chairlift's bottom station and its cable to the top.
    box(0.55f, -0.22f, 0.8f, 0f, Color(0xFFA0663B), 1.1f)
    face(Color(0xFFF7FAFF), 0.5f, -0.22f, 0.675f, -0.36f, 0.85f, -0.22f)
    line(0.675f, -0.3f, 0.06f, -0.66f, Ink.line, 1.5f)
    line(0.675f, -0.28f, 0.06f, -0.64f, Ink.line, 0.6f, 0.5f)
    for (px in floatArrayOf(0.4f, 0.22f)) {
        val py = -0.3f + (0.675f - px) / 0.615f * -0.36f
        line(px, py + 0.03f, px, py - 0.05f, Ink.line, 2f)
    }
    // The log cabin under a snowy roof.
    val hw = 0.4f
    val wallH = 0.3f
    val roofH = 0.24f
    val dep = 0.42f
    shadowHouse(-hw, hw, dep, wallH, roofH, false)
    contact(-0.05f, 0.5f)
    longHouse(hw, wallH, roofH, dep, Color(0xFFA0663B), Color(0xFFB0774B), trim = Color(0xFF7A4B2E))
    for (k in 1..5) line(-hw, -wallH * k / 6f, hw, -wallH * k / 6f, Color(0xFF7A4B2E), 0.8f, 0.6f)
    val cap = path(-hw - 0.06f, -wallH + 0.04f, hw + 0.06f, -wallH + 0.04f, hw + 0.06f + 0.22f, -wallH - roofH + 0.04f - 0.09f, -hw - 0.06f + 0.22f, -wallH - roofH + 0.04f - 0.09f)
    fill(cap, snowW, 0.95f)
    ink(cap, 1f)
    win(-0.3f, -0.25f, -0.14f, -0.13f, shutters = true)
    door(0.05f, 0.2f, 0.22f, Color(0xFF6E4A33), steps = false)
    box(0.22f, -0.3f - roofH * 0.45f, 0.3f, -wallH - roofH * 0.45f + 0.26f, Color(0xFF8C8793), 0.9f)
    chimney(0.12f, -wallH - roofH * 0.55f, 0.07f, 0.16f)
    // Skis stuck in the snow, a sledge and snowy pines.
    for (k in 0 until 2) line(0.34f + k * 0.04f, 0.1f, 0.3f + k * 0.04f, -0.14f, if (k == 0) Color(0xFFE94F4F) else Color(0xFF3E7BD6), 2.4f)
    box(-0.6f, 0.09f, -0.34f, 0.12f, Color(0xFFC98A55), 0.9f)
    line(-0.62f, 0.14f, -0.32f, 0.14f, Color(0xFF7A5134), 1.4f)
    pineTree(-0.95f, 0.04f, 0.6f)
    pineTree(1.05f, -0.05f, 0.7f)
    pineTree(0.88f, 0.12f, 0.5f)
    tufts(-0.8f, 0.0f, 0.6f, y = 0.3f, s = 0.05f)
}

private fun Bx.fjelletLive() {
    // Two tiny skiers sliding down.
    for (k in 0 until 2) {
        val f = wrap(t * 0.07f + k * 0.5f, 1f)
        val px = mix(0.0f, -0.4f, f) - 0.1f * sin(f * 9f)
        val py = mix(-0.6f, -0.04f, f)
        line(px - 0.04f, py + 0.01f, px + 0.05f, py - 0.005f, Ink.line, 1.4f)
        round(px - 0.012f, py - 0.06f, px + 0.014f, py, 0.008f, if (k == 0) Color(0xFFE94F4F) else Color(0xFF3E7BD6), 0.8f)
        disc(px, py - 0.075f, 0.014f, Color(0xFFF2C29B), 0.7f)
    }
    // The chairs gliding up the lift.
    for (k in 0 until 3) {
        val f = wrap(t * 0.05f + k / 3f, 1f)
        val px = mix(0.675f, 0.06f, f)
        val py = mix(-0.3f, -0.66f, f)
        line(px, py, px, py + 0.05f, Ink.line, 1f)
        round(px - 0.03f, py + 0.05f, px + 0.03f, py + 0.08f, 0.01f, Color(0xFFE94F4F), 0.8f)
    }
    chimneySmoke(0.12f, -0.3f - 0.24f * 0.55f, 0.07f, 0.16f)
}

// ------------------------------------------------------------------------------------- HEILEBERGET

private fun Bx.heileberget(stx: Float, sty: Float) {
    // A hut just under the crest: a stone footing, timber walls, a steep snowy roof and a waving flag.
    val hx = 0.3f
    val hw = 0.3f
    val wallH = 0.26f
    val roofH = 0.3f
    val dep = 0.4f
    d.withTransform({ translate(hx, 0f) }) {
        shadowHouse(-hw, hw, dep, wallH, roofH, true)
        contact(0f, 0.45f)
        gable(hw, wallH, roofH, dep, Color(0xFFC98A55), Color(0xFF8C5A32), trim = Color(0xFFF7F3EC))
        val cap = path(-hw - 0.05f, -wallH + 0.04f, 0f, -wallH - roofH - 0.03f, hw + 0.05f, -wallH + 0.04f, hw + 0.05f - 0.03f, -wallH + 0.0f, 0f, -wallH - roofH + 0.06f, -hw - 0.02f, -wallH + 0.0f)
        fill(cap, Color(0xFFF7FAFF), 0.95f)
        ink(cap, 1f)
        d.drawRect(c(Color(0xFF8C8793)), Offset(-hw - 0.01f, -0.09f), Size(2f * hw + 0.02f, 0.09f))
        d.drawRect(Ink.line, Offset(-hw - 0.01f, -0.09f), Size(2f * hw + 0.02f, 0.09f), style = Stroke(lw))
        for (k in 0 until 4) line(-hw + 0.15f * k, -0.09f, -hw + 0.15f * k, 0f, Ink.line, 0.6f, 0.5f)
        win(-0.22f, -0.22f, -0.1f, -0.13f, shutters = true)
        door(0.03f, 0.18f, 0.2f, Color(0xFF6E4A33), steps = false)
        win(-0.04f, -0.46f, 0.04f, -0.38f, round = true)
        flag(0f, -wallH - roofH - 0.02f, 0.3f, Color(0xFFE94F4F), 0.22f)
    }
    // The top station of the cable car: a little timber house with a great wheel.
    d.withTransform({ translate(stx, sty) }) {
        contact(0f, 0.3f)
        box(-0.15f, -0.2f, 0.15f, 0f, Color(0xFFA0663B), 1.1f)
        face(Color(0xFFF7FAFF), -0.2f, -0.2f, 0f, -0.34f, 0.2f, -0.2f)
        d.drawCircle(c(Color(0xFF8C8793)), 0.07f, Offset(0.13f, -0.26f))
        d.drawCircle(Ink.line, 0.07f, Offset(0.13f, -0.26f), style = Stroke(lw * 1.4f))
        win(-0.08f, -0.16f, 0.03f, -0.07f)
    }
    // Prayer-flag pennants from the hut to the station.
    val cols = listOf(Color(0xFF5AA9E6), Color(0xFFFFFFFF), Color(0xFFE94F4F), Color(0xFF5CE0A0), Color(0xFFFFC83D))
    for (k in 0 until 5) {
        val f = 0.12f + k * 0.17f
        val x = mix(stx, hx - 0.3f, f)
        val y = mix(sty - 0.25f, -0.25f, f) + 0.06f * f * (1f - f) * 2.5f
        fill(path(x - 0.022f, y, x + 0.022f, y, x, y + 0.055f), cols[k])
    }
    line(stx, sty - 0.25f, hx - 0.3f, -0.25f, Ink.line, 0.7f, 0.8f)
    tufts(0.0f, 0.6f, y = 0.16f, s = 0.045f)
}

private fun Bx.heilebergetLive(@Suppress("UNUSED_PARAMETER") stx: Float, @Suppress("UNUSED_PARAMETER") sty: Float) {
    // The flag on the hut waves.
    val wallH = 0.26f
    val roofH = 0.3f
    d.withTransform({ translate(0.3f, 0f) }) {
        flagLive(0f, -wallH - roofH - 0.02f, 0.3f, Color(0xFFE94F4F), 0.22f)
        line(0.02f, -wallH - roofH - 0.23f, 0.17f, -wallH - roofH - 0.205f, Color.White, 1.5f)
    }
}

// ------------------------------------------------------------------------------------- FARM

private fun Bx.farm() {
    val hay = Color(0xFFE2BE5C)
    // The silo and the barn.
    val sx = 0.92f
    shadowOf(0.66f, 0f, 1.12f, 0f, 1.6f, 0.16f, 1.9f, 0.2f)
    shadowOf(-0.5f, 0f, 0.52f, 0f, 1.0f, 0.14f, 0.9f, 0.18f)
    contact(0.2f, 0.85f, 0.1f)
    val silo = Path().apply {
        moveTo(sx - 0.14f, -0.72f); lineTo(sx - 0.14f, 0f); arcTo(Rect(sx - 0.14f, -0.035f, sx + 0.14f, 0.035f), 180f, -180f, false); lineTo(sx + 0.14f, -0.72f); close()
    }
    fill(silo, Color(0xFFD5DBE5))
    fill(Path().apply { moveTo(sx + 0.03f, -0.72f); lineTo(sx + 0.14f, -0.72f); lineTo(sx + 0.14f, 0f); lineTo(sx + 0.03f, 0.035f); close() }, Color(0xFFD5DBE5).darken(0.22f))
    for (k in 1..4) line(sx - 0.14f, -0.72f + k * 0.14f, sx + 0.14f, -0.72f + k * 0.14f, Color(0xFF8C93A3), 0.9f, 0.6f)
    ink(silo, 1.2f)
    val dome = Path().apply { moveTo(sx - 0.16f, -0.72f); quadraticTo(sx, -0.98f, sx + 0.16f, -0.72f); close() }
    fill(dome, Color(0xFFB0B8C8))
    ink(dome, 1.2f)
    val pts = floatArrayOf(-0.52f, -0.34f, -0.4f, -0.6f, 0f, -0.76f, 0.4f, -0.6f, 0.52f, -0.34f, 0.52f, 0f, -0.52f, 0f)
    prism(pts, 0.55f, Color(0xFFC0392F), Color(0xFF8C2A24), Color(0xFF55505C))
    // White trim along the roof edges and corners, X-braced doors and the hayloft.
    val trim = Path().apply { moveTo(-0.52f, -0.34f); lineTo(-0.4f, -0.6f); lineTo(0f, -0.76f); lineTo(0.4f, -0.6f); lineTo(0.52f, -0.34f) }
    d.drawPath(trim, c(Color(0xFFF7F3EC)), style = Stroke(lw * 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    d.drawPath(trim, Ink.line, alpha = 0.8f, style = Stroke(lw * 0.8f))
    for (px in floatArrayOf(-0.52f, 0.5f)) d.drawRect(c(Color(0xFFF7F3EC)), Offset(px, -0.34f), Size(0.03f, 0.34f))
    val door = Rect(-0.27f, -0.3f, 0.27f, 0f)
    d.drawRect(c(Color(0xFF8C2A24)), door.topLeft, door.size)
    d.drawRect(c(Color(0xFFC0392F)), Offset(-0.27f, -0.3f), Size(0.27f, 0.3f))
    d.drawLine(c(Color(0xFFF7F3EC)), Offset(-0.27f, -0.3f), Offset(0f, 0f), strokeWidth = lw * 2.2f)
    d.drawLine(c(Color(0xFFF7F3EC)), Offset(0f, -0.3f), Offset(-0.27f, 0f), strokeWidth = lw * 2.2f)
    d.drawLine(c(Color(0xFFF7F3EC)), Offset(0f, -0.3f), Offset(0.27f, 0f), strokeWidth = lw * 2.2f)
    d.drawLine(c(Color(0xFFF7F3EC)), Offset(0.27f, -0.3f), Offset(0f, 0f), strokeWidth = lw * 2.2f)
    d.drawRect(c(Color(0xFFF7F3EC)), door.topLeft, door.size, style = Stroke(lw * 2.6f))
    d.drawRect(Ink.line, Offset(door.left - lw, door.top - lw), Size(door.width + 2 * lw, door.height + lw), style = Stroke(lw * 0.8f))
    val loft = Rect(-0.1f, -0.56f, 0.1f, -0.42f)
    d.drawRect(c(Color(0xFF2B1E2F)), loft.topLeft, loft.size)
    d.drawPath(Path().apply { moveTo(loft.left, loft.bottom); quadraticTo(0f, loft.bottom - 0.08f, loft.right, loft.bottom); close() }, c(hay))
    d.drawRect(c(Color(0xFFF7F3EC)), loft.topLeft, loft.size, style = Stroke(lw * 2f))
    line(0f, -0.62f, 0f, -0.68f, Ink.line, 1.8f)
    // Round hay bales, a fence round the pasture and animals grazing.
    for ((bx, by) in listOf(-0.8f to 0.1f, -0.68f to 0.06f)) {
        disc(bx, by - 0.07f, 0.07f, hay, 1f)
        d.drawCircle(c(hay.darken(0.25f)), 0.045f, Offset(bx, by - 0.07f), style = Stroke(lw * 0.9f))
        d.drawCircle(c(hay.darken(0.25f)), 0.02f, Offset(bx, by - 0.07f), style = Stroke(lw * 0.9f))
    }
    fence(-1.75f, 1.75f, 0.3f, -0.2f, 0.2f, Color(0xFFC9A27A), 0.1f)
    tufts(-1.6f, -0.9f, -0.3f, 0.4f, 1.0f, y = 0.44f, s = 0.055f)
}

private fun Bx.farmLive() {
    cow(-1.3f, 0.2f, 0.42f, t * 0.9f)
    cow(-1.0f, 0.3f, 0.36f, t * 0.9f + 2f, flip = true)
    cow(1.35f, 0.22f, 0.4f, t * 0.9f + 4f, flip = true)
    sheep(1.65f, 0.14f, 0.3f, t * 1.1f + 1f)
    sheep(1.5f, 0.28f, 0.27f, t * 1.1f + 3f)
    // A little red tractor driving back and forth along the track.
    val ph = wrap(t / 26f, 2f)
    val tp = if (ph < 1f) ph else 2f - ph
    val tx = mix(-1.0f, 0.9f, tp)
    val flip = if (ph < 1f) 1f else -1f
    d.withTransform({ translate(tx, 0.46f); scale(flip, 1f, Offset.Zero) }) {
        d.drawOval(Ink.line, Offset(-0.22f, -0.02f), Size(0.44f, 0.07f), alpha = 0.2f)
        round(-0.15f, -0.17f, 0.12f, -0.07f, 0.02f, Color(0xFFE94F4F), 1f)
        round(-0.05f, -0.3f, 0.07f, -0.17f, 0.02f, Color(0xFFE94F4F), 1f)
        d.drawRect(lerp(c(Color(0xFFBFE6F5)), Color(0xFFFFD76B), lit), Offset(-0.035f, -0.27f), Size(0.08f, 0.07f))
        line(0.08f, -0.17f, 0.08f, -0.26f, Ink.line, 1.6f)
        disc(-0.1f, -0.06f, 0.075f, Color(0xFF3B3346), 1f)
        disc(-0.1f, -0.06f, 0.035f, Color(0xFFFFC83D), 0.6f)
        disc(0.1f, -0.03f, 0.04f, Color(0xFF3B3346), 1f)
        d.drawCircle(Color.White, 0.02f + 0.025f * wrap(t * 1.5f, 1f), Offset(0.08f + 0.06f * wrap(t * 1.5f, 1f), -0.3f - 0.1f * wrap(t * 1.5f, 1f)), alpha = 0.6f * (1f - wrap(t * 1.5f, 1f)))
    }
}

// ------------------------------------------------------------------------------------- BEACH

private fun Bx.beach() {
    val sand = Color(0xFFF3DFA8)
    // The boathouses: a bigger red one and a white-and-blue one further along the shore.
    shadowHouse(-0.6f, -0.06f, 0.4f, 0.28f, 0.22f, true)
    d.withTransform({ translate(-0.92f, -0.02f) }) {
        contact(0f, 0.4f)
        gable(0.2f, 0.22f, 0.17f, 0.3f, Color(0xFFF5F5F0), Color(0xFF3E7BD6))
        door(-0.06f, 0.08f, 0.15f, Color(0xFF3E7BD6), steps = false)
    }
    contact(-0.33f, 0.52f)
    d.withTransform({ translate(-0.33f, 0f) }) {
        gable(0.27f, 0.3f, 0.22f, 0.4f, Color(0xFFC0463A), Color(0xFF4A4A57))
        door(-0.14f, 0.02f, 0.22f, Color(0xFF6E4A33), steps = false)
        win(0.09f, -0.26f, 0.2f, -0.17f)
        val boat = path(-0.05f, -0.35f, 0.05f, -0.35f, 0.05f, -0.42f, -0.05f, -0.42f)
        d.drawCircle(c(Color(0xFFFFFFFF)), 0.045f, Offset(0f, -0.42f))
        d.drawCircle(Ink.line, 0.045f, Offset(0f, -0.42f), style = Stroke(lw))
        d.drawCircle(c(Color(0xFFE94F4F)), 0.022f, Offset(0f, -0.42f), style = Stroke(lw * 2f))
    }
    // The pier reaching out into the water with a boat tied up.
    val px0 = 0.46f
    val px1 = 1.5f
    val deckY = 0.06f
    for (k in 0..6) {
        val x = mix(px0 + 0.05f, px1 - 0.05f, k / 6f)
        line(x, deckY + 0.03f, x, deckY + 0.23f, Color(0xFF55402F), 3f)
    }
    val top = path(px0, deckY, px1, deckY, px1 + 0.11f, deckY - 0.08f, px0 + 0.11f, deckY - 0.08f)
    fill(top, Color(0xFFDDB27A))
    val planks = ArrayList<Offset>()
    var xx = px0 + 0.03f
    while (xx < px1) {
        planks.add(Offset(xx, deckY))
        planks.add(Offset(xx + 0.11f, deckY - 0.08f))
        xx += 0.085f
    }
    d.drawPoints(planks, PointMode.Lines, c(Color(0xFF8C5A32)), strokeWidth = lw * 0.8f, alpha = 0.7f)
    ink(top, 1.1f)
    box(px0, deckY, px1, deckY + 0.04f, Color(0xFFA0663B), 1f)
    val by = 0.28f
    val bobY = 0f
    val bx = 1.15f
    d.drawOval(Ink.line, Offset(bx - 0.25f, by + 0.03f + bobY), Size(0.5f, 0.06f), alpha = 0.22f)
    val hull = path(bx - 0.22f, by - 0.06f + bobY, bx + 0.22f, by - 0.06f + bobY, bx + 0.15f, by + 0.03f + bobY, bx - 0.15f, by + 0.03f + bobY)
    val sail = path(bx + 0.02f, by - 0.52f + bobY, bx + 0.2f, by - 0.1f + bobY, bx + 0.02f, by - 0.1f + bobY)
    val jib = path(bx - 0.02f, by - 0.44f + bobY, bx - 0.02f, by - 0.1f + bobY, bx - 0.18f, by - 0.1f + bobY)
    line(bx, by - 0.06f + bobY, bx, by - 0.54f + bobY, Ink.line, 1.6f)
    fill(sail, Color(0xFFFFFFFF))
    ink(sail, 1f)
    fill(jib, Color(0xFFFFE08A))
    ink(jib, 1f)
    fill(hull, Color(0xFFE2504A))
    ink(hull, 1.2f)
    line(px1 - 0.02f, deckY - 0.02f, bx + 0.15f, by - 0.06f + bobY, Ink.line, 0.8f, 0.8f)
    // A seagull on a post.
    line(px1 - 0.05f, deckY - 0.02f, px1 - 0.05f, deckY - 0.2f, Ink.line, 2.4f)
    oval(px1 - 0.05f, deckY - 0.24f, 0.04f, 0.028f, Color(0xFFFFFFFF), 0.9f)
    disc(px1 - 0.015f, deckY - 0.27f, 0.018f, Color(0xFFFFFFFF), 0.8f)
    // Parasol, towel, ball, sandcastle and a net drying rack.
    val tow = path(-0.05f, 0.27f, 0.34f, 0.27f, 0.4f, 0.2f, 0.01f, 0.2f)
    fill(tow, Color(0xFF4FB3E8))
    for (k in 0 until 3) fill(path(0.02f + k * 0.11f, 0.27f, 0.05f + k * 0.11f, 0.27f, 0.11f + k * 0.11f, 0.2f, 0.08f + k * 0.11f, 0.2f), Color(0xFFFFF4D6))
    ink(tow, 1f)
    parasol(0.1f, 0.22f, 0.22f, Color(0xFFE94F4F), Color(0xFFFFFFFF))
    disc(0.52f, 0.3f, 0.04f, Color(0xFFFFC83D), 0.9f)
    d.drawArc(Ink.line, 200f, 120f, false, Offset(0.49f, 0.27f), Size(0.06f, 0.06f), style = Stroke(lw))
    val cas = path(-0.74f, 0.27f, -0.54f, 0.27f, -0.56f, 0.2f, -0.6f, 0.2f, -0.6f, 0.16f, -0.64f, 0.16f, -0.64f, 0.2f, -0.72f, 0.2f)
    fill(cas, Color(0xFFE2C58B))
    ink(cas, 0.9f)
    flag(-0.64f, 0.16f, 0.1f, Color(0xFFE94F4F), 0.06f)
    for (nx in floatArrayOf(-1.5f, -1.25f)) line(nx, 0.24f, nx, -0.1f, Ink.line, 2.4f)
    val net = Path().apply {
        moveTo(-1.5f, -0.08f)
        quadraticTo(-1.375f, 0.04f, -1.25f, -0.08f)
        for (k in 1..4) { moveTo(-1.5f + k * 0.05f, -0.08f); lineTo(-1.5f + k * 0.05f + 0.01f, 0.03f) }
    }
    d.drawPath(net, c(Color(0xFF8C7A5A)), style = Stroke(lw * 1.1f))
    tufts(-1.6f, -1.0f, 0.75f, y = 0.4f, s = 0.05f, )
}

private fun Bx.beachLive() {
    flagLive(-0.64f, 0.16f, 0.1f, Color(0xFFE94F4F), 0.06f)
}

// ------------------------------------------------------------------------------------- UNDERWATER

private fun Bx.dive() {
    // What lies below: a hint of a wreck on the sea floor, drawn faint under the surface.
    val wreck = Path().apply {
        moveTo(-0.5f, 0.42f); lineTo(-0.58f, 0.3f); lineTo(0.3f, 0.28f); lineTo(0.42f, 0.36f); lineTo(0.3f, 0.46f); close()
    }
    d.drawPath(wreck, c(Color(0xFF1B3F73)), alpha = 0.55f)
    line(-0.05f, 0.3f, -0.12f, 0.1f, c(Color(0xFF1B3F73)), 2.2f, 0.55f)
    line(-0.12f, 0.1f, 0.08f, 0.08f, c(Color(0xFF1B3F73)), 1.6f, 0.55f)
    for (k in 0 until 4) disc(-0.3f + k * 0.18f, 0.35f, 0.03f, Color(0xFF6FB8E8), 0f).also { d.drawCircle(Color(0xFF9FD8F5), 0.028f, Offset(-0.3f + k * 0.18f, 0.35f), alpha = 0.3f) }
}

private fun Bx.diveLive() {
    // Ripples spreading from the buoy and bubbles rising.
    for (k in 0 until 2) {
        val ph = wrap(t * 0.4f + k * 0.5f, 1f)
        val rw = 0.4f + ph * 0.6f
        d.drawOval(Color.White, Offset(-rw, -rw * 0.17f), Size(rw * 2f, rw * 0.34f), alpha = 0.6f * (1f - ph), style = Stroke(lw * 1.2f))
    }
    for (k in 0 until 5) {
        val ph = wrap(t * 0.5f + k / 5f, 1f)
        d.drawCircle(Color.White, 0.022f + 0.02f * ph, Offset(0.2f + 0.06f * sin(ph * 8f + k), 0.3f - ph * 0.3f), alpha = 0.85f * (1f - ph), style = Stroke(lw * 1.1f))
    }
    // The periscope looks about.
    val px = 0.46f
    val look = sin(t * 0.5f)
    line(px, 0.03f, px, -0.32f, Ink.line, 6.4f)
    line(px, 0.03f, px, -0.32f, Color(0xFFFFC83D), 4f)
    val head = Rect(px - 0.04f + look * 0.04f, -0.42f, px + 0.1f + look * 0.04f, -0.3f)
    round(head.left, head.top, head.right, head.bottom, 0.03f, Color(0xFFFFC83D), 1.1f)
    disc(head.right - 0.03f, head.center.y, 0.028f, Color(0xFF5AA9E6), 0.9f)
    // The buoy with its flag and a lamp that blinks at night.
    val bob = sin(t * 1.6f) * 0.03f
    val fl = Path().apply {
        moveTo(-0.22f, bob); quadraticTo(-0.24f, -0.3f + bob, 0f, -0.34f + bob); quadraticTo(0.24f, -0.3f + bob, 0.22f, bob); quadraticTo(0f, 0.08f + bob, -0.22f, bob); close()
    }
    fill(fl, Color(0xFFFFFFFF))
    val band = Path().apply { addRect(Rect(-0.3f, -0.22f + bob, 0.3f, -0.12f + bob)) }
    d.clipRect(-0.23f, -0.34f + bob, 0.23f, 0.08f + bob) { d.drawPath(band, c(Color(0xFFE94F4F))) }
    ink(fl, 1.3f)
    line(0f, -0.34f + bob, 0f, -0.7f + bob, Ink.line, 1.6f)
    val flagP = path(0f, -0.7f + bob, 0.24f + sin(t * 4f) * 0.02f, -0.62f + bob, 0f, -0.54f + bob)
    fill(flagP, Color(0xFFE94F4F))
    ink(flagP, 0.9f)
    line(0.02f, -0.66f + bob, 0.16f, -0.6f + bob, Color.White, 1.5f)
    if (lit > 0f && wrap(t, 1.4f) < 0.5f) {
        d.drawCircle(Color(0xFFFFE27A), 0.12f, Offset(0f, -0.72f + bob), alpha = 0.35f * lit)
        d.drawCircle(Color(0xFFFFF3B0), 0.04f, Offset(0f, -0.72f + bob), alpha = lit)
    }
}


// ------------------------------------------------------------------------------------- SPACE

/** The space station hanging in the sky from a balloon, inside a little round window onto space. */
internal fun MapPen.drawSpace(d: DrawScope, g: MapGeo, hl: Boolean = false) {
    val spot = mapSpot(PlaceId.SPACE)
    val c0 = Offset(spot.x * w, spot.y * h)
    val r = S * 0.5f
    val lift = if (hl) abs(sin(t * 3.4f)) * 0.014f * h else 0f
    val c = Offset(c0.x, c0.y - lift)
    // The balloon above holding it up.
    val bc = Offset(c.x + r * 0.15f, c.y - r * 1.95f)
    val br = r * 0.5f
    d.drawLine(Ink.line, Offset(bc.x - br * 0.5f, bc.y + br * 0.9f), Offset(c.x - r * 0.15f, c.y - r * 0.95f), strokeWidth = lw * 0.9f)
    d.drawLine(Ink.line, Offset(bc.x + br * 0.5f, bc.y + br * 0.9f), Offset(c.x + r * 0.35f, c.y - r * 0.92f), strokeWidth = lw * 0.9f)
    val env = Path().apply {
        moveTo(bc.x, bc.y + br * 1.0f)
        cubicTo(bc.x - br * 1.25f, bc.y + br * 0.3f, bc.x - br * 1.05f, bc.y - br * 1.05f, bc.x, bc.y - br * 1.05f)
        cubicTo(bc.x + br * 1.05f, bc.y - br * 1.05f, bc.x + br * 1.25f, bc.y + br * 0.3f, bc.x, bc.y + br * 1.0f)
        close()
    }
    d.drawPath(env, nt(Color(0xFF8B5CF6), 0.4f))
    d.drawPath(Path().apply { moveTo(bc.x, bc.y - br * 1.05f); cubicTo(bc.x + br * 0.55f, bc.y - br * 0.5f, bc.x + br * 0.55f, bc.y + br * 0.4f, bc.x, bc.y + br * 1.0f); cubicTo(bc.x + br * 1.25f, bc.y + br * 0.3f, bc.x + br * 1.05f, bc.y - br * 1.05f, bc.x, bc.y - br * 1.05f); close() }, nt(Color(0xFF6A3FD0), 0.4f))
    d.drawPath(env, Ink.line, style = Stroke(lw * 1.2f))
    // The round window onto space with stars and the planets.
    d.drawCircle(Pal.sun, r * 1.3f, c, alpha = if (hl) 0.3f else 0f)
    d.drawCircle(Color(0xFFDDE8FF), r * 1.08f, c, alpha = 0.35f)
    d.drawCircle(nt(Color(0xFF0E1538), 0.2f), r, c)
    val stars = ArrayList<Offset>(14)
    for (i in 0 until 16) {
        val a = hash01(i, 971) * 6.283f
        val rr = r * 0.9f * sqrt(hash01(i, 972))
        stars.add(Offset(c.x + cos(a) * rr, c.y + sin(a) * rr))
    }
    d.drawPoints(stars, PointMode.Points, Color(0xFFFFF7DA), strokeWidth = lw * 2.2f, cap = StrokeCap.Round, alpha = 0.7f + 0.3f * sin(t * 2f))
    val ang = t * 0.25f
    fun orbit(rr: Float, off: Float, size: Float, col: Color, ringed: Boolean = false) {
        val p = Offset(c.x + cos(ang * (1.3f - rr / r * 0.4f) + off) * rr, c.y + sin(ang * (1.3f - rr / r * 0.4f) + off) * rr * 0.55f)
        if (ringed) d.drawOval(Color(0xFFE6D3A0), Offset(p.x - size * 1.9f, p.y - size * 0.5f), Size(size * 3.8f, size), style = Stroke(lw * 1.4f))
        d.drawCircle(col, size, p)
        d.drawCircle(Ink.line, size, p, alpha = 0.6f, style = Stroke(lw * 0.8f))
    }
    orbit(r * 0.72f, 0f, r * 0.11f, Color(0xFF3F7BE0))
    orbit(r * 0.5f, 2.1f, r * 0.07f, Color(0xFFE2603C))
    orbit(r * 0.8f, 4.2f, r * 0.09f, Color(0xFFE8D29A), ringed = true)
    d.drawCircle(Color(0xFFFFD447), r * 0.11f, Offset(c.x - r * 0.62f, c.y + r * 0.45f))
    d.drawCircle(Ink.line, r, c, style = Stroke(lw * 2f))
    // The station itself in front, with its solar panels.
    val sc = Offset(c.x, c.y + sin(t * 0.9f) * r * 0.03f)
    for (side in intArrayOf(-1, 1)) {
        val panel = Rect(sc.x + side * r * 0.5f - r * 0.3f, sc.y - r * 0.12f, sc.x + side * r * 0.5f + r * 0.3f, sc.y + r * 0.12f)
        d.drawLine(Ink.line, Offset(sc.x + side * r * 0.2f, sc.y), Offset(sc.x + side * r * 0.3f, sc.y), strokeWidth = lw * 1.6f)
        d.drawRect(Color(0xFF3E6FD6), panel.topLeft, panel.size)
        val grid = ArrayList<Offset>(8)
        for (k in 1..3) {
            val gx = panel.left + panel.width * k / 4f
            grid.add(Offset(gx, panel.top)); grid.add(Offset(gx, panel.bottom))
        }
        grid.add(Offset(panel.left, panel.center.y)); grid.add(Offset(panel.right, panel.center.y))
        d.drawPoints(grid, PointMode.Lines, Color(0xFF9CC4FF), strokeWidth = lw * 0.7f)
        d.drawRect(Ink.line, panel.topLeft, panel.size, style = Stroke(lw))
    }
    d.drawRoundRect(Ink.line, Offset(sc.x - r * 0.26f, sc.y - r * 0.2f), Size(r * 0.52f, r * 0.4f), androidx.compose.ui.geometry.CornerRadius(r * 0.12f))
    d.drawRoundRect(Color(0xFFE9EEF3), Offset(sc.x - r * 0.26f + lw, sc.y - r * 0.2f + lw), Size(r * 0.52f - lw * 2f, r * 0.4f - lw * 2f), androidx.compose.ui.geometry.CornerRadius(r * 0.1f))
    d.drawCircle(Color(0xFF6FD8FF), r * 0.07f, Offset(sc.x - r * 0.08f, sc.y))
    d.drawCircle(Ink.line, r * 0.07f, Offset(sc.x - r * 0.08f, sc.y), style = Stroke(lw))
    d.drawLine(Ink.line, Offset(sc.x + r * 0.1f, sc.y - r * 0.2f), Offset(sc.x + r * 0.14f, sc.y - r * 0.36f), strokeWidth = lw * 1.2f)
    d.drawCircle(Color(0xFFFF5A6E), r * 0.04f, Offset(sc.x + r * 0.14f, sc.y - r * 0.37f), alpha = if (wrap(t, 1.2f) < 0.35f) 1f else 0.2f)
    // A twinkling trail.
    for (k in 1..7) {
        val a = ang * 2f - k * 0.3f
        val q = Offset(c.x + cos(a) * r * 1.22f, c.y + sin(a) * r * 0.7f)
        twinkleLive(d, q, r * (0.12f - k * 0.012f), Color(0xFFFFF3B0), (1f - k / 8f) * (0.4f + 0.6f * (0.5f + 0.5f * sin(t * 5f + k * 1.3f))))
    }
}
