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
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Fx
import app.trollfoss.domain.Give
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

    /** Where the glimt counter sits on screen, so found stars can fly to it. Set by the HUD. */
    var counterTarget = Offset(0f, 0f)

    var bagOpen by mutableStateOf(false)
    var bagCount by mutableIntStateOf(0)
        private set
    var found by mutableIntStateOf(world.found.size)
        private set

    private val appeared = HashMap<String, Float>()
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
    }

    private class Grab(val target: Target, val down: Offset, val downTime: Long) {
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

        // A thing carried to the screen edge takes the camera with it.
        if (grabs.values.any { it.moved && heldBody(it) != null }) {
            val edge = grabs.values.filter { it.moved && heldBody(it) != null }.map { it.finger.x }
            for (x in edge) {
                if (x < widthPx * 0.08f) cam -= 1.3f * dt
                if (x > widthPx * 0.92f) cam += 1.3f * dt
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
            drawCircle(Brush.radialGradient(listOf(T.SunTop.copy(alpha = 0.5f), Color.Transparent), c, r * 1.6f), r * 1.6f, c)
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
        val radio = world.fixturesIn(place).firstOrNull { it.type == FixtureType.RADIO && it.on }
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
            val target = held.firstOrNull { it !== p }?.let { Offset(it.x, it.y - it.h / 2) } ?: finger
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
            a.dance = if (radio != null && a.pose == Pose.STAND && p.resting && abs(p.x - radio.x) < 1.6f) {
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
                    if (random.nextBoolean()) a.wave = 1.2f else a.talk = 0.9f
                    if (a.talk > 0f && time - lastBabble > 9f) {
                        lastBabble = time
                        voice(p, Sfx.BABBLE, 0.25f)
                    }
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
        val grab = Grab(pick(at), at, uptime)
        grab.tracker.addPosition(uptime, at)
        val body = heldBody(grab)
        if (body != null) {
            val p = toScene(at)
            grab.offX = body.x - p.x
            grab.offY = body.y - p.y
            if (grab.target is Target.FromBag) lift(grab, body)
        }
        if (grab.target is Target.Pan) camV = 0f
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
            }
            return
        }
        when {
            body != null && body.held -> drop(g, body, vx, vy)
            g.target is Target.Pan -> camV = -vx
        }
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
                if (t.type.edible) add(Part.MOUTH to 0.075f)
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
                if (a.taps >= 6) {
                    a.face = Face.DIZZY
                    a.faceTime = 1.8f
                    a.taps = 0
                    voice(b, Sfx.OOF, 0.8f)
                } else {
                    a.face = if (random.nextBoolean()) Face.LAUGH else Face.GRIN
                    a.faceTime = 1.1f
                    voice(b, Sfx.GIGGLE, 0.8f)
                    if (!b.species.pet) a.wave = 1.1f
                }
                if (b.species == Species.DRAGON) particles.burst(PKind.SPARK, b.x + 0.02f, b.y - b.h * 0.45f, 8, 0.5f, 0.01f, Color(0xFFFFB02E))
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
        // A glimt?
        for (s in sim.visibleSecrets(place)) {
            if (hypot(p.x - s.x, p.y - s.y) < max(0.05f, minTouch)) {
                collect(s)
                return
            }
        }
        // A fixture? Front-most first, wall fixtures last.
        val fixtures = world.fixturesIn(place).sortedWith(compareBy<Fixture> { if (it.spec.wall) 0 else 1 }.thenBy { it.id }).reversed()
        for (f in fixtures) {
            val fx = f.x + f.shiftX
            val fy = f.y + f.shiftY
            val pad = 0.012f
            if (p.x in (fx - f.spec.w / 2 - pad)..(fx + f.spec.w / 2 + pad) && p.y in (fy - f.spec.h - pad)..(fy + pad)) {
                sim.tap(place, f, p.x - fx, p.y - fy)
                host.changed()
                return
            }
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
            Fx.SQUEAK -> s(Sfx.SQUEAK, 0.8f)
            Fx.STRUM -> {
                val root = listOf(0, 5, 7, 3)[param % 4]
                for ((i, n) in listOf(0, 4, 7).withIndex()) pending += (time + i * 0.05f) to { s(Sfx.NOTE, 0.5f, 2f.pow((root + n - 12) / 12f)) }
                particles.add(Particle(PKind.NOTE, x, y - 0.05f, 0.05f, -0.12f, 1.5f, 0.012f, T.Grape))
            }
            Fx.DRUM -> s(Sfx.DRUM, 0.8f)
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

    private fun voice(p: Person, sfx: Sfx, volume: Float) {
        val animal = when (p.species) {
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
            Species.FOLK -> null
        }
        val rate = when (p.species) {
            Species.ELK -> 1.25f
            Species.PUFFIN -> 0.75f
            Species.FOLK -> p.voice
            else -> p.voice.coerceIn(0.85f, 1.2f)
        }
        host.sfx(animal ?: sfx, volume, rate)
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
    private fun fixtureKey(f: Fixture): Float = if (f.spec.wall) -10f + f.id * 0.00001f else f.depth

    private fun bodyKey(b: Body): Float {
        if (b.mode == Mode.SEATED) world.fixtures[b.holder]?.let { return fixtureKey(it) + 0.0006f }
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
        drawFlights()
        drawBag(text, pen)
        if (flash > 0f) drawRect(Color.White.copy(alpha = flash * 0.85f))
        drawNameTagsLate(text)
    }

    private var lateText: List<Body> = emptyList()

    private fun DrawScope.drawNameTagsLate(text: TextMeasurer) = drawNameTags(lateText, text)

    private fun DrawScope.drawVignette() {
        val r = max(size.width, size.height) * 0.72f
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Transparent, Ink.line.copy(alpha = 0.2f)), center, r))
    }

    private fun DrawScope.drawWorld(pen: Pen, lw: Float) {
        drawPlaceBack(place, cam, u, pen)

        // One list for furniture, glimt and bodies, sorted back to front.
        layers.clear()
        for (f in world.fixturesIn(place)) {
            val fx = f.x + f.shiftX
            if (fx + f.spec.w < cam - 0.1f || fx - f.spec.w > cam + viewport + 0.1f) continue
            val k = fixtureKey(f)
            layers += Layer(k, 0, f)
            if (f.spec.front) layers += Layer(k + 0.0008f, 1, f)
        }
        // Glimt sit just behind what lies on the same furniture, so a pillow can hide one.
        for (s in sim.visibleSecrets(place)) layers += Layer(glimtKey(s), 2, s)
        val list = drawList()
        for (b in list) if (!b.held) layers += Layer(bodyKey(b), 3, b)
        layers.sortWith(layerOrder)
        for (l in layers) {
            when (l.kind) {
                0 -> {
                    val f = l.ref as Fixture
                    translate(sx(f.x + f.shiftX), sy(f.y + f.shiftY)) {
                        val bounce = if (motion) 1f + f.anim * 0.05f else 1f
                        scale(bounce, 2f - bounce, pivot = Offset.Zero) {
                            drawFixtureBack(f, u, pen, if (f.spec.machine == app.trollfoss.domain.Machine.BLENDER || f.spec.machine == app.trollfoss.domain.Machine.CAULDRON || f.spec.machine == app.trollfoss.domain.Machine.BUILD) world.inMachine(f) else emptyList())
                        }
                    }
                }
                1 -> {
                    val f = l.ref as Fixture
                    translate(sx(f.x + f.shiftX), sy(f.y + f.shiftY)) { drawFixtureFront(f, u, pen) }
                }
                2 -> drawGlimt(l.ref as Secret, lw)
                else -> {
                    val b = l.ref as Body
                    drawShadow(b)
                    drawBody(b, pen)
                }
            }
        }
        drawPlaceFront(place, cam, u, pen)

        drawPreviews(lw)
        for (b in list) if (b.held) {
            drawShadow(b)
            drawBody(b, pen)
        }

        particles.draw(this, u, cam, lw)
        drawNight(lw)
        lateText = list
    }

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
                val bob = if (a.pose == Pose.FLOAT) sin(time * 1.6f + b.id) * 0.008f else 0f
                translate(sx(b.x), sy(b.y - a.hop + bob)) {
                    rotate(a.tilt + (if (a.pose == Pose.FLOAT) sin(time + b.id) * 6f else 0f), pivot = Offset(0f, -b.h * u * 0.5f)) {
                        scale((1f + sq * 0.22f) * pop, (1f - sq * 0.22f) * pop, pivot = Offset.Zero) {
                            val carried = world.carried(b)
                            val holding = carried.any { it.slot == Slot.HAND.ordinal }
                            drawPerson(b.species, b.look, a.pose, a, b.h * u, pen, holding, seed = b.id * 0.37f)
                            for (t in carried.sortedBy { it.slot }) drawCarried(b, t, pen)
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
                            drawThing(b.type, b.variant, b.used, b.w * u, b.h * u, pen, b.cook)
                        }
                    }
                }
            }
        }
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

    private fun DrawScope.drawGlimt(s: Secret, lw: Float) {
        val born = appeared[s.id]
        val grow = if (born != null && time - born < 0.6f) ((time - born) / 0.6f).let { 1f - (1f - it).pow(3) } * 1.1f else 1f
        val bob = sin(time * 2.2f + s.x * 3f) * 0.006f
        val c = Offset(sx(s.x), sy(s.y + bob))
        val r = 0.022f * u * grow * (1f + sin(time * 5f) * 0.06f)
        drawCircle(Brush.radialGradient(listOf(T.SunTop.copy(alpha = 0.55f), Color.Transparent), c, r * 2.6f), r * 2.6f, c)
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
            drawCircle(Brush.radialGradient(listOf(Color.Black, Color.Black.copy(alpha = 0.6f), Color.Transparent), c, r), r, c, blendMode = BlendMode.DstOut)
        }
        drawContext.canvas.restore()
        for ((c, r, color) in lights) {
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.22f * night), Color.Transparent), c, r * 0.8f), r * 0.8f, c, blendMode = BlendMode.Plus)
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

        /** Scene units that always fit across the screen; a 16:10 tablet zooms out to show them. */
        const val MIN_VIEW = 2.05f
    }
}
