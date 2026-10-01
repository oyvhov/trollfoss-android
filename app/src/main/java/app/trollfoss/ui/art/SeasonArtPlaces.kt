package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Festival
import app.trollfoss.domain.PlaceId
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/*
 * Where the decorations of the three feasts stand in each place. A decoration is one row in a table: what
 * it is, where (scene units: x along the place, y down the scene; the base of a standing thing, the hook of
 * a hanging one) and how big. The tables were fitted to the furniture of the blueprints in domain/Places.kt.
 * "Back" decorations are drawn behind the furniture and the figures, "front" ones over them.
 */

private enum class K { TREE, PRESENTS, WREATH, STAR, STRAND, SWAGS, EGG, TULIP, BUNTING, EGGS, VASE, PUMPKIN, LANTERN, BAT, GHOST, WEB, PINE, ROOF }

/**
 * One decoration. [s] is its size, [x2]/[y2] the far end of a string, [a] a spare number (sag, hang or phase),
 * [n] a count or a style.
 */
private class D(val k: K, val x: Float, val y: Float, val s: Float = 0f, val x2: Float = 0f, val y2: Float = 0f, val a: Float = 0f, val n: Int = 0)

private fun tree(x: Float, y: Float, h: Float) = D(K.TREE, x, y, h)
private fun presents(x: Float, y: Float, w: Float) = D(K.PRESENTS, x, y, w)
private fun wreath(x: Float, y: Float, r: Float) = D(K.WREATH, x, y, r)
private fun star(x: Float, y: Float, r: Float, hang: Float = 0f) = D(K.STAR, x, y, r, a = hang)
private fun strand(x: Float, y: Float, x2: Float, y2: Float, sag: Float, n: Int) = D(K.STRAND, x, y, 0f, x2, y2, sag, n)
private fun swags(x0: Float, x1: Float, y: Float, sag: Float = 0.045f, n: Int = 0) = D(K.SWAGS, x0, y, 0f, x1, y, sag, n)
private fun egg(x: Float, y: Float, h: Float, style: Int) = D(K.EGG, x, y, h, n = style)
private fun tulip(x: Float, y: Float, h: Float, style: Int) = D(K.TULIP, x, y, h, n = style)
private fun bunting(x: Float, y: Float, x2: Float, y2: Float, sag: Float, n: Int, flag: Float = 0.04f) = D(K.BUNTING, x, y, flag, x2, y2, sag, n)
private fun eggs(x: Float, y: Float, x2: Float, y2: Float, sag: Float, n: Int, eggH: Float = 0.05f) = D(K.EGGS, x, y, eggH, x2, y2, sag, n)
private fun vase(x: Float, y: Float, h: Float) = D(K.VASE, x, y, h)
private fun pumpkin(x: Float, y: Float, h: Float, face: Boolean) = D(K.PUMPKIN, x, y, h, n = if (face) 1 else 0)
private fun lantern(x: Float, y: Float, h: Float, phase: Float = 0f) = D(K.LANTERN, x, y, h, a = phase)
private fun bat(x: Float, y: Float, hang: Float, s: Float, phase: Float = 0f) = D(K.BAT, x, y, s, a = phase, y2 = hang)
private fun ghost(x: Float, y: Float, hang: Float, h: Float, phase: Float = 0f) = D(K.GHOST, x, y, h, a = phase, y2 = hang)
private fun web(x: Float, y: Float, r: Float, dir: Float) = D(K.WEB, x, y, r, a = dir)
/** Lights spiralling a standing pine: base [x],[y], height [s]. */
private fun pine(x: Float, y: Float, h: Float) = D(K.PINE, x, y, h)
/** Lights along a roofline: a polyline of x, y pairs in [pts]. */
private fun roof(vararg pts: Float) = D(K.ROOF, pts[0], pts[1], 0f, pts[pts.size - 2], pts[pts.size - 1], 0f, 0).also { roofLines[it] = pts }

private val roofLines = HashMap<D, FloatArray>()

private class Feast(val back: List<D>, val front: List<D> = emptyList())

// ------------------------------------------------------------------------------------------ Christmas

