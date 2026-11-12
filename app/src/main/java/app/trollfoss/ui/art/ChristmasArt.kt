package app.trollfoss.ui.art

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import app.trollfoss.domain.*
import kotlin.math.*

internal val XmasRed=Color(0xFFB83E49)
internal val XmasGreen=Color(0xFF276E55)
internal val XmasGold=Color(0xFFF4CB73)
internal val XmasCream=Color(0xFFFFF1DC)
internal val XmasSnow=Color(0xFFF2F7FF)
internal fun xmasColor(v:Int)=listOf(XmasRed,Color(0xFFD2B497),Color(0xFF8CBACB))[v.mod(3)]
internal fun xmasAccent(v:Int)=listOf(XmasGreen,Color(0xFF9F6850),Color(0xFFE4EDF6))[v.mod(3)]
internal fun xmasStar(x:Float,y:Float,s:Float)=Path().apply {
    for(i in 0..9) { val a=-PI/2+i*PI/5;val q=if(i%2==0) s else s*0.44f;val px=x+cos(a).toFloat()*q;val py=y+sin(a).toFloat()*q;if(i==0) moveTo(px,py) else lineTo(px,py) };close()
}
private fun heart(x:Float,y:Float,s:Float)=Path().apply { moveTo(x,y+s);cubicTo(x-s*2,y-s*0.3f,x-s,y-s*1.8f,x,y-s*0.5f);cubicTo(x+s,y-s*1.8f,x+s*2,y-s*0.3f,x,y+s);close() }
private fun triangle(x:Float,y:Float,w:Float,h:Float)=Path().apply { moveTo(x,y-h);lineTo(x+w/2,y);lineTo(x-w/2,y);close() }
private fun DrawScope.r(x:Float,y:Float,w:Float,h:Float,c:Color,p:Pen,round:Float=0.025f) = inkedRound(Rect(x-w/2,y-h,x+w/2,y),round,c,p)
private fun DrawScope.line(x:Float,y:Float,x1:Float,y1:Float,c:Color,p:Pen) = drawLine(c,Offset(x,y),Offset(x1,y1),strokeWidth=p.lw,cap=StrokeCap.Round)
private fun DrawScope.star(x:Float,y:Float,s:Float,p:Pen,c:Color=XmasGold) = inked(xmasStar(x,y,s),c,p)
private fun DrawScope.ribbon(w:Float,y:Float,p:Pen) {
    line(-w/2,y,w/2,y,XmasGold,p)
    inkedOval(Rect(-0.07f,y-0.035f,0f,y+0.02f),XmasGold,p,shade=false)
    inkedOval(Rect(0f,y-0.035f,0.07f,y+0.02f),XmasGold,p,shade=false)
    inkedCircle(Offset(0f,y),0.015f,XmasCream,p,shade=false)
}

