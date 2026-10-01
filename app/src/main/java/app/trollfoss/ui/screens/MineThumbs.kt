package app.trollfoss.ui.screens

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Mine
import app.trollfoss.domain.MineHouse
import app.trollfoss.domain.RoomKind
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.MineC
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawMineDoor
import app.trollfoss.ui.art.drawMineHouse
import app.trollfoss.ui.art.drawMineWindow
import app.trollfoss.ui.art.drawRoomPreview
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.mineHouseHeight
import app.trollfoss.ui.art.mineHouseWidth
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.theme.T
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Small pictures for the builder: the template houses, the rooms, the doors and windows, and the button icons.

/** The icons of the builder, in the same 100 × 100 ink style as the other icon sets. */
object BuildIcons {
    private fun DrawScope.u(block: DrawScope.(s: Float, pen: Pen) -> Unit) {
        val s = size.minDimension / 100f
        val dx = (size.width - 100f * s) / 2f
        val dy = (size.height - 100f * s) / 2f
        translate(dx, dy) { block(s, Pen(lw = 5.5f * s)) }
    }

    /** A hammer: the build button. */
    val Hammer: DrawScope.() -> Unit = {
        u { s, pen ->
            drawLine(Ink.line, Offset(28f * s, 90f * s), Offset(62f * s, 40f * s), 15f * s, StrokeCap.Round)
            drawLine(Color(0xFFC98A55), Offset(28f * s, 90f * s), Offset(62f * s, 40f * s), 9f * s, StrokeCap.Round)
            rotate(34f, Offset(62f * s, 32f * s)) {
                inkedRound(Rect(34f * s, 14f * s, 90f * s, 42f * s), 8f * s, Color(0xFF9AA5B8), pen)
                drawLine(Color.White.copy(alpha = 0.7f), Offset(42f * s, 22f * s), Offset(78f * s, 22f * s), 4f * s, StrokeCap.Round)
            }
            drawPath(starPath(Offset(20f * s, 22f * s), 11f * s, 5f * s), T.SunTop)
            drawPath(starPath(Offset(20f * s, 22f * s), 11f * s, 5f * s), Ink.line, style = pen.thin)
        }
    }

    /** A house with a green plus: build a room. */
    val RoomPlus: DrawScope.() -> Unit = {
        u { s, pen ->
            val roof = Path().apply { moveTo(8f * s, 46f * s); lineTo(46f * s, 12f * s); lineTo(84f * s, 46f * s); close() }
            inked(roof, Color(0xFFD2443A), pen)
            inkedRound(Rect(16f * s, 44f * s, 76f * s, 88f * s), 4f * s, Color(0xFFFFC83D), pen)
            inkedRound(Rect(32f * s, 54f * s, 52f * s, 74f * s), 3f * s, Color(0xFF9CD2F2), pen, shade = false)
            inkedCircle(Offset(76f * s, 74f * s), 20f * s, T.Mint, pen)
            drawLine(Color.White, Offset(66f * s, 74f * s), Offset(86f * s, 74f * s), 6f * s, StrokeCap.Round)
            drawLine(Color.White, Offset(76f * s, 64f * s), Offset(76f * s, 84f * s), 6f * s, StrokeCap.Round)
        }
    }

