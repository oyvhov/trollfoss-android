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

/** Storhuset, first floor: the blueprint, the rules, the glimt, the golden key, the tasks and the saves. */
class HouseUpperTest {
    private val place = PlaceId.MANOR_UPPER

    /** Every effect of the floor as (code, arg), in the order it went out. */
    private val events = ArrayList<Pair<Int, Int>>()
    private val spawned = ArrayList<Thing>()

    private fun sim(world: World, seed: Int = 5) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            if (fx != Fx.HOUSE) return
            val code = HouseFx.code(param)
            if (code in HouseFx.UPPER until HouseFx.ATTIC) events += (code - HouseFx.UPPER) to HouseFx.arg(param)
        }

        override fun onSpawn(body: Body) {
            if (body is Thing) spawned += body
        }
    }, Random(seed))

    private fun world(seed: Int = 1) = WorldFactory.create(Random(seed))

    private fun fixture(world: World, index: Int): Fixture = world.fixtures.getValue(place.idBase + index)

    private fun run(sim: Sim, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun saw(code: Int) = events.any { it.first == code }

    private fun folk(world: World, x: Float, name: String = "Gjest", height: Float = 0.78f): Person {
        val p = world.addPerson(Species.FOLK, Look(height = height), 1.2f, place, x, place.floor, name)
        p.ground = place.floor
        return p
    }

    // ------------------------------------------------------------------ blueprint

    @Test
    fun `the blueprint has the passages, the rooms and a dense floor of furniture`() {
        val spec = Places.spec(place)
        assertTrue("dense: ${spec.fixtures.size}", spec.fixtures.size in 40..60)
        val ix = HouseUpperIx
        assertEquals(FixtureType.STAIRCASE, spec.fixtures[ix.STAIRS_UP].type)
        assertEquals(FixtureType.STAIRCASE, spec.fixtures[ix.STAIRS_DOWN].type)
        assertEquals(FixtureType.LIFT, spec.fixtures[ix.LIFT].type)
        assertEquals(FixtureType.SLIDE, spec.fixtures[ix.SLIDE].type)
        assertEquals(FixtureType.SLIDE, spec.fixtures[ix.BALCONY_SLIDE].type)
        assertEquals(FixtureType.FIRE_POLE, spec.fixtures[ix.POLE].type)
        assertEquals(FixtureType.DUMBWAITER, spec.fixtures[ix.DUMBWAITER].type)
        assertEquals(FixtureType.HATCH, spec.fixtures[ix.HATCH].type)
        val named = mapOf(
            ix.WINDOW_SEAT to FixtureType.UP_WINDOW_SEAT, ix.BUNK to FixtureType.BUNK_BED, ix.DOLLHOUSE to FixtureType.UP_DOLLHOUSE,
            ix.FORT to FixtureType.UP_FORT, ix.NIGHT_LAMP to FixtureType.UP_NIGHT_LAMP, ix.TRAIN to FixtureType.UP_TOY_TRAIN,
            ix.BLOCKS to FixtureType.UP_BLOCKS, ix.TOY_BOX to FixtureType.TOY_BOX, ix.CLIMB to FixtureType.UP_CLIMBING_WALL,
            ix.KARAOKE to FixtureType.UP_KARAOKE, ix.TRAMP to FixtureType.UP_TRAMPOLINE, ix.PIT to FixtureType.UP_BALL_PIT,
            ix.EASEL to FixtureType.UP_EASEL, ix.PUPPETS to FixtureType.UP_PUPPET_THEATER, ix.SHOWER to FixtureType.UP_SHOWER,
            ix.BATH to FixtureType.BATH, ix.TOILET to FixtureType.TOILET, ix.SINK to FixtureType.SINK,
            ix.MIRROR to FixtureType.UP_BATH_MIRROR, ix.TOWELS to FixtureType.UP_TOWELS, ix.BED to FixtureType.BED,
            ix.STAR_WINDOW to FixtureType.UP_WINDOW, ix.VANITY to FixtureType.UP_VANITY, ix.JEWEL to FixtureType.UP_JEWEL_BOX,
            ix.WARDROBE to FixtureType.UP_WARDROBE, ix.ROCKER to FixtureType.UP_ROCKING_CHAIR, ix.RAILING to FixtureType.UP_RAILING,
            ix.HANGING to FixtureType.UP_HANGING_CHAIR, ix.FEEDER to FixtureType.UP_BIRD_FEEDER, ix.TELESCOPE to FixtureType.TELESCOPE,
        )
        for ((index, type) in named) assertEquals("blueprint index $index", type, spec.fixtures[index].type)
        for (i in ix.PORTRAIT_FIRST..ix.PORTRAIT_LAST) assertEquals(FixtureType.UP_PORTRAIT, spec.fixtures[i].type)
        // The portraits are six different people.
        assertEquals(6, (ix.PORTRAIT_FIRST..ix.PORTRAIT_LAST).map { spec.fixtures[it].variant }.toSet().size)
        assertEquals(6, UpperFloor.rooms.size)
        assertEquals(0f, UpperFloor.rooms.first().start, 0.001f)
        assertEquals(12f, UpperFloor.rooms.last().endInclusive, 0.001f)
        // The designer sees the same six rooms.
        assertEquals(UpperFloor.rooms, Decor.rooms(place))
    }

    @Test
    fun `every type of the floor has a spec, and the hanging ones are on the wall`() {
        val own = FixtureType.entries.filter { it.name.startsWith("UP_") }
        assertTrue(own.size >= 20)
        for (type in own) {
            assertNotNull("$type has a spec", UpperFloor.specOf(type))
            assertEquals(UpperFloor.specOf(type)!!.w, type.spec.w, 0f)
        }
        for (type in listOf(FixtureType.UP_PORTRAIT, FixtureType.UP_WINDOW, FixtureType.UP_POSTER, FixtureType.UP_MOBILE, FixtureType.UP_BATH_MIRROR, FixtureType.UP_TOWELS, FixtureType.UP_BIRD_FEEDER)) {
            assertTrue("$type hangs on the wall", type.spec.wall)
        }
        // Seats, holds and the microphone are where the rules expect them.
        assertEquals(3, FixtureType.UP_BALL_PIT.spec.spots.size)
        assertEquals(4, FixtureType.UP_CLIMBING_WALL.spec.spots.size)
        assertTrue(FixtureType.UP_CLIMBING_WALL.spec.spots.all { it.pose == Pose.HELD })
        assertEquals(Pose.STAND, FixtureType.UP_KARAOKE.spec.spots[0].pose)
        assertNotNull(FixtureType.UP_WARDROBE.spec.container)
        assertNotNull(FixtureType.UP_FORT.spec.container)
        assertTrue(FixtureType.UP_TRAMPOLINE.spec.surfaces.single().bounce >= 1f)
    }

    @Test
    fun `only animals live here for good, and nobody is named twice`() {
        val world = world()
        val here = world.bodiesIn(place).filterIsInstance<Person>()
        assertTrue(here.isNotEmpty())
        assertTrue(here.all { it.species != Species.FOLK })
        val names = world.people().map { it.name }.filter { it.isNotBlank() }
        assertEquals(names.size, names.toSet().size)
        // The cat sits on the window seat.
        val cat = here.first { it.species == Species.CAT }
        assertEquals(Mode.SEATED, cat.mode)
        assertEquals(place.idBase + HouseUpperIx.WINDOW_SEAT, cat.holder)
    }

    @Test
    fun `people have somewhere to be by day and by night`() {
        assertTrue(UpperFloor.hangouts(false).size >= 5)
        assertTrue(UpperFloor.hangouts(true).any { it.name == "bedroom" })
        for (a in UpperFloor.hangouts(false) + UpperFloor.hangouts(true)) assertTrue("${a.name} inside", a.x in 0f..place.width)
    }

    // ------------------------------------------------------------------ the toy train

    @Test
    fun `the toy train runs a loop, takes a passenger, and parks at the station`() {
        val world = world()
        val sim = sim(world)
        val train = fixture(world, HouseUpperIx.TRAIN)
        assertFalse(train.on)
        val kid = folk(world, train.x)
        assertTrue(sim.seat(kid, train, 0))
        val before = kid.x
        world.night = false
        sim.tap(place, train, 0f, -0.05f)
        assertTrue(train.on)
        assertTrue(saw(UpperCodes.TRAIN_GO))
        run(sim, 2.0f)
        assertTrue("the passenger rides along", kid.x != before || kid.y != fixture(world, HouseUpperIx.TRAIN).y)
        // Riding counts as a task for the board.
        world.taskSet.clear()
        world.taskSet += listOf("up_train", "tidy", "photo")
        run(sim, 2.5f)
        assertEquals(1, sim.tasks.progress(sim.tasks.board().first { it.id == "up_train" }))
        // Tap again: it rolls into the station and stops there.
        sim.tap(place, train, 0f, -0.05f)
        run(sim, 12f)
        assertFalse(train.on)
        assertEquals(UpperTrack.STATION, train.angle, 0.001f)
        assertTrue(saw(UpperCodes.TRAIN_STOP))
        assertTrue(saw(UpperCodes.TRAIN_CHUFF))
        assertEquals(Mode.SEATED, kid.mode)
    }

    @Test
    fun `the train carries a second passenger in the second wagon`() {
        val world = world()
        val sim = sim(world)
        val train = fixture(world, HouseUpperIx.TRAIN)
        val a = folk(world, train.x, "A")
        val b = folk(world, train.x, "B")
        assertTrue(sim.seat(a, train, 0))
        assertTrue(sim.seat(b, train, 1))
        assertNotEquals(a.x, b.x)
        assertFalse(sim.seat(folk(world, train.x, "C"), train, 0))
    }

    private fun assertNotEquals(a: Float, b: Float) = assertTrue("$a differs from $b", a != b)

    // ------------------------------------------------------------------ blocks

    @Test
    fun `the tower of blocks falls on a tap and a ball, and can be built again`() {
        val world = world()
        val sim = sim(world)
        val tower = fixture(world, HouseUpperIx.BLOCKS)
        world.taskSet.clear()
        world.taskSet += listOf("up_blocks", "tidy", "photo")
        sim.tap(place, tower, 0f, -0.2f)
        assertEquals(1, tower.mode)
        assertTrue(tower.open)
        assertTrue(saw(UpperCodes.BLOCKS_FALL))
        assertTrue(sim.tasks.done(sim.tasks.board().first { it.id == "up_blocks" }))
        assertTrue("loose blocks tumble out", spawned.any { it.type == ThingType.UP_BLOCK })
        sim.tap(place, tower, 0f, -0.1f)
        assertEquals(0, tower.mode)
        assertFalse(tower.open)
        assertTrue(saw(UpperCodes.BLOCKS_BUILD))
        // A ball let go over the standing tower brings it down too, and still falls on.
        val ball = world.addThing(ThingType.BALL, 0, place, tower.x, tower.y - 0.2f)
        assertFalse(sim.dropInto(place, tower, ball))
        assertEquals(1, tower.mode)
        run(sim, 3f)
        assertTrue(tower.timer > 2f)
    }

    @Test
    fun `a teddy on top of the tower comes down with it`() {
        val world = world()
        val sim = sim(world)
        val tower = fixture(world, HouseUpperIx.BLOCKS)
        val teddy = world.addThing(ThingType.TEDDY, 0, place, tower.x, tower.y - 0.5f)
        run(sim, 2f)
        assertTrue("the teddy sits on the top", teddy.resting && teddy.restOwner == tower.id)
        sim.tap(place, tower, 0f, -0.2f)
        run(sim, 1.5f)
        assertTrue(teddy.y > tower.y - 0.2f)
    }

    // ------------------------------------------------------------------ portraits and the dollhouse

    @Test
    fun `a tapped portrait waves and the whole gallery follows`() {
        val world = world()
        val sim = sim(world)
        val first = fixture(world, HouseUpperIx.PORTRAIT_FIRST)
        sim.tap(place, first, 0f, -0.1f)
        assertTrue(first.timer > 0f)
        run(sim, 2.5f)
        val waved = events.filter { it.first == UpperCodes.PORTRAIT }.map { it.second and 0xFF }.toSet()
        assertEquals("all six waved", 6, waved.size)
        assertTrue(first.timer < 0.5f)
    }

    @Test
    fun `now and then a portrait waves by itself`() {
        val world = world()
        val sim = sim(world, 11)
        run(sim, 60f)
        assertTrue(events.any { it.first == UpperCodes.PORTRAIT && it.second and 0x100 != 0 })
    }

    @Test
    fun `the dollhouse shows who is where in the big house`() {
        val world = world()
        val sim = sim(world)
        val wanderer = world.addPerson(Species.FOLK, Look(topColor = 3, height = 0.78f), 1.2f, PlaceId.MANOR_CELLAR, 5f, PlaceId.MANOR_CELLAR.floor, "Vandrar")
        val upstairs = folk(world, 6f, "Oppe")
        run(sim, 1f)
        val figures = UpperMirror.figures
        val cellar = figures.firstOrNull { it.floor == 3 }
        assertNotNull(cellar)
        assertEquals(0.5f, cellar!!.x, 0.01f)
        assertEquals(1, cellar.kind)
        assertTrue(figures.any { it.floor == 1 })
        // When the figure takes the stairs, the dollhouse shows it a moment later.
        House.moveTo(world, wanderer, PlaceId.MANOR_ATTIC, 2f)
        House.moveTo(world, upstairs, PlaceId.MANOR_GARDEN, 3f)
        run(sim, 0.5f)
        assertTrue(UpperMirror.figures.any { it.floor == 2 })
        assertTrue(UpperMirror.figures.any { it.floor == 4 })
        sim.tap(place, fixture(world, HouseUpperIx.DOLLHOUSE), 0f, -0.1f)
        assertTrue(saw(UpperCodes.DOLL_BELL))
    }

    // ------------------------------------------------------------------ the playroom

    @Test
    fun `figures hop into the ball pit and the pit bobs`() {
        val world = world()
        val sim = sim(world)
        val pit = fixture(world, HouseUpperIx.PIT)
        val kid = folk(world, pit.x)
        assertTrue(sim.seat(kid, pit, 1))
        run(sim, 0.5f)
        assertTrue(events.any { it.first == UpperCodes.PIT_PLUNGE && it.second == kid.id })
        val rest = sim.seatPoint(pit, 1)
        run(sim, 0.2f)
        assertTrue(pit.timer > 0f)
        sim.tap(place, pit, 0f, -0.1f)
        sim.tap(place, pit, 0f, -0.1f)
        sim.tap(place, pit, 0f, -0.1f)
        assertTrue("a ball pops out on the third", spawned.any { it.type == ThingType.BALL || it.type == ThingType.BEACH_BALL })
        assertEquals(3, pit.count)
        assertTrue(saw(UpperCodes.PIT_BURST))
        assertNotNull(rest)
        assertEquals(Pose.SIT, kid.anim.pose)
    }

    @Test
    fun `a child put on the climbing wall climbs to the top, rings the bell and drops onto the trampoline`() {
        val world = world()
        val sim = sim(world)
        val wall = fixture(world, HouseUpperIx.CLIMB)
        val kid = folk(world, wall.x)
        assertTrue(sim.seat(kid, wall, 0))
        assertEquals(Pose.HELD, kid.anim.pose)
        run(sim, 2.0f)
        assertEquals(1, kid.slot)
        run(sim, 3.5f)
        assertEquals(3, kid.slot)
        assertTrue(saw(UpperCodes.CLIMB_TOP))
        run(sim, 3f)
        assertEquals(Mode.FREE, kid.mode)
        assertTrue(saw(UpperCodes.CLIMB_DROP))
    }

    @Test
    fun `the easel squirts paint, a brush dropped on it too, and a task counts it`() {
        val world = world()
        val sim = sim(world)
        val easel = fixture(world, HouseUpperIx.EASEL)
        world.taskSet.clear()
        world.taskSet += listOf("up_paint", "tidy", "photo")
        sim.tap(place, easel, 0f, -0.3f)
        sim.tap(place, easel, 0f, -0.3f)
        assertEquals(2, easel.count)
        val brush = world.addThing(ThingType.UP_PAINTBRUSH, 1, place, easel.x, easel.y - 0.3f)
        assertFalse(sim.dropInto(place, easel, brush))
        assertEquals(3, easel.count)
        assertTrue(sim.tasks.done(sim.tasks.board().first { it.id == "up_paint" }))
        val colours = events.filter { it.first == UpperCodes.PAINT }.map { it.second and 0xFF }
        assertEquals(listOf(1, 2, 3), colours)
        repeat(10) { sim.tap(place, easel, 0f, -0.3f) }
        assertTrue("every thirteenth squirt is a fresh canvas", events.any { it.first == UpperCodes.PAINT && it.second and 0x100 != 0 })
    }

    @Test
    fun `whoever steps up to the microphone sings, and the lights go down again`() {
        val world = world()
        val sim = sim(world)
        val stage = fixture(world, HouseUpperIx.KARAOKE)
        val kid = folk(world, stage.x)
        assertTrue(sim.seat(kid, stage, 0))
        run(sim, 0.3f)
        assertTrue(stage.on)
        assertTrue(events.any { it.first == UpperCodes.KARAOKE && it.second == kid.id })
        run(sim, 10f)
        assertFalse(stage.on)
        // A tap with nobody there still gives a show, without a singer.
        kid.mode = Mode.FREE
        kid.holder = -1
        kid.y = 0.5f
        sim.tap(place, stage, 0f, -0.1f)
        assertTrue(stage.on)
        assertTrue(events.any { it.first == UpperCodes.KARAOKE && it.second == 0xFFFF })
    }

    // ------------------------------------------------------------------ the bathroom

    @Test
    fun `the shower brings a rainbow and a glimt, and a bather sings`() {
        val world = world()
        val sim = sim(world)
        val shower = fixture(world, HouseUpperIx.SHOWER)
        val kid = folk(world, shower.x)
        assertTrue(sim.seat(kid, shower, 0))
        sim.tap(place, shower, 0f, -0.2f)
        assertTrue(shower.on)
        assertTrue("upper_bath" !in world.unlocked)
        run(sim, 2f)
        assertTrue(saw(UpperCodes.RAINBOW))
        assertTrue("upper_bath" in world.unlocked)
        assertTrue(events.any { it.first == UpperCodes.SHOWER_SING && it.second == kid.id })
        run(sim, 30f)
        assertFalse("the shower turns itself off", shower.on)
    }

    @Test
    fun `the mirror fogs in the steam, shows a smiley and clears again`() {
        val world = world()
        val sim = sim(world)
        val shower = fixture(world, HouseUpperIx.SHOWER)
        val mirror = fixture(world, HouseUpperIx.MIRROR)
        sim.tap(place, mirror, 0f, -0.1f)
        assertEquals("a clear mirror just sparkles", 0, mirror.mode)
        sim.tap(place, shower, 0f, -0.2f)
        run(sim, 4f)
        assertEquals(1, mirror.mode)
        sim.tap(place, mirror, 0f, -0.1f)
        assertEquals("a finger draws a smiley", 2, mirror.mode)
        sim.tap(place, mirror, 0f, -0.1f)
        assertEquals(0, mirror.mode)
        sim.tap(place, shower, 0f, -0.2f)
        run(sim, 12f)
        assertEquals(0, mirror.mode)
    }

    @Test
    fun `the bath with its ducks gives up the golden key, once`() {
        val world = world()
        val sim = sim(world)
        val bath = fixture(world, HouseUpperIx.BATH)
        assertEquals(3, world.bodiesIn(place).count { it is Thing && it.type == ThingType.DUCK })
        assertFalse("manor_key_upper" in world.flags)
        sim.tap(place, bath, 0f, -0.05f)
        assertTrue(bath.on)
        run(sim, 3f)
        assertTrue("the ducks float", world.bodiesIn(place).filter { it is Thing && it.type == ThingType.DUCK }.all { !it.resting })
        run(sim, 6f)
        assertTrue("manor_key_upper" in world.flags)
        assertEquals(1, HouseKeys.found(world))
        val key = world.bodiesIn(place).filterIsInstance<Thing>().firstOrNull { it.type == ThingType.GOLDEN_KEY }
        assertNotNull(key)
        assertTrue(saw(UpperCodes.DUCK_KEY))
        // No second key from the same bath.
        sim.tap(place, bath, 0f, -0.05f)
        sim.tap(place, bath, 0f, -0.05f)
        run(sim, 10f)
        assertEquals(1, world.bodiesIn(place).count { it is Thing && it.type == ThingType.GOLDEN_KEY })
    }

    @Test
    fun `the towels hide rubber ducks now and then and the laundry chute takes you to the cellar`() {
        val world = world()
        val sim = sim(world, 9)
        val towels = fixture(world, HouseUpperIx.TOWELS)
        repeat(12) { sim.tap(place, towels, 0f, -0.1f) }
        assertTrue(spawned.any { it.type == ThingType.DUCK })
        assertTrue(world.bodiesIn(place).count { it is Thing && it.type == ThingType.DUCK } <= 6)
        val hatch = fixture(world, HouseUpperIx.HATCH)
        val kid = folk(world, hatch.x)
        sim.tap(place, hatch, 0f, -0.03f)
        assertEquals(PlaceId.MANOR_CELLAR, kid.place)
    }

    // ------------------------------------------------------------------ bedroom

    @Test
    fun `the wardrobe dresses the nearest figure in a new outfit with a hat`() {
        val world = world()
        val sim = sim(world)
        val wardrobe = fixture(world, HouseUpperIx.WARDROBE)
        world.taskSet.clear()
        world.taskSet += listOf("up_dress", "tidy", "photo")
        val kid = folk(world, wardrobe.x - 0.25f)
        val far = folk(world, wardrobe.x - 1.5f, "Langt")
        val oldLook = kid.look
        sim.tap(place, wardrobe, 0f, -0.2f)
        assertTrue(wardrobe.open)
        assertNotEquals(oldLook.topColor.toFloat() + oldLook.top * 100f, kid.look.topColor.toFloat() + kid.look.top * 100f)
        assertEquals(oldLook.skin, kid.look.skin)
        assertEquals(oldLook.hair, kid.look.hair)
        assertTrue("a hat or glasses on", world.carried(kid).isNotEmpty())
        assertTrue(world.carried(far).isEmpty())
        assertTrue(events.any { it.first == UpperCodes.DRESS && it.second == kid.id })
        assertTrue(sim.tasks.done(sim.tasks.board().first { it.id == "up_dress" }))
        // Closing is quiet; opening for nobody tosses a top and a funny hat instead.
        sim.tap(place, wardrobe, 0f, -0.2f)
        assertFalse(wardrobe.open)
        kid.x = 1f
        far.x = 2f
        sim.tap(place, wardrobe, 0f, -0.2f)
        assertTrue(saw(UpperCodes.WARDROBE_TOSS))
        assertTrue(spawned.any { it.type == ThingType.GARMENT })
    }

    @Test
    fun `a slipper makes a hat`() {
        val world = world()
        val sim = sim(world)
        val kid = folk(world, 3f)
        val slipper = world.addThing(ThingType.UP_SLIPPER, 1, place, 3f, 0.8f)
        assertEquals(Give.WORE, sim.give(kid, slipper, Part.HAT))
        assertEquals(ThingType.UP_SLIPPER, world.worn(kid, Slot.HEAD)?.type)
    }

    @Test
    fun `the ballerina dances for a while and then the box shuts`() {
        val world = world()
        val sim = sim(world)
        val box = fixture(world, HouseUpperIx.JEWEL)
        val vanity = fixture(world, HouseUpperIx.VANITY)
        assertEquals(vanity.id, box.host)
        sim.tap(place, box, 0f, -0.05f)
        assertTrue(box.on)
        run(sim, 2f)
        assertTrue(box.angle > 3f)
        assertTrue(box.anim > 0f)
        run(sim, 10f)
        assertFalse(box.on)
        assertEquals(0f, box.angle, 0f)
        assertTrue(events.count { it.first == UpperCodes.JEWEL } >= 2)
    }

    @Test
    fun `the rocking chair rocks with its rider and the hanging chair swings`() {
        val world = world()
        val sim = sim(world)
        val chair = fixture(world, HouseUpperIx.ROCKER)
        val kid = folk(world, chair.x)
        assertTrue(sim.seat(kid, chair, 0))
        val x0 = kid.x
        var moved = 0f
        var t = 0f
        while (t < 3f) {
            sim.step(place, 1f / 60f)
            moved = maxOf(moved, kotlin.math.abs(kid.x - x0))
            t += 1f / 60f
        }
        assertTrue("the rider rocks: $moved", moved > 0.005f)
        val swing = fixture(world, HouseUpperIx.HANGING)
        sim.tap(place, swing, 0f, -0.2f)
        run(sim, 0.5f)
        assertTrue(kotlin.math.abs(swing.angle) > 0.01f)
        run(sim, 30f)
        assertEquals(0f, swing.angle, 0.01f)
    }

    // ------------------------------------------------------------------ fort, birds and the house

    @Test
    fun `the glimt are three, one inside the fort, and every one can be found`() {
        val glimt = Secrets.inPlace(place)
        assertEquals(3, glimt.size)
        assertTrue(glimt.all { it.id.startsWith("upper_") })
        val world = world()
        val sim = sim(world)
        val fort = fixture(world, HouseUpperIx.FORT)
        assertTrue("shut fort hides its star", sim.visibleSecrets(place).none { it.id == "upper_fort" })
        sim.tap(place, fort, 0f, -0.1f)
        assertTrue(fort.open)
        assertTrue(sim.visibleSecrets(place).any { it.id == "upper_fort" })
        for (s in glimt) {
            if (s.event) sim.unlock(s.id)
            assertTrue("${s.id} visible", sim.visibleSecrets(place).any { it.id == s.id })
            assertTrue(sim.collect(s.id))
        }
        // The fort holds a figure lying down inside, hidden when it is shut.
        val kid = folk(world, fort.x)
        assertTrue(sim.seat(kid, fort, 0))
        assertEquals(Pose.LIE, kid.anim.pose)
    }

    @Test
    fun `the birds visit a full feeder, bring a glimt, and leave it empty after six visits until it is filled`() {
        val world = world()
        val sim = sim(world, 21)
        val feeder = fixture(world, HouseUpperIx.FEEDER)
        assertTrue("upper_balcony" !in world.unlocked)
        run(sim, 8f)
        assertTrue("upper_balcony" in world.unlocked)
        assertTrue(events.any { it.first == UpperCodes.BIRD })
        run(sim, 80f)
        assertEquals(6, feeder.count)
        val visits = events.count { it.first == UpperCodes.BIRD }
        run(sim, 30f)
        assertEquals("empty: no more visitors", visits, events.count { it.first == UpperCodes.BIRD })
        // Seeds dropped on it fill it again.
        val seeds = world.addThing(ThingType.SEEDS, 0, place, feeder.x, feeder.y - 0.15f)
        assertTrue(sim.dropInto(place, feeder, seeds))
        assertFalse(world.bodies.containsKey(seeds.id))
        assertEquals(0, feeder.count)
        run(sim, 12f)
        assertTrue(events.count { it.first == UpperCodes.BIRD } > visits)
    }

    @Test
    fun `lamps, theatre, poster and the mobile all answer a tap`() {
        val world = world()
        val sim = sim(world)
        val lamp = fixture(world, HouseUpperIx.NIGHT_LAMP)
        sim.tap(place, lamp, 0f, -0.1f)
        assertTrue(lamp.on)
        sim.tap(place, lamp, 0f, -0.1f)
        assertFalse(lamp.on)
        val theatre = fixture(world, HouseUpperIx.PUPPETS)
        val modes = (0 until 3).map { sim.tap(place, theatre, 0f, -0.1f); theatre.mode }
        assertEquals(listOf(1, 2, 0), modes)
        val poster = world.fixturesIn(place).first { it.type == FixtureType.UP_POSTER }
        sim.tap(place, poster, 0f, -0.1f)
        assertTrue(poster.timer > 0f)
        val mobile = world.fixturesIn(place).first { it.type == FixtureType.UP_MOBILE }
        sim.tap(place, mobile, 0f, -0.1f)
        run(sim, 1f)
        assertTrue(mobile.angle > 0f)
        val railing = fixture(world, HouseUpperIx.RAILING)
        sim.tap(place, railing, 0f, -0.1f)
        assertTrue(saw(UpperCodes.RAIL))
        val window = fixture(world, HouseUpperIx.STAR_WINDOW)
        sim.tap(place, window, 0f, -0.1f)
        assertEquals(1, window.mode)
    }

    @Test
    fun `somebody with nothing to do wanders over to the fun things by day`() {
        val world = world()
        world.night = false
        val sim = sim(world, 17)
        val kid = folk(world, 6.2f, "Leikar")
        var funSeat = false
        var t = 0f
        while (t < 240f && !funSeat) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
            val f = world.fixtures[kid.holder]
            if (kid.mode == Mode.SEATED && f != null && f.type in HouseUpperRules.INVITING) funSeat = true
        }
        assertTrue("the child found something to do", funSeat)
    }

    // ------------------------------------------------------------------ tasks, saves and the way through the house

    @Test
    fun `the floor has four tasks of its own`() {
        val own = TaskBook.ALL.filter { it.place == place }
        assertEquals(4, own.size)
        assertEquals(setOf("up_train", "up_blocks", "up_paint", "up_dress"), own.map { it.id }.toSet())
        assertTrue(own.all { it.fixture != null && it.fixture!!.name.startsWith("UP_") })
        assertTrue(own.all { it.need in 1..3 })
    }

    @Test
    fun `the state of the fixtures survives a save`() {
        val world = world()
        val sim = sim(world)
        fixture(world, HouseUpperIx.BLOCKS).let { sim.tap(place, it, 0f, -0.2f) }
        fixture(world, HouseUpperIx.EASEL).let { repeat(4) { _ -> sim.tap(place, it, 0f, -0.3f) } }
        fixture(world, HouseUpperIx.FORT).let { sim.tap(place, it, 0f, -0.1f) }
        fixture(world, HouseUpperIx.WARDROBE).let { w -> folk(world, w.x - 0.2f); sim.tap(place, w, 0f, -0.2f) }
        fixture(world, HouseUpperIx.NIGHT_LAMP).let { sim.tap(place, it, 0f, -0.1f) }
        fixture(world, HouseUpperIx.PUPPETS).let { sim.tap(place, it, 0f, -0.1f) }
        world.flags += "manor_key_upper"
        world.unlocked += "upper_bath"
        val saved = WorldStore.decode(org.json.JSONObject(WorldStore.encode(world, Settings()).toString()))
        val w2 = saved.world
        assertEquals(1, fixture(w2, HouseUpperIx.BLOCKS).mode)
        // `open` is not saved for furniture without a cupboard; the rules put the fallen tower right on the next step.
        sim(w2).step(place, 1f / 60f)
        assertTrue(fixture(w2, HouseUpperIx.BLOCKS).open)
        assertEquals(4, fixture(w2, HouseUpperIx.EASEL).count)
        assertTrue(fixture(w2, HouseUpperIx.FORT).open)
        assertTrue(fixture(w2, HouseUpperIx.WARDROBE).open)
        assertEquals(1, fixture(w2, HouseUpperIx.WARDROBE).count)
        assertTrue(fixture(w2, HouseUpperIx.NIGHT_LAMP).on)
        assertEquals(1, fixture(w2, HouseUpperIx.PUPPETS).mode)
        assertTrue("manor_key_upper" in w2.flags)
        assertTrue("upper_bath" in w2.unlocked)
        // The animals are still on the floor.
        assertEquals(world.bodiesIn(place).size, w2.bodiesIn(place).size)
    }

    @Test
    fun `the rides and passages of the floor still work`() {
        val world = world()
        val sim = sim(world)
        var landed: PlaceId? = null
        sim.listener = object : SimListener {
            override fun onPassage(passage: Passage, arrivalX: Float, riders: Int) {
                landed = passage.to
            }
        }
        val slide = fixture(world, HouseUpperIx.SLIDE)
        val kid = folk(world, slide.x)
        sim.tap(place, slide, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_GROUND, kid.place)
        assertEquals(PlaceId.MANOR_GROUND, landed)
        val back = folk(world, fixture(world, HouseUpperIx.BALCONY_SLIDE).x, "Balkong")
        sim.tap(place, fixture(world, HouseUpperIx.BALCONY_SLIDE), 0f, -0.1f)
        assertEquals(PlaceId.MANOR_GARDEN, back.place)
        val pole = fixture(world, HouseUpperIx.POLE)
        val fire = folk(world, pole.x, "Brann")
        sim.tap(place, pole, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_GROUND, fire.place)
        assertNull(world.bodies.values.firstOrNull { it is Person && it.name == "Nobody" })
    }

    @Test
    fun `the things of the floor are real things`() {
        for (type in listOf(ThingType.UP_BLOCK, ThingType.UP_SOCK, ThingType.UP_PAINTBRUSH, ThingType.UP_SLIPPER, ThingType.UP_PAPER_PLANE)) {
            assertTrue(type.w > 0f && type.h > 0f && type.variants >= 1)
        }
        assertEquals(Cat.HAT, ThingType.UP_SLIPPER.cat)
        assertTrue("a paper plane glides", ThingType.UP_PAPER_PLANE.lift < 0.5f)
        // They lie around the floor from the start.
        val world = world()
        for (type in listOf(ThingType.UP_BLOCK, ThingType.UP_SOCK, ThingType.UP_PAINTBRUSH, ThingType.UP_SLIPPER, ThingType.UP_PAPER_PLANE)) {
            assertTrue("$type on the floor", world.bodiesIn(place).any { it is Thing && it.type == type })
        }
    }
}
