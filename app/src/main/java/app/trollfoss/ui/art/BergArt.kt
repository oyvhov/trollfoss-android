package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import app.trollfoss.domain.Weather
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Heileberget, «the great long mountain»: the 15th place, nine scene units wide. Behind everything stands
 * one colossal massif, drawn in cel-shaded facets (a warm lit side and a cool shaded side to every peak)
 * with deep snowfields, a hanging glacier, scree, two tall waterfalls and a belt of pines at its foot, in
 * front of a second massif and three far blue ranges. Light is late-afternoon gold.
 *
 * All the big shapes are built once per scale (see [bgGeo]) in scene pixels and drawn with a translate, so
 * a frame only strokes and fills ready-made paths; the moving parts (clouds, eagles, falls, mist, water
 * glints) are drawn from a few lines each. Every helper starts with `bg` so it cannot clash with the
 * other places.
 */

// ---------------------------------------------------------------------------------------------- palette

private object BgC {
    val skyTop = Color(0xFF2F80DC)
    val skyMid = Color(0xFF7FBFF0)
    val skyLow = Color(0xFFFFE4BC)
    val skyGold = Color(0xFFFFC66E)
    val sun = Color(0xFFFFC23D)

    // The five faces of a peak, bright on the left to deep shade on the right: top colour and foot colour.
    val faceTop = intArrayOf(0xFFF0CFA4.toInt(), 0xFFDDB58A.toInt(), 0xFFC89F80.toInt(), 0xFF7B80AE.toInt(), 0xFF575D8D.toInt())
    val faceLow = intArrayOf(0xFFCCAA8E.toInt(), 0xFFB99882.toInt(), 0xFFA38A7E.toInt(), 0xFF5D6391.toInt(), 0xFF434A7A.toInt())
    val snowLit = Color(0xFFFFF5E1)
    val snowShade = Color(0xFFB2C0EC)
    val snowRidgeLit = Color(0xFFF1DDC4)
    val snowRidgeDeep = Color(0xFF93A3D8)
    val couLit = Color(0xFFFFF1DA)
    val couShade = Color(0xFFA9B8E4)
    val strataL = Color(0xFF8F6B58)
    val strataS = Color(0xFF353B68)
    val ledge = Color(0xFFD6DDF6)
    val screeLit = Color(0xFFD6C4AC)
    val screeShade = Color(0xFF7A7DA8)
    val iceLit = Color(0xFFBCEBF8)
    val iceShade = Color(0xFF6FB6E0)
    val iceDeep = Color(0xFF3F86BC)

    val hill = Color(0xFF4C8F7D)
    val hillPine = Color(0xFF3E8271)
    val hillPineShade = Color(0xFF33705F)
    val pine = Color(0xFF1F5E48)
    val pineLit = Color(0xFF2F8059)
    val pineShade = Color(0xFF164A3A)
    val meadowLit = Color(0xFF8FCB5E)
    val meadowLow = Color(0xFF5FA449)
    val grass = Color(0xFF6BB24F)
    val path = Color(0xFFDCC7A2)
    val pathDark = Color(0xFFB9A381)
    val stone = Color(0xFFB7B2BD)
    val stoneShade = Color(0xFF7D7894)
    val water = Color(0xFF8ED8F4)
    val mist = Color(0xFFF4F9FF)
    val gold = Color(0xFFFFD27A)
    val steel = Color(0xFF5A6078)
}

// ---------------------------------------------------------------------------------------------- geometry

/** A growing polyline in scene units. */
private class BgPts(cap: Int = 24) {
    var x = FloatArray(cap)
    var y = FloatArray(cap)
    var n = 0

    fun add(px: Float, py: Float): BgPts {
        if (n == x.size) {
            x = x.copyOf(n * 2)
            y = y.copyOf(n * 2)
        }
        x[n] = px
        y[n] = py
        n++
        return this
    }

    /** The x of a polyline that only ever goes down, at height [yy]. */
    fun xAt(yy: Float): Float {
        if (yy <= y[0]) return x[0]
        for (i in 1 until n) {
            if (yy <= y[i]) {
                val f = (yy - y[i - 1]) / max(0.0001f, y[i] - y[i - 1])
                return x[i - 1] + (x[i] - x[i - 1]) * f
            }
        }
        return x[n - 1]
    }
}

/** Adds points [from]..[to] (in either direction) as lines; [move] starts a new subpath. */
private fun Path.bgRun(p: BgPts, from: Int, to: Int, u: Float, move: Boolean = false) {
    val step = if (to >= from) 1 else -1
    var i = from
    var first = move
    while (true) {
        if (first) {
            moveTo(p.x[i] * u, p.y[i] * u)
            first = false
        } else {
            lineTo(p.x[i] * u, p.y[i] * u)
        }
        if (i == to) break
        i += step
    }
}

private fun Path.bgPoly(u: Float, vararg xy: Float) {
    moveTo(xy[0] * u, xy[1] * u)
    var i = 2
    while (i < xy.size) {
        lineTo(xy[i] * u, xy[i + 1] * u)
        i += 2
    }
    close()
}

/** A range of peaks: saddle positions between them, apexes, how deep the snow lies and which way each spur leans. */
private class BgSpec(
    val sad: FloatArray, val sadY: FloatArray, val apx: FloatArray, val apy: FloatArray,
    val snow: FloatArray, val lean: FloatArray, val baseY: Float, val jag: Float, val salt: Int,
)

/** A peak's ready-made paths in scene pixels. */
private class BgPeak(val x0: Float, val x1: Float) {
    /** Far layers use one lit tile; the great massif uses five graded faces, brightest on the left. */
    val rockLit = Path()
    val face = Array(5) { Path() }
    val snowAll = Path()
    val snowLit = Path()
    val snowRidgeLit = Path()
    val snowRidgeDeep = Path()
    val couLit = Path()
    val couShade = Path()
    val strataL = Path()
    val strataS = Path()
    val ledges = Path()
    val cracks = Path()
    val screeLit = Path()
    val screeShade = Path()
    val screeDots = Path()
    val snowRocksLit = Path()
    val snowRocksShade = Path()
    val ripLit = Path()
    val ripShade = Path()
}

private class BgRange(
    val peaks: Array<BgPeak>, val under: Path, val crest: Path, val gul: Array<BgPts>,
    val ax: FloatArray, val ay: FloatArray, val baseY: Float,
)

/** A peak's flank from (x0, y0) to (x1, y1), concave and jagged; the start point is included. */
private fun bgFlank(out: BgPts, x0: Float, y0: Float, x1: Float, y1: Float, up: Boolean, jag: Float, salt: Int, key: Int) {
    val n = max(4, (abs(x1 - x0) / 0.15f).toInt() + 1)
    out.add(x0, y0)
    for (k in 1 until n) {
        val t = k / n.toFloat()
        val prog = if (up) t * sqrtish(t) else 1f - (1f - t) * sqrtish(1f - t)
        var yy = y0 + (y1 - y0) * prog
        var xx = x0 + (x1 - x0) * t
        val h1 = hash01(key * 31 + k, salt)
        val h2 = hash01(key * 31 + k, salt + 1)
        yy += (h1 - 0.5f) * 2f * jag * sin(t * 3.1416f)
        xx += (h2 - 0.5f) * 0.07f
        // The odd shoulder: a short step in the ridge.
        if (k % 3 == 0 && h1 > 0.55f) yy -= jag * 0.8f
        // Now and then a thin pinnacle stands up on the ridge.
        if (k > 1 && k < n - 1 && hash01(key * 31 + k, salt + 2) > 0.8f) {
            out.add(xx - 0.012f, yy + 0.008f)
            out.add(xx + 0.002f, yy - 0.05f - 0.03f * h2)
            out.add(xx + 0.014f, yy + 0.01f)
        } else {
            out.add(xx, yy)
        }
    }
    out.add(x1, y1)
}

private fun sqrtish(v: Float): Float = kotlin.math.sqrt(v.coerceAtLeast(0f))