    /** A crane with a floor on its hook: the second floor. */
    val Crane: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(14f * s, 20f * s, 24f * s, 92f * s), 2f * s, Color(0xFFE6B450), pen)
            inkedRound(Rect(14f * s, 14f * s, 92f * s, 26f * s), 3f * s, Color(0xFFE6B450), pen)
            drawLine(Ink.line, Offset(74f * s, 26f * s), Offset(74f * s, 44f * s), 4f * s, StrokeCap.Round)
            inkedRound(Rect(52f * s, 44f * s, 96f * s, 70f * s), 4f * s, Color(0xFFB7A3E3), pen)
            inkedRound(Rect(66f * s, 50f * s, 82f * s, 64f * s), 2f * s, Color(0xFF9CD2F2), pen, shade = false)
            inkedRound(Rect(40f * s, 74f * s, 96f * s, 94f * s), 4f * s, Color(0xFFF08CB8), pen)
        }
    }

    /** A paint brush with a drop: the look of the house. */
    val Brush: DrawScope.() -> Unit = {
        u { s, pen ->
            drawLine(Ink.line, Offset(78f * s, 12f * s), Offset(40f * s, 58f * s), 14f * s, StrokeCap.Round)
            drawLine(Color(0xFFC98A55), Offset(78f * s, 12f * s), Offset(40f * s, 58f * s), 8f * s, StrokeCap.Round)
            val tip = Path().apply { moveTo(30f * s, 54f * s); lineTo(48f * s, 66f * s); lineTo(36f * s, 90f * s); lineTo(10f * s, 80f * s); close() }
            inked(tip, T.Berry, pen)
            drawCircle(T.Sea, 8f * s, Offset(80f * s, 80f * s))
            drawCircle(Ink.line, 8f * s, Offset(80f * s, 80f * s), style = pen.thin)
            drawCircle(T.Sun, 6f * s, Offset(86f * s, 52f * s))
            drawCircle(Ink.line, 6f * s, Offset(86f * s, 52f * s), style = pen.thin)
        }
    }

    /** Balloons: the housewarming. */
    val Party: DrawScope.() -> Unit = {
        u { s, pen ->
            val cols = listOf(T.Berry, T.Sun, T.Sea)
            for (k in 0 until 3) {
                val c = Offset((24f + k * 26f) * s, (34f + (k % 2) * 8f) * s)
                drawLine(Ink.line, Offset(c.x, c.y + 20f * s), Offset(50f * s, 92f * s), 3f * s, StrokeCap.Round)
                inkedCircle(c, 19f * s, cols[k], pen)
                drawCircle(Color.White.copy(alpha = 0.6f), 4f * s, Offset(c.x - 7f * s, c.y - 8f * s))
            }
            for (k in 0 until 5) drawCircle(listOf(T.Mint, T.Grape, T.Sun, T.Berry, T.Sea)[k], 3f * s, Offset((10f + k * 20f) * s, (8f + (k % 2) * 6f) * s))
        }
    }

    /** A hammer smashing a wall: tear down. */
    val Smash: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(52f * s, 40f * s, 94f * s, 92f * s), 4f * s, Color(0xFFE8BC86), pen)
            for (k in 0 until 3) drawLine(Ink.line, Offset(52f * s, (56f + k * 14f) * s), Offset(94f * s, (56f + k * 14f) * s), 3f * s)
            rotate(-40f, Offset(30f * s, 56f * s)) {
                drawLine(Ink.line, Offset(30f * s, 56f * s), Offset(30f * s, 100f * s), 13f * s, StrokeCap.Round)
                drawLine(Color(0xFFC98A55), Offset(30f * s, 56f * s), Offset(30f * s, 100f * s), 7f * s, StrokeCap.Round)
                inkedRound(Rect(8f * s, 36f * s, 54f * s, 62f * s), 6f * s, Color(0xFF9AA5B8), pen)
            }
            drawPath(starPath(Offset(56f * s, 34f * s), 14f * s, 6f * s), T.SunTop)
            drawPath(starPath(Offset(56f * s, 34f * s), 14f * s, 6f * s), Ink.line, style = pen.thin)
        }
    }

    /** A door: go inside. */
    val DoorIn: DrawScope.() -> Unit = {
        u { s, pen ->
            inkedRound(Rect(22f * s, 10f * s, 78f * s, 94f * s), 6f * s, Color(0xFFF7F3EC), pen, shade = false)
            inkedRound(Rect(30f * s, 18f * s, 70f * s, 94f * s), 4f * s, T.Sea, pen)
            drawCircle(T.Sun, 4f * s, Offset(62f * s, 58f * s))
            val arrow = Path().apply { moveTo(4f * s, 52f * s); lineTo(26f * s, 52f * s) }
            drawPath(arrow, Ink.line, style = Stroke(8f * s, cap = StrokeCap.Round))
            drawPath(Path().apply { moveTo(18f * s, 40f * s); lineTo(30f * s, 52f * s); lineTo(18f * s, 64f * s) }, Ink.line, style = Stroke(8f * s, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
    }

    /** A house with a heart: all rooms are built. */
    val HouseHappy: DrawScope.() -> Unit = {
        u { s, pen ->
            val roof = Path().apply { moveTo(6f * s, 46f * s); lineTo(50f * s, 8f * s); lineTo(94f * s, 46f * s); close() }
            inked(roof, Color(0xFFD2443A), pen)
            inkedRound(Rect(14f * s, 44f * s, 86f * s, 92f * s), 4f * s, Color(0xFFFFC83D), pen)
            drawPath(app.trollfoss.ui.art.mineHeartPath(50f * s, 64f * s, 12f * s), T.Berry)
            drawPath(app.trollfoss.ui.art.mineHeartPath(50f * s, 64f * s, 12f * s), Ink.line, style = pen.thin)
        }
    }
}

/** A cloudless little scene round a picture: sky above, lawn below. */
private fun DrawScope.scenery(w: Float, h: Float, rad: Float) {
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF8CCBF4), Color(0xFFDDF3FF)), startY = 0f, endY = h * 0.78f), Offset.Zero, Size(w, h), CornerRadius(rad))
    drawRoundRect(Color(0xFF6FAE5A), Offset(0f, h * 0.78f), Size(w, h * 0.22f), CornerRadius(rad))
    drawRect(Color(0xFF6FAE5A), Offset(0f, h * 0.78f), Size(w, h * 0.06f))
}

