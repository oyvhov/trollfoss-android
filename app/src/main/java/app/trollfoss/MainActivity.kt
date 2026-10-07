package app.trollfoss

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import app.trollfoss.ui.TrollfossApp
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.theme.TrollfossTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private val viewModel: TrollfossViewModel by viewModels()
    private var lastShake = 0L
    private var firstJolt = 0L

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
            task = extras.getString("task"),
            seasonName = extras.getString("season"),
            festivalName = extras.getString("festival"),
            mine = extras.getString("mine"),
            shape = extras.getInt("shape", 0),
            build = extras.getString("build"),
            cam = extras.getFloat("cam", Float.NaN),
            layers = extras.getString("layers"),
            toys = extras.getString("toys"),
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.onForeground()
    }

    // ---- shaking the device shakes the world (an Easter egg, and a good way to get a giggle)

    override fun onResume() {
        super.onResume()
        val sensors = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        (getSystemService(Context.SENSOR_SERVICE) as? SensorManager)?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val g = kotlin.math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]) / SensorManager.GRAVITY_EARTH
        if (g < 2.6f) return
        val now = event.timestamp / 1_000_000
        // Two hard jolts within a moment count as a shake; then a few seconds of peace.
        if (now - lastShake < 3_500) return
        if (now - firstJolt in 80..700) {
            lastShake = now
            firstJolt = 0
            viewModel.shake()
        } else {
            firstJolt = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onStop() {
        super.onStop()
        viewModel.onBackground()
    }
}
