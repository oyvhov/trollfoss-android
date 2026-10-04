package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Event codes of the garden (the 500 block of [HouseFx]). `GardenFx` turns each into sound and sparkle. */
object GardenFxCode {
    /** A frog croaks; arg is the frog's number (0 deepest to 4 highest), which is its note. */
    const val CROAK = HouseFx.GARDEN + 1
    /** The frogs have sung together. */
    const val CHOIR = HouseFx.GARDEN + 2
    /** A plant grows; arg is the new stage (1 sprout, 2 big, 3 giant). */
    const val GROW = HouseFx.GARDEN + 3
    const val WATER = HouseFx.GARDEN + 4
    /** A vegetable is giant now; arg is its kind. */
    const val GIANT = HouseFx.GARDEN + 5
    const val HARVEST = HouseFx.GARDEN + 6
    const val MIST = HouseFx.GARDEN + 7
    /** Somebody sits in a giant vegetable and it is tapped. */
    const val WOBBLE = HouseFx.GARDEN + 8
    /** A gnome winks; arg is its number. */
    const val WINK = HouseFx.GARDEN + 9
    /** A gnome pops away in a puff. */
    const val POOF = HouseFx.GARDEN + 10
    /** 0 dig, 1 treasure, 2 sand cake. */
    const val DIG = HouseFx.GARDEN + 11
    /** 0 a push, 1 whee. */
    const val SWING = HouseFx.GARDEN + 12
    const val ZIP_GO = HouseFx.GARDEN + 13
    const val ZIP_END = HouseFx.GARDEN + 14
    const val ZIP_BACK = HouseFx.GARDEN + 15
    /** 0 up, 1 down, 2 arrived at the top, 3 arrived at the bottom. */
    const val CLIMB = HouseFx.GARDEN + 16
    /** 0 off, 1 on. */
    const val GRILL = HouseFx.GARDEN + 17
    /** 0 closed, 1 open. */
    const val PARASOL = HouseFx.GARDEN + 18
    /** 0 off, 1 on, 2 somebody got wet (arg2 is the figure). */
    const val SPRINKLER = HouseFx.GARDEN + 19
    /** 0 off, 1 on, 2 clippings, 3 bump. */
    const val MOWER = HouseFx.GARDEN + 20
    /** The bird in the bird house; arg is the step of its tune (9 is a far-off chirp). */
    const val BIRD = HouseFx.GARDEN + 21
    /** 0 closed, 1 open and a ball comes over the fence. */
    const val GATE = HouseFx.GARDEN + 22
    /** 0 stir, 1 feed, 2 burp, 3 the golden key, 4 no thanks, 5 seeds. */
    const val COMPOST = HouseFx.GARDEN + 23
    const val BARREL = HouseFx.GARDEN + 24
    const val PINWHEEL = HouseFx.GARDEN + 25
    /** 0 butterflies, 1 a flower is picked. */
    const val FLOWERS = HouseFx.GARDEN + 26
    /** 0 leaves, 1 an apple falls. */
    const val SHAKE_TREE = HouseFx.GARDEN + 27
    const val HAMMOCK = HouseFx.GARDEN + 28
    const val BRIDGE = HouseFx.GARDEN + 29
    const val SNOWMAN = HouseFx.GARDEN + 30
    const val FROG_AMBIENT = HouseFx.GARDEN + 31
    /** The treehouse cabin door: 0 closed, 1 open. */
    const val CABIN = HouseFx.GARDEN + 32
    /** A seed goes into a bed. */
    const val SOW = HouseFx.GARDEN + 33
}

/**
 * The part of the garden on screen right now, in scene x. The art writes it every frame it draws the garden;
 * the gnomes read it to move only where nobody is looking. Until the art has drawn, everything counts as seen.
 */
object GardenView {
    @Volatile var from = 0f
    @Volatile var to = Float.MAX_VALUE

    fun set(left: Float, right: Float) {
        from = left
        to = right
    }

    fun reset() {
        from = 0f
        to = Float.MAX_VALUE
    }

    /** True when something at [x] cannot be seen (with [margin] to spare for its own size). */
    fun hidden(x: Float, margin: Float = 0.3f): Boolean = x < from - margin || x > to + margin
}

/**
 * The garden's rules: the greenhouse where plants grow giant, the frog choir, the treehouse with its ladder,
 * swing and zip line, the grill, the sandbox, the lawn robot, the compost heap that hides the golden key, and
 * the gnomes that move when nobody looks. Like everything in the house, it only knows [Sim] and [SimListener].
 */
class GardenRules(private val sim: Sim, private val random: Random) : FloorRules {
    private val world get() = sim.world
    private val listener get() = sim.listener
    private val place = PlaceId.MANOR_GARDEN

    // ------------------------------------------------------------------ small helpers

    private fun fixture(ix: Int): Fixture? = world.fixtures[place.idBase + ix]

    private fun ixOf(f: Fixture): Int = place.indexOf(f.id)

    private fun fx(code: Int, f: Fixture?, arg: Int = 0, x: Float? = null, y: Float? = null, thing: Thing? = null) {
        val fx = x ?: (f?.let { it.x + it.shiftX } ?: 0f)
        val fy = y ?: (f?.let { it.y + it.shiftY - it.spec.h * 0.5f } ?: 0.5f)
        listener.onFx(Fx.HOUSE, fx, fy, f, thing, HouseFx.pack(code, arg))
    }

