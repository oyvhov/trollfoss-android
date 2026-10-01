package app.trollfoss.domain

/**
 * A piece of furniture in a room preset. [dx] is the distance from the slot's left edge (0 to 2). A floor piece stands
 * at the floor line plus [shift] (negative is toward the back wall); a wall piece hangs with its bottom edge at [wallY].
 */
data class MinePiece(
    val type: FixtureType,
    val dx: Float,
    val shift: Float = 0f,
    val variant: Int = 0,
    val wallY: Float = Float.NaN,
    val on: Boolean = false,
)

/** A loose thing that waits on a piece of a preset: [piece] is its index in the list, [rest] the height of the surface. */
data class MineThing(val type: ThingType, val variant: Int, val piece: Int, val dx: Float, val rest: Float)

/**
 * What each room kind brings: furniture (six to nine pieces, plain furniture from the home designer plus a few
 * of its own `MI_` pieces), and a small thing or two. The look of the walls and the floor belongs to the art
 * (it is the kind's own style until the child chooses a wallpaper or a floor).
 */
object MineRooms {
    private fun p(type: FixtureType, dx: Float, shift: Float = 0f, variant: Int = 0, on: Boolean = false) = MinePiece(type, dx, shift, variant, on = on)
    private fun w(type: FixtureType, dx: Float, wallY: Float, variant: Int = 0) = MinePiece(type, dx, 0f, variant, wallY)

    private val BACK = -0.08f

    fun preset(kind: RoomKind): List<MinePiece> = when (kind) {
        RoomKind.LIVING -> listOf(
            p(FixtureType.MI_FIREPLACE, 0.38f, BACK, on = true),
            p(FixtureType.ARMCHAIR, 0.8f, 0.04f),
            p(FixtureType.SOFA, 1.22f, -0.03f),
            p(FixtureType.TV, 1.64f, BACK),
            p(FixtureType.PLANT_BIG, 1.9f, BACK),
            p(FixtureType.RUG, 1.0f, 0.08f, 1),
            w(FixtureType.PICTURE, 0.8f, 0.42f, 0),
            w(FixtureType.PICTURE, 1.2f, 0.4f, 2),
        )
        RoomKind.KITCHEN -> listOf(
            p(FixtureType.FRIDGE, 0.2f, BACK),
            p(FixtureType.STOVE, 0.48f, BACK),
            p(FixtureType.MI_COUNTER, 0.82f, BACK),
            p(FixtureType.SINK, 1.14f, BACK),
            p(FixtureType.OVEN, 1.45f, BACK),
            p(FixtureType.ROUND_TABLE, 1.0f, 0.07f),
            p(FixtureType.STOOL, 0.78f, 0.08f),
            p(FixtureType.STOOL, 1.22f, 0.08f),
            w(FixtureType.SHELF, 0.55f, 0.45f),
        )
        RoomKind.DINING -> listOf(
            p(FixtureType.MI_DINING_TABLE, 1.0f, 0.03f),
            p(FixtureType.CHAIR, 0.68f, 0.04f),
            p(FixtureType.CHAIR, 1.32f, 0.04f),
            p(FixtureType.CHAIR, 0.9f, -0.04f),
            p(FixtureType.CHAIR, 1.1f, -0.04f),
            w(FixtureType.MI_CHANDELIER, 1.0f, 0.22f),
            p(FixtureType.COUNTER, 0.3f, BACK),
            p(FixtureType.RUG, 1.0f, 0.05f, 2),
            w(FixtureType.PICTURE, 1.65f, 0.42f, 4),
        )
        RoomKind.BEDROOM -> listOf(
            p(FixtureType.BED, 0.72f, -0.07f),
            p(FixtureType.MI_BEDSIDE, 0.38f, BACK),
            p(FixtureType.MI_BEDSIDE, 1.06f, BACK, 1),
            p(FixtureType.WARDROBE, 1.62f, BACK),
            p(FixtureType.RUG, 0.72f, 0.08f, 3),
            w(FixtureType.MIRROR, 1.32f, 0.5f),
            w(FixtureType.PICTURE, 0.72f, 0.34f, 3),
            p(FixtureType.LAMP, 1.9f, BACK),
        )
        RoomKind.KIDS -> listOf(
            p(FixtureType.BUNK_BED, 0.5f, BACK),
            p(FixtureType.TOY_BOX, 1.0f, -0.01f),
            p(FixtureType.MI_BLOCKS, 1.35f, 0.03f),
            p(FixtureType.MI_ROCKING_HORSE, 1.7f, 0.04f),
            p(FixtureType.BEANBAG, 1.1f, 0.09f, 1),
            p(FixtureType.RUG, 1.2f, 0.07f, 0),
            w(FixtureType.PICTURE, 0.98f, 0.42f, 1),
            w(FixtureType.PICTURE, 1.42f, 0.38f, 2),
        )
        RoomKind.BATH -> listOf(
            p(FixtureType.BATH, 0.5f, -0.06f),
            p(FixtureType.TOILET, 1.02f, BACK),
            p(FixtureType.SINK, 1.34f, BACK),
            w(FixtureType.MIRROR, 1.34f, 0.5f),
            p(FixtureType.MI_LAUNDRY_BASKET, 1.72f, 0.0f),
            p(FixtureType.RUG, 0.55f, 0.09f, 1),
            p(FixtureType.TRASH_BIN, 1.58f, 0.05f),
            p(FixtureType.PLANT_BIG, 1.92f, BACK),
        )
        RoomKind.LIBRARY -> listOf(
            p(FixtureType.MI_BIG_BOOKCASE, 0.4f, BACK),
            p(FixtureType.BOOKCASE, 0.85f, BACK - 0.01f),
            p(FixtureType.DESK, 1.3f, -0.05f),
            p(FixtureType.CHAIR, 1.3f, 0.06f),
            p(FixtureType.ARMCHAIR, 1.62f, 0.05f, 1),
            p(FixtureType.MI_GLOBE, 1.9f, -0.06f),
            p(FixtureType.RUG, 1.5f, 0.08f, 2),
            p(FixtureType.LAMP, 1.76f, BACK),
            w(FixtureType.PICTURE, 1.3f, 0.4f, 3),
        )
        RoomKind.WORKSHOP -> listOf(
            p(FixtureType.WORKBENCH, 0.5f, BACK),
            w(FixtureType.TOOL_WALL, 0.5f, 0.52f),
            p(FixtureType.MI_SAW_BENCH, 1.05f, -0.05f),
            p(FixtureType.WOOD_PILE, 1.5f, BACK),
            p(FixtureType.STOOL, 0.85f, 0.07f),
            p(FixtureType.TRASH_BIN, 1.82f, 0.03f),
            p(FixtureType.ROBOT_VACUUM, 1.3f, 0.09f),
            w(FixtureType.SHELF, 1.58f, 0.45f),
        )
        RoomKind.MUSIC -> listOf(
            p(FixtureType.PIANO, 0.5f, BACK),
            p(FixtureType.DRUM_KIT, 1.15f, -0.05f),
            p(FixtureType.XYLOPHONE, 1.62f, 0.05f),
            p(FixtureType.MIC_STAND, 1.84f, 0.07f),
            p(FixtureType.SPEAKER, 0.14f, BACK),
            p(FixtureType.MI_GUITAR, 0.82f, 0.05f),
            p(FixtureType.BEANBAG, 0.72f, 0.1f, 2),
            w(FixtureType.DISCO_BALL, 1.15f, 0.3f),
        )
        RoomKind.GREENHOUSE -> listOf(
            p(FixtureType.MI_PLANT_BED, 0.55f, BACK),
            p(FixtureType.MI_PLANT_BED, 1.45f, BACK, 1),
            p(FixtureType.FLOWER_POT, 0.15f, -0.05f, 0),
            p(FixtureType.FLOWER_POT, 1.9f, -0.05f, 1),
            p(FixtureType.PLANT_BIG, 1.0f, BACK),
            w(FixtureType.MI_HANGING_POT, 0.85f, 0.32f),
            w(FixtureType.MI_HANGING_POT, 1.6f, 0.3f, 1),
            p(FixtureType.BENCH, 1.0f, 0.07f),
        )
    }

