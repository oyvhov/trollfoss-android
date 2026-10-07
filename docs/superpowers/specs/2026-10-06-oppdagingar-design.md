# Oppdagingar, Trollfossen og nivå 7–10 – design

Dato: 2026-10-06. Grein: `claude/oppdagingar` (frå `origin/main` e3e5e02, publisert 1.10.0).
Godkjend i samtale med brukaren bolk for bolk; skisser i Git-ignorert `.superpowers/brainstorm/`.

## 1. Bakgrunn

Brukaren har testa 1.10.0 med barna sine:

- Barna var mest opptekne av **fri leik i den opne verda**, ikkje av oppdragsboka.
- Merke og nye nivå kom **for seint**; dei mista interessa før neste gåve.

I dag kjem merke nesten berre frå oppdrag (`Tasks.kt:117`), glimt (`Sim.egg`, `Sim.kt:163`),
togreparasjonen (`ToyPlay.kt:82`), redningsleiken (`CreativePlay.kt:132`) og innflyttingsfesten
(`MineBuilder.kt:346`). Fri leik gir ingenting. Nivå 1–6 krev 0/2/5/9/14/20 merke.

## 2. Mål

Barnet skal merke at **verda belønnar nysgjerrigheit**: å prøve noko nytt gir straks eit merke med ein
morosam reaksjon, nivåa kjem av seg sjølv medan barnet leikar, og vegen mot nivå 10 er synleg på kartet.
Rekkjefølgja for «spennande» (brukaren sitt val): veg og belønning › overraskingar og humor ›
levande scene › dristigare stil.

Ferdig tyder: fungerer i både mobil (2400 × 1080 / 420 dpi) og nettbrett (1920 × 1200 / 240 dpi),
nynorsk og bokmål, gamle lagringar blir bevarte, ingen ting/figurar/opplåsingar går tapt, grøne
einings- og Android-testar. Barnetest er eit eige steg.

## 3. Utgåver

| Utgåve | Innhald |
| --- | --- |
| **1.11** | Oppdagingsmerke, nye nivågrenser, Trollfossen på kartet, gåvepakka, Oppdagingsboka, nivå 7 (fire leiker), tullehendingar med troll som tittar fram, Mailinn |
| 1.12 | Nivå 8 (ekkoboks, danseteppe, konfettimaskin, lysrigg med discokule), lys og partiklar i scena |
| 1.13 | Nivå 9 (hjelperobot, robotverkstad, rakettsett, reaksjonsbane), lydbilete per stad |
| 1.14 | Nivå 10 (drakevogn, luftskip, gøymetroll, trollfest-knapp), regnbogefinale i fossen, glans på leiker |

Denne spesifikasjonen er detaljert for **1.11**. 1.12–1.14 er skildra i §12 og får eigne planar.

## 4. Oppdagingsmerke

### 4.1 Datamodell

- Nytt `enum class First(val group: FirstGroup, val picture: …)` i `domain/Firsts.kt`. Kodenamnet er
  `First` fordi `Sim.discover`/`world.discoveries` allereie finst for oppskrifter (`Sim.kt:1922`).
  UI-namnet er «Oppdagingar» / «Oppdagelser».
- `World.firsts: LinkedHashSet<String>` (enum-namn). Lagra som JSON-liste `"firsts"`; manglar i gamle
  lagringar → tom. Ukjende namn ved lasting blir hoppa over (framtidig nedgradering).
- `World.visited: LinkedHashSet<PlaceId>` for reiseoppdagingar; lagra som `"visited"`; tom i gamle
  lagringar. Staden verda opnar på, blir lagd til ved lasting.
- `Sim.first(f: First, x: Float?, y: Float?)`: viss `world.firsts.add(f.name)` → `world.stickers +=
  world.stickers.size`, `Progression.remember(world)`, deretter `listener.onFirst(f, place, x, y)`.
  Gjer ingenting medan verda blir bygd (`tasks.recording == false`).
- Angre (`WorldHistory`) skal bevare `firsts` og `visited` på same måte som `stickers`, `flags` og `eggs`.

### 4.2 Kjelder

