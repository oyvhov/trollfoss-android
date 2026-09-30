package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Wishes, wandering animals and slapstick. */
class FunTest {
    private val events = ArrayList<Pair<Fx, Int>>()
    private val wishes = ArrayList<WishEvent>()

    private fun sim(world: World, seed: Int = 7) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            events += fx to param
        }
        override fun onWish(person: Person, event: WishEvent) {
            wishes += event
        }
    }, Random(seed))

    private fun step(sim: Sim, place: PlaceId, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun folk(world: World, place: PlaceId, name: String): Person =
        world.bodiesIn(place).first { it is Person && it.name == name } as Person

    @Test
    fun `a wish for a thing is granted by giving it`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val sondre = folk(world, PlaceId.CAFE, "Sondre")
        sondre.anim.wish = Wish(WishKind.THING, ThingType.COOKIE)
        val cookie = world.bodiesIn(PlaceId.CAFE).first { it is Thing && it.type == ThingType.COOKIE } as Thing
        assertEquals(Give.ATE, sim.give(sondre, cookie, Part.MOUTH))
        assertNull(sondre.anim.wish)
        assertEquals(1, world.wishesGranted)
        assertTrue(WishEvent.GRANTED in wishes)
    }

    @Test
    fun `figures start wishing on their own, but never too many at once`() {
        val world = WorldFactory.create(Random(4))
        val sim = sim(world)
        step(sim, PlaceId.HOME, 40f)
        assertTrue(WishEvent.NEW in wishes)
        val active = world.bodiesIn(PlaceId.HOME).count { it is Person && it.anim.wish != null }
        assertTrue(active <= Life.MAX_WISHES)
    }

    @Test
    fun `sitting on the whoopee cushion goes prrt`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        step(sim, PlaceId.HOME, 0.5f)
        val sofa = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.SOFA }
        val cushion = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.WHOOPEE }
        assertEquals(sofa.id, cushion.restOwner)
        val hedda = folk(world, PlaceId.HOME, "Hedda")
        hedda.mode = Mode.FREE
        hedda.holder = -1
        assertTrue(sim.seat(hedda, sofa, 1))
        assertTrue(events.any { it.first == Fx.PRRT && it.second == hedda.id })
    }

    @Test
    fun `a finished banana leaves a peel that makes you slip`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world)
        val tuva = folk(world, PlaceId.HOME, "Tuva")
        val banana = world.addThing(ThingType.BANANA, 0, PlaceId.HOME, tuva.x, tuva.y - 0.2f)
        repeat(ThingType.BANANA.bites) { sim.give(tuva, banana, Part.MOUTH) }
        val peel = world.bodiesIn(PlaceId.HOME).firstOrNull { it is Thing && it.type == ThingType.BANANA_PEEL }
        assertNotNull(peel)
        step(sim, PlaceId.HOME, 2f)
        // Drop Olve's grandma on the peel.
        val walker = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, peel!!.x, 0.3f)
        walker.ground = peel.y
        step(sim, PlaceId.HOME, 1.5f)
        assertTrue(events.any { it.first == Fx.SLIP && it.second == walker.id })
    }

    @Test
    fun `pepper makes a figure sneeze its hat off`() {
        val world = WorldFactory.create(Random(8))
        val sim = sim(world)
        val sondre = folk(world, PlaceId.CAFE, "Sondre")
        assertNotNull(world.worn(sondre, Slot.HEAD))
        val pepper = world.bodiesIn(PlaceId.CAFE).first { it is Thing && it.type == ThingType.PEPPER } as Thing
        assertEquals(Give.SNEEZE, sim.give(sondre, pepper, Part.MOUTH))
        step(sim, PlaceId.CAFE, 1f)
        assertTrue(events.any { it.first == Fx.ATSJO })
        assertNull(world.worn(sondre, Slot.HEAD))
    }

    @Test
    fun `three quick sips give hiccups`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val tuva = folk(world, PlaceId.HOME, "Tuva")
        val milk = world.addThing(ThingType.MILK, 0, PlaceId.HOME, tuva.x, tuva.y - 0.2f)
        repeat(3) { sim.give(tuva, milk, Part.MOUTH) }
        step(sim, PlaceId.HOME, 2f)
        assertTrue(events.any { it.first == Fx.HICCUP })
    }

    @Test
    fun `a cake thrown in the face splats`() {
        val world = WorldFactory.create(Random(10))
        val sim = sim(world)
        val sondre = folk(world, PlaceId.CAFE, "Sondre")
        val head = Anatomy.at(sondre, Part.HEAD)
        val cake = world.addThing(ThingType.CUPCAKE, 0, PlaceId.CAFE, head[0] - 0.12f, head[1] + 0.03f)
        cake.vx = 3f
        cake.vy = -0.2f
        step(sim, PlaceId.CAFE, 0.3f)
        assertTrue(events.any { it.first == Fx.SPLAT && it.second == sondre.id })
        assertTrue(sondre.anim.cream > 0f)
        assertFalse(world.bodies.containsKey(cake.id))
    }

    @Test
    fun `the dog eats a sausage left on the floor`() {
        val world = WorldFactory.create(Random(11))
        val sim = sim(world)
        val dog = world.addPerson(Species.DOG, Look(), 1f, PlaceId.HOME, 3.4f, 0.9f)
        dog.ground = 0.9f
        val sausage = world.addThing(ThingType.SAUSAGE, 0, PlaceId.HOME, 3.9f, 0.9f)
        sausage.ground = 0.9f
        step(sim, PlaceId.HOME, 16f)
        assertTrue(events.any { it.first == Fx.GOBBLE && it.second == dog.id })
        assertFalse(world.bodies.containsKey(sausage.id))
    }

    @Test
    fun `animals left alone stroll about`() {
        val world = WorldFactory.create(Random(12))
        val sim = sim(world)
        val cat = world.bodiesIn(PlaceId.HOME).first { it is Person && it.species == Species.CAT } as Person
        val start = cat.x to cat.ground
        step(sim, PlaceId.HOME, 25f)
        assertTrue(cat.x != start.first || cat.ground != start.second)
        assertTrue(cat.ground in PlaceId.HOME.back..PlaceId.FRONT)
    }
}
