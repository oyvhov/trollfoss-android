# Levande figurar – den godkjende teiknefilmstilen

Brukaren valde eit nytt visuelt uttrykk for alle figurtypane, godkjende arket med Hedda, katten,
Rolf og Sture, og bad om Eilev òg. To privat delte foto gir referanse for Eilev sitt bølgjete,
lysebrune hår, grønkvite fotballtrøye og limegrøne sko. Fotoa blir ikkje lagde i kjeldekode eller APK.
Ingen logoar eller sponsortekst er tekne med. Konseptbileta er referanse; all spelgrafikk er framleis kode.

Arbeidskopi: `C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa`, grein `codex/levande-figurar`,
frå `b957cd0` (lokal 1.15, inkludert dei hemmelege fossane). Ingen push eller publisering er bestilt.
Implementeringa er lagra i lokal commit `04c19ad`.

## Plan og gjennomføring

1. Ta eit samanlikningsark med den gamle teiknekoden og fast klokke før endringar.
2. Teikn dei fem prøvene i Compose: forma kinn og hake, mjukare ledd, tydelegare hender og sko,
   livleg blikk, glede og små hovudrørsler. Eilev får ein eigen bølgjete frisyre og fotballklede.
3. Bruk den same rørslemodellen til teikning og festepunkt for klede og leiker.
4. Før formspråket vidare til alle dei 14 artane; bevar eigne øyre, halar, venger, horn og snutar.
5. Kontroller lagringar, tilbehøyr, boklesing, ball-leik, løfting, sitjing, ligging og redusert rørsle.
6. Bygg lokalt og køyr einings- og Android-testar i mobil- og nettbrettformat. Sjå dei faktiske
   kodeteikningane og appen, og arkiver bilete, APK og kontrollresultat.

## Kontraktar

- `FigurePose` er den felles rørslemodellen. `Anatomy.at` og `figureHead` bruker same hovudtransform.
  Frie hender kan vinke; ei opptatt hand held aktiviteten sin. Mottak av ein ball reserverer ei roleg
  fangsthand, så eit vink ikkje flyttar mottakspunktet bort frå ballbana.
- Rolf bøyer hovudet medan brettet held seg vassrett. Sture held fram med å sveve og bruke lakenarmane.
- Hattar og briller får både posisjon og vinkel frå hovudet, også når figuren ligg.
- Nye hår- og klesval blir lagde til sist i palettane. Eksisterande indeksar og identitetar er bevarte.
- Eilev sin migrasjon går éin gong, berre når namn og heile utsjånaden svarar til den opphavlege
  standardfiguren. Barnet sine endringar, tilbehøyr, spelarval, røyst og plassering blir bevarte.
- Redusert rørsle stansar dekorative klokker og tilfeldig vinking/dansing. Nye klede og aktive
  reaksjonar oppdaterer framleis teiknebufferen. Ingen ny tekst er lagd til i barnet sitt grensesnitt.
- Ingen nye runtime-avhengnader. `compose-performance` og `compose-ui-testing-patterns` er installerte
  som lokale Codex-skills frå chrisbanes/skills (pinna revisjon 00bda528b7a13fb6ba475b0d50e50589aa5d274f).

## Kontroll

- Einingsbygg: 645 testar, 0 feil, feilavbrot eller hoppa over (2026-10-09). Nye testar for
  rørslekontrakt, spegelvending og Eilev-migrasjon.
- Android: 96 testar grøne på mobil (2400 × 1080 / 420 dpi) og 96 på nettbrett (1920 × 1200 / 240 dpi).
  Same delte AVD i to format, eiga pakke `app.trollfoss.figurar`, emulatorendringar under lås.
  Første nettbrettforsøk fekk ein oppstarts-ANR før nokon test; neste forsøk fullførte på 55,33 sekund.
- Visuelt gjennomgått: alle 14 artane i seks stillingar, 19 frisyrar ved yttergrensene for lengd og
  fylde, klesvariantar, redusert rørsle og prøvearket med dei fem figurane.
