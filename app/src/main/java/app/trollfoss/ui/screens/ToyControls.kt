package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.*
import app.trollfoss.ui.SP
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.str
import app.trollfoss.ui.components.*
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.theme.T

/** The same unlock guide is used by the gift book and a locked furniture tile. */
@Composable
fun ToyRewardDetails(vm:TrollfossViewModel,reward:ToyReward,onClose:()->Unit) {
    vm.tasksVersion
    var full by remember(reward) { mutableStateOf(false) }
    TrollDialog(onClose=onClose,maxWidth=480.dp) {
        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
            GameText(SP.name(reward).str(),fontSize=26.sp,color=T.Ink)
            ToyPicture(reward,92.dp)
            GameText(SP.use(reward.type).str(),fontSize=17.sp,color=T.Ink)
            if(Progression.unlocked(vm.world,reward)) {
                BigButton(SP.tryIt.str(),onClick={ full=!vm.tryToy(reward);if(!full) onClose() },tone=Tones.Mint,icon=Icons.Star)
                if(full) {
                    GameText(SP.noSpace.str(),fontSize=16.sp,color=T.Ink)
                    BigButton(app.trollfoss.ui.SM.furnish.str(),onClick={
                        onClose(); vm.open(app.trollfoss.ui.Screen.Play); vm.engine?.designMode=true
                    },tone=Tones.Cream,icon=DesignIcons.Sofa)
                }
            } else if(reward==ToyReward.TRAIN) {
                GameText(SP.trainStep(vm.sim.toys.trainStage).str(),fontSize=18.sp,color=T.Ink)
                RoundButton(S.playGo.str(),onClick={
                    onClose();vm.sim.toys.startTrain();vm.changed()
                    vm.travelPlayCard(PlaceId.MANOR_UPPER,vm.world.fixtures[vm.world.toyInputs["train:fixture"]]?.x ?: 1.15f)
                },tone=Tones.Sea,icon=Icons.Map)
            } else {
                val needed=(Progression.thresholds.getOrNull(reward.level-1) ?: 0)-vm.stickers
                GameText(SP.missing(needed.coerceAtLeast(0)).str(),fontSize=20.sp,color=T.Ink)
                if(vm.tasksLeft==0) RoundButton(SP.newTasks.str(),onClick=vm::newTasks,tone=Tones.Sun,icon=Icons.Dice)
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    for(task in vm.sim.tasks.board().filter { !vm.sim.tasks.done(it) }) Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                        CachedThumb("gift:task:${task.id}",70.dp) { drawTaskPicture(task) }
                        GameText(SP.taskHint(task).str(),fontSize=14.sp,color=T.Ink)
                        RoundButton(S.playGo.str(),onClick={ onClose();vm.goTask(task) },size=48.dp,tone=Tones.Sea,icon=Icons.Map)
                    }
                }
            }
        }
    }
}

