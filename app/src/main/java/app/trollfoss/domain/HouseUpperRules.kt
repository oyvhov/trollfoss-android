package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * What happens on the first floor when something is tapped, dropped or left alone: the family portraits that
 * wave, the toy train that carries a passenger, the tower of blocks, the dollhouse that mirrors the house, the
 * ball pit, the climbing wall, the easel, the karaoke stage, the shower with its rainbow, the mirror that fogs,
 * the wardrobe that dresses you up, the ballerina, the bird feeder and the duck with the golden key.
 *
 * Moving art reads its state from the fixture (`angle`, `timer` and so on, which are never saved) and is kept
 * drawn live by holding [Fixture.anim] just above zero (see [LIVE]); art that stands still is stamped once.
 * Every effect goes out as `Fx.HOUSE` with a code from [UpperCodes].
 */
class HouseUpperRules(private val sim: Sim, private val random: Random) : FloorRules {
    private val world get() = sim.world
    private val listener get() = sim.listener
    private val place = PlaceId.MANOR_UPPER

    // Not saved: who has been seen where, and when.
    private var mirrorClock = 0f
    private var wanderClock = 7f
    private var portraitClock = 25f
    private val onTrain = HashMap<Int, Float>()
    private val trainCounted = HashSet<Int>()
    private val inPit = HashSet<Int>()
    private val climbAt = HashMap<Int, Float>()
    private var singer = -1
    private var showerShown = false
    private var showerSang = 0f
    private var trainLaps = 0

    private fun fixture(index: Int): Fixture? = world.fixtures[place.idBase + index]

    private fun fx(code: Int, x: Float, y: Float, f: Fixture? = null, thing: Thing? = null, arg: Int = 0) {
        listener.onFx(Fx.HOUSE, x, y, f, thing, HouseFx.pack(HouseFx.UPPER + code, arg))
    }

    private fun fx(code: Int, f: Fixture, arg: Int = 0, up: Float = 0.5f) = fx(code, f.x + f.shiftX, f.y - f.spec.h * up, f, null, arg)

    private fun live(f: Fixture) {
        if (f.anim < LIVE) f.anim = LIVE
    }

    private fun count(type: ThingType): Int = world.bodiesIn(place).count { it is Thing && it.type == type }

    /** A new thing that pops out of [f] with a hop, in front of it. */
    private fun toss(type: ThingType, variant: Int, f: Fixture, dx: Float, dy: Float, vx: Float, vy: Float): Thing {
        val t = world.addThing(type, variant, place, f.x + dx, f.y + dy)
        t.ground = if (f.spec.wall) place.back + 0.03f else f.depth + 0.03f
        t.vx = vx
        t.vy = vy
        t.vrot = (random.nextFloat() - 0.5f) * 420f
        listener.onSpawn(t)
        return t
    }

    /** Lets whatever lies on [f] go, so that it falls (or floats up) on its own. */
    private fun release(f: Fixture, push: Float = 0f) {
        for (b in world.bodiesIn(place)) {
            if (b.restOwner != f.id || !b.resting || b.held || b.mode != Mode.FREE) continue
            b.resting = false
            b.restOwner = -2
            b.vy = -0.2f
            b.vx += push
        }
    }

    private fun people(): List<Person> = world.bodiesIn(place).filterIsInstance<Person>().filter { !it.held && it.mode != Mode.BAG }

    private fun nearest(x: Float, reach: Float, folkOnly: Boolean = false): Person? =
        people().filter { (!folkOnly || it.species == Species.FOLK) && it.mode == Mode.FREE && abs(it.x - x) <= reach }.minByOrNull { abs(it.x - x) }

    // ------------------------------------------------------------------ taps

