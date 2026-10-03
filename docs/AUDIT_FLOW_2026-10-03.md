# Trollfoss: gjennomgang av speleflyten

Dato: 3. oktober 2026. Vurdert utgåve: **publisert 1.6.1**, versjonskode 10.

Spelet har mykje av det som kan bli magisk: eigne figurar, ei særmerkt verd, fri leik, roleg belønning og oppskrifter som faktisk endrar rommet. Den største svakheita er at barnet ikkje alltid kan sjå figuren sin, forstå neste handling eller stole på responsen. **Tre feil stoppar eller skjuler sentrale handlingar; dei bør rettast før fleire store funksjonar blir lagde til.**

Dette er ein gjennomført audit med konkrete observasjonar og ein prioritert arbeidsplan. Funna er ikkje retta i appen i denne runden.

## Omfang og grunnlag

- Oppstartskartet vart kontrollert i den publiserte APK-en. Dei vidare handlingane vart køyrde i ei eiga, fersk prøveverd med debug-pakke `app.trollfoss.flowaudit`, bygd frå same spelkjelde som 1.6.1. Berre pakken/debug-konfigurasjonen skil seg.
- Hovudflytane vart prøvde gjennom vanlege knappetrykk og drag: spelarval, utsjånad, kart/menyar, to ekte oppdrag, nivå 2, plassering, lager/sletting/tilbakehenting, eit heilt eventyr, hus/rom/andre etasje, tunnel/trapper, foto, kaldstart og foreldresida.
- Alle **24 stader** vart inspiserte minst éin gong. Ti vart nådde gjennom vanleg spelreise/bygd hus; dei siste fjorten fekk ei debug-reisesnarveg for visuell dekning. Dette provar ikkje at kvar gjenstand eller kvar veg i kvar stad fungerer.
- Mesteparten vart kontrollert på nettbrett, 1920 × 1200 / 240 dpi. Kort mobilkontroll: Kart, figurverkstad og Stranda, 2400 × 1080 / 420 dpi. Eit ekstra bilete viser retur til Kartet.
- Ingen framgang vart injisert for å opne gåver. Nivå 2 vart opptent gjennom faktisk leik. Ingen private/verkelege spelverder vart nullstilte. Eiga nullstilling vart avbroten; oppdatering/installasjon vart ikkje repetert.
- Skjermbileta i denne gjennomgangen vart tekne og inspiserte no. Oppstartsillustrasjonen i bilete 67 hadde forelda UI-tre og er ikkje brukt som dokumentasjon for kaldstart; bilete 68 viser den ferdige oppstarten.
- Spelkjelde: tag `ca0454e03b602365a5ec6e0ad91050c7b3683c98`; rein arbeidskopi/main `42a5149256bfd95775e5b031302ccc463d826303`. Dei uferdige endringane på `codex/magic-rest` er ikkje grunnlag for påstandar om kva barna har i utgitt spel.

**Avgrensingar:** Dette er ei fagleg vurdering og kontroll av faktisk bruk, ikkje ein observasjonsstudie med barn. Lyd vart ikkje lytta til. TalkBack, brytarstyring, kontrastmåling, to samtidige fingrar, yting på fysisk nettbrett, alle frisyrer/storleikar, alle 76 glimt, alle 47 oppskrifter, nivå 3 og alle maskin-/dyreinteraksjonar er ikkje uttømmande prøvde. Krasjbufferen var tom ved avslutning; det er ikkje bevis for at spelet aldri krasjar. Skjermmål/rotasjon er tilbakeførte og den eigne prøveemulatoren er stoppa.

## Flyten steg for steg

Fungerer tyder at den konkrete handlinga lykkast. Friksjon tyder at ho lykkast, men at barnet kan misforstå eller trenge hjelp. Blindgate/blokkert tyder at den konkrete inngangen ikkje gav neste handling.

### 1. Første start og val av to spelarar — Friksjon

Store portrett og nummer 1/2 fungerer. Rutenettet flyttar seg etter første val; draing er ikkje forklart med bilete.

![Steg 1: Første start og val av to spelarar](C:/topa/screenshots/audit-2026-10-03/02-team.png)

### 2. Utsjånad og hår — Fungerer med friksjon

Faktisk draging gav lengre lokkar med stabil hårrot. Mange gode val, men tre hovudfaner krev lesing. Andre frisyrer/storleikar er ikkje uttømmande prøvde.

![Steg 2: Utsjånad og hår](C:/topa/screenshots/audit-2026-10-03/05-hair-max.png)

### 3. Kart, reise og retur frå meny — Fungerer med friksjon

Sengballongen viser begge spelarane. Boka lukka tilbake til Kartet; reisemål utanfor synsfeltet blir forklarte med tekst.

![Steg 3: Kart, reise og retur frå meny](C:/topa/screenshots/audit-2026-10-03/11-back-to-map.png)

### 4. Oppdagingsbok, oppskrifter og tomt album — Blindgater

Tom stjerne gav ingen hint; ukjende oppskrifter er mørke silhuettar og albumet har inga første handling.

![Steg 4: Oppdagingsbok, oppskrifter og tomt album](C:/topa/screenshots/audit-2026-10-03/08-secret-hint.png)

### 5. Gåver og låste leiker — Friksjon/feil

Gåver på Kartet opna Oppdrag. Detaljkortet for låst leike er godt: manglande merke og konkrete handlingar.

![Steg 5: Gåver og låste leiker](C:/topa/screenshots/audit-2026-10-03/12-gifts.png)

### 6. Oppdrag → merke → nivå 2 — Fungerer med friksjon

To oppdrag vart faktisk fullførte. Nye leiker vart opna roleg. Stadnamn og rommarkering var tidvis feil.

![Steg 6: Oppdrag → merke → nivå 2](C:/topa/screenshots/audit-2026-10-03/19-level-unlock.png)

### 7. Prøv ei nyopna leike — Stoppar handlinga

Boblemaskina fekk «Her er fullt» i standardhuset. Ho kunne seinare dragast inn frå Møblar på same stad.

![Steg 7: Prøv ei nyopna leike](C:/topa/screenshots/audit-2026-10-03/22-reward-in-room.png)

### 8. Møblar → Lager → Papirkorg → tilbake — Fungerer med friksjon

Faktisk sletting og tilbakehenting av bordet fungerte. Overlapp gjer det vanskeleg å gripe rett møbel.

![Steg 8: Møblar → Lager → Papirkorg → tilbake](C:/topa/screenshots/audit-2026-10-03/28-recover-deleted-furniture.png)

### 9. Fjerne folk og flytte to spelarar — Fungerer med friksjon

Figurane reiste med og beheldt utsjånad/krone. Legg i sekken fjernar også spelarplassen; ho må veljast på nytt.

![Steg 9: Fjerne folk og flytte to spelarar](C:/topa/screenshots/audit-2026-10-03/32-room-follow.png)

### 10. Oppskrift og heilt eventyr — Fungerer med friksjon

Putehytte vart bygd, Hedda sat i henne, lommelykt fullførte eventyret og gåva kom i sekken. Alva vart heilt skjult av hytta.

![Steg 10: Oppskrift og heilt eventyr](C:/topa/screenshots/audit-2026-10-03/37-fort-seat.png)

### 11. Velje hus, byggje rom/etasje og rive — Delvis blokkert

Stove, kjøken og soverom vart bygde, andre etasje opna. Riv-knappen var utilgjengeleg i dialogen; Behald fungerer.

![Steg 11: Velje hus, byggje rom/etasje og rive](C:/topa/screenshots/audit-2026-10-03/55-demolish-confirmation.png)

### 12. Trollhola → tunnel → Storhuset → trapper — Fungerer med friksjon

Vanlege tunnel- og trappetrykk førte begge figurar vidare. Tunnelen er merkt. Nokre ankomstposisjonar skjuler figurar.

![Steg 12: Trollhola → tunnel → Storhuset → trapper](C:/topa/screenshots/audit-2026-10-03/61-through-secret-tunnel.png)

### 13. Ta, lagre og opne bilete — Fungerer med friksjon

Foto vart lagra og opna. Bag-/ønskjeikon og skjulte figurar blir med i biletet; tomtilstanden manglar hjelp.

![Steg 13: Ta, lagre og opne bilete](C:/topa/screenshots/audit-2026-10-03/66-photo-open.png)

### 14. Lukke appen og starte igjen — Fungerer

Vanleg kaldstart enda på Kartet. Lag, utsjånader, hus og merke var bevarte.

