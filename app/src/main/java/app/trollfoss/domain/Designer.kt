package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * The home designer and tidying up. Furniture can be added from the catalogue, put away in the store
 * and brought back; rooms get new wallpaper and floors. Tidying sends every thing home to where it
 * belongs, in a sparkling arc; things made during play go to the lost-and-found chest or vanish in a
 * puff. The bin and the robot vacuum tidy a little at a time, and burp now and then.
 */
class Designer(private val sim: Sim, private val random: Random) {
    private val world get() = sim.world
    private val listener get() = sim.listener

    // ------------------------------------------------------------------ furniture

    /** Adds a piece of furniture from the catalogue or the store at ([x], [y]). Returns null when the place is full. */
    fun add(place: PlaceId, type: FixtureType, variant: Int, x: Float, y: Float): Fixture? {
        val used = world.fixturesIn(place).map { place.indexOf(it.id) }.toSet()
        val slot = (place.addedFrom..place.addedMax).firstOrNull { it !in used } ?: return null
        val f = Fixture(place.idBase + slot, place, type, x, y, variant, y)
        world.fixtures[f.id] = f
        val spot = sim.clampFixture(place, f, x, y)
        f.x = spot[0]
        f.y = spot[1]
        f.depth = if (f.spec.wall) f.y else spot[1]
        f.anim = 1f
        if (type == FixtureType.LAMP_POST) f.on = true
        sim.invalidate(place)
        listener.onFx(Fx.PLACE, f.x, f.y - f.spec.h / 2, f)
        return f
    }

    /**
     * Whether [f] may be put away: never while a glimt still waits on it or in it, so nothing can be
     * lost for good, and never the few fixtures the village cannot do without.
     */
    fun canStore(place: PlaceId, f: Fixture): Boolean {
        if (!sim.movable(f)) return false
        if (f.type == FixtureType.MAILBOX || f.type == FixtureType.CHEST && place == PlaceId.HOME && f.id == place.idBase) return false
        // The doors, stairs and other ways between floors stay where they are.
        if (House.passageAt(f) != null) return false
        val index = place.indexOf(f.id)
        return Secrets.inPlace(place).none { s -> s.id !in world.found && (s.on == index || s.inside == index) }
    }

    /** Puts [f] away in the store. What stood on it falls; whoever sat on it stands up. */
    fun store(place: PlaceId, f: Fixture): Boolean {
        if (!canStore(place, f)) return false
        for (b in world.bodiesIn(place)) {
            if (b.mode == Mode.SEATED && b.holder == f.id) {
                b.mode = Mode.FREE
                b.holder = -1
                b.resting = false
                b.ground = f.depth + 0.02f
            }
            if (b.mode == Mode.INSIDE && b.holder == f.id) {
                b.mode = Mode.FREE
                b.holder = -1
                b.x = f.x
                b.y = f.top
            }
            if (b.restOwner == f.id || b.inside == f.id) {
                b.resting = false
                b.restOwner = -2
                b.inside = -1
                b.ground = f.depth + 0.02f
                b.vy = -0.4f
            }
        }
        for (child in world.fixturesIn(place).filter { it.host == f.id }) {
            world.storage += Stored(child.type, child.variant)
            world.fixtures.remove(child.id)
        }
        world.fixtures.remove(f.id)
        world.storage += Stored(f.type, f.variant)
        sim.invalidate(place)
        listener.onFx(Fx.STORE, f.x, f.y - f.spec.h / 2, f)
        return true
    }

    /** Takes a piece out of the store and places it. */
    fun unstore(place: PlaceId, index: Int, x: Float, y: Float): Fixture? {
        val item = world.storage.getOrNull(index) ?: return null
        val f = add(place, item.type, item.variant, x, y) ?: return null
        world.storage.removeAt(index)
        return f
    }

