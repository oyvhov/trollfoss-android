package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.PlaceId
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * The building kit of the village map. Every building is drawn in its own little space: the origin is
 * the centre of its base on the ground, one unit is the landmark size S (scaled a little with depth),
 * y points down. Faces are lit on the front and shaded on the right, with the map's oblique depth
 * running up and to the right; the cast shadow falls to the lower right.
 */

internal class Bx(val d: DrawScope, val m: MapPen, val sc: Float, val yf: Float) {
    /** One pixel of outline, in this space. */
    val lw = m.lw / sc
    val n = m.n
    val lit = ramp((m.n - 0.3f) / 0.4f)
    val t = m.t
    val snow = m.snow

    fun c(col: Color): Color = m.tone(col, yf)
    fun roofRaw(col: Color): Color = if (snow) lerp(col, Color(0xFFF4F8FF), 0.78f) else col

    fun path(vararg p: Float): Path {
        val pa = Path()
        for (i in 0 until p.size / 2) if (i == 0) pa.moveTo(p[0], p[1]) else pa.lineTo(p[i * 2], p[i * 2 + 1])
        pa.close()
        return pa
    }

    fun fill(p: Path, col: Color, a: Float = 1f) = d.drawPath(p, c(col), alpha = a)
    fun ink(p: Path, w: Float = 1.2f, a: Float = 1f) = d.drawPath(p, Ink.line, alpha = a, style = Stroke(lw * w, cap = StrokeCap.Round, join = StrokeJoin.Round))
    fun face(col: Color, vararg p: Float, w: Float = 1.2f): Path {
        val pa = path(*p)
        fill(pa, col)
        ink(pa, w)
        return pa
    }

    fun box(x0: Float, y0: Float, x1: Float, y1: Float, col: Color, w: Float = 1.2f) {
        d.drawRect(c(col), Offset(x0, y0), Size(x1 - x0, y1 - y0))
        d.drawRect(Ink.line, Offset(x0, y0), Size(x1 - x0, y1 - y0), style = Stroke(lw * w, join = StrokeJoin.Round))
    }

    fun round(x0: Float, y0: Float, x1: Float, y1: Float, r: Float, col: Color, w: Float = 1.2f) {
        d.drawRoundRect(c(col), Offset(x0, y0), Size(x1 - x0, y1 - y0), CornerRadius(r))
        d.drawRoundRect(Ink.line, Offset(x0, y0), Size(x1 - x0, y1 - y0), CornerRadius(r), style = Stroke(lw * w))
    }

