# Det Store Heileberget – eiga 3D-verd

Brukaren valde det andre viste 3D-forslaget 2. november 2026 og bad om å halde fram utan nye stopp.
Visuell referanse: [valt konsept](design/heileberget/selected-concept.png). Fotografiane av fjellvatn,
lange fura berg, bjørkedal og fjord er naturgrunnlaget. Dei private fotografiane er ikkje kopierte inn.

## Oppleving

Ein kan gå fritt i to bakkeretningar og følgje terrenget opp og ned. Kameraet følgjer flokken, kan
snurrast med høgre spake eller eit drag og kan stillast tilbake. Knip med to fingrar for å zoome;
kameradistansen er avgrensa til 4–16 meter, med nærare kollisjonsklaring inne i hola. Å løfte éin
finger etter zoom startar ikkje gåing eller ei kameradreiing. Skjermlesaren har eigne zoomhandlingar.
Venstre spake og trykk på bakken er alternative styringar. Begge spakane kan brukast samstundes;
små utslag gir rolegare gangfart.
Vatnet har ei øy og ei bru; tørre vegar går rundt vatnet, opp til utsiktsplassen og inn i fjellet.
Dei same valde figur-ID-ane og kleda blir brukte. Portretta vel kven som går først, og venneknappen
opnar det eksisterande spelarvalet. Figurar i sekken som er sette på pause, blir ikkje henta utan løyve.

Hola er eit eige romleg område på 40 × 42 meter, med eit stort, snilt troll, kakehyller, Pepsi Max
og Urge, gull og diamantar. Ein kan gå mellom trollstova, den bakre skattehallen og matlageret på
den øvre ganghylla. Peis, lenestol, tepper, tønner, mjølsekker og lykter innreier romma. Inngangen og
utgangen er tovegspassasjar; det er ingen krav om skattar for å få gå inn eller ut.

Ein diamant i steinen tenner spor over brua. Gullet kan leggjast i kista og hentast att. Matlageret
har brus og kake. Vel tingen i tursekken, gå bort til trollet og bruk handa: brus gir først ein slurk,
så rap og promp. Ingen ting blir øydelagde, og ein kan slå av kameraristing. Telefonen sitt val for
redusert rørsle blir òg respektert. Det er ingen poeng, kjøp, nedteljing eller tap.

Ved fjellvatnet og i trollstova står tre ekkosteinar med sol, lauv og måne. Barnet kan trykkje
direkte på steinane eller bruke symbolknappane. Dei seks siste tonane og pausane mellom dei
blir hugsa. Rumle svarar med den same melodien på fuglefløyta si. Lysmøllene samlar seg under
eit kort nordlys. Barnet kan stryke over lufta og vise ein møll kvar han skal flyge. Etter det første
svaret blir ein lysmøll med vidare og kan setje seg på handa. Neste gong barnet helsar på Rumle
med tomme hender, kan dei høyre melodien saman att.

Inne i trollhola held kvardagen fram også når barnet står stille. Rumle undersøkjer keramikkfuglen,
heng han tilbake og legg handa til øyret når noko rører seg i stova. Hòlemusa spring mellom
ulltøffelen og kakesmulane. Eit trykk på musa, nøtta eller handknappen ved reiret lokkar henne fram
med ei lysande eikenøtt. I bakre hall landar vatn frå eit skiferframspring i ei grunn kjelde.
Trykk på vassflata eller ein av dei tre sopphattane. Lyset går frå sopp til sopp og gjellene held
fram med å lyse neste gong familien kjem tilbake. Tursekken viser veg til både reiret og kjelda.
Kameraet kjem nærare når barnet vel ei av handlingane. Eit drag, ein spak eller eit knip gir
straks styringa tilbake. Redusert rørsle beheld den vanlege kameravinkelen.

Trestega høyrer til fjølene framfor kassane i galleriet. Det er mjukare steg på stove- og skatteteppa.
I resten av hola gir stega klang frå stein. Knitring frå grua og nedslaga i kjelda ligg på kvar si
side i lydbiletet når barnet snur kameraet. Lyden blir svakare med avstand og medan fløyta spelar.

