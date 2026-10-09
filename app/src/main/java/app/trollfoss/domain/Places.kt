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
    FARM(4.4f, true, 0.88f, 0.02f),
    SPACE(3.6f, false, 0.90f, 0.07f),
    TIVOLI(4.4f, true, 0.88f, 0.02f),
    SHOP(3.4f, false, 0.90f, 0.07f),
    DOCTOR(3.0f, false, 0.90f, 0.07f),
    STAGE(3.2f, false, 0.90f, 0.07f),
    /** The sea floor off the beach: everything swims. */
    UNDERWATER(3.8f, true, 0.90f, 0.1f),
    /** The great long mountain: a scene more than twice as wide as the others, to climb and explore. */
    HEILEBERGET(9.0f, true, 0.88f, 0.02f),

    // Storhuset, the big house: four floors and a garden. Each floor is a place of its own, joined by
    // stairs, a lift, slides and hatches (see [House]). Only the ground floor has a button on the map.
    /** Ground floor: hall, living room, library, dining room, kitchen and winter garden. */
    MANOR_GROUND(12.0f, false, 0.90f, 0.07f),
    /** Upper floor: landing, two children's rooms, playroom, bathroom, bedroom and balcony. */
    MANOR_UPPER(12.0f, false, 0.90f, 0.07f),
    /** The attic: trunks, a friendly ghost, the telescope tower and a secret room. */
    MANOR_ATTIC(9.0f, false, 0.90f, 0.07f),
    /** The cellar: workshop, laundry, boiler room, pool and sauna, party room and a tunnel. */
    MANOR_CELLAR(10.0f, false, 0.90f, 0.07f),
    /** The garden around the house: greenhouse, pond, treehouse, shed and a trampoline. */
    MANOR_GARDEN(9.0f, true, 0.88f, 0.02f),

    // Mitt hus, the child's own house (see [Mine]): a plot with a front yard, and two floors of five room
    // slots that the child builds. The plot has the button on the map.
    /** The plot and its front yard: the house seen from outside, with a garden to furnish. */
    MINE_YARD(9.0f, true, 0.88f, 0.02f),
    /** The ground floor: five slots of two units, the first is the hall with the stairs. */
    MINE_GROUND(10.0f, false, 0.90f, 0.07f),
    /** The upper floor, built later: five slots above the ground floor's. */
    MINE_UPPER(10.0f, false, 0.90f, 0.07f),
    /** A quiet river valley with a small log cabin. Append new places to preserve saved ordinals. */
    VAGSTADDALEN(5.4f, true, 0.88f, 0.02f),
    CLOUD_ISLAND(3.6f,true,0.88f,0.02f),
    ;

    /** True for the five places that make up the big house. */
    val manor: Boolean get() = this in MANOR_GROUND..MANOR_GARDEN

    /** True for the three places of the child's own house. */
    val mine: Boolean get() = this in MINE_YARD..MINE_UPPER

    /** True for the places that belong to a house with floors and passages: the big house and the child's own. */
    val big: Boolean get() = manor || mine

    /** True when the place has a button on the map. The other floors are reached from inside the house. */
    val onMap: Boolean get() = this != LAB && (!big || this == MANOR_GROUND || this == MINE_YARD)

    /** Floors share one landmark. Used for the balloon and highlight when opening the map from inside. */
    val mapPlace: PlaceId get() = when {
        this == LAB -> FOREST
        mine -> MINE_YARD
        manor -> MANOR_GROUND
        else -> this
    }

    /**
     * Fixture ids are [idBase] plus the blueprint index. The older places have room for 100 ids each
     * (blueprint furniture below [addedFrom], furniture the child adds from there to [addedMax]). A place
     * of the big house has 300: blueprint furniture up to 199, added furniture from 200 to 299.
     */
    val idBase: Int get() = when {
        manor -> 1500 + (ordinal - MANOR_GROUND.ordinal) * 300
        mine -> 3000 + (ordinal - MINE_YARD.ordinal) * 300
        this == VAGSTADDALEN -> 3900
        this == CLOUD_ISLAND -> 4200
        else -> ordinal * 100
    }
    val addedFrom: Int get() = if (big) 200 else 40
    val addedMax: Int get() = if (big) 299 else 99

    /**
     * The floor is a band with depth («skrå-3D»): the back wall meets the floor at [back], the front edge
     * is [FRONT]. Things and figures stand anywhere in between; the further back, the higher on screen.
     * [floor] is the usual depth for furniture.
     */
    val back: Float get() = if (outdoor) 0.78f else 0.80f

    companion object {
        const val FRONT = 0.97f

        /** The place a fixture id belongs to, or null for an id that fits no place. */
        fun ofFixture(id: Int): PlaceId? =
            entries.lastOrNull { id >= it.idBase }?.takeIf { id < it.idBase + (if (it.big) 300 else 100) }
    }

    /** The blueprint index (or added slot) of a fixture id in this place. */
    fun indexOf(id: Int): Int = id - idBase
}

/** A stretch of ground. Places with water have a sea or pond bed lower than the land. */
data class Ground(val x1: Float, val x2: Float, val y: Float)

/** Open water: things float at [line] or sink to [bottom]. */
data class Water(val x1: Float, val x2: Float, val line: Float, val bottom: Float)

/**
 * A piece of furniture in a blueprint. [shift] is how far it stands in front of (+) or behind (-) the
 * usual depth; [on] is the fixture this one stands on (a radio on a table), so it moves with it.
 */
data class FixtureDef(val type: FixtureType, val x: Float, val y: Float, val variant: Int = 0, val shift: Float = 0f, val on: Int = -1)

