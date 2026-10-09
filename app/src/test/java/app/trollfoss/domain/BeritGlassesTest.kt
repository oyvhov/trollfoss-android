package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class BeritGlassesTest {
    private fun reload(w: World)=WorldStore.decode(WorldStore.encode(w,Settings())).world
    @Test fun glassesAreAnOrdinaryTransferableItemAndDoNotGrowBack() {
        val w=WorldFactory.create();val berit=w.people().single { it.name=="Berit" }
        val glasses=w.worn(berit,Slot.FACE)!!
        assertEquals(ThingType.ROUND_GLASSES,glasses.type);assertEquals(1,glasses.variant)
        assertTrue(glasses.id > w.people().maxOf { it.id })
        val hedda=w.people().single { it.name=="Hedda" }
        Sim(w).give(hedda,glasses,Part.GLASSES)
        val after=reload(w)
        assertNull(after.worn(after.bodies[berit.id] as Person,Slot.FACE))
        assertEquals(glasses.id,after.worn(after.bodies[hedda.id] as Person,Slot.FACE)?.id)
        assertEquals(1,(after.bodies[glasses.id] as Thing).variant)
        assertEquals(w.bodies.size,after.bodies.size)
    }
    @Test fun upgradingDoesNotReplaceTheChildsGlasses() {
        val w=WorldFactory.create();val berit=w.people().single { it.name=="Berit" }
        val chosen=w.addThing(ThingType.STAR_GLASSES,0,berit.place!!,berit.x,berit.y)
        Sim(w).give(berit,chosen,Part.GLASSES)
        w.flags -= Residents.MORE_FAMILY_FLAG
        val count=w.bodies.size
        val after=reload(w)
        assertEquals(chosen.id,after.worn(after.bodies[berit.id] as Person,Slot.FACE)?.id)
        assertEquals(count,after.bodies.size)
    }
}
