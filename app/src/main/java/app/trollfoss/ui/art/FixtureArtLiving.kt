package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
import app.trollfoss.domain.Weather
import kotlin.math.cos
import kotlin.math.sin

// Living room and wall fixtures in oblique 3D: mirror, lamp, TV, radio, piano, windows, mailbox,
// clock, plant and the wood stove.

internal fun DrawScope.fxMirror(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val frame = if (f.place == PlaceId.SALON) FxC.pink else FxC.oak
    val nail = q(0f, -0.186f, 0.014f)
    drawLine(Ink.line, q(-0.02f, -0.162f, 0.004f), nail, pen.lw * 0.6f)
    drawLine(Ink.line, q(0.02f, -0.162f, 0.004f), nail, pen.lw * 0.6f)
    drawCircle(FxC.steel, 0.004f * u, nail)
    val back = fxFront(u, -0.058f, -0.168f, 0.058f, -0.008f, 0.012f)
    drawOval(frame.darken(0.3f), back.topLeft, back.size)
    drawOval(Ink.line, back.topLeft, back.size, style = pen.stroke)
    inkedOval(Rect(-0.058f * u, -0.168f * u, 0.058f * u, -0.008f * u), frame, pen)
    val glassRect = Rect(-0.045f * u, -0.155f * u, 0.045f * u, -0.021f * u)
    val glass = ovalPath(glassRect)
    drawPath(glass, Brush.linearGradient(listOf(Color(0xFFE6F4FB), Color(0xFFB7D6EA), Color(0xFFD9EEF8)), glassRect.topLeft, glassRect.bottomRight))
    clipPath(glass) {
        val cx = (-0.1f + fxFrac(pen.t * 0.2f) * 0.22f) * u
        val band = Path().apply {
            moveTo(cx - 0.008f * u, -0.16f * u)
            lineTo(cx + 0.014f * u, -0.16f * u)
            lineTo(cx - 0.02f * u, -0.015f * u)
            lineTo(cx - 0.042f * u, -0.015f * u)
            close()
        }
        drawPath(band, Color.White.copy(alpha = 0.4f))
        fxLine(p(-0.03f, -0.1f), p(-0.012f, -0.135f), Color.White.copy(alpha = 0.7f), 0.003f * u)
    }
    drawPath(glass, Ink.line, style = pen.thin)
    shine(p(-0.022f, -0.128f), 0.01f * u, 0.02f * u, 0.55f)
    if (f.anim > 0f) twinkle(p(0.024f, -0.12f), 0.018f * u * f.anim, Color.White, f.anim)
}

internal fun DrawScope.fxLamp(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val on = f.on
    val cz = 0.036f
    val base = q(0f, 0f, cz)
    if (on) drawPath(fxDisc(base.x, base.y, 0.14f * u), FxC.warm.copy(alpha = 0.16f + 0.14f * pen.night))
    fxCyl(base.x, base.y, q(0f, -0.013f, cz).y, 0.036f * u, 0.03f * u, FxC.charcoal, pen)
    capsule(q(0f, -0.013f, cz), q(0f, -0.258f, cz), 0.007f * u, FxC.brass, pen)
    val knob = q(0f, -0.144f, cz)
    inkedOval(Rect(knob.x - 0.008f * u, knob.y - 0.006f * u, knob.x + 0.008f * u, knob.y + 0.006f * u), FxC.brass, pen, shade = false)
    val sb = q(0f, -0.258f, cz)
    val st = q(0f, -0.335f, cz)
    val shadeColor = if (on) Color(0xFFFFEDB0) else Color(0xFFF1E4CC)
    if (on) fxGlow(Offset(sb.x, sb.y - 0.04f * u), 0.17f * u, FxC.warm, 0.35f + 0.4f * pen.night)
    fxCyl(sb.x, sb.y, st.y, 0.05f * u, 0.03f * u, shadeColor, pen, top = if (on) Color(0xFFFFF6D6) else shadeColor.darken(0.35f))
    for (k in 0 until 5) {
        val a = FX_PI + (k + 0.5f) * FX_PI / 5f
        val top = Offset(st.x + cos(a) * 0.03f * u + FX_DX * sin(a) * 0.03f * u, st.y + FX_DY * sin(a) * 0.03f * u)
        val bot = Offset(sb.x + cos(a) * 0.05f * u + FX_DX * sin(a) * 0.05f * u, sb.y + FX_DY * sin(a) * 0.05f * u)
        fxLine(top, bot, shadeColor.darken(0.18f).copy(alpha = 0.6f), pen.lw * 0.5f)
    }
    val bandY = sb.y - 0.008f * u
    fxLine(Offset(sb.x - 0.052f * u, bandY + 0.006f * u), Offset(sb.x + 0.052f * u, bandY - 0.009f * u), FxC.terracotta, 0.004f * u)
    if (on) drawOval(Color.White, Offset(sb.x - 0.014f * u, sb.y - 0.004f * u), Size(0.028f * u, 0.008f * u))
    fxLine(Offset(sb.x + 0.022f * u, sb.y), Offset(sb.x + 0.022f * u, sb.y + 0.024f * u), Ink.line, pen.lw * 0.5f)
    drawCircle(FxC.brass, 0.003f * u, Offset(sb.x + 0.022f * u, sb.y + 0.026f * u))
}

// ------------------------------------------------------------------------------------------ TV

