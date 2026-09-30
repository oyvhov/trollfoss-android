# Topa – designunderlag

> **Topa** er ei lita øy full av liv og løyndommar. Ein digital leikekasse for barn frå 4 til 10 år:
> ingen reglar, ingen poeng, ingen tap – berre figurar, ting og stader som svarar når du rører dei.

Dette dokumentet er fasiten for korleis Topa skal sjå ut, låte og kjennast. Koden følgjer det, og
nye idear blir målte mot det. Sjå òg `docs/AI_INSTRUCTIONS.md` for bygg, test og release.

---

## 1. Visjon og haldning

Topa høyrer til sjangeren *digitalt dukkehus* (som Toca Boca World): barnet flyttar figurar og ting
rundt, finn på historier og oppdagar kva ting gjer. Vi kopierer **ikkje** figurar, stader, namn eller
grafikk frå andre spel. Topa har sin eigen verd, sin eigen figurstil og eigne idear.

**Fire søyler**

| Søyle | Kva det betyr i praksis |
| --- | --- |
| **Alt er levande** | Ingenting står heilt stille. Figurar pustar, blunkar og ser etter fingeren. Ting sprett, skvisar seg når dei landar og snurrar når dei blir kasta. Skyer driv, fuglar flyg, vatnet bølgjer. |
| **Alt kan rørast** | Kvar ting kan plukkast opp, kastast, givast til ein figur, puttast i noko eller prøvast på noko. Om ein kombinasjon gir meining for eit barn, skal han gjere *noko*. |
| **Oppdaging utan press** | Løyndommar (glimt), oppskrifter i trollgryta og dagens pakke gir små mål for dei som vil ha det – utan tidsfrist, poeng eller tap. Dette rettar opp den vanlegaste kritikken av sjangeren: at det blir formålslaust. |
| **Nordisk varme** | Lusekofte, brunost, vaflar, skillingsbollar, nordlys, vikinghjelm og ei hytte i skogen. Kjent for norske barn, eksotisk for andre. |

**Kva Topa aldri gjer:** reklame, kjøp i appen, konto, sporing, nedteljing, straff, poengtavler,
tekst barnet må lese for å kunne leike.

---

## 2. Verda

Topa er ei øy. Kartet er heimeskjermen for reiser; kvar stad er ei brei scene ein kan sveipe
sidelengs i.

| Stad | Id | Stemning og farge | Hjartet i staden |
| --- | --- | --- | --- |
| **Heime** | `HOME` | Fersken og varmt tre. Soverom, stove, kjøkken og bad på rad. | Seng, klesskap, TV, radio (dansemusikk), piano, kjøleskap, komfyr, badekar, do (spyler ting til hittegodskista), postkasse med dagens pakke. |
| **Kafeen** | `CAFE` | Mynte og krem, rutete golv. | Bakaromn (deig → brød, bollar, kake), blender (frukt → smoothie i fruktfargen), fruktkasse og is-maskin som gir nye ting, kakedisk. |
| **Salongen** | `SALON` | Rosa og lilla, terrazzo. | Frisørstolar med spegel, saks, føn, kam, fargesprayar, hattehylle og klesstativ med plagg som byter overdel. |
| **Stranda** | `BEACH` | Himmelblått, turkis sjø, varm sand. | Brygge med fiskeplass, båt, badering, sandslott, parasoll, fyrtårn. Ting flyt eller søkk. |
| **Skogen** | `FOREST` | Djupt grønt, kveldsoransje. | Telt, bål (grille pølse, riste marshmallow), stubbar å sitje på, ugle i treet, sopp, nordlys om natta. |
| **Trolllabben** | `LAB` | Djup lilla med grøne glødar. | Trollgryta (blandar to ting til noko nytt), eliksirar (stor, liten, farge, sveve, tilbake), teleskop, krystallkule, den vesle draken Glo som ristar mat. |
| **Parken** | `PARK` | Graset grønt og himmel. | Huske, sklie, trampoline, fontene (kast ein mynt …), ballongselgar, benk. |

