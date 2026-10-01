package app.trollfoss.ui.art

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
import app.trollfoss.domain.GardenLayout
import app.trollfoss.domain.GardenRules
import kotlin.math.cos
import kotlin.math.sin

/*
 * Hagen's play corner and pond things in oblique 3D: the big apple tree with the treehouse, its rope ladder
 * and tyre swing, the zip line's carriage and pole, the trampoline, the sandbox, the hammock, the footbridge
 * and the frogs on their lily pads. Origin at the bottom centre of each fixture's front face. Helpers `gy`.
 */

private fun DrawScope.gyShadow(u: Float, w: Float, d: Float, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

private val GY_LEAF = Color(0xFF7CC24E)
private val GY_LEAF_DARK = Color(0xFF3F9446)
private val GY_APPLE = Color(0xFFE8473F)

// ------------------------------------------------------------------------------------------------ treehouse

/**
 * The big apple tree with a treehouse: a thick trunk, a deck with a rail and a mast with bunting, a cabin whose
 * doors open on a cosy room (a bed, a rug, a lantern, cushions to sit on), a canopy full of apples and a branch
 * that holds the tyre swing. The windows and the lantern glow at night.
 */
internal fun DrawScope.gaTreehouse(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val shake = f.anim * sin(f.anim * 24f) * 0.012f * u
    val wood = FxC.oak
    val bark = Color(0xFF8A5E3C)
    gyShadow(u, 0.9f, 0.2f, 0.8f)

    // The back of the canopy: dark clusters behind the cabin.
    translate(shake * 0.6f, 0f) {
        fxCloud(
            GY_LEAF_DARK, pen, true,
            q(-0.3f, -0.62f, 0.2f).x, q(-0.3f, -0.62f, 0.2f).y, 0.17f * u, q(-0.05f, -0.74f, 0.2f).x, q(-0.05f, -0.74f, 0.2f).y, 0.2f * u,
            q(0.22f, -0.72f, 0.2f).x, q(0.22f, -0.72f, 0.2f).y, 0.19f * u, q(0.4f, -0.6f, 0.2f).x, q(0.4f, -0.6f, 0.2f).y, 0.15f * u,
        )
    }
    // The trunk, widening into roots; a branch to the right for the swing, one to the left under the canopy.
    val base = q(0f, 0f, 0.1f)
    val trunk = Path().apply {
        moveTo(base.x - 0.12f * u, base.y + 0.004f * u)
        quadraticTo(base.x - 0.06f * u, base.y - 0.02f * u, base.x - 0.05f * u, base.y - 0.16f * u)
        lineTo(base.x - 0.042f * u, base.y - 0.6f * u)
        lineTo(base.x + 0.042f * u, base.y - 0.6f * u)
        lineTo(base.x + 0.056f * u, base.y - 0.16f * u)
        quadraticTo(base.x + 0.07f * u, base.y - 0.02f * u, base.x + 0.12f * u, base.y + 0.004f * u)
        close()
    }
    drawPath(trunk, Brush.horizontalGradient(0f to bark.lighten(0.14f), 0.55f to bark, 1f to bark.darken(0.28f), startX = base.x - 0.1f * u, endX = base.x + 0.1f * u))
    drawPath(trunk, Ink.line, style = pen.stroke)
    // Bark lines and a knot hole.
    for (k in 0 until 7) {
        val x = base.x + (k - 3f) * 0.014f * u
        drawLine(bark.darken(0.4f), Offset(x, base.y - 0.02f * u - k * 0.03f * u), Offset(x + 0.004f * u, base.y - 0.08f * u - k * 0.03f * u), strokeWidth = pen.lw * 0.7f, alpha = 0.6f, cap = StrokeCap.Round)
    }
    inkedOval(Rect(base.x - 0.013f * u, base.y - 0.24f * u, base.x + 0.013f * u, base.y - 0.2f * u), Color(0xFF2A1E26), pen, shade = false)
    capsule(q(0.03f, -0.5f, 0.1f), q(0.43f, -0.585f, 0.12f), 0.018f * u, bark, pen)
    capsule(q(-0.02f, -0.52f, 0.1f), q(-0.3f, -0.6f, 0.1f), 0.013f * u, bark, pen)

    // The deck: planks with a rail on the left half; two braces carry it from the trunk.
    val deckT = GardenLayout.DECK_DY
    capsule(q(-0.04f, -0.2f, 0.1f), q(-0.3f, deckT + 0.03f, 0.08f), 0.011f * u, wood.darken(0.2f), pen)
    capsule(q(0.04f, -0.2f, 0.1f), q(0.3f, deckT + 0.03f, 0.08f), 0.011f * u, wood.darken(0.2f), pen)
    fxBox(u, GardenLayout.DECK_X0, deckT, GardenLayout.DECK_X1, deckT + 0.03f, 0.22f, wood, pen, rad = 0.003f, z = 0.0f, top = wood.lighten(0.15f), side = wood.darken(0.25f))
    for (k in 1..9) {
        val x = GardenLayout.DECK_X0 + (GardenLayout.DECK_X1 - GardenLayout.DECK_X0) * k / 10f
        fxLine(q(x, deckT, 0f), q(x, deckT, 0.22f), wood.darken(0.35f), pen.lw * 0.5f)
    }
    for (k in 1..2) fxLine(q(GardenLayout.DECK_X0, deckT, 0.22f * k / 3f), q(GardenLayout.DECK_X1, deckT, 0.22f * k / 3f), wood.darken(0.2f), pen.lw * 0.4f)
    // The rail on the left: posts and a top rail, open where the rope ladder comes up.
    for (px in floatArrayOf(-0.48f, -0.28f, -0.14f)) {
        fxBox(u, px - 0.009f, deckT - 0.1f, px + 0.009f, deckT, 0.018f, wood, pen, rad = 0.002f, z = 0.015f)
    }
    fxBox(u, -0.49f, deckT - 0.105f, -0.27f, deckT - 0.092f, 0.02f, wood.lighten(0.1f), pen, rad = 0.002f, z = 0.014f)
    fxBox(u, -0.16f, deckT - 0.105f, -0.13f, deckT - 0.092f, 0.02f, wood.lighten(0.1f), pen, rad = 0.002f, z = 0.014f)
    for (bx in floatArrayOf(-0.44f, -0.4f, -0.36f, -0.32f)) {
        val a = q(bx, deckT - 0.09f, 0.025f)
        drawLine(Ink.line, a, Offset(a.x, a.y + 0.09f * u), strokeWidth = pen.lw * 0.9f)
    }
    // The mast with the zip line's pulley at the top, a pennant, and bunting down to the cabin roof.
    val mastBase = q(-0.48f, deckT, 0.04f)
    val mastTop = q(-0.48f, -0.72f, 0.04f)
    fxCyl(mastBase.x, mastBase.y, mastTop.y, 0.009f * u, 0.0075f * u, wood.darken(0.1f), pen, top = wood.lighten(0.15f))
    inkedCircle(Offset(mastTop.x, mastTop.y - 0.002f * u), 0.0085f * u, FxC.steel, pen, shade = false)
    val wave = sin(t * 4f) * 0.005f * u
    val flagPole = Offset(mastTop.x, mastTop.y - 0.02f * u)
    fxLine(flagPole, Offset(flagPole.x, flagPole.y - 0.03f * u), Ink.line, pen.lw)
    inked(Path().apply {
        moveTo(flagPole.x, flagPole.y - 0.03f * u)
        quadraticTo(flagPole.x - 0.025f * u, flagPole.y - 0.034f * u + wave, flagPole.x - 0.05f * u, flagPole.y - 0.026f * u)
        quadraticTo(flagPole.x - 0.025f * u, flagPole.y - 0.018f * u - wave, flagPole.x, flagPole.y - 0.01f * u)
        close()
    }, FxC.yellow, pen, shade = false)
    val buntA = q(-0.48f, -0.7f, 0.04f)
    val buntB = q(0.12f, -0.62f, 0.02f)
    val bunt = Path().apply {
        moveTo(buntA.x, buntA.y)
        quadraticTo((buntA.x + buntB.x) / 2f, (buntA.y + buntB.y) / 2f + 0.07f * u, buntB.x, buntB.y)
    }
    drawPath(bunt, Ink.line, alpha = 0.8f, style = Stroke(pen.lw * 0.6f))
    for (k in 1..8) {
        val f0 = k / 9f
        val px = buntA.x + (buntB.x - buntA.x) * f0
        val py = buntA.y + (buntB.y - buntA.y) * f0 + 4f * 0.035f * u * f0 * (1f - f0)
        val col = arrayOf(FxC.red, FxC.yellow, FxC.fjord, FxC.green, FxC.pink)[k % 5]
        inked(fxPoly(1f, px - 0.011f * u, py, px + 0.011f * u, py, px + sin(t * 3f + k) * 0.002f * u, py + 0.026f * u), col, pen, shade = false)
    }

    // The cabin: boards, a roof, a window with a flower box, and two doors that open.
    val cd = 0.17f
    val cl = 0.0f
    val cr = 0.34f
    val cb = deckT
    val ct = -0.53f
    fxBox(u, cl, ct, cr, cb, cd, Color(0xFFE3B27A), pen, rad = 0.003f, z = 0.02f, top = Color(0xFFE3B27A).lighten(0.1f), side = Color(0xFFE3B27A).darken(0.22f), front = false)
    val front = fxFront(u, cl, ct, cr, cb, 0.02f)
    inkedRound(front, 0.003f * u, Color(0xFFE3B27A), pen, shade = false)
    for (k in 1..16) fxLine(Offset(front.left + front.width * k / 17f, front.top + 0.004f * u), Offset(front.left + front.width * k / 17f, front.bottom - 0.004f * u), Color(0xFFB98A55), pen.lw * 0.5f)
    val s0 = q(cr, ct, 0.02f)
    val s1 = q(cr, ct, 0.02f + cd)
    val sb0 = q(cr, cb, 0.02f)
    val sb1 = q(cr, cb, 0.02f + cd)
    for (k in 1..5) fxLine(fxMix(s0, s1, k / 6f), fxMix(sb0, sb1, k / 6f), Color(0xFFB98A55), pen.lw * 0.5f)
    // The side window glows at night.
    val wa = q(cr, ct + 0.04f, 0.07f)
    val wb = q(cr, ct + 0.04f, 0.15f)
    val wc = q(cr, ct + 0.11f, 0.15f)
    val wd = q(cr, ct + 0.11f, 0.07f)
    drawPath(fxPath(wa, wb, wc, wd), lerp(GaK.glass, Color(0xFFFFD66B), pen.night))
    drawPath(fxPath(wa, wb, wc, wd), FxC.paint, style = Stroke(pen.lw * 1.3f))
    // Doors: closed, two red leaves with a porthole each; open, a room.
    val dl = 0.03f
    val dr = 0.31f
    val dt = ct + 0.014f
    val db = cb - 0.012f
    if (!f.open) {
        for (s in 0..1) {
            val l = if (s == 0) dl else (dl + dr) / 2f + 0.002f
            val r = if (s == 0) (dl + dr) / 2f - 0.002f else dr
            val leaf = Rect(l * u + q(0f, 0f, 0.02f).x, dt * u + q(0f, 0f, 0.02f).y, r * u + q(0f, 0f, 0.02f).x, db * u + q(0f, 0f, 0.02f).y)
            inkedRound(leaf, 0.003f * u, FxC.falun, pen, shade = false)
            for (k in 1..3) fxLine(Offset(leaf.left + leaf.width * k / 4f, leaf.top + 0.03f * u), Offset(leaf.left + leaf.width * k / 4f, leaf.bottom - 0.006f * u), FxC.falun.darken(0.25f), pen.lw * 0.5f)
            val pc = Offset(leaf.center.x, leaf.top + 0.05f * u)
            drawCircle(Ink.line, 0.0165f * u, pc)
            drawCircle(lerp(GaK.glass, Color(0xFFFFD66B), pen.night), 0.0135f * u, pc)
            drawCircle(FxC.paint, 0.0165f * u, pc, style = Stroke(pen.lw * 1.2f))
            drawCircle(FxC.brass, 0.0045f * u, Offset(if (s == 0) leaf.right - 0.012f * u else leaf.left + 0.012f * u, leaf.center.y + 0.02f * u))
        }
        // A heart over the doors.
        val hc = q(0.17f, ct + 0.012f, 0.02f)
        drawPath(fxHeart(hc.x, hc.y + 0.018f * u, 0.009f * u), Color(0xFFFF8FB1))
        drawPath(fxHeart(hc.x, hc.y + 0.018f * u, 0.009f * u), Ink.line, style = pen.thin)
        if (pen.night > 0.2f || f.on) fxGlow(q(0.17f, -0.44f, 0.02f), 0.16f * u, FxC.warm, 0.25f + 0.4f * pen.night)
    } else {
        val ox = q(0f, 0f, 0.02f)
        translate(ox.x, ox.y) {
            fxHollow(u, dl, dt, dr, db, 0.14f, Color(0xFFE8C497), pen, back = Color(0xFFB98A55))
            clipRect(dl * u, dt * u, dr * u, db * u) {
                fun p(x: Float, y: Float, z: Float) = q(x, y, z)
                // A rug, a little bed, a shelf with a lantern and a book, fairy lights.
                fxFace(fxFlat(u, 0.08f, 0.26f, db - 0.002f, 0.02f, 0.11f, 0.006f), Color(0xFFD97B8A), pen)
                fxBox(u, 0.04f, db - 0.04f, 0.14f, db, 0.1f, FxC.dusty, pen, rad = 0.003f, z = 0.025f)
                val pillow = p(0.065f, db - 0.04f, 0.08f)
                inkedRound(Rect(pillow.x - 0.015f * u, pillow.y - 0.012f * u, pillow.x + 0.015f * u, pillow.y + 0.004f * u), 0.004f * u, FxC.paint, pen, shade = false)
                val sh = p(0.17f, ct + 0.07f, 0.12f)
                fxBox(u, 0.15f, ct + 0.075f, 0.3f, ct + 0.085f, 0.04f, wood, pen, z = 0.09f)
                fxGlow(Offset(sh.x + 0.03f * u, sh.y - 0.01f * u), 0.1f * u, FxC.warm, 0.35f + 0.45f * pen.night)
                inkedRound(Rect(sh.x + 0.018f * u, sh.y - 0.026f * u, sh.x + 0.038f * u, sh.y), 0.003f * u, Color(0xFFFFE9A8), pen, shade = false)
                inkedRound(Rect(sh.x + 0.07f * u, sh.y - 0.022f * u, sh.x + 0.084f * u, sh.y), 0.002f * u, FxC.fjord, pen, shade = false)
                for (k in 0 until 7) {
                    val c = p(dl + 0.02f + k * 0.04f, dt + 0.03f + sin(k / 6f * 3.1416f) * 0.012f, 0.13f)
                    val on = 0.5f + 0.5f * sin(t * 2f + k * 1.4f)
                    val col = arrayOf(FxC.yellow, FxC.pink, FxC.mint)[k % 3]
                    fxGlow(c, 0.025f * u, col, pen.night * (0.3f + 0.4f * on))
                    drawCircle(Ink.line, 0.004f * u, c)
                    drawCircle(col, 0.003f * u, c)
                }
                // Two round cushions on the floor, where the guests sit.
                for ((k, cx) in floatArrayOf(0.12f, 0.25f).withIndex()) {
                    val cc = p(cx, db, 0.07f)
                    inkedOval(Rect(cc.x - 0.03f * u, cc.y - 0.012f * u, cc.x + 0.03f * u, cc.y + 0.004f * u), if (k == 0) FxC.pink else FxC.mint, pen)
                }
            }
        }
        // The two leaves swung open against the walls, with their portholes.
        val ox2 = q(0f, 0f, 0.02f)
        translate(ox2.x, ox2.y) {
            fxOpenDoor(u, dl, dt, db, 0.14f, -1f, FxC.falun, FxC.falun.darken(0.12f), pen, 118f)
            fxOpenDoor(u, dr, dt, db, 0.14f, 1f, FxC.falun, FxC.falun.darken(0.12f), pen, 118f)
        }
    }
    // The roof: two slopes with a ridge, shingles and a short chimney.
    val ov = 0.03f
    val peak = -0.63f
    val rl = q(cl - ov, ct, 0.0f)
    val rlb = q(cl - ov, ct, cd + 0.04f)
    val rr = q(cr + ov, ct, 0.0f)
    val rrb = q(cr + ov, ct, cd + 0.04f)
    val rp = q((cl + cr) / 2f, peak, 0.0f)
    val rpb = q((cl + cr) / 2f, peak, cd + 0.04f)
    val roofC = Color(0xFF3A8A5A)
    val gableTri = Path().apply {
        val a = q(cl, ct, 0.02f)
        val b = q((cl + cr) / 2f, peak + 0.015f, 0.02f)
        val c = q(cr, ct, 0.02f)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        close()
    }
    drawPath(gableTri, Color(0xFFE3B27A))
    drawPath(gableTri, Ink.line, style = pen.stroke)
    val leftSlope = fxPath(rl, rlb, rpb, rp)
    val rightSlope = fxPath(rp, rpb, rrb, rr)
    drawPath(leftSlope, roofC.darken(0.1f))
    drawPath(rightSlope, roofC.lighten(0.06f))
    for (k in 1..4) {
        fxLine(fxMix(rl, rp, k / 5f), fxMix(rlb, rpb, k / 5f), roofC.darken(0.4f), pen.lw * 0.6f)
        fxLine(fxMix(rp, rr, k / 5f), fxMix(rpb, rrb, k / 5f), roofC.darken(0.35f), pen.lw * 0.6f)
    }
    drawPath(leftSlope, Ink.line, style = pen.stroke)
    drawPath(rightSlope, Ink.line, style = pen.stroke)
    val chim = q(0.27f, peak + 0.01f, 0.1f)
    fxBox(u, 0.255f, peak - 0.05f, 0.285f, peak + 0.02f, 0.03f, FxC.stone, pen, rad = 0.002f, z = 0.09f)
    fxPuffs(chim.x, chim.y - 0.06f * u, t, 0.011f * u, 0.12f * u, Color(0xFFD8DCE8), 0.45f, 4, 0.3f, 0.03f * u)

    // The front canopy: lighter clusters with apples and blossom, drifting a little in the wind.
    val sw = sin(t * 0.9f) * 0.004f * u + shake
    translate(sw, 0f) {
        fxCloud(
            GY_LEAF, pen, true,
            q(-0.36f, -0.66f, 0.05f).x, q(-0.36f, -0.66f, 0.05f).y, 0.12f * u, q(-0.2f, -0.78f, 0.05f).x, q(-0.2f, -0.78f, 0.05f).y, 0.14f * u,
            q(0.02f, -0.8f, 0.05f).x, q(0.02f, -0.8f, 0.05f).y, 0.12f * u, q(0.26f, -0.78f, 0.05f).x, q(0.26f, -0.78f, 0.05f).y, 0.14f * u,
            q(0.43f, -0.68f, 0.05f).x, q(0.43f, -0.68f, 0.05f).y, 0.12f * u, q(0.16f, -0.7f, 0.05f).x, q(0.16f, -0.7f, 0.05f).y, 0.09f * u,
        )
        // Apples and blossom.
        for (k in 0 until 7) {
            val ax = -0.36f + k * 0.12f + 0.03f * hash01(k, 881)
            val ay = -0.64f - 0.1f * hash01(k, 882) - (if (k % 2 == 0) 0.04f else 0f)
            val c = q(ax, ay, 0.05f)
            if (k % 3 == 2) {
                drawCircle(Color(0xFFFFE4EE), 0.0075f * u, c)
                drawCircle(FxC.yellow, 0.0028f * u, c)
            } else {
                inkedCircle(c, 0.0115f * u, GY_APPLE, pen, shade = true)
                shine(Offset(c.x - 0.004f * u, c.y - 0.004f * u), 0.005f * u, 0.003f * u, 0.8f)
                drawLine(Ink.line, Offset(c.x, c.y - 0.01f * u), Offset(c.x + 0.003f * u, c.y - 0.017f * u), strokeWidth = pen.lw * 0.7f)
            }
        }
        // A bird on the branch.
        val bx = q(-0.26f, -0.585f, 0.1f)
        inkedCircle(Offset(bx.x, bx.y - 0.012f * u), 0.0115f * u, Color(0xFFE8C46A), pen)
        inked(fxPoly(1f, bx.x + 0.01f * u, bx.y - 0.013f * u, bx.x + 0.022f * u, bx.y - 0.009f * u, bx.x + 0.01f * u, bx.y - 0.005f * u), FxC.terracotta, pen, shade = false)
        drawCircle(Ink.line, 0.002f * u, Offset(bx.x + 0.004f * u, bx.y - 0.015f * u))
    }
    // A hanging lantern on the deck's right end, glowing at night.
    val lan = q(0.35f, deckT + 0.1f, 0.05f)
    fxLine(Offset(lan.x, lan.y - 0.08f * u), Offset(lan.x, lan.y - 0.012f * u), Ink.line, pen.lw * 0.6f)
    fxGlow(lan, 0.1f * u, FxC.warm, 0.1f + 0.7f * pen.night)
    inkedRound(Rect(lan.x - 0.008f * u, lan.y - 0.011f * u, lan.x + 0.008f * u, lan.y + 0.011f * u), 0.003f * u, lerp(Color(0xFFFFF2B0), Color(0xFFFFD66B), pen.night), pen, shade = false)
}

// -------------------------------------------------------------------------------------------- rope ladder

/** The rope ladder: two thick ropes with nine rungs, swaying, tied under the deck's edge. */
internal fun DrawScope.gaLadder(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val top = -0.43f
    val rope = GaK.rope
    gyShadow(u, 0.08f, 0.04f, 0.6f)
    fun swayAt(k: Float) = sin(t * 1.3f + k * 0.5f) * 0.006f * k + f.anim * sin(f.anim * 20f + k) * 0.01f * k
    // The wooden bar under the deck and its brackets.
    capsule(Offset(-0.05f * u, top * u), Offset(0.05f * u, top * u), 0.0075f * u, FxC.oakDark, pen)
    for (sx in floatArrayOf(-0.05f, 0.05f)) {
        val path = Path().apply {
            moveTo(sx * u, (top - 0.03f) * u)
            for (k in 0..10) lineTo((sx + swayAt(k / 10f) * 0.2f) * u, (top + (-top) * k / 10f) * u)
        }
        drawPath(path, Ink.line, style = Stroke(0.0125f * u, cap = StrokeCap.Round))
        drawPath(path, rope, style = Stroke(0.0085f * u, cap = StrokeCap.Round))
        drawPath(path, GaK.ropeDark, alpha = 0.5f, style = Stroke(0.0085f * u, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round, miter = 4f))
    }
    for (k in 1..9) {
        val y = top + (-top) * k / 10f
        val sx = swayAt(k / 10f) * 0.2f
        capsule(Offset((-0.05f + sx) * u, y * u), Offset((0.05f + sx) * u, y * u), 0.0085f * u, FxC.oak, pen)
        for (s in floatArrayOf(-0.05f, 0.05f)) drawCircle(GaK.ropeDark, 0.0045f * u, Offset((s + sx) * u, y * u))
    }
}

// ------------------------------------------------------------------------------------------------ the swing

private class GySwing(val pivot: Offset, val tire: Offset, val tilt: Float)

private fun gySwing(f: Fixture, u: Float): GySwing {
    val pivot = Offset(0f, -GardenRules.SWING_PIVOT * u)
    val a = f.angle
    val tire = Offset(pivot.x + GardenRules.SWING_ROPE * sin(a) * u, pivot.y + GardenRules.SWING_ROPE * cos(a) * u)
    return GySwing(pivot, tire, a * 0.45f)
}

/** The tyre swing: three ropes from the branch to a black tyre lying flat; the back half here, the near rim in front of the rider. */
internal fun DrawScope.gaSwing(f: Fixture, u: Float, pen: Pen) {
    val g = gySwing(f, u)
    val rx = 0.075f * u
    val ry = 0.034f * u
    // Its shadow stays on the ground, small and shifting with the swing.
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * 0.9f), Offset(g.tire.x - rx, 0.002f * u), Size(rx * 2f, 0.024f * u))
    // The ropes: left and right to the rim, a third behind.
    for (s in floatArrayOf(-0.85f, 0.85f)) {
        val rim = Offset(g.tire.x + s * rx * 0.9f, g.tire.y - ry * 0.35f)
        drawLine(Ink.line, g.pivot, rim, strokeWidth = pen.lw * 2.6f, cap = StrokeCap.Round)
        drawLine(GaK.rope, g.pivot, rim, strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    }
    val back = Offset(g.tire.x + 0.01f * u, g.tire.y - ry * 0.9f)
    drawLine(Ink.line, g.pivot, back, strokeWidth = pen.lw * 2.2f, cap = StrokeCap.Round)
    drawLine(GaK.ropeDark, g.pivot, back, strokeWidth = pen.lw * 1.2f, cap = StrokeCap.Round)
    // A knot and the branch collar at the top.
    drawCircle(Ink.line, 0.0095f * u, g.pivot)
    drawCircle(GaK.rope, 0.0065f * u, g.pivot)
    // The tyre: a black ring seen from a little above; this is its back half and its hole.
    rotate(g.tilt * 57.29578f, pivot = g.tire) {
        drawOval(Ink.line, Offset(g.tire.x - rx - pen.lw, g.tire.y - ry - pen.lw), Size(2f * rx + 2f * pen.lw, 2f * ry + 2f * pen.lw))
        drawOval(GaK.rubber, Offset(g.tire.x - rx, g.tire.y - ry), Size(2f * rx, 2f * ry))
        drawOval(Color(0xFF14121A), Offset(g.tire.x - rx * 0.5f, g.tire.y - ry * 0.55f), Size(rx, ry * 1.1f))
        drawOval(Color(0xFF55525E), Offset(g.tire.x - rx * 0.85f, g.tire.y - ry * 0.8f), Size(rx * 0.7f, ry * 0.32f), alpha = 0.7f)
    }
}