internal fun DrawScope.fxTv(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val mode = f.mode.mod(6)
    val cd = 0.14f
    val tz = 0.02f
    val o = q(0f, 0f, tz)
    val inner = Rect(-0.084f * u, -0.271f * u, 0.03f * u, -0.143f * u)
    if (mode != 0) fxGlow(Offset(inner.center.x + o.x, inner.center.y + o.y), 0.22f * u, fxTvTint(mode), 0.14f + 0.28f * pen.night)
    // Low teak cabinet on splayed legs.
    capsule(q(-0.12f, -0.035f, cd - 0.02f), q(-0.134f, 0f, cd - 0.02f), 0.009f * u, FxC.teak.darken(0.2f), pen)
    capsule(q(0.12f, -0.035f, cd - 0.02f), q(0.134f, 0f, cd - 0.02f), 0.009f * u, FxC.teak.darken(0.2f), pen)
    fxBox(u, -0.15f, -0.12f, 0.15f, -0.032f, cd, FxC.teak, pen, rad = 0.006f)
    fxGrain(Rect(-0.145f * u, -0.115f * u, 0.145f * u, -0.037f * u), FxC.teak, pen, 2)
    drawRoundRect(FxC.teak.darken(0.35f), p(-0.14f, -0.11f), Size(0.136f * u, 0.068f * u), CornerRadius(0.004f * u), style = pen.thin)
    drawRoundRect(FxC.teak.darken(0.35f), p(0.004f, -0.11f), Size(0.136f * u, 0.068f * u), CornerRadius(0.004f * u), style = pen.thin)
    drawCircle(FxC.brass, 0.004f * u, p(-0.012f, -0.076f))
    drawCircle(FxC.brass, 0.004f * u, p(0.012f, -0.076f))
    capsule(p(-0.12f, -0.035f), p(-0.136f, 0f), 0.009f * u, FxC.teak.darken(0.15f), pen)
    capsule(p(0.12f, -0.035f), p(0.136f, 0f), 0.009f * u, FxC.teak.darken(0.15f), pen)
    // The set on the cabinet, with its antenna.
    val ant = q(0f, -0.292f, tz + 0.05f)
    fxLine(ant, Offset(ant.x - 0.052f * u, ant.y - 0.076f * u), FxC.steel.darken(0.3f), pen.lw)
    fxLine(ant, Offset(ant.x + 0.046f * u, ant.y - 0.082f * u), FxC.steel.darken(0.3f), pen.lw)
    drawCircle(FxC.steel, 0.004f * u, Offset(ant.x - 0.052f * u, ant.y - 0.076f * u))
    drawCircle(FxC.steel, 0.004f * u, Offset(ant.x + 0.046f * u, ant.y - 0.082f * u))
    drawPath(fxDisc(ant.x, ant.y, 0.016f * u), FxC.charcoal)
    val casing = Color(0xFFEFE6D4)
    fxBox(u, -0.105f, -0.292f, 0.105f, -0.122f, 0.1f, casing, pen, rad = 0.02f, z = tz)
    translate(o.x, o.y) {
        inkedCircle(p(0.068f, -0.256f), 0.011f * u, FxC.charcoal, pen)
        inkedCircle(p(0.068f, -0.222f), 0.009f * u, FxC.charcoal, pen)
        drawLine(Color.White, p(0.068f, -0.256f), p(0.068f + 0.007f * sin(mode * 1.05f), -0.256f - 0.007f * cos(mode * 1.05f)), 0.0025f * u, StrokeCap.Round)
        for (k in 0 until 5) fxLine(p(0.052f, -0.192f + k * 0.01f), p(0.086f, -0.192f + k * 0.01f), FxC.charcoal.copy(alpha = 0.6f), pen.lw * 0.6f)
        val bezel = roundPath(Rect(-0.09f * u, -0.277f * u, 0.036f * u, -0.137f * u), 0.022f * u)
        drawPath(bezel, FxC.charcoal)
        val screen = roundPath(inner, 0.018f * u)
        clipPath(screen) {
            fxTvScreen(mode, inner, u, pen)
            for (k in 1 until 8) {
                val y = inner.top + inner.height * k / 8f
                drawLine(Color.Black.copy(alpha = 0.07f), Offset(inner.left, y), Offset(inner.right, y), pen.lw * 0.5f)
            }
        }
        drawPath(screen, Ink.line, style = pen.thin)
        drawPath(bezel, Ink.line, style = pen.stroke)
        drawArc(
            Color.White.copy(alpha = 0.35f), 195f, 60f, false,
            Offset(inner.left + inner.width * 0.08f, inner.top + inner.height * 0.1f), Size(inner.width * 0.6f, inner.height * 0.7f),
            style = Stroke(0.004f * u, cap = StrokeCap.Round),
        )
    }
}

private fun fxTvTint(mode: Int): Color = when (mode) {
    1 -> Color(0xFFFFFFFF)
    2 -> Color(0xFF4FB3F0)
    3 -> Color(0xFF6FD08C)
    4 -> Color(0xFFE56FD8)
    else -> FxC.auroraGreen
}

private fun DrawScope.fxTvScreen(mode: Int, r: Rect, u: Float, pen: Pen) {
    val t = pen.t
    val w = r.width
    val h = r.height
    fun q(fx: Float, fy: Float) = Offset(r.left + fx * w, r.top + fy * h)
    when (mode) {
        0 -> {
            drawRect(Color(0xFF2F3A39), r.topLeft, r.size)
            drawCircle(Color.White.copy(alpha = 0.08f), h * 0.3f, q(0.5f, 0.5f))
        }
        1 -> {
            val bars = arrayOf(Color(0xFFF2F2F2), Color(0xFFF7E14A), Color(0xFF4FE0E8), Color(0xFF55D66B), Color(0xFFE05AD8), Color(0xFFE8473F), Color(0xFF3D5BE8))
            val bw = w / bars.size
            for (i in bars.indices) drawRect(bars[i], Offset(r.left + i * bw, r.top), Size(bw + 1f, h * 0.78f))
            for (i in 0 until 4) drawRect(if (i % 2 == 0) Color(0xFF1C2250) else Color(0xFFF2F2F2), Offset(r.left + i * w / 4f, r.top + h * 0.78f), Size(w / 4f + 1f, h * 0.22f))
        }
        2 -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF4FB8EA), Color(0xFF1C5C99)), r.top, r.bottom), r.topLeft, r.size)
            drawRect(FxC.sand, q(0f, 0.86f), Size(w, h * 0.14f))
            for (k in 0 until 2) {
                val weed = Path().apply {
                    val x0 = r.left + w * (0.2f + k * 0.55f)
                    moveTo(x0, r.bottom)
                    quadraticTo(x0 + sin(t * 1.5f + k) * w * 0.05f, r.top + h * 0.7f, x0 + sin(t * 1.2f + k) * w * 0.03f, r.top + h * 0.45f)
                }
                drawPath(weed, FxC.spruceLight, style = Stroke(0.005f * u, cap = StrokeCap.Round))
            }
            fxTvFish(Offset(r.left - w * 0.15f + fxFrac(t * 0.09f) * w * 1.3f, r.top + h * (0.38f + 0.04f * sin(t * 2f))), 0.012f * u, Color(0xFFFF9A3D), 1f)
            fxTvFish(Offset(r.right + w * 0.15f - fxFrac(t * 0.07f + 0.4f) * w * 1.3f, r.top + h * (0.62f + 0.03f * sin(t * 1.7f))), 0.01f * u, FxC.yellow, -1f)
            for (k in 0 until 3) {
                val ph = fxFrac(t * 0.4f + k * 0.33f)
                drawCircle(Color.White.copy(alpha = 0.7f), 0.0025f * u, q(0.7f + k * 0.05f, 0.85f - ph * 0.8f), style = Stroke(pen.lw * 0.5f))
            }
        }
        3 -> {
            drawRect(Color(0xFF4F8FD6), r.topLeft, r.size)
            val land = blobPath(
                r.left + w * 0.05f, r.bottom, r.left + w * 0.2f, r.top + h * 0.6f, r.left + w * 0.42f, r.top + h * 0.3f,
                r.left + w * 0.62f, r.top + h * 0.05f, r.left + w * 0.72f, r.top + h * 0.2f, r.left + w * 0.52f, r.top + h * 0.55f,
                r.left + w * 0.35f, r.bottom,
            )
            drawPath(land, Color(0xFF6FC06A))
            drawPath(land, Color(0xFF3F8A4A), style = pen.thin)
            val sun = q(0.3f, 0.45f)
            rotate(t * 20f, sun) {
                for (k in 0 until 8) {
                    val a = k * FX_PI / 4f
                    drawLine(FxC.yellow, Offset(sun.x + cos(a) * 0.012f * u, sun.y + sin(a) * 0.012f * u), Offset(sun.x + cos(a) * 0.017f * u, sun.y + sin(a) * 0.017f * u), 0.0025f * u, StrokeCap.Round)
                }
            }
            drawCircle(FxC.yellow, 0.009f * u, sun)
            val cl = q(0.68f + 0.03f * sin(t * 0.5f), 0.55f)
            for (k in 0 until 3) drawLine(Color(0xFF2F6FB8), Offset(cl.x - 0.008f * u + k * 0.008f * u, cl.y + 0.008f * u), Offset(cl.x - 0.011f * u + k * 0.008f * u, cl.y + 0.016f * u), 0.0022f * u, StrokeCap.Round)
            drawCircle(Color.White, 0.008f * u, Offset(cl.x - 0.007f * u, cl.y))
            drawCircle(Color.White, 0.01f * u, Offset(cl.x + 0.002f * u, cl.y - 0.004f * u))
            drawCircle(Color.White, 0.007f * u, Offset(cl.x + 0.011f * u, cl.y + 0.001f * u))
        }
        4 -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFE85AB8), Color(0xFF5B2A9E)), r.top, r.bottom), r.topLeft, r.size)
            fxGlow(q(0.5f, 0.35f), h * 0.6f, Color.White, 0.35f)
            val d = Color(0xFF2B1840)
            val sway = sin(t * 6f)
            val hip = q(0.5f + sway * 0.03f, 0.68f)
            val neck = q(0.5f - sway * 0.02f, 0.38f)
            drawLine(d, hip, neck, 0.008f * u, StrokeCap.Round)
            drawCircle(d, 0.011f * u, Offset(neck.x, neck.y - 0.012f * u))
            drawLine(d, neck, Offset(neck.x - 0.022f * u, neck.y - 0.01f * u - sway * 0.014f * u), 0.005f * u, StrokeCap.Round)
            drawLine(d, neck, Offset(neck.x + 0.022f * u, neck.y - 0.01f * u + sway * 0.014f * u), 0.005f * u, StrokeCap.Round)
            drawLine(d, hip, Offset(hip.x - 0.012f * u, r.bottom - h * 0.06f - (if (sway > 0f) sway * 0.01f * u else 0f)), 0.006f * u, StrokeCap.Round)
            drawLine(d, hip, Offset(hip.x + 0.012f * u, r.bottom - h * 0.06f + (if (sway < 0f) sway * 0.01f * u else 0f)), 0.006f * u, StrokeCap.Round)
            for (k in 0 until 4) drawCircle(Color.White.copy(alpha = 0.5f + 0.5f * sin(t * 5f + k * 1.7f)), 0.002f * u, q(0.12f + k * 0.25f, 0.12f + (k % 2) * 0.08f))
        }
        else -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0C1438), Color(0xFF26306A)), r.top, r.bottom), r.topLeft, r.size)
            for (k in 0 until 6) drawCircle(Color.White.copy(alpha = 0.5f + 0.4f * sin(t * 2f + k)), 0.0018f * u, q(0.08f + k * 0.16f, 0.1f + (k * 37 % 5) * 0.05f))
            for (band in 0 until 2) {
                val aur = Path()
                for (k in 0..10) {
                    val fx = k / 10f
                    val y = r.top + h * (0.34f + band * 0.14f + 0.08f * sin(fx * 7f + t * (1.1f + band * 0.4f)))
                    if (k == 0) aur.moveTo(r.left + fx * w, y) else aur.lineTo(r.left + fx * w, y)
                }
                drawPath(aur, (if (band == 0) FxC.auroraGreen else FxC.auroraPink).copy(alpha = 0.7f), style = Stroke(0.01f * u, cap = StrokeCap.Round))
            }
            drawPath(
                blobPath(r.left - w * 0.1f, r.bottom + h * 0.1f, r.left + w * 0.25f, r.top + h * 0.7f, r.left + w * 0.5f, r.top + h * 0.85f, r.left + w * 0.8f, r.top + h * 0.62f, r.right + w * 0.1f, r.bottom + h * 0.1f),
                Color(0xFF0A0F24),
            )
        }
    }
}

