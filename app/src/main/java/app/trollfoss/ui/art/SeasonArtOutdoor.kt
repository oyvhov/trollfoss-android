package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season
import kotlin.math.floor
import kotlin.math.sin

/*
 * What the seasons add to the outdoor places, on top of the colours they already take from the palette
 * (SeasonKit.kt): leaves on the ground, flowers, drifts and sparkles (GroundCover), little brooks in
 * spring, ice on the pool and the sea in winter, mist on autumn mornings, bare, golden or blossoming trees
 * where a place has none of its own. Everything is built once per scale; only a few lines move.
 */

internal fun DrawScope.seasonScenery(place: PlaceId, st: Stage, pen: Pen) {
    when (place) {
        PlaceId.FOREST -> forestSeason(st, pen)
        PlaceId.BEACH -> beachSeason(st, pen)
        PlaceId.MOUNTAIN -> mountainSeason(st, pen)
        PlaceId.FARM -> farmSeason(st, pen)
        PlaceId.TIVOLI -> tivoliSeason(st, pen)
        PlaceId.HEILEBERGET -> bergSeason(st, pen)
        else -> Unit
    }
}

private val edgeCovers: Map<PlaceId, GroundCover> = mapOf(
    PlaceId.BEACH to GroundCover(0f, 2.3f, 0.963f, 0.985f, 1021, 0.7f),
    PlaceId.FOREST to GroundCover(0f, 2.5f, 0.963f, 0.985f, 1022, 0.7f),
    PlaceId.MOUNTAIN to GroundCover(0f, 3.8f, 0.963f, 0.985f, 1023, 0.7f),
    PlaceId.FARM to GroundCover(0f, 4.4f, 0.963f, 0.985f, 1024, 0.7f),
    PlaceId.TIVOLI to GroundCover(0f, 4.4f, 0.963f, 0.985f, 1025, 0.7f),
    PlaceId.HEILEBERGET to GroundCover(0f, 9f, 0.963f, 0.985f, 1026, 0.6f),
)

/** In front of everything: a few leaves or flowers along the front edge, for depth. */
internal fun DrawScope.seasonSceneryFront(place: PlaceId, st: Stage, pen: Pen) {
    if (pen.season == Season.SPRING || pen.season == Season.AUTUMN) edgeCovers[place]?.draw(this, st, pen)
}

// ============================================================================================ FOREST

private val forestCover = GroundCover(0.05f, 2.42f, 0.805f, 0.96f, 1001)
private val forestBrook = Brook(floatArrayOf(1.95f, 0.805f, 2.1f, 0.83f, 2.2f, 0.862f, 2.38f, 0.9f, 2.52f, 0.915f, 2.62f, 0.925f))

private class ForestIce(val sheet: Path, val edge: Path, val cracks: List<Offset>, val icicles: Path)

private val forestIce = Memo { u ->
    val sheet = Path().apply { poly(3.34f * u, 0.788f * u, 4.6f * u, 0.788f * u, 4.6f * u, 0.832f * u, 3.24f * u, 0.832f * u, 3.3f * u, 0.81f * u) }
    val edge = Path().apply { poly(3.24f * u, 0.83f * u, 4.6f * u, 0.83f * u, 4.6f * u, 0.842f * u, 3.24f * u, 0.842f * u) }
    val c = floatArrayOf(
        3.55f, 0.795f, 3.64f, 0.82f, 3.64f, 0.82f, 3.6f, 0.83f, 3.92f, 0.792f, 3.84f, 0.822f,
        4.12f, 0.8f, 4.26f, 0.826f, 3.7f, 0.81f, 3.96f, 0.806f, 4.38f, 0.795f, 4.34f, 0.815f,
    )
    val cracks = ArrayList<Offset>(c.size / 2)
    for (i in c.indices step 2) cracks.add(Offset(c[i] * u, c[i + 1] * u))
    // Icicles under the rock ledges of the cliff: (left, right, front edge) of each ledge.
    val l = floatArrayOf(2.58f, 2.8f, 0.27f, 2.62f, 2.8f, 0.61f, 3.26f, 3.52f, 0.33f, 3.3f, 3.6f, 0.73f, 3.7f, 4.0f, 0.46f)
    val xs = ArrayList<Float>()
    val ys = ArrayList<Float>()
    val ls = ArrayList<Float>()
    for (i in l.indices step 3) {
        val n = ((l[i + 1] - l[i]) / 0.045f).toInt()
        for (k in 0 until n) {
            xs.add((l[i] + (k + 0.5f) * (l[i + 1] - l[i]) / n) * u)
            ys.add(l[i + 2] * u)
            ls.add((0.014f + 0.02f * hash01(k + i * 7, 1031)) * u)
        }
    }
    ForestIce(sheet, edge, cracks, iciclePath(xs.toFloatArray(), ys.toFloatArray(), ls.toFloatArray(), 0.006f * u))
}

