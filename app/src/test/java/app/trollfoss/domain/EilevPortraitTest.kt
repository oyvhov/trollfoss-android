package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class EilevPortraitTest {
    private val old = Look(skin = 0, height = .9f, hair = 8, hairColor = 5, eyes = 0,
        top = 3, topColor = 1, bottom = 0, bottomColor = 1, shoes = 10, extra = 1)
    private fun olderWorld() = WorldFactory.create().also { w ->
        w.flags -= Residents.EILEV_FLAG
        w.people().single { it.name == "Eilev" }.look = old
    }
    private fun reload(w: World) = WorldStore.decode(WorldStore.encode(w, Settings())).world

    @Test fun freshWorldUsesThePortraitAndItsNewChoicesSurviveSaving() {
        val w = reload(WorldFactory.create())
        val e = w.people().single { it.name == "Eilev" }
        assertEquals(Residents.eilevLook(),e.look)
        assertEquals(e.look,e.look.safe())
    }

    @Test fun originalEilevGetsThePortraitWithoutLosingIdentityOrAccessories() {
        val w = olderWorld(); val e = w.people().single { it.name == "Eilev" }
        w.playerIds += e.id
        val cap = w.addThing(ThingType.CAP,0,e.place!!,e.x,e.y).apply { mode=Mode.WORN;holder=e.id;slot=Slot.HEAD.ordinal }
        val back = reload(w); val after = back.bodies[e.id] as Person
        assertEquals(Residents.eilevLook(),after.look)
        assertEquals(e.voice,after.voice,0f); assertEquals(e.place,after.place)
        assertEquals(w.playerIds,back.playerIds)
        assertEquals(cap.id,back.worn(after,Slot.HEAD)?.id)
        assertEquals(after.look,(reload(back).bodies[e.id] as Person).look)
    }

    @Test fun editedClothesAndRenamedCharactersStayExactlyAsTheChildLeftThem() {
        for (rename in listOf(false,true)) {
            val w = olderWorld(); val e = w.people().single { it.name == "Eilev" }
            if (rename) e.name="Min figur" else e.look=e.look.copy(topColor=4)
            val before=e.look; val after=reload(w).bodies[e.id] as Person
            assertEquals(before,after.look); assertEquals(e.name,after.name)
        }
    }

    @Test fun aLaterEditIsNotOverwrittenOnTheNextLaunch() {
        val w=reload(olderWorld()); val e=w.people().single { it.name=="Eilev" }
        e.look=old
        assertEquals(old,(reload(w).bodies[e.id] as Person).look)
    }
}