    override fun tap(place: PlaceId, f: Fixture, dx: Float, dy: Float): Boolean {
        when (f.type) {
            FixtureType.UP_PORTRAIT -> tapPortrait(f)
            FixtureType.UP_WINDOW -> tapWindow(f)
            FixtureType.UP_WINDOW_SEAT -> {
                val pet = (0 until 2).firstNotNullOfOrNull { world.seatedAt(f, it) }
                if (pet != null && pet.species != Species.FOLK) fx(UpperCodes.PURR, pet.x, pet.y - pet.h, f, arg = pet.id)
                else fx(UpperCodes.SEAT_BOING, f, 0)
            }
            FixtureType.UP_TOY_TRAIN -> tapTrain(f)
            FixtureType.UP_BLOCKS -> if (f.mode == 0) knockOver(f) else rebuild(f)
            FixtureType.UP_DOLLHOUSE -> {
                f.timer = DOLL_WAVE
                fx(UpperCodes.DOLL_BELL, f, 0, 0.3f)
            }
            FixtureType.UP_PUPPET_THEATER -> {
                f.mode = (f.mode + 1) % 3
                fx(UpperCodes.PUPPET, f, f.mode)
            }
            FixtureType.UP_NIGHT_LAMP -> {
                f.on = !f.on
                fx(UpperCodes.LAMP, f, if (f.on) 1 else 0)
            }
            FixtureType.UP_POSTER -> {
                f.timer = POSTER_TIME
                fx(UpperCodes.POSTER, f, f.variant)
            }
            FixtureType.UP_MOBILE -> {
                f.angleV += 4.5f
                fx(UpperCodes.MOBILE, f, 0)
            }
            FixtureType.UP_BALL_PIT -> tapPit(f)
            FixtureType.UP_CLIMBING_WALL -> tapClimb(f, dy)
            FixtureType.UP_TRAMPOLINE -> fx(UpperCodes.TRAMP, f, 0, 0.9f)
            FixtureType.UP_EASEL -> paint(f)
            FixtureType.UP_KARAOKE -> startShow(f, stageSinger(f))
            FixtureType.UP_FORT -> {
                f.open = !f.open
                sim.invalidate(place)
                fx(UpperCodes.FORT, f, if (f.open) 1 else 0)
            }
            FixtureType.UP_SHOWER -> {
                f.on = !f.on
                f.timer = 0f
                showerShown = false
                fx(UpperCodes.SHOWER, f, if (f.on) 1 else 0)
            }
            FixtureType.BATH -> {
                // The bath is the shared one; its duck party and the golden key are this floor's (see [stepBath]).
                f.on = !f.on
                f.angleV = 0f
                // The water comes in and the ducks on the bottom bob up.
                if (f.on) release(f)
                fx(UpperCodes.BATH, f, if (f.on) 1 else 0, 0.7f)
            }
            FixtureType.UP_BATH_MIRROR -> tapMirror(f)
            FixtureType.UP_TOWELS -> tapTowels(f)
            FixtureType.UP_WARDROBE -> tapWardrobe(f)
            FixtureType.UP_VANITY -> {
                f.on = !f.on
                fx(UpperCodes.VANITY, f, if (f.on) 1 else 0, 0.7f)
            }
            FixtureType.UP_JEWEL_BOX -> {
                if (!f.on) {
                    f.on = true
                    f.timer = JEWEL_TIME
                    f.angle = 0f
                    fx(UpperCodes.JEWEL, f, 1)
                } else {
                    stopJewel(f)
                }
            }
            FixtureType.UP_ROCKING_CHAIR -> {
                f.angleV += 2.8f
                fx(UpperCodes.ROCK, f, 0, 0.5f)
            }
            FixtureType.UP_HANGING_CHAIR -> {
                f.angleV += 2.2f
                fx(UpperCodes.SWING, f, 0, 0.5f)
            }
            FixtureType.UP_BIRD_FEEDER -> refill(f)
            FixtureType.UP_RAILING -> {
                for (p in people()) if (p.species == Species.FOLK && abs(p.x - f.x) < 0.7f) p.anim.wave = 1.6f
                fx(UpperCodes.RAIL, f.x + dx, f.y - 0.2f, f, arg = 0)
            }
            FixtureType.TELESCOPE -> return false
            else -> return false
        }
        return true
    }

    // ------------------------------------------------------------------ portraits

    private fun portraits(): List<Fixture> = world.fixturesIn(place).filter { it.type == FixtureType.UP_PORTRAIT }

    private fun tapPortrait(f: Fixture) {
        wave(f, quiet = false)
        // The neighbours join in a moment later, one after the other: the whole gallery says hello.
        var rank = 0
        for (o in portraits().sortedBy { abs(it.x - f.x) }) {
            if (o === f) continue
            rank++
            if (o.timer <= 0f && o.angleV <= 0f) o.angleV = 0.22f * rank
        }
    }

    private fun wave(f: Fixture, quiet: Boolean) {
        f.timer = PORTRAIT_WAVE
        f.angleV = 0f
        fx(UpperCodes.PORTRAIT, f, f.variant or (if (quiet) 0x100 else 0))
    }

    private fun stepPortrait(f: Fixture, dt: Float) {
        // Blinking is driven from here so a still portrait costs nothing to draw: `bob` is the blink in progress, `angle` the time to the next one.
        if (f.bob > 0f) {
            f.bob = max(0f, f.bob - dt)
            live(f)
        } else if (f.angle == 0f) {
            f.angle = 1f + random.nextFloat() * 6f
        } else {
            f.angle -= dt
            if (f.angle <= 0f) {
                f.bob = 0.14f
                f.angle = 3f + random.nextFloat() * 5f
            }
        }
        if (f.angleV > 0f) {
            f.angleV -= dt
            if (f.angleV <= 0f) wave(f, quiet = false)
        }
        if (f.timer > 0f) {
            f.timer = max(0f, f.timer - dt)
            live(f)
        }
    }

