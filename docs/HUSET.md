# Storhuset – designguide og byggjekontrakt

Storhuset er det store, spennande huset i Trollfoss: fire etasjar og ein hage, mange rom, hemmelege
gangar og ei kjede av gylne nøklar. Det skal vere **forbløffande** – eit hus ein kan bruke timar på å
utforske, med noko nytt og morosamt bak kvar dør. Dette dokumentet er kontrakten for alle som byggjer på
huset. Les òg `docs/AI_INSTRUCTIONS.md`, `docs/ART_GUIDE.md` og `docs/DESIGN.md`.

## 1. Idé og stemning

Eit stort, gamalt og litt skeivt trehus oppe i lia bak fossen, med tårn, glasshage og altan. Ikkje
stereotypisk norsk: ei vanleg, varm, litt eventyrleg storvilla. Alle rom skal ha sin eigen farge og sitt
eige humør, og det skal skje noko når ein trykkjer på nesten alt. Humor er viktigast: kvar ting skal ha ein
snill, morosam reaksjon (sjå `docs/DESIGN.md`).

**Rolf** er robotbutleren. Han er høfleg, tar alt bokstavleg og snublar over sin eigen forsiktigheit.
**Sture** er det snille spøkelset på loftet. Han er sjenert, kiler folk og blir redd for sitt eige skuggebilete.
Dei to er nye figurar (`Species.ROBOT`, `Species.GHOST`). Namna deira og alle andre nye namn står i §9 og
kan byttast når som helst.

**Dei fem gylne nøklane.** Ein nøkkel gøymer seg på kvar etasje, på ein morosam stad som krev ei lita
handling (mate den kjøtetande planten, snurre globusen, stille inn radioen …). Når ein finn ein, ringjer
det og nøkkelen lyser i talet øvst på skjermen. Har ein alle fem, opnar tunnelen frå kjellaren til
Trollhola.

## 2. Oppbygging

Huset er **fem stader** (`PlaceId.MANOR_*`) som er knytte saman med **vegar** (`Passage`). Berre
`MANOR_GROUND` har knapp på kartet.

| Stad | Namn i spelet | Breidd | Rom |
| --- | --- | --- | --- |
| `MANOR_GROUND` | Storstova | 12 | hall, stove, bibliotek, spisestove, kjøkken, vinterhage |
| `MANOR_UPPER` | Andre høgda | 12 | repo, barnerom, leikerom, bad, soverom, altan |
| `MANOR_ATTIC` | Loftet | 9 | lager, spøkelsekroken, tårnet, det hemmelege rommet |
| `MANOR_CELLAR` | Kjellaren | 10 | verkstad, vaskerom, fyrrom, basseng og badstu, festrom |
| `MANOR_GARDEN` | Hagen | 9 (ute) | drivhus, dam, tretopphytte, skur, trampoline |

Breidda er i scene-einingar; ein skjerm viser ca. 2,1–2,4 einingar, så kvart rom er omtrent éin skjerm.
Rom er x-område (`Floor.rooms`) og blir brukt av heimedesignaren: tapet og golv kan byttast **per rom**.

### Filer

Kvar etasje eig sine eigne filer. Du endrar berre dine, og dei delte filene berre på ankerpunkta (§7).

| Etasje | Domene (blåkopi, reglar, spesar) | Teikning | Effektar |
| --- | --- | --- | --- |
| Storstova | `domain/HouseGround.kt` | `ui/art/HouseGroundArt.kt` (+ fleire `HouseGround*.kt`) | `ui/play/HouseGroundFx.kt` |
| Andre høgda | `domain/HouseUpper.kt` | `ui/art/HouseUpperArt.kt` | `ui/play/HouseUpperFx.kt` |
| Loftet | `domain/HouseAttic.kt` | `ui/art/HouseAtticArt.kt` | `ui/play/HouseAtticFx.kt` |
| Kjellaren | `domain/HouseCellar.kt` | `ui/art/HouseCellarArt.kt` | `ui/play/HouseCellarFx.kt` |
| Hagen | `domain/HouseGarden.kt` | `ui/art/HouseGardenArt.kt` | `ui/play/HouseGardenFx.kt` |

