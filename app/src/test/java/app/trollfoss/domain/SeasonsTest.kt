package app.trollfoss.domain

import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

/** Seasons and feasts follow the calendar unless the grown-ups choose otherwise. */
class SeasonsTest {
    private fun day(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d).toEpochDay()

    @Test
    fun `the calendar gives the season`() {
        assertEquals(Season.WINTER, Seasons.of(day(2026, 1, 15)))
        assertEquals(Season.WINTER, Seasons.of(day(2026, 12, 24)))
        assertEquals(Season.SPRING, Seasons.of(day(2026, 4, 30)))
        assertEquals(Season.SUMMER, Seasons.of(day(2026, 7, 4)))
        assertEquals(Season.AUTUMN, Seasons.of(day(2026, 10, 1)))
    }

    @Test
    fun `a chosen season beats the calendar`() {
        val july = day(2026, 7, 4)
        assertEquals(Season.SUMMER, Seasons.resolve(SeasonChoice.AUTO, july))
        assertEquals(Season.WINTER, Seasons.resolve(SeasonChoice.WINTER, july))
        assertEquals(Season.AUTUMN, Seasons.resolve(SeasonChoice.AUTUMN, july))
    }

    @Test
    fun `easter is found by the church calendar`() {
        assertEquals(LocalDate.of(2026, 4, 5), Seasons.easter(2026))
        assertEquals(LocalDate.of(2027, 3, 28), Seasons.easter(2027))
        assertEquals(LocalDate.of(2028, 4, 16), Seasons.easter(2028))
    }

    @Test
    fun `feasts last a week or two`() {
        assertEquals(Festival.CHRISTMAS, Seasons.festival(day(2026, 12, 24)))
        assertEquals(Festival.CHRISTMAS, Seasons.festival(day(2027, 1, 3)))
        assertEquals(Festival.NONE, Seasons.festival(day(2026, 12, 5)))
        assertEquals(Festival.EASTER, Seasons.festival(day(2026, 3, 30)))
        assertEquals(Festival.EASTER, Seasons.festival(day(2026, 4, 6)))
        assertEquals(Festival.NONE, Seasons.festival(day(2026, 4, 20)))
        assertEquals(Festival.PUMPKIN, Seasons.festival(day(2026, 10, 31)))
        assertEquals(Festival.NONE, Seasons.festival(day(2026, 10, 1)))
    }

    @Test
    fun `the season choice is saved with the settings`() {
        val world = WorldFactory.create(Random(1))
        val json = WorldStore.encode(world, Settings(season = SeasonChoice.AUTUMN, festive = false))
        val saved = WorldStore.decode(json)
        assertEquals(SeasonChoice.AUTUMN, saved.settings.season)
        assertEquals(false, saved.settings.festive)
        assertEquals(SeasonChoice.AUTO, WorldStore.decode(WorldStore.encode(world, Settings())).settings.season)
    }
}
