package app.trollfoss.data

import app.trollfoss.domain.Body
import app.trollfoss.domain.Decor
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Places
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Stored
import app.trollfoss.domain.Look
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.Guest
import app.trollfoss.domain.Mine
import app.trollfoss.domain.SeasonChoice
import app.trollfoss.domain.Mode
import app.trollfoss.domain.Person
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Species
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType
import app.trollfoss.domain.Weather
import app.trollfoss.domain.World
import app.trollfoss.domain.WorldFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Grown-up choices, kept with the world. */
data class Settings(
    val sound: Boolean = true,
    val music: Boolean = true,
    val haptics: Boolean = true,
    val maalform: Maalform = Maalform.NYNORSK,
    /** Follow the calendar for seasons, or keep one all year. */
    val season: SeasonChoice = SeasonChoice.AUTO,
    /** Decorate the village for Christmas, Easter and pumpkin time. */
    val festive: Boolean = true,
)

class Saved(val world: World, val settings: Settings)

/**
 * The island in one JSON file. Writes go to a temporary file first and are moved into place, so a
 * crash mid-write never leaves a half-written save behind. An unreadable file is kept aside.
 */
class WorldStore(private val file: File) {

    fun load(): Saved? {
        if (!file.exists()) return null
        return try {
            decode(JSONObject(file.readText(Charsets.UTF_8)))
        } catch (error: Exception) {
            file.renameTo(File(file.parentFile, "${file.name}.broken-${System.currentTimeMillis()}"))
            null
        }
    }