private val christmas: Map<PlaceId, Feast> = mapOf(
    PlaceId.HOME to Feast(
        back = listOf(
            swags(-0.3f, 4.6f, 0.04f),
            tree(1.12f, 0.8f, 0.7f), presents(1.02f, 0.93f, 0.16f),
            wreath(0.15f, 0.28f, 0.055f), wreath(3.75f, 0.26f, 0.055f),
        ),
        front = listOf(star(0.4f, 0.34f, 0.04f, 0.08f), star(1.9f, 0.43f, 0.04f, 0.08f), star(3.25f, 0.41f, 0.04f, 0.08f)),
    ),
    PlaceId.CAFE to Feast(
        back = listOf(
            tree(2.05f, 0.8f, 0.58f), presents(2.45f, 0.9f, 0.13f),
            wreath(0.72f, 0.27f, 0.055f), wreath(2.8f, 0.3f, 0.055f),
        ),
        front = listOf(star(2.46f, 0.38f, 0.04f, 0.08f)),
    ),
    PlaceId.SALON to Feast(
        back = listOf(
            swags(-0.3f, 3.2f, 0.04f),
            tree(1.28f, 0.8f, 0.45f), presents(1.3f, 0.93f, 0.13f),
            wreath(1.65f, 0.27f, 0.05f),
        ),
        front = listOf(star(2.3f, 0.38f, 0.04f, 0.08f)),
    ),
    PlaceId.SHOP to Feast(
        back = listOf(
            swags(-0.3f, 3.8f, 0.07f),
            tree(1.2f, 0.8f, 0.55f),
        ),
        front = listOf(star(0.2f, 0.28f, 0.05f, 0.18f), star(0.54f, 0.28f, 0.05f, 0.18f), star(0.84f, 0.28f, 0.05f, 0.18f)),
    ),
    PlaceId.DOCTOR to Feast(
        back = listOf(
            swags(-0.3f, 3.3f, 0.04f),
            wreath(0.1f, 0.47f, 0.06f),
            tree(1.38f, 0.8f, 0.45f),
        ),
        front = listOf(star(1.575f, 0.23f, 0.035f, 0.06f), star(1.785f, 0.23f, 0.035f, 0.06f)),
    ),
    PlaceId.STAGE to Feast(
        back = listOf(swags(-0.3f, 3.6f, 0.05f), tree(2.5f, 0.8f, 0.5f), presents(2.28f, 0.83f, 0.13f)),
    ),
    PlaceId.LAB to Feast(
        back = listOf(swags(-0.3f, 3.2f, 0.04f), tree(1.4f, 0.8f, 0.5f), wreath(2.3f, 0.3f, 0.05f)),
    ),
    PlaceId.SPACE to Feast(
        back = listOf(swags(-0.3f, 4.0f, 0.04f), star(1.3f, 0.2f, 0.05f, 0.18f), star(2.7f, 0.18f, 0.045f, 0.16f)),
    ),
    PlaceId.UNDERWATER to Feast(
        back = listOf(strand(-0.2f, 0.08f, 2.0f, 0.12f, 0.1f, 22), strand(2.0f, 0.12f, 4.1f, 0.06f, 0.1f, 22), presents(0.9f, 0.9f, 0.16f)),
    ),
    PlaceId.FOREST to Feast(
        back = listOf(presents(1.05f, 0.955f, 0.14f)),
        front = listOf(pine(0.28f, 0.8f, 0.72f)),
    ),
    PlaceId.BEACH to Feast(
        back = listOf(tree(1.3f, 0.8f, 0.45f), presents(1.15f, 0.955f, 0.13f)),
    ),
    PlaceId.MOUNTAIN to Feast(
        back = listOf(presents(1.2f, 0.96f, 0.14f)),
        front = listOf(pine(0.22f, 0.81f, 0.74f)),
    ),
    PlaceId.FARM to Feast(
        back = listOf(
            roof(0f, 0.38f, 0.14f, 0.2f, 0.6f, 0.08f, 1.06f, 0.2f, 1.2f, 0.38f),
            wreath(0.6f, 0.265f, 0.055f), tree(3.05f, 0.8f, 0.5f), presents(3.15f, 0.955f, 0.13f),
        ),
    ),
    PlaceId.TIVOLI to Feast(
        back = listOf(tree(2.2f, 0.8f, 0.55f), presents(3.3f, 0.96f, 0.13f)),
    ),
    PlaceId.HEILEBERGET to Feast(
        back = listOf(tree(1.7f, 0.8f, 0.55f), presents(2.1f, 0.955f, 0.13f)),
        front = listOf(pine(3.85f, 0.77f, 0.74f)),
    ),
)

// ---------------------------------------------------------------------------------------------- Easter