- Faktisk app: Eilev er sett på Scena i begge format, og flytta frå scena til golvet med fingerdrag
  på mobil. Bileta `phone-stage-now.png`, `phone-eilev-drag.png` og `tablet-stage.png` viser dette.
  Android sin Pixel Launcher viste først gjentekne ANR-dialogar; scenekontrollen fullførte etter at
  dialogen var lukka. Dette friskmelder ikkje kaldstart eller heile menyflyten.
- Siste retting i figurverkstaden bevarer ein observert rammeklokke også med rørsle av, slik at ei
  aktiv ansiktsreaksjon ikkje blir ståande når tida går ut. Dette er prøvd med ekte trykk etter nytt
  APK-bygg; `creator-reduced-reaction.png` og `creator-reduced-calm.png` viser reaksjon og vanleg smil
  tre sekund seinare. Dei 96 Android-testane per format er køyrde før denne avgrensa skjermrettinga.
- Lint: endeleg kontroll fullførte 2026-10-09 kl. 02:20 med 0 feil og 32 åtvaringar. Alle varsel-ID-ar,
  alvorlegheitsgrader og meldingar er dei same som i rapporten frå før arbeidet. Rapportane er arkiverte.
  Tidlegare forsøk blei avbrotne med «stop command received» frå den delte Gradle-daemonen. Den endelege
  kontrollen brukte same byggelås og cache, men ein eigen mellombels Gradle-heim
  (`C:/topa/.gradle-tmp/figurar-gradle-home`) med skilt daemonregister og 5 GiB heap; siste einings- og
  lintkøyring fullførte på 15 minutt 57 sekund. Eit tidlegare isolert forsøk blei stoppa av oss for
  verkstadrettinga. Ingen lintreglar er slått av.
- Programvareteikning av ti figurar på bitmap, 12 oppvarmingar og 60 prøver: gamal median/p95
  182,42/394,75 ms; ny mobil 464,39/946,00 ms under samtidig lint og tung maskinlast; ny nettbrett
  98,04/335,76 ms etter at bygginga stansa. Last og oppsett varierer for mykje til å konkludere om
  betring eller tilbakegang. Desse tala er ikkje målt spel-FPS.
- Arkiv: `C:/topa/dist/levande-figurar/`. `FigureStyleArtTest` lagar faktiske teikneark, alle artar/stillingar,
  verkstadvariantar og Eilev-portrett. `FigureRigUiTest` prøver oppdatering med rørsle av og å plukke
  hatten frå eit skrått hovud. Dei eksisterande testane dekkjer også fleire samtidige fingrar.
- Prøve-APK: `Trollfoss-figurar-proeve.apk`, versjon 1.15.0 / kode 20, eiga debug-pakke som kan
  installerast ved sida av vanleg Trollfoss. SHA-256:
  `8B56AEA08DDF6D2E824698362198AF393532CC232F8BA5E5EDF11225629CF36C`.
- Begge testpakkane er fjerna frå emulatoren etter kontrollen. Skjermoppsett og innstillinga for
  animasjonar er tilbakeførte til utgangspunktet; emulatoren er ikkje stoppa.

## Kjende avgrensingar frå før arbeidet

Oppstarts-ANR på den delte emulatoren er rapportert frå tidlegare arbeid. Årsaka er ikkje fastslått.
Ein grøn isolert teiknetest åleine friskmelder ikkje oppstart, heile menyflyten, yting på fysisk
Android-eining eller ei signert oppdatering. Ingen GitHub-CI, publisering eller barnetest i denne runden.
Den delte AVD-en hadde skriftstorleik 2,0 under manuell kontroll: verkstaden har då frå før for lite
plass til delar av valpanelet og Ferdig-knappen i mobilformat. Denne større skjermtilpassinga er ikkje
del av figurarbeidet, og full menyflyt ved stor systemskrift må framleis prøvast før release.

## Verktøyreferansar

- Dei installerte Compose-skillsa: [chrisbanes/skills, pinna revisjon](https://github.com/chrisbanes/skills/tree/00bda528b7a13fb6ba475b0d50e50589aa5d274f).
- Gradle dokumenterer [eigen brukarheim med `-g`](https://docs.gradle.org/current/userguide/command_line_interface.html)
  og [daemonregister i brukarheimen](https://docs.gradle.org/current/userguide/gradle_directories.html).
