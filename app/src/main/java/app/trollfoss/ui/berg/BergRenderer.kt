package app.trollfoss.ui.berg

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Trace
import android.content.res.AssetManager
import app.trollfoss.domain.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*

internal data class BergAppearance(val id:Int,val look:Look,val carried:List<Pair<ThingType,Int>>,val name:String="")
internal data class BergWalker(val appearance:BergAppearance,val x:Float,val z:Float,val facing:Float)
internal data class BergFrame(
    val x:Float=0f,val z:Float=18f,val cave:Boolean=false,val yaw:Float=0f,val pitch:Float=.25f,
    val distance:Float=10f,val time:Float=0f,val moving:Boolean=false,
    val motion:Boolean=true,val shake:Boolean=true,val light:Boolean=false,val taken:Int=0,
    val held:BergItem=BergItem.NONE,val walkers:List<BergWalker> = emptyList(),val destination:BergPoint?=null,
    val rumle:RumleMoment=RumleMoment.rest,val visitFocus:Float=0f,val treasure:Boolean=false,
    val harePlay:Float=0f,val hareIndex:Int=-1,
    val treasureSlot:Int=0,
    val skipPlay:Float=0f,val skipOrigin:BergPoint=BergPoint(5f,7f),
    val pot:BerryPot=BerryPot.EMPTY,val sharedJuice:Boolean=false,val lastBottle:BergItem=BergItem.NONE,
    val pickedBerries:Boolean=false,val kitchenPlay:Float=0f,val kitchenEvent:BergEvent=BergEvent.NONE,
    val crystalPlay:Float=0f,val crystalIndex:Int=-1,
    val panPlay:Float=0f,
    val panFocus:Float=0f,
    val night:Boolean=false,
    val butterflyPlay:Float=0f,
    val christmas:Boolean=false,val echo:EchoFrame=EchoFrame(),val mothFriend:Boolean=false,val friendReach:Float=0f,
    val roomFocus:Float=0f,val roomTarget:BergAction=BergAction.MOUSE,
    val caveMouseAge:Float=-1f,val springAwake:Boolean=false,val springAge:Float=-1f,val springKey:Int=0,
)

/** All GLES calls and GPU resource ownership stay on GLSurfaceView's render thread. */
internal class BergRenderer(assets: AssetManager, lowDetail: Boolean = false) : GLSurfaceView.Renderer {
    private val art=BergAssets(assets,lowDetail)
    @Volatile var frame=BergFrame()
    @Volatile var failure:String?=null
        private set
    @Volatile var frames=0
        private set
    @Volatile var contexts=0
        private set
    @Volatile var drawNanos=0L
        private set
    @Volatile var triangleCount=0
        private set
    private data class PickView(val inverse:FloatArray,val width:Int,val height:Int,val cave:Boolean,val yaw:Float)
    @Volatile private var pickView:PickView?=null
    private var width=1;private var height=1
    private var program=0
    private var position=0;private var normal=0;private var color=0
    private var uModel=0;private var uVp=0;private var uFog=0;private var uEye=0;private var uCave=0;private var uTime=0;private var uWater=0
    private var uLighting=0;private var uShadow=0;private var uOpacity=0;private var uGlow=0
    private var uGuestGaze=0;private var uBreeze=0
    private var uScenery=0
    private var uWinter=0;private var uNight=0;private var uSky=0;private var uLanterns=0
    private val lanterns=floatArrayOf(
        9.3f,Highlands.height(8.4f,-11.7f)+1.82f,-11.7f,
        18.2f,Highlands.height(17.3f,-12.5f)+1.82f,-12.5f,
        -11.1f,Highlands.height(-12f,7f)+1.82f,7f,
        -19f,Highlands.height(-19f,12f)+1.3f,13.6f,
    )
    private val projection=FloatArray(16);private val view=FloatArray(16);private val vp=FloatArray(16);private val model=FloatArray(16)
    private val scenery=HashMap<Boolean,BergMesh>()
    private val avatars=HashMap<BergAppearance,BergAvatar>()
    private val items=HashMap<BergItem,BergMesh>()
    private val crystals=HashMap<Int,BergMesh>()
    private val servingGestures=setOf(RumleGesture.DRINK,RumleGesture.EAT,RumleGesture.TOAST)
    private val crystalNames=listOf("blue","lilac","honey")
    private lateinit var rumle:BergRumle
    private lateinit var caveLife:BergCaveLife
    private lateinit var spring:BergSpringArt
    private lateinit var stations:BergBerryStations
    private lateinit var echoArt:BergEchoArt
    private lateinit var skyMesh:BergMesh
    private lateinit var nightSky:BergMesh
    private lateinit var christmasOutside:BergMesh
    private lateinit var christmasCave:BergMesh
    private lateinit var water:BergMesh
    private lateinit var marker:BergMesh
    private lateinit var puff:BergMesh
    private lateinit var bird:BergMesh
    private lateinit var birdWing:BergMesh
    private lateinit var butterfly:BergMesh
    private lateinit var butterflyWing:BergMesh
    private lateinit var shadow:BergMesh
    private lateinit var hare:BergMesh
    private lateinit var skippingStone:BergMesh
    private lateinit var ripple:BergMesh
    private lateinit var goldPan:BergMesh
    private lateinit var panWater:BergMesh

