package app.trollfoss.domain

import kotlin.math.*

/** Saved separately from the dollhouse coordinates: travelling never changes a person's identity. */
class HighlandsState {
    var x = 0f
    var z = 18f
    var cave = false
    var yaw = 0f
    var leader = -1
    var light = false
    var treasure = false
    var treasureSlot = 0
    var shake = true
    var taken = 0
    val song = mutableListOf<EchoNote>()
    var pot = BerryPot.EMPTY
    var sharedJuice = false
    var mothFriend = false
    var springAwake = false
    var lastBottle = BergItem.NONE
    var held = BergItem.NONE
    val bag = linkedSetOf<BergItem>()

    fun repair() {
        yaw = if (yaw.isFinite()) yaw % (2f * PI.toFloat()) else 0f
        if (!x.isFinite() || !z.isFinite() || !Highlands.walkable(x, z, cave)) {
            x = 0f; z = if (cave) 10f else 18f
        }
        bag.remove(BergItem.NONE)
        if (held !in bag) held = BergItem.NONE
        taken = taken and 3
        treasureSlot=treasureSlot.coerceIn(0,Highlands.treasures.lastIndex)
    }
}

enum class BergItem { NONE, DIAMOND, GOLD, PEPSI, URGE, CAKE, BERRIES, BERRY_CUP }
enum class BergAction { ENTER, EXIT, DIAMOND, GOLD, SOCKET, TROLL, PEPSI, URGE, CAKE, TREASURE, LOOKOUT, HARE, SKIP, CRYSTAL, BUTTERFLY, BERRIES, KETTLE, ECHO, MOUSE, SPRING }
enum class BergEvent { NONE, PICK, LIGHT, GULP, BURP, FART, CAKE, TRAVEL, WAVE, LOOKOUT, NEED_DIAMOND, NEED_GOLD, HARE, GOLD_ELSEWHERE, SKIP, SPLASH, NOTE, WASH, BUTTERFLY, BERRIES, FILL, STIR, POUR, OFFER, TOAST, SIP, NEED_BERRIES, BASKET_FULL, CUP_FULL, LISTEN, STONE_NOTE, REPLY, FLUTE_NOTE, FRIEND, MOUSE, SPRING }
data class BergPoint(val x: Float, val z: Float)
data class BergSpot(val action: BergAction, val x: Float, val z: Float, val cave: Boolean, val radius: Float = 2.5f)

/** One geometry contract for drawing, walking, picking, collision and route finding. Metres, Y up. */
object Highlands {
    const val WATER = 0.85f
    val hares=listOf(BergPoint(-15f,6f),BergPoint(17f,-5f))
    val treasures=listOf(BergPoint(-9f,-10f),BergPoint(-13f,-23f))
    val lookout=BergPoint(-18f,-18f)
    val goldPan=BergPoint(-14.8f,3f)
    val butterfly=BergPoint(-3f,17.5f)
    const val BUTTERFLY_SECONDS=7.2f
    const val PAN_SECONDS=3.2f
    val crystals=listOf(BergPoint(-17.4f,-16.8f),BergPoint(-15.7f,-17.8f),BergPoint(-13.9f,-17.2f))
    val crystalPitches=listOf(.84f,1f,1.26f)
    private val sideArchXs=floatArrayOf(-19f,-9f,7f,19f)
    const val SKIP_SECONDS=3.6f
    val skipLandings=listOf(BergPoint(2f,5f),BergPoint(0f,4f),BergPoint(-2f,4f))
    val skipTimes=listOf(.9f,1.55f,2.05f)
    val spots = listOf(
        BergSpot(BergAction.ENTER, 13f, -11f, false, 3.2f),
        BergSpot(BergAction.DIAMOND, -12f, 10f, false),
        BergSpot(BergAction.GOLD, goldPan.x, goldPan.z, false),
        BergSpot(BergAction.SOCKET, -9f, 8f, false),
        BergSpot(BergAction.LOOKOUT, lookout.x, lookout.z, false, 3f),
        BergSpot(BergAction.HARE, -15f, 6f, false, 2f),
        BergSpot(BergAction.HARE, 17f, -5f, false, 2f),
        BergSpot(BergAction.SKIP, 5f, 7f, false, 2.4f),
        BergSpot(BergAction.BUTTERFLY, butterfly.x, butterfly.z, false, 2.4f),
        BergSpot(BergAction.EXIT, 0f, 12f, true, 2.5f),
        BergSpot(BergAction.TROLL, -3f, -1f, true, 3.3f),
        BergSpot(BergAction.PEPSI, 6f, -5f, true),
        BergSpot(BergAction.URGE, 9f, -5f, true),
        BergSpot(BergAction.CAKE, -8f, -.2f, true),
        BergSpot(BergAction.TREASURE, -9f, -10f, true),
        BergSpot(BergAction.PEPSI, 11f, -18f, true),
        BergSpot(BergAction.URGE, 15f, -18f, true),
        BergSpot(BergAction.TREASURE, -13f, -23f, true),
        BergKitchen.picking, BergKitchen.hearth, BergEcho.outside, BergEcho.inside, BergSpring.bank,
        BergSpot(BergAction.MOUSE, CaveMouse.calling.x, CaveMouse.calling.z, true, 2.15f),
    ) + crystals.map {BergSpot(BergAction.CRYSTAL,it.x,it.z+2f,true,1.7f)}

