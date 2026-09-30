package app.trollfoss.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.Secrets
import app.trollfoss.domain.Weather
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.Screen
import app.trollfoss.ui.components.DesignIcons
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The scene of the current place, with its buttons in the corners. */
@Composable
fun PlayScreen(vm: TrollfossViewModel) {
    val motion = LocalMotion.current
    val place = vm.place
    val engine = remember(place, vm.generation) { vm.engineFor(place, motion) }
    val tick = remember { mutableLongStateOf(0L) }
    val text = rememberTextMeasurer()
    val density = LocalDensity.current.density
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    LaunchedEffect(engine) {
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                last = now
                engine.update(dt)
                tick.longValue = now
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                }
                .pointerInput(engine) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                when {
                                    change.changedToDown() -> engine.down(change.id.value, change.position, change.uptimeMillis)
                                    change.changedToUp() -> engine.up(change.id.value, change.position, change.uptimeMillis)
                                    change.pressed && change.positionChanged() -> engine.move(change.id.value, change.position, change.uptimeMillis)
                                }
                                change.consume()
                            }
                        }
                    }
                },
        ) {
            tick.longValue
            engine.setSize(size.width, size.height, density)
            engine.draw(this, text)
        }

        // Top left: the map and the task board, with a badge for tasks still to do.
        Row(Modifier.align(Alignment.TopStart).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoundButton(S.map.str(), onClick = { vm.open(Screen.Map) }, tone = Tones.Sea, icon = Icons.Map)
            Box {
                RoundButton(S.tasks.str(), onClick = { vm.open(Screen.Tasks) }, tone = Tones.Sun, icon = DesignIcons.Tasks)
                if (vm.tasksLeft > 0) {
                    Box(
                        Modifier.align(Alignment.TopEnd).size(26.dp).background(T.Berry, androidx.compose.foundation.shape.CircleShape).border(2.dp, T.Ink, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { GameText("${vm.tasksLeft}", fontSize = 14.sp, style = MaterialTheme.typography.titleMedium, color = Color.White) }
                }
            }
        }

        // Top right: found glimt, day and night, weather and the camera (the designer panel takes their place).
        if (!engine.designMode) Row(
            Modifier.align(Alignment.TopEnd).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlimtCounter(vm.found, Secrets.all.size, Modifier.onGloballyPositioned { coords ->
                val b = coords.boundsInRoot()
                engine.counterTarget = Offset(b.left + 22.dp.value * density, b.center.y)
            })
            RoundButton(if (vm.night) S.day.str() else S.night.str(), onClick = vm::toggleNight, tone = Tones.Night, icon = if (vm.night) Icons.Sun else Icons.Moon)
            RoundButton(S.weather.str(), onClick = vm::cycleWeather, tone = Tones.Cream, icon = when (vm.weather) {
                Weather.SUN -> Icons.SunCloud
                Weather.RAIN -> Icons.Rain
                Weather.SNOW -> Icons.Snow
            })
            RoundButton(S.camera.str(), onClick = {
                scope.launch {
                    val image = layer.toImageBitmap()
                    engine.photoFlash()
                    vm.savePhoto(image)
                }
            }, tone = Tones.Cream, tapSound = false, icon = Icons.Camera)
        }

        // Bottom left: the figure workshop and the home designer. The bag, bottom right, is drawn by the engine.
        if (!engine.designMode) {
            Row(Modifier.align(Alignment.BottomStart).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundButton(S.workshop.str(), onClick = { vm.open(Screen.Creator(null)) }, tone = Tones.Grape, icon = Icons.Workshop)
                RoundButton(S.designer.str(), onClick = { engine.designMode = true }, tone = Tones.Berry, icon = DesignIcons.Roller)
            }
        }
        AnimatedVisibility(
            visible = engine.designMode,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            DesignerPanel(engine, vm.world, place, onClose = {
                engine.designMode = false
                engine.storeZone = null
                vm.scheduleSave()
            })
        }

        PlaceBanner(place.let { S.place(it).str() }, key = place)

        TaskBanner(vm)

        if (vm.telescopeOpen) TelescopeView(night = vm.night, onClose = { vm.telescopeOpen = false })
    }
}

@Composable
private fun GlimtCounter(found: Int, total: Int, modifier: Modifier = Modifier) {
    val bump = remember { Animatable(1f) }
    LaunchedEffect(found) {
        if (found > 0) {
            delay(650)
            bump.snapTo(1.35f)
            bump.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
        }
    }
    Row(
        modifier
            .graphicsLayer { scaleX = bump.value; scaleY = bump.value }
            .background(T.Cream.copy(alpha = 0.94f), RoundedCornerShape(50))
            .border(2.5.dp, T.Ink, RoundedCornerShape(50))
            .padding(start = 8.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(Modifier.size(30.dp)) { Icons.Star(this) }
        GameText("$found", fontSize = 22.sp, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = T.Sun)
        GameText("/ $total", fontSize = 15.sp, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
    }
}

/** A finished task drops in from the top with its picture, a stamp and a sticker, then flies away. */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.TaskBanner(vm: TrollfossViewModel) {
    val task = vm.taskDone
    var shown by remember { mutableStateOf<app.trollfoss.domain.Task?>(null) }
    LaunchedEffect(task) {
        if (task != null) {
            shown = task
            delay(2600)
            shown = null
            vm.taskDone = null
        }
    }
    AnimatedVisibility(
        visible = shown != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
    ) {
        val t = shown ?: task
        Row(
            Modifier.background(T.Cream, RoundedCornerShape(28.dp)).border(3.dp, T.Ink, RoundedCornerShape(28.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (t != null) Canvas(Modifier.size(84.dp)) { drawTaskPicture(t) }
            IconCanvas(Icons.Check, Modifier.size(56.dp))
            IconCanvas(DesignIcons.Sticker, Modifier.size(56.dp))
        }
    }
}

/** The place's name floats in at the top for a moment when you arrive. */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.PlaceBanner(name: String, key: Any) {
    var visible by remember(key) { mutableStateOf(true) }
    LaunchedEffect(key) {
        visible = true
        delay(2200)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(420), initialScale = 0.6f),
        exit = fadeOut(tween(600)),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 92.dp),
    ) {
        GameText(name, fontSize = 40.sp, style = MaterialTheme.typography.displayMedium, color = Color.White)
    }
}
