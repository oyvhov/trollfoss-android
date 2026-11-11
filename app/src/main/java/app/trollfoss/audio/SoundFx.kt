package app.trollfoss.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
import kotlin.math.sqrt

/**
 * Plays the synthesised effects. Rendering happens once per install, on a background thread. The
 * playback rate doubles as pitch, which gives every figure its own voice and the piano its notes.
 */
class SoundFx(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val ids = ConcurrentHashMap<Sfx, Int>()

    @Volatile
    var enabled: Boolean = true

    init {
        val directory = File(context.cacheDir, "sfx").apply { mkdirs() }
        thread(name = "trollfoss-sfx", isDaemon = true) {
            // The sounds heard during the first seconds of play are ready before the rest.
            val first = listOf(Sfx.TAP, Sfx.PICK, Sfx.DROP, Sfx.GIGGLE, Sfx.OOH, Sfx.BOING, Sfx.POP)
            for (sfx in first + Sfx.entries.filterNot { it in first }) {
                runCatching {
                    val file = File(directory, "${sfx.name.lowercase()}-v$VERSION.wav")
                    if (!file.exists()) file.writeBytes(Synth.wav(Synth.render(sfx)))
                    ids[sfx] = pool.load(file.path, 1)
                }
            }
        }
    }

    fun play(sfx: Sfx, volume: Float = 0.9f, rate: Float = 1f) {
        play(sfx, volume, rate, pan = 0f)
    }

    fun play(sfx: Sfx, volume: Float, rate: Float, pan: Float) {
        if (!enabled) return
        val id = ids[sfx] ?: return
        val gain = volume.coerceIn(0f, 1f)
        val balance = pan.coerceIn(-1f, 1f)
        val left = gain * sqrt(1f - balance.coerceAtLeast(0f))
        val right = gain * sqrt(1f + balance.coerceAtMost(0f))
        pool.play(id, left, right, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    fun release() {
        pool.release()
    }

    private companion object {
        /** Bump when [Synth] changes so cached files are rendered again. */
        const val VERSION = 1
    }
}
