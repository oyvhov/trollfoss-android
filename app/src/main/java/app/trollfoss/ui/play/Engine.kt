package app.trollfoss.ui.play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import app.trollfoss.ui.art.safeRadialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Anatomy
import app.trollfoss.domain.Body
import app.trollfoss.domain.Decor
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Fx
import app.trollfoss.domain.Give
import app.trollfoss.domain.Jokes
import app.trollfoss.domain.Mode
import app.trollfoss.domain.Part
import app.trollfoss.domain.Person
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Secret
import app.trollfoss.domain.Sim
import app.trollfoss.domain.SimListener
import app.trollfoss.domain.Slot
import app.trollfoss.domain.Species
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType
import app.trollfoss.domain.Weather
import app.trollfoss.domain.Wish
import app.trollfoss.domain.WishEvent
import app.trollfoss.domain.WishKind
import app.trollfoss.domain.World
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawFixtureBack
import app.trollfoss.ui.art.drawFixtureFront
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.art.drawPlaceBack
import app.trollfoss.ui.art.drawPlaceFront
import app.trollfoss.ui.art.drawThing
import app.trollfoss.ui.art.groundShadow
import app.trollfoss.ui.art.headWidth
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.shine
import app.trollfoss.ui.art.starPath
import app.trollfoss.ui.art.twinkle
import app.trollfoss.ui.components.Icons
import app.trollfoss.ui.theme.T
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/** What the engine asks of the app around it: sound, vibration, saving and the screens it opens. */
interface EngineHost {
    fun sfx(effect: Sfx, volume: Float = 0.9f, rate: Float = 1f)
    fun haptic()
    fun changed()
    fun secretFound(id: String)
    fun discovered(key: String)
    fun telescope()
    fun radio(on: Boolean)

    /** An Easter egg was found for the first time. */
    fun egg(id: String)
}

/**
 * One place brought to life: the camera, the fingers, the figures' little lives, particles, light and
 * weather, and the drawing of it all. The rules of the world live in [Sim]; this class turns touches
 * into rules and rules into things you can see and hear.
 *
 * Coordinates: the scene is one unit tall and [PlaceId.width] units wide. [u] is pixels per unit (the
 * screen height) and [cam] the scene x at the left screen edge.
 */