Delt og ferdig (ikkje endre utan grunn): `domain/House.kt` (`Floor`, `FloorRules`, `Passage`, `House`,
`HouseKeys`, `HouseFx`), `ui/art/HouseArt.kt` (ruting), `ui/play/HouseFx.kt` (felles effektar),
`ui/play/FxStage.kt`, `ui/screens/HouseHud.kt`.

Filene som alt finst er **startpunkt** (stubbar): ein enkel blåkopi med vegane og nokre møblar, ein
standardbakgrunn og tomme teikne- og effektfunksjonar. Byt dei ut. Du kan leggje til fleire filer med same
prefiks (`HouseGroundKitchenArt.kt` …) når ei fil blir for stor (hald filer under ca. 1500 liner).

## 3. Kontrakten: vegar og ankomstpunkt

`Passage(id, place, fixture, kind, to, arrive, oneWay, locked)`. `fixture` er blåkopi-indeksen til møbelet i
etasjen (må vere ein av `STAIRCASE, LIFT, SLIDE, FIRE_POLE, HATCH, LADDER, SECRET_DOOR, DOOR, DUMBWAITER`).
`arrive` er namnet på ein `Arrival` i målet. **Id-ane og ankomstnamna under må ikkje endrast.** Du kan flytte
møblane og endre indeksane i din eigen blåkopi, og då oppdaterer du `Passage.fixture` tilsvarande.
`HouseTest` kontrollerer at alt heng saman (ekte møblar, ekte ankomst, veg tilbake for alle vegar som ikkje er
einvegs).

| Veg (id) | Frå → til | Type | Ankomst i målet | Merknad |
| --- | --- | --- | --- | --- |
| `ground-stairs-up` | Storstova → Andre høgda | trapp | `landing` | storslått hovudtrapp |
| `ground-lift` | Storstova → Andre høgda | heis | `lift` | |
| `ground-cellar-door` | Storstova → Kjellaren | trapp (dør under trappa) | `stairs-top` | |
| `ground-bookshelf` | Storstova → Loftet | hemmeleg dør | `secret-room` | låst: `manor_bookshelf` (bokhyllespaken) |
| `ground-dumbwaiter` | Storstova → Andre høgda | kjøkenheis | `dumbwaiter` | |
| `ground-garden-door` | Storstova → Hagen | dør | `house-door` | |
| `upper-stairs-down` | Andre høgda → Storstova | trapp | `stairs-foot` | |
| `upper-lift` | Andre høgda → Storstova | heis | `lift` | |
| `upper-stairs-up` | Andre høgda → Loftet | trapp | `stairs` | |
| `upper-slide` | Andre høgda → Storstova | rutsjebane, einvegs | `slide-end` | landar i stova |
| `upper-pole` | Andre høgda → Storstova | brannstolpe, einvegs | `pole-end` | landar i hallen |
| `upper-dumbwaiter` | Andre høgda → Storstova | kjøkenheis | `dumbwaiter` | |
| `upper-balcony-slide` | Andre høgda → Hagen | rutsjebane, einvegs | `slide-end` | landar i dammen |
| `upper-laundry-chute` | Andre høgda → Kjellaren | luke, einvegs | `laundry-chute-end` | landar i vaskekorga |
| `attic-stairs-down` | Loftet → Andre høgda | trapp | `attic-stairs` | |
| `attic-secret-down` | Loftet → Storstova | hemmeleg dør | `library-secret` | låst: `manor_bookshelf` |
| `cellar-stairs-up` | Kjellaren → Storstova | trapp | `cellar-door` | |
| `cellar-tunnel` | Kjellaren → Trollhola (`LAB`) | hemmeleg dør, einvegs | `tunnel-end` | låst: `manor_tunnel` (opnast av alle fem nøklar) |
| `garden-house-door` | Hagen → Storstova | dør | `garden-door` | |

