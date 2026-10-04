# Trollfoss – designunderlag

Arbeid etter 1.5.0: «Leik og eventyr» samlar to byggjeoppskrifter og tre frivillige bileteventyr.
Pute, bamse, laken, bok og lykt får fleire bruksval. Venner i nærleiken reagerer på leik og
overraskingar. Putehytte og trillevogn bevarer dei opphavlege delane; pakking gir dei tilbake i sekken.
Vogna har to handtak og valfri hjelp for éin finger. Eventyrframgang og gåver blir lagra med verda.
Dette er første runde av [dei 25 prioriterte løfta](MAGISK_LEIK.md), før det nye nivåsystemet.

Tillegg 2026-10-02 (utgåve 1.5.0): barnet vel éin eller fleire faste spelarfigurar ved start
eller seinare med «Spelarar». Dei er dei same figurane overalt, med lagra utsjånad, klede og det dei
held eller har på hovudet. Spelarane kjem med på kartreiser og gjennom passasjar; dei vandrar ikkje
vekk til andre etasjar på eiga hand. To eller fleire barn leikar på same skjerm med kvar sin figur
og fleire fingrar samtidig. Portretta hentar figuren til det synlege rommet, og eit langt trykk opnar
figurverkstaden. Nye figurar laga frå spelarvalet blir automatisk valde som spelarar. Romknappane
tek med spelarane til valt rom med ei mjuk gåtur. Ved panorering byrjar dei å gå medan barnet sveipar,
med roleg start og stopp og eigne plassar ved sida av kvarandre. Små sveip flyttar ikkje figurar som
framleis er godt synlege. Ein figur som barnet held, blir verande under barnet sin kontroll; andre kan
gå med både ved sveiping og når barnet dreg sin figur langs skjermkanten. Sovande figurar og passasjerar
blir verande i senga eller køyretøyet. Valde spelarar vandrar ikkje av garde på eiga hand etterpå.
På golvet går dei mjukt framfor møblane, slik at dei er lette å sjå. Tomme rom i Mitt hus blir ikkje brukte som snarveg.
«Venner» viser kven som er her, med «Legg i sekken», og kven barnet kan hente hit. Å pakke ein figur
tek han også ut av spelarvalet; figuren, utsjånaden og tinga hans blir bevarte. Han kan hentast tilbake
frå Venner eller sekken og veljast som spelar igjen.

> **Trollfoss** er ei lita bygd under ein stor foss, der folk, dyr og troll bur saman. Ein digital
> leikekasse for barn frå 4 til 10 år: ingen reglar, ingen poeng, ingen tap – berre figurar, ting og
> stader som svarar når du rører dei.

Dette dokumentet er fasiten for korleis Trollfoss skal sjå ut, låte og kjennast. Teiknereglane for koden
ligg i `docs/ART_GUIDE.md`, bygg og release i `docs/AI_INSTRUCTIONS.md`.

---

## 1. Visjon og haldning

Trollfoss høyrer til sjangeren *digitalt dukkehus*, men er heilt original: namn, figurar, stader og
grafikk er laga frå botnen for dette spelet. Verda er **nordisk på ein vanleg måte**: ei moderne bygd
med fjord, foss, skog og fjell, der folk går i hettegenser og joggesko – ikkje eit postkort. Det
norske ligg i detaljane (ein strikkagenser i klesstativet, nordlys om natta, kakao i skibakken), og
trolla er det einaste eventyret.

| Søyle | I praksis |
| --- | --- |
| **Alt er levande** | Figurar pustar, blunkar og ser etter fingeren. Ting skvisar seg når dei landar og snurrar når dei blir kasta. Fossen dundrar, skyer driv, nordlyset bølgjer. |
| **Alt kan rørast** | Kvar ting kan plukkast opp, kastast, givast til nokon eller puttast i noko. Gir kombinasjonen meining for eit barn, skjer det noko. |
| **Oppdaging utan press** | 45 løynde glimt, ønskjebobler, oppdragstavle med biletoppdrag, oppskrifter i omn, gryte og verkstad, og dagens pakke – små mål for dei som vil, aldri tidsfrist, poeng eller tap. |
| **Alt heng saman** | Oppdraga sender barnet rundt i heile bygda (egg frå garden til bakaren, fisk heim til katten), klistremerka frå oppdraga opnar spesialmøblar, og ballongen på kvart oppdragskort flyg rett dit. |
| **Humor overalt** | Prompepute, bananskal, pepar som bles hatten av, kake i fjeset, hikke og rap, kiling, hunden som stel mat. Alle ler med, ingen blir lei seg. |
| **Noko for alle** | Kjøkken og frisør, traktor og verktøy, fiskestang og akebrett, rakett og ekte planetar, tivoli, butikk, lege, konsertscene og havbotn. |
| **Nordisk og litt magisk** | Kjent for norske barn, vanleg nok for alle andre. Trolla er snille og litt tullete. |

