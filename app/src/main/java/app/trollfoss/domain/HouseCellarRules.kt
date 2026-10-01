package app.trollfoss.domain

import kotlin.random.Random

/**
 * The codes of the cellar's effects ([Fx.HOUSE] events with codes from [HouseFx.CELLAR] up). The rules send
 * them; `ui/play/HouseCellarFx.kt` turns them into sound and sparkle. The small number that goes with a
 * code is described next to it.
 */
object CellarCode {
    private const val B = HouseFx.CELLAR

    /** A lamp or a neon sign was switched; arg 1 is on. */
    const val SWITCH = B + 1

    /** Charging begins at the station; arg 1 when it is a robot. */
    const val CHARGE = B + 2
    const val CHARGED = B + 3
    const val MOUSE_PEEK = B + 4

    /** The mouse got cheese; arg 1 when it thanks you with a coin. */
    const val MOUSE_CHEESE = B + 5
    const val SAW_START = B + 6
    const val SAW_DONE = B + 7

    /** Washer (arg 0) or dryer (arg 1) starts. */
    const val WASH = B + 8

    /** The cycle is over; arg is how many things came out. */
    const val WASH_DONE = B + 9
    const val SOCK_EAT = B + 10

    /** The monster burps out a mismatched pair. */
    const val SOCK_BURP = B + 11

    /** The monster coughs up the golden key. */
    const val SOCK_KEY = B + 12

    /** The monster does not like what it was given. */
    const val SOCK_BLEH = B + 13

    /** The monster was tapped; arg is how many taps in a row. */
    const val SOCK_PLAY = B + 14
    const val BASKET_POP = B + 15

    /** The clothes line swings; arg 1 when a sock fell off. */
    const val LINE_SWING = B + 16
    const val IRON = B + 17

    /** Someone came down the chute. */
    const val CHUTE = B + 18

    /** The dryer ate a sock. */
    const val DRYER_LOST = B + 19

    /** A valve turned; arg is the valve (0 to 2) times two, plus one when it is open now. */
    const val VALVE = B + 20
    const val VALVES_ALL = B + 21
    const val RELEASE = B + 22
    const val BOILER_TAP = B + 23

    /** A dive from the springboard; arg is how many dived. */
    const val DIVE = B + 24
    const val SLIDE_GO = B + 25
    const val SPLASH_TAP = B + 26
    const val FLOAT_PUSH = B + 27

    /** The shower; arg 1 when it comes on. */
    const val SHOWER = B + 28
    const val LADLE = B + 29
    const val LIFEBUOY = B + 30

    /** A tune begins; arg is the tune (1 to 5). */
    const val JUKE_START = B + 31
    const val JUKE_STOP = B + 32

    /** One beat of the tune: arg is the tune (0 to 4) times 64 plus the step (see [CellarTunes]). */
    const val JUKE_NOTE = B + 33
    const val FLOOR_TAP = B + 34

    /** The karaoke screen; arg 1 when it comes on. */
    const val KARAOKE = B + 35

    /** The snack bar hands out something; arg 0 popcorn, 1 soda, 2 cupcake. */
    const val BAR_POP = B + 36
    const val CONFETTI = B + 37
    const val RIDE_BELL = B + 38
    const val RIDE_START = B + 39

    /** A rail joint; arg 0 to 3 is how fast the cart runs. */
    const val RIDE_CLACK = B + 40
    const val RIDE_ENTER = B + 41

    /** A knock on the locked tunnel door; arg is how many in a row. */
    const val KNOCK = B + 42
    const val RIDE_BACK = B + 43
}

/** Tools the parts of the cellar's rules share: the clock, effects, spawning and little timers. */
internal class CellarCtx(val sim: Sim, val random: Random) {
    val place = PlaceId.MANOR_CELLAR
    val world: World get() = sim.world
    val listener: SimListener get() = sim.listener

    /** Seconds the cellar has been stepped (it stands still while the child is elsewhere). */
    var clock = 0f
        private set

    private class Later(val at: Float, val block: () -> Unit)

    private val later = ArrayList<Later>()
    private var lastTick = System.nanoTime()

    /** Runs [block] after [seconds] of cellar time. */
    fun after(seconds: Float, block: () -> Unit) {
        later += Later(clock + seconds, block)
    }

    /** Advances the clock and runs what is due. Returns how many seconds passed in real time since the last call. */
    fun run(dt: Float): Float {
        val now = System.nanoTime()
        val gap = (now - lastTick) / 1_000_000_000f
        lastTick = now
        clock += dt
        var i = 0
        while (i < later.size) {
            val l = later[i]
            if (clock >= l.at) {
                later.removeAt(i)
                l.block()
            } else {
                i++
            }
        }
        return gap
    }

    /** Forgets what was about to happen (the child was away). */
    fun forgetLater() = later.clear()

    fun fx(code: Int, x: Float, y: Float, f: Fixture? = null, t: Thing? = null, arg: Int = 0) =
        listener.onFx(Fx.HOUSE, x, y, f, t, HouseFx.pack(code, arg))

    fun fixture(index: Int): Fixture? = world.fixtures[place.idBase + index]

    /** The blueprint index of [f] in the cellar. */
    fun indexOf(f: Fixture): Int = place.indexOf(f.id)

    /** New things from a machine land in front of it, not hidden behind it. */
    fun inFront(b: Body, f: Fixture) {
        b.ground = if (f.spec.wall) 0.9f else f.depth + 0.02f
    }

