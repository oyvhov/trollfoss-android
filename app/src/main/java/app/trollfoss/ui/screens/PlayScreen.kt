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
import app.trollfoss.domain.PlaceId
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
    val engine = remember(place, vm.generation, vm.season, vm.festival) { vm.engineFor(place, motion) }
    engine.emptyBagHint = S.emptyBagHint.str()
    val tick = remember { mutableLongStateOf(0L) }
    val text = rememberTextMeasurer()
    val density = LocalDensity.current.density
    val layer = rememberGraphicsLayer()
    var capturePhoto by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val compact = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 520

    val graphics = androidx.compose.ui.platform.LocalGraphicsContext.current
    androidx.compose.runtime.DisposableEffect(engine, graphics) {
        engine.attach(graphics)
        onDispose { engine.detach() }
    }

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

    // Mitt hus: the builder panel follows the open flag; build mode (frames with a plus) is on while it is open.
    val mineUi = vm.mineUi
    val mineVersion = vm.mineVersion
    LaunchedEffect(mineUi.open, place, engine.designMode) {
        vm.sim.mine.setBuildMode(mineUi.open && place.mine && !engine.designMode, place)
    }
    LaunchedEffect(mineVersion) {
        if (vm.world.mine.wantPanel) {
            vm.world.mine.wantPanel = false
            mineUi.open = true
        }
    }
    LaunchedEffect(engine.designMode) { if (engine.designMode) mineUi.open = false }

    val btn = if (compact) 44.dp else 64.dp
    val edge = if (compact) 8.dp else 16.dp
    val gap = if (compact) 8.dp else 12.dp
    var menuOpen by remember { mutableStateOf(false) }
    var friendsOpen by remember { mutableStateOf(false) }
    val builderOpen = place.mine && mineUi.open && !engine.designMode && vm.world.mine.job == null
    val selectedBuildRoom = remember(mineVersion, place) {
        vm.world.mine.let { if (it.selectedPlace == place) it.selected else -1 }
    }
    LaunchedEffect(builderOpen, selectedBuildRoom, place) {
        if (builderOpen && selectedBuildRoom >= 0 && place != PlaceId.MINE_YARD) {
            withFrameNanos { }
            engine.selectRoom(selectedBuildRoom)
        }
    }
    val panelWidth = when {
        engine.designMode -> playPanelWidth(compact, designer = true)
        builderOpen -> playPanelWidth(compact, designer = false)
        else -> 0.dp
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (capturePhoto) {
                        layer.record { this@drawWithContent.drawContent() }
                        drawLayer(layer)
                    } else drawContent()
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
            engine.setSize(size.width, size.height, density, panelWidth.toPx())
            engine.draw(this, text)
        }

        // The controls are small on a phone (a screen less than about 520 dp tall) and big on a tablet.
        engine.compact = compact

        Box(Modifier.fillMaxSize().padding(end = panelWidth)) {

        // In the big house: the five golden keys, top and centre.
        if (place.manor && !engine.designMode) {
            HouseKeysHud(vm.houseKeys, modifier = Modifier.align(Alignment.TopCenter).padding(top = if (compact) 8.dp else 18.dp), compact = compact)
        }

        // Taking stairs, a lift or a slide in the big house: the new floor opens from the dark, like an eye.
        val curtain = remember { androidx.compose.animation.core.Animatable(1f) }
        LaunchedEffect(vm.passageStamp) {
            if (vm.passageStamp > 0) {
                curtain.snapTo(0f)
                curtain.animateTo(1f, androidx.compose.animation.core.tween(560))
            }
        }
        if (curtain.value < 1f) {
            Box(Modifier.fillMaxSize().background(Color(0xFF1F1830).copy(alpha = (1f - curtain.value) * (1f - curtain.value) * 0.92f)))
        }

        // The controls. On a phone (a screen less than about 520 dp tall) they are small and the less common ones
        // sit behind one button, so the scene has the room; on a tablet they stay big and in the corners.
        LaunchedEffect(place, engine.designMode) { menuOpen = false }

        // Top left: the map and the task board, with a badge for tasks still to do.
        Row(Modifier.align(Alignment.TopStart).padding(edge), horizontalArrangement = Arrangement.spacedBy(gap)) {
            RoundButton(S.map.str(), onClick = { vm.open(Screen.Map) }, size = btn, tone = Tones.Sea, icon = Icons.Map)
            Box {
                RoundButton(S.tasks.str(), onClick = { vm.open(Screen.Tasks) }, size = btn, tone = Tones.Sun, icon = DesignIcons.Tasks)
                if (vm.tasksLeft > 0) {
                    val badge = if (compact) 20.dp else 26.dp
                    Box(
                        Modifier.align(Alignment.TopEnd).size(badge).background(T.Berry, androidx.compose.foundation.shape.CircleShape).border(2.dp, T.Ink, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { GameText("${vm.tasksLeft}", fontSize = if (compact) 11.sp else 14.sp, style = MaterialTheme.typography.titleMedium, color = Color.White) }
                }
            }
        }

        RoundButton(S.friends.str(), onClick = { engine.cancel(); friendsOpen = true }, size = btn, tone = Tones.Mint,
            modifier = Modifier.align(Alignment.TopStart).padding(start = edge + (btn + gap) * 2, top = edge), icon = Icons.Friends)
        RoundButton(S.players.str(), onClick = { vm.open(Screen.Players) }, size = btn, tone = Tones.Sun,
            modifier = Modifier.align(Alignment.TopStart).padding(start = edge + (btn + gap) * 3, top = edge), icon = Icons.Friends)
        PlayerBar(vm, compact, Modifier.align(Alignment.TopStart).padding(start = edge, top = edge + btn + 6.dp))

        if (place == app.trollfoss.domain.PlaceId.LAB) {
            var tunnelLabelVisible by remember(place) { mutableStateOf(false) }
            LaunchedEffect(place) { delay(2800); tunnelLabelVisible = true }
            if (tunnelLabelVisible) {
                GameText(S.secretTunnel.str(), fontSize = if (compact) 13.sp else 16.sp, color = T.Ink,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = edge + btn + 8.dp)
                        .background(T.Cream, RoundedCornerShape(18.dp)).border(2.dp, T.Ink, RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }

        val photo = {
            scope.launch {
                if (!capturePhoto) {
                    capturePhoto = true
                    try {
                        // Capture one complete frame; ordinary play avoids an extra recording layer.
                        withFrameNanos { }
                        withFrameNanos { }
                        vm.savePhoto(layer.toImageBitmap())
                        engine.photoFlash()
                    } finally { capturePhoto = false }
                }
            }
            Unit
        }
        val weatherIcon: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit = when (vm.weather) {
            Weather.SUN -> Icons.SunCloud
            Weather.RAIN -> Icons.Rain
            Weather.SNOW -> Icons.Snow
        }
        val counter = Modifier.onGloballyPositioned { coords ->
            val b = coords.boundsInRoot()
            engine.counterTarget = Offset(b.left + (if (compact) 14.dp else 22.dp).value * density, b.center.y)
        }

        if (!engine.designMode && !compact) {
            // Tablet: found glimt, day and night, weather and the camera top right; the workshop and the designer bottom left.
            Row(
                Modifier.align(Alignment.TopEnd).padding(edge),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlimtCounter(vm.found, Secrets.all.size, counter)
                RoundButton(if (vm.night) S.day.str() else S.night.str(), onClick = vm::toggleNight, tone = Tones.Night, icon = if (vm.night) Icons.Sun else Icons.Moon)
                RoundButton(S.weather.str(), onClick = vm::cycleWeather, tone = Tones.Cream, icon = weatherIcon)
                RoundButton(S.camera.str(), onClick = { photo() }, tone = Tones.Cream, tapSound = false, icon = Icons.Camera)
            }
            Row(Modifier.align(Alignment.BottomStart).padding(edge), horizontalArrangement = Arrangement.spacedBy(gap)) {
                RoundButton(S.workshop.str(), onClick = { vm.open(Screen.Creator(null)) }, tone = Tones.Grape, icon = Icons.Workshop)
                if (place.mine) RoundButton(app.trollfoss.ui.SM.build.str(), onClick = { mineUi.open = !mineUi.open }, tone = if (mineUi.open) Tones.Sun else Tones.Mint, icon = BuildIcons.Hammer)
                if (place.mine) RoundButton(app.trollfoss.ui.SM.paint.str(), onClick = {
                    mineUi.tab = 2; mineUi.open = true
                    if (place != app.trollfoss.domain.PlaceId.MINE_YARD) vm.travel(app.trollfoss.domain.PlaceId.MINE_YARD)
                }, tone = Tones.Berry, icon = DesignIcons.Roller)
            }
        }
        if (!engine.designMode && compact) {
            // Phone: a small counter and one button; the rest unfolds from it.
            Row(
                Modifier.align(Alignment.TopEnd).padding(edge),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlimtCounter(vm.found, Secrets.all.size, counter, compact = true)
                RoundButton(
                    S.more.str(), onClick = { menuOpen = !menuOpen }, size = btn, tone = if (menuOpen) Tones.Sun else Tones.Cream,
                    icon = { menuDots(menuOpen) },
                )
            }
            AnimatedVisibility(
                visible = menuOpen,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = edge + btn + 6.dp, end = edge),
            ) {
                val small = 40.dp
                Row(
                    Modifier
                        .background(T.Cream.copy(alpha = 0.92f), RoundedCornerShape(26.dp))
                        .border(2.dp, T.Ink, RoundedCornerShape(26.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundButton(if (vm.night) S.day.str() else S.night.str(), onClick = { vm.toggleNight() }, size = small, tone = Tones.Night, icon = if (vm.night) Icons.Sun else Icons.Moon)
                    RoundButton(S.weather.str(), onClick = { vm.cycleWeather() }, size = small, tone = Tones.Cream, icon = weatherIcon)
                    RoundButton(S.camera.str(), onClick = { menuOpen = false; photo() }, size = small, tone = Tones.Cream, tapSound = false, icon = Icons.Camera)
                    RoundButton(S.workshop.str(), onClick = { menuOpen = false; vm.open(Screen.Creator(null)) }, size = small, tone = Tones.Grape, icon = Icons.Workshop)
                    if (place.mine) RoundButton(app.trollfoss.ui.SM.build.str(), onClick = { menuOpen = false; mineUi.open = !mineUi.open }, size = small, tone = if (mineUi.open) Tones.Sun else Tones.Mint, icon = BuildIcons.Hammer)
                    if (place.mine) RoundButton(app.trollfoss.ui.SM.paint.str(), onClick = {
                        menuOpen = false; mineUi.tab = 2; mineUi.open = true
                        if (place != app.trollfoss.domain.PlaceId.MINE_YARD) vm.travel(app.trollfoss.domain.PlaceId.MINE_YARD)
                    }, size = small, tone = Tones.Berry, icon = DesignIcons.Roller)
                }
            }
        }
        if (place.mine && !engine.designMode) MineTravelButtons(vm, place, compact,
            if (compact) Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            else Modifier.align(Alignment.TopStart).padding(start = edge + (btn + gap) * 4, top = 8.dp))
        if (!compact) RoomNavigator(vm, engine, Modifier.align(Alignment.TopCenter).padding(top = 100.dp, start = 20.dp, end = 20.dp))
        if (!engine.designMode) {
            RoundButton(app.trollfoss.ui.SM.furnish.str(), onClick = { menuOpen = false; engine.closeDriving(); engine.designMode = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = if (compact) 78.dp else 108.dp, bottom = edge),
                size = btn, tone = Tones.Berry, icon = DesignIcons.Sofa)
        }
        if (!engine.designMode && engine.vehicle != null) {
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = edge).background(T.Cream.copy(alpha = 0.94f), RoundedCornerShape(32.dp)).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoundButton(S.driveLeft.str(), onClick = { engine.drive(-1) }, size = btn, tone = Tones.Sea, icon = { driveArrow(false) })
                RoundButton(S.stopDriving.str(), onClick = { engine.drive(0) }, size = btn, tone = Tones.Sun, icon = {
                    drawRect(T.Ink, Offset(size.width * 0.3f, size.height * 0.3f), androidx.compose.ui.geometry.Size(size.width * 0.4f, size.height * 0.4f))
                })
                RoundButton(S.driveRight.str(), onClick = { engine.drive(1) }, size = btn, tone = Tones.Sea, icon = { driveArrow(true) })
                if (engine.vehicle?.type == app.trollfoss.domain.FixtureType.SUBMARINE) {
                    RoundButton(S.rise.str(), onClick = { engine.dive(-1) }, size = btn, tone = Tones.Mint, icon = {
                        rotate(-90f) { driveArrow(true) }
                    })
                    RoundButton(S.dive.str(), onClick = { engine.dive(1) }, size = btn, tone = Tones.Grape, icon = {
                        rotate(90f) { driveArrow(true) }
                    })
                }
                app.trollfoss.ui.components.CloseButton(engine::closeDriving, size = btn)
            }
        }
        } // The controls belong to the visible scene, beside the panel.
        if (friendsOpen) FriendsPanel(vm.world, vm.place, engine::invite, engine::packPerson, onClose = { friendsOpen = false })
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

        // Mitt hus: the builder panel (it steps aside while a job is at work, so the building can be watched).
        val mine = vm.world.mine
        AnimatedVisibility(
            visible = place.mine && mineUi.open && !engine.designMode && mine.job == null && mineVersion >= 0,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            BuildPanel(vm, engine, place, compact, onClose = { mineUi.open = false; vm.scheduleSave() })
        }
        // At the empty plot a bubble points at the hammer: on a phone the menu opens by itself so the hammer shows.
        val showHint = place == app.trollfoss.domain.PlaceId.MINE_YARD && !mine.started && !mineUi.open && !engine.designMode && mine.job == null
        LaunchedEffect(showHint, compact) { if (showHint && compact) menuOpen = true }
        if (showHint) {
            if (compact) {
                BuildHint(pointDown = false, modifier = Modifier.align(Alignment.TopEnd).padding(top = edge + btn + 6.dp + 48.dp, end = edge))
            } else {
                BuildHint(pointDown = true, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = edge + btn + 8.dp, start = edge + (btn + gap) - 10.dp))
            }
        }
        if (place.mine && mine.askDemolish >= 0) DemolishDialog(vm)

        PlaceBanner(place.let { S.place(it).str() }, key = place)

        TaskBanner(vm)

        if (vm.telescopeOpen) TelescopeView(night = vm.night, onClose = { vm.telescopeOpen = false })
    }
}