**Aldri:** reklame, kjøp i appen, konto, sporing, nedteljing, straff, poengtavle, tekst barnet må lese.

---

## 2. Verda: bygda Trollfoss

Kartet viser bygda ovanfrå med **19 reisemål**: dei femten opphavlege stadene, Storhuset, Mitt hus, Vagstaddalen og Skyøya. Kartet er 60 prosent breiare enn skjermen og kan dragast sidelengs. Fjella med trollhola og romstasjonen ligg
øvst, dalen med tivoli, butikk, foss, lege, bakeri og gard i midten, og strandlinja med frisør,
Familiehuset, konserthus, strand og dykkebøya ytst – og bak alt saman det store, lange fjellet **Heileberget**. Kvar stad er ei brei scene i skrå-3D (sjå §5) som ein
sveipar sidelengs i.

Kartet er hovudsida, utan raud X. Dag/natt og vêr kan skiftast direkte her. Stadene har runde
stadskilt med fargemerke, trykkrespons og ein liten ballong på den valde staden. Sidepiler viser kor
det finst meir å utforske. Trykk på vatnet gir ein liten sprut, andre ledige område svarar med farga
glimt, og reiseballongen legg att eit kort spor. Treffområda til stadene er faste, også når skilta
sprett. Redusert rørsle slår av desse animasjonane; reiser og knappar fungerer framleis.

| Stad | Id | Stemning | Hjartet i staden |
| --- | --- | --- | --- |
| **Familiehuset** | `HOME` | Eit lyst, vanleg hus: soverom, stove, kjøkken, bad. | Seng, kiste (hittegods), vedomn, radio (dansemusikk), piano, klokke, kjøleskap, komfyr, badekar, do, postkasse med dagens pakke. |
| **Bakeriet** | `CAFE` | Mynte og krem, rutete golv. | Omn (deig + eple = kake), blender (frukt = smoothie), fruktkasse, is-maskin, kakedisk, mjølsekk. |
| **Frisøren** | `SALON` | Rosa og lilla, terrazzo. | Frisørstolar, hårvask, tørkehjelm, saks, føn, fargesprayar, klesstativ, hattar. |
| **Stranda** | `BEACH` | Sommar ved fjorden. | Brygge og fiskeplass (kvar femte fangst er skatt), båt, parasoll, solseng, sandslott, lundefugl. |
| **Fossen** | `FOREST` | Granskog ved foten av fossen. | Telt, bål (pølser, marshmallow, drakeegg), stokkar og stubbar, ugle i treet, elgkalv, nordlys om natta. |
| **Trollhola** | `LAB` | Grotta bak fossen, krystallar som lyser. | Trollgryta (to ting blir til noko nytt), eliksirar, teleskop, krystallkule, trolldomsbok, troll-Rumle. |
| **Fjellet** | `MOUNTAIN` | Vinter heile året. | Akebakke, hoppbakke, snømann, badstu, islagd tjern med ønskehol, kakaobu, gatelykt. |
| **Garden** | `FARM` | Raud låve, åker og traktor. | Traktor som køyrer, høyballar, hønsehus (gullegg), grønsakshage (så, vatn, hauste), vasstrau, arbeidsbenk (fuglekasse, båt, bil), verktøyvegg, vedstabel, dekk. Ku, sau, høner, fjordhest. |
| **Romstasjonen** | `SPACE` | Vektlaust, stjerner i vindauget. | Rakett med nedteljing, kontrollpanel, koøyer med ekte planetar, tyngdespak, planetarium med dei åtte planetane og Pluto, romsenger, matautomat. |
| **Tivoliet** | `TIVOLI` | Lyspærer, konfetti, fjorden bak. | Pariserhjul (over toppen: «wiii!»), karusell med eigen vals, radiobilar som krasjar, trampoline, sukkerspinn, popkorn, boksbombing med premie. |
| **Butikken** | `SHOP` | Lys og moderne. | Hyller med varer, samleband som skannar med pip, vekt som veg kven som helst, frysedisk, brusskap, handlevogn som rullar med varene. |
| **Legekontoret** | `DOCTOR` | Pastell og snille plakatar. | Røntgen som viser skjelettet, stetoskop som finn hjarteslaget, plaster, medisin (sur grimase!), undersøkingsbenk, høgdemålar. |
| **Scena** | `STAGE` | Raude teppe, lyskastarar. | Trommer, xylofon, mikrofon (figuren syng), høgtalar som får alt til å hoppe, discokule som får alle til å danse, røykmaskin. |
| **Havbotnen** | `UNDERWATER` | Lysstrålar gjennom vatnet. | Alt sym. Ubåt å køyre, kjempemusling med perle, skipsvrak, tare, korallar, blekksprut som sprutar blekk. |
| **Heileberget** | `HEILEBERGET` | Eit stort, langt fjell (ni scenebreidder, dobbelt så langt som dei andre), gyllent ettermiddagslys, to fossar. | Fjellhytte, taubane som glir opp til ein avsats og ned att, ekko-stein som svarar tre gonger (geitene breler tilbake), ørnerede og eit toppflagg som går til topps når nokon står på toppen. Tre fjellgeiter, BesteSonja og Tuva, kikkert og termos. |

