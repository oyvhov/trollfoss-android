package app.trollfoss.domain

import kotlin.math.floor
import kotlin.math.max

/**
 * The party room's rules. The jukebox plays five tunes (a tap on one of its five buttons) and sets the disco
 * ball going, so everybody who stands dances; the floor lights up tile by tile under dancing feet. The karaoke
 * screen wakes up when someone sings into the microphone, the snack bar hands out popcorn, fizz and cupcakes,
 * and the cannon fires confetti.
 */
internal class CellarParty(private val c: CellarCtx) : CellarPart {
    /** Seconds each tile of the dance floor still glows. */
    private val tiles = FloatArray(FLOOR_COLS * FLOOR_ROWS)
    private var scan = 0f
    private var partyTime = 0f
    private var lastMic = -99f

    override fun tap(f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.CE_JUKEBOX -> {
            press(f, dx, dy)
            true
        }
        FixtureType.DISCO_BALL -> {
            // While the jukebox plays, a tap on the ball ends the party; otherwise the ball works as it always does.
            val juke = c.fixture(CellarIx.JUKEBOX)
            if (juke != null && juke.mode > 0) {
                setTune(juke, 0)
                true
            } else {
                false
            }
        }
        FixtureType.MIC_STAND -> {
            lastMic = c.clock
            false
        }
        FixtureType.CE_DANCE_FLOOR -> {
            f.timer = WAVE_SECONDS
            c.fx(CellarCode.FLOOR_TAP, f.x + dx, f.y - 0.05f, f)
            true
        }
        FixtureType.CE_KARAOKE -> {
            f.on = !f.on
            c.fx(CellarCode.KARAOKE, f.x, f.y - 0.1f, f, arg = if (f.on) 1 else 0)
            true
        }
        FixtureType.CE_SNACK_BAR -> {
            serve(f, dx)
            true
        }
        FixtureType.CE_NEON -> {
            f.mode = 1 - f.mode
            f.on = f.mode == 0
            c.fx(CellarCode.SWITCH, f.x, f.y - 0.07f, f, arg = if (f.on) 1 else 0)
            true
        }
        FixtureType.CE_CONFETTI -> {
            fire(f)
            true
        }
        else -> false
    }