Kvar stad har **tre glimt** – løynde stjerner. Nokre ligg under ting, nokre inni ting, nokre kjem
fram når du gjer noko (fiskar opp ei kiste, kastar mynt i fontena, sløkkjer alle lys om natta).

---

## 3. Figurane – «toparar»

Toparar er runde og mjuke som leiketøy av gummi: stort hovud, kort kropp, korte bein og stutte armar
med runde hender. Dei er ikkje menneskelege kopiar; hud kan vere alt frå ljos til djup brun – og
også blå, grøn eller lilla for troll og vesen.

**Byggjeklossar (figurverkstaden)**

| Del | Val |
| --- | --- |
| Hud | 8 menneskelege tonar + 6 eventyrfargar |
| Høgd | Barn, ungdom, vaksen (0,78 – 1,15) |
| Frisyre | Skalla, kort, pannelugg, krøller, lang, fletter, knute, pigg, bob |
| Hårfarge | 10 fargar, også rosa, blå og grøn |
| Auge | Runde, glade, søvnige, vippe, prikk |
| Øyre | Vanlege, katt, kanin, bjørn, troll (spisse) |
| Overdel | T-skjorte, hettegenser, kjole, stripar, selebukse, **lusekofte** |
| Underdel | Bukse, shorts, skjørt – eigen farge |
| Sko | Eigen farge |
| Ekstra | Fregner, raude kinn, skjegg |

**Dyr og vesen:** katt, hund, kanin og draken Glo. Dei kan plukkast opp, sitje, sove, ete og bere
hatt.

**Liv i figuren**

* Pustar (lett opp og ned), blunkar tilfeldig, ser etter fingeren og etter ting som flyg.
* Plukka opp: overraska andlet, beina sparkar, armane går opp, ein liten «oi!».
* Sleppt: skvis og strekk ved landing, støvpuff, «dunk».
* Trykk: fnis, hopp og nytt andlet. Mange trykk på rad: svimmel.
* Et: gomlar tre bitar (synlege bitemerke i maten), smular, hjarte og «mmm».
* Radio på: figurane i nærleiken dansar i takt.
* I seng om natta: søv med «Zzz».
* Eliksirar: veks, krympar, byter farge eller svevar opp til taket ei stund.
* Kvar figur har si eiga røysthøgd; alle snakkar tulleord laga av syntetiske pip.

---

## 4. Samhandling – verbet er «dra»

Alt skjer med éin finger, men fleire fingrar kan dra fleire ting samtidig (nettbrett med to barn).

| Gest | Verknad |
| --- | --- |
| Dra ting eller figur | Løftar, følgjer fingeren med litt etterheng og helling etter farten. |
| Slepp | Fell med tyngdekraft til næraste flate under (golv, bord, hylle, seng). |
| Slepp med fart | Kast. Ting snurrar, ballar sprett, ballongar stig. |
| Dra på tomt område | Panorer scena. Ting ved kanten av skjermen får scena til å gli. |
| Trykk | Ting og møblar gjer det dei gjer: lampa lyser, TV-en byter kanal, pakken opnar seg. |

**Kva skjer når ein ting blir sleppt på ein figur** (næraste sone vinn):

| Sone | Ting | Resultat |
| --- | --- | --- |
| Munn | Mat, drikke, eliksir | Et, drikk eller får eliksirverknad |
| Hovud | Hatt | Tek på hatten (den gamle dett av) |
| Auge | Briller | Tek på brillene |
| Kropp | Plagg | Byter overdel – det gamle plagget dett av, så det er alltid mogleg å byte tilbake |
| Hår | Saks / føn / kam / spray | Kortare / krøllete / pent / ny hårfarge |
| Hand | Alt anna | Held tingen – han følgjer med når figuren blir flytta og når han reiser |

**Møblar**

* **Flater** (bord, hyller, benkar) tek imot ting.
* **Plassar** (stol, seng, huske, båt, bad) tek imot figurar som snappar på plass med rett positur.
* **Skap** (kjøleskap, klesskap, kiste, telt) opnar og lukkar seg ved trykk; ting inni blir borte
  når det er lukka og er der når du opnar igjen.