internal fun DrawScope.gaSwingFront(f: Fixture, u: Float, pen: Pen) {
    val g = gySwing(f, u)
    val rx = 0.075f * u
    val ry = 0.034f * u
    rotate(g.tilt * 57.29578f, pivot = g.tire) {
        // The near half of the rim, in front of the rider's legs, with the tread marks.
        val rimPath = Path().apply {
            moveTo(g.tire.x - rx, g.tire.y)
            cubicTo(g.tire.x - rx, g.tire.y + ry * 1.35f, g.tire.x + rx, g.tire.y + ry * 1.35f, g.tire.x + rx, g.tire.y)
            cubicTo(g.tire.x + rx * 0.6f, g.tire.y + ry * 0.25f, g.tire.x - rx * 0.6f, g.tire.y + ry * 0.25f, g.tire.x - rx, g.tire.y)
            close()
        }
        drawPath(rimPath, GaK.rubber)
        drawPath(rimPath, Ink.line, style = Stroke(pen.lw * 1.4f))
        for (k in 1..6) {
            val x = g.tire.x - rx + 2f * rx * k / 7f
            drawLine(Color(0xFF55525E), Offset(x, g.tire.y + ry * 0.45f), Offset(x, g.tire.y + ry * 1.0f), strokeWidth = pen.lw * 0.8f, alpha = 0.7f)
        }
        drawArc(Color.White, 200f, 80f, false, Offset(g.tire.x - rx * 0.7f, g.tire.y + ry * 0.2f), Size(rx * 0.9f, ry), alpha = 0.4f, style = Stroke(pen.lw * 1.2f, cap = StrokeCap.Round))
    }
}

