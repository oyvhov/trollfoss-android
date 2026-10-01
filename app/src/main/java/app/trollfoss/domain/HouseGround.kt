package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, ground floor: Storstova. Twelve units, six rooms of two units each: the hall with the grand
 * stairs, the living room with the fireplace and the TV, the library with its secret bookcase, the dining
 * room, the kitchen and the winter garden. Rolf, the robot butler, lives here and serves and tidies.
 *
 * This file holds the blueprint, the passages and the specs. The rules live in HouseGroundRules.kt, the
 * butler in HouseGroundRolf.kt, the art in ui/art/HouseGround*Art.kt and the effects in ui/play/HouseGroundFx.kt.
 */

/** Blueprint indices of the ground floor, so rules, glimt and passages never count by hand (a test checks them). */
object GroundIx {
    // The ways between floors keep the indices 0..5 that docs/HUSET.md promises.
    const val STAIRS = 0
    const val LIFT = 1
    const val CELLAR_DOOR = 2
    const val SECRET_SHELF = 3
    const val DUMBWAITER = 4
    const val GARDEN_DOOR = 5

    // Hall
    const val CLOCK = 6
    const val COAT_RACK = 7
    const val UMBRELLAS = 8
    const val STAINED_GLASS = 9
    const val ARMOUR = 10
    const val CHANDELIER = 11
    const val POST_SLOT = 12
    const val PORTRAIT_HALL = 13

    // Living room
    const val FIREPLACE = 14
    const val WING_FIRE = 15
    const val SOFA = 16
    const val COFFEE_TABLE = 17
    const val POPCORN = 18
    const val GLOBE = 19
    const val TV = 20
    const val AQUARIUM = 21
    const val LAMP_LIVING = 22
    const val BEANBAG_A = 23
    const val BEANBAG_B = 24

    // Library
    const val SHELF_A = 25
    const val SHELF_B = 26
    const val LADDER = 27
    const val DESK = 28
    const val WING_READING = 29
    const val LAMP_LIBRARY = 30
    const val LECTERN = 31
    const val BUST = 32
    const val WINDOW_LIBRARY = 33

    // Dining room
    const val DINING_TABLE = 34
    const val CHAIR_ROW = 35
    const val CHAIR_LEFT = 36
    const val CHAIR_RIGHT = 37
    const val BELL = 38
    const val CAKE = 39
    const val CANDELABRA = 40
    const val SIDEBOARD = 41
    const val CHANDELIER_DINING = 42
    const val PORTRAIT_A = 43
    const val PORTRAIT_B = 44

    // Kitchen
    const val RANGE = 45
    const val SINK = 46
    const val MIXER = 47
    const val PIZZA_OVEN = 48
    const val FRIDGE = 49
    const val JAM_CABINET = 50
    const val ISLAND = 51
    const val STOOLS = 52

    // Winter garden
    const val FERN = 53
    const val FOUNTAIN = 54
    const val SOFIE = 55
    const val PALM = 56
    const val HAMMOCK = 57
    const val FLOWERS = 58

    const val COUNT = 59
}

/** Codes of the ground floor's effects ([HouseFx.GROUND] plus a number); the player is `ui/play/HouseGroundFx.kt`. */
object GroundCode {
    private const val B = HouseFx.GROUND

    /** The grandfather clock strikes; arg is how many times (1..12). */
    const val CLOCK = B + 1

    /** The cellar door creaks. */
    const val CREAK = B + 2

    /** The lift doors open and the brass instrument rings. */
    const val LIFT = B + 3

    /** A hat falls from the coat rack; arg is the hat. */
    const val HAT = B + 4

    /** An umbrella pops out of the stand (arg 1) or goes back in (arg 0). */
    const val UMBRELLA = B + 5

    /** Riddar Rusten: 0 clanks, 1 salutes, 2 hiccups, 3 the helmet pops off, 4 it hops back on. */
    const val ARMOUR = B + 6

    /** The postal slot: 1 a letter comes, 0 nothing today, 2 a letter is posted. */
    const val POST = B + 7