    // ------------------------------------------------------------------ windows

    private fun tapWindow(f: Fixture) {
        f.mode = 1 - f.mode
        fx(UpperCodes.WINDOW, f, f.variant + 4 * f.mode + (if (world.night) 16 else 0))
    }

    // ------------------------------------------------------------------ the toy train

    private fun tapTrain(f: Fixture) {
        when {
            f.on && f.mode == 0 -> {
                // Slows down and rolls into the station.
                f.mode = 1
                fx(UpperCodes.TRAIN_STOP, f, 0, 0.5f)
            }
            f.on -> fx(UpperCodes.TRAIN_GO, f, 1, 0.5f)
            else -> {
                f.on = true
                f.mode = 0
                f.timer = 0f
                trainLaps = 0
                trainCounted.clear()
                onTrain.clear()
                fx(UpperCodes.TRAIN_GO, f, 0, 0.5f)
            }
        }
    }

    private fun trainCrossed(from: Float, to: Float, target: Float): Boolean =
        if (to >= from) target > from && target <= to else target > from || target <= to

    private fun stepTrain(f: Fixture, dt: Float) {
        if (!f.on) {
            // Parked at the station: always the same picture, so it can be stamped.
            f.angle = UpperTrack.STATION
            return
        }
        live(f)
        val old = f.angle
        val speed = if (f.mode == 1) 0.8f else 1.25f
        f.angle += dt * speed
        f.timer += dt
        if (f.angle >= TWO_PI) {
            f.angle -= TWO_PI
            trainLaps++
            fx(UpperCodes.TRAIN_LAP, f, trainLaps)
        }
        // The engine chuffs four times a lap, with a puff of steam from the funnel.
        val q = TWO_PI / 4f
        if ((old / q).toInt() != (f.angle / q).toInt() || old > f.angle) {
            val a = f.angle
            fx(UpperCodes.TRAIN_CHUFF, f.x + f.shiftX + UpperTrack.x(a), f.y + UpperTrack.y(a) - 0.12f, f, arg = if (f.mode == 1) 1 else 0)
        }
        // Passengers: riding for a few seconds counts, once per person.
        for (i in f.spec.spots.indices) {
            val p = world.seatedAt(f, i) ?: continue
            val t = (onTrain[p.id] ?: 0f) + dt
            onTrain[p.id] = t
            if (t > 2.5f && trainCounted.add(p.id)) {
                p.anim.face = Face.LAUGH
                p.anim.faceTime = 1.6f
                p.anim.wave = 1.6f
                sim.tasks.record(Deed.UP_TRAIN, place, null, f.type)
                sim.firstTime(First.TOY_TRAIN, f.x, f.top)
            }
        }
        // It rolls into the station when told to, and after a good long run by itself.
        if (f.mode == 0 && f.timer > TRAIN_RUN) f.mode = 1
        if (f.mode == 1 && trainCrossed(old, f.angle, UpperTrack.STATION)) {
            f.on = false
            f.mode = 0
            f.angle = UpperTrack.STATION
            f.timer = 0f
            onTrain.clear()
            fx(UpperCodes.TRAIN_STOP, f, 1, 0.5f)
        }
    }

    // ------------------------------------------------------------------ blocks

    private fun knockOver(f: Fixture) {
        f.mode = 1
        f.open = true
        f.timer = 0f
        sim.invalidate(place)
        // Whatever stood on the top comes down too.
        release(f, 0.5f + random.nextFloat() * 0.4f)
        // A couple of loose blocks tumble out, never more than eight in the room.
        if (count(ThingType.UP_BLOCK) < 8) {
            repeat(2) { k ->
                toss(ThingType.UP_BLOCK, random.nextInt(6), f, 0.05f + k * 0.07f, -0.2f, 0.6f + k * 0.5f, -1.4f - k * 0.3f)
            }
        }
        fx(UpperCodes.BLOCKS_FALL, f, 0, 0.35f)
        sim.tasks.record(Deed.UP_KNOCK, place, ThingType.UP_BLOCK, f.type)
    }

    private fun rebuild(f: Fixture) {
        f.mode = 0
        f.open = false
        f.timer = 0f
        sim.invalidate(place)
        fx(UpperCodes.BLOCKS_BUILD, f, 0, 0.35f)
    }

    // ------------------------------------------------------------------ the ball pit

