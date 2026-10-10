package app.trollfoss.domain

/** The playable river matches the banks drawn in ValleyArt. Cabin doors, fishing and the fire use existing rules. */
object Vagstaddalen {
    const val RIVER_LEFT = 2.8f
    const val RIVER_RIGHT = 3.85f
    const val WATERLINE = 0.83f
    const val BED = 0.97f
    const val CABIN = 0
    // 0..7 and the secret nook at 8 retain their saved identities.
    const val RIVER_FIRE = 9
    const val RIVER_FISHING = 10
    const val DINGHY = 11
    const val SANDCASTLE = 12
    const val RIVER_LIFE = "layout:valley-river-life:1"
    val riversideFixtures = listOf(
        FixtureDef(FixtureType.CAMPFIRE, 4.05f, 0.92f),
        FixtureDef(FixtureType.FISHING_SPOT, 3.77f, 0.80f, variant = 1),
        FixtureDef(FixtureType.BOAT, 3.46f, WATERLINE + 0.02f, variant = 1),
        FixtureDef(FixtureType.SANDCASTLE, 2.27f, 0.91f),
    )
    val riversideThings = listOf(
        ThingDef(ThingType.GARMENT, 2.28f, 0.96f, Garment.pack(Garment.SWIM_SUIT, 4)),
        ThingDef(ThingType.GARMENT, 2.43f, 0.96f, Garment.pack(Garment.SWIM_SET, 8)),
        ThingDef(ThingType.BUCKET, 2.66f, 0.91f, 1),
        ThingDef(ThingType.SPADE, 2.72f, 0.95f),
        ThingDef(ThingType.SWIM_RING, 2.59f, 0.97f),
        ThingDef(ThingType.BEACH_BALL, 2.40f, 0.84f),
        ThingDef(ThingType.MARSHMALLOW, 4.53f, 0.94f),
        ThingDef(ThingType.SAUSAGE, 4.63f, 0.94f),
        ThingDef(ThingType.PINECONE, 4.83f, 0.91f),
        ThingDef(ThingType.MUSHROOM, 5.20f, 0.91f),
        ThingDef(ThingType.STICK, 3.99f, 0.97f),
    )

    /** Only new loose belongings are gifted once. Existing furniture, clothes and packed things stay put. */
    fun upgrade(world: World) {
        if (!world.flags.add(RIVER_LIFE)) return
        for (def in riversideThings) {
            val t = world.addThing(def.type, def.variant, PlaceId.VAGSTADDALEN, def.x, def.y)
            t.age = 10f
            t.ground = def.y
            WorldFactory.remember(world, t)
        }
    }
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
            ) + riversideThings,
            // The family's named figures can be brought here in the bag. The valley has a cat of its own.
            people = listOf(PersonDef(Species.CAT, Look(skin = 2), 1.5f, y = 0.94f)),
        )
    }
}