// ------------------------------------------------------------------------------------------------ zip line

/** The carriage of the zip line: a red trolley with two wheels on the cable, straps to a handle bar, and a sling seat. */
internal fun DrawScope.gaZip(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val moving = f.mode == 1
    val sway = (if (moving) sin(t * 3.2f) * 0.06f else sin(t * 1.2f + 1f) * 0.012f)
    val topY = -GardenLayout.ZIP_HANG * u
    val spin = f.shiftX * 36f
    rotate(sway * 57.29578f, pivot = Offset(0f, topY)) {
        // The seat: a plank on two ropes, behind the rider.
        for (s in floatArrayOf(-0.045f, 0.045f)) {
            drawLine(Ink.line, Offset(s * u * 0.6f, topY + 0.03f * u), Offset(s * u, 0.004f * u), strokeWidth = pen.lw * 2.4f)
            drawLine(GaK.rope, Offset(s * u * 0.6f, topY + 0.03f * u), Offset(s * u, 0.004f * u), strokeWidth = pen.lw * 1.2f)
        }
        capsule(Offset(-0.055f * u, 0.008f * u), Offset(0.055f * u, 0.008f * u), 0.0085f * u, FxC.oak, pen)
        // The handle bar above the rider's head, with soft grips.
        capsule(Offset(-0.05f * u, topY + 0.1f * u), Offset(0.05f * u, topY + 0.1f * u), 0.0075f * u, FxC.steel, pen)
        for (s in floatArrayOf(-1f, 1f)) capsule(Offset(s * 0.035f * u, topY + 0.1f * u), Offset(s * 0.05f * u, topY + 0.1f * u), 0.011f * u, GaK.red, pen)
        drawLine(Ink.line, Offset(-0.02f * u, topY + 0.03f * u), Offset(-0.035f * u, topY + 0.1f * u), strokeWidth = pen.lw * 1.4f)
        drawLine(Ink.line, Offset(0.02f * u, topY + 0.03f * u), Offset(0.035f * u, topY + 0.1f * u), strokeWidth = pen.lw * 1.4f)
        // The trolley: a red body between two wheels that turn on the cable.
        inkedRound(Rect(-0.03f * u, topY - 0.004f * u, 0.03f * u, topY + 0.036f * u), 0.008f * u, GaK.red, pen, shade = true)
        for (s in floatArrayOf(-0.02f, 0.02f)) {
            val wc = Offset(s * u, topY - 0.012f * u)
            inkedCircle(wc, 0.0125f * u, FxC.steel, pen, shade = false)
            drawCircle(FxC.rubber, 0.0085f * u, wc)
            drawLine(FxC.steel, Offset(wc.x + cos(spin) * 0.008f * u, wc.y + sin(spin) * 0.008f * u), Offset(wc.x - cos(spin) * 0.008f * u, wc.y - sin(spin) * 0.008f * u), strokeWidth = pen.lw * 1.1f)
        }
        // A pennant on the trolley.
        val wave = sin(t * 7f) * 0.004f * u
        inked(Path().apply {
            moveTo(0.024f * u, topY + 0.008f * u)
            quadraticTo(0.045f * u, topY + 0.004f * u + wave, 0.062f * u, topY + 0.014f * u)
            quadraticTo(0.045f * u, topY + 0.02f * u - wave, 0.024f * u, topY + 0.02f * u)
            close()
        }, FxC.yellow, pen, shade = false)
    }
}

