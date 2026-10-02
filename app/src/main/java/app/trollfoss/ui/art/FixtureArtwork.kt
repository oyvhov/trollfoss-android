package app.trollfoss.ui.art

import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId

/** Furniture retains its own drawing when moved out of its original floor. Shared passages use their destination. */
internal fun fixtureArtworkPlace(type: FixtureType, place: PlaceId): PlaceId = when (type.name.substringBefore('_')) {
    "GR" -> PlaceId.MANOR_GROUND
    "UP" -> PlaceId.MANOR_UPPER
    "AT" -> PlaceId.MANOR_ATTIC
    "CE" -> PlaceId.MANOR_CELLAR
    "GA" -> PlaceId.MANOR_GARDEN
    else -> place
}
