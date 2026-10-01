package app.trollfoss.domain

import app.trollfoss.audio.Sfx
import app.trollfoss.audio.Synth
import app.trollfoss.ui.play.FigurarFx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Rolf the robot butler and Sture the ghost: where things are on them, what they do and what they say. */
class FigurarTest {
    private val events = ArrayList<Pair<Fx, Int>>()

    private fun sim(world: World, seed: Int = 7) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            events += fx to param
        }
    }, Random(seed))

    private fun step(sim: Sim, place: PlaceId, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun robot(world: World, x: Float = 2.2f): Person =
        world.addPerson(Species.ROBOT, Look(skin = 10), 1f, PlaceId.HOME, x, 0.9f, "Test-Rolf").also { it.ground = 0.9f }

    private fun ghost(world: World, x: Float = 2.2f): Person =
        world.addPerson(Species.GHOST, Look(skin = 9), 1f, PlaceId.HOME, x, 0.9f, "Test-Sture").also { it.ground = 0.9f }

    private fun figurarEvents(code: Int) = events.filter { it.first == Fx.FIGURAR && FigurarEvent.code(it.second) == code }

    // ------------------------------------------------------------------ looks and anatomy

    @Test
    fun `the robot and the ghost have colours for every look, and a first one that is the friendly default`() {
        assertEquals(Palette.robots[0], Palette.furFor(Species.ROBOT, 10))
        assertEquals(Palette.ghosts[0], Palette.furFor(Species.GHOST, 9))
        for (i in -20..60) {
            assertTrue(Palette.furFor(Species.ROBOT, i) in Palette.robots)
            assertTrue(Palette.furFor(Species.GHOST, i) in Palette.ghosts)
        }
    }

    @Test
    fun `every part has a place on both figures in every pose, with the hat on top`() {
        for (species in listOf(Species.ROBOT, Species.GHOST)) {
            for (pose in Pose.entries) {
                for (part in Part.entries) {
                    val f = Anatomy.fraction(species, pose, part)
                    assertEquals(2, f.size)
                    assertTrue("$species $pose $part is finite", f[0].isFinite() && f[1].isFinite())
                }
            }
            val hat = Anatomy.fraction(species, Pose.STAND, Part.HAT)[1]
            for (part in listOf(Part.HEAD, Part.GLASSES, Part.MOUTH, Part.HAND, Part.BODY)) {
                assertTrue("$species: the hat is above the $part", hat < Anatomy.fraction(species, Pose.STAND, part)[1])
            }
            assertTrue(Anatomy.headRadius(species) in 0.2f..0.4f)
        }
    }

    @Test
    fun `sitting and lying work for them like for folk`() {
        for (species in listOf(Species.ROBOT, Species.GHOST)) {
            val stand = Anatomy.fraction(species, Pose.STAND, Part.HAT)
            val sit = Anatomy.fraction(species, Pose.SIT, Part.HAT)
            assertEquals(stand[1] + Anatomy.HIPS, sit[1], 1e-6f)
            assertEquals(stand[0], sit[0], 1e-6f)
            val folkLie = Anatomy.fraction(Species.FOLK, Pose.LIE, Part.HAT)
            val lie = Anatomy.fraction(species, Pose.LIE, Part.HAT)
            // Lying: the head is to the left of the middle of the back, as for people.
            assertTrue(lie[0] > 0f == folkLie[0] > 0f)
        }
        // Animals keep their own way of sitting.
        assertEquals(Anatomy.fraction(Species.CAT, Pose.STAND, Part.HAT)[1], Anatomy.fraction(Species.CAT, Pose.SIT, Part.HAT)[1], 1e-6f)
    }

    @Test
    fun `Rolf's bow dips his head and his hat with it, and nothing else`() {
        val world = WorldFactory.create(Random(3))
        val rolf = robot(world, 3.4f)
        val hatStand = Anatomy.at(rolf, Part.HAT)[1]
        val handStand = Anatomy.at(rolf, Part.HAND)[1]
        rolf.anim.wave = 0.6f
        assertTrue(Anatomy.at(rolf, Part.HAT)[1] > hatStand)
        assertTrue(Anatomy.at(rolf, Part.GLASSES)[1] > Anatomy.fraction(Species.ROBOT, Pose.STAND, Part.GLASSES)[1] * rolf.h + rolf.y)
        assertEquals(handStand, Anatomy.at(rolf, Part.HAND)[1], 1e-6f)
        // Sture does not bow, and a figure that is held does not either.
        val sture = ghost(world, 3.9f)
        val before = Anatomy.at(sture, Part.HAT)[1]
        sture.anim.wave = 0.6f
        assertEquals(before, Anatomy.at(sture, Part.HAT)[1], 1e-6f)
        rolf.anim.pose = Pose.HELD
        assertEquals(hatStand, Anatomy.at(rolf, Part.HAT)[1], 1e-6f)
    }

    @Test
    fun `a bow goes down quickly, holds and comes up`() {
        assertEquals(0f, FigurarPose.bow(0f), 0f)
        assertEquals(0f, FigurarPose.bow(FigurarPose.BOW_SECONDS), 1e-6f)
        assertEquals(1f, FigurarPose.bow(0.6f), 1e-6f)
        assertTrue(FigurarPose.bow(1.1f) < FigurarPose.bow(1.0f))
        assertTrue(FigurarPose.bow(0.2f) < FigurarPose.bow(0.4f))
        for (i in 0..120) assertTrue(FigurarPose.bow(i / 100f) in 0f..1f)
    }

    // ------------------------------------------------------------------ hats, glasses and held things

    @Test
    fun `hats, glasses and things in the hand all work on both`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        for (p in listOf(robot(world), ghost(world))) {
            val cap = world.addThing(ThingType.CAP, 0, PlaceId.HOME, p.x, 0.5f)
            assertEquals(Give.WORE, sim.give(p, cap, Part.HAT))
            val shades = world.addThing(ThingType.SUNGLASSES, 0, PlaceId.HOME, p.x, 0.5f)
            assertEquals(Give.WORE, sim.give(p, shades, Part.GLASSES))
            val ball = world.addThing(ThingType.BALL, 0, PlaceId.HOME, p.x, 0.5f)
            assertEquals(Give.HELD, sim.give(p, ball, Part.HAND))
            assertEquals(3, world.carried(p).size)
        }
    }

    // ------------------------------------------------------------------ food

    @Test
    fun `food is fuel for Rolf, who swallows it whole and is very polite`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val rolf = robot(world, 3.4f)
        val apple = world.addThing(ThingType.APPLE, 0, PlaceId.HOME, rolf.x, 0.6f)
        assertEquals(Give.FINISHED, sim.give(rolf, apple, Part.MOUTH))
        assertFalse(world.bodies.containsKey(apple.id))
        assertEquals(0f, rolf.anim.chew, 0f)
        // Every kind of food and drink is just as welcome, and nothing crashes.
        for (type in ThingType.entries.filter { it.edible && !it.potion }) {
            val t = world.addThing(type, 0, PlaceId.HOME, rolf.x, 0.6f)
            assertEquals(Give.FINISHED, sim.give(rolf, t, Part.MOUTH))
        }
    }

    @Test
    fun `Sture sniffs food, is puzzled and hands it back with a little toss`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val sture = ghost(world, 3.9f)
        val apple = world.addThing(ThingType.APPLE, 0, PlaceId.HOME, sture.x + 0.1f, 0.6f)
        apple.held = true
        assertEquals(Give.SNIFF, sim.give(sture, apple, Part.MOUTH))
        assertTrue(world.bodies.containsKey(apple.id))
        assertEquals(0, apple.used)
        assertEquals(Mode.FREE, apple.mode)
        assertFalse(apple.held)
        assertTrue(apple.vy < 0f)
        assertTrue(apple.vx > 0f)
        assertTrue(world.carried(sture).isEmpty())
        for (type in ThingType.entries.filter { it.edible && !it.potion }) {
            val t = world.addThing(type, 0, PlaceId.HOME, sture.x, 0.6f)
            assertEquals(Give.SNIFF, sim.give(sture, t, Part.MOUTH))
            assertEquals(0, t.used)
        }
    }

    @Test
    fun `potions and pepper still work on them`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val sture = ghost(world, 3.9f)
        val grow = world.addThing(ThingType.POTION_GROW, 0, PlaceId.HOME, sture.x, 0.6f)
        assertEquals(Give.POTION, sim.give(sture, grow, Part.MOUTH))
        assertTrue(sture.scale > 1f)
        val shrink = world.addThing(ThingType.POTION_SHRINK, 0, PlaceId.HOME, sture.x, 0.6f)
        sim.give(sture, shrink, Part.MOUTH)
        val rolf = robot(world, 3.4f)
        val rainbow = world.addThing(ThingType.POTION_RAINBOW, 0, PlaceId.HOME, rolf.x, 0.6f)
        val before = rolf.look.skin
        assertEquals(Give.POTION, sim.give(rolf, rainbow, Part.MOUTH))
        assertEquals(before + 1, rolf.look.skin)
        val pepper = world.addThing(ThingType.PEPPER, 0, PlaceId.HOME, rolf.x, 0.6f)
        assertEquals(Give.SNEEZE, sim.give(rolf, pepper, Part.MOUTH))
    }

    @Test
    fun `Sture wishes for hats, glasses and toys, never for food, and Rolf for tools`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val sture = ghost(world, 3.9f)
        val rolf = robot(world, 3.4f)
        for (type in ThingType.entries) {
            if (type.edible || type.potion) {
                assertFalse("$type is not for a ghost", sim.life.wants(sture, type))
                assertFalse("$type is not for a robot", sim.life.wants(rolf, type))
            }
        }
        assertTrue(sim.life.wants(sture, ThingType.CAP))
        assertTrue(sim.life.wants(sture, ThingType.SUNGLASSES))
        assertTrue(sim.life.wants(sture, ThingType.BALL))
        assertTrue(sim.life.wants(rolf, ThingType.SCISSORS))
        assertTrue(sim.life.wants(rolf, ThingType.CROWN))
    }

    // ------------------------------------------------------------------ behaviour

    @Test
    fun `Rolf bows to a guest who comes up to him, the guest waves back, and then he leaves them in peace`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val rolf = robot(world, 3.4f)
        val guest = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 3.55f, 0.9f, "Gjest")
        guest.ground = 0.9f
        for (p in world.people()) p.anim.nextWish = 1e6f
        step(sim, PlaceId.HOME, 4f)
        assertTrue("a bow", figurarEvents(FigurarEvent.BOW).isNotEmpty())
        assertEquals(rolf.id, FigurarEvent.id(figurarEvents(FigurarEvent.BOW).first().second))
        val first = figurarEvents(FigurarEvent.BOW).size
        step(sim, PlaceId.HOME, 8f)
        assertEquals("no second bow so soon", first, figurarEvents(FigurarEvent.BOW).size)
    }

    @Test
    fun `Rolf does not bow to nobody, nor while he is held or asleep`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val rolf = robot(world, 3.4f)
        val guest = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 3.5f, 0.9f, "Gjest")
        guest.ground = 0.9f
        rolf.held = true
        step(sim, PlaceId.HOME, 3f)
        assertTrue(figurarEvents(FigurarEvent.BOW).isEmpty())
        rolf.held = false
        rolf.anim.face = Face.SLEEP
        rolf.anim.faceTime = 100f
        step(sim, PlaceId.HOME, 3f)
        assertTrue(figurarEvents(FigurarEvent.BOW).isEmpty())
    }

    @Test
    fun `Sture sneezes dust now and then, but only dust`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        val sture = ghost(world, 3.9f)
        val hat = world.addThing(ThingType.PARTY_HAT, 0, PlaceId.HOME, sture.x, 0.5f)
        sim.give(sture, hat, Part.HAT)
        // «Ah … ah …», and the dust flies when it is over.
        sim.figurar.sneeze(sture)
        assertTrue(sture.anim.achoo > 0f)
        assertEquals(1, figurarEvents(FigurarEvent.ACHOO).size)
        step(sim, PlaceId.HOME, 1f)
        assertEquals(0f, sture.anim.achoo, 0f)
        assertEquals(1, figurarEvents(FigurarEvent.DUST).size)
        // The hat stays on and nobody counts it as a real «ATSJO».
        assertEquals(Mode.WORN, hat.mode)
        assertTrue(events.none { it.first == Fx.ATSJO })
    }

    @Test
    fun `Sture sneezes by himself every so often`() {
        val world = WorldFactory.create(Random(5))
        val sim = sim(world)
        ghost(world)
        step(sim, PlaceId.HOME, 1200f)
        assertTrue(figurarEvents(FigurarEvent.ACHOO).isNotEmpty())
        assertTrue(figurarEvents(FigurarEvent.DUST).isNotEmpty())
        assertTrue("not every few seconds", figurarEvents(FigurarEvent.ACHOO).size < 60)
    }

    @Test
    fun `both stroll about on their own like the other animals do, never off the floor`() {
        val world = WorldFactory.create(Random(8))
        val sim = sim(world)
        val rolf = robot(world, 3.4f)
        val sture = ghost(world, 3.9f)
        for (p in world.people()) p.anim.nextWish = 1e6f
        val start = rolf.x to sture.x
        var moved = false
        repeat(40) {
            step(sim, PlaceId.HOME, 5f)
            moved = moved || rolf.x != start.first || sture.x != start.second
            for (p in listOf(rolf, sture)) {
                assertTrue(p.x in 0f..PlaceId.HOME.width)
                assertTrue(p.ground in PlaceId.HOME.back..PlaceId.FRONT)
            }
        }
        assertTrue(moved)
        assertTrue(rolf.x != start.first)
        assertTrue(sture.x != start.second)
    }

    /** A free spot of [pose] in the home: everybody is helped out of their seats first. */
    private fun freeSpot(world: World, pose: Pose): Pair<Fixture, Int> {
        for (p in world.people()) if (p.mode == Mode.SEATED) {
            p.mode = Mode.FREE
            p.holder = -1
        }
        for (f in world.fixturesIn(PlaceId.HOME)) for (i in f.spec.spots.indices) {
            val s = f.spec.spots[i]
            if (s.pose == pose && !s.hidden) return f to i
        }
        error("no $pose spot at home")
    }

    @Test
    fun `they can be seated and put to bed, and made small, big and floaty without a hitch`() {
        val world = WorldFactory.create(Random(3))
        val sim = sim(world)
        val rolf = robot(world, 3.4f)
        val sture = ghost(world, 3.9f)
        val (seat, sitSpot) = freeSpot(world, Pose.SIT)
        assertTrue(sim.seat(rolf, seat, sitSpot))
        assertEquals(Pose.SIT, rolf.anim.pose)
        val (bed, lieSpot) = freeSpot(world, Pose.LIE)
        assertTrue(sim.seat(sture, bed, lieSpot))
        assertEquals(Pose.LIE, sture.anim.pose)
        step(sim, PlaceId.HOME, 3f)
        for (p in listOf(rolf, sture)) {
            p.scale = 0.5f
            step(sim, PlaceId.HOME, 0.5f)
            p.scale = 1.7f
            step(sim, PlaceId.HOME, 0.5f)
            p.floatTime = 3f
            step(sim, PlaceId.HOME, 1f)
            p.scale = 1f
        }
        assertNotNull(world.bodies[rolf.id])
    }

    // ------------------------------------------------------------------ voices

    @Test
    fun `every sound they make is their own and renders cleanly`() {
        val own = Sfx.entries.filter { it.name.startsWith("FG_") }
        assertEquals(12, own.size)
        for (sfx in own) {
            val samples = Synth.render(sfx)
            assertTrue("$sfx has sound", samples.size > 1000)
            assertTrue("$sfx is a short sound", samples.size < Synth.SAMPLE_RATE * 3)
            assertTrue("$sfx has clean samples", samples.all { it.isFinite() })
        }
        for (asked in listOf(Sfx.GIGGLE, Sfx.TICKLE, Sfx.BABBLE, Sfx.HMM, Sfx.OOH, Sfx.OOF, Sfx.YUM, Sfx.CHOMP, Sfx.GULP)) {
            assertTrue(FigurarFx.robotVoice(asked) in own || FigurarFx.robotVoice(asked) == Sfx.BEEP)
            assertTrue(FigurarFx.ghostVoice(asked) in own)
        }
        assertEquals(Sfx.FG_ROLF_LAUGH, FigurarFx.robotVoice(Sfx.GIGGLE))
        assertEquals(Sfx.FG_STURE_GIGGLE, FigurarFx.ghostVoice(Sfx.GIGGLE))
    }
}
