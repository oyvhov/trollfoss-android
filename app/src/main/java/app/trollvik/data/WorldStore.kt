package app.trollvik.data

import app.trollvik.domain.Body
import app.trollvik.domain.Fixture
import app.trollvik.domain.Look
import app.trollvik.domain.Maalform
import app.trollvik.domain.Mode
import app.trollvik.domain.Person
import app.trollvik.domain.PlaceId
import app.trollvik.domain.Species
import app.trollvik.domain.Thing
import app.trollvik.domain.ThingType
import app.trollvik.domain.Weather
import app.trollvik.domain.World
import app.trollvik.domain.WorldFactory
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
            })
            put("place", world.place.name)
            put("night", world.night)
            put("weather", world.weather.name)
            put("nextId", world.nextId)
            put("z", world.zCounter)
            put("giftDay", world.giftDay)
            put("crownGiven", world.crownGiven)
            put("catches", world.catches)
            put("found", JSONArray(world.found.toList()))
            put("unlocked", JSONArray(world.unlocked.toList()))
            put("discoveries", JSONArray(world.discoveries.toList()))
            put("fixtures", JSONArray().apply {
                world.fixtures.values.forEach { f ->
                    if (f.open || f.on || f.mode != 0 || f.count != 0) {
                        put(JSONObject().apply {
                            put("id", f.id)
                            put("open", f.open)
                            put("on", f.on)
                            put("mode", f.mode)
                            put("count", f.count)
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
                }
                is Person -> {
                    put("kind", "person")
                    put("species", b.species.name)
                    put("voice", b.voice.toDouble())
                    put("scale", b.scale.toDouble())
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
            )
            val world = World()
            WorldFactory.addFixtures(world)
            world.place = enumOrNull<PlaceId>(json.optString("place")) ?: PlaceId.HOME
            world.night = json.optBoolean("night", false)
            world.weather = enumOrNull<Weather>(json.optString("weather")) ?: Weather.SUN
            world.giftDay = json.optLong("giftDay", -1L)
            world.crownGiven = json.optBoolean("crownGiven", false)
            world.catches = json.optInt("catches", 0)
            world.found += strings(json.optJSONArray("found"))
            world.unlocked += strings(json.optJSONArray("unlocked"))
            world.discoveries += strings(json.optJSONArray("discoveries"))

            val fixtures = json.optJSONArray("fixtures") ?: JSONArray()
            for (i in 0 until fixtures.length()) {
                val o = fixtures.optJSONObject(i) ?: continue
                val f: Fixture = world.fixtures[o.optInt("id", -1)] ?: continue
                f.open = o.optBoolean("open", false) && f.spec.container != null
                f.on = o.optBoolean("on", false)
                f.mode = o.optInt("mode", 0)
                f.count = o.optInt("count", 0)
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
                if (b.mode != Mode.BAG && b.place == null) b.place = PlaceId.HOME
                b.age = 10f
            }
            return Saved(world, settings)
        }

        private fun decodeBody(o: JSONObject): Body? {
            val id = o.optInt("id", -1).takeIf { it > 0 } ?: return null
            val body: Body = when (o.optString("kind")) {
                "thing" -> {
                    val type = enumOrNull<ThingType>(o.optString("type")) ?: return null
                    Thing(id, type, o.optInt("variant", 0)).apply { used = o.optInt("used", 0).coerceIn(0, maxOf(0, type.bites - 1)) }
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
                    ).safe()
                    Person(id, species, look, o.optDouble("voice", 1.0).toFloat().coerceIn(0.6f, 1.8f)).apply {
                        scale = o.optDouble("scale", 1.0).toFloat().coerceIn(0.5f, 1.7f)
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