    private fun tapPit(f: Fixture) {
        f.count++
        f.timer = PIT_PLAY
        fx(UpperCodes.PIT_BURST, f, f.count, 0.6f)
        // Every third time a ball pops out and bounces away.
        if (f.count % 3 == 0 && count(ThingType.BALL) + count(ThingType.BEACH_BALL) < 6) {
            val ball = toss(if (f.count % 6 == 0) ThingType.BEACH_BALL else ThingType.BALL, 0, f, (random.nextFloat() - 0.5f) * 0.3f, -0.14f, (random.nextFloat() - 0.5f) * 1.4f, -2.2f)
            fx(UpperCodes.PIT_BALL, ball.x, ball.y, f, ball)
        }
        for (i in f.spec.spots.indices) {
            val p = world.seatedAt(f, i) ?: continue
            p.anim.hopV = 1.6f
            p.anim.face = Face.LAUGH
            p.anim.faceTime = 1.2f
        }
    }

    private fun stepPit(f: Fixture, dt: Float) {
        if (f.timer > 0f) {
            f.timer = max(0f, f.timer - dt)
            live(f)
        }
        val now = HashSet<Int>()
        for (b in world.bodiesIn(place)) {
            val p = b as? Person ?: continue
            val inside = (p.mode == Mode.SEATED && p.holder == f.id) || (p.mode == Mode.FREE && p.resting && p.restOwner == f.id && !p.held)
            if (!inside) continue
            now += p.id
            if (inPit.add(p.id)) {
                // Plop! In they go.
                f.timer = PIT_PLAY
                p.anim.hopV = 2.1f
                p.anim.face = Face.LAUGH
                p.anim.faceTime = 1.6f
                fx(UpperCodes.PIT_PLUNGE, p.x, f.y - 0.13f, f, arg = p.id)
            }
        }
        inPit.retainAll(now)
    }

    // ------------------------------------------------------------------ the climbing wall

    private fun climbers(f: Fixture): List<Person> = f.spec.spots.indices.mapNotNull { world.seatedAt(f, it) }

    private fun tapClimb(f: Fixture, dy: Float) {
        val on = climbers(f)
        when {
            dy < -0.52f -> fx(UpperCodes.CLIMB_TOP, f.x + 0.09f, f.y - 0.62f, f, arg = on.firstOrNull()?.id ?: 0xFFFF)
            on.isNotEmpty() -> for (p in on) climbAt[p.id] = -99f   // a friendly cheer: up you go
            else -> fx(UpperCodes.CLIMB_STEP, f.x, f.y - 0.3f, f, arg = 0xFFFF)
        }
    }

    private fun stepClimb(f: Fixture) {
        val top = f.spec.spots.size - 1
        for (i in top downTo 0) {
            val p = world.seatedAt(f, i) ?: continue
            val since = sim.time - (climbAt.getOrPut(p.id) { sim.time })
            if (i < top) {
                if (since > CLIMB_STEP_TIME && world.seatedAt(f, i + 1) == null) {
                    p.slot = i + 1
                    climbAt[p.id] = sim.time
                    p.anim.hopV = 1.0f
                    p.anim.face = Face.GRIN
                    p.anim.faceTime = 1f
                    if (i + 1 == top) {
                        fx(UpperCodes.CLIMB_TOP, f.x + 0.09f, f.y - 0.62f, f, arg = p.id)
                        p.anim.cheer = 2.0f
                    } else {
                        fx(UpperCodes.CLIMB_STEP, p.x, p.y - p.h, f, arg = p.id)
                    }
                }
            } else if (since > CLIMB_TOP_TIME) {
                // Let go: down onto the trampoline in front of the wall.
                p.mode = Mode.FREE
                p.holder = -1
                p.slot = 0
                p.resting = false
                p.restOwner = -2
                p.ground = f.depth + 0.07f
                p.vy = -0.3f
                p.vx = 0.3f
                p.anim.cheer = 0f
                climbAt.remove(p.id)
                fx(UpperCodes.CLIMB_DROP, p.x, p.y, f, arg = p.id)
            }
        }
    }

    // ------------------------------------------------------------------ the easel

    private fun paint(f: Fixture) {
        f.count++
        // Eight colours in turn; every thirteenth squirt starts a new canvas.
        val fresh = f.count % 13 == 0
        fx(UpperCodes.PAINT, f.x, f.y - 0.3f, f, arg = (f.count % 8) + (if (fresh) 0x100 else 0))
        sim.tasks.record(Deed.UP_SPLAT, place, ThingType.UP_PAINTBRUSH, f.type)
    }

    // ------------------------------------------------------------------ karaoke

    private fun stageSinger(f: Fixture): Person? = world.seatedAt(f, 0) ?: people().firstOrNull {
        it.mode == Mode.FREE && it.resting && it.restOwner == f.id
    }

    private fun startShow(f: Fixture, who: Person?) {
        f.on = true
        f.timer = SHOW_TIME
        if (who != null) {
            who.anim.face = Face.GRIN
            who.anim.faceTime = 3f
        }
        fx(UpperCodes.KARAOKE, f, who?.id ?: 0xFFFF, 0.5f)
    }

