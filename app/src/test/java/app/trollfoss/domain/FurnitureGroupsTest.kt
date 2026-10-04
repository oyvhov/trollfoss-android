package app.trollfoss.domain

import org.junit.Assert.*
import org.junit.Test

class FurnitureGroupsTest {
    @Test fun familiarFurnitureIsFoundUnderItsPictureCategory() {
        assertEquals(FurnitureGroup.SEATING, FurnitureGroups.of(FixtureType.BED))
        assertEquals(FurnitureGroup.SEATING, FurnitureGroups.of(FixtureType.SOFA))
        assertEquals(FurnitureGroup.TABLES, FurnitureGroups.of(FixtureType.TABLE))
        assertEquals(FurnitureGroup.STORAGE, FurnitureGroups.of(FixtureType.TREASURE_BOX))
        assertEquals(FurnitureGroup.STORAGE, FurnitureGroups.of(FixtureType.WARDROBE))
        assertEquals(FurnitureGroup.KITCHEN, FurnitureGroups.of(FixtureType.BATH))
        assertEquals(FurnitureGroup.PLAY, FurnitureGroups.of(FixtureType.PIANO))
        assertEquals(FurnitureGroup.DECOR, FurnitureGroups.of(FixtureType.LAMP))
    }
    @Test fun everyPlaceCatalogueRemainsReachableAndEachItemHasOneSpecificCategory() {
        for (place in PlaceId.entries) for (item in Decor.catalogue(place)) {
            assertTrue(FurnitureGroups.matches(FurnitureGroup.ALL, item.type))
            assertEquals(1, FurnitureGroup.entries.drop(1).count { FurnitureGroups.matches(it, item.type) })
        }
    }
}
