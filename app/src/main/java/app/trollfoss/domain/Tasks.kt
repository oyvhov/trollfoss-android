package app.trollfoss.domain

/** Something the child did, as the task board hears of it. */
enum class Deed {
    MADE, ATE, WORE, DRESSED, HAIRCUT, SEATED, FED, BROUGHT, PHOTO, SECRET, WISH, TIDY, PAINT, FURNISH,
    PRRT, SNEEZE, SLIP, SPLAT, BURP, CATCH, BREW, WHEE, SCAN, XRAY, HEART, SING, DISCO, INK, LAUNCH,
    GRAVITY, HARVEST, BUILD, VROOM, HATCH, KNOCK, BOUNCE, SNOWMAN, GIFT, CABLE, ECHO, SUMMIT,

    // Storhuset: taking any way between floors, then each floor's own deeds in its block.
    PASSAGE,
    // ---- ground floor ----
    GR_FILM, GR_TABLE, GR_FEED, GR_PIZZA, GR_GROW, GR_LEVER, GR_SPLASH,

    // ---- upper floor ----
    UP_TRAIN, UP_KNOCK, UP_SPLAT, UP_DRESS,

    // ---- attic ----
    AT_COSTUME, AT_CATCH, AT_RECORD, AT_STARGAZE, AT_KNIT,

    // ---- cellar ----
    /** A sock fed to the sock monster; the three boiler valves all open; a dive from the springboard; a tune started on the jukebox. */
    CE_SOCK_FED, CE_VALVES, CE_DIVE, CE_DANCE,

    // ---- garden ----
    GA_CHOIR, GA_GIANT, GA_ZIP, GA_GRILL,

    // ---- stories and seasons ----

    // ---- Mitt hus (MineBuilder.kt) ----
    MI_ROOM, MI_FLOOR, MI_LOOK, MI_PARTY,
}

/**
 * A task on the board: a picture (a thing, a piece of furniture or an animal, perhaps with a second
 * picture), where it happens, and how many times. [match] says which deeds count.
 */
class Task(
    val id: String,
    val place: PlaceId?,
    val need: Int,
    val fixture: FixtureType? = null,
    val thing: ThingType? = null,
    val species: Species? = null,
    val icon: String? = null,
    val match: (Deed, PlaceId, ThingType?, FixtureType?, Species?) -> Boolean,
)

/**
 * The task board («oppdragstavla»): three picture tasks at a time that send the child all over the
 * village. Each finished task earns a sticker; stickers open special furniture in the catalogue.
 * Nothing is ever lost or timed, and a new set comes when a set is done.
 */
class TaskBook(private val world: World) {
    /** Blueprint population is setup, not something a child did. */
    var recording=true
    /** Called when a task is finished, for the celebration. */
    var onDone: (Task) -> Unit = {}

    /** The three tasks on the board now. */
    fun board(): List<Task> {
        if (world.taskSet.isEmpty()) deal()
        return world.taskSet.mapNotNull { id -> ALL.firstOrNull { it.id == id } }
    }

    fun progress(task: Task): Int = world.taskProgress[task.id] ?: 0

    fun done(task: Task): Boolean = progress(task) >= task.need

    /** Deals the next three tasks from a shuffled deck, each in a different place where possible. */
    fun deal() {
        world.taskSet.clear()
        world.taskProgress.clear()
        if (world.stickers.size < 5) {
            world.taskSet += listOf("feed_horse", "bedtime", "crown")
            world.taskCursor += 3
            return
        }
        val deck = ALL.shuffled(kotlin.random.Random(world.taskSeed))
        val places = HashSet<PlaceId?>()
        var i = world.taskCursor
        var guard = 0
        while (world.taskSet.size < 3 && guard < ALL.size * 2) {
            val t = deck[i % deck.size]
            i++
            guard++
            if (t.place != null && t.place in places && guard < ALL.size) continue
            if (t.id in world.taskSet) continue
            world.taskSet += t.id
            places += t.place
        }
        world.taskCursor = i
    }

    /** True when all three tasks on the board are done, and a new set can be dealt. */
    fun allDone(): Boolean = board().all { done(it) }

    /** A voluntary alternative keeps the progress on the other two cards. */
    fun swap(task: Task) {
        val slot=world.taskSet.indexOf(task.id)
        if(slot<0 || done(task)) return
        val choices=if(world.stickers.size<5) ALL.filter { it.id in listOf("feed_horse","bedtime","crown","feed_dog","bath","tractor","harvest") } else ALL
        val next=choices.shuffled(kotlin.random.Random(world.taskSeed+world.taskCursor++)).firstOrNull { it.id !in world.taskSet } ?: return
        world.taskProgress.remove(task.id);world.taskProgress.remove(next.id)
        world.taskSet[slot]=next.id
    }

