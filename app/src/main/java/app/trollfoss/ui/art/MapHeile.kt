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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/*
 * The living parts of Heileberget: the cable car with two cabins, its valley station, the zig-zag
 * hiking trail with tiny climbers, goats on the ledges and an eagle wheeling over the crest.
 */

/** The cable car's valley station at the foot of the mountain, with a flag and a big wheel. */
internal fun MapPen.drawCableStation(d: DrawScope, g: MapGeo) {
    val p = g.cableBottom
    val yf = p.y / h
    val sc = S * depthScale(yf) * 0.8f
    d.withTransform({ translate(p.x, p.y + 0.012f * h); scale(sc, sc, Offset.Zero) }) {
        val b = Bx(this, this@drawCableStation, sc, yf)
        with(b) {
            shadowOf(-0.32f, 0f, 0.32f, 0f, 0.7f, 0.1f, 0.9f, 0.12f)
            contact(0f, 0.45f)
            val vx = 0.5f * 0.35f
            val vy = -0.36f * 0.35f
            face(Color(0xFFA0663B).darken(0.24f), 0.3f, -0.3f, 0.3f + vx, -0.3f + vy, 0.3f + vx, vy, 0.3f, 0f)
            box(-0.3f, -0.3f, 0.3f, 0f, Color(0xFFC98A55))
            for (k in 1..4) line(-0.3f, -0.3f * k / 5f, 0.3f, -0.3f * k / 5f, Color(0xFF8C5A32), 0.7f, 0.6f)
            face(Color(0xFF4A4A57), -0.38f, -0.28f, 0f, -0.52f, 0.38f, -0.28f)
            face(Color(0xFF55505E), 0f, -0.52f, 0.38f, -0.28f, 0.38f + vx, -0.28f + vy, vx, -0.52f + vy)
            door(-0.08f, 0.08f, 0.2f, Color(0xFF6E4A33), steps = false)
            win(0.14f, -0.24f, 0.25f, -0.12f)
            d.drawCircle(c(Color(0xFFB9C0CC)), 0.1f, Offset(0.3f + vx + 0.06f, -0.4f))
            d.drawCircle(Ink.line, 0.1f, Offset(0.3f + vx + 0.06f, -0.4f), style = Stroke(lw * 1.6f))
            line(-0.34f, -0.28f, -0.34f, -0.62f, Ink.line, 1.6f)
            val wave = 0f
            fill(path(-0.34f, -0.62f, -0.12f, -0.57f + wave, -0.34f, -0.5f), Color(0xFFE94F4F))
            tufts(-0.5f, 0.45f, y = 0.22f, s = 0.05f)
        }
    }
}

/** The cable with a gentle sag and a lattice pylon half way up (still). */
internal fun MapPen.drawCableStatic(d: DrawScope, g: MapGeo) = with(d) {
    val a = g.cableTop
    val b = g.cableBottom
    val steel = nt(Color(0xFF4A4A57), 0.4f)
    val cable = g.cablePath
    drawPath(cable, Ink.line, alpha = 0.5f, style = Stroke(lw * 2.6f, cap = StrokeCap.Round))
    drawPath(cable, steel, style = Stroke(lw * 1.2f, cap = StrokeCap.Round))
    val pc = g.cablePoint(0.5f)
    val ph = 0.026f * h
    drawLine(Ink.line, Offset(pc.x - ph * 0.45f, pc.y + ph * 1.4f), Offset(pc.x, pc.y - ph * 0.2f), strokeWidth = lw * 2f, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset(pc.x + ph * 0.45f, pc.y + ph * 1.4f), Offset(pc.x, pc.y - ph * 0.2f), strokeWidth = lw * 2f, cap = StrokeCap.Round)
    drawLine(Ink.line, Offset(pc.x - ph * 0.28f, pc.y + ph * 0.7f), Offset(pc.x + ph * 0.28f, pc.y + ph * 0.7f), strokeWidth = lw * 1.4f)
    drawLine(Ink.line, Offset(pc.x - ph * 0.5f, pc.y), Offset(pc.x + ph * 0.5f, pc.y), strokeWidth = lw * 2f, cap = StrokeCap.Round)
}

