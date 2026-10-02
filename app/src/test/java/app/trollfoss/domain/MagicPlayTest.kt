package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.*
import org.junit.Test

class MagicPlayTest {
    @Test fun foregroundKitMakesFurnitureAtNormalDepthAndRestartDoesNotKeepFingerGrips() {
        val w = World(); val s = Sim(w)
        s.magic.kit(PlayRecipe.CART,PlaceId.HOME,1.2f)
        val parts = w.playKits.getValue(PlayRecipe.CART).map { w.bodies[it] as Thing }
        parts.forEach { it.x=1.2f }
        val f = requireNotNull(s.magic.combine(parts.last()))
        assertEquals(PlaceId.HOME.floor,f.y,0.01f)
        s.magic.grip(1,f,0,1f); s.magic.grip(2,f,1,1f); assertEquals(2,f.count)
        val saved = WorldStore.decode(WorldStore.encode(w,Settings())).world
        assertEquals(0,saved.fixtures.getValue(f.id).count)
    }

    @Test fun aStaleStoryReferenceNeverReplacesAFriend() {
        val w = World(); val s = Sim(w)
        val friend = w.addPerson(Species.FOLK,Look(),1f,PlaceId.HOME,1f,0.9f)
        w.adventureHat=friend.id; s.magic.start(Adventure.HAT)
        assertSame(friend,w.bodies[friend.id]); assertNotEquals(friend.id,w.adventureHat)
    }
    @Test fun recallingOurKitReleasesItsEarlierCreationAndReusesAllOriginalParts() {
        val w = World(); val s = Sim(w); val f = make(s,PlayRecipe.FORT)
        val ids = w.playAssemblies.getValue(f.id).parts
        assertTrue(s.magic.kit(PlayRecipe.FORT,PlaceId.FOREST,1.2f))
        assertFalse(w.fixtures.containsKey(f.id))
        assertEquals(ids,w.playKits.getValue(PlayRecipe.FORT))
        assertTrue(ids.all { w.bodies[it]?.place == PlaceId.FOREST && w.bodies[it]?.mode == Mode.FREE })
    }

    @Test fun invalidAssemblyMetadataReleasesPartsInsteadOfHidingThemForever() {
        val w = World(); val s = Sim(w); val f = make(s,PlayRecipe.CART)
        val ids = w.playAssemblies.getValue(f.id).parts
        val json = WorldStore.encode(w,Settings())
        json.getJSONObject("play").remove("assemblies")
        val restored = WorldStore.decode(json).world
        assertTrue(ids.all { restored.bodies[it]?.mode == Mode.FREE })
        assertTrue(restored.playAssemblies.isEmpty())
    }

    @Test fun startingAStoryRecognisesARealCreationAlreadyMadeThere() {
        val w = World(); val s = Sim(w); val f = make(s,PlayRecipe.FORT,PlaceId.FOREST)
        val p = w.addPerson(Species.FOLK,Look(),1f,f.place,f.x,f.y)
        s.seat(p,f,0); s.magic.start(Adventure.CAMP)
        assertEquals(2,s.magic.stage(Adventure.CAMP))
    }
    @Test fun missingStoryHatCanBeRecoveredAtTheCurrentStepWithoutRestarting() {
        val w = World(); val s = Sim(w); s.magic.start(Adventure.HAT)
        val hat = w.bodies[w.adventureHat] as Thing
        s.magic.lifted(hat); w.remove(hat); s.magic.start(Adventure.HAT)
        assertEquals(hat.id, w.adventureHat); assertEquals(PlaceId.HOME, w.bodies[hat.id]?.place)
        assertEquals(1, s.magic.stage(Adventure.HAT))
    }
    private fun make(sim: Sim, recipe: PlayRecipe, place: PlaceId = PlaceId.HOME): Fixture {
        sim.magic.kit(recipe, place, 1.2f)
        val parts = sim.world.playKits.getValue(recipe).map { sim.world.bodies[it] as Thing }
        parts.forEach { it.x = 1.2f; it.y = place.floor }
        return requireNotNull(sim.magic.combine(parts.last()))
    }

