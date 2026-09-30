package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random

/** What a figure can wish for. A picture in a thought bubble shows it; no reading needed. */
enum class WishKind { THING, SLEEP, MUSIC, FRIEND }

/** A wish in a thought bubble. [thing] is set for [WishKind.THING]. Never saved. */
class Wish(val kind: WishKind, val thing: ThingType? = null, val variant: Int = 0) {
    var age = 0f
}

/** What happened to a wish, for the engine's sound and sparkle. */
enum class WishEvent { NEW, GRANTED, FADED }

/**
 * The figures' own little lives: wishes that give the child something to do, and animals that wander
 * about on their own. Pure rules on top of [Sim]; the engine draws bubbles and celebrates.
 */
class Life(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world

    fun step(place: PlaceId, dt: Float) {
        val people = world.bodiesIn(place).filterIsInstance<Person>()
        var active = people.count { it.anim.wish != null }
        val zeroG = sim.zeroG(place)
        for (p in people) {
            if (!zeroG) walk(place, p, dt)
            val a = p.anim
            val wish = a.wish
            if (wish != null) {
                wish.age += dt
                when {
                    fulfilled(place, p, wish, people) -> grant(p)
                    wish.age > WISH_LIFE || stale(place, p, wish) -> {
                        a.wish = null
                        a.nextWish = 20f + random.nextFloat() * 25f
                        sim.listener.onWish(p, WishEvent.FADED)
                    }
                }
                continue
            }
            if (p.held || p.mode == Mode.BAG || p.mode == Mode.WORN) continue
            if (a.nextWish < 0f) a.nextWish = 6f + random.nextFloat() * 22f
            a.nextWish -= dt
            if (a.nextWish > 0f) continue
            if (active >= MAX_WISHES) {
                a.nextWish = 4f + random.nextFloat() * 8f
                continue
            }
            a.nextWish = 16f + random.nextFloat() * 24f
            pick(place, p, people)?.let {
                a.wish = it
                active++
                sim.listener.onWish(p, WishEvent.NEW)
            }
        }
    }

    /** Called by [Sim.give] after a thing was given, eaten, worn or held. */
    fun given(p: Person, type: ThingType) {
        val wish = p.anim.wish ?: return
        if (wish.kind == WishKind.THING && wish.thing == type) grant(p)
    }

    /** Called when a figure is put in a seat or bed. */
    fun seated(p: Person) {
        if (p.anim.wish?.kind == WishKind.SLEEP && p.anim.pose == Pose.LIE) grant(p)
    }

    private fun grant(p: Person) {
        p.anim.wish = null
        p.anim.nextWish = 25f + random.nextFloat() * 25f
        world.wishesGranted++
        sim.listener.onWish(p, WishEvent.GRANTED)
        // Every third wish the figure says thank you with a little present.
        if (world.wishesGranted % 3 == 0) {
            val place = p.place ?: return
            val gift = world.addThing(ThingType.GIFT, random.nextInt(ThingType.GIFT.variants), place, p.x + 0.05f, p.y - p.h * 0.6f)
            gift.ground = sim.groundOf(place, p) + 0.01f
            gift.vy = -1.7f
            gift.vx = 0.35f
            gift.vrot = 160f
            sim.listener.onSpawn(gift)
        }
    }

    private fun fulfilled(place: PlaceId, p: Person, wish: Wish, people: List<Person>): Boolean = when (wish.kind) {
        WishKind.THING -> false
        WishKind.SLEEP -> p.anim.pose == Pose.LIE
        WishKind.MUSIC -> world.fixturesIn(place).any { it.type == FixtureType.RADIO && it.on }
        WishKind.FRIEND -> people.any { it !== p && !it.held && near(place, p, it) }
    }

    /** A wish nobody can grant any more (the last banana was eaten) quietly goes away. */
    private fun stale(place: PlaceId, p: Person, wish: Wish): Boolean = when (wish.kind) {
        WishKind.THING -> world.bodiesIn(place).none { it is Thing && it.type == wish.thing && it.mode != Mode.INSIDE } &&
            world.carried(p).none { it.type == wish.thing }
        WishKind.SLEEP -> !world.night
        else -> false
    }

    private fun near(place: PlaceId, a: Person, b: Person): Boolean =
        abs(a.x - b.x) < 0.3f && abs(depthOf(place, a) - depthOf(place, b)) < 0.1f

    private fun depthOf(place: PlaceId, p: Person): Float =
        if (p.mode == Mode.SEATED) world.fixtures[p.holder]?.depth ?: p.y else sim.groundOf(place, p)

    private fun pick(place: PlaceId, p: Person, people: List<Person>): Wish? {
        val options = ArrayList<Pair<Float, () -> Wish?>>()
        val folk = p.species == Species.FOLK
        val carried = world.carried(p).map { it.type }.toSet()
        val things = world.bodiesIn(place).filterIsInstance<Thing>()
            .filter { it.mode == Mode.FREE && !it.held && it.type !in carried && wants(p, it.type) }
        if (things.isNotEmpty()) {
            options += 6f to {
                val t = things[random.nextInt(things.size)]
                Wish(WishKind.THING, t.type, t.variant)
            }
        }
        if (folk && world.night && p.anim.pose != Pose.LIE && hasBed(place)) options += 4f to { Wish(WishKind.SLEEP) }
        if (folk && world.fixturesIn(place).any { it.type == FixtureType.RADIO && !it.on }) options += 1f to { Wish(WishKind.MUSIC) }
        if (people.count { it.species == Species.FOLK } > 1 && people.none { it !== p && near(place, p, it) }) {
            options += (if (folk) 1.5f else 0.5f) to { Wish(WishKind.FRIEND) }
        }
        if (options.isEmpty()) return null
        var roll = random.nextFloat() * options.sumOf { it.first.toDouble() }.toFloat()
        for ((weight, make) in options) {
            roll -= weight
            if (roll <= 0f) return make()
        }
        return options.last().second()
    }

    /** What a figure could sensibly wish for: food for everyone, hats and toys for folk, balls for pets. */
    private fun wants(p: Person, type: ThingType): Boolean {
        if (type.potion || type == ThingType.DOUGH || type == ThingType.EGG || type == ThingType.GIFT) return false
        return when (p.species) {
            Species.FOLK -> type.edible || type.cat == Cat.HAT || type.cat == Cat.GLASSES || type.cat == Cat.TOY
            Species.DOG, Species.CAT -> type == ThingType.BALL || type == ThingType.FISH || type == ThingType.SAUSAGE || type == ThingType.GRILLED_SAUSAGE
            Species.BUNNY, Species.HORSE, Species.COW, Species.SHEEP, Species.ELK -> type == ThingType.CARROT || type == ThingType.APPLE
            Species.PUFFIN -> type == ThingType.FISH
            Species.CHICKEN -> type == ThingType.SEEDS
            Species.DRAGON -> type == ThingType.TOASTED_MARSHMALLOW || type == ThingType.GRILLED_SAUSAGE || type == ThingType.COOKIE
        }
    }

    private fun hasBed(place: PlaceId): Boolean = world.fixturesIn(place).any { f ->
        f.spec.spots.withIndex().any { (i, s) -> s.pose == Pose.LIE && world.seatedAt(f, i) == null }
    }

    // ------------------------------------------------------------------ wandering animals

    /**
     * Animals left alone on the floor stroll about now and then: a few steps sideways and a little
     * forward or back. They never walk into water and never wander off furniture the child put them on.
     */
    private fun walk(place: PlaceId, p: Person, dt: Float) {
        val a = p.anim
        val free = p.species.pet && p.species != Species.DRAGON && !p.held && p.mode == Mode.FREE && p.resting &&
            p.restOwner == -1 && p.floatTime <= 0f && p.inside < 0
        if (!free) {
            a.still = 0f
            a.walkTo = Float.NaN
            a.chase = -1
            return
        }
        a.still += dt
        if (a.still < SETTLE_SECONDS) return
        // Food on the floor? The dog can't resist, the hens peck up seeds, the puffin grabs fish.
        if (a.chase < 0) snack(place, p)?.let { food ->
            a.chase = food.id
            a.walkTo = food.x
            a.walkGround = sim.groundOf(place, food)
        }
        if (a.walkTo.isNaN()) {
            a.nextWalk -= dt
            if (a.nextWalk > 0f) return
            a.nextWalk = 3f + random.nextFloat() * 7f
            val tx = (p.x + (random.nextFloat() * 2f - 1f) * 0.35f).coerceIn(0.15f, place.width - 0.15f)
            val tg = (sim.groundOf(place, p) + (random.nextFloat() * 2f - 1f) * 0.06f).coerceIn(place.back + 0.01f, PlaceId.FRONT - 0.01f)
            val floor = sim.surfaces(place).any { it.band && tx >= it.x1 + 0.05f && tx <= it.x2 - 0.05f }
            if (!floor || sim.poolAt(tx, tg) != null) return
            a.walkTo = tx
            a.walkGround = tg
        }
        val dx = a.walkTo - p.x
        val dg = a.walkGround - p.ground
        val d = hypot(dx, dg * 1.6f)
        if (d < 0.004f) {
            a.walkTo = Float.NaN
            a.walkPhase = 0f
            (world.bodies[a.chase] as? Thing)?.let { food ->
                if (food.place == place && food.mode == Mode.FREE && !food.held && abs(food.x - p.x) < 0.06f) {
                    sim.listener.onFx(Fx.GOBBLE, food.x, food.y, thing = food, param = p.id)
                    sim.removeThing(food, quiet = true)
                }
            }
            a.chase = -1
            return
        }
        val step = min(d, speedOf(p.species) * (if (a.chase >= 0) 2.2f else 1f) * dt)
        p.x += dx / d * step
        p.ground += dg / d * step
        p.y = p.ground
        if (abs(dx) > 0.004f) a.facing = if (dx < 0f) -1f else 1f
        a.walkPhase += step / (p.h * 0.18f)
        sim.jokes.stepped(place, p)
    }

    /** Something tasty lying on the floor close by that this animal would go for. */
    private fun snack(place: PlaceId, p: Person): Thing? {
        val likes: (ThingType) -> Boolean = when (p.species) {
            Species.DOG -> { t -> t.cat == Cat.FOOD && t != ThingType.DOUGH && t != ThingType.EGG }
            Species.CAT, Species.PUFFIN -> { t -> t == ThingType.FISH || t == ThingType.GRILLED_FISH }
            Species.CHICKEN -> { t -> t == ThingType.SEEDS }
            else -> return null
        }
        val ground = sim.groundOf(place, p)
        return world.bodiesIn(place).asSequence()
            .filterIsInstance<Thing>()
            .filter { it.mode == Mode.FREE && it.resting && !it.held && it.restOwner == -1 && likes(it.type) }
            .filter { abs(it.x - p.x) < 0.9f && abs(sim.groundOf(place, it) - ground) < 0.15f && sim.poolAt(it.x, it.y) == null }
            .minByOrNull { abs(it.x - p.x) }
    }

    private fun speedOf(s: Species): Float = when (s) {
        Species.CHICKEN, Species.PUFFIN -> 0.07f
        Species.COW, Species.SHEEP -> 0.05f
        Species.HORSE, Species.DOG -> 0.11f
        else -> 0.08f
    }

    companion object {
        const val WISH_LIFE = 28f
        const val MAX_WISHES = 2
        const val SETTLE_SECONDS = 6f
    }
}
