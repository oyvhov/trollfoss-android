package app.trollfoss.ui.play

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Run on Android: exercises the real pointer engine used by children sharing the screen. */
@RunWith(AndroidJUnit4::class)
class SharedPlayTest {
    @Test fun explicitPackingAndInvitingEachHaveAnUndoEntry() {
        var w=World();val s=Sim(w);val p=w.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,0.7f,0.9f)
        w.playerIds+=p.id
        val h=app.trollfoss.data.WorldHistory({ w });s.journal=h
        val e=Engine(w,PlaceId.HOME,s,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        e.packPerson(p);assertEquals(Mode.BAG,p.mode);w=h.undo()!!
        assertEquals(Mode.FREE,w.bodies[p.id]?.mode);assertTrue(p.id in w.playerIds)
        val next=Sim(w);next.journal=h
        val other=Engine(w,PlaceId.HOME,next,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        other.invite(w.bodies[p.id] as Person);assertTrue(h.available)
        val restored=h.undo()!!;assertEquals(0.7f,restored.bodies[p.id]!!.x,0f)
    }
    @Test fun longPressFurnitureMoveCanBeUndoneWithoutEnteringTheDesigner() {
        val w=World();val s=Sim(w);val f=s.toys.claim(ToyReward.PUMP,PlaceId.HOME,0.8f)!!
        val h=app.trollfoss.data.WorldHistory({ w });s.journal=h
        val e=Engine(w,PlaceId.HOME,s,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        val x=f.x;val tap=Offset((x-e.cam)*e.u,1200f-e.u+(f.y-0.14f)*e.u)
        e.down(1,tap,1000);repeat(60) { e.update(0.016f) }
        e.move(1,tap+Offset(230f,0f),2000);repeat(20) { e.update(0.016f) };e.up(1,tap+Offset(230f,0f),2400)
        assertTrue(f.x>x);assertTrue(h.available);assertEquals(x,h.undo()!!.fixtures[f.id]!!.x,0f)
    }
    @Test fun undoWaitsForBothFingersAndRestoresBothOriginalFigures() {
        val w=World();val s=Sim(w);val h=app.trollfoss.data.WorldHistory({ w });s.journal=h
        val a=w.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,0.7f,0.9f)
        val b=w.addPerson(Species.FOLK,Look(hair=6),1f,PlaceId.HOME,1.4f,0.9f)
        val e=Engine(w,PlaceId.HOME,s,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        val pa=finger(e,a);val pb=finger(e,b)
        e.down(1,pa,1000);e.down(2,pb,1001);e.move(1,pa+Offset(180f,0f),1100);e.move(2,pb+Offset(160f,0f),1101)
        repeat(12) { e.update(0.016f) }
        e.up(1,pa+Offset(180f,0f),1400);assertTrue(e.touching);assertFalse(h.available)
        e.up(2,pb+Offset(160f,0f),1401);assertFalse(e.touching);assertTrue(h.available)
        val restored=h.undo()!!;assertEquals(0.7f,restored.bodies[a.id]!!.x,0f);assertEquals(1.4f,restored.bodies[b.id]!!.x,0f)
        assertEquals(6,(restored.bodies[b.id] as Person).look.hair)
    }
    @Test fun realToyTapOpensItsControlsAndRealDragKeepsOriginalCargo() {
        val w=World();val s=Sim(w)
        val f=s.toys.claim(ToyReward.PUMP,PlaceId.HOME,0.8f)!!
        val e=Engine(w,PlaceId.HOME,s,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        val tap=Offset((f.x-e.cam)*e.u,1200f-e.u+(f.y-0.14f)*e.u)
        e.down(1,tap,1000);e.up(1,tap,1100);assertEquals(f.id,e.toyFixtureId);e.toyFixtureId=-1
        val ball=w.addThing(ThingType.BALL,0,PlaceId.HOME,1.3f,0.9f)
        val from=Offset((ball.x-e.cam)*e.u,1200f-e.u+(ball.y-ball.h/2)*e.u)
        e.down(2,from,1200);e.move(2,tap,1350);repeat(16) { e.update(0.016f) };e.up(2,tap,1700)
        assertEquals(ThingType.BEACH_BALL,ball.type);assertSame(ball,w.bodies[ball.id]);assertEquals(Mode.FREE,ball.mode)
    }
    @Test fun cartHandlesUseTwoIndependentFingersAndCancelClearsBoth() {
        val world = World(); val sim = Sim(world)
        sim.magic.kit(PlayRecipe.CART, PlaceId.HOME, 1.1f)
        val parts = world.playKits.getValue(PlayRecipe.CART).map { world.bodies[it] as Thing }
        parts.forEach { it.x = 1.1f; it.y = PlaceId.HOME.floor }
        val cart = requireNotNull(sim.magic.combine(parts.last()))
        val e = Engine(world, PlaceId.HOME, sim, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        fun handle(side: Float) = Offset((cart.x + side*0.36f - e.cam)*e.u, 1200f - e.u + (cart.y-0.15f)*e.u)
        val left = handle(-1f); val right = handle(1f); val x = cart.x
        e.down(1, left, 1000); e.move(1,left+Offset(40f,0f),1100)
        assertEquals(x, cart.x,0.001f)
        e.down(2,right,1101); e.move(1,left+Offset(120f,0f),1200)
        e.move(2,right+Offset(80f,0f),1201)
        assertTrue(cart.x > x); assertEquals(2,cart.count)
        e.up(1,left+Offset(120f,0f),1300); assertEquals(1,cart.count)
        val after = cart.x; e.move(2,right+Offset(100f,0f),1400)
        assertEquals(after,cart.x,0.001f); e.cancel(); assertEquals(0,cart.count)
    }

    @Test fun aRealDragCombinesPartsAndTapOffersMultipleUses() {
        val world = World(); val sim = Sim(world)
        val e = Engine(world,PlaceId.HOME,sim,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        val sheet = world.addThing(ThingType.AT_SHEET_HAT,0,PlaceId.HOME,1.0f,0.9f)
        val pillow = world.addThing(ThingType.PILLOW,0,PlaceId.HOME,1.4f,0.9f)
        val from = Offset((pillow.x-e.cam)*e.u,1200f-e.u+(pillow.y-pillow.h/2)*e.u)
        e.down(1,from,1000); e.up(1,from,1100); assertEquals(pillow.id,e.playThingId)
        e.playThingId = -1
        e.down(1,from,1200)
        val to = Offset((sheet.x-e.cam)*e.u,from.y)
        e.move(1,to,1400); repeat(20) { e.update(0.016f) }; e.up(1,to,1700)
        assertTrue(world.fixtures.values.any { it.type == FixtureType.PLAY_FORT })
        assertEquals(Mode.INSIDE,pillow.mode); assertEquals(Mode.INSIDE,sheet.mode)
    }
    @Test fun draggingAPlayerIntoTheBagAlsoStopsFollowing() {
        val world = World()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        Players.toggle(world, p)
        val e = engine(world)
        e.down(1, finger(e, p), 1000)
        val bag = Offset(1833f, 1113f)
        e.move(1, bag, 1100)
        e.up(1, bag, 1400)
        assertEquals(Mode.BAG, p.mode)
        assertNull(p.place)
        assertTrue(world.playerIds.isEmpty())
        assertSame(p, world.bodies[p.id])
    }
    @Test fun explicitPackRemovesPersonFromRoomAndTeamButInviteBringsSameFigureBack() {
        val world = World()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        Players.toggle(world, p)
        val e = engine(world)
        e.packPerson(p)
        assertSame(p, world.bodies[p.id])
        assertEquals(1, e.bagCount)
        assertEquals(Mode.BAG, p.mode)
        assertNull(p.place)
        assertTrue(world.playerIds.isEmpty())
        Players.arrive(world, PlaceId.BEACH, 1f)
        assertEquals(Mode.BAG, p.mode)
        e.invite(p)
        assertEquals(PlaceId.HOME, p.place)
        assertEquals(Mode.FREE, p.mode)
        assertFalse(p.id in world.playerIds)
    }
    private val host = object : EngineHost {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id: String) {}
        override fun discovered(key: String) {}
        override fun telescope() {}
        override fun radio(on: Boolean) {}
        override fun egg(id: String) {}
        override fun passage(passage: Passage, arrivalX: Float) {}
    }

    private fun engine(world: World) = Engine(world, PlaceId.HOME, Sim(world), host, false, 0f)
        .apply { setSize(1920f, 1200f, 1.5f) }
    private fun finger(e: Engine, p: Person) = Offset((p.x - e.cam) * e.u, 1200f - e.u + (p.y - p.h / 2f) * e.u)

    @Test fun twoFingersCanMoveDifferentFiguresAndReleaseIndependently() {
        val world = World()
        val p1 = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        val p2 = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1.4f, 0.9f, "B")
        val e = engine(world)
        val a = finger(e, p1)
        val b = finger(e, p2)
        e.down(1, a, 1000)
        e.down(2, b, 1001)
        e.move(1, a + Offset(100f, -80f), 1100)
        e.move(2, b + Offset(-100f, -80f), 1101)
        repeat(10) { e.update(0.016f) }
        assertTrue(p1.held)
        assertTrue(p2.held)
        assertTrue(p1.x > 0.7f)
        assertTrue(p2.x < 1.4f)
        e.up(1, a + Offset(100f, -80f), 1300)
        assertFalse(p1.held)
        assertTrue(p2.held)
        e.cancel()
        assertFalse(p2.held)
    }

    @Test fun secondFingerCannotStealAnAlreadyGrabbedFigure() {
        val world = World()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        val e = engine(world)
        val a = finger(e, p)
        e.down(1, a, 1000)
        e.down(2, a, 1001)
        e.move(1, a + Offset(100f, -80f), 1100)
        e.move(2, a + Offset(-100f, 80f), 1101)
        repeat(10) { e.update(0.016f) }
        assertTrue(p.x > 0.7f)
        e.up(2, a, 1200)
        assertTrue(p.held)
        e.cancel()
    }

    @Test fun cancellingTheOldRoomAfterAStairNeverPacksAnArrivingPlayerIntoTheBag() {
        val world = World()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        Players.toggle(world, p)
        val e = engine(world)
        val a = finger(e, p)
        e.down(1, a, 1000)
        e.move(1, Offset(1840f, 1120f), 1100)
        assertTrue(p.held)
        Players.arrive(world, PlaceId.MANOR_UPPER, 1f)
        e.cancel()
        assertEquals(PlaceId.MANOR_UPPER, p.place)
        assertEquals(Mode.FREE, p.mode)
        assertEquals(1f, p.x, 0.0001f)
    }

    @Test fun draggingFromCataloguePlacesOnlyOnceAtTheDropAndCancellingAddsNothing() {
        val world = World()
        val e = engine(world).apply {
            designMode = true
            setSize(1920f, 1200f, 1.5f, 400f)
            storeZone = Rect(1520f, 0f, 1920f, 1200f)
        }
        e.beginCatalogueDrag(FixtureType.CHAIR, 2, Offset(1700f, 700f))
        e.moveCatalogueDrag(Offset(600f, 950f))
        assertTrue(world.fixtures.isEmpty())
        e.finishCatalogueDrag()
        assertEquals(1, world.fixtures.size)
        val chair = world.fixtures.values.single()
        assertEquals(2, chair.variant)
        assertTrue(chair.x in 0.4f..1.2f)
        e.finishCatalogueDrag()
        assertEquals(1, world.fixtures.size)
        e.beginCatalogueDrag(FixtureType.BED, 0, Offset(1700f, 700f))
        e.moveCatalogueDrag(Offset(500f, 900f))
        e.cancelCatalogueDrag()
        e.finishCatalogueDrag()
        assertEquals(1, world.fixtures.size)
    }

    @Test fun storageItemStaysAvailableIfReleasedOverThePanel() {
        val world = World()
        world.storage.add(Stored(FixtureType.CHAIR, 3))
        val e = engine(world).apply { setSize(1920f, 1200f, 1.5f, 400f); storeZone = Rect(1520f, 0f, 1920f, 1200f) }
        e.beginCatalogueDrag(FixtureType.CHAIR, 3, Offset(1700f, 700f), 0)
        e.finishCatalogueDrag()
        assertEquals(1, world.storage.size)
        assertTrue(world.fixtures.isEmpty())
        e.beginCatalogueDrag(FixtureType.CHAIR, 3, Offset(1700f, 700f), 0)
        e.moveCatalogueDrag(Offset(700f, 950f))
        e.finishCatalogueDrag()
        assertTrue(world.storage.isEmpty())
        assertEquals(3, world.fixtures.values.single().variant)
    }

    @Test fun choosingAnotherRoomBringsTheTeamAndTheirBelongings() {
        val world = World()
        val people = listOf(0.7f, 1.2f).map { x -> world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, x, 0.9f) }
        people.forEach { Players.toggle(world, it) }
        val hat = world.addThing(ThingType.PARTY_HAT, 1, PlaceId.HOME, 0f, 0f).apply {
            mode = Mode.WORN; holder = people[0].id; slot = Slot.HEAD.ordinal
        }
        val e = engine(world)
        e.selectRoom(3)
        people.forEach { assertEquals(3, Decor.roomAt(PlaceId.HOME, it.x)) }
        assertEquals(Mode.WORN, hat.mode)
        assertEquals(people[0].id, hat.holder)
        assertEquals(PlaceId.HOME, hat.place)
    }

    @Test fun panningToAnotherRoomBringsThePlayersAfterTheCameraSettles() {
        val world = World()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f)
        Players.toggle(world, p)
        val e = engine(world)
        val a = Offset(1600f, 350f)
        e.down(1, a, 1000)
        e.move(1, Offset(150f, 350f), 1100)
        e.up(1, Offset(150f, 350f), 1600)
        repeat(300) { e.update(0.016f) }
        assertTrue(p.x in e.cam..(e.cam + e.visibleViewport))
        assertEquals(Decor.roomAt(PlaceId.HOME, e.cam + e.visibleViewport / 2f), Decor.roomAt(PlaceId.HOME, p.x))
        assertTrue(p.x > 1.5f)
    }
}
