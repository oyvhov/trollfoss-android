# Sander etter fotoreferansen

Brukaren sende det siste manglande familiefotoet og merkte det Sander. Han får same godkjende
Compose-stil som dei elleve andre fotoportretta: kort bakovergreidd brunt hår, breitt smil,
lett skjeggstubb og blå vattert jakke med mørkt lag under. Fotoet blir ikkje bygd inn i appen.

## Gjennomføring

- Gjenbruk frisyre 22 og smil 5; legg jakke 16, ekstra 11 og klesfarge 25 sist i listene.
- Teikn lesbare vatterte band, krage og glidelås. Ermsaumar følgjer dei bøygde armane når han vinkar.
- Bruk ein eigen markør, `people:sander:portrait:1`, etter begge familierundane. Berre urørt
  gammal Sander blir oppdatert; namn, spelarval, røyst, plass, stol og tinga hans blir bevarte.
- Ta Sander med i dei eksisterande migrasjons-, klesbyte- og portrettprøvene. Lag eit eige
  prøveark og oppdater familiearket/heile flokken frå den faktiske teiknekoden.
- Bygg prøve-APK og kontroller mobil og nettbrett. Ingen push eller publisering er bestilt.

## Kontroll

- Kjeldekode/testar: lokal commit `dcad7f2` på `codex/levande-figurar` i
  `C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa` (ASCII-kopling `C:/topa/.gradle-tmp/rainbow-finale`).
- Bygg og APK-ar bestod på 3 minutt 19 sekund. 693 JVM-testar: 0 feil, 0 hoppa over.
  Sander er lagt til dei fire eksisterande migrasjonsprøvene, inkludert oppgradering etter begge
  familieoppdateringane. Den eksisterande klesbyte-/lagringsprøva dekkjer no òg jakka og blåfargen hans.
- 100 Android-testar bestod på mobil (2400 × 1080 / 420 dpi, 102,133 s) og nettbrett
  (1920 × 1200 / 240 dpi, 102,462 s). Alle tolv fotoportretta bevarer uttrykka med redusert rørsle.
- `phone/sander-portrait.png` er visuelt kontrollert, med smil og vink. Begge format gir same bilete:
  SHA-256 `31D3E1710DC528D0B51F28980A0C1A5AB68F52DCDC7E917C9F61FC3596AC4B92`.
  Det oppdaterte familiearket med sju figurar er òg visuelt gjennomgått. Heile flokken er rendra på nytt.
- Dei manuelle frisørprøvene kom ikkje fram til scena: mobil viste oppstartsbiletet, nettbrett svart
  skjerm. Nye fokus-ANR-ar kl. 17:13:09 og 17:13:35 er lagra i `anr-events.txt`. Oppstarten er ikkje
  friskmeld; instrumenterte teikneprøver er ikkje ei erstatning for godkjend manuell oppstarts-/reiseflyt.
- Emulatoren hadde om lag 335 MB ledig. Installasjonen gjekk under lågplassgrensa; vanleg tøming
  av hurtiglager gav ikkje meir plass. Under emulatorlåsen blei `sys_storage_threshold_max_bytes`
  mellombels sett til 128 MiB. Etter prøvene blei berre dei eigne `.figurar`/`.figurar.test`-pakkane
  fjerna, og reserveinnstillinga sletta att til opphavleg `null`. Ingen andre appar blei avinstallerte.
  Skjermmål 2400 × 1080, fysisk 420 dpi utan overstyring, skrift 2,0 og animasjon 0 er tilbakeførte.
  Den delte emulatoren køyrer framleis. Berre vårt eige Gradle-daemonregister blei stoppa etter bygget.
- Prøve-APK: `C:/topa/dist/sander-figur/Trollfoss-med-Sander-proeve.apk`,
  `app.trollfoss.figurar`, 1.15.0-debug / kode 20, universal debugpakke.
  SHA-256 `9A41288FBE6313334B4A6B13BABF415FE4E136ADED4495BDAA0B3D3072AE6A0B`.
- Rapportar, bilete og hjelpeskript ligg i `C:/topa/dist/sander-figur/`. `git diff --check` bestod.
  Lint er ikkje køyrd på nytt i denne portrettrunden. Førre lintresultat gjeld førre kjeldekode,
  ikkje denne endringa. Ingen fysisk eining, barnetest, GitHub-CI, push eller signert release.

Alle foto som er sende inn i denne samtalen er no brukte til figurportrett. Bilete 4 frå førre gruppe
er Hedda og siste ettersende er Olvar, begge stadfesta av brukaren. Bilete 5 er framleis tolka som Berit.
