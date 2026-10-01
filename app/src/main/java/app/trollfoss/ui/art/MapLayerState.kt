package app.trollfoss.ui.art

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.trollfoss.domain.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * The cached still layer of the map for a map of [widthPx] × [heightPx] pixels. It is made on a
 * background thread, only when the size, the [night], the [weather] or the secret [tunnel] path of Storhuset
 * (shown once all five golden keys are found) really change, and until it is
 * ready the state is null (or the previous layer, which [drawIslandMapLive] stops using if it no longer fits).
 */
@Composable
fun rememberMapLayer(widthPx: Int, heightPx: Int, night: Float, weather: Weather, rainbow: Float = 0f, tunnel: Boolean = false): State<MapLayer?> {
    val state = remember { mutableStateOf<MapLayer?>(null) }
    val nightStep = (night * 20f).roundToInt()
    val rainbowStep = (rainbow * 10f).roundToInt()
    LaunchedEffect(widthPx, heightPx, nightStep, weather, rainbowStep, tunnel) {
        if (widthPx > 0 && heightPx > 0) {
            state.value = withContext(Dispatchers.Default) { buildMapLayer(widthPx, heightPx, nightStep / 20f, weather, rainbowStep / 10f, tunnel) }
        }
    }
    return state
}

