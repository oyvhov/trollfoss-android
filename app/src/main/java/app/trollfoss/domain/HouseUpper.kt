package app.trollfoss.domain

import kotlin.random.Random

/**
 * Storhuset, first floor («Andre høgda»): the landing with the family gallery, the children's room, the
 * playroom with the slide, the bathroom, the bedroom and the balcony. Six rooms in twelve scene units.
 *
 *  - landing and gallery (0 – 3.3): stairs down and up, the lift, the fireman's pole, the dumbwaiter, a window
 *    seat with a cat, and family portraits that blink and wave;
 *  - children's room (3.3 – 5.2): bunk bed, toy train that takes a passenger, a tower of blocks, the dollhouse of
 *    the house itself, a blanket fort, a night lamp, a rocket poster;
 *  - playroom (5.2 – 8.2): ball pit, slide to the living room, climbing wall, trampoline, easel, karaoke stage and
 *    a puppet theatre;
 *  - bathroom (8.2 – 9.4): shower with a rainbow, bath with ducks, mirror that fogs, towels, laundry chute;
 *  - bedroom (9.4 – 11.0): canopy bed, wardrobe that dresses you up, dressing table with a dancing ballerina,
 *    rocking chair and a window full of stars;
 *  - balcony (11.0 – 12.0): flower boxes, a bird feeder, a hanging chair, a telescope and the long slide to the garden.
 *
 * The rules are in [HouseUpperRules], the art in `ui/art/HouseUpper*Art.kt` and the effects in `ui/play/HouseUpperFx.kt`.
 */
object UpperFloor : Floor {
    override val place = PlaceId.MANOR_UPPER

    override val rooms = listOf(0f..3.3f, 3.3f..5.2f, 5.2f..8.2f, 8.2f..9.4f, 9.4f..11.0f, 11.0f..12f)

    override val passages = listOf(
        Passage("upper-stairs-down", place, HouseUpperIx.STAIRS_DOWN, PassageKind.STAIRS, PlaceId.MANOR_GROUND, "stairs-foot"),
        Passage("upper-lift", place, HouseUpperIx.LIFT, PassageKind.LIFT, PlaceId.MANOR_GROUND, "lift"),
        Passage("upper-stairs-up", place, HouseUpperIx.STAIRS_UP, PassageKind.STAIRS, PlaceId.MANOR_ATTIC, "stairs"),
        Passage("upper-slide", place, HouseUpperIx.SLIDE, PassageKind.SLIDE, PlaceId.MANOR_GROUND, "slide-end", oneWay = true),
        Passage("upper-pole", place, HouseUpperIx.POLE, PassageKind.POLE, PlaceId.MANOR_GROUND, "pole-end", oneWay = true),
        Passage("upper-dumbwaiter", place, HouseUpperIx.DUMBWAITER, PassageKind.DUMBWAITER, PlaceId.MANOR_GROUND, "dumbwaiter"),
        Passage("upper-balcony-slide", place, HouseUpperIx.BALCONY_SLIDE, PassageKind.SLIDE, PlaceId.MANOR_GARDEN, "slide-end", oneWay = true),
        Passage("upper-laundry-chute", place, HouseUpperIx.HATCH, PassageKind.HATCH, PlaceId.MANOR_CELLAR, "laundry-chute-end", oneWay = true),
    )

    override val arrivals = listOf(
        Arrival("landing", 2.86f),
        Arrival("lift", 3.1f),
        Arrival("dumbwaiter", 1.76f),
        Arrival("attic-stairs", 0.98f),
    )

    override fun hangouts(night: Boolean) =
        if (night) listOf(Arrival("kids-bed", 3.6f), Arrival("bedroom", 9.75f), Arrival("bath", 8.8f), Arrival("window", 1.35f), Arrival("rocker", 10.05f))
        else listOf(
            Arrival("gallery", 1.7f), Arrival("kids", 4.1f), Arrival("play", 6.0f), Arrival("play-right", 7.5f),
            Arrival("bath", 8.7f), Arrival("balcony", 11.3f), Arrival("window", 1.35f),
        )

