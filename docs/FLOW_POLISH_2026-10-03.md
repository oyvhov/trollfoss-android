# Arbeidslogg: speleflyt og dei neste magiske leikene

Arbeidsgren: `codex/child-flow-polish`. Grunnlag: 1.6.1 og gjennomgangen i `AUDIT_FLOW_2026-10-03.md`. Ingen ny release er publisert i denne runden.

Dei uferdige kjeldeendringane frå `codex/magic-rest` er tekne inn i ei separat arbeidskopi. Den opphavlege, skitne arbeidskopien er urørt. Nytt innhald er fullført vidare her, med eigne teikningar, lagring og kontroll.

## Endringane frå gjennomgangen

| Funn | Implementert |
| --- | --- |
| F01 | Valde spelarar kjem framme på golvet og blir teikna framfor møblar når dei står der. Portrett hentar same figur, og ei halden hand blir ikkje stolen. |
| F02 | Prøv søkjer alle brukbare golvstriper. Fullt rom har direkte inngang til Møblar. Krone → seng → Prøv boblemaskin er gjennomført i fersk verd. |
| F03 | Behald og Riv står under kvarandre med full breidd. Mindre illustrasjon i liggjande mobilformat gir plass til begge knappane. |
| F04 | Bygg her opnar panelet for rett rom; Leik i rommet tek med laget. Mobil har direkte romoversikt. Førehandsvising viser valt hus. |
| F05 | Leik-knappen er flytta frå golvet til topprada. Færre doble inngangar. Mindre laus bakgrunnspynt og eigen knappestripe på kartet. Den breie nettbrettvisinga er bevart. |
| F06 | Valfri stad, panel lukka under hjelp, ei levande dra-linje mellom rett ting og mål; valde spelarar blir prioriterte. |
| F07 | Stabilt spelarval og tre frivillige biletidear: dra venn, gje ting, prøv leike. |
| F08 | Lager viser tal og stadfesting med Hent tilbake for akkurat den lagra kopien. Papirkorg er framleis varig og reversibel. Møblar har eigne namn i skjermlesartreet. |
| F09 | Pakking set ein spelar på pause. Spelarval/utsjånad blir ståande, og portrettet hentar figuren tilbake. Angre tek òg bort pausen. |
| F10 | Trykkbare glimt med hint/reise, fullfarge oppskriftsdelar og henting av råvarer, invitasjon frå tom fotoalbum. |
| F11 | Nivå 4–6 med fire fungerande leiker kvar. Nyaste nivå og neste gåver kjem først. Nivå 7–10 er framleis den vidare planen. |
| F12 | Gåver opnar gåvene. Prøv er ei tydeleg tekstknapp med stjerne. |
| F13 | Synleg treff går framfor utvida treffområde. Små ting har større gripeområde. Leikeførehandsvising bruker same akseptregel som faktisk handling. |
| F14 | Fullført eventyrpåminning kan lukkast og går bort automatisk. Framgang blir bevart. |
| F15 | Dekkte spelekontrollar er skjulte for skjermlesar; scena har alternative person- og møbelhandlingar. Full TalkBack-/brytarprøve står att. |
| F16 | Foto utan sekk, namneskilt, ønskjebobler eller hjelpepiler; førehandsvising før lagring. |
| F17 | Stabil lagrad, biletfaner i figurverkstaden, angring av terning, heil OK-tekst og husstil i etasjeførehandsvising. |

## Ny leik

20 nye møbel/leiketypar: vippe, dokketeater, tandem, piknikkorg, kran, transportband, byggjebord, vassrenne, spegel, svevepute, vêrglas, portal, epletre, reparasjonslys, hemmeleg bokhylledør, tunnel, mjukt hopp, vasshjul, kunststaffeli og redningsleik.

Vennehandlingar, tre personlegdommar, kjæledyr med lagra heimstad, eit band av tre instrument, stempelkunst/tapet, snø/regn/vind, tre hemmelege kvardagsegenskapar, levande bamse og gjestefest. Skyøya er ein eigen stad med piknik og kikkert, nådd med sengballongen frå kartet. Dyr og valde figurar held identiteten sin.

Runde og boga vindauge i Mitt hus avgrensar no gardiner, måne, stjerner og lys til sjølve glaset. Før kunne innhaldet stikke ut som ein firkant over ramma og veggen.

## Kontroll

- Endeleg debug-bygg og 466 einingstestar grøne. 24 Android-kontrollar grøne, mellom anna to samtidige fingrar, spelarvandring, rommerking ved kamerakanten og teikning av alle nye leiker. Dei siste reine endringane i vindauge/dialogmål er bygde og visuelt kontrollerte separat.
- Endeleg lint: 0 feil, 31 åtvaringar. Sjå den tilhøyrande PR-en for GitHub-kontroll og flettestatus.
- Eiga fersk `app.trollfoss.flowpolish`-verd. Ingen private lagringar er brukte eller endra.
- Nettbrett 1920 × 1200 / 240 dpi: to spelarar, krone og seng gjennom faktisk draging, nivå 2, henting av boblemaskin, foto-førehandsvising/lagring, kaldstart til Kart og ballongreise til Skyøya.
- Bygd Tårnhus med kjøken, stove og soverom i andre etasje; Leik i rommet tek med laget. Riv → Angre gjenoppretta rom og innhald. Eigne stempelbilete er laga med både trykk og draging og hengde opp. Ein bamse vart faktisk dregen gjennom to portalringar.
- Nivå 6 vart gjort tilgjengeleg i den eigne testverda ved å setje 20 merke. Det er ein kontroll av innhald og skjermar; berre dei første to merka vart tente gjennom den naturlege oppdragsflyten.
- Kort mobilkontroll 2400 × 1080 / 420 dpi: foreldrelås løyst, bytt til bokmål, romoversikt og riv-dialog, gåver og spelarval. Hovudgjennomgangen bruker nynorsk på nettbrett. Ingen Trollfoss-krasj i krasjloggen.
- Samanlikning av lagringsfiler stadfestar same spelar-IDar og utsjånad, identisk hus og uendra første kunstverk etter reiser/omstart/ny testinstallasjon. Det andre kunstverket er lagt til utan å overskrive det første. Dette er ikkje ein OTA-releaseprøve.
- Bilete/XML i Git-ignorert `C:/topa/screenshots/polish-2026-10-03/`. Dette er målretta kontroll av endringane; den tidlegare rapporten dokumenterer heile grunnflyten og alle dei 24 opphavlege stadene.
- Ingen barnetest er utført. Oppdragsbalanse, opplesing og full tilgjengelegheitsprøve må ikkje omtalast som ferdig dokumenterte.

## Avgrensingar og vidare arbeid

Dei 25 prioriterte løfta har no fungerande grunninnhald i kjelda. Den større planen med nivå 7–10, fleire historiebonusar, full opplesing og utvida foto-/minneleik står framleis att. F15 har konkrete betringar i skjermlesartreet og alternative handlingar, men er ikkje lukka som ei full TalkBack-/brytergodkjenning. Barnetest må avklare kor lett barna sjølve finn aktivitetane og korleis nivågrensene bør justerast.
