# Heileberget – arbeid gjennom natta 2.–3. november

Brukaren bad om vidare arbeid gjennom natta med design og gameplay, med jamlege stopp for å
kontrollere korleis det faktisk ser ut i Android. Dette dokumenterer den lokale nattøkta, ikkje ein release.

Arbeidskopi: `C:/topa/.gradle-tmp/rainbow-finale`, grein `codex/heileberget-3d`.
Rotkopien sine andre endringar og den opphavlege ulagrede Blender-økta er urørte.
Ingen publisering, push eller endring av signeringsnøkkel.

## Gjeldande prøve – runde 27

- [Installerbar, isolert prøve-APK](C:/topa/dist/heileberget-blender/Trollfoss-round27.apk)
  (`app.trollfoss.heileberg`, debug-signert). Ho erstattar ikkje den vanlege Trollfoss-appen.
- [Redigerbar Blender-fil](C:/topa/dist/heileberget-blender/Heileberget.blend), lagra og open i
  den eigne Blender-økta. Scenene heiter `Heileberget`, `Trollhola` og `Eira og Eilev`.
- [Faktisk Android-nærbilete av familien](C:/topa/dist/heileberget-3d-2026-11-02/round27-tablet-pinch-after.png).
  Dette er PixelCopy frå den native GL-flata etter tofingerzoom, utan Compose-knappar.
- Siste MainActivity-bilete viser [landskapet](C:/topa/dist/heileberget-3d-2026-11-02/round27-tablet-outside.png),
  [hola](C:/topa/dist/heileberget-3d-2026-11-02/round27-tablet-cave.png) og
  [natta](C:/topa/dist/heileberget-3d-2026-11-02/round27-tablet-outside-night.png).
  Tilsvarande `round27-phone-*` er òg visuelt kontrollerte. Eilev går først i desse siste bileta.
- **734 JVM-testar, fire Heileberget-Android-testar per mobil/nettbrett og lint 0 feil /
  32 eksisterande åtvaringar bestod.** Siste breie suite, runde 26: 111 Android-testar per format
  (ScenePerformanceTest utelaten). Den nye zoomtesten kom til etter den breie køyringa.
- Faktiske spakturar har dekt brua, inn/ut av hola, øvre bruskasselager, Rumle, bakre skattkiste
  og krystallane. Aktivitetstrykk og fil-lagring er kontrollerte på begge skjermformat.
- Ansikta er glatte ovalar, med mindre nasar/smil og varsam kinnfarge. Stiar, lauv, grastuer,
  himmel, berg, varm holebelysning og dag/natt har fått gjentekne visuelle gjennomgangar.
- Gullvasking, smuttkast, krystalltonar, harehelsing og sommarfugl på handa gir frivillig leik.
  Kameraet har to spakar, bakkeval, drag og tofingerzoom; risting kan slåast av.
- Kompakt vertexformat sparer 44,44 % av modellbufferane for same geometri. Stillstand med
  redusert rørsle og open sekk gir ingen unødvendige GL-teikningar i dei målte sekvensane.
  Dette er ikkje ein målt FPS- eller batterilovnad på fysisk maskinvare.

## Første fungerande runde

- Blender-modellane av landskap, stor hole og Rumle er eksporterte til native GLES.
- Hola er 40 × 42 meter, med trollstove, skattehall og øvre matlager.
- Eira og Eilev har kvar fem artikulerte Blender-delar. Eira: lyst sideskilje, lilla klede med sol
  og blomar. Eilev: lysebrune bølgjer, grønkvit fotballdrakt, mørke shorts og limegrøne sko.
- Brukaren avviste dei første bulkete ansikta. Kinnkulene og den separate haka vart fjerna;
  nye ansikt er mjuke ovalar med mindre nasar, rolege bryn og små smil.
- Venstre spake går, høgre spake ser rundt; begge kan brukast samstundes. Drag og trykk på bakken
  er framleis alternativ. Kameraet følgjer den valde figuren.
- Dei tre Android-testane bestod på både mobil og nettbrett, med faktisk samtidig tofingerstyring,
  stopp etter slepp, reise, sekk, brus, bevart utsjånad/identitet, GL-teikning og ny grafikkontekst.