private fun bgBuild(sp: BgSpec, u: Float, detail: Boolean): BgRange {
    val np = sp.apx.size
    val salt = sp.salt
    val sm = 10
    val gm = 8
    val base = sp.baseY
    val gul = Array(np + 1) { s ->
        val g = BgPts(gm + 2)
        val drift = (hash01(s, salt + 40) - 0.5f) * 0.5f
        var kink = 0f
        for (j in 0..gm) {
            val t = j / gm.toFloat()
            kink += (hash01(s * 17 + j, salt + 41) - 0.5f) * 0.09f
            g.add(sp.sad[s] + drift * t + kink * sin(t * 3.1416f), sp.sadY[s] + (base - sp.sadY[s]) * t)
        }
        g
    }
    val under = Path()
    val crest = Path()
    val ax = FloatArray(np)
    val ay = FloatArray(np)
    var firstCrest = true
    val peaks = Array(np) { i ->
        val pk = BgPeak(sp.sad[i], sp.sad[i + 1])
        val sx = sp.sad[i]
        val sy = sp.sadY[i]
        val ex = sp.sad[i + 1]
        val ey = sp.sadY[i + 1]
        val px = sp.apx[i]
        val py = sp.apy[i]
        ax[i] = px
        ay[i] = py
        val gl = gul[i]
        val gr = gul[i + 1]
        val fl = BgPts()
        val fr = BgPts()
        bgFlank(fl, sx, sy, px, py, true, sp.jag, salt, i * 2)
        bgFlank(fr, px, py, ex, ey, false, sp.jag, salt, i * 2 + 1)
        // A little horn beside the summit on some peaks.
        if (hash01(i, salt + 5) > 0.45f && fr.n > 4) {
            fr.x[1] = px + 0.08f
            fr.y[1] = py + 0.03f
            fr.x[2] = px + 0.15f
            fr.y[2] = py + 0.028f
        }
        crest.bgRun(fl, 0, fl.n - 1, u, move = firstCrest)
        crest.bgRun(fr, 1, fr.n - 1, u)
        under.bgRun(fl, 0, fl.n - 1, u, move = firstCrest)
        under.bgRun(fr, 1, fr.n - 1, u)
        firstCrest = false

        // The ridges running down from the summit: the main one splits lit from shaded, the others grade the light.
        val nR = if (detail) 4 else 1
        val main = if (detail) 2 else 0
        val offs = if (detail) floatArrayOf(-0.5f, -0.24f, 0f, 0.3f) else floatArrayOf(0f)
        val lo = gl.x[gm] + 0.1f
        val hi = max(lo + 0.05f * nR, gr.x[gm] - 0.1f)
        val xb = FloatArray(nR)
        for (r in 0 until nR) {
            var x = px + (sp.lean[i] + offs[r]) * (base - py)
            x = x.coerceIn(lo, hi)
            if (r > 0) x = max(x, xb[r - 1] + 0.07f)
            xb[r] = x
        }
        val ridges = Array(nR) { r ->
            val line = BgPts(sm + 2)
            var kink = 0f
            for (j in 0..sm) {
                val t = j / sm.toFloat()
                kink += (hash01(i * 50 + r * 11 + j, salt + 6 + r) - 0.5f) * 0.05f
                line.add(px + (xb[r] - px) * t + kink * sin(t * 3.1416f), py + (base - py) * t)
            }
            line
        }
        val spur = ridges[main]
        var js = 1
        while (js < sm - 2 && spur.y[js] < py + sp.snow[i]) js++
        val sxS = spur.x[js]
        val syS = spur.y[js]
        var iCL = 0
        run {
            var idx = fl.n - 1
            while (idx > 0 && fl.y[idx] < syS - 0.03f) idx--
            iCL = idx
        }
        var iCR = fr.n - 1
        run {
            var idx = 0
            while (idx < fr.n - 1 && fr.y[idx] < syS - 0.03f) idx++
            iCR = idx
        }
        fun teeth(toX: Float, toY: Float, key: Int): BgPts {
            val line = BgPts(10)
            line.add(sxS, syS)
            val steps = 7
            for (k in 1..steps) {
                val t = k / (steps + 1).toFloat()
                val tooth = ((k % 2) * 2 - 1) * (0.012f + 0.03f * hash01(key + k, salt + 8))
                line.add(sxS + (toX - sxS) * t + (hash01(key + k, salt + 9) - 0.5f) * 0.03f, syS + (toY - syS) * t + tooth)
            }
            line.add(toX, toY)
            return line
        }
        val sl = teeth(fl.x[iCL], fl.y[iCL], i * 100)
        val sr = teeth(fr.x[iCR], fr.y[iCR], i * 100 + 50)

        pk.snowAll.bgRun(fl, iCL, fl.n - 1, u, move = true)
        pk.snowAll.bgRun(fr, 1, iCR, u)
        pk.snowAll.bgRun(sr, sr.n - 2, 0, u)
        pk.snowAll.bgRun(sl, 1, sl.n - 1, u)
        pk.snowAll.close()

        pk.snowLit.bgRun(fl, iCL, fl.n - 1, u, move = true)
        pk.snowLit.bgRun(spur, 1, js, u)
        pk.snowLit.bgRun(sl, 1, sl.n - 1, u)
        pk.snowLit.close()

        if (!detail) {
            pk.rockLit.bgRun(fl, 0, iCL, u, move = true)
            pk.rockLit.bgRun(sl, sl.n - 2, 0, u)
            pk.rockLit.bgRun(spur, js + 1, sm, u)
            pk.rockLit.bgRun(gl, gm, 0, u)
            pk.rockLit.close()
            return@Array pk
        }

        // ---- the five faces, from the left-hand flank to the right-hand one
        pk.face[0].bgRun(fl, 0, fl.n - 1, u, move = true)
        pk.face[0].bgRun(ridges[0], 1, sm, u)
        pk.face[0].bgRun(gl, gm, 0, u)
        pk.face[0].close()
        for (f in 1..3) {
            pk.face[f].bgRun(ridges[f - 1], 0, sm, u, move = true)
            pk.face[f].bgRun(ridges[f], sm, 0, u)
            pk.face[f].close()
        }
        pk.face[4].bgRun(ridges[3], 0, sm, u, move = true)
        pk.face[4].bgRun(gr, gm, 0, u)
        pk.face[4].bgRun(fr, fr.n - 1, 1, u)
        pk.face[4].close()

        // ---- shadows the ridges throw across the snow, always on their right
        for (r in 0 until nR) {
            if (r == main) continue
            val line = ridges[r]
            var jr = 0
            while (jr < sm && line.y[jr + 1] < syS - 0.01f) jr++
            if (jr < 2) continue
            val path = if (r < main) pk.snowRidgeLit else pk.snowRidgeDeep
            path.moveTo(line.x[0] * u, line.y[0] * u)
            for (j in 1..jr) {
                val w = (0.012f + 0.02f * hash01(i * 20 + r * 5 + j, salt + 20)) * sin((j.toFloat() / (jr + 1)) * 3.1416f + 0.3f).coerceAtLeast(0.2f)
                path.lineTo((line.x[j] + w) * u, line.y[j] * u)
            }
            for (j in jr downTo 0) path.lineTo(line.x[j] * u, line.y[j] * u)
            path.close()
        }
        // ---- rock islands poking out of the snow, and the ripples the wind leaves in it
        for (c in 0 until 6) {
            val r = if (c % 3 == 2) 3 else c % 2
            val line = ridges[r]
            val yy = py + 0.14f + hash01(i * 13 + c, salt + 80) * max(0.04f, syS - py - 0.2f)
            val cx = line.xAt(yy) + (hash01(i * 13 + c, salt + 81) - 0.4f) * 0.08f
            val rr = 0.014f + 0.026f * hash01(i * 13 + c, salt + 82)
            val path = if (cx < spur.xAt(yy)) pk.snowRocksLit else pk.snowRocksShade
            path.moveTo((cx - rr) * u, (yy + rr * 0.35f) * u)
            for (k in 1..6) {
                val a = k / 7f
                val hgt = (0.35f + 0.9f * hash01(i * 99 + c * 7 + k, salt + 87)) * sin(a * 3.1416f)
                path.lineTo((cx - rr + 2f * rr * a) * u, (yy - rr * hgt) * u)
            }
            path.lineTo((cx + rr) * u, (yy + rr * 0.35f) * u)
            path.close()
        }
        for (c in 0 until 16) {
            val yy = py + 0.13f + hash01(i * 17 + c, salt + 83) * max(0.04f, syS - py - 0.18f)
            val rIdx = (hash01(i * 17 + c, salt + 84) * nR).toInt().coerceAtMost(nR - 1)
            val cx = ridges[rIdx].xAt(yy) + (hash01(i * 17 + c, salt + 85) - 0.5f) * 0.09f
            val len = 0.04f + 0.04f * hash01(i * 17 + c, salt + 86)
            val path = if (cx < spur.xAt(yy)) pk.ripLit else pk.ripShade
            path.moveTo(cx * u, yy * u)
            path.quadraticTo((cx + len * 0.5f) * u, (yy - 0.008f) * u, (cx + len) * u, (yy + 0.002f) * u)
        }
        // ---- couloirs: tongues of snow running down the rock
        for (c in 0 until 4) {
            val onLit = c % 2 == 0
            val line = if (onLit) sl else sr
            val k = 1 + (hash01(i * 5 + c, salt + 34) * (line.n - 3)).toInt()
            val bx = line.x[k]
            val by = line.y[k]
            val len = 0.22f + 0.22f * hash01(i * 5 + c, salt + 35)
            val path = if (onLit) pk.couLit else pk.couShade
            val steps = 5
            val lx = FloatArray(steps + 1)
            val rx = FloatArray(steps + 1)
            val yy = FloatArray(steps + 1)
            for (s in 0..steps) {
                val t = s / steps.toFloat()
                val w = 0.035f * (1f - t) + 0.006f
                val cx = bx + (if (onLit) -1f else 1f) * 0.07f * t + sin(t * 5f + c + i) * 0.015f
                lx[s] = cx - w
                rx[s] = cx + w
                yy[s] = by + len * t
            }
            if (yy[steps] > base - 0.14f) continue
            path.moveTo(lx[0] * u, yy[0] * u)
            for (s in 1..steps) path.lineTo(lx[s] * u, yy[s] * u)
            for (s in steps downTo 0) path.lineTo(rx[s] * u, yy[s] * u)
            path.close()
        }
        // ---- strata and cracks: broken bands of darker rock across the faces
        val wall = i == 5 || i == 2
        var yb = syS + 0.06f
        while (yb < base - 0.18f) {
            val xl = gl.xAt(yb) + 0.05f
            val xr = gr.xAt(yb) - 0.05f
            if (xr - xl > 0.25f) {
                val segs = 2 + (hash01(i * 97 + (yb * 100f).toInt(), salt + 50) * 3f).toInt()
                for (s in 0 until segs) {
                    val h = hash01(i * 131 + s * 17 + (yb * 100f).toInt(), salt + 51)
                    val len = 0.07f + 0.15f * hash01(i * 31 + s + (yb * 100f).toInt(), salt + 52)
                    val x0 = xl + (xr - xl - len) * h
                    val slope = (hash01(i * 7 + s + (yb * 100f).toInt(), salt + 53) - 0.5f) * 0.02f
                    val path = if (x0 + len * 0.5f < spur.xAt(yb)) pk.strataL else pk.strataS
                    path.moveTo(x0 * u, yb * u)
                    path.lineTo((x0 + len * 0.5f) * u, (yb + slope * 0.4f + 0.004f) * u)
                    path.lineTo((x0 + len) * u, (yb + slope) * u)
                    if (wall && s == 0) {
                        pk.ledges.moveTo(x0 * u, (yb + 0.012f) * u)
                        pk.ledges.lineTo((x0 + len * 0.7f) * u, (yb + 0.01f) * u)
                    }
                }
            }
            yb += if (wall) 0.05f else 0.075f
        }
        if (wall) {
            for (c in 0 until 9) {
                val yc = syS + 0.1f + hash01(i * 9 + c, salt + 60) * (base - syS - 0.35f)
                val xc = spur.xAt(yc) + 0.08f + hash01(i * 9 + c, salt + 61) * max(0.05f, gr.xAt(yc) - spur.xAt(yc) - 0.2f)
                pk.cracks.moveTo(xc * u, yc * u)
                var cx = xc
                var cy = yc
                for (s in 0 until 4) {
                    cx += (hash01(i * 40 + c * 5 + s, salt + 62) - 0.5f) * 0.05f
                    cy += 0.035f + 0.03f * hash01(i * 40 + c * 5 + s, salt + 63)
                    pk.cracks.lineTo(cx * u, cy * u)
                }
            }
        }
        // ---- scree fans at the foot of the gully
        val gx = gl.x[gm]
        val apexY = base - 0.3f - 0.1f * hash01(i, salt + 70)
        val half = 0.4f + 0.2f * hash01(i, salt + 71)
        pk.screeLit.bgPoly(u, gx, apexY, gx - half, base + 0.02f, gx + 0.02f, base + 0.02f)
        pk.screeShade.bgPoly(u, gx, apexY, gx + 0.02f, base + 0.02f, gx + half, base + 0.02f)
        for (d in 0 until 26) {
            val t = hash01(i * 50 + d, salt + 72)
            val yy = apexY + (base - apexY) * (0.15f + 0.85f * hash01(i * 50 + d, salt + 73))
            val spread = (yy - apexY) / (base - apexY) * half
            val xx = gx + (t * 2f - 1f) * spread
            val rr = 0.006f + 0.008f * hash01(i * 50 + d, salt + 74)
            pk.screeDots.addOval(Rect(xx * u - rr * u, yy * u - rr * 0.6f * u, xx * u + rr * u, yy * u + rr * 0.6f * u))
        }
        pk
    }
    // The underlay closes below the last flank, so it also covers the hairlines between the tiles.
    under.lineTo(sp.sad[np] * u, base * u)
    under.lineTo(sp.sad[0] * u, base * u)
    under.close()
    return BgRange(peaks, under, crest, gul, ax, ay, base)
}

