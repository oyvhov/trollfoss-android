# Familiefigurar i den godkjende teiknefilmstilen

Brukaren gav private fotoreferansar for Eira, Olve, Tuva og Øyvind etter Eilev, og bad deretter om
«Lag alle karakterer på samme måte». Arbeidet byggjer vidare på `codex/levande-figurar` i
`C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa`. Heile figurflokken bruker alt den felles
teikninga, uttrykka og rørslemodellen frå `04c19ad`; desse fire får no òg personlege standardutsjånader.
Kjeldekode og testar for denne runden er lagra i lokal commit `9f3df4a`.

## Gjennomføring

1. Teikn kjenneteikna i den eksisterande Compose-stilen: Eira sitt lange lyse hår og sol-/blomegenser,
   Olve sine lyse lokkar og opne dongerijakke, Tuva sine brune bølgjer og lyse skjorte, Øyvind sitt
   korte gråbrune hår, korte skjegg og rosa stripete skjorte. Bruk enkle klede der fotoet ikkje viser
   heile kroppen. Ansikta skal kunne smile, le og reagere som resten av flokken.
2. Gjer frisyrar, mønster, skjegg og fargar til vanlege val i figurverkstaden. Legg nye indeksar sist.
3. Oppdater dei fire standardfigurane ved ny verd. For gamle lagringar: éin migrasjon, berre ved
   heilt urørt opphavleg namn og utsjånad. Bevar id, røyst, spelarval, plass, setar og alle tilbehøyr.
4. Utvid pakkinga av lause klede for palettfargar over 15, utan å endre gamle variantverdiar.
5. Lag teikneark frå den verkelege renderer-koden: familien, heile flokken og frisyrar ved
   yttergrensene. Bygg og kontroller lagring, klesbyte, uttrykk og mobil/nettbrett.

## Kontraktar

- Ingen foto eller nye biletfiler blir lagde i appen. Referansefotoa er berre teiknegrunnlag.
- Eilev og alle dei andre bruker same `drawPerson`, `FigurePose` og festepunkt som før.
- Hår 19–21, toppar 12–13, ekstra 9, mønster 7–9 og dei nye palettfargane er lagde til sist.
- Ein skjortekrage og saumane blir teikna over mønsteret. Jakka har eiga undertrøye i aksentfargen.
- Ny `people:family:portraits:1`-markør bevarer seinare redigeringar, også når barnet vel den gamle
  standardutsjånaden igjen. Ingen person blir sletta eller oppretta på nytt ved oppgradering.
- Gamle klesvariantar bruker framleis `stil * 16 + farge`. Fargar over 15 bruker den merkte
  utvidinga `65536 + stil * 256 + farge`, slik at av-/påkleding og lagring ikkje mistar fargen.
- Olve startar utan lue i ei ny verd så håret er synleg; luer og andre ting i ei lagra verd blir verande.
- Ingen nye strenger i barnet sitt grensesnitt. Namn på prøvearka er berre del av utviklarkontrollen.

## Kontrollstatus

- 663 JVM-testar grøne, inkludert 16 migrasjonsprøver (fire per person), kompatibilitet med gamle
  klesvariantar og av-/påkleding etter lagring med dei nye fargane.
- 98 Android-testar grøne på mobil (2400 × 1080 / 420 dpi, 51,28 s) og 98 på nettbrett
  (1920 × 1200 / 240 dpi, 58,74 s). Same delte AVD, eiga `app.trollfoss.figurar`-pakke, under lås.
  Testane er køyrde etter siste finpuss av Tuva sitt hår og lengdevalet for den korte frisyren.
- Familiearket, heile flokken (23 namngjevne + 11 dyresilhuettar), og alle 22 frisyrane med
  minst/mest lengd og fylde er visuelt gjennomgåtte. Familiearket er identisk i begge format
  (SHA-256 `7CEE52AEB780AB8D3623A04B4F773B3B55F11BA7964C9C911C275F7394CD60EA`).
- Redusert rørsle held portretta stille mellom ulike klokkeslett, men viser framleis latter.
  Dei eksisterande Android-testane kontrollerer òg felles rigg, hattar og fleire fingrar.
- Manuell kontroll: Olve ved Fossen og Eira på Garden. Eira er flytta med ekte fingerdrag;
  skiftenøkkelen følgjer handa. Tuva er sett med termosen på Heileberget. Øyvind er i Mitt hus
  i testlagringa etter navigasjonstestane; kontrollen bruker den faktiske plasseringa hans.
- Første APK-oppdatering fekk `INSTALL_FAILED_INSUFFICIENT_STORAGE`. Berre våre nyleg installerte
  `.figurar`-testpakkar blei fjerna og installerte att; andre appar og lagringar er ikkje rørt.
- Første manuelle oppstart gav ANR ved venting på fokus. Maskina hadde då ca. 325 MiB ledig minne.
  Etter at vår isolerte Gradle-daemon var stoppa og testappen starta om, gjekk scenekontrollen vidare.
  Dette dokumenterer ei vellykka ny prøve, ikkje ei retting eller sikker årsak til oppstartsproblemet.
  ANR kom att ved seinare sceneskifte. Fleire automatisk namngjevne scenebilete viser dermed den
  førre staden eller ein dialog og er ikkje godkjende skjermkontrollar. Klare bilete er mellom anna
  `phone-forest.png`, `phone-farm-retry.png`, `phone-eira-drag.png`, `quiet/tablet-farm.png` og
  `quiet/phone-heileberget.png`. Øyvind er synleg i `quiet/phone-mine_yard.png`, men den globale
  gåveknappen ligg over ansiktet. `quiet-offset/` dokumenterer nye ANR-forsøk. `last-anr.txt` er lagra.
- Prøve-APK: `Trollfoss-familie-proeve.apk`, 1.15.0-debug / kode 20, separat debug-pakke.
  SHA-256 `C02BBA47DE7114B5D8DFCE3E180ACECBC893EFAA54EFDD461F71553507E581AA`.
- Testpakkane er fjerna frå emulatoren. Skjermmål (2400 × 1080), tettleik (420), systemskrift (2,0)
  og animasjonsinnstilling (0) er tilbake til utgangspunktet. Emulatoren er ikkje stoppa.
- Endeleg `testDebugUnitTest` og `lintDebug`: BUILD SUCCESSFUL (12 min 41 s). 663 testar utan feil
  eller hoppa-over-prøver; lint 0 feil og 32 åtvaringar. ID, alvor og melding for alle åtvaringar
  er identiske med før denne runden. Rapportar og JUnit-XML ligg i arkivet.
  Bygget brukte eige Gradle-daemonregister; vår daemon blei stoppa etter avslutta kontroll.

## Avgrensingar frå før arbeidet

Den delte AVD-en har tidlegare hatt oppstarts-ANR og avklipt figurverkstad ved systemskrift 2,0.
Figurarbeidet friskmelder ikkje dette, fysisk eining, signert oppdatering eller barnetest. Ingen
offentleg release eller push er bestilt. Arkiv for denne runden: `C:/topa/dist/familie-figurar/`.