class Engine(
    val world: World,
    val place: PlaceId,
    private val sim: Sim,
    private val host: EngineHost,
    private val motion: Boolean,
    startCam: Float,
) : SimListener {
    private val random = Random.Default

    var widthPx = 1f
        private set
    var heightPx = 1f
        private set
    private var density = 1f
    /**
     * Pixels per scene unit. On wide phones the scene fills the height; on tablets it zooms out so at
     * least [MIN_VIEW] units show across, and the scene sits on the bottom edge with sky (or wall) above.
     */
    val u: Float get() = min(heightPx, widthPx / MIN_VIEW)
    val viewport: Float get() = widthPx / u

    /** Screen pixels above scene y = 0; the place art fills them with more sky or wall. */
    private val top: Float get() = heightPx - u

    var cam = startCam
        private set
    private var camV = 0f
    var time = 0f
        private set
    private var night = if (world.night) 1f else 0f
    var rainbow = 0f
    private var flash = 0f
    private val particles = Particles(random)
    private val sprites = SpriteCache()

    /** Where the glimt counter sits on screen, so found stars can fly to it. Set by the HUD. */
    var counterTarget = Offset(0f, 0f)

    var bagOpen by mutableStateOf(false)

    /** The home designer is open: furniture moves with a plain drag, and the panel below takes it away. */
    var designMode by mutableStateOf(false)

    /** Screen area of the designer panel; furniture let go over it goes into the store. */
    var storeZone: Rect? = null

    /** A piece of furniture is being dragged over the panel. */
    var overStore by mutableStateOf(false)

    /** Bumps whenever the designer changes something, so its panel redraws. */
    var designVersion by mutableIntStateOf(0)
    var bagCount by mutableIntStateOf(0)
        private set
    var found by mutableIntStateOf(world.found.size)
        private set

    private val appeared = HashMap<String, Float>()

    /** When each thought bubble popped up, for its pop-in. */
    private val bubbleBorn = HashMap<Int, Float>()

    /** The wish whose thing twinkles after a tap on the wisher. */
    private var hint: Wish? = null
    private var hintUntil = 0f
    private val flights = ArrayList<Flight>()
    private val grabs = HashMap<Long, Grab>()
    private var lastBabble = 0f
    private var shake = 0f

    /** Where a held thing would go if let go now: a glowing ring there, and a figure that reacts. */
    private class Preview(val at: Offset, val radius: Float)
    private val previews = ArrayList<Preview>()

    private class Flight(val from: Offset, var t: Float = 0f)

    private sealed interface Target {
        class Hold(val body: Body) : Target
        class FromBag(val body: Body) : Target
        object BagButton : Target
        object Pan : Target

        /** A piece of furniture picked up with a long press, to be moved («heimedesignar»). */
        class Furniture(val fixture: Fixture) : Target
    }

    private class Grab(var target: Target, val down: Offset, val downTime: Long, val born: Float) {
        var moved = false
        var offX = 0f
        var offY = 0f
        var finger = down
        val tracker = VelocityTracker()
    }

    // Weather, in screen fractions.
    private val drops = FloatArray(140 * 2) { random.nextFloat() }
    private val flakes = FloatArray(110 * 2) { random.nextFloat() }

    init {
        sim.listener = this
        sim.settle(place)
        bagCount = world.bag().size
    }

    // ---------------------------------------------------------------------------------- geometry

    fun setSize(width: Float, height: Float, density: Float) {
        widthPx = max(1f, width)
        heightPx = max(1f, height)
        this.density = density
        clampCam()
    }

    private fun clampCam() {
        val maxCam = max(0f, place.width - viewport)
        cam = if (place.width <= viewport) (place.width - viewport) / 2f else cam.coerceIn(0f, maxCam)
    }

    private fun sx(x: Float) = (x - cam) * u
    private fun sy(y: Float) = y * u
    private fun dp(v: Float) = v * density
    private fun units(px: Float) = px / u
    private fun toScene(p: Offset) = Offset(p.x / u + cam, (p.y - top) / u)

    private val bagRadius get() = dp(38f)
    private val bagCenter get() = Offset(widthPx - dp(20f) - bagRadius, heightPx - dp(20f) - bagRadius)

    // ---------------------------------------------------------------------------------- update

    fun update(dt: Float) {
        time += dt
        val targetNight = if (world.night) 1f else 0f
        night += (targetNight - night) * min(1f, dt * 1.6f)
        rainbow = max(0f, rainbow - dt / 14f)
        flash = max(0f, flash - dt * 2.5f)

        for (f in world.fixturesIn(place)) if (f.type == FixtureType.MAILBOX) f.mode = if (sim.giftWaiting()) 1 else 0
        sim.step(place, dt)

        // Held bodies follow their fingers with a little lag, which reads as weight.
        val follow = 1f - exp(-22f * dt)
        for (g in grabs.values) {
            val body = heldBody(g) ?: continue
            if (!g.moved) continue
            val p = toScene(g.finger)
            val tx = p.x + g.offX
            val ty = p.y + g.offY
            val ox = body.x
            body.x += (tx - body.x) * follow
            body.y += (ty - body.y) * follow
            body.vx = (body.x - ox) / max(dt, 0.001f)
            if (body is Thing) body.rot += (-body.rot) * follow * 0.3f + body.vx * 0.4f
            // Down on the floor band a held body picks its depth: further up is further back.
            if (body.y >= place.back) body.ground = min(body.y, PlaceId.FRONT)
        }

        moveFurniture(dt)
        updateSky(dt)
        updateWeather(dt)
        // Things flying home when tidied leave a trail of sparkles.
        if (motion) for (b in world.bodiesIn(place)) if (b.flyT >= 0f && random.nextFloat() < dt * 30f) {
            particles.add(Particle(PKind.SPARK, b.x, b.y - b.h / 2, 0f, 0f, 0.5f, 0.01f, T.SunTop))
        }

        // A thing carried to the screen edge takes the camera with it.
        if (grabs.values.any { it.moved && (heldBody(it) != null || it.target is Target.Furniture) }) {
            val edge = grabs.values.filter { it.moved && (heldBody(it) != null || it.target is Target.Furniture) }.map { it.finger.x }
            // With the designer panel open, the right edge is where the panel starts; over the panel
            // itself the camera stays put, so furniture can be dropped into the store.
            val panel = if (designMode) storeZone else null
            val right = panel?.left?.minus(dp(36f)) ?: (widthPx * 0.92f)
            for (x in edge) {
                if (x < widthPx * 0.08f) cam -= 1.3f * dt
                if (x > right && (panel == null || x < panel.left)) cam += 1.3f * dt
            }
            clampCam()
        } else if (grabs.values.none { it.target is Target.Pan && it.moved }) {
            cam += camV * dt
            camV *= exp(-3.5f * dt)
            clampCam()
        }

        shake *= exp(-7f * dt)
        updatePreviews()
        updatePeople(dt)
        particles.update(dt)
        ambient(dt)
        flights.removeAll { it.t += dt / 0.75f; it.t >= 1f }
        bagCount = world.bag().size
    }

    private fun accepts(f: Fixture, t: Thing): Boolean = when (f.spec.machine) {
        app.trollfoss.domain.Machine.BLENDER -> !f.on && world.inMachine(f).size < 3
        app.trollfoss.domain.Machine.CAULDRON -> !f.on && world.inMachine(f).size < 2
        app.trollfoss.domain.Machine.TOILET -> true
        app.trollfoss.domain.Machine.TRASH -> true
        app.trollfoss.domain.Machine.FOUNTAIN -> t.type == ThingType.COIN
        app.trollfoss.domain.Machine.BUILD -> if (t.type.buildTool) world.inMachine(f).isNotEmpty() else world.inMachine(f).size < 2
        app.trollfoss.domain.Machine.GARDEN -> (t.type == ThingType.SEEDS && f.mode == 0) || (t.type == ThingType.WATERING_CAN && f.mode in 1..2)
        else -> false
    }

    /**
     * Anticipation: while something is carried, show where it would land and let the figure there react
     * — a mouth opens for food, eyes look up for a hat. Children learn the rules without words.
     */
    private fun updatePreviews() {
        previews.clear()
        for (g in grabs.values) {
            if (!g.moved) continue
            val body = heldBody(g) ?: continue
            if (hypot(g.finger.x - bagCenter.x, g.finger.y - bagCenter.y) < bagRadius * 1.4f) continue
            when (body) {
                is Thing -> {
                    val center = Offset(body.x, body.y - body.h / 2)
                    val finger = toScene(g.finger)
                    val machine = world.fixturesIn(place).firstOrNull { f ->
                        val zone = f.spec.dropZone ?: return@firstOrNull false
                        val fx = f.x + f.shiftX
                        (zone.contains(finger.x - fx, finger.y - f.y) || zone.contains(center.x - fx, center.y - f.y)) && accepts(f, body)
                    }
                    if (machine != null) {
                        val zone = machine.spec.dropZone!!
                        previews += Preview(Offset(machine.x + machine.shiftX + (zone.left + zone.right) / 2, machine.y + max(zone.top, -machine.spec.h) + 0.02f), 0.05f)
                        continue
                    }
                    val target = giveTarget(body, center) ?: continue
                    val (p, part) = target
                    val a = Anatomy.at(p, part)
                    val k = max(0.6f, p.h / 0.31f)
                    previews += Preview(Offset(a[0], a[1] - p.anim.hop - (if (part == Part.HAT) 0.02f * k else 0f)), 0.035f * k)
                    when (part) {
                        Part.MOUTH -> {
                            p.anim.face = Face.OOH
                            p.anim.faceTime = 0.25f
                        }
                        Part.HAT, Part.HAIR, Part.GLASSES, Part.BODY -> {
                            p.anim.face = Face.WOW
                            p.anim.faceTime = 0.25f
                        }
                        else -> Unit
                    }
                }
                is Person -> {
                    val seat = sim.freeSeatNear(place, body, body.x, body.y, 0.13f) ?: continue
                    val point = sim.seatPoint(seat.first, seat.second)
                    previews += Preview(Offset(point[0], point[1] - 0.01f), 0.045f)
                }
            }
        }
    }

    private fun DrawScope.drawPreviews(lw: Float) {
        for (p in previews) {
            val c = Offset(sx(p.at.x), sy(p.at.y))
            val pulse = 1f + sin(time * 8f) * 0.12f
            val r = p.radius * u * pulse
            glow(listOf(T.SunTop.copy(alpha = 0.5f), Color.Transparent), c, r * 1.6f)
            drawCircle(Color.White.copy(alpha = 0.9f), r, c, style = Stroke(lw * 2.2f))
            drawCircle(T.Sun, r, c, style = Stroke(lw * 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.35f, r * 0.25f), time * r)))
        }
    }
    private fun heldBody(g: Grab): Body? = when (val t = g.target) {
        is Target.Hold -> t.body
        is Target.FromBag -> t.body
        else -> null
    }

    private fun updatePeople(dt: Float) {
        listen(dt)
        val radio = world.fixturesIn(place).firstOrNull { it.type == FixtureType.RADIO && it.on }
        val discoOn = disco() != null
        val held = grabs.values.filter { it.moved }.mapNotNull { heldBody(it) }
        val finger = grabs.values.firstOrNull()?.let { toScene(it.finger) }
        for (b in world.bodiesIn(place)) {
            val p = b as? Person ?: continue
            val a = p.anim
            a.faceTime -= dt
            if (a.faceTime <= 0f && !p.held) {
                a.face = if ((a.pose == Pose.LIE || inSauna(p)) && night > 0.5f) Face.SLEEP else Face.HAPPY
            }
            a.nextBlink -= dt
            if (a.nextBlink <= 0f) {
                a.blink = 0.13f
                a.nextBlink = 2f + random.nextFloat() * 4f
            }
            a.blink = max(0f, a.blink - dt)
            a.chew = max(0f, a.chew - dt * 1.3f)
            a.talk = max(0f, a.talk - dt)
            a.wave = max(0f, a.wave - dt)
            a.nameTag = max(0f, a.nameTag - dt)
            a.sayTime = max(0f, a.sayTime - dt)
            if (a.sayTime == 0f) a.chatWith = -1
            a.spin = max(0f, a.spin - dt / 0.75f)
            a.tickle = max(0f, a.tickle - dt)
            if (a.cheer > 0f) {
                a.cheer -= dt
                if (a.hop == 0f && a.pose == Pose.STAND) a.hopV = 1.7f
            }
            // Hens peck at the ground now and then.
            if (p.species == Species.CHICKEN && p.resting && a.walkTo.isNaN() && random.nextFloat() < dt * 0.7f) a.tilt = 32f * a.facing
            a.hopV -= 9f * dt
            a.hop = max(0f, a.hop + a.hopV * dt)
            if (a.hop == 0f) a.hopV = 0f
            a.tilt = if (p.held) (-p.vx * 9f).coerceIn(-18f, 18f) else a.tilt * exp(-8f * dt)
            if (a.sparkle > 0f) {
                particles.burst(PKind.SPARK, p.x, p.y - p.h * 0.8f, 10, speed = 0.4f, size = 0.013f)
                a.sparkle = 0f
            }

            // Eyes follow what moves: a held thing, the finger, or they wander.
            val head = Anatomy.at(p, Part.HEAD)
            val partner = (world.bodies[a.chatWith] as? Person)?.let { Anatomy.at(it, Part.HEAD) }?.let { Offset(it[0], it[1]) }
            val target = held.firstOrNull { it !== p }?.let { Offset(it.x, it.y - it.h / 2) } ?: finger ?: partner
            val (lx, ly) = if (target != null) {
                val dx = target.x - head[0]
                val dy = target.y - head[1]
                val d = max(0.001f, hypot(dx, dy))
                dx / d to dy / d
            } else {
                sin(time * 0.45f + p.id) * 0.7f to 0.15f + sin(time * 0.3f + p.id * 2f) * 0.2f
            }
            a.lookX += (lx - a.lookX) * min(1f, dt * 7f)
            a.lookY += (ly - a.lookY) * min(1f, dt * 7f)

            // Dancing to the radio.
            a.dance = if (a.cheer > 0f || (discoOn && a.pose == Pose.STAND) || (radio != null && a.pose == Pose.STAND && p.resting && abs(p.x - radio.x) < 1.6f)) {
                time * 2.07f * PI.toFloat() + 0.001f
            } else {
                0f
            }

            // Sleepy «z» at night, and now and then a wave or a little chat.
            if (a.face == Face.SLEEP && random.nextFloat() < dt * 0.9f && visible(p)) {
                val at = Anatomy.at(p, Part.HEAD)
                particles.add(Particle(PKind.ZZZ, at[0] + 0.03f, at[1] - 0.04f, 0.05f, -0.08f, 1.8f, 0.012f, Color.White))
            }
            a.nextIdle -= dt
            if (a.nextIdle <= 0f) {
                a.nextIdle = 4f + random.nextFloat() * 8f
                if (a.face != Face.SLEEP && !p.held && visible(p)) {
                    val friend = if (p.species == Species.FOLK) chatPartner(p) else null
                    if (friend != null) {
                        chat(p, friend)
                    } else {
                        if (random.nextBoolean()) a.wave = 1.2f else a.talk = 0.9f
                        if (a.talk > 0f && time - lastBabble > 9f) {
                            lastBabble = time
                            voice(p, Sfx.BABBLE, 0.25f)
                        }
                    }
                }
            }
        }
    }

    /** A figure close by to chat with: same side of the room, about the same depth. */
    private fun chatPartner(p: Person): Person? = world.bodiesIn(place).filterIsInstance<Person>().firstOrNull { o ->
        o !== p && o.species == Species.FOLK && !o.held && o.anim.face != Face.SLEEP && o.anim.sayTime == 0f &&
            abs(o.x - p.x) < 0.38f && abs(bodyKey(o) - bodyKey(p)) < 0.12f
    }

    /** Two figures chat: a picture in a speech bubble, a babble, and an answer a moment later. */
    private fun chat(p: Person, other: Person) {
        val a = p.anim
        a.say = random.nextInt(CHAT_ICONS)
        a.sayTime = 1.7f
        a.talk = 1.2f
        a.chatWith = other.id
        other.anim.chatWith = p.id
        if (time - lastBabble > 4f) {
            lastBabble = time
            voice(p, Sfx.BABBLE, 0.3f)
        }
        pending += (time + 1.1f) to {
            val o = other.anim
            if (!other.held && o.face != Face.SLEEP) {
                o.say = random.nextInt(CHAT_ICONS)
                o.sayTime = 1.5f
                o.talk = 1f
                o.chatWith = p.id
                if (random.nextFloat() < 0.4f) {
                    o.face = Face.LAUGH
                    o.faceTime = 0.9f
                    voice(other, Sfx.GIGGLE, 0.35f)
                } else {
                    voice(other, Sfx.BABBLE, 0.28f)
                }
            }
        }
    }

    private fun inSauna(p: Person): Boolean = p.mode == Mode.SEATED && world.fixtures[p.holder]?.type in setOf(FixtureType.SAUNA, FixtureType.TENT)

    private fun visible(b: Body): Boolean = b.x > cam - 0.2f && b.x < cam + viewport + 0.2f

    /** Little background life: chimney steam, notes from the radio, sparks from the fire. */
    private fun ambient(dt: Float) {
        if (!motion) return
        for (f in world.fixturesIn(place)) {
            val x = f.x + f.shiftX
            if (x < cam - 0.5f || x > cam + viewport + 0.5f) continue
            when {
                f.type == FixtureType.RADIO && f.on && random.nextFloat() < dt * 2.2f ->
                    particles.add(Particle(PKind.NOTE, x + (random.nextFloat() - 0.5f) * 0.05f, f.top, (random.nextFloat() - 0.5f) * 0.08f, -0.12f, 1.8f, 0.012f, listOf(T.Grape, T.Berry, T.Sea)[random.nextInt(3)]))
                (f.type == FixtureType.CAMPFIRE || f.type == FixtureType.WOOD_STOVE) && f.on && random.nextFloat() < dt * 4f ->
                    particles.add(Particle(PKind.SPARK, x + (random.nextFloat() - 0.5f) * 0.05f, f.top - 0.03f, (random.nextFloat() - 0.5f) * 0.1f, -0.25f, 0.9f, 0.006f, Color(0xFFFFB02E)))
                f.type == FixtureType.SAUNA && f.on && random.nextFloat() < dt * 3f ->
                    particles.add(Particle(PKind.STEAM, x + 0.12f, f.top - 0.02f, 0.02f, -0.1f, 2f, 0.02f, Color.White))
                f.type == FixtureType.CAULDRON && random.nextFloat() < dt * (if (f.on) 8f else 1.5f) ->
                    particles.add(Particle(PKind.BUBBLE, x + (random.nextFloat() - 0.5f) * 0.12f, f.top, 0f, -0.1f, 1.2f, 0.01f, Color.White))
                (f.type == FixtureType.STOVE && f.on) && random.nextFloat() < dt * 1.5f ->
                    particles.add(Particle(PKind.STEAM, x + (random.nextFloat() - 0.5f) * 0.1f, f.top - 0.02f, 0f, -0.08f, 1.6f, 0.015f, Color.White))
                f.type == FixtureType.TRACTOR && f.on && random.nextFloat() < dt * 10f ->
                    particles.add(Particle(PKind.SMOKE, x + 0.12f, f.top + 0.02f, -0.05f, -0.15f, 1.2f, 0.016f, Color(0xFF8E93A6)))
                f.type == FixtureType.ROCKET_SHIP && f.on && random.nextFloat() < dt * 30f ->
                    particles.add(Particle(PKind.SMOKE, x + (random.nextFloat() - 0.5f) * 0.1f, f.y + f.shiftY, (random.nextFloat() - 0.5f) * 0.3f, 0.2f, 1.2f, 0.03f, Color(0xFFE8ECF5)))
                f.type == FixtureType.BATH && f.on && random.nextFloat() < dt * 2f ->
                    particles.add(Particle(PKind.BUBBLE, x + (random.nextFloat() - 0.5f) * 0.3f, f.y - 0.11f, 0f, -0.06f, 1.5f, 0.012f, Color.White))
            }
        }
        if (sim.underwater(place)) {
            for (b in world.bodiesIn(place)) {
                if (b is Person && visible(b) && random.nextFloat() < dt * 0.7f) {
                    val mouth = Anatomy.at(b, Part.MOUTH)
                    particles.add(Particle(PKind.BUBBLE, mouth[0] + 0.01f, mouth[1], 0.01f, -0.12f, 2.2f, 0.008f + random.nextFloat() * 0.005f, Color.White))
                }
            }
            if (random.nextFloat() < dt * 0.9f) particles.add(Particle(PKind.BUBBLE, cam + random.nextFloat() * viewport, 0.95f, 0f, -0.1f, 5f, 0.006f, Color.White))
            if (particles.count < 220 && random.nextFloat() < dt * 0.25f) {
                val fromLeft = random.nextBoolean()
                val y = 0.25f + random.nextFloat() * 0.45f
                val color = listOf(Color(0xFFFFB02E), Color(0xFF2FD18B), Color(0xFFFF6F91), Color(0xFF7CCBFF))[random.nextInt(4)]
                repeat(3 + random.nextInt(4)) { k ->
                    particles.add(Particle(PKind.FISH, (if (fromLeft) cam - 0.1f - k * 0.05f else cam + viewport + 0.1f + k * 0.05f), y + (k % 3) * 0.025f, if (fromLeft) 0.16f else -0.16f, 0f, 16f, 0.012f, color))
                }
            }
        }
        // Critters: butterflies in the grass by day, birds crossing the sky.
        val day = night < 0.4f && world.weather == Weather.SUN
        if (day && (place == PlaceId.FARM || place == PlaceId.FOREST || place == PlaceId.BEACH) && particles.count < 200 && random.nextFloat() < dt * 0.35f) {
            val fromLeft = random.nextBoolean()
            val x = if (fromLeft) cam - 0.05f else cam + viewport + 0.05f
            if (place != PlaceId.BEACH) {
                particles.add(Particle(PKind.BUTTERFLY, x, 0.62f + random.nextFloat() * 0.2f, if (fromLeft) 0.12f else -0.12f, 0f, 16f, 0.011f, listOf(T.Sun, T.Berry, T.SeaTop, Color.White)[random.nextInt(4)]))
            } else {
                particles.add(Particle(PKind.BIRD, x, 0.1f + random.nextFloat() * 0.2f, if (fromLeft) 0.22f else -0.22f, 0f, 14f, 0.014f, Color.White))
            }
        }
        if (place.outdoor && place != PlaceId.BEACH && night < 0.5f && random.nextFloat() < dt * 0.08f) {
            val fromLeft = random.nextBoolean()
            particles.add(Particle(PKind.BIRD, if (fromLeft) cam - 0.05f else cam + viewport + 0.05f, 0.08f + random.nextFloat() * 0.18f, if (fromLeft) 0.25f else -0.25f, 0f, 12f, 0.012f, Color.White))
        }
        // Weather motion.
        val snowing = world.weather == Weather.SNOW || place == PlaceId.MOUNTAIN
        if (place.outdoor && world.weather == Weather.RAIN) {
            for (i in 0 until drops.size / 2) {
                drops[i * 2 + 1] += dt * 1.6f
                drops[i * 2] -= dt * 0.12f
                if (drops[i * 2 + 1] > 1f) {
                    drops[i * 2 + 1] -= 1.05f
                    drops[i * 2] = random.nextFloat()
                }
                if (drops[i * 2] < 0f) drops[i * 2] += 1f
            }
        }
        if (place.outdoor && snowing) {
            for (i in 0 until flakes.size / 2) {
                flakes[i * 2 + 1] += dt * (0.08f + (i % 5) * 0.02f)
                flakes[i * 2] += sin(time * 0.8f + i) * dt * 0.02f
                if (flakes[i * 2 + 1] > 1f) {
                    flakes[i * 2 + 1] -= 1.05f
                    flakes[i * 2] = random.nextFloat()
                }
            }
        }
    }

    fun photoFlash() {
        flash = 1f
    }

    // ---------------------------------------------------------------------------------- input

    fun down(id: Long, at: Offset, uptime: Long) {
        val grab = Grab(pick(at), at, uptime, time)
        grab.tracker.addPosition(uptime, at)
        val body = heldBody(grab)
        if (body != null) {
            val p = toScene(at)
            grab.offX = body.x - p.x
            grab.offY = body.y - p.y
            if (grab.target is Target.FromBag) lift(grab, body)
        }
        if (grab.target is Target.Pan) camV = 0f
        (grab.target as? Target.Furniture)?.fixture?.let { f ->
            val p = toScene(at)
            grab.offX = f.x - p.x
            grab.offY = f.y - p.y
            grab.moved = true
            f.anim = 1f
            host.sfx(Sfx.PICK, 0.7f, 0.7f)
            host.haptic()
        }
        grabs[id] = grab
    }

    fun move(id: Long, at: Offset, uptime: Long) {
        val g = grabs[id] ?: return
        g.tracker.addPosition(uptime, at)
        val dx = at.x - g.finger.x
        g.finger = at
        if (!g.moved && hypot(at.x - g.down.x, at.y - g.down.y) > dp(9f)) {
            g.moved = true
            heldBody(g)?.let { if (!it.held) lift(g, it) }
            if (g.target is Target.BagButton) bagOpen = true
        }
        if (g.target is Target.Furniture) overStore = storeZone?.contains(at) == true
        if (g.moved && g.target is Target.Pan && grabs.count { it.value.target is Target.Pan } == 1) {
            cam -= dx / u
            clampCam()
        }
    }

    fun up(id: Long, at: Offset, uptime: Long) {
        val g = grabs.remove(id) ?: return
        g.tracker.addPosition(uptime, at)
        g.finger = at
        val v = g.tracker.calculateVelocity()
        val vx = (v.x / u).coerceIn(-7f, 7f)
        val vy = (v.y / u).coerceIn(-7f, 7f)
        val body = heldBody(g)
        if (!g.moved) {
            when (val t = g.target) {
                is Target.Hold -> tapBody(t.body)
                is Target.FromBag -> drop(g, t.body, 0f, 0f)
                Target.BagButton -> {
                    bagOpen = !bagOpen
                    host.sfx(Sfx.ZIP, 0.6f)
                }
                Target.Pan -> tapScene(at)
                is Target.Furniture -> putDown(t.fixture)
            }
            return
        }
        when {
            body != null && body.held -> drop(g, body, vx, vy)
            g.target is Target.Pan -> camV = -vx
            g.target is Target.Furniture -> {
                val f = (g.target as Target.Furniture).fixture
                overStore = false
                if (storeZone?.contains(at) == true && sim.designer.store(place, f)) {
                    designVersion++
                    host.changed()
                } else {
                    putDown(f)
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------- home designer

    private fun centerX(): Float = cam + viewport / 2f

    /** The room in the middle of the screen, which wallpaper and floor choices apply to. */
    val room: Int get() = Decor.roomAt(place, centerX())

    /** Puts a piece from the catalogue in the middle of the screen. */
    fun addFurniture(type: FixtureType, variant: Int) {
        val y = if (type.spec.wall) 0.5f else (place.back + PlaceId.FRONT) / 2f
        sim.designer.add(place, type, variant, centerX() + (random.nextFloat() - 0.5f) * 0.3f, y)?.let {
            designVersion++
            host.changed()
        }
    }

    fun addFromStore(index: Int) {
        val type = world.storage.getOrNull(index)?.type ?: return
        val y = if (type.spec.wall) 0.5f else (place.back + PlaceId.FRONT) / 2f
        sim.designer.unstore(place, index, centerX(), y)?.let {
            designVersion++
            host.changed()
        }
    }

    fun restyle(wall: Int? = null, floor: Int? = null) {
        sim.designer.restyle(place, room, wall, floor)
        designVersion++
        host.changed()
    }

    fun tidy() {
        sim.designer.tidy(place)
        host.changed()
    }

    /**
     * «Heimedesignar»: a long press on a piece of furniture picks it up; it follows the finger along the
     * floor and in depth, taking along whatever is on it, and lands with a thump where it is let go.
     */
    private fun moveFurniture(dt: Float) {
        for (g in grabs.values) {
            if (g.target is Target.Pan && !g.moved && time - g.born > LONG_PRESS) {
                val p = toScene(g.finger)
                val f = fixtureAt(p)
                if (f != null && sim.movable(f)) {
                    g.target = Target.Furniture(f)
                    g.moved = true
                    g.offX = f.x - p.x
                    g.offY = f.y - p.y
                    f.anim = 1f
                    host.sfx(Sfx.PICK, 0.7f, 0.7f)
                    host.haptic()
                    particles.burst(PKind.SPARK, f.x, f.y - f.spec.h / 2, 8, 0.4f, 0.011f)
                    // Folk nearby are impressed.
                    for (o in world.bodiesIn(place)) if (o is Person && abs(o.x - f.x) < 0.6f && !o.held) { o.anim.face = Face.WOW; o.anim.faceTime = 0.8f }
                }
            }
        }
        val follow = 1f - exp(-18f * dt)
        val grabbed = HashSet<Int>()
        for (g in grabs.values) {
            val f = (g.target as? Target.Furniture)?.fixture ?: continue
            grabbed += f.id
            val p = toScene(g.finger)
            val want = sim.clampFixture(place, f, p.x + g.offX, p.y + g.offY)
            sim.moveFixture(place, f, f.x + (want[0] - f.x) * follow, f.y + (want[1] - f.y) * follow)
            f.lift = min(1f, f.lift + dt * 6f)
        }
        for (f in world.fixturesIn(place)) if (f.id !in grabbed && f.lift > 0f) f.lift = max(0f, f.lift - dt * 5f)
    }

    private fun putDown(f: Fixture) {
        f.anim = 1f
        host.sfx(Sfx.THUD, 0.6f, 0.9f + random.nextFloat() * 0.2f)
        particles.burst(PKind.DUST, f.x, f.y, 8, 0.35f, 0.014f, up = 0.05f, life = 0.6f)
        shake = max(shake, 0.15f)
        host.changed()
    }

    /** The front-most piece of furniture under a scene point. */
    private fun fixtureAt(p: Offset): Fixture? {
        for (f in world.fixturesIn(place).sortedByDescending { fixtureKey(it) }) {
            val fx = f.x + f.shiftX
            val fy = f.y + f.shiftY
            val pad = 0.012f
            if (p.x in (fx - f.spec.w / 2 - pad)..(fx + f.spec.w / 2 + pad) && p.y in (fy - f.spec.h - pad)..(fy + pad)) return f
        }
        return null
    }

    fun cancel() {
        for (g in grabs.values) heldBody(g)?.let { drop(g, it, 0f, 0f) }
        grabs.clear()
    }

    /** What a finger touches, from the top down. */
    private fun pick(at: Offset): Target {
        if (hypot(at.x - bagCenter.x, at.y - bagCenter.y) < bagRadius * 1.1f) return Target.BagButton
        if (bagOpen) trayHit(at)?.let { return Target.FromBag(it) }
        val p = toScene(at)
        // In the home designer furniture comes first.
        if (designMode) fixtureAt(p)?.let { f ->
            if (sim.movable(f)) return Target.Furniture(f)
            f.anim = 1f
            host.sfx(Sfx.HMM, 0.4f, 0.8f)
        }
        val list = drawList()
        for (i in list.indices.reversed()) {
            val b = list[i]
            if (b is Person) {
                for (t in world.carried(b)) if (hitCarried(b, t, p)) return Target.Hold(t)
            }
            if (hit(b, p)) return Target.Hold(b)
        }
        return Target.Pan
    }

    private val minTouch get() = units(dp(26f))

    private fun hit(b: Body, p: Offset): Boolean {
        if (b is Person) {
            val half = max(b.w * 0.55f, minTouch)
            return when (b.anim.pose) {
                Pose.LIE -> p.x in (b.x - b.h * 0.55f)..(b.x + b.h * 0.55f) && p.y in (b.y - b.w * 1.1f)..(b.y + 0.02f)
                Pose.SIT -> p.x in (b.x - half)..(b.x + half) && p.y in (b.y - b.h * 0.9f)..(b.y + b.h * 0.12f)
                else -> p.x in (b.x - half)..(b.x + half) && p.y in (b.y - b.anim.hop - b.h * (if (b.species.pet) 1.1f else 1.05f))..(b.y + 0.015f)
            }
        }
        val half = max(b.w * 0.5f, minTouch)
        val tall = max(b.h, minTouch * 1.6f)
        return p.x in (b.x - half)..(b.x + half) && p.y in (b.y - tall - 0.01f)..(b.y + 0.015f)
    }

    private fun hitCarried(p: Person, t: Thing, at: Offset): Boolean {
        val c = carriedCenter(p, t)
        val r = max(max(t.w, t.h) * carriedScale(p, t) * 0.5f, minTouch * 0.8f)
        return hypot(at.x - c.x, at.y - c.y) < r
    }

    private fun carriedScale(p: Person, t: Thing): Float = when (Slot.entries[t.slot.coerceIn(0, 2)]) {
        Slot.HEAD, Slot.FACE -> headWidth(p.species, p.h) / t.type.fitsHead * (if (t.type == ThingType.SPACE_HELMET) 1.25f else 1f)
        Slot.HAND -> if (p.species.pet) 0.8f else 1f
    }

    /** The centre of a carried thing in scene units. */
    private fun carriedCenter(p: Person, t: Thing): Offset {
        val s = carriedScale(p, t)
        return when (Slot.entries[t.slot.coerceIn(0, 2)]) {
            Slot.HEAD -> {
                if (t.type == ThingType.SPACE_HELMET) {
                    val a = Anatomy.at(p, Part.HEAD)
                    Offset(a[0], a[1] - p.anim.hop)
                } else {
                    val a = Anatomy.at(p, Part.HAT)
                    Offset(a[0], a[1] - t.h * s * 0.45f - p.anim.hop)
                }
            }
            Slot.FACE -> Anatomy.at(p, Part.GLASSES).let { Offset(it[0], it[1] - p.anim.hop) }
            Slot.HAND -> Anatomy.at(p, Part.HAND).let { Offset(it[0], it[1] - p.anim.hop - (if (t.type == ThingType.BALLOON) t.h * 0.5f else 0f)) }
        }
    }

    /** Picks a body up: out of seats, hands, cupboards and the bag. */
    private fun lift(g: Grab, body: Body) {
        if (body.mode == Mode.BAG) {
            body.mode = Mode.FREE
            body.place = place
            // Brought along from somewhere else: the task board may be waiting for it.
            if (body is Thing) sim.tasks.record(app.trollfoss.domain.Deed.BROUGHT, place, body.type)
            if (body is Person) world.carried(body).forEach { it.place = place }
            val p = toScene(g.finger)
            body.x = p.x
            body.y = p.y + body.h * 0.5f
            g.offX = 0f
            g.offY = body.h * 0.5f
        }
        if (body.mode == Mode.WORN) {
            val holder = world.bodies[body.holder] as? Person
            if (holder != null) {
                val c = carriedCenter(holder, body as Thing)
                body.x = c.x
                body.y = c.y + body.h / 2
                val p = toScene(g.finger)
                g.offX = body.x - p.x
                g.offY = body.y - p.y
                holder.anim.face = Face.OOH
                holder.anim.faceTime = 0.8f
            }
        }
        // Lifted off a chair, a table or out of a cupboard: if let go in the air, it lands in front of it.
        val from = world.fixtures[if (body.mode == Mode.SEATED) body.holder else if (body.inside >= 0) body.inside else if (body.resting) body.restOwner else -1]
        if (from != null && !from.spec.wall) body.ground = from.depth + 0.02f
        body.restOwner = -2
        if (body.mode == Mode.SEATED && body is Person) {
            body.y += body.h * Anatomy.HIPS
            val p = toScene(g.finger)
            g.offY = body.y - p.y
        }
        body.mode = Mode.FREE
        body.holder = -1
        body.inside = -1
        body.resting = false
        body.held = true
        body.vx = 0f
        body.vy = 0f
        body.z = world.nextZ()
        body.squashV -= 4f
        if (body is Person) {
            body.anim.face = Face.OOH
            body.anim.faceTime = 999f
            voice(body, Sfx.OOH, 0.6f)
        } else {
            host.sfx(Sfx.PICK, 0.55f, 0.9f + random.nextFloat() * 0.2f)
        }
        host.haptic()
    }

    private fun drop(g: Grab, body: Body, vx: Float, vy: Float) {
        body.held = false
        val fingerOnBag = hypot(g.finger.x - bagCenter.x, g.finger.y - bagCenter.y) < bagRadius * 1.4f
        if (fingerOnBag) {
            intoBag(body)
            return
        }
        if (body is Person) {
            body.anim.faceTime = 0f
            val seat = sim.freeSeatNear(place, body, body.x, body.y, 0.13f)
            if (seat != null && hypot(vx, vy) < 3f) {
                sim.seat(body, seat.first, seat.second)
                body.squashV += 5f
                host.sfx(Sfx.DROP, 0.5f)
                host.changed()
                return
            }
        }
        if (body is Thing) {
            val center = Offset(body.x, body.y - body.h / 2)
            val finger = toScene(g.finger)
            // Machines first: the pot, the blender, the toilet, the workbench, the garden.
            for (f in world.fixturesIn(place)) {
                val zone = f.spec.dropZone ?: continue
                val fx = f.x + f.shiftX
                if (zone.contains(finger.x - fx, finger.y - f.y) || zone.contains(center.x - fx, center.y - f.y)) {
                    if (sim.dropInto(place, f, body)) {
                        host.changed()
                        return
                    }
                }
            }
            // Then figures: mouth, head, eyes, body, hair, hand.
            giveTarget(body, center)?.let { (p, part) ->
                val result = sim.give(p, body, part)
                if (result != Give.NONE) {
                    reactToGift(p, body, result)
                    host.changed()
                    return
                }
            }
        }
        if (body.y > PlaceId.FRONT) body.y = PlaceId.FRONT
        if (body.y >= place.back) body.ground = body.y
        body.vx = vx
        body.vy = vy
        if (body is Thing) body.vrot = vx * 220f
        body.resting = false
        host.changed()
    }

    private fun intoBag(body: Body) {
        body.mode = Mode.BAG
        body.place = null
        body.resting = false
        body.z = world.nextZ()
        if (body is Person) world.carried(body).forEach { it.place = null }
        host.sfx(Sfx.ZIP, 0.7f)
        particles.burst(PKind.SPARK, units(bagCenter.x) + cam, units(bagCenter.y - top), 8, 0.4f)
        host.changed()
    }

    private fun trayRect(): Rect {
        val right = bagCenter.x - bagRadius - dp(14f)
        val left = dp(104f)
        val bottom = heightPx - dp(16f)
        return Rect(left, bottom - dp(92f), max(left + dp(92f), right), bottom)
    }

    private fun traySlot(): Float {
        val r = trayRect()
        val n = max(1, world.bag().size)
        return min(dp(88f), (r.width - dp(12f)) / n)
    }

    private fun trayHit(at: Offset): Body? {
        val r = trayRect()
        if (!r.contains(at)) return null
        val bag = world.bag()
        val i = ((at.x - r.left - dp(6f)) / traySlot()).toInt()
        return bag.getOrNull(i)
    }

    /** The best spot on a figure for a thing let go at [center], within reach. */
    private fun giveTarget(t: Thing, center: Offset): Pair<Person, Part>? {
        var best: Pair<Person, Part>? = null
        var bestD = Float.MAX_VALUE
        for (b in drawList()) {
            val p = b as? Person ?: continue
            val k = max(0.6f, p.h / 0.31f)
            val parts = buildList {
                if (t.type.edible || t.type == ThingType.PEPPER) add(Part.MOUTH to (if (t.type == ThingType.PEPPER) 0.09f else 0.075f))
                if (t.type.slot == Slot.HEAD) add(Part.HAT to 0.1f)
                if (t.type.slot == Slot.FACE) add(Part.GLASSES to 0.07f)
                if (t.type == ThingType.GARMENT) add(Part.BODY to 0.1f)
                if (t.type.hairTool) add(Part.HAIR to 0.085f)
                add(Part.HAND to 0.06f)
            }
            for ((part, reach) in parts) {
                val a = Anatomy.at(p, part)
                val lift = if (part == Part.HAT) 0.03f * k else 0f
                val d = hypot(center.x - a[0], center.y - (a[1] - lift - p.anim.hop))
                // Specific spots win over a plain hand when both are in reach.
                val weighted = if (part == Part.HAND) d * 1.3f else d
                if (d < reach * k && weighted < bestD) {
                    bestD = weighted
                    best = p to part
                }
            }
        }
        return best
    }

    private fun reactToGift(p: Person, t: Thing, result: Give) {
        val a = p.anim
        val mouth = Anatomy.at(p, Part.MOUTH)
        when (result) {
            Give.ATE, Give.DRANK -> {
                a.face = Face.CHOMP
                a.faceTime = 0.6f
                host.sfx(if (result == Give.DRANK) Sfx.GULP else Sfx.CHOMP, 0.8f)
                if (result == Give.ATE) particles.burst(PKind.CRUMB, mouth[0], mouth[1], 6, 0.35f, 0.008f, Color(0xFFD9A15A), up = 0.1f)
                // A half-eaten thing drops back into the hand, ready for the next bite.
                if (t.mode == Mode.FREE) sim.give(p, t, Part.HAND)
            }
            Give.FINISHED -> {
                a.face = Face.YUM
                a.faceTime = 1.6f
                host.sfx(Sfx.CHOMP, 0.8f)
                voiceLater(p, Sfx.YUM)
                particles.burst(PKind.CRUMB, mouth[0], mouth[1], 8, 0.4f, 0.008f, Color(0xFFD9A15A), up = 0.1f)
                particles.burst(PKind.HEART, p.x, p.y - p.h, 4, 0.25f, 0.016f, up = 0.3f)
            }
            Give.POTION -> {
                a.face = Face.WOW
                a.faceTime = 1.5f
                host.sfx(Sfx.GULP, 0.8f)
                host.sfx(Sfx.MAGIC, 0.8f)
                particles.burst(PKind.STAR, p.x, p.y - p.h * 0.6f, 14, 0.6f, 0.014f)
            }
            Give.WORE, Give.DRESSED, Give.HAIR -> {
                a.face = Face.GRIN
                a.faceTime = 1.3f
                host.sfx(if (result == Give.HAIR) (if (t.type == ThingType.SCISSORS) Sfx.SNIP else Sfx.SPRAY) else Sfx.MAGIC, 0.7f)
                voiceLater(p, Sfx.GIGGLE)
                particles.burst(PKind.SPARK, p.x, p.y - p.h * 0.9f, 10, 0.45f, 0.012f)
            }
            Give.HELD -> {
                a.face = Face.GRIN
                a.faceTime = 0.8f
                host.sfx(Sfx.POP, 0.5f)
            }
            Give.SNEEZE -> {
                // «Ah … ah …» – the sound runs into the «ATSJO!» the sim sets off in a moment.
                a.face = Face.OOH
                a.faceTime = 0.8f
                a.tilt = -8f
                voice(p, Sfx.SNEEZE, 0.9f, own = true)
            }
            Give.NONE -> Unit
        }
        host.haptic()
    }

    private val pending = ArrayList<Pair<Float, () -> Unit>>()

    private fun voiceLater(p: Person, sfx: Sfx) {
        pending += (time + 0.45f) to { voice(p, sfx, 0.7f) }
    }

    fun runPending() {
        if (pending.isEmpty()) return
        val due = pending.filter { it.first <= time }
        pending.removeAll(due.toSet())
        due.forEach { it.second() }
    }

    private fun tapBody(b: Body) {
        when (b) {
            is Person -> {
                val a = b.anim
                a.nameTag = 2.2f
                a.taps = if (time - a.lastTap < 1.6f) a.taps + 1 else 1
                a.lastTap = time
                if (a.pose == Pose.STAND || a.pose == Pose.FLOAT) a.hopV = 1.9f
                if (a.taps >= 9) {
                    a.face = Face.DIZZY
                    a.faceTime = 1.8f
                    a.taps = 0
                    a.tickle = 0f
                    voice(b, Sfx.OOF, 0.8f)
                } else if (a.taps >= 4) {
                    // Tickles! A helpless giggle fit, tears of laughter and all.
                    if (a.tickle < 0.5f) voice(b, Sfx.TICKLE, 0.8f)
                    a.tickle = 1.8f
                    a.face = Face.LAUGH
                    a.faceTime = 1.8f
                    val eyes = Anatomy.at(b, Part.GLASSES)
                    repeat(2) { k -> particles.add(Particle(PKind.DROP, eyes[0] + (if (k == 0) -1f else 1f) * b.h * 0.1f, eyes[1], (if (k == 0) -1f else 1f) * 0.25f, -0.35f, 0.7f, 0.008f, Color(0xFF9ADAFF))) }
                } else {
                    a.face = if (random.nextBoolean()) Face.LAUGH else Face.GRIN
                    a.faceTime = 1.1f
                    voice(b, Sfx.GIGGLE, 0.8f)
                    if (!b.species.pet) a.wave = 1.1f
                }
                if (b.species == Species.DRAGON) particles.burst(PKind.SPARK, b.x + 0.02f, b.y - b.h * 0.45f, 8, 0.5f, 0.01f, Color(0xFFFFB02E))
                // A tap on someone with a wish makes what they want twinkle, for the youngest players.
                a.wish?.let { w ->
                    hint = w
                    hintUntil = time + 2.6f
                    if (w.kind == WishKind.THING) {
                        for (t in world.bodiesIn(place)) if (t is Thing && t.type == w.thing && !hidden(t)) {
                            particles.burst(PKind.STAR, t.x, t.y - t.h / 2, 6, 0.3f, 0.01f, T.Sun)
                        }
                    }
                }
                particles.burst(PKind.SPARK, b.x, b.y - b.h, 5, 0.3f, 0.01f)
                host.haptic()
            }
            is Thing -> {
                if (b.mode == Mode.WORN) {
                    (world.bodies[b.holder] as? Person)?.let { tapBody(it) }
                    return
                }
                sim.use(place, b)
            }
        }
        host.changed()
    }

    private fun tapScene(at: Offset) {
        val p = toScene(at)
        if (catchStar(p)) return
        // A glimt?
        for (s in sim.visibleSecrets(place)) {
            if (hypot(p.x - s.x, p.y - s.y) < max(0.05f, minTouch)) {
                collect(s)
                return
            }
        }
        // A fixture? Front-most first, wall fixtures last.
        fixtureAt(p)?.let { f ->
            sim.tap(place, f, p.x - (f.x + f.shiftX), p.y - (f.y + f.shiftY))
            host.changed()
            return
        }
        particles.burst(PKind.SPARK, p.x, p.y, 4, 0.25f, 0.008f, Color.White)
    }

    private fun collect(s: Secret) {
        if (!sim.collect(s.id)) return
        found = world.found.size
        flights += Flight(Offset(sx(s.x), sy(s.y) + top))
        particles.burst(PKind.STAR, s.x, s.y, 16, 0.7f, 0.014f, T.Sun)
        host.sfx(Sfx.CHIME, 0.9f)
        host.haptic()
        host.secretFound(s.id)
        host.changed()
    }

    // ---------------------------------------------------------------------------------- sim events

    override fun onLand(body: Body, speed: Float) {
        if (!visible(body)) return
        if (speed > 0.6f) {
            particles.burst(PKind.DUST, body.x, body.y, 4, 0.2f, 0.01f, up = 0f, life = 0.5f)
            val loud = min(1f, speed / 3f)
            host.sfx(if (body is Person || speed > 2f) Sfx.THUD else Sfx.DROP, 0.25f + loud * 0.5f, 0.9f + random.nextFloat() * 0.2f)
        }
        if (speed > 2.2f) shake = max(shake, min(1f, speed / (if (body is Person) 4f else 6f)))
        if (speed > 1.2f) {
            // Neighbours notice a crash.
            for (other in world.bodiesIn(place)) {
                if (other !is Person || other === body || abs(other.x - body.x) > 0.3f || other.anim.face == Face.SLEEP) continue
                other.anim.face = Face.OOH
                other.anim.faceTime = 0.6f
            }
        }
        if (body is Person && speed > 2.3f) {
            body.anim.face = Face.DIZZY
            body.anim.faceTime = 1.4f
            voice(body, Sfx.OOF, 0.7f)
        }
    }

    override fun onBounce(body: Body, speed: Float) {
        if (!visible(body)) return
        host.sfx(Sfx.BOING, min(0.8f, 0.2f + speed * 0.15f), 0.9f + random.nextFloat() * 0.3f)
        if (body is Person && speed > 1.5f) {
            body.anim.face = Face.LAUGH
            body.anim.faceTime = 0.8f
        }
    }

    override fun onSplash(body: Body, speed: Float) {
        host.sfx(Sfx.SPLASH, min(1f, 0.3f + speed * 0.2f))
        repeat(10) {
            particles.add(Particle(PKind.DROP, body.x, body.y - 0.01f, (random.nextFloat() - 0.5f) * 0.8f, -0.5f - random.nextFloat() * 0.6f, 0.8f, 0.012f, Color(0xFF9ADAFF)))
        }
    }

    override fun onEgg(id: String) {
        host.egg(id)
    }

    override fun onWish(person: Person, event: WishEvent) {
        val a = person.anim
        when (event) {
            WishEvent.NEW -> if (visible(person)) {
                bubbleBorn[person.id] = time
                host.sfx(Sfx.BLOOP, 0.35f, 1.25f)
                voice(person, Sfx.HMM, 0.35f)
            }
            WishEvent.GRANTED -> {
                a.cheer = 2.2f
                a.face = Face.LAUGH
                a.faceTime = 2.2f
                a.hopV = 2.4f
                val head = Anatomy.at(person, Part.HEAD)
                particles.burst(PKind.CONFETTI, head[0], head[1], 28, 0.9f, 0.012f, up = 0.7f, life = 1.4f)
                particles.burst(PKind.HEART, head[0], head[1] - 0.05f, 5, 0.3f, 0.016f, up = 0.35f, life = 1.3f)
                host.sfx(Sfx.FANFARE, 0.55f)
                voiceLater(person, Sfx.GIGGLE)
                host.haptic()
                // Friends nearby are happy too.
                for (o in world.bodiesIn(place)) {
                    if (o !is Person || o === person || abs(o.x - person.x) > 0.45f || o.anim.face == Face.SLEEP) continue
                    o.anim.face = Face.GRIN
                    o.anim.faceTime = 1.2f
                    if (o.anim.pose == Pose.STAND) o.anim.hopV = 1.5f
                }
                host.changed()
            }
            WishEvent.FADED -> Unit
        }
    }

    override fun onSpawn(body: Body) {
        body.age = 0f
        particles.burst(PKind.SPARK, body.x, body.y - body.h / 2, 5, 0.3f, 0.01f)
    }

    override fun onRemove(body: Body) {
        particles.burst(PKind.DUST, body.x, body.y - body.h / 2, 6, 0.3f, 0.012f, up = 0.1f)
    }

    override fun onSecret(id: String) {
        appeared[id] = time
        app.trollfoss.domain.Secrets.byId(id)?.let {
            particles.burst(PKind.STAR, it.x, it.y, 12, 0.5f, 0.012f, T.Sun)
            if (it.place == place) host.sfx(Sfx.SPARKLE, 0.8f)
        }
        host.changed()
    }

    override fun onDiscovery(key: String) = host.discovered(key)

    override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) {
        fun s(sfx: Sfx, v: Float = 0.8f, r: Float = 1f) = host.sfx(sfx, v, r)
        when (fx) {
            Fx.OPEN -> s(Sfx.OPEN, 0.6f)
            Fx.CLOSE -> s(Sfx.SHUT, 0.6f)
            Fx.ON -> {
                s(Sfx.CLICK, 0.6f)
                if (fixture?.type == FixtureType.RADIO) host.radio(true)
                if (fixture?.type == FixtureType.CAMPFIRE || fixture?.type == FixtureType.WOOD_STOVE) s(Sfx.POOF, 0.4f, 1.3f)
                if (fixture?.type == FixtureType.BATH || fixture?.type == FixtureType.SINK || fixture?.type == FixtureType.HAIR_WASH) s(Sfx.SPLASH, 0.4f, 1.3f)
            }
            Fx.OFF -> {
                s(Sfx.CLICK, 0.5f, 0.8f)
                if (fixture?.type == FixtureType.RADIO) host.radio(false)
            }
            Fx.CHANNEL -> s(Sfx.CLICK, 0.5f, 1.2f)
            Fx.DISPENSE -> s(Sfx.POP, 0.7f, 0.9f + random.nextFloat() * 0.3f)
            Fx.EMPTY -> s(Sfx.TICK, 0.5f)
            Fx.COOKED -> {
                s(Sfx.SIZZLE, 0.7f)
                particles.burst(PKind.STEAM, x, y, 5, 0.1f, 0.018f, Color.White, up = 0.1f, life = 1.4f)
            }
            Fx.BAKE -> s(Sfx.TICK, 0.6f)
            Fx.DING -> {
                s(Sfx.DING, 0.8f)
                particles.burst(PKind.STEAM, x, y, 6, 0.1f, 0.02f, Color.White, up = 0.1f, life = 1.4f)
            }
            Fx.BLEND -> s(Sfx.WHIRR, 0.7f)
            Fx.BLENDED -> s(Sfx.POP, 0.8f)
            Fx.INTO -> {
                s(if (fixture?.type == FixtureType.CAULDRON) Sfx.BLOOP else Sfx.DROP, 0.7f)
                if (fixture?.type == FixtureType.CAULDRON) particles.burst(PKind.BUBBLE, x, y, 6, 0.2f, 0.012f, Color.White)
            }
            Fx.STIR -> s(Sfx.BUBBLE, 0.6f, 0.8f + random.nextFloat() * 0.4f)
            Fx.BREWED -> {
                s(Sfx.POOF, 0.8f)
                s(Sfx.MAGIC, 0.7f)
                particles.burst(PKind.STAR, x, y, 18, 0.7f, 0.014f)
                particles.burst(PKind.SMOKE, x, y, 6, 0.2f, 0.03f, Color(0xFFC4A6FF), up = 0.1f, life = 1.3f)
            }
            Fx.CAST -> s(Sfx.SWISH, 0.6f)
            Fx.CATCH -> {
                s(Sfx.SPLASH, 0.6f)
                s(Sfx.POP, 0.7f)
            }
            Fx.FLUSH -> {
                s(Sfx.FLUSH, 0.8f)
                particles.burst(PKind.BUBBLE, x, y, 6, 0.2f, 0.01f, Color.White)
            }
            Fx.WISH -> {
                s(Sfx.COIN, 0.8f)
                s(Sfx.MAGIC, 0.6f)
                particles.burst(PKind.STAR, x, y, 16, 0.6f, 0.013f)
            }
            Fx.KEY -> s(Sfx.NOTE, 0.8f, 2f.pow(PENTATONIC[param % PENTATONIC.size] / 12f))
            Fx.TICK -> s(Sfx.TICK, 0.6f)
            Fx.CUCKOO -> s(Sfx.CUCKOO, 0.8f)
            Fx.TOOT -> s(Sfx.HORN, 0.8f)
            Fx.BUILD -> {
                s(Sfx.POP, 0.6f, 0.7f + param * 0.1f)
                particles.burst(if (place == PlaceId.MOUNTAIN) PKind.SNOW else PKind.CRUMB, x, y, 8, 0.3f, 0.01f, if (place == PlaceId.MOUNTAIN) Color.White else Color(0xFFFFE0A3))
            }
            Fx.CRUMBLE -> {
                s(Sfx.POOF, 0.6f)
                particles.burst(PKind.CRUMB, x, y, 14, 0.5f, 0.012f, Color(0xFFFFE0A3))
            }
            Fx.OWL -> s(Sfx.OWL, 0.8f)
            Fx.SHAKE -> {
                s(Sfx.SWISH, 0.6f)
                val kind = if (param == 1) PKind.SNOW else PKind.LEAF
                repeat(8) {
                    particles.add(Particle(kind, x + (random.nextFloat() - 0.5f) * 0.3f, y + (random.nextFloat() - 0.5f) * 0.15f, (random.nextFloat() - 0.5f) * 0.1f, 0.05f, 2.5f, 0.012f, listOf(Color(0xFF3BC46B), Color(0xFFFFC83D), Color(0xFFFF9F43))[random.nextInt(3)]))
                }
            }
            Fx.PUSH -> s(Sfx.SWISH, 0.5f)
            Fx.SLIDE -> s(Sfx.WHOOSH, 0.8f)
            Fx.SPARKLE -> {
                s(Sfx.SPARKLE, 0.7f)
                particles.burst(PKind.SPARK, x, y, 8, 0.35f, 0.01f)
            }
            Fx.PAGE -> s(Sfx.PAGE, 0.7f)
            Fx.LOOK -> {
                s(Sfx.SWISH, 0.5f, 1.4f)
                host.telescope()
            }
            Fx.REGISTER -> s(Sfx.REGISTER, 0.8f)
            Fx.PUMP -> s(Sfx.CLICK, 0.6f, 0.7f)
            Fx.DRY -> s(Sfx.WHIRR, 0.6f, 1.3f)
            Fx.WATER -> {
                s(Sfx.SPLASH, 0.5f, 1.2f)
                repeat(8) { particles.add(Particle(PKind.DROP, x, y, (random.nextFloat() - 0.5f) * 0.4f, -0.4f - random.nextFloat() * 0.3f, 0.8f, 0.01f, Color(0xFF9ADAFF))) }
            }
            Fx.HATCH -> {
                s(Sfx.POOF, 0.8f)
                s(Sfx.ROAR, 0.8f, 1.3f)
                particles.burst(PKind.STAR, x, y, 16, 0.6f, 0.014f)
            }
            Fx.GIFT -> {
                s(Sfx.FANFARE, 0.8f)
                particles.burst(PKind.CONFETTI, x, y, 26, 0.9f, 0.012f, up = 0.6f, life = 2f)
            }
            Fx.POOF -> {
                s(Sfx.POOF, 0.5f)
                particles.burst(PKind.DUST, x, y, 8, 0.3f, 0.015f, up = 0.1f)
            }
            Fx.SPIN -> s(Sfx.SWISH, 0.6f, 1.2f)
            Fx.BOING -> s(Sfx.BOING, 0.4f, 1.2f)
            Fx.SQUEAK -> if (param == 1) {
                // Microphone feedback: everyone winces, then giggles.
                s(Sfx.SQUEAK, 0.7f, 1.8f)
                s(Sfx.SQUEAK, 0.5f, 2f)
                for (o in world.bodiesIn(place)) if (o is Person && abs(o.x - x) < 1f && !o.held) faces(o, Face.OOH, 0.5f, Face.GRIN, 0.8f)
            } else {
                s(Sfx.SQUEAK, 0.8f)
            }
            Fx.STRUM -> {
                val root = listOf(0, 5, 7, 3)[param % 4]
                for ((i, n) in listOf(0, 4, 7).withIndex()) pending += (time + i * 0.05f) to { s(Sfx.NOTE, 0.5f, 2f.pow((root + n - 12) / 12f)) }
                particles.add(Particle(PKind.NOTE, x, y - 0.05f, 0.05f, -0.12f, 1.5f, 0.012f, T.Grape))
            }
            Fx.DRUM -> when (param) {
                1 -> {
                    s(Sfx.DRUM, 0.75f, 1.7f)
                    s(Sfx.CLICK, 0.5f, 0.8f)
                }
                2 -> {
                    s(Sfx.SPRAY, 0.6f, 1.5f)
                    particles.burst(PKind.SPARK, x, y, 5, 0.35f, 0.009f, T.Sun)
                }
                else -> s(Sfx.DRUM, 0.9f, 0.75f)
            }.also { drummerHit(fixture) }
            Fx.RING -> s(Sfx.RING, 0.7f)
            Fx.VROOM -> s(Sfx.VROOM, if (param == 2) 0.9f else 0.7f, if (param == 2) 0.75f else 1f)
            Fx.FIREWORK -> {
                s(Sfx.FIREWORK, 0.9f)
                repeat(40) {
                    val a = random.nextFloat() * 2f * PI.toFloat()
                    val sp = 0.4f + random.nextFloat() * 0.6f
                    particles.add(Particle(PKind.FIREWORK, x, y, kotlin.math.cos(a) * sp, sin(a) * sp, 1.4f, 0.008f, listOf(T.Sun, T.Berry, T.Sea, T.Mint, T.Grape)[it % 5]))
                }
            }
            Fx.HOP -> s(Sfx.TAP, 0.4f)
            Fx.QUAKE -> {
                shake = 1f
                s(Sfx.RUMBLE, 0.9f, 1.3f)
                s(Sfx.THUD, 0.7f, 0.6f)
                repeat(24) { particles.add(Particle(PKind.DUST, cam + random.nextFloat() * viewport, 0.5f + random.nextFloat() * 0.45f, (random.nextFloat() - 0.5f) * 0.3f, -0.1f, 0.9f, 0.02f + random.nextFloat() * 0.02f, Color.White)) }
                var yelped = false
                for (o in world.bodiesIn(place)) {
                    if (o !is Person || o.held || o.anim.face == Face.SLEEP) continue
                    faces(o, Face.OOH, 0.7f, Face.LAUGH, 1.4f)
                    if (!yelped && visible(o)) { yelped = true; voice(o, Sfx.OOF, 0.8f) }
                }
                laughAround(cam + viewport / 2f, null, 1.0f, 1.5f)
                host.haptic()
            }
            Fx.KING -> person(param)?.let { king ->
                s(Sfx.FANFARE, 1f)
                pending += (time + 0.6f) to { s(Sfx.CHIME, 0.8f) }
                particles.burst(PKind.CONFETTI, king.x, king.y - king.h, 40, 1.1f, 0.013f, up = 0.9f, life = 2.2f)
                particles.burst(PKind.STAR, king.x, king.y - king.h, 14, 0.7f, 0.014f, T.Sun)
                king.anim.cheer = 3f
                faces(king, Face.WOW, 0.6f, Face.LAUGH, 2.4f)
                // Everyone bows, and the new king hands out a gem.
                for (o in world.bodiesIn(place)) if (o is Person && o !== king && !o.held && o.anim.face != Face.SLEEP) {
                    o.anim.hopV = 1.5f
                    faces(o, Face.WOW, 0.7f, Face.GRIN, 1.6f)
                }
                val gem = world.addThing(ThingType.GEM, random.nextInt(ThingType.GEM.variants), place, king.x + 0.1f, king.y - king.h * 0.6f)
                gem.ground = sim.groundOf(place, king) + 0.02f
                gem.vy = -1.8f
                gem.vx = 0.4f
                onSpawn(gem)
                host.haptic()
            }
            Fx.DUCK -> {
                // «Plask-plask-kvakk»: the duck sings and the bath fills with bubbles and hearts.
                for ((i, r) in listOf(1f, 1.26f, 1.5f, 1.26f, 1f, 1.5f, 2f).withIndex()) pending += (time + i * 0.17f) to { s(Sfx.SQUEAK, 0.8f, r) }
                pending += (time + 1.3f) to { s(Sfx.FANFARE, 0.5f, 1.4f) }
                particles.burst(PKind.BUBBLE, x, y, 24, 0.35f, 0.014f, Color.White, up = 0.4f, life = 2.4f)
                particles.burst(PKind.HEART, x, y - 0.06f, 8, 0.3f, 0.018f, up = 0.45f, life = 2f)
                for (o in world.bodiesIn(place)) if (o is Person && !o.held && o.anim.face != Face.SLEEP && abs(o.x - x) < 1.2f) faces(o, Face.LAUGH, 1.6f, Face.GRIN, 1f)
            }
            Fx.STARRAIN -> {
                starRainUntil = time + 4.5f
                for ((i, n) in listOf(5, 5, 8, 8, 9, 9, 8, 7, 7, 4, 4, 2, 2, 0).withIndex()) {
                    pending += (time + 0.35f + i * 0.2f) to { s(Sfx.NOTE, 0.6f, 2f.pow(PENTATONIC[n] / 12f) * 2f) }
                }
                for (o in world.bodiesIn(place)) if (o is Person && !o.held && o.anim.face != Face.SLEEP) faces(o, Face.WOW, 1.5f, Face.GRIN, 2f)
            }
            Fx.JIG -> person(param)?.let { elk ->
                s(Sfx.MOO, 0.9f, 1.4f)
                for (k in 1..4) pending += (time + k * 0.3f) to { elk.anim.hopV = 2f; s(Sfx.BOING, 0.4f, 1.1f + k * 0.12f) }
                elk.anim.cheer = 2.4f
                elk.anim.face = Face.YUM
                elk.anim.faceTime = 2.4f
                particles.burst(PKind.HEART, elk.x, elk.y - elk.h, 8, 0.4f, 0.016f, up = 0.4f, life = 1.6f)
                laughAround(elk.x, elk, 0.6f, 1f)
            }
            Fx.CABLE -> {
                s(Sfx.DING, 0.6f, 0.9f)
                s(Sfx.CLICK, 0.7f, 0.6f)
                pending += (time + 0.4f) to { s(Sfx.WHIRR, 0.5f, 0.55f) }
            }
            Fx.ARRIVE -> {
                s(Sfx.DING, 0.85f, 1.15f)
                s(Sfx.THUD, 0.4f, 0.8f)
                for (o in world.bodiesIn(place)) if (o is Person && o.species == Species.FOLK && !o.held && abs(o.x - x) < 0.8f) {
                    faces(o, Face.WOW, 0.5f, Face.GRIN, 1.4f)
                    o.anim.hopV = 1.4f
                }
                particles.burst(PKind.SPARK, x, y, 10, 0.4f, 0.011f, T.SunTop)
            }
            Fx.ECHO -> {
                // «HALLOOO!» The nearest figure shouts, and the mountain answers three times, softer and lower.
                val shouter = person(param)
                val base = (shouter?.voice ?: 1.1f) * 1.15f
                if (shouter != null) {
                    faces(shouter, Face.LAUGH, 1.6f, Face.GRIN, 1f)
                    shouter.anim.talk = 1.6f
                }
                host.sfx(Sfx.OOH, 0.95f, base.coerceIn(0.5f, 2f))
                host.sfx(Sfx.WHOOSH, 0.3f, 1.4f)
                particles.burst(PKind.NOTE, x, y - 0.1f, 4, 0.35f, 0.013f, T.Grape, up = 0.1f, life = 1.2f)
                for (k in 1..3) pending += (time + 0.62f * k) to {
                    val r = (base * 0.93f.pow(k)).coerceIn(0.5f, 2f)
                    host.sfx(Sfx.OOH, 0.6f / k, r)
                    for (side in listOf(-1f, 1f)) particles.burst(PKind.SPARK, x + side * 0.35f * k, y - 0.12f - 0.03f * k, 4, 0.2f, 0.01f, T.SeaTop, up = 0.05f, life = 0.9f)
                    // The second echo makes the goats answer.
                    if (k == 2) for (g in world.bodiesIn(place)) if (g is Person && g.species == Species.GOAT && !g.held) {
                        voice(g, Sfx.BAA, 0.6f)
                        g.anim.hopV = 1.3f
                    }
                }
            }
            Fx.SCREECH -> {
                s(Sfx.CHIRP, 0.9f, 0.5f)
                s(Sfx.ROAR, 0.35f, 1.7f)
                repeat(6) { particles.add(Particle(PKind.LEAF, x + (random.nextFloat() - 0.5f) * 0.12f, y, (random.nextFloat() - 0.5f) * 0.4f, -0.1f + random.nextFloat() * 0.2f, 2.4f, 0.011f, Color(0xFFE8DCC8), random.nextFloat() * 360f, (random.nextFloat() - 0.5f) * 300f)) }
                for (o in world.bodiesIn(place)) if (o is Person && !o.held && o.anim.face != Face.SLEEP) faces(o, Face.OOH, 0.5f, Face.LAUGH, 1f)
            }
            Fx.SUMMIT -> {
                s(Sfx.FANFARE, 1f)
                pending += (time + 0.7f) to { s(Sfx.CHIME, 0.8f) }
                particles.burst(PKind.CONFETTI, x, y, 36, 1f, 0.013f, up = 0.9f, life = 2f)
                particles.burst(PKind.STAR, x, y + 0.05f, 12, 0.6f, 0.014f, T.Sun)
                for (o in world.bodiesIn(place)) if (o is Person && !o.held && o.anim.face != Face.SLEEP) {
                    o.anim.cheer = 2.4f
                    faces(o, Face.LAUGH, 1.6f, Face.GRIN, 1.4f)
                }
                host.haptic()
            }
            Fx.SETTLE -> {
                // A little «ahh» as someone sits down, and a happy sigh when they lie down for the night.
                s(if (param == 2) Sfx.HMM else Sfx.YUM, 0.4f, if (param == 2) 0.7f else 0.9f)
                if (param == 2) repeat(1) { particles.add(Particle(PKind.ZZZ, x + 0.04f, y - 0.1f, 0.04f, -0.08f, 1.8f, 0.012f, Color.White)) }
            }
            Fx.WAKE -> {
                s(Sfx.OOH, 0.4f, 0.9f)
                person(fixture?.let { f -> world.bodiesIn(place).filterIsInstance<Person>().firstOrNull { abs(it.x - x) < 0.01f && it.anim.auto == 0 }?.id } ?: -1)?.let { p ->
                    faces(p, Face.OOH, 0.8f, Face.HAPPY, 0.1f)
                    p.anim.hopV = 1.3f
                    if (param == 1) particles.burst(PKind.SPARK, p.x, p.y - p.h * 0.9f, 5, 0.3f, 0.01f, T.SunTop)
                }
            }
            Fx.PLACE -> {
                s(Sfx.POP, 0.8f, 0.8f)
                particles.burst(PKind.DUST, x, y + (fixture?.spec?.h ?: 0f) / 2, 10, 0.4f, 0.016f, up = 0.05f, life = 0.7f)
                particles.burst(PKind.SPARK, x, y, 8, 0.4f, 0.012f)
            }
            Fx.STORE -> {
                s(Sfx.ZIP, 0.8f)
                particles.burst(PKind.DUST, x, y, 12, 0.4f, 0.018f, up = 0.1f, life = 0.7f)
            }
            Fx.PAINT -> {
                s(Sfx.SWISH, 0.8f, if (param == 0) 1.2f else 0.8f)
                s(Sfx.SPARKLE, 0.5f)
                repeat(16) { particles.add(Particle(PKind.SPARK, x + (random.nextFloat() - 0.5f) * 1.2f, if (param == 0) 0.2f + random.nextFloat() * 0.5f else 0.82f + random.nextFloat() * 0.13f, 0f, -0.05f, 0.9f, 0.012f, T.SunTop)) }
            }
            Fx.TIDY -> {
                s(Sfx.MAGIC, 0.8f)
                s(Sfx.WHOOSH, 0.7f, 1.3f)
                for (o in world.bodiesIn(place)) if (o is Person && !o.held && visible(o)) faces(o, Face.WOW, 0.8f, Face.GRIN, 1f)
            }
            Fx.HOME -> {
                s(Sfx.TAP, 0.35f, 1.2f + random.nextFloat() * 0.4f)
                particles.burst(PKind.SPARK, x, y - (thing?.h ?: 0.03f) / 2, 4, 0.25f, 0.009f)
            }
            Fx.TRASH -> if (param == 0) {
                s(Sfx.CLICK, 0.6f, 0.8f)
            } else {
                s(Sfx.CHOMP, 0.7f, 0.8f)
                particles.burst(PKind.DUST, x, y, 5, 0.25f, 0.01f, up = 0.1f)
                // Every fourth mouthful the bin burps. Everyone thinks that is very funny.
                if (param % 4 == 0) {
                    pending += (time + 0.5f) to {
                        s(Sfx.BURP, 0.8f, 0.8f)
                        particles.add(Particle(PKind.BUBBLE, x, y - 0.02f, 0.02f, -0.12f, 1.1f, 0.018f, Color.White))
                    }
                    laughAround(x, null, 0.9f, 0.9f)
                }
            }
            Fx.SUCK -> {
                s(Sfx.WHOOSH, 0.45f, 2f)
                particles.burst(PKind.DUST, x, y, 4, 0.2f, 0.01f, up = 0f, life = 0.5f)
                if (param % 5 == 0) {
                    pending += (time + 0.4f) to { s(Sfx.BURP, 0.7f, 1.4f) }
                    laughAround(x, null, 0.8f, 0.9f)
                }
            }
            Fx.BUMP -> {
                s(Sfx.BONK, 0.8f, 0.7f)
                shake = max(shake, 0.3f)
                particles.burst(PKind.STAR, x, y, 6, 0.4f, 0.011f, T.Sun, up = 0.3f, life = 0.7f)
                person(param)?.let { faces(it, Face.OOH, 0.3f, Face.LAUGH, 1.2f); voiceLater(it, Sfx.GIGGLE) }
            }
            Fx.SCAN -> {
                s(Sfx.BEEP, 0.6f, 1.4f)
                particles.add(Particle(PKind.SPARK, x, y - 0.01f, 0f, -0.05f, 0.4f, 0.012f, T.Berry))
            }
            Fx.KNOCK -> {
                s(Sfx.THUD, 0.8f, 1.3f)
                for (i in 0 until 4) pending += (time + 0.08f + i * 0.09f) to { s(Sfx.TICK, 0.6f, 0.6f + i * 0.15f) }
                pending += (time + 0.5f) to { s(Sfx.FANFARE, 0.7f) }
                particles.burst(PKind.CONFETTI, x, y, 24, 0.8f, 0.012f, up = 0.6f, life = 1.6f)
                laughAround(x, null, 0.5f, 1.2f)
            }
            Fx.XYLO -> {
                s(Sfx.NOTE, 0.75f, 2f.pow(PENTATONIC[(param + 2).coerceIn(0, PENTATONIC.size - 1)] / 12f) * 2f)
                particles.add(Particle(PKind.NOTE, x, y - 0.03f, (random.nextFloat() - 0.5f) * 0.1f, -0.14f, 1.4f, 0.012f, listOf(T.Grape, T.Berry, T.Sea, T.Mint)[param % 4]))
            }
            Fx.SING -> person(param)?.let { singer ->
                // A little tune on the figure's own voice, with an echo from the speakers.
                val tune = intArrayOf(5, 7, 8, 7, 5, 9)
                for ((i, n) in tune.withIndex()) {
                    val rate = 2f.pow(PENTATONIC[n] / 12f)
                    pending += (time + i * 0.21f) to {
                        host.sfx(Sfx.OOH, 0.55f, (singer.voice * rate).coerceIn(0.5f, 2f))
                        singer.anim.talk = 0.3f
                    }
                    pending += (time + i * 0.21f + 0.14f) to { host.sfx(Sfx.OOH, 0.18f, (singer.voice * rate).coerceIn(0.5f, 2f)) }
                }
                singer.anim.face = Face.GRIN
                singer.anim.faceTime = 1.6f
                repeat(5) { particles.add(Particle(PKind.NOTE, x + (random.nextFloat() - 0.5f) * 0.1f, y, (random.nextFloat() - 0.5f) * 0.12f, -0.12f, 1.6f, 0.013f, listOf(T.Grape, T.Berry, T.Sea)[it % 3])) }
                laughAround(x, singer, 1.3f, 1.2f)
            }
            Fx.BOOM -> {
                s(Sfx.DRUM, 1f, 0.45f)
                s(Sfx.RUMBLE, 0.5f, 1.6f)
                shake = max(shake, 0.6f)
                repeat(3) { k -> particles.add(Particle(PKind.DUST, x, y, 0f, 0f, 0.5f + k * 0.15f, 0.04f + k * 0.03f, Color.White)) }
                for (o in world.bodiesIn(place)) {
                    if (o !is Person || abs(o.x - x) > 0.8f || o.held) continue
                    if (o.anim.pose == Pose.STAND && o.anim.hop == 0f) o.anim.hopV = 2.2f
                    faces(o, Face.WOW, 0.4f, Face.LAUGH, 1f)
                }
            }
            Fx.DISCO -> {
                s(if (param == 1) Sfx.SPARKLE else Sfx.CLICK, 0.8f)
                host.radio(param == 1)
            }
            Fx.FOG -> {
                s(Sfx.SPRAY, 0.6f, 0.6f)
                repeat(18) { particles.add(Particle(PKind.SMOKE, x, y, 0.15f + random.nextFloat() * 0.35f, -0.02f - random.nextFloat() * 0.05f, 2.2f + random.nextFloat(), 0.03f, Color.White)) }
            }
            Fx.BUBBLES -> {
                s(Sfx.BUBBLE, 0.6f, 0.9f + random.nextFloat() * 0.4f)
                particles.burst(PKind.BUBBLE, x, y, if (param == 1) 12 else 6, 0.2f, 0.012f, Color.White, up = 0.25f, life = 1.6f)
            }
            Fx.INK -> {
                s(Sfx.SPLAT, 0.7f, 1.3f)
                s(Sfx.BLOOP, 0.6f, 0.8f)
                repeat(14) { particles.add(Particle(PKind.SMOKE, x + (random.nextFloat() - 0.5f) * 0.1f, y, (random.nextFloat() - 0.5f) * 0.5f, -0.05f - random.nextFloat() * 0.1f, 1.6f, 0.028f, Color(0xFF3B2A55))) }
                person(param)?.let { faces(it, Face.OOH, 0.6f, Face.LAUGH, 1.4f); laughAround(it.x, it, 0.7f, 0.8f) }
            }
            Fx.PRRT -> {
                s(Sfx.PRRT, 0.9f, 0.9f + random.nextFloat() * 0.25f)
                repeat(6) { particles.add(Particle(PKind.SMOKE, x + (random.nextFloat() - 0.5f) * 0.05f, y - 0.01f, (random.nextFloat() - 0.5f) * 0.25f, -0.05f - random.nextFloat() * 0.1f, 1f, 0.012f, Color(0xFFE9F7D6))) }
                val sitter = person(param)
                if (sitter != null) {
                    faces(sitter, Face.WOW, 0.7f, Face.GRIN, 1.6f)
                    sitter.anim.hopV = 1.2f
                    laughAround(x, sitter, 0.55f, 0.9f)
                } else {
                    laughAround(x, null, 0.3f, 0.5f)
                }
                host.haptic()
            }
            Fx.SLIP -> {
                s(Sfx.SLIP, 0.9f)
                person(param)?.let { p ->
                    faces(p, Face.OOH, 0.9f, Face.DIZZY, 1.6f)
                    pending += (time + 2.5f) to { if (p.anim.face == Face.DIZZY) faces(p, Face.LAUGH, 1.2f, Face.HAPPY, 0.1f) }
                    laughAround(p.x, p, 0.9f, 0.9f)
                }
                host.haptic()
            }
            Fx.ATSJO -> {
                shake = max(shake, 0.45f)
                repeat(22) {
                    val a = -PI.toFloat() * (0.15f + random.nextFloat() * 0.7f)
                    val sp = 0.6f + random.nextFloat() * 0.9f
                    particles.add(Particle(PKind.DUST, x, y + 0.02f, kotlin.math.cos(a) * sp * (if (it % 2 == 0) 1f else -1f), sin(a) * sp * 0.4f, 0.7f, 0.01f, Color.White))
                }
                person(param)?.let { p ->
                    faces(p, Face.OOH, 0.5f, Face.GRIN, 1.4f)
                    laughAround(p.x, p, 0.5f, 0.8f)
                }
                host.haptic()
            }
            Fx.PEPPER -> {
                s(Sfx.SPRAY, 0.35f, 0.7f)
                repeat(10) { particles.add(Particle(PKind.CRUMB, x + (random.nextFloat() - 0.5f) * 0.02f, y, (random.nextFloat() - 0.5f) * 0.3f, -0.2f - random.nextFloat() * 0.2f, 0.9f, 0.006f, Color(0xFF4A4452))) }
            }
            Fx.BONK -> {
                s(Sfx.BONK, 0.9f, 0.9f + random.nextFloat() * 0.3f)
                particles.burst(PKind.STAR, x, y - 0.04f, 6, 0.35f, 0.011f, T.Sun, up = 0.3f, life = 0.8f)
                person(param)?.let { p ->
                    faces(p, Face.DIZZY, 0.9f, Face.LAUGH, 1.2f)
                    voice(p, Sfx.OOF, 0.6f)
                    laughAround(p.x, p, 0.6f, 0.6f)
                }
            }
            Fx.FLUFF -> {
                s(Sfx.POOF, 0.7f, 1.3f)
                repeat(14) { particles.add(Particle(PKind.LEAF, x + (random.nextFloat() - 0.5f) * 0.06f, y + (random.nextFloat() - 0.5f) * 0.06f, (random.nextFloat() - 0.5f) * 0.7f, -0.3f - random.nextFloat() * 0.4f, 2.6f, 0.012f, Color.White, random.nextFloat() * 360f, (random.nextFloat() - 0.5f) * 200f)) }
                person(param)?.let { p ->
                    faces(p, Face.OOH, 0.35f, Face.LAUGH, 1.4f)
                    voiceLater(p, Sfx.GIGGLE)
                }
            }
            Fx.SPLAT -> {
                s(Sfx.SPLAT, 0.9f)
                shake = max(shake, 0.25f)
                particles.burst(PKind.CRUMB, x, y, 16, 0.6f, 0.012f, Color(0xFFFFF4E0), up = 0.2f)
                particles.burst(PKind.CRUMB, x, y, 6, 0.5f, 0.01f, Color(0xFFFF6F91), up = 0.2f)
                person(param)?.let { p ->
                    faces(p, Face.WOW, 1.0f, Face.YUM, 1.4f)
                    voiceLater(p, Sfx.YUM)
                    laughAround(p.x, p, 0.4f, 0.9f)
                }
                host.haptic()
            }
            Fx.BURP -> person(param)?.let { p ->
                voice(p, Sfx.BURP, 0.9f, own = true)
                particles.add(Particle(PKind.BUBBLE, x + 0.01f, y, 0.05f, -0.12f, 1.2f, 0.018f, Color.White))
                faces(p, Face.OOH, 0.6f, Face.GRIN, 1.5f)
                laughAround(p.x, p, 0.5f, 0.8f)
            }
            Fx.HICCUP -> person(param)?.let { p ->
                voice(p, Sfx.HICCUP, 0.75f, own = true)
                if (p.anim.pose == Pose.STAND && p.anim.hop == 0f) p.anim.hopV = 1.8f
                p.anim.face = Face.OOH
                p.anim.faceTime = 0.3f
                particles.add(Particle(PKind.BUBBLE, x, y, 0.02f, -0.15f, 0.9f, 0.01f, Color.White))
            }
            Fx.GOBBLE -> {
                s(Sfx.CHOMP, 0.8f)
                particles.burst(PKind.CRUMB, x, y - 0.02f, 8, 0.4f, 0.008f, Color(0xFFD9A15A), up = 0.1f)
                person(param)?.let { dog ->
                    dog.anim.face = Face.YUM
                    dog.anim.faceTime = 1.4f
                    dog.anim.hopV = 1.6f
                    voiceLater(dog, Sfx.YUM)
                    particles.burst(PKind.HEART, dog.x, dog.y - dog.h, 3, 0.2f, 0.014f, up = 0.3f)
                    // The owners notice: «hey!» … then they laugh.
                    for (o in world.bodiesIn(place)) {
                        if (o !is Person || o.species != Species.FOLK || abs(o.x - x) > 0.9f || o.anim.face == Face.SLEEP) continue
                        faces(o, Face.OOH, 0.6f, Face.LAUGH, 1.2f)
                    }
                }
            }
            Fx.CURTAIN -> s(Sfx.SWISH, 0.5f, 1.3f)
            Fx.WHEE -> {
                s(Sfx.WHOOSH, 0.8f)
                world.bodiesIn(place).filterIsInstance<Person>().minByOrNull { hypot(it.x - x, it.y - y) }?.let { voice(it, Sfx.GIGGLE, 0.8f) }
                particles.burst(PKind.SNOW, x, y, 12, 0.5f, 0.012f, Color.White)
            }
            Fx.CRACK -> s(Sfx.TICK, 0.6f, 0.5f)
            Fx.BEEP -> s(Sfx.BEEP, 0.6f, 0.8f + (param % 4) * 0.15f)
            Fx.RUMBLE -> s(Sfx.RUMBLE, 0.6f)
            Fx.LAUNCH -> {
                s(Sfx.RUMBLE, 0.9f)
                pending += (time + 0.6f) to { s(Sfx.WHOOSH, 1f, 0.7f) }
            }
            Fx.GROW -> {
                s(Sfx.POP, 0.5f, 1.2f + param * 0.1f)
                particles.burst(PKind.LEAF, x, y, 5, 0.25f, 0.01f, Color(0xFF3BC46B))
            }
            Fx.HARVEST -> {
                s(Sfx.POP, 0.8f)
                s(Sfx.CHIME, 0.4f, 1.5f)
            }
            Fx.HAMMER -> {
                for (i in 0 until 3) pending += (time + i * 0.14f) to { s(Sfx.DRUM, 0.6f, 1.8f) }
                particles.burst(PKind.DUST, x, y, 8, 0.4f, 0.012f)
            }
            Fx.GRAVITY -> {
                s(Sfx.CLICK, 0.7f)
                s(if (param == 1) Sfx.DROP else Sfx.MAGIC, 0.7f)
            }
        }
    }

    private fun voice(p: Person, sfx: Sfx, volume: Float, own: Boolean = false) {
        val animal = if (own) null else when (p.species) {
            Species.CAT -> Sfx.MEOW
            Species.DOG -> Sfx.WOOF
            Species.BUNNY -> Sfx.CHIRP
            Species.DRAGON -> Sfx.ROAR
            Species.ELK -> Sfx.MOO
            Species.PUFFIN -> Sfx.CHIRP
            Species.COW -> Sfx.MOO
            Species.SHEEP -> Sfx.BAA
            Species.CHICKEN -> Sfx.CLUCK
            Species.HORSE -> Sfx.NEIGH
            Species.GOAT -> Sfx.BAA
            Species.FOLK -> null
        }
        val rate = when (p.species) {
            Species.GOAT -> 1.3f
            Species.ELK -> 1.25f
            Species.PUFFIN -> 0.75f
            Species.FOLK -> p.voice
            else -> p.voice.coerceIn(0.85f, 1.2f)
        }
        // Silly voices: a balloon in the hand is helium, shrunk figures squeak and giants rumble.
        val helium = if (world.carried(p).any { it.type == ThingType.BALLOON }) 1.6f else 1f
        val size = (1f / p.scale).pow(0.7f)
        host.sfx(animal ?: sfx, volume, (rate * helium * size).coerceIn(0.5f, 2f))
    }

    private fun person(id: Int): Person? = world.bodies[id] as? Person

    /** Whoever sits at the drums bounces along. */
    private fun drummerHit(f: Fixture?) {
        val drummer = world.seatedAt(f ?: return, 0) ?: return
        drummer.anim.hopV = 1.1f
        drummer.anim.face = Face.GRIN
        drummer.anim.faceTime = 0.5f
    }

    private var lastBeat = -10f

    /** A stethoscope held against someone's chest: «dunk-dunk». */
    private fun listen(dt: Float) {
        for (g in grabs.values) {
            val scope = heldBody(g) as? Thing ?: continue
            if (scope.type != ThingType.STETHOSCOPE) continue
            val tip = Offset(scope.x + scope.w * 0.28f, scope.y - scope.h * 0.2f)
            val patient = world.bodiesIn(place).filterIsInstance<Person>().firstOrNull { p ->
                val chest = Anatomy.at(p, Part.BODY)
                hypot(chest[0] - tip.x, chest[1] - tip.y) < max(0.07f, p.h * 0.25f)
            } ?: continue
            if (time - lastBeat < 0.75f) continue
            lastBeat = time
            val rate = 0.55f + 0.2f / max(0.5f, patient.scale)
            host.sfx(Sfx.DRUM, 0.8f, rate)
            pending += (time + 0.16f) to { host.sfx(Sfx.DRUM, 0.6f, rate * 1.05f) }
            val chest = Anatomy.at(patient, Part.BODY)
            particles.add(Particle(PKind.HEART, chest[0], chest[1] - 0.03f, 0f, -0.15f, 0.9f, 0.014f, T.Berry))
            patient.anim.face = Face.GRIN
            patient.anim.faceTime = 0.6f
            sim.unlock("doctor_heart")
            sim.tasks.record(app.trollfoss.domain.Deed.HEART, place, fixture = null)
        }
        if (dt < 0f) Unit
    }

    /** Is the disco ball spinning here? Then everyone dances. */
    private fun disco(): Fixture? = world.fixturesIn(place).firstOrNull { it.type == FixtureType.DISCO_BALL && it.on }

    /** Everyone close by laughs, a moment later. At most two voices, so it stays a giggle, not a din. */
    private fun laughAround(x: Float, except: Person?, delay: Float = 0.45f, reach: Float = 0.7f) {
        var voices = 0
        for (o in world.bodiesIn(place)) {
            if (o !is Person || o === except || abs(o.x - x) > reach || o.anim.face == Face.SLEEP || o.held) continue
            val loud = voices < 2
            voices++
            val wait = delay + random.nextFloat() * 0.3f
            pending += (time + wait) to {
                o.anim.face = Face.LAUGH
                o.anim.faceTime = 1.5f
                if (o.anim.pose == Pose.STAND) o.anim.hopV = 1.4f
                if (loud) voice(o, Sfx.GIGGLE, 0.5f)
            }
        }
    }

    /** A face now, another a moment later: surprise first, then a laugh or a sheepish grin. */
    private fun faces(p: Person, first: Face, firstTime: Float, then: Face, thenTime: Float) {
        p.anim.face = first
        p.anim.faceTime = firstTime + 0.05f
        pending += (time + firstTime) to {
            p.anim.face = then
            p.anim.faceTime = thenTime
        }
    }

    // ---------------------------------------------------------------------------------- drawing

    /** Bodies in drawing order, furthest back first; held bodies always on top. */
    private fun drawList(): List<Body> =
        world.bodiesIn(place).filter { !hidden(it) }.sortedWith(compareBy<Body> { if (it.held) 1 else 0 }.thenBy { bodyKey(it) }.thenBy { it.z })

    /*
     * «Skrå-3D» drawing order. Everything on the floor band is sorted by its depth line: the further
     * back, the earlier it is drawn. Wall fixtures come first of all. Things on a piece of furniture are
     * drawn just after it, figures in a seat just before the seat's front part.
     */
    private fun fixtureKey(f: Fixture): Float = when {
        f.spec.wall -> -10f + f.id * 0.00001f
        // Rugs lie flat under everything on the floor.
        f.type == FixtureType.RUG -> -1f + f.id * 0.00001f
        else -> f.depth
    }

    private fun bodyKey(b: Body): Float {
        if (b.mode == Mode.SEATED) world.fixtures[b.holder]?.let { f ->
            // Riders on the carousel go far to near as it turns.
            if (f.type == FixtureType.CAROUSEL) return fixtureKey(f) + 0.0006f + 0.00009f * (sin(f.angle + b.slot * 2.0944f) + 1f)
            return fixtureKey(f) + 0.0006f
        }
        if (b.inside >= 0) world.fixtures[b.inside]?.let { return fixtureKey(it) + 0.0003f }
        if (b.resting) {
            world.fixtures[b.restOwner]?.let { return fixtureKey(it) + 0.0005f }
            return b.y.coerceIn(place.back, PlaceId.FRONT)
        }
        val ground = sim.groundOf(place, b)
        if (!sim.zeroG(place)) {
            // Falling onto furniture: drawn with the furniture it will land on.
            sim.fixtureBelow(place, b)?.let { f -> if (f.y < ground) return fixtureKey(f) + 0.0005f }
        }
        return ground
    }

    private fun glimtKey(s: Secret): Float {
        val index = if (s.on >= 0) s.on else s.inside
        if (index >= 0) world.fixtures[s.place.ordinal * 100 + index]?.let { return fixtureKey(it) + 0.0002f }
        return if (s.y >= place.back) s.y else -5f
    }

    private fun hidden(b: Body): Boolean {
        if (b.mode == Mode.INSIDE || b.mode == Mode.WORN || b.mode == Mode.BAG) return true
        if (b.inside >= 0) {
            val box = world.fixtures[b.inside] ?: return false
            if (!box.open && !box.spec.glass) return true
        }
        if (b.mode == Mode.SEATED) {
            val f = world.fixtures[b.holder] ?: return false
            val spot = f.spec.spots.getOrNull(b.slot) ?: return false
            if (spot.hidden && !f.open) return true
        }
        return false
    }

    fun draw(scope: DrawScope, text: TextMeasurer) = with(scope) {
        runPending()
        if (size.width != widthPx || size.height != heightPx) setSize(size.width, size.height, density)
        val lw = max(1.4f, u * 0.0034f)
        val weather = if (place == PlaceId.MOUNTAIN && world.weather == Weather.SUN) Weather.SNOW else world.weather
        val pen = Pen(lw, if (motion) time else 0f, night, weather, rainbow)

        val sx0 = if (motion) sin(time * 61f) * shake * dp(6f) else 0f
        val sy0 = if (motion) sin(time * 47f + 1f) * shake * dp(5f) else 0f
        withTransform({ translate(sx0, sy0 + top) }) {
            drawWorld(pen, lw)
        }
        drawVignette()
        drawWeather(weather)
        withTransform({ translate(0f, top) }) { drawLightning() }
        drawFlights()
        drawBag(text, pen)
        if (flash > 0f) drawRect(Color.White.copy(alpha = flash * 0.85f))
        drawNameTagsLate(text)
    }

    private var lateText: List<Body> = emptyList()

    private fun DrawScope.drawNameTagsLate(text: TextMeasurer) = drawNameTags(lateText, text)

    private fun DrawScope.drawVignette() {
        val r = max(size.width, size.height) * 0.72f
        drawRect(safeRadialGradient(listOf(Color.Transparent, Color.Transparent, Ink.line.copy(alpha = 0.2f)), center, r))
    }

    // Debug-only frame profiler: where the drawing time goes, logged every few seconds.
    private val prof = LongArray(8)
    private val profTypes = HashMap<String, Long>()
    private var profFrames = 0
    private inline fun <T> timed(slot: Int, key: String? = null, block: () -> T): T {
        if (!app.trollfoss.BuildConfig.DEBUG) return block()
        val t0 = System.nanoTime()
        val r = block()
        val d = System.nanoTime() - t0
        prof[slot] += d
        if (key != null) profTypes[key] = (profTypes[key] ?: 0L) + d
        return r
    }

    private fun profLog() {
        if (!app.trollfoss.BuildConfig.DEBUG) return
        if (++profFrames < 120) return
        val names = listOf("back", "fixtures", "bodies", "front", "overlay", "particles", "night", "total")
        val line = names.indices.joinToString(" ") { "${names[it]}=${"%.1f".format(prof[it] / 1e6 / profFrames)}" }
        val top = profTypes.entries.sortedByDescending { it.value }.take(8).joinToString(" ") { "${it.key}=${"%.2f".format(it.value / 1e6 / profFrames)}" }
        android.util.Log.d("TrollfossPerf", "$place ms/frame: $line | top: $top")
        prof.fill(0L)
        profTypes.clear()
        profFrames = 0
    }

    /** Debug-only: layers to leave out when measuring (1 back, 2 fixtures, 4 bodies, 8 front/particles/night). */
    var skip = 0

    private fun DrawScope.drawWorld(pen: Pen, lw: Float) {
        val t0 = System.nanoTime()
        sprites.frame()
        if (skip and 1 == 0) timed(0) { drawPlaceBack(place, cam, u, pen, Decor.styles(world, place)) }
        drawShootingStar()

        // One list for furniture, glimt and bodies, sorted back to front.
        layers.clear()
        for (f in world.fixturesIn(place)) {
            val fx = f.x + f.shiftX
            if (fx + f.spec.w < cam - 0.1f || fx - f.spec.w > cam + viewport + 0.1f) continue
            val k = fixtureKey(f)
            layers += Layer(k, 0, f)
            if (f.spec.front || f.spec.glass) layers += Layer(k + 0.0008f, 1, f)
        }
        // Glimt sit just behind what lies on the same furniture, so a pillow can hide one.
        for (s in sim.visibleSecrets(place)) layers += Layer(glimtKey(s), 2, s)
        val list = drawList()
        for (b in list) if (!b.held) layers += Layer(bodyKey(b), 3, b)
        if (skip and 2 != 0) layers.removeAll { it.kind < 2 }
        if (skip and 4 != 0) layers.removeAll { it.kind == 3 }
        layers.sortWith(layerOrder)
        for (l in layers) {
            when (l.kind) {
                0 -> timed(1, f0(l)) {
                    val f = l.ref as Fixture
                    if (f.lift > 0f) drawLifted(f)
                    translate(sx(f.x + f.shiftX), sy(f.y + f.shiftY)) {
                        val bounce = (if (motion) 1f + f.anim * 0.05f else 1f) + f.lift * 0.03f
                        scale(bounce, 2f - bounce + f.lift * 0.06f, pivot = Offset.Zero) {
                            val contents = if (f.spec.machine == app.trollfoss.domain.Machine.BLENDER || f.spec.machine == app.trollfoss.domain.Machine.CAULDRON || f.spec.machine == app.trollfoss.domain.Machine.BUILD) world.inMachine(f) else emptyList()
                            if (!stampFixture(f, 0, pen, contents.isEmpty())) drawFixtureBack(f, u, pen, contents)
                        }
                    }
                }
                1 -> timed(1, f0(l)) {
                    // The front layer squashes with the back, so a duvet or a bath side stays in place.
                    val f = l.ref as Fixture
                    translate(sx(f.x + f.shiftX), sy(f.y + f.shiftY)) {
                        val bounce = (if (motion) 1f + f.anim * 0.05f else 1f) + f.lift * 0.03f
                        scale(bounce, 2f - bounce + f.lift * 0.06f, pivot = Offset.Zero) {
                            if (!stampFixture(f, 1, pen, true)) drawFixtureFront(f, u, pen)
                        }
                    }
                }
                2 -> drawGlimt(l.ref as Secret, lw)
                else -> timed(2, if (l.ref is Person) "person" else "thing") {
                    val b = l.ref as Body
                    drawShadow(b)
                    drawBody(b, pen)
                }
            }
        }
        if (skip and 8 == 0) timed(3) { drawPlaceFront(place, cam, u, pen) }
        timed(4) {
            disco()?.let { drawDisco(it) }
            drawHints(lw)
            for (b in list) if (b is Person && !b.held) drawBubbles(b, pen)
        }

        drawPreviews(lw)
        for (b in list) if (b.held) {
            drawShadow(b)
            drawBody(b, pen)
        }

        timed(5) { particles.draw(this, u, cam, lw) }
        timed(6) { drawNight(lw) }
        lateText = list
        if (app.trollfoss.BuildConfig.DEBUG) {
            prof[7] += System.nanoTime() - t0
            profLog()
        }
    }

    private fun f0(l: Layer): String? = if (app.trollfoss.BuildConfig.DEBUG) (l.ref as Fixture).type.name else null

    private class Layer(val key: Float, val kind: Int, val ref: Any)
    private val layers = ArrayList<Layer>(128)
    private val layerOrder = compareBy<Layer> { it.key }.thenBy { it.kind }

    private fun DrawScope.drawShadow(b: Body) {
        val floorY = when {
            b.resting -> b.y
            b.held && b.y >= place.back -> b.y
            else -> sim.previewRest(place, b) ?: return
        }
        val lift = max(0f, floorY - b.y)
        val w = when (b) {
            is Person -> if (b.anim.pose == Pose.LIE) b.h * 0.8f else b.w * 1.1f
            else -> b.w * 1.05f
        }
        if (b is Person && b.mode == Mode.SEATED) return
        val fade = max(0.2f, 1f - lift * 2.5f)
        groundShadow(sx(b.x), sy(floorY), w * u * fade, fade)
    }

    private fun DrawScope.drawBody(b: Body, pen: Pen) {
        val pop = if (b.age < 0.3f && motion) 0.6f + 0.4f * (b.age / 0.3f) + sin(b.age / 0.3f * PI.toFloat()) * 0.25f else 1f
        val sq = b.squash.coerceIn(-0.4f, 0.5f)
        when (b) {
            is Person -> {
                val a = b.anim
                val walking = !a.walkTo.isNaN()
                val step = if (walking) abs(sin(a.walkPhase * PI.toFloat())) * b.h * 0.07f else 0f
                val bob = (if (a.pose == Pose.FLOAT) sin(time * 1.6f + b.id) * 0.008f else 0f) - step
                val sway = if (walking) sin(a.walkPhase * PI.toFloat()) * 4f else 0f
                translate(sx(b.x), sy(b.y - a.hop + bob)) {
                    val spin = if (a.spin > 0f) (1f - a.spin) * 360f * (if (b.id % 2 == 0) 1f else -1f) else 0f
                    val giggle = if (a.tickle > 0f) sin(time * 38f) * 7f * min(1f, a.tickle) else 0f
                    rotate(a.tilt + sway + spin + giggle + (if (a.pose == Pose.FLOAT) sin(time + b.id) * 6f else 0f), pivot = Offset(0f, -b.h * u * 0.5f)) {
                        scale((1f + sq * 0.22f) * pop * a.facing, (1f - sq * 0.22f) * pop, pivot = Offset.Zero) {
                            val carried = world.carried(b)
                            val holding = carried.any { it.slot == Slot.HAND.ordinal }
                            val figure: DrawScope.(Float) -> Unit = { t ->
                                val p = Pen(pen.lw, t, pen.night, pen.weather, pen.rainbow)
                                if (xrayed(b)) drawSkeleton(b, p) else drawPerson(b.species, b.look, a.pose, a, b.h * u, p, holding, seed = b.id * 0.37f)
                                if (a.cream > 0f) drawCream(b, p)
                                if (a.ink > 0f) drawInk(b, p)
                                for (t2 in carried.sortedBy { it.slot }) drawCarried(b, t2, p)
                            }
                            if (!stampPerson(b, pen, figure)) figure(pen.t)
                        }
                    }
                }
            }
            is Thing -> {
                val speed = hypot(b.vx, b.vy)
                if (!b.held && !b.resting && speed > 2.2f && motion) {
                    val c = Offset(sx(b.x), sy(b.y - b.h / 2))
                    val tail = Offset(sx(b.x - b.vx * 0.05f), sy(b.y - b.h / 2 - b.vy * 0.05f))
                    drawLine(Brush.linearGradient(listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.45f)), tail, c), tail, c, strokeWidth = min(b.w, b.h) * u * 0.7f, cap = StrokeCap.Round)
                }
                val bob = if (sim.zeroG(place) && !b.held) sin(time * 1.3f + b.id) * 0.004f else 0f
                translate(sx(b.x), sy(b.y + bob)) {
                    rotate(b.rot, pivot = Offset(0f, -b.h * u * 0.5f)) {
                        scale((1f + sq * 0.25f) * pop, (1f - sq * 0.25f) * pop, pivot = Offset.Zero) {
                            if (!stampThing(b, pen)) drawThing(b.type, b.variant, b.used, b.w * u, b.h * u, pen, b.cook)
                        }
                    }
                }
            }
        }
    }

    private fun xrayed(p: Person): Boolean =
        p.mode == Mode.SEATED && world.fixtures[p.holder]?.let { it.type == FixtureType.XRAY && it.on } == true

    /**
     * What the X-ray shows: a friendly skeleton in the figure's pose, skull grinning, bones glowing,
     * wiggling a little because being X-rayed tickles.
     */
    private fun DrawScope.drawSkeleton(p: Person, pen: Pen) {
        val h = p.h * u
        val bone = Color(0xFFF4FFF8)
        val glow = Color(0xFF7CFFB2).copy(alpha = 0.35f)
        val wiggle = sin(time * 9f) * h * 0.015f
        val w = h * (if (p.species == Species.FOLK) 1f else 1.6f)
        fun b(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = 0.045f) {
            drawLine(glow, Offset(x1 * w, y1 * h), Offset(x2 * w, y2 * h), strokeWidth = h * (width + 0.03f), cap = StrokeCap.Round)
            drawLine(Ink.line, Offset(x1 * w, y1 * h), Offset(x2 * w, y2 * h), strokeWidth = h * width + pen.lw * 1.6f, cap = StrokeCap.Round)
            drawLine(bone, Offset(x1 * w, y1 * h), Offset(x2 * w, y2 * h), strokeWidth = h * width, cap = StrokeCap.Round)
        }
        // Legs, pelvis, spine, ribs, arms.
        b(-0.08f, -0.02f, -0.07f, -0.24f)
        b(0.08f, -0.02f, 0.07f, -0.24f)
        b(-0.1f, -0.26f, 0.1f, -0.26f, 0.05f)
        b(0f, -0.26f, 0f, -0.55f, 0.035f)
        for (k in 0 until 3) {
            val y = -0.33f - k * 0.065f
            b(-0.12f + k * 0.01f, y, 0.12f - k * 0.01f, y, 0.025f)
        }
        b(-0.14f, -0.5f, -0.24f, -0.28f + wiggle / h)
        b(0.14f, -0.5f, 0.24f, -0.28f - wiggle / h)
        // The skull.
        val c = Offset(0f, -0.71f * h)
        val r = Anatomy.headRadius(p.species) * h
        drawCircle(glow, r * 1.25f, c)
        drawCircle(bone, r, c)
        drawCircle(Ink.line, r, c, style = Stroke(pen.lw))
        for (s in listOf(-1f, 1f)) drawOval(Ink.line, Offset(c.x + s * r * 0.38f - r * 0.2f, c.y - r * 0.25f), Size(r * 0.4f, r * 0.46f))
        drawLine(Ink.line, Offset(c.x - r * 0.35f, c.y + r * 0.5f), Offset(c.x + r * 0.35f, c.y + r * 0.5f), strokeWidth = pen.lw)
        for (k in -2..2) drawLine(Ink.line, Offset(c.x + k * r * 0.14f, c.y + r * 0.38f), Offset(c.x + k * r * 0.14f, c.y + r * 0.62f), strokeWidth = pen.lw * 0.7f)
    }

    /** Octopus ink: dark splodges round the eyes and a surprised little mouth. */
    private fun DrawScope.drawInk(p: Person, pen: Pen) {
        val alpha = min(1f, p.anim.ink / 0.8f)
        val f = Anatomy.fraction(p.species, p.anim.pose, Part.HEAD)
        val c = Offset(f[0] * p.h * u, f[1] * p.h * u)
        val r = Anatomy.headRadius(p.species) * p.h * u
        val ink = Color(0xFF3B2A55).copy(alpha = 0.85f * alpha)
        for ((bx, by, br) in listOf(Triple(-0.45f, 0.05f, 0.42f), Triple(0.45f, 0.05f, 0.42f), Triple(0f, -0.35f, 0.35f), Triple(0.2f, 0.45f, 0.25f), Triple(-0.6f, -0.3f, 0.2f))) {
            drawCircle(ink, r * br, Offset(c.x + bx * r, c.y + by * r))
        }
        for (s in listOf(-1f, 1f)) {
            drawCircle(Color.White.copy(alpha = alpha), r * 0.14f, Offset(c.x + s * r * 0.42f, c.y + r * 0.02f))
            drawCircle(Ink.line.copy(alpha = alpha), r * 0.07f, Offset(c.x + s * r * 0.42f, c.y + r * 0.02f))
        }
        if (pen.lw < 0f) Unit
    }

    /** Cream all over the face after a cake in the face, with a cherry on top. Eyes stay free. */
    private fun DrawScope.drawCream(p: Person, pen: Pen) {
        val a = p.anim
        val alpha = min(1f, a.cream / 0.8f)
        val f = Anatomy.fraction(p.species, a.pose, Part.HEAD)
        val c = Offset(f[0] * p.h * u, f[1] * p.h * u)
        val r = Anatomy.headRadius(p.species) * p.h * u
        val cream = Color(0xFFFFFBF2).copy(alpha = alpha)
        val blobs = listOf(-0.62f to 0.3f, 0.62f to 0.3f, 0f to 0.62f, -0.25f to -0.72f, 0.3f to -0.78f, 0f to 0.95f)
        for ((bx, by) in blobs) {
            val o = Offset(c.x + bx * r, c.y + by * r)
            drawCircle(Ink.line.copy(alpha = alpha), r * 0.34f + pen.lw * 0.6f, o)
        }
        for ((bx, by) in blobs) drawCircle(cream, r * 0.34f, Offset(c.x + bx * r, c.y + by * r))
        // Drips, and the cherry.
        val drip = min(1f, (Jokes.CREAM_SECONDS - a.cream) / 2f)
        drawLine(cream, Offset(c.x - r * 0.1f, c.y + r * 0.95f), Offset(c.x - r * 0.1f, c.y + r * (1.05f + drip * 0.35f)), strokeWidth = r * 0.14f, cap = StrokeCap.Round)
        drawCircle(Color(0xFFE8304A).copy(alpha = alpha), r * 0.16f, Offset(c.x + r * 0.3f, c.y - r * 1.02f))
        drawCircle(Ink.line.copy(alpha = alpha), r * 0.16f, Offset(c.x + r * 0.3f, c.y - r * 1.02f), style = Stroke(pen.lw * 0.8f))
        shine(Offset(c.x + r * 0.25f, c.y - r * 1.07f), r * 0.08f, r * 0.06f, alpha * 0.9f)
    }

    /** Draws a carried thing inside the figure's own transform, so it moves and squashes with it. */
    private fun DrawScope.drawCarried(p: Person, t: Thing, pen: Pen) {
        val s = carriedScale(p, t)
        val c = carriedCenter(p, t)
        val lx = (c.x - p.x) * u
        val ly = (c.y - p.y + p.anim.hop) * u
        val lie = p.anim.pose == Pose.LIE
        translate(lx, ly + t.h * s * u * 0.5f) {
            rotate(if (lie) -90f else 0f, pivot = Offset(0f, -t.h * s * u * 0.5f)) {
                scale(s, s, pivot = Offset.Zero) {
                    drawThing(t.type, t.variant, t.used, t.w * u, t.h * u, Pen(pen.lw / s, pen.t, pen.night, pen.weather, pen.rainbow))
                }
            }
        }
    }

    /** A figure's name in a little speech bubble above its head, for a moment after a tap. */
    private fun DrawScope.drawNameTags(list: List<Body>, text: TextMeasurer) {
        for (b in list) {
            val p = b as? Person ?: continue
            if (p.anim.nameTag <= 0f || p.name.isBlank()) continue
            val alpha = min(1f, p.anim.nameTag / 0.4f)
            val grow = min(1f, (2.2f - p.anim.nameTag) / 0.2f)
            val layout = text.measure(p.name, TextStyle(color = T.Ink.copy(alpha = alpha), fontSize = 17.sp, fontWeight = FontWeight.Black))
            val hat = Anatomy.at(p, Part.HAT)
            val cx = sx(p.x)
            val by = sy(hat[1] - p.anim.hop) + top - dp(14f)
            val w = layout.size.width + dp(22f)
            val h = layout.size.height + dp(10f)
            scale(grow, grow, pivot = Offset(cx, by)) {
                val box = Rect(cx - w / 2, by - h, cx + w / 2, by)
                val tail = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - dp(7f), by - dp(1f))
                    lineTo(cx, by + dp(8f))
                    lineTo(cx + dp(7f), by - dp(1f))
                    close()
                }
                drawPath(tail, Color.White.copy(alpha = alpha))
                drawPath(tail, Ink.line.copy(alpha = alpha), style = Stroke(dp(2f)))
                drawRoundRect(Color.White.copy(alpha = alpha), box.topLeft, box.size, CornerRadius(h / 2))
                drawRoundRect(Ink.line.copy(alpha = alpha), box.topLeft, box.size, CornerRadius(h / 2), style = Stroke(dp(2f)))
                drawRect(Color.White.copy(alpha = alpha), Offset(cx - dp(6f), by - dp(3f)), Size(dp(12f), dp(3f)))
                drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, by - h + dp(5f)))
            }
        }
    }

    /** A soft round glow; silently nothing when the radius is too small to draw (a growing glimt, a shrunk light). */
    private fun DrawScope.glow(stops: List<Color>, center: Offset, radius: Float, blendMode: BlendMode = BlendMode.SrcOver) {
        if (radius < 0.5f) return
        drawCircle(safeRadialGradient(stops, center, radius), radius, center, blendMode = blendMode)
    }

    // ---------------------------------------------------------------------------------- sprites

    /** What a piece of furniture looks like right now, as far as its picture goes. */
    private data class FixtureLook(
        val type: FixtureType, val variant: Int, val layer: Int, val open: Boolean, val on: Boolean, val mode: Int, val count: Int,
        val night: Int, val weather: Weather, val rainbow: Int, val size: Int,
    )

    private data class ThingLook(val type: ThingType, val variant: Int, val used: Int, val night: Int, val weather: Weather, val rainbow: Int, val size: Int)

    /** Stamps a cached picture of [f]'s [layer] (0 back, 1 front); false when it must be drawn live. */
    private fun DrawScope.stampFixture(f: Fixture, layer: Int, pen: Pen, empty: Boolean): Boolean {
        if (!empty || f.anim > 0.01f || f.lift > 0f || f.type in LIVE_FIXTURES || (f.on && f.type in LIVE_WHEN_ON)) return false
        val look = FixtureLook(f.type, f.variant, layer, f.open, f.on, f.mode, f.count, (pen.night * 10f).toInt(), pen.weather, (pen.rainbow * 5f).toInt(), u.toInt())
        val w = f.spec.w * u
        val h = f.spec.h * u
        val bounds = Rect(-w / 2 - 0.14f * u, -h - 0.4f * u, w / 2 + 0.34f * u, 0.12f * u)
        val night = look.night / 10f
        val rainbow = look.rainbow / 5f
        val draw: DrawScope.(Float) -> Unit = { t ->
            val p = Pen(pen.lw, t, night, pen.weather, rainbow)
            if (layer == 0) drawFixtureBack(f, u, p, emptyList()) else drawFixtureFront(f, u, p)
        }
        return with(sprites) {
            // Still art is stamped; art that moves by itself is redrawn a few times a second.
            stamp(look, bounds, pen.t, draw) || (animated(look) && stampSlow(SlowKey(f.id, layer), bounds, pen.t, SLOW_HZ, draw))
        }
    }

    /** Identifies a slow picture: a fixture layer (layer 0 or 1), a thing (2) or a figure (3). */
    private data class SlowKey(val id: Int, val layer: Int)

    private fun DrawScope.stampThing(b: Thing, pen: Pen): Boolean {
        if (b.cook != 0f) return false
        val look = ThingLook(b.type, b.variant, b.used, (pen.night * 10f).toInt(), pen.weather, (pen.rainbow * 5f).toInt(), u.toInt())
        val w = b.w * u
        val h = b.h * u
        val pad = max(w, h) * 0.6f + pen.lw * 6f
        val bounds = Rect(-w / 2 - pad, -h - pad * 1.4f, w / 2 + pad * 1.4f, pad)
        val night = look.night / 10f
        val rainbow = look.rainbow / 5f
        val draw: DrawScope.(Float) -> Unit = { t -> drawThing(b.type, b.variant, b.used, w, h, Pen(pen.lw, t, night, pen.weather, rainbow), 0f) }
        return with(sprites) {
            stamp(look, bounds, pen.t, draw) || (animated(look) && stampSlow(SlowKey(b.id, 2), bounds, pen.t, SLOW_HZ, draw))
        }
    }

    /**
     * A figure as a picture refreshed about a dozen times a second (faster while held). Hops, squash,
     * tilt and spins are applied around it every frame, so movement stays smooth; only breathing,
     * blinking and faces update at the slower rate.
     */
    private fun DrawScope.stampPerson(b: Person, pen: Pen, draw: DrawScope.(Float) -> Unit): Boolean {
        val h = b.h * u
        val lie = b.anim.pose == Pose.LIE
        val bounds = Rect(-h * (if (lie) 0.95f else 0.8f), -h * 2.1f, h * (if (lie) 0.95f else 0.8f), h * 0.3f)
        val hz = when {
            b.held -> 24f
            !b.anim.walkTo.isNaN() -> 20f
            else -> 13f
        }
        return with(sprites) { stampSlow(SlowKey(b.id, 3), bounds, pen.t, hz, draw) }
    }

    // ---------------------------------------------------------------------------------- thunder and greetings

    private var lightning = 0f
    private var boltX = 0f
    private val bolt = FloatArray(14)
    private var nextStrike = 8f
    private var greeted = false

    /** One flash and its thunder right now (debug hook and the weather both use it). */
    fun strike() {
        lightning = 1f
        boltX = cam + viewport * (0.15f + random.nextFloat() * 0.7f)
        for (i in bolt.indices) bolt[i] = (random.nextFloat() - 0.5f) * 0.07f
        pending += (time + 0.13f) to { lightning = 0.85f }
        pending += (time + 0.35f + random.nextFloat() * 1.3f) to {
            host.sfx(Sfx.RUMBLE, 0.95f, 0.6f)
            host.sfx(Sfx.THUD, 0.6f, 0.45f)
            shake = max(shake, 0.35f)
            for (o in world.bodiesIn(place)) {
                if (o !is Person || o.held || o.anim.face == Face.SLEEP) continue
                faces(o, Face.OOH, 0.6f, Face.GRIN, 1.2f)
                if (o.anim.pose == Pose.STAND && o.anim.hop == 0f && random.nextFloat() < 0.6f) o.anim.hopV = 1.7f
            }
        }
    }

    /** Rain outdoors sometimes brings lightning, and thunder a moment after it. */
    private fun updateWeather(dt: Float) {
        lightning = max(0f, lightning - dt * 3.2f)
        if (!greeted) {
            greeted = true
            greetVisitors()
        }
        if (!motion || !place.outdoor || place == PlaceId.UNDERWATER || world.weather != Weather.RAIN) return
        nextStrike -= dt
        if (nextStrike > 0f) return
        nextStrike = 7f + random.nextFloat() * 14f
        lightning = 1f
        boltX = cam + viewport * (0.15f + random.nextFloat() * 0.7f)
        for (i in bolt.indices) bolt[i] = (random.nextFloat() - 0.5f) * 0.07f
        // The flicker: a second flash right after the first.
        pending += (time + 0.13f) to { lightning = 0.85f }
        val delay = 0.35f + random.nextFloat() * 1.3f
        pending += (time + delay) to {
            host.sfx(Sfx.RUMBLE, 0.95f, 0.6f)
            host.sfx(Sfx.THUD, 0.6f, 0.45f)
            shake = max(shake, 0.35f)
            for (o in world.bodiesIn(place)) {
                if (o !is Person || o.held || o.anim.face == Face.SLEEP) continue
                faces(o, Face.OOH, 0.6f, Face.GRIN, 1.2f)
                if (o.anim.pose == Pose.STAND && o.anim.hop == 0f && random.nextFloat() < 0.6f) o.anim.hopV = 1.7f
            }
        }
    }

    private fun DrawScope.drawLightning() {
        if (lightning <= 0.02f) return
        val a = lightning
        if (lightning > 0.55f) {
            val path = androidx.compose.ui.graphics.Path()
            var x = sx(boltX)
            var y = -top
            path.moveTo(x, y)
            val steps = bolt.size
            for (i in 0 until steps) {
                x += bolt[i] * u
                y += (heightPx + top) * 0.55f / steps
                path.lineTo(x, y)
            }
            drawPath(path, Color.White.copy(alpha = 0.35f * a), style = Stroke(dp(10f), cap = StrokeCap.Round))
            drawPath(path, Color.White.copy(alpha = a), style = Stroke(dp(3.5f), cap = StrokeCap.Round))
        }
        drawRect(Color(0xFFEAF2FF).copy(alpha = 0.55f * a), Offset(0f, -top), Size(size.width, size.height + top))
    }

    /** Arriving in a place: the folk in view look up and wave hello, one after another. */
    private fun greetVisitors() {
        var n = 0
        for (o in world.bodiesIn(place)) {
            if (o !is Person || o.species != Species.FOLK || o.held || o.anim.face == Face.SLEEP || !visible(o)) continue
            val wait = 0.35f + n * 0.3f + random.nextFloat() * 0.25f
            n++
            pending += (time + wait) to {
                if (!o.held && o.anim.face != Face.SLEEP) {
                    o.anim.wave = 1.5f
                    o.anim.face = Face.GRIN
                    o.anim.faceTime = 1.4f
                    if (n <= 3) voice(o, Sfx.BABBLE, 0.22f)
                }
            }
            if (n >= 4) break
        }
    }

    // ---------------------------------------------------------------------------------- the night sky

    private class ShootingStar(var x: Float, var y: Float, val vx: Float, val vy: Float, var life: Float)

    private var star: ShootingStar? = null
    private var starRainUntil = 0f

    /** Shooting stars cross the night sky now and then; a quick tap catches one and it drops a gem. */
    private fun updateSky(dt: Float) {
        if (time < starRainUntil && random.nextFloat() < dt * 30f) {
            particles.add(Particle(PKind.STAR, cam + random.nextFloat() * viewport, -0.06f, (random.nextFloat() - 0.5f) * 0.1f, 0.45f + random.nextFloat() * 0.3f, 2.6f, 0.012f + random.nextFloat() * 0.012f, listOf(T.Sun, T.SunTop, Color.White, T.Berry, T.SeaTop)[random.nextInt(5)], random.nextFloat() * 360f, (random.nextFloat() - 0.5f) * 300f))
        }
        val s = star
        if (s != null) {
            s.life -= dt
            s.x += s.vx * dt
            s.y += s.vy * dt
            if (s.life <= 0f) star = null
            return
        }
        val sky = place.outdoor && place != PlaceId.UNDERWATER && place != PlaceId.MOUNTAIN
        if (motion && sky && night > 0.8f && world.weather != Weather.RAIN && random.nextFloat() < dt / 40f) {
            val fromLeft = random.nextBoolean()
            star = ShootingStar(cam + viewport * (if (fromLeft) 0.05f + random.nextFloat() * 0.3f else 0.65f + random.nextFloat() * 0.3f), 0.04f + random.nextFloat() * 0.16f, (if (fromLeft) 1f else -1f) * 0.2f, 0.09f, 4.2f)
        }
    }

    private fun DrawScope.drawShootingStar() {
        val s = star ?: return
        val fade = min(1f, min(s.life / 0.6f, (4.2f - s.life) / 0.4f))
        val head = Offset(sx(s.x), sy(s.y))
        val tail = Offset(sx(s.x - s.vx * 1.1f), sy(s.y - s.vy * 1.1f))
        drawLine(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.85f * fade)), tail, head), tail, head, strokeWidth = 0.012f * u, cap = StrokeCap.Round)
        val r = 0.022f * u * (1f + sin(time * 14f) * 0.12f)
        glow(listOf(T.SunTop.copy(alpha = 0.7f * fade), Color.Transparent), head, r * 3f)
        val path = starPath(head, r, r * 0.45f, time * 90f)
        drawPath(path, Color.White.copy(alpha = fade))
        drawPath(path, Ink.line.copy(alpha = 0.6f * fade), style = Stroke(1.4f))
    }

    /** True when a tap at [p] (scene units) caught the shooting star. */
    private fun catchStar(p: Offset): Boolean {
        val s = star ?: return false
        if (hypot(p.x - s.x, p.y - s.y) > 0.12f) return false
        star = null
        particles.burst(PKind.STAR, s.x, s.y, 22, 0.9f, 0.014f, T.Sun, up = 0.2f, life = 1.4f)
        particles.burst(PKind.SPARK, s.x, s.y, 14, 0.5f, 0.012f, Color.White)
        host.sfx(Sfx.CHIME, 0.9f, 1.3f)
        host.sfx(Sfx.SPARKLE, 0.8f)
        val gem = world.addThing(ThingType.GEM, random.nextInt(ThingType.GEM.variants), place, s.x.coerceIn(0.1f, place.width - 0.1f), s.y)
        gem.ground = (place.back + PlaceId.FRONT) / 2f
        gem.vy = 0.2f
        onSpawn(gem)
        sim.egg("starshot")
        host.haptic()
        host.changed()
        return true
    }

    /** A glowing ring on the floor under furniture being moved, so the new spot is clear. */
    private fun DrawScope.drawLifted(f: Fixture) {
        val w = (f.spec.w + 0.06f) * u
        val c = Offset(sx(f.x + f.shiftX), sy(f.y))
        val pulse = 1f + sin(time * 8f) * 0.05f
        val rect = androidx.compose.ui.geometry.Rect(c.x - w / 2 * pulse, c.y - w * 0.09f, c.x + w / 2 * pulse, c.y + w * 0.09f)
        drawOval(T.SunTop.copy(alpha = 0.35f * f.lift), rect.topLeft, rect.size)
        drawOval(Color.White.copy(alpha = 0.9f * f.lift), rect.topLeft, rect.size, style = Stroke(dp(3f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dp(10f), dp(7f)), time * dp(30f))))
    }

    /** Coloured spots sweeping over walls and floor from the spinning disco ball. */
    private fun DrawScope.drawDisco(ball: Fixture) {
        drawRect(Color(0xFF1B1036).copy(alpha = 0.28f), Offset(0f, -top), Size(size.width, size.height + top))
        val colors = listOf(T.Berry, T.Sun, T.Sea, T.Mint, T.Grape, Color(0xFFFF9F43))
        val origin = Offset(sx(ball.x), sy(ball.y - 0.07f))
        for (k in 0 until 9) {
            val a = ball.angle * (if (k % 2 == 0) 1f else -0.7f) + k * 0.7f
            val x = ball.x + sin(a) * (0.5f + (k % 3) * 0.35f)
            val y = 0.45f + (kotlin.math.cos(a * 1.3f) * 0.5f + 0.5f) * 0.45f
            val c = Offset(sx(x), sy(y))
            val r = (0.05f + (k % 3) * 0.015f) * u
            val color = colors[k % colors.size]
            drawLine(color.copy(alpha = 0.10f), origin, c, strokeWidth = r * 0.9f, cap = StrokeCap.Round)
            glow(listOf(color.copy(alpha = 0.55f), Color.Transparent), c, r, blendMode = BlendMode.Plus)
        }
    }

    /** A soft pulsing ring round the things a tapped figure wishes for. */
    private fun DrawScope.drawHints(lw: Float) {
        val w = hint ?: return
        if (time > hintUntil || w.kind != WishKind.THING) return
        val fade = min(1f, (hintUntil - time) / 0.5f)
        val pulse = 1f + sin(time * 9f) * 0.12f
        for (t in world.bodiesIn(place)) {
            if (t !is Thing || t.type != w.thing || hidden(t) || t.mode != Mode.FREE) continue
            val c = Offset(sx(t.x), sy(t.y - t.h / 2))
            val r = max(t.w, t.h) * 0.75f * u * pulse
            glow(listOf(T.SunTop.copy(alpha = 0.55f * fade), Color.Transparent), c, r * 1.5f)
            drawCircle(Color.White.copy(alpha = 0.9f * fade), r, c, style = Stroke(lw * 2f))
        }
    }

    /**
     * A figure's thought bubble (its wish, as a picture) and speech bubble (a little chat). The thought
     * bubble sits beside the head on the side with more room and pops in with a wobble.
     */
    private fun DrawScope.drawBubbles(p: Person, pen: Pen) {
        val a = p.anim
        val head = Anatomy.at(p, Part.HEAD)
        val hat = Anatomy.at(p, Part.HAT)
        val lw = pen.lw
        // Dizzy: little stars circle the head.
        if (a.face == Face.DIZZY) {
            val hw = headWidth(p.species, p.h)
            for (k in 0 until 3) {
                val ang = time * 5f + k * 2.094f
                val depth = sin(ang)
                val c = Offset(sx(head[0] + kotlin.math.cos(ang) * hw * 0.55f), sy(hat[1] - p.anim.hop + 0.012f + depth * 0.012f))
                val r = (0.011f + depth * 0.003f) * u
                val star = starPath(c, r, r * 0.45f, time * 200f)
                drawPath(star, T.Sun)
                drawPath(star, Ink.line, style = Stroke(lw * 0.8f))
            }
        }
        val w = a.wish
        if (w != null) {
            val born = bubbleBorn[p.id] ?: (time - 1f)
            val t = ((time - born) / 0.45f).coerceIn(0f, 1f)
            val grow = 1f - (1f - t).pow(3) + sin(t * PI.toFloat()) * 0.18f
            val fade = ((app.trollfoss.domain.Life.WISH_LIFE - w.age) / 1.2f).coerceIn(0f, 1f)
            val side = if (p.x - cam > viewport - 0.32f) -1f else 1f
            val r = 0.052f * grow * min(1.25f, max(0.8f, p.h / 0.3f))
            val bob = sin(time * 2.4f + p.id) * 0.005f
            val cx = head[0] + side * (headWidth(p.species, p.h) * 0.55f + r * 0.9f)
            val cy = hat[1] - p.anim.hop - r * 0.9f + bob
            val c = Offset(sx(cx), sy(cy))
            val rp = r * u
            val alpha = fade
            // Two little puffs lead from the head to the bubble.
            val p1 = Offset(sx(head[0] + side * headWidth(p.species, p.h) * 0.45f), sy(hat[1] - p.anim.hop + 0.01f))
            val p2 = Offset((p1.x + c.x) / 2f, (p1.y + c.y) / 2f + rp * 0.35f)
            for ((pt, pr) in listOf(p1 to rp * 0.13f, p2 to rp * 0.22f)) {
                drawCircle(Color.White.copy(alpha = alpha), pr, pt)
                drawCircle(Ink.line.copy(alpha = alpha), pr, pt, style = Stroke(lw * 0.8f))
            }
            drawCircle(Ink.shadow.copy(alpha = 0.25f * alpha), rp, c + Offset(rp * 0.08f, rp * 0.12f))
            drawCircle(Color.White.copy(alpha = alpha), rp, c)
            drawCircle(Ink.line.copy(alpha = alpha), rp, c, style = Stroke(lw * 1.1f))
            if (alpha > 0.05f) drawWishPicture(w, c, rp * 0.72f, pen)
        }
        if (a.sayTime > 0f && a.say >= 0) {
            val t = min(1f, (1.7f - a.sayTime) / 0.18f).coerceIn(0f, 1f)
            val alpha = min(1f, a.sayTime / 0.3f)
            val r = 0.034f * t
            val c = Offset(sx(head[0] - 0.02f), sy(hat[1] - p.anim.hop - r * 1.4f))
            val rp = r * u
            val tail = androidx.compose.ui.graphics.Path().apply {
                moveTo(c.x - rp * 0.35f, c.y + rp * 0.75f)
                lineTo(c.x + rp * 0.1f, c.y + rp * 1.45f)
                lineTo(c.x + rp * 0.35f, c.y + rp * 0.7f)
                close()
            }
            drawPath(tail, Color.White.copy(alpha = alpha))
            drawPath(tail, Ink.line.copy(alpha = alpha), style = Stroke(lw * 0.9f))
            drawRoundRect(Color.White.copy(alpha = alpha), Offset(c.x - rp * 1.3f, c.y - rp), Size(rp * 2.6f, rp * 2f), CornerRadius(rp))
            drawRoundRect(Ink.line.copy(alpha = alpha), Offset(c.x - rp * 1.3f, c.y - rp), Size(rp * 2.6f, rp * 2f), CornerRadius(rp), style = Stroke(lw * 0.9f))
            drawRect(Color.White.copy(alpha = alpha), Offset(c.x - rp * 0.3f, c.y + rp * 0.62f), Size(rp * 0.62f, rp * 0.5f))
            drawChatIcon(a.say, c, rp * 0.62f, lw, alpha)
        }
    }

    private fun DrawScope.drawWishPicture(w: Wish, c: Offset, r: Float, pen: Pen) {
        when (w.kind) {
            WishKind.THING -> {
                val type = w.thing ?: return
                val s = r * 1.7f / max(type.w, type.h) / u
                translate(c.x, c.y + type.h * s * u / 2f) {
                    scale(s, s, pivot = Offset.Zero) {
                        drawThing(type, w.variant, 0, type.w * u, type.h * u, Pen(pen.lw / s, pen.t, 0f, pen.weather, 0f))
                    }
                }
            }
            WishKind.SLEEP -> {
                // A moon cut by the white of the bubble, and a little «z».
                val m = Offset(c.x - r * 0.15f, c.y + r * 0.05f)
                drawCircle(T.Sun, r * 0.62f, m)
                drawCircle(Color.White, r * 0.52f, m + Offset(r * 0.3f, -r * 0.2f))
                val z = androidx.compose.ui.graphics.Path().apply {
                    moveTo(c.x + r * 0.35f, c.y - r * 0.7f)
                    lineTo(c.x + r * 0.75f, c.y - r * 0.7f)
                    lineTo(c.x + r * 0.35f, c.y - r * 0.3f)
                    lineTo(c.x + r * 0.75f, c.y - r * 0.3f)
                }
                drawPath(z, T.Grape, style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
            }
            WishKind.MUSIC -> drawChatIcon(2, c, r, pen.lw, 1f)
            WishKind.FRIEND -> drawChatIcon(0, c, r, pen.lw, 1f)
        }
    }

    /** 0 heart, 1 star, 2 notes, 3 sun, 4 flower, 5 laugh. */
    private fun DrawScope.drawChatIcon(icon: Int, c: Offset, r: Float, lw: Float, alpha: Float) {
        when (icon) {
            0 -> {
                val heart = androidx.compose.ui.graphics.Path().apply {
                    moveTo(c.x, c.y + r * 0.75f)
                    cubicTo(c.x - r * 1.3f, c.y - r * 0.1f, c.x - r * 0.55f, c.y - r * 1.1f, c.x, c.y - r * 0.35f)
                    cubicTo(c.x + r * 0.55f, c.y - r * 1.1f, c.x + r * 1.3f, c.y - r * 0.1f, c.x, c.y + r * 0.75f)
                    close()
                }
                drawPath(heart, T.Berry.copy(alpha = alpha))
                drawPath(heart, Ink.line.copy(alpha = alpha), style = Stroke(lw * 0.8f))
            }
            1 -> {
                val star = starPath(c, r * 0.85f, r * 0.4f)
                drawPath(star, T.Sun.copy(alpha = alpha))
                drawPath(star, Ink.line.copy(alpha = alpha), style = Stroke(lw * 0.8f))
            }
            2 -> for (i in 0..1) {
                val nx = c.x - r * 0.45f + i * r * 0.8f
                val ny = c.y + r * 0.45f - i * r * 0.2f
                drawOval(T.Grape.copy(alpha = alpha), Offset(nx - r * 0.3f, ny - r * 0.2f), Size(r * 0.55f, r * 0.42f))
                drawLine(T.Grape.copy(alpha = alpha), Offset(nx + r * 0.2f, ny), Offset(nx + r * 0.2f, ny - r * 0.95f), strokeWidth = lw * 1.1f, cap = StrokeCap.Round)
                if (i == 1) drawLine(T.Grape.copy(alpha = alpha), Offset(nx + r * 0.2f - r * 0.8f, c.y + r * 0.45f - r * 0.95f), Offset(nx + r * 0.2f, ny - r * 0.95f), strokeWidth = lw * 1.3f, cap = StrokeCap.Round)
            }
            3 -> {
                for (k in 0 until 8) {
                    val ang = k * PI.toFloat() / 4f
                    drawLine(T.Sun.copy(alpha = alpha), Offset(c.x + kotlin.math.cos(ang) * r * 0.55f, c.y + sin(ang) * r * 0.55f), Offset(c.x + kotlin.math.cos(ang) * r * 0.9f, c.y + sin(ang) * r * 0.9f), strokeWidth = lw, cap = StrokeCap.Round)
                }
                drawCircle(T.Sun.copy(alpha = alpha), r * 0.42f, c)
                drawCircle(Ink.line.copy(alpha = alpha), r * 0.42f, c, style = Stroke(lw * 0.7f))
            }
            4 -> {
                for (k in 0 until 5) {
                    val ang = k * 2f * PI.toFloat() / 5f
                    drawCircle(T.Berry.lighten(0.3f).copy(alpha = alpha), r * 0.3f, Offset(c.x + kotlin.math.cos(ang) * r * 0.42f, c.y + sin(ang) * r * 0.42f))
                }
                drawCircle(T.Sun.copy(alpha = alpha), r * 0.26f, c)
            }
            else -> {
                // A laughing face.
                drawCircle(T.Sun.copy(alpha = alpha), r * 0.8f, c)
                drawCircle(Ink.line.copy(alpha = alpha), r * 0.8f, c, style = Stroke(lw * 0.7f))
                drawArc(Ink.line.copy(alpha = alpha), 0f, 180f, true, Offset(c.x - r * 0.4f, c.y - r * 0.05f), Size(r * 0.8f, r * 0.55f))
                drawCircle(Ink.line.copy(alpha = alpha), r * 0.08f, Offset(c.x - r * 0.28f, c.y - r * 0.28f))
                drawCircle(Ink.line.copy(alpha = alpha), r * 0.08f, Offset(c.x + r * 0.28f, c.y - r * 0.28f))
            }
        }
    }

    private fun DrawScope.drawGlimt(s: Secret, lw: Float) {
        val born = appeared[s.id]
        val grow = if (born != null && time - born < 0.6f) ((time - born) / 0.6f).let { 1f - (1f - it).pow(3) } * 1.1f else 1f
        val bob = sin(time * 2.2f + s.x * 3f) * 0.006f
        val c = Offset(sx(s.x), sy(s.y + bob))
        val r = 0.022f * u * grow * (1f + sin(time * 5f) * 0.06f)
        glow(listOf(T.SunTop.copy(alpha = 0.55f), Color.Transparent), c, r * 2.6f)
        val star = starPath(c, r, r * 0.46f, sin(time * 1.3f) * 10f)
        drawPath(star, T.Sun)
        drawPath(star, Ink.line, style = Stroke(lw))
        twinkle(Offset(c.x - r * 0.3f, c.y - r * 0.35f), r * 0.35f, Color.White, 0.9f)
        val tw = (sin(time * 3.1f + s.y * 9f) + 1f) / 2f
        twinkle(Offset(c.x + r * 1.4f, c.y - r * 1.2f), r * 0.5f * tw, Color.White, tw)
    }

    private fun DrawScope.drawNight(lw: Float) {
        if (night <= 0.01f || place == PlaceId.SPACE) return
        val dark = (if (place.outdoor) 0.3f else 0.5f) * night
        val lights = lightSources()
        // Drawn inside the world transform, so the band above the scene starts at -top.
        drawContext.canvas.saveLayer(Rect(0f, -top, size.width, size.height), Paint())
        drawRect(Color(0xFF0E0B33).copy(alpha = dark), Offset(0f, -top), Size(size.width, size.height + top))
        for ((c, r, _) in lights) {
            glow(listOf(Color.Black, Color.Black.copy(alpha = 0.6f), Color.Transparent), c, r, blendMode = BlendMode.DstOut)
        }
        drawContext.canvas.restore()
        for ((c, r, color) in lights) {
            glow(listOf(color.copy(alpha = 0.22f * night), Color.Transparent), c, r * 0.8f, blendMode = BlendMode.Plus)
        }
        if (lw < 0f) Unit
    }

    private fun lightSources(): List<Triple<Offset, Float, Color>> {
        val out = ArrayList<Triple<Offset, Float, Color>>()
        for (f in world.fixturesIn(place)) {
            val l = f.spec.light ?: continue
            val lit = when (f.type) {
                FixtureType.CAULDRON, FixtureType.CRYSTAL_BALL, FixtureType.ORRERY, FixtureType.PORTHOLE, FixtureType.CONTROL_PANEL -> true
                FixtureType.TV -> f.mode != 0
                else -> f.on
            }
            if (!lit) continue
            val c = Offset(sx(f.x + f.shiftX + (l.left + l.right) / 2), sy(f.y + f.shiftY + (l.top + l.bottom) / 2))
            val color = when (f.type) {
                FixtureType.CAULDRON -> Color(0xFF7CFFB2)
                FixtureType.TV, FixtureType.CRYSTAL_BALL, FixtureType.PORTHOLE -> Color(0xFF7CCBFF)
                else -> Color(0xFFFFC96B)
            }
            out += Triple(c, l.width / 2 * u, color)
        }
        for (b in world.bodiesIn(place)) {
            if (b !is Thing || !b.type.glows || hidden(b)) continue
            out += Triple(Offset(sx(b.x), sy(b.y - b.h / 2)), 0.13f * u, Color(0xFFFFE58A))
        }
        return out
    }

    private fun DrawScope.drawWeather(weather: Weather) {
        if (!place.outdoor || !motion) return
        if (weather == Weather.RAIN) {
            val c = Color(0xFFB9D9FF).copy(alpha = 0.65f)
            for (i in 0 until drops.size / 2) {
                val x = drops[i * 2] * size.width
                val y = drops[i * 2 + 1] * size.height
                drawLine(c, Offset(x, y), Offset(x - dp(3f), y + dp(14f)), strokeWidth = dp(1.6f), cap = StrokeCap.Round)
            }
        }
        if (weather == Weather.SNOW) {
            for (i in 0 until flakes.size / 2) {
                val x = flakes[i * 2] * size.width
                val y = flakes[i * 2 + 1] * size.height
                drawCircle(Color.White.copy(alpha = 0.9f), dp(1.8f + (i % 4) * 0.8f), Offset(x, y))
            }
        }
    }

    private fun DrawScope.drawFlights() {
        for (f in flights) {
            val t = f.t
            val e = 1f - (1f - t).pow(2)
            val to = counterTarget
            val x = f.from.x + (to.x - f.from.x) * e
            val y = f.from.y + (to.y - f.from.y) * e - sin(t * PI.toFloat()) * dp(120f)
            val r = dp(16f) * (1f - t * 0.4f)
            val star = starPath(Offset(x, y), r, r * 0.46f, t * 360f)
            drawPath(star, T.Sun)
            drawPath(star, Ink.line, style = Stroke(dp(2f)))
        }
    }

    private fun DrawScope.drawBag(text: TextMeasurer, pen: Pen) {
        val c = bagCenter
        val r = bagRadius
        val bag = world.bag()
        if (bagOpen) {
            val tray = trayRect()
            drawRoundRect(T.CreamLine, Offset(tray.left, tray.top + dp(5f)), tray.size, CornerRadius(dp(26f)))
            drawRoundRect(T.Cream, tray.topLeft, tray.size, CornerRadius(dp(26f)))
            drawRoundRect(Ink.line, tray.topLeft, tray.size, CornerRadius(dp(26f)), style = Stroke(dp(2.2f)))
            if (bag.isEmpty()) {
                drawRoundRect(T.CreamLine, Offset(tray.left + dp(12f), tray.top + dp(12f)), Size(tray.width - dp(24f), tray.height - dp(24f)), CornerRadius(dp(18f)), style = Stroke(dp(2.5f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dp(10f), dp(8f)))))
            }
            val slot = traySlot()
            bag.forEachIndexed { i, b ->
                val cx = tray.left + dp(6f) + slot * (i + 0.5f)
                val base = tray.bottom - dp(14f)
                val box = min(slot - dp(10f), dp(66f))
                translate(cx, base) {
                    when (b) {
                        is Thing -> {
                            val k = box / max(b.w, b.h) / u
                            drawThing(b.type, b.variant, b.used, b.w * u * k, b.h * u * k, pen)
                        }
                        is Person -> drawPerson(b.species, b.look, Pose.STAND, b.anim, box * 1.05f, pen, false, b.id * 0.37f)
                    }
                }
            }
        }
        // The bag button, drawn like the other round buttons.
        val wobble = if (bag.isNotEmpty() && motion) sin(time * 3f) * 3f else 0f
        drawCircle(T.SunDeep, r, Offset(c.x, c.y + dp(5f)))
        drawCircle(Brush.verticalGradient(listOf(T.SunTop, T.Sun), c.y - r, c.y + r), r, c)
        drawCircle(Ink.line, r, c, style = Stroke(dp(2.2f)))
        rotate(wobble, c) {
            inset(c.x - r * 0.62f, c.y - r * 0.66f, size.width - (c.x + r * 0.62f), size.height - (c.y + r * 0.58f)) { Icons.Bag(this) }
        }
        if (bag.isNotEmpty()) {
            val badge = Offset(c.x + r * 0.72f, c.y - r * 0.72f)
            drawCircle(T.Berry, dp(13f), badge)
            drawCircle(Ink.line, dp(13f), badge, style = Stroke(dp(2f)))
            val layout = text.measure(bag.size.toString(), TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black))
            drawText(layout, topLeft = Offset(badge.x - layout.size.width / 2f, badge.y - layout.size.height / 2f))
        }
    }

    private companion object {
        /** Piano keys on a friendly pentatonic scale, in semitones from middle C an octave up. */
        val PENTATONIC = intArrayOf(-12, -10, -8, -5, -3, 0, 2, 4, 7, 9)

        /** Furniture whose picture follows moving parts (angles, wheels, rides): always drawn live. */
        val LIVE_FIXTURES = setOf(
            FixtureType.ORRERY, FixtureType.UMBRELLA, FixtureType.PINE_TREE, FixtureType.KELP, FixtureType.FERRIS_WHEEL,
            FixtureType.CAROUSEL, FixtureType.DISCO_BALL, FixtureType.SUBMARINE, FixtureType.BUMPER_CAR, FixtureType.TRACTOR,
            FixtureType.SLED_HILL, FixtureType.SKI_JUMP, FixtureType.CART, FixtureType.BOAT, FixtureType.OWL_TREE,
        )

        /** Furniture that animates from its own timers while it is on. */
        val LIVE_WHEN_ON = setOf(
            FixtureType.OVEN, FixtureType.DRYER_HOOD, FixtureType.FISHING_SPOT, FixtureType.BLENDER, FixtureType.CAULDRON,
            FixtureType.ROCKET_SHIP, FixtureType.XRAY, FixtureType.CHECKOUT, FixtureType.ROBOT_VACUUM,
        )

        /** How often art that moves by itself is redrawn into its picture. */
        const val SLOW_HZ = 8f

        /** Seconds a finger must rest on furniture before it can be moved. */
        const val LONG_PRESS = 0.45f

        /** How many pictures [drawChatIcon] knows. */
        const val CHAT_ICONS = 6

        /** Scene units that always fit across the screen; a 16:10 tablet zooms out to show them. */
        const val MIN_VIEW = 2.05f
    }
}
