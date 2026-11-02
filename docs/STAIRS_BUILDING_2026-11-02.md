# Trapper og husbygging – 2. november 2026

Arbeidskopi: `C:/topa/.gradle-tmp/rainbow-finale` (junction til
`C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa`). Grein: `codex/stairs-building-redesign`,
frå `ef4cef5` med ytingsforbetringane frå førre runde.

## Endringane

- Alle trappene i Storhuset og Mitt hus har fått lyst tre, opne trinn, lyse stolpar og dempa
  grønt rekkverk. Same teiknesett gjer uttrykket samanhengande. Golvopningane har fri inngang
  og tydelege ned-piler; dørbogane viser vegen opp.
- Dør- og golvopningane bruker dei eksisterande `StairDoorways`-ankera. Passasjar, fixtur-ID-ar,
  byggjereglar og lagringsformat er uendra. Pila i Mitt hus heng innanfor trappa sitt treffområde.
- Husval er no «Vel → Sjå → Bygg». Kortet vel berre eit førehandssyn. Barnet kan byte hus,
  og berre «Bygg huset» startar jobben. Pågåande bygging sperrar begge knappane.
- Romkortet får eit eige førehandssyn, ein samla knapp med hammar og tekst og «Vel eit anna rom».
  Etter bygging kjem leik og neste rom først; riving er ei rolegare sekundær handling med den
  eksisterande stadfestinga og lagring av møblar.
- Lys krembakgrunn, mjukare kort, grøn valmarkering og kompakt mobiloversikt over etasjane.
  Den raude X-en er felles lukking. Alle nye ord finst på nynorsk og bokmål.
- Ingen nye biletfiler, bibliotek eller kontinuerlege animasjonar. Kjellartrappa sine rørsler i
  støv og lykt er erstatta av stilleståande detaljar, slik at trappene kan liggje i sprite-bufferen.
  Denne runden måler ikkje ny FPS-gevinst på fysiske einingar.

## Kontroll

- 701 JVM-testar utan feil. Lint: 0 feil, 32 eksisterande åtvaringar.
- 106 Android-testar på kvar av mobil (2400 × 1080, 420 dpi) og nettbrett (1920 × 1200, 240 dpi).
- Dei tre nye testane er køyrde på nytt på begge format etter siste finpuss; alle grøne.
- Nye Compose-testar: husval endrar ikkje verda før bygg, byte av hus gir rett mal, og pågåande
  bygging sperrar nye trykk. Både nynorsk og bokmål er med.
- Teikneprøve: alle trappevariantane er identiske ved to klokkeslett; dørtreff bruker dei same ankera.
- Faktiske trykk på mobil: vel villa → førehandssyn → bygg huset → gå inn → vel stove → bygg.
- Faktiske trykk på nettbrett: vel kjøken → førehandssyn → bygg → etasjefana → bygg andre etasje.
  Ny etasje viser «Etasjen er klar». Trykk på trappepila i Storhuset fører opp, og ned-pila fører tilbake. Opp-pila på trappa i Mitt hus er òg prøvd.
- Det lagra huset før og etter installasjon av nyare prøve-APK er samanlikna: identisk mal,
  utsjånad, rom og etasjestatus før neste byggjehandling. Førehandssyn av kjøken endra ikkje lagringa.
- Skjermbileta er visuelt kontrollerte. Avkorta langt husnamn og for høg mobiloversikt vart retta.

Arkiv med APK, byggloggar, testloggar, skjermbilete og prøveskript:
`C:/topa/dist/stairs-building-2026-11-02/`.
Prøvepakke: `app.trollfoss.redesign`, eiga generert verd. Ingen fysisk eining er rørt.
Android-prøvene brukte éin Linux/KVM-emulator gjennom den delte emulatorlåsen; Stova_TV vart
stoppa mellombels med løyvet frå brukaren. Prøvepakkane er fjerna, skjermstorleiken er nullstilt,
eigen emulator er stoppa og Stova_TV er starta att.

Dette er lokalt arbeid, ikkje publisert release. Versjon er framleis 1.17.0 / 22.
GitHub-CI er ikkje køyrd i denne runden. Rotkopien sine eldre uferdige kodeendringar er urørte.
