# Overlevering: kor arbeidet står og kva som skal gjerast

> **UTGÅVE 1.2.0 PUBLISERT (2026-10-01, Codex):**
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.2.0 er offentleg, stabil og nyaste utgåve.
> Tag `v1.2.0` peikar på `93b46e4994659592922dba85fee8d372e398d5ec`; pakken er `app.trollfoss`,
> versjon 1.2.0 / kode 3. Éin universal APK, 2 943 354 byte, med den opphavlege Trollfoss-signaturen.
> APK SHA-256: `609e82d3fc35d040fa9b355a7a47a915a7768337447252ea3ff060c4a71bed12`.
> Arkiv: `dist/release-v1.2.0/`; GitHub har APK, SHA256SUMS, R8-mapping og SOURCE_COMMIT.
> `testDebugUnitTest` (346 grøne), `lintRelease` og `assembleRelease` via byggjelåsen er grøne.
> Pakke, versjon, ikkje-debuggable og sertifikat er kontrollerte med aapt2/apksigner. Kladden hadde éin APK
> med rett digest og storleik. Etter publisering er API-et kontrollert utan token, og den offentlege APK-en
> lasta ned til eiga fil; hash og signatur er stadfesta like. Ingen signeringsfiler er endra eller publiserte.
> **Ikkje prøvd i denne publiseringsøkta:** oppdatering gjennom appen og installasjon på nettbrett.
> `emulator-5554` var ikkje i gang, og prosjektreglane tillèt berre den eksisterande køyrande emulatoren.
> Den førre publiserte 1.1.0-APK-en vart henta og hashkontrollert i `dist/update-test-v1.2.0/` for seinare test.
> Dei dokumenterte ytingsavgrensingane og oppstartsfrysinga i grafikkemulatoren nedanfor gjeld framleis.

