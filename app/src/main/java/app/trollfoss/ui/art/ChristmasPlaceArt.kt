package app.trollfoss.ui.art

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import app.trollfoss.domain.*
import kotlin.math.*

/** Warm cutaway rooms and a snowy village, culled to the visible part of the scene. */
internal fun DrawScope.christmasBack(place:PlaceId,st:Stage,pen:Pen,styles:List<RoomStyle>) {
    val u=st.u
    if(place.outdoor) {
        drawRect(if(pen.night>0.5f) Color(0xFF263753) else Color(0xFFC6E1ED),Offset(0f,st.backgroundTop),Size(st.w,st.h-st.backgroundTop))
        for(i in 0..7) {
            val x=i*1.2f-0.4f
            if(!st.sees(x-0.6f,x+0.8f)) continue
            val back=Path().apply { moveTo(st.x(x-0.6f),u*0.8f);lineTo(st.x(x+0.1f),u*0.12f);lineTo(st.x(x+0.8f),u*0.8f);close() }
            drawPath(back,Color(0xFFA9C4D4))
            val pine=Path().apply { moveTo(st.x(x+0.2f),u*0.45f);lineTo(st.x(x+0.52f),u*0.79f);lineTo(st.x(x-0.12f),u*0.79f);close() }
            inked(pine,XmasGreen,pen)
            if(i%2==0) {
                val wall=Rect(st.x(x-0.22f),u*0.5f,st.x(x+0.44f),u*0.79f)
                inkedRound(wall,u*0.025f,if(place==PlaceId.CHRISTMAS_STABLE) Color(0xFFBC9876) else XmasRed,pen)
                val roof=Path().apply {moveTo(wall.left-u*0.06f,wall.top);lineTo(wall.center.x,wall.top-u*0.2f);lineTo(wall.right+u*0.06f,wall.top);close()}
                inked(roof,XmasSnow,pen)
                inkedRound(Rect(wall.center.x-u*0.07f,u*0.57f,wall.center.x+u*0.07f,u*0.7f),u*0.01f,XmasGold,pen)
            }
        }
        drawRect(XmasSnow,Offset(0f,u*0.78f),Size(st.w,max(st.h-u*0.78f,0f)))
        drawLine(Color(0xFFD3E1ED),Offset(0f,u*0.94f),Offset(st.w,u*0.94f),u*0.012f)
    } else {
        drawRect(XmasCream,Offset(0f,st.backgroundTop),Size(st.w,st.h-st.backgroundTop))
        drawRect(Color(0xFFD4AD83),Offset(0f,u*place.back),Size(st.w,max(st.h-u*place.back,0f)))
        val rooms=Decor.rooms(place)
        for((i,room) in rooms.withIndex()) {
            if(!st.sees(room.start,room.endInclusive)) continue
            val style=styles.getOrNull(i) ?: RoomStyle()
            val left=st.x(room.start);val right=st.x(room.endInclusive)
            if(style.wall>0) paperWall(st,style.wall,place.back,room.start,room.endInclusive)
            else {
                drawRect(listOf(XmasCream,Color(0xFFE4EBD9),Color(0xFFF1D9D1),Color(0xFFD5E4E8),Color(0xFFE5D7E8))[i%5],Offset(left,st.backgroundTop),Size(right-left,place.back*u-st.backgroundTop))
                drawRect(XmasGreen,Offset(left,place.back*u-0.035f*u),Size(right-left,0.035f*u))
                val x=room.start+1f
                inkedRound(st.rect(x-0.26f,0.22f,x+0.26f,0.51f),u*0.025f,Color(0xFFC0DFEA),pen)
                drawLine(XmasCream,st.o(x,0.22f),st.o(x,0.51f),u*0.018f)
                drawLine(XmasCream,st.o(x-0.26f,0.36f),st.o(x+0.26f,0.36f),u*0.018f)
                adventStar(st.o(x,0.36f),u*0.065f,pen)
            }
            if(style.floor>0) layFloor(st,style.floor,place.back,room.start,room.endInclusive)
            else for(k in 0..9) { val x=room.start+k*0.2f;drawLine(Color(0xFFB98F69),st.o(x+0.1f,place.back),st.o(x,1f),u*0.002f) }
            if(i>0) {
                drawRect(Color(0xFFBA8D6C),Offset(left,st.backgroundTop),Size(u*0.025f,0.61f*u-st.backgroundTop))
                drawRect(Color(0xFFBA8D6C),Offset(left,0.72f*u),Size(u*0.025f,0.08f*u))
            }
        }
    }
    for(i in 0 until ceil(place.width).toInt()) {
        val x=i.toFloat()
        if(st.sees(x,x+1)) lightStrand(st.o(x,0.16f),st.o(x+1,0.16f),u*0.07f,10,u*0.006f,pen)
    }
}

