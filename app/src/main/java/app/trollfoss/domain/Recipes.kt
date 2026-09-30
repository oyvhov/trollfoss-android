package app.trollfoss.domain

/** What a machine makes: a thing type and its variant. */
data class Made(val type: ThingType, val variant: Int = 0)

/** Something the discovery book knows about: made on [machine] from [inputs]. */
data class Recipe(val key: String, val machine: FixtureType, val inputs: List<ThingType>, val result: Made)

/**
 * Every transformation in Trollfoss. The same tables drive the machines and the discovery book, and a test
 * checks that each recipe really produces its result.
 */
object Recipes {
    private val heat: Map<ThingType, ThingType> = mapOf(
        ThingType.EGG to ThingType.FRIED_EGG,
        ThingType.FISH to ThingType.GRILLED_FISH,
        ThingType.SAUSAGE to ThingType.GRILLED_SAUSAGE,
        ThingType.MARSHMALLOW to ThingType.TOASTED_MARSHMALLOW,
    )

    /** The stove fries; dough becomes pancakes. */
    fun stove(type: ThingType): ThingType? = if (type == ThingType.DOUGH) ThingType.PANCAKE else heat[type]

    /** The campfire grills and toasts. */
    fun fire(type: ThingType): ThingType? = heat[type]

    /**
     * The oven bakes everything inside it together. Dough with a partner becomes something special,
     * dough alone becomes a bun, and each other piece is baked on its own.
     */
    fun oven(contents: List<ThingType>): List<Pair<Made, String?>> {
        val out = mutableListOf<Pair<Made, String?>>()
        val rest = contents.toMutableList()
        while (ThingType.DOUGH in rest) {
            rest.remove(ThingType.DOUGH)
            val partner = rest.firstOrNull { it in ovenPairs }
            if (partner != null) {
                rest.remove(partner)
                out += ovenPairs.getValue(partner) to "oven_with_${partner.name}"
            } else {
                out += Made(ThingType.BUN) to "oven_DOUGH"
            }
        }
        for (type in rest) {
            out += when {
                type == ThingType.ICE_CREAM -> Made(ThingType.SLIME, 1) to null
                type in heat -> Made(heat.getValue(type)) to "fire_${type.name}"
                else -> Made(type) to null
            }
        }
        return out
    }

    private val ovenPairs: Map<ThingType, Made> = mapOf(
        ThingType.APPLE to Made(ThingType.CAKE),
        ThingType.STRAWBERRY to Made(ThingType.CUPCAKE, 2),
        ThingType.BANANA to Made(ThingType.CUPCAKE, 1),
        ThingType.EGG to Made(ThingType.WAFFLE),
        ThingType.BROWN_CHEESE to Made(ThingType.PIZZA),
        ThingType.MILK to Made(ThingType.BREAD),
        ThingType.DOUGH to Made(ThingType.COOKIE),
    )

    /** The blender turns fruit into a smoothie the colour of the fruit; anything else into slime. */
    fun blender(contents: List<ThingType>): Made {
        val fruits = contents.filter { it.fruit }
        if (fruits.isEmpty()) return Made(ThingType.SLIME, contents.sumOf { it.ordinal } % ThingType.SLIME.variants)
        if (fruits.size != contents.size) return Made(ThingType.SLIME, 3)
        val distinct = fruits.distinct()
        val color = if (distinct.size == 1) ThingType.FRUITS.indexOf(distinct[0]) else 6 + (distinct.sumOf { it.ordinal } % 2)
        return Made(ThingType.SMOOTHIE, color)
    }

    private fun pair(a: ThingType, b: ThingType) = if (a.ordinal <= b.ordinal) a to b else b to a

