package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class VagstaddalenTest {
    @Test fun newPlaceDoesNotBecomePartOfMittHusOrOverwriteExistingFurniture() {
        val p = PlaceId.VAGSTADDALEN
        assertFalse(p.mine)
        assertFalse(p.big)
        assertTrue(p.onMap)
        val ids = PlaceId.entries.flatMap { place -> (place.idBase until place.idBase + if (place.big) 300 else 100).toList() }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(p, PlaceId.ofFixture(WorldFactory.fixtureId(p, 0)))
        assertEquals(PlaceId.MINE_UPPER, PlaceId.ofFixture(3899))
    }

    @Test fun riverFloatsADuckAndCabinDoorsOpen() {
        val w = WorldFactory.create()
        val p = PlaceId.VAGSTADDALEN
        val s = Sim(w)
        val cabin = w.fixtures[WorldFactory.fixtureId(p, Vagstaddalen.CABIN)]!!
        s.tap(p, cabin, 0f, -0.15f)
        assertTrue(cabin.open)
        val duck = w.bodiesIn(p).filterIsInstance<Thing>().first { it.type == ThingType.DUCK }
        repeat(180) { s.step(p, 1f / 60f) }
        assertTrue(duck.y >= Vagstaddalen.WATERLINE - 0.03f && duck.y < Vagstaddalen.BED)
        assertEquals(3, Secrets.all.count { it.place == p })
    }

    @Test fun oldSaveGetsValleyWithoutChangingTheChildsHouse() {
        val w = WorldFactory.create()
        w.mine.started = true
        w.mine.wall = 4
        val json = WorldStore.encode(w, Settings())
        val known = org.json.JSONArray(PlaceId.entries.filter { it != PlaceId.VAGSTADDALEN }.map { it.name })
        json.put("places", known)
        val oldIds = w.bodiesIn(PlaceId.VAGSTADDALEN).map { it.id }.toSet()
        val bodies = json.getJSONArray("bodies")
        for (i in bodies.length() - 1 downTo 0) if (bodies.getJSONObject(i).getInt("id") in oldIds) bodies.remove(i)
        val loaded = WorldStore.decode(json).world
        assertTrue(loaded.mine.started)
        assertEquals(4, loaded.mine.wall)
        assertTrue(loaded.bodiesIn(PlaceId.VAGSTADDALEN).isNotEmpty())
        assertTrue(loaded.fixturesIn(PlaceId.VAGSTADDALEN).any { it.type == FixtureType.MOUNTAIN_HUT })
    }
}
