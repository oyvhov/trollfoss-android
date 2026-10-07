package app.trollfoss.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Txt
import app.trollfoss.domain.txt
import app.trollfoss.domain.ToyReward
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Task

val LocalMaalform = staticCompositionLocalOf { Maalform.NYNORSK }

object SP {
    val gifts=Txt("Gåver", "Gaver")
    val free=Txt("Tilgjengeleg no", "Tilgjengelig nå")
    val tryIt=Txt("Prøv", "Prøv")
    val doIt=Txt("Bruk", "Bruk")
    val color=Txt("Farge", "Farge")
    val help=Txt("Hent hjelp", "Hent hjelp")
    val swap=Txt("Vel noko anna", "Velg noe annet")
    val newTasks=Txt("Nye oppdrag", "Nye oppdrag")
    val release=Txt("Ta ut tingen", "Ta ut tingen")
    val undo=Txt("Angre", "Angre")
    val noSpace=Txt("Her er fullt. Legg ei leike på lager og prøv igjen.", "Her er fullt. Legg en leke på lager og prøv igjen.")
    val allReady=Txt("Alle desse leikene er klare!", "Alle disse lekene er klare!")
    val newGifts=Txt("Nye leiker til deg!", "Nye leker til deg!")
    val keepStickers=Txt("Merka dine blir verande. Du kan velje eit anna oppdrag.", "Merkene dine blir værende. Du kan velge et annet oppdrag.")
    val noFriend=Txt("Set ein venn ved sida av kameraet.", "Sett en venn ved siden av kameraet.")
    val trainFound=Txt("Det vesle toget", "Det lille toget")
    fun level(n:Int)=Txt("Nivå $n", "Nivå $n")
    fun missing(n:Int)=Txt("$n merke til nye leiker", "$n ${if(n==1) "merke" else "merker"} til nye leker")
    fun name(t:ToyReward):Txt=when(t) {
        ToyReward.BUBBLES -> Txt("Boblemaskin","Boblemaskin")
        ToyReward.WINDMILL -> Txt("Vindmølle","Vindmølle")
        ToyReward.BUS -> Txt("Vennebuss","Vennebuss")
        ToyReward.PILLOW -> Txt("Putekastar","Putekaster")
        ToyReward.MARBLES -> Txt("Klinkekulebane","Klinkekulebane")
        ToyReward.COLORS -> Txt("Fargesprøyte","Fargesprøyte")
        ToyReward.LIFT -> Txt("Miniheis","Miniheis")
        ToyReward.POPCORN -> Txt("Popcornvogn","Popcornvogn")
        ToyReward.PUMP -> Txt("Leikepumpe","Lekepumpe")
        ToyReward.CAMERA -> Txt("Vennekamera","Vennekamera")
        ToyReward.TRAIN -> Txt("Leiketog","Leketog")
        else -> SC.toyName(t.type)
    }
    fun trainStep(stage:Int):Txt=when(stage) {
        0 -> Txt("Finn tannhjulet ved toget i Andre høgda.","Finn tannhjulet ved toget i Andre etasje.")
        1 -> Txt("Dra tannhjulet til toget.","Dra tannhjulet til toget.")
        2 -> Txt("Set ein venn oppi toget.","Sett en venn oppi toget.")
        else -> Txt("Du har reparert toget! Du kan hente nye tog her.","Du har reparert toget! Du kan hente nye tog her.")
    }
    fun use(type:FixtureType):Txt=when(type) {
        FixtureType.PLAY_BUBBLES -> Txt("Start boblene. Trykk på ei boble!","Start boblene. Trykk på en boble!")
        FixtureType.PLAY_WINDMILL -> Txt("Dra hårfønaren til mølla. Ho snurrar!","Dra hårføneren til mølla. Den snurrer!")
        FixtureType.PLAY_BUS,FixtureType.PLAY_TRAIN -> Txt("Set oppi to venner. Trykk på køyrepilene.","Sett oppi to venner. Trykk på kjørepilene.")
        FixtureType.PLAY_LAUNCHER -> Txt("Dra bamsen til puta. Trykk Bruk for eit mjukt kast.","Dra bamsen til puta. Trykk Bruk for et mykt kast.")
        FixtureType.PLAY_MARBLES -> Txt("Vend dei tre rennene. Dra kula øvst i bana.","Vend de tre rennene. Dra kula øverst i banen.")
        FixtureType.PLAY_COLORS -> Txt("Vel farge. Dra ein ball til sprøyta. Vask i vasken for å få fargen tilbake.","Velg farge. Dra en ball til sprøyta. Vask i vasken for å få fargen tilbake.")
        FixtureType.PLAY_LIFT -> Txt("Set oppi ein venn eller dra ein bamse til heisen. Trykk Bruk: opp og ned!","Sett oppi en venn eller dra en bamse til heisen. Trykk Bruk: opp og ned!")
        FixtureType.PLAY_POPCORN -> Txt("Dra maisen til vogna. Så blir han popcorn til vennene!","Dra maisen til vogna. Så blir den popcorn til vennene!")
        FixtureType.PLAY_PUMP -> Txt("Dra ballen til pumpa: større ball! Ei bøtte får vatn.","Dra ballen til pumpa: større ball! En bøtte får vann.")
        FixtureType.PLAY_CAMERA -> Txt("Set ein venn ved kameraet. Trykk Bruk: eit bilete til veggen!","Sett en venn ved kameraet. Trykk Bruk: et bilde til veggen!")
        else -> SC.toyUse(type)
    }
    fun taskHint(task:Task):Txt=when(task.id) {
        "feed_horse" -> Txt("Dra gulrota til hesten.","Dra gulroten til hesten.")
        "feed_dog" -> Txt("Dra pølsa til hunden.","Dra pølsa til hunden.")
        "bedtime", "bath" -> if(task.id=="bedtime") Txt("Dra ein venn til senga.","Dra en venn til senga.") else Txt("Dra ein venn til badekaret.","Dra en venn til badekaret.")
        "crown" -> Txt("Dra krona til hovudet til ein venn.","Dra krona til hodet til en venn.")
        "tractor", "submarine" -> Txt("Set oppi ein venn og køyr.","Sett oppi en venn og kjør.")
        "harvest" -> Txt("Trykk på gulrota i kjøkkenhagen.","Trykk på gulroten i kjøkkenhagen.")
        "bake_cake" -> Txt("Dra mjøl, egg og mjølk til omnen.","Dra mel, egg og melk til ovnen.")
        "smoothie" -> Txt("Dra frukt og mjølk til blandaren.","Dra frukt og melk til blenderen.")
        "egg_to_cafe", "fish_home" -> Txt("Legg tingen i sekken og ta henne med til staden på kortet.","Legg tingen i sekken og ta den med til stedet på kortet.")
        "cans" -> Txt("Kast ein ball mot boksane.","Kast en ball mot boksene.")
        "catch_fish" -> Txt("Trykk ved fiskestonga når fisken nappar.","Trykk ved fiskestanga når fisken napper.")
        "cable_car", "ski_jump", "ferris_top", "bounce", "xray" -> Txt("Dra ein venn til leika på biletet.","Dra en venn til leken på bildet.")
        "scan" -> Txt("Dra varer til kassa.","Dra varer til kassa.")
        "haircut", "dress_up", "heart" -> Txt("Dra tingen på biletet til ein venn.","Dra tingen på bildet til en venn.")
        "sing", "disco", "ink", "gravity", "echo", "summit", "launch", "brew" -> Txt("Trykk på tingen på biletet.","Trykk på tingen på bildet.")
        "tidy" -> Txt("Trykk på kosten i møbelmenyen.","Trykk på kosten i møbelmenyen.")
        "paint" -> Txt("Vel ein ny veggfarge i møbelmenyen.","Velg en ny veggfarge i møbelmenyen.")
        "furnish" -> Txt("Dra inn møblar frå møbelmenyen.","Dra inn møbler fra møbelmenyen.")
        "photo" -> Txt("Trykk på kameraet og ta eit bilete.","Trykk på kameraet og ta et bilde.")
        "wishes" -> Txt("Sjå kva ein venn ønskjer seg, og gi det til venen.","Se hva en venn ønsker seg, og gi det til vennen.")
        "glimt" -> Txt("Finn og trykk på gylne glimt.","Finn og trykk på gylne glimt.")
        else -> Txt("Prøv handlinga på biletet. Prikkane viser kor mange gonger.","Prøv handlingen på bildet. Prikkene viser hvor mange ganger.")
    }
}

