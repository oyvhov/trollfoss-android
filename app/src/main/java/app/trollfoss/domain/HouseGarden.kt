package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, the garden ("Hagen"): the back door of the house with its terrace, a greenhouse where plants
 * grow into giant vegetables you can sit in, a pond with a choir of frogs where the balcony slide ends with
 * a splash, a treehouse with a rope ladder, a tyre swing and a zip line, a shed, a sandbox, a trampoline, a
 * grill, a hammock, a bird house, a compost heap that hides the golden key, and five garden gnomes who
 * sneak about when nobody is looking. The rules are in [GardenRules] (`HouseGardenRules.kt`), the places
 * everything stands in [GardenLayout]. The passage id and the arrival names are the house's contract
 * (docs/HUSET.md section 3).
 */
object GardenFloor : Floor {
    override val place = PlaceId.MANOR_GARDEN

    /** The terrace and greenhouse, the pond, and the play corner with the tree. */
    override val rooms = listOf(0f..3.4f, 3.4f..6.3f, 6.3f..place.width)

    override val passages = listOf(
        Passage("garden-house-door", place, GardenIx.DOOR, PassageKind.DOOR, PlaceId.MANOR_GROUND, "garden-door"),
    )

    override val arrivals = listOf(
        Arrival("house-door", 1.42f),
        // The end of the balcony slide: a little above the pond, so whoever comes down lands in it with a splash.
        Arrival("slide-end", GardenLayout.SLIDE_X1, 0.64f),
    )

    override fun hangouts(night: Boolean): List<Arrival> = if (night) {
        listOf(Arrival("terrace", 1.85f), Arrival("hammock", 7.3f), Arrival("grill", 3.5f), Arrival("treehouse", 8.0f))
    } else {
        listOf(
            Arrival("terrace", 1.9f), Arrival("greenhouse", 1.0f), Arrival("trampoline", 4.05f), Arrival("pond", 4.25f),
            Arrival("sandbox", 6.65f), Arrival("hammock", 7.3f), Arrival("treehouse", 8.05f),
        )
    }

