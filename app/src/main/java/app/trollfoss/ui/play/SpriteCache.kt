package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Pictures of furniture and things, drawn once per look and then stamped onto the screen.
 *
 * Every piece of art is vector drawing that builds fresh paths each frame, and the GPU must rasterise
 * each of them again. A sofa that stands still looks the same every frame, so it is drawn once into a
 * bitmap and reused. Art that moves by itself (a flickering fire, a ticking clock) is found by drawing it
 * at two moments and comparing; such art is always drawn live. New pictures are made a few per frame, so
 * entering a place never stutters.
 */
class SpriteCache(private val maxBytes: Long = 40L * 1024 * 1024) {
    private class Sprite(val image: ImageBitmap, val left: Float, val top: Float) {
        val bytes: Long get() = image.width.toLong() * image.height * 4
    }

    private val sprites = LinkedHashMap<Any, Sprite>(64, 0.75f, true)
    private val live = HashSet<Any>()
    private var bytes = 0L
    private var madeThisFrame = 0

    /**
     * Pictures of art that moves by itself, redrawn a few times a second into the same bitmap. A
     * wobbling price tag or a blinking figure looks just as alive at 8 to 15 pictures a second, and
     * costs a fraction of drawing it every frame.
     */
    private class Slow(var image: ImageBitmap, var left: Float, var top: Float, var drawnAt: Float)

    private val slow = HashMap<Any, Slow>()
    private var refreshedThisFrame = 0
    private val scope = CanvasDrawScope()

    /** Call once per frame before drawing. */
    fun frame() {
        madeThisFrame = 0
        refreshedThisFrame = 0
    }

    /**
     * Stamps a picture of [key] that is redrawn at most [hz] times a second, and at most a couple of
     * pictures per frame across all keys, so the work spreads out. Returns false only before the first
     * picture exists and none could be drawn this frame; then the caller draws live.
     */
    fun DrawScope.stampSlow(key: Any, bounds: Rect, t: Float, hz: Float, draw: DrawScope.(t: Float) -> Unit): Boolean {
        val left = floor(bounds.left)
        val top = floor(bounds.top)
        val w = ceil(bounds.right - left).toInt().coerceIn(1, MAX_SIDE)
        val h = ceil(bounds.bottom - top).toInt().coerceIn(1, MAX_SIDE)
        var s = slow[key]
        val due = s == null || t - s.drawnAt >= 1f / hz || t < s.drawnAt
        if (due && refreshedThisFrame < MAX_REFRESH_PER_FRAME) {
            refreshedThisFrame++
            if (s == null || s.image.width != w || s.image.height != h) {
                s = Slow(ImageBitmap(w, h), left, top, t)
                slow[key] = s
            } else {
                clear(s.image)
                s.left = left
                s.top = top
                s.drawnAt = t
            }
            val image = s.image
            scope.draw(Density(density, fontScale), LayoutDirection.Ltr, Canvas(image), Size(w.toFloat(), h.toFloat())) {
                translate(-left, -top) { draw(t) }
            }
        }
        if (s == null) return false
        drawImage(s.image, Offset(s.left, s.top))
        return true
    }

    /** True once [key] is known to move by itself. */
    fun animated(key: Any): Boolean = key in live

    /** Forgets slow pictures whose keys are gone (figures that left, furniture put away). */
    fun keepSlow(keys: Set<Any>) {
        slow.keys.retainAll(keys)
    }

    private fun clear(image: ImageBitmap) {
        image.asAndroidBitmap().eraseColor(android.graphics.Color.TRANSPARENT)
    }

    /**
     * Draws the picture for [key] with its origin at the current origin. [bounds] (pixels, relative to
     * the origin) must hold everything the art draws. [draw] draws the art at time [t]; returns false when
     * the art must be drawn live instead (it animates, or the picture is not made yet).
     */
    fun DrawScope.stamp(key: Any, bounds: Rect, t: Float, draw: DrawScope.(t: Float) -> Unit): Boolean {
        if (key in live) return false
        if (key in live) return false
        val sprite = sprites[key] ?: make(key, bounds, t, draw) ?: return false
        drawImage(sprite.image, Offset(sprite.left, sprite.top))
        return true
    }

    private fun DrawScope.make(key: Any, bounds: Rect, t: Float, draw: DrawScope.(t: Float) -> Unit): Sprite? {
        if (madeThisFrame >= MAX_NEW_PER_FRAME) return null
        madeThisFrame++
        val left = floor(bounds.left)
        val top = floor(bounds.top)
        val w = ceil(bounds.right - left).toInt().coerceIn(1, MAX_SIDE)
        val h = ceil(bounds.bottom - top).toInt().coerceIn(1, MAX_SIDE)
        val first = render(w, h, left, top) { draw(t) }
        val second = render(w, h, left, top) { draw(t + 0.61f) }
        if (!same(first, second)) {
            live += key
            return null
        }
        val sprite = trim(first, left, top) ?: run {
            live += key
            return null
        }
        sprites[key] = sprite
        bytes += sprite.bytes
        while (bytes > maxBytes && sprites.isNotEmpty()) {
            val oldest = sprites.entries.iterator().next()
            bytes -= oldest.value.bytes
            sprites.remove(oldest.key)
        }
        return sprite
    }

    private fun DrawScope.render(w: Int, h: Int, left: Float, top: Float, block: DrawScope.() -> Unit): ImageBitmap {
        val image = ImageBitmap(w, h)
        scope.draw(Density(density, fontScale), LayoutDirection.Ltr, Canvas(image), Size(w.toFloat(), h.toFloat())) {
            translate(-left, -top) { block() }
        }
        return image
    }

    private fun same(a: ImageBitmap, b: ImageBitmap): Boolean {
        val pa = IntArray(a.width * a.height)
        val pb = IntArray(b.width * b.height)
        a.readPixels(pa)
        b.readPixels(pb)
        return pa.contentEquals(pb)
    }

    /** Crops away the empty border, so a sprite only costs the memory of what it shows. */
    private fun trim(image: ImageBitmap, left: Float, top: Float): Sprite? {
        val w = image.width
        val h = image.height
        val px = IntArray(w * h)
        image.readPixels(px)
        var x0 = w
        var y0 = h
        var x1 = -1
        var y1 = -1
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                if (px[row + x] ushr 24 != 0) {
                    if (x < x0) x0 = x
                    if (x > x1) x1 = x
                    if (y < y0) y0 = y
                    if (y > y1) y1 = y
                }
            }
        }
        if (x1 < 0) return null
        // Art that touches the edge probably spills over it; draw it live rather than cut it off.
        if (x0 == 0 || y0 == 0 || x1 == w - 1 || y1 == h - 1) return null
        val cropped = android.graphics.Bitmap.createBitmap(image.asAndroidBitmap(), x0, y0, x1 - x0 + 1, y1 - y0 + 1)
        return Sprite(cropped.asImageBitmap().also { it.prepareToDraw() }, left + x0, top + y0)
    }

    fun clear() {
        sprites.clear()
        live.clear()
        slow.clear()
        bytes = 0L
    }

    private companion object {
        const val MAX_NEW_PER_FRAME = 3
        const val MAX_REFRESH_PER_FRAME = 3
        const val MAX_SIDE = 2400
    }
}