/** The pole at the end of the zip line: red and white bands, a pulley ring, a spring bumper, a bell and a flag. */
internal fun DrawScope.gaZipPole(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    gyShadow(u, 0.12f, 0.1f, 0.8f)
    val b = q(0f, 0f, 0.05f)
    val top = q(0f, -0.49f, 0.05f)
    // Bands of red and white up the pole.
    val bands = 8
    for (k in 0 until bands) {
        val y0 = b.y + (top.y - b.y) * k / bands
        val y1 = b.y + (top.y - b.y) * (k + 1) / bands
        val col = if (k % 2 == 0) GaK.red else FxC.paint
        val rr = (0.017f - 0.004f * k / bands) * u
        val rt = (0.017f - 0.004f * (k + 1) / bands) * u
        fxCyl(b.x, y0, y1, rr, rt, col, pen, cap = k == bands - 1, bottom = k == 0)
    }
    inkedCircle(Offset(top.x, top.y - 0.012f * u), 0.0115f * u, FxC.brass, pen, shade = true)
    // The ring and pulley the cable ends in, on a short arm to the left.
    val ring = Offset(top.x - 0.03f * u, top.y + 0.02f * u)
    capsule(Offset(top.x - 0.004f * u, top.y + 0.026f * u), ring, 0.0055f * u, FxC.steel, pen)
    drawCircle(Ink.line, 0.0115f * u, ring)
    drawCircle(FxC.steel, 0.0085f * u, ring)
    drawCircle(Color(0xFF5A6078), 0.0042f * u, ring)
    // A spring bumper: the red buffer a trolley would knock against.
    val bumper = Offset(top.x - 0.02f * u, top.y + 0.06f * u)
    inkedRound(Rect(bumper.x - 0.016f * u, bumper.y - 0.008f * u, bumper.x + 0.008f * u, bumper.y + 0.008f * u), 0.004f * u, GaK.red, pen, shade = false)
    for (k in 0 until 4) drawLine(FxC.steel, Offset(bumper.x - 0.014f * u + k * 0.006f * u, bumper.y - 0.012f * u), Offset(bumper.x - 0.01f * u + k * 0.006f * u, bumper.y + 0.012f * u), strokeWidth = pen.lw * 0.9f)
    // A bell that rings when the pole is tapped.
    val bellTop = Offset(top.x + 0.022f * u, top.y + 0.1f * u)
    val swing = f.anim * sin(f.anim * 26f) * 0.4f + sin(t * 1.4f) * 0.04f
    rotate(swing * 57.29578f, pivot = Offset(bellTop.x, bellTop.y - 0.012f * u)) {
        fxLine(Offset(bellTop.x, bellTop.y - 0.012f * u), bellTop, Ink.line, pen.lw * 0.6f)
        val bell = Path().apply {
            moveTo(bellTop.x - 0.013f * u, bellTop.y + 0.028f * u)
            quadraticTo(bellTop.x - 0.012f * u, bellTop.y, bellTop.x, bellTop.y - 0.004f * u)
            quadraticTo(bellTop.x + 0.012f * u, bellTop.y, bellTop.x + 0.013f * u, bellTop.y + 0.028f * u)
            close()
        }
        inked(bell, FxC.brass, pen)
        drawCircle(Ink.line, 0.0035f * u, Offset(bellTop.x, bellTop.y + 0.032f * u))
    }
    // The flag at the top.
    val pole = Offset(top.x, top.y - 0.022f * u)
    fxLine(pole, Offset(pole.x, pole.y - 0.03f * u), Ink.line, pen.lw)
    val wave = sin(t * 4f + 1f) * 0.005f * u
    inked(Path().apply {
        moveTo(pole.x, pole.y - 0.03f * u)
        quadraticTo(pole.x + 0.025f * u, pole.y - 0.034f * u + wave, pole.x + 0.05f * u, pole.y - 0.026f * u)
        quadraticTo(pole.x + 0.025f * u, pole.y - 0.018f * u - wave, pole.x, pole.y - 0.01f * u)
        close()
    }, FxC.yellow, pen, shade = false)
}

