package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class ResidentsTest {
    private fun withoutMailinn(): World = WorldFactory.create().also { w ->
        w.bodies.values.removeIf { it is Person && it.name == "Mailinn" }; w.flags -= Residents.MAILINN_FLAG
    }

    @Test fun newWorldsHaveMailinnAtHomeAddedLast() {
        val w = WorldFactory.create()
        val m = w.people().single { it.name == "Mailinn" }
        assertEquals(PlaceId.HOME, m.place)
        assertEquals(Species.FOLK, m.species)
        assertEquals(w.people().maxOf { it.id }, m.id)
        assertTrue(Residents.MAILINN_FLAG in w.flags)
        assertTrue(m.look.height < 1f)
    }

    @Test fun otherVillagersKeepTheirIdsAndVoices() {
        val a = WorldFactory.create(); val b = withoutMailinn()
        for (p in b.people().filter { it.name.isNotBlank() }) {
            val same = a.people().single { it.name == p.name }
            assertEquals(p.name, p.id, same.id); assertEquals(p.name, p.voice, same.voice, 0f)
        }
    }

    @Test fun oldSavesGetMailinnOnce() {
        val w = withoutMailinn()
        val ids = w.people().filter { it.name.isNotBlank() }.associate { it.name to it.id }
        val back = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val again = WorldStore.decode(WorldStore.encode(back, Settings())).world
        assertEquals(1, again.people().count { it.name == "Mailinn" })
        ids.forEach { (n, id) -> assertEquals(n, id, again.people().first { it.name == n }.id) }
    }

    @Test fun aChildMadeMailinnIsNotDuplicated() {
        val w = withoutMailinn()
        w.addPerson(Species.FOLK, Look(), 1f, PlaceId.BEACH, 1f, PlaceId.BEACH.floor, "Mailinn")
        assertNull(Residents.addMailinn(w))
        assertEquals(1, w.people().count { it.name == "Mailinn" })
    }
}
