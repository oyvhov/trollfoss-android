package app.trollfoss.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The builder panel's own state: whether it is open and which of its tabs shows. The house itself lives in the world. */
class MineUi {
    var open by mutableStateOf(false)
    var tab by mutableIntStateOf(0)

    /** Bumps whenever the house or the builder changes, so the panel and the buttons redraw. */
    var version by mutableIntStateOf(0)
}