@Composable
fun Txt.str(): String = get(LocalMaalform.current)

/**
 * Every word in Trollfoss, in nynorsk and bokmål. Children barely need words; these are for the few
 * labels, the grown-up page and accessibility descriptions.
 */
object S {
    val playCards = Txt("Leik og eventyr", "Lek og eventyr")
    val playBuildToys = Txt("Bygg leiker", "Bygg leker")
    val playAdventures = Txt("Eventyr", "Eventyr")
    val playThings = Txt("Dette kan tingen gjere", "Dette kan tingen gjøre")
    val playHug = Txt("Kose", "Kose")
    val playThrow = Txt("Kaste mjukt", "Kaste mykt")
    val playNap = Txt("Kvile", "Hvile")
    val playHide = Txt("Borte – titt-tei!", "Borte – titt-tei!")
    val playRead = Txt("Lese", "Lese")
    val playStory = Txt("Fortelje for venner", "Fortelle for venner")
    val playLight = Txt("Lyse", "Lyse")
    val playTwinkle = Txt("Stjernelys", "Stjernelys")
    val playFort = Txt("Putehytte", "Putehytte")
    val playCart = Txt("Trillevogn", "Trillevogn")
    val playKit = Txt("Hent delane hit", "Hent delene hit")
    val playCombine = Txt("Dra delane inntil kvarandre", "Dra delene inntil hverandre")
    val playPack = Txt("Ta frå kvarandre", "Ta fra hverandre")
    val playPartsSafe = Txt("Delane kjem tilbake i sekken", "Delene kommer tilbake i sekken")
    val playHandles = Txt("Hald eitt handtak kvar og dra saman", "Hold ett håndtak hver og dra sammen")
    val playHelper = Txt("Hjelp meg å dra", "Hjelp meg å dra")
    val playPullTogether = Txt("Vi dreg sjølve", "Vi drar selv")
    val playSit = Txt("Set deg oppi", "Sett deg oppi")
    val playHats = Txt("Hatten i vinden", "Hatten i vinden")
    val playCamp = Txt("Stjernenatt i hytta", "Stjernenatt i hytta")
    val playParade = Txt("Venneparaden", "Venneparaden")
    val playFindHat = Txt("Finn hatten på stranda og ta han med", "Finn hatten på stranden og ta den med")
    val playReturnHat = Txt("Ta hatten heim og set han på ein venn", "Ta hatten hjem og sett den på en venn")
    val playBuildCamp = Txt("Lag ei putehytte ved fossen", "Lag en putehytte ved fossen")
    val playSitCamp = Txt("La ein venn sitje i hytta", "La en venn sitte i hytta")
    val playLightCamp = Txt("Trykk på lykta ved hytta og vel Lyse", "Trykk på lykten ved hytta og velg Lyse")
    val playBuildCart = Txt("Lag ei trillevogn på garden", "Lag en trillevogn på gården")
    val playSeatCart = Txt("Set to venner oppi vogna", "Sett to venner oppi vognen")
    val playPullCart = Txt("Dra vogna av garde. Spelar du åleine, vel Hjelp meg å dra", "Dra vognen av gårde. Spiller du alene, velg Hjelp meg å dra")
    val playGo = Txt("Gå til neste steg", "Gå til neste steg")
    val playFinished = Txt("Du klarte det! Gåva ligg i sekken", "Du klarte det! Gaven ligger i sekken")
    val playOptional = Txt("Leik fritt, eller følg eit eventyr. Du kan alltid halde fram seinare.", "Lek fritt, eller følg et eventyr. Du kan alltid fortsette senere.")
    val playNearFriend = Txt("Legg tingen ved ein venn først", "Legg tingen ved en venn først")
    val friends = Txt("Venner", "Venner")
    val bringFriend = Txt("Hent ein venn", "Hent en venn")
    val friendsHere = Txt("Her no", "Her nå")
    val packPerson = Txt("Legg i sekken", "Legg i sekken")
    val packedFriendHint = Txt("Venner i sekken kan hentast hit igjen. Vel Spelarar om dei skal følgje med vidare.", "Venner i sekken kan hentes hit igjen. Velg Spillere om de skal følge med videre.")
    val driveLeft = Txt("Køyr til venstre", "Kjør til venstre")
    val driveRight = Txt("Køyr til høgre", "Kjør til høyre")
    val stopDriving = Txt("Stopp", "Stopp")
    val rise = Txt("Opp", "Opp")
    val dive = Txt("Dykk", "Dykk")
    val mapDrag = Txt("Dra kartet sidelengs", "Dra kartet sidelengs")
    val mapLeft = txt("Sjå til venstre", "Se til venstre")
    val mapRight = txt("Sjå til høgre", "Se til høyre")
    val appName = txt("Trollfoss")

