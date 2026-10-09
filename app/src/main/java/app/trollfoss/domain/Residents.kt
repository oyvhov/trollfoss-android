package app.trollfoss.domain

/** Named villagers added after the first release, so every older world meets them too. */
object Residents {
    const val MAILINN_FLAG = "people:mailinn:1"
    const val EILEV_FLAG = "people:eilev:portrait:1"

    /** Family-provided reference: loose light-brown waves and teal/white football kit. */
    fun eilevLook() = Look(skin = 0, height = .9f, hair = 18, hairColor = 14, eyes = 6,
        hairSize = 1.06f, hairLength = .83f, face = 1, mouth = 1, top = 10, topColor = 15,
        bottom = 0, bottomColor = 11, shoes = 14, pattern = 6, accent = 9)

    private val oldEilev = Look(skin = 0, height = .9f, hair = 8, hairColor = 5, eyes = 0,
        top = 3, topColor = 1, bottom = 0, bottomColor = 1, shoes = 10, extra = 1)

    /** Only the untouched original appearance changes. Clothes/accessories placed by the child stay. */
    fun updateEilev(world: World) {
        if (!world.flags.add(EILEV_FLAG)) return
        world.people().firstOrNull { it.species == Species.FOLK && it.name == "Eilev" && it.look == oldEilev }
            ?.let { it.look = eilevLook() }
    }

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
