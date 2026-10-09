package app.trollfoss.domain

/** Named villagers and family-approved portraits, including careful upgrades of older worlds. */
object Residents {
    const val MAILINN_FLAG = "people:mailinn:1"
    const val EILEV_FLAG = "people:eilev:portrait:1"
    const val FAMILY_FLAG = "people:family:portraits:1"
    const val MORE_FAMILY_FLAG = "people:family:portraits:2"

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

    fun eiraLook() = Look(skin = 0, height = .78f, hair = 19, hairColor = 15, eyes = 6,
        eyeColor = 3, hairSize = .95f, hairLength = 1.38f, face = 1, mouth = 1,
        top = 13, topColor = 16, bottom = 0, bottomColor = 16, shoes = 9, pattern = 7, accent = 1)

    fun olveLook() = Look(skin = 1, height = .9f, hair = 18, hairColor = 15, eyes = 6,
        eyeColor = 3, hairSize = .93f, hairLength = .67f, face = 0, mouth = 1,
        top = 12, topColor = 17, bottom = 1, bottomColor = 19, shoes = 20, pattern = 9, accent = 21)

    fun tuvaLook() = Look(skin = 0, height = 1.03f, hair = 20, hairColor = 17, eyes = 6,
        hairSize = 1.08f, hairLength = 1.02f, face = 1, nose = 2, mouth = 3,
        top = 8, topColor = 9, bottom = 0, bottomColor = 11, shoes = 13, accent = 9)

    fun oyvindLook() = Look(skin = 1, height = 1.14f, hair = 21, hairColor = 16, eyes = 6,
        hairSize = .95f, hairLength = .75f, face = 2, nose = 1, mouth = 1, extra = 9,
        top = 8, topColor = 18, bottom = 0, bottomColor = 11, shoes = 12, pattern = 8, accent = 9)

    private val familyPortraits = listOf(
        Triple("Eira", Look(skin = 5, height = .78f, hair = 5, hairColor = 0, eyes = 1,
            top = 3, topColor = 0, bottom = 1, bottomColor = 11, shoes = 1), eiraLook()),
        Triple("Olve", Look(skin = 7, height = .9f, hair = 2, hairColor = 0,
            top = 1, topColor = 1, bottom = 1, bottomColor = 3, shoes = 6), olveLook()),
        Triple("Tuva", Look(skin = 5, height = 1.03f, hair = 6, hairColor = 0, eyes = 3,
            top = 2, topColor = 7, bottom = 2, bottomColor = 7, shoes = 8), tuvaLook()),
        Triple("Øyvind", Look(skin = 2, height = 1.14f, hair = 1, hairColor = 1,
            top = 1, topColor = 6, bottom = 0, bottomColor = 11, shoes = 12, extra = 2), oyvindLook()),
    )

    /** Never recreate a person: their id, voice, place, player selection and worn/held things stay. */
    fun updateFamilyPortraits(world: World) {
        if (!world.flags.add(FAMILY_FLAG)) return
        val people = world.people()
        for ((name, old, portrait) in familyPortraits) {
            people.firstOrNull { it.species == Species.FOLK && it.name == name && it.look == old }
                ?.let { it.look = portrait }
        }
    }

    fun sondreLook() = Look(skin = 1, height = 1.03f, hair = 22, hairColor = 0, eyes = 6,
        hairSize = 1.05f, hairLength = .85f, face = 0, nose = 1, mouth = 5, extra = 8,
        top = 14, topColor = 22, bottom = 0, bottomColor = 11, shoes = 22, accent = 10)

    fun eliseLook() = Look(skin = 1, height = 1.03f, hair = 23, hairColor = 14, eyes = 6,
        eyeColor = 3, hairSize = .98f, hairLength = 1.4f, face = 1, mouth = 5, extra = 7,
        top = 9, topColor = 22, bottom = 0, bottomColor = 11, shoes = 12, accent = 2)

    fun solveLook() = Look(skin = 1, height = 1.14f, hair = 22, hairColor = 17, eyes = 6,
        eyeColor = 2, hairSize = .94f, hairLength = .65f, face = 3, nose = 1, mouth = 1, extra = 10,
        top = 13, topColor = 22, bottom = 0, bottomColor = 11, shoes = 12)

    fun heddaLook() = Look(skin = 0, height = .9f, hair = 24, hairColor = 17, eyes = 6,
        eyeColor = 3, hairSize = .96f, hairLength = .92f, face = 1, mouth = 3, extra = 8,
        top = 1, topColor = 24, bottom = 0, bottomColor = 11, shoes = 9, accent = 22)

    fun beritLook() = Look(skin = 1, height = 1.03f, hair = 24, hairColor = 15, eyes = 6,
        hairSize = 1.02f, hairLength = 1.28f, face = 1, mouth = 5, extra = 8,
        top = 12, topColor = 22, bottom = 0, bottomColor = 11, shoes = 22, pattern = 10, accent = 22)

    fun olvarLook() = Look(skin = 0, height = .78f, hair = 25, hairColor = 18, eyes = 6,
        eyeColor = 3, hairSize = 1f, hairLength = .82f, face = 0, mouth = 3,
        top = 15, topColor = 23, bottom = 0, bottomColor = 11, shoes = 23, accent = 22)

    private val moreFamilyPortraits = listOf(
        Triple("Sondre", Look(skin=6,height=1.03f,hair=1,hairColor=0,top=0,topColor=9,bottom=0,bottomColor=11,shoes=11,extra=3), sondreLook()),
        Triple("Elise", Look(skin=0,height=1.03f,hair=6,hairColor=7,eyes=3,top=4,topColor=10,bottom=0,bottomColor=10,shoes=8), eliseLook()),
        Triple("Sølve", Look(skin=3,height=1.14f,hair=1,hairColor=2,top=4,topColor=3,bottom=0,bottomColor=3,shoes=12,extra=3), solveLook()),
        Triple("Hedda", Look(skin=1,height=.78f,hair=3,hairColor=5,top=1,topColor=5,bottom=1,bottomColor=11,shoes=0,extra=1), heddaLook()),
        Triple("Berit", Look(skin=6,height=1.03f,hair=4,hairColor=0,eyes=3,top=1,topColor=8,bottom=2,bottomColor=11,shoes=11), beritLook()),
        Triple("Olvar", Look(skin=4,height=.9f,hair=2,hairColor=0,eyes=1,top=4,topColor=9,bottom=0,bottomColor=9,shoes=10), olvarLook()),
    )

    /** Separate marker: saves that already received the first portraits still receive these six. */
    fun updateMoreFamilyPortraits(world: World) {
        if (!world.flags.add(MORE_FAMILY_FLAG)) return
        val people = world.people()
        for ((name, old, portrait) in moreFamilyPortraits) {
            val person = people.firstOrNull { it.species == Species.FOLK && it.name == name && (it.look == old || it.look == portrait) }
                ?: continue
            person.look = portrait
            // Add these after every original resident, so a fresh world's existing ids do not shift.
            // They are ordinary removable glasses; never replace something the child put on the face.
            if (name == "Berit" && world.worn(person, Slot.FACE) == null) {
                val glasses = world.addThing(ThingType.ROUND_GLASSES, 1, person.place ?: PlaceId.HOME, person.x, person.y)
                Sim(world).apply { tasks.recording = false }.give(person, glasses, Part.GLASSES)
            }
        }
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
