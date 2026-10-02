package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class PlayInteractionsTest {
    @Test fun fillCarryExtinguishRefillDrinkAndRelightAcrossPlaces() {
        val w = World(); val s = Sim(w)
        val sink = Fixture(40, PlaceId.HOME, FixtureType.SINK, 1f, 0.9f)
        val fire = Fixture(140, PlaceId.CAFE, FixtureType.CAMPFIRE, 1f, 0.9f)
        w.fixtures[sink.id] = sink; w.fixtures[fire.id] = fire
        val cup = w.addThing(ThingType.CUP, 0, PlaceId.HOME, 1f, 0.7f)
        assertFalse(PlayInteractions.water(cup))
        s.dropInto(PlaceId.HOME, sink, cup)
        assertTrue(PlayInteractions.water(cup))
        assertTrue(PlayConnections.canTaste(cup))
        cup.place = PlaceId.CAFE
        fire.on = true
        s.dropInto(PlaceId.CAFE, fire, cup)
        assertFalse(fire.on)
        assertFalse(PlayInteractions.water(cup))
        val wood = w.addThing(ThingType.PLANK, 0, PlaceId.CAFE, 1f, 0.8f)
        s.dropInto(PlaceId.CAFE, fire, wood)
        assertTrue(fire.on)
        assertSame(wood, w.bodies[wood.id])
        s.dropInto(PlaceId.HOME, sink, cup)
        val saved = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val restoredCup = saved.bodies[cup.id] as Thing
        assertTrue(PlayInteractions.water(restoredCup))
        val p = saved.addPerson(Species.FOLK, Look(), 1f, PlaceId.CAFE, 0.5f, 0.9f)
        assertEquals(Give.DRANK, Sim(saved).give(p, restoredCup, Part.MOUTH))
        assertFalse(PlayInteractions.water(restoredCup))
        assertSame(restoredCup, saved.bodies[cup.id])
    }

    @Test fun everyPortableConnectionHasATargetAndKeepsTools() {
        for (place in PlaceId.entries) {
            val w = World(); val s = Sim(w)
            val cases = listOf(
                FixtureType.SINK to ThingType.BUCKET, FixtureType.BATH to ThingType.DUCK,
                FixtureType.WOOD_STOVE to ThingType.WATERING_CAN, FixtureType.PLANT_BIG to ThingType.SEEDS,
                FixtureType.PIANO to ThingType.GUITAR, FixtureType.RADIO to ThingType.DRUM,
                FixtureType.MIRROR to ThingType.AT_FLASHLIGHT,
            )
            for ((index, pair) in cases.withIndex()) {
                val f = Fixture(place.idBase + place.addedFrom + index, place, pair.first, 1f, place.floor)
                w.fixtures[f.id] = f
                val t = w.addThing(pair.second, 0, place, 1f, place.floor)
                assertTrue("$place $pair", PlayInteractions.accepts(f, t))
                assertNotNull(PlayInteractions.zone(f, t))
                assertTrue(PlayInteractions.apply(s, place, f, t))
                assertSame(t, w.bodies[t.id])
            }
        }
    }

    @Test fun musicMakesOnlyNearbyFriendsDanceAndMirrorRewardIsOnce() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val piano = Fixture(40, place, FixtureType.PIANO, 1f, 0.9f)
        val near = w.addPerson(Species.FOLK, Look(), 1f, place, 1.4f, 0.9f)
        val far = w.addPerson(Species.FOLK, Look(), 1f, place, 3f, 0.9f)
        val guitar = w.addThing(ThingType.GUITAR, 0, place, 1f, 0.8f)
        PlayInteractions.apply(s, place, piano, guitar)
        assertEquals(3f, near.anim.dance, 0.001f)
        assertEquals(3f, near.anim.cheer, 0.001f)
        assertEquals(0f, far.anim.dance, 0.001f)
        val mirror = Fixture(41, place, FixtureType.MIRROR, 1f, 0.5f)
        val light = w.addThing(ThingType.AT_FLASHLIGHT, 0, place, 1f, 0.5f)
        repeat(3) { PlayInteractions.apply(s, place, mirror, light) }
        assertEquals(1, w.eggs.count { it == "mirror_light" })
        assertEquals(1, w.stickers.size)
    }

    @Test fun invitationsStayInBuiltRoomsAndDoNotGetReturnedByAnOldParty() {
        val w = World(); val s = Sim(w)
        val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1f, 0.9f)
        assertFalse(PlayConnections.invite(s, p, PlaceId.MINE_GROUND, 9f))
        assertEquals(PlaceId.HOME, p.place)
        w.mine.started = true
        w.mine.ground[0] = 1
        w.mine.guests.add(Guest(p.id, PlaceId.HOME, 1f))
        assertTrue(PlayConnections.invite(s, p, PlaceId.MINE_GROUND, 9f))
        assertTrue(p.x < Mine.SLOT_W)
        assertTrue(w.mine.guests.isEmpty())
        Mine.returnGuests(w)
        assertEquals(PlaceId.MINE_GROUND, p.place)
    }
}