private val easter: Map<PlaceId, Feast> = mapOf(
    PlaceId.HOME to Feast(
        back = listOf(
            bunting(-0.3f, 0.04f, 1.2f, 0.04f, 0.04f, 10), bunting(1.2f, 0.04f, 2.7f, 0.04f, 0.04f, 10), bunting(2.7f, 0.04f, 4.4f, 0.04f, 0.04f, 11),
            eggs(0.3f, 0.1f, 1.4f, 0.1f, 0.05f, 4), eggs(2.5f, 0.1f, 3.1f, 0.1f, 0.04f, 3), eggs(3.5f, 0.1f, 4.1f, 0.1f, 0.04f, 3),
            vase(0.5f, 0.955f, 0.17f), egg(0.75f, 0.955f, 0.06f, 0), egg(1.95f, 0.96f, 0.06f, 2),
        ),
    ),
    PlaceId.CAFE to Feast(
        back = listOf(
            eggs(1.75f, 0.1f, 2.9f, 0.1f, 0.06f, 5), vase(1.25f, 0.955f, 0.18f),
            egg(0.5f, 0.955f, 0.06f, 1), egg(0.62f, 0.955f, 0.05f, 3),
        ),
    ),
    PlaceId.SALON to Feast(
        back = listOf(bunting(-0.3f, 0.04f, 1.5f, 0.04f, 0.05f, 12), bunting(1.5f, 0.04f, 3.3f, 0.04f, 0.05f, 12), vase(1.6f, 0.955f, 0.17f), egg(0.2f, 0.955f, 0.06f, 4)),
    ),
    PlaceId.SHOP to Feast(
        back = listOf(
            bunting(-0.3f, 0.08f, 1.8f, 0.08f, 0.06f, 14), bunting(1.8f, 0.08f, 3.7f, 0.08f, 0.06f, 14),
            egg(1.2f, 0.84f, 0.07f, 3), egg(1.3f, 0.84f, 0.07f, 4), egg(1.25f, 0.84f, 0.05f, 0),
        ),
    ),
    PlaceId.DOCTOR to Feast(
        back = listOf(bunting(-0.3f, 0.04f, 1.5f, 0.04f, 0.05f, 12), bunting(1.5f, 0.04f, 3.3f, 0.04f, 0.05f, 12), vase(1.15f, 0.955f, 0.16f), egg(2.0f, 0.955f, 0.06f, 0)),
    ),
    PlaceId.STAGE to Feast(
        back = listOf(bunting(-0.3f, 0.05f, 1.6f, 0.05f, 0.06f, 12), bunting(1.6f, 0.05f, 3.5f, 0.05f, 0.06f, 12), egg(2.4f, 0.85f, 0.07f, 2)),
    ),
    PlaceId.LAB to Feast(
        back = listOf(bunting(-0.3f, 0.04f, 1.5f, 0.04f, 0.05f, 12), bunting(1.5f, 0.04f, 3.1f, 0.04f, 0.05f, 12), egg(2.4f, 0.85f, 0.06f, 4)),
    ),
    PlaceId.SPACE to Feast(
        back = listOf(bunting(-0.3f, 0.04f, 1.8f, 0.04f, 0.05f, 12), bunting(1.8f, 0.04f, 3.9f, 0.04f, 0.05f, 12)),
    ),
    PlaceId.UNDERWATER to Feast(
        back = listOf(egg(0.9f, 0.93f, 0.07f, 0), egg(1.7f, 0.94f, 0.06f, 3), egg(2.6f, 0.93f, 0.07f, 2), egg(3.4f, 0.94f, 0.06f, 1)),
    ),
    PlaceId.FOREST to Feast(
        back = listOf(
            egg(0.62f, 0.84f, 0.05f, 0), egg(1.15f, 0.9f, 0.05f, 1), egg(1.78f, 0.86f, 0.05f, 2), egg(2.1f, 0.93f, 0.05f, 3),
            tulip(0.1f, 0.84f, 0.1f, 0), tulip(0.16f, 0.85f, 0.09f, 1),
        ),
    ),
    PlaceId.BEACH to Feast(
        back = listOf(egg(0.7f, 0.86f, 0.05f, 0), egg(1.3f, 0.9f, 0.05f, 2), egg(1.9f, 0.88f, 0.05f, 3), tulip(0.1f, 0.82f, 0.1f, 2)),
    ),
    PlaceId.MOUNTAIN to Feast(
        back = listOf(egg(0.8f, 0.88f, 0.05f, 1), egg(1.5f, 0.9f, 0.05f, 4), egg(2.5f, 0.88f, 0.05f, 2), egg(3.3f, 0.9f, 0.05f, 0)),
    ),
    PlaceId.FARM to Feast(
        back = listOf(
            bunting(0f, 0.38f, 0.6f, 0.2f, 0.02f, 6, 0.03f), bunting(0.6f, 0.2f, 1.2f, 0.38f, 0.02f, 6, 0.03f),
            egg(1.5f, 0.84f, 0.05f, 0), egg(2.3f, 0.86f, 0.05f, 2), egg(3.1f, 0.84f, 0.05f, 1),
            tulip(1.4f, 0.74f, 0.09f, 0), tulip(1.47f, 0.74f, 0.09f, 3), tulip(3.5f, 0.74f, 0.09f, 1),
        ),
    ),
    PlaceId.TIVOLI to Feast(
        back = listOf(
            bunting(-0.3f, 0.12f, 1.2f, 0.12f, 0.05f, 12), bunting(1.2f, 0.12f, 2.5f, 0.12f, 0.05f, 12), bunting(2.5f, 0.12f, 3.4f, 0.17f, 0.05f, 9),
            egg(1.3f, 0.88f, 0.06f, 1), egg(2.0f, 0.9f, 0.06f, 3),
        ),
    ),
    PlaceId.HEILEBERGET to Feast(
        back = listOf(egg(1.2f, 0.86f, 0.05f, 0), egg(2.2f, 0.9f, 0.05f, 2), egg(3.4f, 0.88f, 0.05f, 1), egg(5.4f, 0.9f, 0.05f, 3), egg(7.0f, 0.88f, 0.05f, 4)),
    ),
)

