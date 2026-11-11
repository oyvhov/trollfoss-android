package app.trollfoss.ui.berg

import android.opengl.Matrix
import app.trollfoss.domain.CaveMouse
import app.trollfoss.domain.CaveMousePose
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/** A little forager lives between the hearth and the cake table. Coordinates are in cave metres. */
internal class BergCaveLife {
    companion object {
        private const val SIZE = 1.23f
        private const val DEGREES = 57.29578f

        fun mouseTarget(frame: BergFrame): V3 {
            val p = CaveMouse.pose(frame.time, frame.caveMouseAge, frame.motion)
            return V3(p.x, .27f + p.offering * .16f, p.z)
        }
    }

    private fun model(light: Float = 0f, make: BergMeshBuilder.() -> Unit) =
        BergMeshBuilder().apply(make).build(light).also { it.upload() }

    /** Continuous cross sections taper from haunch to neck and from brow to nose. */
    private data class Ring(val z: Float, val xRadius: Float, val yRadius: Float, val y: Float)

    private fun fur(rings: List<Ring>, back: Int, belly: Int): BergMesh {
        val sides = 16
        val data = FloatArray((rings.size - 1) * sides * 54)
        var cursor = 0
        fun vertex(j: Int, i: Int) {
            val ring = rings[j]
            val first = rings[(j - 1).coerceAtLeast(0)]
            val last = rings[(j + 1).coerceAtMost(rings.lastIndex)]
            val angle = i * 2f * PI.toFloat() / sides
            val cx = cos(angle)
            val sy = sin(angle)
            val around = V3(-ring.xRadius * sy, ring.yRadius * cx, 0f)
            val along = V3((last.xRadius - first.xRadius) * cx,
                last.y - first.y + (last.yRadius - first.yRadius) * sy, last.z - first.z)
            val n = around.cross(along).unit()
            val mix = ((.27f - sy) * .85f).coerceIn(0f, 1f)
            data[cursor++] = ring.xRadius * cx
            data[cursor++] = ring.y + ring.yRadius * sy
            data[cursor++] = ring.z
            data[cursor++] = n.x
            data[cursor++] = n.y
            data[cursor++] = n.z
            for (shift in 16 downTo 0 step 8) {
                val a = back shr shift and 255
                val b = belly shr shift and 255
                data[cursor++] = (a + (b - a) * mix) / 255f
            }
        }
        for (j in 0 until rings.lastIndex) for (i in 0 until sides) {
            vertex(j, i); vertex(j, i + 1); vertex(j + 1, i + 1)
            vertex(j, i); vertex(j + 1, i + 1); vertex(j + 1, i)
        }
        return BergMesh(data).also { it.upload() }
    }

