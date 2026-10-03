# Synlege dører og traktor skal ta imot trykk

Brukaren melde at kjøleskapet ikkje kunne lukkast, og at traktoren ikkje starta på Scena.
Målet er samanheng mellom teikninga, trykkflata og leik med ting og figurar.

## Funna og rettingane

- Den opne kjøleskapsdøra strekte seg utanfor treffområdet. Trykk på døra trefte pianoet bak.
  Opne dørpanel bruker no same geometridata til teikning og treffkontroll. Dette omfattar
  kjøleskap, garderobe og omn, pluss kjøleskap, syltetøyskap, skjenk og garderobe i Storhuset.
- Traktortaket og øvre førarhytte låg utanfor det gamle treffområdet. På Scena starta trykk på
  rattet, medan trykk på taket ikkje gjorde det. Takmål er no delte mellom teikning og treffkontroll.
- Ting bak traktoren eller ei open dør skal ikkje ta trykket frå møbelet framfor.
  Figurar framfor, føraren og mat inne i skapet er framleis moglege å dra.
- Møblar med lik djupn blir plukka i motsett teiknerekkjefølgje, slik at det synlege fremste vinn.

Ingen nye spelreglar, tekststrengar, lagringsformat eller versjonsnummer.

## Kontroll

467 JVM-einingstestar og 32 Android-kontrollar er grøne. Dei seks nye Android-kontrollane
dekkjer opne dører, traktoren frå Lager til Scena, start på taket, køyring begge vegar med
førar, stopp, skjulte ting bak traktoren, figur framfor traktoren og mat ut av kjøleskapet.
Debug-APK, Android-test-APK og lint er bygde; lint har 0 feil og 31 åtvaringar.

På eiga prøveverd vart traktoren faktisk flytta frå garden via Lager til Scena.
Før-/etterkontroll viser kjøleskapsdøra som lukkar og traktortaket som opnar køyreknappane.
Nettbrett er hovudflata; mobil får berre ein kort kontroll av den same interaksjonen.
Bilete, UI-tre og Android-resultat ligg lokalt i `screenshots/interaction-fixes-2026-10-03/`.
GitHub-kontrollane er knytte til PR 4. Endringane er ikkje publiserte som APK-release.

Dette er ikkje ein komplett kontroll av alle flyttbare møbeltypar i alle rom. Vidare arbeid
bør følgje same prinsipp: synleg form og trykkflate må samsvare, og bruk må fungere saman
med flytting, lagring, innhald og figurar.