    private data class Solid(val left:Float,val right:Float,val back:Float,val front:Float,val top:Float,val bottom:Float=0f) {
        fun contains(x:Float,z:Float)=x in left..right && z in back..front
        fun cameraHit(x:Float,y:Float,z:Float,dx:Float,dy:Float,dz:Float,limit:Float):Float? {
            var near=0f;var far=limit
            fun slab(p:Float,d:Float,lo:Float,hi:Float):Boolean {
                if(abs(d)<.0001f) return p in lo..hi
                val a=(lo-p)/d;val b=(hi-p)/d
                near=max(near,min(a,b));far=min(far,max(a,b))
                return near<=far
            }
            return if(slab(x,dx,left-.2f,right+.2f) && slab(z,dz,back-.2f,front+.2f) &&
                slab(y,dy,bottom-.2f,top+.25f)) near else null
        }
    }
    // Physical footprints of the authored furniture, with a small clearance for the children.
    // Interaction spots sit beside the objects, never inside their collision footprint.
    private val caveSolids=listOf(
        Solid(-2.53f,-1.02f,-3.24f,-2.47f,1.04f), // Rumle's serving board
        Solid(-10f,-6f,-3.3f,-.7f,1.65f), // cake table and cake
        Solid(-11.15f,-5.85f,-6.9f,-5.1f,4.2f), Solid(5.85f,11.15f,-6.9f,-5.1f,7.42f,3.2f), // front shelves
        Solid(-19.5f,-14.5f,-7.2f,-4.6f,3.8f), // hearth and mugs
        Solid(5.75f,11.25f,-26f,-24f,9.6f,4.4f), Solid(11.75f,17.25f,-26f,-24f,9.6f,4.4f), // great larder
        Solid(-14.8f,-11.2f,-26.2f,-23.9f,2.7f), // rear treasure chest
        Solid(9.95f,12.05f,-20.9f,-19.1f,6.7f,4.4f), // picking crates and their bottles
        Solid(13.95f,16.05f,-20.9f,-19.1f,6.7f,4.4f),
    )

    // The front shell is rippled, so its closest face is before the old z=15.8
    // camera bound. Preserve the wide doorway while keeping the lens indoors.
    private val caveCameraWalls=listOf(
        Solid(-23f,-5.8f,14.6f,17f,19f),
        Solid(5.8f,23f,14.6f,17f,19f),
        Solid(-5.8f,5.8f,14.6f,17f,19f,7.3f),
        Solid(2.95f,3.5f,-28f,0f,4.2f), // stone supporting the raised gallery
    )
    private val caveCameraSolids=caveSolids+caveCameraWalls
    private val caveLamps=listOf(BergPoint(-10f,7f),BergPoint(7f,7f),BergPoint(-11f,-6f),
        BergPoint(10f,-10f),BergPoint(-9f,-15f),BergPoint(7f,-15f),BergPoint(-17f,-24f),
        BergPoint(17f,-24f),BergPoint(2f,-24f))

    /** Conservative clearance below the same vaulted roof authored in Blender. */
    fun caveCameraCeiling(x:Float,z:Float):Float {
        val sourceX=x/1.08f
        val sourceY=7f+(-z-7f)/1.11f
        return 12.2f+3f*cos(sourceX*PI.toFloat()/43f)+.3f*sin(sourceY*.5f)
    }

    fun height(x: Float, z: Float, cave: Boolean = false): Float {
        if (cave) return if (x > 3f) ((8f - z) * .23f).coerceIn(0f, 4.4f) else 0f
        val ridge = ((3f - z) / 24f).coerceIn(0f, 1f) * 6f
        val lake = ((x + 3f) / 10f).pow(2) + ((z - 1f) / 8f).pow(2)
        val island = exp(-((x + 3f).pow(2) + (z - 1f).pow(2)) * .11f) * 6.2f
        return 1.65f + ridge + .22f * sin(x * .44f) * cos(z * .3f) -
            (1f - lake).coerceAtLeast(0f) * 6.2f + island
    }

