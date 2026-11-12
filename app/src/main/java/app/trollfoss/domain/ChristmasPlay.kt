package app.trollfoss.domain

import kotlin.math.abs

/** Drag-and-drop Christmas rules. No calendar lock and no consumption before a valid result. */
class ChristmasPlay(private val sim: Sim) {
    private val world get()=sim.world
    private val state get()=world.christmas
    private var serveCup=-1
    private var serveTime=2f

    fun deed(name:String) { state.deeds+=name;if(name in setOf("calendar","bake","craft","reindeer")) sim.unlock("christmas_$name") }
    private fun fx(f:Fixture, effect:Fx=Fx.SPARKLE) { f.anim=1f; sim.listener.onFx(effect,f.x,f.top,f) }
    fun supply(type:ThingType,variant:Int=0,place:PlaceId=world.place,x:Float=1f):Thing? {
        // Limit idle duplicates without taking held, worn or packed belongings.
        val existing=world.bodiesIn(place).filterIsInstance<Thing>().filter { it.type==type && it.variant==variant && it.mode==Mode.FREE && !it.held && it.homePlace==null }
        if(existing.size>=4) return null
        if(world.bodiesIn(place).count { it is Thing && it.mode==Mode.FREE }>=100) return null
        return world.addThing(type,variant,place,x.coerceIn(0.15f,place.width-0.15f),place.floor-0.3f).also { sim.listener.onSpawn(it) }
    }
    private fun emit(f:Fixture,type:ThingType,v:Int=0):Thing?=supply(type,v,f.place,f.x+0.16f)
    private fun loose(t:Thing,place:PlaceId,x:Float,y:Float) { t.place=place;t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.held=false;t.restOwner=-2;t.resting=false;t.x=x;t.y=y;t.vy=-0.5f }
    private fun take(t:Thing,f:Fixture,slot:Int=0) { t.mode=Mode.INSIDE;t.holder=f.id;t.slot=slot;t.inside=-1;t.held=false;t.restOwner=-2;t.resting=false;t.vx=0f;t.vy=0f }
    fun tap(f:Fixture,dx:Float,dy:Float):Boolean {
        if(f.type !in Christmas.types) return false
        val input=world.inMachine(f)
        when(f.type) {
            in Christmas.trees -> {
                val scale=f.spec.h/0.74f
                val ornament=input.minByOrNull { t -> val p=Christmas.ornamentPoint(t.slot);abs(dx-p.first*scale)+abs(dy-p.second*scale) }
                if(ornament!=null && abs(dx-Christmas.ornamentPoint(ornament.slot).first*scale)<0.05f*scale && abs(dy-Christmas.ornamentPoint(ornament.slot).second*scale)<0.05f*scale) {
                    loose(ornament,f.place,f.x+f.spec.w*0.6f,f.y-0.2f)
                } else { f.mode=(f.mode+1)%3;f.on=f.mode!=0 }
                deed("tree");fx(f)
            }
            FixtureType.XM_CALENDAR -> {
                val col=((dx/f.spec.w+0.5f)*6).toInt().coerceIn(0,5)
                val row=((dy/f.spec.h+1f)*4).toInt().coerceIn(0,3)
                openDoor(row*6+col,f.place,f.x);fx(f,Fx.GIFT)
            }
            FixtureType.XM_BAKE -> {
                if(input.isEmpty()) emit(f,ThingType.XM_DOUGH)
                else if(f.count==3) { f.on=true;f.count=4;f.timer=2.5f;fx(f,Fx.BAKE) }
                else { f.anim=1f;fx(f,Fx.STIR) }
            }
            in Christmas.benches -> {
                if(input.isNotEmpty()) { input.forEach { loose(it,f.place,f.x+0.35f,f.top) };fx(f) }
                else {
                    val type=when(f.type) {
                        FixtureType.XM_BAKE -> ThingType.XM_DOUGH
                        FixtureType.XM_HOUSE_BENCH -> ThingType.XM_GINGER_HEART
                        FixtureType.XM_CRAFT -> ThingType.XM_PAPER
                        FixtureType.XM_TOY_BENCH -> ThingType.PLANK
                        else -> ThingType.XM_PAPER
                    }
                    emit(f,type);fx(f)
                }
            }
            FixtureType.XM_COCOA -> { emit(f,listOf(ThingType.XM_COCOA,ThingType.XM_SPICED_DRINK,ThingType.XM_PORRIDGE)[f.mode.mod(3)]); f.mode=(f.mode+1)%3;deed("cocoa");fx(f,Fx.GIFT) }
            FixtureType.XM_MARKET -> { val choices=Christmas.things;emit(f,choices[f.count.mod(choices.size)]);f.count++;fx(f,Fx.GIFT) }
            FixtureType.XM_BELT -> { f.on=!f.on;deed("belt");fx(f,Fx.VROOM) }
            FixtureType.XM_POST -> { emit(f,ThingType.XM_WISH);fx(f,Fx.PAGE) }
            FixtureType.XM_SLEIGH -> { f.mode=(f.mode+1)%Christmas.stops.size;fx(f,Fx.WHEE) }
            FixtureType.XM_TROUGH -> { emit(f,ThingType.CARROT);fx(f) }
            FixtureType.XM_MUSICBOX -> { f.on=!f.on;deed("concert");fx(f,if(f.on) Fx.ON else Fx.OFF) }
            FixtureType.XM_SNOWGLOBE -> { f.on=!f.on;f.timer=if(f.on) 5f else 0f;deed("snowglobe");fx(f,Fx.SPARKLE) }
            else -> if(f.spec.container!=null) { f.open=!f.open;sim.invalidate(f.place);fx(f,Fx.OPEN) } else { f.on=!f.on;fx(f,if(f.on) Fx.ON else Fx.OFF) }
        }
        return true
    }
    fun accepts(f:Fixture,t:Thing):Boolean = when(f.type) {
        in Christmas.trees -> t.type in Christmas.ornaments && world.inMachine(f).size<12
        FixtureType.XM_WRAP -> t.type!=ThingType.XM_PARCEL && t.type!=ThingType.XM_STOCKING && t.mode!=Mode.INSIDE
        FixtureType.XM_BAKE -> !f.on && when(t.type) {
            ThingType.XM_DOUGH,ThingType.MILK -> world.inMachine(f).isEmpty()
            ThingType.XM_ROLLING_PIN -> f.count==1
            ThingType.XM_CUTTER -> f.count==2
            ThingType.XM_ICING -> true
            else -> false
        }
        FixtureType.XM_HOUSE_BENCH -> (t.type in setOf(ThingType.XM_GINGER_HEART,ThingType.XM_GINGER_MAN) && world.inMachine(f).size<3) || (t.type==ThingType.XM_ICING && world.inMachine(f).size==3)
        FixtureType.XM_CRAFT -> t.type in setOf(ThingType.XM_PAPER,ThingType.XM_BRUSH,ThingType.XM_CONE,ThingType.XM_RIBBON,ThingType.SNOWBALL)
        FixtureType.XM_TOY_BENCH -> (t.type in setOf(ThingType.PLANK,ThingType.STICK) && world.inMachine(f).size<2) || (t.type==ThingType.HAMMER && world.inMachine(f).size==2)
        FixtureType.XM_BELT,FixtureType.XM_SLEIGH -> t.type==ThingType.XM_PARCEL && world.inMachine(f).size<6
        FixtureType.XM_POST -> t.type==ThingType.XM_WISH
        FixtureType.XM_TROUGH -> t.type==ThingType.CARROT || t.type==ThingType.WATERING_CAN || t.type==ThingType.XM_BRUSH
        FixtureType.CAULDRON -> t.type==ThingType.XM_CONE
        else -> false
    }
    fun drop(f:Fixture,t:Thing):Boolean {
        if(!accepts(f,t)) return false
        when(f.type) {
            in Christmas.trees -> { val used=world.inMachine(f).map { it.slot }.toSet();take(t,f,if(t.type==ThingType.XM_STAR && 11 !in used) 11 else (0..11).first { it !in used });deed("tree");if(t.type==ThingType.XM_STAR) deed("star:${f.place.name}") }
            FixtureType.XM_BAKE -> {
                when(t.type) {
                    ThingType.XM_DOUGH -> { take(t,f);f.count=1 }
                    ThingType.MILK -> { take(t,f);f.count=3;f.mode=4 }
                    ThingType.XM_ROLLING_PIN -> { f.count=2;loose(t,f.place,f.x-0.35f,f.top) }
                    ThingType.XM_CUTTER -> { f.count=3;loose(t,f.place,f.x-0.35f,f.top) }
                    ThingType.XM_ICING -> {
                        val cookie=world.bodiesIn(f.place).filterIsInstance<Thing>().firstOrNull { it.type in setOf(ThingType.XM_GINGER_MAN,ThingType.XM_GINGER_HEART,ThingType.XM_GINGER_STAR) && it.mode==Mode.FREE && !it.held && abs(it.x-f.x)<0.5f }
                        if(cookie==null) return false
                        cookie.variant=1;loose(t,f.place,f.x-0.35f,f.top)
                    }
                    else -> return false
                }
            }
            FixtureType.XM_HOUSE_BENCH -> {
                if(t.type!=ThingType.XM_ICING) { take(t,f);f.count=world.inMachine(f).size }
                else {
                    val house=emit(f,ThingType.XM_GINGER_HOUSE) ?: return false
                    world.inMachine(f).toList().forEach { world.remove(it) };f.count=0
                    loose(t,f.place,f.x-0.35f,f.top);house.vy=-0.4f;deed("gingerhouse")
                }
            }
            FixtureType.XM_TOY_BENCH -> {
                if(t.type!=ThingType.HAMMER) {take(t,f);f.count=world.inMachine(f).size}
                else {
                    val output=listOf(ThingType.XM_TRAIN,ThingType.XM_TOP,ThingType.XM_ROBOT)[f.mode.mod(3)]
                    emit(f,output) ?: return false
                    world.inMachine(f).toList().forEach {world.remove(it)};f.count=0
                    loose(t,f.place,f.x-0.35f,f.top);deed("toy")
                }
            }
            FixtureType.XM_WRAP -> {
                val parcel=emit(f,ThingType.XM_PARCEL,f.variant) ?: return false
                // Packages are identities, so an existing package is never reused for another object.
                if(parcel.id in state.parcels) return false
                t.mode=Mode.INSIDE;t.holder=-parcel.id-2;t.place=null;t.held=false;t.inside=-1
                state.parcels[parcel.id]=t.id;deed("wrap")
            }
            FixtureType.XM_BELT,FixtureType.XM_SLEIGH -> { take(t,f);if(f.type==FixtureType.XM_BELT) {f.on=true;f.timer=1.2f} }
            FixtureType.XM_POST -> { t.used=1;loose(t,f.place,f.x+0.2f,f.top);deed("wish");deed("bell:post") }
            FixtureType.XM_TROUGH -> {
                val deer=world.people().firstOrNull { it.place==f.place && it.species==Species.REINDEER && !it.held && abs(it.x-f.x)<2f }
                deer?.let { p -> p.anim.hopV=0.8f;p.anim.chew=1f;p.anim.face=Face.GRIN;p.anim.faceTime=2f
                    if(t.type==ThingType.CARROT) sim.give(p,t,Part.MOUTH)
                }
                if(t.id in world.bodies) loose(t,f.place,f.x+0.2f,f.top)
                deed("reindeer")
            }
            FixtureType.CAULDRON -> { val made=emit(f,ThingType.XM_STAR) ?: return false; world.remove(t);deed("magic");made.vy=-0.5f }
            else -> {
                if(f.type==FixtureType.XM_CRAFT && t.type==ThingType.SNOWBALL) {
                    val lamp=sim.designer.add(f.place,FixtureType.XM_LANTERN,2,f.x+0.5f,f.place.floor) ?: return false
                    lamp.on=true;world.remove(t);deed("snowlantern");fx(f);return true
                }
                val output=when(f.type) {
                    FixtureType.XM_BAKE -> if(t.type==ThingType.MILK || t.type==ThingType.XM_BOWL) ThingType.XM_PORRIDGE else if(t.type==ThingType.XM_ICING) ThingType.XM_GINGER_STAR else listOf(ThingType.XM_GINGER_MAN,ThingType.XM_GINGER_HEART,ThingType.XM_LUSSEKATT)[f.mode.mod(3)]
                    FixtureType.XM_HOUSE_BENCH -> ThingType.XM_GINGER_HOUSE
                    FixtureType.XM_CRAFT -> listOf(ThingType.XM_PAPER_HEART,ThingType.XM_CHAIN,ThingType.XM_SNOWFLAKE)[f.mode.mod(3)]
                    FixtureType.XM_TOY_BENCH -> listOf(ThingType.XM_TRAIN,ThingType.XM_TOP,ThingType.XM_ROBOT)[f.mode.mod(3)]
                    else -> return false
                }
                // Tools stay on the workbench. Materials are consumed only after output capacity was checked.
                val made=emit(f,output) ?: return false
                if(t.type !in setOf(ThingType.XM_CUTTER,ThingType.XM_ROLLING_PIN,ThingType.XM_ICING,ThingType.XM_BRUSH,ThingType.HAMMER)) world.remove(t)
                else loose(t,f.place,f.x-0.25f,f.top)
                deed(when(f.type) { FixtureType.XM_BAKE -> "bake";FixtureType.XM_CRAFT -> "craft";FixtureType.XM_HOUSE_BENCH -> "gingerhouse";else -> "toy" })
                made.vy=-0.4f
            }
        }
        fx(f,Fx.GIFT);return true
    }
    fun openDoor(index:Int,place:PlaceId=world.place,x:Float=1f):Boolean {
        if(index !in 0..23 || index in state.opened) return false
        val type=when(index/6) { 0 -> Christmas.ornaments.toList()[index%6];1 -> Christmas.things[12+index%6];2 -> listOf(ThingType.XM_TRAIN,ThingType.XM_TOP,ThingType.XM_ROBOT,ThingType.XM_BEAR,ThingType.XM_HAT,ThingType.XM_FACE)[index%6];else -> ThingType.XM_WISH }
        val gift=supply(type,if(index>=18) index-18 else if(type==ThingType.XM_HAT || type==ThingType.XM_FACE) index%4 else 0,place,x) ?: return false
        state.opened+=index
        world.fixtures.values.filter {it.type==FixtureType.XM_CALENDAR}.forEach { it.count = state.opened.fold(0) { mask,n -> mask or (1 shl n) } }
        deed("calendar");sim.listener.onFx(Fx.GIFT,x,place.floor-0.2f,thing=gift,param=1)
        return true
    }
    fun use(t:Thing):Boolean {
        val place=t.place ?: return false
        if(t.type==ThingType.XM_PARCEL) {
            val id=state.parcels[t.id] ?: return false
            val child=world.bodies[id] as? Thing ?: return false
            state.parcels.remove(t.id);loose(child,place,t.x,t.y);world.remove(t);deed("give")
            sim.listener.onFx(Fx.GIFT,child.x,child.y,thing=child,param=1);return true
        }
        if(t.type==ThingType.XM_STOCKING) {
            state.stocking.remove(t.id)?.let { id -> (world.bodies[id] as? Thing)?.let { loose(it,place,t.x+0.1f,t.y) } }
            return true
        }
        if(t.type in setOf(ThingType.XM_BELL,ThingType.XM_GINGER_MAN,ThingType.XM_ROBOT,ThingType.XM_TOP,ThingType.XM_TRAIN)) {
            t.resting=false;t.vy=-0.65f;t.vrot=if(t.type==ThingType.XM_TOP) 300f else 30f
            if(t.type==ThingType.XM_TRAIN || t.type==ThingType.XM_ROBOT) t.vx=0.4f
            if(t.type==ThingType.XM_BELL) deed("bell:found")
            deed("concert");sim.listener.onFx(Fx.STRUM,t.x,t.y,thing=t,param=t.id%4);return true
        }
        if(t.type==ThingType.XM_WISH) { t.variant=(t.variant+1)%6;t.used=0;sim.listener.onFx(Fx.PAGE,t.x,t.y,thing=t);return true }
        return false
    }
    fun fillStocking(stock:Thing,item:Thing):Boolean {
        if(stock.type!=ThingType.XM_STOCKING || item.type in setOf(ThingType.XM_PARCEL,ThingType.XM_STOCKING) || stock.id in state.stocking || item.id==stock.id) return false
        state.stocking[stock.id]=item.id;item.mode=Mode.INSIDE;item.holder=-stock.id-2;item.place=null;item.held=false;deed("stocking");return true
    }
    fun sleighRiders(f: Fixture): List<Person> = f.spec.spots.indices.mapNotNull { world.seatedAt(f, it) }