private fun DrawScope.fxTvFish(c: Offset, s: Float, color: Color, dir: Float) {
    val tail = Path().apply {
        moveTo(c.x - dir * s * 0.8f, c.y)
        lineTo(c.x - dir * s * 1.5f, c.y - s * 0.6f)
        lineTo(c.x - dir * s * 1.5f, c.y + s * 0.6f)
        close()
    }
    drawPath(tail, color.darken(0.1f))
    drawOval(color, Offset(c.x - s, c.y - s * 0.55f), Size(s * 2f, s * 1.1f))
    drawCircle(Ink.line, s * 0.15f, Offset(c.x + dir * s * 0.5f, c.y - s * 0.1f))
}

// ------------------------------------------------------------------------------------------ radio, piano

internal fun DrawScope.fxRadio(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.05f
    val tip = q(0.068f, -0.118f, d - 0.01f)
    fxLine(q(0.036f, -0.066f, d - 0.01f), tip, FxC.steel.darken(0.3f), pen.lw)
    drawCircle(FxC.steel, 0.003f * u, tip)
    val h = q(0f, 0f, d * 0.5f)
    translate(h.x, h.y) {
        drawArc(Ink.line, 180f, 180f, false, p(-0.03f, -0.084f), Size(0.06f * u, 0.036f * u), style = Stroke(0.006f * u + pen.lw * 2f))
        drawArc(FxC.charcoal, 180f, 180f, false, p(-0.03f, -0.084f), Size(0.06f * u, 0.036f * u), style = Stroke(0.006f * u))
    }
    fxBox(u, -0.05f, -0.066f, 0.05f, 0f, d, FxC.teak, pen, rad = 0.012f)
    val grille = Rect(-0.042f * u, -0.056f * u, 0.004f * u, -0.008f * u)
    inkedRound(grille, 0.008f * u, FxC.cream, pen, shade = false)
    val pulse = if (f.on) 1f + 0.14f * sin(t * 14f) else 1f
    drawCircle(FxC.cream.darken(0.16f), 0.017f * u * pulse, grille.center)
    drawCircle(FxC.cream.darken(0.3f), 0.006f * u * pulse, grille.center)
    for (k in 0 until 4) {
        val x = -0.035f + k * 0.0115f
        fxLine(p(x, -0.054f), p(x, -0.01f), FxC.teak.darken(0.1f), 0.004f * u)
    }
    val dial = Rect(0.01f * u, -0.056f * u, 0.042f * u, -0.036f * u)
    if (f.on) fxGlow(dial.center, 0.03f * u, FxC.warm, 0.5f + 0.3f * pen.night)
    inkedRound(dial, 0.004f * u, if (f.on) Color(0xFFFFF0B0) else FxC.cream, pen, shade = false)
    for (k in 0 until 5) fxLine(p(0.014f + k * 0.006f, -0.041f), p(0.014f + k * 0.006f, -0.045f), Ink.line, pen.lw * 0.4f)
    val nx = 0.018f + (if (f.on) 0.012f + sin(t * 0.5f) * 0.003f else 0.004f)
    fxLine(p(nx, -0.054f), p(nx, -0.038f), FxC.red, 0.0022f * u)
    inkedCircle(p(0.018f, -0.02f), 0.008f * u, FxC.charcoal, pen)
    inkedCircle(p(0.036f, -0.02f), 0.008f * u, FxC.charcoal, pen)
    if (f.on) {
        for (k in 0 until 2) {
            val ph = fxFrac(t * 1.2f + k * 0.5f)
            val r = (0.03f + ph * 0.03f) * u
            drawArc(Ink.line.copy(alpha = 0.5f * (1f - ph)), 150f, 60f, false, Offset(grille.center.x - r, grille.center.y - r), Size(r * 2f, r * 2f), style = pen.thin)
        }
        val notes = arrayOf(FxC.red, FxC.fjord, FxC.spruceLight)
        for (k in 0 until 3) {
            val ph = fxFrac(t * 0.5f + k / 3f)
            val a = (1f - ph) * (ph * 6f).coerceAtMost(1f)
            fxNote(p(-0.025f + k * 0.022f + sin(ph * 6f + k) * 0.012f, -0.08f - ph * 0.12f), 0.009f * u, notes[k], a)
        }
    }
}

