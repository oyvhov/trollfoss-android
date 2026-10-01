package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/** Storhuset's garden, Hagen: the blueprint, the rules, the gnomes, the key and the save file. */
class HouseGardenTest {
    private val place = PlaceId.MANOR_GARDEN
    private val events = ArrayList<Pair<Int, Int>>()
    private val splashes = ArrayList<Body>()
    private val secrets = ArrayList<String>()
    private val spawned = ArrayList<Thing>()

    @Before
    fun before() = GardenView.reset()

    @After
    fun after() = GardenView.reset()

    private fun sim(world: World, seed: Int = 3) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            if (fx == Fx.HOUSE) events += HouseFx.code(param) to HouseFx.arg(param)
        }

        override fun onSplash(body: Body, speed: Float) {
            splashes += body
        }

        override fun onSecret(id: String) {
            secrets += id
        }

        override fun onSpawn(body: Body) {
            if (body is Thing) spawned += body
        }
    }, Random(seed))

    private fun fix(world: World, ix: Int) = world.fixtures.getValue(place.idBase + ix)

    private fun run(sim: Sim, seconds: Float, where: PlaceId = place) {
        var t = 0f
        while (t < seconds) {
            sim.step(where, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun heard(code: Int, arg: Int? = null) = events.any { it.first == code && (arg == null || it.second == arg) }

    private fun kid(world: World, x: Float, y: Float = place.floor) =
        world.addPerson(Species.FOLK, Look(), 1f, place, x, y, "Kid")

    // ------------------------------------------------------------------ the blueprint

    @Test
    fun `the blueprint has the indices the rules and the art rely on`() {
        val defs = Places.spec(place).fixtures
        assertEquals(GardenIx.COUNT, defs.size)
        assertTrue("a dense garden", defs.size in 30..50)
        assertEquals(FixtureType.DOOR, defs[GardenIx.DOOR].type)
        assertEquals(FixtureType.GA_GREENHOUSE, defs[GardenIx.GREENHOUSE].type)
        for (i in 0..2) assertEquals(FixtureType.GA_PLANTER, defs[GardenIx.PLANTER0 + i].type)
        for (i in 0 until GardenIx.FROGS) assertEquals(FixtureType.GA_FROG, defs[GardenIx.FROG0 + i].type)
        for (i in 0 until GardenIx.GNOMES) assertEquals(FixtureType.GA_GNOME, defs[GardenIx.GNOME0 + i].type)
        assertEquals(FixtureType.GA_SHED, defs[GardenIx.SHED].type)
        assertEquals(FixtureType.GA_COMPOST, defs[GardenIx.COMPOST].type)
        assertEquals(FixtureType.GA_TREEHOUSE, defs[GardenIx.TREEHOUSE].type)
        assertEquals(FixtureType.GA_LADDER, defs[GardenIx.LADDER].type)
        assertEquals(FixtureType.GA_SWING, defs[GardenIx.SWING].type)
        assertEquals(FixtureType.GA_ZIP, defs[GardenIx.ZIP].type)
        assertEquals(FixtureType.GA_ZIP_POLE, defs[GardenIx.ZIP_POLE].type)
        assertEquals(FixtureType.GA_SANDBOX, defs[GardenIx.SANDBOX].type)
        assertEquals(FixtureType.GA_HAMMOCK, defs[GardenIx.HAMMOCK].type)
        assertEquals(FixtureType.GA_GRILL, defs[GardenIx.GRILL].type)
        assertEquals(FixtureType.GA_SNOWMAN, defs[GardenIx.SNOWMAN].type)
        assertEquals(FixtureType.GA_BRIDGE, defs[GardenIx.BRIDGE].type)
        assertEquals(FixtureType.GA_MOWER, defs[GardenIx.MOWER].type)
        assertEquals(FixtureType.GA_GATE, defs[GardenIx.GATE].type)
        assertEquals(FixtureType.LAMP_POST, defs[GardenIx.LAMP0].type)
        assertTrue(defs.all { it.x in -0.01f..place.width })
        assertTrue(Places.spec(place).water != null)
    }

    @Test
    fun `the garden keeps the contract of the house`() {
        val back = House.passages.single { it.id == "garden-house-door" }
        assertEquals(place, back.place)
        assertEquals(PlaceId.MANOR_GROUND, back.to)
        assertEquals("garden-door", back.arrive)
        assertEquals(FixtureType.DOOR, Places.spec(place).fixtures[back.fixture].type)
        val names = GardenFloor.arrivals.map { it.name }
        assertTrue("house-door" in names)
        assertTrue("slide-end" in names)
        assertTrue(GardenFloor.hangouts(false).isNotEmpty())
        assertTrue(GardenFloor.hangouts(true).isNotEmpty())
        assertTrue(GardenFloor.darkness == 0f)
    }

    @Test
    fun `every garden fixture type has a spec of its own`() {
        for (type in FixtureType.entries.filter { it.name.startsWith("GA_") }) {
            assertNotNull("$type has a spec", GardenFloor.specOf(type))
            assertTrue(type.spec.w > 0f && type.spec.h > 0f)
        }
    }

    @Test
    fun `whoever slides down from the balcony lands in the pond with a splash`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        val p = House.passages.single { it.id == "upper-balcony-slide" }
        val slide = world.fixtures.getValue(p.place.idBase + p.fixture)
        val rider = world.addPerson(Species.FOLK, Look(), 1f, p.place, slide.x, p.place.floor, "Rider")
        sim.tap(p.place, slide, 0f, -0.1f)
        assertEquals(place, rider.place)
        run(sim, 3f)
        assertTrue("a splash", splashes.contains(rider))
        assertTrue("in the pond", rider.x in GardenLayout.POND_X1..GardenLayout.POND_X2)
        assertNotNull(sim.poolAt(rider.x, rider.y))
    }

    // ------------------------------------------------------------------ greenhouse

    @Test
    fun `a bed grows from seed to a giant vegetable that can be sat in and then harvested`() {
        val world = WorldFactory.create(Random(2))
        val sim = sim(world)
        sim.step(place, 0.016f)
        val bed = fix(world, GardenIx.PLANTER1)
        assertEquals(0, bed.mode)
        assertFalse(bed.open)
        val rider = kid(world, bed.x)
        // A seat that is not there yet.
        val near = sim.freeSeatNear(place, rider, bed.x, bed.y, 0.6f)
        assertTrue(near == null || near.first !== bed)
        sim.tap(place, bed, 0f, -0.1f)
        assertEquals(1, bed.mode)
        run(sim, 0.5f)
        assertTrue(heard(GardenFxCode.GROW, 1))
        sim.tap(place, bed, 0f, -0.1f)
        run(sim, 1f)
        assertEquals(2, bed.mode)
        sim.tap(place, bed, 0f, -0.1f)
        run(sim, 1f)
        assertEquals(3, bed.mode)
        assertTrue(bed.open)
        assertTrue(heard(GardenFxCode.GIANT))
        assertTrue("garden_greenhouse" in secrets)
        // Now there is a seat.
        assertTrue(sim.seat(rider, bed, 0))
        sim.tap(place, bed, 0f, -0.1f)
        assertTrue(heard(GardenFxCode.WOBBLE))
        assertEquals(3, bed.mode)
        // Whoever sits there gets up; then a tap harvests three vegetables and the bed starts over.
        rider.mode = Mode.FREE
        rider.holder = -1
        val before = world.bodiesIn(place).count { it is Thing && it.type == ThingType.GA_VEGGIE }
        sim.tap(place, bed, 0f, -0.1f)
        assertEquals(before + 3, world.bodiesIn(place).count { it is Thing && it.type == ThingType.GA_VEGGIE })
        assertEquals(1, bed.mode)
        assertFalse(bed.open)
    }

    @Test
    fun `plants grow by themselves in the day and rest at night`() {
        val world = WorldFactory.create(Random(2))
        val sim = sim(world)
        val bed = fix(world, GardenIx.PLANTER0)
        sim.tap(place, bed, 0f, -0.1f)
        world.night = true
        run(sim, GardenLayout.GROW_SECONDS * 3)
        assertEquals("plants sleep at night", 1, bed.mode)
        world.night = false
        run(sim, GardenLayout.GROW_SECONDS * 2 + 1f)
        assertEquals(3, bed.mode)
    }

    @Test
    fun `seeds and a watering can can be dropped on a bed`() {
        val world = WorldFactory.create(Random(2))
        val sim = sim(world)
        val bed = fix(world, GardenIx.PLANTER2)
        val seeds = world.addThing(ThingType.SEEDS, 0, place, bed.x, bed.y - 0.1f)
        assertTrue(sim.dropInto(place, bed, seeds))
        assertEquals(1, bed.mode)
        assertFalse(world.bodies.containsKey(seeds.id))
        val can = world.addThing(ThingType.WATERING_CAN, 0, place, bed.x, bed.y - 0.1f)
        assertFalse("the can falls on as usual", sim.dropInto(place, bed, can))
        run(sim, 1f)
        assertEquals(2, bed.mode)
    }

    @Test
    fun `the greenhouse mist sows and waters every bed`() {
        val world = WorldFactory.create(Random(2))
        val sim = sim(world)
        val house = fix(world, GardenIx.GREENHOUSE)
        sim.tap(place, house, 0f, -0.2f)
        run(sim, 2f)
        for (i in 0..2) assertEquals(1, fix(world, GardenIx.PLANTER0 + i).mode)
        sim.tap(place, house, 0f, -0.2f)
        sim.tap(place, house, 0f, -0.2f)
        run(sim, 2f)
        for (i in 0..2) assertEquals(3, fix(world, GardenIx.PLANTER0 + i).mode)
        assertTrue(heard(GardenFxCode.MIST))
    }

    // ------------------------------------------------------------------ pond

    @Test
    fun `three different frogs make a choir that counts for the task and brings out a glimt`() {
        val world = WorldFactory.create(Random(4))
        val sim = sim(world)
        world.taskSet += listOf("garden_choir", "garden_giant", "garden_zip")
        for (i in 0..2) sim.tap(place, fix(world, GardenIx.FROG0 + i), 0f, -0.03f)
        assertTrue(heard(GardenFxCode.CROAK, 0))
        assertTrue(heard(GardenFxCode.CROAK, 2))
        assertTrue(heard(GardenFxCode.CHOIR))
        assertTrue("garden_pond" in secrets)
        val task = TaskBook.ALL.single { it.id == "garden_choir" }
        assertTrue(sim.tasks.done(task))
        run(sim, 4f)
        assertTrue("the whole tune is sung", events.count { it.first == GardenFxCode.CROAK } >= 3 + GardenRules.CHOIR_TUNE.size)
    }

    @Test
    fun `one frog croaking again and again is no choir`() {
        val world = WorldFactory.create(Random(4))
        val sim = sim(world)
        repeat(6) { sim.tap(place, fix(world, GardenIx.FROG0), 0f, -0.03f) }
        assertFalse(heard(GardenFxCode.CHOIR))
    }

    @Test
    fun `things float on the pond and the ducks start there`() {
        val world = WorldFactory.create(Random(4))
        val sim = sim(world)
        val ducks = world.bodiesIn(place).filter { it is Thing && it.type == ThingType.DUCK }
        assertEquals(2, ducks.size)
        for (d in ducks) assertTrue("a duck floats at the waterline", d.y < GardenLayout.POND_BED)
        run(sim, 1f)
        for (d in ducks) assertNotNull(sim.poolAt(d.x, d.y))
    }

    // ------------------------------------------------------------------ the key and the compost heap

    @Test
    fun `the third mouthful makes the compost burp up the golden key`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val heap = fix(world, GardenIx.COMPOST)
        assertFalse("manor_key_garden" in world.flags)
        repeat(2) { assertTrue(sim.dropInto(place, heap, world.addThing(ThingType.APPLE, 0, place, heap.x, heap.y - 0.2f))) }
        run(sim, 1f)
        assertFalse("not yet", "manor_key_garden" in world.flags)
        assertTrue(sim.dropInto(place, heap, world.addThing(ThingType.BREAD, 0, place, heap.x, heap.y - 0.2f)))
        run(sim, 1f)
        assertTrue(heard(GardenFxCode.COMPOST, 2))
        assertTrue(heard(GardenFxCode.COMPOST, 3))
        assertTrue("manor_key_garden" in world.flags)
        assertEquals(1, HouseKeys.found(world))
        assertTrue(world.bodiesIn(place).any { it is Thing && it.type == ThingType.GOLDEN_KEY })
        assertEquals(1, heap.mode)
    }

    @Test
    fun `the compost heap hands back what it cannot eat, and later gives seeds instead of a second key`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val heap = fix(world, GardenIx.COMPOST)
        val ball = world.addThing(ThingType.BALL, 0, place, heap.x, heap.y - 0.2f)
        assertTrue(sim.dropInto(place, heap, ball))
        assertTrue(world.bodies.containsKey(ball.id))
        assertEquals(0, heap.count)
        assertTrue(heard(GardenFxCode.COMPOST, 4))
        world.flags += "manor_key_garden"
        repeat(3) { sim.dropInto(place, heap, world.addThing(ThingType.CARROT, 0, place, heap.x, heap.y - 0.2f)) }
        run(sim, 1f)
        assertEquals(1, HouseKeys.found(world))
        assertEquals(0, world.bodiesIn(place).count { it is Thing && it.type == ThingType.GOLDEN_KEY })
        assertTrue(heard(GardenFxCode.COMPOST, 5))
    }

    // ------------------------------------------------------------------ gnomes

    private fun gnomeState(world: World, depth: Boolean = true) = (0 until GardenIx.GNOMES).map {
        val g = fix(world, GardenIx.GNOME0 + it)
        listOf(g.x, g.y, if (depth) g.depth else 0f, g.mode.toFloat(), g.count.toFloat())
    }

    @Test
    fun `the gnomes start in their homes, five of them, each in its own spot`() {
        val world = WorldFactory.create(Random(6))
        val slots = (0 until GardenIx.GNOMES).map { GardenGnomes.slotOf(fix(world, GardenIx.GNOME0 + it)) }
        assertEquals(GardenGnomes.homes.toList(), slots)
        assertEquals(slots.size, slots.toSet().size)
        assertTrue(GardenGnomes.spots.size >= 12)
    }

    @Test
    fun `gnomes never move where somebody is looking`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world, seed = 11)
        GardenView.set(3.0f, 5.0f)
        val watched = fix(world, GardenIx.GNOME0 + 2)
        assertTrue("the gnome at the pond is on screen", !GardenView.hidden(watched.x))
        val x0 = watched.x
        run(sim, 240f)
        assertEquals(x0, watched.x, 0.0001f)
        assertEquals(0, watched.mode)
        val moved = (0 until GardenIx.GNOMES).count { fix(world, GardenIx.GNOME0 + it).mode > 0 }
        assertTrue("somebody sneaked off in all that time", moved > 0)
        for (i in 0 until GardenIx.GNOMES) {
            val g = fix(world, GardenIx.GNOME0 + i)
            if (g.mode > 0) assertTrue("a gnome that moved went out of sight", GardenView.hidden(g.x))
        }
        val slots = (0 until GardenIx.GNOMES).map { GardenGnomes.slotOf(fix(world, GardenIx.GNOME0 + it)) }
        assertEquals("never two in one spot", slots.size, slots.toSet().size)
    }

    @Test
    fun `with everything in view the gnomes stay where they are`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world, seed = 11)
        val before = gnomeState(world, depth = false)
        run(sim, 240f)
        assertEquals(before, gnomeState(world, depth = false))
    }

    @Test
    fun `what the gnomes do is the same every time for the same game`() {
        fun play(): List<List<Float>> {
            GardenView.reset()
            val world = WorldFactory.create(Random(6))
            val sim = sim(world, seed = 7)
            GardenView.set(3.0f, 5.0f)
            run(sim, 200f)
            // Away for a while, then back.
            run(sim, 5f, PlaceId.MANOR_GROUND)
            GardenView.reset()
            run(sim, 5f)
            return gnomeState(world)
        }
        assertEquals(play(), play())
    }

    @Test
    fun `back from a visit elsewhere the gnomes have been up to something, but not on the very first visit`() {
        val world = WorldFactory.create(Random(8))
        val sim = sim(world, seed = 5)
        val first = gnomeState(world, depth = false)
        run(sim, 3f)
        assertEquals("the first visit shows the garden as planned", first, gnomeState(world, depth = false))
        assertTrue("garden_visited" in world.flags)
        run(sim, 6f, PlaceId.MANOR_GROUND)
        run(sim, 0.1f)
        assertTrue("some gnome moved while we were away", gnomeState(world, depth = false) != first)
        val slots = (0 until GardenIx.GNOMES).map { GardenGnomes.slotOf(fix(world, GardenIx.GNOME0 + it)) }
        assertEquals(slots.size, slots.toSet().size)
    }

    @Test
    fun `a tapped gnome winks, its neighbours wink back, and three gnomes bring out a glimt`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world)
        for (i in 0..2) sim.tap(place, fix(world, GardenIx.GNOME0 + i), 0f, -0.05f)
        assertTrue(heard(GardenFxCode.WINK, 0))
        assertTrue(heard(GardenFxCode.WINK, 2))
        assertTrue("garden_gnome" in secrets)
        assertTrue("garden_gnome_1" in world.flags)
    }

    @Test
    fun `a gnome that is tickled with five taps pops away and turns up somewhere else`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world)
        val g = fix(world, GardenIx.GNOME0)
        val x0 = g.x
        repeat(5) { sim.tap(place, g, 0f, -0.05f) }
        assertTrue(heard(GardenFxCode.POOF))
        assertTrue(g.x != x0)
        assertTrue(g.mode > 0)
    }

    @Test
    fun `a gnome on the shed roof sits on the shed roof and is drawn just in front of the shed`() {
        val world = WorldFactory.create(Random(6))
        val sim = sim(world)
        val shed = fix(world, GardenIx.SHED)
        val g = fix(world, GardenIx.GNOME0 + 1)
        g.mode = 2
        assertEquals(1, GardenGnomes.slotOf(g))
        assertEquals(GnomeKind.SIT, GardenGnomes.kindOf(g))
        sim.step(place, 0.016f)
        assertTrue("sorted just in front of the shed", g.depth > shed.depth && g.depth < shed.depth + 0.01f)
    }

    // ------------------------------------------------------------------ the treehouse: ladder, swing, zip line

    @Test
    fun `the rope ladder takes whoever stands at its foot up to the deck and whoever is on the deck down again`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        val ladder = fix(world, GardenIx.LADDER)
        val tree = fix(world, GardenIx.TREEHOUSE)
        val climber = kid(world, ladder.x)
        run(sim, 1f)
        assertEquals(-1, climber.restOwner)
        sim.tap(place, ladder, 0f, -0.1f)
        assertEquals(Mode.SEATED, climber.mode)
        run(sim, GardenLayout.CLIMB_SECONDS + 1.2f)
        assertEquals(Mode.FREE, climber.mode)
        assertEquals("standing on the deck", tree.id, climber.restOwner)
        assertTrue(climber.y < place.floor - 0.3f)
        sim.tap(place, ladder, 0f, -0.1f)
        assertEquals(Mode.SEATED, climber.mode)
        run(sim, GardenLayout.CLIMB_SECONDS + 1.5f)
        assertEquals(Mode.FREE, climber.mode)
        assertEquals("back on the grass", -1, climber.restOwner)
        assertEquals(ladder.x, climber.x, 0.1f)
    }

    @Test
    fun `a rider on the tyre swing swings and keeps swinging`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        val swing = fix(world, GardenIx.SWING)
        val rider = kid(world, swing.x)
        assertTrue(sim.seat(rider, swing, 0))
        sim.tap(place, swing, 0f, -0.2f)
        var widest = 0f
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var t = 0f
        while (t < 8f) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
            widest = maxOf(widest, kotlin.math.abs(swing.angle))
            minX = minOf(minX, rider.x)
            maxX = maxOf(maxX, rider.x)
        }
        assertTrue("it swings out", widest > 0.35f)
        assertTrue("the rider goes with it", maxX - minX > 0.2f)
        assertEquals(Mode.SEATED, rider.mode)
    }

    @Test
    fun `an empty swing comes to rest`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        val swing = fix(world, GardenIx.SWING)
        sim.tap(place, swing, 0f, -0.2f)
        run(sim, 25f)
        assertEquals(0f, swing.angle, 0.0001f)
    }

    @Test
    fun `the zip line takes a rider across the garden and drops them in the pond`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        world.taskSet += listOf("garden_zip", "garden_choir", "garden_giant")
        val zip = fix(world, GardenIx.ZIP)
        val rider = kid(world, zip.x, 0.3f)
        assertTrue(sim.seat(rider, zip, 0))
        sim.tap(place, zip, 0f, -0.2f)
        assertEquals(1, zip.mode)
        run(sim, GardenLayout.ZIP_SECONDS / 2f)
        assertTrue("halfway", zip.shiftX < -0.5f && zip.shiftX > GardenLayout.ZIP_X1 - GardenLayout.ZIP_X0 + 0.5f)
        assertEquals(Mode.SEATED, rider.mode)
        assertTrue("the rider hangs from the carriage", kotlin.math.abs(rider.x - (zip.x + zip.shiftX)) < 0.01f)
        run(sim, GardenLayout.ZIP_SECONDS / 2f + 0.3f)
        assertEquals(Mode.FREE, rider.mode)
        run(sim, 1.5f)
        assertTrue("splash", splashes.contains(rider))
        assertTrue(rider.x in GardenLayout.POND_X1..GardenLayout.POND_X2)
        assertTrue("garden_tree" in secrets)
        assertTrue(sim.tasks.done(TaskBook.ALL.single { it.id == "garden_zip" }))
        run(sim, GardenRules.ZIP_BACK_SECONDS + 0.5f)
        assertEquals("the carriage is back at the start", 0, zip.mode)
        assertEquals(0f, zip.shiftX, 0.0001f)
    }

    @Test
    fun `a tap on the zip line with a figure standing on the deck sets them off`() {
        val world = WorldFactory.create(Random(7))
        val sim = sim(world)
        val zip = fix(world, GardenIx.ZIP)
        val tree = fix(world, GardenIx.TREEHOUSE)
        val rider = kid(world, tree.x + GardenLayout.DECK_X0 + 0.2f, tree.y + GardenLayout.DECK_DY - 0.02f)
        run(sim, 1f)
        assertEquals(tree.id, rider.restOwner)
        sim.tap(place, fix(world, GardenIx.ZIP_POLE), 0f, -0.1f)
        assertEquals(Mode.SEATED, rider.mode)
        assertEquals(zip.id, rider.holder)
    }

    // ------------------------------------------------------------------ the small things

    @Test
    fun `sausages on the grill get cooked and count for the task`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        world.taskSet += listOf("garden_grill", "garden_choir", "garden_zip")
        val grill = fix(world, GardenIx.GRILL)
        run(sim, 1f)
        assertTrue(world.bodiesIn(place).any { it is Thing && it.type == ThingType.SAUSAGE && it.restOwner == grill.id })
        sim.tap(place, grill, 0f, -0.1f)
        assertTrue(grill.on)
        run(sim, 3f)
        assertTrue(world.bodiesIn(place).any { it is Thing && it.type == ThingType.GRILLED_SAUSAGE })
        assertTrue(sim.tasks.done(TaskBook.ALL.single { it.id == "garden_grill" }))
        sim.tap(place, grill, 0f, -0.1f)
        assertFalse(grill.on)
    }

    @Test
    fun `digging in the sandbox finds something every third time, and a bucket makes a sand cake`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val box = fix(world, GardenIx.SANDBOX)
        val before = world.bodiesIn(place).count { it is Thing }
        repeat(3) { sim.tap(place, box, 0f, -0.05f) }
        run(sim, 1f)
        assertEquals(before + 1, world.bodiesIn(place).count { it is Thing })
        assertTrue("garden_sand" in secrets)
        val bucket = world.addThing(ThingType.BUCKET, 0, place, box.x, box.y - 0.1f)
        assertFalse(sim.dropInto(place, box, bucket))
        run(sim, 1f)
        assertTrue(world.bodiesIn(place).any { it is Thing && it.type == ThingType.GA_SAND_CAKE })
    }

    @Test
    fun `the shed door opens, and the glimt in the shed shows only then`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val shed = fix(world, GardenIx.SHED)
        assertFalse(sim.visibleSecrets(place).any { it.id == "garden_shed" })
        sim.tap(place, shed, 0f, -0.2f)
        assertTrue(shed.open)
        assertTrue(sim.visibleSecrets(place).any { it.id == "garden_shed" })
        assertTrue("tools inside", world.bodiesIn(place).any { it is Thing && it.type == ThingType.HAMMER && it.inside == shed.id })
    }

    @Test
    fun `the lawn robot mows to and fro, hops over figures and sleeps at night`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val mower = fix(world, GardenIx.MOWER)
        sim.tap(place, mower, 0f, -0.03f)
        assertTrue(mower.on)
        var lo = 0f
        var hi = 0f
        var t = 0f
        while (t < 30f) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
            lo = minOf(lo, mower.shiftX)
            hi = maxOf(hi, mower.shiftX)
        }
        assertTrue("it travels both ways", lo < -0.3f && hi > 0.3f)
        assertTrue(mower.x + lo >= GardenLayout.MOW_X0 - 0.01f && mower.x + hi <= GardenLayout.MOW_X1 + 0.01f)
        assertTrue(heard(GardenFxCode.MOWER, 2))
        world.night = true
        run(sim, 0.2f)
        assertFalse("it goes to sleep", mower.on)
    }

    @Test
    fun `the gate lets a ball over the fence and the birdhouse and barrel answer`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val gate = fix(world, GardenIx.GATE)
        val balls = world.bodiesIn(place).count { it is Thing && it.type == ThingType.BALL }
        sim.tap(place, gate, 0f, -0.1f)
        assertEquals(1, gate.mode)
        assertEquals(balls + 1, world.bodiesIn(place).count { it is Thing && it.type == ThingType.BALL })
        sim.tap(place, gate, 0f, -0.1f)
        assertEquals(0, gate.mode)
        sim.tap(place, fix(world, GardenIx.BIRDHOUSE), 0f, -0.3f)
        sim.tap(place, fix(world, GardenIx.BARREL), 0f, -0.1f)
        assertTrue(heard(GardenFxCode.BIRD))
        assertTrue(heard(GardenFxCode.BARREL))
    }

    @Test
    fun `the sprinkler turns itself off`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val s = fix(world, GardenIx.SPRINKLER)
        sim.tap(place, s, 0f, -0.03f)
        assertTrue(s.on)
        run(sim, GardenRules.SPRINKLER_SECONDS + 1f)
        assertFalse(s.on)
    }

    @Test
    fun `the snowman is there when it snows and gone when it does not`() {
        val world = WorldFactory.create(Random(9))
        val sim = sim(world)
        val snowman = fix(world, GardenIx.SNOWMAN)
        run(sim, 0.5f)
        assertTrue("away in summer", snowman.x < 0f)
        world.weather = Weather.SNOW
        run(sim, 0.5f)
        assertEquals(GardenRules.SNOWMAN_X, snowman.x, 0.001f)
        val balls = world.bodiesIn(place).count { it is Thing && it.type == ThingType.SNOWBALL }
        sim.tap(place, snowman, 0f, -0.1f)
        assertEquals(balls + 1, world.bodiesIn(place).count { it is Thing && it.type == ThingType.SNOWBALL })
        world.weather = Weather.SUN
        run(sim, 0.5f)
        assertTrue(snowman.x < 0f)
    }

    @Test
    fun `a flower bed gives a flower on every third tap and the tree sometimes drops an apple`() {
        val world = WorldFactory.create(Random(10))
        val sim = sim(world)
        val bed = fix(world, GardenIx.BED0)
        val flowers = world.bodiesIn(place).count { it is Thing && it.type == ThingType.FLOWER }
        repeat(3) { sim.tap(place, bed, 0f, -0.05f) }
        assertEquals(flowers + 1, world.bodiesIn(place).count { it is Thing && it.type == ThingType.FLOWER })
        val tree = fix(world, GardenIx.TREEHOUSE)
        repeat(6) { sim.tap(place, tree, -0.3f, -0.2f) }
        assertTrue(heard(GardenFxCode.SHAKE_TREE, 1) || world.bodiesIn(place).count { it is Thing && it.type == ThingType.APPLE } >= 3)
        // A tap on the cabin opens the door instead.
        sim.tap(place, tree, 0.2f, -0.45f)
        assertTrue(tree.open)
        assertTrue(heard(GardenFxCode.CABIN, 1))
    }

    // ------------------------------------------------------------------ glimt, tasks, save

    @Test
    fun `the garden has its glimt, four tasks and a key`() {
        val glimt = Secrets.inPlace(place)
        assertTrue(glimt.size >= 3)
        assertTrue(glimt.all { it.id.startsWith("garden_") })
        assertEquals(glimt.size, glimt.map { it.id }.toSet().size)
        val tasks = TaskBook.ALL.filter { it.place == place }
        assertEquals(4, tasks.size)
        assertEquals(setOf(Deed.GA_CHOIR, Deed.GA_GIANT, Deed.GA_ZIP, Deed.GA_GRILL), Deed.entries.filter { it.name.startsWith("GA_") }.toSet())
        assertTrue("manor_key_garden" in HouseKeys.ids)
    }

    @Test
    fun `every glimt of the garden can be seen once its deed is done`() {
        val world = WorldFactory.create(Random(10))
        val sim = sim(world)
        for (s in Secrets.inPlace(place)) {
            if (s.event) sim.unlock(s.id)
            if (s.inside >= 0) fix(world, s.inside).open = true
            assertTrue("${s.id} visible", sim.visibleSecrets(place).any { it.id == s.id })
        }
    }

    @Test
    fun `a glimt on a gnome follows the gnome`() {
        val world = WorldFactory.create(Random(10))
        val sim = sim(world)
        val g = fix(world, GardenIx.GNOME0)
        val glimt = Secrets.byId("garden_gnome")!!
        val before = sim.secretAt(glimt)
        g.x += 2f
        val after = sim.secretAt(glimt)
        assertEquals(before[0] + 2f, after[0], 0.001f)
    }

    @Test
    fun `the save file keeps the beds, the gnomes, the shed and the heap`() {
        val world = WorldFactory.create(Random(12))
        val sim = sim(world, seed = 2)
        sim.tap(place, fix(world, GardenIx.PLANTER0), 0f, -0.1f)
        sim.tap(place, fix(world, GardenIx.SHED), 0f, -0.2f)
        repeat(3) { sim.dropInto(place, fix(world, GardenIx.COMPOST), world.addThing(ThingType.APPLE, 0, place, 1f, 0.8f)) }
        run(sim, 1f)
        GardenView.set(0f, 0.5f)
        run(sim, 60f)
        val gnomes = gnomeState(world, depth = false)
        val json = WorldStore.encode(world, Settings()).toString()
        val w2 = WorldStore.decode(org.json.JSONObject(json)).world
        assertEquals("the plant went on growing", 3, w2.fixtures.getValue(place.idBase + GardenIx.PLANTER0).mode)
        assertTrue(w2.fixtures.getValue(place.idBase + GardenIx.PLANTER0).open)
        assertTrue(w2.fixtures.getValue(place.idBase + GardenIx.SHED).open)
        assertEquals(1, w2.fixtures.getValue(place.idBase + GardenIx.COMPOST).mode)
        assertEquals(3, w2.fixtures.getValue(place.idBase + GardenIx.COMPOST).count)
        assertTrue("manor_key_garden" in w2.flags)
        assertEquals(gnomes, gnomeState(w2, depth = false))
    }
}
