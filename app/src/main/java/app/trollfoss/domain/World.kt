package app.trollfoss.domain

/** What holds a body in place. */
enum class Mode {
    /** Under physics: falling, rolling or resting on a surface. */
    FREE,
    /** A figure on a seat: [Body.holder] is the fixture, [Body.slot] the spot. */
    SEATED,
    /** A thing carried by a figure: [Body.holder] is the figure, [Body.slot] the [Slot] ordinal. */
    WORN,
    /** In the travel bag, in no place. */
    BAG,
    /** Taken by a machine (blender, cauldron): [Body.holder] is the fixture. */
    INSIDE,
}

enum class Weather { SUN, RAIN, SNOW }

/** A figure's face. The art draws each one; the engine picks them as things happen. */
enum class Face { HAPPY, GRIN, OOH, CHOMP, YUM, SLEEP, DIZZY, LAUGH, WOW, YUCK }

/**
 * Anything that can be picked up: a thing or a figure. Position is the bottom centre in scene units.
 * The fields below the line are physics and animation state that is never saved.
 */
sealed class Body(val id: Int) {
    var place: PlaceId? = null
    var x = 0f
    var y = 0f
    var mode = Mode.FREE
    var holder = -1
    var slot = 0
    var z = 0L
    var rot = 0f

    /** The cupboard this body rests inside, or -1. Hidden while that cupboard is shut. */
    var inside = -1

    // ---- not saved ----
    var vx = 0f
    var vy = 0f
    var vrot = 0f
    var resting = false
    var held = false

    /** Landing squash: 0 is none, positive is squashed flat. Springs back on its own. */
    var squash = 0f
    var squashV = 0f

    /** Seconds since the body appeared; new things pop in. */
    var age = 10f

    /**
     * Where on the floor band this body stands («skrå-3D»): the screen y of its feet when it is on the
     * floor. Not a number until first placed.
     */
    var ground = Float.NaN

    /** The fixture this body rests on, -1 for the floor, -2 for nothing. */
    var restOwner = -2

    /** Seconds before this body can bonk someone again. */
    var cool = 0f

    /** Flying home when the place is tidied: 0 to 1 along an arc, or -1 when not flying. Never saved. */
    var flyT = -1f
    var flyX0 = 0f
    var flyY0 = 0f
    var flyX1 = 0f
    var flyY1 = 0f
    /** Where it lands: a cupboard to be inside of, or -1. */
    var flyInside = -1

    abstract val w: Float
    abstract val h: Float
}

class Thing(id: Int, var type: ThingType, var variant: Int = 0) : Body(id) {
    /** Bites or sips taken. */
    var used = 0

    /**
     * Where this thing belongs, for tidying up: its place, and the fixture it lies on or in ([homeOwner],
     * -1 for the floor) with its offset from that fixture (or its position on the floor). Things made
     * during play have no home.
     */
    var homePlace: PlaceId? = null
    var homeOwner = -1
    var homeDx = 0f
    var homeDy = 0f
    var homeInside = false

    /** Seconds spent on something hot; not saved. */
    var cook = 0f

    override val w: Float get() = type.w
    override val h: Float get() = type.h
}

class Person(id: Int, val species: Species, var look: Look, var voice: Float, var name: String = "") : Body(id) {
    /** Grow and shrink potions. */
    var scale = 1f
    var scaleBefore = 1f
    var scaleTime = 0f

    /** Seconds left of the float potion. */
    var floatTime = 0f

    val anim = PersonAnim()

    override val h: Float get() = species.height * (if (species == Species.FOLK) look.height else 1f) * scale
    override val w: Float get() = h * species.widthRatio
}

/** Moment-to-moment life of a figure. Never saved. */
class PersonAnim {
    var activity = 0
    var activityTime = 0f
    var pose = Pose.STAND
    var face = Face.HAPPY
    var faceTime = 0f
    var blink = 0f
    var nextBlink = 2f
    var lookX = 0f
    var lookY = 0f
    var chew = 0f
    var kick = 0f
    var dance = 0f
    var hop = 0f
    var hopV = 0f
    var taps = 0
    var lastTap = -10f

