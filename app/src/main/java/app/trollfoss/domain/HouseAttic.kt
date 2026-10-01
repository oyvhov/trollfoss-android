package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, the attic ("Loftet"): four rooms in a row under the roof, dim even at noon (darkness 0.6; a finger on
 * the screen is a torch, lamps and candles light their corners).
 *
 *  - **Lageret** (0 to 3): the stairs down, sheets over old furniture (one hides Sture), the costume trunk, a rocking
 *    horse, toy cartons, knitting spiders and a round window with a sunbeam.
 *  - **Spøkelsekroken** (3 to 5.5): Sture's cosy nook with a wing chair, a gramophone with silly records, a book tower,
 *    a blanket fort, a shadow theatre and a grandfather clock.
 *  - **Tårnet** (5.5 to 7.5): the round brass tower with the telescope, a star map, an armillary sphere, a weather
 *    vane, a barometer and an owl hole.
 *  - **Det hemmelege rommet** (7.5 to 9): reached from the library bookshelf; the treasure map table, a chest of coins,
 *    a globe and the family tree.
 *
 * The rules (Sture's hide-and-seek, the trunk, the records and everything else that moves) are in [HouseAtticRules].
 * See docs/HUSET.md section 6.3.
 */
object AtticFloor : Floor {
    override val place = PlaceId.MANOR_ATTIC

    override val rooms = listOf(0f..3f, 3f..5.5f, 5.5f..7.5f, 7.5f..9f)

    override val darkness = 0.6f

    override val passages = listOf(
        Passage("attic-stairs-down", place, AtticIds.STAIRS, PassageKind.STAIRS, PlaceId.MANOR_UPPER, "attic-stairs"),
        Passage("attic-secret-down", place, AtticIds.SECRET_DOOR, PassageKind.SECRET, PlaceId.MANOR_GROUND, "library-secret", locked = "manor_bookshelf"),
    )

    override val arrivals = listOf(
        Arrival("stairs", 1.3f),
        Arrival("secret-room", 8.0f),
    )

    override fun hangouts(night: Boolean) = listOf(
        Arrival("trunks", 1.7f),
        Arrival("nook", 4.0f),
        Arrival("tower", 6.2f),
        Arrival("treasure", 8.1f),
    )

    override fun specOf(type: FixtureType): FixtureSpec? = when (type) {
        // Storage. Everything that can hide Sture has a container (the sheet lifts, the lid opens) and one hidden spot.
        FixtureType.AT_SHEETED -> FixtureSpec(
            0.22f, 0.30f,
            container = RRect(-0.08f, -0.26f, 0.08f, -0.02f),
            surfaces = listOf(SurfaceSpec(-0.07f, 0.07f, -0.30f)),
            spots = listOf(SpotSpec(0f, -0.04f, Pose.SIT, hidden = true)),
        )
        FixtureType.AT_TRUNK -> FixtureSpec(
            0.30f, 0.19f,
            container = RRect(-0.12f, -0.16f, 0.12f, -0.02f),
            surfaces = listOf(SurfaceSpec(-0.11f, 0.11f, -0.02f, interior = true), SurfaceSpec(-0.13f, 0.13f, -0.19f, closedOnly = true)),
            spots = listOf(SpotSpec(0f, -0.03f, Pose.SIT, hidden = true)),
        )
        FixtureType.AT_ROCKING_HORSE -> FixtureSpec(
            0.30f, 0.28f,
            spots = listOf(SpotSpec(-0.01f, -0.16f, Pose.SIT)),
            dropZone = RRect(0.03f, -0.28f, 0.17f, -0.12f),
        )
        FixtureType.AT_SPIDER -> FixtureSpec(0.34f, 0.34f, wall = true)
        FixtureType.AT_CARTON -> FixtureSpec(
            0.27f, 0.25f,
            container = RRect(-0.1f, -0.21f, 0.1f, -0.02f),
            surfaces = listOf(SurfaceSpec(-0.1f, 0.1f, -0.02f, interior = true), SurfaceSpec(-0.12f, 0.12f, -0.25f, closedOnly = true)),
            spots = listOf(SpotSpec(0f, -0.03f, Pose.SIT, hidden = true)),
        )
        FixtureType.AT_LANTERN -> FixtureSpec(0.10f, 0.20f, wall = true, light = RRect(-0.38f, -0.5f, 0.38f, 0.14f))
        FixtureType.AT_ROUND_WINDOW -> FixtureSpec(0.26f, 0.26f, wall = true)

        // The ghost's nook.
        FixtureType.AT_WING_CHAIR -> FixtureSpec(
            0.27f, 0.31f,
            surfaces = listOf(SurfaceSpec(-0.07f, 0.07f, -0.11f)),
            spots = listOf(SpotSpec(0f, -0.11f, Pose.SIT)),
        )
        FixtureType.AT_GRAMOPHONE -> FixtureSpec(
            0.22f, 0.27f,
            surfaces = listOf(SurfaceSpec(-0.09f, 0.09f, -0.13f)),
            dropZone = RRect(-0.1f, -0.22f, 0.1f, -0.1f),
        )
        FixtureType.AT_BOOK_TOWER -> FixtureSpec(0.13f, 0.34f)
        FixtureType.AT_CANDELABRA -> FixtureSpec(0.12f, 0.32f, light = RRect(-0.34f, -0.62f, 0.34f, 0.04f))
        FixtureType.AT_STRING_LIGHTS -> FixtureSpec(0.64f, 0.14f, wall = true, light = RRect(-0.3f, -0.3f, 0.3f, 0.1f))
        FixtureType.AT_BLANKET_FORT -> FixtureSpec(
            0.40f, 0.27f,
            container = RRect(-0.14f, -0.22f, 0.14f, -0.02f),
            surfaces = listOf(SurfaceSpec(-0.13f, 0.13f, -0.02f, interior = true), SurfaceSpec(-0.14f, 0.14f, -0.27f, closedOnly = true)),
            spots = listOf(SpotSpec(-0.06f, -0.03f, Pose.SIT, hidden = true), SpotSpec(0.06f, -0.03f, Pose.SIT, hidden = true)),
        )
        FixtureType.AT_SHADOW_THEATRE -> FixtureSpec(0.32f, 0.27f, wall = true, light = RRect(-0.34f, -0.34f, 0.34f, 0.1f))
        FixtureType.AT_GRANDFATHER -> FixtureSpec(0.15f, 0.47f, surfaces = listOf(SurfaceSpec(-0.06f, 0.06f, -0.47f)))

        // The tower.
        FixtureType.AT_STAR_MAP -> FixtureSpec(0.36f, 0.32f, wall = true)
        FixtureType.AT_WEATHER_VANE -> FixtureSpec(0.22f, 0.30f, wall = true)
        FixtureType.AT_ARMILLARY -> FixtureSpec(0.20f, 0.31f, light = RRect(-0.3f, -0.5f, 0.3f, -0.02f))
        FixtureType.AT_BAROMETER -> FixtureSpec(0.22f, 0.22f, wall = true)
        FixtureType.AT_OWL_HOLE -> FixtureSpec(0.20f, 0.21f, wall = true)

        // The secret room.
        FixtureType.AT_MAP_TABLE -> FixtureSpec(0.42f, 0.17f, surfaces = listOf(SurfaceSpec(-0.18f, 0.18f, -0.17f)))
        FixtureType.AT_TREASURE_CHEST -> FixtureSpec(
            0.30f, 0.21f,
            container = RRect(-0.12f, -0.17f, 0.12f, -0.02f),
            surfaces = listOf(SurfaceSpec(-0.11f, 0.11f, -0.02f, interior = true), SurfaceSpec(-0.13f, 0.13f, -0.21f, closedOnly = true)),
        )
        FixtureType.AT_GLOBE -> FixtureSpec(0.20f, 0.31f)
        FixtureType.AT_FAMILY_TREE -> FixtureSpec(0.56f, 0.42f, wall = true)
        FixtureType.AT_CHANDELIER -> FixtureSpec(0.34f, 0.22f, wall = true, light = RRect(-0.5f, -0.4f, 0.5f, 0.4f))
        FixtureType.AT_SCONCE -> FixtureSpec(0.08f, 0.15f, wall = true, light = RRect(-0.3f, -0.5f, 0.3f, 0.15f))
        else -> null
    }

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        // Furniture on the wall: [y] is the bottom edge.
        fun w(type: FixtureType, x: Float, y: Float, variant: Int = 0) = FixtureDef(type, x, y, variant, 0f)
        fun t(type: ThingType, x: Float, y: Float, variant: Int = 0, on: Int = -1) = ThingDef(type, x, y, variant, on)
        val fixtures = listOf(
            // ---- Lageret (0 to 3)
            f(FixtureType.STAIRCASE, 0.56f, depth = -0.06f),                  // 0 down to the first floor
            w(FixtureType.AT_SPIDER, 0.30f, 0.34f, 0),                         // 1
            f(FixtureType.AT_CARTON, 1.08f, depth = -0.07f),                   // 2
            f(FixtureType.AT_TRUNK, 1.85f, depth = -0.05f),                    // 3 the costume trunk
            f(FixtureType.AT_SHEETED, 1.38f, 0, depth = 0.05f),                // 4
            f(FixtureType.AT_ROCKING_HORSE, 2.38f, depth = 0.05f),             // 5
            f(FixtureType.AT_SHEETED, 2.78f, 1, depth = -0.05f),               // 6
            w(FixtureType.AT_LANTERN, 1.58f, 0.34f),                           // 7
            w(FixtureType.AT_ROUND_WINDOW, 2.15f, 0.46f),                      // 8
            w(FixtureType.SHELF, 1.0f, 0.46f),                                 // 9 old toys
            w(FixtureType.AT_SPIDER, 2.88f, 0.30f, 1),                         // 10
            // ---- Spøkelsekroken (3 to 5.5)
            f(FixtureType.AT_GRANDFATHER, 3.18f, depth = -0.06f),              // 11
            f(FixtureType.AT_WING_CHAIR, 3.78f, depth = 0.02f),                // 12 Sture's chair
            f(FixtureType.AT_GRAMOPHONE, 4.28f, depth = 0.04f),                // 13
            f(FixtureType.AT_BOOK_TOWER, 4.64f, depth = 0.06f),                // 14
            f(FixtureType.AT_BLANKET_FORT, 5.02f, depth = -0.03f),             // 15
            f(FixtureType.AT_CANDELABRA, 3.46f, depth = 0.06f),                // 16
            w(FixtureType.AT_SHADOW_THEATRE, 4.52f, 0.50f),                    // 17
            w(FixtureType.AT_STRING_LIGHTS, 4.05f, 0.17f),                     // 18
            f(FixtureType.AT_SHEETED, 5.4f, 2, depth = 0.05f),                 // 19
            // ---- Tårnet (5.5 to 7.5)
            w(FixtureType.AT_STAR_MAP, 5.9f, 0.5f),                            // 20
            f(FixtureType.STOOL, 6.18f, depth = 0.04f),                        // 21
            f(FixtureType.TELESCOPE, 6.55f, depth = -0.03f),                   // 22 opens the telescope screen
            w(FixtureType.AT_WEATHER_VANE, 6.5f, 0.3f),                        // 23
            f(FixtureType.AT_ARMILLARY, 7.05f, depth = 0.04f),                 // 24
            w(FixtureType.AT_BAROMETER, 7.3f, 0.48f),                          // 25
            w(FixtureType.AT_OWL_HOLE, 6.95f, 0.24f),                          // 26
            // ---- Det hemmelege rommet (7.5 to 9)
            f(FixtureType.SECRET_DOOR, 8.8f, 1, depth = -0.08f),               // 27 the way back down to the library
            f(FixtureType.AT_MAP_TABLE, 7.78f, depth = 0.04f),                 // 28
            f(FixtureType.AT_GLOBE, 8.14f, depth = -0.05f),                    // 29
            f(FixtureType.AT_TREASURE_CHEST, 8.45f, depth = 0.07f),            // 30
            w(FixtureType.AT_FAMILY_TREE, 8.12f, 0.64f),                       // 31
            w(FixtureType.AT_CHANDELIER, 8.15f, 0.2f),                         // 32
            w(FixtureType.AT_SCONCE, 7.62f, 0.5f),                             // 33
            w(FixtureType.AT_SCONCE, 8.6f, 0.5f),                              // 34
        )
        val things = listOf(
            t(ThingType.TEDDY, 0.95f, 0.38f, on = AtticIds.SHELF),
            t(ThingType.DUCK, 1.07f, 0.38f, on = AtticIds.SHELF),
            t(ThingType.AT_FLASHLIGHT, 1.24f, 0.9f),
            t(ThingType.TOY_CAR, 1.62f, 0.94f, 1),
            t(ThingType.BEANIE, 1.38f, 0.6f, 1, on = AtticIds.SHEET_A),
            t(ThingType.BALL, 2.72f, 0.93f),
            t(ThingType.STAR_JAR, 3.18f, 0.33f, on = AtticIds.CLOCK),
            t(ThingType.PILLOW, 3.74f, 0.78f, 2, on = AtticIds.WING_CHAIR),
            t(ThingType.AT_RECORD, 4.1f, 0.95f, 0),
            t(ThingType.BOOK, 4.42f, 0.96f, 1),
            t(ThingType.BOOK, 4.88f, 0.95f, 2),
            t(ThingType.AT_RECORD, 5.4f, 0.6f, 3, on = AtticIds.SHEET_C),
            t(ThingType.BINOCULARS, 6.18f, 0.78f, on = AtticIds.STOOL),
            t(ThingType.CANDLE, 7.7f, 0.72f, on = AtticIds.MAP_TABLE),
            t(ThingType.COIN, 7.86f, 0.72f, on = AtticIds.MAP_TABLE),
            t(ThingType.COIN, 7.94f, 0.72f, on = AtticIds.MAP_TABLE),
            t(ThingType.GEM, 8.45f, 0.72f, 2, on = AtticIds.CHEST),
        )
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = fixtures,
            things = things,
            people = listOf(
                PersonDef(Species.GHOST, Look(skin = 9), 4.0f, name = "Sture"),
                PersonDef(Species.CAT, Look(skin = 1), 3.3f),
            ),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = HouseAtticRules(sim, random)
}

/** Blueprint indices of the attic's furniture; the glimt in Secrets.kt and the rules refer to them. */
object AtticIds {
    const val STAIRS = 0
    const val SPIDER_LEFT = 1
    const val CARTON = 2
    const val TRUNK = 3
    const val SHEET_A = 4
    const val HORSE = 5
    const val SHEET_B = 6
    const val LANTERN = 7
    const val WINDOW = 8
    const val SHELF = 9
    const val SPIDER_RIGHT = 10
    const val CLOCK = 11
    const val WING_CHAIR = 12
    const val GRAMOPHONE = 13
    const val BOOK_TOWER = 14
    const val FORT = 15
    const val CANDELABRA = 16
    const val THEATRE = 17
    const val STRING_LIGHTS = 18
    const val SHEET_C = 19
    const val STAR_MAP = 20
    const val STOOL = 21
    const val TELESCOPE = 22
    const val VANE = 23
    const val ARMILLARY = 24
    const val BAROMETER = 25
    const val OWL_HOLE = 26
    const val SECRET_DOOR = 27
    const val MAP_TABLE = 28
    const val GLOBE = 29
    const val CHEST = 30
    const val FAMILY_TREE = 31
    const val CHANDELIER = 32
    const val SCONCE_LEFT = 33
    const val SCONCE_RIGHT = 34

    /** The silly records, by variant: what is on them (the art and the tunes in `AtticFx` follow this order). */
    const val RECORDS = 6
}

/**
 * The codes of the attic in [HouseFx] (the attic owns 300 to 399); the arg of each is described here and played by
 * `AtticFx` in ui/play/HouseAtticFx.kt.
 */
object AtticCode {
    /** Sture slips away into a hiding place; arg = blueprint index of the place. */
    const val HIDE = HouseFx.ATTIC + 0

    /** A giggle and a wobble from the hiding place; arg = index. */
    const val TELL = HouseFx.ATTIC + 1

    /** Found him; arg = how many times so far. */
    const val CAUGHT = HouseFx.ATTIC + 2
    const val STICKER = HouseFx.ATTIC + 3
    const val SNEEZE = HouseFx.ATTIC + 4

    /** Sture tickles somebody; arg = the person's id. */
    const val TICKLE = HouseFx.ATTIC + 5
    const val BOO = HouseFx.ATTIC + 6

    /** Two things swap places; x, y and thing are the first, arg is the id of the other. */
    const val SWAP = HouseFx.ATTIC + 7
    const val SCARED = HouseFx.ATTIC + 8
    const val KEY = HouseFx.ATTIC + 9

    /** Sture is back in the attic (he drifted to another floor). */
    const val RETURN = HouseFx.ATTIC + 10

    /** A sheet, lid or flap was lifted; arg 0 nothing under it, 1 something rolled out. */
    const val SHEET = HouseFx.ATTIC + 11

    /** The trunk opens; arg = which costume. */
    const val TRUNK = HouseFx.ATTIC + 12
    const val TRUNK_SHUT = HouseFx.ATTIC + 13

    /** The rocking horse; arg 0 a rock, 1 a gallop, 2 it was fed. */
    const val HORSE = HouseFx.ATTIC + 14

    /** A spider knits; arg = the stage 0 to 3, 4 a new sweater is tossed down. */
    const val SPIDER = HouseFx.ATTIC + 15
    const val CARTON = HouseFx.ATTIC + 16

    /** The window; arg 1 open, 0 shut. */
    const val WINDOW = HouseFx.ATTIC + 17

    /** A light; arg 1 on, 0 off. */
    const val LIGHT = HouseFx.ATTIC + 18

    /** The gramophone starts a record; arg = the record. */
    const val RECORD = HouseFx.ATTIC + 19
    const val RECORD_STOP = HouseFx.ATTIC + 20
    const val BOOKS = HouseFx.ATTIC + 21
    const val FORT = HouseFx.ATTIC + 22

    /** The shadow theatre; arg = which shadow animal. */
    const val SHADOWS = HouseFx.ATTIC + 23

    /** The grandfather clock strikes; arg = how many strokes. */
    const val CLOCK = HouseFx.ATTIC + 24

    /** The star map lights a constellation; arg = which (0 to 4); STARS_DONE when all are lit. */
    const val STARS = HouseFx.ATTIC + 25
    const val STARS_DONE = HouseFx.ATTIC + 26
    const val VANE = HouseFx.ATTIC + 27
    const val ARMILLARY = HouseFx.ATTIC + 28

    /** The barometer; arg = what it shows (0 rain, 1 fair, 2 rainbow). */
    const val BAROMETER = HouseFx.ATTIC + 29
    const val OWL = HouseFx.ATTIC + 30

    /** The treasure map; arg = how far the dotted path has come (1 to 4), 5 when the star is reached. */
    const val MAP = HouseFx.ATTIC + 31

    /** The chest; arg 0 opens with a shower of coins, 1 shuts. */
    const val CHEST = HouseFx.ATTIC + 32
    const val GLOBE = HouseFx.ATTIC + 33

    /** A portrait on the family tree winks; arg = which (0 to 5); TREE_DONE when they all have. */
    const val TREE = HouseFx.ATTIC + 34
    const val TREE_DONE = HouseFx.ATTIC + 35
    const val CHANDELIER = HouseFx.ATTIC + 36
    const val TELESCOPE = HouseFx.ATTIC + 37

    /** The stuck record: the needle skips. */
    const val SKIP = HouseFx.ATTIC + 38

    /** The secret door was tried; arg 1 when it is open. */
    const val DOOR = HouseFx.ATTIC + 39

    /** The wing chair is plumped. */
    const val CHAIR = HouseFx.ATTIC + 40

    /** Sture blows out a flame; arg = blueprint index of the light. */
    const val BLOW = HouseFx.ATTIC + 41

    /** The shy twinkle at Sture's belly while the key is still in his sheet. */
    const val KEY_HINT = HouseFx.ATTIC + 42

    /** The book tower; arg 0 teeters, 1 re-stacked. */
    const val BOOKS_BACK = HouseFx.ATTIC + 43

    /** A flash of fun in the dark: everything the shadow theatre scares. */
    const val SHAKE_SHEETS = HouseFx.ATTIC + 44
}

/** What the trunk tosses out: a few pieces of a costume at a time, set after set. */
object AtticCostumes {
    class Piece(val type: ThingType, val variant: Int = 0)

    val sets: List<List<Piece>> = listOf(
        // Pirate: a hat, an eye patch and a red striped sweater.
        listOf(Piece(ThingType.AT_PIRATE_HAT), Piece(ThingType.AT_EYE_PATCH), Piece(ThingType.GARMENT, Garment.pack(3, 0))),
        // Knight: a helmet and grey overalls.
        listOf(Piece(ThingType.AT_KNIGHT_HELMET), Piece(ThingType.GARMENT, Garment.pack(4, 10))),
        // Princess: a crown and a pink dress.
        listOf(Piece(ThingType.CROWN, 0), Piece(ThingType.GARMENT, Garment.pack(2, 8))),
        // Giant flower: huge petals and a green dress.
        listOf(Piece(ThingType.AT_FLOWER_HAT), Piece(ThingType.GARMENT, Garment.pack(2, 3))),
        // Ghost: a sheet over the head and funny glasses.
        listOf(Piece(ThingType.AT_SHEET_HAT), Piece(ThingType.AT_FUNNY_GLASSES)),
        // Wizard: a pointy hat and a purple jumper.
        listOf(Piece(ThingType.WIZARD_HAT), Piece(ThingType.GARMENT, Garment.pack(5, 7))),
    )

    /** Every type the trunk can toss, for keeping the floor from filling up. */
    val types: Set<ThingType> = sets.flatten().map { it.type }.toSet()
}
