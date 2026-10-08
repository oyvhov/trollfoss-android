package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.data.WorldHistory
import app.trollfoss.ui.art.FallProgress
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class FinalePlayTest {
    private val place = PlaceId.HOME
    private fun sim() = Sim(World(), random = Random(14)).also { s -> s.here = place; repeat(40) { s.world.stickers += it }; Progression.remember(s.world) }
    private fun toy(s: Sim, t: FixtureType) = s.designer.add(place, t, 0, 1.1f, place.floor)!!
    private fun step(s: Sim, seconds: Float) = repeat((seconds / 0.02f).toInt()) { s.step(place, 0.02f) }
    private fun folk(s: Sim) = s.world.addPerson(Species.FOLK, Look(), 1f, place, 1.4f, place.floor)
    private fun reload(s: Sim) = Sim(WorldStore.decode(WorldStore.encode(s.world, Settings())).world).also { it.here = place }

    @Test fun fortyStickersOpenFourToysAndTheRainbowAndNeverSpendStickers() {
        val s = sim(); assertEquals(10, Progression.level(s.world))
        val rewards = ToyReward.entries.filter { it.level == 10 }
        assertEquals(4, rewards.size)
        rewards.forEach { assertTrue(Progression.unlocked(s.world, it)) }
        assertTrue(FallProgress.complete(40, Progression.thresholds))
        assertFalse(FallProgress.complete(39, Progression.thresholds))
        assertFalse("nine complete levels are not a finale", FallProgress.complete(90, Progression.thresholds.dropLast(1)))
        s.world.stickers.remove(39)
        assertEquals(9, Progression.level(s.world))
        rewards.forEach { assertTrue("earned rights remain", Progression.unlocked(s.world, it)) }
        assertEquals(39, s.world.stickers.size)
    }

    @Test fun dragonCarriesTwoOriginalFriendsWithTheirClothesAndStopsAtTheEdge() {
        val s = sim(); val f = toy(s, FixtureType.PLAY_DRAGON_CART)
        val a = folk(s); val b = folk(s)
        val crown = s.world.addThing(ThingType.CROWN, 0, place, a.x, a.y)
        s.give(a, crown, Part.HAT)
        s.world.playerIds.addAll(listOf(a.id,b.id))
        assertTrue(s.seat(a,f,0)); assertTrue(s.seat(b,f,1))
        val begin = f.x; s.vehicles.drive(f,1); step(s,0.6f)
        assertTrue(f.x > begin+0.2f)
        assertEquals(s.seatPoint(f,0)[0],a.x,0.02f); assertEquals(s.seatPoint(f,1)[0],b.x,0.02f)
        assertEquals(a.id,crown.holder); assertEquals(Mode.WORN,crown.mode)
        val once = s.world.stickers.size; step(s,15f)
        assertFalse(f.on); assertTrue(f.x <= place.width-f.spec.w/2)
        assertEquals(once,s.world.stickers.size); assertTrue(First.DRAGON_CART.name in s.world.firsts)
        val back = reload(s)
        assertEquals(listOf(a.id,b.id),back.world.playerIds.toList())
        assertEquals(f.id,back.world.bodies[a.id]!!.holder)
        assertEquals(a.id,back.world.bodies[crown.id]!!.holder)
        assertFalse(back.world.fixtures[f.id]!!.on)
    }

    @Test fun airshipMovesItsPassengersAndOneCargoInBothDirectionsAndReloadsAtRest() {
        val s = sim(); val f = toy(s,FixtureType.PLAY_AIRSHIP)
        val a = folk(s); val b = folk(s)
        assertTrue(s.seat(a,f,0)); assertTrue(s.seat(b,f,1))
        val t = s.world.addThing(ThingType.TEDDY,0,place,f.x,f.top)
        val other = s.world.addThing(ThingType.APPLE,0,place,f.x,f.top)
        assertTrue(s.toys.drop(f,t)); assertFalse(s.toys.drop(f,other))
        val y = f.y; val x = f.x
        s.vehicles.dive(f,-1); step(s,0.4f)
        assertTrue(f.y < y-0.08f); assertEquals(f.y+FinalePlay.CARGO_Y,t.y,0.001f)
        assertEquals(place.floor,f.depth,0f)
        s.vehicles.drive(f,1); step(s,0.5f)
        assertTrue(f.x>x+0.2f); assertEquals(f.x+FinalePlay.CARGO_X,t.x,0.001f)
        assertEquals(s.seatPoint(f,1)[1],b.y,0.02f)
        val back = reload(s); val saved = back.world.fixtures[f.id]!!
        assertEquals(f.x,saved.x,0.001f); assertEquals(f.y,saved.y,0.001f)
        assertEquals(Mode.INSIDE,back.world.bodies[t.id]!!.mode)
        assertFalse(saved.on); assertEquals(0,saved.mode)
        assertTrue(First.AIRSHIP.name in back.world.firsts)
        assertTrue(back.designer.store(place,saved))
        for(id in listOf(a.id,b.id,t.id)) { assertEquals(Mode.FREE,back.world.bodies[id]!!.mode); assertEquals(-1,back.world.bodies[id]!!.holder) }
        assertTrue(back.designer.discard(back.world.storage.lastIndex))
        assertTrue(back.designer.undoDiscard())
        val restored = back.designer.unstore(PlaceId.BEACH,back.world.storage.lastIndex,1.3f,0.82f)!!
        assertEquals(FixtureType.PLAY_AIRSHIP,restored.type)
        assertTrue(back.world.inMachine(restored).isEmpty())
        assertEquals(Mode.FREE,other.mode)
    }

    @Test fun airshipCanCrossWaterAndItsWholeBalloonStaysOnScreenInEveryPlace() {
        for(p in PlaceId.entries.filterNot { it.mine }) {
            val s = sim(); val f = s.designer.add(p,FixtureType.PLAY_AIRSHIP,0,p.width/2,p.floor)!!
            s.vehicles.dive(f,-1)
            repeat(200) { s.step(p,0.02f) }
            assertEquals("${p.name} balloon rises to its ceiling limit",p.ceiling+0.02f,f.top,0.001f)
            s.vehicles.dive(f,1); repeat(250) { s.step(p,0.02f) }
            assertTrue(f.y <= PlaceId.FRONT)
        }
    }

    @Test fun hidingHasNoDeadlineOrPenaltyAndOneDiscoveryAfterThreeFinds() {
        val s = sim(); val f = toy(s,FixtureType.PLAY_HIDE_TROLL)
        s.tap(place,f,0f,-0.06f)
        repeat(3) {
            val hole = f.angle.toInt()-1
            val wrong = (hole+1)%3
            s.tap(place,f,(wrong-1)*FinalePlay.HOLE_STEP,-0.14f)
            step(s,30f)
            assertEquals(hole+1f,f.angle,0f)
            s.tap(place,f,(hole-1)*FinalePlay.HOLE_STEP,-0.14f)
        }
        assertTrue(First.HIDE_TROLL.name in s.world.firsts); assertFalse(f.on)
        val total = s.world.stickers.size
        s.tap(place,f,0f,-0.2f); repeat(3) { s.tap(place,f,0f,FinalePlay.ASSIST_Y) }
        assertEquals(total,s.world.stickers.size)
    }

    @Test fun replacementAndReloadNeverReuseAnOldHidingGameOrParty() {
        val s = sim(); val f = toy(s,FixtureType.PLAY_HIDE_TROLL)
        s.tap(place,f,0f,-0.1f); assertTrue(f.on)
        assertTrue(s.designer.store(place,f))
        val another = toy(s,FixtureType.PLAY_HIDE_TROLL)
        assertEquals(f.id,another.id); step(s,0.1f); assertFalse(another.on)
        assertFalse(s.finale.tap(f))
        val party = toy(s,FixtureType.PLAY_TROLL_PARTY); s.tap(place,party,0f,-0.1f)
        val back = reload(s)
        assertFalse(back.world.fixtures[party.id]!!.on); assertFalse(back.musicOn(place))
    }

    @Test fun undoKeepsTheNewDiscoveryAndLevelTenRights() {
        val s = sim(); val history = WorldHistory({ s.world }); s.journal = history
        val f = toy(s,FixtureType.PLAY_AIRSHIP)
        s.vehicles.dive(f,-1); step(s,0.2f)
        val earned = s.world.stickers.toList()
        val back = history.undo()!!
        assertNotEquals(FixtureType.PLAY_AIRSHIP,back.fixtures[f.id]?.type)
        assertTrue(First.AIRSHIP.name in back.firsts)
        assertEquals(earned,back.stickers.toList())
        ToyReward.entries.filter { it.level==10 }.forEach { assertTrue(Progression.unlocked(back,it)) }
    }

    @Test fun partyRespectsHeldSleepingAndSeatedFriendsAndStopsWithoutMutingOtherMusic() {
        val s = sim(); val f = toy(s,FixtureType.PLAY_TROLL_PARTY)
        val a = folk(s); val held = folk(s).also { it.held = true }; val asleep = folk(s).also { it.anim.pose = Pose.LIE }
        val seated = folk(s); val chair = toy(s,FixtureType.CHAIR); s.seat(seated,chair,0)
        s.tap(place,f,0f,-0.1f)
        assertTrue(s.musicOn(place)); assertTrue(s.finale.dances(a))
        assertFalse(s.finale.dances(held)); assertFalse(s.finale.dances(asleep)); assertFalse(s.finale.dances(seated))
        assertEquals(0f,held.anim.wave,0f); assertEquals(0f,asleep.anim.wave,0f)
        s.finale.step(f,FinalePlay.PARTY_TIME+0.01f)
        assertFalse(f.on); assertFalse(s.musicOn(place)); assertFalse(s.finale.dances(a))
        val radio = toy(s,FixtureType.RADIO).also { it.on = true }
        val count = s.world.stickers.size
        s.tap(place,f,0f,-0.1f); s.tap(place,f,0f,-0.1f)
        assertFalse(f.on); assertTrue(s.musicOn(place)); assertTrue(radio.on)
        assertEquals(count,s.world.stickers.size)
    }
}
