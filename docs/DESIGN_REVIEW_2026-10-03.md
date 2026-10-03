# Designgjennomgang – 3. oktober 2026

Brukaren bad om vidare designarbeid og ei konkret vurdering av kva som kan bli finare.
Product Design Audit og Design Critique er brukte med ferske skjermbilete frå eiga
`app.trollfoss.maplayout`-prøveverd. Nettbrett 1920 × 1200 / 240 dpi er hovudflata.
Full lokal rapport med alle bileta: `C:/topa/screenshots/design-2026-10-03/index.html`.
Skjermbileta er Git-ignorerte. Dei er ikkje private brukardata.

## Fem vurderte steg

1. **Kart – godt grunnlag, middels visuell ro.** Flytande knappar gir heile kartflata tilbake.
   Mange like sterke omriss og kvite stadnamn konkurrerer med landemerka. Neste runde bør dempe
   namna og vise valt reisemål tydelegare. Bilete `01-map-before.png`.
2. **Familiehuset – fin verd, travel knapperad.** Varme materiale og romveljaren fungerer godt.
   Sekundærknappane langs toppen konkurrerer om merksemda. Tomt rom i biletet kjem av at ingen
   spelar var vald i prøveverda. Bilete `02-room-before.png`.
3. **Spelarval – tydeleg betre etter denne runden.** Før hadde alle kort tunge svarte kantar,
   og all tekst hadde tjukt omriss. No er figurane større, namna reine og markeringa av laget
   tydeleg: varm flate, lilla ramme, hake og spelarnummer. Den tomme lagrada gir kort rettleiing
   på begge målformer. Bilete `03-players-before.png`, `03b-team-before.png`, `06-players-after.png`
   og `08-empty-team-after.png`.
4. **Figurverkstad – meir samanhengande.** Stor figur og direkte førehandsvising fungerer godt.
   Ei lys spegelramme gir figuren ei eiga flate; rolege kantar og lilla valmarkering bind menyen saman.
   Ferdig-knappen er samla til høgre. Valstatus er også lagd til i Compose-semantikken for
   figur, kategori og stil. Før-biletet viser ein tilfeldig ny figur; etter viser Hedda.
   Bilete `04-creator-before.png` og `07-creator-after.png`.
5. **Gåver – lesbar, men neste nivå er for langt nede.** Store illustrasjonar og Prøv på kvart kort
   fungerer. Ti tilgjengelege gåver skyv nivå 2 ut av første skjerm. Neste runde bør vise den neste
   gåva saman med framdrifta før samlinga. Bilete `05-gifts-before.png`.

## Kontroll

- Debug-bygg og 467 einingstestar er grøne. Endeleg lint og GitHub-kontroll: sjå PR 4.
- Reinstallasjon bevarte Hedda og Alva som valde spelarar. Opning av Hedda og Ferdig tilbake
  fungerer. Begge kan veljast vekk og veljast att; det tomme laget gir hint.
- Dei to endra skjermane er inspiserte på nettbrett og kort på mobil (2400 × 1080 / 420 dpi),
  bilete `09-phone-players-after.png` og `10-phone-creator-after.png`. Listene rullar på kort skjerm.
- Spelreglar, lagringsformat, versjonsnummer og signeringsnøkkel er uendra. Ingen release.

## Avgrensingar og vidare retning

Dette er ikkje ein barnetest eller ein full tilgjengekontroll. TalkBack, brytarstyring,
stor skrift, lyd og to samtidige fingrar er ikkje prøvde her. Svake kortrammer er dekor;
val har i tillegg hake/spelarnummer. Kompakte knappar og forståing av ikona treng barnetest.
Figurverkstaden har framleis lite høgd til alternativa på mobil, sjølv om dei er rullbare.

Prioriter vidare: rolege kartnamn og tydeleg stadmarkør; samla romknappar med svakare sekundærval;
neste gåve synleg øvst; betre utnytting av høgda i figurverkstaden på korte skjermar.
Bevar den teikna verda og figurane. Ikkje erstatt dei med generiske ikon eller rasterbilete.