    /** The chandelier swings; arg 1 when its light is switched on, 0 when off. */
    const val CHANDELIER = B + 8

    /** A portrait pulls a face; arg is its look. */
    const val PORTRAIT = B + 9

    /** The stained-glass window throws coloured light; or the curtains of a window move (arg 1 closed, 0 open). */
    const val WINDOW = B + 10

    /** The fire is lit (1) or put out (0). */
    const val FIRE = B + 11

    /** A treat falls out of a stocking. */
    const val STOCKING = B + 12

    /** The sofa puffs dust, and now and then coins fall out of it. */
    const val SOFA = B + 13

    /** The TV is on channel [arg] (0 is off). */
    const val TV = B + 14

    /** Film night starts (1) or ends (0). */
    const val FILM = B + 15

    /** One piece of popcorn. */
    const val POPCORN = B + 16

    /** The globe spins; arg 1 when a pin jumps out. */
    const val GLOBE = B + 17

    /** The fish swim to the finger. */
    const val FISH = B + 18

    /** The fish eat what was dropped in. */
    const val FISH_FEED = B + 19

    /** A lamp is switched; arg 1 on. */
    const val LAMP = B + 20

    /** Books flutter out of a shelf. */
    const val BOOKS = B + 21

    /** The red book is pulled and the bookcase swings open. */
    const val LEVER = B + 22

    /** The library ladder rolls. */
    const val LADDER = B + 23

    /** The quill scribbles. */
    const val DESK = B + 24

    /** The talking book talks; arg is its story, 9 when it shushes. */
    const val LECTERN = B + 25

    /** The bust coughs. */
    const val BUST = B + 26

    /** The table lays itself (arg 1) or clears itself (arg 0); plates clink (arg 2). */
    const val TABLE = B + 27

    /** The cake: 0 candles lit again, 1 the birthday song starts, 2 the candles are blown out. */
    const val CAKE = B + 28

    /** The oven range: 0 a hob lit, 1 a hob out, 2 something baked. */
    const val RANGE = B + 29

    /** The pizza oven: 0 breathes fire, 1 a pizza comes out, 2 fire out. */
    const val PIZZA = B + 30

    /** The mixer: 0 winds up, 1 sprays. */
    const val MIXER = B + 31

    /** The tap runs (1) or stops (0). */
    const val SINK = B + 32

    /** The fountain: 0 splashes, 1 a wish. */
    const val FOUNTAIN = B + 33

    /** A plant grows to the stage in arg. */
    const val GROW = B + 34

    /** A flower is picked from a plant in bloom. */
    const val BLOOM = B + 35

    /** Sofie: 0 snaps, 1 burps, 2 spits out the key, 3 chews. */
    const val SOFIE = B + 36

    /** The hammock sways. */
    const val HAMMOCK = B + 37

    /** Rolf: 0 beeps, 1 ta-da, 2 clonks, 3 picks something up, 4 puts it down, 5 serves, 6 switches a lamp. */
    const val ROLF = B + 38

    /** The glass door to the garden chimes. */
    const val GARDEN_DOOR = B + 39

    /** The stairs: a grand creak of wood. */
    const val STAIRS = B + 40

    /** A fridge, a cupboard or a sideboard opens (arg 1) or shuts (0). */
    const val DOOR = B + 41

    /** A beanbag or an armchair puffs. */
    const val PUFF = B + 42
}

/** Room numbers of the ground floor, left to right. */
object GroundRoom {
    const val HALL = 0
    const val LIVING = 1
    const val LIBRARY = 2
    const val DINING = 3
    const val KITCHEN = 4
    const val GARDEN = 5
}

object GroundFloor : Floor {
    override val place = PlaceId.MANOR_GROUND

    override val rooms = listOf(0f..2f, 2f..4f, 4f..6f, 6f..8f, 8f..10f, 10f..12f)