private fun DrawScope.forestSeason(st: Stage, pen: Pen) {
    forestCover.draw(this, st, pen)
    when (pen.season) {
        Season.SPRING -> forestBrook.draw(this, st, pen, 0.016f)
        Season.AUTUMN -> mistBand(st, pen, 0.79f, 0.09f, 0.5f, 0.6f, 1011)
        Season.WINTER -> if (st.sees(2.45f, 4.6f)) {
            val g = forestIce.of(st.u)
            inScene(st) {
                iceSheet(g.sheet, g.cracks, pen)
                drawPath(g.edge, SeasonPal.ice.atNight(pen.night, 0.45f), alpha = 0.8f)
                drawPath(g.edge, Ink.line, alpha = 0.4f, style = pen.thin)
                icicles(g.icicles, pen)
            }
        }
        Season.SUMMER -> Unit
    }
}

// ============================================================================================= BEACH

private val beachSand = GroundCover(0.05f, 2.2f, 0.80f, 0.95f, 1002, 0.8f)
private val beachDune = GroundCover(0.0f, 2.2f, 0.795f, 0.82f, 1003, 0.9f)

private class SeaIce(val floes: Path, val shore: Path)

private val seaIce = Memo { u ->
    val floes = Path()
    for (i in 0 until 12) {
        val x = -0.2f + hash01(i, 1021) * 4.2f
        val f = hash01(i, 1022)
        val rx = mix(0.045f, 0.13f, f)
        floes.floorDisc(u, 0f, x, mix(0.54f, 0.765f, f), rx, rx * 0.5f, 12)
    }
    val shore = Path()
    var x = -0.4f
    shore.moveTo(x * u, (0.772f + 0.004f * sin(x * 9f)) * u)
    while (x < 4.6f) {
        x += 0.1f
        shore.lineTo(x * u, (0.772f + 0.004f * sin(x * 9f)) * u)
    }
    while (x > -0.4f) {
        shore.lineTo(x * u, (0.7845f + 0.004f * sin(x * 7f + 1f)) * u)
        x -= 0.1f
    }
    shore.close()
    SeaIce(floes, shore)
}

private fun DrawScope.beachSeason(st: Stage, pen: Pen) {
    val u = st.u
    when (pen.season) {
        Season.SPRING -> beachDune.draw(this, st, pen)
        Season.AUTUMN -> {
            beachSand.draw(this, st, pen)
            mistBand(st, pen, 0.57f, 0.07f, 0.6f, 0.1f, 1012)
        }
        Season.WINTER -> {
            beachSand.draw(this, st, pen)
            val g = seaIce.of(u)
            val n = pen.night
            inScene(st) {
                translate(0f, sin(pen.t * 1.1f) * 0.0015f * u) {
                    translate(0f, 0.005f * u) { drawPath(g.floes, SeasonPal.iceDeep.atNight(n, 0.5f)) }
                    drawPath(g.floes, Color(0xFFF2FAFF).atNight(n, 0.45f))
                    drawPath(g.floes, Ink.line, alpha = 0.35f, style = pen.thin)
                }
                drawPath(g.shore, Color(0xFFF2FAFF).atNight(n, 0.45f), alpha = 0.92f)
                drawPath(g.shore, Ink.line, alpha = 0.3f, style = pen.thin)
            }
        }
        Season.SUMMER -> Unit
    }
}

// =========================================================================================== MOUNTAIN

private val mountainPatchSpecs = floatArrayOf(0.4f, 0.9f, 0.12f, 1.25f, 0.86f, 0.1f, 2.05f, 0.93f, 0.13f, 3.0f, 0.88f, 0.12f, 3.55f, 0.93f, 0.1f, 0.15f, 0.84f, 0.07f)

/** The flowers grow on the green patches where the snow has melted. */
private val mountainFlowers: List<GroundCover> = (0 until mountainPatchSpecs.size / 3).map { i ->
    val x = mountainPatchSpecs[i * 3]
    val y = mountainPatchSpecs[i * 3 + 1]
    val r = mountainPatchSpecs[i * 3 + 2]
    GroundCover(x - r * 0.75f, x + r * 0.75f, y - 0.012f, y + 0.012f, 1007 + i, 3f)
}
private val mountainBrookA = Brook(floatArrayOf(0.88f, 0.792f, 0.97f, 0.84f, 0.9f, 0.9f, 1.0f, 0.965f))
private val mountainBrookB = Brook(floatArrayOf(2.94f, 0.792f, 2.84f, 0.85f, 2.96f, 0.91f, 2.86f, 0.968f))

private val mountainPatches = Memo { u ->
    val p = Path()
    val s = mountainPatchSpecs
    for (i in s.indices step 3) p.floorDisc(u, 0f, s[i], s[i + 1], s[i + 2], 0.05f, 16)
    p
}

