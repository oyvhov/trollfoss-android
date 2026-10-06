package app.trollfoss.ui

import app.trollfoss.domain.First
import app.trollfoss.domain.FirstGroup
import org.junit.Assert.*
import org.junit.Test

class FirstStringsTest {
    @Test fun everyFirstHasANameInBothLanguages() {
        for (f in First.entries) {
            val t = SO.first(f)
            assertTrue(f.name, t.nn.isNotBlank() && t.nb.isNotBlank())
        }
        assertEquals(First.entries.size, First.entries.map { SO.first(it).nn }.toSet().size)
    }

    @Test fun everyGroupHasAHeading() { FirstGroup.entries.forEach { assertTrue(SO.group(it).nn.isNotBlank()) } }
}
