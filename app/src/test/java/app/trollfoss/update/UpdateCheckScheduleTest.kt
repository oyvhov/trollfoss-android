package app.trollfoss.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckScheduleTest {
    private val minute = 60_000L
    private val start = 2_000_000_000L

    @Test
    fun `first launch with automatic checks enabled is due immediately`() {
        assertTrue(UpdateCheckSchedule().isDue(start, automatic = true))
        assertFalse(UpdateCheckSchedule().isDue(start, automatic = false))
    }

    @Test
    fun `a successful check waits twelve hours including across restarts`() {
        val saved = UpdateCheckSchedule.after(start, failure = null)
        val restored = UpdateCheckSchedule(saved.completedAt, saved.intervalMs)
        assertFalse(restored.isDue(start + 720 * minute - 1, automatic = true))
        assertTrue(restored.isDue(start + 720 * minute, automatic = true))
    }

    @Test
    fun `an offline start can retry after five minutes`() {
        val failed = UpdateCheckSchedule.after(start, UpdateMessage.NETWORK)
        assertFalse(failed.isDue(start + 5 * minute - 1, automatic = true))
        assertTrue(failed.isDue(start + 5 * minute, automatic = true))
        assertFalse(failed.isDue(start + 5 * minute, automatic = false))
    }

    @Test
    fun `github rate limits wait an hour`() {
        val limited = UpdateCheckSchedule.after(start, UpdateMessage.RATE)
        assertFalse(limited.isDue(start + 60 * minute - 1, automatic = true))
        assertTrue(limited.isDue(start + 60 * minute, automatic = true))
    }

    @Test
    fun `a backward clock change allows a fresh check when enabled`() {
        val checked = UpdateCheckSchedule.after(start, failure = null)
        assertTrue(checked.isDue(start - minute, automatic = true))
        assertFalse(checked.isDue(start - minute, automatic = false))
    }
}