    fun oval(cx: Float, cy: Float, rx: Float, ry: Float, col: Color, w: Float = 1.2f) {
        d.drawOval(c(col), Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))
        if (w > 0f) d.drawOval(Ink.line, Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f), style = Stroke(lw * w))
    }

    fun disc(cx: Float, cy: Float, r: Float, col: Color, w: Float = 1.2f) {
        d.drawCircle(c(col), r, Offset(cx, cy))
        if (w > 0f) d.drawCircle(Ink.line, r, Offset(cx, cy), style = Stroke(lw * w))
    }

    fun line(x0: Float, y0: Float, x1: Float, y1: Float, col: Color = Ink.line, w: Float = 1.2f, a: Float = 1f) =
        d.drawLine(if (col == Ink.line) col else c(col), Offset(x0, y0), Offset(x1, y1), strokeWidth = lw * w, cap = StrokeCap.Round, alpha = a)

    fun dots(pts: List<Offset>, col: Color, size: Float) = d.drawPoints(pts, PointMode.Points, c(col), strokeWidth = size, cap = StrokeCap.Round)

    /** A warm glow (layered discs) for windows and lamps at night. */
    fun glow(cx: Float, cy: Float, r: Float, col: Color = Color(0xFFFFD76B), a: Float = 0.25f) {
        if (lit <= 0f) return
        d.drawCircle(col, r, Offset(cx, cy), alpha = a * lit * 0.5f)
        d.drawCircle(col, r * 0.6f, Offset(cx, cy), alpha = a * lit)
    }

    /** An extruded polygon: [pts] (clockwise, y down) is the front face, pushed back up and to the right. */
    fun prism(pts: FloatArray, depth: Float, front: Color, side: Color, top: Color, w: Float = 1.2f) {
        val vx = 0.5f * depth
        val vy = -0.36f * depth
        val np = pts.size / 2
        for (i in 0 until np) {
            val ax = pts[2 * i]
            val ay = pts[2 * i + 1]
            val bx = pts[(2 * i + 2) % (2 * np)]
            val by = pts[(2 * i + 3) % (2 * np)]
            val nx = by - ay
            val ny = ax - bx
            if (nx * vx + ny * vy <= 0f) continue
            val up = ny < 0f && -ny > abs(nx) * 0.35f
            val q = path(ax, ay, bx, by, bx + vx, by + vy, ax + vx, ay + vy)
            fill(q, if (up) top else side)
            ink(q, w)
        }
        val f = path(*pts)
        fill(f, front)
        ink(f, w)
    }

    /** The cast shadow on the ground: the convex hull of the points, falling to the lower right. */
    fun shadowOf(vararg pts: Float, a: Float = 0.2f) {
        val n2 = pts.size / 2
        val idx = (0 until n2).sortedWith(compareBy({ pts[it * 2] }, { pts[it * 2 + 1] }))
        fun cross(o: Int, p: Int, q: Int) = (pts[p * 2] - pts[o * 2]) * (pts[q * 2 + 1] - pts[o * 2 + 1]) - (pts[p * 2 + 1] - pts[o * 2 + 1]) * (pts[q * 2] - pts[o * 2])
        val hull = ArrayList<Int>()
        for (i in idx) {
            while (hull.size >= 2 && cross(hull[hull.size - 2], hull[hull.size - 1], i) <= 0f) hull.removeAt(hull.size - 1)
            hull.add(i)
        }
        val lower = hull.size + 1
        for (i in idx.reversed().drop(1)) {
            while (hull.size >= lower && cross(hull[hull.size - 2], hull[hull.size - 1], i) <= 0f) hull.removeAt(hull.size - 1)
            hull.add(i)
        }
        if (hull.size < 4) return
        val p = Path()
        for ((k, i) in hull.withIndex()) if (k == 0) p.moveTo(pts[i * 2], pts[i * 2 + 1]) else p.lineTo(pts[i * 2], pts[i * 2 + 1])
        p.close()
        d.drawPath(p, Ink.line, alpha = a * (1f - 0.45f * n))
    }

    /** A soft dark oval right under a building, so it sits on the ground and not above it. */
    fun contact(cx: Float, rx: Float, ry: Float = rx * 0.16f) {
        d.drawOval(Ink.line, Offset(cx - rx, -ry * 0.4f), Size(rx * 2f, ry * 2f), alpha = 0.22f)
    }

    /** The shadow of a gabled or boxy building: footprint and roof line pushed along the light. */
    fun shadowHouse(x0: Float, x1: Float, depth: Float, wallH: Float, roofH: Float, gableFront: Boolean) {
        val px = 0.62f
        val py = 0.15f
        val vx = 0.5f * depth
        val vy = -0.36f * depth
        val hp = wallH + roofH
        val pts = if (gableFront) floatArrayOf(
            x0, 0f, x1, 0f, x0 + vx, vy, x1 + vx, vy,
            (x0 + x1) / 2f + px * hp, py * hp, (x0 + x1) / 2f + vx + px * hp, vy + py * hp,
            x1 + px * wallH, py * wallH, x1 + vx + px * wallH, vy + py * wallH,
        ) else floatArrayOf(
            x0, 0f, x1, 0f, x0 + vx, vy, x1 + vx, vy,
            x0 + vx / 2f + px * hp, vy / 2f + py * hp, x1 + vx / 2f + px * hp, vy / 2f + py * hp,
            x1 + px * wallH, py * wallH, x1 + vx + px * wallH, vy + py * wallH,
        )
        shadowOf(*pts)
    }

    /** A front-gabled house: lit gable face, shaded long side, roof plane, barge boards and a dark base. */
    fun gable(hw: Float, wallH: Float, roofH: Float, depth: Float, wall: Color, roofC: Color, trim: Color = Color(0xFFF7F3EC)) {
        val pts = floatArrayOf(-hw, -wallH, 0f, -wallH - roofH, hw, -wallH, hw, 0f, -hw, 0f)
        prism(pts, depth, wall, wall.darken(0.24f), roofRaw(roofC).lighten(0.05f))
        // The roof plane lit on its ridge side, white snow caps on top.
        val vx = 0.5f * depth
        val vy = -0.36f * depth
        line(0f, -wallH - roofH, vx, -wallH - roofH + vy, roofC.lighten(0.3f), 1.6f, 0.9f)
        // Barge boards along the gable edge and corner boards.
        line(-hw - 0.03f, -wallH + 0.03f, 0f, -wallH - roofH - 0.02f, trim, 2.2f)
        line(0f, -wallH - roofH - 0.02f, hw + 0.03f, -wallH + 0.03f, trim, 2.2f)
        line(-hw - 0.03f, -wallH + 0.03f, 0f, -wallH - roofH - 0.02f, Ink.line, 0.8f, 0.7f)
        line(-hw, 0f, -hw, -wallH, trim, 2.4f)
        line(hw, 0f, hw, -wallH, trim, 2.4f)
        // A dark foundation course where the walls meet the ground.
        d.drawRect(c(Color(0xFF6E6A78)), Offset(-hw - 0.012f, -0.035f), Size(2f * hw + 0.024f, 0.035f))
        d.drawRect(Ink.line, Offset(-hw - 0.012f, -0.035f), Size(2f * hw + 0.024f, 0.035f), style = Stroke(lw))
    }

    /** A house with the ridge left to right: front wall, sloping roof plane above it and a shaded gable end. */
    fun longHouse(hw: Float, wallH: Float, roofH: Float, depth: Float, wall: Color, roofC: Color, trim: Color = Color(0xFFF7F3EC)) {
        val vx = 0.5f * depth
        val vy = -0.36f * depth
        // The shaded end: wall and gable.
        face(wall.darken(0.24f), hw, -wallH, hw + vx, -wallH + vy, hw + vx, vy, hw, 0f)
        face(wall.darken(0.22f), hw, -wallH, hw + vx, -wallH + vy, hw + vx / 2f, -wallH - roofH + vy / 2f)
        // The lit front wall.
        box(-hw, -wallH, hw, 0f, wall)
        d.drawRect(c(Color(0xFF6E6A78)), Offset(-hw, -0.035f), Size(2f * hw, 0.035f))
        d.drawRect(Ink.line, Offset(-hw, -0.035f), Size(2f * hw, 0.035f), style = Stroke(lw))
        // The roof plane climbing to the ridge.
        val rp = face(roofC.lighten(0.05f), -hw - 0.05f, -wallH + 0.04f, hw + 0.05f, -wallH + 0.04f, hw + vx / 2f + 0.03f, -wallH - roofH + vy / 2f, -hw + vx / 2f - 0.03f, -wallH - roofH + vy / 2f)
        if (!snow) for (k in 1..3) {
            val f = k / 4f
            line(mix(-hw - 0.05f, -hw + vx / 2f - 0.03f, f), mix(-wallH + 0.04f, -wallH - roofH + vy / 2f, f), mix(hw + 0.05f, hw + vx / 2f + 0.03f, f), mix(-wallH + 0.04f, -wallH - roofH + vy / 2f, f), roofC.darken(0.3f), 0.7f, 0.55f)
        }
        line(-hw + vx / 2f - 0.03f, -wallH - roofH + vy / 2f, hw + vx / 2f + 0.03f, -wallH - roofH + vy / 2f, roofC.lighten(0.35f), 1.6f)
        line(-hw - 0.05f, -wallH + 0.04f, hw + 0.05f, -wallH + 0.04f, trim, 2.2f)
        line(-hw, 0f, -hw, -wallH, trim, 2.4f)
        if (snow) fill(rp, Color(0xFFF4F8FF), 0.82f)
    }

    /**
     * A window with frame, cross bars and a glass that warms up at night; optionally shutters and a flower box.
     * With [warm] false the glass stays cold at night: the live layer lights it (see MapManor.kt).
     */
    fun win(x0: Float, y0: Float, x1: Float, y1: Float, shutters: Boolean = false, box: Boolean = false, frame: Color = Color(0xFFFFFFFF), round: Boolean = false, warm: Boolean = true) {
        val gx = (x0 + x1) / 2f
        val gy = (y0 + y1) / 2f
        if (warm) glow(gx, gy, (x1 - x0) * 1.4f)
        val glass = lerp(c(Color(0xFF9CCDE6)), Color(0xFFFFD76B), if (warm) lit else 0f)
        if (round) {
            d.drawCircle(glass, (x1 - x0) / 2f, Offset(gx, gy))
            d.drawCircle(c(frame), (x1 - x0) / 2f, Offset(gx, gy), style = Stroke(lw * 2.2f))
            d.drawCircle(Ink.line, (x1 - x0) / 2f + lw, Offset(gx, gy), style = Stroke(lw * 0.8f))
            d.drawLine(c(frame), Offset(gx, y0), Offset(gx, y1), strokeWidth = lw * 1.2f)
            d.drawLine(c(frame), Offset(x0, gy), Offset(x1, gy), strokeWidth = lw * 1.2f)
            return
        }
        d.drawRect(glass, Offset(x0, y0), Size(x1 - x0, y1 - y0))
        d.drawLine(c(frame), Offset(gx, y0), Offset(gx, y1), strokeWidth = lw * 1.3f)
        d.drawLine(c(frame), Offset(x0, gy), Offset(x1, gy), strokeWidth = lw * 1.3f)
        d.drawRect(c(frame), Offset(x0, y0), Size(x1 - x0, y1 - y0), style = Stroke(lw * 2f))
        d.drawRect(Ink.line, Offset(x0 - lw, y0 - lw), Size(x1 - x0 + lw * 2f, y1 - y0 + lw * 2f), style = Stroke(lw * 0.8f))
        if (shutters) {
            val sw = (x1 - x0) * 0.38f
            d.drawRect(c(Color(0xFF3E6FA8)), Offset(x0 - sw - lw, y0), Size(sw, y1 - y0))
            d.drawRect(c(Color(0xFF3E6FA8)), Offset(x1 + lw, y0), Size(sw, y1 - y0))
            d.drawRect(Ink.line, Offset(x0 - sw - lw, y0), Size(sw, y1 - y0), style = Stroke(lw * 0.8f))
            d.drawRect(Ink.line, Offset(x1 + lw, y0), Size(sw, y1 - y0), style = Stroke(lw * 0.8f))
        }
        if (box) {
            val by = y1 + lw
            d.drawRect(c(Color(0xFFA0663B)), Offset(x0 - 0.01f, by), Size(x1 - x0 + 0.02f, (y1 - y0) * 0.22f))
            d.drawRect(Ink.line, Offset(x0 - 0.01f, by), Size(x1 - x0 + 0.02f, (y1 - y0) * 0.22f), style = Stroke(lw * 0.8f))
            val fl = ArrayList<Offset>()
            val cols = listOf(Color(0xFFFF6B8A), Color(0xFFFFC83D), Color(0xFFFFFFFF))
            for (k in 0 until 4) fl.add(Offset(x0 + (x1 - x0) * (k + 0.5f) / 4f, by - 0.008f))
            for ((i, p) in fl.withIndex()) disc(p.x, p.y, 0.014f, cols[i % 3], 0.6f)
        }
    }

    /** A door with a knob and, optionally, steps leading up to it. */
    fun door(x0: Float, x1: Float, h: Float, col: Color, steps: Boolean = true, arch: Boolean = false) {
        val p = Path()
        if (arch) {
            p.moveTo(x0, 0f)
            p.lineTo(x0, -h + (x1 - x0) / 2f)
            p.quadraticTo(x0, -h, (x0 + x1) / 2f, -h)
            p.quadraticTo(x1, -h, x1, -h + (x1 - x0) / 2f)
            p.lineTo(x1, 0f)
            p.close()
        } else {
            p.addRect(Rect(x0, -h, x1, 0f))
        }
        fill(p, col)
        ink(p, 1.1f)
        disc(x1 - (x1 - x0) * 0.2f, -h * 0.45f, 0.012f, Color(0xFFFFC83D), 0.6f)
        if (steps) {
            d.drawRect(c(Color(0xFFB9B2C0)), Offset(x0 - 0.03f, -0.018f), Size(x1 - x0 + 0.06f, 0.018f))
            d.drawRect(c(Color(0xFF9C95A8)), Offset(x0 - 0.05f, 0f), Size(x1 - x0 + 0.1f, 0.022f))
            d.drawRect(Ink.line, Offset(x0 - 0.05f, 0f), Size(x1 - x0 + 0.1f, 0.022f), style = Stroke(lw * 0.8f))
        }
    }

    /** A brick chimney; its smoke is drawn live by [chimneySmoke]. */
    fun chimney(x: Float, topY: Float, w: Float = 0.085f, h: Float = 0.2f) {
        box(x, topY - h, x + w, topY, Color(0xFF9C5A48))
        d.drawRect(c(Color(0xFF7A4034)), Offset(x + w * 0.6f, topY - h), Size(w * 0.4f, h))
        d.drawRect(c(Color(0xFF6E6A78)), Offset(x - w * 0.12f, topY - h - 0.02f), Size(w * 1.24f, 0.028f))
    }

    /** The smoke of the chimney drawn by [chimney] with the same arguments (moves with time). */
    fun chimneySmoke(x: Float, topY: Float, w: Float = 0.085f, h: Float = 0.2f) = smokeAt(x + w / 2f, topY - h - 0.03f)

    fun smokeAt(x: Float, y: Float, scale: Float = 1f, dark: Boolean = false) {
        for (k in 0 until 4) {
            val ph = wrap(t * 0.25f + k * 0.25f + x * 3.1f, 1f)
            val px = x + ph * 0.3f * scale + sin(ph * 6f + k) * 0.03f
            val py = y - ph * 0.55f * scale
            val col = if (dark) Color(0xFF6E6A78) else Color.White
            d.drawCircle(col, (0.035f + 0.085f * ph) * scale, Offset(px, py), alpha = 0.7f * (1f - ph) * (1f - 0.3f * n))
        }
    }

    /** Picket fence from [x0] to [x1] at ground height [y], with a gap for a gate between [gx0] and [gx1]. */
    fun fence(x0: Float, x1: Float, y: Float, gx0: Float = 0f, gx1: Float = 0f, col: Color = Color(0xFFF7F3EC), h: Float = 0.11f) {
        val posts = Path()
        var x = x0
        while (x <= x1 + 0.001f) {
            if (!(gx1 > gx0 && x > gx0 && x < gx1)) {
                posts.moveTo(x, y)
                posts.lineTo(x, y - h)
                posts.lineTo(x + 0.012f, y - h - 0.018f)
                posts.lineTo(x + 0.024f, y - h)
                posts.lineTo(x + 0.024f, y)
                posts.close()
            }
            x += 0.062f
        }
        d.drawPath(posts, c(col))
        d.drawPath(posts, Ink.line, alpha = 0.85f, style = Stroke(lw * 0.8f, join = StrokeJoin.Round))
        for (yy in floatArrayOf(y - h * 0.35f, y - h * 0.72f)) {
            if (gx1 > gx0) {
                line(x0, yy, gx0, yy, col.darken(0.1f), 1.3f)
                line(gx1, yy, x1 + 0.02f, yy, col.darken(0.1f), 1.3f)
            } else line(x0, yy, x1 + 0.02f, yy, col.darken(0.1f), 1.3f)
        }
    }

    /** A cluster of grass blades hugging the ground. */
    fun tuft(x: Float, y: Float, s: Float = 0.05f, col: Color = Color(0xFF4F9A48)) {
        val p = Path().apply {
            moveTo(x - s * 0.45f, y)
            quadraticTo(x - s * 0.45f, y - s * 0.5f, x - s * 0.62f, y - s * 0.82f)
            quadraticTo(x - s * 0.25f, y - s * 0.5f, x - s * 0.14f, y - s * 0.28f)
            quadraticTo(x - s * 0.12f, y - s * 0.7f, x, y - s)
            quadraticTo(x + s * 0.12f, y - s * 0.62f, x + s * 0.14f, y - s * 0.28f)
            quadraticTo(x + s * 0.3f, y - s * 0.55f, x + s * 0.6f, y - s * 0.78f)
            quadraticTo(x + s * 0.45f, y - s * 0.45f, x + s * 0.45f, y)
            close()
        }
        d.drawPath(p, c(if (snow) Color(0xFFDDE8F5) else m.pen.blade(col)))
        d.drawPath(p, Ink.line, alpha = 0.7f, style = Stroke(lw * 0.7f, join = StrokeJoin.Round))
    }

    fun tufts(vararg xs: Float, y: Float = 0.05f, s: Float = 0.055f) {
        for ((i, x) in xs.withIndex()) tuft(x, y + 0.012f * (i % 2), s * (0.85f + 0.3f * ((i * 7) % 3) / 2f))
    }

    /** A round leafy tree. [fruit] dots it with red apples. */
    fun roundTree(x: Float, y: Float, s: Float, col: Color = Color(0xFF5DB04F), fruit: Boolean = false) {
        d.drawOval(Ink.line, Offset(x - s * 0.35f, y - 0.012f), Size(s * 0.9f, s * 0.12f), alpha = 0.18f)
        line(x, y, x, y - s * 0.5f, Color(0xFF7A5134), 3.2f)
        val r = s * 0.46f
        val cy = y - s * 0.78f
        val shade = crownPath(x, cy, r, r * 0.92f, 3)
        val litP = crownPath(x - r * 0.14f, cy - r * 0.16f, r * 0.88f, r * 0.8f, 4)
        // Each tree has its own autumn colour, picked from where it stands.
        val leaf = if (m.frost) Color(0xFFD3DEE8) else m.leaf(col, ((x * 7f + y * 13f + s * 5f) * 10f).toInt())
        fill(shade, leaf.darken(if (m.frost) 0.14f else 0.28f))
        fill(litP, if (m.frost) leaf.lighten(0.5f) else if (snow) col.lighten(0.5f) else leaf)
        d.drawOval(Color.White, Offset(x - r * 0.65f, cy - r * 0.72f), Size(r * 0.5f, r * 0.34f), alpha = 0.22f)
        ink(shade, 1.1f, 0.85f)
        if (fruit && !snow) for (k in 0 until 6) disc(x + r * (-0.6f + 0.25f * k) * 0.9f, cy + r * (0.1f * ((k * 5) % 4) - 0.2f), 0.017f, m.fruit, 0.7f)
    }

    fun pineTree(x: Float, y: Float, s: Float, col: Color = Color(0xFF2E8552)) {
        d.drawOval(Ink.line, Offset(x - s * 0.3f, y - 0.01f), Size(s * 0.8f, 0.1f), alpha = 0.18f)
        val p = Path().apply { addPine(x, y - s * 0.06f, s * 0.52f, s * 0.94f) }
        val ps = Path().apply { addPineShade(x, y - s * 0.06f, s * 0.52f, s * 0.94f) }
        d.drawRect(c(Color(0xFF6E4A33)), Offset(x - s * 0.035f, y - s * 0.12f), Size(s * 0.07f, s * 0.12f))
        val pine = if (snow) Color(0xFF3F7F63) else m.pen.conifer(col)
        fill(p, pine)
        fill(ps, pine.darken(0.26f))
        ink(p, 1.1f, 0.9f)
        if (snow) {
            val sp = path(x, y - s, x + s * 0.12f, y - s * 0.78f, x - s * 0.12f, y - s * 0.78f)
            fill(sp, Color.White)
        }
    }

    /** A wooden crate of goods: [col]'s round things peeking out. */
    fun crate(x: Float, y: Float, w: Float, h: Float, goods: Color) {
        val dots = ArrayList<Offset>()
        for (k in 0 until 4) dots.add(Offset(x + w * (0.15f + 0.23f * k), y - h - 0.012f - 0.012f * (k % 2)))
        for (p in dots) d.drawCircle(c(goods), w * 0.12f, p)
        for (p in dots) d.drawCircle(Ink.line, w * 0.12f, p, style = Stroke(lw * 0.7f))
        box(x, y - h, x + w, y, Color(0xFFC98A55), 1.1f)
        line(x, y - h * 0.5f, x + w, y - h * 0.5f, Color(0xFF8C5A32), 0.8f)
    }

    fun barrel(x: Float, y: Float, r: Float = 0.06f) {
        val p = Path().apply {
            moveTo(x - r * 0.85f, y)
            quadraticTo(x - r * 1.15f, y - r * 1.05f, x - r * 0.85f, y - r * 2.1f)
            lineTo(x + r * 0.85f, y - r * 2.1f)
            quadraticTo(x + r * 1.15f, y - r * 1.05f, x + r * 0.85f, y)
            close()
        }
        fill(p, Color(0xFFA0663B))
        ink(p, 1f)
        line(x - r, y - r * 0.55f, x + r, y - r * 0.55f, Color(0xFF55505E), 1.3f)
        line(x - r, y - r * 1.55f, x + r, y - r * 1.55f, Color(0xFF55505E), 1.3f)
        oval(x, y - r * 2.1f, r * 0.85f, r * 0.26f, Color(0xFFC98A55), 0.9f)
    }

    /** A bench seen from the front. */
    fun bench(x: Float, y: Float, w: Float = 0.26f, col: Color = Color(0xFFC98A55)) {
        line(x + 0.03f, y, x + 0.03f, y - 0.07f, Ink.line, 2.4f)
        line(x + w - 0.03f, y, x + w - 0.03f, y - 0.07f, Ink.line, 2.4f)
        box(x, y - 0.1f, x + w, y - 0.065f, col, 1f)
        box(x, y - 0.17f, x + w, y - 0.135f, col.darken(0.1f), 1f)
    }
}

