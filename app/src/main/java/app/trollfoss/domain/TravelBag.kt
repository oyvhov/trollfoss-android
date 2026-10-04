package app.trollfoss.domain

/** A small travel bag. Furniture is a Fixture and can never be packed through this Body API. */
object TravelBag {
    const val CAPACITY = 12
    const val PAGE_SIZE = 6
    fun canPack(world: World, body: Body): Boolean =
        body.mode == Mode.BAG || world.bag().size < CAPACITY

    /** Old bags and returned assembly parts are preserved, even when they exceed today's capacity. */
    fun pages(count: Int): Int = maxOf(1, (count + PAGE_SIZE - 1) / PAGE_SIZE)
    fun page(items: List<Body>, page: Int): List<Body> =
        items.drop(page.coerceIn(0, pages(items.size) - 1) * PAGE_SIZE).take(PAGE_SIZE)
}
