package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import app.trollfoss.domain.Styles
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.argb
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.components.BigButton
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.LocalFeedback
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

/** One part of a figure the workshop can change. */
private enum class Part(val label: app.trollfoss.domain.Txt, val count: Int, val color: Boolean) {
    SKIN(S.catSkin, Palette.skins.size, true),
    HEIGHT(S.catHeight, Styles.HEIGHTS.size, false),
    HAIR(S.catHair, Styles.HAIRS, false),
    HAIR_COLOR(S.catHairColor, Palette.hairs.size, true),
    EYES(S.catEyes, Styles.EYES, false),
    EARS(S.catEars, Styles.EARS, false),
    TOP(S.catTop, Styles.TOPS, false),
    TOP_COLOR(S.catTopColor, Palette.cloth.size, true),
    BOTTOM(S.catBottom, Styles.BOTTOMS, false),
    BOTTOM_COLOR(S.catBottomColor, Palette.cloth.size, true),
    SHOES(S.catShoes, Palette.cloth.size, true),
    EXTRA(S.catExtra, Styles.EXTRAS, false),
}

private fun Look.with(part: Part, i: Int): Look = when (part) {
    Part.SKIN -> copy(skin = i)
    Part.HEIGHT -> copy(height = Styles.HEIGHTS[i])
    Part.HAIR -> copy(hair = i)
    Part.HAIR_COLOR -> copy(hairColor = i)
    Part.EYES -> copy(eyes = i)
    Part.EARS -> copy(ears = i)
    Part.TOP -> copy(top = i)
    Part.TOP_COLOR -> copy(topColor = i)
    Part.BOTTOM -> copy(bottom = i)
    Part.BOTTOM_COLOR -> copy(bottomColor = i)
    Part.SHOES -> copy(shoes = i)
    Part.EXTRA -> copy(extra = i)
}

private fun Look.index(part: Part): Int = when (part) {
    Part.SKIN -> skin
    Part.HEIGHT -> Styles.HEIGHTS.indexOfFirst { kotlin.math.abs(it - height) < 0.01f }
    Part.HAIR -> hair
    Part.HAIR_COLOR -> hairColor
    Part.EYES -> eyes
    Part.EARS -> ears
    Part.TOP -> top
    Part.TOP_COLOR -> topColor
    Part.BOTTOM -> bottom
    Part.BOTTOM_COLOR -> bottomColor
    Part.SHOES -> shoes
    Part.EXTRA -> extra
}

private fun Part.swatch(i: Int): Color = when (this) {
    Part.SKIN -> argb(Palette.skins[i])
    Part.HAIR_COLOR -> argb(Palette.hairs[i])
    else -> argb(Palette.cloth[i])
}

/** Which crop of the figure a style preview shows. */
private enum class Crop { HEAD, BODY, WHOLE }