    /** Makes a thing at ([x], [y]) that flies out of [from]; tells the listener so it pops in with sparkles. */
    fun spawn(type: ThingType, variant: Int, x: Float, y: Float, from: Fixture, vx: Float, vy: Float, vrot: Float = 0f): Thing {
        val t = world.addThing(type, variant, place, x, y)
        inFront(t, from)
        t.vx = vx
        t.vy = vy
        t.vrot = vrot
        listener.onSpawn(t)
        return t
    }

    /** At most [max] loose things of [type] stay in the cellar: the oldest goes in a puff. */
    fun limit(type: ThingType, max: Int) {
        val same = world.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == type && it.mode == Mode.FREE && it.inside < 0 && !it.held }
        if (same.size <= max) return
        val oldest = same.minByOrNull { it.z } ?: return
        listener.onFx(Fx.POOF, oldest.x, oldest.y - oldest.h / 2, thing = oldest)
        sim.removeThing(oldest)
    }

    /** Folk and other figures of the cellar. */
    fun persons(): List<Person> = world.bodiesIn(place).filterIsInstance<Person>()

    /** The nearest figure within [reach] of [x] that is free to be picked for something. */
    fun freeFigures(x: Float, reach: Float): List<Person> = persons()
        .filter { it.mode == Mode.FREE && !it.held && it.floatTime <= 0f && kotlin.math.abs(it.x - x) <= reach }
        .sortedBy { kotlin.math.abs(it.x - x) }
}

/** One part of the cellar's rules (a room's worth of furniture). */
internal interface CellarPart {
    fun tap(f: Fixture, dx: Float, dy: Float): Boolean = false
    fun step(f: Fixture, dt: Float) {}
    fun tick(dt: Float) {}
    fun drop(f: Fixture, t: Thing): Boolean = false
    fun seatPoint(f: Fixture, spot: Int): FloatArray? = null
}

/**
 * The cellar's rules: the washer and the dryer spin, the sock monster eats socks (and keeps the golden key),
 * the three boiler valves rumble the house, the pool has a springboard, a slide and a rubber duck, the jukebox
 * plays five tunes and lights up the dance floor, and the mine cart rides to Trollhola once all five keys
 * are found. Each room's rules are in their own file.
 */
class HouseCellarRules(sim: Sim, random: Random) : FloorRules {
    private val c = CellarCtx(sim, random)
    private val work = CellarWork(c)
    private val laundry = CellarLaundry(c)
    private val boiler = CellarBoiler(c)
    private val pool = CellarPool(c)
    private val party = CellarParty(c)
    private val tunnel = CellarTunnel(c)

    private fun partOf(type: FixtureType): CellarPart? = when (type) {
        FixtureType.CE_CHARGER, FixtureType.CE_MOUSE_HOLE, FixtureType.CE_SAW -> work
        FixtureType.CE_WASHER, FixtureType.CE_DRYER, FixtureType.CE_SOCK_MONSTER, FixtureType.CE_BASKET,
        FixtureType.CE_CLOTHESLINE, FixtureType.CE_IRON_BOARD, FixtureType.CE_CHUTE -> laundry
        FixtureType.CE_BOILER, FixtureType.CE_VALVE -> boiler
        FixtureType.CE_DIVING_BOARD, FixtureType.CE_POOL_SLIDE, FixtureType.CE_POOL_FLOAT, FixtureType.CE_POOL_WATER,
        FixtureType.CE_SAUNA_BUCKET, FixtureType.CE_SHOWER, FixtureType.CE_LIFEBUOY -> pool
        FixtureType.CE_DANCE_FLOOR, FixtureType.CE_JUKEBOX, FixtureType.CE_KARAOKE, FixtureType.CE_SNACK_BAR,
        FixtureType.CE_NEON, FixtureType.CE_CONFETTI, FixtureType.DISCO_BALL, FixtureType.MIC_STAND -> party
        FixtureType.SECRET_DOOR, FixtureType.CE_MINE_CART -> tunnel
        else -> null
    }

    override fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean {
        // Trollhola's end of the tunnel: the usual passage does the work, we only add the sound of the cart.
        if (place == PlaceId.LAB) {
            if (f.type == FixtureType.SECRET_DOOR) c.fx(CellarCode.RIDE_BACK, f.x, f.y - 0.2f, f)
            return false
        }
        if (f.type == FixtureType.CE_BULB) {
            f.mode = 1 - f.mode
            f.on = f.mode == 0
            c.fx(CellarCode.SWITCH, f.x, f.y - 0.07f, f, arg = if (f.on) 1 else 0)
            return true
        }
        return partOf(f.type)?.tap(f, dx, dy) ?: false
    }

    override fun step(place: PlaceId, f: Fixture, dt: Float) {
        when (f.type) {
            // The stairs are lit from the hall above; the bulbs, the neon and the bar glow until they are switched off.
            FixtureType.STAIRCASE, FixtureType.CE_SNACK_BAR -> f.on = true
            FixtureType.CE_BULB -> f.on = f.mode == 0
            else -> partOf(f.type)?.step(f, dt)
        }
    }

    override fun tick(place: PlaceId, dt: Float) {
        val away = c.run(dt)
        if (away > AWAY_SECONDS) {
            c.forgetLater()
            tunnel.cameBack()
        }
        laundry.tick(dt)
        party.tick(dt)
    }

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? = partOf(f.type)?.seatPoint(f, spot)

    override fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean = partOf(f.type)?.drop(f, t) ?: false

    private companion object {
        /** If the cellar was not stepped for this long, the child was away and unfinished fun is dropped. */
        const val AWAY_SECONDS = 1.5f
    }
}
