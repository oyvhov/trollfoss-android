package app.trollfoss.ui.berg

import android.opengl.GLES20.*
import android.opengl.Matrix
import app.trollfoss.domain.*
import kotlin.math.*

internal data class EchoFrame(
    val active:Boolean=false,val tone:Int=-1,val glow:Float=0f,
    val replyAge:Float=0f,val replyLength:Float=0f,val guide:EchoLight?=null,
) {
    val armAngle get()=if(guide!=null) -70f else -32f-glow*37f
}

/** Lantern moths perch at rest. During a reply each note calls them to a different stone. */
internal class BergEchoArt(art:BergAssets) {
    private fun load(art:BergAssets,name:String)=art.mesh(name).also {it.upload()}
    private val stones=listOf("sun","leaf","moon").map {load(art,"echo-"+it)}
    private val moth=load(art,"echo-moth")
    private val wing=load(art,"echo-wing")
    val kit=load(art,"traveller-kit")
    private val glowColours=intArrayOf(0xF1CB82,0x8EDBB4,0xCAAEED)
    private val lights=glowColours.map {c->BergMeshBuilder().apply {sphere(0f,0f,0f,.062f,.062f,.062f,c,8,5)}.build(lighting=2f).also {it.upload()}}
    private fun curtain(strand:Int,t:Float,lift:Float):V3 {
        val arch=sin(t*PI.toFloat())
        val fold=sin(t*12f+strand*.56f)
        return V3((t-.5f)*7.2f,1.18f+arch*(2.78f+strand*.13f+fold*.18f+lift),
            -1.16f-arch*.62f+sin(t*9f+strand*.7f)*.32f-lift*.14f)
    }
    private fun aurora(veil:Boolean)=BergMeshBuilder().apply {
        for(k in 0..2) for(i in 0 until 42) {
            val a=i/42f;val b=(i+1)/42f
            fun top(t:Float)=if(veil) .55f+.32f*sin(t*34f+k)*sin(t*8f+k) else .044f
            quad(curtain(k,a,0f),curtain(k,a,top(a)),curtain(k,b,top(b)),curtain(k,b,0f),glowColours[k])
        }
    }.build(lighting=2f).also {it.upload()}
    private val ribbon=aurora(false)
    private val veil=aurora(true)
    private val waterRings=glowColours.map {colour->BergMeshBuilder().apply {
        for(i in 0 until 64) {
            fun p(j:Int,r:Float)=V3(cos(j*PI.toFloat()/32f)*r,0f,sin(j*PI.toFloat()/32f)*r)
            quad(p(i,.976f),p(i,1.02f),p(i+1,1.02f),p(i+1,.976f),colour)
        }
    }.build(lighting=2f).also {it.upload()} }
    private val m=FloatArray(16)
    private val root=FloatArray(16)
    private var current:V3?=null
    private var area:Boolean?=null
    private var lastTime=0f
    private var companion:V3?=null
    private var companionTime=0f
    private var companionId=-1
    private var companionCave=false

    private fun drawMoth(point:V3,heading:Float,flap:Float,size:Float,submit:(BergMesh,FloatArray)->Unit) {
        Matrix.setIdentityM(root,0);Matrix.translateM(root,0,point.x,point.y,point.z)
        Matrix.rotateM(root,0,heading,0f,1f,0f);Matrix.scaleM(root,0,size,size,size)
        submit(moth,root)
        for(side in listOf(-1f,1f)) {
            System.arraycopy(root,0,m,0,16);Matrix.scaleM(m,0,side,1f,1f)
            Matrix.rotateM(m,0,flap,0f,0f,1f);submit(wing,m)
        }
    }

    private fun drawCompanion(f:BergFrame,submit:(BergMesh,FloatArray)->Unit) {
        val p=f.walkers.firstOrNull()
        if(!f.mothFriend || p==null || f.echo.active) {companion=null;return}
        val size=p.appearance.look.height*1.25f
        val y=Highlands.floor(p.x,p.z,f.cave)
        val side=if(f.moving && f.motion) -.92f else -.43f
        val forward=if(f.moving && f.motion) -.46f else .06f
        val pack=V3(p.x+(cos(p.facing)*side+sin(p.facing)*forward)*size,
            y+(if(f.moving && f.motion) 1.4f+.13f*sin(f.time*3f) else .84f)*size,
            p.z+(-sin(p.facing)*side+cos(p.facing)*forward)*size)
        val hand=BergServing.hand(f,p,-69f*f.friendReach,-1f)+V3(0f,y+.10f,0f)
        val destination=pack+(hand-pack)*f.friendReach
        val prev=companion;val dt=(f.time-companionTime).coerceIn(0f,.1f)
        val here=if(f.motion && p.appearance.id==companionId && companionCave==f.cave && prev!=null)
            prev+(destination-prev)*(1f-exp(-dt*7f)) else destination
        companion=here;companionTime=f.time;companionCave=f.cave;companionId=p.appearance.id
        val flap=if(f.motion && (f.moving || f.friendReach in .03f..0.96f)) 37f+sin(f.time*14f)*32f else 57f
        drawMoth(here,p.facing*180f/PI.toFloat()-18f,flap,.57f,submit)
    }

