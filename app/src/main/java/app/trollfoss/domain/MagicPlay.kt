package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.hypot

enum class PlayAction { HUG, THROW, NAP, HIDE, READ, STORY, LIGHT, TWINKLE }
enum class PlayRecipe(val fixture: FixtureType, val parts: List<ThingType>) {
    FORT(FixtureType.PLAY_FORT, listOf(ThingType.AT_SHEET_HAT, ThingType.PILLOW)),
    CART(FixtureType.PLAY_CART, listOf(ThingType.PLANK, ThingType.TIRE, ThingType.TIRE)),
}
data class PlayAssembly(val recipe: PlayRecipe, val parts: List<Int>)
enum class Adventure(val place: PlaceId, val reward: ThingType) {
    HAT(PlaceId.BEACH, ThingType.AT_FLOWER_HAT),
    CAMP(PlaceId.FOREST, ThingType.STAR_JAR),
    PARADE(PlaceId.FARM, ThingType.MICROPHONE),
}

/** Repeatable make-believe, with the original parts kept intact inside each creation. */
class MagicPlay(private val sim: Sim) {
    var revision = 0
        private set
    val active: Adventure? get() = Adventure.entries.firstOrNull { "adventure:active:${it.name}" in world.flags }
    private val world get() = sim.world
    private data class Grip(val fixture: Int, val side: Int, var x: Float)
    private val grips = mutableMapOf<Long, Grip>()
    private val reactionAt = mutableMapOf<Int, Float>()

    fun actions(t: Thing): List<PlayAction> = when (t.type) {
        ThingType.PILLOW, ThingType.TEDDY -> listOf(PlayAction.HUG, PlayAction.THROW, PlayAction.NAP)
        ThingType.AT_SHEET_HAT -> listOf(PlayAction.HIDE, PlayAction.NAP)
        ThingType.BOOK -> listOf(PlayAction.READ, PlayAction.STORY)
        ThingType.AT_FLASHLIGHT -> listOf(PlayAction.LIGHT, PlayAction.TWINKLE)
        else -> emptyList()
    }

    fun act(t: Thing, action: PlayAction): Boolean {
        val place = t.place ?: return false
        if (world.bodies[t.id] !== t || t.held || t.mode !in listOf(Mode.FREE, Mode.WORN) || action !in actions(t)) return false
        sim.here = place
        val owner = (world.bodies[t.holder] as? Person).takeIf { t.mode == Mode.WORN }
        if (owner?.held == true) return false
        val p = owner ?: world.bodiesIn(place).filterIsInstance<Person>().filter {
            !it.held && it.species == Species.FOLK && abs(it.x - t.x) < 0.8f && abs(it.y - t.y) < 0.45f
        }.minByOrNull { abs(it.x - t.x) }
        when (action) {
            PlayAction.THROW -> {
                val hand = owner?.let { Anatomy.at(it, Part.HAND) }
                if (hand != null) { t.x = hand[0]; t.y = hand[1] }
                t.mode = Mode.FREE; t.holder = -1; t.inside = -1; t.restOwner = -2; t.resting = false
                t.vx = (p?.anim?.facing ?: 1f) * 0.8f; t.vy = -1.1f; t.vrot = 90f; t.cool = 0.8f
                sim.listener.onFx(Fx.FLUFF, t.x, t.y - t.h / 2, thing = t, param = p?.id ?: -1)
            }
            PlayAction.LIGHT, PlayAction.TWINKLE -> {
                sim.listener.onFx(if (action == PlayAction.LIGHT) Fx.SPARKLE else Fx.STARRAIN, t.x, t.y - t.h, thing = t)
                if (action == PlayAction.LIGHT && place == Adventure.CAMP.place && stage(Adventure.CAMP) == 2 && world.fixturesIn(place).any {
                    it.type == FixtureType.PLAY_FORT && abs(it.x - t.x) < 0.8f && abs(it.y - t.y) < 0.5f
                }) advance(Adventure.CAMP)
            }
            else -> {
                if (p == null) return false
                sim.give(p, t, if (action == PlayAction.HIDE) Part.HAT else Part.HAND)
                when (action) {
                    PlayAction.NAP -> {
                        world.fixturesIn(place).firstOrNull { abs(it.x - p.x) < 0.8f && it.spec.spots.any { s -> s.pose == Pose.LIE } }?.let { bed ->
                            val spot = bed.spec.spots.indexOfFirst { it.pose == Pose.LIE && world.seatedAt(bed, bed.spec.spots.indexOf(it)) == null }
                            if (spot >= 0) sim.seat(p, bed, spot)
                        }
                        p.anim.face = Face.SLEEP; p.anim.faceTime = 8f; p.anim.nextWalk = 9f
                    }
                    PlayAction.HIDE -> { p.anim.face = Face.WOW; p.anim.faceTime = 3f; p.anim.wave = 2f }
                    PlayAction.STORY -> for (friend in world.bodiesIn(place).filterIsInstance<Person>()) {
                        if (friend.held || abs(friend.x - p.x) > 1f || abs(friend.y - p.y) > 0.3f) continue
                        friend.anim.face = Face.WOW; friend.anim.faceTime = 4f; friend.anim.say = 1; friend.anim.sayTime = 3f
                        friend.anim.walkTo = Float.NaN; friend.anim.nextWalk = 5f
                    }
                    else -> Unit
                }
                sim.listener.onFx(if (action == PlayAction.HIDE || action == PlayAction.HUG) Fx.SQUEAK else Fx.PAGE, p.x, p.y - p.h, thing = t)
            }
        }
        return true
    }

