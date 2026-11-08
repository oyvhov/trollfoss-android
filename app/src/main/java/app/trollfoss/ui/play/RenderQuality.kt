package app.trollfoss.ui.play

/** Decoration only: physics, hit areas and player actions never depend on this policy. */
class RenderQuality(lowMemory: Boolean = false, memoryClassMb: Int = 256) {
    val cacheBytes: Long = (memoryClassMb / 8).coerceIn(8, if (lowMemory) 12 else 40) * 1024L * 1024L
    var reduced = lowMemory
        private set
    val particleLimit get() = if (reduced) 120 else 400
    val effectScale get() = if (reduced) 0.45f else 1f
    private var frames = 0
    private var samples = 0
    private var slow = 0

    /** Ignore scene warm-up and isolated spikes. Stay at the calmer level for this scene. */
    fun recordDraw(nanos: Long) {
        if (reduced || ++frames <= 120 || nanos <= 0 || nanos > 250_000_000L) return
        samples++
        if (nanos > 24_000_000L) slow++
        if (samples >= 120) {
            if (slow >= 72) reduced = true
            samples = 0; slow = 0
        }
    }
}
