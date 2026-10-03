package app.trollfoss.ui.play

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The furniture panel steps aside when the child has gone back to playing. */
class PanelIdleTest {
    @Test fun `two things or figures in a row close the panel, one does not`() {
        val idle = PanelIdle()
        assertFalse(idle.played())
        assertTrue(idle.played())
    }

    @Test fun `furniture or a touch on the panel in between starts the count again`() {
        val idle = PanelIdle()
        assertFalse(idle.played())
        idle.decorating()
        assertFalse(idle.played())
        assertTrue(idle.played())
    }

    @Test fun `after the panel has closed the count starts from nothing`() {
        val idle = PanelIdle()
        idle.played(); idle.played()
        idle.decorating()
        assertFalse(idle.played())
    }
}
