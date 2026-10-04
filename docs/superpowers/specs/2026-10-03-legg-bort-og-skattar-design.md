# Legg bort og skattar: design

Dato: 2026-10-03. Grunnlag: publisert 1.7.1 (`2197745`). Grein: `claude/legg-bort`.

## Bakgrunn

Dette er første runde av «Destiller og test». Ho byggjer på det brukaren har sett når ein seksåring
speler på nettbrett:

1. Han er mest oppteken av å leggje ting på lager. Han kastar dei ut i sida, men det verkar berre når
   sidepanelet er ope. Panelet blir ståande i vegen når han gløymer å lukke det.
2. Han vil fange alle diamantane og leitar etter noko å ha diamantar og fine ting oppi.
3. Han kjem seg ikkje opp ein etasje. Han trykkjer på døropninga øvst i trappa, ikkje på trappa.
4. Han vil fjerne folk frå ein stad, men får det ikkje til.

Brukaren bad i tillegg om litt meir visuelle hint, utan at det blir for mykje, og om at like møblar
i lageret blir samla i eitt kort med eit tal.

Årsakene i 1.7.1-kjelda:

- Berre møblar kan lagrast, og berre over det opne panelet (`Engine.storeZone` er panelet sitt område
  og er `null` når panelet er lukka). Ting og folk går berre i sekken via ein sirkel på
  `bagRadius * 1.4` rundt sekkeknappen.
- Skjermkantane (ytste 8 % på kvar side) flyttar kameraet når barnet held noko. Kanten kan derfor
  ikkje vere mål for å leggje bort.
- `HouseAtticRules.trim` fjernar den eldste lause diamanten når det er fleire enn seks på loftet, og
  den eldste mynten over atten. `capPlace`/`cap`/`limit` fjernar den eldste lause tingen når ein stad
  har over 70 ting.
- Døropninga i `grStairs` er teikna over treffboksen til `STAIRCASE`.
- `FriendsPanel` kan ikkje opnast frå `PlayScreen`; `friendsOpen` blir aldri sett til `true` der.

## Mål

Barnet skal kunne leggje bort ting, møblar og folk med éin gest, samle skattar ein trygg stad, og
kome seg mellom etasjane ved å trykkje der det ser ut som ein kan gå. Ingen ny tekst barnet må lese.

## Ikkje med

Samanslåing av sekk og lager. Kiste som reiser med innhaldet. Endringar i knappar, menyar, symbol,
nivå eller oppdrag. `Venner`-panelet i rommet. Desse høyrer til runde to, etter at brukaren har sett
barnet bruke denne runden.

## 1. Legg bort-hjørnet

Hjørnet nede til høgre, der sekken alt står, blir mål for alt barnet vil leggje bort.

- **Når:** hjørnet er aktivt medan minst éin finger held ein ting, ein figur eller eit møbel som er
  løfta frå scena. Draging ut frå møbelkatalogen eller lageret aktiverer det ikkje.
- **Storleik:** sekken blir teikna 1,6 gonger større. Treffområdet er ein sirkel med radius
  `bagRadius * 2,4` rundt `bagCenter`.
- **Bilete:** held barnet ein ting eller ein figur, viser hjørnet ein open sekk. Held barnet berre
  møblar, viser det ei lagerkasse. Møbelknappen ved sida av sekken er skjult medan hjørnet er aktivt.
- **Slepp i hjørnet:** ting og figurar går i sekken, slik `intoBag` gjer i dag. Møblar går på lager
  med `Designer.store`. Eit møbel som ikkje kan lagrast, blir sett ned att og hjørnet vippar éin gong.
- **Kamera:** ein finger inne i treffområdet flyttar ikkje kameraet. Kantstripene elles er uendra.
- **Markering:** treffområdet lyser når ein halden ting, figur eller eit møbel er over det.
- **Panelet:** er sidepanelet ope, fungerer slepp over panelet som før.
- **Angre:** begge handlingane går gjennom den eksisterande angreloggen.
- **Rørsle:** hjørnet veks på om lag 180 ms med éin liten vipp. Når Android har slått av animasjonar,
  byter det storleik utan vipp.

## 2. Hint i augeblinken

Eit hint kjem berre når barnet prøver noko, og forsvinn att. Ingen faste piler og ingen tekst.

1. **Løft:** hjørnet vaknar (del 1).
2. **Bomtrykk:** treffer eit trykk i scena ingenting, vippar og lyser det næraste møbelet som svarar
   på trykk, dersom det ligg innan 0,35 scene-einingar frå trykket. Møblar som svarar på trykk er
   brukbare gjennomgangar (trapp, dør, heis, luke, stige, hemmeleg dør), møblar med `container`,
   møblar med `machine` og køyretøy. Hintet varer 0,6 s og kjem høgst éin gong per 1,5 s.
3. **Nesten-slepp:** blir eit møbel sleppt utanfor hjørnet, men i nedre høgre fjerdedel av skjermen,
   vippar lagerkassa éin gong.

## 3. Skattekista

Ein ny møbeltype, `TREASURE_BOX` («Skattekiste» på begge målformer). Ho er noko anna enn
`AT_TREASURE_CHEST` på loftet, som kastar ut skattar.

- **Utsjånad:** ei lita kiste med glasfront (`glass = true`), slik at innhaldet er synleg også når ho
  er lukka. Ho er teikna i kode etter `ART_GUIDE.md`.
