package app.trollfoss.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Txt
import app.trollfoss.domain.txt

val LocalMaalform = staticCompositionLocalOf { Maalform.NYNORSK }

@Composable
fun Txt.str(): String = get(LocalMaalform.current)

/**
 * Every word in Trollfoss, in nynorsk and bokmål. Children barely need words; these are for the few
 * labels, the grown-up page and accessibility descriptions.
 */
object S {
    val appName = txt("Trollfoss")

    fun place(id: PlaceId): Txt = when (id) {
        PlaceId.HOME -> txt("Heime", "Hjemme")
        PlaceId.CAFE -> txt("Bakeriet")
        PlaceId.SALON -> txt("Frisøren")
        PlaceId.BEACH -> txt("Stranda", "Stranden")
        PlaceId.FOREST -> txt("Fossen")
        PlaceId.LAB -> txt("Trollhola", "Trollhula")
        PlaceId.MOUNTAIN -> txt("Fjellet")
        PlaceId.FARM -> txt("Garden", "Gården")
        PlaceId.SPACE -> txt("Romstasjonen")
        PlaceId.TIVOLI -> txt("Tivoliet")
        PlaceId.SHOP -> txt("Butikken")
        PlaceId.DOCTOR -> txt("Legekontoret")
        PlaceId.STAGE -> txt("Scena", "Scenen")
        PlaceId.UNDERWATER -> txt("Havbotnen", "Havbunnen")
        PlaceId.HEILEBERGET -> txt("Heileberget")
        PlaceId.MANOR_GROUND -> txt("Storstova")
        PlaceId.MANOR_UPPER -> txt("Andre høgda", "Andre etasje")
        PlaceId.MANOR_ATTIC -> txt("Loftet")
        PlaceId.MANOR_CELLAR -> txt("Kjellaren", "Kjelleren")
        PlaceId.MANOR_GARDEN -> txt("Hagen")
    }

    val map = txt("Kart")
    val workshop = txt("Figurverkstaden")
    val designer = txt("Heimedesignar", "Hjemmedesigner")
    val tasks = txt("Oppdrag")
    val stickers = txt("Klistremerke", "Klistremerker")
    val book = txt("Oppdagingsboka", "Oppdagelsesboka")
    val night = txt("Natt")
    val day = txt("Dag")
    val weather = txt("Vêr", "Vær")
    val camera = txt("Ta bilete", "Ta bilde")
    val close = txt("Lukk")
    val done = txt("Ferdig")
    val random = txt("Tilfeldig figur")
    val newFigure = txt("Ny figur")
    val namePlaceholder = txt("Namn", "Navn")
    val secrets = txt("Glimt")
    val recipes = txt("Oppskrifter")
    val photos = txt("Bilete", "Bilder")
    val parents = txt("For vaksne", "For voksne")
    val telescope = txt("Teleskop")

    // Parent gate
    val gateTitle = txt("For vaksne", "For voksne")
    val gateBody = txt("Løys reknestykket for å opne.", "Løs regnestykket for å åpne.")
    val gateWrong = txt("Ikkje heilt. Prøv igjen.", "Ikke helt. Prøv igjen.")

    // Parent page
    val settings = txt("Innstillingar", "Innstillinger")
    val sound = txt("Lydeffektar", "Lydeffekter")
    val music = txt("Musikk")
    val haptics = txt("Vibrering")
    val language = txt("Målform")
    val nynorsk = txt("Nynorsk")
    val bokmaal = txt("Bokmål")
    val progress = txt("Framgang", "Fremgang")
    fun progressLine(found: Int, total: Int, recipes: Int, allRecipes: Int, figures: Int) = txt(
        "$found av $total glimt · $recipes av $allRecipes oppskrifter · $figures figurar",
        "$found av $total glimt · $recipes av $allRecipes oppskrifter · $figures figurer",
    )
    val reset = txt("Byrj på nytt", "Begynn på nytt")
    val resetBody = txt(
        "Alle figurar, ting og funn blir sette tilbake til starten. Dette kan ikkje angrast.",
        "Alle figurer, ting og funn blir satt tilbake til starten. Dette kan ikke angres.",
    )
    val resetConfirm = txt("Ja, byrj på nytt", "Ja, begynn på nytt")
    val cancel = txt("Avbryt")
    val about = txt("Om Trollfoss")
    val aboutBody = txt(
        "Trollfoss er ein digital leikekasse utan reklame, kjøp, konto eller sporing. Alt blir lagra på eininga. " +
            "Einaste nettkontakt er oppdateringssjekken mot GitHub, som kan slåast av. All grafikk, musikk og lyd er laga i kode.",
        "Trollfoss er en digital lekekasse uten reklame, kjøp, konto eller sporing. Alt lagres på enheten. " +
            "Eneste nettkontakt er oppdateringssjekken mot GitHub, som kan slås av. All grafikk, musikk og lyd er laget i kode.",
    )

