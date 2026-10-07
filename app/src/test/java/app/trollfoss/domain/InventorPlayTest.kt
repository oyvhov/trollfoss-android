package app.trollfoss.domain

import app.trollfoss.ui.art.Oblique
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class InventorPlayTest {
    private val place = PlaceId.HOME
    private fun sim(seed: Int = 7) = Sim(World(), random = Random(seed)).also { s -> repeat(32) { s.world.stickers += it }; Progression.remember(s.world); s.here = place }
    private fun step(s: Sim, sec: Float) = repeat((sec / 0.02f).toInt()) { s.step(place, 0.02f) }
    private fun folk(s: Sim, x: Float) = s.world.addPerson(Species.FOLK, Look(), 1f, place, x, place.floor)
    private fun onFloor(s: Sim, type: ThingType, x: Float) = s.world.addThing(type, 0, place, x, place.floor).also { it.resting = true; it.restOwner = -1; it.ground = place.floor }
    private val heard = ArrayList<Pair<Fx, Int>>()
    private fun listening(s: Sim) {
        heard.clear()
        s.listener = object : SimListener { override fun onFx(fx: Fx, x: Float, y: Float, fixture: Fixture?, thing: Thing?, param: Int) { heard += fx to param } }
    }

    /** Steps until [done] holds, failing (instead of hanging) if it never does. */
    private fun until(s: Sim, what: String, max: Float = 12f, done: () -> Boolean) {
        var t = 0f
        while (!done()) { assertTrue("never happened: $what", t < max); s.step(place, 0.02f); t += 0.02f }
    }

    @Test fun level9OpensAt32StickersWithFourToys() {
        val s = sim()
        assertEquals(9, Progression.level(s.world))
        val nine = ToyReward.entries.filter { it.level == 9 }
        assertEquals(4, nine.size)
        nine.forEach { assertTrue(Progression.unlocked(s.world, it)) }
        assertEquals("one sticker short of the limit is still level 8", 8, Progression.level(Sim(World()).also { t -> repeat(31) { t.world.stickers += it } }.world))
    }

    @Test fun theSlantUsedForHitTestingIsTheSlantThatIsDrawn() {
        assertEquals(Oblique.DX, InventorPlay.SLANT_X, 0f)
        assertEquals(Oblique.DY, InventorPlay.SLANT_Y, 0f)
    }

    @Test fun thingsOnTheBenchAndTheTrayAreSeenAndCanBeTakenBack() {
        assertTrue(FixtureType.PLAY_ROBOT_WORKSHOP in ToyPlay.VISIBLE_INSIDE)
        assertTrue(FixtureType.PLAY_HELPER_ROBOT in ToyPlay.VISIBLE_INSIDE)
    }

    // ------------------------------------------------------------------ robot workshop

    @Test fun theWorkshopBuildsARobotPalFromTwoThingsAndKeepsTheThings() {
        val s = sim(); val shop = s.toys.claim(ToyReward.ROBOT_WORKSHOP, place, 1.2f)!!; listening(s)
        s.tap(place, shop, 0f, -0.1f); step(s, 0.2f)
        assertTrue("with no things it only bonks", heard.any { it.first == Fx.BONK })
        val a = s.world.addThing(ThingType.APPLE, 0, place, shop.x, shop.top); val b = s.world.addThing(ThingType.BALL, 0, place, shop.x, shop.top)
        assertTrue(s.toys.drop(shop, a)); assertTrue(s.toys.drop(shop, b))
        val c = s.world.addThing(ThingType.BANANA, 0, place, shop.x, shop.top)
        assertFalse("only two fit", s.toys.drop(shop, c))
        s.tap(place, shop, 0f, -0.1f)
        assertTrue("nothing yet while it whirs", s.world.bodiesIn(place).none { it is Thing && it.type == ThingType.ROBOT_PAL })
        step(s, InventorPlay.BUILD_TIME + 0.3f)
        val pals = s.world.bodiesIn(place).filterIsInstance<Thing>().filter { it.type == ThingType.ROBOT_PAL }
        assertEquals(1, pals.size)
        assertEquals("the parts stay on the bench", 2, s.world.inMachine(shop).size)
        assertTrue("ROBOT_WORKSHOP" in s.world.firsts)
        assertTrue(heard.any { it.first == Fx.BEEP } && heard.any { it.first == Fx.GIFT })
    }

    @Test fun theWorkshopRestsBetweenBuildsAndCapsThePals() {
        val s = sim(); val shop = s.toys.claim(ToyReward.ROBOT_WORKSHOP, place, 1.2f)!!
        s.toys.drop(shop, s.world.addThing(ThingType.APPLE, 0, place, shop.x, shop.top)); s.toys.drop(shop, s.world.addThing(ThingType.BALL, 0, place, shop.x, shop.top))
        fun pals() = s.world.bodiesIn(place).count { it is Thing && it.type == ThingType.ROBOT_PAL }
        s.tap(place, shop, 0f, -0.1f); step(s, InventorPlay.BUILD_TIME + 0.3f); assertEquals(1, pals())
        s.tap(place, shop, 0f, -0.1f); step(s, InventorPlay.BUILD_TIME + 0.3f)
        assertEquals("it rests first", 1, pals())
        step(s, InventorPlay.REST)
        s.tap(place, shop, 0f, -0.1f); step(s, InventorPlay.BUILD_TIME + 0.3f); assertEquals(2, pals())
        repeat(InventorPlay.MAX_PALS) { s.world.addThing(ThingType.ROBOT_PAL, 0, place, 0.5f, place.floor) }
        step(s, InventorPlay.REST)
        val before = pals()
        s.tap(place, shop, 0f, -0.1f); step(s, InventorPlay.BUILD_TIME + 0.3f)
        assertEquals("a crowded room gets no more pals", before, pals())
    }

    @Test fun aWorkshopStoredMidBuildLeavesNothingBehindForTheNextOne() {
        val s = sim(); val old = s.toys.claim(ToyReward.ROBOT_WORKSHOP, place, 1.2f)!!
        s.toys.drop(old, s.world.addThing(ThingType.APPLE, 0, place, old.x, old.top)); s.toys.drop(old, s.world.addThing(ThingType.BALL, 0, place, old.x, old.top))
        s.tap(place, old, 0f, -0.1f); step(s, 0.5f)
        assertTrue(s.designer.store(place, old))
        val fresh = s.toys.claim(ToyReward.ROBOT_WORKSHOP, place, 1.2f)!!
        s.toys.drop(fresh, s.world.addThing(ThingType.CAKE, 0, place, fresh.x, fresh.top)); s.toys.drop(fresh, s.world.addThing(ThingType.PIZZA, 0, place, fresh.x, fresh.top))
        step(s, InventorPlay.BUILD_TIME + 1f)
        assertTrue("the new bench waits to be tapped", s.world.bodiesIn(place).none { it is Thing && it.type == ThingType.ROBOT_PAL })
    }

    // ------------------------------------------------------------------ helper robot

    @Test fun theHelperRobotFetchesALooseThingAndBringsItToAFriend() {
        val s = sim(); val bot = s.toys.claim(ToyReward.HELPER_ROBOT, place, 1.7f)!!; listening(s)
        val room = Decor.rooms(place)[Decor.roomAt(place, bot.x)]
        val ball = onFloor(s, ThingType.BALL, room.start + 0.25f)
        val kid = folk(s, room.endInclusive - 0.25f)
        s.tap(place, bot, 0f, -0.1f)
        assertTrue(bot.on)
        step(s, 1.5f)
        assertEquals("the ball is on the tray", Mode.INSIDE, ball.mode)
        until(s, "the ball is given to the friend") { ball.mode == Mode.FREE }
        assertTrue("it lands next to the friend", abs(ball.x - kid.x) < 0.3f)
        assertTrue("the friend is delighted", kid.anim.face == Face.LAUGH || kid.anim.face == Face.GRIN)
        assertTrue("HELPER_ROBOT" in s.world.firsts)
        step(s, 4f)
        assertEquals("it rolls home again", 0f, bot.shiftX, 0.01f)
        assertFalse(bot.on)
    }

    @Test fun theHelperRobotDancesWhenThereIsNothingToFetchAndCarriesWhatTheChildPutsOnTheTray() {
        val s = sim(); val bot = s.toys.claim(ToyReward.HELPER_ROBOT, place, 1.7f)!!; listening(s)
        s.tap(place, bot, 0f, -0.1f); step(s, 0.3f)
        assertTrue(heard.any { it.first == Fx.BEEP && it.second == 1 }); assertFalse(bot.on)
        val gift = s.world.addThing(ThingType.CAKE, 0, place, bot.x, bot.top)
        assertTrue(s.toys.drop(bot, gift))
        assertFalse("one thing at a time", s.toys.drop(bot, s.world.addThing(ThingType.APPLE, 0, place, bot.x, bot.top)))
        val kid = folk(s, bot.x - 0.5f)
        step(s, 0.5f)
        assertEquals("the load rides along even when the robot stands still", Mode.INSIDE, gift.mode)
        s.tap(place, bot, 0f, -0.1f); step(s, 4f)
        assertEquals(Mode.FREE, gift.mode); assertTrue(abs(gift.x - kid.x) < 0.3f)
    }

    @Test fun aRobotOnAJobTakesNoSecondThingAndReleasesFromWhereItStands() {
        val s = sim(); val bot = s.toys.claim(ToyReward.HELPER_ROBOT, place, 1.7f)!!
        val room = Decor.rooms(place)[Decor.roomAt(place, bot.x)]
        val ball = onFloor(s, ThingType.BALL, room.start + 0.25f)
        folk(s, room.endInclusive - 0.25f)
        s.tap(place, bot, 0f, -0.1f); step(s, 0.2f)
        assertFalse("hands full", s.toys.drop(bot, s.world.addThing(ThingType.CAKE, 0, place, bot.x, bot.top)))
        until(s, "the ball is picked up") { ball.mode == Mode.INSIDE }
        assertTrue("the robot has rolled away from its spot", abs(bot.shiftX) > 0.05f)
        s.toys.releaseAll(bot)
        assertEquals(Mode.FREE, ball.mode)
        assertEquals("released where the robot is", bot.x + bot.shiftX, ball.x, 0.001f)
    }

    @Test fun theHelperRobotIgnoresThingsInOtherRoomsOnFurnitureAndOutOfReach() {
        val s = sim(); val bot = s.toys.claim(ToyReward.HELPER_ROBOT, place, 1.7f)!!; listening(s)
        onFloor(s, ThingType.BALL, 0.3f) // another room
        onFloor(s, ThingType.BANANA, 3.0f) // another room
        s.world.addThing(ThingType.APPLE, 0, place, bot.x + 0.3f, place.floor - 0.3f).also { it.resting = true; it.restOwner = 5 }
        s.tap(place, bot, 0f, -0.1f); step(s, 0.5f)
        assertFalse(bot.on)
        assertTrue(heard.any { it.first == Fx.BEEP && it.second == 1 })
    }

    // ------------------------------------------------------------------ rocket kit

    @Test fun theRocketCountsDownFliesUpAndComesDownWithItsRider() {
        val s = sim(); val rocket = s.toys.claim(ToyReward.ROCKET_KIT, place, 1.2f)!!; listening(s)
        val rider = folk(s, rocket.x).also { it.mode = Mode.SEATED; it.holder = rocket.id; it.slot = 0 }
        val seatDown = s.seatPoint(rocket, 0)[1]
        s.tap(place, rocket, 0f, -0.1f); s.tap(place, rocket, 0f, -0.1f)
        step(s, 1.6f)
        assertEquals("3, 2, 1 and lift-off, once", listOf(3, 2, 1), heard.filter { it.first == Fx.COUNTDOWN }.map { it.second })
        assertEquals(1, heard.count { it.first == Fx.KIT_LAUNCH })
        assertTrue("ROCKET_KIT" in s.world.firsts)
        step(s, 1.5f)
        assertEquals(1f, rocket.angle, 0.01f)
        assertEquals("the rider goes up with it", InventorPlay.RISE, seatDown - s.seatPoint(rocket, 0)[1], 0.01f)
        assertEquals(Face.OOH, rider.anim.face)
        assertTrue(heard.any { it.first == Fx.FIREWORK })
        until(s, "the rocket is down") { !rocket.on }
        assertEquals(0f, rocket.angle, 0.001f)
        assertEquals(seatDown, s.seatPoint(rocket, 0)[1], 0.001f)
        assertEquals(Face.LAUGH, rider.anim.face)
        s.tap(place, rocket, 0f, -0.1f); step(s, 0.1f)
        assertTrue("it can fly again", rocket.on)
    }

    @Test fun aRocketLoadedMidFlightIsSimplyOnItsPad() {
        val s = sim(); val rocket = s.toys.claim(ToyReward.ROCKET_KIT, place, 1.2f)!!
        rocket.on = true // saved while flying; the flight itself is not saved
        step(s, 0.1f)
        assertFalse("no flame on a rocket that is not flying", rocket.on)
        assertEquals(0f, rocket.angle, 0f)
    }

    // ------------------------------------------------------------------ reaction course

    private fun lit(board: Fixture) = board.angle.toInt() - 1
    private fun padX(pad: Int) = (pad - 1.5f) * InventorPlay.PAD
    private fun waitLit(s: Sim, board: Fixture) = until(s, "a pad lights up", 5f) { lit(board) >= 0 }

    @Test fun theReactionCourseLightsOnePadAtATimeAndRewardsFiveInARow() {
        val s = sim(); val board = s.toys.claim(ToyReward.REACTION_COURSE, place, 1.2f)!!; listening(s)
        s.tap(place, board, 0f, -0.03f)
        assertEquals("nothing is lit at once", -1, lit(board))
        var last = -1
        repeat(InventorPlay.GOAL) { round ->
            waitLit(s, board)
            val pad = lit(board)
            assertTrue("a pad lit up", pad in 0..3)
            assertNotEquals("never the same pad twice", last, pad)
            last = pad
            s.tap(place, board, padX(pad), -0.03f)
            if (round + 1 < InventorPlay.GOAL) assertEquals((round + 1).toFloat(), board.angleV, 0f)
        }
        assertEquals(InventorPlay.GOAL, heard.count { it.first == Fx.REACT })
        assertEquals("the rights climb the scale", (1..InventorPlay.GOAL).toList(), heard.filter { it.first == Fx.REACT }.map { it.second % 16 })
        assertTrue(heard.any { it.first == Fx.CONFETTI })
        assertTrue("REACTION_COURSE" in s.world.firsts)
        step(s, 3f)
        assertEquals("the board rests until it is tapped again", -1, lit(board))
    }

    @Test fun aTapAnywhereOnTheDrawnPadCountsEvenAtTheBackRightCorner() {
        for (pad in 0..3) for (z in listOf(0.0f, 0.04f, 0.09f, 0.14f)) {
            // Where that point of the pad is drawn: right and up with depth, the way `InventorArt` leans it.
            val dx = (-0.24f + pad * InventorPlay.PAD) + InventorPlay.SLANT_X * z
            val dy = -0.018f + InventorPlay.SLANT_Y * z
            assertEquals("pad $pad at depth $z", pad, InventorPlay.padAt(dx, dy))
        }
        val s = sim(); val board = s.toys.claim(ToyReward.REACTION_COURSE, place, 1.2f)!!
        val span = ShowPlay.hitSpan(board)
        assertTrue("the back right corner is still the board", 0.32f + 0.5f * 0.14f in span)
        assertEquals(-0.32f, span.start, 0f)
    }

    @Test fun theDialogsUseButtonPressesWhicheverPadIsLitForThoseWhoCannotAim() {
        val s = sim(); val board = s.toys.claim(ToyReward.REACTION_COURSE, place, 1.2f)!!
        s.tap(place, board, 0f, -0.2f)
        waitLit(s, board)
        s.tap(place, board, 0f, -0.2f)
        assertEquals("one right, whatever pad it was", 1f, board.angleV, 0f)
    }

    @Test fun aWrongPadOrATooSlowTapOnlyStartsTheRunOver() {
        val s = sim(); val board = s.toys.claim(ToyReward.REACTION_COURSE, place, 1.2f)!!; listening(s)
        s.tap(place, board, 0f, -0.03f)
        waitLit(s, board)
        s.tap(place, board, padX(lit(board)), -0.03f)
        waitLit(s, board)
        val wrong = (lit(board) + 1) % 4
        s.tap(place, board, padX(wrong), -0.03f)
        assertEquals(1, heard.count { it.first == Fx.BONK })
        assertEquals("the streak starts over", 0f, board.angleV, 0f)
        s.tap(place, board, 0f, -0.03f)
        assertEquals("a tap between two lights costs nothing", 1, heard.count { it.first == Fx.BONK })
        waitLit(s, board)
        step(s, 2.5f)
        assertEquals("too slow is a miss too", 2, heard.count { it.first == Fx.BONK })
        assertFalse("REACTION_COURSE" in s.world.firsts)
    }

    @Test fun theBoardNapsAfterThreeMissesInARow() {
        val s = sim(); val board = s.toys.claim(ToyReward.REACTION_COURSE, place, 1.2f)!!; listening(s)
        s.tap(place, board, 0f, -0.03f)
        step(s, 12f)
        assertEquals(3, heard.count { it.first == Fx.BONK })
        assertEquals("nothing lights any more", -1, lit(board))
        val ticks = heard.count { it.first == Fx.TICK }
        s.tap(place, board, 0f, -0.03f)
        step(s, 1.2f)
        assertEquals("a fresh tap starts it again", ticks + 1, heard.count { it.first == Fx.TICK })
        assertTrue(lit(board) >= 0)
    }
}
