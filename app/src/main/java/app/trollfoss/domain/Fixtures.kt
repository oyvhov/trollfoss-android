package app.trollfoss.domain

/** How a figure is posed. Seats decide it; physics decides the rest. */
enum class Pose { STAND, SIT, LIE, HELD, SWIM, FLOAT }

/**
 * A rectangle relative to a fixture's bottom centre, in scene units. Up is negative, so [top] is
 * smaller than [bottom].
 */
data class RRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(dx: Float, dy: Float): Boolean = dx in left..right && dy in top..bottom
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/** A flat top that things can rest on, from [x1] to [x2] at height [dy] above the fixture's bottom. */
data class SurfaceSpec(
    val x1: Float,
    val x2: Float,
    val dy: Float,
    /** Inside a cupboard: only there while the cupboard is open, and hides what rests on it when shut. */
    val interior: Boolean = false,
    /** A lid: only there while the cupboard is shut. */
    val closedOnly: Boolean = false,
    /** Extra spring. 1 is a trampoline. */
    val bounce: Float = 0f,
    /** Ice: things glide on it instead of stopping. */
    val slippery: Boolean = false,
)

/** A place for a figure to sit or lie. [hidden] spots are inside something and vanish when it shuts. */
data class SpotSpec(val dx: Float, val dy: Float, val pose: Pose, val hidden: Boolean = false)

/** What a fixture does to things put into or onto it. */
/** [TARGET]: knocked over by things thrown into its drop zone. [BELT]: a conveyor that scans what rides it. */
enum class Machine { NONE, STOVE, OVEN, BLENDER, CAULDRON, CAMPFIRE, FISHING, FOUNTAIN, TOILET, DISPENSER, BUILD, GARDEN, TARGET, BELT, TRASH }

class FixtureSpec(
    val w: Float,
    val h: Float,
    /** Hangs on the wall: its y is the bottom edge on the wall, and it is drawn behind floor fixtures. */
    val wall: Boolean = false,
    /** Has a front layer (a blanket, a bath side, a boat hull) drawn over whoever is in it. */
    val front: Boolean = false,
    val surfaces: List<SurfaceSpec> = emptyList(),
    val spots: List<SpotSpec> = emptyList(),
    /** The inside of a cupboard. Present means the fixture opens and shuts when tapped. */
    val container: RRect? = null,
    /** Glass fronts show what is inside even when shut. */
    val glass: Boolean = false,
    val machine: Machine = Machine.NONE,
    /** Where a dropped thing is taken by the [machine]. */
    val dropZone: RRect? = null,
    /** Water inside the fixture (a bath, a fountain basin); its top is the waterline. */
    val pool: RRect? = null,
    /** Light source offset and reach, used at night when the fixture is on. */
    val light: RRect? = null,
    /** Bobs on the water, taking its seats with it. */
    val floats: Boolean = false,
)

/**
 * Every piece of furniture and every machine. Positions in place blueprints are the bottom centre;
 * [FixtureSpec] describes the rest relative to that point.
 */
enum class FixtureType {
    // Home
    BED, SOFA, CHAIR, STOOL, TABLE, ROUND_TABLE, COUNTER, SHELF, BOOKCASE, FRIDGE, WARDROBE, CHEST,
    STOVE, SINK, BATH, TOILET, MIRROR, LAMP, TV, RADIO, PIANO, WINDOW, MAILBOX, CLOCK, PLANT_BIG, WOOD_STOVE,

    // Café
    CASH_REGISTER, OVEN, BLENDER, FRUIT_CRATE, ICE_CREAM_MACHINE, DISPLAY_CASE, FLOUR_SACK,

    // Salon
    SALON_CHAIR, DRYER_HOOD, HAIR_WASH, CLOTHES_RACK,

    // Lab
    POTION_RACK, CAULDRON, TELESCOPE, CRYSTAL_BALL, SPELLBOOK,

    // Beach
    PIER, FISHING_SPOT, BOAT, UMBRELLA, LOUNGER, SANDCASTLE,

    // Forest
    TENT, CAMPFIRE, LOG, STUMP, OWL_TREE,

    // Mountain
    PINE_TREE, SAUNA, SLED_HILL, SNOWMAN, SKI_JUMP, ICE_POND, COCOA_STAND, BENCH, LAMP_POST,

    // Farm
    TRACTOR, HAY_BALE, CHICKEN_COOP, VEGETABLE_PATCH, WATER_TROUGH, WORKBENCH, TOOL_WALL, WOOD_PILE, TIRE_STACK,