    /** A small thing or two that waits in the room. */
    fun things(kind: RoomKind): List<MineThing> = when (kind) {
        RoomKind.BEDROOM -> listOf(MineThing(ThingType.PILLOW, 1, 0, -0.12f, 0.105f))
        RoomKind.BATH -> listOf(MineThing(ThingType.DUCK, 0, 0, 0f, 0.035f))
        RoomKind.DINING -> listOf(MineThing(ThingType.CAKE, 0, 0, 0f, 0.13f))
        RoomKind.LIBRARY -> listOf(MineThing(ThingType.BOOK, 1, 2, -0.05f, 0.15f))
        RoomKind.KITCHEN -> listOf(MineThing(ThingType.APPLE, 0, 2, -0.08f, 0.18f))
        RoomKind.WORKSHOP -> listOf(MineThing(ThingType.HAMMER, 0, 0, 0.05f, 0.2f))
        else -> emptyList()
    }

    /** What the hall gets with the foundation, and what the landing gets with the second floor. */
    val hall = listOf(
        p(FixtureType.RUG, 0.62f, 0.08f, 1),
        p(FixtureType.MI_COAT_RACK, 0.95f, BACK),
        p(FixtureType.PLANT_BIG, 0.14f, BACK),
    )
    val landing = listOf(
        p(FixtureType.RUG, 0.7f, 0.08f, 2),
        p(FixtureType.PLANT_BIG, 0.18f, BACK),
        w(FixtureType.PICTURE, 0.7f, 0.42f, 1),
    )