// ------------------------------------------------------------------------------------------------ trampoline

/** A round garden trampoline with a blue padded ring, springs, a dark mat with a yellow star, and a safety net behind. */
internal fun DrawScope.gaTrampoline(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    gyShadow(u, 0.42f, 0.2f)
    val cz = 0.1f
    val matY = -0.09f
    val c = q(0f, matY, cz)
    val rx = 0.19f * u
    val rz = 0.1f * u
    // The net posts around the back half and the netting between them, drawn first.
    val posts = ArrayList<Offset>(7)
    for (k in 0..6) {
        val a = 3.1416f * k / 6f + 0.0f
        val rimp = fxRim(c.x, c.y, 0.19f * u, a)
        posts.add(rimp)
        val topP = Offset(rimp.x, rimp.y - 0.2f * u)
        capsule(rimp, topP, 0.0048f * u, FxC.steel, pen)
    }
    val netTop = Path()
    for ((i, p) in posts.withIndex()) {
        val tp = Offset(p.x, p.y - 0.2f * u)
        if (i == 0) netTop.moveTo(tp.x, tp.y) else netTop.lineTo(tp.x, tp.y)
    }
    drawPath(netTop, Ink.line, style = Stroke(pen.lw * 1.2f))
    for (k in 0 until posts.size - 1) {
        val a = posts[k]
        val b = posts[k + 1]
        for (j in 1..3) {
            val hy = j * 0.05f * u
            drawLine(Color(0xFF55525E).copy(alpha = 0.5f), Offset(a.x, a.y - hy), Offset(b.x, b.y - hy), strokeWidth = pen.lw * 0.5f)
        }
        for (j in 1..3) {
            val fr = j / 4f
            drawLine(Color(0xFF55525E).copy(alpha = 0.4f), Offset(a.x + (b.x - a.x) * fr, a.y + (b.y - a.y) * fr), Offset(a.x + (b.x - a.x) * fr, a.y + (b.y - a.y) * fr - 0.2f * u), strokeWidth = pen.lw * 0.5f)
        }
    }
    // The legs.
    for (a in floatArrayOf(0.55f, 2.6f, 3.9f, 5.8f)) {
        val rimp = fxRim(c.x, c.y, 0.17f * u, a)
        capsule(rimp, Offset(rimp.x + (if (cos(a) > 0f) 0.012f else -0.012f) * u, rimp.y + (-matY - (rimp.y - c.y) / u * 0f) * u + (c.y - rimp.y) * 0f), 0.006f * u, FxC.steel, pen)
    }
    // The padded ring, the springs and the mat that dips after a bounce.
    drawPath(fxDisc2(c.x, c.y + 0.004f * u, rx + 0.014f * u, rz + 0.01f * u), Color(0xFF1E5AA8))
    drawPath(fxDisc2(c.x, c.y, rx + 0.014f * u, rz + 0.01f * u), Color(0xFF2F6FB8))
    drawPath(fxDisc2(c.x, c.y, rx + 0.014f * u, rz + 0.01f * u), Ink.line, style = pen.stroke)
    drawPath(fxDisc2(c.x, c.y, rx - 0.012f * u, rz - 0.006f * u), Color(0xFF14121A))
    val dip = f.anim * 0.03f * u * cos((1f - f.anim) * 10f)
    for (k in 0 until 26) {
        val a = k * 6.2832f / 26f
        val outer = fxRim(c.x, c.y, rx / 1.118f * 0.99f, a)
        val inner = fxRim(c.x, c.y + dip * 0.3f, rx / 1.118f * 0.88f, a)
        drawLine(Color(0xFFE3E8EF), outer, inner, strokeWidth = pen.lw * 0.9f, alpha = 0.9f)
    }
    val mat = fxDisc2(c.x, c.y + dip * 0.5f, rx * 0.84f, rz * 0.84f)
    drawPath(mat, safeRadialGradient(0f to Color(0xFF3A3748), 1f to Color(0xFF1E1C28), center = Offset(c.x, c.y + dip * 0.5f), radius = rx))
    drawPath(mat, Ink.line, style = pen.thin)
    inked(starPath(Offset(c.x, c.y + dip * 0.5f), 0.045f * u, 0.02f * u), FxC.yellow, pen, shade = false)
    drawLine(Color.White.copy(alpha = 0.3f), Offset(c.x - rx * 0.6f, c.y - rz * 0.4f), Offset(c.x - rx * 0.2f, c.y - rz * 0.55f), strokeWidth = pen.lw * 1.4f, cap = StrokeCap.Round)
    // A small ladder at the front right.
    val l0 = q(0.15f, matY + 0.03f, 0.0f)
    for (s in floatArrayOf(0f, 0.03f)) capsule(Offset(l0.x + s * u, l0.y), Offset(l0.x + s * u - 0.012f * u, l0.y + 0.08f * u), 0.0042f * u, FxC.steel, pen)
    for (k in 1..2) drawLine(FxC.steel, Offset(l0.x - 0.005f * u * k, l0.y + 0.03f * u * k), Offset(l0.x + 0.03f * u - 0.005f * u * k, l0.y + 0.03f * u * k), strokeWidth = pen.lw * 1.4f)
}