    /** Only our own kit pieces are recalled. Existing room arrangements and held belongings stay put. */
    fun kit(recipe: PlayRecipe, place: PlaceId, x: Float): Boolean {
        sim.here = place
        val ground = PlaceId.FRONT - 0.04f
        if (place.mine && (place == PlaceId.MINE_YARD || !world.mine.standing(place, Mine.slotAt(x)))) return false
        val old = world.playKits[recipe].orEmpty()
        old.mapNotNull { (world.bodies[it] as? Thing)?.takeIf { t -> t.mode == Mode.INSIDE }?.holder }.distinct()
            .mapNotNull { world.fixtures[it] }.filter { it.id in world.playAssemblies }.forEach { unmake(it) }
        val ids = recipe.parts.mapIndexed { i, type ->
            val t = (world.bodies[old.getOrNull(i)] as? Thing)?.takeIf { it.type == type }
                ?: world.addThing(type, i % type.variants, place, x, place.floor)
            if (!t.held && t.mode in listOf(Mode.FREE, Mode.BAG)) {
                t.place = place; t.mode = Mode.FREE; t.holder = -1; t.inside = -1; t.restOwner = -2
                t.x = (x + (i - recipe.parts.lastIndex / 2f) * 0.16f).coerceIn(0.1f, place.width - 0.1f)
                t.y = ground; t.ground = ground; t.resting = false; t.vx = 0f; t.vy = -0.4f
                t.z = world.nextZ(); sim.listener.onSpawn(t)
            }
            t.id
        }
        world.playKits[recipe] = ids
        if (recipe == PlayRecipe.FORT && world.bodies.values.none { it is Thing && it.type == ThingType.AT_FLASHLIGHT && it.id in world.playLightIds }) {
            val light = world.addThing(ThingType.AT_FLASHLIGHT, 0, place, (x + 0.3f).coerceAtMost(place.width - 0.1f), ground)
            world.playLightIds += light.id; sim.listener.onSpawn(light)
        } else if (recipe == PlayRecipe.FORT) world.playLightIds.mapNotNull { world.bodies[it] as? Thing }.firstOrNull {
            !it.held && it.mode in listOf(Mode.FREE, Mode.BAG)
        }?.let { t -> t.mode = Mode.FREE; t.holder = -1; t.inside = -1; t.place = place; t.x = (x + 0.3f).coerceAtMost(place.width - 0.1f); t.y = ground; t.ground = ground; t.resting = false; t.restOwner = -2; t.vx = 0f; t.vy = 0f }
        return true
    }

