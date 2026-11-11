package app.trollfoss.ui.berg

import android.opengl.Matrix
import app.trollfoss.domain.*
import kotlin.math.*

/** Station surfaces, liquids and carried objects use the same authored heights. */
internal class BergBerryStations(art: BergAssets) {
    val cup = art.mesh("berry-cup").also { it.upload() }
    private val emptyCup = art.mesh("berry-cup-empty").also { it.upload() }
    val basket = art.mesh("berry-basket").also { it.upload() }
    private val bush = art.mesh("berry-bush").also { it.upload() }
    private val fruit = art.mesh("berry-fruit").also { it.upload() }
    private val table = BergMeshBuilder().apply {
        box(0f,.92f,0f,1.55f,.12f,.82f,0xB88D65)
        for (x in listOf(-.58f,.58f)) for(z in listOf(-.26f,.26f)) {
            beam(V3(x*.90f,.93f,z*.85f),V3(x,.03f,z),.068f,0x93694A,10)
        }
        for(i in -2..2) box(0f,1.041f,i*.12f,1.32f,.004f,.009f,0xAC835F)
    }.build().also {it.upload()}
    private val saucepan = BergMeshBuilder().apply {
        // Open copper pan with a dark lining and a rolled rim.
        val n=32
        for(i in 0 until n) {
            val a=i*2f*PI.toFloat()/n;val b=(i+1)*2f*PI.toFloat()/n
            fun p(v:Float,h:Float,r:Float)=V3(cos(v)*r,h,sin(v)*r)
            quad(p(a,0f,.32f),p(a,.33f,.48f),p(b,.33f,.48f),p(b,0f,.32f),0xA65E40)
            quad(p(a,.33f,.48f),p(a,.34f,.43f),p(b,.34f,.43f),p(b,.33f,.48f),0xE6B685)
            quad(p(a,.34f,.43f),p(a,.06f,.29f),p(b,.06f,.29f),p(b,.34f,.43f),0x654B48)
        }
        beam(V3(.39f,.25f,0f),V3(.91f,.32f,.02f),.068f,0x77513E,10)
    }.build().also {it.upload()}
    private val liquid=BergMeshBuilder().apply {sphere(0f,0f,0f,.418f,.028f,.418f,0x803F69,24,6)}.build().also {it.upload()}
    private val spoon=BergMeshBuilder().apply {
        sphere(0f,0f,0f,.125f,.045f,.16f,0xD4AA79,14,7)
        beam(V3(.04f,.01f,-.08f),V3(.16f,.65f,-.27f),.035f,0xBF915E,10)
    }.build().also {it.upload()}
    private val steam=BergMeshBuilder().apply {
        for(i in 0..5) {
            val h=i*.06f
            beam(V3(sin(i*.82f)*.05f,h,0f),V3(sin((i+1)*.82f)*.05f,h+.06f,0f),.014f-i*.0012f,0xD7BC9E,6)
        }
    }.build().also {it.upload()}
    private val stream=BergMeshBuilder().apply {
        beam(V3(0f,0f,0f),V3(0f,1f,0f),.027f,0x984F75,8)
    }.build().also {it.upload()}
    private val m=FloatArray(16)

    fun draw(f:BergFrame,items:Map<BergItem,BergMesh>,submit:(BergMesh,FloatArray)->Unit) {
        fun drawAt(mesh:BergMesh,x:Float,y:Float,z:Float,scale:Float=1f,angle:Float=0f) {
            Matrix.setIdentityM(m,0);Matrix.translateM(m,0,x,y,z)
            if(angle!=0f) Matrix.rotateM(m,0,angle,0f,1f,0f)
            if(scale!=1f) Matrix.scaleM(m,0,scale,scale,scale)
            submit(mesh,m)
        }
        if(!f.cave) {
            val p=BergKitchen.patch
            for(i in 0..2) {
                val x=p.x+i*.71f;val z=p.z-i*.26f
                val y=Highlands.floor(x,z,false)
                drawAt(bush,x,y,z,1.45f-i*.16f,i*78f)
                if(!f.pickedBerries) drawAt(fruit,x,y,z,1.45f-i*.16f,i*78f)
            }
            return
        }
        val tx=BergKitchen.TRAY_X;val tz=BergKitchen.TRAY_Z;val top=BergKitchen.TRAY_TOP
        drawAt(table,tx,0f,tz)
        val moment=f.rumle
        val toasting=moment.gesture==RumleGesture.TOAST
        val soda=moment.gesture==RumleGesture.DRINK
        if(f.sharedJuice) {
            val guestCup=BergServing.smallCup(f)
            drawAt(cup,guestCup.x,guestCup.y,guestCup.z,1.10f,170f)
            if(!toasting || moment.elapsed>=3.19f) drawAt(emptyCup,tx+.43f,top,tz-.17f,1.55f)
        }
        if(f.lastBottle!=BergItem.NONE && (!soda || moment.elapsed>=2.76f)) {
            // A finished bottle stands at the rear of the board once tea has also been served.
            drawAt(items.getValue(f.lastBottle),tx+if(f.sharedJuice) .09f else .43f,top,tz-.17f,1.35f)
        }
        val x=-16.48f;val z=-5.34f;val y=.43f
        drawAt(saucepan,x,y,z)
        val active=f.kitchenPlay>0f
        val pour=active && f.kitchenEvent==BergEvent.POUR
        val stir=active && f.kitchenEvent==BergEvent.STIR
        if(f.pot!=BerryPot.EMPTY || pour) {
            drawAt(liquid,x,y+.274f,z)
            if(f.pot==BerryPot.BERRIES) drawAt(fruit,x-.08f,y+.23f,z,.25f)
            if(f.pot==BerryPot.STEEPED || pour) for(i in 0..2) {
                val drift=if(f.motion) (f.time*.65f+i*.32f)%1f else .3f
                drawAt(steam,x-.13f+i*.13f,y+.37f+drift*.22f,z, .68f+.38f*drift,i*61f)
            }
        }
        val t=1.5f-f.kitchenPlay
        val spin=if(stir && f.motion) t*5f else 0f
        val lean=if(pour && f.motion) sin(t*PI.toFloat()/1.5f).coerceAtLeast(0f) else 0f
        val sx=x+.18f*cos(spin)+.47f*lean
        val sz=z+.18f*sin(spin)+.48f*lean
        drawAt(spoon,sx,y+.27f+.55f*lean,sz,angle=spin*180f/PI.toFloat())
        if(f.pot==BerryPot.STEEPED || pour) {
            drawAt(emptyCup,x+.64f,y-.01f,z+.52f,1.12f,25f)
            if(pour) drawAt(liquid,x+.64f,y+.09f+.22f*(t/1.18f).coerceIn(0f,1f),z+.52f,.53f)
            if(pour && lean>.84f) {
                Matrix.setIdentityM(m,0);Matrix.translateM(m,0,sx,y+.34f,sz)
                Matrix.scaleM(m,0,1f,.55f*lean-.07f,1f);submit(stream,m)
            }
        }
    }
}