/**
 * An upright piano built like a little hutch: the lid runs forward over the keys, so things set on
 * top stand at its front edge, and the keys lie in front of the music desk.
 */
internal fun DrawScope.fxPiano(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val wood = FxC.walnut
    val d = 0.15f
    val kd = 0.07f
    fxBox(u, -0.15f, -0.105f, 0.15f, -0.008f, d - 0.05f, wood.darken(0.18f), pen, z = 0.04f)
    val lower = fxFront(u, -0.15f, -0.105f, 0.15f, -0.008f, 0.04f)
    drawRoundRect(wood.lighten(0.12f), Offset(lower.left + 0.02f * u, lower.top + 0.012f * u), Size(0.12f * u, 0.075f * u), CornerRadius(0.004f * u), style = pen.thin)
    drawRoundRect(wood.lighten(0.12f), Offset(lower.left + 0.16f * u, lower.top + 0.012f * u), Size(0.12f * u, 0.075f * u), CornerRadius(0.004f * u), style = pen.thin)
    fxBox(u, -0.026f, -0.012f, -0.01f, -0.006f, 0.04f, FxC.brass, pen, z = 0.004f)
    fxBox(u, 0.01f, -0.012f, 0.026f, -0.006f, 0.04f, FxC.brass, pen, z = 0.004f)
    // The music desk behind the keys.
    fxBox(u, -0.152f, -0.25f, 0.152f, -0.13f, d - kd, wood, pen, z = kd)
    translate(q(0f, 0f, kd).x, q(0f, 0f, kd).y) {
        inkedRound(Rect(-0.05f * u, -0.215f * u, 0.05f * u, -0.165f * u), 0.003f * u, FxC.paint, pen, shade = false)
        for (g in 0 until 2) {
            for (k in 0 until 3) {
                val y = -0.206f + g * 0.02f + k * 0.004f
                fxLine(p(-0.042f, y), p(0.042f, y), Ink.line.copy(alpha = 0.35f), pen.lw * 0.35f)
            }
            for (k in 0 until 4) drawOval(Ink.line, p(-0.034f + k * 0.02f, -0.204f + g * 0.02f + (k % 2) * 0.003f), Size(0.005f * u, 0.0035f * u))
        }
        fxGrain(Rect(-0.14f * u, -0.245f * u, 0.14f * u, -0.22f * u), wood, pen, 1)
    }
    // The left cheek of the case, the keys, then the right cheek.
    fxBox(u, -0.172f, -0.25f, -0.152f, -0.105f, d, wood, pen, rad = 0.004f)
    fxLeg(u, -0.162f, pen, wood)
    val pressed = if (f.anim > 0f) f.mode.coerceIn(0, 9) else -1
    for (i in 0 until 10) {
        val x0 = -0.15f + i * 0.03f
        val dip = if (i == pressed) 0.004f * f.anim else 0f
        val col = if (i == pressed) Color(0xFFDDE4F2) else Color.White
        fxFace(fxFlat(u, x0 + 0.0008f, x0 + 0.0292f, -0.13f + dip, 0f, kd, 0.002f), col, pen)
        inkedRound(Rect((x0 + 0.0008f) * u, (-0.13f + dip) * u, (x0 + 0.0292f) * u, (-0.121f + dip) * u), 0.002f * u, col.darken(0.08f), pen, shade = false)
    }
    for (i in intArrayOf(0, 1, 3, 4, 5, 7, 8)) {
        val cx = -0.15f + (i + 1) * 0.03f
        fxBox(u, cx - 0.008f, -0.138f, cx + 0.008f, -0.13f, kd - 0.028f, Color(0xFF231C2E), pen, z = 0.028f, top = Color(0xFF3A3048), side = Color(0xFF15101C))
    }
    fxBox(u, -0.152f, -0.121f, 0.152f, -0.105f, 0.02f, wood.lighten(0.05f), pen)
    fxBox(u, 0.152f, -0.25f, 0.172f, -0.105f, d, wood, pen, rad = 0.004f)
    fxLeg(u, 0.162f, pen, wood)
    // The lid, with a plain runner hanging over its front edge.
    fxBox(u, -0.172f, -0.26f, 0.172f, -0.25f, d, wood.lighten(0.08f), pen, rad = 0.003f)
    fxFace(fxFlat(u, -0.11f, 0.11f, -0.2605f, 0f, 0.05f), FxC.paint, pen)
    val scallops = FloatArray(11 * 3)
    for (k in 0 until 11) {
        scallops[k * 3] = (-0.1f + k * 0.02f) * u
        scallops[k * 3 + 1] = -0.252f * u
        scallops[k * 3 + 2] = 0.0055f * u
    }
    fxCloud(FxC.paint, pen, false, *scallops)
    drawRect(FxC.paint, p(-0.11f, -0.2605f), Size(0.22f * u, 0.0095f * u))
    if (pressed >= 0) {
        fxNote(p(-0.15f + pressed * 0.03f + 0.012f, -0.17f - (1f - f.anim) * 0.07f), 0.011f * u, FxC.fjord, f.anim)
    }
}

private fun DrawScope.fxLeg(u: Float, x: Float, pen: Pen, wood: Color) {
    fxPost(u, x, 0.012f, 0f, -0.105f, 0.008f, wood, pen)
    fxBox(u, x - 0.012f, -0.012f, x + 0.012f, 0f, 0.024f, wood.darken(0.1f), pen)
}

// ------------------------------------------------------------------------------------------ windows

