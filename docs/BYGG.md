# Mitt hus – bygg ditt eige hus (byggjemodus)

Som i Toca World startar ein med å **bygge eit hus**. I Trollfoss er det «Mitt hus»: ei tom tomt på kartet der
barnet legg grunnmur, byggjer rom for rom, set på ein etasje til, kler huset ute, og møblerer det. Huset blir
barnet sitt, og heile bygda står rundt. Dette er verdsbygginga: eit hus først, seinare meir.

Les `docs/AI_INSTRUCTIONS.md`, `docs/ART_GUIDE.md`, `docs/DESIGN.md` og **`docs/HUSET.md`** (reglane for yting,
stil, ankerpunkt, namneprefiks, arbeidsmåte og bygging med lås gjeld òg her). Storhuset (`MANOR_*`) er eit ferdig
stort hus å utforske; Mitt hus (`MINE_*`) er det barnet byggjer sjølv.

## 1. Opplevinga

1. **Tomta** (`MINE_YARD`, knapp på kartet, nytt «Mitt hus»). Fyrste gong: ei grøn tomt med eit skilt, eit lite
   byggjeskur med reiskapar, ein haug planker og ein hammar. Ein pil og ei boble peikar på hammar-knappen.
2. **Grunnmur.** Hammar-knappen opnar byggjepanelet. Fyrste val: **malen** (fire typar: hytte, villa, tårnhus,
   gamalt bondehus), kvar med eigne fargar, tak og form. Når barnet vel, spelar ein byggjesekvens: reiskapar
   flyg, hamring, støv, planker som reiser seg, og huset spretter opp med konfetti. Entré (slot 0) finst no.
3. **Rom for rom.** Inne (`MINE_GROUND`, fem slot à to einingar; slot 0 er entréen med trappa) viser tomme slot ein
   «bygg her»-ramme med eit +. Trykk, vel romtype (ti typar), og sjå rommet bli bygd: stillas, hamring, sag,
   støvskyer, og rommet fyller seg med møblar og eit tapet og golv som passar. Barnet kan alltid byte
   tapet/golv (`Decor`) og flytte, lagre eller byte møblar med heimedesignaren som før.
4. **Andre etasje.** Når to rom står, kjem knappen «Bygg ein etasje til» (kran som løftar ein etasje).
   Då opnar trappa, og `MINE_UPPER` får fem nye slot. Eit rom oppe krev eit bygd rom under.
5. **Utsjånad.** Eit eige panel: veggfarge (12), taktype (4) og takfarge (8), dør (6), vindauge (4), pipe og
   flagg av/på. Tomta viser huset slik barnet har bygd det (ei fasade i skrå-3D). Same teikning står på kartet.
6. **Riving.** Lang trykk på eit bygd rom → «Riv»: ei bekreftingsdialog (`TrollDialog`), og alle møblar blir lagde i
   lageret til designaren (ingenting går tapt). Entréen kan ikkje rivast.
7. **Hage.** Tomta er ein vanleg stad: barnet kan sette ut gjerde, blomar, postkasse, huske og andre ting frå
   utekatalogen (`Decor.catalogue`; legg til nokre tomteting). Ein figur kan bu der, og figurane går inn og ut av
   døra av seg sjølve (`Floor.hangouts`).
8. **Innflyttingsfest.** Etter tre rom står ein knapp «Innflyttingsfest» som kallar på nokre figurar frå bygda
   (flyttar dei midlertidig inn og tilbake) med musikk og konfetti. Valfri stor overraskingsbonus.

**Ny verd, nytt spel:** på ei heilt ny lagring startar spelet på tomta med byggjepanelet synleg (utan å tvinge noko:
kartknappen er rett der, og heile bygda finst). Eksisterande lagringar er uendra, og tomta dukkar opp på kartet.

## 2. Det som alt finst (grunnmuren)