Éin detektor, `FirstsDetector`, kopla inn i `Sim.Relay.onFx` (`Sim.kt:98-114`) ved sida av
`heard()` og `magic.react()`. Han slår opp `Fx` + `fixture?.type` (+ `param` der det trengst) og kallar
`sim.first(...)`. Hendingar som ikkje går gjennom `Relay`, får eitt eksplisitt kall der dei fullførast:
reise og ballonglanding (`TrollfossViewModel.travel` 379, `landBalloon` 352), bandet
(`Community.startBand` 98), bokhylledøra (`Community.linkDoor` 93), spegel/svevepute
(`CreativePlay.tap` 49-53), vippe/kran/transportband (`CreativePlay.tap` 47-57), robotstøvsugar
(`Sim.seat` 796), ballkast/fangst (`PersonPlay`), tannpuss (`PersonPlay.brush` 70), lesing/telefon
(`PersonPlay.use`). Kvar hending som allereie gir merke (glimt, oppdrag, tog, redning, innflyttingsfest)
er **ikkje** med som oppdaging, så same handling aldri gir to merke.

### 4.3 Katalog i 1.11 (68 oppdagingar)

| Gruppe | Oppdagingar (enum-namn → utløysar) |
| --- | --- |
| Køyre (10) | `TRACTOR`, `BOAT`, `SUBMARINE`, `BUMPER_CAR`, `BUS`, `TANDEM`, `TOY_TRAIN` (køyre PLAY_TRAIN eller ri UP_TOY_TRAIN), `VACUUM_RIDE` (figur på støvsugaren), `BALLOON` (første ballongreise), `SKY_ISLAND` (landa på Skyøya) |
| Leik (31) | `BUBBLE_POP`, `PILLOW_LAUNCH`, `MARBLES`, `COLOUR_SPRAY`, `POPCORN`, `PUMP`, `PHOTO`, `MINI_LIFT`, `WINDMILL`, `SEESAW`, `PUPPETS`, `PICNIC`, `CRANE`, `CONVEYOR`, `BUILD_TABLE`, `WATER_CHANNEL`, `WATER_WHEEL`, `RAIN_CLOUD`, `MIRROR`, `HOVER`, `PORTAL`, `OBSTACLE` (tunnel/hopp, WHEE), `APPLE_HARVEST`, `REPAIR_LIGHT`, `SECRET_DOOR`, `STAMP_ART`, `HANG_ART`, og nivå 7 (§9): `CABLE_CAR`, `DIVING_BELL`, `DIGGER`, `TREASURE_TABLE` |
| Venner (7) | `HUG`, `HIGH_FIVE`, `HOLD_HANDS`, `PET`, `LIVING_TEDDY`, `BAND`, `PARTY` |
| Kvardag (14) | `SNOWMAN`, `SANDCASTLE`, `COOK` (COOKED), `EAT` (ATE/DRANK), `BATH` (seta i bad), `SLEEP` (lagt i seng), `READ`, `PHONE`, `BRUSH_TEETH`, `THROW_BALL`, `CATCH_BALL`, `TIDY`, `PAINT_ROOM`, `BUILD_ROOM` (rom ferdig i Mitt hus) |
| Verda (6) | `FIRST_TRIP` (1 ny stad), `TRIP_5`, `TRIP_12` (tal i `visited`), `TREASURE_BOX` (første skatt i skattekista), `ATTIC_CHEST`, `PEEK_TROLL` (fann trollet som tittar fram, §10) |

Leikeoppdagingar for leiker barnet ikkje har opna enno, er synlege som silhuettar og blir nye
merke når leika blir opna; det gir ein naturleg sløyfe gåve → ny leik → nytt merke.

### 4.4 Gamle lagringar

Éin gong (flagg `firsts:retro:1`, same mønster som `StarterLayout.upgrade`) blir oppdagingar som
kan lesast sikkert ut av lagra tilstand, førte opp utan animasjon: `BUILD_ROOM` (bygd rom i Mitt hus),
`PET` (`community.pets` ikkje tom), `STAMP_ART` (`community.art`), `HANG_ART` (`community.wallArt`),
`SECRET_DOOR` (`community.doors`), `REPAIR_LIGHT` (`place:repaired:`-flagg) og `TREASURE_BOX`
(skattekiste med innhald). Kvar gir merke som vanleg. Ingenting anna blir gjetta; til dømes er eit
grodd tre (`garden:grown`) ikkje det same som ei hausting.

### 4.5 Slik ser det ut

