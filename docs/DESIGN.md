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
ikkje namn, figurar, stader eller grafikk. Verda er nordisk: raude trehus med torvtak, fjord og foss,
granskog, snøfjell, nordlys, lusekofte og bunad, brunost og vaflar – og troll.

| Søyle | I praksis |
| --- | --- |
| **Alt er levande** | Figurar pustar, blunkar og ser etter fingeren. Ting skvisar seg når dei landar og snurrar når dei blir kasta. Fossen dundrar, skyer driv, nordlyset bølgjer. |
| **Alt kan rørast** | Kvar ting kan plukkast opp, kastast, givast til nokon eller puttast i noko. Gir kombinasjonen meining for eit barn, skjer det noko. |
| **Oppdaging utan press** | 21 løynde glimt, oppskrifter i trollgryta og dagens pakke – små mål for dei som vil, aldri tidsfrist, poeng eller tap. |
| **Nordisk og litt magisk** | Kjent for norske barn, eksotisk for andre. Trolla er snille og litt tullete. |

**Aldri:** reklame, kjøp i appen, konto, sporing, nedteljing, straff, poengtavle, tekst barnet må lese.

---

## 2. Verda: bygda Trollfoss

Kartet viser bygda ovanfrå: fjorden, elva og den store fossen i midten, fjella bak. Kvar stad er ei
brei scene ein sveipar sidelengs i.

| Stad | Id | Stemning | Hjartet i staden |
| --- | --- | --- | --- |
| **Raudstova** | `HOME` | Raudt trehus inni: soverom, stove, kjøkken, bad. | Seng, rosemåla kiste (hittegods), vedomn, radio (dansemusikk), piano, gjøkur, kjøleskap, komfyr, badekar, do, postkasse med dagens pakke. |
| **Bakeriet** | `CAFE` | Mynte og krem, rutete golv. | Omn (deig + eple = kake), blender (frukt = smoothie), fruktkasse, is-maskin, kakedisk, vaflar og skillingsbollar. |
| **Frisøren** | `SALON` | Rosa og lilla, terrazzo. | Frisørstolar, saks, føn, kam, fargesprayar, klesstativ med lusekofte og bunad, hattehylle. |
| **Fiskeværet** | `BEACH` | Sommar ved fjorden, raude rorbuer. | Brygge og fiskeplass (kvar femte fangst er skatt), færing, sandslott, fyrlykt, måkar, lundefugl. |
| **Fossen** | `FOREST` | Granskog ved foten av fossen. | Fossebasseng med regnbogedis, telt, bål (pølser, marshmallow, drakeegg), stubbar, ugle i bjørka, elgkalv, nordlys om natta. |
| **Trollhola** | `LAB` | Grotta bak fossen, krystallar som lyser. | Trollgryta (to ting blir til noko nytt), eliksirar, teleskop, krystallkule, trolldomsbok med hint, draken Glo. |
| **Fjellet** | `MOUNTAIN` | Vinter heile året. Snø, stavkyrkje langt borte. | Akebakke, hoppbakke, snømann, badstu, islagd tjern med ønskehol, kakaobu. |

Kvar stad har **tre glimt**: under ting, inni ting, eller dei dukkar opp når du gjer noko (fiskar opp
ei kiste, hoppar tre gonger i hoppbakken, kastar mynt i ønskeholet, er ved fossen når nordlyset kjem).

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

**Dyr og vesen:** katt, hund, kanin, **elgkalv**, **lundefugl** og draken **Glo**.

**Liv:** pustar, blunkar, ser etter fingeren. Plukka opp: «oi!», sparkar med beina. Sleppt: skvis og
dunk. Trykk: fnis og hopp. Et: gomlar med synlege bitemerke. Radio: dansar. Seng om natta: søv.
Eliksirar: veks, krympar, byter farge, svevar. Kvar figur har si eiga røysthøgd.

---

## 4. Samhandling – verbet er «dra»

| Gest | Verknad |
| --- | --- |
| Dra | Løftar ting eller figur; følgjer fingeren med etterheng og helling. |
| Slepp | Fell til næraste flate under. På ein figur: munn = et, hovud = hatt, auge = briller, kropp = plagg, hår = frisørverktøy, hand = held. |
| Kast | Ting snurrar, ballar sprett, ballongar stig, is glir. |
| Trykk | Lampa lyser, TV-en byter kanal, pakken opnar seg, snømannen veks. |
| Dra på tomt område | Panorer scena. |

Møblar er **flater**, **plassar** (stol, seng, akebakke, hoppbakke, badstu), **skap** og **maskinar**.
**Sekken** nede til høgre er korleis ting og figurar reiser mellom stadene.

---

## 5. Visuell stil – «nordisk leikekasse», kvass og detaljert

* **Former:** runde og mjuke på alt menneskeskapt; naturen kan vere kvassare (granbar, fjell, is).
* **Strek:** varm blekkfarge `#2B2140`, `0,0034 × scenehøgd` – tynnare enn før, så detaljane får plass.
  Fine detaljar (treårer, strikkemønster, rosemåling, sømmar) med halv strek i ein mørkare tone.
* **Skugge:** kvass to-tone cel-skugge: ein hard skuggesigd nede til høgre, lys frå oppe til venstre.
  Éin liten glans på blanke ting.
* **Nordisk palett:** falunraud `#B8342B`, gran `#1F7048`, torvtak `#6FAE5A`, fjord `#2F6FB8`,
  bjørk `#F2EEE6`, tre `#C98A55`, snø `#F4F8FF`, nordlys `#7CFFB2`/`#D77BFF`, rosemåling
  (raud, gul, grøn, blå).
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
knirk, lukkar. Figurane snakkar tulleord i si eiga tonehøgd. Rolege loopar per stad, lullaby om natta,
dansemusikk frå radioen.

---

## 8. Skjermar

Opning (logoen hoppar inn), stad med knappar i hjørna, kart, figurverkstad, oppdagingsbok og foreldreside
bak eit gongestykke (lyd, musikk, målform, oppdatering, nullstill).

---

## 9. Android og oppdatering

Liggjande på mobil og nettbrett, oppslukande. Kotlin + Jetpack Compose, alt teikna på `Canvas`. Min.
Android 8.0. Lagring i `files/trollfoss.json`. Oppdatering som Komet: éin signert APK per release på
`oyvhov/trollfoss-android`; appen kontrollerer SHA-256, pakke, versjon og signatur, og ein vaksen
startar installasjonen.

---

## 10. Vegkart

| Versjon | Innhald |
| --- | --- |
| 1.0 | Sju stader, kart, figurverkstad, fysikk, mat, klede, frisør, trollgryte, glimt, dagens pakke, dag/natt, vêr, sekk, foreldreside, GitHub-oppdatering. |
| 1.1 | Heimedesignar, fotoalbum med deling, fleire dyr og klede. |
| 1.2 | Skule, legekontor og stavkyrkje, sesongpynt (jul, 17. mai). |
