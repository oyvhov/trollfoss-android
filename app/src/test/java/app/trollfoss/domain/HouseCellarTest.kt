package app.trollfoss.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/** Storhuset's cellar: the blueprint, the laundry, the boiler, the pool, the party room and the tunnel. */
class HouseCellarTest {
    private val place = PlaceId.MANOR_CELLAR

    /** What happened: the house's codes with their args, other effects, and the passages taken. */
    private val codes = ArrayList<Pair<Int, Int>>()
    private val effects = ArrayList<Fx>()
    private val passages = ArrayList<Passage>()

    private fun newWorld(seed: Int = 1): World = WorldFactory.create(Random(seed))

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            effects += fx
            if (fx == Fx.HOUSE) codes += HouseFx.code(param) to HouseFx.arg(param)
        }

        override fun onPassage(passage: Passage, arrivalX: Float, riders: Int) {
            passages += passage
        }
    }, Random(3))

    private fun step(sim: Sim, seconds: Float, at: PlaceId = place) {
        var t = 0f
        while (t < seconds) {
            sim.step(at, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun fixture(world: World, ix: Int): Fixture = world.fixtures.getValue(place.idBase + ix)

    private fun count(code: Int) = codes.count { it.first == code }

    private fun person(world: World, x: Float, y: Float = place.floor, at: PlaceId = place) =
        world.addPerson(Species.FOLK, Look(), 1f, at, x, y, "Test")

    private fun sock(world: World, x: Float, y: Float = 0.5f, variant: Int = 0) = world.addThing(ThingType.CE_SOCK, variant, place, x, y)

    // ------------------------------------------------------------------ the blueprint

    @Test
    fun `the cellar is dense and every piece of furniture has a real spec`() {
        val spec = Places.spec(place)
        assertTrue("about fifty pieces of furniture (${spec.fixtures.size})", spec.fixtures.size in 35..50)
        for (def in spec.fixtures) assertNotNull("${def.type} has a spec", FixtureType.valueOf(def.type.name).spec)
        for (type in FixtureType.entries.filter { it.name.startsWith("CE_") }) {
            assertNotNull("$type is a cellar type with its own spec", CellarFloor.specOf(type))
            assertTrue("$type is used in the blueprint", spec.fixtures.any { it.type == type })
        }
        assertEquals(FixtureType.STAIRCASE, spec.fixtures[CellarIx.STAIRS].type)
        assertEquals(FixtureType.SECRET_DOOR, spec.fixtures[CellarIx.TUNNEL_DOOR].type)
        assertEquals(FixtureType.WORKBENCH, spec.fixtures[CellarIx.WORKBENCH].type)
        assertEquals(FixtureType.SAUNA, spec.fixtures[CellarIx.SAUNA].type)
        assertEquals(FixtureType.DISCO_BALL, spec.fixtures[CellarIx.DISCO_BALL].type)
        assertEquals(FixtureType.CE_JUKEBOX, spec.fixtures[CellarIx.JUKEBOX].type)
        assertEquals(FixtureType.CE_DANCE_FLOOR, spec.fixtures[CellarIx.DANCE_FLOOR].type)
        assertEquals(FixtureType.CE_MINE_CART, spec.fixtures[CellarIx.MINE_CART].type)
        assertEquals(FixtureType.CE_SOCK_MONSTER, spec.fixtures[CellarIx.SOCK_MONSTER].type)
        assertEquals(FixtureType.CE_BOILER, spec.fixtures[CellarIx.BOILER].type)
        assertEquals(3, CellarIx.VALVES.size)
        for (ix in CellarIx.VALVES) assertEquals(FixtureType.CE_VALVE, spec.fixtures[ix].type)
        assertEquals(FixtureType.WOOD_STOVE, spec.fixtures[CellarIx.FURNACE].type)
        assertEquals(FixtureType.CE_POOL_SLIDE, spec.fixtures[CellarIx.POOL_SLIDE].type)
        assertEquals(FixtureType.CE_DIVING_BOARD, spec.fixtures[CellarIx.DIVING_BOARD].type)
    }

    @Test
    fun `five rooms cover the floor, each with a name, and the designer sees them`() {
        assertEquals(5, CellarFloor.rooms.size)
        assertEquals(5, Decor.rooms(place).size)
        assertEquals(0f, CellarFloor.rooms.first().start, 0.001f)
        assertEquals(place.width, CellarFloor.rooms.last().endInclusive, 0.001f)
        assertEquals(0.45f, CellarFloor.darkness, 0.001f)
        assertEquals(5, app.trollfoss.ui.S.cellarRooms.size)
        assertEquals(CellarTunes.COUNT, app.trollfoss.ui.S.cellarTunes.size)
    }

    @Test
    fun `the pool is real water with a bed, and things float in it`() {
        val spec = Places.spec(place)
        val water = spec.water!!
        assertEquals(CellarFloor.POOL_X1, water.x1, 0.001f)
        assertTrue(spec.grounds.any { it.y == CellarFloor.POOL_BED && it.x1 == water.x1 && it.x2 == water.x2 })
        val world = newWorld()
        val sim = sim(world)
        step(sim, 0.1f)
        val ring = world.bodiesIn(place).filterIsInstance<Thing>().first { it.type == ThingType.SWIM_RING }
        assertNotNull(sim.poolAt(ring.x, ring.y))
        // A person dropped over the water swims; a person on the deck does not.
        val swimmer = person(world, 6.0f, 0.7f)
        val walker = person(world, 3.9f)
        step(sim, 3f)
        assertEquals(Pose.SWIM, swimmer.anim.pose)
        assertEquals(Pose.STAND, walker.anim.pose)
    }

    // ------------------------------------------------------------------ the way down, the chute and the tunnel

    @Test
    fun `the arrivals keep their names and the chute drops you from the ceiling`() {
        assertNotNull(House.arrival(place, "stairs-top"))
        assertNotNull(House.arrival(place, "tunnel"))
        val chute = House.arrival(place, "laundry-chute-end")!!
        assertTrue("falls from above", chute.y < 0.5f)
        val world = newWorld()
        val sim = sim(world)
        val p = person(world, 5.0f)
        House.moveTo(world, p, place, chute.x, chute.y)
        step(sim, 2f)
        val basket = fixture(world, CellarIx.BASKET)
        assertTrue("lands in the basket", p.resting && p.restOwner == basket.id)
        assertTrue(count(CellarCode.CHUTE) > 0)
    }

    @Test
    fun `knocking on the locked tunnel door wiggles it and three knocks bring out a glimt`() {
        val world = newWorld()
        val sim = sim(world)
        val door = fixture(world, CellarIx.TUNNEL_DOOR)
        val p = person(world, door.x - 0.2f)
        repeat(3) { sim.tap(place, door, 0f, -0.2f) }
        assertEquals("stays where it is", place, p.place)
        assertTrue(passages.isEmpty())
        assertTrue(count(HouseFx.LOCKED) >= 3)
        assertTrue(count(CellarCode.KNOCK) >= 3)
        assertTrue("cellar_tunnel" in world.unlocked)
        assertFalse(door.on)
    }

    @Test
    fun `with all five keys the mine cart takes whoever sits in it to Trollhola and the mine door brings them back`() {
        val world = newWorld()
        val sim = sim(world)
        for (key in HouseKeys.ids) sim.flag(key)
        assertTrue(HouseKeys.TUNNEL in world.flags)
        val door = fixture(world, CellarIx.TUNNEL_DOOR)
        val cart = fixture(world, CellarIx.MINE_CART)
        val rider = person(world, cart.x - 0.4f)
        val other = person(world, cart.x - 0.9f)
        val faraway = person(world, 1.0f)
        sim.settle(place)
        step(sim, 0.2f)
        assertTrue("the door shows its five keys", door.count == 5 && door.on)
        sim.tap(place, door, 0f, -0.2f)
        assertTrue(cart.timer > 0f)
        assertEquals(Mode.SEATED, rider.mode)
        assertEquals(cart.id, rider.holder)
        assertEquals(Mode.SEATED, other.mode)
        step(sim, 2.2f)
        assertTrue("the cart is rolling", cart.shiftX > 0.02f)
        assertTrue("and the riders ride with it", rider.x > cart.x + cart.shiftX - 0.2f)
        step(sim, 4f)
        assertEquals(PlaceId.LAB, rider.place)
        assertEquals(PlaceId.LAB, other.place)
        assertNotEquals("far away stays out of Trollhola", PlaceId.LAB, faraway.place)
        assertEquals("cellar-tunnel", passages.last().id)
        assertEquals(0f, cart.timer, 0f)
        assertEquals(0f, cart.shiftX, 0f)
        assertTrue(count(CellarCode.RIDE_CLACK) > 3)
        assertTrue("cellar_tunnel" in world.unlocked)
        // The way back: the mine door in Trollhola.
        val labDoor = world.fixtures.getValue(PlaceId.LAB.idBase + CellarIx.LAB_DOOR)
        assertEquals(FixtureType.SECRET_DOOR, labDoor.type)
        assertNotNull(House.passageAt(labDoor))
        sim.tap(PlaceId.LAB, labDoor, 0f, -0.2f)
        assertEquals(place, rider.place)
        assertEquals(House.arrival(place, "tunnel")!!.x, rider.x, 0.3f)
        assertEquals("lab-tunnel", passages.last().id)
        assertTrue(count(CellarCode.RIDE_BACK) > 0)
    }

    @Test
    fun `a ride with nobody to ride only rings the bell`() {
        val world = newWorld()
        val sim = sim(world)
        for (key in HouseKeys.ids) sim.flag(key)
        // Everyone leaves the cellar.
        for (p in world.people().filter { it.place == place }) p.place = PlaceId.HOME
        val door = fixture(world, CellarIx.TUNNEL_DOOR)
        val cart = fixture(world, CellarIx.MINE_CART)
        step(sim, 0.1f)
        sim.tap(place, door, 0f, -0.2f)
        assertEquals(0f, cart.timer, 0f)
        assertTrue(count(CellarCode.RIDE_BELL) > 0)
        assertTrue(passages.isEmpty())
    }

    @Test
    fun `the rusty cart will not roll before the tunnel is open`() {
        val world = newWorld()
        val sim = sim(world)
        val cart = fixture(world, CellarIx.MINE_CART)
        val p = person(world, cart.x - 0.2f)
        sim.tap(place, cart, 0f, -0.1f)
        assertEquals(0f, cart.timer, 0f)
        assertEquals(Mode.FREE, p.mode)
        assertTrue(count(CellarCode.RIDE_BELL) > 0)
    }

    // ------------------------------------------------------------------ laundry

    @Test
    fun `the washer takes things in, spins and gives them back`() {
        val world = newWorld()
        val sim = sim(world)
        val washer = fixture(world, CellarIx.WASHER)
        val s = sock(world, washer.x, washer.y - 0.15f)
        assertTrue(sim.dropInto(place, washer, s))
        assertEquals(Mode.INSIDE, s.mode)
        assertEquals(1, world.inMachine(washer).size)
        sim.tap(place, washer, 0f, -0.1f)
        assertTrue(washer.on)
        // A busy washer takes nothing more.
        val another = sock(world, washer.x, washer.y - 0.15f)
        assertFalse(sim.dropInto(place, washer, another))
        step(sim, 4f)
        assertFalse(washer.on)
        assertEquals(Mode.FREE, s.mode)
        assertEquals(0, world.inMachine(washer).size)
        assertTrue(count(CellarCode.WASH) >= 2)
        assertEquals(1, codes.first { it.first == CellarCode.WASH_DONE }.second)
    }

    @Test
    fun `the dryer eats one sock and the sock monster burps it back out`() {
        val world = newWorld()
        val sim = sim(world)
        val dryer = fixture(world, CellarIx.DRYER)
        val monster = fixture(world, CellarIx.SOCK_MONSTER)
        val before = world.bodiesIn(place).count { it is Thing && it.type == ThingType.CE_SOCK }
        val a = sock(world, dryer.x, dryer.y - 0.15f, 1)
        val b = world.addThing(ThingType.BOOK, 0, place, dryer.x, dryer.y - 0.15f)
        assertTrue(sim.dropInto(place, dryer, a))
        assertTrue(sim.dropInto(place, dryer, b))
        sim.tap(place, dryer, 0f, -0.1f)
        step(sim, 5.6f)
        assertEquals("the book comes out", Mode.FREE, b.mode)
        assertTrue(count(CellarCode.DRYER_LOST) == 1)
        assertTrue(count(CellarCode.SOCK_BURP) == 1)
        val after = world.bodiesIn(place).count { it is Thing && it.type == ThingType.CE_SOCK }
        assertEquals("one sock was lost and one came back, so one more than before the test sock", before + 1, after)
        assertEquals("the monster did not count it", 0, monster.count)
    }

    @Test
    fun `the sock monster burps socks out in mismatched pairs`() {
        val world = newWorld()
        val sim = sim(world)
        val monster = fixture(world, CellarIx.SOCK_MONSTER)
        val before = world.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == ThingType.CE_SOCK }.map { it.id }.toSet()
        val s = sock(world, monster.x, monster.y - 0.12f, 2)
        assertTrue(sim.dropInto(place, monster, s))
        assertFalse("eaten", world.bodies.containsKey(s.id))
        assertEquals(1, monster.count)
        step(sim, 1.2f)
        val fresh = world.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == ThingType.CE_SOCK && it.id !in before }
        assertEquals(2, fresh.size)
        assertNotEquals("a mismatched pair", fresh[0].variant, fresh[1].variant)
        assertEquals(1, count(CellarCode.SOCK_EAT))
        assertEquals(1, count(CellarCode.SOCK_BURP))
        assertFalse("no key yet", CellarLaundry.CELLAR_KEY in world.flags)
    }

    @Test
    fun `the third sock shakes the golden key loose, once`() {
        val world = newWorld()
        val sim = sim(world)
        val monster = fixture(world, CellarIx.SOCK_MONSTER)
        // Make the first of the board's tasks the sock task, so we can see it count.
        world.taskSet.clear()
        world.taskSet += "cellar_socks"
        repeat(3) {
            sim.dropInto(place, monster, sock(world, monster.x, monster.y - 0.12f, it))
            step(sim, 2f)
        }
        assertTrue(CellarLaundry.CELLAR_KEY in world.flags)
        assertEquals(1, HouseKeys.found(world))
        val keys = world.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == ThingType.GOLDEN_KEY }
        assertEquals(1, keys.size)
        assertTrue(count(HouseFx.KEY_FOUND) == 1)
        assertEquals(1, count(CellarCode.SOCK_KEY))
        // Another sock gives socks but no second key.
        sim.dropInto(place, monster, sock(world, monster.x, monster.y - 0.12f, 4))
        step(sim, 3f)
        assertEquals(1, world.bodiesIn(place).filterIsInstance<Thing>().count { it.type == ThingType.GOLDEN_KEY })
        // The task board heard of the socks.
        val task = TaskBook.ALL.first { it.id == "cellar_socks" }
        assertEquals(task.need, sim.tasks.progress(task).coerceAtMost(task.need))
        assertTrue(sim.tasks.done(task))
    }

    @Test
    fun `the monster hands back what is not a sock`() {
        val world = newWorld()
        val sim = sim(world)
        val monster = fixture(world, CellarIx.SOCK_MONSTER)
        val cake = world.addThing(ThingType.CAKE, 0, place, monster.x, monster.y - 0.12f)
        assertTrue(sim.dropInto(place, monster, cake))
        assertTrue("still there", world.bodies.containsKey(cake.id))
        assertTrue(cake.vy < 0f)
        assertEquals(0, monster.count)
        assertEquals(1, count(CellarCode.SOCK_BLEH))
    }

    @Test
    fun `the basket under the chute hands out socks and now and then a top`() {
        val world = newWorld()
        val sim = sim(world)
        val basket = fixture(world, CellarIx.BASKET)
        repeat(4) { sim.tap(place, basket, 0f, -0.1f) }
        step(sim, 1f)
        assertEquals(4, basket.count)
        assertEquals(4, count(CellarCode.BASKET_POP))
        assertTrue(world.bodiesIn(place).any { it is Thing && it.type == ThingType.GARMENT })
    }

    // ------------------------------------------------------------------ workshop

    @Test
    fun `the saw saws a plank and Rolf's charging station charges whoever stands in it`() {
        val world = newWorld()
        val sim = sim(world)
        val saw = fixture(world, CellarIx.SAW)
        val planks = { world.bodiesIn(place).count { it is Thing && it.type == ThingType.PLANK } }
        val before = planks()
        sim.tap(place, saw, 0f, -0.1f)
        assertTrue(saw.on)
        step(sim, 2.2f)
        assertFalse(saw.on)
        assertEquals(before + 1, planks())
        val charger = fixture(world, CellarIx.CHARGER)
        val rolf = world.addPerson(Species.ROBOT, Look(skin = 10), 1f, place, charger.x, place.floor, "Rolf")
        assertTrue(sim.seat(rolf, charger, 0))
        step(sim, 1f)
        assertTrue(charger.on)
        assertEquals(1, codes.count { it.first == CellarCode.CHARGE && it.second == 1 })
        step(sim, 2.5f)
        assertEquals(1, charger.mode)
        assertEquals(1, count(CellarCode.CHARGED))
    }

    @Test
    fun `the mouse takes cheese and every third piece buys a coin`() {
        val world = newWorld()
        val sim = sim(world)
        val hole = fixture(world, CellarIx.MOUSE_HOLE)
        sim.tap(place, hole, 0f, -0.03f)
        assertTrue(hole.timer > 0f)
        repeat(3) {
            val cheese = world.addThing(ThingType.BROWN_CHEESE, 0, place, hole.x, hole.y - 0.05f)
            assertTrue(sim.dropInto(place, hole, cheese))
        }
        assertEquals(3, hole.count)
        assertTrue(world.bodiesIn(place).any { it is Thing && it.type == ThingType.COIN })
        val book = world.addThing(ThingType.BOOK, 0, place, hole.x, hole.y - 0.05f)
        assertFalse("only cheese", sim.dropInto(place, hole, book))
    }

    // ------------------------------------------------------------------ boiler room

    @Test
    fun `three open valves make the house rumble and then let off steam`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += "cellar_valves"
        val valves = CellarIx.VALVES.map { fixture(world, it) }
        sim.tap(place, valves[0], 0f, -0.1f)
        sim.tap(place, valves[1], 0f, -0.1f)
        assertEquals(2, valves.count { it.mode == 1 })
        assertFalse(effects.contains(Fx.QUAKE))
        sim.tap(place, valves[2], 0f, -0.1f)
        assertTrue("the whole house rumbles", effects.contains(Fx.QUAKE))
        assertEquals(1, count(CellarCode.VALVES_ALL))
        assertTrue(sim.tasks.done(TaskBook.ALL.first { it.id == "cellar_valves" }))
        step(sim, 2f)
        assertTrue(valves.all { it.mode == 1 })
        val boiler = fixture(world, CellarIx.BOILER)
        assertTrue("the gauge is up", boiler.angle > 0.8f)
        step(sim, 4f)
        assertEquals("a safety valve lets go", 1, count(CellarCode.RELEASE))
        assertTrue("and the valves are shut again", valves.all { it.mode == 0 })
        step(sim, 2f)
        assertTrue(boiler.angle < 0.3f)
        // A valve turns open and shut by itself tap.
        sim.tap(place, valves[1], 0f, -0.1f)
        assertEquals(1, valves[1].mode)
        sim.tap(place, valves[1], 0f, -0.1f)
        assertEquals(0, valves[1].mode)
    }

    // ------------------------------------------------------------------ pool

    @Test
    fun `a diver launched from the springboard lands in the pool`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += "cellar_dive"
        val board = fixture(world, CellarIx.DIVING_BOARD)
        val p = person(world, board.x + 0.15f, board.y - 0.3f)
        sim.settle(place)
        assertTrue(p.resting)
        assertEquals(board.id, p.restOwner)
        sim.tap(place, board, 0.15f, -0.13f)
        assertTrue(p.vx > 0.3f && p.vy < -1f)
        assertEquals(1, codes.first { it.first == CellarCode.DIVE }.second)
        step(sim, 2.5f)
        assertNotNull("in the water", sim.poolAt(p.x, p.y))
        assertEquals(Pose.SWIM, p.anim.pose)
        assertTrue(sim.tasks.done(TaskBook.ALL.first { it.id == "cellar_dive" }))
    }

    @Test
    fun `the slide takes a rider down to the water`() {
        val world = newWorld()
        val sim = sim(world)
        val slide = fixture(world, CellarIx.POOL_SLIDE)
        val p = person(world, slide.x)
        assertTrue(sim.seat(p, slide, 0))
        val top = sim.seatPoint(slide, 0)
        step(sim, 1.0f)
        assertTrue(slide.on)
        val mid = sim.seatPoint(slide, 0)
        assertTrue("moves down and to the left", mid[0] < top[0] && mid[1] > top[1])
        step(sim, 2.5f)
        assertEquals(Mode.FREE, p.mode)
        assertTrue(p.x < slide.x)
        assertNotNull(sim.poolAt(p.x, p.y))
        assertFalse(slide.on)
    }

    @Test
    fun `the rubber duck carries two and drifts across the pool`() {
        val world = newWorld()
        val sim = sim(world)
        val duck = fixture(world, CellarIx.POOL_FLOAT)
        val a = person(world, duck.x)
        val b = person(world, duck.x)
        assertTrue(sim.seat(a, duck, 0))
        assertTrue(sim.seat(b, duck, 1))
        sim.tap(place, duck, -0.05f, -0.08f)
        assertTrue(duck.angleV > 0.2f)
        val x0 = a.x
        step(sim, 2f)
        assertNotEquals(x0, a.x)
        assertTrue("keeps inside the pool", a.x in CellarFloor.POOL_X1..CellarFloor.POOL_X2)
        assertEquals(Mode.SEATED, a.mode)
        assertTrue(abs(a.x - b.x) in 0.05f..0.3f)
    }

    @Test
    fun `a tap on the water sends ripples and the shower runs for a while`() {
        val world = newWorld()
        val sim = sim(world)
        val water = fixture(world, CellarIx.POOL_WATER)
        sim.tap(place, water, 0f, -0.05f)
        assertEquals(1, count(CellarCode.SPLASH_TAP))
        val shower = fixture(world, CellarIx.SHOWER)
        sim.tap(place, shower, 0f, -0.2f)
        assertTrue(shower.on)
        step(sim, 8f)
        assertFalse(shower.on)
        assertEquals(1, codes.count { it.first == CellarCode.SHOWER && it.second == 0 })
    }

    @Test
    fun `the sauna holds a glimt and the bucket makes steam`() {
        val world = newWorld()
        val sim = sim(world)
        val sauna = fixture(world, CellarIx.SAUNA)
        val secret = Secrets.byId("cellar_sauna")!!
        assertFalse(sim.visibleSecrets(place).any { it.id == secret.id })
        sim.tap(place, sauna, 0f, -0.1f)
        assertTrue(sauna.open)
        assertTrue(sim.visibleSecrets(place).any { it.id == secret.id })
        sim.tap(place, fixture(world, CellarIx.SAUNA_BUCKET), 0f, -0.05f)
        assertEquals(1, count(CellarCode.LADLE))
    }

    // ------------------------------------------------------------------ party

    @Test
    fun `the jukebox picks one of five tunes, plays them and turns the disco ball`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += "cellar_dance"
        val juke = fixture(world, CellarIx.JUKEBOX)
        val ball = fixture(world, CellarIx.DISCO_BALL)
        assertFalse(ball.on)
        sim.tap(place, juke, 0f, -0.1f)
        assertEquals("the middle button is the third tune", 3, juke.mode)
        assertTrue(ball.on)
        step(sim, 3f)
        val notes = codes.filter { it.first == CellarCode.JUKE_NOTE }
        assertTrue("notes keep coming (${notes.size})", notes.size >= 8)
        assertTrue(notes.all { it.second / 64 == 2 })
        assertTrue(sim.tasks.done(TaskBook.ALL.first { it.id == "cellar_dance" }))
        // The same button stops it; the top cycles on to the next tune.
        sim.tap(place, juke, 0f, -0.1f)
        assertEquals(0, juke.mode)
        assertFalse(ball.on)
        sim.tap(place, juke, 0f, -0.3f)
        assertEquals(1, juke.mode)
        sim.tap(place, juke, 0f, -0.3f)
        assertEquals(2, juke.mode)
        // A tap on the ball ends the party too.
        sim.tap(place, ball, 0f, -0.05f)
        assertEquals(0, juke.mode)
        assertFalse(ball.on)
    }

    @Test
    fun `every tune sticks to the pentatonic scale and has the same length for melody and bass`() {
        for (tune in 0 until CellarTunes.COUNT) {
            val length = CellarTunes.length(tune)
            assertTrue(length in 24..32)
            assertTrue(CellarTunes.stepSeconds(tune) in 0.1f..0.4f)
            var notes = 0
            for (step in 0 until length) {
                val m = CellarTunes.melody(tune, step)
                assertTrue(m in -1..9)
                if (m >= 0) notes++
                val bass = CellarTunes.bass(tune, step)
                assertTrue(bass == CellarTunes.NO_BASS || bass in -12..0)
            }
            assertTrue("tune $tune has a tune in it", notes >= length / 3)
        }
    }

    @Test
    fun `the dance floor lights the tile under dancing feet and the party brings a glimt`() {
        val world = newWorld()
        val sim = sim(world)
        val floor = fixture(world, CellarIx.DANCE_FLOOR)
        val ball = fixture(world, CellarIx.DISCO_BALL)
        val dancer = person(world, floor.x)
        sim.settle(place)
        step(sim, 0.5f)
        assertEquals("nothing lights while the ball is still", 0f, floor.angleV, 0f)
        ball.on = true
        step(sim, 0.5f)
        val mask = floor.angleV.toInt()
        assertNotEquals(0, mask)
        assertEquals("one tile under one pair of feet", 1, Integer.bitCount(mask))
        assertTrue(floor.on)
        // Moving to another spot lights another tile.
        dancer.x += 0.45f
        step(sim, 0.4f)
        assertNotEquals(mask, floor.angleV.toInt())
        assertFalse("not yet: no tune is playing", "cellar_party" in world.unlocked)
        // With a tune playing, dancing for a moment brings the glimt.
        sim.tap(place, fixture(world, CellarIx.JUKEBOX), 0f, -0.1f)
        step(sim, 3f)
        assertTrue("cellar_party" in world.unlocked)
    }

    @Test
    fun `the snack bar hands out party food by where you tap`() {
        val world = newWorld()
        val sim = sim(world)
        val bar = fixture(world, CellarIx.SNACK_BAR)
        sim.tap(place, bar, -0.15f, -0.2f)
        sim.tap(place, bar, 0f, -0.2f)
        sim.tap(place, bar, 0.15f, -0.2f)
        assertEquals(listOf(0, 1, 2), codes.filter { it.first == CellarCode.BAR_POP }.map { it.second })
        val made = world.bodiesIn(place).filterIsInstance<Thing>()
        assertTrue(made.any { it.type == ThingType.POPCORN })
        assertTrue(made.count { it.type == ThingType.SODA } >= 2)
    }

    @Test
    fun `lamps start lit, switch off with a tap, and the stairs are always lit`() {
        val world = newWorld()
        val sim = sim(world)
        val bulb = world.fixturesIn(place).first { it.type == FixtureType.CE_BULB }
        val neon = world.fixturesIn(place).first { it.type == FixtureType.CE_NEON }
        val stairs = fixture(world, CellarIx.STAIRS)
        step(sim, 0.1f)
        assertTrue(bulb.on && neon.on && stairs.on)
        sim.tap(place, bulb, 0f, -0.05f)
        assertFalse(bulb.on)
        step(sim, 0.1f)
        assertFalse("stays off", bulb.on)
        sim.tap(place, bulb, 0f, -0.05f)
        assertTrue(bulb.on)
        sim.tap(place, neon, 0f, -0.05f)
        assertFalse(neon.on)
    }

    @Test
    fun `the karaoke screen wakes up when someone sings into the microphone`() {
        val world = newWorld()
        val sim = sim(world)
        val screen = fixture(world, CellarIx.KARAOKE)
        val mic = fixture(world, CellarIx.MIC_STAND)
        person(world, mic.x)
        sim.settle(place)
        sim.tap(place, mic, 0f, -0.2f)
        step(sim, 0.2f)
        assertTrue(screen.on)
        assertEquals(1, screen.mode)
        step(sim, 5f)
        assertEquals(0, screen.mode)
    }

    @Test
    fun `the confetti cannon fires with a cooldown`() {
        val world = newWorld()
        val sim = sim(world)
        val cannon = fixture(world, CellarIx.CONFETTI)
        sim.tap(place, cannon, 0f, -0.1f)
        sim.tap(place, cannon, 0f, -0.1f)
        assertEquals(1, count(CellarCode.CONFETTI))
        step(sim, 1.5f)
        sim.tap(place, cannon, 0f, -0.1f)
        assertEquals(2, count(CellarCode.CONFETTI))
    }

    // ------------------------------------------------------------------ glimt, tasks, saving

    @Test
    fun `three glimt and four tasks belong to the cellar`() {
        val secrets = Secrets.inPlace(place)
        assertEquals(3, secrets.size)
        assertTrue(secrets.all { it.id.startsWith("cellar_") })
        val cellarTasks = TaskBook.ALL.filter { it.place == place }
        assertEquals(4, cellarTasks.size)
        assertTrue(cellarTasks.all { it.id.startsWith("cellar_") })
        // Every task is something a four year old can do without having found anything first.
        assertTrue(cellarTasks.all { it.need <= 2 })
    }

    @Test
    fun `the state of the cellar is saved and comes back`() {
        val world = newWorld()
        val sim = sim(world)
        val valve = fixture(world, CellarIx.VALVES[1])
        sim.tap(place, valve, 0f, -0.1f)
        val monster = fixture(world, CellarIx.SOCK_MONSTER)
        repeat(3) { sim.dropInto(place, monster, sock(world, monster.x, monster.y - 0.12f, it)) }
        step(sim, 3f)
        sim.tap(place, fixture(world, CellarIx.JUKEBOX), 0.06f, -0.1f)
        val bulb = world.fixturesIn(place).first { it.type == FixtureType.CE_BULB }
        sim.tap(place, bulb, 0f, -0.05f)
        val json = app.trollfoss.data.WorldStore.encode(world, app.trollfoss.data.Settings()).toString()
        val saved = app.trollfoss.data.WorldStore.decode(org.json.JSONObject(json))
        val back = saved.world
        assertEquals(1, back.fixtures.getValue(valve.id).mode)
        assertEquals(3, back.fixtures.getValue(monster.id).count)
        assertEquals(5, back.fixtures.getValue(fixture(world, CellarIx.JUKEBOX).id).mode)
        assertEquals(1, back.fixtures.getValue(bulb.id).mode)
        assertTrue(CellarLaundry.CELLAR_KEY in back.flags)
        assertTrue(back.bodiesIn(place).any { it is Thing && it.type == ThingType.GOLDEN_KEY })
        // A loaded world carries on: the jukebox is still playing when the cellar comes on screen.
        val sim2 = sim(back)
        val before = codes.size
        step(sim2, 1f)
        assertTrue(codes.size > before)
    }

    @Test
    fun `the sock is a thing everyone knows how to carry`() {
        assertEquals(6, ThingType.CE_SOCK.variants)
        assertTrue(ThingType.CE_SOCK.w < 0.1f)
        val world = newWorld()
        val p = person(world, 3.0f)
        val s = sock(world, p.x, p.y - 0.1f)
        val sim = sim(world)
        assertEquals(Give.HELD, sim.give(p, s, Part.HAND))
    }
}
