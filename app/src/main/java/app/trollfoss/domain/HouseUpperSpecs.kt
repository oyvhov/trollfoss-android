package app.trollfoss.domain

/**
 * The shapes of the upper floor's own furniture: how big it is, what things can rest on it, where figures sit
 * or stand, what opens, and what a thing dropped on it does. Sizes are in scene units (a grown-up is 0.30).
 * The art in `ui/art/HouseUpper*Art.kt` draws to exactly these numbers.
 */
internal object UpperSpecs {
    private fun seat(dx: Float, dy: Float) = SpotSpec(dx, dy, Pose.SIT)

    /** A hold on the climbing wall: whoever is put there hangs by their hands from it. */
    private fun hold(dx: Float, dy: Float) = SpotSpec(dx, dy, Pose.HELD)

    fun of(type: FixtureType): FixtureSpec? = when (type) {
        // Landing and gallery
        FixtureType.UP_PORTRAIT -> FixtureSpec(0.15f, 0.19f, wall = true)
        FixtureType.UP_WINDOW -> FixtureSpec(0.30f, 0.36f, wall = true)
        FixtureType.UP_WINDOW_SEAT -> FixtureSpec(
            0.50f, 0.13f,
            surfaces = listOf(SurfaceSpec(-0.22f, 0.22f, -0.10f)),
            spots = listOf(seat(-0.11f, -0.10f), seat(0.12f, -0.10f)),
        )

        // Children's room
        FixtureType.UP_TOY_TRAIN -> FixtureSpec(0.80f, 0.12f, spots = listOf(seat(0f, -0.05f), seat(0f, -0.05f)))
        FixtureType.UP_BLOCKS -> FixtureSpec(
            0.14f, 0.30f,
            // The top of a standing tower can carry a teddy; a fallen one is a heap.
            surfaces = listOf(SurfaceSpec(-0.05f, 0.05f, -0.30f, closedOnly = true)),
            dropZone = RRect(-0.09f, -0.33f, 0.09f, -0.02f),
        )
        FixtureType.UP_DOLLHOUSE -> FixtureSpec(0.52f, 0.60f)
        FixtureType.UP_PUPPET_THEATER -> FixtureSpec(0.34f, 0.46f)
        FixtureType.UP_NIGHT_LAMP -> FixtureSpec(0.10f, 0.26f, light = RRect(-0.35f, -0.5f, 0.35f, 0.1f))
        FixtureType.UP_POSTER -> FixtureSpec(0.16f, 0.20f, wall = true)
        FixtureType.UP_MOBILE -> FixtureSpec(0.22f, 0.30f, wall = true)
        FixtureType.UP_FORT -> FixtureSpec(
            0.44f, 0.32f,
            container = RRect(-0.14f, -0.24f, 0.14f, -0.02f),
            surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.02f, interior = true)),
            spots = listOf(SpotSpec(0f, -0.03f, Pose.LIE, hidden = true)),
        )

        // Playroom
        FixtureType.UP_BALL_PIT -> FixtureSpec(
            0.64f, 0.20f, front = true,
            surfaces = listOf(SurfaceSpec(-0.28f, 0.28f, -0.115f)),
            spots = listOf(seat(-0.19f, -0.09f), seat(0f, -0.09f), seat(0.19f, -0.09f)),
        )
        FixtureType.UP_CLIMBING_WALL -> FixtureSpec(
            0.46f, 0.66f,
            spots = listOf(hold(-0.12f, -0.11f), hold(0.10f, -0.22f), hold(-0.08f, -0.33f), hold(0.09f, -0.43f)),
        )
        FixtureType.UP_TRAMPOLINE -> FixtureSpec(0.44f, 0.11f, surfaces = listOf(SurfaceSpec(-0.2f, 0.2f, -0.085f, bounce = 1f)))
        FixtureType.UP_EASEL -> FixtureSpec(
            0.24f, 0.42f,
            surfaces = listOf(SurfaceSpec(-0.1f, 0.1f, -0.16f)),
            dropZone = RRect(-0.11f, -0.40f, 0.11f, -0.18f),
        )
        FixtureType.UP_KARAOKE -> FixtureSpec(
            0.66f, 0.10f,
            surfaces = listOf(SurfaceSpec(-0.30f, 0.30f, -0.10f)),
            spots = listOf(SpotSpec(0.12f, -0.10f, Pose.STAND)),
            light = RRect(-0.5f, -0.62f, 0.5f, 0.05f),
        )

        // Bathroom
        FixtureType.UP_SHOWER -> FixtureSpec(0.30f, 0.60f, front = true, spots = listOf(SpotSpec(0f, -0.01f, Pose.STAND)))
        FixtureType.UP_BATH_MIRROR -> FixtureSpec(0.20f, 0.26f, wall = true)
        FixtureType.UP_TOWELS -> FixtureSpec(0.26f, 0.18f, wall = true)

        // Bedroom
        FixtureType.UP_WARDROBE -> FixtureSpec(
            0.34f, 0.48f,
            container = RRect(-0.14f, -0.44f, 0.14f, -0.02f),
            surfaces = listOf(
                SurfaceSpec(-0.13f, 0.13f, -0.02f, interior = true),
                SurfaceSpec(-0.13f, 0.13f, -0.24f, interior = true),
                SurfaceSpec(-0.15f, 0.15f, -0.48f),
            ),
        )
        FixtureType.UP_VANITY -> FixtureSpec(0.34f, 0.40f, surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.20f)), light = RRect(-0.28f, -0.52f, 0.28f, 0.05f))
        FixtureType.UP_JEWEL_BOX -> FixtureSpec(0.12f, 0.09f)
        FixtureType.UP_ROCKING_CHAIR -> FixtureSpec(
            0.20f, 0.26f,
            surfaces = listOf(SurfaceSpec(-0.05f, 0.05f, -0.09f)),
            spots = listOf(seat(0f, -0.09f)),
        )

        // Balcony
        FixtureType.UP_BIRD_FEEDER -> FixtureSpec(0.16f, 0.26f, wall = true, dropZone = RRect(-0.09f, -0.28f, 0.09f, 0.02f))
        FixtureType.UP_HANGING_CHAIR -> FixtureSpec(0.26f, 0.46f, surfaces = listOf(SurfaceSpec(-0.06f, 0.06f, -0.15f)), spots = listOf(seat(0f, -0.15f)))
        FixtureType.UP_RAILING -> FixtureSpec(1.0f, 0.15f, surfaces = listOf(SurfaceSpec(-0.48f, 0.48f, -0.14f)))
        else -> null
    }
}
