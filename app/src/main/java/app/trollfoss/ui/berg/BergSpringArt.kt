package app.trollfoss.ui.berg

import android.opengl.GLES20.*
import android.opengl.Matrix
import app.trollfoss.domain.*
import kotlin.math.*

/** A seep in the slate feeds a shallow, moss-lined pool. The three caps lean toward a ripple. */
internal class BergSpringArt {
    private val shades=intArrayOf(0x639E90,0x9481AE,0xC08A5E)
    private val glowShades=intArrayOf(0xA3F3D8,0xE3BCFC,0xFFDB99)
    private fun mesh(light:Float=0f,build:BergMeshBuilder.()->Unit)=
        BergMeshBuilder().apply(build).build(lighting=light).also {it.upload()}
    private val basin=mesh {
        val rock=intArrayOf(0x777F83,0x89938F,0x677271,0x95948D)
        fun edge(i:Int,s:Float,h:Float):V3 {
            val a=i*PI.toFloat()/28f
            val r=s*(1f+.052f*sin(5f*a)+.033f*cos(7f*a))
            return V3(cos(a)*r*1.63f,h,sin(a)*r*.99f)
        }
        for(i in 0 until 56) {
            val a=i*PI.toFloat()/28f;val b=(i+1)*PI.toFloat()/28f
            val ha=.39f+.078f*sin(3f*a)+.035f*cos(8f*a)
            val hb=.39f+.078f*sin(3f*b)+.035f*cos(8f*b)
            quad(edge(i,1f,.01f),edge(i+1,1f,.01f),edge(i+1,.91f,hb),edge(i,.91f,ha),rock[(i/4)%4])
            quad(edge(i,.91f,ha),edge(i+1,.91f,hb),edge(i+1,.75f,.27f),edge(i,.75f,.27f),rock[(i/6+1)%4])
            triangle(V3(0f,BergSpring.WATER,0f),edge(i+1,.81f,BergSpring.WATER),edge(i,.81f,BergSpring.WATER),if(i%11==0) 0x629D9B else 0x538F93)
        }
        // A small cantilever of slate touches the rock face below; the water beads on its lip.
        sphere(-.49f,.54f,-.64f,.73f,.77f,.38f,0x667775,13,8)
        sphere(-.53f,1.13f,-.66f,.56f,.75f,.38f,0x788A86,14,8)
        sphere(-.44f,1.78f,-.58f,.56f,.25f,.42f,0x7E8B88,14,8)
        sphere(-.35f,1.69f,-.34f,.28f,.078f,.23f,0x85AAA2,12,6)
        for(i in 0..8) {
            val a=i*2.37f
            val x=cos(a)*1.42f;val z=sin(a)*.88f
            sphere(x,.31f,z,.22f,.094f,.14f,if(i%2==0) 0x63866B else 0x82956B,9,5)
            for(j in -1..1) {
                val root=V3(x,.34f,z)
                val tip=root+V3(.088f*j,.19f+.023f*(j+1),.075f)
                triangle(root+V3(-.034f,0f,0f),root+V3(.032f,.007f,0f),tip,0x839E76)
            }
        }
    }
    private val mushrooms=shades.mapIndexed {kind,colour->mesh {
        val cream=0xDADEC3
        beam(V3(0f,0f,0f),V3(-.035f,.42f,0f),.075f,cream,12)
        beam(V3(-.035f,.42f,0f),V3(.006f,.66f,0f),.087f,cream,12)
        sphere(0f,.64f,0f,.395f,.096f,.37f,0xC5C6AD,20,7)
        fun cap(i:Int,row:Int):V3 {
            val a=i*PI.toFloat()/14f
            val r=when(row){0->.40f;1->.32f;2->.18f;else->.01f}
            val y=when(row){0->.655f;1->.80f;2->.91f;else->.95f}
            return V3(cos(a)*r,y+.016f*cos(5f*a)*(1f-row/3f),sin(a)*r*.94f)
        }
        for(j in 0..2) for(i in 0 until 28)
            quad(cap(i,j),cap(i,j+1),cap(i+1,j+1),cap(i+1,j),colour)
        for(i in 0..13) {
            val a=i*PI.toFloat()/7
            beam(V3(cos(a)*.09f,.62f,sin(a)*.09f),V3(cos(a)*.366f,.645f,sin(a)*.343f),.009f,cream,5)
        }
        for(i in 0..kind) {
            val a=(i-1)*.64f+.36f
            sphere(sin(a)*.16f,.842f+i*.010f,cos(a)*.235f,.043f,.018f,.047f,0xEEDBB8,10,5)
        }
    } }
    private val gills=glowShades.map {col->mesh(2f){
        for(i in 0 until 18) {
            val a=i*PI.toFloat()/9f
            beam(V3(cos(a)*.12f,.599f,sin(a)*.12f),V3(cos(a)*.376f,.644f,sin(a)*.351f),.011f,col,5)
            sphere(cos(a)*.388f,.653f,sin(a)*.361f,.019f,.012f,.018f,col,6,4)
        }
    }}
    private val ring=mesh(2f) {
        for(i in 0 until 40) {
            fun p(k:Int,r:Float)=V3(cos(k*PI.toFloat()/20)*r,0f,sin(k*PI.toFloat()/20)*r)
            quad(p(i,.96f),p(i+1,.96f),p(i+1,1.025f),p(i,1.025f),0xBCF0DB)
        }
    }
    private val drop=mesh(2f){sphere(0f,0f,0f,.044f,.070f,.044f,0xBDE9DF,9,7)}
    private val boards=mesh {
        val left=BergCaveAudio.BOARD_LEFT;val right=BergCaveAudio.BOARD_RIGHT
        val back=BergCaveAudio.BOARD_BACK;val front=BergCaveAudio.BOARD_FRONT
        val step=(right-left)/14f
        for(i in 0 until 14) {
            val x=left+(i+.5f)*step
            box(x,4.402f,(back+front)*.5f,step-.017f,.042f,front-back,if(i%3==0) 0x9D785A else 0xAE8866)
            for(z in listOf(back+.17f,front-.17f)) sphere(x,4.447f,z,.024f,.006f,.024f,0x564F48,6,3)
        }
    }
    private val matrix=FloatArray(16)
    private fun at(x:Float,y:Float,z:Float) {
        Matrix.setIdentityM(matrix,0);Matrix.translateM(matrix,0,x,y,z)
    }
    fun draw(f:BergFrame,opacity:Int,submit:(BergMesh,FloatArray)->Unit) {
        if(!f.cave) return
        at(0f,0f,0f);submit(boards,matrix)
        val x=BergSpring.X;val z=BergSpring.Z
        if(hypot(f.x-x,f.z-z)>28f) return
        at(x,0f,z);submit(basin,matrix)
        val since=BergSpring.sinceDrop(f.time)
        val until=BergSpring.untilDrop(f.time)
        val waterY=BergSpring.WATER
        val drip=BergSpring.drip
        val fall=if(f.motion) (1f-until/.52f).coerceIn(0f,1f) else 0f
        val size=if(f.motion) (.30f+since*.26f).coerceAtMost(1f) else 1f
        at(drip.x,1.64f-(1.64f-waterY)*fall*fall,drip.z)
        Matrix.scaleM(matrix,0,size,size*(1f+fall*.48f),size);submit(drop,matrix)
        BergSpring.caps.forEachIndexed {i,cap->
            val order=(i-f.springKey+3)%3
            val wave=if(f.springAge>=0f) (f.springAge-.29f*order) else -1f
            val nudge=if(f.motion && wave in 0f..2.5f) sin(wave*6.4f)*exp(-wave*1.9f) else 0f
            val idle=if(f.motion && f.springAwake) sin(f.time*1.35f+i)*2.5f else 0f
            val response=if(f.motion && since in .20f..1.5f) sin((since-.20f)*3.2f)*1.6f else 0f
            at(cap.x,.27f,cap.z);Matrix.rotateM(matrix,0,idle+response-nudge*19f,0f,0f,1f)
            val s=BergSpring.sizes[i];Matrix.scaleM(matrix,0,s,s,s)
            submit(mushrooms[i],matrix)
            if(f.springAwake) submit(gills[i],matrix)
        }
        val ripple=if(!f.motion) .48f else if(f.springAge in 0f..1.3f) f.springAge/1.3f else since/1.5f
        if(ripple in .01f..1f) {
            glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false)
            at(drip.x,waterY+.011f,drip.z)
            val radius=.09f+.75f*ripple
            Matrix.scaleM(matrix,0,radius,1f,radius*.74f)
            glUniform1f(opacity,.7f*(1f-ripple));submit(ring,matrix)
            glUniform1f(opacity,1f);glDepthMask(true);glDisable(GL_BLEND)
        }
    }
}