    private class Later(val at: Float, val action: () -> Unit)

    private val later = ArrayList<Later>()

    private fun after(seconds: Float, action: () -> Unit) {
        later += Later(sim.time + seconds, action)
    }

    private fun planters(): List<Fixture> = (0..2).mapNotNull { fixture(GardenIx.PLANTER0 + it) }

    private fun frogs(): List<Fixture> = (0 until GardenIx.FROGS).mapNotNull { fixture(GardenIx.FROG0 + it) }

    private fun gnomes(): List<Fixture> = (0 until GardenIx.GNOMES).mapNotNull { fixture(GardenIx.GNOME0 + it) }

    private fun persons(): List<Person> = world.bodiesIn(place).filterIsInstance<Person>()

    private fun count(type: ThingType): Int = world.bodiesIn(place).count { it is Thing && it.type == type }

    /** Things the child makes should never fill the garden up: the oldest loose one goes in a puff. */
    private fun limit(keep: Thing) {
        val things = world.bodiesIn(place).filterIsInstance<Thing>()
        if (things.size <= Sim.MAX_THINGS) return
        val oldest = Treasure.oldestToDrop(things, keep) ?: return
        listener.onFx(Fx.POOF, oldest.x, oldest.y - oldest.h / 2, thing = oldest)
        sim.removeThing(oldest)
    }

    private fun spawn(type: ThingType, variant: Int, x: Float, y: Float, vx: Float, vy: Float, ground: Float = Float.NaN): Thing {
        val t = world.addThing(type, variant, place, x, y)
        if (!ground.isNaN()) t.ground = ground
        t.vx = vx
        t.vy = vy
        t.vrot = vx * 300f
        listener.onSpawn(t)
        limit(t)
        return t
    }

    // ------------------------------------------------------------------ taps

    override fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean {
        if (place != PlaceId.MANOR_GARDEN) return false
        when (f.type) {
            FixtureType.GA_GREENHOUSE -> mist(f)
            FixtureType.GA_PLANTER -> tapPlanter(f)
            FixtureType.GA_FROG -> croak(f)
            FixtureType.GA_BRIDGE -> fx(GardenFxCode.BRIDGE, f)
            FixtureType.GA_SHED -> {
                f.open = !f.open
                sim.invalidate(place)
                listener.onFx(if (f.open) Fx.OPEN else Fx.CLOSE, f.x, f.y - f.spec.h / 2, f)
            }
            FixtureType.GA_GRILL -> {
                f.on = !f.on
                if (!f.on) grillThings(f).forEach { it.cook = 0f }
                fx(GardenFxCode.GRILL, f, if (f.on) 1 else 0)
            }
            FixtureType.GA_PATIO -> {
                f.mode = 1 - f.mode
                fx(GardenFxCode.PARASOL, f, if (f.mode == 0) 1 else 0)
            }
            FixtureType.GA_FLOWER_BED -> tapBed(f)
            FixtureType.GA_BIRDHOUSE -> tapBirdhouse(f)
            FixtureType.GA_BARREL -> {
                f.count = (f.count + 1) % 1000
                fx(GardenFxCode.BARREL, f, f.count % 3)
            }
            FixtureType.GA_COMPOST -> fx(GardenFxCode.COMPOST, f, 0)
            FixtureType.GA_HAMMOCK -> fx(GardenFxCode.HAMMOCK, f)
            FixtureType.GA_GNOME -> tapGnome(f)
            FixtureType.GA_MOWER -> {
                f.on = !f.on
                if (f.on && abs(f.angleV) < 0.05f) f.angleV = if (random.nextBoolean()) 0.26f else -0.26f
                fx(GardenFxCode.MOWER, f, if (f.on) 1 else 0)
            }
            FixtureType.GA_SPRINKLER -> {
                f.on = !f.on
                f.timer = SPRINKLER_SECONDS
                fx(GardenFxCode.SPRINKLER, f, if (f.on) 1 else 0)
            }
            FixtureType.GA_GATE -> tapGate(f)
            FixtureType.GA_SNOWMAN -> tapSnowman(f)
            FixtureType.GA_PINWHEEL -> {
                f.angleV += 12f
                fx(GardenFxCode.PINWHEEL, f)
            }
            FixtureType.GA_SANDBOX -> dig(f)
            FixtureType.GA_TRAMPOLINE -> listener.onFx(Fx.BOING, f.x, f.top, f)
            FixtureType.GA_TREEHOUSE -> tapTree(f, dx, dy)
            FixtureType.GA_LADDER -> climb(f)
            FixtureType.GA_SWING -> {
                f.angleV += if (f.angleV >= 0f) 2.6f else -2.6f
                fx(GardenFxCode.SWING, f, 0)
            }
            FixtureType.GA_ZIP, FixtureType.GA_ZIP_POLE -> callZip()
            else -> return false
        }
        return true
    }

    // ------------------------------------------------------------------ greenhouse: plants that grow into giants

    /** Tap a bed: a seed goes in, then it is watered; a giant vegetable is harvested (or wobbles, if somebody sits in it). */
    private fun tapPlanter(f: Fixture) {
        when {
            f.mode == 0 -> sow(f)
            f.mode in 1..2 -> water(f)
            else -> tapGiant(f)
        }
    }

