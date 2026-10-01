package app.trollfoss.domain

import kotlin.math.sin

/**
 * The secret tunnel. The door at the far end shows how many of the five golden keys are found (a little
 * lock for each) and opens when all five are. Then a tap on the door or the mine cart sets off a ride: up to
 * two figures climb in, the cart rattles, rolls along its rails, curves into the dark tunnel mouth and
 * carries everybody to Trollhola. Until all five keys are found, a knock only makes the door wiggle (three
 * knocks make a glimt appear).
 */
internal class CellarTunnel(private val c: CellarCtx) : CellarPart {
    private val passage: Passage by lazy { House.passages.first { it.id == "cellar-tunnel" } }

    /** Which of the ride's one-time events have happened already. */
    private var bumped = false
    private var entered = false

    private fun unlocked(): Boolean = House.usable(c.world, passage)

    override fun tap(f: Fixture, dx: Float, dy: Float): Boolean = when (f.type) {
        FixtureType.SECRET_DOOR -> door(f)
        FixtureType.CE_MINE_CART -> {
            val door = c.fixture(CellarIx.TUNNEL_DOOR)
            if (door != null && unlocked()) {
                start(door)
            } else {
                // The rusty cart will not move yet; its bell still rings.
                f.anim = 1f
                c.fx(CellarCode.RIDE_BELL, f.x, f.y - 0.12f, f, arg = 0)
            }
            true
        }
        else -> false
    }

    /** The door: a locked one only shakes (the usual passage rules show that); an open one starts the ride. */
    private fun door(f: Fixture): Boolean {
        if (!unlocked()) {
            c.fx(CellarCode.KNOCK, f.x, f.y - 0.2f, f, arg = f.taps)
            if (f.taps >= 3) c.sim.unlock("cellar_tunnel")
            return false
        }
        return start(f)
    }

    private fun start(door: Fixture): Boolean {
        val cart = c.fixture(CellarIx.MINE_CART) ?: return false
        if (cart.timer > 0f) return true
        if (recruit(cart) == 0) {
            // Nobody to take along: the bell rings, and the cart waits for someone to climb in.
            cart.anim = 1f
            c.fx(CellarCode.RIDE_BELL, cart.x, cart.y - 0.12f, cart, arg = 1)
            return true
        }
        cart.timer = 0.001f
        cart.angle = 0f
        cart.angleV = 0f
        door.mode = 1
        bumped = false
        entered = false
        c.fx(CellarCode.RIDE_START, cart.x, cart.y - 0.1f, cart)
        return true
    }

    /** Fills the cart's seats with the nearest free figures. Returns how many sit in it now. */
    private fun recruit(cart: Fixture): Int {
        var seated = 0
        for (spot in cart.spec.spots.indices) {
            if (c.world.seatedAt(cart, spot) != null) {
                seated++
                continue
            }
            val p = c.freeFigures(cart.x, RECRUIT_REACH).firstOrNull() ?: continue
            if (c.sim.seat(p, cart, spot)) {
                p.anim.hopV = 1.3f
                seated++
            }
        }
        return seated
    }

