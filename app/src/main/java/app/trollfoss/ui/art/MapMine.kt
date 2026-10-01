package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.MineHouse
import kotlin.math.max

/*
 * Mitt hus on the map: the house the child has built as a small landmark (or, before anything is built, the plot with
 * its shed and sign). Drawn into the map's still layer, so the layer's key must hold [MineHouse.hash]; only the smoke
 * of the chimney moves (see [drawMapMineSmoke]).
 */

/** How many pixels wide the landmark is, for a map [mapW] pixels wide. */
internal fun mapMineSize(mapW: Float): Float = mapW * 0.085f

/**
 * Draws the child's house standing with the middle of its front at ([cx], [base]), about [size] pixels wide, with the
 * map's line width [lw]. The still parts only (no smoke, no flag waving).
 */
internal fun DrawScope.drawMapMine(h: MineHouse, cx: Float, base: Float, size: Float, lw: Float, night: Float, winter: Boolean) {
    val pen = Pen(lw)
    // A lawn patch with a stone path.
    drawOval(Color(0xFF6FAE5A).atNight(night, 0.4f), Offset(cx - size * 0.62f, base - size * 0.07f), Size(size * 1.24f, size * 0.2f))
    drawOval(Ink.line.copy(alpha = 0.5f), Offset(cx - size * 0.62f, base - size * 0.07f), Size(size * 1.24f, size * 0.2f), style = pen.thin)
    if (!h.started) {
        // The empty plot: four posts with string, a little shed and a sign.
        val w = size * 0.5f
        for (i in 0 until 2) for (j in 0 until 2) {
            val x = cx - w / 2f + i * w
            val y = base + j * size * 0.03f
            drawLine(Ink.line, Offset(x, y), Offset(x, y - size * 0.1f), strokeWidth = lw * 2f, cap = StrokeCap.Round)
            drawLine(Color(0xFFE3B27A), Offset(x, y), Offset(x, y - size * 0.1f), strokeWidth = lw, cap = StrokeCap.Round)
        }
        drawLine(Color(0xFFFFE680), Offset(cx - w / 2f, base - size * 0.08f), Offset(cx + w / 2f, base - size * 0.08f), strokeWidth = lw)
        val sx = cx + size * 0.42f
        quadFill(Color(0xFFB98650).atNight(night, 0.4f), pen, sx - size * 0.1f, base, sx + size * 0.1f, base, sx + size * 0.1f, base - size * 0.13f, sx - size * 0.1f, base - size * 0.13f, thin = true)
        triFill(Color(0xFF8E4A3A).atNight(night, 0.4f), pen, sx - size * 0.13f, base - size * 0.13f, sx + size * 0.13f, base - size * 0.13f, sx, base - size * 0.21f, thin = true)
        drawCircle(Color(0xFFFFC83D), size * 0.025f, Offset(cx - size * 0.45f, base - size * 0.13f))
        return
    }
    val width = max(mineHouseWidth(h), 1.6f)
    val k = size / width
    val left = cx - (mineColumns(h) * 1.2f * k) / 2f + (if (h.shape == 2) 0.25f * k else 0f)
    drawMineHouse(h, left, base, k, pen, night = night, t = 0f, winter = winter, detail = 0)
}

/** The chimney's smoke, drawn live over the still map: [phase] runs from 0 to 1 and wraps. */
internal fun DrawScope.drawMapMineSmoke(h: MineHouse, cx: Float, base: Float, size: Float, phase: Float) {
    if (!h.started || !h.chimney || h.shape == 2) return
    val width = max(mineHouseWidth(h), 1.6f)
    val k = size / width
    val left = cx - (mineColumns(h) * 1.2f * k) / 2f
    val x = left + 1.2f * k * 0.78f + 0.2f * k
    val top = base - mineHouseHeight(h) * k * 0.82f
    for (n in 0 until 3) {
        val p = (phase + n / 3f) % 1f
        drawCircle(Color.White.copy(alpha = 0.7f * (1f - p)), k * (0.03f + 0.05f * p), Offset(x + p * k * 0.14f, top - p * k * 0.3f))
    }
}