    // Space station
    ROCKET_SHIP, CONTROL_PANEL, PORTHOLE, GRAVITY_LEVER, ORRERY, SPACE_BED, FOOD_DISPENSER,

    // Tivoli
    FERRIS_WHEEL, CAROUSEL, TRAMPOLINE, CANDY_FLOSS_STAND, POPCORN_CART, CAN_TOSS, BUMPER_CAR,

    // Shop
    SHOP_SHELF, SCALE, FREEZER, SODA_FRIDGE, CHECKOUT, CART,

    // Doctor
    HEIGHT_CHART, XRAY, EXAM_BED, MEDICINE_CABINET, DOCTOR_DESK, EYE_CHART,

    // Stage
    STAGE_PLATFORM, DRUM_KIT, MIC_STAND, XYLOPHONE, SPEAKER, DISCO_BALL, SMOKE_MACHINE,

    // Under water
    SHIPWRECK, KELP, GIANT_CLAM, CORAL, SUBMARINE, OCTOPUS,

    // Heileberget: hut, cable car and stations, rock ledges, echo rock, eagle nest, summit flag
    MOUNTAIN_HUT, CABLE_STATION, CABLE_CAR, ROCK_LEDGE, SUMMIT_ROCK, ECHO_ROCK, EAGLE_NEST, SUMMIT_FLAG,

    // Home designer catalogue, and tidying up
    RUG, PICTURE, AQUARIUM, BEANBAG, ARMCHAIR, BUNK_BED, TOY_BOX, DESK, FLOWER_POT, TRASH_BIN, ROBOT_VACUUM,

    // Storhuset, the big house. Ways between floors (shared; specs in [House]):
    STAIRCASE, LIFT, SLIDE, FIRE_POLE, HATCH, LADDER, SECRET_DOOR, DOOR, DUMBWAITER,

    // Each floor adds its own types below, in its own block. The spec of each goes in the floor's file (see [Floor.specOf]).
    // ---- Storhuset ground floor (HouseGround.kt) ----

    // ---- Storhuset upper floor (HouseUpper.kt) ----
    UP_PORTRAIT, UP_WINDOW, UP_WINDOW_SEAT, UP_TOY_TRAIN, UP_BLOCKS, UP_DOLLHOUSE, UP_PUPPET_THEATER, UP_NIGHT_LAMP,
    UP_POSTER, UP_MOBILE, UP_BALL_PIT, UP_CLIMBING_WALL, UP_TRAMPOLINE, UP_EASEL, UP_KARAOKE, UP_FORT,
    UP_SHOWER, UP_BATH_MIRROR, UP_TOWELS, UP_WARDROBE, UP_VANITY, UP_JEWEL_BOX, UP_ROCKING_CHAIR,
    UP_BIRD_FEEDER, UP_HANGING_CHAIR, UP_RAILING,

    // ---- Storhuset attic (HouseAttic.kt) ----

    // ---- Storhuset cellar (HouseCellar.kt) ----

    // ---- Storhuset garden (HouseGarden.kt) ----
    ;

    val spec: FixtureSpec get() = specs.getValue(this)

    val opens: Boolean get() = spec.container != null

