param([switch]$Baseline, [switch]$Full, [ValidateSet('phone','tablet')][string]$Only, [switch]$SkipInstall)
$ErrorActionPreference = 'Stop'
$adb = 'C:/Android/sdk/platform-tools/adb.exe'
$repo = Split-Path $PSScriptRoot -Parent
$out = 'C:/topa/dist/levande-figurar'
New-Item -ItemType Directory -Force -Path $out | Out-Null
function Adb([string[]]$Arguments) {
    & $adb -s emulator-5554 @Arguments
    if ($LASTEXITCODE -ne 0) { throw "adb failed: $($Arguments -join ' ')" }
}
if (!$SkipInstall) {
    Adb @('install','-r',"$repo/app/build/outputs/apk/debug/app-debug.apk")
    Adb @('install','-r',"$repo/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk")
}
$oldSize = (Adb @('shell','wm','size')) -join "`n"
$oldDensity = (Adb @('shell','wm','density')) -join "`n"
try {
    $formats = if ($Baseline) { @('baseline') } elseif ($Only) { @($Only) } else { @('phone','tablet') }
    foreach ($format in $formats) {
        if ($format -eq 'phone') { Adb @('shell','wm','size','2400x1080'); Adb @('shell','wm','density','420') }
        if ($format -eq 'tablet') { Adb @('shell','wm','size','1920x1200'); Adb @('shell','wm','density','240') }
        $args = @('shell','am','instrument','-w','-r')
        if (!$Full) { $args += @('-e','class','app.trollfoss.ui.art.FigureStyleArtTest') }
        $args += 'app.trollfoss.figurar.test/androidx.test.runner.AndroidJUnitRunner'
        $result = Adb $args
        $result | Set-Content -Encoding UTF8 "$out/$format-tests.txt"
        $result | Select-Object -Last 10
        New-Item -ItemType Directory -Force -Path "$out/$format" | Out-Null
        Adb @('pull','/sdcard/Android/data/app.trollfoss.figurar/files/figure-style/.',"$out/$format")
        if (($result -join "`n") -notmatch 'OK \(\d+ tests?\)') { throw "Tests did not pass: $format" }
    }
} finally {
    if ($oldSize -match 'Override size: (\d+x\d+)') { Adb @('shell','wm','size',$Matches[1]) } else { Adb @('shell','wm','size','reset') }
    if ($oldDensity -match 'Override density: (\d+)') { Adb @('shell','wm','density',$Matches[1]) } else { Adb @('shell','wm','density','reset') }
}