/** A range of peaks made from noise, for the far layers. */
private fun bgSpec(
    x0: Float, x1: Float, spacing: Float, baseY: Float, apexTop: Float, apexBot: Float,
    saddleTop: Float, saddleBot: Float, snowMin: Float, snowMax: Float, jag: Float, salt: Int,
): BgSpec {
    val sad = ArrayList<Float>()
    val sadY = ArrayList<Float>()
    val apx = ArrayList<Float>()
    val apy = ArrayList<Float>()
    val snow = ArrayList<Float>()
    val lean = ArrayList<Float>()
    var x = x0
    var i = 0
    sad.add(x)
    sadY.add(saddleTop + (saddleBot - saddleTop) * hash01(i, salt))
    while (x < x1) {
        val w = spacing * (0.75f + 0.6f * hash01(i, salt + 1))
        apx.add(x + w * (0.35f + 0.3f * hash01(i, salt + 2)))
        x += w
        sad.add(x)
        sadY.add(saddleTop + (saddleBot - saddleTop) * hash01(i + 1, salt))
        val ay = apexTop + (apexBot - apexTop) * hash01(i, salt + 3)
        apy.add(min(ay, min(sadY[i], sadY[i + 1]) - 0.14f))
        snow.add(snowMin + (snowMax - snowMin) * hash01(i, salt + 4))
        lean.add((hash01(i, salt + 5) - 0.4f) * 0.4f)
        i++
    }
    return BgSpec(sad.toFloatArray(), sadY.toFloatArray(), apx.toFloatArray(), apy.toFloatArray(), snow.toFloatArray(), lean.toFloatArray(), baseY, jag, salt)
}

/** The great massif, authored by hand so its peaks stand above the places the child visits. */
private val BG_MAIN = BgSpec(
    sad = floatArrayOf(-0.8f, 0.9f, 1.75f, 2.55f, 3.5f, 4.4f, 5.45f, 6.4f, 7.05f, 8.15f, 9.1f, 9.9f),
    sadY = floatArrayOf(0.5f, 0.3f, 0.26f, 0.28f, 0.26f, 0.3f, 0.24f, 0.3f, 0.32f, 0.28f, 0.3f, 0.55f),
    apx = floatArrayOf(0.3f, 1.3f, 2.1f, 3.0f, 3.95f, 4.85f, 5.9f, 6.7f, 7.6f, 8.6f, 9.5f),
    apy = floatArrayOf(0.02f, -0.12f, -0.24f, -0.04f, -0.18f, -0.34f, -0.1f, -0.2f, -0.16f, -0.08f, 0.04f),
    snow = floatArrayOf(0.36f, 0.42f, 0.55f, 0.4f, 0.5f, 0.64f, 0.44f, 0.5f, 0.46f, 0.4f, 0.3f),
    lean = floatArrayOf(0.14f, 0.1f, -0.06f, 0.12f, -0.04f, 0.16f, 0.02f, 0.1f, -0.08f, 0.12f, -0.1f),
    baseY = 0.84f, jag = 0.03f, salt = 7,
)

/** Which saddles the glacier and the second waterfall sit in. */
private const val BG_GLACIER_SAD = 3
private const val BG_FALL2_SAD = 8

/** Everything built once per scale. */
private class BgGeo(
    val main: BgRange, val back: BgRange, val far: Array<BgRange>,
    val pines: Array<Path>, val pinesShade: Array<Path>, val treeline: Path, val pineChunk: Float,
    val meadow: Path, val pathFill: Path, val pathEdgeA: Path, val pathEdgeB: Path,
    val stones: Array<Path>, val stonesShade: Array<Path>, val pebbles: Array<Path>,
    val flowerPts: Array<Array<List<Offset>>>, val flowerHearts: Array<List<Offset>>,
    val boulders: Path, val bouldersShade: Path, val bouldersTop: Path,
    val brook: Path, val tufts: Array<Path>,
    val iceLit: Path, val iceShade: Path, val iceCliff: Path, val crevasses: Path, val iceFlow: Path,
    val fall1: Float, val fall2: Float, val glacierY: Float,
    val hills: Path, val hillPines: Array<Path>, val hillPinesShade: Array<Path>,
    val frontTufts: Array<Array<Path>>, val frontStems: Array<List<Offset>>, val frontHeads: Array<Array<List<Offset>>>,
)

/** The colours of the meadow flowers: white, yellow, magenta, blue, red, violet. */
private val BG_FLOWERS = arrayOf(Color(0xFFFFFFFF), Color(0xFFFFD84A), Color(0xFFE8509A), Color(0xFF4F8BFF), Color(0xFFE8473F), Color(0xFFA974F0))

private const val BG_CHUNK = 1.0f
private const val BG_CHUNKS = 10

private fun bgChunk(x: Float): Int = (x / BG_CHUNK).toInt().coerceIn(0, BG_CHUNKS - 1)

private fun bgPathY(x: Float): Float = 0.885f + 0.026f * sin(x * 1.25f) + 0.012f * sin(x * 3.1f + 1f)

private fun bgPathHalf(x: Float): Float = 0.045f + 0.012f * sin(x * 2.2f + 2f)

/** The line where the pines begin, across the whole width. */
private fun bgTreeY(x: Float): Float = 0.64f + 0.03f * sin(x * 1.7f + 0.4f) + 0.018f * sin(x * 5.3f + 1f) + 0.01f * sin(x * 11f)

/** The crest of the forested foothills in front of the massif. */
private fun bgHillY(x: Float, f1: Float, f2: Float): Float {
    val base = 0.575f + 0.035f * sin(x * 1.3f + 0.7f) + 0.018f * sin(x * 3.9f) + 0.01f * sin(x * 9f + 2f)
    val d1 = (x - f1) / 0.16f
    val d2 = (x - f2) / 0.16f
    return base + 0.09f * kotlin.math.exp(-d1 * d1) + 0.09f * kotlin.math.exp(-d2 * d2)
}

/** The meadow's top edge, where it meets the forest. */
private fun bgMeadowY(x: Float): Float = 0.745f + 0.016f * sin(x * 1.1f + 2f) + 0.008f * sin(x * 3.7f)

private val bgGeo = Memo { u -> bgBuildAll(u) }

