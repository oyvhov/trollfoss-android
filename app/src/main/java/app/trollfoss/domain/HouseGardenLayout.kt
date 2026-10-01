package app.trollfoss.domain

/**
 * Where everything stands in Hagen, the garden of Storhuset. The blueprint, the rules and the art all read
 * these numbers, so what is drawn is always where it works: the chute ends over the pond, the zip line
 * reaches its pole, the gnome on the shed roof sits on the shed roof.
 *
 * Scene units; x runs from the back door (left) to the treehouse (right), y is the screen (0 top, 1 bottom).
 */
object GardenLayout {
    // ---- the pond: real water. Things float, figures wade, the balcony slide ends in it.
    const val POND_X1 = 4.3f
    const val POND_X2 = 6.2f
    const val POND_LINE = 0.82f
    const val POND_BED = 0.95f

    // ---- the back of the big house, drawn by the background
    const val HOUSE_X0 = -0.45f
    const val HOUSE_X1 = 3.3f

    /** The balcony slide: from the balcony's end out over the lawn, ending just above the pond. */
    const val SLIDE_X0 = 3.26f
    const val SLIDE_Y0 = 0.31f
    const val SLIDE_X1 = 4.52f
    const val SLIDE_Y1 = 0.72f

    // ---- the treehouse tree and what hangs on it
    const val TREE_X = 8.38f

    /** The deck of the treehouse above the tree's foot, and how far it reaches left and right of the trunk. */
    const val DECK_DY = -0.36f
    const val DECK_X0 = -0.5f
    const val DECK_X1 = 0.36f

    // ---- the zip line: from the mast on the deck's left end down to the pole by the pond
    const val ZIP_X0 = 7.9f
    const val ZIP_Y0 = 0.09f
    const val ZIP_X1 = 6.3f
    const val ZIP_Y1 = 0.34f

    /** How far below the cable a rider's hips hang. */
    const val ZIP_HANG = 0.36f

    // ---- the lawn robot's beat, and the garden floor
    const val MOW_X0 = 1.6f
    const val MOW_X1 = 4.1f
    const val FLOOR = 0.88f

    /** The back row, middle row and front row of the garden floor (depth shifts). */
    const val BACK = -0.08f
    const val FRONT_ROW = 0.07f

    const val GROW_SECONDS = 7f
    const val ZIP_SECONDS = 3.1f
    const val CLIMB_SECONDS = 1.5f
}

/** Blueprint indices of Hagen's fixtures (`HouseGarden.kt` builds the list in this order; a test checks it). */
object GardenIx {
    const val DOOR = 0
    const val GREENHOUSE = 1
    const val PLANTER0 = 2
    const val PLANTER1 = 3
    const val PLANTER2 = 4
    const val BIRDHOUSE = 5
    const val COMPOST = 6
    const val BARREL = 7
    const val PATIO = 8
    const val BED0 = 9
    const val PINWHEEL0 = 10
    const val SHED = 11
    const val BED1 = 12
    const val PINWHEEL1 = 13
    const val GRILL = 14
    const val TRAMPOLINE = 15
    const val BRIDGE = 16
    const val FROG0 = 17
    const val FROGS = 5
    const val ZIP_POLE = 22
    const val SANDBOX = 23
    const val HAMMOCK = 24
    const val LADDER = 25
    const val TREEHOUSE = 26
    const val SWING = 27
    const val ZIP = 28
    const val GATE = 29
    const val MOWER = 30
    const val SPRINKLER = 31
    const val GNOME0 = 32
    const val GNOMES = 5
    const val SNOWMAN = 37
    const val LAMP0 = 38
    const val LAMP1 = 39
    const val COUNT = 40
}

/** What a gnome is up to in a spot: the art draws each differently. */
enum class GnomeKind { STAND, SIT, FISH, DIG, NAP }

/**
 * A place a garden gnome can be found. A spot on something ([host] is a blueprint index) moves with it;
 * a spot on the floor is the point itself. [x] and [y] are relative to the host, or absolute without one.
 */
class GnomeSpot(val host: Int, val x: Float, val y: Float, val kind: GnomeKind)

/** The spots the gnomes sneak between. They shift while nobody is looking (see the garden rules). */
object GardenGnomes {
    val spots: List<GnomeSpot> = listOf(
        GnomeSpot(-1, 1.52f, 0.955f, GnomeKind.STAND),                 // 0 on the door step
        GnomeSpot(GardenIx.SHED, 0.04f, -0.47f, GnomeKind.SIT),        // 1 on the shed roof
        GnomeSpot(GardenIx.PATIO, 0.0f, -0.13f, GnomeKind.SIT),        // 2 on the patio table
        GnomeSpot(-1, 2.76f, 0.965f, GnomeKind.STAND),                 // 3 in front of the shed
        GnomeSpot(-1, 3.78f, 0.96f, GnomeKind.STAND),                  // 4 by the grill
        GnomeSpot(-1, 4.22f, 0.925f, GnomeKind.FISH),                  // 5 at the pond's edge, fishing
        GnomeSpot(GardenIx.BRIDGE, 0.12f, -0.1f, GnomeKind.FISH),      // 6 on the bridge, fishing
        GnomeSpot(GardenIx.SANDBOX, 0.14f, -0.055f, GnomeKind.DIG),    // 7 digging in the sandbox
        GnomeSpot(-1, 7.28f, 0.955f, GnomeKind.STAND),                 // 8 in front of the hammock
        GnomeSpot(GardenIx.TREEHOUSE, -0.32f, GardenLayout.DECK_DY, GnomeKind.SIT), // 9 on the treehouse deck
        GnomeSpot(-1, 8.02f, 0.95f, GnomeKind.STAND),                  // 10 under the tree
        GnomeSpot(GardenIx.GREENHOUSE, 0.4f, -0.02f, GnomeKind.STAND), // 11 behind the plants in the greenhouse
        GnomeSpot(-1, 4.16f, 0.975f, GnomeKind.STAND),                 // 12 on the bank, with the watering can
        GnomeSpot(-1, 8.86f, 0.93f, GnomeKind.STAND),                  // 13 at the far edge
        GnomeSpot(GardenIx.COMPOST, 0.0f, -0.17f, GnomeKind.SIT),      // 14 on the compost heap
        GnomeSpot(GardenIx.HAMMOCK, 0.27f, -0.19f, GnomeKind.NAP),     // 15 asleep in the hammock
        GnomeSpot(GardenIx.BARREL, 0.0f, -0.2f, GnomeKind.SIT),        // 16 on the rain barrel
    )

    /** The spot each gnome starts in, by its number (0 to 4). */
    val homes = intArrayOf(0, 14, 5, 10, 11)

    /** The spot a gnome is in now: the home of its number until its first move, then [Fixture.mode] minus one. */
    fun slotOf(f: Fixture): Int {
        if (f.mode > 0) return (f.mode - 1).coerceIn(0, spots.size - 1)
        val n = (f.place.indexOf(f.id) - GardenIx.GNOME0).coerceIn(0, homes.size - 1)
        return homes[n]
    }

    fun kindOf(f: Fixture): GnomeKind = spots[slotOf(f)].kind

    /** True when the gnome looks to the left (it changes every time it has been moved). */
    fun facesLeft(f: Fixture): Boolean = f.count and 1 == 1
}
