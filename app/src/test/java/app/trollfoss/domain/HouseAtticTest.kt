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

/** Storhuset's attic: the blueprint, Sture's hide-and-seek, the trunk, the records, the key and the secret room. */
class HouseAtticTest {
    private val attic = PlaceId.MANOR_ATTIC
    private val codes = ArrayList<Int>()
    private val fxs = ArrayList<Fx>()
    private val secrets = ArrayList<String>()
    private val spawned = ArrayList<Thing>()

    private fun sim(world: World, seed: Int = 3) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            fxs += fx
            if (fx == Fx.HOUSE) codes += HouseFx.code(param)
        }

        override fun onSecret(id: String) {
            secrets += id
        }

        override fun onSpawn(body: Body) {
            if (body is Thing) spawned += body
        }
    }, Random(seed))

    private fun step(sim: Sim, seconds: Float, place: PlaceId = attic) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 60f)
            t += 1f / 60f
        }
    }

    private fun fixture(world: World, index: Int) = world.fixtures.getValue(WorldFactory.fixtureId(attic, index))

    private fun sture(world: World) = world.people().first { it.name == "Sture" }

    private fun newWorld(seed: Int = 1) = WorldFactory.create(Random(seed))

    // ------------------------------------------------------------------ blueprint

    @Test
    fun `the attic is a dense floor of four rooms with real fixtures`() {
        val spec = Places.spec(attic)
        assertTrue("30 to 45 pieces of furniture, got ${spec.fixtures.size}", spec.fixtures.size in 30..45)
        assertEquals(4, AtticFloor.rooms.size)
        assertEquals(0.6f, AtticFloor.darkness, 0.001f)
        // The indices the rules and the glimt refer to point at the right furniture.
        assertEquals(FixtureType.STAIRCASE, spec.fixtures[AtticIds.STAIRS].type)
        assertEquals(FixtureType.SECRET_DOOR, spec.fixtures[AtticIds.SECRET_DOOR].type)
        assertEquals(FixtureType.AT_TRUNK, spec.fixtures[AtticIds.TRUNK].type)
        assertEquals(FixtureType.AT_ROCKING_HORSE, spec.fixtures[AtticIds.HORSE].type)
        assertEquals(FixtureType.AT_GRAMOPHONE, spec.fixtures[AtticIds.GRAMOPHONE].type)
        assertEquals(FixtureType.AT_WING_CHAIR, spec.fixtures[AtticIds.WING_CHAIR].type)
        assertEquals(FixtureType.TELESCOPE, spec.fixtures[AtticIds.TELESCOPE].type)
        assertEquals(FixtureType.AT_STAR_MAP, spec.fixtures[AtticIds.STAR_MAP].type)
        assertEquals(FixtureType.AT_TREASURE_CHEST, spec.fixtures[AtticIds.CHEST].type)
        assertEquals(FixtureType.AT_FAMILY_TREE, spec.fixtures[AtticIds.FAMILY_TREE].type)
        assertEquals(FixtureType.AT_MAP_TABLE, spec.fixtures[AtticIds.MAP_TABLE].type)
        // Every thing that lies on furniture lies on a real piece.
        for (t in spec.things) assertTrue(t.on < spec.fixtures.size)
        // Every one of the floor's own types has a spec of its own (not the plain box).
        for (type in FixtureType.entries.filter { it.name.startsWith("AT_") }) assertNotNull("$type has a spec", AtticFloor.specOf(type))
        // Furniture stands inside the attic.
        for (d in spec.fixtures) assertTrue("${d.type} at ${d.x}", d.x > 0.1f && d.x < attic.width - 0.05f)
    }

    @Test
    fun `the rooms and the passages keep the contract`() {
        assertEquals(listOf("attic-stairs-down", "attic-secret-down"), AtticFloor.passages.map { it.id })
        assertEquals(listOf("stairs", "secret-room"), AtticFloor.arrivals.map { it.name })
        assertEquals("library-secret", AtticFloor.passages[1].arrive)
        assertEquals("manor_bookshelf", AtticFloor.passages[1].locked)
        // The designer sees the four rooms.
        assertEquals(4, Decor.rooms(attic).size)
    }

    @Test
    fun `every effect code of the attic is its own and stays in the attic's block`() {
        val values = AtticCode::class.java.declaredFields
            .filter { java.lang.reflect.Modifier.isStatic(it.modifiers) && it.type == Int::class.javaPrimitiveType && it.name == it.name.uppercase() }
            .map { it.getInt(null) }
        assertTrue(values.size >= 40)
        assertEquals("no two codes are the same", values.size, values.toSet().size)
        for (v in values) assertTrue("code $v is in the attic's block", v in HouseFx.ATTIC until HouseFx.CELLAR)
    }

    @Test
    fun `sture lives in the attic with a cat`() {
        val world = newWorld()
        val s = sture(world)
        assertEquals(attic, s.place)
        assertEquals(Species.GHOST, s.species)
        assertTrue(world.people().any { it.place == attic && it.species == Species.CAT })
    }

    @Test
    fun `lamps start lit once, and the child can put them out for good`() {
        val world = newWorld()
        val sim = sim(world)
        step(sim, 0.1f)
        val lamps = world.fixturesIn(attic).filter { it.type in HouseAtticRules.LAMPS }
        assertTrue(lamps.size >= 6)
        assertTrue(lamps.all { it.on })
        val candles = fixture(world, AtticIds.CANDELABRA)
        sim.tap(attic, candles, 0f, -0.1f)
        assertFalse(candles.on)
        step(sim, 2f)
        assertFalse("it stays out", candles.on)
        assertTrue("attic_lit" in world.flags)
    }

    // ------------------------------------------------------------------ the costume trunk

    @Test
    fun `the trunk opens and tosses out a costume, set after set`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("attic_costume", "tidy", "photo")
        val trunk = fixture(world, AtticIds.TRUNK)
        sim.tap(attic, trunk, 0f, -0.1f)
        assertTrue(trunk.open)
        assertEquals(1, trunk.count)
        assertTrue(AtticCode.TRUNK in codes)
        val pieces = spawned.map { it.type }
        assertTrue(ThingType.AT_PIRATE_HAT in pieces && ThingType.AT_EYE_PATCH in pieces && ThingType.GARMENT in pieces)
        assertEquals("a pirate task is done by opening the trunk", 1, world.taskProgress["attic_costume"])
        // It falls shut by itself.
        step(sim, 5f)
        assertFalse(trunk.open)
        assertTrue(AtticCode.TRUNK_SHUT in codes)
        // The next time it is a knight.
        spawned.clear()
        sim.tap(attic, trunk, 0f, -0.1f)
        assertTrue(spawned.any { it.type == ThingType.AT_KNIGHT_HELMET })
    }

    @Test
    fun `costumes can be worn, and three trunk-fuls bring out a glimt`() {
        val world = newWorld()
        val sim = sim(world)
        val trunk = fixture(world, AtticIds.TRUNK)
        for (i in 0 until 3) {
            sim.tap(attic, trunk, 0f, -0.1f)
            step(sim, 5f)
        }
        assertTrue("attic_trunk" in world.unlocked)
        assertTrue("attic_trunk" in secrets)
        // A piece of costume goes on a figure like any hat.
        val hat = world.addThing(ThingType.AT_KNIGHT_HELMET, 0, attic, 2f, 0.9f)
        val folk = world.addPerson(Species.FOLK, Look(), 1f, attic, 2f, 0.9f, "Gjest")
        assertEquals(Give.WORE, sim.give(folk, hat, Part.HAT))
        val patch = world.addThing(ThingType.AT_EYE_PATCH, 0, attic, 2f, 0.9f)
        assertEquals(Give.WORE, sim.give(folk, patch, Part.GLASSES))
        // Garments from the trunk are real tops.
        val garments = AtticCostumes.sets.flatten().filter { it.type == ThingType.GARMENT }
        assertTrue(garments.isNotEmpty())
        for (g in garments) {
            assertTrue(Garment.style(g.variant) in 0 until Styles.TOPS)
            assertTrue(Garment.color(g.variant) in Palette.cloth.indices)
        }
    }

    @Test
    fun `the trunk never floods the attic with costumes`() {
        val world = newWorld()
        val sim = sim(world)
        val trunk = fixture(world, AtticIds.TRUNK)
        repeat(20) {
            sim.tap(attic, trunk, 0f, -0.1f)
            step(sim, 4f)
        }
        val loose = world.bodiesIn(attic).filterIsInstance<Thing>().count { it.type in AtticCostumes.types && it.homePlace == null }
        assertTrue("$loose costume pieces", loose <= HouseAtticRules.MAX_COSTUMES)
    }

    // ------------------------------------------------------------------ Sture

    /** Taps Sture the way the engine does, and lets a moment go by: he hides. */
    private fun makeHide(world: World, sim: Sim, tapAt: Float) {
        val s = sture(world)
        step(sim, 0.1f)
        s.anim.lastTap = tapAt
        step(sim, 2f)
    }

    private fun hidingPlace(world: World): Fixture? {
        val s = sture(world)
        return if (s.mode == Mode.SEATED) world.fixtures[s.holder]?.takeIf { it.type in HouseAtticRules.HIDERS } else null
    }

    @Test
    fun `a tap on sture makes him hide and the sheet where he is twitches`() {
        val world = newWorld()
        val sim = sim(world)
        makeHide(world, sim, 5f)
        val place = hidingPlace(world)
        assertNotNull("he is hiding", place)
        assertEquals("the place knows he is in it", 2, place!!.mode)
        assertTrue(AtticCode.HIDE in codes)
        // He is not on view while the sheet is down.
        assertFalse(place.open)
        step(sim, 12f)
        assertTrue("he giggles now and then", AtticCode.TELL in codes)
    }

    @Test
    fun `finding him three times earns a sticker and shakes the golden key out of his sheet`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("attic_catch", "tidy", "photo")
        for (round in 1..3) {
            makeHide(world, sim, round * 10f)
            val place = hidingPlace(world)
            assertNotNull("round $round: he hides", place)
            // A wrong guess first: another sheet, with nobody under it.
            val other = world.fixturesIn(attic).first { it.type == FixtureType.AT_SHEETED && it !== place }
            sim.tap(attic, other, 0f, -0.1f)
            assertTrue(other.open)
            assertEquals(Mode.SEATED, sture(world).mode)
            step(sim, 4f)
            // The right one.
            sim.tap(attic, place!!, 0f, -0.1f)
            assertEquals(Mode.FREE, sture(world).mode)
            assertEquals(0, place.mode)
            assertTrue("attic_catch_$round" in world.flags)
            assertEquals(round, codes.count { it == AtticCode.CAUGHT })
            step(sim, 5f)
        }
        assertTrue("attic_ghost" in secrets)
        assertEquals("three catches, one sticker (and the task's sticker)", 2, world.stickers.size)
        assertTrue("attic_sture" in world.eggs)
        assertTrue(AtticCode.STICKER in codes)
        assertEquals(1, world.taskProgress["attic_catch"])
        assertTrue("manor_key_attic" in world.flags)
        assertEquals(1, world.bodiesIn(attic).filterIsInstance<Thing>().count { it.type == ThingType.GOLDEN_KEY })
    }

    @Test
    fun `a tickle shakes the golden key out of sture once`() {
        val world = newWorld()
        val sim = sim(world)
        step(sim, 0.1f)
        val s = sture(world)
        assertFalse("manor_key_attic" in world.flags)
        s.anim.tickle = 2f
        step(sim, 0.1f)
        assertTrue("manor_key_attic" in world.flags)
        assertEquals(1, HouseKeys.found(world))
        assertTrue(codes.count { it == HouseFx.KEY_FOUND } == 1)
        val keys = world.bodiesIn(attic).filterIsInstance<Thing>().filter { it.type == ThingType.GOLDEN_KEY }
        assertEquals(1, keys.size)
        s.anim.tickle = 2f
        step(sim, 0.1f)
        assertEquals("only one key per floor", 1, world.bodiesIn(attic).filterIsInstance<Thing>().count { it.type == ThingType.GOLDEN_KEY })
    }

    @Test
    fun `sture hides, sneezes, swaps and blows out candles when left alone`() {
        val world = newWorld()
        val sim = sim(world, seed = 11)
        step(sim, 400f)
        val seen = codes.toSet()
        assertTrue("he got up to something: $seen", seen.any { it in setOf(AtticCode.HIDE, AtticCode.SNEEZE, AtticCode.SWAP, AtticCode.BLOW, AtticCode.SCARED) })
        assertTrue("and he dropped hints about the key", AtticCode.KEY_HINT in seen)
    }

    @Test
    fun `sture tickles or startles a visitor`() {
        val world = newWorld()
        val sim = sim(world, seed = 5)
        val guest = world.addPerson(Species.FOLK, Look(), 1f, attic, 4.3f, attic.floor, "Gjest")
        step(sim, 0.2f)
        step(sim, 900f)
        assertTrue("codes ${codes.toSet()} guest at ${guest.x},${guest.y} resting ${guest.resting} sture ${sture(world).x} ${sture(world).mode}", AtticCode.TICKLE in codes || AtticCode.BOO in codes)
        assertEquals(attic, guest.place)
    }

    @Test
    fun `a lamp lit next to sture gives him a start and he runs to hide`() {
        val world = newWorld()
        val sim = sim(world)
        step(sim, 0.1f)
        val s = sture(world)
        val candles = fixture(world, AtticIds.CANDELABRA)
        candles.on = false
        s.x = candles.x + 0.1f
        sim.tap(attic, candles, 0f, -0.1f)
        assertTrue(candles.on)
        assertTrue(AtticCode.SCARED in codes)
        step(sim, 3f)
        assertNotNull("he ran to hide", hidingPlace(world))
    }

    @Test
    fun `the grandfather clock makes him jump`() {
        val world = newWorld()
        val sim = sim(world)
        step(sim, 0.1f)
        val clock = fixture(world, AtticIds.CLOCK)
        sture(world).x = clock.x + 1.0f
        sim.tap(attic, clock, 0f, -0.2f)
        assertTrue(AtticCode.CLOCK in codes)
        assertTrue(AtticCode.SCARED in codes)
    }

    @Test
    fun `when sture has drifted to another floor he floats back up`() {
        val world = newWorld()
        val sim = sim(world)
        val s = sture(world)
        House.moveTo(world, s, PlaceId.MANOR_GROUND, 3f)
        step(sim, 12f)
        assertEquals(attic, s.place)
        assertTrue(AtticCode.RETURN in codes)
    }

    @Test
    fun `he stays hidden up here when the child leaves`() {
        val world = newWorld()
        val sim = sim(world, seed = 2)
        step(sim, 0.1f)
        var hid = 0
        for (i in 0 until 12) {
            val stairs = fixture(world, AtticIds.STAIRS)
            val s = sture(world)
            if (s.mode == Mode.SEATED) {
                s.mode = Mode.FREE
                s.holder = -1
            }
            sim.tap(attic, stairs, 0f, -0.1f)
            if (s.mode == Mode.SEATED) hid++
            s.place = attic
        }
        assertTrue("sometimes he hides as the child goes ($hid of 12)", hid in 2..12)
    }

    // ------------------------------------------------------------------ gramophone, horse, spiders, cartons

    @Test
    fun `the gramophone plays silly records and dancers join in`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("attic_record", "tidy", "photo")
        val g = fixture(world, AtticIds.GRAMOPHONE)
        val dancer = world.addPerson(Species.FOLK, Look(), 1f, attic, g.x + 0.4f, attic.floor, "Danser")
        sim.tap(attic, g, 0f, -0.1f)
        assertTrue(g.on)
        assertEquals(1, g.mode)
        assertTrue(AtticCode.RECORD in codes)
        assertEquals(1, world.taskProgress["attic_record"])
        step(sim, 1f)
        assertTrue("the dancer bounces to the music", dancer.anim.cheer > 0f)
        // Each tap puts on the next record; after four different ones a glimt floats out of the horn.
        for (i in 0 until 3) sim.tap(attic, g, 0f, -0.1f)
        assertEquals(4, g.mode)
        assertTrue("attic_music" in world.unlocked)
        // And it plays to the end and stops by itself.
        step(sim, 12f)
        assertFalse(g.on)
        assertTrue(AtticCode.RECORD_STOP in codes)
    }

    @Test
    fun `the stuck record skips until it is stopped`() {
        val world = newWorld()
        val sim = sim(world)
        val g = fixture(world, AtticIds.GRAMOPHONE)
        for (i in 0 until 5) sim.tap(attic, g, 0f, -0.1f)
        assertEquals(5, g.mode)
        step(sim, 6f)
        assertTrue(codes.count { it == AtticCode.SKIP } >= 2)
    }

    @Test
    fun `a record dropped on the gramophone starts that tune and gives the old one back`() {
        val world = newWorld()
        val sim = sim(world)
        val g = fixture(world, AtticIds.GRAMOPHONE)
        val a = world.addThing(ThingType.AT_RECORD, 2, attic, g.x, g.y - 0.2f)
        assertTrue(sim.dropInto(attic, g, a))
        assertEquals(3, g.mode)
        assertTrue(a.id !in world.bodies)
        val b = world.addThing(ThingType.AT_RECORD, 5, attic, g.x, g.y - 0.2f)
        assertTrue(sim.dropInto(attic, g, b))
        assertEquals(6, g.mode)
        assertTrue("the old record is tossed back", world.bodiesIn(attic).any { it is Thing && it.type == ThingType.AT_RECORD && it.variant == 2 })
        assertFalse(sim.dropInto(attic, g, world.addThing(ThingType.APPLE, 0, attic, g.x, g.y - 0.2f)))
    }

    @Test
    fun `the rocking horse rocks and carries a rider, and an apple makes it neigh`() {
        val world = newWorld()
        val sim = sim(world)
        val horse = fixture(world, AtticIds.HORSE)
        val rider = world.addPerson(Species.FOLK, Look(), 1f, attic, horse.x, attic.floor, "Rytter")
        assertTrue(sim.seat(rider, horse, 0))
        val x0 = rider.x
        sim.tap(attic, horse, 0f, -0.15f)
        assertTrue(horse.on)
        var moved = 0f
        for (i in 0 until 90) {
            sim.step(attic, 1f / 60f)
            moved = maxOf(moved, kotlin.math.abs(rider.x - x0))
        }
        assertTrue("the rider rocks with the horse ($moved)", moved > 0.01f)
        step(sim, 15f)
        assertFalse("it comes to rest", horse.on)
        assertEquals(0f, horse.angle, 0.0001f)
        assertEquals(x0, rider.x, 0.01f)
        val apple = world.addThing(ThingType.APPLE, 0, attic, horse.x + 0.1f, horse.y - 0.2f)
        assertTrue(sim.dropInto(attic, horse, apple))
        assertTrue(apple.id !in world.bodies)
        assertTrue(AtticCode.HORSE in codes)
    }

    @Test
    fun `the knitting spider makes a sweater every fourth tap`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("attic_knit", "tidy", "photo")
        val spider = fixture(world, AtticIds.SPIDER_LEFT)
        for (i in 0 until 3) {
            sim.tap(attic, spider, 0f, -0.2f)
            assertEquals(i + 1, spider.mode)
        }
        assertTrue(spawned.none { it.type == ThingType.GARMENT })
        sim.tap(attic, spider, 0f, -0.2f)
        assertEquals(0, spider.mode)
        val sweater = spawned.first { it.type == ThingType.GARMENT }
        assertEquals(5, Garment.style(sweater.variant))
        assertEquals(1, world.taskProgress["attic_knit"])
    }

    @Test
    fun `a carton springs an old toy out and closes again`() {
        val world = newWorld()
        val sim = sim(world)
        val carton = fixture(world, AtticIds.CARTON)
        sim.tap(attic, carton, 0f, -0.2f)
        assertTrue(carton.open)
        assertEquals(1, spawned.size)
        assertTrue(spawned[0].type in HouseAtticRules.TOY_TYPES)
        assertTrue(AtticCode.CARTON in codes)
        step(sim, 4f)
        assertFalse(carton.open)
    }

    @Test
    fun `lifting a sheet that hides nobody only makes dust, and every fourth has a hat`() {
        val world = newWorld()
        val sim = sim(world)
        val sheet = fixture(world, AtticIds.SHEET_B)
        var hats = 0
        for (i in 0 until 4) {
            sim.tap(attic, sheet, 0f, -0.2f)
            assertTrue(sheet.open)
            step(sim, 4f)
            assertFalse(sheet.open)
            if (i == 3) hats = spawned.count { it.type.cat == Cat.HAT }
        }
        assertEquals(1, hats)
        assertTrue(AtticCode.SHEET in codes)
    }

    // ------------------------------------------------------------------ tower and secret room

    @Test
    fun `the telescope opens the telescope screen`() {
        val world = newWorld()
        val sim = sim(world)
        world.taskSet.clear()
        world.taskSet += listOf("attic_stars", "tidy", "photo")
        val scope = fixture(world, AtticIds.TELESCOPE)
        sim.tap(attic, scope, 0f, -0.2f)
        assertTrue(Fx.LOOK in fxs)
        assertEquals(1, world.taskProgress["attic_stars"])
        assertFalse("a glimt only after looking twice or at night", "attic_tower" in world.unlocked)
        sim.tap(attic, scope, 0f, -0.2f)
        assertTrue("attic_tower" in world.unlocked)
        assertFalse("the lab's glimt is not stirred by the attic", "lab_telescope" in world.unlocked)
    }

    @Test
    fun `the star map lights five constellations and then a star appears`() {
        val world = newWorld()
        val sim = sim(world)
        val map = fixture(world, AtticIds.STAR_MAP)
        for (i in 1..4) {
            sim.tap(attic, map, 0f, -0.15f)
            assertEquals(i, map.mode)
        }
        assertFalse("attic_stars" in world.unlocked)
        sim.tap(attic, map, 0f, -0.15f)
        assertEquals(0, map.mode)
        assertTrue("attic_stars" in world.unlocked)
        assertTrue(AtticCode.STARS_DONE in codes)
    }

    @Test
    fun `the treasure map leads to a gem, and the chest showers coins`() {
        val world = newWorld()
        val sim = sim(world)
        val table = fixture(world, AtticIds.MAP_TABLE)
        for (i in 1..4) sim.tap(attic, table, 0f, -0.1f)
        assertEquals(4, table.mode)
        assertTrue(spawned.any { it.type == ThingType.GEM })
        sim.tap(attic, table, 0f, -0.1f)
        assertEquals(0, table.mode)
        val chest = fixture(world, AtticIds.CHEST)
        spawned.clear()
        sim.tap(attic, chest, 0f, -0.1f)
        assertTrue(chest.open)
        assertTrue(spawned.count { it.type == ThingType.COIN } >= 6)
        assertTrue("attic_secret" in world.unlocked)
        step(sim, 5f)
        assertFalse(chest.open)
    }

    @Test
    fun `the globe spins and slows down by itself`() {
        val world = newWorld()
        val sim = sim(world)
        val globe = fixture(world, AtticIds.GLOBE)
        sim.tap(attic, globe, 0f, -0.1f)
        assertTrue(globe.angleV > 1f)
        step(sim, 1f)
        assertTrue(globe.angle > 0.5f)
        step(sim, 12f)
        assertEquals(0f, globe.angleV, 0.001f)
        assertTrue(codes.count { it == AtticCode.GLOBE } >= 2)
    }

    @Test
    fun `all six portraits on the family tree wink and a glimt appears`() {
        val world = newWorld()
        val sim = sim(world)
        val tree = fixture(world, AtticIds.FAMILY_TREE)
        val h = tree.spec.h
        for (row in 0..1) for (col in 0..2) {
            sim.tap(attic, tree, (col - 1) * 0.18f, if (row == 0) -h * 0.75f else -h * 0.25f)
        }
        assertTrue("attic_tree" in world.unlocked)
        assertTrue(AtticCode.TREE_DONE in codes)
        assertEquals(6, codes.count { it == AtticCode.TREE })
    }

    @Test
    fun `the secret door only works once the bookshelf lever is pulled`() {
        val world = newWorld()
        val sim = sim(world)
        val door = fixture(world, AtticIds.SECRET_DOOR)
        val rider = world.addPerson(Species.FOLK, Look(), 1f, attic, door.x, attic.floor, "Rider")
        step(sim, 0.1f)
        assertEquals("locked", 0, door.mode)
        sim.tap(attic, door, 0f, -0.2f)
        assertEquals(attic, rider.place)
        world.flags += "manor_bookshelf"
        step(sim, 0.1f)
        assertEquals("unlocked", 1, door.mode)
        sim.tap(attic, door, 0f, -0.2f)
        assertEquals(PlaceId.MANOR_GROUND, rider.place)
    }

    @Test
    fun `the stairs take whoever stands at them down to the first floor`() {
        val world = newWorld()
        val sim = sim(world)
        val stairs = fixture(world, AtticIds.STAIRS)
        val rider = world.addPerson(Species.FOLK, Look(), 1f, attic, stairs.x + 0.1f, attic.floor, "Rider")
        sim.tap(attic, stairs, 0f, -0.1f)
        assertEquals(PlaceId.MANOR_UPPER, rider.place)
    }

    // ------------------------------------------------------------------ glimt, tasks and saving

    @Test
    fun `the attic has plenty of glimt and five tasks of its own`() {
        val glimt = Secrets.inPlace(attic)
        assertTrue(glimt.size >= 3)
        assertTrue(glimt.all { it.id.startsWith("attic_") && it.event })
        for (g in glimt) {
            val y = g.y
            assertTrue("${g.id} sits on screen", y in 0f..1f)
            assertTrue(g.on < Places.spec(attic).fixtures.size)
        }
        val tasks = TaskBook.ALL.filter { it.place == attic }
        assertTrue(tasks.size >= 4)
        assertEquals(tasks.size, tasks.map { it.id }.toSet().size)
        for (t in tasks) assertTrue("${t.id} has a picture", t.fixture != null || t.thing != null)
    }

    @Test
    fun `progress in the attic survives saving and loading`() {
        val world = newWorld()
        val sim = sim(world)
        step(sim, 0.1f)
        sim.tap(attic, fixture(world, AtticIds.TRUNK), 0f, -0.1f)
        step(sim, 5f)
        sim.tap(attic, fixture(world, AtticIds.GRAMOPHONE), 0f, -0.1f)
        sim.tap(attic, fixture(world, AtticIds.CANDELABRA), 0f, -0.1f)
        makeHide(world, sim, 3f)
        val place = hidingPlace(world)
        assertNotNull(place)
        val placeIndex = attic.indexOf(place!!.id)
        world.flags += "attic_catch_1"
        val json = WorldStore.encode(world, Settings()).toString()
        val w2 = WorldStore.decode(org.json.JSONObject(json)).world
        assertEquals(1, w2.fixtures.getValue(WorldFactory.fixtureId(attic, AtticIds.TRUNK)).count)
        assertEquals(1, w2.fixtures.getValue(WorldFactory.fixtureId(attic, AtticIds.GRAMOPHONE)).mode)
        assertFalse(w2.fixtures.getValue(WorldFactory.fixtureId(attic, AtticIds.CANDELABRA)).on)
        assertTrue("attic_catch_1" in w2.flags && "attic_lit" in w2.flags)
        // Sture is still hiding where he was, and the sheet still knows it.
        val s2 = w2.people().first { it.name == "Sture" }
        assertEquals(Mode.SEATED, s2.mode)
        assertEquals(WorldFactory.fixtureId(attic, placeIndex), s2.holder)
        assertEquals(2, w2.fixtures.getValue(s2.holder).mode)
        // And he is found in the loaded world just the same.
        val sim2 = sim(w2)
        step(sim2, 0.1f)
        sim2.tap(attic, w2.fixtures.getValue(s2.holder), 0f, -0.1f)
        assertEquals(Mode.FREE, s2.mode)
        assertTrue("attic_catch_2" in w2.flags)
        assertNull(w2.people().firstOrNull { it.name == "Sture" && it.mode == Mode.SEATED })
    }
}
