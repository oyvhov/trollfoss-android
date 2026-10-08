# Overlevering: kor arbeidet står og kva som skal gjerast

> **NYAST – HEMMELEGE FOSSAR (2026-10-08, Codex):** Brukaren bad om hemmelege plassar i den store
> kartfossen og fossen inne på ein stad. Begge er no inngangar til den same eksisterande Trollhola;
> den indre fossen er tolka som skogsfossen. Eit lite glimt i vatnet viser trykkstaden. Vassgardina
> inne i hola har ei pil ut til skogen. Kartskilt/landemerke er fjerna, boka skjuler hola til første
> besøk, og reisehint dit før oppdaginga leier til fossen. Gamle besøk, figurar, ting og Storhus-tunnelen
> er bevarte. Same grein `codex/rumles-skattar`, same arbeidskopi og versjon 1.15.0 / kode 20.
> **Kontroll:** 634 JVM-testar og 89 Android-testar på kvar av mobil/nettbrett grøne. Kart, skogsfoss
> og utgang er visuelt gjennomgåtte. Faktiske mobiltrykk gjekk gjennom kartfoss → hola → skogen →
> hola, og lagringa bevarer spelarane 25/45. Oppstarts-ANR og UI-hierarkiproblem på den delte emulatoren
> står framleis att; dette er ikkje ein friskmeld release. Sjå full kontrollstatus og lintresultat i
> `docs/superpowers/plans/2026-10-08-hemmelege-fossar.md`. Arkiv `C:/topa/dist/hemmelege-fossar/`.
> Testpakkane er fjerna, innstillingane tilbakeførte og emulatoren ikkje stoppa. Ingen push/publisering.

> **NYAST – RUMLE SINE SKATTAR / 1.15 LOKALT (2026-10-08, Codex):** Vidareført etter
> «Jobb vidare med spennande ting», frå `af7a987` (lokal 1.14). Grein `codex/rumles-skattar` i
> `C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa`, ASCII-kopling `C:/topa/.gradle-tmp/rainbow-finale`.
> Versjon 1.15.0 / kode 20. Ingen push eller publisering.
> **Gjort:** frivillig skattejakt frå starten, tre ekte biletspor ved ugla, sandslottet og krystallane,
> valfri rekkjefølgje og éi trollykt som premie. Lykta gir stjerner, fisk og troll, i handa og på golvet,
> lyser opp rom om natta og blir heilt roleg med redusert rørsle. Kortet finn att flytta eller pakka
> spor og den same lykta. Lagring og angring bevarer framgangen; to nye oppdagingar, 82 i alt.
> **Kontroll:** 631 JVM-testar og 84 Android-testar på kvar av mobil/nettbrett grøne; lint 0 feil / 32 åtvaringar.
> Siste APK, test-APK og loggar: `C:/topa/dist/rumles-skattar/`. 14 scenebilete frå spelmotoren er laga;
> dei nye teikningane er visuelt gjennomgåtte. Opphavlege sceneplasseringar er prøvde med fingertrykk.
> **Står att før release:** oppstart og full menyflyt på roleg eiga AVD/fysisk eining. Den delte
> emulatoren gir framleis ANR ved oppstart, også før jakta blir opna. Mobil/bokmål starta jakta via
> ekte knappar etter «Wait»; heile menyutforminga i begge målformer er ikkje friskmeld. Årsaka til
> oppstartsproblemet er ikkje fastslått. Ingen barnetest, menneskeleg lytting, GitHub-CI eller signert
> oppdateringsprøve. Full rapport: `docs/superpowers/plans/2026-10-08-rumles-skattar.md`.
> Testpakkane er fjerna og skjerminnstillingane tilbakeførte; den delte emulatoren er ikkje stoppa.
> **Vidare:** bruk `codex/rumles-skattar`; greina inneheld òg alt frå lokal 1.13 og 1.14.

> **NYAST – NIVÅ 10 OG 1.14 IMPLEMENTERT LOKALT (2026-10-08, Codex):** Vidareført etter «Jobb vidare»,
> frå lokal 1.13 (`claude/niva-9`, b75554a). Grein `codex/niva-10` i
> `C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa`; ASCII-kopling
> `C:/topa/.gradle-tmp/rainbow-finale`. Versjon 1.14.0 / kode 19. Ingen publisering.
> **Gjort:** nivå 10 ved 40 merke, drakevogn med to plassar og boblenys, luftskip med to passasjerar
> og éi last, gøymetroll utan tidsfrist og ein ni sekund lang trollfest. Fire nye oppdagingar (80 i alt).
> Regnbogefoss og småtroll på kartet, regnbogevatn i boka, roleg glans og skuggar under leikene.
> Stilleståande nye leiker bruker teiknebufferen. Begge målformer og redusert rørsle er tekne vare på.
> **Kontroll:** 622 JVM-testar og 77 Android-testar på kvar av mobil/nettbrett grøne; lint 0 feil / 32 åtvaringar.
> Debug- og test-APK er bygde og arkiverte i `C:/topa/dist/niva-10/`. Faktiske køyreknappar,
> to opphavlege spelarar, klede, last og lagring er prøvde. Full rapport og status for siste bygg/lint:
> `docs/superpowers/plans/2026-10-08-niva-10-1-14.md`. Bilete/loggar i `C:/topa/screenshots/niva-10/`.
> **Står att før release:** rein kaldstartkontroll på roleg eiga AVD eller fysisk eining. Den delte
> emulatoren gav tidvis «svarar ikkje», også med den uendra 1.13-APK-en. Sporet viser venting i Android
> si teikning; årsaka er ikkje fastslått. Ingen GitHub-CI, signert oppdateringsprøve eller barnetest i
> denne runden. Lyd frå 1.13 er framleis ikkje høyrd av menneske. Sjå rapporten før eventuell release.
> **Vidare arbeid:** bruk denne greina, som inkluderer den lokale 1.13-kjelda. Rotarbeidskopien
> `C:/topa` på `codex/magic-rest` har eldre uferdige endringar; berre ein peikar i overleveringa er lagd til der.

> **NYAST – 1.13.0 FERDIG LOKALT, IKKJE PUBLISERT (2026-10-07, Claude):** Brukaren sa «Det er ei jente, og ja jobb vidare»
> (Mailinn er ei jente; ho har lange krøllar og genser og appen nemner ingen pronomen, så ingenting trengde endring).
> Grein `claude/niva-9` i `C:/topa/.claude/worktrees/oppdagingar` (frå main e0fdcc6). Spesifikasjon §17, plan
> `docs/superpowers/plans/2026-10-07-niva-9-1-13.md`. Versjon 1.13.0 / kode 18.
> **Gjort:** nivå 9 «Oppfinnar» ved 32 merke med `InventorPlay`: robotverkstad (to ting inn, ein `ROBOT_PAL`-robotkompis ut,
> tinga blir verande, 45 s kvile, høgst åtte per stad), hjelperobot (hentar næraste lause ting i sitt eige rom og leverer til
> ein venn; ei last barnet legg på brettet blir liggande til neste trykk), rakettsett (nedtelling, stig 0,38, passasjer via
> `seatPoint`, fyrverkeri på toppen, mjuk landing) og reaksjonsbane (fire felt, trykk direkte på felta, fem rette gir fest;
> dialogens Bruk trykkjer lyst felt for dei som ikkje kan sikte). Fire nye oppdagingar (76). Lydbilete per stad
> (`audio/Soundscape.kt`, `SoundscapePlayer`): vind, bølgjer, foss, fuglar/sirissar, romstille med klokke, murring, drypp,
> romdrone, hav; fossebrusen på kartet veks med nivå (`mapGain`). Same av-bryter som musikken.
> **Kontroll:** 614 einingstestar, 71 Android-testar (mobil og nettbrett, éi AVD) og lint (0 feil / 32 åtvaringar) grøne.
> Visuelt prøvd: rakettoppskyting (flamme, røyk, fyrverkeri), reaksjonsfelt som lyser, verkstad, robot og rampe i skogen.
> Debug: `--es toys inventor` set ut leikene og ein ball. Fersk gjennomgang (Opus) fann ti reelle feil som er retta med testar:
> ting på bordet/brettet vart ikkje teikna (`VISIBLE_INSIDE`), feil felt i skrå 3D (`padAt` tek omsyn til djupna), to ting på
> brettet, tilstand som følgde med ein gammal leike-id (no `WeakHashMap` på fixture-objektet), flamme på ein rakett lasta
> midt i flukta, last som hoppa tilbake til startpunktet, tilgjenge for reaksjonsbana, tonar som svulma i romdrona og
> fade som heldt igjen pause.
> **Ikkje gjort:** barnetest; lyden er ikkje høyrd av menneske (berre rekna og målt: lengd, klipping, saum); robotens
> henting er testa i eining og ikkje sett på skjerm (nivåpakka la seg over scena). Release berre når brukaren ber om det.
> **Utsett (småting):** hjelperoboten stoppar ikkje for golvmøblar (som støvsugaren gjer); ingen nedtoning av lydbileta ved tale
> (ingenting dempar musikken heller); Mailinn sin utsjånad er framleis standard.

> **NYAST – PUBLISERT 1.12.0 (2026-10-07, Claude):** Brukaren sa «Ja, fiks og release» etter statusen for 1.12.
> Lysriggen tek no berre trykk der han er teikna (`ShowPlay.hitSpan`, -0,1..0,15; test i eining og med ekte trykk), og
> `claude/niva-8` er spolt fram til `main` og publisert som stabil, nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.12.0. Kjelde/tag: `5de0bc59f7f5de44ab6610b54552d269f9a39eac`.
> Éin universal APK, 3 434 911 byte, SHA-256 `00227284543069e7cc04836f981ac0a113010b5aaca8d9c457e6df60879685b7`.
> Opphavleg sertifikat (`de170fe9…70fe9f3`), app.trollfoss, 1.12.0 / kode 17, min Android 26, ikkje debuggable.
> Mapping, SHA256SUMS og SOURCE_COMMIT er vedlagde. Offentleg liste utan token viser v1.12.0 først (kladd: nei,
> prerelease: nei), digest stemmer, og nedlasta APK har same hash som den bygde. Signert release-bygg, 592
> einingstestar, 66 Android-testar (mobil og nettbrett) og release-lint grøne (0 feil / 23 åtvaringar).
> **Ekte oppdatering** på eiga `TrollfossRelease180` (mobilformat): publisert 1.11.0 / 16 → 1.12.0 / 17 gjennom
> foreldresida (7 × 7-sperra, «Sjekk no» fann utgåva sjølv) → Last ned → Installer → Android «Update» → Play Protect
> (Scan app, «This app looks safe») → «App installed». Kaldstart viser kartet med Trollfossen; Hedda og Alva er
> framleis spelarar i Familiehuset. Ingen krasj i krasjbufferen. Testpakkane er borte, emulatoren er stoppa og
> kopiane av signeringsfilene i arbeidskopien er fjerna. Arkiv: `dist/release-v1.12.0/` (Git-ignorert); bilete i
> `screenshots/niva-8/` (Git-ignorert). Rotarbeidskopien `C:	opa` (codex/magic-rest) er urørt.
> **Utsett (småting):** ekko-tonehøgd (to øvste tonar smeltar saman), ringar ved redusert rørsle, felt under kvar
> dansar og høgd på teppet, små allokeringar per bilete, namnet «discokule» mot «Diskokule», taklampa i stova lyser
> ikkje om natta (berre `LAMP`), danseteppet er tynt som bilete i leikedialogen. **Står att:** 1.13 nivå 9 + lyd,
> 1.14 nivå 10 + regnbogefinale (spesifikasjon §12), Mailinn sin utsjånad (brukaren gav aldri detaljar) og barnetest av tempo.

> **1.12.0 – BYGGJEHISTORIE (2026-10-07, Claude):** Brukaren bad «Ta neste steg» etter 1.11.0.
> Grein `claude/niva-8` i `C:\topa\.claude\worktrees\oppdagingar` (frå main 48516a4). Spesifikasjon §16, plan
> `docs/superpowers/plans/2026-10-06-niva-8-1-12.md`. Versjon 1.12.0 / kode 17.
> **Gjort:** småtinga frå gjennomgangen av 1.11 (med testar); nivå 8 ved 25 merke med `ShowPlay` (ekkoboks som tek opp
> tonar i nærleiken og spelar dei att med pipestemme, danseteppe med dans og felles hopp, konfettimaskin med kvile,
> lysrigg av/scenelys/disco lagra i `mode`); fire nye oppdagingar (72); levande scene del 1 (lysstriper med støv frå
> `WINDOW`, lamper som følgjer natta, ringar i vatn via `Ripples`, damp frå varmt bad).
> **Kontroll:** 591 einingstestar grøne; debug-, test-APK og lint (0 feil / 32 åtvaringar) gjennom låsen. Fersk
> gjennomgang (Opus): animasjonar som aldri gjekk over (òg eldre leiker sidan 1.7), felles musikkbrytar og tynt treff på
> danseteppet er retta med testar. Utsett: ekko-tonehøgd, ringar ved redusert rørsle, felt under kvar dansar, høgd på
> teppet, små allokeringar per bilete, namnet «discokule».
> **Android og visuelt (7. oktober, etter omstart av maskina):** 66 Android-testar grøne på mobil (2400 × 1080 /
> 420 dpi) og nettbrett (1920 × 1200 / 240 dpi), eiga AVD `TrollfossRelease180`, pakke `app.trollfoss.oppdag`. Ny
> `SceneLightTest` teiknar vindaugslys (av om natta), scenelyset frå lysriggen og ringar i vatn; alle tre feilar når
> lyset er teke bort. Visuelt prøvd i stova: lysrigg av/scenelys/disco, konfetti med hopp og merke, danseteppe med
> dans, nivågåve, vindaugslys med støv, natt med lampe, skumbad med damp og ringar ved bryggja. Danseteppet er
> teikna om til ei flat scene i skrå-3D (4 × 2 ruter i sjakkmønster). Debug: `--es toys show` set nivå 8-leikene ut.
> Ingen Trollfoss-krasj. Testpakken er borte, skjermmål/rotasjon tilbakeførte, emulatoren stoppa, produksjon 1.11.0
> urørt. Bilete: Git-ignorert `screenshots/niva-8/`. Nye småting: lysriggen sin trykkflate tek trykk frå pianoet når
> han står inntil; taklampa i stova lyser ikkje om natta (berre `LAMP`); danseteppet er tynt som bilete i dialogen
> (som teppa). Klar for fletting/release når brukaren seier ja.

