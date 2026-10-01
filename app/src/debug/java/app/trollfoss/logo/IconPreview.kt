package app.trollfoss.logo

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import app.trollfoss.R

/** Where the art can find resources: set by LogoActivity. */
object LogoEnv {
    var context: Context? = null
}

/**
 * A contact sheet for the adaptive launcher icon: the real `ic_launcher` drawable through a circle, a squircle
 * and a rounded square mask, the monochrome layer, a few launcher sizes, and the 66 dp safe zone outlined.
 */
internal fun DrawScope.drawIconSheet() {
    val ctx = LogoEnv.context ?: return
    drawRect(Color(0xFFEDEEF5))
    val icon = ctx.getDrawable(R.mipmap.ic_launcher) as? AdaptiveIconDrawable ?: return
    val bgLayer = icon.background
    val fgLayer = icon.foreground
    val mono: Drawable? = if (Build.VERSION.SDK_INT >= 33) icon.monochrome else null
    drawIntoCanvas { canvas ->
        val c = canvas.nativeCanvas
        fun mask(kind: Int, x: Float, y: Float, s: Float, block: () -> Unit) {
            c.save()
            val p = android.graphics.Path()
            when (kind) {
                0 -> p.addCircle(x + s / 2, y + s / 2, s / 2, android.graphics.Path.Direction.CW)
                1 -> p.addRoundRect(x, y, x + s, y + s, s * 0.36f, s * 0.36f, android.graphics.Path.Direction.CW)
                else -> p.addRoundRect(x, y, x + s, y + s, s * 0.18f, s * 0.18f, android.graphics.Path.Direction.CW)
            }
            c.clipPath(p)
            block()
            c.restore()
        }
        fun layers(x: Float, y: Float, s: Float) {
            val full = s * 1.5f
            val l = (x - s * 0.25f).toInt()
            val t = (y - s * 0.25f).toInt()
            bgLayer.setBounds(l, t, l + full.toInt(), t + full.toInt())
            bgLayer.draw(c)
            fgLayer.setBounds(l, t, l + full.toInt(), t + full.toInt())
            fgLayer.draw(c)
        }
        val s = 420f
        for (k in 0 until 3) {
            val x = 40f + k * (s + 40f)
            mask(k, x, 40f, s) { layers(x, 40f, s) }
        }
        // The safe zone (66 dp of the 108 dp layer) over the first one.
        val outline = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color(0x99FF0066).toArgb()
            isAntiAlias = true
        }
        c.drawCircle(40f + s / 2, 40f + s / 2, s * 66f / 72f / 2f, outline)
        // Monochrome layer, tinted like a themed icon.
        if (mono != null) {
            val x = 40f + 3 * (s + 40f)
            val back = Paint().apply { color = Color(0xFF3B3A55).toArgb(); isAntiAlias = true }
            c.drawCircle(x + s / 2, 40f + s / 2, s / 2, back)
            mono.setTint(Color(0xFFE3DFFF).toArgb())
            val full = (s * 1.5f).toInt()
            mono.setBounds((x - s * 0.25f).toInt(), (40f - s * 0.25f).toInt(), (x - s * 0.25f).toInt() + full, (40f - s * 0.25f).toInt() + full)
            c.save()
            val p = android.graphics.Path()
            p.addCircle(x + s / 2, 40f + s / 2, s / 2, android.graphics.Path.Direction.CW)
            c.clipPath(p)
            mono.draw(c)
            c.restore()
        }
        // Launcher sizes.
        var x = 40f
        for (size in listOf(192f, 144f, 96f, 72f, 48f)) {
            mask(0, x, 520f, size) { layers(x, 520f, size) }
            x += size + 24f
        }
        x += 40f
        for (size in listOf(192f, 96f)) {
            mask(1, x, 520f, size) { layers(x, 520f, size) }
            x += size + 24f
        }
    }
}