    /** Extra pieces in the designer's catalogue for the yard and for the rooms of the house. */
    val yardCatalogue = listOf(
        CatalogueItem(FixtureType.MI_FENCE, 0), CatalogueItem(FixtureType.MI_FENCE, 1), CatalogueItem(FixtureType.MI_FENCE, 2),
        CatalogueItem(FixtureType.MI_FLOWER_BED, 0), CatalogueItem(FixtureType.MI_FLOWER_BED, 1), CatalogueItem(FixtureType.MI_FLOWER_BED, 2), CatalogueItem(FixtureType.MI_FLOWER_BED, 3),
        CatalogueItem(FixtureType.MI_MAILBOX), CatalogueItem(FixtureType.MI_SWING), CatalogueItem(FixtureType.MI_BIRD_BATH),
        CatalogueItem(FixtureType.MI_GNOME), CatalogueItem(FixtureType.MI_SANDBOX), CatalogueItem(FixtureType.MI_APPLE_TREE),
    )

    val roomCatalogue = listOf(
        CatalogueItem(FixtureType.MI_FIREPLACE), CatalogueItem(FixtureType.MI_DINING_TABLE), CatalogueItem(FixtureType.MI_COUNTER),
        CatalogueItem(FixtureType.MI_BEDSIDE), CatalogueItem(FixtureType.MI_BLOCKS), CatalogueItem(FixtureType.MI_ROCKING_HORSE),
        CatalogueItem(FixtureType.MI_BIG_BOOKCASE), CatalogueItem(FixtureType.MI_GLOBE), CatalogueItem(FixtureType.MI_GUITAR),
        CatalogueItem(FixtureType.MI_SAW_BENCH), CatalogueItem(FixtureType.MI_LAUNDRY_BASKET), CatalogueItem(FixtureType.MI_PLANT_BED, 0),
        CatalogueItem(FixtureType.MI_PLANT_BED, 1), CatalogueItem(FixtureType.MI_HANGING_POT), CatalogueItem(FixtureType.MI_CHANDELIER),
        CatalogueItem(FixtureType.MI_COAT_RACK),
    )

    private fun seat(dx: Float, dy: Float) = SpotSpec(dx, dy, Pose.SIT)

    /** The spec of a fixture type of the child's own house, or null for any other type. */
    fun specOf(type: FixtureType): FixtureSpec? = when (type) {
        FixtureType.MI_FIREPLACE -> FixtureSpec(0.34f, 0.34f, surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.34f)), light = RRect(-0.45f, -0.55f, 0.45f, 0.05f))
        FixtureType.MI_DINING_TABLE -> FixtureSpec(0.46f, 0.13f, surfaces = listOf(SurfaceSpec(-0.22f, 0.22f, -0.13f)))
        FixtureType.MI_CHANDELIER -> FixtureSpec(0.22f, 0.12f, wall = true, light = RRect(-0.35f, -0.15f, 0.35f, 0.6f))
        FixtureType.MI_COUNTER -> FixtureSpec(0.34f, 0.18f, surfaces = listOf(SurfaceSpec(-0.16f, 0.16f, -0.18f)))
        FixtureType.MI_BEDSIDE -> FixtureSpec(0.11f, 0.12f, surfaces = listOf(SurfaceSpec(-0.045f, 0.045f, -0.12f)), light = RRect(-0.22f, -0.32f, 0.22f, 0.05f))
        FixtureType.MI_BLOCKS -> FixtureSpec(0.14f, 0.2f)
        FixtureType.MI_ROCKING_HORSE -> FixtureSpec(0.22f, 0.2f, spots = listOf(seat(0f, -0.1f)))
        FixtureType.MI_BIG_BOOKCASE -> FixtureSpec(0.5f, 0.46f, surfaces = listOf(SurfaceSpec(-0.22f, 0.22f, -0.46f)))
        FixtureType.MI_GLOBE -> FixtureSpec(0.1f, 0.16f)
        FixtureType.MI_SAW_BENCH -> FixtureSpec(0.3f, 0.18f, surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.18f)))
        FixtureType.MI_LAUNDRY_BASKET -> FixtureSpec(0.12f, 0.13f)
        FixtureType.MI_GUITAR -> FixtureSpec(0.1f, 0.2f)
        FixtureType.MI_PLANT_BED -> FixtureSpec(0.34f, 0.14f)
        FixtureType.MI_HANGING_POT -> FixtureSpec(0.1f, 0.14f, wall = true)
        FixtureType.MI_COAT_RACK -> FixtureSpec(0.14f, 0.4f)
        FixtureType.MI_MAILBOX -> FixtureSpec(0.08f, 0.2f)
        FixtureType.MI_FENCE -> FixtureSpec(0.3f, 0.12f)
        FixtureType.MI_FLOWER_BED -> FixtureSpec(0.3f, 0.08f)
        FixtureType.MI_SWING -> FixtureSpec(0.4f, 0.32f, spots = listOf(seat(0f, -0.09f)))
        FixtureType.MI_BIRD_BATH -> FixtureSpec(0.12f, 0.16f)
        FixtureType.MI_GNOME -> FixtureSpec(0.06f, 0.12f)
        FixtureType.MI_SANDBOX -> FixtureSpec(0.32f, 0.07f)
        FixtureType.MI_APPLE_TREE -> FixtureSpec(0.42f, 0.62f)
        else -> null
    }
}
