package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * The rules of the attic: everything that moves, opens, rings or hides up here. Sture, the shy ghost, is in
 * [AtticSture]; this class is the furniture. Events go out as `Fx.HOUSE` with a code from [AtticCode], and
 * `AtticFx` (ui/play/HouseAtticFx.kt) turns them into sound and sparkle.
 */
class HouseAtticRules(private val sim: Sim, private val random: Random) : FloorRules {
    private val world get() = sim.world
    private val place = PlaceId.MANOR_ATTIC
    private val sture = AtticSture(sim, random)
    private var started = false

    /** Gramophone state: the record is on the platter, [Fixture.mode] is its number plus one, `on` is playing. */
    private var danceIn = 0f

    // ------------------------------------------------------------------ ticking

    override fun tick(place: PlaceId, dt: Float) {
        if (!started) {
            started = true
            firstTime()
        }
        sture.tick(dt)
        danceIn -= dt
        if (danceIn <= 0f) {
            danceIn = 0.25f
            dance()
        }
    }

    /** The first moment on this floor: the lamps are lit (once; after that the child decides) and flaps are shut. */
    private fun firstTime() {
        if ("attic_lit" !in world.flags) {
            world.flags += "attic_lit"
            for (f in world.fixturesIn(place)) if (f.type in LAMPS) f.on = true
        }
        // A sheet or flap left up by a saved game falls back (trunks and chests stay as the child left them).
        for (f in world.fixturesIn(place)) {
            if (f.type in HIDERS && f.type != FixtureType.AT_TRUNK && f.open && f.spec.spots.indices.none { world.seatedAt(f, it) != null }) {
                f.open = false
                sim.invalidate(place)
            }
        }
    }

