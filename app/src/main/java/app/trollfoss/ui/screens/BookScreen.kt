package app.trollfoss.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Look
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Recipe
import app.trollfoss.domain.Recipes
import app.trollfoss.domain.Secrets
import app.trollfoss.domain.Species
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawFixtureBack
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.art.drawThing
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import java.io.File
import kotlin.math.max

private enum class Tab { SECRETS, RECIPES, STICKERS, PHOTOS }

/** The discovery book: glimt found per place, recipes made, and photos taken. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookScreen(vm: TrollfossViewModel) {
    var tab by remember { mutableStateOf(Tab.SECRETS) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3B2F7A), Color(0xFF1D1A4A)))),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                GameText(S.book.str(), fontSize = 30.sp, style = MaterialTheme.typography.headlineLarge)
                Box(Modifier.size(12.dp))
                for (t in Tab.entries) {
                    val label = when (t) {
                        Tab.SECRETS -> "${S.secrets.str()} ${vm.found}/${Secrets.all.size}"
                        Tab.RECIPES -> "${S.recipes.str()} ${vm.discoveries}/${Recipes.book.size}"
                        Tab.STICKERS -> "${S.stickers.str()} ${vm.stickers}"
                    Tab.PHOTOS -> "${S.photos.str()} ${vm.photos.size}"
                    }
                    TabChip(label, selected = tab == t) { tab = t }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when (tab) {
                    Tab.SECRETS -> SecretsPage(vm)
                    Tab.RECIPES -> RecipesPage(vm)
                    Tab.STICKERS -> StickersPage(vm)
                Tab.PHOTOS -> PhotosPage(vm)
                }
            }
        }
        CloseButton({ vm.back() }, Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) T.Sun else T.NightTop)
            .border(2.5.dp, T.Ink, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        GameText(label, fontSize = 17.sp, style = MaterialTheme.typography.titleMedium, color = if (selected) Color.White else T.Muted)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecretsPage(vm: TrollfossViewModel) {
    FlowRow(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 60.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Fourteen places: five small cards a row, so the whole village fits on a phone.
        for (place in PlaceId.entries) {
            val secrets = Secrets.inPlace(place)
            Column(
                Modifier
                    .width(138.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(T.Cream)
                    .border(2.5.dp, T.Ink, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                GameText(S.place(place).str(), fontSize = 16.sp, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (s in secrets) {
                        val got = s.id in vm.world.found
                        Canvas(Modifier.size(36.dp)) {
                            val star = starPath(center, size.minDimension * 0.46f, size.minDimension * 0.2f)
                            if (got) {
                                drawPath(star, T.Sun)
                                drawPath(star, Ink.line, style = Stroke(3.5f))
                            } else {
                                drawPath(star, T.CreamDeep)
                                drawPath(star, T.CreamLine, style = Stroke(3.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** What each sticker shows, in the order they are earned. */
private val STICKER_ART = listOf(
    ThingType.STAR_JAR to 0, ThingType.PLANET to 2, ThingType.CROWN to 1, ThingType.TEDDY to 0, ThingType.DUCK to 0,
    ThingType.ICE_CREAM to 3, ThingType.PLANET to 6, ThingType.CUPCAKE to 1, ThingType.BALLOON to 2, ThingType.ROCKET to 0,
    ThingType.GEM to 2, ThingType.STARFISH to 0, ThingType.FLOWER to 2, ThingType.LOLLIPOP to 1, ThingType.PEARL to 0,
    ThingType.DRAGON_EGG to 0, ThingType.GUITAR to 0, ThingType.CANDY_FLOSS to 0, ThingType.WATERMELON to 0, ThingType.PLANET to 4,
    ThingType.MUSHROOM to 0, ThingType.SHELL to 0, ThingType.WAND to 0, ThingType.CLOUDBERRY to 0, ThingType.POPCORN to 0,
    ThingType.PLANET to 5, ThingType.GIFT to 3, ThingType.SPACE_HELMET to 0, ThingType.BIRDHOUSE to 0, ThingType.WHOOPEE to 0,
)

