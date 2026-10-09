package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class PortraitGarmentTest {
    @Test fun everyExistingGarmentKeepsItsStyleColourAndEncoding() {
        for (style in 0..11) for (color in 0..15) {
            val saved = style * 16 + color
            assertEquals(style, Garment.style(saved))
            assertEquals(color, Garment.color(saved))
            assertEquals(saved, Garment.pack(style, color))
        }
    }

    @Test fun newShadesSurviveTakingOffSavingAndPuttingBackOn() {
        for (look in listOf(Residents.eiraLook(), Residents.olveLook(), Residents.oyvindLook())) {
            val w = WorldFactory.create(); val sim = Sim(w)
            val p = w.addPerson(Species.FOLK, look, 1f, PlaceId.HOME, 1f, .9f)
            val shirt = w.addThing(ThingType.GARMENT, Garment.pack(0, 6), PlaceId.HOME, 1f, .9f)
            assertEquals(Give.DRESSED, sim.give(p, shirt, Part.BODY))
            val back = WorldStore.decode(WorldStore.encode(w, Settings())).world
            val reloadedPerson = back.bodies[p.id] as Person
            val reloadedShirt = back.bodies[shirt.id] as Thing
            assertEquals(Give.DRESSED, Sim(back).give(reloadedPerson, reloadedShirt, Part.BODY))
            assertEquals(look, reloadedPerson.look)
        }
    }
}