Å ta ein veg: trykk på møbelet. Alle figurar som står ved det (±0,55 einingar) følgjer med, og kameraet
følgjer til ankomstpunktet. Einvegs rutsjebaner og stolpar kastar figuren ut med eit hopp og eit fnis.
Kjellaren **må** få ei veg tilbake frå Trollhola: legg eit `TUNNEL_DOOR`-møbel til sist i `LAB`-blåkopien
(`domain/Places.kt`, ny indeks sist i lista) og ein `Passage` frå Trollhola til `tunnel` i kjellaren. Då set
du `cellar-tunnel` til `oneWay = false` og legg `Arrival("tunnel-end", …)` i `House.outside[LAB]` (finst).

Ein veg kan ha eiga teikning og eigen animasjon (trappa går opp, heisen lyser, rutsjebana glitrar, luka
smell). Same `FixtureType` kan sjå ulik ut per etasje (`f.variant`, `f.place`).

## 4. Delt mekanikk

- **Trykk og slepp** (`FloorRules.tap/step/seatPoint/drop`): sjå mønsteret i `domain/Attractions.kt`. Reglane
  sender hendingar med `listener.onFx(Fx.HOUSE, x, y, fixture, thing, HouseFx.pack(kode, arg))`.
  Kvar etasje har ein kodeblokk i `HouseFx` (`GROUND = 100` … `GARDEN = 500`, 100 koder kvar). Effektfila di
  (`XxxFx.play`) gjer koden om til lyd og gneist med `FxStage`: `sfx`, `after`, `burst`, `particle`, `faces`,
  `voice`, `laughAround`. Rør ikkje `Engine.kt`.
- **Lys og mørke.** Møblar med `spec.light` som er `on` lyser opp området sitt (som om natta). `Floor.darkness`
  (loftet 0,6, kjellaren 0,45) gjer etasjen dunkel òg midt på dagen. Ein finger på skjermen er lommelykt. Lag
  lysbrytarar (`LAMP`/eigne lampar), stearinlys og lommelykt-ting. Ingenting må bli ubrukeleg i mørke: glimt og
  vegar skal alltid kunne finnast.
- **Glimt.** Minst tre per etasje, i `domain/Secrets.kt` (ankerblokk per etasje; byt ut stubbane). Mønster:
  under ei pute, i eit skap (`inside`), eller ei hending (`event = true`, ope med `sim.unlock(id)`).
  Telleren og boka tel automatisk.
- **Nøkkel.** Éin gylden nøkkel per etasje: `sim.flag("manor_key_ground")` (`_upper`, `_attic`, `_cellar`,
  `_garden`). `Sim.flag` spelar jingelen og, når alle fem er funne, opnar tunnelen. Nøkkelen skal vere ein
  **ting** (lag `ThingType.GOLDEN_KEY` éin gong; sjå §7) eller ein gnist ein finn ved ei handling – men det
  skal vere ei historie: «sjå kva nøkkelen gjorde der!».
- **Oppdrag.** Fire per etasje i `domain/Tasks.kt` (nye `Deed` i etasjen sin blokk, og `Task` i lista). Eit
  oppdrag har ein stad, eit bilete (møbel/ting/dyr) og ei handling. Ikkje lag oppdrag som krev tidlegare
  funne ting. Eit barn på fire skal kunne klare dei.
- **Rom og stil.** `Decor.rooms(place)` gir romma. Teikn kvart rom med ein eigen standardstil (veggfarge/
  tapet, golv, takpynt) og respekter `styles` frå heimedesignaren (`styles.wallOf(i)`/`floorOf(i)`): har
  barnet valt tapet eller golv, skal det synast. Tapet og golv frå settet (`paperWall`, `layFloor`) er dyre:
  bruk dei **berre** når barnet har valt eitt, og flate fargar og bufra geometri elles (slik `homeBack`
  gjer). Katalogen i `Decor.catalogue` gir allereie inne-møblar til huset.
- **Folk og dyr** (`PersonDef` i blåkopien). Folk frå huset (alle figurar som står i ein etasje ved start)
  går av seg sjølve mellom etasjar (`House.shuffle`, `Floor.hangouts`): fyll `hangouts(night)` med
  gode plassar (kjøkenet om morgonen, sengene om natta). Dei nye figurane skal ha **eigen åtferd**:
  Rolf serverer og ryddar, Sture gøymer seg og skremmer (og blir skremt). Bruk `Life`-mønsteret
  (`domain/Life.kt`) for ønske og gåing, men ikkje endre det: legg eigne reglar i etasjefila di.