/** A whole house, fitted into the picture (the hall and the rooms built so far). */
fun DrawScope.drawHouseThumb(h: MineHouse, backdrop: Boolean = true) {
    val w = size.width
    val hh = size.height
    if (backdrop) scenery(w, hh, min(w, hh) * 0.1f)
    val width = mineHouseWidth(h)
    val height = mineHouseHeight(h)
    val k = min(w * 0.94f / width, hh * 0.84f / height)
    val pen = Pen(max(1.1f, k * 0.0075f))
    val extra = if (h.shape == 2) 0.3f * k else 0f
    val left = (w - (mineHouseWidth(h) - (if (h.shape == 2) 0.6f else 0f)) * k) / 2f + extra
    drawMineHouse(h, left, hh * 0.82f, k, pen, detail = 1)
}

/** The template [shape] as a house of a hall and a room. */
fun DrawScope.drawTemplateThumb(shape: Int) {
    val h = MineHouse().apply {
        started = true
        Mine.applyTemplate(this, shape)
        ground[1] = RoomKind.LIVING.ordinal + 1
    }
    drawHouseThumb(h)
}

/** A house with only the chosen roof drawn: a small card for the roof types. */
fun DrawScope.drawRoofThumb(roof: Int, roofColor: Int, wall: Int) {
    val h = MineHouse().apply { started = true; shape = 1; this.roof = roof; this.roofColor = roofColor; this.wall = wall; chimney = false }
    scenery(size.width, size.height, min(size.width, size.height) * 0.12f)
    val k = size.width * 0.5f
    val pen = Pen(max(1.1f, k * 0.01f))
    drawMineHouse(h, size.width * 0.18f, size.height * 0.84f, k, pen, detail = 0)
}

fun DrawScope.drawDoorThumb(style: Int, wall: Int) {
    val w = size.width
    val h = size.height
    drawRoundRect(MineC.wall(wall), Offset.Zero, Size(w, h), CornerRadius(w * 0.12f))
    val pen = Pen(max(1.1f, w * 0.022f))
    val dw = w * 0.42f
    val dh = h * 0.66f
    drawMineDoor(style, (w - dw) / 2f, h * 0.2f, (w + dw) / 2f, h * 0.2f + dh, pen, 0f, 0f, MineC.trim(MineC.wall(wall)))
}