> **NYAST – NETTBRETT OG SAMANHENGANDE LEIK (2026-10-01, Codex):** Arbeidet er framleis lokalt på `main`.
> `PlayViewport` held storleiken på tinga fast når sidepanelet opnar seg, og sentrerer arbeid, sekk og kontrollar
> i den synlege delen. Romoversikt med store møbelsymbol gir direkte romval i Familiehuset, Storhuset og Mitt hus.
> Eit valt enderom held fram som mål for tapet/golv sjølv når kameraet ikkje kan sentrere det heilt.
> Større panel/kort på nettbrett, eksplisitt lukk i møbelpanelet og sekkbrett over botnverktøya.
> Kameraet fangar no eit bilete berre ved fotografering, i staden for å ta opp eit ekstra grafikklag kvar ramme.
>
> `MinePlay` koplar saman eksisterande ting: frø + vatning → bær/blomar; egg + mjølk på kjøkenbenken → deig;
> planke på sagbenken → to pinnar. Deig og bær kan bakast i omnen, og pinnar + hammar blir gitar på arbeidsbenken.
> Råvarer/reiskapar kjem ved trykk også i gamle rom. Delvise blandingar blir lagra, vasskanna blir verande,
> og dei nye møblane verkar også etter flytting gjennom lageret til andre hus. Bilettips og oppskriftsbok er oppdaterte.
>
> **Kontroll:** 346 einingstestar (inkludert heile bake-/gitar-kjedene, lagring midt i ei blanding og flytting av
> kjøkenbenk til Familiehuset). `testDebugUnitTest`, `lintDebug`, `assembleDebug` via `Build-Locked.ps1`.
> UI på `emulator-5554`, 1920 × 1200 / 240 dpi: romval, møbelpanel, faktisk tapetendring av `HOME:0`,
> dra egg/mjølk til bollen → deig og oppskrift lagra; frø og to vatningar → moden plante → jordbær.
> Kamerabilete er henta ut og visuelt stadfesta. Mobil 2400 × 1080 / 420 dpi: byggjepanel og Meir-meny.
> 4:3-nettbrett 1600 × 1200 / 240 dpi avdekte liten kollisjon mellom etasjeknappar og stjerneteljar;
> etasjeknappane er flytta ved sida av kart/oppdrag på nettbrett.
> Bilete ligg i `screenshots/village-after/`, særleg `tablet-dough-made.png`, `tablet-greenhouse-ripe.png`,
> `tablet-wall-applied.png`, `tablet-camera-photo.png` og `phone-polish-*.png`.
>
> **Yting/avgrensing:** Før desse endringane gav kald oppstart rett etter skjermomlegging ein ny FocusEvent-ANR
> kl. 19:03:57. Nye installasjonar og dei oppvarma UI-kontrollane ovanfor gav ingen nye ANR eller krasj.
> Oppvarma kortmåling: Mitt hus-kjøken 6,6–7,3 ms/ramme, Familiehuset 15,5–15,7 ms/ramme. Målet < 12 ms
> er dermed ikkje nådd i alle rom, og ekte nettbrett er ikkje prøvd. Full røyktest og oppdateringsflyt står framleis att.
> **Ved sluttgjenoppretting:** ny FocusEvent-ANR kl. 19:46:10 ved kald start rett etter nullstilling av skjermen.
> Originalen opnar i `MANOR_GROUND`. Sporet frå `dumpsys dropbox --print data_app_anr 2026-10-01 19:46`
> (`.gradle-tmp/last-trollfoss-anr.txt`) viser main ventande i `RenderProxy::setStopped`, og RenderThread i
> `libEGL_emulation` → `qemu_pipe_read` → `glCreateProgram_enc` → Skia-programbygging. Dette peikar på
> grafikkemulatoren ved oppstart; ikkje stadfesta som ei generell apparatfeil eller som løyst. Appen kom vidare,
> og ny prosess-start utan skjermbyte gav ingen ny ANR (`restore-warm-restart.png`). Storhuset var framleis
> tungt i den korte oppvarma målinga, om lag 31–34 ms/ramme. Ikkje framstill dette arbeidet som full ytingsgodkjenning.
> Sluttkontroll etter siste bygg er grøn: `tablet-four-three-builder-final.png` viser knappar utan overlapping,
> rombyte med panelet ope og sekkbrett er kontrollerte (`tablet-four-three-room-selected-final.png`,
> `tablet-four-three-bag-final.png`); `phone-polish-designer-final.png` viser møbelpanelet på mobil.
> Denne økta si opphavlege lagring i `files/tablet-polish-original.json` er sett tilbake med lik SHA-256.
> Testfotoet er teke ut av albumet (kopi ligg i skjermbiletemappa); skjermmål, tettleik og rotasjon er nullstilte.
> Ingen ny publisert APK, push eller commit. Spole-emulatorane er ikkje brukte.