- **Musikk.** `MusicTheme.MANOR` (Storstova og Andre høgda), `ATTIC`, `CELLAR`, `GARDEN` finst som
  startpunkt i `audio/Music.kt`. Du kan finpusse oppskrifta (`Recipe`) for etasjen din.
- **Lydar.** Nye `Sfx` går i etasjen sin ankerblokk i `audio/Synth.kt` (enum og `voices`). Gjenbruk
  eksisterande lydar og pitch først. Alt er laga i kode.
- **Tekst.** Alt barnet ser eller høyrer av tekst skal vere `Txt(nn, nb)` i `ui/Strings.kt` (ny blokk per
  etasje). Berre det nødvendige. **Ingen tekst i grafikken.**

## 5. Kvalitetskrav

**Utsjånad.** «Nordisk leikekasse»: ein blekkstrek rundt alt, to-tone skugge, éin glans, runde former,
maks 3–4 fargar per ting. Les `docs/ART_GUIDE.md`, `ui/art/SceneKit.kt` (`Stage`, `inScene`, `drawBase`,
`floorQuad`), `ui/art/RoomArt.kt` (veggar, golv, skillevegger, listar, lampar) og døme: `FixtureArtBerg.kt`,
`BergArt.kt`, `FixtureArtRooms.kt`. Ting er 40–60 dp: detaljane må lesast i liten storleik. Alt skal sjå
omtenkt ut: kvart møbel har ein detalj ein oppdagar (ein tidsfordriv, ein flekk, ein blunkande sak).
**Ikkje gjenbruk same teikning på ulike møblar.** Nett rød kross er forbode i Noreg: bruk grøn plus/hjarte.

**Yting** (hard). Alt blir teikna 60 gonger i sekundet på billige nettbrett.
- Heile bakgrunnen for ein etasje er 9–12 einingar brei. Teikn berre det som er synleg (`st.sees`), og bygg
  geometri (stiar, punktlister) **éin gong per `u`** i ei `Memo`/`lazy`-bufring (sjå `homeStatic`,
  `BergArt`). Ingen `Path`-bygging i løkker per ramme for kvart rom, ingen store løkker (maks ca. 200
  primitiv per rom i bakgrunnen).
- Ingen `Path.op`, ingen uskarpheit, ingen bilete. `safeRadialGradient` (aldri `Brush.radialGradient`).
  Ingen allokering av store lister per ramme.
- Stille møblar blir stempla éin gong av `SpriteCache`; rørsle blir oppdaga automatisk (teikn to gonger).
  Hald rørslene små og rolege, og bruk `pen.t`, `f.anim`, `f.angle`, `f.on` osb.
- Mål: i debug-bygget logger `TrollfossPerf` ms/ramme kvart 120. ramme (`adb logcat -s TrollfossPerf`).
  Etter oppvarming skal `total` vere **under 12 ms** på telefon-emulatoren for dei tyngste romma.

**Test.** Skriv enhetstestar i `app/src/test/java/app/trollfoss/domain/` (eigen fil, t.d. `HouseGroundTest.kt`):
reglane dine (trykk, hendingar, nøkkel, glimt, oppdrag, sparing). `HouseTest` og dei eksisterande testane
må halde seg grøne, og `lintDebug` utan feil.

## 6. Kva som skal vere i kvar etasje

Dette er **minimumet og ei idéliste**: alt merkt (m) skal vere med, resten er forslag. Du skal gjere det
**betre enn lista**: finn på fleire spennande detaljar. Kvart møbel/kvar ting skal ha ei morosam eller
overraskande oppførsel. Dei fleste ting skal kunne gripast, kastast og gjevast til nokon.

### 6.1 Storstova (`MANOR_GROUND`) – eit storslått første inntrykk

- **Hall** (0–2): storslått bøygd hovudtrapp med gelender og trappeløpar (m); heisdøra med messinginstrument
  som tel etasjar (m); stor krystallkrone som svingar og glitrar når ein trykkjer (m); rustning «Riddar
  Rusten» som klirrar, helsar og hikstar så hjelmen spretter av (m); gulvur; knaggrekkje med hattar ein kan ta;
  paraplystativ; postluke som kastar ei dagleg overraskingsbrev; kjellardøra under trappa som knirkar.