fun DrawScope.drawWindowThumb(style: Int, wall: Int, roofColor: Int) {
    val w = size.width
    val h = size.height
    drawRoundRect(MineC.wall(wall), Offset.Zero, Size(w, h), CornerRadius(w * 0.12f))
    val pen = Pen(max(1.1f, w * 0.022f))
    val ww = w * 0.4f
    drawMineWindow(style, null, (w - ww) / 2f, h * 0.22f, (w + ww) / 2f, h * 0.6f, pen, 0f, MineC.trim(MineC.wall(wall)), MineC.roof(roofColor), 0f, 1)
}

/** One kind of room as a card picture. */
fun DrawScope.drawKindThumb(kind: RoomKind) {
    drawRoundRect(Color(0xFFFFF7EA), Offset.Zero, size, CornerRadius(size.width * 0.1f))
    drawRoomPreview(kind, size.width, size.height)
}

fun DrawScope.drawSwatch(color: Color, round: Boolean) {
    val w = size.width
    val pen = Pen(max(1.1f, w * 0.05f))
    if (round) inkedCircle(Offset(w / 2f, size.height / 2f), w * 0.42f, color, pen) else inkedRound(Rect(w * 0.08f, w * 0.08f, w * 0.92f, size.height - w * 0.08f), w * 0.16f, color, pen)
}

/** A chimney with smoke, or a flag: the two switches of the look. */
fun DrawScope.drawChimneyThumb(on: Boolean) {
    val s = size.minDimension / 100f
    val pen = Pen(5.5f * s)
    val roof = Path().apply { moveTo(6f * s, 90f * s); lineTo(50f * s, 44f * s); lineTo(94f * s, 90f * s); close() }
    inked(roof, Color(0xFFB5473A), pen)
    if (on) {
        inkedRound(Rect(60f * s, 30f * s, 76f * s, 66f * s), 2f * s, Color(0xFFB7664F), pen)
        for (k in 0 until 3) drawCircle(Color.White, (6f + k * 2f) * s, Offset((72f + k * 7f) * s, (22f - k * 9f) * s), alpha = 0.8f - k * 0.2f)
    } else {
        drawLine(Color(0xFF9AA5B8), Offset(14f * s, 20f * s), Offset(86f * s, 20f * s), 7f * s, StrokeCap.Round)
    }
}

fun DrawScope.drawFlagThumb(on: Boolean) {
    val s = size.minDimension / 100f
    val pen = Pen(5.5f * s)
    drawLine(Ink.line, Offset(30f * s, 94f * s), Offset(30f * s, 10f * s), 8f * s, StrokeCap.Round)
    drawLine(Color(0xFFE8E4DC), Offset(30f * s, 94f * s), Offset(30f * s, 10f * s), 4f * s, StrokeCap.Round)
    if (on) {
        val f = Path().apply { moveTo(32f * s, 12f * s); lineTo(86f * s, 22f * s); lineTo(32f * s, 46f * s); close() }
        inked(f, T.Berry, pen)
        drawPath(starPath(Offset(52f * s, 26f * s), 8f * s, 3.5f * s), T.SunTop)
    } else {
        drawLine(Color(0xFF9AA5B8), Offset(14f * s, 20f * s), Offset(86f * s, 20f * s), 7f * s, StrokeCap.Round)
    }
}

/** A rough sketch of how many floors a house has, for the floor tab: [floors] boxes on top of each other. */
fun DrawScope.drawFloorsThumb(withUpper: Boolean) {
    scenery(size.width, size.height, min(size.width, size.height) * 0.1f)
    val h = MineHouse().apply {
        started = true
        shape = 1
        Mine.applyTemplate(this, 1)
        ground[1] = RoomKind.LIVING.ordinal + 1
        ground[2] = RoomKind.KITCHEN.ordinal + 1
        upperBuilt = withUpper
        if (withUpper) this.upper[1] = RoomKind.BEDROOM.ordinal + 1
    }
    val width = mineHouseWidth(h)
    val height = mineHouseHeight(h)
    val k = min(size.width * 0.9f / width, size.height * 0.8f / height)
    val pen = Pen(max(1.1f, k * 0.0075f))
    drawMineHouse(h, (size.width - (width - 0.5f) * k) / 2f, size.height * 0.84f, k, pen, detail = 1)
}
