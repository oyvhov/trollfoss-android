package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.sin

private val Paper = Color(0xFFFFEAC0)
private val Wood = Color(0xFFC98A55)
private val Moss = Color(0xFF70AD76)

fun lanternColor(mode: Int): Color = when (mode) {
    1 -> Color(0xFFFFD66D)
    2 -> Color(0xFF77D5E7)
    3 -> Color(0xFFA0DC95)
    else -> Color(0xFFADB6BA)
}

/** The same wordless clue appears on the paper and on its adventure card. */
fun DrawScope.drawTrailPicture(index: Int, at: Offset, side: Float, pen: Pen) {
    val r = side * 0.45f
    translate(at.x, at.y) {
        when (index) {
            0 -> {
                inkedOval(Rect(-r*0.8f,-r*0.9f,r*0.8f,r),Wood,pen)
                for (x in listOf(-0.38f,0.38f)) {
                    inkedCircle(Offset(x*r,-r*0.18f),r*0.4f,Paper,pen,shade=false)
                    drawCircle(Ink.line,r*0.14f,Offset(x*r,-r*0.17f))
                }
                inked(Path().apply { moveTo(-r*0.18f,r*0.12f); lineTo(r*0.18f,r*0.12f); lineTo(0f,r*0.42f); close() },lanternColor(1),pen)
                for (x in listOf(-0.3f,0.3f)) drawLine(Ink.line,Offset(x*r,r*0.76f),Offset(x*r,r*1.07f),pen.lw,StrokeCap.Round)
            }
            1 -> {
                val shell = Path().apply {
                    moveTo(-r*0.25f,r*0.8f); lineTo(-r,-r*0.05f)
                    cubicTo(-r*1.15f,-r*0.55f,-r*0.6f,-r,r*0.02f,-r*0.72f)
                    cubicTo(r*0.65f,-r,r*1.12f,-r*0.45f,r,-r*0.05f)
                    lineTo(r*0.25f,r*0.8f); close()
                }
                inked(shell,Color(0xFFF2AD99),pen)
                for (x in listOf(-0.62f,-0.3f,0f,0.3f,0.62f))
                    drawLine(Wood,Offset(x*r,-r*0.5f),Offset(x*r*0.2f,r*0.65f),pen.lw*0.55f,StrokeCap.Round)
            }
            else -> {
                for ((x,h) in listOf(-0.5f to 0.55f,0f to 1f,0.5f to 0.72f)) {
                    val crystal = Path().apply {
                        moveTo((x-0.24f)*r,r*0.72f); lineTo((x-0.24f)*r,-h*r*0.5f)
                        lineTo(x*r,-h*r); lineTo((x+0.24f)*r,-h*r*0.5f); lineTo((x+0.24f)*r,r*0.72f); close()
                    }
                    inked(crystal,if(x==0f) Color(0xFFBBA0E1) else Color(0xFF7FC8D4),pen)
                    drawLine(Color.White,Offset((x-0.08f)*r,-h*r*0.46f),Offset((x-0.08f)*r,r*0.43f),pen.lw*0.5f,StrokeCap.Round)
                }
            }
        }
    }
}

fun DrawScope.thRumleNote(index: Int, found: Boolean, w: Float, h: Float, pen: Pen) {
    inkedRound(Rect(-w*0.5f,-h,w*0.5f,0f),w*0.05f,Paper,pen)
    drawLine(Wood.copy(alpha=0.35f),Offset(w*0.17f,-h*0.92f),Offset(w*0.17f,-h*0.08f),pen.lw*0.5f)
    drawTrailPicture(index,Offset(-w*0.1f,-h*0.52f),h*0.65f,pen)
    for(i in 0..2) drawCircle(Wood,w*0.017f,Offset((0.28f+i*0.05f)*w,(-0.65f+i*0.15f)*h))
    if(found) {
        drawCircle(Moss,w*0.115f,Offset(w*0.34f,-h*0.22f))
        drawLine(Color.White,Offset(w*0.28f,-h*0.22f),Offset(w*0.33f,-h*0.16f),pen.lw*0.65f,StrokeCap.Round)
        drawLine(Color.White,Offset(w*0.33f,-h*0.16f),Offset(w*0.41f,-h*0.3f),pen.lw*0.65f,StrokeCap.Round)
    }
}

