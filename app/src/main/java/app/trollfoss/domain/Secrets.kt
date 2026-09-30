package app.trollfoss.domain

/**
 * A glimt: a hidden star. Plain ones sit in the scene, often under a thing. [inside] ones sit inside a
 * cupboard (fixture index in the place) and only show while it is open. [event] ones appear when the
 * child does something special; the engine calls [World.unlock] with the secret's id.
 */
data class Secret(val id: String, val place: PlaceId, val x: Float, val y: Float, val inside: Int = -1, val event: Boolean = false)

object Secrets {
    val all: List<Secret> = listOf(
        Secret("home_pillow", PlaceId.HOME, 0.27f, 0.765f),
        Secret("home_fridge", PlaceId.HOME, 2.61f, 0.645f, inside = 15),
        Secret("home_clock", PlaceId.HOME, 1.33f, 0.58f, event = true),

        Secret("cafe_case", PlaceId.CAFE, 1.48f, 0.785f, inside = 8),
        Secret("cafe_bake", PlaceId.CAFE, 1.72f, 0.6f, event = true),
        Secret("cafe_register", PlaceId.CAFE, 0.73f, 0.6f, event = true),

        Secret("salon_hat", PlaceId.SALON, 0.33f, 0.445f),
        Secret("salon_dryer", PlaceId.SALON, 1.88f, 0.5f, event = true),
        Secret("salon_mirror", PlaceId.SALON, 0.7f, 0.41f, event = true),

        Secret("beach_seabed", PlaceId.BEACH, 3.7f, 0.945f),
        Secret("beach_castle", PlaceId.BEACH, 0.97f, 0.66f, event = true),
        Secret("beach_treasure", PlaceId.BEACH, 2.7f, 0.64f, event = true),

        Secret("forest_tent", PlaceId.FOREST, 0.7f, 0.845f, inside = 1),
        Secret("forest_owl", PlaceId.FOREST, 0.3f, 0.47f, event = true),
        Secret("forest_night", PlaceId.FOREST, 2.9f, 0.22f, event = true),

        Secret("lab_mix", PlaceId.LAB, 0.98f, 0.62f, event = true),
        Secret("lab_telescope", PlaceId.LAB, 1.74f, 0.52f, event = true),
        Secret("lab_crystal", PlaceId.LAB, 2.13f, 0.58f, event = true),

        Secret("mountain_wish", PlaceId.MOUNTAIN, 3.1f, 0.62f, event = true),
        Secret("mountain_jump", PlaceId.MOUNTAIN, 2.56f, 0.46f, event = true),
        Secret("mountain_tree", PlaceId.MOUNTAIN, 0.24f, 0.6f, event = true),
    )

    fun byId(id: String): Secret? = all.firstOrNull { it.id == id }

    fun inPlace(place: PlaceId): List<Secret> = all.filter { it.place == place }
}