    @Test fun bothCreationsKeepOriginalPartsThroughRestartAndPacking() {
        for (recipe in PlayRecipe.entries) {
            val world = World(); val sim = Sim(world)
            val f = make(sim, recipe)
            val ids = world.playAssemblies.getValue(f.id).parts
            assertTrue(ids.all { world.bodies[it]?.mode == Mode.INSIDE })
            val loaded = WorldStore.decode(WorldStore.encode(world, Settings())).world
            assertEquals(ids, loaded.playAssemblies.getValue(f.id).parts)
            val before = loaded.bodies.keys.toSet()
            assertTrue(Sim(loaded).designer.store(f.place, loaded.fixtures.getValue(f.id)))
            assertFalse(loaded.fixtures.containsKey(f.id))
            assertTrue(ids.all { loaded.bodies[it]?.mode == Mode.BAG && loaded.bodies[it]?.place == null })
            assertTrue(loaded.bodies.keys.containsAll(before))
            assertTrue(loaded.storage.none { it.type == recipe.fixture })
        }
    }

    @Test fun packingReleasesPassengersAndPreservesTheirClothes() {
        val world = World(); val sim = Sim(world); val f = make(sim, PlayRecipe.CART)
        val p = world.addPerson(Species.FOLK, Look(), 1f, f.place, f.x, f.y)
        val hat = world.addThing(ThingType.CAP, 1, f.place, f.x, f.y)
        sim.give(p, hat, Part.HAT); sim.seat(p, f, 0)
        assertTrue(sim.magic.unmake(f)); assertEquals(Mode.FREE, p.mode)
        assertEquals(Mode.WORN, hat.mode); assertEquals(p.id, hat.holder)
    }

    @Test fun kitsRecallOnlyTheirOwnPiecesWithoutDuplicatesOrTakingHeldThings() {
        val w = World(); val s = Sim(w)
        val ordinary = w.addThing(ThingType.PILLOW, 0, PlaceId.CAFE, 3f, 0.7f)
        s.magic.kit(PlayRecipe.FORT, PlaceId.HOME, 1.2f)
        val ids = w.playKits.getValue(PlayRecipe.FORT)
        val held = w.bodies.getValue(ids.first()); held.held = true
        s.magic.kit(PlayRecipe.FORT, PlaceId.BEACH, 1.2f)
        assertEquals(ids, w.playKits.getValue(PlayRecipe.FORT)); assertEquals(PlaceId.HOME, held.place)
        assertEquals(PlaceId.CAFE, ordinary.place); assertEquals(3f, ordinary.x, 0f)
        assertEquals(4, w.bodies.size)
    }

    @Test fun distantOrHeldPartsDoNotCombineAndEmptyHouseCannotReceiveKit() {
        val w = World(); val s = Sim(w)
        assertFalse(s.magic.kit(PlayRecipe.FORT, PlaceId.MINE_UPPER, 1f))
        s.magic.kit(PlayRecipe.FORT, PlaceId.HOME, 1f)
        val parts = w.playKits.getValue(PlayRecipe.FORT).map { w.bodies[it] as Thing }
        parts.last().x = 3f; assertNull(s.magic.combine(parts.first()))
        parts.last().x = parts.first().x; parts.last().held = true
        assertNull(s.magic.combine(parts.first()))
    }

    @Test fun twoHandlesMoveTogetherAndSinglePlayerHelpIsOptional() {
        val w = World(); val s = Sim(w); val cart = make(s, PlayRecipe.CART)
        val start = cart.x
        assertTrue(s.magic.grip(1, cart, 0, 1f)); assertFalse(s.magic.drag(1, 1.1f))
        assertFalse(s.magic.grip(2, cart, 0, 1f))
        assertTrue(s.magic.grip(2, cart, 1, 1f)); assertTrue(s.magic.drag(1, 1.2f))
        assertTrue(cart.x > start); s.magic.release(2)
        assertFalse(s.magic.drag(1, 1.3f)); cart.on = true
        assertTrue(s.magic.drag(1, 1.4f)); s.magic.cancel(); assertEquals(0, cart.count)
    }