// --------------------------------------------------------------------------------------------- Pumpkin

private val pumpkins: Map<PlaceId, Feast> = mapOf(
    PlaceId.HOME to Feast(
        back = listOf(
            web(0.0f, 0.03f, 0.2f, 1f), web(4.2f, 0.03f, 0.2f, -1f),
            bat(0.7f, 0.03f, 0.1f, 0.06f, 0f), bat(2.9f, 0.03f, 0.13f, 0.06f, 2f), ghost(1.5f, 0.03f, 0.12f, 0.12f, 1f),
            pumpkin(0.5f, 0.955f, 0.1f, true), pumpkin(2.0f, 0.96f, 0.08f, false),
        ),
    ),
    PlaceId.CAFE to Feast(
        back = listOf(
            web(0.0f, 0.03f, 0.2f, 1f), web(3.0f, 0.03f, 0.2f, -1f),
            bat(1.1f, 0.1f, 0.1f, 0.06f, 1f), ghost(2.0f, 0.1f, 0.1f, 0.12f, 2f), lantern(2.65f, 0.1f, 0.1f, 0f),
            pumpkin(0.55f, 0.955f, 0.1f, true), pumpkin(1.3f, 0.96f, 0.08f, false),
        ),
    ),
    PlaceId.SALON to Feast(
        back = listOf(web(0.0f, 0.03f, 0.2f, 1f), web(2.8f, 0.03f, 0.2f, -1f), bat(1.3f, 0.03f, 0.1f, 0.06f, 0f), ghost(2.1f, 0.03f, 0.12f, 0.12f, 3f), pumpkin(1.3f, 0.955f, 0.1f, true), pumpkin(2.0f, 0.96f, 0.08f, false)),
    ),
    PlaceId.SHOP to Feast(
        back = listOf(web(0.0f, 0.03f, 0.2f, 1f), web(3.4f, 0.03f, 0.2f, -1f), ghost(1.6f, 0.08f, 0.12f, 0.12f, 1f), bat(2.6f, 0.08f, 0.1f, 0.06f, 2f), pumpkin(2.2f, 0.955f, 0.1f, true), pumpkin(0.9f, 0.96f, 0.08f, false)),
    ),
    PlaceId.DOCTOR to Feast(
        back = listOf(web(0.0f, 0.03f, 0.2f, 1f), web(3.0f, 0.03f, 0.2f, -1f), ghost(2.3f, 0.03f, 0.1f, 0.12f, 1f), bat(0.9f, 0.03f, 0.1f, 0.06f, 2f), pumpkin(1.25f, 0.955f, 0.1f, true), pumpkin(2.0f, 0.96f, 0.08f, false)),
    ),
    PlaceId.STAGE to Feast(
        back = listOf(web(0.0f, 0.03f, 0.2f, 1f), web(3.2f, 0.03f, 0.2f, -1f), bat(1.0f, 0.04f, 0.12f, 0.07f, 0f), bat(2.4f, 0.04f, 0.1f, 0.06f, 2f), pumpkin(1.5f, 0.96f, 0.1f, true), pumpkin(0.5f, 0.96f, 0.08f, false)),
    ),
    PlaceId.LAB to Feast(
        back = listOf(web(0.0f, 0.03f, 0.2f, 1f), web(2.8f, 0.03f, 0.2f, -1f), bat(1.2f, 0.03f, 0.12f, 0.06f, 1f), ghost(2.4f, 0.03f, 0.1f, 0.12f, 2f), pumpkin(0.6f, 0.96f, 0.1f, true), pumpkin(1.3f, 0.96f, 0.08f, false)),
    ),
    PlaceId.SPACE to Feast(
        back = listOf(web(0.0f, 0.03f, 0.2f, 1f), web(3.6f, 0.03f, 0.2f, -1f), ghost(1.5f, 0.04f, 0.14f, 0.12f, 1f), bat(2.7f, 0.04f, 0.1f, 0.06f, 2f), pumpkin(0.75f, 0.96f, 0.1f, true), pumpkin(2.4f, 0.96f, 0.08f, false)),
    ),
    PlaceId.UNDERWATER to Feast(
        back = listOf(pumpkin(1.0f, 0.93f, 0.08f, true), pumpkin(3.0f, 0.93f, 0.08f, true), ghost(2.0f, 0.1f, 0.2f, 0.12f, 1f)),
    ),
    PlaceId.FOREST to Feast(
        back = listOf(pumpkin(0.62f, 0.96f, 0.1f, true), pumpkin(1.05f, 0.96f, 0.08f, false), pumpkin(2.05f, 0.955f, 0.1f, true), lantern(1.1f, 0.2f, 0.1f, 0f), lantern(2.0f, 0.26f, 0.1f, 2f)),
    ),
    PlaceId.BEACH to Feast(
        back = listOf(pumpkin(0.7f, 0.96f, 0.1f, true), pumpkin(1.5f, 0.96f, 0.08f, false), pumpkin(1.9f, 0.96f, 0.1f, true)),
    ),
    PlaceId.MOUNTAIN to Feast(
        back = listOf(pumpkin(0.5f, 0.96f, 0.1f, true), pumpkin(1.5f, 0.96f, 0.08f, false), pumpkin(3.3f, 0.96f, 0.1f, true)),
    ),
    PlaceId.FARM to Feast(
        back = listOf(pumpkin(1.9f, 0.955f, 0.1f, true), pumpkin(3.0f, 0.955f, 0.1f, true), pumpkin(3.3f, 0.96f, 0.08f, false), ghost(2.0f, 0.1f, 0.2f, 0.12f, 1f), lantern(0.6f, 0.3f, 0.08f, 1f)),
    ),
    PlaceId.TIVOLI to Feast(
        back = listOf(pumpkin(1.3f, 0.96f, 0.1f, true), pumpkin(2.0f, 0.96f, 0.1f, true), bat(1.0f, 0.1f, 0.18f, 0.07f, 0f), bat(2.7f, 0.1f, 0.2f, 0.07f, 2f), ghost(1.9f, 0.1f, 0.2f, 0.13f, 1f)),
    ),
    PlaceId.HEILEBERGET to Feast(
        back = listOf(pumpkin(1.3f, 0.96f, 0.1f, true), pumpkin(2.6f, 0.96f, 0.1f, true), pumpkin(5.4f, 0.96f, 0.1f, true), lantern(1.6f, 0.4f, 0.1f, 0f)),
    ),
)