private fun bgBuildAll(u: Float): BgGeo {
    val main = bgBuild(BG_MAIN, u, detail = true)
    val back = bgBuild(bgSpec(-0.4f, 6.9f, 1.15f, 0.8f, -0.36f, 0.04f, 0.24f, 0.44f, 0.4f, 0.62f, 0.03f, 51), u, detail = false)
    val far = arrayOf(
        bgBuild(bgSpec(-0.2f, 3.3f, 0.7f, 0.78f, -0.1f, 0.22f, 0.3f, 0.5f, 0.28f, 0.42f, 0.02f, 61), u, detail = false),
        bgBuild(bgSpec(-0.2f, 4.0f, 0.62f, 0.76f, 0.06f, 0.3f, 0.4f, 0.55f, 0.2f, 0.34f, 0.016f, 71), u, detail = false),
        bgBuild(bgSpec(-0.2f, 4.9f, 0.5f, 0.74f, 0.2f, 0.4f, 0.5f, 0.62f, 0.14f, 0.24f, 0.012f, 81), u, detail = false),
    )

    val fall1 = main.gul[BG_GLACIER_SAD].xAt(0.5f) + 0.02f
    val fall2 = main.gul[BG_FALL2_SAD].xAt(0.45f) + 0.03f

    // ---- pines at the foot of the massif, in chunks of one unit so a frame only draws what is on screen
    val pines = Array(BG_CHUNKS) { Path() }
    val shades = Array(BG_CHUNKS) { Path() }
    val treeline = Path()
    treeline.moveTo(-1f * u, 0.9f * u)
    var xx = -1f
    while (xx <= 10.2f) {
        treeline.lineTo(xx * u, bgTreeY(xx) * u)
        xx += 0.06f
    }
    treeline.lineTo(10.2f * u, 0.9f * u)
    treeline.close()
    for (row in 0 until 4) {
        val spacing = 0.062f - row * 0.006f
        val hMin = 0.05f + row * 0.011f
        var i = 0
        var x = -0.2f
        while (x < 9.4f) {
            val h = hash01(i + row * 997, 91)
            val px = x + (h - 0.5f) * spacing * 0.9f
            val top = bgTreeY(px)
            val by = top + 0.03f + row * 0.03f + hash01(i + row * 997, 92) * 0.02f
            if (by < bgMeadowY(px) + 0.02f && hash01(i + row * 997, 93) > 0.12f) {
                val ph = hMin + 0.03f * hash01(i + row * 997, 94)
                val c = bgChunk(px)
                pines[c].addPine(px * u, by * u, ph * 0.5f * u, ph * u)
                shades[c].addPineShade(px * u, by * u, ph * 0.5f * u, ph * u)
            }
            x += spacing
            i++
        }
    }

    // ---- forested foothills: a rolling ridge in blue-green with small pines, between the massif and the pine belt
    val hills = Path()
    hills.moveTo(-1f * u, 0.8f * u)
    xx = -1f
    while (xx <= 10.2f) {
        hills.lineTo(xx * u, bgHillY(xx, fall1, fall2) * u)
        xx += 0.06f
    }
    hills.lineTo(10.2f * u, 0.8f * u)
    hills.close()
    val hillPines = Array(BG_CHUNKS) { Path() }
    val hillPinesShade = Array(BG_CHUNKS) { Path() }
    var hx = -0.2f
    var hi = 0
    while (hx < 9.5f) {
        val h = hash01(hi, 141)
        val px = hx + (h - 0.5f) * 0.05f
        // Clumps of trees with open slopes between them.
        val clump = sin(px * 2.1f + 1f) + sin(px * 5.7f)
        if (clump > -0.5f && hash01(hi, 142) > 0.2f) {
            val by = bgHillY(px, fall1, fall2) + 0.012f + hash01(hi, 143) * 0.04f
            val ph = 0.028f + 0.022f * hash01(hi, 144)
            val c = bgChunk(px)
            hillPines[c].addPine(px * u, by * u, ph * 0.5f * u, ph * u)
            hillPinesShade[c].addPineShade(px * u, by * u, ph * 0.5f * u, ph * u)
        }
        hx += 0.045f
        hi++
    }

    // ---- the meadow
    val meadow = Path()
    meadow.moveTo(-1f * u, 1.1f * u)
    xx = -1f
    while (xx <= 10.2f) {
        meadow.lineTo(xx * u, bgMeadowY(xx) * u)
        xx += 0.08f
    }
    meadow.lineTo(10.2f * u, 1.1f * u)
    meadow.close()

    // ---- the stony path along the floor band
    val pathFill = Path()
    val edgeA = Path()
    val edgeB = Path()
    xx = -0.4f
    var first = true
    while (xx <= 9.5f) {
        val y0 = bgPathY(xx) - bgPathHalf(xx)
        if (first) pathFill.moveTo(xx * u, y0 * u) else pathFill.lineTo(xx * u, y0 * u)
        if (first) edgeA.moveTo(xx * u, y0 * u) else edgeA.lineTo(xx * u, y0 * u)
        first = false
        xx += 0.06f
    }
    first = true
    var rx = 9.5f
    while (rx >= -0.4f) {
        val y1 = bgPathY(rx) + bgPathHalf(rx)
        pathFill.lineTo(rx * u, y1 * u)
        if (first) edgeB.moveTo(rx * u, y1 * u) else edgeB.lineTo(rx * u, y1 * u)
        first = false
        rx -= 0.06f
    }
    pathFill.close()

    val stones = Array(BG_CHUNKS) { Path() }
    val stonesShade = Array(BG_CHUNKS) { Path() }
    val pebbles = Array(BG_CHUNKS) { Path() }
    var k = 0
    var sx = -0.3f
    while (sx < 9.4f) {
        val h = hash01(k, 101)
        val px = sx + h * 0.08f
        val py = bgPathY(px) + (hash01(k, 102) - 0.5f) * 2f * bgPathHalf(px) * 0.85f
        val w = 0.014f + 0.022f * hash01(k, 103)
        val c = bgChunk(px)
        val cx = px * u
        val cy = py * u
        stones[c].addOval(Rect(cx - w * u, cy - w * 0.38f * u, cx + w * u, cy + w * 0.38f * u))
        stonesShade[c].addOval(Rect(cx - w * 0.9f * u, cy - w * 0.05f * u, cx + w * 0.95f * u, cy + w * 0.4f * u))
        sx += 0.055f
        k++
    }
    for (d in 0 until 700) {
        val px = hash01(d, 104) * 9.4f - 0.2f
        val py = bgPathY(px) + (hash01(d, 105) - 0.5f) * 2f * bgPathHalf(px)
        val rr = 0.0025f + 0.003f * hash01(d, 106)
        pebbles[bgChunk(px)].addOval(Rect((px - rr) * u, (py - rr * 0.6f) * u, (px + rr) * u, (py + rr * 0.6f) * u))
    }

    // ---- boulders at the edges of the path
    val boulders = Path()
    val bShade = Path()
    val bTop = Path()
    for (b in 0 until 26) {
        val px = hash01(b, 111) * 9.2f
        val side = if (hash01(b, 112) > 0.5f) 1f else -1f
        val py = (bgPathY(px) + side * (bgPathHalf(px) + 0.02f + 0.06f * hash01(b, 113))).coerceIn(0.79f, 0.965f)
        val w = 0.022f + 0.04f * hash01(b, 114)
        val h = w * (0.55f + 0.3f * hash01(b, 115))
        val cx = px * u
        val by = py * u
        val ww = w * u
        val hh = h * u
        boulders.bgPoly(
            1f,
            cx - ww, by, cx - ww * 0.85f, by - hh * 0.6f, cx - ww * 0.35f, by - hh, cx + ww * 0.3f, by - hh * 0.95f,
            cx + ww * 0.85f, by - hh * 0.5f, cx + ww, by,
        )
        bShade.bgPoly(1f, cx + ww * 0.3f, by - hh * 0.95f, cx + ww * 0.85f, by - hh * 0.5f, cx + ww, by, cx + ww * 0.1f, by)
        bTop.addOval(Rect(cx - ww * 0.55f, by - hh * 1.05f, cx + ww * 0.25f, by - hh * 0.8f))
    }

    // ---- flowers, grouped by chunk, colour and size so a frame draws a handful of point lists
    val flowerPts = Array(BG_CHUNKS) { c ->
        val lists = Array(12) { ArrayList<Offset>() }
        for (d in 0 until 110) {
            val px = c * BG_CHUNK + hash01(c * 200 + d, 121) * BG_CHUNK
            val region = hash01(c * 200 + d, 122)
            val py = if (region < 0.5f) 0.757f + 0.03f * hash01(c * 200 + d, 123) else 0.935f + 0.03f * hash01(c * 200 + d, 123)
            val onPath = abs(py - bgPathY(px)) < bgPathHalf(px) + 0.01f
            if (onPath) continue
            val col = (hash01(c * 200 + d, 125) * 6f).toInt().coerceAtMost(5)
            val big = if (hash01(c * 200 + d, 124) > 0.55f) 1 else 0
            lists[col * 2 + big].add(Offset(px * u, py * u))
        }
        Array<List<Offset>>(12) { lists[it] }
    }
    val flowerHearts = Array(BG_CHUNKS) { c ->
        val l = ArrayList<Offset>()
        for (lst in flowerPts[c]) l.addAll(lst)
        l as List<Offset>
    }

    // ---- grass tufts on the meadow and along the path
    val tufts = Array(BG_CHUNKS) { c ->
        val p = Path()
        for (d in 0 until 40) {
            val px = c * BG_CHUNK + hash01(c * 300 + d, 131) * BG_CHUNK
            val near = hash01(c * 300 + d, 132) > 0.5f
            val py = if (near) bgPathY(px) + (if (hash01(c * 300 + d, 133) > 0.5f) 1f else -1f) * (bgPathHalf(px) + 0.012f) else 0.77f + 0.03f * hash01(c * 300 + d, 134)
            val th = 0.012f + 0.014f * hash01(c * 300 + d, 135)
            p.addPath(tuftPath(px * u, py * u, th * u, 0f))
        }
        p
    }

    // ---- the hanging glacier in the col between two peaks, and the falls that its meltwater feeds
    val ice = bgGlacier(main.gul[BG_GLACIER_SAD], u, 0.27f, 0.47f)

    // ---- the tall grass and flowers along the very front edge, drawn over everything
    val frontTufts = Array(BG_CHUNKS) { Array(3) { Path() } }
    val stems = Array(BG_CHUNKS) { ArrayList<Offset>() }
    val heads = Array(BG_CHUNKS) { Array(6) { ArrayList<Offset>() } }
    for (c in 0 until BG_CHUNKS) {
        for (d in 0 until 15) {
            val px = c * BG_CHUNK + hash01(c * 50 + d, 151) * BG_CHUNK
            val py = 0.974f + 0.006f * hash01(c * 50 + d, 152)
            val th = 0.012f + 0.014f * hash01(c * 50 + d, 153)
            frontTufts[c][d % 3].addPath(tuftPath(px * u, py * u, th * u, 0f))
        }
        for (d in 0 until 5) {
            val px = c * BG_CHUNK + hash01(c * 50 + d, 154) * BG_CHUNK
            val py = 0.976f + 0.006f * hash01(c * 50 + d, 155)
            val sh = 0.022f + 0.02f * hash01(c * 50 + d, 156)
            val lean = (hash01(c * 50 + d, 157) - 0.5f) * 0.012f
            stems[c].add(Offset(px * u, py * u))
            stems[c].add(Offset((px + lean) * u, (py - sh) * u))
            val cls = (hash01(c * 50 + d, 158) * 6f).toInt().coerceAtMost(5)
            heads[c][cls].add(Offset((px + lean) * u, (py - sh) * u))
        }
    }
    val frontStems = Array<List<Offset>>(BG_CHUNKS) { stems[it] }
    val frontHeads = Array(BG_CHUNKS) { c -> Array<List<Offset>>(6) { heads[c][it] } }

    // ---- a brook in the meadow, fed by the falls
    val brook = Path()
    bgBrook(brook, u, fall1 - 0.05f, 0.738f, 2.2f)
    bgBrook(brook, u, fall2 - 0.1f, 0.742f, 1.9f)

    return BgGeo(
        main, back, far, pines, shades, treeline, BG_CHUNK, meadow, pathFill, edgeA, edgeB, stones, stonesShade, pebbles,
        flowerPts, flowerHearts, boulders, bShade, bTop, brook, tufts,
        ice.lit, ice.shade, ice.cliff, ice.crevasses, ice.flow, fall1, fall2, 0.47f, hills, hillPines, hillPinesShade, frontTufts, frontStems, frontHeads,
    )
}

/** A meandering ribbon of water from ([x], [y]) running [len] units to the right. */
private fun bgBrook(p: Path, u: Float, x: Float, y: Float, len: Float) {
    val n = 14
    val top = FloatArray(n + 1)
    val bot = FloatArray(n + 1)
    for (k in 0..n) {
        val t = k / n.toFloat()
        val cy = y + 0.012f * sin(t * 7f + x) + 0.004f * t
        val w = 0.0045f + 0.004f * sin(t * 3.1416f)
        top[k] = (cy - w) * u
        bot[k] = (cy + w * 1.4f) * u
    }
    p.moveTo(x * u, top[0])
    for (k in 1..n) p.lineTo((x + len * k / n) * u, top[k])
    for (k in n downTo 0) p.lineTo((x + len * k / n) * u, bot[k])
    p.close()
}

private class BgIce(val lit: Path, val shade: Path, val cliff: Path, val crevasses: Path, val flow: Path)

/** The glacier: a blue-white tongue down the gully from [y0] to its snout at [y1], with crevasses and an ice cliff. */
private fun bgGlacier(g: BgPts, u: Float, y0: Float, y1: Float): BgIce {
    val steps = 8
    val cx = FloatArray(steps + 1)
    val lx = FloatArray(steps + 1)
    val rx = FloatArray(steps + 1)
    val yy = FloatArray(steps + 1)
    for (k in 0..steps) {
        val t = k / steps.toFloat()
        yy[k] = y0 + (y1 - y0) * t
        cx[k] = g.xAt(yy[k]) + 0.05f * sin(t * 3f)
        val w = 0.3f - 0.17f * t + (hash01(k, 301) - 0.5f) * 0.03f
        lx[k] = cx[k] - w
        rx[k] = cx[k] + w * (0.95f + 0.1f * hash01(k, 302))
    }
    val lit = Path()
    val shade = Path()
    val cliff = Path()
    val crev = Path()
    val flow = Path()
    val teeth = 9
    val tx = FloatArray(teeth + 1)
    val ty = FloatArray(teeth + 1)
    for (s in 0..teeth) {
        val t = s / teeth.toFloat()
        tx[s] = rx[steps] + (lx[steps] - rx[steps]) * t
        ty[s] = y1 + (if (s == 0 || s == teeth) 0f else ((s % 2) * 2 - 1) * 0.011f + (hash01(s, 303) - 0.5f) * 0.01f)
    }
    lit.moveTo(lx[0] * u, yy[0] * u)
    for (k in 1..steps) lit.lineTo(lx[k] * u, yy[k] * u)
    for (s in teeth - 1 downTo 1) if (tx[s] < cx[steps]) lit.lineTo(tx[s] * u, ty[s] * u)
    lit.lineTo(cx[steps] * u, (y1 + 0.006f) * u)
    for (k in steps - 1 downTo 0) lit.lineTo(cx[k] * u, yy[k] * u)
    lit.close()
    shade.moveTo(cx[0] * u, yy[0] * u)
    shade.lineTo(rx[0] * u, yy[0] * u)
    for (k in 1..steps) shade.lineTo(rx[k] * u, yy[k] * u)
    for (s in 1 until teeth) if (tx[s] >= cx[steps]) shade.lineTo(tx[s] * u, ty[s] * u)
    shade.lineTo(cx[steps] * u, (y1 + 0.006f) * u)
    for (k in steps - 1 downTo 1) shade.lineTo(cx[k] * u, yy[k] * u)
    shade.close()
    // The ice cliff under the snout.
    cliff.moveTo(lx[steps] * u, y1 * u)
    for (s in 1..teeth) {
        val t = s / teeth.toFloat()
        val x = lx[steps] + (rx[steps] - lx[steps]) * t
        cliff.lineTo(x * u, (y1 + ((s % 2) * 2 - 1) * -0.011f) * u)
    }
    for (s in teeth downTo 0) {
        val t = s / teeth.toFloat()
        val x = lx[steps] + (rx[steps] - lx[steps]) * t
        cliff.lineTo(x * u, (y1 + 0.045f + (hash01(s, 304) - 0.5f) * 0.015f) * u)
    }
    cliff.close()
    for (q in 0 until 4) {
        val t = (q + 0.8f) / 5f
        val k = (t * steps).toInt().coerceAtMost(steps - 1)
        val y = yy[k]
        val a = lx[k] + (rx[k] - lx[k]) * 0.1f
        val b = lx[k] + (rx[k] - lx[k]) * 0.85f
        crev.moveTo(a * u, y * u)
        crev.quadraticTo((a + b) / 2f * u, (y + 0.016f) * u, b * u, (y - 0.004f) * u)
    }
    for (f in 0 until 6) {
        val t0 = 0.08f + 0.1f * hash01(f, 305)
        val off = (f - 2.5f) * 0.12f
        val k0 = (t0 * steps).toInt()
        flow.moveTo((cx[k0] + off * (0.7f)) * u, yy[k0] * u)
        flow.lineTo((cx[steps - 2] + off * 0.45f) * u, yy[steps - 2] * u)
    }
    return BgIce(lit, shade, cliff, crev, flow)
}

