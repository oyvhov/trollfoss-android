package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.UpperMirror
import app.trollfoss.domain.UpperTrack
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The landing and the children's room: portraits that blink and wave, the window and the window seat, the toy train that
 * circles its track and takes a passenger, the tower of blocks, the dollhouse of the big house with tiny figures that
 * stand where the real ones do, the puppet theatre, the moon lamp, posters, the mobile and the blanket fort.
 */

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

// ---------------------------------------------------------------------------------------------- portraits

private val portraitFrames = listOf(UpC.brass, UpC.walnut, UpC.brass.lighten(0.15f), UpC.wood, UpC.cream, UpC.oakDark)
private val portraitSky = listOf(Color(0xFFCDE3C4), Color(0xFFF3D3DD), Color(0xFFD3E3F5), Color(0xFFF7E3B8), Color(0xFFE4D9F6), Color(0xFFD4EBE6))

/** A framed portrait of one of the family. It blinks now and then and, when tapped, waves, tips its cap, blows a kiss … */
internal fun DrawScope.upPortrait(f: Fixture, u: Float, pen: Pen) {
    val v = f.variant.coerceIn(0, 5)
    val t = pen.t
    val wave = f.timer.coerceAtLeast(0f)
    val gesture = if (wave > 0f) sin(PI.toFloat() * (1f - wave / 1.8f)).coerceIn(0f, 1f) else 0f
    val w = 0.15f * u
    val h = 0.19f * u
    box3d(Rect(-w / 2f, -h, w / 2f, 0f), 0.02f * u, portraitFrames[v], pen, radius = 0.006f * u)
    val m = 0.0135f * u
    val inner = Rect(-w / 2f + m, -h + m, w / 2f - m, -m)
    drawRect(Brush.verticalGradient(listOf(portraitSky[v].lighten(0.3f), portraitSky[v]), startY = inner.top, endY = inner.bottom), inner.topLeft, inner.size)
    val closed = f.bob > 0f
    clipRect(inner.left, inner.top, inner.right, inner.bottom) {
        val cx = inner.center.x
        val iw = inner.width
        val ih = inner.height
        val hc = Offset(cx, inner.top + ih * 0.43f)
        val r = iw * 0.255f
        fun eye(dx: Float, dy: Float = 0f) {
            val e = Offset(hc.x + dx * r, hc.y + dy * r)
            if (closed) {
                drawLine(Ink.line, Offset(e.x - r * 0.17f, e.y), Offset(e.x + r * 0.17f, e.y), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
            } else {
                drawCircle(Color.White, r * 0.17f, e)
                drawCircle(Ink.line, r * 0.1f, Offset(e.x, e.y + r * 0.01f))
                drawCircle(Color.White, r * 0.035f, Offset(e.x - r * 0.03f, e.y - r * 0.03f))
            }
        }
        // Shoulders first, then the head.
        fun shoulders(c: Color, collar: Color? = null) {
            val body = Path().apply {
                moveTo(cx - iw * 0.48f, inner.bottom + 2f)
                quadraticTo(cx - iw * 0.46f, hc.y + r * 1.15f, cx, hc.y + r * 1.1f)
                quadraticTo(cx + iw * 0.46f, hc.y + r * 1.15f, cx + iw * 0.48f, inner.bottom + 2f)
                close()
            }
            inked(body, c, pen)
            if (collar != null) {
                val col = Path().apply {
                    moveTo(cx - r * 0.45f, hc.y + r * 1.0f); lineTo(cx, hc.y + r * 1.5f); lineTo(cx + r * 0.45f, hc.y + r * 1.0f); close()
                }
                drawPath(col, collar)
                drawPath(col, Ink.line, style = pen.thin)
            }
        }
        when (v) {
            0 -> {
                // Grandpa: tweed jacket, bow tie, round glasses and a big grey moustache; his flat cap lifts when he waves.
                shoulders(Color(0xFF8C6A46), Color.White)
                drawPath(Path().apply { poly(cx - r * 0.3f, hc.y + r * 1.15f, cx + r * 0.3f, hc.y + r * 1.15f, cx, hc.y + r * 1.32f) }, UpC.red)
                inkedCircle(Offset(hc.x - r * 0.98f, hc.y + r * 0.1f), r * 0.2f, UpC.skin, pen, shade = false)
                inkedCircle(Offset(hc.x + r * 0.98f, hc.y + r * 0.1f), r * 0.2f, UpC.skin, pen, shade = false)
                inkedCircle(hc, r, UpC.skin, pen)
                eye(-0.4f); eye(0.4f)
                for (s in listOf(-1f, 1f)) drawCircle(Ink.line, r * 0.27f, Offset(hc.x + s * r * 0.4f, hc.y), style = pen.thin)
                drawLine(Ink.line, Offset(hc.x - r * 0.13f, hc.y), Offset(hc.x + r * 0.13f, hc.y), strokeWidth = pen.lw * 0.6f)
                val stache = Path().apply {
                    moveTo(hc.x - r * 0.6f, hc.y + r * 0.52f)
                    quadraticTo(hc.x - r * 0.3f, hc.y + r * 0.3f, hc.x, hc.y + r * 0.46f)
                    quadraticTo(hc.x + r * 0.3f, hc.y + r * 0.3f, hc.x + r * 0.6f, hc.y + r * 0.52f)
                    quadraticTo(hc.x + r * 0.3f, hc.y + r * 0.7f, hc.x, hc.y + r * 0.56f)
                    quadraticTo(hc.x - r * 0.3f, hc.y + r * 0.7f, hc.x - r * 0.6f, hc.y + r * 0.52f)
                    close()
                }
                inked(stache, Color(0xFFC9CCD4), pen, shade = false)
                // Hair on the sides, and the cap.
                for (s in listOf(-1f, 1f)) inkedCircle(Offset(hc.x + s * r * 0.9f, hc.y - r * 0.2f), r * 0.2f, Color(0xFFC9CCD4), pen, shade = false)
                val lift = gesture * r * 0.55f
                translate(0f, -lift) {
                    val cap = Path().apply {
                        moveTo(hc.x - r * 0.95f, hc.y - r * 0.5f)
                        quadraticTo(hc.x - r * 0.9f, hc.y - r * 1.25f, hc.x, hc.y - r * 1.2f)
                        quadraticTo(hc.x + r * 0.95f, hc.y - r * 1.25f, hc.x + r * 1.0f, hc.y - r * 0.5f)
                        quadraticTo(hc.x + r * 0.2f, hc.y - r * 0.38f, hc.x - r * 0.95f, hc.y - r * 0.5f)
                        close()
                    }
                    inked(cap, Color(0xFF6B7A5A), pen)
                    drawCircle(Color(0xFF55634A), r * 0.1f, Offset(hc.x, hc.y - r * 1.2f))
                }
                if (gesture > 0f) drawCircle(UpC.skin, r * 0.17f, Offset(hc.x + r * 1.05f, hc.y - r * 1.0f - lift * 0.9f))
            }
            1 -> {
                // Grandma: white bun, pink cardigan, round glasses; she waves.
                inkedCircle(Offset(hc.x, hc.y - r * 1.12f), r * 0.42f, Color(0xFFF1F1F4), pen, shade = false)
                shoulders(Color(0xFFE89BB5), Color(0xFFFFFFFF))
                inkedCircle(hc, r, UpC.skin, pen)
                val hair = Path().apply {
                    moveTo(hc.x - r, hc.y - r * 0.05f)
                    quadraticTo(hc.x - r * 1.05f, hc.y - r * 1.15f, hc.x, hc.y - r * 1.05f)
                    quadraticTo(hc.x + r * 1.05f, hc.y - r * 1.15f, hc.x + r, hc.y - r * 0.05f)
                    quadraticTo(hc.x + r * 0.55f, hc.y - r * 0.62f, hc.x, hc.y - r * 0.6f)
                    quadraticTo(hc.x - r * 0.55f, hc.y - r * 0.62f, hc.x - r, hc.y - r * 0.05f)
                    close()
                }
                inked(hair, Color(0xFFF1F1F4), pen, shade = false)
                eye(-0.4f, 0.05f); eye(0.4f, 0.05f)
                for (s in listOf(-1f, 1f)) drawCircle(Color(0xFF7A5C8A), r * 0.27f, Offset(hc.x + s * r * 0.4f, hc.y + r * 0.05f), style = pen.thin)
                drawArc(Ink.line, 20f, 140f, false, Offset(hc.x - r * 0.3f, hc.y + r * 0.2f), Size(r * 0.6f, r * 0.45f), style = pen.thin)
                drawCircle(Color(0x55FF6F91), r * 0.16f, Offset(hc.x - r * 0.62f, hc.y + r * 0.38f))
                drawCircle(Color(0x55FF6F91), r * 0.16f, Offset(hc.x + r * 0.62f, hc.y + r * 0.38f))
                if (gesture > 0f) {
                    val a = sin(t * 13f) * 0.18f
                    val hand = Offset(hc.x + r * 1.25f + sin(t * 13f) * r * 0.2f, hc.y + r * 0.7f - gesture * r * 0.9f)
                    rotate(a * 57f, hand) { inkedCircle(hand, r * 0.28f, UpC.skin, pen, shade = false) }
                    drawLine(Ink.line, Offset(hand.x - r * 0.1f, hand.y - r * 0.2f), Offset(hand.x - r * 0.1f, hand.y - r * 0.05f), strokeWidth = pen.lw * 0.6f)
                }
            }
            2 -> {
                // Mum: long brown hair, green jumper, a smile; she blows a kiss that floats off.
                shoulders(Color(0xFF59A87A), null)
                val back = Path().apply {
                    moveTo(hc.x - r * 1.1f, hc.y + r * 1.3f); quadraticTo(hc.x - r * 1.3f, hc.y - r * 1.3f, hc.x, hc.y - r * 1.25f)
                    quadraticTo(hc.x + r * 1.3f, hc.y - r * 1.3f, hc.x + r * 1.1f, hc.y + r * 1.3f); close()
                }
                inked(back, Color(0xFF7A4A2E), pen)
                inkedCircle(hc, r, UpC.skin, pen)
                val fringe = Path().apply {
                    moveTo(hc.x - r, hc.y - r * 0.05f); quadraticTo(hc.x - r, hc.y - r * 1.05f, hc.x, hc.y - r * 1.02f)
                    quadraticTo(hc.x + r, hc.y - r * 1.05f, hc.x + r, hc.y - r * 0.05f); quadraticTo(hc.x + r * 0.3f, hc.y - r * 0.55f, hc.x - r, hc.y - r * 0.05f); close()
                }
                inked(fringe, Color(0xFF7A4A2E), pen, shade = false)
                eye(-0.4f); eye(0.4f)
                drawCircle(Color(0x55FF6F91), r * 0.16f, Offset(hc.x - r * 0.62f, hc.y + r * 0.36f))
                drawCircle(Color(0x55FF6F91), r * 0.16f, Offset(hc.x + r * 0.62f, hc.y + r * 0.36f))
                if (gesture > 0.05f) {
                    drawOval(Color(0xFFE94F6A), Offset(hc.x - r * 0.1f, hc.y + r * 0.4f), Size(r * 0.2f, r * 0.14f))
                    val ph = 1f - wave / 1.8f
                    val hp = Offset(hc.x + r * (0.5f + ph * 1.2f), hc.y + r * (0.2f - ph * 1.2f))
                    drawPath(fxHeart(hp.x, hp.y, r * 0.22f), Color(0xFFFF4D6D).copy(alpha = (1f - ph * 0.6f)))
                } else {
                    drawArc(Ink.line, 20f, 140f, false, Offset(hc.x - r * 0.3f, hc.y + r * 0.18f), Size(r * 0.6f, r * 0.45f), style = pen.thin)
                }
            }
            3 -> {
                // Dad: blue hoodie, bobble hat, beard; thumbs up.
                shoulders(Color(0xFF4A86D8), null)
                inkedOval(Rect(hc.x - r * 0.55f, hc.y + r * 1.05f, hc.x + r * 0.55f, hc.y + r * 1.4f), Color(0xFF3D72BF), pen, shade = false)
                inkedCircle(hc, r, UpC.skin.darken(0.06f), pen)
                val beard = Path().apply {
                    moveTo(hc.x - r * 0.95f, hc.y + r * 0.0f); quadraticTo(hc.x - r * 0.9f, hc.y + r * 1.2f, hc.x, hc.y + r * 1.15f)
                    quadraticTo(hc.x + r * 0.9f, hc.y + r * 1.2f, hc.x + r * 0.95f, hc.y + r * 0.0f); quadraticTo(hc.x + r * 0.5f, hc.y + r * 0.5f, hc.x, hc.y + r * 0.45f)
                    quadraticTo(hc.x - r * 0.5f, hc.y + r * 0.5f, hc.x - r * 0.95f, hc.y + r * 0.0f); close()
                }
                inked(beard, Color(0xFF5A3A28), pen, shade = false)
                eye(-0.4f); eye(0.4f)
                drawArc(Color.White, 20f, 140f, false, Offset(hc.x - r * 0.28f, hc.y + r * 0.5f), Size(r * 0.56f, r * 0.4f), style = Stroke(pen.lw * 1.2f))
                val hat = Path().apply {
                    moveTo(hc.x - r * 1.0f, hc.y - r * 0.3f); quadraticTo(hc.x - r * 1.0f, hc.y - r * 1.3f, hc.x, hc.y - r * 1.3f)
                    quadraticTo(hc.x + r * 1.0f, hc.y - r * 1.3f, hc.x + r * 1.0f, hc.y - r * 0.3f); lineTo(hc.x - r * 1.0f, hc.y - r * 0.3f); close()
                }
                inked(hat, Color(0xFFFFC83D), pen)
                inkedCircle(Offset(hc.x, hc.y - r * 1.38f), r * 0.2f, Color(0xFFFF6B6B), pen, shade = false)
                if (gesture > 0f) {
                    val bounce = sin(t * 14f) * 0.1f * r
                    val hand = Offset(hc.x + r * 1.15f, hc.y + r * 1.0f - gesture * r * 0.5f + bounce)
                    inkedRound(Rect(hand.x - r * 0.18f, hand.y - r * 0.1f, hand.x + r * 0.2f, hand.y + r * 0.25f), r * 0.1f, UpC.skin.darken(0.06f), pen, shade = false)
                    inkedRound(Rect(hand.x - r * 0.07f, hand.y - r * 0.4f, hand.x + r * 0.1f, hand.y), r * 0.08f, UpC.skin.darken(0.06f), pen, shade = false)
                }
            }
            4 -> {
                // The baby: bonnet with a bow, big eyes, bib; arms up and laughing when tapped.
                shoulders(Color(0xFFFFE9A8), Color.White)
                inkedCircle(hc, r * 1.05f, UpC.skin.lighten(0.1f), pen)
                val bonnet = Path().apply {
                    moveTo(hc.x - r * 1.05f, hc.y + r * 0.1f); quadraticTo(hc.x - r * 1.15f, hc.y - r * 1.2f, hc.x, hc.y - r * 1.15f)
                    quadraticTo(hc.x + r * 1.15f, hc.y - r * 1.2f, hc.x + r * 1.05f, hc.y + r * 0.1f); quadraticTo(hc.x + r * 0.6f, hc.y - r * 0.5f, hc.x, hc.y - r * 0.5f)
                    quadraticTo(hc.x - r * 0.6f, hc.y - r * 0.5f, hc.x - r * 1.05f, hc.y + r * 0.1f); close()
                }
                inked(bonnet, Color(0xFFFFB6CE), pen)
                drawPath(fxHeart(hc.x, hc.y - r * 1.15f, r * 0.2f), Color(0xFFFF6F91))
                for (s in listOf(-1f, 1f)) {
                    val e = Offset(hc.x + s * r * 0.4f, hc.y + r * 0.05f)
                    if (closed) {
                        drawLine(Ink.line, Offset(e.x - r * 0.2f, e.y), Offset(e.x + r * 0.2f, e.y), strokeWidth = pen.lw, cap = StrokeCap.Round)
                    } else {
                        drawCircle(Color.White, r * 0.24f, e)
                        drawCircle(Ink.line, r * 0.15f, e)
                        drawCircle(Color.White, r * 0.05f, Offset(e.x - r * 0.05f, e.y - r * 0.05f))
                    }
                }
                drawCircle(Color(0x66FF6F91), r * 0.18f, Offset(hc.x - r * 0.7f, hc.y + r * 0.42f))
                drawCircle(Color(0x66FF6F91), r * 0.18f, Offset(hc.x + r * 0.7f, hc.y + r * 0.42f))
                if (gesture > 0f) {
                    drawOval(Color(0xFF7A2440), Offset(hc.x - r * 0.2f, hc.y + r * 0.38f), Size(r * 0.4f, r * 0.34f))
                    for (s in listOf(-1f, 1f)) {
                        val hp = Offset(hc.x + s * r * (1.15f + 0.1f * sin(t * 12f)), hc.y - r * 0.2f - gesture * r * 0.5f)
                        inkedCircle(hp, r * 0.24f, UpC.skin.lighten(0.1f), pen, shade = false)
                    }
                } else {
                    drawArc(Ink.line, 20f, 140f, false, Offset(hc.x - r * 0.18f, hc.y + r * 0.35f), Size(r * 0.36f, r * 0.26f), style = pen.thin)
                }
            }
            else -> {
                // The dog: floppy ears, a wet nose and a tongue that comes out when it is tapped.
                shoulders(Color(0xFFD9774F), Color(0xFFFFE08A))
                val ears = if (gesture > 0f) sin(t * 14f) * 14f * gesture else 0f
                for (s in listOf(-1f, 1f)) rotate(s * ears, Offset(hc.x + s * r * 0.8f, hc.y - r * 0.6f)) {
                    inkedOval(Rect(hc.x + s * r * 0.95f - r * 0.34f, hc.y - r * 0.8f, hc.x + s * r * 0.95f + r * 0.34f, hc.y + r * 0.7f), Color(0xFF8C5A36), pen, shade = false)
                }
                inkedCircle(hc, r * 1.05f, Color(0xFFE3B27A), pen)
                inkedOval(Rect(hc.x - r * 0.55f, hc.y + r * 0.05f, hc.x + r * 0.55f, hc.y + r * 0.8f), Color(0xFFF6E3C4), pen, shade = false)
                eye(-0.45f, -0.1f); eye(0.45f, -0.1f)
                inkedOval(Rect(hc.x - r * 0.2f, hc.y + r * 0.1f, hc.x + r * 0.2f, hc.y + r * 0.34f), Color(0xFF3A3340), pen, shade = false)
                if (gesture > 0f) {
                    val out = r * (0.3f + 0.25f * sin(t * 12f) * gesture)
                    inkedRound(Rect(hc.x - r * 0.14f, hc.y + r * 0.45f, hc.x + r * 0.14f, hc.y + r * 0.45f + out), r * 0.12f, Color(0xFFFF6F91), pen, shade = false)
                }
                drawArc(Ink.line, 20f, 140f, false, Offset(hc.x - r * 0.3f, hc.y + r * 0.3f), Size(r * 0.6f, r * 0.34f), style = pen.thin)
            }
        }
    }
    drawRect(Ink.line, inner.topLeft, inner.size, style = pen.stroke)
    // A glass shine across the corner.
    drawLine(Color.White.copy(alpha = 0.35f), Offset(inner.left + 0.01f * u, inner.top + 0.05f * u), Offset(inner.left + 0.05f * u, inner.top + 0.01f * u), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    // A little brass plate and a hook.
    drawLine(Ink.line, Offset(0f, -h - 0.02f * u), Offset(-0.03f * u, -h), strokeWidth = pen.lw * 0.6f)
    drawLine(Ink.line, Offset(0f, -h - 0.02f * u), Offset(0.03f * u, -h), strokeWidth = pen.lw * 0.6f)
}

// ---------------------------------------------------------------------------------------------- windows

/** Curtains tied back, or drawn across the window. */
private fun DrawScope.curtains(u: Float, l: Float, r: Float, top: Float, bottom: Float, closed: Boolean, color: Color, pen: Pen, t: Float) {
    val sway = 0f
    if (closed) {
        val mid = (l + r) / 2f
        for ((a, b) in listOf(l to mid, mid to r)) {
            val rect = Rect(a * u, top * u, b * u, bottom * u)
            drawRect(color, rect.topLeft, rect.size)
            var x = rect.left + 0.012f * u
            while (x < rect.right) {
                drawLine(color.darken(0.18f), Offset(x + sway, rect.top), Offset(x + sway * 0.5f, rect.bottom), strokeWidth = pen.lw * 0.8f)
                x += 0.026f * u
            }
            drawRect(Ink.line, rect.topLeft, rect.size, style = pen.thin)
        }
    } else {
        for (side in listOf(-1f, 1f)) {
            val edge = if (side < 0f) l else r
            val inward = -side
            val cur = Path().apply {
                moveTo(edge * u, top * u)
                lineTo((edge + inward * 0.05f) * u, top * u)
                quadraticTo((edge + inward * 0.02f) * u, ((top + bottom) / 2f) * u, (edge + inward * 0.045f) * u + sway, bottom * u)
                lineTo(edge * u, bottom * u)
                close()
            }
            inked(cur, color, pen)
            drawLine(color.darken(0.25f), Offset((edge + inward * 0.02f) * u, (top + 0.01f) * u), Offset((edge + inward * 0.02f) * u, (bottom - 0.01f) * u), strokeWidth = pen.lw * 0.7f)
            // The tie.
            drawLine(UpC.brass, Offset((edge + inward * 0.03f) * u, (top + (bottom - top) * 0.55f) * u), Offset(edge * u, (top + (bottom - top) * 0.58f) * u), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
        }
    }
}

/** A window with its view. Variant 0: the landing, a garden view; 1: the bedroom's arched window of stars; 2: a round porthole. */
internal fun DrawScope.upWindow(f: Fixture, u: Float, pen: Pen) {
    val n = pen.night
    val t = pen.t
    val closed = f.mode == 1
    val w = 0.30f
    val h = 0.36f
    val arched = f.variant == 1
    // Frame and the glass.
    val frame = Path().apply {
        if (arched) {
            moveTo(-w / 2f * u, 0f); lineTo(-w / 2f * u, (-h + w / 2f) * u)
            arcTo(Rect(-w / 2f * u, -h * u, w / 2f * u, (-h + w) * u), 180f, 180f, false)
            lineTo(w / 2f * u, 0f); close()
        } else {
            addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(-w / 2f * u, -h * u, w / 2f * u, 0f), androidx.compose.ui.geometry.CornerRadius(0.012f * u)))
        }
    }
    drawPath(frame, Color(0xFFF7F3EC))
    val gm = 0.02f * u
    val glass = Path().apply {
        if (arched) {
            moveTo((-w / 2f) * u + gm, -gm); lineTo((-w / 2f) * u + gm, (-h + w / 2f) * u)
            arcTo(Rect((-w / 2f) * u + gm, -h * u + gm, (w / 2f) * u - gm, (-h + w) * u - gm), 180f, 180f, false)
            lineTo((w / 2f) * u - gm, -gm); close()
        } else {
            addRect(Rect((-w / 2f) * u + gm, -h * u + gm, (w / 2f) * u - gm, -gm))
        }
    }
    clipPath(glass) {
        val skyTop = lerp(lerp(Color(0xFF7FC4F2), Color(0xFF3F8FD9), 0f), Color(0xFF14123F), n)
        val skyLow = lerp(Color(0xFFD9F0FF), Color(0xFF3B2F7A), n)
        drawRect(Brush.verticalGradient(listOf(skyTop, skyLow), startY = -h * u, endY = 0f), Offset(-w * u, -h * u), Size(2f * w * u, h * u))
        if (f.variant == 1) {
            // Stars and a crescent moon by night; a bright sky with a cloud by day.
            if (n > 0.05f) {
                for (k in 0 until 14) {
                    val sx = (-0.12f + 0.24f * hash01(k, 141)) * u
                    val sy = (-h + 0.04f + (h - 0.08f) * hash01(k, 142) * 0.9f) * u
                    val tw = 0.5f + 0.5f * sin(t * (1.1f + hash01(k, 143)) + k * 2.1f)
                    drawCircle(Color(0xFFFFF7DA).copy(alpha = n * (0.5f + 0.5f * tw)), (0.0035f + 0.0025f * hash01(k, 144)) * u, Offset(sx, sy))
                }
                val mc = Offset(0.05f * u, (-h + 0.1f) * u)
                drawCircle(Color(0xFFFFF0BF).copy(alpha = n), 0.034f * u, mc)
                drawCircle(Color(0xFF22205A).copy(alpha = n), 0.03f * u, Offset(mc.x + 0.018f * u, mc.y - 0.008f * u))
                // A shooting star every now and then.
                val ph = (t % 9f) / 1.1f
                if (ph in 0f..1f) {
                    val a = Offset((0.12f - 0.22f * ph) * u, (-h + 0.04f + 0.14f * ph) * u)
                    drawLine(Color.White.copy(alpha = n * (1f - ph)), a, Offset(a.x + 0.05f * u, a.y - 0.03f * u), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
                }
            }
            if (n < 0.95f) {
                val cp = cloudPath(-0.05f * u, (-h + 0.12f) * u, 0.035f * u)
                drawPath(cp, Color.White.copy(alpha = 0.9f * (1f - n)))
            }
            // The roofs of the village below.
            drawPath(Path().apply { poly(-0.15f * u, -0.03f * u, -0.15f * u, -0.07f * u, -0.1f * u, -0.1f * u, -0.05f * u, -0.07f * u, -0.05f * u, -0.03f * u) }, Color(0xFF3A4766).copy(alpha = 0.9f))
            drawPath(Path().apply { poly(0.02f * u, -0.03f * u, 0.02f * u, -0.065f * u, 0.07f * u, -0.095f * u, 0.12f * u, -0.065f * u, 0.12f * u, -0.03f * u) }, Color(0xFF47557A).copy(alpha = 0.9f))
            val lit = ramp((n - 0.4f) / 0.4f)
            drawRect(Color(0xFFFFD66B).copy(alpha = lit), Offset(0.06f * u, -0.055f * u), Size(0.014f * u, 0.016f * u))
        } else {
            // The garden: hills, a tree and the sun, or the moon.
            drawPath(Path().apply {
                moveTo(-0.2f * u, -0.07f * u); quadraticTo(-0.08f * u, -0.14f * u, 0.04f * u, -0.08f * u); quadraticTo(0.12f * u, -0.05f * u, 0.2f * u, -0.09f * u); lineTo(0.2f * u, 0f); lineTo(-0.2f * u, 0f); close()
            }, lerp(Color(0xFF8FCB7E), Color(0xFF2E4A55), n))
            drawCircle(lerp(Color(0xFF55BD73), Color(0xFF244A44), n), 0.04f * u, Offset(-0.07f * u, -0.17f * u))
            drawRect(Color(0xFF7A5230), Offset(-0.075f * u, -0.14f * u), Size(0.01f * u, 0.07f * u))
            if (n < 0.5f) drawCircle(Color(0xFFFFD447).copy(alpha = 1f - n * 2f), 0.03f * u, Offset(0.08f * u, -0.26f * u))
            else drawCircle(Color(0xFFFFF0BF).copy(alpha = n), 0.022f * u, Offset(0.08f * u, -0.26f * u))
            // A bird sits on the sill now and then.
            val bird = true
            if (bird) {
                val b = Offset(0.09f * u, -0.034f * u)
                drawOval(Color(0xFFFFFFFF), Offset(b.x - 0.014f * u, b.y - 0.018f * u), Size(0.028f * u, 0.02f * u))
                drawCircle(Color(0xFFE7A25A), 0.009f * u, Offset(b.x + 0.012f * u, b.y - 0.018f * u))
                drawCircle(Ink.line, 0.002f * u, Offset(b.x + 0.014f * u, b.y - 0.019f * u))
            }
        }
    }
    // Muntins.
    if (arched) {
        drawLine(Color(0xFFF7F3EC), Offset(0f, (-h + gm / u + 0.02f) * u), Offset(0f, -gm), strokeWidth = 0.008f * u)
        drawLine(Color(0xFFF7F3EC), Offset(-w / 2f * u + gm, (-h * 0.5f) * u), Offset(w / 2f * u - gm, (-h * 0.5f) * u), strokeWidth = 0.008f * u)
    } else {
        drawLine(Color(0xFFF7F3EC), Offset(0f, -h * u + gm), Offset(0f, -gm), strokeWidth = 0.008f * u)
        for (k in 1..2) drawLine(Color(0xFFF7F3EC), Offset(-w / 2f * u + gm, -h * u * k / 3f), Offset(w / 2f * u - gm, -h * u * k / 3f), strokeWidth = 0.008f * u)
    }
    drawPath(glass, Ink.line, style = pen.thin)
    drawPath(frame, Ink.line, style = pen.stroke)
    // A glassy shine.
    drawLine(Color.White.copy(alpha = 0.4f), Offset(-w / 2f * u + 0.035f * u, -0.05f * u), Offset(-w / 2f * u + 0.07f * u, -0.12f * u), strokeWidth = 0.01f * u, cap = StrokeCap.Round)
    // The sill.
    fxBox(u, -w / 2f - 0.02f, -0.012f, w / 2f + 0.02f, 0.008f, 0.04f, Color(0xFFF7F3EC), pen, rad = 0.004f)
    curtains(u, -w / 2f - 0.03f, w / 2f + 0.03f, -h - 0.01f, -0.012f, closed, if (arched) UpC.lilac else UpC.mint, pen, t)
    if (arched) {
        // A small plant on the sill.
        inkedRound(Rect(0.07f * u, -0.04f * u, 0.105f * u, -0.012f * u), 0.005f * u, UpC.terracotta(), pen, shade = false)
        for (a in listOf(-30f, 0f, 30f)) rotate(a, Offset(0.0875f * u, -0.04f * u)) { inked(fxLeaf(0.0875f * u, -0.04f * u, 0.0875f * u, -0.075f * u, 0.4f), UpC.leaf, pen, shade = false) }
    }
}

private fun UpC.terracotta(): Color = Color(0xFFD9774F)

// ---------------------------------------------------------------------------------------------- window seat

/** A built-in bench under the window: drawers, a green cushion, two big pillows and a folded blanket. */
internal fun DrawScope.upWindowSeat(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    upShadow(u, 0.5f, 0.14f)
    // The pillows leaning on the wall, at the back.
    fxBox(u, -0.235f, -0.2f, -0.12f, -0.1f, 0.05f, Color(0xFFFFE08A), pen, rad = 0.02f, z = 0.09f)
    fxBox(u, 0.12f, -0.19f, 0.225f, -0.1f, 0.05f, Color(0xFFF08CB8), pen, rad = 0.02f, z = 0.09f)
    // The bench: oak with two drawers.
    fxBox(u, -0.25f, -0.088f, 0.25f, 0f, 0.14f, UpC.oak, pen, rad = 0.006f)
    for (s in listOf(-1f, 1f)) {
        val c = p(s * 0.11f, -0.045f)
        inkedRound(Rect(c.x - 0.085f * u, c.y - 0.03f * u, c.x + 0.085f * u, c.y + 0.03f * u), 0.004f * u, UpC.oak.lighten(0.1f), pen, shade = false)
        inkedCircle(c, 0.007f * u, UpC.brass, pen, shade = false)
    }
    fxGrain(Rect(-0.24f * u, -0.082f * u, 0.24f * u, -0.006f * u), UpC.oak, pen, 1)
    // The cushion on top, green with white dots.
    fxBox(u, -0.24f, -0.103f, 0.24f, -0.086f, 0.135f, Color(0xFF5DB380), pen, rad = 0.008f, z = 0.003f, top = Color(0xFF7BCB98))
    val cush = fxFlat(u, -0.24f, 0.24f, -0.103f, 0.003f, 0.138f, 0.008f)
    clipPath(cush) {
        for (j in 0 until 3) for (i in 0 until 12) {
            val c = fxQ(u, -0.215f + i * 0.04f + (if (j % 2 == 0) 0f else 0.02f), -0.103f, 0.02f + j * 0.045f)
            drawCircle(Color.White.copy(alpha = 0.8f), 0.004f * u, c)
        }
    }
    // A blanket folded at one end.
    fxBox(u, 0.13f, -0.126f, 0.235f, -0.1f, 0.1f, Color(0xFFF2A65A), pen, rad = 0.01f, z = 0.02f)
    for (k in 0..4) fxLine(fxQ(u, 0.14f + k * 0.02f, -0.12f, 0.02f), fxQ(u, 0.14f + k * 0.02f, -0.104f, 0.02f), Color(0xFFD9803A), pen.lw * 0.8f)
}

// ---------------------------------------------------------------------------------------------- the toy train

private class TrackGeo(val bed: Path, val railA: Path, val railB: Path, val ties: List<Offset>)

private val trackGeo = Memo { u ->
    val n = 56
    val bed = Path()
    val a = Path()
    val b = Path()
    val ties = ArrayList<Offset>(60)
    for (i in 0..n) {
        val ang = i * 2f * PI.toFloat() / n
        val cx = UpperTrack.x(ang) * u
        val cy = UpperTrack.y(ang) * u
        // The tangent in pixels, and a normal to it.
        val e = 0.01f
        val dx = (UpperTrack.x(ang + e) - UpperTrack.x(ang - e)) * u
        val dy = (UpperTrack.y(ang + e) - UpperTrack.y(ang - e)) * u
        val l = max(0.0001f, kotlin.math.sqrt(dx * dx + dy * dy))
        val nx = -dy / l
        val ny = dx / l
        val rail = 0.013f * u
        if (i == 0) {
            bed.moveTo(cx, cy); a.moveTo(cx + nx * rail, cy + ny * rail); b.moveTo(cx - nx * rail, cy - ny * rail)
        } else {
            bed.lineTo(cx, cy); a.lineTo(cx + nx * rail, cy + ny * rail); b.lineTo(cx - nx * rail, cy - ny * rail)
        }
        if (i % 2 == 0 && i < n) {
            ties.add(Offset(cx + nx * 0.02f * u, cy + ny * 0.02f * u)); ties.add(Offset(cx - nx * 0.02f * u, cy - ny * 0.02f * u))
        }
    }
    bed.close(); a.close(); b.close()
    TrackGeo(bed, a, b, ties)
}

/** One car of the train at angle [a] on the loop, facing the way it goes. */
private fun DrawScope.trainCar(f: Fixture, u: Float, pen: Pen, a: Float, kind: Int, running: Boolean, front: Boolean) {
    val c = Offset(UpperTrack.x(a) * u, UpperTrack.y(a) * u)
    val dir = UpperTrack.heading(a)
    val bob = if (running) sin(pen.t * 24f + a * 3f) * 0.0012f * u else 0f
    translate(c.x, c.y + bob) {
        scale(dir, 1f, pivot = Offset.Zero) {
            if (front) {
                // Only the near wall of a wagon, over whoever sits in it.
                if (kind > 0) {
                    val col = if (kind == 1) UpC.yellow else UpC.sky
                    val wall = Rect(-0.044f * u, -0.05f * u, 0.044f * u, -0.012f * u)
                    inkedRound(wall, 0.008f * u, col, pen)
                    drawLine(col.darken(0.25f), Offset(wall.left + 0.006f * u, wall.center.y), Offset(wall.right - 0.006f * u, wall.center.y), strokeWidth = pen.lw * 0.8f)
                    for (s in listOf(-1f, 1f)) {
                        val wc = Offset(s * 0.026f * u, -0.006f * u)
                        inkedCircle(wc, 0.012f * u, UpC.dark.lighten(0.2f), pen, shade = false)
                        drawCircle(UpC.brass, 0.004f * u, wc)
                    }
                }
                return@scale
            }
            if (kind == 0) {
                // The engine: a red boiler, a yellow cab, a tall chimney and a cow-catcher; steam when it is running.
                val boiler = Rect(-0.02f * u, -0.058f * u, 0.062f * u, -0.016f * u)
                inkedRound(boiler, 0.014f * u, UpC.red, pen)
                inkedRound(Rect(-0.055f * u, -0.074f * u, -0.012f * u, -0.016f * u), 0.006f * u, UpC.yellow, pen)
                drawRect(Color(0xFFCFEFFF), Offset(-0.046f * u, -0.063f * u), Size(0.022f * u, 0.02f * u))
                drawRect(Ink.line, Offset(-0.046f * u, -0.063f * u), Size(0.022f * u, 0.02f * u), style = pen.thin)
                inkedRound(Rect(-0.06f * u, -0.082f * u, -0.007f * u, -0.07f * u), 0.004f * u, UpC.red.darken(0.2f), pen, shade = false)
                val chim = Path().apply { poly(0.036f * u, -0.058f * u, 0.052f * u, -0.058f * u, 0.058f * u, -0.095f * u, 0.03f * u, -0.095f * u) }
                inked(chim, UpC.dark.lighten(0.15f), pen)
                inkedRound(Rect(0.026f * u, -0.102f * u, 0.062f * u, -0.093f * u), 0.004f * u, UpC.brass, pen, shade = false)
                inkedCircle(Offset(0.012f * u, -0.066f * u), 0.009f * u, UpC.brass, pen, shade = false)
                val catcher = Path().apply { poly(0.062f * u, -0.03f * u, 0.082f * u, -0.008f * u, 0.062f * u, -0.008f * u) }
                inked(catcher, UpC.steelDark, pen, shade = false)
                if (running) fxPuffs(0.045f * u, -0.104f * u, pen.t, 0.012f * u, 0.07f * u, Color.White, 0.8f, 4, 0.6f, -0.03f * u * dir)
                for ((i, wx) in listOf(-0.032f, 0.004f, 0.044f).withIndex()) {
                    val wc = Offset(wx * u, -0.011f * u)
                    val r = if (i == 0) 0.013f else 0.011f
                    inkedCircle(wc, r * u, UpC.dark.lighten(0.2f), pen, shade = false)
                    drawCircle(UpC.brass, 0.004f * u, wc)
                }
                drawLine(UpC.brass, Offset(-0.032f * u, -0.011f * u), Offset(0.044f * u, -0.011f * u), strokeWidth = pen.lw * 0.9f)
            } else {
                // A wagon: an open box with a coloured side, the passenger sits in it.
                val col = if (kind == 1) UpC.yellow else UpC.sky
                val tub = Rect(-0.046f * u, -0.05f * u, 0.046f * u, -0.014f * u)
                inkedRound(tub, 0.008f * u, col.darken(0.15f), pen, shade = false)
                drawRect(col.darken(0.35f), Offset(tub.left + 0.006f * u, tub.top + 0.004f * u), Size(tub.width - 0.012f * u, 0.014f * u))
                drawLine(UpC.dark, Offset(-0.06f * u, -0.02f * u), Offset(-0.046f * u, -0.02f * u), strokeWidth = pen.lw * 1.6f, cap = StrokeCap.Round)
            }
        }
    }
}

/** The track on the floor with a toy village in the middle, and the train: an engine and two wagons that circle it. */
internal fun DrawScope.upTrain(f: Fixture, u: Float, pen: Pen) {
    val geo = trackGeo.of(u)
    val running = f.on
    // The track: a wooden bed, sleepers, two rails.
    translate(0f, 0f) {
        drawPath(geo.bed, Ink.shadow, style = Stroke(0.05f * u, join = StrokeJoin.Round))
        drawPath(geo.bed, Ink.line, style = Stroke(0.044f * u + pen.lw * 2f, join = StrokeJoin.Round))
        drawPath(geo.bed, Color(0xFFD9B27A), style = Stroke(0.044f * u, join = StrokeJoin.Round))
        drawPoints(geo.ties, androidx.compose.ui.graphics.PointMode.Lines, Color(0xFF8A5A33), strokeWidth = 0.007f * u, cap = StrokeCap.Round)
        drawPath(geo.railA, Ink.line, style = Stroke(0.006f * u + pen.lw))
        drawPath(geo.railA, UpC.steel, style = Stroke(0.006f * u))
        drawPath(geo.railB, Ink.line, style = Stroke(0.006f * u + pen.lw))
        drawPath(geo.railB, UpC.steel, style = Stroke(0.006f * u))
    }
    // The village in the middle of the loop: two toy trees and a little station with a flag.
    fun tree(x: Float, y: Float) {
        drawRect(Color(0xFF8A5A33), Offset((x - 0.004f) * u, (y - 0.03f) * u), Size(0.008f * u, 0.03f * u))
        inkedCircle(Offset(x * u, (y - 0.05f) * u), 0.022f * u, UpC.leaf, pen)
    }
    tree(-0.12f, -0.002f)
    tree(0.1f, -0.03f)
    val st = Rect(-0.03f * u, -0.058f * u, 0.03f * u, -0.018f * u)
    inkedRound(Rect(st.left, st.top, st.right, st.bottom), 0.004f * u, Color(0xFFF7F3EC), pen)
    val roofP = Path().apply { poly(st.left - 0.008f * u, st.top, 0f, st.top - 0.026f * u, st.right + 0.008f * u, st.top) }
    inked(roofP, UpC.falun, pen)
    inkedRound(Rect(-0.008f * u, st.bottom - 0.026f * u, 0.008f * u, st.bottom), 0.003f * u, UpC.walnut, pen, shade = false)
    drawRect(UpC.sky, Offset(st.left + 0.007f * u, st.top + 0.008f * u), Size(0.012f * u, 0.012f * u))
    drawRect(UpC.sky, Offset(st.right - 0.019f * u, st.top + 0.008f * u), Size(0.012f * u, 0.012f * u))
    // The cars, far ones first.
    val angles = listOf(f.angle to 0, f.angle - UpperTrack.GAP to 1, f.angle - 2f * UpperTrack.GAP to 2)
    for ((a, kind) in angles.sortedByDescending { sin(it.first) }) trainCar(f, u, pen, a, kind, running, front = false)
    // A signal at the station: green when it runs, red when it waits.
    val sg = Offset(-0.12f * u, 0.026f * u)
    drawLine(Ink.line, sg, Offset(sg.x, sg.y - 0.05f * u), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    inkedCircle(Offset(sg.x, sg.y - 0.056f * u), 0.009f * u, if (running) UpC.green else UpC.red, pen, shade = false)
}

internal fun DrawScope.upTrainFront(f: Fixture, u: Float, pen: Pen) {
    val angles = listOf(f.angle - UpperTrack.GAP to 1, f.angle - 2f * UpperTrack.GAP to 2)
    for ((a, kind) in angles) trainCar(f, u, pen, a, kind, f.on, front = true)
}

// ---------------------------------------------------------------------------------------------- blocks

private val blockColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFD447), Color(0xFF6BCB77), Color(0xFF5AA9F5), Color(0xFFB983FF), Color(0xFFFF9F43))

/** The picture on a block's face: a star, a circle, a triangle, a heart, a moon, a sun. */
private fun DrawScope.blockPicture(kind: Int, c: Offset, s: Float, pen: Pen) {
    val white = Color.White.copy(alpha = 0.92f)
    when (kind % 6) {
        0 -> drawPath(starPath(c, s * 0.34f, s * 0.15f), white)
        1 -> drawCircle(white, s * 0.27f, c)
        2 -> drawPath(Path().apply { poly(c.x - s * 0.3f, c.y + s * 0.22f, c.x + s * 0.3f, c.y + s * 0.22f, c.x, c.y - s * 0.3f) }, white)
        3 -> drawPath(fxHeart(c.x, c.y, s * 0.22f), white)
        4 -> { drawCircle(white, s * 0.27f, c); drawCircle(blockColors[kind % 6].darken(0.0f), s * 0.24f, Offset(c.x + s * 0.13f, c.y - s * 0.05f)) }
        else -> { drawCircle(white, s * 0.2f, c); for (k in 0 until 8) { val a = k * 0.7854f; drawLine(white, Offset(c.x + cos(a) * s * 0.26f, c.y + sin(a) * s * 0.26f), Offset(c.x + cos(a) * s * 0.36f, c.y + sin(a) * s * 0.36f), strokeWidth = s * 0.07f, cap = StrokeCap.Round) } }
    }
}

private fun DrawScope.block(u: Float, cx: Float, bottom: Float, size: Float, kind: Int, rot: Float, pen: Pen) {
    val s = size * u
    val c = Offset(cx * u, (bottom - size / 2f) * u)
    rotate(rot, c) {
        fxBox(u, cx - size / 2f, bottom - size, cx + size / 2f, bottom, size * 0.9f, blockColors[kind % 6], pen, rad = 0.006f)
        blockPicture(kind, c, s, pen)
    }
}

/** A tower of six toy blocks; it tumbles down when tapped and the blocks hop back up when it is built again. */
internal fun DrawScope.upBlocks(f: Fixture, u: Float, pen: Pen) {
    val size = 0.05f
    upShadow(u, 0.14f, 0.06f, 0.8f)
    val fallen = f.mode == 1
    val anim = f.timer
    val towerX = FloatArray(6) { k -> (if (k % 2 == 0) -0.004f else 0.006f) + (k % 3 - 1) * 0.002f }
    val heapX = floatArrayOf(-0.11f, -0.045f, 0.03f, 0.1f, -0.01f, 0.07f)
    val heapY = floatArrayOf(0f, 0f, 0f, 0f, -0.05f, -0.05f)
    val heapRot = floatArrayOf(-12f, 7f, -5f, 14f, 22f, -18f)
    for (k in 0 until 6) {
        val kind = k
        // Position along the fall or the rebuild.
        var x: Float
        var y: Float
        var rot: Float
        if (fallen) {
            val s = ((anim - k * 0.04f) / 0.7f).coerceIn(0f, 1f)
            val ease = s * s
            x = lerpF(towerX[k], heapX[k], ease)
            // The fall is a hop: up a little, then down.
            y = lerpF(-size * (k + 1), heapY[k], ease) - sin(PI.toFloat() * s) * 0.04f * (1f + k * 0.2f)
            rot = heapRot[k] * ease + sin(s * 9f) * 6f * (1f - s)
        } else {
            val s = ((anim - k * 0.18f) / 0.45f).coerceIn(0f, 1f)
            val ease = 1f - (1f - s) * (1f - s)
            x = lerpF(heapX[k], towerX[k], ease)
            y = lerpF(heapY[k], -size * (k + 1), ease) - sin(PI.toFloat() * s) * 0.09f
            rot = heapRot[k] * (1f - ease)
        }
        // Once placed, a block is drawn by its bottom edge.
        block(u, x, y + size, size, kind, rot, pen)
    }
}

// ---------------------------------------------------------------------------------------------- the dollhouse

/** A tiny figure of the big house. */
private fun DrawScope.doll(c: Offset, tone: Int, kind: Int, u: Float, pen: Pen, wave: Float, t: Float) {
    val s = 0.0165f * u
    val body = Color(tone)
    when (kind) {
        2 -> {
            // An animal: a little round body with two ears.
            drawOval(body, Offset(c.x - s * 0.9f, c.y - s * 1.3f), Size(s * 1.8f, s * 1.3f))
            drawOval(Ink.line, Offset(c.x - s * 0.9f, c.y - s * 1.3f), Size(s * 1.8f, s * 1.3f), style = Stroke(pen.lw * 0.5f))
            drawCircle(body, s * 0.55f, Offset(c.x + s * 0.5f, c.y - s * 1.45f))
            drawCircle(Ink.line, s * 0.55f, Offset(c.x + s * 0.5f, c.y - s * 1.45f), style = Stroke(pen.lw * 0.5f))
            drawPath(Path().apply { poly(c.x + s * 0.2f, c.y - s * 1.85f, c.x + s * 0.35f, c.y - s * 2.35f, c.x + s * 0.55f, c.y - s * 1.85f) }, body)
        }
        3 -> {
            // The ghost: a white sheet.
            val g = Path().apply {
                moveTo(c.x - s * 0.7f, c.y); lineTo(c.x - s * 0.7f, c.y - s * 1.7f); quadraticTo(c.x, c.y - s * 2.7f, c.x + s * 0.7f, c.y - s * 1.7f)
                lineTo(c.x + s * 0.7f, c.y); lineTo(c.x + s * 0.35f, c.y - s * 0.4f); lineTo(c.x, c.y); lineTo(c.x - s * 0.35f, c.y - s * 0.4f); close()
            }
            drawPath(g, Color(0xFFF4F4FF))
            drawPath(g, Ink.line, style = Stroke(pen.lw * 0.5f))
            drawCircle(Ink.line, s * 0.13f, Offset(c.x - s * 0.25f, c.y - s * 1.6f)); drawCircle(Ink.line, s * 0.13f, Offset(c.x + s * 0.25f, c.y - s * 1.6f))
        }
        4 -> {
            // The robot: a grey box with an aerial.
            drawRect(Color(0xFFB7BECC), Offset(c.x - s * 0.7f, c.y - s * 1.9f), Size(s * 1.4f, s * 1.9f))
            drawRect(Ink.line, Offset(c.x - s * 0.7f, c.y - s * 1.9f), Size(s * 1.4f, s * 1.9f), style = Stroke(pen.lw * 0.5f))
            drawCircle(Color(0xFF7CFFB2), s * 0.2f, Offset(c.x, c.y - s * 1.4f))
            drawLine(Ink.line, Offset(c.x, c.y - s * 1.9f), Offset(c.x, c.y - s * 2.4f), strokeWidth = pen.lw * 0.5f)
        }
        else -> {
            val k = if (kind == 1) 0.8f else 1f
            val armUp = if (wave > 0f) sin(t * 12f) * s * 0.5f else 0f
            drawRect(body, Offset(c.x - s * 0.55f * k, c.y - s * 1.35f * k), Size(s * 1.1f * k, s * 1.35f * k))
            drawRect(Ink.line, Offset(c.x - s * 0.55f * k, c.y - s * 1.35f * k), Size(s * 1.1f * k, s * 1.35f * k), style = Stroke(pen.lw * 0.5f))
            drawCircle(Color(0xFFF2C29B), s * 0.55f * k, Offset(c.x, c.y - s * 1.9f * k))
            drawCircle(Ink.line, s * 0.55f * k, Offset(c.x, c.y - s * 1.9f * k), style = Stroke(pen.lw * 0.5f))
            if (wave > 0f) drawLine(Color(0xFFF2C29B), Offset(c.x + s * 0.55f * k, c.y - s * 1.2f * k), Offset(c.x + s * 1.0f * k + armUp, c.y - s * 2.1f * k), strokeWidth = s * 0.3f, cap = StrokeCap.Round)
        }
    }
}

/** The big house in small: open at the front, four floors and a garden, with tiny figures standing where the real ones are. */
internal fun DrawScope.upDollhouse(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val n = pen.night
    val d = 0.12f
    val l = -0.26f
    val r = 0.145f
    upShadow(u, 0.52f, d, 1f)
    // The garden to the right: grass, a pond and a tree.
    fxBox(u, 0.145f, -0.014f, 0.262f, 0f, 0.14f, Color(0xFF8CCB6A), pen, rad = 0.004f)
    drawOval(Color(0xFF5AA9E6), Offset(0.17f * u, -0.032f * u), Size(0.06f * u, 0.014f * u))
    drawRect(Color(0xFF8A5A33), Offset(0.236f * u, -0.06f * u), Size(0.008f * u, 0.046f * u))
    inkedCircle(p(0.24f, -0.085f), 0.026f * u, UpC.leaf, pen)
    // Floors: cellar base, ground, first, attic. Levels' heights are the tops of their floors.
    val lv = floatArrayOf(-0.058f, -0.222f, -0.386f)
    val wallCols = listOf(Color(0xFFFFE9C2), Color(0xFFCDE6F7), Color(0xFFE4D6F5))
    // The back walls and the rooms' own little details.
    val bodyL = l + 0.016f
    val bodyR = r - 0.016f
    for (k in 0 until 3) {
        val top = lv[k] - 0.152f
        val bot = lv[k] - 0.012f
        val z = d - 0.02f
        val a = q(bodyL, top, z)
        val b = q(bodyR, bot, z)
        drawRect(wallCols[k], a, Size(b.x - a.x, b.y - a.y))
        drawRect(Ink.line.copy(alpha = 0.5f), a, Size(b.x - a.x, b.y - a.y), style = pen.thin)
        // A window in the back wall, lit at night.
        val wc = q(-0.1f + (k % 2) * 0.12f, (top + bot) / 2f - 0.01f, z)
        drawRect(lerp(Color(0xFFBFE3FA), Color(0xFFFFD66B), n), Offset(wc.x - 0.016f * u, wc.y - 0.02f * u), Size(0.032f * u, 0.04f * u))
        drawRect(Color.White, Offset(wc.x - 0.016f * u, wc.y - 0.02f * u), Size(0.032f * u, 0.04f * u), style = Stroke(pen.lw))
        drawLine(Color.White, Offset(wc.x, wc.y - 0.02f * u), Offset(wc.x, wc.y + 0.02f * u), strokeWidth = pen.lw * 0.8f)
    }
    // Little furniture: a sofa downstairs, a bed on the first floor, a trunk in the attic.
    run {
        val s = q(-0.13f, lv[0] - 0.012f, 0.07f)
        inkedRound(Rect(s.x - 0.032f * u, s.y - 0.02f * u, s.x + 0.032f * u, s.y), 0.006f * u, Color(0xFFF08C6A), pen, shade = false)
        val b = q(0.06f, lv[1] - 0.012f, 0.07f)
        inkedRound(Rect(b.x - 0.035f * u, b.y - 0.018f * u, b.x + 0.035f * u, b.y), 0.006f * u, Color(0xFFF2B8B0), pen, shade = false)
        val tr = q(-0.03f, lv[2] - 0.012f, 0.07f)
        inkedRound(Rect(tr.x - 0.025f * u, tr.y - 0.016f * u, tr.x + 0.025f * u, tr.y), 0.004f * u, Color(0xFF8A5A33), pen, shade = false)
        // The stairs between the floors, drawn as a diagonal of steps at the back right.
        for (k in 0 until 2) {
            val a = q(0.1f, lv[k] - 0.012f, 0.1f)
            val b2 = q(0.0f, lv[k + 1] - 0.012f, 0.1f)
            drawLine(UpC.walnut, a, b2, strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
            for (j in 1..5) {
                val ft = j / 6f
                val pt = Offset(lerpF(a.x, b2.x, ft), lerpF(a.y, b2.y, ft))
                drawLine(UpC.oak, Offset(pt.x - 0.012f * u, pt.y), Offset(pt.x + 0.004f * u, pt.y), strokeWidth = pen.lw * 1.3f, cap = StrokeCap.Round)
            }
        }
    }
    // The floors and the side walls, as thin boxes whose tops we see.
    for (k in 0 until 3) fxBox(u, l, lv[k] - 0.012f, r, lv[k], d, Color(0xFFD9B27A), pen, rad = 0.002f, top = Color(0xFFEAD0A2))
    fxBox(u, l, -0.06f, r, 0f, d, Color(0xFF9D9AAA), pen, rad = 0.003f, top = Color(0xFFC3C0CF), side = Color(0xFF6C6985))
    // Cellar windows in the stone base.
    for (k in 0 until 4) {
        val wc = p(l + 0.05f + k * 0.09f, -0.03f)
        drawArc(Color(0xFF3A3A55), 180f, 180f, true, Offset(wc.x - 0.014f * u, wc.y - 0.014f * u), Size(0.028f * u, 0.028f * u))
        drawRect(Color(0xFF3A3A55), Offset(wc.x - 0.014f * u, wc.y), Size(0.028f * u, 0.014f * u))
        drawRect(lerp(Color(0xFF3A3A55), Color(0xFFFFD66B), n), Offset(wc.x - 0.01f * u, wc.y - 0.004f * u), Size(0.02f * u, 0.016f * u), alpha = 0.6f + 0.4f * n)
    }
    fxBox(u, l, -0.386f, l + 0.016f, -0.06f, d, Color(0xFFF7F3EC), pen, rad = 0.002f)
    fxBox(u, r - 0.016f, -0.386f, r, -0.06f, d, Color(0xFFF7F3EC), pen, rad = 0.002f)
    // The roof: a gable with a red slope that we see at the right, and a chimney with a little smoke.
    val ridge = -0.575f
    val eave = -0.386f
    val tri = floatArrayOf(l * u, eave * u, (l + r) / 2f * u, ridge * u, r * u, eave * u)
    prism(tri, d * u, Color(0xFF3A2F55), pen, shadeFront = false) { _, _ -> Color(0xFFC0463A) }
    // The attic's inside, seen through the open front, and rafters.
    val inside = Path().apply { poly((l + 0.02f) * u, (eave - 0.012f) * u, (l + r) / 2f * u, (ridge + 0.03f) * u, (r - 0.02f) * u, (eave - 0.012f) * u) }
    drawPath(inside, wallCols[2])
    clipPath(inside) {
        val rc = q((l + r) / 2f, -0.46f, d - 0.02f)
        // The window in the gable.
        drawCircle(lerp(Color(0xFFBFE3FA), Color(0xFFFFD66B), n), 0.017f * u, Offset(rc.x, rc.y))
        drawCircle(Color.White, 0.017f * u, Offset(rc.x, rc.y), style = Stroke(pen.lw))
        drawLine(Color.White, Offset(rc.x - 0.017f * u, rc.y), Offset(rc.x + 0.017f * u, rc.y), strokeWidth = pen.lw * 0.8f)
        drawLine(Color.White, Offset(rc.x, rc.y - 0.017f * u), Offset(rc.x, rc.y + 0.017f * u), strokeWidth = pen.lw * 0.8f)
    }
    drawPath(inside, Ink.line, style = pen.stroke)
    fxBox(u, 0.04f, -0.6f, 0.09f, -0.5f, 0.04f, Color(0xFF9D9AAA), pen, z = 0.04f, top = Color(0xFFC3C0CF), side = Color(0xFF6C6985))
    val sm = q(0.065f, -0.605f, 0.06f)
    drawCircle(Color.White.copy(alpha = 0.7f), 0.012f * u, Offset(sm.x + 0.004f * u, sm.y - 0.02f * u))
    drawCircle(Color.White.copy(alpha = 0.5f), 0.016f * u, Offset(sm.x + 0.012f * u, sm.y - 0.05f * u))
    // The flag on the top, and the tiny figures.
    val fl = q((l + r) / 2f, ridge - 0.002f, d / 2f)
    drawLine(Ink.line, fl, Offset(fl.x, fl.y - 0.04f * u), strokeWidth = pen.lw)
    drawPath(Path().apply { poly(fl.x, fl.y - 0.04f * u, fl.x + 0.022f * u, fl.y - 0.032f * u, fl.x, fl.y - 0.024f * u) }, UpC.red)
    val wave = f.timer
    val figs = UpperMirror.figures
    val rows = floatArrayOf(lv[0] - 0.012f, lv[1] - 0.012f, lv[2] - 0.012f, -0.058f, -0.014f)
    for ((i, m) in figs.withIndex()) {
        val x = if (m.floor == 4) lerpF(0.17f, 0.23f, m.x) else lerpF(bodyL + 0.03f, bodyR - 0.03f, m.x)
        val y = rows[m.floor.coerceIn(0, 4)]
        val z = if (m.floor == 4) 0.08f else if (m.floor == 3) 0.05f else 0.06f
        val c = q(x, y - if (m.floor == 3) 0.012f else 0f, z)
        val hop = if (wave > 0f) abs(sin(t * 2.2f + i * 1.7f)) * 0.0045f * u else 0f
        doll(Offset(c.x, c.y - hop), m.tone, m.kind, u, pen, wave, t + i)
    }
    // The front door's bell button.
    drawCircle(UpC.brass, 0.006f * u, p(l + 0.025f, -0.1f))
}

// ---------------------------------------------------------------------------------------------- the puppet theatre

/** A little puppet theatre with red curtains and a spangled stage; a crocodile or a rabbit pops up when it opens. */
internal fun DrawScope.upPuppets(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val t = pen.t
    upShadow(u, 0.34f, 0.1f)
    // The theatre: a box with a window, a header with stars and a scalloped valance.
    fxBox(u, -0.17f, -0.1f, 0.17f, 0f, 0.1f, Color(0xFF6E4630), pen, rad = 0.006f)
    for (k in 0 until 5) drawPath(starPath(p(-0.12f + k * 0.06f, -0.05f), 0.012f * u, 0.005f * u), UpC.yellow)
    fxBox(u, -0.15f, -0.4f, -0.1f, -0.1f, 0.08f, UpC.wood, pen, rad = 0.004f)
    fxBox(u, 0.1f, -0.4f, 0.15f, -0.1f, 0.08f, UpC.wood, pen, rad = 0.004f)
    val win = Rect(-0.1f * u, -0.36f * u, 0.1f * u, -0.11f * u)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF3B2A7A), Color(0xFF7A5CC8)), startY = win.top, endY = win.bottom), win.topLeft, win.size)
    val open = f.mode != 0
    clipRect(win.left, win.top, win.right, win.bottom) {
        // A moon and stars on the backcloth.
        drawCircle(Color(0xFFFFF0BF), 0.026f * u, p(0.06f, -0.3f))
        drawCircle(Color(0xFF4B3A96), 0.022f * u, p(0.072f, -0.306f))
        for (k in 0 until 6) drawCircle(Color.White.copy(alpha = if (open) 0.5f + 0.5f * sin(t * 2f + k * 1.9f) else 0.8f), 0.003f * u, p(-0.08f + k * 0.03f, -0.33f + (k % 3) * 0.03f))
        if (open) {
            val bob = sin(t * 3.1f) * 0.01f
            if (f.mode == 1) {
                // The crocodile: a green head that pops up and chomps.
                val c = p(-0.01f, -0.11f - 0.07f - 0.04f * (0.5f + 0.5f * sin(t * 2.6f)) + bob)
                val chomp = 0.5f + 0.5f * sin(t * 7f)
                inkedRound(Rect(c.x - 0.045f * u, c.y - 0.07f * u, c.x + 0.045f * u, c.y + 0.06f * u + 0.1f * u), 0.02f * u, UpC.green, pen, shade = false)
                inkedRound(Rect(c.x - 0.04f * u, c.y - 0.05f * u, c.x + 0.075f * u, c.y - 0.012f * u + 0.004f * u), 0.012f * u, UpC.green, pen, shade = false)
                val jaw = c.y + 0.002f * u + chomp * 0.022f * u
                inkedRound(Rect(c.x - 0.03f * u, jaw - 0.01f * u, c.x + 0.07f * u, jaw + 0.018f * u), 0.008f * u, UpC.green.darken(0.15f), pen, shade = false)
                drawRect(Color(0xFF7A2440), Offset(c.x - 0.022f * u, c.y - 0.014f * u), Size(0.085f * u, 0.022f * u + chomp * 0.022f * u))
                for (k in 0 until 4) drawPath(Path().apply { val tx = c.x - 0.015f * u + k * 0.02f * u; poly(tx, c.y - 0.014f * u, tx + 0.007f * u, c.y - 0.014f * u, tx + 0.0035f * u, c.y - 0.004f * u) }, Color.White)
                for (s in listOf(-0.02f, 0.012f)) {
                    drawCircle(Color.White, 0.012f * u, Offset(c.x + s * u, c.y - 0.058f * u))
                    drawCircle(Ink.line, 0.006f * u, Offset(c.x + s * u + 0.002f * u, c.y - 0.058f * u))
                }
            } else {
                // The rabbit: long ears and a bow tie.
                val c = p(0.0f, -0.11f - 0.075f - 0.03f * (0.5f + 0.5f * sin(t * 2.3f)) + bob)
                inkedRound(Rect(c.x - 0.04f * u, c.y - 0.01f * u, c.x + 0.04f * u, c.y + 0.12f * u), 0.02f * u, Color(0xFFF1E7F7), pen, shade = false)
                for (s in listOf(-1f, 1f)) {
                    val tilt = sin(t * 3f + s) * 6f
                    rotate(tilt, Offset(c.x + s * 0.015f * u, c.y - 0.03f * u)) {
                        inkedOval(Rect(c.x + s * 0.025f * u - 0.012f * u, c.y - 0.1f * u, c.x + s * 0.025f * u + 0.012f * u, c.y - 0.01f * u), Color(0xFFF1E7F7), pen, shade = false)
                        drawOval(Color(0xFFFFB6CE), Offset(c.x + s * 0.025f * u - 0.006f * u, c.y - 0.085f * u), Size(0.012f * u, 0.06f * u))
                    }
                }
                inkedCircle(Offset(c.x, c.y), 0.036f * u, Color(0xFFF1E7F7), pen, shade = false)
                for (s in listOf(-0.014f, 0.014f)) drawCircle(Ink.line, 0.004f * u, Offset(c.x + s * u, c.y - 0.005f * u))
                drawCircle(Color(0xFFFF8FB1), 0.006f * u, Offset(c.x, c.y + 0.008f * u))
                drawPath(Path().apply { poly(c.x - 0.02f * u, c.y + 0.045f * u, c.x + 0.02f * u, c.y + 0.065f * u, c.x - 0.02f * u, c.y + 0.065f * u, c.x + 0.02f * u, c.y + 0.045f * u) }, UpC.red)
            }
        }
    }
    drawRect(Ink.line, win.topLeft, win.size, style = pen.stroke)
    // The curtains: drawn across when closed, tied back when open.
    val sway = if (open) sin(t * 1.2f) * 0.002f * u else 0f
    if (!open) {
        for ((a, b) in listOf(-0.1f to 0f, 0f to 0.1f)) {
            val r = Rect(a * u, win.top, b * u, win.bottom)
            drawRect(UpC.red, r.topLeft, r.size)
            var x = r.left + 0.01f * u
            while (x < r.right) { drawLine(UpC.red.darken(0.25f), Offset(x + sway, r.top), Offset(x, r.bottom), strokeWidth = pen.lw * 0.8f); x += 0.022f * u }
            drawRect(Ink.line, r.topLeft, r.size, style = pen.thin)
        }
    } else {
        for (s in listOf(-1f, 1f)) {
            val cur = Path().apply {
                moveTo(s * 0.1f * u, win.top); lineTo(s * 0.055f * u, win.top)
                quadraticTo(s * 0.085f * u, (win.top + win.bottom) / 2f, s * 0.06f * u + sway, win.bottom); lineTo(s * 0.1f * u, win.bottom); close()
            }
            inked(cur, UpC.red, pen)
            drawLine(UpC.brass, p(s * 0.08f, -0.24f), p(s * 0.1f, -0.235f), strokeWidth = pen.lw * 1.8f, cap = StrokeCap.Round)
        }
    }
    // The valance: a scalloped red band.
    val val0 = -0.405f
    val band = Path().apply {
        moveTo(-0.14f * u, (val0 - 0.03f) * u); lineTo(0.14f * u, (val0 - 0.03f) * u); lineTo(0.14f * u, val0 * u)
        for (k in 6 downTo 0) {
            val x0 = -0.14f + 0.28f * k / 7f
            val x1 = -0.14f + 0.28f * (k + 1) / 7f
            quadraticTo((x0 + x1) / 2f * u, (val0 + 0.032f) * u, x0 * u, val0 * u)
        }
        close()
    }
    inked(band, UpC.red, pen)
    for (k in 0 until 7) drawPath(starPath(p(-0.12f + k * 0.04f, val0 - 0.012f), 0.006f * u, 0.0025f * u), UpC.yellow)
}

