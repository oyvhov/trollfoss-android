package app.trollfoss.data

import app.trollfoss.domain.*
import org.json.JSONArray
import org.json.JSONObject

/** Small runtime journal. Undo patches changed entities into today's world, retaining all earned progress. */
class WorldHistory(private val world: () -> World, private val changed: () -> Unit = {}) : EditJournal {
    private data class Entry(val before: JSONObject, val after: JSONObject, val watched: Set<Int>)
    private val entries = ArrayDeque<Entry>()
    private var before: JSONObject? = null
    private var depth = 0
    private val watched = mutableSetOf<Int>()
    val available get() = entries.isNotEmpty() && depth == 0

    override fun begin(body: Int?) {
        if (depth++ == 0) { before = snapshot(); watched.clear() }
        body?.let { id ->
            watched += id
            watched += world().bodies.values.filter { it.holder == id || it.restOwner == id || it.inside == id }.map { it.id }
        }
    }
    override fun end() {
        if (depth <= 0 || --depth != 0) return
        val old = before ?: return
        before = null
        val entry = Entry(old, snapshot(), watched.toSet())
        if (patch(entry, JSONObject(entry.after.toString()))) {
            entries.addLast(entry)
            while (entries.size > 8) entries.removeFirst()
        }
        changed()
    }
    override fun clear() { entries.clear(); before = null; depth = 0; watched.clear(); changed() }
    fun undo(): World? {
        if (!available) return null
        val entry = entries.removeLast()
        val now = snapshot()
        patch(entry, now)
        val restored = WorldStore.decode(now).world
        restored.nextId = maxOf(restored.nextId, world().nextId)
        restored.zCounter = maxOf(restored.zCounter, world().zCounter)
        // No fingers or running vehicle commands survive replacement of the scene.
        restored.bodies.values.forEach { it.held = false }
        restored.fixtures.values.filter { Vehicles.controllable(it) }.forEach { it.on = false; it.mode = 0 }
        changed()
        return restored
    }
    private fun snapshot() = WorldStore.encode(world(), Settings())
    private fun rows(j: JSONObject, key: String): LinkedHashMap<Int, JSONObject> = linkedMapOf<Int, JSONObject>().apply {
        val array = j.optJSONArray(key) ?: return@apply
        for (i in 0 until array.length()) array.optJSONObject(i)?.let { put(it.getInt("id"), it) }
    }
    private fun canonical(value: Any?): String = when (value) {
        is JSONObject -> value.keys().asSequence().toList().sorted().joinToString(prefix="{",postfix="}") { it + ":" + canonical(value.opt(it)) }
        is JSONArray -> (0 until value.length()).joinToString(prefix="[",postfix="]") { canonical(value.opt(it)) }
        else -> value.toString()
    }
    private fun structuralBody(j: JSONObject): String {
        val copy = JSONObject(j.toString())
        listOf("x", "y", "z", "rot").forEach(copy::remove)
        return canonical(copy)
    }
    private fun patch(e: Entry, now: JSONObject): Boolean {
        var edited = false
        for (key in listOf("bodies", "fixtures", "added")) {
            val old = rows(e.before,key); val after = rows(e.after,key); val current = rows(now,key)
            for (id in old.keys + after.keys) {
                val a=old[id]; val b=after[id]
                if (canonical(a)==canonical(b)) continue
                if (key=="bodies" && a!=null && b!=null && id !in e.watched && structuralBody(a)==structuralBody(b)) continue
                // A reward earned while editing remains earned, with its one saved copy.
                if (key=="bodies" && a==null && b?.optString("mode")=="BAG") continue
                if (a==null) current.remove(id) else current[id]=JSONObject(a.toString())
                edited=true
            }
            now.put(key,JSONArray(current.values.toList()))
        }
        val oldRemoved=e.before.getJSONArray("removed"); val afterRemoved=e.after.getJSONArray("removed")
        fun ids(a:JSONArray)=(0 until a.length()).map { a.getInt(it) }.toSet()
        val a=ids(oldRemoved);val b=ids(afterRemoved);val current=ids(now.getJSONArray("removed")).toMutableSet()
        if(a!=b) { current.removeAll(b-a);current.addAll(a-b);now.put("removed",JSONArray(current.toList()));edited=true }
        for(key in listOf("storage","discardedStorage","styles","play","toys","players")) if(canonical(e.before.opt(key))!=canonical(e.after.opt(key))) {
            now.put(key,e.before.opt(key) ?: JSONObject.NULL);edited=true
        }
        // Repair state belongs to the edited train; earned story steps and entitlement stay earned.
        fun flags(j:JSONObject)=j.getJSONArray("flags").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
        val oldBroken=flags(e.before).filter { it.startsWith("train:broken:") }.toSet()
        val afterBroken=flags(e.after).filter { it.startsWith("train:broken:") }.toSet()
        if(oldBroken!=afterBroken) {
            val currentFlags=flags(now).toMutableSet();currentFlags.removeAll(afterBroken-oldBroken);currentFlags.addAll(oldBroken-afterBroken)
            now.put("flags",JSONArray(currentFlags.toList()));edited=true
        }
        val mineKeys=listOf("started","ground","upper","upperBuilt","shape","wall","roof","roofColor","door","windows","chimney","flag")
        val oldMine=e.before.getJSONObject("mine");val afterMine=e.after.getJSONObject("mine")
        if(mineKeys.any { canonical(oldMine.opt(it))!=canonical(afterMine.opt(it)) }) {
            val currentMine=now.getJSONObject("mine")
            mineKeys.filter { canonical(oldMine.opt(it))!=canonical(afterMine.opt(it)) }.forEach { currentMine.put(it,oldMine.opt(it)) }
            val flags=(0 until now.getJSONArray("flags").length()).map { now.getJSONArray("flags").getString(it) }.toMutableSet()
            flags.removeAll { it==Mine.FLAG_STARTED || it=="mine_upper" }
            val oldFlags=e.before.getJSONArray("flags")
            (0 until oldFlags.length()).map { oldFlags.getString(it) }.filter { it==Mine.FLAG_STARTED || it=="mine_upper" }.forEach { flags+=it }
            now.put("flags",JSONArray(flags.toList()));edited=true
        }
        return edited
    }
}