    /** Seconds left to show the figure's name above its head. */
    var nameTag = 0f
    var tilt = 0f
    var sparkle = 0f
    var talk = 0f
    var nextIdle = 3f
    var wave = 0f

    /** The wish in the figure's thought bubble, and seconds until the next one (negative: not set yet). */
    var wish: Wish? = null
    var nextWish = -1f

    /** Seconds left of a wish-granted cheer. */
    var cheer = 0f

    /** A little chat: the icon in the speech bubble and how long it shows. */
    var say = -1
    var sayTime = 0f
    var chatWith = -1

    // Animals strolling about on their own.
    var still = 0f
    var walkTo = Float.NaN
    var walkGround = 0f
    var nextWalk = 2f
    var walkPhase = 0f
    /** The snack an animal is heading for, or -1. */
    var chase = -1

    /** 1 facing right, -1 facing left (animals turn to where they walk). */
    var facing = 1f

    // Folk who act on their own: where they are heading (0 stroll, 1 seat, 2 bed, 3 a friend) and why.
    var goal = 0
    var goalFixture = -1
    var goalSpot = 0
    /** 0 nothing; 1 went to bed by themselves; 2 sat down by themselves. They get up again on their own. */
    var auto = 0
    var autoTimer = 0f

    // Slapstick (see Jokes).
    /** A slip on a banana peel: 1 at the start of the spin, down to 0. */
    var spin = 0f
    var slipCool = 0f
    /** Seconds until «ATSJO!». */
    var sneeze = 0f
    /** Seconds until a burp. */
    var burp = 0f
    var hiccups = 0f
    var nextHic = 0f
    var sips = 0
    var lastSip = -10f
    /** Seconds of cream left on the face after a cake in the face. */
    var cream = 0f
    /** Seconds of octopus ink left on the face. */
    var ink = 0f
    /** Seconds left of a helpless giggle fit from tickling. */
    var tickle = 0f

    /** Seconds left of a dusty «ah … ah …» (Sture's own sneezes, see [Figurar]); the dust flies when it ends. */
    var achoo = 0f
}

/**
 * A piece of furniture or a machine in a place. Its state is saved; the rest comes from the blueprint.
 * [depth] is where it stands on the floor band; for a radio on a table it is the table's depth.
 */
class Fixture(val id: Int, val place: PlaceId, val type: FixtureType, var x: Float, var y: Float, val variant: Int = 0, var depth: Float = y) {
    var open = false

    /** The fixture this one stands on (a radio on a table), or -1. Set from the blueprint, never saved. */
    var host = -1

    /** How far a piece of furniture is lifted while the child moves it (0 to 1). Never saved. */
    var lift = 0f
    var on = false
    var mode = 0
    var count = 0

    // ---- not saved ----
    var timer = 0f
    var anim = 0f
    var angle = 0f
    var angleV = 0f
    var taps = 0
    var tapTime = -10f
    var bumpTime = -10f
    var bob = 0f

    /** How far a vehicle has moved from its spot (the tractor drives, the rocket flies). */
    var shiftX = 0f
    var shiftY = 0f

    val spec: FixtureSpec get() = if (type == FixtureType.MOUNTAIN_HUT && place == PlaceId.VAGSTADDALEN) Vagstaddalen.cabinSpec else type.spec

    /** Top edge in scene units. */
    val top: Float get() = y - spec.h
}

/**
 * The whole village: every body, every fixture and what the child has found. One instance lives in the
 * view model and is saved as JSON by [app.trollfoss.data.WorldStore].
 */
class World {
    val bodies = LinkedHashMap<Int, Body>()
    val fixtures = LinkedHashMap<Int, Fixture>()
    var nextId = 1
    var zCounter = 0L
    var place = PlaceId.HOME
    var night = false
    var weather = Weather.SUN

