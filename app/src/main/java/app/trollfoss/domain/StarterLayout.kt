package app.trollfoss.domain

import kotlin.math.abs

/** Give untouched starter props a useful home without clearing the child's own arrangements. */
object StarterLayout {
    const val FLAG = "layout:small-things:1"
    private val small = setOf(ThingType.UP_BLOCK, ThingType.UP_SOCK, ThingType.UP_SLIPPER,
        ThingType.CE_SOCK, ThingType.AT_RECORD, ThingType.BOOK, ThingType.TOY_CAR,
        ThingType.WRENCH, ThingType.SCREWDRIVER, ThingType.HAMMER, ThingType.SEEDS)
    private val storage = setOf(FixtureType.CHEST, FixtureType.TOY_BOX, FixtureType.AT_TRUNK,
        FixtureType.WARDROBE, FixtureType.UP_WARDROBE)
    private val shelves = setOf(FixtureType.SHELF, FixtureType.BOOKCASE, FixtureType.DESK,
        FixtureType.TABLE, FixtureType.ROUND_TABLE, FixtureType.COUNTER, FixtureType.WORKBENCH,
        FixtureType.CE_BASKET)

    fun upgrade(world: World) {
        if (FLAG in world.flags) return
        PlaceId.entries.forEach { tidyPlace(world, it) }
        world.flags += FLAG
    }

    fun tidyPlace(world: World, place: PlaceId): Int {
        if (place.outdoor || place.mine) return 0
        var count = 0
        val fixtures = world.fixturesIn(place).filter {
            place.indexOf(it.id) < place.addedFrom && !WorldFactory.moved(it) &&
                (it.type in storage || it.type in shelves || it.type == FixtureType.AT_GRAMOPHONE)
        }
        for (t in world.bodiesIn(place).filterIsInstance<Thing>()) {
            // Home coordinates are persisted, unlike resting. A moved, carried, used or newly made
            // object has no claim to this automatic, one-time rearrangement.
            if (t.type !in small || t.type.lift != 1f || t.mode != Mode.FREE || t.held ||
                t.inside >= 0 || t.flyT >= 0f || t.used != 0 || t.homePlace != place ||
                t.homeOwner >= 0 || t.y < place.back || abs(t.x - t.homeDx) > 0.005f ||
                abs(t.y - t.homeDy) > 0.005f) continue
            val room = Decor.roomAt(place, t.x)
            val candidates = fixtures.filter { f ->
                (Decor.roomAt(place, f.x) == room || abs(f.x - t.x) < 1.5f) &&
                    (f.type != FixtureType.AT_GRAMOPHONE || t.type == ThingType.AT_RECORD)
            }.sortedWith(compareBy<Fixture> {
                when {
                    t.type in setOf(ThingType.UP_SOCK, ThingType.UP_SLIPPER, ThingType.CE_SOCK) &&
                        it.type in setOf(FixtureType.CE_BASKET, FixtureType.WARDROBE, FixtureType.UP_WARDROBE) -> 0
                    t.type == ThingType.BOOK && it.type == FixtureType.BOOKCASE -> 0
                    t.type == ThingType.AT_RECORD && it.type == FixtureType.AT_GRAMOPHONE -> 0
                    t.type in setOf(ThingType.WRENCH, ThingType.SCREWDRIVER, ThingType.HAMMER) && it.type == FixtureType.WORKBENCH -> 0
                    it.type in storage -> 1
                    else -> 2
                }
            }.thenBy { abs(it.x - t.x) })
            var placed = false
            for (f in candidates) {
                for (surface in f.spec.surfaces.filter { !it.closedOnly &&
                    (it.interior || f.type !in storage) }) {
                    val y = f.y + surface.dy
                    val box = f.spec.container
                    if (surface.interior && (box == null || y - t.h < f.y + box.top)) continue
                    val left = f.x + surface.x1 + t.w / 2f + 0.008f
                    val right = f.x + surface.x2 - t.w / 2f - 0.008f
                    if (right < left) continue
                    // Try actual empty space on this shelf. Never pile all the small props together.
                    var x = left
                    while (x <= right + 0.0001f) {
                        val occupiedFixture = !surface.interior && world.fixturesIn(place).any { other ->
                            other.host == f.id && abs(other.x - x) < (other.spec.w + t.w) / 2f + 0.008f }
                        val occupied = occupiedFixture || world.bodiesIn(place).any { other -> other.id != t.id &&
                            other.mode != Mode.BAG && other.mode != Mode.INSIDE &&
                            abs(other.x - x) < (other.w + t.w) / 2f + 0.008f &&
                            other.y > y - t.h && other.y - other.h < y + 0.005f }
                        if (!occupied) {
                            t.x = x; t.y = y; t.ground = f.depth + 0.012f
                            t.inside = if (surface.interior) f.id else -1
                            t.restOwner = f.id; t.resting = true; t.vx = 0f; t.vy = 0f; t.rot = 0f
                            WorldFactory.remember(world, t)
                            placed = true; count++
                            break
                        }
                        x += t.w + 0.016f
                    }
                    if (placed) break
                }
                if (placed) break
            }
        }
        return count
    }
}