    /** Hears a deed; moves on every task it counts for. */
    fun record(deed: Deed, place: PlaceId, thing: ThingType? = null, fixture: FixtureType? = null, species: Species? = null) {
        if(!recording) return
        for (t in board()) {
            if (done(t)) continue
            if (t.place != null && t.place != place && deed != Deed.BROUGHT) continue
            if (!t.match(deed, place, thing, fixture, species)) continue
            val now = progress(t) + 1
            world.taskProgress[t.id] = now
            if (now >= t.need) {
                world.stickers += world.stickers.size
                Progression.remember(world)
                onDone(t)
            }
        }
    }

    companion object {
        /** The Easter eggs, in the order the book shows them. */
        val EGGS = listOf("quake", "king", "duck", "twinkle", "starshot", "elk", "mountain", "strange_MUSHROOM", "strange_SLIME", "mirror_light") +
            PlaySecrets.nooks.map { (place, spot) -> "nook_${place.name}_${spot.second}" }

        private fun deed(d: Deed): (Deed, PlaceId, ThingType?, FixtureType?, Species?) -> Boolean = { x, _, _, _, _ -> x == d }

        val ALL: List<Task> = listOf(
            Task("bake_cake", PlaceId.CAFE, 1, FixtureType.OVEN, ThingType.CAKE) { d, _, t, _, _ -> d == Deed.MADE && t == ThingType.CAKE },
            Task("smoothie", PlaceId.CAFE, 1, FixtureType.BLENDER, ThingType.SMOOTHIE) { d, _, t, _, _ -> d == Deed.MADE && t == ThingType.SMOOTHIE },
            Task("egg_to_cafe", PlaceId.CAFE, 1, thing = ThingType.EGG, icon = "bring") { d, p, t, _, _ -> d == Deed.BROUGHT && p == PlaceId.CAFE && t == ThingType.EGG },
            Task("fish_home", PlaceId.HOME, 1, thing = ThingType.FISH, species = Species.CAT, icon = "bring") { d, p, t, _, _ -> d == Deed.BROUGHT && p == PlaceId.HOME && (t == ThingType.FISH || t == ThingType.GRILLED_FISH) },
            Task("brew", PlaceId.LAB, 1, FixtureType.CAULDRON, icon = "star", match = deed(Deed.BREW)),
            Task("catch_fish", PlaceId.BEACH, 2, FixtureType.FISHING_SPOT, ThingType.FISH, match = deed(Deed.CATCH)),
            Task("ferris_top", PlaceId.TIVOLI, 1, FixtureType.FERRIS_WHEEL) { d, _, _, f, _ -> d == Deed.WHEE && f == FixtureType.FERRIS_WHEEL },
            Task("cans", PlaceId.TIVOLI, 1, FixtureType.CAN_TOSS, ThingType.BALL, match = deed(Deed.KNOCK)),
            Task("bounce", PlaceId.TIVOLI, 3, FixtureType.TRAMPOLINE, match = deed(Deed.BOUNCE)),
            Task("scan", PlaceId.SHOP, 3, FixtureType.CHECKOUT, match = deed(Deed.SCAN)),
            Task("xray", PlaceId.DOCTOR, 1, FixtureType.XRAY, match = deed(Deed.XRAY)),
            Task("heart", PlaceId.DOCTOR, 1, thing = ThingType.STETHOSCOPE, icon = "heart", match = deed(Deed.HEART)),
            Task("sing", PlaceId.STAGE, 1, FixtureType.MIC_STAND, icon = "note", match = deed(Deed.SING)),
            Task("disco", PlaceId.STAGE, 1, FixtureType.DISCO_BALL, match = deed(Deed.DISCO)),
            Task("ink", PlaceId.UNDERWATER, 1, FixtureType.OCTOPUS, match = deed(Deed.INK)),
            Task("submarine", PlaceId.UNDERWATER, 1, FixtureType.SUBMARINE) { d, _, _, f, _ -> d == Deed.VROOM && f == FixtureType.SUBMARINE },
            Task("launch", PlaceId.SPACE, 1, FixtureType.ROCKET_SHIP, match = deed(Deed.LAUNCH)),
            Task("gravity", PlaceId.SPACE, 1, FixtureType.GRAVITY_LEVER, match = deed(Deed.GRAVITY)),
            Task("harvest", PlaceId.FARM, 1, FixtureType.VEGETABLE_PATCH, ThingType.CARROT, match = deed(Deed.HARVEST)),
            Task("build", PlaceId.FARM, 1, FixtureType.WORKBENCH, ThingType.BIRDHOUSE, match = deed(Deed.BUILD)),
            Task("tractor", PlaceId.FARM, 1, FixtureType.TRACTOR) { d, _, _, f, _ -> d == Deed.VROOM && f == FixtureType.TRACTOR },
            Task("feed_horse", PlaceId.FARM, 1, thing = ThingType.CARROT, species = Species.HORSE) { d, _, _, _, s -> d == Deed.FED && s in setOf(Species.HORSE, Species.COW, Species.SHEEP) },
            Task("hatch", PlaceId.FOREST, 1, thing = ThingType.DRAGON_EGG, match = deed(Deed.HATCH)),
            Task("marshmallow", PlaceId.FOREST, 1, FixtureType.CAMPFIRE, ThingType.TOASTED_MARSHMALLOW) { d, _, t, _, _ -> d == Deed.MADE && t == ThingType.TOASTED_MARSHMALLOW },
            Task("cable_car", PlaceId.HEILEBERGET, 1, FixtureType.CABLE_CAR, match = deed(Deed.CABLE)),
            Task("echo", PlaceId.HEILEBERGET, 1, FixtureType.ECHO_ROCK, icon = "note", match = deed(Deed.ECHO)),
            Task("summit", PlaceId.HEILEBERGET, 1, FixtureType.SUMMIT_FLAG, match = deed(Deed.SUMMIT)),
            Task("ski_jump", PlaceId.MOUNTAIN, 1, FixtureType.SKI_JUMP) { d, _, _, f, _ -> d == Deed.WHEE && f == FixtureType.SKI_JUMP },
            Task("snowman", PlaceId.MOUNTAIN, 1, FixtureType.SNOWMAN, match = deed(Deed.SNOWMAN)),
            Task("bedtime", PlaceId.HOME, 1, FixtureType.BED, icon = "zzz") { d, _, _, f, _ -> d == Deed.SEATED && f in setOf(FixtureType.BED, FixtureType.BUNK_BED) },
            Task("bath", PlaceId.HOME, 1, FixtureType.BATH) { d, _, _, f, _ -> d == Deed.SEATED && f == FixtureType.BATH },
            Task("haircut", PlaceId.SALON, 1, thing = ThingType.SCISSORS, match = deed(Deed.HAIRCUT)),
            Task("dress_up", PlaceId.SALON, 2, thing = ThingType.GARMENT, match = deed(Deed.DRESSED)),
            Task("crown", null, 1, thing = ThingType.CROWN) { d, _, t, _, _ -> d == Deed.WORE && t?.cat == Cat.HAT },
            Task("feed_dog", null, 1, thing = ThingType.SAUSAGE, species = Species.DOG) { d, _, _, _, s -> d == Deed.FED && s == Species.DOG },
            Task("sneeze", null, 1, thing = ThingType.PEPPER, icon = "sneeze", match = deed(Deed.SNEEZE)),
            Task("prrt", null, 1, thing = ThingType.WHOOPEE, match = deed(Deed.PRRT)),
            Task("slip", null, 1, thing = ThingType.BANANA_PEEL, match = deed(Deed.SLIP)),
            Task("splat", null, 1, thing = ThingType.CUPCAKE, icon = "face", match = deed(Deed.SPLAT)),
            Task("burp", null, 1, thing = ThingType.SODA, icon = "burp", match = deed(Deed.BURP)),
            Task("wishes", null, 3, icon = "wish", match = deed(Deed.WISH)),
            Task("glimt", null, 2, icon = "glimt", match = deed(Deed.SECRET)),
            Task("tidy", null, 1, icon = "broom", match = deed(Deed.TIDY)),
            Task("paint", null, 1, icon = "roller", match = deed(Deed.PAINT)),
            Task("furnish", null, 2, icon = "sofa", match = deed(Deed.FURNISH)),
            Task("photo", null, 1, icon = "camera", match = deed(Deed.PHOTO)),
            Task("gift", null, 1, thing = ThingType.GIFT, match = deed(Deed.GIFT)),

            // Storhuset. Each floor adds its tasks in its own block (a place of its own, a picture, a deed).
            // ---- ground floor ----
            Task("ground_film", PlaceId.MANOR_GROUND, 1, thing = ThingType.POPCORN, icon = "star", match = deed(Deed.GR_FILM)),
            Task("ground_table", PlaceId.MANOR_GROUND, 1, thing = ThingType.GR_TRAY, match = deed(Deed.GR_TABLE)),
            Task("ground_sofie", PlaceId.MANOR_GROUND, 1, thing = ThingType.APPLE, icon = "burp", match = deed(Deed.GR_FEED)),
            Task("ground_pizza", PlaceId.MANOR_GROUND, 1, thing = ThingType.PIZZA, match = deed(Deed.GR_PIZZA)),
            Task("ground_plants", PlaceId.MANOR_GROUND, 2, thing = ThingType.WATERING_CAN, match = deed(Deed.GR_GROW)),

            // ---- upper floor ----
            Task("up_train", PlaceId.MANOR_UPPER, 1, FixtureType.UP_TOY_TRAIN, match = deed(Deed.UP_TRAIN)),
            Task("up_blocks", PlaceId.MANOR_UPPER, 1, FixtureType.UP_BLOCKS, ThingType.UP_BLOCK, match = deed(Deed.UP_KNOCK)),
            Task("up_paint", PlaceId.MANOR_UPPER, 3, FixtureType.UP_EASEL, ThingType.UP_PAINTBRUSH, match = deed(Deed.UP_SPLAT)),
            Task("up_dress", PlaceId.MANOR_UPPER, 1, FixtureType.UP_WARDROBE, match = deed(Deed.UP_DRESS)),

            // ---- attic ----
            Task("attic_costume", PlaceId.MANOR_ATTIC, 1, thing = ThingType.AT_PIRATE_HAT, match = deed(Deed.AT_COSTUME)),
            Task("attic_catch", PlaceId.MANOR_ATTIC, 1, species = Species.GHOST, thing = ThingType.AT_SHEET_HAT, match = deed(Deed.AT_CATCH)),
            Task("attic_record", PlaceId.MANOR_ATTIC, 1, thing = ThingType.AT_RECORD, icon = "note", match = deed(Deed.AT_RECORD)),
            Task("attic_stars", PlaceId.MANOR_ATTIC, 1, fixture = FixtureType.TELESCOPE, icon = "star", match = deed(Deed.AT_STARGAZE)),
            Task("attic_knit", PlaceId.MANOR_ATTIC, 1, thing = ThingType.GARMENT, match = deed(Deed.AT_KNIT)),

            // ---- cellar ----
            Task("cellar_socks", PlaceId.MANOR_CELLAR, 2, FixtureType.CE_SOCK_MONSTER, ThingType.CE_SOCK, match = deed(Deed.CE_SOCK_FED)),
            Task("cellar_valves", PlaceId.MANOR_CELLAR, 1, FixtureType.CE_VALVE, match = deed(Deed.CE_VALVES)),
            Task("cellar_dive", PlaceId.MANOR_CELLAR, 1, FixtureType.CE_DIVING_BOARD, match = deed(Deed.CE_DIVE)),
            Task("cellar_dance", PlaceId.MANOR_CELLAR, 1, FixtureType.CE_JUKEBOX, icon = "note", match = deed(Deed.CE_DANCE)),

            // ---- garden ----
            Task("garden_choir", PlaceId.MANOR_GARDEN, 1, thing = ThingType.MICROPHONE, icon = "note", match = deed(Deed.GA_CHOIR)),
            Task("garden_giant", PlaceId.MANOR_GARDEN, 1, thing = ThingType.GA_VEGGIE, icon = "star", match = deed(Deed.GA_GIANT)),
            Task("garden_zip", PlaceId.MANOR_GARDEN, 1, fixture = FixtureType.CABLE_CAR, icon = "star", match = deed(Deed.GA_ZIP)),
            Task("garden_grill", PlaceId.MANOR_GARDEN, 1, fixture = FixtureType.CAMPFIRE, thing = ThingType.GRILLED_SAUSAGE, match = deed(Deed.GA_GRILL)),

            // ---- stories and seasons ----

            // ---- Mitt hus: five tasks that send the child to the plot (the deeds are recorded for the yard, wherever the work is done).
            Task("mine_first_room", PlaceId.MINE_YARD, 1, FixtureType.SOFA) { d, _, _, _, _ -> d == Deed.MI_ROOM },
            Task("mine_three_rooms", PlaceId.MINE_YARD, 3, FixtureType.BED) { d, _, _, _, _ -> d == Deed.MI_ROOM },
            Task("mine_floor", PlaceId.MINE_YARD, 1, FixtureType.STAIRCASE) { d, _, _, _, _ -> d == Deed.MI_FLOOR },
            Task("mine_furnish", null, 2, FixtureType.ARMCHAIR) { d, p, _, _, _ -> d == Deed.FURNISH && p.mine },
            Task("mine_dress", PlaceId.MINE_YARD, 3, FixtureType.MI_MAILBOX) { d, _, _, _, _ -> d == Deed.MI_LOOK },
        )
    }
}