- **Plass:** ho rommar minst 20 små ting (diamant, mynt, perle, skjel).
- **Lokket:** kista opnar seg sjølv når ein halden ting er nærare enn 0,25 scene-einingar, og lukkar
  seg 1,2 s etter at tingen er sleppt eller teken vekk. Trykk opnar og lukkar som andre skap.
- **Lyd og humor:** når ein ting landar i kista, smekkar lokket med `CHOMP`. Kvar femte ting får kista
  til å vippe, spele `BURP` og sende ut ei glitrande sky.
- **Tilgang:** ho ligg gratis i møbelkatalogen på alle stader. Eksisterande og nye verder får éi kiste
  i soverommet i Familiehuset, éin gong, styrt av flagget `layout:treasure-box:1`. Finst det alt ei
  kiste i verda, blir det ikkje lagt til ei ny.
- **Lager:** ei kiste med ting i kan ikkje leggjast på lager. Ho opnar seg og vippar i staden. Ei tom
  kiste kan lagrast som andre møblar.

## 4. Ingen skatt forsvinn

Skattar er `GEM`, `COIN` og `PEARL`.

- `HouseAtticRules.trim` blir ikkje lenger brukt for diamantar og myntar. Loftskista og
  kartbordet kastar ikkje ut ein ny diamant når det alt ligg seks lause diamantar på loftet. Kista
  kastar berre ut så mange myntar at det blir høgst atten lause.
- Kjem det verken mynt eller diamant, hostar kista støv, ein møll flyg ut, og ho spelar `POOF`.
- Laus tyder: på loftet, `Mode.FREE`, ikkje halden og ikkje inni eit møbel (`inside < 0`).
- Plassgrensene (`Sim.capPlace`, `HouseGroundRules.cap`, `HouseGardenRules.limit` og
  `HouseCellarRules.limit`) fjernar aldri ein skatt. Dei fjernar den eldste lause tingen som ikkje er
  ein skatt. Er alle lause ting skattar, blir ingenting fjerna.

## 5. Sidepanelet lukkar seg sjølv

Når møbelpanelet er ope og barnet løftar ting eller figurar to gonger på rad, lukkar panelet seg.
Teljaren blir nullstilt når barnet rører eit møbel, dreg noko ut av panelet eller rører panelet.
Møbelknappen opnar panelet att. Éin flytta figur midt i møbleringa lukkar ikkje panelet.

## 6. Trykkbar døropning

Teikninga og treffområdet til trappene deler geometri, slik `FixtureDoors` gjer for skapdører.
Døropninga, holet eller luka som er teikna som ein del av ei trapp, høyrer med i treffområdet.
Alle `STAIRCASE`-møblar i Storhuset og Mitt hus blir gjennomgåtte, saman med den hemmelege tunnelen.
Eit trykk på døropninga gjer det same som eit trykk på trappa.

## 7. Like møblar i lageret blir eitt kort

I fanene Lager og Papirkorg blir like oppføringar viste som eitt kort.

- **Like** tyder same `Stored`-verdi: type, variant, `mode` og `door`. Kunst med eige motiv og dører
  med eiga kopling er derfor aldri like og blir ståande kvar for seg.
- **Talet:** er det to eller fleire, viser kortet talet i ein rund merkelapp oppe til høgre.
- **Ta ut:** trykk eller draging tek ut éin, den som sist vart lagd inn. Talet går ned med éin.
- **Slett og hent tilbake:** søppelknappen flyttar éin til papirkorga. «Hent tilbake» i papirkorga
  hentar éin.
- **Rekkjefølgje:** korta står der den eldste oppføringa i kvar gruppe står i dag. Rekkjefølgja i
  lista blir elles ikkje endra.
- Teljaren på fana viser framleis det samla talet på møblar.

## Lagring

Ingen endring i lagringsformatet utover eit nytt flagg i `world.flags` og ein ny `FixtureType`.
Gamle lagringar skal opnast uendra, med éi ny skattekiste i Familiehuset.

## Feil og grensetilfelle

- To fingrar: held éin finger ein ting og ein annan eit møbel, viser hjørnet sekken. Slepp blir
  handsama for kvar finger for seg.
- Halden figur som er valt spelar: går i sekken og blir sett på pause, slik 1.7.0 gjer.
- Køyretøy med passasjerar og møblar som er ein gjennomgang, kan ikkje lagrast (`canStore` er uendra).
- Avbroten gest (`cancel`): hjørnet går attende til vanleg storleik, og ingenting blir lagt bort.
- Mobil (`compact`): same reglar med `bagRadius` for mobil.

## Kontroll

- Einingstestar for: treffområde og kamera-unntak for hjørnet, val av bilete, bomtrykk-hint
  (næraste møbel, avstand, pause), skattekista (plass, lokk, kvar femte, lagersperre), loftsreglane
  (ingen skatt forsvinn, tom kiste), plassgrensene, panelteljaren, treffområdet til kvar trapp,
  gruppering i lageret og eingongsoppgraderinga på ei gammal lagring.
- Android-test: dra ein ting, ein figur og eit møbel til hjørnet med panelet lukka.
- `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest` og `lintDebug` gjennom
  `scripts/Build-Trollfoss.ps1`.
- Sett på nettbrett 1920 × 1200 / 240 dpi og kort på mobil 2400 × 1080 / 420 dpi, på nynorsk og bokmål.
- Ingen release før brukaren ber om det. Den eigentlege prøva er at barnet bruker det.
