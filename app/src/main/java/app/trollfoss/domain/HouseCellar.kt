package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, the cellar ("Kjellaren"): a workshop, a laundry, a boiler room, a pool with a sauna, a party
 * room and, in the far corner, the secret mine-cart tunnel to Trollhola. Ten units of floor with about fifty
 * pieces of furniture, dim all over except where lamps and neon are on.
 *
 * Rooms, left to right:
 *  - workshop 0 to 2.0: Rolf's charging station, the stairs, a mouse hole, crates, the workbench, a saw horse
 *  - laundry 2.0 to 3.5: washer, dryer, ironing board, the basket under the chute and the sock monster
 *  - boiler room 3.5 to 4.7: the big boiler, a furnace and three valves
 *  - pool and sauna 4.7 to 6.9: sauna, shower, springboard, rubber duck and a slide
 *  - party room 6.9 to 10: dance floor, jukebox, karaoke, snack bar, and the mine cart at the tunnel door
 *
 * The rules live in [HouseCellarRules]; the art in `ui/art/HouseCellar*Art.kt`.
 */
object CellarFloor : Floor {
    override val place = PlaceId.MANOR_CELLAR

    override val rooms = listOf(0f..2.0f, 2.0f..3.5f, 3.5f..4.7f, 4.7f..6.9f, 6.9f..10f)

    override val darkness = 0.45f

    override val passages = listOf(
        Passage("cellar-stairs-up", place, CellarIx.STAIRS, PassageKind.STAIRS, PlaceId.MANOR_GROUND, "cellar-door"),
        // Open both ways: the mine cart takes you to Trollhola, the mine door there takes you back.
        Passage("cellar-tunnel", place, CellarIx.TUNNEL_DOOR, PassageKind.SECRET, PlaceId.LAB, "tunnel-end", oneWay = false, locked = HouseKeys.TUNNEL),
        Passage("lab-tunnel", PlaceId.LAB, CellarIx.LAB_DOOR, PassageKind.SECRET, place, "tunnel"),
    )

    override val arrivals = listOf(
        Arrival("stairs-top", 1.3f),
        // High up under the ceiling, so whoever comes down the chute falls into the basket.
        Arrival("laundry-chute-end", 2.98f, 0.3f),
        Arrival("tunnel", 9.2f),
    )

    override fun hangouts(night: Boolean) = listOf(
        Arrival("workshop", 1.3f), Arrival("laundry", 2.6f), Arrival("boiler", 4.05f), Arrival("pool", 5.2f), Arrival("party", 8.2f),
    )

