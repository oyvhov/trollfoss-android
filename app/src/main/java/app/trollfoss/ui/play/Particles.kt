package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.art.twinkle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** What a particle looks like and how it moves. */
enum class PKind { DUST, SPARK, HEART, CRUMB, STEAM, BUBBLE, CONFETTI, NOTE, ZZZ, LEAF, SNOW, DROP, FIREWORK, SMOKE, STAR, BUTTERFLY, BIRD }

/** A short-lived bit of life: dust, sparkles, hearts, crumbs, notes. Positions are in scene units. */
class Particle(
    val kind: PKind,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val max: Float,
    val size: Float,
    val color: Color,
    var rot: Float = 0f,
    val vr: Float = 0f,
) {
    var life = max
}

/** All particles of a scene. Scene units in, screen pixels out. */
class Particles(private val random: Random = Random.Default) {
    private val list = ArrayList<Particle>(256)

    val count: Int get() = list.size

    fun add(p: Particle) {
        if (list.size < 400) list += p
    }

    private val party = listOf(Color(0xFFFFC83D), Color(0xFFFF4D6D), Color(0xFF2F9BFF), Color(0xFF2FD18B), Color(0xFF8B5CF6), Color(0xFFFF9F43))

    fun burst(kind: PKind, x: Float, y: Float, n: Int, speed: Float = 0.5f, size: Float = 0.012f, color: Color? = null, up: Float = 0.2f, life: Float = 0.9f) {
        repeat(n) { i ->
            val a = random.nextFloat() * 2f * PI.toFloat()
            val s = speed * (0.4f + random.nextFloat() * 0.6f)
            add(
                Particle(
                    kind, x, y,
                    cos(a) * s, sin(a) * s - up,
                    life * (0.7f + random.nextFloat() * 0.6f),
                    size * (0.7f + random.nextFloat() * 0.6f),
                    color ?: party[(i + random.nextInt(party.size)) % party.size],
                    random.nextFloat() * 360f,
                    (random.nextFloat() - 0.5f) * 720f,
                ),
            )
        }
    }

    fun update(dt: Float) {
        var i = list.size - 1
        while (i >= 0) {
            val p = list[i]
            p.life -= dt
            if (p.life <= 0f) {
                list[i] = list[list.size - 1]
                list.removeAt(list.size - 1)
                i--
                continue
            }
            val g = when (p.kind) {
                PKind.CRUMB, PKind.DROP -> 3.2f
                PKind.CONFETTI -> 0.9f
                PKind.FIREWORK -> 0.8f
                PKind.DUST, PKind.SPARK, PKind.STAR -> 0.3f
                PKind.LEAF, PKind.SNOW -> 0.25f
                PKind.HEART, PKind.NOTE, PKind.ZZZ, PKind.STEAM, PKind.BUBBLE, PKind.SMOKE -> -0.25f
                PKind.BUTTERFLY, PKind.BIRD -> 0f
            }
            p.vy += g * dt
            val drag = when (p.kind) {
                PKind.CONFETTI, PKind.LEAF, PKind.SNOW -> 2.2f
                PKind.BUTTERFLY, PKind.BIRD -> 0f
                else -> 1.2f
            }
            if (p.kind == PKind.BUTTERFLY) p.vy = sin(p.life * 2.3f + p.size * 500f) * 0.12f
            p.vx *= 1f - min(1f, drag * dt)
            if (p.kind == PKind.LEAF || p.kind == PKind.SNOW || p.kind == PKind.CONFETTI) p.x += sin(p.life * 5f + p.size * 300f) * 0.05f * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.rot += p.vr * dt
            i--
        }
    }

