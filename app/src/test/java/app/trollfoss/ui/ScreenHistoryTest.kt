package app.trollfoss.ui

import org.junit.Assert.*
import org.junit.Test

class ScreenHistoryTest {
    @Test fun freshAppStartsOnMap() { assertSame(Screen.Map, ScreenHistory().current) }
    @Test fun everyMenuReturnsToItsOpeningScreen() {
        for (origin in listOf(Screen.Map, Screen.Play)) {
            for (menu in listOf(Screen.Book, Screen.Tasks, Screen.Creator(null), Screen.ParentGate)) {
                val history = ScreenHistory()
                history.arrive(origin)
                history.open(menu)
                assertSame(origin, history.back())
            }
        }
    }
    @Test fun solvedParentGateReturnsToMapWithoutReopeningPuzzle() {
        val history = ScreenHistory()
        history.open(Screen.ParentGate)
        history.open(Screen.Parent)
        assertSame(Screen.Map, history.back())
    }
    @Test fun nestedMenusAndRepeatedOpenKeepTheirOrigins() {
        val history = ScreenHistory()
        history.open(Screen.Book)
        history.open(Screen.Book)
        history.open(Screen.Creator(24))
        assertSame(Screen.Book, history.back())
        assertSame(Screen.Map, history.back())
    }
    @Test fun travellingDiscardsOldMenus() {
        val history = ScreenHistory()
        history.open(Screen.Tasks)
        history.arrive(Screen.Play)
        history.open(Screen.Book)
        assertSame(Screen.Play, history.back())
        history.open(Screen.Map)
        assertSame(Screen.Play, history.back())
    }
}
