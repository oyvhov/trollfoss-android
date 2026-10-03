package app.trollfoss.domain

/** Gems, coins and pearls: what a child collects. Nothing in the game takes them away on its own. */
object Treasure {
    val TYPES = setOf(ThingType.GEM, ThingType.COIN, ThingType.PEARL)

    fun isTreasure(t: Thing): Boolean = t.type in TYPES

    /** How many [type] lie about in [place]: free, not held and not inside any furniture. */
    fun loose(world: World, place: PlaceId, type: ThingType): Int =
        world.bodiesIn(place).count { it is Thing && it.type == type && it.mode == Mode.FREE && !it.held && it.inside < 0 }

    /**
     * The oldest loose thing a full place may let go of: never a treasure, never [keep] (the thing just
     * made) and never one the caller wants to [spare]. Null when there is nothing it may take.
     */
    fun oldestToDrop(things: List<Thing>, keep: Thing?, spare: (Thing) -> Boolean = { false }): Thing? =
        things.filter { it !== keep && it.mode == Mode.FREE && it.inside < 0 && !it.held && !isTreasure(it) && !spare(it) }
            .minByOrNull { it.z }
}
