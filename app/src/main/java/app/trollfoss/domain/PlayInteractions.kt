package app.trollfoss.domain

/** Visible cause/effect connections, shared by all places and moved furniture. */
object PlayInteractions {
    private val basins = setOf(FixtureType.SINK, FixtureType.BATH, FixtureType.WATER_TROUGH)
    private val fires = setOf(FixtureType.STOVE, FixtureType.CAMPFIRE, FixtureType.WOOD_STOVE, FixtureType.GR_FIREPLACE, FixtureType.GA_GRILL)
    private val plants = setOf(FixtureType.PLANT_BIG, FixtureType.FLOWER_POT, FixtureType.GR_PLANT, FixtureType.GA_FLOWER_BED)
    private val instruments = setOf(ThingType.GUITAR, ThingType.DRUM, ThingType.MICROPHONE, ThingType.AT_RECORD)
    private val music = setOf(FixtureType.PIANO, FixtureType.RADIO, FixtureType.CE_JUKEBOX, FixtureType.CE_KARAOKE)
    private val mirrors = setOf(FixtureType.MIRROR, FixtureType.UP_BATH_MIRROR)
    private val carriers = setOf(ThingType.CUP, ThingType.BUCKET, ThingType.WATERING_CAN)
    private val lamps = setOf(FixtureType.LAMP, FixtureType.LAMP_POST, FixtureType.GR_CHANDELIER, FixtureType.CE_BULB)
    private val beds = setOf(FixtureType.BED, FixtureType.BUNK_BED, FixtureType.SPACE_BED)

    fun water(t: Thing) = t.type == ThingType.WATERING_CAN || (t.type in carriers && t.used > 0)
    fun accepts(f: Fixture, t: Thing): Boolean = when {
        ToyPlay.accepts(f, t) -> true
        f.type in basins -> t.type in carriers || t.type == ThingType.DUCK || t.type == ThingType.SNOWBALL || (t.type.variants>1 && t.type.cat !in setOf(Cat.HAT,Cat.GARMENT))
        f.type in fires -> water(t) || t.type == ThingType.SNOWBALL || t.type == ThingType.STICK || t.type == ThingType.PLANK
        f.type in plants -> water(t) || t.type == ThingType.SEEDS
        f.type in music -> t.type in instruments
        f.type in mirrors -> t.type == ThingType.AT_FLASHLIGHT || t.type == ThingType.WAND
        f.type == FixtureType.SANDCASTLE -> t.type == ThingType.SPADE || t.type == ThingType.BUCKET
        f.type == FixtureType.SNOWMAN -> t.type == ThingType.SNOWBALL || t.type == ThingType.CARROT || t.type == ThingType.STICK
        f.type in lamps -> t.type == ThingType.WRENCH || t.type == ThingType.SCREWDRIVER
        f.type == FixtureType.TV || f.type == FixtureType.GR_TV -> t.type == ThingType.PHONE
        f.type == FixtureType.AQUARIUM -> water(t) || t.type == ThingType.DUCK
        f.type in beds -> t.type == ThingType.PILLOW || t.type == ThingType.TEDDY
        f.type == FixtureType.TRAMPOLINE -> t.type == ThingType.BALL || t.type == ThingType.BEACH_BALL
        else -> false
    }
    fun zone(f: Fixture, t: Thing): RRect? = if (accepts(f, t)) RRect(-f.spec.w / 2f - 0.025f, -f.spec.h - 0.06f, f.spec.w / 2f + 0.025f, 0.02f) else null

