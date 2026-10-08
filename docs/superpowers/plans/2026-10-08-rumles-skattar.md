# Rumle sine skattar

Vidare arbeid etter «Jobb vidare med spennande ting», frå `af7a987` (lokal 1.14).
Grein `codex/rumles-skattar`, i den eksisterande administrerte arbeidskopien
`C:/Users/Øyvind/.codex/worktrees/rainbow-finale/topa` / `C:/topa/.gradle-tmp/rainbow-finale`.
Vald retning er skattejakt; brukaren fekk eit val medan grunnlaget vart lese.

## Leiken

Ei frivillig jakt frå Rumle, tilgjengeleg frå starten i Leik og eventyr. Tre små biletkart er lagde
ved ugletreet ved fossen, sandslottet på stranda og krystallane i Trollhola. Kortet viser dei tre
stadene med bilete, kva som er funne og eit kart-/sekk-ikon for neste spor. Ein kan finne spora
i valfri rekkjefølgje, flytte dei, gi dei til venner eller leggje dei i sekken. Ingen tidsfrist,
straff eller tap av framgang når jakta blir lukka eller appen blir starta på nytt.

Når alle tre er funne, får barnet ei trollykt. Ho er ei ekte ting som kan berast, flyttast og
lagrast. Eit trykk vekslar mellom stjerner, fisk og troll som lyser over lykta, og av. Lykta lyser
opp mørke rom. Redusert rørsle gir stilleståande lysbilete. Spora og lykta blir ikkje laga på nytt
ved gjentekne trykk. Biletkortet kan finne den same lykta att i verda eller sekken.

Ingen nye nivå eller stader; den eksisterande progresjonen og regnbogefinalen blir bevarte.
Det kjem to oppdagingar, éi for skatten og éi for lykta. Alle id-ar blir lagde til sist i enumane.
Nynorsk og bokmål, all grafikk og lyd i kode. Ingen publisering i denne runden.

## Kontrollplan

- [x] Domene og lagring: valfri rekkjefølgje, éi gåve, flytta/haldne/pakka spor, gamle lagringar,
  omstart, manglande spor og angring som bevarer framgang og gåva.
- [x] Android: fingerbaner i mobil- og nettbrettgeometri; lykt i handa og på golvet; nye teikningar.
- [x] Bygg, einingstestar, test-APK og lint gjennom byggjelåsen.
- [ ] Full skjermgjennomgang av menyflyten i begge format og målformer med rein oppstart.
  Scenene er visuelt kontrollerte i begge format; sjå avgrensingane nedanfor.
- [x] Rapport, bilete og overlevering. Lokal commit blir oppgjeven i `dist/rumles-skattar/SOURCE_COMMIT`.
  Barnetest og menneskeleg lytting blir skilde
  frå dei automatiserte kontrollane.

Oppstartsvarselet frå førre runde er ikkje forklart eller retta enno. Det skal vurderast på nytt
under skjermprøva, utan å stoppe eller endre andre sine emulatorar eller appdata.

## Gjennomføring og funn

- Versjon 1.15.0 / kode 20, isolert debugpakke `app.trollfoss.skattar`. Den eksisterande emulatoren
  `emulator-5554` blir brukt gjennom `Run-Locked.ps1`; skjermmål, dpi, rotasjon, skriftstorleik,
  animasjonsinnstilling og førre framgrunnsapp blir tilbakeførte etter kvar prøve.
- `TreasureTrail` held id-ane til dei tre spora og éi lykt; `WorldStore` lagrar dei under `rumle`.
  Verdshistoria bevarer desse id-ane og opptente flagg. Gamle lagringar utan feltet fungerer vidare.
- Delte filer er berre utvida for ruting, lagring, nye enum-verdiar, lys og knappen som finn rett
  side i sekken. Storhuset og Mitt hus sine rom, gangar og byggjereglar er ikkje endra.
- Fyrste kontroll fann at den generelle grensa for lagra `used` nullstilte lysbiletet ved omstart.
  Lykta har no eit eksplisitt lagra område 0–3. Den same testen kontrollerer at det valde biletet
  blir verande når ein venn får lykta og når verda blir lesen inn att.
- Teiknegjennomgangen fann at medborne ting blir skjulte i den vanlege frie tinga-løkka.
  Lykta bruker no synlegheita til venen som held henne, for både lysbilete og nattlys. Ein pakka
  eller skjult venn slepp ikkje lys ut av sekken eller skapet.
- Bygg med JVM-testar, debug-APK og test-APK er grønt: **631 testar, ingen feil**.
  Dette er lokal kontroll; ingen GitHub-CI, signert oppdateringsprøve eller barnetest er utført.
- Alle **84 Android-testar er grøne i kvart format på siste bygg**, også etter den siste visuelle rettinga:
  nettbrett 1920 × 1200 / 240 dpi og mobil
  2400 × 1080 / 420 dpi. Dei nye prøvene bruker motoren sine verkelege `down`/`move`/`up`-baner
  for papir og lykt. Biletprøva kontrollerer synleg projeksjon, stille bilete med redusert rørsle,
  projeksjon frå ei hand, lysare rom om natta og at ein pakka venn ikkje slepp lyset ut av sekken.