    /** Draws every particle; [sx] and [sy] turn scene units into pixels, [u] is pixels per unit. */
    fun draw(scope: DrawScope, u: Float, cam: Float, lw: Float) = with(scope) {
        for (p in list) {
            val fade = min(1f, p.life / (p.max * 0.4f))
            val c = Offset((p.x - cam) * u, p.y * u)
            val r = p.size * u
            when (p.kind) {
                PKind.DUST -> {
                    val grow = 1f + (1f - p.life / p.max) * 1.4f
                    drawOval(Color.White.copy(alpha = 0.75f * fade), Offset(c.x - r * grow, c.y - r * 0.6f * grow), Size(r * 2f * grow, r * 1.2f * grow))
                }
                PKind.SPARK -> twinkle(c, r * 1.6f, p.color, fade)
                PKind.STAR -> {
                    rotate(p.rot, c) { drawPath(starPath(c, r * 1.4f, r * 0.6f), p.color.copy(alpha = fade)) }
                }
                PKind.HEART -> {
                    val path = Path().apply {
                        moveTo(c.x, c.y + r * 0.8f)
                        cubicTo(c.x - r * 1.5f, c.y - r * 0.2f, c.x - r * 0.6f, c.y - r * 1.4f, c.x, c.y - r * 0.4f)
                        cubicTo(c.x + r * 0.6f, c.y - r * 1.4f, c.x + r * 1.5f, c.y - r * 0.2f, c.x, c.y + r * 0.8f)
                        close()
                    }
                    drawPath(path, Color(0xFFFF4D6D).copy(alpha = fade))
                    drawPath(path, Ink.line.copy(alpha = fade), style = Stroke(lw * 0.8f))
                }
                PKind.CRUMB -> drawCircle(p.color.copy(alpha = fade), r * 0.7f, c)
                PKind.STEAM, PKind.SMOKE -> {
                    val grow = 1f + (1f - p.life / p.max) * 2f
                    drawCircle(p.color.copy(alpha = 0.35f * fade), r * grow, c)
                }
                PKind.BUBBLE -> {
                    drawCircle(Color.White.copy(alpha = 0.25f * fade), r, c)
                    drawCircle(Color.White.copy(alpha = 0.8f * fade), r, c, style = Stroke(lw * 0.6f))
                    drawCircle(Color.White.copy(alpha = 0.9f * fade), r * 0.25f, Offset(c.x - r * 0.35f, c.y - r * 0.35f))
                }
                PKind.CONFETTI -> rotate(p.rot, c) {
                    drawRect(p.color.copy(alpha = fade), Offset(c.x - r, c.y - r * 0.5f), Size(r * 2f, r))
                }
                PKind.NOTE -> {
                    drawOval(p.color.copy(alpha = fade), Offset(c.x - r, c.y - r * 0.6f), Size(r * 1.6f, r * 1.2f))
                    drawLine(p.color.copy(alpha = fade), Offset(c.x + r * 0.5f, c.y), Offset(c.x + r * 0.5f, c.y - r * 2.4f), strokeWidth = lw, cap = StrokeCap.Round)
                    drawLine(p.color.copy(alpha = fade), Offset(c.x + r * 0.5f, c.y - r * 2.4f), Offset(c.x + r * 1.4f, c.y - r * 1.8f), strokeWidth = lw, cap = StrokeCap.Round)
                }
                PKind.ZZZ -> {
                    val zz = Path().apply {
                        moveTo(c.x - r, c.y - r)
                        lineTo(c.x + r, c.y - r)
                        lineTo(c.x - r, c.y + r)
                        lineTo(c.x + r, c.y + r)
                    }
                    drawPath(zz, Color(0xFF5B32C9).copy(alpha = fade), style = Stroke(lw * 1.4f, cap = StrokeCap.Round))
                }
                PKind.LEAF -> rotate(p.rot, c) {
                    drawOval(p.color.copy(alpha = fade), Offset(c.x - r, c.y - r * 0.5f), Size(r * 2f, r))
                }
                PKind.SNOW -> drawCircle(Color.White.copy(alpha = fade), r * 0.6f, c)
                PKind.DROP -> drawCircle(p.color.copy(alpha = 0.8f * fade), r * 0.6f, c)
                PKind.FIREWORK -> {
                    drawCircle(p.color.copy(alpha = fade), r * 0.6f, c)
                    drawLine(p.color.copy(alpha = fade * 0.5f), c, Offset(c.x - p.vx * u * 0.05f, c.y - p.vy * u * 0.05f), strokeWidth = r * 0.6f, cap = StrokeCap.Round)
                }
                PKind.BUTTERFLY -> {
                    val flap = kotlin.math.abs(sin(p.life * 18f))
                    for (side in listOf(-1f, 1f)) {
                        drawOval(p.color.copy(alpha = fade), Offset(c.x + (if (side < 0) -r * 1.6f * flap else 0f), c.y - r * 0.9f), Size(r * 1.6f * flap, r * 1.4f))
                        drawOval(p.color.copy(alpha = fade * 0.8f), Offset(c.x + (if (side < 0) -r * 1.1f * flap else 0f), c.y), Size(r * 1.1f * flap, r * 0.9f))
                    }
                    drawLine(Ink.line.copy(alpha = fade), Offset(c.x, c.y - r * 0.8f), Offset(c.x, c.y + r * 0.8f), strokeWidth = lw * 0.9f, cap = StrokeCap.Round)
                }
                PKind.BIRD -> {
                    val flap = sin(p.life * 9f) * r * 0.7f
                    val path = Path().apply {
                        moveTo(c.x - r * 1.6f, c.y - flap)
                        quadraticTo(c.x - r * 0.7f, c.y - r * 0.6f, c.x, c.y)
                        quadraticTo(c.x + r * 0.7f, c.y - r * 0.6f, c.x + r * 1.6f, c.y - flap)
                    }
                    drawPath(path, Ink.line.copy(alpha = min(1f, fade * 0.8f)), style = Stroke(lw * 1.1f, cap = StrokeCap.Round))
                }
            }
        }
    }

    fun clear() = list.clear()

    @Suppress("unused")
    private fun clamp(v: Float) = max(0f, min(1f, v))
}