    override fun onSurfaceCreated(gl:GL10?,config:EGLConfig?) {
        contexts++
        failure=null;frames=0;scenery.clear();avatars.clear();items.clear();crystals.clear()
        try {
            val vertex=shader(GL_VERTEX_SHADER,"""
                uniform mat4 uModel; uniform mat4 uVP;
                uniform mediump float uWinter;uniform mediump float uNight;uniform mediump float uLighting;uniform vec3 uLanterns[4];
                uniform mediump float uRumlePart; uniform mediump vec4 uFace;
                uniform mediump vec2 uGuestGaze; uniform mediump float uBreeze;
                attribute vec3 aPosition; attribute vec3 aNormal; attribute vec3 aColor;
                varying vec3 vColor; varying vec3 vNormal; varying vec3 vWorld; varying vec3 vLocal;varying vec3 vNightLight;
                mat2 turn(float a) { float c=cos(a),s=sin(a);return mat2(c,-s,s,c); }
                void main() {
                  vec3 p=aPosition, n=aNormal;
                  if (uRumlePart>0.5) {
                    if(uRumlePart>1.5) {
                      if(uRumlePart>2.5) {p.x+=0.15*uFace.x;p.y-=0.12*uFace.y;}
                      p.y=4.555+(p.y-4.555)*(1.0-0.97*uFace.w);
                      p.z-=0.08*uFace.w;
                    }
                    float jaw=(1.0-smoothstep(3.73,3.99,p.y))*smoothstep(3.34,3.57,p.y)
                              *(1.0-smoothstep(0.62,0.91,abs(p.x)))*smoothstep(0.72,1.12,p.z);
                    p.y-=jaw*uFace.z;
                    float neck=smoothstep(3.29,4.10,p.y);
                    vec3 pivot=vec3(0.0,3.55,0.0);
                    p-=pivot;
                    p.yz=turn(-uFace.y*neck)*p.yz;n.yz=turn(-uFace.y*neck)*n.yz;
                    p.xz=turn(uFace.x*neck)*p.xz;n.xz=turn(uFace.x*neck)*n.xz;
                    p+=pivot;
                  }
                  if(uGuestGaze.x!=0.0 || uGuestGaze.y!=0.0) {
                    float head=smoothstep(1.14,1.41,p.y);
                    p.y-=1.24;
                    p.yz=turn(uGuestGaze.y*head)*p.yz;n.yz=turn(uGuestGaze.y*head)*n.yz;
                    p.xz=turn(uGuestGaze.x*head)*p.xz;n.xz=turn(uGuestGaze.x*head)*n.xz;
                    p.y+=1.24;
                  }
                  if(uBreeze!=0.0) {
                    float hem=smoothstep(0.19,0.32,-p.z);
                    p.x+=hem*hem*uBreeze;p.z-=hem*abs(uBreeze)*0.42;
                  }
                  vec4 world=uModel*vec4(p,1.0);vWorld=world.xyz;
                  vLocal=aPosition;vNormal=normalize(mat3(uModel)*n);vColor=aColor;
                  if(uWinter>0.5 && aNormal.y>0.55 && aColor.g>aColor.r*1.025 && aColor.g>aColor.b*1.025) vColor=mix(aColor,vec3(0.89,0.94,0.98),0.94);
                  vNightLight=vec3(0.45,0.55,0.78);
                  if(uNight>0.0 && uLighting<1.5) {
                    float glow=0.0;
                    for(int i=0;i<4;i++) {vec3 d=world.xyz-uLanterns[i];float a=max(0.0,1.0-dot(d,d)/36.0);glow+=a*a;}
                    vNightLight=mix(vNightLight,vec3(1.18,0.98,0.66),min(1.0,glow));
                  }
                  gl_Position=uVP*world; }
            """.trimIndent())
            val fragment=shader(GL_FRAGMENT_SHADER,"""
                precision mediump float;
                uniform sampler2D uSurface;uniform float uScenery;
                uniform vec3 uFog;uniform vec3 uEye;uniform float uCave;uniform float uTime;uniform float uWater;uniform float uLighting;uniform float uShadow;uniform float uOpacity;uniform float uGlow;uniform float uNight;uniform float uSky;
                varying vec3 vColor;varying vec3 vNormal;varying vec3 vWorld;varying vec3 vLocal;varying vec3 vNightLight;
                void main() {
                  if(uShadow>0.5) {
                    float alpha=(1.0-smoothstep(0.0,1.0,length(vLocal.xz/vec2(0.65,0.5))))*0.27;
                    gl_FragColor=vec4(0.12,0.15,0.16,alpha);return;
                  }
                  if(uLighting>1.5) {
                    vec3 sky=mix(vColor,vColor*vec3(0.13,0.20,0.36)+vec3(0.015,0.022,0.045),uSky*uNight);
                    gl_FragColor=vec4(sky,uOpacity);return;
                  }
                  vec3 n=normalize(vNormal);float sun=max(dot(n,normalize(vec3(-0.5,0.85,0.4))),0.0);
                  vec3 col=vColor*(vec3(0.66,0.69,0.74)+sun*vec3(0.36,0.31,0.24));
                  col=mix(col,col*vec3(1.10,0.94,0.80),uCave*0.45);
                  if(uLighting>0.5) col=vColor;
                  else if(uCave>0.5) {
                    vec3 fire=normalize(vec3(-17.0,2.2,-5.7)-vWorld);
                    float warm=max(dot(n,fire),0.0);
                    float rim=pow(1.0-max(dot(n,normalize(uEye-vWorld)),0.0),2.0);
                    col=vColor*(vec3(0.63,0.62,0.66)+sun*vec3(0.25,0.25,0.23)+warm*vec3(0.34,0.16,0.065));
                    col+=vec3(0.034,0.027,0.018)*rim;
                  }
                  if(uScenery>0.5) {
                    vec3 an=abs(n);
                    vec2 plane=an.y>max(an.z,an.x)?vWorld.xz:(an.z>an.x?vWorld.xy:vWorld.zy);
                    vec4 detail=texture2D(uSurface,plane*0.32);
                    float chroma=max(max(col.r,col.g),col.b)-min(min(col.r,col.g),col.b);
                    float rock=1.0-smoothstep(0.10,0.30,chroma);
                    float timber=smoothstep(0.14,0.32,col.r-col.b)*smoothstep(0.04,0.18,col.r-col.g);
                    float meadow=smoothstep(0.01,0.13,col.g-col.r)*(1.0-uCave);
                    float close=1.0-smoothstep(18.0,60.0,distance(vWorld,uEye));
                    col*=1.0+close*((detail.r-0.50)*(0.22+0.23*rock)+(detail.b-0.50)*timber*0.19);
                    col+=close*(detail.g*rock*vec3(0.060,0.057,0.055)+(detail.a-0.5)*meadow*vec3(0.061,0.075,0.026));
                    if(uCave>0.5) {
                      vec3 d=vWorld-vec3(-17.0,1.2,-5.6);
                      col+=vec3(0.070,0.032,0.010)*max(0.0,1.0-dot(d,d)/34.0)*(0.75+0.25*sin(uTime*3.2));
                    }
                  }
                  col*=mix(vec3(1.0),vNightLight,uNight);
                  col=mix(col,vColor*1.18+vec3(0.16),uGlow);
                  if(uWater>0.5) {
                    float wave=sin(vWorld.x*2.5+vWorld.z*3.0+uTime)*sin(vWorld.x*0.7-vWorld.z*2.0+uTime*0.6);
                    if(abs(vWorld.y-0.85)<0.07) {
                      vec2 lake=(vWorld.xz-vec2(-3.0,1.0))/vec2(10.0,8.0);
                      float shallows=smoothstep(0.36,0.94,dot(lake,lake));
                      vec3 water=mix(vec3(0.15,0.38,0.46),vec3(0.34,0.57,0.54),shallows);
                      col=mix(col,water*mix(vec3(1.0),vNightLight,uNight),0.74);
                    }
                    vec3 view=normalize(uEye-vWorld);
                    float fresnel=pow(1.0-clamp(view.y,0.0,1.0),3.0);
                    vec3 reflection=mix(vec3(0.66,0.78,0.83),vec3(0.17,0.24,0.37),uNight);
                    col=mix(col,reflection,0.48*fresnel)+vec3(0.13,0.14,0.12)*smoothstep(0.80,1.0,wave)*(1.0-0.70*uNight);
                  }
                  float fog=smoothstep(50.0,170.0,distance(vWorld,uEye))*(1.0-uCave*0.7);
                  gl_FragColor=vec4(mix(col,uFog,fog),uOpacity);
                }
            """.trimIndent())
            program=glCreateProgram();glAttachShader(program,vertex);glAttachShader(program,fragment);glLinkProgram(program)
            val status=IntArray(1);glGetProgramiv(program,GL_LINK_STATUS,status,0)
            check(status[0]!=0) { glGetProgramInfoLog(program) }
            glDeleteShader(vertex);glDeleteShader(fragment)
            position=glGetAttribLocation(program,"aPosition");normal=glGetAttribLocation(program,"aNormal");color=glGetAttribLocation(program,"aColor")
            uModel=glGetUniformLocation(program,"uModel");uVp=glGetUniformLocation(program,"uVP")
            uFog=glGetUniformLocation(program,"uFog");uEye=glGetUniformLocation(program,"uEye");uCave=glGetUniformLocation(program,"uCave")
            uTime=glGetUniformLocation(program,"uTime");uWater=glGetUniformLocation(program,"uWater")
            uLighting=glGetUniformLocation(program,"uLighting")
            uShadow=glGetUniformLocation(program,"uShadow")
            uOpacity=glGetUniformLocation(program,"uOpacity")
            uGlow=glGetUniformLocation(program,"uGlow")
            uWinter=glGetUniformLocation(program,"uWinter")
            uNight=glGetUniformLocation(program,"uNight");uSky=glGetUniformLocation(program,"uSky");uLanterns=glGetUniformLocation(program,"uLanterns[0]")
            uGuestGaze=glGetUniformLocation(program,"uGuestGaze");uBreeze=glGetUniformLocation(program,"uBreeze")
            uScenery=glGetUniformLocation(program,"uScenery")
            glUseProgram(program);glUniform1i(glGetUniformLocation(program,"uSurface"),0);uploadBergSurface()
            glEnable(GL_DEPTH_TEST);glDepthFunc(GL_LEQUAL)
            // Terrain, bridges and cave walls are intentionally visible from both sides.
            glDisable(GL_CULL_FACE);glDisable(GL_BLEND)
            rumle=BergRumle(art,glGetUniformLocation(program,"uFace"),glGetUniformLocation(program,"uRumlePart"))
            stations=BergBerryStations(art);echoArt=BergEchoArt(art)
            caveLife=BergCaveLife();spring=BergSpringArt()
            water=BergArt.water().also { it.upload() }
            skyMesh=art.mesh("sky").also { it.upload() }
            nightSky=BergArt.nightSky().also {it.upload()}
            christmasOutside=bergChristmas(false).also {it.upload()}
            christmasCave=bergChristmas(true).also {it.upload()}
            marker=BergArt.marker().also { it.upload() };puff=BergArt.puff().also { it.upload() }
            bird=art.mesh("bird").also {it.upload()}
            birdWing=art.mesh("bird-wing").also {it.upload()};shadow=BergArt.shadow().also {it.upload()}
            hare=art.mesh("hare").also {it.upload()}
            butterfly=art.mesh("butterfly").also {it.upload()};butterflyWing=art.mesh("butterfly-wing").also {it.upload()}
            skippingStone=BergArt.skippingStone().also {it.upload()};ripple=BergArt.ripple().also {it.upload()}
            goldPan=art.mesh("gold-pan").also {it.upload()};panWater=BergArt.panWater().also {it.upload()}
            items.putAll(BergArt.items(art,stations))
        } catch(e:Exception) { failure=e.message ?: "GLES setup failed" }
    }
    private fun shader(type:Int,source:String):Int {
        val shader=glCreateShader(type);glShaderSource(shader,source);glCompileShader(shader)
        val status=IntArray(1);glGetShaderiv(shader,GL_COMPILE_STATUS,status,0)
        check(status[0]!=0) { glGetShaderInfoLog(shader) };return shader
    }
    override fun onSurfaceChanged(gl:GL10?,w:Int,h:Int) { width=w.coerceAtLeast(1);height=h.coerceAtLeast(1);glViewport(0,0,width,height) }
    override fun onDrawFrame(gl:GL10?) {
        if(failure!=null) return
        val start=System.nanoTime()
        val f=frame
        Trace.beginSection("Heileberget.draw")
        try {
            val sky=if(f.cave) floatArrayOf(.16f,.17f,.21f) else if(f.night) floatArrayOf(.14f,.20f,.34f) else floatArrayOf(.78f,.84f,.85f)
            glClearColor(sky[0],sky[1],sky[2],1f);glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glUseProgram(program)
            glUniform1f(uOpacity,1f)
            glUniform1f(uGlow,0f)
            glUniform1f(uNight,if(f.night && !f.cave) 1f else 0f);glUniform1f(uSky,0f)
            glUniform3fv(uLanterns,4,lanterns,0)
            glEnableVertexAttribArray(position);glEnableVertexAttribArray(normal);glEnableVertexAttribArray(color)
            // A short overhead view reveals the bowl without changing saved yaw.
            // Steering cancels this focus; reduced motion keeps the normal camera.
            val focus=if(f.motion && !f.cave) f.panFocus.coerceIn(0f,1f) else 0f
            val room=if(f.motion && f.cave) f.roomFocus else 0f
            val subjectX=if(f.roomTarget==BergAction.MOUSE) CaveMouse.meeting.x else BergSpring.X
            val subjectZ=if(f.roomTarget==BergAction.MOUSE) CaveMouse.meeting.z else BergSpring.Z
            val anchorX=f.x+(Highlands.goldPan.x-f.x)*focus+(subjectX-f.x)*room*.65f
            val anchorZ=f.z+(Highlands.goldPan.z-f.z)*focus+(subjectZ-f.z)*room*.65f
            val visitFocus=if(f.motion) f.visitFocus else 0f
            val hostX=if(f.cave) -3f else 13f;val hostZ=if(f.cave) -5f else -15f
            val toward=atan2(f.x-hostX,f.z-hostZ)
            // View across the child's shoulder so their body never hides the mouse or the water.
            val roomYaw=atan2(f.x-subjectX,f.z-subjectZ)+(if(f.roomTarget==BergAction.MOUSE) 1.12f else .73f)
            val yaw=f.yaw+atan2(sin(toward-f.yaw),cos(toward-f.yaw))*visitFocus+
                atan2(sin(roomYaw-f.yaw),cos(roomYaw-f.yaw))*room
            val pitch=f.pitch+(1.0f-f.pitch)*focus+(.22f-f.pitch)*visitFocus+(.31f-f.pitch)*room
            val roomDistance=if(f.roomTarget==BergAction.MOUSE) 4.05f else 4.55f
            val requested=f.distance+(7f-f.distance)*focus+(roomDistance-f.distance)*room
            val y=Highlands.floor(anchorX,anchorZ,f.cave)
            val distance=if(f.cave) Highlands.cameraDistance(anchorX,anchorZ,yaw,pitch,requested)
                else Highlands.outsideCameraDistance(anchorX,anchorZ,yaw,pitch,requested)
            val joke=if(f.rumle.gesture==RumleGesture.DRINK) f.rumle.elapsed else 0f
            val sway=if(f.motion && f.shake && joke in 4.14f..4.62f)
                sin((joke-4.14f)*32f)*.045f*(1f-(joke-4.14f)/.48f) else 0f
            val eyeX=(anchorX+sin(yaw)*cos(pitch)*distance+sway).coerceIn(if(f.cave) -19.5f else -30f,if(f.cave) 19.5f else 30f)
            // Keep the near camera above the authored plateau. Pulling beyond
            // its rear edge exposed the scenery skirt from underneath at the lookout.
            val eyeZ=(anchorZ+cos(yaw)*cos(pitch)*distance).coerceIn(if(f.cave) -27.5f else -25f,if(f.cave) 15.8f else 40f)
            val eyeY=max(y+sin(pitch)*distance+1.1f+.55f*visitFocus,Highlands.floor(eyeX,eyeZ,f.cave)+1.2f)
                .coerceAtMost(if(f.cave) Highlands.caveCameraCeiling(eyeX,eyeZ) else 100f)
            Matrix.perspectiveM(projection,0,56f,width.toFloat()/height,.2f,260f)
            Matrix.setLookAtM(view,0,eyeX,eyeY,eyeZ,anchorX-sin(yaw)*2.5f*(1-focus)*(1-room*.77f),y+1.55f-1.25f*focus+1.10f*visitFocus-.77f*room,anchorZ-cos(yaw)*2.5f*(1-focus)*(1-room*.77f),0f,1f,0f)
            Matrix.multiplyMM(vp,0,projection,0,view,0)
            glUniformMatrix4fv(uVp,1,false,vp,0)
            glUniform3f(uEye,eyeX,eyeY,eyeZ);glUniform3f(uFog,sky[0],sky[1],sky[2])
            glUniform1f(uCave,if(f.cave) 1f else 0f);glUniform1f(uTime,if(f.motion) f.time else 0f);glUniform1f(uWater,0f)
            triangleCount=0
            // Clouds are real volumes: their front faces must hide their back faces.
            // The distant dome shares this mesh and is behind all playable scenery.
            if(!f.cave) {
                glUniform1f(uSky,1f);draw(skyMesh);glUniform1f(uSky,0f)
                if(f.night) draw(nightSky)
            }
            val scene=scenery.getOrPut(f.cave) { art.mesh(if(f.cave) "cave" else "outside").also { it.upload() } }
            glUniform1f(uWinter,if(f.christmas && !f.cave) 1f else 0f)
            glUniform1f(uScenery,1f);draw(scene);glUniform1f(uScenery,0f)
            glUniform1f(uWinter,0f)
            if(f.christmas) draw(if(f.cave) christmasCave else christmasOutside)
            if(f.cave) Highlands.crystals.forEachIndexed { index,p ->
                val mesh=crystals.getOrPut(index) {art.mesh("crystal-${crystalNames[index]}").also {it.upload()}}
                val active=f.crystalIndex==index && f.crystalPlay>0f
                val strength=if(active) (f.crystalPlay/1.8f).coerceIn(0f,1f) else 0f
                glUniform1f(uGlow,if(f.motion) strength else if(active) .6f else 0f)
                draw(mesh,p.x,0f,p.z)
                glUniform1f(uGlow,0f)
                if(active) {
                    glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false)
                    glUniform1f(uOpacity,strength*.75f)
                    draw(ripple,p.x,.035f,p.z,scale=if(f.motion) .7f+(1-strength)*1.6f else 1.15f)
                    glUniform1f(uOpacity,1f);glDepthMask(true);glDisable(GL_BLEND)
                }
            }
            if(!f.cave) {
                glUniform1f(uWater,1f);draw(water);glUniform1f(uWater,0f)
                val p=Highlands.goldPan
                val progress=if(f.panPlay>0f) Highlands.PAN_SECONDS-f.panPlay else 0f
                val rocking=if(f.motion && f.panPlay>0f) sin(progress*7f)*7f*min(1f,f.panPlay*2f) else 0f
                val raised=if(f.panPlay>0f) .12f else 0f
                val base=Highlands.floor(p.x,p.z,false)+.09f+raised
                fun bowlPart(mesh:BergMesh) {
                    Matrix.setIdentityM(model,0);Matrix.translateM(model,0,p.x,base,p.z)
                    Matrix.rotateM(model,0,rocking,1f,0f,.4f)
                    glUniformMatrix4fv(uModel,1,false,model,0);glUniform1f(uLighting,mesh.lighting)
                    mesh.draw(position,normal,color);triangleCount+=mesh.triangles
                }
                bowlPart(goldPan)
                glUniform1f(uWater,if(f.panPlay>0f) 1f else 0f);bowlPart(panWater);glUniform1f(uWater,0f)
                val showing=f.taken and 2==0 || f.panPlay>0f
                if(showing) repeat(5) { i ->
                    val angle=i*2.4f+if(f.motion && f.panPlay>1f) progress*2f else 0f
                    val radius=.12f+(i%3)*.085f
                    val reveal=if(f.panPlay in .001f..1f) {if(f.motion) (1-f.panPlay)*.35f else .25f} else 0f
                    glUniform1f(uGlow,if(f.panPlay in .001f..1f) .5f else 0f)
                    draw(items.getValue(BergItem.GOLD),p.x+cos(angle)*radius,base+.115f+reveal,p.z+sin(angle)*radius,scale=.15f)
                }
                glUniform1f(uGlow,0f)
            }
            if(!f.cave && f.skipPlay>0f) {
                val elapsed=Highlands.SKIP_SECONDS-f.skipPlay
                if(f.motion && elapsed<Highlands.skipTimes.last()) {
                    val hop=Highlands.skipTimes.indexOfFirst {elapsed<it}
                    val start=if(hop==0) f.skipOrigin else Highlands.skipLandings[hop-1]
                    val end=Highlands.skipLandings[hop]
                    val startTime=if(hop==0) 0f else Highlands.skipTimes[hop-1]
                    val t=((elapsed-startTime)/(Highlands.skipTimes[hop]-startTime)).coerceIn(0f,1f)
                    val fromY=if(hop==0) Highlands.floor(start.x,start.z,false)+1f else Highlands.WATER+.04f
                    val stoneY=fromY*(1-t)+(Highlands.WATER+.04f)*t+sin(t*PI.toFloat())*(if(hop==0) .5f else .65f/hop)
                    draw(skippingStone,start.x+(end.x-start.x)*t,stoneY,start.z+(end.z-start.z)*t,elapsed*520)
                }
                glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false)
                Highlands.skipLandings.forEachIndexed { i,p ->
                    val age=if(f.motion) elapsed-Highlands.skipTimes[i] else .5f
                    if(age in 0f..1.5f) {
                        glUniform1f(uOpacity,if(f.motion) (1f-age/1.5f)*.8f else .7f)
                        draw(ripple,p.x,Highlands.WATER+.025f,p.z,scale=.12f+age*.9f)
                    }
                }
                glUniform1f(uOpacity,1f);glDepthMask(true);glDisable(GL_BLEND)
            }
            if(f.cave) {spring.draw(f,uOpacity,::submit);caveLife.draw(f,::submit)}
            echoArt.draw(f,{glUniform1f(uGlow,it)},uOpacity,::submit)
            stations.draw(f,items,::submit)
            rumle.draw(f,items,::submit)
            val flight=if(f.motion) f.time*.35f else 0f
            if(!f.cave) repeat(3) { i ->
                val orbit=flight+i*2f
                val bx=-3f+cos(orbit)*8f;val by=5f+sin(flight*2f+i)*.5f;val bz=1f+sin(orbit)*6f
                val heading=atan2(-sin(orbit)*8f,cos(orbit)*6f)*180f/PI.toFloat()
                draw(bird,bx,by,bz,heading)
                val flap=if(f.motion) sin(f.time*12f+i)*34f else 10f
                for(side in listOf(-1f,1f)) {
                    Matrix.setIdentityM(model,0);Matrix.translateM(model,0,bx,by+.02f,bz)
                    Matrix.rotateM(model,0,heading,0f,1f,0f);Matrix.translateM(model,0,side*.11f,0f,0f)
                    Matrix.rotateM(model,0,side*flap,0f,0f,1f);Matrix.scaleM(model,0,side,1f,1f)
                    glUniformMatrix4fv(uModel,1,false,model,0);glUniform1f(uLighting,birdWing.lighting)
                    birdWing.draw(position,normal,color);triangleCount+=birdWing.triangles
                }
            }
            if(!f.cave) Highlands.hares.forEachIndexed { index,p ->
                val playing=f.harePlay>0f && f.hareIndex==index
                val phase=5f-f.harePlay
                val hop=if(playing && f.motion && phase<2.4f) abs(sin(phase*PI.toFloat()*1.25f))*.32f else 0f
                val angle=if(playing) atan2(f.x-p.x,f.z-p.z)*180f/PI.toFloat() else sin(flight+index)*12f
                draw(hare,p.x,Highlands.floor(p.x,p.z,false)+hop,p.z,angle)
                if(playing) draw(marker,p.x,Highlands.floor(p.x,p.z,false)+.03f,p.z,scale=.65f)
            }
            if(f.cave && f.treasure) repeat(7) { i ->
                val angle=i*.9f+flight
                val chest=Highlands.treasures[f.treasureSlot.coerceIn(0,Highlands.treasures.lastIndex)]
                draw(items.getValue(BergItem.GOLD),chest.x+cos(angle)*1.7f,1.8f+sin(angle*2)*.3f,chest.z+sin(angle),scale=.5f)
            }
            if(f.cave && f.motion && joke in 4.14f..5.12f) for(i in 0..3) {
                val growth=(joke-4.14f)*.7f
                draw(puff,-5f-i*.3f-growth,1f+growth+i*.25f,-5f,scale=.25f+growth*.4f)
            }
            if(!f.cave) {
                if(f.taken and 1==0) draw(items.getValue(BergItem.DIAMOND),-12f,Highlands.floor(-12f,10f,false)+.1f,10f,angle=if(f.motion) f.time*35f else 0f)
                if(f.light) {
                    draw(items.getValue(BergItem.DIAMOND),-9f,Highlands.floor(-9f,8f,false)+1f,8f,scale=.65f)
                    for(i in 0..9) draw(marker,-8f+i*2f,Highlands.floor(-8f+i*2f,1f,false)+.1f,1f,scale=.32f)
                }
            }
            val present=f.walkers.map { it.appearance }.toSet()
            val stale=avatars.keys.filter { it !in present }
            for(key in stale) avatars.remove(key)?.release()
            // Soft contact shadows are blended once per person, without writing depth.
            glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false);glUniform1f(uShadow,1f)
            val rx=if(f.cave) -3f else 13f;val rz=if(f.cave) -5f else -15f
            draw(shadow,rx,Highlands.floor(rx,rz,f.cave)+.032f,rz+.5f,scale=3.6f)
            for(person in f.walkers) draw(shadow,person.x,Highlands.floor(person.x,person.z,f.cave)+.025f,person.z,scale=person.appearance.look.height*1.25f)
            glUniform1f(uShadow,0f);glDepthMask(true);glDisable(GL_BLEND)
            f.walkers.forEachIndexed { index,person ->
                val size=person.appearance.look.height*1.25f
                val ground=Highlands.floor(person.x,person.z,f.cave)
                val opacity=Highlands.cameraPersonOpacity(eyeX,eyeY,eyeZ,person.x,ground,person.z,size)
                if(index==0) draw(marker,person.x,ground+.035f,person.z,scale=.8f)
                if(opacity<.01f) return@forEachIndexed
                if(opacity<1f) {glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)}
                glUniform1f(uOpacity,opacity)
                val meshes=avatars.getOrPut(person.appearance) {
                    BergCharacters.load(person.appearance,art).also { it.upload() }
                }
                val bob=if(f.motion && f.moving) abs(sin(f.time*9f-index))*.055f else 0f
                val a=person.facing*180f/PI.toFloat()
                val (headYaw,headPitch)=guestGaze(f,person,index==0)
                glUniform2f(uGuestGaze,headYaw,headPitch)
                draw(meshes.body,person.x,ground+bob,person.z,a,size)
                val eyes=if(BergCharacters.blinking(f.time,person.appearance.id,f.motion)) meshes.eyesClosed else meshes.eyesOpen
                eyes?.let {draw(it,person.x,ground+bob,person.z,a,size)}
                meshes.accessories?.let {draw(it,person.x,ground+bob,person.z,a,size)}
                glUniform2f(uGuestGaze,0f,0f)
                draw(echoArt.kit,person.x,ground+bob,person.z,a,size)
                val breeze=if(f.motion) sin(f.time*(if(f.moving) 6f else 2f)+index)*(if(f.moving) .092f else .027f) else 0f
                glUniform1f(uBreeze,breeze)
                draw(meshes.scarf,person.x,ground+bob,person.z,a,size)
                glUniform1f(uBreeze,0f)
                for(limb in meshes.limbs) {
                    val occupied=limb.arm && limb.side>0f && (person.appearance.carried.isNotEmpty() || (index==0 && f.held!=BergItem.NONE))
                    val throwing=index==0 && limb.arm && limb.side<0f && f.skipPlay>Highlands.SKIP_SECONDS-.65f
                    val offering=index==0 && limb.arm && limb.side<0f && !f.cave && f.butterflyPlay>0f
                    val handover=index==0 && limb.arm && limb.side>0f && f.rumle.gesture in servingGestures && BergServing.guest(f)!=null
                    val working=index==0 && limb.arm && limb.side<0f && f.kitchenPlay>0f && !f.moving
                    val swing=if(handover) BergServing.arm(f)
                        else if(index==0 && limb.arm && limb.side<0f && f.echo.active) f.echo.armAngle
                        else if(index==0 && limb.arm && limb.side<0f && f.friendReach>0f) -69f*f.friendReach
                        else if(index==0 && limb.arm && limb.side<0f && !f.moving && f.caveMouseAge in 0f..5f && CaveMouse.canCall(person.x,person.z))
                            -52f*(if(f.motion) sin(f.caveMouseAge/5f*PI.toFloat()) else 1f)
                        else if(index==0 && limb.arm && limb.side<0f && !f.moving && f.springAge in 0f..1.6f && BergSpring.inReach(person.x,person.z))
                            -57f*sin(f.springAge/1.6f*PI.toFloat())
                        else if(working) -52f*(if(f.motion) sin(f.kitchenPlay/1.5f*PI.toFloat()) else 1f)
                        else if(offering) -65f*butterflyPose(f)
                        else if(f.motion && throwing) -sin((Highlands.SKIP_SECONDS-f.skipPlay)/.65f*PI.toFloat())*70f
                        else if(f.motion && f.moving && !occupied) sin(f.time*9f-index)*limb.side*(if(limb.arm) -20f else 24f) else 0f
                    Matrix.setIdentityM(model,0);Matrix.translateM(model,0,person.x,ground+bob,person.z)
                    Matrix.rotateM(model,0,a,0f,1f,0f);Matrix.scaleM(model,0,size,size,size)
                    Matrix.translateM(model,0,limb.x,limb.y,0f);Matrix.rotateM(model,0,swing,1f,0f,0f)
                    glUniformMatrix4fv(uModel,1,false,model,0);glUniform1f(uLighting,limb.mesh.lighting)
                    limb.mesh.draw(position,normal,color);triangleCount+=limb.mesh.triangles
                }
                if(meshes.leg!=null) for(side in listOf(-1f,1f)) {
                    val swing=if(f.motion && f.moving) sin(f.time*9f-index)*side*.14f else 0f
                    val dx=cos(person.facing)*side*.17f*size+sin(person.facing)*swing
                    val dz=-sin(person.facing)*side*.17f*size+cos(person.facing)*swing
                    draw(meshes.leg,person.x+dx,ground+max(0f,swing*.5f),person.z+dz,a,size)
                }
                if(index==0) {
                    val fillingCup=f.kitchenEvent==BergEvent.POUR && f.kitchenPlay>0f && f.held==BergItem.BERRY_CUP && !f.moving
                    if(f.held!=BergItem.NONE && !fillingCup) {
                        val hx=person.x+(cos(person.facing)*.43f+sin(person.facing)*.13f)*size
                        val hz=person.z+(-sin(person.facing)*.43f+cos(person.facing)*.13f)*size
                        draw(items.getValue(f.held),hx,ground+bob+.7f*size,hz,a,scale=(if(f.held==BergItem.BERRIES || f.held==BergItem.BERRY_CUP) .95f else .6f)*size)
                    }
                }
                glUniform1f(uOpacity,1f);glDisable(GL_BLEND)
            }
            if(!f.cave) drawButterfly(f)
            f.destination?.let { draw(marker,it.x,Highlands.floor(it.x,it.z,f.cave)+.09f,it.z) }
            val inverse=FloatArray(16)
            if(Matrix.invertM(inverse,0,vp,0)) pickView=PickView(inverse,width,height,f.cave,yaw)
            val error=glGetError();check(error==GL_NO_ERROR) { "OpenGL error $error" }
            frames++
        } catch(e:Exception) { failure=e.message ?: "3D draw failed" }
        finally {Trace.endSection();drawNanos=System.nanoTime()-start}
    }
    private fun draw(mesh:BergMesh,x:Float=0f,y:Float=0f,z:Float=0f,angle:Float=0f,scale:Float=1f) {
        Matrix.setIdentityM(model,0);Matrix.translateM(model,0,x,y,z)
        if(angle!=0f) Matrix.rotateM(model,0,angle,0f,1f,0f)
        if(scale!=1f) Matrix.scaleM(model,0,scale,scale,scale)
        glUniformMatrix4fv(uModel,1,false,model,0);glUniform1f(uLighting,mesh.lighting);mesh.draw(position,normal,color);triangleCount+=mesh.triangles
    }

    private fun submit(mesh:BergMesh,transform:FloatArray) {
        glUniformMatrix4fv(uModel,1,false,transform,0);glUniform1f(uLighting,mesh.lighting)
        mesh.draw(position,normal,color);triangleCount+=mesh.triangles
    }

    private fun smooth(value:Float):Float {val t=value.coerceIn(0f,1f);return t*t*(3f-2f*t)}
    private fun butterflyPose(f:BergFrame):Float = if(!f.motion) 1f else
        smooth(min((Highlands.BUTTERFLY_SECONDS-f.butterflyPlay)/.65f,f.butterflyPlay/1.2f))

    /** The free left hand uses the same shoulder rotation as the authored arm. */
    private fun drawButterfly(f:BergFrame) {
        val home=Highlands.butterfly
        val active=f.butterflyPlay>0f && f.walkers.isNotEmpty()
        val elapsed=Highlands.BUTTERFLY_SECONDS-f.butterflyPlay
        val landing=if(!active) 0f else if(!f.motion) 1f else smooth(min(elapsed/1.8f,f.butterflyPlay/1.8f))
        val idle=if(f.motion && !f.night) f.time*.8f else 0f
        val hx=home.x+if(active) 0f else cos(idle)*.45f
        val hz=home.z+if(active) 0f else sin(idle)*.3f
        val hy=Highlands.floor(home.x,home.z,false)+if(f.night && !active) .43f else .8f
        var bx=hx;var by=hy;var bz=hz;var heading=idle*180f/PI.toFloat()
        if(active) {
            val person=f.walkers.first();val size=person.appearance.look.height*1.25f
            val authored=BergCharacters.model(person.appearance)!=null
            val angle=-65f*butterflyPose(f)*PI.toFloat()/180f
            val lx=if(authored) -.415f else -.4f
            val down=if(authored) -.445f else -.48f;val depth=if(authored) .05f else .07f
            val ly=(if(authored) 1.16f else 1.05f)+down*cos(angle)-depth*sin(angle)+.08f
            val lz=down*sin(angle)+depth*cos(angle)
            val tx=person.x+(cos(person.facing)*lx+sin(person.facing)*lz)*size
            val tz=person.z+(-sin(person.facing)*lx+cos(person.facing)*lz)*size
            val ty=Highlands.floor(person.x,person.z,false)+ly*size
            bx=hx+(tx-hx)*landing;bz=hz+(tz-hz)*landing
            by=hy+(ty-hy)*landing+if(f.motion) sin(landing*PI.toFloat())*.5f else 0f
            heading=person.facing*180f/PI.toFloat()
        } else if(f.motion && !f.night) by+=sin(f.time*2.2f)*.12f
        draw(butterfly,bx,by,bz,heading)
        val perched=landing>.99f || (f.night && !active)
        val flap=if(!f.motion || perched) 48f else 25f+sin(f.time*17f)*35f
        for(side in listOf(-1f,1f)) {
            Matrix.setIdentityM(model,0);Matrix.translateM(model,0,bx,by,bz)
            Matrix.rotateM(model,0,heading,0f,1f,0f);Matrix.rotateM(model,0,side*flap,0f,0f,1f)
            Matrix.scaleM(model,0,side,1f,1f)
            glUniformMatrix4fv(uModel,1,false,model,0);glUniform1f(uLighting,butterflyWing.lighting)
            butterflyWing.draw(position,normal,color);triangleCount+=butterflyWing.triangles
        }
    }

    private data class Ray(val origin:V3,val direction:V3) {
        fun hits(center:V3,radii:V3):Boolean {
            val d=V3(direction.x/radii.x,direction.y/radii.y,direction.z/radii.z)
            val p=V3((origin.x-center.x)/radii.x,(origin.y-center.y)/radii.y,(origin.z-center.z)/radii.z)
            val t=(-p.dot(d)/d.dot(d)).coerceAtLeast(0f)
            val nearest=p+d*t
            return nearest.dot(nearest)<=1f
        }
    }

    private fun ray(px:Float,py:Float,pick:PickView):Ray? {
        val x=px/pick.width*2f-1f;val y=1f-py/pick.height*2f
        fun unproject(depth:Float):V3 {
            val p=FloatArray(4)
            Matrix.multiplyMV(p,0,pick.inverse,0,floatArrayOf(x,y,depth,1f),0)
            return V3(p[0]/p[3],p[1]/p[3],p[2]/p[3])
        }
        val near=unproject(-1f)
        val direction=(unproject(1f)-near).unit()
        return if(direction.dot(direction)>.99f) Ray(near,direction) else null
    }

    /** A tap on a reachable character or prop uses the same action as the hand button. */
    fun hitsActivity(px:Float,py:Float,spot:BergSpot):Boolean {
        val view=pickView ?: return false
        if(view.cave!=spot.cave) return false
        val ray=ray(px,py,view) ?: return false
        return when(spot.action) {
            BergAction.TROLL,BergAction.ENTER -> {
                val x=if(spot.cave) -3f else 13f;val z=if(spot.cave) -5f else -15f
                val y=Highlands.floor(x,z,spot.cave)
                ray.hits(V3(x,y+2.15f,z+.25f),V3(1.94f,2.20f,1.13f)) ||
                    ray.hits(V3(x,y+4.52f,z+.24f),V3(1.60f,1.35f,1.12f))
            }
            BergAction.BERRIES -> {
                val p=BergKitchen.patch
                ray.hits(V3(p.x+.58f,Highlands.floor(p.x,p.z,false)+.48f,p.z-.19f),V3(1.65f,.85f,1.15f))
            }
            BergAction.KETTLE -> ray.hits(V3(-16.28f,1.00f,-5.13f),V3(1.24f,1.0f,.82f))
            else -> false
        }
    }

    fun soundYaw(cave:Boolean,default:Float)=pickView?.takeIf {it.cave==cave}?.yaw ?: default

    fun hitsMouse(px:Float,py:Float):Boolean {
        val camera=pickView?.takeIf {it.cave} ?: return false
        val f=frame
        if(!f.cave) return false
        val touch=ray(px,py,camera) ?: return false
        val nut=V3(CaveMouse.meeting.x,.105f,CaveMouse.meeting.z+.333f)
        return touch.hits(BergCaveLife.mouseTarget(f),V3(.64f,.53f,.72f)) ||
            (f.caveMouseAge<0f && touch.hits(nut,V3(.36f,.32f,.36f)))
    }
    /** The basin returns 3, the three individual caps return 0 through 2. */
    fun springTarget(px:Float,py:Float):Int? {
        val camera=pickView?.takeIf {it.cave} ?: return null
        if(!frame.cave) return null
        val touch=ray(px,py,camera) ?: return null
        for((i,cap) in BergSpring.caps.withIndex()) {
            val size=BergSpring.sizes[i]
            if(touch.hits(V3(cap.x,.27f+.55f*size,cap.z),V3(.43f,.58f,.43f)*size)) return i
        }
        return if(touch.hits(V3(BergSpring.X,BergSpring.WATER,BergSpring.Z),V3(1.48f,.43f,.92f))) 3 else null
    }

    fun hitsSatchel(px:Float,py:Float):Boolean {
        val camera=pickView ?: return false
        val f=frame;val p=f.walkers.firstOrNull() ?: return false
        if(camera.cave!=f.cave || !f.mothFriend) return false
        val ray=ray(px,py,camera) ?: return false
        val size=p.appearance.look.height*1.25f
        val x=p.x+(-.32f*cos(p.facing)+.03f*sin(p.facing))*size
        val z=p.z+(.32f*sin(p.facing)+.03f*cos(p.facing))*size
        val y=Highlands.floor(p.x,p.z,f.cave)+.73f*size
        return ray.hits(V3(x,y,z),V3(.38f,.40f,.38f)*size)
    }

    fun echoKey(px:Float,py:Float,cave:Boolean):Int? {
        val camera=pickView?.takeIf { it.cave==cave } ?: return null
        val ray=ray(px,py,camera) ?: return null
        return (0..2).firstOrNull {i ->
            val p=BergEcho.stone(cave,i)
            ray.hits(V3(p.x,Highlands.floor(p.x,p.z,cave)+.47f,p.z),V3(.54f,.64f+i*.07f,.47f))
        }
    }

    fun mothTarget(px:Float,py:Float,yaw:Float,cave:Boolean):EchoLight? {
        val camera=pickView?.takeIf {it.cave==cave} ?: return null
        val ray=ray(px,py,camera) ?: return null
        val site=BergEcho.site(cave)
        val dx=sin(yaw);val dz=cos(yaw)
        val along=ray.direction.x*dx+ray.direction.z*dz
        if(abs(along)<.03f) return null
        val t=((site.x-ray.origin.x)*dx+(site.z-1.65f-ray.origin.z)*dz)/along
        if(t !in 0f..55f) return null
        val p=ray.origin+ray.direction*t
        return EchoLight(p.x,p.y,p.z)
    }

    fun pick(px:Float,py:Float):BergPoint? {
        val view=pickView ?: return null
        val ray=ray(px,py,view) ?: return null
        for(i in 1..500) {
            val t=i*.2f;val x=ray.origin.x+ray.direction.x*t;val z=ray.origin.z+ray.direction.z*t
            if(ray.origin.y+ray.direction.y*t<=Highlands.floor(x,z,view.cave))
                return BergPoint(x,z).takeIf { Highlands.walkable(x,z,view.cave) }
        }
        return null
    }
}