    private fun sow(f: Fixture) {
        f.mode = 1
        f.timer = GardenLayout.GROW_SECONDS
        fx(GardenFxCode.SOW, f)
        after(0.3f) { fx(GardenFxCode.GROW, f, 1) }
    }

    private fun water(f: Fixture) {
        fx(GardenFxCode.WATER, f)
        after(0.7f) { grow(f) }
    }

    /** One stage on; at the third the vegetable is giant, has a seat, and the first one brings out a glimt. */
    private fun grow(f: Fixture) {
        if (f.mode !in 1..2) return
        f.mode += 1
        f.timer = GardenLayout.GROW_SECONDS
        if (f.mode >= 3) {
            f.open = true
            f.anim = 1f
            fx(GardenFxCode.GIANT, f, f.variant)
            sim.tasks.record(Deed.GA_GIANT, place, ThingType.GA_VEGGIE, f.type)
            sim.unlock("garden_greenhouse")
        } else {
            f.anim = 1f
            fx(GardenFxCode.GROW, f, f.mode)
        }
    }

    private fun tapGiant(f: Fixture) {
        if (world.seatedAt(f, 0) != null) {
            fx(GardenFxCode.WOBBLE, f, f.variant)
            return
        }
        // Nobody sits in it: it gives three ordinary vegetables and starts again as a sprout.
        f.mode = 1
        f.open = false
        f.timer = GardenLayout.GROW_SECONDS
        for (i in 0 until 3) {
            spawn(ThingType.GA_VEGGIE, f.variant, f.x - 0.1f + i * 0.1f, f.y - 0.14f, (i - 1) * 0.4f, -1.7f - i * 0.2f, f.depth + 0.02f)
        }
        fx(GardenFxCode.HARVEST, f, f.variant)
    }

    /** The greenhouse's mist waters every bed: empty ones get a seed, growing ones grow, giants wobble. */
    private fun mist(f: Fixture) {
        fx(GardenFxCode.MIST, f)
        var delay = 0.45f
        for (p in planters()) {
            val bed = p
            after(delay) {
                when {
                    bed.mode == 0 -> {
                        bed.mode = 1
                        bed.timer = GardenLayout.GROW_SECONDS
                        fx(GardenFxCode.SOW, bed)
                        fx(GardenFxCode.GROW, bed, 1)
                    }
                    bed.mode in 1..2 -> grow(bed)
                    else -> bed.anim = 1f
                }
            }
            delay += 0.3f
        }
    }

    private fun plantersStep(f: Fixture, dt: Float) {
        f.open = f.mode >= 3
        if (f.mode !in 1..2 || world.night) return
        if (f.timer <= 0f) f.timer = GardenLayout.GROW_SECONDS
        f.timer -= dt
        if (f.timer <= 0f) grow(f)
    }

    // ------------------------------------------------------------------ pond: the frog choir

    private val croaks = ArrayList<Pair<Int, Float>>()
    private var lastChoir = -99f
    private var ambientT = 6f
    private var birdT = 9f

    private fun croak(f: Fixture) {
        val k = (ixOf(f) - GardenIx.FROG0).coerceIn(0, GardenIx.FROGS - 1)
        fx(GardenFxCode.CROAK, f, k)
        val now = sim.time
        croaks.removeAll { now - it.second > CHOIR_WINDOW }
        croaks += k to now
        if (croaks.map { it.first }.toSet().size >= 3 && now - lastChoir > CHOIR_PAUSE) choir()
    }

    /** Three different frogs within a few seconds start a song in all five notes. */
    private fun choir() {
        lastChoir = sim.time
        croaks.clear()
        val frogs = frogs()
        if (frogs.isEmpty()) return
        fx(GardenFxCode.CHOIR, frogs[frogs.size / 2], x = (GardenLayout.POND_X1 + GardenLayout.POND_X2) / 2f, y = 0.7f)
        for ((i, k) in CHOIR_TUNE.withIndex()) {
            val frog = frogs.getOrNull(k) ?: continue
            after(0.55f + i * 0.27f) {
                frog.anim = 1f
                fx(GardenFxCode.CROAK, frog, k)
            }
        }
        sim.tasks.record(Deed.GA_CHOIR, place, fixture = FixtureType.GA_FROG)
        sim.unlock("garden_pond")
    }

    private fun ambient(dt: Float) {
        ambientT -= dt
        if (ambientT <= 0f) {
            ambientT = if (world.night) 5f + random.nextFloat() * 8f else 14f + random.nextFloat() * 18f
            if (world.weather != Weather.SNOW) {
                val k = random.nextInt(GardenIx.FROGS)
                fixture(GardenIx.FROG0 + k)?.let {
                    it.anim = 1f
                    fx(GardenFxCode.FROG_AMBIENT, it, k)
                }
            }
        }
        birdT -= dt
        if (birdT <= 0f) {
            birdT = 11f + random.nextFloat() * 20f
            if (!world.night && world.weather == Weather.SUN) fixture(GardenIx.BIRDHOUSE)?.let { fx(GardenFxCode.BIRD, it, 9) }
        }
    }

    // ------------------------------------------------------------------ the small things

    private fun tapBed(f: Fixture) {
        f.count = (f.count + 1) % 1000
        fx(GardenFxCode.FLOWERS, f, 0)
        if (f.count % 3 == 0 && count(ThingType.FLOWER) < 8) {
            spawn(ThingType.FLOWER, f.variant * 2 + random.nextInt(2), f.x + (random.nextFloat() - 0.5f) * 0.12f, f.y - 0.12f, 0.2f + random.nextFloat() * 0.3f, -1.4f, f.depth + 0.02f)
            fx(GardenFxCode.FLOWERS, f, 1)
        }
    }

