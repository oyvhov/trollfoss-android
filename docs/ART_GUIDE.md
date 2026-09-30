# Trollvik – teiknerettleiing for kode

All grafikk i Trollvik er teikna i kode på Compose `DrawScope`. Denne rettleiinga er kontrakten for
`ui/art/ThingArt.kt`, `ui/art/FixtureArt.kt`, `ui/art/PlaceArt.kt` og `ui/art/MapArt.kt`. Les òg
`docs/DESIGN.md` (stil og verd) og `ui/art/Ink.kt` og `ui/art/PersonArt.kt` (ferdige døme).

## Stilen: «nordisk leikekasse», kvass og detaljert

* **Ein blekkstrek rundt alt**: `Ink.line` (#2B2140), breidd `pen.lw`. Fine detaljar (treårer, strikkemønster,
  sømmar, spikarhovud, vindaugssprosser) med `pen.thin` eller `pen.lw * 0.5f` i ein mørkare tone av flata.
* **Kvass to-tone skugge**: bruk `inked`, `inkedCircle`, `inkedOval`, `inkedRound`. Dei gir ein hard skuggesigd
  nede til høgre (lys frå oppe til venstre). `shade = false` for små detaljar.
* **Éin liten glans** (`shine`) på blanke ting: glas, metall, frukt, keramikk.
* **Runde hjørne** på alt som er laga av menneske. Naturen kan vere litt kvassare (granbar, fjell, is).
* **Fine detaljar skal lesast i liten storleik**: ein ting er ofte berre 40–60 dp. Maks 3–4 fargar per ting.
* **Ingen tekst** i grafikken. Ingen `Path.op`, ingen uskarpheit, ingen bilete.

### Nordisk palett (i tillegg til `ui/theme/Theme.kt`)

| Namn | Farge | Bruk |
| --- | --- | --- |
| Falunraud | `#B8342B` / ljos `#D2443A` | Trehus, låvar, naust |
| Gran | `#1F7048` / `#2E8B57` | Skog, tre, grøne detaljar |
| Torvtak | `#6FAE5A` | Tak med gras |
| Fjord | `#2F6FB8` / ljos `#5AA9E6` | Sjø, fjordvatn |
| Bjørk | `#F2EEE6` + svarte flekkar | Bjørkestammar, lyse flater |
| Tre | `#C98A55`, mørkt `#A0663B`, ljost `#E3B27A` | Golv, møblar |
| Snø | `#F4F8FF`, skugge `#C9D6EE` | Snø, is |
| Nordlys | `#7CFFB2`, `#D77BFF` | Himmel om natta, magi |
| Rosemaling | raud `#D2443A`, gul `#FFC83D`, grøn `#3BC46B`, blå `#2F6FB8` | Pynt på møblar, ker og skap |

## Koordinatar og kontrakt

Scena er **éi eining høg** (skjermhøgda). `u` = pikslar per eining. Figurar er ca. 0,30 høge (vaksne).

* **Ting** (`drawThing`): origo nedst i midten; teikn innanfor boksen `(-w/2, -h)` til `(w/2, 0)` i pikslar.
  Signaturen skal ikkje endrast. `variant` = farge/smak, `used` = bitar som er tekne (vis bitemerke:
  klipp bort sirklar på høgre side med `clipPath` av ein sti med `PathFillType.EvenOdd`), `cook` = brunfarge.
  Hattar og briller er teikna i storleiken dei har på eit vakse hovud (breidd ~0,155 einingar).
* **Møblar** (`drawFixtureBack`, `drawFixtureFront`): origo i møbelet sitt nedste midtpunkt. Storleik
  `f.spec.w * u` × `f.spec.h * u`. Veggmøblar (`spec.wall`) heng på veggen; y er underkanten.
  Tilstand: `f.open`, `f.on`, `f.mode`, `f.count`, `f.anim` (1 → 0 etter trykk, for sprett),
  `f.angle`, `f.timer`, `f.bob`, `f.taps`. Les `domain/Fixtures.kt` for flater (`surfaces`), plassar
  (`spots`) og innside (`container`): teikninga må passe med desse tala, så ting ligg på hylla og figurar
  sit på setet.
  * Bakre lag: heile møbelet. Skap: lukka dør når `!open`; når `open`: innsida (bakvegg, hyller på
    `surfaces`-høgdene) og døra slått opp til sida. Glasdisk: innsida alltid synleg.
  * Fremre lag (berre når `spec.front` eller glas): dyna over senga, badekarsida, båtskroget,
    tørkehjelmen, glasruta. Det som skal liggje *framfor* figuren som sit/ligg der.
* **Stader** (`drawPlaceBack`, `drawPlaceFront`): skjermkoordinatar. Scenepunkt (x, y) er
  `((x - cam) * u, y * u)`. Fjerne lag rørest med parallakse (`(x - cam * 0.3f) * u`).
  `pen.night` 0–1, `pen.weather`, `pen.rainbow` 0–1. Scena må fylle heile skjermen.

## Animasjon

Bruk `pen.t` (sekund) for rolege loopar: flammar, røyk, bølgjer, nordlys, blad. Rørsla skal vere lita og
jamn. `f.anim` er eit sprett etter trykk: skaler møbelet `1 + f.anim * 0.06` rundt origo eller rist litt.

## Yting

Alt blir teikna 60 gonger i sekundet. Hald det enkelt: ingen store løkker (maks ~200 primitiv per
stadbakgrunn), ingen allokering av store lister, ingen tekst. `Path` per kall er greitt.