    fun bridge(x: Float, z: Float) = x in -13f..8f && abs(z - 1f) < .9f
    fun floor(x: Float, z: Float, cave: Boolean): Float =
        if (!cave && bridge(x, z)) max(2.55f, height(x, z)) else height(x, z, cave)

    fun walkable(x: Float, z: Float, cave: Boolean): Boolean {
        if (!x.isFinite() || !z.isFinite()) return false
        val bells=BergEcho.site(cave)
        if (abs(x-bells.x)<1.62f && abs(z-(bells.z-1.42f))<.48f) return false
        if (cave) {
            if (x !in -20f..20f || z !in -28f..14f || BergSpring.contains(x,z)) return false
            if (hypot(x + 3f, z + 5f) < 2.8f) return false // the troll's seat
            if (hypot(x + 8f, z + 15f) < 1.4f || hypot(x - 6f, z + 15f) < 1.4f) return false // vaulted hall pillars
            if (sideArchXs.any {hypot(x-it,z+15f)<.9f}) return false // side arch feet
            if(caveSolids.any {it.contains(x,z)}) return false
            if(crystals.any {hypot(x-it.x,z-it.z)<.7f}) return false
            return true
        }
        if (x !in -24f..24f || z !in -23f..24f) return false
        if (x > 2.5f && z < -16f) return false // solid granite spur behind the cave arch
        if (floor(x, z, false) < WATER + .16f) return false
        if (hypot(x + 18f, z - 11f) < 2.3f) return false // cabin
        if (hypot(x - 13f, z + 15f) < 2.7f) return false // Rumle outside the doorway
        return true
    }

    fun canStep(x: Float, z: Float, nx: Float, nz: Float, cave: Boolean): Boolean =
        walkable(nx, nz, cave) && abs(floor(nx, nz, cave) - floor(x, z, cave)) < .72f

    /** Stop the orbit in front of the entrance rock instead of looking through its back. */
    fun outsideCameraDistance(x:Float,z:Float,yaw:Float,pitch:Float,requested:Float):Float {
        val dx=sin(yaw)*cos(pitch);val dz=cos(yaw)*cos(pitch)
        var near=0f;var far=requested
        fun slab(p:Float,d:Float,lo:Float,hi:Float):Boolean {
            if(abs(d)<.0001f) return p in lo..hi
            val a=(lo-p)/d;val b=(hi-p)/d
            near=max(near,min(a,b));far=min(far,max(a,b))
            return near<=far
        }
        if(!slab(x,dx,2.5f,32f) || !slab(z,dz,-50f,-16f)) return requested
        val eyeHeight=floor(x,z,false)+1.1f+sin(pitch)*near
        return if(eyeHeight<18f) min(requested,near-.45f).coerceAtLeast(.8f) else requested
    }

    /** Keep the camera boom in front of walls, pillars and furniture, with lens clearance. */
    fun cameraDistance(x:Float,z:Float,yaw:Float,pitch:Float,requested:Float):Float {
        val dx=sin(yaw)*cos(pitch);val dz=cos(yaw)*cos(pitch)
        var distance=requested.coerceAtMost(11f)
        if(abs(dx)>.001f) distance=min(distance,((if(dx>0) 19.5f else -19.5f)-x)/dx)
        if(abs(dz)>.001f) distance=min(distance,((if(dz>0) 15.8f else -27.5f)-z)/dz)
        fun column(cx:Float,cz:Float,radius:Float,top:Float) {
            val ox=x-cx;val oz=z-cz;val a=dx*dx+dz*dz
            val b=ox*dx+oz*dz;val c=ox*ox+oz*oz-radius*radius
            val discriminant=b*b-a*c
            if(a<.0001f || discriminant<0f) return
            val hit=(-b-sqrt(discriminant))/a
            val cameraHeight=floor(x,z,true)+1.1f+sin(pitch)*hit
            if(hit>0f && cameraHeight<top+.4f) distance=min(distance,hit-.25f)
        }
        // Leave room for the visible frame, not only the centre ray. At the rear
        // chest the old narrow ray passed a pillar that filled half the screen.
        column(-8f,-15f,3.15f,9f);column(6f,-15f,3.15f,13.4f)
        column(-19f,-15f,1.1f,5.2f);column(-9f,-15f,1.1f,5.2f)
        column(7f,-15f,1.1f,9.6f);column(19f,-15f,1.1f,9.6f)
        column(-3f,-5f,2.9f,5.2f) // Rumle and his high-backed armchair
        // The lantern can fill the lens even though its slender post leaves room to walk.
        for(lamp in caveLamps) column(lamp.x,lamp.z,.76f,floor(lamp.x,lamp.z,true)+2.9f)
        val originY=floor(x,z,true)+1.1f
        for(solid in caveCameraSolids) {
            val hit=solid.cameraHit(x,originY,z,dx,sin(pitch),dz,distance)
            if(hit!=null) distance=min(distance,hit-.3f)
        }
        return distance.coerceAtLeast(.8f)
    }

