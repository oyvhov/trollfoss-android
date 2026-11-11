# Juleutvidinga – lokal sluttkontroll 11. november 2026

## Leveranse

Arbeidskopi `C:/trollfoss-wt/christmas`, grein `codex/christmas`. Grunnlaget er `906bdf2`,
med tidlegare lokale Heileberget-/husendringar bevarte separat i `9e501cb`.
Originalane i `C:/topa` og `rainbow-finale` er urørte. Innhaldskontrakten står i `docs/JUL.md`.

Prøvepakken heiter `app.trollfoss.christmas`, versjon 1.17.0-debug (22). Han kan installerast
ved sida av den vanlege appen. APK, test-APK, loggar, biletark og skjermopptak ligg i
`C:/topa/dist/christmas`. `BUILD-MANIFEST.json` oppgir endeleg kjeldecommit og SHA-256.
App-APK SHA-256: `09ca1a5210f3ad2ef9baa8a910d2c4de3efa16ed7f6709a3833ef5600f42372d`.

Ingen versjonsauke, signert release, push eller GitHub-publisering er utført.
Signeringsnøklar og andre arbeidskopiar er ikkje endra.

## Endelege resultat

| Kontroll | Resultat |
| --- | --- |
| `testDebugUnitTest` | 761 testar, 0 feil, 0 hoppa over |
| Debug-APK og Android-test-APK | Bygde utan feil |
| Mobil, full Android-suite | 119 testar, 0 feil; 118 bestått og éi opt-in måling hoppa over |
| Nettbrett, full Android-suite | 119 testar, 0 feil; 118 bestått og éi opt-in måling hoppa over |
| Lint på endeleg kode | 0 feil / 33 åtvaringar, med `ThreadConstraint` utelaten |
| `git diff --check` og UTF-8-kontroll | Kontrollert før commit |

Bygging bruker `C:/topa/scripts/Build-Locked.ps1` med `-Suffix .christmas`.
Emulatoroperasjonar bruker `Run-Locked.ps1 -Name emulator`. Ingen fjerntliggjande CI er køyrd.
Android-loggane er `android-mobile-full.txt` og `android-tablet-full.txt`.
Den medvitne hoppa-over-testen er `ScenePerformanceTest`, som krev `measureScenes=true`.

## Kva testane beviser

Dei 27 nye domenetestane dekkjer katalogen med 180 val, høgtidsgrenser og sommarjul,
migrering av innstillingar og eksisterande verd, gyldige vegar attende frå Nissegrenda,
24 frie kalenderluker utan doble gåver, og innpakka gåver som gir tilbake same ting-ID.
Dei dekkjer òg innhald ved sletting, strømper, sekk og omstart, tre med tolv pynteting,
avtaking, lager/papirkorg og gjenbruk av møbel-ID-ar, baking i fleire steg og etter omstart,
peparkakehus, leikebygging og mengdegrenser utan tapt materiale.

Vidare er faktiske passasjerar og gåver i sleden, Rampenissen sin identitet, heimreise frå
julefest, bjølla til nissen, Rolf sin faktiske kakaokopp, ønskjekort, reinsdyrbørsting og
angring av innpakking/strømper kontrollerte. Oppdagingar blir bevarte ved angre.

`ChristmasEngineTest` bruker verkelege peikarhendingar i mobil- og nettbrettmål:
pynting og avtaking, to samtidige innpakkingar, kalenderkontroll og musikk/dans gjennom
faktiske rammer. Dei seks jule-Android-testane er med i begge dei fulle køyringane.
`ChristmasArtTest` registrerer åtte biletark; bileta blir vurderte separat frå funksjonspåstandane.

## Visuell kontroll og vanlege trykk

`contact-sheets/` viser dei 108 møbel-/pyntetilboda, småting, klede, veggar/golv og utsnitt
frå dei fire områda. Kontaktark beviser teikneruting og utval, og viser ikkje full appflyt.
`mobile/` og `tablet/` har fullappbilete frå Nissetunet, Nisseheimen, Juleverkstaden,
Reinsdyrtunet, Storhuset, Mitt hus, Heileberget, kart og natt.

