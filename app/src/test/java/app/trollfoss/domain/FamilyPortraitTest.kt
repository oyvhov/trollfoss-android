package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FamilyPortraitTest(private val name: String, private val old: Look, private val portrait: Look) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}") fun portraits() = listOf(
            arrayOf("Eira", Look(skin=5,height=.78f,hair=5,hairColor=0,eyes=1,top=3,topColor=0,bottom=1,bottomColor=11,shoes=1), Residents.eiraLook()),
            arrayOf("Olve", Look(skin=7,height=.9f,hair=2,hairColor=0,top=1,topColor=1,bottom=1,bottomColor=3,shoes=6), Residents.olveLook()),
            arrayOf("Tuva", Look(skin=5,height=1.03f,hair=6,hairColor=0,eyes=3,top=2,topColor=7,bottom=2,bottomColor=7,shoes=8), Residents.tuvaLook()),
            arrayOf("Øyvind", Look(skin=2,height=1.14f,hair=1,hairColor=1,top=1,topColor=6,bottom=0,bottomColor=11,shoes=12,extra=2), Residents.oyvindLook()),
        )
    }

    private fun reload(w: World) = WorldStore.decode(WorldStore.encode(w, Settings())).world
    private fun olderWorld() = WorldFactory.create().also { w ->
        w.flags -= Residents.FAMILY_FLAG
        w.people().single { it.name == name }.look = old
    }

    @Test fun freshPortraitIsValidAndSurvivesSaving() {
        val w = reload(WorldFactory.create())
        assertEquals(portrait, w.people().single { it.name == name }.look)
        assertEquals(portrait, portrait.safe())
    }

    @Test fun upgradePreservesPlayerIdentityVoiceLocationAndAccessories() {
        val w = olderWorld()
        val p = w.people().single { it.name == name }
        w.playerIds += p.id
        val hat = w.addThing(ThingType.BEANIE, 1, p.place!!, p.x, p.y)
        val held = w.addThing(ThingType.FLOWER, 2, p.place!!, p.x, p.y)
        Sim(w).apply { give(p, hat, Part.HAT); give(p, held, Part.HAND) }
        val result = reload(w)
        val after = result.bodies[p.id] as Person
        assertEquals(portrait, after.look)
        assertEquals(p.voice, after.voice, 0f)
        assertEquals(p.place, after.place); assertEquals(p.x, after.x, 0f); assertEquals(p.y, after.y, 0f)
        assertEquals(p.holder, after.holder); assertEquals(p.slot, after.slot); assertEquals(p.mode, after.mode)
        assertEquals(w.playerIds, result.playerIds)
        assertEquals(hat.id, result.worn(after, Slot.HEAD)?.id)
        assertEquals(p.id, result.bodies[held.id]?.holder)
        assertEquals(Mode.WORN, result.bodies[held.id]?.mode)
    }

    @Test fun editedOrRenamedFriendsStayAsTheChildLeftThem() {
        for (rename in listOf(false, true)) {
            val w = olderWorld(); val p = w.people().single { it.name == name }
            if (rename) p.name = "Min figur" else p.look = p.look.copy(topColor = 4)
            val after = reload(w).bodies[p.id] as Person
            assertEquals(p.look, after.look); assertEquals(p.name, after.name)
        }
    }

    @Test fun laterEditsAreNotOverwritten() {
        val w = reload(olderWorld()); val p = w.people().single { it.name == name }
        p.look = old
        assertEquals(old, (reload(w).bodies[p.id] as Person).look)
    }
}