    fun draw(f:BergFrame,glow:(Float)->Unit,opacity:Int,submit:(BergMesh,FloatArray)->Unit) {
        val e=f.echo;val site=BergEcho.site(f.cave)
        drawCompanion(f,submit)
        if(hypot(site.x-f.x,site.z-f.z)>34f) {current=null;return}
        val y=Highlands.floor(site.x,site.z,f.cave)
        fun at(p:V3) {Matrix.setIdentityM(m,0);Matrix.translateM(m,0,p.x,p.y,p.z)}
        for(i in stones.indices) {
            val p=BergEcho.stone(f.cave,i)
            at(V3(p.x,Highlands.floor(p.x,p.z,f.cave),p.z))
            if(e.tone==i && e.active) glow(if(f.motion) e.glow*.45f else .23f)
            submit(stones[i],m);glow(0f)
        }
        val bell=BergEcho.stone(f.cave,e.tone.coerceIn(0,2))
        val perch=V3(bell.x,Highlands.floor(bell.x,bell.z,f.cave)+when(e.tone.coerceIn(0,2)){0->.81f;1->1.07f;else->1.27f},bell.z)
        val drift=if(f.motion && e.active) f.time else 0f
        val target=e.guide?.let {V3(it.x,it.y,it.z)} ?: if(e.active) perch+V3(cos(drift*1.3f)*.32f,.77f+sin(drift*1.5f)*.14f,.05f) else perch
        val dt=(f.time-lastTime).coerceIn(0f,.1f)
        val previous=current
        val point=if(f.motion && previous!=null && area==f.cave && e.active) previous+(target-previous)*(1f-exp(-dt*5f)) else target
        current=point;area=f.cave;lastTime=f.time
        val reply=e.replyLength>0f
        if(reply) {
            val rise=if(f.motion) min(1f,min(e.replyAge/.72f,(e.replyLength-e.replyAge)/.46f)).coerceAtLeast(0f) else 1f
            glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false)
            at(V3(site.x,y,site.z-1.6f));Matrix.scaleM(m,0,1f,.85f+rise*.15f,1f)
            Matrix.rotateM(m,0,if(f.motion) sin(e.replyAge*1.38f)*3.8f else 0f,0f,1f,0f)
            glUniform1f(opacity,rise*.18f);submit(veil,m)
            glUniform1f(opacity,rise*.81f);submit(ribbon,m)
            if(!f.cave && e.tone>=0) for(i in 0..2) {
                val t=if(f.motion) (e.replyAge*.44f+i/3f)%1f else .21f+i*.26f
                at(V3(1.6f,Highlands.WATER+.033f,4.3f))
                val radius=.34f+2.16f*t
                Matrix.scaleM(m,0,radius,1f,radius)
                glUniform1f(opacity,rise*(1f-t)*.60f);submit(waterRings[(e.tone+i)%3],m)
            }
            glUniform1f(opacity,1f);glDepthMask(true);glDisable(GL_BLEND)
        }
        if(e.active && !reply && e.tone>=0 && e.glow>0f && f.walkers.isNotEmpty()) {
            val player=f.walkers.first()
            val hand=BergServing.hand(f,player,e.armAngle,-1f)+V3(0f,Highlands.floor(player.x,player.z,f.cave),0f)
            val end=perch+V3(0f,.10f,0f)
            val progress=if(f.motion) 1f-e.glow else 1f
            for(i in 0 until (if(f.motion) 5 else 1)) {
                val t=(progress*1.3f-i*.072f).coerceIn(0f,1f)
                at(hand+(end-hand)*t+V3(0f,sin(t*PI.toFloat())*.46f,0f));submit(lights[e.tone],m)
            }
        }
        val count=if(reply) 3 else 1
        for(i in 0 until count) {
            val a=drift*1.24f+i*2.40f
            val fly=if(i==0) point else point+V3(cos(a)*(.48f+i*.29f),.16f+sin(a*1.37f)*.28f,sin(a)*.77f)
            val heading=if(f.motion && e.active) sin(a*.67f)*30f+20f*i else -18f
            val flap=if(f.motion && e.active) 38f+sin(f.time*12f+i)*32f else 50f
            drawMoth(fly,heading,flap,if(i==0) 1f else .70f,submit)
            if(e.active) {
                val key=(e.tone.coerceAtLeast(0)+i)%3
                for(j in 1..3) {
                    at(fly+V3(-j*.095f,j*.04f,-j*.15f));val s=.95f-j*.17f
                    Matrix.scaleM(m,0,s,s,s);submit(lights[key],m)
                }
            }
        }
    }
}
