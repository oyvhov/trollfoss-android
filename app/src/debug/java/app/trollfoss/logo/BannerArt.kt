package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform

/** The README header, 4:1: the world as a panorama, the wordmark and tagline on the left, the cast along the bottom. */
internal fun DrawScope.drawBanner() {
    val k = size.width / 2400f
    withTransform({ scale(k, k, pivot = Offset.Zero) }) { banner() }
}

private data class Placed(val who: Cast, val x: Float, val feetY: Float)

private val bannerCast = listOf(
    Placed(Cast.RUMLE, 1065f, 584f),
    Placed(Cast.OYVIND, 1262f, 572f),
    Placed(Cast.HEDDA, 1425f, 592f),
    Placed(Cast.ALVA, 1552f, 588f),
    Placed(Cast.CAT, 1676f, 598f),
    Placed(Cast.DOG, 1786f, 592f),
    Placed(Cast.PUFFIN, 1892f, 596f),
    Placed(Cast.ELK, 2002f, 586f),
)

private fun DrawScope.banner() {
    worldSky()
    worldSun(Offset(1086f, 124f), 54f)
    worldClouds()
    worldFarRanges()
    haze(330f, 480f, 0.55f)
    worldMassif()
    balloon(2270f, 128f, 40f)
    birds(listOf(Triple(1330f, 168f, 14f), Triple(1372f, 150f, 11f), Triple(1980f, 224f, 12f), Triple(1520f, 262f, 10f), Triple(2090f, 170f, 10f)))
    ferrisWheel(2032f, 330f, 84f)
    // Forest hills.
    forestRow(-40f, 2440f, 448f, 16f, 9, 36f, 40f, 66f, Color(0xFF7FAAA0), null, skip = 0.15f)
    forestRow(820f, 2440f, 468f, 14f, 13, 62f, 74f, 112f, Color(0xFF3F7D62), Color(0xFF356B53), skip = 0.1f)
    forestRow(-40f, 900f, 470f, 10f, 21, 90f, 46f, 70f, Color(0xFF4F8F6C), null, skip = 0.5f)
    worldFjord(2150f, 2440f)
    sailboat(2232f, 448f, 44f, 3.5f)
    worldFall()
    worldPool()
    worldVillage()
    for ((x, h) in listOf(1330f to 150f, 1392f to 120f, 1446f to 170f, 1486f to 112f, 1908f to 120f, 1946f to 92f)) {
        lgPine(x, 474f, h * 0.44f, h, if ((x / 10f).toInt() % 2 == 0) L.gran else L.granLight, 4f)
    }
    worldMeadow()
    worldFlowers(count = 60)

    // The cast, back to front.
    boulder(2318f, 598f, 176f, 100f, 5f)
    drawCastMember(Cast.GOAT, 2318f, 512f, CAST_UNIT, 5f, 0.6f)
    for (p in bannerCast.sortedBy { it.feetY }) drawCastMember(p.who, p.x, p.feetY, CAST_UNIT, 5.5f, p.x * 0.01f)
    worldTufts()
    worldForeground(listOf(18f, 214f, 2210f))

    drawBannerText()
}

/** The wordmark, the tagline and the small print, on the left. */
private fun DrawScope.drawBannerText() {
    val size = 176f
    drawWordmark(Offset(92f, 292f), size, keyline = 0f)
    drawOutlinedText("Ei bygd under ein stor foss", Offset(96f, 452f), 55f, Fonts.black, Color.White, rim = 9f, drop = 3f)
    drawOutlinedText("for barn 4\u201310 \u00e5r \u00b7 Android \u00b7 utan reklame", Offset(98f, 510f), 41f, Fonts.heavy, L.sunTop, rim = 7.5f, drop = 2.5f)
}