> **LOKALE ENDRINGAR (2026-10-01, Codex):** Brukaren presiserte at kameraet skulle lenger VEKK på nettbrett.
> Minste breidde der er no 2,35 scene-einingar (før utgåva: 2,05; den første lokale 1,85-rettinga var feil retning).
> Det gir om lag 13 % mindre figurar enn utgåva; breie telefonar fyller framleis skjermhøgda. Bakken og skuggen
> under Mitt hus følgjer dei skrå bakre hjørna. Ingen push eller ny publisert utgåve; alt dette ligg lokalt på `main`.
> Himmel- og veggfyll bruker `Stage.backgroundTop` frå skjermhøgda, så zoominga ikkje gir ei mørk stripe øvst.
> Den opphavlege geometrien og plasseringa av ting er uendra; eit nytt testtilfelle dekkjer breitt, 4:3 og kvadratisk format.
>
> Retta ein reprodusert NPE i `MapLive.drawHighlight` når kartet vart opna frå `MINE_GROUND`: innvendige etasjar
> bruker no huset sitt reisemål (`PlaceId.mapPlace`) for ballong, kartposisjon og ring. Mitt hus var dessutan ikkje
> kopla til den ferdige kartteikninga; kartet viser no barnet sitt hus og oppdaterer biletet etter bygging/måling.
> Kartet er 60 % breiare og kan dragast sidelengs; nytt kartikon med brettar, elv og stadmarkør. «Heime» er omdøypt
> til **Familiehuset**, etter brukaren sitt val. **Vagstaddalen** er ein ny stad med laftehytte som kan opnast, svingande
> elv, fiske, bål, katt og tre glimt. Nye `PlaceId` er lagde sist og har eigne fixture-id-ar; gamle lagringar blir utvida.
>
> Mitt hus har no ei open tretrapp og faste Inn/Ut/Oppe/Nede-knappar som bruker dei ekte passasjane. Byggjepanelet
> har namngjevne faner og kort, rettleiing for rom og etasje, synleg lukk/Ferdig, og scroll der det trengst. Eige
> målingsikon opnar veggfargane i tomta; tolv fargar i fire kolonnar passar også på mobil. Møbelverkstaden har sofaikon.
> Viktig Compose-retting: kvar underfane observerer `vm.mineVersion`, elles vart romval/fargeval ståande til fanebyte.
> Mørk `GameText` har ikkje lenger mørk kontur over same fyllfarge; korta er leselege og lange namn kjem heilt fram.
>
> **Kontroll:** 339 einingstestar, `lintDebug` og `assembleDebug` er grøne. På `emulator-5554` i mobilformat
> (2400 × 1080 / 420 dpi) og nettbrettformat (1920 × 1200 / 240 dpi): husmal, bygging av stove/kjøken/etasje,
> romval utan fanebyte, fargeval og markør, Inn/Ut/Oppe/Nede, kart frå Mitt hus-etasjar og Storhuset sin kjellar,
> kartdragging, reise til Vagstaddalen, hyttedører og elv. Ingen krasj i loggen etter desse kontrollane.
> Bilete i `screenshots/village-after/`: `phone-house-choices-final.png`, `phone-enter-rooms-final.png`,
> `phone-floor-requirement-final.png`, `phone-paint-changed-final.png`, `phone-map-upper-final.png`,
> `phone-cabin-open-final.png`, `phone-river-final.png`, og `tablet-*-checked.png`.
> Bakgrunnsfyll er også sett i Mitt hus, Familiehuset, alle Storhuset-etasjane, Romstasjonen, Vagstaddalen og
> Heileberget (`tablet-*-background-final.png`). Ein kald oppstart samstundes med lint gav ein FocusEvent-ANR
> (6:51:43); omstart utan bygging og den oppvarma kontrollen fungerte. `lastanr` hadde same tidspunkt etter
> sluttkontrollen og gjenoppretting. Ikkje bruk dette som ein ytingsgaranti.
> Spole-emulatorane vart ikkje starta. Den opphavlege lagringa er teken vare på i `files/navigation-original.json`;
> testlagringane ligg i eigne app-interne filer. Den opphavlege lagringa er sett tilbake (SHA-256 stadfesta lik),
> og skjermmål, tettleik og rotasjon er nullstilte etter formatkontrollen.
> Full røyktest av alle eldre rom og oppdateringsflyten nedanfor står framleis att. Ytingsmålet < 12 ms er ikkje
> stadfesta; dei tidlegare korte `MINE_YARD`-prøvane under denne økta gav 29–31 ms. Gjer ei eiga oppvarma måling.

