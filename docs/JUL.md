# Den store jula i Trollfoss

## Å finne fram i 1.19.1

På startkartet går «Til Nissegrenda» rett til torget. Nisselua i vanlege scener og i Heileberget
opnar reisefana. Frå torget og romma viser «Stader i grenda» dei fire besøksmåla og vegen tilbake
til den opphavlege staden. Spelet lagrar heimvegen når barnet kryssar inn i grenda. Ein lagra leik
frå 1.19.0 der barnet alt er i grenda, har Familiehuset som heimveg.

Dei tre fysiske dørene på torget viser ei nisselue, eit leiketog og eit reinsdyr. På telefon ligg
nisselua framme under vanleg leik. Desse vegane er tilgjengelege uavhengig av kalender og årstid.

## Utviklingshistorikk

Lokal implementasjon 11. november 2026 på `codex/christmas`, i `C:/trollfoss-wt/christmas`.
Bygd på `906bdf2` og ein separat kopi av det eksisterande, uferdige Heileberget-/husarbeidet
(`9e501cb`). Originalane i `C:/topa` og `rainbow-finale` er ikkje endra. Dette er ei lokal
prøveutgåve med pakkenamn `app.trollfoss.christmas`, ikkje ein publisert release.

## Innhald

- 36 møbel-/pyntemodellar i tre palettar: 108 katalogval. Eiga julegruppe i heimedesignaren.
- 36 småting/mat/reiskapar, 16 utkledningsval, 12 tapet og åtte golv: 180 val i grunnpakken.
  Julestrømpe, eigne innpakka gåver og peparkakehus kjem i tillegg som leikeresultat.
- Palettar: raud/grøn/gull, krem/tre/kopar og isblå/kvit/sølv. Alle eksisterande stilindeksar er bevarte.
- Nissegrenda er eitt nytt kartmål, med Nissetunet, Nisseheimen, Juleverkstaden og Reinsdyrtunet.
  Alle område har opne vegar tilbake; dei er tilgjengelege heile året.
- Julenissen, hjelparane Tindra og Lumi, Rampenissen og to reinsdyr, Stjerne og Bjølle.
- 12 nye glimt, tre per område. Fire av glimta kjem etter ei handling.
- Eige originalt musikktema og julepreg i den eksisterande 3D-utgåva av Heileberget.

## Leik

Treet tek imot tolv flyttbare pynteting, med avtakbar toppstjerne og val mellom varmt lys,
fleirfarga lys og sløkt. Nissegrenda sine eigne tre startar pynta; barnet sine gamle rom blir ikkje møblerte om.

Bakebenken: vel resultat → legg deig på benken → dra kjevle og form på deigen → trykk Steik.
Glasur pyntar ferdige peparkaker. Mjølk i bollen gir graut. Peparkakehuset krev tre peparkaker og glasur.
Hobbybordet lagar tre typar papirpynt; snøball blir ei lysande snølykt.
Leikeverkstaden krev to tredelar og ein hammar, og lagar tog, snurrebass eller robot.

Pakkebenken pakkar éin laus ting med valt papir. Opning gir tilbake same ting-ID og tilstand.
Pakker, strømper og figurar kan ikkje pakkast inni pakker. Gåvebandet fraktar faktiske pakkar,
og sleden tek to sitjande figurar og seks pakkar til Nissegrenda, Storhuset, garden eller Familiehuset.

Ønskjekort: trykk for å velje mellom seks bilete, dra kortet til postkassa for å sende.
Julestrømpa tek éin liten ting og gir han tilbake ved trykk. Reinsdyra kan matast og børstast.
Snømann og akebakke bruker eksisterande leikereglar. Speledåsa gir julemusikk og dans; snøkula
lagar ei kort, lokal snøbyge. Julefesten bruker dei eksisterande gjestereglane og kan avsluttast,
slik at inviterte vener kjem heim. Rolf serverer kakao utan å ta noko frå opptatte hender.
Julekista er òg eit gøymestad Sture kan velje på loftet.

20 biletkort viser leikeidéar og fører til rett stad. Tre frivillige eventyrkort i Juleleik og
Leik og eventyr viser lagra framgang: Nissen si bortkomne bjølle, Gjer klart til julefest og
Stjerner over Trollfoss. Rampenissen flyttar seg gjennom tre gøymestader og gir ei utkledning.

## Kalender og årstid

Automatisk jul frå 20. november til og med 6. januar. Juleknappen i verdskontrollane opnar
julekassa, klede, kalender, leik og valet Følg kalenderen / Jul heile året / Utan høgtid.
På mobil ligg knappen i Meir; på kart og nettbrett saman med dag/natt og vêr.
Årstidsvalet er uavhengig, slik at sommarjul også er mogleg. Eksisterande påske og graskartid blir bevarte.

Alle 24 luker kan opnast i valfri rekkjefølgje. Opna luker blir ikkje nullstilte ved årsskiftet.
Seks pynteting, seks bake-/matoverraskingar, seks leiker/utkledningar og seks aktivitetsbilete.
Det finst ingen kjøp, ventetid eller innhaldslåsar.

## Lagring og tekniske kontraktar

`Christmas`, `ChristmasPlaces` og `ChristmasPlay` eig katalog, stadene og reglane.
`ChristmasStore` lagrar opna luker, gøymestad, handlingar og referansar til pakker og strømper.
Alle nye enum-verdiar er lagde til sist. Nye stader bruker ledige 300-ID-blokker frå 4500.
Gamle `festive`-innstillingar blir omsette til automatikk eller avslått høgtid.

Pakkeinnhald bruker negative haldarreferansar, slik at ein ting-ID aldri kan bli forveksla med
ein møbel-ID. Innhaldet blir validert etter at alle kroppane er lasta. Ved sletting av ei pakke
kjem originalen ut att. Pynt og uferdig arbeid følgjer møblar gjennom lager, papirkorg og uthenting;
lagra innhald har ingen aktiv møbelhaldar før det blir henta ut. Dette hindrar at gjenbrukte
møbel-ID-ar stel innhaldet frå eit lagra tre. Innpakking og strømper er med i angrehistoria.
Alle juleting blir verna mot automatisk oppryddingssletting og gamle mengdegrenser.

Teikning ligg i eigne julefiler, med små rutingspunkt i eksisterande motor. Stille møblar og
mønster blir bufra; rørsle er avgrensa. Heileberget får eit lite bufra pyntelag og snøfarge på grøne,
oppovervende terrengflater. Rørsle, kollisjonar og lagringsformatet til fjellet er bevarte.
All ny tekst ligg i `StringsChristmas.kt` som nynorsk/bokmål. Ingen nye nettfunksjonar.

## Verifikasjon og leveranse

Artefaktar og endelege testresultat blir førte i `docs/CHRISTMAS_QA_2026-11-11.md`.
Bygging bruker `Build-Locked.ps1`; emulatorarbeid bruker `Run-Locked.ps1`.
Prøve-APK, test-APK, biletark, loggar og hashar ligg i `C:/topa/dist/christmas`.
Signeringsfiler er urørte. Ingen push, versjonsauke eller publisering.
