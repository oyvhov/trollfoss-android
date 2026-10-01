# Release-flyt for APK og oppdatering i Trollfoss

Same kontrakt som Komet. Publiser berre når brukaren ber om ein ny release.

## Kontrakten appen forventar

- Offentleg repo: `oyvhov/trollfoss-android`. Appen les
  `https://api.github.com/repos/oyvhov/trollfoss-android/releases?per_page=100` utan token.
- Produksjonspakke: `app.trollfoss`, aldri `.debug`.
- `versionName` og tag må samsvare: `1.0.0` og `v1.0.0`. Testutgåver: `1.1.0-beta1` og `v1.1.0-beta1`.
- `versionCode` må vere høgare enn alle APK-ar som er delte. 1.0.0 = 1.
- Same Trollfoss-signatur. Sertifikat SHA-256:
  `de170fe9ae262caea9a7046d80cd3890d3b047e44608e114587f8a9c770fe9f3`.
- Releasen må vere publisert (ikkje kladd) og ha **nøyaktig éin APK**. Legg òg ved `SHA256SUMS.txt`,
  R8-mappinga og `SOURCE_COMMIT.txt` – dei er ikkje APK-ar og påverkar ikkje utvalet.
- GitHub må oppgi `digest: sha256:…` for APK-en. Appen kontrollerer storleik, hash, pakkenamn,
  minste Android-versjon, versjon og signatur før Android får spørsmål om installasjon.
- Testutgåver skal merkast som prerelease og ikkje som «latest».
- Frå 1.0.1 gjer appen utgivingsnotatet om til rein tekst på foreldresida (`update/ReleaseNotes.kt`):
  bilete, HTML og Markdown-teikn (`#`, `**`, `` ` ``) fell bort, `- ` blir `• `, og teksten blir kutta
  mellom ord etter om lag 360 teikn eller 10 liner. Berre starten syner, så skriv det viktigaste først.
- 1.0.0 viser notatet som rå tekst og kuttar etter 1200 teikn, også midt i ord. Så lenge nokon kan ha
  1.0.0, skal dei første linene vere vanleg tekst; legg bilete og overskrifter lenger ned eller utelat dei.

Automatisk sjekk skjer når appen kjem fram, høgst éin gong per tolv timar. «Sjekk no» på foreldresida
går utanom ventetida. Ingenting blir lasta ned eller installert utan at ein vaksen vel det.

## 1. Frys kjelda

1. `git status` og `git diff`. Ta vare på alt arbeid; ingen reset eller clean.
2. Vel ein ubrukt, høgare versjon og versjonskode i `app/build.gradle.kts`.
3. Oppdater `CHANGELOG.md` og `docs/release-vX.Y.Z.md`.
4. Ingen signeringsfiler, passord, emulatordata eller skjermbilete med ekte persondata i Git eller release.

## 2. Bygg og test

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollfoss.ps1 -Release
```

Vent på BUILD SUCCESSFUL. Kontroller skjermane på emulatoren (sjå `docs/AI_INSTRUCTIONS.md`).

## 3. Kontroller og arkiver artefaktane

```powershell
$version='1.0.0' # endre ved neste release
$tag="v$version"
$out="dist/release-$tag"
New-Item -ItemType Directory -Path $out -Force | Out-Null
$apk="$out/Trollfoss-$tag.apk"
$mapping="$out/mapping-$tag.txt"
Copy-Item app/build/outputs/apk/release/app-release.apk $apk
Copy-Item app/build/outputs/mapping/release/mapping.txt $mapping
$bt='C:\Android\sdk\build-tools\36.1.0'
& "$bt\apksigner.bat" verify --print-certs $apk
& "$bt\aapt2.exe" dump badging $apk
$hash=(Get-FileHash $apk -Algorithm SHA256).Hash.ToLower()
"$hash  $(Split-Path $apk -Leaf)" | Set-Content "$out/SHA256SUMS.txt" -Encoding ascii
```

Kontroller sertifikatet over, pakken `app.trollfoss`, versjon og versjonskode, og at
`application-debuggable` ikkje finst. `dist/` er Git-ignorert.

## 4. Commit, tag og kladd

```powershell
git diff --check
git add <gjennomgåtte filer>
git commit -m "Release Trollfoss $version"
$commit=git rev-parse HEAD
$commit | Set-Content "$out/SOURCE_COMMIT.txt" -Encoding ascii
git tag -a $tag -m "Trollfoss $version"
git push --atomic origin main $tag
gh release create $tag $apk "$out/SHA256SUMS.txt" $mapping "$out/SOURCE_COMMIT.txt" --repo oyvhov/trollfoss-android --verify-tag --draft --latest --title "Trollfoss $version" --notes-file "docs/release-$tag.md"
```

Testutgåve: bruk `--prerelease --latest=false`. Flytt aldri ein publisert tag og byt aldri APK bak same
versjon – lag ny versjon ved feil.

Kontroller kladden: `gh release view $tag --repo oyvhov/trollfoss-android --json isDraft,assets` – éin APK,
`state=uploaded`, rett storleik og `digest` lik `sha256:$hash`.

## 5. Publiser og prøv den ekte oppdateringa

```powershell
gh release edit $tag --repo oyvhov/trollfoss-android --draft=false --latest
```

1. Hent release-lista **utan token** og stadfest utgåva, `draft=false` og rett digest.
2. Last ned `browser_download_url` til ei eiga fil og samanlikn SHA-256 med den bygde APK-en.
3. Byggje ei eldre lokal utgåve av same kjelde, med lågare versjonsnamn og lågare versjonskode, og
   installer henne på emulatoren. AGP godtek ikkje versjonskode 0, så frå og med 1.0.1 brukar du førre
   utgåves kode (for 1.0.1: `1` og `1.0.0`):

   ```powershell
   ./gradlew.bat --gradle-user-home C:\JellyBin\.gradle-home assembleRelease "-PtrollfossVersionCode=1" "-PtrollfossVersionName=1.0.0"
   ```

   Denne APK-en skal aldri delast. Bygg den ekte releasen på nytt etterpå (bygga er ikkje bit-identiske;
   den kontrollerte fila er den i `dist/`, ikkje den i `app/build/`).

   Den første utgåva (1.0.0, kode 1) kan ikkje testast heilt: det finst ingen lågare kode. Då testar du
   berre at appen finn utgåva, lastar ned og kontrollerer fila. Han skal avvise henne med «Oppdateringa
   kunne ikkje stadfestast. Ingenting vart installert.» når koden ikkje er høgare enn den installerte.
4. Opne Kart → tannhjulet → gongestykket → Appoppdateringar → **Sjekk no**. Vel **Last ned
   oppdatering** og så **Installer oppdatering**. Gi løyve til å installere frå Trollfoss om Android spør.
5. Opne appen og stadfest ny versjon, og at figurar og ting er bevarte.

Ein `adb install -r`-test er ikkje ein test av oppdateringa gjennom appen.
