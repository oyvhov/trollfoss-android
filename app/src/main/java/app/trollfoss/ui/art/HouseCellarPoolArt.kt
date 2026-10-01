package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import app.trollfoss.domain.Fixture
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * The pool and the sauna: the cellar's cedar sauna, the sauna bucket, the shower with its rainbow, the
 * lifebuoy, the springboard, the water slide and a giant rubber duck that two can ride.
 */

// ============================================================================================ sauna

/** The cellar's sauna: a cedar cabin with a big glass-and-timber door. Open it and the benches and the glowing stones show. */
internal fun DrawScope.ceSauna(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.22f
    val cedar = Color(0xFFD08C4C)
    val warm = f.on
    ceShadow(u, 0.38f, d)
    // The right side wall running back, and the flat roof with its overhang.
    for (k in 0 until 9) {
        val y0 = -0.03f - k * 0.0307f
        fxFace(fxDeep(u, 0.19f, y0 - 0.0307f, y0, 0f, d), cedar.darken(0.2f), pen)
    }
    // A little window in the side wall.
    val w0 = q(0.19f, -0.2f, 0.06f)
    val w1 = q(0.19f, -0.2f, 0.16f)
    val w2 = q(0.19f, -0.12f, 0.16f)
    val w3 = q(0.19f, -0.12f, 0.06f)
    drawPath(fxPath(w0, w1, w2, w3), if (warm) Color(0xFFFFD27A) else Color(0xFF8E7A6A))
    drawPath(fxPath(w0, w1, w2, w3), Ink.line, style = pen.stroke)
    // The front wall: planks.
    fxBox(u, -0.19f, -0.3f, 0.19f, -0.012f, d, cedar, pen, rad = 0.004f, top = cedar.lighten(0.2f), side = cedar.darken(0.2f), front = false)
    val front = fxFront(u, -0.19f, -0.3f, 0.19f, -0.012f)
    inkedRound(front, 0.004f * u, cedar, pen)
    for (k in 1 until 9) drawLine(cedar.darken(0.35f), Offset(front.left, front.top + front.height * k / 9f), Offset(front.right, front.top + front.height * k / 9f), strokeWidth = pen.lw * 0.7f)
    // The doorway: a wide opening framed in darker wood.
    val door = Rect(p(-0.14f, -0.265f), p(0.14f, -0.02f))
    drawRect(Color(0xFF3A2418), door.topLeft, door.size)
    if (f.open) {
        // Inside: warm wood, two benches and the heater with its glowing stones.
        clipRect(door.left, door.top, door.right, door.bottom) {
            drawRect(CeWarm.warmWall(door), door.topLeft, door.size)
            for (k in 1 until 7) drawLine(Color(0xFFA0602E), Offset(door.left, door.top + door.height * k / 7f), Offset(door.right, door.top + door.height * k / 7f), strokeWidth = pen.lw * 0.7f)
            fxBox(u, -0.14f, -0.115f, 0.14f, -0.095f, 0.1f, CeC.woodLight, pen, rad = 0.002f, z = 0.04f)
            fxBox(u, -0.14f, -0.06f, 0.14f, -0.042f, 0.08f, CeC.woodLight.darken(0.1f), pen, rad = 0.002f, z = 0.04f)
            val h = p(0.1f, -0.06f)
            fxGlow(h, 0.11f * u, CeC.orange, 0.7f)
            for ((i, s) in floatArrayOf(-0.014f, 0.004f, 0.02f, -0.004f, 0.012f).withIndex()) {
                val c = Offset(h.x + s * u, h.y - (i % 3) * 0.012f * u)
                inkedCircle(c, 0.011f * u, Color(0xFF3A3844), pen, shade = false)
                drawCircle(CeC.orange.copy(alpha = 0.55f), 0.0055f * u, Offset(c.x + 0.002f * u, c.y + 0.002f * u))
            }
        }
        drawRect(Ink.line, door.topLeft, door.size, style = pen.stroke)
        // The door swung to the left: a narrow slab seen edge-on.
        val leaf = fxQuad(-0.14f * u, -0.265f * u, -0.2f * u, -0.25f * u, -0.2f * u, -0.01f * u, -0.14f * u, -0.02f * u, 0.003f * u)
        fxFace(leaf, cedar.darken(0.1f), pen)
        drawOval(Color(0xFFFFE9C4), Offset(-0.188f * u, -0.18f * u), Size(0.04f * u, 0.07f * u))
    } else {
        // The closed door: boards across and a round window, glowing when somebody is inside.
        inkedRound(door, 0.004f * u, cedar.darken(0.08f), pen, shade = false)
        for (k in 1..5) drawLine(cedar.darken(0.4f), Offset(door.left + door.width * k / 6f, door.top), Offset(door.left + door.width * k / 6f, door.bottom), strokeWidth = pen.lw * 0.6f)
        val wc = door.center
        val wr = 0.055f * u
        if (warm) fxGlow(wc, 0.2f * u, Color(0xFFFFC870), 0.55f + 0.1f * sin(t * 3f))
        drawCircle(Ink.line, wr + pen.lw * 1.4f, wc)
        drawCircle(CeC.woodDark, wr + pen.lw * 0.3f, wc)
        drawCircle(if (warm) Color(0xFFFFD27A) else Color(0xFFD9ECF4), wr * 0.85f, wc)
        drawArc(Color.White.copy(alpha = 0.7f), 200f, 50f, false, Offset(wc.x - wr * 0.65f, wc.y - wr * 0.65f), Size(wr * 1.3f, wr * 1.3f), style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
        drawLine(Ink.line, Offset(wc.x - wr * 0.85f, wc.y), Offset(wc.x + wr * 0.85f, wc.y), strokeWidth = pen.lw * 0.6f)
        inkedRound(Rect(door.right - 0.03f * u, door.center.y - 0.04f * u, door.right - 0.02f * u, door.center.y + 0.04f * u), 0.004f * u, CeC.steel, pen, shade = false)
    }
    // A plate over the door with three wavy steam lines, a thermometer and an hour glass.
    val plate = Rect(p(-0.06f, -0.298f), p(0.06f, -0.272f))
    inkedRound(plate, 0.005f * u, Color(0xFFFFF4C2), pen, shade = false)
    for (k in -1..1) {
        val x = plate.center.x + k * 0.03f * u
        val wave = Path().apply {
            moveTo(x, plate.bottom - 0.004f * u)
            quadraticTo(x + 0.008f * u, plate.center.y, x, plate.center.y - 0.002f * u)
            quadraticTo(x - 0.008f * u, plate.top + 0.004f * u, x, plate.top + 0.003f * u)
        }
        drawPath(wave, CeC.orange, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
    }
    val th = p(0.165f, -0.2f)
    drawRoundRect(Color.White, Offset(th.x - 0.004f * u, th.y - 0.05f * u), Size(0.008f * u, 0.07f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u))
    drawRoundRect(Ink.line, Offset(th.x - 0.004f * u, th.y - 0.05f * u), Size(0.008f * u, 0.07f * u), androidx.compose.ui.geometry.CornerRadius(0.004f * u), style = pen.thin)
    drawRect(CeC.orange, Offset(th.x - 0.0025f * u, th.y - (if (warm) 0.04f else 0.02f) * u), Size(0.005f * u, (if (warm) 0.055f else 0.035f) * u))
    // The roof: a flat slab that hangs over, with a steam vent that puffs when it is in use.
    fxBox(u, -0.21f, -0.325f, 0.21f, -0.3f, d + 0.04f, cedar.darken(0.15f), pen, rad = 0.004f, z = -0.02f, top = cedar.lighten(0.1f), side = cedar.darken(0.3f))
    val vent = q(0.12f, -0.325f, 0.12f)
    fxCyl(vent.x, vent.y, vent.y - 0.04f * u, 0.016f * u, 0.016f * u, CeC.steel, pen)
    fxCyl(vent.x, vent.y - 0.04f * u, vent.y - 0.05f * u, 0.022f * u, 0.022f * u, CeC.ironLight, pen)
    if (warm) fxPuffs(vent.x, vent.y - 0.055f * u, t, 0.018f * u, 0.14f * u, CeC.steam, 0.7f, 5, 0.5f, 0.03f * u)
    // A plinth.
    inkedRound(Rect(p(-0.2f, -0.012f), p(0.2f, 0.004f)), 0.003f * u, CeC.iron, pen, shade = false)
}

private object CeWarm {
    fun warmWall(r: Rect) = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFE9A25E), Color(0xFFC77B3F)), startY = r.top, endY = r.bottom)
}

