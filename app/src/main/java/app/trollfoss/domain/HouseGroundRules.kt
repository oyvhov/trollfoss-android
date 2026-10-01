package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * The rules of Storstova. Every piece of furniture here does something when it is tapped, and most of them
 * do something more when a thing is dropped on them. State that must survive a save lives in the fixture's
 * `open`, `on`, `mode` and `count`; what is only a moment lives in `timer`, `angle`, `angleV` and `bob`.
 * Who does what is told in the comments of each handler; the sounds and sparkle are in
 * ui/play/HouseGroundFx.kt and are asked for with [emit].
 */
class GroundRules(private val sim: Sim, private val random: Random) : FloorRules {
    private val world get() = sim.world
    private val listener get() = sim.listener
    private val place = PlaceId.MANOR_GROUND

    private val rolf = GroundRolf(sim, random, this)

    // What is going on right now. Never saved: a film night is a moment, not a place.
    private var started = false
    private var lastNight = false
    private var film = 0f
    private var filmLamps = ArrayList<Int>()
    private var dim = 0f
    private var cookClock = 0f

    init {
        GroundFloor.dim = 0f
    }

    // ------------------------------------------------------------------ helpers

    internal fun fixture(index: Int): Fixture? = world.fixtures[place.idBase + index]

    internal fun emit(code: Int, arg: Int, x: Float, y: Float, f: Fixture? = null, thing: Thing? = null) {
        listener.onFx(Fx.HOUSE, x, y, f, thing, HouseFx.pack(code, arg))
    }

    private fun emitAt(code: Int, arg: Int, f: Fixture) = emit(code, arg, f.x + f.shiftX, f.y - f.spec.h * 0.6f, f)

    /** Keeps a fixture drawn live (not as a still picture) while it moves. */
    private fun live(f: Fixture) {
        f.anim = max(f.anim, 0.03f)
    }

    /** Where things that leave [f] land: in front of it, or on the floor in front of the wall. */
    private fun front(f: Fixture): Float = if (f.spec.wall) place.floor - 0.02f else f.depth + 0.03f

    /** Keeps the floor from filling up: the oldest loose thing goes in a puff. */
    internal fun cap(keep: Thing) {
        val things = world.bodiesIn(place).filterIsInstance<Thing>()
        if (things.size <= Sim.MAX_THINGS) return
        val oldest = things.filter { it !== keep && it.mode == Mode.FREE && it.inside < 0 && !it.held && it.type != ThingType.GOLDEN_KEY }
            .minByOrNull { it.z } ?: return
        listener.onFx(Fx.POOF, oldest.x, oldest.y - oldest.h / 2, thing = oldest)
        sim.removeThing(oldest)
    }

    internal fun spawn(type: ThingType, variant: Int, x: Float, y: Float, vx: Float, vy: Float, ground: Float, vrot: Float = 0f): Thing {
        val t = world.addThing(type, variant, place, x, y)
        t.ground = ground
        t.vx = vx
        t.vy = vy
        t.vrot = vrot
        cap(t)
        listener.onSpawn(t)
        return t
    }

    private fun puff(f: Fixture, dx: Float = 0f, dy: Float = 0f) = emit(GroundCode.PUFF, 0, f.x + dx, f.y + dy, f)

    /** The lamps of the floor: they come on at dusk and go out at dawn. */
    private fun lamps(): List<Fixture> = world.fixturesIn(place).filter {
        it.type == FixtureType.GR_FLOOR_LAMP || it.type == FixtureType.GR_CHANDELIER || it.type == FixtureType.GR_CANDELABRA ||
            it.type == FixtureType.GR_DESK || it.type == FixtureType.GR_FIREPLACE
    }

    private fun setLamps(on: Boolean) {
        for (l in lamps()) l.on = on
    }

    private fun nearestPerson(x: Float, depth: Float, reach: Float): Person? =
        world.bodiesIn(place).filterIsInstance<Person>()
            .filter { !it.held && it.mode != Mode.BAG && abs(it.x - x) < reach && abs(sim.groundOf(place, it) - depth) < 0.14f }
            .minByOrNull { abs(it.x - x) }

    private fun laughNear(f: Fixture) {
        for (p in world.bodiesIn(place)) {
            if (p is Person && !p.held && abs(p.x - f.x) < 0.7f && p.anim.face != Face.SLEEP) {
                p.anim.face = Face.LAUGH
                p.anim.faceTime = 1.2f
            }
        }
    }

    // ------------------------------------------------------------------ taps