    override fun blueprint(): PlaceSpec {
        val fl = place.floor
        /** A piece standing on the floor; [depth] is how far it stands in front of (+) or behind (-) the usual line. */
        fun f(type: FixtureType, x: Float, variant: Int = 0, depth: Float = 0f) = FixtureDef(type, x, fl + depth, variant, depth)
        /** A piece hanging on the wall: [y] is its bottom edge. */
        fun w(type: FixtureType, x: Float, y: Float, variant: Int = 0) = FixtureDef(type, x, y, variant)
        /** A piece that stands on another one; [top] is the height of that one's top above the floor line. */
        fun on(type: FixtureType, x: Float, top: Float, host: Int, variant: Int = 0) = FixtureDef(type, x, fl - top, variant, 0f, host)
        val ix = HouseUpperIx
        val fixtures = listOf(
            f(FixtureType.STAIRCASE, 0.50f, 1, -0.07f),            // 0 up to the attic
            f(FixtureType.PLANT_BIG, 0.99f, 0, -0.05f),            // 1
            f(FixtureType.UP_WINDOW_SEAT, 1.27f, 0, -0.07f),       // 2 window bench, the cat's place
            w(FixtureType.DUMBWAITER, 1.76f, 0.58f),               // 3 hatch to the kitchen
            f(FixtureType.FIRE_POLE, 1.98f, 0, 0f),                // 4 down to the hall
            f(FixtureType.STAIRCASE, 2.50f, 0, 0.02f),             // 5 down to the hall
            f(FixtureType.LIFT, 3.10f, 0, -0.08f),                 // 6
            f(FixtureType.SLIDE, 7.38f, 0, -0.04f),                // 7 playroom slide to the living room
            f(FixtureType.SLIDE, 11.56f, 1, 0.03f),                // 8 balcony slide to the garden
            f(FixtureType.HATCH, 8.42f, 0, 0.05f),                 // 9 laundry chute in the bathroom
            w(FixtureType.UP_WINDOW, 1.27f, 0.69f, 0),             // 10 landing window above the bench
            w(FixtureType.UP_PORTRAIT, 0.40f, 0.30f, 0),           // 11 grandpa
            w(FixtureType.UP_PORTRAIT, 0.76f, 0.30f, 1),           // 12 grandma
            w(FixtureType.UP_PORTRAIT, 1.27f, 0.285f, 2),          // 13 mum
            w(FixtureType.UP_PORTRAIT, 2.34f, 0.30f, 3),           // 14 dad
            w(FixtureType.UP_PORTRAIT, 2.72f, 0.30f, 4),           // 15 the baby
            w(FixtureType.UP_PORTRAIT, 3.08f, 0.30f, 5),           // 16 the dog
            f(FixtureType.RUG, 1.27f, 1, 0.045f),                  // 17 under the window seat
            f(FixtureType.BUNK_BED, 3.54f, 0, -0.07f),             // 18
            w(FixtureType.UP_MOBILE, 3.78f, 0.30f, 0),             // 19 over the top bunk
            w(FixtureType.UP_POSTER, 5.08f, 0.40f, 0),             // 20 rocket
            f(FixtureType.UP_DOLLHOUSE, 4.12f, 0, -0.07f),         // 21 the big house in small
            f(FixtureType.UP_FORT, 4.76f, 0, -0.06f),              // 22 blanket fort
            f(FixtureType.UP_NIGHT_LAMP, 5.1f, 0, -0.06f),         // 23
            f(FixtureType.UP_TOY_TRAIN, 4.0f, 0, 0.03f),           // 24
            f(FixtureType.UP_BLOCKS, 4.66f, 0, 0.055f),            // 25
            f(FixtureType.TOY_BOX, 5.08f, 0, 0.04f),               // 26
            f(FixtureType.RUG, 4.0f, 2, 0.01f),                    // 27 play mat
            f(FixtureType.UP_CLIMBING_WALL, 5.58f, 0, -0.07f),     // 28
            f(FixtureType.UP_KARAOKE, 6.3f, 0, -0.06f),            // 29
            f(FixtureType.UP_TRAMPOLINE, 5.7f, 0, 0.06f),          // 30
            f(FixtureType.UP_BALL_PIT, 6.62f, 0, 0.06f),           // 31
            f(FixtureType.UP_EASEL, 7.73f, 0, 0.035f),             // 32
            f(FixtureType.UP_PUPPET_THEATER, 8.02f, 0, -0.07f),    // 33
            w(FixtureType.UP_POSTER, 6.98f, 0.30f, 1),             // 34 dinosaur
            w(FixtureType.UP_POSTER, 7.62f, 0.27f, 2),             // 35 rainbow
            f(FixtureType.UP_SHOWER, 8.4f, 0, -0.07f),             // 36
            f(FixtureType.BATH, 8.82f, 0, -0.05f),                 // 37
            f(FixtureType.TOILET, 9.12f, 0, -0.08f),               // 38
            f(FixtureType.SINK, 9.28f, 0, -0.08f),                 // 39
            w(FixtureType.UP_BATH_MIRROR, 9.28f, 0.5f, 0),         // 40 the mirror that fogs
            w(FixtureType.UP_TOWELS, 8.84f, 0.44f, 0),             // 41
            f(FixtureType.BED, 9.72f, 1, -0.06f),                  // 42 the canopy bed
            w(FixtureType.UP_WINDOW, 10.12f, 0.6f, 1),             // 43 the window with the stars
            f(FixtureType.UP_VANITY, 10.5f, 0, -0.07f),            // 44
            on(FixtureType.UP_JEWEL_BOX, 10.5f, 0.20f, ix.VANITY),// 45 the ballerina
            f(FixtureType.UP_WARDROBE, 10.83f, 0, -0.08f),         // 46
            f(FixtureType.UP_ROCKING_CHAIR, 10.14f, 0, 0.08f),     // 47
            f(FixtureType.RUG, 9.9f, 3, 0.02f),                    // 48
            f(FixtureType.UP_RAILING, 11.5f, 0, -0.09f),           // 49 balcony railing
            f(FixtureType.UP_HANGING_CHAIR, 11.24f, 0, -0.06f),    // 50
            w(FixtureType.UP_BIRD_FEEDER, 11.82f, 0.36f, 0),       // 51
            f(FixtureType.TELESCOPE, 11.1f, 0, 0.07f),             // 52
            on(FixtureType.FLOWER_POT, 11.18f, 0.13f, ix.RAILING, 1),   // 53
            on(FixtureType.FLOWER_POT, 11.78f, 0.13f, ix.RAILING, 2),   // 54
        )
        val things = listOf(
            ThingDef(ThingType.DUCK, 8.74f, 0.74f, 0, on = ix.BATH),
            ThingDef(ThingType.DUCK, 8.84f, 0.74f, 0, on = ix.BATH),
            ThingDef(ThingType.DUCK, 8.93f, 0.74f, 0, on = ix.BATH),
            ThingDef(ThingType.PILLOW, 1.2f, 0.7f, 3, on = ix.WINDOW_SEAT),
            ThingDef(ThingType.BOOK, 1.4f, 0.7f, 2, on = ix.WINDOW_SEAT),
            ThingDef(ThingType.TEDDY, 3.5f, 0.45f, 0, on = ix.BUNK),
            ThingDef(ThingType.UP_BLOCK, 4.36f, 0.9f, 0),
            ThingDef(ThingType.UP_BLOCK, 4.43f, 0.93f, 2),
            ThingDef(ThingType.UP_BLOCK, 4.5f, 0.92f, 4),
            ThingDef(ThingType.BALL, 4.95f, 0.9f, 0),
            ThingDef(ThingType.TOY_CAR, 3.35f, 0.95f, 1),
            ThingDef(ThingType.UP_PAPER_PLANE, 3.9f, 0.7f, 1),
            ThingDef(ThingType.UP_PAINTBRUSH, 7.82f, 0.7f, 0, on = ix.EASEL),
            ThingDef(ThingType.BEACH_BALL, 6.7f, 0.7f, 0, on = ix.PIT),
            ThingDef(ThingType.UP_SOCK, 8.6f, 0.95f, 2),
            ThingDef(ThingType.UP_SLIPPER, 9.95f, 0.92f, 0),
            ThingDef(ThingType.SEEDS, 11.9f, 0.7f, 0),
            ThingDef(ThingType.MICROPHONE, 6.4f, 0.7f, 0, on = ix.KARAOKE),
            ThingDef(ThingType.CUP, 10.55f, 0.6f, 1, on = ix.VANITY),
        )
        return PlaceSpec(
            place,
            grounds = listOf(Ground(0f, place.width, fl)),
            water = null,
            fixtures = fixtures,
            things = things,
            // Only animals live here for good; folk come and go through the house (see [House.shuffle]).
            people = listOf(
                PersonDef(Species.CAT, Look(skin = 0), 1.6f, seat = ix.WINDOW_SEAT to 0),
                PersonDef(Species.BUNNY, Look(skin = 3), 6.9f, y = 0.96f),
            ),
        )
    }