    fun place(id: PlaceId): Txt = when (id) {
        PlaceId.HOME -> txt("Familiehuset", "Familiehuset")
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
        PlaceId.MINE_YARD -> txt("Mitt hus")
        PlaceId.MINE_GROUND -> txt("Mitt hus")
        PlaceId.MINE_UPPER -> txt("Mitt hus, oppe", "Mitt hus, oppe")
        PlaceId.VAGSTADDALEN -> txt("Vagstaddalen", "Vagstaddalen")
        PlaceId.CLOUD_ISLAND -> txt("Skyøya", "Skyøya")
    }

    val map = txt("Kart")
    val workshop = txt("Figurverkstaden")
    val players = txt("Spelarar", "Spillere")
    val choosePlayers = txt("Kven vil de vere?", "Hvem vil dere være?")
    val chooseTeamHint = txt("Trykk på figurane de vil leike med.", "Trykk på figurene dere vil leke med.")
    val playersHint = txt("Vel éin, to eller fleire figurar. Dei følgjer med overalt.", "Velg én, to eller flere figurer. De følger med overalt.")
    val togetherHint = txt("Leik saman på same skjerm – de kan dra kvar dykkar figur samtidig.", "Lek sammen på samme skjerm – dere kan dra hver deres figur samtidig.")
    val chooseLater = txt("Vel seinare", "Velg senere")
    val editPlayer = txt("Endre utsjånad og klede", "Endre utseende og klær")
    val playTogether = txt("Vi er klare!", "Vi er klare!")
    val playerNumber = txt("Spelar", "Spiller")
    val recallPlayers = txt("Samle spelarane her", "Samle spillerne her")
    val furnitureDragHint = txt("Dra ut eller trykk.", "Dra ut eller trykk.")
    val designer = txt("Heimedesignar", "Hjemmedesigner")
    val tasks = txt("Oppdrag")
    val stickers = txt("Klistremerke", "Klistremerker")
    val book = txt("Oppdagingsboka", "Oppdagelsesboka")
    val night = txt("Natt")
    val day = txt("Dag")
    val weather = txt("Vêr", "Vær")
    val camera = txt("Ta bilete", "Ta bilde")
    val close = txt("Lukk")
    val emptyBagHint = txt("Tom sekk – dra ein ting til sekken", "Tom sekk – dra en ting til sekken")
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
    val seasonTitle = txt("Årstid")
    val more = txt("Meir", "Mer")
    val bagFull = txt("Sekken er full. Ta ut noko først.", "Sekken er full. Ta ut noe først.")
    val bagPrevious = txt("Førre ting i sekken", "Forrige ting i sekken")
    val bagNext = txt("Neste ting i sekken")
    val dropFurniture = txt("Slepp i rommet", "Slipp i rommet")
    val allFurniture = txt("Alle møblar", "Alle møbler")
    val furnitureSeats = txt("Senger og sitjeplassar", "Senger og sitteplasser")
    val furnitureTables = txt("Bord og benkar", "Bord og benker")
    val furnitureStorage = txt("Skap og kister")
    val furnitureKitchen = txt("Kjøken og bad", "Kjøkken og bad")
    val furniturePlay = txt("Leik og musikk", "Lek og musikk")
    val furnitureDecor = txt("Lys og pynt")
    val seasonAuto = txt("Automatisk")
    val winter = txt("Vinter")
    val spring = txt("Vår")
    val summer = txt("Sommar", "Sommer")
    val autumn = txt("Haust", "Høst")
    val festive = txt("Pynt til høgtider", "Pynt til høytider")
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
    val catEyeColor = txt("Augefarge", "Øyenfarge")
    val catHairSize = txt("Hårstorleik", "Hårstørrelse")
    val catHairLength = txt("Hårlengd", "Hårlengde")
    val chooseHairFirst = txt("Vel ein frisyre med hår først.", "Velg en frisyre med hår først.")
    val catEyeSize = txt("Augestorleik", "Øyestørrelse")
    val catEyeSpacing = txt("Avstand mellom auga", "Avstand mellom øynene")
    val catFace = txt("Ansiktsform")
    val catNose = txt("Nase", "Nese")
    val catMouth = txt("Smil")
    val catPattern = txt("Mønster")
    val catAccent = txt("Detaljfarge")
    val hairGroup = txt("Hår")
    val faceGroup = txt("Ansikt og kropp")
    val clothesGroup = txt("Klede", "Klær")
    val secretTunnel = txt("Hemmeleg tunnel til Storhuset", "Hemmelig tunnel til Storhuset")
    val catEars = txt("Øyre", "Ører")
    val catTop = txt("Overdel")
    val catTopColor = txt("Farge på overdel")
    val catBottom = txt("Underdel")
    val catBottomColor = txt("Farge på underdel")
    val catShoes = txt("Sko")
    val catExtra = txt("Ekstra")

