package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class StorageRecoveryTest {
    @Test fun recoverAnIndividualRemovedPieceAfterRestartWithoutLosingItsVariant() {
        val world = WorldFactory.create()
        world.storage += listOf(Stored(FixtureType.FLOWER_POT, 3), Stored(FixtureType.BED, 2))
        val sim = Sim(world)
        assertTrue(sim.designer.discard(0))
        assertTrue(sim.designer.discard(0))
        val restored = WorldStore.decode(WorldStore.encode(world, Settings())).world
        val afterRestart = Sim(restored)
        assertTrue(afterRestart.designer.restoreDiscarded(0))
        assertEquals(listOf(Stored(FixtureType.FLOWER_POT, 3)), restored.storage)
        assertEquals(1, restored.discardedStorage.size)
        assertFalse(afterRestart.designer.restoreDiscarded(99))
        assertTrue(afterRestart.designer.undoDiscard())
        assertEquals(2, restored.storage.size)
        assertTrue(restored.storage.contains(Stored(FixtureType.BED, 2)))
        assertTrue(restored.discardedStorage.isEmpty())
        assertFalse(afterRestart.designer.undoDiscard())
    }
    @Test fun legacyWorldHasAnEmptyRecyclingBoxAndKeepsExistingStorage() {
        val world = WorldFactory.create()
        world.storage += Stored(FixtureType.BENCH, 1)
        val json = WorldStore.encode(world, Settings()).apply { remove("discardedStorage") }
        val restored = WorldStore.decode(json).world
        assertTrue(restored.discardedStorage.isEmpty())
        assertEquals(world.storage, restored.storage)
    }
    @Test fun hallCanBeSelectedForOrientationButCannotBeBuiltOrDemolished() {
        val world = WorldFactory.create()
        val sim = Sim(world)
        sim.mine.layFoundation(0)
        sim.mine.finishJob()
        sim.mine.select(PlaceId.MINE_GROUND, 0)
        assertEquals(0, world.mine.selected)
        assertFalse(sim.mine.buildRoom(PlaceId.MINE_GROUND, 0, RoomKind.BATH))
        assertFalse(Mine.canDemolish(world.mine, PlaceId.MINE_GROUND, 0))
    }
}
