package app.trollfoss.ui.theme

import androidx.compose.ui.graphics.Color

/** Portraits, selection cards and temporary scene marks share the player's place in the team. */
object PlayerPalette {
    private val colors = listOf(T.Sun, T.GrapeTop, T.SeaTop, T.MintTop, T.BerryTop)
    private val edges = listOf(T.SunDeep, T.GrapeDeep, T.SeaDeep, T.MintDeep, T.BerryDeep)
    fun color(index: Int): Color = colors[index.mod(colors.size)]
    fun edge(index: Int): Color = edges[index.mod(edges.size)]
}
