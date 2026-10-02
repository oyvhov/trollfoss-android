package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.max

/** Things become activities in a friend's hand. Tools and companions survive every activity. */
class PersonPlay(private val sim: Sim) {
    private val world get() = sim.world
    private val passes = HashMap<Int, Int>()

    fun given(p: Person, t: Thing) {
        p.anim.activity = 0; p.anim.activityTime = 0f
        if (t.type == ThingType.BALL || t.type == ThingType.BEACH_BALL || t.type == ThingType.SNOWBALL) return
        use(p, t)
    }

    fun use(p: Person, t: Thing): Boolean {
        val place = p.place ?: return false
        if (t.mode != Mode.WORN || t.holder != p.id || t.slot != Slot.HAND.ordinal) return false
        val a = p.anim
        val activity = when (t.type) {
            ThingType.BOOK -> READ
            ThingType.PHONE -> PHONE
            ThingType.TOOTHBRUSH -> BRUSH
            ThingType.TEDDY, ThingType.PILLOW -> HUG
            ThingType.GUITAR, ThingType.DRUM, ThingType.MICROPHONE -> MUSIC
            ThingType.BALL, ThingType.BEACH_BALL, ThingType.SNOWBALL -> THROW
            else -> return false
        }
        a.activity = activity; a.activityTime = 4f; a.face = Face.HAPPY; a.faceTime = 4f
        a.walkTo = Float.NaN; a.nextWalk = 5f
        val fx = when (activity) {
            READ -> { t.used = t.used % 3 + 1; a.say = 1; a.sayTime = 3f; Fx.PAGE }
            PHONE -> {
                a.talk = 3f; a.say = 0; a.sayTime = 3f
                world.people().firstOrNull { it !== p && world.worn(it, Slot.HAND)?.type == ThingType.PHONE }?.let {
                    it.anim.activity = PHONE; it.anim.activityTime = 4f; it.anim.talk = 3f
                    it.anim.say = 0; it.anim.sayTime = 3f; it.anim.face = Face.GRIN; it.anim.faceTime = 3f
                }
                Fx.RING
            }
            BRUSH -> { a.sparkle = 1f; Fx.WATER }
            HUG -> { a.say = 0; a.sayTime = 3f; Fx.SQUEAK }
            MUSIC -> {
                for (friend in world.bodiesIn(place).filterIsInstance<Person>()) {
                    if (friend.held || abs(friend.x - p.x) > 1.3f || friend.anim.pose == Pose.LIE) continue
                    friend.anim.dance = 3f; friend.anim.face = Face.GRIN; friend.anim.faceTime = 3f
                    friend.anim.say = 2; friend.anim.sayTime = 3f
                }
                when (t.type) { ThingType.DRUM -> Fx.DRUM; ThingType.MICROPHONE -> Fx.SING; else -> Fx.STRUM }
            }
            else -> {
                val other = world.bodiesIn(place).filterIsInstance<Person>().filter {
                    it !== p && !it.held && it.mode != Mode.BAG && abs(it.x - p.x) in 0.15f..1.2f && world.worn(it, Slot.HAND) == null
                }.minByOrNull { abs(it.x - p.x) }
                val hand = Anatomy.at(p, Part.HAND)
                t.mode = Mode.FREE; t.holder = -1; t.resting = false; t.inside = -1
                t.x = hand[0]; t.y = hand[1]; t.ground = p.y
                t.vx = other?.let { (Anatomy.at(it, Part.HAND)[0] - t.x) / 0.38f } ?: 0.8f * p.anim.facing
                t.vy = -0.85f; t.vrot = t.vx * 160f; t.cool = 1f
                if (other != null) passes[t.id] = other.id
                a.wave = 1f
                Fx.BOING
            }
        }
        sim.listener.onFx(fx, p.x, p.y - p.h * 0.5f, thing = t)
        return true
    }

    fun brush(p: Person, t: Thing): Boolean {
        if (t.type != ThingType.TOOTHBRUSH) return false
        sim.give(p, t, Part.HAND)
        p.anim.sparkle = 1f
        return true
    }

    fun tickle(p: Person, t: Thing): Boolean {
        if (t.type != ThingType.FEATHER && !(t.type == ThingType.COMB && p.species != Species.FOLK)) return false
        p.anim.face = Face.LAUGH; p.anim.faceTime = 2f; p.anim.tickle = 2f; p.anim.hopV = 0.7f
        p.anim.say = 0; p.anim.sayTime = 2f
        sim.listener.onFx(Fx.SQUEAK, p.x, p.y - p.h * 0.5f, thing = t)
        return true
    }

