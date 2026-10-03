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
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import app.trollfoss.domain.*
import app.trollfoss.ui.SP
import app.trollfoss.ui.components.TrollDialog
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
    vm.tasksVersion
    var tab by remember { mutableIntStateOf(if(vm.giftsFirst || vm.levelGift>0) 1 else 0) }
    LaunchedEffect(tab) { if(tab==1) vm.dismissLevelGift() }
    var selected by remember { mutableStateOf<ToyReward?>(null) }
    val compact=androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp<520
    val board=vm.sim.tasks.board()
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF3B2A6B),Color(0xFF1F1840)))).padding(if(compact) 12.dp else 24.dp)) {
        Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Row(Modifier.padding(end=60.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                GameText(SP.level(Progression.level(vm.world)).str(),fontSize=if(compact) 22.sp else 30.sp,color=Color.White)
                IconCanvas(DesignIcons.Sticker,Modifier.size(28.dp))
                GameText("${vm.stickers}",fontSize=22.sp,color=Color.White)
                if(Progression.missing(vm.world)>0) {
                    val next=ToyReward.entries.first { it.level==Progression.level(vm.world)+1 }
                    ToyPicture(next,if(compact) 42.dp else 60.dp)
                    GameText(SP.missing(Progression.missing(vm.world)).str(),fontSize=if(compact) 14.sp else 18.sp,color=Color.White)
                    LevelDots(vm)
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
                for(i in 0..1) Row(Modifier.background(if(tab==i) T.Sun else T.Cream,RoundedCornerShape(18.dp)).clickable { tab=i }.padding(horizontal=18.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    IconCanvas(if(i==0) DesignIcons.Tasks else Icons.Star,Modifier.size(28.dp))
                    GameText((if(i==0) S.tasks else SP.gifts).str(),fontSize=18.sp,color=T.Ink)
                }
                if(tab==0 && vm.tasksLeft==0) RoundButton(SP.newTasks.str(),onClick=vm::newTasks,size=48.dp,tone=Tones.Sun,icon=Icons.Dice)
                if(vm.levelGift>0) GameText(SP.newGifts.str(),fontSize=16.sp,color=T.Sun)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                if(tab==0) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        for(task in board) TaskCard(vm,task,Modifier.weight(1f),compact)
                    }
                    GameText(SP.keepStickers.str(),fontSize=14.sp,color=Color.White)
                } else {
                    val levels=(listOf(Progression.level(vm.world),Progression.level(vm.world)+1) + (1..Progression.thresholds.size)).distinct().filter { it<=Progression.thresholds.size }
                    for(level in levels) {
                        GameText((if(level==1) SP.free else SP.level(level)).str(),fontSize=20.sp,color=Color.White)
                        for (rewards in ToyReward.entries.filter { it.level==level }.chunked(if (compact) 3 else 4)) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            for(reward in rewards) Column(Modifier.weight(1f).background(T.Cream,RoundedCornerShape(20.dp)).border(2.dp,T.Ink,RoundedCornerShape(20.dp)).clickable { selected=reward }.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                ToyPicture(reward,if(compact) 64.dp else 104.dp)
                                GameText(SP.name(reward).str(),fontSize=if(compact) 14.sp else 18.sp,color=T.Ink,maxLines=2)
                                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                                    IconCanvas(if(Progression.unlocked(vm.world,reward)) Icons.Star else Icons.Lock,Modifier.size(22.dp))
                                    GameText((if(Progression.unlocked(vm.world,reward)) SP.tryIt else SP.level(level)).str(),fontSize=14.sp,color=T.Ink)
                                }
                            }
                            repeat((if(compact) 3 else 4)-rewards.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(T.Cream,RoundedCornerShape(20.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        ToyPicture(ToyReward.TRAIN,76.dp)
                        Column(Modifier.weight(1f)) {
                            GameText(SP.trainFound.str(),fontSize=20.sp,color=T.Ink)
                            GameText(SP.trainStep(vm.sim.toys.trainStage).str(),fontSize=16.sp,color=T.Ink)
                            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { repeat(3) { i -> Box(Modifier.size(40.dp),contentAlignment=Alignment.Center) {
                                if(i==0) CachedThumb("train:gear",40.dp) { drawThingThumb(ThingType.PLAY_GEAR,0,Rect(Offset.Zero,size)) }
                                else IconCanvas(if(i==1) Icons.Gear else Icons.Friends,Modifier.size(34.dp))
                                if(vm.sim.toys.trainStage>i) IconCanvas(Icons.Check,Modifier.size(20.dp).align(Alignment.BottomEnd).background(T.Mint,RoundedCornerShape(12.dp)))
                            } } }
                        }
                        RoundButton((if(Progression.unlocked(vm.world,ToyReward.TRAIN)) SP.tryIt else S.playGo).str(),onClick={
                            if(Progression.unlocked(vm.world,ToyReward.TRAIN)) selected=ToyReward.TRAIN
                            else { vm.sim.toys.startTrain();vm.changed();vm.travelPlayCard(PlaceId.MANOR_UPPER,vm.world.fixtures[vm.world.toyInputs["train:fixture"]]?.x ?: 1.15f) }
                        },size=52.dp,tone=Tones.Sea,icon=Icons.Map)
                    }
                }
            }
        }
        CloseButton(vm::back,Modifier.align(Alignment.TopEnd))
    }
    selected?.let { reward -> ToyRewardDetails(vm,reward) { selected=null } }
}

@Composable
fun ToyPicture(reward:ToyReward,side:androidx.compose.ui.unit.Dp) {
    CachedThumb("reward:${reward.name}",side) { drawFixtureThumb(reward.type,0,Rect(Offset.Zero,size)) }
}

@Composable
private fun TaskCard(vm:TrollfossViewModel,task:Task,modifier:Modifier,compact:Boolean) {
    val done=vm.sim.tasks.done(task)
    Column(modifier.background(if(done) Color(0xFFE8FFF1) else T.Cream,RoundedCornerShape(22.dp)).border(2.dp,T.Ink,RoundedCornerShape(22.dp)).padding(if(compact) 9.dp else 14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Box(contentAlignment=Alignment.Center) {
            CachedThumb("task:${task.id}",if(compact) 76.dp else 120.dp) { drawTaskPicture(task) }
            if(done) IconCanvas(Icons.Check,Modifier.size(50.dp).background(T.Mint.copy(alpha=0.8f),RoundedCornerShape(30.dp)))
        }
        GameText(SP.taskHint(task).str(),fontSize=if(compact) 14.sp else 18.sp,color=T.Ink,maxLines=3)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            repeat(task.need) { i -> Canvas(Modifier.size(18.dp)) { drawCircle(if(i<vm.sim.tasks.progress(task)) T.Sun else T.CreamDeep);drawCircle(Ink.line,size.minDimension/2-1.5f,style=Stroke(3f)) } }
        }
        GameText((task.place?.let { S.place(it) } ?: app.trollfoss.ui.SF.anywhere).str(),fontSize=14.sp,color=T.Grape)
        if(!done) Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                RoundButton(SP.help.str(),onClick={ vm.goTask(task) },size=48.dp,tone=Tones.Sea,icon=Icons.Map)
                GameText(SP.help.str(),fontSize=12.sp,color=T.Ink)
            }
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                RoundButton(SP.swap.str(),onClick={ vm.swapTask(task) },size=48.dp,tone=Tones.Cream,icon=Icons.Dice)
                GameText(SP.swap.str(),fontSize=12.sp,color=T.Ink)
            }
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
        task.id=="crown" -> {
            drawSpeciesThumb(app.trollfoss.domain.Species.FOLK,square(c,s*0.82f))
            drawThingThumb(app.trollfoss.domain.ThingType.CROWN,1,square(Offset(c.x,c.y-s*0.34f),s*0.32f))
        }
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
        "egg" -> sign(c, s * 0.8f) { DesignIcons.Egg(this) }
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