// -------------------------------------------------------------------------------------------------- sandbox

/** A wooden sandbox with corner seats, warm sand with a few footprints, a half-built castle, and holes where somebody dug. */
internal fun DrawScope.gaSandbox(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val d = 0.26f
    val wood = FxC.oak
    gyShadow(u, 0.52f, d)
    // The frame: front board, right board, and the back boards behind the sand.
    fxBox(u, -0.26f, -0.08f, 0.26f, 0f, d, wood, pen, rad = 0.003f, top = Color(0xFFE8C98A), side = wood.darken(0.22f))
    fxFace(fxFlat(u, -0.245f, 0.245f, -0.055f, 0.014f, d - 0.014f, 0.004f), GaK.sand, pen)
    for (k in 1..5) fxLine(Offset((-0.26f + k * 0.0867f) * u, -0.076f * u), Offset((-0.26f + k * 0.0867f) * u, -0.004f * u), wood.darken(0.3f), pen.lw * 0.5f)
    fxLine(Offset(-0.255f * u, -0.04f * u), Offset(0.255f * u, -0.04f * u), wood.darken(0.25f), pen.lw * 0.5f)
    // The sand: ripples, footprints and the dug holes.
    for (k in 0 until 14) {
        val c = q(-0.22f + 0.44f * hash01(k, 891), -0.0552f, 0.03f + 0.19f * hash01(k, 892))
        drawArc(GaK.sand.darken(0.18f), 200f, 120f, false, Offset(c.x - 0.012f * u, c.y - 0.003f * u), Size(0.024f * u, 0.006f * u), style = pen.thin)
    }
    for (k in 0..2) {
        val c = q(-0.1f + k * 0.07f, -0.0552f, 0.03f + 0.01f * (k % 2))
        drawOval(GaK.sand.darken(0.22f), Offset(c.x - 0.007f * u, c.y - 0.004f * u), Size(0.012f * u, 0.0065f * u))
    }
    val dig = f.count % 3
    if (dig >= 1) {
        val c = q(0.1f, -0.0552f, 0.1f)
        drawPath(fxDisc2(c.x, c.y, (0.02f + 0.01f * dig) * u, (0.012f + 0.006f * dig) * u), GaK.sand.darken(0.35f))
        drawPath(fxDisc2(c.x + 0.03f * u, c.y - 0.002f * u, 0.022f * u, 0.011f * u), GaK.sand.lighten(0.1f))
    }
    // A little half-built castle with a flag.
    val cc = q(-0.14f, -0.0552f, 0.12f)
    val tower = Path().apply {
        moveTo(cc.x - 0.026f * u, cc.y)
        lineTo(cc.x - 0.022f * u, cc.y - 0.034f * u)
        lineTo(cc.x + 0.022f * u, cc.y - 0.034f * u)
        lineTo(cc.x + 0.026f * u, cc.y)
        close()
    }
    inked(tower, GaK.sand.darken(0.05f), pen)
    for (k in 0..2) inkedRound(Rect(cc.x - 0.022f * u + k * 0.0165f * u, cc.y - 0.043f * u, cc.x - 0.012f * u + k * 0.0165f * u, cc.y - 0.034f * u), 0.0015f * u, GaK.sand.darken(0.05f), pen, shade = false)
    fxLine(Offset(cc.x, cc.y - 0.034f * u), Offset(cc.x, cc.y - 0.07f * u), Ink.line, pen.lw)
    inked(fxPoly(1f, cc.x, cc.y - 0.07f * u, cc.x + 0.02f * u, cc.y - 0.064f * u, cc.x, cc.y - 0.058f * u), FxC.red, pen, shade = false)
    // Corner seat boards on the frame.
    for (s in floatArrayOf(-1f, 1f)) fxBox(u, s * 0.2f - 0.035f, -0.092f, s * 0.2f + 0.035f, -0.08f, 0.05f, wood.lighten(0.1f), pen, rad = 0.002f, z = 0.0f)
    if (f.anim > 0.05f) {
        for (k in 0 until 8) {
            val ph = 1f - f.anim
            val c = q(0.1f + (k - 3.5f) * 0.012f, -0.06f - sin(ph * 3.1416f) * (0.04f + 0.01f * (k % 3)), 0.1f)
            drawCircle(GaK.sand.copy(alpha = f.anim), 0.0032f * u, c)
        }
    }
}

// --------------------------------------------------------------------------------------------------- hammock

private fun DrawScope.gyHammockPath(u: Float, sag: Float, lift: Float, shift: Float): Path = Path().apply {
    moveTo(-0.3f * u, (-0.27f - lift) * u)
    quadraticTo((shift) * u, (-0.27f + sag * 2f) * u, 0.3f * u, (-0.27f - lift) * u)
}

