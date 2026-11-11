package app.trollfoss.ui.berg

import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform4f
import android.opengl.Matrix
import app.trollfoss.domain.*
import kotlin.math.*

/** World-space hand anchors also drive the articulated arm and the two touching cup rims. */
internal object BergServing {
    private fun ease(a:Float,b:Float,t:Float):Float {
        val u=((t-a)/(b-a)).coerceIn(0f,1f)
        return u*u*(3f-2f*u)
    }
    fun guest(f:BergFrame):BergWalker? {
        if(f.moving) return null
        val p=f.walkers.firstOrNull() ?: return null
        if(f.rumle.guest!=-1 && p.appearance.id!=f.rumle.guest) return null
        return p.takeIf {hypot(it.x-BergKitchen.serving.x,it.z-BergKitchen.serving.z)<.85f}
    }
    fun arm(f:BergFrame):Float {
        val t=f.rumle.elapsed
        if(f.rumle.item==BergItem.NONE) return 0f
        val offer=-68f*ease(0f,.26f,t)*(1f-ease(.5f,.84f,t))
        val cheers=if(f.rumle.gesture==RumleGesture.TOAST)
            -111f*ease(.58f,1.13f,t)*(1f-ease(2.22f,2.88f,t)) else 0f
        return if(f.motion) offer+cheers else if(t<.8f) -68f else if(t<2.75f && f.rumle.gesture==RumleGesture.TOAST) -95f else 0f
    }
    fun hand(f:BergFrame,p:BergWalker,degrees:Float=arm(f),side:Float=1f):V3 {
        val authored=BergCharacters.model(p.appearance)!=null
        val shoulder=if(authored) 1.16f else 1.05f
        val down=if(authored) -.445f else -.48f
        val depth=if(authored) .05f else .07f
        val angle=degrees*PI.toFloat()/180f
        val h=shoulder+down*cos(angle)-depth*sin(angle)
        val z=down*sin(angle)+depth*cos(angle)
        val x=(if(authored) .415f else .40f)*side
        val size=p.appearance.look.height*1.25f
        return V3(p.x+(x*cos(p.facing)+z*sin(p.facing))*size,h*size,
            p.z+(-x*sin(p.facing)+z*cos(p.facing))*size)
    }
    val smallCupOnBoard=V3(BergKitchen.TRAY_X-.53f,BergKitchen.TRAY_TOP,BergKitchen.TRAY_Z+.06f)
    fun smallCup(f:BergFrame):V3 {
        if(f.rumle.gesture!=RumleGesture.TOAST) return smallCupOnBoard
        val p=guest(f) ?: return smallCupOnBoard
        val t=f.rumle.elapsed
        val lift=if(f.motion) ease(.58f,1.12f,t)*(1f-ease(2.22f,2.88f,t)) else if(t in .60f..2.75f) 1f else 0f
        val target=hand(f,p)+V3(0f,-.14f,0f)
        return smallCupOnBoard+(target-smallCupOnBoard)*lift
    }
}

/** The palm, fingers and serving share one grip point. Solve the two bones toward that point. */
internal class BergRumle(art: BergAssets, private val face: Int, private val part: Int) {
    private fun load(art: BergAssets, name: String) = art.mesh(name).also { it.upload() }
    private val body = load(art,"troll")
    private val whistle = load(art,"rumle-whistle")
    private val eyes = load(art,"rumle-eyes")
    private val pupils = load(art,"rumle-gaze")
    private val upper = load(art,"arm")
    private val lower = load(art,"rumle-forearm")
    private val open = load(art,"rumle-hand-open")
    private val closed = load(art,"rumle-hand-grip")
    private val m = FloatArray(16)
    private val shoulder = V3(1.46f,3.01f,.31f)
    private val relaxed = V3(1.86f,1.50f,1.07f)
    private val tray = V3(BergKitchen.TRAY_X+3f, BergKitchen.TRAY_TOP, BergKitchen.TRAY_Z+5f)
    private val pendant = V3(.02f,2.39f,1.14f)
    private val ear = V3(1.52f,4.36f,.51f)
    private val home = RumleAtHome()
    private val gaze = RumleGaze()
    private val pendantCord = BergMeshBuilder().apply {
        beam(V3(0f,0f,0f),V3(0f,1f,0f),.021f,0x805641,7)
    }.build().also { it.upload() }