    override fun step(f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.CE_JUKEBOX -> play(f, dt)
            FixtureType.CE_DANCE_FLOOR, FixtureType.CE_CONFETTI -> f.timer = max(0f, f.timer - dt)
            FixtureType.CE_NEON -> f.on = f.mode == 0
            FixtureType.CE_KARAOKE -> {
                // Singing into the microphone wakes the screen: the ball bounces along to the song.
                val singing = c.clock - lastMic < SING_SECONDS
                f.mode = if (singing) 1 else 0
                if (singing && !f.on) {
                    f.on = true
                    c.fx(CellarCode.KARAOKE, f.x, f.y - 0.1f, f, arg = 1)
                }
            }
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ the jukebox

    /** A tap on the jukebox: the top cycles through the tunes, the five buttons below pick one (or stop it). */
    private fun press(f: Fixture, dx: Float, dy: Float) {
        val next = if (dy < -0.24f) {
            (f.mode + 1) % (CellarTunes.COUNT + 1)
        } else {
            val button = (((dx + f.spec.w * 0.4f) / (f.spec.w * 0.8f)) * CellarTunes.COUNT).toInt().coerceIn(0, CellarTunes.COUNT - 1) + 1
            if (button == f.mode) 0 else button
        }
        setTune(f, next)
    }

    /** Starts tune [tune] (1 to 5), or stops the music with 0. A playing jukebox keeps the disco ball turning. */
    fun setTune(f: Fixture, tune: Int) {
        val was = f.mode
        f.mode = tune
        f.timer = 0f
        f.angle = 0f
        partyTime = 0f
        val ball = c.fixture(CellarIx.DISCO_BALL)
        if (tune > 0) {
            ball?.on = true
            if (was == 0) c.sim.tasks.record(Deed.CE_DANCE, c.place, null, f.type)
            c.fx(CellarCode.JUKE_START, f.x, f.y - 0.3f, f, arg = tune)
        } else {
            if (ball?.on == true) {
                ball.on = false
                c.listener.onFx(Fx.DISCO, ball.x, ball.y, ball, param = 0)
            }
            c.fx(CellarCode.JUKE_STOP, f.x, f.y - 0.3f, f)
        }
    }

    private fun play(f: Fixture, dt: Float) {
        if (f.mode <= 0) {
            f.on = false
            return
        }
        f.on = true
        c.fixture(CellarIx.DISCO_BALL)?.on = true
        f.timer -= dt
        var guard = 0
        while (f.timer <= 0f && guard++ < 3) {
            val tune = f.mode - 1
            val length = CellarTunes.length(tune)
            val step = f.angle.toInt() % length
            c.fx(CellarCode.JUKE_NOTE, f.x, f.y - 0.3f, f, arg = tune * 64 + step)
            // The jukebox and the speakers pump on the kick.
            if (CellarTunes.drum(tune, step) and CellarTunes.KICK != 0) {
                f.anim = max(f.anim, 0.4f)
                for (ix in CellarIx.SPEAKERS) c.fixture(ix)?.let { it.anim = max(it.anim, 0.5f) }
            }
            f.angle = ((step + 1) % length).toFloat()
            f.timer += CellarTunes.stepSeconds(tune)
        }
    }

    // ------------------------------------------------------------------ the dance floor

    override fun tick(dt: Float) {
        val floor = c.fixture(CellarIx.DANCE_FLOOR) ?: return
        val ball = c.fixture(CellarIx.DISCO_BALL)
        val party = ball?.on == true
        floor.on = party
        for (i in tiles.indices) if (tiles[i] > 0f) tiles[i] = max(0f, tiles[i] - dt)
        scan -= dt
        if (scan <= 0f) {
            scan = SCAN_SECONDS
            if (party) light(floor)
        }
        var mask = 0
        for (i in tiles.indices) if (tiles[i] > 0f) mask = mask or (1 shl i)
        // The art reads the lit tiles as a bit mask held in a float (21 bits fit exactly).
        floor.angleV = mask.toFloat()
        val playing = (c.fixture(CellarIx.JUKEBOX)?.mode ?: 0) > 0
        if (party && playing && mask != 0) {
            partyTime += dt
            if (partyTime > 2f) c.sim.unlock("cellar_party")
        } else {
            partyTime = 0f
        }
    }

    /** Every figure that stands (and dances) on the floor lights the tile under its feet. */
    private fun light(floor: Fixture) {
        val w = floor.spec.w
        val depth = floor.spec.h
        for (b in c.world.bodiesIn(c.place)) {
            if (b !is Person || b.mode != Mode.FREE || !b.resting || b.held || b.anim.pose != Pose.STAND) continue
            val up = floor.y - c.sim.groundOf(c.place, b)
            if (up < 0f || up >= depth) continue
            val shift = up * RECEDE
            val col = floor((b.x - (floor.x - w / 2f) - shift) / (w / FLOOR_COLS)).toInt()
            val row = floor(up / depth * FLOOR_ROWS).toInt()
            if (col in 0 until FLOOR_COLS && row in 0 until FLOOR_ROWS) tiles[row * FLOOR_COLS + col] = GLOW_SECONDS
        }
    }

    // ------------------------------------------------------------------ bar and cannon

    private fun serve(f: Fixture, dx: Float) {
        f.count++
        val kind = when {
            dx < -0.08f -> 0
            dx > 0.08f -> 2
            else -> 1
        }
        val x = f.x + dx.coerceIn(-0.15f, 0.15f)
        val y = f.y - 0.26f
        val t = when (kind) {
            0 -> c.spawn(ThingType.POPCORN, 0, x, y, f, 0.15f, -1.4f, 100f)
            1 -> c.spawn(ThingType.SODA, f.count % 3, x, y, f, 0.1f, -1.5f, 120f)
            else -> c.spawn(if (f.count % 5 == 0) ThingType.PIZZA else ThingType.CUPCAKE, c.random.nextInt(4), x, y, f, 0.2f, -1.4f, 140f)
        }
        c.limit(t.type, MAX_FOOD)
        c.fx(CellarCode.BAR_POP, x, y, f, t, arg = kind)
    }

    private fun fire(f: Fixture) {
        if (f.timer > 0f) return
        f.timer = 1f
        for (p in c.persons()) {
            if (p.mode == Mode.FREE && !p.held && p.resting && kotlin.math.abs(p.x - f.x) < 0.9f) p.anim.hopV = 1.6f
        }
        c.fx(CellarCode.CONFETTI, f.x, f.y - 0.17f, f)
    }

    companion object {
        const val FLOOR_COLS = 7
        const val FLOOR_ROWS = 3
        const val GLOW_SECONDS = 0.8f
        const val SCAN_SECONDS = 0.1f
        const val WAVE_SECONDS = 2.4f
        const val SING_SECONDS = 4f
        const val MAX_FOOD = 6

        /** How far right a point of the floor moves per unit it goes up the screen (oblique depth). */
        const val RECEDE = 0.5f / 0.36f
    }
}

