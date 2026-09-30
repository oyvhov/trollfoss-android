package app.trollvik.domain

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

    /** Height of the hips above the feet, as a fraction of height. */
    const val HIPS = 0.15f

    /** Head radius as a fraction of height. */
    fun headRadius(species: Species): Float = if (species == Species.FOLK) 0.25f else 0.33f

    /** A part's offset from the origin for [pose], in fractions of height. */
    fun fraction(species: Species, pose: Pose, part: Part): FloatArray {
        val base = (if (species == Species.FOLK) folk else pet).getValue(part)
        if (species != Species.FOLK) return base
        return when (pose) {
            Pose.SIT -> floatArrayOf(base[0], base[1] + HIPS)
            // Lying on the back, head to the left: the figure turns a quarter round its middle.
            Pose.LIE -> floatArrayOf(base[1] + 0.5f, -base[0] - 0.22f)
            else -> base
        }
    }

    /** A part's position in the scene. */
    fun at(person: Person, part: Part): FloatArray {
        val f = fraction(person.species, person.anim.pose, part)
        return floatArrayOf(person.x + f[0] * person.h, person.y + f[1] * person.h)
    }
}
