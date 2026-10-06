package app.trollfoss.domain

/** Kinds of first-time discoveries, in the order the book shows them. */
enum class FirstGroup { DRIVE, PLAY, FRIENDS, LIFE, WORLD }

/** Something the child did for the first time in free play. Each one earns a sticker once per world. */
enum class First(val group: FirstGroup) {
    TRACTOR(FirstGroup.DRIVE), BOAT(FirstGroup.DRIVE), SUBMARINE(FirstGroup.DRIVE), BUMPER_CAR(FirstGroup.DRIVE),
    BUS(FirstGroup.DRIVE), TANDEM(FirstGroup.DRIVE), TOY_TRAIN(FirstGroup.DRIVE), VACUUM_RIDE(FirstGroup.DRIVE),
    BALLOON(FirstGroup.DRIVE), SKY_ISLAND(FirstGroup.DRIVE),
    BUBBLE_POP(FirstGroup.PLAY), PILLOW_LAUNCH(FirstGroup.PLAY), MARBLES(FirstGroup.PLAY), COLOUR_SPRAY(FirstGroup.PLAY),
    POPCORN(FirstGroup.PLAY), PUMP(FirstGroup.PLAY), PHOTO(FirstGroup.PLAY), MINI_LIFT(FirstGroup.PLAY), WINDMILL(FirstGroup.PLAY),
    SEESAW(FirstGroup.PLAY), PUPPETS(FirstGroup.PLAY), PICNIC(FirstGroup.PLAY), CRANE(FirstGroup.PLAY), CONVEYOR(FirstGroup.PLAY),
    BUILD_TABLE(FirstGroup.PLAY), WATER_CHANNEL(FirstGroup.PLAY), WATER_WHEEL(FirstGroup.PLAY), RAIN_CLOUD(FirstGroup.PLAY),
    MIRROR(FirstGroup.PLAY), HOVER(FirstGroup.PLAY), PORTAL(FirstGroup.PLAY), OBSTACLE(FirstGroup.PLAY), APPLE_HARVEST(FirstGroup.PLAY),
    REPAIR_LIGHT(FirstGroup.PLAY), SECRET_DOOR(FirstGroup.PLAY), STAMP_ART(FirstGroup.PLAY), HANG_ART(FirstGroup.PLAY),
    CABLE_CAR(FirstGroup.PLAY), DIVING_BELL(FirstGroup.PLAY), DIGGER(FirstGroup.PLAY), TREASURE_TABLE(FirstGroup.PLAY),
    ECHO_BOX(FirstGroup.PLAY), DANCE_FLOOR(FirstGroup.PLAY), CONFETTI(FirstGroup.PLAY), LIGHT_RIG(FirstGroup.PLAY),
    HUG(FirstGroup.FRIENDS), HIGH_FIVE(FirstGroup.FRIENDS), HOLD_HANDS(FirstGroup.FRIENDS), PET(FirstGroup.FRIENDS),
    LIVING_TEDDY(FirstGroup.FRIENDS), BAND(FirstGroup.FRIENDS), PARTY(FirstGroup.FRIENDS),
    SNOWMAN(FirstGroup.LIFE), SANDCASTLE(FirstGroup.LIFE), COOK(FirstGroup.LIFE), EAT(FirstGroup.LIFE), BATH(FirstGroup.LIFE),
    SLEEP(FirstGroup.LIFE), READ(FirstGroup.LIFE), PHONE(FirstGroup.LIFE), BRUSH_TEETH(FirstGroup.LIFE), THROW_BALL(FirstGroup.LIFE),
    CATCH_BALL(FirstGroup.LIFE), TIDY(FirstGroup.LIFE), PAINT_ROOM(FirstGroup.LIFE), BUILD_ROOM(FirstGroup.LIFE),
    FIRST_TRIP(FirstGroup.WORLD), TRIP_5(FirstGroup.WORLD), TRIP_12(FirstGroup.WORLD), TREASURE_BOX(FirstGroup.WORLD),
    ATTIC_CHEST(FirstGroup.WORLD), PEEK_TROLL(FirstGroup.WORLD);

