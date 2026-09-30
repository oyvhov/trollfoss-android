package app.trollfoss.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.EnumMap
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * Plays the composed loops. A writer thread streams the loop over and over, so it never clicks at the
 * seam; switching place fades the old music out and the new one in. Music sits under speech: while the
 * narrator talks it is ducked, and during tasks it plays softer.
 */
class MusicPlayer(cacheDir: File) {

    private val control = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "trollfoss-music").apply { isDaemon = true } }
    private val cache = EnumMap<MusicTheme, ShortArray>(MusicTheme::class.java)
    private val directory = File(cacheDir, "music")
    private val locks: Map<MusicTheme, Any> = MusicTheme.entries.associateWith { Any() }

    @Volatile private var playing: Playing? = null
    @Volatile private var wanted: MusicTheme? = null
    @Volatile private var paused = false
    @Volatile private var ducked = false
    @Volatile private var quiet = false

    init {
        // Composing takes a moment on a slow tablet, so every loop is made once, in the background,
        // and kept on the device. Later starts only read the files.
        thread(name = "trollfoss-music-prepare", isDaemon = true, priority = Thread.MIN_PRIORITY) {
            MusicTheme.entries.forEach { runCatching { loop(it) } }
        }
    }

    /** Parents can switch music off; effects and speech are separate. */
    @Volatile
    var enabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (value) wanted?.let(::play) else control.execute { stopCurrent(fade = true) }
        }

    private class Playing(val theme: MusicTheme, val track: AudioTrack) {
        @Volatile var running = true
        @Volatile var level = 0f
        var writer: Thread? = null
    }

    /** Starts [theme], crossfading from whatever plays now. Calling it again for the same theme does nothing. */
    fun play(theme: MusicTheme) {
        wanted = theme
        if (!enabled) return
        control.execute {
            if (playing?.theme == theme && playing?.running == true) return@execute
            stopCurrent(fade = true)
            if (wanted != theme || !enabled) return@execute
            val pcm = loop(theme)
            start(theme, pcm)
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
            if (current != null) {
                if (enabled) runCatching { current.track.play() }
            } else {
                wanted?.let(::play)
            }
        }
    }

    /** Lowers the music while speech is playing. */
    fun duck(on: Boolean) {
        if (ducked == on) return
        ducked = on
        control.execute { playing?.let { fadeTo(it, targetLevel(), if (on) 200 else 600) } }
    }

    /** Softer music while the child works on a task. */
    fun setQuiet(on: Boolean) {
        if (quiet == on) return
        quiet = on
        control.execute { playing?.let { fadeTo(it, targetLevel(), 700) } }
    }

    fun release() {
        control.execute { stopCurrent(fade = false) }
        control.shutdown()
    }

    /** The loop for [theme]: from memory, from the device, or composed now and saved for next time. */
    private fun loop(theme: MusicTheme): ShortArray = synchronized(locks.getValue(theme)) {
        synchronized(cache) { cache[theme] }?.let { return it }
        val file = File(directory, "${theme.name.lowercase()}-v$VERSION.pcm")
        val stored = runCatching {
            val bytes = file.readBytes()
            ShortArray(bytes.size / 2).also { ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(it) }
        }.getOrNull()?.takeIf { it.isNotEmpty() }
        val pcm = stored ?: MusicComposer.render(theme).also { rendered ->
            runCatching {
                directory.mkdirs()
                val buffer = ByteBuffer.allocate(rendered.size * 2).order(ByteOrder.LITTLE_ENDIAN)
                buffer.asShortBuffer().put(rendered)
                val temporary = File(directory, file.name + ".tmp")
                temporary.writeBytes(buffer.array())
                temporary.renameTo(file)
            }
        }
        synchronized(cache) { cache[theme] = pcm }
        pcm
    }

    private fun targetLevel(): Float {
        val base = if (quiet) VOLUME * 0.6f else VOLUME
        return if (ducked) base * 0.35f else base
    }

    private fun start(theme: MusicTheme, pcm: ShortArray) {
        val minBuffer = AudioTrack.getMinBufferSize(MusicComposer.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(MusicComposer.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(maxOf(minBuffer, 8192) * 2)
                .build()
        }.getOrNull() ?: return
        val current = Playing(theme, track)
        runCatching { track.setVolume(0f) }
        if (!paused) runCatching { track.play() }
        playing = current
        current.writer = thread(name = "trollfoss-music-writer", isDaemon = true) {
            runCatching { Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO) }
            var position = 0
            val chunk = 2048
            while (current.running) {
                val count = minOf(chunk, pcm.size - position)
                val written = runCatching { track.write(pcm, position, count) }.getOrDefault(-1)
                if (written < 0) break
                // A paused track takes nothing; wait instead of spinning.
                if (written == 0) {
                    runCatching { Thread.sleep(40) }
                    continue
                }
                position = (position + written) % pcm.size
            }
        }
        fadeTo(current, targetLevel(), 900)
    }

    private fun stopCurrent(fade: Boolean) {
        val current = playing ?: return
        if (fade) fadeTo(current, 0f, 500)
        current.running = false
        playing = null
        runCatching {
            current.track.pause()
            current.track.flush()
        }
        // Let the writer leave its write call before the track goes away.
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
        const val VOLUME = 0.5f

        /** Bump when [MusicComposer] changes, so stored loops are composed again. */
        const val VERSION = 1
    }
}

