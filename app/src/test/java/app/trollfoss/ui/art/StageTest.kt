package app.trollfoss.ui.art

import org.junit.Assert.assertTrue
import org.junit.Test

class StageTest {
    @Test
    fun `background covers the top when the scene is anchored at the bottom`() {
        val views = listOf(
            Triple(1920f, 1200f, 1920f / 2.35f),
            Triple(1600f, 1200f, 1600f / 2.35f),
            Triple(1080f, 1080f, 1080f / 2.35f),
            Triple(2400f, 1080f, 1080f),
        )
        for ((w, h, u) in views) {
            val stage = Stage(0f, u, w, h, 8f)
            val translatedTop = stage.backgroundTop + h - u
            assertTrue("unpainted strip at $w x $h: $translatedTop", translatedTop <= 0.01f)
        }
    }
}