![Steg 14: Lukke appen og starte igjen](C:/topa/screenshots/audit-2026-10-03/68-cold-start-ready.png)

### 15. Foreldreside, språk og avbryte nullstilling — Fungerer med liten feil

Multiplikasjonsport og språkbyte fungerer. Nullstilling vart avbroten trygt. OK-teksten i porten blir klipt.

![Steg 15: Foreldreside, språk og avbryte nullstilling](C:/topa/screenshots/audit-2026-10-03/71-reset-confirmation.png)

### 16. Stader, uteplass og aktiv traktor — Visuell friksjon

Alle 24 stader inspiserte minst éin gong. Radio frå Lager fungerte på Stranda. Traktormeny og finn-att-portrett prøvde; mange stader har overlapp/rot.

![Steg 16: Stader, uteplass og aktiv traktor](C:/topa/screenshots/audit-2026-10-03/76-beach-radio-placed.png)

### 17. Dag/natt og vêr — Fungerer

Vanlege knappetrykk gav natt og regn med framleis spelbare figurar.

![Steg 17: Dag/natt og vêr](C:/topa/screenshots/audit-2026-10-03/92-night-weather.png)

### 18. Kort mobilkontroll — Friksjon

Kart, verkstad og Stranda sjekka. Kartknappar dekkjer nokre stadnamn og botnstjerna ligg framleis i leikområdet.

![Steg 18: Kort mobilkontroll](C:/topa/screenshots/audit-2026-10-03/96-phone-beach.png)

## Kva som bør bevarast

- Dei store portretta og spelar nummer 1/2 gir ei tydeleg eigarskjensle. Utsjånad, klede og ei påsett krone følgde med gjennom reiser og kaldstart.
- Den store sengballongen med spelarane gjer reisa personleg. Den hemmelege tunnelen er merkt og førte faktisk til Storhuset.
- To ekte oppdrag gav nivå 2 utan at merke vart brukte opp. Det vesle Nye leiker-kortet er ei roleg feiring som ikkje overtek heile leiken.
- Låst-leike-kortet viser kor mange merke som manglar og konkrete handlingar. Bygg leiker viser delane i fargar og kan hente dei til barnet. Dette er gode mønster å bruke fleire stader.
- Papirkorg tok vare på bordet. Hent tilbake og ny plassering fungerte; radioen kunne reisast med og dragast ut på Stranda.
- Romførevising, byggjeanimasjon og dei tydelege krava for andre etasje gav faktisk framgang.
- Romstasjonen, Scena og Legekontoret viser kor mykje meir lesbar leiken blir med store ting og ro rundt forgrunnen.

## Prioriterte funn og konkrete rettingar

**P1:** Rett først; sentral handling stoppar eller spelar blir borte frå barnet sitt synsfelt. **P2:** Gjer spelet lettare å forstå og bruke. **P3:** Forbetrar kvalitet og eigarskjensle etter at kjernen er trygg.

### F01 · P1 · Barnets figur blir skjult

**Observert:** Alva vart heilt skjult bak den nye putehytta. Hedda vart skjult av ein større figur på Heileberget; på loftet stod spøkelset framfor henne. Dette kan opplevast som at figuren er borte, sjølv om portrettet er synleg.

**Rett slik:** Vel fri ankomstplass for laget, hald av plass rundt begge figurar og la framgrunn som dekkjer vald spelar bli gjennomsiktig. Gjer spelarportrettet til ein tydeleg «Finn meg»-handling med kort lysring. Når kameraet flyttar seg, følg med jamn gange og la barnet hente laget med eitt trykk.

**Ferdig når:** To valde figurar skal vere synlege eller tydeleg viste som sitjande i eit møbel etter reise, rombyte, bygging og panorering. Eitt portretttrykk finn figuren utan å endre rolla hans/hennar.

Dokumentasjon: [36-combined-fort](C:/topa/screenshots/audit-2026-10-03/36-combined-fort.png), [37-fort-seat](C:/topa/screenshots/audit-2026-10-03/37-fort-seat.png), [88-heileberget](C:/topa/screenshots/audit-2026-10-03/88-heileberget.png), [89-manor-attic](C:/topa/screenshots/audit-2026-10-03/89-manor-attic.png). Kjelde for vidare arbeid: [ui/play/Engine.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/play/Engine.kt), [domain/Players.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Players.kt).

### F02 · P1 · Første nye leike blir avvist

**Observert:** Etter to vanlege oppdrag i ei fersk verd gav Prøv på boblemaskina «Her er fullt». Faktisk drag frå Møblar sette henne likevel inn i same Familiehus og boblene virka. Regelen for automatisk fri plass er strengare enn regelen for draging.

**Rett slik:** Bruk same plasseringsregel i begge inngangar. Vis ein plasseringstilstand med grønt mål som barnet kan flytte. Ved reell plassmangel: tilby Set på ein annan stad eller Opne Lager, med bilete og ein direkte knapp.

**Ferdig når:** Fersk verd → krone → seng → Prøv boblemaskin skal føre til brukbar leike utan at barnet må finne ein annan meny. Ingen uriktig «fullt»-melding.

Dokumentasjon: [22-reward-in-room](C:/topa/screenshots/audit-2026-10-03/22-reward-in-room.png), [25-drag-reward-from-furniture](C:/topa/screenshots/audit-2026-10-03/25-drag-reward-from-furniture.png). Kjelde for vidare arbeid: [domain/ToyPlay.kt:14](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/ToyPlay.kt:14), [domain/Designer.kt:44](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Designer.kt:44), [ui/TrollfossViewModel.kt:229](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/TrollfossViewModel.kt:229).

### F03 · P1 · Riv-knappen er utanfor dialogen

**Observert:** På nettbrett viser Riv rommet?-dialogen berre Behald. Riv er pressa ut til høgre, og sveip gjer han ikkje tilgjengeleg. Riving vart derfor ikkje gjennomført.

**Rett slik:** Stable dei to knappane som i den fungerande nullstillingsdialogen, eller fordel breidda eksplisitt mellom dei. Behald visuell førehandsvising og at ting blir tekne vare på.

**Ferdig når:** Begge knappar skal vere synlege utan sveip på nettbrett og mobil, på nynorsk og bokmål. Riving flyttar innhaldet til Lager; avbryt endrar ingenting.

Dokumentasjon: [55-demolish-confirmation](C:/topa/screenshots/audit-2026-10-03/55-demolish-confirmation.png), [56-hidden-demolish-button](C:/topa/screenshots/audit-2026-10-03/56-hidden-demolish-button.png). Kjelde for vidare arbeid: [ui/screens/BuildPanel.kt:487](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/BuildPanel.kt:487), [ui/components/Buttons.kt:160](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/components/Buttons.kt:160), [ui/components/GameText.kt:60](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/components/GameText.kt:60).

### F04 · P2 · Husbygging manglar ei samla orientering

**Observert:** Husoversikt og Du er her finst i Bygg-panelet og er nyttige. Utanfor panelet fokuserer Bygg her berre ei tom romplass; barnet må så finne Bygg ein gong til. Ved ei ubygd plass var begge spelarane utanfor skjermen. Oversikta hjelper dermed ikkje nok ved overgangen mellom bygging og vanleg leik.

**Rett slik:** Gjer den eksisterande husoversikta enkelt tilgjengeleg også frå vanleg leik, med aktuell etasje, romnummer og Du er her. Bygg her skal opne romvala for akkurat den plassen. Den eksisterande Leik i rommet-knappen bør gje ein tydeleg overgang med laget på ferdig golv og byggjepanelet lukka.

**Ferdig når:** Barnet kan byggje to rom og andre etasje, finne rommet att og gå over til leik utan vaksenforklaring. Ingen tom vising utan ein tydeleg veg tilbake.

Dokumentasjon: [45-build-room-navigation](C:/topa/screenshots/audit-2026-10-03/45-build-room-navigation.png), [47-building-options](C:/topa/screenshots/audit-2026-10-03/47-building-options.png), [52-upper-floor-ready](C:/topa/screenshots/audit-2026-10-03/52-upper-floor-ready.png). Kjelde for vidare arbeid: [ui/screens/BuildPanel.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/BuildPanel.kt), [ui/screens/PlayScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt).

### F05 · P2 · Knappar og pynt tek plass frå leiken

**Observert:** Den faste Leik og eventyr-stjerna ligg over golvet og figurbein/småting. På Stranda og Fjellet er leiken samla heilt nedst under ein stor himmel. Løv, prikkar og konfetti liknar ting ein kan ta.

