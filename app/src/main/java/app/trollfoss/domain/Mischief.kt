package app.trollfoss.domain

import kotlin.random.Random

enum class MischiefKind { SNEEZE, BIRD, CAT, TROLL }
data class TrollPeek(val fixtureId: Int, var time: Float)
data class BirdVisit(val personId: Int, var time: Float)

/**
 * Small funny surprises now and then while the child plays: a sneeze that blows a hat off, a bird on a head,
 * a cat after its tail, a troll peeking out of a cupboard. Figures that are held, asleep or riding are left alone,
 * and nothing here is an undo step: it is the world being silly, not the child.
 */
class Mischief(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    var enabled = false
    var paused = false
    /** Reduced motion: no bird or spinning cat, only the sneeze and the troll. */
    var calm = false
    var wait = next()
    var last: MischiefKind? = null; private set
    var troll: TrollPeek? = null; private set
    var bird: BirdVisit? = null; private set
    var catSpin: Int? = null; private set
    private var catTime = 0f

    private fun next() = MIN_WAIT + random.nextFloat() * (MAX_WAIT - MIN_WAIT)
    private fun free(place: PlaceId, species: Species) = world.people().filter {
        it.place == place && it.species == species && !it.held && it.mode == Mode.FREE && it.anim.pose != Pose.LIE
    }
    private fun hideouts(place: PlaceId) = world.fixturesIn(place).filter { it.spec.container != null && !it.open }

    fun step(place: PlaceId, dt: Float) {
        troll?.let { it.time -= dt; if (it.time <= 0f) troll = null }
        bird?.let { it.time -= dt; if (it.time <= 0f) bird = null }
        if (catSpin != null) { catTime -= dt; if (catTime <= 0f) catSpin = null }
        if (paused) return
        wait -= dt; if (wait > 0f) return
        wait = next()
        val kinds = MischiefKind.entries.filter { possible(it, place) }
        if (kinds.isNotEmpty()) force(kinds[random.nextInt(kinds.size)], place)
    }

    private fun possible(k: MischiefKind, place: PlaceId) = when (k) {
        MischiefKind.SNEEZE -> free(place, Species.FOLK).isNotEmpty()
        MischiefKind.BIRD -> !calm && free(place, Species.FOLK).isNotEmpty()
        MischiefKind.CAT -> !calm && free(place, Species.CAT).isNotEmpty()
        MischiefKind.TROLL -> hideouts(place).isNotEmpty()
    }

    fun force(k: MischiefKind, place: PlaceId) {
        if (!possible(k, place)) return
        last = k
        when (k) {
            MischiefKind.SNEEZE -> {
                val p = free(place, Species.FOLK).random(random)
                world.worn(p, Slot.HEAD)?.let { hat ->
                    hat.mode = Mode.FREE; hat.holder = -1; hat.slot = -1; hat.place = place
                    // With reduced motion the hat just drops; otherwise it hops off to the side.
                    hat.x = if (calm) p.x else p.x + 0.08f * p.anim.facing; hat.y = p.y - p.h; hat.ground = p.y
                    hat.vx = 0f; hat.vy = if (calm) 0f else -0.3f; hat.resting = false
                }
                p.anim.face = Face.WOW; p.anim.faceTime = 1.5f
                // The world sneezed, not the child: no task or sticker may come of it.
                val recording = sim.tasks.recording
                sim.tasks.recording = false
                try { sim.listener.onFx(Fx.ATSJO, p.x, p.y - p.h, param = p.id) } finally { sim.tasks.recording = recording }
            }
            MischiefKind.BIRD -> bird = BirdVisit(free(place, Species.FOLK).random(random).id, 4f)
            MischiefKind.CAT -> free(place, Species.CAT).random(random).let { cat -> catSpin = cat.id; catTime = 3f; cat.anim.spin = 1f; cat.anim.face = Face.DIZZY; cat.anim.faceTime = 2f }
            MischiefKind.TROLL -> { troll = TrollPeek(hideouts(place).random(random).id, 5f); sim.listener.onFx(Fx.TICK, 0f, 0f) }
        }
    }

    /** The child caught the peeking troll: it giggles and pops away. The first catch is a discovery. */
    fun tapTroll(): Boolean {
        val t = troll ?: return false
        troll = null
        val f = world.fixtures[t.fixtureId]
        sim.listener.onFx(Fx.POOF, f?.x ?: 0f, f?.top ?: 0f)
        sim.firstTime(First.PEEK_TROLL, f?.x ?: Float.NaN, f?.top ?: Float.NaN)
        return true
    }

    companion object { const val MIN_WAIT = 75f; const val MAX_WAIT = 120f }
}