    /** Dropping any part beside its partners makes the same creation as its picture card. */
    fun combine(t: Thing): Fixture? {
        val place = t.place ?: return null
        sim.here = place
        if (t.held || t.mode != Mode.FREE) return null
        if (place.mine && (place == PlaceId.MINE_YARD || !world.mine.standing(place, Mine.slotAt(t.x)))) return null
        val available = world.bodiesIn(place).filterIsInstance<Thing>().filter {
            !it.held && it.mode == Mode.FREE && it.inside < 0 && hypot(it.x - t.x, it.y - t.y) < 0.23f
        }
        for (recipe in PlayRecipe.entries) {
            if (t.type !in recipe.parts) continue
            val parts = mutableListOf<Thing>()
            for (type in recipe.parts) available.firstOrNull { it.type == type && it !in parts }?.let { parts += it }
            if (parts.size != recipe.parts.size || t !in parts) continue
            val f = sim.designer.add(place, recipe.fixture, 0, t.x, place.floor) ?: return null
            for (part in parts) {
                part.mode = Mode.INSIDE; part.holder = f.id; part.inside = -1; part.resting = false
                part.vx = 0f; part.vy = 0f; part.vrot = 0f; part.rot = 0f
            }
            world.playAssemblies[f.id] = PlayAssembly(recipe, parts.map { it.id })
            f.open = true; f.anim = 1f
            sim.listener.onFx(Fx.BUILD, f.x, f.top, f)
            if (recipe == PlayRecipe.FORT && place == Adventure.CAMP.place && stage(Adventure.CAMP) == 0 && started(Adventure.CAMP)) advance(Adventure.CAMP)
            if (recipe == PlayRecipe.CART && place == Adventure.PARADE.place && stage(Adventure.PARADE) == 0 && started(Adventure.PARADE)) advance(Adventure.PARADE)
            return f
        }
        return null
    }

    fun unmake(f: Fixture): Boolean {
        val assembly = world.playAssemblies.remove(f.id) ?: return false
        cancel(f.id)
        // Use the normal safe storage path to release seats, loose contents and carried clothes.
        if (!sim.designer.store(f.place, f)) { world.playAssemblies[f.id] = assembly; return false }
        world.storage.removeAt(world.storage.lastIndex)
        for (id in assembly.parts) (world.bodies[id] as? Thing)?.let {
            it.mode = Mode.BAG; it.place = null; it.holder = -1; it.inside = -1; it.restOwner = -2; it.resting = false; it.held = false
        }
        sim.listener.onFx(Fx.POOF, f.x, f.top, f)
        return true
    }

    fun seated(f: Fixture) {
        if (f.type == FixtureType.PLAY_FORT && f.place == Adventure.CAMP.place && stage(Adventure.CAMP) == 1) advance(Adventure.CAMP)
        if (f.type == FixtureType.PLAY_CART && f.place == Adventure.PARADE.place && stage(Adventure.PARADE) == 1 && f.spec.spots.indices.count { world.seatedAt(f, it) != null } >= 2) advance(Adventure.PARADE)
    }

    fun handle(f: Fixture, dx: Float, dy: Float): Int? = if (f.type == FixtureType.PLAY_CART && dy in -0.22f..-0.06f && abs(dx) in 0.25f..0.43f) { if (dx < 0) 0 else 1 } else null

    fun grip(id: Long, f: Fixture, side: Int, x: Float): Boolean {
        if (world.fixtures[f.id] !== f || f.type != FixtureType.PLAY_CART || grips.values.any { it.fixture == f.id && it.side == side }) return false
        grips[id] = Grip(f.id, side, x); f.count = grips.values.count { it.fixture == f.id }; return true
    }

    fun drag(id: Long, x: Float): Boolean {
        val grip = grips[id] ?: return false
        val f = world.fixtures[grip.fixture] ?: return false
        val dx = (x - grip.x).coerceIn(-0.15f, 0.15f); grip.x = x
        val crew = grips.values.count { it.fixture == f.id }
        if (crew < 2 && !f.on || abs(dx) < 0.0001f) return false
        val at = sim.clampFixture(f.place, f, f.x + dx / crew, f.y)
        val distance = abs(at[0] - f.x)
        sim.moveFixture(f.place, f, at[0], at[1]); f.angle += distance; f.anim = 0.2f
        f.timer += distance
        if (f.place == Adventure.PARADE.place && stage(Adventure.PARADE) == 2 && f.timer >= 0.3f) advance(Adventure.PARADE)
        return distance > 0f
    }

