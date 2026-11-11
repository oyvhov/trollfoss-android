package app.trollfoss.ui.berg

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/** Meshes are generated once, uploaded once per EGL context, and kept out of the Compose draw loop. */
internal class BergMesh private constructor(
    private var staging:ByteBuffer?,private val vertexCount:Int,val lighting:Float,private val packed:Boolean,
) {
    constructor(data:FloatArray,lighting:Float=0f):this(
        ByteBuffer.allocateDirect(data.size*4).order(ByteOrder.nativeOrder()).apply {asFloatBuffer().put(data)},
        data.size/9,lighting,false,
    )
    constructor(data:ByteBuffer,vertexCount:Int,lighting:Float):this(data,vertexCount,lighting,true) {
        require(data.isDirect && data.remaining()==vertexCount*20)
    }
    private val stride=if(packed) 20 else 36
    val gpuBytes get()=vertexCount*stride
    private var buffer = 0
    val triangles get() = vertexCount / 3
    fun upload() {
        val memory = checkNotNull(staging)
        val names = IntArray(1); glGenBuffers(1, names, 0); buffer = names[0]
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        memory.position(0)
        glBufferData(GL_ARRAY_BUFFER, gpuBytes, memory, GL_STATIC_DRAW)
        staging = null // Context recreation regenerates geometry; do not retain a second CPU copy.
    }
    fun draw(position: Int, normal: Int, color: Int) {
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        glVertexAttribPointer(position, 3, GL_FLOAT, false, stride, 0)
        glVertexAttribPointer(normal, 3, if(packed) GL_BYTE else GL_FLOAT, packed, stride, 12)
        glVertexAttribPointer(color, 3, if(packed) GL_UNSIGNED_BYTE else GL_FLOAT, packed, stride, if(packed) 16 else 24)
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
    }
    fun release() { if (buffer != 0) glDeleteBuffers(1, intArrayOf(buffer), 0); buffer = 0 }
}

internal data class V3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(v: V3) = V3(x+v.x,y+v.y,z+v.z)
    operator fun times(scale: Float) = V3(x*scale,y*scale,z*scale)
    fun dot(v: V3) = x*v.x+y*v.y+z*v.z
    operator fun minus(v: V3) = V3(x - v.x, y - v.y, z - v.z)
    fun cross(v: V3) = V3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
    fun unit(): V3 { val n = sqrt(x*x+y*y+z*z).coerceAtLeast(.0001f); return V3(x/n,y/n,z/n) }
}

internal class BergMeshBuilder {
    private var vertices = FloatArray(65536)
    private var used = 0
    private fun add(value:Float) {
        if(used==vertices.size) vertices=vertices.copyOf(vertices.size*2)
        vertices[used++]=value
    }
    private fun vertex(p: V3, n: V3, color: Int) {
        add(p.x); add(p.y); add(p.z)
        add(n.x); add(n.y); add(n.z)
        add((color shr 16 and 255) / 255f); add((color shr 8 and 255) / 255f); add((color and 255) / 255f)
    }
    fun triangle(a: V3, b: V3, c: V3, color: Int) {
        val normal = (b-a).cross(c-a).unit()
        vertex(a,normal,color); vertex(b,normal,color); vertex(c,normal,color)
    }
    fun quad(a: V3, b: V3, c: V3, d: V3, color: Int) { triangle(a,b,c,color); triangle(a,c,d,color) }
    fun ground(a:V3,b:V3,c:V3,d:V3,color:(V3)->Int) {
        val n1=(b-a).cross(c-a).unit();val n2=(c-a).cross(d-a).unit()
        vertex(a,n1,color(a));vertex(b,n1,color(b));vertex(c,n1,color(c))
        vertex(a,n2,color(a));vertex(c,n2,color(c));vertex(d,n2,color(d))
    }
    fun box(x: Float, y: Float, z: Float, w: Float, h: Float, d: Float, color: Int) {
        val a=V3(x-w/2,y,z-d/2); val b=V3(x+w/2,y,z-d/2)
        val c=V3(x+w/2,y,z+d/2); val e=V3(x-w/2,y,z+d/2)
        val aa=a.copy(y=y+h);val bb=b.copy(y=y+h);val cc=c.copy(y=y+h);val ee=e.copy(y=y+h)
        quad(aa,ee,cc,bb,color);quad(e,c,cc,ee,color);quad(b,a,aa,bb,color)
        quad(a,e,ee,aa,color);quad(c,b,bb,cc,color);quad(a,b,c,e,color)
    }
    fun sphere(x: Float, y: Float, z: Float, rx: Float, ry: Float, rz: Float, color: Int, sides: Int = 12, rings: Int = 8) {
        fun p(i: Int,j: Int): Pair<V3,V3> {
            val a=i*PI.toFloat()/rings; val b=j*2*PI.toFloat()/sides
            val sx=sin(a)*cos(b);val sy=cos(a);val sz=sin(a)*sin(b)
            return V3(x+rx*sx,y+ry*sy,z+rz*sz) to V3(sx/rx,sy/ry,sz/rz).unit()
        }
        fun tri(a: Pair<V3,V3>,b: Pair<V3,V3>,c: Pair<V3,V3>) { vertex(a.first,a.second,color);vertex(b.first,b.second,color);vertex(c.first,c.second,color) }
        for(i in 0 until rings) for(j in 0 until sides) {
            val a=p(i,j);val b=p(i+1,j);val c=p(i+1,j+1);val d=p(i,j+1)
            tri(a,d,c);tri(a,c,b)
        }
    }
    fun beam(a: V3,b: V3,r: Float,color: Int,sides: Int=6) {
        val direction=(b-a).unit()
        val basis=direction.cross(if(abs(direction.y)<.9f) V3(0f,1f,0f) else V3(1f,0f,0f)).unit()
        val other=direction.cross(basis).unit()
        fun point(v:V3,i:Int):V3 { val t=i*2*PI.toFloat()/sides;return V3(v.x+r*(basis.x*cos(t)+other.x*sin(t)),v.y+r*(basis.y*cos(t)+other.y*sin(t)),v.z+r*(basis.z*cos(t)+other.z*sin(t))) }
        for(i in 0 until sides) {
            quad(point(a,i),point(b,i),point(b,i+1),point(a,i+1),color)
            triangle(a,point(a,i+1),point(a,i),color);triangle(b,point(b,i),point(b,i+1),color)
        }
    }
    fun gem(x:Float,y:Float,z:Float,s:Float,color:Int=0x62DFFA) {
        for(i in 0..5) {
            val a=i*PI.toFloat()/3;val b=(i+1)*PI.toFloat()/3
            val p=V3(x+cos(a)*s*.6f,y+s*.55f,z+sin(a)*s*.6f)
            val q=V3(x+cos(b)*s*.6f,y+s*.55f,z+sin(b)*s*.6f)
            triangle(V3(x,y+s*1.5f,z),q,p,color);triangle(V3(x,y,z),p,q,color)
        }
    }
    fun build(lighting:Float=0f)=BergMesh(vertices.copyOf(used),lighting)
}
