package app.trollfoss

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.trollfoss.logo.renderLogoArt
import java.io.File

/**
 * Debug only. Draws one piece of the brand art (`--es art emblem|wordmark|banner|social`) for
 * `scripts/Render-Logos.ps1`. Animations are frozen. Two ways to get the pixels out:
 *
 * * `--es mode file --ei w 2048 --ei h 2048` draws on a transparent bitmap on a worker thread and writes
 *   `files/<art>.png` into the app's private folder (the default of the script; true alpha, no screen needed).
 * * Without `mode` the art fills the whole display on a Compose canvas, drawn after `--es bg black|white`
 *   (the script sets the display to the target size and captures it on black and on white, which gives a
 *   true transparent PNG as well).
 */
class LogoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.trollfoss.logo.LogoEnv.context = applicationContext
        val art = intent.getStringExtra("art") ?: "emblem"
        val bg = intent.getStringExtra("bg") ?: "none"
        if (intent.getStringExtra("mode") == "file") {
            val w = intent.getIntExtra("w", 1024)
            val h = intent.getIntExtra("h", 1024)
            Thread({ renderToFile(art, bg, w, h) }, "logo-render").start()
            return
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.BLACK))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        setContent {
            Canvas(Modifier.fillMaxSize()) {
                renderLogoArt(art, bg)
                // The script waits for this line before it takes the screenshot.
                Log.i("LogoArt", "drawn $art $bg ${size.width.toInt()}x${size.height.toInt()}")
            }
        }
    }

    private fun renderToFile(art: String, bg: String, w: Int, h: Int) {
        try {
            val image = ImageBitmap(w, h, ImageBitmapConfig.Argb8888, hasAlpha = true)
            val canvas = androidx.compose.ui.graphics.Canvas(image)
            CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(w.toFloat(), h.toFloat())) {
                renderLogoArt(art, bg)
            }
            val out = File(filesDir, "$art.png")
            out.outputStream().use { image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
            Log.i("LogoArt", "wrote $art ${w}x$h ${out.length()}")
        } catch (t: Throwable) {
            Log.e("LogoArt", "failed $art", t)
        }
        runOnUiThread { finish() }
    }
}