    override val passages = listOf(
        Passage("ground-stairs-up", place, GroundIx.STAIRS, PassageKind.STAIRS, PlaceId.MANOR_UPPER, "landing"),
        Passage("ground-lift", place, GroundIx.LIFT, PassageKind.LIFT, PlaceId.MANOR_UPPER, "lift"),
        Passage("ground-cellar-door", place, GroundIx.CELLAR_DOOR, PassageKind.STAIRS, PlaceId.MANOR_CELLAR, "stairs-top"),
        Passage("ground-bookshelf", place, GroundIx.SECRET_SHELF, PassageKind.SECRET, PlaceId.MANOR_ATTIC, "secret-room", locked = "manor_bookshelf"),
        Passage("ground-dumbwaiter", place, GroundIx.DUMBWAITER, PassageKind.DUMBWAITER, PlaceId.MANOR_UPPER, "dumbwaiter"),
        Passage("ground-garden-door", place, GroundIx.GARDEN_DOOR, PassageKind.DOOR, PlaceId.MANOR_GARDEN, "house-door"),
    )

    override val arrivals = listOf(
        Arrival("stairs-foot", 1.82f),
        Arrival("lift", 0.55f),
        Arrival("cellar-door", 1.0f),
        Arrival("library-secret", 5.12f),
        Arrival("dumbwaiter", 9.88f),
        Arrival("garden-door", 11.62f),
        Arrival("slide-end", 3.86f),
        Arrival("pole-end", 0.78f),
    )

    /** How dark the floor is during film night (see [GroundRules]); 0 the rest of the time. */
    @Volatile
    internal var dim = 0f

    override val darkness: Float get() = dim

    override fun hangouts(night: Boolean) = if (night) {
        listOf(
            Arrival("fire", 2.9f), Arrival("sofa", 3.1f), Arrival("reading", 5.35f), Arrival("snack", 8.9f), Arrival("hall", 1.5f),
        )
    } else {
        listOf(
            Arrival("hall", 1.6f), Arrival("living", 3.0f), Arrival("library", 5.4f), Arrival("dining", 6.9f),
            Arrival("kitchen", 8.9f), Arrival("garden", 10.9f),
        )
    }

    override fun specOf(type: FixtureType): FixtureSpec? = GroundSpecs.of(type)

    override fun rules(sim: Sim, random: Random): FloorRules = GroundRules(sim, random)

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        /** Furniture standing on the floor, [depth] in front of (+) or behind (-) the usual line. */
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        /** Something hanging on the wall: [y] is its bottom edge. */
        fun w(type: FixtureType, x: Float, y: Float, variant: Int = 0) = FixtureDef(type, x, y, variant)
        /** Something standing on [host]; [y] is the height of the host's top above nothing (the host's own depth is added). */
        fun on(type: FixtureType, x: Float, y: Float, host: Int, variant: Int = 0) = FixtureDef(type, x, y, variant, 0f, host)

