package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.clipRect
import app.trollfoss.domain.*

/** Saved portraits contain their own look; changing the sitter never changes yesterday's picture. */
object ToyArt { var photos: Map<Int, Person> = emptyMap() }
val ToyColors = listOf(0xFFFF5A4E,0xFF73BFE8,0xFF8AD5B4,0xFFFFCE69,0xFFAD91DC,0xFFF7A3BD).map { Color(it) }
private val ToyWood=Color(0xFFDEA66C)
private val ToyDark=Color(0xFF564768)
private val ToyPale=Color(0xFFFFF0CC)

fun DrawScope.drawToyBack(f: Fixture,u: Float,pen: Pen): Boolean {
    if (drawCreativeBack(f,u,pen)) return true
    if(f.type==FixtureType.PICTURE && f.variant>=ToyPlay.PHOTO_BASE && f.variant<Community.ART_BASE) {
        val r=Rect(-f.spec.w*u/2,-f.spec.h*u,f.spec.w*u/2,0f)
        inkedRound(r,0.015f*u,ToyWood,pen)
        inkedRound(Rect(r.left+0.015f*u,r.top+0.015f*u,r.right-0.015f*u,-0.015f*u),0.008f*u,Color(0xFFD3E9DD),pen)
        ToyArt.photos[f.variant-ToyPlay.PHOTO_BASE]?.let { p ->
            clipRect(r.left+0.015f*u,r.top+0.015f*u,r.right-0.015f*u,-0.015f*u) {
                translate(0f,-0.025f*u) { drawPerson(p.species,p.look,Pose.STAND,PersonAnim().apply { face=Face.GRIN },f.spec.h*u*0.82f,pen) }
            }
        }
        return true
    }
    if(f.type !in ToyPlay.TYPES) return false
    fun round(l:Float,t:Float,r:Float,b:Float,color:Color,radius:Float=0.018f) = inkedRound(Rect(l*u,t*u,r*u,b*u),radius*u,color,pen)
    fun circle(x:Float,y:Float,r:Float,color:Color) = inkedCircle(Offset(x*u,y*u),r*u,color,pen)
    fun line(x:Float,y:Float,xx:Float,yy:Float,color:Color=Ink.line,width:Float=pen.lw) = drawLine(color,Offset(x*u,y*u),Offset(xx*u,yy*u),width,StrokeCap.Round)
    when(f.type) {
        FixtureType.PLAY_BUS,FixtureType.PLAY_TRAIN -> {
            val train=f.type==FixtureType.PLAY_TRAIN
            for(x in listOf(-0.19f,0.19f)) { circle(x,-0.045f,0.046f,ToyDark);circle(x,-0.045f,0.020f,ToyColors[2]) }
            round(-0.28f,-0.25f,0.28f,-0.08f,if(train) ToyColors[1] else ToyColors[3],0.04f)
            round(-0.23f,-0.23f,0.15f,-0.12f,ToyPale)
            if(train) {
                round(0.13f,-0.29f,0.23f,-0.23f,ToyDark)
                circle(-0.02f,-0.16f,0.042f,if(f.variant==1) ToyDark else ToyColors[0])
                if(f.variant==1) { line(-0.045f,-0.185f,0.005f,-0.135f,ToyColors[0]);line(-0.045f,-0.135f,0.005f,-0.185f,ToyColors[0]) }
                else line(-0.08f,-0.16f,0.035f,-0.16f)
            }
            else { round(-0.25f,-0.285f,0.22f,-0.25f,ToyColors[0]);circle(0.245f,-0.12f,0.013f,ToyPale) }
        }
        FixtureType.PLAY_BUBBLES -> {
            round(-0.13f,-0.15f,0.13f,-0.02f,ToyColors[2],0.04f)
            round(-0.07f,-0.205f,0.07f,-0.12f,ToyColors[1])
            circle(0f,-0.22f,0.028f,ToyPale);circle(0.075f,-0.19f,0.015f,ToyColors[0])
            for(i in 0..2) circle(-0.10f+i*0.09f,-0.25f-i*0.035f,0.018f,Color(0xFFCBE9F5))
            line(-0.09f,-0.055f,0.09f,-0.055f,Color.White,pen.lw)
        }
        FixtureType.PLAY_WINDMILL -> {
            round(-0.11f,-0.06f,0.11f,0f,ToyWood);round(-0.025f,-0.34f,0.025f,-0.03f,ToyPale)
            translate(0f,-0.29f*u) { rotate(f.angle*57.3f,pivot=Offset.Zero) {
                repeat(4) { i -> rotate(i*90f,pivot=Offset.Zero) {
                    inked(Path().apply { moveTo(0f,0f);lineTo(0.045f*u,-0.15f*u);quadraticBezierTo(0.135f*u,-0.13f*u,0.025f*u,-0.02f*u);close() },ToyColors[i+1],pen)
                } }
                inkedCircle(Offset.Zero,0.024f*u,ToyColors[0],pen)
            } }
        }
        FixtureType.PLAY_LAUNCHER -> {
            round(-0.14f,-0.04f,0.14f,0f,ToyWood)
            val pillow=Path().apply { moveTo(-0.13f*u,-0.04f*u);quadraticBezierTo(-0.16f*u,-0.14f*u,0f,-0.095f*u);quadraticBezierTo(0.16f*u,-0.14f*u,0.13f*u,-0.04f*u);close() }
            inked(pillow,ToyColors[4],pen);line(-0.07f,-0.07f,0.07f,-0.07f,ToyPale)
        }
        FixtureType.PLAY_MARBLES -> {
            round(-0.27f,-0.38f,-0.235f,0f,ToyWood);round(0.235f,-0.38f,0.27f,0f,ToyWood)
            repeat(3) { i ->
                val down=f.mode and (1 shl i)!=0;val yy=-0.32f+i*0.1f
                val left=yy+if(i%2==0) 0f else 0.04f;val right=yy+if(i%2==0) 0.04f else 0f
                line(-0.23f,if(down) left else right,0.23f,if(down) right else left,Ink.line,pen.lw*3f)
                line(-0.23f,if(down) left else right,0.23f,if(down) right else left,ToyColors[i+1],pen.lw*1.7f)
                circle(0f,yy+0.02f,0.017f,ToyPale)
            }
            round(-0.27f,-0.035f,0.27f,0f,ToyColors[2])
        }
        FixtureType.PLAY_COLORS -> {
            round(-0.11f,-0.13f,0.11f,-0.015f,ToyColors[f.mode.mod(6)],0.035f)
            round(-0.075f,-0.22f,0.075f,-0.12f,ToyPale);round(-0.11f,-0.28f,0.05f,-0.22f,ToyDark)
            circle(0f,-0.065f,0.032f,Color.White)
            for(i in 0..2) circle(-0.09f+i*0.065f,-0.25f,0.009f,ToyColors[(f.mode+i).mod(6)])
        }
        FixtureType.PLAY_LIFT -> {
            round(-0.16f,-0.035f,0.16f,0f,ToyColors[1])
            round(-0.15f,-0.47f,-0.125f,-0.02f,ToyWood);round(0.125f,-0.47f,0.15f,-0.02f,ToyWood)
            round(-0.16f,-0.48f,0.16f,-0.445f,ToyColors[1]);circle(0f,-0.46f,0.025f,ToyPale)
            line(0f,-0.43f,0f,-0.055f-f.angle*0.28f,ToyDark,pen.lw)
            round(-0.12f,-0.065f-f.angle*0.28f,0.12f,-0.035f-f.angle*0.28f,ToyColors[2])
        }
        FixtureType.PLAY_POPCORN -> {
            for(x in listOf(-0.11f,0.11f)) circle(x,-0.033f,0.034f,ToyDark)
            round(-0.16f,-0.15f,0.16f,-0.055f,ToyColors[0]);round(-0.13f,-0.30f,0.13f,-0.15f,ToyPale)
            round(-0.165f,-0.37f,0.165f,-0.30f,ToyColors[0])
            repeat(5) { i -> round(-0.14f+i*0.06f,-0.36f,-0.115f+i*0.06f,-0.31f,ToyPale,0.005f) }
            for(i in 0..3) circle(-0.08f+i*0.055f,-0.16f,0.023f,Color(0xFFFFDF79))
            circle(0f,-0.10f,0.024f,ToyColors[3])
        }
        FixtureType.PLAY_PUMP -> {
            round(-0.08f,-0.03f,0.08f,0f,ToyDark);round(-0.035f,-0.19f,0.035f,-0.025f,ToyColors[0])
            line(0f,-0.19f,0f,-0.24f,ToyDark,pen.lw*2);round(-0.08f,-0.265f,0.08f,-0.235f,ToyColors[2])
            val hose=Path().apply { moveTo(0.025f*u,-0.06f*u);cubicTo(0.15f*u,-0.13f*u,0.15f*u,-0.015f*u,0.07f*u,-0.005f*u) }
            drawPath(hose,ToyDark,style=pen.stroke)
        }
        FixtureType.PLAY_CAMERA -> {
            line(0f,-0.15f,0f,-0.035f,ToyDark,pen.lw*2);line(0f,-0.08f,-0.08f,0f);line(0f,-0.08f,0.08f,0f)
            round(-0.095f,-0.29f,0.095f,-0.17f,ToyColors[4]);round(-0.06f,-0.33f,0.015f,-0.28f,ToyDark)
            circle(0f,-0.23f,0.047f,ToyDark);circle(0f,-0.23f,0.029f,ToyColors[1]);circle(0.056f,-0.265f,0.012f,ToyPale)
        }
        else -> Unit
    }
    return true
}

