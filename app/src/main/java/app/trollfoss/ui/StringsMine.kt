package app.trollfoss.ui

import app.trollfoss.domain.RoomKind
import app.trollfoss.domain.Txt
import app.trollfoss.domain.txt

/** The few words of Mitt hus, in nynorsk and bokmål. The builder is made of pictures; these are for descriptions and the one question. */
object SM {
    val build = txt("Bygg")
    val buildHouse = txt("Bygg huset")
    val rooms = txt("Rom")
    val floor = txt("Bygg ein etasje til", "Bygg en etasje til")
    val look = txt("Utsjånad", "Utseende")
    val party = txt("Innflyttingsfest")
    val done = txt("Ferdig")
    val goInside = txt("Gå inn", "Gå inn")
    val tearDown = txt("Riv rommet?")
    val tearDownBody = txt("Alle møblane blir lagde i lageret.", "Alle møblene blir lagt i lageret.")
    val tearDownYes = txt("Riv")
    val tearDownNo = txt("Behald", "Behold")
    val chooseHouse = txt("Vel eit hus", "Velg et hus")
    val myHouse = txt("Mitt hus")
    val allBuilt = txt("Alle romma står", "Alle rommene står")
    val needRooms = txt("Du treng to rom først", "Du trenger to rom først")
    val upstairsFirst = txt("Riv rommet oppe først", "Riv rommet oppe først")

    fun template(i: Int): Txt = when (i) {
        0 -> txt("Hytte")
        1 -> txt("Villa")
        2 -> txt("Tårnhus")
        else -> txt("Gamalt bondehus", "Gammelt bondehus")
    }

    fun kind(k: RoomKind): Txt = when (k) {
        RoomKind.LIVING -> txt("Stove", "Stue")
        RoomKind.KITCHEN -> txt("Kjøken", "Kjøkken")
        RoomKind.DINING -> txt("Spisestove", "Spisestue")
        RoomKind.BEDROOM -> txt("Soverom")
        RoomKind.KIDS -> txt("Barnerom")
        RoomKind.BATH -> txt("Bad")
        RoomKind.LIBRARY -> txt("Bibliotek")
        RoomKind.WORKSHOP -> txt("Verkstad", "Verksted")
        RoomKind.MUSIC -> txt("Musikkrom")
        RoomKind.GREENHOUSE -> txt("Drivhus")
    }
}
