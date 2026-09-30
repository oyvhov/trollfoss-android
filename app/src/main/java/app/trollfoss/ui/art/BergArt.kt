package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope

// Heileberget, the great long mountain: the place background. A stand-in until it is drawn properly.

internal fun DrawScope.bergBack(st: Stage, pen: Pen) {
    mountainBack(st, pen)
}

internal fun DrawScope.bergFront(st: Stage, pen: Pen) {
    mountainFront(st, pen)
}
