package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class WaterfallSecretTest {
    @Test fun theCaveHasNoMapSignAndHintsLeadToTheWaterUntilItIsDiscovered() {
        val w = WorldFactory.create()
        assertFalse(PlaceId.LAB.onMap)
        assertFalse(WaterfallSecret.known(w))
        assertEquals(PlaceId.FOREST, WaterfallSecret.destination(w, PlaceId.LAB))
        assertEquals(PlaceId.HOME, WaterfallSecret.destination(w, PlaceId.HOME))
        Sim(w).visit(PlaceId.LAB)
        assertTrue(WaterfallSecret.known(w))
        assertEquals(PlaceId.LAB, WaterfallSecret.destination(w, PlaceId.LAB))
    }

    @Test fun anExistingCaveAndItsContentsSurviveDiscoveryAndReload() {
        val w = WorldFactory.create()
        val rumle = w.people().single { it.name == "Rumle" }
        val wand = w.bodiesIn(PlaceId.LAB).filterIsInstance<Thing>().first { it.type == ThingType.WAND }
        wand.x = 1.6f; wand.y = 0.95f
        Sim(w).visit(PlaceId.LAB)
        w.place = PlaceId.FOREST
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        assertTrue(WaterfallSecret.known(loaded))
        assertEquals(rumle.id, loaded.people().single { it.name == "Rumle" }.id)
        assertEquals(1.6f, loaded.bodies.getValue(wand.id).x, 0.001f)
        assertEquals(w.bodies.size, loaded.bodies.size)
    }

    @Test fun theExitReturnsToDryGroundAndTheWaterDoesNotCoverOtherScenes() {
        val exit = WaterfallSecret.exit
        val x = WaterfallSecret.arrival(exit)
        val water = Places.spec(exit.to).water!!
        assertTrue(x < water.x1 || x > water.x2)
        assertTrue(WaterfallSecret.at(PlaceId.FOREST, 3.02f, 0.59f))
        assertTrue(WaterfallSecret.at(PlaceId.LAB, 2.735f, 0.235f))
        assertFalse(WaterfallSecret.at(PlaceId.FOREST, 2.4f, 0.59f))
        assertFalse(WaterfallSecret.at(PlaceId.HOME, 3.02f, 0.59f))
        assertFalse(WaterfallSecret.at(PlaceId.LAB, 2.735f, 0.95f))
    }
}
