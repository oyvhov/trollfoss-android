# Heileberget – visuell og interaktiv kontroll

Pågåande nattarbeid 2.–3. november 2026. Ekte Android/Compose/GLES, med original kunst
modellert i Blender gjennom MCP etter uttrykkeleg ønske frå brukaren.
Konseptet i `docs/design/heileberget/selected-concept.png` er ei retning, ikkje eit spelbilete.

Målformat: mobil 2400 × 1080 / 420 dpi og nettbrett 1920 × 1200 / 240 dpi, liggjande.
Skjermbilete under `C:/topa/dist/heileberget-3d-2026-11-02/` kjem frå MainActivity på vår
isolerte prøvepakke. Blender-bileta under `dist/heileberget-blender/` har anna lys og er ikkje
bevis for korleis Android-utgåva ser ut.

## Runde 2 – faktisk gjennomgått på begge format

Bilete: `round2-phone-{outside,cave,deep,children}.png` og tilsvarande `round2-tablet-*`.

- **Figurar:** Eira/Eilev har mjukare ansiktsformer etter brukaren si tilbakemelding. Dei kan
  skiljast frå kvarandre i frontvisinga. Spreidd følgje gjer Eira synleg mellom dei andre.
  Den generiske vaksenfiguren har framleis enklare hår/ansikt og treng meir sams stil.
- **Utsikt:** lågare kameravinkel og større avstand til bakgrunnsfjellet opnar himmelen.
  Fjellmassiva såg framleis ut som klossar, og skyene som flate skiver. Dette er reelle
  visuelle manglar, ikkje berre skilnader frå konseptet.
- **Hole:** trollet, peisen, kakebordet og matlageret er synlege. Støtta langs galleriet
  følgjer no bakken. Svarte overdimensjonerte trepluggar og svart sti er retta.
  Ved bakre skattkammer tek ei nærliggjande søyle for mykje av biletet.
- **Lys:** samla salvie/blålilla/honning-palett, men lyset er nokså flatt. Dei ugjennomsiktige
  grøne skuggane under figurane ser ut som plater. Meir mjuke kontaktar trengst.
- **Kontrollar:** separate tommelspakar, kart/portrett øvst, handling/sekk til høgre.
  Mobilknappane er store og tydelege. Handlingsetiketten visast nær rett knapp.
  Risting bruker førebels sol/måne-symbol, som ikkje forklarar handlinga godt nok.
- **Flyt:** det er lett å sjå inngangen og rampen. Fleire dekorasjonar ser handterbare ut,
  men har inga handling enno; innhaldet treng fleire små svar på barnet sine initiativ.

Runde 2: 715 JVM-testar, 3 Android-testar på mobil og lint (0 feil / 32 eksisterande åtvaringar).
Nettbrettets gangtest feila fordi testoppsettet arva lagra yaw frå frontfotoet; han nullstilte
posisjon, men ikkje kameraretning. Testoppsettet er retta og skal køyrast om att. Dette var
ikkje grunnlag for å friskmelde nettbrettprøva.

## Runde 3 – endringar

- Samla granittvegg med avrunda platå og romlege fjell i horisonten erstattar blokkfjella.
- Volumskyer med eigne lyse/mørke flater erstattar flate skiver.
- Mjuke, blanda kontaktskuggar erstattar grøne plater under figurane.
- Kameraarmen prøver treff mot søyler og holevegger før han plasserer kameraet.
- To Blender-harar blir sjølvstendige modellar. «Hels på haren» gir snuing og små hopp,
  med stille respons når redusert rørsle er valt. Handlinga kostar ingenting.
- Gullpanna er sett på høgda til den tørre breidda ho vart flytta til.

## Runde 4–6 – nye faktiske gjennomgangar

Runde 4 og 5 har tre grøne Android-testar på kvart format. Runde 5 har 718 JVM-testar og lint
utan feil (32 eksisterande åtvaringar). Runde 6 har også tre grøne Android-testar per format.
Alle har fire MainActivity-bilete per format i artefaktmappa. Innspel frå desse bileta:

- Dei grøne skuggeplatene er borte. Følgjet er lettare å skilje frå bakken.
- Kameraikonet forklarar risting betre, og den lyse glipa under fjellhorisonten er lukka av fjordvatn.
- Den nye Øyvind-modellen høyrer betre saman med barna: smalare hovud, mjuke øyre, tilpassa skjegg,
  skjortekrage og normale bein. Har framleis stilisert, svært jamt greidd hår.
- Ny lyktbaking gir synlege varme soner på holegolvet, særleg ved peisen. Framleis rom for
  meir form i breie veggflater. Søyla ved bakre skattkammer kan dominere kanten av biletet.
- Bergveggen har slutt på dei jamne loddrette stripene, men er no nokså mjuk i forma.
  Fjellendane er synleg avskorne på brei mobil. Ny avtrapping blir eksportert i runde 7.
- **Skyfeil berre synleg i Android:** bakflater blir teikna over framflater når djupneskriving
  er av for heile himmelmeshen. Rettast i runde 7; må sjåast på nytt, ikkje friskmeldast frå Blender.

Runde 7 legg òg til smuttkast, rett retning/høgd på ting i handa og handlingsnamn som «Gi Urge»
når barnet står hos Rumle med brus. 719 JVM-testar og tre Android-testar per format bestod,
lint 0 feil / 32 eksisterande åtvaringar. Skyene er reine i Android, fjellendane avrunda.
Faktisk smuttopptak (`round7-smutt.mp4`) viste at ei bjørk skjuler kastet. Dette skal rettast;
ein grøn funksjonstest er ikkje nok til å godkjenne presentasjonen.

## Runde 8–15 – kontroll i sjølve Android-spelet

- Runde 8: smuttkastet er synleg på faktisk video. Brei suite: 111 Android-testar per format.
- Runde 9: kake går til munnen til Rumle, og han vinkar. Gull har varm metallfarge.
- Runde 10–12: utsiktsplassen vender mot fjorden, terrengskjøten er tetta og holeopninga sit
  i ein bergkropp. Lauvblad følgjer krona, og Rumle sin munnkant har jamt tverrsnitt.
- Runde 12–15: faktiske spakprøver på begge format stadfestar gange, kamerastyring og fil-lagring
  medan appen framleis er framme. Siste kameravinkel vart tidlegare berre lagra ved bakgrunnsbytte.
- Runde 13: tre krystallar med ulike tonar, lys og ringar. Video er utan lyd og dokumenterer
  berre det visuelle svaret. Peisen skjulte nedste del av biletet ved krystallane.
- Runde 14: kameraet tek omsyn til møblane si høgd; krystallplassen er no synleg. Kontinuerleg
  hår på Eilev er betre enn tjukke enkeltkrøllar. Runde 15 rettar innovervende hårnormalar.
- Runde 15: seks faktiske scenebilete per format, inkludert front/bakside av familien, matplassen,
  krystallar, varde og gull. Pilarane har meir mineralvariasjon. Fjellveggen er litt for jamn;
  mindre brot/hyller kjem i 16. Ei overlappande skulderflate på Rumle blir sameina i 16.
- **Ny kamerafeil frå ekstra bilete 15:** bak matreolen blir kameradistansen 0,8 meter. Bakdelen
  av Eira sitt hovud fyller då skjermen. Runde 16 tonar ut figurar som omsluttar kameraet;
  dette må kontrollerast på begge format før rettinga kan godkjennast.

Runde 15: **724 JVM-testar, 3 Android-testar per format, ingen testfeil**. Siste lint er runde 13:
0 feil / 32 eksisterande åtvaringar. Kjelde/API-reglar, noverande status og filstiar ligg i
`docs/HEILEBERGET_NATTARBEID_2026-11-02.md`. Status er **arbeid pågår**; ingen release eller
påstand om ferdig visuell kvalitet eller målt FPS på fysiske einingar.

## Runde 16–17 – faktisk kontroll og nye funn

- Runde 16: 726 JVM-testar og tre Android-testar per format. Skuldersaum og nærkameraet
  bak matreolen er retta i faktiske bilete. Varden er meir avrunda, himmelen klarare blå.