    override fun step(place: PlaceId, f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.AT_ROCKING_HORSE -> horseStep(f, dt)
            FixtureType.AT_TRUNK, FixtureType.AT_CARTON, FixtureType.AT_SHEETED, FixtureType.AT_BLANKET_FORT,
            FixtureType.AT_TREASURE_CHEST -> autoClose(f, dt)
            FixtureType.AT_GRAMOPHONE -> recordStep(f, dt)
            FixtureType.AT_BOOK_TOWER -> if (f.mode == 1) {
                f.timer -= dt
                if (f.timer <= 0f) {
                    f.mode = 0
                    f.count++
                    fx(AtticCode.BOOKS_BACK, f.x, f.y - 0.15f, f, arg = 1)
                }
            }
            FixtureType.AT_OWL_HOLE -> if (f.mode == 1) {
                f.timer -= dt
                if (f.timer <= 0f) f.mode = 0
            }
            FixtureType.AT_FAMILY_TREE -> if (f.mode != 0) {
                f.timer -= dt
                if (f.timer <= 0f) f.mode = 0
            }
            FixtureType.AT_WEATHER_VANE -> spin(f, dt, 0.9f)
            FixtureType.AT_GLOBE -> spin(f, dt, 0.7f)
            FixtureType.AT_CHANDELIER -> swing(f, dt)
            FixtureType.AT_STAR_MAP, FixtureType.AT_MAP_TABLE -> if (f.mode != 0) {
                // A map or a chart left alone for a long while rolls itself up again.
                f.timer += dt
                if (f.timer > IDLE_ROLL) {
                    f.timer = 0f
                    f.mode = 0
                }
            }
            FixtureType.SECRET_DOOR -> {
                val passage = House.passageAt(f)
                val open = if (passage == null || House.usable(world, passage)) 1 else 0
                if (f.mode != open) f.mode = open
            }
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ taps

    override fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.STAIRCASE, FixtureType.SECRET_DOOR -> {
            // Whoever leaves the attic may leave Sture hiding in it. The way itself is the house's business.
            sture.beforeLeaving()
            false
        }
        FixtureType.AT_SHEETED, FixtureType.AT_CARTON, FixtureType.AT_BLANKET_FORT -> lift(f)
        FixtureType.AT_TRUNK -> trunk(f)
        FixtureType.AT_ROCKING_HORSE -> horse(f)
        FixtureType.AT_SPIDER -> spider(f)
        FixtureType.AT_ROUND_WINDOW -> window(f)
        FixtureType.AT_WING_CHAIR -> {
            f.count++
            fx(AtticCode.CHAIR, f.x, f.y - 0.1f, f)
            true
        }
        FixtureType.AT_GRAMOPHONE -> gramophone(f)
        FixtureType.AT_BOOK_TOWER -> bookTower(f)
        FixtureType.AT_SHADOW_THEATRE -> {
            f.mode = (f.mode + 1) % SHADOW_ANIMALS
            fx(AtticCode.SHADOWS, f.x, f.y - 0.12f, f, arg = f.mode)
            sture.scareNear(f.x, 1.9f)
            true
        }
        FixtureType.AT_GRANDFATHER -> clock(f)
        FixtureType.AT_STAR_MAP -> starMap(f)
        FixtureType.AT_WEATHER_VANE -> {
            f.angleV += 9f + random.nextFloat() * 4f
            fx(AtticCode.VANE, f.x, f.y - 0.15f, f)
            true
        }
        FixtureType.AT_ARMILLARY -> {
            f.on = !f.on
            f.timer = 2.5f
            fx(AtticCode.ARMILLARY, f.x, f.y - 0.18f, f, arg = if (f.on) 1 else 0)
            true
        }
        FixtureType.AT_BAROMETER -> {
            f.mode = (f.mode + 1) % 3
            fx(AtticCode.BAROMETER, f.x, f.y - 0.1f, f, arg = f.mode)
            true
        }
        FixtureType.AT_OWL_HOLE -> {
            f.mode = 1
            f.timer = 2.8f
            fx(AtticCode.OWL, f.x, f.y - 0.1f, f)
            true
        }
        FixtureType.TELESCOPE -> telescope(f)
        FixtureType.AT_MAP_TABLE -> mapTable(f)
        FixtureType.AT_TREASURE_CHEST -> chest(f)
        FixtureType.AT_GLOBE -> {
            f.angleV += 6f + random.nextFloat() * 3f
            f.count++
            f.timer = 0f
            fx(AtticCode.GLOBE, f.x, f.y - 0.15f, f, arg = 0)
            true
        }
        FixtureType.AT_FAMILY_TREE -> familyTree(f, dx, dy)
        FixtureType.AT_CHANDELIER -> {
            f.on = !f.on
            f.angleV += 2.2f
            fx(AtticCode.CHANDELIER, f.x, f.y, f, arg = if (f.on) 1 else 0)
            true
        }
        FixtureType.AT_LANTERN, FixtureType.AT_CANDELABRA, FixtureType.AT_STRING_LIGHTS, FixtureType.AT_SCONCE -> {
            f.on = !f.on
            fx(AtticCode.LIGHT, f.x, f.y - f.spec.h * 0.8f, f, arg = if (f.on) 1 else 0)
            // A ghost who sees his shadow jump out from a freshly lit candle is a ghost who jumps.
            if (f.on) sture.scareNear(f.x, 0.5f)
            true
        }
        else -> false
    }

    // ------------------------------------------------------------------ drops

    override fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean = when {
        f.type == FixtureType.AT_GRAMOPHONE && t.type == ThingType.AT_RECORD -> {
            val old = f.mode - 1
            sim.removeThing(t, quiet = true)
            if (old in 0 until AtticIds.RECORDS) {
                val back = toss(ThingType.AT_RECORD, old, f, 0.08f, -0.2f, 0.5f, -1.8f)
                back.vrot = 300f
            }
            play(f, t.variant.mod(AtticIds.RECORDS))
            true
        }
        f.type == FixtureType.AT_ROCKING_HORSE && t.type.fruit -> {
            sim.removeThing(t, quiet = true)
            f.angleV += 4.5f
            f.on = true
            fx(AtticCode.HORSE, f.x, f.y - 0.2f, f, arg = 2)
            true
        }
        else -> false
    }

