package app.trollfoss.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import java.util.EnumMap
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * Plays the soundscape under the music: one soft bed at a time that loops without a click, faded in and out when the
 * place changes. Beds are made in memory the first time they are needed (a fraction of a second); nothing is stored.
 * It follows the music switch in the parents' settings.
 */
class SoundscapePlayer {

    private val control = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "trollfoss-soundscape").apply { isDaemon = true } }
    private val cache = EnumMap<SoundBed, ShortArray>(SoundBed::class.java)

    @Volatile private var playing: Playing? = null
    @Volatile private var wanted: SoundBed? = null
    @Volatile private var wantedGain = 0.5f
    @Volatile private var paused = false

    @Volatile
    var enabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (value) wanted?.let { play(it, wantedGain) } else control.execute { stopCurrent(fade = true) }
        }

    private class Playing(val bed: SoundBed, val track: AudioTrack) {
        @Volatile var running = true
        @Volatile var level = 0f
        var writer: Thread? = null
    }

    /** Starts [bed] at [gain] (0 to 1), crossfading from what plays now. The same bed only changes its volume. */
    fun play(bed: SoundBed, gain: Float) {
        wanted = bed; wantedGain = gain
        if (!enabled) return
        control.execute {
            val current = playing
            if (current?.bed == bed && current.running) { fadeTo(current, volume(gain), 600); return@execute }
            stopCurrent(fade = true)
            if (wanted != bed || !enabled) return@execute
            start(bed, loop(bed), volume(wantedGain))
        }
    }

    fun pause() {
        paused = true
        control.execute { runCatching { playing?.track?.pause() } }
    }

    fun resume() {
        paused = false
        control.execute {
            val current = playing
            if (current != null) { if (enabled) runCatching { current.track.play() } } else wanted?.let { play(it, wantedGain) }
        }
    }

    fun release() {
        control.execute { stopCurrent(fade = false) }
        control.shutdown()
    }

    private fun volume(gain: Float) = (gain * BASE).coerceIn(0f, 1f)

    private fun loop(bed: SoundBed): ShortArray = synchronized(cache) { cache[bed] } ?: Soundscape.render(bed).also { synchronized(cache) { cache[bed] = it } }

    private fun start(bed: SoundBed, pcm: ShortArray, target: Float) {
        val minBuffer = AudioTrack.getMinBufferSize(Soundscape.RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(Soundscape.RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(maxOf(minBuffer, 8192) * 2)
                .build()
        }.getOrNull() ?: return
        val current = Playing(bed, track)
        runCatching { track.setVolume(0f) }
        if (!paused) runCatching { track.play() }
        playing = current
        current.writer = thread(name = "trollfoss-soundscape-writer", isDaemon = true) {
            runCatching { Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO) }
            var position = 0
            while (current.running) {
                val count = minOf(2048, pcm.size - position)
                val written = runCatching { track.write(pcm, position, count) }.getOrDefault(-1)
                if (written < 0) break
                if (written == 0) { runCatching { Thread.sleep(40) }; continue }
                position = (position + written) % pcm.size
            }
        }
        fadeTo(current, target, 1200)
    }

    private fun stopCurrent(fade: Boolean) {
        val current = playing ?: return
        if (fade) fadeTo(current, 0f, 600)
        current.running = false
        playing = null
        runCatching { current.track.pause(); current.track.flush() }
        runCatching { current.writer?.join(300) }
        runCatching { current.track.release() }
    }

    private fun fadeTo(current: Playing, target: Float, millis: Long) {
        val steps = (millis / 25).coerceAtLeast(1)
        val from = current.level
        for (i in 1..steps) {
            if (!current.running) return
            val level = from + (target - from) * i / steps
            current.level = level
            runCatching { current.track.setVolume(level) }
            Thread.sleep(25)
        }
    }

    private companion object {
        /** Under the music, which plays at 0.5: the bed is a texture, not a tune. */
        const val BASE = 0.4f
    }
}