**Rett slik:** Reserver trygg plass til knappar utanfor drageområdet. Gjer leikescena høgare og ha ein roleg start med få ting i forgrunnen. Flytt pynt til bakgrunnen og gje aktive ting meir tydeleg form/kontrast. La barnet fylle rommet sjølv.

**Ferdig når:** Alle startstader har fri plass til to figurar og fleire handlingar. Ingen fast knapp dekkjer aktive mål; pynt kan ikkje forvekslast med ting barnet får tak i.

Dokumentasjon: [73-beach-arrival](C:/topa/screenshots/audit-2026-10-03/73-beach-arrival.png), [79-salon](C:/topa/screenshots/audit-2026-10-03/79-salon.png), [80-mountain](C:/topa/screenshots/audit-2026-10-03/80-mountain.png), [83-tivoli](C:/topa/screenshots/audit-2026-10-03/83-tivoli.png), [90-manor-garden](C:/topa/screenshots/audit-2026-10-03/90-manor-garden.png), [96-phone-beach](C:/topa/screenshots/audit-2026-10-03/96-phone-beach.png). Kjelde for vidare arbeid: [ui/screens/PlayScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt), [ui/art/PlaceArt.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/art/PlaceArt.kt), [domain/Places.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Places.kt).

### F06 · P2 · Hjelpa peikar ikkje alltid på rett stad eller handling

**Observert:** Kroneoppdraget seier Kart, men hjelpa reiser til Mitt hus-tomta. Byggjepanelet vart ståande over kroneoppgåva. Sengoppdraget viste Stove som vald romknapp medan senga låg i Soverom. Neste handling blir hovudsakleg forklart med tekst.

**Rett slik:** Gje generelle oppdrag namnet Valfri stad. Lukk uvedkommande panel ved Hent hjelp, oppdater rommarkeringa etter faktisk kamera/mål og vis ei kort dra-pil frå rett ting til rett figur eller møbel.

**Ferdig når:** Kvar hjelpereise endar med synleg startting og mål, samsvarande rommarkering og ei forklaring som kan forståast utan lesing.

Dokumentasjon: [15-crown-help](C:/topa/screenshots/audit-2026-10-03/15-crown-help.png), [18-bed-help](C:/topa/screenshots/audit-2026-10-03/18-bed-help.png), [35-camp-first-step](C:/topa/screenshots/audit-2026-10-03/35-camp-first-step.png). Kjelde for vidare arbeid: [ui/screens/TasksScreen.kt:157](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/TasksScreen.kt:157), [ui/TrollfossViewModel.kt:238](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/TrollfossViewModel.kt:238).

### F07 · P2 · Første minutt lærer ikkje den grunnleggjande leiken

**Observert:** Figurval er tydeleg, men det er inga kort biletvis innføring i å dra ein figur, gje han ein ting og prøve eit møbel. Rutenettet flyttar seg etter første spelarval.

**Rett slik:** Behald valfri start. Tilby ein kort leik med tre handlingar, til dømes ball → venn → sklie, med animert hand/pil og utan krav om tekst. Hald korta i ro når laget blir valt; gje første suksess straks.

**Ferdig når:** Eit barn som ikkje les kan velje ein figur og få ei morosam reaksjon innan første minutt. Barnet kan hoppe over hjelpa og få henne tilbake seinare.

Dokumentasjon: [01-first-start](C:/topa/screenshots/audit-2026-10-03/01-first-start.png), [02-team](C:/topa/screenshots/audit-2026-10-03/02-team.png), [06-after-onboarding](C:/topa/screenshots/audit-2026-10-03/06-after-onboarding.png). Kjelde for vidare arbeid: [ui/screens/PlayersScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/PlayersScreen.kt), [ui/screens/PlayScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt).

### F08 · P2 · Opprydding er trygg, men vanskeleg å forstå

**Observert:** Bordet vart bevart i Papirkorg og henta tilbake. Men det var vanskeleg å gripe rett møbel; radioen på bordet vart med til Lager. Lager, sekk, Rydd og Papirkorg er ulike system utan ei samla forklaring.

**Rett slik:** Vis kva som blir teke med før barnet slepper eit møbel på Lager. Bruk eit tydeleg ryddemodus med store markeringar og eit Hent tilbake-bilete rett etter sletting. Ha éin inngang til Alt eg har, med tydelege undergrupper.

**Ferdig når:** Barnet kan pakke bort rett ting, forstå kvar han er, slette til Papirkorg og hente tilbake utan å vere redd for at han er tapt.

Dokumentasjon: [27-storage-gesture](C:/topa/screenshots/audit-2026-10-03/27-storage-gesture.png), [28-recover-deleted-furniture](C:/topa/screenshots/audit-2026-10-03/28-recover-deleted-furniture.png), [75-beach-stored-radio](C:/topa/screenshots/audit-2026-10-03/75-beach-stored-radio.png). Kjelde for vidare arbeid: [domain/Designer.kt:20](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Designer.kt:20), [ui/screens/DesignerPanel.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/DesignerPanel.kt).

### F09 · P2 · Å pakke vekk ein spelar fjernar spelarvalet

**Observert:** Legg i sekken fjernar Hedda både frå rommet og laget. Når ho blir henta tilbake via Venner, kjem ikkje spelarplassen tilbake automatisk. Ho må veljast igjen via den andre, liknande Spelarar-knappen.

**Rett slik:** Skil tydeleg mellom Ta med meg vidare og Legg bort denne vennen. Gje valde spelarar ein enkel Pause / Bli med att-handling som bevarer identitet og spelarplass. Samle venner og spelarval i same forståelege panel.

**Ferdig når:** Eit barn kan ta figuren sin bort frå rommet og få han tilbake med same utsjånad og same spelarrolle, utan å miste styringa hans/hennar.

Dokumentasjon: [30-player-packed](C:/topa/screenshots/audit-2026-10-03/30-player-packed.png), [31-retrieved-player-team](C:/topa/screenshots/audit-2026-10-03/31-retrieved-player-team.png). Kjelde for vidare arbeid: [domain/Players.kt:16](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Players.kt:16), [ui/screens/FriendsPanel.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/FriendsPanel.kt).

### F10 · P2 · Samleboka hjelper lite når barnet står fast

**Observert:** Ei tom glimtstjerne kan trykkjast på utan respons. Ukjende oppskrifter blir mørke silhuettar, og tomt album er blankt. Den separate Bygg leiker-flata viser derimot farga delar, resultat og Hent delane hit.

**Rett slik:** La tomme stjerner opne eit gradvis hint med stadbilete og Gå dit. Bruk same farga handlingskort for oppskrifter som i Bygg leiker. Tomt album skal vise eit kamerabilete og Ta første bilete.

**Ferdig når:** Alle tomme og låste flater tilbyr ei konkret valfri handling. Barnet får hjelp utan at alle hemmelegheiter blir avslørte på ein gong.

Dokumentasjon: [08-secret-hint](C:/topa/screenshots/audit-2026-10-03/08-secret-hint.png), [09-recipes](C:/topa/screenshots/audit-2026-10-03/09-recipes.png), [10-empty-photos](C:/topa/screenshots/audit-2026-10-03/10-empty-photos.png), [33-play-recipes](C:/topa/screenshots/audit-2026-10-03/33-play-recipes.png). Kjelde for vidare arbeid: [ui/screens/BookScreen.kt:151](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/BookScreen.kt:151), [ui/screens/BookScreen.kt:282](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/BookScreen.kt:282).

### F11 · P2 · Langtidsprogresjonen sluttar ved nivå 3

**Observert:** Utgitt kode har tersklane 0, 2 og 5 merke, fire leiker på nivå 2 og fire på nivå 3, i tillegg til startverktøy og reparerbart tog. Det er ikkje eit ferdig system med mange seinare nivå. Nivå 3 vart ikkje opptent i denne runden; avgrensinga er stadfesta i kjelda.

**Rett slik:** Bygg vidare på den gode låst-leike-flata: Neste leike → tre konkrete biletval → Gå dit → liten feiring → bruk leika straks. Knyt nye etappar til nye måtar å leike på og fleire stader, framfor mykje repetisjon av same oppgåve. Ha ulike korte vegar for bygging, omsorg, utforsking og samarbeid.

**Ferdig når:** Barnet ser både neste belønning, kor langt det er att og minst éi forståeleg handling. Nytt nivå inneheld ferdige aktive leiker som er prøvde saman med figurar.