    // ------------------------------------------------------------------ seats

    /** The rocking horse carries its rider along with it: the rockers roll, so the saddle swings round their middle. */
    override fun seatPoint(f: Fixture, spot: Int): FloatArray? {
        if (f.type != FixtureType.AT_ROCKING_HORSE) return null
        val s = f.spec.spots[spot]
        val a = f.angle
        val c = cos(a)
        val n = sin(a)
        val r = HORSE_RADIUS
        val dy = s.dy + r
        return floatArrayOf(f.x + f.shiftX + r * a + s.dx * c - dy * n, f.y + f.shiftY - r + s.dx * n + dy * c)
    }

    // ------------------------------------------------------------------ hiding places

    /** The sheet lifts, the flap opens, the lid pops: and perhaps Sture was in there. */
    private fun lift(f: Fixture): Boolean {
        val index = place.indexOf(f.id)
        if (sture.hidingIn(f)) {
            f.open = true
            f.timer = REVEAL
            sim.invalidate(place)
            sture.found(f)
            return true
        }
        if (f.open) {
            if (f.spec.spots.indices.any { world.seatedAt(f, it) != null }) return true
            shut(f)
            return true
        }
        f.open = true
        f.timer = REVEAL
        f.count++
        sim.invalidate(place)
        when (f.type) {
            FixtureType.AT_CARTON -> {
                // An old toy springs out on a spring.
                val toy = OLD_TOYS[random.nextInt(OLD_TOYS.size)]
                val t = toss(toy.first, random.nextInt(toy.first.variants), f, 0f, -0.2f, (random.nextFloat() - 0.5f) * 0.9f, -2.6f)
                t.vrot = (random.nextFloat() - 0.5f) * 500f
                trim(MAX_TOYS, TOY_TYPES)
                fx(AtticCode.CARTON, f.x, f.y - 0.22f, f, t)
            }
            FixtureType.AT_SHEETED -> {
                // Dust and nothing else, mostly. Every fourth sheet has a hat that nobody has missed.
                val surprise = f.count % 4 == 0
                if (surprise) {
                    val hat = HATS[random.nextInt(HATS.size)]
                    val t = toss(hat, random.nextInt(hat.variants), f, 0f, -0.2f, 0.45f, -1.9f)
                    t.vrot = 240f
                    trim(MAX_TOYS, TOY_TYPES)
                }
                fx(AtticCode.SHEET, f.x, f.y - 0.15f, f, arg = if (surprise) 1 else 0)
                // Dust in the nose: whoever stands close sneezes.
                world.bodiesIn(place).filterIsInstance<Person>()
                    .filter { it.species == Species.FOLK && !it.held && it.mode == Mode.FREE && abs(it.x - f.x) < 0.45f }
                    .minByOrNull { abs(it.x - f.x) }
                    ?.takeIf { random.nextFloat() < 0.4f }
                    ?.let { sim.jokes.pepper(it) }
            }
            else -> fx(AtticCode.FORT, f.x, f.y - 0.1f, f, arg = 1)
        }
        return true
    }

    private fun shut(f: Fixture) {
        f.open = false
        f.timer = 0f
        sim.invalidate(place)
        when (f.type) {
            FixtureType.AT_BLANKET_FORT -> fx(AtticCode.FORT, f.x, f.y - 0.1f, f, arg = 0)
            FixtureType.AT_TRUNK -> fx(AtticCode.TRUNK_SHUT, f.x, f.y - 0.1f, f)
            FixtureType.AT_TREASURE_CHEST -> fx(AtticCode.CHEST, f.x, f.y - 0.1f, f, arg = 1)
            else -> fx(AtticCode.SHEET, f.x, f.y - 0.12f, f, arg = 2)
        }
    }

    /** Sheets, flaps and lids fall shut by themselves a moment later, unless a figure has climbed inside. */
    private fun autoClose(f: Fixture, dt: Float) {
        if (!f.open || f.timer <= 0f) return
        if (f.spec.spots.indices.any { world.seatedAt(f, it) != null }) return
        f.timer -= dt
        if (f.timer <= 0f) shut(f)
    }

