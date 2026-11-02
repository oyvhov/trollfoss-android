package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.ui.SM
import app.trollfoss.ui.components.*
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T

internal val BuildTeal = Color(0xFF34736F)
internal val BuildPale = Color(0xFFE5F1E9)
private val buildTone = Tone(Color(0xFF70A59A), BuildTeal, Color(0xFF24534F))

/** A labelled action keeps the icon and its touch target together, including at larger text sizes. */
@Composable
internal fun BuildAction(text: String, icon: DrawScope.() -> Unit, onClick: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, primary: Boolean = true) {
    PressSurface(onClick, modifier.fillMaxWidth().heightIn(min = 48.dp),
        tone = if (primary) buildTone else Tones.Cream, enabled = enabled,
        shape = RoundedCornerShape(16.dp), depth = 3.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconCanvas(icon, Modifier.size(26.dp))
            GameText(text, color = if (primary) Color.White else T.Ink, fontSize = 15.sp, maxLines = 3,
                modifier = Modifier.weight(1f))
        }
    }
}

@Composable
internal fun BuildSteps(preview: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(SM.choose, SM.preview, SM.build).forEachIndexed { index, label ->
            val active = index == if (preview) 1 else 0
            Row(Modifier.weight(1f).background(if (active) BuildPale else Color.Transparent, RoundedCornerShape(10.dp))
                .semantics { selected = active }.padding(horizontal = 5.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GameText("${index + 1}", fontSize = 12.sp, color = BuildTeal)
                GameText(label.str(), fontSize = 12.sp, color = T.Ink)
            }
        }
    }
}

/** Choosing a picture only previews it. The one explicit action is the only path into the world builder. */
@Composable
internal fun FoundationChooser(compact: Boolean, busy: Boolean, onBuild: (Int) -> Unit) {
    var template by rememberSaveable { mutableIntStateOf(-1) }
    val tile = if (compact) 94.dp else 124.dp
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BuildSteps(template >= 0)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (template < 0) {
                for (row in 0..1) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in row * 2..row * 2 + 1) {
                        Tile(tile, desc = SM.template(i).str(), enabled = !busy, onClick = { template = i }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CachedThumb("mine:tpl:$i", tile - 40.dp) { drawTemplateThumb(i) }
                                GameText(SM.template(i).str(), fontSize = 12.sp, color = T.Ink, maxLines = 2,
                                    textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, T.CreamLine, RoundedCornerShape(20.dp)).padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    CachedThumb("mine:tpl:$template", if (compact) 100.dp else 176.dp) { drawTemplateThumb(template) }
                    GameText(SM.template(template).str(), fontSize = 18.sp, color = T.Ink)
                }
            }
        }
        if (template >= 0) {
            BuildAction((if (busy) SM.buildingNow else SM.buildHouse).str(), BuildIcons.Hammer,
                onClick = { onBuild(template) }, enabled = !busy)
            BuildAction(SM.chooseAnotherHouse.str(), Icons.House, onClick = { template = -1 },
                enabled = !busy, primary = false)
        }
    }
}