    private val body = fur(listOf(
        Ring(-.375f, .005f, .006f, .172f), Ring(-.34f, .096f, .105f, .183f),
        Ring(-.265f, .166f, .166f, .205f), Ring(-.14f, .193f, .193f, .226f),
        Ring(.015f, .183f, .184f, .234f), Ring(.145f, .145f, .136f, .246f),
        Ring(.228f, .064f, .066f, .248f), Ring(.246f, .005f, .006f, .251f),
    ), 0x996B58, 0xE5CDA2)
    private val head = fur(listOf(
        Ring(-.12f, .01f, .015f, .015f), Ring(-.085f, .113f, .117f, .060f),
        Ring(-.018f, .149f, .146f, .071f), Ring(.068f, .151f, .140f, .064f),
        Ring(.147f, .130f, .111f, .052f), Ring(.218f, .097f, .082f, .031f),
        Ring(.278f, .058f, .051f, .018f), Ring(.311f, .014f, .014f, .019f),
    ), 0xA67962, 0xF0D9B2)
    private val ear = model {
        sphere(0f, .015f, 0f, .092f, .105f, .036f, 0x936453, 14, 8)
        sphere(0f, .016f, .023f, .066f, .080f, .021f, 0xD9A799, 12, 7)
    }
    private val muzzle = model {
        sphere(0f, .030f, .305f, .042f, .029f, .030f, 0xB57D77, 12, 7)
        for (side in listOf(-1f, 1f)) {
            for (i in 0..2) {
                val y = -.004f + i * .019f
                val root = V3(side * .052f, y, .267f)
                val middle = V3(side * .133f, y + .015f, .292f - i * .010f)
                val tip = V3(side * (.208f + i * .009f), y + .012f, .32f - i * .029f)
                beam(root, middle, .0045f, 0xDDD1BC, 5)
                beam(middle, tip, .003f, 0xCCC3B0, 5)
            }
        }
    }
    private val eyes = model {
        for (side in listOf(-1f, 1f)) {
            sphere(side * .109f, 0f, .160f, .041f, .044f, .045f, 0x362B32, 12, 8)
            sphere(side * .129f - .005f, .015f, .191f, .012f, .013f, .009f, 0xFAEDD4, 8, 5)
        }
    }
    private val forePaw = model {
        beam(V3(0f, 0f, 0f), V3(0f, -.091f, .025f), .028f, 0xC59B7C, 8)
        sphere(0f, -.098f, .055f, .030f, .022f, .061f, 0xDDB49A, 10, 6)
    }
    private val hindPaw = model {
        sphere(0f, .015f, -.018f, .079f, .088f, .099f, 0xAC7F66, 12, 7)
        sphere(0f, -.061f, .025f, .049f, .028f, .110f, 0xD1A086, 12, 6)
    }
    private val tail = model {
        fun point(i: Int, j: Int): V3 {
            val t = i / 18f
            val a = j * PI.toFloat() / 4f
            val r = .029f * (1f - t * .94f)
            return V3(.17f * sin(t * 3.6f) + r * cos(a),
                -.128f * sin(t * 1.70f) + r * sin(a), -.67f * t)
        }
        for (i in 0 until 18) for (j in 0 until 8) {
            quad(point(i,j), point(i+1,j), point(i+1,j+1), point(i,j+1),
                if (i < 4) 0xAB826D else 0xC69988)
        }
    }
    private val nut = model {
        sphere(0f, 0f, 0f, .065f, .096f, .067f, 0xE8B76A, 14, 8)
        sphere(0f, .055f, 0f, .074f, .047f, .075f, 0x9C6F48, 14, 6)
        beam(V3(0f,.074f,0f), V3(.032f,.128f,-.011f), .011f, 0x7B684F, 7)
        sphere(.012f, -.011f, .062f, .017f, .023f, .006f, 0xFFF0AE, 8, 5)
    }
    private val spark = model(2f) { gem(0f, 0f, 0f, .072f, 0xFFE29C) }
    private val ember = model(2f) { sphere(0f,0f,0f,.023f,.038f,.019f,0xFFC480,7,4) }
    private val crumbs = model {
        for (i in 0..6) {
            val angle = i * 2.40f
            val r = .11f + .035f * i
            sphere(cos(angle)*r,.057f,sin(angle)*r,.042f,.029f,.058f,
                if (i % 3 == 0) 0xA76D47 else 0xDAB379, 7, 4)
        }
    }
    private val den = model {
        // An old teal wool slipper has a hollow toe, a ribbed cuff and straw in the doorway.
        fun shell(j: Int, angle: Float): V3 {
            val t = j / 10f
            val width = .43f * (1f - .75f * t * t)
            val height = .36f * (1f - .66f * t * t)
            return V3(cos(angle)*width, .035f+sin(angle)*height, .13f-.79f*t)
        }
        for (j in 0 until 10) for (i in 0 until 16) {
            val a = i*PI.toFloat()/16f
            val b = (i+1)*PI.toFloat()/16f
            quad(shell(j,a),shell(j,b),shell(j+1,b),shell(j+1,a),
                if (j == 1 || j == 3) 0x8BACA0 else 0x607F78)
        }
        sphere(0f, .12f, -.62f, .16f, .15f, .12f, 0x586B63, 10, 6)
        for (i in 0 until 16) {
            val a = shell(0,i*PI.toFloat()/16f)
            val b = shell(0,(i+1)*PI.toFloat()/16f)
            beam(a,b,.033f,0xB5B49A,7)
        }
        for (i in 0..8) {
            val x = (i - 4)*.065f
            beam(V3(x,.036f,-.34f),V3(x+.07f*sin(i*2f),.046f,.19f+(i%3)*.045f),
                .014f,if (i%2==0) 0xBD9C69 else 0xDFC397,6)
        }
    }

    private val root = FloatArray(16)
    private val torso = FloatArray(16)
    private val skull = FloatArray(16)
    private val part = FloatArray(16)
    private val grip = FloatArray(4)
    private val cradle = floatArrayOf(0f, .107f, .284f, 1f)

    private fun place(x: Float, y: Float, z: Float) {
        Matrix.setIdentityM(part,0)
        Matrix.translateM(part,0,x,y,z)
    }

    fun draw(frame: BergFrame, submit: (BergMesh, FloatArray) -> Unit) {
        if (!frame.cave) return
        place(CaveMouse.den.x - .23f, .014f, CaveMouse.den.z - .10f)
        Matrix.rotateM(part,0,-17f,0f,1f,0f)
        submit(den,part)
        place(CaveMouse.crumbs.x+.15f, 0f, CaveMouse.crumbs.z-.12f)
        submit(crumbs,part)
        val pose = CaveMouse.pose(frame.time,frame.caveMouseAge,frame.motion)
        drawMouse(frame,pose,submit)
        if (frame.motion && hypot(frame.x+17f,frame.z+5.7f)<24f) {
            // Tiny embers leave the real firebox and fade out below its stone lintel.
            for (i in 0 until 4) {
                val u = (frame.time*(.33f+i*.029f)+i*.277f)%1f
                val size = sin(u*PI.toFloat())
                place(-17f+.24f*sin(i*2.35f)+.13f*sin(u*4f+i),1.04f+u*1.19f,-5.70f+.15f*cos(i*2.1f))
                Matrix.scaleM(part,0,size,size,size)
                submit(ember,part)
            }
        }
    }

