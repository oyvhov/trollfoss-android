package app.trollfoss.ui.berg

import app.trollfoss.domain.BergEcho
import app.trollfoss.domain.BergSpring
import app.trollfoss.domain.CaveMouse
import app.trollfoss.domain.Highlands
import app.trollfoss.domain.Palette
import kotlin.math.*

/** A folded neckerchief fitted over the traveller's own shirt. Its loose end is free to move. */
internal fun trailScarf(person:BergAppearance,authored:Boolean):BergMesh = BergMeshBuilder().apply {
    val look=person.look.safe()
    val cloth=when(person.name) {
        "Eira" -> 0xA34F54
        "Eilev" -> 0xB57843
        "Øyvind" -> 0x3B7880
        else -> Palette.cloth[look.accent]
    }
    val piping=0xF2C78A
    val height=if(authored) 1.24f else 1.105f
    val radius=if(authored) .15f else .25f
    fun fold(i:Int,row:Float):V3 {
        val a=i*2f*PI.toFloat()/36f
        val front=max(0f,cos(a))
        val drop=.095f+.22f*front.pow(4f)+.085f*max(0f,-cos(a)).pow(2)
        val width=radius+row*(if(authored) .21f else .12f)
        val depth=(if(authored) .125f else .20f)+row*.112f+.011f*sin(a*9f)*row
        return V3(sin(a)*width,height-row*drop,cos(a)*depth)
    }
    val rows=listOf(0f,.22f,.53f,.86f,.92f,1f)
    for(row in 0 until rows.lastIndex) for(i in 0 until 36) {
        val a=rows[row];val b=rows[row+1]
        quad(fold(i,a),fold(i+1,a),fold(i+1,b),fold(i,b),if(row==3) piping else cloth)
    }
    // Tied on the left behind the shoulder, with a rolled seam and gold thread on the free hem.
    val knot=V3(-.18f,height-.067f,-.133f)
    sphere(knot.x,knot.y,knot.z,.081f,.062f,.086f,cloth,12,8)
    fun tail(t:Float,side:Float):V3 {
        val width=(.057f+.036f*sin(PI.toFloat()*t))*(1f-.30f*t)
        return V3(knot.x-.095f*t+width*side,knot.y-.47f*t+side*.03f*t,
            knot.z-.21f*sin(t*1.56f)-side*.025f+sin(t*6f)*.026f)
    }
    for(i in 0 until 17) {
        val a=i/17f;val b=(i+1)/17f
        quad(tail(a,-1f),tail(a,1f),tail(b,1f),tail(b,-1f),if(i==14 || i==11) piping else cloth)
    }
    // A small brass sun pin sits on the scarf at the chest.
    val pin=V3(-.105f,height-.133f,if(authored) .196f else .259f)
    sphere(pin.x,pin.y,pin.z,.039f,.039f,.014f,0xD09B53,14,6)
    sphere(pin.x,pin.y,pin.z+.012f,.020f,.020f,.007f,0x84A89A,10,6)
}.build()

/** Turn the face independently of the shoulders when there is something to look at. */
internal fun guestGaze(frame:BergFrame,person:BergWalker,leader:Boolean):Pair<Float,Float> {
    val reach=if(leader) frame.friendReach else 0f
    if(reach>.01f) return Pair(-.39f*reach,-.23f*reach)
    val echo=frame.echo
    if(leader && echo.active) {
        val light=echo.guide
        val stone=BergEcho.stone(frame.cave,echo.tone.coerceIn(0,2))
        val x=(light?.x ?: stone.x)-person.x
        val z=(light?.z ?: stone.z)-person.z
        val yaw=atan2(x,z)-person.facing
        val pitch=if(light!=null) atan2(light.y-Highlands.floor(person.x,person.z,frame.cave)-1.6f,
            hypot(x,z).coerceAtLeast(.5f)).coerceIn(-.26f,.32f) else if(echo.replyLength>0f) .19f else -.19f
        return Pair(atan2(sin(yaw),cos(yaw)).coerceIn(-.44f,.44f),pitch)
    }
    if(leader && frame.cave && !frame.moving) {
        val target=when {
            frame.caveMouseAge>=0f && CaveMouse.canCall(person.x,person.z) -> {
                val mouse=CaveMouse.pose(frame.time,frame.caveMouseAge,frame.motion)
                Pair(mouse.x,mouse.z)
            }
            frame.springAge>=0f && BergSpring.inReach(person.x,person.z) -> {
                val cap=BergSpring.caps[frame.springKey]
                Pair(cap.x,cap.z)
            }
            else -> null
        }
        if(target!=null) {
            val yaw=atan2(target.first-person.x,target.second-person.z)-person.facing
            return Pair(atan2(sin(yaw),cos(yaw)).coerceIn(-.5f,.5f),-.35f)
        }
    }
    val x=(if(frame.cave) -3f else 13f)-person.x
    val z=(if(frame.cave) -5f else -15f)-person.z
    val distance=hypot(x,z)
    val yaw=atan2(x,z)-person.facing
    val attention=(1f-(distance-3f)/4.5f).coerceIn(0f,1f)*max(0f,cos(yaw))
    return Pair(atan2(sin(yaw),cos(yaw)).coerceIn(-.44f,.44f)*attention,.30f*attention)
}