/** All ornaments are actual things; the tree's content list supplies their saved positions. */
internal fun DrawScope.drawChristmasFixture(f:Fixture,u:Float,pen:Pen,contents:List<Thing>):Boolean {
    if(f.type==FixtureType.DOOR && f.place.christmas) {
        villageDoor(House.passageAt(f)?.to, f.spec.w * u, f.spec.h * u, pen)
        return true
    }
    if(f.type !in Christmas.types) return false
    val w=f.spec.w;val h=f.spec.h
    val p=Pen(pen.lw/u,pen.t,pen.night,pen.weather,pen.rainbow,pen.season,pen.festival)
    val c=xmasColor(f.variant);val a=xmasAccent(f.variant)
    withTransform({ scale(u,u,Offset.Zero) }) {
        if(!f.spec.wall && f.type!=FixtureType.XM_RUG) drawOval(Ink.shadow,Offset(-w*0.52f,-0.018f),Size(w*1.1f,0.035f))
        when(f.type) {
            in Christmas.trees -> {
                val s=h/0.74f
                withTransform({ scale(s,s,Offset.Zero) }) {
                    r(0f,0f,0.16f,0.1f,c,p)
                    for(i in 0..2) inked(triangle(0f,-0.1f-i*0.16f,0.57f-i*0.12f,0.32f),if(i%2==0) XmasGreen else XmasGreen.lighten(0.1f),p)
                    if(f.on) for(i in 0..11) { val q=Christmas.ornamentPoint(i);drawCircle(if(f.mode==2) xmasLights[i%5] else XmasGold,0.009f,Offset(q.first,q.second)) }
                    for(t in contents.filter {it.type!=ThingType.XM_STAR || it.slot!=11}) { val q=Christmas.ornamentPoint(t.slot);translate(q.first,q.second) { drawChristmasThing(t.type,t.variant,t.used,0.047f,0.055f,p) } }
                    if(contents.any { it.type==ThingType.XM_STAR && it.slot==11 }) star(0f,-0.71f,0.045f,p)
                }
            }
            FixtureType.XM_CHAIR -> {
                box3d(Rect(-w*0.4f,-h,w*0.4f,-h*0.4f),0.035f,a,p,radius=0.02f)
                for(i in listOf(-1,1)) r(i*w*0.35f,0f,0.025f,h*0.6f,a,p)
                box3d(Rect(-w/2,-h*0.5f,w/2,-h*0.4f),0.11f,c,p,radius=0.015f)
                star(0f,-h*0.78f,0.018f,p)
            }
            FixtureType.XM_SOFA,FixtureType.XM_ARMCHAIR -> {
                box3d(Rect(-w*0.41f,-h,w*0.41f,-0.01f),0.065f,a,p,radius=0.025f);box3d(Rect(-w/2,-h*0.48f,w/2,-0.01f),0.13f,c,p,radius=0.025f)
                r(-w*0.42f,-0.04f,w*0.16f,h*0.6f,c,p);r(w*0.42f,-0.04f,w*0.16f,h*0.6f,c,p)
                val seats=if(f.type==FixtureType.XM_SOFA) 2 else 1
                for(i in 0 until seats) { val x=(i-(seats-1)/2f)*0.23f;r(x,-h*0.45f,0.13f,0.085f,XmasCream,p);star(x,-h*0.62f,0.021f,p,c) }
            }
            FixtureType.XM_TABLE,FixtureType.XM_BED -> {
                r(-w*0.39f,0f,0.04f,h,a,p);r(w*0.39f,0f,0.04f,h,a,p)
                box3d(Rect(-w/2,-h*0.98f,w/2,-h*0.5f),0.14f,if(f.type==FixtureType.XM_BED) a else Color(0xFFD9AB75),p,radius=0.018f)
                r(0f,-h*0.45f,w*0.86f,0.09f,c,p)
                if(f.type==FixtureType.XM_BED) {
                    box3d(Rect(-w*0.5f,-h*1.1f,-w*0.35f,-h*0.3f),0.14f,a,p,radius=0.025f)
                    r(-w*0.27f,-h*0.6f,0.12f,0.075f,XmasCream,p)
                    for(i in 0..2) star(w*(0.02f+i*0.13f),-h*0.72f,0.013f,p)
                }
                ribbon(w*0.8f,-h*0.72f,p)
            }
            FixtureType.XM_RUG -> {
                inkedOval(Rect(-w/2,-0.07f,w/2,0.005f),c,p)
                drawOval(XmasGold,Offset(-w*0.45f,-0.06f),Size(w*0.9f,0.05f),style=p.thin)
                star(0f,-0.03f,0.025f,p,XmasCream)
            }
            FixtureType.XM_CUPBOARD,FixtureType.XM_SHELF,FixtureType.XM_TOYBOX,FixtureType.XM_CHEST,FixtureType.XM_DISPLAY -> {
                box3d(Rect(-w/2,-h,w/2,0f),0.09f,a,p,radius=0.02f);r(0f,-0.025f,w*0.85f,h-0.05f,if(f.open) Color(0xFF684D46) else c,p)
                if(f.open || f.type==FixtureType.XM_SHELF || f.type==FixtureType.XM_DISPLAY) {
                    for(i in 1..2) line(-w*0.42f,-h*i/3,w*0.42f,-h*i/3,XmasGold,p)
                } else { line(0f,-0.03f,0f,-h+0.03f,XmasGold,p);inkedCircle(Offset(w*0.12f,-h/2),0.013f,XmasGold,p) }
                if(f.type==FixtureType.XM_TOYBOX) star(0f,-h*0.55f,0.04f,p)
                if(f.type==FixtureType.XM_CHEST) ribbon(w,-h*0.6f,p)
                if(f.type==FixtureType.XM_DISPLAY) { r(-0.1f,-h/3,0.09f,0.07f,XmasCream,p);r(0.09f,-h/3,0.09f,0.07f,XmasRed,p) }
            }
            FixtureType.XM_SACK -> { inkedOval(Rect(-w/2,-h*0.85f,w/2,0f),c,p);r(0f,-h*0.68f,w*0.65f,h*0.3f,a,p);ribbon(w*0.5f,-h*0.7f,p) }
            FixtureType.XM_WREATH -> {
                inkedCircle(Offset(0f,-h/2),w*0.45f,XmasGreen,p)
                inkedCircle(Offset(0f,-h/2),w*0.24f,XmasCream,p)
                for(i in 0..7) { val q=i*PI/4;inkedCircle(Offset(cos(q).toFloat()*w*0.34f,-h/2+sin(q).toFloat()*w*0.34f),0.012f,c,p,shade=false) };ribbon(w*0.5f,-h*0.15f,p)
            }
            FixtureType.XM_GARLAND,FixtureType.XM_LIGHTS -> {
                val path=Path().apply { moveTo(-w/2,-h);quadraticTo(0f,h*0.35f,w/2,-h) }
                drawPath(path,if(f.type==FixtureType.XM_GARLAND) a else Ink.line,style=Stroke(if(f.type==FixtureType.XM_GARLAND) 0.035f else p.lw))
                for(i in 0..9) { val t=i/9f;val x=(t-0.5f)*w;val y=-h+(1f-(t*2-1).pow(2))*h*0.65f;inkedCircle(Offset(x,y),0.015f,if(f.on) xmasLights[i%5] else c,p,shade=false) }
            }
            FixtureType.XM_STAR -> { line(0f,-h,0f,-h*0.85f,Ink.line,p);star(0f,-h*0.45f,w*0.46f,p,if(f.on) XmasGold else c) }
            FixtureType.XM_CANDLES -> {
                r(0f,0f,w,0.04f,a,p)
                for(i in 0..3) { val x=(i-1.5f)*0.042f;r(x,-0.035f,0.027f,0.08f+i*0.02f,c,p);if(f.on) inkedOval(Rect(x-0.012f,-0.14f-i*0.02f,x+0.012f,-0.11f-i*0.02f),XmasGold,p,shade=false) }
            }
            FixtureType.XM_LANTERN -> {
                r(0f,0f,w,0.025f,a,p);r(0f,-0.025f,w*0.7f,h*0.67f,if(f.on) XmasGold else XmasCream,p)
                inked(triangle(0f,-h*0.77f,w,h*0.23f),c,p);line(-w*0.29f,-0.02f,-w*0.29f,-h*0.75f,Ink.line,p);line(w*0.29f,-0.02f,w*0.29f,-h*0.75f,Ink.line,p)
            }
            FixtureType.XM_FIREPLACE -> {
                box3d(Rect(-w/2,-h,w/2,0f),0.09f,XmasCream,p,radius=0.02f);r(0f,0f,w*0.67f,h*0.74f,Color(0xFF493C4A),p)
                r(0f,-h*0.92f,w*1.08f,0.045f,a,p)
                if(f.on) { inked(triangle(0f,-0.025f,0.21f,0.23f),XmasRed,p);inked(triangle(0.025f,-0.025f,0.12f,0.18f),XmasGold,p) }
                for(i in -1..1) { r(i*0.12f,-h*0.74f,0.045f,0.075f,c,p);r(i*0.12f,-h*0.84f,0.05f,0.02f,XmasCream,p) }
            }
            FixtureType.XM_SNOWGLOBE -> {
                inkedCircle(Offset(0f,-h*0.55f),w*0.46f,Color(0xFFD5EAF1),p)
                inked(triangle(0f,-h*0.3f,0.1f,0.12f),XmasGreen,p)
                for(i in 0..7) { val x=sin(i*2.4f)*w*0.32f;val y=-h*0.85f+((i*0.027f+if(f.on) pen.t*0.06f else 0f).mod(h*0.55f));drawCircle(XmasSnow,0.006f,Offset(x,y)) }
                r(0f,0f,w,0.07f,c,p);star(0f,-0.035f,0.018f,p)
            }
            FixtureType.XM_MUSICBOX -> { r(0f,0f,w,h*0.6f,c,p);r(0f,-h*0.52f,w*1.03f,0.03f,a,p);star(0f,-h*0.79f,0.035f,p);ribbon(w,-0.04f,p) }
            FixtureType.XM_PICTURE -> { r(0f,0f,w,h,a,p);r(0f,-0.02f,w-0.04f,h-0.04f,Color(0xFF435877),p);inked(triangle(0f,-0.04f,w*0.65f,h*0.6f),XmasSnow,p);star(-0.06f,-0.18f,0.023f,p) }
            in Christmas.benches -> {
                r(-w*0.4f,0f,0.04f,h,c,p);r(w*0.4f,0f,0.04f,h,c,p);box3d(Rect(-w/2,-h,w/2,-h*0.7f),0.12f,Color(0xFFDDB480),p,radius=0.018f)
                r(0f,-0.06f,w*0.8f,0.08f,a,p);inkedCircle(Offset(0f,-0.1f),0.013f,XmasGold,p)
                val type=when(f.type) { FixtureType.XM_BAKE -> ThingType.XM_ROLLING_PIN;FixtureType.XM_HOUSE_BENCH -> ThingType.XM_GINGER_HOUSE;FixtureType.XM_CRAFT -> ThingType.XM_PAPER_HEART;FixtureType.XM_WRAP -> ThingType.XM_PARCEL;else -> ThingType.XM_ROBOT }
                translate(0f,-h) { drawChristmasThing(type,0,0,0.12f,0.11f,p) }
                for((i,t) in contents.withIndex()) translate(-0.18f+i*0.13f,-h-0.01f) { drawThing(t.type,t.variant,t.used,if(f.count==2) 0.17f else 0.085f,0.06f,p) }
                if(f.type==FixtureType.XM_BAKE && f.on) { r(0f,-0.05f,0.22f,0.12f,Color(0xFF4C3E47),p);star(0f,-0.11f,0.035f,p) }
            }
            FixtureType.XM_BELT -> {
                r(-0.24f,0f,0.035f,h,c,p);r(0.24f,0f,0.035f,h,c,p);r(0f,-0.03f,w,0.1f,a,p)
                for(i in 0..8) { val x=-w*0.43f+i*w*0.105f;inkedCircle(Offset(x,-0.075f),0.022f,Color(0xFFA8B2BC),p) }
                star(w*0.35f,-0.07f,0.02f,p)
                for((i,t) in contents.withIndex()) translate(-0.25f+i*0.09f,-0.13f) { drawChristmasThing(t.type,t.variant,0,0.075f,0.075f,p) }
            }
            FixtureType.XM_MARKET,FixtureType.XM_COCOA -> {
                r(-w*0.44f,0f,0.035f,h,a,p);r(w*0.44f,0f,0.035f,h,a,p);r(0f,0f,w,0.24f,c,p)
                r(0f,-h*0.7f,w*1.05f,h*0.28f,a,p)
                for(i in 0..5) r((i-2.5f)*w/6,-h*0.7f,w/7,h*0.28f,if(i%2==0) c else XmasCream,p,0.005f)
                r(0f,-0.22f,w*1.04f,0.04f,Color(0xFFE6BE8D),p)
                translate(0f,-0.26f) { drawChristmasThing(if(f.type==FixtureType.XM_COCOA) ThingType.XM_COCOA else ThingType.XM_COOKIE_TIN,0,0,0.1f,0.1f,p) }
                ribbon(w*0.75f,-0.12f,p)
            }
            FixtureType.XM_TROUGH -> { r(0f,0f,w,h,c,p);r(0f,-h*0.5f,w*0.83f,h*0.35f,Color(0xFFBD9859),p);for(i in -2..2) line(i*0.05f,-0.08f,i*0.05f+0.03f,-0.12f,XmasGold,p) }
            FixtureType.XM_SLEIGH -> {
                drawPath(Path().apply { moveTo(-w/2,0f);lineTo(w*0.35f,0f);quadraticTo(w*0.65f,-0.01f,w*0.5f,-0.12f) },XmasGold,style=Stroke(0.025f,cap=StrokeCap.Round))
                r(-w*0.25f,-0.04f,0.08f,h*0.85f,c,p);r(0f,-0.04f,w*0.85f,h*0.37f,c,p);star(w*0.3f,-0.1f,0.03f,p)
                for((i,t) in contents.withIndex()) translate(0.15f+(i%2)*0.07f,-0.16f-(i/2)*0.06f) { drawChristmasThing(t.type,t.variant,0,0.07f,0.07f,p) }
            }
            FixtureType.XM_CALENDAR -> {
                r(0f,0f,w,h,c,p);inked(triangle(0f,-h,w+0.04f,0.12f),a,p)
                for(i in 0..23) { val x=(i%6-2.5f)*w/6;val y=-h+(i/6+1)*h/4;r(x,y-0.012f,w/6-0.014f,h/4-0.014f,if(f.count and (1 shl i)!=0) a else XmasCream,p,0.008f);if(f.count and (1 shl i)==0) star(x,y-h/8,0.016f,p,c) }
            }
            FixtureType.XM_POST -> { r(0f,0f,0.045f,h*0.6f,a,p);r(0f,-h*0.35f,w,h*0.65f,c,p);r(0f,-h*0.65f,w*0.65f,0.02f,Ink.line,p);star(0f,-h*0.51f,0.022f,p) }
            else -> Unit
        }
    }
    return true
}