internal fun DrawScope.fxWindow(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    if (f.variant == 1) {
        fxWaterfallWindow(u, pen)
        return
    }
    val frame = FxC.paint
    val fabric = when (f.place) {
        PlaceId.CAFE -> FxC.mint
        PlaceId.SALON -> FxC.pink
        else -> FxC.dusty
    }
    val gd = 0.03f
    fxBox(u, -0.1f, -0.22f, 0.1f, -0.012f, 0.02f, frame, pen, rad = 0.006f)
    // Looking out through the deep window reveal.
    val glass = Rect(-0.08f * u, -0.202f * u, 0.08f * u, -0.03f * u)
    val o = q(0f, 0f, gd)
    clipRect(glass.left, glass.top, glass.right, glass.bottom) {
        translate(o.x, o.y) {
            fxSky(glass, u, pen, f.id * 0.37f)
            drawRect(frame, p(-0.005f, -0.202f), Size(0.01f * u, 0.172f * u))
            drawRect(frame, p(-0.08f, -0.121f), Size(0.16f * u, 0.01f * u))
            drawRect(Ink.line, p(-0.005f, -0.202f), Size(0.01f * u, 0.172f * u), style = pen.thin)
            drawRect(Ink.line, p(-0.08f, -0.121f), Size(0.16f * u, 0.01f * u), style = pen.thin)
            fxLine(p(-0.07f, -0.176f), p(-0.056f, -0.194f), Color.White.copy(alpha = 0.55f), 0.003f * u)
            fxLine(p(-0.07f, -0.16f), p(-0.045f, -0.192f), Color.White.copy(alpha = 0.35f), 0.002f * u)
        }
        val lx = glass.left
        val by = glass.bottom
        drawPath(fxQuad(lx, glass.top, lx + o.x, glass.top + o.y, lx + o.x, by + o.y, lx, by), frame.darken(0.12f))
        drawPath(fxQuad(lx, by, lx + o.x, by + o.y, glass.right + o.x, by + o.y, glass.right, by), frame.darken(0.05f))
        drawLine(Ink.line.copy(alpha = 0.5f), Offset(lx + o.x, glass.top), Offset(lx + o.x, by + o.y), pen.lw * 0.6f)
    }
    drawRect(Ink.line, glass.topLeft, glass.size, style = pen.stroke)
    fxBox(u, -0.112f, -0.026f, 0.112f, -0.006f, 0.05f, frame.darken(0.04f), pen, rad = 0.003f, z = -0.03f)
    // A little plant on the sill.
    val pot = q(-0.073f, -0.026f, -0.012f)
    translate(pot.x + 0.073f * u, pot.y + 0.026f * u) {
        for (k in 0 until 3) {
            val a = (-0.6f + k * 0.6f)
            val leaf = fxLeaf(-0.073f * u, -0.045f * u, (-0.073f + sin(a) * 0.022f) * u, (-0.045f - cos(a) * 0.022f) * u, 0.3f)
            inked(leaf, FxC.spruceLight, pen, shade = false)
        }
        inked(fxPoly(u, -0.085f, -0.046f, -0.061f, -0.046f, -0.065f, -0.026f, -0.081f, -0.026f), FxC.terracotta, pen, shade = false)
    }
    // Curtain rod and curtains hanging a little in front of the window.
    val cz = -0.02f
    val co = q(0f, 0f, cz)
    fxLine(q(-0.118f, -0.226f, cz), q(-0.118f, -0.226f, 0f), FxC.charcoal, pen.lw)
    fxLine(q(0.118f, -0.226f, cz), q(0.118f, -0.226f, 0f), FxC.charcoal, pen.lw)
    translate(co.x, co.y) {
        capsule(p(-0.122f, -0.226f), p(0.122f, -0.226f), 0.005f * u, FxC.oak, pen)
        inkedCircle(p(-0.124f, -0.226f), 0.006f * u, FxC.oak, pen, shade = false)
        inkedCircle(p(0.124f, -0.226f), 0.006f * u, FxC.oak, pen, shade = false)
        for (s in 0..1) {
            val m = if (s == 0) 1f else -1f
            val panel = Path()
            if (f.mode == 1) {
                panel.moveTo(-0.12f * m * u, -0.222f * u)
                panel.lineTo(0.003f * m * u, -0.222f * u)
                panel.lineTo(0.004f * m * u, -0.02f * u)
                panel.quadraticTo(-0.06f * m * u, -0.013f * u, -0.12f * m * u, -0.02f * u)
                panel.close()
            } else {
                panel.moveTo(-0.122f * m * u, -0.222f * u)
                panel.lineTo(-0.068f * m * u, -0.222f * u)
                panel.quadraticTo(-0.08f * m * u, -0.16f * u, -0.094f * m * u, -0.125f * u)
                panel.quadraticTo(-0.07f * m * u, -0.07f * u, -0.078f * m * u, -0.018f * u)
                panel.lineTo(-0.122f * m * u, -0.018f * u)
                panel.close()
            }
            inked(panel, fabric, pen, outline = false)
            clipPath(panel) {
                val folds = if (f.mode == 1) 4 else 2
                for (k in 1..folds) {
                    val fx = if (f.mode == 1) (-0.12f + k * 0.025f) * m else (-0.118f + k * 0.015f) * m
                    val fold = Path().apply {
                        moveTo(fx * u, -0.222f * u)
                        quadraticTo((fx + 0.006f * m) * u, -0.12f * u, fx * u, -0.015f * u)
                    }
                    drawPath(fold, fabric.darken(0.22f), style = pen.thin)
                }
                for (k in 0 until 6) fxLine(p(-0.13f * m, -0.2f + k * 0.034f), p(0.01f * m, -0.2f + k * 0.034f), Color.White.copy(alpha = 0.35f), 0.003f * u)
            }
            drawPath(panel, Ink.line, style = pen.stroke)
            if (f.mode != 1) inkedRound(Rect((if (m > 0) -0.108f else 0.084f) * u, -0.13f * u, (if (m > 0) -0.084f else 0.108f) * u, -0.12f * u), 0.004f * u, fabric.darken(0.2f), pen, shade = false)
        }
    }
}

/** The view out of a window: sky by time and weather, far hills, sun or moon and stars, rainbow. */
private fun DrawScope.fxSky(r: Rect, u: Float, pen: Pen, seed: Float) {
    val t = pen.t
    val night = pen.night
    val rain = pen.weather == Weather.RAIN
    val snow = pen.weather == Weather.SNOW
    var top = lerp(Color(0xFF6CC0F5), Color(0xFF141A48), night)
    var low = lerp(Color(0xFFD5F0FF), Color(0xFF3B2F7A), night)
    if (rain) {
        top = lerp(top, Color(0xFF7D8799), 0.55f * (1f - night * 0.5f))
        low = lerp(low, Color(0xFFB4BCC8), 0.5f * (1f - night * 0.5f))
    }
    if (snow) {
        top = lerp(top, Color(0xFFB7C4D6), 0.4f * (1f - night * 0.5f))
        low = lerp(low, Color(0xFFEFF3F8), 0.4f * (1f - night * 0.5f))
    }
    drawRect(Brush.verticalGradient(listOf(top, low), r.top, r.bottom), r.topLeft, r.size)
    val w = r.width
    val h = r.height
    fun q(fx: Float, fy: Float) = Offset(r.left + fx * w, r.top + fy * h)
    if (night > 0.05f) {
        for (k in 0 until 6) {
            val a = night * (0.55f + 0.45f * sin(t * 2f + k * 1.9f))
            twinkle(q(0.1f + (k * 0.37f + seed) % 0.8f, 0.08f + ((k * 53) % 7) * 0.06f), 0.004f * u, Color.White, a)
        }
        val aur = Path()
        for (k in 0..6) {
            val fx = k / 6f
            val y = r.top + h * (0.3f + 0.06f * sin(fx * 6f + t * 0.8f + seed))
            if (k == 0) aur.moveTo(r.left + fx * w, y) else aur.lineTo(r.left + fx * w, y)
        }
        drawPath(aur, FxC.auroraGreen.copy(alpha = 0.35f * night), style = Stroke(0.012f * u, cap = StrokeCap.Round))
        val moon = q(0.72f, 0.24f)
        drawCircle(Color(0xFFFFF4C8).copy(alpha = night), 0.014f * u, moon)
        drawCircle(top.copy(alpha = night), 0.012f * u, Offset(moon.x + 0.007f * u, moon.y - 0.004f * u))
    }
    if (!rain && night < 0.95f) {
        val sun = q(0.74f, 0.26f)
        fxGlow(sun, 0.035f * u, FxC.yellow, 0.6f * (1f - night))
        drawCircle(Color(0xFFFFD84A).copy(alpha = 1f - night), 0.012f * u, sun)
    }
    val cloudA = (if (rain) 0.95f else 0.85f) * (1f - night * 0.7f)
    val cloudC = if (rain) Color(0xFFD5DAE2) else Color.White
    for (k in 0 until 2) {
        val cx = r.left + fxFrac(t * 0.012f + seed + k * 0.55f) * (w + 0.08f * u) - 0.04f * u
        val cy = r.top + h * (0.2f + k * 0.18f)
        drawCircle(cloudC.copy(alpha = cloudA), 0.01f * u, Offset(cx - 0.01f * u, cy + 0.002f * u))
        drawCircle(cloudC.copy(alpha = cloudA), 0.013f * u, Offset(cx + 0.002f * u, cy - 0.003f * u))
        drawCircle(cloudC.copy(alpha = cloudA), 0.009f * u, Offset(cx + 0.014f * u, cy + 0.002f * u))
    }
    if (pen.rainbow > 0.01f) {
        val cols = arrayOf(Color(0xFFFF5A4E), Color(0xFFFF9F43), Color(0xFFFFD23F), Color(0xFF3BC46B), Color(0xFF4AB3FF), Color(0xFF8B5CF6))
        val c = q(0.5f, 1.15f)
        for (k in cols.indices) {
            val rr = w * (0.62f - k * 0.045f)
            drawArc(cols[k].copy(alpha = 0.6f * pen.rainbow), 180f, 180f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f), style = Stroke(w * 0.045f))
        }
    }
    val far = lerp(Color(0xFF8FA8C8), Color(0xFF2A2F5A), night)
    val hills = Path().apply {
        moveTo(r.left, r.bottom)
        lineTo(r.left, r.top + h * 0.72f)
        lineTo(r.left + w * 0.22f, r.top + h * 0.52f)
        lineTo(r.left + w * 0.4f, r.top + h * 0.68f)
        lineTo(r.left + w * 0.62f, r.top + h * 0.46f)
        lineTo(r.left + w * 0.85f, r.top + h * 0.66f)
        lineTo(r.right, r.top + h * 0.6f)
        lineTo(r.right, r.bottom)
        close()
    }
    drawPath(hills, far)
    drawPath(fxPoly(1f, r.left + w * 0.22f, r.top + h * 0.52f, r.left + w * 0.27f, r.top + h * 0.56f, r.left + w * 0.17f, r.top + h * 0.56f), Color.White.copy(alpha = 0.85f - night * 0.4f))
    drawPath(fxPoly(1f, r.left + w * 0.62f, r.top + h * 0.46f, r.left + w * 0.68f, r.top + h * 0.51f, r.left + w * 0.56f, r.top + h * 0.51f), Color.White.copy(alpha = 0.85f - night * 0.4f))
    val near = if (snow) lerp(Color(0xFFF1F5FB), Color(0xFF7E86B0), night) else lerp(Color(0xFF7ED06A), Color(0xFF1E3A3A), night)
    drawPath(blobPath(r.left - w * 0.1f, r.bottom + h * 0.1f, r.left + w * 0.3f, r.top + h * 0.8f, r.left + w * 0.7f, r.top + h * 0.86f, r.right + w * 0.1f, r.top + h * 0.78f, r.right + w * 0.1f, r.bottom + h * 0.1f), near)
    if (rain) {
        for (k in 0 until 8) {
            val x = r.left + fxFrac(k * 0.137f + seed) * w
            val y = r.top + fxFrac(t * 1.6f + k * 0.23f) * (h + 0.03f * u) - 0.03f * u
            drawLine(Color.White.copy(alpha = 0.6f), Offset(x, y), Offset(x - 0.006f * u, y + 0.018f * u), pen.lw * 0.6f, StrokeCap.Round)
        }
    }
    if (snow) {
        for (k in 0 until 10) {
            val y = r.top + fxFrac(t * 0.22f + k * 0.17f) * h
            val x = r.left + fxFrac(k * 0.29f + seed) * w + sin(t * 1.5f + k) * 0.006f * u
            drawCircle(Color.White, 0.0028f * u, Offset(x, y))
        }
    }
}