    override fun specOf(type: FixtureType): FixtureSpec? = when (type) {
        // Workshop.
        FixtureType.CE_CHARGER -> FixtureSpec(0.17f, 0.36f, spots = listOf(SpotSpec(0f, -0.012f, Pose.STAND)))
        FixtureType.CE_BULB -> FixtureSpec(0.09f, 0.15f, wall = true, light = RRect(-0.34f, -0.3f, 0.34f, 0.38f))
        FixtureType.CE_CRATES -> FixtureSpec(0.21f, 0.19f, surfaces = listOf(SurfaceSpec(-0.09f, 0.09f, -0.19f)))
        FixtureType.CE_MOUSE_HOLE -> FixtureSpec(0.08f, 0.08f, wall = true, dropZone = RRect(-0.06f, -0.1f, 0.06f, 0.03f))
        FixtureType.CE_SAW -> FixtureSpec(0.30f, 0.18f, surfaces = listOf(SurfaceSpec(-0.1f, 0.1f, -0.15f)))

        // Laundry. The washer and the dryer take up to three things each (they count as blenders for the pictures).
        FixtureType.CE_WASHER, FixtureType.CE_DRYER -> FixtureSpec(
            0.21f, 0.25f,
            surfaces = listOf(SurfaceSpec(-0.095f, 0.095f, -0.25f)),
            machine = Machine.BLENDER,
            dropZone = RRect(-0.075f, -0.2f, 0.075f, -0.05f),
        )
        FixtureType.CE_SOCK_MONSTER -> FixtureSpec(0.25f, 0.27f, machine = Machine.TRASH, dropZone = RRect(-0.09f, -0.2f, 0.09f, -0.05f))
        // A soft heap of washing: whoever drops in from the chute bounces a little.
        FixtureType.CE_BASKET -> FixtureSpec(0.34f, 0.17f, front = true, surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.15f, bounce = 0.55f)))
        FixtureType.CE_CLOTHESLINE -> FixtureSpec(0.62f, 0.2f, wall = true)
        FixtureType.CE_IRON_BOARD -> FixtureSpec(0.30f, 0.15f, surfaces = listOf(SurfaceSpec(-0.13f, 0.13f, -0.14f)))
        FixtureType.CE_CHUTE -> FixtureSpec(0.17f, 0.26f, wall = true)

        // Boiler room.
        FixtureType.CE_BOILER -> FixtureSpec(0.34f, 0.54f, light = RRect(-0.34f, -0.55f, 0.34f, 0.12f))
        FixtureType.CE_VALVE -> FixtureSpec(0.14f, 0.30f, wall = true)

        // Pool and sauna. The pool itself is water in the blueprint; CE_POOL_WATER is only there to be tapped.
        FixtureType.CE_DIVING_BOARD -> FixtureSpec(0.42f, 0.14f, surfaces = listOf(SurfaceSpec(-0.09f, 0.2f, -0.125f)))
        FixtureType.CE_POOL_SLIDE -> FixtureSpec(0.36f, 0.45f, spots = listOf(SpotSpec(0.11f, -0.4f, Pose.SIT)))
        FixtureType.CE_POOL_FLOAT -> FixtureSpec(
            0.30f, 0.13f, front = true, floats = true,
            spots = listOf(SpotSpec(-0.07f, -0.075f, Pose.SIT), SpotSpec(0.075f, -0.075f, Pose.SIT)),
        )
        FixtureType.CE_POOL_WATER -> FixtureSpec(CellarFloor.POOL_X2 - CellarFloor.POOL_X1, 0.17f, wall = true)
        FixtureType.CE_SAUNA_BUCKET -> FixtureSpec(0.10f, 0.09f)
        FixtureType.CE_SHOWER -> FixtureSpec(0.13f, 0.46f, wall = true)
        FixtureType.CE_LIFEBUOY -> FixtureSpec(0.13f, 0.13f, wall = true)

        // Party room.
        FixtureType.CE_DANCE_FLOOR -> FixtureSpec(1.0f, 0.14f, wall = true, light = RRect(-0.55f, -0.4f, 0.55f, 0.1f))
        FixtureType.CE_JUKEBOX -> FixtureSpec(0.20f, 0.36f, light = RRect(-0.3f, -0.5f, 0.3f, 0.12f))
        FixtureType.CE_KARAOKE -> FixtureSpec(0.32f, 0.2f, wall = true, light = RRect(-0.28f, -0.3f, 0.28f, 0.2f))
        FixtureType.CE_SNACK_BAR -> FixtureSpec(0.46f, 0.30f, surfaces = listOf(SurfaceSpec(-0.21f, 0.21f, -0.3f)), light = RRect(-0.4f, -0.45f, 0.4f, 0.12f))
        FixtureType.CE_BAR_STOOL -> FixtureSpec(0.09f, 0.14f, surfaces = listOf(SurfaceSpec(-0.04f, 0.04f, -0.14f)), spots = listOf(SpotSpec(0f, -0.14f, Pose.SIT)))
        FixtureType.CE_NEON -> FixtureSpec(0.13f, 0.15f, wall = true, light = RRect(-0.3f, -0.4f, 0.3f, 0.25f))
        FixtureType.CE_CONFETTI -> FixtureSpec(0.11f, 0.18f)

        // The tunnel.
        FixtureType.CE_MINE_CART -> FixtureSpec(
            0.30f, 0.14f, front = true,
            spots = listOf(SpotSpec(-0.07f, -0.07f, Pose.SIT), SpotSpec(0.075f, -0.07f, Pose.SIT)),
        )
        else -> null
    }

    /** The pool: water between these x, with its surface at [POOL_LINE] and its tiled bed at [POOL_BED]. */
    const val POOL_X1 = 5.45f
    const val POOL_X2 = 6.68f
    const val POOL_LINE = 0.8f
    const val POOL_BED = 0.97f

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        /** A piece of furniture on the floor band; [depth] is how far in front of (+) or behind (-) the usual line. */
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        /** Something on the wall (or in the water): [y] is its bottom edge. */
        fun w(type: FixtureType, x: Float, y: Float, variant: Int = 0) = FixtureDef(type, x, y, variant)
        fun t(type: ThingType, x: Float, y: Float, variant: Int = 0, on: Int = -1) = ThingDef(type, x, y, variant, on)
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, POOL_X1, fl), Ground(POOL_X1, POOL_X2, POOL_BED), Ground(POOL_X2, place.width, fl)),
            water = Water(POOL_X1, POOL_X2, POOL_LINE, POOL_BED),
            fixtures = listOf(
                f(FixtureType.STAIRCASE, 0.74f, depth = -0.07f),                  // 0 up to the hall
                f(FixtureType.SECRET_DOOR, 9.84f, 2, depth = -0.08f),             // 1 the tunnel
                // Workshop.
                f(FixtureType.CE_CHARGER, 0.17f, depth = -0.08f),                 // 2 Rolf's charging station
                w(FixtureType.CE_MOUSE_HOLE, 1.27f, 0.78f),                       // 3
                f(FixtureType.CE_CRATES, 1.42f, depth = -0.07f),                  // 4
                f(FixtureType.WORKBENCH, 1.68f, depth = -0.06f),                  // 5
                w(FixtureType.TOOL_WALL, 1.68f, 0.5f),                            // 6
                f(FixtureType.CE_SAW, 1.93f, depth = 0.05f),                      // 7
                w(FixtureType.CE_BULB, 0.52f, 0.2f),                              // 8
                w(FixtureType.CE_BULB, 1.55f, 0.2f),                              // 9
                // Laundry.
                f(FixtureType.CE_WASHER, 2.16f, depth = -0.08f),                  // 10
                f(FixtureType.CE_DRYER, 2.4f, depth = -0.08f),                    // 11
                f(FixtureType.CE_IRON_BOARD, 2.68f, depth = 0.03f),               // 12
                f(FixtureType.CE_BASKET, 2.98f, depth = -0.02f),                  // 13 catches the chute
                w(FixtureType.CE_CHUTE, 2.98f, 0.36f),                            // 14
                f(FixtureType.CE_SOCK_MONSTER, 3.31f, depth = -0.04f),            // 15
                w(FixtureType.CE_CLOTHESLINE, 2.66f, 0.3f),                       // 16
                w(FixtureType.CE_BULB, 3.2f, 0.2f),                               // 17
                // Boiler room.
                w(FixtureType.CE_VALVE, 3.62f, 0.62f, 0),                         // 18 the first valve
                f(FixtureType.CE_BOILER, 4.02f, depth = -0.08f),                  // 19
                f(FixtureType.WOOD_STOVE, 4.4f, depth = -0.05f),                  // 20 the furnace
                w(FixtureType.CE_VALVE, 4.4f, 0.36f, 1),                          // 21 the second valve
                w(FixtureType.CE_VALVE, 4.62f, 0.64f, 2),                         // 22 the third valve
                // Pool and sauna.
                f(FixtureType.SAUNA, 4.93f, depth = -0.07f),                      // 23
                f(FixtureType.CE_SAUNA_BUCKET, 5.2f, depth = 0.05f),              // 24
                w(FixtureType.CE_SHOWER, 5.33f, 0.8f),                            // 25
                w(FixtureType.CE_LIFEBUOY, 5.95f, 0.34f),                         // 26
                w(FixtureType.CE_POOL_WATER, (POOL_X1 + POOL_X2) / 2f, POOL_BED), // 27 tap the water
                f(FixtureType.CE_DIVING_BOARD, 5.62f, depth = -0.04f),            // 28
                w(FixtureType.CE_POOL_FLOAT, 6.18f, 0.865f),                      // 29 a rubber duck to ride
                f(FixtureType.CE_POOL_SLIDE, 6.78f, depth = -0.06f),              // 30
                w(FixtureType.CE_BULB, 5.5f, 0.2f),                               // 31
                // Party room.
                f(FixtureType.BEANBAG, 7.12f, 0, depth = 0.06f),                  // 32
                f(FixtureType.BEANBAG, 7.34f, 1, depth = 0.07f),                  // 33
                f(FixtureType.CE_JUKEBOX, 7.62f, depth = -0.08f),                 // 34
                w(FixtureType.CE_KARAOKE, 7.62f, 0.34f),                          // 35
                w(FixtureType.CE_DANCE_FLOOR, 8.45f, 0.96f),                      // 36 lights up under dancing feet
                w(FixtureType.DISCO_BALL, 8.45f, 0.22f),                          // 37
                f(FixtureType.SPEAKER, 7.98f, depth = -0.09f),                    // 38
                f(FixtureType.SPEAKER, 8.9f, 1, depth = -0.09f),                  // 39
                f(FixtureType.MIC_STAND, 8.12f, depth = 0.03f),                   // 40
                f(FixtureType.CE_SNACK_BAR, 9.22f, depth = -0.07f),               // 41
                f(FixtureType.CE_BAR_STOOL, 9.06f, depth = 0.04f),                // 42
                f(FixtureType.CE_BAR_STOOL, 9.34f, 1, depth = 0.04f),             // 43
                w(FixtureType.CE_NEON, 7.1f, 0.4f, 0),                            // 44 a star
                w(FixtureType.CE_NEON, 9.22f, 0.38f, 1),                          // 45 a music note
                f(FixtureType.CE_CONFETTI, 7.8f, depth = 0.07f),                  // 46
                f(FixtureType.CE_MINE_CART, 9.58f, depth = 0.03f),                // 47 waits at the tunnel
            ),
            things = listOf(
                // Workshop: planks and a hammer on the bench, cheese for the mouse and a candle on the crates.
                t(ThingType.PLANK, 1.62f, 0.55f, on = CellarIx.WORKBENCH),
                t(ThingType.HAMMER, 1.76f, 0.55f, on = CellarIx.WORKBENCH),
                t(ThingType.BROWN_CHEESE, 1.45f, 0.58f, on = CellarIx.CRATES),
                t(ThingType.CANDLE, 1.38f, 0.58f, on = CellarIx.CRATES),
                t(ThingType.WRENCH, 2.05f, 0.95f),
                // Laundry: odd socks everywhere.
                t(ThingType.CE_SOCK, 2.92f, 0.6f, 0, on = CellarIx.BASKET),
                t(ThingType.CE_SOCK, 3.05f, 0.6f, 1, on = CellarIx.BASKET),
                t(ThingType.CE_SOCK, 2.62f, 0.72f, 2, on = CellarIx.IRON_BOARD),
                t(ThingType.CE_SOCK, 2.5f, 0.94f, 3),
                // Pool: things that float.
                t(ThingType.SWIM_RING, 5.75f, 0.9f),
                t(ThingType.DUCK, 5.98f, 0.9f),
                t(ThingType.BEACH_BALL, 6.4f, 0.9f),
                // Party: cake, hats and fizz on the bar, balloons under the ceiling, a guitar.
                t(ThingType.CAKE, 9.18f, 0.5f, on = CellarIx.SNACK_BAR),
                t(ThingType.PARTY_HAT, 9.31f, 0.5f, 2, on = CellarIx.SNACK_BAR),
                t(ThingType.PARTY_HAT, 9.38f, 0.5f, 1, on = CellarIx.SNACK_BAR),
                t(ThingType.SODA, 9.08f, 0.5f, 1, on = CellarIx.SNACK_BAR),
                t(ThingType.BALLOON, 7.25f, 0.5f, 1),
                t(ThingType.BALLOON, 8.9f, 0.5f, 3),
                t(ThingType.BALLOON, 9.7f, 0.5f, 5),
                t(ThingType.GUITAR, 8.55f, 0.95f),
            ),
            // A cat naps by the warm boiler.
            people = listOf(PersonDef(Species.CAT, Look(skin = 2), 4.62f, y = 0.94f)),
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = HouseCellarRules(sim, random)
}