    private val cauldronTable: Map<Pair<ThingType, ThingType>, Made> = listOf(
        Triple(ThingType.APPLE, ThingType.FLOWER, Made(ThingType.POTION_GROW)),
        Triple(ThingType.MUSHROOM, ThingType.ROCK, Made(ThingType.POTION_SHRINK)),
        Triple(ThingType.FEATHER, ThingType.BALLOON, Made(ThingType.POTION_FLOAT)),
        Triple(ThingType.FLOWER, ThingType.GEM, Made(ThingType.POTION_RAINBOW)),
        Triple(ThingType.MILK, ThingType.LEAF, Made(ThingType.POTION_NORMAL)),
        Triple(ThingType.EGG, ThingType.GEM, Made(ThingType.DRAGON_EGG)),
        Triple(ThingType.ROCK, ThingType.WAND, Made(ThingType.GEM, 1)),
        Triple(ThingType.SHELL, ThingType.STARFISH, Made(ThingType.GEM, 4)),
        Triple(ThingType.LEAF, ThingType.FLOWER, Made(ThingType.FLOWER_CROWN)),
        Triple(ThingType.STICK, ThingType.GEM, Made(ThingType.WAND)),
        Triple(ThingType.COIN, ThingType.WAND, Made(ThingType.GIFT, 2)),
        Triple(ThingType.MUSHROOM, ThingType.FEATHER, Made(ThingType.WIZARD_HAT)),
        Triple(ThingType.STAR_JAR, ThingType.CANDLE, Made(ThingType.ROCKET)),
        Triple(ThingType.PINECONE, ThingType.COIN, Made(ThingType.CROWN)),
        Triple(ThingType.BOOT, ThingType.FLOWER, Made(ThingType.SUN_HAT)),
        Triple(ThingType.COCOA, ThingType.MARSHMALLOW, Made(ThingType.STAR_JAR)),
    ).associate { (a, b, made) -> pair(a, b) to made }

    /** The teddy and the wand bring a pet to life; the engine creates the animal. */
    fun cauldronMakesPet(a: ThingType, b: ThingType): Boolean = pair(a, b) == pair(ThingType.TEDDY, ThingType.WAND)

    /** Two things in the troll pot. Unknown pairs still make something, so every try is rewarded. */
    fun cauldron(a: ThingType, b: ThingType): Made {
        cauldronTable[pair(a, b)]?.let { return it }
        val mix = (a.ordinal * 31 + b.ordinal * 17) % 10
        return when (mix) {
            0, 1, 2 -> Made(ThingType.SLIME, mix)
            3, 4 -> Made(ThingType.BALLOON, mix)
            5 -> Made(ThingType.ROCKET)
            6 -> Made(ThingType.GEM, a.ordinal % ThingType.GEM.variants)
            7 -> Made(ThingType.STAR_JAR)
            else -> Made(ThingType.SLIME, 3 + mix % 2)
        }
    }

    private val workbenchTable: Map<Pair<ThingType, ThingType>, Made> = listOf(
        Triple(ThingType.PLANK, ThingType.PLANK, Made(ThingType.BIRDHOUSE)),
        Triple(ThingType.PLANK, ThingType.TIRE, Made(ThingType.TOY_CAR, 1)),
        Triple(ThingType.TIRE, ThingType.TIRE, Made(ThingType.TOY_CAR, 2)),
        Triple(ThingType.PLANK, ThingType.STICK, Made(ThingType.SLED)),
        Triple(ThingType.PLANK, ThingType.ROCK, Made(ThingType.DRUM)),
        Triple(ThingType.STICK, ThingType.STICK, Made(ThingType.GUITAR)),
        Triple(ThingType.PLANK, ThingType.TOY_CAR, Made(ThingType.SLED)),
        Triple(ThingType.TIRE, ThingType.STICK, Made(ThingType.SWIM_RING)),
    ).associate { (a, b, made) -> pair(a, b) to made }

    /**
     * The workbench builds what lies on it when a tool is dropped on it. One piece alone becomes a
     * smaller thing; unknown pairs still make a wooden toy car, so every build is rewarded.
     */
    fun workbench(contents: List<ThingType>): Made {
        if (contents.size >= 2) return workbenchTable[pair(contents[0], contents[1])] ?: Made(ThingType.TOY_CAR, 0)
        return when (contents.singleOrNull()) {
            ThingType.PLANK -> Made(ThingType.BIRDHOUSE)
            ThingType.TIRE -> Made(ThingType.SWIM_RING)
            ThingType.STICK -> Made(ThingType.WAND)
            else -> Made(ThingType.TOY_CAR, 0)
        }
    }