// ------------------------------------------------------------------------------------- the ground

private class Plot(val rx: Float, val ry: Float, val kind: Int, val dy: Float = 0f)

private fun plotOf(p: PlaceId): Plot? = when (p.name) {
    "HOME" -> Plot(1.3f, 0.34f, 0)
    "CAFE" -> Plot(1.25f, 0.3f, 1)
    "SALON" -> Plot(1.05f, 0.3f, 0)
    "SHOP" -> Plot(1.3f, 0.32f, 1)
    "DOCTOR" -> Plot(1.15f, 0.3f, 0)
    "TIVOLI" -> Plot(2.1f, 0.42f, 1)
    "STAGE" -> Plot(1.25f, 0.3f, 1)
    "FOREST" -> Plot(1.25f, 0.32f, 3)
    "LAB" -> Plot(1.05f, 0.26f, 2)
    "MOUNTAIN" -> Plot(1.3f, 0.3f, 4)
    "HEILEBERGET" -> Plot(0.8f, 0.2f, 4)
    "FARM" -> Plot(1.95f, 0.4f, 0)
    "BEACH" -> Plot(1.6f, 0.34f, 5)
    "MANOR_GROUND" -> Plot(1.6f, 0.8f, 0)
    else -> null
}

/** The cached shapes of one place's ground. */
internal class PlotGeo(
    val top: Path, val wall: Path, val joints: List<Offset>, val shade: Path, val paving: Path?, val dots: List<Offset>,
    val kind: Int,
) {
    val ledge: Boolean get() = kind == 2 || kind == 4
}