- 714 JVM-testar var grøne i denne runden. Ingen fysisk Android-eining er testa.

Arkiverte bilete: `C:/topa/dist/heileberget-3d-2026-11-02/round1-tablet-outside.png` og
`round1-phone-cave.png`. Prøve-APK: `C:/topa/dist/heileberget-blender/Trollfoss-round1.apk`.
Desse er faktiske Android-bilete, medan `eira-eilev-blender.png` og `landscape-harmony.png` er
Blender-renderingar med anna lys. Dei må ikkje blandast saman i rapportering.

## Funn frå biletkontrollen og neste runde

1. Eira vart gøymd bak større følgjefigurar. Følgjet er spreidd meir til sidene.
2. Lite himmel, og kameraet vart for bratt ved holeinngangen. Kameraarmen blir no korta før høgda
   blir rekna ut; lågare standardvinkel og meir blikk framover. Fjellmassiva er flytta bakover.
3. Gult/grått landskap og ujamn skugge på Rumle. Ny samla palett: salviegrønt, blå/lilla berg,
   honningfarga tre og varmt lampelys. Rumle og barna bruker mjuk, rein retningsbelysning.
4. Første eksport gav enorme svarte «trepluggar» i hola. Skjulte Blender-samlingar hadde utdaterte
   verdsmatriser; eksport bruker no gjeldande lokale matriser for rotobjekt. Retting sett i Android.
5. Stien var svart på grunn av nedovervendte normalar. Retta og visuelt stadfesta i Android.
6. Galleriet hadde ein rektangulær støtte som stakk over rampen. Ny støtte følgjer rampeprofilen.
7. Nye puter, bokstabel, krus/kanne, vevde motiv, rotgirlandarar, blomefelt og to harar gir meir liv.
8. Spaken gir no gradvis fart. Bord, peis og hyller har fysiske grenser; alle aktivitetspunkt og
   fram-/tilbakevegar er kontrollerte med rutetestane. 715 JVM-testar grøne etter desse endringane.

Andre runde er visuelt gjennomgått på begge format. Arkiv: `round2-phone-*` og `round2-tablet-*`.
Mobil sine 3 Android-testar og lint (0 feil / 32 eksisterande åtvaringar) bestod.
Nettbrettets gangtest arva kameraretninga frå frontfotoet og feila; testoppsettet er retta til
å nullstille yaw saman med posisjon. Ny køyring er nødvendig før nettbrett blir friskmeldt.

## Runde 3: fjell, skuggar, kamera og harar

Kl. 23.40: samanhengande granittvegg, avrunda platå og romlege fjellryggar erstattar klossane.
Skyene har volum og måla skyggesider. Blender-preview `landscape-ridges.png` er gjennomgått;
Android-eksport og ny biletkontroll er på veg. Bergveggen har enno svært regelmessige furer.

Dei grøne platene under figurane er erstatta med mjuke, blanda kontaktskuggar. Kameraarmen
tek omsyn til søyler. Gullpanna er høgdejustert på den nye breidda. To små harar har fått
handling: dei snur seg mot barnet og hoppar, utan krav eller forbruk. Redusert rørsle stoppar hopp.
717 JVM-testar bestod etter desse endringane. Endelege Android-prøver står att i denne runden.

## Runde 4–5: kompakte modellar og faktisk mobil/nettbrettkontroll

Runde 4: 717 JVM-testar og dei tre Heileberget-Android-testane bestod på begge format.
Testfeilen frå førre runde var manglande framdrift i den manuelle Compose-testklokka etter haretrykk;
testen ventar no på oppdatert UI. Samtidig tofingerstyring, reise, harehelsing, sekk, brus,
identitet/utsjånad og ny grafikkontekst er kontrollerte. Dette er den avgrensa 3D-suiten, ikkje alle
Android-testane i resten av appen. Fire faktiske scenebilete per format er arkiverte som `round4-*`.