fun DrawScope.drawToyFront(f:Fixture,u:Float,pen:Pen):Boolean {
    if (drawCreativeFront(f,u,pen)) return true
    if(f.type !in ToyPlay.TYPES) return false
    if(f.type==FixtureType.PLAY_BUS || f.type==FixtureType.PLAY_TRAIN) {
        inkedRound(Rect(-0.28f*u,-0.11f*u,0.28f*u,-0.065f*u),0.016f*u,if(f.type==FixtureType.PLAY_BUS) ToyColors[3] else ToyColors[1],pen)
        repeat(3) { i -> inkedCircle(Offset((-0.19f+i*0.16f)*u,-0.088f*u),0.012f*u,ToyPale,pen) }
    }
    return true
}

fun DrawScope.drawToyIngredient(type:ThingType,w:Float,h:Float,pen:Pen) {
    if(type==ThingType.PLAY_CORN) {
        inkedRound(Rect(-w*0.4f,-h,w*0.4f,0f),w*0.08f,ToyColors[3],pen)
        for(i in 0..2) for(j in 0..2) inkedCircle(Offset((i-1)*w*0.23f,-h*(0.22f+j*0.25f)),w*0.11f,ToyPale,pen)
    } else {
        translate(0f,-h/2) {
            repeat(8) { i -> rotate(i*45f,pivot=Offset.Zero) { inkedRound(Rect(-w*0.13f,-h*0.48f,w*0.13f,-h*0.22f),w*0.04f,ToyColors[3],pen) } }
            inkedCircle(Offset.Zero,w*0.33f,ToyColors[3],pen);inkedCircle(Offset.Zero,w*0.14f,ToyDark,pen)
        }
    }
}