@Composable
private fun GlimtCounter(found: Int, total: Int, modifier: Modifier = Modifier, compact: Boolean = false) {
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
            .padding(start = if (compact) 5.dp else 8.dp, end = if (compact) 9.dp else 14.dp, top = if (compact) 3.dp else 6.dp, bottom = if (compact) 3.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 6.dp),
    ) {
        Canvas(Modifier.size(if (compact) 20.dp else 30.dp)) { Icons.Star(this) }
        GameText("$found", fontSize = if (compact) 15.sp else 22.sp, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = T.Sun)
        GameText("/ $total", fontSize = if (compact) 10.sp else 15.sp, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
    }
}

/** Three dots, or a cross when the menu is open. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.driveArrow(right: Boolean) {
    val s = size.minDimension
    val a = if (right) 0.3f else 0.7f
    val b = 1f - a
    drawLine(T.Ink, Offset(a * s, 0.5f * s), Offset(b * s, 0.5f * s), s * 0.09f)
    drawLine(T.Ink, Offset(0.5f * s, 0.28f * s), Offset(b * s, 0.5f * s), s * 0.09f)
    drawLine(T.Ink, Offset(0.5f * s, 0.72f * s), Offset(b * s, 0.5f * s), s * 0.09f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.menuDots(open: Boolean) {
    val s = size.minDimension
    val ink = T.Ink
    if (open) {
        val w = s * 0.12f
        drawLine(ink, Offset(s * 0.28f, s * 0.28f), Offset(s * 0.72f, s * 0.72f), strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(ink, Offset(s * 0.72f, s * 0.28f), Offset(s * 0.28f, s * 0.72f), strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    } else {
        for (i in 0 until 3) drawCircle(ink, s * 0.085f, Offset(s * (0.28f + 0.22f * i), s * 0.5f))
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
            if (t != null) CachedThumb("task:${t.id}", 84.dp) { drawTaskPicture(t) }
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
