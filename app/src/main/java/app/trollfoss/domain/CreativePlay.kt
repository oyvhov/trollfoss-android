package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.sin

/** Moving mechanisms, garden memories and repeatable creative routes. Inputs keep their IDs. */
class CreativePlay(private val sim:Sim) {
    private val world get()=sim.world
    private var weatherTime=0f
    fun drop(f:Fixture,t:Thing):Boolean {
        if(f.type in AdventurePlay.TYPES) return sim.adventure.drop(f,t)
        if(!accepts(f,t) || t.held) return false
        when(f.type) {
            FixtureType.PLAY_TREE -> { t.used=0;water(f) }
            FixtureType.PLAY_CHANNEL -> { t.used=0;f.count=(f.count+8).coerceAtMost(32);f.on=true }
            FixtureType.PLAY_WATER_WHEEL -> { t.used=0;f.on=true;f.count=8 }
            FixtureType.PLAY_CLOUD -> { t.used=0;f.on=true;f.count=12 }
            FixtureType.PLAY_REPAIR -> { f.mode=1;f.on=true;world.flags += "place:repaired:${f.id}";sim.listener.onFx(Fx.BUILD,f.x,f.top,f,t) }
            FixtureType.PLAY_PORTAL -> return transport(f,t)
            else -> {
                if(world.inMachine(f).isNotEmpty() && f.type!=FixtureType.PLAY_BUILD) return false
                if(f.type==FixtureType.PLAY_BUILD && world.inMachine(f).size>=3) return false
                t.mode=Mode.INSIDE;t.holder=f.id;t.inside=-1;t.resting=false;t.restOwner=-2;t.x=f.x;t.y=f.top;t.vx=0f;t.vy=0f;f.timer=0f
                if(f.type==FixtureType.PLAY_RESCUE) { f.mode=1;f.open=true }
                else if(f.type!=FixtureType.PLAY_HOVER) f.on=true
                if(f.type==FixtureType.PLAY_CONVEYOR && f.mode==0) f.mode=1
            }
        }
        f.anim=1f;sim.listener.onFx(Fx.INTO,f.x,f.top,f,t);return true
    }
    fun water(f:Fixture) {
        if(f.type==FixtureType.PLAY_TREE) { f.mode=(f.mode+1).coerceAtMost(3);f.anim=1f;sim.listener.onFx(Fx.GROW,f.x,f.top,f);if(f.mode==3) world.flags += "garden:grown:${f.id}" }
        if(f.type==FixtureType.PLAY_WINDMILL || f.type==FixtureType.PLAY_WATER_WHEEL) { f.on=true;f.timer=4f;f.count=8 }
    }
    fun tap(f:Fixture):Boolean {
        if(f.type in AdventurePlay.TYPES) return sim.adventure.tap(f)
        if(f.type !in TYPES) return false
        when(f.type) {
            FixtureType.PLAY_TREE -> if(f.mode>=3) {
                val key="tree:fruit:${f.id}"
                val old=world.bodies[world.toyInputs[key]] as? Thing
                if(old==null || old.mode==Mode.BAG || old.type!=ThingType.APPLE) {
                    val fruit=old?.takeIf { it.type==ThingType.APPLE } ?: world.addThing(ThingType.APPLE,0,f.place,f.x,f.y).also { world.toyInputs[key]=it.id }
                    fruit.place=f.place;fruit.mode=Mode.FREE;fruit.holder=-1;fruit.x=f.x;fruit.y=f.top;fruit.ground=f.depth;fruit.vy=-0.25f;fruit.resting=false;sim.listener.onSpawn(fruit);sim.firstTime(First.APPLE_HARVEST,f.x,f.top)
                }
            }
            FixtureType.PLAY_REPAIR -> if(f.mode>0) { f.on=!f.on;sim.listener.onFx(Fx.ON,f.x,f.top,f) }
            FixtureType.PLAY_CHANNEL -> { f.on=!f.on }
            FixtureType.PLAY_CRANE -> { f.mode=(f.mode+1)%3;f.on=true;sim.firstTime(First.CRANE,f.x,f.top) }
            FixtureType.PLAY_CONVEYOR -> { f.mode=if(f.mode==1) -1 else 1;f.on=!f.on;sim.firstTime(First.CONVEYOR,f.x,f.top) }
            FixtureType.PLAY_HOVER -> { f.on=!f.on;f.mode=if(f.on) 1 else 0;sim.firstTime(First.HOVER,f.x,f.top) }
            FixtureType.PLAY_BUILD -> assemble(f)
            FixtureType.PLAY_MIRROR -> world.people().firstOrNull { it.place==f.place && !it.held && abs(it.x-f.x)<0.65f }?.let { p ->
                p.anim.face=Face.entries[(f.mode++).mod(4)];p.anim.faceTime=3f;p.anim.wave=1f;f.count=p.id;sim.firstTime(First.MIRROR,f.x,f.top)
            }
            FixtureType.PLAY_PUPPETS -> { f.on=!f.on;f.timer=0f;sim.listener.onFx(Fx.PAGE,f.x,f.top,f) }
            FixtureType.PLAY_PICNIC -> { f.open=!f.open;if(f.open) { share(f);sim.firstTime(First.PICNIC,f.x,f.top) } }
            FixtureType.PLAY_PORTAL -> world.people().firstOrNull { it.place==f.place && !it.held && it.mode==Mode.FREE && abs(it.x-f.x)<0.2f }?.let { transport(f,it) }
            FixtureType.PLAY_SEESAW -> { f.on=!f.on;sim.firstTime(First.SEESAW,f.x,f.top) }
            FixtureType.PLAY_TUNNEL,FixtureType.PLAY_JUMP -> { f.on=true;f.timer=0f }
            FixtureType.PLAY_CLOUD,FixtureType.PLAY_WATER_WHEEL -> { f.on=!f.on }
            FixtureType.PLAY_RESCUE -> {
                val p=world.people().firstOrNull { it.place==f.place && it.species==Species.FOLK && !it.held && it.mode==Mode.FREE && abs(it.x-f.x)<0.7f }
                if(p!=null && (f.mode==1 || sim.community.bonds.any { p.id in listOf(it.a,it.b) && it.action==FriendAction.HOLD_HANDS })) { f.count=p.id;f.on=true;f.timer=0f }
            }
            else -> Unit
        }
        f.anim=1f;return true
    }
    fun transport(f:Fixture,b:Body):Boolean {
        val other=world.fixturesIn(f.place).filter { it.type==FixtureType.PLAY_PORTAL && it.id!=f.id && it.variant==f.variant }.minByOrNull { abs(it.x-f.x) } ?: return false
        if(b.held || b.mode !in listOf(Mode.FREE,Mode.WORN) || b is Thing && b.mode==Mode.WORN) return false
        val arrival=(other.x+0.24f).coerceIn(b.w/2+0.01f,f.place.width-b.w/2-0.01f)
        if(b is Person) House.moveTo(world,b,f.place,arrival,other.y)
        else { b.mode=Mode.FREE;b.holder=-1;b.inside=-1;b.x=arrival;b.y=other.y;b.ground=other.depth;b.resting=false;b.restOwner=-2;b.vx=0f;b.vy=0f }
        sim.listener.onFx(Fx.POOF,other.x,other.top,other);return true
    }
    private fun assemble(f:Fixture) {
        val parts=world.inMachine(f)
        if(parts.size<3 || f.id in world.playAssemblies) return
        // A real, steerable cart; the bench is emptied and every original part belongs to the cart.
        val cart=sim.designer.add(f.place,FixtureType.PLAY_CART,0,f.x+0.5f,f.y) ?: return
        world.playAssemblies[cart.id]=PlayAssembly(PlayRecipe.BLOCK_CART,parts.map { it.id })
        parts.forEach { it.holder=cart.id }
        cart.on=true;f.on=false;sim.listener.onFx(Fx.BUILD,cart.x,cart.top,cart)
    }
    private fun share(f:Fixture) {
        val food=world.bodiesIn(f.place).filterIsInstance<Thing>().filter { it.mode==Mode.FREE && !it.held && it.type.cat==Cat.FOOD && abs(it.x-f.x)<0.45f }
        val friends=f.spec.spots.indices.mapNotNull { world.seatedAt(f,it) }
        food.zip(friends.filter { world.worn(it,Slot.HAND)==null }).forEach { (t,p) -> sim.give(p,t,Part.HAND) }
    }
    fun step(f:Fixture,dt:Float):Boolean {
        if(f.type !in TYPES || Vehicles.controllable(f)) return false
        if(f.type in AdventurePlay.TYPES) return sim.adventure.step(f,dt)
        val cargo=world.inMachine(f).firstOrNull()
        when(f.type) {
            FixtureType.PLAY_SEESAW -> {
                val left=world.seatedAt(f,0);val right=world.seatedAt(f,1)
                val target=when { left==null && right==null -> 0f;left==null -> 0.10f;right==null -> -0.10f;f.on -> sin(sim.time*1.8f)*0.10f;else -> 0f }
                f.angle+=(target-f.angle).coerceIn(-dt*0.18f,dt*0.18f)
            }
            FixtureType.PLAY_PUPPETS -> if(f.on) { f.timer+=dt;if(f.timer>1.5f) { f.timer=0f;f.mode=(f.mode+1)%3;sim.listener.onFx(Fx.PAGE,f.x,f.top,f);world.people().filter { it.place==f.place && !it.held && abs(it.x-f.x)<1f && it.anim.pose!=Pose.LIE }.forEach { it.anim.face=Face.LAUGH;it.anim.faceTime=2f;it.anim.wave=1f } } }
            FixtureType.PLAY_CRANE -> if(cargo!=null) {
                f.angle+=((if(f.mode==1) 1f else 0f)-f.angle).coerceIn(-dt*0.5f,dt*0.5f)
                cargo.x=f.x+if(f.mode==2) 0.22f else -0.1f;cargo.y=f.y-0.08f-f.angle*0.32f
                if(f.mode==2) release(f,cargo,cargo.x)
            }
            FixtureType.PLAY_CONVEYOR -> if(cargo!=null && f.on) { f.timer+=dt*0.3f;cargo.x=f.x+f.mode*(f.timer-0.25f);cargo.y=f.y-0.12f;if(f.timer>=0.52f) release(f,cargo,cargo.x) }
            FixtureType.PLAY_HOVER -> { f.angle+=(f.mode-f.angle).coerceIn(-dt*0.6f,dt*0.6f);cargo?.let { it.x=f.x;it.y=f.y-0.06f-f.angle*0.22f+sin(sim.time*2f)*0.004f } }
            FixtureType.PLAY_CHANNEL,FixtureType.PLAY_CLOUD,FixtureType.PLAY_WATER_WHEEL -> if(f.on && f.count>0) {
                f.timer+=dt;f.angle+=dt*2f
                if(f.timer>0.25f) { f.timer=0f;f.count--;flow(f) }
                if(f.count<=0) f.on=false
            }
            FixtureType.PLAY_TUNNEL,FixtureType.PLAY_JUMP -> {
                val p=world.seatedAt(f,0)
                if(p!=null) { f.on=true;f.timer+=dt;if(f.timer>1.5f) { House.moveTo(world,p,f.place,(f.x+0.35f).coerceAtMost(f.place.width-0.2f),f.y);p.vy=if(f.type==FixtureType.PLAY_JUMP) -0.65f else -0.15f;p.anim.face=Face.GRIN;p.anim.faceTime=2f;f.on=false;f.timer=0f;sim.listener.onFx(Fx.WHEE,p.x,p.y-p.h,f,param=p.id) } }
            }
            FixtureType.PLAY_RESCUE -> if(f.on) {
                val p=world.bodies[f.count] as? Person
                if(p==null || p.held || p.place!=f.place || p.mode!=Mode.FREE) f.on=false
                else { f.timer+=dt;p.x=(f.x-0.25f+f.timer*0.3f).coerceIn(0.2f,f.place.width-0.2f);p.anim.nextWalk=2f
                    if(f.timer>=2f) { finishRescue(f,if(f.mode==1) "bridge" else "friend");f.on=false } }
            }
            else -> Unit
        }
        if(f.type==FixtureType.PLAY_RESCUE && f.mode==0) {
            val boat=world.fixturesIn(f.place).firstOrNull { it.type==FixtureType.BOAT && it.spec.spots.indices.any { i -> world.seatedAt(it,i)!=null } && it.x>f.x+0.3f && abs(it.x-f.x)<0.9f }
            if(boat!=null && "rescue:boat:start:${f.id}" in world.flags) finishRescue(f,"boat")
        }
        return true
    }
    private fun finishRescue(f:Fixture,route:String) {
        world.flags += "rescue:solution:$route"
        if(world.flags.add("rescue:done")) { world.stickers+=world.stickers.size;Progression.remember(world);sim.listener.onFx(Fx.GIFT,f.x,f.top,f) }
    }
    private fun release(f:Fixture,t:Thing,x:Float) { t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.x=x;t.y=f.y-0.04f;t.ground=f.depth;t.resting=false;t.restOwner=-2;t.vx=0f;t.vy=-0.1f;f.on=false;f.timer=0f }
    private fun flow(f:Fixture) {
        if(f.type==FixtureType.PLAY_WATER_WHEEL) return
        val outlet=f.x+if(f.type==FixtureType.PLAY_CHANNEL) 0.25f else 0f
        val next=world.fixturesIn(f.place).filter { it.id!=f.id && it.type in setOf(FixtureType.PLAY_CHANNEL,FixtureType.PLAY_TREE,FixtureType.PLAY_WATER_WHEEL) && abs(it.y-f.y)<0.16f && it.x>f.x+0.1f && it.x-outlet<it.spec.w/2+0.12f }.minByOrNull { it.x }
        if(next!=null) { if(next.type==FixtureType.PLAY_CHANNEL) { next.count=(next.count+1).coerceAtMost(32);next.on=true } else water(next) }
        else if(f.type==FixtureType.PLAY_CLOUD) world.fixturesIn(f.place).filter { it.type==FixtureType.PLAY_TREE && abs(it.x-f.x)<0.45f }.forEach(::water)
        sim.listener.onFx(Fx.WATER,outlet,f.y-0.05f,f)
    }
    fun secret(t:Thing):Boolean {
        val place=t.place ?: return false
        if(t.held) return false
        when(t.type) {
            ThingType.MAGNET -> world.bodiesIn(place).filterIsInstance<Thing>().filter { it.id!=t.id && !it.held && it.mode==Mode.FREE && it.type in setOf(ThingType.PLAY_GEAR,ThingType.GOLDEN_KEY,ThingType.SCREWDRIVER) && abs(it.x-t.x)<0.8f }.forEach { it.vx=(t.x-it.x)*1.2f;it.vy=-0.3f;it.resting=false;it.cool=1f }
            ThingType.GR_UMBRELLA -> { world.flags.toggleWind();world.fixturesIn(place).filter { it.type==FixtureType.PLAY_WINDMILL }.forEach { it.on=true;it.timer=8f };sim.listener.onFx(Fx.WHEE,t.x,t.y,thing=t) }
            ThingType.SPOON -> { sim.listener.onFx(Fx.XYLO,t.x,t.y,thing=t);world.people().filter { it.place==place && !it.held && abs(it.x-t.x)<0.8f }.forEach { it.anim.dance=1f;it.anim.face=Face.GRIN;it.anim.faceTime=2f } }
            else -> return false
        };return true
    }
    private fun MutableSet<String>.toggleWind() { if(!remove("play:wind")) add("play:wind") }
    fun weather(place:PlaceId,dt:Float) {
        weatherTime+=dt;if(weatherTime<2f || !place.outdoor) return;weatherTime=0f
        if(world.weather==Weather.RAIN) {
            world.bodiesIn(place).filterIsInstance<Thing>().filter { it.mode==Mode.FREE && !it.held && it.type in setOf(ThingType.BUCKET,ThingType.WATERING_CAN) }.forEach { it.used=1 }
            world.fixturesIn(place).filter { it.type==FixtureType.PLAY_TREE }.forEach(::water)
        }
        if("play:wind" in world.flags) world.fixturesIn(place).filter { it.type==FixtureType.PLAY_WINDMILL }.forEach { it.on=true;it.timer=3f }
    }
    companion object {
    fun accepts(f:Fixture,t:Thing):Boolean=if(f.type in AdventurePlay.TYPES) AdventurePlay.accepts(f,t) else when(f.type) {
        FixtureType.PLAY_TREE,FixtureType.PLAY_CHANNEL,FixtureType.PLAY_WATER_WHEEL,FixtureType.PLAY_CLOUD -> t.type in setOf(ThingType.BUCKET,ThingType.WATERING_CAN) && t.used>0
        FixtureType.PLAY_REPAIR -> t.type in setOf(ThingType.PLAY_GEAR,ThingType.HAMMER,ThingType.SCREWDRIVER)
        FixtureType.PLAY_BUILD -> t.type==ThingType.UP_BLOCK
        FixtureType.PLAY_RESCUE -> t.type==ThingType.PLANK
        FixtureType.PLAY_CRANE,FixtureType.PLAY_CONVEYOR,FixtureType.PLAY_HOVER,FixtureType.PLAY_PORTAL -> t.type.cat !in setOf(Cat.HAT,Cat.GARMENT)
        else -> false
    }
        val TYPES=ToyReward.entries.filter { it.ordinal>ToyReward.TRAIN.ordinal }.map { it.type }.toSet() }
}
