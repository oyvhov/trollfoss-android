package app.trollfoss.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.HouseKeys
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Look
import app.trollfoss.domain.Species
import app.trollfoss.domain.Pose
import app.trollfoss.domain.PersonAnim
import app.trollfoss.ui.S
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.cloudPuff
import app.trollfoss.ui.art.drawBalloon
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawIslandMapLive
import app.trollfoss.ui.art.MapTap
import app.trollfoss.ui.art.mapGeo
import app.trollfoss.ui.art.inWater
import app.trollfoss.ui.art.drawMapTap
import app.trollfoss.ui.art.drawBalloonTrail
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.art.mapLabel
import app.trollfoss.ui.art.mapSpot
import app.trollfoss.ui.art.MAP_WIDTH_FACTOR
import app.trollfoss.ui.art.rememberMapLayer
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.sin

/** The village seen from above. Tap a place and the balloon flies you there. */
@Composable
fun MapScreen(vm: TrollfossViewModel) {
    val motion = LocalMotion.current
    var t by remember { mutableFloatStateOf(0f) }
    var target by remember { mutableStateOf<PlaceId?>(null) }
    // The troll face in the mountain yawns when tapped (an Easter egg).
    var yawnAt by remember { mutableFloatStateOf(-10f) }
    val flight = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val taps = remember { mutableStateListOf<MapTap>() }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { t = if (motion) (it - start) / 1e9f else 0f }
    }
    val from = vm.place.mapPlace
    val riders = remember(vm.place, vm.generation, vm.playersVersion) {
        val team = app.trollfoss.domain.Players.activeTeam(vm.world)
        (if (vm.world.playerIds.isNotEmpty()) team else vm.world.people().filter { it.species == Species.FOLK && it.place == vm.place }.take(3)).map { it.look }
    }
    val scroll = rememberScrollState()
    val canPanLeft by remember { derivedStateOf { scroll.value > 2 } }
    val canPanRight by remember { derivedStateOf { scroll.value < scroll.maxValue - 2 } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val controls = mapControls(maxHeight.value)
        var controlsHeightPx by remember { mutableIntStateOf(0) }
        val controlsHeight = if (controlsHeightPx > 0) with(LocalDensity.current) { controlsHeightPx.toDp() } else 96.dp
        val viewportWidth = constraints.maxWidth
        val mapHeight = constraints.maxHeight
        val w = maxWidth * MAP_WIDTH_FACTOR
        val h = maxHeight
        val mapWidth = with(LocalDensity.current) { w.roundToPx() }
        fun centered(place: PlaceId) = (mapSpot(place).x * mapWidth - viewportWidth / 2f).toInt().coerceIn(0, mapWidth - viewportWidth)
        LaunchedEffect(mapWidth, from) { scroll.scrollTo(centered(from)) }
        fun fly(place: PlaceId) {
            if(target!=null) return
            if(place==from) { vm.open(Screen.Play); return }
            target=place
            vm.sfx(Sfx.WHOOSH,0.8f)
            scope.launch {
                flight.snapTo(0f)
                launch { scroll.animateScrollTo(centered(place),tween(if(motion) 1400 else 1)) }
                flight.animateTo(1f,tween(if(motion) 1400 else 1,easing=FastOutSlowInEasing))
                vm.landBalloon(place)
            }
        }
        LaunchedEffect(Unit) {
            vm.balloonDestination?.let { vm.balloonDestination=null;fly(it) }
        }
        // The still scenery is drawn once into a bitmap (off the main thread); each frame draws only what moves.
        // The secret path from the cellar of Storhuset to Trollhola shows once all five golden keys are found.
        val tunnel = remember(vm.generation, vm.houseKeys) { HouseKeys.TUNNEL in vm.world.flags }
        val mapLayer = rememberMapLayer(mapWidth, constraints.maxHeight, if (vm.night) 1f else 0f, vm.weather, tunnel = tunnel, house = vm.world.mine, season = vm.season, festival = vm.festival)
        Box(Modifier.fillMaxSize().horizontalScroll(scroll)) {
        Box(Modifier.width(w).fillMaxHeight()) {
        // The map is its own layer: every frame only it is redrawn, not the labels and buttons over it.
        Canvas(Modifier.fillMaxSize().graphicsLayer().pointerInput(vm, motion, mapWidth, mapHeight) {
            detectTapGestures { at ->
                val x = at.x / mapWidth
                val water = mapGeo(mapWidth.toFloat(), mapHeight.toFloat()).inWater(at)
                vm.sfx(if(water) Sfx.SPLASH else Sfx.POP, .45f, .9f + (x % .3f))
                if(motion) {
                    val tap = MapTap(at, t, water)
                    if(taps.size >= 6) taps.removeAt(0)
                    taps.add(tap)
                    scope.launch { delay(1200); taps.remove(tap) }
                }
            }
        }) {
            val pen = Pen(max(1.4f, size.height * 0.0034f), t, if (vm.night) 1f else 0f, vm.weather, season = vm.season, festival = vm.festival)
            drawIslandMapLive(pen, target ?: from, t, mapLayer.value)
            val cloud=mapSpot(PlaceId.CLOUD_ISLAND)
            val c=Offset(cloud.x*size.width,cloud.y*size.height-size.height*0.045f)
            val r=size.height*0.035f
            cloudPuff(c,r*1.2f,Color(0xFFFFFAEF),pen)
            inkedRound(Rect(c.x-r*1.3f,c.y,c.x+r*1.3f,c.y+r*0.3f),r*0.2f,Color(0xFF94C9A5),pen)

            val yawn = (t - yawnAt) / 2.4f
            if (yawn in 0f..1f) {
                val open = sin(yawn * Math.PI.toFloat())
                val c = Offset(size.width * 0.41f, size.height * 0.25f)
                val sz = size.height * 0.07f
                // Eyes squeezed shut, a huge yawn, and a few z rising from the mountain.
                for (side in intArrayOf(-1, 1)) drawLine(Color(0xFF3B3346), Offset(c.x + side * sz * 0.56f, c.y - sz * 0.2f), Offset(c.x + side * sz * 0.12f, c.y - sz * 0.2f), strokeWidth = pen.lw * 1.6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawOval(Color(0xFF2B2140), Offset(c.x - sz * 0.22f * open, c.y + sz * 0.12f), androidx.compose.ui.geometry.Size(sz * 0.44f * open, sz * 0.5f * open))
                drawOval(Color(0xFFFF7A9A), Offset(c.x - sz * 0.14f * open, c.y + sz * 0.42f), androidx.compose.ui.geometry.Size(sz * 0.28f * open, sz * 0.14f * open))
                for (k in 0 until 3) {
                    val zt = (yawn * 1.6f - k * 0.22f).coerceIn(0f, 1f)
                    if (zt > 0f && zt < 1f) drawPath(
                        Path().apply {
                            val zx = c.x + sz * (0.7f + k * 0.35f)
                            val zy = c.y - sz * (0.3f + zt * 1.4f + k * 0.2f)
                            val zs = sz * (0.16f + k * 0.05f)
                            moveTo(zx - zs, zy - zs); lineTo(zx + zs, zy - zs); lineTo(zx - zs, zy + zs); lineTo(zx + zs, zy + zs)
                        },
                        Color.White.copy(alpha = 1f - zt), style = Stroke(pen.lw * 1.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    )
                }
            }
            // The travel balloon: at home over the current place, or flying to the next.
            val a = mapSpot(from)
            val b = mapSpot(target ?: from)
            val p = flight.value
            if(motion) drawBalloonTrail(a, b, p, pen)
            val x = (a.x + (b.x - a.x) * p) * size.width
            val arc = sin(p * Math.PI.toFloat()) * size.height * 0.18f
            val y = ((a.y + (b.y - a.y) * p) * size.height - size.height * 0.1f - arc + sin(t * 1.6f) * size.height * 0.008f).coerceIn(size.height * 0.12f, size.height * 0.83f)
            drawBalloon(Offset(x, y), size.height * 0.09f, pen, riders)
            for(tap in taps) drawMapTap(tap, t, pen)
        }

        Box(
            Modifier
                .offset(x = w * 0.41f - 48.dp, y = h * 0.25f - 40.dp)
                .size(96.dp, 80.dp)
                .clickable(remember { MutableInteractionSource() }, indication = null) {
                    if (motion && t - yawnAt < 2.4f) return@clickable
                    if(motion) yawnAt = t
                    vm.sfx(Sfx.SNORE, 0.9f, 0.55f)
                    vm.sfx(Sfx.ROAR, 0.35f, 0.5f)
                    vm.sim.egg("mountain")
                },
        )
        // Storhuset (the widest place) goes first, so the other places stay on top where their touch areas meet.
        for (place in PlaceId.entries.filter { it.onMap }.sortedBy { if (it == PlaceId.MANOR_GROUND) 0 else 1 }) {
            val label = mapLabel(place).str()
            val bounds = mapMarkerBounds(place, w.value, h.value, controlsHeight.value)
            MapPlaceMarker(place, label, place == (target ?: from), h.value < 500f, onClick = { fly(place) }, modifier =
                Modifier
                    // Keep coastal labels above the floating controls without shrinking the map.
                    .offset(x = bounds.left.dp, y = bounds.top.dp)
                    .size(bounds.width.dp, bounds.height.dp))
        }
        }
        }

        Row(Modifier.align(Alignment.TopStart).padding(controls.edge.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorldControlButtons(vm, size = if (h.value < 500f) 48.dp else 56.dp)
        }
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().onSizeChanged { controlsHeightPx = it.height }.padding(controls.edge.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(controls.gap.dp)) {
                RoundButton(S.book.str(), onClick = { vm.open(Screen.Book) }, size = controls.button.dp, tone = Tones.Grape, icon = Icons.Book)
                RoundButton(S.workshop.str(), onClick = { vm.open(Screen.Creator(null)) }, size = controls.button.dp, tone = Tones.Grape, icon = Icons.Workshop)
                RoundButton(S.players.str(), onClick = { vm.open(Screen.Players) }, size = controls.button.dp, tone = Tones.Mint, icon = Icons.Friends)
            }
            ProgressButton(vm)
        }
        RoundButton(S.parents.str(), onClick = { vm.open(Screen.ParentGate) }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp), size = 52.dp, tone = Tones.Cream, icon = Icons.Gear)
        Row(Modifier.align(Alignment.TopCenter).padding(top = controls.edge.dp)
            .background(T.Cream, RoundedCornerShape(50)).border(2.dp, T.Ink, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconCanvas(Icons.Map, Modifier.size(if(h.value < 500f) 24.dp else 32.dp))
            GameText(S.appName.str(), fontSize = if(h.value < 500f) 20.sp else 26.sp, color = T.Sea, maxLines = 1)
        }
        if(canPanLeft) RoundButton(S.mapLeft.str(), onClick = {
            scope.launch { scroll.animateScrollTo((scroll.value - viewportWidth * .65f).toInt().coerceAtLeast(0), tween(if(motion) 480 else 1)) }
        }, modifier = Modifier.align(Alignment.CenterStart).padding(controls.edge.dp), size = 48.dp, tone = Tones.Cream, icon = Icons.Back)
        if(canPanRight) RoundButton(S.mapRight.str(), onClick = {
            scope.launch { scroll.animateScrollTo((scroll.value + viewportWidth * .65f).toInt().coerceAtMost(scroll.maxValue), tween(if(motion) 480 else 1)) }
        }, modifier = Modifier.align(Alignment.CenterEnd).padding(controls.edge.dp), size = 48.dp, tone = Tones.Cream,
            icon = { rotate(180f) { Icons.Back(this) } })
    }
}