    private fun tapBirdhouse(f: Fixture) {
        f.count = (f.count + 1) % 1000
        fx(GardenFxCode.BIRD, f, f.count % 6)
        // Now and then a feather drifts down from the nest.
        if (f.count % 4 == 0 && count(ThingType.FEATHER) < 4) {
            val t = spawn(ThingType.FEATHER, 0, f.x + 0.03f, f.y - 0.38f, 0.1f, -0.3f, f.depth + 0.02f)
            t.vrot = 90f
        }
    }

    private fun tapGate(f: Fixture) {
        f.mode = 1 - f.mode
        fx(GardenFxCode.GATE, f, f.mode)
        // Whoever is on the other side kicks their ball over the fence.
        if (f.mode == 1 && count(ThingType.BALL) < 3) {
            spawn(ThingType.BALL, 0, f.x, f.y - 0.38f, (random.nextFloat() - 0.5f) * 0.5f, -1.8f, place.floor)
        }
    }

    private fun tapSnowman(f: Fixture) {
        fx(GardenFxCode.SNOWMAN, f, 0)
        if (count(ThingType.SNOWBALL) < 3) {
            spawn(ThingType.SNOWBALL, 0, f.x + 0.06f, f.y - 0.12f, 0.7f, -1.4f, f.depth + 0.02f)
            fx(GardenFxCode.SNOWMAN, f, 1)
        }
    }

    private fun tapTree(f: Fixture, dx: Float, dy: Float) {
        if (dx in 0.0f..0.38f && dy in -0.64f..-0.34f) {
            f.open = !f.open
            sim.invalidate(place)
            fx(GardenFxCode.CABIN, f, if (f.open) 1 else 0)
            return
        }
        f.angleV += 1.2f
        val apple = count(ThingType.APPLE) < 3 && random.nextFloat() < 0.6f
        fx(GardenFxCode.SHAKE_TREE, f, if (apple) 1 else 0)
        if (apple) spawn(ThingType.APPLE, 0, f.x + (random.nextFloat() - 0.5f) * 0.5f, f.y - 0.62f, 0f, 0f, f.depth + 0.04f)
    }

    // ------------------------------------------------------------------ sandbox, compost and the other drops

    private val loot = listOf(
        ThingType.SHELL to 0, ThingType.COIN to 0, ThingType.STARFISH to 0, ThingType.GEM to 1, ThingType.DUCK to 0,
        ThingType.COIN to 0, ThingType.SHELL to 0, ThingType.BOOT to 0, ThingType.GEM to 3, ThingType.ROCK to 0,
    )

    private fun dig(f: Fixture) {
        f.count = (f.count + 1) % 1000
        val treasure = f.count % 3 == 0
        fx(GardenFxCode.DIG, f, if (treasure) 1 else 0)
        if (treasure) after(0.45f) { treasure(f) }
    }

    private fun treasure(f: Fixture) {
        val (type, variant) = loot[random.nextInt(loot.size)]
        spawn(type, variant, f.x + (random.nextFloat() - 0.5f) * 0.2f, f.y - 0.08f, (random.nextFloat() - 0.5f) * 0.8f, -1.7f, f.depth + 0.03f)
        sim.unlock("garden_sand")
    }

    override fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean {
        if (place != PlaceId.MANOR_GARDEN) return false
        return when (f.type) {
            FixtureType.GA_PLANTER -> when {
                t.type == ThingType.SEEDS && f.mode == 0 -> {
                    sim.removeThing(t, quiet = true)
                    sow(f)
                    true
                }
                t.type == ThingType.WATERING_CAN && f.mode in 1..2 -> {
                    water(f)
                    false
                }
                else -> false
            }
            FixtureType.GA_COMPOST -> feed(f, t)
            FixtureType.GA_SANDBOX -> {
                when (t.type) {
                    // A spade digs for treasure right away, and a bucket turned over makes a sand cake.
                    ThingType.SPADE -> {
                        f.count = (f.count + 1) % 1000
                        fx(GardenFxCode.DIG, f, 1)
                        after(0.4f) { treasure(f) }
                    }
                    ThingType.BUCKET -> {
                        fx(GardenFxCode.DIG, f, 2)
                        after(0.4f) { spawn(ThingType.GA_SAND_CAKE, 0, f.x + 0.06f, f.y - 0.1f, 0.3f, -1.4f, f.depth + 0.03f) }
                    }
                    else -> Unit
                }
                false
            }
            FixtureType.GA_BIRDHOUSE -> if (t.type == ThingType.SEEDS) {
                sim.removeThing(t, quiet = true)
                f.count = (f.count + 1) % 1000
                fx(GardenFxCode.BIRD, f, 7)
                true
            } else false
            else -> false
        }
    }

    /** What the compost heap likes: leftovers and plants. Anything else it politely hands back. */
    private fun likesCompost(t: Thing): Boolean =
        t.type.cat == Cat.FOOD || t.type == ThingType.SEEDS || t.type == ThingType.FLOWER || t.type == ThingType.MUSHROOM ||
            t.type == ThingType.PINECONE || t.type == ThingType.STICK || t.type == ThingType.LEAF || t.type == ThingType.FEATHER ||
            t.type == ThingType.BANANA_PEEL