/** The hammock between two posts: a striped sling that sags in the middle, a pillow, and tassels; its front edge drapes over whoever lies in it. */
internal fun DrawScope.gaHammock(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    gyShadow(u, 0.74f, 0.16f, 0.8f)
    val sway = sin(t * 0.7f + f.id) * 0.01f + f.anim * 0.02f * sin(f.anim * 14f)
    // The two posts with caps and a hook each.
    for (s in floatArrayOf(-1f, 1f)) {
        val px = s * 0.335f
        fxPost(u, px, 0.06f, 0f, -0.32f, 0.014f, GaK.woodDark, pen)
        val cap = fxQ(u, px, -0.32f, 0.06f)
        inkedCircle(Offset(cap.x, cap.y - 0.004f * u), 0.0145f * u, GaK.woodLight, pen, shade = false)
        inkedOval(Rect(cap.x - 0.03f * u, fxQ(u, px, 0f, 0.06f).y - 0.006f * u, cap.x + 0.03f * u, fxQ(u, px, 0f, 0.06f).y + 0.008f * u), FxC.stone, pen, shade = false)
    }
    // The back of the sling.
    val sag = 0.125f
    val back = Path().apply {
        moveTo(fxQ(u, -0.31f, -0.3f, 0.1f).x, fxQ(u, -0.31f, -0.3f, 0.1f).y)
        quadraticTo(fxQ(u, sway, -0.3f + sag * 2f - 0.06f, 0.1f).x, fxQ(u, sway, -0.3f + sag * 2f - 0.06f, 0.1f).y, fxQ(u, 0.31f, -0.3f, 0.1f).x, fxQ(u, 0.31f, -0.3f, 0.1f).y)
        lineTo(fxQ(u, 0.31f, -0.27f, 0.04f).x, fxQ(u, 0.31f, -0.27f, 0.04f).y)
        quadraticTo(fxQ(u, sway, -0.27f + sag * 2f - 0.02f, 0.04f).x, fxQ(u, sway, -0.27f + sag * 2f - 0.02f, 0.04f).y, fxQ(u, -0.31f, -0.27f, 0.04f).x, fxQ(u, -0.31f, -0.27f, 0.04f).y)
        close()
    }
    drawPath(back, Color(0xFF2F8F94).darken(0.15f))
    drawPath(back, Ink.line, style = pen.stroke)
    // The ropes from the hooks to the ends of the sling.
    for (s in floatArrayOf(-1f, 1f)) {
        val hook = fxQ(u, s * 0.335f, -0.3f, 0.06f)
        val end = fxQ(u, s * 0.3f, -0.285f, 0.07f)
        drawLine(Ink.line, hook, end, strokeWidth = pen.lw * 2.2f)
        drawLine(GaK.rope, hook, end, strokeWidth = pen.lw)
        // Tassels.
        for (k in 0..3) drawLine(FxC.yellow, Offset(end.x + (k - 1.5f) * 0.006f * u * s, end.y), Offset(end.x + (k - 1.5f) * 0.009f * u * s, end.y + 0.03f * u), strokeWidth = pen.lw * 0.9f, cap = StrokeCap.Round)
    }
    // A pillow at the left end.
    val pil = fxQ(u, -0.2f, -0.2f, 0.07f)
    inkedRound(Rect(pil.x - 0.04f * u, pil.y - 0.022f * u, pil.x + 0.03f * u, pil.y + 0.006f * u), 0.012f * u, FxC.blush, pen, shade = true)
    drawLine(FxC.paint, Offset(pil.x - 0.03f * u, pil.y - 0.01f * u), Offset(pil.x + 0.02f * u, pil.y - 0.01f * u), strokeWidth = pen.lw * 0.9f, alpha = 0.8f)
}