* **Maskinar** (komfyr, omn, blender, bål, gryte, fiskeplass) gjer om ting.

**Sekken** (nede til høgre) er korleis ting og figurar reiser: slepp noko på sekken, reis via kartet,
opne sekken og dra det ut. Figurar tek med seg det dei held.

---

## 5. Oppdaging

| System | Korleis |
| --- | --- |
| **Glimt** | 21 løynde stjerner, 3 per stad. Når alle er funne, dukkar det opp ei gullkrone i postkassa. |
| **Oppdagingsboka** | Alt barnet har laga i gryta, omnen, blenderen, bålet og på komfyren blir teikna inn. Tomme ruter viser silhuett – eit hint utan ord. |
| **Dagens pakke** | Éin pakke i postkassa kvar dag (lokal dato, ingen nett). Trykk for å opne med konfetti. |
| **Dag og natt** | Sol/måne-knappen byter med ein mjuk overgang. Om natta: stjerner, nordlys ute, lamper som lyser, figurar som søv. |
| **Vêr** | Sol, regn og snø. Når regnet sluttar, kjem ein regnboge. |

---

## 6. Visuell stil – «mjuk leikekasse»

**Former:** runde hjørne overalt, ingen spisse kantar utan grunn. Alt ser ut som det kan klemmast.

**Strek:** ein varm, mørk blekk-kontur (`Ink #2B2140`) rundt alle figurar, ting og møblar.
Strekbreidda følgjer skjermen (`0,0045 × scenehøgd`), så alt har same tjukkleik uansett storleik.

**Skugge:** to-tone cel-skugge. Grunnfarge, ljosare topp (20 % mot kvitt) og ein mjuk mørkare botn
(15 % mot blekk). Éin liten kvit glans på blanke ting. Mjuk oval skugge på golvet under alt.

**Fargepalett – grensesnitt**

| Token | Hovud | Topp | Djup | Bruk |
| --- | --- | --- | --- | --- |
| `Sun` | `#FFC83D` | `#FFE58A` | `#D98A00` | Hovudhandling, sol, glimt |
| `Berry` | `#FF4D6D` | `#FF8FA3` | `#C21F45` | Raud X (lukk), hjarte |
| `Sea` | `#2F9BFF` | `#7CCBFF` | `#1560C0` | Kart og reise |
| `Mint` | `#2FD18B` | `#86F2BF` | `#14935C` | Lag / ferdig |
| `Grape` | `#8B5CF6` | `#C4A6FF` | `#5B32C9` | Magi, boka |
| `Cream` | `#FFF7EA` | – | `#F3E3C8` | Panel |
| `Ink` | `#2B2140` | – | – | Kontur, tekstkant |
| `Night` | `#1D1A4A` | `#3B2F7A` | – | Natt, foreldresida |

**Stadfargar** er definerte i `ui/art/PlaceArt.kt` og held seg innanfor tre-fire kulørar per stad.

**Tekst:** nesten ingen. Der det finst (logo, foreldreside), brukar vi `GameText`: svært feit
systemskrift med mørk kontur og fall-skugge.

**Grensesnitt i spelet (HUD)** – runde, blanke 3D-knappar (`PressSurface`) i hjørna, minst 64 dp:

```
┌──────────────────────────────────────────────────────────┐
│ (Kart)                              (✦ 5)  (☀/☾)  (☂) (📷)│
│                                                          │
│                     scena – sveip ←→                      │
│                                                          │
│ (Verkstad)                                        (Sekk) │
└──────────────────────────────────────────────────────────┘
```

---

## 7. Rørsle – reglane for animasjon

