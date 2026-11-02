# Yting på treigare einingar – 2. november 2026

Arbeidskopi: `rainbow-finale/topa` (`C:/topa/.gradle-tmp/rainbow-finale`).
Grein: `codex/performance-slow-devices`, frå `f80479f` etter publisert 1.17.0.
Rotkopien sine eldre uferdige endringar er urørte. Ingen versjonsauke eller publisering.

## Endringar

Motoren sende tidlegare alle synlege (ikkje gøymde i skap osv.) figurar og ting på
heile staden gjennom djupnesortering, skuggar, teikning og sprite-oppfrisking.
Dette omfatta også dei som stod fleire rom utanfor kameraet. `BodyVisibility`
avviser no dei fjerne kroppane før dette arbeidet. Grensa er medvite romsleg:
to gonger største kroppsmål, ein halv scene-eining ekstra og kastesporet.
Haldne ting/figurar og aktive namneskilt blir alltid tekne med. Ingen loddrett
avskjering. Dette endrar ikkje simulering, treffprøver eller lagring.

Når `SpriteCache` samanliknar to prøvebilete for å oppdage animasjon, bruker han
no Android si piksel-samanlikning direkte. Før kopierte han begge bitmapane til
kvar sitt `IntArray`. Endringa sparer `8 × breidd × høgd` byte mellombelse
pikseldata per slik samanlikning; trimmeoperasjonen for stille bilete er uendra.
Fargar, detaljar, biletfrekvens og animasjonar er ikkje reduserte.

Compose-kontrollen fann at den vanlege rammetikkaren i `PlayScreen` allereie blir
lesen i teiknefasen. Ingen stabilitetsannotasjonar eller nye Compose-buffer er lagde til.

## Reproduserbar kontroll

Bygg med prosjektet sin byggjelås og ei eiga pakke:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:/topa/scripts/Build-Locked.ps1 -Tasks ':app:testDebugUnitTest,:app:assembleDebug,:app:assembleDebugAndroidTest,:app:lintDebug' -Suffix .perf -Plain
```

`BodyVisibilityTest` kontrollerer kamerabyte, begge skjermkantar, liggjande/store
figurar, haldne ting, namneskilt og raske kast. `SpriteCacheTest` kontrollerer at
stille bilete blir brukte om att, medan reelle pikselendringar blir oppdaga som animasjon.

Den eksplisitte `ScenePerformanceTest` køyrer same faste speltilstand gjennom
programvarerenderaren ved 1920 × 1200 og 2400 × 1080, i Storstova, Kjellaren og
Vagstaddalen. Køyr med instrumenteringsargumenta `-e class
app.trollfoss.ui.play.ScenePerformanceTest -e measureScenes true`. Han varmar opp
60 rammer, skriv PNG-ar og måler heile `Engine.draw` (inkludert sprite-oppfrisking)
over 120 rammer. Målinga er valfri og blir hoppa over i vanlege funksjonstestar.
Ho måler teiknekostnad med frosen simulering, ikkje total FPS under spel.

Arkiv for APK-ar, målingar og bilete: `C:/topa/dist/performance-2026-11-02/`.

## Resultat og avgrensingar

- 701 JVM-testar utan feil. Debug-APK og Android-test-APK er bygde.
- Endeleg bygg, testar og lint er grøne. Lint: 0 feil / 32 åtvaringar. Omkøyring
  med `--max-workers=1 --no-parallel` fullførte på 4 minutt og 13 sekund.
- 103 Android-testar utan feil på kvart av mobil- og nettbrettformat, inkludert
  teikning, påkleding, griping, samspel, lys og båtleik.
- Fire av seks faste scenebilete er pikselidentiske. Kjellaren har 1586 endra
  pikslar på nettbrett (maks 2 av 255 per fargekanal) og 6 på mobil (maks 1).
  Visuell kontroll viste same innhald; små avrundingsskilnader mellom teiknevegane.
- Ingen fysisk eining er testa. Dette er API 36 på eigen KVM-emulator med
  programvaregrafikk, under varierande last på vertsmaskina.

Første målerunde gav ein stor fartsskilnad, men omkøyring av den gamle APK-en
viste at mykje av han kom frå oppvarming/testmiljøet. Dei første tala er difor
ikkje dokumentasjon på ein stor fartsvinst. Tabellen viser dei siste, oppvarma
før-/etterrundane, median millisekund for **berre teikning** med frosen simulering:

| Format | Stad | Før | Etter |
| --- | --- | ---: | ---: |
| Nettbrett | Storstova | 55,43 | 53,75 |
| Nettbrett | Kjellaren | 62,71 | 60,71 |
| Nettbrett | Vagstaddalen | 38,82 | 38,46 |
| Mobil | Storstova | 55,22 | 51,25 |
| Mobil | Kjellaren | 68,99 | 66,11 |
| Mobil | Vagstaddalen | 47,62 | 46,91 |

Skilnadene er små og ikkje nok til å love ein bestemt FPS-auke. P95 varierer;
til dømes vart Storstova på nettbrett dårlegare i den siste samanlikninga.
Den sikre effekten er at fjerne kroppar ikkje blir sende til teikning eller
sprite-oppfrisking, og at to pikselkopiar er borte ved kvar animasjonsprobe.
Mål vidare på eit fysisk tregt nettbrett, med panorering og mange figurar/ting.

Dei første appmålingane frå `gfxinfo` hadde Android sitt fullskjermvarsel over
spelet og er ikkje brukte som dokumentasjon på full speleflyt eller FPS.
Den separate, faste renderprøva ovanfor er ikkje påverka av dette varselet.
Den siste manuelle sveipprøva på begge format fekk dessutan dialogen «System UI
isn't responding». Ho er **ikkje bestått** som manuell spelkontroll. Android-testane
køyrde direkte mot spelmotoren utan dette hinderet. Ny manuell prøve på ei stabil
eining står att før release. Testemulatoren er stoppa, og den mellombels stoppa
`Stova_TV` er starta att på port 5580 med dei opphavlege oppstartsvala.

Første fullbygg vart avbrote under lint etter over 17 minutt for å frigjere
minne; test- og APK-oppgåvene var då ferdige. KVM-oppstart frå Windows-filsystemet
var svært treg og WSL-kall feila tidvis. Ein lokal kopi av systembiletet og
ADB direkte til emulatoren gav eit brukbart testmiljø. Eit Windows-emulatorforsøk
feila i WHPX før Android starta. Desse er testmiljøfeil, ikkje app-krasj.

## Vidare leik som er verd å prioritere

- Små, valfrie hendingar mellom stadene: til dømes pakk piknik, ta båten og inviter
  ein ven. Bruk dei eksisterande figurane og tinga, med bilethint og fleire moglege
  avslutningar. Barnet skal kunne avbryte og leike fritt.
- Eigne forteljingar: lagre nokre oppstilte scener i fotoalbumet, legg dei i rekkjefølgje
  og spel dei av som ei lita biletbok. Ingen nett-teneste eller konto.
- Fleire synlege samspel: to figurar som samarbeider om ei konkret leike eller oppskrift,
  med tydelege plassar å sleppe ting på og små morosame reaksjonar.

Desse er forslag, ikkje implementerte funksjonar i denne ytingsrunden.