Blåbæra ved vatnet kan plukkast i korga, rørast i kopargryta og ausast opp i ein kopp. Trollet
ser mot gjesten, strekkjer fram handa og skålar ved brettet. Begge koppane blir ståande etter besøket.
Tursekk → Blåbærsaft med Rumle → Vis vegen følgjer stiane til neste stopp. Barnet kan når som helst
ta over styringa, gå ein annan stad og kome att. Korga, gryta, den ferdige safta og melodien blir lagra.

Tre krystallklynger ved skattehallen kan spelast som eit lite instrument. Kvar farge har sin tone
og eit mjukt lyssvar; prikkar på soklane skil dei også utan farge. Ein kan spele dei om att utan
krav, poeng eller forbruk. Ute svarar harane på helsing, fuglar flyg mellom trea, og ein kan kaste
smutt ved vatnet eller samle familien ved utsiktsplassen.

Gullpanna ved breidda vuggar medan vatnet glitrar og små gullkorn kjem fram. Den første fullførte
vaskinga gir gullet som kan leggjast i skattkista. Ein kan vaske fleire gonger for leiken si skuld;
det lagar ikkje kopiar av gull som allereie ligg i sekken eller ei kiste. Å gå bort avbryt ei
uferdig vasking utan å miste gullet. Redusert rørsle held panna og vatnet stille.

Dag/natt-valet frå kartet gjeld også ute i Heileberget. Om natta får himmelen stjerner og måne,
landskapet eit lesbart blått lys, og lyktene varme lysfelt på bakken. Hola har sitt eige lune lys.

## Teknisk kontrakt

- `PlayScreen` rutar berre `HEILEBERGET` til den nye native `HighlandsScreen`.
- `GLSurfaceView` / OpenGL ES 2 teiknar ekte 3D-geometri med perspektiv og djupnebuffer. Compose
  teiknar knappar og dialogar. Ingen ny ekstern spelmotor eller nettavhengigheit er lagd til.
- Brukaren bad seinare uttrykkeleg om Blender gjennom MCP etter å ha avvist første grafikkrunde.
  Original kunst blir modellert i Blender 5.2 gjennom MCP, og eksportert frå `art/heileberget`.
  Redigerbar fil: `C:/topa/dist/heileberget-blender/Heileberget.blend`, med scenene Heileberget,
  Trollhola og Eira og Eilev. Den opphavlege ulagrede Blender-økta til brukaren er urørt.
  Den redigerbare modellen for Rumle og dei nye leikene ligg i `art/heileberget/Rumle.blend`.
  `rumle_workshop.py` kan byggje opp att modellen og speleksportane frå den opphavlege fjellfila.
- `Highlands` er felles fasit for høgder, brua, treffprøver, vatngrenser og vegfinning. Geometrien
  må ikkje få ei anna gangflate enn simuleringa. Vegfinning skjer berre ved eit trykk.
- `HighlandsWalk` køyrer på UI-tråden. Rendertråden får uforanderlege bilete av posisjonar/utsjånad
  og eig sjølv GL-program, bufferar og grafikkressursar. Han skriv aldri til spelverda.
- Modellane blir lesne frå lokale ressursar og lasta til GPU éin gong per grafikkontekst.
  Kjeldegzip blir pakka ut av Android sitt byggverktøy og komprimert i APK-en som `.hbg`.
  `HBG2` held posisjonar som float32 og normalar/fargar som normaliserte byte (20 byte per vertex),
  med lesestøtte for det eldre `HBG1`-formatet. AO og lys blir rekna ut ved eksport,
  ikkje på telefonen. Ingen MCP-/Blender-tilkopling eller nettkall finst i appen. Berre aktive figurar
  blir haldne i figurbufferen. Grafikken stoppar på andre skjermar og når appen går i bakgrunnen;
  lågminneeiningar får høgst 30 teikningar i sekundet. Dette er eit tak, ikkje ein målt FPS-lovnad.
  Identiske rammer blir ikkje sende til GPU når sekken pausar verda, eller ved stillstand med
  redusert rørsle. Inndata blir framleis lesne, så gåing og kamerastyring oppdaterer scena.
