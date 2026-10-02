package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.*
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType

private val Sheet = Color(0xFFB4A2E4)
private val Wood = Color(0xFFDEAA72)
private val Mint = Color(0xFF80D6B2)

/** A picture of the action, rather than the same generic sparkle on every button. */
fun DrawScope.drawPlayAction(action: PlayAction, type: ThingType) {
    val s = size.minDimension
    val pen = Pen(s * 0.026f)
    val u = s * 0.67f / maxOf(type.w, type.h)
    if (action == PlayAction.THROW || action == PlayAction.LIGHT || action == PlayAction.TWINKLE) {
        translate(s * 0.44f, s * 0.73f) { drawThing(type, 0, 0, type.w * u, type.h * u, pen) }
        if (action == PlayAction.THROW) {
            val arc = Path().apply { moveTo(s*0.12f,s*0.26f); quadraticBezierTo(s*0.65f,s*0.06f,s*0.88f,s*0.44f) }
            drawPath(arc, Ink.line, style = pen.thin)
            drawLine(Ink.line, Offset(s*0.88f,s*0.44f), Offset(s*0.87f,s*0.28f), pen.lw, StrokeCap.Round)
        } else if (action == PlayAction.TWINKLE) {
            for (p in listOf(Offset(0.82f,0.23f),Offset(0.77f,0.63f),Offset(0.32f,0.16f))) inked(starPath(p*s, s*0.095f,s*0.04f), Color(0xFFFFDC79),pen)
        } else for (i in -1..1) drawLine(Color(0xFFFFDF7F), Offset(s*0.64f,s*0.41f), Offset(s*0.9f,s*(0.41f+i*0.2f)), pen.lw*2, StrokeCap.Round)
        return
    }
    val anim = PersonAnim().apply { face = if (action == PlayAction.NAP) Face.SLEEP else Face.GRIN }
    translate(s*0.34f, s*0.88f) {
        drawPerson(Species.FOLK, Look(hair = 3, topColor = 4), Pose.STAND, anim, s*0.73f, pen, true)
    }
    translate(s*0.67f, s*0.72f) { drawThing(type, 0, 0, type.w*u*0.60f, type.h*u*0.60f, pen) }
    when (action) {
        PlayAction.NAP -> { drawLine(Ink.line, Offset(s*0.6f,s*0.13f),Offset(s*0.87f,s*0.13f),pen.lw); drawLine(Ink.line,Offset(s*0.87f,s*0.13f),Offset(s*0.6f,s*0.30f),pen.lw); drawLine(Ink.line,Offset(s*0.6f,s*0.30f),Offset(s*0.87f,s*0.30f),pen.lw) }
        PlayAction.HUG -> {
            val heart = Path().apply { moveTo(s*0.72f,s*0.34f); cubicTo(s*0.48f,s*0.21f,s*0.58f,s*0.08f,s*0.72f,s*0.17f); cubicTo(s*0.88f,s*0.07f,s*0.98f,s*0.23f,s*0.72f,s*0.34f); close() }
            inked(heart, Color(0xFFFF9CB2), pen)
        }
        PlayAction.STORY -> { inkedCircle(Offset(s*0.8f,s*0.23f),s*0.12f,Color.White,pen); inked(starPath(Offset(s*0.8f,s*0.23f),s*0.075f,s*0.03f),Mint,pen) }
        PlayAction.HIDE -> { translate(s*0.34f,s*0.31f) { drawThing(ThingType.AT_SHEET_HAT,0,0,s*0.34f,s*0.32f,pen) } }
        else -> Unit
    }
}

fun DrawScope.drawPlayBack(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.PLAY_FORT -> {
            inkedRound(Rect(-0.27f*u, -0.055f*u, 0.27f*u, 0f), 0.025f*u, Mint, pen)
            val roof = Path().apply {
                moveTo(-0.29f*u, -0.04f*u); lineTo(-0.20f*u, -0.36f*u)
                quadraticBezierTo(0f, -0.42f*u, 0.20f*u, -0.36f*u); lineTo(0.29f*u, -0.04f*u); close()
            }
            inked(roof, Sheet, pen)
            val door = Path().apply {
                moveTo(-0.16f*u, -0.035f*u); lineTo(0f, -0.30f*u); lineTo(0.16f*u, -0.035f*u); close()
            }
            inked(door, Color(0xFF594575), pen)
            drawLine(Color(0xFFE6DFFF), Offset(-0.20f*u, -0.33f*u), Offset(0.16f*u, -0.33f*u), pen.lw*1.4f, StrokeCap.Round)
            for (i in -2..2) {
                val center = Offset(i*0.08f*u, -0.35f*u)
                inkedCircle(center, 0.012f*u, if (f.on) Color(0xFFFFDF7F) else Mint, pen)
            }
            inkedRound(Rect(-0.22f*u, -0.055f*u, -0.08f*u, -0.015f*u), 0.015f*u, Color(0xFFFFC58A), pen)
        }
        FixtureType.PLAY_CART -> {
            for (x in listOf(-0.19f, 0.19f)) {
                inkedCircle(Offset(x*u, -0.045f*u), 0.045f*u, Color(0xFF514967), pen)
                inkedCircle(Offset(x*u, -0.045f*u), 0.018f*u, Mint, pen)
            }
            inkedRound(Rect(-0.25f*u, -0.19f*u, 0.25f*u, -0.08f*u), 0.018f*u, Wood, pen)
            inkedRound(Rect(-0.23f*u, -0.17f*u, 0.23f*u, -0.12f*u), 0.012f*u, Color(0xFF9A6848), pen)
            for (side in listOf(-1f, 1f)) {
                drawLine(Ink.line, Offset(side*0.24f*u, -0.12f*u), Offset(side*0.38f*u, -0.15f*u), pen.lw*3, StrokeCap.Round)
                drawLine(Wood, Offset(side*0.24f*u, -0.12f*u), Offset(side*0.38f*u, -0.15f*u), pen.lw*1.8f, StrokeCap.Round)
                inkedRound(Rect((side*0.36f-0.035f)*u, -0.21f*u, (side*0.36f+0.035f)*u, -0.09f*u), 0.025f*u,
                    if (f.count >= 2 || f.on) Mint else Color(0xFFFFD06D), pen)
            }
        }
        else -> return false
    }
    return true
}

fun DrawScope.drawPlayFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.PLAY_FORT -> {
            inkedRound(Rect(-0.29f*u, -0.12f*u, -0.17f*u, -0.02f*u), 0.035f*u, Sheet, pen)
            inkedRound(Rect(0.17f*u, -0.12f*u, 0.29f*u, -0.02f*u), 0.035f*u, Sheet, pen)
        }
        FixtureType.PLAY_CART -> {
            inkedRound(Rect(-0.25f*u, -0.11f*u, 0.25f*u, -0.065f*u), 0.012f*u, Wood, pen)
            drawLine(Color(0xFFFFD3A0), Offset(-0.21f*u, -0.097f*u), Offset(0.21f*u, -0.097f*u), pen.lw, StrokeCap.Round)
            inkedCircle(Offset(0f, -0.089f*u), 0.018f*u, Mint, pen)
        }
        else -> return false
    }
    return true
}
