package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipesTest {

    @Test
    fun `every book recipe has a unique key`() {
        val keys = Recipes.book.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `every book recipe really makes its result`() {
        for (r in Recipes.book) {
            val made: Made? = when (r.machine) {
                FixtureType.MI_COUNTER, FixtureType.MI_SAW_BENCH -> Recipes.houseWork(r.machine, r.inputs)
                FixtureType.CAMPFIRE -> if (r.inputs.single() == ThingType.DRAGON_EGG) Made(ThingType.DRAGON_EGG) else Recipes.fire(r.inputs.single())?.let { Made(it) }
                FixtureType.STOVE -> Recipes.stove(r.inputs.single())?.let { Made(it) }
                FixtureType.OVEN -> Recipes.oven(r.inputs).single().first
                FixtureType.BLENDER -> Recipes.blender(r.inputs)
                FixtureType.CAULDRON -> if (r.key == "pot_pet") Made(ThingType.TEDDY) else Recipes.cauldron(r.inputs[0], r.inputs[1])
                FixtureType.WORKBENCH -> Recipes.workbench(r.inputs)
                else -> null
            }
            assertEquals(r.key, r.result, made)
            assertEquals(r.key, r.key, Recipes.keyFor(r.machine, r.inputs))
        }
    }

    @Test
    fun `the cauldron always makes something, in either order`() {
        for (a in ThingType.entries) for (b in listOf(ThingType.APPLE, ThingType.ROCK, ThingType.WAND)) {
            assertEquals(Recipes.cauldron(a, b), Recipes.cauldron(b, a))
            assertNotNull(Recipes.cauldron(a, b))
        }
    }

    @Test
    fun `fruit smoothies take the fruit's own colour`() {
        ThingType.FRUITS.forEachIndexed { i, fruit ->
            assertEquals(Made(ThingType.SMOOTHIE, i), Recipes.blender(listOf(fruit, fruit)))
        }
        assertTrue(Recipes.blender(listOf(ThingType.ROCK)).type == ThingType.SLIME)
    }

    @Test
    fun `the daily gift is the same all day and always valid`() {
        for (day in 19000L..19400L) {
            val gift = Gifts.forDay(day)
            assertEquals(gift, Gifts.forDay(day))
            assertTrue(gift.type.variants >= 1)
        }
    }
}