    /** Every third mouthful it burps, and the first burp brings up the golden key. */
    private fun feed(f: Fixture, t: Thing): Boolean {
        if (!likesCompost(t)) {
            t.resting = false
            t.vy = -1.4f
            t.vx = 0.5f
            fx(GardenFxCode.COMPOST, f, 4)
            return true
        }
        sim.removeThing(t, quiet = true)
        f.count = (f.count + 1) % 1000
        f.anim = 1f
        if (f.count % 3 != 0) {
            fx(GardenFxCode.COMPOST, f, 1)
            return true
        }
        fx(GardenFxCode.COMPOST, f, 2)
        if ("manor_key_garden" !in world.flags) {
            after(0.55f) {
                spawn(ThingType.GOLDEN_KEY, 0, f.x, f.y - 0.24f, 0.4f, -2.2f, f.depth + 0.03f)
                fx(GardenFxCode.COMPOST, f, 3)
                f.mode = 1
                sim.flag("manor_key_garden")
            }
        } else {
            after(0.55f) {
                spawn(ThingType.SEEDS, 0, f.x, f.y - 0.22f, 0.3f, -1.8f, f.depth + 0.03f)
                fx(GardenFxCode.COMPOST, f, 5)
            }
        }
        return true
    }

    // ------------------------------------------------------------------ grill

    private fun grillThings(f: Fixture): List<Thing> = world.bodiesIn(place).filterIsInstance<Thing>().filter {
        it.mode == Mode.FREE && it.resting && !it.held && it.restOwner == f.id
    }

    private fun grillStep(f: Fixture, dt: Float) {
        if (!f.on) return
        for (t in grillThings(f)) {
            t.cook += dt
            if (t.cook < 2.2f) continue
            val to = Recipes.fire(t.type) ?: continue
            val from = t.type
            t.type = to
            t.variant = 0
            t.used = 0
            t.cook = 0f
            t.squashV += 6f
            listener.onFx(Fx.COOKED, t.x, t.y - t.h, f, t)
            Recipes.keyFor(FixtureType.CAMPFIRE, listOf(from))?.let(sim::discover)
            if (to == ThingType.GRILLED_SAUSAGE) sim.tasks.record(Deed.GA_GRILL, place, to, f.type)
        }
    }

    // ------------------------------------------------------------------ gnomes

    private var lastTick = -1000f
    private var gnomeT = 8f

    /** Where a gnome spot is: x, y and the draw depth (a spot on something sorts with that something). */
    private fun spotPos(i: Int): FloatArray {
        val s = GardenGnomes.spots[i]
        val host = if (s.host >= 0) fixture(s.host) else null
        return if (host != null) floatArrayOf(host.x + host.shiftX + s.x, host.y + host.shiftY + s.y, host.depth + 0.0007f) else floatArrayOf(s.x, s.y, s.y)
    }

    private fun freeSpots(): List<Int> {
        val taken = gnomes().map { GardenGnomes.slotOf(it) }.toSet()
        return GardenGnomes.spots.indices.filter { it !in taken }
    }

    private fun moveGnome(g: Fixture, slot: Int) {
        val pos = spotPos(slot)
        g.mode = slot + 1
        g.count = (g.count + 1) % 4
        g.x = pos[0]
        g.y = pos[1]
        g.depth = pos[2]
    }

    /** Once in a while a gnome that nobody sees moves to a spot that nobody sees. */
    private fun gnomesStep(dt: Float) {
        gnomeT -= dt
        if (gnomeT > 0f) return
        gnomeT = 7f + random.nextFloat() * 10f
        if (!sneak()) gnomeT = 1.5f
    }

    private fun sneak(): Boolean {
        for (g in gnomes().shuffled(random)) {
            if (!GardenView.hidden(g.x)) continue
            val to = freeSpots().shuffled(random).firstOrNull { GardenView.hidden(spotPos(it)[0]) } ?: continue
            moveGnome(g, to)
            return true
        }
        return false
    }

    /** Back in the garden after a while away: most of the gnomes have been up to something. */
    private fun returned() {
        if ("garden_visited" !in world.flags) {
            // The very first visit shows the garden as it was planned.
            sim.flag("garden_visited")
            return
        }
        var moved = 0
        for (g in gnomes().shuffled(random)) {
            if (random.nextFloat() >= 0.6f) continue
            val to = freeSpots().shuffled(random).firstOrNull() ?: break
            moveGnome(g, to)
            moved++
        }
        if (moved == 0) {
            val g = gnomes().randomOrNull(random)
            val to = freeSpots().shuffled(random).firstOrNull()
            if (g != null && to != null) moveGnome(g, to)
        }
    }