Storhuset har fire etasjar og ein hage (sjå `HUSET.md`), og Mitt hus har ei tomt og to etasjar som barnet byggjer sjølv (sjå `BYGG.md`). Etasjane deler eitt reisemål på kartet.

På nettbrett ligg ei lita biletoversikt over romma over scena. Eit trykk glir til rommet, og markøren følgjer
romval og sveiping. Byggje- og møbelpanel reserverer plass på høgre side; figurar og ting held same storleik,
og arbeidsområdet og sekken flyttar seg inn i den synlege delen. Sekken opnar over verktøylinja.

Små klossar, sokkar, bøker, verktøy og liknande frå det opphavlege innandørsoppsettet får plass i
eksisterande kasser, skap og på hyller. Golvet får meir fri plass til figurane. Ting blir bevarte og kan
hentast fram, og ryddeknappen bruker desse nye heimane. Oppdateringa ordnar berre urørte ting som
framleis ligg på sin opphavlege golvplass, éin gong; barnet sine flytta ting og Mitt hus blir bevarte.

Mitt hus knyter romma saman gjennom brukbare ting: frø og vatning i drivhuset gir bær eller blomar; egg og
mjølk blir deig i mjølbollen på kjøkenbenken, og deigen kan bakast med bæra i omnen. I verkstaden gir saga to
pinnar av ein planke; pinnane kan bli ein gitar på arbeidsbenken. Resultata er vanlege flyttbare ting som
kan serverast, spelast på og takast med mellom rom og stader. Bilettips og oppdagingsboka viser oppskriftene.

**Vagstaddalen** (`VAGSTADDALEN`) er ein roleg dal med ei lita laftehytte, ei svingande elv frå fjellet og grøne elvebreidder. Hyttedørene kan opnast; det finst fiskeplass, bål, benk, ved og ein katt. Scena er 5,4 einingar brei, med plass til å leike på begge sider av elva.

Verda har **76 glimt**. Vagstaddalen og dei femten opphavlege stadene har tre kvar: under ting, inni ting, eller dei dukkar opp når du gjer noko
(fiskar opp ei kiste, køyrer traktoren, sender opp raketten, slår av tyngdekrafta, tek pariserhjulet
over toppen, skannar fem varer, røntgar nokon, spelar i band med trommer, xylofon og song).

---

## 3. Figurane

Folket i Trollfoss er runde som leiketøy: stort hovud, kort kropp, stutte armar. Hud frå ljos til djup
brun – og mosegrøn, blå eller lilla for troll og vesen. Trolløyre, katteøyre, kaninøyre eller
bjørneøyre.