@Composable
fun ToyControls(vm:TrollfossViewModel,engine:Engine,f:Fixture) {
    if(f.type==FixtureType.PLAY_DOOR) { DoorControl(vm,engine,f);return }
    if(f.type==FixtureType.PLAY_ART) { TrollDialog(onClose={ engine.toyFixtureId=-1 }) { ArtEditor(vm,engine) { engine.toyFixtureId=-1 } };return }
    var redraw by remember(f.id) { mutableIntStateOf(0) }
    var noFriend by remember(f.id) { mutableStateOf(false) }
    val reward=ToyReward.entries.first { it.type==f.type }
    redraw // A control changes ordinary world data, so invalidate this small panel only.
    val broken=vm.sim.toys.broken(f)
    TrollDialog(onClose={ engine.toyFixtureId=-1 },maxWidth=520.dp) {
        GameText(SP.name(reward).str(),fontSize=26.sp,color=T.Ink)
        ToyPicture(reward,92.dp)
        GameText((if(broken) SP.trainStep(1) else SP.use(f.type)).str(),fontSize=17.sp,color=T.Ink)
        if(f.type==FixtureType.PLAY_MARBLES) {
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                repeat(3) { rail ->
                    RoundButton("${rail+1}",onClick={ vm.sim.edit { vm.sim.tap(f.place,f,0f,-0.37f+rail*0.12f) };vm.changed();redraw++ },size=64.dp,tone=if((redraw>=0) && f.mode and (1 shl rail)!=0) Tones.Mint else Tones.Sun,icon={
                        drawLine(T.Ink,Offset(size.width*0.15f,size.height*(if(f.mode and (1 shl rail)!=0) 0.3f else 0.7f)),Offset(size.width*0.85f,size.height*(if(f.mode and (1 shl rail)!=0) 0.7f else 0.3f)),size.width*0.08f)
                    })
                }
            }
        }
        if(f.type==FixtureType.PLAY_COLORS) Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            repeat(6) { color -> RoundButton(SP.color.str(),onClick={ vm.sim.edit { f.mode=color };vm.changed();redraw++ },size=48.dp,tone=Tone(app.trollfoss.ui.art.ToyColors[color],app.trollfoss.ui.art.ToyColors[color],T.Ink),icon={
                drawCircle(app.trollfoss.ui.art.ToyColors[color]);if(f.mode==color) Icons.Check(this)
            }) }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(18.dp)) {
            if(!broken && f.type!=FixtureType.PLAY_MARBLES && f.type!=FixtureType.PLAY_COLORS) Column(horizontalAlignment=Alignment.CenterHorizontally) {
                RoundButton(SP.doIt.str(),onClick={
                    noFriend=f.type==FixtureType.PLAY_CAMERA && vm.world.people().none { it.place==f.place && !it.held && it.species==Species.FOLK && kotlin.math.abs(it.x-f.x)<0.8f }
                    if(!noFriend) { vm.sim.edit { vm.sim.tap(f.place,f,0f,-0.2f) };vm.changed();engine.toyFixtureId=-1 }
                },tone=Tones.Mint,icon=Icons.Check)
                GameText(SP.doIt.str(),fontSize=14.sp,color=T.Ink)
            }
            if(vm.sim.toys.inputs(f.type).isNotEmpty()) Column(horizontalAlignment=Alignment.CenterHorizontally) {
                RoundButton(SP.help.str(),onClick={ vm.sim.toys.supply(f);vm.changed();engine.toyFixtureId=-1 },tone=Tones.Sea,icon=Icons.Bag)
                GameText(SP.help.str(),fontSize=14.sp,color=T.Ink)
                Row { for(input in vm.sim.toys.inputs(f.type)) CachedThumb("toy:input:$input",34.dp) { drawThingThumb(input,0,Rect(Offset.Zero,size)) } }
            }
            if(vm.world.inMachine(f).isNotEmpty()) Column(horizontalAlignment=Alignment.CenterHorizontally) {
                RoundButton(SP.release.str(),onClick={ vm.sim.toys.releaseAll(f);vm.changed();engine.toyFixtureId=-1 },tone=Tones.Berry,icon=Icons.Bag)
                GameText(SP.release.str(),fontSize=14.sp,color=T.Ink)
            }
        }
        if(noFriend) GameText(SP.noFriend.str(),fontSize=16.sp,color=T.Ink)
    }
}

/** One visible invitation on the map, with a picture of what the child will receive. */
@Composable
fun ProgressButton(vm:TrollfossViewModel,modifier:Modifier=Modifier) {
    vm.tasksVersion
    val level=Progression.level(vm.world)
    // The way to the next level is shown on Trollfossen itself; here only the gifts and the level number.
    Box(modifier.onGloballyPositioned { vm.bookAnchor=it.boundsInRoot().center }) {
        RoundButton(SP.gifts.str()+", "+SP.level(level).str(),onClick=vm::openGifts,size=56.dp,tone=Tones.Sun,icon=Icons.Star)
        Box(Modifier.align(Alignment.TopEnd).size(24.dp).background(T.Sea,RoundedCornerShape(12.dp)).border(2.dp,T.Ink,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {
            GameText("$level",fontSize=13.sp,color=Color.White)
        }
    }
}

@Composable
fun LevelDots(vm:TrollfossViewModel) {
    val level=Progression.level(vm.world)
    if(level>=Progression.thresholds.size) return
    val from=Progression.thresholds[level-1]
    Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        repeat(Progression.thresholds[level]-from) { i -> Canvas(Modifier.size(15.dp)) {
            drawCircle(if(vm.stickers-from>i) T.Sun else T.CreamDeep)
            drawCircle(T.Ink,size.minDimension/2-1f,style=androidx.compose.ui.graphics.drawscope.Stroke(2f))
        } }
    }
}