> **NYAST – PUBLISERT 1.11.0 (2026-10-06, Claude):** Brukaren bad «Flette inn og commit og push. Lag ny release».
> `claude/oppdagingar` er spolt fram til `main` (frå e3e5e02) og publisert som stabil, nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.11.0. Kjelde/tag: `fe9f5d8662d33aa98ff54c26b20c1a72cabcdf15`.
> Éin universal APK, 3 418 527 byte, SHA-256 `4508453c71114d89a66f4a2d7feb86eda4d6fecc11bc9eb9ad7fb3e08cabd715`.
> Opphavleg sertifikat (`de170fe9…70fe9f3`), app.trollfoss, 1.11.0 / kode 16, min Android 26, ikkje debuggable.
> Mapping, SHA256SUMS og SOURCE_COMMIT er vedlagde; alle fire storleikar/digest stemmer med arkivet
> `dist/release-v1.11.0/` i arbeidskopien. Offentleg release-liste/latest og APK-nedlasting utan token er stadfesta.
> Signert release-bygg, 573 einingstestar og release-lint grøne (0 feil / 23 åtvaringar). GitHub main (37481534285)
> og tag (37481534629) grøne. **Ekte oppdatering** på eiga `TrollfossRelease180`: publisert 1.9.0 / 14 → 1.11.0 / 16
> gjennom foreldresida (appen fann utgåva sjølv) → Last ned → Installer → Android Update → Play Protect «This app looks
> safe» → «App installed». Kaldstart viser kartet med Trollfossen; Hedda og Alva er framleis spelarar, og Mailinn er
> lagd til i den gamle verda. Ingen krasj i krasjbufferen. Emulatoren er stoppa; kopiane av signeringsfilene i
> arbeidskopien er fjerna. Bilete: Git-ignorert `screenshots/release-v1.11.0/`. Rotarbeidskopien `C:\topa`
> (codex/magic-rest) er urørt.

> **NYAST – 1.11.0 «OPPDAGINGAR» FERDIG LOKALT, IKKJE PUBLISERT (2026-10-06, Claude):** Brukaren har testa med
> barna: dei leikar mest fritt, og merke/nivå kom for seint. Bestilt: nivå 7–10 og eit designløft (veg og
> belønning › humor › levande scene › stil) og Mailinn som ny figur. Spesifikasjon:
> `docs/superpowers/specs/2026-10-06-oppdagingar-design.md`; plan: `docs/superpowers/plans/2026-10-06-oppdagingar-1-11.md`.
> Grein `claude/oppdagingar` i `C:\topa\.claude\worktrees\oppdagingar`, frå `origin/main` e3e5e02. Versjon 1.11.0 / kode 16.
> **Gjort (1.11):** 68 oppdagingsmerke frå fri leik (`Firsts.kt`, detektor i `Sim.Relay` + eksplisitte kall),
> grenser 0/2/4/7/10/14/19, tilbakeverkande merke for lagra tilstand (`firsts:retro:1`), flygande merke med jubel,
> fana Oppdagingar (først i boka, silhuettar), Trollfossen på kartet (glød, ti steinar, gåve, troll), gåvepakke i
> fallskjerm (`ToyPlay.openGift`), nivå 7 (taubane, dykkarklokke med arm ut over vatnet, gravemaskin med Grav-knapp,
> skattebord), tullehendingar (nys, fugl, katt, troll som tittar fram; av ved redusert rørsle) og Mailinn i
> Familiehuset (`Residents.kt`, `people:mailinn:1`, ingen endra id-ar/røyster for andre).
> **Kontroll:** 573 JVM-testar og 61 Android-testar grøne (nye: ekte draging til taubane, gravemaskin, trollfangst) på mobil (2400 × 1080 / 420 dpi) og nettbrett
> (1920 × 1200 / 240 dpi), eiga AVD `TrollfossRelease180`, pakke `app.trollfoss.oppdag`. Lint 0 feil / 31 åtvaringar.
> Visuelt: spelarval med Mailinn, kart med foss/steinar/troll, første reise → flygande merke → nivå 2 → gåvepakke →
> boblemaskin i stova, Oppdagingar-fana med silhuettar, Gåver med nivå 7-teikningane. Bilete: Git-ignorert
> `screenshots/oppdagingar/`. Testpakkane er avinstallerte, skjermmål tilbakeførte og emulatoren stoppa;
> produksjonsappen på AVD-en er urørt. **Ikkje prøvd:** bokmål visuelt (strengtestar dekkjer begge), nivå 7-leikene
> i ein faktisk scene (berre einings-/teiknetestar og miniatyrar), tullehendingar på skjerm, fysisk eining, barnetest.
> **Står att:** 1.12 nivå 8 + lys/partiklar, 1.13 nivå 9 + lyd, 1.14 nivå 10 + regnbogefinale (spesifikasjonen §12).
> Mailinn sin utsjånad er ein standard brukaren kan endre. Ein fersk kodegjennomgang (Opus) fann fem feil som er retta med
> testar. Utsette småting: tullehendingar pausar ikkje ved open sekk/leikedialog; hatten hoppar til sida også ved redusert
> rørsle; TIDY-merket startar i (0,0); FirstPops tikkar alltid; dykkarklokka kallar sim.pools() fleire gonger per bilete og kan
> plukke opp ting på land; skattebordet manglar tak per stad; Mailinn-sjekken skil store/små bokstavar; steinane ved fossen er
> ikkje trykkbare; bokmål «Sprakk en boble» → «Sprengte en boble». Push/PR/release berre når brukaren ber om det.

> **NYAST – 1.10.0 PUBLISERT (2026-10-04, Codex):** Brukaren bad om å
> gjennomføre tilrådingane frå den vedlagde designguiden. Rein arbeidskopi frå `origin/main`
> (7b32c87), grein `codex/playful-polish`, i
> `C:\Users\Øyvind\.codex\worktrees\playful-polish\topa`. Nunito/Fredoka er pakka lokalt med
> lisensar, tekstomriss og kartskilt er rolegare, spelar/angre har kremtone, og figurval, portrett
> og kortvarige scenemerke deler spelarfarge og nummer. Portrett har minst 48 dp høg trykkflate.
> Støvsugaren tek ein katt eller person som passasjer, startar ved slepp på setet, snur ved møblar,
> pausar når han blir løfta og tek vare på forskyving og passasjer ved lagring. Gamle lagringar
> er kompatible. Nye samlesystem, skjulte troll, vedmating og levande miniatyrar er utsette.
> 522 JVM-testar og 58 Android-testar grøne på både mobil og nettbrett; debug-lint 0 feil / 31
> åtvaringar. Nynorsk, bokmål og rørsle av er kontrollerte visuelt, og katten er prøvd med faktiske
> skjermdrag. Skjermbilete/loggar i `screenshots/playful-polish/`, eiga prøve-APK i
> `dist/playful-polish/`. Sjå `docs/PLAYFUL_POLISH.md` for detaljar, hash og avgrensingar.
> Brukaren bad deretter om mildare namneboksar på karta og ein kjapp release utan alle testar på nytt.
> Kartskilta har no tynnare kant, svakare skugge og pastell på vald stad; kontrollert på mobil og nettbrett.
> Signert 1.10.0 / kode 15 er bygd med opphavleg nøkkel og installert over 1.9.0 på eiga AVD.
> Kaldstart viser kartet og dei to bevarte spelarane. APK: 3 385 767 byte, SHA-256
> `b00df28e045315e23be92457317a491ae63d4fb0a377e57da92e213b9cde2524`.
> Arkiv: `dist/release-v1.10.0/`. Pakke, signatur, versjon og ikkje-debuggable er kontrollerte.
> Publisert som nyaste stabile release: https://github.com/oyvhov/trollfoss-android/releases/tag/v1.10.0.
> Kjelde/tag: `4f73eb330474bd4a8b1556268f3a9c3b2dc900ff`. Alle fire vedlegg har rett storleik og digest;
> offentleg release-liste/latest og APK-nedlasting utan token er stadfesta. Hedda og Alva er framleis valde.
> Ingen ny full testkøyring eller prøve av oppdateringsflyten gjennom foreldresida i denne kjappe runden.
> Prøveappen er fjerna, emulatorinnstillingar tilbakeførte og eiga AVD stoppa; produksjon og prøveverd er bevarte.
> Private verdener, signeringsnøkkel og gammalt ukommittert arbeid i `C:\topa` er urørte.

> **NYAST – PUBLISERT 1.9.0 (2026-10-04, Codex):** Brukaren bad «Release» etter kart-/sekk-/menyendringa.
> PR 6 er fletta, og stabil 1.9.0 / kode 14 er publisert som nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.9.0.
> Inneheld det nye kartet, sekk med 12 plassar og biletkategoriar i høgremenyen som er skildra under.
> Sluttgjennomgangen retta dessutan tilbakeføring til gamle store sekkar, reserverte plassar medan
> ein annan finger dreg ut eit ting, eingongsopning av lageret, skjermlesarpiler og nøyaktige vatntreff.
> Kjelde/tag: `a677eead12dd68050d841d43f9fe4e8785604252`. Bygd frå
> `92603f64ea0d99a3090cfbeecc6f09e6206c3a4b`, med identisk Git-tre etter fletting.
> Éin universal APK, 3 156 438 byte, SHA-256:
> `8e1e8da21f6521b987ca1f7d5851d57017da98abc079d9dbe02db17c2e319175`.
> Opphavleg Trollfoss-sertifikat, app.trollfoss, min Android 26, ikkje debuggable. Mapping, hash og
> SOURCE_COMMIT er vedlagde. Alle fire filstorleikar og digest i kladden er kontrollerte; offentleg
> release-liste, latest og nedlasting av alle fire filer utan token stemmer med det lokale arkivet.
> Signert release-bygg, 519 einingstestar og 56 Android-testar grøne; release-lint 0 feil / 22 åtvaringar.
> GitHub kjelde/PR (37222398796 / 37222402001), main (37222983734) og tag (37223027754) er grøne.
> Ekte oppdatering på eiga `TrollfossRelease180`-prøveeining: publisert 1.8.0 / 13 → 1.9.0 / 14 gjennom
> foreldresida → Sjekk no → Last ned oppdatering → Installer oppdatering → Android Update.
> Appen stadfesta nedlastinga, Play Protect gav «This app looks safe», og Android gav «App installed».
> Installert versjon/kode er kontrollert; ingen avinstallering eller tømming mellom utgåvene.
> Heile spelarvalskjermen har identiske pikslar før/etter: Hedda og Alva er valde med same hår og klede.
> Den pakka gitaren og dei gamle møblane er bevarte; glasfrontkista står framleis i soverommet.
> Release-APK er prøvd med kaldstart, kart utan X, dag/natt, regn/snø/sol, sidepil og møbelkategoriar.
> Ein stol vart faktisk dregen frå katalogen og lagt på lager; sekken hadde framleis berre gitaren.
> Mobilprøva etter ny kaldstart viste stor gitar i sekken og éin stol på separat lager, med brukbare menyval.
> Nettbrett 1920 × 1200 / 240 dpi og mobil 2400 × 1080 / 420 dpi: éin eigen AVD i to format.
> Ingen produksjonskrasj eller ANR i dei bevarte loggane frå denne emulatoroppstarten.
> Ingen prøve på fysisk eining eller barnetest. Dei grundigare native featureprøvene står under.
> Bilete/XML/loggar: Git-ignorert `screenshots/release-v1.9.0/`; arkiv: `dist/release-v1.9.0/` i
> `C:\topa\.claude\worktrees\legg-bort`. Grein `codex/release-1.9.0`; featuregreina er bevart.
> Prøvepakkane er avinstallerte, eigen emulator stoppa og skjerm-/rotasjons-/animasjonsinnstillingar
> tilbakeførte. Produksjonsappen 1.9.0 og prøveverda er bevarte på den eigne AVD-en.
> Private spelverdener og gammalt ukommittert arbeid i `C:\topa` er urørte. Runde to ventar på barnetest.

