package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class PlayConnectionsTest {
    private fun person(w: World, place: PlaceId = PlaceId.CAFE) = w.addPerson(Species.FOLK, Look(), 1f, place, 0.8f, place.floor, "Hedda")
    private fun advance(s: Sim, place: PlaceId, seconds: Float) { repeat((seconds * 60).toInt()) { s.step(place, 1f / 60f) } }

    @Test fun rawFishIsRejectedThenTheSameFishCanBeGrilledAndEaten() {
        for (place in PlaceId.entries) {
            val w = World(); val s = Sim(w); val p = person(w, place)
            val f = Fixture(place.idBase + place.addedFrom, place, FixtureType.CAMPFIRE, 0.7f, place.floor)
            w.fixtures[f.id] = f
            val fish = w.addThing(ThingType.FISH, 0, place, p.x, p.y)
            assertTrue(PlayConnections.canTaste(fish.type))
            p.anim.talk = 1f
            p.anim.chew = 1f
            assertEquals(Give.YUCK, s.give(p, fish, Part.MOUTH))
            assertEquals(0, fish.used)
            assertEquals(Face.YUCK, p.anim.face)
            assertEquals(0f, p.anim.talk, 0.001f)
            assertEquals(0f, p.anim.chew, 0.001f)
            assertTrue(s.dropInto(place, f, fish))
            advance(s, place, 2.5f)
            assertEquals("$place", ThingType.GRILLED_FISH, fish.type)
            assertEquals(Give.ATE, s.give(p, fish, Part.MOUTH))
            assertEquals(Give.ATE, s.give(p, fish, Part.MOUTH))
            assertEquals(Give.FINISHED, s.give(p, fish, Part.MOUTH))
            assertFalse(w.bodies.containsKey(fish.id))
        }
    }

    @Test fun magicLastsTwentySecondsAndRestoresTheOriginalSizeAfterReload() {
        val w = World(); val s = Sim(w); val p = person(w)
        p.scale = 0.75f
        val mushroom = w.addThing(ThingType.MUSHROOM, 0, PlaceId.CAFE, 0f, 0f)
        assertEquals(Give.POTION, s.give(p, mushroom, Part.MOUTH))
        assertEquals(2.6f, p.scale, 0.001f)
        advance(s, PlaceId.CAFE, 10f)
        assertTrue(p.scaleTime > 9f)
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val friend = loaded.bodies[p.id] as Person
        assertEquals(2.6f, friend.scale, 0.001f)
        advance(Sim(loaded), PlaceId.CAFE, 11f)
        assertEquals(0.75f, friend.scale, 0.001f)
        assertEquals(0f, friend.scaleTime, 0.001f)
    }

    @Test fun repeatedMagicRefreshesTimeWithoutLosingOriginalSizeAndNormalPotionCancelsIt() {
        val w = World(); val s = Sim(w); val p = person(w)
        p.scale = 1.3f
        repeat(2) { s.give(p, w.addThing(ThingType.MUSHROOM, 0, PlaceId.CAFE, 0f, 0f), Part.MOUTH) }
        assertEquals(1.3f, p.scaleBefore, 0.001f)
        s.applyPotion(p, ThingType.POTION_NORMAL)
        advance(s, PlaceId.CAFE, 21f)
        assertEquals(1f, p.scale, 0.001f)
    }

    @Test fun invitingASeatedOrBaggedFriendPreservesTheirHatHandAndIdentity() {
        val w = WorldFactory.create(); val s = Sim(w)
        val p = w.people().first { it.name.isNotBlank() }
        val hat = w.addThing(ThingType.CROWN, 0, p.place, p.x, p.y)
        val hand = w.addThing(ThingType.APPLE, 0, p.place, p.x, p.y)
        s.give(p, hat, Part.HAT); s.give(p, hand, Part.HAND)
        val seat = w.fixturesIn(PlaceId.CAFE).first { it.spec.spots.isNotEmpty() }
        s.seat(p, seat, 0)
        val n = w.people().size
        assertTrue(PlayConnections.invite(s, p, PlaceId.VAGSTADDALEN, 3.3f))
        assertEquals(n, w.people().size)
        assertSame(p, w.bodies[p.id])
        assertEquals(Mode.FREE, p.mode)
        assertEquals(-1, p.holder)
        assertFalse(p.x in Vagstaddalen.RIVER_LEFT..Vagstaddalen.RIVER_RIGHT)
        assertEquals(PlaceId.VAGSTADDALEN, hat.place)
        assertEquals(p.id, hat.holder)
        assertEquals(p.id, hand.holder)
        p.mode = Mode.BAG; p.place = null
        assertTrue(PlayConnections.invite(s, p, PlaceId.HOME, 1f))
        assertEquals(PlaceId.HOME, p.place)
        assertFalse(w.bag().contains(p))
    }

    @Test fun tractorTravelsFurtherThanOldScriptCarriesRiderAndPushesWithoutDeleting() {
        val w = World(); val s = Sim(w); val place = PlaceId.CAFE
        val tractor = Fixture(place.idBase + 40, place, FixtureType.TRACTOR, 0.4f, place.floor)
        val table = Fixture(place.idBase + 41, place, FixtureType.TABLE, 1.1f, place.floor)
        w.fixtures[tractor.id] = tractor; w.fixtures[table.id] = table
        val rider = person(w); s.seat(rider, tractor, 0)
        val cake = w.addThing(ThingType.CAKE, 0, place, table.x, table.top)
        cake.resting = true; cake.restOwner = table.id; cake.ground = cake.y
        val before = w.bodies.size
        s.driveTractor(tractor, 1); advance(s, place, 3f)
        assertTrue(tractor.x > 1.6f)
        assertTrue(table.x > 1.1f)
        assertEquals(before, w.bodies.size)
        assertEquals(Mode.SEATED, rider.mode)
        assertEquals(s.seatPoint(tractor, 0)[0], rider.x, 0.001f)
        s.driveTractor(tractor, -1); advance(s, place, 1f)
        val stopped = tractor.x
        s.driveTractor(tractor, 0); advance(s, place, 1f)
        assertEquals(stopped, tractor.x, 0.001f)
        s.driveTractor(tractor, 1); advance(s, place, 10f)
        assertTrue(tractor.x + tractor.spec.w / 2f <= place.width)
        assertFalse(tractor.on)
    }

    @Test fun tractorDoesNotCrossTheRiverAndEveryPlaceCanUseTheSameControls() {
        for (place in PlaceId.entries.filter { !it.mine }) {
            val w = World(); val s = Sim(w)
            val tractor = Fixture(place.idBase + 40, place, FixtureType.TRACTOR, 0.8f, place.floor)
            w.fixtures[tractor.id] = tractor
            s.driveTractor(tractor, 1); advance(s, place, 1f)
            assertTrue("$place", tractor.x > 0.8f)
        }
        val w = World(); val s = Sim(w); val place = PlaceId.VAGSTADDALEN
        val tractor = Fixture(place.idBase + 40, place, FixtureType.TRACTOR, 2.4f, place.floor)
        w.fixtures[tractor.id] = tractor
        s.driveTractor(tractor, 1); advance(s, place, 5f)
        assertTrue(tractor.x + tractor.spec.w / 2f <= Vagstaddalen.RIVER_LEFT + 0.001f)
        assertFalse(tractor.on)
    }

    @Test fun tractorFromFarmStorageDrivesInBakeryAndKeepsItsPositionAfterReload() {
        val w = WorldFactory.create(); val s = Sim(w)
        val tractor = w.fixturesIn(PlaceId.FARM).first { it.type == FixtureType.TRACTOR }
        assertTrue(s.designer.store(PlaceId.FARM, tractor))
        val index = w.storage.indexOfFirst { it.type == FixtureType.TRACTOR }
        val moved = s.designer.unstore(PlaceId.CAFE, index, 0.5f, PlaceId.CAFE.floor)!!
        val before = moved.x
        s.driveTractor(moved, 1); advance(s, PlaceId.CAFE, 2f)
        assertTrue(moved.x > before + 0.6f)
        val position = moved.x
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val saved = loaded.fixtures[moved.id]!!
        val restarted = Sim(loaded)
        restarted.settle(PlaceId.CAFE)
        assertEquals(position, saved.x, 0.001f)
        assertFalse(saved.on)
        restarted.driveTractor(saved, -1); advance(restarted, PlaceId.CAFE, 1f)
        assertTrue(saved.x < position)
    }

    @Test fun secretRoomsOpenOnThreeKnocksOrKeyAndRewardsAreNotDuplicatedAfterReload() {
        val w = WorldFactory.create(); val s = Sim(w)
        for (place in PlaySecrets.nooks.keys) {
            val f = w.fixturesIn(place).first { it.type == FixtureType.SECRET_NOOK }
            assertTrue(place.indexOf(f.id) < place.addedFrom)
            s.tap(place, f, -0.1f, -0.3f); assertFalse(f.open)
            s.tap(place, f, -0.1f, -0.3f); assertFalse(f.open)
            s.tap(place, f, -0.1f, -0.3f); assertTrue(f.open)
            assertEquals(1, w.bodiesIn(place).count { it.inside == f.id })
            s.tap(place, f, 0.2f, -0.3f); assertFalse(f.open)
            val key = w.addThing(ThingType.GOLDEN_KEY, 0, place, f.x, f.y)
            s.dropInto(place, f, key); assertTrue(f.open)
            assertTrue(w.bodies.containsKey(key.id))
        }
        val saved = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val f = saved.fixturesIn(PlaceId.CAFE).first { it.type == FixtureType.SECRET_NOOK }
        val n = saved.bodies.size
        Sim(saved).tap(f.place, f, 0.2f, -0.3f)
        Sim(saved).tap(f.place, f, 0.2f, -0.3f)
        assertEquals(n, saved.bodies.size)
    }
}
