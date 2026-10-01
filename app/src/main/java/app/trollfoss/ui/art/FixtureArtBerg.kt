package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Thing

// The furniture of Heileberget. A stand-in until it is drawn: returning false makes the plain box draw.

fun DrawScope.drawBergBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean = false

fun DrawScope.drawBergFront(f: Fixture, u: Float, pen: Pen): Boolean = false