> **NYAST – KART, SEKK OG HØGREMENY (2026-10-04, Codex):** Brukaren melde feil på kartet og i
> sekken/høgremenyen etter 1.8.0, og bad om eit meir spennande kart med animasjonar.
> Ferdig lokalt på `codex/map-and-play-menu` i `C:\topa\.claude\worktrees\legg-bort`.
> Kartet er hovudsida utan X eller retur til eit gammalt rom. Dag/natt og vêr har eigne knappar;
> same stad kan opnast att. Runde, farga stadskilt svarar på trykk og markerer staden med ein liten
> ballong. Sidepiler blar i kartet, vatn svarar med sprut og andre ledige område med farga glimt.
> Reiseballongen legg att eit kort spor. Faste treffområde, høgst seks samtidige trykkeffektar og
> den eksisterande mellomlagra bakgrunnen er bevarte. Redusert rørsle gir straksreise utan ekstra effektar.
> Sekken tek imot høgst 12 ting/figurar og viser seks store bilete per side. Full sekk avviser utan tap;
> gamle, større sekkar og tilbakeførte byggjedelar blir bevarte. Møblar går berre til det separate
> møbellageret; lagerkassa tel møblane. Opne sekkesider ligg over mobilknappane, så begge piler kan brukast.
> Høgremenyen har ein rullbar ikonrad, biletkategoriar, tilgjengelege møblar først, mjuke kategoriskifte
> og markert førehandsvising under drag. Lageret viser kva som nett vart lagt bort; avbroten draging
> legg ikkje til noko og tek ikkje noko frå lageret. Nynorsk og bokmål er på plass.
> **Kontroll:** 517 einingstestar og 51 Android-testar grøne, debug-APK og test-APK bygde. Lint: 0 feil /
> 31 åtvaringar. Nye Android-kontrollar dekkjer to fingrar om siste sekkplass, møblar ved full sekk,
> avbroten katalogdrag og mobilpaging/uthenting frå siste side av ein gammal sekk med 37 ting.
> **Native prøve:** dag/natt/regn/snø, stadopning og retur, sidepiler, faktisk sprut/glimt/ballongreise,
> møbelkategoriar, loddrett blading og faktisk katalogdrag på mobil og nettbrett. Full 12-sekk avviste
> den trettande ballen utan tap. Alle 37 originale prøve-ID-ar vart bevarte etter paging, kaldstart og
> reiser; ein stol frå mobilkatalogen gjekk til møbellageret med sekken uendra. Bokmål vart valt gjennom
> den faktiske foreldreporten. Reise, gjenopning av same stad, Android-tilbake og sidepiler vart også
> prøvde med Android-animasjonar avslått. Ingen krasj/ANR for prøvepakken i dei bevarte emulatorloggane.
> Nettbrett 1920 × 1200 / 240 dpi og mobil 2400 × 1080 / 420 dpi: éin eigen `TrollfossRelease180`-AVD
> i to format, eiga pakke `app.trollfoss.menus` og kontrollert prøveverd. Ingen fysisk prøve eller barnetest.
> Bilete/XML og film: Git-ignorert `screenshots/map-and-menu/`. Prøve-APK: `dist/map-and-menu/`.
> Prøvepakkane er avinstallerte, eigen emulator stoppa og skjerm-/rotasjons-/animasjonsinnstillingar
> tilbakeførte. Produksjonspakken og private spelverdener er urørte. Ingen push, PR, ny release eller
> versjonsauke i denne runden; publisert 1.8.0 under inneheld ikkje desse endringane.

> **NYAST – PUBLISERT 1.8.0 (2026-10-04, Codex):** Brukaren bad om ny release etter ferdig plan.
> PR 5 er fletta, og stabil 1.8.0 / kode 13 er publisert som nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.8.0.
> Kjelde/tag: `726408c316473486a7aea3d0819803619ab8ddf5`. Bygd frå `e59ba34733819de94f7a93c818a478fdf4f7de50`,
> med identisk Git-tre etter fletting. Éin universal APK, 3 156 438 byte, SHA-256:
> `5136184240a9a3fbb5df54fbf01397abd6224c61b9f9e71c7e7c516e2a678613`.
> Opphavleg Trollfoss-sertifikat, app.trollfoss, min Android 26, ikkje debuggable. Mapping, hash og
> SOURCE_COMMIT er vedlagde. Alle fire filstorleikar og digest i kladden er kontrollerte; offentleg
> release-liste, latest og nedlasting av alle fire filer utan token stemmer med det lokale arkivet.
> Signert release-bygg og 511 einingstestar grøne, release-lint 0 feil / 22 åtvaringar. Dei 47 Android-testane
> og dei grundige visuelle prøvene var grøne på spelkjelda før versjonsauken, sjå avsnittet under.
> GitHub kjelde/PR (37184180502 / 37184183603), main (37184681295) og tag (37184684185) er grøne.
> Ekte oppdatering på fersk, eiga `TrollfossRelease180`-prøveeining: publisert 1.7.1 / 12 → 1.8.0 / 13
> gjennom Kart → foreldreport → Sjekk no → Last ned → Installer oppdatering → Android Update.
> Appen verifiserte nedlastinga; Play Protect gav «This app looks safe», Android gav «App installed»,
> og installert versjon/kode er stadfesta. Ingen avinstallering eller tømming mellom utgåvene.
> Etter kaldstart er Hedda og Alva framleis valde, med same hår og klede. Flyttinga av Hedda i stova
> og dei gamle møblane er visuelt bevarte. Den nye glasfrontkista står i soverommet etter oppdateringa.
> Nettbrett 1920 × 1200 / 240 dpi var hovudprøva; mobil 2400 × 1080 / 420 dpi fekk kaldstart,
> kart, Familiehuset, spelarar, Meir-menyen og løft av kista med den store lagerkassa i hjørnet.
> Andre kaldstart gav framleis berre éi gåvekiste. Dette er éin eigen AVD i to skjermformat.
> Ingen krasj eller ANR vart registrert under denne release-prøva. Ingen prøve på fysisk eining eller barnetest.
> Bilete/XML: Git-ignorert `screenshots/release-v1.8.0/`; arkiv: `dist/release-v1.8.0/` i arbeidskopien
> `C:\topa\.claude\worktrees\legg-bort`. Arbeidet ligg på `codex/release-1.8.0`; featuregreina er bevart.
> Eigen emulator er stoppa; skjermmål, tettleik og rotasjonsinnstillingar er tilbakeførte.
> Private spelverdener og gammalt ukommittert arbeid i `C:\topa` er urørte. Runde to ventar framleis på barnetest.

> **ARBEIDET FØR RELEASE – LEGG BORT OG SKATTAR (2026-10-04, Codex):** Alle ni oppgåvene
> i `docs/superpowers/plans/2026-10-03-legg-bort-og-skattar.md` er ferdige. Grein `claude/legg-bort` i
> `C:\topa\.claude\worktrees\legg-bort`, frå publisert 1.7.1. Spec: `docs/superpowers/specs/`.
> Sekken veks og viser open sekk eller kasse ved løft; ting og figurar går i sekken og møblar på lager.
> Ny gratis skattekiste med glasfront på alle stader, med éi startgåve i Familiehuset. Lokket svarar på
> nærleik, skattane ligg på hyller, kvar femte får glitter, og ei kiste med innhald kan ikkje lagrast.
> Diamantar, myntar og perler blir ikkje automatisk sletta; loftskista gir berre ledige funn og hostar
> støv med ein møll når ho er tom. Trykkbare trappeopningar, korte hint ved bomtrykk, automatisk lukking
> av møbelpanelet etter to flyttingar, og like lagermøblar viste som eitt kort med tal er også ferdige.
> **Kontroll:** 511 einingstestar og 47 Android-testar grøne, begge debug-APK-ar bygde; lint 0 feil / 31
> åtvaringar. Fersk sluttgjennomgang fann fire feil som er retta med testar som først feila og så vart grøne:
> panelet ventar på andre pågåande fingerrørsler; synleg ope kistelokk tek imot trykk og langt trykk;
> skattar ligg på hylla også utan tyngdekraft og under vatn; synlege kantar av trappehol kan trykkjast på.
> **Visuell prøve:** faktisk løft, slepp, pakking av ball/figur/møbel, uthenting av figur, grupperte lagerkort
> med uthenting/papirkorg/tilbakeføring, kamerafølging utanfor hjørnet og stillstand inni, fem skattar bak glas,
> avvist lagring av fylt kiste, bomtrykk-hint, panelet etter éin og to flyttingar, trappene hall–oppe–loft og
> kjellar–hall, loftskistestøv og bevarte funn, bokmål gjennom foreldresida og «Skattekiste» i katalogen.
> Kaldstart bevarte kista, plasseringa, dei fem originale diamantane og lageret. Mobil fekk den korte
> prøva av løft/pakking, stol til lager, automatisk lokk/glitter/glas og Storhuset via kartet og døropninga.
> Prøvene brukte eiga pakke `app.trollfoss.leggbort` og ei kontrollert, mellombels prøveverd; private verdener
> vart ikkje lesne, endra eller tømde. Bilete/lagringskontroll: Git-ignorert `screenshots/legg-bort/`.
> Gode kontrollbilete er mellom anna 12–24, 45–53, 57–61, 64, 67–68, 72–77, 87–89 og 93–98.
> **Avgrensingar:** den separate JellyBin-emulatoren kunne ikkje nåast. Eigen native `Tunet_Ascii` vart brukt
> i begge skjermformat: nettbrett 1920 × 1200 / 240 dpi og mobil 2400 × 1080 / 420 dpi. Dette er éin AVD
> med to skjermformat. Éin oppstart gav ANR på vindaugsfokus medan slutt-lint køyrde; årsaka er ikkje
> fastslått. Etter omstart var kontrollane og seinare kaldstart responsive. Ingen ytelsesprøve på fysisk eining.
> Testpakken er avinstallert, eigen emulator stoppa og skjermmål, tettleik og rotasjonsinnstillingar tilbakeførte.
> Mellombels planlogg i Git-ignorert `.superpowers/sdd/2026-10-03-legg-bort-og-skattar/` ligg att:
> automatisk godkjenningskontroll avviste slettinga med «blocked by policy», også med kontrollert, eksakt sti.
> **Ikkje gjort før release-oppdraget over:** ingen push, PR, release, versjonsendring eller barnetest. Runde to
> (knappar, menyar, symbol) ventar til brukaren har sett barnet bruke dette.
> `C:\topa` står framleis på `codex/magic-rest` med gammalt, ukommittert arbeid som er urørt.

> **NYAST – PUBLISERT 1.7.1 (2026-10-03, Codex):** Brukaren bad «Lag ny release».
> PR 4 er fletta, og stabil 1.7.1 / kode 12 er publisert som nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.7.1.
> Kjelde/tag: `008ef08321c16ddda4091ce1e55f9b495ccc3ff2`. Bygd frå `f74a213fdac149e335966275d24713808efd4c45`,
> med identisk Git-tre etter fletting. Éin universal APK, 3 140 054 byte, SHA-256:
> `ea3776e816a3f56d27643d24aea145f2a78826bd5b4a8f9c98e77adfcb60fb92`.
> Opphavleg sertifikat, app.trollfoss, min Android 26, ikkje debuggable. Mapping, hash og
> SOURCE_COMMIT er vedlagde. Alle fire GitHub-storleikar/digest og offentleg latest/nedlasting utan token er kontrollerte.
> Signert release-bygg, 467 einingstestar og release-lint grøne (0 feil / 22 åtvaringar).
> Dei 32 Android-kontrollane var grøne på spelkjelda før versjonsauken. GitHub kjelde/PR
> (37134011847 / 37134013619), main (37134617762) og tag (37134621542) er grøne.
> Ekte oppdatering på eiga Release160-prøveeining: 1.7.0 / 11 → 1.7.1 / 12 gjennom foreldresida,
> nedlasting/verifisering, Installer oppdatering, Android Update og Play Protect. Skanning gav
> «This app looks safe», Android gav «App installed», og installert kode 12 er stadfesta.
> Ingen avinstallering eller tømming. Kaldstart opnar Kartet. Hedda og Alva er framleis valde med
> same hår, utsjånad og klede; dei nye figurkorta er visuelt kontrollerte. Nettbrett var hovudprøva,
> mobil fekk berre kort kaldstart-/kartkontroll. Før oppdateringa gav 1.7.0 og fleire systemappar
> ANR under tung emulatoroppstart; prøveverda vart ikkje nullstilt. Ingen nye ANR eller krasj er
> registrerte under kontrollen av den installerte 1.7.1. Bilete/XML: `screenshots/release-v1.7.1/`.
> Arkiv: `C:/topa/dist/release-v1.7.1/`. Eigen emulator er stoppa med skjermmål/rotasjon tilbakeførte.
> Hovudarbeidskopien og private verdener er urørte. Ingen ny signeringsnøkkel.
> Neste designprioritet står i DESIGN_REVIEW_2026-10-03.md; dør-/traktorarbeidet står i INTERACTION_FIXES_2026-10-03.md.

> **NYAST – DØRER OG TRAKTOR PÅ SCENA (2026-10-03):** Opne skapdører og traktortaket låg
> utanfor dei gamle treffområda. Teikning og trykk bruker no delte geometridata; ting bak
> tek ikkje trykk frå traktoren/døra framfor. Mat, førar og figurar framfor er framleis dragbare.
> Sjå `INTERACTION_FIXES_2026-10-03.md`. 467 JVM- og 32 Android-kontrollar grøne, debug-bygg
> og lint grøne (0 feil / 31 åtvaringar). Faktisk flytting via Lager og taktrykk prøvd på Scena.
> Same reine arbeidskopi og PR 4 som designarbeidet; ingen ny APK-release.

> **NYAST – DESIGNGJENNOMGANG OG FIGURMENYAR (2026-10-03):** Brukaren bad om betre design.
> Fersk gjennomgang av kart, Familiehuset, spelarval, figurverkstad og gåver er dokumentert i
> `DESIGN_REVIEW_2026-10-03.md`. Spelarvalet har større figurar, rein tekst, rolege kortrammer,
> tydeleg lagmarkering og tomt-lag-hint. Verkstaden har lys spegelramme og same valfargar.
> Arbeidet held fram på `codex/map-full-height` i den reine arbeidskopien; PR 4 omfattar også dette.
> Debug-bygg og 467 einingstestar grøne; endeleg lint/CI står i PR-en. Nettbrett visuelt kontrollert,
> berre kort kontroll av dei to endra skjermane på mobil. Ingen ny release eller endra lagringsformat.
> Neste designprioritet: kartnamn/stadmarkør, rolegare romknappar, neste gåve synleg før samlinga.