    override fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean {
        if (place != this.place) return false
        return when (f.type) {
            FixtureType.STAIRCASE -> { emitAt(GroundCode.STAIRS, 0, f); false }
            FixtureType.LIFT -> { emitAt(GroundCode.LIFT, 0, f); false }
            FixtureType.DOOR -> { emitAt(if (f.variant == 0) GroundCode.CREAK else GroundCode.GARDEN_DOOR, 0, f); false }
            FixtureType.SECRET_DOOR -> secretShelf(f, dx, dy)

            FixtureType.GR_CLOCK -> clock(f)
            FixtureType.GR_COAT_RACK -> coatRack(f, dx)
            FixtureType.GR_UMBRELLA_STAND -> umbrellas(f)
            FixtureType.GR_WINDOW -> window(f)
            FixtureType.GR_ARMOUR -> armour(f)
            FixtureType.GR_CHANDELIER -> chandelier(f)
            FixtureType.GR_POST_SLOT -> post(f)
            FixtureType.GR_PORTRAIT -> portrait(f)

            FixtureType.GR_FIREPLACE -> fireplace(f, dx, dy)
            FixtureType.GR_WINGCHAIR -> { puff(f); f.anim = 1f; true }
            FixtureType.GR_SOFA -> sofa(f)
            FixtureType.GR_COFFEE_TABLE -> { emitAt(GroundCode.TABLE, 2, f); true }
            FixtureType.GR_POPCORN_BOWL -> popcornBowl(f)
            FixtureType.GR_GLOBE -> globe(f)
            FixtureType.GR_TV -> tv(f)
            FixtureType.GR_AQUARIUM -> aquarium(f, dx, dy)
            FixtureType.GR_FLOOR_LAMP, FixtureType.GR_CANDELABRA -> lamp(f)

            FixtureType.GR_BOOKSHELF -> books(f, dx, dy)
            FixtureType.GR_LADDER -> ladder(f)
            FixtureType.GR_DESK -> desk(f, dx)
            FixtureType.GR_LECTERN -> lectern(f)
            FixtureType.GR_BUST -> bust(f)

            FixtureType.GR_DINING_TABLE -> { emitAt(GroundCode.TABLE, if (f.mode == 1) 2 else 3, f); true }
            FixtureType.GR_CHAIR_ROW, FixtureType.GR_DINING_CHAIR -> { puff(f); true }
            FixtureType.GR_BELL -> bell(f)
            FixtureType.GR_CAKE -> cake(f)
            FixtureType.GR_SIDEBOARD, FixtureType.GR_FRIDGE, FixtureType.GR_JAM_CABINET -> cupboard(f)

            FixtureType.GR_RANGE -> range(f, dy)
            FixtureType.GR_SINK -> sink(f)
            FixtureType.GR_MIXER -> mixer(f)
            FixtureType.GR_PIZZA_OVEN -> pizzaOven(f)
            FixtureType.GR_ISLAND -> island(f, dx)
            FixtureType.GR_STOOLS -> { puff(f); f.anim = 1f; true }

            FixtureType.GR_PLANT -> plant(f)
            FixtureType.GR_FOUNTAIN -> { f.timer = 1.2f; emitAt(GroundCode.FOUNTAIN, 0, f); true }
            FixtureType.GR_SOFIE -> { f.timer = 0.8f; emitAt(GroundCode.SOFIE, 0, f); true }
            FixtureType.GR_HAMMOCK -> { f.angleV += 1.6f; emitAt(GroundCode.HAMMOCK, 0, f); true }
            else -> false
        }
    }

    // ---- hall

    /** The grandfather clock strikes one more hour each time; at twelve a coin falls from its top. */
    private fun clock(f: Fixture): Boolean {
        f.count = f.count % 12 + 1
        f.angleV = 1f
        emit(GroundCode.CLOCK, f.count, f.x, f.top, f)
        if (f.count == 12) {
            spawn(ThingType.COIN, 0, f.x + 0.03f, f.top, 0.4f, -1.4f, front(f), 300f)
        }
        return true
    }

    /** A hat falls off a peg of the coat rack, a different one every time. */
    private fun coatRack(f: Fixture, dx: Float): Boolean {
        val hat = COAT_HATS[f.count % COAT_HATS.size]
        f.count++
        spawn(hat.first, hat.second, f.x + dx.coerceIn(-0.12f, 0.12f), f.y - 0.04f, (random.nextFloat() - 0.4f) * 0.5f, -0.4f, front(f), (random.nextFloat() - 0.5f) * 240f)
        emit(GroundCode.HAT, f.count % COAT_HATS.size, f.x + dx, f.y - 0.05f, f)
        return true
    }

    /** An umbrella in a colour of its own pops out of the stand. */
    private fun umbrellas(f: Fixture): Boolean {
        val variant = f.count % 4
        f.count++
        spawn(ThingType.GR_UMBRELLA, variant, f.x + 0.01f, f.top + 0.02f, 0.3f, -1.7f, front(f), 150f)
        emitAt(GroundCode.UMBRELLA, 1, f)
        return true
    }

    private fun window(f: Fixture): Boolean {
        if (f.variant == 0) {
            f.timer = 1.6f
            emitAt(GroundCode.WINDOW, 2, f)
        } else {
            f.mode = 1 - f.mode
            emitAt(GroundCode.WINDOW, f.mode, f)
        }
        return true
    }

    /**
     * Riddar Rusten. Tap after tap: he clanks, he salutes, and then he gets the hiccups and the helmet
     * pops off (and a mouse looks out of it). Tap him once more and the helmet hops back on.
     * `bob` is what he is doing (1 clank, 2 salute, 3 hiccup, 4 helmet in the air) and `timer` how long it lasts.
     */
    private fun armour(f: Fixture): Boolean {
        if (f.mode == 1) {
            f.mode = 0
            f.bob = 4f
            f.timer = 0.8f
            emitAt(GroundCode.ARMOUR, 4, f)
            return true
        }
        if (f.bob == 3f) return true
        val action = f.count % 3
        f.count++
        when (action) {
            0 -> { f.bob = 1f; f.timer = 0.7f }
            1 -> { f.bob = 2f; f.timer = 1.4f }
            else -> { f.bob = 3f; f.timer = 0.45f; f.angle = 3f }
        }
        emitAt(GroundCode.ARMOUR, action, f)
        return true
    }