Dokumentasjon: [13-gift-catalogue](C:/topa/screenshots/audit-2026-10-03/13-gift-catalogue.png), 20-unlocked-gifts; kjeldekontroll Progression.kt. Kjelde for vidare arbeid: [domain/Progression.kt:5](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Progression.kt:5), [domain/Progression.kt:18](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/Progression.kt:18).

### F12 · P2 · Namn og symbol lovar ei anna handling

**Observert:** Gåver opnar Oppdrag. Prøv er berre ei grøn hake som andre stader tyder Ferdig. Barnet fekk oppdragsmerke medan det store stjernefeltet stod på 0/76, fordi det tel glimt.

**Rett slik:** La inngangen styre riktig fane. Bruk eit plassering-/leik-symbol på Prøv. Gje glimt og oppdragsmerke ulike attkjennelege symbol, og vis neste gåve i same framgangsstripe.

**Ferdig når:** Kvar knapp fører til det namnet/biletet lovar. Når barnet får eit merke, endrar den synlege framgangen seg i same augeblink.

Dokumentasjon: [12-gifts](C:/topa/screenshots/audit-2026-10-03/12-gifts.png), [16-crown-dropped](C:/topa/screenshots/audit-2026-10-03/16-crown-dropped.png), [21-ready-reward](C:/topa/screenshots/audit-2026-10-03/21-ready-reward.png). Kjelde for vidare arbeid: [ui/screens/TasksScreen.kt:70](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/TasksScreen.kt:70), [ui/screens/PlayScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt).

### F13 · P2 · Små mål og utydeleg respons på bomtreff

**Observert:** Første trykk ved lommelyktstrålen gav ingen synleg reaksjon; presist trykk på verktøyet opna menyen. Nokre drag trefte møbel eller panorering i staden for ønskt ting. Mating ved den tette dyregruppa vart ikkje sikkert stadfesta.

**Rett slik:** Gjer trefflatene romslege og vel synleg gjenstand i overlapp på ein føreseieleg måte. Vis eit lite grep/lys når tingen er vald, og eit tydeleg gyldig mål når barnet dreg. Ved feil mål, gje ein venleg visuell reaksjon og la barnet prøve igjen.

**Ferdig når:** Vanlege fingertreff nær små verktøy fungerer. Barnet kan sjå om det held ei gulrot, flyttar eit møbel eller panorerer.

Dokumentasjon: [38-torch-action](C:/topa/screenshots/audit-2026-10-03/38-torch-action.png), [39-torch-menu](C:/topa/screenshots/audit-2026-10-03/39-torch-menu.png), [27-storage-gesture](C:/topa/screenshots/audit-2026-10-03/27-storage-gesture.png), [81d-feed-animal](C:/topa/screenshots/audit-2026-10-03/81d-feed-animal.png). Kjelde for vidare arbeid: [ui/play/Engine.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/play/Engine.kt).

### F14 · P2 · Fullført eventyr blir ståande i andre rom

**Observert:** Du klarte det! Gåva ligg i sekken vart ståande gjennom vidare reiser til Trollhola, Storhuset og Stranda. Det tek plass og gir ei gamal oppgåve prioritet framfor den nye leiken.

**Rett slik:** Gje ein kort feiring, tydeleg Hent gåva og enkel lukkeknapp. Flytt ferdige eventyr til minneboka når barnet reiser vidare; la neste val vere valfritt.

**Ferdig når:** Ferdigmelding blir borte ved lukk eller vidare reise, og gåva kan alltid finnast att.

Dokumentasjon: [40-adventure-finished](C:/topa/screenshots/audit-2026-10-03/40-adventure-finished.png), [57-troll-cave](C:/topa/screenshots/audit-2026-10-03/57-troll-cave.png), [73-beach-arrival](C:/topa/screenshots/audit-2026-10-03/73-beach-arrival.png), [96-phone-beach](C:/topa/screenshots/audit-2026-10-03/96-phone-beach.png). Kjelde for vidare arbeid: [ui/screens/PlayScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/PlayScreen.kt), [domain/MagicPlay.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/domain/MagicPlay.kt).

### F15 · P2 · Tilgjengelegheit treng eigne handlingar og rett fokus

**Observert:** UI-treet inneheld romkontrollar bak dekkjande menyar. Canvas-figurar/småting har ikkje same individuelle etikettar som vanlege knappar; møbelkort bruker same generelle dra-instruksjon som namn. Dette er ein konkret semantikkrisiko, ikkje ein ferdig TalkBack-test.

**Rett slik:** Gje kvar figur/møbel eit eige namn og alternative handlingar som Flytt til, Gje til og Set på. Skjul bakgrunnssemantikk når ein dekkjande meny er open. Legg til bilete og valfri opplesing på dei viktige stega.

**Ferdig når:** Eiga TalkBack- og brytarstyringstest skal kunne velje figur, bruke leike og lukke meny utan å treffe bakgrunnskontrollar. Kontrast, stor tekst og redusert rørsle må målast særskilt.

Dokumentasjon: [01-first-start.xml](C:/topa/screenshots/audit-2026-10-03/01-first-start.xml), [04-hair-length.xml](C:/topa/screenshots/audit-2026-10-03/04-hair-length.xml), [65-saved-photo.xml](C:/topa/screenshots/audit-2026-10-03/65-saved-photo.xml), [74-beach-furniture.xml](C:/topa/screenshots/audit-2026-10-03/74-beach-furniture.xml). Kjelde for vidare arbeid: [ui/play/Engine.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/play/Engine.kt), [ui/screens/DesignerPanel.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/DesignerPanel.kt), [ui/components/Overlays.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/components/Overlays.kt).

### F16 · P3 · Bilete bør kjennast som barnets eigne minne

**Observert:** Biletet lagrar ønskjebobler/bagikon og kan ta med ein nesten skjult figur. Det finst inga enkel ramme som viser kva som blir med.

**Rett slik:** Ha ein valfri fotomodus med fri plass, gjøymde spelkontrollar, ramme og enkel førehandsvising. Tilby ulike morosame poseringar og minnesider frå fullførte eventyr.

**Ferdig når:** Barnet ser kva som blir fotografert og kan finne att biletet. Kameraet tek vare på rett scene og personlege figurar.

Dokumentasjon: [65-saved-photo](C:/topa/screenshots/audit-2026-10-03/65-saved-photo.png), [66-photo-open](C:/topa/screenshots/audit-2026-10-03/66-photo-open.png). Kjelde for vidare arbeid: [ui/screens/BookScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/BookScreen.kt), [ui/play/Engine.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/play/Engine.kt).

### F17 · P3 · Små layoutdetaljar svekkjer tilliten

**Observert:** Figurvala flyttar seg etter første val. Etasjeførehandsvisinga viser ei villaform for eit Tårnhus. OK-teksten i foreldreporten blir klipt til O. Kartknappar på mobil ligg over nokre stadnamn. Terningen ved Ferdig har utydeleg verknad før ein prøver han.

**Rett slik:** Stabiliser valrutenettet, bruk faktisk husstil i førehandsvising, rett knappestorleik/tekst og hald kartnamn fri for kontrollar. Gje tilfeldig figur ein tydeleg eigentleg funksjon og ei lokal angrehandling.

**Ferdig når:** Ingen nødvendige tekstar eller knappar blir klipte; førehandsvising samsvarer med barnet sitt val. Tilfeldig utsjånad kan enkelt angrast.

Dokumentasjon: [02-team](C:/topa/screenshots/audit-2026-10-03/02-team.png), [03-creator](C:/topa/screenshots/audit-2026-10-03/03-creator.png), [52-upper-floor-ready](C:/topa/screenshots/audit-2026-10-03/52-upper-floor-ready.png), [69-parent-gate](C:/topa/screenshots/audit-2026-10-03/69-parent-gate.png), [93-phone-map](C:/topa/screenshots/audit-2026-10-03/93-phone-map.png). Kjelde for vidare arbeid: [ui/screens/BuildPanel.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/BuildPanel.kt), [ui/screens/ParentScreens.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/ParentScreens.kt), [ui/screens/CreatorScreen.kt](C:/topa/.gradle-tmp/follow-worktree/app/src/main/java/app/trollfoss/ui/screens/CreatorScreen.kt).

## Dekning av stadene

«Visuell» er kontroll av ankomst og synleg oppbygging, ikkje gjennomspeling av alle aktivitetar. «Vanleg» betyr vanleg kart, rom, trapp eller bygghandling. Alle bileta vart inspiserte i denne runden.