    override fun specOf(type: FixtureType): FixtureSpec? = UpperSpecs.of(type)

    override fun rules(sim: Sim, random: Random): FloorRules = HouseUpperRules(sim, random)
}

/** Blueprint indices of the upper floor's fixtures, so the rules, the glimt and the tests agree. */
object HouseUpperIx {
    const val STAIRS_UP = 0
    const val WINDOW_SEAT = 2
    const val DUMBWAITER = 3
    const val POLE = 4
    const val STAIRS_DOWN = 5
    const val LIFT = 6
    const val SLIDE = 7
    const val BALCONY_SLIDE = 8
    const val HATCH = 9
    const val PORTRAIT_FIRST = 11
    const val PORTRAIT_LAST = 16
    const val BUNK = 18
    const val DOLLHOUSE = 21
    const val FORT = 22
    const val NIGHT_LAMP = 23
    const val TRAIN = 24
    const val BLOCKS = 25
    const val TOY_BOX = 26
    const val CLIMB = 28
    const val KARAOKE = 29
    const val TRAMP = 30
    const val PIT = 31
    const val EASEL = 32
    const val PUPPETS = 33
    const val SHOWER = 36
    const val BATH = 37
    const val TOILET = 38
    const val SINK = 39
    const val MIRROR = 40
    const val TOWELS = 41
    const val BED = 42
    const val STAR_WINDOW = 43
    const val VANITY = 44
    const val JEWEL = 45
    const val WARDROBE = 46
    const val ROCKER = 47
    const val RAILING = 49
    const val HANGING = 50
    const val FEEDER = 51
    const val TELESCOPE = 52
}