| Del | Val |
| --- | --- |
| Hud | 8 menneskelege tonar + 6 troll- og vesenfargar |
| Høgd | Barn, ungdom, vaksen, høg |
| Frisyre | Skalla, kort, pannelugg, krøller, lang, fletter, knute, pigg, bob |
| Hårfarge | 10, også rosa, blå og grøn |
| Auge | Runde, store, søvnige, vipper, prikkar |
| Øyre | Vanlege, katt, kanin, bjørn, troll |
| Overdel | T-skjorte, hettegenser, kjole, stripar, selebukse, **lusekofte**, **bunad** |
| Underdel | Bukse, shorts, skjørt |
| Ekstra | Fregner, skjegg, bart |

**Dyr og vesen:** katt, hund, kanin, **elgkalv**, **lundefugl**, ku, sau, **fjellgeit**, høner, fjordhest og ein drake
som klekkjer frå drakeegget.

**Namn:** alle figurane har namn som visest i ei snakkeboble når ein trykkjer på dei.
Barn: Hedda, Alva, Frida, Velte, Eilev, Olve, Eira, Iver, Olvar. Vaksne: Tuva, Øyvind, Sondre, Elise,
Sander, Hilde, Berit, Sølve. Besteforeldre: BesteSonja og Besten. Trollet heiter Rumle. Nye figurar
frå verkstaden får eit ledig namn, og barnet kan skrive sitt eige.

**Liv:** pustar, blunkar, ser etter fingeren. Plukka opp: «oi!», sparkar med beina. Sleppt: skvis og
dunk. Trykk: fnis og hopp. Et: gomlar med synlege bitemerke. Radio: dansar. Seng om natta: søv.
Eliksirar: veks, krympar, byter farge, svevar. Kvar figur har si eiga røysthøgd – ballong i handa gir
heliumstemme, krympa figurar pip og kjempar brummar.

**Ønskjebobler:** figurar tenkjer på noko i ei bilettboble (ein ting som finst på staden, ei seng om
natta, musikk eller ein ven). Oppfyller barnet ønsket: konfetti, jubel og kvar tredje gong ein liten
pakke. Trykk på ein som ønskjer seg noko, så blinkar tingen dei vil ha. Figurar som står nær
kvarandre pratar med bilettbobler, og dyr som får vere i fred tuslar rundt på golvet.

**Humor:** prompepute på sofaen, bananskal etter bananen (den som landar på det, sklir og ser
stjerner), pepar ved nasen gir «ATSJO!» som bles hatt og briller av, ting kasta i hovudet seier
«bonk» (puter blir fjørsky), kake i fjeset gir krem og kirsebær, tre raske slurkar gir hikke, eit stort
måltid kan ende med rap, mange raske trykk kilar, hunden stel mat frå golvet, blekkspruten sprutar
blekk. Alle rundt ler.

---

## 4. Samhandling – verbet er «dra»

| Gest | Verknad |
| --- | --- |
| Dra | Løftar ting eller figur; følgjer fingeren med etterheng og helling. |
| Slepp | Fell til næraste flate under. På ein figur: munn = et, hovud = hatt, auge = briller, kropp = plagg, hår = frisørverktøy, hand = held. |
| Kast | Ting snurrar, ballar sprett, ballongar stig, is glir. |
| Trykk | Lampa lyser, TV-en byter kanal, pakken opnar seg, snømannen veks. |
| Dra på tomt område | Panorer scena. |
| Hald inne på ein møbel | Møbelen løftar seg og kan flyttast langs golvet og i djupna, med alt som står på han. |

**Heimedesignaren** (møbelknappen nede til høgre) opnar eit smalt panel på høgre side, så golvet
er synleg: **møblar** frå ein biletkatalog (spesialmøblar opnar seg med klistremerke), **tapet** (12)
og **golv** (9) for rommet midt på skjermen, **lager** (dra ein møbel over panelet for å leggje han
vekk) og **kosten** som ryddar heile staden: kvar ting flyg heim i ein glitrande boge, ting laga i
leiken havnar i hittegodskista, rusk forsvinn i ein puff. Søppelbøtta et rusk og rapar, og
robotstøvsugaren tuslar langs rommet og slurpar opp det som ligg på golvet.

Ein smal, rullbar ikonrad skiftar mellom møblar, tapet, golv, lager, rydding og papirkorg.
Møbelkatalogen har biletkategoriar for senger/sitjeplassar, bord, skap/kister, kjøken/bad,
leik/musikk og pynt. Tilgjengelege møblar kjem før låste leiker. Loddrett sveip blar i møblane;
eit drag til venstre viser møbelet under fingeren før slepp. Avbroten draging endrar ikkje verda.