| Stad | Inngang og kontroll | Dokumentasjon |
| --- | --- | --- |
| Familiehuset | Vanleg: oppdrag, seng, møblar/lager, foto og rombyte | [32-room-follow](C:/topa/screenshots/audit-2026-10-03/32-room-follow.png) |
| Fossen / skogen | Vanleg: bygde putehytte og fullførte Stjernenatt | [37-fort-seat](C:/topa/screenshots/audit-2026-10-03/37-fort-seat.png) |
| Trollhola | Vanleg: hjelpereise, tunnel og vidare til Storhuset | [57-troll-cave](C:/topa/screenshots/audit-2026-10-03/57-troll-cave.png) |
| Mitt hus: tomt | Vanleg: kroneoppdrag og val av hus | [43-house-panel](C:/topa/screenshots/audit-2026-10-03/43-house-panel.png) |
| Mitt hus: nede | Vanleg: bygde stove og kjøken | [50-built-room](C:/topa/screenshots/audit-2026-10-03/50-built-room.png) |
| Mitt hus: oppe | Vanleg: bygde soverom og prøvde riv-dialog | [55-demolish-confirmation](C:/topa/screenshots/audit-2026-10-03/55-demolish-confirmation.png) |
| Storhuset: kjellar | Vanleg: hemmeleg tunnel og trapp | [61-through-secret-tunnel](C:/topa/screenshots/audit-2026-10-03/61-through-secret-tunnel.png) |
| Storhuset: første høgda | Vanleg: trapp og romveljar | [63-manor-stairs](C:/topa/screenshots/audit-2026-10-03/63-manor-stairs.png) |
| Storhuset: andre høgda | Vanleg: trapp og ankomst | [64-manor-upstairs](C:/topa/screenshots/audit-2026-10-03/64-manor-upstairs.png) |
| Stranda | Vanleg: kartreise, radio frå Lager, kort mobilkontroll | [76-beach-radio-placed](C:/topa/screenshots/audit-2026-10-03/76-beach-radio-placed.png) |
| Bakeriet | Debugreise: visuell | [78-cafe](C:/topa/screenshots/audit-2026-10-03/78-cafe.png) |
| Frisøren | Debugreise: visuell | [79-salon](C:/topa/screenshots/audit-2026-10-03/79-salon.png) |
| Fjellet | Debugreise: visuell | [80-mountain](C:/topa/screenshots/audit-2026-10-03/80-mountain.png) |
| Garden | Debugreise; faktisk traktormeny, Stopp/Lukk og finn-att-portrett | [81c-find-player-after-tractor](C:/topa/screenshots/audit-2026-10-03/81c-find-player-after-tractor.png) |
| Romstasjonen | Debugreise: visuell | [82-space](C:/topa/screenshots/audit-2026-10-03/82-space.png) |
| Tivoliet | Debugreise: visuell | [83-tivoli](C:/topa/screenshots/audit-2026-10-03/83-tivoli.png) |
| Butikken | Debugreise: visuell | [84-shop](C:/topa/screenshots/audit-2026-10-03/84-shop.png) |
| Legekontoret | Debugreise: visuell | [85-doctor](C:/topa/screenshots/audit-2026-10-03/85-doctor.png) |
| Scena | Debugreise: visuell | [86-stage](C:/topa/screenshots/audit-2026-10-03/86-stage.png) |
| Havbotnen | Debugreise: visuell | [87-underwater](C:/topa/screenshots/audit-2026-10-03/87-underwater.png) |
| Heileberget | Debugreise: visuell | [88-heileberget](C:/topa/screenshots/audit-2026-10-03/88-heileberget.png) |
| Storhuset: loftet | Debugreise: visuell | [89-manor-attic](C:/topa/screenshots/audit-2026-10-03/89-manor-attic.png) |
| Storhuset: hagen | Debugreise: visuell | [90-manor-garden](C:/topa/screenshots/audit-2026-10-03/90-manor-garden.png) |
| Vagstaddalen | Debugreise; faktisk natt og vêr | [92-night-weather](C:/topa/screenshots/audit-2026-10-03/92-night-weather.png) |

## Tidlegare problem som vart kontrollerte på nytt

| Tidlegare problem | Resultat i denne runden |
| --- | --- |
| Lukke bok frå Kartet gir siste rom | Ikkje gjenskapt: tilbake til Kartet. |
| Appen startar i siste rom | Ikkje gjenskapt: kaldstart til Kartet. |
| Hårlengd verkar ikkje | På den prøvde krøllfrisyren vart håret faktisk lengre, med stabil hårrot. Ikkje generalisert til alle frisyrer eller til hårstorleik. |
| Sletting frå Lager mistar tingen | Bordet vart henta tilbake frå Papirkorg og sett ut igjen. Ingen permanent tap observert. |
| Kan ikkje fjerne folk | Legg i sekken fungerer, men påverkar òg spelarvalet og gjer tilbakekomst tungvint. |
| Kan ikkje dra frå Møblar | Boblemaskin vart dratt ut og brukt i Familiehuset. |
| Kan ikkje plassere på Stranda | Radio vart faktisk dratt frå Lager til Stranda. |
| Ting blir berre bokser | Boblemaskin, bord, radio og dei synlege stadene hadde gjenkjenneleg grafikk. Ingen ny reserveboks påvist; alle katalogkombinasjonar vart ikkje prøvde. |
| Figurar følgjer ikkje med | Begge kom med gjennom prøvde reiser/rombyte. Synlegheit og overlapp er framleis eit stort problem; to samtidige barn og jamn bildefrekvens er ikkje stadfesta i denne auditen. |

Etter kaldstart og den vidare runden var `players`, `styles`, `mine`, `stickers` og `discardedStorage` like som i lagringskopien før kaldstart. `storage` vart medvite tømt ved å setje radioen ut på Stranda; reisesnarvegen til Heileberget la til `berg_top`. Det er derfor ikkje rett å seie at heile lagringsfila er uendra.

## Plan for å gjere leiken betre

### Første bolk: barnet skal alltid ha kontroll

Rett F01–F03 først. Samstundes: fri plass ved ankomst, synlege spelarar, eitt tydeleg finn-att-trykk, like reglar for Prøv og draging, og begge knappar i riv-dialogen. Flytt botnstjerna frå aktive mål. Verifiser med ei fersk verd og to figurar på nettbrett; berre ein kort mobilkontroll.

**Prøvesløyfe:** vel to figurar → krone → seng → Prøv boblemaskin → dra/pakke/hente tilbake → bygg to rom og andre etasje → riv/avbryt → reise → finn begge → kaldstart. Ho skal lykkast utan skjult kontroll, uriktig fullt-melding eller tapt spelarrolle.

### Andre bolk: barnet skal forstå kva det kan gjere

Samle spelar/venner-val og husoversikt. Gjer hjelp visuelt med korte dra-piler. Rett Gåver-fana, rommarkering og framgangssymbol. Gje samleboka hint og alle tomme flater ei første handling. La fullførte eventyr flytte til minneboka når barnet går vidare. Rydd scene for scene; prioriter heilt frie flater rundt spelarane og dei neste aktivitetsmåla.

**Prøvesløyfe:** eit barn som ikkje les skal kunne starte ei leik, forstå kva som manglar til neste gåve, hente rette delar og bruke gåva straks. Alle nye barnetekstar finst som `Txt(nn, nb)`.

### Tredje bolk: meir magi og lengre levetid

Utvid progresjonen med ferdige, aktive leiker i små etappar. Kvar etappe får nokre ulike vegar: bygg noko, hjelp ein venn/dyr, finn ein hemmeleg stad eller gjer noko saman. Vis neste belønning som eit stort bilete og eit lite tal forståelege handlingar. Unngå at belønninga berre blir eit katalogkort; ho skal starte ein ny leik med personane.

Gode vidare retningar er ein vennebuss/togtur med stopp barnet vel, matlaging og kafé med rollebyte, ein oppfinnarmaskin der barna kombinerer ting, eit hus med valfri romhistorie, dyr som reagerer på omsorg, og eit minnealbum som lagrar barnas eigne historier. Behald fri leik mellom etappane, og gje to barn kvar sin synlege figur, plass og enkel måte å delta på. Dette er forslag til vidare arbeid, ikkje påstandar om ferdige funksjonar.

### Korte prøver med barn før ein stor innhaldsauke

Følg eit yngre barn som ikkje les og eit eldre barn, deretter to barn saman på nettbrett. La dei prøve utan vaksenforklaring først. Sjå etter om dei finn figuren sin, skjønar neste gåve, kan setje ting bort og hente dei tilbake, finn fram i huset og får til ein aktivitet saman. Noter kvar dei stoppar eller spør. Bruk funna til å justere hjelpa og trefflatene; ikkje berre legg til meir innhald.

