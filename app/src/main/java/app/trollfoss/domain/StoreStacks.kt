package app.trollfoss.domain

/** Equal pieces in the store, shown as one card: [count] of [item], and the [index] a tap takes (the newest). */
data class StoreStack(val item: Stored, val count: Int, val index: Int)

object StoreStacks {
    /** Groups equal pieces. A stack stands where its oldest piece lies in [storage], so the list keeps its order. */
    fun of(storage: List<Stored>): List<StoreStack> {
        val newest = LinkedHashMap<Stored, Int>()
        val count = HashMap<Stored, Int>()
        storage.forEachIndexed { i, item ->
            newest[item] = i
            count[item] = (count[item] ?: 0) + 1
        }
        return newest.map { (item, index) -> StoreStack(item, count.getValue(item), index) }
    }

    /** The recycling box grouped the same way; [StoreStack.index] points into [discarded]. */
    fun ofDiscarded(discarded: List<Pair<Int, Stored>>): List<StoreStack> = of(discarded.map { it.second })
}