    override fun step(f: Fixture, dt: Float) {
        when (f.type) {
            FixtureType.SECRET_DOOR -> {
                // The door shows the keys: a lock for each one found, all lit when the tunnel is open.
                f.count = HouseKeys.found(c.world)
                f.on = unlocked()
            }
            FixtureType.CE_MINE_CART -> ride(f, dt)
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ the ride

    private fun ride(cart: Fixture, dt: Float) {
        if (cart.timer <= 0f) return
        cart.timer += dt
        val t = cart.timer
        if (t < PREP_SECONDS) {
            // The cart rattles and shakes before it sets off.
            cart.bob = sin(t * 34f) * 0.003f
            return
        }
        if (t < PREP_SECONDS + GO_SECONDS) {
            val p = (t - PREP_SECONDS) / GO_SECONDS
            val u = p * p
            place(cart, u)
            cart.angle += dt * (3f + 40f * u)
            cart.angleV += dt * (1.6f + 9f * u)
            if (cart.angleV >= 1f) {
                cart.angleV -= 1f
                c.fx(CellarCode.RIDE_CLACK, cart.x + cart.shiftX, cart.y + cart.shiftY - 0.02f, cart, arg = (u * 3.99f).toInt())
            }
            // A bump in the track at the bend, and the dark of the tunnel mouth.
            if (!bumped && u > CORNER) {
                bumped = true
                c.listener.onFx(Fx.BUMP, cart.x + cart.shiftX, cart.y + cart.shiftY - 0.1f, cart, param = -1)
            }
            if (!entered && u > 0.8f) {
                entered = true
                c.fx(CellarCode.RIDE_ENTER, cart.x + cart.shiftX, cart.y + cart.shiftY - 0.1f, cart)
            }
            return
        }
        arrive(cart)
    }

    /** Puts the cart at [u] (0 to 1) of its way: along the front of the room, then back into the tunnel mouth. */
    private fun place(cart: Fixture, u: Float) {
        if (u < CORNER) {
            cart.shiftX = STRAIGHT * (u / CORNER)
            cart.shiftY = 0f
        } else {
            val q = (u - CORNER) / (1f - CORNER)
            cart.shiftX = STRAIGHT + BACK_X * q
            cart.shiftY = -BACK_Y * q
        }
        // Further up the screen is further back: the cart goes behind the tunnel door's frame.
        cart.depth = cart.y + cart.shiftY
    }

    /** The cart reaches the tunnel: everybody in it, and near the door, goes through to Trollhola. */
    private fun arrive(cart: Fixture) {
        val door = c.fixture(CellarIx.TUNNEL_DOOR)
        for (spot in cart.spec.spots.indices) {
            val p = c.world.seatedAt(cart, spot) ?: continue
            p.mode = Mode.FREE
            p.holder = -1
            p.x = door?.x ?: cart.x
            p.y = door?.y ?: cart.y
            p.ground = p.y
            p.vx = 0f
            p.vy = 0f
            p.resting = true
            p.restOwner = -1
        }
        reset(cart, door)
        c.sim.unlock("cellar_tunnel")
        if (door != null) c.sim.house.usePassage(passage, door)
    }

    private fun reset(cart: Fixture, door: Fixture?) {
        cart.shiftX = 0f
        cart.shiftY = 0f
        cart.depth = cart.y
        cart.timer = 0f
        cart.angle = 0f
        cart.angleV = 0f
        cart.bob = 0f
        door?.mode = 0
        bumped = false
        entered = false
    }

    /** The child was away in the middle of a ride: the cart is back at its place and the riders step out. */
    fun cameBack() {
        val cart = c.fixture(CellarIx.MINE_CART) ?: return
        if (cart.timer <= 0f) return
        for (spot in cart.spec.spots.indices) {
            val p = c.world.seatedAt(cart, spot) ?: continue
            p.mode = Mode.FREE
            p.holder = -1
            p.x = cart.x - 0.25f
            p.y = cart.y
            p.ground = cart.y
            p.vx = 0f
            p.vy = 0f
            p.resting = true
            p.restOwner = -1
        }
        reset(cart, c.fixture(CellarIx.TUNNEL_DOOR))
    }

    override fun seatPoint(f: Fixture, spot: Int): FloatArray? {
        if (f.type != FixtureType.CE_MINE_CART) return null
        val s = f.spec.spots[spot]
        return floatArrayOf(f.x + s.dx + f.shiftX, f.y + s.dy + f.shiftY + f.bob)
    }

    companion object {
        const val PREP_SECONDS = 0.9f
        const val GO_SECONDS = 2.3f
        const val RECRUIT_REACH = 2.6f

        /** How far along the ride the cart turns into the tunnel, how far it runs straight before, and how far it goes into the tunnel. */
        const val CORNER = 0.34f
        const val STRAIGHT = 0.10f
        const val BACK_X = 0.16f
        const val BACK_Y = 0.115f
    }
}