// ---------------------------------------------------------------------------------------------- the draw

/** Mist, rain and night take their share of a colour; far layers take more than near ones. */
private fun Color.bg(pen: Pen, k: Float = 0.6f, fog: Float = 0.45f): Color {
    val oc = overcast(pen)
    val c = if (oc > 0f) lerp(this, Color(0xFFB3BCCB), oc * fog) else this
    return c.atNight(pen.night, k)
}

internal fun DrawScope.bergBack(st: Stage, pen: Pen) {
    val geo = bgGeo.of(st.u)
    val u = st.u
    bgSky(st, pen)
    bgFar(st, pen, geo)
    bgFlock(st, pen)
    bgMassif(st, pen, geo)
    bgPlumes(st, pen, geo)
    bgFarHut(st, pen)
    bgParaglider(st, pen)
    bgEagles(st, pen)
    // After rain a wide rainbow arches over the valley, translucent in front of the mountains.
    if (pen.rainbow > 0.01f) drawRainbow(Offset(st.fx(0.52f, 0.1f), 0.8f * u), 0.74f * u, 0.024f * u, pen.rainbow * 0.6f)
    bgCable(st, pen)
    bgGroundAndFoot(st, pen, geo)
    bgRays(st, pen)
}

internal fun DrawScope.bergFront(st: Stage, pen: Pen) {
    bgFront(st, pen)
}

// ---------------------------------------------------------------------------------------------- sky

private fun DrawScope.bgSky(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val oc = overcast(pen)
    val rain = pen.weather == Weather.RAIN
    val greyTop = if (rain) Color(0xFF76839D) else Color(0xFF97A5BE)
    val greyLow = if (rain) Color(0xFFC1C9D7) else Color(0xFFE4E8F0)
    val top = lerp(lerp(BgC.skyTop, greyTop, oc), Color(0xFF0F0C33), n)
    val mid = lerp(lerp(BgC.skyMid, lerp(greyTop, greyLow, 0.5f), oc), Color(0xFF241D63), n)
    val low = lerp(lerp(BgC.skyLow, greyLow, oc), Color(0xFF55409A), n)
    val gold = lerp(lerp(BgC.skyGold, greyLow, oc), Color(0xFF7A4FA8), n)
    drawRect(
        Brush.verticalGradient(0f to top, 0.45f to mid, 0.78f to low, 1f to gold, startY = SKY_TOP * u, endY = 0.8f * u),
        Offset(0f, SKY_TOP * u), Size(st.w, (0.86f - SKY_TOP) * u),
    )
    val vis = (1f - n) * (1f - 0.85f * oc)
    val sun = Offset(st.fx(0.13f, 0.03f), -0.1f * u)
    if (vis > 0.01f) {
        drawCircle(
            safeRadialGradient(listOf(Color(0xFFFFD9A0).copy(alpha = 0.7f * vis), Color(0xFFFFB86B).copy(alpha = 0f)), center = sun, radius = 1.6f * u),
            1.6f * u, sun, blendMode = BlendMode.Screen,
        )
        drawSun(sun, 0.075f * u, pen, vis, rays = false, color = BgC.sun)
    }
    drawStars(st, pen, 0.5f)
    drawMoon(Offset(st.fx(0.84f, 0.03f), -0.06f * u), 0.045f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, pen, 0.95f, -0.3f, 0.6f)
    drawClouds(st, pen, -0.27f, 0.14f, 4, 0.08f, salt = 5)
}

// ---------------------------------------------------------------------------------------------- far ranges and the second massif

private fun DrawScope.bgRange(r: BgRange, rock: Color, shade: Color, snowLit: Color, snowShade: Color) {
    drawPath(r.under, shade)
    for (p in r.peaks) {
        drawPath(p.rockLit, rock)
        drawPath(p.snowAll, snowShade)
        drawPath(p.snowLit, snowLit)
    }
}

/** Fades the foot of a layer into the haze of the valley. */
private fun DrawScope.bgHaze(st: Stage, pen: Pen, base: Float, height: Float, alpha: Float) {
    val u = st.u
    val haze = lerp(BgC.skyLow, BgC.skyGold, 0.25f).bg(pen, 0.7f, 0.9f)
    drawRect(
        Brush.verticalGradient(listOf(haze.copy(alpha = 0f), haze.copy(alpha = alpha)), startY = (base - height) * u, endY = base * u),
        Offset(0f, (base - height) * u), Size(st.w, height * u + 1f),
    )
}