- Prøva i dei opphavlege scenene stadfesta at alle tre papir kan treffast i både mobil- og
  nettbrettgeometri. Ho lagrar 14 bilete frå spelmotoren, eksporterte til `C:/topa/screenshots/rumles-skattar/scene-*`.
  Visuell gjennomgang fann at omnen kunne skjule lysbiletet. Lykta projiserer no litt høgare,
  etter møblane og nattlaget, slik at lyset kan falle over synlege flater. Ein pikseltest legg
  omnsrøyret framfor lysbiletet og kontrollerer at det framleis er synleg.
- Kortet og påminninga observerer `engine.playVersion` i sin eigen Compose-scope, så funne spor
  og premieknappen kan oppdaterast utan å lukke eller opne påminninga på nytt.

## Endeleg lokal kontroll

Siste bygg med `testDebugUnitTest`, `assembleDebug` og `assembleDebugAndroidTest` er grønt.
Lint på den same kjelda er grønt: **0 feil / 32 åtvaringar**, same tal som ved starten.
Dei ni nye JVM-testane er med i dei 631; sju nye Android-testar er med i dei 84.
Alle kontrollar er køyrde gjennom prosjektet sine byggje- og emulatorlåsar.

Dei siste scenebileta er visuelt inspiserte: uglesporet ved treet, skjelsporet ved sandslottet,
krystallsporet ved bordet i Trollhola, og lykt med stjerner, fisk og troll i det møblerte
Familiehuset. Lykta er òg vist i Hedda si hand. Bileta kjem frå den faktiske spelmotoren og
teikningane, med `photoMode` og redusert rørsle; dei viser ikkje Compose-menyane rundt scena.
Ekte `down`/`up` i dei same møblerte scenene finn alle tre spora i begge storleikar.

I den vanlege appen på mobil/bokmål vart **Lek og eventyr → Finn neste spor → Fossen** trykt
gjennom. Lagringa stadfesta tre opphavlege papir og starta jakt; påminninga viste bokmål og
dei to valde spelarane kom med. Dette skjedde etter at «Wait» vart valt på eit oppstartsvarsel.
Kortbiletet vart teke medan berre det mørke dialogbaklaget var teikna, og kan difor ikkje brukast
som prov på ferdig kortutforming. Nettbrett/nynorsk vart forsøkt, men UI-tilgangen stoppa under
oppstart. Begge målformer er implementerte og gjennomgått i tekstkjelda.

**Oppstart står att før release.** «Trollfoss isn't responding» vart observert både med vanleg
kaldstart og med debug-snarveg, i mobil- og nettbrettformat. Dette var òg observert med uendra
1.13 i førre runde; sjå [1.14-rapporten](2026-10-08-niva-10-1-14.md). Årsaka er framleis ikkje
fastslått. Full menyflyt, oppstart, språkbyte og lagringsoppdatering må prøvast på ei roleg eiga
eining før publisering. Ingen barnetest, menneskeleg lytting, GitHub-CI eller signert oppdatering
er utført i denne runden.

Emulatoren melde òg for lite lagringsplass ved éi installering. Berre våre to testpakkar vart
fjerna og installerte på nytt. Ei seinare prøvestart vart avbroten med `DeadObjectException`,
og Android-tenestene `activity` og `window` var mellombels borte. Dei kom tilbake utan at vi
starta emulatoren på nytt. Skjerminnstillingane vart henta frå den lagra før-prøve-kopien,
tilbakeførte og samanlikna; deretter vart begge komplette Android-rundane grøne (84 kvar).
Denne avbrotne starten er ikkje rekna som ein bestått test.

Testpakkane `app.trollfoss.skattar` og `app.trollfoss.skattar.test` er fjerna etter kontrollen;
pakkelista er kontrollert. Den delte AVD-en `Tunet_Ascii` er ikkje stoppa. Ingen andre appdata
eller signeringsfiler er endra. Rotarbeidskopien får berre ein oppdatert peikar i overleveringa.

## Arkiv

- `C:/topa/dist/rumles-skattar/trollfoss-1.15.0-skattar-debug.apk`: isolert testapp, versjon
  `1.15.0-debug` / kode 20, pakke `app.trollfoss.skattar`.
- SHA-256: `6cef1c98453132928f850b110fd9c267ad18b7dadd1be99301a41488f4cae16a`.
- Same mappe har test-APK, metadata, `SHA256SUMS`, `SOURCE_COMMIT`, byggje-/lintloggar,
  lint-rapport, JVM-samandrag og Android-loggar frå begge format.
- `C:/topa/screenshots/rumles-skattar/` har dei 14 scenebileta, faktiske skjermbilete,
  oppstartslogg, registrerte ANR-forsøk og innstillingskopiar. Berre syntetiske testverder er brukte.

Ingen fletting til main, push eller publisering er gjort.
