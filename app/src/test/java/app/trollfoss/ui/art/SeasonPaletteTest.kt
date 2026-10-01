package app.trollfoss.ui.art

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import app.trollfoss.domain.Festival
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The palette of the year: summer is the plain look and must stay exactly as it was; the other seasons change it. */
class SeasonPaletteTest {
    private fun pen(season: Season) = Pen(lw = 2f, season = season)

    private val samples = listOf(Color(0xFF6FAE5A), Color(0xFF3F7D62), Color(0xFFE6C48A), Color(0xFF55505C), Color(0xFF2E7D46))

    @Test
    fun `summer leaves every colour untouched`() {
        val p = pen(Season.SUMMER)
        for (c in samples) {
            assertEquals(c, p.ground(c))
            assertEquals(c, p.farGround(c))
            assertEquals(c, p.farTrees(c))
            assertEquals(c, p.conifer(c))
            assertEquals(c, p.foliage(c, 3))
            assertEquals(c, p.sandy(c))
            assertEquals(c, p.snowy(c))
            assertEquals(c, p.blade(c))
            assertEquals(c, p.plant(c))
            for (i in 0 until 4) assertEquals(c, p.field(i, c))
        }
    }

    @Test
    fun `winter turns the ground and the roofs white`() {
        val p = pen(Season.WINTER)
        val grass = Color(0xFF6FAE5A)
        assertTrue(p.ground(grass).luminance() > 0.7f)
        assertTrue(p.snowy(Color(0xFF55505C)).luminance() > 0.6f)
        assertTrue(p.sandy(Color(0xFFE6C48A)).luminance() > Color(0xFFE6C48A).luminance())
    }

    @Test
    fun `autumn leaves are orange red or gold and each tree keeps its own colour`() {
        val p = pen(Season.AUTUMN)
        val green = Color(0xFF6DBB5A)
        val leaves = (0 until 12).map { p.foliage(green, it) }
        assertTrue("trees differ", leaves.toSet().size > 2)
        for (c in leaves) assertTrue("redder than green: $c", c.red > c.green * 0.9f && c.red > c.blue)
        assertEquals(p.foliage(green, 7), p.foliage(green, 7))
    }

    @Test
    fun `recolouring keeps the lightness so cel shading survives`() {
        val dark = Color(0xFF2E7D46)
        val light = Color(0xFF9CCB5A)
        val target = SeasonPal.autumn[0]
        val a = recolor(dark, target, 1f)
        val b = recolor(light, target, 1f)
        assertTrue(b.luminance() > a.luminance())
        assertEquals(dark.alpha, a.alpha, 0f)
    }

    @Test
    fun `spring is a fresh green, not the plain one`() {
        val grass = Color(0xFF6FAE5A)
        val spring = pen(Season.SPRING).ground(grass)
        assertNotEquals(grass, spring)
        assertTrue(spring.green > spring.red && spring.green > spring.blue)
        // Fresher means more yellow-green than blue-green.
        assertTrue(spring.red / spring.green >= grass.red / grass.green)
    }

    @Test
    fun `the fields change with the year`() {
        val c = Color(0xFFEBCB5E)
        val seen = Season.entries.map { pen(it).field(0, c) }.toSet()
        assertEquals(4, seen.size)
    }

    @Test
    fun `every place has decorations for every feast`() {
        for (feast in listOf(Festival.CHRISTMAS, Festival.EASTER, Festival.PUMPKIN)) {
            val dressed = feastPlaces(feast)
            for (place in PlaceId.entries.filter { !it.manor }) {
                assertTrue("$place has no $feast decorations", place in dressed)
            }
        }
    }
}