    // ------------------------------------------------------------------ the costume trunk

    private fun trunk(f: Fixture): Boolean {
        if (sture.hidingIn(f)) {
            f.open = true
            f.timer = REVEAL + 1f
            sim.invalidate(place)
            sture.found(f)
            tossCostume(f, 0)
            return true
        }
        if (f.open) {
            shut(f)
            return true
        }
        f.open = true
        f.timer = REVEAL + 1.2f
        sim.invalidate(place)
        val set = f.count % AtticCostumes.sets.size
        f.count++
        tossCostume(f, set)
        sim.tasks.record(Deed.AT_COSTUME, place, fixture = f.type)
        // Three trunk-fuls and a star floats out of the lid.
        if (f.count >= 3) sim.unlock("attic_trunk")
        return true
    }

    /** Tosses the pieces of costume [set] out of the trunk, one after the other in a fountain. */
    private fun tossCostume(f: Fixture, set: Int) {
        val pieces = AtticCostumes.sets[set]
        fx(AtticCode.TRUNK, f.x, f.y - 0.18f, f, arg = set)
        for ((i, piece) in pieces.withIndex()) {
            val side = if (pieces.size == 1) 0f else (i - (pieces.size - 1) / 2f)
            val t = toss(piece.type, piece.variant, f, side * 0.04f, -0.16f, side * 0.45f + (random.nextFloat() - 0.5f) * 0.2f, -2.5f - i * 0.25f)
            t.vrot = side * 220f + 90f
        }
        trim(MAX_COSTUMES, AtticCostumes.types)
    }

    // ------------------------------------------------------------------ the rocking horse

    private fun horse(f: Fixture): Boolean {
        f.on = true
        val gallop = f.taps >= 4
        f.angleV += if (gallop) 5.5f else 3.2f
        fx(AtticCode.HORSE, f.x, f.y - 0.15f, f, arg = if (gallop) 1 else 0)
        if (gallop) {
            f.taps = 0
            // The hat on the sheet beside it hops with the gallop.
            for (b in world.bodiesIn(place)) {
                if (b is Thing && b.mode == Mode.FREE && !b.held && b.resting && abs(b.x - f.x) < 0.6f) {
                    b.resting = false
                    b.restOwner = -2
                    b.vy = -0.9f
                }
            }
        }
        return true
    }

    private fun horseStep(f: Fixture, dt: Float) {
        if (!f.on) return
        f.angleV += (-f.angle * 46f - f.angleV * 1.5f) * dt
        f.angle = (f.angle + f.angleV * dt).coerceIn(-0.42f, 0.42f)
        // Moving art is drawn live while it moves (see Engine.stampFixture).
        f.anim = max(f.anim, 0.012f)
        if (abs(f.angle) < 0.006f && abs(f.angleV) < 0.05f) {
            f.angle = 0f
            f.angleV = 0f
            f.on = false
        }
    }

    // ------------------------------------------------------------------ spiders

    private fun spider(f: Fixture): Boolean {
        f.mode += 1
        if (f.mode >= 4) {
            f.mode = 0
            f.count++
            val t = toss(ThingType.GARMENT, Garment.pack(5, random.nextInt(Palette.cloth.size)), f, 0f, 0f, 0.35f, 0.1f)
            t.ground = place.floor - 0.015f
            t.vrot = 180f
            trim(MAX_COSTUMES, AtticCostumes.types)
            fx(AtticCode.SPIDER, f.x, f.y - 0.1f, f, t, arg = 4)
            sim.tasks.record(Deed.AT_KNIT, place, ThingType.GARMENT, f.type)
        } else {
            fx(AtticCode.SPIDER, f.x, f.y - 0.1f, f, arg = f.mode)
        }
        return true
    }

    // ------------------------------------------------------------------ the window

