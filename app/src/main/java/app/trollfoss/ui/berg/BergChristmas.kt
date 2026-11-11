package app.trollfoss.ui.berg

import app.trollfoss.domain.Highlands
import kotlin.math.*

/** Small, cached dressing for the authored Blender world; no collision or movement changes. */
internal fun bergChristmas(cave:Boolean):BergMesh=BergMeshBuilder().apply {
    val x=if(cave) -7f else 3.5f
    val z=if(cave) 1f else 11f
    val y=Highlands.floor(x,z,cave)
    box(x,y,z,.65f,.12f,.65f,0x986D4E)
    for(i in 0..2) {
        val r=1f-i*.22f;val base=y+.1f+i*.68f
        val tip=V3(x,base+1.45f,z)
        for(k in 0..11) {
            val a=k*PI.toFloat()/6;val b=(k+1)*PI.toFloat()/6
            triangle(tip,V3(x+cos(a)*r,base,z+sin(a)*r),V3(x+cos(b)*r,base,z+sin(b)*r),if(k%2==0) 0x316B55 else 0x3E8060)
        }
        for(k in 0..9) { val a=k*PI.toFloat()/5; sphere(x+cos(a)*r*.8f,base+.25f,z+sin(a)*r*.8f,.055f,.055f,.055f,listOf(0xE6BB67,0xB94D56,0x92BFCB)[k%3],6,4) }
    }
    sphere(x,y+3f,z,.15f,.22f,.15f,0xF2CE7E,7,5)
    for(i in 0..3) {
        val px=x-1.1f+i*.53f;val pz=z+.8f+(i%2)*.2f;val py=Highlands.floor(px,pz,cave)
        val c=if(i%2==0) 0xA64B54 else 0x7CA4B0
        box(px,py,pz,.42f,.32f,.4f,c);box(px,py+.32f,pz,.45f,.04f,.43f,c)
        box(px,py,pz,.07f,.37f,.44f,0xE4C17D)
    }
    for(i in 0..15) {
        val px=x-2f+i*.26f;val py=y+2.7f-sin(i/15f*PI.toFloat())*.4f
        sphere(px,py,z-1.2f,.045f,.045f,.045f,if(i%2==0) 0xE9BB6E else 0xC56D6F,6,4)
        if(i>0) beam(V3(px-.26f,y+2.7f-sin((i-1)/15f*PI.toFloat())*.4f,z-1.2f),V3(px,py,z-1.2f),.012f,0x635B55,4)
    }
}.build()
