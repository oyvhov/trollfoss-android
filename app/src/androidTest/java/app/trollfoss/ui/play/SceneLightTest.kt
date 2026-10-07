package app.trollfoss.ui.play

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The living scene drawn for real: daylight through a window, the light rig's spotlight and rings on water. Each test
 * renders the same frame twice with one thing changed and no update between, so only that light can differ.
 * Furniture and figures are left out (`skip`), so nothing stands in front of the light being measured.
 */
@RunWith(AndroidJUnit4::class)
class SceneLightTest {
    private val density = Density(1.5f)
    private val text = TextMeasurer(createFontFamilyResolver(InstrumentationRegistry.getInstrumentation().targetContext), density, LayoutDirection.Ltr)
    private val image = Bitmap.createBitmap(1920, 1200, Bitmap.Config.ARGB_8888)

    @After fun recycle() = image.recycle()

    private fun engine(w: World, place: PlaceId, cam: Float, sim: Sim = Sim(w)) =
        Engine(w, place, sim, host, false, cam).apply { setSize(1920f, 1200f, 1.5f); skip = 2 or 4; repeat(20) { update(0.016f) } }

    private fun render(e: Engine): IntArray {
        image.eraseColor(android.graphics.Color.BLACK)
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image.asImageBitmap()), Size(1920f, 1200f)) { e.draw(this, text) }
        return IntArray(1920 * 1200).also { image.getPixels(it, 0, 1920, 0, 0, 1920, 1200) }
    }

    /** A box in scene units, turned into screen pixels for [e]. */
    private class Box(val x1: Int, val y1: Int, val x2: Int, val y2: Int)
    private fun box(e: Engine, x1: Float, y1: Float, x2: Float, y2: Float) = Box(
        ((x1 - e.cam) * e.u).toInt().coerceIn(0, 1919), (1200f - e.u + y1 * e.u).toInt().coerceIn(0, 1199),
        ((x2 - e.cam) * e.u).toInt().coerceIn(0, 1919), (1200f - e.u + y2 * e.u).toInt().coerceIn(0, 1199))

    private fun brightness(pixels: IntArray, b: Box): Double {
        var sum = 0.0; var n = 0
        for (y in b.y1..b.y2) for (x in b.x1..b.x2) {
            val c = pixels[y * 1920 + x]
            sum += ((c shr 16 and 0xFF) + (c shr 8 and 0xFF) + (c and 0xFF)) / 3.0; n++
        }
        return sum / n
    }

    private fun changed(a: IntArray, b: IntArray, box: Box): Int {
        var n = 0
        for (y in box.y1..box.y2) for (x in box.x1..box.x2) if (a[y * 1920 + x] != b[y * 1920 + x]) n++
        return n
    }

    @Test fun daylightFallsThroughAnOpenWindowButNotAtNight() {
        for (night in listOf(false, true)) {
            val w = WorldFactory.create().apply { this.night = night; weather = Weather.SUN }
            val place = PlaceId.HOME
            val window = w.fixturesIn(place).first { it.type == FixtureType.WINDOW }
            window.mode = 0
            val e = engine(w, place, (window.x - 0.5f).coerceAtLeast(0f))
            val open = render(e)
            window.mode = 1
            val closed = render(e)
            val top = window.y - 0.2f; val floor = place.floor
            val shaft = box(e, window.x + 0.04f, top + 0.4f * (floor - top), window.x + 0.18f, top + 0.6f * (floor - top))
            if (night) assertEquals("no daylight at night", 0, changed(open, closed, shaft))
            else assertTrue("the open window lights the room", brightness(open, shaft) - brightness(closed, shaft) > 8.0)
        }
    }

    @Test fun theLightRigsSceneLightMakesABrightPatchOnTheFloor() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val rig = s.designer.add(place, FixtureType.PLAY_LIGHT_RIG, 0, 1.2f, place.floor)!!
        val e = engine(w, place, 0f, s)
        val dark = render(e)
        rig.mode = 1
        val lit = render(e)
        val patch = box(e, rig.x + 0.28f, rig.y - 0.1f, rig.x + 0.52f, rig.y - 0.01f)
        assertTrue("the spotlight lights the floor", brightness(lit, patch) - brightness(dark, patch) > 15.0)
        assertEquals("and nothing far away", 0, changed(dark, lit, box(e, 0f, 0.1f, rig.x - 0.6f, place.floor)))
    }

    @Test fun aRingGrowsOnTheWaterWhereSomethingSplashed() {
        val w = World()
        val place = PlaceId.entries.first { Places.spec(it).water != null }
        val water = Places.spec(place).water!!
        val x = water.x1 + 0.6f
        val e = engine(w, place, (x - 1f).coerceAtLeast(0f))
        val calm = render(e)
        e.ripples.add(x, water.line); e.ripples.step(0.4f)
        val ringed = render(e)
        assertTrue("rings on the water", changed(calm, ringed, box(e, x - 0.15f, water.line - 0.04f, x + 0.15f, water.line + 0.04f)) > 100)
        assertEquals("and nowhere else", 0, changed(calm, ringed, box(e, x - 0.6f, 0.1f, x - 0.3f, water.line + 0.1f)))
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
