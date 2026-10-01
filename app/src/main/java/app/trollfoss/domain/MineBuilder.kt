package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * The rules of Mitt hus: laying the foundation, building and tearing down rooms, the second floor, the look from
 * outside, taps in build mode, the housewarming, and what the furniture does when it is tapped. Every effect goes out
 * as a [Fx.HOUSE] event in the [HouseFx.MINE] block (see [MineEvent]); `ui/play/MineFx.kt` plays them.
 *
 * Building takes a few seconds of work that the art shows (scaffolding, hammering, a crane): the house changes when
 * the job reaches its commit time, and [finishJob] completes a job at once (when the child leaves, or the app closes),
 * so nothing is ever lost. Furniture that is torn down goes to the home designer's store.
 */
class MineBuilder(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    val house: MineHouse get() = world.mine

    val rules: FloorRules = MineRules(this)

    private fun fx(code: Int, arg: Int = 0, x: Float = 0f, y: Float = 0.5f, f: Fixture? = null) {
        sim.listener.onFx(Fx.HOUSE, x, y, f, null, HouseFx.pack(code, arg))
    }

    private fun touch() {
        house.version++
        fx(MineEvent.CHANGED)
    }

    private fun yardDoorX(): Float = Mine.FACADE_X0 + Mine.FACADE_MW / 2f

    // ------------------------------------------------------------------ build mode and picking slots

    fun setBuildMode(on: Boolean, place: PlaceId = world.place) {
        val h = house
        h.buildMode = on
        if (on && place.mine && place != PlaceId.MINE_YARD && (h.selectedPlace != place || h.selected !in 1 until Mine.SLOTS || !pickable(place, h.selected))) {
            selectNext(place, 1f)
        }
        touch()
    }

    private fun pickable(place: PlaceId, slot: Int): Boolean = Mine.canBuild(house, place, slot) || (slot != 0 && house.kind(place, slot) != null)

    /** Picks the free slot of [place] nearest to scene x [near], or nothing. */
    fun selectNext(place: PlaceId, near: Float) {
        val free = Mine.buildable(house, place)
        val best = free.minByOrNull { abs(near - (it * Mine.SLOT_W + Mine.SLOT_W / 2f)) }
        house.selected = best ?: -1
        house.selectedPlace = place
    }

    fun select(place: PlaceId, slot: Int) {
        if (!pickable(place, slot)) return
        house.selected = slot
        house.selectedPlace = place
        fx(MineEvent.SELECT, slot, slot * Mine.SLOT_W + 1f, 0.6f)
        touch()
    }

    /** A tap on the picture of the place when nothing else was hit. True when the builder used it. */
    fun tapScene(place: PlaceId, x: Float, y: Float): Boolean {
        if (!place.mine) return false
        val h = house
        if (place == PlaceId.MINE_YARD) {
            if (!h.started && abs(x - Mine.SHED_X) < 0.6f && y in 0.3f..0.95f) {
                h.wantPanel = true
                fx(MineEvent.STARTED_TAP, 0, Mine.SHED_X, 0.7f)
                touch()
                return true
            }
            return false
        }
        if (!h.buildMode) return false
        val slot = Mine.slotAtScene(x, y)
        if (slot == 0) return false
        if (pickable(place, slot)) {
            select(place, slot)
        } else {
            fx(MineEvent.DENIED, slot, slot * Mine.SLOT_W + 1f, 0.6f)
        }
        return true
    }

    /** A long press on the picture: on a built room, the app asks whether to tear it down. */
    fun longPress(place: PlaceId, x: Float, y: Float): Boolean {
        if (place != PlaceId.MINE_GROUND && place != PlaceId.MINE_UPPER) return false
        val slot = Mine.slotAtScene(x, y)
        if (slot == 0 || house.kind(place, slot) == null) return false
        if (!Mine.canDemolish(house, place, slot)) {
            fx(MineEvent.DENIED, slot, slot * Mine.SLOT_W + 1f, 0.6f)
            return true
        }
        house.askDemolish = slot
        house.askPlace = place
        fx(MineEvent.SELECT, slot, slot * Mine.SLOT_W + 1f, 0.6f)
        touch()
        return true
    }

    fun cancelDemolish() {
        house.askDemolish = -1
        touch()
    }

    // ------------------------------------------------------------------ jobs

    val busy: Boolean get() = house.job != null

    /** The foundation goes down and the template's house rises. */
    fun layFoundation(shape: Int): Boolean {
        val h = house
        if (h.started || h.job != null) return false
        h.job = MineJob(JobKind.FOUNDATION, PlaceId.MINE_YARD, 0, null, shape.coerceIn(0, Mine.TEMPLATES - 1)).also { it.pieces = emptyList() }
        fx(MineEvent.FOUNDATION_START, shape, yardDoorX(), 0.7f)
        touch()
        return true
    }

    /** A room of [kind] is built in [slot] of [place]. */
    fun buildRoom(place: PlaceId, slot: Int, kind: RoomKind): Boolean {
        val h = house
        if (h.job != null || !Mine.canBuild(h, place, slot)) return false
        h.job = MineJob(JobKind.ROOM, place, slot, kind, 0).also { it.pieces = MineRooms.preset(kind) }
        h.askDemolish = -1
        fx(MineEvent.PLANK, slot, slot * Mine.SLOT_W + 1f, 0.85f)
        touch()
        return true
    }

    /** The second floor goes up with a crane. */
    fun buildUpper(): Boolean {
        val h = house
        if (h.job != null || !Mine.canBuildUpper(h)) return false
        h.job = MineJob(JobKind.UPPER, world.place.takeIf { it.mine } ?: PlaceId.MINE_YARD, 0, null, 0).also { it.pieces = MineRooms.landing }
        fx(MineEvent.CRANE, 0, yardDoorX(), 0.4f)
        touch()
        return true
    }

    /** Tears a room down: everything in it goes to the store, whoever stood in it walks to the hall, nothing is lost. */
    fun demolish(place: PlaceId, slot: Int): Boolean {
        val h = house
        if (h.job != null || !Mine.canDemolish(h, place, slot)) return false
        val range = Mine.slotRange(slot)
        val hallX = Mine.SLOT_W * 0.6f
        for (f in world.fixturesIn(place).filter { it.x in range && House.passageAt(it) == null && it.host < 0 }) {
            if (!sim.designer.store(place, f)) {
                // Whatever the designer would not take still goes to the store: nothing is lost.
                for (child in world.fixturesIn(place).filter { it.host == f.id }) {
                    world.storage += Stored(child.type, child.variant)
                    world.fixtures.remove(child.id)
                }
                world.storage += Stored(f.type, f.variant)
                world.fixtures.remove(f.id)
            }
        }
        for (b in world.bodiesIn(place)) {
            if (b.x !in range || b.held || b.mode != Mode.FREE) continue
            if (b is Person) House.moveTo(world, b, place, hallX + random.nextFloat() * 0.5f) else { b.x = hallX + 0.2f + random.nextFloat() * 0.5f; b.resting = false; b.vy = -0.3f }
        }
        h.set(place, slot, null)
        world.styles.remove(Decor.key(place, slot))
        sim.invalidate(place)
        if (h.selected == slot && h.selectedPlace == place) h.selected = -1
        h.askDemolish = -1
        fx(MineEvent.DEMOLISH, slot, slot * Mine.SLOT_W + 1f, 0.8f)
        touch()
        return true
    }

    /** Changes the look from outside; only what is given changes. */
    fun restyle(wall: Int? = null, roof: Int? = null, roofColor: Int? = null, door: Int? = null, windows: Int? = null, chimney: Boolean? = null, flag: Boolean? = null) {
        val h = house
        wall?.let { h.wall = it.mod(Mine.WALL_COLORS) }
        roof?.let { h.roof = it.mod(Mine.ROOFS) }
        roofColor?.let { h.roofColor = it.mod(Mine.ROOF_COLORS) }
        door?.let { h.door = it.mod(Mine.DOORS) }
        windows?.let { h.windows = it.mod(Mine.WINDOWS) }
        chimney?.let { h.chimney = it }
        flag?.let { h.flag = it }
        Mine.syncFixtures(world)
        fx(MineEvent.LOOK, if (wall != null) 0 else if (roof != null || roofColor != null) 1 else 2, yardDoorX(), 0.5f)
        sim.tasks.record(Deed.MI_LOOK, PlaceId.MINE_YARD)
        touch()
    }

    /** Completes the running job at once. */
    fun finishJob() {
        val job = house.job ?: return
        if (!job.committed) commit(job)
        while (job.pieceIndex < job.pieces.size) addPiece(job)
        finish(job)
    }

    private fun slotCenter(job: MineJob): Float = if (job.place == PlaceId.MINE_YARD) yardDoorX() else job.slot * Mine.SLOT_W + 1f

    private fun commit(job: MineJob) {
        val h = house
        job.committed = true
        when (job.kind) {
            JobKind.FOUNDATION -> {
                Mine.applyTemplate(h, job.shape)
                h.started = true
                world.flags += Mine.FLAG_STARTED
                Mine.syncFixtures(world)
                job.pieces = MineRooms.hall
                sim.unlock("mine_start")
                fx(MineEvent.WALLS_UP, 0, yardDoorX(), 0.8f)
            }
            JobKind.ROOM -> {
                h.set(job.place, job.slot, job.room)
                world.styles.remove(Decor.key(job.place, job.slot))
                sim.invalidate(job.place)
                fx(MineEvent.WALLS_UP, job.slot, slotCenter(job), 0.8f)
            }
            JobKind.UPPER -> {
                Mine.buildUpper(world)
                sim.unlock("mine_second_floor")
                fx(MineEvent.WALLS_UP, 0, yardDoorX(), 0.4f)
            }
        }
        touch()
    }

    /** Puts the next piece of the preset in its room. */
    private fun addPiece(job: MineJob) {
        val piece = job.pieces.getOrNull(job.pieceIndex) ?: return
        job.pieceIndex++
        val place = when (job.kind) {
            JobKind.FOUNDATION -> PlaceId.MINE_GROUND
            JobKind.UPPER -> PlaceId.MINE_UPPER
            JobKind.ROOM -> job.place
        }
        val f = placePiece(place, piece, if (job.kind == JobKind.ROOM) job.slot else 0) ?: return
        fx(MineEvent.PIECE, f.type.ordinal, f.x, f.y - f.spec.h / 2f, f)
    }

    /** Puts a piece of furniture in a room without the designer's «furnished» cheer (see [Designer.add]). */
    fun placePiece(place: PlaceId, piece: MinePiece, slot: Int): Fixture? {
        val used = world.fixturesIn(place).map { place.indexOf(it.id) }.toSet()
        val index = (place.addedFrom..place.addedMax).firstOrNull { it !in used } ?: return null
        val x = slot * Mine.SLOT_W + piece.dx
        val y = if (piece.wallY.isNaN()) place.floor + piece.shift else piece.wallY
        val f = Fixture(place.idBase + index, place, piece.type, x, y, piece.variant, y)
        world.fixtures[f.id] = f
        val spot = sim.clampFixture(place, f, x, y)
        f.x = spot[0]
        f.y = spot[1]
        f.depth = if (f.spec.wall) f.y else spot[1]
        f.anim = 1f
        f.on = piece.on
        sim.invalidate(place)
        return f
    }

    private fun finish(job: MineJob) {
        val h = house
        val place = when (job.kind) {
            JobKind.FOUNDATION -> PlaceId.MINE_GROUND
            JobKind.UPPER -> PlaceId.MINE_UPPER
            JobKind.ROOM -> job.place
        }
        if (job.kind == JobKind.ROOM && job.room != null) spawnThings(place, job)
        h.job = null
        when (job.kind) {
            JobKind.FOUNDATION -> fx(MineEvent.FOUNDATION_DONE, job.shape, yardDoorX(), 0.5f)
            JobKind.ROOM -> {
                fx(MineEvent.ROOM_DONE, job.room?.ordinal ?: 0, slotCenter(job), 0.6f)
                sim.tasks.record(Deed.MI_ROOM, PlaceId.MINE_YARD)
                selectNext(job.place, slotCenter(job))
            }
            JobKind.UPPER -> {
                fx(MineEvent.UPPER_DONE, 0, yardDoorX(), 0.4f)
                sim.tasks.record(Deed.MI_FLOOR, PlaceId.MINE_YARD)
            }
        }
        touch()
    }

    /** The small things of a room land on their furniture and are remembered as theirs, so tidying puts them back. */
    private fun spawnThings(place: PlaceId, job: MineJob) {
        val room = job.room ?: return
        val things = MineRooms.things(room)
        val placed = world.fixturesIn(place).filter { it.x in Mine.slotRange(job.slot) }
        for (th in things) {
            val piece = job.pieces.getOrNull(th.piece) ?: continue
            val f = placed.firstOrNull { it.type == piece.type && abs(it.x - (job.slot * Mine.SLOT_W + piece.dx)) < 0.15f } ?: continue
            val t = world.addThing(th.type, th.variant, place, f.x + th.dx, f.y - th.rest)
            t.age = 10f
            t.homePlace = place
            t.homeOwner = f.id
            t.homeDx = th.dx
            t.homeDy = -th.rest
            t.homeInside = false
            t.resting = false
            t.restOwner = -2
        }
    }

    // ------------------------------------------------------------------ housewarming

    /** Calls some figures from the village to the house for a party, around scene x [centerX]. */
    fun housewarming(centerX: Float): Boolean {
        val h = house
        if (!Mine.canParty(h) || h.party != null || h.job != null) return false
        val place = world.place.takeIf { it.mine } ?: PlaceId.MINE_YARD
        val away = world.people().filter {
            val at = it.place
            at != null && !at.mine && it.mode == Mode.FREE && !it.held && it.species != Species.DRAGON
        }.shuffled(random).take(5)
        val party = MineParty(place)
        h.party = party
        val n = away.size
        for ((i, p) in away.withIndex()) {
            h.guests += Guest(p.id, p.place!!, p.x)
            val x = (centerX + (i - (n - 1) / 2f) * 0.34f).coerceIn(0.3f, place.width - 0.3f)
            val ground = (if (place == PlaceId.MINE_YARD) 0.86f else 0.87f) + (i % 3) * 0.04f
            House.moveTo(world, p, place, x, ground)
            p.ground = ground
            p.anim.cheer = 2f
            p.anim.sparkle = 1f
            fx(MineEvent.GUEST, i, x, ground)
        }
        sim.unlock("mine_housewarming")
        if (world.flags.add(Mine.FLAG_PARTY)) world.stickers += world.stickers.size
        sim.tasks.record(Deed.MI_PARTY, PlaceId.MINE_YARD)
        fx(MineEvent.PARTY_START, n, centerX, 0.4f)
        touch()
        return true
    }

    /** The party is over: the guests go back where they came from, and a present is left behind. */
    fun endParty() {
        val h = house
        val party = h.party ?: return
        val place = party.place
        for (g in h.guests) {
            val p = world.bodies[g.id] as? Person ?: continue
            if (p.place == place) {
                fx(MineEvent.GUEST, -1, p.x, p.y)
                House.moveTo(world, p, g.place, g.x)
            }
        }
        h.guests.clear()
        h.party = null
        val spotX = if (place == PlaceId.MINE_YARD) yardDoorX() + 0.6f else 1.6f
        val gift = world.addThing(ThingType.GIFT, random.nextInt(ThingType.GIFT.variants), place, spotX, 0.5f)
        gift.vy = -0.5f
        sim.listener.onSpawn(gift)
        fx(MineEvent.PARTY_END, 0, spotX, 0.5f)
        touch()
    }

    /** The child leaves the house, or the app goes to the background: whatever runs is finished, so nothing is lost. */
    fun leave() {
        finishJob()
        endParty()
    }

    // ------------------------------------------------------------------ time

    private var doorClock = 8f
    private val heading = HashSet<Int>()
    private var cheerClock = 0f

    fun tick(place: PlaceId, dt: Float) {
        val h = house
        val job = h.job
        if (job != null) {
            if (job.place != place && !(job.kind == JobKind.UPPER && place.mine)) finishJob() else stepJob(job, place, dt)
        }
        h.party?.let { party ->
            if (party.place != place) {
                endParty()
            } else {
                party.t += dt
                cheerClock -= dt
                if (cheerClock <= 0f) {
                    cheerClock = 1.2f
                    for (g in h.guests) (world.bodies[g.id] as? Person)?.let { if (it.place == place) it.anim.cheer = 1.6f }
                    for (p in world.people()) if (p.place == place && p.place?.mine == true && p.species == Species.FOLK) p.anim.cheer = 1.6f
                }
                party.nextConfetti -= dt
                if (party.nextConfetti <= 0f) {
                    party.nextConfetti = 2.4f
                    fx(MineEvent.CONFETTI, 0, if (place == PlaceId.MINE_YARD) yardDoorX() + 0.4f else 1.4f, 0.3f)
                }
                if (party.t >= PARTY_SECONDS) endParty()
            }
        }
        if (place == PlaceId.MINE_YARD && h.started && h.job == null && h.party == null) stepDoor(dt)
    }

    private fun stepJob(job: MineJob, place: PlaceId, dt: Float) {
        job.t += dt
        val cx = slotCenter(job)
        val spread = if (job.kind == JobKind.ROOM) 1.5f else 1.0f
        val y = if (job.kind == JobKind.UPPER) 0.45f else 0.84f
        job.nextHit -= dt
        if (job.nextHit <= 0f && job.t < job.total - 0.5f) {
            job.nextHit = if (job.kind == JobKind.UPPER && !job.committed) 0.55f else 0.3f
            val x = cx + (random.nextFloat() - 0.5f) * spread
            val code = when {
                job.kind == JobKind.UPPER && !job.committed -> MineEvent.CRANE
                job.hits % 5 == 2 -> MineEvent.SAW
                job.hits % 5 == 4 && job.kind == JobKind.ROOM -> MineEvent.DRILL
                job.hits % 7 == 6 -> MineEvent.PLANK
                else -> MineEvent.HAMMER
            }
            job.hits++
            fx(code, job.hits, x, y)
        }
        if (!job.committed && job.t >= job.commitAt) commit(job)
        if (job.committed) {
            val first = job.commitAt + 0.35f
            while (job.pieceIndex < job.pieces.size && job.t >= first + job.pieceIndex * 0.14f) addPiece(job)
        }
        if (job.t >= job.total) finishJob()
    }

    /** Figures who live here walk to the door and go in, and come out again, by themselves. */
    private fun stepDoor(dt: Float) {
        val doorX = yardDoorX()
        val doorGround = 0.86f
        for (id in heading.toList()) {
            val p = world.bodies[id] as? Person
            if (p == null || p.place != PlaceId.MINE_YARD || p.held || p.mode != Mode.FREE) {
                heading.remove(id)
            } else if (abs(p.x - doorX) < 0.07f) {
                heading.remove(id)
                fx(MineEvent.DOOR_PASS, 1, doorX, 0.7f)
                House.moveTo(world, p, PlaceId.MINE_GROUND, 0.9f)
            } else if (p.anim.walkTo.isNaN() && p.resting && p.anim.wish == null) {
                p.anim.walkTo = doorX
                p.anim.walkGround = doorGround
            }
        }
        doorClock -= dt
        if (doorClock > 0f) return
        doorClock = 14f + random.nextFloat() * 16f
        val residents = world.people().filter { it.place?.mine == true && it.species != Species.DRAGON }
        val inside = residents.filter { it.place == PlaceId.MINE_GROUND && it.mode == Mode.FREE && !it.held && it.anim.pose == Pose.STAND }
        val outside = residents.filter { it.place == PlaceId.MINE_YARD && it.mode == Mode.FREE && !it.held && it.id !in heading && it.anim.wish == null && it.resting && it.anim.pose == Pose.STAND }
        if (inside.isNotEmpty() && (outside.isEmpty() || random.nextBoolean())) {
            val p = inside[random.nextInt(inside.size)]
            fx(MineEvent.DOOR_PASS, 0, doorX, 0.7f)
            House.moveTo(world, p, PlaceId.MINE_YARD, doorX + 0.04f, doorGround)
            p.ground = doorGround
            p.anim.hopV = 1.2f
        } else if (outside.isNotEmpty()) {
            val p = outside[random.nextInt(outside.size)]
            heading += p.id
            p.anim.walkTo = doorX
            p.anim.walkGround = doorGround
        }
    }

    // ------------------------------------------------------------------ furniture

    /** What the furniture of the rooms and the yard does when it is tapped. True when [f] is one of them. */
    fun tapFixture(place: PlaceId, f: Fixture): Boolean {
        val x = f.x + f.shiftX
        val y = f.y - f.spec.h / 2f
        when (f.type) {
            FixtureType.MI_FIREPLACE -> { f.count++; f.on = true; fx(MineEvent.FLARE, if (f.count % 5 == 0) 1 else 0, x, y, f) }
            FixtureType.MI_DINING_TABLE -> { f.on = !f.on; fx(MineEvent.CANDLE, if (f.on) 1 else 0, x, f.y - f.spec.h, f) }
            FixtureType.MI_CHANDELIER -> { f.on = !f.on; fx(MineEvent.CHANDELIER, if (f.on) 1 else 0, x, y, f) }
            FixtureType.MI_COUNTER -> { f.on = true; f.timer = 1.3f; fx(MineEvent.TOAST, 0, x, f.y - f.spec.h, f) }
            FixtureType.MI_BEDSIDE -> { f.on = !f.on; fx(MineEvent.NIGHT_LIGHT, if (f.on) 1 else 0, x, y, f) }
            FixtureType.MI_BLOCKS -> { f.mode = 1 - f.mode; fx(if (f.mode == 1) MineEvent.BLOCKS_FALL else MineEvent.BLOCKS_BUILD, 0, x, y, f) }
            FixtureType.MI_ROCKING_HORSE -> { f.on = true; f.timer = 3f; fx(MineEvent.HORSE, 0, x, y, f) }
            FixtureType.MI_BIG_BOOKCASE -> { f.mode = (f.mode + 1) % 3; fx(MineEvent.LADDER, f.mode, x, y, f) }
            FixtureType.MI_GLOBE -> { f.on = true; f.timer = 3.5f; fx(MineEvent.GLOBE, 0, x, y, f) }
            FixtureType.MI_SAW_BENCH -> { f.on = true; f.timer = 1.6f; fx(MineEvent.SAW_BENCH, 0, x, f.y - f.spec.h, f) }
            FixtureType.MI_LAUNDRY_BASKET -> { f.count++; fx(MineEvent.SOCKS, f.count, x, y, f) }
            FixtureType.MI_GUITAR -> { f.on = true; f.timer = 1.4f; fx(MineEvent.STRUM, 0, x, y, f) }
            FixtureType.MI_PLANT_BED -> { f.count = (f.count + 1) % 4; fx(MineEvent.WATER_BED, f.count, x, y, f) }
            FixtureType.MI_HANGING_POT -> { f.on = true; f.timer = 2f; fx(MineEvent.HANGING_POT, 0, x, y, f) }
            FixtureType.MI_COAT_RACK -> { f.on = true; f.timer = 1.6f; fx(MineEvent.COAT_RACK, 0, x, y, f) }
            FixtureType.MI_MAILBOX -> { f.on = !f.on; fx(MineEvent.MAILBOX, if (f.on) 1 else 0, x, y, f) }
            FixtureType.MI_FENCE -> { f.count++; fx(MineEvent.FENCE, f.count, x, y, f) }
            FixtureType.MI_FLOWER_BED -> fx(MineEvent.FLOWER_BED, f.variant, x, y, f)
            FixtureType.MI_SWING -> { f.on = true; f.timer = 4.5f; fx(MineEvent.SWING, 0, x, y, f) }
            FixtureType.MI_BIRD_BATH -> { f.on = true; f.timer = 2.5f; fx(MineEvent.BIRD_BATH, 0, x, y, f) }
            FixtureType.MI_GNOME -> { f.count++; fx(MineEvent.GNOME, f.count, x, y, f) }
            FixtureType.MI_SANDBOX -> fx(MineEvent.SANDBOX, 0, x, y, f)
            FixtureType.MI_APPLE_TREE -> shakeTree(place, f)
            else -> return false
        }
        return true
    }

    private fun shakeTree(place: PlaceId, f: Fixture) {
        val x = f.x + f.shiftX
        fx(MineEvent.APPLE_TREE, 0, x, f.y - f.spec.h * 0.7f, f)
        val apples = world.bodiesIn(place).count { it is Thing && it.type == ThingType.APPLE }
        if (apples >= 3) return
        val dropX = x + (random.nextFloat() - 0.5f) * 0.3f
        val apple = world.addThing(ThingType.APPLE, 0, place, dropX, f.y - f.spec.h * 0.75f)
        apple.vy = 0.2f
        apple.vx = (dropX - x) * 0.6f
        sim.listener.onSpawn(apple)
        // An apple on the head says «bonk».
        val under = world.bodiesIn(place).filterIsInstance<Person>().firstOrNull { !it.held && it.mode == Mode.FREE && abs(it.x - dropX) < 0.08f && it.y > f.y - 0.1f }
        if (under != null) fx(MineEvent.APPLE_BONK, under.id, under.x, under.y - under.h)
    }

    fun stepFixture(f: Fixture, dt: Float) {
        if (f.timer <= 0f) return
        if (f.type !in TIMED) return
        f.timer -= dt
        if (f.timer <= 0f) {
            f.timer = 0f
            f.on = false
        }
    }

    /** Where a figure sits on a swing or a rocking horse that is moving. */
    fun seatPoint(f: Fixture, spot: Int): FloatArray? {
        val s = f.spec.spots.getOrNull(spot) ?: return null
        val fade = (f.timer / 1.2f).coerceIn(0f, 1f)
        return when {
            f.type == FixtureType.MI_SWING && f.on -> floatArrayOf(f.x + f.shiftX + s.dx + sin(sim.time * 3.2f) * 0.075f * fade, f.y + s.dy + (1f - kotlin.math.cos(sim.time * 3.2f)) * 0.012f)
            f.type == FixtureType.MI_ROCKING_HORSE && f.on -> floatArrayOf(f.x + f.shiftX + s.dx + sin(sim.time * 5f) * 0.012f * fade, f.y + s.dy)
            else -> null
        }
    }

    companion object {
        const val PARTY_SECONDS = 26f

        private val TIMED = setOf(
            FixtureType.MI_COUNTER, FixtureType.MI_ROCKING_HORSE, FixtureType.MI_GLOBE, FixtureType.MI_SAW_BENCH, FixtureType.MI_GUITAR,
            FixtureType.MI_HANGING_POT, FixtureType.MI_COAT_RACK, FixtureType.MI_SWING, FixtureType.MI_BIRD_BATH,
        )
    }
}

/** The rules of the three places of Mitt hus: all of them live in [MineBuilder]. */
class MineRules(private val builder: MineBuilder) : FloorRules {
    override fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean = builder.tapFixture(place, f)

    override fun step(place: PlaceId, f: Fixture, dt: Float) = builder.stepFixture(f, dt)

    override fun tick(place: PlaceId, dt: Float) = builder.tick(place, dt)

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? = builder.seatPoint(f, spot)
}
