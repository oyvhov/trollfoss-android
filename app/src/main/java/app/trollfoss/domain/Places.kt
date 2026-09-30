package app.trollfoss.domain

/**
 * The places on the island. [width] is in scene units (the scene is one unit tall), [floor] is where
 * the ground is and [ceiling] how high a balloon can rise.
 */
enum class PlaceId(val width: Float, val outdoor: Boolean, val floor: Float, val ceiling: Float) {
    HOME(4.2f, false, 0.90f, 0.07f),
    CAFE(3.0f, false, 0.90f, 0.07f),
    SALON(2.8f, false, 0.90f, 0.07f),
    BEACH(3.8f, true, 0.88f, 0.02f),
    FOREST(3.4f, true, 0.88f, 0.02f),
    LAB(2.8f, false, 0.90f, 0.07f),
    MOUNTAIN(3.8f, true, 0.88f, 0.02f),
}

/** A stretch of ground. Places with water have a sea or pond bed lower than the land. */
data class Ground(val x1: Float, val x2: Float, val y: Float)

/** Open water: things float at [line] or sink to [bottom]. */
data class Water(val x1: Float, val x2: Float, val line: Float, val bottom: Float)

data class FixtureDef(val type: FixtureType, val x: Float, val y: Float, val variant: Int = 0)

data class ThingDef(val type: ThingType, val x: Float, val y: Float, val variant: Int = 0)

data class PersonDef(
    val species: Species,
    val look: Look,
    val x: Float,
    val y: Float = 0f,
    /** Fixture index in the place and the spot on it, or null to stand. */
    val seat: Pair<Int, Int>? = null,
    val hat: ThingType? = null,
    val hatVariant: Int = 0,
    val glasses: ThingType? = null,
    val hand: ThingType? = null,
    val handVariant: Int = 0,
)

class PlaceSpec(
    val id: PlaceId,
    val grounds: List<Ground>,
    val water: Water?,
    val fixtures: List<FixtureDef>,
    val things: List<ThingDef>,
    val people: List<PersonDef>,
)

/** Blueprints for a new island. Positions are bottom centres; things settle onto what is below them. */
object Places {
    fun spec(id: PlaceId): PlaceSpec = specs.getValue(id)

    private val specs: Map<PlaceId, PlaceSpec> by lazy { PlaceId.entries.associateWith(::build) }

    private fun f(type: FixtureType, x: Float, y: Float = Float.NaN, variant: Int = 0) = FixtureDef(type, x, y, variant)
    private fun t(type: ThingType, x: Float, y: Float, variant: Int = 0) = ThingDef(type, x, y, variant)

