package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The furniture of Heileberget in oblique 3D: the mountain hut with its turf roof, the two cable-car
 * stations and the cabin that rides between them, the rock ledge, the snowy summit block, the echo rock
 * with its funny face, the eagle nest and the summit flag. Origin at the bottom centre of each fixture's
 * front face; depth recedes up and to the right. Private helpers start with `br`.
 */

/** Draws the back layer of Heileberget's furniture. Returns false for any other type. */
@Suppress("UNUSED_PARAMETER")
fun DrawScope.drawBergBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        FixtureType.MOUNTAIN_HUT -> if (f.place == app.trollfoss.domain.PlaceId.VAGSTADDALEN) drawValleyCabin(f, u, pen) else brHut(f, u, pen)
        FixtureType.CABLE_STATION -> brStation(f, u, pen)
        FixtureType.CABLE_CAR -> brCabin(f, u, pen)
        FixtureType.ROCK_LEDGE -> brLedge(f, u, pen)
        FixtureType.SUMMIT_ROCK -> brSummit(f, u, pen)
        FixtureType.ECHO_ROCK -> brEcho(f, u, pen)
        FixtureType.EAGLE_NEST -> brNest(f, u, pen)
        FixtureType.SUMMIT_FLAG -> brFlag(f, u, pen)
        else -> return false
    }
    return true
}

/** Draws the cabin's glass front over its riders. Returns true for all eight types and false for any other. */
fun DrawScope.drawBergFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.CABLE_CAR -> brCabinFront(f, u, pen)
        FixtureType.MOUNTAIN_HUT, FixtureType.CABLE_STATION, FixtureType.ROCK_LEDGE, FixtureType.SUMMIT_ROCK,
        FixtureType.ECHO_ROCK, FixtureType.EAGLE_NEST, FixtureType.SUMMIT_FLAG -> Unit
        else -> return false
    }
    return true
}

// ---------------------------------------------------------------------------------------------- kit

private object BrC {
    val log = Color(0xFFB9824F)
    val logLight = Color(0xFFD29A62)
    val logDark = Color(0xFF8E5E38)
    val turf = Color(0xFF6FAE5A)
    val turfDark = Color(0xFF4F8F48)
    val fascia = Color(0xFF5A3F2A)
    val door = Color(0xFFB8342B)
    val stone = Color(0xFF9D9AAA)
    val stoneLight = Color(0xFFC3C0CF)
    val stoneDark = Color(0xFF6C6985)
    val rockWarm = Color(0xFFB59C88)
    val snow = Color(0xFFF6F9FF)
    val snowShade = Color(0xFFC5D2EE)
    val glass = Color(0xFF9CCDEA)
    val glow = Color(0xFFFFD27A)
    val red = Color(0xFFD2443A)
    val yellow = Color(0xFFFFC83D)
}

