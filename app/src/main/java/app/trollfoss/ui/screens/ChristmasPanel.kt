package app.trollfoss.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.*
import app.trollfoss.domain.*
import app.trollfoss.ui.*
import app.trollfoss.ui.art.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.theme.T

@Composable
internal fun ChristmasButton(vm:TrollfossViewModel,size:Dp) {
    var show by remember { mutableStateOf(false) }
    RoundButton(SX.title.str(),onClick={ show=true },size=size,tone=Tones.Berry,icon={
        val s=this.size.minDimension*0.8f
        translate(this.size.width/2,this.size.height*0.9f) { drawThing(ThingType.XM_HAT,0,0,s,s,Pen(2.dp.toPx())) }
    })
    if(show) ChristmasPanel(vm,onClose={show=false})
}

@Composable
internal fun ChristmasPicture(type:ThingType,v:Int=0,side:Dp=48.dp) {
    CachedThumb("christmas:${type.name}:$v",side) {
        val u=size.minDimension*0.82f/maxOf(type.w,type.h)
        translate(size.width/2,(size.height+type.h*u)/2) { drawThing(type,v,0,type.w*u,type.h*u,Pen(size.minDimension*0.028f)) }
    }
}

@Composable
fun ChristmasPanel(vm:TrollfossViewModel,onClose:()->Unit,initialTab:Int=0) {
    var tab by remember { mutableIntStateOf(initialTab) }
    var revision by remember { mutableIntStateOf(0) }
    var notice by remember { mutableStateOf<Txt?>(null) }
    val compact=androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp<520
    val x=vm.engine?.let { it.cam+it.visibleViewport/2f } ?: 1f
    val state=vm.world.christmas
    fun refresh() { revision++;vm.changed() }
    TrollDialog(onClose,maxWidth=720.dp) {
        GameText(SX.supplies.str(),fontSize=22.sp,color=T.Ink)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf(SX.supplies,SX.clothes,SX.calendar,SX.ideas,SX.settings).forEachIndexed { i,t ->
                Box(Modifier.background(if(i==tab) T.SunTop else Color.White,RoundedCornerShape(12.dp)).clickable {tab=i;notice=null}.padding(10.dp)) { GameText(t.str(),fontSize=14.sp,color=T.Ink) }
            }
        }
        revision
        notice?.let { GameText(it.str(),fontSize=14.sp,color=T.Ink) }
        when(tab) {
            0,1 -> {
                val choices=if(tab==0) Christmas.things.map { it to 0 }+listOf(ThingType.XM_STOCKING to 0) else Christmas.clothes
                LazyVerticalGrid(GridCells.Adaptive(96.dp),Modifier.fillMaxWidth().height(if(compact) 200.dp else 340.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    items(choices.size) { i -> val (type,v)=choices[i]
                        Column(Modifier.background(Color.White,RoundedCornerShape(12.dp)).clickable {
                            notice=if(vm.sim.christmas.supply(type,v,vm.place,x)==null) SX.full else null;refresh();if(notice==null) onClose()
                        }.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            ChristmasPicture(type,v)
                            GameText(SX.thing(type).str(),fontSize=12.sp,color=T.Ink,maxLines=2)
                        }
                    }
                }
                GameText(SX.hint.str(),fontSize=12.sp,color=T.Ink)
            }
            2 -> LazyVerticalGrid(GridCells.Fixed(6),Modifier.fillMaxWidth().height(if(compact) 210.dp else 350.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                items(24) { i ->
                    val done=i in state.opened
                    Column(Modifier.background(if(done) Color(0xFFDDE9DC) else Color(0xFFFFE9CF),RoundedCornerShape(12.dp)).clickable { if(!done) { notice=if(vm.sim.christmas.openDoor(i,vm.place,x)) null else SX.full;refresh() } }.padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        ChristmasPicture(if(done) ThingType.XM_STAR else ThingType.XM_PARCEL,side=if(compact) 28.dp else 44.dp)
                        GameText(if(done) "âœ“" else (i+1).toString(),fontSize=14.sp,color=T.Ink)
                    }
                }
            }
            3 -> Column(Modifier.height(if(compact) 220.dp else 380.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                ChristmasStoryCards(vm) { refresh();onClose() }
                ChristmasIdeas(vm) { onClose() }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    RoundButton((if("christmas:party" in vm.world.flags) SX.endParty else SX.party).str(),onClick={vm.sim.christmas.party(vm.place,x);refresh();onClose()},icon=Icons.Friends,tone=Tones.Berry)
                    GameText((if("christmas:party" in vm.world.flags) SX.endParty else SX.party).str(),fontSize=14.sp,color=T.Ink)
                }
                val sled=vm.world.fixturesIn(vm.place).firstOrNull { it.type==FixtureType.XM_SLEIGH }
                if(sled!=null) for(stop in Christmas.stops) {
                    Row(Modifier.fillMaxWidth().clickable {
                        if(vm.sim.christmas.fly(sled,stop)) {refresh();onClose();vm.flyTo(stop)} else notice=SX.passengers
                    }.background(Color.White,RoundedCornerShape(12.dp)).padding(10.dp),verticalAlignment=Alignment.CenterVertically) {
                        ChristmasPicture(ThingType.XM_PARCEL,side=32.dp)
                        GameText(S.place(stop).str(),fontSize=16.sp,color=T.Ink)
                    }
                }
            }
            else -> Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                for((choice,label) in listOf(FestivalChoice.AUTO to SX.auto,FestivalChoice.CHRISTMAS to SX.allYear,FestivalChoice.NONE to SX.off)) {
                    Row(Modifier.fillMaxWidth().background(if(vm.settings.festival==choice) T.SunTop else Color.White,RoundedCornerShape(12.dp)).clickable {vm.setFestival(choice);refresh()}.padding(12.dp)) { GameText(label.str(),fontSize=16.sp,color=T.Ink) }
                }
                Row(Modifier.fillMaxWidth().clickable {onClose();vm.flyTo(PlaceId.CHRISTMAS_SQUARE)}.padding(12.dp)) { GameText(SX.visit.str(),fontSize=18.sp,color=T.Ink) }
            }
        }
    }
}
