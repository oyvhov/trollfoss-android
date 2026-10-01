package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** A flat top in scene coordinates. [owner] is the fixture id, or -1 for the ground. */
/**
 * A flat top in scene coordinates. [owner] is the fixture id, or -1 for the ground. A [band] surface is
 * the floor band of «skrå-3D»: each body meets it at its own depth line instead of at [y].
 */
class Surface(val x1: Float, val x2: Float, val y: Float, val owner: Int, val interior: Boolean, val bounce: Float, val slippery: Boolean = false, val band: Boolean = false)

/** Water in scene coordinates: the sea, a pond, a bath or a fountain basin. */
class Pool(val x1: Float, val x2: Float, val line: Float, val bottom: Float, val owner: Int)

/** Things that happen in the world, for the engine to turn into sound, sparkle and speech. */
enum class Fx {
    OPEN, CLOSE, ON, OFF, CHANNEL, DISPENSE, EMPTY, COOKED, BAKE, DING, BLEND, BLENDED, INTO, STIR, BREWED,
    CAST, CATCH, FLUSH, WISH, KEY, TICK, CUCKOO, TOOT, BUILD, CRUMBLE, OWL, SHAKE, PUSH, SLIDE, SPARKLE,
    PAGE, LOOK, REGISTER, PUMP, DRY, WATER, HATCH, GIFT, POOF, SPIN, BOING, SQUEAK, STRUM, DRUM, RING,
    VROOM, FIREWORK, HOP, CURTAIN, WHEE, CRACK, BEEP, RUMBLE, LAUNCH, GROW, HARVEST, HAMMER, GRAVITY,

    // Slapstick; param is the figure it happened to.
    PRRT, SLIP, ATSJO, PEPPER, BONK, FLUFF, SPLAT, BURP, HICCUP, GOBBLE,

    // Tivoli, shop, stage and sea.
    BUMP, SCAN, KNOCK, XYLO, SING, BOOM, DISCO, FOG, BUBBLES, INK,

    // Home designer and tidying.
    PLACE, STORE, PAINT, TIDY, HOME, TRASH, SUCK,

    // Figures acting on their own: settling in a seat or bed, getting up again.
    SETTLE, WAKE,

    // Heileberget: the cable car, the echo, the eagle and the summit.
    CABLE, ARRIVE, ECHO, SCREECH, SUMMIT,

    // Easter eggs.
    QUAKE, KING, DUCK, STARRAIN, JIG,

    // Storhuset: one event for the whole house; the code is in the param (see [HouseFx]).
    HOUSE,

    // Rolf and Sture: a bow, a dusty sneeze; the code and the figure are in the param (see [FigurarEvent]).
    FIGURAR,
}

interface SimListener {
    fun onLand(body: Body, speed: Float) {}
    fun onBounce(body: Body, speed: Float) {}
    fun onSplash(body: Body, speed: Float) {}
    fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture? = null, thing: Thing? = null, param: Int = 0) {}
    fun onSpawn(body: Body) {}
    fun onRemove(body: Body) {}
    fun onSecret(id: String) {}
    fun onDiscovery(key: String) {}
    fun onWish(person: Person, event: WishEvent) {}

    /** An Easter egg was found for the first time. */
    fun onEgg(id: String) {}

    /** Someone used a passage of the big house; the view follows to the arrival at [arrivalX]. */
    fun onPassage(passage: Passage, arrivalX: Float, riders: Int) {}
}

/** What happened when a thing was given to a figure. */
enum class Give { ATE, FINISHED, DRANK, POTION, WORE, HELD, HAIR, DRESSED, SNEEZE, SNIFF, NONE }

/**
 * The rules of the island: gravity, water, cupboards, seats, machines and what figures do with
 * things. Pure Kotlin, so every rule can be tested without a device. The engine calls [step] every
 * frame for the place on screen and turns [SimListener] events into sound and sparkle.
 */
class Sim(val world: World, listener: SimListener = object : SimListener {}, private val random: Random = Random.Default) {
    var time = 0f

    /** The task board; it hears everything that happens. */
    val tasks = TaskBook(world)

    /** The place being stepped, tapped or played in right now, for the task board. */
    var here: PlaceId = world.place

    /** Where events go: the engine, with the task board listening in. */
    var listener: SimListener = Relay(listener)
        set(value) {
            field = if (value is Relay) value else Relay(value)
        }

    /** Passes every event on, and tells the task board what it means. */
    private inner class Relay(private val inner: SimListener) : SimListener by inner {
        override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
            heard(fx, fixture, thing, param)
            inner.onFx(fx, x, y, fixture, thing, param)
        }

        override fun onWish(person: Person, event: WishEvent) {
            if (event == WishEvent.GRANTED) tasks.record(Deed.WISH, person.place ?: here)
            inner.onWish(person, event)
        }