private fun DrawScope.bgFar(st: Stage, pen: Pen, geo: BgGeo) {
    val u = st.u
    translate(-st.cam * 0.08f * u, 0f) {
        bgRange(geo.far[0], Color(0xFFC3D3F0).bg(pen, 0.7f, 0.8f), Color(0xFFABBBE3).bg(pen, 0.7f, 0.8f), Color(0xFFF6F8FF).bg(pen, 0.7f, 0.8f), Color(0xFFD5DEF4).bg(pen, 0.7f, 0.8f))
    }
    bgHaze(st, pen, 0.78f, 0.34f, 0.5f)
    translate(-st.cam * 0.16f * u, 0f) {
        bgRange(geo.far[1], Color(0xFFAABCE6).bg(pen, 0.7f, 0.75f), Color(0xFF90A3D8).bg(pen, 0.7f, 0.75f), Color(0xFFEBF1FF).bg(pen, 0.7f, 0.75f), Color(0xFFC3D0F0).bg(pen, 0.7f, 0.75f))
    }
    bgHaze(st, pen, 0.76f, 0.3f, 0.5f)
    translate(-st.cam * 0.3f * u, 0f) {
        bgRange(geo.far[2], Color(0xFF8FA3D9).bg(pen, 0.7f, 0.7f), Color(0xFF7589C8).bg(pen, 0.7f, 0.7f), Color(0xFFE6EDFC).bg(pen, 0.7f, 0.7f), Color(0xFFB8C6EC).bg(pen, 0.7f, 0.7f))
        bgFarFall(st, pen)
    }
    bgHaze(st, pen, 0.74f, 0.3f, 0.5f)
    bgMistBand(st, pen, 0.3f, 0.5f, 0.18f, 0.3f, 3, 0.05f, 0.5f)
    translate(-st.cam * 0.55f * u, 0f) {
        bgRange(geo.back, Color(0xFF7787C6).bg(pen, 0.65f, 0.6f), Color(0xFF5F6EAD).bg(pen, 0.65f, 0.6f), Color(0xFFFFEFD8).bg(pen, 0.65f, 0.6f), Color(0xFFB9C6EE).bg(pen, 0.65f, 0.6f))
        drawPath(geo.back.crest, Ink.line, alpha = 0.18f, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    bgHaze(st, pen, 0.8f, 0.4f, 0.4f)
    bgMistBand(st, pen, 0.2f, 0.7f, 0.14f, 0.4f, 4, 0.06f, 0.7f)
}

/** A thin far-off waterfall in the second layer of blue peaks, with its mist. */
private fun DrawScope.bgFarFall(st: Stage, pen: Pen) {
    val u = st.u
    val x = 2.7f
    val top = 0.22f
    val bot = 0.5f
    val c = Color(0xFFF2F7FF).bg(pen, 0.6f, 0.5f)
    drawLine(c, Offset(x * u, top * u), Offset((x + 0.004f) * u, bot * u), strokeWidth = 0.006f * u, cap = StrokeCap.Round, alpha = 0.9f)
    val t = pen.t
    for (k in 0 until 3) {
        val ph = wrap(t * 0.2f + k / 3f, 1f)
        drawCircle(c, (0.012f + ph * 0.02f) * u, Offset((x + 0.004f + sin(ph * 5f + k) * 0.006f) * u, (bot - ph * 0.025f) * u), alpha = 0.5f * (1f - ph))
    }
}

/** A band of mist with drifting cloud puffs, [p] times as fast as the camera. */
private fun DrawScope.bgMistBand(st: Stage, pen: Pen, y: Float, p: Float, thick: Float, alpha: Float, puffs: Int, scale: Float, drift: Float) {
    val u = st.u
    val oc = overcast(pen)
    val col = lerp(Color(0xFFF4F9FF), Color(0xFFD5DBE8), oc).atNight(pen.night, 0.7f)
    val a = alpha * (1f + 0.6f * oc)
    drawRect(
        Brush.verticalGradient(0f to col.copy(alpha = 0f), 0.5f to col.copy(alpha = a.coerceAtMost(0.9f)), 1f to col.copy(alpha = 0f), startY = (y - thick) * u, endY = (y + thick) * u),
        Offset(0f, (y - thick) * u), Size(st.w, 2f * thick * u),
    )
    val span = st.w + 1.2f * u
    val shade = lerp(Color(0xFFD7E4F8), Color(0xFFB9C2D6), oc).atNight(pen.night, 0.75f)
    for (i in 0 until puffs) {
        val s = u * scale * (0.8f + 0.7f * hash01(i, 401 + (y * 100f).toInt()))
        val x = wrap(hash01(i, 402 + (y * 100f).toInt()) * span + pen.t * u * 0.004f * drift * (1f + hash01(i, 403)) - st.cam * u * p, span) - 0.6f * u
        val yy = (y + (hash01(i, 404 + (y * 100f).toInt()) - 0.5f) * thick * 1.2f) * u
        // A wisp of mist: a long flat lens with a lighter back and a faint shadow underneath.
        val w = s * 8f
        val h = s * 0.55f
        val k = a.coerceAtMost(1f)
        drawOval(shade, Offset(x - w * 0.42f, yy + h * 0.1f), Size(w * 0.84f, h * 0.7f), alpha = 0.3f * k)
        drawOval(col, Offset(x - w * 0.5f, yy - h * 0.5f), Size(w, h), alpha = 0.55f * k)
        drawOval(col, Offset(x - w * 0.3f, yy - h * 0.95f), Size(w * 0.56f, h), alpha = 0.5f * k)
    }
}

// ---------------------------------------------------------------------------------------------- the great massif

private fun DrawScope.bgMassif(st: Stage, pen: Pen, geo: BgGeo) {
    val u = st.u
    val r = geo.main
    translate(-st.cam * u, 0f) {
        val faceBrush = Array(5) { f ->
            Brush.verticalGradient(
                0f to Color(BgC.faceTop[f]).bg(pen, 0.62f, 0.35f), 1f to Color(BgC.faceLow[f]).bg(pen, 0.62f, 0.35f),
                startY = -0.3f * u, endY = 0.84f * u,
            )
        }
        val snowLit = BgC.snowLit.bg(pen, 0.66f, 0.3f)
        val snowShade = BgC.snowShade.bg(pen, 0.66f, 0.3f)
        val ridgeLit = BgC.snowRidgeLit.bg(pen, 0.66f, 0.3f)
        val ridgeDeep = BgC.snowRidgeDeep.bg(pen, 0.66f, 0.3f)
        val rockIsleLit = Color(BgC.faceTop[1]).bg(pen, 0.62f, 0.35f)
        val rockIsleShade = Color(BgC.faceTop[3]).bg(pen, 0.62f, 0.35f)
        val couLit = BgC.couLit.bg(pen, 0.66f, 0.3f)
        val couShade = BgC.couShade.bg(pen, 0.66f, 0.3f)
        val strataL = BgC.strataL.bg(pen, 0.5f, 0.2f)
        val strataS = BgC.strataS.bg(pen, 0.5f, 0.2f)
        val ledge = BgC.ledge.bg(pen, 0.66f, 0.3f)
        val screeLit = BgC.screeLit.bg(pen, 0.62f, 0.35f)
        val screeShade = BgC.screeShade.bg(pen, 0.62f, 0.35f)
        drawPath(r.under, faceBrush[3])
        for (p in r.peaks) {
            if (p.x1 < st.cam - 1.2f || p.x0 > st.cam + st.vw + 1.2f) continue
            for (f in 0 until 5) drawPath(p.face[f], faceBrush[f])
            drawPath(p.strataL, strataL, alpha = 0.5f, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(p.strataS, strataS, alpha = 0.4f, style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(p.cracks, strataS, alpha = 0.45f, style = Stroke(pen.lw * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(p.snowAll, snowShade)
            drawPath(p.snowLit, snowLit)
            drawPath(p.snowRidgeLit, ridgeLit)
            drawPath(p.snowRidgeDeep, ridgeDeep)
            drawPath(p.ripLit, ridgeLit, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
            drawPath(p.ripShade, ridgeDeep, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round))
            drawPath(p.snowRocksLit, rockIsleLit)
            drawPath(p.snowRocksShade, rockIsleShade)
            drawPath(p.couLit, couLit)
            drawPath(p.couShade, couShade)
            drawPath(p.ledges, ledge, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
            drawPath(p.screeLit, screeLit)
            drawPath(p.screeShade, screeShade)
            drawPath(p.screeDots, screeShade, alpha = 0.6f)
        }
        drawPath(r.crest, Ink.line, alpha = 0.4f, style = Stroke(pen.lw * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // The glacier.
        drawPath(geo.iceLit, BgC.iceLit.bg(pen, 0.6f, 0.3f))
        drawPath(geo.iceShade, BgC.iceShade.bg(pen, 0.6f, 0.3f))
        drawPath(geo.iceCliff, BgC.iceDeep.bg(pen, 0.55f, 0.3f))
        drawPath(geo.iceFlow, Color.White, alpha = 0.35f, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
        drawPath(geo.crevasses, BgC.iceDeep.bg(pen, 0.55f, 0.3f), alpha = 0.75f, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        drawPath(geo.iceLit, Ink.line, alpha = 0.3f, style = pen.thin)
        drawPath(geo.iceShade, Ink.line, alpha = 0.3f, style = pen.thin)
    }
    bgMistBand(st, pen, 0.5f, 0.9f, 0.1f, 0.3f, 4, 0.05f, 1f)
}

private fun DrawScope.bgFalls(st: Stage, pen: Pen, geo: BgGeo) {
    bgFall(st, pen, geo.fall1, 0.505f, 0.74f, 0.035f, 0.05f, 11)
    bgFall(st, pen, geo.fall2, 0.34f, 0.54f, 0.03f, 0.04f, 23)
    bgFall(st, pen, geo.fall2 + 0.045f, 0.54f, 0.745f, 0.04f, 0.06f, 37)
}

/** A waterfall: a bright ribbon with streaks running down it and mist rising where it lands. */
private fun DrawScope.bgFall(st: Stage, pen: Pen, x: Float, y0: Float, y1: Float, w0: Float, w1: Float, seed: Int) {
    if (!st.sees(x - 0.2f, x + 0.2f)) return
    val u = st.u
    val t = pen.t
    val n = pen.night
    val oc = overcast(pen)
    val cx = st.x(x)
    val boost = 1f + 0.35f * oc
    val bodyC = lerp(Color(0xFFEAF8FF), Color(0xFFB4C8E8), n * 0.7f)
    val body = Path().apply {
        moveTo(cx - w0 * 0.5f * u, y0 * u)
        quadraticTo(cx - (w0 + w1) * 0.3f * u + 0.006f * u, (y0 + y1) * 0.5f * u, cx - w1 * 0.5f * u, y1 * u)
        lineTo(cx + w1 * 0.5f * u, y1 * u)
        quadraticTo(cx + (w0 + w1) * 0.3f * u + 0.006f * u, (y0 + y1) * 0.5f * u, cx + w0 * 0.5f * u, y0 * u)
        close()
    }
    // The wet dark rock beside the fall.
    val wet = Path().apply {
        moveTo(cx - w0 * 1.1f * u, y0 * u)
        lineTo(cx - w1 * 1.3f * u, y1 * u)
        lineTo(cx + w1 * 1.3f * u, y1 * u)
        lineTo(cx + w0 * 1.1f * u, y0 * u)
        close()
    }
    drawPath(wet, Color(0xFF2E3562), alpha = 0.28f)
    drawPath(body, bodyC, alpha = 0.9f)
    drawPath(body, BgC.iceShade.bg(pen, 0.5f, 0.2f), alpha = 0.6f, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
    val dashes = ArrayList<Offset>(40)
    val dark = ArrayList<Offset>(40)
    val len = y1 - y0
    for (k in 0 until 14) {
        val ph = wrap(t * (0.5f + 0.35f * hash01(k, seed)) * boost + hash01(k, seed + 1), 1f)
        val yy = y0 + ph * len
        val w = mix(w0, w1, ph)
        val off = (hash01(k, seed + 2) - 0.5f) * w * 0.8f
        val l = (0.04f + 0.05f * hash01(k, seed + 3)).coerceAtMost(y1 - yy)
        val xx = cx + off * u
        dashes.add(Offset(xx, yy * u))
        dashes.add(Offset(xx + 0.001f * u, (yy + l) * u))
        dark.add(Offset(xx + 0.004f * u, (yy - 0.01f) * u))
        dark.add(Offset(xx + 0.005f * u, (yy + l * 0.7f) * u))
    }
    drawPoints(dark, PointMode.Lines, BgC.iceShade.bg(pen, 0.5f, 0.2f), strokeWidth = 0.004f * u, cap = StrokeCap.Round, alpha = 0.35f)
    drawPoints(dashes, PointMode.Lines, Color.White, strokeWidth = 0.0045f * u, cap = StrokeCap.Round, alpha = 0.95f)
    // Mist where it lands.
    val mist = Color(0xFFF4FAFF).atNight(n, 0.6f)
    for (k in 0 until 6) {
        val ph = wrap(t * 0.28f + k / 6f, 1f)
        val px = cx + sin(ph * 4f + k * 1.7f) * 0.03f * u + (k - 2.5f) * 0.012f * u
        drawCircle(mist, (0.016f + ph * 0.04f) * u * boost, Offset(px, (y1 - ph * 0.045f) * u), alpha = 0.5f * (1f - ph))
    }
    drawOval(Color(0xFFDFF3FF).atNight(n, 0.5f), Offset(cx - 0.05f * u, (y1 - 0.004f) * u), Size(0.1f * u, 0.016f * u), alpha = 0.7f)
}

// ---------------------------------------------------------------------------------------------- life on the mountain

/** Banners of blown snow streaming from the two highest summits. */
private fun DrawScope.bgPlumes(st: Stage, pen: Pen, geo: BgGeo) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    val col = lerp(Color.White, Color(0xFFDDE6FA), n)
    val wind = 1f + 0.5f * overcast(pen)
    for (idx in intArrayOf(5, 2, 8)) {
        val ax = geo.main.ax[idx]
        val ay = geo.main.ay[idx]
        if (!st.sees(ax - 0.3f, ax + 1.2f)) continue
        for (k in 0 until 7) {
            val s = k / 6f
            val x = (ax + 0.04f + s * 0.55f * wind) * u - st.cam * u
            val y = (ay + 0.01f + s * 0.05f + sin(t * 0.7f + k * 0.9f) * 0.012f * (0.4f + s)) * u
            val w = (0.07f + 0.1f * sin(s * 3.1416f) + 0.03f * hash01(k, 601 + idx)) * u
            val h = (0.012f + 0.02f * s) * u
            drawOval(col, Offset(x - w * 0.5f, y - h * 0.5f), Size(w, h), alpha = 0.7f * (1f - s * 0.85f))
        }
    }
}

/** A loose flock of tiny far-off birds drifting across the sky, to say how far away the sky is. */
private fun DrawScope.bgFlock(st: Stage, pen: Pen) {
    val vis = 1f - ramp(pen.night * 1.6f)
    if (vis <= 0.01f) return
    val u = st.u
    val t = pen.t
    val span = st.w + 0.8f * u
    val lead = wrap(t * 0.012f * u + 0.35f * span - st.cam * u * 0.2f, span) - 0.4f * u
    val stroke = Stroke(width = pen.lw * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    for (i in 0 until 7) {
        val row = (i + 1) / 2
        val side = if (i % 2 == 0) 1f else -1f
        val x = lead - row * 0.03f * u
        val y = (0.05f + side * row * 0.012f) * u + sin(t * 0.5f + i) * 0.003f * u
        val flap = sin(t * 5f + i * 1.3f).let { if (it > 0f) it else it * 0.3f }
        val s = 0.007f * u
        val p = Path().apply {
            moveTo(x - s, y - s * 0.2f * flap)
            quadraticTo(x - s * 0.4f, y - s * (0.5f + 0.5f * flap), x, y)
            quadraticTo(x + s * 0.4f, y - s * (0.5f + 0.5f * flap), x + s, y - s * 0.2f * flap)
        }
        drawPath(p, Color(0xFF3A3550).atNight(pen.night, 0.5f), alpha = 0.7f * vis, style = stroke)
    }
}

/** A paraglider with a striped canopy circling in front of the great wall, tiny against it. */
private fun DrawScope.bgParaglider(st: Stage, pen: Pen) {
    val vis = 1f - ramp(pen.night * 1.5f)
    if (vis <= 0.01f) return
    val u = st.u
    val t = pen.t
    val cx = st.px(5.0f + sin(t * 0.04f) * 1.6f, 0.9f)
    val cy = (0.2f + 0.035f * sin(t * 0.11f) + 0.02f * sin(t * 0.37f)) * u
    if (cx < -0.2f * u || cx > st.w + 0.2f * u) return
    val dir = if (cos(t * 0.04f) >= 0f) 1f else -1f
    val tilt = -6f * dir + sin(t * 0.7f) * 2f
    val ws = 0.036f * u
    rotate(tilt, Offset(cx, cy)) {
        // The canopy: a curved wing of coloured cells seen from below, with lines down to the pilot.
        val cols = intArrayOf(0xFFE8473F.toInt(), 0xFFFFC83D.toInt(), 0xFFFFFFFF.toInt(), 0xFF4F8BFF.toInt(), 0xFFFFC83D.toInt(), 0xFFE8473F.toInt())
        val n = cols.size
        for (k in 0 until n) {
            val a = -1f + 2f * k / n
            val b = -1f + 2f * (k + 1) / n
            val arch = { v: Float -> -(1f - v * v) * 0.45f * ws }
            val cell = Path().apply {
                moveTo(cx + a * ws, cy + arch(a))
                lineTo(cx + b * ws, cy + arch(b))
                lineTo(cx + b * ws, cy + arch(b) + 0.0075f * u)
                lineTo(cx + a * ws, cy + arch(a) + 0.0075f * u)
                close()
            }
            drawPath(cell, Color(cols[k]).atNight(pen.night, 0.5f), alpha = vis)
            drawPath(cell, Ink.line, alpha = 0.55f * vis, style = pen.thin)
        }
        val body = Offset(cx, cy + 0.075f * u)
        for (k in intArrayOf(-1, 0, 1)) {
            drawLine(Ink.line, Offset(cx + k * ws * 0.9f, cy + 0.0075f * u), body, strokeWidth = pen.lw * 0.45f, alpha = 0.5f * vis)
        }
        drawCircle(Color(0xFF3A3550), 0.005f * u, Offset(body.x, body.y - 0.004f * u), alpha = vis)
        drawLine(Color(0xFF3A3550), Offset(body.x, body.y), Offset(body.x + 0.002f * u * dir, body.y + 0.012f * u), strokeWidth = 0.004f * u, cap = StrokeCap.Round, alpha = vis)
    }
}

/** A tiny hut high on the slope with a zigzag path climbing to it from the meadow, for scale. */
private fun DrawScope.bgFarHut(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    if (!st.sees(0.8f, 2.2f)) return
    translate(-st.cam * u, 0f) {
        val trail = Path().apply {
            moveTo(1.05f * u, 0.64f * u)
            lineTo(1.5f * u, 0.612f * u)
            lineTo(1.22f * u, 0.575f * u)
            lineTo(1.62f * u, 0.545f * u)
            lineTo(1.35f * u, 0.508f * u)
            lineTo(1.66f * u, 0.478f * u)
        }
        drawPath(trail, Color(0xFFF7E8D0).atNight(n, 0.6f), alpha = 0.85f, style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.012f * u, 0.008f * u))))
        val s = 0.026f * u
        drawHouse3d(1.7f * u, 0.478f * u, s, 0.5f * s, 0.42f * s, 0.8f * s, Color(0xFFB8503E), Color(0xFFF2F5FB), pen, n, outline = false, sideWindows = 0)
        // A flag on its roof.
        val fx = 1.7f * u
        val fy = (0.478f - 0.019f) * u
        drawLine(Ink.line, Offset(fx, fy), Offset(fx, fy - 0.014f * u), strokeWidth = pen.lw * 0.7f)
        drawPath(Path().apply {
            moveTo(fx, fy - 0.014f * u)
            lineTo(fx + 0.01f * u, fy - 0.0115f * u + sin(pen.t * 5f) * 0.001f * u)
            lineTo(fx, fy - 0.009f * u)
            close()
        }, Color(0xFFE8473F))
    }
}

// ---------------------------------------------------------------------------------------------- eagles

private fun bgEaglePath(c: Offset, s: Float, dir: Float, flap: Float): Path = Path().apply {
    fun px(v: Float) = c.x + v * s * dir
    fun py(v: Float) = c.y + v * s
    val up = flap * 0.1f
    moveTo(px(0.2f), py(-0.03f))
    lineTo(px(0.11f), py(-0.075f))
    quadraticTo(px(0.05f), py(-0.1f), px(0f), py(-0.07f))
    quadraticTo(px(0.45f), py(-0.2f - up), px(1f), py(-0.12f - up * 2f))
    lineTo(px(0.96f), py(-0.04f - up * 1.6f))
    lineTo(px(0.87f), py(-0.07f - up * 1.3f))
    lineTo(px(0.84f), py(0.0f - up * 1.1f))
    lineTo(px(0.73f), py(-0.03f - up))
    lineTo(px(0.68f), py(0.04f - up * 0.7f))
    lineTo(px(0.5f), py(0.02f - up * 0.4f))
    quadraticTo(px(0.3f), py(0.05f), px(0.12f), py(0.08f))
    lineTo(px(0.1f), py(0.27f))
    lineTo(px(0f), py(0.22f))
    lineTo(px(-0.1f), py(0.27f))
    lineTo(px(-0.12f), py(0.08f))
    quadraticTo(px(-0.3f), py(0.05f), px(-0.5f), py(0.02f - up * 0.4f))
    lineTo(px(-0.68f), py(0.04f - up * 0.7f))
    lineTo(px(-0.73f), py(-0.03f - up))
    lineTo(px(-0.84f), py(0.0f - up * 1.1f))
    lineTo(px(-0.87f), py(-0.07f - up * 1.3f))
    lineTo(px(-0.96f), py(-0.04f - up * 1.6f))
    lineTo(px(-1f), py(-0.12f - up * 2f))
    quadraticTo(px(-0.45f), py(-0.2f - up), px(0f), py(-0.07f))
    close()
}

/** Two golden eagles circling on the warm air in front of the great wall. */
private fun DrawScope.bgEagles(st: Stage, pen: Pen) {
    val vis = 1f - ramp(pen.night * 1.7f)
    if (vis <= 0.01f) return
    val u = st.u
    val t = pen.t
    for (i in 0 until 2) {
        val a = t * (0.22f + 0.05f * i) + i * 2.6f
        val cx = st.px(if (i == 0) 3.9f else 6.9f, 0.8f)
        val cy = (if (i == 0) 0.14f else 0.2f) * u
        val rx = (0.42f - 0.08f * i) * u
        val x = cx + cos(a) * rx
        val y = cy + sin(a) * rx * 0.18f
        val dir = if (-sin(a) >= 0f) 1f else -1f
        val s = u * (0.05f + 0.012f * sin(a)) * (1f - 0.18f * i)
        if (x < -s * 2f || x > st.w + s * 2f) continue
        val flap = sin(t * 1.6f + i * 3f) * 0.4f + 0.2f
        val p = bgEaglePath(Offset(x, y), s, dir, flap)
        drawPath(p, Color(0xFF4B3A3A).bg(pen, 0.3f, 0.1f), alpha = vis)
        drawPath(p, Ink.line, alpha = 0.5f * vis, style = pen.thin)
        val neck = Offset(x + dir * s * 0.1f, y - s * 0.07f)
        drawCircle(Color(0xFFF6EBD4).bg(pen, 0.4f, 0.1f), s * 0.05f, neck, alpha = vis)
    }
}

// ---------------------------------------------------------------------------------------------- the cable line

/** Where the cable of the cable car runs: from the bottom station wheel to the top station wheel. */
private const val CABLE_X0 = 3.15f
private const val CABLE_Y0 = 0.54f
private const val CABLE_X1 = 6.3f
private const val CABLE_Y1 = 0.25f

private fun DrawScope.bgPylon(st: Stage, pen: Pen, x: Float, tower: Color, steel: Color) {
    val u = st.u
    val cy = CABLE_Y0 + (CABLE_Y1 - CABLE_Y0) * (x - CABLE_X0) / (CABLE_X1 - CABLE_X0)
    val topY = cy + 0.012f
    val baseY = 0.79f
    val h = baseY - topY
    val cx = st.x(x)
    val l0 = Offset(cx - 0.03f * u, baseY * u)
    val l1 = Offset(cx - 0.01f * u, topY * u)
    val r0 = Offset(cx + 0.03f * u, baseY * u)
    val r1 = Offset(cx + 0.01f * u, topY * u)
    val w = pen.lw * 1.5f
    drawLine(tower, l0, l1, strokeWidth = w, cap = StrokeCap.Round)
    drawLine(tower, r0, r1, strokeWidth = w, cap = StrokeCap.Round)
    val rungs = 7
    for (k in 0 until rungs) {
        val a = k / rungs.toFloat()
        val b = (k + 1) / rungs.toFloat()
        val ya = baseY - h * a
        val yb = baseY - h * b
        val xa = 0.03f - 0.02f * a
        val xb = 0.03f - 0.02f * b
        val sgn = if (k % 2 == 0) 1f else -1f
        drawLine(tower, Offset(cx - xa * u * sgn, ya * u), Offset(cx + xb * u * sgn, yb * u), strokeWidth = w * 0.6f)
        drawLine(tower, Offset(cx - xb * u, yb * u), Offset(cx + xb * u, yb * u), strokeWidth = w * 0.6f)
    }
    // The cross-arm with the two wheels the cables run over.
    drawLine(steel, Offset(cx - 0.032f * u, topY * u), Offset(cx + 0.06f * u, (topY - 0.012f) * u), strokeWidth = w * 1.4f, cap = StrokeCap.Round)
    drawCircle(steel, 0.0075f * u, Offset(cx, (topY - 0.004f) * u))
    drawCircle(steel, 0.0075f * u, Offset(cx + 0.03f * u, (topY - 0.012f) * u))
    drawCircle(Ink.line, 0.0075f * u, Offset(cx, (topY - 0.004f) * u), style = pen.thin)
    drawCircle(Ink.line, 0.0075f * u, Offset(cx + 0.03f * u, (topY - 0.012f) * u), style = pen.thin)
}

private fun DrawScope.bgCable(st: Stage, pen: Pen) {
    if (!st.sees(CABLE_X0 - 0.2f, CABLE_X1 + 0.2f)) return
    val u = st.u
    val steel = BgC.steel.bg(pen, 0.45f, 0.1f)
    val tower = Color(0xFF4A5170).bg(pen, 0.45f, 0.1f)
    bgPylon(st, pen, 4.25f, tower, steel)
    bgPylon(st, pen, 5.3f, tower, steel)
    val a = st.o(CABLE_X0, CABLE_Y0)
    val b = st.o(CABLE_X1, CABLE_Y1)
    // The loaded cable runs straight; the return strand sags a little behind it.
    val dx = 0.018f * u
    val dy = -0.013f * u
    val sag = Path().apply {
        moveTo(a.x + dx, a.y + dy)
        quadraticTo((a.x + b.x) / 2f + dx, (a.y + b.y) / 2f + dy + 0.03f * u, b.x + dx, b.y + dy)
    }
    drawPath(sag, steel, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    drawLine(steel, a, b, strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(a.x, a.y - pen.lw), Offset(b.x, b.y - pen.lw), strokeWidth = pen.lw * 0.5f, alpha = 0.35f)
    // Red and white marker balls on the return strand.
    for (k in 0 until 3) {
        val t = 0.28f + k * 0.2f
        val px = (1f - t) * (1f - t) * (a.x + dx) + 2f * (1f - t) * t * ((a.x + b.x) / 2f + dx) + t * t * (b.x + dx)
        val py = (1f - t) * (1f - t) * (a.y + dy) + 2f * (1f - t) * t * ((a.y + b.y) / 2f + dy + 0.03f * u) + t * t * (b.y + dy)
        drawCircle(if (k % 2 == 0) BgC.snowLit else Color(0xFFE8473F), 0.007f * u, Offset(px, py + 0.007f * u))
        drawCircle(Ink.line, 0.007f * u, Offset(px, py + 0.007f * u), style = pen.thin)
    }
}

// ---------------------------------------------------------------------------------------------- foot, meadow and path

private fun DrawScope.bgGroundAndFoot(st: Stage, pen: Pen, geo: BgGeo) {
    val u = st.u
    val n = pen.night
    val snowy = pen.weather == Weather.SNOW
    val first = max(0, floor(st.cam / BG_CHUNK).toInt() - 1)
    val last = min(BG_CHUNKS - 1, floor((st.cam + st.vw) / BG_CHUNK).toInt() + 1)
    // The forested foothills, then the falls running down through their notches, then the pine belt.
    inScene(st) {
        drawPath(geo.hills, BgC.hill.bg(pen, 0.55f, 0.3f))
        for (c in first..last) {
            drawPath(geo.hillPines[c], BgC.hillPine.bg(pen, 0.55f, 0.3f))
            drawPath(geo.hillPinesShade[c], BgC.hillPineShade.bg(pen, 0.55f, 0.3f))
        }
    }
    bgFalls(st, pen, geo)
    inScene(st) {
        drawPath(geo.treeline, BgC.pine.bg(pen, 0.55f, 0.2f))
    }
    inScene(st) {
        for (c in first..last) {
            drawPath(geo.pines[c], BgC.pineLit.bg(pen, 0.55f, 0.2f))
            drawPath(geo.pinesShade[c], BgC.pineShade.bg(pen, 0.55f, 0.2f))
        }
        bgChaletRow(st, pen)
        val lit = lerp(BgC.meadowLit, Color(0xFFE9F0F6), if (snowy) 0.55f else 0f).bg(pen, 0.5f, 0.2f)
        val low = lerp(BgC.meadowLow, Color(0xFFD5E0EC), if (snowy) 0.5f else 0f).bg(pen, 0.5f, 0.2f)
        drawPath(geo.meadow, Brush.verticalGradient(0f to lit, 1f to low, startY = 0.74f * u, endY = 0.97f * u))
        drawPath(geo.brook, BgC.water.bg(pen, 0.4f, 0.2f))
        drawPath(geo.pathFill, BgC.path.bg(pen, 0.5f, 0.2f))
        drawPath(geo.pathEdgeA, BgC.pathDark.bg(pen, 0.5f, 0.2f), alpha = 0.7f, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
        drawPath(geo.pathEdgeB, BgC.pathDark.bg(pen, 0.5f, 0.2f), alpha = 0.7f, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
        val stone = BgC.stone.bg(pen, 0.5f, 0.2f)
        val stoneShade = BgC.stoneShade.bg(pen, 0.5f, 0.2f)
        val pebble = BgC.pathDark.bg(pen, 0.5f, 0.2f)
        val grass = BgC.grass.bg(pen, 0.5f, 0.2f)
        for (c in first..last) {
            drawPath(geo.stones[c], stone)
            drawPath(geo.stonesShade[c], stoneShade, alpha = 0.6f)
            drawPath(geo.pebbles[c], pebble)
            drawPath(geo.tufts[c], grass)
            for (k in 0 until 12) {
                val pts = geo.flowerPts[c][k]
                if (pts.isEmpty()) continue
                val big = k % 2 == 1
                drawPoints(pts, PointMode.Points, BG_FLOWERS[k / 2].bg(pen, 0.45f, 0.1f), strokeWidth = if (big) 0.011f * u else 0.007f * u, cap = StrokeCap.Round)
            }
            drawPoints(geo.flowerHearts[c], PointMode.Points, Color(0xFFFFE680).bg(pen, 0.45f, 0.1f), strokeWidth = 0.0035f * u, cap = StrokeCap.Round)
        }
        drawPath(geo.boulders, stone)
        drawPath(geo.bouldersShade, stoneShade)
        drawPath(geo.bouldersTop, BgC.grass.bg(pen, 0.5f, 0.2f))
        drawPath(geo.boulders, Ink.line, style = pen.thin)
        // Sparkles on the brook.
        val sparks = ArrayList<Offset>(10)
        for (k in 0 until 10) {
            val a = max(0f, sin(pen.t * 1.7f + k * 2.3f))
            if (a < 0.6f) continue
            val bx = if (k < 5) geo.fall1 - 0.05f + 2.2f * hash01(k, 411) else geo.fall2 - 0.1f + 1.9f * hash01(k, 412)
            sparks.add(Offset(bx * u, (0.745f + 0.004f * sin(bx * 7f)) * u))
        }
        drawPoints(sparks, PointMode.Points, Color.White, strokeWidth = 0.004f * u, cap = StrokeCap.Round, alpha = 0.8f * (1f - n))
    }
    // The warm light lying on the ground.
    drawRect(
        Brush.verticalGradient(listOf(Color(0x33FFC98A), Color(0x00FFC98A)), startY = 0.74f * u, endY = 0.9f * u),
        Offset(0f, 0.74f * u), Size(st.w, 0.16f * u), alpha = (1f - n) * (1f - overcast(pen)),
    )
    drawBase(st, pen, BgC.grass.bg(pen, 0.5f, 0.2f), Color(0xFF7A6A5A).atNight(n, 0.5f))
    drawBaseStones(st, pen, Color(0xFF9A8F86).atNight(n, 0.5f), 29)
}

/** Three small chalets on the meadow far off, with smoke and, at night, lit windows. */
private fun DrawScope.bgChaletRow(st: Stage, pen: Pen) {
    val u = st.u
    val xs = floatArrayOf(1.95f, 5.6f, 8.3f)
    val walls = intArrayOf(0xFFB8503E.toInt(), 0xFF9A6A48.toInt(), 0xFFC89B62.toInt())
    for (i in xs.indices) {
        if (!st.sees(xs[i] - 0.2f, xs[i] + 0.3f)) continue
        val s = 0.05f * u
        val by = (bgMeadowY(xs[i]) + 0.012f) * u
        // drawHouse3d draws in pixels; the scene is already translated, so hand it scene pixels.
        drawHouse3d(xs[i] * u, by, s, 0.5f * s, 0.42f * s, 0.9f * s, Color(walls[i]), Color(0xFF55607A), pen, pen.night, outline = false, sideWindows = 1, chimney = true, t = pen.t)
    }
}

/** Soft shafts of the late sun slanting down across the valley. */
private fun DrawScope.bgRays(st: Stage, pen: Pen) {
    val vis = (1f - pen.night) * (1f - overcast(pen))
    if (vis <= 0.02f) return
    val u = st.u
    val t = pen.t
    for (k in 0 until 4) {
        val x0 = (0.1f + 0.55f * k) * u - st.cam * u * 0.04f
        val a = (0.045f + 0.015f * sin(t * 0.3f + k * 1.9f)) * vis
        val p = Path().apply {
            moveTo(x0, -0.3f * u)
            lineTo(x0 + 0.1f * u, -0.3f * u)
            lineTo(x0 + 1.05f * u + 0.12f * u * k * 0.3f, 0.9f * u)
            lineTo(x0 + 0.72f * u, 0.9f * u)
            close()
        }
        drawPath(p, Color(0xFFFFE6A8), alpha = a * 1.6f, blendMode = BlendMode.Screen)
    }
}

// ---------------------------------------------------------------------------------------------- the front

private fun DrawScope.bgFront(st: Stage, pen: Pen) {
    val geo = bgGeo.of(st.u)
    val u = st.u
    val t = pen.t
    val n = pen.night
    val first = max(0, floor(st.cam / BG_CHUNK).toInt() - 1)
    val last = min(BG_CHUNKS - 1, floor((st.cam + st.vw) / BG_CHUNK).toInt() + 1)
    val snowy = pen.weather == Weather.SNOW
    val grassA = lerp(BgC.grass, Color(0xFFDDE8F2), if (snowy) 0.5f else 0f)
    inScene(st) {
        for (c in first..last) {
            for (g in 0 until 3) {
                val sway = sin(t * (1.1f + 0.2f * g) + g * 2f + c) * 0.0028f * u
                translate(sway, 0f) {
                    val col = if (g == 1) grassA.lighten(0.12f) else grassA.darken(0.06f * g)
                    drawPath(geo.frontTufts[c][g], col.bg(pen, 0.5f, 0.2f))
                    drawPath(geo.frontTufts[c][g], Ink.line, style = pen.thin, alpha = 0.7f)
                }
            }
            val stems = geo.frontStems[c]
            if (stems.isNotEmpty()) drawPoints(stems, PointMode.Lines, BgC.grass.darken(0.1f).bg(pen, 0.5f, 0.2f), strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
            for (k in 0 until 6) {
                val pts = geo.frontHeads[c][k]
                if (pts.isEmpty()) continue
                drawPoints(pts, PointMode.Points, Ink.line, strokeWidth = 0.0175f * u, cap = StrokeCap.Round)
                drawPoints(pts, PointMode.Points, BG_FLOWERS[k].bg(pen, 0.45f, 0.1f), strokeWidth = 0.0135f * u, cap = StrokeCap.Round)
                drawPoints(pts, PointMode.Points, if (k == 1) Color(0xFFFF9A3D) else Color(0xFFFFE680), strokeWidth = 0.005f * u, cap = StrokeCap.Round)
            }
        }
    }
    // Butterflies by day, fireflies by night; none in rain or snow.
    val calm = 1f - overcast(pen)
    val day = (1f - n) * calm
    if (day > 0.05f) {
        val cols = intArrayOf(0xFFFFC83D.toInt(), 0xFF4F8BFF.toInt(), 0xFFFFFFFF.toInt())
        for (k in 0 until 3) {
            val bx = st.px(1.6f + k * 2.9f + sin(t * 0.21f + k * 1.7f) * 1.1f, 1.0f)
            val by = (0.9f + 0.035f * sin(t * 0.8f + k * 2f) + 0.01f * sin(t * 2.3f + k)) * u
            if (bx < -0.05f * u || bx > st.w + 0.05f * u) continue
            val flap = abs(cos(t * 13f + k * 2f))
            val s = 0.011f * u
            val wing = Path().apply {
                moveTo(bx, by)
                cubicTo(bx - s * 1.4f, by - s * (1.4f * flap + 0.1f), bx - s * 1.6f, by + s * 0.2f, bx, by + s * 0.3f)
                cubicTo(bx + s * 1.6f, by + s * 0.2f, bx + s * 1.4f, by - s * (1.4f * flap + 0.1f), bx, by)
                close()
            }
            drawPath(wing, Color(cols[k]), alpha = day)
            drawPath(wing, Ink.line, alpha = 0.7f * day, style = pen.thin)
            drawLine(Ink.line, Offset(bx, by - s * 0.2f), Offset(bx, by + s * 0.35f), strokeWidth = pen.lw, alpha = day, cap = StrokeCap.Round)
        }
    }
    if (n > 0.3f && calm > 0.3f) {
        val a = ramp((n - 0.3f) / 0.5f) * calm
        for (k in 0 until 9) {
            val fx = st.px(0.5f + k * 1.0f + sin(t * 0.3f + k * 2.1f) * 0.6f, 1.0f)
            val fy = (0.83f + 0.12f * hash01(k, 611) + 0.025f * sin(t * 0.7f + k * 1.3f)) * u
            if (fx < -0.05f * u || fx > st.w + 0.05f * u) continue
            val g = 0.5f + 0.5f * sin(t * 1.6f + k * 2.7f)
            drawCircle(
                safeRadialGradient(listOf(Color(0xFFE8FF9A).copy(alpha = 0.8f * g * a), Color(0xFFE8FF9A).copy(alpha = 0f)), center = Offset(fx, fy), radius = 0.022f * u),
                0.022f * u, Offset(fx, fy), blendMode = BlendMode.Screen,
            )
            drawCircle(Color(0xFFFFFFD0), 0.0025f * u, Offset(fx, fy), alpha = g * a)
        }
    }
}
