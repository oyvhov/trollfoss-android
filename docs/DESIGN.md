# Trollfoss – designunderlag

> **Trollfoss** er ei lita bygd under ein stor foss, der folk, dyr og troll bur saman. Ein digital
> leikekasse for barn frå 4 til 10 år: ingen reglar, ingen poeng, ingen tap – berre figurar, ting og
> stader som svarar når du rører dei.

Dette dokumentet er fasiten for korleis Trollfoss skal sjå ut, låte og kjennast. Levande versjon med
figurar du kan dra rundt: `docs/design/trollfoss-designunderlag.html`. Teiknereglane for koden ligg i
`docs/ART_GUIDE.md`, bygg og release i `docs/AI_INSTRUCTIONS.md`.

---

## 1. Visjon og haldning

Trollfoss høyrer til sjangeren *digitalt dukkehus* (som Toca Boca World), men lånar ingenting derifrå:
ikkje namn, figurar, stader eller grafikk. Verda er **nordisk på ein vanleg måte**: ei moderne bygd
med fjord, foss, skog og fjell, der folk går i hettegenser og joggesko – ikkje eit postkort. Det
norske ligg i detaljane (ein strikkagenser i klesstativet, nordlys om natta, kakao i skibakken), og
trolla er det einaste eventyret.

| Søyle | I praksis |
| --- | --- |
| **Alt er levande** | Figurar pustar, blunkar og ser etter fingeren. Ting skvisar seg når dei landar og snurrar når dei blir kasta. Fossen dundrar, skyer driv, nordlyset bølgjer. |
| **Alt kan rørast** | Kvar ting kan plukkast opp, kastast, givast til nokon eller puttast i noko. Gir kombinasjonen meining for eit barn, skjer det noko. |
| **Oppdaging utan press** | 42 løynde glimt, ønskjebobler, oppskrifter i omn, gryte og verkstad, og dagens pakke – små mål for dei som vil, aldri tidsfrist, poeng eller tap. |
| **Humor overalt** | Prompepute, bananskal, pepar som bles hatten av, kake i fjeset, hikke og rap, kiling, hunden som stel mat. Alle ler med, ingen blir lei seg. |
| **Noko for alle** | Kjøkken og frisør, traktor og verktøy, fiskestang og akebrett, rakett og ekte planetar, tivoli, butikk, lege, konsertscene og havbotn. |
| **Nordisk og litt magisk** | Kjent for norske barn, vanleg nok for alle andre. Trolla er snille og litt tullete. |

**Aldri:** reklame, kjøp i appen, konto, sporing, nedteljing, straff, poengtavle, tekst barnet må lese.

---

## 2. Verda: bygda Trollfoss

Kartet viser bygda ovanfrå med **14 stader** i tre rader: fjella med trollhola og romstasjonen
øvst, dalen med tivoli, butikk, foss, lege, bakeri og gard i midten, og strandlinja med frisør,
heime, konserthus, strand og dykkebøya ytst. Kvar stad er ei brei scene i skrå-3D (sjå §5) som ein
sveipar sidelengs i.

| Stad | Id | Stemning | Hjartet i staden |
| --- | --- | --- | --- |
| **Heime** | `HOME` | Eit lyst, vanleg hus: soverom, stove, kjøkken, bad. | Seng, kiste (hittegods), vedomn, radio (dansemusikk), piano, klokke, kjøleskap, komfyr, badekar, do, postkasse med dagens pakke. |
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

Kvar stad har **tre glimt** (42 i alt): under ting, inni ting, eller dei dukkar opp når du gjer noko
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

**Dyr og vesen:** katt, hund, kanin, **elgkalv**, **lundefugl**, ku, sau, høner, fjordhest og ein drake
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
| Hald inne på ein møbel | **Heimedesignar:** møbelen løftar seg og kan flyttast langs golvet og i djupna, med alt som står på han. |

Møblar er **flater**, **plassar** (stol, seng, akebakke, hoppbakke, badstu), **skap** og **maskinar**.
**Sekken** nede til høgre er korleis ting og figurar reiser mellom stadene.

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
14 stadene (tivolivals, butikkbossa, rolege legetonar, scenepop, drøymande havbotn), lullaby om
natta, dansemusikk frå radioen og discokula, karusellmelodi og instrument i same pentatone skala, så
alt barnet speler høyrest fint ut.

---

## 8. Skjermar

Opning (logoen hoppar inn), stad med knappar i hjørna, kart, figurverkstad, oppdagingsbok og foreldreside
bak eit gongestykke (lyd, musikk, målform, oppdatering, nullstill).

---

## 9. Android og oppdatering

Liggjande på mobil og nettbrett, oppslukande. Kotlin + Jetpack Compose, alt teikna på `Canvas`. Min.
Android 8.0. Nettbrett zoomar ut så minst 2,05 scenebreidder får plass, med meir himmel eller vegg over.
**Spelmotor:** stille møblar og ting blir teikna éin gong per utsjånad til eit bilete og stempla inn
(«sprite cache»); det som rører seg sjølv, blir oppdaga automatisk og teikna levande. Figurane er alltid
levande vektorteikning. Lagring i `files/trollfoss.json`. Oppdatering som Komet: éin signert APK per release på
`oyvhov/trollfoss-android`; appen kontrollerer SHA-256, pakke, versjon og signatur, og ein vaksen
startar installasjonen.

---

## 10. Vegkart

| Versjon | Innhald |
| --- | --- |
| 1.0 | 14 stader i skrå-3D, kart, figurverkstad med namn, fysikk med djupn, vektløyse og symjing, mat, klede, frisør, trollgryte, verkstad og traktor, rakett og planetar, tivoli, butikk, lege, scene og havbotn, ønskjebobler, humor, heimedesignar, 42 glimt, dagens pakke, dag/natt, vêr, sekk, foto, foreldreside, GitHub-oppdatering. |
| 1.1 | Fotoalbum med deling, fleire dyr (sel, katt-ungar), fleire klede og frisyrar. |
| 1.2 | Skule og togstasjon, sesongpynt (jul, 17. mai). |