    private fun stepArmour(f: Fixture, dt: Float) {
        if (f.bob == 0f) return
        live(f)
        f.timer -= dt
        if (f.bob == 3f) {
            if (f.timer <= 0f) {
                f.angle -= 1f
                if (f.angle <= 0f) {
                    // The third hiccup: off pops the helmet.
                    f.bob = 4f
                    f.timer = 1.2f
                    f.mode = 1
                    emitAt(GroundCode.ARMOUR, 3, f)
                    sim.unlock("ground_hall")
                    laughNear(f)
                } else {
                    f.timer = 0.5f
                    emitAt(GroundCode.ARMOUR, 2, f)
                }
            }
        } else if (f.timer <= 0f) {
            f.bob = 0f
            f.timer = 0f
        }
    }

    /** The slot throws a letter once a day; the rest of the time it only rattles. */
    private fun post(f: Fixture): Boolean {
        val day = sim.today.toInt()
        if (f.count != day) {
            f.count = day
            spawn(ThingType.GR_LETTER, day.mod(4), f.x + 0.02f, f.y - 0.02f, 0.45f, -0.8f, place.floor - 0.03f, 220f)
            emitAt(GroundCode.POST, 1, f)
        } else {
            emitAt(GroundCode.POST, 0, f)
        }
        return true
    }

    /** The chandelier swings and sparkles, and its light goes on or off. */
    private fun chandelier(f: Fixture): Boolean {
        f.on = !f.on
        f.angleV += if (f.angleV >= 0f) 2.4f else -2.4f
        emitAt(GroundCode.CHANDELIER, if (f.on) 1 else 0, f)
        return true
    }

    private fun portrait(f: Fixture): Boolean {
        f.timer = 2.4f
        emitAt(GroundCode.PORTRAIT, f.variant, f)
        return true
    }

    // ---- living room

    /**
     * The fire is lit or put out. A tap at the stockings gives a treat. Sausages and marshmallows left on
     * the grate are roasted by the fire.
     */
    private fun fireplace(f: Fixture, dx: Float, dy: Float): Boolean {
        if (dx < -0.09f && dy < -0.34f) {
            val treat = TREATS[f.count % TREATS.size]
            f.count++
            spawn(treat.first, treat.second, f.x + dx, f.y - 0.36f, -0.1f, -0.2f, front(f), 120f)
            emitAt(GroundCode.STOCKING, 0, f)
            return true
        }
        f.on = !f.on
        emitAt(GroundCode.FIRE, if (f.on) 1 else 0, f)
        return true
    }

    /** The sofa puffs dust. Four quick taps and coins fall out of it, like in every sofa. */
    private fun sofa(f: Fixture): Boolean {
        f.anim = 1f
        if (f.taps >= 4) {
            f.taps = 0
            repeat(3) { i ->
                spawn(ThingType.COIN, 0, f.x - 0.12f + i * 0.12f, f.y - 0.1f, (i - 1) * 0.35f, -1.5f - i * 0.2f, front(f), 260f)
            }
            emitAt(GroundCode.SOFA, 1, f)
        } else {
            emitAt(GroundCode.SOFA, 0, f)
        }
        return true
    }

    private fun tv(f: Fixture): Boolean {
        if (film > 0f) {
            endFilm()
            return true
        }
        f.mode = (f.mode + 1) % 6
        f.on = f.mode != 0
        emit(GroundCode.TV, f.mode, f.x, f.top + 0.1f, f)
        return true
    }

    /** The popcorn bowl starts film night; once the film runs, it gives a piece of popcorn. */
    private fun popcornBowl(f: Fixture): Boolean {
        if (film <= 0f) {
            startFilm(f)
        } else {
            val t = spawn(ThingType.POPCORN, 0, f.x + 0.03f, f.y - 0.05f, 0.25f, -1.3f, front(f), 120f)
            emit(GroundCode.POPCORN, 0, t.x, t.y, f, t)
        }
        return true
    }

    private fun startFilm(bowl: Fixture) {
        film = FILM_SECONDS
        val tv = fixture(GroundIx.TV)
        if (tv != null) {
            tv.on = true
            tv.count = 1
        }
        filmLamps.clear()
        for (l in lamps()) {
            if (l.type == FixtureType.GR_FIREPLACE) continue
            if (l.on) filmLamps += l.id
            l.on = false
        }
        emit(GroundCode.FILM, 1, bowl.x, bowl.top, bowl)
        val table = fixture(GroundIx.COFFEE_TABLE)
        repeat(3) { i ->
            spawn(ThingType.POPCORN, 0, (table?.x ?: bowl.x) - 0.07f + i * 0.07f, bowl.y - 0.1f, 0f, -0.5f - i * 0.1f, front(bowl), 90f)
        }
        sim.unlock("ground_living")
        sim.tasks.record(Deed.GR_FILM, place, ThingType.POPCORN, FixtureType.GR_POPCORN_BOWL)
        // Everybody on the sofa cheers.
        for (p in world.bodiesIn(place)) if (p is Person && p.mode == Mode.SEATED && abs(p.x - bowl.x) < 0.6f) {
            p.anim.face = Face.WOW
            p.anim.faceTime = 1.2f
            p.anim.hopV = 1.2f
        }
    }