    private fun ease(a: Float, b: Float, t: Float): Float {
        val u=((t-a)/(b-a)).coerceIn(0f,1f)
        return u*u*(3f-2f*u)
    }
    private fun mix(a: V3,b: V3,t: Float) = a+(b-a)*t
    private fun length(v: V3) = sqrt(v.dot(v))
    private fun turnY(v: V3, angle: Float): V3 = V3(
        v.x*cos(angle)+v.z*sin(angle), v.y, -v.x*sin(angle)+v.z*cos(angle),
    )

    /** Use the same neck pivot and rotation order as the head skinning in BergRenderer. */
    private fun earPosition(): V3 {
        val height = ear.y-3.55f
        val pitch = gaze.pitch
        val afterPitch = V3(ear.x, height*cos(pitch)-ear.z*sin(pitch), height*sin(pitch)+ear.z*cos(pitch))
        return turnY(afterPitch,gaze.yaw)+V3(0f,3.55f,0f)
    }

    fun draw(f: BergFrame, items: Map<BergItem,BergMesh>, submit: (BergMesh,FloatArray)->Unit) {
        val x = if (f.cave) -3f else 13f
        val z = if (f.cave) -5f else -15f
        val y = Highlands.floor(x,z,f.cave)
        fun at(p: V3) {
            Matrix.setIdentityM(m,0)
            Matrix.translateM(m,0,x+p.x,y+p.y,z+p.z)
        }
        val moment=f.rumle
        val kind=moment.gesture
        val serving=kind==RumleGesture.DRINK || kind==RumleGesture.EAT || kind==RumleGesture.TOAST
        val t=moment.elapsed
        val song=f.echo
        val playing=song.replyLength>0f && !serving
        val busyAge = when {
            playing -> song.replyAge
            kind != RumleGesture.REST -> t
            else -> null
        }
        val mouseCalling = f.caveMouseAge in 0f..5.6f
        val quiet = home.sample(f.time, f.cave && f.motion, busyAge, song.active || mouseCalling)
        val music=if(playing && f.motion) ease(.06f,.86f,song.replyAge)*(1f-ease(song.replyLength-.74f,song.replyLength,song.replyAge)) else if(playing) 1f else 0f
        val sipTime=if(kind==RumleGesture.TOAST) 2.05f else 1.62f
        val up=ease(.72f,sipTime,t)*(1f-ease(sipTime+.48f,sipTime+1.10f,t))
        val burp=if(kind==RumleGesture.DRINK) ease(3.10f,3.23f,t)*(1f-ease(3.23f,3.60f,t)) else 0f
        val chew=if(kind==RumleGesture.EAT && t in 1.6f..2.60f) .5f+.5f*sin((t-1.6f)*17f) else 0f
        val guest = f.walkers.firstOrNull()
        val guestDistance = hypot(f.x-x,f.z-z)
        val near = if (guest == null) 0f else (1f-ease(7f,17f,guestDistance))*ease(-1.5f,1.8f,f.z-z)
        val watch = if(serving) ease(0f,.45f,t)*(1f-ease(2.8f,3.4f,t)) else 0f
        val guestYaw = atan2(f.x-x,max(.4f,f.z-z)).coerceIn(-.55f,.55f)
        val guestEyes = Highlands.floor(f.x,f.z,f.cave)-y+(guest?.appearance?.look?.height ?: 1f)*1.99f
        val guestPitch = atan2(4.55f-guestEyes, max(1f,guestDistance)).coerceIn(-.25f,.36f)
        var attentionYaw = guestYaw*near
        var attentionPitch = guestPitch*near
        val lookAtBird = quiet.watchBird * (if(f.moving) 1f-.76f*near else 1f)
        attentionYaw += (.16f+.10f*quiet.turn-attentionYaw)*lookAtBird
        attentionPitch += (.48f-attentionPitch)*lookAtBird
        val listening = if (!f.motion && f.cave && busyAge == null && (song.active || mouseCalling)) 1f else quiet.listen
        if (listening > 0f) {
            val listenYaw = when {
                mouseCalling -> {
                    val mouse = CaveMouse.pose(f.time,f.caveMouseAge,f.motion)
                    atan2(mouse.x-x,mouse.z-z).coerceIn(-.61f,.61f)
                }
                song.active -> atan2(BergEcho.inside.x-x,BergEcho.inside.z-z).coerceIn(-.60f,.60f)
                else -> -.46f // the fire and the little animals at the left of the rug
            }
            val listenPitch = if (mouseCalling) .38f else .20f
            attentionYaw += (listenYaw-attentionYaw)*listening
            attentionPitch += (listenPitch-attentionPitch)*listening
        }
        val nod=if(f.motion && kind==RumleGesture.WAVE) .055f*sin(t*4f)*sin(t/kind.seconds*PI.toFloat()) else 0f
        val targetYaw = attentionYaw*(1f-watch)+.13f*watch
        val targetPitch = if(f.motion) attentionPitch*(1f-watch)+.22f*watch-.34f*up+nod-.09f*burp-.15f*music else attentionPitch
        gaze.follow(f.time,f.motion,targetYaw,targetPitch)
        val phase=(f.time+.95f)%4.73f
        val blink=if(f.motion) (1f-abs(phase-.13f)/.105f).coerceIn(0f,1f) else 0f
        glUniform4f(face,gaze.yaw,gaze.pitch,if(f.motion) .045f*up+.090f*chew+.18f*burp else 0f,blink)
        at(V3(0f,0f,0f))
        val breath=if(f.motion) sin(f.time*1.46f)*.004f else 0f
        Matrix.scaleM(m,0,1f+breath*.35f,1f+breath,1f)
        glUniform1f(part,1f);submit(body,m)
        glUniform1f(part,2f);submit(eyes,m)
        Matrix.translateM(m,0,gaze.eyeX*(1f-blink),gaze.eyeY*(1f-blink),0f)
        glUniform1f(part,3f);submit(pupils,m)
        glUniform1f(part,0f)

        val magnitude=if(kind==RumleGesture.TOAST) 1.55f else if(kind==RumleGesture.EAT) .86f else 1.35f
        val gripHeight=when(kind) {
            RumleGesture.TOAST -> .17f
            RumleGesture.EAT -> .20f
            else -> .27f
        }*magnitude
        val pickup=tray+V3(0f,gripHeight,0f)
        val leftOnBoard=pickup+V3(if(kind==RumleGesture.DRINK && f.sharedJuice) .09f else .43f,0f,-.17f)
        val wave=if(kind==RumleGesture.WAVE && !playing) {
            if(f.motion) ease(0f,.58f,t)*(1f-ease(1.86f,2.55f,t)) else .82f
        } else 0f
        var whistleAt = mix(pendant,V3(.48f,3.22f,1.78f),quiet.lift)
        val birdGrip = V3(.17f,-.12f,.045f)
        val earGrip = if (quiet.cupEar > 0f) mix(relaxed,earPosition(),quiet.cupEar) else relaxed
        val idleGrip = mix(earGrip,whistleAt+birdGrip,quiet.touch)
        val earRoll = -143f+quiet.cupEar*154f
        val idleRoll = earRoll+(-7f+16f*quiet.turn-earRoll)*quiet.touch
        var grip=mix(idleGrip,V3(2.20f,4.00f,1.03f),wave)
        var roll=idleRoll+wave*(-6f+(if(f.motion) sin(t*10f)*16f else 0f)-idleRoll)
        val holdEnd=if(kind==RumleGesture.EAT) 2.32f else sipTime+1.14f
        val reach=ease(0f,.63f,t)
        val release=ease(holdEnd,holdEnd+.48f,t)
        val tilt=if(kind==RumleGesture.DRINK) -64f*up else if(kind==RumleGesture.TOAST) -42f*up else -12f*up
        val mouth=when(kind) {
            RumleGesture.TOAST -> V3(.17f,3.62f,1.70f)
            RumleGesture.EAT -> V3(.16f,3.67f,1.65f)
            else -> V3(.13f,3.54f,1.88f)
        }
        val pickupTime=.64f
        if(serving) {
            if(f.motion) {
                val salute=if(kind==RumleGesture.TOAST) ease(.7f,1.13f,t)*(1f-ease(1.35f,2.05f,t)) else 0f
                val returning=ease(sipTime+.48f,holdEnd,t)
                val from=mix(pickup,leftOnBoard,returning)
                val toasted=if(salute>0f) {
                    val guestCup=BergServing.smallCup(f)
                    // Radii 0.23 m, scaled by 1.10 and 1.55. The two lips meet at the clink.
                    V3(guestCup.x-x,guestCup.y+.36f*(1.10f-1.55f)+gripHeight,guestCup.z-z-.60f)
                } else from
                val to=mix(mix(from,toasted,salute),mouth,up)
                grip=mix(mix(idleGrip,to,reach),relaxed,release)
                roll=idleRoll*(1f-reach*(1f-release))
            } else {
                grip=if(t<holdEnd) mouth else relaxed
                roll=if(t<holdEnd) 0f else -143f
            }
        }
        if(playing) {
            val touch=if(f.motion) ease(0f,.31f,song.replyAge)*(1f-ease(song.replyLength-.27f,song.replyLength,song.replyAge)) else 1f
            val lift=if(f.motion) ease(.31f,.86f,song.replyAge)*(1f-ease(song.replyLength-.74f,song.replyLength-.25f,song.replyAge)) else 1f
            whistleAt=mix(whistleAt,V3(-.30f,3.57f,1.65f),lift)
            grip=mix(idleGrip,whistleAt+V3(.09f,-.085f,.09f),touch)
            roll=idleRoll+touch*(-2f+(if(f.motion) sin(song.replyAge*8f)*song.glow*4f else 0f)-idleRoll)
        }
        at(whistleAt)
        Matrix.rotateM(m,0,quiet.turn*25f,0f,1f,0f)
        Matrix.rotateM(m,0,quiet.turn*12f-8f*music,0f,0f,1f)
        submit(whistle,m)
        val rad=roll*PI.toFloat()/180f
        val handYaw = gaze.yaw*quiet.cupEar*(1f-wave)
        val wrist=grip+turnY(V3(.05f*cos(rad)+.29f*sin(rad),.05f*sin(rad)-.29f*cos(rad),-.16f),handYaw)
        val axis=(wrist-shoulder).unit()
        val d=length(wrist-shoulder).coerceIn(.30f,2.78f)
        val along=(1.30f*1.30f-1.50f*1.50f+d*d)/(2f*d)
        val height=sqrt(max(0f,1.30f*1.30f-along*along))
        val pole=V3(1.25f,-.60f,-.45f)
        val elbow=shoulder+axis*along+(pole-axis*axis.dot(pole)).unit()*height
        fun bone(mesh: BergMesh, a: V3, b: V3) {
            val u=b-a
            val side=u.cross(V3(0f,0f,1f)).unit()
            val front=side.cross(u).unit()
            at(a)
            m[0]=side.x;m[1]=side.y;m[2]=side.z
            m[4]=u.x;m[5]=u.y;m[6]=u.z
            m[8]=front.x;m[9]=front.y;m[10]=front.z
            submit(mesh,m)
        }
        // The knot at his vest anchors the short loose lanyard as the whistle leaves his chest.
        val separation = length(whistleAt-pendant)
        if (separation > .08f) {
            val knot = pendant+V3(0f,.08f,-.08f)
            val end = whistleAt+V3(.14f,.065f,-.10f)
            val middle = mix(knot,end,.48f)+V3(.035f,-min(.19f,separation*.22f),.045f)
            bone(pendantCord,knot,middle)
            bone(pendantCord,middle,end)
        }
        bone(upper,shoulder,elbow);bone(lower,elbow,wrist)
        val grasp=serving && (if(f.motion) t in pickupTime..holdEnd else t<holdEnd)
        at(grip)
        if (handYaw != 0f) Matrix.rotateM(m,0,handYaw*180f/PI.toFloat(),0f,1f,0f)
        Matrix.rotateM(m,0,roll,0f,0f,1f)
        submit(if(grasp || playing && music>.20f || quiet.touch>.91f) closed else open,m)

        if(!serving) return
        val eaten=kind==RumleGesture.EAT && t>sipTime+.40f
        if(eaten || (kind!=RumleGesture.EAT && t>=holdEnd)) return
        val anchor=if(!f.motion || t>=pickupTime) grip else {
            // The guest offers from their right hand, then leaves the serving on the tray.
            val guest=BergServing.guest(f)
            if(guest==null) pickup else {
                val hand=BergServing.hand(f,guest)-V3(x,y,z)
                mix(hand,pickup,ease(.16f,.46f,t))
            }
        }
        at(anchor)
        Matrix.rotateM(m,0,if(f.motion) tilt else -35f,1f,0f,0f)
        val bite=if(kind==RumleGesture.EAT && f.motion) 1f-.68f*ease(sipTime,sipTime+.36f,t) else 1f
        Matrix.scaleM(m,0,magnitude*bite,magnitude*bite,magnitude*bite)
        Matrix.translateM(m,0,0f,-gripHeight/magnitude,0f)
        submit(items.getValue(moment.item),m)
    }
}