/** The round window in the troll cave, looking out through the back of the waterfall. */
private fun DrawScope.fxWaterfallWindow(u: Float, pen: Pen) {
    val t = pen.t
    val c = Offset(0f, -0.11f * u)
    val rr = 0.085f * u
    for (k in 0 until 11) {
        val a = k * FX_PI * 2f / 11f + 0.2f
        val sc = Offset(c.x + cos(a) * 0.099f * u, c.y + sin(a) * 0.099f * u)
        inkedCircle(sc, (0.02f + (k % 3) * 0.003f) * u, if (k % 2 == 0) FxC.stone else FxC.stone.darken(0.14f), pen)
    }
    val glass = ovalPath(Rect(c, rr))
    val o = Oblique.offset(0.045f, u)
    clipPath(glass) {
        drawRect(FxC.stone.darken(0.45f), Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f))
        translate(o.x, o.y) {
            val view = ovalPath(Rect(c, rr))
            clipPath(view) {
                val top = lerp(Color(0xFFD8F6FF), Color(0xFF1B3C63), pen.night)
                val low = lerp(Color(0xFF63BDE6), Color(0xFF0E2242), pen.night)
                drawRect(Brush.verticalGradient(listOf(top, low), c.y - rr, c.y + rr), Offset(c.x - rr, c.y - rr), Size(rr * 2f, rr * 2f))
                fxGlow(Offset(c.x - rr * 0.4f, c.y - rr * 0.5f), rr * 1.1f, Color.White, 0.5f * (1f - pen.night) + 0.1f)
                for (i in 0 until 9) {
                    val x = c.x - rr + (i + 0.5f) * (rr * 2f / 9f)
                    for (k in 0 until 3) {
                        val ph = fxFrac(t * (0.8f + (i % 3) * 0.15f) + i * 0.37f + k / 3f)
                        val y0 = c.y - rr - 0.04f * u + ph * (rr * 2f + 0.06f * u)
                        drawLine(Color.White.copy(alpha = 0.55f), Offset(x, y0), Offset(x, y0 + 0.035f * u), (0.004f + (i % 2) * 0.003f) * u, StrokeCap.Round)
                    }
                }
                for (i in 0 until 5) {
                    val x = c.x - rr * 0.8f + i * rr * 0.4f
                    val ph = fxFrac(t * 1.3f + i * 0.21f)
                    val y0 = c.y - rr - 0.05f * u + ph * (rr * 2f + 0.08f * u)
                    drawLine(Color(0xFF3E9ACB).copy(alpha = 0.35f), Offset(x, y0), Offset(x, y0 + 0.05f * u), 0.006f * u, StrokeCap.Round)
                }
                for (k in 0 until 6) drawCircle(Color.White.copy(alpha = 0.5f), rr * 0.22f, Offset(c.x - rr + k * rr * 0.4f, c.y + rr * 0.9f + sin(t * 3f + k) * rr * 0.05f))
            }
            drawPath(view, FxC.iron, style = Stroke(0.005f * u))
            drawArc(Color.White.copy(alpha = 0.5f), 200f, 60f, false, Offset(c.x - rr * 0.8f, c.y - rr * 0.8f), Size(rr * 1.6f, rr * 1.6f), style = Stroke(0.004f * u, cap = StrokeCap.Round))
            drawCircle(Color.White.copy(alpha = 0.6f), 0.004f * u, Offset(c.x + rr * 0.4f, c.y + rr * 0.1f))
        }
    }
    drawCircle(Ink.line, rr, c, style = pen.stroke)
    fxCloud(FxC.leaf, pen, true, c.x - 0.07f * u, c.y + 0.078f * u, 0.012f * u, c.x - 0.05f * u, c.y + 0.092f * u, 0.01f * u, c.x + 0.06f * u, c.y + 0.085f * u, 0.009f * u)
    for (k in 0 until 2) {
        val bx0 = if (k == 0) 0.075f else -0.085f
        val by0 = if (k == 0) -0.19f else -0.175f
        val crystal = fxPoly(u, bx0, by0, bx0 + 0.012f, by0 - 0.035f, bx0 + 0.024f, by0, bx0 + 0.012f, by0 + 0.008f)
        val col = if (k == 0) FxC.lilac else Color(0xFF7FE6F2)
        fxGlow(Offset((bx0 + 0.012f) * u, (by0 - 0.012f) * u), 0.04f * u, col, 0.25f + 0.4f * pen.night)
        inked(crystal, col, pen)
        fxLine(Offset((bx0 + 0.012f) * u, (by0 - 0.03f) * u), Offset((bx0 + 0.012f) * u, (by0 + 0.004f) * u), Color.White.copy(alpha = 0.6f), pen.lw * 0.6f)
    }
}