    // Storhuset Storstova
    /** The six rooms of the ground floor, left to right (for the room name that floats in, and for accessibility). */
    fun groundRoom(index: Int): Txt = when (index) {
        0 -> txt("Hallen", "Hallen")
        1 -> txt("Stova", "Stuen")
        2 -> txt("Biblioteket", "Biblioteket")
        3 -> txt("Spisestova", "Spisestuen")
        4 -> txt("Kjøkenet", "Kjøkkenet")
        else -> txt("Vinterhagen", "Vinterhagen")
    }

    /** What the golden key of the ground floor is called when it is shown. */
    val groundKey = txt("Den gylne nøkkelen frå Storstova", "Den gylne nøkkelen fra Storstua")

    /** The things Storstova brings, for accessibility labels (the picture is what the child sees). */
    val groundUmbrella = txt("Paraply")
    val groundLetter = txt("Brev")
    val groundPin = txt("Kartnål")
    val groundJam = txt("Syltetøy")
    val groundTray = txt("Serveringsbrett")

    /** What Rolf, the robot butler, is called. */
    val rolf = txt("Rolf")
    // Storhuset Loftet
    /** The four rooms of the attic, for the home designer and for screen readers. */
    fun atticRoom(index: Int): Txt = when (index) {
        0 -> txt("Lageret")
        1 -> txt("Spøkelsekroken", "Spøkelseskroken")
        2 -> txt("Tårnet")
        else -> txt("Det hemmelege rommet", "Det hemmelige rommet")
    }