- Engine tek imot `onFirst` og omset (x, y) til skjermkoordinatar. Eit merke (rund klistrelapp med
  oppdagingsbiletet) sprett opp der det skjedde, snurrar éin gong og flyg i ein boge til knappen som
  opnar boka. Knappen pulserer (eksisterande `bookPulse`).
- Lyd: kort «pling» (`Sfx`, ny eller eksisterande lys tone); haptikk som i dag.
- Venner innan rekkjevidd jublar gjennom `magic.react` med ein eigen reaksjon (ikkje via `Fx`, så
  oppdrag/deeds ikkje blir utløyste).
- Kø: høgst eitt flygande merke per 1,2 s; meir enn fem i kø blir slått saman til eitt merke med «+N».
- Ingen dialog og ingen banner. Redusert rørsle: merket blir vist i 0,8 s ved knappen utan boge.
- Skjermlesar: «Ny oppdaging: <namn>» som `liveRegion`-kunngjering.

## 5. Nivå og grenser

`Progression.thresholds` blir `0, 2, 4, 7, 10, 14, 19` i 1.11 (nivå 1–7). Seinare utgåver legg til
`25, 32, 40`. Berre nivå med ferdig innhald blir eksponerte (som før). Grensene blir berre lågare,
så ingen mister nivå; `toy:`-flagga gjer opplåsingar varige. Katalogens låsetal (`Decor.kt:76`) følgjer
automatisk. Mål: nytt nivå innan ti minutt for ein ny spelar, deretter om lag kvart 15.–25. minutt.

## 6. Trollfossen på kartet

- Fossen (`MapGeo.buildFall`, `MapTerrain.kt:501-591`, cx 0,485w, topp 0,158h, botn 0,392h) blir
  framgangsmålaren. Teiknast per bilete i det levande laget (`MapLive.drawLive`), ikkje i det
  mellomlagra biletet, så eit nytt merke syner straks utan ny oppbygging.
- **Fylling:** ein glitrande, lysare blå glød stig frå botnen av fossen. Høgda = framgang mot nivå 10:
  steinane ligg jamt, og glødet ligg mellom steinen for noverande nivå og neste etter delen av merke i
  noverande nivå.
- **Ti nivåsteinar** langs venstre kant av fossen frå botn til topp, med tal. Oppnådde nivå: gull;
  komande: krem. Ved neste stein: ei lita gåvepakke som hoppar når nivået er fullt. Nivå utan innhald
  (8–10 i 1.11) har stein utan gåve.
- **Trollet på toppen:** ein liten trollfigur ved toppen av fossen som blunkar og vinkar. Må ikkje
  dekkje Skyøya-merket (0,48, 0,12) eller det eksisterande trollansiktet med gjesp-glimtet
  (0,41w, 0,25h, `MapScreen.kt:180-224`). Nivå 10 (1.14): regnbogefoss og trollfest.
- **Trykk** på fossen eller steinane opnar boka på fana «Oppdagingar». Gjesp-glimtet skal framleis
  fungere.
- **Knapperada:** `ProgressButton` (`ToyControls.kt:113-126`) blir forenkla til gåveknappen med eit
  lite nivåtal; prikkane og teksten flyttar til fossen. Kartet får ikkje fleire knappar.

## 7. Gåvepakka

Erstattar `LevelGiftCard` (`ToyControls.kt:141-150`, vist i `TrollfossApp.kt:102`). Same tilstand
(`vm.levelGift`, `seenLevel`, flagg `gift:level:seen:N`); berre visinga er ny.

1. **Fall:** ei gåvepakke i fallskjerm dalar ned frå toppen (1,6 s) og landar nær midten av skjermen
   på golvhøgd.
2. **Vente:** pakka vrikkar, blunkar med to augo og fnisar med jamne mellomrom. Ho blokkerer ikkje
   leiken; berre trykk på sjølve pakka blir fanga.
3. **Pang:** trykk → konfetti, fanfare, og dei fire leikene for nivået sprett ut i bogar.
4. **I leiken:** i ein stad (Play) blir første leike sett ut med den eksisterande `vm.tryToy`-logikken
   på trygg plass i noverande stad, utan reise og utan dialog. Dei andre flyg inn i møbelknappen,
   som lyser. Er rommet fullt, flyg alle fire dit. På kartet flyg alle til gåveknappen.
5. Deretter `dismissLevelGift()`. Kryssar barnet fleire nivå samstundes, gjeld det nyaste (som i dag).
6. Redusert rørsle: pakka står straks på plass; pang utan bogar. Skjermlesar: «Gåve. Trykk for å opne».