- **Stove** (2–4): steinpeis med levande eld, strømper og ei klokke; stor sofa og lenestolar (fleire plassar);
  **TV med fem animerte kanalar** teikna i kode (dansande troll, rakett, fisk, matlaging, snø/stille bilete) (m);
  **filmkveld**: lyset går av, popkornskål (m); akvariumvegg med fiskar som fylgjer fingeren; globus som snurrar
  og hoppar ut ei kartnål; landingsmatte med sekkestolar der rutsjebana frå andre høgda lender (m).
- **Bibliotek** (4–6): golv-til-tak-bokhyller med rullestige som glid når ein trykkjer (m); leseplass; skrive-
  pult; bøker som flaksar ut; ei snakkande bok. **Bokhyllespaken** (m): ei raud bok er ein spak. Trekkjer ein
  henne, snurrar hylla og opnar `ground-bookshelf` (set flagget `manor_bookshelf`).
- **Spisestove** (6–8): langbord med seks plassar og dekketøy (m); **dukk-bordet-sjølv**: ring på bjella og
  tallerkenane flyg inn (m); kake med lys som ein kan blåse ut; skjenk; ei fødselsdagssong.
- **Kjøkken** (8–10): stor øy med stolar, to omnar, pizzaomn, stor kjøleskap (innvendig fleire hyller), blandar
  som sprutar (m); skaplukene med syltetøy; **kjøkenheis** (`ground-dumbwaiter`) (m). Nye oppskrifter: legg dei i
  `domain/Recipes.kt` berre om det trengst (hald det lite).
- **Vinterhage** (10–12): glas-vegger med plantar som veks når ein vatnar (m), fontene, sommarfuglar,
  hengekøye, **den kjøtetande planten Sofie** som rapar når ho får mat (m) – her kan nøkkelen ligge. Døra til hagen.
- Nøkkel: ein stad på golvet i etasjen (til dømes bak peisen, i pianoet eller i Sofie). Rolf bur her og
  serverer (m).

### 6.2 Andre høgda (`MANOR_UPPER`) – leik og lune

- **Repo** (0–2,2): trapper ned og opp, heisen, familiebilete som **blunkar og vinkar** (m), brannstolpen (m),
  kjøkenheisen, ein vindaugsbenk.
- **Barnerom** (2,2–4,2): køyseng (m); **leiketog som går rundt i rommet** og tek med seg ein passasjer (m);
  leikekiste; klossetårn å velte; **dokkehuset i huset** (m): eit lite Storhus med små figurar som speglar kven
  som er kvar; plakatar; nattlampe.
- **Leikerom** (4,2–6,5): **ballbasseng** (ting flyt, ein kan hoppe uti) (m); **inneløyve rutsjebane** ned til stova
  (`upper-slide`) (m); klatrevegg; trampoline; staffeli der ein kan **sprute farge** (m); karaokescene.
- **Bad** (6,5–8,5): badekar med boblar og badeender (`BATH`-regelen finst), toalett (finst), speil som dampar,
  dusj med regnboge, **vaskeluka** (`upper-laundry-chute`) (m).
- **Soverom** (8,5–10,5): himmelseng; **garderobe som kler om** den nærmaste figuren (opnar, klede flyg, ein
  ny drakt) (m); sminkebord; speiljuvelboks med dansande ballerina; gyngestol; stjernehimmel i vindauget.
- **Altan** (10,5–12): rekkverk med blomekassar; fuglemating med fuglar som kjem; hengestol; utsikt over
  bygda (parallakse); den lange **altanrutsjebana** til hagen (`upper-balcony-slide`) (m).
- Nøkkel: eit morosamt gøymestad (i badeendene, under leiketoget …).

### 6.3 Loftet (`MANOR_ATTIC`) – mørkt, kilande og mystisk

- Dunkelt (`darkness 0,6`): lommelykt med fingeren (m), stearinlys, lampar. Nye ting: lommelykt/stearinlys.
- **Lager** (0–3): kister med kostyme (**drakt-kista**: opnar og kastar ut forkledningar: pirat, ridder,
  prinsesse, kjempeblomst …) (m); gamle leiker; gyngehest som gyng; støvdekte lakenmøblar (eit av dei er
  Sture); spindelvev med vennlege edderkoppar.
