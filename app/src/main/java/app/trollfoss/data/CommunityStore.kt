package app.trollfoss.data

import app.trollfoss.domain.*
import org.json.JSONArray
import org.json.JSONObject

/** Optional, bounded save extension. Older worlds have no community section. */
object CommunityStore {
    fun encode(s:CommunityState)=JSONObject().apply {
        put("traits",JSONObject().apply { s.traits.forEach { (id,t) -> put("$id",t.name) } })
        put("pets",JSONObject().apply { s.pets.forEach { (id,pet) -> put("$id",pet) } })
        put("petHomes",JSONArray().apply { s.petHomes.values.forEach { h -> put(JSONObject().apply { put("id",h.id);put("place",h.place?.name);put("x",h.x.toDouble());put("y",h.y.toDouble());put("mode",h.mode.name);put("holder",h.holder);put("slot",h.slot) }) } })
        put("teddies",JSONObject().apply { s.teddies.forEach { (id,owner) -> put("$id",owner) } })
        put("art",JSONObject().apply { s.art.forEach { (id,marks) -> put("$id",JSONArray().apply { marks.forEach { m -> put(JSONObject().apply { put("shape",m.shape);put("color",m.color);put("x",m.x.toDouble());put("y",m.y.toDouble()) }) } }) } })
        put("doors",JSONObject().apply { s.doors.forEach { (id,to) -> put("$id",JSONObject().apply { put("place",to.place.name);put("slot",to.slot) }) } })
        put("band",JSONArray(s.band.toList()))
        put("wallArt",JSONObject().apply { s.wallArt.forEach { (key,id) -> put(key,id) } })
        put("partyPlace",s.partyPlace?.name);put("music",s.partyMusic);put("lights",s.partyLights)
        put("guests",JSONArray().apply { s.guests.forEach { h -> put(JSONObject().apply { put("id",h.id);put("place",h.place?.name);put("x",h.x.toDouble());put("y",h.y.toDouble());put("mode",h.mode.name);put("holder",h.holder);put("slot",h.slot) }) } })
        put("returnPlace",s.returnPlace?.name);put("returnX",s.returnX.toDouble())
    }
    private fun place(name:String)=PlaceId.entries.firstOrNull { it.name==name }
    fun decode(world:World,j:JSONObject?) {
        if(j==null) return
        val s=world.community
        fun map(key:String,action:(Int,Any)->Unit) { j.optJSONObject(key)?.let { o -> o.keys().asSequence().take(500).forEach { k -> k.toIntOrNull()?.takeIf { it>0 }?.let { action(it,o.get(k)) } } } }
        map("traits") { id,value -> if(world.bodies[id] is Person) Temperament.entries.firstOrNull { it.name==value.toString() }?.let { s.traits[id]=it } }
        map("pets") { id,value -> val pet=(value as? Number)?.toInt();if((world.bodies[id] as? Person)?.species==Species.FOLK && (world.bodies[pet] as? Person)?.species in Community.PETS && pet !in s.pets.values) s.pets[id]=pet!! }
        j.optJSONArray("petHomes")?.let { a -> for(i in 0 until minOf(a.length(),500)) {
            val o=a.optJSONObject(i) ?: continue;val id=o.optInt("id")
            if(id !in s.pets.values) continue
            val p=place(o.optString("place"));val x=o.optDouble("x").toFloat();val y=o.optDouble("y").toFloat()
            val mode=Mode.entries.firstOrNull { it.name==o.optString("mode") } ?: Mode.FREE
            if(x.isFinite() && y.isFinite()) s.petHomes[id]=GuestHome(id,p,x.coerceIn(0f,p?.width ?: 12f),y.coerceIn(0f,1f),mode,o.optInt("holder",-1),o.optInt("slot"))
        } }
        map("teddies") { id,value -> val owner=(value as? Number)?.toInt();if((world.bodies[id] as? Thing)?.type==ThingType.TEDDY && world.bodies[owner] is Person) s.teddies[id]=owner!! }
        map("art") { id,value -> val a=value as? JSONArray ?: return@map;val marks=mutableListOf<ArtMark>();for(i in 0 until minOf(a.length(),80)) { val o=a.optJSONObject(i) ?: continue;val x=o.optDouble("x").toFloat();val y=o.optDouble("y").toFloat();if(x.isFinite() && y.isFinite()) marks+=ArtMark(o.optInt("shape").coerceIn(0,2),o.optInt("color").mod(6),x.coerceIn(0f,1f),y.coerceIn(0f,1f)) };s.art[id]=marks;world.nextId=maxOf(world.nextId,id+1) }
        map("doors") { id,value -> val o=value as? JSONObject ?: return@map;val p=place(o.optString("place")) ?: return@map;val slot=o.optInt("slot",-1);if(world.fixtures[id]?.type==FixtureType.PLAY_DOOR && p in listOf(PlaceId.MINE_GROUND,PlaceId.MINE_UPPER) && slot in 0 until Mine.SLOTS) s.doors[id]=RoomLink(p,slot) }
        j.optJSONArray("band")?.let { a -> for(i in 0 until minOf(a.length(),8)) a.optInt(i).takeIf { world.bodies[it] is Person }?.let { s.band+=it } }
        j.optJSONObject("wallArt")?.let { o -> o.keys().asSequence().take(100).forEach { key -> val id=o.optInt(key);if(id in s.art && key.length<80) s.wallArt[key]=id } }
        s.partyPlace=place(j.optString("partyPlace"));s.partyMusic=j.optInt("music").coerceIn(0,2);s.partyLights=j.optInt("lights").coerceIn(0,2)
        j.optJSONArray("guests")?.let { a -> for(i in 0 until minOf(a.length(),8)) {
            val o=a.optJSONObject(i) ?: continue;val id=o.optInt("id");if(world.bodies[id] !is Person || s.guests.any { it.id==id }) continue
            val mode=Mode.entries.firstOrNull { it.name==o.optString("mode") } ?: Mode.FREE
            val p=place(o.optString("place"));val x=o.optDouble("x",1.0).toFloat();val y=o.optDouble("y",0.9).toFloat()
            if(x.isFinite() && y.isFinite()) s.guests+=GuestHome(id,p,x.coerceIn(0f,p?.width ?: 12f),y.coerceIn(0f,1f),mode,o.optInt("holder",-1),o.optInt("slot"))
        } }
        s.returnPlace=place(j.optString("returnPlace"));s.returnX=j.optDouble("returnX",1.0).toFloat().takeIf { it.isFinite() } ?: 1f
    }
}