    companion object {
        private val specs: Map<FixtureType, FixtureSpec> by lazy { entries.associateWith(::build) }

        private fun seat(dx: Float, dy: Float) = SpotSpec(dx, dy, Pose.SIT)

        private fun build(type: FixtureType): FixtureSpec = when (type) {
            BED -> FixtureSpec(
                0.40f, 0.17f, front = true,
                surfaces = listOf(SurfaceSpec(-0.17f, 0.17f, -0.105f)),
                spots = listOf(SpotSpec(0.02f, -0.105f, Pose.LIE)),
            )
            // Seats are also surfaces, so a teddy (or a whoopee cushion) can wait on them.
            SOFA -> FixtureSpec(0.40f, 0.17f, surfaces = listOf(SurfaceSpec(-0.16f, 0.16f, -0.075f)), spots = listOf(seat(-0.09f, -0.075f), seat(0.09f, -0.075f)))
            CHAIR -> FixtureSpec(0.10f, 0.20f, surfaces = listOf(SurfaceSpec(-0.04f, 0.04f, -0.085f)), spots = listOf(seat(0f, -0.085f)))
            STOOL -> FixtureSpec(
                0.08f, 0.12f,
                surfaces = listOf(SurfaceSpec(-0.035f, 0.035f, -0.12f)),
                spots = listOf(seat(0f, -0.12f)),
            )
            TABLE -> FixtureSpec(0.30f, 0.13f, surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.13f)))
            ROUND_TABLE -> FixtureSpec(0.16f, 0.13f, surfaces = listOf(SurfaceSpec(-0.08f, 0.08f, -0.13f)))
            COUNTER -> FixtureSpec(0.30f, 0.17f, surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.17f)))
            SHELF -> FixtureSpec(0.26f, 0.03f, wall = true, surfaces = listOf(SurfaceSpec(-0.13f, 0.13f, -0.03f)))
            BOOKCASE -> FixtureSpec(
                0.20f, 0.38f,
                surfaces = listOf(
                    SurfaceSpec(-0.09f, 0.09f, -0.02f),
                    SurfaceSpec(-0.09f, 0.09f, -0.14f),
                    SurfaceSpec(-0.09f, 0.09f, -0.26f),
                    SurfaceSpec(-0.1f, 0.1f, -0.38f),
                ),
            )
            FRIDGE -> FixtureSpec(
                0.17f, 0.36f,
                container = RRect(-0.07f, -0.33f, 0.07f, -0.02f),
                surfaces = listOf(
                    SurfaceSpec(-0.07f, 0.07f, -0.02f, interior = true),
                    SurfaceSpec(-0.07f, 0.07f, -0.12f, interior = true),
                    SurfaceSpec(-0.07f, 0.07f, -0.22f, interior = true),
                    SurfaceSpec(-0.085f, 0.085f, -0.36f),
                ),
            )
            WARDROBE -> FixtureSpec(
                0.24f, 0.38f,
                container = RRect(-0.1f, -0.35f, 0.1f, -0.02f),
                surfaces = listOf(
                    SurfaceSpec(-0.1f, 0.1f, -0.02f, interior = true),
                    SurfaceSpec(-0.1f, 0.1f, -0.19f, interior = true),
                    SurfaceSpec(-0.12f, 0.12f, -0.38f),
                ),
            )
            CHEST -> FixtureSpec(
                0.18f, 0.10f,
                container = RRect(-0.08f, -0.09f, 0.08f, -0.015f),
                surfaces = listOf(
                    SurfaceSpec(-0.075f, 0.075f, -0.015f, interior = true),
                    SurfaceSpec(-0.09f, 0.09f, -0.10f, closedOnly = true),
                ),
            )
            STOVE -> FixtureSpec(
                0.18f, 0.20f,
                surfaces = listOf(SurfaceSpec(-0.08f, 0.08f, -0.20f)),
                machine = Machine.STOVE,
                dropZone = RRect(-0.09f, -0.32f, 0.09f, -0.18f),
            )
            SINK -> FixtureSpec(
                0.18f, 0.20f,
                surfaces = listOf(
                    SurfaceSpec(-0.06f, 0.06f, -0.165f),
                    SurfaceSpec(-0.09f, -0.066f, -0.20f),
                    SurfaceSpec(0.066f, 0.09f, -0.20f),
                ),
            )
            BATH -> FixtureSpec(
                0.40f, 0.15f, front = true,
                surfaces = listOf(SurfaceSpec(-0.16f, 0.16f, -0.035f)),
                spots = listOf(seat(0f, -0.035f)),
                pool = RRect(-0.17f, -0.11f, 0.17f, -0.035f),
            )
            TOILET -> FixtureSpec(
                0.12f, 0.18f,
                spots = listOf(seat(0f, -0.085f)),
                machine = Machine.TOILET,
                dropZone = RRect(-0.05f, -0.16f, 0.05f, -0.07f),
            )
            MIRROR -> FixtureSpec(0.12f, 0.17f, wall = true)
            LAMP -> FixtureSpec(0.10f, 0.34f, light = RRect(-0.35f, -0.65f, 0.35f, 0.05f))
            TV -> FixtureSpec(0.30f, 0.31f, light = RRect(-0.3f, -0.45f, 0.3f, 0f))
            RADIO -> FixtureSpec(0.10f, 0.07f)
            PIANO -> FixtureSpec(
                0.34f, 0.26f,
                surfaces = listOf(SurfaceSpec(-0.17f, 0.17f, -0.26f)),
                dropZone = RRect(-0.15f, -0.165f, 0.15f, -0.12f),
            )
            WINDOW -> FixtureSpec(0.20f, 0.22f, wall = true)
            MAILBOX -> FixtureSpec(0.08f, 0.10f, wall = true)
            CLOCK -> FixtureSpec(0.09f, 0.13f, wall = true)
            PLANT_BIG -> FixtureSpec(0.12f, 0.26f)
            WOOD_STOVE -> FixtureSpec(
                0.17f, 0.32f,
                surfaces = listOf(SurfaceSpec(-0.075f, 0.075f, -0.32f)),
                machine = Machine.CAMPFIRE,
                dropZone = RRect(-0.08f, -0.45f, 0.08f, -0.3f),
                light = RRect(-0.4f, -0.55f, 0.4f, 0.05f),
            )

            CASH_REGISTER -> FixtureSpec(0.10f, 0.08f)
            OVEN -> FixtureSpec(
                0.20f, 0.24f,
                container = RRect(-0.075f, -0.17f, 0.075f, -0.035f),
                surfaces = listOf(
                    SurfaceSpec(-0.075f, 0.075f, -0.035f, interior = true),
                    SurfaceSpec(-0.1f, 0.1f, -0.24f),
                ),
                machine = Machine.OVEN,
            )
            BLENDER -> FixtureSpec(0.07f, 0.15f, machine = Machine.BLENDER, dropZone = RRect(-0.06f, -0.28f, 0.06f, -0.08f))
            FRUIT_CRATE -> FixtureSpec(0.16f, 0.10f, machine = Machine.DISPENSER)
            ICE_CREAM_MACHINE -> FixtureSpec(0.14f, 0.24f, machine = Machine.DISPENSER)
            DISPLAY_CASE -> FixtureSpec(
                0.26f, 0.18f,
                container = RRect(-0.12f, -0.16f, 0.12f, -0.02f),
                glass = true,
                surfaces = listOf(
                    SurfaceSpec(-0.12f, 0.12f, -0.02f, interior = true),
                    SurfaceSpec(-0.12f, 0.12f, -0.09f, interior = true),
                    SurfaceSpec(-0.13f, 0.13f, -0.18f),
                ),
            )
            FLOUR_SACK -> FixtureSpec(0.11f, 0.13f, machine = Machine.DISPENSER)

            SALON_CHAIR -> FixtureSpec(0.14f, 0.20f, surfaces = listOf(SurfaceSpec(-0.05f, 0.05f, -0.10f)), spots = listOf(seat(0f, -0.10f)))
            DRYER_HOOD -> FixtureSpec(0.14f, 0.34f, front = true, spots = listOf(seat(0f, -0.09f)))
            HAIR_WASH -> FixtureSpec(0.18f, 0.19f, spots = listOf(seat(0f, -0.08f)))
            CLOTHES_RACK -> FixtureSpec(0.32f, 0.30f, machine = Machine.DISPENSER)

            POTION_RACK -> FixtureSpec(0.32f, 0.10f, wall = true, machine = Machine.DISPENSER)
            CAULDRON -> FixtureSpec(
                0.22f, 0.17f,
                machine = Machine.CAULDRON,
                dropZone = RRect(-0.11f, -0.32f, 0.11f, -0.1f),
                light = RRect(-0.25f, -0.4f, 0.25f, 0.05f),
            )
            TELESCOPE -> FixtureSpec(0.14f, 0.30f)
            CRYSTAL_BALL -> FixtureSpec(0.10f, 0.13f, light = RRect(-0.15f, -0.25f, 0.15f, 0.05f))
            SPELLBOOK -> FixtureSpec(0.16f, 0.17f)

            PIER -> FixtureSpec(1.0f, 0.30f, surfaces = listOf(SurfaceSpec(-0.5f, 0.5f, -0.26f)))
            FISHING_SPOT -> FixtureSpec(0.10f, 0.10f, machine = Machine.FISHING)
            BOAT -> FixtureSpec(0.36f, 0.13f, front = true, floats = true, spots = listOf(seat(-0.08f, -0.05f), seat(0.08f, -0.05f)))
            UMBRELLA -> FixtureSpec(0.26f, 0.38f)
            LOUNGER -> FixtureSpec(0.30f, 0.10f, spots = listOf(SpotSpec(0.01f, -0.07f, Pose.LIE)))
            SANDCASTLE -> FixtureSpec(0.16f, 0.15f)

            TENT -> FixtureSpec(
                0.34f, 0.25f,
                container = RRect(-0.13f, -0.2f, 0.13f, -0.01f),
                surfaces = listOf(SurfaceSpec(-0.12f, 0.12f, -0.01f, interior = true)),
                spots = listOf(SpotSpec(0f, -0.01f, Pose.LIE, hidden = true)),
            )
            CAMPFIRE -> FixtureSpec(
                0.16f, 0.10f,
                surfaces = listOf(SurfaceSpec(-0.05f, 0.05f, -0.10f)),
                machine = Machine.CAMPFIRE,
                dropZone = RRect(-0.08f, -0.28f, 0.08f, -0.05f),
                light = RRect(-0.5f, -0.6f, 0.5f, 0.1f),
            )
            LOG -> FixtureSpec(0.26f, 0.08f, surfaces = listOf(SurfaceSpec(-0.12f, 0.12f, -0.07f)), spots = listOf(seat(-0.07f, -0.07f), seat(0.07f, -0.07f)))
            STUMP -> FixtureSpec(
                0.09f, 0.08f,
                surfaces = listOf(SurfaceSpec(-0.04f, 0.04f, -0.08f)),
                spots = listOf(seat(0f, -0.08f)),
            )
            OWL_TREE -> FixtureSpec(0.36f, 0.72f)

            PINE_TREE -> FixtureSpec(0.38f, 0.74f)
            SAUNA -> FixtureSpec(
                0.38f, 0.33f,
                container = RRect(-0.15f, -0.25f, 0.15f, -0.02f),
                surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.02f, interior = true)),
                spots = listOf(SpotSpec(-0.075f, -0.075f, Pose.SIT, hidden = true), SpotSpec(0.075f, -0.075f, Pose.SIT, hidden = true)),
                light = RRect(-0.3f, -0.45f, 0.3f, 0.05f),
            )
            SLED_HILL -> FixtureSpec(0.52f, 0.34f, spots = listOf(seat(-0.19f, -0.32f)))
            SNOWMAN -> FixtureSpec(0.16f, 0.24f)
            SKI_JUMP -> FixtureSpec(0.58f, 0.44f, spots = listOf(seat(-0.23f, -0.42f)))
            ICE_POND -> FixtureSpec(
                0.62f, 0.03f,
                surfaces = listOf(SurfaceSpec(-0.31f, 0.31f, -0.006f, slippery = true)),
                machine = Machine.FOUNTAIN,
                dropZone = RRect(-0.06f, -0.14f, 0.06f, 0.01f),
            )
            BENCH -> FixtureSpec(0.32f, 0.12f, surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.07f)), spots = listOf(seat(-0.08f, -0.07f), seat(0.08f, -0.07f)))
            COCOA_STAND -> FixtureSpec(0.22f, 0.34f, surfaces = listOf(SurfaceSpec(-0.1f, 0.1f, -0.15f)), machine = Machine.DISPENSER)
            TRACTOR -> FixtureSpec(0.52f, 0.36f, front = true, spots = listOf(seat(-0.07f, -0.19f)))
            HAY_BALE -> FixtureSpec(
                0.24f, 0.15f,
                surfaces = listOf(SurfaceSpec(-0.11f, 0.11f, -0.15f)),
                spots = listOf(seat(0f, -0.15f)),
            )
            CHICKEN_COOP -> FixtureSpec(0.32f, 0.32f, machine = Machine.DISPENSER)
            VEGETABLE_PATCH -> FixtureSpec(0.44f, 0.06f, machine = Machine.GARDEN, dropZone = RRect(-0.22f, -0.22f, 0.22f, 0.02f))
            WATER_TROUGH -> FixtureSpec(
                0.32f, 0.13f,
                surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.02f)),
                pool = RRect(-0.14f, -0.1f, 0.14f, -0.02f),
            )
            WORKBENCH -> FixtureSpec(
                0.36f, 0.2f,
                surfaces = listOf(SurfaceSpec(-0.18f, 0.18f, -0.2f)),
                machine = Machine.BUILD,
                dropZone = RRect(-0.18f, -0.36f, 0.18f, -0.16f),
            )
            TOOL_WALL -> FixtureSpec(0.44f, 0.22f, wall = true, machine = Machine.DISPENSER)
            WOOD_PILE -> FixtureSpec(0.26f, 0.14f, machine = Machine.DISPENSER)
            TIRE_STACK -> FixtureSpec(0.14f, 0.2f, machine = Machine.DISPENSER)

            ROCKET_SHIP -> FixtureSpec(0.34f, 0.66f, front = true, spots = listOf(seat(0f, -0.4f)), light = RRect(-0.2f, 0f, 0.2f, 0.3f))
            CONTROL_PANEL -> FixtureSpec(0.36f, 0.2f, surfaces = listOf(SurfaceSpec(-0.17f, 0.17f, -0.2f)), light = RRect(-0.25f, -0.35f, 0.25f, 0f))
            PORTHOLE -> FixtureSpec(0.3f, 0.3f, wall = true, light = RRect(-0.25f, -0.4f, 0.25f, 0.05f))
            GRAVITY_LEVER -> FixtureSpec(0.1f, 0.22f)
            ORRERY -> FixtureSpec(0.32f, 0.32f, light = RRect(-0.2f, -0.35f, 0.2f, -0.05f))
            SPACE_BED -> FixtureSpec(0.14f, 0.32f, wall = true, spots = listOf(seat(0f, -0.1f)))
            FOOD_DISPENSER -> FixtureSpec(0.16f, 0.28f, machine = Machine.DISPENSER)
            LAMP_POST -> FixtureSpec(0.08f, 0.50f, light = RRect(-0.4f, -0.8f, 0.4f, 0.05f))

            // Tivoli. The wheel's and the carousel's spots move; see Sim.seatPoint.
            FERRIS_WHEEL -> FixtureSpec(
                0.80f, 0.78f, front = true,
                spots = List(4) { seat(0f, -0.42f) },
                light = RRect(-0.45f, -0.85f, 0.45f, 0f),
            )
            CAROUSEL -> FixtureSpec(0.74f, 0.62f, front = true, spots = List(3) { seat(0f, -0.2f) }, light = RRect(-0.45f, -0.7f, 0.45f, 0f))
            TRAMPOLINE -> FixtureSpec(0.40f, 0.10f, surfaces = listOf(SurfaceSpec(-0.18f, 0.18f, -0.08f, bounce = 1f)))
            CANDY_FLOSS_STAND -> FixtureSpec(0.28f, 0.42f, surfaces = listOf(SurfaceSpec(-0.13f, 0.13f, -0.2f)), machine = Machine.DISPENSER)
            POPCORN_CART -> FixtureSpec(0.24f, 0.40f, machine = Machine.DISPENSER, light = RRect(-0.2f, -0.45f, 0.2f, 0f))
            CAN_TOSS -> FixtureSpec(
                0.36f, 0.50f,
                surfaces = listOf(SurfaceSpec(-0.17f, 0.17f, -0.2f)),
                machine = Machine.TARGET,
                dropZone = RRect(-0.12f, -0.38f, 0.12f, -0.2f),
            )
            BUMPER_CAR -> FixtureSpec(0.22f, 0.13f, front = true, spots = listOf(seat(0f, -0.075f)))

            // Shop
            SHOP_SHELF -> FixtureSpec(
                0.36f, 0.44f,
                surfaces = listOf(
                    SurfaceSpec(-0.16f, 0.16f, -0.03f),
                    SurfaceSpec(-0.16f, 0.16f, -0.17f),
                    SurfaceSpec(-0.16f, 0.16f, -0.31f),
                    SurfaceSpec(-0.18f, 0.18f, -0.44f),
                ),
            )
            SCALE -> FixtureSpec(0.16f, 0.06f, surfaces = listOf(SurfaceSpec(-0.075f, 0.075f, -0.05f)))
            FREEZER -> FixtureSpec(
                0.44f, 0.22f,
                container = RRect(-0.2f, -0.2f, 0.2f, -0.04f),
                glass = true,
                surfaces = listOf(
                    SurfaceSpec(-0.19f, 0.19f, -0.05f, interior = true),
                    SurfaceSpec(-0.21f, 0.21f, -0.22f, closedOnly = true),
                ),
            )
            SODA_FRIDGE -> FixtureSpec(
                0.24f, 0.42f,
                container = RRect(-0.1f, -0.38f, 0.1f, -0.03f),
                glass = true,
                surfaces = listOf(
                    SurfaceSpec(-0.1f, 0.1f, -0.04f, interior = true),
                    SurfaceSpec(-0.1f, 0.1f, -0.16f, interior = true),
                    SurfaceSpec(-0.1f, 0.1f, -0.28f, interior = true),
                    SurfaceSpec(-0.12f, 0.12f, -0.42f),
                ),
                light = RRect(-0.2f, -0.45f, 0.2f, 0f),
            )
            CHECKOUT -> FixtureSpec(
                0.56f, 0.20f, front = true,
                surfaces = listOf(SurfaceSpec(-0.27f, 0.27f, -0.2f)),
                spots = listOf(seat(0.2f, -0.12f)),
                machine = Machine.BELT,
            )
            CART -> FixtureSpec(
                0.26f, 0.28f, front = true,
                surfaces = listOf(SurfaceSpec(-0.1f, 0.06f, -0.12f)),
                spots = listOf(seat(0.08f, -0.16f)),
            )

            // Doctor
            HEIGHT_CHART -> FixtureSpec(0.12f, 0.5f, wall = true)
            XRAY -> FixtureSpec(0.30f, 0.44f, front = true, spots = listOf(SpotSpec(0f, -0.01f, Pose.STAND)), light = RRect(-0.25f, -0.5f, 0.25f, 0f))
            EXAM_BED -> FixtureSpec(
                0.44f, 0.17f,
                surfaces = listOf(SurfaceSpec(-0.2f, 0.2f, -0.17f)),
                spots = listOf(SpotSpec(0.02f, -0.17f, Pose.LIE)),
            )
            MEDICINE_CABINET -> FixtureSpec(
                0.26f, 0.24f, wall = true,
                container = RRect(-0.11f, -0.22f, 0.11f, -0.02f),
                surfaces = listOf(
                    SurfaceSpec(-0.11f, 0.11f, -0.02f, interior = true),
                    SurfaceSpec(-0.11f, 0.11f, -0.12f, interior = true),
                ),
            )
            DOCTOR_DESK -> FixtureSpec(0.36f, 0.18f, surfaces = listOf(SurfaceSpec(-0.18f, 0.18f, -0.18f)))
            EYE_CHART -> FixtureSpec(0.16f, 0.22f, wall = true)

            // Stage
            STAGE_PLATFORM -> FixtureSpec(1.5f, 0.12f, surfaces = listOf(SurfaceSpec(-0.75f, 0.75f, -0.12f)), light = RRect(-0.8f, -0.9f, 0.8f, 0f))
            DRUM_KIT -> FixtureSpec(0.36f, 0.30f, front = true, spots = listOf(seat(0.02f, -0.11f)))
            MIC_STAND -> FixtureSpec(0.07f, 0.34f)
            XYLOPHONE -> FixtureSpec(0.34f, 0.16f)
            SPEAKER -> FixtureSpec(0.18f, 0.34f, surfaces = listOf(SurfaceSpec(-0.09f, 0.09f, -0.34f)))
            DISCO_BALL -> FixtureSpec(0.12f, 0.14f, wall = true, light = RRect(-0.6f, -0.2f, 0.6f, 0.9f))
            SMOKE_MACHINE -> FixtureSpec(0.14f, 0.10f)

            // Under water
            SHIPWRECK -> FixtureSpec(
                0.80f, 0.50f,
                container = RRect(-0.12f, -0.26f, 0.12f, -0.04f),
                surfaces = listOf(
                    SurfaceSpec(-0.11f, 0.11f, -0.05f, interior = true),
                    SurfaceSpec(-0.36f, 0.3f, -0.36f),
                ),
            )
            KELP -> FixtureSpec(0.14f, 0.60f)
            GIANT_CLAM -> FixtureSpec(
                0.26f, 0.14f,
                container = RRect(-0.1f, -0.1f, 0.1f, -0.02f),
                surfaces = listOf(SurfaceSpec(-0.09f, 0.09f, -0.03f, interior = true)),
            )
            CORAL -> FixtureSpec(0.30f, 0.22f, surfaces = listOf(SurfaceSpec(-0.1f, 0.1f, -0.22f)))
            SUBMARINE -> FixtureSpec(0.46f, 0.26f, front = true, spots = listOf(seat(-0.08f, -0.08f), seat(0.1f, -0.08f)), light = RRect(0.15f, -0.25f, 0.6f, 0f))
            OCTOPUS -> FixtureSpec(0.30f, 0.28f)

            // Heileberget
            MOUNTAIN_HUT -> FixtureSpec(
                0.92f, 0.56f,
                container = RRect(-0.3f, -0.36f, 0.3f, -0.02f),
                surfaces = listOf(SurfaceSpec(-0.3f, 0.3f, -0.02f, interior = true), SurfaceSpec(-0.46f, 0.46f, -0.56f)),
                light = RRect(-0.5f, -0.6f, 0.5f, 0.05f),
            )
            CABLE_STATION -> FixtureSpec(0.5f, 0.44f, surfaces = listOf(SurfaceSpec(-0.25f, 0.25f, -0.44f)), light = RRect(-0.3f, -0.5f, 0.3f, 0f))
            // The cabin hangs from the cable; its floor is at the fixture's bottom edge. Two seats.
            CABLE_CAR -> FixtureSpec(0.34f, 0.3f, front = true, spots = listOf(seat(-0.07f, -0.06f), seat(0.08f, -0.06f)), light = RRect(-0.3f, -0.4f, 0.3f, 0.1f))
            // A tall rock with a flat top to stand on; the variant is the look (0 grey, 1 summit with snow).
            ROCK_LEDGE -> FixtureSpec(1.3f, 0.3f, surfaces = listOf(SurfaceSpec(-0.6f, 0.6f, -0.3f)))
            SUMMIT_ROCK -> FixtureSpec(1.2f, 0.5f, surfaces = listOf(SurfaceSpec(-0.5f, 0.5f, -0.5f)))
            ECHO_ROCK -> FixtureSpec(0.5f, 0.42f, surfaces = listOf(SurfaceSpec(-0.2f, 0.2f, -0.42f)))
            EAGLE_NEST -> FixtureSpec(0.36f, 0.2f, surfaces = listOf(SurfaceSpec(-0.12f, 0.12f, -0.12f)))
            SUMMIT_FLAG -> FixtureSpec(0.22f, 0.22f)

            // Catalogue furniture. Variants are colours or motifs.
            RUG -> FixtureSpec(0.46f, 0.01f)
            PICTURE -> FixtureSpec(0.16f, 0.14f, wall = true)
            AQUARIUM -> FixtureSpec(0.30f, 0.34f, surfaces = listOf(SurfaceSpec(-0.14f, 0.14f, -0.34f)), light = RRect(-0.2f, -0.4f, 0.2f, 0f))
            BEANBAG -> FixtureSpec(0.18f, 0.12f, spots = listOf(seat(0f, -0.06f)))
            ARMCHAIR -> FixtureSpec(0.20f, 0.20f, surfaces = listOf(SurfaceSpec(-0.07f, 0.07f, -0.08f)), spots = listOf(seat(0f, -0.08f)))
            BUNK_BED -> FixtureSpec(
                0.40f, 0.42f, front = true,
                surfaces = listOf(SurfaceSpec(-0.17f, 0.17f, -0.1f), SurfaceSpec(-0.17f, 0.17f, -0.3f)),
                spots = listOf(SpotSpec(0.02f, -0.1f, Pose.LIE), SpotSpec(0.02f, -0.3f, Pose.LIE)),
            )
            TOY_BOX -> FixtureSpec(
                0.20f, 0.12f,
                container = RRect(-0.09f, -0.11f, 0.09f, -0.015f),
                surfaces = listOf(
                    SurfaceSpec(-0.08f, 0.08f, -0.015f, interior = true),
                    SurfaceSpec(-0.1f, 0.1f, -0.12f, closedOnly = true),
                ),
            )
            DESK -> FixtureSpec(0.30f, 0.15f, surfaces = listOf(SurfaceSpec(-0.15f, 0.15f, -0.15f)))
            FLOWER_POT -> FixtureSpec(0.09f, 0.14f)
            TRASH_BIN -> FixtureSpec(0.10f, 0.14f, machine = Machine.TRASH, dropZone = RRect(-0.07f, -0.26f, 0.07f, -0.1f))
            ROBOT_VACUUM -> FixtureSpec(0.10f, 0.03f)

            // The big house: its floors know their own furniture. A type nobody knows yet is a plain box.
            else -> House.specOf(type) ?: FixtureSpec(0.3f, 0.3f)
        }
    }
}

/** The potions on the lab's rack, left to right. */
val POTION_ROW = listOf(
    ThingType.POTION_GROW,
    ThingType.POTION_SHRINK,
    ThingType.POTION_RAINBOW,
    ThingType.POTION_FLOAT,
    ThingType.POTION_NORMAL,
)

/** The tools on the farm's tool wall, left to right. */
val TOOL_ROW = listOf(
    ThingType.HAMMER,
    ThingType.SAW,
    ThingType.WRENCH,
    ThingType.SCREWDRIVER,
    ThingType.WATERING_CAN,
    ThingType.SEEDS,
)