    /** A connection happened; tools remain available for the next experiment. */
    fun apply(sim: Sim, place: PlaceId, f: Fixture, t: Thing): Boolean {
        if (!accepts(f, t)) return false
        when {
            f.type in basins -> {
                sim.toys.wash(t)
                f.on = true
                if (t.type in carriers) t.used = 1
                if (t.type == ThingType.SNOWBALL) { t.type = ThingType.SLIME; t.variant = 1 }
                sim.listener.onFx(if (t.type == ThingType.DUCK) Fx.DUCK else Fx.WATER, f.x, f.top, f, t)
                sim.invalidate(place)
                for (p in sim.world.bodiesIn(place).filterIsInstance<Person>().filter { it.holder == f.id && it.mode == Mode.SEATED }) {
                    p.anim.cream = 0f; p.anim.ink = 0f; p.anim.sparkle = 1f; p.anim.face = Face.GRIN; p.anim.faceTime = 2f
                }
            }
            f.type in fires -> {
                f.on = !(water(t) || t.type == ThingType.SNOWBALL)
                if (!f.on) {
                    if (t.type == ThingType.SNOWBALL) { t.type = ThingType.SLIME; t.variant = 1 }
                    else if (t.type != ThingType.WATERING_CAN) t.used = 0
                    sim.world.bodiesIn(place).filterIsInstance<Thing>().filter { it.restOwner == f.id }.forEach { it.cook = 0f }
                }
                sim.listener.onFx(if (f.on) Fx.SPARKLE else Fx.WATER, f.x, f.top, f, t)
            }
            f.type in plants -> {
                f.anim = 1f
                f.count++
                if (t.type == ThingType.SEEDS && f.count % 3 == 0) {
                    val flower = sim.world.addThing(ThingType.FLOWER, f.count.mod(5), place, f.x, f.top)
                    flower.vy = -0.8f
                    sim.listener.onSpawn(flower)
                } else if (t.type != ThingType.SEEDS && t.type != ThingType.WATERING_CAN) t.used = 0
                sim.listener.onFx(Fx.GROW, f.x, f.top, f, t, param = f.count.mod(4))
            }
            f.type in music -> {
                f.on = true
                f.anim = 1f
                for (p in sim.world.bodiesIn(place).filterIsInstance<Person>()) {
                    if (kotlin.math.abs(p.x - f.x) > 1.2f || p.held) continue
                    p.anim.cheer = 3f; p.anim.dance = 3f; p.anim.face = Face.GRIN; p.anim.faceTime = 3f
                }
                sim.listener.onFx(if (t.type == ThingType.DRUM) Fx.DRUM else Fx.STRUM, f.x, f.top, f, t)
            }
            f.type in mirrors -> {
                sim.listener.onFx(Fx.STARRAIN, f.x, f.top, f, t)
                sim.egg("mirror_light")
            }
            f.type == FixtureType.SANDCASTLE -> {
                if (water(t)) { f.mode = 0; if (t.type != ThingType.WATERING_CAN) t.used = 0 }
                else f.mode = (f.mode + 1).coerceAtMost(4)
                sim.listener.onFx(if (f.mode == 0) Fx.CRUMBLE else Fx.BUILD, f.x, f.top, f, t, param = f.mode)
                if (f.mode == 4) sim.unlock("beach_castle")
            }
            f.type == FixtureType.SNOWMAN -> {
                f.mode = if (t.type == ThingType.CARROT) 4 else (f.mode + 1).coerceAtMost(4)
                sim.listener.onFx(Fx.BUILD, f.x, f.top, f, t, param = f.mode)
            }
            f.type in lamps -> { f.on = true; sim.listener.onFx(Fx.SPARKLE, f.x, f.top, f, t) }
            f.type == FixtureType.TV || f.type == FixtureType.GR_TV -> {
                f.mode = (f.mode + 1) % Sim.TV_CHANNELS; f.on = f.mode != 0
                sim.personPlay.fixture(f)
                sim.listener.onFx(Fx.CHANNEL, f.x, f.top, f, t, param = f.mode)
            }
            f.type == FixtureType.AQUARIUM -> { f.on = true; if (t.type == ThingType.BUCKET || t.type == ThingType.CUP) t.used = 0; sim.listener.onFx(Fx.BUBBLES, f.x, f.top, f, t) }
            f.type in beds -> {
                for (p in sim.world.bodiesIn(place).filterIsInstance<Person>().filter { it.holder == f.id && it.mode == Mode.SEATED }) {
                    p.anim.face = Face.SLEEP; p.anim.faceTime = 5f; p.anim.say = 0; p.anim.sayTime = 1.5f
                }
                sim.listener.onFx(Fx.FLUFF, f.x, f.top, f, t)
            }
            f.type == FixtureType.TRAMPOLINE -> {
                t.mode = Mode.FREE; t.holder = -1; t.inside = -1; t.resting = false
                t.x = f.x; t.y = f.top - 0.015f; t.vy = -2.4f; t.vx = 0.3f
                sim.listener.onFx(Fx.BOING, f.x, f.top, f, t)
            }
        }
        f.anim = 1f
        return true
    }
}
