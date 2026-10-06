package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class AdventurePlayTest {
    private fun sim() = Sim(World()).also { s -> repeat(19) { s.world.stickers += it }; Progression.remember(s.world) }
    private fun toy(s: Sim, r: ToyReward, place: PlaceId = PlaceId.BEACH) = s.toys.claim(r, place, 1.2f)!!
    private fun step(s: Sim, place: PlaceId, sec: Float) = repeat((sec / 0.02f).toInt()) { s.step(place, 0.02f) }
    private fun person(s: Sim, place: PlaceId) = s.world.addPerson(Species.FOLK, Look(), 1f, place, 1.2f, place.floor)

    @Test fun level7OpensAtNineteenStickers() {
        val s = sim()
        assertEquals(7, Progression.level(s.world))
        val seven = ToyReward.entries.filter { it.level == 7 }
        assertEquals(4, seven.size)
        seven.forEach { assertTrue(Progression.unlocked(s.world, it)) }
    }

    @Test fun cableCarCarriesARiderToTheOtherTowerAndKeepsItsPositionAfterSaving() {
        val s = sim(); val f = toy(s, ToyReward.CABLE_CAR); val p = person(s, f.place)
        assertTrue(s.seat(p, f, 0))
        val startX = p.x
        s.tap(f.place, f, 0f, -0.2f); step(s, f.place, 4f)
        assertTrue(f.angle > 0.95f)
        assertEquals(Mode.SEATED, p.mode)
        assertTrue(p.x > startX + 0.5f)
        assertTrue("CABLE_CAR" in s.world.firsts)
        val back = WorldStore.decode(WorldStore.encode(s.world, Settings())).world
        assertEquals(f.angle, back.fixtures[f.id]!!.angle, 0.01f)
    }

    @Test fun divingBellBringsALooseThingUpFromRealWater() {
        val s = sim(); val place = PlaceId.BEACH; val pool = s.pools(place).first()
        val f = s.designer.add(place, FixtureType.PLAY_DIVING_BELL, 0, (pool.x1 + pool.x2) / 2, place.floor)!!
        assertTrue(s.adventure.overWater(f))
        val duck = s.world.addThing(ThingType.DUCK, 0, place, s.adventure.bellX(f), pool.line)
        s.tap(place, f, 0f, -0.2f); step(s, place, 4f)
        assertEquals(Mode.INSIDE, duck.mode); assertEquals(f.id, duck.holder)
        s.tap(place, f, 0f, -0.2f); step(s, place, 4f)
        assertTrue(f.angle < 0.05f)
        assertTrue("DIVING_BELL" in s.world.firsts)
    }

    @Test fun diggerFindsTreasureOutsideButNeverFloodsThePlace() {
        val s = sim(); val f = toy(s, ToyReward.DIGGER, PlaceId.BEACH)
        assertTrue(Vehicles.controllable(f))
        val found = (0 until 20).mapNotNull { s.vehicles.dig(f) }
        assertTrue(found.isNotEmpty())
        assertTrue(found.size <= 8)
        assertTrue("DIGGER" in s.world.firsts)
        val inside = toy(s, ToyReward.DIGGER, PlaceId.HOME)
        assertNull(s.vehicles.dig(inside))
    }

    @Test fun treasureTableNeedsThreeThingsKeepsThemAndRests() {
        val s = sim(); val f = toy(s, ToyReward.TREASURE_TABLE)
        val parts = (0 until 3).map { s.world.addThing(ThingType.APPLE, 0, f.place, f.x, f.y) }
        parts.take(2).forEach { assertTrue(s.dropInto(f.place, f, it)) }
        val before = s.world.bodies.size
        s.tap(f.place, f, 0f, -0.2f); assertEquals(before, s.world.bodies.size)
        assertTrue(s.dropInto(f.place, f, parts[2]))
        s.tap(f.place, f, 0f, -0.2f)
        assertEquals(before + 1, s.world.bodies.size)
        parts.forEach { assertSame(it, s.world.bodies[it.id]) }
        s.tap(f.place, f, 0f, -0.2f); assertEquals(before + 1, s.world.bodies.size)
        assertTrue("TREASURE_TABLE" in s.world.firsts)
    }

    @Test fun storingACableCarReleasesItsRider() {
        val s = sim(); val f = toy(s, ToyReward.CABLE_CAR); val p = person(s, f.place)
        s.seat(p, f, 0)
        assertTrue(s.designer.store(f.place, f))
        assertNotEquals(Mode.SEATED, p.mode)
    }
}
