package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.*
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

private fun PlayAction.label(): Txt = when (this) {
    PlayAction.HUG -> S.playHug; PlayAction.THROW -> S.playThrow; PlayAction.NAP -> S.playNap
    PlayAction.HIDE -> S.playHide; PlayAction.READ -> S.playRead; PlayAction.STORY -> S.playStory
    PlayAction.LIGHT -> S.playLight; PlayAction.TWINKLE -> S.playTwinkle
}
private fun PlayRecipe.label() = if (this == PlayRecipe.FORT) S.playFort else S.playCart
private fun Adventure.label(): Txt = when (this) { Adventure.HAT -> S.playHats; Adventure.CAMP -> S.playCamp; Adventure.PARADE -> S.playParade }
private fun Adventure.hint(stage: Int): Txt = when (this) {
    Adventure.HAT -> if (stage == 0) S.playFindHat else S.playReturnHat
    Adventure.CAMP -> listOf(S.playBuildCamp, S.playSitCamp, S.playLightCamp)[stage.coerceIn(0, 2)]
    Adventure.PARADE -> listOf(S.playBuildCart, S.playSeatCart, S.playPullCart)[stage.coerceIn(0, 2)]
}

@Composable
private fun PlayThingPicture(type: ThingType, side: androidx.compose.ui.unit.Dp = 44.dp) {
    CachedThumb("play:thing:${type.name}", side) {
        val scale = size.minDimension * 0.78f / maxOf(type.w, type.h)
        translate(size.width / 2, size.height / 2 + type.h * scale / 2) {
            drawThing(type, 0, 0, type.w * scale, type.h * scale, Pen(lw = size.minDimension * 0.035f))
        }
    }
}

@Composable
fun PlayActions(vm: TrollfossViewModel, engine: Engine, thing: Thing) {
    var failed by remember(thing.id) { mutableStateOf(false) }
    TrollDialog(onClose = { engine.playThingId = -1 }, maxWidth = 500.dp) {
        GameText(S.playThings.str(), fontSize = 22.sp, color = T.Ink)
        PlayThingPicture(thing.type, 62.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (action in vm.sim.magic.actions(thing)) Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                RoundButton(action.label().str(), onClick = { failed = !engine.playAction(action) }, size = 62.dp, tone = Tones.Mint,
                    icon = { drawPlayAction(action, thing.type) })
                GameText(action.label().str(), fontSize = 14.sp, color = T.Ink, maxLines = 2)
            }
        }
        if (failed) GameText(S.playNearFriend.str(), fontSize = 15.sp, color = T.Ink)
    }
}