    private fun drawMouse(frame:BergFrame, p:CaveMousePose, submit:(BergMesh, FloatArray)->Unit) {
        val t = if (frame.motion) frame.time else 0f
        val gait = sin(t * 22f) * p.running
        val sightline = atan2(frame.x-p.x,frame.z-p.z)-p.heading
        val look = atan2(sin(sightline),cos(sightline)).coerceIn(-1.12f,1.12f)
        val near = (1f - (hypot(frame.x-p.x,frame.z-p.z)-2.2f)/3.8f).coerceIn(0f,1f)
        val interest = max(near * (1f-p.running)*.62f, p.listening)
        val angle = p.heading + look * p.listening * .72f
        Matrix.setIdentityM(root,0)
        Matrix.translateM(root,0,p.x,.028f + abs(gait)*.029f,p.z)
        Matrix.rotateM(root,0,angle*DEGREES,0f,1f,0f)
        Matrix.scaleM(root,0,SIZE,SIZE,SIZE)
        System.arraycopy(root,0,torso,0,16)
        Matrix.translateM(torso,0,0f,.105f,-.205f)
        Matrix.rotateM(torso,0,-49f*p.offering+gait*3f,1f,0f,0f)
        Matrix.translateM(torso,0,0f,-.105f,.205f)
        submit(body,torso)
        System.arraycopy(root,0,part,0,16)
        Matrix.translateM(part,0,0f,.164f,-.32f)
        Matrix.rotateM(part,0,if(frame.motion) sin(t*4.2f)*4f+gait*12f else 0f,0f,1f,0f)
        submit(tail,part)
        for (side in -1..1 step 2) {
            System.arraycopy(root,0,part,0,16)
            Matrix.translateM(part,0,side*.117f,.090f,-.186f)
            Matrix.rotateM(part,0,side*gait*29f,1f,0f,0f)
            submit(hindPaw,part)
            System.arraycopy(torso,0,part,0,16)
            Matrix.translateM(part,0,side*(.102f-.045f*p.offering),.112f+.048f*p.offering,.13f+.05f*p.offering)
            Matrix.rotateM(part,0,-side*gait*31f-35f*p.offering,1f,0f,0f)
            submit(forePaw,part)
        }
        System.arraycopy(torso,0,skull,0,16)
        Matrix.translateM(skull,0,0f,.256f,.176f)
        Matrix.rotateM(skull,0,look*interest*(1f-.72f*p.listening)*DEGREES,0f,1f,0f)
        val sniff = if(frame.motion) sin(t*8.6f)*2.9f*(1f-p.running) else 0f
        Matrix.rotateM(skull,0,sniff+22f*p.offering+4f*(1f-interest),1f,0f,0f)
        submit(head,skull)
        submit(muzzle,skull)
        for (side in -1..1 step 2) {
            System.arraycopy(skull,0,part,0,16)
            Matrix.translateM(part,0,side*.119f,.163f,-.028f)
            val flick = if(frame.motion) max(0f,sin(t*2.2f+side)).let{it*it*it*7f} else 0f
            Matrix.rotateM(part,0,side*(-20f+flick+6f*interest),0f,0f,1f)
            submit(ear,part)
        }
        System.arraycopy(skull,0,part,0,16)
        Matrix.translateM(part,0,0f,.090f,0f)
        val blink = if (frame.motion) (1f-abs((t+.71f)%4.8f-.11f)/.11f).coerceIn(0f,1f) else 0f
        Matrix.scaleM(part,0,1f,1f-.88f*blink,1f)
        submit(eyes,part)

        // Lift the nut from the same place she puts it down, into the gap between her two paws.
        Matrix.multiplyMV(grip,0,torso,0,cradle,0)
        val acornX = CaveMouse.meeting.x
        val acornZ = CaveMouse.meeting.z+.333f
        val lift = p.offering
        val x = acornX + (grip[0]-acornX)*lift
        val y = .105f + (grip[1]-.105f)*lift
        val z = acornZ + (grip[2]-acornZ)*lift
        place(x,y,z)
        Matrix.rotateM(part,0,18f*(1f-lift),0f,0f,1f)
        submit(nut,part)
        if (lift>.02f) for (i in 0..2) {
            val u = if(frame.motion) (t*.68f+i*.34f)%1f else .38f+i*.21f
            val a = i*2.4f+u*.9f
            place(x+cos(a)*(.08f+u*.17f),y+.10f+u*.29f,z+sin(a)*(.08f+u*.10f))
            val s = .74f*sin(u*PI.toFloat())*lift
            Matrix.scaleM(part,0,s,s,s)
            submit(spark,part)
        }
    }
}