> **NYAST – KART UTAN BOTNFELT (2026-10-03):** Brukaren melde at den mørke rada under
> kartknappane tok for mykje plass på mobil. `codex/map-full-height` fjernar den faste 88 dp-rada.
> Kartet fyller heile skjermen; knappane ligg over kartet, med 56 dp knappar og 8 dp kant på korte
> skjermar. Stadnamna held avstand til den målte knapperada. Dei flytta kystnamna har eigne 48 dp
> treffområde som ikkje strekkjer seg opp i dalen. Sjå PR-en for endeleg bygg- og skjermkontroll.
> Endringa er ikkje med i publisert 1.7.0. Arbeid i den reine arbeidskopien `C:/topa/.gradle-tmp/follow-worktree`.

> **NYAST – PUBLISERT 1.7.0 (2026-10-03, Codex):** Brukaren bad «Release».
> PR 3 er fletta til main og publisert som stabil, nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.7.0.
> Tag/kjelde: `b397b828980d4c712bee0abff89d7856e3d5496e`, versjon 1.7.0 / kode 11.
> Bygd frå `10516afc1df548dc6fdd9de2db8f843320783c34`; samanfletta kjelde har identisk Git-tre.
> Éin universal APK, 3 140 054 byte, SHA-256:
> `e4cf5672402a8736e47dc835797478aa89107c5351268342e761c0c0cff64424`.
> Opphavleg sertifikat, app.trollfoss, min Android 26, ikkje debuggable. Mapping, hash og
> SOURCE_COMMIT er vedlagde. Alle fire digest/storleikar og offentleg latest/nedlasting utan token er kontrollerte.
> Arkiv: `C:/topa/dist/release-v1.7.0/`. Release-bygg, 466 einingstestar og release-lint grøne
> (0 feil / 22 åtvaringar). Dei 26 Android-kontrollane var grøne på spelkjelda før versjonsauken.
> GitHub kjelde (37125637288), PR (37125639853), main (37126187074) og tag (37126190511) er grøne.
> Ekte oppdatering på eiga Release160-prøveeining: 1.6.1 / 10 → 1.7.0 / 11 via foreldresida,
> Sjekk no, nedlasting/verifisering, Installer oppdatering, Android Update og Play Protect-skanning.
> Play Protect gav «This app looks safe», Android gav «App installed», og installert kode 11 er stadfesta.
> Ingen avinstallering eller tømming. Kartet opna ved kaldstart. Dei valde Hedda-/Alva-korta før/etter
> har identiske pikslar (namn, utsjånad og klede). Skyøya og faktisk retur til Familiehuset fungerer.
> Nettbrett 1920 × 1200 / 240 dpi var hovudprøva; mobil 2400 × 1080 / 420 dpi fekk berre kort
> kaldstart-/Kart-kontroll. Ingen Trollfoss-krasj i krasjloggen. Bilete/XML: `screenshots/release-v1.7.0/`.
> Skjermmål/rotasjon er tilbakeførte og den eigne emulatoren er stoppa. Private verdener og
> den skitne hovudarbeidskopien `C:/topa` er urørte. Ingen ny signeringsnøkkel er laga.
> Nivå 1–6 og grunninnhaldet for dei 25 løfta er no utgitt. Nivå 7–10, vidare historiebonusar,
> barnetest og full TalkBack-/brytar-/lydkontroll står framleis att.

> **NYAST – SPELEFLYT OG MAGISK LEIK LOKALT KONTROLLERTE (2026-10-03):**
> Brukaren bad om å gjennomføre gjennomgangen og gjere heile opplevinga vakker og spennande.
> Arbeid skjer på `codex/child-flow-polish` i `C:/topa/.gradle-tmp/follow-worktree`.
> Det tidlegare uferdige `magic-rest`-arbeidet er teke inn som kjelde, fullført vidare og testa her.
> Hovudarbeidskopien på `C:/topa` er urørt. Ingen ny release er publisert i denne runden.
> Sjå `FLOW_POLISH_2026-10-03.md` for F01–F17, nye nivå 4–6 / 20 leiker og kontrollstatus.
> Endeleg debug-bygg, 466 einingstestar og lint er grøne (0 feil / 31 åtvaringar).
> 26 Android-kontrollar grøne, med nye kontrollar av band-/festdans og ny figur i spegelen. Teikne-/dialogendringar er
> visuelt stadfesta: runde vindauge held innhaldet innanfor ramma, og begge riv-knappane er heile på mobil.
> Nettbrett er hovudprøva; mobil er avgrensa til foreldrelås, rom/riving, gåver og spelarval.
> Bevarte IDar, utsjånad, hus og kunst er samanlikna etter reise, lagring og ny installasjon av testpakken.
> Sjekk den tilhøyrande PR-en for GitHub-kontroll og flettestatus. Versjon og signeringsnøkkel er uendra.
> PR: https://github.com/oyvhov/trollfoss-android/pull/3. Sju ekstra funn i ny leik er retta;
> mellom anna pausar i ballongen, tilgang til magnet/skei og kartretur utan flytting av laget.
> Nivå 7–10 er framtidsplan; ikkje påstå at desse er ferdige. Barnetest og full TalkBack-prøve står att.

> **NYAST – GJENNOMGANG AV SPELEFLYT (2026-10-03, Codex):** Brukaren bad om heile flyten og feil/friksjon for barn.
> Rapport: `docs/AUDIT_FLOW_2026-10-03.md`, 18 hovudsteg, 17 prioriterte funn med konkrete rettingar og godkjenningskrav.
> Vurdert publisert 1.6.1-kjelde i eiga fersk `app.trollfoss.flowaudit`-verd. To ekte oppdrag gav nivå 2;
> eit heilt Stjernenatt-eventyr, bygging av rom/andre etasje, lager/papirkorg/tilbakehenting, tunnel/trapper,
> foto, foreldreside og kaldstart vart prøvde. Alle 24 stader visuelt inspiserte; 14 brukte debug-reisesnarveg.
> Nettbrett 1920 × 1200 / 240 dpi, berre kort kontroll av tre ulike mobilflater. Ikkje alle ting/oppgåver,
> to samtidige fingrar, lyd eller TalkBack prøvde. Ingen produktkode endra eller ny release i denne runden.
> **Rett først:** F01 skjulte spelarar bak hytte/møblar/folk; F02 Prøv boblemaskin melder fullt i ferskt
> Familiehus medan drag frå Møblar fungerer; F03 Riv-knappen utanfor riv-dialogen (Row/BigButton/GameText).
> Deretter F04 husoversikt/Bygg her, F05 knapp/golvrot, F06 hjelp/rommarkering, F09 spelarrolle ved pakking,
> F10 hint/tomme flater og F12 Gåver-fane/symbol. Høgare nivå enn 3 finst ikkje i den publiserte kjelda.
> Kaldstart til Kartet, bokretur til Kartet, bord frå Papirkorg og radio frå Lager til Stranda fungerte.
> Spelar-IDar/utsjånader/hus/merke er samanlikna og bevarte. Gamle private spelverder er urørte.
> Bilete/XML: Git-ignorert `screenshots/audit-2026-10-03/`; 67 er oppstartsillustrasjon med forelda XML,
> 95 viser Kartet trass filnamnet. Den eigne emulatoren er stoppa og skjermmål/rotasjon er tilbakeførte.
> Rein rapportgren: `codex/child-flow-audit` i `C:\topa\.gradle-tmp\follow-worktree`.
> Dei uferdige appendringane i hovudarbeidskopien på `codex/magic-rest` er ikkje vurderte som utgitt innhald.

> **NYAST – PUBLISERT 1.6.1 (2026-10-03, Codex):** Brukaren bad «Lag en release etterpå».
> Mjuk spelarfølgje og mindre golvrot er fletta til main og publiserte som stabil, nyaste release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.6.1
> Kjelde/tag: `ca0454e03b602365a5ec6e0ad91050c7b3683c98`, versjon 1.6.1 / kode 10.
> Bygd frå `244e3170a928ff3c209539686da3101af991bece`; samanfletta kjelde har identisk Git-tre.
> Éin universal APK, 3 074 518 byte. SHA-256:
> `5f1508d677e06763e2099b8ef9a6f7894e638f66e0efb8c58498c8de2a567e9f`.
> Same opphavlege sertifikat, app.trollfoss, min Android 26, ikkje debuggable. Mapping, hash og
> SOURCE_COMMIT er vedlagde. Alle fire vedlegg sine digest/storleikar og offentleg nedlasting er
> kontrollerte; offentleg release-liste/latest og APK vart henta utan token. Arkiv: `dist/release-v1.6.1/`.
> Release-bygg/test/lint grøne: 455 einingstestar, 0 lint-feil / 22 åtvaringar. Dei 23 Android-testane
> og den lengre følgje-/lagringsprøva var grøne på spelkjelda før den reine versjonsauken.
> GitHub kjelde (37112058187), PR 2 (37112219697), main (37112753267) og tag (37112756290) grøne.
> Ekte oppdatering på eiga Release160-prøveeining: publisert 1.6.0 / 9 → 1.6.1 / 10 gjennom
> foreldresida, Sjekk no, nedlasting/verifisering, Installer oppdatering og Android Update.
> Første forsøk vart avbrote då prøveopninga kom medan Play Protect-dialogen var open. Eit nytt
> Installer/Update-forsøk gav «App installed», og installert pakke vart stadfesta til kode 10.
> Ingen avinstallering eller tømming. Kartet opna ved kaldstart. Hedda og Alva var framleis valde;
> spelarvalskjermen før/etter har identiske pikselbilete (utsjånad og klede bevarte).
> Nettbrett 1920 × 1200 / 240 dpi: faktisk sveip frå stove til kjøken i Familiehuset, begge gjekk med.
> Mobil 2400 × 1080 / 420 dpi: berre kort oppstart-/Kart-kontroll. Ingen Trollfoss-krasj i krasjloggen.
> Bilete: Git-ignorert `screenshots/release-v1.6.1/`. Skjermmål/rotasjon er tilbakeførte og emulatoren
> vi starta er stoppa. Den private Tunet_Ascii-eininga og den opphavlege debug-verda er urørte.
> **Restarbeid:** Dei uferdige Community/CreativePlay/cloud/nivå-endringane i `C:\topa` på
> `codex/magic-rest` er ikkje med i 1.6.1 og er framleis ikkje fullt validerte. Ikkje påstå at alle 25
> løft eller høgare nivå er ferdige. Den reine release-kjelda ligg i den administrerte arbeidskopien
> `C:\topa\.gradle-tmp\follow-worktree`; der står no berre ein dokumentasjonsgren etter release.

> **NYAST – MJUK SPELARFØLGJE OG MINDRE GOLVROT (2026-10-03, Codex):**
> Brukaren bad om betre flyt når fleire spelarar blir med kameraet, mindre smårot i romma og lite
> mobiltesting. Ferdige endringar på `codex/smooth-player-follow`, bygd isolert frå `main`.
> Arbeidskopi: `C:\topa\.gradle-tmp\follow-worktree` (kopling til eit administrert Codex-worktree).
> `PlayerFollow` erstattar gruppeflytting etter panorering med ei gåtur medan barnet sveipar:
> mjuk akselerasjon/brems, avgrensa steg, stabil rekkjefølgje og separate plassar. Romval gir same
> gåtur; små sveip lèt synlege figurar stå. Også draging av ein spelar langs skjermkanten får vennene
> til å gå med. Ein halden figur blir ikkje stolen, og hindrar ikkje den andre i å gå med.
> Sovande figurar og køyretøypassasjerar blir verande; ubygde rom er ingen snarveg. På fast golv går
> figurane mjukt fram i golvbandet, framfor stolar og sofaer. Stegrytme og utslag avtek ved stopp.
> Valde spelarar vandrar ikkje på eiga hand etterpå; ønskje og reaksjonar er framleis aktive.
> Same figur-ID-ar, klede og handhaldne ting blir bevarte; ingen kopiar eller ny ankomst under sveip.
> `StarterLayout` samlar urørte småting frå innandørsoppsettet i eksisterande skap/kasser og på
> hyller. Det blir ikkje sletta noko, fulle flater blir ikkje fylte vidare, og nye heimar blir lagra så
> ryddeknappen bruker dei. Éin oppgraderingsrunde med `layout:small-things:1`; flytta, haldne, brukte,
> pakka, borne og heimlause nye ting samt Mitt hus blir bevarte. Nye stader får det ryddigare oppsettet.
> **Kontroll:** 455 einingstestar og 23 Android-testar grøne på siste kjelde. Debug-APK,
> Android-test-APK og lint grøne gjennom byggelåsen: 0 lint-feil / 31 åtvaringar.
> Eiga prøvepakke `app.trollfoss.follow`, nettbrett 1920 × 1200 / 240 dpi: faktisk sveip med Hedda 25
> og Alva 45, mjuk gåtur, framfor møblane, avstand ca. 0,28, lagring og verkeleg omstart utan endra
> posisjon/utsjånad. Video og bilete: Git-ignorert `screenshots/follow/`. Prøveverda viser m.a. to
> klossar i leikekassa, leikebil i kista på loftet, sokk ved kleskorga og skiftenøkkel på arbeidsbenken.
> Android-testen tek den same klossen ut att frå eit ope skap med dei verkelege gripefunksjonane.
> Mobil 2400 × 1080 / 420 dpi fekk berre kort oppstart-/skjermkontroll. Ingen krasj eller ANR.
> Skjermmål og rotasjon er tilbakeførte; emulatoren vi starta er stoppa. Produksjonspakken og den
> opphavlege debug-verda er urørte. Ingen ny APK-release i denne runden; versjon framleis 1.6.0 / 9.
> **Pågåande magi:** `C:\topa` er framleis på `codex/magic-rest` med uferdige, ikkje-committa
> Community/CreativePlay/cloud/nivå-endringar frå før. Følgje/rydding er òg kopiert inn der.
> Magiforsøket har ikkje grøn full validering; ikkje publiser det eller påstå at resten av dei 25 løfta
> er ferdige. Fullfør og kontroller desse endringane før den tidlegare bestilte neste store releasen.