> **UTGÅVE: v1.1.0 er publisert (2026-10-01).** Tag `v1.1.0` = commit `f400457`, éin signert APK (2,9 MB, kode 2,
> SHA-256 `2487c913…6f5e`, sertifikat `de170fe9…`), notat i `docs/release-v1.1.0.md`; stadfesta utan token og med
> nedlasta hash. CI køyrer på `main`. **Ikkje gjort:** den ekte oppdateringstesten gjennom appen (bygg ei lokal
> 1.0.0 med `-PtrollfossVersionCode=1 -PtrollfossVersionName=1.0.0`, installer, Kart → tannhjul → reknestykket →
> Appoppdateringar → Sjekk no → last ned og installer; sjå `docs/RELEASE_WORKFLOW.md` §5). Visuell kontroll på ei
> frisk emulator (nye spel, Storstova og Loftet er sett og ser bra ut; resten av romma, natt, nettbrett, yting
> og Mitt hus-bygginga gjenstår) og punkta under «Det som står att» gjeld framleis.
>> **OPPDATERING (nyast, 2026-10-01 ca. 15:00): alle ni greinene `house/*` og `claude/gracious-kapitsa-ff5964`
> (rein tekst i oppdateringsnotatet) er flettet inn i `main`. `main` er grøn: 334 enhetstestar, `lintDebug` og
> `assembleDebug` går gjennom (commit `644a056`, ikkje pusha, ingen release). Tabellen i §3 under viser
> greinene slik dei såg ut før flettinga; bruk han som oppslagsverk for kva kvar branch inneheldt.**
>
> Fletteproblem som er retta (sjå commit «Merge fixes»): same hjelpenamn i ulike filer (`heartPath`, `Opening`),
> ei øydelagd Synth-fletting, samanslåtte debug-ekstra (`MainActivity`/`TrollfossViewModel.debug`: sesong, fest,
> mine, shape, build, cam), duplikate glimt frå union-fletting, `House.hasPassages` omfattar no `place.big` + LAB,
> og nokre testar som antok gamle tal (kjellaren har eigne figurar; draumehuset-testen).
>
> **Det som står att, i rekkjefølgje:** (1) **sjå appen på ein frisk emulator**: telefon-emulatoren var overbelasta
> og viste «Process system isn't responding», så røyktesten gav berre krasj-fri logg, ingen brukbare bilete. Start
> emulatoren på nytt og køyr `scripts\Smoke-Houses.ps1` via `Run-Locked.ps1` (ny lagring + alle åtte nye stader;
> bileta hamnar i `screenshots\smoke\`); sjekk nyspel-starten på tomta, byggjepanelet, kvar etasje dag/natt, trapper,
> nøklar, kartet og kompakt meny. (2) **Yting:** `adb logcat -s TrollfossPerf` (mål < 12 ms/ramme etter oppvarming;
> Kjellaren var 2,3 gonger Heime; sjå §3). (3) Auk `MusicPlayer.VERSION` og `SoundFx.VERSION`. (4) `Thumbs.kt`:
> teikn fixturar med eigen stad (huset sine møblar er «pending»-boksar i miniatyrar). (5) Dokumentasjon/endringslogg/
> roadmap og `docs/release-v1.1.0.md`. (6) Spør brukaren om utgåve 1.1.0 (kode 2; `docs/RELEASE_WORKFLOW.md`).
> Ikkje-starta ting: §5. Mitt hus er skrive men aldri sett på ei eining (se «Status» i `docs/BYGG.md`).

Skrive 2026-10-01 av Claude (Sonnet 5.5) fordi bruksgrensa (vekegrensa) var nesten tom. Les dette fyrst,
deretter `AGENTS.md`, `docs/AI_INSTRUCTIONS.md`, `docs/HUSET.md` og `docs/BYGG.md`. Appen heiter **Trollfoss**
(Kotlin og Jetpack Compose, `C:\topa`, offentleg repo `oyvhov/trollfoss-android`). Brukaren skriv nynorsk og vil
ha eit **storslått, morosamt** barnespel (4–10 år); humor er viktigast.

## 1. Faste reglar (bryt dei ikkje)

- **Publiser aldri** ein release, push eller opprett noko offentleg utan at brukaren seier det i chatten. Siste
  publiserte utgåve er **v1.0.0** (kode 1). Lisens (alle rettar reserverte) og CI-oppdatering er alt pusha til `main`;
  alt anna under er lokalt.
- **Lag aldri ny signeringsnøkkel.** `.signing/trollfoss-release.jks` og `signing.properties` er Git-ignorerte og skal
  aldri i Git, release eller chat.
- Alt barnet ser av tekst finst på nynorsk og bokmål (`Txt(nn, nb)` i `ui/Strings.kt`). Ingen tekst i grafikken. Ingen
  raud kross (grøn plus eller hjarte). Alt er snilt og morosamt, aldri skummelt. All grafikk, musikk og lyd er kode.
- **Éin emulator om gongen** (brukaren bad om det). Berre `emulator-5554` (telefon). For nettbrett: stopp telefonen
  (`adb -s emulator-5554 emu kill`), start nettbrettet med `scripts\Start-TrollfossTablet.ps1`, stopp det etterpå.
  Brukaren har stengt Spole-emulatorane; ikkje start dei.
- Maskina har lite ledig minne: **bygg alltid med** `scripts\Build-Locked.ps1` (éin bygg om gongen, delt daemon) og
  bruk emulatoren berre gjennom `scripts\Run-Locked.ps1 -Name emulator -Command "& <skript>"`.
- **PowerShell-fallgruver:** `R` er eit alias (ikkje bruk det som funksjonsnamn); `` `n `` i vanlege strengar kan
  ete kode; `adb ... > fil.png` i eit skript ødelegg bilete (Windows PowerShell 5 skriv UTF-16): bruk
  `scripts\Screenshot.ps1 -Out <png>`; `Remove-Item` under `C:\topa` er blokkert (bruk `git rm`); skript med æøå må
  lagrast som UTF-8 med BOM; fleirlinjers endringar går best med redigeringsverktøyet. Ein debug-intent til ei
  køyrande app treng `am start -f 0x20000000`. `PlaceId.entries`-rekkjefølgja må aldri endrast (lagringar).
- Spar bruk: ikkje les store filer om att, bygg i omgangar (`-Tasks ':app:compileDebugKotlin'` er billigast).

## 2. Status på `main` (lokalt, ikkje pusha)

Commitar etter v1.0.0 (sjå `git log --oneline`):
- Lisens og nye GitHub Actions-versjonar (pusha).
- **Storhuset-grunnmur:** fem stader `MANOR_GROUND/UPPER/ATTIC/CELLAR/GARDEN` (`PlaceId.manor`, `big`), eigne
  fixtur-id-blokker (`idBase`, 300 per stad), passasjar mellom etasjar (`domain/House.kt`: `Passage`, `Floor`,
  `FloorRules`, `House.shuffle`), gylne nøklar og `world.flags` (`Sim.flag`, `HouseKeys`), mørke etasjar med
  fingeren som lommelykt (`Floor.darkness`), nøkkel-HUD (`ui/screens/HouseHud.kt`), ankerblokker i delte filer,
  stubbar for kvar etasje, `docs/HUSET.md` (designguida og kontrakten).
- **Sesong-grunnmur:** `domain/Seasons.kt` (årstid frå dato, påske/jul/gresskar), val i foreldresida, `Pen.season`/
  `festival`, fargeskjær, blad/blomar/lysfluger, vinter-snø, krokar `ui/art/SeasonArt.kt`.
- **Kompakte kontrollar på telefon** (`PlayScreen.kt`: høgd < 520 dp gir små knappar, éin «Meir»-meny, liten sekk
  via `Engine.compact`). Verifisert på emulator.
- **Mitt hus-grunnmur** (`domain/Mine.kt`, `PlaceId.MINE_YARD/GROUND/UPPER`, `World.mine`, lagring) og `docs/BYGG.md`.
- Hjelpeskript: `scripts\Build-Locked.ps1`, `Run-Locked.ps1`, `Screenshot.ps1`, `Merge-House.ps1`.
- Tester og lint var grøne på `main` før dei ni greinene blei laga. Ein kjend regel: antal glimt er `>= 60` og
  «minst tre per stad» (ikkje nøyaktig 45/3).

## 3. Hjelpar-greiner (arbeidet som ikkje er flettet inn enno)

Kvar ligg som git-branch `house/<namn>` og som worktree `C:\trollfoss-wt\<namn>`. Køyr `git branch -v`,
`git log main..house/<namn> --oneline` og `git -C C:\trollfoss-wt\<namn> status` for live status.

| Greine | Kva | Status (2026-10-01 kl. 14:20) |
| --- | --- | --- |
| `house/stova` | Storstova (59 møblar, 6 rom, Sofie-nøkkel, 6 glimt, 5 oppdrag, 17 lydar, 41 `GR_`-typar) | ferdig, `main` flettet inn, testar grøne; ikkje sett om natta/tablet |
| `house/oppe` | Andre høgda (55 møblar, 6 rom, leiketog, dokkehus, ballbasseng, garderobe, nøkkel i badet, 3 glimt, 4 oppdrag) | ferdig, 111 testar og lint grøne; balkong og natt aldri sett |
| `house/hage` | Hagen (40 møblar, dam med ekte vatn, frosk-kor, drivhus, hagenissar, nøkkel via kompost, 6 glimt, 4 oppdrag) | ferdig, 120 testar og lint grøne; tretopphytta og froskekoret ikkje sett |
| `house/loft` | Loftet (35 møblar, 4 rom, Sture-åtferd og gøym-og-leit, drakt-kista, grammofon, tårn, hemmeleg rom, nøkkel hos Sture, 7 glimt, 5 oppdrag, 8 `AT_`-lydar) | ferdig, `main` flettet inn, 34 testar + alt grønt; ingen bilete av mørke/lommelykt, `AT_`-møblar er pending-boksar i panel-miniatyrar |
| `house/kjeller` | Kjellaren (48 møblar/29 `CE_`-typar, 5 rom, tunnel + Trollhola-dør med veg tilbake, sokkemonster med nøkkelen, 3 glimt, 4 oppdrag, 12 `CE_`-lydar, ny musikk; rør `Places.kt` (dør sist i LAB), `House.kt` (`hasPassages`, LAB), `Decor.kt`, `Sim.kt` éi linje) | ferdig, testar og lint grøne; tunnelturen aldri sett; **tung: ca. 2,3 gonger Heime (`thing`, STAIRCASE, SAUNA, CE_VALVE/BULB/BOILER/CLOTHESLINE), må optimaliserast** |
| `house/figurar` | Roboten Rolf og spøkelset Sture: art (`PersonArtRolf/Sture/House/Xray.kt`), åtferd (`domain/Figurar.kt`), 12 `FG_`-lydar, `Fx.FIGURAR`, `Give.SNIFF`, debug `FigurarSheetActivity` | ferdig, 17 testar + lint grøne; **rører `Engine.kt`, `Sim.kt`, `Life.kt`, `Anatomy.kt` (flett tidleg og sjekk konfliktar)**; ikkje merga `main` inn |
| `house/kart` | Storhuset på kartet (`ui/art/MapManor.kt`, tunnel-sti via `manor_tunnel`, spot (0.76, 0.355)) | ferdig, `main` flettet inn, testar grøne |
| `house/sesong` | Sesongar og høgtider i dei 15 gamle stadene (`SeasonKit.kt` palett, `SeasonArt*.kt`, debug-ekstra `--es season/festival`; jul, påske, gresskar) | ferdig, testar og lint grøne; sommar skal vere uendra (ikkje pikselsjekka); rør `PlayScreen.kt`, `TrollfossViewModel.kt`, `MainActivity.kt`, `SceneKit.kt` og stadkunsten; rain/natt/feiringar om natta og ytinga ikkje sett; storhuset og kartet ikkje med |
| `house/bygg` | **Mitt hus**: byggjemotor, 10 romtypar, 23 `MI_`-møblar, fasade/tomt/rom-teikning, byggjepanel, effektar, demo, `MineTest`, kartlandemerke (`MapMine.kt`) | mykje skrive (5 700 liner), **aldri sett på ei eining**; sjå statusdelen i `docs/BYGG.md` på greina |

Dei fire fyrste rapportane (stova, oppe, hage, kart) står oppsummerte over; rapportar frå loft, kjeller og figurar
kom ikkje før eg måtte skrive dette. Les commit-meldingane og `git diff --stat main...house/<namn>`.

**Kjende hol frå rapportane:** ingen måling av `TrollfossPerf` (mål < 12 ms/ramme etter oppvarming; emulatoren var
overbelasta, 65–175 ms var støy); få eller ingen nattbilete og ingen nettbrett-test av dei nye stadene; oppgåve-
miniatyrar i `ui/screens/Thumbs.kt` teiknar fixturar med `PlaceId.HOME` slik at huset sine møblar blir «pending»-boksar
i oppgåvekort (bruk fixturen sin eigen stad); `MusicPlayer.VERSION` og `SoundFx.VERSION` må aukast så nye lydar og
omskrivne musikkoppskrifter blir rendra på nytt hos dei som har cache; kartetiketten heiter «Storhuset» medan
stadnamnet i `S.place` er «Storstova»; `DesignerTest` og `WorldTest` måtte justerast (oppgåvedekk, glimt-tal).

## 4. Anbefalt rekkjefølgje (billigast først)

1. `powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Merge-House.ps1` (set opp union-fletting for
   delte listefiler, flettar `figurar, stova, oppe, loft, kjeller, hage, kart, sesong, bygg` éin om gongen og stoppar
   på konflikt). Fiks konfliktar for hand (typisk `Places.kt`, `Sim.kt`, `Engine.kt`, `TrollfossViewModel.kt`,
   `PlayScreen.kt`, `Strings.kt`, `Secrets.kt`; ta begge sidene). **Flett `bygg` sist**: han endrar `PlayScreen`,
   `Engine`, `ViewModel` og `Sim` meir enn dei andre.
2. `Build-Locked.ps1 -Tasks ':app:testDebugUnitTest',':app:lintDebug',':app:assembleDebug'`. Rett kompileringsfeil,
   duplikat og manglande komma etter union-flettinga, og testar som antek gamle tal.
3. Auk `MusicPlayer.VERSION` og `SoundFx.VERSION`. Fiks `Thumbs.kt` (stad per fixtur).
4. Sjå på telefon-emulatoren (`--es place manor_ground|manor_upper|manor_attic|manor_cellar|manor_garden|mine_yard|
   mine_ground|mine_upper`, `--es night on|off`, `--es weather SUN|RAIN|SNOW`, `--es screen map`): kvart rom, dag/natt,
   éi handling per rom, trappa og rutsjebanane, nøkkelen, kartet, byggjepanelet i `mine_yard` (`--es mine demo` fyller
   eit hus). Sjekk `adb logcat -s TrollfossPerf` og korriger tunge bakgrunnar (cache per `u`). Test éin gong på
   nettbrett (éin emulator om gongen).
5. Oppdater `README.md`, `docs/DESIGN.md`, `CHANGELOG.md`, `ROADMAP.md` (nye stader: 5 Storhus + 3 Mitt hus; talet glimt
   er no ca. 60+; sesongar; kompakte kontrollar; lisens). Legg til `docs/release-v1.1.0.md` (**fyrste linjene som
   vanleg tekst**: appen viser notatet som rå tekst) og ta inn rettinga for oppdateringspanelet frå den andre økta:
   `git merge claude/gracious-kapitsa-ff5964` (`update/ReleaseNotes.kt`).
6. **Spør brukaren** om utgåve. Då: versjon 1.1.0, kode 2 i `app/build.gradle.kts`, følg `docs/RELEASE_WORKFLOW.md`
   (bygg test-utgåva `-PtrollfossVersionCode=1 -PtrollfossVersionName=1.0.0`, installer, test oppdateringa i appen:
   fyrste ekte test, sidan 1.0.0 hadde kode 1). Social preview og andre GitHub-ting er brukaren sine.

## 5. Det som ikkje er starta (etter 5. oktober, når vekegrensa er nullstilt)

- **Historie-systemet «Det skjer noko i bygda»:** kjeder av enkle oppdrag på tvers av stader med morosam utbetaling
  (døme: Rumle har mista kronen; ei geit har rømt frå Heileberget; husmysteriet med Sture og dei fem nøklane; Rolf si
  store husfest der vener frå bygda flyttar midlertidig inn). Idé: `domain/Stories.kt` med `Story`(steg = `Deed` +
  stad + bilete), ei kort-rad i `TasksScreen`, «!»-markør over ein figur som gir neste steg, lagring i `World`.
- **Garderobe og nye figurar/dyr:** fleire klede, hårfrisyrar og drakter (`Styles.TOPS` osv. i `domain/People.kt`,
  `ui/art/PersonArt.kt`), nye dyr (til dømes rev, piggsvin, ekorn), kostyme som kjem ut av loftkista.
- Resten av sesong-grafikken (kartet i alle årstider, fleire stader), fleire husting og meir verdsbygging (fleire
  tomter og bygningar etter Mitt hus).

## 6. Bruk og grenser

Ved handoff var vekegrensa ca. 90 % brukt (nullstillast 5. oktober kl. 00:00 UTC) og 5-timarsgrensa ca. 67 % (nullstillast
ca. 16:20 UTC). Ni parallelle agentar brukar mykje; køyr få om gongen. Kvar agent (Agent-verktøyet) får ein eigen
`git worktree`, byggjer med `Build-Locked.ps1` og får dei same reglane som i §1. Les rapportane (kort) i staden
for å lese alle filene dei laga.