## Sporbare arbeidsnotat

Dei følgjande notata er frå denne runden; fleire skjermbilete høyrer til same hovudsteg. Dei skil faktiske handlingar frå visuell kontroll.

- **Steg 3, Kart frå publisert 1.6.1 (Friksjon)** — [00-release-map](C:/topa/screenshots/audit-2026-10-03/00-release-map.png): Reisemål er store og ulike. Fleire stader ligg utanfor skjermen; hintet om sidelengs draging er berre tekst. Raud X tek barnet ut av kartet utan å forklare kvar det kjem. Fire nedre ikon og nivåfelt konkurrerer med kartet.

- **Steg 1, Første oppstart (Friksjon)** — [01-first-start](C:/topa/screenshots/audit-2026-10-03/01-first-start.png): Ny figurveljar har store portrett, valfri Vel seinare og ingen tidspress. Byggje-, spel- og figurvalkontrollar er samstundes med i UI-semantikken bak den dekkjande veljaren. Første start har ikkje ei kort biletvis forklaring på å dra eller leite etter ting.

- **Steg 1, To spelarar (Fungerer)** — [02-team](C:/topa/screenshots/audit-2026-10-03/02-team.png): Hedda og Alva får tydeleg gul markering og nummer 1/2. Eit nytt lag med valde portrett skyv alle figurkorta nedover etter første val; risiko for at neste fingertykk hamnar på feil rad.

- **Steg 2, Endre vald spelar (Friksjon)** — [03-creator](C:/topa/screenshots/audit-2026-10-03/03-creator.png): Stor førehandsvising og 18 biletfrisyrer. Fanene Hår/Ansikt og kropp/Klede har berre tekst, sjølv om undermenyane har bilete. Tilfeldig figur er eit anonymt terningikon heilt ved Ferdig; bør ha enkel gjenoppretting og tydeleg lokal verknad.

- **Steg 2, Hårlengd (Fungerer)** — [04-hair-length](C:/topa/screenshots/audit-2026-10-03/04-hair-length.png): Glidebrytar med tre biletdøme. UI-treet har fleire like Hårlengd-etikettar og kontrollar frå bakgrunnens byggjepanel; faktisk TalkBack vart ikkje prøvd.

- **Steg 2, Dra hårlengd (Fungerer)** — [05-hair-max](C:/topa/screenshots/audit-2026-10-03/05-hair-max.png): Faktisk draging til høg verdi gir lengre lokkar rundt øyra/nakken. Hovudet og hårrota held same storleik. Dette tidlegare problemet lét seg ikkje gjenskape på den aktuelle kjelda.

- **Steg 3, Etter første spelarval (Friksjon)** — [06-after-onboarding](C:/topa/screenshots/audit-2026-10-03/06-after-onboarding.png): Ferdig i verkstaden og Vi er klare fører til Kartet. To personlege figurar sit i sengballongen. Barnet må lese reisemålsnamn og teksthint for å finne fleire stader utanfor skjermen.

- **Steg 4, Oppdagingsboka, ny verd (Friksjon)** — [07-discovery-book](C:/topa/screenshots/audit-2026-10-03/07-discovery-book.png): Første bokflate er 76 tomme stjerner, stadnamn og fire tekstfaner. Ingen bilete av handlingane eller tydeleg Neste leik. 'Glimt', 'Klistremerke', 'Oppdrag', 'Gåver' og nivå er spreidde mellom ulike bøker/knappar; barnet må lære fleire omgrep for same framgang.

- **Steg 4, Trykk på tom stjerne (Blindgate)** — [08-secret-hint](C:/topa/screenshots/audit-2026-10-03/08-secret-hint.png): Faktisk trykk på første tomme stjerne gav ingen ny skjerm, hint, respons eller reiseknapp. Kjelda stadfestar at stjernene er passive Canvas-teikningar. Boka hjelper ikkje barnet med den neste oppdaginga.

- **Steg 4, Oppskrifter før oppdaging (Blindgate)** — [09-recipes](C:/topa/screenshots/audit-2026-10-03/09-recipes.png): Ingrediensar og resultat er mørke silhuettar mot lilla bakgrunn. Til dømes egg og mjølk ved benken er ikkje viste i sine verkelege fargar. Ingen reiseknapp eller steg ved korta; stor mørk samlebok hjelper lite når barnet ikkje veit kor det skal starte.

- **Steg 4, Tomt fotoalbum (Blindgate)** — [10-empty-photos](C:/topa/screenshots/audit-2026-10-03/10-empty-photos.png): Bilete 0 gir heilt tom flate. Ingen kameraillustrasjon, forklaring eller knapp Ta første bilete. Dette er ein faktisk tomtilstand, ikkje lasting.

- **Steg 3, Lukke bok frå Kartet (Fungerer)** — [11-back-to-map](C:/topa/screenshots/audit-2026-10-03/11-back-to-map.png): Raud X i boka returnerer til Kartet; den tidlegare feilen med å hamne i siste rom vart ikkje gjenskapt.

- **Steg 5, Trykk Gåver på Kartet (Feil)** — [12-gifts](C:/topa/screenshots/audit-2026-10-03/12-gifts.png): Gåver-knappen opnar Oppdrag-fana, ikkje Gåver. Barnet får ein annan skjerm enn namnet lovar. Tre oppdrag har klare bilete, men Hent hjelp ser ut som Kart/reiseikon. Kroneoppdraget oppgir Kart som stad sjølv om handlinga må gjerast i eit rom.

- **Steg 5, Gåvefana (Fungerer med friksjon)** — [13-gift-catalogue](C:/topa/screenshots/audit-2026-10-03/13-gift-catalogue.png): Fullfargebilete av gratis og låste leiker; ingenting krev kjøp. Nivå 2/3 og lås er tydelege. Første skjerm er mest låst innhald; reparer-tog-kortet ligg nedanfor og har ingen synleg inngang på første skjerm.

- **Steg 5, Låst boblemaskin (Fungerer)** — [14-locked-reward](C:/topa/screenshots/audit-2026-10-03/14-locked-reward.png): Detaljkortet forklarer 2 manglande merke og viser tre handlingsbilete/reiseknappar. Dette er den beste inngangen til konkret framgang. Android-systemlinjer dukkar opp over/under spelet når dialogen opnar. Ingen animert dra-forklaring eller opplesing vart synleg.

- **Steg 6, Reise til kroneoppdraget (Friksjon)** — [15-crown-help](C:/topa/screenshots/audit-2026-10-03/15-crown-help.png): Reiseknappen henta krona ved Hedda på Mitt hus-tomta; 'Kart' i oppdragskortet var feil stadnamn. Byggjepanelet Vel eit hus vart ståande ope og dekte høgre del av scena, sjølv om barnet no bad om hjelp til kroneleiken. Ingen dra-pil frå krona til hovudet.

- **Steg 6, Faktisk oppdragsfullføring (Fungerer med friksjon)** — [16-crown-dropped](C:/topa/screenshots/audit-2026-10-03/16-crown-dropped.png): Krona vart dratt på Hedda; den same tingen 357 vart WORN av spelar 25, og eitt merke vart lagra. Oppdragsbadgen gjekk 3→2. Det store stjernetal-feltet heldt seg 0/76 fordi det tel glimt, ikkje oppdragsmerke. To ulike framgangsteljarar bruker stjernesymbol og kan forvirre.

- **Steg 6, Opptent merke (Fungerer)** — [17-task-progress](C:/topa/screenshots/audit-2026-10-03/17-task-progress.png): Boka viser merke 1, 1 att til neste nivå og grøn hake på fullført kroneoppdrag. Framgang er tydeleg når boka er open.

- **Steg 6, Hjelp til sengoppdrag (Friksjon)** — [18-bed-help](C:/topa/screenshots/audit-2026-10-03/18-bed-help.png): Begge spelarar og krona reiste med. Hjelpa tok oss nær senga, men romveljaren markerte Stove sjølv om oppdraget var i Soverom. Hedda stod delvis bak leikekassa. Ingen tydeleg målpil viste sengas droppunkt.

- **Steg 6, To ekte oppdrag opnar nivå 2 (Fungerer)** — [19-level-unlock](C:/topa/screenshots/audit-2026-10-03/19-level-unlock.png): Dra Hedda til senga gav andre merke og nivå 2. Rolig lite Nye leiker til deg-kort med fire bilete, utan tidspress eller fullskjermavbrot. Hedda låg i senga med krona si og same ID. Bileta har ingen eigne etikettar/knappar i UI-treet; heile kortet må oppdagast som inngang.

