package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class MischiefTest {
    private fun sim() = Sim(World(), random = kotlin.random.Random(7))
    private fun folk(s: Sim, x: Float) = s.world.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, x, PlaceId.HOME.floor)

    @Test fun nothingHappensBeforeTheWaitOrWhilePaused() {
        val s = sim(); folk(s, 1f); s.mischief.wait = 10f
        s.mischief.step(PlaceId.HOME, 5f); assertNull(s.mischief.last)
        s.mischief.paused = true; s.mischief.step(PlaceId.HOME, 20f); assertNull(s.mischief.last)
        s.mischief.paused = false; s.mischief.step(PlaceId.HOME, 20f); assertNotNull(s.mischief.last)
    }

    @Test fun aSneezeDropsTheSameHatWhichIsSaved() {
        val s = sim(); val p = folk(s, 1f)
        val hat = s.world.addThing(ThingType.CAP, 0, PlaceId.HOME, p.x, p.y)
        assertEquals(Give.WORE, s.give(p, hat, Part.HAT))
        s.mischief.force(MischiefKind.SNEEZE, PlaceId.HOME)
        assertEquals(Mode.FREE, hat.mode); assertEquals(PlaceId.HOME, hat.place); assertNull(s.world.worn(p, Slot.HEAD))
        val back = WorldStore.decode(WorldStore.encode(s.world, Settings())).world
        assertEquals(Mode.FREE, back.bodies[hat.id]!!.mode)
    }

    @Test fun heldSleepingAndRidingFiguresAreLeftAlone() {
        val s = sim()
        val held = folk(s, 1f).also { it.held = true }
        val sleeper = folk(s, 1.5f).also { it.anim.pose = Pose.LIE }
        val hat = s.world.addThing(ThingType.CAP, 0, PlaceId.HOME, held.x, held.y); s.give(held, hat, Part.HAT)
        s.mischief.force(MischiefKind.SNEEZE, PlaceId.HOME); s.mischief.force(MischiefKind.BIRD, PlaceId.HOME)
        assertNull(s.mischief.last); assertNull(s.mischief.bird)
        assertSame(hat, s.world.worn(held, Slot.HEAD))
        assertEquals(Pose.LIE, sleeper.anim.pose)
    }

    @Test fun catchingThePeekingTrollIsAFirstOnlyOnce() {
        val s = sim()
        s.designer.add(PlaceId.HOME, FixtureType.WARDROBE, 0, 1.2f, PlaceId.HOME.floor)
        assertTrue(s.world.fixturesIn(PlaceId.HOME).any { it.spec.container != null })
        s.mischief.force(MischiefKind.TROLL, PlaceId.HOME)
        assertNotNull(s.mischief.troll)
        val stickers = s.world.stickers.size
        assertTrue(s.mischief.tapTroll())
        assertTrue("PEEK_TROLL" in s.world.firsts); assertNull(s.mischief.troll)
        s.mischief.force(MischiefKind.TROLL, PlaceId.HOME); s.mischief.tapTroll()
        assertEquals(stickers + 1, s.world.stickers.size)
    }

    @Test fun aMischiefSneezeNeverCompletesATaskOrEarnsASticker() {
        val s = sim(); folk(s, 1f)
        s.world.taskSet.clear(); s.world.taskSet += "sneeze"
        val stickers = s.world.stickers.size
        s.mischief.force(MischiefKind.SNEEZE, PlaceId.HOME)
        assertEquals(MischiefKind.SNEEZE, s.mischief.last)
        assertEquals(stickers, s.world.stickers.size)
        assertEquals(0, s.world.taskProgress["sneeze"] ?: 0)
    }

    @Test fun reducedMotionKeepsOnlyTheSneezeAndTheTroll() {
        val s = sim(); folk(s, 1f); s.mischief.calm = true
        s.mischief.force(MischiefKind.BIRD, PlaceId.HOME)
        assertNull(s.mischief.bird)
        s.mischief.force(MischiefKind.SNEEZE, PlaceId.HOME)
        assertEquals(MischiefKind.SNEEZE, s.mischief.last)
    }

    @Test fun theTrollHidesAgainByItself() {
        val s = sim(); s.designer.add(PlaceId.HOME, FixtureType.WARDROBE, 0, 1.2f, PlaceId.HOME.floor)
        s.mischief.force(MischiefKind.TROLL, PlaceId.HOME)
        s.mischief.step(PlaceId.HOME, 6f)
        assertNull(s.mischief.troll)
        assertFalse(s.mischief.tapTroll())
    }
}
