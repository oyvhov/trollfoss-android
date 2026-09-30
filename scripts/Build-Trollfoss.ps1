<#
.SYNOPSIS
    Byggjer og testar Trollfoss med same oppsett som Komet brukar på denne maskina.

.DESCRIPTION
    Brukarmappa har «Ø» i namnet, og fleire Android-verktøy tolar ikkje slike stiar i TEMP eller
    Gradle-heimen. Skriptet set difor TEMP/TMP til C:\topa\.gradle-tmp og brukar ein Gradle-heim
    på ein ASCII-sti (standard: den som Komet og Spole alt har fylt, så ingenting må lastast ned på nytt).

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollfoss.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Build-Trollfoss.ps1 -Release
#>
param(
    [switch]$Release,
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
$tasks += if ($Release) { ':app:assembleRelease' } else { ':app:assembleDebug' }

$gradleArgs = @('--gradle-user-home', $GradleHome) + $tasks + @('--console=plain')

Push-Location $root
try {
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle feila med kode $LASTEXITCODE" }
} finally {
    Pop-Location
}

$artifact = if ($Release) {
    Join-Path $root 'app\build\outputs\apk\release\app-release.apk'
} else {
    Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
}
if (Test-Path $artifact) {
    $hash = (Get-FileHash $artifact -Algorithm SHA256).Hash.ToLowerInvariant()
    $size = [math]::Round((Get-Item $artifact).Length / 1MB, 1)
    Write-Host "APK: $artifact ($size MB)"
    Write-Host "SHA-256: $hash"
}