internal fun DrawScope.drawChristmasThing(type:ThingType,v:Int,used:Int,w:Float,h:Float,pen:Pen):Boolean {
    if(!type.name.startsWith("XM_")) return false
    val s=min(w,h);val p=Pen(pen.lw/s,pen.t,pen.night)
    withTransform({ scale(w,h,Offset.Zero) }) {
        val c=xmasColor(v);val a=xmasAccent(v)
        fun rr(x:Float,y:Float,ww:Float,hh:Float,col:Color)=r(x,y,ww,hh,col,p,0.07f)
        when(type) {
            ThingType.XM_BAUBLE -> { inkedCircle(Offset(0f,-0.43f),0.42f,c,p);rr(0f,-0.83f,0.2f,0.12f,XmasGold);drawCircle(XmasCream,0.08f,Offset(-0.15f,-0.6f)) }
            ThingType.XM_HEART,ThingType.XM_PAPER_HEART -> { inked(heart(0f,-0.5f,0.32f),if(type==ThingType.XM_HEART) c else Color(0xFFEAB1B1),p);line(-0.15f,-0.6f,0.1f,-0.33f,XmasCream,p) }
            ThingType.XM_STAR -> star(0f,-0.48f,0.47f,p)
            ThingType.XM_BELL -> { inked(triangle(0f,-0.15f,0.85f,0.7f),XmasGold,p);inkedCircle(Offset(0f,-0.12f),0.12f,c,p);rr(0f,-0.12f,0.94f,0.12f,XmasGold) }
            ThingType.XM_MUSHROOM -> { rr(0f,0f,0.28f,0.65f,XmasCream);inkedOval(Rect(-0.48f,-0.95f,0.48f,-0.35f),c,p);drawCircle(XmasCream,0.07f,Offset(-0.19f,-0.7f));drawCircle(XmasCream,0.07f,Offset(0.16f,-0.62f)) }
            ThingType.XM_CANDYCANE -> { drawPath(Path().apply { moveTo(-0.13f,0f);lineTo(-0.13f,-0.7f);cubicTo(-0.13f,-1.04f,0.38f,-1.04f,0.38f,-0.68f) },Ink.line,style=Stroke(0.28f,cap=StrokeCap.Round));drawPath(Path().apply { moveTo(-0.13f,0f);lineTo(-0.13f,-0.7f);cubicTo(-0.13f,-1.04f,0.38f,-1.04f,0.38f,-0.68f) },XmasCream,style=Stroke(0.25f,cap=StrokeCap.Round));for(i in 0..3) line(-0.24f,-0.12f-i*0.19f,-0.02f,-0.2f-i*0.19f,c,Pen(0.09f)) }
            ThingType.XM_CHAIN -> for(i in 0..3) drawOval(if(i%2==0)c else a,Offset(-0.42f+i*0.19f,-0.85f+i*0.18f),Size(0.31f,0.4f),style=Stroke(0.09f))
            ThingType.XM_SNOWFLAKE -> for(i in 0..5) { val q=i*PI/3;line(0f,-0.5f,cos(q).toFloat()*0.45f,-0.5f+sin(q).toFloat()*0.45f,Color(0xFF91C9E0),Pen(0.07f)) }
            ThingType.XM_CONE -> { inkedOval(Rect(-0.32f,-0.9f,0.32f,0f),Color(0xFFA7754D),p);for(i in 0..3) line(-0.2f,-0.18f-i*0.18f,0.2f,-0.28f-i*0.18f,XmasGold,p) }
            ThingType.XM_BOW,ThingType.XM_RIBBON -> { inkedOval(Rect(-0.48f,-0.8f,0f,-0.2f),c,p);inkedOval(Rect(0f,-0.8f,0.48f,-0.2f),c,p);rr(0f,-0.25f,0.2f,0.55f,XmasGold);if(type==ThingType.XM_RIBBON) {line(-0.1f,-0.25f,-0.3f,0f,c,Pen(0.13f));line(0.1f,-0.25f,0.3f,0f,c,Pen(0.13f))} }
            ThingType.XM_GINGER_MAN -> { val col=Color(0xFFBC8550);inkedCircle(Offset(0f,-0.78f),0.2f,col,p);rr(0f,-0.1f,0.35f,0.5f,col);line(-0.35f,-0.55f,0.35f,-0.55f,col,Pen(0.2f));line(-0.1f,-0.15f,-0.2f,0f,col,Pen(0.18f));line(0.1f,-0.15f,0.2f,0f,col,Pen(0.18f));drawCircle(XmasRed,0.045f,Offset(0f,-0.4f));drawCircle(Ink.line,0.025f,Offset(-0.07f,-0.8f));drawCircle(Ink.line,0.025f,Offset(0.07f,-0.8f)) }
            ThingType.XM_GINGER_HEART -> inked(heart(0f,-0.5f,0.32f),Color(0xFFBC8550),p)
            ThingType.XM_GINGER_STAR -> { star(0f,-0.5f,0.47f,p,Color(0xFFBC8550));star(0f,-0.5f,0.3f,p,XmasCream) }
            ThingType.XM_LUSSEKATT -> { inkedOval(Rect(-0.4f,-0.9f,0.15f,-0.3f),XmasGold,p);inkedOval(Rect(-0.15f,-0.6f,0.4f,0f),XmasGold,p);drawCircle(Color(0xFF68453B),0.07f,Offset(-0.14f,-0.63f));drawCircle(Color(0xFF68453B),0.07f,Offset(0.14f,-0.27f)) }
            ThingType.XM_PORRIDGE,ThingType.XM_BOWL -> { inkedOval(Rect(-0.48f,-0.7f,0.48f,0f),c,p);inkedOval(Rect(-0.48f,-0.8f,0.48f,-0.35f),if(type==ThingType.XM_BOWL) XmasCream.darken(0.2f) else XmasCream,p);if(type==ThingType.XM_PORRIDGE) {drawCircle(Color(0xFFA56D4C),0.15f,Offset(0f,-0.58f));rr(0f,-0.58f,0.15f,0.12f,XmasGold)} }
            ThingType.XM_CLEMENTINE -> { inkedCircle(Offset(0f,-0.45f),0.44f,Color(0xFFFFAC4A),p);inkedOval(Rect(-0.03f,-0.98f,0.28f,-0.78f),XmasGreen,p) }
            ThingType.XM_COCOA,ThingType.XM_SPICED_DRINK -> { drawCircle(c,0.22f,Offset(0.3f,-0.5f),style=Stroke(0.12f));rr(0f,0f,0.7f,0.8f,if(type==ThingType.XM_COCOA)c else a);inkedOval(Rect(-0.35f,-0.9f,0.35f,-0.65f),if(type==ThingType.XM_COCOA) Color(0xFF71504B) else Color(0xFF933953),p);star(0f,-0.4f,0.16f,p);if(used==0 && type==ThingType.XM_COCOA) for(i in -1..1) rr(i*0.18f,-0.73f,0.16f,0.13f,XmasCream) }
            ThingType.XM_SODA -> { rr(0f,0f,0.55f,0.72f,c);rr(0f,-0.65f,0.25f,0.3f,a);rr(0f,-0.2f,0.6f,0.3f,XmasCream);star(0f,-0.35f,0.12f,p,c) }
            ThingType.XM_COOKIE_TIN -> { rr(0f,0f,0.9f,0.72f,c);rr(0f,-0.62f,0.98f,0.16f,a);star(0f,-0.34f,0.22f,p) }
            ThingType.XM_PLATTER -> { inkedOval(Rect(-0.5f,-0.55f,0.5f,-0.05f),XmasCream,p);drawOval(c,Offset(-0.4f,-0.48f),Size(0.8f,0.35f),style=p.stroke) }
            ThingType.XM_DOUGH -> inkedOval(Rect(-0.5f,-0.65f,0.5f,0f),XmasGold,p)
            ThingType.XM_ROLLING_PIN -> { rr(0f,-0.18f,0.95f,0.18f,Color(0xFFAB794F));rr(0f,-0.02f,0.62f,0.5f,Color(0xFFDCB17A)) }
            ThingType.XM_CUTTER -> drawPath(xmasStar(0f,-0.5f,0.45f),Color(0xFFADB6C4),style=Stroke(0.1f))
            ThingType.XM_ICING -> { inked(triangle(0f,0f,0.8f,0.9f),XmasCream,p);rr(0f,-0.8f,0.3f,0.1f,c) }
            ThingType.XM_BRUSH -> { rr(0f,0f,0.2f,0.7f,Color(0xFFCF9D6A));rr(0f,-0.55f,0.58f,0.36f,a);for(i in -2..2) line(i*0.09f,-0.62f,i*0.09f,-0.86f,XmasGold,p) }
            ThingType.XM_PAPER,ThingType.XM_WISH -> { rr(0f,0f,0.9f,0.95f,XmasCream);if(type==ThingType.XM_WISH) translate(0f,-0.18f) {drawChristmasThing(listOf(ThingType.XM_BAUBLE,ThingType.XM_GINGER_MAN,ThingType.XM_PARCEL,ThingType.XM_HAT,ThingType.XM_BELL,ThingType.XM_STAR)[v.mod(6)],0,0,0.6f,0.6f,p)} else for(i in -1..1) line(-0.35f,-0.2f+i*0.2f,0.35f,-0.6f+i*0.2f,c,Pen(0.07f)) }
            ThingType.XM_TRAIN -> { rr(-0.08f,-0.13f,0.8f,0.36f,c);rr(-0.24f,-0.4f,0.37f,0.45f,a);rr(0.25f,-0.45f,0.18f,0.45f,c);for(i in -1..1) inkedCircle(Offset(i*0.28f,-0.12f),0.12f,XmasGold,p) }
            ThingType.XM_HORSE -> { rr(0f,-0.3f,0.7f,0.3f,Color(0xFFC59B76));rr(-0.22f,-0.45f,0.2f,0.4f,Color(0xFFC59B76));drawArc(a,0f,180f,false,Offset(-0.45f,-0.4f),Size(0.9f,0.4f),style=Stroke(0.12f)) }
            ThingType.XM_TOP -> { inked(triangle(0f,-0.05f,0.95f,0.7f),a,p);rr(0f,-0.64f,0.12f,0.34f,XmasGold);line(-0.33f,-0.3f,0.33f,-0.3f,c,Pen(0.12f)) }
            ThingType.XM_BEAR -> { inkedCircle(Offset(-0.25f,-0.82f),0.17f,Color(0xFFBD9876),p);inkedCircle(Offset(0.25f,-0.82f),0.17f,Color(0xFFBD9876),p);inkedOval(Rect(-0.34f,-0.55f,0.34f,0f),Color(0xFFBD9876),p);inkedCircle(Offset(0f,-0.66f),0.31f,Color(0xFFD6B28B),p);drawCircle(Ink.line,0.035f,Offset(-0.1f,-0.7f));drawCircle(Ink.line,0.035f,Offset(0.1f,-0.7f));rr(0f,-0.26f,0.58f,0.12f,c) }
            ThingType.XM_ROBOT -> { rr(0f,-0.04f,0.68f,0.46f,a);rr(0f,-0.5f,0.8f,0.4f,c);for(i in listOf(-1,1)) {drawCircle(XmasCream,0.08f,Offset(i*0.2f,-0.72f));rr(i*0.24f,0f,0.22f,0.12f,XmasGold)};star(0f,-0.27f,0.13f,p) }
            ThingType.XM_PARCEL -> { box3d(Rect(-0.47f,-0.8f,0.47f,0f),0.18f,c,p,radius=0.04f);rr(0f,0f,0.14f,0.8f,XmasGold);line(-0.47f,-0.5f,0.47f,-0.5f,XmasGold,Pen(0.12f));inkedOval(Rect(-0.35f,-1f,0f,-0.8f),a,p);inkedOval(Rect(0f,-1f,0.35f,-0.8f),a,p) }
            ThingType.XM_GINGER_HOUSE -> { rr(0f,0f,0.82f,0.62f,Color(0xFFC18F60));inked(triangle(0f,-0.6f,1f,0.4f),c,p);rr(0f,0f,0.23f,0.4f,XmasCream);for(i in listOf(-1,1)) rr(i*0.27f,-0.3f,0.15f,0.15f,XmasGold) }
            ThingType.XM_STOCKING -> { rr(-0.1f,-0.2f,0.48f,0.75f,c);rr(0.05f,0f,0.8f,0.32f,c);rr(-0.1f,-0.78f,0.56f,0.18f,XmasCream) }
            ThingType.XM_HAT -> when(v.mod(4)) {
                0,1 -> { inked(Path().apply {moveTo(-0.48f,-0.15f);quadraticTo(-0.15f,-1.3f,0.4f,-0.65f);lineTo(0.48f,-0.15f);close()},if(v==0)c else XmasGreen,p);rr(0f,-0.05f,0.98f,0.17f,XmasCream);inkedCircle(Offset(0.4f,-0.65f),0.12f,XmasCream,p) }
                2 -> { for(i in listOf(-1,1)) {line(i*0.2f,-0.1f,i*0.28f,-0.9f,Color(0xFFA77D58),Pen(0.1f));line(i*0.28f,-0.55f,i*0.47f,-0.72f,Color(0xFFA77D58),Pen(0.08f))};rr(0f,-0.05f,0.9f,0.14f,c) }
                else -> {rr(0f,0f,0.95f,0.3f,c);star(0f,-0.58f,0.4f,p)}
            }
            ThingType.XM_FACE -> when(v.mod(4)) {
                0 -> { inked(Path().apply {moveTo(-0.45f,-0.8f);quadraticTo(-0.5f,-0.05f,0f,0f);quadraticTo(0.5f,-0.05f,0.45f,-0.8f);quadraticTo(0f,-0.5f,-0.45f,-0.8f)},XmasCream,p) }
                2 -> {inkedCircle(Offset(0f,-0.45f),0.23f,XmasRed,p);line(-0.5f,-0.6f,0.5f,-0.6f,Ink.line,p)}
                else -> {for(i in listOf(-1,1)) drawCircle(if(v==1) XmasGold else Color(0xFF83C8E3),0.23f,Offset(i*0.25f,-0.5f),style=Stroke(0.07f));line(-0.03f,-0.5f,0.03f,-0.5f,Ink.line,p)}
            }
            else -> Unit
        }
        if(v>0 && type in setOf(ThingType.XM_GINGER_MAN,ThingType.XM_GINGER_HEART,ThingType.XM_GINGER_STAR)) for(i in -1..1) drawCircle(XmasCream,0.045f,Offset(i*0.17f,-0.46f))
    }
    return true
}