    private fun window(f: Fixture): Boolean {
        f.on = !f.on
        fx(AtticCode.WINDOW, f.x, f.y - 0.12f, f, arg = if (f.on) 1 else 0)
        // The draught makes every sheet in the attic shiver.
        for (o in world.fixturesIn(place)) if (o.type == FixtureType.AT_SHEETED) o.anim = 1f
        return true
    }

    // ------------------------------------------------------------------ the gramophone

    private fun gramophone(f: Fixture): Boolean {
        val next = if (f.mode <= 0) 0 else f.mode % AtticIds.RECORDS
        play(f, next)
        return true
    }

    /** Puts record [record] on the platter and lets it play: the tune is in `AtticFx`, its length in [AtticTunes]. */
    private fun play(f: Fixture, record: Int) {
        f.mode = record + 1
        f.on = true
        f.timer = AtticTunes.seconds[record]
        f.count = f.count or (1 shl record)
        fx(AtticCode.RECORD, f.x, f.y - 0.2f, f, arg = record)
        sim.tasks.record(Deed.AT_RECORD, place, ThingType.AT_RECORD, f.type)
        if (Integer.bitCount(f.count) >= 4) sim.unlock("attic_music")
    }

    private fun recordStep(f: Fixture, dt: Float) {
        if (!f.on) return
        f.timer -= dt
        // The stuck record skips every couple of seconds, then the needle hops out.
        if (f.mode - 1 == STUCK_RECORD && f.timer > 0f) {
            f.angle += dt
            if (f.angle >= 1.9f) {
                f.angle = 0f
                fx(AtticCode.SKIP, f.x, f.y - 0.2f, f)
            }
        }
        if (f.timer <= 0f) {
            f.on = false
            f.angle = 0f
            fx(AtticCode.RECORD_STOP, f.x, f.y - 0.2f, f)
        }
    }

    /** While a record plays, everybody near it dances. */
    private fun dance() {
        val player = world.fixturesIn(place).firstOrNull { it.type == FixtureType.AT_GRAMOPHONE && it.on } ?: return
        for (b in world.bodiesIn(place)) {
            if (b !is Person || b.held || b.mode != Mode.FREE || !b.resting) continue
            if (abs(b.x - player.x) < 1.7f) b.anim.cheer = max(b.anim.cheer, 0.35f)
        }
    }

    // ------------------------------------------------------------------ book tower, clock, star map, telescope

    private fun bookTower(f: Fixture): Boolean {
        if (f.mode == 1) {
            fx(AtticCode.BOOKS, f.x, f.y - 0.2f, f, arg = 0)
            return true
        }
        f.mode = 1
        f.timer = 2.4f
        val t = toss(ThingType.BOOK, random.nextInt(ThingType.BOOK.variants), f, 0.03f, -0.3f, 0.7f, -1.2f)
        t.vrot = 260f
        trim(MAX_BOOKS, setOf(ThingType.BOOK))
        fx(AtticCode.BOOKS, f.x, f.y - 0.2f, f, t, arg = 0)
        return true
    }

    private fun clock(f: Fixture): Boolean {
        f.count++
        val strokes = 1 + (f.count - 1) % 5
        fx(AtticCode.CLOCK, f.x, f.y - 0.3f, f, arg = strokes)
        sture.scareNear(f.x, 2.2f)
        return true
    }

    private fun starMap(f: Fixture): Boolean {
        f.timer = 0f
        f.mode += 1
        if (f.mode >= CONSTELLATIONS) {
            f.mode = 0
            fx(AtticCode.STARS_DONE, f.x, f.y - 0.15f, f)
            sim.unlock("attic_stars")
        } else {
            fx(AtticCode.STARS, f.x, f.y - 0.15f, f, arg = f.mode - 1)
        }
        return true
    }

    private fun telescope(f: Fixture): Boolean {
        f.count++
        sim.listener.onFx(Fx.LOOK, f.x, f.top, f)
        sim.tasks.record(Deed.AT_STARGAZE, place, fixture = f.type)
        // Look twice, or look at night, and a star drifts out of the sky.
        if (world.night || f.count >= 2) sim.unlock("attic_tower")
        return true
    }

