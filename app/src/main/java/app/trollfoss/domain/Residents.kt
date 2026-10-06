package app.trollfoss.domain

/** Named villagers added after the first release, so every older world meets them too. */
object Residents {
    const val MAILINN_FLAG = "people:mailinn:1"

    /** Long chestnut waves, a green sweater, blue trousers and red shoes; the child can change it all in the workshop. */
    fun mailinnLook(): Look = Look(
        skin = 2, height = 0.9f, hair = 15, hairColor = 2, hairLength = 1.25f, eyes = 1, eyeColor = 2,
        top = 9, topColor = 3, bottom = 0, bottomColor = 6, shoes = 0, extra = 8, mouth = 1, accent = 2,
    )

    /** Mailinn moves into Familiehuset next to Hedda and Øyvind, once, after everyone else (so no other ids change). */
    fun addMailinn(world: World): Person? {
        if (!world.flags.add(MAILINN_FLAG)) return null
        if (world.people().any { it.name.trim().equals("Mailinn", ignoreCase = true) }) return null
        val home = PlaceId.HOME
        val near = world.people().filter { it.place == home && it.name in setOf("Hedda", "Øyvind") }.map { it.x }
        val x = ((near.maxOrNull() ?: 1.2f) + 0.35f).coerceIn(0.3f, home.width - 0.3f)
        val look = mailinnLook()
        return world.addPerson(Species.FOLK, look, Look.voiceFor(look, kotlin.random.Random(1105)), home, x, home.floor, "Mailinn")
    }
}