    /** Everything the discovery book shows, in book order. */
    val book: List<Recipe> by lazy {
        buildList {
            heat.forEach { (from, to) -> add(Recipe("fire_${from.name}", FixtureType.CAMPFIRE, listOf(from), Made(to))) }
            add(Recipe("stove_DOUGH", FixtureType.STOVE, listOf(ThingType.DOUGH), Made(ThingType.PANCAKE)))
            add(Recipe("oven_DOUGH", FixtureType.OVEN, listOf(ThingType.DOUGH), Made(ThingType.BUN)))
            ovenPairs.forEach { (partner, made) -> add(Recipe("oven_with_${partner.name}", FixtureType.OVEN, listOf(ThingType.DOUGH, partner), made)) }
            ThingType.FRUITS.forEachIndexed { index, fruit -> add(Recipe("blend_${fruit.name}", FixtureType.BLENDER, listOf(fruit), Made(ThingType.SMOOTHIE, index))) }
            cauldronTable.forEach { (inputs, made) -> add(Recipe("pot_${inputs.first.name}_${inputs.second.name}", FixtureType.CAULDRON, listOf(inputs.first, inputs.second), made)) }
            add(Recipe("pot_pet", FixtureType.CAULDRON, listOf(ThingType.TEDDY, ThingType.WAND), Made(ThingType.TEDDY)))
            add(Recipe("fire_DRAGON_EGG", FixtureType.CAMPFIRE, listOf(ThingType.DRAGON_EGG), Made(ThingType.DRAGON_EGG)))
            workbenchTable.forEach { (inputs, made) -> add(Recipe("bench_${inputs.first.name}_${inputs.second.name}", FixtureType.WORKBENCH, listOf(inputs.first, inputs.second), made)) }
        }
    }

    /** The book key for a machine result, or null when the result is not in the book. */
    fun keyFor(machine: FixtureType, inputs: List<ThingType>): String? {
        val sorted = inputs.sortedBy { it.ordinal }
        return when (machine) {
            FixtureType.CAMPFIRE -> inputs.singleOrNull()?.takeIf { it in heat || it == ThingType.DRAGON_EGG }?.let { "fire_${it.name}" }
            FixtureType.STOVE -> inputs.singleOrNull()?.let { if (it == ThingType.DOUGH) "stove_DOUGH" else if (it in heat) "fire_${it.name}" else null }
            FixtureType.OVEN -> oven(inputs).singleOrNull()?.second
            FixtureType.BLENDER -> inputs.distinct().singleOrNull()?.takeIf { it.fruit }?.let { "blend_${it.name}" }
            FixtureType.WORKBENCH -> if (sorted.size == 2 && workbenchTable.containsKey(pair(sorted[0], sorted[1]))) "bench_${sorted[0].name}_${sorted[1].name}" else null
            FixtureType.CAULDRON -> if (sorted.size == 2) {
                if (cauldronMakesPet(sorted[0], sorted[1])) "pot_pet"
                else cauldronTable[pair(sorted[0], sorted[1])]?.let { "pot_${sorted[0].name}_${sorted[1].name}" }
            } else null
            else -> null
        }
    }
}

/** The daily gift and other surprises. */
object Gifts {
    val pool: List<Made> = listOf(
        Made(ThingType.PARTY_HAT, 0), Made(ThingType.PARTY_HAT, 2), Made(ThingType.CROWN, 0), Made(ThingType.BEANIE, 2),
        Made(ThingType.CAP, 1), Made(ThingType.BOW, 0), Made(ThingType.STAR_GLASSES), Made(ThingType.SUNGLASSES),
        Made(ThingType.TEDDY), Made(ThingType.BALL), Made(ThingType.BALLOON, 3), Made(ThingType.DUCK),
        Made(ThingType.TOY_CAR, 1), Made(ThingType.DRUM), Made(ThingType.GUITAR), Made(ThingType.ROCKET),
        Made(ThingType.GEM, 0), Made(ThingType.GEM, 3), Made(ThingType.STAR_JAR), Made(ThingType.WAND),
        Made(ThingType.CUPCAKE, 3), Made(ThingType.LOLLIPOP, 1), Made(ThingType.ICE_CREAM, 4), Made(ThingType.WAFFLE),
        Made(ThingType.POTION_RAINBOW), Made(ThingType.POTION_FLOAT), Made(ThingType.DRAGON_EGG), Made(ThingType.VIKING_HELMET),
        Made(ThingType.GARMENT, Garment.pack(5, 6)), Made(ThingType.GARMENT, Garment.pack(2, 8)), Made(ThingType.SWIM_RING), Made(ThingType.PHONE),
    )

    /** The same day always gives the same gift, so it feels like a real delivery. */
    fun forDay(day: Long): Made = pool[(day * 7919 % pool.size).toInt().let { if (it < 0) it + pool.size else it }]

    /** What a wrapped gift found on the island holds. */
    fun surprise(seed: Int): Made = pool[(seed * 131 + 7).mod(pool.size)]
}
