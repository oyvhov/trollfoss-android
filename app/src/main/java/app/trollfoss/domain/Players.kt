package app.trollfoss.domain

/** Local players share the dollhouse and each keep their own figure. No copies or separate worlds. */
object Players {
    const val CHOSEN = "players_chosen"

    fun team(world: World): List<Person> = world.playerIds.mapNotNull { world.bodies[it] as? Person }
        .filter { it.species == Species.FOLK }

    fun toggle(world: World, person: Person) {
        if (world.bodies[person.id] !== person || person.species != Species.FOLK) return
        if (!world.playerIds.remove(person.id)) world.playerIds.add(person.id)
    }

    /** Keep the actual figure and its belongings, but stop following until chosen again. */
    fun pack(world: World, person: Person): Boolean {
        if (world.bodies[person.id] !== person) return false
        person.place?.let { House.moveTo(world, person, it, person.x) }
        world.playerIds.remove(person.id)
        world.mine.guests.removeAll { it.id == person.id }
        person.mode = Mode.BAG
        person.place = null
        person.held = false
        person.resting = false
        person.z = world.nextZ()
        world.carried(person).forEach { it.place = null }
        return true
    }

    /** Bring everyone, including a seated or packed figure, into the visible arrival area. */
    fun arrive(world: World, to: PlaceId, x: Float, y: Float = Float.NaN) {
        val people = team(world)
        for ((i, person) in people.withIndex()) {
            val spread = (i - (people.size - 1) / 2f) * 0.25f
            var cx = (x + spread).coerceIn(0.2f, to.width - 0.2f)
            if ((to == PlaceId.MINE_GROUND || to == PlaceId.MINE_UPPER) &&
                (0 until Mine.SLOTS).any { world.mine.standing(to, it) }) {
                cx = Mine.clampX(world.mine, to, cx, person.w / 2f)
            }
            House.moveTo(world, person, to, cx, y)
            person.held = false
            person.anim.nameTag = 2.5f
            person.anim.wave = 1f
            person.z = world.nextZ()
            // Do not send a selected party guest home later.
            world.mine.guests.removeAll { it.id == person.id }
        }
    }
}
