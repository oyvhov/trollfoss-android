package app.trollfoss.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.Maalform
import app.trollfoss.ui.LocalMaalform
import app.trollfoss.ui.SM
import app.trollfoss.ui.components.GameText
import app.trollfoss.ui.str
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T
import app.trollfoss.ui.theme.TrollfossTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class BuildChoicesTest {
    @get:Rule val compose = createComposeRule()

    private fun content(language: Maalform = Maalform.NYNORSK, busy: () -> Boolean = { false }, build: (Int) -> Unit) {
        compose.setContent {
            TrollfossTheme {
                CompositionLocalProvider(LocalMaalform provides language, LocalMotion provides false) {
                    val compact = LocalWindowInfo.current.containerSize.height < 600 * LocalDensity.current.density
                    Column(Modifier.width(playPanelWidth(compact, false)).fillMaxHeight().background(T.Cream).padding(12.dp)) {
                        GameText(SM.chooseHouse.str(), color = T.Ink)
                        FoundationChooser(compact, busy(), build)
                    }
                }
            }
        }
    }

    private fun capture(name: String) {
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val size = context.resources.displayMetrics.widthPixels
        val directory = File(context.getExternalFilesDir(null), "redesign").apply { mkdirs() }
        File(directory, "$name-$size.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun choosingAndChangingAHouseOnlyBuildsAfterConfirmation() {
        val built = mutableListOf<Int>()
        content { built += it }
        capture("houses")
        compose.onNodeWithContentDescription("Villa").performScrollTo().performClick()
        compose.onNodeWithText("Bygg huset").assertIsDisplayed()
        assertEquals(emptyList<Int>(), built)
        capture("house-preview")
        compose.onNodeWithText("Vel eit anna hus").performClick()
        compose.onNodeWithContentDescription("Tårnhus").performScrollTo().performClick()
        assertEquals(emptyList<Int>(), built)
        compose.onNodeWithText("Bygg huset").performClick()
        assertEquals(listOf(2), built)
    }

    @Test fun buildingDisablesBothConfirmationAndChangingThePreviewInBokmal() {
        var busy by mutableStateOf(false)
        var calls = 0
        content(Maalform.BOKMAAL, { busy }) { calls++; busy = true }
        compose.onNodeWithContentDescription("Hytte").performScrollTo().performClick()
        compose.onNodeWithText("Velg et annet hus").assertIsDisplayed()
        compose.onNodeWithText("Bygg huset").performClick()
        compose.onNodeWithText("Vi bygger …").assertIsNotEnabled()
        compose.onNodeWithText("Velg et annet hus").assertIsNotEnabled()
        assertEquals(1, calls)
    }
}
