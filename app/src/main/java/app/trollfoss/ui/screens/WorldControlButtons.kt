package app.trollfoss.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import app.trollfoss.domain.Weather
import app.trollfoss.ui.S
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.components.RoundButton
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.str

/** The same world controls on the home map and inside each place. */
@Composable
internal fun WorldControlButtons(vm: TrollfossViewModel, size: Dp) {
    RoundButton(if (vm.night) S.day.str() else S.night.str(), vm::toggleNight, size = size,
        tone = Tones.Night, icon = if (vm.night) Icons.Sun else Icons.Moon)
    RoundButton(S.weather.str(), vm::cycleWeather, size = size, tone = Tones.Cream,
        icon = when (vm.weather) { Weather.SUN -> Icons.SunCloud; Weather.RAIN -> Icons.Rain; Weather.SNOW -> Icons.Snow })
}
