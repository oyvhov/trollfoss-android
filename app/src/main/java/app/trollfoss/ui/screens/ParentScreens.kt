package app.trollfoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.BuildConfig
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.Recipes
import app.trollfoss.domain.Secrets
import app.trollfoss.ui.S
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.BigButton
import app.trollfoss.ui.components.CloseButton
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.PressSurface
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.components.TrollDialog
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.T
import app.trollfoss.update.UpdateMessage
import app.trollfoss.update.plainReleaseNotes
import java.util.Locale
import kotlin.random.Random

/** A multiplication a six-year-old won't solve, with big number keys. */
@Composable
fun ParentGateScreen(vm: TrollfossViewModel) {
    val a = remember { 6 + Random.nextInt(4) }
    val b = remember { 6 + Random.nextInt(4) }
    var entry by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(T.NightTop, T.Night)))) {
        Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GameText(S.gateTitle.str(), fontSize = 34.sp, style = MaterialTheme.typography.headlineLarge)
                Text(S.gateBody.str(), color = T.Muted, style = MaterialTheme.typography.bodyLarge)
                GameText("$a × $b = ${entry.ifEmpty { "?" }}", fontSize = 52.sp, style = MaterialTheme.typography.displayMedium, color = T.Sun)
                if (wrong) Text(S.gateWrong.str(), color = T.BerryTop, style = MaterialTheme.typography.bodyLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val keys = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("⌫", "0", "OK"))
                for (row in keys) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (k in row) {
                            PressSurface(
                                onClick = {
                                    wrong = false
                                    when (k) {
                                        "⌫" -> entry = entry.dropLast(1)
                                        "OK" -> if (entry.toIntOrNull() == a * b) vm.open(Screen.Parent) else { wrong = true; entry = "" }
                                        else -> if (entry.length < 3) entry += k
                                    }
                                },
                                modifier = Modifier.size(72.dp, 64.dp),
                                tone = if (k == "OK") Tones.Mint else Tones.Cream,
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Text(k, fontSize = 26.sp, fontWeight = FontWeight.Black, color = T.Ink)
                            }
                        }
                    }
                }
            }
        }
        CloseButton({ vm.back() }, Modifier.align(Alignment.TopEnd).padding(16.dp))
    }
}