    private fun stepKaraoke(f: Fixture, dt: Float) {
        if (f.on) {
            f.timer -= dt
            if (f.timer <= 0f) {
                f.on = false
                world.seatedAt(f, 0)?.anim?.hopV = 1.8f
            }
        }
        // Whoever steps up to the microphone sings by themselves.
        val who = world.seatedAt(f, 0)
        val id = who?.id ?: -1
        if (id != singer) {
            singer = id
            if (who != null) startShow(f, who)
        }
    }

    // ------------------------------------------------------------------ the shower, the bath and the mirror

    private fun stepShower(f: Fixture, dt: Float) {
        if (!f.on) {
            showerSang = 0f
            return
        }
        f.timer += dt
        // The sun finds the steam, and a rainbow stands in the shower: a glimt comes out with it.
        if (f.timer > 1.3f && !showerShown) {
            showerShown = true
            sim.unlock("upper_bath")
            fx(UpperCodes.RAINBOW, f, 0, 0.6f)
        }
        val bather = world.seatedAt(f, 0)
        if (bather != null) {
            showerSang -= dt
            if (showerSang <= 0f) {
                showerSang = 5f
                fx(UpperCodes.SHOWER_SING, bather.x, bather.y - bather.h, f, arg = bather.id)
            }
        }
        if (f.timer > SHOWER_TIME) {
            f.on = false
            fx(UpperCodes.SHOWER, f, 0)
        }
    }

    /** A running bath with a duck in it: after the duck party (see [Sim]) the duck hiccups up the golden key. */
    private fun stepBath(f: Fixture, dt: Float) {
        if (!f.on || "manor_key_upper" in world.flags) {
            f.angleV = 0f
            return
        }
        val duck = world.bodiesIn(place).firstOrNull { b ->
            b is Thing && b.type == ThingType.DUCK && b.mode == Mode.FREE && !b.held && sim.poolAt(b.x, b.y)?.owner == f.id
        } as Thing?
        if (duck == null) {
            f.angleV = 0f
            return
        }
        f.angleV += dt
        if (f.angleV >= DUCK_KEY_TIME) {
            f.angleV = 0f
            sim.flag("manor_key_upper")
            val key = toss(ThingType.GOLDEN_KEY, 0, f, duck.x - f.x, duck.y - duck.h - f.y, 0.55f, -2.6f)
            key.vrot = 380f
            fx(UpperCodes.DUCK_KEY, duck.x, duck.y - duck.h, f, key, arg = duck.id)
        }
    }

    private fun stepMirror(f: Fixture, dt: Float) {
        val steam = listOf(HouseUpperIx.SHOWER, HouseUpperIx.BATH, HouseUpperIx.SINK).any { fixture(it)?.on == true }
        f.timer = if (steam) min(f.timer + dt, 6f) else max(0f, f.timer - dt * 0.6f)
        if (f.mode == 0 && f.timer > 2.5f) {
            f.mode = 1
            f.anim = 1f
            fx(UpperCodes.MIRROR, f, 1)
        } else if (f.mode != 0 && !steam && f.timer < 0.4f) {
            f.mode = 0
            f.anim = 1f
        }
    }

    private fun tapMirror(f: Fixture) {
        when (f.mode) {
            0 -> fx(UpperCodes.MIRROR, f, 0)
            1 -> {
                // A finger draws a smiley in the fog.
                f.mode = 2
                f.anim = 1f
                fx(UpperCodes.MIRROR, f, 2)
            }
            else -> {
                f.mode = 0
                f.timer = 0f
                f.anim = 1f
                fx(UpperCodes.MIRROR, f, 3)
            }
        }
    }

    private fun tapTowels(f: Fixture) {
        f.angleV += 4.2f
        // Now and then a rubber duck has been hiding in the towels.
        if (count(ThingType.DUCK) < 6 && random.nextFloat() < 0.55f) {
            toss(ThingType.DUCK, 0, f, (random.nextFloat() - 0.5f) * 0.1f, -0.04f, (random.nextFloat() - 0.5f) * 0.5f, -0.3f)
            fx(UpperCodes.TOWEL, f, 1)
        } else {
            fx(UpperCodes.TOWEL, f, 0)
        }
    }

    // ------------------------------------------------------------------ the wardrobe

    private class Outfit(val top: Int, val topColor: Int, val bottom: Int, val bottomColor: Int, val shoes: Int, val hat: ThingType?, val hatVariant: Int = 0, val glasses: ThingType? = null)

