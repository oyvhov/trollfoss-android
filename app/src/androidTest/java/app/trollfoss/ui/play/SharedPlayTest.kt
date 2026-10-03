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
    @Test fun openFridgeAndWardrobeDoorsCloseWhereTheDoorIsDrawn() {
        for ((type, dx) in listOf(FixtureType.FRIDGE to -0.18f, FixtureType.WARDROBE to -0.19f,
            FixtureType.WARDROBE to 0.15f, FixtureType.OVEN to -0.18f)) {
            val w = World(); val s = Sim(w); val place = PlaceId.HOME
            val f = s.designer.add(place, type, 0, 1f, 0.91f)!!.apply { open = true }
            // A neighbour behind the swung-out door must not take the tap.
            s.designer.add(place, FixtureType.PIANO, 0, f.x - 0.25f, 0.85f)
            val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
            val y = if (type == FixtureType.OVEN) -0.1f else -0.2f
            val at = Offset((f.x + dx - e.cam) * e.u, 1200f - e.u + (f.y + y) * e.u)
            e.down(1, at, 1000); e.up(1, at, 1100)
            assertFalse("$type closes on its visible open door", f.open)
            e.cancel()
        }
    }

    @Test fun tractorFromStorageStartsOnItsRoofOnStageAndDrivesBothWaysWithItsRider() {
        val w = WorldFactory.create(); val s = Sim(w); val place = PlaceId.STAGE
        val farmTractor = w.fixturesIn(PlaceId.FARM).first { it.type == FixtureType.TRACTOR }
        assertTrue(s.designer.store(PlaceId.FARM, farmTractor))
        val f = s.designer.unstore(place, w.storage.indexOfFirst { it.type == FixtureType.TRACTOR }, 1.4f, 0.97f)!!
        val rider = w.addPerson(Species.FOLK, Look(), 1f, place, f.x, f.y)
        assertTrue(s.seat(rider, f, 0))
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val roof = Offset((f.x - 0.04f - e.cam) * e.u, 1200f - e.u + (f.y - 0.525f) * e.u)
        e.down(1, roof, 1000); e.up(1, roof, 1100)
        assertEquals(f.id, e.vehicleId); assertTrue(f.on)
        e.drive(1)
        val start = f.x
        repeat(30) { e.update(0.016f) }
        assertTrue(f.x > start + 0.1f)
        e.drive(-1); val right = f.x
        repeat(30) { e.update(0.016f) }
        assertTrue(f.x < right - 0.1f)
        assertEquals(Mode.SEATED, rider.mode); assertEquals(f.id, rider.holder)
        e.drive(0); val stopped = f.x
        repeat(10) { e.update(0.016f) }
        assertEquals(stopped, f.x, 0.001f); e.cancel()
    }

    @Test fun aHiddenThingBehindTheTractorCannotStealTheStartTap() {
        val w = World(); val s = Sim(w); val place = PlaceId.STAGE
        val f = s.designer.add(place, FixtureType.TRACTOR, 0, 1f, 0.98f)!!
        val t = w.addThing(ThingType.DRUM, 0, place, f.x + 0.1f, f.y - 0.12f).apply {
            resting = true; ground = 0.84f
        }
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val at = Offset((t.x - e.cam) * e.u, 1200f - e.u + (t.y - t.h / 2) * e.u)
        e.down(1, at, 1000); e.up(1, at, 1100)
        assertEquals(f.id, e.vehicleId); assertFalse(t.held); e.cancel()
    }

    @Test fun aFigureInFrontOfTheTractorCanStillBeDragged() {
        val w = World(); val s = Sim(w); val place = PlaceId.STAGE
        val f = s.designer.add(place, FixtureType.TRACTOR, 0, 1f, 0.91f)!!
        val p = w.addPerson(Species.FOLK, Look(), 1f, place, f.x, PlaceId.FRONT)
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val at = finger(e, p)
        e.down(1, at, 1000); e.move(1, at + Offset(80f, -60f), 1100); e.update(0.016f)
        assertTrue(p.held); assertEquals(-1, e.vehicleId); e.cancel()
    }

    @Test fun foodInsideTheOpenFridgeCanStillBeDraggedOut() {
        val w = World(); val s = Sim(w); val place = PlaceId.HOME
        val f = s.designer.add(place, FixtureType.FRIDGE, 0, 1f, 0.91f)!!.apply { open = true }
        val apple = w.addThing(ThingType.APPLE, 0, place, f.x, f.y - 0.12f).apply {
            inside = f.id; restOwner = f.id; resting = true; ground = y
        }
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val at = Offset((apple.x - e.cam) * e.u, 1200f - e.u + (apple.y - apple.h / 2) * e.u)
        e.down(1, at, 1000); e.move(1, at + Offset(200f, -80f), 1100); e.update(0.016f)
        assertTrue(apple.held); assertEquals(-1, apple.inside); assertTrue(f.open)
        e.cancel()
    }

    @Test fun cupboardsInTheBigHouseUseTheirVisibleOpenDoorsToo() {
        val cases = listOf(
            Triple(FixtureType.GR_FRIDGE, -0.2f, -0.2f),
            Triple(FixtureType.GR_JAM_CABINET, -0.18f, -0.1f),
            Triple(FixtureType.GR_SIDEBOARD, -0.24f, -0.1f),
            Triple(FixtureType.UP_WARDROBE, -0.25f, -0.2f),
        )
        for ((type, dx, dy) in cases) {
            val w = World(); val s = Sim(w)
            val place = if (type == FixtureType.UP_WARDROBE) PlaceId.MANOR_UPPER else PlaceId.MANOR_GROUND
            val f = s.designer.add(place, type, 0, 1f, 0.91f)!!.apply { open = true }
            val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
            val at = Offset((f.x + dx - e.cam) * e.u, 1200f - e.u + (f.y + dy) * e.u)
            e.down(1, at, 1000); e.up(1, at, 1100)
            assertFalse("$type closes on its door", f.open); e.cancel()
        }
    }

    @Test fun bandAndPartyDanceSurviveTheCompleteFrameAndStopWhenPlayEnds() {
        val w=World();val s=Sim(w)
        val people=Community.INSTRUMENTS.mapIndexed { i,type ->
            w.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,0.7f+i*0.3f,0.95f).also { p ->
                val t=w.addThing(type,0,p.place,p.x,p.y);s.give(p,t,Part.HAND)
            }
        }
        val e=Engine(w,PlaceId.HOME,s,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        assertTrue(s.community.startBand(people.map { it.id }))
        repeat(60) { e.update(0.016f) }
        people.forEach { assertTrue(it.anim.dance>0f) }
        s.community.stopBand();e.update(0.016f)
        people.forEach { assertEquals(0f,it.anim.dance,0f) }
        assertTrue(s.community.startParty(PlaceId.HOME,1f,people.map { it.id },2,1))
        e.update(0.016f);people.forEach { assertTrue(it.anim.dance>0f) }
        assertTrue(s.community.endParty());e.update(0.016f)
        people.forEach { assertEquals(0f,it.anim.dance,0f) }
        e.cancel()
    }
    @Test fun draggingAPlayerAlongTheScreenEdgeLetsTheOtherWalkAlongWithoutStealingTheLeader() {
        val w = World(); val place = PlaceId.MANOR_GROUND; val s = Sim(w)
        val leader = w.addPerson(Species.FOLK, Look(), 1f, place, 1.5f, 0.9f)
        val friend = w.addPerson(Species.FOLK, Look(), 1f, place, 0.7f, 0.9f)
        w.playerIds += listOf(leader.id, friend.id)
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val from = finger(e, leader); val edge = Offset(1850f, from.y)
        e.down(1, from, 1000); e.move(1, edge, 1100)
        var last = friend.x
        repeat(140) {
            e.update(0.016f); assertTrue(leader.held); assertFalse(leader.anim.following)
            assertTrue(kotlin.math.abs(friend.x-last) <= PlayerFollow.MAX_SPEED*0.016f + 0.0001f)
            last = friend.x
        }
        assertTrue(e.cam > 2f); assertTrue(friend.x > 1.5f)
        e.up(1, edge, 3500); assertFalse(leader.held); e.cancel()
    }

    @Test fun aCollectedStarterObjectCanBeDraggedOutOfItsBoxWithTheSameIdentity() {
        val w = World(); WorldFactory.addFixtures(w)
        val t = w.addThing(ThingType.UP_BLOCK, 3, PlaceId.HOME, 0.75f, 0.9f)
        WorldFactory.remember(w,t); StarterLayout.upgrade(w)
        val s = Sim(w); s.settle(PlaceId.HOME)
        val box = w.fixtures[t.inside]!!; s.tap(PlaceId.HOME,box,0f,0f)
        val e = Engine(w,PlaceId.HOME,s,host,false,0f).apply { setSize(1920f,1200f,1.5f) }
        val from = Offset((t.x-e.cam)*e.u,1200f-e.u+(t.y-t.h/2f)*e.u)
        e.down(1,from,1000); e.move(1,from+Offset(450f,-120f),1100)
        repeat(20) { e.update(0.016f) }
        assertTrue(t.held); assertEquals(-1,t.inside); assertSame(t,w.bodies[t.id])
        e.up(1,from+Offset(450f,-120f),1600)
        assertFalse(t.held); assertEquals(3,t.variant); e.cancel()
    }

    @Test fun swipingWithTwoPlayersWalksBeforeReleaseAndNeverTeleportsOnPhoneOrTablet() {
        for ((width, height, density) in listOf(Triple(2400f, 1080f, 2.625f), Triple(1920f, 1200f, 1.5f))) {
            val w = World(); val place = PlaceId.MANOR_GROUND; val s = Sim(w)
            val a = w.addPerson(Species.FOLK, Look(), 1f, place, 0.7f, 0.9f)
            val b = w.addPerson(Species.FOLK, Look(hair = 6), 1f, place, 1.1f, 0.9f)
            w.playerIds += listOf(a.id, b.id)
            val e = Engine(w, place, s, host, false, 0f).apply { setSize(width, height, density) }
            val from = Offset(width * 0.85f, height - e.u + e.u * 0.20f)
            e.down(10, from, 1000)
            var ax = a.x; var bx = b.x
            fun frame() {
                e.update(0.016f)
                assertTrue(kotlin.math.abs(a.x - ax) <= PlayerFollow.MAX_SPEED * 0.016f + 0.0001f)
                assertTrue(kotlin.math.abs(b.x - bx) <= PlayerFollow.MAX_SPEED * 0.016f + 0.0001f)
                ax = a.x; bx = b.x
            }
            repeat(40) { i -> e.move(10, from - Offset(e.u * (i + 1) * 0.05f, 0f), 1016L + i * 16L); frame() }
            assertTrue(a.x > 0.7f); assertTrue(b.x > 1.1f) // They follow during the swipe.
            val to = from - Offset(e.u * 2f, 0f)
            e.up(10, to, 1650)
            repeat(300) { frame() }
            assertTrue(a.x > e.cam); assertTrue(b.x < e.cam + e.visibleViewport)
            assertTrue(b.x - a.x > 0.24f); assertFalse(a.anim.following); assertFalse(b.anim.following)
            assertSame(a, w.bodies[a.id]); assertSame(b, w.bodies[b.id])
            e.cancel()
        }
    }

    @Test fun swipingWithASecondFingerDoesNotWaitForOrReleaseTheHeldPlayersFigure() {
        val w = World(); val place = PlaceId.MANOR_GROUND; val s = Sim(w)
        val a = w.addPerson(Species.FOLK, Look(), 1f, place, 1f, 0.9f)
        val b = w.addPerson(Species.FOLK, Look(), 1f, place, 1.4f, 0.9f)
        w.playerIds += listOf(a.id, b.id)
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val hand = finger(e, a)
        e.down(1, hand, 1000); e.move(1, hand + Offset(20f, -30f), 1100)
        val sky = Offset(1600f, 400f); e.down(2, sky, 1200)
        repeat(40) { i -> e.move(2, sky - Offset(e.u * (i + 1) * 0.05f, 0f), 1216L + i * 16L); e.update(0.016f); assertTrue(a.held) }
        e.up(2, sky - Offset(e.u * 2f, 0f), 1900)
        repeat(180) { e.update(0.016f); assertTrue(a.held) }
        assertFalse(a.anim.following); assertTrue(b.x > 2f); assertTrue(e.touching)
        e.cancel(); assertFalse(a.held)
    }

    @Test fun choosingAnotherRoomDoesNotStealTheOtherChildsHeldFigure() {
        val w = World(); val place = PlaceId.MANOR_GROUND; val s = Sim(w)
        val a = w.addPerson(Species.FOLK, Look(), 1f, place, 0.7f, 0.9f)
        val b = w.addPerson(Species.FOLK, Look(), 1f, place, 1.4f, 0.9f)
        w.playerIds += listOf(a.id, b.id)
        val e = Engine(w, place, s, host, false, 0f).apply { setSize(1920f, 1200f, 1.5f) }
        val hand = finger(e, a); e.down(1, hand, 1000); e.move(1, hand + Offset(20f, -30f), 1100)
        val bx = b.x; e.selectRoom(2)
        assertTrue(a.held); assertEquals(bx, b.x, 0f)
        repeat(240) { e.update(0.016f); assertTrue(a.held) }
        assertTrue(b.x > bx + 1f); e.cancel()
    }

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
        assertTrue(p.id in world.playerIds)
        assertTrue(Players.paused(world,p))
        assertSame(p, world.bodies[p.id])
    }
    @Test fun explicitPackPausesPlayerAndInviteBringsSameFigureBack() {
        val world = World()
        val p = world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 0.7f, 0.9f, "A")
        Players.toggle(world, p)
        val e = engine(world)
        e.packPerson(p)
        assertSame(p, world.bodies[p.id])
        assertEquals(1, e.bagCount)
        assertEquals(Mode.BAG, p.mode)
        assertNull(p.place)
        assertTrue(p.id in world.playerIds)
        assertTrue(Players.paused(world,p))
        Players.arrive(world, PlaceId.BEACH, 1f)
        assertEquals(Mode.BAG, p.mode)
        e.invite(p)
        assertEquals(PlaceId.HOME, p.place)
        assertEquals(Mode.FREE, p.mode)
        assertTrue(p.id in world.playerIds)
        assertFalse(Players.paused(world,p))
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

    @Test fun focusingAnEndRoomKeepsItsLabelWhenCameraCannotCenterIt() {
        val e=engine(World())
        e.focusOn(0.4f)
        e.setSize(1920f,1200f,1.5f)
        assertEquals(0,e.visibleRoom)
        e.focusOn(PlaceId.HOME.width-0.3f)
        e.setSize(1920f,1200f,1.5f)
        assertEquals(Decor.rooms(PlaceId.HOME).lastIndex,e.visibleRoom)
    }
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
        assertEquals(0.7f, people[0].x, 0f)
        assertEquals(1.2f, people[1].x, 0f)
        repeat(250) { e.update(0.016f) }
        people.forEach { assertEquals(3, Decor.roomAt(PlaceId.HOME, it.x)) }
        assertEquals(Mode.WORN, hat.mode)
        assertEquals(people[0].id, hat.holder)
        assertEquals(PlaceId.HOME, hat.place)
    }

    @Test fun panningToAnotherRoomBringsThePlayersIntoTheViewedRoomWithoutAnArrival() {
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