    /** What the things of the attic are called, for screen readers and the discovery book. */
    fun atticThing(name: String): Txt = when (name) {
        "sture" -> txt("Sture, det snille spøkelset", "Sture, det snille spøkelset")
        "trunk" -> txt("Drakt-kista", "Drakt-kista")
        "horse" -> txt("Gyngehesten")
        "gramophone" -> txt("Grammofonen")
        "telescope" -> txt("Teleskopet")
        "starmap" -> txt("Stjernekartet")
        "chest" -> txt("Skattekista")
        "globe" -> txt("Globusen")
        "tree" -> txt("Slektstreet")
        "key" -> txt("Den gylne nøkkelen", "Den gylne nøkkelen")
        else -> txt(name)
    }

    /** The shout when Sture is found in his hiding place. */
    val atticFound = txt("Fann deg!", "Fant deg!")
    // Storhuset Andre høgda: the rooms, in the order of `UpperFloor.rooms`. The floor itself speaks in pictures and sounds.
    fun upperRoom(index: Int): Txt = when (index) {
        0 -> txt("Repoet", "Repoet")
        1 -> txt("Barnerommet")
        2 -> txt("Leikerommet", "Lekerommet")
        3 -> txt("Badet")
        4 -> txt("Soverommet")
        else -> txt("Altanen")
    }

    /** What the child's own words for the toys of Andre høgda would be, for a screen reader. */
    fun upperToy(name: String): Txt = when (name) {
        "train" -> txt("Leiketog", "Leketog")
        "blocks" -> txt("Klossetårn", "Klossetårn")
        "dollhouse" -> txt("Dokkehuset", "Dukkehuset")
        "ballpit" -> txt("Ballbasseng", "Ballbasseng")
        "climbing" -> txt("Klatrevegg")
        "easel" -> txt("Staffeli")
        "karaoke" -> txt("Karaokescene", "Karaokescene")
        "puppets" -> txt("Dokketeater", "Dukketeater")
        "wardrobe" -> txt("Garderobe")
        "jewelbox" -> txt("Smykkeskrin", "Smykkeskrin")
        "feeder" -> txt("Fuglemating")
        else -> txt(name)
    }
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
    // Storhuset Kjellaren: the child sees no words here, but the grown-up pages and screen readers know the rooms and the tunes.
    val cellarRooms = listOf(
        txt("Verkstaden", "Verkstedet"),
        txt("Vaskerommet"),
        txt("Fyrrommet"),
        txt("Bassenget og badstua", "Bassenget og badstuen"),
        txt("Festrommet"),
    )
    val cellarTunes = listOf(
        txt("Polka"),
        txt("Diskofunk"),
        txt("Vals"),
        txt("Spelledåse", "Spilledåse"),
        txt("Marsj"),
    )
    val cellarTunnel = txt("Gruvetunnelen til Trollhola", "Gruvetunnelen til Trollhula")
    val cellarSockMonster = txt("Sokkemonsteret")
    val cellarKeyHint = txt("Sokkemonsteret har ei gylden nøkkel i magen.", "Sokkemonsteret har en gylden nøkkel i magen.")
}