HBG2 lagrar normalar/fargar kompakt og held posisjonane eksakt uendra. Alle samla modellbufferar
frå runde 4 vart reduserte frå 72 453 204 til 40 251 780 byte (44,4 %). Ikkje alle er i minnet samstundes;
talet er ikkje total RAM eller målt FPS. Den unødige float-kopien ved lasting er fjerna.
Skjermbileta stadfestar at pakkinga bevarer uttrykket. Mjuke kontaktskuggar er visuelt kontrollerte.

Runde 5, om lag kl. 00.05: 718 JVM-testar, tre Android-testar på kvart format og lint med
0 feil / 32 eksisterande åtvaringar. Gull i den bakre kista lagrar no rett kisteplass og kan ikkje
hentast ut frå den fremre. Fjordvatn lukkar den lyse glipa under fjella, og risteknappen har eit
kameraikon med tilgjengeleg på/av-tekst. `round5-*` er faktiske Android-bilete; APK ligg som
`C:/topa/dist/heileberget-blender/Trollfoss-round5.apk`. Ny lagringstest dekkjer begge kistene.

## Runde 6: fjell, holelys og familie

Fjellveggen får ujamne, skrå berglag og søkk, og skyene er samla med voxel-remesh til heile volum.
Færre skytriangel: 23 232 → 14 514. Bakt lys følgjer dei faktiske lyktene i hola, med varme soner
ved peisen og kaldare krystalllys. Øyvind har fått ein eigen femdelt Blender-modell i same stil
som barna: mindre hovud, skjegg langs ansiktsforma, skjorte med saum og sko. Hårnormalar på alle tre
er kontrollerte og vende utover. Tre Android-testar per format bestod, og fire faktiske bilete per
format er tekne. Bileta viste ein skyfeil som ikkje fanst i Blender, og synleg avskorne fjellendar.

## Runde 7: skyretting og smuttkast

Om lag kl. 00.30: 719 JVM-testar, tre Android-testar per format, lint 0 feil / 32 eksisterande
åtvaringar. Skyvoluma skriv no djupne og skjuler sine eigne bakflater; dette rettar dei kvite
triangelflekkene. Lange fjellendar trappar mjukt ned, og hola har ein forma, mørk boge.
Begge rettingane er sette i faktiske Android-bilete. Fire bilete per format: `round7-*`.

Ved breidda kan barnet kaste smutt: kast, tre sprett og ringar på vatnet, utan poeng eller forbruk.
Handlinga kan gjentakast. Redusert rørsle gir stille ringar. Ting i handa følgjer retning og
figurstorleik; hos Rumle står det no til dømes «Gi Urge» når barnet held Urge.
Lagring, kisteplass, reise og spakstyring er framleis med i den avgrensa 3D-testsuiten.

Faktisk MainActivity-opptak: `C:/topa/dist/heileberget-3d-2026-11-02/round7-smutt.mp4`.
Opptaket viste at ei bjørk skjulte mykje av barnet og kastet. Funksjonen er testa, men den visuelle
presentasjonen er ikkje god nok enno. Runde 8 opnar sikta og gir smuttplassen flate steinar.
Andre funn: for glatt fjellvegg, jamne hårpinnar på Øyvind, søyle framfor skattkammeret.
Runde 8 arbeider med desse, meir avrunda bjørkelauv og tydelegare bakt lys/skugge.

## Runde 8: fri sikt og breiare regresjonskontroll

719 JVM-testar og tre 3D-Android-testar på kvart format bestod. Deretter bestod den breiare
Android-suiten med **111 testar på både mobil og nettbrett** (ScenePerformanceTest er utelaten).
Loggane er `round8-phone-all-tests.txt` og `round8-tablet-all-tests.txt`; ingen fatal AndroidRuntime-feil
vart rapportert. Det er ikkje ei måling av fysisk eining eller ein generell FPS-garanti.

Fire vanlege scenebilete per format og fire ekstra nettbrettvinklar vart faktisk tekne i MainActivity.
Bjørka som skjulte smuttkastet er flytta, og runde 8-opptaket viser kast/ringar med fri sikt.
Ny birkegrein/lauvgeometri, flate kastesteinar, mindre sylindrisk hår på Øyvind, meir brotne
fjellflater og kameraklaring rundt søylene er med. Gull såg framleis for olivenfarga ut.

