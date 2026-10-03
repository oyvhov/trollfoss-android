package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import app.trollfoss.domain.*
import kotlin.math.sin

object CreativeArt {
    var state=CommunityState()
    var bodies: Map<Int,Body> = emptyMap()
    var place=PlaceId.HOME
}
object CreativeIcons {
    val Music:DrawScope.()->Unit = { val s=size.minDimension;drawLine(Ink.line,Offset(s*0.6f,s*0.7f),Offset(s*0.6f,s*0.2f),s*0.09f);inkedOval(Rect(s*0.2f,s*0.6f,s*0.6f,s*0.85f),ToyColors[4],Pen(s*0.04f));drawLine(Ink.line,Offset(s*0.6f,s*0.2f),Offset(s*0.85f,s*0.32f),s*0.09f) }
    val Paint:DrawScope.()->Unit = { val s=size.minDimension;inkedRound(Rect(s*0.42f,s*0.18f,s*0.58f,s*0.65f),s*0.06f,ToyColors[3],Pen(s*0.04f));inkedRound(Rect(s*0.3f,s*0.55f,s*0.7f,s*0.85f),s*0.05f,ToyColors[1],Pen(s*0.04f)) }
    val Home:DrawScope.()->Unit = { val s=size.minDimension;inkedRound(Rect(s*0.2f,s*0.42f,s*0.8f,s*0.85f),s*0.04f,ToyColors[3],Pen(s*0.04f));inked(Path().apply { moveTo(s*0.12f,s*0.44f);lineTo(s*0.5f,s*0.12f);lineTo(s*0.88f,s*0.44f);close() },ToyColors[0],Pen(s*0.04f));inkedRound(Rect(s*0.4f,s*0.57f,s*0.6f,s*0.85f),s*0.03f,ToyColors[4],Pen(s*0.04f)) }
    val Weather:DrawScope.()->Unit = { val s=size.minDimension;inkedCircle(Offset(s*0.66f,s*0.28f),s*0.17f,ToyColors[3],Pen(s*0.03f));for(i in 0..2) inkedCircle(Offset(s*(0.3f+i*0.18f),s*(if(i==1) 0.42f else 0.5f)),s*0.18f,Color.White,Pen(s*0.03f));for(i in 0..2) drawLine(ToyColors[1],Offset(s*(0.32f+i*0.18f),s*0.72f),Offset(s*(0.27f+i*0.18f),s*0.86f),s*0.055f,StrokeCap.Round) }
}
fun DrawScope.drawFriendAction(action:FriendAction) {
    val s=size.minDimension;val pen=Pen(s*0.03f)
    for(i in 0..1) translate(s*(0.33f+i*0.32f),s*0.89f) { drawPerson(Species.FOLK,Look(topColor=i*3,hair=i+2),Pose.STAND,PersonAnim().apply { face=Face.GRIN;wave=if(action==FriendAction.HIGH_FIVE) 1f else 0f },s*0.65f,pen) }
    if(action==FriendAction.HUG) inked(starPath(Offset(s*0.5f,s*0.18f),s*0.1f,s*0.045f),ToyColors[0],pen)
    else drawLine(ToyColors[3],Offset(s*0.35f,s*0.54f),Offset(s*0.65f,s*(if(action==FriendAction.HIGH_FIVE) 0.3f else 0.54f)),s*0.07f,StrokeCap.Round)
}
fun DrawScope.drawTemperament(t:Temperament) {
    val s=size.minDimension;translate(s/2,s*0.88f) { drawPerson(Species.FOLK,Look(hair=3),Pose.STAND,PersonAnim().apply { face=when(t) { Temperament.PLAYFUL -> Face.LAUGH;Temperament.CALM -> Face.GRIN;Temperament.CURIOUS -> Face.OOH };wave=if(t==Temperament.PLAYFUL) 0.8f else 0f },s*0.75f,Pen(s*0.025f)) }
}
fun DrawScope.drawArtwork(marks:List<ArtMark>,r:Rect,pen:Pen) {
    for(m in marks.take(80)) {
        val at=Offset(r.left+m.x*r.width,r.top+m.y*r.height)
        val s=r.width*0.055f;val color=ToyColors[m.color.mod(6)]
        when(m.shape) {
            0 -> inkedCircle(at,s,color,pen)
            1 -> inkedRound(Rect(at.x-s,at.y-s,at.x+s,at.y+s),s*0.3f,color,pen)
            else -> inked(starPath(at,s*1.2f,s*0.55f),color,pen)
        }
    }
}
fun DrawScope.drawCreativeBack(f:Fixture,u:Float,pen:Pen):Boolean {
    if(f.type==FixtureType.PICTURE && f.variant>=Community.ART_BASE) {
        val r=Rect(-f.spec.w*u/2,-f.spec.h*u,f.spec.w*u/2,0f)
        inkedRound(r,u*0.01f,Color(0xFFDEA66C),pen)
        val inside=Rect(r.left+u*0.016f,r.top+u*0.016f,r.right-u*0.016f,r.bottom-u*0.016f)
        drawRect(Color(0xFFFFFAEE),inside.topLeft,inside.size)
        clipRect(inside.left,inside.top,inside.right,inside.bottom) { drawArtwork(CreativeArt.state.art[f.variant-Community.ART_BASE].orEmpty(),inside,pen) };return true
    }
    if(f.type !in CreativePlay.TYPES) return false
    val wood=Color(0xFFDEA66C);val pale=Color(0xFFFFF0CC);val dark=Color(0xFF564768)
    fun box(l:Float,t:Float,r:Float,b:Float,c:Color) {
        val rect=Rect(l*u,t*u,r*u,b*u)
        inkedRound(rect,u*0.012f,c,pen)
        if(rect.width>pen.lw*6 && rect.height>pen.lw*5) {
            drawLine(Color.White.copy(alpha=0.35f),Offset(rect.left+pen.lw*2,rect.top+pen.lw*2),Offset(rect.right-pen.lw*2,rect.top+pen.lw*2),pen.lw,StrokeCap.Round)
            drawLine(Ink.line.copy(alpha=0.14f),Offset(rect.left+pen.lw*2,rect.bottom-pen.lw*2),Offset(rect.right-pen.lw*2,rect.bottom-pen.lw*2),pen.lw*1.7f,StrokeCap.Round)
        }
    }
    fun dot(x:Float,y:Float,r:Float,c:Color) {
        inkedCircle(Offset(x*u,y*u),r*u,c,pen)
        if(r>0.03f) drawOval(Color.White.copy(alpha=0.24f),Offset((x-r*0.55f)*u,(y-r*0.65f)*u),Size(r*u,r*u*0.45f))
    }
    fun line(x:Float,y:Float,xx:Float,yy:Float,c:Color=Ink.line,w:Float=pen.lw)=drawLine(c,Offset(x*u,y*u),Offset(xx*u,yy*u),w,StrokeCap.Round)
    when(f.type) {
        FixtureType.PLAY_SEESAW -> {
            inked(Path().apply { moveTo(-0.06f*u,0f);lineTo(0f,-0.14f*u);lineTo(0.06f*u,0f);close() },ToyColors[4],pen)
            line(-0.31f,-0.1f+f.angle,0.31f,-0.1f-f.angle,Ink.line,pen.lw*5)
            line(-0.31f,-0.1f+f.angle,0.31f,-0.1f-f.angle,wood,pen.lw*3)
            for(side in listOf(-1f,1f)) { val y=-0.1f-f.angle*side;box(side*0.25f-0.055f,y-0.01f,side*0.25f+0.055f,y+0.02f,ToyColors[1]);line(side*0.21f,y,side*0.21f,y-0.055f) }
        }
        FixtureType.PLAY_PUPPETS -> {
            box(-0.25f,-0.48f,0.25f,-0.02f,wood);box(-0.21f,-0.4f,0.21f,-0.13f,dark)
            for(i in 0..1) { val xx=-0.09f+i*0.18f;val yy=-0.21f+if(f.on) sin(pen.t*3+i)*0.02f else 0f
                box(xx-0.04f,yy-0.07f,xx+0.04f,yy,ToyColors[(i+f.mode).mod(6)]);dot(xx,yy-0.085f,0.04f,pale);dot(xx-0.013f,yy-0.09f,0.006f,dark);dot(xx+0.013f,yy-0.09f,0.006f,dark) }
            box(-0.26f,-0.48f,0.26f,-0.43f,ToyColors[0])
        }
        FixtureType.PLAY_TANDEM -> {
            for(x in listOf(-0.22f,0.22f)) { dot(x,-0.06f,0.062f,dark);dot(x,-0.06f,0.046f,pale);repeat(4) { i -> rotate(i*45f+f.angle*90,pivot=Offset(x*u,-0.06f*u)) { line(x-0.045f,-0.06f,x+0.045f,-0.06f,ToyColors[1]) } } }
            line(-0.22f,-0.06f,-0.1f,-0.15f,ToyColors[0],pen.lw*3);line(-0.1f,-0.15f,0.05f,-0.06f,ToyColors[0],pen.lw*3);line(0.05f,-0.06f,0.22f,-0.18f,ToyColors[0],pen.lw*3)
            for(x in listOf(-0.12f,0.12f)) { line(x,-0.06f,x,-0.13f);box(x-0.045f,-0.145f,x+0.035f,-0.125f,dark) }
            line(0.22f,-0.18f,0.2f,-0.245f);line(0.17f,-0.245f,0.25f,-0.245f);dot(0.21f,-0.23f,0.014f,ToyColors[3])
        }
        FixtureType.PLAY_PICNIC -> {
            box(-0.33f,-0.035f,0.33f,0f,ToyColors[0]);repeat(7) { i -> line(-0.3f+i*0.1f,-0.035f,-0.3f+i*0.1f,0f,pale,pen.lw*2) }
            box(-0.09f,-0.09f,0.09f,-0.04f,wood)
            if(f.open) { line(-0.09f,-0.09f,0.06f,-0.12f,wood,pen.lw*3);dot(-0.04f,-0.10f,0.024f,ToyColors[0]);dot(0.025f,-0.10f,0.022f,ToyColors[3]) }
        }
        FixtureType.PLAY_CRANE -> {
            box(-0.22f,-0.04f,0.14f,0f,ToyColors[3]);box(-0.19f,-0.48f,-0.15f,-0.03f,wood);line(-0.18f,-0.47f,0.28f,-0.47f,ToyColors[3],pen.lw*4)
            val x=if(f.mode==2) 0.22f else -0.1f;val y=-0.08f-f.angle*0.32f
            line(x,-0.47f,x,y,dark);line(x-0.035f,y-0.02f,x,y+0.025f,dark,pen.lw*2);line(x,y+0.025f,x+0.035f,y-0.02f,dark,pen.lw*2)
        }
        FixtureType.PLAY_CONVEYOR -> {
            box(-0.31f,-0.12f,0.31f,-0.035f,dark);for(i in 0..6) dot(-0.27f+i*0.09f,-0.078f,0.031f,ToyColors[1])
            box(-0.27f,-0.035f,-0.23f,0f,wood);box(0.23f,-0.035f,0.27f,0f,wood)
            repeat(8) { i -> val x=-0.28f+(i*0.075f+(if(f.on) pen.t*f.mode*0.06f else 0f)).mod(0.6f);line(x,-0.115f,x+0.024f,-0.115f,pale) }
        }
        FixtureType.PLAY_BUILD -> {
            box(-0.19f,-0.17f,0.19f,-0.14f,wood);for(x in listOf(-0.15f,0.15f)) box(x-0.025f,-0.14f,x+0.025f,0f,wood)
            for(i in 0..2) box(-0.13f+i*0.1f,-0.22f,-0.085f+i*0.1f,-0.17f,ToyColors[i+1])
        }
        FixtureType.PLAY_CHANNEL -> {
            line(-0.24f,-0.145f,0.24f,-0.04f,Ink.line,pen.lw*5);line(-0.24f,-0.145f,0.24f,-0.04f,wood,pen.lw*3)
            for(x in listOf(-0.18f,0.18f)) line(x,if(x<0) -0.12f else -0.04f,x,0f,wood,pen.lw*3)
            if(f.count>0 && f.on) repeat(4) { i -> val x=-0.22f+(i*0.12f+pen.t*0.15f).mod(0.45f);dot(x,-0.143f+(x+0.22f)*0.21f,0.014f,ToyColors[1]) }
        }
        FixtureType.PLAY_MIRROR -> {
            box(-0.16f,-0.46f,0.16f,-0.025f,ToyColors[4]);box(-0.125f,-0.42f,0.125f,-0.065f,Color(0xFFBCE5F3))
            (CreativeArt.bodies[f.count] as? Person)?.let { p -> clipRect(-0.12f*u,-0.41f*u,0.12f*u,-0.06f*u) { translate(0f,-0.065f*u) { drawPerson(p.species,p.look,p.anim.pose,p.anim,0.29f*u,pen) } } }
            line(-0.105f,-0.14f,0.075f,-0.38f,Color.White,pen.lw*1.5f)
        }
        FixtureType.PLAY_HOVER -> {
            box(-0.16f,-0.095f-f.angle*0.22f,0.16f,-0.04f-f.angle*0.22f,ToyColors[4]);repeat(3) { i -> inked(starPath(Offset((-0.1f+i*0.1f)*u,-0.025f*u),0.017f*u,0.007f*u),ToyColors[3],pen) }
        }
        FixtureType.PLAY_CLOUD -> {
            box(-0.105f,-0.32f,0.105f,-0.035f,Color(0xFFC6E9F1));box(-0.13f,-0.355f,0.13f,-0.32f,wood);box(-0.13f,-0.035f,0.13f,0f,wood)
            for(i in 0..2) dot(-0.055f+i*0.055f,-0.24f-if(i==1) 0.025f else 0f,0.043f,Color.White)
            if(f.on) repeat(4) { i -> line(-0.075f+i*0.05f,-0.16f,-0.075f+i*0.05f,-0.10f,ToyColors[1],pen.lw*2) }
        }
        FixtureType.PLAY_PORTAL -> {
            inkedOval(Rect(-0.17f*u,-0.46f*u,0.17f*u,0f),ToyColors[f.variant.mod(6)],pen);inkedOval(Rect(-0.125f*u,-0.405f*u,0.125f*u,-0.04f*u),pale,pen)
            repeat(3) { i -> inked(starPath(Offset(sin(pen.t+i)*0.05f*u,(-0.11f-i*0.105f)*u),0.024f*u,0.01f*u),ToyColors[f.variant.mod(6)],pen) }
        }
        FixtureType.PLAY_TREE -> {
            box(-0.13f,-0.09f,0.13f,0f,wood);box(-0.02f,-0.12f-f.mode*0.06f,0.02f,-0.07f,wood)
            val y=-0.13f-f.mode*0.06f;for(i in 0..2) dot(-0.065f+i*0.065f,y-if(i==1) 0.035f else 0f,0.05f+f.mode*0.012f,ToyColors[2])
            if(f.mode>=3) for(i in 0..2) dot(-0.065f+i*0.065f,y-0.01f,0.018f,ToyColors[0])
        }
        FixtureType.PLAY_REPAIR -> {
            box(-0.18f,-0.045f,0.18f,0f,wood);box(-0.035f,-0.30f,0.035f,-0.04f,dark);dot(0f,-0.285f,0.066f,if(f.mode>0 && f.on) ToyColors[3] else ToyColors[1])
            if(f.mode==0) { line(-0.02f,-0.335f,0.02f,-0.28f,ToyColors[0]);line(0.02f,-0.28f,-0.02f,-0.24f,ToyColors[0]) }
            else if(f.on) for(i in -1..1) line(i*0.10f,-0.34f,i*0.13f,-0.38f,ToyColors[3],pen.lw*2)
        }
        FixtureType.PLAY_DOOR -> {
            box(-0.19f,-0.49f,0.19f,0f,wood);box(-0.15f,-0.45f,0.15f,-0.035f,dark)
            for(row in 0..2) { line(-0.15f,-0.08f-row*0.12f,0.15f,-0.08f-row*0.12f,wood,pen.lw*3);for(i in 0..4) box(-0.13f+i*0.055f,-0.18f-row*0.12f,-0.09f+i*0.055f,-0.085f-row*0.12f,ToyColors[(i+row).mod(6)]) }
            dot(0.12f,-0.2f,0.017f,ToyColors[3]);inked(starPath(Offset(0f,-0.47f*u),u*0.018f,u*0.008f),pale,pen)
        }
        FixtureType.PLAY_TUNNEL -> {
            inkedRound(Rect(-0.225f*u,-0.3f*u,0.225f*u,0f),0.15f*u,ToyColors[1],pen)
            inkedRound(Rect(-0.16f*u,-0.25f*u,0.16f*u,0.01f*u),0.13f*u,ToyColors[4],pen)
            inkedRound(Rect(-0.125f*u,-0.22f*u,0.125f*u,0.01f*u),0.11f*u,dark,pen)
            for(side in listOf(-1,1)) for(i in 0..2) dot(side*0.185f,-0.065f-i*0.065f,0.014f,pale)
            inked(starPath(Offset(0f,-0.273f*u),0.024f*u,0.011f*u),ToyColors[3],pen)
        }
        FixtureType.PLAY_JUMP -> {
            box(-0.2f,-0.025f,0.2f,0.01f,ToyColors[4])
            inked(Path().apply { moveTo(-0.18f*u,-0.025f*u);lineTo(-0.18f*u,-0.17f*u);quadraticTo(-0.17f*u,-0.21f*u,-0.12f*u,-0.18f*u);lineTo(0.18f*u,-0.025f*u);close() },ToyColors[2],pen)
            line(-0.13f,-0.17f,0.15f,-0.025f,pale,pen.lw*2)
            for(i in 0..2) inked(starPath(Offset((-0.12f+i*0.07f)*u,(-0.075f+i*0.015f)*u),0.017f*u,0.008f*u),ToyColors[3],pen)
        }
        FixtureType.PLAY_WATER_WHEEL -> {
            box(-0.035f,-0.15f,0.035f,0f,wood);dot(0f,-0.2f,0.15f,wood);dot(0f,-0.2f,0.115f,ToyColors[1])
            translate(0f,-0.2f*u) { rotate(f.angle*57.3f,pivot=Offset.Zero) { repeat(8) { i -> rotate(i*45f,pivot=Offset.Zero) { line(0f,0f,0f,-0.13f,wood,pen.lw*3);box(-0.03f,-0.15f,0.03f,-0.11f,wood) } } } };dot(0f,-0.2f,0.023f,pale)
        }
        FixtureType.PLAY_ART -> {
            line(-0.13f,0f,0f,-0.43f,wood,pen.lw*3);line(0.13f,0f,0f,-0.43f,wood,pen.lw*3);box(-0.15f,-0.37f,0.15f,-0.12f,pale)
            for(i in 0..2) dot(-0.065f+i*0.06f,-0.25f-i*0.025f,0.04f,ToyColors[i+1])
        }
        FixtureType.PLAY_RESCUE -> {
            box(-0.33f,-0.045f,0.33f,0f,ToyColors[1]);for(x in listOf(-0.3f,0.3f)) { dot(x,-0.035f,0.035f,ToyColors[2]);dot(x,-0.10f,0.028f,ToyColors[3]) }
            if(f.mode==1) box(-0.33f,-0.095f,0.33f,-0.075f,wood)
            else { dot(-0.24f,-0.12f,0.016f,pale);dot(0.24f,-0.12f,0.016f,pale) }
        }
        else -> Unit
    };return true
}
fun DrawScope.drawCreativeFront(f:Fixture,u:Float,pen:Pen):Boolean {
    if(f.type !in CreativePlay.TYPES) return false
    if(f.type==FixtureType.PLAY_PUPPETS) for(side in listOf(-1f,1f)) inkedRound(Rect((side*0.21f-0.035f)*u,-0.41f*u,(side*0.21f+0.035f)*u,-0.1f*u),0.016f*u,ToyColors[0],pen)
    return true
}
fun DrawScope.cloudIslandBack(cam:Float,u:Float,pen:Pen) {
    val top=size.height-u
    val sky=androidx.compose.ui.graphics.lerp(Color(0xFF8CCCEB),Color(0xFF25214F),pen.night)
    val cloud=androidx.compose.ui.graphics.lerp(Color(0xFFFFFAF1),Color(0xFF8585AF),pen.night*0.7f)
    drawRect(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(sky,cloud),startY=-top,endY=u),Offset(0f,-top),Size(size.width,size.height))
    // Soft cloud banks, a distant rainbow and the moored bed balloon frame the landing.
    for(i in 0..4) {
        val r=(0.43f-i*0.027f)*u
        drawArc(ToyColors[i].copy(alpha=0.48f*(1f-pen.night)),180f,180f,false,
            Offset((1.9f-cam*0.16f)*u-r,0.03f*u-r),Size(r*2,r*2),style=Stroke(u*0.023f))
    }
    repeat(6) { i ->
        val x=(i*0.71f-cam*0.16f)*u
        val y=(-0.16f+(i%3)*0.24f)*u
        cloudPuff(Offset(x,y),u*(0.14f+i%2*0.06f),cloud,pen)
    }
    if(pen.night>0.1f) repeat(18) { i ->
        val at=Offset(((i*0.317f)%2.8f-cam*0.08f)*u,(-0.18f+(i%5)*0.13f)*u)
        inked(starPath(at,u*0.008f,u*0.003f),ToyColors[3].copy(alpha=pen.night),pen)
    }
    val balloon=Offset((0.58f-cam)*u,(0.31f+sin(pen.t*0.6f)*0.008f)*u)
    drawLine(Color(0xFFC79E75),balloon+Offset(u*0.13f,u*0.20f),Offset((0.81f-cam)*u,0.82f*u),pen.lw*1.3f)
    drawBalloon(balloon,u*0.13f,pen)
    // A cloud cushion and a quiet meadow, with flowers kept behind the play strip.
    repeat(10) { i -> inkedOval(Rect((i*0.4f-0.2f-cam)*u,0.76f*u,(i*0.4f+0.3f-cam)*u,1.07f*u),cloud,pen) }
    val meadow=Rect(-cam*u,0.79f*u,(3.6f-cam)*u,0.99f*u)
    inkedRound(meadow,0.10f*u,androidx.compose.ui.graphics.lerp(Color(0xFFC4E4CA),Color(0xFF546881),pen.night*0.8f),pen)
    for(i in 0..5) {
        val x=(i*0.58f+0.15f-cam)*u
        drawLine(Color(0xFF568875),Offset(x,0.825f*u),Offset(x,0.765f*u),pen.lw*1.3f)
        inked(starPath(Offset(x,0.76f*u),u*0.022f,u*0.012f),ToyColors[3],pen)
        inkedCircle(Offset(x,0.76f*u),u*0.007f,cloud,pen)
    }
}

/** One outline around a cloud, so its lobes do not look like overlapping buttons. */
fun DrawScope.cloudPuff(at:Offset,r:Float,color:Color,pen:Pen) {
    val shape=Path().apply {
        moveTo(at.x-r,at.y+r*0.32f)
        cubicTo(at.x-r*1.55f,at.y+r*0.28f,at.x-r*1.45f,at.y-r*0.38f,at.x-r*0.7f,at.y-r*0.35f)
        cubicTo(at.x-r*0.62f,at.y-r*1.15f,at.x+r*0.52f,at.y-r*1.1f,at.x+r*0.6f,at.y-r*0.4f)
        cubicTo(at.x+r*1.4f,at.y-r*0.55f,at.x+r*1.6f,at.y+r*0.3f,at.x+r,at.y+r*0.32f)
        close()
    }
    inked(shape,color,pen)
    drawLine(Color.White.copy(alpha=0.42f),Offset(at.x-r*0.7f,at.y+r*0.12f),Offset(at.x+r*0.55f,at.y+r*0.12f),r*0.09f,StrokeCap.Round)
}
