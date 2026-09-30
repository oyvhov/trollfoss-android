package app.trollfoss.domain

/** A room's wallpaper and floor. 0 is the place's own look; the art knows [Decor.WALLS] and [Decor.FLOORS]. */
data class RoomStyle(val wall: Int = 0, val floor: Int = 0)

/** Furniture put away in the home designer's store, ready to be placed again. */
data class Stored(val type: FixtureType, val variant: Int)

/** Something the catalogue offers: a piece of furniture and, for special pieces, the stickers it takes. */
data class CatalogueItem(val type: FixtureType, val variant: Int = 0, val stickers: Int = 0)

/**
 * The home designer («heimedesignar»): rooms with wallpaper and floors, a furniture catalogue and a
 * store for furniture that is put away. Indoor places can be redecorated; every place can get furniture.
 */
object Decor {
    const val WALLS = 12
    const val FLOORS = 9

    /** Added furniture gets ids from here up within its place (blueprint furniture uses 0 until this). */
    const val FIRST_ADDED = 40
    const val MAX_ADDED = 59

    /** Places whose walls and floors can be changed. */
    fun decoratable(place: PlaceId): Boolean = !place.outdoor && place != PlaceId.LAB && place != PlaceId.SPACE

    /** The rooms of a place, as x ranges. Home has four rooms; other places are one room. */
    fun rooms(place: PlaceId): List<ClosedFloatingPointRange<Float>> = when (place) {
        PlaceId.HOME -> listOf(0f..1.0f, 1.0f..2.45f, 2.45f..3.45f, 3.45f..place.width)
        else -> listOf(0f..place.width)
    }

    fun roomAt(place: PlaceId, x: Float): Int = rooms(place).indexOfFirst { x in it }.coerceAtLeast(0)

    fun key(place: PlaceId, room: Int): String = "${place.name}:$room"

    fun style(world: World, place: PlaceId, room: Int): RoomStyle = world.styles[key(place, room)] ?: RoomStyle()

    fun styles(world: World, place: PlaceId): List<RoomStyle> = rooms(place).indices.map { style(world, place, it) }

    /**
     * What the catalogue offers in [place]: indoor furniture for rooms, garden furniture outside. Pieces
     * with [CatalogueItem.stickers] open up as the child collects stickers from tasks.
     */
    fun catalogue(place: PlaceId): List<CatalogueItem> {
        val indoor = listOf(
            CatalogueItem(FixtureType.RUG, 0), CatalogueItem(FixtureType.RUG, 1), CatalogueItem(FixtureType.RUG, 2), CatalogueItem(FixtureType.RUG, 3),
            CatalogueItem(FixtureType.PICTURE, 0), CatalogueItem(FixtureType.PICTURE, 1), CatalogueItem(FixtureType.PICTURE, 2),
            CatalogueItem(FixtureType.PICTURE, 3, stickers = 2), CatalogueItem(FixtureType.PICTURE, 4, stickers = 4), CatalogueItem(FixtureType.PICTURE, 5, stickers = 6),
            CatalogueItem(FixtureType.FLOWER_POT, 0), CatalogueItem(FixtureType.FLOWER_POT, 1), CatalogueItem(FixtureType.FLOWER_POT, 2),
            CatalogueItem(FixtureType.CHAIR), CatalogueItem(FixtureType.STOOL), CatalogueItem(FixtureType.ARMCHAIR, 0), CatalogueItem(FixtureType.ARMCHAIR, 1),
            CatalogueItem(FixtureType.BEANBAG, 0), CatalogueItem(FixtureType.BEANBAG, 1), CatalogueItem(FixtureType.BEANBAG, 2),
            CatalogueItem(FixtureType.SOFA), CatalogueItem(FixtureType.TABLE), CatalogueItem(FixtureType.ROUND_TABLE), CatalogueItem(FixtureType.DESK),
            CatalogueItem(FixtureType.BED), CatalogueItem(FixtureType.BUNK_BED, stickers = 3),
            CatalogueItem(FixtureType.BOOKCASE), CatalogueItem(FixtureType.SHELF), CatalogueItem(FixtureType.WARDROBE),
            CatalogueItem(FixtureType.TOY_BOX), CatalogueItem(FixtureType.CHEST),
            CatalogueItem(FixtureType.LAMP), CatalogueItem(FixtureType.PLANT_BIG), CatalogueItem(FixtureType.MIRROR), CatalogueItem(FixtureType.CLOCK),
            CatalogueItem(FixtureType.TRASH_BIN), CatalogueItem(FixtureType.RADIO), CatalogueItem(FixtureType.TV),
            CatalogueItem(FixtureType.AQUARIUM, stickers = 5), CatalogueItem(FixtureType.PIANO, stickers = 7),
            CatalogueItem(FixtureType.ROBOT_VACUUM, stickers = 4), CatalogueItem(FixtureType.DISCO_BALL, stickers = 9),
            CatalogueItem(FixtureType.TRAMPOLINE, stickers = 10), CatalogueItem(FixtureType.XYLOPHONE, stickers = 8),
        )
        val outdoor = listOf(
            CatalogueItem(FixtureType.BENCH), CatalogueItem(FixtureType.LAMP_POST), CatalogueItem(FixtureType.UMBRELLA),
            CatalogueItem(FixtureType.LOUNGER), CatalogueItem(FixtureType.STUMP), CatalogueItem(FixtureType.LOG),
            CatalogueItem(FixtureType.HAY_BALE), CatalogueItem(FixtureType.FLOWER_POT, 0), CatalogueItem(FixtureType.FLOWER_POT, 1),
            CatalogueItem(FixtureType.FLOWER_POT, 2), CatalogueItem(FixtureType.TRASH_BIN), CatalogueItem(FixtureType.SNOWMAN),
            CatalogueItem(FixtureType.PINE_TREE, stickers = 2), CatalogueItem(FixtureType.CAMPFIRE, stickers = 3), CatalogueItem(FixtureType.TENT, stickers = 4),
            CatalogueItem(FixtureType.TRAMPOLINE, stickers = 6), CatalogueItem(FixtureType.SANDCASTLE, stickers = 2),
        )
        return when {
            place == PlaceId.UNDERWATER -> listOf(
                CatalogueItem(FixtureType.CORAL, 0), CatalogueItem(FixtureType.CORAL, 1), CatalogueItem(FixtureType.KELP),
                CatalogueItem(FixtureType.GIANT_CLAM, stickers = 3), CatalogueItem(FixtureType.CHEST, stickers = 2),
            )
            place.outdoor -> outdoor
            else -> indoor
        }
    }
}