    companion object { fun byName(name: String): First? = entries.firstOrNull { it.name == name } }
}

/** One look back at an older save: what it clearly shows the child has done counts, without a celebration. */
object FirstsRetro {
    const val FLAG = "firsts:retro:1"
    fun upgrade(world: World) {
        if (!world.flags.add(FLAG)) return
        val found = buildList {
            if (world.mine.ground.any { it != 0 } || world.mine.upper.any { it != 0 }) add(First.BUILD_ROOM)
            if (world.community.pets.isNotEmpty()) add(First.PET)
            if (world.community.art.isNotEmpty()) add(First.STAMP_ART)
            if (world.community.wallArt.isNotEmpty()) add(First.HANG_ART)
            if (world.community.doors.isNotEmpty()) add(First.SECRET_DOOR)
            if (world.flags.any { it.startsWith("place:repaired:") }) add(First.REPAIR_LIGHT)
            if (world.bodies.values.any { it.mode == Mode.INSIDE && world.fixtures[it.holder]?.type == FixtureType.TREASURE_BOX }) add(First.TREASURE_BOX)
        }
        for (f in found) if (world.firsts.add(f.name)) world.stickers += world.stickers.size
        Progression.remember(world)
    }
}

/** Turns the coarse sim events into first-time discoveries. Events that already earn a sticker are left out. */
object FirstsDetector {
    fun of(fx: Fx, fixture: Fixture?, thing: Thing?, param: Int): First? {
        val type = fixture?.type
        return when (fx) {
            Fx.VROOM -> if (param != 2) null else when (type) {
                FixtureType.TRACTOR -> First.TRACTOR; FixtureType.SUBMARINE -> First.SUBMARINE
                FixtureType.BUMPER_CAR -> First.BUMPER_CAR; FixtureType.PLAY_BUS -> First.BUS
                FixtureType.PLAY_TRAIN -> First.TOY_TRAIN; FixtureType.PLAY_TANDEM -> First.TANDEM
                else -> null
            }
            Fx.TOOT -> if (type == FixtureType.BOAT) First.BOAT else null
            Fx.BOING -> if (type == FixtureType.PLAY_LAUNCHER) First.PILLOW_LAUNCH else null
            Fx.DING -> if (type == FixtureType.PLAY_MARBLES) First.MARBLES else null
            Fx.SPLAT -> if (type == FixtureType.PLAY_COLORS) First.COLOUR_SPRAY else null
            Fx.COOKED -> if (type == FixtureType.PLAY_POPCORN) First.POPCORN else if (thing != null) First.COOK else null
            Fx.PUMP -> when (type) { FixtureType.PLAY_PUMP -> First.PUMP; FixtureType.PLAY_LIFT -> First.MINI_LIFT; else -> null }
            Fx.PAGE -> if (type == FixtureType.PLAY_PUPPETS) First.PUPPETS else null
            Fx.BUILD -> when (type) {
                FixtureType.PLAY_CART -> First.BUILD_TABLE; FixtureType.PLAY_REPAIR -> First.REPAIR_LIGHT
                FixtureType.SNOWMAN -> First.SNOWMAN; FixtureType.SANDCASTLE -> First.SANDCASTLE
                else -> null
            }
            Fx.INTO -> when (type) {
                FixtureType.PLAY_CHANNEL -> First.WATER_CHANNEL; FixtureType.PLAY_WATER_WHEEL -> First.WATER_WHEEL
                FixtureType.PLAY_CLOUD -> First.RAIN_CLOUD; else -> null
            }
            Fx.POOF -> if (type == FixtureType.PLAY_PORTAL) First.PORTAL else null
            Fx.WHEE -> if (type == FixtureType.PLAY_TUNNEL || type == FixtureType.PLAY_JUMP) First.OBSTACLE else null
            Fx.TIDY -> First.TIDY
            Fx.PAINT -> First.PAINT_ROOM
            Fx.TREASURE_IN -> First.TREASURE_BOX
            else -> null
        }
    }
}