// -------------------------------------------------------------------------------------------- drawing

internal fun DrawScope.feastBack(place: PlaceId, st: Stage, pen: Pen) {
    val f = feastOf(pen.festival)[place] ?: return
    drawDecos(st, pen, f.back)
}

internal fun DrawScope.feastFront(place: PlaceId, st: Stage, pen: Pen) {
    val f = feastOf(pen.festival)[place] ?: return
    drawDecos(st, pen, f.front)
    if (!place.outdoor) cosyGlow(st, pen)
}

/** The places a feast has decorations for (the tests check that none is forgotten). */
internal fun feastPlaces(f: Festival): Set<PlaceId> = feastOf(f).keys

private fun feastOf(f: Festival): Map<PlaceId, Feast> = when (f) {
    Festival.CHRISTMAS -> christmas
    Festival.EASTER -> easter
    Festival.PUMPKIN -> pumpkins
    Festival.NONE -> emptyMap()
}

private fun DrawScope.drawDecos(st: Stage, pen: Pen, list: List<D>) {
    val u = st.u
    for (d in list) {
        val lo = min(d.x, d.x2) - d.s - 0.2f
        val hi = max(d.x, d.x2) + d.s + 0.2f
        if (d.k != K.SWAGS && !st.sees(lo, hi)) continue
        val c = st.o(d.x, d.y)
        when (d.k) {
            K.TREE -> xmasTree(c, d.s * u, pen)
            K.PRESENTS -> presents(c, d.s * u, pen)
            K.WREATH -> wreath(c, d.s * u, pen)
            K.STAR -> adventStar(c, d.s * u, pen, d.a * u)
            K.STRAND -> lightStrand(c, st.o(d.x2, d.y2), d.a * u, d.n, 0.007f * u, pen)
            K.SWAGS -> swags(st, pen, d)
            K.EGG -> egg(c, d.s * u * 1.25f, pen, d.n)
            K.TULIP -> tulip(c, d.s * u, pen, d.n, sway = 0.03f * kotlin.math.sin(pen.t * 1.2f + d.x * 5f))
            K.BUNTING -> bunting(c, st.o(d.x2, d.y2), d.a * u, d.n, d.s * u, pen)
            K.EGGS -> eggGarland(c, st.o(d.x2, d.y2), d.a * u, d.n, d.s * u, pen)
            K.VASE -> twigVase(c, d.s * u, pen)
            K.PUMPKIN -> pumpkin(c, d.s * u * 1.25f, pen, d.n == 1, 0f, pen.night)
            K.LANTERN -> lantern(c, d.s * u, pen, d.a)
            K.BAT -> bat(c, d.y2 * u, d.s * u * 1.3f, pen, d.a)
            K.GHOST -> ghost(c, d.y2 * u, d.s * u * 1.3f, pen, d.a)
            K.WEB -> cobweb(c, d.s * u, d.a, pen)
            K.PINE -> pineLights(st, pen, d)
            K.ROOF -> roofLights(st, pen, d)
        }
    }
}