- Mobil og lågminneeiningar bruker lettare modellar frå same Blender-kjelde. Gjeldande tal og
  hash ligg i ressursmappa sitt `manifest.json`; mobile grenser blir prøvde i Android-testane.
  Gangflater og innhald er felles.
  Både full og lett modell er pakka i APK-en. Den lette modellen er framleis ekte romleg geometri.
- `World.highlands` blir lagra som eit valfritt JSON-felt. Gamle lagringar får startverdiar. Dei gamle
  dukkehuskoordinatane, figurane og tinga blir ikkje erstatta av 3D-koordinatane. Ugyldige koordinatar
  blir flytta til ei trygg gangflate. Tilstanden blir lagra ved handlingar, etter endra posisjon eller
  kameravinkel (også etter at spaken er sleppt) og ved avgang.
- Tekst og tilgjengelege handlingar ligg i `StringsBerg.kt` som `Txt(nn, nb)`.

## Kontroll

Einingstestane prøver at alle stopp kan nåast på tørre, samanhengande gangflater, faktisk fullført
veg til hola, styring relativt til kameraet, tovegspassasje, skattar som kan hentast att, rekkjefølgja
slurk–rap–promp og bevaring av gamle og nye lagringar. Android-prøvene kontrollerer ekte GL-teikning,
grafikkontekst, spak, handlingar, tursekk, reise, bakgrunn/framgrunn og figuridentitetar.

Eira/Eilev/Øyvind sine standardutsjånader bruker artikulerte Blender-modellar med mjuke ovale ansikt.
Figurar med eigne utsjånader bruker valde auge, hår, klesfargar og høgd, med ledda armar, blunk og turveske.
Alle turfigurane ber eit knytt halstørkle med brodert kant. Det lause stykket rører seg når barnet går.
Figuren lyfter blikket mot Rumle og ser ned når lysmøllen landar på handa.
Trollbesøka bruker handposisjonar frå figuren og dei faktiske hankane på koppane. Testar med den
opne spelvisinga prøver både heile saftturen og spelet på steinane.
To harar svarar på helsing utan krav eller forbruk. Ein kan kaste smutt ved vatnet; tre sprett med
ringar og plask, utan poeng eller tidsfrist. Ein blå sommarfugl ved blomane kan kome bort og
setje seg på den ledige handa. Helsinga bruker ikkje ting frå sekken, kan gjentakast og
avbrytast ved gåing, figurbyte eller reise. Redusert rørsle viser ei roleg sitjande stilling.
Møbelgrenser og kamera mot søyler blir prøvde
saman med vandringa. Den nyaste prøveutgåva og kontrollane står først i `OVERLEVERING.md`.
`HEILEBERGET_NATTARBEID_2026-11-02.md` og `design-qa.md` skildrar den førre gjennomgangen av landskapet.

### Spelopptak på Android

I `C:/topa/dist/rumle-3d` ligg `Livet-i-trollhola.mp4` med Rumle, musa og kjelda og
`Heileberget-lysmoll.mp4` med eit spørsmål og svar på fire tonar. Kvart klipp er teke opp
i den køyrande Android-appen med skjermtrykk og spellyd. Opptaka startar ved dei aktuelle
stadene. Android-testane går heile saftturen og turen mellom reiret og kjelda med tursekken.
Bileta i profil-mappene kjem frå den native 3D-flata.

Ved musebesøket fanga Perfetto 161 fullstendige kall til `Heileberget.draw` på nettbrettemulatoren.
Median for teiknekallet var 1,97 ms, og 95-persentilen var 28,14 ms medan skjermopptaket gjekk.
Tala gjeld rendertråden på emulatoren. Biletfrekvens og batteribruk på fysisk utstyr krev ei eiga måling.