| Hending | Animasjon | Tid / fjør |
| --- | --- | --- |
| Knappetrykk | Søkk ned på kanten + skalering 0,965 | 70 ms / demping 0,68 |
| Plukk opp | Løft 4 %, skvis 1,08 × 0,92 | fjør, 120 ms |
| Følgje finger | Lerp mot fingeren, helling ±18° etter fart | 18 per sekund |
| Landing | Skvis 1,25 × 0,75 og tilbake med overskot | 260 ms, demping 0,45 |
| Kast | Snurr etter fart, sprett med ball-restitusjon 0,72 | fysikk |
| Idle-pust | Skala-y ±1,5 % | 2,4 s periode |
| Blunk | Auge lukka | 120 ms, kvar 2–6 s |
| Dag ↔ natt | Himmel og lys glir | 1,6 s |
| Reise | Luftballong flyg over kartet, iris-overgang | 1,4 s |
| Glimt funne | Stjerna flyg i boge til teljaren, gnistar | 700 ms |

Alle tidsstyrte rørsler stoppar når Android har slått av animasjonar.

---

## 8. Lyd

* **Effektar** er syntetiserte i kode (ingen lydfiler): pop, dunk, boing, gomle, slurp, fnis,
  magisk glissando, sprut, fres, pling, spyling, klikk, lukkar, konfetti.
* **Røyster:** korte, tonehøgde-styrte tulle-pip. Kvar figur har si eiga høgd.
* **Musikk:** roleg loop per stad laga i kode (same motor som Komet), dempa om natta. Radioen spelar
  eigen dansemusikk.
* Aldri stressande lyd. Ingen alarmar.

---

## 9. Skjermar

| Skjerm | Innhald |
| --- | --- |
| **Opning** | Logoen hoppar inn bokstav for bokstav, ein topar vinkar, deretter rett inn i sist brukte stad. |
| **Stad** | Scena med HUD. |
| **Kart** | Øya ovanfrå, animert sjø, skyer og båt. Trykk på ein stad → ballongen flyg dit. |
| **Figurverkstaden** | Stor figur i midten som reagerer på kvart val; kategoriar som runde ikon; terning for tilfeldig figur; «ferdig» sender figuren heim med konfetti. |
| **Oppdagingsboka** | Glimt per stad, oppskrifter og gåver – bilete, ingen tekst. |
| **Foreldre** | Bak eit gongestykke: lyd, musikk, haptikk, målform, appoppdateringar, nullstill verda, personvern. |

---

## 10. Android

* **Liggjande** (`sensorLandscape`) på både mobil og nettbrett – eit dukkehus er breitt.
  Scena er éi eining høg (skjermhøgda); breidda varierer frå 2,4 til 3,6 einingar.
* **Oppslukande:** systemlinjene er skjulte og kjem fram med sveip.
* Min. Android 8.0 (API 26), mål API 36. Kotlin + Jetpack Compose, all grafikk teikna på `Canvas`.
* Lagring: éi JSON-fil (`files/trollvik.json`), atomisk skriving, autolagring etter endringar.
* **Oppdatering gjennom GitHub:** same kontrakt som Komet. Éin signert universal-APK per release på
  `oyvhov/trollvik-android`, SHA-256 frå GitHub, kontroll av pakke, versjon og signatur før Android får
  spørsmål. Ein vaksen startar nedlastinga på foreldresida.
* Ytelse: éin spel-løkke (`withFrameNanos`) oppdaterer fysikken; éin `Canvas` teiknar scena.
  Mål: 60 fps med 80 ting på eit mellomklassenettbrett.

---

## 11. Tryggleik og personvern

Ingen reklame, analyse, konto eller kjøp. Einaste nettkontakt er oppdateringssjekken mot GitHub,
som kan slåast av. Bilete frå kameraknappen blir verande i appen.

---

## 12. Vegkart

| Versjon | Innhald |
| --- | --- |
| 1.0 | Sju stader, kart, figurverkstad, fysikk, mat, klede, frisør, trollgryte, glimt, dagens pakke, dag/natt, vêr, sekk, foreldreside, GitHub-oppdatering. |
| 1.1 | Heimedesignar (tapet, golv, flytte møblar), fotoalbum med deling. |
| 1.2 | Nye stader: skule, sjukehus, fjellhytte med ski. Sesongpynt (jul, 17. mai). |
