package app.trollfoss.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Look
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawFixtureBack
import app.trollfoss.ui.art.drawFixtureFront
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.art.drawThing
import app.trollfoss.ui.art.groundShadow
import kotlin.math.max
import kotlin.math.min

// Small pictures of furniture, things and animals, for panels, task cards and stickers.

/** Draws a piece of furniture fitted into [box] (pixels). */
fun DrawScope.drawFixtureThumb(type: FixtureType, variant: Int, box: Rect) {
    // Furniture that only a floor of the big house knows is drawn by that floor's art, which is chosen by place.
    val place = app.trollfoss.domain.House.floors.firstOrNull { it.specOf(type) != null }?.place ?: PlaceId.HOME
    val f = Fixture(-1, place, type, 0f, 0f, variant)
    val spec = f.spec
    // Leave room for the oblique top and side, which reach up and to the right.
    val w = spec.w + 0.12f
    val h = max(spec.h, 0.05f) + 0.12f
    val u = min(box.width / w, box.height / h) * 0.92f
    val pen = Pen(max(1.2f, u * 0.0045f))
    translate(box.center.x - 0.05f * u, box.center.y + (max(spec.h, 0.05f) / 2f) * u + 0.03f * u) {
        drawFixtureBack(f, u, pen)
        if (spec.front || spec.glass) drawFixtureFront(f, u, pen)
    }
}

/**
 * How far below the middle of its thumb a piece of furniture has its feet, as a fraction of the thumb's side
 * (see [drawFixtureThumb]): next to nothing for a rug, almost half for a wardrobe.
 */
fun fixtureThumbFeet(type: FixtureType): Float {
    val place = app.trollfoss.domain.House.floors.firstOrNull { it.specOf(type) != null }?.place ?: PlaceId.HOME
    val spec = Fixture(-1, place, type, 0f, 0f, 0).spec
    val tall = max(spec.h, 0.05f)
    val u = min(1f / (spec.w + 0.12f), 1f / (tall + 0.12f)) * 0.92f
    return (tall / 2f + 0.03f) * u
}

/** Draws a thing fitted into [box]. */
fun DrawScope.drawThingThumb(type: ThingType, variant: Int, box: Rect) {
    val s = min(box.width / type.w, box.height / type.h) * 0.78f
    val pen = Pen(max(1.2f, s * 0.0034f))
    translate(box.center.x, box.center.y + type.h * s / 2f) {
        drawThing(type, variant, 0, type.w * s, type.h * s, pen)
    }
}

/** Draws an animal or a figure standing, fitted into [box]. */
fun DrawScope.drawSpeciesThumb(species: Species, box: Rect, look: Look = Look()) {
    val h = box.height * 0.86f
    val pen = Pen(max(1.2f, h * 0.012f))
    translate(box.center.x, box.bottom - box.height * 0.06f) {
        // Sture floats: his shadow stays on the ground.
        if (species == Species.GHOST) groundShadow(0f, 0f, h * 0.55f)
        // The plain default look would be the third colour for them; their first is the friendly one.
        val shown = if ((species == Species.ROBOT || species == Species.GHOST) && look == Look()) Look(skin = 0) else look
        drawPerson(species, shown, Pose.STAND, PersonAnim(), h, pen, false, seed = 0.3f)
    }
}

/** A square box of [side] pixels centred on [c]. */
fun square(c: Offset, side: Float): Rect = Rect(c.x - side / 2f, c.y - side / 2f, c.x + side / 2f, c.y + side / 2f)

/**
 * Small pictures drawn once into bitmaps and reused. The panels and cards show dozens of detailed
 * drawings; as bitmaps they cost the GPU next to nothing, even on slow tablets.
 */
object ThumbCache {
    private val images = LinkedHashMap<String, androidx.compose.ui.graphics.ImageBitmap>(96, 0.75f, true)
    private val scope = androidx.compose.ui.graphics.drawscope.CanvasDrawScope()

    fun get(key: String, px: Int, density: Float, draw: DrawScope.() -> Unit): androidx.compose.ui.graphics.ImageBitmap {
        val full = "$key@$px"
        images[full]?.let { return it }
        val image = androidx.compose.ui.graphics.ImageBitmap(px, px)
        scope.draw(
            androidx.compose.ui.unit.Density(density),
            androidx.compose.ui.unit.LayoutDirection.Ltr,
            androidx.compose.ui.graphics.Canvas(image),
            androidx.compose.ui.geometry.Size(px.toFloat(), px.toFloat()),
        ) { draw() }
        image.prepareToDraw()
        images[full] = image
        while (images.size > 160) images.remove(images.keys.first())
        return image
    }
}

/** A cached picture of [side], drawn once by [draw] for each [key]. */
@Composable
fun CachedThumb(key: String, side: Dp, modifier: Modifier = Modifier, draw: DrawScope.() -> Unit) {
    val density = LocalDensity.current
    val px = with(density) { side.roundToPx() }.coerceAtLeast(1)
    val image = remember(key, px) { ThumbCache.get(key, px, density.density, draw) }
    Image(image, contentDescription = null, modifier = modifier.size(side))
}