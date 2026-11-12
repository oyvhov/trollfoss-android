package app.trollfoss.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.trollfoss.domain.Christmas
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Thing
import app.trollfoss.ui.LocalMaalform
import app.trollfoss.ui.SX
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.TrollfossTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChristmasPanelTravelTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val visible = mutableStateOf(true)

    private fun show(place: PlaceId, from: Screen, tab: ChristmasTab = ChristmasTab.TRAVEL): TrollfossViewModel {
        val vm = rule.runOnUiThread {
            ViewModelProvider(rule.activity)[TrollfossViewModel::class.java].apply {
                travel(place)
                open(from)
            }
        }
        rule.setContent {
            TrollfossTheme {
                CompositionLocalProvider(LocalMaalform provides Maalform.NYNORSK, LocalMotion provides false) {
                    if (visible.value) ChristmasPanel(vm, onClose = { visible.value = false }, initialTab = tab)
                }
            }
        }
        return vm
    }

    @Test fun openMapCanLeaveForVillageVisitWorkshopAndReturnToFarm() {
        val vm = show(PlaceId.FARM, Screen.Map)

        rule.onNodeWithContentDescription("Til Nissegrenda").assertIsDisplayed().performClick()
        rule.runOnIdle {
            assertEquals(Screen.Play, vm.screen)
            assertEquals(PlaceId.CHRISTMAS_SQUARE, vm.place)
            assertEquals(PlaceId.FARM, vm.christmasReturnPlace)
            assertNull(vm.balloonDestination)
            visible.value = true
        }
        rule.onNodeWithContentDescription("Juleverkstaden").assertIsDisplayed().performClick()
        rule.runOnIdle {
            assertEquals(PlaceId.CHRISTMAS_WORKSHOP, vm.place)
            visible.value = true
        }
        rule.onNodeWithContentDescription("Tilbake til Garden").performScrollTo().performClick()
        rule.runOnIdle {
            assertEquals(PlaceId.FARM, vm.place)
            assertEquals(Screen.Play, vm.screen)
        }
    }

    @Test fun takingSuppliesFromMapShowsTheToyInTheCurrentDollhouseScene() {
        checkDelivery(PlaceId.HOME, Screen.Map, PlaceId.HOME)
    }

    @Test fun takingSuppliesOnMountainArrivesWithToyAtVillageTree() {
        checkDelivery(PlaceId.HEILEBERGET, Screen.Play, PlaceId.CHRISTMAS_SQUARE)
    }

    private fun checkDelivery(start: PlaceId, screen: Screen, delivery: PlaceId) {
        val vm = show(start, screen, ChristmasTab.SUPPLIES)
        val before = rule.runOnIdle { vm.world.bodiesIn(delivery).filterIsInstance<Thing>().toList() }
        val type = Christmas.things.minBy { candidate -> before.count { it.type == candidate } }
        val name = SX.thing(type).nn
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(name))
        rule.onNode(hasText(name) and hasClickAction()).performClick()
        rule.runOnIdle {
            assertEquals(Screen.Play, vm.screen)
            assertEquals(delivery, vm.place)
            val priorIds = before.map { it.id }.toSet()
            val added = vm.world.bodiesIn(delivery).filterIsInstance<Thing>().single { it.type == type && it.id !in priorIds }
            assertTrue(added.x in 0f..delivery.width)
            if (start == PlaceId.HEILEBERGET) {
                assertEquals(start, vm.christmasReturnPlace)
                val tree = vm.world.fixturesIn(delivery).first { it.type == app.trollfoss.domain.FixtureType.XM_TREE }
                assertEquals(tree.x, added.x, .01f)
            }
            vm.world.remove(added)
            vm.changed()
        }
    }
}