    fun boardingChoices(f: Fixture): List<Person> {
        if (f.type != FixtureType.XM_SLEIGH || sleighRiders(f).size == f.spec.spots.size) return emptyList()
        return world.people().filter { person ->
            person.species == Species.FOLK && person.place != null && person.mode == Mode.FREE &&
                !person.held && !Players.paused(world, person)
        }.sortedWith(compareBy<Person>({ it.place != f.place }, { it.id !in world.playerIds }, { it.name != "Julenissen" }, { it.id }))
    }

    fun boardSleigh(f: Fixture, person: Person): Boolean {
        if (world.fixtures[f.id] !== f || person !in boardingChoices(f)) return false
        val seat = f.spec.spots.indices.firstOrNull { world.seatedAt(f, it) == null } ?: return false
        House.moveTo(world, person, f.place, f.x)
        return sim.seatByChild(person, f, seat)
    }

    fun fly(f:Fixture,to:PlaceId):Boolean {
        if(f.type!=FixtureType.XM_SLEIGH || to !in Christmas.stops) return false
        val passengers=sleighRiders(f)
        if(passengers.isEmpty()) return false
        passengers.forEachIndexed { i,p -> House.moveTo(world,p,to,0.8f+i*0.25f) }
        world.inMachine(f).forEachIndexed { i,t -> loose(t,to,1.4f+i*0.1f,to.floor-0.2f) }
        state.sleighStop=Christmas.stops.indexOf(to);deed("sleigh");return true
    }
    fun party(place:PlaceId,x:Float) {
        if(world.community.partyPlace==place && "christmas:party" in world.flags) {sim.community.endParty();return}
        val ids=world.people().filter { it.species==Species.FOLK && it.mode==Mode.FREE && !it.held }.take(4).map {it.id}
        if(sim.community.startParty(place,x,ids,0,1)) { world.flags+="christmas:party";deed("party") }
    }
    val rampLocation:PlaceId get()=listOf(PlaceId.CHRISTMAS_SQUARE,PlaceId.CHRISTMAS_WORKSHOP,PlaceId.CHRISTMAS_STABLE,PlaceId.CHRISTMAS_HOME)[state.rampStage.coerceIn(0,3)]
    fun found(p:Person):Boolean {
        if(p.name!="Rampenissen" || p.held || state.rampStage>=3) return false
        val old=p.place ?: return false
        if(state.rampStage==2 && supply(ThingType.XM_HAT,3,old,p.x)==null) return false
        sim.listener.onFx(Fx.SPARKLE,p.x,p.y-p.h,param=1)
        state.rampStage++
        House.moveTo(world,p,rampLocation,if(state.rampStage==1) 7.6f else if(state.rampStage==2) 6.6f else 1.2f)
        if(state.rampStage==3) deed("ramp")
        return true
    }
    fun tick(place:PlaceId,dt:Float) {
        val rolf=world.people().firstOrNull {it.name=="Rolf" && it.place==place && !it.held && it.mode==Mode.FREE}
        val cup=rolf?.let {world.worn(it,Slot.HAND)}?.takeIf {it.type==ThingType.XM_COCOA}
        if(cup!=null && rolf!=null) {
            if(serveCup!=cup.id) {serveCup=cup.id;serveTime=2f}
            serveTime-=dt
            if(serveTime<=0f) {
                val friend=world.people().firstOrNull {it.species==Species.FOLK && it.place==place && !it.held && it.mode==Mode.FREE && abs(it.x-rolf.x)<1f && world.worn(it,Slot.HAND)==null}
                if(friend!=null) {sim.give(friend,cup,Part.HAND);rolf.anim.wave=1f;deed("serve");serveCup=-1}
                serveTime=2f
            }
        } else serveCup=-1
        world.fixturesIn(place).filter { it.type==FixtureType.XM_SNOWGLOBE && it.on }.forEach { f -> f.timer-=dt;if(f.timer<=0) f.on=false }
        for(f in world.fixturesIn(place)) {
            if(f.type==FixtureType.XM_BELT && f.on) {
                f.timer-=dt;f.angle=(f.angle+dt*0.4f)%1f
                if(f.timer<=0f) { world.inMachine(f).firstOrNull()?.let { loose(it,place,f.x+0.4f,f.top);deed("belt");fx(f,Fx.BEEP) };f.timer=1.2f }
            }
            if(f.type==FixtureType.XM_BAKE && f.on && f.count==4) {
                f.timer-=dt
                if(f.timer<=0f) {
                    val material=world.inMachine(f).firstOrNull()
                    if(material==null) { f.on=false;f.count=0;continue }
                    val output=listOf(ThingType.XM_GINGER_MAN,ThingType.XM_GINGER_HEART,ThingType.XM_GINGER_STAR,ThingType.XM_LUSSEKATT,ThingType.XM_PORRIDGE)[f.mode.coerceIn(0,4)]
                    val made=emit(f,output)
                    if(made!=null) { world.remove(material);f.on=false;f.count=0;deed("bake");fx(f,Fx.COOKED) }
                    else f.timer=1f
                }
            }
        }
    }
    companion object {
        fun enclosed(world:World,b:Body):Boolean = b.mode==Mode.INSIDE && ((b.holder < -1 && (world.christmas.parcels[-b.holder-2]==b.id || world.christmas.stocking[-b.holder-2]==b.id)) || (world.storage+world.discardedStorage.map { it.second }).any { b.id in it.contents })
    }
}
