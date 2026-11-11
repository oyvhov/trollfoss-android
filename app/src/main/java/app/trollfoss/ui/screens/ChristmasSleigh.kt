package app.trollfoss.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import app.trollfoss.domain.*
import app.trollfoss.ui.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.theme.T

@Composable
internal fun ChristmasSleigh(vm:TrollfossViewModel,f:Fixture,onClose:()->Unit) {
    val riders=vm.world.people().filter {it.mode==Mode.SEATED && it.holder==f.id}
    var notice by remember {mutableStateOf(false)}
    TrollDialog(onClose,maxWidth=580.dp) {
        GameText(SX.fly.str(),fontSize=22.sp,color=T.Ink)
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
            IconCanvas(Icons.Friends,Modifier.size(40.dp))
            GameText("${riders.size} / 2",fontSize=20.sp,color=T.Ink)
            ChristmasPicture(ThingType.XM_PARCEL,side=38.dp)
            GameText("${vm.world.inMachine(f).size} / 6",fontSize=20.sp,color=T.Ink)
        }
        if(notice || riders.isEmpty()) GameText(SX.passengers.str(),fontSize=15.sp,color=T.Ink)
        for(stop in Christmas.stops) Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp)).clickable {
            if(vm.sim.christmas.fly(f,stop)) { vm.changed();onClose();vm.flyTo(stop) } else notice=true
        }.padding(14.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically) {
            IconCanvas(Icons.Map,Modifier.size(32.dp))
            GameText(S.place(stop).str(),fontSize=18.sp,color=T.Ink)
        }
    }
}