    @Synchronized
    fun write(json: String) {
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeText(json, Charsets.UTF_8)
        try {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: Exception) {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    companion object {
        private const val VERSION = 1

        fun encode(world: World, settings: Settings): JSONObject = JSONObject().apply {
            put("version", VERSION)
            put("settings", JSONObject().apply {
                put("sound", settings.sound)
                put("music", settings.music)
                put("haptics", settings.haptics)
                put("maalform", settings.maalform.name)
                put("season", settings.season.name)
                put("festive", settings.festive)
            })
            put("place", world.place.name)
            put("night", world.night)
            put("weather", world.weather.name)
            put("nextId", world.nextId)
            put("z", world.zCounter)
            put("giftDay", world.giftDay)
            put("crownGiven", world.crownGiven)
            put("catches", world.catches)
            put("wishes", world.wishesGranted)
            // Which places this save knows, so places added by an update get filled on loading.
            put("places", JSONArray(PlaceId.entries.map { it.name }))
            put("found", JSONArray(world.found.toList()))
            put("unlocked", JSONArray(world.unlocked.toList()))
            put("flags", JSONArray(world.flags.toList()))
            put("players", JSONArray(world.playerIds.toList()))
            put("mine", JSONObject().apply {
                val h = world.mine
                put("started", h.started)
                put("shape", h.shape)
                put("wall", h.wall)
                put("roof", h.roof)
                put("roofColor", h.roofColor)
                put("door", h.door)
                put("windows", h.windows)
                put("chimney", h.chimney)
                put("flag", h.flag)
                put("upperBuilt", h.upperBuilt)
                put("ground", JSONArray(h.ground.toList()))
                put("upper", JSONArray(h.upper.toList()))
                put("guests", JSONArray().apply { h.guests.forEach { g -> put(JSONObject().put("id", g.id).put("place", g.place.name).put("x", g.x.toDouble())) } })
            })
            put("discoveries", JSONArray(world.discoveries.toList()))
            put("styles", JSONObject().apply { world.styles.forEach { (k, s) -> put(k, JSONArray(listOf(s.wall, s.floor))) } })
            put("storage", JSONArray().apply { world.storage.forEach { put(JSONObject().put("type", it.type.name).put("variant", it.variant)) } })
            put("discardedStorage", JSONArray().apply { world.discardedStorage.forEach { (index, item) -> put(JSONObject().put("index", index).put("type", item.type.name).put("variant", item.variant)) } })
            put("stickers", JSONArray(world.stickers))
            put("eggs", JSONArray(world.eggs.toList()))
            put("tasks", JSONObject().apply {
                put("set", JSONArray(world.taskSet))
                put("progress", JSONObject().apply { world.taskProgress.forEach { (k, v) -> put(k, v) } })
                put("seed", world.taskSeed)
                put("cursor", world.taskCursor)
            })
            // Furniture the child added from the catalogue, and blueprint furniture put away in the store.
            put("added", JSONArray().apply {
                world.fixtures.values.filter { it.place.indexOf(it.id) >= it.place.addedFrom }.forEach { f ->
                    put(JSONObject().apply {
                        put("id", f.id)
                        put("place", f.place.name)
                        put("type", f.type.name)
                        put("variant", f.variant)
                        put("x", f.x.toDouble())
                        put("y", f.y.toDouble())
                        put("depth", f.depth.toDouble())
                    })
                }
            })
            put("removed", JSONArray().apply {
                for (place in PlaceId.entries) {
                    Places.spec(place).fixtures.indices.map { WorldFactory.fixtureId(place, it) }.filter { it !in world.fixtures }.forEach { put(it) }
                }
            })
            put("fixtures", JSONArray().apply {
                world.fixtures.values.forEach { f ->
                    val moved = WorldFactory.moved(f)
                    if (f.open || f.on || f.mode != 0 || f.count != 0 || moved) {
                        put(JSONObject().apply {
                            put("id", f.id)
                            put("open", f.open)
                            put("on", f.on)
                            put("mode", f.mode)
                            put("count", f.count)
                            // Furniture the child has moved keeps its new spot.
                            if (moved) {
                                put("x", f.x.toDouble())
                                put("y", f.y.toDouble())
                                put("depth", f.depth.toDouble())
                            }
                        })
                    }
                }
            })
            put("bodies", JSONArray().apply { world.bodies.values.forEach { put(encodeBody(it)) } })
        }

        private fun encodeBody(b: Body): JSONObject = JSONObject().apply {
            put("id", b.id)
            b.place?.let { put("place", it.name) }
            put("x", b.x.toDouble())
            put("y", b.y.toDouble())
            // A body in a finger is saved where it is, falling.
            put("mode", (if (b.held) Mode.FREE else b.mode).name)
            put("holder", b.holder)
            put("slot", b.slot)
            put("z", b.z)
            put("rot", b.rot.toDouble())
            put("inside", b.inside)
            when (b) {
                is Thing -> {
                    put("kind", "thing")
                    put("type", b.type.name)
                    put("variant", b.variant)
                    put("used", b.used)
                    b.homePlace?.let { home ->
                        put("home", home.name)
                        put("homeOwner", b.homeOwner)
                        put("homeDx", b.homeDx.toDouble())
                        put("homeDy", b.homeDy.toDouble())
                        put("homeInside", b.homeInside)
                    }
                }
                is Person -> {
                    put("kind", "person")
                    put("species", b.species.name)
                    put("name", b.name)
                    put("voice", b.voice.toDouble())
                    put("scale", b.scale.toDouble())
                    put("scaleBefore", b.scaleBefore.toDouble())
                    put("scaleTime", b.scaleTime.toDouble())
                    put("floatTime", b.floatTime.toDouble())
                    put("look", JSONObject().apply {
                        put("skin", b.look.skin)
                        put("height", b.look.height.toDouble())
                        put("hair", b.look.hair)
                        put("hairColor", b.look.hairColor)
                        put("eyes", b.look.eyes)
                        put("ears", b.look.ears)
                        put("top", b.look.top)
                        put("topColor", b.look.topColor)
                        put("bottom", b.look.bottom)
                        put("bottomColor", b.look.bottomColor)
                        put("shoes", b.look.shoes)
                        put("extra", b.look.extra)
                        put("eyeColor", b.look.eyeColor)
                        put("hairSize", b.look.hairSize.toDouble())
                        put("hairLength", b.look.hairLength.toDouble())
                        put("eyeSize", b.look.eyeSize.toDouble())
                        put("eyeSpacing", b.look.eyeSpacing.toDouble())
                        put("face", b.look.face)
                        put("nose", b.look.nose)
                        put("mouth", b.look.mouth)
                        put("pattern", b.look.pattern)
                        put("accent", b.look.accent)
                    })
                }
            }
        }

        fun decode(json: JSONObject): Saved {
            val s = json.optJSONObject("settings") ?: JSONObject()
            val defaults = Settings()
            val settings = Settings(
                sound = s.optBoolean("sound", defaults.sound),
                music = s.optBoolean("music", defaults.music),
                haptics = s.optBoolean("haptics", defaults.haptics),
                maalform = enumOrNull<Maalform>(s.optString("maalform")) ?: defaults.maalform,
                season = enumOrNull<SeasonChoice>(s.optString("season")) ?: defaults.season,
                festive = s.optBoolean("festive", defaults.festive),
            )
            val world = World()
            WorldFactory.addFixtures(world)
            world.place = enumOrNull<PlaceId>(json.optString("place")) ?: PlaceId.HOME
            world.night = json.optBoolean("night", false)
            world.weather = enumOrNull<Weather>(json.optString("weather")) ?: Weather.SUN
            world.giftDay = json.optLong("giftDay", -1L)
            world.crownGiven = json.optBoolean("crownGiven", false)
            world.catches = json.optInt("catches", 0)
            world.wishesGranted = json.optInt("wishes", 0)
            world.found += strings(json.optJSONArray("found"))
            world.unlocked += strings(json.optJSONArray("unlocked"))
            world.flags += strings(json.optJSONArray("flags"))
            json.optJSONObject("mine")?.let { o ->
                val h = world.mine
                h.started = o.optBoolean("started", false)
                h.shape = o.optInt("shape", 0)
                h.wall = o.optInt("wall", 0)
                h.roof = o.optInt("roof", 0)
                h.roofColor = o.optInt("roofColor", 0)
                h.door = o.optInt("door", 0)
                h.windows = o.optInt("windows", 0)
                h.chimney = o.optBoolean("chimney", true)
                h.flag = o.optBoolean("flag", false)
                h.upperBuilt = o.optBoolean("upperBuilt", false)
                o.optJSONArray("ground")?.let { a -> for (i in 0 until minOf(a.length(), Mine.SLOTS)) h.ground[i] = a.optInt(i, 0) }
                o.optJSONArray("upper")?.let { a -> for (i in 0 until minOf(a.length(), Mine.SLOTS)) h.upper[i] = a.optInt(i, 0) }
                o.optJSONArray("guests")?.let { a ->
                    for (i in 0 until a.length()) {
                        val g = a.optJSONObject(i) ?: continue
                        val gp = enumOrNull<PlaceId>(g.optString("place")) ?: continue
                        h.guests += Guest(g.optInt("id", -1), gp, g.optDouble("x", 1.0).toFloat())
                    }
                }
            }
            Mine.sync(world)
            world.discoveries += strings(json.optJSONArray("discoveries"))

            json.optJSONArray("removed")?.let { removed -> for (i in 0 until removed.length()) world.fixtures.remove(removed.optInt(i, -1)) }
            json.optJSONArray("added")?.let { added ->
                for (i in 0 until added.length()) {
                    val o = added.optJSONObject(i) ?: continue
                    val place = enumOrNull<PlaceId>(o.optString("place")) ?: continue
                    val type = enumOrNull<FixtureType>(o.optString("type")) ?: continue
                    val y = o.optDouble("y", place.floor.toDouble()).toFloat()
                    val f = Fixture(o.optInt("id", -1), place, type, o.optDouble("x", 1.0).toFloat(), y, o.optInt("variant", 0), o.optDouble("depth", y.toDouble()).toFloat())
                    if (PlaceId.ofFixture(f.id) == place && place.indexOf(f.id) in place.addedFrom..place.addedMax) world.fixtures[f.id] = f
                }
            }
            json.optJSONObject("styles")?.let { styles ->
                for (key in styles.keys()) {
                    val a = styles.optJSONArray(key) ?: continue
                    world.styles[key] = RoomStyle(a.optInt(0, 0).coerceIn(0, Decor.WALLS - 1), a.optInt(1, 0).coerceIn(0, Decor.FLOORS - 1))
                }
            }
            json.optJSONArray("storage")?.let { storage ->
                for (i in 0 until storage.length()) {
                    val o = storage.optJSONObject(i) ?: continue
                    val type = enumOrNull<FixtureType>(o.optString("type")) ?: continue
                    world.storage += Stored(type, o.optInt("variant", 0))
                }
            }
            json.optJSONArray("stickers")?.let { s -> for (i in 0 until s.length()) world.stickers += s.optInt(i) }
            json.optJSONArray("discardedStorage")?.let { trash ->
                for (i in 0 until trash.length()) {
                    val o = trash.optJSONObject(i) ?: continue
                    val type = enumOrNull<FixtureType>(o.optString("type")) ?: continue
                    world.discardedStorage += o.optInt("index", 0).coerceAtLeast(0) to Stored(type, o.optInt("variant", 0))
                }
            }
            world.eggs += strings(json.optJSONArray("eggs"))
            json.optJSONObject("tasks")?.let { t ->
                world.taskSet += strings(t.optJSONArray("set"))
                t.optJSONObject("progress")?.let { p -> for (k in p.keys()) world.taskProgress[k] = p.optInt(k, 0) }
                world.taskSeed = t.optInt("seed", world.taskSeed)
                world.taskCursor = t.optInt("cursor", 0)
            }

            val fixtures = json.optJSONArray("fixtures") ?: JSONArray()
            for (i in 0 until fixtures.length()) {
                val o = fixtures.optJSONObject(i) ?: continue
                val f: Fixture = world.fixtures[o.optInt("id", -1)] ?: continue
                f.open = o.optBoolean("open", false) && f.spec.container != null
                f.on = o.optBoolean("on", false)
                f.mode = o.optInt("mode", 0)
                f.count = o.optInt("count", 0)
                if (o.has("x")) {
                    f.x = o.optDouble("x", f.x.toDouble()).toFloat()
                    f.y = o.optDouble("y", f.y.toDouble()).toFloat()
                    f.depth = o.optDouble("depth", f.depth.toDouble()).toFloat()
                }
            }

            var maxId = 0
            val bodies = json.optJSONArray("bodies") ?: JSONArray()
            for (i in 0 until bodies.length()) {
                val o = bodies.optJSONObject(i) ?: continue
                val body = decodeBody(o) ?: continue
                maxId = maxOf(maxId, body.id)
                world.bodies[body.id] = body
            }
            world.nextId = maxOf(json.optInt("nextId", 1), maxId + 1)
            world.zCounter = maxOf(json.optLong("z", 0L), world.bodies.values.maxOfOrNull { it.z } ?: 0L)
            json.optJSONArray("players")?.let { players ->
                for (i in 0 until players.length()) {
                    val id = players.optInt(i, -1)
                    if ((world.bodies[id] as? Person)?.species == Species.FOLK) world.playerIds.add(id)
                }
            }

            // Anything that refers to something that is gone falls free where it was.
            for (b in world.bodies.values) {
                val valid = when (b.mode) {
                    Mode.SEATED, Mode.INSIDE -> world.fixtures[b.holder] != null
                    Mode.WORN -> world.bodies[b.holder] is Person
                    else -> true
                }
                if (!valid) {
                    b.mode = Mode.FREE
                    b.holder = -1
                }
                if (b.inside >= 0 && world.fixtures[b.inside] == null) b.inside = -1
                if (b.mode != Mode.BAG && b.mode != Mode.WORN && b.place == null) b.place = PlaceId.HOME
                b.age = 10f
            }
            // Belongings stay with their owner, including when the owner is packed in the bag.
            for (b in world.bodies.values) {
                if (b.mode == Mode.WORN) b.place = (world.bodies[b.holder] as Person).place
            }
            val known = json.optJSONArray("places")?.let { a -> strings(a).mapNotNull { enumOrNull<PlaceId>(it) }.toSet() }
                ?: world.bodies.values.mapNotNull { it.place }.toSet()
            // Guests of a housewarming that was cut short by closing the app go home.
            Mine.returnGuests(world)
            Mine.syncFixtures(world)
            WorldFactory.addMissingPlaces(world, known)
            return Saved(world, settings)
        }

        private fun decodeBody(o: JSONObject): Body? {
            val id = o.optInt("id", -1).takeIf { it > 0 } ?: return null
            val body: Body = when (o.optString("kind")) {
                "thing" -> {
                    val type = enumOrNull<ThingType>(o.optString("type")) ?: return null
                    Thing(id, type, o.optInt("variant", 0)).apply {
                        used = o.optInt("used", 0).coerceIn(0, when (type) {
                            ThingType.CUP, ThingType.BUCKET, ThingType.WATERING_CAN -> 1
                            ThingType.BOOK -> 3
                            else -> maxOf(0, type.bites - 1)
                        })
                        enumOrNull<PlaceId>(o.optString("home"))?.let { home ->
                            homePlace = home
                            homeOwner = o.optInt("homeOwner", -1)
                            homeDx = o.optDouble("homeDx", 0.0).toFloat()
                            homeDy = o.optDouble("homeDy", 0.0).toFloat()
                            homeInside = o.optBoolean("homeInside", false)
                        }
                    }
                }
                "person" -> {
                    val species = enumOrNull<Species>(o.optString("species")) ?: return null
                    val l = o.optJSONObject("look") ?: JSONObject()
                    val look = Look(
                        skin = l.optInt("skin", 2),
                        height = l.optDouble("height", 1.03).toFloat(),
                        hair = l.optInt("hair", 1),
                        hairColor = l.optInt("hairColor", 1),
                        eyes = l.optInt("eyes", 0),
                        ears = l.optInt("ears", 0),
                        top = l.optInt("top", 0),
                        topColor = l.optInt("topColor", 0),
                        bottom = l.optInt("bottom", 0),
                        bottomColor = l.optInt("bottomColor", 11),
                        shoes = l.optInt("shoes", 9),
                        extra = l.optInt("extra", 0),
                        eyeColor = l.optInt("eyeColor", 0),
                        hairSize = l.optDouble("hairSize", 1.0).toFloat(),
                        hairLength = l.optDouble("hairLength", 1.0).toFloat(),
                        eyeSize = l.optDouble("eyeSize", 1.0).toFloat(),
                        eyeSpacing = l.optDouble("eyeSpacing", 1.0).toFloat(),
                        face = l.optInt("face", 0),
                        nose = l.optInt("nose", 0),
                        mouth = l.optInt("mouth", 0),
                        pattern = l.optInt("pattern", 0),
                        accent = l.optInt("accent", 9),
                    ).safe()
                    Person(id, species, look, o.optDouble("voice", 1.0).toFloat().coerceIn(0.6f, 1.8f), o.optString("name", "").take(24)).apply {
                        scaleTime = o.optDouble("scaleTime", 0.0).toFloat().coerceIn(0f, 20f)
                        scaleBefore = o.optDouble("scaleBefore", 1.0).toFloat().coerceIn(0.5f, 1.7f)
                        scale = o.optDouble("scale", 1.0).toFloat().coerceIn(0.5f, if (scaleTime > 0f) 2.6f else 1.7f)
                        floatTime = o.optDouble("floatTime", 0.0).toFloat().coerceIn(0f, 20f)
                    }
                }
                else -> return null
            }
            body.place = enumOrNull<PlaceId>(o.optString("place"))
            body.x = o.optDouble("x", 1.0).toFloat()
            body.y = o.optDouble("y", 0.9).toFloat()
            body.mode = enumOrNull<Mode>(o.optString("mode")) ?: Mode.FREE
            body.holder = o.optInt("holder", -1)
            body.slot = o.optInt("slot", 0)
            body.z = o.optLong("z", 0L)
            body.rot = o.optDouble("rot", 0.0).toFloat()
            body.inside = o.optInt("inside", -1)
            body.place?.let { place -> body.x = body.x.coerceIn(0f, place.width) }
            body.y = body.y.coerceIn(0f, 1.2f)
            return body
        }

        private fun strings(array: JSONArray?): List<String> =
            if (array == null) emptyList() else (0 until array.length()).mapNotNull { array.optString(it, "").ifBlank { null } }

        private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? = enumValues<T>().firstOrNull { it.name == name }
    }
}
