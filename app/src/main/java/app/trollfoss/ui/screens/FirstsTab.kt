package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.First
import app.trollfoss.domain.FirstGroup
import app.trollfoss.domain.ToyReward
import app.trollfoss.ui.SO
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.DesignIcons
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.IconCanvas
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.TrollDialog
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

/** Every discovery in its group: found ones in colour with a tick, the rest as silhouettes that invite a try. */
@Composable
fun FirstsTab(vm: TrollfossViewModel, compact: Boolean) {
    vm.tasksVersion
    var selected by remember { mutableStateOf<First?>(null) }
    val found = vm.world.firsts
    val notYet = SO.notYet.str()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconCanvas(DesignIcons.Sticker, Modifier.size(28.dp))
        GameText("${found.size} / ${First.entries.size}", fontSize = 20.sp, color = Color.White)
    }
    val tile = if (compact) 56.dp else 76.dp
    val perRow = if (compact) 7 else 9
    for (group in FirstGroup.entries) {
        GameText(SO.group(group).str(), fontSize = if (compact) 16.sp else 20.sp, color = Color.White)
        for (row in First.entries.filter { it.group == group }.chunked(perRow)) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (f in row) {
                val got = f.name in found
                val label = SO.first(f).str()
                Box(Modifier.size(tile).background(if (got) T.Cream else Color(0xFF5A4A86), RoundedCornerShape(16.dp))
                    .border(2.dp, T.Ink, RoundedCornerShape(16.dp)).clickable { selected = f }
                    .semantics { contentDescription = if (got) label else notYet }) {
                    FirstPicture(f, Modifier.padding(6.dp).size(tile - 12.dp), found = got)
                    if (got) IconCanvas(Icons.Check, Modifier.size(20.dp).align(Alignment.BottomEnd).background(T.Mint, RoundedCornerShape(10.dp)))
                }
            }
            repeat(perRow - row.size) { Spacer(Modifier.size(tile)) }
        }
    }
    selected?.let { f ->
        val reward = (firstSubject(f) as? FirstSubject.Fixture)?.let { s -> ToyReward.entries.firstOrNull { it.type == s.type } }
        if (reward != null) ToyRewardDetails(vm, reward) { selected = null }
        else TrollDialog(onClose = { selected = null }) {
            val got = f.name in found
            FirstPicture(f, Modifier.size(140.dp).align(Alignment.CenterHorizontally), found = got)
            GameText(if (got) SO.first(f).str() else SO.notYet.str(), fontSize = 22.sp, color = T.Ink, modifier = Modifier.align(Alignment.CenterHorizontally))
            if (got) GameText(SO.found.str(), fontSize = 16.sp, color = T.Ink, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
