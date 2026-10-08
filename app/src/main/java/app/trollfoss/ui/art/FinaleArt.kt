package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.FinalePlay
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import kotlin.math.sin

private val Moss = Color(0xFF6CAE73)
private val DeepMoss = Color(0xFF397755)
private val Honey = Color(0xFFEEC879)
private val Birch = Color(0xFFF6ECDC)
private val Wood = Color(0xFFC98A55)
private val Teal = Color(0xFF57BFC3)
private val Plum = Color(0xFF655073)

/** Four pieces of physical, ink-outlined toy furniture. Passenger and cargo spaces stay open. */
fun DrawScope.drawFinaleBack(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in FinalePlay.TYPES) return false
    fun box(l: Float, t: Float, r: Float, b: Float, c: Color) = inkedRound(Rect(l*u,t*u,r*u,b*u), u*0.014f, c, pen)
    fun oval(l: Float, t: Float, r: Float, b: Float, c: Color) = inkedOval(Rect(l*u,t*u,r*u,b*u), c, pen)
    fun line(x: Float, y: Float, xx: Float, yy: Float, c: Color = Ink.line, width: Float = pen.lw) = drawLine(c, Offset(x*u,y*u), Offset(xx*u,yy*u), width, StrokeCap.Round)
    when (f.type) {
        FixtureType.PLAY_DRAGON_CART -> {
            // A round tail, soft triangular wing, two benches and a friendly, bubble-snorting head.
            inked(Path().apply {
                moveTo(-0.22f*u,-0.09f*u); quadraticTo(-0.38f*u,-0.08f*u,-0.32f*u,-0.23f*u)
                quadraticTo(-0.28f*u,-0.17f*u,-0.18f*u,-0.16f*u); close()
            }, Moss, pen)
            val flap = if (f.on) sin(pen.t*4f)*0.018f else 0f
            inked(Path().apply {
                moveTo(-0.12f*u,-0.16f*u); quadraticTo(-0.26f*u,(-0.34f+flap)*u,0.025f*u,(-0.25f+flap)*u)
                lineTo(0.02f*u,-0.13f*u); close()
            }, DeepMoss, pen)
            oval(-0.27f,-0.18f,0.24f,-0.045f,Moss)
            box(-0.23f,-0.16f,0.16f,-0.095f,Wood)
            box(-0.22f,-0.125f,-0.085f,-0.09f,Honey); box(-0.005f,-0.125f,0.135f,-0.09f,Honey)
            oval(0.155f,-0.285f,0.29f,-0.105f,Moss)
            oval(0.215f,-0.215f,0.345f,-0.135f,Moss)
            oval(0.205f,-0.31f,0.23f,-0.265f,Honey)
            oval(0.175f,-0.255f,0.24f,-0.205f,Color.White)
            drawCircle(Ink.line,u*0.01f,Offset(0.22f*u,-0.228f*u))
            drawCircle(DeepMoss,u*0.007f,Offset(0.316f*u,-0.185f*u))
            line(0.263f,-0.155f,0.31f,-0.15f)
            drawCircle(Color(0xFFFFB4A7),u*0.015f,Offset(0.259f*u,-0.2f*u))
            for (x in floatArrayOf(-0.17f,0.14f)) {
                oval(x-0.048f,-0.09f,x+0.048f,0.006f,Plum)
                oval(x-0.023f,-0.065f,x+0.023f,-0.019f,Honey)
                val a = f.angle*8f
                line(x,-0.042f,x+sin(a)*0.034f,-0.042f+kotlin.math.cos(a)*0.034f,Birch,pen.lw*0.5f)
            }
        }
        FixtureType.PLAY_AIRSHIP -> {
            // Ropes run outside the two heads. The little rear basket holds one real thing.
            line(-0.26f,-0.45f,-0.25f,-0.1f,Wood,pen.lw*1.5f)
            line(0.27f,-0.45f,0.28f,-0.1f,Wood,pen.lw*1.5f)
            inked(Path().apply { moveTo(-0.26f*u,-0.52f*u); lineTo(-0.345f*u,-0.575f*u); lineTo(-0.33f*u,-0.45f*u); close() }, Honey, pen)
            val envelope = Rect(-0.3f*u,-0.62f*u,0.32f*u,-0.385f*u)
            oval(-0.3f,-0.62f,0.32f,-0.385f,Teal)
            val shape = Path().apply { addOval(envelope) }
            clipPath(shape) {
                drawOval(Birch,Offset(-0.16f*u,-0.62f*u),Size(0.14f*u,0.235f*u))
                drawOval(Honey,Offset(0.025f*u,-0.62f*u),Size(0.14f*u,0.235f*u))
                drawOval(DeepMoss.copy(alpha=0.25f),Offset(-0.31f*u,-0.445f*u),Size(0.65f*u,0.09f*u))
            }
            drawPath(shape,Ink.line,style=pen.stroke)
            drawLine(Color.White.copy(alpha=0.65f),Offset(-0.19f*u,-0.57f*u),Offset(-0.095f*u,-0.587f*u),u*0.011f,StrokeCap.Round)
            box(-0.265f,-0.135f,0.295f,-0.065f,Wood)
            box(-0.22f,-0.125f,-0.055f,-0.10f,Honey); box(0.005f,-0.125f,0.145f,-0.10f,Honey)
            box(0.18f,-0.11f,0.3f,-0.045f,Wood)
            oval(-0.12f,-0.055f,0.02f,-0.005f,Plum)
            line(-0.30f,-0.045f,-0.30f,-0.135f,Plum,pen.lw*2f)
            val a = if (f.on) pen.t*13f else 0.6f
            line(-0.30f,-0.09f,-0.30f+sin(a)*0.025f,-0.09f+kotlin.math.cos(a)*0.058f,Teal,pen.lw*2.4f)
        }
        FixtureType.PLAY_HIDE_TROLL -> {
            for (i in 0..2) {
                val x = (i-1)*FinalePlay.HOLE_STEP
                box(x-0.095f,-0.10f,x+0.095f,-0.008f,Wood)
                oval(x-0.097f,-0.13f,x+0.097f,-0.07f,Honey)
                oval(x-0.063f,-0.116f,x+0.063f,-0.087f,Plum)
                line(x-0.067f,-0.06f,x-0.067f,-0.022f,Wood.darken(0.25f),pen.lw*0.5f)
                line(x+0.059f,-0.065f,x+0.059f,-0.03f,Wood.darken(0.25f),pen.lw*0.5f)
                if ((f.on && f.angle.toInt()==i+1) || (!f.on && i==1)) {
                    // Always visible eyes: a child can take as long as they need.
                    val bob = if (f.on) sin(pen.t*2.1f)*0.004f else 0f
                    finaleTroll(Offset(x*u,(-0.14f+bob)*u),0.037f*u,pen,false)
                }
                oval(x-0.095f,-0.13f,x-0.034f,-0.106f,Moss)
                oval(x+0.03f,-0.13f,x+0.095f,-0.105f,DeepMoss)
            }
        }
        FixtureType.PLAY_TROLL_PARTY -> {
            box(-0.12f,-0.22f,0.12f,-0.01f,Wood)
            oval(-0.13f,-0.085f,0.13f,-0.005f,Plum)
            oval(-0.105f,-0.105f,0.105f,-0.035f,if(f.on) Honey else Teal)
            finaleTroll(Offset(0f,-0.155f*u),0.059f*u,pen,f.on)
            for (side in intArrayOf(-1,1)) {
                val lift = if(f.on) sin(pen.t*4f+side)*0.013f else 0f
                line(side*0.065f,-0.16f,side*0.125f,-0.20f+lift,Moss,pen.lw*2.5f)
            }
            for (i in 0..2) drawCircle(if(f.on) Honey else Birch,u*0.006f,Offset((-0.06f+i*0.06f)*u,-0.023f*u))
        }
        else -> Unit
    }
    return true
}