    private fun tapWardrobe(f: Fixture) {
        f.open = !f.open
        f.timer = 0f
        sim.invalidate(place)
        if (!f.open) {
            fx(UpperCodes.WARDROBE, f, 0)
            return
        }
        fx(UpperCodes.WARDROBE, f, 1)
        val who = nearest(f.x, 0.5f)
        if (who == null) {
            // Nobody to dress: a top and a funny hat tumble out instead.
            if (count(ThingType.GARMENT) < 4) toss(ThingType.GARMENT, Garment.pack(random.nextInt(Styles.TOPS), random.nextInt(Palette.cloth.size)), f, 0.02f, -0.3f, 0.5f, -1.3f)
            if (count(ThingType.UP_SLIPPER) < 3) toss(ThingType.UP_SLIPPER, random.nextInt(3), f, -0.03f, -0.4f, -0.4f, -1.6f)
            fx(UpperCodes.WARDROBE_TOSS, f, 0)
            return
        }
        dress(f, who)
    }

    private fun dress(f: Fixture, who: Person) {
        val outfit = OUTFITS[(f.count + f.id) % OUTFITS.size]
        f.count++
        if (who.species == Species.FOLK) {
            who.look = who.look.copy(top = outfit.top, topColor = outfit.topColor, bottom = outfit.bottom, bottomColor = outfit.bottomColor, shoes = outfit.shoes).safe()
        }
        val hat = if (who.species == Species.FOLK) outfit.hat else listOf(ThingType.PARTY_HAT, ThingType.BOW, ThingType.UP_SLIPPER)[f.count % 3]
        if (hat != null) {
            val variant = if (who.species == Species.FOLK) outfit.hatVariant else f.count % 3
            sim.give(who, world.addThing(hat, variant, place, who.x, who.y - who.h), Part.HAT)
        }
        if (who.species == Species.FOLK) outfit.glasses?.let { sim.give(who, world.addThing(it, 0, place, who.x, who.y - who.h), Part.GLASSES) }
        who.anim.spin = 1f
        who.anim.sparkle = 1f
        who.anim.hopV = 2.4f
        who.anim.cheer = 2.4f
        fx(UpperCodes.DRESS, who.x, who.y - who.h * 0.6f, f, arg = who.id)
        sim.tasks.record(Deed.UP_DRESS, place, null, f.type)
    }

    // ------------------------------------------------------------------ the jewellery box

    private fun stopJewel(f: Fixture) {
        f.on = false
        f.angle = 0f
        f.timer = 0f
        fx(UpperCodes.JEWEL, f, 0)
    }

    private fun stepJewel(f: Fixture, dt: Float) {
        if (!f.on) return
        live(f)
        f.timer -= dt
        f.angle += dt * 3f
        if (f.timer <= 0f) stopJewel(f)
    }

    // ------------------------------------------------------------------ swings and rockers

    private fun stepRocker(f: Fixture, dt: Float) {
        if (world.seatedAt(f, 0) != null) {
            f.angle = 0.11f * sin(sim.time * 2.6f)
            f.angleV = 0f
            live(f)
            return
        }
        f.angleV += (-f.angle * 34f - f.angleV * 1.7f) * dt
        f.angle += f.angleV * dt
        if (abs(f.angle) < 0.003f && abs(f.angleV) < 0.05f) {
            f.angle = 0f
            f.angleV = 0f
        } else {
            live(f)
        }
    }

    private fun stepHanging(f: Fixture, dt: Float) {
        val occupied = world.seatedAt(f, 0) != null
        f.angleV += (-f.angle * 9f - f.angleV * (if (occupied) 0.5f else 0.8f) + (if (occupied) 0.4f * cos(sim.time * 3f) else 0f)) * dt
        f.angle += f.angleV * dt
        if (abs(f.angle) < 0.004f && abs(f.angleV) < 0.03f && !occupied) {
            f.angle = 0f
            f.angleV = 0f
        } else {
            live(f)
        }
    }

    private fun stepSway(f: Fixture, dt: Float) {
        // Towels and the mobile: a pendulum (towels) or a spinner that slows down (mobile).
        if (f.type == FixtureType.UP_MOBILE) {
            f.angle += f.angleV * dt
            if (f.angle > TWO_PI) f.angle -= TWO_PI
            f.angleV *= exp(-0.7f * dt)
            if (abs(f.angleV) > 0.08f) live(f)
        } else {
            f.angleV += (-f.angle * 30f - f.angleV * 2f) * dt
            f.angle += f.angleV * dt
            if (abs(f.angle) < 0.004f && abs(f.angleV) < 0.05f) {
                f.angle = 0f
                f.angleV = 0f
            } else {
                live(f)
            }
        }
    }

    // ------------------------------------------------------------------ the bird feeder

    private fun refill(f: Fixture) {
        f.count = 0
        f.timer = 1.2f
        fx(UpperCodes.FEEDER_FILL, f, 0)
    }