Dei ekstra bileta avdekte at utsiktspunktet gav nærbilete av baksida på holeinngangen.
Det skal rettast i ein eigen runde; runde 8 er ikkje visuell sluttgodkjenning.
Runde 9 legg til målretta aktivitetssymbol, kake som blir gitt til Rumle og varmare bakt gull.
Runde 9 er kontrollert: **720 JVM-testar**, **3 3D-Android-testar på kvart format**, lint **0 feil /
32 eksisterande åtvaringar**. MainActivity-opptak `round9-phone-cake.mp4` og
`round9-tablet-cake.mp4` viser kaka ved munnen til Rumle og vinking. Gullfargen og aktivitetssymbola
er kontrollerte i bileta. APK er arkivert som `Trollfoss-round9.apk`.

Runde 10 modellerer ein samanhengande bergkropp rundt holeportalen, ei kort skuggelagd innopning
og ny varde/sti vest i landskapet. Utsiktshandlinga vender mot fjorden. Ganggrenser og kamera tek
omsyn til den nye bergkroppen. **721 JVM-testar og tre Android-testar per format bestod**.
Faktiske utsiktsopptak på begge format viser at familien vender seg mot fjorden. Den store
kameravstanden viste også ein uheldig skjøt/underside ved kanten av den ytre landskapsflata;
runde 11 held kameraet over det modellerte platået. Lint er sist køyrt på runde 9.

Runde 11 rettar dei overlappande trekantane i Rumle sin munnkant (den gamle justeringa flata ut
heile tverrsnittet), bevarer meir avrunda lauv i mobilmodellen og legg til modellerte fuglar med
artikulerte venger. **721 JVM-testar og tre Android-testar per format bestod**. Fire MainActivity-
bilete og faktisk gå-/kameravideo per format er tekne. Munnkanten er jamn. Lauvblad skar framleis
gjennom krona; dette var ein geometrifeil, ikkje berre mobilforenkling. Gåprøva fann også at siste
kameravinkel ikkje vart lagra før bakgrunnsbytte. Lagring vart berre trigga medan figuren gjekk.

Runde 12 legg blad tangentielt utanpå trekrona, formar holeberget mindre symmetrisk og erstattar
terrengskjøten med ringar som møter den faktiske grensa. Lagring sjekkar no x/z/yaw, også etter at
fingeren er sleppt. **721 JVM-testar og tre Android-testar per format bestod**, inkludert ein ny
regresjonssekvens for stilleståande kamerastyring og fil-lagring. MainActivity-gåprøvene på begge
format stadfestar at lagra posisjon/vinkel før bakgrunnsbytte er identiske med sluttilstanden.
Bileta viser tydeleg betre avrunda lauv. APK/.blend 11 og 12 er arkiverte. Siste lint er framleis 9.

Runde 13: tre modellerte, syngjande krystallklynger i hola med ulike tonar og eit roleg lys/ring-svar.
Dei tek ikkje ting frå barnet og kan spelast fleire gonger. Varden er flytta ut av ståplassen til
familien. **722 JVM-testar, tre Android-testar per format og lint (0 feil / 32 eksisterande
åtvaringar) bestod.** Fire faktiske scenebilete, gå-/lagringsprøve, utsiktsvideo og krystallvideo
per format er arkiverte som `round13-*`. Tonane er kopla til lydsystemet og prøvde som hendingar;
opptaka har ikkje lyd, så dei stadfestar ikkje høyrbar lydkvalitet. Bileta fann at peisen skjulte
nedste del av krystallscena når kameraet stod bak han. Eilev sine hårkrøllar var framleis for tjukke.