    override fun specOf(type: FixtureType): FixtureSpec? = when (type) {
        FixtureType.GA_GREENHOUSE -> FixtureSpec(
            0.88f, 0.56f,
            surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.57f)),
            light = RRect(-0.45f, -0.5f, 0.45f, 0.1f),
        )
        // The three beds. When a vegetable has grown giant, `open` is true and it has a seat.
        FixtureType.GA_PLANTER -> FixtureSpec(
            0.2f, 0.2f, front = true,
            container = RRect(-0.08f, -0.2f, 0.08f, -0.04f),
            spots = listOf(SpotSpec(0f, -0.09f, Pose.SIT, hidden = true)),
            machine = Machine.GARDEN,
            dropZone = RRect(-0.11f, -0.24f, 0.11f, 0.02f),
        )
        FixtureType.GA_FROG -> FixtureSpec(0.1f, 0.07f, surfaces = listOf(SurfaceSpec(-0.045f, 0.045f, -0.022f)))
        FixtureType.GA_BRIDGE -> FixtureSpec(0.72f, 0.16f, surfaces = listOf(SurfaceSpec(-0.31f, 0.31f, -0.1f)))
        FixtureType.GA_SHED -> FixtureSpec(
            0.58f, 0.46f,
            container = RRect(-0.22f, -0.38f, 0.08f, -0.02f),
            surfaces = listOf(
                SurfaceSpec(-0.2f, 0.06f, -0.03f, interior = true),
                SurfaceSpec(-0.2f, 0.06f, -0.2f, interior = true),
                SurfaceSpec(-0.2f, 0.2f, -0.47f),
            ),
        )
        FixtureType.GA_GRILL -> FixtureSpec(
            0.22f, 0.26f,
            surfaces = listOf(SurfaceSpec(-0.075f, 0.075f, -0.17f)),
            machine = Machine.CAMPFIRE,
            light = RRect(-0.35f, -0.5f, 0.35f, 0.1f),
        )
        // A table with a parasol and two chairs.
        FixtureType.GA_PATIO -> FixtureSpec(
            0.54f, 0.42f,
            surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.13f)),
            spots = listOf(SpotSpec(-0.215f, -0.085f, Pose.SIT), SpotSpec(0.215f, -0.085f, Pose.SIT)),
        )
        FixtureType.GA_FLOWER_BED -> FixtureSpec(0.34f, 0.12f)
        FixtureType.GA_BIRDHOUSE -> FixtureSpec(0.14f, 0.5f, dropZone = RRect(-0.07f, -0.5f, 0.07f, -0.3f))
        FixtureType.GA_BARREL -> FixtureSpec(0.14f, 0.2f, surfaces = listOf(SurfaceSpec(-0.055f, 0.055f, -0.2f)))
        FixtureType.GA_COMPOST -> FixtureSpec(
            0.3f, 0.17f,
            surfaces = listOf(SurfaceSpec(-0.08f, 0.08f, -0.17f)),
            dropZone = RRect(-0.16f, -0.26f, 0.16f, 0.02f),
        )
        FixtureType.GA_HAMMOCK -> FixtureSpec(0.74f, 0.34f, spots = listOf(SpotSpec(0f, -0.15f, Pose.LIE)))
        FixtureType.GA_GNOME -> FixtureSpec(0.1f, 0.16f)
        FixtureType.GA_MOWER -> FixtureSpec(0.16f, 0.09f)
        FixtureType.GA_SPRINKLER -> FixtureSpec(0.1f, 0.06f)
        FixtureType.GA_GATE -> FixtureSpec(0.34f, 0.3f, wall = true)
        FixtureType.GA_SNOWMAN -> FixtureSpec(0.16f, 0.26f)
        FixtureType.GA_PINWHEEL -> FixtureSpec(0.09f, 0.22f)
        FixtureType.GA_SANDBOX -> FixtureSpec(
            0.52f, 0.1f,
            surfaces = listOf(SurfaceSpec(-0.21f, 0.21f, -0.055f)),
            spots = listOf(SpotSpec(-0.1f, -0.055f, Pose.SIT), SpotSpec(0.1f, -0.055f, Pose.SIT)),
            dropZone = RRect(-0.24f, -0.2f, 0.24f, 0f),
        )
        FixtureType.GA_TRAMPOLINE -> FixtureSpec(0.42f, 0.12f, surfaces = listOf(SurfaceSpec(-0.18f, 0.18f, -0.09f, bounce = 1f)))
        // The big tree. The deck is a surface; the cabin opens, and two can sit inside.
        FixtureType.GA_TREEHOUSE -> FixtureSpec(
            0.96f, 0.8f,
            container = RRect(0.03f, -0.53f, 0.31f, -0.38f),
            surfaces = listOf(SurfaceSpec(GardenLayout.DECK_X0, GardenLayout.DECK_X1, GardenLayout.DECK_DY)),
            spots = listOf(SpotSpec(0.12f, -0.38f, Pose.SIT, hidden = true), SpotSpec(0.25f, -0.38f, Pose.SIT, hidden = true)),
            light = RRect(-0.05f, -0.65f, 0.42f, -0.28f),
        )
        // Two lanes: climbing up and climbing down. Nobody can be dropped on them; the rules move the climbers.
        FixtureType.GA_LADDER -> FixtureSpec(
            0.1f, 0.42f,
            spots = listOf(SpotSpec(0f, 0f, Pose.STAND, hidden = true), SpotSpec(0f, 0f, Pose.STAND, hidden = true)),
        )
        FixtureType.GA_SWING -> FixtureSpec(0.22f, 0.62f, spots = listOf(SpotSpec(0f, -0.14f, Pose.SIT)))
        // The zip line's carriage: the fixture's bottom is the rider's hips, the trolley is above.
        FixtureType.GA_ZIP -> FixtureSpec(0.14f, 0.4f, spots = listOf(SpotSpec(0f, 0f, Pose.SIT)))
        FixtureType.GA_ZIP_POLE -> FixtureSpec(0.1f, 0.5f)
        else -> null
    }

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        val defs = ArrayList<FixtureDef>()
        fun add(type: FixtureType, x: Float, depth: Float = 0f, variant: Int = 0, on: Int = -1): Int {
            defs += FixtureDef(type, x, fl + depth, variant, depth, on)
            return defs.size - 1
        }
        // ---- the house end: door, greenhouse with its three beds, the gardener's corner, the terrace
        add(FixtureType.DOOR, 1.3f, -0.09f, 1)                                   // 0 the back door of the house
        add(FixtureType.GA_GREENHOUSE, 0.46f, -0.05f)                            // 1
        add(FixtureType.GA_PLANTER, 0.22f, -0.03f, 0)                            // 2 pumpkin
        add(FixtureType.GA_PLANTER, 0.46f, -0.03f, 1)                            // 3 tomato
        add(FixtureType.GA_PLANTER, 0.7f, -0.03f, 2)                             // 4 pea pod
        add(FixtureType.GA_BIRDHOUSE, 0.98f, -0.04f)                             // 5
        add(FixtureType.GA_COMPOST, 0.98f, GardenLayout.FRONT_ROW + 0.01f)       // 6
        add(FixtureType.GA_BARREL, 1.62f, -0.04f)                                // 7
        add(FixtureType.GA_PATIO, 2.05f)                                         // 8
        val bed0 = add(FixtureType.GA_FLOWER_BED, 2.56f, GardenLayout.FRONT_ROW, 0) // 9 tulips
        add(FixtureType.GA_PINWHEEL, 2.68f, 0f, 0, on = bed0)                    // 10
        add(FixtureType.GA_SHED, 3.02f, -0.07f)                                  // 11
        val bed1 = add(FixtureType.GA_FLOWER_BED, 3.3f, GardenLayout.FRONT_ROW + 0.01f, 1) // 12 daisies
        add(FixtureType.GA_PINWHEEL, 3.4f, 0f, 1, on = bed1)                     // 13
        add(FixtureType.GA_GRILL, 3.66f, 0.02f)                                  // 14
        add(FixtureType.GA_TRAMPOLINE, 4.04f)                                    // 15
        // ---- the pond: a bridge across the back, five frogs on lily pads, each with its own note
        add(FixtureType.GA_BRIDGE, 5.1f, -0.09f)                                 // 16
        val frogX = floatArrayOf(4.9f, 5.12f, 5.34f, 5.56f, 5.78f)
        for (i in 0 until GardenIx.FROGS) defs += FixtureDef(FixtureType.GA_FROG, frogX[i], GardenLayout.POND_LINE + 0.02f, i)  // 17..21
        // ---- the play corner: zip line pole, sandbox, hammock, rope ladder, the big tree, tyre swing
        add(FixtureType.GA_ZIP_POLE, 6.3f, -0.05f)                               // 22
        add(FixtureType.GA_SANDBOX, 6.82f, 0.02f)                                // 23
        add(FixtureType.GA_HAMMOCK, 7.42f)                                       // 24
        add(FixtureType.GA_LADDER, GardenLayout.TREE_X - 0.38f, 0.03f)           // 25
        add(FixtureType.GA_TREEHOUSE, GardenLayout.TREE_X, -0.07f)               // 26
        add(FixtureType.GA_SWING, 8.8f, 0.03f)                                   // 27
        defs += FixtureDef(FixtureType.GA_ZIP, GardenLayout.ZIP_X0, GardenLayout.ZIP_Y0 + GardenLayout.ZIP_HANG)   // 28 the carriage, hips at the deck
        defs += FixtureDef(FixtureType.GA_GATE, 6.78f, 0.79f)                    // 29 a gate in the back fence (a wall piece)
        // ---- the lawn robot, the sprinkler, five gnomes, the snowman (it only shows in snow), two lamps
        defs += FixtureDef(FixtureType.GA_MOWER, 3.0f, 0.965f)                   // 30
        defs += FixtureDef(FixtureType.GA_SPRINKLER, 2.32f, 0.965f)              // 31
        check(defs.size == GardenIx.GNOME0)
        for (k in 0 until GardenIx.GNOMES) {
            val spot = GardenGnomes.spots[GardenGnomes.homes[k]]
            val host = if (spot.host >= 0) defs[spot.host] else null
            defs += FixtureDef(FixtureType.GA_GNOME, (host?.x ?: 0f) + spot.x, (host?.y ?: 0f) + spot.y, k)  // 32..36
        }
        defs += FixtureDef(FixtureType.GA_SNOWMAN, 1.8f, 0.965f)                 // 37
        add(FixtureType.LAMP_POST, 2.38f, -0.06f)                                // 38
        add(FixtureType.LAMP_POST, 6.52f, GardenLayout.FRONT_ROW)                // 39
        check(defs.size == GardenIx.COUNT)

        fun thing(type: ThingType, x: Float, y: Float, variant: Int = 0, on: Int = -1) = ThingDef(type, x, y, variant, on)
        val things = listOf(
            // The patio: cake, juice and cups for a garden party.
            thing(ThingType.CAKE, 2.05f, 0.74f, on = GardenIx.PATIO),
            thing(ThingType.JUICE, 2.15f, 0.74f, on = GardenIx.PATIO),
            thing(ThingType.CUP, 1.95f, 0.74f, 1, on = GardenIx.PATIO),
            // The grill, ready for sausages.
            thing(ThingType.SAUSAGE, 3.64f, 0.70f, on = GardenIx.GRILL),
            thing(ThingType.SAUSAGE, 3.69f, 0.70f, on = GardenIx.GRILL),
            // The gardener's corner.
            thing(ThingType.SEEDS, 0.1f, 0.94f),
            thing(ThingType.SEEDS, 0.88f, 0.93f),
            thing(ThingType.WATERING_CAN, 1.2f, 0.95f),
            thing(ThingType.BANANA_PEEL, 1.02f, 0.7f, on = GardenIx.COMPOST),
            // The sandbox.
            thing(ThingType.BUCKET, 6.72f, 0.815f, 1, on = GardenIx.SANDBOX),
            thing(ThingType.SPADE, 6.93f, 0.8f, on = GardenIx.SANDBOX),
            // Toys on the lawn, and ducks on the pond.
            thing(ThingType.BALL, 3.92f, 0.94f),
            thing(ThingType.DUCK, 5.0f, 0.8f),
            thing(ThingType.DUCK, 5.67f, 0.8f),
            // Under the apple tree.
            thing(ThingType.APPLE, 8.05f, 0.95f),
            thing(ThingType.APPLE, 7.9f, 0.94f),
            thing(ThingType.MUSHROOM, 7.72f, 0.95f),
            thing(ThingType.FLOWER, 2.4f, 0.95f, 2),
            thing(ThingType.STICK, 5.0f, 0.955f),
            // Inside the shed: tools on the shelf and a pot on the floor.
            thing(ThingType.HAMMER, 2.9f, 0.67f, on = GardenIx.SHED),
            thing(ThingType.WRENCH, 2.98f, 0.67f, on = GardenIx.SHED),
            thing(ThingType.PLANT_POT, 3.0f, 0.84f, on = GardenIx.SHED),
        )

        val animals = listOf(
            PersonDef(Species.CAT, Look(skin = 1), 2.9f, y = 0.93f),
            PersonDef(Species.BUNNY, Look(skin = 3), 2.35f, y = 0.955f),
            PersonDef(Species.CHICKEN, Look(skin = 0), 7.15f, y = 0.94f),
            PersonDef(Species.CHICKEN, Look(skin = 1), 7.32f, y = 0.955f),
        )
        return PlaceSpec(
            place,
            grounds = listOf(
                Ground(0f, GardenLayout.POND_X1, fl),
                Ground(GardenLayout.POND_X1, GardenLayout.POND_X2, GardenLayout.POND_BED),
                Ground(GardenLayout.POND_X2, place.width, fl),
            ),
            water = Water(GardenLayout.POND_X1, GardenLayout.POND_X2, GardenLayout.POND_LINE, GardenLayout.POND_BED),
            fixtures = defs,
            things = things,
            people = animals,
        )
    }

    override fun rules(sim: Sim, random: Random): FloorRules = GardenRules(sim, random)
}