- **Steg 5, Opna gåver (Fungerer)** — [20-unlocked-gifts](C:/topa/screenshots/audit-2026-10-03/20-unlocked-gifts.png): Trykk på ny-leike-kortet opnar riktig Gåver-fane. Alle fire nivå 2-leiker har Prøv og er ulåste; neste nivå viser 3 att. Ingen merke blir brukte opp.

- **Steg 7, Prøv ulåst leike (Friksjon)** — [21-ready-reward](C:/topa/screenshots/audit-2026-10-03/21-ready-reward.png): Fungerande Prøv-kontroll er berre ei grøn hake, som elles betyr Ferdig/fullført. Barnet kan tru det lukkar kortet, medan det faktisk set ut eit nytt møbel. Bruk leike-/plasseringssymbol og vis kvar det kjem.

- **Steg 7, Første nye leike får ikkje plass (Blindgate)** — [22-reward-in-room](C:/topa/screenshots/audit-2026-10-03/22-reward-in-room.png): Etter to ekte oppdrag i ei fersk verd stoppar Prøv på boblemaskina med Her er fullt. Legg ei leike på lager og prøv igjen. Standard Familiehuset er fullt. Ingen direkte rydding, anna plass eller snarveg er tilbydd i feilmeldinga. Seinare faktisk draging frå Møblar plasserte boblemaskina i same rom. 'Fullt' gjeld automatisk fri golvplass i ToyPlay.claim, ikkje alle plasseringar. Dette er motstridande reglar, ikkje fullt møbel-ID-lager.

- **Steg 8, Dra frå Møblar (Fungerer med friksjon)** — [25-drag-reward-from-furniture](C:/topa/screenshots/audit-2026-10-03/25-drag-reward-from-furniture.png): Boblemaskina vart faktisk dratt ut og laga bobler i rommet. Møblar fungerer sjølv om Prøv melder fullt. Tingen kom tett på sofa/bord/radio; behov for synleg plasseringssilhuett og frie mål.

- **Steg 8, Legg møbel på lager (Fungerer med friksjon)** — [27-storage-gesture](C:/topa/screenshots/audit-2026-10-03/27-storage-gesture.png): Eit dra-forsøk tok bordet, med radio som vertsbarn, til Lager. Begge vart bevarte og Lager opna automatisk. To tidlegare forsøk trefte/panorera i staden; overlappande småting gjer grepet lite føreseieleg.

- **Steg 8, Papirkorg og tilbakehenting (Fungerer)** — [28-recover-deleted-furniture](C:/topa/screenshots/audit-2026-10-03/28-recover-deleted-furniture.png): Bordet vart sletta frå Lager til Papirkorg, og Hent tilbake førte det til Lager. Faktisk draging sette det tilbake i rommet. Ingen permanent tap observert. Radioen vart verande på Lager. Rydding, lager, papirkorg og sekk treng eit felles forståeleg biletspråk.

- **Steg 9, Fjerne ein person frå rommet (Friksjon)** — [30-player-packed](C:/topa/screenshots/audit-2026-10-03/30-player-packed.png): Legg i sekken fjernar Hedda frå rommet og samstundes frå spelarteamet; Alva blir spelar 1. Tilbakehenting gjennom Venner gir ikkje automatisk den opphavlege spelarplassen tilbake. Må velje Hedda på nytt i Spelarar. Funksjonen fungerer, men person/venn/spelar/sekken er fire omgrep og to nærlike toppikon.

- **Steg 9, Flytte to spelarar mellom rom (Fungerer med friksjon)** — [32-room-follow](C:/topa/screenshots/audit-2026-10-03/32-room-follow.png): Romval til Kjøken fekk begge figurane fram på golvet, utsjånad og krone bevarte. Figuren ved midten blir delvis dekt av den faste Leik og eventyr-knappen. Dette er ikkje ei måling av bildefrekvens eller ei prøve med to samtidige fingrar.

- **Steg 10, Bygg leiker (Fungerer)** — [33-play-recipes](C:/topa/screenshots/audit-2026-10-03/33-play-recipes.png): Putehytte og trillevogn har farga delar, resultat, kort dra-forklaring og Hent delane hit. Mykje meir forståeleg enn den mørke oppskriftsboka.

- **Steg 10, Start Stjernenatt i hytta (Friksjon)** — [35-camp-first-step](C:/topa/screenshots/audit-2026-10-03/35-camp-first-step.png): Reiseknappen skaffar rette delar og følgjande figurar. Målhintet er berre tekst; botnstjerna ligg over arbeidsområdet. Stor putehytte vart faktisk bygd ved å dra pute til teppe.

- **Steg 10, La venn sitje i hytta (Fungerer med friksjon)** — [37-fort-seat](C:/topa/screenshots/audit-2026-10-03/37-fort-seat.png): Hedda kunne dragast til hytta og framgangen oppdaterte seg til Lyse. Den andre valde spelaren Alva vart heilt skjult bak den store nybygde hytta. Treng synlege figurar eller tydeleg flytt-fram-knapp.

- **Steg 10, Bruke lommelykta (Fungerer med friksjon)** — [39-torch-menu](C:/topa/screenshots/audit-2026-10-03/39-torch-menu.png): Eit presist trykk gav Lyse/Stjernelys med gode handlingsbilete. Første trykk ved strålesida fekk ingen respons; treng romsleg tydeleg trefflate på små verktøy.

- **Steg 11, Husoversikt og romval (Friksjon)** — [47-building-options](C:/topa/screenshots/audit-2026-10-03/47-building-options.png): Fire fine husval, fem plassar i kvar etasje og tydeleg gul vald plass. Vanleg Bygg her i romveljaren fokuserer berre tom plass om byggjepanelet er lukka; ingen kortliste før eit nytt trykk på Bygg. Ved tom plass vart begge figurar utanfor skjermen, fordi dei ikkje skal gå over ubygd golv. Barnet treng ein synleg veg tilbake.

- **Steg 11, Byggje eit rom (Fungerer)** — [49-room-preview](C:/topa/screenshots/audit-2026-10-03/49-room-preview.png): Farga romkort, stor førehandsvising, Bygg dette rommet og roleg byggjeanimasjon. Faktisk bygde stove og kjøken; begge spelarar stod framme på ferdig golv. Bygg neste rom og Leik i rommet er gode snarvegar.

- **Steg 11, Byggje andre etasje (Fungerer med friksjon)** — [52-upper-floor-ready](C:/topa/screenshots/audit-2026-10-03/52-upper-floor-ready.png): To rom gjer kravet tydeleg og knappen skiftar frå grå til grøn. Førehandsbiletet viser likevel ei villaform trass i at vald stil var Tårnhus. Etasjeskiftet må gjerast etter byggjeanimasjonen; ei rask handling under bygging fekk ingen synleg varig respons.

- **Steg 11, Rive rom (Feil)** — [55-demolish-confirmation](C:/topa/screenshots/audit-2026-10-03/55-demolish-confirmation.png): Riv rommet?-dialogen viser berre Behald, sjølv på nettbrett. Riv-knappen er pressa ut til høgre og ikkje tilgjengeleg ved sveip. Kjelda viser to BigButton i Row utan avgrensa breidd/vekter; breiddfordelinga er den sannsynlege årsaka. Behald avbryt trygt. Riving vart ikkje gjennomført fordi kontrollen er utilgjengeleg.

- **Steg 12, Trollhola og hemmeleg tunnel (Friksjon)** — [57-troll-cave](C:/topa/screenshots/audit-2026-10-03/57-troll-cave.png): Tunnelen er no tydeleg merkt Hemmeleg tunnel til Storhuset med hus-og-pil-skilt. Vanleg ankomst fokuserer midten, medan tunneldøra er mot venstre. Småting og andre figurar overlappar spelarane. Fullført-eventyrbanner blir ståande også i ei ny, uvedkommande hole.

- **Steg 12, Tunnel og trappereise (Fungerer med friksjon)** — [61-through-secret-tunnel](C:/topa/screenshots/audit-2026-10-03/61-through-secret-tunnel.png): Faktisk trykk i tunneldøra førte til kjellaren, med begge spelarar, Hedda si krone og same utsjånad. Faktiske trappetrykk førte vidare til Storstova og Andre høgda. Ankomst oppe legg figurar bak trapperekkverk/andre folk; romval over store avstandar kan vise tom scene medan spelarane går inn frå sida. Nøkkelsamlinga er fem grå symbol utan forklaring i første møte.

