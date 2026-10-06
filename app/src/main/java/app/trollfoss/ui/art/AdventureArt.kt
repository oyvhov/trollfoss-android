package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.AdventurePlay
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/** Level 7 toys, drawn from the bottom centre in scene units × [u]: cable car, diving bell, digger and treasure table. */
private class AdventureKit(val d: DrawScope, val u: Float, val pen: Pen) {
    val wood = Color(0xFFDEA66C); val pale = Color(0xFFFFF0CC); val dark = Color(0xFF564768); val glass = Color(0xFFBFE7F5)
    fun box(l: Float, t: Float, r: Float, b: Float, c: Color) = with(d) {
        val rect = Rect(l * u, t * u, r * u, b * u)
        inkedRound(rect, u * 0.012f, c, pen)
        if (rect.width > pen.lw * 6 && rect.height > pen.lw * 5)
            drawLine(Color.White.copy(alpha = 0.35f), Offset(rect.left + pen.lw * 2, rect.top + pen.lw * 2), Offset(rect.right - pen.lw * 2, rect.top + pen.lw * 2), pen.lw, StrokeCap.Round)
    }
    fun dot(x: Float, y: Float, r: Float, c: Color) = with(d) { inkedCircle(Offset(x * u, y * u), r * u, c, pen) }
    fun line(x: Float, y: Float, xx: Float, yy: Float, c: Color = Ink.line, w: Float = pen.lw) = with(d) { drawLine(c, Offset(x * u, y * u), Offset(xx * u, yy * u), w, StrokeCap.Round) }
    fun fish(x: Float, y: Float, dir: Float, c: Color) = with(d) {
        inked(Path().apply { moveTo((x - dir * 0.03f) * u, y * u); lineTo((x - dir * 0.055f) * u, (y - 0.016f) * u); lineTo((x - dir * 0.055f) * u, (y + 0.016f) * u); close() }, c, pen)
        drawOval(c, Offset((x - 0.03f) * u, (y - 0.014f) * u), Size(0.06f * u, 0.028f * u)); dot(x + dir * 0.015f, y - 0.004f, 0.005f, dark)
    }
}

/** The cable car's line rises gently towards the middle, so the gondola seems to fly. */
private fun lineY(k: Float) = -0.48f - 0.06f * sin(k * PI.toFloat())

