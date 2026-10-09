package app.trollfoss.domain

/** Points on a figure that things attach to or are dropped on. */
enum class Part { HAT, GLASSES, MOUTH, HAND, BODY, HAIR, HEAD }

/**
 * Where the parts of a figure are, as fractions of its height from its origin. The origin is the feet
 * when standing, the hips when sitting and the middle of the back when lying. The art draws figures to
 * these same numbers, so a hat lands on the head and a spoon reaches the mouth.
 */
object Anatomy {
    /** Standing figure, origin at the feet, y up is negative. */
    private val folk = mapOf(
        Part.HAT to floatArrayOf(0f, -0.905f),
        Part.GLASSES to floatArrayOf(0f, -0.70f),
        Part.MOUTH to floatArrayOf(0f, -0.575f),
        Part.HAND to floatArrayOf(0.245f, -0.245f),
        Part.BODY to floatArrayOf(0f, -0.32f),
        Part.HAIR to floatArrayOf(0f, -0.86f),
        Part.HEAD to floatArrayOf(0f, -0.70f),
    )

    /** Animals sit up facing us; they carry things in the mouth. */
    private val pet = mapOf(
        Part.HAT to floatArrayOf(0f, -0.9f),
        Part.GLASSES to floatArrayOf(0f, -0.64f),
        Part.MOUTH to floatArrayOf(0f, -0.49f),
        Part.HAND to floatArrayOf(0f, -0.42f),
        Part.BODY to floatArrayOf(0f, -0.22f),
        Part.HAIR to floatArrayOf(0f, -0.85f),
        Part.HEAD to floatArrayOf(0f, -0.62f),
    )

    /**
     * Rolf the robot butler: a boxy head on a boxy body, the glowing screen is the face. His right hand
     * (the left of the picture) always holds a little serving tray, so things go on it.
     */
    private val robot = mapOf(
        Part.HAT to floatArrayOf(0f, -0.905f),
        Part.GLASSES to floatArrayOf(0f, -0.742f),
        Part.MOUTH to floatArrayOf(0f, -0.64f),
        Part.HAND to floatArrayOf(0.335f, -0.43f),
        Part.BODY to floatArrayOf(0f, -0.33f),
        Part.HAIR to floatArrayOf(0f, -0.88f),
        Part.HEAD to floatArrayOf(0f, -0.725f),
    )

    /** Sture the ghost: a round head on a sheet, floating; he holds things up on his right arm, beside his tummy. */
    private val ghost = mapOf(
        Part.HAT to floatArrayOf(0f, -0.965f),
        Part.GLASSES to floatArrayOf(0f, -0.70f),
        Part.MOUTH to floatArrayOf(0f, -0.595f),
        Part.HAND to floatArrayOf(0.27f, -0.38f),
        Part.BODY to floatArrayOf(0f, -0.45f),
        Part.HAIR to floatArrayOf(0f, -0.93f),
        Part.HEAD to floatArrayOf(0f, -0.70f),
    )

    /** Height of the hips above the feet, as a fraction of height. */
    const val HIPS = 0.15f

    /** Head radius as a fraction of height. */
    fun headRadius(species: Species): Float = when (species) {
        Species.FOLK -> 0.25f
        Species.ROBOT -> 0.30f
        Species.GHOST -> 0.31f
        else -> 0.33f
    }

    /** Folk, the robot and the ghost stand upright, so they sit on seats and lie in beds like people do. */
    fun upright(species: Species): Boolean = species == Species.FOLK || species == Species.ROBOT || species == Species.GHOST

    private fun table(species: Species) = when (species) {
        Species.FOLK -> folk
        Species.ROBOT -> robot
        Species.GHOST -> ghost
        else -> pet
    }

    /** A part's offset from the origin for [pose], in fractions of height. */
    fun fraction(species: Species, pose: Pose, part: Part): FloatArray {
        val base = table(species).getValue(part)
        if (!upright(species)) return base
        return when (pose) {
            Pose.SIT -> floatArrayOf(base[0], base[1] + HIPS)
            // Lying on the back, head to the left: the figure turns a quarter round its middle.
            Pose.LIE -> floatArrayOf(base[1] + 0.5f, -base[0] - 0.22f)
            else -> base
        }
    }

    /** A part's position in the scene. */
    fun at(person: Person, part: Part, mirrored: Boolean = true): FloatArray {
        val a = person.anim
        val base = table(person.species).getValue(part)
        var p = FigurePose.Point(base[0], base[1])
        if (person.species == Species.FOLK && part == Part.HAND) {
            p = FigurePose.hand(a, right = true, holding = a.holding)
            p = FigurePose.Point(p.x, p.y + FigurePose.bob(a))
        } else if (FigurePose.headPart(part) || (!upright(person.species) && part == Part.HAND)) {
            p = FigurePose.head(person.species, a).at(p.x, p.y)
        } else if (person.species == Species.GHOST) {
            p = FigurePose.head(person.species, a).at(p.x, p.y)
        } else if (person.species == Species.ROBOT && part == Part.HAND) {
            p = FigurePose.Point(p.x, p.y + FigurePose.bob(a))
        }
        p = FigurePose.posed(person.species, a.pose, p)
        return floatArrayOf(person.x + p.x * person.h * (if (mirrored) a.facing else 1f), person.y + p.y * person.h)
    }

}
