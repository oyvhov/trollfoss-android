package app.trollfoss.domain

import kotlin.math.abs

/** Level 8 «Showmeister»: an echo box, a dance floor, a confetti machine and a light rig. Nothing here is saved but the rig's mode. */
class ShowPlay(private val sim: Sim) {
    private val world get() = sim.world

    private class Note(val kind: Int, val pitch: Int, val at: Float)
    private class Playback(val notes: List<Note>, var index: Int = 0, var wait: Float = 0f)

    /** What each echo box has heard, newest last. */
    private val recordings = HashMap<Int, ArrayDeque<Note>>()
    private val playing = HashMap<Int, Playback>()
    private val rest = HashMap<Int, Float>()

    fun recorded(f: Fixture): Int = recordings[f.id]?.size ?: 0

    fun tap(f: Fixture): Boolean {
        when (f.type) {
            FixtureType.PLAY_ECHO_BOX -> play(f)
            FixtureType.PLAY_DANCE_FLOOR -> { f.on = !f.on; f.timer = 0f; sim.listener.onFx(Fx.DISCO, f.x, f.top, f, param = if (f.on) 1 else 0) }
            FixtureType.PLAY_CONFETTI -> confetti(f)
            FixtureType.PLAY_LIGHT_RIG -> {
                val was = f.mode
                f.mode = (f.mode + 1) % 3; f.on = f.mode > 0; f.timer = 0f
                when (f.mode) {
                    1 -> { sim.listener.onFx(Fx.ON, f.x, f.top, f); sim.firstTime(First.LIGHT_RIG, f.x, f.top) }
                    2 -> sim.listener.onFx(Fx.DISCO, f.x, f.top, f, param = 1)
                    else -> sim.listener.onFx(if (was == 2) Fx.DISCO else Fx.OFF, f.x, f.top, f, param = 0)
                }
            }
            else -> return false
        }
        f.anim = 1f
        return true
    }

    fun step(f: Fixture, dt: Float): Boolean {
        when (f.type) {
            FixtureType.PLAY_ECHO_BOX -> playing[f.id]?.let { pb ->
                pb.wait -= dt
                if (pb.wait > 0f) return true
                val note = pb.notes[pb.index]
                f.anim = 1f
                sim.listener.onFx(Fx.ECHO_NOTE, f.x, f.top, f, param = echoNote(note.kind, note.pitch))
                pb.index++
                if (pb.index >= pb.notes.size) { playing.remove(f.id); cheer(f.place, f.x) }
                else pb.wait = (pb.notes[pb.index].at - note.at).coerceIn(0.12f, 0.8f)
            }
            FixtureType.PLAY_DANCE_FLOOR -> if (f.on) {
                val dancers = world.people().filter { onFloor(it, f) }
                if (dancers.isNotEmpty()) sim.firstTime(First.DANCE_FLOOR, f.x, f.top)
                f.timer += dt
                // Two or more on the floor hop together on the beat.
                if (f.timer >= BEAT) { f.timer = 0f; if (dancers.size >= 2) dancers.forEach { if (it.anim.hop <= 0f) it.anim.hopV = 1.2f } }
            }
            FixtureType.PLAY_CONFETTI -> rest[f.id]?.let { rest[f.id] = it - dt }
            FixtureType.PLAY_LIGHT_RIG -> if (f.mode == 2) f.angle += dt * 1.5f else if (f.mode == 1) {
                f.timer += dt
                if (f.timer >= 3f) {
                    f.timer = 0f
                    // Figures in the warm spotlight smile and wave, as on a stage.
                    world.people().filter { it.place == f.place && free(it) && it.x in (f.x + 0.1f)..(f.x + 0.7f) }
                        .forEach { it.anim.face = Face.GRIN; it.anim.faceTime = 2f; it.anim.wave = 1f }
                }
            }
            else -> return false
        }
        return true
    }

    /** Notes played close to an echo box are remembered: at most [MAX_NOTES], and never its own playback. */
    fun heard(fx: Fx, x: Float, place: PlaceId, param: Int) {
        val (kind, pitch) = when (fx) {
            Fx.KEY -> 0 to param.coerceIn(0, 9)
            Fx.XYLO -> 0 to (param + 2).coerceIn(0, 9)
            Fx.STRUM -> 0 to 5 + abs(param) % 4
            Fx.SING -> 0 to 7
            Fx.DRUM -> 1 to abs(param) % 3
            else -> return
        }
        for (f in world.fixturesIn(place)) {
            if (f.type != FixtureType.PLAY_ECHO_BOX || f.id in playing || abs(f.x - x) > HEARING) continue
            val notes = recordings.getOrPut(f.id) { ArrayDeque() }
            notes.addLast(Note(kind, pitch, sim.time))
            while (notes.size > MAX_NOTES) notes.removeFirst()
        }
    }

    /** Is [p] dancing on a dance floor that is on? Held, seated or sleeping figures never are. */
    fun onDanceFloor(p: Person): Boolean {
        val place = p.place ?: return false
        return world.fixturesIn(place).any { it.type == FixtureType.PLAY_DANCE_FLOOR && onFloor(p, it) }
    }

    /** The dance floors in [place] that are on, for the engine to check each figure against once per frame. */
    fun floorsOn(place: PlaceId): List<Fixture> = world.fixturesIn(place).filter { it.type == FixtureType.PLAY_DANCE_FLOOR && it.on }

    fun onFloor(p: Person, f: Fixture): Boolean = f.on && p.place == f.place && free(p) && abs(p.x - f.x) < f.spec.w / 2

    private fun free(p: Person) = p.mode == Mode.FREE && !p.held && p.anim.pose != Pose.LIE

    private fun play(f: Fixture) {
        val notes = recordings[f.id]?.toList().orEmpty()
        if (notes.isEmpty() || f.id in playing) { sim.listener.onFx(Fx.SQUEAK, f.x, f.top, f); return }
        playing[f.id] = Playback(notes)
        sim.firstTime(First.ECHO_BOX, f.x, f.top)
    }

    private fun confetti(f: Fixture) {
        if ((rest[f.id] ?: 0f) > 0f) { sim.listener.onFx(Fx.BONK, f.x, f.top, f); return }
        rest[f.id] = 2f
        sim.listener.onFx(Fx.CONFETTI, f.x, f.top, f)
        world.people().filter { it.place == f.place && free(it) && abs(it.x - f.x) < 1f }
            .forEach { it.anim.hopV = 1.6f; it.anim.face = Face.LAUGH; it.anim.faceTime = 2f }
        sim.firstTime(First.CONFETTI, f.x, f.top)
    }

    private fun cheer(place: PlaceId, x: Float) {
        world.people().filter { it.place == place && free(it) && abs(it.x - x) < 1.2f }.forEach { it.anim.cheer = 1f }
    }

    companion object {
        val TYPES = setOf(FixtureType.PLAY_ECHO_BOX, FixtureType.PLAY_DANCE_FLOOR, FixtureType.PLAY_CONFETTI, FixtureType.PLAY_LIGHT_RIG)
        const val MAX_NOTES = 8
        const val HEARING = 1.5f
        const val BEAT = 0.8f
        fun echoNote(kind: Int, pitch: Int): Int = kind * 16 + pitch
        fun echoKind(param: Int): Int = param / 16
        fun echoPitch(param: Int): Int = param % 16
    }
}