    /** Fade only the person enclosing a close camera, including a tall follower behind the leader. */
    fun cameraPersonOpacity(eyeX:Float,eyeY:Float,eyeZ:Float,x:Float,ground:Float,z:Float,size:Float):Float {
        val scale=size.coerceAtLeast(.1f)
        val nearestY=eyeY.coerceIn(ground+.25f*scale,ground+1.9f*scale)
        val distance=sqrt((eyeX-x).pow(2)+(eyeY-nearestY).pow(2)+(eyeZ-z).pow(2))
        val t=((distance-.95f*scale)/(1.35f*scale)).coerceIn(0f,1f)
        return t*t*(3f-2f*t)
    }

    /** Bounded BFS only on a destination tap, never every frame. Diagonals cannot cut lake corners. */
    fun route(from: BergPoint, to: BergPoint, cave: Boolean): List<BergPoint> {
        if (!walkable(to.x, to.z, cave)) return emptyList()
        val extent = if(cave) 30 else 24
        val width = extent * 2 + 1
        fun index(x: Int, z: Int) = (z + extent) * width + x + extent
        val sx = from.x.roundToInt().coerceIn(-extent, extent)
        val sz = from.z.roundToInt().coerceIn(-extent, extent)
        val tx = to.x.roundToInt().coerceIn(-extent, extent)
        val tz = to.z.roundToInt().coerceIn(-extent, extent)
        val start = index(sx, sz); val goal = index(tx, tz)
        val parent = IntArray(width * width) { -1 }
        val queue = IntArray(parent.size)
        var head = 0; var tail = 0
        queue[tail++] = start; parent[start] = start
        while (head < tail && parent[goal] == -1) {
            val current = queue[head++]
            val x = current % width - extent; val z = current / width - extent
            for (dz in -1..1) for (dx in -1..1) {
                if (dx == 0 && dz == 0) continue
                val nx = x + dx; val nz = z + dz
                if (nx !in -extent..extent || nz !in -extent..extent) continue
                val next = index(nx, nz)
                if (parent[next] != -1 || !canStep(x.toFloat(), z.toFloat(), nx.toFloat(), nz.toFloat(), cave)) continue
                if (dx != 0 && dz != 0 && (!walkable(nx.toFloat(), z.toFloat(), cave) || !walkable(x.toFloat(), nz.toFloat(), cave))) continue
                parent[next] = current; queue[tail++] = next
            }
        }
        if (parent[goal] == -1) return emptyList()
        val path = ArrayList<BergPoint>()
        var at = goal
        while (at != start) {
            path.add(BergPoint((at % width - extent).toFloat(), (at / width - extent).toFloat()))
            at = parent[at]
        }
        path.reverse()
        path.add(to)
        return path
    }
}

/** UI-thread simulation. Rendering receives snapshots and never touches this mutable save state. */
class HighlandsWalk(val state: HighlandsState) {
    var side = 0f
    var forward = 0f
    var lookSide = 0f
    var lookUp = 0f
    var facing = PI.toFloat()
    var moving = false
        private set
    var time = 0f
        private set
    val echo = EchoPlay(state)
    private var friendLeader = -1
    var friendHand = false
        private set
    var friendReach = 0f
        private set
    private val visit = RumleVisit()
    val rumle get() = visit.moment
    private var pendingGift = BergItem.NONE
    private var giftLeader = -1
    private var welcomed = false
    private var frameVisit = false
    var visitFocus = 0f
        private set
    var caveMouseAge = -1f
        private set
    var springAge = -1f
        private set
    var springKey = 0
        private set
    var roomFocus=0f
        private set
    var roomTarget=BergAction.MOUSE
        private set
    private var roomFraming=false
    var springHit = false
        private set
    var kitchenPlay = 0f
        private set
    var kitchenEvent = BergEvent.NONE
        private set
    var panPlay=0f
        private set
    var panFocus=0f
        private set
    private var panFocusAllowed=false
    private var panAwarded=false
    var crystalPlay=0f
        private set
    var crystalIndex=-1
        private set
    var harePlay = 0f
        private set
    var butterflyPlay=0f
        private set
    private var butterflyLeader=-1
    var hareIndex = -1
        private set
    var skipPlay=0f
        private set
    var skipOrigin=BergPoint(5f,7f)
        private set
    var pitch = .25f
    var distance = 10f
    var destination: BergPoint? = null
        private set
    private val path = ArrayDeque<BergPoint>()
    private val trail = ArrayDeque<BergPoint>()
    private var skipStage=0
    private var lastTrail = BergPoint(state.x, state.z)