- **Spøkelsekroken** (3–5,5): Sture sitt gøymestad: lenestol, grammofon med skrøpelege plater (m), bøker; Sture
  gøymer seg, dukkar opp, kiler, byter om på ting og spelar **gøym-og-leit** med fingeren (m): ein finn han
  tre gonger og får eit klistremerke. Når han nys, flyg støvet.
- **Tårnet** (5,5–7,5): runde rommet med **teleskopet** (opnar teleskopskjermen som allereie finst) (m),
  stjernekart, messinginstrument, værhane, uglehole.
- **Det hemmelege rommet** (7,5–9) (m): bak biblioteket (`attic-secret-down`): skattekart-bord, kiste med
  mynter, globus, **slektstreet** på veggen. Her er den store belønninga.
- Nøkkel: i spøkelsekroken eller i ei kiste.

### 6.4 Kjellaren (`MANOR_CELLAR`) – maskinar, vatn og fest

- Dunkelt (`darkness 0,45`), men **festrommet** er opplyst (m).
- **Verkstad** (0–2,5): arbeidsbenk (finst), reiskapsvegg, sag som sagar, Rolf si ladestasjon.
- **Vaskerom** (2,5–4,5): vaskemaskin og tørketrommel som snurrar (m), **sokkemonsteret** som et sokkar og
  rapar dei ut att i feil par (m), ei stor vaskekorg som tek imot luka frå badet, strykebrett, klesline.
- **Fyrrom** (4,5–6): stor dampkjele med røyr og trykkmålar (m): vri på tre ventilar → damp, klonk og heile
  huset rumlar (m); her kan nøkkelen gøyme seg bak ein ventil.
- **Basseng og badstu** (6–8,5): innandørs basseng (svømming, stupebrett) (m), badstu (finst `SAUNA`), ringar å
  flyte på, glidar ned i bassenget.
- **Festrom** (8,5–10) (m): **dansegolv med lysande fliser** som lyser når figurar dansar (m), diskokule (finst),
  jukeboks med fem melodiar, karaoke, snackbar, sekkestolar.
- **Tunnelen** (m): hemmeleg dør i enden: **gruvevogn-tur** gjennom tunnelen til Trollhola (`cellar-tunnel`),
  opnar først når alle fem nøklar er funne (`manor_tunnel`).

### 6.5 Hagen (`MANOR_GARDEN`) – ute, parallakse, vêr

- Utandørs (`outdoor`): himmel, sol og vêr (`pen.weather`), parallakse, gras. Bruk same mønster som
  `BergArt.kt`.
- **Drivhus** (m): glas, plantar som veks til kjempegrønsaker ein kan sitje i; **dammen** med åkandblad og
  froskar som kvekar i kor (m); altanrutsjebana lander i dammen med eit plask (m); **tretopphytta** (m): stort tre
  med repstige, plattform, tau-bane (ein ride over hagen) og dekkgynge; **skur** med hagereiskap; sandkasse;
  trampoline (finst `TRAMPOLINE`); grill som stekjer pølser; hengekøye; fuglehus; **hagenissar** som flyttar
  seg når ingen ser (m).
- Sesong: tilpassa snø (snømann finst), blomar og haust (sjå sesongjobben seinare; lag fixtures som kan
  endre utsjånad etter `Season`, men les det frå `Pen` når det finst).

## 7. Delte ankerpunkt (dei einaste stadene du endrar delte filer)

Kvar delt fil har ein blokk per etasje, skilt av ei tom linje (`// ---- ground floor ----` osv.). Legg
innhaldet **berre i din blokk**, så flettar git utan konflikt.

**Namneregel:** alle nye enum-namn (`FixtureType`, `ThingType`, `Sfx`, `Deed`) får ein etasjeprefiks, så to
agentar aldri lagar same namn: Storstova `GR_`, Andre høgda `UP_`, Loftet `AT_`, Kjellaren `CE_`, Hagen `GA_`
(døme: `GR_CHANDELIER`, `UP_BALL_PIT`). Glimt-id-ar har same prefiksmønster (`ground_`, `upper_` …). Dei
delte vegtypane (`STAIRCASE`, `LIFT`, `SLIDE` …) og `GOLDEN_KEY` har ingen prefiks.

