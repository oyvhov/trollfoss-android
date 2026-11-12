package app.trollfoss.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import app.trollfoss.domain.FestivalChoice
import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.S
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.TrollfossTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChristmasMapTravelTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun openMap(from: PlaceId): TrollfossViewModel {
        lateinit var vm: TrollfossViewModel
        rule.runOnUiThread {
            vm = ViewModelProvider(rule.activity)[TrollfossViewModel::class.java]
            vm.travel(from)
            vm.open(Screen.Map)
        }
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TrollfossTheme {
                CompositionLocalProvider(LocalMotion provides false) {
                    if (vm.screen == Screen.Map) MapScreen(vm)
                }
            }
        }
        rule.mainClock.advanceTimeBy(96)
        return vm
    }

    @Test
    fun villageSignWorksWithoutPanningEvenWithSeasonalDecorationsOff() {
        val vm = openMap(PlaceId.HOME)
        val previous = vm.settings.festival
        try {
            rule.runOnIdle { vm.setFestival(FestivalChoice.NONE) }
            rule.mainClock.advanceTimeBy(32)
            val sign = rule.onNodeWithContentDescription("Til Nissegrenda")
                .assertIsDisplayed().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
            val signBounds = sign.fetchSemanticsNode().boundsInRoot
            val weather = rule.onNodeWithContentDescription(S.weather.nn).fetchSemanticsNode().boundsInRoot
            val parents = rule.onNodeWithContentDescription(S.parents.nn).fetchSemanticsNode().boundsInRoot
            assertTrue(weather.right <= signBounds.left && signBounds.right <= parents.left)
            sign.performTouchInput { click(); click() }
            rule.mainClock.advanceTimeBy(240)
            rule.runOnIdle {
                assertEquals(Screen.Play, vm.screen)
                assertEquals(PlaceId.CHRISTMAS_SQUARE, vm.place)
            }
        } finally {
            rule.runOnIdle { vm.setFestival(previous) }
        }
    }

    @Test
    fun travelRequestedOnAnAlreadyOpenMapReachesTheSquareFromItsWorkshop() {
        val vm = openMap(PlaceId.CHRISTMAS_WORKSHOP)
        rule.runOnIdle { vm.flyTo(PlaceId.CHRISTMAS_SQUARE) }
        rule.mainClock.advanceTimeBy(240)
        rule.runOnIdle {
            assertEquals(Screen.Play, vm.screen)
            assertEquals(PlaceId.CHRISTMAS_SQUARE, vm.place)
            assertEquals(null, vm.balloonDestination)
        }
    }
}