/** The sticker album: one round sticker per task done, and empty places for the next ones. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickersPage(vm: TrollfossViewModel) {
    val count = vm.stickers
    FlowRow(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 60.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (i in 0 until maxOf(12, count + 6)) {
            Canvas(Modifier.size(86.dp)) {
                val r = size.minDimension / 2f - 4f
                if (i < count) {
                    val (type, variant) = STICKER_ART[i % STICKER_ART.size]
                    val tint = listOf(T.SunTop, T.MintTop, T.SeaTop, T.BerryTop, T.GrapeTop)[i % 5]
                    drawCircle(Color.White, r + 3f, center)
                    drawCircle(tint, r - 3f, center)
                    drawCircle(Ink.line, r + 3f, center, style = Stroke(4f))
                    drawThingThumb(type, variant, square(center, r * 1.3f))
                    drawOval(Color.White.copy(alpha = 0.4f), Offset(center.x - r * 0.6f, center.y - r * 0.8f), Size(r * 0.7f, r * 0.35f))
                } else {
                    drawCircle(T.CreamDeep.copy(alpha = 0.35f), r, center)
                    drawCircle(T.CreamLine.copy(alpha = 0.6f), r, center, style = Stroke(4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipesPage(vm: TrollfossViewModel) {
    FlowRow(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 60.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (r in Recipes.book) {
            val known = r.key in vm.world.discoveries
            Box(
                Modifier
                    .size(236.dp, 86.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (known) T.Cream else T.NightTop)
                    .border(2.5.dp, T.Ink, RoundedCornerShape(20.dp)),
            ) {
                Canvas(Modifier.fillMaxSize().padding(8.dp)) { drawRecipe(r, known) }
            }
        }
    }
}

/** Ingredients, the machine and the result in a row; unknown recipes show only shadows. */
private fun DrawScope.drawRecipe(r: Recipe, known: Boolean) {
    val pen = Pen(max(1.5f, size.height * 0.03f))
    val slot = size.height * 0.8f
    val silhouette = Paint().apply { colorFilter = ColorFilter.tint(Color(0xFF16133F), BlendMode.SrcIn) }
    fun item(type: ThingType, variant: Int, x: Float, dim: Boolean) {
        val k = slot / max(type.w, type.h)
        translate(x, size.height * 0.92f) {
            if (dim) drawContext.canvas.saveLayer(Rect(-slot, -slot * 1.2f, slot, slot * 0.2f), silhouette)
            drawThing(type, variant, 0, type.w * k * 0.9f, type.h * k * 0.9f, pen)
            if (dim) drawContext.canvas.restore()
        }
    }
    var x = slot * 0.5f
    for ((i, input) in r.inputs.withIndex()) {
        item(input, 0, x, !known)
        x += slot * (if (i < r.inputs.size - 1) 0.85f else 0.75f)
    }
    // The machine, small.
    val fixture = Fixture(0, PlaceId.HOME, r.machine, 0f, 0f)
    val m = slot * 0.9f / max(fixture.spec.w, fixture.spec.h)
    translate(x + slot * 0.2f, size.height * 0.95f) { drawFixtureBack(fixture, m, Pen(pen.lw * 0.7f)) }
    x += slot * 0.95f
    drawLine(if (known) T.InkSoft else T.Muted, Offset(x - slot * 0.15f, size.height / 2), Offset(x + slot * 0.1f, size.height / 2), strokeWidth = pen.lw * 1.4f)
    x += slot * 0.5f
    if (r.key == "pot_pet" || r.key == "fire_DRAGON_EGG") {
        translate(x, size.height * 0.95f) {
            if (!known) drawContext.canvas.saveLayer(Rect(-slot, -slot * 1.2f, slot, slot * 0.2f), silhouette)
            drawPerson(if (r.key == "pot_pet") Species.CAT else Species.DRAGON, Look(skin = 0), Pose.STAND, PersonAnim(), slot * 0.9f, pen)
            if (!known) drawContext.canvas.restore()
        }
    } else {
        item(r.result.type, r.result.variant, x, !known)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotosPage(vm: TrollfossViewModel) {
    var open by remember { mutableStateOf<File?>(null) }
    FlowRow(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = 60.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (file in vm.photos) {
            val bitmap = remember(file) {
                runCatching { BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap() }.getOrNull()
            } ?: continue
            Image(
                bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp, 112.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(4.dp, Color.White, RoundedCornerShape(14.dp))
                    .clickable { open = file },
            )
        }
    }
    open?.let { file ->
        val big = remember(file) { runCatching { BitmapFactory.decodeFile(file.path)?.asImageBitmap() }.getOrNull() }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { open = null }, contentAlignment = Alignment.Center) {
            if (big != null) Image(big, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(24.dp))
        }
    }
}
