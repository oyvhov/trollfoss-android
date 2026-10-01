package app.trollfoss.ui.art

import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Thing

/*
 * Storhuset, ground floor (Storstova): the art. The background is in HouseGroundBack.kt, the furniture of
 * each room in HouseGround<Room>Art.kt and the things in HouseGroundThingsArt.kt (docs/HUSET.md, docs/ART_GUIDE.md).
 * This file only routes: which type is drawn by which room's file.
 */

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawGroundFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        // The ways between the floors
        FixtureType.STAIRCASE -> grStairs(f, u, pen)
        FixtureType.LIFT -> grLift(f, u, pen)
        FixtureType.DOOR -> grDoor(f, u, pen)
        FixtureType.SECRET_DOOR -> grSecretShelf(f, u, pen)
        FixtureType.DUMBWAITER -> grDumbwaiter(f, u, pen)

        // Hall
        FixtureType.GR_CLOCK -> grClock(f, u, pen)
        FixtureType.GR_COAT_RACK -> grCoatRack(f, u, pen)
        FixtureType.GR_UMBRELLA_STAND -> grUmbrellaStand(f, u, pen)
        FixtureType.GR_WINDOW -> grWindow(f, u, pen)
        FixtureType.GR_ARMOUR -> grArmour(f, u, pen)
        FixtureType.GR_CHANDELIER -> grChandelier(f, u, pen)
        FixtureType.GR_POST_SLOT -> grPostSlot(f, u, pen)
        FixtureType.GR_PORTRAIT -> grPortrait(f, u, pen)

        // Living room
        FixtureType.GR_FIREPLACE -> grFireplace(f, u, pen)
        FixtureType.GR_WINGCHAIR -> grWingChair(f, u, pen)
        FixtureType.GR_SOFA -> grSofa(f, u, pen)
        FixtureType.GR_COFFEE_TABLE -> grCoffeeTable(f, u, pen)
        FixtureType.GR_POPCORN_BOWL -> grPopcornBowl(f, u, pen)
        FixtureType.GR_GLOBE -> grGlobe(f, u, pen)
        FixtureType.GR_TV -> grTv(f, u, pen)
        FixtureType.GR_AQUARIUM -> grAquarium(f, u, pen)
        FixtureType.GR_FLOOR_LAMP -> grFloorLamp(f, u, pen)

        // Library
        FixtureType.GR_BOOKSHELF -> grBookshelf(f, u, pen)
        FixtureType.GR_LADDER -> grLadder(f, u, pen)
        FixtureType.GR_DESK -> grDesk(f, u, pen)
        FixtureType.GR_LECTERN -> grLectern(f, u, pen)
        FixtureType.GR_BUST -> grBust(f, u, pen)

        // Dining room
        FixtureType.GR_DINING_TABLE -> grDiningTable(f, u, pen)
        FixtureType.GR_CHAIR_ROW -> grChairRow(f, u, pen)
        FixtureType.GR_DINING_CHAIR -> grDiningChair(f, u, pen)
        FixtureType.GR_BELL -> grBell(f, u, pen)
        FixtureType.GR_CAKE -> grCake(f, u, pen)
        FixtureType.GR_CANDELABRA -> grCandelabra(f, u, pen)
        FixtureType.GR_SIDEBOARD -> grSideboard(f, u, pen)

        // Kitchen
        FixtureType.GR_RANGE -> grRange(f, u, pen)
        FixtureType.GR_SINK -> grSink(f, u, pen)
        FixtureType.GR_MIXER -> grMixer(f, u, pen, contents)
        FixtureType.GR_PIZZA_OVEN -> grPizzaOven(f, u, pen)
        FixtureType.GR_FRIDGE -> grFridge(f, u, pen)
        FixtureType.GR_JAM_CABINET -> grJamCabinet(f, u, pen)
        FixtureType.GR_ISLAND -> grIsland(f, u, pen)
        FixtureType.GR_STOOLS -> grStools(f, u, pen)

        // Winter garden
        FixtureType.GR_PLANT -> grPlant(f, u, pen)
        FixtureType.GR_FOUNTAIN -> grFountain(f, u, pen)
        FixtureType.GR_SOFIE -> grSofie(f, u, pen)
        FixtureType.GR_HAMMOCK -> grHammock(f, u, pen)

        else -> return false
    }
    return true
}

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawGroundFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.GR_HAMMOCK -> grHammockFront(f, u, pen)
        else -> return false
    }
    return true
}
