package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Attractions
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * The tivoli's rides and stalls and the shop's furniture, in oblique 3D («skrå-3D»).
 *
 * Origin is the fixture's bottom centre, up is negative y, and one scene unit is [u] pixels. The back
 * layer always draws the whole fixture, so it looks right on its own; the front layer redraws only the
 * parts that must cover riders and contents (gondola tubs, the carousel's canopy and near poles, the
 * bumper car's shell, the checkout's front panel, the trolley's near wire side, glass sheen).
 */

/** Draws the back layer of one of the tivoli or shop fixtures. Returns false for any other type. */
fun DrawScope.drawRidesBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    if (contents.size < 0) return false
    when (f.type) {
        FixtureType.FERRIS_WHEEL -> ridesFerrisBack(f, u, pen)
        FixtureType.CAROUSEL -> ridesCarouselBack(f, u, pen)
        FixtureType.TRAMPOLINE -> ridesTrampoline(f, u, pen)
        FixtureType.CANDY_FLOSS_STAND -> ridesFlossStand(f, u, pen)
        FixtureType.POPCORN_CART -> ridesPopcornCart(f, u, pen)
        FixtureType.CAN_TOSS -> ridesCanToss(f, u, pen)
        FixtureType.BUMPER_CAR -> ridesBumperBack(f, u, pen)
        FixtureType.SHOP_SHELF -> ridesShelf(f, u, pen)
        FixtureType.SCALE -> ridesScale(f, u, pen)
        FixtureType.FREEZER -> ridesFreezer(f, u, pen)
        FixtureType.SODA_FRIDGE -> ridesFridge(f, u, pen)
        FixtureType.CHECKOUT -> ridesCheckoutBack(f, u, pen)
        FixtureType.CART -> ridesCartBack(f, u, pen)
        else -> return false
    }
    return true
}

/**
 * Draws the front layer (over riders and contents) of one of the tivoli or shop fixtures. Returns false
 * for any other type. Fixtures with nothing in front draw nothing here but still return true.
 */
fun DrawScope.drawRidesFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.FERRIS_WHEEL -> ridesFerrisFront(f, u, pen)
        FixtureType.CAROUSEL -> ridesCarouselFront(f, u, pen)
        FixtureType.BUMPER_CAR -> ridesBumperFront(f, u, pen)
        FixtureType.CHECKOUT -> ridesCheckoutFront(f, u, pen)
        FixtureType.CART -> ridesCartFront(f, u, pen)
        FixtureType.FREEZER -> ridesFreezerGlass(f, u, pen)
        FixtureType.SODA_FRIDGE -> ridesFridgeGlass(f, u, pen)
        FixtureType.TRAMPOLINE, FixtureType.CANDY_FLOSS_STAND, FixtureType.POPCORN_CART, FixtureType.CAN_TOSS,
        FixtureType.SHOP_SHELF, FixtureType.SCALE -> Unit
        else -> return false
    }
    return true
}

// ============================================================================================ kit

private const val RIDES_PI = 3.1415927f
private const val RIDES_TAU = 6.2831855f

private val ridesSteel = Color(0xFFC3CAD6)
private val ridesSteelDark = Color(0xFF6B7288)
private val ridesGold = Color(0xFFFFC83D)
private val ridesCoral = Color(0xFFFF5A5F)
private val ridesCream = Color(0xFFFFF4E0)
private val ridesTeal = Color(0xFF2EC4B6)
private val ridesPink = Color(0xFFFF8FC0)
private val ridesSky = Color(0xFF5AA9E6)
private val ridesNavy = Color(0xFF2B3A67)
private val ridesRubber = Color(0xFF3B3649)

private fun ridesDeg(a: Float): Float = a * 57.29578f

private fun ridesWrap(v: Float, m: Float): Float {
    val r = v % m
    return if (r < 0f) r + m else r
}

private fun ridesEase(v: Float): Float {
    val c = v.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}

/** A steady pseudo-random number from 0 to 1 for [i]. */
private fun ridesHash(i: Int): Float {
    var x = i * 0x27D4EB2D + 0x5BD1E995
    x = x xor (x ushr 15)
    x *= 0x2C1B3C6D
    x = x xor (x ushr 12)
    x *= 0x297A2D39
    x = x xor (x ushr 15)
    return (x and 0xFFFF) / 65536f
}

private fun ridesPoly(vararg p: Float): Path = Path().apply {
    moveTo(p[0], p[1])
    var i = 2
    while (i < p.size) {
        lineTo(p[i], p[i + 1])
        i += 2
    }
    close()
}

private fun ridesQuad(a: Offset, b: Offset, c: Offset, d: Offset): Path = Path().apply {
    moveTo(a.x, a.y)
    lineTo(b.x, b.y)
    lineTo(c.x, c.y)
    lineTo(d.x, d.y)
    close()
}

private fun DrawScope.ridesFlat(path: Path, color: Color, pen: Pen, thin: Boolean = false) {
    drawPath(path, color)
    drawPath(path, Ink.line, style = if (thin) pen.thin else pen.stroke)
}