**Legg bort-hjørnet:** når barnet løftar ein ting, ein figur eller eit møbel, veks sekken nede til høgre.
Slepp der legg ting og figurar i sekken og møblar på lager, utan at møbelpanelet er ope. Skjermkantane
ber framleis kameraet vidare. **Skattekista** har glasfront og tek imot alt barnet samlar; diamantar,
myntar og perler forsvinn aldri av seg sjølv. Hint kjem berre i augeblinken barnet prøver noko: hjørnet
vaknar ved løft, og eit trykk som ikkje treffer noko, får det næraste som svarar på trykk til å vippe og lyse.

**Oppdragstavla** (utklippstavla øvst til venstre, med tal for kor mange som står att) har tre
biletoppdrag om gongen – bak ei kake, ta pariserhjulet over toppen, røntg nokon, nys hatten av
nokon. Kvart oppdrag gir eit klistremerke til albumet i oppdagingsboka; når alle tre er gjort,
deler terningen ut tre nye.

Møblar er **flater**, **plassar** (stol, seng, akebakke, hoppbakke, badstu), **skap** og **maskinar**.
**Sekken** nede til høgre er korleis ting og figurar reiser mellom stadene. Han har 12 plassar og
viser seks store bilete per side over verktøyknappane, med piler for å bla. Ein full sekk avviser nye ting og figurar;
det som blir avvist, blir verande i verda. Gamle sekkar med fleire ting og delar frå byggjeleikar
blir bevarte og kan blaast gjennom. Møblar går berre til det separate møbellageret. Ved møbelløft
viser hjørnet ei lagerkasse med talet på lagra møblar, og etter lagring kan lageret opnast direkte.

---

## 5. Visuell stil – «nordisk leikekasse», kvass og detaljert

* **Skrå-3D:** verda er teikna i skrå projeksjon. Golvet er eit band med djupn: jo lenger bak, jo
  høgare opp på skjermen. Møblar har synleg topp og side (`box3d`, `topFace3d`, djupn `DX 0,5`,
  `DY −0,36`), og alt blir teikna bakfrå og fram i éi felles liste, så ein figur kan stå bak bordet
  eller framfor det. Ting ein slepp nede på golvet blir ståande på den djupna; ting som fell ned frå
  ein møbel landar framfor han. Skuggar landar der tingen kjem til å lande.
* **Former:** runde og mjuke på alt menneskeskapt; naturen kan vere kvassare (granbar, fjell, is).
* **Strek:** varm blekkfarge `#2B2140`, `0,0034 × scenehøgd` – tynnare enn før, så detaljane får plass.
  Fine detaljar (treårer, strikkemønster, sømmar, skruar) med halv strek i ein mørkare tone.
* **Skugge:** kvass to-tone cel-skugge: ein hard skuggesigd nede til høgre, lys frå oppe til venstre.
  Éin liten glans på blanke ting.
* **Nordisk palett:** låveraud `#B8342B`, gran `#1F7048`, gras `#6FAE5A`, fjord `#2F6FB8`,
  bjørk `#F2EEE6`, tre `#C98A55`, snø `#F4F8FF`, nordlys `#7CFFB2`/`#D77BFF`. Romstasjonen og
  frisøren får sterkare, meir leikne fargar.
* **Grensesnitt:** blanke 3D-knappar i hjørna (sol `#FFC83D`, bær `#FF4D6D`, sjø `#2F9BFF`,
  mynte `#2FD18B`, drue `#8B5CF6`), raud X lukkar alt, `GameText` for dei få orda.
  Spelarval og angre brukar kremtone, slik at dei rolegare hjelpevala tek mindre merksemd.
* **Typografi:** Nunito i lesetekst, namn, knappar og små skilt; Fredoka Bold i store overskrifter.
  Fontane er pakka lokalt med SIL OFL-lisensane, med full teikndekning for mellom anna æ/ø/å.
  `GameText` brukar Nunito under 20 sp og eit fint blekkomriss på lys tekst
  (7,5 % av tekststorleiken, avgrensa til 0,8–4,5 dp). Mørke skilt er utan omriss.
  Stadskilta på kartet held namna synlege, med kremfarga kant og svak skugge; vald stad har farge.