    init { state.repair(); resetTrail() }
    fun resetTrail() {
        trail.clear()
        var last=BergPoint(state.x,state.z)
        repeat(100) { index ->
            val candidate=BergPoint(state.x,state.z+index*.2f)
            if(Highlands.canStep(last.x,last.z,candidate.x,candidate.z,state.cave)) last=candidate
            trail.addLast(last)
        }
        lastTrail = BergPoint(state.x, state.z)
    }
    fun cancelPanFocus() { panFocusAllowed=false; frameVisit=false;roomFraming=false }
    fun stop() { friendHand=false;echo.close(); pendingGift = BergItem.NONE; cancelPanFocus();butterflyPlay=0f;side = 0f; forward = 0f; lookSide = 0f; lookUp = 0f; path.clear(); destination = null; moving = false }
    fun resetCamera() { cancelPanFocus();state.yaw = facing - PI.toFloat(); pitch = .25f; distance = 10f }
    fun zoomBy(scale:Float) {
        if(!scale.isFinite() || scale<=0f) return
        cancelPanFocus()
        distance=(distance/scale).coerceIn(4f,16f)
    }
    fun walkTo(point: BergPoint) {
        cancelPanFocus()
        pendingGift = BergItem.NONE
        val route = Highlands.route(BergPoint(state.x, state.z), point, state.cave)
        path.clear(); path.addAll(route); destination = if (route.isEmpty()) null else point
    }
    fun callFriend() {
        if (!state.mothFriend || echo.active) return
        val lift = !friendHand
        stop()
        friendHand = lift
        friendLeader = state.leader
    }

    fun playEcho(key:Int):BergEvent {
        val result=echo.note(key)
        if(result==BergEvent.STONE_NOTE) {
            val p=BergEcho.stone(state.cave,key)
            facing=atan2(p.x-state.x,p.z-state.z)
        }
        return result
    }

    fun leadEcho(point:EchoLight?) {
        echo.lead(point)
        echo.guide?.let { if(hypot(it.x-state.x,it.z-state.z)>.18f) facing=atan2(it.x-state.x,it.z-state.z) }
    }

    fun waveOutside(): BergEvent {
        if (nearby()?.action != BergAction.ENTER) return BergEvent.NONE
        welcomed = true
        visit.wave()
        frameVisit = true
        return BergEvent.WAVE
    }

    fun callMouse():BergEvent {
        if(!state.cave || !CaveMouse.canCall(state.x,state.z) || caveMouseAge>=0f) return BergEvent.NONE
        stop()
        val mouse=CaveMouse.pose(time)
        facing=atan2(mouse.x-state.x,mouse.z-state.z)
        caveMouseAge=0f;roomTarget=BergAction.MOUSE;roomFraming=true
        return BergEvent.MOUSE
    }

    fun touchSpring(key:Int?=null):BergEvent {
        if(!state.cave || !BergSpring.inReach(state.x,state.z)) return BergEvent.NONE
        stop()
        springKey=key?.coerceIn(0,2) ?: if(state.springAwake) (springKey+1)%3 else 0
        val cap=BergSpring.caps[springKey]
        facing=atan2(cap.x-state.x,cap.z-state.z)
        state.springAwake=true
        springAge=0f;roomTarget=BergAction.SPRING;roomFraming=true
        return BergEvent.SPRING
    }

    fun followRecipe() {
        val next = BergKitchen.next(state)
        if (BergItem.BERRY_CUP in state.bag) state.held = BergItem.BERRY_CUP
        val spot = Highlands.spots.first { it.action == next && it.cave == state.cave }
        walkTo(if (next == BergAction.TROLL) BergKitchen.serving else BergPoint(spot.x, spot.z))
    }

