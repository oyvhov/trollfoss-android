# Trollfoss: lokal arbeidsrettleiing

Trollfoss er eit digitalt dukkehus for barn (4–10 år) for Android, laga med Kotlin og Jetpack Compose.
Les `docs/AI_INSTRUCTIONS.md` før arbeid i prosjektet, `docs/DESIGN.md` for verda og `docs/ART_GUIDE.md`
for teiknereglane.

- Bygg og test: `powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollfoss.ps1` (legg til `-Release` for signert APK).
- Emulator: `powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossEmulator.ps1`.
- Signeringsnøkkelen ligg i `.signing/trollfoss-release.jks` med passord i `signing.properties`. Begge er
  Git-ignorerte. **Lag aldri ein ny nøkkel** – då kan appen ikkje lenger oppdaterast.
- Ny APK-release: følg `docs/RELEASE_WORKFLOW.md` (éin universal APK i ein publisert GitHub Release på
  `oyvhov/trollfoss-android`).
- Alt barnet ser av tekst, skal finnast på både nynorsk og bokmål (`Txt(nn, nb)`).
- Stil: vanleg og moderne med lett nordisk preg, ikkje stereotypisk norsk. Grafikk og musikk er laga i kode.
