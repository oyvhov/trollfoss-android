package app.trollfoss.domain

/** Written standard for the grown-up pages. Chosen in the app, independent of the device language. */
enum class Maalform { NYNORSK, BOKMAAL }

/**
 * A piece of text in both written standards. Most words are identical, so [nb] defaults to [nn];
 * only the places where the standards differ spell out both.
 */
data class Txt(val nn: String, val nb: String = nn) {
    fun get(maalform: Maalform): String = if (maalform == Maalform.NYNORSK) nn else nb
}

fun txt(nn: String, nb: String = nn) = Txt(nn, nb)