internal fun buildPlot(g: MapGeo, place: PlaceId): PlotGeo? {
    val pl = plotOf(place) ?: return null
    val b = g.bases[place]!!
    val cx = b.x
    val cy = b.y + 0.004f * g.h
    val rx = pl.rx * g.S
    val ry = pl.ry * g.S * 0.5f
    val n = 22
    val ledgeK = if (pl.kind == 2 || pl.kind == 4) 2.6f else 1f
    fun wob(a: Float) = 1f + ledgeK * (0.05f * sin(a * 3f + place.ordinal) + 0.03f * sin(a * 5f + place.ordinal * 2f) + 0.025f * sin(a * 9f + place.ordinal))
    val top = Path()
    for (i in 0 until n) {
        val a = i * 6.2831855f / n
        val o = Offset(cx + rx * wob(a) * cos(a), cy + ry * wob(a) * sin(a))
        if (i == 0) top.moveTo(o.x, o.y) else top.lineTo(o.x, o.y)
    }
    top.close()
    // The retaining wall along the lower half, with joints.
    val wallH = (if (pl.kind == 2 || pl.kind == 4) 0f else 0.02f) * g.h
    val wall = Path()
    val joints = ArrayList<Offset>()
    for (i in 0..12) {
        val a = 0.05f + 3.04f * i / 12f
        val o = Offset(cx + rx * wob(a) * cos(a), cy + ry * wob(a) * sin(a))
        if (i == 0) wall.moveTo(o.x, o.y) else wall.lineTo(o.x, o.y)
    }
    for (i in 12 downTo 0) {
        val a = 0.05f + 3.04f * i / 12f
        val o = Offset(cx + rx * wob(a) * cos(a), cy + ry * wob(a) * sin(a) + wallH)
        wall.lineTo(o.x, o.y)
        if (i in 1..11 && i % 2 == 0) {
            joints.add(Offset(o.x, o.y))
            joints.add(Offset(o.x, o.y - wallH))
        }
    }
    wall.close()
    // Shading toward the lower right.
    val shade = Path()
    for (i in 0..10) {
        val a = -0.5f + 2.7f * i / 10f
        val o = Offset(cx + rx * wob(a) * cos(a), cy + ry * wob(a) * sin(a))
        if (i == 0) shade.moveTo(o.x, o.y) else shade.lineTo(o.x, o.y)
    }
    for (i in 10 downTo 0) {
        val a = -0.5f + 2.7f * i / 10f
        val o = Offset(cx + rx * 0.86f * wob(a) * cos(a) - rx * 0.05f, cy + ry * 0.8f * wob(a) * sin(a) - ry * 0.12f)
        shade.lineTo(o.x, o.y)
    }
    shade.close()
    var paving: Path? = null
    val dots = ArrayList<Offset>()
    if (pl.kind == 1) {
        val pv = Path()
        for (r in 1..2) {
            val f = r / 3f
            val half = rx * 0.97f * sqrt1(1f - f * f)
            pv.moveTo(cx - half, cy - ry * f)
            pv.lineTo(cx + half, cy - ry * f)
            pv.moveTo(cx - half, cy + ry * f)
            pv.lineTo(cx + half, cy + ry * f)
        }
        paving = pv
    } else if (pl.kind == 0 || pl.kind == 3) {
        for (k in 0 until 14) {
            val a = hash01(k, 71 + place.ordinal) * 6.28f
            val rr = 0.3f + 0.6f * hash01(k, 72 + place.ordinal)
            dots.add(Offset(cx + rx * rr * cos(a), cy + ry * rr * sin(a)))
        }
    }
    return PlotGeo(top, wall, joints, shade, paving, dots, pl.kind)
}

