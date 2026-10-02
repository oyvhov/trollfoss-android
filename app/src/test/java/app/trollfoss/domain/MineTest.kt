package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Mitt hus: building rules, presets, tearing down with a store, the second floor, saving, glimt and tasks. */
class MineTest {
    private val codes = ArrayList<Int>()

    private fun sim(world: World) = Sim(world, object : SimListener {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            if (fx == Fx.HOUSE) codes += HouseFx.code(param)
        }
    }, Random(7))

    private fun step(sim: Sim, place: PlaceId, seconds: Float) {
        var t = 0f
        while (t < seconds) {
            sim.step(place, 1f / 30f)
            t += 1f / 30f
        }
    }

    private fun started(world: World = WorldFactory.create(Random(1)), shape: Int = 0): Pair<World, Sim> {
        val sim = sim(world)
        assertTrue(sim.mine.layFoundation(shape))
        step(sim, PlaceId.MINE_YARD, 6f)
        return world to sim
    }

    private fun build(sim: Sim, slot: Int, kind: RoomKind, place: PlaceId = PlaceId.MINE_GROUND) {
        assertTrue("room $kind in $slot", sim.mine.buildRoom(place, slot, kind))
        step(sim, place, 5f)
    }

    private fun furniture(world: World, place: PlaceId, slot: Int) =
        world.fixturesIn(place).filter { it.x in Mine.slotRange(slot) && House.passageAt(it) == null }

    @Test
    fun `a new house is an empty plot and nothing can be built yet`() {
        val world = WorldFactory.create(Random(1))
        val h = world.mine
        assertFalse(h.started)
        for (slot in 0 until Mine.SLOTS) assertFalse(Mine.canBuild(h, PlaceId.MINE_GROUND, slot))
        assertFalse(sim(world).mine.buildRoom(PlaceId.MINE_GROUND, 1, RoomKind.LIVING))
        // The door of the yard is shut until the foundation is laid.
        assertFalse(House.usable(world, House.passages.first { it.id == "mine-yard-door" }))
        assertFalse(Mine.canBuildUpper(h))
    }

    @Test
    fun `laying the foundation picks the template, builds the hall and opens the door`() {
        val (world, sim) = started(shape = 2)
        val h = world.mine
        assertTrue(h.started)
        assertEquals(2, h.shape)
        // The template's own look.
        assertTrue(h.flag)
        assertEquals(3, h.roofColor)
        assertTrue(Mine.FLAG_STARTED in world.flags)
        assertTrue(House.usable(world, House.passages.first { it.id == "mine-yard-door" }))
        assertNull(h.job)
        // The hall is furnished in the ground floor and the first glimt is out.
        assertEquals(MineRooms.hall.size, furniture(world, PlaceId.MINE_GROUND, 0).size)
        assertTrue("mine_start" in world.unlocked)
        // The front doors show the chosen door.
        assertEquals(h.door + 1, world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MINE_GROUND, 0)).mode)
        assertTrue(MineEvent.FOUNDATION_DONE in codes)
    }

    @Test
    fun `every template has its own look`() {
        val looks = (0 until Mine.TEMPLATES).map { shape ->
            val h = MineHouse()
            Mine.applyTemplate(h, shape)
            listOf(h.wall, h.roof, h.roofColor, h.door, h.windows)
        }
        assertEquals(Mine.TEMPLATES, looks.toSet().size)
    }

    @Test
    fun `a room is built into a free slot with six to nine pieces of furniture`() {
        val (world, sim) = started()
        for (kind in RoomKind.entries) {
            val (w, s) = started()
            build(s, 1, kind)
            assertEquals(kind, w.mine.kind(PlaceId.MINE_GROUND, 1))
            val n = furniture(w, PlaceId.MINE_GROUND, 1).size
            assertTrue("$kind has $n pieces", n in 6..9)
            assertEquals(MineRooms.preset(kind).size, n)
        }
    }

    @Test
    fun `a room that is built ticks the job to its end and records the deed`() {
        val (world, sim) = started()
        world.taskSet += "mine_first_room"
        assertTrue(sim.mine.buildRoom(PlaceId.MINE_GROUND, 1, RoomKind.KITCHEN))
        // At first nothing stands yet: the walls come halfway through the job.
        assertNull(world.mine.kind(PlaceId.MINE_GROUND, 1))
        assertTrue(world.mine.job != null)
        step(sim, PlaceId.MINE_GROUND, 1f)
        assertNull(world.mine.kind(PlaceId.MINE_GROUND, 1))
        step(sim, PlaceId.MINE_GROUND, 4f)
        assertEquals(RoomKind.KITCHEN, world.mine.kind(PlaceId.MINE_GROUND, 1))
        assertNull(world.mine.job)
        assertEquals(1, world.taskProgress["mine_first_room"])
        assertTrue(MineEvent.ROOM_DONE in codes)
        // The things of the room lie on their furniture and remember it.
        val apple = world.bodiesIn(PlaceId.MINE_GROUND).filterIsInstance<Thing>().first { it.type == ThingType.APPLE }
        assertEquals(PlaceId.MINE_GROUND, apple.homePlace)
        assertTrue(apple.homeOwner >= 0)
    }

    @Test
    fun `finishing a job at once leaves the finished room`() {
        val (world, sim) = started()
        assertTrue(sim.mine.buildRoom(PlaceId.MINE_GROUND, 2, RoomKind.BATH))
        sim.mine.finishJob()
        assertEquals(RoomKind.BATH, world.mine.kind(PlaceId.MINE_GROUND, 2))
        assertEquals(MineRooms.preset(RoomKind.BATH).size, furniture(world, PlaceId.MINE_GROUND, 2).size)
        assertNull(world.mine.job)
        // Leaving while the party or a job runs completes everything.
        assertTrue(sim.mine.buildRoom(PlaceId.MINE_GROUND, 3, RoomKind.BEDROOM))
        sim.mine.leave()
        assertEquals(RoomKind.BEDROOM, world.mine.kind(PlaceId.MINE_GROUND, 3))
    }

    @Test
    fun `the hall and built slots cannot be built over, and only one job runs at a time`() {
        val (world, sim) = started()
        assertFalse(sim.mine.buildRoom(PlaceId.MINE_GROUND, 0, RoomKind.LIVING))
        assertTrue(sim.mine.buildRoom(PlaceId.MINE_GROUND, 1, RoomKind.LIVING))
        assertFalse("busy", sim.mine.buildRoom(PlaceId.MINE_GROUND, 2, RoomKind.KITCHEN))
        sim.mine.finishJob()
        assertFalse(sim.mine.buildRoom(PlaceId.MINE_GROUND, 1, RoomKind.KITCHEN))
        assertFalse(Mine.canBuild(world.mine, PlaceId.MINE_GROUND, 5))
        assertFalse(Mine.canBuild(world.mine, PlaceId.MINE_YARD, 1))
    }

    @Test
    fun `the second floor needs two rooms and then opens the stairs and five new slots`() {
        val (world, sim) = started()
        val stairs = House.passages.first { it.id == "mine-stairs-up" }
        assertFalse(House.usable(world, stairs))
        assertFalse(sim.mine.buildUpper())
        build(sim, 1, RoomKind.LIVING)
        assertFalse("one room is not enough", sim.mine.buildUpper())
        build(sim, 2, RoomKind.KITCHEN)
        assertTrue(Mine.canBuildUpper(world.mine))
        world.taskSet += "mine_floor"
        assertTrue(sim.mine.buildUpper())
        step(sim, PlaceId.MINE_YARD, 7f)
        assertTrue(world.mine.upperBuilt)
        assertTrue(House.usable(world, stairs))
        assertEquals(1, world.taskProgress["mine_floor"])
        assertTrue("mine_second_floor" in world.unlocked)
        assertFalse(Mine.canBuildUpper(world.mine))
        // Upstairs a room needs a built room underneath.
        assertTrue(Mine.canBuild(world.mine, PlaceId.MINE_UPPER, 1))
        assertTrue(Mine.canBuild(world.mine, PlaceId.MINE_UPPER, 2))
        assertFalse(Mine.canBuild(world.mine, PlaceId.MINE_UPPER, 3))
        build(sim, 1, RoomKind.KIDS, PlaceId.MINE_UPPER)
        assertEquals(RoomKind.KIDS, world.mine.kind(PlaceId.MINE_UPPER, 1))
        assertTrue(furniture(world, PlaceId.MINE_UPPER, 1).size in 6..9)
    }

    @Test
    fun `tearing a room down stores its furniture and nothing is lost`() {
        val (world, sim) = started()
        build(sim, 1, RoomKind.LIVING)
        val rug = furniture(world, PlaceId.MINE_GROUND, 1).size
        // The child has added a toy of their own, and a figure stands in the room.
        val extra = sim.designer.add(PlaceId.MINE_GROUND, FixtureType.BEANBAG, 2, 2.9f, 0.9f)
        assertNotNull(extra)
        val friend = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MINE_GROUND, 2.5f, 0.9f, "Venn")
        world.styles[Decor.key(PlaceId.MINE_GROUND, 1)] = RoomStyle(3, 2)
        assertTrue(Mine.canDemolish(world.mine, PlaceId.MINE_GROUND, 1))
        assertTrue(sim.mine.demolish(PlaceId.MINE_GROUND, 1))
        assertNull(world.mine.kind(PlaceId.MINE_GROUND, 1))
        assertEquals(0, furniture(world, PlaceId.MINE_GROUND, 1).size)
        assertEquals(rug + 1, world.storage.size)
        assertTrue(world.storage.any { it.type == FixtureType.MI_FIREPLACE })
        assertTrue(world.storage.any { it.type == FixtureType.BEANBAG })
        assertNull(world.styles[Decor.key(PlaceId.MINE_GROUND, 1)])
        assertTrue("the figure walked to the hall", friend.x < 2.0f)
        assertTrue(MineEvent.DEMOLISH in codes)
        // The slot can be built again.
        build(sim, 1, RoomKind.MUSIC)
        assertEquals(RoomKind.MUSIC, world.mine.kind(PlaceId.MINE_GROUND, 1))
    }

    @Test
    fun `the hall cannot be torn down, nor a room with a room on top`() {
        val (world, sim) = started()
        build(sim, 1, RoomKind.LIVING)
        build(sim, 2, RoomKind.KITCHEN)
        assertFalse(sim.mine.demolish(PlaceId.MINE_GROUND, 0))
        assertTrue(sim.mine.buildUpper())
        step(sim, PlaceId.MINE_YARD, 7f)
        build(sim, 1, RoomKind.BEDROOM, PlaceId.MINE_UPPER)
        assertFalse(Mine.canDemolish(world.mine, PlaceId.MINE_GROUND, 1))
        assertFalse(sim.mine.demolish(PlaceId.MINE_GROUND, 1))
        assertTrue(Mine.canDemolish(world.mine, PlaceId.MINE_UPPER, 1))
        assertTrue(sim.mine.demolish(PlaceId.MINE_UPPER, 1))
        assertTrue(sim.mine.demolish(PlaceId.MINE_GROUND, 1))
    }

    @Test
    fun `furniture stays inside the rooms that stand`() {
        val (world, sim) = started()
        build(sim, 1, RoomKind.LIVING)
        // Dropped in an empty slot, a chair ends up in the nearest room.
        val chair = sim.designer.add(PlaceId.MINE_GROUND, FixtureType.CHAIR, 0, 7.0f, 0.9f)!!
        assertTrue(chair.x < 4.0f)
        val moved = sim.clampFixture(PlaceId.MINE_GROUND, chair, 9.5f, 0.9f)
        assertTrue(moved[0] < 4.0f)
        // The doors and stairs of the hall cannot be moved or put away.
        val door = world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MINE_GROUND, 0))
        assertFalse(sim.movable(door))
        assertFalse(sim.designer.canStore(PlaceId.MINE_GROUND, door))
    }

    @Test
    fun `the look from outside changes, wraps its choices and counts as a task`() {
        val (world, sim) = started()
        world.taskSet += "mine_dress"
        val before = world.mine.hash()
        sim.mine.restyle(wall = 5, roof = 3, roofColor = 6, door = 4, windows = 2, chimney = false, flag = true)
        val h = world.mine
        assertEquals(5, h.wall)
        assertEquals(3, h.roof)
        assertEquals(6, h.roofColor)
        assertFalse(h.chimney)
        assertTrue(h.flag)
        assertNotEquals(before, h.hash())
        sim.mine.restyle(wall = 17)
        assertEquals(17 % Mine.WALL_COLORS, h.wall)
        assertEquals(2, world.taskProgress["mine_dress"])
        assertEquals(5, world.fixtures.getValue(WorldFactory.fixtureId(PlaceId.MINE_YARD, 0)).mode)
    }

    @Test
    fun `the hash of the house follows rooms and look`() {
        val (world, sim) = started()
        val a = world.mine.hash()
        build(sim, 1, RoomKind.LIVING)
        val b = world.mine.hash()
        build(sim, 2, RoomKind.KITCHEN)
        assertNotEquals(a, b)
        assertNotEquals(b, world.mine.hash())
        assertEquals(world.mine.hash(), world.mine.hash())
    }

    @Test
    fun `everything is saved and an old save without the house loads an empty plot`() {
        val (world, sim) = started(shape = 1)
        build(sim, 1, RoomKind.LIBRARY)
        build(sim, 3, RoomKind.GREENHOUSE)
        assertTrue(sim.mine.buildUpper())
        step(sim, PlaceId.MINE_YARD, 7f)
        build(sim, 3, RoomKind.KIDS, PlaceId.MINE_UPPER)
        sim.mine.restyle(wall = 8, roofColor = 2, flag = true)
        val json = WorldStore.encode(world, Settings())
        val back = WorldStore.decode(JSONObject(json.toString())).world
        val h = back.mine
        assertTrue(h.started)
        assertEquals(1, h.shape)
        assertEquals(8, h.wall)
        assertTrue(h.flag)
        assertTrue(h.upperBuilt)
        assertEquals(RoomKind.LIBRARY, h.kind(PlaceId.MINE_GROUND, 1))
        assertEquals(RoomKind.GREENHOUSE, h.kind(PlaceId.MINE_GROUND, 3))
        assertEquals(RoomKind.KIDS, h.kind(PlaceId.MINE_UPPER, 3))
        assertEquals(world.mine.hash(), h.hash())
        assertTrue(Mine.FLAG_UPPER in back.flags)
        assertEquals(furniture(world, PlaceId.MINE_GROUND, 1).size, furniture(back, PlaceId.MINE_GROUND, 1).size)
        assertEquals(furniture(world, PlaceId.MINE_UPPER, 3).size, furniture(back, PlaceId.MINE_UPPER, 3).size)
        // An old save has no «mine» at all.
        val old = JSONObject(json.toString()).apply { remove("mine"); remove("flags") }
        val fresh = WorldStore.decode(old).world
        assertFalse(fresh.mine.started)
        assertEquals(0, fresh.mine.roomCount())
        assertFalse(House.usable(fresh, House.passages.first { it.id == "mine-yard-door" }))
    }

    @Test
    fun `a housewarming calls figures from the village and sends them home again`() {
        val (world, sim) = started()
        build(sim, 1, RoomKind.LIVING)
        build(sim, 2, RoomKind.KITCHEN)
        assertFalse("three rooms are needed", sim.mine.housewarming(2f))
        build(sim, 3, RoomKind.DINING)
        world.place = PlaceId.MINE_GROUND
        assertTrue(sim.mine.housewarming(3f))
        val party = world.mine.party
        assertNotNull(party)
        val guests = world.mine.guests.toList()
        assertTrue(guests.size in 1..5)
        for (g in guests) assertEquals(PlaceId.MINE_GROUND, world.bodies[g.id]!!.place)
        assertTrue("mine_housewarming" in world.unlocked)
        assertTrue(Mine.FLAG_PARTY in world.flags)
        assertFalse("only one party at a time", sim.mine.housewarming(3f))
        // Saved in the middle of the party, the guests go home when the save is loaded.
        val mid = WorldStore.decode(JSONObject(WorldStore.encode(world, Settings()).toString())).world
        for (g in guests) assertEquals(g.place, mid.bodies[g.id]!!.place)
        assertTrue(mid.mine.guests.isEmpty())
        step(sim, PlaceId.MINE_GROUND, MineBuilder.PARTY_SECONDS + 1f)
        assertNull(world.mine.party)
        for (g in guests) assertEquals(g.place, world.bodies[g.id]!!.place)
        assertTrue(world.bodiesIn(PlaceId.MINE_GROUND).any { it is Thing && it.type == ThingType.GIFT })
    }

    @Test
    fun `figures who live in the house go in and out of the door by themselves`() {
        val (world, sim) = started()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.MINE_YARD, 3.4f, 0.9f, "Hedda")
        sim.settle(PlaceId.MINE_YARD)
        var seenInside = false
        var t = 0f
        while (t < 200f && !seenInside) {
            sim.step(PlaceId.MINE_YARD, 1f / 30f)
            t += 1f / 30f
            if (p.place == PlaceId.MINE_GROUND) seenInside = true
        }
        assertTrue("went in by the door", seenInside)
        var out = false
        while (t < 400f && !out) {
            sim.step(PlaceId.MINE_YARD, 1f / 30f)
            t += 1f / 30f
            if (p.place == PlaceId.MINE_YARD) out = true
        }
        assertTrue("came out again", out)
    }

    @Test
    fun `build mode picks slots from taps and asks before tearing down`() {
        val (world, sim) = started()
        build(sim, 1, RoomKind.LIVING)
        sim.mine.setBuildMode(true, PlaceId.MINE_GROUND)
        assertTrue(world.mine.buildMode)
        // Keep the finished room selected so the child can see and play in it.
        assertEquals(1, world.mine.selected)
        // A tap on the picture of slot 4 (the wall is shifted right by its depth) picks it.
        assertTrue(sim.mine.tapScene(PlaceId.MINE_GROUND, 8.6f, 0.4f))
        assertEquals(4, world.mine.selected)
        // The hall is not pickable; the yard is no place for rooms.
        assertFalse(sim.mine.tapScene(PlaceId.MINE_GROUND, 1.0f, 0.4f))
        // A long press on a built room asks first.
        assertTrue(sim.mine.longPress(PlaceId.MINE_GROUND, 2.9f + Mine.WALL_SHIFT, 0.4f))
        assertEquals(1, world.mine.askDemolish)
        assertTrue(world.fixturesIn(PlaceId.MINE_GROUND).any { it.x in Mine.slotRange(1) })
        sim.mine.cancelDemolish()
        assertEquals(-1, world.mine.askDemolish)
    }

    @Test
    fun `the shed on the empty plot opens the panel`() {
        val world = WorldFactory.create(Random(1))
        val sim = sim(world)
        assertTrue(sim.mine.tapScene(PlaceId.MINE_YARD, Mine.SHED_X, 0.7f))
        assertTrue(world.mine.wantPanel)
        assertFalse(sim.mine.tapScene(PlaceId.MINE_YARD, 7.5f, 0.7f))
    }

    @Test
    fun `tapping the furniture of the rooms answers with something funny`() {
        val (world, sim) = started()
        build(sim, 1, RoomKind.LIVING)
        build(sim, 2, RoomKind.KIDS)
        codes.clear()
        for (type in listOf(FixtureType.MI_FIREPLACE, FixtureType.MI_BLOCKS, FixtureType.MI_ROCKING_HORSE)) {
            val f = world.fixturesIn(PlaceId.MINE_GROUND).first { it.type == type }
            sim.tap(PlaceId.MINE_GROUND, f, 0f, -0.05f)
        }
        assertTrue(MineEvent.FLARE in codes)
        assertTrue(MineEvent.BLOCKS_FALL in codes)
        assertTrue(MineEvent.HORSE in codes)
        val blocks = world.fixturesIn(PlaceId.MINE_GROUND).first { it.type == FixtureType.MI_BLOCKS }
        assertEquals(1, blocks.mode)
        sim.tap(PlaceId.MINE_GROUND, blocks, 0f, -0.05f)
        assertEquals(0, blocks.mode)
    }

    @Test
    fun `yard furniture works and the apple tree drops an apple`() {
        val (world, sim) = started()
        val tree = world.fixturesIn(PlaceId.MINE_YARD).first { it.type == FixtureType.MI_APPLE_TREE }
        val before = world.bodiesIn(PlaceId.MINE_YARD).count { it is Thing && it.type == ThingType.APPLE }
        sim.tap(PlaceId.MINE_YARD, tree, 0f, -0.3f)
        assertEquals(before + 1, world.bodiesIn(PlaceId.MINE_YARD).count { it is Thing && it.type == ThingType.APPLE })
        val mailbox = world.fixturesIn(PlaceId.MINE_YARD).first { it.type == FixtureType.MI_MAILBOX }
        sim.tap(PlaceId.MINE_YARD, mailbox, 0f, -0.1f)
        assertTrue(mailbox.on)
        // The yard has its own pieces in the catalogue.
        assertTrue(Decor.catalogue(PlaceId.MINE_YARD).any { it.type == FixtureType.MI_FENCE })
        assertTrue(Decor.catalogue(PlaceId.MINE_GROUND).any { it.type == FixtureType.MI_FIREPLACE })
    }

    @Test
    fun `every piece of every preset has a spec and fits inside its slot`() {
        for (kind in RoomKind.entries) {
            for (piece in MineRooms.preset(kind) + MineRooms.hall + MineRooms.landing) {
                val spec = piece.type.spec
                assertTrue("${piece.type} in $kind has a size", spec.w > 0f && spec.h > 0f)
                assertTrue("${piece.type} in $kind sits inside the slot", piece.dx - spec.w / 2f >= 0f && piece.dx + spec.w / 2f <= Mine.SLOT_W)
            }
        }
        for (type in FixtureType.entries.filter { it.name.startsWith("MI_") }) assertNotNull("$type has a spec", MineRooms.specOf(type))
    }

    @Test
    fun `furnishing a room of the house counts for the task, and the presets do not`() {
        val (world, sim) = started()
        world.taskSet += "mine_furnish"
        build(sim, 1, RoomKind.LIVING)
        assertEquals("the preset is not the child's work", 0, world.taskProgress["mine_furnish"] ?: 0)
        sim.designer.add(PlaceId.MINE_GROUND, FixtureType.STOOL, 0, 2.5f, 0.9f)
        sim.designer.add(PlaceId.MINE_GROUND, FixtureType.STOOL, 0, 2.6f, 0.9f)
        assertEquals(2, world.taskProgress["mine_furnish"])
    }

    @Test
    fun `three glimt wait in the yard for the building`() {
        val ids = Secrets.inPlace(PlaceId.MINE_YARD).map { it.id }
        assertEquals(listOf("mine_start", "mine_second_floor", "mine_housewarming"), ids)
        assertTrue(Secrets.inPlace(PlaceId.MINE_YARD).all { it.event })
    }

    @Test
    fun `fixture ids of the house have room for the pieces of five rooms`() {
        for (place in listOf(PlaceId.MINE_GROUND, PlaceId.MINE_UPPER)) {
            assertTrue(place.addedMax - place.addedFrom + 1 >= 90)
            assertEquals(place, PlaceId.ofFixture(place.idBase + place.addedMax))
        }
    }

    @Test
    fun `a point of the picture belongs to the slot its wall or floor stands in`() {
        // The back wall is shifted right by its depth; the floor by its height.
        assertEquals(0, Mine.slotAtScene(1.5f, 0.4f))
        assertEquals(1, Mine.slotAtScene(2.5f + Mine.WALL_SHIFT, 0.4f))
        assertEquals(1, Mine.slotAtScene(2.1f, 0.97f))
        assertEquals(4, Mine.slotAtScene(9.9f, 0.9f))
        assertEquals(0, Mine.slotAtScene(-0.3f, 0.9f))
    }

    @Test
    fun `clamping keeps pieces inside standing rooms and leaves the yard alone`() {
        val h = MineHouse()
        h.started = true
        h.ground[2] = RoomKind.KITCHEN.ordinal + 1
        // The hall (slot 0) and slot 2 stand; slot 1 is air.
        assertEquals(1.0f, Mine.clampX(h, PlaceId.MINE_GROUND, 1.0f, 0.1f), 0.001f)
        val fromAir = Mine.clampX(h, PlaceId.MINE_GROUND, 3.0f, 0.1f)
        assertTrue("moved out of the air: $fromAir", fromAir < 2.0f || fromAir >= 4.0f)
        assertEquals(7.5f, Mine.clampX(h, PlaceId.MINE_YARD, 7.5f, 0.1f), 0.001f)
        // A wide piece is kept off the walls.
        val wide = Mine.clampX(h, PlaceId.MINE_GROUND, 4.05f, 0.25f)
        assertTrue(wide >= 4.0f + 0.25f)
    }

    @Test
    fun `a demolished room can be built again with other furniture and the old furniture is still in the store`() {
        val (world, sim) = started()
        build(sim, 2, RoomKind.WORKSHOP)
        assertTrue(sim.mine.demolish(PlaceId.MINE_GROUND, 2))
        val stored = world.storage.size
        build(sim, 2, RoomKind.GREENHOUSE)
        assertEquals(stored, world.storage.size)
        // Pieces can be brought back from the store into the new room.
        val back = sim.designer.unstore(PlaceId.MINE_GROUND, 0, 5.0f, 0.9f)
        assertNotNull(back)
        assertEquals(stored - 1, world.storage.size)
    }
}