* **Spelarmerking:** `PlayerPalette` gir spelaren same farge i valkort, portrett og scene:
  gul, lys lilla, lys blå, lys grøn, lys rosa, så om att. Nummeret skil også spelarane.
  I scena kjem ein liten ring og nummer fram under berøring og i 1,8 sekund etterpå eller etter
  innkalling. Dei siste 0,4 sekunda tonar merket ut, utan puls eller ekstra rørsle.
  Foto tek ikkje med merka. Portrettet har minst 48 dp høg trykkflate og skjermlesarnamn.

---

## 6. Rørsle

| Hending | Animasjon | Tid |
| --- | --- | --- |
| Knappetrykk | Søkk ned på kanten, skalering 0,95 | 70 ms |
| Plukk opp | Løft og skvis, «oi!» | fjør |
| Landing | Skvis 1,25 × 0,75 og sprett tilbake | 260 ms |
| Kast | Snurr etter fart, sprett | fysikk |
| Pust / blunk | Kroppen opp og ned / augo lukkar seg | 2,4 s / kvar 2–6 s |
| Dag ↔ natt | Himmel, lys og nordlys glir | 1,6 s |
| Reise | Luftballong over kartet | 1,4 s |
| Glimt | Stjerna flyg i boge til teljaren | 700 ms |

Alle tidsstyrte rørsler stoppar når Android har slått av animasjonar.

---

## 7. Lyd

Alle effektar er syntetiserte i kode: pop, dunk, boing, gomle, fnis, magi, plask, fres, pling, spyling,
knirk, lukkar – og tulleljodane: rap, hikke, prompepute, «atsjo», glideløype-skli, bonk, kilelatter og
splat. Figurane snakkar tulleord i si eiga tonehøgd og syng i mikrofonen. Eigen loop for kvar av dei
15 stadene (tivolivals, butikkbossa, rolege legetonar, scenepop, drøymande havbotn), lullaby om
natta, dansemusikk frå radioen og discokula, karusellmelodi og instrument i same pentatone skala, så
alt barnet speler høyrest fint ut.

---

## 8. Skjermar

Opning (logoen hoppar inn), stad med knappar i hjørna, kart, figurverkstad, oppdagingsbok og foreldreside
bak eit gongestykke (lyd, musikk, målform, oppdatering, nullstill).

---

## 9. Android og oppdatering

Liggjande på mobil og nettbrett, oppslukande. Kotlin + Jetpack Compose, alt teikna på `Canvas`. Min.
Android 8.0. Nettbrett viser minst 2,35 scene-einingar i breidda, med meir himmel eller vegg over.
Kameraet er om lag 13 prosent lenger unna enn i v1.1.0. Breie telefonar fyller framleis skjermhøgda.
**Spelmotor:** stille møblar og ting blir teikna éin gong per utsjånad til eit bilete og stempla inn
(«sprite cache»). Det som rører seg sjølv, blir oppdaga automatisk og teikna på nytt 8 gonger i
sekundet inn i same bilete; figurane 13 gonger i sekundet (24 når dei blir haldne), medan hopp,
skvis, helling og spinn blir lagt utanpå i full fart. Miniatyrar i panel og kort blir teikna éin gong.
Resultat: få millisekund per bilete på mobil, og eit tregt nettbrett utan skjermkort heng ikkje. Lagring i `files/trollfoss.json`. Oppdatering som Komet: éin signert APK per release på
`oyvhov/trollfoss-android`; appen kontrollerer SHA-256, pakke, versjon og signatur, og ein vaksen
startar installasjonen.

---

## 10. Vegkart

| Versjon | Innhald |
| --- | --- |
| 1.0 | 15 stader i skrå-3D, kart, figurverkstad med namn, fysikk med djupn, vektløyse og symjing, mat, klede, frisør, trollgryte, verkstad og traktor, rakett og planetar, tivoli, butikk, lege, scene og havbotn, ønskjebobler, humor, heimedesignar med katalog, tapet, golv og lager, rydding, oppdragstavle med klistremerke, 42 glimt, dagens pakke, dag/natt, vêr, sekk, foto, foreldreside, GitHub-oppdatering. |
| 1.1 | Fotoalbum med deling, fleire dyr (sel, katt-ungar), fleire klede og frisyrar. |
| 1.2 | Skule og togstasjon, sesongpynt (jul, 17. mai). |