    private fun endFilm() {
        film = 0f
        fixture(GroundIx.TV)?.let { tv ->
            tv.count = 0
            tv.on = tv.mode != 0
        }
        for (id in filmLamps) world.fixtures[id]?.on = true
        filmLamps.clear()
        fixture(GroundIx.POPCORN)?.let { emit(GroundCode.FILM, 0, it.x, it.top, it) }
    }

    /** The globe spins; spun hard it spits out a map pin. */
    private fun globe(f: Fixture): Boolean {
        f.angleV += 5.5f + random.nextFloat() * 2f
        val fast = f.angleV > 8f && f.timer <= 0f
        if (fast) {
            f.timer = 1.1f
            val dir = if (random.nextBoolean()) 1f else -1f
            spawn(ThingType.GR_PIN, f.count % 4, f.x + 0.05f * dir, f.y - 0.2f, 0.55f * dir, -1.8f, front(f), 300f)
            f.count++
        }
        emitAt(GroundCode.GLOBE, if (fast) 1 else 0, f)
        return true
    }

    /** The fish swim over to the finger. */
    private fun aquarium(f: Fixture, dx: Float, dy: Float): Boolean {
        f.angle = (dx / (f.spec.w / 2f)).coerceIn(-1f, 1f)
        f.angleV = ((dy + f.spec.h / 2f) / (f.spec.h / 2f)).coerceIn(-1f, 1f)
        f.timer = 3.5f
        emit(GroundCode.FISH, 0, f.x + dx, f.y + dy, f)
        return true
    }

    private fun lamp(f: Fixture): Boolean {
        f.on = !f.on
        emitAt(GroundCode.LAMP, if (f.on) 1 else 0, f)
        return true
    }

    // ---- library

    /** Books flap out of the shelf like startled birds. */
    private fun books(f: Fixture, dx: Float, dy: Float): Boolean {
        val n = if (f.taps >= 3) 3 else 1
        repeat(n) { i ->
            spawn(
                ThingType.BOOK, random.nextInt(4), f.x + dx.coerceIn(-0.12f, 0.12f), f.y + dy.coerceIn(-0.7f, -0.1f),
                (random.nextFloat() - 0.5f) * 1.0f, -1.2f - i * 0.2f, front(f), (random.nextFloat() - 0.5f) * 400f,
            )
        }
        f.count++
        emit(GroundCode.BOOKS, n, f.x + dx, f.y + dy, f)
        return true
    }

    /**
     * The red book in the bookcase between the shelves is a lever: the bookcase swings open on the secret
     * way to the attic. Tapped anywhere else it is only a locked bookcase.
     */
    private fun secretShelf(f: Fixture, dx: Float, dy: Float): Boolean {
        val open = f.mode == 1 || "manor_bookshelf" in world.flags
        if (open) {
            if (f.mode != 1) f.mode = 1
            return false
        }
        if (abs(dx - RED_BOOK_DX) < 0.05f && dy in -0.42f..-0.18f) {
            f.mode = 1
            f.timer = 1.8f
            sim.flag("manor_bookshelf")
            sim.unlock("ground_library")
            sim.tasks.record(Deed.GR_LEVER, place, fixture = f.type)
            emit(GroundCode.LEVER, 0, f.x, f.y - 0.3f, f)
            return true
        }
        return false
    }

    /** The ladder rolls along its rail to the next stop (with whoever sits on it). */
    private fun ladder(f: Fixture): Boolean {
        f.mode = (f.mode + 1) % LADDER_STOPS.size
        emitAt(GroundCode.LADDER, f.mode, f)
        return true
    }

    /** A tap on the green lamp switches it; a tap on the desk makes the quill scribble. */
    private fun desk(f: Fixture, dx: Float): Boolean {
        if (dx > 0.1f) {
            f.on = !f.on
            emitAt(GroundCode.LAMP, if (f.on) 1 else 0, f)
        } else {
            f.timer = 1.4f
            emitAt(GroundCode.DESK, 0, f)
        }
        return true
    }

    /** The talking book mumbles a story; tapped three times in a row it says «shhh!». */
    private fun lectern(f: Fixture): Boolean {
        val shush = f.taps >= 3
        if (shush) f.taps = 0
        f.timer = if (shush) 1.0f else 2.2f
        emitAt(GroundCode.LECTERN, if (shush) 9 else f.count % 3, f)
        f.count++
        if (!shush) laughNear(f)
        return true
    }

    private fun bust(f: Fixture): Boolean {
        f.timer = 1.4f
        emitAt(GroundCode.BUST, 0, f)
        return true
    }

    // ---- dining room

