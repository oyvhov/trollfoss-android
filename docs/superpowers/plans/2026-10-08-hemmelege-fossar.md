# Hemmelege inngangar bak fossane

Brukaren bad om at den store fossen på kartet og fossen inne på ein stad skal vere hemmelege plassar.
Den indre fossen er tolka som fossen på skogstaden Fossen. Begge opnar same eksisterande Trollhola.
Arbeidet byggjer vidare på `100662d`, på `codex/rumles-skattar`. Versjon 1.15.0 / kode 20 er uendra.

## Gjort

- Trykk på den store kartfossen opnar Trollhola. Oppdagingsboka er framleis tilgjengeleg via bok- og framgangsknappane.
- Trollhola har ikkje vanleg kartskilt eller synleg grottelandemerke. Fossen/skogen er framleis eit vanleg reisemål.
- Trykk på sjølve vatnet i skogen går inn i hola. Eit lite glimt viser at vatnet svarar.
- Vassgardina inne i hola har ei lita pil og fører ut att til tørr grunn i skogen. Utgangen er synleg ved ankomst.
- Spelarane, kleda og handhaldne ting følgjer med gjennom eksisterande navigasjon. Ingen nye kopiar av hola, Rumle eller ting.
- Boka skjuler hola fram til første besøk. Reisehint, mellom anna frå Rumle-jakta, fører først til fossen dersom hola ikkje er oppdaga.
- Gamle besøk blir respekterte. Tunnelen frå Storhuset er framleis ein inngang, og den oppdaga kartstien endar ved fossen.
- Sveip panorerer; ein annan aktiv finger, open sekk og møbelmodus hindrar utilsikta reise. Nye skjermlesarhandlingar finst på begge målformer.
- Redusert rørsle held glimtet stille. Ingen lagringsformat eller enum-rekkjefølgjer er endra.

## Kontroll

- `testDebugUnitTest`, `assembleDebug` og `assembleDebugAndroidTest` gjennom `Build-Locked.ps1`: grøne.
- 634 JVM-testar, 0 feil. Lagring av oppdaging og uendra figur-/ting-ID-ar er med.
- 89 Android-testar på kvar av nettbrett (1920 × 1200 / 240 dpi) og mobil (2400 × 1080 / 420 dpi): grøne.
- Nye Android-kontrollar trykkjer på ekte scenevatn, prøver sveip og fleire fingrar, og brukar den faktiske ViewModel-navigasjonen for kartinngang, hint, utgang, spelarar og klede.
- Seks bilete frå spelmotoren er lagra. Kart med oppdaga tunnel, skogsfoss og utgang i Trollhola er visuelt gjennomgåtte.
- Vanleg skjermprøve: den delte emulatoren gav igjen oppstarts-ANR og tidvis manglande UI-hierarki. Kartet vart synleg på nettbrett; dette stadfestar ikkje at oppstartsproblemet er løyst.
- Etter oppstart vart heile turen prøvd med faktiske skjermtrykk på mobil/bokmål: stor kartfoss → Trollhola → utgangspil → skogen → skogsfossen → Trollhola. Ingen debug-reise mellom stega. Skjermbilete viser begge spelarane og begge scenene; lagringa etterpå stadfestar `LAB`, besøka og dei same spelar-ID-ane 25/45. På nettbrett er reisene kontrollerte gjennom Android-testane, ikkje ein full manuell skjermrunde.

- `lintDebug` er grøn: 0 feil / 32 åtvaringar. Kontrollen tok 16 minutt og 36 sekund.

## Leveranse og avgrensing

Isolert testpakke `app.trollfoss.foss`; den vanlege appen og den private lagringa er urørte.
Begge testpakkane er avinstallerte, skjermmål/rotasjon/skriftstorleik/animasjonsval er tilbakeførte,
og den delte emulatoren er framleis i gang. Berre `emulator-5554` vart brukt; ingen omstart.
APK, test-APK og kontrollfiler: `C:/topa/dist/hemmelege-fossar/`.
Skjermbilete og prøveverder: Git-ignorert `C:/topa/screenshots/hemmelege-fossar/`.
Ingen push, GitHub-CI, signert oppdatering, fysisk eining, barnetest eller publisering i denne runden.
Oppstartsproblemet frå førre arbeidsrunde må framleis avklarast før release.
