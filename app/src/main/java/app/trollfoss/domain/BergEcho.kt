package app.trollfoss.domain

import kotlin.math.*

data class EchoNote(val tone:Int,val beats:Int)
data class EchoLight(val x:Float,val y:Float,val z:Float)

object BergEcho {
    const val LIMIT=6
    const val BEAT=.15f
    val pitches=listOf(1f,1.2599f,1.4983f)
    val outside=BergSpot(BergAction.ECHO,9f,7.1f,false,3.1f)
    val inside=BergSpot(BergAction.ECHO,1.3f,-1.25f,true,2.60f)
    fun site(cave:Boolean)=if(cave) inside else outside
    fun stone(cave:Boolean,index:Int):BergPoint {
        val site=site(cave)
        return BergPoint(site.x+(index-1)*1.15f,site.z-1.55f+(index-1)*(index-1)*.27f)
    }
}

/** Times are simulation seconds. Pausing a visit never plays queued notes in the background. */
class EchoPlay(private val state:HighlandsState) {
    var active=false
        private set
    var tone=-1
        private set
    var glow=0f
        private set
    var replyAge=0f
        private set
    var replyLength=0f
        private set
    var guide:EchoLight?=null
        private set
    private var sinceNote=0f
    private var fresh=true
    private var nextNote=0
    private var nextTime=0f
    private var byHost=false
    private var cave=false
    private var leader=-1
    fun open():BergEvent {
        val site=BergEcho.site(state.cave)
        if(hypot(state.x-site.x,state.z-site.z)>site.radius) return BergEvent.NONE
        byHost=false;active=true;cave=state.cave;leader=state.leader
        return BergEvent.LISTEN
    }
    fun recallAtRumle():BergEvent {
        if(!state.cave || !state.mothFriend || state.song.isEmpty()) return BergEvent.NONE
        byHost=true;cave=true;leader=state.leader;active=true
        return answer()
    }
    fun close() {
        active=false;replyAge=0f;replyLength=0f;tone=-1;glow=0f;guide=null;fresh=true
    }
    fun note(key:Int):BergEvent {
        if(!active || key !in BergEcho.pitches.indices) return BergEvent.NONE
        val beats=if(fresh || replyLength>0f) 3 else (sinceNote/BergEcho.BEAT).roundToInt().coerceIn(2,7)
        if(state.song.size>=BergEcho.LIMIT) state.song.removeAt(0)
        state.song.add(EchoNote(key,beats))
        tone=key;glow=1f;fresh=false;sinceNote=0f;replyLength=0f;replyAge=0f
        return BergEvent.STONE_NOTE
    }
    fun answer():BergEvent {
        if(!active || state.song.isEmpty() || replyLength>0f) return BergEvent.NONE
        nextTime=.88f;replyAge=0f;nextNote=0
        replyLength=nextTime+state.song.drop(1).sumOf {it.beats}*BergEcho.BEAT+1.05f
        guide=null;fresh=true
        return BergEvent.REPLY
    }
    fun clearSong() {
        if(!active) return
        state.song.clear();tone=-1;glow=0f;replyAge=0f;replyLength=0f;fresh=true
    }
    fun lead(point:EchoLight?) {
        if(!active) return
        val site=BergEcho.site(cave)
        guide=point?.takeIf {it.x.isFinite() && it.y.isFinite() && it.z.isFinite()}?.let {
            val x=it.x.coerceIn(site.x-7f,site.x+7f);val z=it.z.coerceIn(site.z-7f,site.z+4f)
            val ground=if(cave) Highlands.floor(x,z,true) else max(Highlands.WATER,Highlands.height(x,z))
            EchoLight(x,it.y.coerceIn(ground+.55f,ground+5.3f),z)
        }
    }
    fun step(dt:Float):BergEvent {
        if(!active) return BergEvent.NONE
        val site=if(byHost) Highlands.spots.first {it.action==BergAction.TROLL} else BergEcho.site(cave)
        if(cave!=state.cave || leader!=state.leader || hypot(state.x-site.x,state.z-site.z)>site.radius+1.0f) {
            close();return BergEvent.NONE
        }
        sinceNote=(sinceNote+dt).coerceAtMost(2f)
        glow=(glow-dt*1.65f).coerceAtLeast(0f)
        if(replyLength<=0f) return BergEvent.NONE
        replyAge+=dt
        if(nextNote<state.song.size && replyAge>=nextTime) {
            tone=state.song[nextNote++].tone;glow=1f
            if(nextNote<state.song.size) nextTime+=state.song[nextNote].beats*BergEcho.BEAT
            return BergEvent.FLUTE_NOTE
        }
        if(replyAge>=replyLength) {
            replyAge=0f;replyLength=0f
            if(!state.mothFriend) {state.mothFriend=true;return BergEvent.FRIEND}
        }
        return BergEvent.NONE
    }
}