/** The loop of the toy train, shared by the rules and the art: an oblique ellipse on the floor round the fixture. */
object UpperTrack {
    /** Half the width and half the depth of the loop, in scene units. */
    const val RX = 0.30f
    const val RZ = 0.115f

    /** Where the train waits when it is not running, as an angle on the loop. */
    const val STATION = 4.712389f

    /** The gap between two cars, as an angle. */
    const val GAP = 0.62f

    /** Screen offset of the point at angle [a] from the fixture's bottom centre, x across, y down. */
    fun x(a: Float): Float = RX * kotlin.math.cos(a) + 0.5f * RZ * kotlin.math.sin(a)
    fun y(a: Float): Float = -0.36f * RZ * kotlin.math.sin(a)

    /** Which way the train faces at angle [a]: 1 to the right, -1 to the left. */
    fun heading(a: Float): Float = if (-RX * kotlin.math.sin(a) + 0.5f * RZ * kotlin.math.cos(a) >= 0f) 1f else -1f
}

/**
 * What the dollhouse in the children's room shows: tiny figures that stand where the figures of the big house
 * really are. The rules fill it while the floor is on screen; the art reads it (the dollhouse is drawn live
 * a few times a second, so the little ones really move).
 */
class MiniFigure(val floor: Int, val x: Float, val tone: Int, val kind: Int)