    /** New wallpaper ([wall]) or floor ([floor]) for a room; null keeps what is there. */
    fun restyle(place: PlaceId, room: Int, wall: Int? = null, floor: Int? = null) {
        if (!Decor.decoratable(place)) return
        val old = Decor.style(world, place, room)
        world.styles[Decor.key(place, room)] = RoomStyle(
            wall?.coerceIn(0, Decor.WALLS - 1) ?: old.wall,
            floor?.coerceIn(0, Decor.FLOORS - 1) ?: old.floor,
        )
        val r = Decor.rooms(place)[room]
        listener.onFx(Fx.PAINT, (r.start + r.endInclusive) / 2f, 0.5f, param = if (wall != null) 0 else 1)
    }

    // ------------------------------------------------------------------ tidying

    /** Where a thing belongs now, following its furniture if that has been moved. */
    private fun homeOf(t: Thing): FloatArray? {
        if (t.homePlace == null) return null
        if (t.homeOwner < 0) return floatArrayOf(t.homeDx, t.homeDy)
        val f = world.fixtures[t.homeOwner] ?: return null
        return floatArrayOf(f.x + t.homeDx, f.y + t.homeDy)
    }

    private fun keepsake(t: Thing): Boolean = t.type.cat in KEEPSAKES || t.type == ThingType.GIFT

    /**
     * Tidies [place]: every loose thing that belongs somewhere flies home, keepsakes made during play
     * fly to the lost-and-found chest at home, and crumbs, peels and leftovers vanish in a puff.
     * Returns how many things were tidied.
     */
    fun tidy(place: PlaceId): Int {
        var n = 0
        for (b in world.bodiesIn(place).toList()) {
            val t = b as? Thing ?: continue
            if (t.held || t.mode != Mode.FREE || t.flyT >= 0f) continue
            if (sendHome(place, t)) n++
        }
        if (n > 0) listener.onFx(Fx.TIDY, 0f, 0f, param = n)
        return n
    }

    /** Sends one thing home (or to the chest, or away). Returns false when it is already where it belongs. */
    private fun sendHome(place: PlaceId, t: Thing): Boolean {
        val home = homeOf(t)
        if (home != null && t.homePlace == place) {
            if (abs(t.x - home[0]) < 0.01f && abs(t.y - home[1]) < 0.01f) return false
            val inside = if (t.homeInside) t.homeOwner else -1
            fly(t, home[0], home[1], inside)
            return true
        }
        if (home != null) {
            // Belongs in another place: off it goes, and it is back there when the child arrives.
            teleport(t, t.homePlace!!, home[0], home[1], if (t.homeInside) t.homeOwner else -1)
            listener.onFx(Fx.POOF, t.x, t.y - t.h / 2, thing = t)
            return true
        }
        if (keepsake(t)) {
            val chest = world.fixturesIn(PlaceId.HOME).firstOrNull { it.type == FixtureType.CHEST } ?: return false
            if (place == PlaceId.HOME) {
                fly(t, chest.x + (random.nextFloat() - 0.5f) * 0.1f, chest.y - 0.015f, chest.id)
            } else {
                listener.onFx(Fx.POOF, t.x, t.y - t.h / 2, thing = t)
                teleport(t, PlaceId.HOME, chest.x + (random.nextFloat() - 0.5f) * 0.1f, chest.y - 0.015f, chest.id)
            }
            return true
        }
        listener.onFx(Fx.POOF, t.x, t.y - t.h / 2, thing = t)
        sim.removeThing(t, quiet = true)
        return true
    }

    private fun fly(t: Thing, x: Float, y: Float, inside: Int) {
        t.flyT = 0f
        t.flyX0 = t.x
        t.flyY0 = t.y
        t.flyX1 = x
        t.flyY1 = y
        t.flyInside = inside
        t.resting = false
        t.inside = -1
        t.restOwner = -2
        t.vx = 0f
        t.vy = 0f
    }

    private fun teleport(t: Thing, place: PlaceId, x: Float, y: Float, inside: Int) {
        t.place = place
        t.x = x
        t.y = y
        t.ground = if (y >= place.back) y else Float.NaN
        t.inside = inside
        t.resting = true
        t.restOwner = if (inside >= 0) inside else -2
        t.vx = 0f
        t.vy = 0f
        t.rot = 0f
        // Not resting on anything known yet: the place settles it when it is next shown.
        if (inside < 0) t.resting = false
    }