    private fun tapGnome(g: Fixture) {
        val k = (ixOf(g) - GardenIx.GNOME0).coerceIn(0, GardenIx.GNOMES - 1)
        fx(GardenFxCode.WINK, g, k)
        // The ones nearby wink back, a moment later, as if they were in on it.
        for ((i, o) in gnomes().withIndex()) {
            if (o === g || abs(o.x - g.x) > 1.0f) continue
            after(0.3f + 0.15f * i) {
                o.anim = 1f
                fx(GardenFxCode.WINK, o, i)
            }
        }
        sim.flag("garden_gnome_$k")
        if ((0 until GardenIx.GNOMES).count { "garden_gnome_$it" in world.flags } >= 3) sim.unlock("garden_gnome")
        // Tickled with five quick taps, it pops away in a puff and turns up somewhere else, in plain sight.
        if (g.taps >= 5) {
            g.taps = 0
            val to = freeSpots().shuffled(random).firstOrNull() ?: return
            fx(GardenFxCode.POOF, g, k)
            moveGnome(g, to)
            g.anim = 1f
            fx(GardenFxCode.POOF, g, k)
        }
    }

    // ------------------------------------------------------------------ rope ladder

    private val ladderT = floatArrayOf(0f, 0f)

    private fun deckY(): Float = (fixture(GardenIx.TREEHOUSE)?.y ?: (place.floor - 0.07f)) + GardenLayout.DECK_DY

    private fun treeX(): Float = fixture(GardenIx.TREEHOUSE)?.x ?: GardenLayout.TREE_X

    private fun ease(t: Float): Float {
        val c = t.coerceIn(0f, 1f)
        return c * c * (3f - 2f * c)
    }

    /** Figures at the foot go up, figures on the deck come down; each lane carries one at a time. */
    private fun climb(f: Fixture) {
        val tree = fixture(GardenIx.TREEHOUSE)
        if (world.seatedAt(f, 0) == null) {
            val below = persons().filter {
                it.mode == Mode.FREE && !it.held && it.resting && it.restOwner == -1 && abs(it.x - f.x) <= 0.55f &&
                    abs(sim.groundOf(place, it) - f.depth) <= 0.2f
            }.minByOrNull { abs(it.x - f.x) }
            if (below != null && sim.seat(below, f, 0)) {
                ladderT[0] = 0f
                fx(GardenFxCode.CLIMB, f, 0)
            }
        }
        if (tree != null && world.seatedAt(f, 1) == null) {
            val above = persons().filter {
                it.mode == Mode.FREE && !it.held && it.resting && it.restOwner == tree.id && abs(it.x - f.x) <= 0.9f
            }.minByOrNull { abs(it.x - f.x) }
            if (above != null && sim.seat(above, f, 1)) {
                ladderT[1] = 0f
                fx(GardenFxCode.CLIMB, f, 1)
            }
        }
        if (world.seatedAt(f, 0) == null && world.seatedAt(f, 1) == null) fx(GardenFxCode.CLIMB, f, 4)
    }

    private fun ladderPoint(f: Fixture, lane: Int): FloatArray {
        val e = ease(ladderT[lane.coerceIn(0, 1)])
        val foot = f.y
        val top = deckY()
        val y = if (lane == 0) foot + (top - foot) * e else top + (foot - top) * e
        // A little rope-ladder sway, and a step from side to side.
        val x = f.x + (if (lane == 0) -0.012f else 0.012f) + sin(sim.time * 7f + lane) * 0.006f
        return floatArrayOf(x, y)
    }

    private fun ladderStep(f: Fixture, dt: Float) {
        val tree = fixture(GardenIx.TREEHOUSE)
        for (lane in 0..1) {
            val rider = world.seatedAt(f, lane)
            if (rider == null) {
                ladderT[lane] = 0f
                continue
            }
            f.anim = max(f.anim, 0.02f)
            ladderT[lane] += dt / GardenLayout.CLIMB_SECONDS
            if (ladderT[lane] < 1f) continue
            ladderT[lane] = 0f
            rider.mode = Mode.FREE
            rider.holder = -1
            rider.resting = false
            rider.restOwner = -2
            rider.vx = 0f
            rider.vy = 0.1f
            rider.anim.hopV = 1.1f
            rider.anim.face = Face.LAUGH
            rider.anim.faceTime = 1.0f
            if (lane == 0) {
                rider.x = (tree?.x ?: GardenLayout.TREE_X) + GardenLayout.DECK_X0 + 0.16f
                rider.y = deckY() - 0.01f
                rider.ground = Float.NaN
                fx(GardenFxCode.CLIMB, f, 2, x = rider.x, y = rider.y - 0.15f)
            } else {
                rider.x = f.x
                rider.ground = f.depth + 0.025f
                rider.y = rider.ground - 0.01f
                fx(GardenFxCode.CLIMB, f, 3, x = rider.x, y = rider.y - 0.1f)
            }
            rider.z = world.nextZ()
        }
    }

    // ------------------------------------------------------------------ tyre swing

    private fun swingPoint(f: Fixture): FloatArray =
        floatArrayOf(f.x + SWING_ROPE * sin(f.angle), f.y - SWING_PIVOT + SWING_ROPE * cos(f.angle))

    private var swingWhee = 0f

    private fun swingStep(f: Fixture, dt: Float) {
        val rider = world.seatedAt(f, 0)
        // The seat's own weight is a pendulum; a rider pumps their legs and keeps it going, gently.
        val damping = if (rider != null) 0.15f else 0.55f
        f.angleV += (-SWING_OMEGA2 * sin(f.angle) - damping * f.angleV) * dt
        if (rider != null && abs(f.angleV) < 1.6f) f.angleV += (if (f.angleV >= 0f) 1f else -1f) * 1.4f * dt
        f.angle += f.angleV * dt
        f.angle = f.angle.coerceIn(-0.85f, 0.85f)
        val moving = abs(f.angle) > 0.012f || abs(f.angleV) > 0.04f
        if (moving) f.anim = max(f.anim, 0.02f) else {
            f.angle = 0f
            f.angleV = 0f
        }
        swingWhee -= dt
        if (rider != null && abs(f.angle) > 0.55f && swingWhee <= 0f) {
            swingWhee = 2.6f
            fx(GardenFxCode.SWING, f, 1, x = rider.x, y = rider.y - 0.2f)
        }
    }

