package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WorldTest {

    private fun step(sim: Sim, place: PlaceId, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    @Test
    fun `a new village has every place, fixture and named figure`() {
        val world = WorldFactory.create(Random(1))
        for (place in PlaceId.entries) {
            assertEquals(Places.spec(place).fixtures.size, world.fixturesIn(place).size)
            // The floors of the big house may stand empty; the house as a whole must have people.
            if (!place.big) assertTrue("people in $place", world.bodiesIn(place).any { it is Person })
        }
        assertTrue("people live in the big house", PlaceId.entries.filter { it.manor }.any { p -> world.bodiesIn(p).any { it is Person } })
        val names = world.people().filter { it.species == Species.FOLK }.map { it.name }
        assertTrue(names.none { it.isBlank() })
        assertEquals(names.size, names.toSet().size)
        assertTrue("Hedda" in names && "Øyvind" in names && "BesteSonja" in names && "Besten" in names)
    }

    @Test
    fun `fixtures stay inside their place and do not start outside the screen height`() {
        for (place in PlaceId.entries) {
            for (def in Places.spec(place).fixtures) {
                val spec = def.type.spec
                assertTrue("$place ${def.type} left", def.x - spec.w / 2 >= -0.001f)
                assertTrue("$place ${def.type} right", def.x + spec.w / 2 <= place.width + 0.001f)
                assertTrue("$place ${def.type} top", def.y - spec.h >= 0f)
                assertTrue("$place ${def.type} bottom", def.y <= 1f)
            }
        }
    }

    @Test
    fun `things settle on the surfaces below them`() {
        val world = WorldFactory.create(Random(2))
        val fridge = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.FRIDGE }
        val milk = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.MILK }
        assertEquals(fridge.id, milk.inside)
        assertEquals(fridge.y - 0.02f, milk.y, 0.001f)
        val pillow = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.PILLOW }
        val bed = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.BED }
        assertEquals(bed.id, pillow.restOwner)
        assertEquals(bed.y - 0.105f, pillow.y, 0.001f)
    }

    @Test
    fun `a thing let go on the floor band stays at its own depth`() {
        val world = WorldFactory.create(Random(4))
        val sim = Sim(world)
        val back = world.addThing(ThingType.APPLE, 0, PlaceId.HOME, 3.9f, 0.82f)
        val front = world.addThing(ThingType.APPLE, 0, PlaceId.HOME, 3.95f, 0.95f)
        back.ground = 0.82f
        front.ground = 0.95f
        back.y = 0.5f
        front.y = 0.5f
        repeat(240) { sim.step(PlaceId.HOME, 1f / 60f) }
        assertTrue(back.resting && front.resting)
        assertEquals(0.82f, back.y, 0.002f)
        assertEquals(0.95f, front.y, 0.002f)
    }

    @Test
    fun `a dropped apple falls to the floor and rests`() {
        val world = WorldFactory.create(Random(3))
        val sim = Sim(world)
        val apple = world.addThing(ThingType.APPLE, 0, PlaceId.HOME, 1.9f, 0.3f)
        step(sim, PlaceId.HOME, 3f)
        assertTrue(apple.resting)
        assertEquals(PlaceId.HOME.floor, apple.y, 0.002f)
    }

    @Test
    fun `things inside a shut fridge stay hidden and still`() {
        val world = WorldFactory.create(Random(4))
        val sim = Sim(world)
        val egg = world.bodiesIn(PlaceId.HOME).first { it is Thing && it.type == ThingType.EGG }
        val y = egg.y
        step(sim, PlaceId.HOME, 1f)
        assertEquals(y, egg.y, 0.0001f)
    }

    @Test
    fun `a figure dropped near a chair sits on it`() {
        val world = WorldFactory.create(Random(5))
        val sim = Sim(world)
        val chair = world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.CHAIR && world.seatedAt(it, 0) == null }
        val kid = world.addPerson(Species.FOLK, Look(height = 0.78f), 1.2f, PlaceId.HOME, chair.x, chair.y - 0.08f)
        val seat = sim.freeSeatNear(PlaceId.HOME, kid, kid.x, kid.y + kid.h * Anatomy.HIPS - 0.02f, 0.13f)
        assertNotNull(seat)
        assertTrue(sim.seat(kid, seat!!.first, seat.second))
        assertEquals(Mode.SEATED, kid.mode)
        assertEquals(Pose.SIT, kid.anim.pose)
    }

    @Test
    fun `eating takes bites until the food is gone`() {
        val world = WorldFactory.create(Random(6))
        val sim = Sim(world)
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.9f, 0.9f)
        val apple = world.addThing(ThingType.APPLE, 0, PlaceId.HOME, 1.9f, 0.5f)
        assertEquals(Give.ATE, sim.give(p, apple, Part.MOUTH))
        assertEquals(Give.ATE, sim.give(p, apple, Part.MOUTH))
        assertEquals(Give.FINISHED, sim.give(p, apple, Part.MOUTH))
        assertNull(world.bodies[apple.id])
    }

    @Test
    fun `a new hat replaces the old one, which falls off`() {
        val world = WorldFactory.create(Random(7))
        val sim = Sim(world)
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.9f, 0.9f)
        val cap = world.addThing(ThingType.CAP, 0, PlaceId.HOME, 1.9f, 0.9f)
        val crown = world.addThing(ThingType.CROWN, 0, PlaceId.HOME, 1.9f, 0.9f)
        sim.give(p, cap, Part.HAT)
        sim.give(p, crown, Part.HAT)
        assertEquals(crown, world.worn(p, Slot.HEAD))
        assertEquals(Mode.FREE, cap.mode)
    }

    @Test
    fun `a garment swaps tops so the old one can be given back`() {
        val world = WorldFactory.create(Random(8))
        val sim = Sim(world)
        val p = world.addPerson(Species.FOLK, Look(top = 0, topColor = 2), 1f, PlaceId.SALON, 1f, 0.9f)
        val sweater = world.addThing(ThingType.GARMENT, Garment.pack(5, 6), PlaceId.SALON, 1f, 0.9f)
        assertEquals(Give.DRESSED, sim.give(p, sweater, Part.BODY))
        assertEquals(5, p.look.top)
        assertEquals(6, p.look.topColor)
        assertEquals(0, Garment.style(sweater.variant))
        assertEquals(2, Garment.color(sweater.variant))
    }

    @Test
    fun `things float in the sea and stones sink`() {
        val world = WorldFactory.create(Random(9))
        val sim = Sim(world)
        val duck = world.addThing(ThingType.DUCK, 0, PlaceId.BEACH, 3.2f, 0.5f)
        val rock = world.addThing(ThingType.ROCK, 0, PlaceId.BEACH, 3.3f, 0.5f)
        step(sim, PlaceId.BEACH, 6f)
        val water = Places.spec(PlaceId.BEACH).water!!
        assertTrue(duck.y < water.line + duck.h)
        assertEquals(water.bottom, rock.y, 0.01f)
    }

    @Test
    fun `nothing falls in the space station until gravity is switched on`() {
        val world = WorldFactory.create(Random(10))
        val sim = Sim(world)
        val planet = world.bodiesIn(PlaceId.SPACE).first { it is Thing && it.type == ThingType.PLANET }
        step(sim, PlaceId.SPACE, 2f)
        assertTrue(planet.y < 0.85f)
        val lever = world.fixturesIn(PlaceId.SPACE).first { it.type == FixtureType.GRAVITY_LEVER }
        sim.tap(PlaceId.SPACE, lever, 0f, -0.1f)
        assertFalse(sim.zeroG(PlaceId.SPACE))
        step(sim, PlaceId.SPACE, 4f)
        assertEquals(PlaceId.SPACE.floor, planet.y, 0.01f)
    }

    @Test
    fun `the oven bakes dough and apple into a cake and opens`() {
        val world = WorldFactory.create(Random(11))
        val sim = Sim(world)
        val oven = world.fixturesIn(PlaceId.CAFE).first { it.type == FixtureType.OVEN }
        sim.tap(PlaceId.CAFE, oven, 0f, -0.1f)
        assertTrue("door opens", oven.open)
        val dough = world.addThing(ThingType.DOUGH, 0, PlaceId.CAFE, oven.x, oven.y - 0.1f)
        val apple = world.addThing(ThingType.APPLE, 0, PlaceId.CAFE, oven.x + 0.02f, oven.y - 0.1f)
        step(sim, PlaceId.CAFE, 1f)
        assertEquals(oven.id, dough.inside)
        assertEquals(oven.id, apple.inside)
        sim.tap(PlaceId.CAFE, oven, 0f, -0.1f)
        sim.tap(PlaceId.CAFE, oven, 0f, -0.22f)
        assertTrue("baking", oven.on)
        step(sim, PlaceId.CAFE, 3f)
        assertTrue("opens after baking", oven.open)
        assertTrue("cake: " + world.bodies.values.filterIsInstance<Thing>().filter { kotlin.math.abs(it.x - oven.x) < 0.15f }.map { "${it.type} in=${it.inside} y=${it.y} m=${it.mode} p=${it.place}" } + " oven=${oven.id} on=${oven.on} t=${oven.timer}", world.bodiesIn(PlaceId.CAFE).any { it is Thing && it.type == ThingType.CAKE && it.inside == oven.id })
        assertTrue("discovered", "oven_with_APPLE" in world.discoveries)
    }

    @Test
    fun `the workbench builds a birdhouse from two planks with a hammer`() {
        val world = WorldFactory.create(Random(12))
        val sim = Sim(world)
        val bench = world.fixturesIn(PlaceId.FARM).first { it.type == FixtureType.WORKBENCH }
        val a = world.addThing(ThingType.PLANK, 0, PlaceId.FARM, bench.x, 0.3f)
        val b = world.addThing(ThingType.PLANK, 0, PlaceId.FARM, bench.x, 0.3f)
        assertTrue(sim.dropInto(PlaceId.FARM, bench, a))
        assertTrue(sim.dropInto(PlaceId.FARM, bench, b))
        val hammer = world.addThing(ThingType.HAMMER, 0, PlaceId.FARM, bench.x, 0.3f)
        assertFalse(sim.dropInto(PlaceId.FARM, bench, hammer))
        assertTrue(world.bodiesIn(PlaceId.FARM).any { it is Thing && it.type == ThingType.BIRDHOUSE })
        assertTrue("farm_build" in world.unlocked)
    }

    @Test
    fun `every glimt can appear and be collected`() {
        val world = WorldFactory.create(Random(13))
        val sim = Sim(world)
        for (s in Secrets.all) {
            if (s.event) sim.unlock(s.id)
            if (s.inside >= 0) world.fixturesIn(s.place)[s.inside].open = true
            assertTrue("${s.id} visible", sim.visibleSecrets(s.place).any { it.id == s.id })
            assertTrue(sim.collect(s.id))
        }
        assertTrue(world.allSecretsFound())
        assertTrue(Secrets.all.size >= 60)
        // Every place has at least three glimt, except the two floors of the child's own house (their glimt wait in the yard).`n        for (place in PlaceId.entries) {`n            val n = Secrets.inPlace(place).size`n            if (place == PlaceId.MINE_GROUND || place == PlaceId.MINE_UPPER) assertEquals("no glimt in $place", 0, n) else assertTrue("at least three glimt in $place", n >= 3)`n        }
    }

    @Test
    fun `the save file keeps figures, things, fixtures and finds`() {
        val world = WorldFactory.create(Random(14))
        val sim = Sim(world)
        world.found += "home_pillow"
        world.discoveries += "oven_DOUGH"
        world.night = true
        world.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.FRIDGE }.open = true
        val hedda = world.people().first { it.name == "Hedda" }
        hedda.look = hedda.look.copy(hairColor = 7)
        val cap = world.addThing(ThingType.CAP, 2, PlaceId.HOME, 1f, 0.9f)
        sim.give(hedda, cap, Part.HAT)
        val json = WorldStore.encode(world, Settings(music = false, maalform = Maalform.BOKMAAL)).toString()
        val saved = WorldStore.decode(org.json.JSONObject(json))
        val w2 = saved.world
        assertEquals(world.bodies.size, w2.bodies.size)
        assertTrue(w2.night)
        assertTrue("home_pillow" in w2.found)
        assertTrue("oven_DOUGH" in w2.discoveries)
        assertTrue(w2.fixturesIn(PlaceId.HOME).first { it.type == FixtureType.FRIDGE }.open)
        val h2 = w2.people().first { it.name == "Hedda" }
        assertEquals(7, h2.look.hairColor)
        assertEquals(ThingType.CAP, w2.worn(h2, Slot.HEAD)?.type)
        assertFalse(saved.settings.music)
        assertEquals(Maalform.BOKMAAL, saved.settings.maalform)
    }
}