        val fixtures = listOf(
            // 0..5: the ways between floors
            f(FixtureType.STAIRCASE, 1.30f, depth = -0.07f),                 // 0 the grand stairs up
            f(FixtureType.LIFT, 0.55f, depth = -0.08f),                      // 1
            f(FixtureType.DOOR, 1.00f, 0, depth = -0.065f),                  // 2 cellar door, under the stairs
            f(FixtureType.SECRET_DOOR, 5.06f, depth = -0.085f),              // 3 the bookcase with the red book
            w(FixtureType.DUMBWAITER, 9.88f, 0.66f),                         // 4 hatch in the kitchen wall
            f(FixtureType.DOOR, 11.66f, 1, depth = -0.085f),                 // 5 glass door to the garden

            // Hall
            f(FixtureType.GR_CLOCK, 0.84f, depth = -0.085f),                 // 6
            w(FixtureType.GR_COAT_RACK, 0.20f, 0.50f),                       // 7
            f(FixtureType.GR_UMBRELLA_STAND, 0.22f, depth = -0.07f),         // 8
            w(FixtureType.GR_WINDOW, 1.88f, 0.52f, 0),                       // 9 stained glass behind the knight
            f(FixtureType.GR_ARMOUR, 1.88f, depth = -0.04f),                 // 10
            w(FixtureType.GR_CHANDELIER, 1.30f, 0.30f, 0),                   // 11
            w(FixtureType.GR_POST_SLOT, 1.62f, 0.44f),                       // 12
            w(FixtureType.GR_PORTRAIT, 0.55f, 0.28f, 0),                     // 13

            // Living room
            f(FixtureType.GR_FIREPLACE, 2.55f, depth = -0.085f),             // 14
            f(FixtureType.GR_WINGCHAIR, 2.22f, 0, depth = 0.04f),            // 15
            f(FixtureType.GR_SOFA, 3.12f, depth = -0.04f),                   // 16
            f(FixtureType.GR_COFFEE_TABLE, 3.12f, depth = 0.05f),            // 17
            on(FixtureType.GR_POPCORN_BOWL, 3.12f, fl - 0.08f, GroundIx.COFFEE_TABLE), // 18
            f(FixtureType.GR_GLOBE, 2.76f, depth = 0.04f),                   // 19
            f(FixtureType.GR_TV, 3.72f, depth = -0.085f),                    // 20
            w(FixtureType.GR_AQUARIUM, 3.12f, 0.40f),                        // 21
            f(FixtureType.GR_FLOOR_LAMP, 3.45f, 0, depth = -0.06f),          // 22
            f(FixtureType.BEANBAG, 3.70f, 0, depth = 0.05f),                 // 23 where the slide lands
            f(FixtureType.BEANBAG, 3.86f, 1, depth = 0.03f),                 // 24

            // Library
            f(FixtureType.GR_BOOKSHELF, 4.34f, 0, depth = -0.085f),          // 25
            f(FixtureType.GR_BOOKSHELF, 4.70f, 1, depth = -0.085f),          // 26
            f(FixtureType.GR_LADDER, 4.70f, depth = -0.07f),                 // 27
            f(FixtureType.GR_DESK, 5.62f, depth = -0.05f),                   // 28
            f(FixtureType.GR_WINGCHAIR, 5.28f, 2, depth = 0.04f),            // 29 the reading chair
            f(FixtureType.GR_FLOOR_LAMP, 5.90f, 1, depth = -0.05f),          // 30
            f(FixtureType.GR_LECTERN, 4.46f, depth = 0.05f),                 // 31 the talking book
            f(FixtureType.GR_BUST, 4.18f, depth = 0.03f),                    // 32
            w(FixtureType.GR_WINDOW, 5.62f, 0.50f, 1),                       // 33

            // Dining room
            f(FixtureType.GR_DINING_TABLE, 7.00f, depth = 0f),               // 34
            f(FixtureType.GR_CHAIR_ROW, 7.00f, depth = -0.075f),             // 35 four chairs behind the table
            f(FixtureType.GR_DINING_CHAIR, 6.40f, 0, depth = 0f),            // 36
            f(FixtureType.GR_DINING_CHAIR, 7.60f, 1, depth = 0f),            // 37
            on(FixtureType.GR_BELL, 7.34f, fl - 0.13f, GroundIx.DINING_TABLE), // 38
            on(FixtureType.GR_CAKE, 7.00f, fl - 0.13f, GroundIx.DINING_TABLE), // 39
            f(FixtureType.GR_CANDELABRA, 7.84f, depth = -0.07f),             // 40
            f(FixtureType.GR_SIDEBOARD, 6.26f, depth = -0.085f),             // 41
            w(FixtureType.GR_CHANDELIER, 7.00f, 0.30f, 1),                   // 42
            w(FixtureType.GR_PORTRAIT, 6.26f, 0.46f, 1),                     // 43
            w(FixtureType.GR_PORTRAIT, 7.80f, 0.46f, 2),                     // 44

            // Kitchen
            f(FixtureType.GR_RANGE, 8.32f, depth = -0.085f),                 // 45
            f(FixtureType.GR_SINK, 8.68f, depth = -0.085f),                  // 46
            on(FixtureType.GR_MIXER, 8.76f, fl - 0.22f, GroundIx.SINK),      // 47
            f(FixtureType.GR_PIZZA_OVEN, 9.14f, depth = -0.085f),            // 48
            f(FixtureType.GR_FRIDGE, 9.64f, depth = -0.085f),                // 49
            w(FixtureType.GR_JAM_CABINET, 8.62f, 0.40f),                     // 50
            f(FixtureType.GR_ISLAND, 9.00f, depth = -0.01f),                 // 51
            f(FixtureType.GR_STOOLS, 9.00f, depth = 0.07f),                  // 52 in front of the island

            // Winter garden
            f(FixtureType.GR_PLANT, 10.30f, 0, depth = -0.07f),              // 53 fern
            f(FixtureType.GR_FOUNTAIN, 10.86f, depth = -0.01f),              // 54
            f(FixtureType.GR_SOFIE, 10.46f, depth = 0.05f),                  // 55
            f(FixtureType.GR_PLANT, 11.18f, 1, depth = -0.075f),             // 56 palm
            f(FixtureType.GR_HAMMOCK, 11.36f, depth = 0.02f),                // 57
            f(FixtureType.GR_PLANT, 11.86f, 2, depth = -0.065f),             // 58 flowers
        )
        check(fixtures.size == GroundIx.COUNT) { "ground floor blueprint has ${fixtures.size} fixtures" }

