param(
    [string]$Out = 'shot',
    [string]$Extras = '',
    [int]$Wait = 14,
    [switch]$Install,
    [string]$Taps = '',
    [int]$TapWait = 2,
    [string]$Out2 = '',
    [string]$Swipe = ''
)
$adb = 'C:\Android\sdk\platform-tools\adb.exe'
$s = 'emulator-5554'
$pkg = 'app.trollfoss.bygg'
$dir = 'C:\trollfoss-wt\bygg\shots'
if ($Install) { & $adb -s $s install -r C:\trollfoss-wt\bygg\app\build\outputs\apk\debug\app-debug.apk | Out-Host }
& $adb -s $s shell am force-stop $pkg
& $adb -s $s shell "am start -f 0x20000000 -n $pkg/app.trollfoss.MainActivity $Extras" | Out-Host
Start-Sleep $Wait
& C:\topa\scripts\Screenshot.ps1 -Out "$dir\$Out.png"
if ($Swipe) {
    & $adb -s $s shell input swipe $Swipe.Split(' ')
    Start-Sleep 2
    & C:\topa\scripts\Screenshot.ps1 -Out "$dir\$Out2.png"
}
if ($Taps) {
    foreach ($t in $Taps.Split(';')) {
        & $adb -s $s shell input tap $t.Split(' ')
        Start-Sleep $TapWait
    }
    if ($Out2) { & C:\topa\scripts\Screenshot.ps1 -Out "$dir\$Out2.png" }
}
& $adb -s $s logcat -d -s TrollfossPerf:D | Select-Object -Last 4 | Out-Host
& $adb -s $s shell am force-stop $pkg