## 8. Oppdagingsboka

`TasksScreen` får fanene **Oppdagingar · Gåver · Oppdrag**, i den rekkjefølgja (barna leikar fritt).
Opning frå fossen eller boknappen viser Oppdagingar, unntatt når `giftsFirst`/`levelGift` gjeld.

- Rutenett per gruppe (Køyre, Leik, Venner, Kvardag, Verda), store bilete. Funne: full farge og
  hake. Ikkje funne: mørk silhuett av same bilete (`ColorFilter`), utan tekst; trykk viser eit
  stort bilete og eventuelt «Prøv»/«Reis dit» der det finst ein eksisterande veg (leike → `tryToy`,
  stad → reise). Låste leiker viser lås og nivå.
- Toppen viser nivå og ein liten foss med same fylling som på kartet.
- Bilete blir henta frå eksisterande teikningar (`ToyPicture`, ting- og køyretøykunst). Ingen rasterbilete.

## 9. Nivå 7 «Eventyrar»

Fire nye `ToyReward` på nivå 7, kvar med primærhandling, figurreaksjon og samanheng med noko som
finst, etter mønsteret for nivå 5–6. Alle finst i Møblar på alle stader, kan lagrast, flyttast og
setjast i papirkorga; figurar og last blir sleppte trygt ved lagring.

1. **Taubane (`PLAY_CABLE_CAR`).** Eitt breitt møbel (om lag 0,9 breitt) med eitt tårn i kvar ende og
   ei line mellom. Barnet flyttar heile taubana som eit vanleg møbel; tårna står fast i høve til
   kvarandre i 1.11 (enklare og tryggare enn to lause delar). Gondolen har éin sitjeplass og plass til
   éin ting (drop-sone). Trykk sender gondolen til den andre enden og tilbake med mjuk fart, etter
   mønsteret i `Attractions.cable` (`Attractions.kt:145-189`); passasjer og last følgjer gjennom
   `seatPoint`. Katt i gondolen held seg for auga. Posisjonen (`angle` 0–1) blir lagra som `toyAngle`.
   Oppdaging `CABLE_CAR`.
2. **Dykkarklokke (`PLAY_DIVING_BELL`).** Etter mønsteret til miniheisen (`PLAY_LIFT`): éin sitjeplass,
   `angle` 0–1 er djupn, trykk byter retning, lagra som `toyAngle`. Fisk sym forbi og ein fisk kysser
   glaset (teikna med `pen.t`). Står klokka over vatn (`Sim.pools`/`PlaceSpec.water`, eller under
   havet), kan ein laus ting i vatnet under klokka bli med opp som last. Elles «tørrdykk»: bobler og
   ein forvirra fisk i ei bøtte. Oppdaging `DIVING_BELL`.
3. **Gravemaskin (`PLAY_DIGGER`).** Køyretøy i `Vehicles` (pilar/stopp som traktoren), éin førar.
   Pilrada får ein ekstra «Grav»-knapp (same mønster som ubåten sine opp/ned). Ute
   (`PlaceId.outdoor`) gir graving ein skatt (mynt, perle eller annan skatt skattekista tek imot,
   jf. byttetabellen i `HouseGardenRules.kt:404-420`) eller av og til ein gammal sokk, lagd ved
   skuffa som ein vanleg laus ting. Ligg det alt åtte eller fleire slike lause skattar på staden, blir
   skuffa tom med ei støvsky. Inne: berre støvsky og ein forundra førar. Oppdaging `DIGGER`.
4. **Skattebord (`PLAY_TREASURE_TABLE`).** Etter mønsteret til byggjebordet (`PLAY_BUILD`, tek tre ting):
   når tre ting ligg på bordet, gir trykk ei overrasking: ein kostymebit (krone, festhatt eller
   blomekrans), ei kvakkande badeand (`DUCK`) eller glitterregn. Tinga på bordet blir verande og kan
   takast av att. Etter ein ny ting kviler bordet i 45 s (berre glitter) så rommet ikkje fyllest.
   Oppdaging `TREASURE_TABLE`.

Alle fire blir lagde til etter `RESCUE` i `ToyReward` med nivå 7, slik at dei går gjennom
`CreativePlay` (tap/drop/step), blir teikna levande, havnar i møbelgruppa PLAY og blir kontrollerte av
`MovedFurnitureArtTest` på alle stader. Gravemaskina sitt trykk må returnere `false` i
`CreativePlay.tap`, så køyringa går vidare til `Vehicles.drive` (som bussen).

