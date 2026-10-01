package app.trollfoss.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * The two figures of the big house: Rolf the polite robot butler (Species.ROBOT) and Sture the shy,
 * friendly ghost (Species.GHOST). This file holds the small rules that only they follow; the pictures are
 * in `ui/art/PersonArtHouse.kt` (and Rolf and Sture files next to it) and the sounds and sparkle in
 * `ui/play/FigurarFx.kt`.
 */
object FigurarPose {
    /** A bow lasts this long; [PersonAnim.wave] counts it down. */
    const val BOW_SECONDS = 1.2f

    /** How far Rolf's head dips at the deepest point of a bow, as a fraction of his height. */
    const val DIP = 0.055f

    private fun smooth(x: Float): Float {
        val c = x.coerceIn(0f, 1f)
        return c * c * (3f - 2f * c)
    }

    /** From 0 (upright) to 1 (deepest bow) when a bow has [wave] seconds left: down quickly, hold, up slowly. */
    fun bow(wave: Float): Float = if (wave <= 0f) 0f else minOf(smooth((BOW_SECONDS - wave) / 0.2f), smooth(wave / 0.5f))

    /** The dip of Rolf's head (fraction of height), for hats and glasses to follow. Only standing or sitting figures bow. */
    fun dip(pose: Pose, wave: Float): Float = if (pose == Pose.STAND || pose == Pose.SIT) bow(wave) * DIP else 0f
}

/** What [Fx.FIGURAR] says: the code and the figure's id travel together in the event's param. */
object FigurarEvent {
    /** Rolf greets someone with a bow. */
    const val BOW = 1

    /** Sture starts a dusty sneeze: «ah … ah …». */
    const val ACHOO = 2

    /** … and the dust flies. */
    const val DUST = 3

    fun pack(code: Int, id: Int): Int = (code shl 24) or (id and 0xFFFFFF)
    fun code(param: Int): Int = param ushr 24
    fun id(param: Int): Int = param and 0xFFFFFF
}

class Figurar(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world

    /** Seconds until a robot may greet again, by figure id. Never saved. */
    private val greetWait = HashMap<Int, Float>()

    /** Called every frame for every figure in the place on screen. */
    fun step(place: PlaceId, p: Person, people: List<Person>, dt: Float) {
        when (p.species) {
            Species.ROBOT -> greet(place, p, people, dt)
            Species.GHOST -> dust(p, dt)
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ Rolf: a polite bow

    /** Rolf bows to anyone who comes up to him. Afterwards he leaves them in peace for a while. */
    private fun greet(place: PlaceId, p: Person, people: List<Person>, dt: Float) {
        val a = p.anim
        val wait = greetWait[p.id] ?: 0f
        if (wait > 0f) {
            greetWait[p.id] = wait - dt
            return
        }
        if (p.held || p.mode != Mode.FREE || a.pose != Pose.STAND || a.wave > 0f || a.face == Face.SLEEP || !p.resting) return
        val depth = sim.groundOf(place, p)
        val guest = people.firstOrNull {
            it.species == Species.FOLK && !it.held && it.mode != Mode.BAG && it.anim.face != Face.SLEEP &&
                abs(it.x - p.x) < GREET_REACH && abs(depthOf(place, it) - depth) < 0.12f
        } ?: return
        bow(p)
        greetWait[p.id] = GREET_PAUSE + random.nextFloat() * 10f
        // The guest waves back.
        guest.anim.wave = maxOf(guest.anim.wave, 1.1f)
    }

    private fun depthOf(place: PlaceId, p: Person): Float =
        if (p.mode == Mode.SEATED) world.fixtures[p.holder]?.depth ?: p.y else sim.groundOf(place, p)

    /** A polite little bow, with a ding. */
    fun bow(p: Person) {
        p.anim.wave = FigurarPose.BOW_SECONDS
        fire(FigurarEvent.BOW, p)
    }

    // ------------------------------------------------------------------ Sture: dust

    /** Now and then, in the dust of the old house, Sture has to sneeze. It is only dust: nobody loses a hat. */
    private fun dust(p: Person, dt: Float) {
        val a = p.anim
        if (a.achoo > 0f) {
            a.achoo -= dt
            if (a.achoo <= 0f) {
                a.achoo = 0f
                fire(FigurarEvent.DUST, p)
            }
            return
        }
        if (p.held || p.mode == Mode.BAG || a.face == Face.SLEEP || a.sneeze > 0f) return
        if (random.nextFloat() < dt / SNEEZE_EVERY) sneeze(p)
    }

    /** «Ah … ah …» now, dust in a moment. Used by Sture himself and by anything in the house that tickles his nose. */
    fun sneeze(p: Person) {
        if (p.anim.achoo > 0f) return
        p.anim.achoo = Jokes.SNEEZE_DELAY
        fire(FigurarEvent.ACHOO, p)
    }

    private fun fire(code: Int, p: Person) {
        sim.listener.onFx(Fx.FIGURAR, p.x, p.y - p.h * 0.7f, param = FigurarEvent.pack(code, p.id))
    }

    // ------------------------------------------------------------------ food

    /**
     * Food at Rolf's mouth is «fuel»: he swallows it whole into his tank (and is very polite about it).
     * A robot gets no bites and no crumbs; the engine plays the joke.
     */
    fun fuel(p: Person, t: Thing): Give {
        p.anim.chew = 0f
        sim.removeThing(t, quiet = true)
        return Give.FINISHED
    }

    /** Food at Sture's mouth is sniffed with great puzzlement and handed kindly back, with a little toss. */
    fun sniff(p: Person, t: Thing): Give {
        t.held = false
        t.mode = Mode.FREE
        t.holder = -1
        t.inside = -1
        t.resting = false
        t.restOwner = -2
        p.place?.let { t.ground = sim.groundOf(it, p) + 0.02f }
        t.vy = -1.4f
        t.vx = if (t.x >= p.x) 0.55f else -0.55f
        t.vrot = t.vx * 300f
        return Give.SNIFF
    }

    companion object {
        /** How close a guest must come for Rolf to greet them, in scene units. */
        const val GREET_REACH = 0.4f

        /** Seconds of peace after a greeting (plus up to ten more). */
        const val GREET_PAUSE = 18f

        /** On average, Sture sneezes once in so many seconds. */
        const val SNEEZE_EVERY = 75f
    }
}