Runde 14: samanhengande mjukt bølgja hår på Eilev, avrunda skulderledd på Rumle og
kameraklaring mot møblar med reell høgd. Terrengskjøten er kontrollert med 204 geometriske prøver:
største avstand er 0,0081 meter, den tilsikta overlappinga for å unngå flimring. **723 JVM-testar
og tre Android-testar per format bestod.** Fem faktiske scenebilete per format og gå-/lagrings-,
krystall- og utsiktsopptak er arkiverte. Krystallkameraet er fritt for peisen. Skulderoverlappinga
gav framleis ein synleg kant, og hårkappa hadde innovervende normalar.

Runde 15: hårnormalane er retta. Fleire brot i fjellet, forma holepilarar og mineralfargar. Gull
har enklare mobilgeometri. **724 JVM-testar og tre Android-testar per format bestod**; seks
scenebilete og same praktiske opptak som 14 er arkiverte. Ein handlingsfeil er retta: ein nærare
hare utanfor si rekkevidde kan ikkje skjule ei gullpanne innanfor rekkevidde. Navigasjonen tek
også omsyn til føtene på sidebogane. Siste lint er runde 13, 0 feil / 32 åtvaringar.

Det ekstra skattbiletet bak matreolen avdekte at kamerakollisjon kan presse kameraet heilt inn
i bakhovudet til Eira. Runde 16 rettar dette med mjuk uttoning nær kameraet, også for høge
følgjarar. I tillegg blir Rumle si skulder sameina med armen, vardesteinane avrunda, fjellhyllene
meir ujamne og himmel/eng klarare i fargen. Hola får drypplyd ved innreise og utelyden tilbake
ved utreise. **Runde 16: 726 JVM-testar og tre Android-testar per format bestod.** Seks
faktiske scenebilete, gå-/fil-lagringsprøve og krystall-/utsiktsopptak per format er arkiverte.
Skuldra er samanhengande, nærkameraet har fri sikt og vardesteinane er avrunda. Mobilbiletet
avdekte lyse sprekkar i fjellveggen: delte kantar før forenkling lét kvar flate bli flytta ulikt.

Runde 17 held fjellgeometrien samanhengande gjennom forenklinga. Den faktiske mobilvisinga
stadfestar at sprekkane er borte. Ei ny Blender-panne med avrunda kant og riffler har fått
gullvasking: panna ruggar, vatnet rører seg, gullet kjem fram og blir lagra etter handlinga.
Avbrot tek ikkje gullet, og ny leik etter første funn dupliserer ikkje gullet i sekken/kista.
**728 JVM-testar, brei Android-suite med 111 testar på begge format og lint 0 feil / 32
eksisterande åtvaringar bestod.** Faktiske trykk/lagra JSON stadfestar gullfunnet på begge format.
Gå-/kameraprøvene og lagring før bakgrunnsbytte bestod også. Modellbufferane er totalt
44 416 620 byte mot 79 949 916 før HBG2-pakking (44,44 % mindre; ikkje total RAM eller målt FPS).

Bileta av gullvaskinga fann ei bjørk som skjuler familien og panna. Runde 18 flyttar treet,
gir eit kort mellombels kamerafokus ovanfrå som gir etter for barnet si styring, og mjukar
opp stiane med varierande jordfarge, gras i kantane og små steinar. **729 JVM-testar og tre
Android-testar per format bestod.** Faktisk gullvasking/lagring, gåing/lagring og sju scenebilete
per format er arkiverte. Bileta viste fri sikt forbi treet, men hovudet skjulte noko av panna.

Runde 19 løftar vaskekameraet vidare, gir skattkammeret lange krystallfasettar med korte spissar,
og legg til nattelys som følgjer verda sitt dag/natt-val. Mørkeblå himmel, stjerner og måne,
blått omgivingslys og varme lysfelt ved lyktene blir teikna med enkel geometri/vertexlys.
Hola held sitt eige lys. GPU-prøva av natt fann først ei synkroniseringsfeil i testen: ferdig
`onDrawFrame` kjem før GLSurfaceView byter biletramma. Testen les no sjølve GL-flata med PixelCopy
etter to rammer. Same lysassert bestod deretter på begge format. Faktiske MainActivity-bilete
viste òg dag/natt-skiftet. **729 JVM-testar og tre Android-testar per format bestod.** Sju
dagbilete og tre nattbilete per format, gulltrykk/lagra fil, gå-/kameraprøve med lagring og
krystall-/utsiktsopptak er arkiverte som `round19-*`. Panna er tydeleg, men ein breiddestein
skjer framleis inn i kanten. Månen er synleg i landskapet, men delvis bak stadskiltet i startutsikta.

