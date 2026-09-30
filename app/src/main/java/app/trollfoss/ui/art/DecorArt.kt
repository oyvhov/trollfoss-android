package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import app.trollfoss.domain.Decor

/**
 * Wallpapers and floors for the home designer, as swatches for its panel. Index 0 is a place's own
 * look. The room backgrounds use the same palettes, so a swatch shows what the room will get.
 */
object DecorPalette {
    /** Base colour and motif colour of each wallpaper; the motif is chosen by [wallMotif]. */
    val walls = listOf(
        Color(0xFFF3E7D3) to Color(0xFFE2CFAF), // 0 the place's own
        Color(0xFFFFE3EE) to Color(0xFFFF8FB1), // 1 pink with flowers
        Color(0xFFDDF0FF) to Color(0xFF7CC4F5), // 2 sky blue with clouds
        Color(0xFFE6F6E0) to Color(0xFF7BC47F), // 3 mint with leaves
        Color(0xFFFFF4C9) to Color(0xFFFFC83D), // 4 butter with stars
        Color(0xFFEDE3FF) to Color(0xFFA98BF0), // 5 lilac with dots
        Color(0xFFFFE6D0) to Color(0xFFFF9F68), // 6 peach with stripes
        Color(0xFF2E3A5C) to Color(0xFFFFE08A), // 7 night sky with stars
        Color(0xFFF7F7F2) to Color(0xFF2B2140), // 8 white with little hearts
        Color(0xFFD9F2EC) to Color(0xFF3FB3A1), // 9 sea green with waves
        Color(0xFFFFF0F5) to Color(0xFFE85D75), // 10 candy stripes
        Color(0xFFE9DCC8) to Color(0xFFB8864F), // 11 wood panels
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

    val floors = listOf(
        Color(0xFFD9A066) to Color(0xFFB9824C), // 0 the place's own
        Color(0xFF8A5A3B) to Color(0xFF6E4630), // 1 dark wood
        Color(0xFFF1E3CF) to Color(0xFFD9C3A5), // 2 white-washed wood
        Color(0xFFFFFFFF) to Color(0xFF2B2140), // 3 black and white chequers
        Color(0xFF9FD3F0) to Color(0xFFFFFFFF), // 4 blue tiles
        Color(0xFFF7B6C8) to Color(0xFFF08DAA), // 5 pink carpet
        Color(0xFF9CD6A4) to Color(0xFF7CBF86), // 6 green carpet
        Color(0xFFE8E4DE) to Color(0xFFB9B0A6), // 7 terrazzo
        Color(0xFFC9D6E3) to Color(0xFF8FA2B8), // 8 grey stone
    )

    init {
        check(walls.size == Decor.WALLS && floors.size == Decor.FLOORS)
    }
}

/** A wallpaper swatch filling [rect]. */
fun DrawScope.drawWallSwatch(index: Int, rect: Rect, pen: Pen) {
    val (base, motif) = DecorPalette.walls[index.coerceIn(0, Decor.WALLS - 1)]
    drawRect(base, rect.topLeft, rect.size)
    clipRect(rect.left, rect.top, rect.right, rect.bottom) {
        val step = rect.width / 3f
        for (row in 0..3) for (col in 0..3) {
            val c = Offset(rect.left + (col + if (row % 2 == 0) 0.25f else 0.75f) * step, rect.top + (row + 0.5f) * step)
            val r = step * 0.18f
            when (DecorPalette.wallMotif(index)) {
                1 -> for (a in 0 until 5) drawCircle(motif, r * 0.6f, Offset(c.x + kotlin.math.cos(a * 1.2566f) * r, c.y + kotlin.math.sin(a * 1.2566f) * r))
                2 -> {
                    drawCircle(Color.White, r, c)
                    drawCircle(Color.White, r * 0.8f, Offset(c.x + r, c.y + r * 0.2f))
                }
                3 -> drawOval(motif, Offset(c.x - r, c.y - r * 0.5f), androidx.compose.ui.geometry.Size(r * 2f, r))
                4 -> drawPath(starPath(c, r, r * 0.45f), motif)
                5 -> drawCircle(motif, r * 0.55f, c)
                6 -> if (row == 0) drawRect(motif, Offset(rect.left + col * step, rect.top), androidx.compose.ui.geometry.Size(step * 0.45f, rect.height))
                7 -> {
                    val heart = Path().apply {
                        moveTo(c.x, c.y + r * 0.7f)
                        cubicTo(c.x - r * 1.3f, c.y - r * 0.1f, c.x - r * 0.5f, c.y - r, c.x, c.y - r * 0.3f)
                        cubicTo(c.x + r * 0.5f, c.y - r, c.x + r * 1.3f, c.y - r * 0.1f, c.x, c.y + r * 0.7f)
                        close()
                    }
                    drawPath(heart, motif)
                }
                8 -> if (col == 0) {
                    val wave = Path().apply {
                        moveTo(rect.left, c.y)
                        var x = rect.left
                        while (x < rect.right) {
                            quadraticTo(x + step * 0.25f, c.y - r, x + step * 0.5f, c.y)
                            quadraticTo(x + step * 0.75f, c.y + r, x + step, c.y)
                            x += step
                        }
                    }
                    drawPath(wave, motif, style = androidx.compose.ui.graphics.drawscope.Stroke(pen.lw * 1.2f))
                }
                9 -> if (row == 0) drawLine(motif, Offset(rect.left + col * step, rect.top), Offset(rect.left + col * step, rect.bottom), pen.lw)
                else -> Unit
            }
        }
    }
    drawRect(Ink.line, rect.topLeft, rect.size, style = pen.stroke)
}

/** A floor swatch filling [rect], seen a little in perspective. */
fun DrawScope.drawFloorSwatch(index: Int, rect: Rect, pen: Pen) {
    val (base, line) = DecorPalette.floors[index.coerceIn(0, Decor.FLOORS - 1)]
    drawRect(base, rect.topLeft, rect.size)
    clipRect(rect.left, rect.top, rect.right, rect.bottom) {
        when (index) {
            3, 4 -> {
                val n = 4
                val w = rect.width / n
                for (row in 0 until n) for (col in 0 until n) if ((row + col) % 2 == 1) drawRect(line, Offset(rect.left + col * w, rect.top + row * w), androidx.compose.ui.geometry.Size(w, w))
            }
            5, 6 -> for (k in 0 until 18) drawCircle(line, pen.lw * 0.6f, Offset(rect.left + (k * 37 % 100) / 100f * rect.width, rect.top + (k * 61 % 100) / 100f * rect.height))
            7 -> for (k in 0 until 26) drawCircle(listOf(line, Color(0xFFE8A0A0), Color(0xFF8FB7D9))[k % 3], pen.lw * 0.7f, Offset(rect.left + (k * 29 % 100) / 100f * rect.width, rect.top + (k * 53 % 100) / 100f * rect.height))
            8 -> {
                val w = rect.width / 2
                for (row in 0 until 3) for (col in 0 until 3) drawRect(line, Offset(rect.left + col * w - (row % 2) * w / 2, rect.top + row * rect.height / 3), androidx.compose.ui.geometry.Size(w, rect.height / 3), style = pen.thin)
            }
            else -> for (k in 1 until 4) drawLine(line, Offset(rect.left + k * rect.width / 4, rect.top), Offset(rect.left + k * rect.width / 4 - rect.width * 0.1f, rect.bottom), pen.lw * 0.8f)
        }
    }
    drawRect(Ink.line, rect.topLeft, rect.size, style = pen.stroke)
}