// ------------------------------------------------------------------------------------------ wall bits

internal fun DrawScope.fxMailbox(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val gift = f.mode == 1
    val d = 0.045f
    inkedRound(fxFront(u, -0.03f, -0.098f, 0.03f, -0.002f, d + 0.004f), 0.004f * u, FxC.oakDark, pen, shade = false)
    if (gift) {
        fxBox(u, -0.022f, -0.106f, 0.018f, -0.082f, 0.03f, FxC.yellow, pen, rad = 0.002f, z = 0.008f)
        val g = q(-0.002f, -0.106f, 0.008f)
        fxLine(g, q(-0.002f, -0.084f, 0.008f), FxC.red, 0.004f * u)
        drawCircle(FxC.red, 0.005f * u, Offset(g.x - 0.006f * u, g.y - 0.004f * u), style = Stroke(0.0025f * u))
        drawCircle(FxC.red, 0.005f * u, Offset(g.x + 0.006f * u, g.y - 0.004f * u), style = Stroke(0.0025f * u))
    }
    fxBox(u, -0.036f, -0.088f, 0.036f, -0.008f, d, FxC.red, pen, rad = 0.01f)
    rotate(if (gift) -14f else 0f, p(-0.038f, -0.086f)) {
        fxBox(u, -0.038f, -0.094f, 0.038f, -0.08f, d + 0.003f, FxC.red.darken(0.08f), pen, rad = 0.006f)
    }
    val env = Rect(-0.016f * u, -0.058f * u, 0.016f * u, -0.036f * u)
    inkedRound(env, 0.002f * u, FxC.paint, pen, shade = false)
    drawLine(Ink.line, env.topLeft, Offset(env.center.x, env.center.y + env.height * 0.1f), pen.lw * 0.5f)
    drawLine(Ink.line, Offset(env.right, env.top), Offset(env.center.x, env.center.y + env.height * 0.1f), pen.lw * 0.5f)
    // The flag on the side: down when empty, up (and waggling) when a parcel waits.
    val pivot = q(0.036f, -0.034f, d * 0.5f)
    rotate(if (gift) sin(t * 4f) * 6f else 172f, pivot) {
        drawLine(FxC.charcoal, pivot, Offset(pivot.x, pivot.y - 0.045f * u), 0.004f * u, StrokeCap.Round)
        inkedRound(Rect(pivot.x, pivot.y - 0.048f * u, pivot.x + 0.022f * u, pivot.y - 0.032f * u), 0.002f * u, FxC.yellow, pen, shade = false)
    }
    fxBolt(pivot, 0.003f * u, pen)
    if (gift) twinkle(p(0.02f, -0.118f), 0.008f * u * (0.6f + 0.4f * sin(t * 4f)), FxC.yellow)
}

internal fun DrawScope.fxClock(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val wood = FxC.oak
    val d = 0.035f
    val hz = 0.02f
    val ho = q(0f, 0f, hz)
    translate(ho.x, ho.y) {
        for (s in 0..1) {
            val x = if (s == 0) -0.02f else 0.02f
            fxLine(p(x, -0.05f), p(x, -0.022f), Ink.line.copy(alpha = 0.7f), pen.lw * 0.5f)
            inkedRound(Rect((x - 0.006f) * u, -0.025f * u, (x + 0.006f) * u, -0.003f * u), 0.004f * u, FxC.brass, pen)
        }
        rotate(sin(t * 4.7f) * 14f, p(0f, -0.05f)) {
            fxLine(p(0f, -0.05f), p(0f, -0.016f), FxC.brass.darken(0.2f), 0.003f * u)
            inkedCircle(p(0f, -0.014f), 0.009f * u, FxC.brass, pen)
        }
    }
    fxBox(u, -0.034f, -0.1f, 0.034f, -0.046f, d, wood, pen, rad = 0.003f)
    // A gable roof: both slopes seen from above, then the front gable.
    val a = q(-0.047f, -0.093f, 0f)
    val b = q(0f, -0.131f, 0f)
    val c = q(0.047f, -0.093f, 0f)
    val a2 = q(-0.047f, -0.093f, d + 0.006f)
    val b2 = q(0f, -0.131f, d + 0.006f)
    val c2 = q(0.047f, -0.093f, d + 0.006f)
    fxFace(fxQuad(b.x, b.y, b2.x, b2.y, c2.x, c2.y, c.x, c.y), FxC.red.darken(0.12f), pen)
    fxFace(fxQuad(a.x, a.y, a2.x, a2.y, b2.x, b2.y, b.x, b.y), FxC.red.lighten(0.12f), pen)
    inked(fxPoly(u, -0.047f, -0.093f, 0f, -0.131f, 0.047f, -0.093f), FxC.red, pen)
    fxLine(p(-0.04f, -0.097f), p(0.04f, -0.097f), FxC.red.darken(0.25f), pen.lw * 0.6f)
    val pop = ((f.anim - 0.3f) / 0.7f).coerceIn(0f, 1f)
    val door = Rect(-0.009f * u, -0.118f * u, 0.009f * u, -0.101f * u)
    if (pop > 0f) {
        drawRoundRect(Color(0xFF2A1E26), door.topLeft, door.size, CornerRadius(0.003f * u))
        val s = 0.8f + pop * 0.5f
        val bc = p(-0.004f * pop, -0.108f + pop * 0.008f)
        inkedCircle(bc, 0.0085f * u * s, FxC.mustard, pen)
        val head = Offset(bc.x + 0.004f * u * s, bc.y - 0.007f * u * s)
        inkedCircle(head, 0.0058f * u * s, FxC.mustard, pen)
        inked(fxPoly(1f, head.x + 0.004f * u * s, head.y - 0.001f * u * s, head.x + 0.012f * u * s, head.y - 0.004f * u * s, head.x + 0.005f * u * s, head.y + 0.002f * u * s), FxC.terracotta, pen, shade = false)
        inked(fxPoly(1f, head.x + 0.004f * u * s, head.y + 0.002f * u * s, head.x + 0.011f * u * s, head.y + 0.005f * u * s, head.x + 0.004f * u * s, head.y + 0.004f * u * s), FxC.terracotta, pen, shade = false)
        drawCircle(Ink.line, 0.0012f * u * s, Offset(head.x + 0.001f * u * s, head.y - 0.002f * u * s))
    } else {
        inkedRound(door, 0.003f * u, wood.darken(0.15f), pen, shade = false)
        drawCircle(FxC.brass, 0.0015f * u, p(0.005f, -0.109f))
    }
    val c0 = p(0f, -0.073f)
    inkedCircle(c0, 0.02f * u, FxC.paint, pen, shade = false)
    for (i in 0 until 12) {
        val an = i * FX_PI / 6f
        val r1 = if (i % 3 == 0) 0.013f else 0.015f
        drawLine(Ink.line, Offset(c0.x + sin(an) * r1 * u, c0.y - cos(an) * r1 * u), Offset(c0.x + sin(an) * 0.018f * u, c0.y - cos(an) * 0.018f * u), pen.lw * 0.45f)
    }
    val now = java.time.LocalTime.now()
    val min = now.minute + now.second / 60f
    val hr = (now.hour % 12) + min / 60f
    val ha = hr * FX_PI / 6f
    val ma = min * FX_PI / 30f
    drawLine(Ink.line, c0, Offset(c0.x + sin(ha) * 0.0105f * u, c0.y - cos(ha) * 0.0105f * u), 0.0032f * u, StrokeCap.Round)
    drawLine(Ink.line, c0, Offset(c0.x + sin(ma) * 0.016f * u, c0.y - cos(ma) * 0.016f * u), 0.0022f * u, StrokeCap.Round)
    drawCircle(FxC.red, 0.0022f * u, c0)
}