    @Test fun hatAdventureNeedsItsOwnHatAtHomeAndRewardIsOnlyGivenOnce() {
        val w = World(); val s = Sim(w); s.magic.start(Adventure.HAT)
        val hat = w.bodies[w.adventureHat] as Thing
        val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.BEACH, 1.2f, 0.9f)
        s.magic.lifted(hat); s.give(p, hat, Part.HAT)
        assertEquals(1, s.magic.stage(Adventure.HAT))
        p.place = PlaceId.HOME; s.give(p, hat, Part.HAT)
        assertEquals(3, s.magic.stage(Adventure.HAT))
        val reward = w.bag().single { it is Thing && it.type == Adventure.HAT.reward }
        s.give(p, hat, Part.HAT); s.magic.start(Adventure.HAT)
        assertEquals(1, w.bag().count { it is Thing && it.type == Adventure.HAT.reward })
        assertSame(reward, w.bodies[reward.id])
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world
        assertEquals(3, Sim(loaded).magic.stage(Adventure.HAT)); assertEquals(hat.id, loaded.adventureHat)
    }

    @Test fun campAdventureFollowsBuildSeatThenLightAndSavesEachStep() {
        val w = World(); val s = Sim(w); s.magic.start(Adventure.CAMP)
        val fort = make(s, PlayRecipe.FORT, PlaceId.FOREST)
        assertEquals(1, s.magic.stage(Adventure.CAMP))
        val p = w.addPerson(Species.FOLK, Look(), 1f, fort.place, fort.x, fort.y)
        s.seat(p, fort, 0); assertEquals(2, s.magic.stage(Adventure.CAMP))
        val loaded = WorldStore.decode(WorldStore.encode(w, Settings())).world; val restored = Sim(loaded)
        val light = loaded.bodies.values.filterIsInstance<Thing>().single { it.type == ThingType.AT_FLASHLIGHT }
        light.x = fort.x; light.y = fort.y
        assertTrue(restored.magic.act(light, PlayAction.LIGHT)); assertEquals(3, restored.magic.stage(Adventure.CAMP))
        assertTrue(loaded.bag().any { it is Thing && it.type == ThingType.STAR_JAR })
    }

    @Test fun paradeCanBeFinishedAloneWithTwoFriendsAndHelp() {
        val w = World(); val s = Sim(w); s.magic.start(Adventure.PARADE)
        val f = make(s, PlayRecipe.CART, PlaceId.FARM)
        repeat(2) { i -> s.seat(w.addPerson(Species.FOLK, Look(), 1f, f.place, f.x, f.y), f, i) }
        assertEquals(2, s.magic.stage(Adventure.PARADE)); f.on = true
        s.magic.grip(1, f, 0, 1f)
        repeat(5) { i -> s.magic.drag(1, 1.1f + i * 0.1f) }
        assertEquals(3, s.magic.stage(Adventure.PARADE))
        assertTrue(w.bag().any { it is Thing && it.type == ThingType.MICROPHONE })
    }

    @Test fun multiUsePillowPreservesItAndSleepingFriendsIgnoreReactions() {
        val w = World(); val s = Sim(w); val p = w.addPerson(Species.FOLK, Look(), 1f, PlaceId.HOME, 1f, 0.9f)
        val t = w.addThing(ThingType.PILLOW, 0, p.place, 1f, 0.9f)
        assertTrue(s.magic.act(t, PlayAction.HUG)); assertEquals(Mode.WORN, t.mode)
        assertTrue(s.magic.act(t, PlayAction.THROW)); assertEquals(Mode.FREE, t.mode); assertTrue(t.vy < 0)
        assertSame(t, w.bodies[t.id]); assertEquals(Face.LAUGH, p.anim.face)
        p.anim.pose = Pose.LIE; p.anim.face = Face.SLEEP
        s.magic.react(PlaceId.HOME, Fx.BUILD, p.x, p.y)
        assertEquals(Face.SLEEP, p.anim.face)
    }
}