        override fun onSpawn(body: Body) {
            if (body is Thing) tasks.record(Deed.MADE, here, body.type)
            inner.onSpawn(body)
        }
    }

    private fun heard(fx: Fx, fixture: Fixture?, thing: Thing?, param: Int) {
        val deed = when (fx) {
            Fx.PRRT -> if (param >= 0) Deed.PRRT else null
            Fx.ATSJO -> Deed.SNEEZE
            Fx.SLIP -> Deed.SLIP
            Fx.SPLAT -> Deed.SPLAT
            Fx.BURP -> Deed.BURP
            Fx.CATCH -> Deed.CATCH
            Fx.BREWED -> Deed.BREW
            Fx.WHEE -> Deed.WHEE
            Fx.SCAN -> Deed.SCAN
            Fx.SING -> Deed.SING
            Fx.DISCO -> if (param == 1) Deed.DISCO else null
            Fx.INK -> Deed.INK
            Fx.LAUNCH -> Deed.LAUNCH
            Fx.GRAVITY -> Deed.GRAVITY
            Fx.HARVEST -> Deed.HARVEST
            Fx.HAMMER -> Deed.BUILD
            Fx.VROOM -> if (param == 2) Deed.VROOM else null
            Fx.HATCH -> Deed.HATCH
            Fx.KNOCK -> Deed.KNOCK
            Fx.BUILD -> if (fixture?.type == FixtureType.SNOWMAN) Deed.SNOWMAN else null
            Fx.GIFT -> if (param == 1) Deed.GIFT else null
            Fx.TIDY -> Deed.TIDY
            Fx.PAINT -> Deed.PAINT
            Fx.PLACE -> Deed.FURNISH
            Fx.COOKED -> if (thing != null) Deed.MADE else null
            else -> null
        } ?: return
        tasks.record(deed, here, thing?.type, fixture?.type)
    }

    /** Today's date as an epoch day, set by the app so the daily gift follows the device clock. */
    var today: Long = 0L

    private val surfaceCache = HashMap<PlaceId, List<Surface>>()

    /** Wishes and wandering animals. */
    val life = Life(this, random)

    // ------------------------------------------------------------------ Easter eggs

    /** Notes the child has played on the piano lately, to spot a hidden tune. */
    private val pianoTrail = ArrayList<Int>()
    private var pianoLast = -99f

    /** Finds an Easter egg: a sticker the first time, and the listener hears of it. Returns true when new. */
    fun egg(id: String): Boolean {
        if (!world.eggs.add(id)) return false
        world.stickers += world.stickers.size
        listener.onEgg(id)
        return true
    }

    /** Shake the device and the world shakes: everything loose jumps, figures yelp, trees rattle. */
    fun quake(place: PlaceId) {
        for (b in world.bodiesIn(place).toList()) {
            if (b.held || b.mode != Mode.FREE || b.inside >= 0) continue
            b.resting = false
            b.restOwner = -2
            b.vy = -(0.7f + random.nextFloat() * 1.3f)
            b.vx += (random.nextFloat() - 0.5f) * 1.2f
            if (b is Thing) b.vrot = (random.nextFloat() - 0.5f) * 500f
            if (b is Person) b.squashV -= 4f
        }
        for (f in world.fixturesIn(place)) {
            f.anim = 1f
            if (f.type == FixtureType.PINE_TREE || f.type == FixtureType.UMBRELLA) f.angleV += 2f
        }
        listener.onFx(Fx.QUAKE, 0f, 0f)
        egg("quake")
    }

    /** A duck in a running bath gets its bubble party after a moment. */
    private fun duckParty(place: PlaceId, f: Fixture, dt: Float) {
        val duck = if (f.on) world.bodiesIn(place).firstOrNull { b ->
            b is Thing && b.type == ThingType.DUCK && b.mode == Mode.FREE && !b.held && poolAt(b.x, b.y)?.owner == f.id
        } else null
        if (duck == null) {
            f.timer = 0f
            return
        }
        if (f.timer < 0f) return
        f.timer += dt
        if (f.timer >= 4f) {
            f.timer = -1f
            listener.onFx(Fx.DUCK, duck.x, duck.y, f, duck as? Thing)
            egg("duck")
        }
    }

    /** Whoopee cushions, banana peels, sneezes and bonks. */
    val jokes = Jokes(this, random)

    /** Rides, games, instruments and machines of the newer places. */
    val attractions = Attractions(this, random)

    /** The rules of Storhuset: its floors, and the passages between them. */
    val house = HouseRules(this, random)

    /** Rolf the robot butler and Sture the ghost: greetings, dusty sneezes and what they do with food. */
    val figurar = Figurar(this, random)

    /** Furniture catalogue, store, wallpaper and tidying up. */
    val designer = Designer(this, random)

    /** Mitt hus: building, the housewarming and what the furniture of the child's own house does. */
    val mine = MineBuilder(this, random)
    private var pools: List<Pool> = emptyList()

    fun invalidate(place: PlaceId) {
        surfaceCache.remove(place)
    }

    fun surfaces(place: PlaceId): List<Surface> = surfaceCache.getOrPut(place) { buildSurfaces(place, allInterior = false) }

    private fun buildSurfaces(place: PlaceId, allInterior: Boolean): List<Surface> {
        val list = ArrayList<Surface>()
        Places.spec(place).grounds.forEach { list += Surface(it.x1, it.x2, it.y, -1, false, 0f, band = it.y == place.floor) }
        for (f in world.fixturesIn(place)) {
            for (s in f.spec.surfaces) {
                val active = when {
                    s.interior -> allInterior || f.open
                    s.closedOnly -> !f.open
                    else -> true
                }
                // Shifted with the fixture, so a rolling trolley takes its basket along.
                if (active) list += Surface(f.x + f.shiftX + s.x1, f.x + f.shiftX + s.x2, f.y + f.shiftY + s.dy, f.id, s.interior, s.bounce, s.slippery)
            }
        }
        return list
    }

    fun pools(place: PlaceId): List<Pool> {
        val list = ArrayList<Pool>()
        Places.spec(place).water?.let { list += Pool(it.x1, it.x2, it.line, it.bottom, -1) }
        for (f in world.fixturesIn(place)) {
            val pool = f.spec.pool ?: continue
            if (f.type == FixtureType.BATH && !f.on) continue
            list += Pool(f.x + pool.left, f.x + pool.right, f.y + pool.top, f.y + pool.bottom, f.id)
        }
        return list
    }

    /**
     * The pool that covers a point just below its waterline. A body must be strictly below the line: one a hair
     * above it still falls through the line, and that crossing is what makes the splash.
     */
    fun poolAt(x: Float, y: Float): Pool? = pools.firstOrNull { x in it.x1..it.x2 && y > it.line && y <= it.bottom + 0.02f }

    private fun below(list: List<Surface>, x: Float, y: Float): Surface? =
        list.filter { x >= it.x1 && x <= it.x2 && it.y >= y - 0.001f }.minByOrNull { it.y }

    /** Where a body stands on the floor band: its own depth line, kept between the back wall and the front. */
    fun groundOf(place: PlaceId, b: Body): Float {
        if (b.ground.isNaN()) b.ground = if (b.y in place.back..PlaceId.FRONT) b.y else place.floor
        return b.ground.coerceIn(place.back, PlaceId.FRONT)
    }

    /** The height at which [b] would rest on [s]. */
    fun restY(place: PlaceId, s: Surface, b: Body): Float = if (s.band) groundOf(place, b) else s.y

    /**
     * The surface a body falling from [from] to [to] meets first. Furniture tops win over the floor
     * band, so a cup dropped over a table lands on it even when its depth line lies higher up.
     */
    private fun landing(place: PlaceId, list: List<Surface>, b: Body, from: Float, to: Float): Surface? {
        list.filter { !it.band && b.x >= it.x1 && b.x <= it.x2 && it.y >= from - 0.003f && it.y <= to }.minByOrNull { it.y }?.let { return it }
        val band = list.firstOrNull { it.band && b.x >= it.x1 && b.x <= it.x2 } ?: return null
        val g = groundOf(place, b)
        return if (g >= from - 0.003f && g <= to) band else null
    }

    fun floats(body: Body): Boolean = when (body) {
        is Thing -> body.type.buoyant
        is Person -> true
    }

    fun floatLine(body: Body, pool: Pool): Float = when (body) {
        is Thing -> pool.line + body.h * 0.35f
        is Person -> pool.line + body.h * (if (body.species == Species.FOLK) 0.55f else 0.45f)
    }

    /** On the sea floor everything swims: slow, soft and a little floaty. */
    fun underwater(place: PlaceId): Boolean = place == PlaceId.UNDERWATER

    /** The space station floats in zero gravity until someone pulls the gravity lever. */
    fun zeroG(place: PlaceId): Boolean =
        place == PlaceId.SPACE && world.fixturesIn(place).firstOrNull { it.type == FixtureType.GRAVITY_LEVER }?.on != true

    /** Puts every body in [place] where it belongs: on the surface below it, in its seat or afloat. */
    fun settle(place: PlaceId) {
        val all = buildSurfaces(place, allInterior = true)
        pools = pools(place)
        val floating = zeroG(place)
        for (f in world.fixturesIn(place)) {
            if (f.type == FixtureType.TRACTOR) f.shiftX = f.mode * TRACTOR_DRIVE
        }
        for (b in world.bodiesIn(place)) {
            when (b.mode) {
                Mode.SEATED -> (b as? Person)?.let { placeSeated(it) }
                Mode.FREE -> {
                    if (floating && b.inside < 0) {
                        b.resting = false
                        continue
                    }
                    // Under water, figures and light things stay where they swim.
                    if (underwater(place) && b.inside < 0 && (b is Person || (b is Thing && (b.type.buoyant || b.type.lift < 0f)))) {
                        b.resting = false
                        continue
                    }
                    if (b is Thing && b.type.lift < 0f) {
                        b.y = place.ceiling + b.h
                        continue
                    }
                    val s = landing(place, all, b, b.y - 0.002f, 2f)
                    val pool = pools.firstOrNull { b.x in it.x1..it.x2 }
                    if (pool != null && (s == null || restY(place, s, b) > pool.line) && floats(b)) {
                        b.y = floatLine(b, pool)
                        b.resting = false
                    } else if (s != null) {
                        b.y = restY(place, s, b)
                        b.resting = true
                        b.restOwner = s.owner
                        b.inside = if (s.interior) s.owner else -1
                    }
                    b.vx = 0f
                    b.vy = 0f
                }
                else -> Unit
            }
            if (b is Person) updatePose(b)
        }
    }

    // ------------------------------------------------------------------ step

    fun step(place: PlaceId, dt: Float) {
        here = place
        time += dt
        if (place.big) house.tick(place, dt)
        pools = pools(place)
        floating = zeroG(place)
        val fixtures = world.fixturesIn(place)
        for (f in fixtures) stepFixture(place, f, dt)
        // Read after the machines: an oven that just opened has shelves again.
        val list = surfaces(place)
        for (b in world.bodiesIn(place)) {
            b.age += dt
            if (b.cool > 0f) b.cool = max(0f, b.cool - dt)
            if (b.flyT >= 0f) {
                designer.stepFlight(b, dt)
                continue
            }
            b.squashV += (-b.squash * 260f - b.squashV * 15f) * dt
            b.squash += b.squashV * dt
            when (b.mode) {
                Mode.FREE -> if (!b.held) stepFree(place, b, list, dt)
                Mode.SEATED -> (b as? Person)?.let { placeSeated(it) }
                else -> Unit
            }
            if (b is Person) stepPerson(b, dt)
            if (b is Thing && b.cook < 0f) stepRocket(b, dt)
        }
        life.step(place, dt)
        if (place == PlaceId.FOREST && world.night) unlock("forest_night")
    }

    private fun stepFree(place: PlaceId, b: Body, list: List<Surface>, dt: Float) {
        if (b.inside >= 0) {
            val box = world.fixtures[b.inside]
            if (box != null && !box.open) return
        }
        if (floating) {
            stepFloating(place, b, list, dt)
            return
        }
        if (underwater(place)) {
            stepSwimming(place, b, list, dt)
            return
        }
        val lift = liftOf(b)
        if (b.resting) {
            val support = if (lift < 0f) null else list.firstOrNull { abs(restY(place, it, b) - b.y) < 0.004f && b.x >= it.x1 - 0.003f && b.x <= it.x2 + 0.003f }
            if (support != null) {
                val rolls = b is Thing && b.type.rolls
                if (abs(b.vx) > 0.002f) {
                    b.x += b.vx * dt
                    val friction = when {
                        support.slippery -> 0.35f
                        rolls -> 1.0f
                        else -> 9f
                    }
                    b.vx *= exp(-friction * dt)
                    if (rolls) b.rot += b.vx * dt / max(0.01f, b.h * 0.5f) * 57.3f
                    wall(place, b)
                    if (b.x < support.x1 - 0.003f || b.x > support.x2 + 0.003f) {
                        leaveSupport(b)
                    }
                } else {
                    b.vx = 0f
                }
                if (!rolls) b.rot *= exp(-12f * dt)
                if (b.resting) return
            } else {
                leaveSupport(b)
            }
        }

        val pool = poolAt(b.x, b.y)
        if (pool != null && floats(b) && lift >= 0f) {
            val target = floatLine(b, pool) + sin(time * 2.1f + b.id) * 0.004f
            b.vy += ((target - b.y) * 42f - b.vy * 7f) * dt
            b.vx *= exp(-1.6f * dt)
            b.vrot *= exp(-3f * dt)
            b.rot += b.vrot * dt
            b.rot *= exp(-1.2f * dt)
            b.x += b.vx * dt
            b.y = min(b.y + b.vy * dt, pool.bottom)
            wall(place, b)
            return
        }

        var g = GRAVITY * lift
        if (pool != null) g *= 0.3f
        b.vy += g * dt
        if (pool != null) {
            b.vy = min(b.vy, 0.35f)
            b.vx *= exp(-2f * dt)
        }
        b.vx *= exp(-0.2f * dt)
        b.vrot *= exp(-0.8f * dt)
        b.rot += b.vrot * dt
        if (b is Thing && jokes.flying(place, b)) return
        if (b is Thing) attractions.flying(place, b)
        val oldY = b.y
        var newY = b.y + b.vy * dt
        b.x += b.vx * dt
        wall(place, b)

        for (p in pools) {
            if (b.x in p.x1..p.x2 && oldY <= p.line && newY > p.line && b.vy > 0.25f) {
                listener.onSplash(b, b.vy)
                if (floats(b)) b.vy *= 0.35f
            }
        }

        if (b.vy > 0f) {
            val s = landing(place, list, b, oldY, newY)
            if (s != null) {
                land(place, b, s)
                return
            }
        }
        if (newY - b.h < place.ceiling) {
            newY = place.ceiling + b.h
            b.vy = if (lift < 0f) 0f else abs(b.vy) * 0.3f
        }
        if (newY > 1.4f) {
            newY = place.floor
            b.vy = 0f
            b.resting = true
        }
        b.y = newY
    }

    /**
     * Zero gravity: nothing falls. Bodies drift, slow down a little, bounce softly off floor, ceiling,
     * walls and furniture tops, and get a tiny nudge now and then so the station never looks frozen.
     */
    private fun stepFloating(place: PlaceId, b: Body, list: List<Surface>, dt: Float) {
        b.resting = false
        b.vx *= exp(-0.35f * dt)
        b.vy *= exp(-0.35f * dt)
        b.vrot *= exp(-0.3f * dt)
        if (abs(b.vx) + abs(b.vy) < 0.03f && random.nextFloat() < dt * 0.6f) {
            b.vx += (random.nextFloat() - 0.5f) * 0.06f
            b.vy += (random.nextFloat() - 0.5f) * 0.06f
            b.vrot += (random.nextFloat() - 0.5f) * 30f
        }
        val oldY = b.y
        var newY = b.y + b.vy * dt
        b.x += b.vx * dt
        b.rot += b.vrot * dt
        wall(place, b)
        if (b.vy > 0f) {
            val s = landing(place, list, b, oldY, newY)
            if (s != null) {
                newY = restY(place, s, b) - 0.001f
                b.vy = -max(abs(b.vy) * 0.55f, 0.04f)
                if (abs(b.vy) > 0.3f) listener.onBounce(b, abs(b.vy))
            }
        }
        if (newY - b.h < place.ceiling) {
            newY = place.ceiling + b.h
            b.vy = abs(b.vy) * 0.55f + 0.02f
        }
        b.y = newY
    }

    private var floating = false

    /**
     * Under water: heavy things sink slowly, light ones rise to the surface, figures hover and drift
     * down so gently it looks like swimming. Everything is slowed by the water.
     */
    private fun stepSwimming(place: PlaceId, b: Body, list: List<Surface>, dt: Float) {
        val sink = when (b) {
            is Person -> 0.035f
            is Thing -> if (b.type.buoyant || b.type.lift < 0f) -0.3f else 0.45f
        }
        if (b.resting) {
            if (sink >= 0f) {
                if (abs(b.vx) > 0.002f) {
                    b.x += b.vx * dt
                    b.vx *= exp(-6f * dt)
                    wall(place, b)
                }
                return
            }
            leaveSupport(b)
        }
        val drag = exp(-2.4f * dt)
        b.vx *= drag
        b.vy *= drag
        b.vrot *= exp(-1.5f * dt)
        b.vy += sink * 2.5f * dt
        val oldY = b.y
        var newY = b.y + b.vy * dt
        b.x += b.vx * dt
        b.rot += b.vrot * dt
        wall(place, b)
        if (b.vy > 0f) {
            val s = landing(place, list, b, oldY, newY)
            if (s != null) {
                b.y = restY(place, s, b)
                b.vy = 0f
                b.resting = true
                b.restOwner = s.owner
                b.inside = if (s.interior) s.owner else -1
                listener.onLand(b, 0.2f)
                return
            }
        }
        if (newY - b.h < place.ceiling) {
            newY = place.ceiling + b.h
            b.vy = 0f
        }
        b.y = newY
    }

    /** Where [b] would come to rest if it fell from where it is now, or null in zero gravity or with nothing below. */
    fun previewRest(place: PlaceId, b: Body): Float? {
        if (zeroG(place)) return null
        val s = landing(place, surfaces(place), b, b.y, 2f) ?: return null
        return restY(place, s, b)
    }

    /** The piece of furniture straight below [b] with a top it could land on. */
    fun fixtureBelow(place: PlaceId, b: Body): Fixture? {
        val s = surfaces(place).filter { !it.band && b.x >= it.x1 && b.x <= it.x2 && it.y >= b.y - 0.001f }.minByOrNull { it.y } ?: return null
        return world.fixtures[s.owner]
    }

    /** New things from a machine land in front of it, not hidden behind it. */
    private fun inFront(t: Body, f: Fixture) {
        t.ground = (if (f.spec.wall) t.place?.back ?: f.depth else f.depth + 0.02f)
    }

    /** Something that rolls or is pulled off a piece of furniture falls to the floor in front of it. */
    private fun leaveSupport(b: Body) {
        world.fixtures[b.restOwner]?.let { f -> if (!f.spec.wall) b.ground = f.depth + 0.012f }
        b.resting = false
        b.inside = -1
        b.restOwner = -2
    }

    private fun land(place: PlaceId, b: Body, s: Surface) {
        val impact = b.vy
        b.y = restY(place, s, b)
        val e = max(bounceOf(b), s.bounce)
        when {
            s.bounce >= 1f -> {
                b.vy = -min(max(impact * 0.96f, 2.4f), 3.6f)
                b.vx *= 0.9f
                b.squashV += 9f
                listener.onBounce(b, impact)
                world.fixtures[s.owner]?.let { it.anim = 1f }
                attractions.bounced(place, b, world.fixtures[s.owner])
            }
            impact * e > 0.5f -> {
                b.vy = -impact * e
                b.vx *= 0.82f
                b.vrot *= -0.6f
                b.squashV += impact * 4f
                listener.onBounce(b, impact)
            }
            else -> {
                b.vy = 0f
                b.resting = true
                b.restOwner = s.owner
                b.inside = if (s.interior) s.owner else -1
                b.vrot = 0f
                if (!(b is Thing && b.type.rolls) && !s.slippery) b.vx *= 0.35f
                b.squashV += impact * 5f
                listener.onLand(b, impact)
                if (b is Person) jokes.landed(place, b)
            }
        }
    }

    private fun wall(place: PlaceId, b: Body) {
        val half = b.w * 0.5f
        if (b.x < half) {
            b.x = half
            b.vx = abs(b.vx) * 0.5f
        }
        if (b.x > place.width - half) {
            b.x = place.width - half
            b.vx = -abs(b.vx) * 0.5f
        }
    }

    fun liftOf(b: Body): Float = when (b) {
        is Thing -> if (b.cook < 0f) -2.2f else b.type.lift
        is Person -> when {
            b.floatTime > 0f -> -0.3f
            world.worn(b, Slot.HAND)?.type == ThingType.BALLOON -> 0.3f
            else -> 1f
        }
    }

    private fun bounceOf(b: Body): Float = when (b) {
        is Thing -> b.type.bounce
        is Person -> 0.22f
    }

    private fun stepPerson(p: Person, dt: Float) {
        jokes.step(p, dt)
        if (p.floatTime > 0f) {
            p.floatTime = max(0f, p.floatTime - dt)
            if (p.mode == Mode.FREE && !p.held) p.resting = false
        }
        updatePose(p)
    }

    fun updatePose(p: Person) {
        p.anim.pose = when {
            p.held -> Pose.HELD
            p.mode == Mode.SEATED -> world.fixtures[p.holder]?.spec?.spots?.getOrNull(p.slot)?.pose ?: Pose.SIT
            (p.floatTime > 0f || floating) && !p.resting && p.mode == Mode.FREE -> Pose.FLOAT
            p.place?.let { underwater(it) } == true && !p.resting && p.mode == Mode.FREE -> Pose.SWIM
            p.mode == Mode.FREE && !p.resting && poolAt(p.x, p.y) != null -> Pose.SWIM
            else -> Pose.STAND
        }
    }

    private fun stepRocket(t: Thing, dt: Float) {
        t.cook -= dt
        t.resting = false
        if (t.cook < -1.0f) {
            listener.onFx(Fx.FIREWORK, t.x, t.y - t.h, thing = t)
            removeThing(t)
        }
    }

    // ------------------------------------------------------------------ seats

    fun seatPoint(f: Fixture, spot: Int): FloatArray {
        if (f.place.big) house.seatPoint(f, spot)?.let { return it }
        attractions.seatPoint(f, spot)?.let { return it }
        val s = f.spec.spots[spot]
        var x = f.x + s.dx + f.shiftX
        var y = f.y + s.dy + f.shiftY
        when (f.type) {
            FixtureType.SALON_CHAIR -> y -= f.mode * 0.04f
            FixtureType.BOAT -> y += f.bob
            FixtureType.SLED_HILL, FixtureType.SKI_JUMP -> if (f.on) {
                val point = ridePoint(f.type, f.angle.coerceIn(0f, 1f))
                x = f.x + point[0]
                y = f.y + point[1]
            }
            else -> Unit
        }
        return floatArrayOf(x, y)
    }

    /**
     * Where a rider is on a sled hill or ski jump after [progress] (0 to 1) of the ride, relative to
     * the fixture's bottom centre. The ride speeds up as it goes, like sliding down a slope.
     */
    fun ridePoint(type: FixtureType, progress: Float): FloatArray {
        val p = progress * progress
        return if (type == FixtureType.SKI_JUMP) {
            // Down the in-run and up to the lip.
            val x = -0.23f + (0.21f + 0.23f) * p
            val dip = sin(p * PI_F) * 0.06f
            val y = -0.42f + (0.42f - 0.15f) * p + dip
            floatArrayOf(x, y)
        } else {
            val x = -0.19f + (0.24f + 0.19f) * p
            val drop = p * p * (3f - 2f * p)
            floatArrayOf(x, -0.32f + (0.32f - 0.03f) * drop)
        }
    }

    private fun placeSeated(p: Person) {
        val f = world.fixtures[p.holder]
        if (f == null || p.slot !in f.spec.spots.indices) {
            p.mode = Mode.FREE
            p.holder = -1
            p.resting = false
            return
        }
        val point = seatPoint(f, p.slot)
        p.x = point[0]
        p.y = point[1]
    }

    /** Seats [p] on [f] if the spot is free. */
    fun seat(p: Person, f: Fixture, spot: Int): Boolean {
        if (spot !in f.spec.spots.indices || world.seatedAt(f, spot) != null) return false
        p.mode = Mode.SEATED
        p.holder = f.id
        p.slot = spot
        p.resting = false
        p.vx = 0f
        p.vy = 0f
        p.z = world.nextZ()
        placeSeated(p)
        updatePose(p)
        if (f.type in RIDES) f.timer = 0f
        life.seated(p)
        p.place?.let { jokes.seated(it, p) }
        tasks.record(Deed.SEATED, p.place ?: here, fixture = f.type)
        return true
    }

    /** The nearest free seat within [reach] of a figure dropped at ([x], [y]) (its feet). */
    fun freeSeatNear(place: PlaceId, p: Person, x: Float, y: Float, reach: Float): Pair<Fixture, Int>? {
        var best: Pair<Fixture, Int>? = null
        var bestD = reach
        for (f in world.fixturesIn(place)) {
            f.spec.spots.forEachIndexed { index, spot ->
                if (world.seatedAt(f, index) != null) return@forEachIndexed
                if (spot.hidden && !f.open) return@forEachIndexed
                val point = seatPoint(f, index)
                // Compare against where the hips would be for a seat, the back for a bed.
                val hipY = y - p.h * Anatomy.HIPS
                val d = kotlin.math.hypot(point[0] - x, point[1] - hipY)
                if (d < bestD) {
                    bestD = d
                    best = f to index
                }
            }
        }
        return best
    }

    // ------------------------------------------------------------------ fixtures

    private fun stepFixture(place: PlaceId, f: Fixture, dt: Float) {
        f.anim = max(0f, f.anim - dt * 2.5f)
        if (time - f.tapTime > 3.5f) f.taps = 0
        if (place.big) house.step(place, f, dt)
        when (f.type) {
            FixtureType.BOAT -> f.bob = sin(time * 1.7f + f.id) * 0.007f
            FixtureType.PINE_TREE -> {
                f.angle += f.angleV * dt
                f.angleV += (-f.angle * 60f - f.angleV * 5f) * dt
            }
            FixtureType.SAUNA -> {
                // Steam rises while someone sits inside with the door shut.
                f.on = !f.open && f.spec.spots.indices.any { world.seatedAt(f, it) != null }
            }
            FixtureType.UMBRELLA -> {
                f.angle += f.angleV * dt
                f.angleV *= exp(-1.2f * dt)
            }
            FixtureType.OWL_TREE -> if (f.mode == 1) {
                f.timer -= dt
                if (f.timer <= 0f) f.mode = 0
            }
            FixtureType.TRACTOR -> {
                val target = f.mode * TRACTOR_DRIVE
                val step = 0.32f * dt
                f.on = abs(target - f.shiftX) > 0.001f
                f.shiftX = if (f.shiftX < target) min(target, f.shiftX + step) else max(target, f.shiftX - step)
                if (f.on) f.bob = sin(time * 30f) * 0.003f else f.bob = 0f
            }
            FixtureType.ROCKET_SHIP -> if (f.on) {
                f.timer += dt
                val t = f.timer
                f.shiftX = 0f
                f.shiftY = when {
                    t < 0.7f -> sin(t * 90f) * 0.004f
                    t < 2.0f -> -((t - 0.7f) / 1.3f).let { it * it } * 1.6f
                    t < 3.2f -> -1.6f
                    t < 4.8f -> -(1f - ((t - 3.2f) / 1.6f).let { 1f - (1f - it) * (1f - it) }) * 1.6f
                    else -> 0f
                }
                if (t >= 4.8f) {
                    f.on = false
                    f.timer = 0f
                    f.shiftY = 0f
                    listener.onFx(Fx.BOING, f.x, f.y, f)
                }
            }
            FixtureType.ORRERY -> {
                f.timer = max(0f, f.timer - dt)
                f.angle += dt * (if (f.timer > 0f) 3.2f else 0.35f)
            }
            FixtureType.VEGETABLE_PATCH -> if (f.mode in 1..2) {
                f.timer -= dt
                if (f.timer <= 0f) {
                    f.mode += 1
                    f.timer = GROW_SECONDS
                    listener.onFx(Fx.GROW, f.x, f.y - 0.05f, f, param = f.mode)
                }
            }
            FixtureType.SLED_HILL, FixtureType.SKI_JUMP -> {
                val rider = world.seatedAt(f, 0)
                if (rider == null) {
                    f.on = false
                    f.timer = 0f
                } else if (!f.on) {
                    f.timer += dt
                    if (f.timer > 0.5f) {
                        f.on = true
                        f.angle = 0f
                        listener.onFx(Fx.SLIDE, f.x, f.top, f)
                    }
                } else {
                    // Ride progress lives in angle; anim is only the tap squash.
                    val jump = f.type == FixtureType.SKI_JUMP
                    f.angle += dt / (if (jump) 1.0f else 0.9f)
                    if (f.angle >= 1f) {
                        f.angle = 1f
                        placeSeated(rider)
                        rider.mode = Mode.FREE
                        rider.holder = -1
                        rider.ground = f.depth + 0.02f
                        rider.y += rider.h * Anatomy.HIPS
                        rider.vx = if (jump) 2.3f else 1.5f
                        rider.vy = if (jump) -2.5f else -0.4f
                        rider.resting = false
                        rider.z = world.nextZ()
                        f.on = false
                        f.timer = 0f
                        f.angle = 0f
                        if (jump) {
                            f.count++
                            listener.onFx(Fx.WHEE, rider.x, rider.y - rider.h, f)
                            if (f.count >= 3) unlock("mountain_jump")
                        }
                    }
                }
                return
            }
            FixtureType.STOVE, FixtureType.CAMPFIRE, FixtureType.WOOD_STOVE -> if (f.on) cook(place, f, dt)
            FixtureType.OVEN -> if (f.on) {
                f.timer -= dt
                if (f.timer <= 0f) finishBake(place, f)
            }
            FixtureType.BLENDER -> if (f.on) {
                f.timer -= dt
                if (f.timer <= 0f) finishBlend(place, f)
            }
            FixtureType.CAULDRON -> {
                val contents = world.inMachine(f)
                if (!f.on && contents.size >= 2) {
                    f.on = true
                    f.timer = 1.1f
                }
                if (f.on) {
                    f.timer -= dt
                    if (f.timer <= 0f) brew(place, f)
                }
            }
            FixtureType.FISHING_SPOT -> if (f.on) {
                f.timer -= dt
                if (f.timer <= 0f) catchFish(place, f)
            }
            FixtureType.DRYER_HOOD -> if (f.on) {
                f.timer -= dt
                if (f.timer <= 0f) {
                    f.on = false
                    world.seatedAt(f, 0)?.let { p ->
                        p.look = p.look.copy(hair = if (p.look.hair == 3) 7 else 3)
                        p.anim.sparkle = 1f
                        listener.onFx(Fx.SPARKLE, p.x, p.y - p.h, f)
                        unlock("salon_dryer")
                    }
                }
            }
            FixtureType.ROBOT_VACUUM -> designer.stepVacuum(place, f, dt)
            FixtureType.BATH -> duckParty(place, f, dt)
            else -> attractions.step(place, f, dt)
        }
    }

    private fun thingsOn(place: PlaceId, f: Fixture): List<Thing> {
        val tops = f.spec.surfaces.map { f.y + it.dy }
        return world.bodiesIn(place).filterIsInstance<Thing>().filter { t ->
            t.mode == Mode.FREE && t.resting && !t.held && t.x in (f.x - f.spec.w / 2)..(f.x + f.spec.w / 2) && tops.any { abs(it - t.y) < 0.004f }
        }
    }

    private fun cook(place: PlaceId, f: Fixture, dt: Float) {
        for (t in thingsOn(place, f)) {
            t.cook += dt
            if (f.type == FixtureType.CAMPFIRE && t.type == ThingType.DRAGON_EGG) {
                if (t.cook > 3f) hatch(place, t)
                continue
            }
            if (t.cook < 2.2f) continue
            val from = t.type
            val to = (if (f.type == FixtureType.STOVE) Recipes.stove(from) else Recipes.fire(from)) ?: continue
            t.type = to
            t.variant = 0
            t.used = 0
            t.cook = 0f
            t.squashV += 6f
            listener.onFx(Fx.COOKED, t.x, t.y - t.h, f, t)
            Recipes.keyFor(f.type, listOf(from))?.let(::discover)
        }
    }

    private fun hatch(place: PlaceId, egg: Thing) {
        val x = egg.x
        val y = egg.y
        removeThing(egg)
        val dragon = world.addPerson(Species.DRAGON, Look(skin = random.nextInt(Palette.scales.size)), 1.3f, place, x, y)
        dragon.ground = egg.ground
        dragon.vy = -1.6f
        dragon.age = 0f
        listener.onSpawn(dragon)
        listener.onFx(Fx.HATCH, x, y - 0.05f)
        discover("fire_DRAGON_EGG")
    }

    private fun finishBake(place: PlaceId, f: Fixture) {
        f.on = false
        val inside = world.bodiesIn(place).filterIsInstance<Thing>().filter { it.inside == f.id && it.mode == Mode.FREE }
        val floorY = f.y + (f.spec.surfaces.firstOrNull { it.interior }?.dy ?: -0.035f)
        if (inside.isNotEmpty()) {
            val results = Recipes.oven(inside.map { it.type })
            inside.forEach { removeThing(it, quiet = true) }
            results.forEachIndexed { index, (made, key) ->
                val x = if (results.size == 1) f.x else f.x - 0.045f + index * 0.09f / (results.size - 1)
                val t = world.addThing(made.type, made.variant, place, x, floorY)
                t.inside = f.id
                t.resting = true
                listener.onSpawn(t)
                key?.let(::discover)
            }
            unlock("cafe_bake")
        }
        f.open = true
        invalidate(place)
        listener.onFx(Fx.DING, f.x, f.top, f)
    }

    private fun finishBlend(place: PlaceId, f: Fixture) {
        f.on = false
        val contents = world.inMachine(f)
        if (contents.isEmpty()) return
        val made = Recipes.blender(contents.map { it.type })
        Recipes.keyFor(FixtureType.BLENDER, contents.map { it.type })?.let(::discover)
        contents.forEach { removeThing(it, quiet = true) }
        val t = world.addThing(made.type, made.variant, place, f.x + 0.075f, f.y - 0.02f)
        inFront(t, f)
        t.vy = -1.2f
        t.vx = 0.35f
        listener.onSpawn(t)
        listener.onFx(Fx.BLENDED, f.x, f.top, f, t)
    }

    private fun brew(place: PlaceId, f: Fixture) {
        f.on = false
        val contents = world.inMachine(f).take(2)
        if (contents.size < 2) return
        val a = contents[0].type
        val b = contents[1].type
        contents.forEach { removeThing(it, quiet = true) }
        val key = Recipes.keyFor(FixtureType.CAULDRON, listOf(a, b))
        if (Recipes.cauldronMakesPet(a, b)) {
            val species = listOf(Species.CAT, Species.DOG, Species.BUNNY)[random.nextInt(3)]
            val pet = world.addPerson(species, Look(skin = random.nextInt(Palette.furs.size)), 1.2f, place, f.x, f.top)
            inFront(pet, f)
            pet.vy = -2.2f
            pet.age = 0f
            listener.onSpawn(pet)
        } else {
            val made = Recipes.cauldron(a, b)
            val t = world.addThing(made.type, made.variant, place, f.x, f.top)
            inFront(t, f)
            t.vy = -2.4f
            t.vx = (random.nextFloat() - 0.5f) * 0.6f
            t.vrot = 240f
            listener.onSpawn(t)
        }
        key?.let(::discover)
        listener.onFx(Fx.BREWED, f.x, f.top, f)
        unlock("lab_mix")
    }

    private fun catchFish(place: PlaceId, f: Fixture) {
        f.on = false
        world.catches++
        val made = if (world.catches % 5 == 0 && "beach_treasure" !in world.unlocked) {
            unlock("beach_treasure")
            Made(ThingType.GEM, random.nextInt(ThingType.GEM.variants))
        } else {
            val roll = random.nextFloat()
            when {
                roll < 0.45f -> Made(ThingType.FISH)
                roll < 0.57f -> Made(ThingType.BOOT)
                roll < 0.69f -> Made(ThingType.SHELL)
                roll < 0.79f -> Made(ThingType.STARFISH)
                roll < 0.86f -> Made(ThingType.COIN)
                roll < 0.92f -> Made(ThingType.DUCK)
                roll < 0.96f -> Made(ThingType.GEM, random.nextInt(ThingType.GEM.variants))
                else -> Made(ThingType.SWIM_RING)
            }
        }
        val t = world.addThing(made.type, made.variant, place, f.x + 0.12f, f.y + 0.06f)
        t.vy = -2.0f
        t.vx = -0.55f
        t.vrot = -300f
        capPlace(place, keep = t)
        listener.onSpawn(t)
        listener.onFx(Fx.CATCH, t.x, t.y, f, t)
    }

    // ------------------------------------------------------------------ taps

    /** A tap on a fixture at ([dx], [dy]) from its bottom centre. */
    fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float) {
        here = place
        f.taps = if (time - f.tapTime < 3.5f) f.taps + 1 else 1
        f.tapTime = time
        f.anim = 1f
        val top = f.top
        if (House.hasPassages(place) && house.tap(place, f, dx, dy)) return
        if (attractions.tap(place, f, dx, dy)) return
        when (f.type) {
            FixtureType.CANDY_FLOSS_STAND, FixtureType.POPCORN_CART -> dispense(place, f, dx)
            FixtureType.TOY_BOX -> {
                f.open = !f.open
                invalidate(place)
                listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.ROBOT_VACUUM -> {
                f.on = !f.on
                f.timer = 0f
                listener.onFx(if (f.on) Fx.ON else Fx.OFF, f.x + f.shiftX, top, f)
            }
            FixtureType.AQUARIUM -> listener.onFx(Fx.BUBBLES, f.x, f.y - f.spec.h * 0.6f, f)
            FixtureType.FLOWER_POT -> listener.onFx(Fx.SHAKE, f.x, f.y - f.spec.h * 0.7f, f)
            FixtureType.BEANBAG, FixtureType.ARMCHAIR, FixtureType.BUNK_BED -> listener.onFx(Fx.BOING, f.x, top, f)
            FixtureType.TRASH_BIN -> listener.onFx(Fx.TRASH, f.x, top, f, param = 0)
            FixtureType.FRIDGE, FixtureType.WARDROBE, FixtureType.CHEST, FixtureType.DISPLAY_CASE, FixtureType.TENT, FixtureType.SAUNA -> {
                f.open = !f.open
                invalidate(place)
                if (f.open && f.type == FixtureType.FRIDGE) restock(place, f)
                listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.OVEN -> when {
                f.on -> listener.onFx(Fx.TICK, f.x, top, f)
                dy < -0.19f && !f.open -> {
                    f.on = true
                    f.timer = 2.6f
                    listener.onFx(Fx.BAKE, f.x, top, f)
                }
                dy < -0.19f -> {
                    f.open = false
                    invalidate(place)
                    listener.onFx(Fx.CLOSE, f.x, f.y - 0.1f, f)
                }
                else -> {
                    f.open = !f.open
                    invalidate(place)
                    listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - 0.1f, f)
                }
            }
            FixtureType.STOVE, FixtureType.LAMP, FixtureType.CAMPFIRE, FixtureType.RADIO, FixtureType.SINK,
            FixtureType.BATH, FixtureType.HAIR_WASH, FixtureType.LAMP_POST, FixtureType.WOOD_STOVE -> {
                f.on = !f.on
                if (!f.on) thingsOn(place, f).forEach { it.cook = 0f }
                listener.onFx(if (f.on) Fx.ON else Fx.OFF, f.x, top, f)
            }
            FixtureType.TV -> {
                f.mode = (f.mode + 1) % TV_CHANNELS
                f.on = f.mode != 0
                listener.onFx(Fx.CHANNEL, f.x, top + 0.1f, f, param = f.mode)
            }
            FixtureType.PIANO -> {
                val keys = f.spec.dropZone!!
                val key = if (keys.contains(dx, dy)) ((dx - keys.left) / keys.width * PIANO_KEYS).toInt().coerceIn(0, PIANO_KEYS - 1) else random.nextInt(PIANO_KEYS)
                f.mode = key
                listener.onFx(Fx.KEY, f.x + dx, f.y + dy, f, param = key)
                // «Twinkle twinkle little star» (or «Bæ bæ lille lam»): C C G G A A G, an octave up.
                if (time - pianoLast > 2.5f) pianoTrail.clear()
                pianoLast = time
                pianoTrail += key
                if (pianoTrail.size > TWINKLE.size) pianoTrail.removeAt(0)
                if (pianoTrail == TWINKLE) {
                    pianoTrail.clear()
                    listener.onFx(Fx.STARRAIN, f.x, f.top, f)
                    egg("twinkle")
                }
            }
            FixtureType.WINDOW -> {
                f.mode = 1 - f.mode
                listener.onFx(Fx.CURTAIN, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.MAILBOX -> mailbox(place, f)
            FixtureType.CLOCK -> {
                if (f.taps >= 3) {
                    f.taps = 0
                    listener.onFx(Fx.CUCKOO, f.x, top, f)
                    unlock("home_clock")
                } else {
                    listener.onFx(Fx.TICK, f.x, top, f)
                }
            }
            FixtureType.MIRROR -> {
                listener.onFx(Fx.SPARKLE, f.x, f.y - f.spec.h / 2, f)
                if (place == PlaceId.SALON && f.taps >= 3) unlock("salon_mirror")
            }
            FixtureType.CRYSTAL_BALL -> {
                f.mode = (f.mode + 1) % 5
                listener.onFx(Fx.SPARKLE, f.x, f.y - f.spec.h * 0.6f, f)
                if (f.taps >= 3) unlock("lab_crystal")
            }
            FixtureType.PLANT_BIG -> listener.onFx(Fx.SHAKE, f.x, f.y - f.spec.h * 0.7f, f)
            FixtureType.PINE_TREE -> {
                f.angleV += 1.6f
                listener.onFx(Fx.SHAKE, f.x, f.y - f.spec.h * 0.7f, f, param = 1)
                if (f.taps >= 3) unlock("mountain_tree")
            }
            FixtureType.SNOWMAN -> {
                if (f.mode < 4) {
                    f.mode += 1
                    listener.onFx(Fx.BUILD, f.x, f.y - 0.05f * f.mode, f, param = f.mode)
                } else {
                    // A finished snowman shares a snowball.
                    val ball = world.addThing(ThingType.SNOWBALL, 0, place, f.x + 0.06f, f.y - 0.12f)
                    inFront(ball, f)
                    ball.vy = -1.4f
                    ball.vx = 0.7f
                    capPlace(place, keep = ball)
                    listener.onSpawn(ball)
                    listener.onFx(Fx.DISPENSE, ball.x, ball.y, f, ball)
                }
            }
            FixtureType.ICE_POND -> listener.onFx(Fx.CRACK, f.x + dx, f.y, f)
            FixtureType.TRACTOR -> {
                f.mode = 1 - f.mode
                f.count++
                listener.onFx(Fx.VROOM, f.x + f.shiftX, top, f, param = 2)
                if (f.count >= 3) unlock("farm_drive")
            }
            FixtureType.ROCKET_SHIP -> {
                if (f.on) return
                if (world.seatedAt(f, 0) != null) {
                    f.on = true
                    f.timer = 0f
                    listener.onFx(Fx.LAUNCH, f.x, f.y, f)
                    unlock("space_launch")
                } else {
                    listener.onFx(Fx.RUMBLE, f.x, f.y, f)
                }
            }
            FixtureType.CONTROL_PANEL -> {
                f.mode = (f.mode + 1) % 8
                world.fixturesIn(place).filter { it.type == FixtureType.PORTHOLE }.forEach { it.mode = (it.mode + 1) % PORTHOLE_VIEWS }
                listener.onFx(Fx.BEEP, f.x + dx, top, f, param = f.mode)
            }
            FixtureType.GRAVITY_LEVER -> {
                f.on = !f.on
                f.count++
                listener.onFx(Fx.GRAVITY, f.x, top, f, param = if (f.on) 1 else 0)
                if (!f.on) world.bodiesIn(place).forEach { if (it.mode == Mode.FREE && it.inside < 0) { it.resting = false; it.vy -= 0.25f } }
                if (f.count >= 2) unlock("space_gravity")
            }
            FixtureType.ORRERY -> {
                f.timer = 3f
                listener.onFx(Fx.SPARKLE, f.x, f.y - f.spec.h * 0.6f, f)
                if (f.taps >= 3) unlock("space_orrery")
            }
            FixtureType.PORTHOLE -> {
                f.mode = (f.mode + 1) % PORTHOLE_VIEWS
                listener.onFx(Fx.SPARKLE, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.VEGETABLE_PATCH -> when (f.mode) {
                3 -> {
                    f.mode = 0
                    repeat(3) { i ->
                        val veg = world.addThing(if ((i + f.count) % 2 == 0) ThingType.CARROT else ThingType.POTATO, 0, place, f.x - 0.12f + i * 0.12f, f.y - 0.02f)
                        inFront(veg, f)
                        veg.vy = -1.6f - i * 0.2f
                        veg.vx = (i - 1) * 0.3f
                        veg.vrot = (i - 1) * 200f
                        listener.onSpawn(veg)
                    }
                    f.count++
                    listener.onFx(Fx.HARVEST, f.x, f.y - 0.05f, f)
                }
                else -> listener.onFx(Fx.HOP, f.x, top, f)
            }
            FixtureType.WORKBENCH -> listener.onFx(if (world.inMachine(f).isEmpty()) Fx.HOP else Fx.HAMMER, f.x, top, f)
            FixtureType.WATER_TROUGH -> listener.onFx(Fx.WATER, f.x, top, f)
            FixtureType.HAY_BALE, FixtureType.SPACE_BED -> listener.onFx(Fx.BOING, f.x, top, f)
            FixtureType.CHICKEN_COOP, FixtureType.TOOL_WALL, FixtureType.WOOD_PILE, FixtureType.TIRE_STACK,
            FixtureType.FOOD_DISPENSER -> dispense(place, f, dx)
            FixtureType.OWL_TREE -> {
                f.mode = 1
                f.timer = 2.5f
                listener.onFx(Fx.OWL, f.x + 0.02f, f.y - 0.42f, f)
                if (f.taps >= 3) unlock("forest_owl")
            }
            FixtureType.CASH_REGISTER -> {
                listener.onFx(Fx.REGISTER, f.x, top, f)
                if (f.taps >= 5) unlock("cafe_register")
            }
            FixtureType.BLENDER -> if (!f.on) {
                if (world.inMachine(f).isNotEmpty()) {
                    f.on = true
                    f.timer = 1.3f
                    listener.onFx(Fx.BLEND, f.x, top, f)
                } else {
                    listener.onFx(Fx.EMPTY, f.x, top, f)
                }
            }
            FixtureType.FRUIT_CRATE, FixtureType.ICE_CREAM_MACHINE, FixtureType.FLOUR_SACK, FixtureType.CLOTHES_RACK,
            FixtureType.POTION_RACK, FixtureType.COCOA_STAND -> dispense(place, f, dx)
            FixtureType.CAULDRON -> listener.onFx(Fx.STIR, f.x, top, f)
            FixtureType.TELESCOPE -> {
                listener.onFx(Fx.LOOK, f.x, top, f)
                if (world.night) unlock("lab_telescope")
            }
            FixtureType.SPELLBOOK -> {
                f.mode = (f.mode + 1) % SPELL_PAGES
                listener.onFx(Fx.PAGE, f.x, top, f, param = f.mode)
            }
            FixtureType.FISHING_SPOT -> if (!f.on) {
                f.on = true
                f.timer = 1.5f
                listener.onFx(Fx.CAST, f.x, top, f)
            }
            FixtureType.BOAT -> listener.onFx(Fx.TOOT, f.x, top, f)
            FixtureType.UMBRELLA -> {
                f.angleV += 9f
                listener.onFx(Fx.SPIN, f.x, top, f)
            }
            FixtureType.SANDCASTLE -> {
                f.mode += 1
                if (f.mode > 4) {
                    f.mode = 0
                    listener.onFx(Fx.CRUMBLE, f.x, top, f)
                } else {
                    listener.onFx(Fx.BUILD, f.x, f.y - 0.03f * f.mode, f, param = f.mode)
                    if (f.mode == 4) unlock("beach_castle")
                }
            }
            FixtureType.SALON_CHAIR -> {
                f.mode = 1 - f.mode
                listener.onFx(Fx.PUMP, f.x, top, f)
            }
            FixtureType.DRYER_HOOD -> if (!f.on) {
                if (world.seatedAt(f, 0) != null) {
                    f.on = true
                    f.timer = 1.6f
                }
                listener.onFx(Fx.DRY, f.x, top, f, param = if (f.on) 1 else 0)
            }
            FixtureType.TOILET -> listener.onFx(Fx.FLUSH, f.x, top, f)
            FixtureType.BED, FixtureType.SOFA, FixtureType.LOUNGER, FixtureType.BENCH,
            FixtureType.CHAIR, FixtureType.LOG, FixtureType.SLED_HILL, FixtureType.SKI_JUMP -> listener.onFx(Fx.BOING, f.x, top, f)
            else -> listener.onFx(Fx.HOP, f.x, top, f)
        }
    }

    private fun restock(place: PlaceId, f: Fixture) {
        val inside = world.bodiesIn(place).count { it.inside == f.id }
        if (inside >= 3) return
        val food = listOf(ThingType.MILK, ThingType.EGG, ThingType.APPLE, ThingType.CARROT, ThingType.BROWN_CHEESE, ThingType.SAUSAGE, ThingType.JUICE, ThingType.STRAWBERRY)
        val shelves = f.spec.surfaces.filter { it.interior }
        repeat(3 - inside) { index ->
            val shelf = shelves[index % shelves.size]
            val t = world.addThing(food[random.nextInt(food.size)], 0, place, f.x - 0.035f + index * 0.035f, f.y + shelf.dy)
            t.inside = f.id
            t.resting = true
        }
    }

    private fun mailbox(place: PlaceId, f: Fixture) {
        val made = when {
            world.allSecretsFound() && !world.crownGiven -> {
                world.crownGiven = true
                Made(ThingType.CROWN, 1)
            }
            giftWaiting() -> {
                world.giftDay = today
                Made(ThingType.GIFT, (today % ThingType.GIFT.variants).toInt())
            }
            else -> null
        }
        if (made == null) {
            listener.onFx(Fx.EMPTY, f.x, f.y - 0.05f, f)
            return
        }
        val t = world.addThing(made.type, made.variant, place, f.x, f.y)
        inFront(t, f)
        t.vy = -1.4f
        t.vx = 0.45f
        t.vrot = 200f
        listener.onSpawn(t)
        listener.onFx(Fx.GIFT, f.x, f.y, f, t)
    }

    /** A gift is waiting when today's has not been taken, or the crown is due. */
    fun giftWaiting(): Boolean = world.giftDay != today || (world.allSecretsFound() && !world.crownGiven)

    private fun dispense(place: PlaceId, f: Fixture, dx: Float) {
        val spec = f.spec
        val index = (((dx + spec.w / 2) / spec.w) * 6).toInt().coerceIn(0, 5)
        val made = when (f.type) {
            FixtureType.FRUIT_CRATE -> Made(ThingType.FRUITS.random(random))
            FixtureType.ICE_CREAM_MACHINE -> Made(ThingType.ICE_CREAM, random.nextInt(ThingType.ICE_CREAM.variants))
            FixtureType.FLOUR_SACK -> Made(ThingType.DOUGH)
            FixtureType.CLOTHES_RACK -> Made(ThingType.GARMENT, Garment.pack(index, random.nextInt(Palette.cloth.size)))
            FixtureType.POTION_RACK -> Made(POTION_ROW[(((dx + spec.w / 2) / spec.w) * POTION_ROW.size).toInt().coerceIn(0, POTION_ROW.size - 1)])
            FixtureType.COCOA_STAND -> {
                f.count++
                Made(listOf(ThingType.COCOA, ThingType.WAFFLE, ThingType.BUN, ThingType.BROWN_CHEESE, ThingType.CLOUDBERRY)[f.count % 5])
            }
            FixtureType.CHICKEN_COOP -> {
                f.count++
                // Every sixth egg is golden, and the first golden egg brings out a glimt.
                if (f.count % 6 == 0) {
                    unlock("farm_egg")
                    Made(ThingType.EGG, 1)
                } else {
                    Made(ThingType.EGG)
                }
            }
            FixtureType.TOOL_WALL -> Made(TOOL_ROW[(((dx + spec.w / 2) / spec.w) * TOOL_ROW.size).toInt().coerceIn(0, TOOL_ROW.size - 1)])
            FixtureType.WOOD_PILE -> Made(ThingType.PLANK)
            FixtureType.TIRE_STACK -> Made(ThingType.TIRE)
            FixtureType.FOOD_DISPENSER -> {
                f.count++
                if (f.count % 4 == 3) Made(ThingType.ICE_CREAM, 4) else Made(ThingType.SPACE_FOOD, f.count % ThingType.SPACE_FOOD.variants)
            }
            else -> attractions.dispensed(f) ?: return
        }
        val y = when (f.type) {
            FixtureType.POTION_RACK, FixtureType.TOOL_WALL -> f.y - 0.01f
            FixtureType.CLOTHES_RACK -> f.y - 0.18f
            FixtureType.COCOA_STAND -> f.y - 0.16f
            FixtureType.CHICKEN_COOP -> f.y - 0.08f
            FixtureType.FOOD_DISPENSER -> f.y - 0.1f
            FixtureType.CANDY_FLOSS_STAND -> f.y - 0.22f
            FixtureType.POPCORN_CART -> f.y - 0.3f
            else -> f.top
        }
        val t = world.addThing(made.type, made.variant, place, f.x + dx.coerceIn(-spec.w / 2 + 0.03f, spec.w / 2 - 0.03f), y)
        inFront(t, f)
        t.vy = if (f.type == FixtureType.POTION_RACK || f.type == FixtureType.CLOTHES_RACK) -0.4f else -1.8f
        t.vx = if (f.type == FixtureType.FRUIT_CRATE || f.type == FixtureType.FLOUR_SACK) (random.nextFloat() - 0.3f) * 0.8f else 0.2f
        t.vrot = (random.nextFloat() - 0.5f) * 300f
        capPlace(place, keep = t)
        listener.onSpawn(t)
        listener.onFx(Fx.DISPENSE, t.x, t.y, f, t)
    }

    /** Keeps a place from filling up: the oldest loose thing on the floor disappears in a puff. */
    private fun capPlace(place: PlaceId, keep: Thing) {
        val things = world.bodiesIn(place).filterIsInstance<Thing>()
        if (things.size <= MAX_THINGS) return
        val oldest = things.filter { it !== keep && it.mode == Mode.FREE && it.inside < 0 && !it.held }.minByOrNull { it.z } ?: return
        listener.onFx(Fx.POOF, oldest.x, oldest.y - oldest.h / 2, thing = oldest)
        removeThing(oldest)
    }

    // ------------------------------------------------------------------ things

    /** A tap on a loose thing. */
    fun use(place: PlaceId, t: Thing) {
        when (t.type) {
            ThingType.GIFT -> {
                val made = Gifts.surprise(t.id + t.variant * 13 + today.toInt())
                val x = t.x
                val y = t.y
                removeThing(t, quiet = true)
                val inside = world.addThing(made.type, made.variant, place, x, y)
                inside.vy = -1.8f
                inside.vrot = 180f
                listener.onSpawn(inside)
                listener.onFx(Fx.GIFT, x, y - 0.04f, thing = inside, param = 1)
            }
            ThingType.ROCKET -> {
                t.cook = -0.01f
                t.vy = -1.2f
                t.resting = false
                listener.onFx(Fx.VROOM, t.x, t.y, thing = t, param = 1)
            }
            ThingType.BALL, ThingType.BEACH_BALL, ThingType.SLIME -> {
                t.resting = false
                t.vy = -2.2f
                t.vx = (random.nextFloat() - 0.5f) * 1.4f
                t.vrot = t.vx * 400f
                listener.onFx(Fx.BOING, t.x, t.y, thing = t)
            }
            ThingType.TOY_CAR -> {
                t.vx = if (t.vx >= 0f) 1.3f else -1.3f
                listener.onFx(Fx.VROOM, t.x, t.y, thing = t)
            }
            ThingType.DUCK -> listener.onFx(Fx.SQUEAK, t.x, t.y - t.h, thing = t)
            ThingType.WHOOPEE -> jokes.squeeze(t)
            ThingType.PEPPER -> jokes.shake(place, t)
            ThingType.GUITAR -> listener.onFx(Fx.STRUM, t.x, t.y - t.h / 2, thing = t, param = random.nextInt(4))
            ThingType.DRUM -> listener.onFx(Fx.DRUM, t.x, t.y - t.h, thing = t)
            ThingType.PHONE -> listener.onFx(Fx.RING, t.x, t.y - t.h, thing = t)
            ThingType.BOOK -> listener.onFx(Fx.PAGE, t.x, t.y - t.h, thing = t)
            ThingType.WAND, ThingType.GEM, ThingType.STAR_JAR, ThingType.CROWN -> listener.onFx(Fx.SPARKLE, t.x, t.y - t.h / 2, thing = t)
            else -> {
                if (t.mode == Mode.FREE && t.resting) {
                    t.resting = false
                    t.inside = -1
                    t.vy = -1.1f
                    t.vrot = (random.nextFloat() - 0.5f) * 200f
                }
                listener.onFx(Fx.HOP, t.x, t.y, thing = t)
            }
        }
    }

    /**
     * A thing dropped over a machine. Returns true when the machine took it; otherwise it falls as usual.
     */
    fun dropInto(place: PlaceId, f: Fixture, t: Thing): Boolean {
        if (place.big && house.drop(place, f, t)) return true
        when (f.type) {
            FixtureType.BLENDER -> {
                if (f.on || world.inMachine(f).size >= 3) return false
                take(t, f)
            }
            FixtureType.CAULDRON -> {
                if (f.on || world.inMachine(f).size >= 2) return false
                take(t, f)
            }
            FixtureType.TOILET -> {
                val chest = world.fixturesIn(PlaceId.HOME).firstOrNull { it.type == FixtureType.CHEST } ?: return false
                listener.onFx(Fx.FLUSH, f.x, f.top, f, t)
                t.place = PlaceId.HOME
                t.mode = Mode.FREE
                t.holder = -1
                t.x = chest.x + (random.nextFloat() - 0.5f) * 0.1f
                t.y = chest.y - 0.015f
                t.inside = chest.id
                t.resting = true
                t.vx = 0f
                t.vy = 0f
                t.rot = 0f
                t.held = false
                return true
            }
            FixtureType.WORKBENCH -> {
                val contents = world.inMachine(f)
                if (t.type.buildTool) {
                    // A tool builds what lies on the bench; the tool itself stays.
                    if (contents.isEmpty()) return false
                    val made = Recipes.workbench(contents.map { it.type })
                    Recipes.keyFor(FixtureType.WORKBENCH, contents.map { it.type })?.let(::discover)
                    contents.forEach { removeThing(it, quiet = true) }
                    val built = world.addThing(made.type, made.variant, place, f.x, f.top - 0.02f)
                    inFront(built, f)
                    built.vy = -1.8f
                    built.vx = 0.3f
                    built.vrot = 200f
                    listener.onSpawn(built)
                    listener.onFx(Fx.HAMMER, f.x, f.top, f, built)
                    unlock("farm_build")
                    return false
                }
                if (contents.size >= 2) return false
                take(t, f)
            }
            FixtureType.VEGETABLE_PATCH -> {
                when {
                    t.type == ThingType.SEEDS && f.mode == 0 -> {
                        removeThing(t, quiet = true)
                        f.mode = 1
                        f.timer = GROW_SECONDS
                        listener.onFx(Fx.GROW, f.x, f.y - 0.04f, f, param = 1)
                        return true
                    }
                    t.type == ThingType.WATERING_CAN && f.mode in 1..2 -> {
                        f.mode += 1
                        f.timer = GROW_SECONDS
                        listener.onFx(Fx.WATER, f.x, f.y - 0.1f, f)
                        listener.onFx(Fx.GROW, f.x, f.y - 0.04f, f, param = f.mode)
                    }
                }
                return false
            }
            FixtureType.TRASH_BIN -> return designer.bin(place, f, t)
            FixtureType.ICE_POND -> {
                if (t.type != ThingType.COIN) return false
                listener.onFx(Fx.WISH, f.x, f.y - 0.05f, f, t)
                removeThing(t, quiet = true)
                unlock("mountain_wish")
                return true
            }
            else -> return false
        }
        listener.onFx(Fx.INTO, f.x, f.top, f, t)
        return true
    }

    private fun take(t: Thing, f: Fixture) {
        t.mode = Mode.INSIDE
        t.holder = f.id
        t.held = false
        t.resting = false
        t.inside = -1
        t.x = f.x
        t.y = f.top
    }

    /** Hands [t] to [p] at [part]. The engine picks the part from where the thing was dropped. */
    fun give(p: Person, t: Thing, part: Part): Give {
        val type = t.type
        p.place?.let { here = it }
        val result = giveTo(p, t, part)
        when (result) {
            Give.ATE, Give.DRANK, Give.FINISHED -> if (p.species.pet) tasks.record(Deed.FED, here, type, species = p.species)
            Give.WORE -> tasks.record(Deed.WORE, here, type)
            Give.DRESSED -> tasks.record(Deed.DRESSED, here, type)
            Give.HAIR -> tasks.record(Deed.HAIRCUT, here, type)
            else -> Unit
        }
        if (result != Give.NONE && result != Give.SNEEZE) life.given(p, type)
        if (result == Give.ATE || result == Give.DRANK || result == Give.FINISHED) jokes.ate(p, type, result, time)
        // Brunost for the moose calf: a little dance.
        if (p.species == Species.ELK && type == ThingType.BROWN_CHEESE && result != Give.NONE) {
            listener.onFx(Fx.JIG, p.x, p.y - p.h, param = p.id)
            egg("elk")
        }
        return result
    }

    private fun giveTo(p: Person, t: Thing, part: Part): Give {
        when {
            t.type == ThingType.PEPPER && (part == Part.MOUTH || part == Part.GLASSES) -> {
                jokes.pepper(p)
                return Give.SNEEZE
            }
            // The robot butler runs on «fuel» and the ghost has no tummy: both make a joke of food (not of potions).
            part == Part.MOUTH && t.type.edible && !t.type.potion && p.species == Species.ROBOT -> return figurar.fuel(p, t)
            part == Part.MOUTH && t.type.edible && !t.type.potion && p.species == Species.GHOST -> return figurar.sniff(p, t)
            part == Part.MOUTH && t.type.edible -> {
                t.used++
                p.anim.chew = 1f
                if (t.type.potion) {
                    applyPotion(p, t.type)
                    removeThing(t, quiet = true)
                    return Give.POTION
                }
                if (t.used >= t.type.bites) {
                    removeThing(t, quiet = true)
                    return Give.FINISHED
                }
                return if (t.type.drink) Give.DRANK else Give.ATE
            }
            part == Part.HAT && t.type.slot == Slot.HEAD -> {
                wear(p, t, Slot.HEAD)
                // A crown on the troll makes him king.
                if (t.type == ThingType.CROWN && p.name == "Rumle") {
                    listener.onFx(Fx.KING, p.x, p.y - p.h, thing = t, param = p.id)
                    egg("king")
                }
                return Give.WORE
            }
            part == Part.GLASSES && t.type.slot == Slot.FACE -> {
                wear(p, t, Slot.FACE)
                return Give.WORE
            }
            part == Part.BODY && t.type == ThingType.GARMENT && p.species == Species.FOLK -> {
                val old = Garment.pack(p.look.top, p.look.topColor)
                p.look = p.look.copy(top = Garment.style(t.variant), topColor = Garment.color(t.variant))
                t.variant = old
                t.held = false
                t.mode = Mode.FREE
                t.resting = false
                t.vy = -1.4f
                t.vx = 0.6f
                t.vrot = 300f
                p.anim.sparkle = 1f
                return Give.DRESSED
            }
            part == Part.HAIR && t.type.hairTool && p.species == Species.FOLK -> {
                val look = p.look
                p.look = when (t.type) {
                    ThingType.SCISSORS -> look.copy(hair = shorter(look.hair))
                    ThingType.COMB -> look.copy(hair = look.hair % (Styles.HAIRS - 1) + 1)
                    ThingType.HAIR_DRYER -> look.copy(hair = if (look.hair == 3) 7 else 3)
                    ThingType.SPRAY -> look.copy(hairColor = t.variant.mod(Palette.hairs.size))
                    else -> look
                }
                p.anim.sparkle = 1f
                return Give.HAIR
            }
            part == Part.HAND -> {
                wear(p, t, Slot.HAND)
                return Give.HELD
            }
        }
        return Give.NONE
    }

    private fun shorter(hair: Int): Int = when (hair) {
        4, 5 -> 8
        8 -> 1
        6, 3, 2, 7 -> 1
        else -> 0
    }

    private fun wear(p: Person, t: Thing, slot: Slot) {
        world.worn(p, slot)?.takeIf { it !== t }?.let { old ->
            val at = Anatomy.at(p, if (slot == Slot.HEAD) Part.HAT else if (slot == Slot.FACE) Part.GLASSES else Part.HAND)
            old.mode = Mode.FREE
            old.holder = -1
            old.place = p.place
            old.x = at[0]
            old.y = at[1]
            old.resting = false
            old.vy = -1.0f
            old.vx = 0.5f
            old.vrot = 220f
        }
        t.mode = Mode.WORN
        t.holder = p.id
        t.slot = slot.ordinal
        t.place = p.place
        t.held = false
        t.resting = false
        t.inside = -1
        t.rot = 0f
    }

    fun applyPotion(p: Person, type: ThingType) {
        p.anim.sparkle = 1f
        when (type) {
            ThingType.POTION_GROW -> p.scale = (p.scale * 1.3f).coerceAtMost(1.7f)
            ThingType.POTION_SHRINK -> p.scale = (p.scale * 0.75f).coerceAtLeast(0.5f)
            ThingType.POTION_RAINBOW -> p.look = if (p.species == Species.FOLK) {
                p.look.copy(skin = Palette.HUMAN_SKINS + (p.look.skin + 1 + random.nextInt(5)).mod(Palette.skins.size - Palette.HUMAN_SKINS))
            } else {
                p.look.copy(skin = p.look.skin + 1)
            }
            ThingType.POTION_FLOAT -> p.floatTime = 9f
            ThingType.POTION_NORMAL -> {
                p.scale = 1f
                p.floatTime = 0f
            }
            else -> Unit
        }
    }

    fun removeThing(t: Thing, quiet: Boolean = false) {
        world.remove(t)
        if (!quiet) listener.onRemove(t)
    }

    // ------------------------------------------------------------------ home designer

    /** Furniture the child may pick up and move. Water-bound and built-in things stay put. */
    fun movable(f: Fixture): Boolean = when (f.type) {
        FixtureType.PIER, FixtureType.FISHING_SPOT, FixtureType.BOAT, FixtureType.ICE_POND, FixtureType.STAGE_PLATFORM,
        FixtureType.SHIPWRECK, FixtureType.CABLE_CAR, FixtureType.CABLE_STATION, FixtureType.ROCK_LEDGE, FixtureType.SUMMIT_ROCK,
        FixtureType.ECHO_ROCK, FixtureType.MOUNTAIN_HUT, FixtureType.EAGLE_NEST, FixtureType.SUMMIT_FLAG -> false
        // Small things on furniture (the radio on the table) go with their furniture; running rides wait.
        else -> f.host < 0 && !(f.on && f.type in MOVING) && !(f.place.mine && House.passageAt(f) != null)
    }

    /**
     * Where a moved piece of furniture may stand: on the floor band for floor furniture, on the wall for
     * wall furniture, and never off the ends of the place or into water.
     */
    fun clampFixture(place: PlaceId, f: Fixture, x: Float, y: Float): FloatArray {
        val half = f.spec.w / 2f
        var cx = x.coerceIn(half + 0.01f, place.width - half - 0.01f)
        // Furniture stays inside the rooms that stand: an empty slot of Mitt hus is open air.
        if (place.mine) cx = Mine.clampX(world.mine, place, cx, half)
        val cy = if (f.spec.wall) {
            y.coerceIn(place.ceiling + f.spec.h + 0.02f, place.back - 0.01f)
        } else {
            // Only where there is dry floor.
            val band = surfaces(place).filter { it.band }
            val onBand = band.firstOrNull { cx >= it.x1 && cx <= it.x2 } ?: band.minByOrNull { minOf(kotlin.math.abs(cx - it.x1), kotlin.math.abs(cx - it.x2)) }
            if (onBand != null) cx = cx.coerceIn(onBand.x1 + min(half, (onBand.x2 - onBand.x1) / 2f), onBand.x2 - min(half, (onBand.x2 - onBand.x1) / 2f))
            y.coerceIn(place.back + 0.004f, PlaceId.FRONT)
        }
        return floatArrayOf(cx, cy)
    }

    /** Moves a piece of furniture with everything on it, in it, seated on it and standing on it. */
    fun moveFixture(place: PlaceId, f: Fixture, x: Float, y: Float) {
        val dx = x - f.x
        val dy = y - f.y
        if (dx == 0f && dy == 0f) return
        shiftFixture(place, f, dx, dy)
        invalidate(place)
    }

    private fun shiftFixture(place: PlaceId, f: Fixture, dx: Float, dy: Float) {
        f.x += dx
        f.y += dy
        f.depth += dy
        for (b in world.bodiesIn(place)) {
            if (b.held || b.mode != Mode.FREE) continue
            if ((b.resting && b.restOwner == f.id) || b.inside == f.id) {
                b.x += dx
                b.y += dy
                if (!b.ground.isNaN()) b.ground += dy
            }
        }
        for (other in world.fixturesIn(place)) if (other.host == f.id) shiftFixture(place, other, dx, dy)
    }

    /** Where a glimt is now: glimt on furniture follow the furniture when the child moves it. */
    fun secretAt(s: Secret): FloatArray {
        if (s.on < 0) return floatArrayOf(s.x, s.y)
        val f = world.fixtures[s.place.idBase + s.on] ?: return floatArrayOf(s.x, s.y)
        val def = Places.spec(s.place).fixtures[s.on]
        return floatArrayOf(s.x + f.x - def.x, s.y + f.y - def.y)
    }

    // ------------------------------------------------------------------ discovery

    fun unlock(id: String) {
        if (id in world.found || id in world.unlocked) return
        if (Secrets.byId(id)?.event != true) return
        world.unlocked += id
        listener.onSecret(id)
    }

    /**
     * Marks progress in the big house: a key found, a lever pulled, a door opened. Returns true the first time.
     * The five golden keys (see [HouseKeys]) open the tunnel door when all are found.
     */
    fun flag(name: String): Boolean {
        if (!world.flags.add(name)) return false
        if (name in HouseKeys.ids) {
            val n = HouseKeys.found(world)
            listener.onFx(Fx.HOUSE, 0f, 0f, null, null, HouseFx.pack(HouseFx.KEY_FOUND, n))
            if (n == HouseKeys.ids.size && world.flags.add(HouseKeys.TUNNEL)) {
                listener.onFx(Fx.HOUSE, 0f, 0f, null, null, HouseFx.pack(HouseFx.KEYS_DONE, n))
            }
        }
        return true
    }

    fun collect(id: String): Boolean {
        if (id in world.found) return false
        world.found += id
        Secrets.byId(id)?.let { tasks.record(Deed.SECRET, it.place) }
        return true
    }

    fun discover(key: String) {
        if (world.discoveries.add(key)) listener.onDiscovery(key)
    }

    /** Glimt that can be seen and collected in [place] right now. */
    fun visibleSecrets(place: PlaceId): List<Secret> = Secrets.inPlace(place).filter { s ->
        if (s.id in world.found) return@filter false
        if (s.event && s.id !in world.unlocked) return@filter false
        if (s.inside >= 0) {
            val box = world.fixturesIn(place).getOrNull(s.inside)
            if (box == null || !box.open) return@filter false
        }
        true
    }

    companion object {
        const val PI_F = 3.1415927f
        const val TRACTOR_DRIVE = 0.62f
        const val GROW_SECONDS = 6f
        const val PORTHOLE_VIEWS = 6
        val RIDES = setOf(FixtureType.SLED_HILL, FixtureType.SKI_JUMP)
        val MOVING = setOf(FixtureType.CABLE_CAR, FixtureType.ROCKET_SHIP, FixtureType.SUBMARINE, FixtureType.FERRIS_WHEEL, FixtureType.CAROUSEL, FixtureType.BUMPER_CAR, FixtureType.TRACTOR)
        const val GRAVITY = 5.2f
        const val MAX_THINGS = 70
        const val TV_CHANNELS = 6
        /** Piano keys (index into the pentatonic scale) of the hidden tune. */
        val TWINKLE = listOf(5, 5, 8, 8, 9, 9, 8)
        const val PIANO_KEYS = 10
        const val SPELL_PAGES = 6
    }
}
