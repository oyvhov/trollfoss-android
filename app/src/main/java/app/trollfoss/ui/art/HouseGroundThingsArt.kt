package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.ThingType

// The things Storstova brings: the umbrella from the hall's stand, the daily letter, the map pin from the
// globe, a jar of jam and Rolf's silver tray. Origin bottom centre, the thing inside (-w/2, -h) to (w/2, 0).

internal fun DrawScope.drawGroundThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean {
    when (type) {
        ThingType.GR_UMBRELLA -> thUmbrella(variant, w, h, pen)
        ThingType.GR_LETTER -> thLetter(variant, w, h, pen)
        ThingType.GR_PIN -> thPin(variant, w, h, pen)
        ThingType.GR_JAM -> thJam(variant, used, w, h, pen)
        ThingType.GR_TRAY -> thTray(w, h, pen)
        else -> return false
    }
    return true
}

private val UMBRELLA = intArrayOf(0xFFD2443A.toInt(), 0xFF2F6FB8.toInt(), 0xFFFFC83D.toInt(), 0xFF3BC46B.toInt())

private fun DrawScope.thUmbrella(variant: Int, w: Float, h: Float, pen: Pen) {
    val c = Color(UMBRELLA[variant.mod(UMBRELLA.size)])
    val x = 0f
    // The crook handle at the foot, the shaft, and the furled canopy with its strap.
    val hook = Path().apply {
        moveTo(x, -h * 0.28f)
        lineTo(x, -h * 0.08f)
        quadraticTo(x, 0f, x - w * 0.28f, 0f)
        quadraticTo(x - w * 0.55f, 0f, x - w * 0.5f, -h * 0.07f)
    }
    drawPath(hook, Ink.line, style = Stroke(w * 0.26f + pen.lw * 2f, cap = StrokeCap.Round))
    drawPath(hook, Color(0xFF5A3F2A), style = Stroke(w * 0.26f, cap = StrokeCap.Round))
    val shaft = Offset(x, -h * 0.3f)
    drawLine(Ink.line, Offset(x, -h * 0.12f), shaft, w * 0.14f + pen.lw * 2f, StrokeCap.Round)
    drawLine(Color(0xFFBAC4D4), Offset(x, -h * 0.12f), shaft, w * 0.14f, StrokeCap.Round)
    val canopy = Path().apply {
        moveTo(x - w * 0.22f, -h * 0.3f)
        quadraticTo(x - w * 0.46f, -h * 0.62f, x, -h * 0.98f)
        quadraticTo(x + w * 0.46f, -h * 0.62f, x + w * 0.22f, -h * 0.3f)
        quadraticTo(x, -h * 0.26f, x - w * 0.22f, -h * 0.3f)
        close()
    }
    inked(canopy, c, pen)
    // The folds of the cloth.
    for (k in -1..1) drawLine(c.darken(0.3f), Offset(x + k * w * 0.1f, -h * 0.3f), Offset(x + k * w * 0.03f, -h * 0.9f), pen.lw * 0.6f)
    // The strap with a snap.
    drawRect(Color(0xFFF2EEE6), Offset(x - w * 0.26f, -h * 0.46f), Size(w * 0.52f, h * 0.045f))
    drawRect(Ink.line, Offset(x - w * 0.26f, -h * 0.46f), Size(w * 0.52f, h * 0.045f), style = pen.thin)
    drawCircle(Color(0xFFE0B04A), w * 0.05f, Offset(x + w * 0.16f, -h * 0.44f))
    drawCircle(Color(0xFFE0B04A), w * 0.045f, Offset(x, -h * 0.99f))
    shine(Offset(x - w * 0.12f, -h * 0.65f), w * 0.07f, h * 0.18f, 0.6f)
}

private val SEAL = intArrayOf(0xFFD2443A.toInt(), 0xFF2F6FB8.toInt(), 0xFF3BC46B.toInt(), 0xFFFFC83D.toInt())

private fun DrawScope.thLetter(variant: Int, w: Float, h: Float, pen: Pen) {
    val seal = Color(SEAL[variant.mod(SEAL.size)])
    val body = Rect(-w / 2f, -h, w / 2f, 0f)
    inkedRound(body, w * 0.04f, Color(0xFFFFFBF0), pen)
    // The flap, with its lines meeting in the middle.
    val flap = Path().apply {
        moveTo(body.left, body.top)
        lineTo(0f, -h * 0.45f)
        lineTo(body.right, body.top)
    }
    drawPath(flap, Ink.line.copy(alpha = 0.55f), style = Stroke(pen.lw * 0.8f, cap = StrokeCap.Round))
    drawLine(Ink.line.copy(alpha = 0.3f), Offset(body.left + w * 0.02f, body.bottom - h * 0.04f), Offset(0f, -h * 0.5f), pen.lw * 0.5f)
    drawLine(Ink.line.copy(alpha = 0.3f), Offset(body.right - w * 0.02f, body.bottom - h * 0.04f), Offset(0f, -h * 0.5f), pen.lw * 0.5f)
    // A stamp in the corner with a little star, and the seal.
    val st = Rect(body.right - w * 0.28f, body.top + h * 0.1f, body.right - w * 0.06f, body.top + h * 0.45f)
    drawRect(Color(0xFFFFE9A8), st.topLeft, st.size)
    drawRect(Ink.line, st.topLeft, st.size, style = pen.thin)
    drawPath(starPath(st.center, st.width * 0.38f, st.width * 0.16f), seal)
    inkedCircle(Offset(0f, -h * 0.45f), h * 0.16f, seal, pen, shade = false)
    drawCircle(Color.White.copy(alpha = 0.5f), h * 0.04f, Offset(-h * 0.05f, -h * 0.49f))
}

