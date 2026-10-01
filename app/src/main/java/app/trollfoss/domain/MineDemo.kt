package app.trollfoss.domain

/** Debug builds only: a ready-made house for taking pictures (`--es mine demo`). */
object MineDemo {
    /** Set 1 shows living room, kitchen, dining room, bedroom and, upstairs, kids' room, bathroom, library, workshop; set 2 the rest. */
    fun fill(sim: Sim, set: Int, shape: Int) {
        val world = sim.world
        if (world.mine.started) return
        sim.mine.layFoundation(shape)
        sim.mine.finishJob()
        val ground = if (set == 2) listOf(RoomKind.MUSIC, RoomKind.GREENHOUSE, RoomKind.LIVING, RoomKind.KIDS) else listOf(RoomKind.LIVING, RoomKind.KITCHEN, RoomKind.DINING, RoomKind.BEDROOM)
        val upper = if (set == 2) listOf(RoomKind.BEDROOM, RoomKind.BATH, RoomKind.GREENHOUSE, RoomKind.MUSIC) else listOf(RoomKind.KIDS, RoomKind.BATH, RoomKind.LIBRARY, RoomKind.WORKSHOP)
        for ((i, kind) in ground.withIndex()) {
            sim.mine.buildRoom(PlaceId.MINE_GROUND, i + 1, kind)
            sim.mine.finishJob()
        }
        sim.mine.buildUpper()
        sim.mine.finishJob()
        for ((i, kind) in upper.withIndex()) {
            sim.mine.buildRoom(PlaceId.MINE_UPPER, i + 1, kind)
            sim.mine.finishJob()
        }
    }
}
