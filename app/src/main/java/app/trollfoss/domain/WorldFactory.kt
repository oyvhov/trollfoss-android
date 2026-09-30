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
                // Street lamps are lit, so the park and the beach glow when night falls.
                if (def.type == FixtureType.LAMP_POST) f.on = true
                world.fixtures[f.id] = f
            }
        }
    }

    fun create(random: Random = Random(2026)): World {
        val world = World()
        addFixtures(world)
        val sim = Sim(world)
        for (place in PlaceId.entries) {
            val spec = Places.spec(place)
            spec.things.forEach { world.addThing(it.type, it.variant, place, it.x, it.y).age = 10f }
            val fixtures = world.fixturesIn(place)
            for (def in spec.people) {
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
        world.bodies.values.forEach { it.age = 10f }
        return world
    }
}
