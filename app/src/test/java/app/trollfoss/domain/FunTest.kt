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
        val tuva = folk(world, PlaceId.CAFE, "Sondre")
        val banana = world.addThing(ThingType.BANANA, 0, PlaceId.CAFE, tuva.x, tuva.y - 0.2f)
        repeat(ThingType.BANANA.bites) { sim.give(tuva, banana, Part.MOUTH) }
        val peel = world.bodiesIn(PlaceId.CAFE).firstOrNull { it is Thing && it.type == ThingType.BANANA_PEEL }
        assertNotNull(peel)
        step(sim, PlaceId.CAFE, 2f)
        // Drop Olve's grandma on the peel.
        val walker = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.CAFE, peel!!.x, 0.3f)
        walker.ground = peel.y
        step(sim, PlaceId.CAFE, 1.5f)
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
        val tuva = folk(world, PlaceId.HOME, "Øyvind")
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

    @Test
    fun `folk go to bed by themselves at night and get up when day comes`() {
        val world = WorldFactory.create(Random(21))
        val sim = sim(world)
        val hedda = folk(world, PlaceId.HOME, "Hedda")
        // Hedda gets out of bed and stands in the room; nobody wishes for anything.
        hedda.mode = Mode.FREE
        hedda.holder = -1
        hedda.x = 1.8f
        hedda.y = 0.92f
        hedda.ground = 0.92f
        hedda.resting = false
        for (p in world.people()) p.anim.nextWish = 1e6f
        world.night = true
        step(sim, PlaceId.HOME, 90f)
        val bed = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.BED }
        assertEquals(Mode.SEATED, hedda.mode)
        assertEquals(bed.id, hedda.holder)
        assertEquals(1, hedda.anim.auto)
        assertTrue(events.any { it.first == Fx.SETTLE })
        world.night = false
        step(sim, PlaceId.HOME, 10f)
        assertEquals(Mode.FREE, hedda.mode)
        assertTrue(events.any { it.first == Fx.WAKE })
    }

    @Test
    fun `folk stroll about by themselves in daytime`() {
        val world = WorldFactory.create(Random(22))
        val sim = sim(world)
        val tuva = folk(world, PlaceId.HEILEBERGET, "Tuva")
        for (p in world.people()) p.anim.nextWish = 1e6f
        val start = tuva.x
        var moved = false
        var t = 0f
        while (t < 120f) {
            sim.step(PlaceId.HEILEBERGET, 1f / 60f)
            if (kotlin.math.abs(tuva.x - start) > 0.1f || tuva.mode == Mode.SEATED) moved = true
            t += 1f / 60f
        }
        assertTrue(moved)
    }

    // ------------------------------------------------------------------ Easter eggs

    private val eggs = ArrayList<String>()

    private fun eggSim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            events += fx to param
        }
        override fun onEgg(id: String) {
            eggs += id
        }
    }, Random(31))

    @Test
    fun `shaking the world makes everything jump and finds an egg`() {
        val world = WorldFactory.create(Random(40))
        val sim = eggSim(world)
        val apple = world.bodiesIn(PlaceId.CAFE).first { it is Thing && it.restOwner == -1 }
        sim.quake(PlaceId.CAFE)
        assertTrue(apple.vy < 0f)
        assertTrue(Fx.QUAKE in events.map { it.first })
        assertEquals(listOf("quake"), eggs)
        val stickers = world.stickers.size
        sim.quake(PlaceId.CAFE)
        assertEquals(stickers, world.stickers.size)
    }

    @Test
    fun `a crown on Rumle makes him king`() {
        val world = WorldFactory.create(Random(41))
        val sim = eggSim(world)
        val rumle = world.people().first { it.name == "Rumle" }
        val crown = world.addThing(ThingType.CROWN, 0, rumle.place, rumle.x, rumle.y)
        sim.give(rumle, crown, Part.HAT)
        assertTrue(Fx.KING in events.map { it.first })
        assertTrue("king" in world.eggs)
        val hedda = world.people().first { it.name == "Hedda" }
        val crown2 = world.addThing(ThingType.CROWN, 0, hedda.place, hedda.x, hedda.y)
        sim.give(hedda, crown2, Part.HAT)
        assertEquals(1, events.count { it.first == Fx.KING })
    }

    @Test
    fun `a duck in a running bath gets a party`() {
        val world = WorldFactory.create(Random(42))
        val sim = eggSim(world)
        val bath = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.BATH }
        sim.tap(PlaceId.HOME, bath, 0f, -0.05f)
        assertTrue(bath.on)
        val duck = world.addThing(ThingType.DUCK, 0, PlaceId.HOME, bath.x, bath.y - 0.3f)
        step(sim, PlaceId.HOME, 10f)
        assertTrue(Fx.DUCK in events.map { it.first })
        assertTrue("duck" in world.eggs)
        assertNotNull(duck)
    }

    @Test
    fun `the hidden tune on the piano rains stars`() {
        val world = WorldFactory.create(Random(43))
        val sim = eggSim(world)
        val piano = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.PIANO }
        val keys = piano.spec.dropZone!!
        for (key in Sim.TWINKLE) {
            val dx = keys.left + (key + 0.5f) / Sim.PIANO_KEYS * keys.width
            sim.tap(PlaceId.HOME, piano, dx, (keys.top + keys.bottom) / 2f)
            step(sim, PlaceId.HOME, 0.3f)
        }
        assertTrue(Fx.STARRAIN in events.map { it.first })
        assertTrue("twinkle" in world.eggs)
    }

    @Test
    fun `brunost makes the moose calf dance`() {
        val world = WorldFactory.create(Random(44))
        val sim = eggSim(world)
        val elk = world.bodiesIn(PlaceId.FOREST).first { it is Person && it.species == Species.ELK } as Person
        val cheese = world.addThing(ThingType.BROWN_CHEESE, 0, PlaceId.FOREST, elk.x, elk.y - 0.1f)
        sim.give(elk, cheese, Part.MOUTH)
        assertTrue(Fx.JIG in events.map { it.first })
        assertTrue("elk" in world.eggs)
    }
}