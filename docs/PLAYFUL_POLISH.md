# Typografi, spelarfargar og støvsugarleik

Gjennomført etter brukaren sin design- og forbetringsguide 2026-10-04, på grunnlag av publisert
1.9.0 (`origin/main`, 7b32c87). Arbeidet ligg i `codex/playful-polish` i den tilknytte
arbeidskopien `C:\Users\Øyvind\.codex\worktrees\playful-polish\topa`.

## Endringar

- Lokale Nunito-fontar i 400/700/800 og Fredoka i 700. Ingen fontnedlasting i appen.
  Opphav, fast kjeldecommit og SHA-256 ligg i `app/src/main/assets/font-licenses/sources.txt`,
  saman med begge SIL OFL-lisensane. Fontressursane er til saman 446 964 byte.
- Tynnare `GameText`-omriss, rolegare kartskilt, kremfarga spelar- og angreknapp.
  Dei eksisterande stadnamna og reisevala er framleis synlege.
- Felles spelarfargar og nummer i figurval, portrett og kortvarige scenemerke.
  Fleire fingrar kan halde kvar sitt merke i live. Dei blir ikkje med på foto.
- Støvsugaren har éin vanleg sitjeplass for ein katt eller annan figur. Eit slepp på setet startar
  køyringa, så ein stor figur ikkje kan skjule knappen og hindre leiken. Passasjeren følgjer
  både framover og bakover, og kan løftast av att med vanleg drag. Møblar på same golvrad
  får støvsugaren til å snu; teppe og veggpynt gjer det ikkje. Løfting pausar køyringa.
  Køyringa stoppar automatisk etter den eksisterande 14-sekundsperioden.
- Lagring tek vare på støvsugaren si sideforskyving i det valfrie feltet `vacuumShift`.
  Etter ny oppstart ventar han på eit trykk, med passasjeren på same stad.
  Gamle lagringar utan feltet får null forskyving. Lageret frigjer passasjeren på vanleg vis.
  Klede, namn og medborne ting høyrer framleis til den same figuren.

Detaljerte designreglar står i `DESIGN.md` §5. Statiske bilete, lagrekkjefølgje, eksisterande
nattlys og kontaktskuggar er vidareførte; analysen skildra også problem som alt var løyste
i nyare utgåver. Nye samlesystem, skjulte troll, vedmating og levande miniatyrar er utsette.

## Kontroll

Sluttbygg med `Build-Locked.ps1 -Suffix .polish`: `testDebugUnitTest`, `lintDebug`,
`assembleDebug` og `assembleDebugAndroidTest` grøne. 522 JVM-testar; 58 Android-testar grøne
på kvart skjermformat. Lint: 0 feil / 31 åtvaringar i eksisterande kode og avhengigheiter.
`git diff --check` rein. Nye testar prøver katt og person, medborne ting, klede, snuing ved
møblar og romkant, løftepause, automatisk stopp, lagring/lager, verkelege fingerdrag og
to samtidige spelarfargar, uttoning, innkalling og foto utan merking.

Eiga native Android 36 AVD (`PlayfulPolish`, berre éin emulator om gongen), prøvd liggjande som
nettbrett 1920 × 1200 / 240 dpi og mobil 2400 × 1080 / 420 dpi. Visuell kontroll av nynorsk på
nettbrett, bokmål på mobil, æ/ø/å i namn, kartskilt, figurval, portrett, romval og tom sekk.
Mobilprøva køyrde også med animatorskala 0. Alle fire fontressursane har æ/ø/å og Æ/Ø/Å i cmap.
Katten vart også manuelt dregen av og på støvsugaren i den native appen; lagringa stadfesta
same katt (ID 26), `SEATED` på møbel 28, støvsugar på og sideforskyving 0,2383 under turen.
Krasjbufferen frå prøveemulatoren er tom. Ingen prøve med barn eller fysisk eining.

Skjermbilete, XML og testloggar ligg i Git-ignorert `screenshots/playful-polish/`. Nyttige bilete:
`tablet-players-selected.png`, `tablet-map-after.png`, `tablet-cat-ride-final.png`,
`phone-players-bokmaal.png`, `phone-map-bokmaal.png` og `phone-bag-bokmaal.png`.
Prøve-APK: `dist/playful-polish/Trollfoss-designprove.apk`, 18 786 384 byte, pakke
`app.trollfoss.polish` (eiga prøveapp). SHA-256:
`42bea6d4aa2fee96b64773f2ca860c4fc05e3d307a7b62b9bebfa2f4cd434bf6`.

## Kjapp release 1.10.0

Brukaren bad deretter om mildare namneboksar på kartet og ein kjapp release utan alle testar på nytt.
Namneboksane fekk tynnare kant, svakare skugge og pastell på vald stad; den siste justeringa er
kontrollert visuelt på mobil og nettbrett (`phone-map-milder.png`, `tablet-map-milder.png`).
Signert 1.10.0 / kode 15 er bygd med opphavleg nøkkel. Pakke, versjon, sertifikat, min Android 26
og ikkje-debuggable er kontrollerte. Installert over 1.9.0 med `adb install -r` på eiga prøve-AVD;
kaldstart viser kartet og dei to bevarte spelarane (`tablet-release-v1.10.0.png`).
APK: 3 385 767 byte, SHA-256 `b00df28e045315e23be92457317a491ae63d4fb0a377e57da92e213b9cde2524`.
Arkiv: `dist/release-v1.10.0/`, med mapping, hash og kjeldecommit. Dette er ein kort installasjons-
og startkontroll; full testpakke og oppdateringsflyten gjennom foreldresida er ikkje køyrde om att.
Publisert som nyaste stabile release: https://github.com/oyvhov/trollfoss-android/releases/tag/v1.10.0.
Kjelde/tag: `4f73eb330474bd4a8b1556268f3a9c3b2dc900ff`. Fire vedlegg har kontrollerte storleikar/digests;
offentleg liste/latest og APK-nedlasting utan token stemmer. Hedda og Alva er framleis valde.
Prøveappen er fjerna og eiga AVD stoppa etter at skjerm-, rotasjons- og animasjonsinnstillingane er tilbakeførte.
Den gamle arbeidskopien og private spelverdener i `C:\topa` er urørte.