    /** Moves a flying thing along its arc; lands it at the end. Called by [Sim.step]. */
    fun stepFlight(b: Body, dt: Float) {
        b.flyT = min(1f, b.flyT + dt / FLY_SECONDS)
        val t = b.flyT
        val e = t * t * (3f - 2f * t)
        b.x = b.flyX0 + (b.flyX1 - b.flyX0) * e
        b.y = b.flyY0 + (b.flyY1 - b.flyY0) * e - sin(t * PI_F) * ARC
        b.rot += 540f * dt * (1f - t)
        if (t >= 1f) {
            b.flyT = -1f
            b.rot = 0f
            b.x = b.flyX1
            b.y = b.flyY1
            if (b.flyInside >= 0) {
                b.inside = b.flyInside
                b.restOwner = b.flyInside
                b.resting = true
            } else {
                b.resting = false
                b.vy = 0.1f
            }
            if (b.flyY1 >= (b.place?.back ?: 1f)) b.ground = b.flyY1
            listener.onFx(Fx.HOME, b.x, b.y, thing = b as? Thing)
        }
    }

    // ------------------------------------------------------------------ the bin and the robot vacuum

    /** A thing dropped in the bin: leftovers are eaten, things that belong somewhere pop back home. */
    fun bin(place: PlaceId, f: Fixture, t: Thing): Boolean {
        f.count++
        f.anim = 1f
        listener.onFx(Fx.TRASH, f.x, f.top, f, t, param = f.count)
        if (homeOf(t) != null || keepsake(t)) {
            t.x = f.x
            t.y = f.top - 0.02f
            sendHome(place, t)
        } else {
            sim.removeThing(t, quiet = true)
        }
        return true
    }

    /** The robot vacuum trundles along its room, slurping up what lies on the floor. */
    fun stepVacuum(place: PlaceId, f: Fixture, dt: Float) {
        if (!f.on) return
        f.timer += dt
        if (f.angleV == 0f) f.angleV = VACUUM_SPEED
        f.shiftX += f.angleV * dt
        val room = Decor.rooms(place)[Decor.roomAt(place, f.x)]
        val left = room.start + f.spec.w - f.x
        val right = room.endInclusive - f.spec.w - f.x
        if (f.shiftX < left || f.shiftX > right || abs(f.shiftX) > 1.2f) {
            f.shiftX = f.shiftX.coerceIn(maxOf(left, -1.2f), minOf(right, 1.2f))
            f.angleV = -f.angleV
            listener.onFx(Fx.BUMP, f.x + f.shiftX, f.y, f, param = -1)
        }
        val x = f.x + f.shiftX
        for (b in world.bodiesIn(place).toList()) {
            val t = b as? Thing ?: continue
            if (t.held || t.mode != Mode.FREE || !t.resting || t.restOwner != -1 || t.flyT >= 0f) continue
            if (abs(t.x - x) > 0.05f || abs(sim.groundOf(place, t) - f.depth) > 0.07f) continue
            if (homeOf(t) != null && t.homePlace == place) {
                val home = homeOf(t)!!
                if (abs(t.x - home[0]) < 0.02f && abs(t.y - home[1]) < 0.02f) continue
            }
            f.count++
            listener.onFx(Fx.SUCK, t.x, t.y, f, t, param = f.count)
            sendHome(place, t)
        }
        if (f.timer > VACUUM_SECONDS) {
            f.on = false
            f.timer = 0f
            listener.onFx(Fx.OFF, x, f.y, f)
        }
    }

    private companion object {
        const val PI_F = 3.1415927f
        const val FLY_SECONDS = 0.8f
        const val ARC = 0.22f
        const val VACUUM_SPEED = 0.22f
        const val VACUUM_SECONDS = 14f
        val KEEPSAKES = setOf(Cat.HAT, Cat.GLASSES, Cat.GARMENT, Cat.TOY, Cat.TOOL, Cat.MAGIC)
    }
}
