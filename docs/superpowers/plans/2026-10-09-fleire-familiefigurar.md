# Fleire familiefigurar etter fotoreferansar

Brukaren bad om same behandling av neste familiegruppe. Bilete 1–3 er Sondre, Elise og Sølve.
Brukaren presiserte at bilete 4 er Hedda og stadfesta at det siste, ettersende biletet er Olvar.
Bilete 5 er tolka som Berit; dette er sagt tydeleg i samtalen. Bilete av Sander manglar, så hans eksisterande
figur blir verande til referansen kjem. Referansefotoa blir ikkje kopierte inn i kjeldekode eller APK.
Kjeldekode og testar er lagra i lokal commit `fd15f50` på `codex/levande-figurar`.

## Arbeidet

1. Teikn seks personlege standardutsjånader i den godkjende, felles Compose-stilen.
2. Gjer særtrekka til val i figurverkstaden: bakovergreidd kort hår, langt midtskilje, lågt knytt hår,
   mjuk pannelugg, fyldig skjegg, glidelåsgenser/fleece, blomejakke og nye naturlege fargar.
3. Legg nye indeksar sist. Berit sine briller er variant 1 av eksisterande brilletypen; variant 0
   beheld den gamle teikninga. Brillene er ein vanleg laus ting som barnet kan flytte og ta av.
4. Bruk ein ny migrasjonsmarkør, `people:family:portraits:2`, slik at tidlegare oppgraderte lagringar
   også får desse portretta. Urørte namn/utsjånader blir oppdaterte; andre blir bevarte.
5. Bevar spelarval, person-id, røyst, stad, stol/seng, hatt og det figuren held. Berit sine briller
   blir lagde til etter alle opphavlege personar/ting i ei ny verd, slik at eksisterande id-ar står.
   Eigne briller blir aldri bytte ut, og briller som barnet tek av, kjem ikkje automatisk tilbake.
6. Render prøveark frå den faktiske teiknekoden, sjekk hårslidarar og uttrykk, lagring/klesbyte,
   mobil og nettbrett. Bygg ei eiga prøvepakke; ingen push eller release er bestilt.

## Kontrollstatus

- Bygg, test-APK og debug-APK: grøne. Etter siste smil-/panneluggjustering tok bygget 45 sekund.
- 689 JVM-testar: ingen feil eller hoppa-over-prøver. 24 nye portrettprøver dekkjer dei seks personane,
  og to brilleprøver dekkjer overføring, lagring, ingen duplikat og bevaring av barnet sine briller.
  Første køyring fann berre at eksisterande oppteljing av startting mangla det nye brilleparet;
  testen tel no dette paret eksplisitt, medan alle gamle startting framleis skal vere der.
- 99 Android-testar bestod på mobil (2400 × 1080 / 420 dpi, 50,800 s) og på nettbrett
  (1920 × 1200 / 240 dpi, 58,222 s). Begge blei køyrde etter siste teikneendring.
- Dei seks nye portretta, heile figurflokken og alle 26 frisyrane ved minste/største lengd og fylde
  er visuelt gjennomgåtte. Portrettarket frå mobil/nettbrett er identisk:
  SHA-256 `CA188FA1B07388DDE3270760A7FA2D50CE542BCD40CCFCA8CB09D917B1EA6417`.
- Portrettprøvene dekkjer alle elleve fotobaserte figurar: redusert rørsle held dei stille, medan
  latter framleis endrar uttrykket. Brillene blir teikna med same feste og hovudrørsle som i spelet.
- Berit er manuelt sett i Butikken på mobil, med rette briller og smil (`phone-shop.png`).
  Nettbrettoppstarten gav svart skjerm (`tablet-shop.png`) og fokus-ANR kl. 13:18:53, lagra i
  `anr-events.txt`. Dette er ikkje ein godkjend manuell nettbrettgjennomgang, sjølv om alle
  instrumenterte testar og portrettark på nettbrett bestod. Den kjende oppstartsfeilen er ikkje retta.
- Emulatoren hadde for lite plass til APK-oppdatering. Berre våre eigne `.figurar`/`.figurar.test`
  frå denne oppgåva blei fjerna og installerte på nytt. Etter kontrollen er dei fjerna att.
  Storleik 2400 × 1080, fysisk tettleik 420 utan overstyring, systemskrift 2,0 og animasjon 0 er
  tilbake til utgangspunktet. Den delte emulatoren er ikkje stoppa.
- Prøve-APK: `C:/topa/dist/fleire-familiefigurar/Trollfoss-fleire-familiefigurar-proeve.apk`,
  `app.trollfoss.figurar`, 1.15.0-debug / kode 20.
  SHA-256 `7C7FE0108AF6DEC70CAE3A16D1E96CB39A32F115577A1D6CA44507C55BA28E55`.
- Endeleg lint bestod på 16 minutt 59 sekund: 0 feil og 32 åtvaringar. ID, alvor og melding er
  uendra frå førre familieversjon. Rapportar og bilete ligg i `C:/topa/dist/fleire-familiefigurar/`.
  `review/` viser første utkast før siste smil-/panneluggjustering; endelege bilete ligg i `phone/`
  og `tablet/`. Ingen fysisk eining, barnetest, GitHub-CI eller signert oppdatering i denne runden.

Nye indeksar: hår 22–25, toppar 14–15, ekstra 10, munn 5, mønster 10, hårfarge 18 og klesfargar
22–24. Gamle indeksar og den bakoverkompatible klespakkinga er uendra. Eit seinare portrett av Sander
treng ein ny migrasjonsmarkør; ikkje legg han inn under markøren som desse seks alt har brukt.

Den delte emulatoren har frå før tidvise ANR ved oppstart/reise og problem med figurverkstaden
ved systemskrift 2,0. Dette er ikkje del av portrettendringa og må framleis kontrollerast før release.