private fun DrawScope.mountainSeason(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    when (pen.season) {
        Season.SPRING -> {
            if (st.sees(0f, 3.8f)) {
                val p = mountainPatches.of(u)
                inScene(st) {
                    drawPath(p, Color(0xFF8CCB62).atNight(n, 0.45f), alpha = 0.9f)
                    drawPath(p, Ink.line, alpha = 0.3f, style = pen.thin)
                }
                for (c in mountainFlowers) c.draw(this, st, pen)
                mountainBrookA.draw(this, st, pen, 0.012f)
                mountainBrookB.draw(this, st, pen, 0.012f)
            }
        }
        Season.AUTUMN -> mountainTrees(st, pen)
        else -> Unit
    }
}

/** In the gaps between the snowy pines of the slope stand a few golden trees (in spring, blossoming ones). */
private fun DrawScope.mountainTrees(st: Stage, pen: Pen) {
    val u = st.u
    val sp = 0.5f
    val spacing = 0.36f
    val l0 = st.cam * sp - 0.3f
    val l1 = st.cam * sp + st.vw + 0.3f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        val gap = hash01(i, 111) < 0.3f
        if (!gap && i % 3 != 0) continue
        val lx = i * spacing + (hash01(i, 112) - 0.5f) * 0.14f + if (gap) 0f else 0.13f
        val h = (0.15f + 0.06f * hash01(i, 113)) * u
        seasonTree(st.px(lx, sp), (ridgeY(lx, 0.775f, 0.03f, 37) + 0.01f) * u, h, pen, i + 5, pen.night)
    }
}

// ============================================================================================== FARM

private val farmCoverA = GroundCover(1.4f, 3.3f, 0.76f, 0.865f, 1004)
private val farmCoverB = GroundCover(3.4f, 4.4f, 0.795f, 0.86f, 1008)
private val farmCoverC = GroundCover(0.75f, 4.4f, 0.94f, 0.965f, 1005)
private val farmFence = GroundCover(1.35f, 4.4f, 0.725f, 0.75f, 1009, 1.2f)

private fun DrawScope.farmSeason(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    farmCoverA.draw(this, st, pen)
    farmCoverB.draw(this, st, pen)
    farmCoverC.draw(this, st, pen)
    farmFence.draw(this, st, pen)
    when (pen.season) {
        Season.AUTUMN -> {
            // A little pumpkin patch by the barn.
            for ((i, p) in listOf(Triple(1.04f, 0.806f, 0.03f), Triple(1.14f, 0.816f, 0.022f), Triple(1.21f, 0.802f, 0.027f)).withIndex()) {
                if (st.sees(p.first - 0.05f, p.first + 0.05f)) pumpkin(st.o(p.first, p.second), p.third * u * 1.7f, pen, face = i == 0, glow = 0f, night = n)
            }
        }
        Season.WINTER -> {
            val white = Color.White.atNight(n, 0.4f)
            inScene(st) {
                for (y in floatArrayOf(0.68f, 0.7f)) drawRect(white, Offset(-0.4f * u, (y - 0.0035f) * u), Size(5.2f * u, 0.006f * u))
            }
            var px = -0.3f
            while (px < 4.8f) {
                if (st.sees(px - 0.03f, px + 0.03f)) snowCap(st.x(px - 0.012f), st.x(px + 0.012f), 0.668f * u, 0.006f * u, pen)
                px += 0.25f
            }
        }
        else -> Unit
    }
}

// ============================================================================================ TIVOLI

private val tivoliLitter = GroundCover(-0.2f, 3.1f, 0.805f, 0.96f, 1010)
private val tivoliBeds = GroundCover(-0.2f, 4.4f, 0.768f, 0.792f, 1011, 1.4f)

private fun DrawScope.tivoliSeason(st: Stage, pen: Pen) {
    val u = st.u
    tivoliLitter.draw(this, st, pen)
    tivoliBeds.draw(this, st, pen)
    // Trees between the tents far back in the park; in the plain summer look the park has none.
    val p = 0.5f
    val spacing = 0.42f
    val l0 = st.cam * p - 0.3f
    val l1 = st.cam * p + st.vw + 0.3f
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        val skip = hash01(i, 911) < 0.15f
        val lx = i * spacing + (if (skip) 0f else spacing * 0.5f) + (hash01(i, 916) - 0.5f) * 0.06f
        val bx = st.px(lx, p)
        if (bx < -0.2f * u || bx > st.w + 0.2f * u) continue
        seasonTree(bx, (ridgeY(lx, 0.77f, 0.006f, 55) + 0.003f) * u, (0.17f + 0.05f * hash01(i, 917)) * u, pen, i + 9, pen.night)
    }
}

// ======================================================================================= HEILEBERGET

private val bergTop = GroundCover(-0.2f, 9.4f, 0.76f, 0.82f, 1012, 0.8f)
private val bergEdgeCover = GroundCover(-0.2f, 9.4f, 0.95f, 0.972f, 1013, 0.8f)

private fun DrawScope.bergSeason(st: Stage, pen: Pen) {
    bergTop.draw(this, st, pen)
    bergEdgeCover.draw(this, st, pen)
    if (pen.season == Season.AUTUMN) mistBand(st, pen, 0.68f, 0.09f, 0.3f, 0.7f, 1014)
}