/** A round tube along [path] with an ink rim. */
private fun DrawScope.ridesTube(path: Path, w: Float, color: Color, pen: Pen) {
    drawPath(path, Ink.line, style = Stroke(w + pen.lw * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, color, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.ridesRing(c: Offset, r: Float, w: Float, color: Color, pen: Pen) {
    drawCircle(Ink.line, r, c, style = Stroke(w + pen.lw * 2f))
    drawCircle(color, r, c, style = Stroke(w))
}

/** A light bulb: [lit] 0..1, with a halo that grows at night. */
private fun DrawScope.ridesBulb(c: Offset, r: Float, color: Color, lit: Float, pen: Pen) {
    val l = lit.coerceIn(0f, 1f)
    val glow = l * (0.3f + 0.7f * pen.night)
    if (glow > 0.04f) {
        drawCircle(color.copy(alpha = 0.18f * glow), r * 3.4f, c)
        drawCircle(color.copy(alpha = 0.32f * glow), r * 2f, c)
    }
    drawCircle(Ink.line, r + pen.lw * 0.5f, c)
    drawCircle(lerp(color.darken(0.4f), color.lighten(0.3f), l), r, c)
    drawCircle(Color.White.copy(alpha = 0.3f + 0.6f * l), r * 0.42f, Offset(c.x - r * 0.28f, c.y - r * 0.3f))
}

/** A pole with a candy-cane spiral. */
private fun DrawScope.ridesCandyPole(a: Offset, b: Offset, w: Float, base: Color, stripe: Color, pen: Pen) {
    capsule(a, b, w, base, pen)
    val v = b - a
    val len = v.getDistance()
    if (len < 1f) return
    val dir = v / len
    val nrm = Offset(-dir.y, dir.x)
    var s = w * 0.9f
    while (s < len - w * 0.6f) {
        val c = a + dir * s
        drawLine(stripe, c - nrm * (w * 0.42f) - dir * (w * 0.3f), c + nrm * (w * 0.42f) + dir * (w * 0.3f), strokeWidth = w * 0.42f)
        s += w * 1.7f
    }
    drawLine(Color.White.copy(alpha = 0.45f), a - nrm * (w * 0.22f), b - nrm * (w * 0.22f), strokeWidth = w * 0.18f, cap = StrokeCap.Round)
}

/** A zigzag spring from [a] to [b]. */
private fun DrawScope.ridesSpring(a: Offset, b: Offset, amp: Float, turns: Int, color: Color, width: Float) {
    val v = b - a
    val len = v.getDistance()
    if (len < 0.5f) return
    val dir = v / len
    val nrm = Offset(-dir.y, dir.x)
    val p = Path()
    p.moveTo(a.x, a.y)
    val n = turns * 2
    for (k in 1 until n) {
        val c = a + dir * (len * k / n) + nrm * (if (k % 2 == 0) -amp else amp)
        p.lineTo(c.x, c.y)
    }
    p.lineTo(b.x, b.y)
    drawPath(p, Ink.line, style = Stroke(width * 2.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(p, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** A spoked wheel turned by [turn] radians. */
private fun DrawScope.ridesWheel(c: Offset, r: Float, turn: Float, tire: Color, rim: Color, spokes: Int, pen: Pen) {
    inkedCircle(c, r, tire, pen)
    drawCircle(rim, r * 0.74f, c, style = Stroke(r * 0.16f))
    for (k in 0 until spokes) {
        val a = turn + k * RIDES_TAU / spokes
        drawLine(rim, c, c + Offset(cos(a) * r * 0.72f, sin(a) * r * 0.72f), strokeWidth = max(pen.lw * 0.8f, r * 0.08f), cap = StrokeCap.Round)
    }
    drawCircle(Ink.line, r * 0.74f + r * 0.08f, c, style = Stroke(pen.lw * 0.5f))
    inkedCircle(c, r * 0.22f, rim.lighten(0.2f), pen, shade = false)
}

/** Fills a strip of stripes clipped to [clip]. */
private fun DrawScope.ridesStripes(clip: Path, left: Float, top: Float, right: Float, bottom: Float, step: Float, width: Float, color: Color, slant: Float = 0f) {
    clipPath(clip) {
        var x = left - abs(slant) - step
        while (x < right + abs(slant)) {
            drawPath(ridesPoly(x, top, x + width, top, x + width + slant, bottom, x + slant, bottom), color)
            x += step
        }
    }
}

private val RIDES_SEG = intArrayOf(63, 6, 91, 79, 102, 109, 125, 7, 127, 111)

/** One seven-segment digit in the box ([left], [top], [w], [h]); a digit outside 0..9 is blank. */
private fun DrawScope.ridesDigit(left: Float, top: Float, w: Float, h: Float, digit: Int, on: Color, off: Color, glow: Boolean = false) {
    val th = w * 0.26f
    val t2 = th / 2f
    val g = th * 0.22f
    val x0 = left + t2
    val x1 = left + w - t2
    val y0 = top + t2
    val y2 = top + h - t2
    val mid = (y0 + y2) / 2f
    val mask = if (digit in 0..9) RIDES_SEG[digit] else 0
    fun sk(x: Float, y: Float) = x + (mid - y) * 0.13f
    fun seg(ax: Float, ay: Float, bx: Float, by: Float, horizontal: Boolean): Path = Path().apply {
        if (horizontal) {
            moveTo(sk(ax, ay), ay)
            lineTo(sk(ax + t2, ay - t2), ay - t2)
            lineTo(sk(bx - t2, by - t2), by - t2)
            lineTo(sk(bx, by), by)
            lineTo(sk(bx - t2, by + t2), by + t2)
            lineTo(sk(ax + t2, ay + t2), ay + t2)
        } else {
            moveTo(sk(ax, ay), ay)
            lineTo(sk(ax + t2, ay + t2), ay + t2)
            lineTo(sk(bx + t2, by - t2), by - t2)
            lineTo(sk(bx, by), by)
            lineTo(sk(bx - t2, by - t2), by - t2)
            lineTo(sk(ax - t2, ay + t2), ay + t2)
        }
        close()
    }
    for (k in 0 until 7) {
        val p = when (k) {
            0 -> seg(x0 + g, y0, x1 - g, y0, true)
            1 -> seg(x1, y0 + g, x1, mid - g, false)
            2 -> seg(x1, mid + g, x1, y2 - g, false)
            3 -> seg(x0 + g, y2, x1 - g, y2, true)
            4 -> seg(x0, mid + g, x0, y2 - g, false)
            5 -> seg(x0, y0 + g, x0, mid - g, false)
            else -> seg(x0 + g, mid, x1 - g, mid, true)
        }
        val lit = mask and (1 shl k) != 0
        if (lit && glow) drawPath(p, on.copy(alpha = 0.35f), style = Stroke(th * 0.9f, join = StrokeJoin.Round))
        drawPath(p, if (lit) on else off)
    }
}

/** [value] as [digits] seven-segment digits ending at [right]; leading zeros stay dark. */
private fun DrawScope.ridesNumber(value: Int, digits: Int, right: Float, top: Float, w: Float, h: Float, gap: Float, on: Color, off: Color, glow: Boolean = false) {
    var limit = 1
    repeat(digits) { limit *= 10 }
    val v = value.coerceIn(0, limit - 1)
    var place = 1
    for (k in 0 until digits) {
        val left = right - (k + 1) * w - k * gap
        val d = if (k > 0 && v < place) -1 else (v / place) % 10
        ridesDigit(left, top, w, h, d, on, if (d < 0) off.copy(alpha = off.alpha * 0.5f) else off, glow)
        place *= 10
    }
}

/**
 * A striped awning: a sloping top from the front edge at [y] back by [depth] pixels (rising by [rise]),
 * with a scalloped valance [drop] pixels deep along the front.
 */
private fun DrawScope.ridesAwning(left: Float, right: Float, y: Float, depth: Float, rise: Float, n: Int, c1: Color, c2: Color, drop: Float, pen: Pen) {
    val dx = Oblique.DX * depth
    val dy = Oblique.DY * depth - rise
    val top = ridesPoly(left, y, right, y, right + dx, y + dy, left + dx, y + dy)
    drawPath(top, c1.lighten(0.1f))
    for (i in 0 until n step 2) {
        val a = left + (right - left) * i / n
        val b = left + (right - left) * (i + 1) / n
        drawPath(ridesPoly(a, y, b, y, b + dx, y + dy, a + dx, y + dy), c2.lighten(0.08f))
    }
    drawPath(top, Ink.line, style = pen.stroke)
    // The valance, one scallop per stripe.
    val sw = (right - left) / n
    for (i in 0 until n) {
        val a = left + sw * i
        val b = a + sw
        val p = Path().apply {
            moveTo(a, y)
            lineTo(b, y)
            lineTo(b, y + drop * 0.55f)
            quadraticTo((a + b) / 2f, y + drop * 1.45f, a, y + drop * 0.55f)
            close()
        }
        val c = if (i % 2 == 0) c2 else c1
        inked(p, c, pen, outline = false)
        drawPath(p, Ink.line, style = pen.thin)
    }
    drawLine(Ink.line, Offset(left, y), Offset(right, y), strokeWidth = pen.lw)
    drawLine(Color.White.copy(alpha = 0.6f), Offset(left + pen.lw, y + pen.lw * 1.2f), Offset(right - pen.lw, y + pen.lw * 1.2f), strokeWidth = pen.lw * 0.6f)
}

/** Bunting: a sagging string from [a] to [b] with [n] little flags. */
private fun DrawScope.ridesBunting(a: Offset, b: Offset, sag: Float, n: Int, size: Float, t: Float, pen: Pen) {
    val colors = longArrayOf(0xFFFF5A5F, 0xFFFFC83D, 0xFF2EC4B6, 0xFFB58CFF, 0xFF5AA9E6, 0xFFFF8FC0)
    fun at(q: Float) = Offset(a.x + (b.x - a.x) * q, a.y + (b.y - a.y) * q + sag * 4f * q * (1f - q))
    val string = Path().apply {
        moveTo(a.x, a.y)
        quadraticTo((a.x + b.x) / 2f, (a.y + b.y) / 2f + sag * 2f, b.x, b.y)
    }
    drawPath(string, Ink.line, style = pen.thin)
    for (i in 0 until n) {
        val q0 = (i + 0.15f) / n
        val q1 = (i + 0.85f) / n
        val p0 = at(q0)
        val p1 = at(q1)
        val sway = sin(t * 2.2f + i * 1.3f) * size * 0.12f
        val tip = Offset((p0.x + p1.x) / 2f + sway, (p0.y + p1.y) / 2f + size)
        val flag = ridesPoly(p0.x, p0.y, p1.x, p1.y, tip.x, tip.y)
        drawPath(flag, Color(colors[i % colors.size]))
        drawPath(flag, Ink.line, style = pen.thin)
    }
}

// ==================================================================================== FERRIS WHEEL

private val RIDES_GONDOLA = longArrayOf(0xFFFF6B5E, 0xFFFFC83D, 0xFF34C6B8, 0xFFB58CFF)
private val RIDES_BULB = longArrayOf(0xFFFFE08A, 0xFFFF9EC4, 0xFFA8F0FF)

/** Where the rider of gondola [i] sits, in pixels from the fixture's origin (matches Attractions.seatPoint). */
private fun ridesWheelSeat(f: Fixture, i: Int, u: Float): Offset {
    val a = f.angle + i * (RIDES_PI / 2f)
    val r = Attractions.WHEEL_RADIUS
    return Offset(cos(a) * r * u, (-Attractions.WHEEL_HUB + sin(a) * r + 0.075f) * u)
}

private fun DrawScope.ridesFerrisBack(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val hub = Offset(0f, -Attractions.WHEEL_HUB * u)
    val r = Attractions.WHEEL_RADIUS * u
    val thick = Oblique.offset(0.06f, u)
    val frame = Oblique.offset(0.13f, u)
    val steel = Color(0xFF3F74C4)
    val rim = ridesCoral

    val glow = pen.night * (if (f.on) 1f else 0.45f)
    if (glow > 0.01f) {
        val gr = r * 1.6f
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD98A).copy(alpha = 0.45f * glow), Color(0x00FFD98A)), hub, gr), gr, hub)
    }
    groundShadow(0.04f * u, -0.012f * u, 0.84f * u, 0.9f)

    // Concrete footings, deep enough for both frames' feet.
    val footY = -0.022f * u
    for (s in intArrayOf(-1, 1)) {
        box3d(Rect(s * 0.3f * u - 0.042f * u, footY, s * 0.3f * u + 0.042f * u, 0f), 0.17f * u, Color(0xFFD3D8E2), pen)
    }

    // The back A-frame and the axle.
    val apexB = hub + frame
    val qb = 0.64f
    for (s in intArrayOf(-1, 1)) capsule(apexB, Offset(s * 0.3f * u, footY) + frame, 0.022f * u, steel.darken(0.34f), pen)
    capsule(
        apexB + (Offset(-0.3f * u, footY) + frame - apexB) * qb,
        apexB + (Offset(0.3f * u, footY) + frame - apexB) * qb,
        0.012f * u, steel.darken(0.34f), pen,
    )
    capsule(apexB, hub, 0.034f * u, Color(0xFF9AA3B5), pen)

    // The wheel's far ring and the ties that give it thickness.
    val hubB = hub + thick
    ridesRing(hubB, r, 0.014f * u, rim.darken(0.42f), pen)
    ridesRing(hubB, r * 0.68f, 0.007f * u, ridesGold.darken(0.36f), pen)
    for (k in 0 until 8) {
        val a = f.angle + k * RIDES_PI / 4f
        val p = hub + Offset(cos(a) * r, sin(a) * r)
        capsule(p, p + thick, 0.007f * u, Color(0xFFB8C0CF), pen)
    }

    // Spokes, lattice and the near rings.
    for (k in 0 until 8) {
        val a = f.angle + k * RIDES_PI / 4f
        capsule(hub, hub + Offset(cos(a) * r, sin(a) * r), 0.008f * u, ridesCream, pen)
    }
    val zig = Path()
    for (k in 0..16) {
        val a = f.angle + k * RIDES_PI / 8f
        val ix = hub.x + cos(a) * r * 0.68f
        val iy = hub.y + sin(a) * r * 0.68f
        if (k == 0) zig.moveTo(ix, iy) else zig.lineTo(ix, iy)
        if (k < 16) {
            val b = a + RIDES_PI / 16f
            zig.lineTo(hub.x + cos(b) * r, hub.y + sin(b) * r)
        }
    }
    ridesTube(zig, 0.0042f * u, ridesCream, pen)
    ridesRing(hub, r * 0.68f, 0.009f * u, ridesGold, pen)
    ridesRing(hub, r, 0.019f * u, rim, pen)
    drawArc(rim.lighten(0.5f), 198f, 62f, false, Offset(hub.x - r, hub.y - r), Size(2f * r, 2f * r), style = Stroke(0.005f * u, cap = StrokeCap.Round))

    // Bulbs round the rim: chasing when it turns, a slow twinkle when it stands still.
    for (k in 0 until 24) {
        val a = f.angle + (k + 0.5f) * RIDES_TAU / 24f
        val lit = if (f.on) (if (ridesWrap(k - t * 9f, 6f) < 2f) 1f else 0.35f) else 0.62f + 0.25f * sin(t * 2.1f + k * 1.7f)
        ridesBulb(hub + Offset(cos(a) * r, sin(a) * r), 0.0078f * u, Color(RIDES_BULB[k % 3]), lit, pen)
    }

    // The near A-frame with a cross brace and bolts.
    for (s in intArrayOf(-1, 1)) capsule(hub, Offset(s * 0.3f * u, footY), 0.025f * u, steel, pen)
    val bl = hub + (Offset(-0.3f * u, footY) - hub) * qb
    val br = hub + (Offset(0.3f * u, footY) - hub) * qb
    capsule(bl, br, 0.013f * u, steel, pen)
    for (s in intArrayOf(-1, 1)) {
        drawLine(steel.lighten(0.45f), hub + Offset(s * 0.012f * u, 0.02f * u), Offset(s * 0.29f * u, footY - 0.012f * u) + Offset(-s * 0.012f * u, 0f), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
        inkedCircle(if (s < 0) bl else br, 0.008f * u, ridesGold, pen, shade = false)
    }

    ridesSunHub(hub, u, f.angle, f.on, t, pen)

    for (i in 0 until 4) ridesGondolaBack(ridesWheelSeat(f, i, u), Color(RIDES_GONDOLA[i]), u, pen, thick)
    for (i in 0 until 4) ridesGondolaFront(ridesWheelSeat(f, i, u), Color(RIDES_GONDOLA[i]), u, pen)
}

private fun DrawScope.ridesFerrisFront(f: Fixture, u: Float, pen: Pen) {
    for (i in 0 until 4) ridesGondolaFront(ridesWheelSeat(f, i, u), Color(RIDES_GONDOLA[i]), u, pen)
}

/** The hub: a sunburst that turns with the wheel and a face that stays upright and cheers when it runs. */
private fun DrawScope.ridesSunHub(c: Offset, u: Float, spin: Float, happy: Boolean, t: Float, pen: Pen) {
    val rays = Path()
    val n = 12
    for (k in 0 until n * 2) {
        val rr = if (k % 2 == 0) 0.074f * u else 0.052f * u
        val a = spin + k * RIDES_PI / n
        val x = c.x + cos(a) * rr
        val y = c.y + sin(a) * rr
        if (k == 0) rays.moveTo(x, y) else rays.lineTo(x, y)
    }
    rays.close()
    inked(rays, Color(0xFFFFB02E), pen)
    val rr = 0.048f * u
    inkedCircle(c, rr, Color(0xFFFFD84D), pen)
    val ey = c.y - rr * 0.14f
    val ex = rr * 0.36f
    if (happy) {
        for (s in intArrayOf(-1, 1)) {
            drawArc(Ink.line, 200f, 140f, false, Offset(c.x + s * ex - rr * 0.15f, ey - rr * 0.06f), Size(rr * 0.3f, rr * 0.24f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
        }
        val m = Path().apply {
            moveTo(c.x - rr * 0.32f, c.y + rr * 0.16f)
            quadraticTo(c.x, c.y + rr * 0.84f, c.x + rr * 0.32f, c.y + rr * 0.16f)
            close()
        }
        drawPath(m, Color(0xFF9E3346))
        clipPath(m) { drawCircle(Color(0xFFFF7F8E), rr * 0.2f, Offset(c.x, c.y + rr * 0.52f)) }
        drawPath(m, Ink.line, style = pen.thin)
    } else {
        val blink = ridesWrap(t, 3.7f) < 0.13f
        for (s in intArrayOf(-1, 1)) {
            val e = Offset(c.x + s * ex, ey)
            if (blink) {
                drawLine(Ink.line, e - Offset(rr * 0.1f, 0f), e + Offset(rr * 0.1f, 0f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
            } else {
                drawCircle(Ink.line, rr * 0.11f, e)
                drawCircle(Color.White, rr * 0.04f, e - Offset(rr * 0.035f, rr * 0.04f))
            }
        }
        drawArc(Ink.line, 25f, 130f, false, Offset(c.x - rr * 0.3f, c.y - rr * 0.08f), Size(rr * 0.6f, rr * 0.46f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    }
    for (s in intArrayOf(-1, 1)) drawCircle(Ink.blush, rr * 0.15f, Offset(c.x + s * rr * 0.6f, c.y + rr * 0.16f))
    shine(Offset(c.x - rr * 0.45f, c.y - rr * 0.5f), rr * 0.3f, rr * 0.18f, 0.7f)
}

/** What hangs behind the rider: the pivot, the yoke arms, the seat back and the inside of the tub. */
private fun DrawScope.ridesGondolaBack(seat: Offset, color: Color, u: Float, pen: Pen, thick: Offset) {
    val pv = seat - Offset(0f, 0.075f * u)
    val rimY = seat.y - 0.028f * u
    val rx = 0.088f * u
    val ry = 0.022f * u
    capsule(pv, pv + thick, 0.011f * u, Color(0xFF9AA3B5), pen)
    drawOval(color.darken(0.55f), Offset(seat.x - rx, rimY - ry), Size(rx * 2f, ry * 2f))
    drawOval(Ink.line, Offset(seat.x - rx, rimY - ry), Size(rx * 2f, ry * 2f), style = pen.stroke)
    val backRest = Rect(seat.x - 0.062f * u, seat.y - 0.112f * u, seat.x + 0.062f * u, rimY + ry * 0.2f)
    inkedRound(backRest, 0.03f * u, color.darken(0.1f), pen)
    val cushion = Rect(backRest.left + 0.012f * u, backRest.top + 0.01f * u, backRest.right - 0.012f * u, backRest.bottom - 0.004f * u)
    drawRoundRect(color.lighten(0.3f), cushion.topLeft, cushion.size, CornerRadius(0.022f * u))
    for (k in 1..2) {
        val x = cushion.left + cushion.width * k / 3f
        drawLine(color.darken(0.2f), Offset(x, cushion.top + 0.006f * u), Offset(x, cushion.bottom - 0.004f * u), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
    }
    for (s in intArrayOf(-1, 1)) capsule(pv, Offset(seat.x + s * rx * 0.97f, rimY), 0.008f * u, ridesSteelDark, pen)
    inkedCircle(pv, 0.013f * u, ridesGold, pen)
    drawCircle(Ink.line, 0.0035f * u, pv)
}

/** The tub's front, drawn over the rider's legs. */
private fun DrawScope.ridesGondolaFront(seat: Offset, color: Color, u: Float, pen: Pen) {
    val rimY = seat.y - 0.028f * u
    val rx = 0.088f * u
    val ry = 0.022f * u
    val bottom = seat.y + 0.048f * u
    val cx = seat.x
    val oval = Rect(cx - rx, rimY - ry, cx + rx, rimY + ry)
    val body = Path().apply {
        moveTo(cx - rx, rimY)
        cubicTo(cx - rx, rimY + (bottom - rimY) * 0.78f, cx - rx * 0.6f, bottom, cx, bottom)
        cubicTo(cx + rx * 0.6f, bottom, cx + rx, rimY + (bottom - rimY) * 0.78f, cx + rx, rimY)
        arcTo(oval, 0f, 180f, false)
        close()
    }
    inked(body, color, pen, outline = false)
    clipPath(body) {
        val band = oval.translate(0f, (bottom - rimY) * 0.5f)
        drawArc(Color.White, 0f, 180f, false, band.topLeft, band.size, style = Stroke(0.011f * u))
        drawArc(color.darken(0.25f), 0f, 180f, false, band.translate(0f, 0.009f * u).topLeft, band.size, style = Stroke(pen.lw * 0.6f))
    }
    drawPath(body, Ink.line, style = pen.stroke)
    drawArc(color.lighten(0.45f), 12f, 156f, false, oval.topLeft, oval.size, style = Stroke(0.006f * u, cap = StrokeCap.Round))
    val star = starPath(Offset(cx, rimY + (bottom - rimY) * 0.3f), 0.013f * u, 0.0058f * u)
    drawPath(star, Color.White)
    drawPath(star, Ink.line, style = pen.thin)
    for (s in intArrayOf(-1, 1)) inkedCircle(Offset(cx + s * rx * 0.97f, rimY), 0.0075f * u, ridesSteel, pen, shade = false)
    shine(Offset(cx - rx * 0.62f, rimY + (bottom - rimY) * 0.35f), 0.01f * u, 0.022f * u, 0.55f)
}

// ======================================================================================== CAROUSEL

private const val CAR_PLAT_Y = -0.095f
private const val CAR_PLAT_RX = 0.345f
private const val CAR_PLAT_RY = 0.06f
private const val CAR_PLAT_TH = 0.036f
private const val CAR_CAN_Y = -0.55f
private const val CAR_CAN_RX = 0.372f
private const val CAR_CAN_RY = 0.058f
private const val CAR_APEX_Y = -0.64f
private const val CAR_VAL_H = 0.03f
private const val CAR_COL_HW = 0.048f
private const val CAR_RING = 0.24f

private val RIDES_HORSE_BODY = longArrayOf(0xFFFFF8EE, 0xFFF0C07A, 0xFFCFC6F0)
private val RIDES_HORSE_MANE = longArrayOf(0xFFFF7FB0, 0xFF9A5530, 0xFF5AA9E6)
private val RIDES_HORSE_SADDLE = longArrayOf(0xFF2EC4B6, 0xFFFF5A5F, 0xFFFFC83D)

/**
 * The three horses, four numbers each: x and y of the saddle in units (as Attractions.seatPoint), facing
 * (-1..1, squeezed thin where a horse turns at the sides) and the sine of its angle (> 0: near side).
 */
private fun ridesHorses(f: Fixture, t: Float): FloatArray {
    val out = FloatArray(12)
    for (i in 0 until 3) {
        val a = f.angle + i * (RIDES_TAU / 3f)
        val s = sin(a)
        val bob = if (f.on) sin(t * 4f + i * 2f) * 0.018f else 0f
        val m = (abs(s) / 0.25f).coerceIn(0.1f, 1f)
        out[i * 4] = cos(a) * CAR_RING
        out[i * 4 + 1] = -0.2f + s * 0.035f + bob
        out[i * 4 + 2] = if (s > 0f) -m else m
        out[i * 4 + 3] = s
    }
    return out
}

private fun ridesHorseOrder(hs: FloatArray): IntArray {
    val o = intArrayOf(0, 1, 2)
    for (a in 0 until 3) for (b in 0 until 2 - a) {
        if (hs[o[b] * 4 + 3] > hs[o[b + 1] * 4 + 3]) {
            val tmp = o[b]
            o[b] = o[b + 1]
            o[b + 1] = tmp
        }
    }
    return o
}

private const val RIDES_HORSE_K = 1.35f

private fun ridesPoleX(hs: FloatArray, i: Int): Float = hs[i * 4] + hs[i * 4 + 2] * 0.085f

/** A horse turning at the side of the ride is drawn thin; its head and pole then stay behind the rider. */
private fun ridesSqueezed(hs: FloatArray, i: Int): Boolean = abs(hs[i * 4 + 2]) < 0.55f

private fun DrawScope.ridesCarouselBack(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val hs = ridesHorses(f, t)
    val order = ridesHorseOrder(hs)
    val glow = pen.night * (if (f.on) 1f else 0.45f)
    if (glow > 0.01f) {
        val c = Offset(0f, -0.3f * u)
        val gr = 0.5f * u
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD98A).copy(alpha = 0.42f * glow), Color(0x00FFD98A)), c, gr), gr, c)
    }
    groundShadow(0f, -0.004f * u, 0.8f * u, 0.9f)
    ridesPlatform(f, u, pen)
    for (i in order) {
        if (hs[i * 4 + 3] > 0f) continue
        ridesHorsePole(hs, i, u, pen, 0)
        ridesHorse(hs[i * 4] * u, hs[i * 4 + 1] * u, hs[i * 4 + 2], i, u, pen, false)
    }
    ridesColumn(f, u, pen)
    for (i in order) {
        if (hs[i * 4 + 3] <= 0f) continue
        if (ridesSqueezed(hs, i)) {
            ridesHorsePole(hs, i, u, pen, 0)
            ridesHorse(hs[i * 4] * u, hs[i * 4 + 1] * u, hs[i * 4 + 2], i, u, pen, false)
            continue
        }
        ridesHorsePole(hs, i, u, pen, 1)
        ridesHorse(hs[i * 4] * u, hs[i * 4 + 1] * u, hs[i * 4 + 2], i, u, pen, false)
        ridesHorsePole(hs, i, u, pen, 2)
        ridesHorse(hs[i * 4] * u, hs[i * 4 + 1] * u, hs[i * 4 + 2], i, u, pen, true)
    }
    ridesCanopy(f, u, pen)
}

/**
 * Over the riders: the centre column where no near rider stands in front of it (so riders on the far
 * side pass behind it), the near horses' heads and poles, and the whole canopy.
 */
private fun DrawScope.ridesCarouselFront(f: Fixture, u: Float, pen: Pen) {
    val hs = ridesHorses(f, pen.t)
    val order = ridesHorseOrder(hs)
    val hw = CAR_COL_HW * u + pen.lw * 2f
    // The column's visible strips: its width minus the near horses and their riders.
    var segs = listOf(-hw to hw)
    for (i in 0 until 3) {
        if (hs[i * 4 + 3] <= 0f) continue
        val cl = (hs[i * 4] - 0.17f) * u
        val cr = (hs[i * 4] + 0.17f) * u
        val next = ArrayList<Pair<Float, Float>>(3)
        for ((a, b) in segs) {
            if (cr <= a || cl >= b) {
                next += a to b
                continue
            }
            if (cl > a) next += a to cl
            if (cr < b) next += cr to b
        }
        segs = next
    }
    for ((a, b) in segs) {
        if (b - a < 0.5f) continue
        clipRect(a, CAR_APEX_Y * u, b, 0f) { ridesColumn(f, u, pen) }
    }
    for (i in order) {
        if (hs[i * 4 + 3] <= 0f || ridesSqueezed(hs, i)) continue
        ridesHorsePole(hs, i, u, pen, 2)
        ridesHorse(hs[i * 4] * u, hs[i * 4 + 1] * u, hs[i * 4 + 2], i, u, pen, true)
    }
    ridesCanopy(f, u, pen)
}

/** A brass pole: [part] 0 whole, 1 below the horse, 2 above it. */
private fun DrawScope.ridesHorsePole(hs: FloatArray, i: Int, u: Float, pen: Pen, part: Int) {
    val s = hs[i * 4 + 3]
    val x = ridesPoleX(hs, i) * u
    val horseY = hs[i * 4 + 1] * u
    val floor = (CAR_PLAT_Y + s * CAR_PLAT_RY * (CAR_RING / CAR_PLAT_RX)) * u
    val top = (CAR_CAN_Y + s * CAR_CAN_RY * (CAR_RING / CAR_CAN_RX)) * u
    val (y0, y1) = when (part) {
        1 -> horseY + 0.05f * u to floor
        2 -> top to horseY
        else -> top to floor
    }
    val w = 0.011f * u
    ridesCandyPole(Offset(x, y0), Offset(x, y1), w, Color(0xFFF6C453), Color.White.copy(alpha = 0.85f), pen)
    if (part != 2) inkedOval(Rect(x - w * 1.3f, y1 - w * 0.45f, x + w * 1.3f, y1 + w * 0.45f), Color(0xFFE0A93A), pen, shade = false)
}

private fun DrawScope.ridesPlatform(f: Fixture, u: Float, pen: Pen) {
    val cy = CAR_PLAT_Y * u
    val rx = CAR_PLAT_RX * u
    val ry = CAR_PLAT_RY * u
    val th = CAR_PLAT_TH * u
    val oval = Rect(-rx, cy - ry, rx, cy + ry)
    val ovalB = oval.translate(0f, th)
    fun rim(a: Float, drop: Float) = Offset(cos(a) * rx, cy + sin(a) * ry + drop)
    val band = Path().apply {
        moveTo(-rx, cy)
        lineTo(-rx, cy + th)
        arcTo(ovalB, 180f, -180f, false)
        lineTo(rx, cy)
        arcTo(oval, 0f, 180f, false)
        close()
    }
    drawPath(band, Color(0xFFF2545B))
    // Panels round the skirt turn with the ride.
    val n = 18
    val step = RIDES_TAU / n
    val base = ridesWrap(f.angle, step)
    val first = floor(f.angle / step).toInt()
    clipPath(band) {
        for (j in -1..n / 2 + 1) {
            val a0 = base + j * step
            val a1 = a0 + step
            if (a1 <= 0f || a0 >= RIDES_PI) continue
            val c0 = max(a0, 0f)
            val c1 = min(a1, RIDES_PI)
            val cm = (c0 + c1) / 2f
            val p = Path().apply {
                val p0 = rim(c0, 0f)
                moveTo(p0.x, p0.y)
                val pm = rim(cm, 0f)
                val p1 = rim(c1, 0f)
                lineTo(pm.x, pm.y)
                lineTo(p1.x, p1.y)
                lineTo(p1.x, p1.y + th)
                lineTo(pm.x, pm.y + th)
                lineTo(p0.x, p0.y + th)
                close()
            }
            val light = (j + first) % 2 == 0
            drawPath(p, if (light) Color(0xFFFFF3DC) else Color(0xFFF2545B))
            drawPath(p, Ink.line.copy(alpha = 0.55f), style = pen.thin)
            if (sin(cm) > 0.2f) {
                val pm = rim(cm, th * 0.5f)
                ridesBulb(pm, 0.0065f * u, Color(RIDES_BULB[(j + first).mod(3)]), if (f.on) 0.6f + 0.4f * sin(pen.t * 6f + j) else 0.55f, pen)
            }
        }
        drawRect(Ink.line.copy(alpha = 0.18f), Offset(-rx, cy + th * 0.72f), Size(rx * 2f, th))
    }
    drawPath(band, Ink.line, style = pen.stroke)
    // The floor, boards turning with the ride.
    val floorC = Color(0xFFEBC08A)
    drawOval(floorC, oval.topLeft, oval.size)
    clipPath(Path().apply { addOval(oval) }) {
        drawOval(floorC.lighten(0.14f), Offset(-rx * 0.96f, cy - ry * 1.02f), Size(rx * 1.8f, ry * 1.7f))
        for (k in 0 until 16) {
            val a = f.angle + k * RIDES_TAU / 16f
            drawLine(floorC.darken(0.2f), Offset(cos(a) * rx * 0.2f, cy + sin(a) * ry * 0.2f), Offset(cos(a) * rx, cy + sin(a) * ry), strokeWidth = pen.lw * 0.6f)
        }
        val inner = Rect(-rx * 0.72f, cy - ry * 0.72f, rx * 0.72f, cy + ry * 0.72f)
        drawOval(Color(0xFFFFE3B0), inner.topLeft, inner.size, style = Stroke(0.006f * u))
        drawOval(floorC.darken(0.25f), inner.topLeft, inner.size, style = Stroke(pen.lw * 0.5f))
    }
    drawOval(Ink.line, oval.topLeft, oval.size, style = pen.stroke)
    drawArc(Color(0xFFE0A93A), 10f, 160f, false, oval.topLeft, oval.size, style = Stroke(0.004f * u))
}

private fun DrawScope.ridesColumn(f: Fixture, u: Float, pen: Pen) {
    val hw = CAR_COL_HW * u
    val top = (CAR_CAN_Y + 0.02f) * u
    val bot = CAR_PLAT_Y * u
    val ry = 0.011f * u
    val body = Path().apply {
        moveTo(-hw, top)
        lineTo(-hw, bot)
        arcTo(Rect(-hw, bot - ry, hw, bot + ry), 180f, -180f, false)
        lineTo(hw, top)
        close()
    }
    val gold = Color(0xFFF6C453)
    inked(body, gold, pen, outline = false)
    clipPath(body) {
        // Mirror panels on the drum turn with the ride.
        for (k in 0 until 6) {
            val a = f.angle + k * RIDES_PI / 3f
            if (sin(a) < 0.12f) continue
            val x1 = cos(a + 0.38f) * hw * 0.94f
            val x2 = cos(a - 0.38f) * hw * 0.94f
            val l = min(x1, x2)
            val r = max(x1, x2)
            if (r - l < pen.lw * 1.2f) continue
            for (tier in 0 until 2) {
                val y0 = if (tier == 0) top + 0.07f * u else top + 0.23f * u
                val y1 = if (tier == 0) top + 0.2f * u else bot - 0.035f * u
                val panel = Rect(l, y0, r, y1)
                val mirror = if (tier == 0) Color(0xFFBFE6FF) else Color(0xFFFF8FA8)
                drawRoundRect(Brush.verticalGradient(listOf(mirror.lighten(0.4f), mirror), y0, y1), panel.topLeft, panel.size, CornerRadius(min(panel.width, 0.02f * u) * 0.5f))
                drawRoundRect(Ink.line, panel.topLeft, panel.size, CornerRadius(min(panel.width, 0.02f * u) * 0.5f), style = pen.thin)
                if (tier == 0 && panel.width > 0.02f * u) {
                    drawLine(Color.White.copy(alpha = 0.8f), Offset(l + panel.width * 0.3f, y0 + 0.02f * u), Offset(l + panel.width * 0.55f, y0 + 0.05f * u), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
                }
            }
        }
        for (y in floatArrayOf(top + 0.05f * u, top + 0.215f * u)) {
            drawArc(Color(0xFFE0A93A).darken(0.1f), 0f, 180f, false, Offset(-hw, y - ry), Size(hw * 2f, ry * 2f), style = Stroke(0.008f * u))
            drawArc(Ink.line, 0f, 180f, false, Offset(-hw, y - ry), Size(hw * 2f, ry * 2f), style = pen.thin)
        }
        drawRect(Color.White.copy(alpha = 0.25f), Offset(-hw * 0.7f, top), Size(hw * 0.18f, bot - top))
    }
    drawPath(body, Ink.line, style = pen.stroke)
}

private fun DrawScope.ridesCanopy(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val cy = CAR_CAN_Y * u
    val rx = CAR_CAN_RX * u
    val ry = CAR_CAN_RY * u
    val apex = Offset(0f, CAR_APEX_Y * u)
    val oval = Rect(-rx, cy - ry, rx, cy + ry)
    fun rim(a: Float) = Offset(cos(a) * rx, cy + sin(a) * ry)

    // Roof wedges, back to front.
    val n = 12
    val step = RIDES_TAU / n
    val order = (0 until n).sortedBy { sin(f.angle + (it + 0.5f) * step) }
    for (k in order) {
        val a0 = f.angle + k * step
        val mid = a0 + step / 2f
        val p = Path()
        p.moveTo(apex.x, apex.y)
        for (j in 0..4) {
            val q = rim(a0 + step * j / 4f)
            p.lineTo(q.x, q.y)
        }
        p.close()
        var c = if (k % 2 == 0) Color(0xFFF2545B) else Color(0xFFFFF3DC)
        if (cos(mid) > 0.35f) c = c.shadow() else if (cos(mid) < -0.55f) c = c.lighten(0.08f)
        drawPath(p, c)
        drawPath(p, Ink.line.copy(alpha = 0.7f), style = pen.thin)
    }
    // Outline of the roof's silhouette.
    val d = CAR_CAN_Y - CAR_APEX_Y
    val st = -CAR_CAN_RY / d
    val ct = sqrt(1f - st * st)
    val aR = atan2(st, ct)
    var aL = atan2(st, -ct)
    if (aL < aR) aL += RIDES_TAU
    val sil = Path().apply {
        moveTo(apex.x, apex.y)
        val pr = rim(aR)
        lineTo(pr.x, pr.y)
        arcTo(oval, ridesDeg(aR), ridesDeg(aL - aR), false)
        close()
    }
    drawPath(sil, Ink.line, style = pen.stroke)
    shine(Offset(-rx * 0.35f, cy - ry * 0.35f), rx * 0.18f, ry * 0.35f, 0.35f)

    // The scalloped valance hanging from the front half of the rim.
    val h = CAR_VAL_H * u
    val m = 24
    val vs = RIDES_TAU / m
    val base = ridesWrap(f.angle, vs)
    val first = floor(f.angle / vs).toInt()
    for (j in -1..m / 2 + 1) {
        val a0 = base + j * vs
        val a1 = a0 + vs
        if (a1 <= 0f || a0 >= RIDES_PI) continue
        val c0 = max(a0, 0f)
        val c1 = min(a1, RIDES_PI)
        val p0 = rim(c0)
        val p1 = rim(c1)
        val pm = rim((c0 + c1) / 2f)
        val depth = 0.028f * u * ((c1 - c0) / vs)
        val p = Path().apply {
            moveTo(p0.x, p0.y)
            arcTo(oval, ridesDeg(c0), ridesDeg(c1 - c0), false)
            lineTo(p1.x, p1.y + h)
            quadraticTo(pm.x, pm.y + h + depth, p0.x, p0.y + h)
            close()
        }
        val teal = (j + first).mod(2) == 0
        val c = if (teal) ridesTeal else ridesGold
        drawPath(p, if (cos((c0 + c1) / 2f) > 0.45f) c.shadow() else c)
        drawPath(p, Ink.line, style = pen.thin)
    }
    // A gold rim tube and bulbs along it.
    drawArc(Ink.line, 0f, 180f, false, oval.topLeft, oval.size, style = Stroke(0.009f * u + pen.lw * 2f))
    drawArc(Color(0xFFF6C453), 0f, 180f, false, oval.topLeft, oval.size, style = Stroke(0.009f * u))
    for (j in -1..m / 2 + 1) {
        val a = base + j * vs
        if (a <= 0.12f || a >= RIDES_PI - 0.12f) continue
        val lit = if (f.on) (if ((j + first).mod(3) == (t * 5f).toInt().mod(3)) 1f else 0.4f) else 0.6f + 0.2f * sin(t * 1.9f + j)
        ridesBulb(rim(a), 0.0072f * u, Color(RIDES_BULB[(j + first).mod(3)]), lit, pen)
    }
    // Cupola, finial and a little flag.
    val cup = Rect(-0.034f * u, apex.y - 0.004f * u, 0.034f * u, apex.y + 0.012f * u)
    inkedOval(cup, Color(0xFFF6C453), pen)
    val poleTop = Offset(0f, apex.y - 0.05f * u)
    inkLine(Offset(0f, apex.y), poleTop, pen, pen.lw * 1.6f)
    val wave = sin(t * 4f) * 0.006f * u
    val flag = Path().apply {
        moveTo(poleTop.x, poleTop.y)
        quadraticTo(0.03f * u, poleTop.y + 0.004f * u + wave, 0.055f * u, poleTop.y + 0.012f * u - wave)
        quadraticTo(0.03f * u, poleTop.y + 0.02f * u + wave, 0f, poleTop.y + 0.026f * u)
        close()
    }
    inked(flag, ridesPink, pen)
    inkedCircle(poleTop, 0.009f * u, ridesGold, pen)
}

/** A carousel horse with its saddle at ([x], [y]) pixels, facing right when [face] > 0. */
private fun DrawScope.ridesHorse(x: Float, y: Float, face: Float, i: Int, u: Float, pen: Pen, headOnly: Boolean) {
    val body = Color(RIDES_HORSE_BODY[i % 3])
    val mane = Color(RIDES_HORSE_MANE[i % 3])
    val saddle = Color(RIDES_HORSE_SADDLE[i % 3])
    val gold = Color(0xFFF6C453)
    val k = u * RIDES_HORSE_K
    withTransform({
        translate(x, y)
        scale(face, 1f, pivot = Offset.Zero)
    }) {
        fun o(a: Float, b: Float) = Offset(a * k, b * k)
        val legW = 0.015f * k
        fun leg(a: Offset, b: Offset, c: Offset, color: Color) {
            capsule(a, b, legW, color, pen)
            capsule(b, c, legW * 0.9f, color, pen)
            inkedOval(Rect(c.x - legW * 0.75f, c.y - legW * 0.35f, c.x + legW * 0.75f, c.y + legW * 0.55f), gold, pen, shade = false)
        }
        if (!headOnly) {
            leg(o(0.04f, 0.035f), o(0.066f, 0.06f), o(0.056f, 0.084f), body.darken(0.2f))
            leg(o(-0.045f, 0.036f), o(-0.074f, 0.058f), o(-0.096f, 0.074f), body.darken(0.2f))
            val tail = blobPath(
                -0.058f * k, 0.006f * k, -0.09f * k, -0.004f * k, -0.118f * k, 0.018f * k, -0.124f * k, 0.058f * k,
                -0.106f * k, 0.078f * k, -0.098f * k, 0.05f * k, -0.084f * k, 0.03f * k, -0.064f * k, 0.028f * k,
            )
            inked(tail, mane, pen)
            leg(o(0.05f, 0.04f), o(0.082f, 0.048f), o(0.104f, 0.062f), body)
            leg(o(-0.05f, 0.044f), o(-0.058f, 0.074f), o(-0.042f, 0.09f), body)
            inkedOval(Rect(-0.074f * k, -0.002f * k, 0.06f * k, 0.058f * k), body, pen)
            // Blanket and saddle.
            val blanket = Path().apply {
                moveTo(-0.036f * k, 0.0f)
                lineTo(0.03f * k, 0.0f)
                lineTo(0.034f * k, 0.03f * k)
                quadraticTo(0.017f * k, 0.042f * k, 0f, 0.032f * k)
                quadraticTo(-0.02f * k, 0.042f * k, -0.04f * k, 0.03f * k)
                close()
            }
            inked(blanket, saddle, pen)
            for (k in 0 until 4) drawCircle(gold, 0.0035f * k, o(-0.03f + k * 0.019f, 0.028f))
            val seat = Path().apply {
                moveTo(-0.034f * k, -0.012f * k)
                quadraticTo(-0.02f * k, 0.004f * k, 0.0f, 0.002f * k)
                quadraticTo(0.02f * k, 0.0f, 0.028f * k, -0.01f * k)
                lineTo(0.032f * k, 0.004f * k)
                quadraticTo(0f, 0.014f * k, -0.034f * k, 0.006f * k)
                close()
            }
            inked(seat, Color(0xFF8A4B2A), pen)
            drawLine(Ink.line, o(0.002f, 0.012f), o(0.006f, 0.05f), strokeWidth = pen.lw * 0.6f)
            inkedOval(Rect(-0.002f * k, 0.046f * k, 0.014f * k, 0.056f * k), gold, pen, shade = false)
            // Reins from the bit back to the saddle.
            val rein = Path().apply {
                moveTo(0.1f * k, -0.024f * k)
                quadraticTo(0.06f * k, 0.004f * k, 0.028f * k, -0.004f * k)
            }
            drawPath(rein, Ink.line, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
        }
        // Neck and head, the part that stays in front of a rider.
        val head = blobPath(
            0.022f * k, 0.022f * k, 0.028f * k, -0.012f * k, 0.044f * k, -0.046f * k, 0.062f * k, -0.071f * k,
            0.082f * k, -0.074f * k, 0.1f * k, -0.058f * k, 0.12f * k, -0.032f * k, 0.114f * k, -0.016f * k,
            0.092f * k, -0.018f * k, 0.073f * k, -0.028f * k, 0.066f * k, -0.004f * k, 0.062f * k, 0.03f * k,
            0.046f * k, 0.046f * k,
        )
        inked(head, body, pen)
        val ear = ridesPoly(0.064f * k, -0.066f * k, 0.068f * k, -0.094f * k, 0.082f * k, -0.07f * k)
        inked(ear, body, pen)
        drawLine(Ink.blush, o(0.069f, -0.07f), o(0.071f, -0.084f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        val maneP = blobPath(
            0.018f * k, 0.012f * k, 0.022f * k, -0.02f * k, 0.036f * k, -0.05f * k, 0.052f * k, -0.074f * k,
            0.074f * k, -0.086f * k, 0.072f * k, -0.07f * k, 0.058f * k, -0.058f * k, 0.046f * k, -0.034f * k,
            0.036f * k, -0.004f * k, 0.034f * k, 0.02f * k,
        )
        inked(maneP, mane, pen)
        // Bridle, eye and a smile.
        drawLine(saddle, o(0.074f, -0.062f), o(0.1f, -0.03f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        drawLine(saddle, o(0.098f, -0.042f), o(0.112f, -0.024f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        drawCircle(gold, 0.004f * k, o(0.1f, -0.03f))
        drawCircle(Ink.line, 0.0062f * k, o(0.086f, -0.05f))
        drawCircle(Color.White, 0.0022f * k, o(0.0845f, -0.0525f))
        drawLine(Ink.line, o(0.08f, -0.057f), o(0.076f, -0.061f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
        drawCircle(Ink.line, 0.0024f * k, o(0.112f, -0.027f))
        drawArc(Ink.line, 20f, 120f, false, o(0.094f, -0.03f), Size(0.016f * k, 0.01f * k), style = Stroke(pen.lw * 0.6f, cap = StrokeCap.Round))
        drawCircle(Ink.blush, 0.006f * k, o(0.094f, -0.035f))
        drawCircle(gold, 0.0045f * k, o(0.054f, 0.018f))
    }
}

// ====================================================================================== TRAMPOLINE

private fun DrawScope.ridesTrampoline(f: Fixture, u: Float, pen: Pen) {
    val d = 0.19f
    // Pad, spring gap and mat sizes: x in pixels, depth in units.
    val pw = 0.026f * u
    val pd = 0.026f
    val sg = 0.02f * u
    val sd = 0.02f
    val sag = 0.004f * u
    // Placed so the mat's front edge is the surface at -0.08, centred on the origin.
    val md0 = pd + sd
    val x0 = -0.2f * u - Oblique.DX * md0 * u
    val x1 = 0.2f * u - Oblique.DX * md0 * u
    val y0 = -0.08f * u - sag - Oblique.DY * md0 * u
    val pad = ridesTeal
    val padLight = Color(0xFF7FE0D4)
    val steel = Color(0xFF5C6680)
    fun p(x: Float, depth: Float, drop: Float = 0f) = Offset(x + Oblique.DX * depth * u, y0 + Oblique.DY * depth * u + drop)
    groundShadow(0.03f * u, -0.02f * u, 0.52f * u, 0.8f)

    // U-shaped legs, far pair first.
    val th = 0.02f * u
    for (back in intArrayOf(1, 0)) for (s in intArrayOf(-1, 1)) {
        val shift = Oblique.offset(if (back == 1) d * 0.84f else 0.03f, u)
        val cx = (x0 + x1) / 2f + s * 0.145f * u + shift.x
        val top = y0 + th + shift.y
        val bot = shift.y
        val w = 0.036f * u
        val leg = Path().apply {
            moveTo(cx - w, top)
            lineTo(cx - w, bot - 0.014f * u)
            quadraticTo(cx - w, bot, cx - w + 0.014f * u, bot)
            lineTo(cx + w - 0.014f * u, bot)
            quadraticTo(cx + w, bot, cx + w, bot - 0.014f * u)
            lineTo(cx + w, top)
        }
        ridesTube(leg, 0.009f * u, if (back == 1) steel.darken(0.3f) else steel, pen)
    }

    // The padded frame: right end, front band and top, in segments of two blues.
    val side = ridesQuad(p(x1, 0f), p(x1, d), p(x1, d, th), p(x1, 0f, th))
    ridesFlat(side, pad.darken(0.32f), pen)
    val segs = 9
    for (k in 0 until segs) {
        val a = x0 + (x1 - x0) * k / segs
        val b = x0 + (x1 - x0) * (k + 1) / segs
        val c = if (k % 2 == 0) pad else padLight
        drawPath(ridesPoly(a, y0, b, y0, b, y0 + th, a, y0 + th), c.darken(0.14f))
        drawPath(ridesQuad(p(a, 0f), p(b, 0f), p(b, pd), p(a, pd)), c)
        drawPath(ridesQuad(p(a, d - pd), p(b, d - pd), p(b, d), p(a, d)), c.darken(0.06f))
    }
    for (k in 0 until 4) {
        val c = if (k % 2 == 0) padLight else pad
        val da = pd + (d - 2f * pd) * k / 4f
        val db = pd + (d - 2f * pd) * (k + 1) / 4f
        drawPath(ridesQuad(p(x0, da), p(x0 + pw, da), p(x0 + pw, db), p(x0, db)), c)
        drawPath(ridesQuad(p(x1 - pw, da), p(x1, da), p(x1, db), p(x1 - pw, db)), c.darken(0.06f))
    }
    drawPath(ridesPoly(x0, y0, x1, y0, x1, y0 + th, x0, y0 + th), Ink.line, style = pen.stroke)
    drawPath(ridesQuad(p(x0, 0f), p(x1, 0f), p(x1, d), p(x0, d)), Ink.line, style = pen.stroke)
    drawLine(Color.White.copy(alpha = 0.55f), p(x0 + 0.01f * u, 0.006f), p(x1 - 0.01f * u, 0.006f), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)

    // The spring gap and the springs.
    val gap = ridesQuad(p(x0 + pw, pd), p(x1 - pw, pd), p(x1 - pw, d - pd), p(x0 + pw, d - pd))
    ridesFlat(gap, Color(0xFF232739), pen, thin = true)
    val mx0 = x0 + pw + sg
    val mx1 = x1 - pw - sg
    val md1 = d - pd - sd
    // The mat dips and wobbles after a bounce.
    val dip = f.anim * 0.042f * u * cos((1f - f.anim) * 11f)
    val springC = Color(0xFFE3E8EF)
    for (k in 0..11) {
        val q = k / 11f
        val x = mx0 + (mx1 - mx0) * q
        val bow = 4f * q * (1f - q)
        ridesSpring(p(x, pd), p(x, md0, sag + dip * bow * 0.9f), 0.0045f * u, 3, springC, pen.lw * 0.55f)
        ridesSpring(p(x, d - pd), p(x, md1, sag + dip * bow * 0.45f), 0.004f * u, 3, springC, pen.lw * 0.5f)
    }
    for (k in 1..4) {
        val dd = md0 + (md1 - md0) * k / 5f
        ridesSpring(p(x0 + pw, dd), p(mx0, dd, sag + dip * 0.4f), 0.0035f * u, 2, springC, pen.lw * 0.5f)
        ridesSpring(p(x1 - pw, dd), p(mx1, dd, sag + dip * 0.4f), 0.0035f * u, 2, springC, pen.lw * 0.5f)
    }

    // The mat, with a big star to aim for.
    val fl = p(mx0, md0, sag)
    val fr = p(mx1, md0, sag)
    val br = p(mx1, md1, sag)
    val bl = p(mx0, md1, sag)
    val c = Offset((fl.x + br.x) / 2f, (fl.y + br.y) / 2f + dip)
    val mat = Path().apply {
        moveTo(fl.x, fl.y)
        quadraticTo((fl.x + fr.x) / 2f, fl.y + dip * 1.8f, fr.x, fr.y)
        quadraticTo((fr.x + br.x) / 2f, (fr.y + br.y) / 2f + dip * 0.9f, br.x, br.y)
        quadraticTo((bl.x + br.x) / 2f, bl.y + dip * 0.9f, bl.x, bl.y)
        quadraticTo((bl.x + fl.x) / 2f, (bl.y + fl.y) / 2f + dip * 0.9f, fl.x, fl.y)
        close()
    }
    val matC = Color(0xFF2E3552)
    drawPath(mat, matC)
    clipPath(mat) {
        drawOval(matC.lighten(0.09f), Offset(bl.x - 0.02f * u, bl.y - 0.01f * u), Size((fr.x - fl.x) * 0.75f, (fl.y - bl.y) * 0.95f))
        val ring = Rect(c.x - 0.12f * u, c.y - 0.03f * u, c.x + 0.12f * u, c.y + 0.03f * u)
        drawOval(Color.White.copy(alpha = 0.22f), ring.topLeft, ring.size, style = Stroke(0.006f * u))
        withTransform({ scale(1f, 0.42f, pivot = c) }) {
            drawPath(starPath(c, 0.058f * u, 0.026f * u), ridesGold)
        }
        if (dip > 0f) drawOval(Color.Black.copy(alpha = min(0.3f, dip / u * 7f)), Offset(c.x - 0.13f * u, c.y - 0.028f * u), Size(0.26f * u, 0.056f * u))
    }
    drawPath(mat, Ink.line, style = pen.thin)
    drawLine(Color.White.copy(alpha = 0.3f), Offset(bl.x + 0.03f * u, bl.y + 0.008f * u), Offset(bl.x + 0.15f * u, bl.y + 0.008f * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)

    // A little ladder hooked over the left end.
    val lad = Color(0xFFFFC83D)
    val ra = Offset(x0 - 0.004f * u, y0 + 0.004f * u)
    val rb = Offset(x0 - 0.04f * u, 0f)
    val lo = Oblique.offset(0.075f, u)
    val hook = { off: Offset, color: Color ->
        val path = Path().apply {
            moveTo(rb.x + off.x, rb.y + off.y)
            lineTo(ra.x + off.x, ra.y + off.y)
            quadraticTo(ra.x + off.x + 0.004f * u, ra.y + off.y - 0.016f * u, ra.x + off.x + 0.02f * u, ra.y + off.y - 0.012f * u)
        }
        ridesTube(path, 0.007f * u, color, pen)
    }
    hook(lo, lad.darken(0.22f))
    for (k in 1..3) {
        val q = k / 4f
        val a = ra + (rb - ra) * q
        capsule(a + lo, a, 0.006f * u, lad.darken(0.08f), pen)
    }
    hook(Offset.Zero, lad)
}

// =============================================================================== CANDY FLOSS STAND

private fun DrawScope.ridesFlossStand(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val d = 0.12f
    val o = Oblique.offset(d, u)
    val pink = ridesPink
    val white = Color(0xFFFFF7F0)
    val sky = Color(0xFF7CC8F2)
    val floss = Color(0xFFFFA8D2)
    val left = -0.135f * u
    val right = 0.135f * u
    val top = -0.2f * u
    groundShadow(0.03f * u, -0.01f * u, 0.36f * u, 0.8f)

    // Posts at the back corners, up to the awning.
    val awnY = -0.345f * u
    for (x in floatArrayOf(left + 0.012f * u, right - 0.012f * u)) {
        val b = Offset(x, top) + o * 0.85f
        ridesCandyPole(b, Offset(b.x, awnY + o.y * 0.85f), 0.011f * u, white, pink, pen)
    }

    // The counter: candy stripes, a sky-blue lip and a badge.
    val frontR = Rect(left, top, right, 0f)
    box3d(frontR, d * u, white, pen, top = Color.White, side = pink.darken(0.28f))
    val frontP = Path().apply { addRect(frontR) }
    ridesStripes(frontP, left, top, right, 0f, 0.036f * u, 0.018f * u, pink)
    clipPath(frontP) { drawRect(Ink.line.copy(alpha = 0.12f), Offset(left + (right - left) * 0.86f, top), Size((right - left) * 0.14f, -top)) }
    inked(ridesPoly(left, -0.022f * u, right, -0.022f * u, right, 0f, left, 0f), pink.darken(0.3f), pen)
    drawRect(Ink.line, frontR.topLeft, frontR.size, style = pen.stroke)
    inkedRound(Rect(left - 0.004f * u, top, right + 0.004f * u, top + 0.016f * u), 0.004f * u, sky, pen)
    val badge = Offset(0f, -0.1f * u)
    inkedCircle(badge, 0.046f * u, white, pen)
    drawCircle(sky, 0.039f * u, badge, style = Stroke(0.006f * u))
    drawLine(Color(0xFFF2E3C4), badge + Offset(0.004f * u, 0.03f * u), badge + Offset(0f, -0.004f * u), strokeWidth = 0.006f * u, cap = StrokeCap.Round)
    val icon = blobPath(
        -0.026f * u, -0.004f * u, -0.022f * u, -0.024f * u, -0.004f * u, -0.032f * u, 0.016f * u, -0.028f * u,
        0.026f * u, -0.012f * u, 0.014f * u, 0.004f * u, -0.01f * u, 0.004f * u,
    )
    translate(badge.x, badge.y) { inked(icon, floss, pen) }

    // A jar of sticks at the back left.
    val jar = Offset(right - 0.05f * u, top) + Oblique.offset(0.08f, u)
    for (k in 0 until 5) {
        val a = -0.35f + k * 0.17f
        val tip = jar + Offset(sin(a) * 0.05f * u, -0.075f * u)
        drawLine(Ink.line, jar + Offset(0f, -0.01f * u), tip, strokeWidth = pen.lw * 1.9f, cap = StrokeCap.Round)
        drawLine(Color(0xFFF2E3C4), jar + Offset(0f, -0.01f * u), tip, strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    }
    val jarR = Rect(jar.x - 0.017f * u, jar.y - 0.042f * u, jar.x + 0.017f * u, jar.y)
    drawRoundRect(Color(0x66CFEFFF), jarR.topLeft, jarR.size, CornerRadius(0.006f * u))
    drawRoundRect(Ink.line, jarR.topLeft, jarR.size, CornerRadius(0.006f * u), style = pen.thin)
    drawLine(Color.White.copy(alpha = 0.8f), Offset(jarR.left + 0.006f * u, jarR.top + 0.008f * u), Offset(jarR.left + 0.006f * u, jarR.bottom - 0.01f * u), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)

    // The floss machine: a blue base, a steel bowl and a cloud spinning inside.
    val mc = Offset(-0.035f * u, top) + Oblique.offset(0.06f, u)
    box3d(Rect(mc.x - 0.05f * u, mc.y - 0.028f * u, mc.x + 0.05f * u, mc.y), 0.03f * u, sky, pen)
    inkedCircle(Offset(mc.x - 0.028f * u, mc.y - 0.014f * u), 0.006f * u, ridesCoral, pen, shade = false)
    drawRoundRect(Color.White.copy(alpha = 0.8f), Offset(mc.x - 0.01f * u, mc.y - 0.02f * u), Size(0.04f * u, 0.009f * u), CornerRadius(0.004f * u))
    val rimC = Offset(mc.x + 0.008f * u, mc.y - 0.06f * u)
    val brx = 0.07f * u
    val bry = 0.018f * u
    val rimOval = Rect(rimC.x - brx, rimC.y - bry, rimC.x + brx, rimC.y + bry)
    drawOval(Color(0xFF8C98AC), rimOval.topLeft, rimOval.size)
    drawOval(Ink.line, rimOval.topLeft, rimOval.size, style = pen.thin)
    // The cloud, its lumps rolling round.
    val pts = FloatArray(20)
    for (k in 0 until 10) {
        val a = k * RIDES_TAU / 10f
        val rr = 1f + 0.12f * sin(k * 2.3f + t * 3f)
        pts[k * 2] = rimC.x + cos(a) * 0.064f * u * rr
        pts[k * 2 + 1] = rimC.y - 0.026f * u + sin(a) * 0.036f * u * rr
    }
    inked(blobPath(*pts), floss, pen)
    for (k in 0 until 3) {
        val a = t * 2.4f + k * 2.1f
        val sw = Rect(rimC.x - 0.045f * u + k * 0.006f * u, rimC.y - 0.052f * u + k * 0.01f * u, rimC.x + 0.04f * u, rimC.y - 0.006f * u)
        drawArc(Color.White.copy(alpha = 0.7f), ridesDeg(a), 110f, false, sw.topLeft, sw.size, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round))
    }
    twinkle(Offset(rimC.x + 0.03f * u, rimC.y - 0.05f * u), 0.009f * u, Color.White, 0.5f + 0.5f * sin(t * 5f))
    val bowl = Path().apply {
        moveTo(rimC.x - brx, rimC.y)
        cubicTo(rimC.x - brx, rimC.y + 0.03f * u, rimC.x - brx * 0.5f, rimC.y + 0.034f * u, rimC.x, rimC.y + 0.034f * u)
        cubicTo(rimC.x + brx * 0.5f, rimC.y + 0.034f * u, rimC.x + brx, rimC.y + 0.03f * u, rimC.x + brx, rimC.y)
        arcTo(rimOval, 0f, 180f, false)
        close()
    }
    inked(bowl, Color(0xFFDCE3EC), pen)
    drawArc(Color.White, 20f, 140f, false, rimOval.topLeft, rimOval.size, style = Stroke(pen.lw * 0.8f))
    shine(Offset(rimC.x - brx * 0.55f, rimC.y + 0.014f * u), 0.014f * u, 0.008f * u, 0.8f)
    // A wisp escapes and floats off.
    val ph = ridesWrap(t * 0.2f, 1f)
    val fade = min(1f, (1f - ph) * 3f) * min(1f, ph * 10f)
    if (fade > 0.02f) {
        val wc = Offset(rimC.x + 0.02f * u + ph * 0.07f * u + sin(ph * 7f) * 0.012f * u, rimC.y - 0.07f * u - ph * 0.2f * u)
        val wr = 0.013f * u * (1f - ph * 0.4f)
        drawCircle(floss.copy(alpha = fade), wr, wc)
        drawCircle(floss.copy(alpha = fade), wr * 0.75f, wc + Offset(wr * 0.9f, wr * 0.2f))
        drawCircle(Ink.line.copy(alpha = fade * 0.8f), wr, wc, style = pen.thin)
    }

    // Awning and a cloud sign on top.
    ridesAwning(-0.158f * u, 0.158f * u, awnY, d * u, 0.03f * u, 8, pink, white, 0.03f * u, pen)
    val sc = Offset(0.035f * u, -0.448f * u)
    for (s in intArrayOf(-1, 1)) inkLine(Offset(sc.x + s * 0.04f * u, sc.y + 0.02f * u), Offset(sc.x + s * 0.045f * u, awnY + o.y - 0.028f * u), pen, pen.lw * 1.5f)
    val signPts = floatArrayOf(
        -0.085f, 0.005f, -0.08f, -0.03f, -0.045f, -0.045f, -0.01f, -0.06f, 0.035f, -0.05f,
        0.07f, -0.035f, 0.088f, -0.005f, 0.075f, 0.025f, 0.03f, 0.03f, -0.02f, 0.03f, -0.07f, 0.03f,
    )
    for (k in signPts.indices) signPts[k] = signPts[k] * u + (if (k % 2 == 0) sc.x else sc.y)
    val sign = blobPath(*signPts)
    inked(sign, Color(0xFFBFE6FF), pen)
    translate(sc.x, sc.y - 0.012f * u) {
        drawLine(Ink.line, Offset(0.002f * u, 0.028f * u), Offset(0f, 0.004f * u), strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
        drawLine(Color(0xFFF2E3C4), Offset(0.002f * u, 0.028f * u), Offset(0f, 0.004f * u), strokeWidth = pen.lw * 1.1f, cap = StrokeCap.Round)
        inked(icon, floss, pen)
    }
    for (k in 0 until 7) {
        val a = RIDES_PI * (0.95f + k * 0.18f)
        val bc = sc + Offset(cos(a) * 0.082f * u, sin(a) * 0.05f * u - 0.012f * u)
        ridesBulb(bc, 0.0055f * u, Color(RIDES_BULB[k % 3]), 0.55f + 0.45f * sin(t * 3f + k * 1.4f), pen)
    }
}

// ==================================================================================== POPCORN CART

private fun DrawScope.ridesPopcornCart(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val red = Color(0xFFE8304A)
    val cream = Color(0xFFFFF4DE)
    val puff = Color(0xFFFFF3D6)
    val d = 0.09f
    val o = Oblique.offset(d, u)
    groundShadow(0.02f * u, -0.008f * u, 0.3f * u, 0.8f)

    // Far wheel, then the stand on the right.
    val wc = Offset(-0.062f * u, -0.058f * u)
    ridesWheel(wc + o, 0.058f * u, 0.3f, ridesRubber.darken(0.2f), ridesGold.darken(0.3f), 8, pen)
    capsule(Offset(0.08f * u, -0.08f * u), Offset(0.086f * u, -0.006f * u), 0.012f * u, ridesSteelDark, pen)
    inkedRound(Rect(0.07f * u, -0.01f * u, 0.104f * u, 0f), 0.004f * u, ridesRubber, pen, shade = false)

    // The cart body.
    val body = Rect(-0.105f * u, -0.19f * u, 0.105f * u, -0.075f * u)
    box3d(body, d * u, red, pen, top = red.lighten(0.25f), side = red.darken(0.3f))
    drawLine(ridesGold, Offset(body.left + pen.lw, body.top + 0.014f * u), Offset(body.right - pen.lw, body.top + 0.014f * u), strokeWidth = 0.006f * u)
    drawLine(ridesGold, Offset(body.left + pen.lw, body.bottom - 0.012f * u), Offset(body.right - pen.lw, body.bottom - 0.012f * u), strokeWidth = 0.004f * u)
    val plate = Rect(-0.05f * u, -0.165f * u, 0.05f * u, -0.098f * u)
    inkedRound(plate, 0.012f * u, cream, pen, shade = false)
    // A striped popcorn box on the plate.
    val bx = Path().apply {
        moveTo(-0.02f * u, -0.148f * u)
        lineTo(0.02f * u, -0.148f * u)
        lineTo(0.014f * u, -0.106f * u)
        lineTo(-0.014f * u, -0.106f * u)
        close()
    }
    for (k in 0 until 3) inkedCircle(Offset((-0.012f + k * 0.012f) * u, -0.152f * u - (k % 2) * 0.004f * u), 0.009f * u, puff, pen, shade = false)
    drawPath(bx, Color.White)
    ridesStripes(bx, -0.02f * u, -0.148f * u, 0.02f * u, -0.106f * u, 0.012f * u, 0.006f * u, red)
    drawPath(bx, Ink.line, style = pen.thin)
    for (s in intArrayOf(-1, 1)) drawCircle(ridesGold, 0.005f * u, Offset(s * 0.08f * u, -0.13f * u))

    // Push handle.
    val h0 = Offset(0.105f * u, -0.16f * u)
    val h1 = Offset(0.155f * u, -0.205f * u)
    capsule(h0, h1, 0.008f * u, ridesSteelDark, pen)
    capsule(h0 + Offset(0f, 0.03f * u), h1, 0.008f * u, ridesSteelDark, pen)
    capsule(h1, h1 + Oblique.offset(0.07f, u), 0.013f * u, ridesRubber, pen)

    // Near wheel.
    ridesWheel(wc, 0.058f * u, 0.1f, ridesRubber, ridesGold, 8, pen)

    // The glass cabinet with popcorn popping inside.
    val cab = Rect(-0.09f * u, -0.335f * u, 0.09f * u, -0.19f * u)
    val co = Oblique.offset(0.075f, u)
    val inside = Path().apply { addRect(cab) }
    val glowN = pen.night
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE9B0), Color(0xFFFFD27A)), cab.top, cab.bottom), cab.topLeft, cab.size)
    clipPath(inside) {
        // The far wall's red posts, seen through the glass.
        drawLine(red.darken(0.2f), Offset(cab.left + co.x, cab.top), Offset(cab.left + co.x, cab.bottom), strokeWidth = 0.006f * u)
        // The kettle, rocking a little as it stirs.
        val kc = Offset(0.01f * u, -0.296f * u)
        drawLine(ridesSteelDark, Offset(kc.x, cab.top), kc, strokeWidth = pen.lw * 1.2f)
        rotate(sin(t * 3f) * 8f - 12f, kc) {
            val kettle = Path().apply {
                moveTo(kc.x - 0.032f * u, kc.y - 0.004f * u)
                lineTo(kc.x + 0.032f * u, kc.y - 0.004f * u)
                quadraticTo(kc.x + 0.03f * u, kc.y + 0.032f * u, kc.x, kc.y + 0.034f * u)
                quadraticTo(kc.x - 0.03f * u, kc.y + 0.032f * u, kc.x - 0.032f * u, kc.y - 0.004f * u)
                close()
            }
            for (k in 0 until 4) inkedCircle(Offset(kc.x + (-0.022f + k * 0.015f) * u, kc.y - 0.008f * u - (k % 2) * 0.005f * u), 0.009f * u, puff, pen, shade = false)
            inked(kettle, Color(0xFFD5DAE6), pen)
            drawLine(ridesSteelDark, Offset(kc.x - 0.036f * u, kc.y - 0.004f * u), Offset(kc.x + 0.036f * u, kc.y - 0.004f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
            shine(Offset(kc.x - 0.015f * u, kc.y + 0.01f * u), 0.008f * u, 0.012f * u, 0.7f)
        }
        // The heap.
        for (k in 0 until 16) {
            val row = k / 6
            val x = cab.left + (0.012f + (k % 6) * 0.032f + row * 0.016f) * u
            val y = cab.bottom - (0.01f + row * 0.016f) * u - ridesHash(k) * 0.006f * u
            drawCircle(puff.darken(0.08f), 0.013f * u, Offset(x + 0.002f * u, y + 0.002f * u))
            drawCircle(puff, 0.012f * u, Offset(x, y))
            drawCircle(Ink.line, 0.012f * u, Offset(x, y), style = pen.thin)
            drawCircle(Color(0xFFFFC857), 0.004f * u, Offset(x + 0.003f * u, y + 0.003f * u))
        }
        // Kernels flying.
        for (k in 0 until 6) {
            val ph = ridesWrap(t * 0.9f + k * 0.173f, 1f)
            val sx = 0.01f * u + (ridesHash(k * 3) - 0.5f) * 0.03f * u
            val vx = (ridesHash(k * 3 + 1) - 0.5f) * 0.14f * u
            val x = sx + vx * ph
            val y = -0.29f * u - sin(ph * RIDES_PI) * 0.03f * u + ph * ph * 0.08f * u
            drawCircle(puff, 0.0065f * u, Offset(x, y))
            drawCircle(Ink.line, 0.0065f * u, Offset(x, y), style = pen.thin)
        }
        if (glowN > 0f) drawRect(Color(0xFFFFE08A).copy(alpha = 0.25f * glowN), cab.topLeft, cab.size)
    }
    // Glass: side pane, reflections and the frame.
    val sidePane = ridesQuad(Offset(cab.right, cab.top), Offset(cab.right, cab.top) + co, Offset(cab.right, cab.bottom) + co, Offset(cab.right, cab.bottom))
    drawPath(sidePane, Color(0x66FFE9B0))
    drawPath(sidePane, Ink.line, style = pen.thin)
    clipPath(inside) {
        drawPath(ridesPoly(cab.left + 0.02f * u, cab.bottom, cab.left + 0.05f * u, cab.bottom, cab.left + 0.1f * u, cab.top, cab.left + 0.07f * u, cab.top), Color.White.copy(alpha = 0.22f))
        drawPath(ridesPoly(cab.left + 0.1f * u, cab.bottom, cab.left + 0.11f * u, cab.bottom, cab.left + 0.16f * u, cab.top, cab.left + 0.15f * u, cab.top), Color.White.copy(alpha = 0.18f))
    }
    for (x in floatArrayOf(cab.left, cab.right)) capsule(Offset(x, cab.top), Offset(x, cab.bottom), 0.008f * u, red, pen)
    capsule(Offset(cab.right, cab.top) + co, Offset(cab.right, cab.bottom) + co, 0.006f * u, red.darken(0.25f), pen)
    capsule(Offset(cab.left, cab.bottom), Offset(cab.right, cab.bottom), 0.008f * u, ridesGold, pen)

    // Roof: a striped hood with scallops, bulbs and a little flag.
    val ry = cab.top - 0.004f * u
    ridesAwning(-0.115f * u, 0.115f * u, ry, 0.1f * u, 0.045f * u, 6, red, Color.White, 0.024f * u, pen)
    val ridge = Offset(0f, ry) + Oblique.offset(0.05f, u) + Offset(0f, -0.03f * u)
    for (k in 0 until 5) {
        val bx2 = -0.092f * u + k * 0.046f * u
        val lit = if (ridesWrap(t * 2f + k * 0.4f, 2f) < 1f) 1f else 0.5f
        ridesBulb(Offset(bx2, ry + 0.02f * u), 0.0055f * u, Color(RIDES_BULB[k % 3]), lit, pen)
    }
    val flagTop = ridge + Offset(0f, -0.055f * u)
    inkLine(ridge, flagTop, pen, pen.lw * 1.5f)
    val wave = sin(t * 4.5f) * 0.005f * u
    val flag = Path().apply {
        moveTo(flagTop.x, flagTop.y)
        quadraticTo(flagTop.x + 0.02f * u, flagTop.y + 0.006f * u + wave, flagTop.x + 0.04f * u, flagTop.y + 0.01f * u - wave)
        lineTo(flagTop.x, flagTop.y + 0.022f * u)
        close()
    }
    inked(flag, ridesGold, pen)
    // Every few seconds a kernel escapes through the roof and drops back in.
    val esc = ridesWrap(t * 0.3f, 1f)
    if (esc < 0.35f) {
        val q = esc / 0.35f
        val kp = ridge + Offset(q * 0.05f * u, -sin(q * RIDES_PI) * 0.08f * u + 0.006f * u)
        rotate(q * 540f, kp) {
            drawCircle(puff, 0.008f * u, kp)
            drawCircle(Ink.line, 0.008f * u, kp, style = pen.thin)
            drawCircle(Color(0xFFFFC857), 0.003f * u, kp + Offset(0.002f * u, 0.002f * u))
        }
    }
}

// ======================================================================================== CAN TOSS

private val RIDES_CAN = longArrayOf(0xFFFF5A5F, 0xFF3D8BFF, 0xFF6BCB77, 0xFFFFC83D, 0xFFFF8FC0, 0xFFFF9F43)

/** A tin can centred on [c], turned [rot] degrees. */
private fun DrawScope.ridesCan(c: Offset, w: Float, h: Float, label: Color, rot: Float, pen: Pen) {
    rotate(rot, c) {
        val body = Rect(c.x - w / 2f, c.y - h / 2f, c.x + w / 2f, c.y + h / 2f)
        val corner = CornerRadius(w * 0.14f)
        drawRoundRect(Color(0xFFBCC4D2), body.topLeft, body.size, corner)
        val lab = Rect(body.left, body.top + h * 0.2f, body.right, body.bottom - h * 0.16f)
        drawRect(label, lab.topLeft, lab.size)
        drawRect(label.darken(0.22f), Offset(lab.right - w * 0.24f, lab.top), Size(w * 0.24f, lab.height))
        val star = starPath(Offset(c.x - w * 0.05f, lab.center.y), w * 0.2f, w * 0.09f)
        drawPath(star, Color.White)
        for (y in floatArrayOf(body.top + h * 0.1f, body.bottom - h * 0.08f)) drawLine(Color(0xFF8A94A8), Offset(body.left, y), Offset(body.right, y), strokeWidth = pen.lw * 0.5f)
        drawRoundRect(Ink.line, body.topLeft, body.size, corner, style = pen.stroke)
        val lid = Rect(body.left, body.top - w * 0.14f, body.right, body.top + w * 0.14f)
        drawOval(Color(0xFFE3E8EF), lid.topLeft, lid.size)
        drawOval(Ink.line, lid.topLeft, lid.size, style = pen.thin)
        drawOval(Color(0xFFBCC4D2), Offset(lid.left + w * 0.14f, lid.top + w * 0.06f), Size(w * 0.72f, w * 0.16f))
        drawLine(Color.White.copy(alpha = 0.7f), Offset(body.left + w * 0.18f, lab.top + h * 0.06f), Offset(body.left + w * 0.18f, lab.bottom - h * 0.06f), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
    }
}

/** A little prize teddy hanging from [top], optionally upside down by one foot. */
private fun DrawScope.ridesPrizeTeddy(top: Offset, s: Float, fur: Color, upside: Boolean, sway: Float, pen: Pen) {
    val pivot = top
    rotate(sway, pivot) {
        val c = Offset(top.x, top.y + s * 0.55f)
        drawLine(Ink.line, top, Offset(top.x, top.y + s * 0.1f), strokeWidth = pen.lw * 0.7f)
        withTransform({ if (upside) rotate(180f, c) }) {
            val light = fur.lighten(0.45f)
            val head = Offset(c.x, c.y - s * 0.18f)
            for (sd in intArrayOf(-1, 1)) inkedCircle(Offset(c.x + sd * s * 0.12f, c.y + s * 0.34f), s * 0.09f, fur, pen)
            inkedOval(Rect(c.x - s * 0.19f, c.y - s * 0.02f, c.x + s * 0.19f, c.y + s * 0.34f), fur, pen)
            drawOval(light, Offset(c.x - s * 0.1f, c.y + s * 0.08f), Size(s * 0.2f, s * 0.18f))
            for (sd in intArrayOf(-1, 1)) inkedOval(Rect(c.x + sd * s * 0.2f - s * 0.07f, c.y + s * 0.02f, c.x + sd * s * 0.2f + s * 0.07f, c.y + s * 0.2f), fur, pen)
            for (sd in intArrayOf(-1, 1)) inkedCircle(Offset(head.x + sd * s * 0.14f, head.y - s * 0.14f), s * 0.07f, fur, pen)
            inkedCircle(head, s * 0.2f, fur, pen)
            inkedOval(Rect(head.x - s * 0.09f, head.y + s * 0.01f, head.x + s * 0.09f, head.y + s * 0.12f), light, pen, shade = false)
            drawCircle(Ink.line, s * 0.03f, Offset(head.x, head.y + s * 0.045f))
            for (sd in intArrayOf(-1, 1)) {
                drawCircle(Ink.line, s * 0.028f, Offset(head.x + sd * s * 0.075f, head.y - s * 0.04f))
                drawCircle(Ink.blush, s * 0.035f, Offset(head.x + sd * s * 0.13f, head.y + s * 0.04f))
            }
        }
    }
}

private fun DrawScope.ridesCanToss(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val d = 0.14f
    val o = Oblique.offset(d, u)
    val left = -0.175f * u
    val right = 0.175f * u
    val top = -0.2f * u
    val red = Color(0xFFE8304A)
    val navy = ridesNavy
    val cloth = Color(0xFFFFD35C)
    groundShadow(0.03f * u, -0.01f * u, 0.44f * u, 0.8f)

    // The booth: back wall, the left wall's inside and the right wall's outside.
    val wallTop = -0.46f * u
    val back = Rect(left + o.x, wallTop + o.y, right + o.x, top + o.y)
    val backP = Path().apply { addRect(back) }
    drawRect(Color(0xFFFFF1CF), back.topLeft, back.size)
    ridesStripes(backP, back.left, back.top, back.right, back.bottom, 0.044f * u, 0.022f * u, cloth)
    drawRect(Ink.line.copy(alpha = 0.12f), back.topLeft, Size(back.width, 0.03f * u))
    drawRect(Ink.line, back.topLeft, back.size, style = pen.thin)
    val lwall = ridesPoly(left, wallTop, left + o.x, wallTop + o.y, left + o.x, top + o.y, left, top)
    ridesFlat(lwall, Color(0xFFF3C04A), pen, thin = true)
    val rwall = ridesPoly(right, wallTop, right + o.x, wallTop + o.y, right + o.x, o.y, right, 0f)
    ridesFlat(rwall, navy.lighten(0.1f), pen)
    for (k in 1..3) {
        val q = k / 4f
        drawLine(navy.lighten(0.3f), Offset(right + o.x * q, wallTop + o.y * q + 0.01f * u), Offset(right + o.x * q, o.y * q - 0.01f * u), strokeWidth = pen.lw * 0.6f)
    }

    // Prizes on pegs: two teddies at the sides and a small one hanging upside down over the cans.
    val furs = longArrayOf(0xFFC98A55, 0xFF7CC8F2, 0xFFFF9EC4)
    val pegs = floatArrayOf(-0.112f, -0.43f, 0.07f, 0.035f, -0.442f, 0.05f, 0.128f, -0.43f, 0.07f)
    for (k in 0 until 3) {
        val peg = Offset(pegs[k * 3] * u, pegs[k * 3 + 1] * u)
        ridesPrizeTeddy(peg, pegs[k * 3 + 2] * u, Color(furs[k]), k == 1, sin(t * 1.3f + k * 1.9f) * 5f, pen)
        inkedCircle(peg, 0.005f * u, ridesGold, pen, shade = false)
    }

    // The counter.
    val frontR = Rect(left, top, right, 0f)
    box3d(frontR, d * u, red, pen, top = Color(0xFFE3B27A), side = red.darken(0.3f))
    val frontP = Path().apply { addRect(frontR) }
    ridesStripes(frontP, left, top, right, 0f, 0.05f * u, 0.025f * u, Color.White)
    drawRect(Ink.line, frontR.topLeft, frontR.size, style = pen.stroke)
    inkedRound(Rect(left - 0.004f * u, top, right + 0.004f * u, top + 0.018f * u), 0.004f * u, navy, pen)
    val medal = Offset(0f, -0.1f * u)
    inkedCircle(medal, 0.048f * u, ridesGold, pen)
    drawCircle(Color.White, 0.036f * u, medal)
    drawCircle(red, 0.026f * u, medal)
    drawCircle(Color.White, 0.015f * u, medal)
    drawCircle(red, 0.006f * u, medal)
    drawCircle(Ink.line, 0.036f * u, medal, style = pen.thin)
    val grain = Color(0xFFC98A55)
    for (k in 1..3) {
        val q = k / 4f
        drawLine(grain, Offset(left + o.x * q + 0.01f * u, top + o.y * q), Offset(right + o.x * q - 0.01f * u, top + o.y * q), strokeWidth = pen.lw * 0.5f)
    }

    // The cans: a pyramid in the drop zone, or tumbled after a hit.
    val cw = 0.036f * u
    val ch = 0.046f * u
    val base = top + Oblique.DY * 0.07f * u
    val cx = Oblique.DX * 0.07f * u
    val wob = f.anim
    if (f.mode == 0) {
        var k = 0
        for (row in 0 until 3) {
            for (j in 0 until 3 - row) {
                val x = cx + (j - (2 - row) / 2f) * (cw + 0.002f * u)
                val y = base - ch * (row + 0.5f)
                val rot = sin(t * 30f + k * 1.7f) * wob * 7f
                ridesCan(Offset(x, y), cw, ch, Color(RIDES_CAN[k]), rot, pen)
                k++
            }
        }
    } else {
        // (x, lift, rotation) in units and degrees; lying cans rest on their side.
        val tumble = wob * wob
        val lie = base - cw / 2f
        val spots = floatArrayOf(
            -0.105f, lie, 90f,
            -0.04f, lie, -84f,
            0.048f, base - ch / 2f, 11f,
            0.112f, lie, 97f,
            -0.078f, lie - cw * 0.92f, 64f,
            0.155f, -cw / 2f, -90f,
        )
        for (k in 0 until 6) {
            val x = if (k == 5) spots[k * 3] * u else spots[k * 3] * u + cx
            val y = spots[k * 3 + 1] - tumble * (0.04f + ridesHash(k) * 0.05f) * u
            val rot = spots[k * 3 + 2] + tumble * (ridesHash(k + 9) - 0.5f) * 220f
            ridesCan(Offset(x, y), cw, ch, Color(RIDES_CAN[k]), rot, pen)
        }
    }

    // Posts, the sign board with bulbs and a target, and bunting.
    for (x in floatArrayOf(left + 0.006f * u, right - 0.006f * u)) {
        ridesCandyPole(Offset(x, -0.004f * u), Offset(x, -0.47f * u), 0.014f * u, Color.White, red, pen)
    }
    val board = Rect(-0.19f * u, -0.54f * u, 0.19f * u, -0.47f * u)
    box3d(board, 0.05f * u, navy, pen)
    drawRoundRect(navy.lighten(0.15f), Offset(board.left + 0.008f * u, board.top + 0.008f * u), Size(board.width - 0.016f * u, board.height - 0.016f * u), CornerRadius(0.01f * u))
    val tc = Offset(0f, board.center.y)
    for ((k, rr) in floatArrayOf(0.026f, 0.018f, 0.01f).withIndex()) drawCircle(if (k % 2 == 0) red else Color.White, rr * u, tc)
    drawCircle(Ink.line, 0.026f * u, tc, style = pen.thin)
    for (s in intArrayOf(-1, 1)) {
        val sp = starPath(Offset(s * 0.1f * u, tc.y), 0.016f * u, 0.007f * u, turn = s * 12f)
        drawPath(sp, ridesGold)
        drawPath(sp, Ink.line, style = pen.thin)
        ridesCan(Offset(s * 0.055f * u, tc.y + 0.004f * u), 0.018f * u, 0.024f * u, Color(RIDES_CAN[if (s < 0) 1 else 2]), s * 8f, pen)
    }
    for (k in 0 until 9) {
        val bx2 = board.left + 0.02f * u + k * (board.width - 0.04f * u) / 8f
        val lit = if (ridesWrap(t * 3f + k * 0.5f, 3f) < 1.5f) 1f else 0.5f
        ridesBulb(Offset(bx2, board.bottom), 0.0058f * u, Color(RIDES_BULB[k % 3]), lit, pen)
    }
    ridesBunting(Offset(left, -0.468f * u), Offset(right, -0.468f * u), 0.012f * u, 7, 0.022f * u, t, pen)
}

// ====================================================================================== BUMPER CAR

private val RIDES_BUMPER = longArrayOf(0xFFFF5A5F, 0xFF34C6B8)
private val RIDES_BUMPER_DECAL = longArrayOf(0xFFFFD447, 0xFFFF9EC4)

private fun ridesBumperDir(f: Fixture): Float = when {
    f.angleV < -0.001f -> -1f
    f.angleV > 0.001f -> 1f
    else -> if (f.variant % 2 == 0) 1f else -1f
}

private const val BUMP_CY = -0.056f
private const val BUMP_RX = 0.122f
private const val BUMP_RY = 0.03f
private const val BUMP_IRX = 0.1f
private const val BUMP_IRY = 0.021f
private const val BUMP_TH = 0.026f

/** A path along the top edge ([pts], x y pairs in units, rear to nose), closed along half the inner oval. */
private fun ridesTubPath(pts: FloatArray, dir: Float, u: Float, lower: Boolean): Path = Path().apply {
    moveTo(pts[0] * dir * u, pts[1] * u)
    var k = 2
    while (k < pts.size) {
        lineTo(pts[k] * dir * u, pts[k + 1] * u)
        k += 2
    }
    val inner = Rect(-BUMP_IRX * u, (BUMP_CY - BUMP_IRY) * u, BUMP_IRX * u, (BUMP_CY + BUMP_IRY) * u)
    lineTo(BUMP_IRX * dir * u, BUMP_CY * u)
    // From the nose side round to the rear side, under (near wall) or over (far wall) the middle.
    val start = if (dir > 0f) 0f else 180f
    val sweep = if (lower) 180f * dir else -180f * dir
    arcTo(inner, start, sweep, false)
    close()
}

private fun DrawScope.ridesBumperBack(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val dir = ridesBumperDir(f)
    val body = Color(RIDES_BUMPER[f.variant % 2])
    fun o(x: Float, y: Float) = Offset(x * dir * u, y * u)
    groundShadow(0f, -0.004f * u, 0.28f * u, 0.95f)
    translate(0f, f.bob * u) {
        // The pole with its spring, the contact on top and sparks while it runs.
        val poleBot = o(-0.078f, -0.11f)
        val poleTop = o(-0.078f, -0.43f)
        ridesCandyPole(poleBot, poleTop, 0.009f * u, Color(0xFFE9EDF3), ridesCoral, pen)
        ridesSpring(o(-0.078f, -0.096f), o(-0.078f, -0.13f), 0.007f * u, 3, ridesSteel, pen.lw * 0.8f)
        capsule(poleTop + Offset(-0.02f * u, 0f), poleTop + Offset(0.02f * u, 0f), 0.006f * u, ridesSteelDark, pen)
        drawLine(Ink.line, poleTop + Offset(-0.014f * u, -0.003f * u), poleTop + Offset(-0.016f * u, -0.012f * u), strokeWidth = pen.lw * 0.8f)
        drawLine(Ink.line, poleTop + Offset(0.014f * u, -0.003f * u), poleTop + Offset(0.016f * u, -0.012f * u), strokeWidth = pen.lw * 0.8f)
        if (f.on) ridesSpark(poleTop - Offset(0f, 0.012f * u), 0.03f * u, t, pen)

        // The rubber ring's top all round, and the floor inside it.
        val outer = Rect(-BUMP_RX * u, (BUMP_CY - BUMP_RY) * u, BUMP_RX * u, (BUMP_CY + BUMP_RY) * u)
        val inner = Rect(-BUMP_IRX * u, (BUMP_CY - BUMP_IRY) * u, BUMP_IRX * u, (BUMP_CY + BUMP_IRY) * u)
        drawOval(Color(0xFF5A546C), outer.topLeft, outer.size)
        drawOval(Ink.line, outer.topLeft, outer.size, style = pen.stroke)
        drawOval(Color(0xFF26232F), inner.topLeft, inner.size)
        drawOval(Ink.line, inner.topLeft, inner.size, style = pen.thin)

        // The far wall seen from inside, and the high seat back.
        val far = ridesTubPath(floatArrayOf(-0.1f, -0.124f, -0.05f, -0.116f, 0.02f, -0.108f, 0.07f, -0.102f, 0.097f, -0.084f), dir, u, lower = false)
        inked(far, body.darken(0.4f), pen)
        drawLine(body.darken(0.15f), o(-0.095f, -0.119f), o(0.07f, -0.098f), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        val seatBack = Rect(min(o(-0.1f, 0f).x, o(-0.03f, 0f).x), -0.158f * u, max(o(-0.1f, 0f).x, o(-0.03f, 0f).x), -0.096f * u)
        inkedRound(seatBack, 0.024f * u, body.darken(0.06f), pen)
        val cushion = Rect(seatBack.left + 0.01f * u, seatBack.top + 0.01f * u, seatBack.right - 0.01f * u, seatBack.bottom - 0.004f * u)
        drawRoundRect(Color(0xFF3F4A6E), cushion.topLeft, cushion.size, CornerRadius(0.015f * u))
        drawLine(Color(0xFF56628A), Offset(cushion.center.x, cushion.top + 0.007f * u), Offset(cushion.center.x, cushion.bottom - 0.005f * u), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
        // A little tail light on the rear.
        inkedCircle(o(-0.101f, -0.108f), 0.006f * u, Color(0xFFFF4D5E), pen, shade = false)
        ridesBumperShell(f, u, pen, dir, body)
    }
}

private fun DrawScope.ridesBumperFront(f: Fixture, u: Float, pen: Pen) {
    val dir = ridesBumperDir(f)
    translate(0f, f.bob * u) { ridesBumperShell(f, u, pen, dir, Color(RIDES_BUMPER[f.variant % 2])) }
}

/** The ring's near half and the car's near side: everything that covers the driver's legs. */
private fun DrawScope.ridesBumperShell(f: Fixture, u: Float, pen: Pen, dir: Float, body: Color) {
    fun o(x: Float, y: Float) = Offset(x * dir * u, y * u)
    val outer = Rect(-BUMP_RX * u, (BUMP_CY - BUMP_RY) * u, BUMP_RX * u, (BUMP_CY + BUMP_RY) * u)
    val inner = Rect(-BUMP_IRX * u, (BUMP_CY - BUMP_IRY) * u, BUMP_IRX * u, (BUMP_CY + BUMP_IRY) * u)
    val lowOuter = outer.translate(0f, BUMP_TH * u)
    val band = Path().apply {
        moveTo(outer.left, BUMP_CY * u)
        arcTo(outer, 180f, -180f, false)
        lineTo(outer.right, (BUMP_CY + BUMP_TH) * u)
        arcTo(lowOuter, 0f, 180f, false)
        close()
    }
    inked(band, ridesRubber, pen)
    drawArc(Color.White.copy(alpha = 0.18f), 30f, 120f, false, outer.translate(0f, BUMP_TH * u * 0.45f).topLeft, outer.size, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    val topRing = Path().apply {
        moveTo(outer.left, BUMP_CY * u)
        arcTo(outer, 180f, -180f, false)
        lineTo(inner.right, BUMP_CY * u)
        arcTo(inner, 0f, 180f, false)
        close()
    }
    drawPath(topRing, Color(0xFF5A546C))
    drawPath(topRing, Ink.line, style = pen.thin)
    drawArc(Color.White.copy(alpha = 0.3f), 110f, 50f, false, Offset(outer.left + 0.008f * u, outer.top + 0.004f * u), Size(outer.width - 0.016f * u, outer.height), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))

    // The near wall of the car.
    val shell = ridesTubPath(floatArrayOf(-0.1f, -0.116f, -0.076f, -0.1f, -0.03f, -0.09f, 0.03f, -0.089f, 0.07f, -0.092f, 0.094f, -0.08f, 0.1f, -0.064f), dir, u, lower = true)
    inked(shell, body, pen, outline = false)
    clipPath(shell) {
        drawRect(Color.White.copy(alpha = 0.92f), Offset(-0.13f * u, -0.074f * u), Size(0.26f * u, 0.009f * u))
        drawRect(body.darken(0.2f), Offset(-0.13f * u, -0.065f * u), Size(0.26f * u, 0.004f * u))
        drawOval(body.darken(0.25f), Offset(inner.left, inner.top + 0.01f * u), inner.size)
    }
    drawPath(shell, Ink.line, style = pen.stroke)
    // A chrome rim along the top edge.
    val rim = Path().apply {
        val a = o(-0.1f, -0.116f)
        moveTo(a.x, a.y)
        for (q in listOf(o(-0.076f, -0.1f), o(-0.03f, -0.09f), o(0.03f, -0.089f), o(0.07f, -0.092f), o(0.094f, -0.08f))) lineTo(q.x, q.y)
    }
    ridesTube(rim, 0.006f * u, Color(0xFFE3E8EF), pen)
    val decal = starPath(o(0.03f, -0.058f), 0.016f * u, 0.007f * u, turn = 8f * dir)
    drawPath(decal, Color(RIDES_BUMPER_DECAL[f.variant % 2]))
    drawPath(decal, Ink.line, style = pen.thin)
    inkedCircle(o(-0.055f, -0.058f), 0.012f * u, Color.White, pen, shade = false)
    drawCircle(body.darken(0.1f), 0.0055f * u, o(-0.055f, -0.058f))
    val head = o(0.093f, -0.066f)
    if (f.on) drawCircle(Color(0xFFFFF3A0).copy(alpha = 0.5f), 0.018f * u, head)
    inkedCircle(head, 0.0075f * u, if (f.on) Color(0xFFFFF7C2) else Color(0xFFFFE08A), pen, shade = false)
    shine(o(-0.08f, -0.098f), 0.014f * u, 0.006f * u, 0.6f)
    // Steering wheel in front of the driver's hands.
    val col0 = o(0.068f, -0.09f)
    val col1 = o(0.046f, -0.118f)
    capsule(col0, col1, 0.005f * u, ridesSteelDark, pen)
    val wheel = Rect(col1.x - 0.02f * u, col1.y - 0.0075f * u, col1.x + 0.02f * u, col1.y + 0.0075f * u)
    drawOval(Ink.line, wheel.topLeft, wheel.size, style = Stroke(0.006f * u + pen.lw * 2f))
    drawOval(Color(0xFF2E2A3A), wheel.topLeft, wheel.size, style = Stroke(0.006f * u))
    drawCircle(Color(RIDES_BUMPER_DECAL[f.variant % 2]), 0.0035f * u, col1)
    // Bonk lines after a bump.
    if (f.anim > 0.05f) {
        val c = o(0.13f, -0.04f)
        for (k in -1..1) {
            val a = k * 0.6f
            val r0 = 0.012f * u
            val r1 = (0.012f + 0.032f * f.anim) * u
            drawLine(Ink.line.copy(alpha = f.anim), c + Offset(cos(a) * dir * r0, sin(a) * r0), c + Offset(cos(a) * dir * r1, sin(a) * r1), strokeWidth = pen.lw, cap = StrokeCap.Round)
        }
    }
}

/** A crackling spark that changes shape many times a second. */
private fun DrawScope.ridesSpark(c: Offset, r: Float, t: Float, pen: Pen) {
    val k = floor(t * 14f).toInt()
    val a0 = ridesHash(k) * RIDES_TAU
    val s = 0.6f + 0.6f * ridesHash(k + 7)
    drawCircle(Color(0xFFFFF3A0).copy(alpha = 0.3f), r * 1.2f * s, c)
    drawCircle(Color(0xFF9CF0FF).copy(alpha = 0.35f), r * 0.6f * s, c)
    val p = Path()
    for (j in 0 until 5) {
        val a = a0 + j * RIDES_TAU / 5f
        val len = r * s * (0.7f + 0.5f * ridesHash(k * 5 + j))
        p.moveTo(c.x, c.y)
        p.lineTo(c.x + cos(a + 0.35f) * len * 0.5f, c.y + sin(a + 0.35f) * len * 0.5f)
        p.lineTo(c.x + cos(a) * len, c.y + sin(a) * len)
    }
    drawPath(p, Color(0xFFFFD84D), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(p, Color.White, style = Stroke(pen.lw * 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    twinkle(c, r * 0.45f * s, Color.White)
}

// ====================================================================================== SHOP SHELF

private fun DrawScope.ridesShelf(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val v = f.variant.mod(2)
    val d = 0.12f
    val o = Oblique.offset(d, u)
    val frame = if (v == 0) Color(0xFFDCE1E9) else Color(0xFFE4EAE6)
    val backC = if (v == 0) Color(0xFFDDE7F4) else Color(0xFFD7F0E2)
    val strip = if (v == 0) Color(0xFFFFC83D) else Color(0xFF6BCB77)
    val accent = if (v == 0) Color(0xFFFF8A3D) else Color(0xFF2E9E5B)
    val left = -0.175f * u
    val right = 0.175f * u
    val top = -0.44f * u
    groundShadow(0.04f * u, -0.012f * u, 0.44f * u, 0.8f)

    // Header sign rising behind the top.
    val hc = Offset(o.x, top + o.y - 0.03f * u)
    val header = Rect(hc.x - 0.085f * u, hc.y - 0.03f * u, hc.x + 0.085f * u, hc.y + 0.03f * u)
    inkedRound(header, 0.02f * u, accent, pen)
    drawRoundRect(Color.White.copy(alpha = 0.25f), Offset(header.left + 0.006f * u, header.top + 0.005f * u), Size(header.width - 0.012f * u, 0.012f * u), CornerRadius(0.006f * u))
    if (v == 0) {
        // A loaf of bread.
        val loaf = Rect(hc.x - 0.035f * u, hc.y - 0.018f * u, hc.x + 0.035f * u, hc.y + 0.016f * u)
        inkedRound(loaf, 0.016f * u, Color(0xFFE3A55A), pen)
        for (k in -1..1) drawLine(Color(0xFFB9793F), Offset(hc.x + k * 0.016f * u - 0.004f * u, loaf.top + 0.006f * u), Offset(hc.x + k * 0.016f * u + 0.004f * u, loaf.top + 0.018f * u), strokeWidth = pen.lw * 0.8f, cap = StrokeCap.Round)
    } else {
        // An apple with a leaf.
        inkedCircle(Offset(hc.x, hc.y + 0.002f * u), 0.02f * u, Color(0xFFFF5A5F), pen)
        drawLine(Ink.line, Offset(hc.x, hc.y - 0.016f * u), Offset(hc.x + 0.004f * u, hc.y - 0.026f * u), strokeWidth = pen.lw, cap = StrokeCap.Round)
        val leaf = blobPath(hc.x + 0.004f * u, hc.y - 0.02f * u, hc.x + 0.02f * u, hc.y - 0.032f * u, hc.x + 0.026f * u, hc.y - 0.022f * u, hc.x + 0.012f * u, hc.y - 0.016f * u)
        inked(leaf, Color(0xFF3BC46B), pen)
        shine(Offset(hc.x - 0.008f * u, hc.y - 0.006f * u), 0.008f * u, 0.006f * u, 0.8f)
    }

    // The back panel, pegboard holes, the left wall's inside and the right wall's outside.
    val back = Rect(left + o.x, top + o.y, right + o.x, o.y)
    drawRect(backC, back.topLeft, back.size)
    var row = 0
    var y = back.top + 0.02f * u
    while (y < back.bottom - 0.01f * u) {
        var x = back.left + 0.015f * u + (row % 2) * 0.012f * u
        while (x < back.right - 0.01f * u) {
            drawCircle(backC.darken(0.18f), pen.lw * 0.55f, Offset(x, y))
            x += 0.024f * u
        }
        y += 0.022f * u
        row++
    }
    // Soft shadows under each board on the back wall.
    for (cy in floatArrayOf(top + 0.016f * u, -0.292f * u, -0.152f * u)) {
        val y0 = cy + o.y
        drawRect(Brush.verticalGradient(listOf(Ink.line.copy(alpha = 0.22f), Ink.line.copy(alpha = 0f)), y0, y0 + 0.05f * u), Offset(back.left, y0), Size(back.width, 0.05f * u))
    }
    drawRect(Ink.line, back.topLeft, back.size, style = pen.thin)
    ridesFlat(ridesPoly(left, top, left + o.x, top + o.y, left + o.x, o.y, left, 0f), frame.darken(0.18f), pen, thin = true)
    // The end panel on the right, painted in the aisle's colour.
    val end = ridesPoly(right, top, right + o.x, top + o.y, right + o.x, o.y, right, 0f)
    ridesFlat(end, accent.darken(0.12f), pen)
    clipPath(end) {
        drawPath(ridesPoly(right + o.x * 0.35f, top, right + o.x * 0.6f, top, right + o.x * 0.6f, 0f, right + o.x * 0.35f, 0f), accent.lighten(0.25f))
    }
    drawPath(end, Ink.line, style = pen.stroke)

    // Shelves with price strips and tags.
    val board = frame.lighten(0.2f)
    val levels = floatArrayOf(-0.31f, -0.17f, -0.03f)
    for ((li, lv) in levels.withIndex()) {
        val sy = lv * u
        topFace3d(left, right, sy, d * u, board, pen)
        val sr = Rect(left - 0.004f * u, sy, right + 0.004f * u, sy + 0.018f * u)
        inkedRound(sr, 0.003f * u, strip, pen, shade = false)
        drawLine(strip.lighten(0.5f), Offset(sr.left + 0.004f * u, sr.top + 0.004f * u), Offset(sr.right - 0.004f * u, sr.top + 0.004f * u), strokeWidth = pen.lw * 0.6f)
        for (k in 0 until 3) {
            val tx = left + (0.05f + k * 0.12f + (li % 2) * 0.03f) * u
            val tag = Rect(tx, sy + 0.003f * u, tx + 0.034f * u, sy + 0.015f * u)
            drawRect(Color.White, tag.topLeft, tag.size)
            drawRect(Ink.line, tag.topLeft, tag.size, style = Stroke(pen.lw * 0.5f))
            drawCircle(if ((k + li) % 2 == 0) Color(0xFFFF5A5F) else accent, 0.0035f * u, Offset(tag.left + 0.007f * u, tag.center.y))
            for (b in 0 until 4) {
                val bx = tag.left + 0.015f * u + b * 0.0045f * u
                drawLine(Ink.line, Offset(bx, tag.top + 0.0025f * u), Offset(bx, tag.bottom - 0.0025f * u), strokeWidth = if (b % 2 == 0) pen.lw * 0.5f else pen.lw * 0.3f)
            }
        }
    }
    // Base plinth under the bottom shelf.
    inked(ridesPoly(left, -0.012f * u, right, -0.012f * u, right, 0f, left, 0f), Color(0xFF6B7288), pen)

    // The top board and the front uprights.
    box3d(Rect(left - 0.006f * u, top, right + 0.006f * u, top + 0.016f * u), d * u, frame, pen, top = frame.lighten(0.3f))
    for (x in floatArrayOf(left, right)) {
        val post = Rect(x - 0.008f * u, top + 0.016f * u, x + 0.008f * u, 0f)
        inkedRound(post, 0.003f * u, frame.darken(0.05f), pen)
        var sy = post.top + 0.02f * u
        while (sy < post.bottom - 0.02f * u) {
            drawLine(frame.darken(0.45f), Offset(x, sy), Offset(x, sy + 0.006f * u), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
            sy += 0.018f * u
        }
    }

    // A shelf wobbler that never stops wobbling.
    val wb = Offset(0.11f * u, -0.17f * u + 0.018f * u)
    val wobble = sin(t * 5f) * 10f
    rotate(wobble, wb) {
        val head = Offset(wb.x + 0.028f * u, wb.y + 0.018f * u)
        drawLine(Ink.line, wb, head, strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        inkedCircle(head, 0.016f * u, if (v == 0) Color(0xFFFF5A5F) else Color(0xFFFF9F43), pen, shade = false)
        val star = starPath(head, 0.011f * u, 0.005f * u)
        drawPath(star, Color.White)
    }
}

// =========================================================================================== SCALE

private fun DrawScope.ridesScale(f: Fixture, u: Float, pen: Pen) {
    val d = 0.1f
    groundShadow(0.025f * u, -0.006f * u, 0.2f * u, 0.8f)
    for (s in intArrayOf(-1, 1)) inkedRound(Rect(s * 0.062f * u - 0.012f * u, -0.009f * u, s * 0.062f * u + 0.012f * u, 0f), 0.004f * u, ridesRubber, pen, shade = false)
    val body = Rect(-0.08f * u, -0.05f * u, 0.08f * u, -0.007f * u)
    box3d(body, d * u, Color(0xFFE9EDF3), pen, top = Color(0xFFD5DBE4))
    // Ribbed rubber mat on top.
    fun p(x: Float, depth: Float) = Offset(x + Oblique.DX * depth * u, body.top + Oblique.DY * depth * u)
    val mat = ridesQuad(p(body.left + 0.008f * u, 0.01f), p(body.right - 0.008f * u, 0.01f), p(body.right - 0.008f * u, d - 0.01f), p(body.left + 0.008f * u, d - 0.01f))
    drawPath(mat, Color(0xFF4A4F63))
    clipPath(mat) {
        for (k in 0 until 7) {
            val dd = 0.012f + k * 0.013f
            drawLine(Color(0xFF6B7288), p(body.left, dd), p(body.right, dd), strokeWidth = pen.lw * 0.6f)
        }
    }
    drawPath(mat, Ink.line, style = pen.thin)
    // The display, glowing, with the weight in big digits.
    val panel = Rect(-0.068f * u, -0.047f * u, 0.068f * u, -0.01f * u)
    inkedRound(panel, 0.006f * u, Color(0xFF1C2233), pen, shade = false)
    val flash = f.anim
    val on = lerp(Color(0xFF7CFFB2), Color.White, flash * 0.6f)
    drawRoundRect(on.copy(alpha = 0.14f + 0.1f * pen.night + 0.25f * flash), Offset(panel.left + 0.004f * u, panel.top + 0.004f * u), Size(panel.width - 0.008f * u, panel.height - 0.008f * u), CornerRadius(0.004f * u))
    ridesNumber(f.mode, 3, panel.right - 0.017f * u, panel.top + 0.006f * u, 0.021f * u, 0.025f * u, 0.005f * u, on, Color(0xFF7CFFB2).copy(alpha = 0.08f), glow = true)
    drawCircle(if (f.mode > 0) Color(0xFF7CFFB2) else Color(0xFF3A4A48), 0.0028f * u, Offset(panel.right - 0.009f * u, panel.bottom - 0.008f * u))
    inkedCircle(Offset(panel.left + 0.01f * u, panel.center.y), 0.005f * u, ridesCoral, pen, shade = false)
    shine(Offset(panel.left + 0.03f * u, panel.top + 0.006f * u), 0.03f * u, 0.004f * u, 0.25f)
}

// ========================================================================================= FREEZER

private fun ridesSlide(f: Fixture): Float = if (f.open) 1f - ridesEase(f.anim) else ridesEase(f.anim)

private fun DrawScope.ridesFreezer(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val d = 0.15f
    val left = -0.22f * u
    val right = 0.22f * u
    val rimTop = -0.2f * u
    val lidY = -0.22f * u
    val white = Color(0xFFF3F6FA)
    val blue = Color(0xFF2F6FB8)
    val ice = Color(0xFFCFEFFF)
    fun p(x: Float, depth: Float) = Offset(x + Oblique.DX * depth * u, lidY + Oblique.DY * depth * u)
    groundShadow(0.04f * u, -0.01f * u, 0.52f * u, 0.8f)

    box3d(Rect(left, rimTop, right, 0f), d * u, white, pen, top = white, side = Color(0xFFC9D6EE))
    box3d(Rect(left, lidY, right, rimTop), d * u, Color(0xFFDDE4EE), pen, top = Color(0xFFE9EEF5))

    // Base band with vents and a snowflake badge.
    val band = Rect(left, -0.036f * u, right, 0f)
    inked(Path().apply { addRect(band) }, blue, pen)
    for (k in 0 until 9) {
        val x = left + 0.03f * u + k * 0.018f * u
        drawLine(blue.darken(0.35f), Offset(x, band.top + 0.01f * u), Offset(x, band.bottom - 0.01f * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    }
    val flake = Offset(right - 0.05f * u, band.center.y)
    for (k in 0 until 3) {
        val a = k * RIDES_PI / 3f
        drawLine(Color.White, flake + Offset(cos(a) * 0.011f * u, sin(a) * 0.011f * u), flake - Offset(cos(a) * 0.011f * u, sin(a) * 0.011f * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    }

    // The front window into the cold.
    val win = Rect(left + 0.022f * u, -0.188f * u, right - 0.022f * u, -0.046f * u)
    ridesColdInside(win, u, pen, t)
    drawRoundRect(Ink.line, win.topLeft, win.size, CornerRadius(0.008f * u), style = pen.stroke)
    drawRoundRect(Color.White, Offset(win.left - 0.004f * u, win.top - 0.004f * u), Size(win.width + 0.008f * u, win.height + 0.008f * u), CornerRadius(0.012f * u), style = Stroke(pen.lw * 0.9f))
    ridesFreezerSheen(win, u, 1f)

    // The lid: the cold seen from above, and two sliding glass panes.
    val i0 = left + 0.014f * u
    val i1 = right - 0.014f * u
    val hole = ridesQuad(p(i0, 0.014f), p(i1, 0.014f), p(i1, d - 0.014f), p(i0, d - 0.014f))
    drawPath(hole, Brush.verticalGradient(listOf(Color(0xFF3F78B4), Color(0xFF7DB8E6)), p(0f, d).y, p(0f, 0f).y))
    clipPath(hole) {
        // The inside of the back wall, frosty and lit, and the cold floor below it.
        val wallDrop = 0.07f * u
        val b0 = p(i0, d - 0.014f)
        val b1 = p(i1, d - 0.014f)
        drawPath(ridesPoly(b0.x, b0.y, b1.x, b1.y, b1.x, b1.y + wallDrop, b0.x, b0.y + wallDrop), Brush.verticalGradient(listOf(Color(0xFFD6F0FF), Color(0xFF9CCDEE)), b0.y, b0.y + wallDrop))
        for (k in 0 until 9) {
            val x = b0.x + (b1.x - b0.x) * (k + 0.5f) / 9f
            drawLine(Color.White.copy(alpha = 0.7f), Offset(x, b0.y), Offset(x + 0.004f * u, b0.y + (0.012f + ridesHash(k + 21) * 0.02f) * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
        }
        for (k in 0 until 14) {
            val fx = i0 + ridesHash(k * 2) * (i1 - i0)
            val fd = 0.02f + ridesHash(k * 2 + 1) * (d - 0.04f)
            drawCircle(Color.White.copy(alpha = 0.5f), (0.003f + ridesHash(k + 40) * 0.005f) * u, p(fx, fd))
        }
        drawPath(ridesQuad(p(i0, 0.014f), p(i1, 0.014f), p(i1, 0.04f), p(i0, 0.04f)), Ink.line.copy(alpha = 0.2f))
    }
    drawPath(hole, Ink.line, style = pen.thin)
    val slide = ridesSlide(f)
    val mid = (i0 + i1) / 2f
    val shift = slide * (mid - i0) * 0.96f
    ridesPane(mid - 0.002f * u, i1, d, lidY, u, pen)
    ridesPane(i0 + shift, mid + 0.002f * u + shift, d, lidY, u, pen)

    // Cold mist curls out of an open lid.
    if (slide > 0.05f) {
        for (k in 0 until 5) {
            val ph = ridesWrap(t * 0.32f + k * 0.2f, 1f)
            val x = i0 + (0.03f + k * 0.035f) * u + Oblique.DX * d * 0.5f * u + sin(ph * 5f + k) * 0.012f * u
            val y = lidY + Oblique.DY * d * 0.5f * u - ph * 0.12f * u
            val a = slide * 0.4f * sin(ph * RIDES_PI)
            val r = (0.016f + ph * 0.03f) * u
            drawCircle(Color.White.copy(alpha = a), r, Offset(x, y))
            drawCircle(Color.White.copy(alpha = a * 0.8f), r * 0.7f, Offset(x + r * 0.8f, y + r * 0.2f))
        }
    }
}

/** A frosty inside seen through the freezer's front window. */
private fun DrawScope.ridesColdInside(win: Rect, u: Float, pen: Pen, t: Float) {
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFA7D5F2), Color(0xFFE4F6FF)), win.top, win.bottom), win.topLeft, win.size, CornerRadius(0.008f * u))
    clipRect(win.left, win.top, win.right, win.bottom) {
        // The far wall and floor edge.
        drawLine(Color(0xFF8CC2E6), Offset(win.left, win.bottom - 0.03f * u), Offset(win.right, win.bottom - 0.03f * u), strokeWidth = pen.lw * 0.7f)
        // Frost creeping in from the corners.
        for (corner in 0 until 2) {
            val cx = if (corner == 0) win.left else win.right
            val cy = if (corner == 0) win.top else win.bottom
            val sx = if (corner == 0) 1f else -1f
            val sy = if (corner == 0) 1f else -1f
            val frost = Path().apply {
                moveTo(cx, cy)
                lineTo(cx + sx * 0.07f * u, cy)
                quadraticTo(cx + sx * 0.045f * u, cy + sy * 0.012f * u, cx + sx * 0.04f * u, cy + sy * 0.022f * u)
                quadraticTo(cx + sx * 0.02f * u, cy + sy * 0.026f * u, cx + sx * 0.014f * u, cy + sy * 0.045f * u)
                quadraticTo(cx + sx * 0.006f * u, cy + sy * 0.03f * u, cx, cy + sy * 0.05f * u)
                close()
            }
            drawPath(frost, Color.White.copy(alpha = 0.85f))
        }
        for (k in 0 until 6) {
            val c = Offset(win.left + ridesHash(k + 3) * win.width, win.top + ridesHash(k + 11) * win.height * 0.8f)
            twinkle(c, 0.006f * u, Color.White, 0.4f + 0.6f * sin(t * 2f + k * 2.3f).coerceAtLeast(0f))
        }
    }
}

private fun DrawScope.ridesFreezerSheen(win: Rect, u: Float, alpha: Float) {
    clipRect(win.left, win.top, win.right, win.bottom) {
        drawPath(ridesPoly(win.left + 0.04f * u, win.bottom, win.left + 0.075f * u, win.bottom, win.left + 0.13f * u, win.top, win.left + 0.095f * u, win.top), Color.White.copy(alpha = 0.3f * alpha))
        drawPath(ridesPoly(win.left + 0.1f * u, win.bottom, win.left + 0.112f * u, win.bottom, win.left + 0.167f * u, win.top, win.left + 0.155f * u, win.top), Color.White.copy(alpha = 0.25f * alpha))
    }
}

/** A sliding glass pane on the freezer lid from [x0] to [x1]. */
private fun DrawScope.ridesPane(x0: Float, x1: Float, d: Float, lidY: Float, u: Float, pen: Pen) {
    fun p(x: Float, depth: Float) = Offset(x + Oblique.DX * depth * u, lidY + Oblique.DY * depth * u)
    val pane = ridesQuad(p(x0, 0.012f), p(x1, 0.012f), p(x1, d - 0.012f), p(x0, d - 0.012f))
    drawPath(pane, Color(0xA6E3F5FF))
    clipPath(pane) {
        val a = p(x0 + 0.05f * u, 0.012f)
        drawPath(ridesPoly(a.x, a.y, a.x + 0.03f * u, a.y, a.x + 0.075f * u, a.y - 0.06f * u, a.x + 0.045f * u, a.y - 0.06f * u), Color.White.copy(alpha = 0.6f))
        val b = p(x0 + 0.1f * u, 0.012f)
        drawPath(ridesPoly(b.x, b.y, b.x + 0.012f * u, b.y, b.x + 0.057f * u, b.y - 0.06f * u, b.x + 0.045f * u, b.y - 0.06f * u), Color.White.copy(alpha = 0.5f))
        for (k in 0 until 3) drawCircle(Color.White.copy(alpha = 0.8f), 0.004f * u, p(x1 - (0.012f + k * 0.014f) * u, d - 0.03f - (k % 2) * 0.012f))
    }
    drawPath(pane, Color.White, style = Stroke(pen.lw * 1.6f, join = StrokeJoin.Round))
    drawPath(pane, Ink.line, style = pen.thin)
    val h = p((x0 + x1) / 2f, 0.012f)
    inkedRound(Rect(h.x - 0.022f * u, h.y - 0.004f * u, h.x + 0.022f * u, h.y + 0.004f * u), 0.004f * u, Color(0xFF6B7288), pen, shade = false)
}

/** Over contents (if the engine draws a glass layer): the window's sheen and frame. */
private fun DrawScope.ridesFreezerGlass(f: Fixture, u: Float, pen: Pen) {
    if (f.type != FixtureType.FREEZER) return
    val win = Rect(-0.198f * u, -0.188f * u, 0.198f * u, -0.046f * u)
    ridesFreezerSheen(win, u, 0.7f)
    drawRoundRect(Ink.line, win.topLeft, win.size, CornerRadius(0.008f * u), style = pen.stroke)
}

// ===================================================================================== SODA FRIDGE

private val ridesFridgeBody = Color(0xFF2F6FB8)

/** How far the door is open, 0..1, swinging with f.anim right after a tap. */
private fun ridesDoor(f: Fixture): Float = ridesSlide(f)

private fun DrawScope.ridesFridge(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val d = 0.12f
    val body = ridesFridgeBody
    val left = -0.12f * u
    val right = 0.12f * u
    val top = -0.42f * u
    groundShadow(0.035f * u, -0.01f * u, 0.3f * u, 0.8f)
    if (pen.night > 0f) {
        val c = Offset(0f, -0.2f * u)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFBFF0FF).copy(alpha = 0.35f * pen.night), Color(0x00BFF0FF)), c, 0.3f * u), 0.3f * u, c)
    }
    for (s in intArrayOf(-1, 1)) inkedRound(Rect(s * 0.095f * u - 0.012f * u, -0.01f * u, s * 0.095f * u + 0.012f * u, 0f), 0.003f * u, ridesRubber, pen, shade = false)
    box3d(Rect(left, top, right, -0.008f * u), d * u, body, pen, top = body.lighten(0.25f), side = body.darken(0.3f))

    // A lit header with a bottle and rising bubbles.
    val header = Rect(left + 0.008f * u, top + 0.008f * u, right - 0.008f * u, top + 0.044f * u)
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFFFE27A), ridesGold), header.top, header.bottom), header.topLeft, header.size, CornerRadius(0.008f * u))
    drawRoundRect(Ink.line, header.topLeft, header.size, CornerRadius(0.008f * u), style = pen.thin)
    val bc = Offset(header.left + 0.04f * u, header.center.y)
    val bottle = Path().apply {
        moveTo(bc.x - 0.004f * u, bc.y - 0.014f * u)
        lineTo(bc.x + 0.004f * u, bc.y - 0.014f * u)
        lineTo(bc.x + 0.004f * u, bc.y - 0.006f * u)
        quadraticTo(bc.x + 0.009f * u, bc.y - 0.003f * u, bc.x + 0.009f * u, bc.y + 0.004f * u)
        lineTo(bc.x + 0.009f * u, bc.y + 0.014f * u)
        lineTo(bc.x - 0.009f * u, bc.y + 0.014f * u)
        lineTo(bc.x - 0.009f * u, bc.y + 0.004f * u)
        quadraticTo(bc.x - 0.009f * u, bc.y - 0.003f * u, bc.x - 0.004f * u, bc.y - 0.006f * u)
        close()
    }
    inked(bottle, ridesCoral, pen)
    drawRect(Color.White, Offset(bc.x - 0.009f * u, bc.y + 0.002f * u), Size(0.018f * u, 0.005f * u))
    clipRect(header.left, header.top, header.right, header.bottom) {
        for (k in 0 until 6) {
            val ph = ridesWrap(t * 0.5f + k * 0.17f, 1f)
            val x = header.left + 0.065f * u + k * 0.022f * u + sin(ph * 6f + k) * 0.003f * u
            val y = header.bottom - ph * header.height
            drawCircle(Color.White, (0.003f + (k % 3) * 0.0015f) * u, Offset(x, y), style = Stroke(pen.lw * 0.6f))
        }
    }

    // The inside: a glowing back wall, shelves at the surface heights and the side wall.
    val open = Rect(-0.104f * u, -0.378f * u, 0.104f * u, -0.024f * u)
    val io = Oblique.offset(0.09f, u)
    clipRect(open.left, open.top, open.right, open.bottom) {
        drawRect(Color(0xFFBFE3F7), open.topLeft, open.size)
        val backWall = Rect(open.left + io.x, open.top + io.y, open.right + io.x, open.bottom + io.y)
        drawRect(Brush.verticalGradient(listOf(Color.White, Color(0xFFD9F4FF)), backWall.top, backWall.bottom), backWall.topLeft, backWall.size)
        drawPath(ridesPoly(open.left, open.top, open.left + io.x, open.top + io.y, open.left + io.x, open.bottom + io.y, open.left, open.bottom), Color(0xFFA9D6F0))
        drawPath(ridesPoly(open.left, open.bottom, open.right, open.bottom, open.right + io.x, open.bottom + io.y, open.left + io.x, open.bottom + io.y), Color(0xFF9CCBE8))
        // LED strip.
        drawRect(Color.White, Offset(open.left, open.top), Size(open.width, 0.006f * u))
        drawRect(Color(0xFFBFF0FF).copy(alpha = 0.5f), Offset(open.left, open.top + 0.006f * u), Size(open.width, 0.02f * u))
        for (lv in floatArrayOf(-0.28f, -0.16f, -0.04f)) {
            val sy = lv * u
            val shelf = ridesPoly(open.left, sy, open.right, sy, open.right + io.x, sy + io.y, open.left + io.x, sy + io.y)
            drawPath(shelf, Color(0x66FFFFFF))
            for (k in 0 until 9) {
                val x = open.left + k * open.width / 8f
                drawLine(Color(0xFF9AA6B8), Offset(x, sy), Offset(x + io.x, sy + io.y), strokeWidth = pen.lw * 0.45f)
            }
            drawLine(Color(0xFF9AA6B8), Offset(open.left + io.x, sy + io.y), Offset(open.right + io.x, sy + io.y), strokeWidth = pen.lw * 0.6f)
            drawRect(Ink.line.copy(alpha = 0.12f), Offset(open.left, sy + 0.006f * u), Size(open.width, 0.012f * u))
            drawRect(Color(0xFFE3E8EF), Offset(open.left, sy), Size(open.width, 0.006f * u))
            drawLine(ridesCoral, Offset(open.left, sy + 0.003f * u), Offset(open.right, sy + 0.003f * u), strokeWidth = pen.lw * 0.5f)
        }
    }
    drawRect(Ink.line, open.topLeft, open.size, style = pen.thin)

    ridesFridgeDoor(f, u, pen, full = true)
}

/**
 * The glass door, hinged on the left. Closed it covers the opening; open it swings out toward us.
 * [full] draws the glass tint too, not only the frame and the sheen.
 */
private fun DrawScope.ridesFridgeDoor(f: Fixture, u: Float, pen: Pen, full: Boolean) {
    val swing = ridesDoor(f)
    val phi = swing * 1.85f
    val w = 0.224f
    val h0 = Offset(-0.112f * u, -0.384f * u)
    val h1 = Offset(-0.112f * u, -0.016f * u)
    val dx = w * u * (cos(phi) - Oblique.DX * sin(phi))
    val dy = -Oblique.DY * w * u * sin(phi)
    fun q(a: Float, b: Float) = Offset(h0.x + dx * a, h0.y + dy * a + (h1.y - h0.y) * b)
    val door = ridesQuad(q(0f, 0f), q(1f, 0f), q(1f, 1f), q(0f, 1f))
    val glass = ridesQuad(q(0.07f, 0.03f), q(0.93f, 0.03f), q(0.93f, 0.97f), q(0.07f, 0.97f))
    val frame = Color(0xFF24305A)
    // The frame is the door minus the glass: draw it as four bars.
    val bars = listOf(
        ridesQuad(q(0f, 0f), q(1f, 0f), q(0.93f, 0.03f), q(0.07f, 0.03f)),
        ridesQuad(q(0.07f, 0.97f), q(0.93f, 0.97f), q(1f, 1f), q(0f, 1f)),
        ridesQuad(q(0f, 0f), q(0.07f, 0.03f), q(0.07f, 0.97f), q(0f, 1f)),
        ridesQuad(q(0.93f, 0.03f), q(1f, 0f), q(1f, 1f), q(0.93f, 0.97f)),
    )
    if (full) drawPath(glass, Color(0x3DBFE9FF))
    clipPath(glass) {
        val a = q(0.15f, 1f)
        val b = q(0.35f, 1f)
        val c = q(0.75f, 0f)
        val e = q(0.55f, 0f)
        drawPath(ridesQuad(a, b, c, e), Color.White.copy(alpha = if (full) 0.22f else 0.14f))
        val a2 = q(0.45f, 1f)
        val b2 = q(0.5f, 1f)
        val c2 = q(0.9f, 0f)
        val e2 = q(0.85f, 0f)
        drawPath(ridesQuad(a2, b2, c2, e2), Color.White.copy(alpha = if (full) 0.18f else 0.1f))
    }
    for (b in bars) drawPath(b, frame)
    drawPath(door, Ink.line, style = pen.stroke)
    drawPath(glass, Ink.line, style = pen.thin)
    // The handle stands out from the free edge.
    val ha = q(0.88f, 0.36f)
    val hb = q(0.88f, 0.64f)
    val out = Offset(0f, 0f) + Oblique.offset(-0.012f, u) * cos(phi)
    capsule(ha + out, hb + out, 0.008f * u, Color(0xFFE3E8EF), pen)
    drawLine(Color.White, ha + out + Offset(-0.001f * u, 0.01f * u), hb + out - Offset(0.001f * u, 0.01f * u), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
}

private fun DrawScope.ridesFridgeGlass(f: Fixture, u: Float, pen: Pen) {
    ridesFridgeDoor(f, u, pen, full = false)
}

// ======================================================================================== CHECKOUT

private fun DrawScope.ridesCheckoutBack(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val d = 0.12f
    val left = -0.28f * u
    val right = 0.28f * u
    val top = -0.2f * u
    groundShadow(0.03f * u, -0.012f * u, 0.64f * u, 0.8f)

    // The lane light on its pole, green for open.
    val lp = Offset(0.25f * u, top) + Oblique.offset(d * 0.9f, u)
    val lamp = Offset(lp.x, -0.46f * u)
    capsule(lp, lamp + Offset(0f, 0.02f * u), 0.008f * u, ridesSteelDark, pen)
    val green = Color(0xFF3BC46B)
    drawCircle(green.copy(alpha = 0.18f + 0.3f * pen.night), 0.05f * u, lamp)
    inkedCircle(lamp, 0.028f * u, green, pen)
    val tick = Path().apply {
        moveTo(lamp.x - 0.012f * u, lamp.y)
        lineTo(lamp.x - 0.003f * u, lamp.y + 0.009f * u)
        lineTo(lamp.x + 0.013f * u, lamp.y - 0.01f * u)
    }
    drawPath(tick, Color.White, style = Stroke(0.005f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    shine(lamp + Offset(-0.01f * u, -0.012f * u), 0.01f * u, 0.006f * u, 0.8f)

    // The cashier's chair back peeks over the counter.
    val chair = Rect(0.16f * u, -0.285f * u, 0.25f * u, -0.18f * u)
    inkedRound(chair, 0.03f * u, Color(0xFF3F4A6E), pen)
    drawRoundRect(Color(0xFF56628A), Offset(chair.left + 0.012f * u, chair.top + 0.01f * u), Size(chair.width - 0.024f * u, 0.04f * u), CornerRadius(0.016f * u))

    // Counter body and top.
    val birch = Color(0xFFEBD3AE)
    box3d(Rect(left, top, right, 0f), d * u, birch, pen, top = Color(0xFFF1F3F7), side = birch.darken(0.25f))

    // The belt, its stripes running toward the scanner.
    val bx0 = -0.27f * u
    val bx1 = 0.08f * u
    val bo = Oblique.offset(0.1f, u)
    val belt = ridesPoly(bx0, top, bx1, top, bx1 + bo.x, top + bo.y, bx0 + bo.x, top + bo.y)
    drawPath(belt, Color(0xFF3A3A4C))
    clipPath(belt) {
        val step = 0.03f * u
        val shift = ridesWrap(t * Attractions.BELT_SPEED * u, step)
        var x = bx0 - step + shift
        while (x < bx1 + step) {
            drawLine(Color(0xFF55566E), Offset(x, top), Offset(x + bo.x, top + bo.y), strokeWidth = 0.004f * u)
            x += step
        }
        drawRect(Color.White.copy(alpha = 0.07f), Offset(bx0, top + bo.y), Size(bx1 - bx0 + bo.x, -bo.y * 0.4f))
    }
    drawPath(belt, Ink.line, style = pen.thin)
    capsule(Offset(bx0 + bo.x, top + bo.y), Offset(bx1 + bo.x, top + bo.y), 0.005f * u, ridesSteel, pen)
    for (x in floatArrayOf(bx0, bx1)) capsule(Offset(x, top), Offset(x + bo.x, top + bo.y), 0.007f * u, ridesSteel, pen)
    // A "next customer" divider parked at the start of the belt.
    capsule(Offset(bx0 + 0.02f * u, top) + Oblique.offset(0.108f, u), Offset(bx0 + 0.1f * u, top) + Oblique.offset(0.108f, u), 0.009f * u, Color(0xFFFF9F43), pen)

    // The scanner tower with a red window and a display counting what has been scanned.
    val flash = f.anim
    val laser = Color(0xFFFF3B4E)
    drawLine(laser.copy(alpha = 0.35f + 0.6f * flash), Offset(0f, top), Offset(bo.x, top + bo.y), strokeWidth = 0.003f * u + flash * 0.004f * u, cap = StrokeCap.Round)
    val tw = Rect(-0.03f * u + bo.x, -0.34f * u, 0.03f * u + bo.x, top + bo.y)
    box3d(tw, 0.02f * u, Color(0xFF4A5068), pen)
    val window = Rect(tw.left + 0.008f * u, tw.bottom - 0.05f * u, tw.right - 0.008f * u, tw.bottom - 0.01f * u)
    if (flash > 0.02f) drawCircle(laser.copy(alpha = 0.35f * flash), 0.06f * u, window.center)
    drawRoundRect(lerp(Color(0xFF6B1E2A), Color(0xFFFF4D5E), 0.25f + 0.75f * flash), window.topLeft, window.size, CornerRadius(0.004f * u))
    drawRoundRect(Ink.line, window.topLeft, window.size, CornerRadius(0.004f * u), style = pen.thin)
    for (k in 0 until 3) drawLine(Color(0xFFFF9AA4).copy(alpha = 0.4f + 0.6f * flash), Offset(window.left + 0.003f * u, window.top + (0.01f + k * 0.01f) * u), Offset(window.right - 0.003f * u, window.top + (0.01f + k * 0.01f) * u), strokeWidth = pen.lw * 0.5f)
    val screen = Rect(tw.left + 0.006f * u, tw.top + 0.008f * u, tw.right - 0.006f * u, tw.top + 0.05f * u)
    drawRoundRect(Color(0xFF14202A), screen.topLeft, screen.size, CornerRadius(0.004f * u))
    val digit = Color(0xFF7CFFB2)
    ridesNumber(f.count.mod(100), 2, screen.right - 0.006f * u, screen.top + 0.008f * u, 0.016f * u, 0.026f * u, 0.004f * u, digit, digit.copy(alpha = 0.08f), glow = true)
    drawRoundRect(Ink.line, screen.topLeft, screen.size, CornerRadius(0.004f * u), style = pen.thin)

    ridesCheckoutFront(f, u, pen)
}

/** The counter's front panel (over the cashier's legs), sweets rack, card terminal and receipt. */
private fun DrawScope.ridesCheckoutFront(f: Fixture, u: Float, pen: Pen) {
    val left = -0.28f * u
    val right = 0.28f * u
    val top = -0.2f * u
    val birch = Color(0xFFEBD3AE)
    val front = Rect(left, top, right, 0f)
    inked(Path().apply { addRect(front) }, birch, pen, outline = false)
    for (k in 1 until 14) {
        val x = left + (right - left) * k / 14f
        drawLine(birch.darken(0.12f), Offset(x, top + 0.02f * u), Offset(x, -0.024f * u), strokeWidth = pen.lw * 0.5f)
    }
    drawRect(ridesNavy, Offset(left, -0.024f * u), Size(right - left, 0.024f * u))
    drawRect(ridesSky, Offset(left, top), Size(right - left, 0.014f * u))
    drawLine(Color.White.copy(alpha = 0.6f), Offset(left + pen.lw, top + 0.004f * u), Offset(right - pen.lw, top + 0.004f * u), strokeWidth = pen.lw * 0.6f)
    drawRect(Ink.line, front.topLeft, front.size, style = pen.stroke)
    drawLine(Ink.line, Offset(left, -0.024f * u), Offset(right, -0.024f * u), strokeWidth = pen.lw * 0.8f)
    drawLine(Ink.line, Offset(left, top + 0.014f * u), Offset(right, top + 0.014f * u), strokeWidth = pen.lw * 0.8f)

    // A little rack of sweets on the customer side.
    val rack = Rect(-0.255f * u, -0.16f * u, -0.1f * u, -0.04f * u)
    drawRoundRect(Color(0xFFF7F1E6), rack.topLeft, rack.size, CornerRadius(0.006f * u))
    val sweets = longArrayOf(0xFFFF5A5F, 0xFF3D8BFF, 0xFFFFC83D, 0xFF6BCB77, 0xFFB58CFF, 0xFFFF9F43, 0xFFFF8FC0)
    for (shelf in 0 until 2) {
        val sy = rack.top + 0.055f * u + shelf * 0.058f * u
        for (k in 0 until 5) {
            val x = rack.left + 0.008f * u + k * 0.029f * u
            val c = Color(sweets[(k + shelf * 3) % sweets.size])
            if ((k + shelf) % 3 == 1) {
                val lc = Offset(x + 0.012f * u, sy - 0.03f * u)
                drawLine(Ink.line, lc, Offset(lc.x, sy), strokeWidth = pen.lw * 0.8f)
                inkedCircle(lc, 0.011f * u, c, pen, shade = false)
                drawArc(Color.White, 180f, 180f, false, Offset(lc.x - 0.006f * u, lc.y - 0.006f * u), Size(0.012f * u, 0.012f * u), style = Stroke(pen.lw * 0.6f))
            } else {
                val bar = Rect(x, sy - 0.038f * u, x + 0.024f * u, sy)
                inkedRound(bar, 0.004f * u, c, pen, shade = false)
                drawRect(Color.White.copy(alpha = 0.8f), Offset(bar.left, bar.top + 0.012f * u), Size(bar.width, 0.006f * u))
            }
        }
        capsule(Offset(rack.left, sy), Offset(rack.right, sy), 0.004f * u, ridesSteel, pen)
    }
    drawRoundRect(Ink.line, rack.topLeft, rack.size, CornerRadius(0.006f * u), style = pen.thin)

    // The receipt grows with every scan, then tears off and starts again.
    val len = (0.03f + f.count.mod(9) * 0.012f) * u
    val rx = 0.2f * u
    val paper = Path().apply {
        moveTo(rx - 0.014f * u, top)
        lineTo(rx + 0.014f * u, top)
        lineTo(rx + 0.014f * u, top + len)
        quadraticTo(rx + 0.02f * u, top + len + 0.012f * u, rx + 0.004f * u, top + len + 0.014f * u)
        quadraticTo(rx - 0.01f * u, top + len + 0.012f * u, rx - 0.014f * u, top + len)
        close()
    }
    inked(paper, Color.White, pen, outline = false)
    var ly = top + 0.008f * u
    while (ly < top + len - 0.004f * u) {
        drawLine(Color(0xFF9AA6B8), Offset(rx - 0.009f * u, ly), Offset(rx + (if ((ly / u * 1000f).toInt() % 3 == 0) 0.002f else 0.009f) * u, ly), strokeWidth = pen.lw * 0.45f)
        ly += 0.007f * u
    }
    drawPath(paper, Ink.line, style = pen.thin)
    capsule(Offset(rx - 0.02f * u, top + 0.002f * u), Offset(rx + 0.02f * u, top + 0.002f * u), 0.006f * u, Color(0xFF4A5068), pen)

    // The card terminal on a little swivel stand.
    val tb = Offset(0.1f * u, top) + Oblique.offset(0.02f, u)
    capsule(tb, tb + Offset(0f, -0.018f * u), 0.006f * u, ridesSteelDark, pen)
    rotate(-8f, tb + Offset(0f, -0.04f * u)) {
        val term = Rect(tb.x - 0.017f * u, tb.y - 0.068f * u, tb.x + 0.017f * u, tb.y - 0.014f * u)
        inkedRound(term, 0.006f * u, Color(0xFF2E3446), pen)
        val scr = Rect(term.left + 0.005f * u, term.top + 0.005f * u, term.right - 0.005f * u, term.top + 0.02f * u)
        drawRoundRect(Color(0xFF9CF0C8), scr.topLeft, scr.size, CornerRadius(0.002f * u))
        for (r in 0 until 3) for (c in 0 until 3) {
            drawCircle(if (r == 2 && c == 2) Color(0xFF3BC46B) else Color(0xFFB8C0CF), 0.0028f * u, Offset(term.left + 0.009f * u + c * 0.008f * u, scr.bottom + 0.009f * u + r * 0.008f * u))
        }
    }
}

// ============================================================================================ CART

private val RIDES_CART = longArrayOf(0xFFE8304A, 0xFF2F7BE6)

private class RidesCartShape(u: Float) {
    val bl = Offset(-0.1f * u, -0.12f * u)
    val br = Offset(0.07f * u, -0.12f * u)
    val tr = Offset(0.108f * u, -0.238f * u)
    val tl = Offset(-0.128f * u, -0.238f * u)
    val o = Oblique.offset(0.1f, u)
}

private fun DrawScope.ridesMesh(a: Offset, b: Offset, c: Offset, d: Offset, u: Float, color: Color, width: Float, stepU: Float, stepV: Float) {
    val clip = ridesQuad(a, b, c, d)
    clipPath(clip) {
        // Lines from edge ad to edge bc, and from edge ab to edge dc.
        val n = max(2, ((b - a).getDistance() / (stepU * u)).toInt())
        for (k in 1 until n) {
            val q = k / n.toFloat()
            drawLine(color, a + (b - a) * q, d + (c - d) * q, strokeWidth = width)
        }
        val m = max(2, ((d - a).getDistance() / (stepV * u)).toInt())
        for (k in 1 until m) {
            val q = k / m.toFloat()
            drawLine(color, a + (d - a) * q, b + (c - b) * q, strokeWidth = width)
        }
    }
}

private fun DrawScope.ridesCartBack(f: Fixture, u: Float, pen: Pen) {
    val s = RidesCartShape(u)
    val o = s.o
    val plastic = Color(RIDES_CART[f.variant.mod(2)])
    val chrome = Color(0xFFD5DBE5)
    val wire = Color(0xFF8E97AA)
    val turn = f.shiftX / 0.018f
    groundShadow(0.03f * u, -0.008f * u, 0.3f * u, 0.9f)

    // Far casters and chassis.
    for (x in floatArrayOf(-0.095f, 0.086f)) ridesCaster(Offset(x * u, 0f) + o, u, turn, pen, far = true)
    val railY = -0.036f * u
    capsule(Offset(-0.105f * u, railY) + o, Offset(0.096f * u, railY) + o, 0.006f * u, wire, pen)
    capsule(s.bl + o, Offset(-0.095f * u, railY) + o, 0.006f * u, wire, pen)
    capsule(s.tr + o, Offset(0.09f * u, railY) + o, 0.006f * u, wire, pen)

    // Lower tray.
    val t0 = Offset(-0.09f * u, -0.05f * u)
    val t1 = Offset(0.08f * u, -0.05f * u)
    ridesMesh(t0, t1, t1 + o, t0 + o, u, wire, pen.lw * 0.5f, 0.02f, 0.03f)
    drawPath(ridesQuad(t0, t1, t1 + o, t0 + o), Ink.line.copy(alpha = 0.6f), style = pen.thin)

    // Handle behind the child seat.
    val hA = s.tr + Offset(0.03f * u, -0.034f * u)
    capsule(s.tr + o, hA + o, 0.006f * u, chrome.darken(0.2f), pen)
    capsule(s.tr, hA, 0.006f * u, chrome, pen)
    capsule(hA, hA + o, 0.014f * u, plastic, pen)
    val lock = hA + o * 0.5f
    inkedRound(Rect(lock.x - 0.01f * u, lock.y - 0.008f * u, lock.x + 0.01f * u, lock.y + 0.008f * u), 0.003f * u, ridesGold, pen, shade = false)

    // Far side, floor and ends of the basket.
    ridesMesh(s.tl + o, s.tr + o, s.br + o, s.bl + o, u, wire.copy(alpha = 0.75f), pen.lw * 0.5f, 0.018f, 0.018f)
    drawPath(ridesQuad(s.tl + o, s.tr + o, s.br + o, s.bl + o), Ink.line.copy(alpha = 0.55f), style = pen.thin)
    ridesMesh(s.bl, s.br, s.br + o, s.bl + o, u, wire, pen.lw * 0.5f, 0.02f, 0.025f)
    ridesMesh(s.tl, s.bl, s.bl + o, s.tl + o, u, wire.copy(alpha = 0.7f), pen.lw * 0.45f, 0.02f, 0.025f)
    // The child seat folds down at the back, with a plastic seat back.
    val seatY = -0.16f * u
    val sb0 = Offset(0.06f * u, seatY)
    val sb1 = Offset(s.tr.x - 0.012f * u, seatY)
    inked(ridesQuad(sb0, sb1, sb1 + o, sb0 + o), plastic.darken(0.1f), pen)
    val gate = ridesQuad(Offset(s.tr.x - 0.004f * u, s.tr.y + 0.004f * u), Offset(s.tr.x - 0.004f * u, s.tr.y + 0.004f * u) + o, Offset(0.082f * u, seatY) + o, Offset(0.082f * u, seatY))
    inked(gate, plastic, pen)
    capsule(s.tl + o, s.tr + o, 0.006f * u, chrome.darken(0.15f), pen)
    capsule(s.tl, s.tl + o, 0.006f * u, chrome.darken(0.05f), pen)
    capsule(s.tr, s.tr + o, 0.006f * u, chrome.darken(0.05f), pen)
    capsule(s.bl, s.bl + o, 0.006f * u, chrome.darken(0.1f), pen)

    ridesCartFront(f, u, pen)
}

/** The near wire side and its frame, so what lies in the basket (and a riding child) is inside it. */
private fun DrawScope.ridesCartFront(f: Fixture, u: Float, pen: Pen) {
    val s = RidesCartShape(u)
    val plastic = Color(RIDES_CART[f.variant.mod(2)])
    val chrome = Color(0xFFD5DBE5)
    val wire = Color(0xFF7D8699)
    val turn = f.shiftX / 0.018f

    // Near chassis and casters.
    val railY = -0.036f * u
    capsule(s.bl, Offset(-0.095f * u, railY), 0.007f * u, chrome, pen)
    capsule(s.tr, Offset(0.09f * u, railY), 0.007f * u, chrome, pen)
    capsule(Offset(-0.105f * u, railY), Offset(0.096f * u, railY), 0.007f * u, chrome, pen)
    for (x in floatArrayOf(-0.095f, 0.086f)) ridesCaster(Offset(x * u, 0f), u, turn, pen, far = false)

    // The near side.
    ridesMesh(s.tl, s.tr, s.br, s.bl, u, wire, pen.lw * 0.6f, 0.017f, 0.017f)
    val side = Path().apply {
        moveTo(s.tl.x, s.tl.y)
        lineTo(s.tr.x, s.tr.y)
        lineTo(s.br.x, s.br.y)
        lineTo(s.bl.x, s.bl.y)
        close()
    }
    ridesTube(side, 0.006f * u, chrome, pen)
    drawLine(Color.White, s.tl + Offset(0.01f * u, -0.001f * u), s.tr + Offset(-0.01f * u, -0.001f * u), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
    // Plastic corner bumpers.
    inkedRound(Rect(s.tl.x - 0.012f * u, s.tl.y - 0.01f * u, s.tl.x + 0.014f * u, s.tl.y + 0.014f * u), 0.008f * u, plastic, pen)
    inkedRound(Rect(s.bl.x - 0.01f * u, s.bl.y - 0.012f * u, s.bl.x + 0.012f * u, s.bl.y + 0.01f * u), 0.007f * u, plastic, pen)
}

private fun DrawScope.ridesCaster(c: Offset, u: Float, turn: Float, pen: Pen, far: Boolean) {
    val r = 0.018f * u
    val hub = Offset(c.x, c.y - r)
    val fork = Color(if (far) 0xFF5C6378 else 0xFF8E97AA)
    capsule(Offset(c.x, c.y - 0.036f * u), hub, 0.008f * u, fork, pen)
    val tire = if (far) Color(0xFF2A2735) else ridesRubber
    inkedCircle(hub, r, tire, pen)
    drawCircle(Color(0xFFB8C0CF), r * 0.5f, hub)
    for (k in 0 until 3) {
        val a = turn + k * RIDES_TAU / 3f
        drawLine(Color(0xFF6B7288), hub, hub + Offset(cos(a) * r * 0.5f, sin(a) * r * 0.5f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
    }
    drawCircle(Ink.line, r * 0.5f, hub, style = pen.thin)
    drawCircle(Ink.line, r * 0.14f, hub)
}
