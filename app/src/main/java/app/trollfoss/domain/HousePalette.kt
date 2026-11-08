package app.trollfoss.domain

/** Existing colour indices remain stable. Only the coordinated exterior changes. */
enum class HousePalette(val wall: Int, val roof: Int, val roofColor: Int, val door: Int, val windows: Int) {
    MOON(11, 4, 5, 3, 4), FOREST(6, 4, 5, 5, 4), SUN(2, 0, 1, 3, 1);
    fun matches(h: MineHouse) = h.wall == wall && h.roof == roof && h.roofColor == roofColor && h.door == door && h.windows == windows
}