> **NYAST – PUBLISERT 1.6.0 (2026-10-02, Codex):** Brukaren bad «Publiser».
> Begge magirundane er fletta til main og publiserte som stabil, nyaste GitHub-release:
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.6.0
> Kjelde/tag: `e55e9be60c58531ded8402ca39c81bd46ab8a22b`, versjon 1.6.0 / kode 9.
> Éin universal APK, 3 074 518 byte. SHA-256:
> `addffa2a277cd5da0af471d089a5b4952c01ddb57e579eb465fec062e46d8a5f`.
> Same opphavlege sertifikat, app.trollfoss, min Android 26, ikkje debuggable. Mapping, hash og
> SOURCE_COMMIT er vedlagde. Offentleg release-liste og nedlasta APK er kontrollerte utan token.
> Release-bygg/test/lint er grøne: 442 einingstestar, 0 lint-feil / 21 åtvaringar. Dei 18 Android-testane
> var grøne på kjelda før versjonsauken. GitHub main (37064858594) og tag (37065446570) grøne.
> Ekte oppdatering gjennom foreldresida: publisert 1.5.0 / kode 8 → 1.6.0 / kode 9, utan avinstallering.
> Nedlasting, verifisering, løyve frå Android og Update fungerte på ei eiga ny Release160-prøveeining.
> Hedda og Alva vart valde i 1.5.0 og var framleis spelarar etter oppdateringa. Appen opna Kartet.
> Mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi er kontrollerte: Kart, spelarval,
> oppdragsbok, gåvekatalog og nye møbelbilete. Lukking av boka frå Kart gjekk tilbake til Kart.
> Den eldre Tunet_Ascii-prøveeininga (456 MB ledig) fekk STORAGE ved nedlasting, også etter omstart
> og rydding av cache. Årsaka er ikkje stadfesta; ny prøveeining hadde ikkje feilen. Ingen privat verd
> vart lesen eller tømd. Tunet_Ascii er framleis på 1.5.0; diskbackup før prøva finst Git-ignorert i
> `.gradle-tmp/release-1.6.0-original/`. Skjermmål/rotasjon vart tilbakeførte før ho vart stoppa.
> Release160 hadde ingen Trollfoss-krasj eller ANR; skjermmål vart nullstilte og emulatoren stoppa.
> Det står framleis att 15 av 25 løft og nivå 4–10. Barnetest og dyre-/hjelpeplassering er framleis
> oppfølging som skildra nedanfor; ikkje påstå at alle 25 eller alle ti nivå er ferdige.


> **NYAST – MAGISK LEIK, ANDRE RUNDE (2026-10-02, Codex):** Brukaren bad «Go go».
> Tiltaka 7, 10, 18 og 21 og dei første åtte nivåleikene er implementerte på `codex/magic-play`.
> Kjelde: `1cceea4ad2b97c8431d05288aa54447c3462d274`. Plan/status: `docs/MAGISK_LEIK.md`.
> Nivå 1–3 er aktive ved 0/2/5 merke. Høgare nivå og dei resterande 15 løfta er framleis planlagde.
> Gamle merke blir bevarte; opptente rettar blir lagra som `toy:*`. Merka blir ikkje brukte opp.
> Ny verd byrjar med gulrot til hest, venn i seng og krone på hovudet. Blåkopi-oppsett gir ikkje merke.
> Kartet viser neste leike og manglande merke. Oppdrag/Gåver viser bilete, framgang, hjelp og byte av
> eitt kort. Nytt nivå gir eit roleg kort med fire leiker. Låste nye møbelkort opnar same vegvisar.
> Alle elleve nye møbeltypar finst i Møblar på alle 24 stader; opna leiker kan dragast inn eller prøvast.
> Nivå 2: boblemaskin, fønar-driven vindmølle, vennebuss og mjuk putekastar. Nivå 3: tre vendbare
> klinkekulerenner, vaskbar fargesprøyte, miniheis og popcornvogn. Originale ting/last blir bevarte.
> Gratis pumpe for ball/vatn og kamera med varige Look-portrett. Foto kan flyttast og lagrast på lager.
> Togeventyr i Andre høgda: løft tannhjul, reparer tog, set oppi venn. Første fullføring gir eitt merke
> og varig tog-rett. Øydelagt variant overlever pakking; gamle brot-flagg blir migrerte utan å smitte
> ein annan møbeltype som har teke over same ID. Tog/buss brukar dei vanlege køyrepilene.
> Angreloggen har åtte steg i den opne økta: flytting, bygging/riving, samansetjing, møblering og
> figurpakking/henting. To fingrar ventar på begge sleppa. Originale figurar, klede og delar blir
> tilbakeførte, medan opptente merke, oppdrag, eventyrsteg og opplåsingar blir verande. Loggen er
> mellombels; lager/papirkorg/delar/rettar er lagra. Verkstad- og spelarvalendringar tømmer loggen.
> Angring lastar ei samanføyd verd gjennom WorldStore; eventuelle innflyttingsgjester blir sende heim.
> **Kontroll:** 442 einingstestar og 18 Android-testar grøne på siste kjelde. `testDebugUnitTest`,
> `assembleDebug`, `assembleDebugAndroidTest` og `lintDebug` grøne gjennom byggelåsen: 0 lint-feil,
> 30 åtvaringar (éin ny om same skjermhøgd-mønsteret i oppdragsboka; resten er eksisterande).
> GitHub «Bygg og test» er grøn på `1cceea4` (37061426858).
> Isolert pakke `app.trollfoss.levels`: mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi.
> Faktisk krone- og sengdraging fullførte oppdrag. Angring av krona lét det opptente merket stå.
> På stranda vart same ball 359 pumpa til badeball. Kamera laga portrett 361 av Hedda, og bilete
> variant 10361 vart pakka til lager. Det og to spelar-ID-ar 25/45 overlevde installasjon/omstart.
> På mobil vart boblemaskina dratt ut av Møblar og starta. Låst klinkekulebane viste manglande merke
> og reiseknappar. I lås-dialogen på den låge mobilen kan barnet rulle for å sjå heile reiseknappane.
> Tannhjul 362 vart dratt til toget, same del gjekk i sekken, og Alva 45 vart sett oppi. Fire merke vart
> førehandslagde i prøveverda for grensekontroll; denne verkelege fullføringa gav det femte, nivå 3,
> gåvekort og varig `toy:TRAIN`. Toget vart køyrt med pilene og stoppa, med same passasjer.
> Vanleg kaldstart opna Kartet. Oppdrag opna frå Kartet vart lukka tilbake til Kartet.
> På siste nettbrettprøve vart mais 363 dratt til popcornvogna og vart den same tingen som POPCORN,
> FREE. Foto 10361 vart henta frå lager etter omstart som nytt flyttbart møbel 344 med same portrett.
> Ingen Trollfoss-krasj i krasjbufferen; `lastanr` viste ingen ANR sidan oppstart av denne emulatoren.
> Prøvepakkane `.levels`/`.levels.test` er avinstallerte. Fysisk 1080 × 2400 / 420 dpi og rotasjon 1/1
> er tilbakeførte; emulatoren starta for prøva er stoppa. Produksjonsapp 1.5.0 og original debug-app
> står att, og deira verdsfiler er urørte. Prøveskript, testdata og bilete i `screenshots/levels/` er
> Git-ignorerte. Ingen privat verd eller signeringsinformasjon er lagt i Git.
> **Att før barnetest:** dyret kan ha vandra inn mellom gamle møblar før hjelpa blir brukt. Gulrot-
> hjelpa viste stad/råvare og gav ein roleg pause; den manuelle matarprøva vart ikkje fullført. Barnet
> kan velje eit anna kort. Plassering/hint og balanse må prøvast med barn før vi kallar dei forståelege.
> Versjon er framleis 1.5.0 / kode 8; ingen ny APK-release eller signeringsendring.
> Neste runde er 8, 13, 14, 15 og 19. Barnetest, fletting til `main` og publisering står att.

> **NYAST – MAGISK LEIK, FØRSTE RUNDE (2026-10-02, Codex):** Etter dei 25 prioriterte ideane
> bad brukaren «GO!». Dei seks første tiltaka er implementerte på `codex/magic-play`.
> Kjelde: `2a38289b1470c951fec86c0eb658f297df797e85`; førre hovudendring `3086d86`.
> Dei resterande 19, rekkjefølgje, ferdigkrav og kopling til nivåplanen står i `docs/MAGISK_LEIK.md`.
> Versjon er framleis 1.5.0 / kode 8. Ingen ny APK-release eller signeringsendring i denne runden.
> Pute, bamse, laken, bok og lykt har fleire faktiske handlingar gjennom «Leik og eventyr».
> Venner i nærleiken reagerer på leik og overraskingar; haldne og sovande figurar blir respekterte.
> Laken + pute blir ei putehytte, planke + to dekk ei trillevogn. Same del-ID-ar blir lagra i leika.
> Ta frå kvarandre eller legg leika på lager frigjer passasjerar/last og gir delane tilbake i sekken.
> Leikepakkane hentar berre sine eigne delar. Andre møbleringar og heldne/brukte ting blir bevarte.
> Vogna har to separate handtak med finger-eigarskap, og valfri hjelp for éin finger. Avbrot slepper grep.
> Tre frivillige eventyr har bilete, neste handling, stad, framgang og ei gåve som berre blir gitt éin gong.
> Påminninga i rommet følgjer eventyrsteget. Lagra kit, samansetjingar og eventyr blir validerte ved opning.
> Husbygging har større romval og førehandsvising før hammartrykket. Mobilførehandsvisinga er avgrensa
> slik at heile hammaren og «Bygg dette rommet» er synlege over «Ferdig». Begge språk er med.
> **Kontroll:** 414 einingstestar og 13 Android-testar grøne på siste kode. `testDebugUnitTest`,
> `assembleDebug`, `assembleDebugAndroidTest` og `lintDebug` grøne gjennom byggelåsen (0 lint-feil / 29 åtvaringar).
> GitHub «Bygg og test» er grøn på siste kjelde `2a38289` (37050050046), og på `3086d86` (37046982619).
> Isolert prøvepakke `app.trollfoss.magic`; same emulator i mobil 2400 × 1080 / 420 dpi og nettbrett
> 1920 × 1200 / 240 dpi. Faktisk draging laga hytta og vogna. Figur vart sett i hytta, lykt-handling
> fullførte Stjernenatt, og gåva/framgangen vart lagra. Hytta overlevde omstart og pakking gav dei same
> delane 360/361 tilbake som BAG. To spelar-ID-ar 25/45 stod ved lag. Vogna flytta med hjelparknappen.
> Kjøken på nettbrett og stove på mobil vart bygde etter førehandsvising; siste mobilknapp/tekst er synleg.
> Android-kontrollen dekkjer to grep samtidig, separat slepp/avbrot og ny møbelkunst i alle 24 stader.
> Éin emulator-ANR ved formatbyte kl. 20:37 viste native `HardwareRenderer.setStopped` i hovudtråden.
> Rein omstart gav fungerande vognkontroll; etterfølgjande mobilprøver og Android-kontroll hadde ingen
> ny ANR eller Trollfoss-krasj. Sporet ligg ignorert i `.gradle-tmp/magic-anr.txt` for eventuell oppfølging.
> Prøvepakkane `.magic`/`.magic.test` er avinstallerte; fysisk 1080 × 2400 / 420 dpi og rotasjon 1/1
> er tilbakeførte. Emulatoren starta for prøva er stoppa. Produksjonsapp 1.5.0 og original debug-app
> står att; deira verdsfiler er urørte. Bilete i `screenshots/magic/` og prøveskript er Git-ignorerte.
> Neste arbeidsrunde er opplåsingsvegvisar, nye nivåleiker og trygg angrehandling, slik planen seier.
> Barnetest, fletting til `main` og ny publisering står att som eigne steg.

