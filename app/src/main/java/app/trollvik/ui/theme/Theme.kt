package app.trollvik.ui.theme

import android.app.Activity
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Trollvik's interface palette (see docs/DESIGN.md §6). Every colour has a face, a lighter top for the
 * gloss and a deep edge the button rests on.
 */
object T {
    val Ink = Color(0xFF2B2140)
    val InkSoft = Color(0xFF5B5078)

    val Sun = Color(0xFFFFC83D)
    val SunTop = Color(0xFFFFE58A)
    val SunDeep = Color(0xFFD98A00)

    val Berry = Color(0xFFFF4D6D)
    val BerryTop = Color(0xFFFF8FA3)
    val BerryDeep = Color(0xFFC21F45)

    val Sea = Color(0xFF2F9BFF)
    val SeaTop = Color(0xFF7CCBFF)
    val SeaDeep = Color(0xFF1560C0)

    val Mint = Color(0xFF2FD18B)
    val MintTop = Color(0xFF86F2BF)
    val MintDeep = Color(0xFF14935C)

    val Grape = Color(0xFF8B5CF6)
    val GrapeTop = Color(0xFFC4A6FF)
    val GrapeDeep = Color(0xFF5B32C9)

    val Cream = Color(0xFFFFF7EA)
    val CreamDeep = Color(0xFFF3E3C8)
    val CreamLine = Color(0xFFE2CDA8)

    val Night = Color(0xFF1D1A4A)
    val NightTop = Color(0xFF3B2F7A)
    val NightLine = Color(0xFF4B4190)

    val Sky = Color(0xFF7CCBFF)
    val Text = Color(0xFFFFFFFF)
    val Muted = Color(0xFFCFC7F0)
}

/** Read once at the root: a binder call per animated element would be wasteful. */
val LocalMotion = compositionLocalOf { true }

private val trollvikColors = lightColorScheme(
    primary = T.Sun,
    onPrimary = T.Ink,
    secondary = T.Sea,
    onSecondary = Color.White,
    tertiary = T.Grape,
    background = T.Night,
    onBackground = T.Text,
    surface = T.Night,
    onSurface = T.Text,
    surfaceVariant = T.NightTop,
    onSurfaceVariant = T.Muted,
    outline = T.NightLine,
    error = T.Berry,
)

private val sans = FontFamily.SansSerif

private val trollvikTypography = Typography(
    displayLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Black, fontSize = 64.sp, lineHeight = 68.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
)

@Composable
fun TrollvikTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                // A play space: the system bars stay out of the way and come back with a swipe.
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    val context = LocalContext.current
    val motion = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }.getOrDefault(true)
    }
    CompositionLocalProvider(LocalMotion provides motion) {
        MaterialTheme(colorScheme = trollvikColors, typography = trollvikTypography, content = content)
    }
}
