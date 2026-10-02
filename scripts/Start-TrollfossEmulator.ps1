<#
.SYNOPSIS
    Startar Android-emulatoren (felles AVD eller eksisterande køyring) og installerer siste debug-APK.

.DESCRIPTION
    Trollfoss kan nytte den felles emulatoren i C:\Android\avd (t.d. Tunet_Ascii) eller kople
    seg direkte til ein emulator som allereie køyrer. Emulatoren blir starta gjennom
    ASCII-koplinga C:\Android\sdk for å unngå problem med spesialteikn i brukarnamnet.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossEmulator.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossEmulator.ps1 -Headless
#>
param(
    [string]$AvdName = 'Tunet_Ascii',
    [string]$AvdHome = 'C:\Android\avd',
    [switch]$Headless,
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$sdk = 'C:\Android\sdk'
$adb = Join-Path $sdk 'platform-tools\adb.exe'

$env:ANDROID_AVD_HOME = $AvdHome
$env:ANDROID_SDK_ROOT = $sdk
$env:ANDROID_HOME = $sdk

# Sjekk om det allereie køyrer ein emulator
$devices = @((& $adb devices) | Where-Object { $_ -match '^emulator-(\d+)\s+device$' })
$serial = $null

if ($devices) {
    $serial = ($devices[0] -split '\s+')[0]
    Write-Host "Bruker allereie køyrende emulator: $serial"
} else {
    Write-Host "Ingen køyrende emulator funnen. Startar $AvdName frå $AvdHome …"
    $arguments = @('-avd', $AvdName, '-no-boot-anim', '-gpu', 'auto')
    if ($Headless) { $arguments += @('-no-window', '-no-audio') }

    $logDir = 'C:\Android'
    Start-Process -FilePath (Join-Path $sdk 'emulator\emulator.exe') -ArgumentList $arguments `
        -RedirectStandardOutput (Join-Path $logDir 'emulator-out.log') `
        -RedirectStandardError (Join-Path $logDir 'emulator-err.log') -WindowStyle Hidden | Out-Null

    Write-Host 'Ventar på at emulatoren skal starte …'
    & $adb wait-for-device
    $devices = @((& $adb devices) | Where-Object { $_ -match '^emulator-(\d+)' })
    if ($devices) {
        $serial = ($devices[0] -split '\s+')[0]
    } else {
        $serial = 'emulator-5554'
    }

    do {
        Start-Sleep -Seconds 3
        $booted = ((& $adb -s $serial shell getprop sys.boot_completed) -join '').Trim()
    } until ($booted -eq '1')
    Write-Host "Emulatoren er klar: $serial"
}

if (-not $SkipInstall) {
    $apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
    if (Test-Path $apk) {
        Write-Host "Installerer $apk på $serial …"
        & $adb -s $serial install -r $apk
        & $adb -s $serial shell monkey -p app.Trollfoss.debug -c android.intent.category.LAUNCHER 1 | Out-Null
    } else {
        Write-Host "Fann ikkje debug-APK ($apk). Bygg først med Build-Trollfoss.ps1"
    }
}
Write-Host "Ferdig: $serial"