Lokalt tillegg 2026-10-01: nettbrett har romoversikt med romsymbol, og sidepanel reserverer synleg
arbeidsplass utan å forstørre tinga. Kjøkenbenken blandar egg og mjølk til deig (mjølet står i bollen),
plantekassa tek frø og vatning og gir bær eller blomar, og sagbenken gjer ei planke til to pinnar.
Resultata er vanlege flyttbare ting: bær og deig kan bakast i omnen, og pinnar kan bli gitar på
arbeidsbenken. Trykk på benk/kasse hentar fram råvarer eller reiskapar også i gamle lagringar.
Dei nye aktivitetane verkar også når møblane blir flytta gjennom lageret til andre hus.
Oppskriftsbilete viser moglege kombinasjonar; barnet vel sjølv kva det vil gjere.

- `domain/Mine.kt`: `RoomKind` (ti typar), `MineHouse` (malen, utsjånaden og slot-liste for kvar etasje), `Mine`
  (konstantar, `canBuild`, `buildUpper`, `sync`), og flatene `MineYardFloor`, `MineGroundFloor`, `MineUpperFloor`
  (blåkopi med dør og trapp, vegar, ankomstpunkt). Staten ligg i `World.mine` og blir lagra av `WorldStore` ("mine").
- `PlaceId.MINE_YARD/MINE_GROUND/MINE_UPPER` (`mine`, `big`, `onMap`; fixtur-id-ar som Storhuset: eigen 300-blokk).
- Vegar: `mine-yard-door` ↔ `mine-front-door`, `mine-stairs-up` (låst av flagget `mine_upper`) ↔ `mine-stairs-down`.
- Rom i designaren = slot (`Decor.rooms` følgjer `Floor.rooms`), så tapet/golv er per slot.
- Stand-in-teikning i `ui/art/MineArt.kt` (bakgrunn, fixtur, ting), ruta frå `PlaceArt`, `FixtureArt` og `HouseArt`.
- Eit kartspot (`mapSpot(MINE_YARD)` er ein plassholdar) og strengar `S.place`. Musikk: `PARK` ute, `HOME` inne.

## 3. Det du byggjer (alt av dette)

**Domene** (`domain/MineBuilder.kt` og `domain/MineRooms.kt`, fleire filer ved behov):
- Reglane: `Mine.canBuild`, byggje/rive rom (med lager av møblar), byggje etasje, valde utsjånad, tasting og
  hendingar. Bruk `FloorRules` (`rules(...)` i `MineGroundFloor` osv., sjå `House.kt`) og `Sim.tap`/`Fx.HOUSE`
  (eigen kodeblokk `HouseFx.MINE = 700`, legg han til i `House.kt` ved sidan av dei andre) for hamring og lyd.