object UpperMirror {
    @Volatile
    var figures: List<MiniFigure> = emptyList()
        private set

    /** A number that changes when the little figures stand differently (coarsely), so a picture of the dollhouse can be kept until then. */
    @Volatile
    var signature = 0
        private set

    /** The floors of the big house, bottom to top, as the dollhouse shows them (the garden counts as floor 4). */
    val floors = listOf(PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER, PlaceId.MANOR_ATTIC, PlaceId.MANOR_CELLAR, PlaceId.MANOR_GARDEN)

    fun update(world: World) {
        val out = ArrayList<MiniFigure>(8)
        for (p in world.people()) {
            val at = p.place ?: continue
            val floor = floors.indexOf(at)
            if (floor < 0 || p.mode == Mode.BAG) continue
            val tone = if (p.species == Species.FOLK) Palette.cloth[p.look.topColor.mod(Palette.cloth.size)] else Palette.furFor(p.species, p.look.skin)
            val kind = when {
                p.species == Species.FOLK -> if (p.h < 0.26f) 1 else 0
                p.species == Species.GHOST -> 3
                p.species == Species.ROBOT -> 4
                else -> 2
            }
            out += MiniFigure(floor, (p.x / at.width).coerceIn(0.02f, 0.98f), tone, kind)
        }
        figures = out
        var h = 17
        for (m in out) h = h * 31 + (m.floor * 97 + (m.x * 14f).toInt() * 7 + m.kind * 3 + m.tone)
        signature = h
    }
}

/** The effect codes of the upper floor, in [HouseFx.UPPER] up to [HouseFx.ATTIC]. */
object UpperCodes {
    const val TRAIN_GO = 1
    const val TRAIN_STOP = 2
    const val TRAIN_CHUFF = 3
    const val TRAIN_LAP = 4
    const val BLOCKS_FALL = 5
    const val BLOCKS_BUILD = 6
    const val PORTRAIT = 7
    const val DOLL_BELL = 8
    const val PUPPET = 9
    const val LAMP = 10
    const val POSTER = 11
    const val MOBILE = 12
    const val PIT_BURST = 13
    const val PIT_PLUNGE = 14
    const val PIT_BALL = 15
    const val CLIMB_STEP = 16
    const val CLIMB_TOP = 17
    const val CLIMB_DROP = 18
    const val TRAMP = 19
    const val PAINT = 20
    const val KARAOKE = 21
    const val KARAOKE_SQUEAL = 22
    const val FORT = 23
    const val SHOWER = 24
    const val SHOWER_SING = 25
    const val RAINBOW = 26
    const val MIRROR = 27
    const val TOWEL = 28
    const val WARDROBE = 29
    const val DRESS = 30
    const val WARDROBE_TOSS = 31
    const val VANITY = 32
    const val JEWEL = 33
    const val ROCK = 34
    const val STAR_WISH = 35
    const val FEEDER_FILL = 36
    const val BIRD = 37
    const val SWING = 38
    const val RAIL = 39
    const val DUCK_KEY = 40
    const val BATH = 41
    const val WINDOW = 42
    const val PURR = 43
    const val SEAT_BOING = 44
    const val LOOK_AT_TELESCOPE = 45
}
