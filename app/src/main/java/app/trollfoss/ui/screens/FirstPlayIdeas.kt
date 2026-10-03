package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.TaskBook
import app.trollfoss.domain.ToyReward
import app.trollfoss.ui.SF
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Three optional picture paths; they work with existing figures and never reset a world. */
@Composable
fun FirstPlayIdeas(vm: TrollfossViewModel, onClose: () -> Unit) {
    GameText(SF.start.str(),fontSize=20.sp,color=T.Ink)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        for(i in 0..2) Column(Modifier.weight(1f).background(Color.White,RoundedCornerShape(18.dp))
            .border(2.dp,T.Ink,RoundedCornerShape(18.dp)).clickable {
                if(i==2) { if(vm.tryToy(ToyReward.PUMP)) onClose() }
                else { onClose();vm.goTask(TaskBook.ALL.first { it.id==if(i==0) "bedtime" else "crown" }) }
            }.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            if(i==2) ToyPicture(ToyReward.PUMP,66.dp)
            else {
                val task=TaskBook.ALL.first { it.id==if(i==0) "bedtime" else "crown" }
                CachedThumb("first:${task.id}",66.dp) { drawTaskPicture(task) }
            }
            GameText((when(i) { 0 -> SF.drag;1 -> SF.give;else -> SF.tryToy }).str(),fontSize=14.sp,color=T.Ink,maxLines=2)
        }
    }
}