## 10. Tullehendingar og troll som tittar fram

`Mischief` i domenet, tikka frå Sim når ein stad er open. Etter 75–120 s med leik (tilfeldig) vel han
éi hending som passar i noverande stad:

- **Nys:** ein synleg figur nys («atsjo»-sky). Har figuren hatt (WORN), dett hatten av og landar
  litt til sida som same ting (lagra). Elles berre nysen.
- **Fugl:** ein liten fugl landar på hovudet til ein figur i 4 s og flyg (berre visuelt).
- **Katt:** ein katt på staden jagar halen sin i 3 s (mellombels animasjon).
- **Troll som tittar fram:** eit lite troll tittar ut frå eit skap/ei kiste/ein garderobe på staden i
  5 s og fnisar. Trykk på trollet før det gøymer seg → `PEEK_TROLL` (første gong) og ein fnisande
  forsvinning.

Reglar: figurar som blir haldne, søv eller køyrer, blir ikkje rørte; ingenting skjer under draging,
open dialog eller ventande gåvepakke; ingen angreoppføring. Redusert rørsle: berre nys (hatten dett
rett ned) og troll. Tullehendingar gir aldri merke, unntatt første fangst av trollet.

## 11. Mailinn

Ny namngjeven barnefigur, **Mailinn**, på same namn i nynorsk og bokmål.

- Bur i Familiehuset (`PlaceId.HOME`) saman med Hedda og Øyvind; står på ledig golv nær dei.
- Blir lagd til **etter** alle andre i nye verder (slutten av `WorldFactory.create`) så id-ar og
  røyster til eksisterande figurar ikkje endrar seg. Røyst frå `Look.voiceFor`, ikkje frå den delte
  `Random(2026)`.
- Gamle lagringar: éingongsoppgradering med flagg `people:mailinn:1` i `WorldStore.decode`, etter
  `TreasureStart.upgrade`. Finst det alt ein FOLK med namnet Mailinn (laga i figurverkstaden), blir
  ingen ny laga.
- Utsjånad (standard, kan endrast i figurverkstaden): barnehøgd 0,9, langt bølgjete hår i
  kastanjebrunt, grøn hettegenser, blå bukse, raude sko, venleg smil. Ingen spesialrolle utover
  å vere ein vanleg, leiken venn; brukaren kan gi fleire detaljar seinare.

## 12. Seinare utgåver (kort)

- **1.12:** nivå 8-leikene i §3; lys gjennom vindauge med støv i strålane, lamper som gløder om
  kvelden, damp og ringar i vatn; partiklar mellomlagra og med fast tak.
- **1.13:** nivå 9-leikene; mjuke lydbilete per stad laga i kode; fossebrusen på kartet aukar med nivå.
- **1.14:** nivå 10-leikene; regnbogefoss og trollfest ved nivå 10; svak glans på leiker som kan
  brukast; djupare skuggar.

Grensene for nivå 8–10 er 25/32/40, justerte etter barnetest.

## 13. Utanfor

Ingen nye stader. Ingen endring av knappar/menyar utover §6–§8 før meir barnetest. Ingen valuta,
nedteljing eller tap. Ingen nettverk. Glimt, oppdrag og eventyr held fram som før.

## 14. Kontroll

- Einingstestar: oppdaging gir eitt merke éin gong; ingen merke under verdsbygging; angre bevarer
  firsts/visited/stickers; lagring/lasting av firsts og visited; gamle lagringar utan felta;
  tilbakeverkande oppgradering éin gong; nye grenser (0…19) og at ingen mister nivå; detektoren for
  kvar Fx-type; Mailinn i ny og gammal verd utan duplikat og med uendra id-ar for andre;
  nivå 7-leikene (handling, passasjer/last, lagring, lager/papirkorg); Mischief-reglane.
- Android-testar: gåvepakka (fall, trykk, plassering, full stad), flygande merke, fossetrykk opnar
  rett fane, nivå 7-leikene med faktisk draging.
- Visuell kontroll på mobil og nettbrett, nynorsk og bokmål, redusert rørsle.

## 15. Risiko