/** Creative play, relationships and every label on the new picture controls. */
object SC {
    val back=Txt("Tilbake","Tilbake")
    val rain=Txt("Regn","Regn")
    val together=Txt("Venner og fantasi","Venner og fantasi")
    val choose=Txt("Vel ein venn","Velg en venn")
    val needTwo=Txt("Set to venner nær kvarandre på golvet.","Sett to venner nær hverandre på gulvet.")
    val personalities=Txt("Slik likar eg å leike","Slik liker jeg å leke")
    val pets=Txt("Dyrevenn","Dyrevenn")
    val petHome=Txt("Send dyret heim","Send dyret hjem")
    val petHint=Txt("Vel same dyr som skal bli med. Dra det når du vil.","Velg samme dyr som skal bli med. Dra det når du vil.")
    val band=Txt("Band","Band")
    val bandHint=Txt("Gi gitar, tromme og mikrofon til tre venner. Vel dei og start!","Gi gitar, tromme og mikrofon til tre venner. Velg dem og start!")
    val instruments=Txt("Hent instrument","Hent instrumenter")
    val start=Txt("Start","Start")
    val stop=Txt("Stopp","Stopp")
    val party=Txt("Lag fest","Lag fest")
    val guests=Txt("Vel gjester","Velg gjester")
    val music=Txt("Musikk","Musikk")
    val lights=Txt("Lys","Lys")
    val food=Txt("Hent festmat","Hent festmat")
    val endParty=Txt("Avslutt festen","Avslutt festen")
    val noGuests=Txt("Vel minst ein gjest. Gi bandvennene instrument først.","Velg minst en gjest. Gi bandvennene instrumenter først.")
    val sky=Txt("Sengballongen til Skyøya","Sengeballongen til Skyøya")
    val skyHint=Txt("Vel stopp ved Skyøya. Venner, dyr og bagasje blir med. Piknik og kikkert ventar!","Velg stopp ved Skyøya. Venner, dyr og bagasje blir med. Piknik og kikkert venter!")
    val returnTo=Txt("Tilbake til start","Tilbake til start")
    val art=Txt("Lag kunst","Lag kunst")
    val artHint=Txt("Trykk eller dra på arket. Vel farge og form.","Trykk eller dra på arket. Velg farge og form.")
    val hang=Txt("Heng på veggen","Heng på veggen")
    val wallpaper=Txt("Bruk som tapet","Bruk som tapet")
    val artUndo=Txt("Ta vekk siste stempel","Ta bort siste stempel")
    val shape=Txt("Form","Form")
    val door=Txt("Vel det hemmelege rommet","Velg det hemmelige rommet")
    val buildRoom=Txt("Bygg eit rom i Mitt hus først.","Bygg et rom i Mitt hus først.")
    val enter=Txt("Opne den hemmelege døra","Åpne den hemmelige døra")
    val secret=Txt("Prøv den løynde eigenskapen","Prøv den skjulte egenskapen")
    val secretThings=Txt("Ting med hemmelegheiter","Ting med hemmeligheter")
    val secretThingsHint=Txt("Hent magnet, paraply og skei. Trykk på dei og prøv stjerneknappen!","Hent magnet, paraply og skje. Trykk på dem og prøv stjerneknappen!")
    val awaken=Txt("Tryll bamsen levande – eller tilbake","Tryll bamsen levende – eller tilbake")
    val weather=Txt("Vêrleik","Værlek")
    val wind=Txt("Vind","Vind")
    val snow=Txt("Form snø","Form snø")
    val waterHint=Txt("Fyll bøtta ved pumpa. Sett rennene tett, med høgre ende nedover.","Fyll bøtta ved pumpa. Sett rennene tett, med høyre ende nedover.")
    val obstacle=Txt("Bygg hinderbane","Bygg hinderbane")
    val rescue=Txt("Hjelp over bekken","Hjelp over bekken")
    val rescueHint=Txt("Vel bru, båt eller ein venn. Flytt til høgre side for å hjelpe blomen.","Velg bro, båt eller en venn. Flytt til høyre side for å hjelpe blomsten.")
    val bridge=Txt("Hent planke til bru","Hent planke til bro")
    val boat=Txt("Hent båt","Hent båt")
    val saved=Txt("Dette blir verande i verda di.","Dette blir værende i verden din.")
    fun trait(t:app.trollfoss.domain.Temperament)=when(t) {
        app.trollfoss.domain.Temperament.PLAYFUL -> Txt("Leiken","Leken")
        app.trollfoss.domain.Temperament.CALM -> Txt("Roleg","Rolig")
        app.trollfoss.domain.Temperament.CURIOUS -> Txt("Nysgjerrig","Nysgjerrig")
    }
    fun friend(a:app.trollfoss.domain.FriendAction)=when(a) {
        app.trollfoss.domain.FriendAction.HUG -> Txt("Klem","Klem")
        app.trollfoss.domain.FriendAction.HIGH_FIVE -> Txt("High five","High five")
        app.trollfoss.domain.FriendAction.HOLD_HANDS -> Txt("Halde hender","Holde hender")
    }
    fun toyName(t:FixtureType):Txt=when(t) {
        FixtureType.PLAY_CABLE_CAR -> Txt("Taubane","Taubane")
        FixtureType.PLAY_ECHO_BOX -> Txt("Ekkoboks","Ekkoboks")
        FixtureType.PLAY_DANCE_FLOOR -> Txt("Danseteppe","Dansematte")
        FixtureType.PLAY_CONFETTI -> Txt("Konfettimaskin","Konfettimaskin")
        FixtureType.PLAY_LIGHT_RIG -> Txt("Lysrigg med discokule","Lysrigg med discokule")
        FixtureType.PLAY_ROBOT_WORKSHOP -> Txt("Robotverkstad","Robotverksted")
        FixtureType.PLAY_HELPER_ROBOT -> Txt("Hjelperobot","Hjelperobot")
        FixtureType.PLAY_ROCKET_KIT -> Txt("Rakettsett","Rakettsett")
        FixtureType.PLAY_REACTION_COURSE -> Txt("Reaksjonsbane","Reaksjonsbane")
        FixtureType.PLAY_DIVING_BELL -> Txt("Dykkarklokke","Dykkerklokke")
        FixtureType.PLAY_DIGGER -> Txt("Gravemaskin","Gravemaskin")
        FixtureType.PLAY_TREASURE_TABLE -> Txt("Skattebord","Skattebord")
        FixtureType.PLAY_SEESAW -> Txt("Vennevippe","Vennevippe")
        FixtureType.PLAY_PUPPETS -> Txt("Dokketeater","Dukketeater")
        FixtureType.PLAY_TANDEM -> Txt("Tandemsykkel","Tandemsykkel")
        FixtureType.PLAY_PICNIC -> Txt("Piknikkorg","Piknikkurv")
        FixtureType.PLAY_CRANE -> Txt("Leikekran","Lekekran")
        FixtureType.PLAY_CONVEYOR -> Txt("Transportband","Transportbånd")
        FixtureType.PLAY_BUILD -> Txt("Byggjebord","Byggebord")
        FixtureType.PLAY_CHANNEL -> Txt("Vassrenne","Vannrenne")
        FixtureType.PLAY_MIRROR -> Txt("Tryllespegel","Tryllespeil")
        FixtureType.PLAY_HOVER -> Txt("Svevepute","Svevepute")
        FixtureType.PLAY_CLOUD -> Txt("Vêrglas","Værglass")
        FixtureType.PLAY_PORTAL -> Txt("Portalring","Portalring")
        FixtureType.PLAY_TREE -> Txt("Mitt epletre","Mitt epletre")
        FixtureType.PLAY_REPAIR -> Txt("Lys å reparere","Lys å reparere")
        FixtureType.PLAY_DOOR -> Txt("Hemmeleg bokhylledør","Hemmelig bokhylledør")
        FixtureType.PLAY_TUNNEL -> Txt("Leiketunnel","Leketunnel")
        FixtureType.PLAY_JUMP -> Txt("Mjukt hopp","Mykt hopp")
        FixtureType.PLAY_WATER_WHEEL -> Txt("Vasshjul","Vannhjul")
        FixtureType.PLAY_ART -> Txt("Kunststaffeli","Kunststaffeli")
        FixtureType.PLAY_RESCUE -> Txt("Blomen over bekken","Blomsten over bekken")
        else -> Txt("Leike","Leke")
    }
    fun toyUse(t:FixtureType):Txt=when(t) {
        FixtureType.PLAY_ECHO_BOX -> Txt("Spel noko i nærleiken, og trykk Bruk. Boksen syng det att – med pipestemme!","Spill noe i nærheten, og trykk Bruk. Boksen synger det tilbake – med pipestemme!")
        FixtureType.PLAY_DANCE_FLOOR -> Txt("Trykk Bruk for musikk, og set vennene på teppet. To saman hoppar i takt!","Trykk Bruk for musikk, og sett vennene på matta. To sammen hopper i takt!")
        FixtureType.PLAY_CONFETTI -> Txt("Trykk Bruk: pang! Konfetti over heile rommet, og vennene hoppar.","Trykk Bruk: pang! Konfetti over hele rommet, og vennene hopper.")
        FixtureType.PLAY_LIGHT_RIG -> Txt("Trykk Bruk for scenelys, ein gong til for disco, og ein gong til for å slå av.","Trykk Bruk for scenelys, en gang til for disco, og en gang til for å slå av.")
        FixtureType.PLAY_ROBOT_WORKSHOP -> Txt("Legg to ting på bordet og trykk Bruk. Knatring og gneistar – ein robotkompis rullar ut!","Legg to ting på bordet og trykk Bruk. Knatring og gnister – en robotkompis ruller ut!")
        FixtureType.PLAY_HELPER_ROBOT -> Txt("Trykk Bruk, så hentar roboten noko som ligg på golvet og gir det til ein venn. Du kan òg leggje noko på brettet hans.","Trykk Bruk, så henter roboten noe som ligger på gulvet og gir det til en venn. Du kan også legge noe på brettet hans.")
        FixtureType.PLAY_ROCKET_KIT -> Txt("Set ein venn i raketten og trykk Bruk. 3 – 2 – 1 – opp i lufta!","Sett en venn i raketten og trykk Bruk. 3 – 2 – 1 – opp i lufta!")
        FixtureType.PLAY_REACTION_COURSE -> Txt("Trykk på brettet for å starte. Trykk fort på feltet som lyser! Fem rette på rad gir fest.","Trykk på brettet for å starte. Trykk raskt på feltet som lyser! Fem riktige på rad gir fest.")
        FixtureType.PLAY_CABLE_CAR -> Txt("Set ein venn i gondolen og trykk Bruk. Oooh, høgt!","Sett en venn i gondolen og trykk Bruk. Oooh, høyt!")
        FixtureType.PLAY_DIVING_BELL -> Txt("Set klokka ved vatnet og ein venn inni. Dykk ned og hels på fisken!","Sett klokka ved vannet og en venn inni. Dykk ned og hils på fisken!")
        FixtureType.PLAY_DIGGER -> Txt("Køyr ut og trykk Grav. Kanskje finn du skatt – eller ein gammal støvel.","Kjør ut og trykk Grav. Kanskje finner du skatt – eller en gammel støvel.")
        FixtureType.PLAY_TREASURE_TABLE -> Txt("Legg tre ting på bordet og trykk Bruk. Kva dukkar opp?","Legg tre ting på bordet og trykk Bruk. Hva dukker opp?")
        FixtureType.PLAY_SEESAW -> Txt("Set ein venn i kvar ende. Trykk Bruk for å vippe.","Sett en venn i hver ende. Trykk Bruk for å vippe.")
        FixtureType.PLAY_PUPPETS -> Txt("Set vennene i teatret. Start dokkene – publikum ler!","Sett vennene i teatret. Start dukkene – publikum ler!")
        FixtureType.PLAY_TANDEM -> Txt("To venner kan sykle med pilene. Trykk på sykkelen for ringjeklokke.","To venner kan sykle med pilene. Trykk på sykkelen for ringeklokke.")
        FixtureType.PLAY_PICNIC -> Txt("Trykk for å opne korga. Set oppi venner og legg mat på duken.","Trykk for å åpne kurven. Sett oppi venner og legg mat på duken.")
        FixtureType.PLAY_CRANE -> Txt("Dra ei ting til kroken. Bruk løftar, flyttar og set ned.","Dra en ting til kroken. Bruk løfter, flytter og setter ned.")
        FixtureType.PLAY_CONVEYOR -> Txt("Dra ei ting til bandet. Bruk startar, stoppar og vender.","Dra en ting til båndet. Bruk starter, stopper og snur.")
        FixtureType.PLAY_BUILD -> Txt("Dra tre klossar til bordet. Bruk gjer dei til ei køyrbar vogn. Delane kan hentast att.","Dra tre klosser til bordet. Bruk gjør dem til en kjørbar vogn. Delene kan hentes tilbake.")
        FixtureType.PLAY_CHANNEL -> waterHint
        FixtureType.PLAY_MIRROR -> Txt("Set ein venn framfor spegelen. Bruk speglar figuren og skiftar uttrykk ei lita stund.","Sett en venn foran speilet. Bruk speiler figuren og endrer uttrykk en liten stund.")
        FixtureType.PLAY_HOVER -> Txt("Dra ei ting til puta. Bruk løftar henne – og senkar igjen.","Dra en ting til puta. Bruk løfter den – og senker igjen.")
        FixtureType.PLAY_CLOUD -> Txt("Dra ei bøtte med vatn til glaset. Skyregnet vatnar tre i nærleiken.","Dra en bøtte med vann til glasset. Skyregnet vanner trær i nærheten.")
        FixtureType.PLAY_PORTAL -> Txt("Set ut to ringar med same farge. Dra ei ting inn eller set ein venn ved ringen og trykk Bruk.","Sett ut to ringer med samme farge. Dra en ting inn eller sett en venn ved ringen og trykk Bruk.")
        FixtureType.PLAY_TREE -> Txt("Vatn treet tre gonger. Det veks og blir ståande. Trykk på det store treet for eple.","Vann treet tre ganger. Det vokser og blir stående. Trykk på det store treet for eple.")
        FixtureType.PLAY_REPAIR -> Txt("Dra skrutrekkaren til lyset. No kan det slåast av og på.","Dra skrutrekkeren til lyset. Nå kan det slås av og på.")
        FixtureType.PLAY_DOOR -> Txt("Kople bokhylla til eit rom du har bygd. Ei tydeleg knapp tek deg tilbake.","Koble bokhylla til et rom du har bygd. En tydelig knapp tar deg tilbake.")
        FixtureType.PLAY_TUNNEL,FixtureType.PLAY_JUMP -> Txt("Flytt delane som du vil. Dra ein venn til tunnelen eller hoppet og sjå kva som skjer.","Flytt delene som du vil. Dra en venn til tunnelen eller hoppet og se hva som skjer.")
        FixtureType.PLAY_WATER_WHEEL -> Txt("Vatn frå bøtta eller rennene driv hjulet.","Vann fra bøtta eller rennene driver hjulet.")
        FixtureType.PLAY_ART -> artHint
        FixtureType.PLAY_RESCUE -> rescueHint
        else -> Txt("Prøv leika!","Prøv leken!")
    }
}
