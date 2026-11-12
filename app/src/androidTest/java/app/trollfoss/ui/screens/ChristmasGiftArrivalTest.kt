package app.trollfoss.ui.screens

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.ViewModelStore
import app.trollfoss.domain.Anatomy
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Part
import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.SO
import app.trollfoss.ui.TrollfossApp
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.TrollfossTheme
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ChristmasGiftArrivalTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    @get:Rule val saves = TemporaryFolder()
    private val models = ViewModelStore()

    @After fun releaseGame() = rule.runOnUiThread { models.clear() }

    @Test fun unopenedLevelGiftLeavesSantaTouchableAndStillDeliversHisVisitorAToy() {
        val save = saves.newFolder()
        val vm = rule.runOnUiThread {
            val app = object : Application() {
                init { attachBaseContext(rule.activity.application) }
                override fun getFilesDir(): File = save
            }
            TrollfossViewModel(app).also {
                models.put("gift-arrival", it)
                it.updateSettings { settings -> settings.copy(sound = false, music = false, haptics = false) }
                it.splash = false
                it.travel(PlaceId.HOME)
                it.world.stickers.addAll(listOf(0, 1))
                it.refreshTasks()
                assertEquals(2, it.levelGift)
            }
        }
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrollfossTheme {
                CompositionLocalProvider(LocalMotion provides false) { TrollfossApp(vm) }
            }
        }
        rule.mainClock.advanceTimeBy(96)
        val parcel = rule.onNodeWithContentDescription(SO.parcel.nn).assertIsDisplayed()
        val scene = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val usual = parcel.fetchSemanticsNode().boundsInRoot
        assertEquals(scene.center.x, usual.center.x, 1f)
        assertEquals(scene.top + scene.height * 0.62f, usual.center.y, 1f)

        rule.runOnIdle { vm.travel(PlaceId.CHRISTMAS_HOME) }
        rule.mainClock.advanceTimeBy(96)
        val gift = parcel.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(gift.right < scene.center.x)
        val santa = rule.runOnIdle {
            vm.world.people().single { it.place == PlaceId.CHRISTMAS_HOME && it.name == "Julenissen" }
        }
        val face = rule.runOnIdle {
            val engine = requireNotNull(vm.engine)
            val eyes = Anatomy.at(santa, Part.GLASSES)
            Offset((eyes[0] - engine.cam) * engine.u, scene.height - engine.u + eyes[1] * engine.u)
        }
        assertTrue(scene.contains(scene.topLeft + face))
        assertFalse(gift.contains(scene.topLeft + face))
        rule.onRoot().performTouchInput { click(face) }
        rule.runOnIdle {
            assertTrue("Santa responded to the child's tap", santa.anim.nameTag > 0f)
            assertEquals(2, vm.levelGift)
        }

        fun bubbles() = vm.world.fixturesIn(PlaceId.CHRISTMAS_HOME).count { it.type == FixtureType.PLAY_BUBBLES }
        val before = rule.runOnIdle { bubbles() }
        parcel.performTouchInput { click() }
        rule.mainClock.advanceTimeBy(32)
        rule.runOnIdle {
            assertEquals(0, vm.levelGift)
            assertEquals(2, vm.parcelOpening)
            assertEquals(before + 1, bubbles())
            assertTrue("gift:level:seen:2" in vm.world.flags)
        }
        rule.mainClock.advanceTimeBy(650)
        parcel.assertDoesNotExist()
        rule.runOnIdle { assertEquals(0, vm.parcelOpening) }
    }
}