    /** Glimt the child has collected. */
    val found = linkedSetOf<String>()

    /** Glimt that an event has brought out. */
    val unlocked = linkedSetOf<String>()

    /** Progress in the big house: golden keys found, levers pulled, doors opened (see [House]). Saved. */
    val flags = linkedSetOf<String>()

    /** The child's own house, built room by room (see [Mine]). Saved. */
    val mine = MineHouse()

    /** Recipes made at least once. */
    val discoveries = linkedSetOf<String>()

    /** Epoch day of the last daily gift taken from the mailbox. */
    var giftDay = -1L
    var crownGiven = false
    var catches = 0

    /** Wishes the child has granted, all places together. */
    var wishesGranted = 0

    /** Wallpaper and floor per room, keyed by [Decor.key]. */
    val styles = HashMap<String, RoomStyle>()

    /** Furniture the child has put away in the home designer's store. */
    val storage = ArrayList<Stored>()
    /** Removed furniture waits here until restored, including across app restarts. */
    val discardedStorage = ArrayList<Pair<Int, Stored>>()

    /** Stickers earned from tasks, by sticker number. They open special furniture in the catalogue. */
    val stickers = ArrayList<Int>()

    /** Easter eggs found, by id. Each one earns a sticker the first time. */
    val eggs = LinkedHashSet<String>()

    /** The tasks on the board, how far each has come, and where the shuffled deck is. */
    val taskSet = ArrayList<String>()
    val taskProgress = HashMap<String, Int>()
    var taskSeed = 2026
    var taskCursor = 0

    fun nextZ(): Long = ++zCounter

    fun fixturesIn(place: PlaceId): List<Fixture> = fixtures.values.filter { it.place == place }

    fun bodiesIn(place: PlaceId): List<Body> = bodies.values.filter { it.place == place && it.mode != Mode.BAG }

    fun bag(): List<Body> = bodies.values.filter { it.mode == Mode.BAG }.sortedBy { it.z }

    fun people(): List<Person> = bodies.values.filterIsInstance<Person>()

    fun worn(person: Person, slot: Slot): Thing? = bodies.values.firstOrNull {
        it is Thing && it.mode == Mode.WORN && it.holder == person.id && it.slot == slot.ordinal
    } as Thing?

    fun carried(person: Person): List<Thing> =
        bodies.values.filter { it is Thing && it.mode == Mode.WORN && it.holder == person.id }.map { it as Thing }

    fun inMachine(fixture: Fixture): List<Thing> =
        bodies.values.filter { it is Thing && it.mode == Mode.INSIDE && it.holder == fixture.id }.map { it as Thing }

    fun seatedAt(fixture: Fixture, spot: Int): Person? = bodies.values.firstOrNull {
        it is Person && it.mode == Mode.SEATED && it.holder == fixture.id && it.slot == spot
    } as Person?

    fun addThing(type: ThingType, variant: Int, place: PlaceId?, x: Float, y: Float): Thing {
        val thing = Thing(nextId++, type, variant.mod(type.variants.coerceAtLeast(1)).let { if (type == ThingType.GARMENT) variant else it })
        thing.place = place
        thing.x = x
        thing.y = y
        thing.z = nextZ()
        thing.age = 0f
        bodies[thing.id] = thing
        return thing
    }

    fun addPerson(species: Species, look: Look, voice: Float, place: PlaceId?, x: Float, y: Float, name: String = ""): Person {
        val person = Person(nextId++, species, look.safe(), voice, name)
        person.place = place
        person.x = x
        person.y = y
        person.z = nextZ()
        bodies[person.id] = person
        return person
    }

    /** Removes a thing (eaten, flushed, blended). Anything it carried is dropped where it was. */
    fun remove(body: Body) {
        bodies.remove(body.id)
        if (body is Person) {
            carried(body).forEach { it.mode = Mode.FREE; it.holder = -1; it.place = body.place; it.resting = false }
        }
    }

    fun allSecretsFound(): Boolean = Secrets.all.all { it.id in found }
}
