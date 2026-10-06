package app.trollfoss.ui

import app.trollfoss.domain.First
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.screens.FirstSubject
import app.trollfoss.ui.screens.firstSubject
import org.junit.Assert.*
import org.junit.Test

class FirstSubjectsTest {
    @Test fun everyFirstHasAPicture() { First.entries.forEach { assertNotNull(it.name, firstSubject(it)) } }

    @Test fun toyFirstsShowTheirToy() {
        assertEquals(FirstSubject.Fixture(FixtureType.PLAY_CRANE), firstSubject(First.CRANE))
        assertEquals(FirstSubject.Fixture(FixtureType.TRACTOR), firstSubject(First.TRACTOR))
        assertEquals(FirstSubject.Thing(ThingType.BOOK), firstSubject(First.READ))
        assertEquals(FirstSubject.Troll, firstSubject(First.PEEK_TROLL))
    }
}