/** A thing in a blueprint. [on] is the fixture it lies on, so it moves with that fixture's depth. */
data class ThingDef(val type: ThingType, val x: Float, val y: Float, val variant: Int = 0, val on: Int = -1)

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
    /** The figure's name, shown when it is tapped. Animals have none. */
    val name: String = "",
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

    private val specs: Map<PlaceId, PlaceSpec> by lazy { PlaceId.entries.associateWith { resolve(build(it)) } }

    /** Moves things and small fixtures with the furniture they stand on. */
    private fun resolve(raw: PlaceSpec): PlaceSpec {
        val fixtures = raw.fixtures.map { d ->
            if (d.on >= 0) raw.fixtures[d.on].shift.let { s -> d.copy(y = d.y + s, shift = s) } else d
        }
        val things = raw.things.map { t -> if (t.on >= 0) t.copy(y = t.y + fixtures[t.on].shift) else t }
        val nook = PlaySecrets.nooks[raw.id]?.let { (x, variant) -> FixtureDef(FixtureType.SECRET_NOOK, x, raw.id.back + 0.012f, variant) }
        return PlaceSpec(raw.id, raw.grounds, raw.water, fixtures + listOfNotNull(nook), things, raw.people)
    }

    private fun f(type: FixtureType, x: Float, y: Float = Float.NaN, variant: Int = 0, on: Int = -1) = FixtureDef(type, x, y, variant, 0f, on)
    private fun t(type: ThingType, x: Float, y: Float, variant: Int = 0, on: Int = -1) = ThingDef(type, x, y, variant, on)

    private fun build(id: PlaceId): PlaceSpec {
        val floor = id.floor
        fun fl(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, floor + depth, variant, depth)
        return when (id) {
            PlaceId.HOME -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.CHEST, 0.12f, depth = 0.04f),               // 0 lost and found
                    f(FixtureType.WINDOW, 0.40f, 0.44f),         // 1
                    fl(FixtureType.BED, 0.40f, depth = -0.06f),                  // 2
                    f(FixtureType.SHELF, 0.15f, 0.52f),          // 3
                    fl(FixtureType.LAMP, 0.69f, depth = -0.06f),                 // 4
                    fl(FixtureType.WARDROBE, 0.87f, depth = -0.09f),             // 5
                    fl(FixtureType.WOOD_STOVE, 1.1f, depth = -0.07f),            // 6
                    fl(FixtureType.SOFA, 1.38f, depth = -0.04f),                 // 7
                    f(FixtureType.CLOCK, 1.38f, 0.42f),          // 8
                    fl(FixtureType.ROUND_TABLE, 1.66f, depth = 0.04f),          // 9
                    f(FixtureType.RADIO, 1.66f, 0.77f, on = 9),          // 10
                    f(FixtureType.MAILBOX, 1.66f, 0.52f),        // 11
                    fl(FixtureType.TV, 1.9f, depth = -0.08f),                    // 12
                    f(FixtureType.WINDOW, 1.9f, 0.54f),          // 13
                    fl(FixtureType.PIANO, 2.25f, depth = -0.08f),                // 14
                    fl(FixtureType.FRIDGE, 2.56f, depth = -0.08f),               // 15
                    fl(FixtureType.STOVE, 2.76f, depth = -0.08f),                // 16
                    fl(FixtureType.SINK, 2.95f, depth = -0.08f),                 // 17
                    f(FixtureType.SHELF, 2.86f, 0.48f),          // 18
                    fl(FixtureType.CHAIR, 3.07f, depth = 0.03f),                // 19
                    fl(FixtureType.TABLE, 3.25f, depth = 0.03f),                // 20
                    fl(FixtureType.CHAIR, 3.43f, depth = 0.03f),                // 21
                    f(FixtureType.WINDOW, 3.25f, 0.52f),         // 22
                    fl(FixtureType.BATH, 3.73f, depth = -0.04f),                 // 23
                    f(FixtureType.SHELF, 3.73f, 0.46f),          // 24
                    f(FixtureType.MIRROR, 4.07f, 0.52f),         // 25
                    fl(FixtureType.TOILET, 4.07f, depth = -0.08f),               // 26
                    fl(FixtureType.TRASH_BIN, 2.9f, depth = 0.06f),              // 27
                    fl(FixtureType.ROBOT_VACUUM, 1.22f, depth = 0.06f),          // 28
                ),
                things = listOf(
                    t(ThingType.PILLOW, 0.27f, 0.79f, 1, on = 2),
                    t(ThingType.TEDDY, 0.09f, 0.49f),
                    t(ThingType.BOOK, 0.21f, 0.49f, 1),
                    t(ThingType.BALL, 0.12f, 0.87f, on = 0),
                    t(ThingType.MILK, 2.52f, 0.87f, on = 15),
                    t(ThingType.EGG, 2.6f, 0.87f, on = 15),
                    t(ThingType.APPLE, 2.52f, 0.77f, on = 15),
                    t(ThingType.CARROT, 2.61f, 0.77f, on = 15),
                    t(ThingType.BROWN_CHEESE, 2.53f, 0.67f, on = 15),
                    t(ThingType.SAUSAGE, 2.76f, 0.69f, on = 16),
                    t(ThingType.CUP, 2.8f, 0.44f, 0),
                    t(ThingType.CUP, 2.92f, 0.44f, 2),
                    t(ThingType.BREAD, 3.3f, 0.76f, on = 20),
                    t(ThingType.CUP, 3.17f, 0.76f, 1, on = 20),
                    t(ThingType.TOOTHBRUSH, 3.66f, 0.42f),
                    t(ThingType.DUCK, 3.8f, 0.42f),
                    t(ThingType.TOY_CAR, 1.52f, 0.95f, 0),
                    t(ThingType.GUITAR, 2.04f, 0.93f),
                    t(ThingType.CANDLE, 2.33f, 0.63f, on = 14),
                    t(ThingType.PLANT_POT, 2.14f, 0.63f, on = 14),
                    // Jokes: a whoopee cushion on the free half of the sofa, pepper by the bread, a banana.
                    t(ThingType.WHOOPEE, 1.47f, 0.825f, 0, on = 7),
                    t(ThingType.PEPPER, 3.38f, 0.77f, on = 20),
                    t(ThingType.BANANA, 2.62f, 0.67f, on = 15),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.oyvindLook(), 1.3f, seat = 7 to 0, name = "Øyvind"),
                    PersonDef(Species.FOLK, Residents.heddaLook(), 0.4f, seat = 2 to 0, name = "Hedda"),
                    PersonDef(Species.CAT, Look(skin = 0), 1.62f, y = 0.95f),
                ),
            )
            PlaceId.CAFE -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.FRUIT_CRATE, 0.16f, depth = 0.05f),          // 0
                    fl(FixtureType.FLOUR_SACK, 0.34f, depth = 0.02f),           // 1
                    fl(FixtureType.ICE_CREAM_MACHINE, 0.52f, depth = -0.07f),    // 2
                    fl(FixtureType.COUNTER, 0.80f, depth = -0.07f),              // 3
                    fl(FixtureType.COUNTER, 1.10f, 1, depth = -0.07f),           // 4
                    f(FixtureType.CASH_REGISTER, 0.73f, 0.73f, on = 3),  // 5
                    f(FixtureType.BLENDER, 0.93f, 0.73f, on = 3),        // 6
                    f(FixtureType.SHELF, 0.92f, 0.46f),          // 7
                    fl(FixtureType.DISPLAY_CASE, 1.42f, depth = -0.05f),         // 8
                    fl(FixtureType.OVEN, 1.72f, depth = -0.08f),                 // 9
                    f(FixtureType.SHELF, 1.72f, 0.44f),          // 10
                    fl(FixtureType.CHAIR, 2.06f, depth = 0.04f),                // 11
                    fl(FixtureType.ROUND_TABLE, 2.2f, depth = 0.04f),           // 12
                    fl(FixtureType.CHAIR, 2.34f, depth = 0.04f),                // 13
                    fl(FixtureType.CHAIR, 2.58f, depth = -0.03f),                // 14
                    fl(FixtureType.ROUND_TABLE, 2.72f, depth = -0.03f),          // 15
                    fl(FixtureType.CHAIR, 2.86f, depth = -0.03f),                // 16
                    f(FixtureType.WINDOW, 2.46f, 0.5f),          // 17
                    fl(FixtureType.TRASH_BIN, 1.95f, depth = 0.06f),             // 18
                ),
                things = listOf(
                    t(ThingType.CAKE, 1.42f, 0.71f, on = 8),
                    t(ThingType.CUPCAKE, 1.35f, 0.87f, 0, on = 8),
                    t(ThingType.CUPCAKE, 1.49f, 0.87f, 2, on = 8),
                    t(ThingType.BUN, 1.36f, 0.80f, on = 8),
                    t(ThingType.COOKIE, 1.5f, 0.80f, on = 8),
                    t(ThingType.EGG, 0.83f, 0.42f),
                    t(ThingType.EGG, 0.89f, 0.42f),
                    t(ThingType.MILK, 0.99f, 0.42f),
                    t(ThingType.DOUGH, 1.65f, 0.40f),
                    t(ThingType.DOUGH, 1.79f, 0.40f),
                    t(ThingType.COCOA, 2.2f, 0.76f, on = 12),
                    t(ThingType.JUICE, 2.74f, 0.76f, on = 15),
                    t(ThingType.WAFFLE, 2.67f, 0.76f, on = 15),
                    t(ThingType.BANANA, 0.3f, 0.96f),
                    t(ThingType.WHOOPEE, 2.34f, 0.815f, 1, on = 13),
                    t(ThingType.PEPPER, 1.2f, 0.73f, on = 4),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.sondreLook(), 1.22f, y = 0.86f, hat = ThingType.CHEF_HAT, name = "Sondre"),
                    PersonDef(Species.FOLK, Look(skin = 3, height = 0.78f, hair = 5, hairColor = 3, eyes = 1, top = 3, topColor = 8, bottom = 2, bottomColor = 6, shoes = 5), 2.06f, seat = 11 to 0, name = "Alva"),
                ),
            )
            PlaceId.SALON -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.CLOTHES_RACK, 0.25f, depth = -0.07f),         // 0
                    f(FixtureType.SHELF, 0.25f, 0.5f),           // 1
                    f(FixtureType.MIRROR, 0.70f, 0.5f),          // 2
                    fl(FixtureType.SALON_CHAIR, 0.70f, depth = -0.03f),          // 3
                    f(FixtureType.SHELF, 0.90f, 0.45f),          // 4
                    f(FixtureType.MIRROR, 1.10f, 0.5f),          // 5
                    fl(FixtureType.SALON_CHAIR, 1.10f, depth = -0.03f),          // 6
                    fl(FixtureType.HAIR_WASH, 1.48f, depth = -0.06f),            // 7
                    f(FixtureType.SHELF, 1.66f, 0.45f),          // 8
                    fl(FixtureType.DRYER_HOOD, 1.88f, depth = -0.06f),           // 9
                    fl(FixtureType.SOFA, 2.3f, depth = -0.02f),                  // 10
                    f(FixtureType.WINDOW, 2.3f, 0.5f),           // 11
                    fl(FixtureType.PLANT_BIG, 2.66f, depth = -0.06f),            // 12
                ),
                things = listOf(
                    t(ThingType.CAP, 0.17f, 0.46f, 2),
                    t(ThingType.BOW, 0.46f, 0.95f, 1),
                    t(ThingType.VIKING_HELMET, 0.33f, 0.46f),
                    t(ThingType.SCISSORS, 0.82f, 0.41f),
                    t(ThingType.COMB, 0.9f, 0.41f),
                    t(ThingType.HAIR_DRYER, 0.98f, 0.41f),
                    t(ThingType.SPRAY, 1.57f, 0.41f, 7),
                    t(ThingType.SPRAY, 1.63f, 0.41f, 8),
                    t(ThingType.SPRAY, 1.69f, 0.41f, 9),
                    t(ThingType.SPRAY, 1.75f, 0.41f, 3),
                    t(ThingType.SUNGLASSES, 2.62f, 0.94f),
                    t(ThingType.FLOWER_CROWN, 2.46f, 0.93f),
                    t(ThingType.STAR_GLASSES, 1.3f, 0.95f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.eliseLook(), 0.92f, y = 0.94f, name = "Elise"),
                    PersonDef(Species.FOLK, Residents.sanderLook(), 1.1f, seat = 6 to 0, name = "Sander"),
                ),
            )
            PlaceId.BEACH -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, 2.3f, floor), Ground(2.3f, id.width, 0.97f)),
                water = Water(2.3f, id.width, 0.78f, 0.97f),
                fixtures = listOf(
                    fl(FixtureType.UMBRELLA, 0.28f, depth = -0.06f),             // 0
                    fl(FixtureType.LOUNGER, 0.44f, depth = -0.02f),              // 1
                    fl(FixtureType.SANDCASTLE, 0.97f, depth = 0.06f),           // 2
                    f(FixtureType.PIER, 2.45f, 0.97f),           // 3
                    f(FixtureType.FISHING_SPOT, 2.88f, 0.71f),   // 4
                    f(FixtureType.BOAT, 3.42f, 0.80f),           // 5
                    fl(FixtureType.LAMP_POST, 1.62f, depth = -0.08f),            // 6
                ),
                things = listOf(
                    t(ThingType.BEACH_BALL, 0.75f, 0.93f),
                    t(ThingType.BUCKET, 1.14f, 0.94f, 0),
                    t(ThingType.SPADE, 1.21f, 0.94f),
                    t(ThingType.SHELL, 1.42f, 0.95f),
                    t(ThingType.STARFISH, 1.84f, 0.87f),
                    t(ThingType.SWIM_RING, 2.12f, 0.87f),
                    t(ThingType.DUCK, 3.0f, 0.7f),
                    t(ThingType.ROCK, 3.7f, 0.9f),
                    t(ThingType.ICE_CREAM, 0.5f, 0.8f, 3),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 1, height = 1.14f, hair = 0, hairColor = 6, top = 3, topColor = 6, bottom = 1, bottomColor = 13, shoes = 12, extra = 2), 0.44f, seat = 1 to 0, hat = ThingType.SUN_HAT, name = "Besten"),
                    PersonDef(Species.PUFFIN, Look(skin = 0), 1.74f, y = 0.95f),
                ),
            )
            PlaceId.FOREST -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, 2.55f, floor), Ground(2.55f, id.width, 0.95f)),
                water = Water(2.55f, id.width, 0.83f, 0.95f),
                fixtures = listOf(
                    fl(FixtureType.OWL_TREE, 0.28f, depth = -0.08f),             // 0
                    fl(FixtureType.TENT, 0.80f, depth = -0.05f),                 // 1
                    fl(FixtureType.LOG, 1.32f, depth = 0.03f),                  // 2
                    fl(FixtureType.CAMPFIRE, 1.62f, depth = 0.05f),             // 3
                    fl(FixtureType.STUMP, 1.9f, depth = -0.03f),                 // 4
                    fl(FixtureType.STUMP, 2.26f, depth = 0.06f),                // 5
                ),
                things = listOf(
                    t(ThingType.MARSHMALLOW, 1.9f, 0.79f, on = 4),
                    t(ThingType.SAUSAGE, 2.26f, 0.79f, on = 5),
                    t(ThingType.PINECONE, 1.08f, 0.95f),
                    t(ThingType.MUSHROOM, 0.44f, 0.93f),
                    t(ThingType.MUSHROOM, 2.42f, 0.87f),
                    t(ThingType.FLOWER, 2.05f, 0.94f, 2),
                    t(ThingType.STICK, 1.47f, 0.96f),
                    t(ThingType.FEATHER, 2.5f, 0.87f),
                    t(ThingType.DRAGON_EGG, 0.86f, 0.87f, on = 1),
                    t(ThingType.ROCK, 3.05f, 0.93f),
                    t(ThingType.LEAF, 0.12f, 0.87f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.olveLook(), 2.07f, y = 0.93f, name = "Olve"),
                    PersonDef(Species.BUNNY, Look(skin = 3), 2.4f),
                    PersonDef(Species.ELK, Look(skin = 0), 0.54f, y = 0.86f),
                ),
            )
            PlaceId.LAB -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.TABLE, 0.38f, depth = -0.07f),                // 0
                    f(FixtureType.POTION_RACK, 0.38f, 0.5f),     // 1
                    fl(FixtureType.CAULDRON, 0.98f, depth = 0.04f),             // 2
                    fl(FixtureType.SPELLBOOK, 1.36f, depth = -0.03f),            // 3
                    fl(FixtureType.TELESCOPE, 1.74f, depth = -0.07f),            // 4
                    f(FixtureType.WINDOW, 1.74f, 0.44f, 1),      // 5 round window
                    fl(FixtureType.ROUND_TABLE, 2.13f, depth = -0.03f),          // 6
                    f(FixtureType.CRYSTAL_BALL, 2.13f, 0.77f, on = 6),   // 7
                    fl(FixtureType.BOOKCASE, 2.56f, depth = -0.09f),             // 8
                    f(FixtureType.SHELF, 1.0f, 0.46f),           // 9
                    // The mine tunnel to the cellar of Storhuset (appended last: never reorder this list).
                    fl(FixtureType.SECRET_DOOR, 0.2f, depth = -0.11f),           // 10
                ),
                things = listOf(
                    t(ThingType.FLOWER, 0.29f, 0.76f, 4, on = 0),
                    t(ThingType.ROCK, 0.38f, 0.76f, on = 0),
                    t(ThingType.MUSHROOM, 0.47f, 0.76f, on = 0),
                    t(ThingType.FEATHER, 0.94f, 0.42f),
                    t(ThingType.GEM, 1.06f, 0.42f, 2),
                    t(ThingType.BOOK, 2.51f, 0.75f, 2, on = 8),
                    t(ThingType.BOOK, 2.6f, 0.75f, 3, on = 8),
                    t(ThingType.STAR_JAR, 2.56f, 0.63f, on = 8),
                    t(ThingType.WAND, 2.56f, 0.51f, on = 8),
                    t(ThingType.TEDDY, 1.55f, 0.95f),
                ),
                people = listOf(
                    // Rumle the troll lives here, in the cave behind the falls.
                    PersonDef(Species.FOLK, Look(skin = 13, height = 1.14f, hair = 3, hairColor = 1, ears = 4, top = 4, topColor = 12, bottom = 0, bottomColor = 12, shoes = 12, extra = 2), 1.28f, y = 0.94f, hat = ThingType.WIZARD_HAT, name = "Rumle"),
                    PersonDef(Species.DRAGON, Look(skin = 0), 0.7f, y = 0.95f),
                ),
            )
            PlaceId.MOUNTAIN -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.PINE_TREE, 0.22f, depth = -0.07f),            // 0
                    fl(FixtureType.SAUNA, 0.66f, depth = -0.06f),                // 1
                    fl(FixtureType.SLED_HILL, 1.25f, depth = -0.04f),            // 2
                    fl(FixtureType.SNOWMAN, 1.74f, depth = 0.05f),              // 3
                    fl(FixtureType.SKI_JUMP, 2.3f, depth = -0.05f),              // 4
                    fl(FixtureType.LAMP_POST, 2.7f, depth = -0.07f),             // 5
                    fl(FixtureType.ICE_POND, 3.1f, depth = 0.06f),              // 6
                    fl(FixtureType.COCOA_STAND, 3.62f, depth = -0.06f),          // 7
                ),
                things = listOf(
                    t(ThingType.SNOWBALL, 1.6f, 0.95f),
                    t(ThingType.SNOWBALL, 1.88f, 0.94f),
                    t(ThingType.SLED, 0.94f, 0.95f),
                    t(ThingType.COIN, 2.76f, 0.93f),
                    t(ThingType.COIN, 3.3f, 0.86f, on = 6),
                    t(ThingType.COCOA, 3.6f, 0.72f, on = 7),
                    t(ThingType.WAFFLE, 3.66f, 0.72f, on = 7),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 1, height = 0.9f, hair = 5, hairColor = 3, eyes = 1, top = 5, topColor = 6, bottom = 0, bottomColor = 11, shoes = 0), 1.06f, seat = 2 to 0, hat = ThingType.BEANIE, hatVariant = 2, name = "Frida"),
                    PersonDef(Species.DOG, Look(skin = 1), 2.02f, y = 0.95f),
                ),
            )
            PlaceId.FARM -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.TRACTOR, 0.45f, depth = 0.03f),              // 0
                    fl(FixtureType.HAY_BALE, 1.62f, depth = -0.05f),             // 1
                    fl(FixtureType.CHICKEN_COOP, 2.0f, depth = -0.07f),          // 2
                    fl(FixtureType.VEGETABLE_PATCH, 2.5f, depth = 0.05f),       // 3
                    fl(FixtureType.WATER_TROUGH, 2.98f, depth = -0.02f),         // 4
                    fl(FixtureType.TIRE_STACK, 3.3f, depth = -0.06f),            // 5
                    fl(FixtureType.WORKBENCH, 3.7f, depth = -0.06f),             // 6
                    f(FixtureType.TOOL_WALL, 3.7f, 0.5f),        // 7
                    fl(FixtureType.WOOD_PILE, 4.15f, depth = 0.02f),            // 8
                ),
                things = listOf(
                    t(ThingType.EGG, 1.94f, 0.92f),
                    t(ThingType.SEEDS, 2.3f, 0.95f),
                    t(ThingType.WATERING_CAN, 2.72f, 0.95f),
                    t(ThingType.PLANK, 3.64f, 0.67f, on = 6),
                    t(ThingType.HAMMER, 3.8f, 0.67f, on = 6),
                    t(ThingType.CARROT, 2.2f, 0.96f),
                    t(ThingType.MILK, 1.58f, 0.72f, on = 1),
                    t(ThingType.TIRE, 3.42f, 0.93f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.solveLook(), 0.38f, seat = 0 to 0, hat = ThingType.CAP, hatVariant = 1, name = "Sølve"),
                    PersonDef(Species.FOLK, Residents.eiraLook(), 3.5f, y = 0.93f, hand = ThingType.WRENCH, name = "Eira"),
                    PersonDef(Species.HORSE, Look(skin = 0), 1.28f, y = 0.9f),
                    PersonDef(Species.CHICKEN, Look(skin = 0), 1.9f, y = 0.92f),
                    PersonDef(Species.CHICKEN, Look(skin = 1), 2.12f, y = 0.95f),
                    PersonDef(Species.SHEEP, Look(skin = 0), 2.72f),
                    PersonDef(Species.COW, Look(skin = 0), 3.06f, y = 0.94f),
                ),
            )
            PlaceId.SPACE -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.ROCKET_SHIP, 0.35f, depth = -0.06f),          // 0
                    fl(FixtureType.CONTROL_PANEL, 0.95f, depth = -0.08f),        // 1
                    f(FixtureType.PORTHOLE, 0.95f, 0.5f),        // 2
                    fl(FixtureType.GRAVITY_LEVER, 1.35f, depth = 0.04f),        // 3
                    fl(FixtureType.ORRERY, 1.8f, depth = 0.03f),                // 4
                    f(FixtureType.SPACE_BED, 2.3f, 0.75f),       // 5
                    fl(FixtureType.FOOD_DISPENSER, 2.75f, depth = -0.08f),       // 6
                    f(FixtureType.PORTHOLE, 3.2f, 0.5f, 1),      // 7
                ),
                things = listOf(
                    t(ThingType.PLANET, 1.55f, 0.35f, 0),
                    t(ThingType.PLANET, 1.95f, 0.28f, 1),
                    t(ThingType.PLANET, 2.1f, 0.5f, 2),
                    t(ThingType.PLANET, 2.3f, 0.3f, 3),
                    t(ThingType.PLANET, 2.55f, 0.45f, 4),
                    t(ThingType.PLANET, 2.8f, 0.25f, 5),
                    t(ThingType.PLANET, 3.05f, 0.62f, 6),
                    t(ThingType.PLANET, 3.3f, 0.35f, 7),
                    t(ThingType.PLANET, 3.45f, 0.72f, 8),
                    t(ThingType.PLANET, 1.2f, 0.3f, 9),
                    t(ThingType.SPACE_FOOD, 1.0f, 0.6f, 1),
                    t(ThingType.SPACE_HELMET, 2.95f, 0.8f),
                    t(ThingType.ROCKET, 1.6f, 0.62f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.olvarLook(), 0.35f, seat = 0 to 0, hat = ThingType.SPACE_HELMET, name = "Olvar"),
                    PersonDef(Species.CAT, Look(skin = 1), 2.6f, y = 0.4f, hat = ThingType.SPACE_HELMET),
                ),
            )
            PlaceId.TIVOLI -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.FERRIS_WHEEL, 0.45f, depth = -0.08f),          // 0
                    fl(FixtureType.CANDY_FLOSS_STAND, 1.05f, depth = -0.07f),     // 1
                    fl(FixtureType.CAROUSEL, 1.62f, depth = -0.04f),              // 2
                    fl(FixtureType.TRAMPOLINE, 2.22f, depth = 0.05f),             // 3
                    fl(FixtureType.POPCORN_CART, 2.64f, depth = -0.06f),          // 4
                    fl(FixtureType.CAN_TOSS, 3.04f, depth = -0.08f),              // 5
                    fl(FixtureType.BUMPER_CAR, 3.5f, depth = 0.03f),              // 6
                    fl(FixtureType.BUMPER_CAR, 4.05f, 1, depth = 0.03f),          // 7
                    fl(FixtureType.TRASH_BIN, 2.47f, depth = 0.07f),              // 8
                ),
                things = listOf(
                    t(ThingType.CANDY_FLOSS, 0.99f, 0.68f, 0, on = 1),
                    t(ThingType.CANDY_FLOSS, 1.11f, 0.68f, 1, on = 1),
                    t(ThingType.BALL, 2.92f, 0.68f, on = 5),
                    t(ThingType.BALL, 2.98f, 0.68f, on = 5),
                    t(ThingType.BALL, 3.18f, 0.68f, on = 5),
                    t(ThingType.TEDDY, 3.1f, 0.68f, on = 5),
                    t(ThingType.POPCORN, 2.4f, 0.95f),
                    t(ThingType.BALLOON, 2.44f, 0.3f, 2),
                    t(ThingType.BALLOON, 2.5f, 0.3f, 4),
                    t(ThingType.WHOOPEE, 2.2f, 0.95f, 1),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 9, height = 0.9f, hair = 8, hairColor = 8, eyes = 4, ears = 1, top = 1, topColor = 2, bottom = 1, bottomColor = 11, shoes = 0), 1.62f, seat = 2 to 0, name = "Velte"),
                    PersonDef(Species.SHEEP, Look(skin = 0), 3.5f, seat = 6 to 0),
                ),
            )
            PlaceId.SHOP -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.SHOP_SHELF, 0.28f, depth = -0.08f),            // 0
                    fl(FixtureType.SHOP_SHELF, 0.68f, 1, depth = -0.08f),         // 1
                    fl(FixtureType.FRUIT_CRATE, 1.06f, depth = 0.03f),            // 2
                    fl(FixtureType.SCALE, 1.32f, depth = 0.05f),                  // 3
                    fl(FixtureType.FREEZER, 1.72f, depth = -0.05f),               // 4
                    fl(FixtureType.SODA_FRIDGE, 2.14f, depth = -0.09f),           // 5
                    fl(FixtureType.CHECKOUT, 2.72f, depth = -0.01f),              // 6
                    fl(FixtureType.CART, 3.22f, depth = 0.05f),                   // 7
                    fl(FixtureType.TRASH_BIN, 1.52f, depth = 0.05f),              // 8
                ),
                things = listOf(
                    t(ThingType.BREAD, 0.18f, 0.87f, on = 0),
                    t(ThingType.MILK, 0.3f, 0.87f, on = 0),
                    t(ThingType.JUICE, 0.38f, 0.87f, on = 0),
                    t(ThingType.COOKIE, 0.2f, 0.73f, on = 0),
                    t(ThingType.CUPCAKE, 0.3f, 0.73f, 1, on = 0),
                    t(ThingType.EGG, 0.37f, 0.73f, on = 0),
                    t(ThingType.PIZZA, 0.28f, 0.59f, on = 0),
                    t(ThingType.CARROT, 0.59f, 0.87f, on = 1),
                    t(ThingType.POTATO, 0.68f, 0.87f, on = 1),
                    t(ThingType.APPLE, 0.77f, 0.87f, on = 1),
                    t(ThingType.BANANA, 0.64f, 0.73f, on = 1),
                    t(ThingType.STRAWBERRY, 0.76f, 0.73f, on = 1),
                    t(ThingType.BROWN_CHEESE, 0.68f, 0.59f, on = 1),
                    t(ThingType.ICE_CREAM, 1.62f, 0.85f, 0, on = 4),
                    t(ThingType.ICE_CREAM, 1.72f, 0.85f, 2, on = 4),
                    t(ThingType.ICE_CREAM, 1.82f, 0.85f, 4, on = 4),
                    t(ThingType.SODA, 2.1f, 0.86f, 0, on = 5),
                    t(ThingType.SODA, 2.18f, 0.86f, 1, on = 5),
                    t(ThingType.SODA, 2.14f, 0.74f, 2, on = 5),
                    t(ThingType.JUICE, 2.14f, 0.62f, on = 5),
                    t(ThingType.WATERMELON, 2.54f, 0.7f, on = 6),
                    t(ThingType.APPLE, 3.16f, 0.78f, on = 7),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.beritLook(), 2.92f, seat = 6 to 0, name = "Berit"),
                    PersonDef(Species.CAT, Look(skin = 2), 3.3f, seat = 7 to 0),
                ),
            )
            PlaceId.DOCTOR -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.BENCH, 0.36f, depth = 0.03f),                  // 0
                    f(FixtureType.EYE_CHART, 0.36f, 0.5f),                        // 1
                    f(FixtureType.HEIGHT_CHART, 0.78f, 0.8f),                     // 2
                    fl(FixtureType.XRAY, 1.12f, depth = -0.08f),                  // 3
                    fl(FixtureType.EXAM_BED, 1.66f, depth = -0.02f),              // 4
                    f(FixtureType.MEDICINE_CABINET, 2.12f, 0.52f),                // 5
                    fl(FixtureType.DOCTOR_DESK, 2.48f, depth = -0.06f),           // 6
                    fl(FixtureType.CHAIR, 2.8f, depth = 0.02f),                   // 7
                    fl(FixtureType.PLANT_BIG, 2.92f, depth = -0.09f),             // 8
                ),
                things = listOf(
                    t(ThingType.BANDAGE, 2.05f, 0.5f),
                    t(ThingType.BANDAGE, 2.19f, 0.5f),
                    t(ThingType.SYRUP, 2.1f, 0.4f),
                    t(ThingType.STETHOSCOPE, 2.4f, 0.72f, on = 6),
                    t(ThingType.THERMOMETER, 2.53f, 0.72f, on = 6),
                    t(ThingType.LOLLIPOP, 2.62f, 0.72f, 2, on = 6),
                    t(ThingType.TEDDY, 0.24f, 0.83f, on = 0),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 10, height = 1.03f, hair = 4, hairColor = 4, eyes = 0, top = 1, topColor = 9, bottom = 0, bottomColor = 11, shoes = 7), 2.8f, seat = 7 to 0, glasses = ThingType.ROUND_GLASSES, name = "Hilde"),
                    PersonDef(Species.DOG, Look(skin = 2), 0.44f, seat = 0 to 1, glasses = ThingType.BANDAGE),
                    PersonDef(Species.BUNNY, Look(skin = 1), 1.66f, seat = 4 to 0),
                ),
            )
            PlaceId.STAGE -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.STAGE_PLATFORM, 1.0f, depth = -0.07f),         // 0
                    f(FixtureType.DRUM_KIT, 0.52f, 0.78f, on = 0),                // 1
                    f(FixtureType.MIC_STAND, 1.0f, 0.78f, on = 0),                // 2
                    f(FixtureType.XYLOPHONE, 1.42f, 0.78f, on = 0),               // 3
                    fl(FixtureType.SPEAKER, 1.95f, depth = -0.08f),               // 4
                    f(FixtureType.DISCO_BALL, 1.0f, 0.22f),                       // 5
                    fl(FixtureType.SMOKE_MACHINE, 2.28f, depth = 0.04f),          // 6
                    fl(FixtureType.BENCH, 2.66f, depth = 0.05f),                  // 7
                    fl(FixtureType.SPEAKER, 3.05f, 1, depth = -0.08f),            // 8
                ),
                things = listOf(
                    t(ThingType.GUITAR, 1.2f, 0.78f, on = 0),
                    t(ThingType.MICROPHONE, 0.78f, 0.78f, on = 0),
                    t(ThingType.FLOWER, 2.58f, 0.83f, 1, on = 7),
                    t(ThingType.FLOWER, 2.72f, 0.83f, 3, on = 7),
                    t(ThingType.DRUM, 2.42f, 0.95f),
                    t(ThingType.BALLOON, 2.9f, 0.3f, 5),
                    t(ThingType.SODA, 2.8f, 0.83f, 1, on = 7),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Residents.eilevLook(), 1.08f, y = 0.705f, name = "Eilev"),
                    PersonDef(Species.DOG, Look(skin = 0), 0.54f, seat = 1 to 0, glasses = ThingType.SUNGLASSES),
                ),
            )
            // Storhuset: each floor describes itself in its own file (HouseGround.kt and so on).
            PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER, PlaceId.MANOR_ATTIC, PlaceId.MANOR_CELLAR, PlaceId.MANOR_GARDEN -> House.floor(id)!!.blueprint()

            // Mitt hus: the stairs and doors only; the rooms are built by the child (see [Mine]).
            PlaceId.MINE_YARD, PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> House.floor(id)!!.blueprint()

            PlaceId.VAGSTADDALEN -> Vagstaddalen.blueprint()
            PlaceId.CLOUD_ISLAND -> PlaceSpec(id,grounds=listOf(Ground(0f,id.width,floor)),water=null,
                fixtures=listOf(fl(FixtureType.PLAY_PICNIC,1.1f),fl(FixtureType.TELESCOPE,2.7f)),
                things=listOf(t(ThingType.APPLE,1.1f,0.6f),t(ThingType.STAR_JAR,2.2f,0.9f)),
                people=listOf(PersonDef(Species.PUFFIN,Look(),2.1f,0.88f)))

            PlaceId.HEILEBERGET -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.MOUNTAIN_HUT, 0.75f, depth = -0.08f),          // 0
                    fl(FixtureType.BENCH, 1.5f, depth = 0.02f),                   // 1
                    fl(FixtureType.CAMPFIRE, 1.98f, depth = 0.05f),               // 2
                    fl(FixtureType.LOG, 2.38f, depth = 0.05f),                    // 3
                    fl(FixtureType.CABLE_STATION, 3.15f, depth = -0.08f),         // 4 bottom station
                    fl(FixtureType.CABLE_CAR, 3.15f, depth = -0.04f),             // 5 the cabin; rides up to the ledge
                    fl(FixtureType.ECHO_ROCK, 4.4f, depth = 0.03f),               // 6
                    fl(FixtureType.PINE_TREE, 3.85f, depth = -0.11f),             // 7
                    fl(FixtureType.TENT, 5.0f, depth = 0.04f),                    // 8
                    fl(FixtureType.ROCK_LEDGE, 6.3f, depth = -0.03f),             // 9 reached by the cable car
                    f(FixtureType.CABLE_STATION, 6.0f, 0.58f, on = 9),            // 10 top station
                    fl(FixtureType.SUMMIT_ROCK, 8.15f, depth = -0.02f),           // 11 the summit
                    f(FixtureType.SUMMIT_FLAG, 8.4f, 0.38f, on = 11),             // 12
                    f(FixtureType.EAGLE_NEST, 7.8f, 0.38f, on = 11),              // 13
                    fl(FixtureType.LAMP_POST, 2.75f, depth = -0.06f),             // 14
                ),
                things = listOf(
                    t(ThingType.THERMOS, 1.55f, 0.79f, on = 1),
                    t(ThingType.BUN, 1.42f, 0.79f, on = 1),
                    t(ThingType.BINOCULARS, 2.4f, 0.79f, on = 3),
                    t(ThingType.MARSHMALLOW, 2.3f, 0.79f, on = 3),
                    t(ThingType.COCOA, 0.6f, 0.4f, on = 0),
                    t(ThingType.EGG, 7.8f, 0.2f, 1, on = 13),
                    t(ThingType.FEATHER, 7.45f, 0.3f, on = 11),
                    t(ThingType.CARROT, 4.0f, 0.95f),
                    t(ThingType.APPLE, 4.7f, 0.94f),
                    t(ThingType.MUSHROOM, 3.55f, 0.94f),
                    t(ThingType.FLOWER, 5.7f, 0.95f, 4),
                    t(ThingType.FLOWER, 5.8f, 0.96f, 2),
                    t(ThingType.ROCK, 2.95f, 0.95f),
                    t(ThingType.GEM, 6.9f, 0.5f, 3, on = 9),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 2, height = 1.03f, hair = 6, hairColor = 6, eyes = 2, top = 2, topColor = 7, bottom = 2, bottomColor = 11, shoes = 12), 1.5f, seat = 1 to 0, hat = ThingType.BEANIE, hatVariant = 2, glasses = ThingType.ROUND_GLASSES, name = "BesteSonja"),
                    PersonDef(Species.FOLK, Residents.tuvaLook(), 2.05f, y = 0.94f, hand = ThingType.THERMOS, name = "Tuva"),
                    PersonDef(Species.GOAT, Look(skin = 0), 4.0f, y = 0.93f),
                    PersonDef(Species.GOAT, Look(skin = 1), 6.65f, y = 0.4f),
                    PersonDef(Species.GOAT, Look(skin = 2), 8.4f, y = 0.25f),
                ),
            )
            PlaceId.UNDERWATER -> PlaceSpec(
                id,
                grounds = listOf(Ground(0f, id.width, floor)),
                water = null,
                fixtures = listOf(
                    fl(FixtureType.SHIPWRECK, 0.5f, depth = -0.09f),              // 0
                    fl(FixtureType.KELP, 1.02f, depth = -0.07f),                  // 1
                    fl(FixtureType.GIANT_CLAM, 1.34f, depth = 0.04f),             // 2
                    fl(FixtureType.CORAL, 1.8f, depth = -0.05f),                  // 3
                    fl(FixtureType.SUBMARINE, 2.42f, depth = -0.02f),             // 4
                    fl(FixtureType.OCTOPUS, 2.98f, depth = 0.03f),                // 5
                    fl(FixtureType.CHEST, 3.32f, depth = -0.05f),                 // 6
                    fl(FixtureType.CORAL, 3.62f, 1, depth = -0.07f),              // 7
                ),
                things = listOf(
                    t(ThingType.COIN, 0.5f, 0.85f, on = 0),
                    t(ThingType.PEARL, 1.34f, 0.87f, on = 2),
                    t(ThingType.STARFISH, 1.8f, 0.68f, on = 3),
                    t(ThingType.GEM, 3.28f, 0.885f, 1, on = 6),
                    t(ThingType.COIN, 3.37f, 0.885f, on = 6),
                    t(ThingType.SHELL, 2.1f, 0.95f),
                    t(ThingType.BOOT, 0.95f, 0.95f),
                    t(ThingType.FISH, 2.72f, 0.5f),
                    t(ThingType.DIVING_MASK, 1.58f, 0.95f),
                    t(ThingType.SWIM_RING, 3.1f, 0.3f),
                ),
                people = listOf(
                    PersonDef(Species.FOLK, Look(skin = 4, height = 0.9f, hair = 7, hairColor = 3, top = 0, topColor = 1, bottom = 1, bottomColor = 5, shoes = 4), 1.62f, y = 0.55f, glasses = ThingType.DIVING_MASK, name = "Iver"),
                    PersonDef(Species.PUFFIN, Look(skin = 0), 2.2f, y = 0.42f),
                ),
            )
        }
    }
}