    fun follower(index: Int): BergPoint {
        val p=trail.elementAtOrNull((index * 3).coerceAtMost(trail.size - 1)) ?: BergPoint(state.x,state.z)
        val offset=(if(index%2==0) 1f else -1f)*(1.45f+(index-1)/2*.85f)
        val side=BergPoint(p.x+cos(facing)*offset,p.z-sin(facing)*offset)
        return if(Highlands.canStep(p.x,p.z,side.x,side.z,state.cave)) side else p
    }
    fun nearby(): BergSpot? {
        var closest:BergSpot?=null;var best=Float.POSITIVE_INFINITY
        for(spot in Highlands.spots) {
            if(spot.cave!=state.cave || !available(spot)) continue
            val dx=spot.x-state.x;val dz=spot.z-state.z;val squared=dx*dx+dz*dz
            // Each activity has its own reach. A closer but out-of-reach small
            // activity must not hide a larger reachable one beside it.
            if(squared<=spot.radius*spot.radius && squared<best) {closest=spot;best=squared}
        }
        return closest
    }
    private fun available(spot: BergSpot): Boolean = when (spot.action) {
        BergAction.DIAMOND -> state.taken and 1 == 0
        else -> true
    }
    fun step(seconds: Float): BergEvent {
        val dt = seconds.coerceIn(0f, .05f)
        val previousTime=time
        time += dt
        springHit=state.cave && BergSpring.sinceDrop(time)<BergSpring.sinceDrop(previousTime)
        if(caveMouseAge>=0f) {
            caveMouseAge+=dt
            if(!state.cave || caveMouseAge>=CaveMouse.RESPONSE_SECONDS) caveMouseAge=-1f
        }
        if(springAge>=0f) {
            springAge+=dt
            if(!state.cave || springAge>=BergSpring.RESPONSE) springAge=-1f
        }
        if (abs(lookSide) > .12f) state.yaw -= lookSide * dt * 1.8f
        if (abs(lookUp) > .12f) pitch = (pitch - lookUp * dt * .85f).coerceIn(.15f, 1.12f)
        kitchenPlay = (kitchenPlay - dt).coerceAtLeast(0f)
        crystalPlay=(crystalPlay-dt).coerceAtLeast(0f)
        harePlay=(harePlay-dt).coerceAtLeast(0f)
        val stick=hypot(side,forward).coerceAtMost(1f)
        val manual=stick>.12f
        butterflyPlay=if(manual || path.isNotEmpty() || state.cave || state.leader!=butterflyLeader) 0f
            else (butterflyPlay-dt).coerceAtLeast(0f)
        if(manual || abs(lookSide)>.12f || abs(lookUp)>.12f) cancelPanFocus()
        val roomStillPlaying=if(roomTarget==BergAction.MOUSE) caveMouseAge>=0f else springAge>=0f
        val roomAmount=if(state.cave && roomFraming && roomStillPlaying) 1f else 0f
        roomFocus+=(roomAmount-roomFocus)*min(1f,dt*3.25f)
        if(abs(roomAmount-roomFocus)<.001f) roomFocus=roomAmount
        val framed = if (frameVisit && rumle.gesture != RumleGesture.REST) 1f else 0f
        visitFocus += (framed-visitFocus)*min(1f,dt*3.8f)
        if (abs(visitFocus-framed)<.001f) visitFocus=framed
        if (rumle.gesture==RumleGesture.REST) frameVisit=false
        val focusTarget=if(panPlay>0f && panFocusAllowed && !state.cave) 1f else 0f
        panFocus+=(focusTarget-panFocus)*min(1f,dt*5f)
        if(abs(panFocus-focusTarget)<.001f) panFocus=focusTarget
        var dx = if(manual) side * cos(state.yaw) - forward * sin(state.yaw) else 0f
        var dz = if(manual) -side * sin(state.yaw) - forward * cos(state.yaw) else 0f
        if (manual) { path.clear(); destination = null; pendingGift = BergItem.NONE }
        else if (path.isNotEmpty()) {
            val target = path.first()
            dx = target.x - state.x; dz = target.z - state.z
            if (hypot(dx, dz) < .18f) { path.removeFirst(); if (path.isEmpty()) destination = null }
        }
        val length = hypot(dx, dz)
        moving = length > .08f
        if (moving) {
            val speed=if(manual) ((stick-.12f)/.88f).coerceIn(0f,1f) else 1f
            val travel = min(3.6f * dt * speed, length)
            dx = dx / length * travel; dz = dz / length * travel
            val oldX = state.x; val oldZ = state.z
            if (Highlands.canStep(state.x, state.z, state.x + dx, state.z + dz, state.cave)) {
                state.x += dx; state.z += dz
            } else {
                if (Highlands.canStep(state.x, state.z, state.x + dx, state.z, state.cave)) state.x += dx
                if (Highlands.canStep(state.x, state.z, state.x, state.z + dz, state.cave)) state.z += dz
            }
            moving = hypot(state.x - oldX, state.z - oldZ) > .001f
            if (moving) facing = atan2(dx, dz)
            else if (path.isNotEmpty()) { path.clear(); destination = null }
            if (hypot(state.x - lastTrail.x, state.z - lastTrail.z) > .25f) {
                lastTrail = BergPoint(state.x, state.z); trail.addFirst(lastTrail)
                if (trail.size > 160) trail.removeLast()
            }
        }
        if(panPlay>0f) {
            if(state.cave || hypot(state.x-Highlands.goldPan.x,state.z-Highlands.goldPan.z)>3.4f) panPlay=0f
            else {
                panPlay=(panPlay-dt).coerceAtLeast(0f)
                if(panPlay<=1f && !panAwarded) {
                    panAwarded=true
                    if(state.taken and 2==0) {
                        state.taken=state.taken or 2;state.bag.add(BergItem.GOLD);state.held=BergItem.GOLD
                        return BergEvent.PICK
                    }
                    return BergEvent.LIGHT
                }
            }
        }
        if (pendingGift != BergItem.NONE) {
            if (!state.cave || pendingGift != state.held || giftLeader != state.leader) pendingGift = BergItem.NONE
            else if (atServingPlace()) return serveRumle()
            else if (destination == null) pendingGift = BergItem.NONE
        }
        val trollX = if (state.cave) -3f else 13f
        val trollZ = if (state.cave) -5f else -15f
        val fromTroll = hypot(state.x - trollX, state.z - trollZ)
        if (fromTroll > 12f) welcomed = false
        if (!welcomed && fromTroll < 8f && state.z > trollZ) {
            welcomed = true
            visit.welcome()
        }
        if(moving || friendLeader!=state.leader || echo.active || rumle.item!=BergItem.NONE || butterflyPlay>0f) friendHand=false
        val hand=if(friendHand) 1f else 0f
        friendReach+=(hand-friendReach)*min(1f,dt*5.5f)
        if(abs(hand-friendReach)<.001f) friendReach=hand
        val echoCue=echo.step(dt)
        if(echoCue!=BergEvent.NONE) return echoCue
        val cue = visit.step(dt)
        if (cue != BergEvent.NONE) return cue
        if(skipPlay>0f) {
            skipPlay=(skipPlay-dt).coerceAtLeast(0f)
            if(skipStage<Highlands.skipTimes.size && Highlands.SKIP_SECONDS-skipPlay>=Highlands.skipTimes[skipStage]) {
                skipStage++;return BergEvent.SPLASH
            }
        }
        return BergEvent.NONE
    }

