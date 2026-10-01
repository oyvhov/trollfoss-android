package app.trollfoss.domain

import java.time.LocalDate

/** The four seasons. Summer is how the village has always looked; the others change light, ground and air. */
enum class Season { WINTER, SPRING, SUMMER, AUTUMN }

/** What a grown-up can choose: follow the calendar, or keep one season all year. */
enum class SeasonChoice { AUTO, WINTER, SPRING, SUMMER, AUTUMN }

/** Little feasts that decorate the village for a week or two (and can be switched off). */
enum class Festival { NONE, CHRISTMAS, EASTER, PUMPKIN }

/** Seasons and feasts from the calendar. Dates are epoch days, set by the app from the device clock. */
object Seasons {
    fun of(epochDay: Long): Season = when (LocalDate.ofEpochDay(epochDay).monthValue) {
        12, 1, 2 -> Season.WINTER
        3, 4, 5 -> Season.SPRING
        6, 7, 8 -> Season.SUMMER
        else -> Season.AUTUMN
    }

    fun resolve(choice: SeasonChoice, epochDay: Long): Season = when (choice) {
        SeasonChoice.AUTO -> of(epochDay)
        SeasonChoice.WINTER -> Season.WINTER
        SeasonChoice.SPRING -> Season.SPRING
        SeasonChoice.SUMMER -> Season.SUMMER
        SeasonChoice.AUTUMN -> Season.AUTUMN
    }

    /** Christmas from 10 December to 6 January, Easter from a week before to the day after, pumpkins 20 October to 2 November. */
    fun festival(epochDay: Long): Festival {
        val d = LocalDate.ofEpochDay(epochDay)
        val easter = easter(d.year)
        return when {
            !d.isBefore(easter.minusDays(7)) && !d.isAfter(easter.plusDays(1)) -> Festival.EASTER
            d.monthValue == 12 && d.dayOfMonth >= 10 || d.monthValue == 1 && d.dayOfMonth <= 6 -> Festival.CHRISTMAS
            d.monthValue == 10 && d.dayOfMonth >= 20 || d.monthValue == 11 && d.dayOfMonth <= 2 -> Festival.PUMPKIN
            else -> Festival.NONE
        }
    }

    /** Easter Sunday of [year] (the anonymous Gregorian algorithm). */
    fun easter(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}