> **UTGÅVE 1.5.0 PUBLISERT (2026-10-02, Codex):** Brukaren bad «Release» etter arbeidet nedanfor.
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.5.0 er offentleg, stabil og nyaste utgåve.
> Kjelde/tagg: `499574312c9260a67777ef5ebf378d40f7aee30a`. Versjon 1.5.0 / kode 8.
> Faste spelarar, lokal fleirspelar, møbeldraging, flytta møbelkunst og trygg pakking/henting av
> figurar er med. Release-notat: `docs/release-v1.5.0.md`. Nivåplanen er framleis berre ein plan.
> 399 einingstestar grøne; dei 10 Android-testane frå funksjonskontrollen er grøne.
> `testDebugUnitTest`, `lintRelease` (0 feil / 20 åtvaringar) og `assembleRelease` er grøne gjennom
> byggelåsen. GitHub «Bygg og test» er grøn på `main` (37037222481) og taggen (37037222530).
> Universal APK: 3 025 366 byte; SHA-256
> `67d81f436c52ad2817ad9c98c9ee4a24590a3a60a2dd05196f1c9f94bea9f5ef`.
> Pakke `app.trollfoss`, minste Android 26, ikkje-debuggable og opphavleg signatur er stadfesta.
> Arkiv `dist/release-v1.5.0/`: APK, R8-mapping, `SHA256SUMS.txt`, `SOURCE_COMMIT.txt`.
> Kladden og den offentlege releasen har éin APK og tre tekstfiler, alle med kontrollert storleik og
> GitHub-digest. Taggen og SOURCE_COMMIT peikar på same kjelde. Ingen vedlegg er erstatta etter publisering.
> Release-lista og latest-endepunkt er prøvde utan token. Separat offentleg nedlasting har same
> SHA-256/signatur som arkivet. Den offentlege lista vart synleg etter kort publiseringspropagering.
> **Ekte oppdatering:** den eksisterande signerte 1.4.1 / kode 7 vart oppdatert gjennom Kart →
> For vaksne → gongestykke → Sjekk no → Last ned oppdatering → Installer oppdatering → Android Update.
> Play Protect viste sitt vanlege skanningsspørsmål; «Install without scanning» gav «App installed».
> Installert versjon er 1.5.0 / kode 8. APK-en trekt ut av den installerte appen har same hash som
> den publiserte fila. Ingen avinstallering, nullstilling eller utskifting av produksjonsverda.
> Fyrste opning viser spelarvalet. «Vel seinare» går til Kartet, og neste vanlege kaldstart opnar Kartet.
> Fjellet, synlege ting, 0/76 glimt, tre oppdrag og gamle namn/utsjånad/klede er bevarte i UI-kontrollen.
> Ingen spelar eller ny figur vart vald/lagra i den private verda. Ny spelarvals-flagging er lagra.
> Signert utgåve er visuelt prøvd på mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi:
> spelarval, kartretur/kaldstart, venner med pakkeknapp og møbelpanel med dragehint. Ingen privat
> møblering/husbygging er endra. Funksjonsprøva i isolert pakke er dokumentert nedanfor.
> Før oppstart vart begge stoppade userdata-diskar kopierte til det ignorerte
> `.gradle-tmp/release-1.5.0-original/emulator-disk/` (om lag 6,4 GB). Produksjons-JSON er ikkje direkte
> lesen på denne Play Store-emulatoren; lagringskompatibilitet er i tillegg dekt av einingstestar.
> Ingen Trollfoss-krasj i krasjbufferen. Fjellet står att valt; fysisk 1080 × 2400 / 420 dpi og rotasjon
> 1/1 er tilbakeførte. Installasjonsløyvet er framleis `allow`, emulatoren er stoppa, og produksjonsappen
> står att oppdatert til 1.5.0. Opphavleg debug-app/verdsfil er urørt. Bilete i
> `screenshots/release-v1.5.0/` og prøveskript er Git-ignorerte; ingen private data er publiserte.
> «Lokalt/ikkje utgjeve»-status nedanfor er historikk frå før denne publiseringa.

> **NYAST – FASTE SPELARAR, MØBELDRAGING OG TA BORT FIGURAR (2026-10-02, Codex, lokalt):**
> Arbeidet ligg ukommittert på `main`, etter publisert 1.4.1. Ingen versjonsendring, release, push
> eller signeringsendring. Produksjonsappen og den opphavlege debug-verda er urørte.
> Fyrste start opnar spelarval over Kartet; barnet kan velje seinare. «Spelarar» finst på Kartet
> og i romma. Éin, to eller fleire faste figur-ID-ar blir lagra med verda; ingen figurkopiar.
> Spelarane følgjer med på reiser, gjennom trappene og ved romval/panorering. Utsjånad, klede,
> hatt og ting i hendene følgjer same figur. Dei vandrar ikkje til andre etasjar på eiga hand.
> Portrett: trykk hentar hit, hald inne opnar verkstaden. Ny figur frå spelarval blir automatisk vald.
> To barn kan flytte kvar sin figur samtidig; ein annan finger kan ikkje overta ein halden figur.
> «Venner» viser «Her no» med «Legg i sekken», og venner ein kan hente hit. Pakking tek figuren
> ut av spelarvalet, også ved draging til sekken, så han ikkje automatisk kjem etter. Figuren og
> tinga hans blir bevarte og kan hentast tilbake. Lagra WORN-ting skal ha same stad som eigaren.
> Møblar/Lager støttar sidelengs draging til rommet, med førehandsvising fram til slepp; loddrett
> rørsle rullar menyen. Avbrot eller slepp over panelet tek ingenting frå lager og legg ingenting til.
> GR/UP/AT/CE/GA-møblar blir teikna med eigen etasje-kunst også i andre stader. Android-test
> dekkjer alle desse og MI-møblar i alle 24 stader. Tomme golv i Mitt hus tek ikkje imot spelarar.
> Isolert prøvepakke `app.trollfoss.players`, bilete i ignorerte `screenshots/players/`.
> Mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi blir brukte på same emulator.
> **Kontroll:** 399 einingstestar og 10 Android-testar grøne. `assembleDebug` og
> `assembleDebugAndroidTest` grøne; `lintDebug` 0 feil / 29 åtvaringar, alt gjennom byggelåsen.
> Fyrsteval, to spelarar, fire spelarar, ny figur, endring av Hedda, romval, panorering og trapp er
> prøvde. Mobil og nettbrett: ekte draging frå Møblar, meny-rulling utan ekstra plassering, og
> sofa/leiketog frå Lager til Stranda med rett kunst. Hatt/bamse er bevarte gjennom reiser/omstart.
> Nettbrett: Hedda (ID 25) pakka, omstart/reise, framleis BAG og ute av spelarvalet; henta tilbake
> til Stranda med same utsjånad og to WORN-ting. Mobil: tilsvarande pakking/henting av Alva (ID 45),
> deretter begge valde på nytt; fire spelar-ID-ar lagra og bevarte ved vanleg kaldstart på Kartet.
> Alle fire og tinga deira følgde «Oppe» i Mitt hus. Ingen Trollfoss-krasj i krasjbufferen.
> Prøvepakkane `.players`/`.players.test` er avinstallerte. Fysisk 1080 × 2400 / 420 dpi og rotasjon
> 1/1 er tilbakeførte; emulatoren som vart starta for prøva er stoppa. Original debug- og
> produksjonsapp/verdsfil er urørte. Ignorerte bilete og prøveskript står att lokalt.

> **UTGÅVE 1.4.1 PUBLISERT (2026-10-02, Codex):** Brukaren bad «Publiser» etter rettingane nedanfor.
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.4.1 er offentleg, stabil og nyaste utgåve.
> Kjelde/tagg: `a968d4a9be22832cf5bed3e3dab3d46f2b570cbb`. Versjon 1.4.1 / kode 7.
> Husoversikt, kartretur/oppstart, uavhengig hårlengd/fylde, lagra papirkorg og tydeleg sekk er med.
> Nivåa og dei 44 belønningane i `docs/PROGRESJON.md` er framleis ein utviklingsplan.
> 393 einingstestar er grøne, `lintRelease` 0 feil/18 åtvaringar og `assembleRelease` grønt gjennom låsen.
> GitHub «Bygg og test» er grøn på både `main` (37009652958) og taggen (37009653610).
> Universal APK: 3 008 982 byte; SHA-256
> `5b61915d6e809cf316bcecf19434f33cb981990277c722b59462100fe99cc0ec`.
> Pakke `app.trollfoss`, minste Android 26, ikkje-debuggable og opphavleg signatur er stadfesta.
> Arkiv `dist/release-v1.4.1/`: APK, R8-mapping, `SHA256SUMS.txt`, `SOURCE_COMMIT.txt`.
> Kladden hadde éin APK og tre tekstfiler; alle fire storleikar/hashar vart kontrollerte mot GitHub.
> Offentleg release-liste og latest-endepunkt er prøvde utan token. Separat offentleg nedlasting
> har same SHA-256/signatur. Taggen og SOURCE_COMMIT peikar på same kjelde.
> **Ekte oppdatering:** signert 1.4.0 / kode 6 vart oppdatert gjennom Kart → For vaksne → gongestykke →
> Sjekk no → Last ned oppdatering → Installer oppdatering → Android Update. Play Protect viste det
> vanlege skanningsspørsmålet; «Install without scanning» gav «App installed». Installert versjon er
> 1.4.1 / kode 7, og den trekte base-APK-en har same hash som den publiserte fila.
> Appen opna Kartet. Fjellet, synlege ting, 0/76 glimt, tre oppdrag og namn/klede på figurane er bevarte
> i før-/etter-kontrollen. Ingen nullstilling, avinstallering eller utskifting av den private verdsfila.
> Før oppdateringa vart begge stoppade userdata-diskar kopierte til det ignorerte
> `.gradle-tmp/release-1.4.1-original/emulator-disk/` (om lag 6,4 GB). Produksjons-JSON er ikkje direkte
> lesen på denne Play Store-emulatoren; gammal lagring/papirkorg er i tillegg dekt av einingstestar.
> Signert utgåve er visuelt prøvd på mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi:
> kartoppstart, figurverkstad, kort/langt/fyldig hår, grunnmurval utan bygging og papirkorg.
> Ingen førehandsvisingsfigur er lagra. Det private huset vart ikkje bygd/endra i releaseprøva.
> Bilete i `screenshots/release-v1.4.1/` er Git-ignorerte; ingen private data er publiserte.
> Ingen Trollfoss-krasj i krasjbufferen. Fjellet er vald att; storleik/dpi og rotasjon er tilbakeførte
> til fysisk 1080 × 2400 / 420 dpi og 1/1. Installasjonsløyvet er som før (`allow`), emulatoren er
> stoppa, og produksjonsappen står att oppdatert til 1.4.1. Opphavleg debug-app/verdsfil er urørt.
> Tidlegare «lokalt/ikkje utgjeve»-status nedanfor er historikk frå før denne publiseringa.

> **NYAST – KARTRETUR, HÅR, PAPIRKORG OG HUSOVERSIKT (2026-10-02, Codex, lokalt):**
> Nye tilbakemeldingar etter publisert 1.4.0. Arbeidet ligg ukommittert på `main`; ingen ny release,
> versjonsendring, push eller signeringsendring. Produksjonsappen og den opphavlege debug-verda er urørte.
> Appen startar på Kartet; ein meny/bok/figurverkstad går tilbake til skjermen som opna han.
> `ScreenHistory` har testar for oppstart, begge opphav, nøsting, foreldreport og reise.
> Håret er teikna i hovudkoordinatar med faste tinningar og hårfeste: uavhengig lengd og fylde for alle
> 18 frisyrer. `HairFit` testar uavhengige dimensjonar. Debug-kontaktark viser min/normal/maks.
> Lagerfjerning flyttar til `world.discardedStorage`, lagra i verdsfil og valvis gjenoppretting i
> Papirkorg. Gamle lagringar har tom papirkorg; allereie permanent sletta ting kan ikkje rekonstruerast.
> Ekte UI-prøve: jukeboks variant 2 fjerna, appen omstarta, henta tilbake; JSON stadfesta type/variant.
> Reisesekken har lesbar tom-tekst og grøn open-knapp. Eple er drege inn og stadfesta lagra i sekken.
> Mitt hus: kompakte faner, oversikt over begge etasjane, markeringsfarge, romnamn/byggjeplass,
> kamerahopp ved val/opning, behald ferdig rom valt, synlege «Leik i rommet»/«Bygg neste rom».
> Låst etasje opnar etasjesteget; usupportert plass oppe går til plassen som må byggjast nede.
> Ny plan `docs/PROGRESJON.md`: ti nivå frå eksisterande klistremerke, 36 aktive nivåleiker og åtte
> historiegåver, konkrete biletoppdrag/hint, trygg lagring og fasa gjennomføring. Desse er planlagde,
> ikkje implementerte. Vidare husbygging: faktisk førehandsvising, leikedemonstrering og husforteljing.
> Testpakke `app.trollfoss.haarfix` isolerer prøvene frå dei private verdane. Bilete i
> `screenshots/haarfix/` er ignorerte. Sluttbygg gjennom låsen er grønt: 393 einingstestar,
> 0 feil/0 hoppa over, `lintDebug` 0 feil/27 åtvaringar og `assembleDebug` grønt.
> Mobil 2400 × 1080 / 420 dpi: faktisk rombygging, ferdig-rom-val, synlege bygg/leik-knappar,
> rom-/etasjehopp, entréval, kartretur frå bok/figurverkstad/foreldreport, Android tilbake,
> kald oppstart og figurlagring. Nettbrett 1920 × 1200 / 240 dpi: bygg andre etasje, bygg rom oppe,
> vel neste rom og gå rett til leik. Hårark min/normal/maks og sitjande/liggjande figurar er sette i
> begge format. Papirkorg og reisesekk er prøvde med faktisk omstart; eplet vart henta ut att.
> Ingen testapp-krasj i krasjbufferen. Testpakken er avinstallert; produksjon/opphavleg debug er
> urørte. Storleik/dpi er tilbakeførte til fysisk 1080 × 2400 / 420 dpi, rotasjon 1/1,
> og emulatoren som vart starta for prøva, er stoppa. Ingen utgjeving av desse endringane.