| Fil | Anker |
| --- | --- |
| `domain/Fixtures.kt` (`enum FixtureType`) | `// ---- Storhuset ... (HouseXxx.kt) ----` (nye møbeltypar; spesen går i `Floor.specOf`) |
| `domain/Things.kt` (`enum ThingType`) | `// ---- ground floor ----` osv. (nye ting) |
| `domain/Secrets.kt` | `// ---- ground floor ----` osv. (glimt) |
| `domain/Tasks.kt` | `Deed`-enumen og `ALL`-lista, `// ---- ground floor ----` osv. |
| `audio/Synth.kt` | `enum Sfx` og `voices()`, `// ---- ground floor ----` osv. |
| `ui/Strings.kt` | legg ny blokk `// Storhuset <etasje>` på slutten av `S`-objektet |

**`ThingType.GOLDEN_KEY`** finst allereie (teikna i `HouseArt.kt`): bruk han for nøkkelen din. Gjer han
gjerne til eit eige overraskingsstunt på etasjen din (han kan gøymast, flyge eller bli bite av ein plante).

`FixtureArt`, `ThingArt`, `PlaceArt`, `Engine`, `Sim`, `Attractions` og `Life` **skal du ikkje endre** (ruting til
etasjefilene er alt ferdig). Må du endre noko i dei: gjer det minst mogleg, og skriv det i rapporten.

## 8. Arbeidsmåte

- Du jobbar i ein eigen git-worktree og branch (`git worktree add C:\trollfoss-wt\<namn> -b house/<namn> main`
  frå `C:\topa`). Commit ofte (`git add -A; git commit`, korte engelske meldingar med
  `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`). Flett `main` inn i branchen din av og til.
  Ikkje push, ikkje opprett release og rør aldri `.signing/` eller `signing.properties`. Lag **aldri** ny
  signeringsnøkkel.
- **Bygg alltid med låsen** (maskina har lite ledig minne og fleire agentar bygger samtidig; skriptet tek
  éin sperre for heile maskina og køyrer éi bygging om gongen, utan lingrande Gradle-daemon). Frå worktreen din:
  ```powershell
  powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Locked.ps1 -Tasks ':app:testDebugUnitTest',':app:lintDebug',':app:assembleDebug' -Suffix .<namn>
  ```
  Berre `-Tasks ':app:compileDebugKotlin'` er raskast for sjekk av kode. Pakkenamnet blir `app.trollfoss.<namn>`,
  så du overskriv ingen andre. Køyr aldri `gradlew` direkte og start ingen eigne Gradle-daemonar.
- **Emulator**: berre `emulator-5554` (telefon, `C:\Android\sdk\platform-tools\adb.exe`), og alltid med
  `Run-Locked`, som sørgjer for at éin agent om gongen har skjermen. Lag eit lite `.ps1`-skript som i **éi**
  køyring installerer, startar, ventar og tek skjermbilete, og køyr det slik:
  ```powershell
  powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Run-Locked.ps1 -Name emulator -Command "& C:\trollfoss-wt\<namn>\shot.ps1"
  ```
  I skriptet: `adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk`, så
  `adb -s emulator-5554 shell am start -f 0x20000000 -n app.trollfoss.<namn>/app.trollfoss.MainActivity --es place manor_ground --es night off --es weather SUN`
  (stadnamn `manor_ground|manor_upper|manor_attic|manor_cellar|manor_garden`), vent 15–25 s (første ramme er
  treg), `adb -s emulator-5554 exec-out screencap -p > fil.png`, og til slutt
  `adb -s emulator-5554 shell am force-stop app.trollfoss.<namn>`. Scroll i scena med
  `adb shell input swipe 1800 600 300 600 600`. Ta nokre skjermbilete per økt. Fyrste start etter ei
  ny installasjon kan gi ein «ikkje svar»-dialog: vent og prøv att. Rør **aldri** `emulator-5558`, `5560`
  eller `5562`. Ikkje la kommandoar hengje: bruk tidsavgrensing.
