package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Entry point: paints the background (black or white, for the transparency trick in
 * scripts/Render-Logos.ps1) and then the chosen art.
 */
fun DrawScope.renderLogoArt(art: String, bg: String) {
    when (bg) {
        "white" -> drawRect(Color.White)
        "black" -> drawRect(Color.Black)
        else -> Unit
    }
    when (art) {
        "emblem" -> drawEmblem()
        "wordmark" -> drawWordmarkArt()
        "banner" -> drawBanner()
        "social" -> drawSocial()
        "icon" -> drawIconSheet()
        else -> drawEmblem()
    }
}

/** The wordmark alone, big in the middle of the canvas, on transparent. */
private fun DrawScope.drawWordmarkArt() {
    val size = size.width * 0.22f
    val w = wordmarkWidth(size)
    drawWordmark(Offset((this.size.width - w) / 2f, this.size.height * 0.69f), size, keyline = size * 0.022f)
}
