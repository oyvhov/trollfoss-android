package app.trollfoss.domain

/** A visit keeps the camera on a resident or workbench while guests gather on either side. */
data class VisitTarget(val x: Float, val halfWidth: Float, val resident: Person? = null) {
    companion object {
        // Include hair, hats and outstretched arms, also on figures enlarged by a potion.
        fun halfWidthOf(person: Person) = maxOf(person.h * 0.5f, person.w * 0.7f)
    }

    fun guestPositions(place: PlaceId, guests: List<Person>): List<Float> {
        if (guests.isEmpty()) return emptyList()
        val guestRadius = guests.maxOf(::halfWidthOf)
        val edge = maxOf(0.2f, guestRadius)
        val clearance = halfWidth + guestRadius + 0.08f
        val leftSpace = x - edge
        val rightSpace = place.width - edge - x
        val firstSide = if (x <= place.width / 2f) 1f else -1f
        val twoSides = minOf(leftSpace, rightSpace) >= clearance
        val firstCount = if (twoSides) (guests.size + 1) / 2 else guests.size
        return List(guests.size) { i ->
            val first = !twoSides || i % 2 == 0
            val side = if (first) firstSide else -firstSide
            val count = if (first) firstCount else guests.size - firstCount
            val space = if (side > 0f) rightSpace else leftSpace
            // Close ranks for a large party or a nearby wall; keep the centre of the visit clear.
            val span = (space - clearance).coerceIn(0f, 0.70f)
            val spacing = minOf(2 * guestRadius + 0.06f, span / (count - 1).coerceAtLeast(1))
            val row = if (twoSides) i / 2 else i
            (x + side * (clearance + row * spacing)).coerceIn(edge, place.width - edge)
        }
    }
}

/** Local players share the dollhouse and each keep their own figure. No copies or separate worlds. */
object Players {
    const val CHOSEN = "players_chosen"
    fun paused(world: World, person: Person) = "player:paused:${person.id}" in world.flags
    fun resume(world: World, person: Person) { world.flags.remove("player:paused:${person.id}") }

    fun team(world: World): List<Person> = world.playerIds.mapNotNull { world.bodies[it] as? Person }
        .filter { it.species == Species.FOLK }
    fun activeTeam(world: World): List<Person> = team(world).filterNot { paused(world, it) }

    fun toggle(world: World, person: Person) {
        if (world.bodies[person.id] !== person || person.species != Species.FOLK) return
        if (!world.playerIds.remove(person.id)) world.playerIds.add(person.id)
        resume(world, person)
    }

    /** Keep the actual figure and its belongings, but stop following until chosen again. */
    fun pack(world: World, person: Person): Boolean {
        if (world.bodies[person.id] !== person) return false
        person.place?.let { House.moveTo(world, person, it, person.x) }
        if (person.id in world.playerIds) world.flags += "player:paused:${person.id}"
        world.mine.guests.removeAll { it.id == person.id }
        person.mode = Mode.BAG
        person.place = null
        person.held = false
        person.resting = false
        person.z = world.nextZ()
        world.carried(person).forEach { it.place = null }
        return true
    }

    /** Bring the active team into the visible arrival area; paused figures keep their place in the bag. */
    fun arrive(world: World, to: PlaceId, x: Float, y: Float = Float.NaN, visit: VisitTarget? = null) {
        val people = activeTeam(world)
        val guestPositions = visit?.let { target ->
            target.guestPositions(to, people.filterNot { it === target.resident }).iterator()
        }
        for ((i, person) in people.withIndex()) {
            // A selected host who already lives here keeps their seat, belongings and depth.
            world.mine.guests.removeAll { it.id == person.id }
            if (person === visit?.resident && person.place == to) continue
            var cx = when {
                person === visit?.resident -> x
                guestPositions != null -> guestPositions.next()
                else -> (x + (i - (people.size - 1) / 2f) * 0.25f).coerceIn(0.2f, to.width - 0.2f)
            }
            if ((to == PlaceId.MINE_GROUND || to == PlaceId.MINE_UPPER) &&
                (0 until Mine.SLOTS).any { world.mine.standing(to, it) }) {
                cx = Mine.clampX(world.mine, to, cx, person.w / 2f)
            }
            House.moveTo(world, person, to, cx, if (y.isNaN()) PlaceId.FRONT - 0.012f else y)
            person.held = false
            person.anim.nameTag = 2.5f
            person.anim.wave = 1f
            person.z = world.nextZ()
        }
    }
}
