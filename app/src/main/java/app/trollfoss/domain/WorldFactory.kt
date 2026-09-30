package app.trollfoss.domain

import kotlin.random.Random

/** Builds a fresh island from the blueprints in [Places]. */
object WorldFactory {
    fun fixtureId(place: PlaceId, index: Int): Int = place.ordinal * 100 + index

    /** The fixtures of every place, in their starting state. Saves apply their own state on top. */
    fun addFixtures(world: World) {
        for (place in PlaceId.entries) {
            val defs = Places.spec(place).fixtures
            defs.forEachIndexed { index, def ->
                val depth = if (def.on >= 0) defs[def.on].y + 0.0004f else def.y
                val f = Fixture(fixtureId(place, index), place, def.type, def.x, def.y, def.variant, depth)
                if (def.on >= 0) f.host = fixtureId(place, def.on)
                // Street lamps are lit, so the park and the beach glow when night falls.
                if (def.type == FixtureType.LAMP_POST) f.on = true
                world.fixtures[f.id] = f
            }
        }
    }

    /** True when the child has moved [f] away from where the blueprint puts it. */
    fun moved(f: Fixture): Boolean {
        val index = f.id - f.place.ordinal * 100
        val def = Places.spec(f.place).fixtures.getOrNull(index) ?: return false
        return kotlin.math.abs(f.x - def.x) > 0.0005f || kotlin.math.abs(f.y - def.y) > 0.0005f
    }

    fun create(random: Random = Random(2026)): World {
        val world = World()
        addFixtures(world)
        val sim = Sim(world)
        for (place in PlaceId.entries) populate(world, sim, place, random)
        world.bodies.values.forEach { it.age = 10f }
        return world
    }

    /**
     * An older save knows nothing of places added in an update: fill each place it did not know from its
     * blueprint, leaving out figures whose names already live somewhere else. A place the child emptied
     * on purpose stays empty. Returns true if anything was added.
     */
    fun addMissingPlaces(world: World, known: Set<PlaceId>, random: Random = Random(2026)): Boolean {
        val sim = Sim(world)
        var added = false
        for (place in PlaceId.entries) {
            if (place in known) continue
            populate(world, sim, place, random)
            added = true
        }
        if (added) world.bodies.values.forEach { it.age = 10f }
        return added
    }

    private fun populate(world: World, sim: Sim, place: PlaceId, random: Random) {
        val spec = Places.spec(place)
        val taken = world.people().map { it.name }.filter { it.isNotBlank() }.toSet()
        spec.things.forEach { world.addThing(it.type, it.variant, place, it.x, it.y).age = 10f }
        val fixtures = world.fixturesIn(place)
        for (def in spec.people) {
            if (def.name.isNotBlank() && def.name in taken) continue
            val voice = if (def.species.pet) 1.3f + random.nextFloat() * 0.3f else Look.voiceFor(def.look, random)
            val y = if (def.y > 0f) def.y else place.floor
            val person = world.addPerson(def.species, def.look, voice, place, def.x, y, def.name)
            def.seat?.let { (index, spot) -> sim.seat(person, fixtures[index], spot) }
            def.hat?.let { sim.give(person, world.addThing(it, def.hatVariant, place, def.x, y), Part.HAT) }
            def.glasses?.let { sim.give(person, world.addThing(it, 0, place, def.x, y), Part.GLASSES) }
            def.hand?.let { sim.give(person, world.addThing(it, def.handVariant, place, def.x, y), Part.HAND) }
        }
        sim.settle(place)
    }
}
