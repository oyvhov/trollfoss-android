package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Task
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.DesignIcons
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.delay

/**
 * The task board: three picture cards. Each shows what to do and where; a tap on the balloon flies
 * there. Done tasks get a big green stamp and a sticker. When all three are done, the dice deals three
 * new ones.
 */
@Composable
fun TasksScreen(vm: TrollfossViewModel) {
    val tasks = vm.sim.tasks
    val board = remember(vm.tasksLeft, vm.stickers) { tasks.board() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3B2A6B), Color(0xFF1F1840))))
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconCanvas(DesignIcons.Tasks, Modifier.size(52.dp))
                GameText(S.tasks.str(), fontSize = 34.sp, style = MaterialTheme.typography.displaySmall, color = Color.White)
                Row(
                    Modifier.background(T.Grape, RoundedCornerShape(50)).border(2.dp, T.Ink, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IconCanvas(DesignIcons.Sticker, Modifier.size(30.dp))
                    GameText("${vm.stickers}", fontSize = 22.sp, style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
            }
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                for (task in board) TaskCard(vm, task, tasks.progress(task), tasks.done(task))
                if (vm.tasksLeft == 0) {
                    RoundButton("Nye oppdrag", onClick = vm::newTasks, size = 96.dp, tone = Tones.Sun, icon = Icons.Dice)
                }
            }
        }
        CloseButton(onClick = vm::back, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun TaskCard(vm: TrollfossViewModel, task: Task, progress: Int, done: Boolean) {
    val stamp = remember(task.id) { Animatable(if (done) 1f else 0f) }
    LaunchedEffect(done) {
        if (done && stamp.value < 1f) {
            delay(200)
            stamp.snapTo(2.2f)
            stamp.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
        }
    }
    Column(
        Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (done) Color(0xFFE8FFF1) else T.Cream)
            .border(3.dp, T.Ink, RoundedCornerShape(26.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            CachedThumb("task:${task.id}", 150.dp) { drawTaskPicture(task) }
            if (stamp.value > 0.01f) {
                Canvas(Modifier.size(120.dp).graphicsLayer { scaleX = stamp.value; scaleY = stamp.value; alpha = (2.2f - stamp.value).coerceIn(0f, 1f) }) {
                    rotate(-14f) {
                        drawCircle(T.Mint.copy(alpha = 0.25f), size.minDimension / 2f)
                        drawCircle(T.MintDeep, size.minDimension / 2f - 4f, style = Stroke(8f))
                        val tick = Path().apply {
                            moveTo(size.width * 0.28f, size.height * 0.52f)
                            lineTo(size.width * 0.44f, size.height * 0.68f)
                            lineTo(size.width * 0.74f, size.height * 0.34f)
                        }
                        drawPath(tick, T.MintDeep, style = Stroke(16f, cap = StrokeCap.Round))
                    }
                }
            }
        }
        // Progress: one dot for each time it has to be done.
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0 until task.need) {
                Canvas(Modifier.size(20.dp)) {
                    drawCircle(if (i < progress) T.Sun else T.CreamDeep, size.minDimension / 2f)
                    drawCircle(Ink.line, size.minDimension / 2f - 1.5f, style = Stroke(3f))
                }
            }
        }
        val place = task.place
        if (place != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameText(S.place(place).str(), fontSize = 16.sp, style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (!done && place != vm.place) RoundButton(S.map.str(), onClick = { vm.travel(place) }, size = 44.dp, tone = Tones.Sea, icon = Icons.Map)
            }
        } else {
            // Anywhere at all.
            IconCanvas(Icons.Map, Modifier.height(36.dp).size(36.dp).graphicsLayer { alpha = 0.35f })
        }
    }
}

/** The picture on a task card: the furniture, the thing and the animal it is about, and a sign. */
fun DrawScope.drawTaskPicture(task: Task) {
    val s = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val fixture = task.fixture
    val thing = task.thing
    val species = task.species
    when {
        fixture != null && thing != null -> {
            drawFixtureThumb(fixture, 0, square(Offset(c.x - s * 0.08f, c.y - s * 0.04f), s * 0.8f))
            drawThingThumb(thing, 0, square(Offset(c.x + s * 0.3f, c.y + s * 0.3f), s * 0.36f))
        }
        fixture != null -> drawFixtureThumb(fixture, 0, square(c, s * 0.9f))
        species != null && thing != null -> {
            drawSpeciesThumb(species, square(Offset(c.x - s * 0.12f, c.y), s * 0.8f))
            drawThingThumb(thing, 0, square(Offset(c.x + s * 0.3f, c.y + s * 0.28f), s * 0.36f))
        }
        thing != null -> drawThingThumb(thing, if (thing == app.trollfoss.domain.ThingType.CROWN) 1 else 0, square(c, s * 0.8f))
    }
    when (task.icon) {
        "bring" -> sign(Offset(c.x + s * 0.34f, c.y - s * 0.32f), s * 0.2f) { Icons.Bag(this) }
        "star", "glimt" -> drawPath(starPath(if (fixture != null) Offset(c.x + s * 0.32f, c.y - s * 0.32f) else c, s * (if (fixture != null) 0.12f else 0.3f), s * (if (fixture != null) 0.05f else 0.13f)), T.Sun)
        "heart" -> sign(Offset(c.x - s * 0.3f, c.y - s * 0.3f), s * 0.2f) { heart(this) }
        "note" -> sign(Offset(c.x + s * 0.3f, c.y - s * 0.34f), s * 0.2f) { note(this) }
        "zzz" -> sign(Offset(c.x + s * 0.3f, c.y - s * 0.34f), s * 0.2f) { zzz(this) }
        "sneeze", "face", "burp" -> sign(Offset(c.x - s * 0.28f, c.y - s * 0.3f), s * 0.24f) { funny(this, task.icon) }
        "wish" -> { sign(c, s * 0.5f) { bubble(this) } }
        "broom" -> sign(c, s * 0.8f) { DesignIcons.Broom(this) }
        "roller" -> sign(c, s * 0.8f) { DesignIcons.Roller(this) }
        "sofa" -> sign(c, s * 0.8f) { DesignIcons.Sofa(this) }
        "camera" -> sign(c, s * 0.7f) { Icons.Camera(this) }
    }
}

