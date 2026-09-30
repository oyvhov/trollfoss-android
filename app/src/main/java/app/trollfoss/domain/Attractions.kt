package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * The rules of the newer places: the tivoli's rides and games, the shop's belt, scale and trolley, the
 * doctor's X-ray, the stage's instruments and the sea floor's submarine and octopus. [Sim] asks here
 * first and falls back to its own rules for everything else.
 */
class Attractions(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    private val listener get() = sim.listener

    // The stage secret: drums, xylophone and a song within a few seconds of each other.
    private var lastDrum = -99f
    private var lastXylo = -99f
    private var lastSing = -99f

    /** Where a rider sits on a moving ride, or null for an ordinary seat. */
    fun seatPoint(f: Fixture, spot: Int): FloatArray? = when (f.type) {
        FixtureType.FERRIS_WHEEL -> {
            val a = f.angle + spot * (PI_F / 2f)
            val cx = f.x + f.shiftX
            val cy = f.y - WHEEL_HUB
            // The gondola hangs below its point on the rim, so it always stays upright.
            floatArrayOf(cx + cos(a) * WHEEL_RADIUS, cy + sin(a) * WHEEL_RADIUS + 0.075f)
        }
        FixtureType.CAROUSEL -> {
            val a = f.angle + spot * (2f * PI_F / 3f)
            val bob = if (f.on) sin(sim.time * 4f + spot * 2f) * 0.018f else 0f
            floatArrayOf(f.x + cos(a) * 0.24f, f.y - 0.2f + sin(a) * 0.035f + bob)
        }
        else -> null
    }

    /** True when the gondola or horse at [spot] is on the far side of its ride. */
    fun behind(f: Fixture, spot: Int): Boolean = f.type == FixtureType.CAROUSEL && sin(f.angle + spot * (2f * PI_F / 3f)) < 0f

    fun step(place: PlaceId, f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.FERRIS_WHEEL -> if (f.on) {
                f.angle += dt * 0.38f
                for (i in f.spec.spots.indices) {
                    val rider = world.seatedAt(f, i) ?: continue
                    val a = f.angle + i * (PI_F / 2f)
                    // Over the top: «wheee!»
                    if (sin(a) < -0.995f && sim.time - f.timer > 2f) {
                        f.timer = sim.time
                        listener.onFx(Fx.WHEE, rider.x, rider.y - rider.h, f, param = rider.id)
                        sim.unlock("tivoli_top")
                    }
                }
            }
            FixtureType.CAROUSEL -> if (f.on) {
                f.angle += dt * 1.1f
                f.timer -= dt
                if (f.timer <= 0f) {
                    f.timer = 0.32f
                    f.count++
                    listener.onFx(Fx.KEY, f.x, f.top, f, param = CAROUSEL_TUNE[f.count % CAROUSEL_TUNE.size])
                }
            }
            FixtureType.BUMPER_CAR -> if (f.on) {
                f.shiftX += f.angleV * dt
                if (abs(f.shiftX) > 0.32f) {
                    f.shiftX = f.shiftX.coerceIn(-0.32f, 0.32f)
                    f.angleV = -f.angleV
                }
                f.bob = sin(sim.time * 22f) * 0.002f
                for (o in world.fixturesIn(place)) {
                    if (o === f || o.type != FixtureType.BUMPER_CAR) continue
                    val d = (o.x + o.shiftX) - (f.x + f.shiftX)
                    val closing = (o.angleV - f.angleV) * d < 0f
                    if (abs(d) < 0.21f && closing) {
                        f.angleV = -f.angleV
                        o.angleV = if (o.on) -o.angleV else f.angleV * -0.8f
                        if (!o.on) { o.on = true }
                        f.anim = 1f
                        o.anim = 1f
                        val rider = world.seatedAt(f, 0) ?: world.seatedAt(o, 0)
                        listener.onFx(Fx.BUMP, f.x + f.shiftX + d / 2, f.y - 0.08f, f, param = rider?.id ?: -1)
                    }
                }
            }
            FixtureType.CART -> if (abs(f.angleV) > 0.01f) {
                val old = f.shiftX
                f.shiftX += f.angleV * dt
                f.angleV *= exp(-1.1f * dt)
                val min = f.spec.w / 2 + 0.03f - f.x
                val max = place.width - f.spec.w / 2 - 0.03f - f.x
                if (f.shiftX < min || f.shiftX > max) {
                    f.shiftX = f.shiftX.coerceIn(min, max)
                    f.angleV = -f.angleV * 0.5f
                    listener.onFx(Fx.BUMP, f.x + f.shiftX, f.y - 0.1f, f, param = -1)
                }
                carry(place, f, f.shiftX - old)
            } else {
                f.angleV = 0f
            }
            FixtureType.SCALE -> {
                var weight = 0
                var someone = false
                for (b in world.bodiesIn(place)) {
                    if (b.restOwner != f.id || !b.resting || b.held) continue
                    weight += when (b) {
                        is Person -> { someone = true; (b.h * 100f).toInt() }
                        is Thing -> max(1, (b.w * b.h * 2500f).toInt())
                    }
                }
                if (weight != f.mode) {
                    f.mode = weight
                    if (weight > 0) listener.onFx(Fx.BEEP, f.x, f.top, f, param = weight % 4)
                    if (someone) sim.unlock("shop_scale")
                }
            }
            FixtureType.CHECKOUT -> {
                val scanner = f.x
                for (b in world.bodiesIn(place)) {
                    if (b.restOwner != f.id || !b.resting || b.held || b.mode != Mode.FREE) continue
                    if (b.x >= f.x + 0.08f) continue
                    val old = b.x
                    b.x += BELT_SPEED * dt
                    if (old < scanner && b.x >= scanner) {
                        f.count++
                        f.anim = 1f
                        listener.onFx(Fx.SCAN, scanner, f.top, f, thing = b as? Thing, param = f.count)
                        if (f.count % 5 == 0) {
                            listener.onFx(Fx.REGISTER, f.x + 0.2f, f.top, f)
                            sim.unlock("shop_scan")
                        }
                    }
                }
            }
            FixtureType.XRAY -> if (f.on && world.seatedAt(f, 0) != null) {
                sim.unlock("doctor_xray")
                // Once per patient, for the task board.
                if (f.timer == 0f) {
                    f.timer = 1f
                    sim.tasks.record(Deed.XRAY, place, fixture = f.type)
                }
            } else {
                f.timer = 0f
            }
            FixtureType.KELP -> {
                f.angleV += (-f.angle * 20f - f.angleV * 2f) * dt
                f.angle += f.angleV * dt + sin(sim.time * 0.9f + f.id) * 0.02f * dt
            }
            FixtureType.SUBMARINE -> if (f.on) {
                f.timer += dt
                val t = f.timer
                val rise = when {
                    t < 1.2f -> ease(t / 1.2f)
                    t < 8.2f -> 1f
                    t < 9.4f -> 1f - ease((t - 8.2f) / 1.2f)
                    else -> 0f
                }
                val cruise = if (t in 1.2f..8.2f) sin((t - 1.2f) / 7f * 2f * PI_F) else 0f
                f.shiftY = -0.3f * rise + (if (rise > 0.5f) sin(t * 1.4f) * 0.02f else 0f)
                f.shiftX = cruise * 0.75f
                f.angle = if (cruise == 0f) 0f else cos((t - 1.2f) / 7f * 2f * PI_F)
                if (t >= 9.4f) {
                    f.on = false
                    f.timer = 0f
                    f.shiftX = 0f
                    f.shiftY = 0f
                    listener.onFx(Fx.BOING, f.x, f.y, f)
                }
            }
            FixtureType.DISCO_BALL -> if (f.on) f.angle += dt * 1.4f
            else -> Unit
        }
    }

    /** A tap on one of the newer fixtures. Returns false for fixtures [Sim] handles itself. */
    fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean {
        val top = f.top
        when (f.type) {
            FixtureType.FREEZER, FixtureType.SODA_FRIDGE, FixtureType.MEDICINE_CABINET, FixtureType.SHIPWRECK, FixtureType.GIANT_CLAM -> {
                f.open = !f.open
                sim.invalidate(place)
                listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.FERRIS_WHEEL, FixtureType.CAROUSEL, FixtureType.XRAY -> {
                f.on = !f.on
                listener.onFx(if (f.on) Fx.ON else Fx.OFF, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.BUMPER_CAR -> {
                if (!f.on) {
                    f.on = true
                    f.angleV = if (dx < 0f || random.nextBoolean()) 0.45f else -0.45f
                    listener.onFx(Fx.VROOM, f.x + f.shiftX, top, f)
                } else {
                    f.on = false
                    listener.onFx(Fx.OFF, f.x + f.shiftX, top, f)
                }
            }
            FixtureType.CART -> {
                // Push from the side you tap.
                f.angleV = if (dx < 0f) 0.75f else -0.75f
                listener.onFx(Fx.PUSH, f.x + f.shiftX, top, f)
            }
            FixtureType.CAN_TOSS -> if (f.mode == 1) {
                f.mode = 0
                listener.onFx(Fx.BUILD, f.x, f.y - 0.3f, f, param = 3)
            } else {
                listener.onFx(Fx.HOP, f.x, top, f)
            }
            FixtureType.CANDY_FLOSS_STAND, FixtureType.POPCORN_CART -> return false
            FixtureType.TRAMPOLINE -> listener.onFx(Fx.BOING, f.x, top, f)
            FixtureType.CHECKOUT -> listener.onFx(Fx.REGISTER, f.x + 0.2f, top, f)
            FixtureType.SCALE -> listener.onFx(Fx.BEEP, f.x, top, f, param = 0)
            FixtureType.DOCTOR_DESK -> listener.onFx(Fx.BEEP, f.x + dx, top, f, param = 2)
            FixtureType.EYE_CHART -> {
                f.mode = (f.mode + 1) % 4
                listener.onFx(Fx.PAGE, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.EXAM_BED -> listener.onFx(Fx.BOING, f.x, top, f)
            FixtureType.DRUM_KIT -> {
                val kind = when {
                    dx < -0.07f -> 2
                    dx > 0.07f -> 1
                    else -> 0
                }
                f.mode = kind
                lastDrum = sim.time
                listener.onFx(Fx.DRUM, f.x + dx, f.y + dy, f, param = kind)
                band()
            }
            FixtureType.XYLOPHONE -> {
                val bar = ((dx + f.spec.w * 0.45f) / (f.spec.w * 0.9f) * XYLO_BARS).toInt().coerceIn(0, XYLO_BARS - 1)
                f.mode = bar
                lastXylo = sim.time
                listener.onFx(Fx.XYLO, f.x + dx, f.y - f.spec.h, f, param = bar)
                band()
            }
            FixtureType.MIC_STAND -> {
                val singer = world.bodiesIn(place).filterIsInstance<Person>()
                    .filter { !it.held && abs(it.x - f.x) < 0.3f && abs(it.y - f.y) < 0.2f }
                    .minByOrNull { abs(it.x - f.x) }
                if (singer != null) {
                    lastSing = sim.time
                    listener.onFx(Fx.SING, singer.x, singer.y - singer.h, f, param = singer.id)
                    band()
                } else {
                    // Nobody at the mic: feedback squeal.
                    listener.onFx(Fx.SQUEAK, f.x, top, f, param = 1)
                }
            }
            FixtureType.SPEAKER -> {
                f.count++
                for (b in world.bodiesIn(place)) {
                    if (b.held || b.mode != Mode.FREE || abs(b.x - f.x) > 0.8f || b.inside >= 0) continue
                    if (b is Thing) {
                        b.resting = false
                        b.restOwner = -2
                        b.vy = -0.8f - random.nextFloat() * 0.5f
                        b.vx += (random.nextFloat() - 0.5f) * 0.4f
                        b.vrot = (random.nextFloat() - 0.5f) * 400f
                    }
                }
                listener.onFx(Fx.BOOM, f.x, f.y - f.spec.h / 2, f)
                if (f.count >= 3) sim.unlock("stage_boom")
            }
            FixtureType.DISCO_BALL -> {
                f.on = !f.on
                listener.onFx(Fx.DISCO, f.x, f.y, f, param = if (f.on) 1 else 0)
                if (f.on && world.night) sim.unlock("stage_disco")
            }
            FixtureType.SMOKE_MACHINE -> listener.onFx(Fx.FOG, f.x + 0.05f, f.y - 0.05f, f)
            FixtureType.STAGE_PLATFORM, FixtureType.HEIGHT_CHART -> listener.onFx(Fx.HOP, f.x, top, f)
            FixtureType.KELP -> {
                f.angleV += 1.2f
                listener.onFx(Fx.BUBBLES, f.x, f.y - f.spec.h * 0.6f, f)
            }
            FixtureType.CORAL -> listener.onFx(Fx.BUBBLES, f.x, top, f, param = 1)
            FixtureType.SUBMARINE -> if (!f.on) {
                f.on = true
                f.timer = 0f
                listener.onFx(Fx.VROOM, f.x, top, f, param = 2)
            }
            FixtureType.OCTOPUS -> {
                f.mode = (f.mode + 1) % 4
                f.count++
                // A squirt of ink for whoever is closest.
                val target = world.bodiesIn(place).filterIsInstance<Person>()
                    .filter { !it.held && abs(it.x - f.x) < 0.5f }
                    .minByOrNull { abs(it.x - f.x) }
                target?.anim?.ink = INK_SECONDS
                listener.onFx(Fx.INK, f.x, f.y - f.spec.h * 0.6f, f, param = target?.id ?: -1)
                if (f.count >= 3) sim.unlock("sea_octopus")
            }
            else -> return false
        }
        return true
    }

    /** What the tivoli stands hand out. */
    fun dispensed(f: Fixture): Made? = when (f.type) {
        FixtureType.CANDY_FLOSS_STAND -> Made(ThingType.CANDY_FLOSS, random.nextInt(ThingType.CANDY_FLOSS.variants))
        FixtureType.POPCORN_CART -> Made(ThingType.POPCORN)
        else -> null
    }

    /** A thing thrown hard at the cans knocks them down and wins a prize. */
    fun flying(place: PlaceId, t: Thing) {
        if (hypot(t.vx, t.vy) < 1.1f) return
        for (f in world.fixturesIn(place)) {
            if (f.spec.machine != Machine.TARGET || f.mode != 0) continue
            val zone = f.spec.dropZone ?: continue
            if (!zone.contains(t.x - f.x, t.y - t.h / 2 - f.y)) continue
            f.mode = 1
            f.anim = 1f
            f.count++
            t.vx *= -0.3f
            t.vy = -0.6f
            listener.onFx(Fx.KNOCK, f.x, f.y + zone.top / 2, f, t)
            val prize = PRIZES[(f.count - 1) % PRIZES.size]
            val won = world.addThing(prize.type, prize.variant, place, f.x + 0.1f, f.y - 0.22f)
            won.ground = f.depth + 0.02f
            won.vy = -1.8f
            won.vx = 0.5f
            won.vrot = 200f
            listener.onSpawn(won)
            sim.unlock("tivoli_cans")
            return
        }
    }

    /** Somebody bounced on the trampoline. */
    fun bounced(place: PlaceId, b: Body, f: Fixture?) {
        if (f?.type != FixtureType.TRAMPOLINE || b !is Person) return
        f.count++
        sim.tasks.record(Deed.BOUNCE, place, fixture = f.type)
        if (f.count >= 3) sim.unlock("tivoli_jump")
    }

    private fun band() {
        val now = sim.time
        if (now - lastDrum < 6f && now - lastXylo < 6f && now - lastSing < 6f) sim.unlock("stage_band")
    }

    /** Moves what lies on a rolling fixture along with it. */
    private fun carry(place: PlaceId, f: Fixture, dx: Float) {
        if (dx == 0f) return
        for (b in world.bodiesIn(place)) {
            if (b.restOwner == f.id && b.resting && !b.held && b.mode == Mode.FREE) b.x += dx
        }
        sim.invalidate(place)
    }

    private fun ease(t: Float): Float {
        val c = t.coerceIn(0f, 1f)
        return c * c * (3f - 2f * c)
    }

    companion object {
        const val PI_F = 3.1415927f
        const val WHEEL_RADIUS = 0.3f
        const val WHEEL_HUB = 0.42f
        const val BELT_SPEED = 0.13f
        const val XYLO_BARS = 8
        const val INK_SECONDS = 5f

        /** A little waltz on the pentatonic scale the piano uses (indices into it). */
        val CAROUSEL_TUNE = intArrayOf(5, 7, 8, 7, 5, 3, 5, 7, 5, 3, 2, 0, 2, 3, 5, 3)

        val PRIZES = listOf(
            Made(ThingType.TEDDY), Made(ThingType.BALLOON, 1), Made(ThingType.PARTY_HAT, 2),
            Made(ThingType.STAR_GLASSES), Made(ThingType.BALLOON, 3), Made(ThingType.CROWN, 0),
        )
    }
}