internal fun DrawScope.christmasVillage(place:PlaceId,st:Stage,pen:Pen) {
    if(pen.festival!=Festival.CHRISTMAS || place.christmas) return
    // Roof-height dressing avoids the free floor and all saved room styles.
    if(place.mine || place.manor) for(i in 0 until ceil(place.width/2).toInt()) {
        val x=i*2f
        if(!st.sees(x,x+2)) continue
        lightStrand(st.o(x,0.13f),st.o(x+2,0.13f),0.045f*st.u,16,0.006f*st.u,pen)
        adventStar(st.o(x+1,0.28f),0.045f*st.u,pen)
    }
}

internal fun DrawScope.christmasClothes(style:Int,h:Float,p:Pen) {
    val v=style-Christmas.FIRST_TOP
    val white=XmasCream
    if(v in 0..3) {
        drawLine(white,Offset(0f,-0.49f*h),Offset(0f,-0.22f*h),0.025f*h)
        drawLine(Ink.line,Offset(-0.18f*h,-0.28f*h),Offset(0.18f*h,-0.28f*h),0.035f*h)
        inkedRound(Rect(-0.025f*h,-0.305f*h,0.025f*h,-0.255f*h),0.006f*h,XmasGold,p)
    } else if(v==7) {
        for(i in -1..1) for(j in 0..2) drawCircle(white,0.008f*h,Offset(i*0.07f*h,(-0.43f+j*0.07f)*h))
    } else {
        val motif=if(v==5) xmasStar(0f,-0.36f*h,0.045f*h) else xmasStar(0f,-0.36f*h,0.055f*h)
        inked(motif,XmasGold,p)
        for(i in -2..2) drawCircle(white,0.007f*h,Offset(i*0.05f*h,-0.26f*h))
    }
}

internal fun DrawScope.christmasGarment(variant:Int,w:Float,h:Float,pen:Pen) {
    val style=Garment.style(variant)
    val c=argb(Palette.cloth[Garment.color(variant)])
    val p=Pen(pen.lw/minOf(w,h))
    val dress=style in setOf(20,22,25)
    withTransform({scale(w,h,Offset.Zero)}) {
        val shape=Path().apply {moveTo(-0.18f,-0.85f);lineTo(-0.45f,-0.65f);lineTo(-0.32f,-0.4f);lineTo(-0.22f,-0.5f);lineTo(if(dress) -0.42f else -0.23f,0f);lineTo(if(dress) 0.42f else 0.23f,0f);lineTo(0.22f,-0.5f);lineTo(0.32f,-0.4f);lineTo(0.45f,-0.65f);lineTo(0.18f,-0.85f);quadraticTo(0f,-0.65f,-0.18f,-0.85f);close()}
        inked(shape,c,p)
        if(style in 19..22) {
            drawLine(XmasCream,Offset(0f,-0.75f),Offset(0f,-0.1f),0.07f)
            drawLine(Ink.line,Offset(-0.27f,-0.21f),Offset(0.27f,-0.21f),0.08f)
            inkedRound(Rect(-0.07f,-0.28f,0.07f,-0.14f),0.02f,XmasGold,p)
            if(style<21) drawLine(XmasCream,Offset(-0.23f,-0.04f),Offset(0.23f,-0.04f),0.07f)
        } else if(style==26) {
            for(i in -1..1) for(j in 0..2) drawCircle(XmasCream,0.025f,Offset(i*0.13f,-0.6f+j*0.2f))
        } else {
            inked(xmasStar(0f,-0.43f,if(style==24) 0.12f else 0.18f),XmasGold,p)
            if(style==24) {drawLine(XmasCream,Offset(-0.16f,-0.44f),Offset(-0.22f,-0.7f),0.035f);drawLine(XmasCream,Offset(0.16f,-0.44f),Offset(0.22f,-0.7f),0.035f)}
        }
    }
}