Runde 20 flyttar panna litt bort frå ein stein som framleis skar inn i kanten, og gir separate
auge/augelokk for korte individuelle blunk utan endring av ansiktsforma. **729 JVM-testar,
tre Android-testar per format og lint 0 feil / 32 eksisterande åtvaringar bestod.** GPU-prøva
kontrollerer byte mellom opne/lukka auge og at redusert rørsle held dei opne. Faktiske
gulltrykk/lagra fil og bilete viser heile panna fri. Den nye samanhengande reiseprøva startar
med éin lagrings-fixture; alle etterfølgjande posisjonar kjem frå faktiske trykk på bakken og
spaken i MainActivity. Familien gjekk langs breidda, over heile brua, til hola, inn til Rumle,
helsa og gjekk tilbake til utgangen med spaken. ID-ane vart bevarte på begge format.

Reisebileta fann tre bjørker som veks gjennom brua og skjuler familien. Dei fann òg at
helsingsteksten ligg over ansiktet til Rumle. Runde 21 flyttar bjørkene til bakken bak vatnet
og meldingane til ledig plass over dei nedste midtknappane. **Runde 21: tre Android-testar og
den samanhengande reisa bestod på begge format.** Bileta viser fri bru og fri sikt til ansiktet
til Rumle. GPU-blunkbiletet vender no ansikta mot kameraet og viser rette, lukka augelokk;
førre faste testbilete viste bakhovuda. Tolv sekund lange faktiske familieopptak er arkiverte.

Runde 22 erstattar spisse, spreidde grasblad med bøygde tuer og rolegare mellomrom. Full
utemodell går frå 253 309 til 236 955 trekantar; mobilmodellen frå 124 698 til 126 586.
Det er ei visuell endring med mindre fullmodell, ikkje ein målt FPS-auke. Android-bygg og tre
Android-testar per format bestod. Faktisk lang spaktur i hola fann to feil som faste bilete ikkje viste:
kameraet kan hamne bak framveggen ved rampen, og figuren kan gå inni bruskassene i øvre lager.
Runde 23 legg til kamerasperrer og tak-klaring, kollisjon på kassene og hentepunkt framfor dei.
Begge spakturane i 22 fullførte handlingane, men bileta viste desse visuelle feila.

**Runde 23: 731 JVM-testar, tre Android-testar per format og lang spaktur på begge format
bestod.** Familien gjekk opp rampa, henta Urge på lageret, gjekk ned att og gav henne til Rumle,
vitja bakre skattkiste og krystallar, og gjekk ut. Bileta frå både mobil og nettbrett stadfestar
fri sikt ved rampen og at figurane står framfor kassene. Tak-klaringa følgjer forma på kvelvet.
Eira/Eilev fekk varsam kinnfarge på den eksisterande glatte ansiktsflata, utan nye kuler.

Runde 24 legg til ein original modellert blå sommarfugl ved blomane, med ei frivillig helsing
på den ledige handa. Leiken kan gjentakast, bruker ingen ting og slepper styringa fri.
**733 JVM-testar og tre Android-testar per format bestod.** Både faste GL-nærbilete og
faktiske trykk/opptak i MainActivity viser handstillinga og sommarfuglen. Gåing, kamerastyring
og lagring bestod på begge format. Kaka på den andre handa blir bevart under besøket.

Runde 25 hoppar over identiske GL-teikningar når sekken er open og ved stillstand med redusert
rørsle. Tre Android-testar per format bestod. Seks sekund lange Android-traces viste på mobil
75 teiknekall med animert scene, 0 med open sekk, 0 i redusert stillstand og 22 under gåing.
Nettbrett viste høvesvis 73, 0, 0 og 10. Det stadfestar spart teiknearbeid og at spaken framleis
oppdaterer scena; tala er ikkje målt FPS på fysisk maskinvare. Lint fann ei ny sporing-åtvaring;
`beginSection` er flytta rett framfor `try`, med `endSection` først i `finally`, for ny kontroll.

