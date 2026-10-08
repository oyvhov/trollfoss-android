package app.trollfoss.domain

/** Both waterfalls lead to the existing cave, preserving every figure and object in old saves. */
object WaterfallSecret {
    val forestWater = RRect(2.80f, 0.18f, 3.26f, 0.77f)
    val caveWater = RRect(2.66f, 0.18f, 2.80f, 0.76f)
    // Keep the way out in view on both phone and tablet as the friends arrive.
    const val CAVE_ARRIVAL = 1.70f
    // Arrive on the dry bank, not in the plunge pool.
    const val FOREST_ARRIVAL = 2.30f
    val entrance = Passage("waterfall-in", PlaceId.FOREST, -1, PassageKind.SECRET, PlaceId.LAB, "waterfall")
    val exit = Passage("waterfall-out", PlaceId.LAB, -1, PassageKind.SECRET, PlaceId.FOREST, "waterfall")

    fun known(world: World): Boolean = PlaceId.LAB in world.visited || world.place == PlaceId.LAB

    /** Hints lead to the waterfall until the child has actually entered the cave. */
    fun destination(world: World, requested: PlaceId): PlaceId =
        if (requested == PlaceId.LAB && !known(world)) PlaceId.FOREST else requested

    fun at(place: PlaceId, x: Float, y: Float): Boolean = when (place) {
        PlaceId.FOREST -> x in forestWater.left..forestWater.right && y in forestWater.top..forestWater.bottom
        PlaceId.LAB -> x in caveWater.left..caveWater.right && y in caveWater.top..caveWater.bottom
        else -> false
    }

    fun passage(place: PlaceId): Passage? = when (place) {
        PlaceId.FOREST -> entrance
        PlaceId.LAB -> exit
        else -> null
    }

    fun arrival(passage: Passage): Float = if (passage.to == PlaceId.LAB) CAVE_ARRIVAL else FOREST_ARRIVAL
}
