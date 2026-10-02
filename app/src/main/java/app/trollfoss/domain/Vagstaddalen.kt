package app.trollfoss.domain

/** The playable river matches the banks drawn in ValleyArt. Cabin doors, fishing and the fire use existing rules. */
object Vagstaddalen {
    const val RIVER_LEFT = 2.8f
    const val RIVER_RIGHT = 3.85f
    const val WATERLINE = 0.83f
    const val BED = 0.97f
    const val CABIN = 0
    val cabinSpec = FixtureSpec(0.92f, 0.42f,
        container = RRect(-0.3f, -0.30f, 0.3f, -0.02f),
        surfaces = listOf(SurfaceSpec(-0.3f, 0.3f, -0.02f, interior = true), SurfaceSpec(-0.46f, 0.46f, -0.42f)),
        light = RRect(-0.5f, -0.46f, 0.5f, 0.05f))

    fun blueprint(): PlaceSpec {
        val p = PlaceId.VAGSTADDALEN
        return PlaceSpec(
            p,
            grounds = listOf(Ground(0f, RIVER_LEFT, p.floor), Ground(RIVER_LEFT, RIVER_RIGHT, BED), Ground(RIVER_RIGHT, p.width, p.floor)),
            water = Water(RIVER_LEFT, RIVER_RIGHT, WATERLINE, BED),
            fixtures = listOf(
                FixtureDef(FixtureType.MOUNTAIN_HUT, 0.9f, 0.82f), // 0: a log cabin whose doors open
                FixtureDef(FixtureType.WOOD_PILE, 0.3f, 0.9f),
                FixtureDef(FixtureType.BENCH, 1.6f, 0.91f),
                FixtureDef(FixtureType.CAMPFIRE, 2.06f, 0.94f),
                FixtureDef(FixtureType.STUMP, 2.52f, 0.91f),
                FixtureDef(FixtureType.FISHING_SPOT, 2.9f, 0.8f),
                FixtureDef(FixtureType.LOG, 4.35f, 0.93f),
                FixtureDef(FixtureType.PINE_TREE, 4.96f, 0.81f),
            ),
            things = listOf(
                ThingDef(ThingType.CUP, 0.74f, 0.76f, 2, on = CABIN),
                ThingDef(ThingType.BOOK, 1.04f, 0.76f, 1, on = CABIN),
                ThingDef(ThingType.MARSHMALLOW, 2.5f, 0.77f, on = 4),
                ThingDef(ThingType.SAUSAGE, 2.56f, 0.78f, on = 4),
                ThingDef(ThingType.DUCK, 3.35f, 0.75f),
                ThingDef(ThingType.ROCK, 3.65f, 0.91f),
                ThingDef(ThingType.APPLE, 4.25f, 0.87f, on = 6),
                ThingDef(ThingType.FLOWER_CROWN, 4.5f, 0.88f),
            ),
            // The family's named figures can be brought here in the bag. The valley has a cat of its own.
            people = listOf(PersonDef(Species.CAT, Look(skin = 2), 1.5f, y = 0.94f)),
        )
    }
}