    fun release(id: Long) { grips.remove(id)?.let { g -> world.fixtures[g.fixture]?.count = grips.values.count { it.fixture == g.fixture } } }
    fun cancel(fixture: Int? = null) { grips.keys.toList().filter { fixture == null || grips[it]?.fixture == fixture }.forEach(::release) }

    fun react(place: PlaceId, fx: Fx, x: Float, y: Float) {
        val face = when (fx) {
            Fx.BUILD, Fx.PLACE, Fx.GIFT, Fx.STARRAIN -> Face.WOW
            Fx.FLUFF, Fx.BOING, Fx.SQUEAK -> Face.LAUGH
            Fx.WATER, Fx.BUBBLES -> Face.OOH
            else -> return
        }
        for (p in world.bodiesIn(place).filterIsInstance<Person>()) {
            if (p.held || p.anim.pose == Pose.LIE || abs(p.x - x) > 0.85f || abs(p.y - y) > 0.6f || sim.time - (reactionAt[p.id] ?: -10f) < 0.8f) continue
            reactionAt[p.id] = sim.time; p.anim.face = face; p.anim.faceTime = 2.4f
            p.anim.lookX = (x - p.x).coerceIn(-1f, 1f); p.anim.walkTo = Float.NaN; p.anim.nextWalk = 3f
            if (face == Face.LAUGH) { p.anim.tickle = 1.3f; p.anim.hopV = 0.45f } else p.anim.wave = 1.2f
        }
    }

    fun started(a: Adventure) = "adventure:${a.name}:start" in world.flags
    fun stage(a: Adventure): Int = (1..3).count { "adventure:${a.name}:$it" in world.flags }
    fun nextPlace(a: Adventure) = if (a == Adventure.HAT && stage(a) >= 1) PlaceId.HOME else a.place
    fun start(a: Adventure) {
        world.flags.removeAll { it.startsWith("adventure:active:") }
        world.flags.add("adventure:active:${a.name}")
        world.flags.add("adventure:${a.name}:start")
        revision++
        if (a == Adventure.HAT && stage(a) < 3 && world.bodies[world.adventureHat] !is Thing) {
            val at = nextPlace(a)
            val hat = Thing(world.adventureHat.takeIf { it > 0 && it !in world.bodies } ?: world.nextId++, ThingType.CAP, 2)
            hat.place = at; hat.x = 1.15f; hat.y = at.floor; hat.z = world.nextZ()
            world.bodies[hat.id] = hat
            world.adventureHat = hat.id
        }
        if (a != Adventure.HAT) world.fixturesIn(a.place).firstOrNull {
            it.type == (if (a == Adventure.CAMP) FixtureType.PLAY_FORT else FixtureType.PLAY_CART) && it.id in world.playAssemblies
        }?.let { f ->
            if (stage(a) == 0) advance(a)
            val needed = if (a == Adventure.CAMP) 1 else 2
            if (stage(a) == 1 && f.spec.spots.indices.count { world.seatedAt(f, it) != null } >= needed) advance(a)
        }
    }
    fun lifted(t: Thing) { if (t.id == world.adventureHat && started(Adventure.HAT) && stage(Adventure.HAT) == 0) advance(Adventure.HAT) }
    fun wore(p: Person, t: Thing) {
        if (t.id == world.adventureHat && p.species == Species.FOLK && p.place == PlaceId.HOME && stage(Adventure.HAT) == 1) {
            advance(Adventure.HAT); advance(Adventure.HAT)
        }
    }
    private fun advance(a: Adventure) {
        if (!started(a)) return
        val stage = stage(a)
        if (stage >= 3 || !world.flags.add("adventure:${a.name}:${stage + 1}")) return
        revision++
        if (stage + 1 == 3) {
            val reward = world.addThing(a.reward, 0, null, 0f, 0f)
            reward.mode = Mode.BAG; reward.z = world.nextZ()
            sim.listener.onSpawn(reward)
            sim.listener.onFx(Fx.GIFT, 1.1f, sim.here.floor - 0.3f, thing = reward, param = 1)
        }
    }
}