- **Rom-førehandsval** (`RoomKind` → standardtapet/golv + 6–9 møblar kvar, plassert relativt til sloten, ofte
  eksisterande `FixtureType` (SOFA, TV, BED, BUNK_BED, BATH, TOILET, SINK, STOVE, FRIDGE, TABLE, CHAIR, PIANO, BOOKCASE,
  WORKBENCH, AQUARIUM, PLANT_BIG, LAMP, RUG, PICTURE …) og nokre eigne (prefiks `MI_`). Møblane blir lagde til som
  vanleg tilleggsmøbel (`Designer.add`), så barnet kan flytte, lagre og byte dei. Kvar romtype skal ha ein
  overraskande liten detalj og eit morosamt trykk-svar. Ti typar: stove, kjøken, spisestove, soverom, barnerom, bad,
  bibliotek, verkstad, musikkrom, drivhus (glas, planter).
- **Malar** (4) og utsjånadsval (tal i `MineHouse`; endre eller utvid etter behov og oppdater `WorldStore` saman
  med ein test, med standardverdiar for gamle lagringar).
- Tomteting (nytt til `Decor.catalogue(MINE_YARD)` om nødvendig), og at figurar bur og går inn/ut.
- Innflyttingsfesten (valfri bonus): `House.moveTo`-mønsteret flyttar nokre kjende figurar inn og tilbake etterpå.
- 3 glimt i tomta (stubbar `mine_start`, `mine_second_floor`, `mine_housewarming` i `Secrets.kt`: sett riktig posisjon,
  opne med `sim.unlock(id)` ved handlinga), 4–5 oppdrag (`Deed` med prefiks `MI_` og `Task`-ar: bygg første rom, bygg
  tre rom, bygg etasje, møbler eit rom, kle huset), musikk (reglar i `Music.kt` om du vil ha eigne).

**UI** (`ui/screens/BuildPanel.kt` m.fl.):
- Ein byggjeknapp (hammar-ikon) som er synleg i dei tre `MINE_*`-stadene. På mobil skal han liggje i menyen
  bak «Meir»-knappen (sjå `PlayScreen.kt`; kontrollane er små på telefon: halde det slik, ikkje legg til faste
  knappar). Byggjepanelet skal vere kompakt og ikkje dekkje scena: slide inn frå høgre som `DesignerPanel`.
- Mal-val, rom-val (kort med små førehandsvisingar teikna i kode, `ui/screens/Thumbs.kt`-mønsteret), «bygg etasje»,
  utsjånad, riv-dialog. Pen animasjon (sprett, støv). Alt tekst som `Txt(nn, nb)`; nesten ingen tekst, bruk bilete.
- `Engine`: byggjemodus (liknar `designMode`): ramme med + på tomme slot, berøring på ei ramme opnar romvalet;
  konstruksjonsanimasjon (stillas, støv, hammar, kran) lagt i ei eiga fil, med den minste moglege endringa i
  `Engine.kt` (ein hook). Lyd: hamring, saging, bor, `pling` (nye `Sfx` med prefiks `MI_` i ankerblokk).

**Teikning** (`ui/art/MineArt.kt`, `MineHouseArt.kt`, `MineRoomArt.kt`, …):
- **Fasaden** er helten: `drawMineHouse(...)` teiknar huset ut frå `MineHouse` (mal, farger, tak, dør, vindauge,
  pipe med røyk, flagg, hovudetasje og eventuell etasje oppe, kvar bygd slot som sin eigen modul med eit eige
  vindauge/detalj etter romtype, tårn på tårnhus osv.). Same funksjon brukast i tomta (stor, bakgrunn), i
  byggjepanelet (liten førehandsvising) og på kartet (sjå under). Heile huset i skrå-3D med blekkstrek og
  to-tone skugge, parallakse-tomt: hage, stig, port, tre, skur.
- **Tomta** (`MINE_YARD`): bakgrunn med himmel, vêr, natt, sesong (les `pen.season`), byggjeskur og planker
  så lenge ingenting er bygd; fasaden med døra; hage framfor.
- **Inne** (`MINE_GROUND`, `MINE_UPPER`): for kvar slot: bygd rom (veggar, golv, tak, vindauge, alt i `Decor`-stilen
  når barnet har valt tapet/golv, elles romtypen sin eigen stil) eller **tom slot**: eit ope «utsyn» (himmel og gras
  utanfor, stillasstolpar på golvet, ein stiplet ramme når byggjemodus er på). Berre synleg område teiknast, med
  bufra geometri (yting!). Skillevegger mellom slot (døropningar).
- Teikn alle nye møbeltypar (prefiks `MI_`) og dei delte vegtypane som er i stubbane (`DOOR`, `STAIRCASE`).

**Kartet:** vis det barnet har bygd på kartet (fasade i liten storleik, utan tekst) som eit kart-landemerke på
`mapSpot(MINE_YARD)`. Kartet har bitmap-lag (`MapLayer*`, sjå `ui/art/Map*.kt`): berre rørsler (røyk) i live-laget,
resten i still-laget med ein **laggnøkkel som inneheld ein hash av `MineHouse`** så laget bygger seg om berre når
huset endrar seg. Eit anna lag av hjelparar (`house/kart`) endrar kartet samtidig; la dei gjere ferdig fyrst og
**flett main inn i branchen din** før du rører `Map*.kt`. Finn ein god plass på kartet (flytt gjerne spot).

## 4. Kvalitet og avgrensing

Alle yting- og stilkrav frå `HUSET.md` §5 gjeld. Alt barnet ser skal vere tryggjande, enkelt og morosamt:
byggjesekvensane er gøy (støv, kaos som blir ryddig), aldri skummelt. Ingen tid, poeng eller tap: ein kan alltid
bygge om. Ingenting må kunne gå tapt (møblar til lager ved riving). Små barn (4–6) skal klare dei fyrste trinna
utan lesing: store ikon, ei tydeleg boble og pil, eitt val om gongen.

## 5. Arbeidsmåte

Eigen worktree: `git -C C:\topa worktree add C:\trollfoss-wt\bygg -b house/bygg main`, debug-suffiks `.bygg`,
bygg berre med `C:\topa\scripts\Build-Locked.ps1`, emulator berre via `Run-Locked.ps1 -Name emulator` og
`Screenshot.ps1` (sjå `HUSET.md` §8). Debug-ekstra for å sjå: `--es place mine_yard|mine_ground|mine_upper`.
Legg til debug-ekstra for å lage eit ferdig hus (`--es mine demo`) så du kan ta bilete av fleire rom raskt (berre
debug-bygg, i `TrollfossViewModel.debug`).

Skriv testar (`domain/MineTest.kt`: bygging, rekkefølgje, riving med lager, etasje og trapp, sparing av alt, gamle
lagringar utan `mine`, glimt og oppdrag). Eksisterande testar, `HouseTest` og lint må halde seg grøne.

**Rapport til slutt:** kva som er laga og kva som manglar, avvik, nye `Sfx`/`FixtureType`/`Deed`-id-ar, delte filer du
endra (og kvifor), skjermbilete (tomt tomt, kvar mal, alle ti romtypar, etasje, utsjånadspanel, kart), yting
(`TrollfossPerf`), og siste commit på `house/bygg`.

## 6. Slik er det bygd (status)

**Domene:** `Mine.kt` (tilstand, malar, reglar for kor ein kan byggje, vegar, `MineYardFloor`/`MineGroundFloor`/`MineUpperFloor`),
`MineBuilder.kt` (`sim.mine`: fundament, rom, etasje, riving med lager, utsjånad, innflyttingsfest, bebuarar som går ut og inn,
trykk på møblar, byggjejobbar med tid), `MineRooms.kt` (ti rom-førehandsval, 23 `MI_`-møblar med mål, tomteting i katalogen),
`MineDemo.kt` (debug). Alle hendingar går som `Fx.HOUSE` i blokka `HouseFx.MINE` (`MineEvent`).
Eit byggjearbeid går i sekund (`MineJob`): huset endrar seg ved `commitAt`, møblane kjem ein og ein, og `finishJob()` fullfører alt med
ein gong (når barnet går ut eller appen går i bakgrunnen), så ingenting går tapt.

**UI:** `ui/screens/BuildPanel.kt` (panelet, bobla, rivedialogen), `MineThumbs.kt` (små bilete og ikon), `MineUi.kt`, `ui/StringsMine.kt`.
Hammarknappen står ved sida av heimedesignaren på nettbrett og i «Meir»-menyen på telefon.

**Teikning:** `MineKit.kt` (palettar, dører, vindauge, stillas, verktøy, kran), `MineHouseArt.kt` (fasaden), `MineYardArt.kt` (tomta),
`MineRoomArt.kt` (romma), `MineFixtureArt.kt` (møblane), `MapMine.kt` (kartlandemerket). `MineView.house` er huset teikninga les.

**Debug-ekstra:** `--es mine demo|demo2` (ferdig hus, to sett med romtypar), `--ei shape 0..3`, `--es build on|off`, `--ef cam <x>`
saman med `--es place mine_yard|mine_ground|mine_upper`.
