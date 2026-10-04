# AI-instruksar for Trollfoss

Arbeidsmanual for ein AI-agent som skal vidareutvikle, teste og byggje Trollfoss. Oppsettet følgjer same
mønster som Komet (`C:\LeseApp`), men appen er eit eige prosjekt med eigen pakke, nøkkel og repo.

## 1. Produktet og faste føringar

Trollfoss er eit digitalt dukkehus for barn (4–10 år): ei bygd under ein stor foss med ni stader. Ingen
reglar, poeng eller tap. Sjå `docs/DESIGN.md` for verda og stilen, og `docs/ART_GUIDE.md` for teikneregler.

- **Vanleg og moderne, med lett nordisk preg.** Ikkje stereotypisk norsk (ingen rosemåling overalt,
  ingen bunad som standard). Brukaren har bede om dette.
- **Namna på figurane** er valde av familien: barn Hedda, Alva, Frida, Velte, Eilev, Olve, Eira, Iver,
  Olvar; vaksne Tuva, Øyvind, Sondre, Elise, Sander, Hilde, Berit, Sølve; eldre BesteSonja og Besten.
  Trollet heiter Rumle.
- **Liggjande skjerm** på mobil og nettbrett. All grafikk, musikk og lyd er laga i kode.
- **Raud X lukkar menyar og dialogar.** Kartet er hovudsida og har ikkje lukkeknapp. Nye dialogar bruker `TrollDialog`.
- **Nesten ingen tekst for barnet.** All tekst finst som `Txt(nn, nb)` i `ui/Strings.kt`.
- **Personvern.** Ingen reklame, kjøp, konto, analyse eller andre nettkall enn oppdateringssjekken.
- **Foreldresida** ligg bak eit gongestykke (6–9 × 6–9).

## 2. Kodestruktur

| Mappe | Innhald |
| --- | --- |
| `domain/` | Rein Kotlin: ting (`Things`), figurar (`People`, `Anatomy`), møblar (`Fixtures`), stader (`Places`), glimt (`Secrets`), oppskrifter (`Recipes`), verda (`World`, `WorldFactory`) og reglane (`Sim`: fysikk, vatn, skap, setar, maskinar). |
| `data/` | `WorldStore` – éi JSON-fil, atomisk skriving. |
| `audio/` | `Synth` (effektar i kode), `SoundFx`, `MusicComposer` og `MusicPlayer`. |
| `ui/art/` | Teikning: `Ink` (stilsettet), `PersonArt`, `ThingArt`, `FixtureArt`, `PlaceArt`, `MapArt`. |
| `ui/play/` | `Engine` (fingrar, kamera, liv, partiklar, lys, vêr, sekk) og `Particles`. |
| `ui/screens/` | Spel, kart, figurverkstad, oppdagingsbok, foreldresider, teleskop. |
| `update/` | GitHub-oppdatering, same kontrakt som Komet. |

Dataflyt: `PlayScreen` køyrer `Engine.update` kvar ramme → `Sim.step` → `Engine.draw`. Endringar kallar
`EngineHost.changed()`, og view-modellen lagrar 1,5 s seinare og når appen går i bakgrunnen.

### Leggje til ein ting, eit møbel eller ein stad

1. Ting: ny verdi i `ThingType` (storleik i scene-einingar) og teikning i `ThingArt`.
2. Møbel: ny `FixtureType` med `FixtureSpec` (flater, plassar, skap, maskin) og teikning i `FixtureArt`.
   Oppførsel ved trykk og slepp i `Sim.tap` og `Sim.dropInto`.
3. Stad: ny `PlaceId`, blåkopi i `Places`, tre glimt i `Secrets`, bakgrunn i `PlaceArt`, `mapSpot` i
   `MapArt`, namn i `S.place` og musikk i `TrollfossViewModel.updateMusic`.
4. Einingstest i `app/src/test`.

## 3. Bygging

Brukarmappa har «Ø» i namnet; skriptet set TEMP til ein ASCII-sti og bruker Gradle-heimen i
`C:\JellyBin\.gradle-home`.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollfoss.ps1            # testar + debug-APK
powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollfoss.ps1 -Release   # testar + signert release
```

- Debug: `app.trollfoss.debug`. Release: `app.trollfoss` (R8 og ressurskrymping).
- Signeringsnøkkelen ligg i `.signing/trollfoss-release.jks` med passord i `signing.properties`. Begge er
  Git-ignorerte. **Lag aldri ein ny nøkkel.** Sertifikat SHA-256:
  `de170fe9ae262caea9a7046d80cd3890d3b047e44608e114587f8a9c770fe9f3`.

## 4. Emulator og visuell kontroll

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossEmulator.ps1
```

Debug-bygget tek imot kommandoar:

```bash
adb shell am start -f 0x20000000 -n app.trollfoss.debug/app.trollfoss.MainActivity --es place space --es night on
```

| Ekstra | Verknad |
| --- | --- |
| `--es place home\|cafe\|salon\|beach\|forest\|lab\|mountain\|farm\|space` | Reiser dit |
| `--es screen map\|creator\|book\|parent\|gate\|play` | Opnar skjermen |
| `--es night on\|off` | Natt eller dag |
| `--es weather sun\|rain\|snow` | Vêr |
| `--ei secrets <n>` | Markerer dei første n glimta som funne |

Skjermbilete: `adb exec-out screencap -p > bilete.png`. Nettbrett: `adb shell wm size 2560x1600` og
`wm density 320` (nullstill etterpå).

## 5. Ferdig-kriterium

- `testDebugUnitTest` er grøn, og CI «Bygg og test» på GitHub er grøn.
- Nye stader, ting og møblar er sjekka på emulatoren i mobil- og nettbrettformat.
- Nynorsk og bokmål er på plass. Ingen persondata, passord eller signeringsfiler i Git.
