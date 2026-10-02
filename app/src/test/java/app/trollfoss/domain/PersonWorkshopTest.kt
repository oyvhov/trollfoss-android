package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class PersonWorkshopTest {
    @Test fun everyNewAppearanceFieldSurvivesSavingAndEditingAnExistingNamedFriend() {
        val w = WorldFactory.create()
        val p = w.people().first { it.name == "Hedda" }
        val look = p.look.copy(hair = 17, hairColor = 13, hairSize = 1.47f, hairLength = 1.55f,
            eyeColor = 8, eyeSize = 1.24f, eyeSpacing = 0.81f, face = 3, nose = 2, mouth = 4,
            top = 11, bottom = 4, pattern = 5, accent = 7, extra = 8)
        p.look = look
        val hat = w.worn(p, Slot.HEAD)
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val same = loaded.bodies[p.id] as Person
        assertEquals("Hedda", same.name)
        assertEquals(look, same.look)
        assertEquals(p.place, same.place)
        assertEquals(hat?.id, loaded.worn(same, Slot.HEAD)?.id)
    }

    @Test fun oldAppearanceHasSafeDefaultsWithoutLosingClothesHairOrName() {
        val w = World(); val p = w.addPerson(Species.FOLK, Look(hair = 5, eyes = 3, top = 6), 1f, PlaceId.LAB, 1f, 0.9f, "Rumle")
        val json = WorldStore.encode(w, Settings())
        val bodies = json.getJSONArray("bodies")
        val look = (0 until bodies.length()).map { bodies.getJSONObject(it) }.first { it.getInt("id") == p.id }.getJSONObject("look")
        for (field in listOf("eyeColor", "hairSize", "hairLength", "eyeSize", "eyeSpacing", "face", "nose", "mouth", "pattern", "accent")) look.remove(field)
        val loaded = WorldStore.decode(json).world.bodies[p.id] as Person
        assertEquals(p.look, loaded.look)
        assertEquals("Rumle", loaded.name)
    }

    @Test fun malformedNumbersAndStyleIndicesCannotMakeDrawingInvalid() {
        val safe = Look(height = Float.NaN, hairSize = Float.POSITIVE_INFINITY, hairLength = -40f,
            eyeSize = Float.NaN, eyeSpacing = 100f, hair = -8, eyeColor = -10, face = 50, nose = 50, pattern = 50).safe()
        assertTrue(safe.height.isFinite()); assertEquals(1f, safe.hairSize, 0f)
        assertEquals(0.65f, safe.hairLength, 0f); assertEquals(1.2f, safe.eyeSpacing, 0f)
        assertTrue(safe.hair in 0 until Styles.HAIRS)
        assertTrue(safe.eyeColor in Palette.eyes.indices)
        assertEquals(safe, safe.safe())
        repeat(200) { assertEquals(Look.random(Random(it)).safe(), Look.random(Random(it))) }
    }

    @Test fun expandedClothesStillFitTheExistingGarmentEncoding() {
        for (style in 0 until Styles.TOPS) for (color in Palette.cloth.indices) {
            val packed = Garment.pack(style, color)
            assertEquals(style, Garment.style(packed)); assertEquals(color, Garment.color(packed))
        }
    }
}
