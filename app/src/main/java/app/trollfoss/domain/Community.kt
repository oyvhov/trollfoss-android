package app.trollfoss.domain

import kotlin.math.abs
import kotlin.math.sin

enum class Temperament { PLAYFUL, CALM, CURIOUS }
enum class FriendAction { HUG, HIGH_FIVE, HOLD_HANDS }
data class ArtMark(val shape: Int, val color: Int, val x: Float, val y: Float)
data class RoomLink(val place: PlaceId, val slot: Int)
data class GuestHome(val id: Int, val place: PlaceId?, val x: Float, val y: Float, val mode: Mode, val holder: Int, val slot: Int)
class CommunityState {
    val traits = mutableMapOf<Int, Temperament>()
    val pets = mutableMapOf<Int, Int>()
    val petHomes = mutableMapOf<Int, GuestHome>()
    val teddies = mutableMapOf<Int, Int>()
    val art = mutableMapOf<Int, MutableList<ArtMark>>()
    val doors = mutableMapOf<Int, RoomLink>()
    val wallArt = mutableMapOf<String,Int>()
    val band = mutableSetOf<Int>()
    val guests = mutableListOf<GuestHome>()
    var partyPlace: PlaceId? = null
    var partyMusic = 0
    var partyLights = 0
    var returnPlace: PlaceId? = null
    var returnX = 1f
}