fun DrawScope.drawAdventureBack(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in AdventurePlay.TYPES) return false
    val k = AdventureKit(this, u, pen)
    when (f.type) {
        FixtureType.PLAY_CABLE_CAR -> with(k) {
            for (x in floatArrayOf(-0.40f, 0.40f)) { box(x - 0.03f, -0.5f, x + 0.03f, 0f, wood); line(x - 0.06f, 0f, x, -0.2f, wood, pen.lw * 2); line(x + 0.06f, 0f, x, -0.2f, wood, pen.lw * 2); dot(x, -0.5f, 0.022f, ToyColors[3]) }
            var px = -0.40f; var py = lineY(0f)
            for (i in 1..12) { val kk = i / 12f; val x = -0.40f + kk * 0.8f; val y = lineY(kk); line(px, py, x, y, Ink.line, pen.lw * 1.4f); px = x; py = y }
            val gk = (f.angle * 0.72f + 0.04f) / 0.8f
            val gx = -0.36f + f.angle * 0.72f; val gy = lineY(gk)
            line(gx, gy, gx, gy + 0.1f, dark, pen.lw * 1.5f); dot(gx, gy, 0.015f, dark)
            box(gx - 0.075f, gy + 0.1f, gx + 0.075f, gy + 0.27f, ToyColors[0]); box(gx - 0.055f, gy + 0.12f, gx + 0.055f, gy + 0.19f, glass)
        }
        FixtureType.PLAY_DIVING_BELL -> with(k) {
            // The stand stays on the shore; the arm reaches out over the water on whichever side it is.
            val rx = f.reach; val post = if (f.reach < 0f) 0.11f else -0.11f
            val by = -0.26f + f.angle * f.dive
            box(post - 0.02f, -0.44f, post + 0.02f, 0f, wood); line(post, -0.42f, rx, -0.42f, wood, pen.lw * 3); line(rx, -0.42f, rx, by, Ink.line, pen.lw * 1.2f)
            if (f.angle > 0.3f) {
                val t = pen.t
                fish(rx + sin(t * 1.3f) * 0.17f, by + 0.13f, if (kotlin.math.cos(t * 1.3f) > 0f) 1f else -1f, ToyColors[3])
                fish(rx - sin(t * 0.9f + 1f) * 0.15f, by + 0.2f, if (kotlin.math.cos(t * 0.9f + 1f) > 0f) -1f else 1f, ToyColors[1])
                for (i in 0..2) { val bk = (t * 0.6f + i / 3f) % 1f; dot(rx + 0.05f * sin(i * 2f + t), by - bk * 0.15f, 0.008f + i * 0.003f, Color.White) }
            }
            dot(rx, by + 0.02f, 0.11f, ToyColors[3]); box(rx - 0.11f, by + 0.02f, rx + 0.11f, by + 0.26f, ToyColors[3])
            dot(rx, by + 0.12f, 0.055f, glass)
            if (f.angle > 0.6f && pen.t % 5f < 0.8f) heart(d, Offset((rx + 0.08f) * u, (by + 0.06f) * u), 0.022f * u)
        }
        FixtureType.PLAY_DIGGER -> with(k) {
            box(-0.27f, -0.07f, 0.17f, 0f, dark); for (i in 0..4) dot(-0.23f + i * 0.09f, -0.035f, 0.022f, pale)
            box(-0.22f, -0.13f, 0.14f, -0.07f, ToyColors[3]); box(-0.22f, -0.29f, -0.01f, -0.12f, ToyColors[3]); box(-0.19f, -0.26f, -0.04f, -0.16f, glass)
            val dip = sin(f.anim * PI.toFloat()) * 0.05f
            val bx = 0.27f; val by = -0.09f + dip
            line(0.08f, -0.13f, 0.18f, -0.24f, ToyColors[3], pen.lw * 4); line(0.18f, -0.24f, bx, by - 0.04f, ToyColors[3], pen.lw * 4)
            inked(Path().apply { moveTo((bx - 0.04f) * u, (by - 0.06f) * u); lineTo((bx + 0.05f) * u, (by - 0.06f) * u); lineTo((bx + 0.06f) * u, by * u); lineTo((bx - 0.05f) * u, by * u); close() }, Color(0xFF8C8FA3), pen)
        }
        FixtureType.PLAY_TREASURE_TABLE -> with(k) {
            box(-0.2f, -0.18f, -0.16f, 0f, wood); box(0.16f, -0.18f, 0.2f, 0f, wood); box(-0.23f, -0.22f, 0.23f, -0.17f, wood)
            for (i in -1..1) drawOval(dark.copy(alpha = 0.55f), Offset((i * 0.12f - 0.04f) * u, -0.215f * u), Size(0.08f * u, 0.022f * u))
            line(0.2f, -0.22f, 0.2f, -0.26f, dark); dot(0.2f, -0.275f, 0.028f, ToyColors[3]); dot(0.2f, -0.305f, 0.008f, dark)
            if (f.anim > 0.05f) for (i in 0..5) { val a = i * 1.047f + pen.t * 2f; dot(sin(a) * 0.2f, -0.3f - kotlin.math.cos(a) * 0.08f * f.anim, 0.01f, ToyColors[i % 6]) }
        }
        else -> Unit
    }
    return true
}

fun DrawScope.drawAdventureFront(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in AdventurePlay.TYPES) return false
    val k = AdventureKit(this, u, pen)
    when (f.type) {
        FixtureType.PLAY_CABLE_CAR -> with(k) {
            val gk = (f.angle * 0.72f + 0.04f) / 0.8f
            val gx = -0.36f + f.angle * 0.72f; val gy = lineY(gk)
            box(gx - 0.075f, gy + 0.19f, gx + 0.075f, gy + 0.27f, ToyColors[0])
        }
        FixtureType.PLAY_DIVING_BELL -> with(k) {
            val rx = f.reach; val by = -0.26f + f.angle * f.dive
            drawCircle(glass.copy(alpha = 0.35f), 0.055f * u, Offset(rx * u, (by + 0.12f) * u))
        }
        FixtureType.PLAY_DIGGER -> with(k) { line(-0.19f, -0.16f, -0.04f, -0.16f, Ink.line, pen.lw * 1.5f) }
        else -> Unit
    }
    return true
}

private fun heart(d: DrawScope, at: Offset, s: Float) = with(d) {
    val c = Color(0xFFF7A3BD)
    drawCircle(c, s * 0.5f, Offset(at.x - s * 0.4f, at.y)); drawCircle(c, s * 0.5f, Offset(at.x + s * 0.4f, at.y))
    drawPath(Path().apply { moveTo(at.x - s * 0.88f, at.y + s * 0.15f); lineTo(at.x, at.y + s * 1.1f); lineTo(at.x + s * 0.88f, at.y + s * 0.15f); close() }, c)
}
