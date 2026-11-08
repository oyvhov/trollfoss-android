package app.trollfoss.domain

/** A disposable drawing model. Never installs fixtures or changes the saved house. */
class RoomSuggestion(val place: PlaceId, val slot: Int, val kind: RoomKind) {
    val pieces: List<Fixture> = MineRooms.preset(kind).mapIndexed { index, pc ->
        val y = if (pc.wallY.isNaN()) place.floor + pc.shift else pc.wallY
        Fixture(-1 - kind.ordinal * 100 - index, place, pc.type, slot * Mine.SLOT_W + pc.dx, y, pc.variant, y).also { it.on = pc.on }
    }.sortedBy { if (it.spec.wall) -10f else if (it.spec.h < RUG_HEIGHT) -5f else it.y } // rugs lie under the furniture

    private companion object { const val RUG_HEIGHT = 0.05f }

    fun visible(house: MineHouse): Boolean = house.job == null && house.selectedPlace == place &&
        house.selected == slot && Mine.canBuild(house, place, slot)
}