> **UTGÅVE 1.4.0 PUBLISERT (2026-10-02, Codex):** Brukaren bad om ny release.
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.4.0 er offentleg, stabil og nyaste utgåve.
> Taggen peikar på `e39707e6af6f70c9e215917964e3bf6a734dbb24`.
> Versjon 1.4.0 / kode 6 samlar figurverkstad, sengballong, personleik, lager og køyretøy nedanfor.
> 383 einingstestar er grøne (0 feil/0 hoppa over), `lintRelease` har 0 feil og 18 åtvaringar,
> og `assembleRelease` er grøn gjennom byggjelåsen. Arkiv: `dist/release-v1.4.0/`.
> Universal APK: 3 008 982 byte, SHA-256
> `8e3494131eed06787f4dae7f2c9eef1ea6699ab54600e0718f64eb8b1f0852da`.
> Pakke `app.trollfoss`, minste Android 26, ikkje-debuggable og opphavleg signatur er stadfesta.
> Tidlegare «ikkje utgjeve»-statusar nedanfor er historikk frå før denne releaseførespurnaden.
> Kladden hadde nøyaktig éin APK og tre tekstfiler, med stadfesta storleik/digest for alle fire.
> Offentleg API er kontrollert utan token; separat offentleg nedlasting har same hash/signatur.
> GitHub «Bygg og test» er grøn for både `main` (36992451246) og taggen (36992450979).
> **Ekte oppdatering:** den installerte, signerte 1.3.0 / kode 5 vart oppdatert gjennom
> Kart → For vaksne → gongestykket → Sjekk no → Last ned → Installer → Android Update.
> Play Protect viste vanleg spørsmål om skanning; «Install without scanning» gav «App installed».
> Appen vart opna att som 1.4.0 / kode 6; den installerte APK-en har same hash som releasefila.
> Fjellet, synlege ting, 0/76 glimt, tre oppdrag og figurane/namna var bevarte i skjermkontrollen.
> Ingen nullstilling, avinstallering eller utskifting av den private verdsfila vart gjort.
> Før oppdateringa vart emulatoren stoppa og begge userdata-diskfilene kopierte til det ignorerte
> `.gradle-tmp/release-1.4.0-original/emulator-disk/`. Play Store-systemet tillèt ikkje adb root;
> den private JSON-fila vart derfor ikkje lesen direkte. Lagringskompatibilitet er òg dekt av testar.
> Signert release er visuelt prøvd i mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi:
> figurverkstad, hårglidebrytar, nye frisyrer og augefargar. Førehandsvisingsfiguren vart ikkje lagra.
> Bilete ligg Git-ignorert i `screenshots/release-v1.4.0/`; ingen private bilete/data er publiserte.
> Ingen Trollfoss-krasj i krasjloggen. Storleik/dpi og rotasjon er tilbakeførte, installasjonsløyvet
> er som før (`allow`), og emulatoren er avslutta. Produksjonsappen står att oppdatert til 1.4.0.

> **NYAST – FIGURVERKSTAD, SENGBALLONG OG MEIR PERSONLEIK (2026-10-02, Codex, lokalt og ikkje utgjeve):**
> Dette kjem i tillegg til lager/køyretøy-endringane nedanfor. Alt ligg ukommittert på `main`;
> publisert utgåve er framleis 1.3.0. Ingen release, versjonsendring, push eller signeringsendring.
> Folk (og Rumle) har nytt uttrykk og fleire utsjånadval: 18 frisyrer, 14 hårfargar, 12
> augefargar, åtte augetypar, ansiktsform, nase, smil, ni ekstra detaljar, nye klede og mønster.
> Hårstorleik/lengd, augestorleik/avstand og høgd har glidebrytarar med direkte førehandsvising.
> Ti nye `Look`-felt blir lagra med trygge standardverdiar for gamle lagringar. Gamle id-ar,
> namn, stemmer, plagg og handting er bevarte. Langt hår ligg bak kleda og hendene.
>
> Ballongen på kartet har ei brei seng med madrass, puter, teppe og sengegavlar, og opptil tre
> synlege folk sit oppi. Reisereglane er bevarte. Brukaren svarte **«Behald som hemmeleg tunnel»**:
> Trollhola–kjellaren i Storhuset er behalden, med hus/pil på inngangen og forklaring etter
> stadstittelen. Eit anna trappemøbel kan ikkje bruke passasjen berre fordi id-en er lik.
>
> `PersonPlay`: lesing, telefonprat (også to telefonar på ulike stader), tannpuss, kos med
> bamse/pute, instrument og dans, kiling/pelsstell, og ballkast med fangst/retur av same ball.
> Trykk på den haldne tingen gjentar handlinga. Bøker lagrar sida. `PlayInteractions` utvidar
> spade/bøtte–sandslott, snømann, verktøy–lys, telefon–TV, vatn/and–akvarium, seng og trampoline;
> bading fjernar krem/blekk. Verktøy og leiker blir ikkje brukte opp. Detaljar i `INTERAKSJON.md` 8.
>
> **Kontroll:** 383 einingstestar, 0 feil og 0 hoppa over; `lintDebug` (0 feil, 27 åtvaringar),
> `assembleDebug` og `git diff --check` grøne gjennom byggjelåsen. Testar av ny/gammal lagring,
> ugyldige mål, plagg, reise med bok, telefonar, ball fram/tilbake utan kopiar, kos/tannpuss/kiling,
> musikk, verktøysamband i alle 24 stader, bading/trampoline og tunnel. Vanleg debug-APK:
> `app/build/outputs/apk/debug/app-debug.apk`, pakke `app.trollfoss.debug`.
>
> **Sett og brukt:** Tunet_Ascii, mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi.
> I begge format: figurverkstad, hengande seng med synlege figurar, lesing, telefon ved øyret,
> ballkast–fangst–retur, og hemmeleg tunnel til Storhuset. Øyvind fekk ny frisyre og hårstorleik
> på mobil, vart lagra/opna att med same id/namn/bok, og fekk augefarge, augestorleik, kappe og
> mønster på nettbrett; den lagra fila stadfesta alle vala. Alle 18 frisyrer og ekstremmål med
> krone/handting i ulike positurar er visuelt gjennomgåtte. Bokmålsval og tunneltekst er prøvde.
> Bilete: `screenshots/figurar/` (Git-ignorert). Den nye `WorkshopSheetActivity` er berre i debug
> og teiknar ei fersk verd utan å lese privat lagring.
>
> Visuell kontroll brukte berre den isolerte pakken `app.trollfoss.leike`. Produksjons- og
> opphavleg debug-app vart ikkje opna, installerte eller nullstilte i denne delen av arbeidet.
> Testpakken er avinstallert; emulatorstorleik/dpi/rotasjon er tilbakeførte, og emulatoren er
> avslutta. Ingen Trollfoss-krasj i krasjloggen. Fysisk mobil/nettbrett og full visuell gjennomgang
> av alle 24 stader står att; dette er målretta kontroll av tillegget.

> **NYAST – LAGER OG FLEIRE KØYRETØY (2026-10-02, Codex, lokalt og ikkje utgjeve):**
> Endringane ligg ukommitterte på `main`. Publisert utgåve er framleis 1.3.0; ingen ny release,
> versjonsendring, push eller endring av signeringsnøkkelen er gjort.
> Lager kan setje ut ting i alle 24 stader (Mitt hus må ha golv), og kameraet viser faktisk
> plassering, også på stranda. Dei opphavlege stadene har 60 ekstra møbelplassar, Storhuset 100
> per etasje. Full stad gir forklaring utan tap av lagertingen. Kvart lagerkort har sletting av
> éin ting og angre; angrehistorikken varer medan appen er open, også etter reise.
> Båt, radiobil og ubåt har verkeleg rørsle med piler/stopp og passasjerar, som traktoren.
> Ubåten kan stige/dykke, båten held seg på naturleg vatn, og radiobilen snur ved kollisjon og
> kan køyre fri om han er plassert oppå inventar. Køyring stoppar ved bakgrunn/reise/innlasting.
> Detaljar: `docs/INTERAKSJON.md` punkt 7 og øvst i `CHANGELOG.md`.
>
> **Kontroll:** 371 einingstestar, 0 feil og 0 hoppa over; `lintDebug` (0 feil, 26 åtvaringar),
> `assembleDebug` og `git diff --check` grøne gjennom byggjelåsen. Testar av alle 24
> lagerdestinasjonar, meir enn 20 tillegg og innlasting, full stad utan tap, sletting/angring,
> passasjerar, køyring/retur/stopp, kollisjon og grenser for vatn/høgde. Vanleg debug-APK:
> `app/build/outputs/apk/debug/app-debug.apk`, pakke `app.trollfoss.debug`.
>
> **Sett og brukt:** Tunet_Ascii i mobilformat 2400 × 1080 / 420 dpi og nettbrettformat
> 1920 × 1200 / 240 dpi. I begge format: utplassering på stranda, lagring, enkeltvis sletting,
> angre og ny utplassering; båt, radiobil og ubåt med synlege styringsknappar og faktisk rørsle.
> Ingen Trollfoss-krasj i krasjloggen. Bilete: `screenshots/lager-leik/` (Git-ignorert).
> Kontrollane brukte ei ny isolert testpakke `app.trollfoss.leike`; ho er avinstallert etterpå.
> Produksjonsverda er urørt. Den opphavlege debug-appen fekk ei kodeoppdatering under ein
> tidleg APK-kopiering, men vart ikkje opna og lagringa hennar vart ikkje endra.
> Emulatorens storleik, dpi og rotasjon er sette tilbake, og emulatoren er avslutta.
> Emulatorstartskriptet er òg retta: eitt einingsnamn skal vere ei liste, ikkje bli første bokstav.
>
> Alle 24 stader er prøvde i einingstestar, men ikkje visuelt gjennomgåtte på nytt.
> Full belastningsprøve med 60 ekstra møblar og kontroll på fysisk mobil/nettbrett står att.

> **NYAST – RELEASE 1.3.0 PUBLISERT (2026-10-02, Codex):**
> https://github.com/oyvhov/trollfoss-android/releases/tag/v1.3.0 er offentleg, stabil og nyaste utgåve.
> Commit og tag: `889bfa24a7dfe0612c712a24a1232dd2d94ff90b`. README, plan og endringslogg er pusha.
> Versjon 1.3.0 / kode 5, éin universal APK på 2 976 214 byte, med opphavleg signatur.
> APK SHA-256: `d6c342ec62ef4ad8f454231a906a1101c1f5258726ba30337ea50b742faa387a`.
> Arkiv: `dist/release-v1.3.0/`; mapping, SHA256SUMS og SOURCE_COMMIT er med i releasen.
> Alle fire kladdfilene er hash- og storleikskontrollerte. Offentleg release-liste og latest er
> kontrollerte utan token; den offentleg nedlasta APK-en har lik hash og original signatur.
> 362 einingstestar, `lintRelease` (0 feil, 18 åtvaringar), `assembleRelease` og begge GitHub CI-køyringane
> er grøne: main `36965692125`, tag `36965691850`.
>
> **Ekte oppdatering prøvd:** eldre lokal kopi av same kjelde (1.2.1 / kode 4) installert oppå den
> eksisterande release-appen, utan nullstilling. Kart → vaksenport → Sjekk no → Last ned → Installer,
> Android-løyve og installasjonsval. PackageManager stadfesta fullført installasjon med data bevart.
> Den installerte `base.apk` er henta ut og har nøyaktig same hash som den offentlege APK-en.
> Foreldresida viser 1.3.0; same 20 figurar, framgang, namngjeven Øyvind, portrett og ting på Heileberget
> er bevarte. Release-appen er sett i mobilformat 2400 × 1080 / 420 dpi og nettbrettformat
> 1920 × 1200 / 240 dpi; vennelista og høgre møbelknapp er på plass. Ingen Trollfoss-krasj.
>
> **Avvik i Android-dialogen:** etter Play Protect-valet «Install without scanning» viste Android
> «App not installed», trass i at installasjonen var fullført. Loggen viser både InstallSuccess og
> InstallFailed, med status 1 på sistnemnde. Årsaka er ikkje avklart; dette er ikkje ein feilfri
> installasjonsdialog. Appen opnar og rett offentleg APK er installert, stadfesta uavhengig av dialogen.
> Prøv oppdatering på fysisk eining. Bilete: `screenshots/release-v1.3.0/` (lokalt, Git-ignorert).
>
> Emulatorens skjermstorleik, dpi, rotasjon og installasjonsløyve er sette tilbake etter prøva,
> og emulatoren er avslutta.
> Produksjonsverda er ikkje nullstilt eller erstatta; debug-verda er urørt i denne publiseringsøkta.
> Kontroll på fysisk mobil og nettbrett står framleis att.

