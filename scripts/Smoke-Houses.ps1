$adb = 'C:\Android\sdk\platform-tools\adb.exe'
$s = 'emulator-5554'
$out = 'C:\topa\screenshots\smoke'
New-Item -ItemType Directory -Force $out | Out-Null
& $adb -s $s install -r C:\topa\app\build\outputs\apk\debug\app-debug.apk | Select-Object -Last 1
& $adb -s $s shell pm clear app.trollfoss.debug | Out-Null
& $adb -s $s logcat -c
& $adb -s $s shell am start -f 0x20000000 -n app.trollfoss.debug/app.trollfoss.MainActivity | Out-Null
Start-Sleep 28
& C:\topa\scripts\Screenshot.ps1 -Out "$out\newgame.png"
$crash = & $adb -s $s logcat -d | Select-String 'FATAL EXCEPTION|ANR in'
"newgame crash lines: $(@($crash).Count)"
foreach ($p in 'manor_ground', 'manor_upper', 'manor_attic', 'manor_cellar', 'manor_garden', 'mine_yard', 'mine_ground') {
    & $adb -s $s shell am force-stop app.trollfoss.debug
    & $adb -s $s logcat -c
    & $adb -s $s shell am start -f 0x20000000 -n app.trollfoss.debug/app.trollfoss.MainActivity --es place $p --es night off --es weather SUN | Out-Null
    Start-Sleep 18
    & C:\topa\scripts\Screenshot.ps1 -Out "$out\$p.png"
    $crash = & $adb -s $s logcat -d | Select-String 'FATAL EXCEPTION|ANR in|AndroidRuntime'
    "$p crash lines: $(@($crash).Count)"
    if (@($crash).Count -gt 0) { $crash | Select-Object -First 4 | ForEach-Object { $_.Line.Substring(0, [Math]::Min(200, $_.Line.Length)) } }
}
& $adb -s $s shell am force-stop app.trollfoss.debug
"done"