/** Draws [draw] in a square of [side] centred on [c], as icons expect (they fill their canvas). */
private fun DrawScope.sign(c: Offset, side: Float, draw: DrawScope.() -> Unit) {
    val r = square(c, side)
    drawContext.transform.translate(r.left, r.top)
    val old = drawContext.size
    drawContext.size = androidx.compose.ui.geometry.Size(side, side)
    draw()
    drawContext.size = old
    drawContext.transform.translate(-r.left, -r.top)
}

private fun heart(d: DrawScope) = with(d) {
    val w = size.width
    val p = Path().apply {
        moveTo(w * 0.5f, w * 0.9f)
        cubicTo(w * 0.0f, w * 0.55f, w * 0.1f, w * 0.05f, w * 0.5f, w * 0.3f)
        cubicTo(w * 0.9f, w * 0.05f, w * 1.0f, w * 0.55f, w * 0.5f, w * 0.9f)
        close()
    }
    drawPath(p, T.Berry)
    drawPath(p, Ink.line, style = Stroke(w * 0.07f))
}

private fun note(d: DrawScope) = with(d) {
    val w = size.width
    drawOval(T.Grape, Offset(w * 0.1f, w * 0.62f), androidx.compose.ui.geometry.Size(w * 0.42f, w * 0.3f))
    drawLine(T.Grape, Offset(w * 0.5f, w * 0.75f), Offset(w * 0.5f, w * 0.08f), w * 0.1f, StrokeCap.Round)
    drawLine(T.Grape, Offset(w * 0.5f, w * 0.08f), Offset(w * 0.85f, w * 0.25f), w * 0.1f, StrokeCap.Round)
}

private fun zzz(d: DrawScope) = with(d) {
    val w = size.width
    for (k in 0..1) {
        val o = k * w * 0.4f
        val z = Path().apply {
            moveTo(w * 0.1f + o, w * 0.2f + o * 0.6f); lineTo(w * 0.45f + o, w * 0.2f + o * 0.6f)
            lineTo(w * 0.1f + o, w * 0.55f + o * 0.6f); lineTo(w * 0.45f + o, w * 0.55f + o * 0.6f)
        }
        drawPath(z, T.Grape, style = Stroke(w * 0.09f, cap = StrokeCap.Round))
    }
}

private fun bubble(d: DrawScope) = with(d) {
    val w = size.width
    drawCircle(Color.White, w * 0.4f, Offset(w * 0.55f, w * 0.42f))
    drawCircle(Ink.line, w * 0.4f, Offset(w * 0.55f, w * 0.42f), style = Stroke(w * 0.04f))
    drawCircle(Color.White, w * 0.08f, Offset(w * 0.16f, w * 0.88f))
    drawCircle(Ink.line, w * 0.08f, Offset(w * 0.16f, w * 0.88f), style = Stroke(w * 0.03f))
    drawPath(starPath(Offset(w * 0.55f, w * 0.44f), w * 0.2f, w * 0.09f), T.Sun)
}

/** A funny face for the slapstick tasks: sneezing, cream or a burp. */
private fun funny(d: DrawScope, kind: String?) = with(d) {
    val w = size.width
    val c = Offset(w / 2f, w / 2f)
    drawCircle(Color(0xFFFFD2B0), w * 0.45f, c)
    drawCircle(Ink.line, w * 0.45f, c, style = Stroke(w * 0.05f))
    when (kind) {
        "sneeze" -> {
            drawLine(Ink.line, Offset(w * 0.3f, w * 0.4f), Offset(w * 0.42f, w * 0.44f), w * 0.06f, StrokeCap.Round)
            drawLine(Ink.line, Offset(w * 0.7f, w * 0.4f), Offset(w * 0.58f, w * 0.44f), w * 0.06f, StrokeCap.Round)
            drawOval(Ink.line, Offset(w * 0.4f, w * 0.58f), androidx.compose.ui.geometry.Size(w * 0.2f, w * 0.2f))
            for (k in 0 until 4) drawCircle(Color(0xFF9ADAFF), w * 0.04f, Offset(w * (0.2f + k * 0.2f), w * 0.95f))
        }
        "face" -> for ((x, y) in listOf(0.3f to 0.3f, 0.65f to 0.35f, 0.45f to 0.65f, 0.7f to 0.7f)) drawCircle(Color.White, w * 0.14f, Offset(w * x, w * y))
        else -> {
            drawCircle(Ink.line, w * 0.05f, Offset(w * 0.36f, w * 0.42f))
            drawCircle(Ink.line, w * 0.05f, Offset(w * 0.64f, w * 0.42f))
            drawOval(Color(0xFF7A2440), Offset(w * 0.38f, w * 0.58f), androidx.compose.ui.geometry.Size(w * 0.24f, w * 0.16f))
            drawCircle(Color.White.copy(alpha = 0.8f), w * 0.1f, Offset(w * 0.85f, w * 0.2f))
        }
    }
}