    // ------------------------------------------------------------------ zip line

    private fun zip(): Fixture? = fixture(GardenIx.ZIP)

    private fun callZip() {
        val z = zip() ?: return
        if (z.mode != 0) {
            fx(GardenFxCode.ZIP_BACK, z, 1)
            return
        }
        // Whoever stands at the carriage on the deck hops on; riders dropped on it are already there.
        if (world.seatedAt(z, 0) == null) {
            val tree = fixture(GardenIx.TREEHOUSE)
            val rider = persons().filter {
                it.mode == Mode.FREE && !it.held && it.resting && tree != null && it.restOwner == tree.id && abs(it.x - z.x) <= 0.6f
            }.minByOrNull { abs(it.x - z.x) }
            if (rider != null) sim.seat(rider, z, 0)
        }
        z.mode = 1
        z.timer = 0f
        z.on = true
        fx(GardenFxCode.ZIP_GO, z, if (world.seatedAt(z, 0) != null) 1 else 0)
    }

    private fun zipStep(z: Fixture, dt: Float) {
        // Drawn in front of the garden, so the rider glides past everything.
        z.depth = ZIP_DEPTH
        val dx = GardenLayout.ZIP_X1 - GardenLayout.ZIP_X0
        val dy = GardenLayout.ZIP_Y1 - GardenLayout.ZIP_Y0
        when (z.mode) {
            0 -> {
                z.shiftX = 0f
                z.shiftY = 0f
                z.on = false
            }
            1 -> {
                z.on = true
                z.anim = max(z.anim, 0.02f)
                z.timer += dt
                val q = ease(z.timer / GardenLayout.ZIP_SECONDS)
                z.shiftX = dx * q
                z.shiftY = dy * q + sin(q * 3.1416f) * ZIP_SAG
                if (z.timer >= GardenLayout.ZIP_SECONDS) finishZip(z)
            }
            else -> {
                z.anim = max(z.anim, 0.02f)
                z.timer += dt
                val q = 1f - ease(z.timer / ZIP_BACK_SECONDS)
                z.shiftX = dx * q
                z.shiftY = dy * q + sin(q * 3.1416f) * ZIP_SAG
                if (z.timer >= ZIP_BACK_SECONDS) {
                    z.mode = 0
                    z.timer = 0f
                    z.on = false
                    z.shiftX = 0f
                    z.shiftY = 0f
                }
            }
        }
    }

    /** At the pole the rider lets go, flies out over the pond and lands in it with a splash. */
    private fun finishZip(z: Fixture) {
        z.shiftX = GardenLayout.ZIP_X1 - GardenLayout.ZIP_X0
        z.shiftY = GardenLayout.ZIP_Y1 - GardenLayout.ZIP_Y0
        val rider = world.seatedAt(z, 0)
        if (rider != null) {
            rider.mode = Mode.FREE
            rider.holder = -1
            rider.resting = false
            rider.restOwner = -2
            rider.x = z.x + z.shiftX
            rider.y = z.y + z.shiftY + rider.h * Anatomy.HIPS
            rider.ground = Float.NaN
            rider.vx = -0.7f
            rider.vy = -0.9f
            rider.anim.hopV = 1.0f
            rider.anim.face = Face.LAUGH
            rider.anim.faceTime = 2.0f
            rider.z = world.nextZ()
            sim.tasks.record(Deed.GA_ZIP, place, fixture = FixtureType.GA_ZIP)
            sim.unlock("garden_tree")
        }
        fx(GardenFxCode.ZIP_END, z, if (rider != null) 1 else 0, x = z.x + z.shiftX, y = z.y + z.shiftY - 0.2f)
        z.mode = 2
        z.timer = 0f
    }

    // ------------------------------------------------------------------ lawn robot and sprinkler

    private val bumped = HashMap<Int, Float>()

    private fun mowerStep(f: Fixture, dt: Float) {
        if (world.night && f.on) {
            f.on = false
            fx(GardenFxCode.MOWER, f, 0)
        }
        if (!f.on) {
            f.bob = 0f
            return
        }
        f.anim = max(f.anim, 0.02f)
        if (abs(f.angleV) < 0.05f) f.angleV = 0.26f
        f.shiftX += f.angleV * dt
        val lo = GardenLayout.MOW_X0 - f.x
        val hi = GardenLayout.MOW_X1 - f.x
        if (f.shiftX < lo) {
            f.shiftX = lo
            f.angleV = abs(f.angleV)
        } else if (f.shiftX > hi) {
            f.shiftX = hi
            f.angleV = -abs(f.angleV)
        }
        f.bob = sin(sim.time * 38f) * 0.0012f
        f.timer -= dt
        if (f.timer <= 0f) {
            f.timer = 0.3f
            fx(GardenFxCode.MOWER, f, 2)
        }
        // It politely hops over anyone in its way, who hops too.
        val mx = f.x + f.shiftX
        for (p in persons()) {
            if (p.mode != Mode.FREE || p.held || !p.resting || p.restOwner != -1) continue
            if (abs(p.x - mx) > 0.11f || abs(sim.groundOf(place, p) - f.depth) > 0.12f) continue
            if (sim.time - (bumped[p.id] ?: -9f) < 2.2f) continue
            bumped[p.id] = sim.time
            p.resting = false
            p.vy = -1.2f
            p.anim.face = Face.LAUGH
            p.anim.faceTime = 1.0f
            fx(GardenFxCode.MOWER, f, 3, x = p.x, y = p.y - 0.15f)
        }
        // And it greets every gnome it passes.
        for ((i, g) in gnomes().withIndex()) {
            if (abs(g.x - mx) < 0.08f && abs(g.depth - f.depth) < 0.1f && g.anim < 0.01f) {
                g.anim = 1f
                fx(GardenFxCode.WINK, g, i)
            }
        }
    }