    // Updates (same contract as Komet)
    val updates = txt("Appoppdateringar", "Appoppdateringer")
    val updateCheck = txt("Sjekk no", "Sjekk nå")
    val updateDownload = txt("Last ned oppdatering")
    val updateInstall = txt("Installer oppdatering")
    val updateCancel = txt("Avbryt")
    fun updateProgress(percent: Int) = txt("Lastar ned · $percent %", "Laster ned · $percent %")
    fun updateReady(version: String) = txt("Versjon $version er klar", "Versjon $version er klar")
    fun updateSize(mb: String) = txt("$mb MB")
    fun installedVersion(version: String) = txt("Installert: versjon $version")
    val updateAuto = txt("Sjekk automatisk")
    val updateAutoHint = txt(
        "Ser etter ny versjon på GitHub når appen blir opna, høgst to gonger om dagen.",
        "Ser etter ny versjon på GitHub når appen åpnes, høyst to ganger om dagen.",
    )
    val updatePreviews = txt("Testutgåver", "Testutgaver")
    val updatePreviewsHint = txt("Ta med utgåver som ikkje er ferdig testa.", "Ta med utgaver som ikke er ferdig testet.")
    val updateHint = txt(
        "Figurar og ting blir verande. Android ber deg godkjenne installasjonen.",
        "Figurer og ting blir værende. Android ber deg godkjenne installasjonen.",
    )
    val updateCurrent = txt("Du har den nyaste versjonen.", "Du har den nyeste versjonen.")
    val updateNetwork = txt("Fekk ikkje kontakt med GitHub. Prøv igjen.", "Fikk ikke kontakt med GitHub. Prøv igjen.")
    val updateInvalid = txt("Oppdateringa kunne ikkje stadfestast. Ingenting vart installert.", "Oppdateringen kunne ikke bekreftes. Ingenting ble installert.")
    val updateStorage = txt("Kunne ikkje lagre oppdateringa. Sjekk ledig plass.", "Kunne ikke lagre oppdateringen. Sjekk ledig plass.")
    val updatePermission = txt(
        "Tillat at Trollfoss installerer oppdateringar. Gå så tilbake og vel «Installer oppdatering».",
        "Tillat at Trollfoss installerer oppdateringer. Gå så tilbake og velg «Installer oppdatering».",
    )
    val updateInstallError = txt("Android kunne ikkje opne installasjonen. Prøv igjen.", "Android kunne ikke åpne installasjonen. Prøv igjen.")
    val updateAccess = txt("Utgåva er ikkje tilgjengeleg no. Prøv igjen seinare.", "Utgaven er ikke tilgjengelig nå. Prøv igjen senere.")
    val updateRate = txt("GitHub avgrensar førespurnader. Vent litt før du sjekkar igjen.", "GitHub begrenser forespørsler. Vent litt før du sjekker igjen.")
    val newVersion = txt("Ny versjon er klar", "Ny versjon er klar")

    // Workshop categories (for accessibility; the buttons show pictures)
    val catSkin = txt("Hud")
    val catHeight = txt("Høgd", "Høyde")
    val catHair = txt("Frisyre")
    val catHairColor = txt("Hårfarge")
    val catEyes = txt("Auge", "Øyne")
    val catEars = txt("Øyre", "Ører")
    val catTop = txt("Overdel")
    val catTopColor = txt("Farge på overdel")
    val catBottom = txt("Underdel")
    val catBottomColor = txt("Farge på underdel")
    val catShoes = txt("Sko")
    val catExtra = txt("Ekstra")

    // Storhuset Hagen (the garden). The child barely reads here: these are the names the gnomes and frogs go by,
    // for a name tag or the discovery book. The family can change them whenever they like.
    val gardenGnomes = listOf(txt("Gunnar"), txt("Gudrun"), txt("Knut"), txt("Snurre"), txt("Pjokken"))
    val gardenFrogs = listOf(txt("Brumle"), txt("Kvekke"), txt("Plopp"), txt("Hoppla", "Hopla"), txt("Pip"))
    val gardenPond = txt("Dammen")
    val gardenGreenhouse = txt("Drivhuset")
    val gardenTreehouse = txt("Tretopphytta", "Trehytta")
    val gardenShed = txt("Skuret")
    val gardenZip = txt("Taubana", "Tauet")
    val gardenCompost = txt("Komposthaugen", "Kompostkassen")
    val gardenMower = txt("Gressklipparen", "Plenklipperen")
}