/** Where each piece of the cellar stands in its blueprint (the index is the fixture's id minus the place's id base). */
object CellarIx {
    const val STAIRS = 0
    const val TUNNEL_DOOR = 1
    const val CHARGER = 2
    const val MOUSE_HOLE = 3
    const val CRATES = 4
    const val WORKBENCH = 5
    const val TOOL_WALL = 6
    const val SAW = 7
    const val WASHER = 10
    const val DRYER = 11
    const val IRON_BOARD = 12
    const val BASKET = 13
    const val CHUTE = 14
    const val SOCK_MONSTER = 15
    const val CLOTHESLINE = 16
    val VALVES = intArrayOf(18, 21, 22)
    const val BOILER = 19
    const val FURNACE = 20
    const val SAUNA = 23
    const val SAUNA_BUCKET = 24
    const val SHOWER = 25
    const val LIFEBUOY = 26
    const val POOL_WATER = 27
    const val DIVING_BOARD = 28
    const val POOL_FLOAT = 29
    const val POOL_SLIDE = 30
    const val JUKEBOX = 34
    const val KARAOKE = 35
    const val DANCE_FLOOR = 36
    const val DISCO_BALL = 37
    val SPEAKERS = intArrayOf(38, 39)
    const val MIC_STAND = 40
    const val SNACK_BAR = 41
    const val CONFETTI = 46
    const val MINE_CART = 47

    /** The mine door on Trollhola's side: the last fixture of the lab's blueprint. */
    const val LAB_DOOR = 10
}
