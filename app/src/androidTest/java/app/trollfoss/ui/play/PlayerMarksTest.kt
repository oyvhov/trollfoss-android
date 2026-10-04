package app.trollfoss.ui.play

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import app.trollfoss.ui.theme.PlayerPalette
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerMarksTest {
    @Test fun bothFingersKeepTheirOwnPlayerColourThenTheMarksDisappearAndStayOutOfPhotos() {
        val w = World(); val place = PlaceId.HOME
        val a = w.addPerson(Species.FOLK, Look(), 1f, place, 0.6f, 0.9f)
        val b = w.addPerson(Species.FOLK, Look(), 1f, place, 1.2f, 0.9f)
        w.playerIds += listOf(a.id, b.id)
        val e = Engine(w, place, Sim(w), host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        repeat(30) { e.update(0.016f) }
        val density = Density(1.5f)
        val text = TextMeasurer(createFontFamilyResolver(InstrumentationRegistry.getInstrumentation().targetContext), density, LayoutDirection.Ltr)
        val image = Bitmap.createBitmap(1920, 1200, Bitmap.Config.ARGB_8888)
        fun render() = CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image.asImageBitmap()), Size(1920f, 1200f)) { e.draw(this, text) }
        fun colourNear(p: Person, index: Int): Int {
            val x = ((p.x - e.cam) * e.u - p.h * e.u * 0.3f).toInt()
            val y = (1200f - e.u + p.y * e.u - 20f).toInt()
            val colour = PlayerPalette.color(index).toArgb()
            var count = 0
            for (dy in -18..18) for (dx in -18..18) if (image.getPixel(x + dx, y + dy) == colour) count++
            return count
        }
        fun finger(p: Person) = Offset((p.x - e.cam) * e.u, 1200f - e.u + (p.y - p.h / 2) * e.u)
        try {
            render(); assertEquals(0, colourNear(a, 0)); assertEquals(0, colourNear(b, 1))
            e.down(1, finger(a), 1000); e.down(2, finger(b), 1000)
            repeat(150) { e.update(0.016f) }
            render(); assertTrue(colourNear(a, 0) > 100); assertTrue(colourNear(b, 1) > 100)
            e.photoMode = true; render()
            assertEquals(0, colourNear(a, 0)); assertEquals(0, colourNear(b, 1))
            e.photoMode = false
            e.up(1, finger(a), 3500); e.up(2, finger(b), 3500)
            repeat(150) { e.update(0.016f) }
            render(); assertEquals(0, colourNear(a, 0)); assertEquals(0, colourNear(b, 1))
            // Calling a portrait also identifies that player using the same colour.
            e.invite(b); render(); assertTrue(colourNear(b, 1) > 100)
        } finally { e.cancel(); image.recycle() }
    }

    private val host = object : EngineHost {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id: String) {}
        override fun discovered(key: String) {}
        override fun telescope() {}
        override fun radio(on: Boolean) {}
        override fun egg(id: String) {}
        override fun passage(passage: Passage, arrivalX: Float) {}
    }
}
