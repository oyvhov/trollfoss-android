package app.trollfoss.data

import app.trollfoss.domain.BergEcho
import app.trollfoss.domain.EchoNote
import app.trollfoss.domain.BerryPot
import app.trollfoss.domain.BergItem
import app.trollfoss.domain.HighlandsState
import org.json.JSONArray
import org.json.JSONObject

internal object HighlandsStore {
    fun encode(s:HighlandsState)=JSONObject().apply {
        put("x",s.x.toDouble());put("z",s.z.toDouble());put("cave",s.cave);put("yaw",s.yaw.toDouble())
        put("leader",s.leader);put("light",s.light);put("treasure",s.treasure);put("shake",s.shake)
        put("treasureSlot",s.treasureSlot)
        put("echoSong",JSONArray().apply { s.song.takeLast(BergEcho.LIMIT).forEach { put(JSONArray(listOf(it.tone,it.beats))) } })
        put("berryPot",s.pot.name);put("sharedJuice",s.sharedJuice);put("lastBottle",s.lastBottle.name);put("mothFriend",s.mothFriend);put("springAwake",s.springAwake)
        put("taken",s.taken);put("held",s.held.name);put("bag",JSONArray(s.bag.map { it.name }))
    }
    fun decode(s:HighlandsState,o:JSONObject?) {
        if(o==null) return
        s.x=o.optDouble("x",0.0).toFloat();s.z=o.optDouble("z",18.0).toFloat()
        s.cave=o.optBoolean("cave",false);s.yaw=o.optDouble("yaw",0.0).toFloat();s.leader=o.optInt("leader",-1)
        s.light=o.optBoolean("light",false);s.treasure=o.optBoolean("treasure",false);s.shake=o.optBoolean("shake",true)
        s.song.clear()
        o.optJSONArray("echoSong")?.let { song ->
            for(i in (song.length()-BergEcho.LIMIT).coerceAtLeast(0) until song.length()) {
                val note=song.optJSONArray(i) ?: continue
                val tone=note.optInt(0,-1)
                if(tone in BergEcho.pitches.indices) s.song.add(EchoNote(tone,note.optInt(1,3).coerceIn(2,7)))
            }
        }
        s.pot=BerryPot.entries.find { it.name==o.optString("berryPot") } ?: BerryPot.EMPTY
        s.lastBottle=listOf(BergItem.PEPSI,BergItem.URGE).find { it.name==o.optString("lastBottle") } ?: BergItem.NONE
        s.mothFriend=o.optBoolean("mothFriend",false)
        s.springAwake=o.optBoolean("springAwake",false)
        s.sharedJuice=o.optBoolean("sharedJuice",false)
        s.treasureSlot=o.optInt("treasureSlot",0)
        s.taken=o.optInt("taken",0);s.held=BergItem.entries.find { it.name==o.optString("held") } ?: BergItem.NONE
        o.optJSONArray("bag")?.let { a -> for(i in 0 until a.length()) BergItem.entries.find { it.name==a.optString(i) }?.let { s.bag.add(it) } }
        s.repair()
    }
}