fun DrawScope.drawFinaleFront(f: Fixture, u: Float, pen: Pen): Boolean {
    if (f.type !in FinalePlay.TYPES) return false
    if (f.type == FixtureType.PLAY_DRAGON_CART || f.type == FixtureType.PLAY_AIRSHIP) {
        // A low front rim covers feet, never the passengers' faces.
        val right = if(f.type==FixtureType.PLAY_AIRSHIP) 0.3f else 0.17f
        inkedRound(Rect(-0.245f*u,-0.091f*u,right*u,-0.04f*u),u*0.012f,if(f.type==FixtureType.PLAY_AIRSHIP) Wood else Moss,pen)
        drawLine(Honey,Offset(-0.23f*u,-0.087f*u),Offset((right-0.01f)*u,-0.087f*u),pen.lw*1.3f,StrokeCap.Round)
        for (i in 0..2) drawCircle(Birch,u*0.007f,Offset((-0.17f+i*0.105f)*u,-0.065f*u))
    }
    return true
}

/** Small toy troll, reused by the hiding game, party button and waterfall finale. */
internal fun DrawScope.finaleTroll(at: Offset, r: Float, pen: Pen, smiling: Boolean) {
    for (side in intArrayOf(-1,1)) inkedOval(Rect(at.x+side*r*0.9f-r*0.3f,at.y-r*0.6f,at.x+side*r*0.9f+r*0.3f,at.y+r*0.1f),Moss,pen)
    inkedCircle(at,r,Moss,pen)
    for (side in intArrayOf(-1,1)) {
        drawCircle(Color.White,r*0.25f,Offset(at.x+side*r*0.35f,at.y-r*0.1f))
        drawCircle(Ink.line,r*0.10f,Offset(at.x+side*r*0.35f,at.y-r*0.1f))
    }
    drawCircle(Honey,r*0.18f,Offset(at.x,at.y+r*0.18f))
    if(smiling) drawArc(Ink.line,15f,150f,false,Offset(at.x-r*0.43f,at.y+r*0.12f),Size(r*0.86f,r*0.55f),style=pen.stroke)
    else drawLine(Ink.line,Offset(at.x-r*0.25f,at.y+r*0.48f),Offset(at.x+r*0.25f,at.y+r*0.48f),pen.lw*0.5f,StrokeCap.Round)
}

/** A small, steady sheen when motion is off; no extra bitmap or particles per toy. */
fun DrawScope.drawToySheen(f: Fixture, u: Float, pen: Pen, motion: Boolean) {
    if(f.on || f.lift>0f) return
    val shimmer = if(motion) (sin(pen.t*0.8f+f.id)*0.5f+0.5f) else 0.5f
    val c = Color.White.copy(alpha=0.25f+shimmer*0.25f)
    val at = Offset(f.spec.w*0.22f*u,-f.spec.h*0.77f*u)
    val r = minOf(f.spec.w,f.spec.h,0.3f)*0.035f*u
    drawLine(c,Offset(at.x-r,at.y),Offset(at.x+r,at.y),pen.lw*0.5f,StrokeCap.Round)
    drawLine(c,Offset(at.x,at.y-r*1.7f),Offset(at.x,at.y+r*1.7f),pen.lw*0.5f,StrokeCap.Round)
}
