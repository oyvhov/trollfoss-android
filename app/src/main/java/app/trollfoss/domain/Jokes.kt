package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random

/**
 * Slapstick: humour is half of Trollfoss. Whoopee cushions, banana peels, pepper sneezes, bonks on the
 * head, cream pies in the face, hiccups and burps. Everything is kind: nobody is hurt, everyone laughs,
 * and the figure it happened to laughs loudest. The engine turns the [Fx] events into sound and sparkle.
 */
class Jokes(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    private val listener get() = sim.listener

    // ------------------------------------------------------------------ whoopee cushion

    private fun cushionAt(place: PlaceId, x: Float, y: Float): Thing? = world.bodiesIn(place).firstOrNull {
        it is Thing && it.type == ThingType.WHOOPEE && it.mode == Mode.FREE && !it.held &&
            abs(it.x - x) < 0.075f && it.y >= y - 0.04f && it.y <= y + 0.05f
    } as Thing?

    /** Someone sat down: on a cushion? */
    fun seated(place: PlaceId, p: Person) {
        val seat = world.fixtures[p.holder] ?: return
        val point = sim.seatPoint(seat, p.slot)
        cushionAt(place, point[0], point[1])?.let { prrt(it, p) }
    }

    /** Someone landed on their feet: on a cushion or a peel? */
    fun landed(place: PlaceId, p: Person) {
        cushionAt(place, p.x, p.y)?.let {
            prrt(it, p)
            return
        }
        peelAt(place, p)?.let { slip(place, p, it) }
    }

    private fun prrt(cushion: Thing, p: Person?) {
        cushion.squashV += 11f
        cushion.squash = 0.35f
        listener.onFx(Fx.PRRT, cushion.x, cushion.y, thing = cushion, param = p?.id ?: -1)
    }

    /** A tap on the cushion squeezes out a little one. */
    fun squeeze(cushion: Thing) = prrt(cushion, null)

    // ------------------------------------------------------------------ banana peel

    private fun peelAt(place: PlaceId, p: Person): Thing? {
        if (p.anim.slipCool > 0f) return null
        return world.bodiesIn(place).firstOrNull {
            it is Thing && it.type == ThingType.BANANA_PEEL && it.mode == Mode.FREE && it.resting && !it.held &&
                abs(it.x - p.x) < 0.05f && abs(it.y - p.y) < 0.035f
        } as Thing?
    }

    /** Called while an animal strolls. */
    fun stepped(place: PlaceId, p: Person) {
        peelAt(place, p)?.let { slip(place, p, it) }
    }

    private fun slip(place: PlaceId, p: Person, peel: Thing) {
        val dir = if (random.nextBoolean()) 1f else -1f
        p.resting = false
        p.restOwner = -2
        p.vy = -2.3f
        p.vx = 0.35f * dir
        p.anim.spin = 1f
        p.anim.slipCool = 2f
        p.anim.walkTo = Float.NaN
        peel.resting = false
        peel.vx = -dir * 1.8f
        peel.vy = -0.6f
        peel.vrot = -dir * 500f
        listener.onFx(Fx.SLIP, p.x, p.y, thing = peel, param = p.id)
    }

    /** A finished banana leaves its peel, tossed over the shoulder. */
    fun peel(p: Person) {
        val place = p.place ?: return
        val mouth = Anatomy.at(p, Part.MOUTH)
        val peel = world.addThing(ThingType.BANANA_PEEL, 0, place, mouth[0], mouth[1])
        peel.ground = sim.groundOf(place, p) + (random.nextFloat() - 0.3f) * 0.05f
        peel.vx = if (random.nextBoolean()) 0.7f else -0.7f
        peel.vy = -1.6f
        peel.vrot = peel.vx * 500f
        listener.onSpawn(peel)
    }

    // ------------------------------------------------------------------ pepper

    /** Pepper at the nose: «ah … ah …» now, «ATSJO!» in a moment. */
    fun pepper(p: Person) {
        if (p.anim.sneeze > 0f) return
        p.anim.sneeze = SNEEZE_DELAY
    }

    /** A tap on the pepper shaker peppers whoever's face is close. */
    fun shake(place: PlaceId, pepper: Thing) {
        listener.onFx(Fx.PEPPER, pepper.x, pepper.y - pepper.h, thing = pepper)
        world.bodiesIn(place).filterIsInstance<Person>()
            .filter { !it.held && it.mode != Mode.BAG }
            .minByOrNull { val h = Anatomy.at(it, Part.MOUTH); hypot(h[0] - pepper.x, h[1] - (pepper.y - pepper.h)) }
            ?.takeIf { val h = Anatomy.at(it, Part.MOUTH); hypot(h[0] - pepper.x, h[1] - (pepper.y - pepper.h)) < 0.16f }
            ?.let { pepper(it) }
    }

    private fun atsjo(p: Person) {
        val place = p.place ?: return
        val head = Anatomy.at(p, Part.HEAD)
        // Hat and glasses fly off.
        for (slot in listOf(Slot.HEAD, Slot.FACE)) {
            val worn = world.worn(p, slot) ?: continue
            worn.mode = Mode.FREE
            worn.holder = -1
            worn.place = place
            worn.x = head[0]
            worn.y = head[1] - (if (slot == Slot.HEAD) p.h * 0.2f else 0f)
            worn.ground = sim.groundOf(place, p) + 0.02f
            worn.resting = false
            worn.vy = -2.2f - random.nextFloat() * 0.6f
            worn.vx = (random.nextFloat() - 0.5f) * 1.6f
            worn.vrot = worn.vx * 600f + 300f
        }
        // Light things close by are blown away.
        for (b in world.bodiesIn(place)) {
            if (b !is Thing || b.mode != Mode.FREE || b.held || b.inside >= 0) continue
            val d = abs(b.x - p.x)
            if (d > 0.45f || abs(b.y - p.y) > 0.3f || b.w * b.h > 0.008f) continue
            val push = (1f - d / 0.45f)
            b.resting = false
            b.restOwner = -2
            b.vx += sign(b.x - p.x + 0.0001f) * (0.6f + push * 1.4f)
            b.vy = -0.5f - push * 1.2f
            b.vrot = b.vx * 300f
        }
        p.squashV -= 8f
        listener.onFx(Fx.ATSJO, head[0], head[1], param = p.id)
    }

    // ------------------------------------------------------------------ bonk and splat

    /**
     * A thing flying fast into someone's head bonks off it (a pillow bursts into feathers), and a cake
     * or cupcake splats all over the face. Returns true when the thing is gone.
     */
    fun flying(place: PlaceId, t: Thing): Boolean {
        if (t.cool > 0f) return false
        val speed = hypot(t.vx, t.vy)
        if (speed < 1.5f || t.type.lift < 0f) return false
        for (b in world.bodiesIn(place)) {
            val p = b as? Person ?: continue
            if (p.held || p.mode == Mode.BAG || world.carried(p).any { it === t }) continue
            val head = Anatomy.at(p, Part.HEAD)
            val r = p.h * Anatomy.headRadius(p.species) + max(t.w, t.h) * 0.4f
            if (hypot(t.x - head[0], t.y - t.h / 2 - head[1]) > r) continue
            t.cool = 0.5f
            if (t.type == ThingType.CAKE || t.type == ThingType.CUPCAKE) {
                p.anim.cream = CREAM_SECONDS
                listener.onFx(Fx.SPLAT, head[0], head[1], thing = t, param = p.id)
                sim.removeThing(t, quiet = true)
                return true
            }
            val soft = t.type == ThingType.PILLOW || t.type == ThingType.TEDDY || t.type == ThingType.FEATHER
            t.vx = -t.vx * (if (soft) 0.25f else 0.45f)
            t.vy = -abs(t.vy) * 0.3f - 0.7f
            t.vrot = -t.vrot
            p.squashV += 6f
            listener.onFx(if (soft) Fx.FLUFF else Fx.BONK, head[0], head[1], thing = t, param = p.id)
            return false
        }
        return false
    }

    // ------------------------------------------------------------------ hiccups and burps

    /** After a sip or the last bite. Three quick sips give hiccups; a big meal may end in a burp. */
    fun ate(p: Person, type: ThingType, result: Give, now: Float) {
        val a = p.anim
        if (type.drink && !type.potion) {
            a.sips = if (now - a.lastSip < 5f) a.sips + 1 else 1
            a.lastSip = now
            if (a.sips >= 3) {
                a.sips = 0
                a.hiccups = HICCUP_SECONDS
                a.nextHic = 0.5f
            }
        }
        val big = type.drink || type == ThingType.CAKE || type == ThingType.WATERMELON || type == ThingType.PIZZA || type == ThingType.BREAD
        if (result == Give.FINISHED && big && !type.potion && random.nextFloat() < 0.6f) a.burp = 0.8f
        if (result == Give.FINISHED && type == ThingType.BANANA) peel(p)
    }

    fun step(p: Person, dt: Float) {
        val a = p.anim
        a.cool(dt)
        if (a.sneeze > 0f) {
            a.sneeze -= dt
            if (a.sneeze <= 0f) {
                a.sneeze = 0f
                atsjo(p)
            }
        }
        if (a.burp > 0f) {
            a.burp -= dt
            if (a.burp <= 0f) {
                a.burp = 0f
                val mouth = Anatomy.at(p, Part.MOUTH)
                listener.onFx(Fx.BURP, mouth[0], mouth[1], param = p.id)
            }
        }
        if (a.hiccups > 0f) {
            a.hiccups -= dt
            a.nextHic -= dt
            if (a.nextHic <= 0f) {
                a.nextHic = 0.7f + random.nextFloat() * 0.5f
                val mouth = Anatomy.at(p, Part.MOUTH)
                listener.onFx(Fx.HICCUP, mouth[0], mouth[1], param = p.id)
            }
        }
    }

    private fun PersonAnim.cool(dt: Float) {
        slipCool = max(0f, slipCool - dt)
        cream = max(0f, cream - dt)
        ink = max(0f, ink - dt)
    }

    companion object {
        /** Matches the «ah … ah …» at the start of the sneeze sound. */
        const val SNEEZE_DELAY = 0.72f
        const val HICCUP_SECONDS = 5f
        const val CREAM_SECONDS = 6f
    }
}
