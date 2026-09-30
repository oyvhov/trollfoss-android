<#
.SYNOPSIS
    Startar nettbrettemulatoren frå JellyBin/Spole og installerer Trollfoss.

.DESCRIPTION
    Brukar Spole sin testemulator (Spole_Instrumentation i C:\JellyBin\.spole-test-avds, port 5562)
    i WSL med same nettbrettmål som Spole: 1920 x 1200 / 240 dpi (1280 x 800 dp). Review-emulatoren
    med ekte kontoar (5560) blir aldri rørt. Skjermbilete: -Shot heime.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossTablet.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossTablet.ps1 -SkipInstall -Place FARM -Shot garden
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Start-TrollfossTablet.ps1 -Reset
#>
param(
    [switch]$SkipInstall,
    [string]$Place,
    [string]$Screen = 'play',
    [string]$Shot,
    [switch]$Reset
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$adb = '/home/oyvhov/Android/Sdk/platform-tools/adb'
$serial = 'emulator-5562'

function Linux([string]$command) {
    & wsl.exe -d Ubuntu -u root --exec bash -c $command
}

if ($Reset) {
    Linux "$adb -s $serial shell wm size reset; $adb -s $serial shell wm density reset"
    Write-Host 'Nettbrettmål nullstilte.'
    return
}

$running = Linux "ps -eo args | grep -c '[-]avd Spole_Instrumentation' || true"
if ([int]$running -eq 0) {
    Write-Host 'Startar Spole_Instrumentation i WSL …'
    $logs = Join-Path $root '.gradle-tmp\tablet'
    New-Item -ItemType Directory -Force $logs | Out-Null
    $arguments = @('-d', 'Ubuntu', '-u', 'root', '--exec', '/usr/bin/env', 'ANDROID_AVD_HOME=/mnt/c/JellyBin/.spole-test-avds',
        '/home/oyvhov/Android/Sdk/emulator/emulator', '-avd', 'Spole_Instrumentation', '-port', '5562', '-gpu', 'swiftshader',
        '-memory', '2048', '-no-window', '-no-audio', '-no-snapshot')
    Start-Process wsl.exe -ArgumentList $arguments -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $logs 'out.log') -RedirectStandardError (Join-Path $logs 'err.log') | Out-Null
}

$flag = if ($SkipInstall) { '--no-install' } else { '' }
Linux "bash /mnt/c/topa/scripts/start-tablet-wsl.sh $flag"

if ($Place -or $Shot) {
    $extras = "--es screen $Screen"
    if ($Place) { $extras += " --es place $Place" }
    Linux "$adb -s $serial shell am start -n app.trollfoss.debug/app.trollfoss.MainActivity $extras > /dev/null; sleep 6"
}
if ($Shot) {
    New-Item -ItemType Directory -Force (Join-Path $root 'screenshots') | Out-Null
    Linux "$adb -s $serial exec-out screencap -p > /mnt/c/topa/screenshots/tablet-$Shot.png"
    Write-Host "Skjermbilete: screenshots\tablet-$Shot.png"
}