fun DrawScope.thTrollLantern(mode: Int, w: Float, h: Float, pen: Pen) {
    val lit = mode in 1..3
    drawArc(Ink.line,180f,180f,false,Offset(-w*0.23f,-h*0.99f),Size(w*0.46f,h*0.34f),style=Stroke(pen.lw*1.8f))
    inkedRound(Rect(-w*0.39f,-h*0.75f,w*0.39f,-h*0.12f),w*0.12f,if(lit) lanternColor(mode) else Color(0xFFD8E1D6),pen)
    for(side in listOf(-1f,1f)) {
        inkedOval(Rect(side*w*0.37f-w*0.12f,-h*0.67f,side*w*0.37f+w*0.12f,-h*0.42f),Moss,pen)
        drawCircle(Ink.line,w*0.055f,Offset(side*w*0.15f,-h*0.53f))
    }
    drawCircle(Paper,w*0.07f,Offset(0f,-h*0.4f))
    drawArc(Ink.line,15f,150f,false,Offset(-w*0.14f,-h*0.39f),Size(w*0.28f,h*0.15f),style=pen.thin)
    inkedRound(Rect(-w*0.44f,-h*0.82f,w*0.44f,-h*0.70f),w*0.05f,Wood,pen)
    inkedRound(Rect(-w*0.45f,-h*0.17f,w*0.45f,0f),w*0.06f,Wood,pen)
    drawCircle(if(lit) lanternColor(mode) else Paper,w*0.065f,Offset(0f,-h*0.08f))
    drawLine(Color.White.copy(alpha=0.75f),Offset(-w*0.23f,-h*0.65f),Offset(-w*0.23f,-h*0.57f),pen.lw,StrokeCap.Round)
}

/** A small pool of light and a playful silhouette, not a full-screen effect. pen.t=0 freezes it. */
fun DrawScope.drawLanternProjection(mode: Int, from: Offset, at: Offset, side: Float, pen: Pen) {
    if(mode !in 1..3) return
    val color = lanternColor(mode)
    val beam = Path().apply { moveTo(from.x,from.y); lineTo(at.x-side*0.5f,at.y); lineTo(at.x+side*0.5f,at.y); close() }
    drawPath(beam,color.copy(alpha=0.11f))
    drawOval(color.copy(alpha=0.22f),at-Offset(side*0.6f,side*0.48f),Size(side*1.2f,side*0.96f))
    val ink = Ink.line.copy(alpha=0.65f)
    val bob = sin(pen.t*1.3f)*side*0.02f
    translate(at.x,at.y+bob) {
        when(mode) {
            1 -> for(i in 0..4) {
                val x = (i%3-1)*side*0.3f
                val y = (i/3-0.5f)*side*0.35f
                drawPath(starPath(Offset(x,y),side*0.11f,side*0.045f),color.copy(alpha=0.95f))
                drawPath(starPath(Offset(x,y),side*0.11f,side*0.045f),ink,style=Stroke(pen.lw*0.65f))
            }
            2 -> {
                val fish = Path().apply { addOval(Rect(-side*0.27f,-side*0.17f,side*0.26f,side*0.17f)); moveTo(-side*0.2f,0f);lineTo(-side*0.43f,-side*0.21f);lineTo(-side*0.43f,side*0.21f);close() }
                drawPath(fish,ink); drawCircle(color,side*0.035f,Offset(side*0.14f,-side*0.04f))
                for(i in 0..2) drawCircle(color,side*(0.025f+i*0.008f),Offset(side*(0.33f+i*0.07f),-side*(0.08f+i*0.08f)),style=Stroke(pen.lw*0.6f))
            }
            else -> {
                drawCircle(ink,side*0.23f,Offset.Zero)
                for(s in listOf(-1f,1f)) {
                    drawOval(ink,Offset(s*side*0.24f-side*0.085f,-side*0.16f),Size(side*0.17f,side*0.17f))
                    drawCircle(color,side*0.043f,Offset(s*side*0.08f,-side*0.04f))
                }
                drawArc(color,15f,150f,false,Offset(-side*0.1f,side*0.025f),Size(side*0.2f,side*0.12f),style=Stroke(pen.lw))
            }
        }
    }
}