    /** Ring the bell and the table sets itself; ring again and the plates fly home. */
    private fun bell(f: Fixture): Boolean {
        val table = fixture(GroundIx.DINING_TABLE) ?: return true
        val setting = table.mode == 0
        table.mode = if (setting) 1 else 0
        table.timer = TABLE_SECONDS
        f.anim = 1f
        emit(GroundCode.TABLE, if (setting) 1 else 0, table.x, table.top, table)
        if (setting) sim.tasks.record(Deed.GR_TABLE, place, ThingType.GR_TRAY, FixtureType.GR_BELL)
        return true
    }

    /**
     * The birthday cake. Candles lit: a tap starts the song, and when it ends the candles are blown out
     * and a cupcake falls off the cake. Candles out: a tap lights them again.
     */
    private fun cake(f: Fixture): Boolean {
        if (f.mode == 1) return true
        if (f.on) {
            f.mode = 1
            f.timer = SONG_SECONDS
            emitAt(GroundCode.CAKE, 1, f)
        } else {
            f.on = true
            emitAt(GroundCode.CAKE, 0, f)
        }
        return true
    }

    private fun stepCake(f: Fixture, dt: Float) {
        if (f.mode != 1) return
        live(f)
        f.timer -= dt
        if (f.timer > 0f) return
        f.mode = 0
        f.on = false
        f.count++
        emitAt(GroundCode.CAKE, 2, f)
        spawn(ThingType.CUPCAKE, f.count % 4, f.x + 0.07f, f.y - 0.12f, 0.3f, -1.4f, front(f), 160f)
        sim.unlock("ground_dining")
        for (p in world.bodiesIn(place)) if (p is Person && !p.held && abs(p.x - f.x) < 1f) {
            p.anim.face = Face.LAUGH
            p.anim.faceTime = 1.6f
            p.anim.cheer = 1.6f
        }
    }