/** The two cabins gliding along the cable in opposite directions (live). */
internal fun MapPen.drawCabins(d: DrawScope, g: MapGeo) = with(d) {
    val s0 = 0.5f + 0.5f * sin(t * 0.11f)
    for ((k, s) in listOf(s0 to Color(0xFFE94F4F), (1f - s0) to Color(0xFFFFC83D)).withIndex()) {
        val c = g.cablePoint(s.first)
        val cw = h * 0.026f
        val ch = h * 0.022f
        val sw = sin(t * 1.2f + k * 2f) * cw * 0.05f
        drawLine(Ink.line, c, Offset(c.x + sw, c.y + ch * 0.35f), strokeWidth = lw * 1.4f)
        val body = Rect(c.x - cw / 2f + sw, c.y + ch * 0.35f, c.x + cw / 2f + sw, c.y + ch * 1.35f)
        drawRoundRect(Ink.line, Offset(body.left - lw, body.top - lw), Size(body.width + lw * 2f, body.height + lw * 2f), CornerRadius(ch * 0.18f))
        drawRoundRect(nt(s.second, 0.35f), body.topLeft, body.size, CornerRadius(ch * 0.15f))
        drawRect(lerp(Color(0xFFBFE6F5), Color(0xFFFFD76B), ramp((n - 0.3f) / 0.4f)), Offset(body.left + cw * 0.12f, body.top + ch * 0.18f), Size(cw * 0.76f, ch * 0.4f))
        drawLine(Ink.line, Offset(body.left + cw * 0.5f, body.top + ch * 0.18f), Offset(body.left + cw * 0.5f, body.top + ch * 0.58f), strokeWidth = lw * 0.9f)
    }
}