/** The piece of ground each place stands on: a dug-in terrace with a retaining wall, paving or lawn. */
internal fun MapPen.drawPlot(d: DrawScope, g: MapGeo, place: PlaceId) = with(d) {
    val pg = g.plots[place] ?: return@with
    val kind = pg.kind
    val stone = kind == 1 || kind == 2
    if (!pg.ledge) {
        val wallC = nt(if (snow) Color(0xFFB8C4DA) else if (stone) Color(0xFF9A8F80) else Color(0xFF8C6A45), 0.5f)
        drawPath(pg.wall, wallC)
        drawPoints(pg.joints, PointMode.Lines, wallC.darken(0.3f), strokeWidth = lw * 0.9f)
        drawPath(pg.wall, Ink.line, alpha = 0.6f, style = Stroke(lw * 0.9f, join = StrokeJoin.Round))
    }
    val fillC = when (kind) {
        1 -> if (snow) Color(0xFFF1F4FB) else Color(0xFFDCD2BE)
        2 -> if (snow) Color(0xFFE6EBF5) else Color(0xFF9E9CAA)
        3 -> grass(Color(0xFF6DA155), Color(0xFFE8EFF7))
        4 -> Color(0xFFF7FAFF)
        5 -> Color(0xFFF3DFA8)
        else -> grass(Color(0xFFA3D46C), Color(0xFFF1F6FC))
    }
    drawPath(pg.top, nt(fillC, 0.5f))
    drawPath(pg.shade, nt(fillC, 0.5f).darken(0.1f), alpha = 0.6f)
    pg.paving?.let { drawPath(it, nt(Color(0xFFB3A791), 0.5f), alpha = 0.7f, style = Stroke(lw * 0.8f)) }
    if (pg.dots.isNotEmpty()) drawPoints(pg.dots, PointMode.Points, nt(if (kind == 3) Color(0xFF3F7F47) else Color(0xFFFFF3B0), 0.4f), strokeWidth = h * 0.007f, cap = StrokeCap.Round, alpha = 0.8f)
    if (!pg.ledge) drawPath(pg.top, Ink.line, alpha = 0.5f, style = Stroke(lw * 0.9f, join = StrokeJoin.Round))
}