/** Settings, progress, updates and a fresh start. Plain, calm text for grown-ups. */
@Composable
fun ParentScreen(vm: TrollfossViewModel) {
    var confirmReset by remember { mutableStateOf(false) }
    val settings = vm.settings
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(T.NightTop, T.Night)))) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GameText(S.parents.str(), fontSize = 32.sp, style = MaterialTheme.typography.headlineLarge)
            Row(Modifier.widthIn(max = 980.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Panel(S.settings.str()) {
                        ToggleRow(S.sound.str(), settings.sound) { on -> vm.updateSettings { it.copy(sound = on) } }
                        ToggleRow(S.music.str(), settings.music) { on -> vm.updateSettings { it.copy(music = on) } }
                        ToggleRow(S.haptics.str(), settings.haptics) { on -> vm.updateSettings { it.copy(haptics = on) } }
                        Text(S.language.str(), color = T.Muted, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Choice(S.nynorsk.str(), settings.maalform == Maalform.NYNORSK) { vm.setMaalform(Maalform.NYNORSK) }
                            Choice(S.bokmaal.str(), settings.maalform == Maalform.BOKMAAL) { vm.setMaalform(Maalform.BOKMAAL) }
                        }
                    }
                    Panel(S.progress.str()) {
                        Text(
                            S.progressLine(vm.found, Secrets.all.size, vm.discoveries, Recipes.book.size, vm.folk().size).str(),
                            color = T.Text,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        BigButton(S.reset.str(), onClick = { confirmReset = true }, tone = Tones.Night, textColor = Color.White, modifier = Modifier.fillMaxWidth())
                    }
                    Panel(S.about.str()) {
                        Text(S.aboutBody.str(), color = T.Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(Modifier.weight(1f)) {
                    UpdatePanel(vm)
                }
            }
        }
        CloseButton({ vm.back() }, Modifier.align(Alignment.TopEnd).padding(16.dp))
    }
    if (confirmReset) {
        TrollDialog(onClose = { confirmReset = false }) {
            GameText(S.reset.str(), fontSize = 28.sp, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(S.resetBody.str(), color = T.Ink, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            BigButton(S.cancel.str(), onClick = { confirmReset = false }, tone = Tones.Mint, modifier = Modifier.fillMaxWidth())
            BigButton(S.resetConfirm.str(), onClick = {
                confirmReset = false
                vm.resetWorld()
            }, tone = Tones.Berry, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(T.NightTop.copy(alpha = 0.55f))
            .border(2.dp, T.NightLine, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = T.Sun, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        content()
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, detail: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!value) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = T.Text, style = MaterialTheme.typography.titleMedium)
            if (detail != null) Text(detail, color = T.Muted, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = T.Mint, uncheckedTrackColor = T.Night, uncheckedThumbColor = T.Muted),
        )
    }
}

@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) T.Sun else T.Night)
            .border(2.dp, if (selected) T.Ink else T.NightLine, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(label, color = if (selected) T.Ink else T.Text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UpdatePanel(vm: TrollfossViewModel) {
    val updater = vm.updater
    val state = updater.state
    Panel(S.updates.str()) {
        val release = state.release
        if (release != null) {
            Text(S.updateReady(release.tag.removePrefix("v")).str(), style = MaterialTheme.typography.titleLarge, color = T.Sun)
            Text(S.updateSize(String.format(Locale.ROOT, "%.1f", release.size / (1024f * 1024f))).str(), style = MaterialTheme.typography.bodyMedium, color = T.Muted)
            val notes = remember(release.notes) { plainReleaseNotes(release.notes) }
            // Capped so a long body never pushes the download button out of sight.
            if (notes.isNotEmpty()) Text(notes, style = MaterialTheme.typography.bodyMedium, color = T.Text, maxLines = 12, overflow = TextOverflow.Ellipsis)
        } else {
            Text(S.installedVersion(BuildConfig.VERSION_NAME).str(), style = MaterialTheme.typography.titleMedium, color = T.Text)
        }
        val message = when (state.message) {
            null -> null
            UpdateMessage.CURRENT -> S.updateCurrent
            UpdateMessage.NETWORK -> S.updateNetwork
            UpdateMessage.INVALID -> S.updateInvalid
            UpdateMessage.STORAGE -> S.updateStorage
            UpdateMessage.PERMISSION -> S.updatePermission
            UpdateMessage.INSTALL -> S.updateInstallError
            UpdateMessage.ACCESS -> S.updateAccess
            UpdateMessage.RATE -> S.updateRate
        }
        if (message != null) Text(message.str(), style = MaterialTheme.typography.bodyMedium, color = if (state.message == UpdateMessage.CURRENT) T.MintTop else T.SunTop)
        if (state.checking) LinearProgressIndicator(Modifier.fillMaxWidth(), color = T.Sun, trackColor = T.Night)
        if (state.downloading) {
            LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth(), color = T.Sun, trackColor = T.Night)
            Text(S.updateProgress((state.progress * 100).toInt()).str(), style = MaterialTheme.typography.bodyMedium, color = T.Muted)
        }
        when {
            state.downloading -> BigButton(S.updateCancel.str(), onClick = updater::cancel, tone = Tones.Night, modifier = Modifier.fillMaxWidth())
            state.ready -> BigButton(S.updateInstall.str(), onClick = updater::install, tone = Tones.Mint, icon = Icons.Check, modifier = Modifier.fillMaxWidth())
            release != null -> BigButton(S.updateDownload.str(), onClick = updater::download, icon = Icons.Refresh, enabled = !state.checking, modifier = Modifier.fillMaxWidth())
        }
        BigButton(S.updateCheck.str(), onClick = { updater.check(manual = true) }, tone = Tones.Sea, enabled = !state.checking && !state.downloading, modifier = Modifier.fillMaxWidth())
        Text(S.updateHint.str(), style = MaterialTheme.typography.bodySmall, color = T.Muted)
        ToggleRow(S.updateAuto.str(), state.automatic, detail = S.updateAutoHint.str()) { updater.setAutomatic(it) }
        ToggleRow(S.updatePreviews.str(), state.previews, detail = S.updatePreviewsHint.str()) { updater.setPreviews(it) }
    }
}