/** The hiking trail: a thin switchback path with little dashes (still). */
internal fun MapPen.drawTrail(d: DrawScope, g: MapGeo) = with(d) {
    val tr = g.trailPath
    drawPath(tr, Ink.line, alpha = 0.3f, style = Stroke(lw * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(g.trailDashes, nt(if (snow) Color.White else Color(0xFFF1E2B8), 0.5f), style = Stroke(lw * 1.2f, cap = StrokeCap.Round))
}

/** Climbers on the hiking trail and goats on the ledges: tiny figures that give the mountain its scale (live). */
internal fun MapPen.drawHeileLife(d: DrawScope, g: MapGeo) = with(d) {
    // Three climbers walking up and down, roped as a pair or alone.
    for (k in 0 until 3) {
        val raw = t * 0.0075f * (if (k == 1) -1f else 1f) + k * 0.31f
        val f = 0.06f + 0.88f * abs(wrap(raw, 2f) - 1f)
        val p = g.trail.at(f)
        val sz = h * 0.02f
        val col = listOf(Color(0xFFE94F4F), Color(0xFF3E7BD6), Color(0xFFFFC83D))[k]
        val step = sin(t * 5f + k * 2f)
        drawLine(Ink.line, Offset(p.x - sz * 0.07f, p.y - sz * 0.3f), Offset(p.x - sz * 0.07f + step * sz * 0.1f, p.y), strokeWidth = sz * 0.13f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset(p.x + sz * 0.07f, p.y - sz * 0.3f), Offset(p.x + sz * 0.07f - step * sz * 0.1f, p.y), strokeWidth = sz * 0.13f, cap = StrokeCap.Round)
        drawRoundRect(Ink.line, Offset(p.x - sz * 0.17f - lw * 0.4f, p.y - sz * 0.72f - lw * 0.4f), Size(sz * 0.34f + lw * 0.8f, sz * 0.44f + lw * 0.8f), CornerRadius(sz * 0.1f))
        drawRoundRect(nt(col, 0.35f), Offset(p.x - sz * 0.17f, p.y - sz * 0.72f), Size(sz * 0.34f, sz * 0.44f), CornerRadius(sz * 0.08f))
        drawRoundRect(nt(Color(0xFF55505E), 0.35f), Offset(p.x - sz * 0.27f, p.y - sz * 0.68f), Size(sz * 0.12f, sz * 0.34f), CornerRadius(sz * 0.05f))
        drawCircle(Ink.line, sz * 0.2f + lw * 0.5f, Offset(p.x, p.y - sz * 0.86f))
        drawCircle(nt(Color(0xFFF2C29B), 0.3f), sz * 0.2f, Offset(p.x, p.y - sz * 0.86f))
        drawArc(nt(Color(0xFFD2443A), 0.3f), 180f, 180f, true, Offset(p.x - sz * 0.22f, p.y - sz * 1.08f), Size(sz * 0.44f, sz * 0.34f))
    }
    // Goats on the ledges.
    for ((i, gp) in listOf(Offset(0.304f, 0.139f), Offset(0.336f, 0.113f), Offset(0.565f, 0.345f), Offset(0.075f, 0.34f)).withIndex()) {
        val c = Offset(gp.x * w, gp.y * h)
        val s = h * 0.02f
        val dip = max(0f, sin(t * 0.7f + i * 2.3f)) * s * 0.18f
        val flip = if (i % 2 == 0) 1f else -1f
        drawOval(Ink.line, Offset(c.x - s * 0.45f, c.y - s * 0.04f), Size(s * 0.9f, s * 0.14f), alpha = 0.2f)
        for (lx in floatArrayOf(-0.28f, -0.14f, 0.14f, 0.28f)) drawLine(Ink.line, Offset(c.x + lx * s, c.y - s * 0.3f), Offset(c.x + lx * s, c.y), strokeWidth = s * 0.1f, cap = StrokeCap.Round)
        drawOval(Ink.line, Offset(c.x - s * 0.42f - lw * 0.4f, c.y - s * 0.62f - lw * 0.4f), Size(s * 0.84f + lw * 0.8f, s * 0.42f + lw * 0.8f))
        drawOval(nt(Color(0xFFFFFDF4), 0.35f), Offset(c.x - s * 0.42f, c.y - s * 0.62f), Size(s * 0.84f, s * 0.42f))
        val hx = c.x + flip * s * 0.46f
        val hy = c.y - s * 0.62f + dip
        drawCircle(Ink.line, s * 0.17f + lw * 0.4f, Offset(hx, hy))
        drawCircle(nt(Color(0xFFFFFDF4), 0.35f), s * 0.16f, Offset(hx, hy))
        drawLine(Ink.line, Offset(hx - flip * s * 0.04f, hy - s * 0.13f), Offset(hx - flip * s * 0.16f, hy - s * 0.3f), strokeWidth = s * 0.07f, cap = StrokeCap.Round)
        drawLine(Ink.line, Offset(hx + flip * s * 0.05f, hy + s * 0.12f), Offset(hx + flip * s * 0.03f, hy + s * 0.26f), strokeWidth = s * 0.06f, cap = StrokeCap.Round)
    }
}

/** An eagle wheeling over the crest on rising air. */
internal fun MapPen.drawEagle(d: DrawScope) = with(d) {
    if (n > 0.8f) return@with
    val a = t * 0.22f
    val c = Offset((0.56f + 0.085f * cos(a)) * w, (0.115f + 0.03f * sin(a)) * h)
    val dirX = -sin(a)
    val s = h * 0.02f
    val tilt = -0.25f * cos(a)
    val col = nt(Color(0xFF2E2838), 0.4f)
    val flip = if (dirX >= 0f) 1f else -1f
    withTransform({ translate(c.x, c.y); scale(flip, 1f, Offset.Zero); rotate(tilt * 20f, Offset.Zero) }) {
        val wing = Path().apply {
            moveTo(-s * 2.6f, -s * 0.2f)
            quadraticTo(-s * 1.4f, -s * 0.85f, -s * 0.2f, -s * 0.3f)
            quadraticTo(0f, -s * 0.2f, s * 0.2f, -s * 0.3f)
            quadraticTo(s * 1.4f, -s * 0.85f, s * 2.6f, -s * 0.2f)
            quadraticTo(s * 1.5f, s * 0.05f, s * 0.5f, s * 0.12f)
            lineTo(-s * 0.5f, s * 0.12f)
            quadraticTo(-s * 1.5f, s * 0.05f, -s * 2.6f, -s * 0.2f)
            close()
        }
        drawPath(wing, col)
        drawPath(wing, Ink.line, style = Stroke(lw * 0.9f, join = StrokeJoin.Round))
        drawOval(col, Offset(-s * 0.18f, -s * 0.28f), Size(s * 0.36f, s * 0.75f))
        drawPath(Path().apply { moveTo(-s * 0.2f, s * 0.4f); lineTo(0f, s * 0.9f); lineTo(s * 0.2f, s * 0.4f); close() }, col)
        drawCircle(nt(Color(0xFFF7F3EC), 0.4f), s * 0.17f, Offset(s * 0.05f, -s * 0.32f))
        drawPath(Path().apply { moveTo(s * 0.17f, -s * 0.34f); lineTo(s * 0.34f, -s * 0.27f); lineTo(s * 0.15f, -s * 0.24f); close() }, nt(Color(0xFFFFC83D), 0.4f))
    }
}