    /** Opens and shuts; the first time it is opened, the shelves are stocked. */
    private fun cupboard(f: Fixture): Boolean {
        f.open = !f.open
        sim.invalidate(place)
        if (f.open) restock(f)
        listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2, f)
        return true
    }

    private fun restock(f: Fixture) {
        val inside = world.bodiesIn(place).count { it.inside == f.id }
        val shelves = f.spec.surfaces.filter { it.interior }
        if (shelves.isEmpty()) return
        val wanted = when (f.type) {
            FixtureType.GR_FRIDGE -> listOf(ThingType.MILK to 0, ThingType.EGG to 0, ThingType.APPLE to 0, ThingType.JUICE to 0, ThingType.BROWN_CHEESE to 0, ThingType.SAUSAGE to 0, ThingType.CARROT to 0, ThingType.STRAWBERRY to 0)
            FixtureType.GR_JAM_CABINET -> listOf(ThingType.GR_JAM to 0, ThingType.GR_JAM to 1, ThingType.GR_JAM to 2, ThingType.GR_JAM to 3)
            else -> listOf(ThingType.CUP to 0, ThingType.CUP to 2, ThingType.CANDLE to 0)
        }
        val limit = if (f.type == FixtureType.GR_FRIDGE) 4 else 3
        if (inside >= limit) return
        for (i in 0 until limit - inside) {
            val pick = if (f.type == FixtureType.GR_FRIDGE) wanted[random.nextInt(wanted.size)] else wanted[(i + inside) % wanted.size]
            val shelf = shelves[i % shelves.size]
            val t = world.addThing(pick.first, pick.second, place, f.x - 0.06f + i * 0.045f, f.y + shelf.dy)
            t.inside = f.id
            t.resting = true
        }
    }

    // ---- kitchen

    /**
     * The range: a tap on the oven door opens it; shut with something inside it bakes (2.6 seconds); a tap
     * on the hobs lights them. Eggs and sausages on the hobs are fried.
     */
    private fun range(f: Fixture, dy: Float): Boolean {
        if (dy > -0.15f) {
            when {
                f.mode == 1 -> listener.onFx(Fx.TICK, f.x, f.top, f)
                !f.open -> {
                    f.open = true
                    sim.invalidate(place)
                    listener.onFx(Fx.OPEN, f.x, f.y - 0.1f, f)
                }
                else -> {
                    f.open = false
                    sim.invalidate(place)
                    if (world.bodiesIn(place).any { it.inside == f.id }) {
                        f.mode = 1
                        f.timer = 2.6f
                        emitAt(GroundCode.RANGE, 3, f)
                    } else {
                        listener.onFx(Fx.CLOSE, f.x, f.y - 0.1f, f)
                    }
                }
            }
        } else {
            f.on = !f.on
            emitAt(GroundCode.RANGE, if (f.on) 0 else 1, f)
        }
        return true
    }

    private fun bake(f: Fixture) {
        f.mode = 0
        val inside = world.bodiesIn(place).filterIsInstance<Thing>().filter { it.inside == f.id && it.mode == Mode.FREE }
        val floorY = f.y + (f.spec.surfaces.firstOrNull { it.interior }?.dy ?: -0.03f)
        if (inside.isNotEmpty()) {
            val results = Recipes.oven(inside.map { it.type })
            inside.forEach { sim.removeThing(it, quiet = true) }
            results.forEachIndexed { i, (made, key) ->
                val x = if (results.size == 1) f.x else f.x - 0.05f + i * 0.1f / (results.size - 1)
                val t = world.addThing(made.type, made.variant, place, x, floorY)
                t.inside = f.id
                t.resting = true
                listener.onSpawn(t)
                key?.let(sim::discover)
            }
        }
        f.open = true
        sim.invalidate(place)
        emitAt(GroundCode.RANGE, 2, f)
    }

    private fun sink(f: Fixture): Boolean {
        f.on = !f.on
        emitAt(GroundCode.SINK, if (f.on) 1 else 0, f)
        return true
    }

    /** The mixer winds up, and then it sprays whatever is in the bowl (and whoever is close) all over the place. */
    private fun mixer(f: Fixture): Boolean {
        if (f.on) return true
        f.on = true
        f.timer = 1.7f
        emitAt(GroundCode.MIXER, 0, f)
        return true
    }

    private fun finishMix(f: Fixture) {
        f.on = false
        val contents = world.inMachine(f)
        if (contents.isNotEmpty()) {
            val made = Recipes.blender(contents.map { it.type })
            Recipes.keyFor(FixtureType.BLENDER, contents.map { it.type })?.let(sim::discover)
            contents.forEach { sim.removeThing(it, quiet = true) }
            spawn(made.type, made.variant, f.x + 0.09f, f.y - 0.12f, 0.4f, -1.2f, front(f), 160f)
        }
        f.count++
        emitAt(GroundCode.MIXER, 1, f)
        val victim = nearestPerson(f.x, f.depth, 0.42f)
        if (victim != null) {
            victim.anim.cream = Jokes.CREAM_SECONDS
            listener.onFx(Fx.SPLAT, victim.x, victim.y - victim.h * 0.8f, thing = null, param = victim.id)
            sim.tasks.record(Deed.GR_SPLASH, place, fixture = f.type)
        }
    }

    /** The pizza oven breathes fire when it is tapped; dough dropped in comes out a pizza. */
    private fun pizzaOven(f: Fixture): Boolean {
        f.on = !f.on
        emitAt(GroundCode.PIZZA, if (f.on) 0 else 2, f)
        return true
    }

    private fun finishPizza(f: Fixture) {
        f.mode = 0
        spawn(ThingType.PIZZA, 0, f.x + 0.04f, f.y - 0.12f, 0.45f, -1.3f, front(f), 200f)
        emitAt(GroundCode.PIZZA, 1, f)
        sim.unlock("ground_kitchen")
        sim.tasks.record(Deed.GR_PIZZA, place, ThingType.PIZZA, f.type)
    }

    /** The fruit bowl on the island gives a piece of fruit; the rest of the island just boings. */
    private fun island(f: Fixture, dx: Float): Boolean {
        if (dx < -0.15f) {
            val fruit = ThingType.FRUITS[random.nextInt(ThingType.FRUITS.size)]
            val t = spawn(fruit, 0, f.x + dx, f.y - 0.22f, 0.2f, -1.2f, front(f), 120f)
            listener.onFx(Fx.DISPENSE, t.x, t.y, f, t)
        } else {
            listener.onFx(Fx.BOING, f.x + dx, f.top, f)
        }
        return true
    }

    // ---- winter garden

    /** A ripe plant gives a flower and goes back a step; a young one only rustles and may grow a little. */
    private fun plant(f: Fixture): Boolean {
        if (f.mode >= 3) {
            f.mode = 2
            f.timer = 1.0f
            val flower = spawn(ThingType.FLOWER, f.variant + f.count, f.x + 0.04f, f.y - 0.3f, 0.3f, -1.4f, front(f), 160f)
            f.count++
            emit(GroundCode.BLOOM, f.mode, flower.x, flower.y, f, flower)
        } else {
            f.timer = 0.8f
            emitAt(GroundCode.GROW, -1, f)
        }
        return true
    }

    private fun water(f: Fixture) {
        if (f.mode < 3) {
            f.mode++
            sim.tasks.record(Deed.GR_GROW, place, ThingType.WATERING_CAN, f.type)
            if (f.mode == 3) sim.unlock("ground_garden")
        }
        f.timer = 1.4f
        emitAt(GroundCode.GROW, f.mode, f)
    }

    // ------------------------------------------------------------------ drops

    override fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean {
        if (place != this.place) return false
        return when (f.type) {
            FixtureType.GR_UMBRELLA_STAND -> if (t.type == ThingType.GR_UMBRELLA) {
                sim.removeThing(t, quiet = true)
                emitAt(GroundCode.UMBRELLA, 0, f)
                true
            } else false
            FixtureType.GR_POST_SLOT -> if (t.type == ThingType.GR_LETTER) {
                sim.removeThing(t, quiet = true)
                emitAt(GroundCode.POST, 2, f)
                true
            } else false
            FixtureType.GR_AQUARIUM -> if (t.type.cat == Cat.FOOD) {
                sim.removeThing(t, quiet = true)
                f.timer = 3.5f
                f.angle = 0f
                f.angleV = -0.7f
                emitAt(GroundCode.FISH_FEED, 0, f)
                true
            } else false
            FixtureType.GR_FOUNTAIN -> if (t.type == ThingType.COIN) {
                sim.removeThing(t, quiet = true)
                f.timer = 2f
                emitAt(GroundCode.FOUNTAIN, 1, f)
                for (p in world.fixturesIn(place)) if (p.type == FixtureType.GR_PLANT) water(p)
                true
            } else false
            FixtureType.GR_PLANT -> {
                if (t.type == ThingType.WATERING_CAN) water(f)
                false
            }
            FixtureType.GR_SOFIE -> feedSofie(f, t)
            FixtureType.GR_PIZZA_OVEN -> if (t.type == ThingType.DOUGH && f.mode == 0) {
                sim.removeThing(t, quiet = true)
                f.mode = 1
                f.timer = 2.4f
                f.on = true
                emitAt(GroundCode.PIZZA, 0, f)
                true
            } else false
            FixtureType.GR_MIXER -> if (!f.on && world.inMachine(f).size < 3) {
                t.mode = Mode.INSIDE
                t.holder = f.id
                t.held = false
                t.resting = false
                t.inside = -1
                t.x = f.x
                t.y = f.top
                listener.onFx(Fx.INTO, f.x, f.top, f, t)
                true
            } else false
            else -> false
        }
    }

    /**
     * Sofie eats food and drink, burps, and the second time she burps hard enough to spit out the golden
     * key she swallowed (it glints in her belly until then). Anything else she spits back with a «ptui».
     */
    private fun feedSofie(f: Fixture, t: Thing): Boolean {
        val food = (t.type.cat == Cat.FOOD || t.type.cat == Cat.DRINK) && !t.type.potion
        if (!food) {
            emitAt(GroundCode.SOFIE, 4, f)
            return false
        }
        sim.removeThing(t, quiet = true)
        f.count++
        f.timer = 1.6f
        f.angleV = 0.9f
        sim.tasks.record(Deed.GR_FEED, place, t.type, f.type)
        emitAt(GroundCode.SOFIE, 3, f)
        return true
    }

    private fun sofieBurp(f: Fixture) {
        val first = f.count >= 2 && "manor_key_ground" !in world.flags
        emitAt(GroundCode.SOFIE, if (first) 2 else 1, f)
        if (first) {
            val key = world.addThing(ThingType.GOLDEN_KEY, 0, place, f.x + 0.03f, f.y - 0.3f)
            key.ground = f.depth + 0.05f
            key.vx = 0.55f
            key.vy = -2.1f
            key.vrot = 260f
            listener.onSpawn(key)
            sim.flag("manor_key_ground")
        }
    }

    // ------------------------------------------------------------------ time

    override fun step(place: PlaceId, f: Fixture, dt: Float) {
        if (place != this.place) return
        when (f.type) {
            FixtureType.GR_CHANDELIER -> {
                if (abs(f.angle) > 0.002f || abs(f.angleV) > 0.002f) {
                    f.angleV += (-f.angle * 16f - f.angleV * 0.9f) * dt
                    f.angle += f.angleV * dt
                    live(f)
                } else {
                    f.angle = 0f
                    f.angleV = 0f
                }
            }
            FixtureType.GR_CLOCK -> {
                if (f.angleV > 0f) {
                    f.angleV = max(0f, f.angleV - dt)
                    live(f)
                }
            }
            FixtureType.GR_PORTRAIT, FixtureType.GR_LECTERN, FixtureType.GR_BUST, FixtureType.GR_DESK, FixtureType.GR_FOUNTAIN, FixtureType.GR_WINDOW -> {
                if (f.timer > 0f) {
                    f.timer = max(0f, f.timer - dt)
                    live(f)
                }
            }
            FixtureType.GR_ARMOUR -> stepArmour(f, dt)
            FixtureType.GR_GLOBE -> {
                if (f.angleV > 0f || f.timer > 0f) {
                    f.angle += f.angleV * dt
                    if (f.angle > TAU) f.angle -= TAU
                    f.angleV *= exp(-0.55f * dt)
                    if (f.angleV < 0.05f) f.angleV = 0f
                    f.timer = max(0f, f.timer - dt)
                    live(f)
                }
            }
            FixtureType.GR_AQUARIUM -> if (f.timer > 0f) {
                f.timer = max(0f, f.timer - dt)
                live(f)
            }
            FixtureType.SECRET_DOOR -> {
                if (f.timer > 0f) {
                    f.timer = max(0f, f.timer - dt)
                    live(f)
                }
            }
            FixtureType.GR_LADDER -> {
                val target = LADDER_STOPS[f.mode.coerceIn(0, LADDER_STOPS.size - 1)]
                val d = target - f.shiftX
                if (abs(d) > 0.002f) {
                    f.shiftX += d * min(1f, dt * 2.6f)
                    live(f)
                } else {
                    f.shiftX = target
                }
            }
            FixtureType.GR_DINING_TABLE -> if (f.timer > 0f) {
                f.timer = max(0f, f.timer - dt)
                live(f)
            }
            FixtureType.GR_CAKE -> stepCake(f, dt)
            FixtureType.GR_RANGE -> if (f.mode == 1) {
                live(f)
                f.timer -= dt
                if (f.timer <= 0f) bake(f)
            }
            FixtureType.GR_PIZZA_OVEN -> if (f.mode == 1) {
                live(f)
                f.timer -= dt
                if (f.timer <= 0f) finishPizza(f)
            }
            FixtureType.GR_MIXER -> if (f.on) {
                live(f)
                f.timer -= dt
                if (f.timer <= 0f) finishMix(f)
            }
            FixtureType.GR_PLANT -> if (f.timer > 0f) {
                f.timer = max(0f, f.timer - dt)
                live(f)
            }
            FixtureType.GR_SOFIE -> {
                if (f.timer > 0f) {
                    f.timer = max(0f, f.timer - dt)
                    live(f)
                }
                if (f.angleV > 0f) {
                    f.angleV -= dt
                    if (f.angleV <= 0f) {
                        f.angleV = 0f
                        sofieBurp(f)
                    }
                }
            }
            FixtureType.GR_HAMMOCK -> {
                f.angleV *= exp(-0.5f * dt)
                f.angle = sin(sim.time * 1.1f) * 0.05f + f.angleV * 0.1f
            }
            else -> Unit
        }
    }

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? {
        if (f.type != FixtureType.GR_HAMMOCK) return null
        val s = f.spec.spots.getOrNull(spot) ?: return null
        val sway = sin(sim.time * 1.1f)
        return floatArrayOf(f.x + f.shiftX + s.dx + sway * 0.012f, f.y + s.dy + sin(sim.time * 2.2f) * 0.003f)
    }

    override fun tick(place: PlaceId, dt: Float) {
        if (place != this.place) return
        if (!started) begin()
        // Dusk and dawn: the lamps (and the fire) follow the clock of the day.
        val night = world.night
        if (night != lastNight) {
            lastNight = night
            if (film <= 0f) {
                setLamps(night)
                rolf.lampsSwitched()
            }
        }
        if (film > 0f) {
            film -= dt
            if (film <= 0f) endFilm()
        }
        val target = if (film > 0f) FILM_DIM else 0f
        dim += (target - dim) * min(1f, dt * 2.2f)
        if (target == 0f && dim < 0.004f) dim = 0f
        GroundFloor.dim = dim
        cookClock += dt
        if (cookClock >= 0.25f) {
            cook(cookClock)
            cookClock = 0f
        }
        rolf.tick(dt)
    }

    /** The first moment of a visit: put the floor in the state its saved world says. */
    private fun begin() {
        started = true
        lastNight = world.night
        setLamps(world.night)
        fixture(GroundIx.TV)?.let { if (it.count == 1) it.count = 0 }
        fixture(GroundIx.CAKE)?.let { if (it.count == 0 && it.mode == 0) it.on = true }
        fixture(GroundIx.SECRET_SHELF)?.let { if ("manor_bookshelf" in world.flags) it.mode = 1 }
        fixture(GroundIx.LADDER)?.let { it.shiftX = LADDER_STOPS[it.mode.coerceIn(0, LADDER_STOPS.size - 1)] }
    }

    /** Roasting over the fire and frying on the hobs. */
    private fun cook(dt: Float) {
        val fire = fixture(GroundIx.FIREPLACE)
        if (fire != null && fire.on) cookOn(fire, -0.065f, dt, stove = false)
        val range = fixture(GroundIx.RANGE)
        if (range != null && range.on) cookOn(range, -0.3f, dt, stove = true)
    }

    private fun cookOn(f: Fixture, dy: Float, dt: Float, stove: Boolean) {
        for (b in world.bodies.values) {
            val t = b as? Thing ?: continue
            if (t.place != place || t.mode != Mode.FREE || !t.resting || t.held) continue
            if (abs(t.x - (f.x + f.shiftX)) > f.spec.w / 2f || abs(t.y - (f.y + dy)) > 0.006f) continue
            t.cook += dt
            if (t.cook < 2.2f) continue
            val to = (if (stove) Recipes.stove(t.type) else Recipes.fire(t.type)) ?: continue
            val from = t.type
            t.type = to
            t.variant = 0
            t.used = 0
            t.cook = 0f
            t.squashV += 6f
            listener.onFx(Fx.COOKED, t.x, t.y - t.h, f, t)
            Recipes.keyFor(if (stove) FixtureType.STOVE else FixtureType.CAMPFIRE, listOf(from))?.let(sim::discover)
        }
    }

    companion object {
        private const val TAU = 6.2831855f
        const val FILM_SECONDS = 75f
        const val FILM_DIM = 0.62f
        const val TABLE_SECONDS = 2.6f
        const val SONG_SECONDS = 8.2f

        /** Where the red book sits on the bookcase, from its bottom centre. */
        const val RED_BOOK_DX = 0.04f

        /** The stops of the library ladder, as how far it is from where it stands in the blueprint. */
        val LADDER_STOPS = floatArrayOf(-0.30f, 0f, 0.26f)

        /** The hats on the coat rack, in the order they fall: the thing and its variant. */
        val COAT_HATS = listOf(
            ThingType.CAP to 0, ThingType.PARTY_HAT to 1, ThingType.WIZARD_HAT to 0, ThingType.BEANIE to 2, ThingType.SUN_HAT to 0,
            ThingType.NISSE_HAT to 0, ThingType.CHEF_HAT to 0, ThingType.VIKING_HELMET to 0, ThingType.CAP to 2, ThingType.BOW to 1,
        )

        /** What falls out of the stockings. */
        val TREATS = listOf(ThingType.LOLLIPOP to 0, ThingType.COOKIE to 0, ThingType.COIN to 0, ThingType.LOLLIPOP to 2, ThingType.GEM to 1, ThingType.CUPCAKE to 2)
    }
}
