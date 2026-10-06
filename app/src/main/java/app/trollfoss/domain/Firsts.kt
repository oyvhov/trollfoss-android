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
    HUG(FirstGroup.FRIENDS), HIGH_FIVE(FirstGroup.FRIENDS), HOLD_HANDS(FirstGroup.FRIENDS), PET(FirstGroup.FRIENDS),
    LIVING_TEDDY(FirstGroup.FRIENDS), BAND(FirstGroup.FRIENDS), PARTY(FirstGroup.FRIENDS),
    SNOWMAN(FirstGroup.LIFE), SANDCASTLE(FirstGroup.LIFE), COOK(FirstGroup.LIFE), EAT(FirstGroup.LIFE), BATH(FirstGroup.LIFE),
    SLEEP(FirstGroup.LIFE), READ(FirstGroup.LIFE), PHONE(FirstGroup.LIFE), BRUSH_TEETH(FirstGroup.LIFE), THROW_BALL(FirstGroup.LIFE),
    CATCH_BALL(FirstGroup.LIFE), TIDY(FirstGroup.LIFE), PAINT_ROOM(FirstGroup.LIFE), BUILD_ROOM(FirstGroup.LIFE),
    FIRST_TRIP(FirstGroup.WORLD), TRIP_5(FirstGroup.WORLD), TRIP_12(FirstGroup.WORLD), TREASURE_BOX(FirstGroup.WORLD),
    ATTIC_CHEST(FirstGroup.WORLD), PEEK_TROLL(FirstGroup.WORLD);

    companion object { fun byName(name: String): First? = entries.firstOrNull { it.name == name } }
}