    // ------------------------------------------------------------------ the secret room

    private fun mapTable(f: Fixture): Boolean {
        f.timer = 0f
        f.mode += 1
        if (f.mode > 4) {
            f.mode = 0
            fx(AtticCode.MAP, f.x, f.y - 0.2f, f, arg = 0)
            return true
        }
        fx(AtticCode.MAP, f.x, f.y - 0.2f, f, arg = f.mode)
        if (f.mode == 4) {
            // The green star on the map was a gem all along.
            val t = toss(ThingType.GEM, random.nextInt(ThingType.GEM.variants), f, 0f, -0.2f, 0.4f, -1.9f)
            t.vrot = 200f
            trim(MAX_GEMS, setOf(ThingType.GEM))
        }
        return true
    }

    private fun chest(f: Fixture): Boolean {
        if (f.open) {
            shut(f)
            return true
        }
        f.open = true
        f.timer = 3.4f
        f.count++
        sim.invalidate(place)
        for (i in 0 until 6) {
            val t = toss(ThingType.COIN, 0, f, (i - 2.5f) * 0.02f, -0.14f, (i - 2.5f) * 0.22f + (random.nextFloat() - 0.5f) * 0.1f, -2.2f - random.nextFloat() * 0.9f)
            t.vrot = (random.nextFloat() - 0.5f) * 500f
        }
        if (f.count % 3 == 1) toss(ThingType.GEM, random.nextInt(ThingType.GEM.variants), f, 0f, -0.14f, 0.2f, -2.8f)
        trim(MAX_COINS, setOf(ThingType.COIN))
        trim(MAX_GEMS, setOf(ThingType.GEM))
        fx(AtticCode.CHEST, f.x, f.y - 0.2f, f, arg = 0)
        sim.unlock("attic_secret")
        return true
    }

    private fun familyTree(f: Fixture, dx: Float, dy: Float): Boolean {
        // Six portraits in two rows of three; the tap picks the nearest.
        val col = (((dx + f.spec.w / 2f) / f.spec.w) * 3f).toInt().coerceIn(0, 2)
        val row = if (dy < -f.spec.h / 2f) 0 else 1
        val which = col + row * 3
        f.mode = which + 1
        f.timer = 1.5f
        f.count = f.count or (1 shl which)
        fx(AtticCode.TREE, f.x, f.y - 0.2f, f, arg = which)
        if (f.count == (1 shl PORTRAITS) - 1) {
            f.count = 0
            fx(AtticCode.TREE_DONE, f.x, f.y - 0.2f, f)
            sim.unlock("attic_tree")
        }
        return true
    }

    // ------------------------------------------------------------------ spinning and swinging

    /** A thing that spins on its own axis: the weather vane and the globe. */
    private fun spin(f: Fixture, dt: Float, drag: Float) {
        if (abs(f.angleV) < 0.01f) {
            f.angleV = 0f
            return
        }
        f.angle += f.angleV * dt
        f.angleV *= exp(-drag * dt)
        f.anim = max(f.anim, 0.012f)
        if (abs(f.angleV) < 0.35f) {
            f.angleV = 0f
            if (f.type == FixtureType.AT_GLOBE) fx(AtticCode.GLOBE, f.x, f.y - 0.15f, f, arg = 1 + (((f.angle % 6.2832f) + 6.2832f) % 6.2832f / 6.2832f * 6f).toInt().coerceIn(0, 5))
        }
    }

    private fun swing(f: Fixture, dt: Float) {
        if (abs(f.angleV) < 0.01f && abs(f.angle) < 0.004f) {
            f.angle = 0f
            f.angleV = 0f
            return
        }
        f.angleV += (-f.angle * 18f - f.angleV * 0.7f) * dt
        f.angle += f.angleV * dt
        f.anim = max(f.anim, 0.012f)
    }

