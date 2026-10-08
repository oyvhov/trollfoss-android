package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.*
import app.trollfoss.ui.S
import app.trollfoss.ui.ST
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawTrailPicture
import app.trollfoss.ui.art.thTrollLantern
import app.trollfoss.ui.components.*
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

@Composable
private fun TrailPicture(index: Int, side: androidx.compose.ui.unit.Dp) {
    CachedThumb("rumle:clue:$index", side) {
        drawTrailPicture(index,Offset(size.width/2,size.height/2),size.minDimension*0.78f,Pen(size.minDimension*0.025f))
    }
}

@Composable
private fun LanternPicture(side: androidx.compose.ui.unit.Dp) {
    CachedThumb("rumle:lantern",side) {
        translate(size.width/2,size.height*0.94f) {
            thTrollLantern(1,size.minDimension*0.55f,size.minDimension*0.84f,Pen(size.minDimension*0.024f))
        }
    }
}

/** This follows the saved item; it never recalls it from a child's room or another friend's hand. */
private fun visitTrail(vm: TrollfossViewModel, engine: Engine, onClose: () -> Unit, index: Int? = null) {
    vm.sim.magic.start(Adventure.RUMLE)
    val target = index?.let(vm.sim.trail::note) ?: vm.sim.trail.target()
    vm.changed(); onClose()
    if (target != null && target.place == null) {
        engine.openBagAt(target)
    } else {
        vm.travelPlayCard(target?.place ?: vm.sim.trail.nextPlace(),target?.x ?: 1.15f)
    }
}

@Composable
fun TreasureTrailCard(vm: TrollfossViewModel, engine: Engine, onClose: () -> Unit) {
    engine.playVersion // Observe story progress in this composable as well as in its parent.
    val trail = vm.sim.trail
    val compact = engine.compact
    Column(Modifier.fillMaxWidth().background(T.SunTop.copy(alpha=0.45f),RoundedCornerShape(20.dp))
        .border(2.dp,T.Ink,RoundedCornerShape(20.dp)).padding(if(compact) 10.dp else 14.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            LanternPicture(if(compact) 48.dp else 62.dp)
            Column(Modifier.weight(1f)) {
                GameText(ST.title.str(),fontSize=if(compact) 18.sp else 22.sp,color=T.Ink)
                GameText((if(trail.complete) ST.done else ST.invitation).str(),fontSize=14.sp,color=T.Ink)
            }
            RoundButton((if(trail.complete) ST.findLantern else ST.go).str(),onClick={ visitTrail(vm,engine,onClose) },
                size=52.dp,tone=Tones.Mint,icon=if(trail.complete && trail.target()?.place==null) Icons.Bag else Icons.Map)
        }
        if(!trail.complete) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            for(i in 0..2) {
                val found = trail.found(i)
                val description = ST.clue(i).str() + if(found) ", " + ST.found.str() else ""
                Column(Modifier.weight(1f).background(if(found) T.Mint.copy(alpha=0.22f) else T.Cream,RoundedCornerShape(12.dp))
                    .semantics { contentDescription=description }.clickable(enabled=!found) { visitTrail(vm,engine,onClose,i) }.padding(6.dp),
                    horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)) {
                    Box(contentAlignment=Alignment.BottomEnd) {
                        TrailPicture(i,if(compact) 45.dp else 60.dp)
                        if(found) IconCanvas(Icons.Check,Modifier.size(22.dp).background(T.Mint,RoundedCornerShape(12.dp)))
                    }
                    GameText(S.place(TreasureTrail.stops[i].place).str(),fontSize=12.sp,color=T.Ink,maxLines=1)
                }
            }
        }
    }
}

@Composable
fun TreasureTrailReminder(vm: TrollfossViewModel, engine: Engine, modifier: Modifier, onClick: () -> Unit) {
    engine.playVersion
    val trail = vm.sim.trail
    Row(modifier.widthIn(max=350.dp).background(T.Cream.copy(alpha=0.97f),RoundedCornerShape(18.dp))
        .border(2.dp,T.Ink,RoundedCornerShape(18.dp)).padding(6.dp),
        horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically) {
        if(trail.complete) LanternPicture(42.dp) else TrailPicture(trail.nextIndex,42.dp)
        Column(Modifier.weight(1f).clickable(onClick=onClick)) {
            GameText(ST.title.str(),fontSize=14.sp,color=T.Ink)
            GameText((if(trail.complete) ST.lantern else S.place(trail.nextPlace())).str(),fontSize=12.sp,color=T.Ink,maxLines=1)
            Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                for(i in 0..2) Box(Modifier.size(12.dp).background(if(trail.found(i)) T.Mint else T.CreamDeep,RoundedCornerShape(6.dp)))
            }
        }
        RoundButton((if(trail.complete) ST.findLantern else if(trail.target()?.place==null) ST.bag else ST.go).str(),
            onClick={ visitTrail(vm,engine,{}) },size=44.dp,tone=Tones.Sea,icon=if(trail.target()?.place==null) Icons.Bag else Icons.Map)
        CloseButton({ vm.sim.magic.dismissAdventure(); vm.changed() },size=40.dp)
    }
}