private fun DrawScope.brShadow(u: Float, w: Float, d: Float, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

/** Warm light from a window or doorway: a soft glow that grows at night. */
private fun DrawScope.brLight(c: Offset, r: Float, pen: Pen, day: Float = 0.25f) {
    fxGlow(c, r, BrC.glow, (day + 0.7f * pen.night).coerceAtMost(1f))
}

// ---------------------------------------------------------------------------------------------- the hut

private const val HUT_D = 0.3f

private fun DrawScope.brHut(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = HUT_D
    val open = f.open
    val log = BrC.log
    brShadow(u, 0.92f, d)

    // The chimney stands behind the roof's front edge.
    val sc = q(0.25f, -0.74f, 0.22f)
    fxBox(u, 0.2f, -0.76f, 0.3f, -0.56f, 0.09f, BrC.stone, pen, rad = 0.004f, z = 0.18f, top = BrC.stoneLight, side = BrC.stoneDark)
    fxBox(u, 0.195f, -0.775f, 0.305f, -0.755f, 0.1f, BrC.stoneDark, pen, rad = 0.003f, z = 0.175f)
    val smoke = if (open) 1.5f else 1f
    fxPuffs(sc.x, sc.y - 0.02f * u, t, 0.02f * u, 0.28f * u, Color(0xFFD8DCE8), 0.5f * smoke, 5, 0.3f, 0.05f * u)

    // The log walls: front, courses of logs with their ends showing at the corners, and the long right side.
    fxBox(u, -0.4f, -0.5f, 0.4f, 0f, d, log, pen, rad = 0.004f, z = 0.015f, top = BrC.logLight, side = BrC.logDark)
    val wallFront = fxFront(u, -0.4f, -0.5f, 0.4f, 0f, 0.015f)
    val course = 0.0417f
    for (k in 1 until 12) {
        val y = -0.5f + k * course
        val a = q(-0.4f, y, 0.015f)
        val b = q(0.4f, y, 0.015f)
        drawLine(BrC.logDark, a, b, strokeWidth = pen.lw * 0.8f, alpha = 0.65f)
        drawLine(BrC.logLight, Offset(a.x, a.y + pen.lw), Offset(b.x, b.y + pen.lw), strokeWidth = pen.lw * 0.5f, alpha = 0.55f)
        // The right side shows the same courses running back.
        drawLine(BrC.logDark.darken(0.15f), q(0.4f, y, 0.015f), q(0.4f, y, 0.015f + d), strokeWidth = pen.lw * 0.7f, alpha = 0.6f)
    }
    clipRect(wallFront.left, wallFront.top, wallFront.right, wallFront.bottom) {
        for (k in 0 until 6) {
            val y = -0.46f + k * course * 2f
            drawLine(BrC.logLight, q(-0.32f + 0.12f * hash01(k, 501), y, 0.015f), q(-0.32f + 0.12f * hash01(k, 501) + 0.05f, y + 0.002f, 0.015f), 0.004f * u, alpha = 0.5f)
        }
    }
    for (k in 0 until 12) {
        val y = -0.479f + k * course
        for (s in 0..1) {
            val x = if (s == 0) -0.403f else 0.403f
            val c = q(x, y, 0.015f)
            drawOval(BrC.logLight, Offset(c.x - 0.014f * u, c.y - 0.014f * u), Size(0.028f * u, 0.028f * u))
            drawOval(Ink.line, Offset(c.x - 0.014f * u, c.y - 0.014f * u), Size(0.028f * u, 0.028f * u), style = pen.thin)
            drawCircle(BrC.logDark, 0.005f * u, c, style = pen.thin)
        }
    }
    // A window in the side wall, glowing at night.
    val w0 = q(0.4f, -0.34f, 0.07f)
    val w1 = q(0.4f, -0.34f, 0.17f)
    val w2 = q(0.4f, -0.2f, 0.17f)
    val w3 = q(0.4f, -0.2f, 0.07f)
    val sideWin = fxPath(w0, w1, w2, w3)
    drawPath(sideWin, lerp(BrC.glass, BrC.glow, pen.night))
    drawPath(sideWin, Color.White, style = Stroke(pen.lw * 1.4f))
    drawLine(Color.White, fxMix(w0, w1, 0.5f), fxMix(w3, w2, 0.5f), strokeWidth = pen.lw)

    // The opening: two red doors, or a warm room behind them.
    val doorL = -0.3f
    val doorR = 0.3f
    val doorT = -0.36f
    val doorB = -0.02f
    inkedRound(Rect(doorL * u - 0.012f * u, doorT * u - 0.012f * u, doorR * u + 0.012f * u, 0f), 0.004f * u, FxC.paint, pen, shade = false)
    if (!open) {
        for (s in 0..1) {
            val l = if (s == 0) doorL else 0.003f
            val r = if (s == 0) -0.003f else doorR
            val leaf = Rect(l * u + 0.004f * u, doorT * u, r * u - 0.004f * u, doorB * u + 0.02f * u)
            inkedRound(leaf, 0.004f * u, BrC.door, pen, shade = false)
            val bx = (l + r) / 2f
            val lw = r - l
            // Vertical boards, a glazed upper panel with a flower-box light, and a Z brace on the lower boards.
            for (k in 1..3) fxLine(p(l + lw * k / 4f, doorT + 0.14f), p(l + lw * k / 4f, doorB + 0.01f), BrC.door.darken(0.22f), pen.lw * 0.5f)
            val glass = Rect((l + 0.03f) * u, (doorT + 0.025f) * u, (r - 0.03f) * u, (doorT + 0.115f) * u)
            brLight(glass.center, 0.08f * u, pen, 0f)
            inkedRound(glass, 0.004f * u, lerp(BrC.glass, BrC.glow, pen.night), pen, shade = false)
            drawLine(Color.White, Offset(glass.center.x, glass.top), Offset(glass.center.x, glass.bottom), strokeWidth = pen.lw)
            drawLine(Color.White, Offset(glass.left, glass.center.y), Offset(glass.right, glass.center.y), strokeWidth = pen.lw)
            drawRect(Color.White, glass.topLeft, glass.size, style = Stroke(pen.lw * 1.5f))
            drawLine(BrC.door.darken(0.3f), p(l + 0.012f, doorT + 0.135f), p(r - 0.012f, doorT + 0.135f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
            drawLine(BrC.door.darken(0.3f), p(l + 0.012f, doorB - 0.02f), p(r - 0.012f, doorB - 0.02f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
            drawLine(BrC.door.darken(0.3f), p(l + 0.012f, doorT + 0.135f), p(r - 0.012f, doorB - 0.02f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
            val h = fxHeart(bx * u, (doorT + 0.2f) * u, 0.015f * u)
            drawPath(h, Color(0xFF3A1E2A))
            drawPath(h, Ink.line, style = pen.thin)
            val hx = if (s == 0) r - 0.03f else l + 0.03f
            inkedCircle(p(hx, -0.17f), 0.008f * u, FxC.brass, pen, shade = false)
        }
        // Warm light leaks through the cracks of the doors at night.
        if (pen.night > 0.2f) fxLine(p(0f, doorT + 0.02f), p(0f, doorB), BrC.glow.copy(alpha = pen.night * 0.9f), 0.004f * u)
    } else {
        val inD = 0.25f
        fxHollow(u, doorL, doorT, doorR, doorB, inD, Color(0xFFE2B27A), pen, back = Color(0xFFB8844F))
        clipRect(doorL * u, doorT * u, doorR * u, doorB * u) {
            val roomC = q(0f, -0.2f, 0.12f)
            brLight(roomC, 0.34f * u, pen, 0.65f)
            // Back wall: a shelf of mugs, a little window, a hanging lantern, boots under a bench.
            val zb = inD - 0.01f
            fxBox(u, -0.26f, -0.215f, -0.04f, -0.2f, 0.04f, BrC.logDark, pen, z = zb - 0.04f)
            val mugs = arrayOf(FxC.red, FxC.fjord, FxC.mustard, FxC.paint)
            for (k in 0 until 4) {
                val m = q(-0.23f + k * 0.052f, -0.215f, zb - 0.03f)
                inkedRound(Rect(m.x - 0.009f * u, m.y - 0.02f * u, m.x + 0.009f * u, m.y), 0.003f * u, mugs[k], pen, shade = false)
            }
            val wc = q(0.1f, -0.25f, zb)
            inkedRound(Rect(wc.x - 0.05f * u, wc.y - 0.045f * u, wc.x + 0.05f * u, wc.y + 0.045f * u), 0.004f * u, lerp(Color(0xFFA9D6F2), BrC.glow, pen.night), pen, shade = false)
            drawLine(Color.White, Offset(wc.x, wc.y - 0.045f * u), Offset(wc.x, wc.y + 0.045f * u), strokeWidth = pen.lw)
            drawLine(Color.White, Offset(wc.x - 0.05f * u, wc.y), Offset(wc.x + 0.05f * u, wc.y), strokeWidth = pen.lw)
            // A woven bench with a wool blanket along the left wall, a wood stove at the back right.
            fxBox(u, -0.28f, -0.09f, -0.12f, -0.07f, 0.12f, FxC.oak, pen, z = 0.08f)
            val blanket = fxFront(u, -0.27f, -0.11f, -0.14f, -0.09f, 0.1f)
            inkedRound(blanket, 0.004f * u, FxC.mustard, pen, shade = false)
            fxKnit(blanket, FxC.mustard.darken(0.25f), 5, 1, pen.lw * 0.4f)
            fxBox(u, 0.17f, -0.13f, 0.27f, -0.02f, 0.07f, Color(0xFF3A3844), pen, rad = 0.004f, z = 0.14f)
            val fire = q(0.22f, -0.08f, 0.14f)
            fxGlow(fire, 0.05f * u, BrC.glow, 0.8f)
            drawRoundRect(Color(0xFFFF9A3D), Offset(fire.x - 0.018f * u, fire.y - 0.022f * u), Size(0.036f * u, 0.03f * u), CornerRadius(0.004f * u))
            fxFire(fire.x, fire.y + 0.006f * u, 0.022f * u, 0.03f * u, t, 1.3f, pen, false)
            capsule(q(0.22f, -0.13f, 0.17f), q(0.22f, -0.36f, 0.17f), 0.007f * u, Color(0xFF3A3844), pen)
            val lan = q(-0.02f, -0.34f, 0.1f)
            fxLine(Offset(lan.x, lan.y - 0.03f * u), lan, Ink.line, pen.lw * 0.6f)
            inkedRound(Rect(lan.x - 0.008f * u, lan.y, lan.x + 0.008f * u, lan.y + 0.022f * u), 0.003f * u, Color(0xFFFFE9A8), pen, shade = false)
        }
        fxOpenDoor(u, doorL, doorT, doorB, 0.3f, -1f, BrC.door, BrC.door.darken(0.12f), pen, 120f)
        fxOpenDoor(u, doorR, doorT, doorB, 0.3f, 1f, BrC.door, BrC.door.darken(0.12f), pen, 120f)
        brLight(q(0f, -0.1f, -0.12f), 0.3f * u, pen, 0.0f)
    }
    // Shuttered windows either side of the doors, each with a flower box.
    for (s in 0..1) {
        val cx = if (s == 0) -0.352f else 0.352f
        val win = Rect((cx - 0.026f) * u, -0.335f * u, (cx + 0.026f) * u, -0.215f * u)
        brLight(win.center, 0.09f * u, pen, 0f)
        inkedRound(win, 0.004f * u, lerp(BrC.glass, BrC.glow, pen.night), pen, shade = false)
        drawLine(Color.White, Offset(win.center.x, win.top), Offset(win.center.x, win.bottom), strokeWidth = pen.lw)
        drawLine(Color.White, Offset(win.left, win.center.y), Offset(win.right, win.center.y), strokeWidth = pen.lw)
        drawRect(Color.White, win.topLeft, win.size, style = Stroke(pen.lw * 1.5f))
        for (sh in 0..1) {
            val sx = if (sh == 0) win.left - 0.012f * u else win.right
            inkedRound(Rect(sx, win.top, sx + 0.012f * u, win.bottom), 0.002f * u, BrC.door, pen, shade = false)
        }
        val box = Rect((cx - 0.034f) * u, -0.213f * u, (cx + 0.034f) * u, -0.185f * u)
        inkedRound(box, 0.003f * u, BrC.logDark, pen, shade = false)
        for (k in 0 until 4) {
            val fx0 = box.left + (0.01f + k * 0.0165f) * u
            drawLine(FxC.spruceLight, Offset(fx0, box.top), Offset(fx0, box.top - 0.016f * u), strokeWidth = pen.lw, cap = StrokeCap.Round)
            val col = arrayOf(FxC.pink, BrC.yellow, FxC.red, Color.White)[(k + s) % 4]
            inkedCircle(Offset(fx0, box.top - 0.02f * u), 0.0075f * u, col, pen, shade = false)
        }
    }
    // A lantern at the door, crossed skis over it and a rucksack with an ice axe, because it is a hut for hikers.
    for (s in 0..1) {
        val m = if (s == 0) -1f else 1f
        val a = p(-0.13f * m, -0.49f)
        val b = p(0.13f * m, -0.385f)
        val ski = Path().apply {
            moveTo(a.x - 0.007f * u, a.y)
            lineTo(b.x, b.y - 0.006f * u)
            quadraticTo(b.x + 0.014f * u * m, b.y - 0.002f * u, b.x + 0.016f * u * m, b.y - 0.012f * u)
            lineTo(b.x, b.y + 0.006f * u)
            lineTo(a.x + 0.007f * u, a.y)
            close()
        }
        inked(ski, if (s == 0) BrC.yellow else FxC.fjord, pen, shade = false)
    }
    inkedCircle(p(0f, -0.4375f), 0.007f * u, FxC.paint, pen, shade = false)
    val lamp = p(0.3f, -0.425f)
    brLight(lamp, 0.11f * u, pen, 0.05f)
    fxLine(p(0.3f, -0.46f), lamp, Ink.line, pen.lw * 0.6f)
    inkedRound(Rect(lamp.x - 0.009f * u, lamp.y, lamp.x + 0.009f * u, lamp.y + 0.026f * u), 0.003f * u, lerp(Color(0xFFFFF0B8), BrC.glow, pen.night), pen, shade = false)
    val pack = Rect(-0.455f * u, -0.115f * u, -0.405f * u, -0.005f * u)
    inkedRound(pack, 0.014f * u, FxC.falun, pen)
    inkedRound(Rect(pack.left + 0.008f * u, pack.top + 0.04f * u, pack.right - 0.008f * u, pack.bottom - 0.01f * u), 0.006f * u, FxC.falun.darken(0.15f), pen, shade = false)
    drawLine(BrC.yellow, Offset(pack.left, pack.top + 0.03f * u), Offset(pack.right, pack.top + 0.03f * u), strokeWidth = pen.lw * 1.4f)
    capsule(p(-0.47f, -0.13f), p(-0.43f, 0f), 0.006f * u, FxC.steel, pen)
    inkedRound(Rect(-0.482f * u, -0.14f * u, -0.452f * u, -0.126f * u), 0.003f * u, FxC.steel, pen, shade = false)

    // The roof: a turf roof with a dark fascia, snow patches and tufts. Its front edge is where things stand.
    fxBox(u, -0.46f, -0.56f, 0.46f, -0.5f, d + 0.06f, BrC.fascia, pen, rad = 0.004f, z = -0.02f, top = BrC.turf, side = BrC.turfDark.darken(0.2f))
    val roofTop = fxFlat(u, -0.46f, 0.46f, -0.56f, -0.02f, d + 0.04f)
    clipPath(roofTop) {
        for (k in 0 until 22) {
            val gx = -0.44f + 0.88f * hash01(k, 511)
            val gz = 0.03f + (d - 0.02f) * hash01(k, 512)
            val c = q(gx, -0.56f, gz)
            drawPath(tuftPath(c.x, c.y, (0.012f + 0.01f * hash01(k, 513)) * u, 0f), if (k % 3 == 0) BrC.turf.lighten(0.15f) else BrC.turfDark, alpha = 0.9f)
        }
        for (k in 0 until 4) {
            val c = q(-0.36f + k * 0.24f + 0.04f * hash01(k, 514), -0.56f, 0.12f + 0.12f * hash01(k, 515))
            brDrift(c, 0.04f * u, 0.009f * u, BrC.snow, pen)
        }
        for (k in 0 until 7) {
            val c = q(-0.4f + 0.8f * hash01(k, 516), -0.56f, 0.06f + 0.2f * hash01(k, 517))
            drawCircle(arrayOf(BrC.yellow, FxC.pink, Color.White)[k % 3], 0.0035f * u, Offset(c.x, c.y - 0.012f * u))
        }
    }
    drawPath(roofTop, Ink.line, style = pen.stroke)
    // Icicles along the front edge.
    for (k in 0 until 8) {
        val x = -0.42f + k * 0.12f + 0.02f * hash01(k, 518)
        val len = 0.014f + 0.016f * hash01(k, 519)
        val a = q(x, -0.5f, -0.02f)
        drawPath(fxPoly(1f, a.x - 0.004f * u, a.y, a.x + 0.004f * u, a.y, a.x, a.y + len * u), Color(0xFFE6F4FF).copy(alpha = 0.9f))
    }
}

// ---------------------------------------------------------------------------------------------- cable stations

private fun brIsTopStation(f: Fixture): Boolean = f.host >= 0 || f.y < 0.7f

private fun DrawScope.brStation(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val top = brIsTopStation(f)
    val d = 0.22f
    val wood = FxC.oakDark
    val steel = Color(0xFF8A93AA)
    brShadow(u, 0.5f, d)
    // The platform and the back wall.
    fxBox(u, -0.26f, -0.03f, 0.26f, 0f, d + 0.04f, BrC.stone, pen, rad = 0.003f, z = -0.02f, top = BrC.stoneLight, side = BrC.stoneDark)
    fxBox(u, -0.24f, -0.34f, 0.24f, -0.03f, 0.04f, wood.darken(0.25f), pen, z = d - 0.02f)
    val back = fxFront(u, -0.24f, -0.34f, 0.24f, -0.03f, d - 0.02f)
    var x = back.left + 0.03f * u
    while (x < back.right) {
        drawLine(Ink.line.copy(alpha = 0.3f), Offset(x, back.top), Offset(x, back.bottom), pen.lw * 0.6f)
        x += 0.036f * u
    }
    // Ticket booth on the left: a little timber house with a window and a counter.
    val bl = if (top) 0.02f else -0.25f
    val br = if (top) 0.25f else -0.05f
    fxBox(u, bl, -0.3f, br, -0.03f, 0.16f, wood, pen, rad = 0.004f, z = 0.02f, top = wood.lighten(0.15f), side = wood.darken(0.25f))
    val win = Rect((bl + 0.03f) * u, -0.26f * u, (br - 0.03f) * u, -0.14f * u)
    fxBox(u, bl + 0.025f, -0.17f, br - 0.025f, -0.15f, 0.03f, FxC.oak, pen, z = -0.01f)
    inkedRound(win, 0.004f * u, lerp(Color(0xFFBFE6F8), BrC.glow, pen.night), pen, shade = false)
    drawLine(Color.White, Offset(win.center.x, win.top), Offset(win.center.x, win.bottom), pen.lw)
    brLight(win.center, 0.11f * u, pen, 0f)
    // A little red flag and a bell under the roof.
    val bellX = if (top) -0.17f else 0.1f
    val bell = q(bellX, -0.35f, 0.04f)
    val swing = sin(t * 1.6f + f.id) * 4f
    rotate(swing, Offset(bell.x, bell.y - 0.02f * u)) {
        fxLine(Offset(bell.x, bell.y - 0.02f * u), Offset(bell.x, bell.y), Ink.line, pen.lw * 0.7f)
        val b = Path().apply {
            moveTo(bell.x - 0.011f * u, bell.y + 0.026f * u)
            quadraticTo(bell.x - 0.01f * u, bell.y, bell.x, bell.y - 0.003f * u)
            quadraticTo(bell.x + 0.01f * u, bell.y, bell.x + 0.011f * u, bell.y + 0.026f * u)
            close()
        }
        inked(b, FxC.brass, pen)
        drawCircle(Ink.line, 0.003f * u, Offset(bell.x, bell.y + 0.03f * u))
    }
    // The wheel the cable runs over. Down at the bottom station it sits above the cabin; at the top it hangs on an arm.
    val wc = if (top) q(0.3f, -0.33f, 0.0f) else q(0f, -0.26f, 0.0f)
    val bracketFrom = if (top) q(0.1f, -0.4f, 0.04f) else q(0f, -0.4f, 0.04f)
    capsule(bracketFrom, wc, 0.007f * u, steel, pen)
    if (top) capsule(q(0.1f, -0.4f, 0.04f), q(0.3f, -0.4f, 0.04f), 0.008f * u, steel, pen)
    if (top) capsule(q(0.3f, -0.4f, 0.04f), wc, 0.006f * u, steel, pen)
    val r = 0.04f * u
    drawCircle(Ink.line, r + pen.lw * 1.4f, wc)
    drawCircle(steel, r, wc)
    drawCircle(Color(0xFF5A6078), r * 0.72f, wc)
    for (k in 0 until 6) {
        val a = k * FX_PI / 3f
        drawLine(steel, wc, Offset(wc.x + cos(a) * r * 0.72f, wc.y + sin(a) * r * 0.72f), strokeWidth = pen.lw * 1.2f)
    }
    inkedCircle(wc, r * 0.22f, FxC.brass, pen, shade = false)
    // The roof, with snow on top: this is where things can stand.
    fxBox(u, -0.28f, -0.44f, 0.28f, -0.37f, d + 0.06f, wood.darken(0.1f), pen, rad = 0.004f, z = -0.02f, top = BrC.snow, side = BrC.snowShade)
    val roof = fxFlat(u, -0.28f, 0.28f, -0.44f, -0.02f, d + 0.04f)
    clipPath(roof) {
        for (k in 0 until 6) {
            val c = q(-0.22f + k * 0.09f, -0.44f, 0.05f + 0.1f * hash01(k, 531))
            brDrift(c, 0.032f * u, 0.008f * u, BrC.snow, pen)
        }
    }
    for (k in 0 until 7) {
        val xx = -0.25f + k * 0.083f
        val len = 0.012f + 0.014f * hash01(k, 532 + f.id)
        val a = q(xx, -0.37f, -0.02f)
        drawPath(fxPoly(1f, a.x - 0.004f * u, a.y, a.x + 0.004f * u, a.y, a.x, a.y + len * u), Color(0xFFE6F4FF).copy(alpha = 0.9f))
    }
    // Pennant flag on the roof corner.
    val pole = q(if (top) -0.25f else 0.25f, -0.44f, 0.12f)
    fxLine(pole, Offset(pole.x, pole.y - 0.07f * u), Ink.line, pen.lw)
    val wave = sin(t * 5f + f.id) * 0.006f * u
    val pennant = Path().apply {
        moveTo(pole.x, pole.y - 0.07f * u)
        quadraticTo(pole.x + 0.02f * u, pole.y - 0.066f * u + wave, pole.x + 0.04f * u, pole.y - 0.058f * u)
        quadraticTo(pole.x + 0.02f * u, pole.y - 0.05f * u - wave, pole.x, pole.y - 0.048f * u)
        close()
    }
    inked(pennant, BrC.red, pen, shade = false)
}

// ---------------------------------------------------------------------------------------------- the cabin

private fun DrawScope.brCabinCable(f: Fixture, u: Float, pen: Pen) {
    val h = f.spec.h
    val a = Offset(-f.shiftX * u, (-f.shiftY - h) * u)
    val b = Offset((-f.shiftX + 3.15f) * u, (-f.shiftY - 0.29f - h) * u)
    val steel = Color(0xFF5A6078)
    drawLine(steel, a, b, strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(a.x, a.y - pen.lw), Offset(b.x, b.y - pen.lw), strokeWidth = pen.lw * 0.5f, alpha = 0.3f)
}

private fun DrawScope.brCabin(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.14f
    val h = f.spec.h
    brShadow(u, 0.34f, d, 0.35f)
    brCabinCable(f, u, pen)
    val pivot = p(0f, -h)
    rotate((f.angle * 0.5f * 57.29578f), pivot) {
        val red = Color(0xFFD2443A)
        // The carriage on the cable and the hanger down to the roof.
        val grip = Path().apply {
            moveTo(-0.012f * u, -h * u)
            lineTo(-0.006f * u, (-h + 0.03f) * u)
            lineTo(0.006f * u, (-h + 0.03f) * u)
            lineTo(0.012f * u, -h * u)
            close()
        }
        inked(grip, Color(0xFF8A93AA), pen)
        for (s in 0..1) inkedCircle(p(if (s == 0) -0.016f else 0.016f, -h - 0.002f), 0.009f * u, Color(0xFF5A6078), pen, shade = false)
        capsule(p(0f, -h + 0.03f), p(0f, -0.275f), 0.007f * u, Color(0xFF8A93AA), pen)
        // Inside: warm wooden walls and floor seen through the open front.
        fxHollow(u, -0.165f, -0.255f, 0.165f, 0f, d, Color(0xFFE9C58F), pen, back = Color(0xFFC79A60))
        clipRect(-0.165f * u, -0.255f * u, 0.165f * u, 0f) {
            val zb = d - 0.01f
            fxBox(u, -0.15f, -0.125f, 0.15f, -0.105f, 0.06f, FxC.oak, pen, z = zb - 0.06f)
            val c = q(0f, -0.16f, zb)
            brLight(c, 0.2f * u, pen, 0.18f)
            // A little window in the back wall and a heart painted on it.
            val win = Rect(c.x - 0.1f * u, c.y - 0.075f * u, c.x + 0.1f * u, c.y + 0.07f * u)
            val sky = Color(0xFFBFE2F6)
            drawRect(sky, win.topLeft, win.size)
            drawPath(fxPoly(1f, win.left + 0.01f * u, win.bottom, win.left + 0.07f * u, win.top + 0.05f * u, win.left + 0.12f * u, win.bottom), Color(0xFF8FA2D8))
            drawPath(fxPoly(1f, win.left + 0.1f * u, win.bottom, win.left + 0.15f * u, win.top + 0.07f * u, win.right, win.bottom), Color(0xFFB4C4EA))
            drawRect(Ink.line, win.topLeft, win.size, style = pen.thin)
            drawLine(Color.White, Offset(win.center.x, win.top), Offset(win.center.x, win.bottom), strokeWidth = pen.lw)
        }
        // The right side wall seen from outside, with its windows.
        val rs = fxDeep(u, 0.165f, -0.255f, 0f, 0f, d, 0.004f)
        fxFace(rs, red.darken(0.22f), pen)
        for (k in 0 until 2) {
            val z0 = 0.025f + k * 0.075f
            val win = fxPath(q(0.165f, -0.205f, z0), q(0.165f, -0.205f, z0 + 0.06f), q(0.165f, -0.105f, z0 + 0.06f), q(0.165f, -0.105f, z0))
            drawPath(win, lerp(Color(0xFFB4DCF2), BrC.glow, pen.night))
            drawPath(win, Color.White, style = Stroke(pen.lw * 1.3f))
        }
        val stripe = fxPath(q(0.165f, -0.06f, 0.0f), q(0.165f, -0.06f, d), q(0.165f, -0.03f, d), q(0.165f, -0.03f, 0.0f))
        drawPath(stripe, BrC.yellow.darken(0.15f))
        // The roof: a rounded cap, yellow, with its top face showing.
        val roofTop = fxFlat(u, -0.18f, 0.18f, -0.285f, -0.01f, d + 0.01f, 0.012f)
        fxFace(roofTop, BrC.yellow.lighten(0.1f), pen)
        drawLine(BrC.yellow.darken(0.2f), q(-0.1f, -0.285f, 0.02f), q(-0.1f, -0.285f, d - 0.02f), 0.004f * u)
        drawLine(BrC.yellow.darken(0.2f), q(0.1f, -0.285f, 0.02f), q(0.1f, -0.285f, d - 0.02f), 0.004f * u)
    }
}

/** The front of the cabin: the roof edge, the door panel and the glass over the riders. */
private fun DrawScope.brCabinFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val h = f.spec.h
    val pivot = p(0f, -h)
    val red = Color(0xFFD2443A)
    rotate((f.angle * 0.5f * 57.29578f), pivot) {
        // The glass: a faint blue pane with frames, mullions and shine, in front of whoever sits inside.
        val glass = Rect(-0.165f * u, -0.255f * u, 0.165f * u, -0.11f * u)
        drawRect(Color(0x24BFE8FF), glass.topLeft, glass.size)
        drawLine(Color.White, Offset(glass.center.x, glass.top), Offset(glass.center.x, glass.bottom), strokeWidth = pen.lw * 1.4f)
        val shine = Path().apply {
            moveTo(glass.left + 0.02f * u, glass.bottom)
            lineTo(glass.left + 0.05f * u, glass.bottom)
            lineTo(glass.left + 0.09f * u, glass.top)
            lineTo(glass.left + 0.06f * u, glass.top)
            close()
        }
        drawPath(shine, Color.White, alpha = 0.3f)
        drawLine(Color.White.copy(alpha = 0.5f), Offset(glass.right - 0.09f * u, glass.bottom - 0.01f * u), Offset(glass.right - 0.065f * u, glass.top + 0.03f * u), strokeWidth = 0.004f * u, cap = StrokeCap.Round)
        drawRect(Ink.line, glass.topLeft, glass.size, style = pen.thin)
        // The door panel below, red with a yellow band and a handle.
        val panel = Rect(-0.172f * u, -0.115f * u, 0.172f * u, 0f)
        inkedRound(panel, 0.01f * u, red, pen)
        drawRect(BrC.yellow, Offset(panel.left, panel.top + 0.045f * u), Size(panel.width, 0.016f * u))
        drawLine(Ink.line, Offset(panel.center.x, panel.top), Offset(panel.center.x, panel.bottom), strokeWidth = pen.lw * 0.8f)
        for (s in 0..1) {
            val hx = panel.center.x + (if (s == 0) -0.02f else 0.02f) * u
            drawLine(FxC.steel, Offset(hx, panel.top + 0.02f * u), Offset(hx, panel.top + 0.042f * u), strokeWidth = 0.005f * u, cap = StrokeCap.Round)
        }
        // The roof's front edge over the heads.
        val fascia = Rect(-0.18f * u, -0.285f * u, 0.18f * u, -0.25f * u)
        inkedRound(fascia, 0.012f * u, BrC.yellow, pen)
        drawRect(red, Offset(fascia.left + 0.012f * u, fascia.top + 0.024f * u), Size(fascia.width - 0.024f * u, 0.006f * u))
        // A little lamp on the roof edge, lit at night.
        val lamp = Offset(fascia.center.x, fascia.top + 0.012f * u)
        brLight(lamp, 0.07f * u, pen, 0f)
        inkedCircle(lamp, 0.006f * u, lerp(Color(0xFFFFF0B8), BrC.glow, pen.night), pen, shade = false)
    }
}

// ---------------------------------------------------------------------------------------------- the rocks

/**
 * A rock in oblique 3D: the outline [front] (pixels) swept [d] units back so rounded shapes stay round,
 * with an optional flat top between [l] and [r] at height [topY] (scene units) where things can stand.
 */
private fun DrawScope.brRock(
    u: Float, front: Path, d: Float, pen: Pen, frontColor: Color, sideColor: Color, topColor: Color?,
    l: Float = 0f, r: Float = 0f, topY: Float = 0f,
) {
    val n = 10
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    for (k in n downTo 0) {
        translate(dx * k / n, dy * k / n) { drawPath(front, Ink.line, style = Stroke(pen.lw * 2.1f, join = StrokeJoin.Round)) }
    }
    for (k in n downTo 1) {
        translate(dx * k / n, dy * k / n) { drawPath(front, sideColor) }
    }
    if (topColor != null) {
        val top = fxFlat(u, l, r, topY, 0f, d)
        drawPath(top, topColor)
        drawPath(top, Ink.line, style = pen.stroke)
    }
    drawPath(front, frontColor)
}

/** Layers of rock across the front and the side: sloping, tapering bands, joints and chips, and the same layers on the side. */
private fun DrawScope.brBands(
    u: Float, front: Path, l: Float, r: Float, t: Float, b: Float, d: Float, light: Color, dark: Color, pen: Pen, seed: Int,
) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    clipPath(front) {
        var y = t + 0.015f
        var k = 0
        while (y < b) {
            val h = 0.012f + 0.02f * hash01(k, seed)
            val col = when (k % 3) {
                0 -> light
                1 -> dark
                else -> lerp(light, dark, 0.5f)
            }
            // Several broken stretches across each layer, each sloping a little and thinning out at its end.
            val pieces = 2 + (hash01(k, seed + 9) * 2f).toInt()
            for (s in 0 until pieces) {
                val span = r - l
                val x0 = l + span * (s / pieces.toFloat()) + span * 0.04f * hash01(k * 7 + s, seed + 10)
                val x1 = x0 + span / pieces * (0.55f + 0.4f * hash01(k * 7 + s, seed + 11))
                val slope = (hash01(k * 7 + s, seed + 12) - 0.5f) * 0.05f
                val band = Path().apply {
                    moveTo(x0 * u, y * u)
                    quadraticTo((x0 + x1) / 2f * u, (y + slope * (x1 - x0) * 0.5f - 0.003f) * u, x1 * u, (y + slope * (x1 - x0)) * u)
                    lineTo(x1 * u, (y + slope * (x1 - x0) + h * 0.2f) * u)
                    quadraticTo((x0 + x1) / 2f * u, (y + slope * (x1 - x0) * 0.5f + h * 0.8f) * u, x0 * u, (y + h) * u)
                    close()
                }
                drawPath(band, col, alpha = 0.78f)
            }
            y += h + 0.025f + 0.045f * hash01(k, seed + 4)
            k++
        }
        // Vertical joints, and angular chips of lighter and darker stone.
        for (c in 0 until 7) {
            var cx = l + (r - l) * (0.06f + 0.88f * hash01(c, seed + 5))
            var cy = t + (b - t) * 0.25f * hash01(c, seed + 6)
            val cr = Path()
            cr.moveTo(cx * u, cy * u)
            for (s in 0 until 4) {
                cx += (hash01(c * 9 + s, seed + 7) - 0.5f) * 0.05f
                cy += 0.03f + 0.04f * hash01(c * 9 + s, seed + 8)
                cr.lineTo(cx * u, cy * u)
            }
            drawPath(cr, Ink.line, alpha = 0.4f, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        for (c in 0 until 9) {
            val cx = l + (r - l) * (0.05f + 0.9f * hash01(c, seed + 13))
            val cy = t + (b - t) * (0.1f + 0.85f * hash01(c, seed + 14))
            val s = 0.012f + 0.018f * hash01(c, seed + 15)
            val chip = fxPoly(u, cx - s, cy + s * 0.6f, cx - s * 0.2f, cy - s * 0.8f, cx + s, cy + s * 0.3f)
            drawPath(chip, if (c % 2 == 0) light.lighten(0.2f) else dark, alpha = 0.55f)
        }
    }
    // The same layers run back along the right-hand side.
    var y = t + 0.03f
    var k = 0
    while (y < b - 0.02f) {
        drawLine(dark.darken(0.2f), Offset((r + 0.012f) * u, y * u), Offset((r + 0.012f) * u + dx * 0.9f, y * u + dy * 0.9f), strokeWidth = pen.lw * 0.8f, alpha = 0.45f)
        y += 0.04f + 0.05f * hash01(k, seed + 20)
        k++
    }
}

/** A low drift of snow on a flat top, lit and shaded, at [c] pixels. */
private fun DrawScope.brDrift(c: Offset, w: Float, h: Float, snow: Color, pen: Pen) {
    val p = Path().apply {
        moveTo(c.x - w, c.y)
        cubicTo(c.x - w * 0.95f, c.y - h * 1.2f, c.x - w * 0.35f, c.y - h * 1.7f, c.x + w * 0.1f, c.y - h * 1.1f)
        cubicTo(c.x + w * 0.4f, c.y - h * 1.5f, c.x + w * 0.95f, c.y - h * 1.0f, c.x + w, c.y)
        quadraticTo(c.x, c.y + h * 0.4f, c.x - w, c.y)
        close()
    }
    inked(p, snow, pen)
}

private fun DrawScope.brLedge(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.2f
    brShadow(u, 1.3f, d)
    val front = blobPath(
        -0.4f * u, -0.3f * u, -0.62f * u, -0.3f * u, -0.66f * u, -0.27f * u, -0.67f * u, -0.2f * u, -0.64f * u, -0.17f * u, -0.67f * u, -0.1f * u,
        -0.71f * u, -0.05f * u, -0.69f * u, 0f, -0.4f * u, 0.004f * u, 0.4f * u, 0.004f * u, 0.7f * u, 0f, 0.7f * u, -0.05f * u, 0.66f * u, -0.12f * u,
        0.68f * u, -0.2f * u, 0.65f * u, -0.27f * u, 0.62f * u, -0.3f * u, 0.4f * u, -0.3f * u,
    )
    brRock(u, front, d, pen, Color(0xFF9F9CB0), Color(0xFF6E6B8A), Color(0xFFC9C6D8), -0.62f, 0.62f, -0.3f)
    brBands(u, front, -0.7f, 0.7f, -0.3f, 0f, d, Color(0xFFD6B79A), Color(0xFF6F6C8C), pen, 541)
    val lip = Path().apply {
        moveTo(-0.63f * u, -0.3f * u)
        lineTo(0.63f * u, -0.3f * u)
        var lx = 0.63f
        while (lx > -0.63f) {
            lineTo(lx * u, (-0.283f + 0.014f * hash01((lx * 100f).toInt(), 559)) * u)
            lx -= 0.04f
        }
        close()
    }
    drawPath(lip, BrC.turfDark)
    drawPath(lip, Ink.line, alpha = 0.6f, style = pen.thin)
    // Grass and a few flowers on top, kept back so the front edge stays clear for standing things.
    val top = fxFlat(u, -0.62f, 0.62f, -0.3f, 0f, d)
    clipPath(top) {
        for (k in 0 until 16) {
            val gx = -0.58f + 1.16f * hash01(k, 551)
            val gz = 0.08f + (d - 0.1f) * hash01(k, 552)
            val c = q(gx, -0.3f, gz)
            drawPath(tuftPath(c.x, c.y, (0.016f + 0.014f * hash01(k, 553)) * u, 0f), if (k % 3 == 0) BrC.turf.lighten(0.15f) else BrC.turfDark)
            if (k % 3 == 1) drawCircle(arrayOf(BrC.yellow, FxC.pink, Color.White, FxC.fjord)[k % 4], 0.0045f * u, Offset(c.x + 0.008f * u, c.y - 0.012f * u))
        }
        for (k in 0 until 5) {
            val c = q(-0.5f + k * 0.26f + 0.05f * hash01(k, 554), -0.3f, 0.06f + 0.14f * hash01(k, 555))
            drawOval(BrC.stoneLight, Offset(c.x - 0.012f * u, c.y - 0.005f * u), Size(0.024f * u, 0.01f * u))
        }
    }
    drawPath(top, Ink.line, style = pen.stroke)
    // Snow lying in the ledges of the face, and a few loose stones at the foot.
    for (k in 0 until 5) {
        val x = -0.56f + k * 0.28f + 0.03f * hash01(k, 556)
        val y = -0.25f + 0.1f * hash01(k, 557)
        brDrift(Offset(x * u, y * u), 0.035f * u, 0.008f * u, BrC.snow, pen)
    }
    for (k in 0 until 3) {
        val x = -0.5f + 0.5f * k + 0.1f * hash01(k, 558)
        inkedOval(Rect((x - 0.03f) * u, -0.022f * u, (x + 0.03f) * u, 0.008f * u), BrC.stoneLight, pen)
    }
}

private fun DrawScope.brSummit(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.26f
    brShadow(u, 1.2f, d)
    val front = blobPath(
        -0.3f * u, -0.5f * u, -0.5f * u, -0.5f * u, -0.55f * u, -0.45f * u, -0.56f * u, -0.37f * u, -0.6f * u, -0.3f * u, -0.58f * u, -0.2f * u,
        -0.62f * u, -0.1f * u, -0.6f * u, 0f, -0.3f * u, 0.004f * u, 0.3f * u, 0.004f * u, 0.62f * u, 0f, 0.62f * u, -0.08f * u, 0.58f * u, -0.18f * u,
        0.6f * u, -0.3f * u, 0.55f * u, -0.38f * u, 0.54f * u, -0.45f * u, 0.5f * u, -0.5f * u, 0.3f * u, -0.5f * u,
    )
    brRock(u, front, d, pen, Color(0xFF9C99AE), Color(0xFF6B6888), BrC.snow, -0.5f, 0.5f, -0.5f)
    brBands(u, front, -0.62f, 0.62f, -0.5f, 0f, d, Color(0xFFD6B79A), Color(0xFF6F6C8C), pen, 561)
    // A thick snow cap over the shoulders, jagged along its lower edge, shaded on the right.
    val cap = Path().apply {
        moveTo(-0.52f * u, -0.5f * u)
        lineTo(0.52f * u, -0.5f * u)
        lineTo(0.56f * u, -0.4f * u)
        lineTo(0.5f * u, -0.35f * u)
        lineTo(0.44f * u, -0.41f * u)
        lineTo(0.36f * u, -0.32f * u)
        lineTo(0.28f * u, -0.42f * u)
        lineTo(0.18f * u, -0.34f * u)
        lineTo(0.08f * u, -0.44f * u)
        lineTo(-0.04f * u, -0.33f * u)
        lineTo(-0.15f * u, -0.43f * u)
        lineTo(-0.26f * u, -0.35f * u)
        lineTo(-0.36f * u, -0.44f * u)
        lineTo(-0.46f * u, -0.36f * u)
        lineTo(-0.57f * u, -0.42f * u)
        close()
    }
    drawPath(cap, BrC.snow)
    val capShade = Path().apply {
        moveTo(0.14f * u, -0.5f * u)
        lineTo(0.52f * u, -0.5f * u)
        lineTo(0.56f * u, -0.4f * u)
        lineTo(0.5f * u, -0.35f * u)
        lineTo(0.44f * u, -0.41f * u)
        lineTo(0.36f * u, -0.32f * u)
        lineTo(0.28f * u, -0.42f * u)
        lineTo(0.2f * u, -0.37f * u)
        close()
    }
    drawPath(capShade, BrC.snowShade)
    // Wind ripples in the snow, and icicles along the edge of the cap.
    for (k in 0 until 6) {
        val y = -0.485f + 0.014f * (k % 3)
        val x = -0.45f + k * 0.15f
        drawLine(BrC.snowShade, Offset(x * u, y * u), Offset((x + 0.07f) * u, (y - 0.003f) * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    }
    drawPath(cap, Ink.line, style = pen.thin)
    for (k in 0 until 6) {
        val x = -0.42f + k * 0.17f
        val a = Offset(x * u, -0.375f * u)
        drawPath(fxPoly(1f, a.x - 0.005f * u, a.y, a.x + 0.005f * u, a.y, a.x, a.y + (0.022f + 0.012f * hash01(k, 562)) * u), Color(0xFFE6F4FF).copy(alpha = 0.95f))
    }
    val topSnow = fxFlat(u, -0.5f, 0.5f, -0.5f, 0f, d)
    drawPath(topSnow, Color(0xFFF8FBFF))
    // Drifts and tracks on top, with a few sparkles.
    val topC = q(0f, -0.5f, d * 0.5f)
    for (k in 0 until 4) {
        val c = q(-0.38f + k * 0.26f, -0.5f, 0.06f + 0.12f * hash01(k, 564))
        drawLine(BrC.snowShade, c, Offset(c.x + 0.07f * u, c.y - 0.004f * u), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
    }
    drawPath(topSnow, Ink.line, style = pen.stroke)
    for (k in 0 until 5) {
        val c = q(-0.4f + k * 0.2f, -0.5f, 0.06f + 0.14f * hash01(k, 563))
        val ph = max(0f, sin(pen.t * 1.4f + k * 2.2f))
        if (ph > 0.5f) twinkle(c, 0.01f * u, Color.White, ph)
    }
    if (topC.x < -9999f) return
}

private fun DrawScope.brEcho(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.22f
    val a = f.anim.coerceIn(0f, 1f)
    brShadow(u, 0.52f, d)
    val front = blobPath(
        -0.1f * u, -0.42f * u, -0.21f * u, -0.42f * u, -0.25f * u, -0.38f * u, -0.27f * u, -0.3f * u, -0.285f * u, -0.18f * u, -0.27f * u, -0.08f * u,
        -0.22f * u, -0.01f * u, -0.1f * u, 0.004f * u, 0.1f * u, 0.004f * u, 0.22f * u, -0.01f * u, 0.27f * u, -0.08f * u, 0.285f * u, -0.18f * u,
        0.27f * u, -0.3f * u, 0.25f * u, -0.38f * u, 0.21f * u, -0.42f * u, 0.1f * u, -0.42f * u,
    )
    brRock(u, front, d, pen, Color(0xFFA9A7B8), Color(0xFF6F6C88), Color(0xFFCFCDDC), -0.2f, 0.2f, -0.42f)
    clipPath(front) {
        // A lighter belly and a shadow under the brow of the rock give it the round look of a face.
        drawOval(Color(0xFFBEBCCC), Offset(-0.2f * u, -0.3f * u), Size(0.4f * u, 0.26f * u), alpha = 0.55f)
        // Lichen patches and moss on the face.
        for (k in 0 until 8) {
            val c = p(-0.22f + 0.44f * hash01(k, 571), -0.4f + 0.38f * hash01(k, 572))
            drawCircle(if (k % 2 == 0) Color(0xFF9DBA6A) else Color(0xFFE0A24E), (0.008f + 0.01f * hash01(k, 573)) * u, c, alpha = 0.5f)
        }
        drawLine(Ink.line, p(0.02f, -0.1f), p(0.1f, -0.02f), strokeWidth = pen.lw * 0.8f, alpha = 0.4f)
        drawLine(Ink.line, p(-0.03f, -0.12f), p(-0.13f, -0.02f), strokeWidth = pen.lw * 0.8f, alpha = 0.4f)
        // The cleft: a dark open mouth, wider when it shouts.
        val mw = 0.062f + 0.03f * a
        val mh = 0.1f + 0.055f * a
        val my = -0.1f
        val mouth = Path().apply {
            moveTo(0f, (my - mh) * u)
            cubicTo((mw * 0.9f) * u, (my - mh * 0.7f) * u, mw * u, (my - mh * 0.1f) * u, (mw * 0.55f) * u, my * u)
            cubicTo((mw * 0.3f) * u, (my + 0.012f) * u, (-mw * 0.3f) * u, (my + 0.012f) * u, (-mw * 0.55f) * u, my * u)
            cubicTo(-mw * u, (my - mh * 0.1f) * u, (-mw * 0.9f) * u, (my - mh * 0.7f) * u, 0f, (my - mh) * u)
            close()
        }
        drawPath(mouth, Color(0xFF2A1E34))
        drawOval(Color(0xFFD9667A), Offset(-mw * 0.4f * u, (my - 0.025f) * u), Size(mw * 0.8f * u, 0.032f * u))
        drawPath(mouth, Ink.line, style = pen.stroke)
        drawPath(mouth, Color(0xFFD9D6E6), style = Stroke(pen.lw * 2.4f, join = StrokeJoin.Round), alpha = 0.55f)
        // Eyes: two round pale stones looking up at whoever shouts, with heavy mossy brows.
        for (s in 0..1) {
            val m = if (s == 0) -1f else 1f
            val ec = p(m * 0.085f, -0.27f - 0.01f * a)
            val er = (0.034f + 0.006f * a) * u
            drawCircle(Color(0xFFFDF8EC), er, ec)
            drawCircle(Ink.line, er, ec, style = pen.stroke)
            val look = Offset(-m * 0.2f * er + sin(t * 0.8f) * er * 0.1f, -0.25f * er)
            drawCircle(Ink.line, er * 0.52f, Offset(ec.x + look.x, ec.y + look.y))
            drawCircle(Color.White, er * 0.18f, Offset(ec.x + look.x - er * 0.14f, ec.y + look.y - er * 0.16f))
            val brow = Path().apply {
                moveTo(ec.x - m * er * 1.3f, ec.y - er * (1.0f + 0.2f * a))
                quadraticTo(ec.x, ec.y - er * (1.7f + 0.3f * a), ec.x + m * er * 1.1f, ec.y - er * (1.1f + 0.5f * a))
                lineTo(ec.x + m * er * 1.1f, ec.y - er * (0.8f + 0.5f * a))
                quadraticTo(ec.x, ec.y - er * (1.35f + 0.3f * a), ec.x - m * er * 1.3f, ec.y - er * 0.75f)
                close()
            }
            drawPath(brow, Color(0xFF6FAE5A))
            drawPath(brow, Ink.line, style = pen.thin)
            drawOval(Color(0xFFFF9EB0), Offset(ec.x - er * 0.9f + m * er * 0.2f, ec.y + er * 1.35f), Size(er * 1.6f, er * 0.7f), alpha = 0.55f)
        }
        drawCircle(Color(0xFFB9B6C8), 0.016f * u, p(0f, -0.2f))
        drawCircle(Ink.line, 0.016f * u, p(0f, -0.2f), style = pen.thin)
        drawOval(Ink.line, Offset(-0.008f * u, -0.205f * u), Size(0.005f * u, 0.007f * u))
        drawOval(Ink.line, Offset(0.003f * u, -0.205f * u), Size(0.005f * u, 0.007f * u))
    }
    drawPath(front, Ink.line, style = pen.stroke)
    // A tuft of grass on top, and one stubborn flower.
    val top = fxFlat(u, -0.2f, 0.2f, -0.42f, 0f, d)
    clipPath(top) {
        for (k in 0 until 4) {
            val c = q(-0.14f + k * 0.09f, -0.42f, 0.07f + 0.08f * hash01(k, 574))
            drawPath(tuftPath(c.x, c.y, 0.016f * u, 0f), if (k % 2 == 0) BrC.turfDark else BrC.turf)
        }
    }
    val fl = q(0.1f, -0.42f, 0.14f)
    drawLine(FxC.spruceLight, fl, Offset(fl.x, fl.y - 0.02f * u), strokeWidth = pen.lw, cap = StrokeCap.Round)
    inkedCircle(Offset(fl.x, fl.y - 0.024f * u), 0.007f * u, FxC.pink, pen, shade = false)
    // Sound rings spreading out when someone shouts.
    if (a > 0f) {
        val c = p(0f, -0.14f)
        for (k in 0 until 3) {
            val ph = (1f - a) * 1.4f - k * 0.16f
            if (ph <= 0f || ph >= 1f) continue
            val r = (0.07f + ph * 0.34f) * u
            val rect = Rect(c.x - r, c.y - r * 0.82f, c.x + r, c.y + r * 0.82f)
            drawOval(Color.White, rect.topLeft, rect.size, alpha = 0.75f * (1f - ph), style = Stroke(pen.lw * 3f))
            drawOval(Ink.line, rect.topLeft, rect.size, alpha = 0.3f * (1f - ph), style = pen.thin)
        }
        fxNote(p(0.3f, -0.36f - (1f - a) * 0.08f), 0.02f * u, FxC.fjord, a)
        fxNote(p(-0.32f, -0.3f - (1f - a) * 0.06f), 0.016f * u, FxC.red, a)
    }
}

// ---------------------------------------------------------------------------------------------- the eagle nest

private fun DrawScope.brNest(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val a = f.anim.coerceIn(0f, 1f)
    brShadow(u, 0.36f, 0.2f, 0.7f)
    val cz = 0.1f
    val twig = Color(0xFF8A6A44)
    val twigLight = Color(0xFFB08A5A)
    val b = q(0f, 0f, cz)
    val rimC = q(0f, -0.12f, cz)
    // The back wall of the bowl, then the inside with its down and the egg bed.
    val rimBack = fxDisc2(rimC.x, rimC.y, 0.17f * u, 0.1f * u)
    fxFace(rimBack, Color(0xFF4A3A2A), pen)
    val inner = fxDisc2(rimC.x, rimC.y + 0.006f * u, 0.135f * u, 0.07f * u)
    drawPath(inner, Color(0xFFD6C09A))
    drawPath(inner, Ink.line, alpha = 0.4f, style = pen.thin)
    for (k in 0 until 7) {
        val c = Offset(rimC.x + (hash01(k, 581) - 0.5f) * 0.2f * u, rimC.y + (hash01(k, 582) - 0.4f) * 0.04f * u)
        drawOval(Color(0xFFF3E9D2), Offset(c.x - 0.012f * u, c.y - 0.004f * u), Size(0.024f * u, 0.007f * u))
    }
    // The chick peeks out when tapped: a fluffy head with a big open beak.
    if (a > 0.02f) {
        val rise = ((a * 1.4f).coerceAtMost(1f)) * 0.075f
        val cc = Offset(rimC.x - 0.01f * u, rimC.y - 0.005f * u - rise * u + sin(t * 9f) * 0.003f * u * a)
        val fluff = Color(0xFFEDE3D0)
        for (k in 0 until 6) {
            val ang = k * FX_PI / 3f
            drawCircle(fluff, 0.012f * u, Offset(cc.x + cos(ang) * 0.028f * u, cc.y + sin(ang) * 0.022f * u))
        }
        inkedCircle(cc, 0.034f * u, fluff, pen)
        for (s in 0..1) {
            val e = Offset(cc.x + (s * 2 - 1) * 0.015f * u, cc.y - 0.004f * u)
            drawCircle(Color.White, 0.009f * u, e)
            drawCircle(Ink.line, 0.005f * u, Offset(e.x, e.y + 0.001f * u))
        }
        val open = 0.004f + 0.012f * abs(sin(t * 14f))
        inked(fxPoly(1f, cc.x - 0.014f * u, cc.y + 0.008f * u, cc.x + 0.014f * u, cc.y + 0.008f * u, cc.x, cc.y + (0.026f + open) * u), BrC.yellow, pen, shade = false)
        inked(fxPoly(1f, cc.x - 0.011f * u, cc.y + 0.01f * u, cc.x + 0.011f * u, cc.y + 0.01f * u, cc.x, cc.y + 0.004f * u), Color(0xFFD9667A), pen, shade = false)
    }
    // The woven twig wall of the front half of the bowl.
    val wall = Path().apply {
        moveTo(-0.17f * u, -0.12f * u)
        cubicTo(-0.18f * u, -0.04f * u, -0.13f * u, 0f, 0f, 0.004f * u)
        cubicTo(0.13f * u, 0f, 0.18f * u, -0.04f * u, 0.17f * u, -0.12f * u)
        quadraticTo(0f, -0.09f * u, -0.17f * u, -0.12f * u)
        close()
    }
    inked(wall, twig, pen, outline = false)
    clipPath(wall) {
        for (k in 0 until 18) {
            val y0 = -0.115f + 0.115f * hash01(k, 583)
            val x0 = -0.17f + 0.34f * hash01(k, 584)
            val len = 0.06f + 0.1f * hash01(k, 585)
            val tilt = (hash01(k, 586) - 0.5f) * 0.05f
            drawLine(if (k % 2 == 0) twigLight else twig.darken(0.3f), p(x0, y0), p(x0 + len, y0 + tilt), strokeWidth = (0.004f + 0.003f * hash01(k, 587)) * u, cap = StrokeCap.Round)
        }
    }
    drawPath(wall, Ink.line, style = pen.stroke)
    // The rim: a thick twisted ring of twigs along the front edge, with a few loose sticks.
    val rim = Path().apply {
        moveTo(-0.17f * u, -0.12f * u)
        quadraticTo(0f, -0.09f * u, 0.17f * u, -0.12f * u)
    }
    drawPath(rim, Ink.line, style = Stroke(0.013f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(rim, twigLight, style = Stroke(0.013f * u, cap = StrokeCap.Round))
    drawPath(rim, twig.darken(0.2f), style = Stroke(0.004f * u, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.012f * u, 0.009f * u))))
    capsule(p(0.14f, -0.12f), p(0.2f, -0.17f), 0.004f * u, twigLight, pen)
    capsule(p(-0.15f, -0.12f), p(-0.21f, -0.15f), 0.004f * u, twig, pen)
}

// ---------------------------------------------------------------------------------------------- the summit flag

private fun DrawScope.brFlag(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val a = f.anim.coerceIn(0f, 1f)
    val high = f.mode == 1
    brShadow(u, 0.22f, 0.14f, 0.7f)
    val cz = 0.05f
    // A little cairn at the foot.
    val c0 = q(0f, 0f, cz)
    val cairn = arrayOf(
        floatArrayOf(0f, -0.014f, 0.055f, 0.03f), floatArrayOf(0.004f, -0.04f, 0.042f, 0.025f), floatArrayOf(-0.002f, -0.063f, 0.03f, 0.02f),
    )
    for ((k, s) in cairn.withIndex()) {
        val r = Rect(c0.x + (s[0] - s[2]) * u, c0.y + (s[1] - s[3]) * u, c0.x + (s[0] + s[2]) * u, c0.y + (s[1] + s[3]) * u)
        inkedOval(r, if (k % 2 == 0) BrC.stone else BrC.stoneLight, pen)
    }
    // The pole, with a golden ball on top.
    val polePos = q(0f, -0.06f, cz)
    val topY = polePos.y - 0.15f * u
    capsule(polePos, Offset(polePos.x, topY), 0.007f * u, FxC.paint, pen)
    inkedCircle(Offset(polePos.x, topY - 0.005f * u), 0.009f * u, FxC.brass, pen)
    // The flag: red with a white fjord-and-mountain emblem. High and waving, or low at half mast and limp.
    val fw = 0.12f * u
    val fh = 0.078f * u
    val flutter = a * 0.5f
    val fy = if (high) topY + 0.006f * u else polePos.y - 0.088f * u
    val amp = if (high) (0.007f + 0.01f * flutter) * u else 0.0025f * u
    val speed = if (high) 6f + 8f * flutter else 1.5f
    val n = 7
    val top = FloatArray(n + 1)
    val bot = FloatArray(n + 1)
    for (k in 0..n) {
        val s = k / n.toFloat()
        val w = sin(t * speed - s * 4.5f) * amp * s
        top[k] = fy + w + (if (high) 0f else s * s * 0.012f * u)
        bot[k] = fy + fh + w + (if (high) 0f else s * s * 0.03f * u)
    }
    val cx0 = polePos.x
    val cloth = Path().apply {
        moveTo(cx0, top[0])
        for (k in 1..n) lineTo(cx0 + fw * k / n * (if (high) 1f else 0.82f + 0.18f * (1f - k / n.toFloat())), top[k])
        for (k in n downTo 0) lineTo(cx0 + fw * k / n * (if (high) 1f else 0.82f + 0.18f * (1f - k / n.toFloat())), bot[k])
        close()
    }
    inked(cloth, BrC.red, pen)
    clipPath(cloth) {
        val ex = cx0 + fw * 0.5f
        val ey = fy + fh * 0.58f + sin(t * speed - 2.2f) * amp * 0.5f
        val sw = fw * 0.32f
        // Two white mountains over a wavy fjord line.
        drawPath(fxPoly(1f, ex - sw, ey + fh * 0.12f, ex - sw * 0.3f, ey - fh * 0.34f, ex + sw * 0.2f, ey + fh * 0.12f), Color.White)
        drawPath(fxPoly(1f, ex - sw * 0.1f, ey + fh * 0.12f, ex + sw * 0.55f, ey - fh * 0.2f, ex + sw, ey + fh * 0.12f), Color(0xFFE8EEFA))
        val wave = Path().apply {
            moveTo(ex - sw, ey + fh * 0.22f)
            quadraticTo(ex - sw * 0.5f, ey + fh * 0.14f, ex, ey + fh * 0.22f)
            quadraticTo(ex + sw * 0.5f, ey + fh * 0.3f, ex + sw, ey + fh * 0.22f)
        }
        drawPath(wave, Color.White, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
    }
    // A flutter of sparkles when it is tapped.
    if (a > 0.1f) {
        for (k in 0 until 3) twinkle(Offset(cx0 + fw * (0.3f + 0.3f * k), fy - 0.02f * u - (1f - a) * 0.04f * u), 0.009f * u, Color.White, a)
    }
}
