package app.trollfoss.ui.play

import android.graphics.Bitmap
import android.util.Log
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
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Run explicitly with -e class ...ScenePerformanceTest for repeatable software-renderer evidence. */
@RunWith(AndroidJUnit4::class)
class ScenePerformanceTest {
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

    @Test fun renderScenesAtBothSizes() {
        val args = InstrumentationRegistry.getArguments()
        // Keep this longer measurement out of ordinary correctness runs.
        org.junit.Assume.assumeTrue(args.getString("measureScenes") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for ((width, height) in listOf(1920 to 1200, 2400 to 1080)) {
            val density = Density(if (height == 1200) 1.5f else 2.625f)
            val text = TextMeasurer(createFontFamilyResolver(context), density, LayoutDirection.Ltr)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap.asImageBitmap())
            val scope = CanvasDrawScope()
            for (place in listOf(PlaceId.MANOR_GROUND, PlaceId.MANOR_CELLAR, PlaceId.VAGSTADDALEN)) {
                val world = WorldFactory.create()
                val sim = Sim(world)
                sim.settle(place)
                val engine = Engine(world, place, sim, host, false, 0f).apply {
                    setSize(width.toFloat(), height.toFloat(), density.density)
                    photoMode = true
                }
                fun frame() = scope.draw(density, LayoutDirection.Ltr, canvas, Size(width.toFloat(), height.toFloat())) {
                    engine.draw(this, text)
                }
                try {
                    // Fixed simulation state makes before/after pixels directly comparable.
                    repeat(60) { frame() }
                    File(context.getExternalFilesDir(null), "perf-$width-$place.png").outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    val elapsed = LongArray(120) {
                        val start = System.nanoTime()
                        frame()
                        System.nanoTime() - start
                    }.sorted()
                    Log.i("SceneBenchmark", "$width/$place draw p50=${elapsed[60] / 1e6}ms p95=${elapsed[114] / 1e6}ms mean=${elapsed.average() / 1e6}ms")
                } finally { engine.detach() }
            }
            bitmap.recycle()
        }
    }
}