/** Small persistent choices, and voluntary shared play. Every participant remains the original body. */
class Community(private val sim: Sim) {
    private val world get() = sim.world
    val state get() = world.community
    data class Bond(val a: Int, val b: Int, val action: FriendAction, var time: Float = 0f)
    val bonds = mutableListOf<Bond>()
    private var beat = 0f
    private val personalityTime = mutableMapOf<Int,Float>()
    fun trait(p: Person) = state.traits[p.id] ?: Temperament.PLAYFUL
    fun setTrait(p: Person, trait: Temperament) = sim.edit {
        if(world.bodies[p.id] === p) { state.traits[p.id] = trait;personalityTime[p.id]=10f }
    }
    fun friend(a: Person, b: Person, action: FriendAction): Boolean = sim.edit {
        if(a === b || a.place == null || a.place != b.place || a.held || b.held || a.mode != Mode.FREE || b.mode != Mode.FREE || abs(a.x-b.x)>0.9f) return@edit false
        bonds.removeAll { it.a in listOf(a.id,b.id) || it.b in listOf(a.id,b.id) }
        val mid=(a.x+b.x)/2
        val gap=if(action==FriendAction.HUG) 0.11f else 0.23f
        a.x=mid-gap/2;b.x=mid+gap/2;b.y=a.y;b.ground=a.ground
        for(p in listOf(a,b)) { p.anim.walkTo=Float.NaN;p.anim.nextWalk=5f;p.anim.face=Face.GRIN;p.anim.faceTime=3f;p.vx=0f;p.vy=0f }
        bonds += Bond(a.id,b.id,action)
        sim.listener.onFx(Fx.SQUEAK,mid,a.y-a.h/2,param=a.id)
        true
    }
    fun choosePet(owner: Person, pet: Person?): Boolean = sim.edit {
        if(world.bodies[owner.id] !== owner || owner.species != Species.FOLK) return@edit false
        if(pet!=null && (world.bodies[pet.id] !== pet || pet.species !in PETS || pet.held)) return@edit false
        val previous=world.bodies[state.pets[owner.id]] as? Person
        if(previous?.held==true) return@edit false
        if(previous!=null && previous!==pet) {
            state.petHomes.remove(previous.id)?.let { home ->
                if(home.place==null) Players.pack(world,previous)
                else {
                    House.moveTo(world,previous,home.place,home.x,home.y)
                    if(home.mode==Mode.SEATED) world.fixtures[home.holder]?.let { sim.seat(previous,it,home.slot) }
                }
            }
        }
        if(pet==null) { state.pets.remove(owner.id);return@edit true }
        state.petHomes.putIfAbsent(pet.id,GuestHome(pet.id,pet.place,pet.x,pet.y,pet.mode,pet.holder,pet.slot))
        state.pets.entries.removeAll { it.value==pet.id }
        state.pets[owner.id]=pet.id
        owner.place?.let { House.moveTo(world,pet,it,(owner.x+0.24f).coerceAtMost(it.width-0.1f),owner.y) }
        pet.anim.face=Face.GRIN;pet.anim.faceTime=3f
        true
    }
    fun follow(to: PlaceId) {
        state.pets.forEach { (owner,id) ->
            val p=world.bodies[owner] as? Person ?: return@forEach
            val pet=world.bodies[id] as? Person ?: return@forEach
            if(p.place==to && p.id in world.playerIds && !pet.held && pet.mode != Mode.BAG) House.moveTo(world,pet,to,(p.x+0.24f).coerceIn(0.1f,to.width-0.1f),p.y)
        }
        state.teddies.forEach { (id,owner) ->
            val p=world.bodies[owner] as? Person ?: return@forEach
            val t=world.bodies[id] as? Thing ?: return@forEach
            if(p.place==to && p.id in world.playerIds && !t.held && t.mode==Mode.FREE) { t.place=to;t.x=p.x-0.2f;t.y=p.y;t.ground=p.y;t.vx=0f;t.vy=0f }
        }
    }
    fun awaken(t: Thing, owner: Person?): Boolean = sim.edit {
        if(t.type != ThingType.TEDDY || t.held || t.mode !in listOf(Mode.FREE,Mode.WORN)) return@edit false
        if(state.teddies.remove(t.id)!=null) return@edit true
        val p=owner?.takeIf { it.place==t.place && !it.held } ?: world.people().firstOrNull { it.place==t.place && it.species==Species.FOLK && !it.held && abs(it.x-t.x)<0.8f } ?: return@edit false
        state.teddies[t.id]=p.id
        if(t.mode==Mode.WORN) { val hand=Anatomy.at(p,Part.HAND);t.x=hand[0];t.y=hand[1];t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.resting=false;t.restOwner=-2;t.ground=p.y }
        sim.listener.onFx(Fx.POOF,t.x,t.y,thing=t);true
    }
    fun linkDoor(f: Fixture, place: PlaceId, slot: Int): Boolean = sim.edit {
        if(f.type != FixtureType.PLAY_DOOR || world.fixtures[f.id] !== f || !place.mine || place==PlaceId.MINE_YARD || slot !in 0 until Mine.SLOTS || !world.mine.standing(place,slot)) return@edit false
        state.doors[f.id]=RoomLink(place,slot);true
    }
    fun door(f: Fixture): RoomLink? = state.doors[f.id]?.takeIf { world.mine.standing(it.place,it.slot) }
    fun startBand(ids: List<Int>): Boolean = sim.edit {
        val people=ids.distinct().mapNotNull { world.bodies[it] as? Person }
        if(people.size<3 || people.map { it.place }.distinct().size!=1 || people.any { it.held || instrument(it)==null }) return@edit false
        state.band.clear();state.band.addAll(people.map { it.id });beat=0f;true
    }
    fun stopBand() { state.band.clear() }
    fun instrument(p:Person)=world.worn(p,Slot.HAND)?.type?.takeIf { it in INSTRUMENTS }
    fun dances(p: Person): Boolean = !p.held && p.mode == Mode.FREE && p.anim.pose == Pose.STAND &&
        (p.id in state.band && instrument(p) != null || state.partyPlace == p.place && state.guests.any { it.id == p.id })
    fun startParty(place: PlaceId, x: Float, ids: List<Int>, music: Int, lights: Int): Boolean = sim.edit {
        if(state.guests.isNotEmpty() || place.mine && place!=PlaceId.MINE_YARD && !world.mine.standing(place,Mine.slotAt(x))) return@edit false
        val guests=ids.distinct().take(8).mapNotNull { world.bodies[it] as? Person }.filter { !it.held && it.species==Species.FOLK }
        if(guests.isEmpty()) return@edit false
        state.partyPlace=place;state.partyMusic=music.coerceIn(0,2);state.partyLights=lights.coerceIn(0,2)
        guests.forEachIndexed { i,p ->
            state.guests += GuestHome(p.id,p.place,p.x,p.y,p.mode,p.holder,p.slot)
            House.moveTo(world,p,place,(x+(i-guests.lastIndex/2f)*0.22f).coerceIn(0.2f,place.width-0.2f),PlaceId.FRONT-0.04f)
            p.anim.wave=2f;p.anim.nextWalk=30f
        }
        sim.listener.onFx(Fx.GIFT,x,place.floor-0.3f);true
    }
    fun endParty(): Boolean = sim.edit {
        if(state.guests.any { world.bodies[it.id]?.held==true }) return@edit false
        for(home in state.guests) {
            val p=world.bodies[home.id] as? Person ?: continue
            p.anim.dance=0f
            if(p.id in world.playerIds) continue
            if(home.place==null) { Players.pack(world,p);continue }
            House.moveTo(world,p,home.place,home.x,home.y)
            if(home.mode==Mode.SEATED) world.fixtures[home.holder]?.let { sim.seat(p,it,home.slot) }
        }
        state.guests.clear();state.partyPlace=null;true
    }
    fun addMark(id:Int,shape:Int,color:Int,x:Float,y:Float):Boolean {
        val marks=state.art[id] ?: return false
        if(marks.size>=80 || !x.isFinite() || !y.isFinite()) return false
        marks += ArtMark(shape.coerceIn(0,2),color.mod(6),x.coerceIn(0f,1f),y.coerceIn(0f,1f));return true
    }
    fun newArt(): Int { val id=world.nextId++;state.art[id]=mutableListOf();return id }
    fun hangArt(id:Int,place:PlaceId,x:Float):Fixture? = sim.edit {
        if(state.art[id].isNullOrEmpty()) return@edit null
        sim.designer.add(place,FixtureType.PICTURE,ART_BASE+id,x,place.back-0.17f)
    }
    fun step(place:PlaceId,dt:Float) {
        for((id,trait) in state.traits) {
            val p=world.bodies[id] as? Person ?: continue
            if(p.place!=place || p.held || p.mode!=Mode.FREE || p.anim.pose==Pose.LIE || !p.anim.walkTo.isNaN()) continue
            val elapsed=(personalityTime[id] ?: 0f)+dt
            personalityTime[id]=elapsed
            if(elapsed<8f+id.mod(5) || p.anim.faceTime>0f) continue
            personalityTime[id]=0f;p.anim.faceTime=2.2f
            when(trait) {
                Temperament.PLAYFUL -> { p.anim.face=Face.LAUGH;p.anim.wave=1.2f;if(p.resting) p.anim.hopV=0.9f }
                Temperament.CALM -> { p.anim.face=Face.GRIN;p.anim.blink=0.22f;p.anim.nextWalk=maxOf(p.anim.nextWalk,5f) }
                Temperament.CURIOUS -> { p.anim.face=Face.OOH;p.anim.say=1;p.anim.sayTime=2f }
            }
        }
        bonds.removeAll { bond ->
            val a=world.bodies[bond.a] as? Person;val b=world.bodies[bond.b] as? Person
            a==null || b==null || a.held || b.held || a.place!=b.place || a.mode!=Mode.FREE || b.mode!=Mode.FREE || abs(a.x-b.x)>0.45f || bond.action!=FriendAction.HOLD_HANDS && bond.time>2.5f
        }
        bonds.forEach { bond -> bond.time+=dt
            for(id in listOf(bond.a,bond.b)) (world.bodies[id] as? Person)?.takeIf { it.place==place }?.let { p -> p.anim.nextWalk=2f;p.anim.wave=if(bond.action==FriendAction.HIGH_FIVE) 0.9f else 0.2f;p.anim.tilt=if(bond.action==FriendAction.HUG) 0.07f*sin(bond.time*2) else 0f }
        }
        for((owner,id) in state.pets) {
            val p=world.bodies[owner] as? Person ?: continue
            val pet=world.bodies[id] as? Person ?: continue
            if(p.place!=place || pet.place!=place || pet.held || pet.mode!=Mode.FREE || p.held) continue
            pet.anim.nextWalk=2f;pet.anim.walkTo=Float.NaN
            val delta=p.x+0.24f-pet.x
            if(abs(delta)>0.1f) { pet.x+=(delta).coerceIn(-dt*0.35f,dt*0.35f);pet.anim.walkPhase+=dt*7f;pet.anim.facing=if(delta>0) 1f else -1f }
            pet.anim.still=2f
        }
        for((id,owner) in state.teddies) {
            val t=world.bodies[id] as? Thing ?: continue
            val p=world.bodies[owner] as? Person ?: continue
            if(t.place!=place || p.place!=place || t.held || t.mode!=Mode.FREE || p.held) continue
            t.x+=(p.x-0.2f-t.x).coerceIn(-dt*0.22f,dt*0.22f);t.rot=sin(sim.time*5f)*7f
        }
        beat+=dt
        if(beat>=0.6f) {
            beat=0f
            state.band.toList().mapNotNull { world.bodies[it] as? Person }.filter { !it.held && it.place==place && it.anim.pose!=Pose.LIE }.forEach { p ->
                instrument(p)?.let { type -> p.anim.face=Face.GRIN;p.anim.faceTime=1f;sim.listener.onFx(if(type==ThingType.DRUM) Fx.DRUM else if(type==ThingType.GUITAR) Fx.STRUM else Fx.SING,p.x,p.y-p.h/2,param=p.id) }
            }
            if(state.partyPlace==place) state.guests.mapNotNull { world.bodies[it.id] as? Person }.filter { !it.held && it.place==place && it.anim.pose!=Pose.LIE }.forEach { p ->
                p.anim.nextWalk=3f
                if(state.partyLights>0) sim.listener.onFx(Fx.SPARKLE,p.x,p.y-p.h,param=p.id)
            }
        }
    }
    companion object {
        const val ART_BASE=2_000_000
        val PETS=setOf(Species.CAT,Species.DOG,Species.BUNNY,Species.DRAGON,Species.PUFFIN,Species.GOAT)
        val INSTRUMENTS=setOf(ThingType.GUITAR,ThingType.DRUM,ThingType.MICROPHONE)
    }
}
