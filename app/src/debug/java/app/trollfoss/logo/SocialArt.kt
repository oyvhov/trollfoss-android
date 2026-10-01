package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.ui.art.Ink

/**
 * The GitHub social preview, 1280 x 640: the emblem on the left, the wordmark and tagline on the right over a
 * low range of snowy mountains, and part of the cast standing in the meadow along the bottom. Everything stays
 * 40 px inside the edges because the preview is often cropped.
 */
internal fun DrawScope.drawSocial() {
    val k = size.width / 1280f
    withTransform({ scale(k, k, pivot = Offset.Zero) }) { social() }
}

private data class Standing(val who: Cast, val x: Float, val feetY: Float)

private val socialCast = listOf(
    Standing(Cast.HEDDA, 628f, 606f),
    Standing(Cast.ALVA, 742f, 600f),
    Standing(Cast.CAT, 842f, 612f),
    Standing(Cast.DOG, 934f, 606f),
    Standing(Cast.PUFFIN, 1020f, 610f),
    Standing(Cast.ELK, 1118f, 600f),
)

private fun DrawScope.social() {
    // Sky and a warm glow behind the emblem.
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF3B98EA), 0.35f to Color(0xFF6DBFF5), 0.62f to Color(0xFFBCE6FB), 0.84f to horizon,
            startY = 0f, endY = 560f,
        ),
        Offset(-20f, -20f), Size(1320f, 680f),
    )
    lgGlow(Offset(280f, 300f), 420f, Color(0xFFFFE08A), 0.6f)
    lgCloud(1120f, 70f, 40f, 4f)
    lgCloud(560f, 52f, 26f, 4f)
    lgCloud(1236f, 196f, 28f, 4f)

    // Far ranges and a low massif.
    translate(0f, 40f) {
        withTransform({ scale(0.8f, 0.8f, pivot = Offset(0f, 480f)) }) {
            worldFarRanges()
        }
        haze(300f, 480f, 0.55f, -40f, 1320f)
    }
    withTransform({
        translate(-290f, 130f)
        scale(1f, 0.5f, pivot = Offset(0f, 480f))
    }) {
        worldMassif()
    }
    translate(0f, 14f) {
        forestRow(-40f, 1320f, 508f, 14f, 9, 34f, 36f, 58f, Color(0xFF7FAAA0), null, skip = 0.1f)
        forestRow(-40f, 1320f, 530f, 12f, 13, 56f, 56f, 84f, Color(0xFF3F7D62), Color(0xFF356B53), skip = 0.12f)
    }
    worldMeadow(1280f, 536f)
    worldFlowers(1280f, 560f, 626f, 40)

    // The emblem on the left, with a soft shadow under it.
    drawCircle(Ink.shadow, 232f, Offset(274f, 320f))
    drawEmblemIn(44f, 80f, 464f)

    // The wordmark and the tagline.
    val size = 158f
    drawWordmark(Offset(584f, 200f), size, keyline = 0f)
    drawOutlinedText("Ei bygd under ein stor foss", Offset(588f, 336f), 42f, Fonts.black, Color.White, rim = 7f, drop = 2.5f)
    drawOutlinedText("for barn 4–10 år · Android · utan reklame", Offset(590f, 380f), 28f, Fonts.heavy, L.sunTop, rim = 5.5f, drop = 2f)

    for (p in socialCast.sortedBy { it.feetY }) drawCastMember(p.who, p.x, p.feetY, 780f, 4.5f, p.x * 0.01f)
    worldTufts(1280f, 618f, 18f)
}