    private fun build(id: PlaceId): PlaceSpec {
        val floor = id.floor
        fun fl(type: FixtureType, x: Float, variant: Int = 0) = FixtureDef(type, x, floor, variant)
        return when (id) {
            PlaceId.HOME -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.CHEST, 0.12f),               // 0 lost and found
                    f(FixtureType.WINDOW, 0.40f, 0.44f),         // 1
                    fl(FixtureType.BED, 0.40f),                  // 2
                    f(FixtureType.SHELF, 0.15f, 0.52f),          // 3
                    fl(FixtureType.LAMP, 0.69f),                 // 4
                    fl(FixtureType.WARDROBE, 0.87f),             // 5
                    fl(FixtureType.WOOD_STOVE, 1.1f),            // 6
                    fl(FixtureType.SOFA, 1.38f),                 // 7
                    f(FixtureType.CLOCK, 1.38f, 0.42f),          // 8
                    fl(FixtureType.ROUND_TABLE, 1.66f),          // 9
                    f(FixtureType.RADIO, 1.66f, 0.77f),          // 10
                    f(FixtureType.MAILBOX, 1.66f, 0.52f),        // 11
                    fl(FixtureType.TV, 1.9f),                    // 12
                    f(FixtureType.WINDOW, 1.9f, 0.54f),          // 13
                    fl(FixtureType.PIANO, 2.25f),                // 14
                    fl(FixtureType.FRIDGE, 2.56f),               // 15
                    fl(FixtureType.STOVE, 2.76f),                // 16
                    fl(FixtureType.SINK, 2.95f),                 // 17
                    f(FixtureType.SHELF, 2.86f, 0.48f),          // 18
                    fl(FixtureType.CHAIR, 3.07f),                // 19
                    fl(FixtureType.TABLE, 3.25f),                // 20
                    fl(FixtureType.CHAIR, 3.43f),                // 21
                    f(FixtureType.WINDOW, 3.25f, 0.52f),         // 22
                    fl(FixtureType.BATH, 3.73f),                 // 23
                    f(FixtureType.SHELF, 3.73f, 0.46f),          // 24
                    f(FixtureType.MIRROR, 4.07f, 0.52f),         // 25
                    fl(FixtureType.TOILET, 4.07f),               // 26
                ),
                things = listOf(
                    t(ThingType.PILLOW, 0.27f, 0.79f, 1),
                    t(ThingType.TEDDY, 0.09f, 0.49f),
                    t(ThingType.BOOK, 0.21f, 0.49f, 1),
                    t(ThingType.BALL, 0.12f, 0.87f),
                    t(ThingType.MILK, 2.52f, 0.87f),
                    t(ThingType.EGG, 2.6f, 0.87f),
                    t(ThingType.APPLE, 2.52f, 0.77f),
                    t(ThingType.CARROT, 2.61f, 0.77f),
                    t(ThingType.BROWN_CHEESE, 2.53f, 0.67f),
                    t(ThingType.SAUSAGE, 2.76f, 0.69f),
                    t(ThingType.CUP, 2.8f, 0.44f, 0),
                    t(ThingType.CUP, 2.92f, 0.44f, 2),
                    t(ThingType.BREAD, 3.3f, 0.76f),
                    t(ThingType.CUP, 3.17f, 0.76f, 1),
                    t(ThingType.TOOTHBRUSH, 3.66f, 0.42f),
                    t(ThingType.DUCK, 3.8f, 0.42f),
                    t(ThingType.TOY_CAR, 1.62f, 0.89f, 0),
                    t(ThingType.GUITAR, 2.04f, 0.89f),
                    t(ThingType.CANDLE, 2.33f, 0.63f),
                    t(ThingType.PLANT_POT, 2.14f, 0.63f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 2, height = 1.14f, hair = 1, hairColor = 1, top = 5, topColor = 0, bottom = 0, bottomColor = 11, shoes = 12, extra = 2), 1.3f, seat = 7 to 0),
                    PersonDef(Species.FOLK, Look(skin = 5, height = 1.03f, hair = 6, hairColor = 0, eyes = 3, top = 2, topColor = 7, bottom = 2, bottomColor = 7, shoes = 8), 2.95f, hand = ThingType.CUP, handVariant = 3),
                    PersonDef(Species.FOLK, Look(skin = 1, height = 0.78f, hair = 3, hairColor = 5, top = 1, topColor = 5, bottom = 1, bottomColor = 11, shoes = 0, extra = 1), 0.4f, seat = 2 to 0),
                    PersonDef(Species.CAT, Look(skin = 0), 1.62f),
                ),
            )
            PlaceId.CAFE -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.FRUIT_CRATE, 0.16f),          // 0
                    fl(FixtureType.FLOUR_SACK, 0.34f),           // 1
                    fl(FixtureType.ICE_CREAM_MACHINE, 0.52f),    // 2
                    fl(FixtureType.COUNTER, 0.80f),              // 3
                    fl(FixtureType.COUNTER, 1.10f, 1),           // 4
                    f(FixtureType.CASH_REGISTER, 0.73f, 0.73f),  // 5
                    f(FixtureType.BLENDER, 0.93f, 0.73f),        // 6
                    f(FixtureType.SHELF, 0.92f, 0.46f),          // 7
                    fl(FixtureType.DISPLAY_CASE, 1.42f),         // 8
                    fl(FixtureType.OVEN, 1.72f),                 // 9
                    f(FixtureType.SHELF, 1.72f, 0.44f),          // 10
                    fl(FixtureType.CHAIR, 2.06f),                // 11
                    fl(FixtureType.ROUND_TABLE, 2.2f),           // 12
                    fl(FixtureType.CHAIR, 2.34f),                // 13
                    fl(FixtureType.CHAIR, 2.58f),                // 14
                    fl(FixtureType.ROUND_TABLE, 2.72f),          // 15
                    fl(FixtureType.CHAIR, 2.86f),                // 16
                    f(FixtureType.WINDOW, 2.46f, 0.5f),          // 17
                ),
                things = listOf(
                    t(ThingType.CAKE, 1.42f, 0.71f),
                    t(ThingType.CUPCAKE, 1.35f, 0.87f, 0),
                    t(ThingType.CUPCAKE, 1.49f, 0.87f, 2),
                    t(ThingType.BUN, 1.36f, 0.80f),
                    t(ThingType.COOKIE, 1.5f, 0.80f),
                    t(ThingType.EGG, 0.83f, 0.42f),
                    t(ThingType.EGG, 0.89f, 0.42f),
                    t(ThingType.MILK, 0.99f, 0.42f),
                    t(ThingType.DOUGH, 1.65f, 0.40f),
                    t(ThingType.DOUGH, 1.79f, 0.40f),
                    t(ThingType.COCOA, 2.2f, 0.76f),
                    t(ThingType.JUICE, 2.74f, 0.76f),
                    t(ThingType.WAFFLE, 2.67f, 0.76f),
                    t(ThingType.BANANA, 0.16f, 0.79f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 6, height = 1.03f, hair = 1, hairColor = 0, top = 0, topColor = 9, bottom = 0, bottomColor = 11, shoes = 11, extra = 3), 1.22f, hat = ThingType.CHEF_HAT),
                    PersonDef(Species.FOLK, Look(skin = 3, height = 0.78f, hair = 5, hairColor = 3, eyes = 1, top = 3, topColor = 8, bottom = 2, bottomColor = 6, shoes = 5), 2.06f, seat = 11 to 0),
                    PersonDef(Species.FOLK, Look(skin = 9, height = 0.9f, hair = 8, hairColor = 8, eyes = 4, ears = 1, top = 1, topColor = 2, bottom = 1, bottomColor = 11, shoes = 0), 2.86f, seat = 16 to 0),
                ),
            )
            PlaceId.SALON -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.CLOTHES_RACK, 0.25f),         // 0
                    f(FixtureType.SHELF, 0.25f, 0.5f),           // 1
                    f(FixtureType.MIRROR, 0.70f, 0.5f),          // 2
                    fl(FixtureType.SALON_CHAIR, 0.70f),          // 3
                    f(FixtureType.SHELF, 0.90f, 0.45f),          // 4
                    f(FixtureType.MIRROR, 1.10f, 0.5f),          // 5
                    fl(FixtureType.SALON_CHAIR, 1.10f),          // 6
                    fl(FixtureType.HAIR_WASH, 1.48f),            // 7
                    f(FixtureType.SHELF, 1.66f, 0.45f),          // 8
                    fl(FixtureType.DRYER_HOOD, 1.88f),           // 9
                    fl(FixtureType.SOFA, 2.3f),                  // 10
                    f(FixtureType.WINDOW, 2.3f, 0.5f),           // 11
                    fl(FixtureType.PLANT_BIG, 2.66f),            // 12
                ),
                things = listOf(
                    t(ThingType.CAP, 0.17f, 0.46f, 2),
                    t(ThingType.BOW, 0.46f, 0.89f, 1),
                    t(ThingType.VIKING_HELMET, 0.33f, 0.46f),
                    t(ThingType.SCISSORS, 0.82f, 0.41f),
                    t(ThingType.COMB, 0.9f, 0.41f),
                    t(ThingType.HAIR_DRYER, 0.98f, 0.41f),
                    t(ThingType.SPRAY, 1.57f, 0.41f, 7),
                    t(ThingType.SPRAY, 1.63f, 0.41f, 8),
                    t(ThingType.SPRAY, 1.69f, 0.41f, 9),
                    t(ThingType.SPRAY, 1.75f, 0.41f, 3),
                    t(ThingType.SUNGLASSES, 2.62f, 0.89f),
                    t(ThingType.FLOWER_CROWN, 2.46f, 0.89f),
                    t(ThingType.STAR_GLASSES, 1.3f, 0.89f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 0, height = 1.03f, hair = 6, hairColor = 7, eyes = 3, top = 4, topColor = 10, bottom = 0, bottomColor = 10, shoes = 8), 0.92f),
                    PersonDef(Species.FOLK, Look(skin = 4, height = 1.14f, hair = 7, hairColor = 8, top = 1, topColor = 3, bottom = 0, bottomColor = 11, shoes = 2), 1.1f, seat = 6 to 0),
                ),
            )
            PlaceId.BEACH -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, 2.3f, floor), Ground(2.3f, id.width, 0.97f)),
                water = Water(2.3f, id.width, 0.78f, 0.97f),
                fixtures = listOf(
                    fl(FixtureType.UMBRELLA, 0.28f),             // 0
                    fl(FixtureType.LOUNGER, 0.44f),              // 1
                    fl(FixtureType.SANDCASTLE, 0.97f),           // 2
                    f(FixtureType.PIER, 2.45f, 0.97f),           // 3
                    f(FixtureType.FISHING_SPOT, 2.88f, 0.71f),   // 4
                    f(FixtureType.BOAT, 3.42f, 0.80f),           // 5
                    fl(FixtureType.LAMP_POST, 1.62f),            // 6
                ),
                things = listOf(
                    t(ThingType.BEACH_BALL, 0.75f, 0.87f),
                    t(ThingType.BUCKET, 1.14f, 0.87f, 0),
                    t(ThingType.SPADE, 1.21f, 0.87f),
                    t(ThingType.SHELL, 1.42f, 0.87f),
                    t(ThingType.STARFISH, 1.84f, 0.87f),
                    t(ThingType.SWIM_RING, 2.12f, 0.87f),
                    t(ThingType.DUCK, 3.0f, 0.7f),
                    t(ThingType.ROCK, 3.7f, 0.9f),
                    t(ThingType.ICE_CREAM, 0.5f, 0.8f, 3),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 4, height = 1.03f, hair = 7, hairColor = 3, top = 0, topColor = 1, bottom = 1, bottomColor = 5, shoes = 4), 1.35f),
                    PersonDef(Species.FOLK, Look(skin = 1, height = 1.14f, hair = 0, hairColor = 6, top = 3, topColor = 6, bottom = 1, bottomColor = 13, shoes = 12, extra = 2), 0.44f, seat = 1 to 0, hat = ThingType.SUN_HAT),
                    PersonDef(Species.PUFFIN, Look(skin = 0), 1.74f),
                ),
            )
            PlaceId.FOREST -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, 2.55f, floor), Ground(2.55f, id.width, 0.95f)),
                water = Water(2.55f, id.width, 0.83f, 0.95f),
                fixtures = listOf(
                    fl(FixtureType.OWL_TREE, 0.28f),             // 0
                    fl(FixtureType.TENT, 0.80f),                 // 1
                    fl(FixtureType.LOG, 1.32f),                  // 2
                    fl(FixtureType.CAMPFIRE, 1.62f),             // 3
                    fl(FixtureType.STUMP, 1.9f),                 // 4
                    fl(FixtureType.STUMP, 2.26f),                // 5
                ),
                things = listOf(
                    t(ThingType.MARSHMALLOW, 1.9f, 0.79f),
                    t(ThingType.SAUSAGE, 2.26f, 0.79f),
                    t(ThingType.PINECONE, 1.08f, 0.87f),
                    t(ThingType.MUSHROOM, 0.44f, 0.87f),
                    t(ThingType.MUSHROOM, 2.42f, 0.87f),
                    t(ThingType.FLOWER, 2.05f, 0.87f, 2),
                    t(ThingType.STICK, 1.47f, 0.87f),
                    t(ThingType.FEATHER, 2.5f, 0.87f),
                    t(ThingType.DRAGON_EGG, 0.86f, 0.87f),
                    t(ThingType.ROCK, 3.05f, 0.93f),
                    t(ThingType.LEAF, 0.12f, 0.87f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 13, height = 1.14f, hair = 3, hairColor = 1, ears = 4, top = 5, topColor = 3, bottom = 0, bottomColor = 12, shoes = 12, extra = 2), 1.25f, seat = 2 to 0),
                    PersonDef(Species.FOLK, Look(skin = 7, height = 0.9f, hair = 2, hairColor = 0, top = 1, topColor = 1, bottom = 1, bottomColor = 3, shoes = 6), 2.07f, hat = ThingType.BEANIE, hatVariant = 1),
                    PersonDef(Species.BUNNY, Look(skin = 3), 2.4f),
                    PersonDef(Species.ELK, Look(skin = 0), 0.54f),
                ),
            )
            PlaceId.LAB -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.TABLE, 0.38f),                // 0
                    f(FixtureType.POTION_RACK, 0.38f, 0.5f),     // 1
                    fl(FixtureType.CAULDRON, 0.98f),             // 2
                    fl(FixtureType.SPELLBOOK, 1.36f),            // 3
                    fl(FixtureType.TELESCOPE, 1.74f),            // 4
                    f(FixtureType.WINDOW, 1.74f, 0.44f, 1),      // 5 round window
                    fl(FixtureType.ROUND_TABLE, 2.13f),          // 6
                    f(FixtureType.CRYSTAL_BALL, 2.13f, 0.77f),   // 7
                    fl(FixtureType.BOOKCASE, 2.56f),             // 8
                    f(FixtureType.SHELF, 1.0f, 0.46f),           // 9
                ),
                things = listOf(
                    t(ThingType.FLOWER, 0.29f, 0.76f, 4),
                    t(ThingType.ROCK, 0.38f, 0.76f),
                    t(ThingType.MUSHROOM, 0.47f, 0.76f),
                    t(ThingType.FEATHER, 0.94f, 0.42f),
                    t(ThingType.GEM, 1.06f, 0.42f, 2),
                    t(ThingType.BOOK, 2.51f, 0.75f, 2),
                    t(ThingType.BOOK, 2.6f, 0.75f, 3),
                    t(ThingType.STAR_JAR, 2.56f, 0.63f),
                    t(ThingType.WAND, 2.56f, 0.51f),
                    t(ThingType.TEDDY, 1.55f, 0.89f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 10, height = 1.03f, hair = 4, hairColor = 4, eyes = 0, top = 2, topColor = 7, bottom = 0, bottomColor = 11, shoes = 7), 1.2f, glasses = ThingType.ROUND_GLASSES, hat = ThingType.WIZARD_HAT),
                    PersonDef(Species.DRAGON, Look(skin = 0), 0.7f),
                ),
            )
            PlaceId.MOUNTAIN -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.PINE_TREE, 0.22f),            // 0
                    fl(FixtureType.SAUNA, 0.66f),                // 1
                    fl(FixtureType.SLED_HILL, 1.25f),            // 2
                    fl(FixtureType.SNOWMAN, 1.74f),              // 3
                    fl(FixtureType.SKI_JUMP, 2.3f),              // 4
                    fl(FixtureType.LAMP_POST, 2.7f),             // 5
                    fl(FixtureType.ICE_POND, 3.1f),              // 6
                    fl(FixtureType.COCOA_STAND, 3.62f),          // 7
                ),
                things = listOf(
                    t(ThingType.SNOWBALL, 1.6f, 0.87f),
                    t(ThingType.SNOWBALL, 1.88f, 0.87f),
                    t(ThingType.SLED, 0.94f, 0.87f),
                    t(ThingType.COIN, 2.76f, 0.87f),
                    t(ThingType.COIN, 3.3f, 0.86f),
                    t(ThingType.COCOA, 3.6f, 0.72f),
                    t(ThingType.WAFFLE, 3.66f, 0.72f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 1, height = 0.9f, hair = 5, hairColor = 3, eyes = 1, top = 5, topColor = 6, bottom = 0, bottomColor = 11, shoes = 0), 1.06f, seat = 2 to 0, hat = ThingType.BEANIE, hatVariant = 2),
                    PersonDef(Species.FOLK, Look(skin = 2, height = 1.14f, hair = 0, hairColor = 6, top = 6, topColor = 11, bottom = 0, bottomColor = 11, shoes = 12, extra = 2), 3.42f, hat = ThingType.NISSE_HAT),
                    PersonDef(Species.FOLK, Look(skin = 6, height = 1.03f, hair = 4, hairColor = 0, eyes = 3, top = 6, topColor = 0, bottom = 2, bottomColor = 11, shoes = 11), 2.84f, hand = ThingType.COCOA),
                    PersonDef(Species.DOG, Look(skin = 1), 2.02f),
                ),
            )
        }
    }
}
