<#
.SYNOPSIS
    Byggjer og testar Trollvik med same oppsett som Komet brukar på denne maskina.

.DESCRIPTION
    Brukarmappa har «Ø» i namnet, og fleire Android-verktøy tolar ikkje slike stiar i TEMP eller
    Gradle-heimen. Skriptet set difor TEMP/TMP til C:\topa\.gradle-tmp og brukar ein Gradle-heim
    på ein ASCII-sti (standard: den som Spole alt har fylt, så ingenting må lastast ned på nytt).

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollvik.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollvik.ps1 -Release
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollvik.ps1 -PlayStore
#>
param(
    [switch]$Release,
    [switch]$PlayStore,
    [switch]$SkipTests,
    [string]$GradleHome = $(if (Test-Path 'C:\JellyBin\.gradle-home') { 'C:\JellyBin\.gradle-home' } else { 'C:\topa\.gradle-home' })
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$temp = Join-Path $root '.gradle-tmp'
New-Item -ItemType Directory -Force $temp | Out-Null
$env:TEMP = $temp
$env:TMP = $temp

$tasks = @()
if (-not $SkipTests) { $tasks += ':app:testDebugUnitTest' }
$tasks += if ($PlayStore) {
    ':app:bundleRelease'
} elseif ($Release) {
    ':app:assembleRelease'
} else {
    ':app:assembleDebug'
}

$gradleArgs = @('--gradle-user-home', $GradleHome) + $tasks
if ($PlayStore) { $gradleArgs += '-PtrollvikPlayStore=true' }
$gradleArgs += '--console=plain'

Push-Location $root
try {
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle feila med kode $LASTEXITCODE" }
} finally {
    Pop-Location
}

$artifact = if ($PlayStore) {
    Join-Path $root 'app\build\outputs\bundle\release\app-release.aab'
} elseif ($Release) {
    Join-Path $root 'app\build\outputs\apk\release\app-release.apk'
} else {
    Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
}
if (Test-Path $artifact) {
    $hash = (Get-FileHash $artifact -Algorithm SHA256).Hash.ToLowerInvariant()
    $size = [math]::Round((Get-Item $artifact).Length / 1MB, 1)
    $kind = if ($PlayStore) { 'AAB' } else { 'APK' }
    Write-Host "$kind`: $artifact ($size MB)"
    Write-Host "SHA-256: $hash"
}

