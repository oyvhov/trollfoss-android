package app.trollfoss.domain

/** Picture categories for the furniture drawer. Every catalogue item remains available in ALL. */
enum class FurnitureGroup { ALL, SEATING, TABLES, STORAGE, KITCHEN, PLAY, DECOR }

object FurnitureGroups {
    fun of(type: FixtureType): FurnitureGroup = when {
        type in setOf(FixtureType.STOVE, FixtureType.FRIDGE, FixtureType.SINK, FixtureType.BATH,
            FixtureType.TOILET, FixtureType.OVEN, FixtureType.BLENDER, FixtureType.ICE_CREAM_MACHINE,
            FixtureType.HAIR_WASH, FixtureType.DRYER_HOOD) -> FurnitureGroup.KITCHEN
        type in CreativePlay.TYPES || type in setOf(FixtureType.PIANO, FixtureType.TV, FixtureType.RADIO) ||
            type.spec.machine in setOf(Machine.BUILD, Machine.GARDEN, Machine.TARGET) -> FurnitureGroup.PLAY
        type.spec.container != null || type in setOf(FixtureType.SHELF, FixtureType.BOOKCASE, FixtureType.CLOTHES_RACK) -> FurnitureGroup.STORAGE
        type.spec.spots.isNotEmpty() -> FurnitureGroup.SEATING
        type.spec.surfaces.isNotEmpty() && !type.spec.wall -> FurnitureGroup.TABLES
        else -> FurnitureGroup.DECOR
    }
    fun matches(group: FurnitureGroup, type: FixtureType) = group == FurnitureGroup.ALL || of(type) == group
}
