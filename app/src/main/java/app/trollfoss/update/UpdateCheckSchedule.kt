package app.trollfoss.update

/** The last completed request determines when the next automatic check can run. */
internal data class UpdateCheckSchedule(val completedAt: Long = 0L, val intervalMs: Long = 0L) {
    fun isDue(now: Long, automatic: Boolean): Boolean = automatic &&
        (completedAt == 0L || now < completedAt || now - completedAt >= intervalMs)

    companion object {
        fun after(now: Long, failure: UpdateMessage?): UpdateCheckSchedule {
            val minutes = when (failure) {
                null -> 12 * 60
                UpdateMessage.RATE -> 60
                else -> 5
            }
            return UpdateCheckSchedule(now, minutes * 60_000L)
        }
    }
}
