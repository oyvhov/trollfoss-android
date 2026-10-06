package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class LampsTest {
    @Test fun lampsFollowTheNightAndAChildsChoiceLastsUntilTheNextChange() {
        val s = Sim(World())
        val a = s.designer.add(PlaceId.HOME, FixtureType.LAMP, 0, 1f, PlaceId.HOME.floor)!!
        val b = s.designer.add(PlaceId.CAFE, FixtureType.LAMP, 0, 1f, PlaceId.CAFE.floor)!!
        s.lampsFollowNight(true)
        assertTrue(a.on && b.on)
        a.on = false // the child turns one off in the night
        s.step(PlaceId.HOME, 0.5f)
        assertFalse("the child's choice stays", a.on)
        s.lampsFollowNight(false)
        assertFalse(a.on || b.on)
    }
}