private fun Part.crop(): Crop = when (this) {
    Part.HAIR, Part.EYES, Part.EARS, Part.EXTRA, Part.HAIR_COLOR, Part.SKIN -> Crop.HEAD
    Part.TOP, Part.TOP_COLOR -> Crop.BODY
    else -> Crop.WHOLE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorScreen(vm: TrollfossViewModel, startId: Int?) {
    val folk = remember { vm.folk() }
    var editId by remember { mutableStateOf(startId) }
    var look by remember { mutableStateOf(startId?.let { id -> folk.firstOrNull { it.id == id }?.look } ?: Look.random(Random.Default)) }
    var name by remember { mutableStateOf(startId?.let { id -> folk.firstOrNull { it.id == id }?.name } ?: vm.freeName(look)) }
    var part by remember { mutableStateOf(Part.HAIR) }
    val anim = remember { PersonAnim() }
    var t by remember { mutableFloatStateOf(0f) }
    val spin = remember { Animatable(0f) }
    val hop = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val feedback = LocalFeedback.current
    var reactions by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        var last = start
        var nextBlink = 2f
        while (true) withFrameNanos { now ->
            val dt = (now - last) / 1e9f
            last = now
            t = (now - start) / 1e9f
            nextBlink -= dt
            if (nextBlink <= 0f) {
                anim.blink = 0.13f
                nextBlink = 2f + Random.nextFloat() * 3f
            }
            anim.blink = max(0f, anim.blink - dt)
            anim.faceTime -= dt
            if (anim.faceTime <= 0f) anim.face = Face.HAPPY
            anim.wave = max(0f, anim.wave - dt)
            anim.lookX = kotlin.math.sin(t * 0.7f) * 0.6f
            anim.lookY = 0.15f
        }
    }

    fun react(big: Boolean) {
        reactions++
        anim.face = if (big) Face.WOW else if (reactions % 2 == 0) Face.GRIN else Face.LAUGH
        anim.faceTime = 1f
        feedback.sfx(Sfx.GIGGLE, 0.6f, Look.voiceFor(look, Random(reactions)))
        scope.launch {
            hop.snapTo(0f)
            hop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        }
        if (big) scope.launch {
            spin.snapTo(0f)
            spin.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFFFFF4E2), Color(0xFFFFD7B0)), radius = 1600f)),
    ) {
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // The figure on its little stage, with its name below.
            Column(Modifier.weight(0.42f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.fillMaxWidth().weight(1f).semantics { contentDescription = name }) {
                    val pen = Pen(max(2f, size.height * 0.006f), t)
                    val floor = size.height * 0.94f
                    drawOval(Color(0x332B2140), Offset(size.width / 2 - size.height * 0.2f, floor - size.height * 0.03f), androidx.compose.ui.geometry.Size(size.height * 0.4f, size.height * 0.06f))
                    val h = size.height * 0.82f * (look.height / 1.14f) * (if (look.ears == 2) 0.86f else 1f)
                    val jump = (1f - hop.value) * 0f + kotlin.math.sin(hop.value * Math.PI.toFloat()) * size.height * 0.05f
                    translate(size.width / 2, floor - jump) {
                        val turn = kotlin.math.cos(spin.value * 2f * Math.PI.toFloat())
                        drawContext.transform.scale(if (spin.value in 0.01f..0.99f) turn else 1f, 1f, Offset.Zero)
                        drawPerson(Species.FOLK, look, Pose.STAND, anim, h, pen, seed = 0f)
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    singleLine = true,
                    label = { Text(S.namePlaceholder.str()) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = T.Ink,
                        unfocusedTextColor = T.Ink,
                        focusedBorderColor = T.Grape,
                        unfocusedBorderColor = T.InkSoft,
                        focusedLabelColor = T.Grape,
                        unfocusedLabelColor = T.InkSoft,
                        cursorColor = T.Grape,
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }

            Column(Modifier.weight(0.58f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Who: every figure, and a new one.
                Row(
                    Modifier.fillMaxWidth().padding(end = 64.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipHead(null, selected = editId == null, label = S.newFigure.str()) {
                        editId = null
                        look = Look.random(Random.Default)
                        name = vm.freeName(look)
                        react(true)
                    }
                    for (p in folk) {
                        ChipHead(p.look, selected = editId == p.id, label = p.name) {
                            editId = p.id
                            look = p.look
                            name = p.name
                            react(false)
                        }
                    }
                }
                // What: the parts.
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (p in Part.entries) {
                        PartTab(p, look, selected = p == part) {
                            part = p
                            feedback.sfx(Sfx.TAP)
                        }
                    }
                }
                // Choices for that part.
                FlowRow(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val current = look.index(part)
                    for (i in 0 until part.count) {
                        Choice(part, i, look, selected = i == current) {
                            look = look.with(part, i).safe()
                            react(part == Part.TOP || part == Part.HEIGHT)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundButton(S.random.str(), onClick = {
                        look = Look.random(Random.Default)
                        if (editId == null) name = vm.freeName(look)
                        feedback.sfx(Sfx.MAGIC, 0.7f)
                        react(true)
                    }, size = 64.dp, tone = Tones.Grape, icon = Icons.Dice)
                    BigButton(S.done.str(), onClick = { vm.saveFigure(editId, look, name) }, tone = Tones.Mint, icon = Icons.Check, modifier = Modifier.weight(1f))
                }
            }
        }
        CloseButton({ vm.back() }, Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

@Composable
private fun ChipHead(look: Look?, selected: Boolean, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (selected) T.Sun else Color.White)
                .border(if (selected) 3.5.dp else 2.dp, T.Ink, CircleShape)
                .clickable(onClick = onClick)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) {
            if (look == null) {
                Canvas(Modifier.size(30.dp)) {
                    drawLine(T.Ink, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = size.width * 0.28f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    drawLine(T.Ink, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = size.width * 0.28f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    drawLine(T.Mint, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = size.width * 0.14f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    drawLine(T.Mint, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = size.width * 0.14f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            } else {
                Canvas(Modifier.fillMaxSize()) { drawCrop(look, Crop.HEAD, 0f) }
            }
        }
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = T.Ink, maxLines = 1)
    }
}

@Composable
private fun PartTab(part: Part, look: Look, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) T.Sun else Color.White)
            .border(if (selected) 3.dp else 2.dp, T.Ink, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = part.label.get(app.trollfoss.domain.Maalform.NYNORSK) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            if (part.color) {
                drawCircle(part.swatch(look.index(part).coerceAtLeast(0)), size.minDimension * 0.34f)
                drawCircle(T.Ink, size.minDimension * 0.34f, style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
                drawCircle(Color.White.copy(alpha = 0.6f), size.minDimension * 0.08f, Offset(size.width * 0.4f, size.height * 0.38f))
            }
            if (!part.color || part == Part.SKIN) drawCrop(look, part.crop(), 0f, small = true)
        }
    }
}

@Composable
private fun Choice(part: Part, i: Int, look: Look, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(if (part.color) 40.dp else 18.dp)
    val size = if (part.color) 58.dp else 84.dp
    Box(
        Modifier
            .size(size)
            .graphicsLayer { if (selected) { scaleX = 1.06f; scaleY = 1.06f } }
            .clip(shape)
            .background(if (part.color) part.swatch(i) else if (selected) T.SunTop else Color.White)
            .border(if (selected) 4.dp else 2.dp, if (selected) T.Grape else T.Ink, shape)
            .clickable(onClick = onClick),
    ) {
        if (!part.color) Canvas(Modifier.fillMaxSize()) { drawCrop(look.with(part, i).safe(), part.crop(), 0f) }
        else if (selected) Canvas(Modifier.fillMaxSize()) { Icons.Check(this) }
    }
}

/** Draws the figure cropped to its head, its body or all of it, to fit this canvas. */
private fun DrawScope.drawCrop(look: Look, crop: Crop, t: Float, small: Boolean = false) {
    val a = PersonAnim()
    val pen = Pen(max(1.2f, size.minDimension * (if (small) 0.03f else 0.022f)), t)
    clipRect {
        when (crop) {
            Crop.HEAD -> {
                val h = size.height * 1.55f
                translate(size.width / 2, size.height * 0.5f + h * 0.7f) { drawPerson(Species.FOLK, look, Pose.STAND, a, h, pen) }
            }
            Crop.BODY -> {
                val h = size.height * 1.25f
                translate(size.width / 2, size.height * 0.5f + h * 0.4f) { drawPerson(Species.FOLK, look, Pose.STAND, a, h, pen) }
            }
            Crop.WHOLE -> {
                val h = size.height * 0.9f * (look.height / 1.14f)
                translate(size.width / 2, size.height * 0.95f) { drawPerson(Species.FOLK, look, Pose.STAND, a, h, pen) }
            }
        }
    }
}