    private fun atServingPlace() = hypot(state.x - BergKitchen.serving.x, state.z - BergKitchen.serving.z) < .24f

    private fun serveRumle(): BergEvent {
        stop()
        facing = atan2(BergKitchen.TRAY_X-state.x, BergKitchen.TRAY_Z-state.z)
        frameVisit = true
        return visit.start(state)
    }

    fun use(): BergEvent {
        val spot = nearby() ?: return BergEvent.NONE
        fun take(item: BergItem): BergEvent { state.bag.add(item); state.held = item; return BergEvent.PICK }
        return when (spot.action) {
            BergAction.ENTER, BergAction.EXIT -> {
                state.cave = !state.cave
                state.x = if (state.cave) 0f else 13f
                state.z = if (state.cave) 10f else -7.5f
                state.yaw = 0f; stop(); roomFocus=0f; caveMouseAge=-1f; springAge=-1f; resetTrail(); visit.clear(); visitFocus=0f; welcomed=false; kitchenPlay=0f
                harePlay=0f;skipPlay=0f;panPlay=0f;crystalPlay=0f;crystalIndex=-1
                BergEvent.TRAVEL
            }
            BergAction.DIAMOND -> { state.taken = state.taken or 1; take(BergItem.DIAMOND) }
            BergAction.GOLD -> {
                if(panPlay>0f) BergEvent.NONE else {
                    stop();panPlay=Highlands.PAN_SECONDS;panAwarded=false;panFocusAllowed=true
                    facing=atan2(Highlands.goldPan.x-state.x,Highlands.goldPan.z-state.z)
                    BergEvent.WASH
                }
            }
            BergAction.PEPSI -> take(BergItem.PEPSI)
            BergAction.URGE -> take(BergItem.URGE)
            BergAction.CAKE -> take(BergItem.CAKE)
            BergAction.SOCKET -> {
                if (state.light) { state.light = false; take(BergItem.DIAMOND) }
                else if (state.bag.remove(BergItem.DIAMOND)) { state.light = true; if (state.held == BergItem.DIAMOND) state.held = BergItem.NONE; BergEvent.LIGHT }
                else BergEvent.NEED_DIAMOND
            }
            BergAction.TREASURE -> {
                val slot=Highlands.treasures.indexOfFirst {it.x==spot.x && it.z==spot.z}.coerceAtLeast(0)
                if (state.treasure && state.treasureSlot!=slot) BergEvent.GOLD_ELSEWHERE
                else if (state.treasure) { state.treasure = false; take(BergItem.GOLD) }
                else if (state.bag.remove(BergItem.GOLD)) { state.treasure = true;state.treasureSlot=slot; if (state.held == BergItem.GOLD) state.held = BergItem.NONE; BergEvent.LIGHT }
                else BergEvent.NEED_GOLD
            }
            BergAction.TROLL -> {
                if (rumle.gesture != RumleGesture.REST && rumle.gesture != RumleGesture.WAVE) return BergEvent.NONE
                val food = state.held in listOf(BergItem.URGE, BergItem.PEPSI, BergItem.CAKE, BergItem.BERRY_CUP)
                if (food && !atServingPlace()) {
                    walkTo(BergKitchen.serving)
                    if (destination != null) { pendingGift = state.held; giftLeader = state.leader }
                    BergEvent.NONE
                } else if (food) serveRumle() else if(state.held==BergItem.NONE && state.mothFriend && state.song.isNotEmpty()) {
                    stop();facing=atan2(-3f-state.x,-5f-state.z);echo.recallAtRumle()
                } else {
                    facing = atan2(-3f-state.x, -5f-state.z)
                    frameVisit = true
                    visit.start(state)
                }
            }
            BergAction.MOUSE -> callMouse()
            BergAction.SPRING -> touchSpring()
            BergAction.ECHO -> {
                if (echo.active) echo.answer() else {
                    stop()
                    val stone=BergEcho.stone(state.cave,1)
                    facing=atan2(stone.x-state.x,stone.z-state.z)
                    echo.open()
                }
            }
            BergAction.BERRIES, BergAction.KETTLE -> {
                val result = BergKitchen.use(state, spot.action)
                if (result in listOf(BergEvent.BERRIES, BergEvent.FILL, BergEvent.STIR, BergEvent.POUR)) {
                    stop()
                    facing = if (spot.cave) atan2(-17f-state.x, -5.25f-state.z)
                        else atan2(BergKitchen.patch.x-state.x, BergKitchen.patch.z-state.z)
                    kitchenEvent = result
                    kitchenPlay = 1.5f
                }
                result
            }
            BergAction.LOOKOUT -> {
                stop();state.yaw=PI.toFloat();facing=0f;pitch=.25f
                distance=if(distance<14f) 16f else 10f
                BergEvent.LOOKOUT
            }
            BergAction.HARE -> {
                hareIndex=Highlands.hares.indexOfFirst { it.x==spot.x && it.z==spot.z }
                harePlay=5f;BergEvent.HARE
            }
            BergAction.SKIP -> {
                stop();skipOrigin=BergPoint(state.x,state.z);skipPlay=Highlands.SKIP_SECONDS;skipStage=0
                facing=atan2(Highlands.skipLandings.first().x-state.x,Highlands.skipLandings.first().z-state.z)
                BergEvent.SKIP
            }
            BergAction.CRYSTAL -> {
                crystalIndex=Highlands.crystals.indexOfFirst {it.x==spot.x && it.z+2f==spot.z}
                crystalPlay=1.8f
                facing=atan2(spot.x-state.x,spot.z-2f-state.z)
                BergEvent.NOTE
            }
            BergAction.BUTTERFLY -> {
                if(butterflyPlay>0f) BergEvent.NONE else {
                    stop();butterflyLeader=state.leader;butterflyPlay=Highlands.BUTTERFLY_SECONDS
                    facing=atan2(Highlands.butterfly.x-state.x,Highlands.butterfly.z-state.z)
                    BergEvent.BUTTERFLY
                }
            }
        }
    }
}