- Runde 17: 728 JVM-testar, 111 Android-testar per format, lint 0 feil / 32 eksisterande
  åtvaringar. Fjellveggen held saman gjennom mobilforenkling; dei lyse sprekkane er borte.
- Modellert gullpanne med rørsle og forseinka gullfunn. Faktisk trykk og lagra fil stadfestar
  funnet på begge format. Gjentaking/avbrot/kiste-duplikat er dekte av logikktestar.
- **Ny visuell mangel:** ei bjørk og familien skjuler mykje av gullvaskinga. Runde 18 flyttar
  treet og prøver eit kort kamerafokus ovanfrå. Kameraet må gi etter for barnet sine spakar,
  bakkeval og skjermdrag. Redusert rørsle held den vanlege kameravinkelen.
- Stiane har svært reine kantar og einsfarga jord. Runde 18 prøver mjuke graskantar og
  slitasje måla i geometrien, utan ekstra teksturar eller skuggar rekna på telefonen.

## Runde 18–19 – gullplass, natursti og natt

- Begge rundane: 729 JVM-testar og tre Android-testar per format bestod. Runde 19 har sju
  dagbilete og tre nattbilete per format, med faktiske aktivitetstrykk og lagringskontroll.
- Treet skjuler ikkje gullvaskinga. Høgare kameravinkel i 19 viser gullkorna, men ein stein
  skjer inn i pannekanten. Panna blir flytta litt vestover i 20.
- Stiane har mjukare kantar og jordvariasjon. Skattkammeret har no fasetterte krystallstavar
  og slipte diamantar. Natt er lesbar, med kjøleg blått lys og varme felt ved lyktene.
- Nattprøva les GL-flata etter bufferbytte. Den opphavlege feilen var eit førre dagbilete
  frå testen, ikkje eit manglande natteskift i spelet. Same lysassert består no.
- Månen ligg delvis bak stadskiltet i startutsikta. Kameravinkelen kan endrast fritt.
- Runde 20: korte individuelle familieblunk og ein samanhengande tur over brua til Rumle
  og ut igjen bestod på begge format. 729 JVM / tre Android per format, lint 0 feil / 32 åtvaringar.
  Pannekanten er heilt fri. Ingen figur-ID-ar blir endra av turen.
- **Funn frå reisa:** tre bjørker står i brua, og helsingsteksten dekkjer Rumle sitt ansikt.
  Runde 21 flyttar trea og meldinga; faktisk reise på begge format stadfestar rettinga.
- Runde 22 gir mjukare bøygde grastuer. Den lange spakturen gjennom hola fann grå skjerm
  ved rampen (kamera bak framveggen) og figurar inni bruskassene oppe. Runde 23 rettar
  kameraklaring og kassekollisjon. Same lange tur på begge format stadfestar fri sikt ved
  rampen og figurane framfor kassene. 731 JVM / tre Android per format bestod.
- Runde 24: original blå sommarfugl på den ledige handa, stadfesta i ekte Android-bilete og
  trykk på begge format. 733 JVM / tre Android per format; gåing, kamerastyring og lagring grøne.
  Nærbileta fann harde, rette stiendar. Runde 26 tonar desse inn i enga; nye Android-nærbilete
  stadfestar den mjuke avsluttinga. Brei suite med 111 Android-testar per format bestod.
- Runde 25: faktiske sekssekunds-traces viser 0 GL-teikningar med open sekk og i redusert
  stillstand, og nye teikningar ved styring på begge format. Dette er ikkje fysisk FPS-måling.
- Runde 27: den første zoomtesten bestod, men viste berre ei tidleg mellomramme. Biletkontrollen
  stoppa godkjenninga. Testen ventar no på stabilt GL-bilete og meir enn dobbelt synleg
  skjorteareal. Den strengare prøva og nærbileta bestod på begge format: tydeleg større figurar,
  glatte ansikt, uendra posisjon/vinkel og ingen feilaktig gåing når ein finger blir løfta.
  Gjeldande kontroll: 734 JVM, fire Heileberget-Android-testar per format, lint 0 feil / 32 åtvaringar.