private fun sqrt1(v: Float): Float = kotlin.math.sqrt(max(0f, v))

// ------------------------------------------------------------------------------------- cottages

/** A small house standing along a road, with a shadow, a glowing window at night and a bit of garden. */
internal fun MapPen.drawCottage(d: DrawScope, c: Cottage) {
    val yf = c.y / h
    val sc = S * c.sc
    d.withTransform({ translate(c.x, c.y); scale(sc, sc, Offset.Zero) }) {
        val b = Bx(this, this@drawCottage, sc, yf)
        with(b) {
            val hw = 0.36f
            val wallH = 0.3f
            val roofH = 0.25f
            val depth = 0.4f
            contact(0f, 0.5f)
            when (c.kind) {
                0 -> {
                    shadowHouse(-hw, hw, depth, wallH, roofH, true)
                    gable(hw, wallH, roofH, depth, c.wall, c.roof)
                    win(-0.25f, -0.26f, -0.1f, -0.13f, shutters = false, box = true)
                    door(0.08f, 0.22f, 0.2f, Color(0xFF8C5A32), steps = false)
                    chimney(0.14f, -wallH - roofH * 0.65f, 0.07f, 0.16f)
                }
                1 -> {
                    shadowHouse(-0.45f, 0.45f, depth, wallH, roofH, false)
                    longHouse(0.45f, wallH, roofH, depth, c.wall, c.roof)
                    win(-0.34f, -0.25f, -0.2f, -0.13f, box = true)
                    win(0.14f, -0.25f, 0.28f, -0.13f, box = true)
                    door(-0.08f, 0.05f, 0.2f, Color(0xFFC84A3C), steps = false)
                }
                else -> {
                    shadowHouse(-hw, hw + 0.2f, depth, wallH, roofH, true)
                    gable(hw, wallH, roofH, depth, c.wall, c.roof)
                    box(hw, -0.2f, hw + 0.2f, 0f, c.wall.darken(0.05f))
                    face(c.roof.lighten(0.05f), hw - 0.02f, -0.22f, hw + 0.24f, -0.2f, hw + 0.2f, -0.28f, hw + 0.05f, -0.3f)
                    win(-0.22f, -0.24f, -0.06f, -0.1f, shutters = true)
                    door(0.06f, 0.2f, 0.2f, Color(0xFF3E6FA8), steps = false)
                }
            }
            tufts(-0.4f, 0.4f, y = 0.04f, s = 0.05f)
            if (c.kind == 1) roundTree(0.6f, 0.03f, 0.3f, fruit = false)
        }
    }
}

/** The moving part of a cottage: smoke from the chimney of the gabled ones. */
internal fun MapPen.drawCottageLive(d: DrawScope, c: Cottage) {
    if (c.kind != 0) return
    val yf = c.y / h
    val sc = S * c.sc
    d.withTransform({ translate(c.x, c.y); scale(sc, sc, Offset.Zero) }) {
        Bx(this, this@drawCottageLive, sc, yf).chimneySmoke(0.14f, -0.3f - 0.25f * 0.65f, 0.07f, 0.16f)
    }
}
