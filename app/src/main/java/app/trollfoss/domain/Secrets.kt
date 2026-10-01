package app.trollfoss.domain

/**
 * A glimt: a hidden star. Plain ones sit in the scene, often under a thing. [inside] ones sit inside a
 * cupboard (fixture index in the place) and only show while it is open. [event] ones appear when the
 * child does something special; the engine calls [World.unlock] with the secret's id.
 */
data class Secret(val id: String, val place: PlaceId, val x: Float, private val y0: Float, val inside: Int = -1, val event: Boolean = false, val on: Int = -1) {
    /** Height on screen; a glimt on or in a piece of furniture moves with that furniture's depth. */
    val y: Float get() = y0 + if (on >= 0) Places.spec(place).fixtures[on].shift else 0f
}

object Secrets {
    val all: List<Secret> = listOf(
        Secret("home_pillow", PlaceId.HOME, 0.27f, 0.765f, on = 2),
        Secret("home_fridge", PlaceId.HOME, 2.61f, 0.645f, inside = 15, on = 15),
        Secret("home_clock", PlaceId.HOME, 1.33f, 0.58f, event = true),

        Secret("cafe_case", PlaceId.CAFE, 1.48f, 0.785f, inside = 8, on = 8),
        Secret("cafe_bake", PlaceId.CAFE, 1.72f, 0.6f, event = true, on = 9),
        Secret("cafe_register", PlaceId.CAFE, 0.73f, 0.6f, event = true, on = 3),

        Secret("salon_hat", PlaceId.SALON, 0.33f, 0.445f),
        Secret("salon_dryer", PlaceId.SALON, 1.88f, 0.5f, event = true, on = 9),
        Secret("salon_mirror", PlaceId.SALON, 0.7f, 0.41f, event = true),

        Secret("beach_seabed", PlaceId.BEACH, 3.7f, 0.945f),
        Secret("beach_castle", PlaceId.BEACH, 0.97f, 0.66f, event = true, on = 2),
        Secret("beach_treasure", PlaceId.BEACH, 2.7f, 0.64f, event = true),

        Secret("forest_tent", PlaceId.FOREST, 0.7f, 0.845f, inside = 1, on = 1),
        Secret("forest_owl", PlaceId.FOREST, 0.3f, 0.47f, event = true, on = 0),
        Secret("forest_night", PlaceId.FOREST, 2.7f, 0.22f, event = true),

        Secret("lab_mix", PlaceId.LAB, 0.98f, 0.62f, event = true, on = 2),
        Secret("lab_telescope", PlaceId.LAB, 1.74f, 0.52f, event = true, on = 4),
        Secret("lab_crystal", PlaceId.LAB, 2.13f, 0.58f, event = true, on = 6),

        Secret("mountain_wish", PlaceId.MOUNTAIN, 3.1f, 0.62f, event = true, on = 6),
        Secret("mountain_jump", PlaceId.MOUNTAIN, 2.56f, 0.46f, event = true, on = 4),
        Secret("mountain_tree", PlaceId.MOUNTAIN, 0.24f, 0.6f, event = true, on = 0),

        Secret("farm_drive", PlaceId.FARM, 1.15f, 0.5f, event = true),
        Secret("farm_egg", PlaceId.FARM, 2.0f, 0.46f, event = true, on = 2),
        Secret("farm_build", PlaceId.FARM, 3.7f, 0.55f, event = true, on = 6),

        Secret("space_launch", PlaceId.SPACE, 0.35f, 0.18f, event = true, on = 0),
        Secret("space_gravity", PlaceId.SPACE, 1.35f, 0.55f, event = true, on = 3),
        Secret("space_orrery", PlaceId.SPACE, 1.8f, 0.45f, event = true, on = 4),

        Secret("tivoli_top", PlaceId.TIVOLI, 0.45f, 0.1f, event = true, on = 0),
        Secret("tivoli_cans", PlaceId.TIVOLI, 3.04f, 0.42f, event = true, on = 5),
        Secret("tivoli_jump", PlaceId.TIVOLI, 2.22f, 0.3f, event = true, on = 3),

        Secret("shop_freezer", PlaceId.SHOP, 1.86f, 0.845f, inside = 4, on = 4),
        Secret("shop_scan", PlaceId.SHOP, 2.72f, 0.52f, event = true, on = 6),
        Secret("shop_scale", PlaceId.SHOP, 1.32f, 0.62f, event = true, on = 3),

        Secret("doctor_xray", PlaceId.DOCTOR, 1.12f, 0.3f, event = true, on = 3),
        Secret("doctor_cabinet", PlaceId.DOCTOR, 2.2f, 0.395f, inside = 5, on = 5),
        Secret("doctor_heart", PlaceId.DOCTOR, 1.66f, 0.5f, event = true, on = 4),

        Secret("stage_band", PlaceId.STAGE, 1.0f, 0.42f, event = true, on = 0),
        Secret("stage_disco", PlaceId.STAGE, 1.0f, 0.33f, event = true),
        Secret("stage_boom", PlaceId.STAGE, 1.95f, 0.45f, event = true, on = 4),

        Secret("sea_clam", PlaceId.UNDERWATER, 1.39f, 0.87f, inside = 2, on = 2),
        Secret("sea_wreck", PlaceId.UNDERWATER, 0.57f, 0.85f, inside = 0, on = 0),
        Secret("sea_octopus", PlaceId.UNDERWATER, 2.98f, 0.52f, event = true, on = 5),

        Secret("berg_cable", PlaceId.HEILEBERGET, 6.05f, 0.34f, event = true, on = 10),
        Secret("berg_echo", PlaceId.HEILEBERGET, 4.4f, 0.4f, event = true, on = 6),
        Secret("berg_top", PlaceId.HEILEBERGET, 8.4f, 0.13f, event = true, on = 12),

        // Storhuset. Three glimt per floor at least; each floor's builder replaces its stand-ins below
        // (an `on` or `inside` is a blueprint index in that floor).
        // ---- ground floor ----
        Secret("ground_hall", PlaceId.MANOR_GROUND, 1.0f, 0.3f, event = true),
        Secret("ground_library", PlaceId.MANOR_GROUND, 4.8f, 0.3f, event = true),
        Secret("ground_kitchen", PlaceId.MANOR_GROUND, 8.8f, 0.3f, event = true),

        // ---- upper floor ----
        Secret("upper_slide", PlaceId.MANOR_UPPER, 5.0f, 0.3f, event = true),
        Secret("upper_bath", PlaceId.MANOR_UPPER, 7.4f, 0.3f, event = true),
        Secret("upper_balcony", PlaceId.MANOR_UPPER, 11.2f, 0.3f, event = true),

        // ---- attic ----
        Secret("attic_ghost", PlaceId.MANOR_ATTIC, 4.0f, 0.3f, event = true),
        Secret("attic_tower", PlaceId.MANOR_ATTIC, 6.4f, 0.3f, event = true),
        Secret("attic_secret", PlaceId.MANOR_ATTIC, 8.0f, 0.3f, event = true),

        // ---- cellar ----
        Secret("cellar_party", PlaceId.MANOR_CELLAR, 9.0f, 0.3f, event = true),
        Secret("cellar_pool", PlaceId.MANOR_CELLAR, 7.0f, 0.3f, event = true),
        Secret("cellar_tunnel", PlaceId.MANOR_CELLAR, 9.4f, 0.3f, event = true),

        // ---- garden ----
        Secret("garden_pond", PlaceId.MANOR_GARDEN, 5.0f, 0.3f, event = true),
        Secret("garden_tree", PlaceId.MANOR_GARDEN, 7.4f, 0.3f, event = true),
        Secret("garden_greenhouse", PlaceId.MANOR_GARDEN, 3.5f, 0.3f, event = true),

        // Mitt hus: three glimt in the yard, brought out by building (the builder replaces the positions).
        Secret("mine_start", PlaceId.MINE_YARD, 1.6f, 0.45f, event = true),
        Secret("mine_second_floor", PlaceId.MINE_YARD, 7.6f, 0.3f, event = true),
        Secret("mine_housewarming", PlaceId.MINE_YARD, 3.0f, 0.62f, event = true),
    )

    fun byId(id: String): Secret? = all.firstOrNull { it.id == id }

    fun inPlace(place: PlaceId): List<Secret> = all.filter { it.place == place }
}
