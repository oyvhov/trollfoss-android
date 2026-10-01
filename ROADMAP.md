# Vegkart

Oppdatert 2026-10-01. Detaljert status og kontrollar står i `docs/OVERLEVERING.md`, utgjevne endringar i
`CHANGELOG.md`.

**Retning:** verda er stor nok for no. Dei neste utgåvene gjer det som finst solid og sett på ekte einingar,
og går så i djupna med historier og sesongar før det kjem fleire stader.

## Utgjeve

- [x] **1.0.0** (2026-10-01): 15 stader, 45 glimt, heimedesignar, oppdragstavle, rydding, figurverkstad,
      foreldreside og oppdatering frå GitHub.
- [x] **1.1.0** (2026-10-01): Storhuset (fire etasjar og hage), Mitt hus, årstider og høgtider, kompakte
      kontrollar på mobil, 73 glimt.
- [x] **1.2.0** (2026-10-01): nettbrett-oversikt og sidepanel, Vagstaddalen, breiare kart, samanhengande leik
      (drivhus, kjøken, verkstad).
- [x] **1.2.1** (2026-10-01): årstider og høgtidspynt på kartet, finare kartbygningar, møbelmeny og lager,
      dokkehussnitt over romma, møblar som glir på plass, jamnare bilete og Android 16-tilpassing.

## 1.2.1 – Finare hus, levande kart og lettare møblering

Ingen nye stader. Målet er at alt som finst, er sett, målt og trygt, og at det barnet ser og tek på, verkar.

- [x] Årstidene på kartet (sommar, haust, vinter, vår)
- [x] Møbellageret i sidemenyen oppdaterer seg straks, opnar seg når noko blir lagt i det, og viser kor mange
      ting som ligg der. Trykk på tomme felt i sidemenyen går ikkje lenger gjennom til rommet bak
- [x] Liggjande skjerm også på nettbrett med Android 16 (elles: ståande bilete med mest himmel)
- [x] Alle 24 stadene sett på dag i mobilformat, utan krasj (`screenshots/audit-1.2.1/`)
- [x] Yting, del 1: figurar og møblar i rørsle får nye bilete innanfor ei tidsramme per skjermbilete, dei
      eldste først. Teiknetida på hovudtråden fall 13–70 % i dei tyngste stadene (tal i `docs/OVERLEVERING.md`)
- [x] Høgtidene på kartet: graskar, juletre med pakker og påskeegg ved kvar stad
- [x] Alle 24 stadene sett om natta i nettbrettformat, utan krasj (`screenshots/audit-1.2.1/sheet-night-*.jpg`)
- [x] Møbelmenyen: utstillingsrom for møblane og enkle, lyse flater i lageret
- [x] Kartet: Mitt hus som eit skikkeleg hus (og klar av taubana), Butikken friska opp
- [x] Rom og hus: dokkehus-snitt over taket på nettbrett (loft, etasjen over, flatt tak og himmel) i
      Familiehuset, Bakeriet, Frisøren, Legekontoret, Butikken, Storhuset og Mitt hus (`ui/art/CutawayArt.kt`)
- [x] Møblar set seg på rett plass (`Designer.settle`), og ting sleppt ved sida av eit bord hamnar på det

## Vidare kontroll og finpuss etter 1.2.1

- [ ] Rom og hus, vidare: Scena, Trollhola, Romstasjonen og loftet i Storhuset har ikkje fått snitt over taket;
      på mobil viser berre takbjelken. Romma i Mitt hus er framleis enkle under taket
- [ ] Snapp, vidare: veggane mellom romma gjeld berre Familiehuset; Storhuset og Mitt hus brukar berre
      veggmagnet og nabomøblar
- [ ] Kartet: fleire bygningar brukaren ikkje er nøgd med (spør kva for nokre; Vagstaddalen-hytta står trongt
      ved tunnelen, Legekontoret og Bakeriet er enkle)
- [ ] Yting, del 2: Tivoliet (karusell og pariserhjul blir teikna kvar ramme, om lag 10 ms på hovudtråden) og
      mål på ekte nettbrett. Emulatoren må køyre med `-gpu host`; utan vindauge vel han elles
      programvare-grafikk og målingane blir meiningslause
- [ ] Ekte oppdateringstest gjennom appen, frå 1.1.0 til 1.2.0 (`docs/RELEASE_WORKFLOW.md` §5; aldri gjort.
      1.1.0-APK-en ligg i `dist/update-test-v1.2.0/`)
- [ ] Prøv på ekte nettbrett og telefon. Avklar oppstartsfrysinga (FocusEvent-ANR) som til no berre er sett
      i grafikkemulatoren
- [ ] Lagringstestar: ekte lagringsfiler frå 1.0.0, 1.1.0 og 1.2.0 som testdata, så ingen barn mistar verda
      si ved oppdatering
- [ ] Sjå appen med stor skrift
- [ ] Prøv skjermbilettestar på JVM (til dømes Roborazzi), så romma kan kontrollerast utan emulator
- [ ] Kort ned `docs/OVERLEVERING.md` til gjeldande status

## Leiketest med barn

- [ ] Tjue minutt med eitt eller to barn, berre sjå på: kva ler dei av, kvar stoppar dei, kva kjem dei
      tilbake til? Funna styrer rekkjefølgja under.

## 1.3 – Det skjer noko i bygda

Historier som gir grunn til å reise mellom stadene og kome tilbake dagen etter, alltid med ei morosam
utbetaling. Design først, så bygging.

- [ ] `domain/Stories.kt`: `Story` med steg (`Deed` + stad + bilete), lagra i `World`
- [ ] Kort-rad i oppdragstavla og «!»-markør over figuren som gir neste steg
- [ ] Første historie: Rumle har mista krona
- [ ] Deretter: geita som rømde frå Heileberget, husmysteriet med Sture og dei fem nøklane, Rolf si store
      husfest

## Sesongar (kalenderen er fristen)

- [ ] Gresskartid: ferdig sett og utgjeve før slutten av oktober
- [ ] Jul frå 1. desember: julekalender med 24 luker oppå «dagens pakke» i postkassa
- [ ] Sesonggrafikk for Storhuset, Mitt hus, Vagstaddalen og kartet
- [ ] 17. mai

## Seinare

- Garderobe: fleire klede, frisyrar og drakter, kostyme frå loftkista
- Nye dyr (rev, piggsvin, ekorn)
- Del bilete frå fotoalbumet
- Fleire tomter og bygningar etter Mitt hus
- Skule

## Arbeidsreglar

- Ingen nye stader før ytingsmålet er nådd.
- Ingenting er ferdig før det er sett på mobil og nettbrett, dag og natt.
- Kvar ny ting får ein morosam og snill reaksjon.
- Aldri: reklame, kjøp i appen, konto, sporing, nedteljing, straff, poengtavle, tekst barnet må lese.

## Ope val

- **Distribusjon:** GitHub-APK for familie og vener (som no), eller Google Play? Play krev ein eigen
  byggvariant utan sjølvoppdateraren, personvernerklæring og ein testperiode, og bør i så fall kome rett
  etter 1.2.1.
