package app.trollfoss.domain

/**
 * What each piece of furniture of Storstova is: how big it is, what rests on it, who sits on it, what opens,
 * what it takes. The art (ui/art/HouseGround*Art.kt) draws to these numbers, so a cup stands on the mantel
 * and a figure sits on the cushion. All offsets are from the fixture's bottom centre; up is negative.
 */
internal object GroundSpecs {
    private fun seat(dx: Float, dy: Float) = SpotSpec(dx, dy, Pose.SIT)

    /** Shelf boards of the tall bookcases, from the floor up. */
    val SHELF_BOARDS = floatArrayOf(-0.04f, -0.18f, -0.32f, -0.46f, -0.60f, -0.74f)

    fun of(type: FixtureType): FixtureSpec? = when (type) {
        // ---- Hall
        FixtureType.GR_CLOCK -> FixtureSpec(0.13f, 0.46f, surfaces = listOf(SurfaceSpec(-0.05f, 0.05f, -0.46f)))
        FixtureType.GR_COAT_RACK -> FixtureSpec(0.30f, 0.20f, wall = true)
        FixtureType.GR_UMBRELLA_STAND -> FixtureSpec(0.10f, 0.15f, dropZone = RRect(-0.05f, -0.2f, 0.05f, -0.08f))
        FixtureType.GR_WINDOW -> FixtureSpec(0.20f, 0.40f, wall = true)
        FixtureType.GR_ARMOUR -> FixtureSpec(0.15f, 0.40f)
        FixtureType.GR_CHANDELIER -> FixtureSpec(0.34f, 0.30f, wall = true, light = RRect(-0.45f, -0.5f, 0.45f, 0.4f))
        FixtureType.GR_POST_SLOT -> FixtureSpec(0.10f, 0.10f, wall = true, dropZone = RRect(-0.05f, -0.1f, 0.05f, 0f))
        FixtureType.GR_PORTRAIT -> FixtureSpec(0.16f, 0.20f, wall = true)

        // ---- Living room
        FixtureType.GR_FIREPLACE -> FixtureSpec(
            0.52f, 0.52f,
            surfaces = listOf(SurfaceSpec(-0.24f, 0.24f, -0.43f), SurfaceSpec(-0.09f, 0.09f, -0.065f)),
            light = RRect(-0.55f, -0.7f, 0.55f, 0.1f),
        )
        FixtureType.GR_WINGCHAIR -> FixtureSpec(0.20f, 0.26f, surfaces = listOf(SurfaceSpec(-0.05f, 0.05f, -0.09f)), spots = listOf(seat(0f, -0.09f)))
        FixtureType.GR_SOFA -> FixtureSpec(
            0.50f, 0.24f,
            surfaces = listOf(SurfaceSpec(-0.2f, 0.2f, -0.085f)),
            spots = listOf(seat(-0.15f, -0.085f), seat(0f, -0.085f), seat(0.15f, -0.085f)),
        )
        FixtureType.GR_COFFEE_TABLE -> FixtureSpec(0.26f, 0.08f, surfaces = listOf(SurfaceSpec(-0.12f, 0.12f, -0.08f)))
        FixtureType.GR_POPCORN_BOWL -> FixtureSpec(0.08f, 0.06f)
        FixtureType.GR_GLOBE -> FixtureSpec(0.12f, 0.22f)
        FixtureType.GR_TV -> FixtureSpec(0.30f, 0.34f, light = RRect(-0.3f, -0.45f, 0.3f, 0f))
        FixtureType.GR_AQUARIUM -> FixtureSpec(0.46f, 0.22f, wall = true, dropZone = RRect(-0.2f, -0.24f, 0.2f, -0.08f))
        FixtureType.GR_FLOOR_LAMP -> FixtureSpec(0.10f, 0.40f, light = RRect(-0.45f, -0.7f, 0.45f, 0.05f))

        // ---- Library
        FixtureType.GR_BOOKSHELF -> FixtureSpec(0.32f, 0.74f, surfaces = SHELF_BOARDS.map { SurfaceSpec(-0.14f, 0.14f, it) })
        FixtureType.GR_LADDER -> FixtureSpec(0.12f, 0.66f, spots = listOf(SpotSpec(0f, -0.44f, Pose.SIT)))
        FixtureType.GR_DESK -> FixtureSpec(0.36f, 0.16f, surfaces = listOf(SurfaceSpec(-0.17f, 0.17f, -0.16f)), light = RRect(-0.35f, -0.5f, 0.35f, 0f))
        FixtureType.GR_LECTERN -> FixtureSpec(0.12f, 0.24f)
        FixtureType.GR_BUST -> FixtureSpec(0.10f, 0.20f, surfaces = listOf(SurfaceSpec(-0.04f, 0.04f, -0.2f)))

        // ---- Dining room
        FixtureType.GR_DINING_TABLE -> FixtureSpec(1.04f, 0.13f, surfaces = listOf(SurfaceSpec(-0.5f, 0.5f, -0.13f)))
        FixtureType.GR_CHAIR_ROW -> FixtureSpec(
            0.74f, 0.26f,
            spots = listOf(seat(-0.27f, -0.085f), seat(-0.09f, -0.085f), seat(0.09f, -0.085f), seat(0.27f, -0.085f)),
        )
        FixtureType.GR_DINING_CHAIR -> FixtureSpec(0.11f, 0.24f, surfaces = listOf(SurfaceSpec(-0.04f, 0.04f, -0.085f)), spots = listOf(seat(0f, -0.085f)))
        FixtureType.GR_BELL -> FixtureSpec(0.06f, 0.06f)
        FixtureType.GR_CAKE -> FixtureSpec(0.13f, 0.13f, light = RRect(-0.2f, -0.3f, 0.2f, 0.05f))
        FixtureType.GR_CANDELABRA -> FixtureSpec(0.10f, 0.34f, light = RRect(-0.4f, -0.6f, 0.4f, 0.05f))
        FixtureType.GR_SIDEBOARD -> FixtureSpec(
            0.36f, 0.26f,
            container = RRect(-0.15f, -0.2f, 0.15f, -0.03f),
            surfaces = listOf(
                SurfaceSpec(-0.15f, 0.15f, -0.03f, interior = true),
                SurfaceSpec(-0.15f, 0.15f, -0.115f, interior = true),
                SurfaceSpec(-0.17f, 0.17f, -0.26f),
            ),
        )

        // ---- Kitchen
        FixtureType.GR_RANGE -> FixtureSpec(
            0.34f, 0.32f,
            container = RRect(-0.12f, -0.14f, 0.12f, -0.025f),
            surfaces = listOf(SurfaceSpec(-0.12f, 0.12f, -0.03f, interior = true), SurfaceSpec(-0.14f, 0.14f, -0.3f)),
        )
        FixtureType.GR_SINK -> FixtureSpec(0.30f, 0.22f, surfaces = listOf(SurfaceSpec(0.0f, 0.14f, -0.22f)))
        FixtureType.GR_MIXER -> FixtureSpec(0.12f, 0.18f, machine = Machine.BLENDER, dropZone = RRect(-0.06f, -0.2f, 0.06f, -0.06f))
        FixtureType.GR_PIZZA_OVEN -> FixtureSpec(
            0.38f, 0.34f,
            surfaces = listOf(SurfaceSpec(-0.1f, 0.1f, -0.34f)),
            dropZone = RRect(-0.1f, -0.2f, 0.1f, -0.05f),
            light = RRect(-0.35f, -0.5f, 0.35f, 0.05f),
        )
        FixtureType.GR_FRIDGE -> FixtureSpec(
            0.26f, 0.46f,
            container = RRect(-0.1f, -0.42f, 0.1f, -0.03f),
            surfaces = listOf(
                SurfaceSpec(-0.1f, 0.1f, -0.03f, interior = true),
                SurfaceSpec(-0.1f, 0.1f, -0.15f, interior = true),
                SurfaceSpec(-0.1f, 0.1f, -0.27f, interior = true),
                SurfaceSpec(-0.1f, 0.1f, -0.39f, interior = true),
                SurfaceSpec(-0.12f, 0.12f, -0.46f),
            ),
        )
        FixtureType.GR_JAM_CABINET -> FixtureSpec(
            0.26f, 0.22f, wall = true,
            container = RRect(-0.11f, -0.19f, 0.11f, -0.025f),
            surfaces = listOf(SurfaceSpec(-0.11f, 0.11f, -0.025f, interior = true), SurfaceSpec(-0.11f, 0.11f, -0.11f, interior = true)),
        )
        FixtureType.GR_ISLAND -> FixtureSpec(0.70f, 0.20f, surfaces = listOf(SurfaceSpec(-0.33f, 0.33f, -0.2f)))
        FixtureType.GR_STOOLS -> FixtureSpec(0.50f, 0.22f, spots = listOf(seat(-0.17f, -0.10f), seat(0f, -0.10f), seat(0.17f, -0.10f)))

        // ---- Winter garden
        FixtureType.GR_PLANT -> FixtureSpec(0.16f, 0.36f, dropZone = RRect(-0.12f, -0.42f, 0.12f, 0f))
        FixtureType.GR_FOUNTAIN -> FixtureSpec(
            0.42f, 0.34f,
            pool = RRect(-0.17f, -0.11f, 0.17f, -0.04f),
            dropZone = RRect(-0.17f, -0.3f, 0.17f, -0.06f),
        )
        FixtureType.GR_SOFIE -> FixtureSpec(0.22f, 0.34f, dropZone = RRect(-0.12f, -0.36f, 0.12f, -0.12f))
        FixtureType.GR_HAMMOCK -> FixtureSpec(0.50f, 0.22f, front = true, spots = listOf(SpotSpec(0f, -0.075f, Pose.LIE)))
        else -> null
    }
}