    // ------------------------------------------------------------------ helpers

    private fun fx(code: Int, x: Float, y: Float, f: Fixture? = null, t: Thing? = null, arg: Int = 0) {
        sim.listener.onFx(Fx.HOUSE, x, y, f, t, HouseFx.pack(code, arg))
    }

    /** Throws a new thing out of [f]: it lands in front of it. */
    private fun toss(type: ThingType, variant: Int, f: Fixture, dx: Float, dy: Float, vx: Float, vy: Float): Thing {
        val t = world.addThing(type, variant, place, f.x + f.shiftX + dx, f.y + f.shiftY + dy)
        t.ground = if (f.spec.wall) place.floor - 0.03f else f.depth + 0.045f
        t.vx = vx
        t.vy = vy
        sim.listener.onSpawn(t)
        return t
    }

    /** Keeps the floor from filling up: when there are more than [limit] loose things of [types], the oldest goes in a puff. */
    private fun trim(limit: Int, types: Set<ThingType>) {
        val loose = world.bodiesIn(place).filterIsInstance<Thing>()
            .filter { it.type in types && it.mode == Mode.FREE && !it.held && it.inside < 0 && it.homePlace == null }
        if (loose.size <= limit) return
        val oldest = loose.minByOrNull { it.z } ?: return
        sim.listener.onFx(Fx.POOF, oldest.x, oldest.y - oldest.h / 2, thing = oldest)
        sim.removeThing(oldest, quiet = true)
    }

    companion object {
        /** Everything that lights up its corner of the attic when it is on. */
        val LAMPS = setOf(
            FixtureType.AT_LANTERN, FixtureType.AT_CANDELABRA, FixtureType.AT_STRING_LIGHTS, FixtureType.AT_CHANDELIER,
            FixtureType.AT_SCONCE, FixtureType.AT_ARMILLARY, FixtureType.AT_SHADOW_THEATRE,
        )

        /** Where Sture may hide: each has a container and a hidden spot. */
        val HIDERS = setOf(
            FixtureType.AT_SHEETED, FixtureType.AT_TRUNK, FixtureType.AT_CARTON, FixtureType.AT_BLANKET_FORT,
        )

        /** What the cartons hold: old toys, and now and then a whoopee cushion. */
        val OLD_TOYS = listOf(
            ThingType.TEDDY to 0, ThingType.DUCK to 0, ThingType.TOY_CAR to 0, ThingType.BALL to 0, ThingType.ROCKET to 0,
            ThingType.DRUM to 0, ThingType.GUITAR to 0, ThingType.WHOOPEE to 0, ThingType.BEACH_BALL to 0,
        )
        val TOY_TYPES = OLD_TOYS.map { it.first }.toSet() + setOf(ThingType.CAP, ThingType.BEANIE, ThingType.PARTY_HAT, ThingType.BOW, ThingType.NISSE_HAT)
        val HATS = listOf(ThingType.CAP, ThingType.BEANIE, ThingType.PARTY_HAT, ThingType.BOW, ThingType.NISSE_HAT)

        const val MAX_TOYS = 12
        const val MAX_COSTUMES = 14
        const val MAX_BOOKS = 5
        const val MAX_COINS = 18
        const val MAX_GEMS = 6

        /** Seconds a lifted sheet or open flap stays up. */
        const val REVEAL = 2.4f
        const val SHADOW_ANIMALS = 4
        const val CONSTELLATIONS = 5
        const val PORTRAITS = 6
        const val IDLE_ROLL = 25f

        /** The radius of the horse's rockers, which is also where the art turns it (see atRockingHorse). */
        const val HORSE_RADIUS = 0.12f

        /** The record that gets stuck (see [AtticTunes]). */
        const val STUCK_RECORD = 4
    }
}

/** How long each silly record plays, in seconds; the tunes themselves are in `AtticFx`. */
object AtticTunes {
    val seconds = floatArrayOf(7.5f, 6.5f, 7.0f, 8.0f, 9.5f, 6.0f)
}
