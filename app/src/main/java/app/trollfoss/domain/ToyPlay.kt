package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin

/** The first eight level toys, free experiment tools and a repairable train. Original inputs are retained. */
class ToyPlay(private val sim: Sim) {
    private val world get() = sim.world
    data class Bubble(val fixture: Int, var x: Float, var y: Float, var age: Float = 0f)
    val bubbles = mutableListOf<Bubble>()
    val trainStage get() = (1..3).count { "train:$it" in world.flags }
    fun broken(f: Fixture) = f.type==FixtureType.PLAY_TRAIN && f.variant==1
    fun claim(reward: ToyReward, place: PlaceId, x: Float): Fixture? = sim.edit {
        if (!Progression.unlocked(world,reward)) return@edit null
        Progression.remember(world)
        val probe=Fixture(-1,place,reward.type,x,place.floor)
        val neighbours=world.fixturesIn(place).filter { !it.spec.wall && it.spec.h>=0.05f && it.host<0 }
        val candidates=sequence {
            // Search the whole floor band, beginning near the child. A busy back wall
            // does not mean that the open foreground is full.
            for (depth in listOf(place.floor, PlaceId.FRONT - 0.012f, place.back + 0.015f)) {
                yield(sim.designer.settle(place,probe,x,depth))
                for(i in 1..(place.width/0.18f).toInt()+1) {
                    yield(sim.designer.settle(place,probe,x+i*0.18f,depth))
                    yield(sim.designer.settle(place,probe,x-i*0.18f,depth))
                }
            }
        }
        val spot=candidates.firstOrNull { at ->
            neighbours.none { abs(it.y-at[1])<0.055f && abs(it.x-at[0])<(it.spec.w+probe.spec.w)/2+0.04f }
        } ?: return@edit null
        sim.designer.add(place,reward.type,0,spot[0],spot[1])
    }
    /** The gift parcel of a new level: its first toy is set out near [x] in [place] if there is room; the rest wait in the catalogue. */
    fun openGift(level: Int, place: PlaceId, x: Float): Fixture? {
        if (level <= 1) return null
        val reward = ToyReward.entries.firstOrNull { it.level == level } ?: return null
        return claim(reward, place, x)?.also { supply(it) }
    }
    fun inputs(type: FixtureType): List<ThingType> = when(type) {
        FixtureType.PLAY_TREE,FixtureType.PLAY_CHANNEL,FixtureType.PLAY_WATER_WHEEL,FixtureType.PLAY_CLOUD -> listOf(ThingType.BUCKET)
        FixtureType.PLAY_REPAIR -> listOf(ThingType.SCREWDRIVER)
        FixtureType.PLAY_CRANE,FixtureType.PLAY_CONVEYOR,FixtureType.PLAY_HOVER,FixtureType.PLAY_PORTAL -> listOf(ThingType.TEDDY)
        FixtureType.PLAY_BUILD -> listOf(ThingType.UP_BLOCK,ThingType.UP_BLOCK,ThingType.UP_BLOCK)
        FixtureType.PLAY_RESCUE -> listOf(ThingType.PLANK)
        FixtureType.PLAY_WINDMILL -> listOf(ThingType.HAIR_DRYER)
        FixtureType.PLAY_LAUNCHER -> listOf(ThingType.TEDDY,ThingType.BALL)
        FixtureType.PLAY_MARBLES, FixtureType.PLAY_COLORS -> listOf(ThingType.BALL)
        FixtureType.PLAY_LIFT -> listOf(ThingType.TEDDY)
        FixtureType.PLAY_POPCORN -> listOf(ThingType.PLAY_CORN)
        FixtureType.PLAY_PUMP -> listOf(ThingType.BALL,ThingType.BUCKET)
        FixtureType.PLAY_TRAIN -> listOf(ThingType.PLAY_GEAR)
        else -> emptyList()
    }
    fun supply(f: Fixture) = sim.edit {
        val supplied=mutableSetOf<ThingType>()
        inputs(f.type).forEachIndexed { i,type ->
            val key=if(supplied.add(type)) "${f.id}:$type" else "${f.id}:$type:$i"
            val tool=input(key,type,f.place,(f.x+(i*0.16f)-0.25f).coerceIn(0.1f,f.place.width-0.1f))
            if(type==ThingType.BUCKET && f.type in CreativePlay.TYPES && tool.mode==Mode.FREE && !tool.held) tool.used=1
        }
    }
    private fun input(key: String,type: ThingType,place: PlaceId,x: Float): Thing {
        val old=world.bodies[world.toyInputs[key]] as? Thing
        val t=old?.takeIf { it.type==type } ?: world.addThing(type,0,place,x,PlaceId.FRONT-0.03f).also { world.toyInputs[key]=it.id }
        if (!t.held && t.mode in listOf(Mode.FREE,Mode.BAG)) {
            t.place=place;t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.restOwner=-2;t.resting=false
            t.x=x;t.y=PlaceId.FRONT-0.03f;t.ground=t.y;t.vx=0f;t.vy=0f;t.z=world.nextZ()
        }
        return t
    }
    fun startTrain() = sim.edit {
        world.flags += "train:start"
        val place=PlaceId.MANOR_UPPER
        val f=world.fixtures[world.toyInputs["train:fixture"]]?.takeIf { it.type==FixtureType.PLAY_TRAIN } ?: sim.designer.add(place,FixtureType.PLAY_TRAIN,if(trainStage<2) 1 else 0,1.15f,place.floor)?.also {
            world.toyInputs["train:fixture"]=it.id
            if(trainStage<2) world.flags += "train:broken:${it.id}"
        } ?: return@edit
        if(trainStage<2) input("train:gear",ThingType.PLAY_GEAR,f.place,(f.x-0.4f).coerceAtLeast(0.1f))
    }
    fun lifted(t: Thing) {
        if("train:start" in world.flags && t.id==world.toyInputs["train:gear"] && trainStage==0) world.flags += "train:1"
    }
    fun seated(f: Fixture) {
        if(f.type==FixtureType.PLAY_TRAIN && !broken(f) && trainStage==2 && "train:start" in world.flags) {
            world.flags += "train:3"; world.flags += "toy:TRAIN"
            world.stickers += world.stickers.size
            Progression.remember(world)
            sim.listener.onFx(Fx.GIFT,f.x,f.top,f,param=1)
        }
    }
    fun tap(f: Fixture, dx: Float, dy: Float): Boolean {
        if(f.type in CreativePlay.TYPES) return sim.creative.tap(f)
        if(f.type !in TYPES) return false
        if(f.type==FixtureType.PLAY_BUS || f.type==FixtureType.PLAY_TRAIN && !broken(f)) return false
        sim.here=f.place
        when(f.type) {
            FixtureType.PLAY_BUBBLES -> { f.on=!f.on; if(f.on) emit(f) }
            FixtureType.PLAY_WINDMILL -> { f.on=!f.on; f.timer=if(f.on) 6f else 0f; if(f.on) sim.firstTime(First.WINDMILL,f.x,f.top) }
            FixtureType.PLAY_LAUNCHER -> world.inMachine(f).firstOrNull()?.let { t ->
                release(f,t);t.vx=0.65f;t.vy=-1.35f;t.cool=0.4f;world.flags += "soft:${t.id}";fx(Fx.BOING,f,t)
            }
            FixtureType.PLAY_MARBLES -> {
                val rail=((dy+0.38f)/0.12f).toInt().coerceIn(0,2)
                f.mode=f.mode xor (1 shl rail);f.anim=1f;fx(Fx.TICK,f)
            }
            FixtureType.PLAY_COLORS -> { f.mode=(f.mode+1)%6;fx(Fx.SPARKLE,f) }
            FixtureType.PLAY_LIFT -> { f.mode=1-f.mode.coerceIn(0,1);fx(Fx.PUMP,f) }
            FixtureType.PLAY_POPCORN -> { if(world.inMachine(f).isNotEmpty()) { f.on=true;f.timer=0f;fx(Fx.COOKED,f) } }
            FixtureType.PLAY_PUMP -> { world.bodiesIn(f.place).filterIsInstance<Thing>().firstOrNull { abs(it.x-f.x)<0.35f && accepts(f,it) }?.let { drop(f,it) } }
            FixtureType.PLAY_CAMERA -> photograph(f)
            else -> fx(Fx.BONK,f)
        }
        return true
    }
    fun drop(f: Fixture,t: Thing): Boolean {
        if(f.type in CreativePlay.TYPES) return sim.creative.drop(f,t)
        if(!accepts(f,t) || t.held || world.fixtures[f.id] !== f) return false
        sim.here=f.place
        when(f.type) {
            FixtureType.PLAY_WINDMILL -> { f.on=true;f.timer=8f;fx(Fx.DRY,f,t);free(t);sim.firstTime(First.WINDMILL,f.x,f.top) }
            FixtureType.PLAY_COLORS -> {
                if(world.flags.none { it.startsWith("dye:${t.id}:") }) world.flags += "dye:${t.id}:${t.variant}"
                t.variant=f.mode.mod(t.type.variants);free(t);fx(Fx.SPLAT,f,t)
            }
            FixtureType.PLAY_PUMP -> {
                if(t.type==ThingType.BUCKET || t.type==ThingType.WATERING_CAN) { t.used=1;fx(Fx.WATER,f,t) }
                else { t.type=ThingType.BEACH_BALL;t.used=0;t.vy=-0.5f;fx(Fx.PUMP,f,t) }
                free(t)
            }
            FixtureType.PLAY_TRAIN -> {
                if(!broken(f)) return false
                f.variant=0;world.flags.remove("train:broken:${f.id}");t.mode=Mode.BAG;t.place=null;t.holder=-1;t.inside=-1
                if("train:start" in world.flags) { world.flags += "train:1";world.flags += "train:2" }
                fx(Fx.BUILD,f,t)
            }
            else -> {
                if(world.inMachine(f).isNotEmpty()) return false
                t.mode=Mode.INSIDE;t.holder=f.id;t.inside=-1;t.vx=0f;t.vy=0f;t.rot=0f;t.resting=false;t.ground=f.depth
                t.x=f.x;t.y=f.top;f.count=t.id;f.timer=0f
                if(f.type==FixtureType.PLAY_MARBLES || f.type==FixtureType.PLAY_POPCORN) f.on=true
                fx(Fx.INTO,f,t)
            }
        }
        f.anim=1f
        return true
    }
    fun wash(t: Thing) {
        val flag=world.flags.firstOrNull { it.startsWith("dye:${t.id}:") } ?: return
        flag.substringAfterLast(':').toIntOrNull()?.let { t.variant=it.mod(t.type.variants) }
        world.flags.remove(flag)
    }
    private fun photograph(f: Fixture) {
        val p=world.bodiesIn(f.place).filterIsInstance<Person>().filter { it.species==Species.FOLK && !it.held && abs(it.x-f.x)<0.8f }
            .minByOrNull { abs(it.x-f.x) } ?: return
        val id=world.nextId++
        val frame=sim.designer.add(f.place,FixtureType.PICTURE,PHOTO_BASE+id,f.x,f.place.back-0.15f) ?: return
        world.toyPhotos[id]=Person(id,p.species,p.look.copy(),p.voice,p.name)
        frame.anim=1f;fx(Fx.SPARKLE,f);sim.tasks.record(Deed.PHOTO,f.place);sim.firstTime(First.PHOTO,f.x,f.top)
        p.anim.face=Face.GRIN;p.anim.faceTime=2f;p.anim.wave=1f
    }
    fun releaseAll(f: Fixture) = sim.edit { world.inMachine(f).forEach { release(f,it) };f.on=false;f.count=0 }
    private fun free(t: Thing) { t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.resting=false;t.restOwner=-2 }
    private fun release(f: Fixture,t: Thing) { free(t);t.x=f.x;t.y=f.y-0.06f;t.ground=f.depth;t.vx=0f;t.vy=-0.2f;f.count=0 }
    private fun fx(fx: Fx,f: Fixture,t: Thing?=null) = sim.listener.onFx(fx,f.x,f.top,f,t)
    private fun emit(f: Fixture) {
        if(bubbles.size<36) bubbles += Bubble(f.id,f.x+sin(sim.time)*0.08f,f.top)
        fx(Fx.BUBBLES,f)
    }
    fun pop(place: PlaceId,x: Float,y: Float): Boolean {
        val bubble=bubbles.firstOrNull { world.fixtures[it.fixture]?.place==place && hypot(it.x-x,it.y-y)<0.065f } ?: return false
        bubbles.remove(bubble);sim.listener.onFx(Fx.BOING,x,y);sim.firstTime(First.BUBBLE_POP,x,y);return true
    }
    fun tick(place: PlaceId,dt: Float) {
        bubbles.removeAll { world.fixtures[it.fixture]?.place != place || it.age>4f }
        bubbles.forEach { it.age+=dt;it.y-=dt*0.06f;it.x+=sin(it.age*2)*dt*0.014f }
        val soft=world.flags.filter { it.startsWith("soft:") }
        for(flag in soft) {
            val t=world.bodies[flag.substringAfter(':').toIntOrNull()] as? Thing
            if(t==null || t.mode!=Mode.FREE) { world.flags.remove(flag);continue }
            if(t.place!=place || t.held || t.vy<0f) continue
            val p=world.bodiesIn(place).filterIsInstance<Person>().firstOrNull { !it.held && it.species==Species.FOLK && abs(it.x-t.x)<0.2f && abs(it.y-t.y)<0.25f && world.worn(it,Slot.HAND)==null }
            if(p!=null) { sim.give(p,t,Part.HAND);p.anim.face=Face.LAUGH;p.anim.faceTime=2f;world.flags.remove(flag) }
            else if(t.resting) world.flags.remove(flag)
        }
    }
    fun step(f: Fixture,dt: Float): Boolean {
        if(sim.creative.step(f,dt)) return true
        if(f.type !in TYPES || Vehicles.controllable(f)) return false
        val t=(world.bodies[f.count] as? Thing)?.takeIf { it.mode==Mode.INSIDE && it.holder==f.id }
        when(f.type) {
            FixtureType.PLAY_BUBBLES -> if(f.on) { f.timer+=dt;if(f.timer>0.65f) { f.timer=0f;emit(f) } }
            FixtureType.PLAY_WINDMILL -> if(f.on) { f.angle+=dt*3f;f.timer-=dt;if(f.timer<=0f) f.on=false }
            FixtureType.PLAY_LIFT -> {
                val target=f.mode.coerceIn(0,1).toFloat()
                f.angle += (target-f.angle).coerceIn(-dt*0.8f,dt*0.8f)
                t?.let { it.x=f.x;it.y=f.y-0.045f-f.angle*0.28f }
            }
            FixtureType.PLAY_LAUNCHER -> t?.let { it.x=f.x;it.y=f.y-0.04f }
            FixtureType.PLAY_MARBLES -> if(t!=null && f.on) {
                f.timer+=dt*0.7f
                val rail=f.timer.toInt().coerceIn(0,2)
                if(f.mode and (1 shl rail)==0 || f.timer>=3f) { release(f,t);f.on=false;fx(Fx.DING,f,t) }
                else {
                    val progress=f.timer%1f
                    t.x=f.x+if(rail%2==0) -0.23f+progress*0.46f else 0.23f-progress*0.46f
                    t.y=f.y-0.32f+rail*0.10f+progress*0.04f
                }
            }
            FixtureType.PLAY_POPCORN -> if(t!=null && f.on) {
                f.timer+=dt;t.x=f.x;t.y=f.y-0.14f
                if(f.timer>=1.8f) { t.type=ThingType.POPCORN;t.used=0;release(f,t);f.on=false;fx(Fx.COOKED,f,t) }
            }
            else -> Unit
        }
        return true
    }
    companion object {
        const val PHOTO_BASE=10000
        val TYPES=ToyReward.entries.map { it.type }.toSet()
        val VISIBLE_INSIDE=setOf(FixtureType.PLAY_LIFT,FixtureType.PLAY_MARBLES,FixtureType.PLAY_LAUNCHER,FixtureType.PLAY_POPCORN,
            FixtureType.PLAY_CRANE,FixtureType.PLAY_CONVEYOR,FixtureType.PLAY_BUILD,FixtureType.PLAY_HOVER,FixtureType.PLAY_RESCUE)
        fun accepts(f: Fixture,t: Thing): Boolean = when(f.type) {
            FixtureType.PLAY_WINDMILL -> t.type==ThingType.HAIR_DRYER
            FixtureType.PLAY_COLORS -> t.type.variants>1 && t.type.cat !in setOf(Cat.HAT,Cat.GARMENT)
            FixtureType.PLAY_LAUNCHER -> t.type in setOf(ThingType.BALL,ThingType.BEACH_BALL,ThingType.TEDDY)
            FixtureType.PLAY_MARBLES -> t.type==ThingType.BALL
            FixtureType.PLAY_LIFT -> t.type.cat in setOf(Cat.TOY,Cat.HOME,Cat.TOOL)
            FixtureType.PLAY_POPCORN -> t.type==ThingType.PLAY_CORN
            FixtureType.PLAY_PUMP -> t.type in setOf(ThingType.BALL,ThingType.BEACH_BALL,ThingType.BUCKET,ThingType.WATERING_CAN)
            FixtureType.PLAY_TRAIN -> t.type==ThingType.PLAY_GEAR
            else -> CreativePlay.accepts(f,t)
        }
    }
}