private val PIN = intArrayOf(0xFFD2443A.toInt(), 0xFF2F6FB8.toInt(), 0xFFFFC83D.toInt(), 0xFF3BC46B.toInt())

private fun DrawScope.thPin(variant: Int, w: Float, h: Float, pen: Pen) {
    val c = Color(PIN[variant.mod(PIN.size)])
    // A needle going down, and a round head with a shine.
    drawLine(Ink.line, Offset(0f, -h * 0.5f), Offset(0f, 0f), w * 0.12f + pen.lw * 2f, StrokeCap.Round)
    drawLine(Color(0xFFBAC4D4), Offset(0f, -h * 0.5f), Offset(0f, 0f), w * 0.12f, StrokeCap.Round)
    inkedCircle(Offset(0f, -h * 0.72f), w * 0.5f, c, pen)
    shine(Offset(-w * 0.15f, -h * 0.8f), w * 0.2f, w * 0.14f, 0.85f)
}

private val JAM = intArrayOf(0xFFD2443A.toInt(), 0xFF3B4F9E.toInt(), 0xFFFFA23A.toInt(), 0xFF7A3B6E.toInt())

private fun DrawScope.thJam(variant: Int, used: Int, w: Float, h: Float, pen: Pen) {
    val c = Color(JAM[variant.mod(JAM.size)])
    val jar = Path().apply {
        moveTo(-w * 0.44f, -h * 0.78f)
        lineTo(w * 0.44f, -h * 0.78f)
        lineTo(w * 0.46f, -h * 0.06f)
        quadraticTo(w * 0.46f, 0f, w * 0.4f, 0f)
        lineTo(-w * 0.4f, 0f)
        quadraticTo(-w * 0.46f, 0f, -w * 0.46f, -h * 0.06f)
        close()
    }
    drawPath(jar, Color(0xFFE6F4FB))
    // The jam level falls as it is eaten.
    val level = (0.7f - used * 0.28f).coerceAtLeast(0.2f)
    clipPath(jar) {
        drawRect(c, Offset(-w * 0.5f, -h * level), Size(w, h * level))
        drawRect(Color.White.copy(alpha = 0.25f), Offset(-w * 0.5f, -h * level), Size(w, h * 0.04f))
    }
    drawPath(jar, Ink.line, style = pen.stroke)
    // The label with a little fruit drawn on it.
    val label = Rect(-w * 0.3f, -h * 0.5f, w * 0.3f, -h * 0.18f)
    drawRect(Color(0xFFFFF8EA), label.topLeft, label.size)
    drawRect(Ink.line, label.topLeft, label.size, style = pen.thin)
    drawCircle(c.lighten(0.1f), h * 0.06f, label.center)
    drawLine(Color(0xFF3BC46B), Offset(label.center.x, label.center.y - h * 0.06f), Offset(label.center.x + w * 0.05f, label.center.y - h * 0.1f), pen.lw, StrokeCap.Round)
    // The gingham cloth over the lid, tied with string.
    val cloth = Path().apply {
        moveTo(-w * 0.5f, -h * 0.74f)
        lineTo(w * 0.5f, -h * 0.74f)
        lineTo(w * 0.56f, -h * 0.9f)
        lineTo(w * 0.28f, -h * 0.84f)
        lineTo(0f, -h * 0.98f)
        lineTo(-w * 0.28f, -h * 0.84f)
        lineTo(-w * 0.56f, -h * 0.9f)
        close()
    }
    drawPath(cloth, Color(0xFFFFF8EA))
    clipPath(cloth) {
        for (k in 0 until 6) {
            drawRect(Color(0xFFD2443A).copy(alpha = 0.45f), Offset(-w * 0.56f + k * w * 0.2f, -h), Size(w * 0.1f, h * 0.3f))
            drawRect(Color(0xFFD2443A).copy(alpha = 0.45f), Offset(-w * 0.56f, -h * 0.98f + k * h * 0.06f), Size(w * 1.12f, h * 0.03f))
        }
    }
    drawPath(cloth, Ink.line, style = pen.thin)
    drawLine(Color(0xFF8A5A3C), Offset(-w * 0.46f, -h * 0.76f), Offset(w * 0.46f, -h * 0.76f), pen.lw * 1.2f, StrokeCap.Round)
    shine(Offset(-w * 0.3f, -h * 0.38f), w * 0.07f, h * 0.3f, 0.7f)
}

private fun DrawScope.thTray(w: Float, h: Float, pen: Pen) {
    val silver = Color(0xFFDDE3EC)
    // A round silver tray seen from the front, and a dome with a knob.
    val tray = Rect(-w / 2f, -h * 0.14f, w / 2f, 0f)
    inkedOval(tray, silver, pen)
    drawOval(Color(0xFFBAC4D4), Offset(-w * 0.4f, -h * 0.1f), Size(w * 0.8f, h * 0.06f), style = pen.thin)
    val dome = Path().apply {
        moveTo(-w * 0.36f, -h * 0.1f)
        quadraticTo(-w * 0.36f, -h * 0.78f, 0f, -h * 0.78f)
        quadraticTo(w * 0.36f, -h * 0.78f, w * 0.36f, -h * 0.1f)
        quadraticTo(0f, -h * 0.02f, -w * 0.36f, -h * 0.1f)
        close()
    }
    inked(dome, silver, pen)
    shine(Offset(-w * 0.16f, -h * 0.5f), w * 0.06f, h * 0.3f, 0.8f)
    inkedCircle(Offset(0f, -h * 0.84f), h * 0.09f, silver.darken(0.1f), pen, shade = false)
    drawLine(Color(0xFF8E9AB2), Offset(-w * 0.34f, -h * 0.16f), Offset(w * 0.34f, -h * 0.16f), pen.lw * 0.7f)
}