På mobilen vart den verkelege kalenderen opna ved trykk i scena. Trykk på luke 1 vart
kontrollert mot den faktisk lagra JSON-fila. Juleklede vart viste, «Jul heile året» vart
valt og kontrollert som lagra `CHRISTMAS`, og den raude lukkeknappen vart brukt.
`mobile/ui-controls.txt` og PNG-ane dokumenterer dette. Prøvelagringa vart gjenoppretta
byte for byte etter kontrollen. Alle opptak bruker berre den isolerte prøvepakken si lagring.

På nettbrett var alle 24 lukene synlege samtidig. Luke 24 vart opna før jul og kontrollert
som lagra. Juleklede, «Jul heile året» og retur til scena med raud X vart kontrollerte på
same måte; sjå `tablet/ui-controls.txt`. Begge prøvelagringane vart gjenoppretta byte for
byte. Den eigne emulatoren vart sett tilbake til standard skjermmål og stoppa etter kontrollen.

Automatisk UiAutomator-dumping vart forsøkt, men fekk ikkje eit stabilt XML-tre medan scena
animerte. Menykontrollen vart difor gjort med vanlege, observerte skjermtrykk og lagringskontroll.
Dette er ein avgrensing i opptaksmetoden, ikkje ein bestått automatisk UiAutomator-test.

## Miljø og feil funne under kontrollen

Den pålagde `Start-TrollfossTablet.ps1 -SkipInstall` starta Spole_Instrumentation på 5562,
men installasjonen feila med «Requested internal only, but not enough space». Ingen appar
eller lagringar vart sletta. Emulatoren denne økta starta, vart stoppa. Nettbrettkontrollen
bruker difor den eigne `TrollfossChristmas`-emulatoren på 5554, Android 36, i nøyaktig
1920 × 1200 / 240 dpi. Mobilprofilen er 1080 × 2400 / 420 dpi; appen køyrer liggjande.

Første breie mobilrunde fann to feil: ein `captureToImage`-frist i husvaltesten og retur frå
Heileberget etter stopp/start av testaktiviteten. Diagnosebileta for den siste viste at
aktiviteten stod i smalt ståande format, og den ekstra juleknappen gjorde at figurrada dekte
kartknappen. HighlandsScreen reserverer no kartknappen sin plass og lèt figurknappane rulle
innanfor resten av vindauget. Den opphavlege testen er uendra og går gjennom på begge profilar.
Dei mellombelse diagnoseendringane i testen er fjerna. Første feil er bevarte i eigne loggar.

Programvareteikning med automatisk SwiftShader/lavapipe gav òg SystemUI- og oppstarts-ANR.
Sporet i `startup-anr.zip` viser Android HardwareRenderer.setStopped medan RenderThread
ventar i emulatoren sitt qemu/GLES glCreateProgram. Den eigne emulatoren vart starta med
`-gpu host`, som bruker NVIDIA RTX 3060 gjennom Android OpenGL ES Translator. Endelege
Android-køyringar og fullappbilete bruker denne profilen. APK-en vart ikkje endra for miljøbytet.
Dei første svarte bileta og ANR-dialogane tel ikkje som godkjende visuelle opptak.

## Lint og avgrensingar

Full lint vart avbroten etter om lag 24 minutt utan resultat i JoinEffectDetector si
interprosedurale fikspunktanalyse. Første avgrensa køyring gav 35 åtvaringar, der to kom
frå ei mellombels `BlockingMethod`-utelating som testmodellane ikkje kjende.

Endeleg køyring bruker berre `ThreadConstraint`-utelatinga via ei lokal Gradle-initfil,
ikkje ei endring av prosjektet sine lint-reglar. Ho vart ferdig på 9 minutt 36 sekund:
0 feil og 33 åtvaringar. 32 gjeld grunnlaget; éi ny åtvaring gjeld bruk av prosjektet sitt
`Configuration.screenHeightDp`-mønster i ChristmasPanel. Rapportane ligg i leveransemappa.
Full lint med trådanalysen er altså ikkje stadfesta som grøn.

Ingen fysisk telefon eller nettbrett er testa. Det blir ikkje hevda målt FPS på fysisk
maskinvare eller eit publiseringsklart releasebygg. Dette er ei lokal, funksjonstesta prøveutgåve.