@Composable
fun PlayCreation(vm: TrollfossViewModel, engine: Engine, f: Fixture) {
    var helper by remember(f.id) { mutableStateOf(f.on) }
    TrollDialog(onClose = { engine.playFixtureId = -1 }, maxWidth = 460.dp) {
        val recipe = if (f.type == FixtureType.PLAY_FORT) PlayRecipe.FORT else PlayRecipe.CART
        GameText(recipe.label().str(), fontSize = 24.sp, color = T.Ink)
        GameText((if (recipe == PlayRecipe.CART) S.playHandles else S.playSit).str(), fontSize = 16.sp, color = T.Ink)
        if (recipe == PlayRecipe.CART) {
            RoundButton((if (helper) S.playPullTogether else S.playHelper).str(), onClick = {
                f.on = !f.on; helper = f.on; vm.changed()
            }, size = 64.dp, tone = if (helper) Tones.Mint else Tones.Sea, icon = Icons.Friends)
            GameText((if (helper) S.playPullTogether else S.playHelper).str(), fontSize = 15.sp, color = T.Ink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundButton(S.playPack.str(), onClick = { vm.sim.magic.unmake(f); vm.changed(); engine.playFixtureId = -1 }, tone = Tones.Berry, icon = Icons.Bag)
            Column { GameText(S.playPack.str(), fontSize = 16.sp, color = T.Ink); GameText(S.playPartsSafe.str(), fontSize = 12.sp, color = T.Ink) }
        }
    }
}

@Composable
fun PlayCards(vm: TrollfossViewModel, engine: Engine, onClose: () -> Unit) {
    var tab by remember { mutableIntStateOf(if (vm.sim.magic.active == null) 0 else 1) }
    val compact = engine.compact
    TrollDialog(onClose = onClose, maxWidth = 620.dp) {
        GameText(S.playCards.str(), fontSize = if (compact) 20.sp else 26.sp, color = T.Ink)
        if (!compact) GameText(S.playOptional.str(), fontSize = 14.sp, color = T.Ink)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (i in 0..1) Row(Modifier.weight(1f).background(if (tab == i) T.SunTop else Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, T.Ink, RoundedCornerShape(16.dp)).clickable { tab = i }.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconCanvas(if (i == 0) BuildIcons.Hammer else Icons.Book, Modifier.size(28.dp))
                GameText((if (i == 0) S.playBuildToys else S.playAdventures).str(), fontSize = 16.sp, color = T.Ink)
            }
        }
        if (tab == 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (recipe in PlayRecipe.entries) Column(Modifier.weight(1f)
                .background(Color.White, RoundedCornerShape(20.dp)).border(2.dp, T.Ink, RoundedCornerShape(20.dp)).padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CachedThumb("play:recipe:${recipe.name}", if (compact) 48.dp else 96.dp) {
                    val f = Fixture(-1, PlaceId.HOME, recipe.fixture, 0f, 0f)
                    val u = size.minDimension * 0.88f / maxOf(f.spec.w, f.spec.h)
                    translate(size.width / 2, size.height / 2 + f.spec.h * u / 2) {
                        drawFixtureBack(f, u, Pen(lw = size.width * 0.025f)); drawFixtureFront(f, u, Pen(lw = size.width * 0.025f))
                    }
                }
                GameText(recipe.label().str(), fontSize = if (compact) 15.sp else 18.sp, color = T.Ink)
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    recipe.parts.forEachIndexed { i, part ->
                        if (i > 0) GameText("+", fontSize = 16.sp, color = T.Ink)
                        PlayThingPicture(part, 34.dp)
                    }
                }
                GameText(S.playCombine.str(), fontSize = 12.sp, color = T.Ink)
                RoundButton(S.playKit.str(), onClick = {
                    if (vm.sim.magic.kit(recipe, vm.place, engine.cam + engine.visibleViewport / 2)) { vm.changed(); onClose() }
                }, size = 52.dp, tone = Tones.Mint, icon = Icons.Bag)
                GameText(S.playKit.str(), fontSize = 12.sp, color = T.Ink)
            }
        }
        if (tab == 1) for (adventure in Adventure.entries) {
            val stage = vm.sim.magic.stage(adventure)
            val done = stage >= 3
            Row(Modifier.fillMaxWidth().background(if (done) T.Mint.copy(alpha = 0.15f) else T.SunTop.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .border(2.dp, T.Ink, RoundedCornerShape(18.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (done) PlayThingPicture(adventure.reward, 60.dp)
                    else when {
                        adventure == Adventure.HAT -> PlayThingPicture(ThingType.CAP, 60.dp)
                        stage == 0 -> CachedThumb("play:goal:${adventure.name}", 70.dp) {
                            val f = Fixture(-1, adventure.place, if (adventure == Adventure.CAMP) FixtureType.PLAY_FORT else FixtureType.PLAY_CART, 0f, 0f)
                            val u = size.minDimension*0.88f/maxOf(f.spec.w,f.spec.h)
                            translate(size.width/2,size.height/2+f.spec.h*u/2) { drawPlayBack(f,u,Pen(size.width*0.03f)); drawPlayFront(f,u,Pen(size.width*0.03f)) }
                        }
                        stage == 1 -> IconCanvas(Icons.Friends, Modifier.size(60.dp))
                        adventure == Adventure.CAMP -> PlayThingPicture(ThingType.AT_FLASHLIGHT, 60.dp)
                        else -> IconCanvas(Icons.Friends, Modifier.size(60.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        val count = if (adventure == Adventure.HAT) 2 else 3
                        repeat(count) { i -> Box(Modifier.size(22.dp).background(if (done || i < stage) T.Mint else T.CreamDeep, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            if (done || i < stage) IconCanvas(Icons.Check, Modifier.size(18.dp))
                            else GameText("${i + 1}", fontSize = 12.sp, color = T.Ink)
                        } }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GameText(adventure.label().str(), fontSize = 18.sp, color = T.Ink)
                    GameText((if (done) S.playFinished else adventure.hint(stage)).str(), fontSize = 14.sp, color = T.Ink)
                    if (!done) GameText(S.place(vm.sim.magic.nextPlace(adventure)).str(), fontSize = 12.sp, color = T.Ink)
                    if (!done) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        IconCanvas(Icons.Star, Modifier.size(20.dp)); PlayThingPicture(adventure.reward, 25.dp)
                    }
                }
                if (!done) RoundButton(S.playGo.str(), onClick = {
                    vm.sim.magic.start(adventure)
                    val to = vm.sim.magic.nextPlace(adventure)
                    if (vm.sim.magic.stage(adventure) == 0 && adventure != Adventure.HAT) vm.sim.magic.kit(if (adventure == Adventure.CAMP) PlayRecipe.FORT else PlayRecipe.CART, to, 1.15f)
                    vm.changed(); onClose(); vm.travelPlayCard(to)
                }, size = 52.dp, tone = Tones.Sea, icon = Icons.Map)
            }
        }
    }
}

/** Updates on a deed, not on every animation frame. One reminder keeps the next step in view. */
@Composable
fun AdventureReminder(vm: TrollfossViewModel, engine: Engine, modifier: Modifier, onClick: () -> Unit) {
    engine.playVersion
    val a = vm.sim.magic.active ?: return
    val stage = vm.sim.magic.stage(a)
    Row(modifier.widthIn(max = 350.dp).background(T.Cream.copy(alpha = 0.96f), RoundedCornerShape(18.dp))
        .border(2.dp,T.Ink,RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconCanvas(if (stage >= 3) Icons.Check else Icons.Book,Modifier.size(30.dp))
        Column(Modifier.weight(1f)) {
            GameText(a.label().str(),fontSize=14.sp,color=T.Ink)
            GameText((if (stage >= 3) S.playFinished else a.hint(stage)).str(),fontSize=12.sp,color=T.Ink,maxLines=2)
        }
    }
}