    fun step(p: Person, dt: Float) {
        p.anim.activityTime = max(0f, p.anim.activityTime - dt)
        if (p.anim.activityTime == 0f || p.held || world.worn(p, Slot.HAND) == null && p.anim.activity != BRUSH) p.anim.activity = 0
    }

    /** Familiar furniture invites a nearby friend into a small, repeatable scene. */
    fun fixture(f: Fixture) {
        val nearby = world.bodiesIn(f.place).filterIsInstance<Person>().filter { !it.held && abs(it.x - f.x) < 0.8f }
        when (f.type) {
            FixtureType.TV, FixtureType.GR_TV -> for (p in nearby) {
                p.anim.face = if (f.mode % 2 == 0) Face.WOW else Face.LAUGH; p.anim.faceTime = 3f
                p.anim.lookX = ((f.x - p.x) * 2f).coerceIn(-1f, 1f); p.anim.say = 1; p.anim.sayTime = 2f
            }
            FixtureType.RADIO, FixtureType.PIANO -> for (p in nearby) {
                if (f.type == FixtureType.RADIO && f.on) continue
                p.anim.dance = 3f; p.anim.face = Face.GRIN; p.anim.faceTime = 3f; p.anim.say = 2; p.anim.sayTime = 3f
            }
            FixtureType.MIRROR, FixtureType.UP_BATH_MIRROR -> for (p in nearby) {
                p.anim.wave = 2f; p.anim.face = if (f.taps % 2 == 0) Face.WOW else Face.GRIN; p.anim.faceTime = 2f
            }
            FixtureType.BOOKCASE -> nearby.firstOrNull { Anatomy.upright(it.species) }?.let { p ->
                val hand = world.worn(p, Slot.HAND)
                if (hand?.type == ThingType.BOOK) use(p, hand)
                else if (hand == null) {
                    val book = world.bodiesIn(f.place).filterIsInstance<Thing>().firstOrNull { it.type == ThingType.BOOK && it.mode == Mode.FREE && it.restOwner == f.id && !it.held }
                        ?: world.addThing(ThingType.BOOK, f.taps.mod(4), f.place, f.x, f.top)
                    sim.give(p, book, Part.HAND)
                }
            }
            FixtureType.BATH, FixtureType.HAIR_WASH -> if (!f.on) for (p in nearby.filter { it.mode == Mode.SEATED && it.holder == f.id }) {
                p.anim.cream = 0f; p.anim.ink = 0f; p.anim.sparkle = 1f; p.anim.face = Face.GRIN; p.anim.faceTime = 2f
            }
            FixtureType.TOILET -> for (p in nearby.filter { it.mode == Mode.SEATED && it.holder == f.id }) {
                p.anim.face = Face.WOW; p.anim.faceTime = 1.5f; p.anim.hopV = 0.3f
            }
            else -> Unit
        }
    }

    fun catchBalls(place: PlaceId) {
        val iterator = passes.iterator()
        while (iterator.hasNext()) {
            val pass = iterator.next()
            val t = world.bodies[pass.key] as? Thing
            val p = world.bodies[pass.value] as? Person
            if (t == null || p == null || t.place != place || p.place != place || t.held || t.mode != Mode.FREE || p.held || world.worn(p, Slot.HAND) != null || t.cool <= 0f) { iterator.remove(); continue }
            val hand = Anatomy.at(p, Part.HAND)
            if (abs(t.x - hand[0]) < 0.095f && abs(t.y - hand[1]) < 0.12f) {
                sim.give(p, t, Part.HAND)
                p.anim.face = Face.GRIN; p.anim.faceTime = 2f; p.anim.wave = 1f
                iterator.remove()
            }
        }
    }

    companion object {
        const val READ = 1; const val PHONE = 2; const val BRUSH = 3; const val HUG = 4; const val MUSIC = 5; const val THROW = 6
        fun hand(activity: Int): FloatArray? = when (activity) {
            READ -> floatArrayOf(0.13f, -0.38f)
            PHONE -> floatArrayOf(0.22f, -0.73f)
            BRUSH -> floatArrayOf(0.07f, -0.58f)
            HUG -> floatArrayOf(0.10f, -0.36f)
            else -> null
        }
    }
}
