# Vagstaddalen: foss, strand og bading

Brukaren bad om meir innhald i Vagstaddalen: ein spennande foss på høgre side, fisk, bål,
ei lita strand og fleire detaljar i ting og tre. Tillegget var at ungane skal kunne bade i
elva med badeklede og ein liten gummibåt.

Arbeidet ligg på `codex/vagstaddalen-elveliv`, frå `178b519`, i
`C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa` (ASCII-kopling
`C:/topa/.gradle-tmp/rainbow-finale`). Versjonen er framleis 1.16.0 / kode 21.
Dette er eit lokalt tillegg etter den publiserte 1.16.0-utgåva.

## Leiken

- Foss over to fjellhyller på høgresida, med vassband, skum, sprut og fire fiskar i kulpen.
- Lita sandstrand med våt sandkant, sandslott, bøtte, spade, badering, badeball og klesmatte.
- To flyttbare badedrakter. Dei bruker den vanlege påkledinga og viser nakne føter og korte bein.
  Det gamle antrekket kjem tilbake som ein ting; underdel, sko og andre utsjånadval er bevarte.
- Barna flyt og sym i det eksisterande elvevatnet. Dei kan dragast inn på stranda att.
- Ein gul gummibåt med to setar, handtak, tau, ventil og åre. Dei vanlege køyrepilene flyttar
  både båt og passasjerar; båten stoppar ved elvebreidda.
- Ny fiskeplass og tent bål på høgre breidd, med pølse og marshmallow. Fiskefangsten kan steikjast.
- Meir bark, lauv, granbar, mose, bregner, steinar, plankemønster og detaljar på hytta.

Alt er teikna i kode i same stil. Natt, årstider og redusert rørsle bruker dei eksisterande
palettane og klokkene. Det er ingen ny tekst for barnet.

## Lagringskontrakt

Dei åtte gamle møbel-ID-ane og den hemmelege steinen på indeks 8 er urørte. Nye møblar blir lagde
etter steinen. Markøren `layout:valley-river-life:1` gir dei nye lause tinga berre éin gong.
Flytta møblar, pakka ting, personar og barnet sine endringar blir bevarte.

Badestilane er lagde sist, som topp 17 og 18. Båten bruker eksisterande `BOAT` med variant 1;
variant 0 og undervassbåten bevarer den gamle skrogmarginen. Den spegelvende fiskeplassen har
variant 1 og kastar fangsten mot land.

## Kontroll

Bygg er køyrde gjennom `Build-Locked.ps1` med separat prøvepakke `app.trollfoss.valley`.
`testDebugUnitTest`, `assembleDebug` og `assembleDebugAndroidTest` er grøne: **698 JVM-testar**.
Dei fem nye prøvene dekkjer påkleding/avkleding, bading/ilandstiging, to passasjerar og begge
elvebreidder, fisk/steiking, samt oppgradering og ny lagring utan duplikat eller tap av flytta ting.
Første båttest fann at den gamle båtmarginen stoppa gummibåten for tidleg; marginen er retta for
denne varianten, og heile JVM-suiten er køyrd etter rettinga.

Heile Android-suiten er grøn med **102 testar på kvart format**, inkludert den nye prøva med
`down`/`move`/`up` gjennom spelmotoren: påkleding → bading → båt → køyring → ilandstiging.
Prøva held strand og båt innanfor skjermen og slepp barnet på fri sand, utanfor stubben sitt sete.
Lint er grøn med **0 feil og 32 åtvaringar**. `git diff --check` er òg grøn.

Dei ti scenebileta under `C:/topa/dist/vagstaddalen-elveliv/valley-scenes/` viser hytte,
strand, elv, natt og vinter i både mobil- og nettbrettformat. Dette er faktisk Compose-teikning
frå spelmotoren, med eit barn i vatnet og eit i båten. Filene er kontrollerte visuelt;
testen lagar bilete, men gjer ingen automatisk samanlikning mot ein fasit.

Manuell prøve på nettbrett: val av Hedda og Alva, panorering til stranda, påkleding av Hedda,
bading, opp i båten og tilbake på stranda med faktiske fingerdrag. Bileta
`live-tablet-boarded.png` og `live-tablet-back-on-beach.png` dokumenterer dette. Prøvelagringa
stadfestar topp 17 og overgang `SEATED` i båt 3911 → `FREE` på sandstranda.
På mobil er same lagra antrekk, oppstart, panorering og eit nytt drag frå sanden ut i elva kontrollerte
(`live-phone.png`). Det er ingen registrerte krasj eller ANR for prøvepakken i loggane frå denne økta.

## Arkiv og avgrensingar

Autoritative lokale filer ligg i `C:/topa/dist/vagstaddalen-elveliv/`. Ingen signeringsnøkkel,
versjon, offentleg release eller GitHub-innhald er endra. GitHub-CI er ikkje køyrd for dette
lokale tillegget. Det er ikkje prøvd av barn eller på ei fysisk eining.

Emulatoren er eiga KVM-prøveeining `TrollfossRelease1160Kvm`, med Skia/Vulkan som i førre
releasekontroll. Éin emulator er brukt om gongen, i 1920 × 1200 / 240 dpi og 2400 × 1080 / 420 dpi.
Etter namnekontroll er prøvepakkane avinstallerte, skjermmål/tettleik/rotasjonsval tilbakeførte og
den eigne emulatoren stoppa. Produksjonspakken og private spelverdener er ikkje endra.

Prøve-APK: `trollfoss-vagstaddalen-proeve.apk`, SHA-256
`3ce4ac58ac2265386db6f64cc025f9a79c90d566ad02406092adf6aef9c79ce7`.
Byggloggen `build-corrected.txt`, `build-ui-final.txt`, `lint.txt` og
`instrumentation-full-tablet.txt` / `instrumentation-full-phone.txt` er dei endelege kontrollane.
Tidlegare loggar kan innehalde mislukka forsøk frå før rettingane.