- **For raskt tempo:** 68 oppdagingar + glimt + oppdrag kan gi nivå 7 for fort. Grensene ligg i éi
  liste og blir justerte etter barnetest.
- **Detektor som fyrer feil:** `Fx` er grov; testar per Fx-type og `fixture.type`.
- **Yting på kartet:** fossefyllinga er få primitive figurar per bilete; mål på det trege nettbrettet.
- **Tullehendingar som irriterer:** sjeldne, korte, aldri under draging; kan skruast ned i éin konstant.

## 16. Utgåve 1.12: nivå 8 «Showmeister» og levande scene (del 1)

Godkjent i bolk 3–4; brukaren bad «Ta neste steg» etter publisert 1.11.0 og har bede om at arbeidet ikkje stoppar
ved kvar port. Release først når brukaren ber om det. Barnetest av tempoet i 1.11 står framleis att.

### 16.1 Småting frå gjennomgangen av 1.11 (ferdige først)
Tullehendingar pausar også ved open sekk/leikedialog; hatten dett rett ned ved redusert rørsle; merke frå effektar
utan stad (0, 0) startar midt på skjermen; Mailinn-sjekken skil ikkje store/små bokstavar; overlegget for merke tikkar
berre når noko ventar; steinane ved fossen er trykkbare; bokmål «Sprengte en boble»; dykkarklokka hentar berre ting
som faktisk ligg i vatnet og finn vatnet éin gong per steg; skattebordet stoppar ved åtte lause overraskingar.

### 16.2 Nivå 8 (grense 25 merke), fire leiker i `ShowPlay`
1. **Ekkoboks (`PLAY_ECHO_BOX`).** Høyrer dei siste (høgst åtte) tonane som blir spela innan 1,5 på same stad
   (piano `KEY`, gitar `STRUM`, tromme `DRUM`, xylofon `XYLO`, song `SING`), med tidsavstand. Trykk spelar dei att i
   same rytme med pipestemme (høgare tonehøgd), og vennene i nærleiken dansar ein augeblink. Tom boks: eit lite
   «hæ?»-pip. Opptaket er mellombels (ikkje lagra). Oppdaging `ECHO_BOX` ved første avspeling.
2. **Danseteppe (`PLAY_DANCE_FLOOR`).** Flatt teppe med fargefelt. Trykk slår musikk på/av (same radiomusikk som
   discokula). Medan det er på, dansar alle frie, ståande figurar som står på teppet; to eller fleire saman får eit
   felles hopp på takta. Feltet under kvar dansar lyser. Oppdaging `DANCE_FLOOR` når nokon dansar på det.
3. **Konfettimaskin (`PLAY_CONFETTI`).** Trykk: konfettiregn (partiklar, ikkje ting som må ryddast), «pang»,
   og venner innan 1,0 hoppar og ler. Kviler 2 s mellom skota. Oppdaging `CONFETTI`.
4. **Lysrigg med discokule (`PLAY_LIGHT_RIG`).** Trykk byter mellom av, varmt lyskjegle og fargedisco (lagra i
   `mode`). Disco: same mørklegging og fargeflekkar som discokula, og alle ståande dansar. Lyskjegle: eit varmt kjegle
   frå riggen ned på golvet; figurar i kjegla smiler og vinkar. Oppdaging `LIGHT_RIG` første gong lyset blir slått på.

Nye `First` (72 i alt) med bilete og tekst på begge målformer. Grenser `0, 2, 4, 7, 10, 14, 19, 25`.

### 16.3 Levande scene, del 1
- **Lys gjennom vindauge:** for `WINDOW`-møblar (Familiehuset, kafeen, frisøren, laben) med gardinene oppe, om dagen:
  ei mjuk, lys stripe frå glaset ned på golvet, svakare i regn/snø, borte om natta; 8 støvkorn som dansar i stripa.
  Mitt hus og Storhuset (vindauge i bakgrunnskunsten) kjem i ein seinare runde.
- **Lamper om kvelden:** når natta kjem, går lampene (`LAMP`) på av seg sjølv, og av att om morgonen.
- **Ringar i vatnet:** eit plask lagar ringar som veks og forsvinn; av og til ein roleg ring der det er vatn.
  Høgst 12 ringar.
- **Damp:** eit varmt bad (på) dampar lett.
- Redusert rørsle: lysstriper står stille utan støvdans, ingen sjølvstendige ringar; plask-ringar vert viste kort.
- Yting: få primitivar per stripe, støv som `drawPoints`, ingen nye bitmapar; mål på nettbrettet.

