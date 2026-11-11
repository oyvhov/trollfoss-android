package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Decor

/**
 * Wallpapers and floors for the home designer. Index 0 is a place's own look. The rooms and the panel
 * swatches draw the very same patterns (see WallpaperArt.kt), so a swatch shows exactly what the room gets.
 */
object DecorPalette {
    /** Base colour and motif colour of each wallpaper. */
    val walls = listOf(
        Color(0xFFF3E7D3) to Color(0xFFD9C3A0), // 0 the place's own
        Color(0xFFFFE0EA) to Color(0xFFF58FB2), // 1 pink with flowers
        Color(0xFFBFE3FA) to Color(0xFFFFFFFF), // 2 sky blue with clouds
        Color(0xFFD5F0E3) to Color(0xFF6FC39A), // 3 mint with leaves
        Color(0xFFFFF1BD) to Color(0xFFFFC93D), // 4 butter with stars
        Color(0xFFE7DAF8) to Color(0xFFBC9DEE), // 5 lilac with dots
        Color(0xFFFFE2CE) to Color(0xFFFFC2A0), // 6 peach with stripes
        Color(0xFF1D2A5E) to Color(0xFFFFE9A8), // 7 night sky with stars
        Color(0xFFFBF8F4) to Color(0xFFF59BB2), // 8 white with little hearts
        Color(0xFFC4ECE4) to Color(0xFF79CBBB), // 9 sea green with waves
        Color(0xFFFFF4F7) to Color(0xFFFF8FB6), // 10 candy stripes
        Color(0xFFD9A46C) to Color(0xFFA9713F), // 11 wood panels
        Color(0xFFA73748) to Color(0xFFFFD881), Color(0xFF235E4E) to Color(0xFF9BC69F),
        Color(0xFFDDEFF5) to Color(0xFF85B4CE), Color(0xFFFFF1DC) to Color(0xFFC95E6D),
        Color(0xFFFFE4E3) to Color(0xFFC95E6D), Color(0xFF3F6958) to Color(0xFFD7BE88),
        Color(0xFFC48F68) to Color(0xFFFFF1DC), Color(0xFF263D62) to Color(0xFFFFDA8C),
        Color(0xFFE5CADF) to Color(0xFFB386B4), Color(0xFFFEF8EE) to Color(0xFFA8B5BD),
        Color(0xFFB95056) to Color(0xFF7B303E), Color(0xFFB4D7CA) to Color(0xFF4A7D66),
    )

    /** 0 plain, 1 flowers, 2 clouds, 3 leaves, 4 stars, 5 dots, 6 stripes, 7 hearts, 8 waves, 9 panels. */
    fun wallMotif(index: Int): Int = when (index) {
        1 -> 1
        2 -> 2
        3 -> 3
        4, 7 -> 4
        5 -> 5
        6, 10 -> 6
        8 -> 7
        9 -> 8
        11 -> 9
        else -> 0
    }

    /** Base colour and line colour of each floor. */
    val floors = listOf(
        Color(0xFFD9A873) to Color(0xFFB9824C), // 0 the place's own
        Color(0xFF7E4E33) to Color(0xFF55321F), // 1 dark wood
        Color(0xFFEDE4D7) to Color(0xFFC7B9A5), // 2 white-washed wood
        Color(0xFFF4F2EC) to Color(0xFF2E2E38), // 3 black and white chequers
        Color(0xFFBFE0F7) to Color(0xFF7FB6EA), // 4 blue tiles
        Color(0xFFF6AFC7) to Color(0xFFE88BAC), // 5 pink carpet
        Color(0xFF8FCB7E) to Color(0xFF6FB164), // 6 green carpet
        Color(0xFFEFE9E3) to Color(0xFFB69AE6), // 7 terrazzo
        Color(0xFFAEB3BB) to Color(0xFF80868F), // 8 grey stone
        Color(0xFF993B49) to Color(0xFFD8A076), Color(0xFF30634F) to Color(0xFF9DBB8A),
        Color(0xFFDBBA8D) to Color(0xFFB08C67), Color(0xFFEEE3CD) to Color(0xFFBB947A),
        Color(0xFFB5D1DE) to Color(0xFFEEEFF2), Color(0xFFEEE7D8) to Color(0xFF4A7462),
        Color(0xFFC7B1B6) to Color(0xFF93515F), Color(0xFFEDD4AE) to Color(0xFFA87750),
    )

    init {
        check(walls.size == Decor.WALLS && floors.size == Decor.FLOORS)
        check(WALLPAPERS == Decor.WALLS - 1 && FLOORINGS == Decor.FLOORS - 1)
    }
}

/** A wallpaper swatch filling [rect]: exactly the pattern the room gets, with a strip of skirting. */
fun DrawScope.drawWallSwatch(index: Int, rect: Rect, pen: Pen) {
    if (index in 1..WALLPAPERS) {
        wallSwatch(index, rect, pen)
    } else {
        ownLook(rect, pen, DecorPalette.walls[0])
    }
    drawRect(Ink.line, rect.topLeft, rect.size, style = pen.stroke)
}

/** A floor swatch filling [rect]: a piece of the receding floor band, exactly as the room shows it. */
fun DrawScope.drawFloorSwatch(index: Int, rect: Rect, pen: Pen) {
    if (index in 1..FLOORINGS) {
        floorSwatch(index, rect)
    } else {
        ownLook(rect, pen, DecorPalette.floors[0])
    }
    drawRect(Ink.line, rect.topLeft, rect.size, style = pen.stroke)
}

/** «As it was»: the place's own look, shown as a little house on a plain ground. */
private fun DrawScope.ownLook(r: Rect, pen: Pen, colors: Pair<Color, Color>) {
    drawRect(colors.first, r.topLeft, r.size)
    val s = minOf(r.width, r.height) * 0.28f
    val c = r.center
    val house = Path().apply {
        moveTo(c.x - s, c.y - s * 0.1f)
        lineTo(c.x, c.y - s * 0.95f)
        lineTo(c.x + s, c.y - s * 0.1f)
        lineTo(c.x + s * 0.72f, c.y - s * 0.1f)
        lineTo(c.x + s * 0.72f, c.y + s * 0.75f)
        lineTo(c.x - s * 0.72f, c.y + s * 0.75f)
        lineTo(c.x - s * 0.72f, c.y - s * 0.1f)
        close()
    }
    drawPath(house, colors.second, style = Stroke(pen.lw * 1.6f, cap = StrokeCap.Round))
    drawRect(colors.second, Offset(c.x - s * 0.2f, c.y + s * 0.2f), Size(s * 0.4f, s * 0.55f))
}