- **Steg 13, Ta og sjå bilete (Fungerer med friksjon)** — [65-saved-photo](C:/topa/screenshots/audit-2026-10-03/65-saved-photo.png): Ta bilete laga eit faktisk lagra foto i albumet; trykk på det forstørrar. Fotoet tek med bag-/ønskjeikon og nær heilt skjult figur, utan førehandsvising. Tomt album manglar første handling. Underliggjande romkontrollar er eksponerte i UI-treet også når boka er open.

- **Steg 14, Kaldstart (Fungerer)** — [68-cold-start-ready](C:/topa/screenshots/audit-2026-10-03/68-cold-start-ready.png): Ekte force-stop og normal oppstart gir Kartet etter oppstartsillustrasjonen. To spelarar, nivå 2 og personlege utsjånader synlege. Etterfølgjande samanlikning av eigne lagringsdata viser uendra players, styles, mine, stickers og discardedStorage. Lageret vart medvite tømt ved å setje ut radioen på stranda seinare; reiserunda la til berg_top. Ikkje påstand om at heile lagringsfila er identisk.

- **Steg 15, Foreldreside og språk (Fungerer med friksjon)** — [71-reset-confirmation](C:/topa/screenshots/audit-2026-10-03/71-reset-confirmation.png): Faktisk multiplikasjonsport 9×6 opnar innstillingane; OK-tasten blir klipt til O på nettbrett. Nynorsk/Bokmål byter tekst. Begynn på nytt har tydeleg åtvaring og to fullt synlege stablede knappar; Avbryt vart faktisk brukt og verda behalden. Foreldreoversikta viser funn/oppskrifter men ikkje nivå/merke.

- **Steg 16, Stranda og lager på uteplassar (Fungerer med friksjon)** — [76-beach-radio-placed](C:/topa/screenshots/audit-2026-10-03/76-beach-radio-placed.png): Normal kartreise til Stranda med begge spelarar. Faktisk drag av radio frå Lager plasserer han med rett radioillustrasjon; ingen boks. Sidepanelet flyttar kameraet slik at Alva blir delvis utanfor venstrekanten. Leikeområdet har mykje små rusk og svært lite høgd samanlikna med den store himmelen. Fullført eventyr ligg framleis over ei anna scene.

- **Steg 16, Bakeriet (Visuelt lesbart)** — [78-cafe](C:/topa/screenshots/audit-2026-10-03/78-cafe.png): Visuell ankomst via debug-reisesnarveg. Mat og apparat attkjennelege, god bakgrunn/forgrunn. Tre personar overlappar ved start; forteljande tavle meir lesbar enn små gjenstandar.

- **Steg 16, Frisøren (Visuelt lesbar med friksjon)** — [79-salon](C:/topa/screenshots/audit-2026-10-03/79-salon.png): Visuell debugreise. Sakser, fønar, stolar og hårfargar attkjennelege. Fire figurar tett samla og små briller nedst; den sentrale Leik-knappen dekkjer beina til Hedda.

- **Steg 16, Fjellet (Visuell friksjon)** — [80-mountain](C:/topa/screenshots/audit-2026-10-03/80-mountain.png): Visuell debugreise. Flott hoppbakke men sjølve leiken samla i nedste kvartdel. Snøballar svært små og Leik-knapp dekker midtfiguren, tre personar overlappar.

- **Steg 16, Garden og traktoren (Fungerer med friksjon)** — [81b-tractor-actions](C:/topa/screenshots/audit-2026-10-03/81b-tractor-actions.png): Debugreise til Garden; vanleg trykk på traktoren viser store venstre/stopp/høgre-knappar og kamera følgjer han. Stopp/Lukk prøvd; trykk på Hedda-portrettet finn att henne og Alva blir med inn i synsfeltet. Mange dyr overlappar og gulrot ved traktoren var vanskeleg å dra presist til dyret; mating ikkje sikkert stadfesta i dette forsøket.

- **Steg 16, Romstasjonen (Visuelt lesbar)** — [82-space](C:/topa/screenshots/audit-2026-10-03/82-space.png): Visuell debugreise. Sveving og stor rakett gir staden eige preg, spelarane tydelegare framme og lite rusk. Funksjonane til det vesle planetapparatet/raketten ikkje prøvde.

- **Steg 16, Tivoliet (Visuelt spennande med friksjon)** — [83-tivoli](C:/topa/screenshots/audit-2026-10-03/83-tivoli.png): Visuell debugreise. Karusell/pariserhjul/trampoline har stor og tydeleg grafikk, men mykje konfetti i golvet blandar pynt og faktiske småting. Køyring og alle seter ikkje prøvde.

- **Steg 16, Butikken (Visuell friksjon)** — [84-shop](C:/topa/screenshots/audit-2026-10-03/84-shop.png): Visuell debugreise. Fine matkategoriar med biletskilt, men fruktkasse skjuler Alva under haka og robotstøvsugar/knapp deler same golvplass. Ingen påvist grafisk reserveboks; fruktkassa er ei teikna kasse med mat.

- **Steg 16, Legekontoret (Visuelt lesbart)** — [85-doctor](C:/topa/screenshots/audit-2026-10-03/85-doctor.png): Visuell debugreise. Ro og få figurar, stor undersøkjingsbenk/røntgen og bilete. Instrumenta på bordet er små; berre visuell kontroll, ikkje behandling prøvd.

- **Steg 16, Scena (Visuelt lesbar)** — [86-stage](C:/topa/screenshots/audit-2026-10-03/86-stage.png): Visuell debugreise. Tydelig scene/skilje/gjenkjennelege instrument og lite golvpynt, store figurar. Lyd ikkje vurdert; alle instrument ikkje spela.

- **Steg 16, Havbotnen (Visuelt spennande med friksjon)** — [87-underwater](C:/topa/screenshots/audit-2026-10-03/87-underwater.png): Visuell debugreise. Ubåt, svev og stort skjell gir særpreg. Skjellet skjuler Hedda nesten heilt under andletet, medan Leik-knappen dekker skjellet/brillene. Ubåt og skatt ikkje prøvde.

- **Steg 16, Heileberget (Visuell friksjon)** — [88-heileberget](C:/topa/screenshots/audit-2026-10-03/88-heileberget.png): Visuell debugreise. Heile Hedda skjult av ein større lokal figur ved ankomst, trass synleg spelarportrett. Stor scene utan oversiktskart i sjølve rommet; vidare klatring ikkje gjennomspela.

- **Steg 16, Loftet (Visuell friksjon)** — [89-manor-attic](C:/topa/screenshots/audit-2026-10-03/89-manor-attic.png): Visuell debugreise. Stemningsfullt lys, stor nedoverpil og fire romsnarvegar. Spøkelset og andre figurar skjuler Hedda; grå nøklar øvst utan tekst, skattelås ikkje prøvd.

- **Steg 16, Hagen (Visuell friksjon)** — [90-manor-garden](C:/topa/screenshots/audit-2026-10-03/90-manor-garden.png): Visuell debugreise. Alva delvis bak benk/bed, mange små nissar, pynt og krus. Drivhus/vatn/plantefelt tydelege men ruten vidare ikkje vist i eit oversiktskart.

- **Steg 16, Vagstaddalen (Visuelt rolegare)** — [91-valley](C:/topa/screenshots/audit-2026-10-03/91-valley.png): Visuell debugreise. Færre folk og enklare scene, god plass rundt begge spelarar. Bål og leirstad ikkje gjennomspelte; bakgrunnen mindre detaljert enn dei andre stadene.

- **Steg 17, Dag, natt og vêr (Fungerer)** — [92-night-weather](C:/topa/screenshots/audit-2026-10-03/92-night-weather.png): Faktisk måne-/vêrtrykk gir synleg natt og regn utan å stoppe leiken. Knappen byter til Dag. Skjermen er mørkare og små instrument krev framleis presisjon; lyd ikkje vurdert.

- **Steg 18, Kort mobilkontroll (Friksjon)** — [93-phone-map](C:/topa/screenshots/audit-2026-10-03/93-phone-map.png): Kort kontroll av tre ulike flater på 2400×1080 / 420 dpi: Kart, figurverkstad og Stranda. Eit ekstra bilete 95 viser ny retur til Kartet. Kartknappar dekkjer familiehus-/havbotnnamn; figurverkstaden har stor preview og rulling. Meir-knappen samlar øvre leikekontrollar, men den faste Leik-knappen ligg framleis i golvbandet. Full mobilrunde ikkje køyrd.
