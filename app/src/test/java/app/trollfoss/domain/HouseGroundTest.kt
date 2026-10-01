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

/** Storstova: the blueprint, the rules of every room, the key, the glimt, the tasks and what is saved. */
class HouseGroundTest {
    private val place = PlaceId.MANOR_GROUND
    private val codes = ArrayList<Pair<Int, Int>>()
    private val fxs = ArrayList<Fx>()
    private val keys = ArrayList<Int>()

    private fun newWorld(night: Boolean = false, seed: Int = 1): World {
        val world = WorldFactory.create(Random(seed))
        world.place = place
        world.night = night
        return world
    }

    private fun sim(world: World, seed: Int = 3): Sim {
        val sim = Sim(world, object : SimListener {
            override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
                fxs += fx
                if (fx == Fx.HOUSE) {
                    codes += HouseFx.code(param) to HouseFx.arg(param)
                    if (HouseFx.code(param) == HouseFx.KEY_FOUND) keys += HouseFx.arg(param)
                }
            }
        }, Random(seed))
        sim.today = 20000L
        // The first moment of a visit sets the floor up.
        sim.step(place, 1f / 60f)
        return sim
    }

    private fun step(sim: Sim, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun fx(world: World, index: Int) = world.fixtures.getValue(place.idBase + index)

    private fun tap(sim: Sim, world: World, index: Int, dx: Float = 0f, dy: Float = -0.1f) {
        val f = fx(world, index)
        sim.tap(place, f, dx, dy)
    }

    private fun heard(code: Int, arg: Int? = null) = codes.any { it.first == code && (arg == null || it.second == arg) }

    private fun person(world: World, x: Float, name: String = "Gjest") =
        world.addPerson(Species.FOLK, Look(), 1f, place, x, place.floor, name)

    // ---------------------------------------------------------------- blueprint

    @Test
    fun `the blueprint is as planned and every index names the right furniture`() {
        val defs = Places.spec(place).fixtures
        assertEquals(GroundIx.COUNT, defs.size)
        assertTrue("room for added furniture", defs.size <= place.addedFrom)
        val expected = mapOf(
            GroundIx.STAIRS to FixtureType.STAIRCASE, GroundIx.LIFT to FixtureType.LIFT, GroundIx.CELLAR_DOOR to FixtureType.DOOR,
            GroundIx.SECRET_SHELF to FixtureType.SECRET_DOOR, GroundIx.DUMBWAITER to FixtureType.DUMBWAITER, GroundIx.GARDEN_DOOR to FixtureType.DOOR,
            GroundIx.CLOCK to FixtureType.GR_CLOCK, GroundIx.ARMOUR to FixtureType.GR_ARMOUR, GroundIx.CHANDELIER to FixtureType.GR_CHANDELIER,
            GroundIx.FIREPLACE to FixtureType.GR_FIREPLACE, GroundIx.SOFA to FixtureType.GR_SOFA, GroundIx.POPCORN to FixtureType.GR_POPCORN_BOWL,
            GroundIx.TV to FixtureType.GR_TV, GroundIx.LADDER to FixtureType.GR_LADDER, GroundIx.DINING_TABLE to FixtureType.GR_DINING_TABLE,
            GroundIx.BELL to FixtureType.GR_BELL, GroundIx.CAKE to FixtureType.GR_CAKE, GroundIx.RANGE to FixtureType.GR_RANGE,
            GroundIx.MIXER to FixtureType.GR_MIXER, GroundIx.PIZZA_OVEN to FixtureType.GR_PIZZA_OVEN, GroundIx.FRIDGE to FixtureType.GR_FRIDGE,
            GroundIx.ISLAND to FixtureType.GR_ISLAND, GroundIx.FERN to FixtureType.GR_PLANT, GroundIx.FOUNTAIN to FixtureType.GR_FOUNTAIN,
            GroundIx.SOFIE to FixtureType.GR_SOFIE, GroundIx.HAMMOCK to FixtureType.GR_HAMMOCK, GroundIx.FLOWERS to FixtureType.GR_PLANT,
            GroundIx.CHAIR_ROW to FixtureType.GR_CHAIR_ROW, GroundIx.STOOLS to FixtureType.GR_STOOLS, GroundIx.SINK to FixtureType.GR_SINK,
        )
        for ((index, type) in expected) assertEquals("fixture $index", type, defs[index].type)
        // Everything stands inside the floor, and wall furniture hangs inside the wall.
        for ((i, d) in defs.withIndex()) {
            assertTrue("fixture $i is inside the floor", d.x in 0.1f..(place.width - 0.1f))
            assertTrue("fixture $i is inside the picture", d.y in 0.05f..1.0f)
        }
    }

    @Test
    fun `every piece of ground floor furniture has a spec of its own`() {
        val own = FixtureType.entries.filter { it.name.startsWith("GR_") }
        assertTrue(own.size >= 38)
        for (type in own) {
            assertNotNull("spec of $type", GroundSpecs.of(type))
            assertTrue("$type is not a plain box", type.spec.w != 0.3f || type.spec.h != 0.3f)
        }
        // Every type in the blueprint is one of the floor's own, a shared way or an existing beanbag.
        for (d in Places.spec(place).fixtures) {
            assertTrue("${d.type} belongs here", d.type.name.startsWith("GR_") || d.type in setOf(
                FixtureType.STAIRCASE, FixtureType.LIFT, FixtureType.DOOR, FixtureType.SECRET_DOOR, FixtureType.DUMBWAITER, FixtureType.BEANBAG,
            ))
        }
    }

    @Test
    fun `the passages keep their ids and their arrivals`() {
        val ids = GroundFloor.passages.map { it.id }
        assertEquals(
            listOf("ground-stairs-up", "ground-lift", "ground-cellar-door", "ground-bookshelf", "ground-dumbwaiter", "ground-garden-door"),
            ids,
        )
        val names = GroundFloor.arrivals.map { it.name }
        for (n in listOf("stairs-foot", "lift", "cellar-door", "library-secret", "dumbwaiter", "garden-door", "slide-end", "pole-end")) assertTrue(n in names)
        val bookshelf = GroundFloor.passages.first { it.id == "ground-bookshelf" }
        assertEquals("manor_bookshelf", bookshelf.locked)
        assertEquals(6, GroundFloor.rooms.size)
        assertEquals(GroundFloor.rooms, Decor.rooms(place))
    }

    @Test
    fun `the food on the island, the candle on the mantel and the pepper on the table rest where the blueprint puts them`() {
        val world = newWorld()
        val things = world.bodiesIn(place).filterIsInstance<Thing>()
        val island = fx(world, GroundIx.ISLAND)
        val onIsland = things.filter { it.restOwner == island.id }
        assertTrue("apples, dough and cheese lie on the island, not on the floor", onIsland.map { it.type }.containsAll(listOf(ThingType.APPLE, ThingType.DOUGH, ThingType.BROWN_CHEESE)))
        assertTrue(things.first { it.type == ThingType.CANDLE }.restOwner == fx(world, GroundIx.FIREPLACE).id)
        assertTrue(things.first { it.type == ThingType.PEPPER }.restOwner == fx(world, GroundIx.DINING_TABLE).id)
        assertTrue(things.first { it.type == ThingType.COCOA }.restOwner == fx(world, GroundIx.COFFEE_TABLE).id)
        assertTrue(things.first { it.type == ThingType.BOOK }.restOwner == fx(world, GroundIx.DESK).id)
    }

    @Test
    fun `there is a butler, a cat and a bunny, and things to play with`() {
        val world = newWorld()
        val people = world.bodiesIn(place).filterIsInstance<Person>()
        val rolf = people.first { it.species == Species.ROBOT }
        assertEquals("Rolf", rolf.name)
        assertTrue(people.any { it.species == Species.CAT })
        assertTrue(people.any { it.species == Species.BUNNY })
        assertTrue(world.bodiesIn(place).filterIsInstance<Thing>().count { it.type == ThingType.DOUGH } >= 2)
        assertTrue(GroundFloor.hangouts(false).isNotEmpty() && GroundFloor.hangouts(true).isNotEmpty())
    }

    // ---------------------------------------------------------------- hall

    @Test
    fun `the grandfather clock strikes one more hour at every tap, and at twelve it drops a coin`() {
        val world = newWorld()
        val sim = sim(world)
        val coins = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.COIN }
        for (n in 1..12) {
            tap(sim, world, GroundIx.CLOCK)
            assertTrue("strike $n", heard(GroundCode.CLOCK, n))
        }
        assertEquals(coins + 1, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.COIN })
        tap(sim, world, GroundIx.CLOCK)
        assertTrue(heard(GroundCode.CLOCK, 1))
    }

    @Test
    fun `the coat rack and the umbrella stand hand out hats and umbrellas`() {
        val world = newWorld()
        val sim = sim(world)
        val before = world.bodiesIn(place).size
        tap(sim, world, GroundIx.COAT_RACK)
        tap(sim, world, GroundIx.COAT_RACK)
        tap(sim, world, GroundIx.UMBRELLAS)
        val things = world.bodiesIn(place).filterIsInstance<Thing>()
        assertEquals(before + 3, world.bodiesIn(place).size)
        assertTrue(things.count { it.type.cat == Cat.HAT } >= 2)
        assertTrue(things.any { it.type == ThingType.GR_UMBRELLA })
        // An umbrella goes back into the stand.
        val umbrella = things.first { it.type == ThingType.GR_UMBRELLA }
        assertTrue(sim.dropInto(place, fx(world, GroundIx.UMBRELLAS), umbrella))
        assertFalse(world.bodies.containsKey(umbrella.id))
    }

    @Test
    fun `the post slot throws one letter a day`() {
        val world = newWorld()
        val sim = sim(world)
        tap(sim, world, GroundIx.POST_SLOT)
        assertTrue(world.bodiesIn(place).any { (it as? Thing)?.type == ThingType.GR_LETTER })
        val letters = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GR_LETTER }
        tap(sim, world, GroundIx.POST_SLOT)
        assertEquals(letters, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GR_LETTER })
        sim.today = 20001L
        tap(sim, world, GroundIx.POST_SLOT)
        assertEquals(letters + 1, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GR_LETTER })
    }

    @Test
    fun `the chandelier swings, settles and switches its light`() {
        val world = newWorld()
        val sim = sim(world)
        val chandelier = fx(world, GroundIx.CHANDELIER)
        val on = chandelier.on
        tap(sim, world, GroundIx.CHANDELIER)
        assertEquals(!on, chandelier.on)
        assertTrue(chandelier.angleV != 0f)
        var swung = false
        repeat(120) {
            sim.step(place, 1f / 60f)
            if (kotlin.math.abs(chandelier.angle) > 0.02f) swung = true
        }
        assertTrue(swung)
        step(sim, 25f)
        assertEquals(0f, chandelier.angle, 0.01f)
        assertTrue(heard(GroundCode.CHANDELIER))
    }

    @Test
    fun `Riddar Rusten clanks, salutes, hiccups and loses his helmet, and gets it back`() {
        val world = newWorld()
        val sim = sim(world)
        val knight = fx(world, GroundIx.ARMOUR)
        tap(sim, world, GroundIx.ARMOUR)
        assertTrue(heard(GroundCode.ARMOUR, 0))
        step(sim, 1f)
        tap(sim, world, GroundIx.ARMOUR)
        assertTrue(heard(GroundCode.ARMOUR, 1))
        step(sim, 2f)
        assertEquals(0, knight.mode)
        tap(sim, world, GroundIx.ARMOUR)
        assertTrue(heard(GroundCode.ARMOUR, 2))
        step(sim, 3f)
        assertEquals("the helmet pops off", 1, knight.mode)
        assertTrue(heard(GroundCode.ARMOUR, 3))
        assertTrue("ground_hall" in world.unlocked)
        tap(sim, world, GroundIx.ARMOUR)
        assertEquals(0, knight.mode)
        assertTrue(heard(GroundCode.ARMOUR, 4))
    }

    // ---------------------------------------------------------------- living room

    @Test
    fun `the fire is lit by a tap, the stockings give treats and sausages are roasted`() {
        val world = newWorld()
        val sim = sim(world)
        val fire = fx(world, GroundIx.FIREPLACE)
        fire.on = false
        tap(sim, world, GroundIx.FIREPLACE, 0f, -0.1f)
        assertTrue(fire.on)
        assertTrue(heard(GroundCode.FIRE, 1))
        val before = world.bodiesIn(place).size
        tap(sim, world, GroundIx.FIREPLACE, -0.16f, -0.4f)
        assertEquals(before + 1, world.bodiesIn(place).size)
        assertTrue(fire.on)
        val sausage = world.addThing(ThingType.SAUSAGE, 0, place, fire.x, fire.y - 0.065f)
        step(sim, 4f)
        assertEquals(ThingType.GRILLED_SAUSAGE, sausage.type)
    }

    @Test
    fun `the TV has five channels and the sofa gives coins to the persistent`() {
        val world = newWorld()
        val sim = sim(world)
        val tv = fx(world, GroundIx.TV)
        assertFalse(tv.on)
        val seen = ArrayList<Int>()
        for (i in 1..6) {
            tap(sim, world, GroundIx.TV)
            seen += tv.mode
        }
        assertEquals(listOf(1, 2, 3, 4, 5, 0), seen)
        assertFalse(tv.on)
        val coins = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.COIN }
        repeat(4) { tap(sim, world, GroundIx.SOFA) }
        assertEquals(coins + 3, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.COIN })
    }

    @Test
    fun `film night turns the lights off, starts the film, and ends by itself`() {
        val world = newWorld(night = true)
        val sim = sim(world)
        val lamp = fx(world, GroundIx.LAMP_LIVING)
        assertTrue("the lamp is on at night", lamp.on)
        val popcorn = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.POPCORN }
        tap(sim, world, GroundIx.POPCORN)
        val tv = fx(world, GroundIx.TV)
        assertTrue(tv.on)
        assertEquals(1, tv.count)
        assertFalse(lamp.on)
        assertTrue(world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.POPCORN } > popcorn)
        assertTrue("ground_living" in world.unlocked)
        assertTrue(heard(GroundCode.FILM, 1))
        step(sim, 3f)
        assertTrue("the room gets dark", GroundFloor.darkness > 0.3f)
        // Tapping the bowl during the film gives popcorn; the TV ends it.
        tap(sim, world, GroundIx.POPCORN)
        tap(sim, world, GroundIx.TV)
        assertEquals(0, tv.count)
        assertTrue(lamp.on)
        step(sim, 4f)
        assertEquals(0f, GroundFloor.darkness, 0.001f)
    }

    @Test
    fun `an unattended film night ends after a while`() {
        val world = newWorld()
        val sim = sim(world)
        tap(sim, world, GroundIx.POPCORN)
        step(sim, GroundRules.FILM_SECONDS + 5f)
        assertEquals(0, fx(world, GroundIx.TV).count)
        step(sim, 5f)
        assertEquals(0f, GroundFloor.darkness, 0.001f)
    }

    @Test
    fun `lamps come on at night and go out in the morning`() {
        val world = newWorld()
        val sim = sim(world)
        val lamp = fx(world, GroundIx.LAMP_LIVING)
        assertFalse(lamp.on)
        world.night = true
        step(sim, 0.1f)
        assertTrue(lamp.on)
        assertTrue(fx(world, GroundIx.CHANDELIER).on)
        assertTrue(heard(GroundCode.ROLF, 6))
        world.night = false
        step(sim, 0.1f)
        assertFalse(lamp.on)
        // And a child can switch one by hand.
        tap(sim, world, GroundIx.LAMP_LIVING)
        assertTrue(lamp.on)
    }

    @Test
    fun `the globe spins and spits out a pin when it is spun hard`() {
        val world = newWorld()
        val sim = sim(world)
        val globe = fx(world, GroundIx.GLOBE)
        var pins = 0
        repeat(4) {
            tap(sim, world, GroundIx.GLOBE)
            step(sim, 0.2f)
        }
        pins = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GR_PIN }
        assertTrue(pins >= 1)
        step(sim, 0.5f)
        assertTrue(globe.angle != 0f)
    }

    @Test
    fun `the fish follow the finger and eat what is dropped in`() {
        val world = newWorld()
        val sim = sim(world)
        val tank = fx(world, GroundIx.AQUARIUM)
        sim.tap(place, tank, 0.15f, -0.05f)
        assertTrue(tank.timer > 3f)
        assertEquals(0.15f / 0.23f, tank.angle, 0.01f)
        val apple = world.addThing(ThingType.APPLE, 0, place, tank.x, tank.y - 0.1f)
        assertTrue(sim.dropInto(place, tank, apple))
        assertFalse(world.bodies.containsKey(apple.id))
        assertTrue(heard(GroundCode.FISH_FEED))
        val ball = world.addThing(ThingType.BALL, 0, place, tank.x, tank.y - 0.1f)
        assertFalse(sim.dropInto(place, tank, ball))
    }

    // ---------------------------------------------------------------- library

    @Test
    fun `the red book opens the bookcase and the way to the attic`() {
        val world = newWorld()
        val sim = sim(world)
        val shelf = fx(world, GroundIx.SECRET_SHELF)
        val rider = person(world, shelf.x)
        // Tapped anywhere else, the bookcase is locked.
        sim.tap(place, shelf, -0.1f, -0.1f)
        assertEquals(place, rider.place)
        assertFalse("manor_bookshelf" in world.flags)
        // The red book is the lever.
        sim.tap(place, shelf, GroundRules.RED_BOOK_DX, -0.3f)
        assertTrue("manor_bookshelf" in world.flags)
        assertEquals(1, shelf.mode)
        assertTrue("ground_library" in world.unlocked)
        assertTrue(heard(GroundCode.LEVER))
        assertEquals(place, rider.place)
        sim.tap(place, shelf, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_ATTIC, rider.place)
        assertTrue(House.usable(world, House.passages.first { it.id == "attic-secret-down" }))
    }

    @Test
    fun `an open bookcase stays open after a save`() {
        val world = newWorld()
        val sim = sim(world)
        sim.tap(place, fx(world, GroundIx.SECRET_SHELF), GroundRules.RED_BOOK_DX, -0.3f)
        val again = WorldStore.decode(org.json.JSONObject(WorldStore.encode(world, Settings()).toString())).world
        assertTrue("manor_bookshelf" in again.flags)
        assertEquals(1, again.fixtures.getValue(place.idBase + GroundIx.SECRET_SHELF).mode)
    }

    @Test
    fun `books flutter out of a shelf`() {
        val world = newWorld()
        val sim = sim(world)
        val before = world.bodiesIn(place).size
        tap(sim, world, GroundIx.SHELF_A, 0f, -0.3f)
        assertEquals(before + 1, world.bodiesIn(place).size)
        assertTrue(heard(GroundCode.BOOKS))
    }

    @Test
    fun `the library ladder rolls along its rail with whoever sits on it`() {
        val world = newWorld()
        val sim = sim(world)
        val ladder = fx(world, GroundIx.LADDER)
        val reader = person(world, ladder.x)
        assertTrue(sim.seat(reader, ladder, 0))
        val x0 = reader.x
        tap(sim, world, GroundIx.LADDER)
        assertEquals(1, ladder.mode)
        step(sim, 2.5f)
        assertEquals(GroundRules.LADDER_STOPS[1], ladder.shiftX, 0.01f)
        tap(sim, world, GroundIx.LADDER)
        step(sim, 2.5f)
        assertEquals(GroundRules.LADDER_STOPS[2], ladder.shiftX, 0.01f)
        assertTrue("the reader rode along", reader.x > x0 + 0.2f)
        assertEquals(Mode.SEATED, reader.mode)
    }

    @Test
    fun `the talking book mumbles, and shushes when it is tapped three times`() {
        val world = newWorld()
        val sim = sim(world)
        tap(sim, world, GroundIx.LECTERN)
        assertTrue(heard(GroundCode.LECTERN, 0))
        tap(sim, world, GroundIx.LECTERN)
        tap(sim, world, GroundIx.LECTERN)
        assertTrue(heard(GroundCode.LECTERN, 9))
    }

    // ---------------------------------------------------------------- dining room

    @Test
    fun `the bell lays the table and clears it again`() {
        val world = newWorld()
        val sim = sim(world)
        val table = fx(world, GroundIx.DINING_TABLE)
        assertEquals(0, table.mode)
        tap(sim, world, GroundIx.BELL)
        assertEquals(1, table.mode)
        assertTrue(table.timer > 2f)
        assertTrue(heard(GroundCode.TABLE, 1))
        step(sim, 3f)
        assertEquals(0f, table.timer, 0.001f)
        tap(sim, world, GroundIx.BELL)
        assertEquals(0, table.mode)
        assertTrue(heard(GroundCode.TABLE, 0))
        // It is saved.
        tap(sim, world, GroundIx.BELL)
        val again = WorldStore.decode(org.json.JSONObject(WorldStore.encode(world, Settings()).toString())).world
        assertEquals(1, again.fixtures.getValue(place.idBase + GroundIx.DINING_TABLE).mode)
    }

    @Test
    fun `six can sit at the dining table`() {
        val world = newWorld()
        val sim = sim(world)
        val row = fx(world, GroundIx.CHAIR_ROW)
        val left = fx(world, GroundIx.CHAIR_LEFT)
        val right = fx(world, GroundIx.CHAIR_RIGHT)
        assertEquals(4, row.spec.spots.size)
        val guests = (0 until 6).map { person(world, 7f, "G$it") }
        for (i in 0 until 4) assertTrue(sim.seat(guests[i], row, i))
        assertTrue(sim.seat(guests[4], left, 0))
        assertTrue(sim.seat(guests[5], right, 0))
        assertTrue(guests.all { it.mode == Mode.SEATED })
        val xs = guests.map { it.x }.sorted()
        for (i in 1 until xs.size) assertTrue("guests do not sit on each other", xs[i] - xs[i - 1] > 0.1f)
    }

    @Test
    fun `the birthday cake sings, blows out its candles and gives a cupcake`() {
        val world = newWorld()
        val sim = sim(world)
        val cake = fx(world, GroundIx.CAKE)
        assertTrue("the candles are lit from the start", cake.on)
        val cupcakes = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.CUPCAKE }
        tap(sim, world, GroundIx.CAKE)
        assertEquals(1, cake.mode)
        assertTrue(heard(GroundCode.CAKE, 1))
        step(sim, GroundRules.SONG_SECONDS + 0.5f)
        assertFalse(cake.on)
        assertEquals(0, cake.mode)
        assertEquals(1, cake.count)
        assertTrue(heard(GroundCode.CAKE, 2))
        assertEquals(cupcakes + 1, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.CUPCAKE })
        assertTrue("ground_dining" in world.unlocked)
        // Out: a tap lights them again.
        tap(sim, world, GroundIx.CAKE)
        assertTrue(cake.on)
    }

    @Test
    fun `the sideboard opens and has plates and a candle`() {
        val world = newWorld()
        val sim = sim(world)
        val board = fx(world, GroundIx.SIDEBOARD)
        tap(sim, world, GroundIx.SIDEBOARD)
        assertTrue(board.open)
        assertTrue(world.bodiesIn(place).count { it.inside == board.id } >= 2)
        tap(sim, world, GroundIx.SIDEBOARD)
        assertFalse(board.open)
    }

    // ---------------------------------------------------------------- kitchen

    @Test
    fun `dough in the pizza oven comes out a pizza`() {
        val world = newWorld()
        val sim = sim(world)
        val oven = fx(world, GroundIx.PIZZA_OVEN)
        val dough = world.bodiesIn(place).filterIsInstance<Thing>().first { it.type == ThingType.DOUGH }
        assertTrue(sim.dropInto(place, oven, dough))
        assertEquals(1, oven.mode)
        assertTrue(oven.on)
        val pizzas = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.PIZZA }
        step(sim, 3f)
        assertEquals(0, oven.mode)
        assertEquals(pizzas + 1, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.PIZZA })
        assertTrue("ground_kitchen" in world.unlocked)
        assertTrue(heard(GroundCode.PIZZA, 1))
        // Not everything fits.
        val ball = world.addThing(ThingType.BALL, 0, place, oven.x, oven.y - 0.1f)
        assertFalse(sim.dropInto(place, oven, ball))
    }

    @Test
    fun `the range bakes what is shut in its oven`() {
        val world = newWorld()
        val sim = sim(world)
        val range = fx(world, GroundIx.RANGE)
        tap(sim, world, GroundIx.RANGE, 0f, -0.05f)
        assertTrue(range.open)
        val dough = world.addThing(ThingType.DOUGH, 0, place, range.x, range.y - 0.03f)
        dough.inside = range.id
        dough.resting = true
        tap(sim, world, GroundIx.RANGE, 0f, -0.05f)
        assertFalse(range.open)
        assertEquals(1, range.mode)
        step(sim, 3.2f)
        assertTrue(range.open)
        val inside = world.bodiesIn(place).filter { it.inside == range.id }
        assertEquals(1, inside.size)
        assertEquals(ThingType.BUN, (inside[0] as Thing).type)
    }

    @Test
    fun `the hobs fry an egg`() {
        val world = newWorld()
        val sim = sim(world)
        val range = fx(world, GroundIx.RANGE)
        tap(sim, world, GroundIx.RANGE, 0f, -0.25f)
        assertTrue(range.on)
        val egg = world.addThing(ThingType.EGG, 0, place, range.x, range.y - 0.3f)
        egg.resting = true
        egg.restOwner = range.id
        step(sim, 3f)
        assertEquals(ThingType.FRIED_EGG, egg.type)
    }

    @Test
    fun `the mixer makes smoothies from fruit and sprays whoever stands close`() {
        val world = newWorld()
        val sim = sim(world)
        val mixer = fx(world, GroundIx.MIXER)
        val victim = person(world, mixer.x + 0.1f, "Sprutet")
        victim.ground = mixer.depth + 0.03f
        victim.y = victim.ground
        val apple = world.addThing(ThingType.APPLE, 0, place, mixer.x, mixer.y - 0.1f)
        assertTrue(sim.dropInto(place, mixer, apple))
        assertEquals(1, world.inMachine(mixer).size)
        tap(sim, world, GroundIx.MIXER)
        assertTrue(mixer.on)
        step(sim, 2.2f)
        assertFalse(mixer.on)
        assertEquals(1, mixer.count)
        assertTrue(world.bodiesIn(place).any { (it as? Thing)?.type == ThingType.SMOOTHIE })
        assertTrue("the victim got a splat", victim.anim.cream > 0f || Fx.SPLAT in fxs)
        assertTrue(heard(GroundCode.MIXER, 1))
    }

    @Test
    fun `the fridge is stocked when it is opened`() {
        val world = newWorld()
        val sim = sim(world)
        val fridge = fx(world, GroundIx.FRIDGE)
        tap(sim, world, GroundIx.FRIDGE)
        assertTrue(fridge.open)
        assertEquals(4, world.bodiesIn(place).count { it.inside == fridge.id })
        tap(sim, world, GroundIx.JAM_CABINET)
        assertTrue(world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GR_JAM } >= 3)
    }

    @Test
    fun `the island hands out fruit`() {
        val world = newWorld()
        val sim = sim(world)
        val before = world.bodiesIn(place).size
        tap(sim, world, GroundIx.ISLAND, -0.25f, -0.1f)
        assertEquals(before + 1, world.bodiesIn(place).size)
    }

    // ---------------------------------------------------------------- winter garden

    @Test
    fun `plants grow when they are watered, and ripe ones give a flower`() {
        val world = newWorld()
        val sim = sim(world)
        val fern = fx(world, GroundIx.FERN)
        val can = world.bodiesIn(place).filterIsInstance<Thing>().first { it.type == ThingType.WATERING_CAN }
        assertEquals(0, fern.mode)
        repeat(3) { assertFalse(sim.dropInto(place, fern, can)) }
        assertEquals(3, fern.mode)
        assertTrue("ground_garden" in world.unlocked)
        assertTrue(heard(GroundCode.GROW, 3))
        val flowers = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.FLOWER }
        tap(sim, world, GroundIx.FERN)
        assertEquals(2, fern.mode)
        assertEquals(flowers + 1, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.FLOWER })
        // Growth is saved.
        val again = WorldStore.decode(org.json.JSONObject(WorldStore.encode(world, Settings()).toString())).world
        assertEquals(2, again.fixtures.getValue(place.idBase + GroundIx.FERN).mode)
    }

    @Test
    fun `a coin in the fountain is a wish that makes every plant grow`() {
        val world = newWorld()
        val sim = sim(world)
        val fountain = fx(world, GroundIx.FOUNTAIN)
        val coin = world.addThing(ThingType.COIN, 0, place, fountain.x, fountain.y - 0.1f)
        assertTrue(sim.dropInto(place, fountain, coin))
        assertEquals(1, fx(world, GroundIx.FERN).mode)
        assertEquals(1, fx(world, GroundIx.PALM).mode)
        assertEquals(1, fx(world, GroundIx.FLOWERS).mode)
        assertTrue(heard(GroundCode.FOUNTAIN, 1))
    }

    @Test
    fun `Sofie burps for every bite and the second one spits out the golden key`() {
        val world = newWorld()
        val sim = sim(world)
        val sofie = fx(world, GroundIx.SOFIE)
        assertFalse("manor_key_ground" in world.flags)
        // Not food: she spits it back.
        val ball = world.addThing(ThingType.BALL, 0, place, sofie.x, sofie.y - 0.2f)
        assertFalse(sim.dropInto(place, sofie, ball))
        assertTrue(heard(GroundCode.SOFIE, 4))
        // The first bite.
        val apple = world.addThing(ThingType.APPLE, 0, place, sofie.x, sofie.y - 0.2f)
        assertTrue(sim.dropInto(place, sofie, apple))
        assertEquals(1, sofie.count)
        step(sim, 1.5f)
        assertTrue(heard(GroundCode.SOFIE, 1))
        assertFalse("manor_key_ground" in world.flags)
        // The second bite: BUURP, and out flies the key.
        val banana = world.addThing(ThingType.BANANA, 0, place, sofie.x, sofie.y - 0.2f)
        assertTrue(sim.dropInto(place, sofie, banana))
        step(sim, 1.5f)
        assertTrue(heard(GroundCode.SOFIE, 2))
        assertTrue("manor_key_ground" in world.flags)
        assertEquals(listOf(1), keys)
        assertTrue(world.bodiesIn(place).any { (it as? Thing)?.type == ThingType.GOLDEN_KEY })
        assertEquals(1, HouseKeys.found(world))
        // The key is only spat out once.
        val keysBefore = world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GOLDEN_KEY }
        val pear = world.addThing(ThingType.CARROT, 0, place, sofie.x, sofie.y - 0.2f)
        assertTrue(sim.dropInto(place, sofie, pear))
        step(sim, 1.5f)
        assertEquals(keysBefore, world.bodiesIn(place).count { (it as? Thing)?.type == ThingType.GOLDEN_KEY })
    }

    @Test
    fun `the hammock holds a sleeper and sways with them`() {
        val world = newWorld()
        val sim = sim(world)
        val hammock = fx(world, GroundIx.HAMMOCK)
        val sleeper = person(world, hammock.x)
        assertTrue(sim.seat(sleeper, hammock, 0))
        assertEquals(Pose.LIE, sleeper.anim.pose)
        val ys = HashSet<Float>()
        repeat(120) {
            sim.step(place, 1f / 60f)
            ys += sleeper.y
        }
        assertTrue("the sleeper bobs", ys.size > 5)
    }

    // ---------------------------------------------------------------- the way between floors

    @Test
    fun `the stairs, the lift and the doors take riders and the doors creak`() {
        val world = newWorld()
        val sim = sim(world)
        val up = person(world, fx(world, GroundIx.STAIRS).x)
        sim.tap(place, fx(world, GroundIx.STAIRS), 0f, -0.1f)
        assertEquals(PlaceId.MANOR_UPPER, up.place)
        val down = person(world, fx(world, GroundIx.CELLAR_DOOR).x, "Kjeller")
        sim.tap(place, fx(world, GroundIx.CELLAR_DOOR), 0f, -0.1f)
        assertTrue(heard(GroundCode.CREAK))
        assertEquals(PlaceId.MANOR_CELLAR, down.place)
        val out = person(world, fx(world, GroundIx.GARDEN_DOOR).x, "Hage")
        sim.tap(place, fx(world, GroundIx.GARDEN_DOOR), 0f, -0.1f)
        assertEquals(PlaceId.MANOR_GARDEN, out.place)
        val lift = person(world, fx(world, GroundIx.LIFT).x, "Heis")
        sim.tap(place, fx(world, GroundIx.LIFT), 0f, -0.1f)
        assertEquals(PlaceId.MANOR_UPPER, lift.place)
        assertTrue(heard(GroundCode.LIFT))
    }

    // ---------------------------------------------------------------- Rolf

    @Test
    fun `Rolf serves a snack to somebody who sits down`() {
        val world = newWorld()
        val sim = sim(world)
        val sofa = fx(world, GroundIx.SOFA)
        val guest = person(world, sofa.x - 0.15f, "Gjest")
        assertTrue(sim.seat(guest, sofa, 0))
        assertNull(world.worn(guest, Slot.HAND))
        step(sim, 90f)
        val held = world.worn(guest, Slot.HAND)
        assertNotNull("Rolf brought something", held)
        assertTrue(heard(GroundCode.ROLF, 5))
        assertTrue(heard(GroundCode.ROLF, 1))
    }

    @Test
    fun `Rolf puts a thing that lies about where it belongs`() {
        val world = newWorld()
        val sim = sim(world)
        val ball = world.bodiesIn(place).filterIsInstance<Thing>().first { it.type == ThingType.BALL }
        val home = ball.x
        // Somebody left it far away, on the floor.
        ball.x = home + 3.2f
        ball.y = 0.93f
        ball.ground = 0.93f
        ball.resting = true
        ball.restOwner = -1
        step(sim, 150f)
        assertTrue("Rolf heard the thing: he picked it up or put it away", heard(GroundCode.ROLF, 3) || heard(GroundCode.ROLF, 4))
        assertTrue("it is no longer where it was left", kotlin.math.abs(ball.x - (home + 3.2f)) > 0.3f || ball.mode == Mode.WORN)
    }

    @Test
    fun `a tap on Rolf gets an answer`() {
        val world = newWorld()
        val sim = sim(world)
        val rolf = world.people().first { it.species == Species.ROBOT }
        step(sim, 1f)
        rolf.anim.lastTap = 12.5f
        step(sim, 0.2f)
        assertTrue(heard(GroundCode.ROLF))
    }

    // ---------------------------------------------------------------- tasks, glimt and the key

    @Test
    fun `the floor has four tasks that need nothing found before, and at least three glimt`() {
        val mine = TaskBook.ALL.filter { it.place == place }
        assertTrue(mine.size >= 4)
        for (t in mine) assertTrue("${t.id} has a picture", t.fixture != null || t.thing != null || t.species != null)
        val glimt = Secrets.inPlace(place)
        assertTrue(glimt.size >= 3)
        assertTrue(glimt.all { it.event })
        for (s in glimt) if (s.on >= 0) assertTrue(s.on < Places.spec(place).fixtures.size)
        assertEquals(glimt.size, glimt.map { it.id }.toSet().size)
        assertTrue(glimt.all { it.id.startsWith("ground_") })
    }

    @Test
    fun `the tasks count what the child does`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("ground_film", "ground_table", "ground_pizza")
        tap(sim, world, GroundIx.BELL)
        tap(sim, world, GroundIx.POPCORN)
        val dough = world.bodiesIn(place).filterIsInstance<Thing>().first { it.type == ThingType.DOUGH }
        sim.dropInto(place, fx(world, GroundIx.PIZZA_OVEN), dough)
        step(sim, 3f)
        for (id in listOf("ground_film", "ground_table", "ground_pizza")) {
            val task = TaskBook.ALL.first { it.id == id }
            assertTrue("$id is done", sim.tasks.done(task))
        }
    }

    @Test
    fun `the water task and the sofie task count too`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("ground_plants", "ground_sofie")
        val fern = fx(world, GroundIx.FERN)
        val can = world.bodiesIn(place).filterIsInstance<Thing>().first { it.type == ThingType.WATERING_CAN }
        sim.dropInto(place, fern, can)
        sim.dropInto(place, fx(world, GroundIx.PALM), can)
        val apple = world.addThing(ThingType.APPLE, 0, place, 10.5f, 0.7f)
        sim.dropInto(place, fx(world, GroundIx.SOFIE), apple)
        assertTrue(sim.tasks.done(TaskBook.ALL.first { it.id == "ground_plants" }))
        assertTrue(sim.tasks.done(TaskBook.ALL.first { it.id == "ground_sofie" }))
    }

    // ---------------------------------------------------------------- saving

    @Test
    fun `what the child did is saved with the fixtures`() {
        val world = newWorld()
        val sim = sim(world)
        tap(sim, world, GroundIx.TV)
        tap(sim, world, GroundIx.TV)
        tap(sim, world, GroundIx.CLOCK)
        tap(sim, world, GroundIx.CLOCK)
        tap(sim, world, GroundIx.SIDEBOARD)
        tap(sim, world, GroundIx.LADDER)
        val again = WorldStore.decode(org.json.JSONObject(WorldStore.encode(world, Settings()).toString())).world
        fun back(i: Int) = again.fixtures.getValue(place.idBase + i)
        assertEquals(2, back(GroundIx.TV).mode)
        assertTrue(back(GroundIx.TV).on)
        assertEquals(2, back(GroundIx.CLOCK).count)
        assertTrue(back(GroundIx.SIDEBOARD).open)
        assertEquals(1, back(GroundIx.LADDER).mode)
        // The things came along: Rolf, the cat, the bunny.
        assertEquals(Species.ROBOT, again.people().first { it.name == "Rolf" }.species)
    }

    @Test
    fun `an old save without the house gets the ground floor filled in`() {
        val world = newWorld()
        val json = WorldStore.encode(world, Settings())
        // A save from before the house: no places beyond the older ones.
        val known = PlaceId.entries.filter { !it.manor }.map { it.name }
        json.put("places", org.json.JSONArray(known))
        val bodies = org.json.JSONArray()
        val all = json.getJSONArray("bodies")
        for (i in 0 until all.length()) {
            val o = all.getJSONObject(i)
            if (PlaceId.entries.firstOrNull { it.name == o.optString("place") }?.manor != true) bodies.put(o)
        }
        json.put("bodies", bodies)
        val loaded = WorldStore.decode(json).world
        assertTrue(loaded.people().any { it.name == "Rolf" && it.place == place })
    }
}