> **FØR PUBLISERING – 1.3.0 KLARGJORT (2026-10-02, Codex):** implementert på `main`.
> Brukaren har bede om publisering. Versjon 1.3.0 / kode 5, tag `v1.3.0`; original signeringsnøkkel.
> Den grundige planen og oversikta for alle stader står i `docs/INTERAKSJON.md`; endringane står
> under «1.3.0» i `CHANGELOG.md`. Utgivingsnotat: `docs/release-v1.3.0.md`.
> Publiseringskontroll: 362 testar, `lintRelease` (0 feil, 18 åtvaringar) og `assembleRelease` grøne.
> Signert universal APK: `app.trollfoss`, versjon 1.3.0 / kode 5, Android 8+, ikkje debuggbar.
> Sertifikat SHA-256 samsvarar med originalnøkkelen. Publiseringsresultat blir ført øvst når verifisert.
>
> **Gjort:** fast portrettknapp for å hente eksisterande venner (og namngjevne eigne figurar), med
> klede og handting; tørr ankomst ved elver og berre i bygde rom. Gripeprioriteten lèt folk flyttast
> medan møbelpanelet er ope. Møbleringsknappen ligg til høgre. Traktoren flyttar faktisk posisjon
> med piler, stopp, passasjer, kamera og skubbing, også etter flytting gjennom lageret til Bakeriet.
> Rå fisk/egg/pølse/deig gir grimase; råvarene kan tilberedast på bål/vedomn/komfyr og etast vidare.
> Koppar og bøtter held på vatnet ved lagring; vatn/ved, planter, instrument og spegel gir felles
> samanhengar på tvers av stader. Sopp gir kjempe i 20 sekund, slime gir sveving, lagra saman med
> gjenverande effekttid. Seks hemmelege små rom og ni nye oppdagingsbok-overraskingar.
> Vagstaddalen har fotoinspirert brun plankehytte, raude lister, blå vindauge, torvtak, grå pipe,
> bjørker og bergvegg; elva byrjar i terrenget. Rommet i dalen er skjult i ein stein med raud bok.
>
> **Kontroll:** 362 einingstestar, ingen feil eller hoppa over; `assembleDebug` og `lintDebug` grøne,
> gjennom `Build-Locked.ps1`. Testane prøver rå fisk → bål → grilla fisk → alle bitane i alle 24 stader,
> vatn/musikk/lys på tvers av stader, lagring midt i magi, henting med klede og handting, sekken,
> bygde rom, traktor frå Garden sitt lager til Bakeriet med innlasting etter køyring, kollisjonar og
> elvebreidd, og hemmelege rom utan gjenteken premie. `git diff --check` er rein.
>
> **Sett og brukt på Tunet_Ascii (`-gpu host`, denne gongen starta emulatoren):** mobil 2400 × 1080
> / 420 dpi og nettbrett 1920 × 1200 / 240 dpi. I begge format: venneliste og ankomst, rå fisk-grimase,
> grilling og første bit, traktor med passasjer og skubbing, hemmeleg bokrom, høgre møbleringsknapp
> og ny hytte/elv. Mobil: Hedda flytta medan møbelpanelet står ope, sopp-kjempe og tilbake til
> opphavleg storleik etter 20 sekund. Nettbrett: BesteSonja henta med lue/briller, Berit på tørr
> elvebreidd, natt i dalen. Ingen Trollfoss-krasj i krasjloggen. Bilete i `screenshots/interaction/`.
> Dette er målretta kontroll av tillegget; alle 24 stader er ikkje visuelt gjennomgått på nytt.
>
> **Data verna:** brukt isolert kopi av testverda. Opphavleg privat lagring er sett tilbake, verifisert
> med SHA-256 `a0d97549ca33476618fbc82987534017994b2902275230299a422744c0077d8f`.
> Skjermstorleik/dpi og begge rotasjonsinnstillingane er sette tilbake, og emulatoren er avslutta.
> Release-appen og signeringa er urørte. Debug-APK: `app/build/outputs/apk/debug/app-debug.apk`.
> Kontroll på fysisk mobil/nettbrett og publisering av dette tillegget står att.

> **RELEASE 1.2.1 PUBLISERT (2026-10-01, Codex):** https://github.com/oyvhov/trollfoss-android/releases/tag/v1.2.1
> er offentleg, stabil og nyaste utgåve. Taggen peikar på `90e6cd694b2af31abd10d35f56a9b0cba102e977`.
> Éin universal APK, 2 959 830 byte, med den opphavlege Trollfoss-signaturen.
> APK SHA-256: `1883c556efbf226a7cede5adb38cd5b32df34389985d070f3744e47c4202de01`.
> 350 einingstestar, `lintRelease`, `assembleRelease` og `assembleDebug` via byggjelåsen er grøne.
> Begge GitHub CI-køyringane (main og tag) er grøne. Alle fire kladdfilene er hash- og storleikskontrollerte.
> Offentleg release-liste og latest er kontrollerte utan token; nedlasta APK har lik hash og rett signatur.
> Brukaren bad om commit, oppdatert README på GitHub og ny release; alt er utført.
> Versjon 1.2.1 / kode 4. README har no gjeldande stadnamn, 18 reisemål, 24 stader og 76 glimt,
> oppdaterte funksjonar og retta lisensformatering. Utgivingsnotat: `docs/release-v1.2.1.md`.
> Release-arkiv: `dist/release-v1.2.1/`.
> **Ny emulatorprøve blokkert:** Tunet_Ascii vart starta med `-gpu host`, men WHPX feila med
> `Failed to setup partition, hr=80070005`; ingen eining kom opp. Ingen emulatordata vart endra.
> Mobil-/nettbrettkontrollane av same spelendringar frå førre økt er dokumenterte nedanfor.
> Oppdatering gjennom appen og kontroll på ekte maskinvare er framleis ikkje utførte.

Resten av dokumentet er historikk frå tidlegare økter; den øvste statusen er gjeldande.

> **NYAST – 1.2.1 UNDER ARBEID (2026-10-01 kveld, Claude Fable 5.1):** lokalt på `main`, **ikkje committa**,
> ikkje pusha, inga utgåve. Brukaren vil at fokuset alltid er betre UI og oppleving for barna. Planen står i
> `ROADMAP.md`; endringane i `CHANGELOG.md` under «Ikkje utgjeve».
>
> **Gjort og sett på emulator (mobil 2400 × 1080 og nettbrett 1920 × 1200):**
> - *Årstider på kartet* (brukaren melde at dei mangla): `season` går frå `MapScreen` gjennom `rememberMapLayer`,
>   `buildMapLayer`, `MapLayer`, `LiveKit` og `MapPen`. `MapPen.snow` er sann ved snøvêr **eller** vinter,
>   `snowing` berre ved snøvêr, `frost` berre om vinteren. Fargane kjem frå `SeasonKit` (`MapPen.grass`, `leaf`,
>   `fruit`). Høgtidene (jul, påske, gresskar) er ikkje på kartet enno.
> - *Møbellageret* (`DesignerPanel.kt`): rutenettet fekk aldri ei ny liste (lambdaen vart hugsa), så lageret stod
>   stille til fanebyte. No er lista ein verdi per `designVersion`. Lageret opnar seg når noko blir lagt i det, og
>   lagerknappen har eit tal. `Engine.down` ser bort frå fingrar som startar på sidepanelet (før gjekk trykk på tomme
>   felt gjennom til møblane bak og la dei i lageret).
> - *Liggjande skjerm på Android 16-nettbrett*: `android:appCategory="game"` og
>   `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` i manifestet. Utan dei ignorerer Android 16 `screenOrientation`
>   på skjermar frå 600 dp (appen har `targetSdk` 36) og viser appen ståande med mest himmel. Emulatoren med
>   `ignoreOrientationRequest=true` viser no appen liggjande i ei ramme; ikkje prøvd på ekte nettbrett.
> - *Yting* (`SpriteCache.kt`, `Engine.draw`): bileta av det som rører seg (figurar 13–24 Hz, møblar 8 Hz) blir bedne
>   om under teikninga og laga i `finish()`, dei mest forseinka først, innanfor 3 ms per ramme (alltid minst eitt).
>   Før: opptil tre per ramme i teiknerekkjefølgje, utan tidsgrense, og dei fremste figurane kunne svelte i fulle rom.
>   Målt med `-gpu host` (RTX 3060), mobilformat, 20 s per stad, teiknetid på hovudtråden per ramme, før → etter:
>   Garden 10,6 → 6,0 ms; hagen til Storhuset 7,1 → 2,3; Tivoliet 11,8 → 10,3; Heileberget 11,0 → 3,2;
>   Havbotnen 7,9 → 4,9. Rammer på 20 s: 1215/1203/1190/1167/1148 → 1214/1212/1176/1210/1202 (60 per sekund = 1200).
>   Loggen `TrollfossPerf` viser no også `worst` og `over16`.
> - *Forsøk som er slått av*: bileta som `GraphicsLayer` på grafikkortet (`SpriteCache.useLayers`, debug
>   `--es layers on`). På emulatoren var det ikkje raskare, og hagen til Storhuset fall frå 60 til 43 bilete i
>   sekundet. Prøv på ekte nettbrett før det eventuelt blir slått på; elles kan koden fjernast.
>
> - *Kartbygningar* (brukaren: «nokon av bygningane på kartet er litt dårlege/stygge»): Mitt hus vart teikna med
>   heile fasaden pressa inn i landemerkebreidda og utan vindauge (`detail = 0`), altså ei låg brakke. No:
>   `MineHouse.forMap()` i `MapMine.kt` (høgst to modular, `detail = 1`, `MAP_MINE_SCALE` 1,7), og `mapSpot` for
>   Mitt hus er flytta til (0,29, 0,36) så taubana ikkje kryssar huset. Butikken (`Bx.shop`) har blått tak, raudt
>   skiltband og sidevindauge. Brukaren har ikkje sagt kva for bygningar han meinte; spør før fleire blir teikna om.
> - *Høgtider på kartet*: `MapPen.drawFeast` i `MapArt.kt` (graskar, juletre med pakker, påskeegg ved kvar stad),
>   `festival` går gjennom `rememberMapLayer`/`buildMapLayer`/`MapLayer` som årstida.
> - *Møbelmenyen* (brukaren: «kan bli finare enn nokre boksar rundt tingen»): `Tile` i `DesignerPanel.kt` har
>   `TileLook.ROOM` (vegg i fem pastellfargar, tregolv som byrjar rett over føtene på møbelet, sjå
>   `fixtureThumbFeet` i `Thumbs.kt`) og `TileLook.BOX` (flyttekasse framfor møbelet), og gir etter ved trykk.
>
>   Brukaren ville ikkje ha flyttekasser i lageret («Enklare»), så `TileLook.BOX` er bytt ut med `SOFT`.
> - *Rom og hus* (brukaren: «alle romma / hus kan bli betre og kulare»): ny fil `ui/art/CutawayArt.kt`, kalla frå
>   `drawPlaceBack` (`cutaway`) og frå `mineInteriorBack` (`cutawayRoom` per rom som står). Alt over scenehøgd 0 blir
>   teikna som eit snitt: bjelkelag, så `attic` (loft, tak, himmel), `floorAbove` (hòlrom med mus og hybelkaninar,
>   og rommet over med møbelbein) eller `flatRoof` (Butikken). Synleg mest på nettbrett (0,47 einingar over taket
>   på 16:10, 0,76 på 4:3); mobil viser om lag bjelken. Bakgrunnen kostar om lag 1 ms meir per ramme.
>   Ikkje gjort: Scena, Trollhola, Romstasjonen, loftet i Storhuset. Lysekruna i hallen hang frå høgd −0,33; ho heng
>   no frå 0.
> - *Snapp* (brukaren: «bør objekta ha ein liten snap?»): `Designer.settle` (rein regel, fire nye testar i
>   `DesignerTest`), `Engine.letGo` og gliding i `moveFurniture`, markør i `drawPreviews`. `Sim.nudgeOnto` flyttar
>   ein ting som blir sleppt roleg like ved sida av ein møbeltopp, inn på han. Ingen snapp flyttar meir enn 0,12.
>
> **Kontroll:** 350 einingstestar, `lintDebug` og `assembleDebug` grøne (siste køyring). Alle 24 stadene besøkte på dag i mobilformat
> og om natta i nettbrettformat, utan krasj (bilete i `screenshots/audit-1.2.1/`, m.a. `kart-fire-arstider.jpg`,
> `sheet-day-*.jpg`, `sheet-night-*.jpg`; kart og meny i `screenshots/map-buildings/`). Stor skrift er ikkje
> gjennomgått. Tivoliet er ikkje optimalisert (om lag 10 ms teiknetid, 59 bilete i sekundet på emulatoren).
>
> **Viktig om måling:** startar ein emulatoren utan vindauge med `-gpu auto`, vel han SwiftShader
> (programvare-grafikk); då blir rammetidene 150 ms og oppover uansett kode. Start med `-gpu host` og sjekk
> `C:\Android\emulator-out.log` (`gles_mode_selected:host`). Dei eldre tala i dette dokumentet (31–34 ms i Storhuset,
> FocusEvent-ANR ved kald start) kan kome av dette og er ikkje stadfesta på ekte maskinvare.
> `MusicPlayer.VERSION`/`SoundFx.VERSION` treng **ikkje** aukast: cachen er per lyd- og temanamn, og sidan 1.0.0 er
> det berre lagt til nye lydar og tema (ingen eksisterande oppskrifter er endra).
>
> **Emulatoren:** lagringa er sett tilbake frå `files/kids-audit-original.json`, skjermmål og rotasjon er
> nullstilte, emulatoren er stoppa. Merk: i den andre økta vart denne kopien teken på nytt etter at appen hadde
> køyrt litt (SHA-256 `a0d97549…`, ikkje lenger `1aaf94f5…`), og siste gjenoppretting kom frå den same tilstanden
> (`files/rooms-original.json`). Den nøyaktige tilstanden `1aaf94f5…` er ikkje teken vare på; skilnaden er noko
> sjølvlagring medan kartet og romma vart viste. `files/tablet-polish-original.json` har SHA `9d97732e…`. For å få plass til installasjon vart dei ti gamle hjelpebygga
> (`app.trollfoss.kjeller`, `figurar`, `sesong`, `kart`, `oppe`, `hage`, `loft`, `stova`, `bygg`, `kartbase`)
> avinstallerte med `pm uninstall -k` (dataa deira ligg att). Emulatoren har berre om lag 500 MB ledig.

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
