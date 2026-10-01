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

/** Copy only the persistent appearance: a worker must never read the live house while it is being built. */
internal fun MineHouse.mapSnapshot(): MineHouse = MineHouse().also {
    it.started = started; it.shape = shape; it.wall = wall; it.roof = roof; it.roofColor = roofColor
    it.door = door; it.windows = windows; it.chimney = chimney; it.flag = flag; it.upperBuilt = upperBuilt
    ground.copyInto(it.ground); upper.copyInto(it.upper)
}

/** The landmark's width, in the landmark scale of its plot. */
internal const val MAP_MINE_SCALE = 1.7f

/** How many modules wide the house is on the map at most. */
private const val MAP_COLUMNS = 2

/**
 * The house as the map shows it: the child's own walls, roof, door and windows, but never more than [MAP_COLUMNS]
 * modules wide. A long house squeezed into the landmark's width became a low dark shed; this keeps it a house, with
 * the rooms it has moved up beside the hall.
 */
private fun MineHouse.forMap(): MineHouse = MineHouse().also { m ->
    m.started = started; m.shape = shape; m.wall = wall; m.roof = roof; m.roofColor = roofColor
    m.door = door; m.windows = windows; m.chimney = chimney; m.flag = flag; m.upperBuilt = upperBuilt
    var column = 1
    for (i in 1 until ground.size) {
        if (ground[i] <= 0 || column >= MAP_COLUMNS) continue
        m.ground[column] = ground[i]
        m.upper[column] = upper[i]
        column++
    }
    m.upper[0] = upper[0]
}

/**
 * Draws the child's house standing with the middle of its front at ([cx], [base]), about [size] pixels wide, with the
 * map's line width [lw]. The still parts only (no smoke, no flag waving).
 */
internal fun DrawScope.drawMapMine(h: MineHouse, cx: Float, base: Float, size: Float, lw: Float, night: Float, winter: Boolean, lawn: Color = Color(0xFF6FAE5A)) {
    val pen = Pen(lw)
    // A lawn patch with a stone path.
    drawOval(lawn.atNight(night, 0.4f), Offset(cx - size * 0.62f, base - size * 0.07f), Size(size * 1.24f, size * 0.2f))
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
    val m = h.forMap()
    val width = max(mineHouseWidth(m), 1.6f)
    val k = size / width
    val left = cx - (mineColumns(m) * 1.2f * k) / 2f + (if (m.shape == 2) 0.25f * k else 0f)
    // Detail 1: with its door and windows, like the other houses on the map.
    drawMineHouse(m, left, base, k, pen, night = night, t = 0f, winter = winter, detail = 1)
}

/** The chimney's smoke, drawn live over the still map: [phase] runs from 0 to 1 and wraps. */
internal fun DrawScope.drawMapMineSmoke(h: MineHouse, cx: Float, base: Float, size: Float, phase: Float) {
    if (!h.started || !h.chimney || h.shape == 2) return
    val m = h.forMap()
    val width = max(mineHouseWidth(m), 1.6f)
    val k = size / width
    val left = cx - (mineColumns(m) * 1.2f * k) / 2f
    val x = left + 1.2f * k * 0.78f + 0.2f * k
    val top = base - mineHouseHeight(m) * k * 0.82f
    for (n in 0 until 3) {
        val p = (phase + n / 3f) % 1f
        drawCircle(Color.White.copy(alpha = 0.7f * (1f - p)), k * (0.03f + 0.05f * p), Offset(x + p * k * 0.14f, top - p * k * 0.3f))
    }
}