    private fun sprinklerStep(f: Fixture, dt: Float) {
        if (!f.on) return
        f.anim = max(f.anim, 0.02f)
        f.timer -= dt
        if (f.timer <= 0f) {
            f.on = false
            fx(GardenFxCode.SPRINKLER, f, 0)
            return
        }
        f.angleV -= dt
        if (f.angleV > 0f) return
        f.angleV = 1.4f
        for (p in persons()) {
            if (p.mode != Mode.FREE || p.held || abs(p.x - f.x) > 0.34f || abs(sim.groundOf(place, p) - f.depth) > 0.2f) continue
            p.anim.face = Face.LAUGH
            p.anim.faceTime = 1.2f
            p.anim.hopV = 1.0f
            fx(GardenFxCode.SPRINKLER, f, 2, x = p.x, y = p.y - 0.15f, thing = null)
        }
    }

    // ------------------------------------------------------------------ the step of every fixture, and the step of the garden

    override fun step(place: PlaceId, f: Fixture, dt: Float) {
        if (place != PlaceId.MANOR_GARDEN) return
        when (f.type) {
            FixtureType.GA_PLANTER -> plantersStep(f, dt)
            FixtureType.GA_GRILL -> grillStep(f, dt)
            FixtureType.GA_ZIP -> zipStep(f, dt)
            FixtureType.GA_LADDER -> ladderStep(f, dt)
            FixtureType.GA_SWING -> swingStep(f, dt)
            FixtureType.GA_MOWER -> mowerStep(f, dt)
            FixtureType.GA_SPRINKLER -> sprinklerStep(f, dt)
            FixtureType.GA_PINWHEEL -> {
                f.angle += f.angleV * dt
                f.angleV *= exp(-1.1f * dt)
                if (f.angleV > 0.3f) f.anim = max(f.anim, 0.02f) else f.angleV = 0f
            }
            FixtureType.GA_GNOME -> {
                val s = GardenGnomes.spots[GardenGnomes.slotOf(f)]
                val host = if (s.host >= 0) fixture(s.host) else null
                f.depth = if (host != null) host.depth + 0.0007f else f.y
            }
            // Windows and lamps shine at night.
            FixtureType.GA_GREENHOUSE, FixtureType.GA_TREEHOUSE -> f.on = world.night
            else -> Unit
        }
    }

    override fun tick(place: PlaceId, dt: Float) {
        if (place != PlaceId.MANOR_GARDEN) return
        val due = later.filter { it.at <= sim.time }
        if (due.isNotEmpty()) {
            later.removeAll(due.toSet())
            for (l in due) l.action()
        }
        val away = sim.time - lastTick > AWAY_SECONDS
        lastTick = sim.time
        if (away) returned()
        gnomesStep(dt)
        ambient(dt)
        // The snowman is only there when it snows; when it melts it is simply gone, and a new one comes with the next snow.
        fixture(GardenIx.SNOWMAN)?.let { sn ->
            val snowing = world.weather == Weather.SNOW
            if (snowing && sn.x < 0f) {
                sn.x = SNOWMAN_X
                fx(GardenFxCode.SNOWMAN, sn, 2)
            } else if (!snowing && sn.x >= 0f) {
                sn.x = -2f
            }
        }
    }

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? = when (f.type) {
        FixtureType.GA_LADDER -> ladderPoint(f, spot)
        FixtureType.GA_SWING -> swingPoint(f)
        else -> null
    }

    companion object {
        /** Three croaks from three frogs within this many seconds make a choir. */
        const val CHOIR_WINDOW = 3.4f
        const val CHOIR_PAUSE = 5f

        /** The choir's tune, as frog numbers: up, down, and home. */
        val CHOIR_TUNE = intArrayOf(2, 3, 4, 3, 2, 0, 2, 4, 3, 2)

        /** Seconds away from the garden that make a new visit, where the gnomes have been busy. */
        const val AWAY_SECONDS = 2.5f

        const val SPRINKLER_SECONDS = 12f
        const val ZIP_DEPTH = 0.935f
        const val ZIP_SAG = 0.025f
        const val ZIP_BACK_SECONDS = 3.6f

        /** The tyre swing hangs from a branch [SWING_PIVOT] above its foot on a rope [SWING_ROPE] long. */
        const val SWING_PIVOT = 0.58f
        const val SWING_ROPE = 0.44f
        const val SWING_OMEGA2 = 10f
        const val SNOWMAN_X = 1.8f
    }
}