// ====================================================================================== sauna bucket

/** A wooden bucket of water with a ladle, steaming gently. */
internal fun DrawScope.ceSaunaBucket(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    ceShadow(u, 0.1f, 0.08f)
    val cx = 0f
    fxCyl(cx, -0.004f * u, -0.07f * u, 0.036f * u, 0.044f * u, CeC.wood, pen, top = Color(0xFF6EA8D6))
    // Staves and the metal hoops.
    for (k in -2..2) drawLine(CeC.woodDark.copy(alpha = 0.6f), p(k * 0.014f, -0.015f), p(k * 0.017f, -0.065f), strokeWidth = pen.lw * 0.6f)
    for (y in floatArrayOf(-0.018f, -0.055f)) {
        val w = (0.036f + (0.044f - 0.036f) * ((-y - 0.004f) / 0.066f)) * 1.118f
        drawLine(Ink.line, p(-w / 1.118f * 1.0f, y + 0.006f), p(w / 1.118f * 1.0f, y - 0.006f), strokeWidth = 0.007f * u + pen.lw * 1.4f)
        drawLine(CeC.steel, p(-w / 1.118f, y + 0.006f), p(w / 1.118f, y - 0.006f), strokeWidth = 0.007f * u)
    }
    // The ladle leans on the rim.
    val handle = Path().apply { moveTo(p(0.02f, -0.07f).x, p(0.02f, -0.07f).y); lineTo(p(-0.045f, -0.12f).x, p(-0.045f, -0.12f).y) }
    drawPath(handle, Ink.line, style = Stroke(0.008f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(handle, CeC.woodLight, style = Stroke(0.008f * u, cap = StrokeCap.Round))
    inkedCircle(p(0.022f, -0.066f), 0.012f * u, CeC.woodDark, pen, shade = false)
    // A wisp of steam from the water.
    fxPuffs(p(0f, -0.075f).x, p(0f, -0.075f).y, t, 0.01f * u, 0.06f * u, CeC.steam, 0.45f, 2, 0.4f, 0.01f * u)
}

// ============================================================================================ shower

/** A shower on the wall: a rain head on a chrome pipe. Tapped on, it rains with steam and a little rainbow in the spray. */
internal fun DrawScope.ceShower(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val on = f.on
    val chrome = Color(0xFFD8E2EC)
    // The drain tray on the floor.
    val tray = fxDisc2(0.035f * u, -0.004f * u, 0.06f * u, 0.05f * u)
    fxFace(tray, Color(0xFFB9C7D4), pen)
    val drain = fxDisc2(0.035f * u, -0.004f * u, 0.022f * u, 0.018f * u)
    fxFace(drain, CeC.iron, pen)
    // The pipe up to the ceiling pipes, a bend and the head.
    val pipe = Path().apply {
        moveTo(p(-0.045f, 0f).x, p(-0.045f, 0f).y)
        lineTo(p(-0.045f, -0.72f).x, p(-0.045f, -0.72f).y)
    }
    drawPath(pipe, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Butt))
    drawPath(pipe, chrome, style = Stroke(0.012f * u, cap = StrokeCap.Butt))
    drawLine(Color.White.copy(alpha = 0.7f), p(-0.047f, -0.02f), p(-0.047f, -0.7f), strokeWidth = 0.003f * u)
    val arm = Path().apply {
        moveTo(p(-0.045f, -0.4f).x, p(-0.045f, -0.4f).y)
        quadraticTo(p(-0.045f, -0.435f).x, p(-0.045f, -0.435f).y, p(0f, -0.435f).x, p(0f, -0.435f).y)
        lineTo(p(0.03f, -0.435f).x, p(0.03f, -0.435f).y)
    }
    drawPath(arm, Ink.line, style = Stroke(0.012f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(arm, chrome, style = Stroke(0.012f * u, cap = StrokeCap.Round))
    // The rain head: a flat disc seen from the side-front.
    val hc = p(0.035f, -0.43f)
    val head = fxDisc2(hc.x, hc.y, 0.05f * u, 0.04f * u)
    fxFace(head, chrome, pen)
    for (k in -3..3) drawCircle(Ink.line.copy(alpha = 0.5f), 0.0025f * u, Offset(hc.x + k * 0.012f * u, hc.y + 0.006f * u + (k % 2) * 0.004f * u))
    // Two tap handles on the pipe.
    for ((i, c) in listOf(Color(0xFF4D96FF), Color(0xFFFF6F91)).withIndex()) {
        val y = -0.22f - i * 0.05f
        capsule(p(-0.045f, y), p(-0.02f, y), 0.006f * u, chrome, pen)
        inkedCircle(p(-0.012f, y), 0.011f * u, c, pen, shade = false)
    }
    if (on) {
        // Rain falling from the head to the tray, steam rising, and a rainbow in the mist.
        val x = 0.035f
        ceRain(x * u, hc.y + 0.012f * u, -0.004f * u, 0.09f * u, t, 16, Color(0xFF9ADAFF), pen)
        fxPuffs(p(x, -0.1f).x, p(x, -0.1f).y, t, 0.022f * u, 0.14f * u, CeC.steam, 0.5f, 3, 0.4f, 0.03f * u)
        val rb = listOf(Color(0xFFFF5A5F), Color(0xFFFF9F43), Color(0xFFFFD93D), Color(0xFF6BCB77), Color(0xFF4D96FF))
        val center = p(0.115f, -0.07f)
        for ((i, c) in rb.withIndex()) {
            val r = (0.055f - i * 0.006f) * u
            drawArc(c, 190f, 160f, false, Offset(center.x - r, center.y - r), Size(r * 2f, r * 2f), alpha = 0.55f, style = Stroke(0.006f * u))
        }
        val ph = ceFrac(t, 0.7f)
        drawCircle(Color(0xFF9ADAFF), 0.01f * u * (1f + ph), p(x, -0.004f), alpha = 0.5f * (1f - ph), style = Stroke(pen.lw))
    } else {
        val ph = ceFrac(t, 3.2f, f.id * 0.1f)
        if (ph < 0.4f) drawCircle(CeC.water, 0.003f * u, Offset(hc.x, hc.y + 0.012f * u + ph * 1.1f * u), alpha = 1f - ph * 2f)
    }
}

// ========================================================================================== lifebuoy

/** An orange-and-white lifebuoy on a hook; tapped, it swings. */
internal fun DrawScope.ceLifebuoy(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val hook = p(0f, -0.128f)
    val swing = f.angle * 20f + sin(t * 0.8f + f.id) * 1.2f
    rotate(swing, hook) {
        val c = p(0f, -0.066f)
        val ro = 0.062f * u
        val ri = 0.033f * u
        val mid = (ro + ri) / 2f
        val w = ro - ri
        drawCircle(Ink.line, ro + pen.lw, c)
        for (k in 0 until 4) {
            drawArc(if (k % 2 == 0) CeC.orange else Color.White, k * 90f - 45f, 90f, false, Offset(c.x - mid, c.y - mid), Size(mid * 2f, mid * 2f), style = Stroke(w))
        }
        drawCircle(Ink.line, ri - pen.lw * 0.2f, c, style = pen.stroke)
        drawCircle(Ink.line, ro, c, style = pen.stroke)
        drawArc(Color.White.copy(alpha = 0.6f), 200f, 40f, false, Offset(c.x - ro * 0.9f, c.y - ro * 0.9f), Size(ro * 1.8f, ro * 1.8f), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
        // A rope looped round it.
        val rope = Path().apply {
            for (k in 0 until 4) {
                val a = (k * 90f + 45f) * (PI.toFloat() / 180f)
                val x = c.x + cos(a) * (mid + w * 0.55f)
                val y = c.y + sin(a) * (mid + w * 0.55f)
                if (k == 0) moveTo(x, y) else quadraticTo(c.x + cos(a - 0.8f) * (mid + w * 1.2f), c.y + sin(a - 0.8f) * (mid + w * 1.2f), x, y)
            }
        }
        drawPath(rope, Color(0xFFE9C58F), style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
        drawLine(Ink.line, hook, Offset(c.x, c.y - ro), strokeWidth = pen.lw)
    }
    inkedCircle(hook, 0.008f * u, CeC.steel, pen, shade = false)
}

// ===================================================================================== diving board

/** The springboard: a blue stand with a ladder and a long white board that flexes when somebody jumps. */
internal fun DrawScope.ceDivingBoard(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    ceShadow(u, 0.3f, 0.08f, 0.7f)
    val flex = f.angle * 7f
    // The stand: a blue block with chrome rails, and a ladder at the left side.
    fxBox(u, -0.2f, -0.118f, -0.07f, 0f, 0.09f, CeC.waterDeep, pen, rad = 0.006f, top = CeC.water, side = CeC.waterDeep.darken(0.25f))
    for (k in 0 until 3) drawLine(Color.White.copy(alpha = 0.4f), p(-0.185f, -0.03f - k * 0.03f), p(-0.085f, -0.03f - k * 0.03f), strokeWidth = pen.lw)
    for (s in floatArrayOf(-0.215f, -0.185f)) {
        val rail = Path().apply { moveTo(p(s, 0f).x, p(s, 0f).y); lineTo(p(s, -0.14f).x, p(s, -0.14f).y); quadraticTo(p(s, -0.165f).x, p(s, -0.165f).y, p(s + 0.03f, -0.165f).x, p(s + 0.03f, -0.165f).y) }
        drawPath(rail, Ink.line, style = Stroke(0.009f * u + pen.lw * 2f, cap = StrokeCap.Round))
        drawPath(rail, Color(0xFFD8E2EC), style = Stroke(0.009f * u, cap = StrokeCap.Round))
    }
    for (k in 0 until 3) {
        val y = -0.03f - k * 0.04f
        drawLine(Ink.line, p(-0.215f, y), p(-0.185f, y), strokeWidth = 0.008f * u + pen.lw * 1.6f, cap = StrokeCap.Round)
        drawLine(Color(0xFFD8E2EC), p(-0.215f, y), p(-0.185f, y), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    }
    // The board itself, hinged on the stand.
    rotate(flex, p(-0.08f, -0.12f)) {
        fxBox(u, -0.1f, -0.128f, 0.205f, -0.114f, 0.08f, Color(0xFFF4F6F8), pen, rad = 0.005f, top = Color.White, side = Color(0xFFC9D6E4))
        // Non-slip dots and a stripe, and the springy end.
        for (k in 0 until 9) drawCircle(Color(0xFF9ADAFF), 0.0032f * u, p(-0.07f + k * 0.03f, -0.121f))
        drawLine(Ink.line.copy(alpha = 0.5f), p(0.15f, -0.128f), p(0.15f, -0.114f), strokeWidth = pen.lw * 0.7f)
    }
}

// ========================================================================================== the slide

/** A water slide: a tower with a railed platform and a blue trough curving down to the pool, water running all the time. */
internal fun DrawScope.ceSlide(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val d = 0.1f
    ceShadow(u, 0.36f, d)
    // The posts of the tower, and a ladder up its back.
    for (x in floatArrayOf(0.065f, 0.165f)) {
        capsule(p(x, 0f), p(x, -0.4f), 0.011f * u, Color(0xFFD8E2EC), pen)
    }
    for (k in 1..6) drawLine(Ink.line.copy(alpha = 0.6f), p(0.065f, -0.06f * k), p(0.165f, -0.06f * k + 0.01f), strokeWidth = pen.lw * 0.9f)
    // Supports under the trough.
    for (x in floatArrayOf(-0.04f, -0.12f)) {
        val y = -0.4f + 0.285f * (1f - (1f - (0.11f - x) / 0.27f) * (1f - (0.11f - x) / 0.27f))
        capsule(p(x, 0f), p(x, y), 0.008f * u, Color(0xFFD8E2EC), pen)
    }
    // The platform and its railing.
    fxBox(u, 0.03f, -0.41f, 0.2f, -0.395f, d, CeC.waterDeep, pen, rad = 0.004f, top = CeC.water, side = CeC.waterDeep.darken(0.25f))
    val rail = Path().apply { moveTo(p(0.2f, -0.395f).x, p(0.2f, -0.395f).y); lineTo(p(0.2f, -0.46f).x, p(0.2f, -0.46f).y); lineTo(p(0.12f, -0.46f).x, p(0.12f, -0.46f).y) }
    drawPath(rail, Ink.line, style = Stroke(0.007f * u + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(rail, Color(0xFFD8E2EC), style = Stroke(0.007f * u, cap = StrokeCap.Round))
    // The trough: a thick curved band from the platform down to a lip over the water.
    val trough = Path().apply {
        moveTo(p(0.1f, -0.4f).x, p(0.1f, -0.4f).y)
        cubicTo(p(0.0f, -0.4f).x, p(0.0f, -0.4f).y, p(-0.08f, -0.2f).x, p(-0.08f, -0.2f).y, p(-0.17f, -0.115f).x, p(-0.17f, -0.115f).y)
    }
    drawPath(trough, Ink.line, style = Stroke(0.05f * u + pen.lw * 2.4f, cap = StrokeCap.Round))
    drawPath(trough, CeC.waterDeep, style = Stroke(0.05f * u, cap = StrokeCap.Round))
    drawPath(trough, CeC.water, style = Stroke(0.034f * u, cap = StrokeCap.Round))
    drawPath(trough, Color.White.copy(alpha = 0.55f), style = Stroke(0.009f * u, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.03f * u, 0.022f * u), -t * 0.2f * u)))
    // A flag at the top and a drip of water at the lip.
    val pole = p(0.19f, -0.46f)
    drawLine(Ink.line, pole, Offset(pole.x, pole.y - 0.06f * u), strokeWidth = pen.lw)
    val wave = sin(t * 4f) * 0.006f * u
    val flag = Path().apply {
        moveTo(pole.x, pole.y - 0.06f * u)
        quadraticTo(pole.x + 0.02f * u, pole.y - 0.054f * u + wave, pole.x + 0.04f * u, pole.y - 0.045f * u)
        quadraticTo(pole.x + 0.02f * u, pole.y - 0.04f * u - wave, pole.x, pole.y - 0.036f * u)
        close()
    }
    inked(flag, CeC.yellow, pen, shade = false)
    val ph = ceFrac(t * 1.5f, 1f)
    drawCircle(CeC.water, 0.005f * u, p(-0.17f - 0.01f * ph, -0.115f + 0.07f * ph * ph), alpha = 1f - ph)
}

// ============================================================================================== duck

/** A giant rubber duck that floats; two can sit on its back. The front layer is its belly, over their legs. */
internal fun DrawScope.ceDuck(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    val yellow = Color(0xFFFFD447)
    translate(0f, f.bob * u) {
        // The tail, rising at the back.
        val tail = Path().apply {
            moveTo(p(0.11f, -0.06f).x, p(0.11f, -0.06f).y)
            quadraticTo(p(0.15f, -0.07f).x, p(0.15f, -0.07f).y, p(0.155f, -0.115f).x, p(0.155f, -0.115f).y)
            quadraticTo(p(0.12f, -0.095f).x, p(0.12f, -0.095f).y, p(0.09f, -0.09f).x, p(0.09f, -0.09f).y)
            close()
        }
        inked(tail, yellow, pen)
        // The body: a big rounded hull with a hollow on the back where the riders sit.
        val body = Path().apply {
            moveTo(p(-0.125f, -0.06f).x, p(-0.125f, -0.06f).y)
            quadraticTo(p(-0.14f, 0.0f).x, p(-0.14f, 0.0f).y, p(-0.06f, 0.004f).x, p(-0.06f, 0.004f).y)
            lineTo(p(0.08f, 0.004f).x, p(0.08f, 0.004f).y)
            quadraticTo(p(0.16f, 0.0f).x, p(0.16f, 0.0f).y, p(0.14f, -0.06f).x, p(0.14f, -0.06f).y)
            quadraticTo(p(0.13f, -0.085f).x, p(0.13f, -0.085f).y, p(0.1f, -0.078f).x, p(0.1f, -0.078f).y)
            quadraticTo(p(0.0f, -0.055f).x, p(0.0f, -0.055f).y, p(-0.09f, -0.078f).x, p(-0.09f, -0.078f).y)
            quadraticTo(p(-0.12f, -0.085f).x, p(-0.125f, -0.06f).y, p(-0.125f, -0.06f).x, p(-0.125f, -0.06f).y)
            close()
        }
        inked(body, yellow, pen)
        // Two soft dents for the riders.
        for (x in floatArrayOf(-0.07f, 0.075f)) {
            drawOval(yellow.darken(0.12f), p(x - 0.035f, -0.082f), Size(0.07f * u, 0.016f * u))
        }
        // The head, beak, eye and a pink cheek.
        val head = p(-0.125f, -0.1f)
        inkedCircle(head, 0.046f * u, yellow, pen)
        val beak = Path().apply {
            moveTo(p(-0.164f, -0.108f).x, p(-0.164f, -0.108f).y)
            quadraticTo(p(-0.205f, -0.112f).x, p(-0.205f, -0.112f).y, p(-0.2f, -0.094f).x, p(-0.2f, -0.094f).y)
            quadraticTo(p(-0.185f, -0.084f).x, p(-0.185f, -0.084f).y, p(-0.16f, -0.088f).x, p(-0.16f, -0.088f).y)
            close()
        }
        inked(beak, CeC.orange, pen, shade = false)
        drawLine(Ink.line, p(-0.2f, -0.099f), p(-0.17f, -0.099f), strokeWidth = pen.lw * 0.8f)
        val blink = ceFrac(t, 3.6f, f.id * 0.1f) < 0.04f
        if (blink) drawLine(Ink.line, p(-0.14f, -0.112f), p(-0.128f, -0.112f), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round) else {
            drawCircle(Ink.line, 0.0072f * u, p(-0.134f, -0.112f))
            drawCircle(Color.White, 0.0024f * u, p(-0.136f, -0.115f))
        }
        drawCircle(Color(0xFFFF8FB1), 0.008f * u, p(-0.148f, -0.098f), alpha = 0.7f)
        // A wing painted on the side.
        val wing = Path().apply {
            moveTo(p(-0.02f, -0.05f).x, p(-0.02f, -0.05f).y)
            quadraticTo(p(0.03f, -0.07f).x, p(0.03f, -0.07f).y, p(0.07f, -0.04f).x, p(0.07f, -0.04f).y)
            quadraticTo(p(0.03f, -0.015f).x, p(0.03f, -0.015f).y, p(-0.02f, -0.05f).x, p(-0.02f, -0.05f).y)
            close()
        }
        inked(wing, yellow.darken(0.08f), pen, shade = false)
        // A blue stripe on the tail and a squeaker on top.
        drawCircle(CeC.waterDeep, 0.004f * u, p(0.12f, -0.1f))
        // Ripples round the hull where it sits in the water.
        for (k in 0 until 2) {
            val ph = ceFrac(t * 0.5f, 1f, k * 0.5f)
            drawOval(Color.White.copy(alpha = 0.55f * (1f - ph)), p(-0.15f - 0.05f * ph, 0.0f - 0.006f * ph), Size((0.3f + 0.1f * ph) * u, (0.02f + 0.01f * ph) * u), style = pen.thin)
        }
    }
}

/** The duck's belly in front of whoever sits on it, with a cheerful stripe. */
internal fun DrawScope.ceDuckFront(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val yellow = Color(0xFFFFD447)
    translate(0f, f.bob * u) {
        val belly = Path().apply {
            moveTo(p(-0.12f, -0.058f).x, p(-0.12f, -0.058f).y)
            quadraticTo(p(-0.138f, 0.0f).x, p(-0.138f, 0.0f).y, p(-0.06f, 0.004f).x, p(-0.06f, 0.004f).y)
            lineTo(p(0.08f, 0.004f).x, p(0.08f, 0.004f).y)
            quadraticTo(p(0.158f, 0.0f).x, p(0.158f, 0.0f).y, p(0.138f, -0.058f).x, p(0.138f, -0.058f).y)
            quadraticTo(p(0.0f, -0.045f).x, p(0.0f, -0.045f).y, p(-0.12f, -0.058f).x, p(-0.12f, -0.058f).y)
            close()
        }
        inked(belly, yellow, pen)
        clipPath(belly) {
            drawRect(CeC.orange.copy(alpha = 0.85f), p(-0.15f, -0.026f), Size(0.32f * u, 0.012f * u))
        }
        drawPath(belly, Ink.line, style = pen.stroke)
        drawArc(Color.White.copy(alpha = 0.6f), 195f, 50f, false, p(-0.1f, -0.045f), Size(0.06f * u, 0.04f * u), style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    }
}