internal fun DrawScope.fxPlant(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val cz = 0.042f
    val base = q(0f, -0.084f, cz)
    val angles = floatArrayOf(-72f, -48f, -26f, -6f, 14f, 34f, 56f, 76f)
    val lens = floatArrayOf(0.12f, 0.16f, 0.18f, 0.19f, 0.185f, 0.17f, 0.15f, 0.115f)
    fun leaf(i: Int, col: Color) {
        val wob = sin(t * 1.3f + i * 1.1f) * 2f + f.anim * sin(t * 28f + i * 1.3f) * 10f
        val a = (angles[i] + wob) * FX_PI / 180f
        val len = lens[i] * u
        val x0 = base.x + sin(a) * 0.008f * u
        val y0 = base.y
        val tx = x0 + sin(a) * len
        val ty = y0 - cos(a) * len
        inked(fxLeaf(x0, y0, tx, ty, 0.2f), col, pen)
        drawLine(col.lighten(0.3f), Offset(x0 + (tx - x0) * 0.15f, y0 + (ty - y0) * 0.15f), Offset(x0 + (tx - x0) * 0.85f, y0 + (ty - y0) * 0.85f), pen.lw * 0.6f)
    }
    for (i in angles.indices) if (i % 2 == 0) leaf(i, FxC.spruce)
    val pb = q(0f, 0f, cz)
    val pt = q(0f, -0.076f, cz)
    fxCyl(pb.x, pb.y, pt.y, 0.03f * u, 0.04f * u, FxC.paint, pen, cap = false)
    val band = q(0f, -0.048f, cz)
    fxLine(Offset(band.x - 0.039f * u, band.y + 0.006f * u), Offset(band.x + 0.039f * u, band.y - 0.006f * u), FxC.terracotta, 0.006f * u)
    val rt = q(0f, -0.088f, cz)
    fxCyl(pt.x, pt.y + 0.002f * u, rt.y, 0.043f * u, 0.043f * u, FxC.paint, pen, top = FxC.soil)
    for (i in angles.indices) if (i % 2 == 1) leaf(i, FxC.spruceLight)
    shine(Offset(pb.x - 0.022f * u, pb.y - 0.05f * u), 0.006f * u, 0.016f * u, 0.5f)
}

/** A modern black wood stove with a big glass door; its pipe runs up to the ceiling. */
internal fun DrawScope.fxWoodStove(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val black = Color(0xFF2E2D36)
    val on = f.on
    val d = 0.12f
    val pb = q(0f, -0.32f, d * 0.5f)
    val ceiling = -(f.y + 0.05f) * u
    fxCyl(pb.x, pb.y, ceiling, 0.017f * u, 0.017f * u, black, pen, cap = false, bottom = false)
    val ring = q(0f, -0.37f, d * 0.5f)
    fxCyl(ring.x, ring.y + 0.006f * u, ring.y - 0.006f * u, 0.021f * u, 0.021f * u, black.lighten(0.12f), pen)
    if (on) fxGlow(p(0f, -0.17f), 0.36f * u, FxC.flame2, 0.18f + 0.3f * pen.night)
    for (s in 0..1) {
        val x = if (s == 0) -0.05f else 0.05f
        fxBox(u, x - 0.006f, -0.03f, x + 0.006f, 0f, 0.012f, black, pen, z = d - 0.02f)
    }
    fxBox(u, -0.072f, -0.3f, 0.072f, -0.028f, d, black, pen, rad = 0.014f, top = black.lighten(0.15f), side = black.darken(0.25f))
    fxBox(u, -0.08f, -0.32f, 0.08f, -0.298f, d + 0.012f, black.lighten(0.1f), pen, rad = 0.005f, top = black.lighten(0.22f))
    val win = Rect(-0.05f * u, -0.265f * u, 0.05f * u, -0.1f * u)
    val winPath = roundPath(win, 0.01f * u)
    drawPath(winPath, if (on) Color(0xFF5A2410) else Color(0xFF1E1A22))
    clipPath(winPath) {
        if (on) fxGlow(p(0f, -0.12f), 0.1f * u, FxC.flame2, 0.8f)
        capsule(p(-0.04f, -0.108f), p(0.035f, -0.12f), 0.014f * u, FxC.woodDark, pen)
        capsule(p(0.04f, -0.106f), p(-0.03f, -0.124f), 0.013f * u, FxC.wood, pen)
        if (on) {
            fxFire(-0.018f * u, -0.114f * u, 0.03f * u, 0.085f * u, t, 0f, pen, false)
            fxFire(0.017f * u, -0.114f * u, 0.028f * u, 0.07f * u, t, 1.9f, pen, false)
            fxFire(0f, -0.11f * u, 0.036f * u, 0.11f * u, t, 3.3f, pen, false)
        } else {
            for (k in 0 until 4) drawCircle(FxC.flame1.copy(alpha = 0.35f + 0.2f * sin(t * 2f + k)), 0.0025f * u, p(-0.02f + k * 0.013f, -0.104f))
        }
        fxLine(p(-0.04f, -0.2f), p(-0.01f, -0.255f), Color.White.copy(alpha = 0.16f), 0.006f * u)
    }
    drawPath(winPath, Ink.line, style = pen.stroke)
    drawRoundRect(black.lighten(0.18f), p(-0.062f, -0.278f), Size(0.124f * u, 0.192f * u), CornerRadius(0.012f * u), style = pen.thin)
    capsule(p(0.058f, -0.215f), p(0.058f, -0.16f), 0.006f * u, FxC.steel, pen)
    inkedRound(Rect(-0.03f * u, -0.078f * u, 0.03f * u, -0.068f * u), 0.003f * u, black.lighten(0.14f), pen, shade = false)
    drawCircle(FxC.steel, 0.003f * u, p(if (on) 0.014f else -0.014f, -0.073f))
    fxLine(p(-0.064f, -0.28f), p(-0.064f, -0.05f), Color.White.copy(alpha = 0.12f), 0.004f * u)
    for (s in 0..1) {
        val x = if (s == 0) -0.05f else 0.05f
        fxBox(u, x - 0.006f, -0.03f, x + 0.006f, 0f, 0.012f, black, pen)
    }
    if (on) {
        for (k in 0 until 2) {
            val ph = fxFrac(t * 0.7f + k * 0.5f)
            val w0 = q(-0.03f + k * 0.06f, -0.33f, d * 0.5f)
            val wave = Path().apply {
                moveTo(w0.x, w0.y - ph * 0.06f * u)
                quadraticTo(w0.x + 0.008f * u, w0.y - (0.02f + ph * 0.06f) * u, w0.x, w0.y - (0.04f + ph * 0.06f) * u)
            }
            drawPath(wave, Color.White.copy(alpha = 0.4f * (1f - ph)), style = pen.thin)
        }
    }
}
