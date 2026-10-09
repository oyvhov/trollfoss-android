package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MoreFamilyPortraitTest(private val name: String, private val old: Look, private val portrait: Look) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}") fun portraits() = listOf(
            arrayOf("Sondre",Look(skin=6,height=1.03f,hair=1,hairColor=0,top=0,topColor=9,bottom=0,bottomColor=11,shoes=11,extra=3),Residents.sondreLook()),
            arrayOf("Elise",Look(skin=0,height=1.03f,hair=6,hairColor=7,eyes=3,top=4,topColor=10,bottom=0,bottomColor=10,shoes=8),Residents.eliseLook()),
            arrayOf("Sølve",Look(skin=3,height=1.14f,hair=1,hairColor=2,top=4,topColor=3,bottom=0,bottomColor=3,shoes=12,extra=3),Residents.solveLook()),
            arrayOf("Hedda",Look(skin=1,height=.78f,hair=3,hairColor=5,top=1,topColor=5,bottom=1,bottomColor=11,shoes=0,extra=1),Residents.heddaLook()),
            arrayOf("Berit",Look(skin=6,height=1.03f,hair=4,hairColor=0,eyes=3,top=1,topColor=8,bottom=2,bottomColor=11,shoes=11),Residents.beritLook()),
            arrayOf("Olvar",Look(skin=4,height=.9f,hair=2,hairColor=0,eyes=1,top=4,topColor=9,bottom=0,bottomColor=9,shoes=10),Residents.olvarLook()),
        )
    }
    private fun reload(w: World)=WorldStore.decode(WorldStore.encode(w,Settings())).world
    private fun olderWorld()=WorldFactory.create().also { w ->
        w.flags -= Residents.MORE_FAMILY_FLAG
        w.people().single { it.name==name }.also { p ->
            p.look=old
            if(name=="Berit") w.worn(p,Slot.FACE)?.let { w.bodies.remove(it.id) }
        }
        // The previous family marker stays: this update must work independently.
        assertTrue(Residents.FAMILY_FLAG in w.flags)
    }

    @Test fun newWorldAndReloadUseValidPortrait() {
        val w=reload(WorldFactory.create())
        assertEquals(portrait,w.people().single { it.name==name }.look)
        assertEquals(portrait,portrait.safe())
    }
    @Test fun upgradePreservesIdsPlayerVoiceSeatAndHeldThings() {
        val w=olderWorld();val p=w.people().single { it.name==name }
        val ids=w.people().map { it.id }
        w.playerIds += p.id
        val flower=w.addThing(ThingType.FLOWER,1,p.place!!,p.x,p.y)
        Sim(w).give(p,flower,Part.HAND)
        val after=reload(w);val q=after.bodies[p.id] as Person
        assertEquals(portrait,q.look);assertEquals(ids,after.people().map { it.id })
        assertEquals(p.voice,q.voice,0f);assertEquals(w.playerIds,after.playerIds)
        assertEquals(p.mode,q.mode);assertEquals(p.holder,q.holder);assertEquals(p.slot,q.slot)
        assertEquals(p.place,q.place);assertEquals(p.x,q.x,0f);assertEquals(p.y,q.y,0f)
        assertEquals(w.worn(p,Slot.HEAD)?.id,after.worn(q,Slot.HEAD)?.id)
        assertEquals(flower.id,after.worn(q,Slot.HAND)?.id)
    }
    @Test fun editedOrRenamedPortraitIsNotReplaced() {
        for(rename in listOf(false,true)) {
            val w=olderWorld();val p=w.people().single { it.name==name }
            if(rename) p.name="Min venn" else p.look=p.look.copy(topColor=4)
            val q=reload(w).bodies[p.id] as Person
            assertEquals(p.name,q.name);assertEquals(p.look,q.look)
        }
    }
    @Test fun laterChangesStayEvenIfTheyMatchTheOldDefault() {
        val w=reload(olderWorld());val p=w.people().single { it.name==name }
        p.look=old
        assertEquals(old,(reload(w).bodies[p.id] as Person).look)
    }
}