/** Swags of lights along the top of the wall, one every 0.7 units, from [d].x to [d].x2. */
private fun DrawScope.swags(st: Stage, pen: Pen, d: D) {
    val u = st.u
    val w = 0.7f
    val first = ((max(d.x, st.left - w) - d.x) / w).toInt()
    var x = d.x + first * w
    while (x < min(d.x2, st.right)) {
        val b = min(x + w, d.x2)
        lightStrand(st.o(x, d.y), st.o(b, d.y), d.a * u * ((b - x) / w), if (d.n > 0) d.n else 9, 0.0075f * u, pen, phase = x)
        x += w
    }
}

/** Lights on a pine: three swags across its tiers and a star on top. */
private fun DrawScope.pineLights(st: Stage, pen: Pen, d: D) {
    val u = st.u
    val h = d.s
    val hw = h * 0.25f
    for (k in 0 until 3) {
        val y = d.y - h * (0.22f + 0.26f * k)
        val half = hw * (1f - 0.28f * k)
        lightStrand(st.o(d.x - half, y), st.o(d.x + half, y), 0.03f * u * (1f - 0.2f * k), 7 - k, 0.007f * u, pen, phase = k.toFloat())
    }
    adventStar(st.o(d.x, d.y - h * 1.02f), 0.035f * u, pen)
}

/** Lights along a roof line given in scene (x, y) pairs. */
private fun DrawScope.roofLights(st: Stage, pen: Pen, d: D) {
    val pts = roofLines[d] ?: return
    val u = st.u
    var i = 0
    while (i < pts.size - 2) {
        val a = Offset(st.x(pts[i]), pts[i + 1] * u)
        val b = Offset(st.x(pts[i + 2]), pts[i + 3] * u)
        lightStrand(a, b, 0.012f * u, max(4, (abs(b.x - a.x) / (0.07f * u)).toInt()), 0.007f * u, pen, phase = i.toFloat())
        i += 2
    }
}

/** The warm glow of a cosy room: a faint gold wash over indoor scenes at Christmas, stronger at night. */
internal fun DrawScope.cosyGlow(st: Stage, pen: Pen) {
    if (pen.festival != Festival.CHRISTMAS) return
    drawRect(Color(0xFFFFB347), Offset(0f, SKY_TOP * st.u), androidx.compose.ui.geometry.Size(st.w, st.h - SKY_TOP * st.u), alpha = 0.03f + 0.05f * pen.night)
}