/** The front fold of the hammock: stripes that cover the lower part of whoever lies in it. */
internal fun DrawScope.gaHammockFront(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val sway = sin(t * 0.7f + f.id) * 0.01f + f.anim * 0.02f * sin(f.anim * 14f)
    val sag = 0.125f
    val upper = Path().apply {
        moveTo(-0.31f * u, -0.27f * u)
        quadraticTo(sway * u, (-0.27f + sag * 2f - 0.07f) * u, 0.31f * u, -0.27f * u)
        quadraticTo(sway * u, (-0.27f + sag * 2f + 0.01f) * u, -0.31f * u, -0.27f * u)
        close()
    }
    val stripes = 11
    clipRect(-0.32f * u, -0.3f * u, 0.32f * u, 0.0f) {
        val colors = arrayOf(Color(0xFF2F8F94), Color(0xFFF7F5F0), Color(0xFFFFC83D), Color(0xFFF7F5F0))
        for (k in 0 until stripes) {
            val x0 = -0.32f + 0.64f * k / stripes
            val x1 = -0.32f + 0.64f * (k + 1) / stripes
            val band = Path().apply {
                addRect(Rect(x0 * u, -0.3f * u, x1 * u, 0f))
            }
            clipPath(upper) { drawPath(band, colors[k % 4]) }
        }
    }
    drawPath(upper, Ink.line, style = pen.stroke)
    // A soft highlight along the sag.
    val hl = Path().apply {
        moveTo(-0.2f * u, (-0.27f + sag * 1.5f - 0.04f) * u)
        quadraticTo(sway * u, (-0.27f + sag * 2f - 0.055f) * u, 0.2f * u, (-0.27f + sag * 1.5f - 0.04f) * u)
    }
    drawPath(hl, Color.White, alpha = 0.3f, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
}

// ---------------------------------------------------------------------------------------------------- bridge

/** A little wooden footbridge over the back of the pond: planks, a rope rail, two legs in the water and a lantern. */
internal fun DrawScope.gaBridge(f: Fixture, u: Float, pen: Pen) {
    fun q(x: Float, y: Float, z: Float) = fxQ(u, x, y, z)
    val t = pen.t
    val d = 0.14f
    val wood = FxC.oak
    // Legs down to the water.
    for (x in floatArrayOf(-0.25f, 0.25f)) {
        fxBox(u, x - 0.016f, -0.1f, x + 0.016f, 0.012f, 0.03f, wood.darken(0.25f), pen, rad = 0.002f, z = 0.05f)
    }
    // The deck.
    fxBox(u, -0.36f, -0.1f, 0.36f, -0.068f, d, wood, pen, rad = 0.004f, z = 0.0f, top = wood.lighten(0.16f), side = wood.darken(0.25f))
    for (k in 1..13) fxLine(q(-0.36f + k * 0.0514f, -0.1f, 0f), q(-0.36f + k * 0.0514f, -0.1f, d), wood.darken(0.35f), pen.lw * 0.5f)
    for (k in 1..13) fxLine(Offset((-0.36f + k * 0.0514f) * u, -0.098f * u), Offset((-0.36f + k * 0.0514f) * u, -0.07f * u), wood.darken(0.35f), pen.lw * 0.45f)
    // Rail posts and the sagging rope, at the back edge so the deck stays clear.
    val posts = floatArrayOf(-0.34f, -0.17f, 0f, 0.17f, 0.34f)
    for (px in posts) fxBox(u, px - 0.008f, -0.2f, px + 0.008f, -0.1f, 0.016f, wood.lighten(0.05f), pen, rad = 0.002f, z = d - 0.02f)
    for (k in 0 until posts.size - 1) {
        val a = q(posts[k], -0.185f, d - 0.012f)
        val b = q(posts[k + 1], -0.185f, d - 0.012f)
        val rope = Path().apply {
            moveTo(a.x, a.y)
            quadraticTo((a.x + b.x) / 2f, (a.y + b.y) / 2f + 0.03f * u, b.x, b.y)
        }
        drawPath(rope, Ink.line, style = Stroke(pen.lw * 2.6f, cap = StrokeCap.Round))
        drawPath(rope, GaK.rope, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
    }
    // A lantern on the middle post, and a potted flower at the right end.
    val lan = q(0f, -0.2f, d - 0.012f)
    fxGlow(Offset(lan.x, lan.y - 0.016f * u), 0.1f * u, FxC.warm, 0.1f + 0.7f * pen.night)
    inkedRound(Rect(lan.x - 0.008f * u, lan.y - 0.03f * u, lan.x + 0.008f * u, lan.y - 0.006f * u), 0.003f * u, lerp(Color(0xFFFFF2B0), Color(0xFFFFD66B), pen.night), pen, shade = false)
    val pot = q(0.3f, -0.1f, 0.04f)
    inkedRound(Rect(pot.x - 0.015f * u, pot.y - 0.026f * u, pot.x + 0.015f * u, pot.y), 0.003f * u, FxC.terracotta, pen, shade = false)
    for (k in -1..1) {
        drawLine(GaK.leaf, Offset(pot.x, pot.y - 0.026f * u), Offset(pot.x + k * 0.014f * u + sin(t * 1.2f + k) * 0.003f * u, pot.y - 0.055f * u), strokeWidth = pen.lw * 1.3f, cap = StrokeCap.Round)
    }
    drawCircle(FxC.pink, 0.008f * u, Offset(pot.x + sin(t * 1.2f) * 0.003f * u, pot.y - 0.058f * u))
    // A ripple around each leg where it meets the water.
    for (x in floatArrayOf(-0.25f, 0.25f)) {
        val ph = fxFrac(t * 0.3f + x)
        val c = q(x, 0.004f, 0.065f)
        drawOval(Color.White, Offset(c.x - (0.03f + ph * 0.02f) * u, c.y - (0.006f + ph * 0.004f) * u), Size((0.06f + ph * 0.04f) * u, (0.012f + ph * 0.008f) * u), alpha = 0.5f * (1f - ph), style = pen.thin)
    }
}

// ------------------------------------------------------------------------------------------------ the frogs

private val GY_FROGS = arrayOf(
    Color(0xFF3FA84E), Color(0xFF3AA6A0), Color(0xFF9CCB3A), Color(0xFFE8803A), Color(0xFF5FD06A),
)

/**
 * A frog on a lily pad. Each of the five has its own colour and size and its own note when it croaks; its throat
 * puffs out as it does. The pad floats and bobs, and the frog blinks.
 */
internal fun DrawScope.gaFrog(f: Fixture, u: Float, pen: Pen) {
    val t = pen.t
    val k = f.variant.coerceIn(0, 4)
    val size = floatArrayOf(1.18f, 1.05f, 1f, 0.95f, 0.82f)[k]
    val body = GY_FROGS[k]
    val bob = sin(t * 1.3f + k * 1.7f) * 0.0016f * u
    translate(0f, bob) {
        // The lily pad: a flat green disc with a notch and veins, a pink flower on the big one.
        val padR = 0.056f * u
        val pad = fxDisc2(0f, -0.004f * u, padR, 0.036f * u)
        drawPath(fxDisc2(0f, 0f, padR, 0.036f * u), Color(0xFF2E7D46))
        drawPath(pad, Color(0xFF55B34A))
        drawPath(pad, Ink.line, style = pen.stroke)
        drawLine(Color(0xFF2E7D46), Offset(0f, -0.004f * u), Offset(padR * 0.9f, -0.012f * u), strokeWidth = pen.lw * 1.6f)
        for (v in 0 until 5) {
            val a = v * 1.26f
            drawLine(Color(0xFF2E7D46).copy(alpha = 0.55f), Offset(0f, -0.004f * u), Offset(cos(a) * padR * 0.8f, -0.004f * u + sin(a) * 0.026f * u), strokeWidth = pen.lw * 0.5f)
        }
        if (k == 2) {
            val fl = Offset(-0.034f * u, -0.012f * u)
            for (p in 0 until 6) {
                val a = p * 1.047f
                drawCircle(Color(0xFFFFB3C7), 0.006f * u, Offset(fl.x + cos(a) * 0.008f * u, fl.y + sin(a) * 0.005f * u))
                drawCircle(Ink.line, 0.006f * u, Offset(fl.x + cos(a) * 0.008f * u, fl.y + sin(a) * 0.005f * u), alpha = 0.5f, style = pen.thin)
            }
            drawCircle(FxC.yellow, 0.0055f * u, fl)
        }
        // The frog: back legs folded, a round body, front legs, a big head with bulging eyes.
        val s = size * u
        val base = Offset(0.004f * s, -0.008f * u)
        for (sx in floatArrayOf(-1f, 1f)) {
            inkedOval(Rect(base.x + sx * 0.026f * s - 0.014f * s, base.y - 0.02f * s, base.x + sx * 0.026f * s + 0.014f * s, base.y + 0.004f * s), body.darken(0.12f), pen)
            inkedOval(Rect(base.x + sx * 0.034f * s - 0.012f * s, base.y - 0.004f * s, base.x + sx * 0.034f * s + 0.014f * s, base.y + 0.006f * s), body.darken(0.05f), pen, shade = false)
        }
        val bodyR = Rect(base.x - 0.03f * s, base.y - 0.044f * s, base.x + 0.03f * s, base.y - 0.003f * s)
        inkedOval(bodyR, body, pen)
        // Spots on the back.
        for (sp in 0..2) drawCircle(body.darken(0.25f), 0.0042f * s, Offset(base.x + (sp - 1) * 0.015f * s, base.y - 0.032f * s + (sp % 2) * 0.006f * s))
        // The throat sac: puffs out when the frog croaks (f.anim after a tap).
        val puff = (f.anim * 1.4f).coerceIn(0f, 1f)
        val head = Offset(base.x + 0.012f * s, base.y - 0.038f * s)
        if (puff > 0.04f) {
            val sac = Rect(head.x - 0.014f * s - 0.006f * s * puff, head.y + 0.006f * s, head.x + 0.014f * s + 0.006f * s * puff, head.y + 0.006f * s + 0.03f * s * puff)
            inkedOval(sac, Color(0xFFFFE9A8), pen)
        }
        inkedOval(Rect(head.x - 0.024f * s, head.y - 0.02f * s, head.x + 0.024f * s, head.y + 0.012f * s), body.lighten(0.04f), pen)
        for (sx in floatArrayOf(-1f, 1f)) {
            val e = Offset(head.x + sx * 0.0125f * s, head.y - 0.02f * s)
            drawCircle(Ink.line, 0.0105f * s, e)
            drawCircle(Color.White, 0.0085f * s, e)
            if (fxFrac(t / (3.2f + k * 0.4f)) < 0.045f) {
                drawLine(Ink.line, Offset(e.x - 0.007f * s, e.y), Offset(e.x + 0.007f * s, e.y), strokeWidth = pen.lw * 1.3f)
            } else {
                drawCircle(Ink.line, 0.0045f * s, Offset(e.x + sx * 0.0008f * s + 0.001f * s, e.y + 0.0005f * s))
                drawCircle(Color.White, 0.0015f * s, Offset(e.x - 0.001f * s, e.y - 0.002f * s))
            }
        }
        // A wide smile; open a little when it croaks.
        drawArc(Ink.line, 10f, 160f, false, Offset(head.x - 0.014f * s, head.y - 0.012f * s), Size(0.028f * s, 0.016f * s + 0.008f * s * puff), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
        // Front legs.
        for (sx in floatArrayOf(-1f, 1f)) capsule(Offset(base.x + sx * 0.018f * s, base.y - 0.012f * s), Offset(base.x + sx * 0.03f * s, base.y + 0.002f * s), 0.008f * s, body.darken(0.05f), pen)
    }
}