// ---------------------------------------------------------------------------------------------- the moon lamp

/** A sleepy crescent-moon lamp on a stand; it glows warm when it is on. */
internal fun DrawScope.upNightLamp(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    val on = f.on
    upShadow(u, 0.1f, 0.06f)
    // A round base and a thin stem.
    inkedRound(Rect(-0.04f * u, -0.025f * u, 0.04f * u, 0f), 0.01f * u, UpC.wood, pen)
    capsule(p(0f, -0.02f), p(0f, -0.12f), 0.01f * u, UpC.walnut, pen)
    val c = p(0f, -0.185f)
    val r = 0.062f * u
    if (on) {
        fxGlow(c, 0.2f * u, Color(0xFFFFD27A), 0.9f)
        fxGlow(c, 0.1f * u, Color(0xFFFFF0B0), 0.9f)
    }
    // The crescent: a circle with a bite taken out of the right side.
    val bite = Path().apply { addOval(Rect(c.x + r * 0.42f - r * 0.82f, c.y - r * 0.15f - r * 0.82f, c.x + r * 0.42f + r * 0.82f, c.y - r * 0.15f + r * 0.82f)) }
    clipPath(bite, ClipOp.Difference) {
        drawCircle(if (on) Color(0xFFFFE9A0) else Color(0xFFE6D58C), r, c)
        drawCircle(if (on) Color(0xFFFFF7D0) else Color(0xFFF1E3A0), r * 0.7f, Offset(c.x - r * 0.2f, c.y - r * 0.2f))
        drawCircle(Ink.line, r, c, style = pen.stroke)
    }
    clipPath(Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }) {
        drawCircle(Ink.line, r * 0.82f, Offset(c.x + r * 0.42f, c.y - r * 0.15f), style = pen.stroke)
    }
    // A sleepy face on the crescent.
    drawArc(Ink.line, 20f, 140f, false, Offset(c.x - r * 0.62f, c.y - r * 0.12f), Size(r * 0.34f, r * 0.2f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    drawArc(Ink.line, 30f, 120f, false, Offset(c.x - r * 0.62f, c.y + r * 0.3f), Size(r * 0.34f, r * 0.2f), style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
    drawCircle(Color(0x66FF6F91), r * 0.12f, Offset(c.x - r * 0.62f, c.y + r * 0.1f))
    // Two little stars hanging from it.
    for ((k, dx) in listOf(-0.075f, 0.085f).withIndex()) {
        val sc = p(dx, -0.15f + k * 0.03f)
        drawLine(Ink.line, Offset(sc.x, sc.y - 0.03f * u), sc, strokeWidth = pen.lw * 0.5f)
        drawPath(starPath(Offset(sc.x, sc.y + 0.008f * u), 0.012f * u, 0.005f * u), if (on) UpC.yellow.lighten(0.2f) else UpC.yellow)
        drawPath(starPath(Offset(sc.x, sc.y + 0.008f * u), 0.012f * u, 0.005f * u), Ink.line, style = pen.thin)
    }
}

// ---------------------------------------------------------------------------------------------- posters

/** A poster on the wall, taped at the corners: a rocket (variant 0), a dinosaur, a rainbow or a fish. When tapped it comes to life. */
internal fun DrawScope.upPoster(f: Fixture, u: Float, pen: Pen) {
    val v = f.variant.coerceIn(0, 3)
    val t = pen.t
    val alive = (f.timer / 1.4f).coerceIn(0f, 1f)
    val w = 0.16f * u
    val h = 0.2f * u
    val rect = Rect(-w / 2f, -h, w / 2f, 0f)
    drawRect(Ink.shadow, Offset(rect.left + 0.004f * u, rect.top + 0.005f * u), rect.size)
    val bg = listOf(Color(0xFF1F2A66), Color(0xFFFFE8B0), Color(0xFFD9F0FF), Color(0xFF9ADAF2))[v]
    drawRect(bg, rect.topLeft, rect.size)
    clipRect(rect.left, rect.top, rect.right, rect.bottom) {
        val cx = rect.center.x
        when (v) {
            0 -> {
                for (k in 0 until 9) drawCircle(Color.White.copy(alpha = if (alive > 0f) 0.5f + 0.5f * sin(t * 6f + k * 1.7f) else 0.85f), 0.003f * u, Offset(rect.left + w * hash01(k, 151), rect.top + h * hash01(k, 152) * 0.8f))
                drawCircle(Color(0xFFF2A65A), 0.032f * u, Offset(rect.right - 0.03f * u, rect.top + 0.05f * u))
                drawArc(Color(0xFFD9803A), 0f, 360f, false, Offset(rect.right - 0.044f * u, rect.top + 0.036f * u), Size(0.028f * u, 0.028f * u), style = pen.thin)
                // The rocket takes off when the poster is tapped.
                val lift = alive * 0.1f * u
                translate(sin(alive * 20f) * 0.002f * u, -lift) {
                    val rk = Offset(cx - 0.012f * u, rect.bottom - 0.065f * u)
                    val flame = 0.014f * u + (if (alive > 0f) 0.012f * u * sin(t * 25f).coerceAtLeast(0f) else 0f) + alive * 0.03f * u
                    drawPath(Path().apply { poly(rk.x - 0.01f * u, rk.y + 0.045f * u, rk.x + 0.01f * u, rk.y + 0.045f * u, rk.x, rk.y + 0.045f * u + flame * 2f) }, UpC.orange)
                    drawPath(Path().apply { poly(rk.x - 0.005f * u, rk.y + 0.045f * u, rk.x + 0.005f * u, rk.y + 0.045f * u, rk.x, rk.y + 0.045f * u + flame) }, Color(0xFFFFEE88))
                    val body = Path().apply {
                        moveTo(rk.x, rk.y - 0.05f * u); quadraticTo(rk.x + 0.026f * u, rk.y - 0.015f * u, rk.x + 0.02f * u, rk.y + 0.045f * u)
                        lineTo(rk.x - 0.02f * u, rk.y + 0.045f * u); quadraticTo(rk.x - 0.026f * u, rk.y - 0.015f * u, rk.x, rk.y - 0.05f * u); close()
                    }
                    inked(body, Color(0xFFF7F3EC), pen)
                    drawPath(Path().apply { poly(rk.x - 0.02f * u, rk.y + 0.02f * u, rk.x - 0.036f * u, rk.y + 0.05f * u, rk.x - 0.016f * u, rk.y + 0.045f * u) }, UpC.red)
                    drawPath(Path().apply { poly(rk.x + 0.02f * u, rk.y + 0.02f * u, rk.x + 0.036f * u, rk.y + 0.05f * u, rk.x + 0.016f * u, rk.y + 0.045f * u) }, UpC.red)
                    inkedCircle(Offset(rk.x, rk.y - 0.004f * u), 0.008f * u, UpC.sky, pen, shade = false)
                    drawPath(Path().apply { poly(rk.x - 0.012f * u, rk.y - 0.028f * u, rk.x + 0.012f * u, rk.y - 0.028f * u, rk.x, rk.y - 0.05f * u) }, UpC.red)
                }
            }
            1 -> {
                // A friendly green dinosaur with plates on its back; it roars with little music notes.
                drawRect(Color(0xFF8FCB6A), Offset(rect.left, rect.bottom - 0.04f * u), Size(w, 0.04f * u))
                drawCircle(Color(0xFFFFC83D), 0.018f * u, Offset(rect.left + 0.03f * u, rect.top + 0.03f * u))
                val d = Offset(cx, rect.bottom - 0.04f * u)
                val body = Path().apply {
                    moveTo(d.x - 0.06f * u, d.y); quadraticTo(d.x - 0.07f * u, d.y - 0.07f * u, d.x - 0.01f * u, d.y - 0.08f * u)
                    quadraticTo(d.x + 0.02f * u, d.y - 0.1f * u, d.x + 0.03f * u, d.y - 0.12f * u)
                    quadraticTo(d.x + 0.06f * u, d.y - 0.15f * u, d.x + 0.075f * u, d.y - 0.11f * u)
                    quadraticTo(d.x + 0.08f * u, d.y - 0.085f * u, d.x + 0.04f * u, d.y - 0.07f * u)
                    quadraticTo(d.x + 0.05f * u, d.y - 0.03f * u, d.x + 0.04f * u, d.y); close()
                }
                inked(body, Color(0xFF55BD73), pen)
                for (k in 0 until 4) drawPath(Path().apply { val px = d.x - 0.03f * u + k * 0.012f * u; val py = d.y - 0.075f * u - k * 0.008f * u; poly(px, py, px + 0.007f * u, py - 0.016f * u, px + 0.014f * u, py + 0.002f * u) }, UpC.orange)
                drawCircle(Color.White, 0.007f * u, Offset(d.x + 0.05f * u, d.y - 0.12f * u))
                drawCircle(Ink.line, 0.0035f * u, Offset(d.x + 0.052f * u, d.y - 0.12f * u))
                val mouth = 0.003f * u + alive * 0.01f * u
                drawLine(Ink.line, Offset(d.x + 0.042f * u, d.y - 0.1f * u), Offset(d.x + 0.074f * u, d.y - 0.1f * u + mouth), strokeWidth = pen.lw, cap = StrokeCap.Round)
                if (alive > 0.05f) for (k in 0 until 3) {
                    val ph = (alive * 1.5f + k * 0.33f) % 1f
                    val np = Offset(d.x + 0.1f * u + ph * 0.04f * u, d.y - 0.13f * u - ph * 0.06f * u)
                    drawOval(Ink.line, Offset(np.x - 0.005f * u, np.y - 0.003f * u), Size(0.01f * u, 0.007f * u))
                    drawLine(Ink.line, Offset(np.x + 0.004f * u, np.y), Offset(np.x + 0.004f * u, np.y - 0.02f * u), strokeWidth = pen.lw)
                }
            }
            2 -> {
                val cc = Offset(cx, rect.bottom - 0.03f * u)
                val cols = listOf(Color(0xFFFF6B6B), Color(0xFFFFA24D), Color(0xFFFFE066), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFF8B5CF6))
                for ((k, c) in cols.withIndex()) {
                    val rr = (0.07f - k * 0.0095f) * u
                    drawArc(c, 180f, 180f, false, Offset(cc.x - rr, cc.y - rr), Size(rr * 2f, rr * 2f), style = Stroke(0.0105f * u))
                }
                drawPath(cloudPath(cc.x - 0.07f * u, cc.y + 0.004f * u, 0.022f * u), Color.White)
                drawPath(cloudPath(cc.x + 0.07f * u, cc.y + 0.004f * u, 0.022f * u), Color.White)
                for (k in 0 until 3) {
                    val tw = if (alive > 0f) 0.5f + 0.5f * sin(t * 3f + k * 2f + alive * 20f) else 0.8f
                    twinkle(Offset(rect.left + 0.03f * u + k * 0.05f * u, rect.top + 0.035f * u + (k % 2) * 0.025f * u), (0.012f + 0.014f * alive) * u * tw, Color.White, 1f)
                }
            }
            else -> {
                // A blue fish with bubbles; it flips its tail.
                val fc = Offset(cx - 0.005f * u, rect.top + h * 0.55f)
                val tail = 0f
                drawPath(Path().apply { poly(fc.x - 0.05f * u, fc.y, fc.x - 0.085f * u, fc.y - 0.03f * u + tail, fc.x - 0.085f * u, fc.y + 0.03f * u + tail) }, UpC.orange)
                drawOval(Color(0xFF3D7FD6), Offset(fc.x - 0.055f * u, fc.y - 0.035f * u), Size(0.11f * u, 0.07f * u))
                drawOval(Ink.line, Offset(fc.x - 0.055f * u, fc.y - 0.035f * u), Size(0.11f * u, 0.07f * u), style = pen.thin)
                drawCircle(Color.White, 0.01f * u, Offset(fc.x + 0.03f * u, fc.y - 0.008f * u))
                drawCircle(Ink.line, 0.005f * u, Offset(fc.x + 0.032f * u, fc.y - 0.008f * u))
                for (k in 0 until 4) {
                    val ph = if (alive > 0f) (t * 0.4f + k * 0.25f + alive) % 1f else k * 0.25f + 0.1f
                    drawCircle(Color.White.copy(alpha = 0.8f * (1f - ph)), (0.005f + 0.002f * k) * u, Offset(fc.x + 0.05f * u + k * 0.006f * u, fc.y - 0.04f * u - ph * 0.08f * u), style = pen.thin)
                }
            }
        }
    }
    drawRect(Ink.line, rect.topLeft, rect.size, style = pen.stroke)
    // Tape at the top corners.
    for (s in listOf(rect.left, rect.right)) {
        rotate(if (s < 0f) -30f else 30f, Offset(s, rect.top)) { drawRect(Color(0xCCFFE9A8), Offset(s - 0.014f * u, rect.top - 0.006f * u), Size(0.028f * u, 0.012f * u)) }
    }
}

// ---------------------------------------------------------------------------------------------- the mobile

/** A mobile of felt shapes that turns on its own and spins when it is tapped. */
internal fun DrawScope.upMobile(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val spin = f.angle + t * 0.45f
    val top = Offset(0f, -0.3f * u)
    // A cord up to the hook, and a ring with a cross-bar.
    drawLine(Ink.line, top, Offset(0f, top.y + 0.05f * u), strokeWidth = pen.lw * 0.8f)
    drawCircle(UpC.brass, 0.008f * u, Offset(0f, top.y + 0.05f * u))
    val barY = top.y + 0.065f * u
    capsule(Offset(-0.09f * u, barY), Offset(0.09f * u, barY), 0.007f * u, UpC.oak, pen)
    class Piece(val kind: Int, val color: Color, val len: Float, val phase: Float)
    val pieces = listOf(Piece(0, UpC.yellow, 0.11f, 0f), Piece(1, Color(0xFFF2E6A0), 0.15f, 1.6f), Piece(2, UpC.pink, 0.12f, 3.2f), Piece(3, UpC.sky, 0.17f, 4.7f))
    for (pc in pieces.sortedBy { sin(spin + it.phase) }) {
        val a = spin + pc.phase
        val x = cos(a) * 0.075f * u
        val depth = sin(a)
        val y = barY + pc.len * u + sin(t * 1.3f + pc.phase) * 0.003f * u
        drawLine(Ink.line.copy(alpha = 0.8f), Offset(x, barY), Offset(x, y), strokeWidth = pen.lw * 0.6f)
        val s = (0.9f + 0.1f * depth) * u
        val c = Offset(x, y + 0.02f * s / u)
        when (pc.kind) {
            0 -> {
                for (k in 0 until 8) {
                    val aa = k * 0.7854f
                    drawLine(pc.color, Offset(c.x + cos(aa) * 0.022f * s, c.y + sin(aa) * 0.022f * s), Offset(c.x + cos(aa) * 0.034f * s, c.y + sin(aa) * 0.034f * s), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
                }
                inkedCircle(c, 0.022f * s, pc.color, pen)
            }
            1 -> {
                val cr = Path().apply { addOval(Rect(c.x - 0.026f * s, c.y - 0.026f * s, c.x + 0.026f * s, c.y + 0.026f * s)) }
                clipPath(Path().apply { addOval(Rect(c.x + 0.012f * s - 0.024f * s, c.y - 0.012f * s - 0.024f * s, c.x + 0.012f * s + 0.024f * s, c.y - 0.012f * s + 0.024f * s)) }, ClipOp.Difference) {
                    drawPath(cr, pc.color); drawPath(cr, Ink.line, style = pen.stroke)
                }
            }
            2 -> inked(starPath(c, 0.03f * s, 0.013f * s), pc.color, pen)
            else -> {
                drawOval(pc.color.darken(0.2f), Offset(c.x - 0.04f * s, c.y - 0.008f * s), Size(0.08f * s, 0.016f * s), style = Stroke(0.006f * u))
                inkedCircle(c, 0.02f * s, pc.color, pen)
                drawArc(pc.color.darken(0.2f), 0f, 180f, false, Offset(c.x - 0.04f * s, c.y - 0.008f * s), Size(0.08f * s, 0.016f * s), style = Stroke(0.006f * u))
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------- the blanket fort

/** A fort of blankets over a frame, with flags on top; the door flap rolls up to show pillows, fairy lights and a teddy. */
internal fun DrawScope.upFort(f: Fixture, u: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val open = f.open
    val d = 0.2f
    upShadow(u, 0.46f, d, 1f)
    val apex = Offset(0f, -0.3f * u)
    val hw = 0.215f
    // The right-hand slope, patchwork, seen from the side as a parallelogram going back.
    val side = Path().apply {
        val a = q(hw, 0f, 0f)
        val b = q(hw, 0f, d)
        val c = q(0f, -0.3f, d)
        moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(0f, -0.3f * u); close()
    }
    fxFace(side, Color(0xFFE9A06A), pen)
    clipPath(side) {
        for (k in 0 until 6) for (j in 0 until 4) {
            val col = listOf(Color(0xFFFFD27A), Color(0xFFF08CB8), Color(0xFF8FD9C0), Color(0xFFB9A2F0))[(k + j) % 4]
            val a = q(hw * (1f - j / 4f), -0.3f * j / 4f, d * k / 6f)
            drawCircle(col.copy(alpha = 0.5f), 0.013f * u, a)
        }
    }
    // The front: a triangle of blanket with a door in it.
    val front = Path().apply { poly(-hw * u, 0f, 0f, -0.3f * u, hw * u, 0f) }
    if (open) {
        // The inside: a warm glow, fairy lights along the roof, a teddy and a flashlight.
        drawPath(front, Color(0xFF3B2540))
        clipPath(front) {
            fxGlow(p(0f, -0.1f), 0.2f * u, Color(0xFFFFD27A), 0.55f)
            // Pillows on the floor.
            fxBox(u, -0.17f, -0.045f, -0.06f, -0.01f, 0.1f, Color(0xFFF08CB8), pen, rad = 0.014f, z = 0.04f)
            fxBox(u, 0.04f, -0.05f, 0.16f, -0.01f, 0.1f, Color(0xFF8FD9C0), pen, rad = 0.014f, z = 0.05f)
            // The teddy.
            val tc = p(-0.02f, -0.045f)
            inkedCircle(Offset(tc.x, tc.y - 0.012f * u), 0.024f * u, Color(0xFFC98A55), pen)
            inkedCircle(Offset(tc.x, tc.y - 0.05f * u), 0.018f * u, Color(0xFFC98A55), pen)
            for (s in listOf(-1f, 1f)) inkedCircle(Offset(tc.x + s * 0.014f * u, tc.y - 0.066f * u), 0.007f * u, Color(0xFFC98A55), pen, shade = false)
            drawCircle(Ink.line, 0.002f * u, Offset(tc.x - 0.006f * u, tc.y - 0.052f * u)); drawCircle(Ink.line, 0.002f * u, Offset(tc.x + 0.006f * u, tc.y - 0.052f * u))
            // A torch lying next to it.
            rotate(-14f, p(0.1f, -0.06f)) { inkedRound(Rect(0.085f * u, -0.067f * u, 0.125f * u, -0.053f * u), 0.004f * u, UpC.red, pen, shade = false) }
        }
        // Fairy lights along the two slopes.
        for (k in 0 until 9) {
            val ft = (k + 0.5f) / 9f
            val a = p(-hw * (1f - ft) * 0.9f + 0.01f, -0.3f * ft * 0.92f - 0.012f)
            val lit = 0.5f + 0.5f * sin(t * 3f + k * 1.3f)
            val col = listOf(Color(0xFFFFE066), Color(0xFFFF8FB1), Color(0xFF8FD9C0), Color(0xFFB9A2F0))[k % 4]
            fxGlow(a, 0.03f * u, col, 0.7f * lit)
            drawCircle(col.lighten(0.2f), 0.005f * u, a)
        }
    }
    drawPath(front, if (open) Color.Transparent else Color(0xFFF2B27A))
    if (!open) {
        // The closed flap: blanket with a pattern of stars, tied with a ribbon.
        clipPath(front) {
            drawRect(Color(0xFFF2B27A), Offset(-hw * u, -0.3f * u), Size(2f * hw * u, 0.3f * u))
            for (k in 0 until 7) for (j in 0 until 3) {
                val cc = p(-0.15f + k * 0.05f + (j % 2) * 0.025f, -0.05f - j * 0.07f)
                drawPath(starPath(cc, 0.011f * u, 0.005f * u), Color(0xFFFFF3C4))
            }
            // The flap's slit and ribbon.
            drawLine(Ink.line, p(0f, -0.28f), p(0f, 0f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            drawPath(fxHeart(0f, -0.12f * u, 0.014f * u), UpC.red)
        }
    } else {
        // The rolled-up door flap at the left, tied with a ribbon.
        val roll = Path().apply { poly(-0.09f * u, -0.18f * u, -0.04f * u, -0.18f * u, -0.04f * u, -0.04f * u, -0.095f * u, -0.04f * u) }
        inked(roll, Color(0xFFF2B27A), pen)
        drawLine(UpC.red, p(-0.09f, -0.12f), p(-0.045f, -0.12f), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    }
    drawPath(front, Ink.line, style = pen.stroke)
    // Blanket peg ropes and bunting flags on the top.
    capsule(apex, Offset(0f, -0.38f * u), 0.006f * u, UpC.walnut, pen)
    val wave = 0.003f * u
    drawPath(Path().apply { poly(0f, -0.38f * u, 0.05f * u, -0.365f * u + wave, 0f, -0.345f * u) }, UpC.red)
    drawPath(Path().apply { poly(0f, -0.38f * u, 0.05f * u, -0.365f * u + wave, 0f, -0.345f * u) }, Ink.line, style = pen.thin)
    // Pegs at the corners.
    for (s in listOf(-1f, 1f)) {
        val pc = p(s * hw, -0.012f)
        inkedRound(Rect(pc.x - 0.006f * u, pc.y - 0.012f * u, pc.x + 0.006f * u, pc.y + 0.004f * u), 0.003f * u, UpC.red, pen, shade = false)
    }
}