### 16.4 Kontroll
Einingstestar for kvar leike (opptak/avspeling, dans på teppet, konfettikvile, lysmodus og lagring), nye grenser,
lamper som følgjer natta og ringtak; Android-test med ekte draging til danseteppet og trykk på lysriggen;
visuell kontroll på mobil og nettbrett.


## 17. Utgåve 1.13: nivå 9 «Oppfinnar» og lydbilete

Brukaren sa «jobb vidare» etter publisert 1.12.0 og at Mailinn er ei jente (ho har lange krøllar og genser, ingenting i
appen bruker pronomen, så ingen endring trengst). Release først når brukaren ber om det.

### 17.1 Nivå 9 (grense 32 merke), fire leiker i `InventorPlay`
1. **Robotverkstad (`PLAY_ROBOT_WORKSHOP`).** Verkstadbord som tek to ting (som skattebordet, `dropZone`). Trykk med to
   ting: 2,5 s knatring og gneistar, så rullar ein liten **robotkompis** (`ThingType.ROBOT_PAL`, ny, fire fargar) ut. Tinga
   på bordet blir verande. Kviler 45 s; høgst åtte robotkompisar laus per stad. Med færre enn to ting: «hmm»-bank og ein
   gneist. Oppdaging `ROBOT_WORKSHOP` første gong ein robotkompis kjem ut.
2. **Hjelperobot (`PLAY_HELPER_ROBOT`).** Rullande robot med brett. Trykk: han ser etter den næraste lause tingen på golvet
   innan 1,2 til kvar side, rullar dit (`shiftX`), tek han opp på brettet (`Mode.INSIDE`), rullar til næraste figur innan
   rekkjevidde og set han ned ved føtene hennar med «ta-daa» (figuren ler og hoppar). Ingen ting eller ingen figur: robotten
   dansar og pip-pip. Brettlasta blir sleppt ved føtene om roboten står stille utan oppdrag (trygt ved lagring). Oppdaging
   `HELPER_ROBOT` når ein ting er levert.
3. **Rakettsett (`PLAY_ROCKET_KIT`).** Utskytingsrampe med ein liten rakett og ei sitjeplass. Trykk: nedtelling 3-2-1 (1,5 s),
   så stig raketten (`angle` 0–1) med flamme og røyk, snurrar ei stjerneskur på toppen og dalar mjukt ned att med fallskjerm.
   Passasjeren held seg fast («oooh») og ler ved landing. Oppdaging `ROCKET_KIT` ved første oppskyting.
4. **Reaksjonsbane (`PLAY_REACTION_COURSE`).** Flat brett med fire fargefelt. Eit trykk startar; eit felt lyser, og barnet må
   trykke akkurat det feltet på 2,0 s (kortare for kvar rette, ned til 0,9 s). Rett: tone (pentatonisk) og glitter; feil eller
   for sein: ei lita «bonk» og rekkja startar på nytt. Fem rette på rad: konfetti, jubel frå vennene og oppdaging
   `REACTION_COURSE`. Trykket går direkte til feltet (ikkje gjennom leikedialogen). Lyset er sim-tilstand utan lagring.

Nye `First` (76 i alt); grenser `0, 2, 4, 7, 10, 14, 19, 25, 32`; nye `FixtureType`/`ThingType`/`ToyReward` blir lagde til
sist så ordinalane held. Kunst i `ui/art/InventorArt.kt`; tekstar på begge målformer.

### 17.2 Lydbilete per stad
Mjuke, løkkjande lydbilete laga i kode (`audio/Soundscape.kt`): vind, bølgjer, fossebrus, fuglar, siklader, romstille med
klokketikk, murring, drypp, romdrone. Eit eige lag under musikken (same dempa/av-reglar som musikken: sluttar når musikk er
av, lågare ved tale og oppgåver). `Soundscape.bedFor(place, night, onMap)` er ein rein funksjon. **Fossebrusen på kartet aukar
med nivå** (`Soundscape.mapGain(level)`).

### 17.3 Kontroll
Einingstestar for kvar leike, nye grenser og lydbilete (val per stad, lengd, ingen klipping, saumlaus løkke, aukande foss-
styrke); Android-test med ekte trykk på reaksjonsbana og oppskyting; visuell kontroll på mobil og nettbrett.
