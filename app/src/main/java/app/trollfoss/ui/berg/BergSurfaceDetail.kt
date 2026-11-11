package app.trollfoss.ui.berg

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import kotlin.math.*

/** One small tile of stone grain, pale seams, timber grain and vegetation variation.
 * World-space coordinates and mipmaps keep the pattern attached to the mountainside at any zoom. */
internal fun uploadBergSurface() {
    val size=128
    fun lattice(x:Int,y:Int,cells:Int):Float {
        var n=(x.mod(cells)*1619+y.mod(cells)*31337+811)*6971
        n=(n xor (n ushr 13))*1274126177
        return ((n xor (n ushr 16)) and 65535)/65535f
    }
    fun noise(u:Float,v:Float,cells:Int):Float {
        val x=u*cells;val y=v*cells;val i=floor(x).toInt();val j=floor(y).toInt()
        val fx=x-i;val fy=y-j
        val sx=fx*fx*(3f-2f*fx);val sy=fy*fy*(3f-2f*fy)
        fun row(k:Int)=lattice(i,k,cells)*(1f-sx)+lattice(i+1,k,cells)*sx
        return row(j)*(1f-sy)+row(j+1)*sy
    }
    val data=ByteBuffer.allocateDirect(size*size*4)
    fun channel(value:Float) {data.put((value.coerceIn(0f,1f)*255f).roundToInt().toByte())}
    for(y in 0 until size) for(x in 0 until size) {
        val u=x.toFloat()/size;val v=y.toFloat()/size
        val cloud=noise(u,v,4);val grain=noise(u,v,32)
        val stone=.39f*cloud+.34f*noise(u,v,12)+.27f*noise(u,v,72)
        val layer=abs(sin((u+v*3f+cloud*.27f)*2f*PI.toFloat()))
        val seam=(1f-layer*10f).coerceAtLeast(0f)*(.25f+grain*.75f)
        val wood=sin(2f*PI.toFloat()*(v*9f+.62f*cloud+.16f*grain))
        channel(stone);channel(seam);channel(.50f+.30f*wood+.17f*(grain-.5f));channel(noise(u,v,8))
    }
    data.flip()
    val name=IntArray(1)
    glGenTextures(1,name,0);glActiveTexture(GL_TEXTURE0);glBindTexture(GL_TEXTURE_2D,name[0])
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR)
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT)
    glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,size,size,0,GL_RGBA,GL_UNSIGNED_BYTE,data)
    glGenerateMipmap(GL_TEXTURE_2D)
}