    private fun stepFeeder(f: Fixture, dt: Float) {
        if (f.angle > 0f) {
            live(f)
            val before = f.angle
            f.angle += dt / BIRD_VISIT
            if (before < 0.14f && f.angle >= 0.14f) {
                sim.unlock("upper_balcony")
                fx(UpperCodes.BIRD, f.x, f.y - 0.1f, f, arg = f.mode)
            }
            if (f.angle >= 1f) {
                f.angle = 0f
                f.count++
                f.timer = 2.5f + random.nextFloat() * 3.5f
            }
        } else if (f.count < FEEDER_VISITS) {
            f.timer -= dt
            if (f.timer <= 0f) {
                f.mode = random.nextInt(4)
                f.angle = 0.001f
            }
        }
    }

    // ------------------------------------------------------------------ the rest of the frame

    override fun step(place: PlaceId, f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.UP_PORTRAIT -> stepPortrait(f, dt)
            FixtureType.UP_TOY_TRAIN -> stepTrain(f, dt)
            FixtureType.UP_BLOCKS -> {
                f.timer = min(f.timer + dt, 5f)
                if (f.timer < 2.6f) live(f)
                // `open` is not saved for furniture without a cupboard, so a fallen tower after loading is put right here.
                if (f.open != (f.mode == 1)) {
                    f.open = f.mode == 1
                    sim.invalidate(place)
                }
            }
            FixtureType.UP_DOLLHOUSE -> if (f.timer > 0f) {
                f.timer = max(0f, f.timer - dt)
                live(f)
            }
            FixtureType.UP_PUPPET_THEATER -> if (f.mode != 0) {
                f.timer += dt
                if (f.timer > 25f) {
                    f.mode = 0
                    f.timer = 0f
                    fx(UpperCodes.PUPPET, f, 0)
                }
            } else {
                f.timer = 0f
            }
            FixtureType.UP_WARDROBE -> if (f.open) {
                f.timer += dt
                if (f.timer > 15f) {
                    f.open = false
                    f.timer = 0f
                    sim.invalidate(place)
                    fx(UpperCodes.WARDROBE, f, 0)
                }
            }
            FixtureType.UP_POSTER -> if (f.timer > 0f) {
                f.timer = max(0f, f.timer - dt)
                live(f)
            }
            FixtureType.UP_MOBILE, FixtureType.UP_TOWELS -> stepSway(f, dt)
            FixtureType.UP_BALL_PIT -> stepPit(f, dt)
            FixtureType.UP_CLIMBING_WALL -> stepClimb(f)
            FixtureType.UP_KARAOKE -> stepKaraoke(f, dt)
            FixtureType.UP_SHOWER -> stepShower(f, dt)
            FixtureType.BATH -> stepBath(f, dt)
            FixtureType.UP_BATH_MIRROR -> stepMirror(f, dt)
            FixtureType.UP_JEWEL_BOX -> stepJewel(f, dt)
            FixtureType.UP_ROCKING_CHAIR -> stepRocker(f, dt)
            FixtureType.UP_HANGING_CHAIR -> stepHanging(f, dt)
            FixtureType.UP_BIRD_FEEDER -> stepFeeder(f, dt)
            else -> Unit
        }
    }

    override fun tick(place: PlaceId, dt: Float) {
        mirrorClock -= dt
        if (mirrorClock <= 0f) {
            mirrorClock = 0.25f
            UpperMirror.update(world)
            // The dollhouse picture is stamped once per look: the arrangement of its figures is part of the look.
            fixture(HouseUpperIx.DOLLHOUSE)?.let { if (it.count != UpperMirror.signature) it.count = UpperMirror.signature }
        }
        wanderClock -= dt
        if (wanderClock <= 0f) {
            wanderClock = 5f + random.nextFloat() * 8f
            invite()
        }
        portraitClock -= dt
        if (portraitClock <= 0f) {
            portraitClock = 22f + random.nextFloat() * 25f
            portraits().randomOrNull(random)?.takeIf { it.timer <= 0f }?.let { wave(it, quiet = true) }
        }
    }

    /**
     * Somebody who has been standing about wanders over to something fun: a seat in the ball pit, the rocking
     * chair, the window seat or the hanging chair, a hold on the climbing wall, the microphone. Life takes them
     * there and sits them down (see [Life]); by day only, because at night they go to bed.
     */
    private fun invite() {
        if (world.night) return
        val who = people().filter {
            it.species == Species.FOLK && it.mode == Mode.FREE && it.resting && it.restOwner == -1 &&
                it.anim.walkTo.isNaN() && it.anim.wish == null && it.anim.still >= 2f
        }.randomOrNull(random) ?: return
        val options = ArrayList<Pair<Fixture, Int>>()
        for (f in world.fixturesIn(place)) {
            if (f.type !in INVITING || abs(f.x - who.x) > 3.2f) continue
            // Only the bottom hold of the wall: the climbing goes on by itself from there.
            val spots = if (f.type == FixtureType.UP_CLIMBING_WALL) listOf(0) else f.spec.spots.indices.toList()
            for (i in spots) if (world.seatedAt(f, i) == null) options += f to i
        }
        val (f, spot) = options.randomOrNull(random) ?: return
        val a = who.anim
        a.goal = 1
        a.goalFixture = f.id
        a.goalSpot = spot
        a.walkTo = f.x + f.shiftX + f.spec.spots[spot].dx
        a.walkGround = (f.depth + 0.03f).coerceAtMost(PlaceId.FRONT - 0.01f)
    }

    // ------------------------------------------------------------------ seats that move

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? = when (f.type) {
        FixtureType.UP_TOY_TRAIN -> {
            val a = f.angle - (spot + 1) * UpperTrack.GAP
            floatArrayOf(f.x + f.shiftX + UpperTrack.x(a), f.y + UpperTrack.y(a) - 0.045f)
        }
        FixtureType.UP_ROCKING_CHAIR -> floatArrayOf(f.x + f.shiftX + sin(f.angle) * 0.09f, f.y - cos(f.angle) * 0.09f)
        FixtureType.UP_HANGING_CHAIR -> floatArrayOf(f.x + f.shiftX + sin(f.angle) * 0.31f, f.y - 0.46f + cos(f.angle) * 0.31f)
        FixtureType.UP_BALL_PIT -> {
            val s = f.spec.spots[spot]
            val bob = if (f.timer > 0f) sin(sim.time * 9f + spot * 2f) * 0.012f * min(1f, f.timer) else 0f
            floatArrayOf(f.x + f.shiftX + s.dx, f.y + s.dy + bob)
        }
        else -> null
    }

    // ------------------------------------------------------------------ things dropped on furniture

    override fun drop(place: PlaceId, f: Fixture, t: Thing): Boolean {
        when (f.type) {
            // Anything let go over the tower knocks it down; it still falls on its own way.
            FixtureType.UP_BLOCKS -> if (f.mode == 0) knockOver(f)
            FixtureType.UP_BIRD_FEEDER -> if (t.type == ThingType.SEEDS) {
                sim.removeThing(t, quiet = true)
                refill(f)
                return true
            }
            FixtureType.UP_EASEL -> if (t.type == ThingType.UP_PAINTBRUSH) paint(f)
            else -> Unit
        }
        return false
    }

    companion object {
        /** Holding [Fixture.anim] just above this keeps a fixture drawn live (the engine stamps anything else). */
        const val LIVE = 0.012f
        const val TWO_PI = 6.2831855f
        const val PORTRAIT_WAVE = 1.8f
        const val POSTER_TIME = 1.4f
        const val DOLL_WAVE = 2.6f
        const val PIT_PLAY = 2.4f
        const val CLIMB_STEP_TIME = 1.6f
        const val CLIMB_TOP_TIME = 2.0f
        const val SHOW_TIME = 9f
        const val SHOWER_TIME = 26f
        const val JEWEL_TIME = 9.5f
        const val TRAIN_RUN = 70f
        const val DUCK_KEY_TIME = 5.4f
        const val BIRD_VISIT = 5.2f
        const val FEEDER_VISITS = 6

        /** What somebody with nothing to do may wander over to. */
        val INVITING = setOf(
            FixtureType.UP_BALL_PIT, FixtureType.UP_ROCKING_CHAIR, FixtureType.UP_WINDOW_SEAT, FixtureType.UP_HANGING_CHAIR,
            FixtureType.UP_CLIMBING_WALL, FixtureType.UP_KARAOKE,
        )

        private val OUTFITS = listOf(
            Outfit(0, 9, 0, 10, 9, ThingType.SPACE_HELMET),                       // astronaut
            Outfit(2, 8, 2, 8, 8, ThingType.CROWN, 0),                            // princess
            Outfit(2, 7, 0, 11, 11, ThingType.WIZARD_HAT),                        // wizard
            Outfit(0, 9, 0, 11, 9, ThingType.CHEF_HAT),                           // chef
            Outfit(5, 0, 0, 11, 12, ThingType.VIKING_HELMET),                     // viking
            Outfit(4, 2, 0, 5, 0, ThingType.SUN_HAT),                             // a ray of sunshine
            Outfit(1, 8, 1, 4, 2, ThingType.PARTY_HAT, 1),                        // birthday
            Outfit(2, 3, 2, 3, 9, ThingType.FLOWER_CROWN),                        // flower fairy
            Outfit(0, 11, 0, 11, 7, null, glasses = ThingType.STAR_GLASSES),      // rock star
            Outfit(3, 6, 1, 6, 6, ThingType.UP_SLIPPER, 0),                       // a bunny slipper on the head: very fashionable
            Outfit(1, 3, 0, 12, 5, ThingType.NISSE_HAT),                          // little gnome
        )
    }
}
