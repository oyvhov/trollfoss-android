package app.trollfoss

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import app.trollfoss.ui.TrollfossApp
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.theme.TrollfossTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TrollfossViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrollfossTheme {
                TrollfossApp(viewModel)
            }
        }
        if (BuildConfig.DEBUG && savedInstanceState == null) handleDebugIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (BuildConfig.DEBUG) handleDebugIntent(intent)
    }

    /**
     * Debug builds only: lets verification jump straight to a place or a screen, e.g.
     * `adb shell am start -f 0x20000000 -n app.trollfoss.debug/app.trollfoss.MainActivity --es place beach --es night on`.
     */
    private fun handleDebugIntent(intent: Intent) {
        val extras = intent.extras ?: return
        viewModel.debug(
            placeName = extras.getString("place"),
            screenName = extras.getString("screen"),
            nightOn = extras.getString("night"),
            weatherName = extras.getString("weather"),
            secrets = extras.getInt("secrets", 0),
            wishes = extras.getBoolean("wishes", false),
            skip = extras.getInt("skip", 0),
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.onForeground()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onBackground()
    }
}
