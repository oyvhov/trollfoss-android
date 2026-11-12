package app.trollfoss.data

import app.trollfoss.domain.*
import org.json.JSONArray
import org.json.JSONObject

/** References are validated after bodies load; corrupt references cannot swallow a child's item. */
object ChristmasStore {
    fun encode(s:ChristmasState)=JSONObject().apply {
        s.origin?.let { put("origin", it.place.name); put("originX", it.x.toDouble()) }
        put("opened",JSONArray(s.opened.toList()));put("deeds",JSONArray(s.deeds.toList()));put("stop",s.sleighStop);put("ramp",s.rampStage)
        put("parcels",JSONObject().apply { s.parcels.forEach { (k,v)->put(k.toString(),v) } })
        put("stockings",JSONObject().apply { s.stocking.forEach { (k,v)->put(k.toString(),v) } })
    }
    fun decode(world:World,o:JSONObject?) {
        if(o==null) return
        val s=world.christmas
        PlaceId.entries.firstOrNull { it.name == o.optString("origin") }?.let { place ->
            s.rememberOrigin(place, o.optDouble("originX", 1.0).toFloat())
        }
        o.optJSONArray("opened")?.let { a -> for(i in 0 until a.length()) a.optInt(i,-1).takeIf { it in 0..23 }?.let { s.opened+=it } }
        o.optJSONArray("deeds")?.let { a -> for(i in 0 until a.length()) a.optString(i).takeIf { it.length in 1..100 }?.let { s.deeds+=it } }
        s.rampStage=o.optInt("ramp",0).coerceIn(0,3)
        s.sleighStop=o.optInt("stop",0).coerceIn(0,3)
        val claimed=hashSetOf<Int>()
        fun load(key:String,type:ThingType,out:MutableMap<Int,Int>) {
            o.optJSONObject(key)?.let { map -> for(k in map.keys()) {
                val id=k.toIntOrNull() ?: continue
                val owner=world.bodies[id] as? Thing ?: continue
                val item=world.bodies[map.optInt(k,-1)] as? Thing ?: continue
                if(owner.type!=type || item.type in setOf(ThingType.XM_PARCEL,ThingType.XM_STOCKING) || item.id==id || item.id in claimed || item.mode!=Mode.INSIDE || item.holder!= -id-2) continue
                out[id]=item.id;claimed+=item.id
            } }
        }
        load("parcels",ThingType.XM_PARCEL,s.parcels);load("stockings",ThingType.XM_STOCKING,s.stocking)
        fun validate(item:Stored):Stored = item.copy(contents=item.contents.filter { id ->
            val body=world.bodies[id] as? Thing
            val valid=body!=null && body.mode==Mode.INSIDE && body.place==null && id !in claimed
            if(valid) claimed+=id
            valid
        })
        world.storage.indices.forEach {world.storage[it]=validate(world.storage[it])}
        world.discardedStorage.indices.forEach {i->val (slot,item)=world.discardedStorage[i];world.discardedStorage[i]=slot to validate(item)}

    }
}