        fun t(type: ThingType, x: Float, y: Float, variant: Int = 0, host: Int = -1) = ThingDef(type, x, y, variant, host)

        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = fixtures,
            things = listOf(
                // The mantel, the sofa, the table and the desk
                t(ThingType.CANDLE, 2.40f, fl - 0.43f, host = GroundIx.FIREPLACE),
                t(ThingType.STAR_JAR, 2.70f, fl - 0.43f, host = GroundIx.FIREPLACE),
                t(ThingType.PILLOW, 2.98f, fl - 0.085f, 1, host = GroundIx.SOFA),
                t(ThingType.WHOOPEE, 3.27f, fl - 0.085f, 1, host = GroundIx.SOFA),
                t(ThingType.COCOA, 3.02f, fl - 0.08f, host = GroundIx.COFFEE_TABLE),
                t(ThingType.CHEF_HAT, 0.84f, fl - 0.46f, host = GroundIx.CLOCK),
                t(ThingType.BOOK, 5.52f, fl - 0.16f, 1, host = GroundIx.DESK),
                t(ThingType.BOOK, 5.68f, fl - 0.16f, 3, host = GroundIx.DESK),
                t(ThingType.PEPPER, 6.68f, fl - 0.13f, host = GroundIx.DINING_TABLE),
                // The kitchen island: fruit, and dough for pizza
                t(ThingType.APPLE, 8.80f, fl - 0.20f, host = GroundIx.ISLAND),
                t(ThingType.APPLE, 8.88f, fl - 0.20f, host = GroundIx.ISLAND),
                t(ThingType.STRAWBERRY, 8.96f, fl - 0.20f, host = GroundIx.ISLAND),
                t(ThingType.DOUGH, 9.10f, fl - 0.20f, host = GroundIx.ISLAND),
                t(ThingType.DOUGH, 9.20f, fl - 0.20f, host = GroundIx.ISLAND),
                t(ThingType.BROWN_CHEESE, 9.27f, fl - 0.20f, host = GroundIx.ISLAND),
                // On the floor, for Rolf to tidy and the cat to chase
                t(ThingType.BALL, 1.60f, 0.955f),
                t(ThingType.TOY_CAR, 2.95f, 0.965f, 1),
                t(ThingType.WATERING_CAN, 10.70f, 0.955f),
                t(ThingType.CARROT, 10.58f, 0.965f),
            ),
            people = listOf(
                PersonDef(Species.ROBOT, Look(skin = 10), 1.55f, name = "Rolf"),
                PersonDef(Species.CAT, Look(skin = 1), 2.40f, y = 0.95f),
                PersonDef(Species.BUNNY, Look(skin = 3), 10.62f, y = 0.955f),
            ),
        )
    }
}
