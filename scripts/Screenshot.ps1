<#
.SYNOPSIS
    Takes a screenshot from an emulator into a PNG file. Use this instead of `adb exec-out screencap -p > file.png`
    in a script: Windows PowerShell 5 writes `>` as UTF-16 and ruins the picture.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\topa\scripts\Screenshot.ps1 -Out C:\trollfoss-wt\stova\shots\hall.png
    # or, inside your own script run under Run-Locked:  & C:\topa\scripts\Screenshot.ps1 -Out "$dir\hall.png"
#>
param(
    [Parameter(Mandatory = $true)][string]$Out,
    [string]$Serial = 'emulator-5554'
)
$adb = 'C:\Android\sdk\platform-tools\adb.exe'
$dir = Split-Path -Parent $Out
if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Force $dir | Out-Null }
cmd.exe /c "`"$adb`" -s $Serial exec-out screencap -p > `"$Out`""
if ((Test-Path $Out) -and ((Get-Item $Out).Length -gt 1000)) { Write-Host "Saved $Out" } else { Write-Host "Screenshot failed: $Out" }
