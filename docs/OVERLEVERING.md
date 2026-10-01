# Overlevering: kor arbeidet står og kva som skal gjerast

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
| `house/kjeller` | Kjellaren (vaskerom, fyrrom, basseng, festrom, tunnel + Trollhola-dør) | art og tunnel committa; rapport ikkje lesen — sjekk |
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