- Hald deg til dine filer. Ser du noko galt i ein delt fil, fiks det minst mogleg og skriv det i
  rapporten. Står du fast, vel det enklaste som held kvaliteten, og skriv kva du gjorde.
- Når du er ferdig: rapportér (kort) **kva som er laga**, **kva som mangler**, **avvik frå kontrakten**,
  **dei nye `Sfx`/`Thing`/`Deed`/glimt-id-ane**, og kvar du endra delte filer. Siste commit-hash på branchen din.

## 9. Figurar og namn

| Figur | Art | Stad | Merknad |
| --- | --- | --- | --- |
| Rolf | `ROBOT` | Storstova | butleren; serverer, ryddar, vil gjerne vere til nytte |
| Sture | `GHOST` | Loftet | spøkelset; sjenert, ler lett, blir redd for sin eigen skugge |

Nye namn her er nye og kan byttast ut av familien. Dei 20 namna frå `docs/AI_INSTRUCTIONS.md` bur
andre stader og blir ikkje flytta. Dyr (katt, hund …) kan vere med i huset som vanleg.

### Rolf og Sture i koden

Begge er ferdig teikna og har eigne små reglar (`domain/Figurar.kt`, `ui/art/PersonArtHouse.kt`,
`PersonArtRolf.kt`, `PersonArtSture.kt`, `PersonArtXray.kt`, `ui/play/FigurarFx.kt`). Etasjefilene treng ikkje
teikne dei, berre bruke dei:

- Dei står, sit og ligg som folk (`Anatomy`: hatt, briller, hand og munn passar). **Rolf** held alltid eit
  lite serveringsbrett på høgre hand, så ting blir lagde der. **Sture** held ting på høgre arm, ved sida av
  magen, så andletet ikkje blir dekt.
- `sim.figurar.bow(rolf)` er ein høfleg bukk (hovud og hatt dukkar saman, med «ding»); han helsar òg av seg
  sjølv på folk som kjem bort til han, og dei vinkar tilbake. `sim.figurar.sneeze(sture)` er «ah … ah …
  tsjuu» med gammalt støv; det er berre støv (ingen `Deed.SNEEZE`, ingen hatt som flyg), og Sture nyser av og til
  sjølv.
- Sture kan krympe og «poppe»: set `Person.scale` (teikninga toler ned mot null). Han gøymer augo bak armane av
  seg sjølv ein augneblink, blir raud på kinna når han ler, og svevar med ei lita gyngerørsle (motoren legg
  ho på). Rolf har antenne, glødande auge og ei brystlampe som slår som eit hjarte.
- Mat: Rolf slukar alt som «drivstoff» (`Give.FINISHED`, dampar frå antenna og bukkar), Sture snusar og gir
  maten tilbake med eit lite kast (`Give.SNIFF`). Eliksirar og pepar verkar som vanleg.
- Alle ansikt (`Face`) er teikna for begge; `PersonAnim.achoo` er den støvete nysinga. Lydar: `Sfx.FG_ROLF_*`
  og `Sfx.FG_STURE_*` (stemmene deira i `FigurarFx`). Røntgen viser tannhjul og ei badeand i Rolf, og ein
  liten spøkelsesvenn i Sture.
- Sjå dei begge, med alle ansikt, stillingar, hattar og ting: debug-aktiviteten `FigurarSheetActivity`
  (`--es page faces|poses|worn|small|big|peek|xray|thumb`).

## 10. Ferdig-kriterium for ein etasje

- Alle (m)-punkta er med og fungerer, med lyd, rørsle og humor. Minst fire oppdrag, minst tre glimt, ein
  nøkkel, ei morosam hending for kvart rom.
- Eigne testar og `HouseTest` er grøne; `lintDebug` utan feil; nynorsk og bokmål finst for alt.
- Sett på emulatoren (telefon): kvart rom, dag og natt, minst éi ferdig handling frå kvart rom, og
  `TrollfossPerf total` under 12 ms etter oppvarming. Skjermbilete av kvart rom ligg i rapporten (stiar).
