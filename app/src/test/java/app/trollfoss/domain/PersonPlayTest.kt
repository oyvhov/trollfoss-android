package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class PersonPlayTest {
    private fun friend(w: World, x: Float, place: PlaceId = PlaceId.CAFE) = w.addPerson(Species.FOLK, Look(), 1f, place, x, place.floor, "Venn")
    private fun advance(s: Sim, place: PlaceId, seconds: Float) { repeat((seconds * 60).toInt()) { s.step(place, 1f / 60f) } }

    @Test fun booksAreReadAndKeptWithTheSameFriendAfterTravelAndReload() {
        val w = World(); val s = Sim(w); val p = friend(w, 0.8f)
        val book = w.addThing(ThingType.BOOK, 2, p.place, p.x, p.y)
        assertEquals(Give.HELD, s.give(p, book, Part.HAND))
        assertEquals(PersonPlay.READ, p.anim.activity); assertEquals(1, book.used)
        val activeHand = Anatomy.at(p, Part.HAND)
        assertEquals(p.y - p.h * 0.38f, activeHand[1], 0.0001f)
        s.use(PlaceId.CAFE, book); assertEquals(2, book.used)
        assertTrue(PlayConnections.invite(s, p, PlaceId.LAB, 1f))
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        val same = loaded.bodies[p.id] as Person
        assertEquals(book.id, loaded.worn(same, Slot.HAND)?.id)
        assertEquals(2, (loaded.bodies[book.id] as Thing).used)
        assertEquals(0, same.anim.activity)
    }

    @Test fun twoPhonesConnectFriendsAcrossRoomsWithoutMovingThemOrTheirBelongings() {
        val w = World(); val s = Sim(w); val p = friend(w, 0.7f); val q = friend(w, 1.2f, PlaceId.LAB)
        val phone = w.addThing(ThingType.PHONE, 0, p.place, p.x, p.y)
        val other = w.addThing(ThingType.PHONE, 0, q.place, q.x, q.y)
        s.give(p, phone, Part.HAND); s.give(q, other, Part.HAND); s.use(PlaceId.CAFE, phone)
        assertEquals(PersonPlay.PHONE, p.anim.activity); assertEquals(PersonPlay.PHONE, q.anim.activity)
        assertTrue(p.anim.talk > 0); assertTrue(q.anim.talk > 0)
        assertEquals(PlaceId.LAB, q.place); assertSame(other, w.worn(q, Slot.HAND))
    }

    @Test fun aBallCanBeThrownCaughtAndThrownBackWithoutDuplication() {
        val w = World(); val s = Sim(w); val p = friend(w, 0.7f); val q = friend(w, 1.3f)
        s.settle(PlaceId.CAFE)
        val ball = w.addThing(ThingType.BALL, 0, p.place, p.x, p.y)
        s.give(p, ball, Part.HAND); s.use(PlaceId.CAFE, ball)
        assertEquals(Mode.FREE, ball.mode)
        advance(s, PlaceId.CAFE, 0.65f)
        assertSame(ball, w.worn(q, Slot.HAND))
        s.use(PlaceId.CAFE, ball); advance(s, PlaceId.CAFE, 0.65f)
        assertSame(ball, w.worn(p, Slot.HAND))
        assertEquals(1, w.bodies.values.filterIsInstance<Thing>().count())
    }

    @Test fun teddyToothbrushAndFeatherGiveDifferentRepeatableReactionsAndStayAvailable() {
        val w = World(); val s = Sim(w); val p = friend(w, 1f)
        val teddy = w.addThing(ThingType.TEDDY, 0, p.place, 1f, 0.9f)
        s.give(p, teddy, Part.HAND); assertEquals(PersonPlay.HUG, p.anim.activity)
        val brush = w.addThing(ThingType.TOOTHBRUSH, 0, p.place, 1f, 0.9f)
        assertEquals(Give.PLAY, s.give(p, brush, Part.MOUTH)); assertEquals(PersonPlay.BRUSH, p.anim.activity)
        val feather = w.addThing(ThingType.FEATHER, 0, p.place, 1f, 0.9f)
        assertEquals(Give.PLAY, s.give(p, feather, Part.BODY)); assertEquals(Face.LAUGH, p.anim.face)
        assertSame(brush, w.worn(p, Slot.HAND)); assertTrue(w.bodies.containsKey(teddy.id)); assertTrue(w.bodies.containsKey(feather.id))
    }

    @Test fun anInstrumentInAHandMakesNearbyFriendsDance() {
        val w = World(); val s = Sim(w); val p = friend(w, 0.8f); val q = friend(w, 1.2f)
        val guitar = w.addThing(ThingType.GUITAR, 0, p.place, p.x, p.y)
        s.give(p, guitar, Part.HAND)
        assertTrue(p.anim.dance > 0f); assertTrue(q.anim.dance > 0f)
        assertSame(guitar, w.worn(p, Slot.HAND))
    }

    @Test fun movedFurnitureUsesToolsInEveryPlaceAndNeverConsumesThem() {
        for (place in PlaceId.entries) {
            val w = World(); val s = Sim(w)
            val castle = Fixture(place.idBase + place.addedFrom, place, FixtureType.SANDCASTLE, 1f, place.floor)
            val lamp = Fixture(castle.id + 1, place, FixtureType.LAMP, 1.5f, place.floor)
            val tv = Fixture(castle.id + 2, place, FixtureType.TV, 2f, place.floor)
            w.fixtures[castle.id] = castle; w.fixtures[lamp.id] = lamp; w.fixtures[tv.id] = tv
            val spade = w.addThing(ThingType.SPADE, 0, place, 0f, 0f)
            repeat(4) { s.dropInto(place, castle, spade) }; assertEquals(4, castle.mode)
            val bucket = w.addThing(ThingType.BUCKET, 0, place, 0f, 0f).apply { used = 1 }
            s.dropInto(place, castle, bucket); assertEquals(0, castle.mode); assertEquals(0, bucket.used)
            val wrench = w.addThing(ThingType.WRENCH, 0, place, 0f, 0f)
            s.dropInto(place, lamp, wrench); assertTrue(lamp.on)
            val phone = w.addThing(ThingType.PHONE, 0, place, 0f, 0f)
            s.dropInto(place, tv, phone); assertTrue(tv.on); assertEquals(1, tv.mode)
            assertEquals(4, w.bodies.size)
            val saved = WorldStore.decode(WorldStore.encode(w, Settings())).world
            assertEquals(1, saved.fixtures[tv.id]?.mode); assertTrue(saved.fixtures[lamp.id]?.on == true)
        }
    }

    @Test fun bathingWashesTheSeatedFriendAndTrampolineLaunchesTheSameBall() {
        val w = World(); val s = Sim(w); val place = PlaceId.CAFE; val p = friend(w, 1f)
        val bath = Fixture(place.idBase + 40, place, FixtureType.BATH, 1f, place.floor)
        val trampoline = Fixture(place.idBase + 41, place, FixtureType.TRAMPOLINE, 1.6f, place.floor)
        w.fixtures[bath.id] = bath; w.fixtures[trampoline.id] = trampoline
        s.seat(p, bath, 0); p.anim.cream = 1f; p.anim.ink = 1f
        s.tap(place, bath, 0f, 0f); assertEquals(0f, p.anim.cream, 0f); assertEquals(0f, p.anim.ink, 0f)
        val ball = w.addThing(ThingType.BALL, 0, place, trampoline.x, trampoline.y)
        assertTrue(s.dropInto(place, trampoline, ball)); assertTrue(ball.vy < -2f)
        assertSame(ball, w.bodies[ball.id])
    }

    @Test fun theCaveHasOneSecretTunnelAndAnUnrelatedStairCannotUseIt() {
        val w = WorldFactory.create(); val door = w.fixturesIn(PlaceId.LAB).first { it.type == FixtureType.SECRET_DOOR }
        val passage = House.passageAt(door)!!
        assertEquals(PassageKind.SECRET, passage.kind); assertEquals(PlaceId.MANOR_CELLAR, passage.to)
        val stair = Fixture(door.id, PlaceId.LAB, FixtureType.STAIRCASE, door.x, door.y)
        assertNull(House.passageAt(stair))
        val added = Fixture(PlaceId.LAB.idBase + PlaceId.LAB.addedFrom, PlaceId.LAB, FixtureType.SECRET_DOOR, 1f, 0.9f)
        assertNull(House.passageAt(added))
    }
}
