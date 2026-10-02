package app.trollfoss.domain

/** Only complete levels are exposed. Stickers and older catalogue unlocks are never spent. */
enum class ToyReward(val level: Int, val type: FixtureType) {
    BUBBLES(2, FixtureType.PLAY_BUBBLES), WINDMILL(2, FixtureType.PLAY_WINDMILL),
    BUS(2, FixtureType.PLAY_BUS), PILLOW(2, FixtureType.PLAY_LAUNCHER),
    MARBLES(3, FixtureType.PLAY_MARBLES), COLORS(3, FixtureType.PLAY_COLORS),
    LIFT(3, FixtureType.PLAY_LIFT), POPCORN(3, FixtureType.PLAY_POPCORN),
    PUMP(1, FixtureType.PLAY_PUMP), CAMERA(1, FixtureType.PLAY_CAMERA),
    TRAIN(0, FixtureType.PLAY_TRAIN),
}

object Progression {
    val thresholds = listOf(0, 2, 5)
    fun level(world: World): Int = thresholds.count { world.stickers.size >= it }.coerceAtLeast(1)
    fun missing(world: World): Int = thresholds.getOrNull(level(world))?.let { (it - world.stickers.size).coerceAtLeast(0) } ?: 0
    fun unlocked(world: World, reward: ToyReward): Boolean = "toy:${reward.name}" in world.flags || reward.level > 0 && level(world) >= reward.level
    fun remember(world: World) { ToyReward.entries.filter { unlocked(world, it) }.forEach { world.flags += "toy:${it.name}" } }
}

/** A gesture or a nested structural edit forms one undo entry; UI state alone never does. */
interface EditJournal {
    fun begin(body: Int? = null)
    fun end()
    fun clear()
}

inline fun <T> Sim.edit(block: () -> T): T {
    journal?.begin()
    return try { block() } finally { journal?.end() }
}