Runde 26 mjukar opp dei rette stiendane som vart synlege i sommarfugl-nærbiletet. Rutene held
same midtlinje og gangflate; berre breidd/farge tonar inn i graset. **733 JVM-testar, brei suite
med 111 Android-testar på både mobil og nettbrett, og lint 0 feil / 32 eksisterande åtvaringar
bestod.** Nye faktiske dag-/nattbilete, sommarfugltrykk og nattleg gåing/kamerastyring med
fil-lagring er kontrollerte på begge format. Stienden er mjuk i nærbiletet. Alle 39 pakka modellar
er verifiserte mot kjeldehash, manifest og APK; samla vertexbufferar er 44 203 740 byte,
mot 79 566 732 med den tidlegare float-representasjonen (44,44 % mindre for same geometri).

Runde 27 legg til tofingerzoom med grenser og tilgjengelege zoomhandlingar. 734 JVM-testar og
fire Android-testar per format bestod først, men nærbiletkontrollen viste at zoomtesten tok
ei mellomramme. Testen ventar no på to stabile bilete og krev meir enn dobbelt synleg skjorteareal
på Eilev, i tillegg til uendra posisjon/yaw. **Den strengare prøva bestod på begge format.**
Dei faktiske nærbileta viser ferdig zoom og glatte ansikt. Lint er framleis 0 feil / 32 eksisterande
åtvaringar. Alle 39 ressursar er kontrollerte mot den siste APK-en; Blender-fila er lagra utan
ulagra endringar og har same kunst som runde 26 (runde 27 endrar berre styring/testar).

## Kjende avgrensingar og neste kontrollpunkt

- Grafikken skal vurderast vidare i faktisk spel, særleg fjellformene, skyene, lys i hola og ansikt
  under rørsle. Konseptbiletet er ikkje ein påstand om ferdig visuell kvalitet.
- Eira/Eilev/Øyvind sine standardutsjånader bruker Blender-modellane i runde 6. Verkstadendringar bruker førebels
  den generiske 3D-teikninga slik at eigne klede/hår ikkje blir stille erstatta. Andre figurar er
  framleis enklare modellar. Dei same ID-ane og tinga følgjer med.
- Mange innreiingsdetaljar er dekor. Bruk-handlingane gjeld definerte aktivitetspunkt.
- Eiga programvare-AVD TrollfossPerf viste oppstarts-ANR med HWUI `skiagl`; `skiavk` gav fungerande
  MainActivity og testkøyringar. Dette er ei emulatoravgrensing, ikkje prov på alle ekte einingar.
  Sporet er arkivert i `tablet-skiagl-startup-anr.txt`. Ikkje friskmeld fysisk yting utan måling.
- Testpakke er isolert: `app.trollfoss.heileberg`. Fysiske ADB-einingar blir ikkje brukte.
- Månen ligg delvis bak stadskiltet i startutsikta om natta; han er synleg når kameraet blir snudd.
- Lydhendingane er kopla og logikktesta, men skjermopptaka har ikkje lyd. Dei dokumenterer ikkje lytting.

## Avslutting av nattøkta

Kl. 04.40 norsk tid 3. november: siste APK, 39 modellar og Blender-kjelde er kontrollerte;
hashar ligg i `final-artifact-hashes.json` i bilete-/loggmappa. Testpakken og den isolerte
spelpakken er avinstallerte frå den eigne TrollfossPerf-emulatoren, og skjermstorleik/tettleik
er nullstilte. ADB sitt konsollstopp fungerte ikkje over WSL-tilkoplinga; den verifiserte,
eigde QEMU-prosessen vart avslutta med TERM og kontrollert borte. Fysiske einingar er urørte.
Den eigne Blender-økta står open på den lagra Heileberget-scena. Brukaren si opphavlege
ulagrede Blender-økt er framleis open og urørt. Ingen publisering eller push er utført.
