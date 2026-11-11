package app.trollfoss.ui.berg

import android.content.res.AssetManager
import java.io.DataInputStream
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Original Blender art, batched and lit offline. No network or Blender runtime on the device. */
internal class BergAssets(private val assets: AssetManager, private val lowDetail: Boolean = false) {
    fun mesh(name: String): BergMesh =
        // Android's asset merger unpacks the source .gz and packages it as .hbg.
        DataInputStream(BufferedInputStream(assets.open("heileberget/$name${if(lowDetail && name in setOf("outside","cave","troll","arm")) "-low" else ""}.hbg"),65536)).use { input ->
            val format=input.readInt()
            check(format == 0x48424731 || format == 0x48424732) { "Unsupported Heileberget mesh" }
            val vertices = input.readInt()
            val lighting = input.readInt()
            check(vertices in 3..2_000_000 && vertices % 3 == 0 && lighting in 0..2)
            if(format==0x48424732) {
                // Positions retain full precision. Normals/colours are normalized bytes on the GPU.
                // Read directly into the one upload buffer, avoiding a second float-array copy.
                val memory=ByteBuffer.allocateDirect(vertices*20).order(ByteOrder.LITTLE_ENDIAN)
                val chunk=ByteArray(64*1024)
                while(memory.hasRemaining()) {
                    val count=minOf(chunk.size,memory.remaining())
                    input.readFully(chunk,0,count);memory.put(chunk,0,count)
                }
                check(input.read()==-1) {"Unexpected mesh data"}
                memory.flip()
                return@use BergMesh(memory,vertices,lighting.toFloat())
            }
            val values = FloatArray(vertices * 9)
            val bytes = ByteArray(36 * 1024)
            val floats = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
            var offset = 0
            while (offset < values.size) {
                val count = minOf(bytes.size / 4, values.size - offset)
                input.readFully(bytes, 0, count * 4)
                floats.position(0)
                floats.get(values, offset, count)
                offset += count
            }
            check(input.read() == -1) { "Unexpected mesh data" }
            BergMesh(values, lighting.toFloat())
        }
}
