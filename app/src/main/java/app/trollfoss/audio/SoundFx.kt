package app.trollfoss.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

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
        if (!enabled) return
        val id = ids[sfx] ?: return
        pool.play(id, volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f), 1, 0, rate.coerceIn(0.5f, 2f))
    }

    fun release() {
        pool.release()
    }

    private companion object {
        /** Bump when [Synth] changes so cached files are rendered again. */
        const val VERSION = 1
    }
}
